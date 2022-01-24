/*
 * $Id: b6b728b07f335a3da4d89f88f0d5ed8dde8ed8be $
 *
 * 作成日: 2013/07/25
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 学校教育システム 賢者 [成績管理] 成績判定会議資料(模試版)
 */

public class KNJH441C {

	private static final Log log = LogFactory.getLog(KNJH441C.class);
	private boolean _hasData;
	private Param _param;

	private static final BigDecimal HIST_COMP_MAX = new BigDecimal(75.0);
	private static final BigDecimal HIST_COMP_DEVILINE = new BigDecimal(60.0);
	private static final BigDecimal HIST_COMP_MIN = new BigDecimal(37.5);
	/**
	 * @param request
	 *            リクエスト
	 * @param response
	 *            レスポンス
	 */
	public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		Vrw32alp svf = null;
		DB2UDB db2 = null;
		try {
			response.setContentType("application/pdf");

			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();

			_hasData = false;
			_param = createParam(db2, request);

			svf = new Vrw32alp();
			svf.VrInit();
			svf.VrSetSpoolFileStream(response.getOutputStream());

			if (!(_param._kamoku.isEmpty())) {

				final List<Student> studentList = getStudentList(db2, _param);
				final List<HrClass> hrClassList = HrClass.getHrClassList(studentList);
				printMain(db2, svf, studentList, hrClassList);
			}

		} catch (final Exception e) {
			log.error("Exception:", e);
		} finally {
			if (!_hasData) {
				svf.VrSetForm("MES001.frm", 0);
				svf.VrsOut("note", "note");
				svf.VrEndPage();
			}
			svf.VrQuit();

			if (null != db2) {
				db2.commit();
				db2.close();
			}
		}
	}

	private void printMain(final DB2UDB db2, final Vrw32alp svf, final List<Student> studentList,
			final List<HrClass> hrClassList) {
		int bangou = 1;

		// 度数分布
		if (_param._outputDosubupu.equals("1")) {
			printDosubupu(svf, studentList, hrClassList, bangou);
			bangou++;
		}

		// 成績上位
		if (_param._outputYuryo.equals("1")) {
			printJoui(svf, studentList, bangou);
			bangou++;
		}
		// 成績下位
		if (_param._outputFushin.equals("1")) {
			printKai(svf, studentList, bangou);
			bangou++;
		}
		_hasData = true;
	}

	private void printDosubupu(final Vrw32alp svf, final List<Student> studentList, final List<HrClass> hrClassList,
			final int bangou) {

		final int maxHrPerPage = 10;
		final int maxSubclassLine = 40;
		final List<List<HrClass>> hrPageList = getPageList(hrClassList, maxHrPerPage);
		for (int hrpi = 0; hrpi < hrPageList.size(); hrpi++) {
			final List<HrClass> pageHrClassLit = hrPageList.get(hrpi);
			svf.VrSetForm("KNJH441C_1.frm", 4);
			svf.VrsOut("TOTAL_NAME", "クラス平均"); // 合計名称

			for (int j = 0; j < hrClassList.size(); j++) {
				final HrClass hrClass = hrClassList.get(j);
				final int gyo = j + 1;
				svf.VrsOutn("HR_NAME1", gyo, hrClass._hrNameabbv); // クラス名
			}

			int consume = 0;
			final ScoreDistribution distAll = new ScoreDistribution();
			final BigDecimal[][] table = distAll.getDistributionScoreTable(_param);

			int moshiNameCnt = 0;
			final int moshiNamelen = _param._moshiName.length();

			// 科目ごとのレコード
			for (String key : _param._kamoku.keySet()) {
				svf.VrsOut("GRPCD", _param._mockcd); // グループコード
				final String moshiName = getMoshiName(_param._moshiName, moshiNameCnt, moshiNamelen);
				svf.VrsOut("course1", moshiName);
				moshiNameCnt++;
				String kamokumei = _param._kamoku.get(key);
				if (StringUtils.defaultString(kamokumei).length() <= 12) {
					svf.VrsOut("SUBCLASS", kamokumei); // 科目
				} else {
					svf.VrsOut("SUBCLASS_1", kamokumei); // 科目
					svf.VrsOut("SUBCLASS_2", kamokumei); // 科目
				}

				// クラス別平均
				for (int j = 0; j < pageHrClassLit.size(); j++) {
					final HrClass hrClass = pageHrClassLit.get(j);
					List<BigDecimal> score = new ArrayList<BigDecimal>();
					for (Student stu : hrClass._studentList) {
						if (stu._moshiDeviation.containsKey(key)) {
							score.add(stu._moshiDeviation.get(key));
						}
					}
					final int gyo = j + 1;
					String avg = calcAverage(score);
					svf.VrsOut("SCORE" + gyo, avg); // 成績

					if (avg != null) {
						hrClass._kamokuSum.put(key, avg);
					}
				}

				// 度数分布
				distAll._scoreList.clear();
				distAll.addScoreList(getScoreList(studentList, key));
				int j = 0;
				for (j = 0; j < table.length; j++) {
					if (null != table[j]) {
						final int distidx = j + 1;
						if (j == 0) {
							svf.VrsOut("POINT_AREA" + distidx, String.valueOf(table[j][0])+"以上"); // 点数
						} else {
							svf.VrsOut("POINT_AREA" + distidx, table[j][1] + "～" + table[j][0]); // 点数
						}
						svf.VrsOut("POINT" + distidx, zeroToNull(distAll.getCount(table[j][0], table[j][1]))); // 点数
					}
				}
				svf.VrsOut("POINT_AREA" + j, table[j-1][1] + "未満");
				svf.VrsOut("POINT" + j, zeroToNull(distAll.getCount(table[j-1][1], table[j-1][1]))); // 点数

				String avg = null;
				Integer avgDatCount = null;
				BigDecimal avgDatHighscore = new BigDecimal(0);
				BigDecimal avgDatLowscore = new BigDecimal(0);
				final List scoreBdList = new ArrayList(distAll._scoreList);
				Collections.sort(scoreBdList);

				if (!scoreBdList.isEmpty()) {
					avg = calcAverage(scoreBdList);
					avgDatCount = new Integer(scoreBdList.size());
					avgDatHighscore = ((BigDecimal) scoreBdList.get(scoreBdList.size() - 1)).setScale(1, BigDecimal.ROUND_HALF_UP);
					avgDatLowscore = ((BigDecimal) scoreBdList.get(0)).setScale(1, BigDecimal.ROUND_HALF_UP);
				}
				svf.VrsOut("NUM", null == avgDatCount ? null : avgDatCount.toString()); // 人数
				svf.VrsOut("AVE_CLASS", avg); //科目平均
				svf.VrsOut("DEVI", String.valueOf(distAll.getCount(HIST_COMP_DEVILINE, HIST_COMP_DEVILINE)));  //60点以上の人数
				if (avgDatHighscore.intValue() != Integer.MIN_VALUE) {
					svf.VrsOut("MAX_SCORE", avgDatHighscore.toString()); // 最高点
				}
				if (avgDatLowscore.intValue() != Integer.MAX_VALUE) {
					svf.VrsOut("MIN_SCORE", avgDatLowscore.toString()); // 最低点
				}
				consume += 1;
				svf.VrEndRecord();
			}

			for (int j = 0; j < pageHrClassLit.size(); j++) {
				final HrClass hrClass = pageHrClassLit.get(j);
				double sum = 0;
				for (String key : _param._kamoku.keySet()) {
					if (hrClass._kamokuSum.containsKey(key)) {
						sum += Double.parseDouble(hrClass._kamokuSum.get(key));
					}
				}
				final int gyo = j + 1;
				final String avgavg = getAvgScore(new BigDecimal(sum),new BigDecimal(_param._kamoku.size()));
				svf.VrsOut("TOTAL" + gyo, avgavg); // 平均の平均
			}

			for (int i = (consume > 0 && consume % maxSubclassLine == 0 ? maxSubclassLine
					: consume <= maxSubclassLine ? consume : consume % maxSubclassLine); i < maxSubclassLine; i++) {
				svf.VrEndRecord();
			}
		}
	}

	private void printJoui(final Vrw32alp svf, final List<Student> studentList, final int bangou) {
		Collections.sort(studentList, new jouiComp());
		final int maxLine = 40;
		final List<Student> scoreStudentListAll = take(studentList, _param._yuryo);

		if (scoreStudentListAll.size() > 0) {
			// 一番最後の生徒と同じ成績の生徒は、同一の順位として表示対象とする
			final Student lastStudent = scoreStudentListAll.get(scoreStudentListAll.size() - 1);
			for (final Student s : drop(studentList, _param._yuryo)) {
				if (0 == lastStudent._sum.compareTo(new BigDecimal(0)) || lastStudent._sum != s._sum) {
					break;
				}
				scoreStudentListAll.add(s);
			}
		}

		final List<List<Student>> pageList = getPageList(scoreStudentListAll, maxLine);

		for (int pi = 0; pi < Math.max(1, pageList.size()); pi++) {
			final List<Student> scoreStudentList = pi < pageList.size() ? pageList.get(pi) : Collections.EMPTY_LIST;
			svf.VrSetForm("KNJH441C_2.frm", 4);

			svf.VrsOut("ADD_LEAD", bangou + ".成績上位者(上位者" + String.valueOf(_param._yuryo) + "位まで)");
			int consume = 0;
			for (int cnt = 0; cnt < scoreStudentList.size(); cnt++) {
				final int gyo = cnt + 1;
				final Student student = scoreStudentList.get(cnt);
				svf.VrsOutn("NO", gyo, String.valueOf(pi * maxLine + gyo)); // 番号
				svf.VrsOutn("HR_NO", gyo, student.getHrNameabbvAttendnoCd(_param)); // 年組番号
				svf.VrsOut("name" + gyo, student._name); // 氏名
				svf.VrsOut("TOTAL" + gyo, String.valueOf(student._sum)); // 合計

				if (student._sum.compareTo(new BigDecimal(0)) != 0) {
					final String avg = getAvgScore(student._sum, new BigDecimal(_param._kamoku.size()));
					svf.VrsOut("AVERAGE" + gyo, avg); // 平均
				} else {
					svf.VrsOut("AVERAGE" + gyo, "0"); // 平均
				}
			}

			int moshiNameCnt = 0;
			final int moshiNamelen = _param._moshiName.length();

			for (String key : _param._kamoku.keySet()) {
				svf.VrsOut("GRPCD", _param._mockcd); // グループコード
				final String moshiName = getMoshiName(_param._moshiName, moshiNameCnt, moshiNamelen);
				svf.VrsOut("course1", moshiName);
				moshiNameCnt++;

				String kamokumei = _param._kamoku.get(key);
				if (StringUtils.defaultString(kamokumei).length() <= 12) {
					svf.VrsOut("SUBCLASS", kamokumei); // 科目
				} else {
					svf.VrsOut("SUBCLASS_1", kamokumei); // 科目
					svf.VrsOut("SUBCLASS_2", kamokumei); // 科目
				}

				for (int cnt = 0; cnt < scoreStudentList.size(); cnt++) {

					final Student student = scoreStudentList.get(cnt);

					if (student._moshiDeviation.containsKey(key)) {
						BigDecimal chkWk = student._moshiDeviation.get(key);
						if (chkWk != null) {
						    String score = chkWk.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
						    svf.VrsOut("SCORE" + (cnt + 1), score); // 成績
						}
					}

				}
				svf.VrEndRecord();
				consume += 1;
			}
			for (int i = (consume <= 52 ? consume : consume % 52); i < 52; i++) {
				svf.VrEndRecord();
			}
		}
	}

	private void printKai(final Vrw32alp svf, final List<Student> studentList, final int bangou) {
		Collections.sort(studentList, new kaiComp());
		final int maxSubclassLine = 52;
		final int maxStudentLine = 40;

		final List<Student> scoreStudentListAll = take(studentList, _param._fushin);

		if (scoreStudentListAll.size() > 0) {
			// 一番最後の生徒と同じ成績の生徒は、同一の順位として表示対象とする
			final Student lastStudent = scoreStudentListAll.get(scoreStudentListAll.size() - 1);
			for (final Student s : drop(studentList, _param._fushin)) {
				if (0 == lastStudent._sum.compareTo(new BigDecimal(0)) || lastStudent._sum != s._sum) {
					break;
				}
				scoreStudentListAll.add(s);
			}
		}

		final List<List<Student>> pageList = getPageList(scoreStudentListAll, maxStudentLine);
		for (int pi = 0; pi < Math.max(1, pageList.size()); pi++) {
			svf.VrSetForm("KNJH441C_3.frm", 4);
			final List<Student> scoreStudentList = pi < pageList.size() ? pageList.get(pi) : Collections.EMPTY_LIST;

			svf.VrsOut("ADD_RANK", bangou + ".成績下位者(下位者" + String.valueOf(_param._fushin) + "位まで)");
			int consume = 0;
			int gyo = 1;
			for (int cnt = 0; cnt < scoreStudentList.size(); cnt++) {
				final Student student = scoreStudentList.get(cnt);
				svf.VrsOutn("NO", gyo, String.valueOf(pi * maxStudentLine + gyo)); // 番号
				svf.VrsOutn("HR_NO", gyo, student.getHrNameabbvAttendnoCd(_param)); // 年組番号
				svf.VrsOut("name" + gyo, student._name); // 氏名
				svf.VrsOut("TOTAL" + gyo, String.valueOf(student._sum)); // 合計
				if (student._sum.compareTo(new BigDecimal(0)) != 0) {
					final String avg = getAvgScore(student._sum,new BigDecimal(_param._kamoku.size()));
					svf.VrsOut("AVERAGE" + gyo, avg); // 平均
				} else {
					svf.VrsOut("AVERAGE" + gyo, "0"); // 平均
				}
				gyo++;
			}

			int moshiNameCnt = 0;
			final int moshiNamelen = _param._moshiName.length();

			for (String key : _param._kamoku.keySet()) {
				svf.VrsOut("GRPCD", _param._mockcd); // グループコード
				final String moshiName = getMoshiName(_param._moshiName, moshiNameCnt, moshiNamelen);
				svf.VrsOut("course1", moshiName);
				moshiNameCnt++;

				String kamokumei = _param._kamoku.get(key);
				if (StringUtils.defaultString(kamokumei).length() <= 12) {
					svf.VrsOut("SUBCLASS", kamokumei); // 科目
				} else {
					svf.VrsOut("SUBCLASS_1", kamokumei); // 科目
					svf.VrsOut("SUBCLASS_2", kamokumei); // 科目
				}
				for (int cnt = 0; cnt < scoreStudentList.size(); cnt++) {

					final Student student = scoreStudentList.get(cnt);
					if (student._moshiDeviation.containsKey(key)) {
						BigDecimal chkWk = student._moshiDeviation.get(key);
						if (chkWk != null) {
						    String score = String.valueOf(chkWk.setScale(1, BigDecimal.ROUND_HALF_UP));
						    svf.VrsOut("SCORE" + (cnt + 1), score); // 成績
						}
					}

				}
				consume += 1;
				svf.VrEndRecord();
			}

			for (int i = (consume <= maxSubclassLine ? consume : consume % maxSubclassLine); i < maxSubclassLine; i++) {
				svf.VrEndRecord();
			}
		}
	}

	private static List<Student> getStudentList(final DB2UDB db2, final Param param) {
		final List<Student> studentList = new ArrayList<Student>();
		if ("1".equals(param._outputYuryo) || "1".equals(param._outputFushin)) {
			final StringBuffer stb = new StringBuffer();
			stb.append(" SELECT ");
			stb.append("     T1.SCHREGNO, ");
			stb.append("     T1.GRADE, ");
			stb.append("     T1.HR_CLASS, ");
			stb.append("     T1.ATTENDNO, ");
			stb.append("     T3.HR_NAME, ");
			stb.append("     T3.HR_NAMEABBV, ");
			stb.append("     T2.NAME, ");
			stb.append("     T1.MAJORCD ");
			stb.append(" FROM ");
			stb.append("     SCHREG_REGD_DAT T1 ");
			stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
			stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ");
			stb.append("         ON T3.YEAR = T1.YEAR AND ");
			stb.append("          T3.SEMESTER = T1.SEMESTER AND ");
			stb.append("          T3.GRADE    = T1.GRADE AND ");
			stb.append("          T3.HR_CLASS = T1.HR_CLASS ");
			stb.append(" WHERE ");
			stb.append("     T1.YEAR = '" + param._year + "' AND ");
			stb.append("     T1.SEMESTER = '" + param._semester + "' AND ");
			if (!param._majorcd.equals("000")) {
				stb.append("     T1.MAJORCD = '" + param._majorcd + "' AND ");
			}
			stb.append("     T1.GRADE = '" + param._grade + "' ");
			stb.append(" ORDER BY ");
			stb.append("     GRADE, ");
			stb.append("     HR_CLASS, ");
			stb.append("     ATTENDNO ");

			PreparedStatement ps = null;
			ResultSet rs = null;
			log.info(" sql = " + stb.toString());

			final StringBuffer stb2 = new StringBuffer();

			stb2.append(" SELECT ");
			stb2.append("     T1.SCHREGNO, ");
			stb2.append("     T4.MOCKCD, ");
			stb2.append("     T4.MOCK_SUBCLASS_CD, ");
			stb2.append("     T5.SUBCLASS_ABBV, ");
			stb2.append("     T4.DEVIATION ");
			stb2.append(" FROM ");
			stb2.append("     SCHREG_REGD_DAT T1 ");
			stb2.append("     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
			stb2.append("     LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR AND ");
			stb2.append("          T3.SEMESTER = T1.SEMESTER AND T3.GRADE    = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ");
			stb2.append("     LEFT JOIN MOCK_RANK_RANGE_DAT T4 ON T1.SCHREGNO = T4.SCHREGNO ");
			stb2.append("     LEFT JOIN MOCK_SUBCLASS_MST T5 ON T4.MOCK_SUBCLASS_CD = T5.MOCK_SUBCLASS_CD ");
			stb2.append(" WHERE ");
			stb2.append("     T1.YEAR = '" + param._year + "' AND ");
			stb2.append("     T1.SEMESTER = '" + param._semester + "' AND ");
			if (!param._majorcd.equals("000")) {
				stb2.append("     T1.MAJORCD = '" + param._majorcd + "' AND ");
			}
			stb2.append("     T1.GRADE = '" + param._grade + "' AND ");
			stb2.append("     T4.MOCKCD = '" + param._mockcd + "' AND ");
			stb2.append("     T4.RANK_RANGE = '1' AND ");
			stb2.append("     T4.RANK_DIV = '02' AND ");
			stb2.append("     T4.MOCKDIV = '1' AND ");
			stb2.append("     T4.SCORE IS NOT NULL ");
			stb2.append(" ORDER BY ");
			stb2.append("     SCHREGNO, ");
			stb2.append("     MOCK_SUBCLASS_CD ");

			try {
				ps = db2.prepareStatement(stb.toString());
				rs = ps.executeQuery();
				while (rs.next()) {
					final String schregno = rs.getString("SCHREGNO");
					final String grade = rs.getString("GRADE");
					final String hr_Class = rs.getString("HR_CLASS");
					final String attendno = rs.getString("ATTENDNO");
					final String hr_Name = rs.getString("HR_NAME");
					final String hrNameabbv = rs.getString("HR_NAMEABBV");
					final String name = rs.getString("NAME");
					final String majorcd = rs.getString("MAJORCD");

					final Student student = new Student(schregno, grade, hr_Class, hrNameabbv, attendno, hr_Name, name,
							majorcd);
					studentList.add(student);
				}

				DbUtils.closeQuietly(null, ps, rs);
				ps = null;
				rs = null;

				ps = db2.prepareStatement(stb2.toString());
				rs = ps.executeQuery();

				log.info(" sql = " + stb2.toString());
				while (rs.next()) {
					final String schregno = rs.getString("SCHREGNO");
//					final String mockcd = rs.getString("MOCKCD");
					final String mock_Subclass_Cd = rs.getString("MOCK_SUBCLASS_CD");
//					final String subclass_Abbv = rs.getString("SUBCLASS_ABBV");
					final BigDecimal deviation = rs.getBigDecimal("DEVIATION");

					for (Student stu : studentList) {
						if (stu._schregno.equals(schregno)) {
							stu._moshiDeviation.put(mock_Subclass_Cd, deviation);
							stu._sum = stu._sum.add(deviation);
						}
					}
				}
				log.debug(" studentList = " + studentList);

			} catch (Exception e) {
				log.error("Exception", e);
			} finally {
				DbUtils.closeQuietly(null, ps, rs);
				db2.commit();
			}
		}
		return studentList;
	}

	private static class Student {

		private static DecimalFormat attendnodf = new DecimalFormat("00");

		final String _schregno;
		final String _grade;
		final String _hr_Class;
		final String _attendno;
		final String _hr_Name;
		final String _hrNameabbv;
		final String _name;
		final String _majorcd;
		BigDecimal _sum = new BigDecimal(0);
		double _avg = 0;
		Map<String, BigDecimal> _moshiDeviation;

		public Student(final String schregno, final String grade, final String hr_Class, final String hrNameabbv,
				final String attendno, final String hr_Name, final String name, final String majorcd) {
			_schregno = schregno;
			_grade = grade;
			_hr_Class = hr_Class;
			_attendno = attendno;
			_hr_Name = hr_Name;
			_hrNameabbv = hrNameabbv;
			_name = name;
			_majorcd = majorcd;
			_moshiDeviation = new HashMap<String, BigDecimal>();
		}

		public BigDecimal getSum() {
			return _sum;
		}

		private String getHrNameabbvAttendnoCd(final Param param) {
			return StringUtils.defaultString(_hrNameabbv) + "-" + StringUtils.defaultString(
					(NumberUtils.isDigits(_attendno)) ? attendnodf.format(Integer.parseInt(_attendno)) : _attendno);
		}

		private String getScore(final String mockcd) {
			String score = null;
			if (_moshiDeviation.containsKey(mockcd)) {
				score = _moshiDeviation.get(mockcd).toString();
			}
			return null == score ? null : score;
		}
	}

	private static class HrClass {
		final String _grade;
		final String _hrClass;
		final String _hrNameabbv;
		final List<Student> _studentList;
		Map<String, String> _kamokuSum = new TreeMap();

		HrClass(final String grade, final String hrClass, final String hrNameabbv, final List<Student> studentList) {
			_grade = grade;
			_hrClass = hrClass;
			_hrNameabbv = hrNameabbv;
			_studentList = Collections.unmodifiableList(studentList);
		}

		public static HrClass getHrClass(final String grade, final String hrClass, final List<HrClass> hrClassList) {
			for (final HrClass hrclass : hrClassList) {
				if (hrclass._grade.equals(grade) && hrclass._hrClass.equals(hrClass)) {
					return hrclass;
				}
			}
			return null;
		}

		public static List<HrClass> getHrClassList(final List<Student> studentList) {
			final Map<String, List<Student>> gradeHrclassStudentListMap = new TreeMap<String, List<Student>>();
			for (final Student student : studentList) {
				getMappedList(gradeHrclassStudentListMap, student._grade + student._hr_Class).add(student);
			}

			final List<HrClass> list = new ArrayList<HrClass>();
			for (final List<Student> gradeHrclassStudentList : gradeHrclassStudentListMap.values()) {
				final Student st0 = gradeHrclassStudentList.get(0);
				list.add(new HrClass(st0._grade, st0._hr_Class, st0._hrNameabbv, gradeHrclassStudentList));
			}

			return list;
		}
	}

	private static int toInt(final String s, final int defaultInt) {
		return NumberUtils.isNumber(s) ? Integer.parseInt(s) : defaultInt;
	}

	/** パラメータ取得処理 */
	private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
		log.fatal("$Revision: 75468 $");
		KNJServletUtils.debugParam(request, log);
		final Param param = new Param(db2, request);
		return param;
	}

	/** パラメータクラス */
	private static class Param {
		final String _year;
		final String _semester;
		final String _grade;
		final String _outputDosubupu;
		final String _outputYuryo;
		final String _outputFushin;
		final String _majorcd;
		final String _mockcd;
		final int _fushin;
		final int _yuryo;
		final Map<String, String> _kamoku = new TreeMap();
		final String _moshiName;

		Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
			_year = request.getParameter("CTRL_YEAR");
			_semester = request.getParameter("SEMESTER");
			_grade = request.getParameter("GRADE");
			_majorcd = request.getParameter("MAJOR").substring(1);
			_mockcd = request.getParameter("MOCKCD");
			_outputDosubupu = request.getParameter("OUTPUT_DOSUBUPU") == null ? "0"
					: request.getParameter("OUTPUT_DOSUBUPU");
			_outputYuryo = request.getParameter("OUTPUT_YURYO") == null ? "0" : request.getParameter("OUTPUT_YURYO");
			_outputFushin = request.getParameter("OUTPUT_FUSHIN") == null ? "0" : request.getParameter("OUTPUT_FUSHIN");
			_fushin = toInt(request.getParameter("FUSHIN"), 0);
			_yuryo = toInt(request.getParameter("YURYO"), 0);

			_moshiName = setMoshiName(db2); // 模試名称
			setKamokusuu(db2); // 科目数、科目名称
		}

		private String setMoshiName(final DB2UDB db2) {
			final StringBuffer stb = new StringBuffer();
			stb.append(" SELECT MOCKNAME2 AS MOCKNAME FROM MOCK_MST WHERE MOCKCD = '" + _mockcd + "' ");
			PreparedStatement ps = null;
			ResultSet rs = null;
			String moshiName = "";
			try {
				ps = db2.prepareStatement(stb.toString());
				rs = ps.executeQuery();
				while (rs.next()) {
					moshiName = StringUtils.defaultString(rs.getString("MOCKNAME"));
				}
			} catch (Exception e) {
				log.error("Exception", e);
			} finally {
				DbUtils.closeQuietly(null, ps, rs);
				db2.commit();
			}
			return moshiName;
		}

		private void setKamokusuu(final DB2UDB db2) {
			final StringBuffer stb = new StringBuffer();
			stb.append(" SELECT ");
			stb.append("     MIN(T5.SUBCLASS_ABBV) AS SUBCLASS_ABBV, ");
			stb.append("     T4.MOCK_SUBCLASS_CD ");
			stb.append(" FROM ");
			stb.append("     SCHREG_REGD_DAT T1 ");
			stb.append("     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
			stb.append("     LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR AND ");
			stb.append("          T3.SEMESTER = T1.SEMESTER AND T3.GRADE    = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ");
			stb.append("     LEFT JOIN MOCK_RANK_RANGE_DAT T4 ON T1.SCHREGNO = T4.SCHREGNO ");
			stb.append("     LEFT JOIN MOCK_SUBCLASS_MST T5 ON T4.MOCK_SUBCLASS_CD = T5.MOCK_SUBCLASS_CD ");
			stb.append(" WHERE ");
			stb.append("     T1.YEAR = '" + _year + "' AND ");
			stb.append("     T1.SEMESTER = '" + _semester + "' AND ");
			stb.append("     T1.GRADE = '" + _grade + "' AND ");
			stb.append("     T4.MOCKCD = '" + _mockcd + "' AND ");
			stb.append("     T4.RANK_RANGE = '1' AND ");
			stb.append("     T4.RANK_DIV = '02' AND ");
			stb.append("     T4.MOCKDIV = '1'");
			stb.append(" GROUP BY ");
			stb.append("     T4.MOCK_SUBCLASS_CD ");

			PreparedStatement ps = null;
			ResultSet rs = null;

			log.info(" sql = " + stb.toString());
			try {
				ps = db2.prepareStatement(stb.toString());
				rs = ps.executeQuery();
				while (rs.next()) {
					String kamokuCd = rs.getString("MOCK_SUBCLASS_CD");
					String kamokuName = rs.getString("SUBCLASS_ABBV");
					_kamoku.put(kamokuCd, kamokuName);
				}
			} catch (Exception e) {
				log.error("Exception", e);
			} finally {
				DbUtils.closeQuietly(null, ps, rs);
				db2.commit();
			}

		}
	}

	private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key) {
		if (null == map.get(key)) {
			map.put(key, new ArrayList<B>());
		}
		return map.get(key);
	}

	private static String zeroToNull(final int n) {
		return 0 == n ? null : String.valueOf(n);
	}

	private class jouiComp implements Comparator<Student> {
		public int compare(Student c1, Student c2) {
			if (c1.getSum() == c2.getSum()) {
				return 0;
			} else if (c1.getSum().compareTo(c2.getSum()) < 0) {
				return 1;
			} else
				return -1;
		}
	}

	private class kaiComp implements Comparator<Student> {
		public int compare(Student c1, Student c2) {
			if (0 == c1.getSum().compareTo(new BigDecimal(0)) && 0 == c2.getSum().compareTo(new BigDecimal(0))) {
				return 0;
			} else if (0 == c1.getSum().compareTo(new BigDecimal(0))) {
				return 1;
			} else if (0 == c2.getSum().compareTo(new BigDecimal(0))) {
				return -1;
			} else if (c1.getSum() == c2.getSum()) {
				return 0;
			} else if (c1.getSum().compareTo(c2.getSum()) > 0) {
				return 1;
			} else
				return -1;
		}
	}

	/**
	 * 得点分布
	 */
	private static class ScoreDistribution {
		final List<BigDecimal> _scoreList = new ArrayList<BigDecimal>(); // 母集団の得点

		void addScore(final String score) {
			if (NumberUtils.isNumber(score)) {
				_scoreList.add(new BigDecimal(score));
			}
		}

		void addScoreList(final List<String> scoreList) {
			for (final String score : scoreList) {
				addScore(score);
			}
		}

		int getCount(final BigDecimal lower, final BigDecimal upper) {
			return getList(lower, upper).size();
		}

		List<BigDecimal> getList(final BigDecimal lower, final BigDecimal upper) {
			final List<BigDecimal> rtn = new ArrayList<BigDecimal>();
			for (final BigDecimal i : _scoreList) {
				if (null == i) {
					continue;
				}
				if (lower.compareTo(upper) == 0) {
					if (lower.compareTo(HIST_COMP_MAX) == 0 || lower.compareTo(HIST_COMP_DEVILINE) == 0) {
					    if (lower.compareTo(i) <= 0) {
						    rtn.add(i);
					    }
					} else {
					    if (lower.compareTo(i) > 0) {
						    rtn.add(i);
					    }
					}
				} else {
				    if (lower.compareTo(i) <= 0 && upper.compareTo(i) > 0) {
					    rtn.add(i);
				    }
				}
			}
			return rtn;
		}

		/**
		 * 得点分布のテーブル
		 */
		private BigDecimal[][] getDistributionScoreTable(final Param param) {
			final BigDecimal margin = new BigDecimal(-2.5);
			final BigDecimal[][] table = new BigDecimal[17][];
			for (int i = 0; i < table.length; i++) {
				final BigDecimal conv_i = new BigDecimal(i);
				final BigDecimal lower = HIST_COMP_MAX.add(margin.multiply(conv_i)).setScale(1);
				final BigDecimal uconv_i = new BigDecimal(i-1);
				final BigDecimal upper = uconv_i.compareTo(new BigDecimal(0)) < 0 ? HIST_COMP_MAX : HIST_COMP_MAX.add(margin.multiply(uconv_i)).setScale(1);
				table[i] = new BigDecimal[] { lower, upper };
			}
			return table;
		}
	}

	/**
	 * 生徒のリストから生徒の科目の得点のリストを得る
	 *
	 * @param studentList
	 *            生徒のリスト
	 * @param subclasscd
	 *            科目
	 * @return 生徒のリストから生徒の科目の得点のリストを得る
	 */
	private static List<String> getScoreList(final List<Student> studentList, final String mockcd) {
		final List<String> scoreList = new ArrayList<String>();
		for (final Student student : studentList) {
			final String score = student.getScore(mockcd);
			if (null != score) {
				scoreList.add(score);
			}
		}
		return scoreList;
	}

	private static String calcAverage(final List<BigDecimal> elems) {
		if (0 == elems.size())
			return null;
		BigDecimal sum = sum(elems);
		log.debug(" elems = " + elems);
		return sum.divide(new BigDecimal(elems.size()), 1, BigDecimal.ROUND_HALF_UP).toString();
	}

	private static BigDecimal sum(final List<BigDecimal> elems) {
		if (0 == elems.size())
			return null;
		BigDecimal sum = new BigDecimal(0);
		for (final BigDecimal e : elems) {
			sum = sum.add(e);
		}
		log.debug(" elems = " + elems);
		return sum;
	}

	private static <T> List<List<T>> getPageList(final List<T> list, final int count) {
		final List<List<T>> rtn = new ArrayList<List<T>>();
		List<T> current = null;
		for (final T o : list) {
			if (null == current || current.size() >= count) {
				current = new ArrayList<T>();
				rtn.add(current);
			}
			current.add(o);
		}
		return rtn;
	}

	private static <T> List<T> take(final List<T> list, final int count) {
		return 0 < count && count < list.size() ? new ArrayList<T>(list.subList(0, count)) : new ArrayList<T>(list);
	}

	private static <T> List<T> drop(final List<T> list, final int count) {
		return 0 < count && count < list.size() ? new ArrayList<T>(list.subList(count, list.size()))
				: 0 < count ? new ArrayList<T>() : new ArrayList<T>(list);
	}

	private static String getMoshiName(final String name, final int cnt, final int len) {
		String moshiName = new String();
		if (len - 2 * cnt > 1) {
			moshiName = name.substring(cnt * 2, cnt * 2 + 2);
		} else if (len - 2 * cnt == 1) {
			moshiName = name.substring(cnt * 2, cnt * 2 + 1);
		} else {
			moshiName = "";
		}
		return moshiName;
	}

	private static String getAvgScore(final BigDecimal b1, final BigDecimal b2) {
		final String avg = String.valueOf(b1.divide(b2, 1, BigDecimal.ROUND_HALF_UP));
		return avg;
	}

}

// eof
