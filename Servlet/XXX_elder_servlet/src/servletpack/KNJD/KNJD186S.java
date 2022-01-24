/*
 * $Id: 536706b24b8849e40f597d3fded4ceb44458a78b $
 *
 * 作成日: 2012/02/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理] 中学通知票
 */

public class KNJD186S {

    private static final Log log = LogFactory.getLog(KNJD186S.class);

    private static String SEMEALL = "9";

    private boolean _hasData;

    private Param _param;

    private static final String STATUS = "STATUS";

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List studentList = Student.getStudentList(db2, _param);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug(" student = " + student._schregno + " : " + student._name);
            print(db2, svf, student);
        }
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static String mkString(final List textList, final String comma1) {
        final StringBuffer stb = new StringBuffer();
        String comma = "";
        for (final Iterator it = textList.iterator(); it.hasNext();) {
            final String text = (String) it.next();
            if (StringUtils.isBlank(text)) {
                continue;
            }
            stb.append(comma).append(text);
            comma = comma1;
        }
        return stb.toString();
    }

	private void print(final DB2UDB db2, final Vrw32alp svf, final Student student) {
		final String form;
		if (!SEMEALL.equals(_param._semester)) {
    	    form = "KNJD186S_1.frm";
		} else {
			form = "KNJD186S_2.frm";
		}
		svf.VrSetForm(form, 1);

		printHeader(db2, svf, student);

		if (!SEMEALL.equals(_param._semester)) {
			printTsushinaran(svf, student);
		}
		printShoken(svf, student);

		printAttendance(svf, student);

		printKanten(svf, student);

		svf.VrEndPage();
		_hasData = true;
	}

	private void printHeader(final DB2UDB db2, final Vrw32alp svf, final Student student) {

		svf.VrsOut("SCHOOL_NAME1", _param._certifSchoolRemark8); // 学校名 法人名
		svf.VrsOut("SCHOOL_NAME2", _param._certifSchoolSchoolName); // 学校名

		final int ketaName = KNJ_EditEdit.getMS932ByteLength(student._name);
		svf.VrsOut("NAME" + (ketaName <= 14 ? "1" : ketaName <= 20 ? "2" : "3"), student._name); // 氏名

		svf.VrsOut("SEMESTER", (String) _param._semesternameMap.get(_param._semester)); // 学期
		svf.VrsOut("NENDO", _param._isSeireki ? _param._year + "年度" : KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度"); // 年度
		final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno) + "番"): student._attendno;
		svf.VrsOut("HR_NAME", StringUtils.defaultString(student._hrName) + attendno); // 年組番

		svf.VrsOut("TEACHER1", _param._certifSchoolPrincipalName); // 職員名
		svf.VrsOut("TEACHER2", student._staffname); // 職員名
