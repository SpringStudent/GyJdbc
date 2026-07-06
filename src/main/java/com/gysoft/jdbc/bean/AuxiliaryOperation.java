package com.gysoft.jdbc.bean;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author DJZ-HXF
 */
public interface AuxiliaryOperation<S extends AuxiliaryOperation<S>> {

    S like(String key, Object value);

    S likeR(String key, Object value);

    S likeL(String key, Object value);

    <T, R> S like(TypeFunction<T, R> function, Object value);

    <T, R> S likeR(TypeFunction<T, R> function, Object value);

    <T, R> S likeL(TypeFunction<T, R> function, Object value);

    S and(String key, Object value);

    <T, R> S and(TypeFunction<T, R> function, Object value);

    S or(String key, Object value);

    <T, R> S or(TypeFunction<T, R> function, Object value);

    S orLike(String key, Object value);

    <T, R> S orLike(TypeFunction<T, R> function, Object value);

    S in(String key, Collection<?> args);

    <T, R> S in(TypeFunction<T, R> function, Collection<?> args);

    S notIn(String key, Collection<?> args);

    <T, R> S notIn(TypeFunction<T, R> function, Collection<?> args);

    S gt(String key, Object value);

    <T, R> S gt(TypeFunction<T, R> function, Object value);

    S gte(String key, Object value);

    <T, R> S gte(TypeFunction<T, R> function, Object value);

    S lt(String key, Object value);

    <T, R> S lt(TypeFunction<T, R> function, Object value);

    S let(String key, Object value);

    <T, R> S let(TypeFunction<T, R> function, Object value);

    S doNothing();

    S notEqual(String key, Object value);

    <T, R> S notEqual(TypeFunction<T, R> function, Object value);

    S betweenAnd(String key, Object v1, Object v2);

    <T, R> S betweenAnd(TypeFunction<T, R> function, Object v1, Object v2);

    S orBetweenAnd(String key, Object v1, Object v2);

    <T, R> S orBetweenAnd(TypeFunction<T, R> function, Object v1, Object v2);

    S and(String key, String opt, Object value);

    <T, R> S and(TypeFunction<T, R> function, String opt, Object value);

    S or(String key, String opt, Object value);

    <T, R> S or(TypeFunction<T, R> function, String opt, Object value);

    S where(String key, Object value);

    <T, R> S where(TypeFunction<T, R> function, Object value);

    S where(String key, String opt, Object value);

    <T, R> S where(TypeFunction<T, R> function, String opt, Object value);

    default S likeIfAbsent(String key, Object value) {
        return likeIfAbsent(key, value, getDefaultPredicate());
    }

