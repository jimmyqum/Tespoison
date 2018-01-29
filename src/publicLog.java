import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author quming
 *
 */
public class publicLog {

	public static class sys {
		//macos目录
		protected static String basePath = "/Users/jimmy/临时/项目/";
		protected static String logPath ="/slowsql";
		//是否输出明细
		protected static boolean CSV_all = false;
		//是否输出分析
		protected static boolean CSV_2 = true;

		//限定日期
		protected static boolean isDate = true;
		protected static String isDateStr = "2018-01-12";

		//windows目录
		//protected static String = "E:\\workspace\\";
		//protected static String = "\\serverlog";
	}

}
