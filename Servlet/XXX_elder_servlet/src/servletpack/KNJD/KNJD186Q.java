/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: b55ee616551434bf7a21dd12d4873645a8c7056e $
 *
 * 作成日: 2018/09/21
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJD186Q {

    private static final Log log = LogFactory.getLog(KNJD186Q.class);

    private boolean _hasData;

    private Param _param;

    KNJEditString knjobj = new KNJEditString();

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
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            svf.VrSetForm("KNJD186Q.frm", 4);
            final Student student = (Student) iterator.next();
            svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度　" + _param._semesterName + "　成績通知票");
            svf.VrsOut("SCHREGNO", student._schregNo);
            svf.VrsOut("HR_NAME", student._hrName);
            svf.VrsOut("NAME", student._name);
            svf.VrsOut("SCHOOL_NAME", _param._schoolName);
            svf.VrsOut("TR_NAME", student._trName);
            printSvfHReport(svf, student);

            int totalCredit = 0;
            int printCnt = 0;
            for (Iterator itScore = student._scoreList.iterator(); itScore.hasNext();) {
                final SubclassScore subclassScore = (SubclassScore) itScore.next();
                svf.VrsOut("CLASS_NAME", subclassScore._className);
                svf.VrsOut("SUBCLASS_NAME", subclassScore._subclassName);
                svf.VrsOut("VALUE", StringUtils.defaultString(subclassScore._score));
                if (!"1".equals(subclassScore._provFlg)) {
                    svf.VrsOut("CREDIT", StringUtils.defaultString(subclassScore._credit));
                    if (null != subclassScore._credit && !"".equals(subclassScore._credit)) {
                        totalCredit += Integer.parseInt(subclassScore._credit);
                    }
                }
                svf.VrEndRecord();
                printCnt++;
                if (printCnt >= 24) {
                    printCnt = 0;
                }
            }
            for (int i = printCnt; i < 23; i++) {
                svf.VrsOut("BLANK", String.valueOf(i));
                svf.VrEndRecord();
            }
            svf.VrsOut("TOTAL_VALUE_NAME", "計");
            svf.VrsOut("TOTAL_CREDIT", String.valueOf(totalCredit));
            svf.VrEndRecord();
            _hasData = true;
        }
    }

    /**
     * 『通信欄』を印字する
     * @param svf
     * @param student
     */
    private void printSvfHReport(final Vrw32alp svf, final Student student) {

        final int pcharsttCM = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE, 0);
        final int plinesttCM = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE, 1);
        final int charsttCM = (-1 == pcharsttCM || -1 == plinesttCM) ? 40 : pcharsttCM;
        final int linesttCM = (-1 == pcharsttCM || -1 == plinesttCM) ?  7 : plinesttCM;

        for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) it.next();

            //通信欄
            VrsOutnRenban(svf, "COMM", knjobj.retDividString(hReportRemarkDat._communication, charsttCM * 2, linesttCM));
        }

    }

    /**
     * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
     * @param param サイズタイプのパラメータ文字列
     * @param pos split後のインデクス (0:w, 1:h)
     * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
     */
    private static int getParamSizeNum(final String param, final int pos) {
        int num = -1;
        String[] nums = StringUtils.split(param, " * ");
        if (StringUtils.isBlank(param) || !(0 <= pos && pos < nums.length)) {
            num = -1;
        } else {
            try {
                num = Integer.valueOf(nums[pos]).intValue();
            } catch (Exception e) {
                log.error("Exception!", e);
            }
        }
        return num;
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final List list) {
        if (null != list) {
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOutn(field, i + 1, (String) list.get(i));
            }
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String hrName = rs.getString("HR_NAME");
                final String trName = rs.getString("STAFFNAME");
                final String attendNo = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");

                final Student student = new Student(schregNo, hrName, trName, attendNo, name);
                student.setScore(db2);
                student.getHReportRemarkDatList(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     STAFF.STAFFNAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGDH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST STAFF ON STAFF.STAFFCD = REGDH.TR_CD1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._classSelected) + " ");
        stb.append("     AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                    WHERE ");
        stb.append("                        S1.SCHREGNO = BASE.SCHREGNO ");
        stb.append("                        AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < '" + _param._date + "') ");
        stb.append("                              OR ");
        stb.append("                             (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > '" + _param._date + "')) ");
        stb.append("                   ) ");
        stb.append("     AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append("                    WHERE ");
        stb.append("                        S1.SCHREGNO = BASE.SCHREGNO ");
        stb.append("                        AND S1.TRANSFERCD IN ('2') ");
        stb.append("                        AND '" + _param._date + "' BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE ");
        stb.append("                   ) ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        log.debug(stb.toString());
        return stb.toString();
    }

    private class Student {
        final String _schregNo;
        final String _hrName;
        final String _trName;
        final String _attendNo;
        final String _name;
        final List _scoreList;
        final List _hReportRemarkDatList;
        public Student(final String schregNo, final String hrName, final String trName, final String attendNo, final String name) {
            _schregNo = schregNo;
            _hrName = hrName;
            _trName = trName;
            _attendNo = attendNo;
            _name = name;
            _scoreList = new ArrayList();
            _hReportRemarkDatList = new ArrayList();
        }
        private void setScore(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SUBM AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     CDAT.CLASSCD, ");
            stb.append("     CDAT.SCHOOL_KIND, ");
            stb.append("     CDAT.CURRICULUM_CD, ");
            stb.append("     CDAT.SUBCLASSCD, ");
            stb.append("     CLM.CLASSNAME, ");
            stb.append("     SUBM.SUBCLASSNAME, ");
            stb.append("     PROV.PROV_FLG ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT CSTD ");
            stb.append("     INNER JOIN CHAIR_DAT CDAT ON CSTD.YEAR = CDAT.YEAR ");
            stb.append("           AND CSTD.SEMESTER = CDAT.SEMESTER ");
            stb.append("           AND CSTD.CHAIRCD = CDAT.CHAIRCD ");
            stb.append("           AND CDAT.CLASSCD != '81' ");
            stb.append("           AND CDAT.CLASSCD <= '90' ");
            if ("1".equals(_param._printZenki)) {
                stb.append("           AND CDAT.TAKESEMES = '1' ");
            } else if ("1".equals(_param._printKouki)) {
                stb.append("           AND VALUE(CDAT.TAKESEMES, '0') <> '1' ");
            }
            if ("1".equals(_param._printSubclassLastChairStd)) {
                stb.append("     INNER JOIN SEMESTER_MST STDSEME ON STDSEME.YEAR = CSTD.YEAR ");
                stb.append("       AND STDSEME.SEMESTER = CSTD.SEMESTER ");
                stb.append("       AND STDSEME.EDATE = CSTD.APPENDDATE ");
            }
            stb.append("     LEFT JOIN CLASS_MST CLM ON CDAT.CLASSCD = CLM.CLASSCD ");
            stb.append("          AND CDAT.SCHOOL_KIND = CLM.SCHOOL_KIND ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON CDAT.CLASSCD = SUBM.CLASSCD ");
            stb.append("          AND CDAT.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
            stb.append("          AND CDAT.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            stb.append("          AND CDAT.SUBCLASSCD = SUBM.SUBCLASSCD ");
            stb.append("     LEFT JOIN RECORD_PROV_FLG_DAT PROV ON CDAT.YEAR = PROV.YEAR ");
            stb.append("          AND CDAT.CLASSCD = PROV.CLASSCD ");
            stb.append("          AND CDAT.SCHOOL_KIND = PROV.SCHOOL_KIND ");
            stb.append("          AND CDAT.CURRICULUM_CD = PROV.CURRICULUM_CD ");
            stb.append("          AND CDAT.SUBCLASSCD = PROV.SUBCLASSCD ");
            stb.append("          AND CSTD.SCHREGNO = PROV.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     CSTD.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND CSTD.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("     AND CSTD.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     SUBM.CLASSCD, ");
            stb.append("     SUBM.SCHOOL_KIND, ");
            stb.append("     SUBM.CURRICULUM_CD, ");
            stb.append("     SUBM.SUBCLASSCD, ");
            stb.append("     SUBM.CLASSNAME, ");
            stb.append("     SUBM.SUBCLASSNAME, ");
            stb.append("     SCORE.SCORE, ");
            stb.append("     SCORE.GET_CREDIT AS CREDIT, ");
            stb.append("     SUBM.PROV_FLG ");
            stb.append(" FROM ");
            stb.append("     SUBM ");
            stb.append("     LEFT JOIN RECORD_SCORE_DAT SCORE ON SCORE.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("          AND SCORE.SEMESTER = '9' ");
            stb.append("          AND SCORE.TESTKINDCD = '99' ");
            stb.append("          AND SCORE.TESTITEMCD = '00' ");
            stb.append("          AND SCORE.SCORE_DIV = '09' ");
            stb.append("          AND SCORE.CLASSCD = SUBM.CLASSCD ");
            stb.append("          AND SCORE.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
            stb.append("          AND SCORE.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            stb.append("          AND SCORE.SUBCLASSCD = SUBM.SUBCLASSCD ");
            stb.append("          AND SCORE.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SUBM.CLASSCD, ");
            stb.append("     SUBM.SCHOOL_KIND, ");
            stb.append("     SUBM.CURRICULUM_CD, ");
            stb.append("     SUBM.SUBCLASSCD ");

            log.debug(stb);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String className = rs.getString("CLASSNAME");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String score = rs.getString("SCORE");
                    final String credit = rs.getString("CREDIT");
                    final String provFlg = rs.getString("PROV_FLG");
                    final SubclassScore subclassScore = new SubclassScore(classCd, schoolKind, curriculumCd, subclasscd, className, subclassName, score, credit, provFlg);
                    _scoreList.add(subclassScore);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void getHReportRemarkDatList(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHReportRemarkSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String totalstudytime = rs.getString("TOTALSTUDYTIME");
                    final String specialactremark = rs.getString("SPECIALACTREMARK");
                    final String communication = rs.getString("COMMUNICATION");
                    final String remark1 = rs.getString("REMARK1");
                    final String remark2 = rs.getString("REMARK2");
                    final String remark3 = rs.getString("REMARK3");
                    final String attendrecRemark = rs.getString("ATTENDREC_REMARK");

                    final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(semester, totalstudytime, specialactremark, communication,
                            remark1, remark2, remark3, attendrecRemark);
                    _hReportRemarkDatList.add(hReportRemarkDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getHReportRemarkSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregNo + "' ");
            return stb.toString();
        }

    }

    private class SubclassScore {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _className;
        final String _subclassName;
        final String _score;
        final String _credit;
        final String _provFlg;
        public SubclassScore(
                final String classCd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String className,
                final String subclassName,
                final String score,
                final String credit,
                final String provFlg
        ) {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _className = className;
            _subclassName = subclassName;
            _score = score;
            _credit = StringUtils.defaultString(credit);
            _provFlg = provFlg;
        }
    }

    /**
     * 通知表所見
     */
    private class HReportRemarkDat {
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
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77012 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String[] _classSelected;
        private final String _date;
        final String _ctrlDate;
        final String _ctrlSemester;
        final String _ctrlYear;
        final String _schoolName;
        final String _semesterName;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE;
        final String _printZenki; // 1: 前期のみ出力
        final String _printKouki; // 1: 後期のみ出力
        /** 講座名簿の終了日付が学期の終了日付と同一 */
        final String _printSubclassLastChairStd;

        /** グラフイメージファイルの Set&lt;File&gt; */
        final Set _graphFiles = new HashSet();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _date = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE") );
            _ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _printZenki = request.getParameter("PRINT_ZENKI");
            _printKouki = request.getParameter("PRINT_KOUKI");
            _printSubclassLastChairStd = request.getParameter("printSubclassLastChairStd");
            _schoolName = getSchoolName(db2);
            _semesterName = getSemesterName(db2);
            _HREPORTREMARK_DAT_COMMUNICATION_SIZE = null == request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_H") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_H"), "+", " ");                      //通信欄
        }

        private String getSchoolName(final DB2UDB db2) {
            String retSchoolName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '104' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolName = rs.getString("SCHOOL_NAME");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
        }

        private String getSemesterName(final DB2UDB db2) {
            String rtn = "";

            final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _ctrlSemester + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("SEMESTERNAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

    }
}

// eof
