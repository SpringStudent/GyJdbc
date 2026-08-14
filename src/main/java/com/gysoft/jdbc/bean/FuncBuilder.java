package com.gysoft.jdbc.bean;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * SQL 函数表达式工厂。
 * <p>所有方法返回不可变的 {@link FuncExpr} 表达式对象，支持函数嵌套组合与链式
 * {@link FuncExpr#as(String)}。参数约定：</p>
 * <ul>
 *   <li>{@code String} 参数 = 裸 SQL 片段（列名 / 表达式 / 变量），原样拼接，不加引号；</li>
 *   <li>{@code TypeFunction} 参数 = 实体方法引用，解析为对应列名；</li>
 *   <li>{@code FuncExpr} 参数 = 已组合的表达式（{@link #col} / {@link #lit} / 其它函数结果）；</li>
 *   <li>{@code Object} 值参数 = 值字面量：{@code String} 自动加单引号并转义（' → ''），
 *       {@code Number}/{@code Boolean} 原样输出，{@code FuncExpr} 取表达式文本，
 *       {@link FieldReference} 取列名，{@code null} 输出 NULL。</li>
 * </ul>
 * <p>由此列/表达式与字符串常量在类型上区分开：想拼常量必须用 {@link #lit(String)} 或直接传
 * 值参数，杜绝手写引号导致的 SQL 语法破坏与注入。</p>
 *
 * @author 周宁
 */
public final class FuncBuilder {

    private FuncBuilder() {
    }

    // ==================== 基础工厂 ====================

    /**
     * 校验列名/表达式非空，防痴呆使用
     *
     * @param sql 列名或 SQL 片段
     * @return String 校验后的列名
     */
    private static String require(String sql) {
        if (sql == null) {
            throw new GyjdbcException("func field cannot be null");
        }
        return sql;
    }

    /**
     * 列名 / 裸表达式
     *
     * @param field 列名或 SQL 片段
     * @return FuncExpr 表达式
     */
    public static FuncExpr col(String field) {
        return new FuncExpr(require(field));
    }

    /**
     * 实体字段方法引用对应的列
     *
     * @param function 方法引用
     * @return FuncExpr 表达式
     */
    public static <T, R> FuncExpr col(TypeFunction<T, R> function) {
        if (function == null) {
            throw new GyjdbcException("func field cannot be null");
        }
        return new FuncExpr(TypeFunction.getLambdaColumnName(function));
    }

    /**
     * 字符串字面量：自动加单引号并转义（' → ''）。
     * <p>与 {@code val()} 语义统一：{@code null} 输出 NULL 而非空字符串。</p>
     *
     * @param value 字符串常量
     * @return FuncExpr 如 'Tom''s'，null 时为 NULL
     */
    public static FuncExpr lit(String value) {
        if (value == null) {
            return new FuncExpr("NULL");
        }
        return new FuncExpr("'" + escape(value) + "'");
    }

    /**
     * 单引号转义（' → ''）
     *
     * @param s 原始字符串
     * @return String 转义后字符串
     */
    private static String escape(String s) {
        return s.replace("'", "''");
    }

    /**
     * 值字面量归一化：String 加引号转义，Number/Boolean 原样，表达式/列取文本，null→NULL
     *
     * @param v 值
     * @return String SQL 值文本
     */
    private static String val(Object v) {
        if (v == null) {
            return "NULL";
        }
        if (v instanceof FuncExpr) {
            return ((FuncExpr) v).getSql();
        }
        if (v instanceof FieldReference) {
            return ((FieldReference) v).getField();
        }
        if (v instanceof Number || v instanceof Boolean) {
            return v.toString();
        }
        return "'" + escape(String.valueOf(v)) + "'";
    }

    // ==================== 聚合函数 ====================

    public static FuncExpr count() {
        return new FuncExpr("COUNT(*)");
    }

    public static FuncExpr count(String field) {
        return new FuncExpr("COUNT(" + require(field) + ")");
    }

    public static <T, R> FuncExpr count(TypeFunction<T, R> function) {
        return count(col(function));
    }

    public static FuncExpr count(FuncExpr field) {
        return count(field.getSql());
    }

    public static FuncExpr countDistinct(String field) {
        return new FuncExpr("COUNT(DISTINCT " + require(field) + ")");
    }

    public static <T, R> FuncExpr countDistinct(TypeFunction<T, R> function) {
        return countDistinct(col(function));
    }

    public static FuncExpr countDistinct(FuncExpr field) {
        return countDistinct(field.getSql());
    }

    public static FuncExpr sum(String field) {
        return new FuncExpr("SUM(" + require(field) + ")");
    }

    public static <T, R> FuncExpr sum(TypeFunction<T, R> function) {
        return sum(col(function));
    }

    public static FuncExpr sum(FuncExpr field) {
        return sum(field.getSql());
    }

    public static FuncExpr avg(String field) {
        return new FuncExpr("AVG(" + require(field) + ")");
    }

    public static <T, R> FuncExpr avg(TypeFunction<T, R> function) {
        return avg(col(function));
    }

    public static FuncExpr avg(FuncExpr field) {
        return avg(field.getSql());
    }

    public static FuncExpr max(String field) {
        return new FuncExpr("MAX(" + require(field) + ")");
    }

    public static <T, R> FuncExpr max(TypeFunction<T, R> function) {
        return max(col(function));
    }

    public static FuncExpr max(FuncExpr field) {
        return max(field.getSql());
    }

    public static FuncExpr min(String field) {
        return new FuncExpr("MIN(" + require(field) + ")");
    }

    public static <T, R> FuncExpr min(TypeFunction<T, R> function) {
        return min(col(function));
    }

    public static FuncExpr min(FuncExpr field) {
        return min(field.getSql());
    }

    public static FuncExpr groupConcat(String field) {
        return new FuncExpr("GROUP_CONCAT(" + require(field) + ")");
    }

    public static <T, R> FuncExpr groupConcat(TypeFunction<T, R> function) {
        return groupConcat(col(function));
    }

    public static FuncExpr groupConcat(FuncExpr field) {
        return groupConcat(field.getSql());
    }

    public static FuncExpr groupConcat(String field, Object separator) {
        return new FuncExpr("GROUP_CONCAT(" + require(field) + " SEPARATOR " + val(separator) + ")");
    }

    public static <T, R> FuncExpr groupConcat(TypeFunction<T, R> function, Object separator) {
        return groupConcat(col(function), separator);
    }

    public static FuncExpr groupConcat(FuncExpr field, Object separator) {
        return groupConcat(field.getSql(), separator);
    }

    public static FuncExpr groupConcatDistinct(String field) {
        return new FuncExpr("GROUP_CONCAT(DISTINCT " + require(field) + ")");
    }

    public static <T, R> FuncExpr groupConcatDistinct(TypeFunction<T, R> function) {
        return groupConcatDistinct(col(function));
    }

    public static FuncExpr groupConcatDistinct(FuncExpr field) {
        return groupConcatDistinct(field.getSql());
    }

    // ==================== 字符串函数 ====================

    public static FuncExpr length(String field) {
        return new FuncExpr("LENGTH(" + field + ")");
    }

    public static <T, R> FuncExpr length(TypeFunction<T, R> function) {
        return length(col(function));
    }

    public static FuncExpr length(FuncExpr field) {
        return length(field.getSql());
    }

    public static FuncExpr charLength(String field) {
        return new FuncExpr("CHAR_LENGTH(" + field + ")");
    }

    public static <T, R> FuncExpr charLength(TypeFunction<T, R> function) {
        return charLength(col(function));
    }

    public static FuncExpr charLength(FuncExpr field) {
        return charLength(field.getSql());
    }

    /**
     * 拼接：参数为裸 SQL 片段或表达式，字符串常量请用 {@link #lit(String)} 组合
     */
    public static FuncExpr concat(String... fields) {
        return new FuncExpr("CONCAT(" + Arrays.stream(fields).collect(Collectors.joining(",")) + ")");
    }

    @SafeVarargs
    public static <T, R> FuncExpr concat(TypeFunction<T, R>... functions) {
        return concat(Arrays.stream(functions).map(FuncBuilder::col).toArray(FuncExpr[]::new));
    }

    public static FuncExpr concat(FuncExpr... fields) {
        return new FuncExpr("CONCAT(" + Arrays.stream(fields).map(FuncExpr::getSql).collect(Collectors.joining(",")) + ")");
    }

    public static FuncExpr concat_ws(String joinStr, String... fields) {
        return new FuncExpr("CONCAT_WS(" + val(joinStr) + "," + Arrays.stream(fields).collect(Collectors.joining(",")) + ")");
    }

    @SafeVarargs
    public static <T, R> FuncExpr concat_ws(String joinStr, TypeFunction<T, R>... functions) {
        return concat_ws(joinStr, Arrays.stream(functions).map(FuncBuilder::col).toArray(FuncExpr[]::new));
    }

    public static FuncExpr concat_ws(String joinStr, FuncExpr... fields) {
        return new FuncExpr("CONCAT_WS(" + val(joinStr) + "," + Arrays.stream(fields).map(FuncExpr::getSql).collect(Collectors.joining(",")) + ")");
    }

    public static FuncExpr upper(String field) {
        return new FuncExpr("UPPER(" + field + ")");
    }

    public static <T, R> FuncExpr upper(TypeFunction<T, R> function) {
        return upper(col(function));
    }

    public static FuncExpr upper(FuncExpr field) {
        return upper(field.getSql());
    }

    public static FuncExpr lower(String field) {
        return new FuncExpr("LOWER(" + field + ")");
    }

    public static <T, R> FuncExpr lower(TypeFunction<T, R> function) {
        return lower(col(function));
    }

    public static FuncExpr lower(FuncExpr field) {
        return lower(field.getSql());
    }

    public static FuncExpr ltrim(String field) {
        return new FuncExpr("LTRIM(" + field + ")");
    }

    public static <T, R> FuncExpr ltrim(TypeFunction<T, R> function) {
        return ltrim(col(function));
    }

    public static FuncExpr ltrim(FuncExpr field) {
        return ltrim(field.getSql());
    }

    public static FuncExpr rtrim(String field) {
        return new FuncExpr("RTRIM(" + field + ")");
    }

    public static <T, R> FuncExpr rtrim(TypeFunction<T, R> function) {
        return rtrim(col(function));
    }

    public static FuncExpr rtrim(FuncExpr field) {
        return rtrim(field.getSql());
    }

    public static FuncExpr trim(String field) {
        return new FuncExpr("TRIM(" + field + ")");
    }

    public static <T, R> FuncExpr trim(TypeFunction<T, R> function) {
        return trim(col(function));
    }

    public static FuncExpr trim(FuncExpr field) {
        return trim(field.getSql());
    }

    public static FuncExpr findInSet(String field, String field2) {
        return new FuncExpr("FIND_IN_SET(" + field + "," + field2 + ")");
    }

    public static FuncExpr findInSet(FuncExpr field, FuncExpr field2) {
        return findInSet(field.getSql(), field2.getSql());
    }

    public static FuncExpr locate(String field, String field2) {
        return new FuncExpr("LOCATE(" + field + "," + field2 + ")");
    }

    public static FuncExpr locate(FuncExpr field, FuncExpr field2) {
        return locate(field.getSql(), field2.getSql());
    }

    public static FuncExpr position(String field, String field2) {
        return new FuncExpr("POSITION(" + field + " IN " + field2 + ")");
    }

    public static FuncExpr position(FuncExpr field, FuncExpr field2) {
        return position(field.getSql(), field2.getSql());
    }

    public static FuncExpr instr(String field, String field2) {
        return new FuncExpr("INSTR(" + field + "," + field2 + ")");
    }

    public static FuncExpr instr(FuncExpr field, FuncExpr field2) {
        return instr(field.getSql(), field2.getSql());
    }

    public static FuncExpr left(String field, int index) {
        return new FuncExpr("LEFT(" + field + "," + index + ")");
    }

    public static <T, R> FuncExpr left(TypeFunction<T, R> function, int index) {
        return left(col(function), index);
    }

    public static FuncExpr left(FuncExpr field, int index) {
        return left(field.getSql(), index);
    }

    public static FuncExpr right(String field, int index) {
        return new FuncExpr("RIGHT(" + field + "," + index + ")");
    }

    public static <T, R> FuncExpr right(TypeFunction<T, R> function, int index) {
        return right(col(function), index);
    }

    public static FuncExpr right(FuncExpr field, int index) {
        return right(field.getSql(), index);
    }

    public static FuncExpr substring(String field, int index, int index2) {
        return new FuncExpr("SUBSTRING(" + field + "," + index + "," + index2 + ")");
    }

    public static <T, R> FuncExpr substring(TypeFunction<T, R> function, int index, int index2) {
        return substring(col(function), index, index2);
    }

    public static FuncExpr substring(FuncExpr field, int index, int index2) {
        return substring(field.getSql(), index, index2);
    }

    public static FuncExpr elt(int index, String... fields) {
        return new FuncExpr("ELT(" + index + "," + Arrays.stream(fields).collect(Collectors.joining(",")) + ")");
    }

    public static FuncExpr insert(String field, int start, int end, Object field2) {
        return new FuncExpr("INSERT(" + field + "," + start + "," + end + "," + val(field2) + ")");
    }

    public static <T, R> FuncExpr insert(TypeFunction<T, R> function, int start, int end, Object field2) {
        return insert(col(function), start, end, field2);
    }

    public static FuncExpr insert(FuncExpr field, int start, int end, Object field2) {
        return insert(field.getSql(), start, end, field2);
    }

    public static FuncExpr replace(String field, Object field2, Object field3) {
        return new FuncExpr("REPLACE(" + field + "," + val(field2) + "," + val(field3) + ")");
    }

    public static <T, R> FuncExpr replace(TypeFunction<T, R> function, Object field2, Object field3) {
        return replace(col(function), field2, field3);
    }

    public static FuncExpr replace(FuncExpr field, Object field2, Object field3) {
        return replace(field.getSql(), field2, field3);
    }

    // ==================== 数值函数 ====================

    public static FuncExpr abs(String field) {
        return new FuncExpr("ABS(" + field + ")");
    }

    public static <T, R> FuncExpr abs(TypeFunction<T, R> function) {
        return abs(col(function));
    }

    public static FuncExpr abs(FuncExpr field) {
        return abs(field.getSql());
    }

    public static FuncExpr ceil(String field) {
        return new FuncExpr("CEIL(" + field + ")");
    }

    public static <T, R> FuncExpr ceil(TypeFunction<T, R> function) {
        return ceil(col(function));
    }

    public static FuncExpr ceil(FuncExpr field) {
        return ceil(field.getSql());
    }

    public static FuncExpr floor(String field) {
        return new FuncExpr("FLOOR(" + field + ")");
    }

    public static <T, R> FuncExpr floor(TypeFunction<T, R> function) {
        return floor(col(function));
    }

    public static FuncExpr floor(FuncExpr field) {
        return floor(field.getSql());
    }

    public static FuncExpr mod(String field, String field2) {
        return new FuncExpr("MOD(" + field + "," + field2 + ")");
    }

    public static FuncExpr mod(FuncExpr field, FuncExpr field2) {
        return mod(field.getSql(), field2.getSql());
    }

    public static FuncExpr rand() {
        return new FuncExpr("RAND()");
    }

    public static FuncExpr rand(int seed) {
        return new FuncExpr("RAND(" + seed + ")");
    }

    public static FuncExpr round(String field, int digit) {
        return new FuncExpr("ROUND(" + field + "," + digit + ")");
    }

    public static <T, R> FuncExpr round(TypeFunction<T, R> function, int digit) {
        return round(col(function), digit);
    }

    public static FuncExpr round(FuncExpr field, int digit) {
        return round(field.getSql(), digit);
    }

    public static FuncExpr truncate(String field, int digit) {
        return new FuncExpr("TRUNCATE(" + field + "," + digit + ")");
    }

    public static <T, R> FuncExpr truncate(TypeFunction<T, R> function, int digit) {
        return truncate(col(function), digit);
    }

    public static FuncExpr truncate(FuncExpr field, int digit) {
        return truncate(field.getSql(), digit);
    }

    // ==================== 日期函数 ====================

    public static FuncExpr curdate() {
        return new FuncExpr("CURDATE()");
    }

    public static FuncExpr curtime() {
        return new FuncExpr("CURTIME()");
    }

    public static FuncExpr now() {
        return new FuncExpr("NOW()");
    }

    public static FuncExpr month(String dateField) {
        return new FuncExpr("MONTH(" + dateField + ")");
    }

    public static <T, R> FuncExpr month(TypeFunction<T, R> function) {
        return month(col(function));
    }

    public static FuncExpr month(FuncExpr field) {
        return month(field.getSql());
    }

    public static FuncExpr monthname(String dateField) {
        return new FuncExpr("MONTHNAME(" + dateField + ")");
    }

    public static <T, R> FuncExpr monthname(TypeFunction<T, R> function) {
        return monthname(col(function));
    }

    public static FuncExpr monthname(FuncExpr field) {
        return monthname(field.getSql());
    }

    public static FuncExpr week(String dateField) {
        return new FuncExpr("WEEK(" + dateField + ")");
    }

    public static <T, R> FuncExpr week(TypeFunction<T, R> function) {
        return week(col(function));
    }

    public static FuncExpr week(FuncExpr field) {
        return week(field.getSql());
    }

    public static FuncExpr year(String dateField) {
        return new FuncExpr("YEAR(" + dateField + ")");
    }

    public static <T, R> FuncExpr year(TypeFunction<T, R> function) {
        return year(col(function));
    }

    public static FuncExpr year(FuncExpr field) {
        return year(field.getSql());
    }

    public static FuncExpr hour(String timeField) {
        return new FuncExpr("HOUR(" + timeField + ")");
    }

    public static <T, R> FuncExpr hour(TypeFunction<T, R> function) {
        return hour(col(function));
    }

    public static FuncExpr hour(FuncExpr field) {
        return hour(field.getSql());
    }

    public static FuncExpr minute(String timeField) {
        return new FuncExpr("MINUTE(" + timeField + ")");
    }

    public static <T, R> FuncExpr minute(TypeFunction<T, R> function) {
        return minute(col(function));
    }

    public static FuncExpr minute(FuncExpr field) {
        return minute(field.getSql());
    }

    public static FuncExpr weekday(String dateField) {
        return new FuncExpr("WEEKDAY(" + dateField + ")");
    }

    public static <T, R> FuncExpr weekday(TypeFunction<T, R> function) {
        return weekday(col(function));
    }

    public static FuncExpr weekday(FuncExpr field) {
        return weekday(field.getSql());
    }

    public static FuncExpr dayname(String dateField) {
        return new FuncExpr("DAYNAME(" + dateField + ")");
    }

    public static <T, R> FuncExpr dayname(TypeFunction<T, R> function) {
        return dayname(col(function));
    }

    public static FuncExpr dayname(FuncExpr field) {
        return dayname(field.getSql());
    }

    public static FuncExpr date(String field) {
        return new FuncExpr("DATE(" + field + ")");
    }

    public static <T, R> FuncExpr date(TypeFunction<T, R> function) {
        return date(col(function));
    }

    public static FuncExpr date(FuncExpr field) {
        return date(field.getSql());
    }

    public static FuncExpr dateFormat(String field, Object formatPattern) {
        return new FuncExpr("DATE_FORMAT(" + field + "," + val(formatPattern) + ")");
    }

    public static <T, R> FuncExpr dateFormat(TypeFunction<T, R> function, Object formatPattern) {
        return dateFormat(col(function), formatPattern);
    }

    public static FuncExpr dateFormat(FuncExpr field, Object formatPattern) {
        return dateFormat(field.getSql(), formatPattern);
    }

    public static FuncExpr format(String field, Object formatPattern) {
        return new FuncExpr("FORMAT(" + field + "," + val(formatPattern) + ")");
    }

    public static <T, R> FuncExpr format(TypeFunction<T, R> function, Object formatPattern) {
        return format(col(function), formatPattern);
    }

    public static FuncExpr format(FuncExpr field, Object formatPattern) {
        return format(field.getSql(), formatPattern);
    }

    public static FuncExpr dateSub(String field, String express) {
        return new FuncExpr("DATE_SUB(" + field + "," + express + ")");
    }

    public static <T, R> FuncExpr dateSub(TypeFunction<T, R> function, String express) {
        return dateSub(col(function), express);
    }

    public static FuncExpr dateSub(FuncExpr field, String express) {
        return dateSub(field.getSql(), express);
    }

    public static FuncExpr dateAdd(String field, String express) {
        return new FuncExpr("DATE_ADD(" + field + "," + express + ")");
    }

    public static <T, R> FuncExpr dateAdd(TypeFunction<T, R> function, String express) {
        return dateAdd(col(function), express);
    }

    public static FuncExpr dateAdd(FuncExpr field, String express) {
        return dateAdd(field.getSql(), express);
    }

    public static FuncExpr strToDate(String field, Object formatPattern) {
        return new FuncExpr("STR_TO_DATE(" + field + "," + val(formatPattern) + ")");
    }

    public static <T, R> FuncExpr strToDate(TypeFunction<T, R> function, Object formatPattern) {
        return strToDate(col(function), formatPattern);
    }

    public static FuncExpr strToDate(FuncExpr field, Object formatPattern) {
        return strToDate(field.getSql(), formatPattern);
    }

    // ==================== 条件函数 ====================

    public static FuncExpr ifNull(String field, Object val) {
        return new FuncExpr("IFNULL(" + field + "," + FuncBuilder.val(val) + ")");
    }

    public static <T, R> FuncExpr ifNull(TypeFunction<T, R> function, Object val) {
        return ifNull(col(function), val);
    }

    public static FuncExpr ifNull(FuncExpr field, Object val) {
        return ifNull(field.getSql(), val);
    }

    public static FuncExpr _if(String express, Object val1, Object val2) {
        return new FuncExpr("IF(" + express + "," + FuncBuilder.val(val1) + "," + FuncBuilder.val(val2) + ")");
    }

    public static <T, R> FuncExpr _if(TypeFunction<T, R> function, Object val1, Object val2) {
        return _if(col(function), val1, val2);
    }

    public static FuncExpr _if(FuncExpr express, Object val1, Object val2) {
        return _if(express.getSql(), val1, val2);
    }

    public static FuncExpr caseWhen(String express, Object thenVal, Object elseVal) {
        return caseWhen(express, thenVal).elseThen(elseVal).end();
    }

    public static CaseWhenBuilder caseWhen(String express, Object thenVal) {
        return new CaseWhenBuilder().when(express, thenVal);
    }

    /**
     * CASE WHEN 构建器
     */
    public static class CaseWhenBuilder {
        private final StringBuilder caseSql = new StringBuilder("CASE");

        public CaseWhenBuilder when(String express, Object thenVal) {
            caseSql.append(" WHEN ").append(express).append(" THEN ").append(FuncBuilder.val(thenVal));
            return this;
        }

        public CaseWhenBuilder elseThen(Object elseVal) {
            caseSql.append(" ELSE ").append(FuncBuilder.val(elseVal));
            return this;
        }

        public FuncExpr end() {
            return new FuncExpr(caseSql + " END");
        }

        public FuncExpr as(String as) {
            return end().as(as);
        }
    }

    // ==================== JSON 函数 ====================

    public static FuncExpr jsonExtract(String field, Object key) {
        return new FuncExpr("JSON_EXTRACT(" + field + "," + val(key) + ")");
    }

    public static <T, R> FuncExpr jsonExtract(TypeFunction<T, R> function, Object key) {
        return jsonExtract(col(function), key);
    }

    public static FuncExpr jsonExtract(FuncExpr field, Object key) {
        return jsonExtract(field.getSql(), key);
    }

    public static FuncExpr jsonUnquote(String express) {
        return new FuncExpr("JSON_UNQUOTE(" + express + ")");
    }

    public static FuncExpr jsonUnquote(FuncExpr express) {
        return jsonUnquote(express.getSql());
    }

    public static FuncExpr jsonContains(String field, Object candidate) {
        return new FuncExpr("JSON_CONTAINS(" + field + "," + val(candidate) + ")");
    }

    public static <T, R> FuncExpr jsonContains(TypeFunction<T, R> function, Object candidate) {
        return jsonContains(col(function), candidate);
    }

    public static FuncExpr jsonContains(FuncExpr field, Object candidate) {
        return jsonContains(field.getSql(), candidate);
    }

    public static FuncExpr jsonContains(String field, Object candidate, Object key) {
        return new FuncExpr("JSON_CONTAINS(" + field + "," + val(candidate) + "," + val(key) + ")");
    }

    public static <T, R> FuncExpr jsonContains(TypeFunction<T, R> function, Object candidate, Object key) {
        return jsonContains(col(function), candidate, key);
    }

    public static FuncExpr jsonContains(FuncExpr field, Object candidate, Object key) {
        return jsonContains(field.getSql(), candidate, key);
    }

    public static FuncExpr jsonSet(String field, Object key, Object val) {
        return new FuncExpr("JSON_SET(" + field + "," + FuncBuilder.val(key) + "," + FuncBuilder.val(val) + ")");
    }

    public static <T, R> FuncExpr jsonSet(TypeFunction<T, R> function, Object key, Object val) {
        return jsonSet(col(function), key, val);
    }

    public static FuncExpr jsonSet(FuncExpr field, Object key, Object val) {
        return jsonSet(field.getSql(), key, val);
    }

    public static FuncExpr jsonRemove(String field, Object... keys) {
        return new FuncExpr("JSON_REMOVE(" + field + "," + Arrays.stream(keys).map(FuncBuilder::val).collect(Collectors.joining(",")) + ")");
    }

    public static <T, R> FuncExpr jsonRemove(TypeFunction<T, R> function, Object... keys) {
        return jsonRemove(col(function), keys);
    }

    public static FuncExpr jsonRemove(FuncExpr field, Object... keys) {
        return jsonRemove(field.getSql(), keys);
    }

    /**
     * 键值对均为值字面量：键自动加引号转义，值按 {@code val()} 归一化
     */
    public static FuncExpr jsonObject(Object... keyValues) {
        return new FuncExpr("JSON_OBJECT(" + Arrays.stream(keyValues).map(FuncBuilder::val).collect(Collectors.joining(",")) + ")");
    }

    public static FuncExpr jsonArray(String... fields) {
        return new FuncExpr("JSON_ARRAY(" + Arrays.stream(fields).collect(Collectors.joining(",")) + ")");
    }

    @SafeVarargs
    public static <T, R> FuncExpr jsonArray(TypeFunction<T, R>... functions) {
        return jsonArray(Arrays.stream(functions).map(FuncBuilder::col).toArray(FuncExpr[]::new));
    }

    public static FuncExpr jsonArray(FuncExpr... fields) {
        return new FuncExpr("JSON_ARRAY(" + Arrays.stream(fields).map(FuncExpr::getSql).collect(Collectors.joining(",")) + ")");
    }

    // ==================== 其他函数 ====================

    public static FuncExpr distinct(String field) {
        return new FuncExpr("DISTINCT(" + field + ")");
    }

    public static <T, R> FuncExpr distinct(TypeFunction<T, R> function) {
        return distinct(col(function));
    }

    public static FuncExpr distinct(FuncExpr field) {
        return distinct(field.getSql());
    }

    public static FuncExpr convertUsingGbk(String field) {
        return new FuncExpr("CONVERT(" + field + " USING GBK)");
    }

    public static <T, R> FuncExpr convertUsingGbk(TypeFunction<T, R> function) {
        return convertUsingGbk(col(function));
    }

    public static FuncExpr convertUsingGbk(FuncExpr field) {
        return convertUsingGbk(field.getSql());
    }

    public static FuncExpr unixTimeStamp(String field) {
        return new FuncExpr("UNIX_TIMESTAMP(" + field + ")");
    }

    public static <T, R> FuncExpr unixTimeStamp(TypeFunction<T, R> function) {
        return unixTimeStamp(col(function));
    }

    public static FuncExpr unixTimeStamp(FuncExpr field) {
        return unixTimeStamp(field.getSql());
    }

    public static FuncExpr fromUnixTime(String field, Object formatPattern) {
        return new FuncExpr("FROM_UNIXTIME(" + field + "," + val(formatPattern) + ")");
    }

    public static <T, R> FuncExpr fromUnixTime(TypeFunction<T, R> function, Object formatPattern) {
        return fromUnixTime(col(function), formatPattern);
    }

    public static FuncExpr fromUnixTime(FuncExpr field, Object formatPattern) {
        return fromUnixTime(field.getSql(), formatPattern);
    }
}
