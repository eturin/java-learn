package ture.bank.repository.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import ture.bank.dto.*;
import ture.bank.entity.Role;
import ture.bank.entity.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserSpecifications {

    public static Specification<User> withCriteria(UserSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Добавляем фильтры по логину
            if (criteria.getLogin() != null && !criteria.getLogin().isEmpty()) {
                Predicate loginPredicate = buildStringPredicates(
                        criteria.getLogin(),
                        root.get("login"),
                        cb
                );
                if (loginPredicate != null) {
                    predicates.add(loginPredicate);
                }
            }

            // Добавляем фильтры по ФИО
            if (criteria.getFio() != null && !criteria.getFio().isEmpty()) {
                Predicate fioPredicate = buildStringPredicates(
                        criteria.getFio(),
                        root.get("fio"),
                        cb
                );
                if (fioPredicate != null) {
                    predicates.add(fioPredicate);
                }
            }

            // Добавляем фильтры по роли
            if (criteria.getRole() != null && !criteria.getRole().isEmpty()) {
                Join<User, Role> roleJoin = root.join("role", JoinType.INNER);
                Predicate rolePredicate = buildRolePredicates(
                        criteria.getRole(),
                        roleJoin,
                        cb
                );
                if (rolePredicate != null) {
                    predicates.add(rolePredicate);
                }
            }

            // Добавляем фильтр по дате создания
            if (criteria.getCreatedAt() != null) {
                Predicate datePredicate = buildDatePredicate(
                        criteria.getCreatedAt(),
                        root.get("createdAt"),
                        cb
                );
                if (datePredicate != null) {
                    predicates.add(datePredicate);
                }
            }

            // Добавляем фильтр по дате обновления
            if (criteria.getUpdatedAt() != null) {
                Predicate datePredicate = buildDatePredicate(
                        criteria.getUpdatedAt(),
                        root.get("updatedAt"),
                        cb
                );
                if (datePredicate != null) {
                    predicates.add(datePredicate);
                }
            }

            // Добавляем фильтр по удаленным пользователям
            if (criteria.getDeleted() != null) {
                if (criteria.getDeleted()) {
                    predicates.add(cb.isNotNull(root.get("deletedAt")));
                } else {
                    predicates.add(cb.isNull(root.get("deletedAt")));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate buildStringPredicates(
            List<StringFilter> filters,
            Path<String> path,
            CriteriaBuilder cb
    ) {
        List<Predicate> predicates = new ArrayList<>();

        for (int i = 0; i < filters.size(); i++) {
            StringFilter filter = filters.get(i);
            if (filter.getValue() == null || filter.getValue().trim().isEmpty()) {
                continue;
            }

            Predicate predicate = buildStringPredicate(filter, path, cb);
            if (predicate != null) {
                predicates.add(predicate);
            }
        }

        if (predicates.isEmpty()) {
            return null;
        }

        // Объединяем предикаты с учетом логических операторов
        Predicate result = predicates.get(0);
        for (int i = 1; i < predicates.size(); i++) {
            if (filters.get(i).getLogicalOperator() == StringFilter.LogicalOperator.OR) {
                result = cb.or(result, predicates.get(i));
            } else {
                result = cb.and(result, predicates.get(i));
            }
        }

        return result;
    }

    private static Predicate buildStringPredicate(
            StringFilter filter,
            Path<String> path,
            CriteriaBuilder cb
    ) {
        String value = filter.getValue().trim();

        switch (filter.getOperator()) {
            case EQUALS:
                return cb.equal(cb.lower(path), value.toLowerCase());

            case NOT_EQUALS:
                return cb.notEqual(cb.lower(path), value.toLowerCase());

            case CONTAINS:
                return cb.like(cb.lower(path), "%" + value.toLowerCase() + "%");

            case STARTS_WITH:
                return cb.like(cb.lower(path), value.toLowerCase() + "%");

            case ENDS_WITH:
                return cb.like(cb.lower(path), "%" + value.toLowerCase());

            case IN:
                List<String> values = Arrays.asList(value.split(","));
                return cb.lower(path).in(
                        values.stream()
                                .map(String::trim)
                                .map(String::toLowerCase)
                                .toArray()
                );

            case NOT_IN:
                List<String> notInValues = Arrays.asList(value.split(","));
                return cb.not(cb.lower(path).in(
                        notInValues.stream()
                                .map(String::trim)
                                .map(String::toLowerCase)
                                .toArray()
                ));

            default:
                return cb.equal(cb.lower(path), value.toLowerCase());
        }
    }

    private static Predicate buildRolePredicates(
            List<RoleFilter> filters,
            Join<User, Role> roleJoin,
            CriteriaBuilder cb
    ) {
        List<Predicate> predicates = new ArrayList<>();

        for (int i = 0; i < filters.size(); i++) {
            RoleFilter filter = filters.get(i);
            Predicate predicate = buildRolePredicate(filter, roleJoin, cb);
            if (predicate != null) {
                predicates.add(predicate);
            }
        }

        if (predicates.isEmpty()) {
            return null;
        }

        // Объединяем предикаты
        Predicate result = predicates.get(0);
        for (int i = 1; i < predicates.size(); i++) {
            if (filters.get(i).getLogicalOperator() == RoleFilter.LogicalOperator.OR) {
                result = cb.or(result, predicates.get(i));
            } else {
                result = cb.and(result, predicates.get(i));
            }
        }

        return result;
    }

    private static Predicate buildRolePredicate(
            RoleFilter filter,
            Join<User, Role> roleJoin,
            CriteriaBuilder cb
    ) {
        if (filter.getId() != null) {
            switch (filter.getOperator()) {
                case EQUALS:
                    return cb.equal(roleJoin.get("id"), filter.getId());
                case NOT_EQUALS:
                    return cb.notEqual(roleJoin.get("id"), filter.getId());
                default:
                    return cb.equal(roleJoin.get("id"), filter.getId());
            }
        } else if (filter.getName() != null) {
            switch (filter.getOperator()) {
                case EQUALS:
                    return cb.equal(cb.lower(roleJoin.get("name")), filter.getName().toLowerCase());
                case NOT_EQUALS:
                    return cb.notEqual(cb.lower(roleJoin.get("name")), filter.getName().toLowerCase());
                default:
                    return cb.equal(cb.lower(roleJoin.get("name")), filter.getName().toLowerCase());
            }
        } else if (filter.getNameFilter() != null) {
            return buildStringPredicate(filter.getNameFilter(), roleJoin.get("name"), cb);
        }

        return null;
    }

    private static Predicate buildDatePredicate(
            DateFilter filter,
            Path<Instant> path,
            CriteriaBuilder cb
    ) {
        switch (filter.getOperator()) {
            case EQUALS:
                return cb.equal(path, filter.getFrom());

            case NOT_EQUALS:
                return cb.notEqual(path, filter.getFrom());

            case GREATER_THAN:
                return cb.greaterThan(path, filter.getFrom());

            case LESS_THAN:
                return cb.lessThan(path, filter.getFrom());

            case BETWEEN:
                if (filter.getFrom() != null && filter.getTo() != null) {
                    return cb.between(path, filter.getFrom(), filter.getTo());
                } else if (filter.getFrom() != null) {
                    return cb.greaterThanOrEqualTo(path, filter.getFrom());
                } else if (filter.getTo() != null) {
                    return cb.lessThanOrEqualTo(path, filter.getTo());
                }
                return null;

            default:
                return cb.equal(path, filter.getFrom());
        }
    }
}
