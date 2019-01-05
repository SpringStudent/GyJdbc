package com.gysoft.jdbc.bean;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * @author DJZ-HXF
 */
public interface AuxiliaryOperation {

    Criteria like(String key, Object value);
    <T, R> Criteria like(TypeFunction<T, R> function, Object value);

    Criteria and(String key, Object value);
    <T, R> Criteria and(TypeFunction<T, R> function, Object value);

    Criteria or(String key, Object value);
    <T,R> Criteria or(TypeFunction<T,R> function,Object value);

    Criteria in(String key, Collection<?> args);
    <T, R> Criteria in(TypeFunction<T, R> function, Collection<?> args);

    Criteria notIn(String key, Collection<?> args);
    <T, R> Criteria notIn(TypeFunction<T, R> function, Collection<?> args);

    Criteria gt(String key, Object value);
    <T, R> Criteria gt(TypeFunction<T, R> function, Object value);
    Criteria gte(String key, Object value);
    <T, R> Criteria gte(TypeFunction<T, R> function, Object value);

    Criteria lt(String key, Object value);
    <T, R> Criteria lt(TypeFunction<T, R> function, Object value);
    Criteria let(String key, Object value);
    <T, R> Criteria let(TypeFunction<T, R> function, Object value);

    Criteria doNothing();

    default Criteria likeIfAbsent(String key,Object value){
        return likeIfAbsent(key,value,getDefaultPredicate(value));
    }

    default Criteria likeIfAbsent(String key, Object value, Predicate<Object> predicate){
        if(predicate.test(value)){
            return like(key,value);
        }
        return doNothing();
    }

    default <T, R> Criteria likeIfAbsent(TypeFunction<T, R> function, Object value){
        return likeIfAbsent(function,value,getDefaultPredicate(value));
    }

    default <T,R> Criteria likeIfAbsent(TypeFunction<T, R> function, Object value, Predicate<Object> predicate){
        if(predicate.test(value)){
            return like(function,value);
        }
        return doNothing();
    }

    default  Criteria gtIfAbsent(String key, Object value) {
        return gtIfAbsent(key,value,getDefaultPredicate(value));
    }

    default  Criteria gtIfAbsent(String key, Object value,Predicate<Object> predicate) {
        if(predicate.test(value)){
            return gt(key,value);
        }
        return doNothing();
    }

    default <T,R> Criteria gtIfAbsent(TypeFunction<T,R> function, Object value) {
        return gtIfAbsent(function,value,getDefaultPredicate(value));
    }

    default <T,R> Criteria gtIfAbsent(TypeFunction<T,R> function, Object value,Predicate<Object> predicate) {
        if(predicate.test(value)){
            return gt(function,value);
        }
        return doNothing();
    }

    default  Criteria gteIfAbsent(String key, Object value) {
        return gteIfAbsent(key,value,getDefaultPredicate(value));
    }

    default  Criteria gteIfAbsent(String key, Object value,Predicate<Object> predicate) {
        if(predicate.test(value)){
            return gte(key,value);
        }
        return doNothing();
    }

    default <T,R> Criteria gteIfAbsent(TypeFunction<T,R> function, Object value) {
        return gteIfAbsent(function,value,getDefaultPredicate(value));
    }

    default <T,R> Criteria gteIfAbsent(TypeFunction<T,R> function, Object value,Predicate<Object> predicate) {
        if(predicate.test(value)){
            return gte(function,value);
        }
        return doNothing();
    }

    default  Criteria ltIfAbsent(String key, Object value) {
        return ltIfAbsent(key,value,getDefaultPredicate(value));
    }

    default  Criteria ltIfAbsent(String key, Object value,Predicate<Object> predicate) {
        if(predicate.test(value)){
            return lt(key,value);
        }
        return doNothing();
    }

    default <T,R> Criteria ltIfAbsent(TypeFunction<T,R> function, Object value) {
        return ltIfAbsent(function,value,getDefaultPredicate(value));
    }