//		svf.VrsOut("STAFFBTM1", null); //
//		svf.VrsOut("STAFFBTMC1", null); //
//		svf.VrsOut("STAFFBTM2", null); //
//		svf.VrsOut("STAFFBTMC2", null); //

	}

	private void printKanten(final Vrw32alp svf, final Student student) {
		for (int i = 0; i < Math.min(student._viewClassList.size(), 9); i++) {
			final ViewClass vc = (ViewClass) student._viewClassList.get(i);
			final int classline = i + 1;
			if (classline == 5 || classline == 7 || classline == 8) {
				final String[] token = KNJ_EditEdit.get_token(vc._classname, StringUtils.defaultString(vc._classname).length() == 4 ? 4 : 6, 2);
				if (null != token) {
					for (int j = 0; j < token.length; j++) {
						svf.VrsOut("CLASS_NAME" + String.valueOf(classline) + "_" + String.valueOf(j + 1), token[j]); // 教科名
					}
				}
			} else {
    			svf.VrsOut("CLASS_NAME" + String.valueOf(classline), vc._classname); // 教科名
			}
			final int maxLine;
			if (i == 0) {
				maxLine = 5;
			} else {
				maxLine = 4;
			}
    		for (int vi = 0; vi < Math.min(maxLine, vc.getViewSize()); vi++) {
    			final int viewline = vi + 1;
    			final String viewcd = vc.getViewCd(vi);
    			final int ketaViewname = KNJ_EditEdit.getMS932ByteLength(vc.getViewName(vi));
    			svf.VrsOutn("VIEW" + String.valueOf(classline) + "_" + (ketaViewname <= 30 ? "2" : "3"), viewline, vc.getViewName(vi)); // 観点
        		final Map stat = getMappedMap(getMappedMap(vc._viewcdSemesterStatDatMap, viewcd), _param._semester);
    			svf.VrsOutn("VALUE" + String.valueOf(classline), viewline, KnjDbUtils.getString(stat, STATUS)); // 評価
    		}
			svf.VrsOut("DEVIATION" + String.valueOf(classline), (String) vc._semesterScoreMap.get(_param._semester)); // 評定
		}
	}

	private void printAttendance(final Vrw32alp svf, final Student student) {
        if (null != student._attendSemesDat) {
        	final AttendSemesDat att = student._attendSemesDat;
        	svf.VrsOutn("ATTEND", 1, String.valueOf(att._lesson)); // 出欠 授業日数
        	svf.VrsOutn("ATTEND", 2, String.valueOf(att._suspend + att._mourning)); // 出欠 忌引・出席停止日数
        	svf.VrsOutn("ATTEND", 3, String.valueOf(att._mlesson)); // 出欠 出席すべき日数
        	svf.VrsOutn("ATTEND", 4, String.valueOf(att._sick)); // 出欠 欠席日数
        	svf.VrsOutn("ATTEND", 5, String.valueOf(att._present)); // 出欠 出席日数
        	svf.VrsOutn("ATTEND", 6, String.valueOf(att._late)); // 出欠 遅刻回数
        	svf.VrsOutn("ATTEND", 7, String.valueOf(att._early)); // 出欠 早退回数
        }
	}

	private void printTsushinaran(final Vrw32alp svf, final Student student) {
	    final List behavList = new ArrayList(student._behaviorSemesDatMap.values());
	    for (int j = 0; j < Math.min(10, behavList.size()); j++) {
	        final int line = j + 1;
	        final BehaviorSemesDat bsd = (BehaviorSemesDat) behavList.get(j);
	        svf.VrsOutn("BEHAVIOR_NAME", line, bsd._codename); // 行動の記録名称
	        svf.VrsOutn("BEHAVIOR_VALUE", line, (String) bsd._semesterRecordMap.get(_param._semester)); // 行動の記録評価
	    }
		final HReportRemarkDat dat = (HReportRemarkDat) student._hReportRemarkDatMap.get(_param._semester);
	    if (null != dat) {
	    	final List tokenList = KNJ_EditKinsoku.getTokenList(dat._communication, 50);
	    	for (int j = 0; j < Math.min(10, tokenList.size()); j++) {
	    		final int line = j + 1;
	    		svf.VrsOutn("COMMUNICATION", line, (String) tokenList.get(j)); // 通信欄
	    	}
	    }
	}

	private void printShoken(final Vrw32alp svf, final Student student) {
		svf.VrsOut("SPECIAL1", student._committee2); // 特別活動の記録 学級活動
		svf.VrsOut("SPECIAL2", student._committee1); // 特別活動の記録 生徒会活動
		svf.VrsOut("SPECIAL3", student._committee3); // 特別活動の記録 学校行事
		svf.VrsOut("SPECIAL4", student._club); // 特別活動の記録 部活動

		final HReportRemarkDat dat = (HReportRemarkDat) student._hReportRemarkDatMap.get(_param._semester);
		if (null != dat) {
			final List tokenList = KNJ_EditKinsoku.getTokenList(dat._remark1, 40);
			for (int j = 0; j < Math.min(3, tokenList.size()); j++) {
				final int line = j + 1;
				svf.VrsOutn("SPECIAL5", line, (String) tokenList.get(j)); // 特別活動の記録
			}
		}

		final List tokenList = KNJ_EditKinsoku.getTokenList(student._attendSemesRemark, 20);
		for (int j = 0; j < Math.min(4, tokenList.size()); j++) {
			final int line = j + 1;
			svf.VrsOutn("ATTEND_REMARK1", line, (String) tokenList.get(j)); // 出欠備考
		}
	}

