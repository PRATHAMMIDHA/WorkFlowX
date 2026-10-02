package com.workflowx.task.specification;

import com.workflowx.common.enums.TaskPriority;
import com.workflowx.common.enums.TaskStatus;
import com.workflowx.task.dto.TaskFilterRequest;
import com.workflowx.task.entity.Task;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskSpecification — builds dynamic JPA Criteria predicates from filter parameters.
 *
 * WHY use Specifications instead of multiple repository methods?
 * With 6+ filter fields you'd need 2^6 = 64 combinations of repository methods.
 * Specifications let you combine predicates dynamically at runtime.
 * The JPA Criteria API is type-safe and database-agnostic.
 *
 * Pattern: each field is checked for null. If null → predicate is skipped.
 * This gives true optional filtering behaviour.
 */
public class TaskSpecification {

    public static Specification<Task> buildSpec(TaskFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ── Project filter (required in practice, optional in spec) ──
            if (filter.getProjectId() != null) {
                predicates.add(cb.equal(root.get("project").get("id"), filter.getProjectId()));
            }

            // ── Sprint filter ────────────────────────────────────────────
            if (filter.getSprintId() != null) {
                predicates.add(cb.equal(root.get("sprint").get("id"), filter.getSprintId()));
            }

            // ── Assignee filter ──────────────────────────────────────────
            if (filter.getAssigneeId() != null) {
                predicates.add(cb.equal(root.get("assignee").get("id"), filter.getAssigneeId()));
            }

            // ── Status filter ────────────────────────────────────────────
            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                try {
                    TaskStatus status = TaskStatus.valueOf(filter.getStatus().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException ignored) {}
            }

            // ── Priority filter ──────────────────────────────────────────
            if (filter.getPriority() != null && !filter.getPriority().isBlank()) {
                try {
                    TaskPriority priority = TaskPriority.valueOf(filter.getPriority().toUpperCase());
                    predicates.add(cb.equal(root.get("priority"), priority));
                } catch (IllegalArgumentException ignored) {}
            }

            // ── Keyword search (title OR description) ────────────────────
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String pattern = "%" + filter.getKeyword().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(titleMatch, descMatch));
            }

            // ── Due date range filter ─────────────────────────────────────
            if (filter.getDueDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), filter.getDueDateFrom()));
            }
            if (filter.getDueDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), filter.getDueDateTo()));
            }

            // Avoid duplicate rows when joining collections
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
