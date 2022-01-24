/*
 * $Id: afd5102c2f3531d18db0c91a7b25e1596de31544 $
 *
 * 作成日: 2020/07/15
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理] 中学通知票
 */

public class KNJD184O {

    private static final Log log = LogFactory.getLog(KNJD184O.class);
    private static final String SEME1 = "1";
    private static final String SEME2 = "2";
    private static final String SEME3 = "3";
    private static final String SEMEALL = "9";

    private boolean _hasData;

    private Param _param;

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

            log.info(" student = " + student._schregno);

            printSeiseki(db2, svf, student);
        }
    }

    private void setTitleAttendShoken(final DB2UDB db2, final Vrw32alp svf, final Student student) {
    	final String form = "H".equals(_param._schoolkind) ? "KNJD184O_2.frm" : "KNJD184O_1.frm";
        svf.VrSetForm(form, 1);

        svf.VrsOut("NENDO", _param._year + "年度");  //ログイン年度
        svf.VrsOut("TITLE", "学習成績通知表");
        svf.VrsOut("HR_NAME", _param._gradeName + Integer.parseInt(_param._hrClass) + "組" + Integer.parseInt(student._attendno) + "番");
        svf.VrsOut("NAME", student._name);
        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate) + "発行");

        final int pnlen = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName);
        final String pnField = pnlen > 20 ? "2" : "1";
        svf.VrsOut("PRINCIPAL_NAME" + pnField, _param._certifSchoolPrincipalName);

        final int trlen = KNJ_EditEdit.getMS932ByteLength(student._staffname);
        final String trField = trlen > 20 ? "2" : "1";
        svf.VrsOut("TR_NAME" + trField, student._staffname);

        printShoken(svf, student);
        printAttendance(svf, student);
    }

    private void printSeiseki(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        //final int maxClass = 10;
    	final int lineMax = "H".equals(_param._schoolkind) ? 26 : 30;
    	final Map C010101Map = (Map)student._ValuationInfoMap.get("010101");  //1学期中間考査素点
    	final Map C010201Map = (Map)student._ValuationInfoMap.get("010201");  //1学期末考査素点
    	final Map C020101Map = (Map)student._ValuationInfoMap.get("020101");  //2学期中間考査素点
    	final Map C020201Map = (Map)student._ValuationInfoMap.get("020201");  //2学期末考査素点
    	final Map C030201Map = (Map)student._ValuationInfoMap.get("030201");  //3学期末考査素点
    	final Map C990008Map = (Map)student._ValuationInfoMap.get("990008");  //評価
    	final Map C990009Map = (Map)student._ValuationInfoMap.get("990009");  //評定

    	setTitleAttendShoken(db2, svf, student);
    	int cli = 0;
        for (Iterator ite = student._viewRecordMap.keySet().iterator();ite.hasNext();) {
        	final String kStr = (String)ite.next();
            final int classline = (cli % lineMax) + 1;
            final ChairRecord CRObj = (ChairRecord) student._viewRecordMap.get(kStr);

            if (cli > 0 && cli % lineMax == 0) {
            	svf.VrEndPage();
            	setTitleAttendShoken(db2, svf, student);
            }
            final int cnLen = KNJ_EditEdit.getMS932ByteLength(CRObj._classabbv);
            final String cnField = cnLen > 6 ? "2" : "1";
            svf.VrsOutn("CLASS_NAME" + cnField, classline, CRObj._classabbv);  //教科名

            final int cLen = KNJ_EditEdit.getMS932ByteLength(CRObj._chairname);
            final String cField;
            if ("H".equals(_param._schoolkind)) {
            	cField = cLen > 22 ? "2" : "1";
            } else {
            	cField = cLen > 26 ? "2" : "1";
            }
            svf.VrsOutn("CHAIR_NAME" + cField, classline, CRObj._chairname);  //講座名

            svf.VrsOutn("CREDIT", classline, CRObj._credits);  //単位

            final int sLen = KNJ_EditEdit.getMS932ByteLength(CRObj._staffnames);
            final String sField = sLen > 30 ? "4" : sLen > 20 ? "3" : sLen > 16 ? "2" :"1";
            svf.VrsOutn("CHAIR_STF_NAME" + sField, classline, CRObj._staffnames);

            printScore(svf, C010101Map, CRObj, "1", "SCORE1_1", classline, "SOTEN");  //1学期中間考査
            printScore(svf, C010201Map, CRObj, "1", "SCORE1_2", classline, "SOTEN");  //1学期末考査
            printScore(svf, C990008Map, CRObj, "1", "SCORE1_3", classline, "HYOUKA"); //1学期末評価

            printScore(svf, C020101Map, CRObj, "2", "SCORE2_1", classline, "SOTEN");  //2学期中間考査
            printScore(svf, C020201Map, CRObj, "2", "SCORE2_2", classline, "SOTEN");  //2学期末考査
            printScore(svf, C990008Map, CRObj, "2", "SCORE2_3", classline, "HYOUKA"); //2学期末評価

            printScore(svf, C030201Map, CRObj, "3", "SCORE3_1", classline, "SOTEN");  //3学期末考査
            printScore(svf, C990008Map, CRObj, "3", "SCORE3_2", classline, "HYOUKA"); //3学期末評価

            printScore(svf, C990009Map, CRObj, "9", "SCORE9", classline, "HYOUTEI");  //学年末評定
            cli++;
        }
      svf.VrEndPage();
      _hasData = true;
    }
    private void printScore(final Vrw32alp svf, final Map detailScoreMap, final ChairRecord CRObj, final String semesStr, final String prtField, final int classline, final String prtType) {
        final String fKey = CRObj.getFullSubclassCd();
        if (detailScoreMap == null || (!_param._isLastSemester && semesStr.compareTo(_param._gakki) > 0)) {  //データが取れていなかったり、指定学期より後のデータは出力しない。
        	return;
        }
        if (detailScoreMap.containsKey(fKey + ":" + semesStr)) {
        	final ValuationInfo prtWk = (ValuationInfo)detailScoreMap.get(fKey + ":" + semesStr);
        	if (prtWk._score != null) {
        		if ("H".equals(_param._schoolkind) && "HYOUTEI".equals(prtType)) {
        			if ("".equals(StringUtils.defaultString(prtWk._score, ""))) {
        				//何もしない。
        			} else if (7 <= Integer.parseInt(prtWk._score)) {
                        svf.VrsOutn(prtField, classline, "認定");
        			} else if (Integer.parseInt(prtWk._score) == 1) {
                        svf.VrsOutn(prtField, classline, "不認定");
        			} else {
                        svf.VrsOutn(prtField, classline, prtWk._score);
        			}
        		} else {
                    svf.VrsOutn(prtField, classline, prtWk._score);
        		}
        	} else {
                if ("HYOUKA".equals(prtType) || "HYOUTEI".equals(prtType)) {
                    svf.VrsOutn(prtField, classline, "-");
                }
        	}
        } else {
        	if ("HYOUKA".equals(prtType) || "HYOUTEI".equals(prtType)) {
                svf.VrsOutn(prtField, classline, "-");
        	}
        }
    }

    private void printAttendance(final Vrw32alp svf, final Student student) {
        final String[] seme = {SEME1,SEME2, SEME3, SEMEALL};
        for (int semei = 0; semei < seme.length; semei++) {
            final int line = semei + 1;
            if (!_param._isLastSemester && Integer.parseInt(seme[semei]) > Integer.parseInt(_param._gakki)) {
                continue;
            }

            final AttendSemesDat att = (AttendSemesDat) student._attendSemesDatMap.get(seme[semei]);
            if (null != att) {
                svf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
                svf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend + att._mourning + att._virus + att._koudome)); // 出停・忌引
                svf.VrsOutn("ABSENT", line, String.valueOf(att._sick)); // 欠席日数
                svf.VrsOutn("WORSHIP_ABSENT", line, String.valueOf(att._reihaiKekka)); // 礼拝欠席
                svf.VrsOutn("WORSHIP_LATE", line, String.valueOf(att._reihaiLate)); // 礼拝遅刻
                svf.VrsOutn("NOTICE", line, String.valueOf(att._present)); // 授業欠席
                svf.VrsOutn("LATE", line, String.valueOf(att._late)); // 遅刻
                svf.VrsOutn("EARLY", line, String.valueOf(att._early)); // 早退
                svf.VrsOutn("ABROAD", line, String.valueOf(att._transferDate)); // 留学
            }
        }
    }

    private void printShoken(final Vrw32alp svf, final Student student) {
    	for (Iterator ite = student._hReportRemarkDatMap.keySet().iterator();ite.hasNext();) {
    		final String semesStr = (String)ite.next();
    		if (semesStr.compareTo(_param._gakki) > 0) continue;
    		HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) student._hReportRemarkDatMap.get(semesStr);

            svfVrsOutnRepeat(svf, "COMMENT" + semesStr, KNJ_EditKinsoku.getTokenList(hReportRemarkDat._communication, 60, 9));
    	}
    }

    private void svfVrsOutnRepeat(final Vrw32alp svf, final String field, final List token) {
        for (int j = 0; j < token.size(); j++) {
            final int line = j + 1;
            svf.VrsOutn(field, line, (String) token.get(j));
        }
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        final String _staffname;
        final String _staffname2;
        Map _viewRecordMap = Collections.EMPTY_MAP; // 観点
        Map _ValuationInfoMap = Collections.EMPTY_MAP; // 評定
        Map _attendSemesDatMap = Collections.EMPTY_MAP; // 出欠の記録
        Map _hReportRemarkDatMap = Collections.EMPTY_MAP; // 通知表所見
        List _chairSubclassList = Collections.EMPTY_LIST;

        public Student(final String schregno, final String name, final String hrName, final String attendno, final String staffname, final String staffname2) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
            _staffname = staffname;
            _staffname2 = staffname2;
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentSql(param);
                log.info(" regd sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");

                    final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                    final String hrName = rs.getString("HR_NAME");
                    final String attendno = rs.getString("ATTENDNO");
                    final String staffname = rs.getString("STAFFNAME");
                    final String staffname2 = rs.getString("STAFFNAME2");
                    final Student student = new Student(schregno, name, hrName, attendno, staffname, staffname2);
                    studentList.add(student);
                }

            } catch (SQLException e) {
                log.error("exception!!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            AttendSemesDat.setAttendSemesDatList(db2, param, studentList);     //☆
            HReportRemarkDat.setHReportRemarkDatMap(db2, param, studentList);  //☆
            ChairRecord.setChairRecordList(db2, param, studentList);           //☆
            ValuationInfo.setValuationInfoList(db2, param, studentList);       //☆
            ChairSubclass.load(db2, param, studentList);                       //★
            return studentList;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append(" WITH SCHNO_A AS ( ");
            stb.append("    SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append("    FROM    SCHREG_REGD_DAT T1 ");
            stb.append("            , V_SEMESTER_GRADE_MST T2 ");
            stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.SEMESTER = '"+ param._gakki +"' ");
            stb.append("        AND T1.YEAR = T2.YEAR ");
            stb.append("        AND T1.GRADE = T2.GRADE ");
            stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("        AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("        AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
            //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
            stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("                           AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
            stb.append("                             OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
            //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
            stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("                           AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(" ) ");
            //メイン表
            stb.append(" SELECT T1.SCHREGNO, ");
            stb.append("        T7.HR_NAME, ");
            stb.append("        T1.ATTENDNO, ");
            stb.append("        T5.NAME, ");
            stb.append("        T5.REAL_NAME, ");
            stb.append("        CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append("        T8.STAFFNAME, ");
            stb.append("        T9.STAFFNAME AS STAFFNAME2 ");
            stb.append(" FROM   SCHNO_A T1 ");
            stb.append("        INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append("        LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
            stb.append("        LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = '" + param._year + "' AND T7.SEMESTER = T1.SEMESTER AND T7.GRADE = T1.GRADE AND T7.HR_CLASS = T1.HR_CLASS ");
            stb.append("        LEFT JOIN STAFF_MST T8 ON T8.STAFFCD = T7.TR_CD1 ");
            stb.append("        LEFT JOIN STAFF_MST T9 ON T9.STAFFCD = T7.TR_CD2 ");
            stb.append(" ORDER BY ATTENDNO ");
            return stb.toString();
        }
    }

    /**
     * 学習の記録（講座）
     */
   	private static class ChairRecord {
	    final String _year;
	    final  String _classcd;
	    final String _school_Kind;
	    final String _curriculum_Cd;
	    final String _subclasscd;
	    final String _chaircd;
	    final String _chairname;
	    final String _chairabbv;
	    final String _credits;
	    final String _classname;
	    final String _classabbv;
	    final String _subclassname;
	    final String _subclassabbv;
	    String _staffnames;
	    public ChairRecord (final String year, final String classcd, final String school_Kind, final String curriculum_Cd, final String subclasscd, final String chaircd, final String chairname, final String chairabbv,
	             final String credits, final String classname, final String classabbv, final String subclassname, final String subclassabbv)
        {
            _year = year;
            _classcd = classcd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclasscd = subclasscd;
            _chaircd = chaircd;
            _chairname = chairname;
            _chairabbv = chairabbv;
            _credits = credits;
            _classname = classname;
            _classabbv = classabbv;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
            _staffnames = "";
        }

	    public String getFullSubclassCd() {
	    	return _classcd + "-" + _school_Kind + "-" + _curriculum_Cd + "-" + _subclasscd;
	    }
        public static void setChairRecordList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getChairRecordSql(param);
                log.debug(" setChairRecordList sql = "+  sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._viewRecordMap = new LinkedMap();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                    	final String year = rs.getString("YEAR");
                    	final String classcd = rs.getString("CLASSCD");
                    	final String school_Kind = rs.getString("SCHOOL_KIND");
                    	final String curriculum_Cd = rs.getString("CURRICULUM_CD");
                    	final String subclasscd = rs.getString("SUBCLASSCD");
                    	final String chaircd = rs.getString("CHAIRCD");
                    	final String chairname = rs.getString("CHAIRNAME");
                    	final String chairabbv = rs.getString("CHAIRABBV");
                    	final String credits = rs.getString("CREDITS");
                    	final String classname = rs.getString("CLASSNAME");
                    	final String classabbv = rs.getString("CLASSABBV");
                    	final String subclassname = rs.getString("SUBCLASSNAME");
                    	final String subclassabbv = rs.getString("SUBCLASSABBV");
                    	final String staffname = rs.getString("STAFFNAME_SHOW");

                        final String pKey = chaircd;
                        final String sKey = classcd + "-" + school_Kind + "-" +  curriculum_Cd + "-" +  subclasscd;
                        final String psKey = pKey + ":" + sKey;
                        final ChairRecord viewRecord;
                        final String sep;
                        if (student._viewRecordMap.containsKey(psKey)) {
                            viewRecord = (ChairRecord)student._viewRecordMap.get(psKey);
                            sep = "".equals(viewRecord._staffnames) ? "" : "・";
                        } else {
                            viewRecord = new ChairRecord(year, classcd, school_Kind, curriculum_Cd, subclasscd, chaircd, chairname, chairabbv, credits, classname, classabbv, subclassname, subclassabbv);
                            student._viewRecordMap.put(pKey + ":" + sKey, viewRecord);
                            sep = "";
                        }
                        viewRecord._staffnames += "".equals(StringUtils.defaultString(staffname, "")) ? "" : sep + staffname;
                    }

                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getChairRecordSql(final Param param) {
        	final StringBuffer stb = new StringBuffer();
        	stb.append(" WITH CSDAT AS ( ");
        	stb.append(" SELECT ");
        	stb.append("   T01.YEAR, ");
        	stb.append("   T01.SEMESTER, ");
        	stb.append("   T00.SCHREGNO, ");
        	stb.append("   T01.APPDATE, ");
        	stb.append("   T01.APPENDDATE, ");
        	stb.append("   T02.CLASSCD, ");
        	stb.append("   T02.SCHOOL_KIND, ");
        	stb.append("   T02.CURRICULUM_CD, ");
        	stb.append("   T02.SUBCLASSCD, ");
        	stb.append("   T01.CHAIRCD, ");
        	stb.append("   T02.CHAIRNAME, ");
        	stb.append("   T02.CHAIRABBV, ");
        	stb.append("   T03.CREDITS, ");
        	stb.append("   M1.CLASSNAME, ");
        	stb.append("   M1.CLASSABBV, ");
        	stb.append("   M2.SUBCLASSNAME, ");
        	stb.append("   M2.SUBCLASSABBV, ");
        	stb.append("   M3.CHARGEDIV, ");
        	stb.append("   M4.STAFFCD, ");
        	stb.append("   M4.STAFFNAME_SHOW ");
        	stb.append(" FROM ");
        	stb.append("   SCHREG_REGD_DAT T00 ");
        	stb.append("   LEFT JOIN CHAIR_STD_DAT T01 ");
        	stb.append("     ON T01.YEAR = T00.YEAR ");
        	stb.append("    AND T01.SEMESTER = T00.SEMESTER ");
        	stb.append("    AND T01.SCHREGNO = T00.SCHREGNO ");
        	stb.append("   LEFT JOIN CHAIR_DAT T02 ");
        	stb.append("     ON T02.YEAR = T01.YEAR ");
        	stb.append("    AND T02.SEMESTER = T01.SEMESTER ");
        	stb.append("    AND T02.CHAIRCD = T01.CHAIRCD ");
        	stb.append("   LEFT JOIN CREDIT_MST T03 ");
        	stb.append("     ON T03.YEAR = T00.YEAR ");
        	stb.append("    AND T03.COURSECD = T00.COURSECD ");
        	stb.append("    AND T03.MAJORCD = T00.MAJORCD ");
        	stb.append("    AND T03.COURSECODE = T00.COURSECODE ");
        	stb.append("    AND T03.GRADE = T00.GRADE ");
        	stb.append("    AND T03.CLASSCD = T02.CLASSCD ");
        	stb.append("    AND T03.SCHOOL_KIND = T02.SCHOOL_KIND ");
        	stb.append("    AND T03.CURRICULUM_CD = T02.CURRICULUM_CD ");
        	stb.append("    AND T03.SUBCLASSCD = T02.SUBCLASSCD ");
        	stb.append("   LEFT JOIN CLASS_MST M1 ");
        	stb.append("     ON M1.CLASSCD = T02.CLASSCD ");
        	stb.append("    AND M1.SCHOOL_KIND = T02.SCHOOL_KIND ");
        	stb.append("   LEFT JOIN SUBCLASS_MST M2 ");
        	stb.append("     ON M2.CLASSCD = T02.CLASSCD ");
        	stb.append("    AND M2.SCHOOL_KIND = T02.SCHOOL_KIND ");
        	stb.append("    AND M2.CURRICULUM_CD = T02.CURRICULUM_CD ");
        	stb.append("    AND M2.SUBCLASSCD = T02.SUBCLASSCD ");
        	stb.append("   LEFT JOIN CHAIR_STF_DAT M3 ");
        	stb.append("     ON M3.YEAR = T01.YEAR ");
        	stb.append("    AND M3.SEMESTER = T01.SEMESTER ");
        	stb.append("    AND M3.CHAIRCD = T01.CHAIRCD ");
        	stb.append("   LEFT JOIN STAFF_MST M4 ");
        	stb.append("     ON M4.STAFFCD = M3.STAFFCD ");
        	stb.append(" WHERE ");
        	stb.append("   T01.YEAR = '" + param._year + "' ");
        	stb.append("   AND T01.SEMESTER <= '" + param._gakki + "' ");
        	stb.append("   AND T01.SCHREGNO = ? ");
        	stb.append("   AND T02.CLASSCD < '90' ");
        	stb.append(" ), APPD_FILTER AS ( ");
        	//現在学期のデータ
        	stb.append(" SELECT ");
        	stb.append("   T1.* ");
        	stb.append(" FROM ");
        	stb.append("   CSDAT T1 ");
        	stb.append(" WHERE ");
        	stb.append("   T1.SEMESTER = '" + param._gakki + "' ");
        	stb.append("   AND '" + param._date + "' BETWEEN T1.APPDATE AND T1.APPENDDATE ");
        	stb.append(" UNION ALL ");
        	//過学期のデータ
        	stb.append(" SELECT ");
        	stb.append("   T2.* ");
        	stb.append(" FROM ");
        	stb.append("   CSDAT T2 ");
        	stb.append(" WHERE ");
        	stb.append("   T2.SEMESTER < '" + param._gakki + "' ");
        	stb.append("   AND T2.APPENDDATE = ( ");
        	stb.append("                        SELECT ");
        	stb.append("                          MAX(T3.APPENDDATE) ");
        	stb.append("                        FROM ");
        	stb.append("                          CSDAT T3 ");
        	stb.append("                        WHERE ");
        	stb.append("                          T3.YEAR = T2.YEAR ");
        	stb.append("                          AND T3.SEMESTER = T2.SEMESTER ");
        	stb.append("                          AND T3.SCHREGNO = T2.SCHREGNO ");
        	stb.append("                          AND T3.CLASSCD = T2.CLASSCD ");
        	stb.append("                          AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
        	stb.append("                          AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
        	stb.append("                          AND T3.SUBCLASSCD = T2.SUBCLASSCD ");
        	stb.append("                       ) ");
        	stb.append(" ) ");
        	stb.append(" SELECT DISTINCT  ");
        	stb.append("   YEAR, ");
        	stb.append("   SCHREGNO, ");
        	stb.append("   CLASSCD, ");
        	stb.append("   SCHOOL_KIND, ");
        	stb.append("   CURRICULUM_CD, ");
        	stb.append("   SUBCLASSCD, ");
        	stb.append("   CHAIRCD, ");
        	stb.append("   CHAIRNAME, ");
        	stb.append("   CHAIRABBV, ");
        	stb.append("   CREDITS, ");
        	stb.append("   CLASSNAME, ");
        	stb.append("   CLASSABBV, ");
        	stb.append("   SUBCLASSNAME, ");
        	stb.append("   SUBCLASSABBV, ");
        	stb.append("   CHARGEDIV, ");
        	stb.append("   STAFFCD, ");      //スタッフの同名で集約がかからないようにするためのスタッフコード。クラス格納時に省略して、名称の集約をかける。
        	stb.append("   STAFFNAME_SHOW ");
        	stb.append(" FROM ");
        	stb.append("   APPD_FILTER ");
        	stb.append(" ORDER BY ");
        	stb.append("   YEAR, ");
        	stb.append("   SCHREGNO, ");
        	stb.append("   CLASSCD, ");
        	stb.append("   SCHOOL_KIND, ");
        	stb.append("   CURRICULUM_CD, ");
        	stb.append("   SUBCLASSCD, ");
        	stb.append("   CHARGEDIV DESC, ");
        	stb.append("   STAFFCD ");
        	return stb.toString();
        }
    }

    /**
     * 学習の記録（評定）
     */
    private static class ValuationInfo {
   	    final String _year;
   	    final String _schregno;
   	    final String _classcd;
   	    final String _school_Kind;
   	    final String _curriculum_Cd;
   	    final String _subclasscd;
   	    final String _semester;
   	    final String _score;
   	    public ValuationInfo (final String year, final String schregno, final String classcd, final String school_Kind, final String curriculum_Cd, final String subclasscd, final String semester, final String score)
   	    {
   	   	    _year = year;
   	   	    _schregno = schregno;
   	   	    _classcd = classcd;
   	   	    _school_Kind = school_Kind;
   	   	    _curriculum_Cd = curriculum_Cd;
   	   	    _subclasscd = subclasscd;
   	   	    _semester = semester;
   	   	    _score = score;
        }

        public static void setValuationInfoList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map subMap = null;
            try {
                final String sql = getViewValuationSql(param);
                log.debug(" setValuationInfoList sql = "+  sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._ValuationInfoMap = new LinkedMap();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                    	final String year = rs.getString("YEAR");
                    	final String schregno = rs.getString("SCHREGNO");
                    	final String classcd = rs.getString("CLASSCD");
                    	final String school_Kind = rs.getString("SCHOOL_KIND");
                    	final String curriculum_Cd = rs.getString("CURRICULUM_CD");
                    	final String subclasscd = rs.getString("SUBCLASSCD");
                    	final String semester = rs.getString("SEMESTER");
                        final String testKindCd = rs.getString("TESTKINDCD");
                        final String testItemCd = rs.getString("TESTITEMCD");
                        final String scoreDiv = rs.getString("SCORE_DIV");
                    	final String score = rs.getString("SCORE");
                        final ValuationInfo viewValuation = new ValuationInfo(year, schregno, classcd, school_Kind, curriculum_Cd, subclasscd, semester, score);

                        final String fKey = testKindCd + testItemCd + scoreDiv;
                        if (student._ValuationInfoMap.containsKey(fKey)) {
                        	subMap = (Map)student._ValuationInfoMap.get(fKey);
                        } else {
                        	subMap = new LinkedMap();
                        	student._ValuationInfoMap.put(fKey, subMap);
                        }
                        final String sKey = classcd + "-" + school_Kind + "-" + curriculum_Cd + "-" + subclasscd + ":" + semester;
                        subMap.put(sKey, viewValuation);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getViewValuationSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T04.YEAR, ");
            stb.append("   T04.SCHREGNO, ");
            stb.append("   T04.CLASSCD, ");
            stb.append("   T04.SCHOOL_KIND, ");
            stb.append("   T04.CURRICULUM_CD, ");
            stb.append("   T04.SUBCLASSCD, ");
            stb.append("   T04.SEMESTER, ");
            stb.append("   T04.TESTKINDCD, ");
            stb.append("   T04.TESTITEMCD, ");
            stb.append("   T04.SCORE_DIV, ");
            stb.append("   T04.SCORE ");
            stb.append(" FROM ");
            stb.append("   RECORD_RANK_SDIV_DAT T04 ");
            stb.append(" WHERE ");
            stb.append("   T04.YEAR = '" + param._year + "' ");
            if (param._isLastSemester) {
                stb.append("   AND T04.SEMESTER <= '9' ");
           } else {
                stb.append("   AND T04.SEMESTER <= '" + param._gakki + "' ");
            }
            stb.append("   AND T04.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("   SCHREGNO, ");
            stb.append("   YEAR, ");
            stb.append("   CLASSCD, ");
            stb.append("   SCHOOL_KIND, ");
            stb.append("   CURRICULUM_CD, ");
            stb.append("   SUBCLASSCD, ");
            stb.append("   SEMESTER ");
            return stb.toString();
        }
    }

    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {

        final String _semester;
        int _lesson;
        int _suspend;
        int _mourning;
        int _mlesson;
        int _sick;
        int _absent;
        int _present;
        int _late;
        int _early;
        int _transferDate;
        int _offdays;
        int _kekkaJisu;
        int _virus;
        int _koudome;
        int _reihaiKekka;
        int _reihaiLate;

        public AttendSemesDat(
                final String semester
        ) {
            _semester = semester;
        }

        public void add(
                final AttendSemesDat o
        ) {
            _lesson += o._lesson;
            _suspend += o._suspend;
            _mourning += o._mourning;
            _mlesson += o._mlesson;
            _sick += o._sick;
            _absent += o._absent;
            _present += o._present;
            _late += o._late;
            _early += o._early;
            _transferDate += o._transferDate;
            _offdays += o._offdays;
            _kekkaJisu += o._kekkaJisu;
            _virus += o._virus;
            _koudome += o._koudome;
        }

        private static void setAttendSemesDatList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._gakki,
                        null,
                        param._date,
                        param._attendParamMap
                );
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._attendSemesDatMap = new HashMap();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");
                        final int lesson = rs.getInt("LESSON");
                        final int suspend = rs.getInt("SUSPEND");
                        final int mourning = rs.getInt("MOURNING");
                        final int mlesson = rs.getInt("MLESSON");
                        final int sick = rs.getInt("SICK");
                        final int absent = rs.getInt("ABSENT");
                        final int present = rs.getInt("PRESENT");
                        final int late = rs.getInt("LATE");
                        final int early = rs.getInt("EARLY");
                        final int transferDate = rs.getInt("TRANSFER_DATE");  //留学?
                        final int offdays = rs.getInt("OFFDAYS");  //留学?
                        final int kekkaJisu = rs.getInt("KEKKA_JISU");
                        final int virus = rs.getInt("VIRUS");
                        final int koudome = rs.getInt("KOUDOME");
                        final int reihaiKekka = rs.getInt("REIHAI_KEKKA");  //礼拝欠席
                        final int reihaiLate = rs.getInt("REIHAI_TIKOKU");  //礼拝遅刻

                        final AttendSemesDat attendSemesDat = new AttendSemesDat(semester);
                        attendSemesDat._lesson = lesson;
                        attendSemesDat._suspend = suspend;
                        attendSemesDat._mourning = mourning;
                        attendSemesDat._mlesson = mlesson;
                        attendSemesDat._sick = sick;
                        attendSemesDat._absent = absent;
                        attendSemesDat._present = present;
                        attendSemesDat._late = late;
                        attendSemesDat._early = early;
                        attendSemesDat._transferDate = transferDate;
                        attendSemesDat._offdays = offdays;
                        attendSemesDat._kekkaJisu = kekkaJisu;
                        attendSemesDat._virus = virus;
                        attendSemesDat._koudome = koudome;
                        attendSemesDat._reihaiKekka = reihaiKekka;
                        attendSemesDat._reihaiLate = reihaiLate;

                        student._attendSemesDatMap.put(semester, attendSemesDat);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

    }

    private static class ChairSubclass {
        final String _subclasscd;
        final List _chaircdList;
        public ChairSubclass(final String subclasscd) {
            _subclasscd = subclasscd;
            _chaircdList = new ArrayList();
        }
        public static void load(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT ");
                stb.append("   T2.CHAIRCD, ");
                stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD ");
                stb.append(" FROM ");
                stb.append("   CHAIR_STD_DAT T1 ");
                stb.append("   INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("     AND T2.CHAIRCD = T1.CHAIRCD ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + param._year + "' ");
                if (!param._isLastSemester) {
                    stb.append("     AND T1.SEMESTER <= '" + param._gakki + "' ");
                }
                stb.append("     AND T1.SCHREGNO = ? ");
                stb.append("     AND '" + param._date + "' BETWEEN T1.APPDATE AND T1.APPENDDATE ");

                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._chairSubclassList = new ArrayList();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {

                        ChairSubclass cs = getChairSubclass(student._chairSubclassList, rs.getString("SUBCLASSCD"));
                        if (null == cs) {
                            cs = new ChairSubclass(rs.getString("SUBCLASSCD"));
                            student._chairSubclassList.add(cs);
                        }
                        cs._chaircdList.add(rs.getString("CHAIRCD"));
                    }
                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        public static ChairSubclass getChairSubclass(final List chairSubclassList, final String subclasscd) {
            ChairSubclass chairSubclass = null;
            for (final Iterator it = chairSubclassList.iterator(); it.hasNext();) {
                final ChairSubclass cs = (ChairSubclass) it.next();
                if (null != cs._subclasscd && cs._subclasscd.equals(subclasscd)) {
                    chairSubclass = cs;
                    break;
                }
            }
            return chairSubclass;
        }
    }

    /**
     * 通知表所見
     */
    private static class HReportRemarkDat {
        final String _semester;
        final String _communication;

        public HReportRemarkDat (final String semester, final String communication)
        {
            _semester = semester;
            _communication = communication;
        }

        public static void setHReportRemarkDatMap(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHReportRemarkSql(param);
                log.debug("setHReportRemarkDatMap sql = "+sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._hReportRemarkDatMap = new LinkedMap();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String communication = rs.getString("COMMUNICATION");
                        final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(semester, communication);
                        student._hReportRemarkDatMap.put(semester, hReportRemarkDat);
                    }
                    DbUtils.closeQuietly(rs);

                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getHReportRemarkSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   SEMESTER, ");
            stb.append("   COMMUNICATION ");
            stb.append(" FROM ");
            stb.append("   HREPORTREMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("   YEAR = '" + param._year + "' ");
            stb.append("   AND SEMESTER <= '" + param._gakki + "' ");
            stb.append("   AND SCHREGNO = ? ");
            stb.append(" ORDER BY SEMESTER ");

            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75465 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _gakki;
        final String _date;
        final String _gradeHrclass;
        final String _grade;
        final String _hrClass;
        final String[] _categorySelected;
        final String _ctrlDate;

        final String _gradeCdStr;
        final String _certifSchoolSchoolName;
        final String _certifSchoolPrincipalName;
        final String _gradeName;
        private boolean _isLastSemester;

        final Map _attendParamMap;

        private String _schoolkind;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
        	_ctrlDate = request.getParameter("CTRL_DATE");
            _year = request.getParameter("YEAR");
            _gakki = request.getParameter("GAKKI");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            _hrClass = null != _gradeHrclass && _gradeHrclass.length() >= 5 ? _gradeHrclass.substring(2, 5) : null;
            _categorySelected = request.getParameterValues("category_selected");
            _schoolkind = getSchoolKind(db2);

            _gradeCdStr = getGradeCdIntStr(db2, _grade, "GRADE_CD");
            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _gradeName = getNameMstChkName1(db2, _year, "A023", _schoolkind, "ABBV1") + Integer.parseInt(_gradeCdStr) + "年";

            setSemester(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);


        }

        private String getSchoolKind(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2," SELECT GDAT.SCHOOL_KIND FROM SCHREG_REGD_GDAT GDAT WHERE GDAT.YEAR = '" + _year + "' AND GDAT.GRADE = '" + _grade + "' "));
        }

        private String getNameMst(final DB2UDB db2, final String year, final String namecd1, final String namecd2, final String field) {
            return getNameMstFnc(db2, year, namecd1, namecd2, field, false);
        }
        private String getNameMstChkName1(final DB2UDB db2, final String year, final String namecd1, final String namecd2, final String field) {
            return getNameMstFnc(db2, year, namecd1, namecd2, field, true);
        }
        private String getNameMstFnc(final DB2UDB db2, final String year, final String namecd1, final String namecd2, final String field, final boolean chkName) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("    " + field + " ");
                sql.append(" FROM NAME_MST T1 ");
                sql.append("    INNER JOIN NAME_YDAT T2 ON T2.YEAR = '" + year + "' AND T2.NAMECD1 = T1.NAMECD1 AND T2.NAMECD2 = T1.NAMECD2 ");
                sql.append(" WHERE ");
                sql.append("    T1.NAMECD1 = '" + namecd1 + "' ");
                if (chkName) {  //A023用チェック
                    sql.append("    AND T1.NAME1 = '" + namecd2 + "' ");
                } else {
                    sql.append("    AND T1.NAMECD2 = '" + namecd2 + "' ");
                }
                sql.append("   ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            final String CKCode = "H".equals(_schoolkind) ? "104" : "103";
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + CKCode + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getGradeCdIntStr(final DB2UDB db2, final String grade, final String getField) {
            String gradeCd = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     " + getField + " ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String tmp = rs.getString(getField);
                    if ("GRADE_CD".equals(getField)) {
                        if (NumberUtils.isDigits(tmp)) {
                            gradeCd = String.valueOf(Integer.parseInt(tmp));
                        }
                    } else {
                        gradeCd = tmp;
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradeCd;
        }

        private void setSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String lastSemester = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SEMESTER_MST T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                stb.append(" ORDER BY ");
                stb.append("     T1.SEMESTER ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    if (!SEMEALL.equals(semester)) {
                        lastSemester = semester;
                    }
                }
                _isLastSemester = null != lastSemester && lastSemester.equals(_gakki);
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}

// eof

