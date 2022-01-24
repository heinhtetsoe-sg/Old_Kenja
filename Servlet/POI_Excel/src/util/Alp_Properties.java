package util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//定義ファイルを読み出す
public class Alp_Properties {
	Log log = LogFactory.getLog(Alp_Properties.class);
	//定義
	static final String[] weekString = {"日", "月", "火", "水", "木", "金", "土"};
	// 定義ファイル
	private static final String PROPERTI_FILE = "/Alp.properties";
	private static String PROPERTI_PATH;

	// コンストラクタ
	// パラメータ：サーブレットのコンテキスト
	public Alp_Properties(ServletContext contxt) {
		PROPERTI_PATH = contxt.getRealPath(PROPERTI_FILE);
	}

	// 設定値取出し
	// パラメータ：取出すプロパティ名称
	// 戻り値：取出したプロパティ値文字列
	public String getProperty(String Property) {
		Properties prop = new Properties();
		try {
			InputStream in = new FileInputStream(PROPERTI_PATH);
			prop.load(new InputStreamReader(in, "UTF-8"));
			return prop.getProperty(Property);
		} catch (Exception e) {
			StackTrace(e);
			return null;
		}
	}

	//西暦日付から和暦年号を取出す。
	public String getJapanYear(int Y, int M, int D) {
		String[] nen = getJapanYear0(Y, M, D);
		return nen[0] + nen[1] + "年";
	}

	//西暦日付から和暦年号と和暦年を取出す。
	public String[] getJapanYear0(int Y, int M, int D) {
		Properties prop = new Properties();
		try {
			InputStream in = new FileInputStream(PROPERTI_PATH);
			prop.load(new InputStreamReader(in, "UTF-8"));
		} catch (Exception e) {
			StackTrace(e);
			return null;
		}
		String[] Value = {"", ""};
		//日付を数値化
		int cmp = Y * 10000;
		if (M == 0)
			cmp += 100;
		else
			cmp += (M * 100);
		if (D == 0)
			cmp += 1;
		else
			cmp += D;
		//log.info(String.format("和暦取出し[%d]", cmp));
		int n = Integer.valueOf(prop.getProperty("JYN"));
		int start = 18680908;
		for(int i = 0; i <= n; i++) {
			String key = "JY" + String.valueOf(i);
			String value = prop.getProperty(key);
			String[] text = value.split(",", 2);
			int ymd = Integer.valueOf(text[0]);
			//log.info(String.format("比較[%d]", ymd));
			if (ymd >= cmp) {
				int yy = (cmp / 10000) - (start / 10000);
				Value[0] = text[1];
				if (yy == 0) {
					Value[1] = "元";
				}
				else {
					Value[1] = String.format("%2d", yy + 1);
				}
				return Value;
			}
			start = ymd;
		}
		return Value;
	}

	//西暦日付から和暦年のみを取出す。
	public String getJYear(int Y, int M, int D) {
		String[] nen = getJapanYear0(Y, M, D);
		return nen[1];
	}

	//Dateから和暦年日付を取出す
	public String DateToJdayString(Date date) {
		String[] D = date.toString().split("-");
		int y = Integer.valueOf(D[0]);
		int m = Integer.valueOf(D[1]);
		int d = Integer.valueOf(D[0]);
		String jday = getJapanYear(y, m, d);
		jday += String.format("%02d月%02d日", m, d);
		return jday;
	}

	//西暦日付から和暦年日付を取出す→年号99年99月99日(曜日)
	//パラメーターはCalendar
	public String getJDayString0(Calendar Day) {
		int[] calendarDay = new int[3];
		calendarDay[0] = Day.get(Calendar.YEAR);
		calendarDay[1] = Day.get(Calendar.MONTH) + 1;
		calendarDay[2] = Day.get(Calendar.DATE);
		String day = getJapanYear(calendarDay[0], calendarDay[1], calendarDay[2]);
		day += String.format("%02d月%02d日", calendarDay[1], calendarDay[2]);
		return day;
	}

	//西暦日付から和暦年日付を取出す→年号99年99月99日(曜日)
	//パラメーターはCalendar
	public String getJDayString(Calendar Day) {
		String day = getJDayString0(Day);
		int week = Day.get(Calendar.DAY_OF_WEEK);
		day += ("(" + weekString[week - 1] + ")");
		return day;
	}

	//西暦日付から曜日を取出す
	//パラメーターはCalendar
	public String getWeek(Calendar Day) {
		int week = Day.get(Calendar.DAY_OF_WEEK);
		return weekString[week - 1];
	}

	//日にちの差を取出す
	public int calcuDayDefference(Calendar Start, Calendar End) {
		int[] start = new int[3];
		int[] end = new int[3];
		int count = 0;
		int yearMaxDay;
		start[0] = Start.get(Calendar.YEAR);
		start[1] = Start.get(Calendar.MONTH);
		start[2] = Start.get(Calendar.DATE);
		end[0] = End.get(Calendar.YEAR);
		end[1] = End.get(Calendar.MONTH);
		end[2] = End.get(Calendar.DATE);
		Calendar cl = Calendar.getInstance();
		if (end[0] > start[0]) {
			cl.set(start[0], 11, 31, 12, 0);
			yearMaxDay = cl.get(Calendar.DAY_OF_YEAR);
			count = yearMaxDay - Start.get(Calendar.DAY_OF_YEAR);
			int s_d = start[0], e_d = end[0];
			while(++s_d < e_d) {
				cl.set(s_d, 11, 31, 12, 0);
				count += cl.get(Calendar.DAY_OF_YEAR);
			}
			count += End.get(Calendar.DAY_OF_YEAR);
		}
		else if (end[0] == start[0]) {
			if ((end[1] > start[1]) || ((end[1] == start[1]) && (end[2] > start[2]))) {
				int endDay = End.get(Calendar.DAY_OF_YEAR);
				count += (endDay - Start.get(Calendar.DAY_OF_YEAR));
			}
		}
		return count;
	}

	//午前・午後制の時刻表記を取出す
	//　パラメータtime："00:00"～23:59"
	public String getJTimeString(String time) {
		String jTime = "";
		String[] ts = (time != null) ? time.split(":") : "0".split(":");
		if (ts.length != 2) {
			return "";
		}
		int T = Integer.valueOf(ts[0]);
		int M = Integer.valueOf(ts[1]);
		if ((T <= 12)) {
			jTime = String.format("午前%02d時", T);
		}
		else {
			jTime = String.format("午後%02d時", T - 12);
		}
		jTime += String.format("%02d分", M);
		return jTime;
	}

	//例外時のスタックトレースをログへ出力
	private void StackTrace(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter  pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		pw.flush();
		String trace = sw.toString();
		log.error(trace);
	}
}