    default <T,R> Criteria ltIfAbsent(TypeFunction<T,R> function, Object value,Predicate<Object> predicate) {
        if(predicate.test(value)){
            return lt(function,value);
        }
        return doNothing();
    }

    default  Criteria letIfAbsent(String key, Object value) {
        return letIfAbsent(key,value,getDefaultPredicate(value));
    }

    default  Criteria letIfAbsent(String key, Object value,Predicate<Object> predicate) {
        if(predicate.test(value)){
            return let(key,value);
        }
        return doNothing();
    }

    default <T,R> Criteria letIfAbsent(TypeFunction<T,R> function, Object value) {
        return letIfAbsent(function,value,getDefaultPredicate(value));
    }

    default <T,R>  Criteria letIfAbsent(TypeFunction<T,R> function, Object value,Predicate<Object> predicate) {
        if(predicate.test(value)){
            return let(function,value);
        }
        return doNothing();
    }


    default Criteria andIfAbsent(String key, Object value){
        return andIfAbsent(key,value,getDefaultPredicate(value));
    }
    default Criteria andIfAbsent(String key, Object value,Predicate<Object> predicate){
        if(predicate.test(value)){
            return and(key, value);
        }
        return doNothing();
    }

    default <T, R> Criteria andIfAbsent(TypeFunction<T, R> function, Object value){
        return andIfAbsent(function,value,getDefaultPredicate(value));
    }
    default <T,R> Criteria andIfAbsent(TypeFunction<T, R> function, Object value,Predicate<Object> predicate){
        if(predicate.test(value)){
            return and(function, value);
        }
        return doNothing();
    }

    default Criteria orIfAbsent(String key, Object value){
        return orIfAbsent(key,value,getDefaultPredicate(value));
    }
    default Criteria orIfAbsent(String key, Object value,Predicate<Object> predicate){
        if(predicate.test(value)){
            return or(key, value);
        }
        return doNothing();
    }

    default <T,R> Criteria orIfAbsent(TypeFunction<T,R> function, Object value){
        return orIfAbsent(function,value,getDefaultPredicate(value));
    }
    default <T,R> Criteria orIfAbsent(TypeFunction<T,R> function, Object value,Predicate<Object> predicate){
        if(predicate.test(value)){
            return or(function, value);
        }
        return doNothing();
    }

    default Criteria inIfAbsent(String key,  Collection<?> args){
        return inIfAbsent(key,args,getDefaultPredicate(args));
    }
    default Criteria inIfAbsent(String key,  Collection<?> args,Predicate<Collection> predicate){
        if(predicate.test(args)){
            return in(key, args);
        }
        return doNothing();
    }

    default <T,R> Criteria inIfAbsent(TypeFunction function,  Collection<?> args){
        return inIfAbsent(function,args,getDefaultPredicate(args));
    }
    default <T,R> Criteria inIfAbsent(TypeFunction<T,R> function,Collection<?> args,Predicate<Collection> predicate){
        if(predicate.test(args)){
            return in(function, args);
        }
        return doNothing();
    }

    default Criteria notInIfAbsent(String key,  Collection<?> args){
        return notInIfAbsent(key,args,getDefaultPredicate(args));
    }
    default Criteria notInIfAbsent(String key,  Collection<?> args,Predicate<Collection> predicate){
        if(predicate.test(args)){
            return notIn(key, args);
        }
        return doNothing();
    }

    default <T,R> Criteria notInIfAbsent(TypeFunction<T,R> function,  Collection<?> args){
        return notInIfAbsent(function,args,getDefaultPredicate(args));
    }
    default <T,R> Criteria notInIfAbsent(TypeFunction<T,R> function,  Collection<?> args,Predicate<Collection> predicate){
        if(predicate.test(args)){
            return notIn(function, args);
        }
        return doNothing();
    }

    default <T> Predicate<T> getDefaultPredicate(T value){
        return (t)->{
            if(value instanceof String){
                if(StringUtils.isNotEmpty((String)value)){
                    return true;
                }
            }
            if(value instanceof  Collection){
                if(CollectionUtils.isNotEmpty((Collection) value)){
                    return true;
                }
            }
            return false;
        };
    }
}