//    /**
//     * 『出欠の記録』を印字する
//     * @param svf
//     * @param student
//     */
//    private void printSvfAttendSemesOld(final Vrw32alp svf, final Student student) {
//
//        final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(_param._HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J, 14, 2);
//
//        for (final Iterator it = student._hReportRemarkDatMap.keySet().iterator(); it.hasNext();) {
//            final String semester = (String) it.next();
//            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) student._hReportRemarkDatMap.get(semester);
//            if ("9".equals(semester)) {
//                continue;
//            }
//
//            final List list = KNJ_EditKinsoku.getTokenList(hReportRemarkDat._attendrecRemark, size.getKeta(), size._gyo);
//            if (null != list) {
//                if (list.size() == 1) {
//                    svf.VrsOutn("REMARK2", Integer.parseInt(hReportRemarkDat._semester), (String) list.get(0));
//                } else {
//                    for (int i = 0 ; i < list.size(); i++) {
//                        svf.VrsOutn("REMARK" + String.valueOf(i + 1), Integer.parseInt(hReportRemarkDat._semester), (String) list.get(i));
//                    }
//                }
//            }
//        }
//    }
//

    private static class Student {
		final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        final String _gradeCourse;
        final String _hrClassName1;
        final String _staffname;
        AttendSemesDat _attendSemesDat = null;
        final Map _hReportRemarkDatMap = new HashMap(); // 所見
        final List _viewClassList = new ArrayList();
        final Map _behaviorSemesDatMap = new TreeMap();
		private String _attendSemesRemark = "";
        private String _club;
		private String _committee1;
		private String _committee2;
		private String _committee3;

        public Student(final String schregno, final String name, final String hrName, final String attendno, final String gradeCourse, final String hrClassName1, final String staffname) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
            _gradeCourse = gradeCourse;
            _hrClassName1 = hrClassName1;
            _staffname = staffname;
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            final String sql = getStudentSql(param);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map rs = (Map) it.next();
                final String schregno = KnjDbUtils.getString(rs, "SCHREGNO");
                final String name = "1".equals(KnjDbUtils.getString(rs, "USE_REAL_NAME")) ? KnjDbUtils.getString(rs, "REAL_NAME") : KnjDbUtils.getString(rs, "NAME");
                final String hrName = KnjDbUtils.getString(rs, "HR_NAME");
                final String attendno = KnjDbUtils.getString(rs, "ATTENDNO");
                final String gradeCourse = KnjDbUtils.getString(rs, "COURSE");
                final String hrClassName1 = KnjDbUtils.getString(rs, "HR_CLASS_NAME1");
                final String staffname = KnjDbUtils.getString(rs, "STAFFNAME");
                final Student student = new Student(schregno, name, hrName, attendno, gradeCourse, hrClassName1, staffname);
                studentList.add(student);
            }

            AttendSemesDat.setAttendSemesDatList(db2, param, studentList);
            ViewClass.setViewClassList(db2, param, studentList);
            HReportRemarkDat.setHReportRemarkDatMap(db2, param, studentList);
            BehaviorSemesDat.setBehaviorSemesDatMap(db2, param, studentList);
            setClubCommittee(db2, param, studentList);

            return studentList;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO_A AS(");
            stb.append("  SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.SEMESTER ");
            stb.append("  FROM    SCHREG_REGD_DAT T1 ");
            stb.append("          , SEMESTER_MST T2 ");
            stb.append("  WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("     AND T1.YEAR = T2.YEAR ");
            stb.append("     AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
//            //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
//            //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
//            stb.append("     AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
//            stb.append("                     WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//            stb.append("                         AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
//            stb.append("                           OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
//            //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
//            stb.append("     AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
//            stb.append("                     WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//            stb.append("                         AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(" ) ");
            //メイン表
            stb.append(" SELECT ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T7.HR_NAME, ");
            stb.append("    T1.ATTENDNO, ");
            stb.append("    T1.COURSE, ");
            stb.append("    T5.NAME, ");
            stb.append("    T5.REAL_NAME, ");
            stb.append("    T7.HR_CLASS_NAME1, ");
            stb.append("    CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append("    STF.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("    SCHNO_A T1 ");
            stb.append("    INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = '" + param._year + "' AND T7.SEMESTER = T1.SEMESTER AND T7.GRADE = T1.GRADE AND T7.HR_CLASS = T1.HR_CLASS ");
            stb.append("    LEFT JOIN STAFF_MST STF ON STF.STAFFCD = T7.TR_CD1 ");
            stb.append(" ORDER BY ATTENDNO");
            return stb.toString();
        }

        private static void setClubCommittee(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            final StringBuffer stb = new StringBuffer();
            stb.append("  ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CLUBCD, ");
            stb.append("     T2.CLUBNAME, ");
            stb.append("     CASE WHEN T1.SDATE BETWEEN SEM1.SDATE AND TSEM.EDATE OR ");
            stb.append("               T1.EDATE BETWEEN SEM1.SDATE AND TSEM.EDATE OR ");
            stb.append("               T1.SDATE < SEM1.SDATE AND TSEM.EDATE < VALUE(T1.EDATE, '9999-12-31') THEN 1 END AS FLG ");
            stb.append(" FROM SCHREG_CLUB_HIST_DAT T1 ");
            stb.append(" INNER JOIN CLUB_MST T2 ON T2.CLUBCD = T1.CLUBCD ");
            stb.append(" INNER JOIN SEMESTER_MST SEM1 ON SEM1.YEAR = '" + param._year + "' ");
            stb.append("     AND SEM1.SEMESTER = '1' ");
            stb.append(" INNER JOIN SEMESTER_MST TSEM ON TSEM.YEAR = '" + param._year + "' ");
            stb.append("     AND TSEM.SEMESTER = '" + param._semester + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            stb.append("     AND T2.SCHOOL_KIND = 'J' ");
            stb.append("     AND (T1.EDATE IS NULL OR T1.EDATE >= '" + param._date + "') ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CLUBCD ");
            try {
            	final String sql = stb.toString();
            	if (param._isOutputDebug) {
            		log.info(" club sql = " + stb.toString());
            	}

                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final List list = new ArrayList();
                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                        final Map rs = (Map) rit.next();
                        final String clubname = KnjDbUtils.getString(rs, "CLUBNAME");
                        final String flg = KnjDbUtils.getString(rs, "FLG");

                        if (!"1".equals(flg) || StringUtils.isBlank(clubname) || list.contains(clubname)) {
                            continue;
                        }
                        list.add(clubname);
                    }
                    student._club = mkString(list, "　");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

            final StringBuffer stb2 = new StringBuffer();
            stb2.append(" SELECT ");
            stb2.append("     T1.SEMESTER, ");
            stb2.append("     T1.SCHREGNO, ");
            stb2.append("     T1.COMMITTEE_FLG, ");
            stb2.append("     T1.COMMITTEECD, ");
            stb2.append("     T1.CHARGENAME, ");
            stb2.append("     T2.COMMITTEENAME, ");
            stb2.append("     J002.NAME1 AS EXECUTIVENAME ");
            stb2.append(" FROM SCHREG_COMMITTEE_HIST_DAT T1 ");
            stb2.append(" LEFT JOIN COMMITTEE_MST T2 ON T2.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
            stb2.append("     AND T2.COMMITTEECD = T1.COMMITTEECD ");
            stb2.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb2.append(" LEFT JOIN V_NAME_MST J002 ");
            stb2.append("      ON J002.YEAR    = T1.YEAR ");
            stb2.append("     AND J002.NAMECD1 = 'J002' ");
            stb2.append("     AND J002.NAMECD2 = T1.EXECUTIVECD ");
            stb2.append(" WHERE ");
            stb2.append("     T1.YEAR = '" + param._year + "' ");
            stb2.append("     AND T1.SEMESTER <> '9' ");
            stb2.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb2.append("     AND T1.COMMITTEE_FLG IN ('1', '2', '3') ");
            stb2.append("     AND T1.SCHREGNO = ? ");
            stb2.append(" ORDER BY ");
            stb2.append("     T1.SEMESTER, ");
            stb2.append("     T1.COMMITTEE_FLG, ");
            stb2.append("     T1.COMMITTEECD ");
            try {

            	final String sql = stb2.toString();
            	if (param._isOutputDebug) {
            		log.info(" comm sql = " + stb2.toString());
            	}

                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final List list1 = new ArrayList();
                    final List list2 = new ArrayList();
                    final List list3 = new ArrayList();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                        final Map rs = (Map) rit.next();
                    	//final String semester = KnjDbUtils.getString(rs, "SEMESTER");
                        final String committeeFlg = KnjDbUtils.getString(rs, "COMMITTEE_FLG");
                        List list;
                        String name;
                        if ("2".equals(committeeFlg)) {
                            name = KnjDbUtils.getString(rs, "COMMITTEENAME");
                            list = list2;
                        } else if ("3".equals(committeeFlg)) {
                        	name = KnjDbUtils.getString(rs, "COMMITTEENAME");
                            list = list3;
                        } else if ("1".equals(committeeFlg)) {
                            name = KnjDbUtils.getString(rs, "COMMITTEENAME");
                            String executiveName = StringUtils.defaultString(KnjDbUtils.getString(rs, "EXECUTIVENAME"));
                            if(!"".equals(executiveName)) name =  name + " " + executiveName;
                            list = list1;
                        } else {
                        	continue;
                        }
                        if (StringUtils.isBlank(name) || list.contains(name)) {
                            continue;
                        }
                        list.add(name);
                    }
                    student._committee1 = mkString(list1, "　");
                    student._committee2 = mkString(list2, "　");
                    student._committee3 = mkString(list3, "　");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {

        final String _semester;
        final int _lesson;
        final int _suspend;
        final int _mourning;
        final int _mlesson;
        final int _sick;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _transferDate;
        final int _offdays;

        public AttendSemesDat(
                final String semester,
                final int lesson,
                final int suspend,
                final int mourning,
                final int mlesson,
                final int sick,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int transferDate,
                final int offdays
        ) {
            _semester = semester;
            _lesson = lesson;
            _suspend = suspend;
            _mourning = mourning;
            _mlesson = mlesson;
            _sick = sick;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _transferDate = transferDate;
            _offdays = offdays;
        }

        private static void setAttendSemesDatList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            try {
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        param._sdate,
                        param._date,
                        param._attendParamMap
                );
                ps = db2.prepareStatement(sql);

                final Integer zero = new Integer(0);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                    	final Map rs = (Map) rit.next();

                        final String semester = KnjDbUtils.getString(rs, "SEMESTER");
                        if (!"9".equals(semester)) {
                        	continue;
                        }
                        final int lesson = KnjDbUtils.getInt(rs, "LESSON", zero).intValue();
                        final int suspend = KnjDbUtils.getInt(rs, "SUSPEND", zero).intValue() + KnjDbUtils.getInt(rs, "VIRUS", zero).intValue() + KnjDbUtils.getInt(rs, "KOUDOME", zero).intValue();
                        final int mourning = KnjDbUtils.getInt(rs, "MOURNING", zero).intValue();
                        final int mlesson = KnjDbUtils.getInt(rs, "MLESSON", zero).intValue();
                        final int sick = KnjDbUtils.getInt(rs, "SICK", zero).intValue();
                        final int absent = KnjDbUtils.getInt(rs, "ABSENT", zero).intValue();
                        final int present = KnjDbUtils.getInt(rs, "PRESENT", zero).intValue();
                        final int late = KnjDbUtils.getInt(rs, "LATE", zero).intValue();
                        final int early = KnjDbUtils.getInt(rs, "EARLY", zero).intValue();
                        final int transferDate = KnjDbUtils.getInt(rs, "TRANSFER_DATE", zero).intValue();
                        final int offdays = KnjDbUtils.getInt(rs, "OFFDAYS", zero).intValue();

                        final AttendSemesDat attendSemesDat = new AttendSemesDat(semester, lesson, suspend, mourning, mlesson, sick, absent, present, late, early, transferDate, offdays);
                        student._attendSemesDat = attendSemesDat;
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }

            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("     T1.ATTEND_REMARK ");
                sql.append(" FROM ");
                sql.append("     ATTEND_REASON_COLLECTION_DAT T1 ");
                sql.append("     INNER JOIN ATTEND_REASON_COLLECTION_MST T2 ");
                sql.append("         ON T1.YEAR = T2.YEAR ");
                sql.append("         AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                sql.append("         AND T1.COLLECTION_CD = T2.COLLECTION_CD ");
                sql.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ");
                sql.append("       ON GDAT.YEAR = T1.YEAR ");
                sql.append("      AND GDAT.SCHOOL_KIND = T1.SCHOOL_KIND ");
                sql.append(" WHERE ");
                sql.append("     T1.YEAR = '" + param._year + "' ");
                sql.append("     AND GDAT.GRADE = '" + param._grade + "' ");
                sql.append("     AND T1.COLLECTION_CD = '" + param._bikoTermType + "' ");
                sql.append("     AND T1.SCHREGNO = ? ");
                
                ps = db2.prepareStatement(sql.toString());
                
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    
                    student._attendSemesRemark = "";
                    
                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                        final Map row = (Map) rit.next();
                        final String remark1 = KnjDbUtils.getString(row, "ATTEND_REMARK");
                        if (StringUtils.isBlank(remark1)) {
                            continue;
                        }
                        student._attendSemesRemark = remark1;
                    }
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }

        public String toString() {
            return "AttendSemesDat(" + _semester + ": [" + _lesson + ", " + _suspend  + ", " + _mourning  + ", " + _mlesson  + ", " + _sick  + ", " + _present + ", " + _late + ", " + _early + "])";
        }
    }

    /**
     * 通知表所見
     */
    private static class HReportRemarkDat {
        final String _semester;
        final String _totalstudytime;     // 総合的な学習の時間
        final String _specialactremark;   // 特別活動の記録・その他
        final String _communication;      // 担任からの所見
        final String _remark1;            // 特別活動の記録・学級活動
        final String _remark2;            // 特別活動の記録・生徒会活動
        final String _remark3;            // 特別活動の記録・学校行事
        final String _attendrecRemark;    // 出欠備考

        public HReportRemarkDat(
                final String semester,
                final String totalstudytime,
                final String specialactremark,
                final String communication,
                final String remark1,
                final String remark2,
                final String remark3,
                final String attendrecRemark) {
            _semester = semester;
            _totalstudytime = totalstudytime;
            _specialactremark = specialactremark;
            _communication = communication;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _attendrecRemark = attendrecRemark;
        }

        public static void setHReportRemarkDatMap(final DB2UDB db2, final Param param, final List studentList) {
            final String sql = getHReportRemarkDatSql(param);

            PreparedStatement ps = null;

            try {
            	ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                	final Student student = (Student) it.next();

                	student._hReportRemarkDatMap.clear();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                    	final Map rs = (Map) rit.next();
                        final String semester = KnjDbUtils.getString(rs, "SEMESTER");
                        final String totalstudytime = KnjDbUtils.getString(rs, "TOTALSTUDYTIME");
                        final String specialactremark = KnjDbUtils.getString(rs, "SPECIALACTREMARK");
                        final String communication = KnjDbUtils.getString(rs, "COMMUNICATION");
                        final String remark1 = KnjDbUtils.getString(rs, "REMARK1");
                        final String remark2 = KnjDbUtils.getString(rs, "REMARK2");
                        final String remark3 = KnjDbUtils.getString(rs, "REMARK3");
                        final String attendrecRemark = KnjDbUtils.getString(rs, "ATTENDREC_REMARK");
                        final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(semester, totalstudytime, specialactremark, communication, remark1, remark2, remark3, attendrecRemark);
                        student._hReportRemarkDatMap.put(semester, hReportRemarkDat);
                    }
                }
            } catch (Exception e) {
            	log.error("exception!", e);
            } finally {
            	DbUtils.closeQuietly(ps);
            }
        }

        private static String getHReportRemarkDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND (T1.SEMESTER <= '" + param._semester + "' OR T1.SEMESTER = '9') ");
            stb.append("     AND T1.SCHREGNO = ? ");
            return stb.toString();
        }

        public String toString() {
            return "HReportRemarkDat(" + _semester + ": totalstudytime=" + _totalstudytime + ", specialactremark=" + _specialactremark + ", communication=" + _communication + ", remark1=" + _remark1 + ", remark2=" + _remark2 + ", remark3=" + _remark3 + ", attendrecRemark= " + _attendrecRemark + ")";
        }
    }

    private static class BehaviorSemesDat {
        final String _code;
        final String _codename;
        final String _viewname;
        final Map _semesterRecordMap = new HashMap();

        public BehaviorSemesDat(
            final String code,
            final String codename,
            final String viewname
        ) {
            _code = code;
            _codename = codename;
            _viewname = viewname;
        }

        public static void setBehaviorSemesDatMap(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            try {
            	final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.CODE, ");
                stb.append("     T1.CODENAME, ");
                stb.append("     T1.VIEWNAME, ");
                stb.append("     L1.SCHREGNO, ");
                stb.append("     L1.SEMESTER, ");
                stb.append("     L1.RECORD, ");
                stb.append("     L2.ABBV1 ");
                stb.append(" FROM BEHAVIOR_SEMES_MST T1 ");
                stb.append(" LEFT JOIN BEHAVIOR_SEMES_DAT L1 ON ");
                stb.append("    L1.YEAR = T1.YEAR ");
                stb.append("    AND L1.SEMESTER <= '" + param._semester + "' ");
                stb.append("    AND L1.SCHREGNO = ? ");
                stb.append("    AND L1.CODE = T1.CODE ");
                stb.append(" LEFT JOIN NAME_MST L2 ON ");
                stb.append("    L2.NAMECD1 = 'D036' ");
                stb.append("    AND L2.NAMECD2 = L1.RECORD ");
                stb.append(" WHERE ");
                stb.append("    T1.YEAR = '" + param._year + "' ");
                stb.append("    AND T1.GRADE = '" + param._grade + "' ");
                stb.append(" ORDER BY ");
                stb.append("     T1.CODE ");
                stb.append("   , L1.SEMESTER ");

                final String sql = stb.toString();
                if (param._isOutputDebug) {
                	log.info(" behavior sql = " + sql);
                }
                ps = db2.prepareStatement(sql);

            	for (final Iterator it = studentList.iterator(); it.hasNext();) {
            		final Student student = (Student) it.next();

            		student._behaviorSemesDatMap.clear();

            		for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
            			final Map row = (Map) rit.next();
            			final String code = KnjDbUtils.getString(row, "CODE");
            			if (null == student._behaviorSemesDatMap.get(code)) {
            				final String codename = KnjDbUtils.getString(row, "CODENAME");
            				final String viewname = KnjDbUtils.getString(row, "VIEWNAME");
            				final BehaviorSemesDat bsd = new BehaviorSemesDat(code, codename, viewname);
            				student._behaviorSemesDatMap.put(code, bsd);
            			}
        				final String semester = KnjDbUtils.getString(row, "SEMESTER");
        				if (null != semester) {
//        				    final String record = KnjDbUtils.getString(row, "RECORD");
        					final BehaviorSemesDat bsd = (BehaviorSemesDat) student._behaviorSemesDatMap.get(code);
        					bsd._semesterRecordMap.put(semester, KnjDbUtils.getString(row, "ABBV1"));
        				}
            		}
            	}
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
            	DbUtils.closeQuietly(ps);
            }
        }

        public String toString() {
        	return "BehaviorSemesDat(" + _code + ", " + _viewname + ", " + _semesterRecordMap + ")";
        }
    }

    /**
     * 観点の教科
     */
    private static class ViewClass {
        final String _classcd;
        final String _subclasscd;
        final String _electDiv;
        final String _classname;
        final String _subclassname;
        final List _viewList;
        final Map _semesterScoreMap = new HashMap();
        final Map _viewcdSemesterStatDatMap = new HashMap();

        ViewClass(
                final String classcd,
                final String subclasscd,
                final String electDiv,
                final String classname,
                final String subclassname) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _electDiv = electDiv;
            _classname = classname;
            _subclassname = subclassname;
            _viewList = new ArrayList();
        }

        public void addView(final String viewcd, final String viewname) {
            _viewList.add(new String[]{viewcd, viewname});
        }

        public String getViewCd(final int i) {
            return ((String[]) _viewList.get(i))[0];
        }

        public String getViewName(final int i) {
            return ((String[]) _viewList.get(i))[1];
        }

        public int getViewSize() {
            return _viewList.size();
        }

        public String toString() {
        	return "ViewClass(" + _subclasscd + ", " + _classname + ")";
        }

        public static void setViewClassList(final DB2UDB db2, final Param param, final List studentList) {
            final String sql = getViewClassSql(param);
            if (param._isOutputDebug) {
            	log.info(" view class sql = " + sql);
            }

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                	final Student student = (Student) it.next();

                	student._viewClassList.clear();

                	for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno, student._schregno}).iterator(); rit.hasNext();) {
                		final Map row = (Map) rit.next();

                		final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                		final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                		final String viewcd = KnjDbUtils.getString(row, "VIEWCD");

                		ViewClass viewClass = null;
                		for (final Iterator vit = student._viewClassList.iterator(); vit.hasNext();) {
                			final ViewClass viewClass0 = (ViewClass) vit.next();
                			if (viewClass0._subclasscd.equals(subclasscd)) {
                				viewClass = viewClass0;
                				break;
                			}
                		}

                		if (null == viewClass) {
                			final String electDiv = KnjDbUtils.getString(row, "ELECTDIV");
                			final String classname = KnjDbUtils.getString(row, "CLASSNAME");
                			final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");

                			viewClass = new ViewClass(classcd, subclasscd, electDiv, classname, subclassname);
                			student._viewClassList.add(viewClass);
                		}

                		final String viewname = KnjDbUtils.getString(row, "VIEWNAME");
                		viewClass.addView(viewcd, viewname);

                		final String semester = param._semester;
                		if (null == semester) {
                			continue;
                		}
                		viewClass._semesterScoreMap.put(semester, KnjDbUtils.getString(row, "SCORE"));

                		final Map stat = getMappedMap(getMappedMap(viewClass._viewcdSemesterStatDatMap, viewcd), semester);
                		stat.put(STATUS, KnjDbUtils.getString(row, STATUS));
                	}
                }

            } catch (Exception e) {
            	log.error("exception!", e);
            } finally {
            	DbUtils.closeQuietly(ps);
            }
        }

        private static String getViewClassSql(final Param param) {

            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.GRADE ");
            stb.append("   , CLM.CLASSCD ");
            stb.append("   , VALUE(CLM.CLASSORDERNAME2, CLM.CLASSNAME) AS CLASSNAME ");
            stb.append("   , VALUE(SCLM.SUBCLASSORDERNAME2, SCLM.SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.append("   , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("   , VALUE(SCLM.ELECTDIV, '0') AS ELECTDIV ");
            stb.append("   , T1.VIEWCD ");
            stb.append("   , T1.VIEWNAME ");
            stb.append("   , REC.SCHREGNO ");
            stb.append("   , REC.STATUS ");
            stb.append("   , NM_D029.NAME1 AS STATUS_NAME1 ");
            stb.append("   , T10.SCORE ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("     INNER JOIN CLASS_MST CLM ON CLM.CLASSCD = T1.CLASSCD ");
            stb.append("         AND CLM.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("     LEFT JOIN SUBCLASS_MST SCLM ON SCLM.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND SCLM.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND SCLM.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND SCLM.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT REC ON REC.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("         AND REC.CLASSCD = T2.CLASSCD  ");
            stb.append("         AND REC.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            stb.append("         AND REC.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            stb.append("         AND REC.VIEWCD = T2.VIEWCD ");
            stb.append("         AND REC.YEAR = T2.YEAR ");
            stb.append("         AND REC.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND REC.SCHREGNO = ? ");
            stb.append("     LEFT JOIN NAME_MST NM_D029 ON NM_D029.NAMECD1 = 'D029' ");
            stb.append("         AND NM_D029.ABBV1 = REC.STATUS ");
            stb.append("     LEFT JOIN RECORD_SCORE_DAT T10 ON T10.YEAR = T2.YEAR ");
            stb.append("         AND T10.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND T10.TESTKINDCD = '99' ");
            stb.append("         AND T10.TESTITEMCD = '00' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("         AND T10.SCORE_DIV = '09' ");
            } else {
                stb.append("         AND T10.SCORE_DIV = '08' ");
            }
            stb.append("         AND T10.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T10.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T10.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T10.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T10.SCHREGNO = ? ");
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.SCHOOL_KIND = 'J' ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(SCLM.ELECTDIV, '0'), ");
            stb.append("     VALUE(CLM.SHOWORDER3, -1), ");
            stb.append("     CLM.CLASSCD, ");
            stb.append("     VALUE(SCLM.SHOWORDER3, -1), ");
            stb.append("     SCLM.CLASSCD, ");
            stb.append("     SCLM.SCHOOL_KIND, ");
            stb.append("     SCLM.CURRICULUM_CD, ");
            stb.append("     SCLM.SUBCLASSCD, ");
            stb.append("     VALUE(T1.SHOWORDER, -1), ");
            stb.append("     T1.VIEWCD ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75818 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _sdate;
        final String _date;
        final String _gradeHrclass;
        final String _grade;
        final String[] _categorySelected;
        final String _documentRoot;
        final String _imagePath;
        final String _extension;
        final String _bikoTermType;

        final String _certifSchoolSchoolName;
        final String _certifSchoolRemark8;
        final String _certifSchoolPrincipalName;
        final String _certifSchoolJobName;

        final String _HREPORTREMARK_DAT_REMARK1_SIZE_J;
        final String _HREPORTREMARK_DAT_REMARK2_SIZE_J;
        final String _HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE_J;
        final String _HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J;

        /** 各学校における定数等設定 */
        private KNJSchoolMst _knjSchoolMst;

        private String _gradeCdStr;

        final Map _attendParamMap;
        final Map _semesternameMap;
        final boolean _isOutputDebug;
        final boolean _isSeireki;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _sdate = request.getParameter("SDATE").replace('/', '-');
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            _categorySelected = request.getParameterValues("category_selected");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _bikoTermType = request.getParameter("BIKO_TERM_TYPE");

            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
            _imagePath = null == returnval ? null : returnval.val4;
            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子

            setGrade(db2, _grade);
            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolRemark8 = getCertifSchoolDat(db2, "REMARK8");
            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _certifSchoolJobName = getCertifSchoolDat(db2, "JOB_NAME");

            _HREPORTREMARK_DAT_REMARK1_SIZE_J = request.getParameter("_HREPORTREMARK_DAT_REMARK1_SIZE_J");
            _HREPORTREMARK_DAT_REMARK2_SIZE_J = request.getParameter("_HREPORTREMARK_DAT_REMARK2_SIZE_J");
            _HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J = request.getParameter("HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J");
            _HREPORTREMARK_DAT_COMMUNICATION_SIZE_J = request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_J");
            _HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J = request.getParameter("HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            _isOutputDebug = "1".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD186S' AND NAME = 'outputDebug' ")));
            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '01' ")));
            _semesternameMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT SEMESTER, SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' "), "SEMESTER", "SEMESTERNAME");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
        }

        private String getHrClassName1(final DB2UDB db2, final String year, final String semester, final String gradeHrclass) {
            String rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT HR_CLASS_NAME1 FROM SCHREG_REGD_HDAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' "));
            if (null == rtn) {
            	String hrClass = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT HR_CLASS FROM SCHREG_REGD_DAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' "));
                if (null != hrClass) {
                    rtn = NumberUtils.isDigits(hrClass) ? String.valueOf(Integer.parseInt(hrClass)) : hrClass;
                }
            }
            if (null == rtn) {
                rtn = "";
            }
            return rtn;
        }

        /**
         * 写真データ格納フォルダの取得 --NO001
         */
        private KNJ_Control.ReturnVal getDocumentroot(final DB2UDB db2) {
            KNJ_Control.ReturnVal returnval = null;
            try {
                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
                returnval = imagepath_extension.Control(db2);
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            return returnval;
        }

        private String getNameMst(final DB2UDB db2, final String year, final String namecd1, final String namecd2, final String field) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("    " + field + " ");
            sql.append(" FROM NAME_MST T1 ");
            sql.append("    INNER JOIN NAME_YDAT T2 ON T2.YEAR = '" + year + "' AND T2.NAMECD1 = T1.NAMECD1 AND T2.NAMECD2 = T1.NAMECD2 ");
            sql.append(" WHERE ");
            sql.append("    T1.NAMECD1 = '" + namecd1 + "' ");
            sql.append("    AND T1.NAMECD2 = '" + namecd2 + "' ");
            final String rtn = StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString())));
            return rtn;
        }

        public String getImagePath(final String filename) {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + filename + "." + _extension;
            if (new File(path).exists()) {
                return path;
            }
            return null;
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            final String sql = " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '103' ";
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql)));
        }

        private void setGrade(final DB2UDB db2, final String grade) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GRADE_CD ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHOOL_KIND = 'J' ");
            stb.append("     AND T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            final String tmp = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
            if (NumberUtils.isDigits(tmp)) {
            	_gradeCdStr = String.valueOf(Integer.parseInt(tmp));
            }
        }
    }
}

// eof

