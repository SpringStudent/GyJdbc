package com.gysoft.jdbc.bean;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author 周宁
 */
public class FuncBuilder {

    protected String funcSql;

    public FuncBuilder(String funcSql) {
        this.funcSql = funcSql;
    }

    public String as(String as) {
        return funcSql + " AS " + as;
    }

    public static FuncBuilder lengthAs(String field) {
        return new FuncBuilder(length(field));
    }

    public static <T, R> FuncBuilder lengthAs(TypeFunction<T, R> function) {
        return lengthAs(TypeFunction.getLambdaColumnName(function));
    }

    public static <T, R> String length(TypeFunction<T, R> function) {
        return length(TypeFunction.getLambdaColumnName(function));
    }

    public static String length(String field) {
        return "LENGTH(" + field + ")";
    }

    public static String charLength(String field) {
        return "CHAR_LENGTH(" + field + ")";
    }

    public static <T, R> String charLength(TypeFunction<T, R> function) {
        return charLength(TypeFunction.getLambdaColumnName(function));
    }

    public static FuncBuilder charLengthAs(String field) {
        return new FuncBuilder(charLength(field));
    }

    public static <T, R> FuncBuilder charLengthAs(TypeFunction<T, R> field) {
        return charLengthAs(TypeFunction.getLambdaColumnName(field));
    }

    public static FuncBuilder avgAs(String field) {
        return new FuncBuilder(avg(field));
    }

