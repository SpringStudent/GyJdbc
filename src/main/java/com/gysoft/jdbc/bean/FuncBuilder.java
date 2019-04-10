package com.gysoft.jdbc.bean;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author 周宁
 */
public class FuncBuilder {

    public static AbstractFuncBuilder lengthAs(String field) {
        return new LengthFuncBuilder(field);
    }

    public static <T, R> AbstractFuncBuilder lengthAs(TypeFunction<T, R> function) {
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

    public static AbstractFuncBuilder charLengthAs(String field) {
        return new CharLengthFuncBuilder(field);
    }

    public static <T, R> AbstractFuncBuilder charLengthAs(TypeFunction<T, R> field) {
        return charLengthAs(TypeFunction.getLambdaColumnName(field));
    }

    public static AbstractFuncBuilder avgAs(String field) {
        return new AvgFuncBuilder(field);
    }

    public static <T, R> AbstractFuncBuilder avgAs(TypeFunction<T, R> function) {
        return avgAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String avg(String field) {
        return "AVG(" + field + ")";
    }

    public static <T, R> String avg(TypeFunction<T, R> function) {
        return avg(TypeFunction.getLambdaColumnName(function));
    }

    public static <T, R> AbstractFuncBuilder countAs(TypeFunction<T, R> function) {
        return countAs(TypeFunction.getLambdaColumnName(function));
    }

    public static AbstractFuncBuilder countAs(String field) {
        return new CountFuncBuilder(field);
    }

    public static String count(String field) {
        return "COUNT(" + field + ")";
    }

    public static <T, R> String count(TypeFunction<T, R> function) {
        return count(TypeFunction.getLambdaColumnName(function));
    }


    public static AbstractFuncBuilder maxAs(String field) {
        return new MaxFuncBuilder(field);
    }

    public static <T, R> AbstractFuncBuilder maxAs(TypeFunction<T, R> function) {
        return maxAs(TypeFunction.getLambdaColumnName(function));
    }


    public static String max(String field) {
        return "MAX(" + field + ")";
    }

    public static <T, R> String max(TypeFunction<T, R> function) {
        return max(TypeFunction.getLambdaColumnName(function));
    }


    public static AbstractFuncBuilder minAs(String field) {
        return new MinFuncBuilder(field);
    }

    public static <T, R> AbstractFuncBuilder minAs(TypeFunction<T, R> function) {
        return minAs(TypeFunction.getLambdaColumnName(function));
    }

    public static <T, R> String min(TypeFunction<T, R> function) {
        return min(TypeFunction.getLambdaColumnName(function));
    }

    public static String min(String field) {
        return "MIN(" + field + ")";
    }

    public static AbstractFuncBuilder sumAs(String field) {
        return new SumFuncBuilder(field);
    }

    public static <T, R> AbstractFuncBuilder sumAs(TypeFunction<T, R> function) {
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

    public static AbstractFuncBuilder concatAs(String... fields) {
        return new ConcatFuncBuilder(fields);
    }

    public static <T, R> AbstractFuncBuilder concatAs(TypeFunction<T, R>... functions) {
        return concatAs(Arrays.stream(functions).map(function -> TypeFunction.getLambdaColumnName(function)).collect(Collectors.toList()).toArray(new String[0]));
    }

    public static AbstractFuncBuilder concat_wsAs(String joinStr, String... fields) {
        return new ConcatWsFuncBuilder(joinStr, fields);
    }

    public static <T, R> AbstractFuncBuilder concat_wsAs(String joinStr, TypeFunction<T, R>... functions) {
        return concat_wsAs(joinStr, Arrays.stream(functions).map(function -> TypeFunction.getLambdaColumnName(function)).collect(Collectors.toList()).toArray(new String[0]));
    }

    public static String concat_ws(String joinStr, String... fields) {
        return "CONCAT_WS(" + joinStr + "," + Arrays.stream(fields).collect(Collectors.joining(",")) + ")";
    }

    public static <T, R> String concat_ws(String joinStr, TypeFunction<T, R>... functions) {
        return concat_ws(joinStr, Arrays.stream(functions).map(function -> TypeFunction.getLambdaColumnName(function)).collect(Collectors.toList()).toArray(new String[0]));
    }


    public static AbstractFuncBuilder upperAs(String field) {
        return new UpperFuncBuilder(field);
    }

    public static <T,R>AbstractFuncBuilder upperAs(TypeFunction<T,R> function) {
        return upperAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String upper(String field) {
        return "UPPER(" + field + ")";
    }

    public static <T,R>String upper(TypeFunction<T,R> field) {
        return upper(TypeFunction.getLambdaColumnName(field));
    }

    public static AbstractFuncBuilder lowerAs(String field) {
        return new LowerFuncBuilder(field);
    }

    public static <T,R>AbstractFuncBuilder lowerAs(TypeFunction<T,R> function) {
        return lowerAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String lower(String field) {
        return "LOWER(" + field + ")";
    }

    public static <T,R>String lower(TypeFunction<T,R> function) {
        return lower(TypeFunction.getLambdaColumnName(function));
    }

    private static class ConcatWsFuncBuilder extends AbstractFuncBuilder {
        private ConcatWsFuncBuilder(String joinStr, String... fields) {
            this.funcSql = "CONCAT_WS(" + joinStr + "," + Arrays.stream(fields).collect(Collectors.joining(",")) + ")";
        }
    }

    private static class ConcatFuncBuilder extends AbstractFuncBuilder {
        private ConcatFuncBuilder(String... fields) {
            this.funcSql = "CONCAT(" + Arrays.stream(fields).collect(Collectors.joining(",")) + ")";
        }
    }

    private static class SumFuncBuilder extends AbstractFuncBuilder {
        private SumFuncBuilder(String field) {
            this.funcSql = "SUM(" + field + ")";
        }
    }

    private static class MinFuncBuilder extends AbstractFuncBuilder {
        private MinFuncBuilder(String field) {
            this.funcSql = "MIN(" + field + ")";
        }
    }

    private static class MaxFuncBuilder extends AbstractFuncBuilder {
        private MaxFuncBuilder(String field) {
            this.funcSql = "MAX(" + field + ")";
        }
    }

    private static class CountFuncBuilder extends AbstractFuncBuilder {
        private CountFuncBuilder(String field) {
            this.funcSql = "COUNT(" + field + ")";
        }
    }

    private static class AvgFuncBuilder extends AbstractFuncBuilder {
        private AvgFuncBuilder(String field) {
            this.funcSql = "AVG(" + field + ")";
        }
    }

    private static class LengthFuncBuilder extends AbstractFuncBuilder {
        private LengthFuncBuilder(String field) {
            this.funcSql = "LENGTH(" + field + ")";
        }
    }

    private static class CharLengthFuncBuilder extends AbstractFuncBuilder {
        private CharLengthFuncBuilder(String field) {
            this.funcSql = "CHAR_LENGTH(" + field + ")";
        }
    }

    private static class UpperFuncBuilder extends AbstractFuncBuilder {
        private UpperFuncBuilder(String field) {
            this.funcSql = "UPPER(" + field + ")";
        }
    }

    private static class LowerFuncBuilder extends AbstractFuncBuilder {
        private LowerFuncBuilder(String field) {
            this.funcSql = "LOWER(" + field + ")";
        }
    }

    private static class FindInSetFuncBuilder extends AbstractFuncBuilder {
        private FindInSetFuncBuilder(String field, String field2) {
            this.funcSql = "FIND_IN_SET(" + field + "," + field2 + ")";
        }
    }

    public static AbstractFuncBuilder findInSetAs(String field, String field2) {
        return new FindInSetFuncBuilder(field, field2);
    }

    public static String findInSet(String field, String field2) {
        return "FIND_IN_SET(" + field + "," + field2 + ")";
    }

    private static class LocateFuncBuilder extends AbstractFuncBuilder {
        private LocateFuncBuilder(String field, String field2) {
            this.funcSql = "LOCATE(" + field + "," + field2 + ")";
        }
    }

    public static AbstractFuncBuilder locateAs(String field, String field2) {
        return new LocateFuncBuilder(field, field2);
    }

    public static String locate(String field, String field2) {
        return "LOCATE(" + field + "," + field2 + ")";
    }

    private static class PositionFuncBuilder extends AbstractFuncBuilder {
        private PositionFuncBuilder(String field, String field2) {
            this.funcSql = "POSITION(" + field + " IN " + field2 + ")";
        }
    }

    public static AbstractFuncBuilder positionAs(String field, String field2) {
        return new PositionFuncBuilder(field, field2);
    }

    public static String position(String field, String field2) {
        return "POSITION(" + field + " IN " + field2 + ")";
    }

    private static class InstrFuncBuilder extends AbstractFuncBuilder {
        private InstrFuncBuilder(String field, String field2) {
            this.funcSql = "INSTR(" + field + "," + field2 + ")";
        }
    }

    public static AbstractFuncBuilder instrAs(String field, String field2) {
        return new InstrFuncBuilder(field, field2);
    }

    public static String instr(String field, String field2) {
        return "INSTR(" + field + "," + field2 + ")";
    }

    private static class LeftFuncBuilder extends AbstractFuncBuilder {
        private LeftFuncBuilder(String field, int index) {
            this.funcSql = "LEFT(" + field + "," + index + ")";
        }
    }

    public static AbstractFuncBuilder leftAs(String field, int index) {
        return new LeftFuncBuilder(field, index);
    }

    public static String left(String field, int index) {
        return "LEFT(" + field + "," + index + ")";
    }

    private static class EltFuncBuilder extends AbstractFuncBuilder {
        private EltFuncBuilder(int index, String... fields) {
            this.funcSql = "ELT(" + index + "," + Arrays.stream(fields).collect(Collectors.joining(",")) + ")";
        }
    }

    public static AbstractFuncBuilder eltAs(int index, String... fields) {
        return new EltFuncBuilder(index, fields);
    }

    public static String elt(int index, String... fields) {
        return "ELT(" + index + "," + Arrays.stream(fields).collect(Collectors.joining(",")) + ")";
    }

    private static class RightFuncBuilder extends AbstractFuncBuilder {
        private RightFuncBuilder(String field, int index) {
            this.funcSql = "RIGHT(" + field + "," + index + ")";

        }
    }

    public static AbstractFuncBuilder rightAs(String field, int index) {
        return new RightFuncBuilder(field, index);
    }

    public static String right(String field, int index) {
        return "RIGHT(" + field + "," + index + ")";
    }

    private static class SubStringFuncBuilder extends AbstractFuncBuilder {
        private SubStringFuncBuilder(String field, int index, int index2) {
            this.funcSql = "SUBSTRING(" + field + "," + index + "," + index2 + ")";
        }
    }

    public static AbstractFuncBuilder substringAs(String field, int index, int index2) {
        return new SubStringFuncBuilder(field, index, index2);
    }

    public static String substring(String field, int index, int index2) {
        return "SUBSTRING(" + field + "," + index + "," + index2 + ")";
    }

    private static class LtrimFuncBuilder extends AbstractFuncBuilder {
        private LtrimFuncBuilder(String field) {
            this.funcSql = "LTRIM(" + field + ")";
        }
    }

    public static AbstractFuncBuilder ltrimAs(String field) {
        return new LtrimFuncBuilder(field);
    }

    public static <T, R> AbstractFuncBuilder ltrimAs(TypeFunction<T, R> function) {
        return ltrimAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String ltrim(String field) {
        return "LTRIM(" + field + ")";
    }

    public static <T, R> String ltrim(TypeFunction<T, R> function) {
        return ltrim(TypeFunction.getLambdaColumnName(function));
    }

    private static class RtrimFuncBuilder extends AbstractFuncBuilder {
        private RtrimFuncBuilder(String field) {
            this.funcSql = "RTRIM(" + field + ")";
        }
    }

    public static AbstractFuncBuilder rtrimAs(String field) {
        return new RtrimFuncBuilder(field);
    }

    public static <T, R> AbstractFuncBuilder rtrimAs(TypeFunction<T, R> function) {
        return rtrimAs(TypeFunction.getLambdaColumnName(function));
    }


    public static String rtrim(String field) {
        return "RTRIM(" + field + ")";
    }

    public static <T, R> String rtrim(TypeFunction<T, R> function) {
        return rtrim(TypeFunction.getLambdaColumnName(function));
    }

    private static class TrimFuncBuilder extends AbstractFuncBuilder {
        private TrimFuncBuilder(String field) {
            this.funcSql = "TRIM(" + field + ")";
        }

    }

    public static AbstractFuncBuilder trimAs(String field) {
        return new TrimFuncBuilder(field);
    }

    public static <T, R> AbstractFuncBuilder trimAs(TypeFunction<T, R> function) {
        return trimAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String trim(String field) {
        return "TRIM(" + field + ")";
    }

    public static <T, R> String trim(TypeFunction<T, R> function) {
        return trim(TypeFunction.getLambdaColumnName(function));
    }


    private static class InsertFuncBuilder extends AbstractFuncBuilder {
        private InsertFuncBuilder(String field, int start, int end, String field2) {
            this.funcSql = "INSERT(" + field + "," + start + "," + end + "," + field2 + ")";
        }
    }

    public static AbstractFuncBuilder insertAs(String field, int start, int end, String field2) {
        return new InsertFuncBuilder(field, start, end, field2);
    }

    public static String insert(String field, int start, int end, String field2) {
        return "INSERT(" + field + "," + start + "," + end + "," + field2 + ")";
    }

    private static class ReplaceFuncBuilder extends AbstractFuncBuilder {
        private ReplaceFuncBuilder(String field, String field2, String field3) {
            this.funcSql = "REPLACE(" + field + "," + field2 + "," + field3 + ")";
        }
    }

    public static AbstractFuncBuilder replaceAs(String field, String field2, String field3) {
        return new ReplaceFuncBuilder(field, field2, field3);
    }

    public static String replace(String field, String field2, String field3) {
        return "REPLACE(" + field + "," + field2 + "," + field3 + ")";
    }

    private static class AbsFuncBuilder extends AbstractFuncBuilder {
        private AbsFuncBuilder(String field) {
            this.funcSql = "ABS(" + field + ")";
        }
    }

    public static AbstractFuncBuilder absAs(String field) {
        return new AbsFuncBuilder(field);
    }

    public static <T, R> AbstractFuncBuilder absAs(TypeFunction<T, R> function) {
        return absAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String abs(String field) {
        return "ABS(" + field + ")";
    }

    public static <T, R> String abs(TypeFunction<T, R> function) {
        return abs(TypeFunction.getLambdaColumnName(function));
    }

    private static class CeilFuncBuilder extends AbstractFuncBuilder {
        private CeilFuncBuilder(String field) {
            this.funcSql = "CEIL(" + field + ")";
        }
    }

    public static AbstractFuncBuilder ceilAs(String field) {
        return new CeilFuncBuilder(field);
    }

    public static <T, R> AbstractFuncBuilder ceilAs(TypeFunction<T, R> function) {
        return ceilAs(TypeFunction.getLambdaColumnName(function));
    }


    public static String ceil(String field) {
        return "CEIL(" + field + ")";
    }

    public static <T, R> String ceil(TypeFunction<T, R> function) {
        return ceil(TypeFunction.getLambdaColumnName(function));
    }

    private static class FloorFuncBuilder extends AbstractFuncBuilder {
        private FloorFuncBuilder(String field) {
            this.funcSql = "FLOOR(" + field + ")";
        }
    }

    public static AbstractFuncBuilder floorAs(String field) {
        return new FloorFuncBuilder(field);
    }

    public static <T, R> AbstractFuncBuilder floorAs(TypeFunction<T, R> function) {
        return floorAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String floor(String field) {
        return "FLOOR(" + field + ")";
    }

    public static <T, R> String floor(TypeFunction<T, R> function) {
        return floor(TypeFunction.getLambdaColumnName(function));
    }

    private static class ModFuncBuilder extends AbstractFuncBuilder {
        private ModFuncBuilder(String field, String field2) {
            this.funcSql = "MOD(" + field + "," + field2 + ")";
        }
    }

    public static AbstractFuncBuilder modAs(String field, String field2) {
        return new ModFuncBuilder(field, field2);
    }

    public static String mod(String field, String field2) {
        return "MOD(" + field + "," + field2 + ")";
    }

    private static class RandFuncBuilder extends AbstractFuncBuilder {
        private RandFuncBuilder() {
            this.funcSql = "RAND()";
        }

        private RandFuncBuilder(int seed) {
            this.funcSql = "RAND(" + seed + ")";
        }
    }

    public static AbstractFuncBuilder randAs() {
        return new RandFuncBuilder();
    }

    public static AbstractFuncBuilder randAs(int seed) {
        return new RandFuncBuilder(seed);
    }

    public static String rand() {
        return "RAND()";
    }

    public static String rand(int seed) {
        return "RAND(" + seed + ")";
    }

    private static class RoundFuncBuilder extends AbstractFuncBuilder {
        private RoundFuncBuilder(String field, int digit) {
            this.funcSql = "ROUND(" + field + "," + digit + ")";
        }
    }

    public static AbstractFuncBuilder roundAs(String field, int digit) {
        return new RoundFuncBuilder(field, digit);
    }

    public static String round(String field, int digit) {
        return "ROUND(" + field + "," + digit + ")";
    }

    private static class TruncateFuncBuilder extends AbstractFuncBuilder {
        private TruncateFuncBuilder(String field, int digit) {
            this.funcSql = "TRUNCATE(" + field + "," + digit + ")";

        }
    }

    public static AbstractFuncBuilder truncateAs(String field, int digit) {
        return new TruncateFuncBuilder(field, digit);
    }

    public static String truncate(String field, int digit) {
        return "TRUNCATE(" + field + "," + digit + ")";
    }

    private static class CurDateFuncBuilder extends AbstractFuncBuilder {
        private CurDateFuncBuilder() {
            this.funcSql = "CURDATE()";
        }
    }

    public static AbstractFuncBuilder curdateAs() {
        return new CurDateFuncBuilder();
    }

    public static String curdate() {
        return "CURDATE()";
    }

    private static class CurTimeFuncBuilder extends AbstractFuncBuilder {
        private CurTimeFuncBuilder() {
            this.funcSql = "CURTIME()";
        }
    }

    public static AbstractFuncBuilder curtimeAs() {
        return new CurTimeFuncBuilder();
    }

    public static String curtime() {
        return "CURTIME()";
    }

    private static class NowFuncBuilder extends AbstractFuncBuilder {
        private NowFuncBuilder() {
            this.funcSql = "NOW()";
        }
    }

    public static AbstractFuncBuilder nowAs() {
        return new NowFuncBuilder();
    }

    public static String now() {
        return "NOW()";
    }

    private static class MonthFuncBuilder extends AbstractFuncBuilder {
        private MonthFuncBuilder(String dateField) {
            this.funcSql = "MONTH(" + dateField + ")";
        }
    }

    public static AbstractFuncBuilder monthAs(String dateField) {
        return new MonthFuncBuilder(dateField);
    }

    public static String month(String dateField) {
        return "MONTH(" + dateField + ")";
    }

    private static class MonthNameFuncBuilder extends AbstractFuncBuilder {
        private MonthNameFuncBuilder(String dateField) {
            this.funcSql = "MONTHNAME(" + dateField + ")";
        }
    }

    public static AbstractFuncBuilder monthnameAs(String dateField) {
        return new MonthNameFuncBuilder(dateField);
    }

    public static String monthname(String dateField) {
        return "MONTHNAME(" + dateField + ")";
    }

    private static class WeekFuncBuilder extends AbstractFuncBuilder {
        private WeekFuncBuilder(String dateField) {
            this.funcSql = "WEEK(" + dateField + ")";
        }
    }

    public static AbstractFuncBuilder weekAs(String dateField) {
        return new WeekFuncBuilder(dateField);
    }

    public static String week(String dateField) {
        return "WEEK(" + dateField + ")";
    }

    private static class YearFuncBuilder extends AbstractFuncBuilder {
        private YearFuncBuilder(String dateField) {
            this.funcSql = "YEAR(" + dateField + ")";
        }
    }

    public static AbstractFuncBuilder yearAs(String dateField) {
        return new YearFuncBuilder(dateField);
    }

    public static String year(String dateField) {
        return "YEAR(" + dateField + ")";
    }

    private static class HourFuncBuilder extends AbstractFuncBuilder {
        private HourFuncBuilder(String timeField) {
            this.funcSql = "HOUR(" + timeField + ")";
        }
    }

    public static AbstractFuncBuilder hourAs(String timeField) {
        return new HourFuncBuilder(timeField);
    }

    public static String hour(String timeField) {
        return "HOUR(" + timeField + ")";
    }

    private static class MinuteFuncBuilder extends AbstractFuncBuilder {
        private MinuteFuncBuilder(String timeField) {
            this.funcSql = "MINUTE(" + timeField + ")";
        }
    }

    public static AbstractFuncBuilder minuteAs(String timeField) {
        return new MinuteFuncBuilder(timeField);
    }

    public static String minute(String timeField) {
        return "MINUTE(" + timeField + ")";
    }

    private static class WeekDayFuncBuilder extends AbstractFuncBuilder {
        private WeekDayFuncBuilder(String dateField) {
            this.funcSql = "WEEKDAY(" + dateField + ")";
        }
    }

    public static AbstractFuncBuilder weekdayAs(String dateField) {
        return new WeekDayFuncBuilder(dateField);
    }

    public static String weekday(String dateField) {
        return "WEEKDAY(" + dateField + ")";
    }

    private static class DayNameFuncBuilder extends AbstractFuncBuilder {
        private DayNameFuncBuilder(String dateField) {
            this.funcSql = "DAYNAME(" + dateField + ")";
        }
    }

    public static AbstractFuncBuilder daynameAs(String dateField) {
        return new DayNameFuncBuilder(dateField);
    }

    public static String dayname(String dateField) {
        return "DAYNAME(" + dateField + ")";
    }

    private static class DistinctFuncBuilder extends AbstractFuncBuilder {
        private DistinctFuncBuilder(String field) {
            this.funcSql = "DISTINCT(" + field + ")";
        }
    }

    public static AbstractFuncBuilder distinctAs(String field) {
        return new DistinctFuncBuilder(field);
    }

    public static String distinct(String field) {
        return "DISTINCT(" + field + ")";
    }

    public static <T, R> AbstractFuncBuilder distinctAs(TypeFunction<T, R> function) {
        return distinctAs(TypeFunction.getLambdaColumnName(function));
    }

    public static <T, R> String distinct(TypeFunction<T, R> function) {
        return distinct(TypeFunction.getLambdaColumnName(function));
    }

    private static class ConvertUsingGbkFuncBuilder extends AbstractFuncBuilder{
        private ConvertUsingGbkFuncBuilder(String field){
            this.funcSql = "CONVERT("+field+" USING GBK)";
        }
    }

    public static AbstractFuncBuilder convertUsingGbkAs(String field) {
        return new ConvertUsingGbkFuncBuilder(field);
    }

    public static <T,R>AbstractFuncBuilder convertUsingGbkAs(TypeFunction<T,R> function) {
        return convertUsingGbkAs(TypeFunction.getLambdaColumnName(function));
    }

    public static String convertUsingGbk(String field) {
        return "CONVERT("+field+" USING GBK)";
    }

    public static <T,R>String convertUsingGbk(TypeFunction<T,R> function) {
        return convertUsingGbk(TypeFunction.getLambdaColumnName(function));
    }

    public static abstract class AbstractFuncBuilder {
        protected String funcSql;

        public String as(String as) {
            return funcSql + " AS " + as;
        }
    }

    private static class DateFormatFuncBuilder extends AbstractFuncBuilder {

        private DateFormatFuncBuilder(String field, String formatPattern) {
            this.funcSql = "DATE_FORMAT(" + field + ",'" + formatPattern + "')";
        }

    }

    public static AbstractFuncBuilder dateFormatAs(String field, String formatPattern) {
        return new DateFormatFuncBuilder(field, formatPattern);
    }

    public static <T, R> AbstractFuncBuilder dateFormatAs(TypeFunction<T, R> function, String formatPattern) {
        return dateFormatAs(TypeFunction.getLambdaColumnName(function), formatPattern);
    }

    public static String dateFormat(String field, String formatPattern) {
        return "DATE_FORMAT(" + field + ",'" + formatPattern + "')";
    }

    public static <T, R> String dateFormat(TypeFunction<T, R> function, String formatPattern) {
        return dateFormat(TypeFunction.getLambdaColumnName(function), formatPattern);
    }

    public static class FormatFuncBuilder extends AbstractFuncBuilder {
        private FormatFuncBuilder(String field, String formatPattern) {
            this.funcSql = "FORMAT(" + field + ",'" + formatPattern + "')";
        }
    }

    public static AbstractFuncBuilder formatAs(String field, String formatPattern) {
        return new FormatFuncBuilder(field, formatPattern);
    }

    public static <T, R> AbstractFuncBuilder formatAs(TypeFunction<T, R> function, String formatPattern) {
        return formatAs(TypeFunction.getLambdaColumnName(function), formatPattern);
    }

    public static String format(String field, String formatPattern) {
        return "FORMAT(" + field + ",'" + formatPattern + "')";
    }

    public static <T, R> String format(TypeFunction<T, R> function, String formatPattern) {
        return format(TypeFunction.getLambdaColumnName(function), formatPattern);
    }
}
