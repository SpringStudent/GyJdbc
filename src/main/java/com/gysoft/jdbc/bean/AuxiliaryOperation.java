package com.gysoft.jdbc.bean;

import org.apache.commons.collections.CollectionUtils;
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

    default S likeIfAbsent(String key, Object value) {
        return likeIfAbsent(key, value, getDefaultPredicate(value));
    }

    default S likeIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return like(key, value);
        }
        return doNothing();
    }

    default S likeLIfAbsent(String key, Object value) {
        return likeLIfAbsent(key, value, getDefaultPredicate(value));
    }

    default S likeLIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return likeL(key, value);
        }
        return doNothing();
    }

    default S likeRIfAbsent(String key, Object value) {
        return likeRIfAbsent(key, value, getDefaultPredicate(value));
    }

    default S likeRIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return likeR(key, value);
        }
        return doNothing();
    }

    default <T, R> S likeIfAbsent(TypeFunction<T, R> function, Object value) {
        return likeIfAbsent(function, value, getDefaultPredicate(value));
    }

    default <T, R> S likeIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return like(function, value);
        }
        return doNothing();
    }

    default <T, R> S likeLIfAbsent(TypeFunction<T, R> function, Object value) {
        return likeLIfAbsent(function, value, getDefaultPredicate(value));
    }

    default <T, R> S likeLIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return likeL(function, value);
        }
        return doNothing();
    }

    default <T, R> S likeRIfAbsent(TypeFunction<T, R> function, Object value) {
        return likeRIfAbsent(function, value, getDefaultPredicate(value));
    }

    default <T, R> S likeRIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return likeR(function, value);
        }
        return doNothing();
    }

    default S gtIfAbsent(String key, Object value) {
        return gtIfAbsent(key, value, getDefaultPredicate(value));
    }

    default S gtIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return gt(key, value);
        }
        return doNothing();
    }

    default <T, R> S gtIfAbsent(TypeFunction<T, R> function, Object value) {
        return gtIfAbsent(function, value, getDefaultPredicate(value));
    }

    default <T, R> S gtIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return gt(function, value);
        }
        return doNothing();
    }

    default S gteIfAbsent(String key, Object value) {
        return gteIfAbsent(key, value, getDefaultPredicate(value));
    }

    default S gteIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return gte(key, value);
        }
        return doNothing();
    }

    default <T, R> S gteIfAbsent(TypeFunction<T, R> function, Object value) {
        return gteIfAbsent(function, value, getDefaultPredicate(value));
    }

    default <T, R> S gteIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return gte(function, value);
        }
        return doNothing();
    }

    default S ltIfAbsent(String key, Object value) {
        return ltIfAbsent(key, value, getDefaultPredicate(value));
    }

    default S ltIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return lt(key, value);
        }
        return doNothing();
    }

    default <T, R> S ltIfAbsent(TypeFunction<T, R> function, Object value) {
        return ltIfAbsent(function, value, getDefaultPredicate(value));
    }

    default <T, R> S ltIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return lt(function, value);
        }
        return doNothing();
    }

    default S letIfAbsent(String key, Object value) {
        return letIfAbsent(key, value, getDefaultPredicate(value));
    }

    default S letIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return let(key, value);
        }
        return doNothing();
    }

    default <T, R> S letIfAbsent(TypeFunction<T, R> function, Object value) {
        return letIfAbsent(function, value, getDefaultPredicate(value));
    }

    default <T, R> S letIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return let(function, value);
        }
        return doNothing();
    }


    default S andIfAbsent(String key, Object value) {
        return andIfAbsent(key, value, getDefaultPredicate(value));
    }

    default S andIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return and(key, value);
        }
        return doNothing();
    }

    default <T, R> S andIfAbsent(TypeFunction<T, R> function, Object value) {
        return andIfAbsent(function, value, getDefaultPredicate(value));
    }

    default <T, R> S andIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return and(function, value);
        }
        return doNothing();
    }

    default S orIfAbsent(String key, Object value) {
        return orIfAbsent(key, value, getDefaultPredicate(value));
    }

    default S orIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return or(key, value);
        }
        return doNothing();
    }

    default <T, R> S orIfAbsent(TypeFunction<T, R> function, Object value) {
        return orIfAbsent(function, value, getDefaultPredicate(value));
    }

    default <T, R> S orIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return or(function, value);
        }
        return doNothing();
    }

    default S orLikeIfAbsent(String key, Object value) {
        return orLikeIfAbsent(key, value, getDefaultPredicate(value));
    }

    default S orLikeIfAbsent(String key, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return orLike(key, value);
        }
        return doNothing();
    }


    default <T, R> S orLikeIfAbsent(TypeFunction<T, R> function, Object value) {
        return orLikeIfAbsent(function, value, getDefaultPredicate(value));
    }

    default <T, R> S orLikeIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate) {
        if (predicate.test(value)) {
            return orLike(function, value);
        }
        return doNothing();
    }

    default S inIfAbsent(String key, Collection<?> args) {
        return inIfAbsent(key, args, getDefaultPredicate(args));
    }

    default S inIfAbsent(String key, Collection<?> args, Predicate<Collection> predicate) {
        if (predicate.test(args)) {
            return in(key, args);
        }
        return doNothing();
    }

    default <T, R> S inIfAbsent(TypeFunction<T, R> function, Collection<?> args) {
        return inIfAbsent(function, args, getDefaultPredicate(args));
    }

    default <T, R> S inIfAbsent(TypeFunction<T, R> function, Collection<?> args, Predicate<Collection> predicate) {
        if (predicate.test(args)) {
            return in(function, args);
        }
        return doNothing();
    }

    default S notInIfAbsent(String key, Collection<?> args) {
        return notInIfAbsent(key, args, getDefaultPredicate(args));
    }

    default S notInIfAbsent(String key, Collection<?> args, Predicate<Collection> predicate) {
        if (predicate.test(args)) {
            return notIn(key, args);
        }
        return doNothing();
    }

    default <T, R> S notInIfAbsent(TypeFunction<T, R> function, Collection<?> args) {
        return notInIfAbsent(function, args, getDefaultPredicate(args));
    }

    default <T, R> S notInIfAbsent(TypeFunction<T, R> function, Collection<?> args, Predicate<Collection> predicate) {
        if (predicate.test(args)) {
            return notIn(function, args);
        }
        return doNothing();
    }

    static <T> Predicate<T> getDefaultPredicate(T value) {
        return (t) -> {
            if (Objects.isNull(value)) {
                return false;
            }
            if (value instanceof String) {
                if (StringUtils.isEmpty((String) value)) {
                    return false;
                }
            }
            if (value instanceof Collection) {
                if (CollectionUtils.isEmpty((Collection) value)) {
                    return false;
                }
            }
            if (value.getClass().isArray()) {
                if (ArrayUtils.getLength(value) == 0) {
                    return false;
                }
            }
            return true;
        };
    }

}