    default S likeIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return like(key, value);
        }
        return doNothing();
    }

    default S likeLIfAbsent(String key, Object value) {
        return likeLIfAbsent(key, value, getDefaultPredicate());
    }

    default S likeLIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return likeL(key, value);
        }
        return doNothing();
    }

    default S likeRIfAbsent(String key, Object value) {
        return likeRIfAbsent(key, value, getDefaultPredicate());
    }

    default S likeRIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return likeR(key, value);
        }
        return doNothing();
    }

    default <T, R> S likeIfAbsent(TypeFunction<T, R> function, Object value) {
        return likeIfAbsent(function, value, getDefaultPredicate());
    }

    default <T, R> S likeIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return like(function, value);
        }
        return doNothing();
    }

    default <T, R> S likeLIfAbsent(TypeFunction<T, R> function, Object value) {
        return likeLIfAbsent(function, value, getDefaultPredicate());
    }

    default <T, R> S likeLIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return likeL(function, value);
        }
        return doNothing();
    }

    default <T, R> S likeRIfAbsent(TypeFunction<T, R> function, Object value) {
        return likeRIfAbsent(function, value, getDefaultPredicate());
    }

    default <T, R> S likeRIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return likeR(function, value);
        }
        return doNothing();
    }

    default S gtIfAbsent(String key, Object value) {
        return gtIfAbsent(key, value, getDefaultPredicate());
    }

    default S gtIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return gt(key, value);
        }
        return doNothing();
    }

    default <T, R> S gtIfAbsent(TypeFunction<T, R> function, Object value) {
        return gtIfAbsent(function, value, getDefaultPredicate());
    }

    default <T, R> S gtIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return gt(function, value);
        }
        return doNothing();
    }

    default S gteIfAbsent(String key, Object value) {
        return gteIfAbsent(key, value, getDefaultPredicate());
    }

    default S gteIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return gte(key, value);
        }
        return doNothing();
    }

    default <T, R> S gteIfAbsent(TypeFunction<T, R> function, Object value) {
        return gteIfAbsent(function, value, getDefaultPredicate());
    }

    default <T, R> S gteIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return gte(function, value);
        }
        return doNothing();
    }

    default S ltIfAbsent(String key, Object value) {
        return ltIfAbsent(key, value, getDefaultPredicate());
    }

    default S ltIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return lt(key, value);
        }
        return doNothing();
    }

    default <T, R> S ltIfAbsent(TypeFunction<T, R> function, Object value) {
        return ltIfAbsent(function, value, getDefaultPredicate());
    }

    default <T, R> S ltIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return lt(function, value);
        }
        return doNothing();
    }

    default S letIfAbsent(String key, Object value) {
        return letIfAbsent(key, value, getDefaultPredicate());
    }

    default S letIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return let(key, value);
        }
        return doNothing();
    }

    default <T, R> S letIfAbsent(TypeFunction<T, R> function, Object value) {
        return letIfAbsent(function, value, getDefaultPredicate());
    }

    default <T, R> S letIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return let(function, value);
        }
        return doNothing();
    }


    default S andIfAbsent(String key, Object value) {
        return andIfAbsent(key, value, getDefaultPredicate());
    }

    default S andIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return and(key, value);
        }
        return doNothing();
    }

    default <T, R> S andIfAbsent(TypeFunction<T, R> function, Object value) {
        return andIfAbsent(function, value, getDefaultPredicate());
    }

    default <T, R> S andIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return and(function, value);
        }
        return doNothing();
    }

    default S orIfAbsent(String key, Object value) {
        return orIfAbsent(key, value, getDefaultPredicate());
    }

    default S orIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return or(key, value);
        }
        return doNothing();
    }

    default <T, R> S orIfAbsent(TypeFunction<T, R> function, Object value) {
        return orIfAbsent(function, value, getDefaultPredicate());
    }

    default <T, R> S orIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return or(function, value);
        }
        return doNothing();
    }

    default S orLikeIfAbsent(String key, Object value) {
        return orLikeIfAbsent(key, value, getDefaultPredicate());
    }

    default S orLikeIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return orLike(key, value);
        }
        return doNothing();
    }


    default <T, R> S orLikeIfAbsent(TypeFunction<T, R> function, Object value) {
        return orLikeIfAbsent(function, value, getDefaultPredicate());
    }

    default <T, R> S orLikeIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return orLike(function, value);
        }
        return doNothing();
    }

    default S inIfAbsent(String key, Collection<?> args) {
        return inIfAbsent(key, args, getDefaultPredicate());
    }

    default S inIfAbsent(String key, Collection<?> args, Predicate<Collection> predicate) {
        if (predicate.test(args)) {
            return in(key, args);
        }
        return doNothing();
    }

    default <T, R> S inIfAbsent(TypeFunction<T, R> function, Collection<?> args) {
        return inIfAbsent(function, args, getDefaultPredicate());
    }

    default <T, R> S inIfAbsent(TypeFunction<T, R> function, Collection<?> args, Predicate<Collection> predicate) {
        if (predicate.test(args)) {
            return in(function, args);
        }
        return doNothing();
    }

    default S notInIfAbsent(String key, Collection<?> args) {
        return notInIfAbsent(key, args, getDefaultPredicate());
    }

    default S notInIfAbsent(String key, Collection<?> args, Predicate<Collection> predicate) {
        if (predicate.test(args)) {
            return notIn(key, args);
        }
        return doNothing();
    }

    default <T, R> S notInIfAbsent(TypeFunction<T, R> function, Collection<?> args) {
        return notInIfAbsent(function, args, getDefaultPredicate());
    }

    default <T, R> S notInIfAbsent(TypeFunction<T, R> function, Collection<?> args, Predicate<Collection> predicate) {
        if (predicate.test(args)) {
            return notIn(function, args);
        }
        return doNothing();
    }

    default S notEqualIfAbsent(String key, Object value) {
        return notEqualIfAbsent(key, value, getDefaultPredicate());
    }

    default S notEqualIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return notEqual(key, value);
        }
        return doNothing();
    }

    default <T, R> S notEqualIfAbsent(TypeFunction<T, R> function, Object value) {
        return notEqualIfAbsent(function, value, getDefaultPredicate());
    }

    default <T, R> S notEqualIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return notEqual(function, value);
        }
        return doNothing();
    }

    default S betweenAndIfAbsent(String key, Object v1, Object v2) {
        return betweenAndIfAbsent(key, v1, v2, getDefaultPredicate());
    }

    default S betweenAndIfAbsent(String key, Object v1, Object v2, Predicate<Object> predicate) {
        if (predicate.test(v1) && predicate.test(v2)) {
            return betweenAnd(key, v1, v2);
        }
        return doNothing();
    }

    default <T, R> S betweenAndIfAbsent(TypeFunction<T, R> function, Object v1, Object v2) {
        return betweenAndIfAbsent(function, v1, v2, getDefaultPredicate());
    }

    default <T, R> S betweenAndIfAbsent(TypeFunction<T, R> function, Object v1, Object v2, Predicate<Object> predicate) {
        if (predicate.test(v1) && predicate.test(v2)) {
            return betweenAnd(function, v1, v2);
        }
        return doNothing();
    }

    default S orBetweenAndIfAbsent(String key, Object v1, Object v2) {
        return orBetweenAndIfAbsent(key, v1, v2, getDefaultPredicate());
    }

    default S orBetweenAndIfAbsent(String key, Object v1, Object v2, Predicate<Object> predicate) {
        if (predicate.test(v1) && predicate.test(v2)) {
            return orBetweenAnd(key, v1, v2);
        }
        return doNothing();
    }

    default <T, R> S orBetweenAndIfAbsent(TypeFunction<T, R> function, Object v1, Object v2) {
        return orBetweenAndIfAbsent(function, v1, v2, getDefaultPredicate());
    }

    default <T, R> S orBetweenAndIfAbsent(TypeFunction<T, R> function, Object v1, Object v2, Predicate<Object> predicate) {
        if (predicate.test(v1) && predicate.test(v2)) {
            return orBetweenAnd(function, v1, v2);
        }
        return doNothing();
    }

    default S andOptIfAbsent(String key, String opt, Object value) {
        return andOptIfAbsent(key, opt, value, getDefaultPredicate());
    }

    default S andOptIfAbsent(String key, String opt, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return and(key, opt, value);
        }
        return doNothing();
    }

    default <T, R> S andOptIfAbsent(TypeFunction<T, R> function, String opt, Object value) {
        return andOptIfAbsent(function, opt, value, getDefaultPredicate());
    }

    default <T, R> S andOptIfAbsent(TypeFunction<T, R> function, String opt, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return and(function, opt, value);
        }
        return doNothing();
    }

    default S orOptIfAbsent(String key, String opt, Object value) {
        return orOptIfAbsent(key, opt, value, getDefaultPredicate());
    }

    default S orOptIfAbsent(String key, String opt, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return or(key, opt, value);
        }
        return doNothing();
    }

    default <T, R> S orOptIfAbsent(TypeFunction<T, R> function, String opt, Object value) {
        return orOptIfAbsent(function, opt, value, getDefaultPredicate());
    }

    default <T, R> S orOptIfAbsent(TypeFunction<T, R> function, String opt, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return or(function, opt, value);
        }
        return doNothing();
    }

    default S whereIfAbsent(String key, Object value) {
        return whereIfAbsent(key, value, getDefaultPredicate());
    }

    default S whereIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return where(key, value);
        }
        return doNothing();
    }

    default <T, R> S whereIfAbsent(TypeFunction<T, R> function, Object value) {
        return whereIfAbsent(function, value, getDefaultPredicate());
    }

    default <T, R> S whereIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return where(TypeFunction.getLambdaColumnName(function), value);
        }
        return doNothing();
    }

    default S whereOptIfAbsent(String key, String opt, Object value) {
        return whereOptIfAbsent(key, opt, value, getDefaultPredicate());
    }

    default S whereOptIfAbsent(String key, String opt, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return where(key, opt, value);
        }
        return doNothing();
    }

    default <T, R> S whereOptIfAbsent(TypeFunction<T, R> function, String opt, Object value) {
        return whereOptIfAbsent(function, opt, value, getDefaultPredicate());
    }

    default <T, R> S whereOptIfAbsent(TypeFunction<T, R> function, String opt, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return where(TypeFunction.getLambdaColumnName(function), opt, value);
        }
        return doNothing();
    }

    /**
     * 获取默认的判空断言
     */
    static <T> Predicate<T> getDefaultPredicate() {
        return (t) -> {
            if (Objects.isNull(t)) {
                return false;
            }
            if (t instanceof String) {
                return StringUtils.isNotBlank((String) t);
            }
            if (t instanceof Collection) {
                return CollectionUtils.isNotEmpty((Collection<?>) t);
            }
             if (t instanceof java.util.Map) {
                 return MapUtils.isNotEmpty((java.util.Map<?, ?>) t);
             }
            if (t.getClass().isArray()) {
                return ArrayUtils.getLength(t) > 0;
            }
            if (t instanceof java.util.Optional) {
                return ((java.util.Optional<?>) t).isPresent();
            }
            return true;
        };
    }

}