    public static <T, R> FuncBuilder avgAs(TypeFunction<T, R> function) {
        return avgAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String avg(String field) {
        return "AVG(" + field + ")";
    }

    public static <T, R> String avg(TypeFunction<T, R> function) {
        return avg(TypeFunction.getLambdaColumnName(function));
    }

    public static <T, R> FuncBuilder countAs(TypeFunction<T, R> function) {
        return countAs(TypeFunction.getLambdaColumnName(function));
    }

    public static FuncBuilder countAs(String field) {
        return new FuncBuilder(count(field));
    }

    public static String count(String field) {
        return "COUNT(" + field + ")";
    }

    public static <T, R> String count(TypeFunction<T, R> function) {
        return count(TypeFunction.getLambdaColumnName(function));
    }

    public static FuncBuilder maxAs(String field) {
        return new FuncBuilder(max(field));
    }

    public static <T, R> FuncBuilder maxAs(TypeFunction<T, R> function) {
        return maxAs(TypeFunction.getLambdaColumnName(function));
    }


    public static String max(String field) {
        return "MAX(" + field + ")";
    }

    public static <T, R> String max(TypeFunction<T, R> function) {
        return max(TypeFunction.getLambdaColumnName(function));
    }


    public static FuncBuilder minAs(String field) {
        return new FuncBuilder(min(field));
    }

    public static <T, R> FuncBuilder minAs(TypeFunction<T, R> function) {
        return minAs(TypeFunction.getLambdaColumnName(function));
    }

    public static <T, R> String min(TypeFunction<T, R> function) {
        return min(TypeFunction.getLambdaColumnName(function));
    }

    public static String min(String field) {
        return "MIN(" + field + ")";
    }

    public static FuncBuilder sumAs(String field) {
        return new FuncBuilder(sum(field));
    }

    public static <T, R> FuncBuilder sumAs(TypeFunction<T, R> function) {
        return sumAs(TypeFunction.getLambdaColumnName(function));
    }

    public static <T, R> String sum(TypeFunction<T, R> function) {
        return sum(TypeFunction.getLambdaColumnName(function));
    }


    public static String sum(String field) {
        return "SUM(" + field + ")";
    }

    public static String concat(String... fields) {
        return "CONCAT(" + Arrays.stream(fields).collect(Collectors.joining(",")) + ")";
    }

    public static <T, R> String concat(TypeFunction<T, R>... functions) {
        return concat(Arrays.stream(functions).map(function -> TypeFunction.getLambdaColumnName(function)).collect(Collectors.toList()).toArray(new String[0]));
    }

    public static FuncBuilder concatAs(String... fields) {
        return new FuncBuilder(concat(fields));
    }

    public static <T, R> FuncBuilder concatAs(TypeFunction<T, R>... functions) {
        return concatAs(Arrays.stream(functions).map(function -> TypeFunction.getLambdaColumnName(function)).collect(Collectors.toList()).toArray(new String[0]));
    }

    public static FuncBuilder concat_wsAs(String joinStr, String... fields) {
        return new FuncBuilder(concat_ws(joinStr, fields));
    }

    public static <T, R> FuncBuilder concat_wsAs(String joinStr, TypeFunction<T, R>... functions) {
        return concat_wsAs(joinStr, Arrays.stream(functions).map(function -> TypeFunction.getLambdaColumnName(function)).collect(Collectors.toList()).toArray(new String[0]));
    }

    public static String concat_ws(String joinStr, String... fields) {
        return "CONCAT_WS(" + joinStr + "," + Arrays.stream(fields).collect(Collectors.joining(",")) + ")";
    }
    public static <T, R> String concat_ws(String joinStr, TypeFunction<T, R>... functions) {
        return concat_ws(joinStr, Arrays.stream(functions).map(function -> TypeFunction.getLambdaColumnName(function)).collect(Collectors.toList()).toArray(new String[0]));
    }
    public static FuncBuilder upperAs(String field) {
        return new FuncBuilder(upper(field));
    }

    public static <T, R> FuncBuilder upperAs(TypeFunction<T, R> function) {
        return upperAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String upper(String field) {
        return "UPPER(" + field + ")";
    }

    public static <T, R> String upper(TypeFunction<T, R> field) {
        return upper(TypeFunction.getLambdaColumnName(field));
    }

    public static FuncBuilder lowerAs(String field) {
        return new FuncBuilder(lower(field));
    }

    public static <T, R> FuncBuilder lowerAs(TypeFunction<T, R> function) {
        return lowerAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String lower(String field) {
        return "LOWER(" + field + ")";
    }

    public static <T, R> String lower(TypeFunction<T, R> function) {
        return lower(TypeFunction.getLambdaColumnName(function));
    }

    public static FuncBuilder findInSetAs(String field, String field2) {
        return new FuncBuilder(findInSet(field, field2));
    }

    public static String findInSet(String field, String field2) {
        return "FIND_IN_SET(" + field + "," + field2 + ")";
    }

    public static FuncBuilder locateAs(String field, String field2) {
        return new FuncBuilder(locate(field, field2));
    }

    public static String locate(String field, String field2) {
        return "LOCATE(" + field + "," + field2 + ")";
    }

    public static FuncBuilder positionAs(String field, String field2) {
        return new FuncBuilder(position(field, field2));
    }

    public static String position(String field, String field2) {
        return "POSITION(" + field + " IN " + field2 + ")";
    }


    public static FuncBuilder instrAs(String field, String field2) {
        return new FuncBuilder(instr(field, field2));
    }

    public static String instr(String field, String field2) {
        return "INSTR(" + field + "," + field2 + ")";
    }

    public static FuncBuilder leftAs(String field, int index) {
        return new FuncBuilder(left(field, index));
    }

    public static String left(String field, int index) {
        return "LEFT(" + field + "," + index + ")";
    }

    public static FuncBuilder eltAs(int index, String... fields) {
        return new FuncBuilder(elt(index, fields));
    }

    public static String elt(int index, String... fields) {
        return "ELT(" + index + "," + Arrays.stream(fields).collect(Collectors.joining(",")) + ")";
    }


    public static FuncBuilder rightAs(String field, int index) {
        return new FuncBuilder(right(field, index));
    }

    public static String right(String field, int index) {
        return "RIGHT(" + field + "," + index + ")";
    }

    public static FuncBuilder substringAs(String field, int index, int index2) {
        return new FuncBuilder(substring(field, index, index2));
    }

    public static String substring(String field, int index, int index2) {
        return "SUBSTRING(" + field + "," + index + "," + index2 + ")";
    }

    public static FuncBuilder ltrimAs(String field) {
        return new FuncBuilder(ltrim(field));
    }

    public static <T, R> FuncBuilder ltrimAs(TypeFunction<T, R> function) {
        return ltrimAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String ltrim(String field) {
        return "LTRIM(" + field + ")";
    }

    public static <T, R> String ltrim(TypeFunction<T, R> function) {
        return ltrim(TypeFunction.getLambdaColumnName(function));
    }

    public static FuncBuilder rtrimAs(String field) {
        return new FuncBuilder(rtrim(field));
    }

    public static <T, R> FuncBuilder rtrimAs(TypeFunction<T, R> function) {
        return rtrimAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String rtrim(String field) {
        return "RTRIM(" + field + ")";
    }

    public static <T, R> String rtrim(TypeFunction<T, R> function) {
        return rtrim(TypeFunction.getLambdaColumnName(function));
    }

    public static FuncBuilder trimAs(String field) {
        return new FuncBuilder(trim(field));
    }

    public static <T, R> FuncBuilder trimAs(TypeFunction<T, R> function) {
        return trimAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String trim(String field) {
        return "TRIM(" + field + ")";
    }

    public static <T, R> String trim(TypeFunction<T, R> function) {
        return trim(TypeFunction.getLambdaColumnName(function));
    }

    public static FuncBuilder insertAs(String field, int start, int end, String field2) {
        return new FuncBuilder(insert(field, start, end, field2));
    }

    public static String insert(String field, int start, int end, String field2) {
        return "INSERT(" + field + "," + start + "," + end + "," + field2 + ")";
    }

    public static FuncBuilder replaceAs(String field, String field2, String field3) {
        return new FuncBuilder(replace(field, field2, field3));
    }

    public static String replace(String field, String field2, String field3) {
        return "REPLACE(" + field + "," + field2 + "," + field3 + ")";
    }

    public static FuncBuilder absAs(String field) {
        return new FuncBuilder(abs(field));
    }

    public static <T, R> FuncBuilder absAs(TypeFunction<T, R> function) {
        return absAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String abs(String field) {
        return "ABS(" + field + ")";
    }

    public static <T, R> String abs(TypeFunction<T, R> function) {
        return abs(TypeFunction.getLambdaColumnName(function));
    }

    public static FuncBuilder ceilAs(String field) {
        return new FuncBuilder(ceil(field));
    }

    public static <T, R> FuncBuilder ceilAs(TypeFunction<T, R> function) {
        return ceilAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String ceil(String field) {
        return "CEIL(" + field + ")";
    }

    public static <T, R> String ceil(TypeFunction<T, R> function) {
        return ceil(TypeFunction.getLambdaColumnName(function));
    }


    public static FuncBuilder floorAs(String field) {
        return new FuncBuilder(floor(field));
    }

    public static <T, R> FuncBuilder floorAs(TypeFunction<T, R> function) {
        return floorAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String floor(String field) {
        return "FLOOR(" + field + ")";
    }

    public static <T, R> String floor(TypeFunction<T, R> function) {
        return floor(TypeFunction.getLambdaColumnName(function));
    }

    public static FuncBuilder modAs(String field, String field2) {
        return new FuncBuilder(mod(field, field2));
    }

    public static String mod(String field, String field2) {
        return "MOD(" + field + "," + field2 + ")";
    }

    public static FuncBuilder randAs() {
        return new FuncBuilder(rand());
    }

    public static FuncBuilder randAs(int seed) {
        return new FuncBuilder(rand(seed));
    }

    public static String rand() {
        return "RAND()";
    }

    public static String rand(int seed) {
        return "RAND(" + seed + ")";
    }

    public static FuncBuilder roundAs(String field, int digit) {
        return new FuncBuilder(round(field, digit));
    }

    public static String round(String field, int digit) {
        return "ROUND(" + field + "," + digit + ")";
    }

    public static FuncBuilder truncateAs(String field, int digit) {
        return new FuncBuilder(truncate(field, digit));
    }

    public static String truncate(String field, int digit) {
        return "TRUNCATE(" + field + "," + digit + ")";
    }

    public static FuncBuilder curdateAs() {
        return new FuncBuilder(curdate());
    }

    public static String curdate() {
        return "CURDATE()";
    }

    public static FuncBuilder curtimeAs() {
        return new FuncBuilder(curtime());
    }

    public static String curtime() {
        return "CURTIME()";
    }

    public static FuncBuilder nowAs() {
        return new FuncBuilder(now());
    }

    public static String now() {
        return "NOW()";
    }

    public static FuncBuilder monthAs(String dateField) {
        return new FuncBuilder(month(dateField));
    }

    public static String month(String dateField) {
        return "MONTH(" + dateField + ")";
    }

    public static FuncBuilder monthnameAs(String dateField) {
        return new FuncBuilder(monthname(dateField));
    }

    public static String monthname(String dateField) {
        return "MONTHNAME(" + dateField + ")";
    }

    public static FuncBuilder weekAs(String dateField) {
        return new FuncBuilder(week(dateField));
    }

    public static String week(String dateField) {
        return "WEEK(" + dateField + ")";
    }

    public static FuncBuilder yearAs(String dateField) {
        return new FuncBuilder(year(dateField));
    }

    public static String year(String dateField) {
        return "YEAR(" + dateField + ")";
    }

    public static FuncBuilder hourAs(String timeField) {
        return new FuncBuilder(hour(timeField));
    }

    public static String hour(String timeField) {
        return "HOUR(" + timeField + ")";
    }

    public static FuncBuilder minuteAs(String timeField) {
        return new FuncBuilder(minute(timeField));
    }

    public static String minute(String timeField) {
        return "MINUTE(" + timeField + ")";
    }

    public static FuncBuilder weekdayAs(String dateField) {
        return new FuncBuilder(weekday(dateField));
    }

    public static String weekday(String dateField) {
        return "WEEKDAY(" + dateField + ")";
    }

    public static FuncBuilder daynameAs(String dateField) {
        return new FuncBuilder(dayname(dateField));
    }

    public static String dayname(String dateField) {
        return "DAYNAME(" + dateField + ")";
    }

    public static FuncBuilder distinctAs(String field) {
        return new FuncBuilder(distinct(field));
    }

    public static String distinct(String field) {
        return "DISTINCT(" + field + ")";
    }

    public static <T, R> FuncBuilder distinctAs(TypeFunction<T, R> function) {
        return distinctAs(TypeFunction.getLambdaColumnName(function));
    }

    public static <T, R> String distinct(TypeFunction<T, R> function) {
        return distinct(TypeFunction.getLambdaColumnName(function));
    }

    public static FuncBuilder convertUsingGbkAs(String field) {
        return new FuncBuilder(convertUsingGbk(field));
    }

    public static <T, R> FuncBuilder convertUsingGbkAs(TypeFunction<T, R> function) {
        return convertUsingGbkAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String convertUsingGbk(String field) {
        return "CONVERT(" + field + " USING GBK)";
    }

    public static <T, R> String convertUsingGbk(TypeFunction<T, R> function) {
        return convertUsingGbk(TypeFunction.getLambdaColumnName(function));
    }

    public static FuncBuilder dateFormatAs(String field, String formatPattern) {
        return new FuncBuilder(dateFormat(field, formatPattern));
    }

    public static <T, R> FuncBuilder dateFormatAs(TypeFunction<T, R> function, String formatPattern) {
        return dateFormatAs(TypeFunction.getLambdaColumnName(function), formatPattern);
    }

    public static String dateFormat(String field, String formatPattern) {
        return "DATE_FORMAT(" + field + ",'" + formatPattern + "')";
    }

    public static <T, R> String dateFormat(TypeFunction<T, R> function, String formatPattern) {
        return dateFormat(TypeFunction.getLambdaColumnName(function), formatPattern);
    }

    public static FuncBuilder formatAs(String field, String formatPattern) {
        return new FuncBuilder(format(field, formatPattern));
    }

    public static <T, R> FuncBuilder formatAs(TypeFunction<T, R> function, String formatPattern) {
        return formatAs(TypeFunction.getLambdaColumnName(function), formatPattern);
    }

    public static String format(String field, String formatPattern) {
        return "FORMAT(" + field + ",'" + formatPattern + "')";
    }

    public static <T, R> String format(TypeFunction<T, R> function, String formatPattern) {
        return format(TypeFunction.getLambdaColumnName(function), formatPattern);
    }

    public static FuncBuilder dateSubAs(String field, String express) {
        return new FuncBuilder(dateSub(field, express));
    }

    public static <T, R> FuncBuilder dateSubAs(TypeFunction<T, R> function, String express) {
        return dateSubAs(TypeFunction.getLambdaColumnName(function), express);
    }

    public static String dateSub(String field, String express) {
        return "DATE_SUB(" + field + "," + express + ")";
    }

    public static <T, R> String dateSub(TypeFunction<T, R> function, String express) {
        return dateSub(TypeFunction.getLambdaColumnName(function), express);
    }


    public static FuncBuilder dateAddAs(String field, String express) {
        return new FuncBuilder(dateAdd(field, express));
    }

    public static <T, R> FuncBuilder dateAddAs(TypeFunction<T, R> function, String express) {
        return dateAddAs(TypeFunction.getLambdaColumnName(function), express);
    }

    public static String dateAdd(String field, String express) {
        return "DATE_ADD(" + field + "," + express + ")";
    }

    public static <T, R> String dateAdd(TypeFunction<T, R> function, String express) {
        return dateAdd(TypeFunction.getLambdaColumnName(function), express);
    }

    public static FuncBuilder strToDateAs(String field, String formatPattern) {
        return new FuncBuilder(strToDate(field, formatPattern));
    }

    public static <T, R> FuncBuilder strToDateAs(TypeFunction<T, R> function, String formatPattern) {
        return strToDateAs(TypeFunction.getLambdaColumnName(function), formatPattern);
    }

    public static String strToDate(String field, String formatPattern) {
        return "STR_TO_DATE(" + field + ",'" + formatPattern + "')";
    }

    public static <T, R> String strToDate(TypeFunction<T, R> function, String formatPattern) {
        return strToDate(TypeFunction.getLambdaColumnName(function), formatPattern);
    }

    public static FuncBuilder ifNullAs(String field, Object val) {
        return new FuncBuilder(ifNull(field, val));
    }

    public static <T, R> FuncBuilder ifNullAs(TypeFunction<T, R> function, Object val) {
        return ifNullAs(TypeFunction.getLambdaColumnName(function), val);
    }

    public static String ifNull(String field, Object val) {
        return "IFNULL(" + field + "," + val + ")";
    }

    public static <T, R> String ifNull(TypeFunction<T, R> function, Object val) {
        return ifNull(TypeFunction.getLambdaColumnName(function), val);
    }

    public static FuncBuilder ifAs(String express, Object val1, Object val2) {
        return new FuncBuilder(_if(express, val1, val2));
    }

    public static <T, R> FuncBuilder ifAs(TypeFunction<T, R> function, Object val1, Object val2) {
        return ifAs(TypeFunction.getLambdaColumnName(function), val1, val2);
    }

    public static String _if(String express, Object val1, Object val2) {
        return "IF(" + express + "," + val1 + "," + val2 + ")";
    }

    public static <T, R> String _if(TypeFunction<T, R> function, Object val1, Object val2) {
        return _if(TypeFunction.getLambdaColumnName(function), val1, val2);
    }

    public static FuncBuilder unixTimeStampAs(String field) {
        return new FuncBuilder(unixTimeStamp(field));
    }

    public static <T, R> FuncBuilder unixTimeStampAs(TypeFunction<T, R> function) {
        return unixTimeStampAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String unixTimeStamp(String field) {
        return "UNIX_TIMESTAMP(" + field + ")";
    }

    public static <T, R> String unixTimeStamp(TypeFunction<T, R> function) {
        return unixTimeStamp(TypeFunction.getLambdaColumnName(function));
    }

    public static FuncBuilder fromUnixTimeAs(String field, String formatPattern) {
        return new FuncBuilder(fromUnixTime(field, formatPattern));
    }

    public static <T, R> FuncBuilder fromUnixTimeAs(TypeFunction<T, R> function, String formatPattern) {
        return fromUnixTimeAs(TypeFunction.getLambdaColumnName(function), formatPattern);
    }

    public static String fromUnixTime(String field, String formatPattern) {
        return "FROM_UNIXTIME(" + field + ",'" + formatPattern + "')";
    }

    public static <T, R> String fromUnixTime(TypeFunction<T, R> function, String formatPattern) {
        return fromUnixTime(TypeFunction.getLambdaColumnName(function), formatPattern);
    }

    public static FuncBuilder dateAs(String field) {
        return new FuncBuilder(date(field));
    }

    public static <T, R> FuncBuilder dateAs(TypeFunction<T, R> function) {
        return dateAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String date(String field) {
        return "DATE(" + field + ")";
    }

    public static <T, R> String date(TypeFunction<T, R> function) {
        return date(TypeFunction.getLambdaColumnName(function));
    }

    public static String jsonExtract(String field, String $key) {
        return "json_extract(" + field + ",'" + $key + "')";
    }

    public static <T, R> String jsonExtract(TypeFunction<T, R> function, String $key) {
        return jsonExtract(TypeFunction.getLambdaColumnName(function), $key);
    }

    public static FuncBuilder jsonExtractAs(String field, String $key) {
        return new FuncBuilder(jsonExtract(field, $key));
    }

    public static <T, R> FuncBuilder jsonExtractAs(TypeFunction<T, R> function, String $key) {
        return jsonExtractAs(TypeFunction.getLambdaColumnName(function), $key);
    }

}
