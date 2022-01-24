// kanji=漢字
/*
 * $Id: 6d8d4810ec3566587972d32eecb9fa4b5cfe129f $
 *
 * 作成日: 2009/10/08 16:46:14 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 6d8d4810ec3566587972d32eecb9fa4b5cfe129f $
 */
public class KNJF102A {

    private static final Log log = LogFactory.getLog("KNJF102A.class");

    private static final String FROM_TO_MARK = "\uFF5E";

    private boolean _hasData;

    public final String FORM_NAME = "KNJF102A.frm";

    public final String NAIKA = "1";
    public final String GEKA = "2";
    public final String SONOTA = "3";
    public final String SEITO_IGAI = "4";
    public final String KENKO_SODAN_KATSUDO = "5";

    public final String NAIKA_NAME = "内科";
    public final String GEKA_NAME = "外科";
    public final String SONOTA_NAME = "その他";
    public final String SEITO_IGAI_NAME = "生徒以外";
    public final String KENKO_SODAN_KATSUDO_NAME = "健康相談活動";

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
            init(response, svf);

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
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printData = getPritnData(db2);
        for (final Iterator itPrint = printData.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            for (final Iterator itVisit = student._nurseoffVisitrecDatList.iterator(); itVisit.hasNext();) {
                final List nurseoffVisitrecDatAllList = (List) itVisit.next();
                svf.VrSetForm(FORM_NAME, 1);
                final List pageList = getPageList(nurseoffVisitrecDatAllList, 25);

                for (int pi = 0; pi < pageList.size(); pi++) {

                    final List nurseoffVisitrecDatList = (List) pageList.get(pi);

                    for (int i = 0; i < nurseoffVisitrecDatList.size(); i++) {

                        final NurseoffVisitrecDat nvd = (NurseoffVisitrecDat) nurseoffVisitrecDatList.get(i);
                        final int line = i + 1;

                        svf.VrsOut("DATE1", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));
                        svf.VrsOut("PERIOD", _param.changePrintDateGengou(db2, _param._sDate) + FROM_TO_MARK + _param.changePrintDateGengou(db2, _param._eDate));
                        svf.VrsOut("HR_NAME", student._hrName);
                        svf.VrsOut("SUBJECT", student._majorName);
                        svf.VrsOut("NAME", student._name);
                        svf.VrsOut("SCHREGNO", student._schregno);
                        svf.VrsOut("ATTENDNO", Integer.valueOf(student._attendno).toString());
                        svf.VrsOut("GRADE", Integer.valueOf(student._grade).toString());
                        svf.VrsOut("SEX", student._sex);

                        svf.VrsOutn("VISIT_DATE", line, _param.changePrintDateGengou(db2, nvd._visitDate));
                        svf.VrsOutn("NENDO", line, KNJ_EditDate.h_format_JP_N(db2, nvd._visitDate));
                        if (null != nvd._visitHour || null != nvd._visitMinute) {
                            svf.VrsOutn("VISIT", line, StringUtils.defaultString(nvd._visitHour) + ":" + StringUtils.defaultString(nvd._visitMinute));
                        }
                        if (null != nvd._leaveHour || null != nvd._leaveMinute) {
                            svf.VrsOutn("LEAVE", line, StringUtils.defaultString(nvd._leaveHour) + ":" + StringUtils.defaultString(nvd._leaveMinute));
                        }
                        svf.VrsOutn("TYPE", line, nvd._typeName);
                        svf.VrsOutn("VISIT_REASON1", line, nvd._visitReasonName1);
                        svf.VrsOutn("TREATMENT1", line, nvd._treatmentName1);
                        if("fukuiken".equals(_param._schoolName)){
                            if(nvd._specialNote.length() != 0) {
                                final String [] remark = KNJ_EditEdit.get_token(nvd._specialNote, 100, 2);
                                svf.VrsOutn("REMARK2_1", line, remark[0]);
                                svf.VrsOutn("REMARK2_2", line, remark[1]);
                            }
                        }else {
                            svf.VrsOutn("REMARK1", line, nvd._specialNote);
                        }
                    }

                    svf.VrEndPage();
                }
            }
            _hasData = true;
        }
    }

    private List getPritnData(DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String nurseOffSql = getNurseOffSql();
        log.debug(" nurseOffSql = " + nurseOffSql);
        PreparedStatement psNurs = null;
        ResultSet rsNurs = null;
        try {
            psNurs = db2.prepareStatement(nurseOffSql);
            rsNurs = psNurs.executeQuery();
            while (rsNurs.next()) {
                final String schregno = rsNurs.getString("SCHREGNO");
                final String grade = rsNurs.getString("GRADE");
                final String hrClass = rsNurs.getString("HR_CLASS");
                final String hrName = rsNurs.getString("HR_NAME");
                final String majorName = rsNurs.getString("MAJORNAME");
                final String attendno = rsNurs.getString("ATTENDNO");
                final String name = rsNurs.getString("NAME");
                final String sex = rsNurs.getString("SEX");
                final String visitDate = rsNurs.getString("VISIT_DATE");
                final String visitHour = rsNurs.getString("VISIT_HOUR");
                final String visitMinute = rsNurs.getString("VISIT_MINUTE");
                final String type = rsNurs.getString("TYPE");
                final String visitReason1 = rsNurs.getString("VISIT_REASON1");
                final String treatment1 = rsNurs.getString("TREATMENT1");
                final String specialNote = StringUtils.defaultString(rsNurs.getString("SPECIAL_NOTE"));
                final String leaveHour = rsNurs.getString("LEAVE_HOUR");
                final String leaveMinute = rsNurs.getString("LEAVE_MINUTE");
                final NurseoffVisitrecDat nurseoffVisitrecDat = new NurseoffVisitrecDat(db2,
                                                                                        visitDate,
                                                                                        visitHour,
                                                                                        visitMinute,
                                                                                        type,
                                                                                        visitReason1,
                                                                                        treatment1,
                                                                                        specialNote,
                                                                                        leaveHour,
                                                                                        leaveMinute);

                Student student = null;
                for (final Iterator it = retList.iterator(); it.hasNext();) {
                    final Student s = (Student) it.next();
                    if (null != s._schregno && s._schregno.equals(schregno)) {
                        student = s;
                        break;
                    }
                }

                if (null == student) {
                    student = new Student(schregno, grade, hrClass, hrName, majorName, attendno, name, sex);
                    retList.add(student);
                }

                if (null == student._lastType || _param._isPageChangeType && !student._lastType.equals(type)) {
                    student._nurseoffVisitrecDatList.add(new ArrayList());
                }
                final List nurseoffVisitrecDatList = (List) student._nurseoffVisitrecDatList.get(student._nurseoffVisitrecDatList.size() - 1);
                nurseoffVisitrecDatList.add(nurseoffVisitrecDat);

                student._lastType = type;
            }
        } catch (Exception e) {
            log.error(e);
        } finally {
            DbUtils.closeQuietly(null, psNurs, rsNurs);
            db2.commit();
        }
        return retList;
    }

    private String getNurseOffSql() {
        String sep = "";
        StringBuffer typeIn = new StringBuffer();
        typeIn.append("(");
        if (_param._printNaika) {
            typeIn.append(sep + "'1'");
            sep = ",";
        }
        if (_param._printGeka) {
            typeIn.append(sep + "'2'");
            sep = ",";
        }
        if (_param._printSonota) {
            typeIn.append(sep + "'3'");
            sep = ",";
        }
        if (_param._printSeitoIgai) {
            typeIn.append(sep + "'4'");
            sep = ",";
        }
        if (_param._printKenkoSodanKatsudo) {
            typeIn.append(sep + "'5'");
            sep = ",";
        }
        typeIn.append(")");
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     L3.NAME2 AS SEX, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     L5.MAJORNAME, ");
        stb.append("     I1.VISIT_DATE, ");
        stb.append("     I1.VISIT_HOUR, ");
        stb.append("     I1.VISIT_MINUTE, ");
        stb.append("     I1.TYPE, ");
        stb.append("     I1.VISIT_REASON1, ");
        stb.append("     I1.TREATMENT1, ");
        stb.append("     I1.SPECIAL_NOTE, ");
        stb.append("     I1.LEAVE_HOUR, ");
        stb.append("     I1.LEAVE_MINUTE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN NURSEOFF_VISITREC_DAT I1 ON T1.SCHREGNO = I1.SCHREGNO ");
        stb.append("           AND I1.VISIT_DATE BETWEEN '" + _param._sDate + "' AND '" + _param._eDate + "' ");
        stb.append("           AND I1.TYPE IN " + typeIn.toString() + " ");
        stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'F206' ");
        stb.append("          AND I1.OCCUR_PLACE = L1.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'F212' ");
        stb.append("          AND I1.RESTTIME = L2.NAMECD2 ");
        stb.append("     INNER JOIN V_SEMESTER_GRADE_MST SEME ON SEME.YEAR = T1.YEAR ");
        stb.append("          AND SEME.SEMESTER = T1.SEMESTER  ");
        stb.append("          AND SEME.GRADE = T1.GRADE  ");
        stb.append("          AND SEME.SEMESTER <> '9' ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON T1.YEAR = REGDH.YEAR ");
        stb.append("          AND T1.SEMESTER = REGDH.SEMESTER ");
        stb.append("          AND T1.GRADE = REGDH.GRADE ");
        stb.append("          AND T1.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.SEX = L3.NAMECD2 ");
        stb.append("     LEFT JOIN COURSE_MST L4 ON L4.COURSECD = T1.COURSECD ");
        stb.append("     LEFT JOIN MAJOR_MST L5 ON L5.COURSECD = T1.COURSECD ");
        stb.append("          AND L5.MAJORCD = T1.MAJORCD ");
        stb.append("     WHERE ");
        stb.append("          T1.YEAR = '" + _param._ctrlYear +"' ");
        if ("2".equals(_param._output)) {
            stb.append("          AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        } else {
            stb.append("          AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        }
        stb.append("          AND ( (I1.VISIT_DATE BETWEEN SEME.SDATE AND SEME.EDATE) ");
        // 来室日付がMIN学期開始日より前
        stb.append("                OR (SEME.SEMESTER = (SELECT MIN(SEMESTER) FROM V_SEMESTER_GRADE_MST WHERE YEAR = T1.YEAR AND SEMESTER <> '9' AND GRADE = T1.GRADE)");
        stb.append("                    AND I1.VISIT_DATE < SEME.SDATE) ");
        // 来室日付が直近の前学期終了日より後
        stb.append("                OR (SEME.SEMESTER = (SELECT SEMESTER      FROM V_SEMESTER_GRADE_MST WHERE YEAR = T1.YEAR AND SEMESTER <> '9' AND GRADE = T1.GRADE ");
        stb.append("                                      AND I1.VISIT_DATE - EDATE = (SELECT MIN(I1.VISIT_DATE - TT.EDATE) FROM V_SEMESTER_GRADE_MST TT WHERE TT.YEAR = T1.YEAR AND GRADE = T1.GRADE AND I1.VISIT_DATE > TT.EDATE)) ");
        stb.append("                    AND NOT EXISTS (SELECT SEMESTER FROM V_SEMESTER_GRADE_MST WHERE SEMESTER <> '9' AND GRADE = T1.GRADE AND I1.VISIT_DATE BETWEEN SDATE AND EDATE))");
        stb.append("              ) ");
        stb.append(" ORDER BY ");
        stb.append("          T1.GRADE, ");
        stb.append("          T1.HR_CLASS, ");
        stb.append("          T1.ATTENDNO, ");
        if (_param._isPageChangeType) {
            stb.append("          I1.TYPE, ");
        }
        stb.append("          I1.VISIT_DATE");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    public class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _majorName;
        final String _attendno;
        final String _name;
        final String _sex;
        final List _nurseoffVisitrecDatList;
        String _lastType;

        Student(final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String majorName,
                final String attendno,
                final String name,
                final String sex
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _majorName = majorName;
            _attendno = attendno;
            _name = name;
            _sex = sex;
            _nurseoffVisitrecDatList = new ArrayList();
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _schregno + ":" + _name;
        }
    }

    private class NurseoffVisitrecDat {

        final String _visitDate;
        final String _visitHour;
        final String _visitMinute;
        final String _type;
        final String _typeName;
        final String _visitReason1;
        final String _visitReasonName1;
        final String _treatment1;
        final String _treatmentName1;
        final String _specialNote;
        final String _leaveHour;
        final String _leaveMinute;

        /**
         * コンストラクタ。
         */
        public NurseoffVisitrecDat(
                final DB2UDB db2,
                final String visitDate,
                final String visitHour,
                final String visitMinute,
                final String type,
                final String visitReason1,
                final String treatment1,
                final String specialNote,
                final String leaveHour,
                final String leaveMinute
        ) throws SQLException {
            _visitDate = visitDate;
            _visitHour = visitHour;
            _visitMinute = visitMinute;
            _type = type;
            _typeName = getTypeName();
            _visitReason1 = visitReason1;
            final String visit = getVisitReasonNameCd1(_type);
            _visitReasonName1 = getNameMst(db2, visit, _visitReason1);
            _treatment1 = treatment1;
            final String treat = getTreatNameCd1(_type);
            _treatmentName1 = getNameMst(db2, treat, _treatment1);
            _specialNote = specialNote;
            _leaveHour = leaveHour;
            _leaveMinute = leaveMinute;
        }

        private String getVisitReasonNameCd1(final String type) {
            if (NAIKA.equals(type)) return "F200";
            else if (GEKA.equals(type)) return "F201";
            else if (SONOTA.equals(type)) return "F203";
            else if (SEITO_IGAI.equals(type)) return "F202";
            else return "F219";
        }

        private String getTreatNameCd1(final String type) {
            if (NAIKA.equals(type)) return "F208";
            else if (GEKA.equals(type)) return "F209";
            else if (SONOTA.equals(type)) return "F210";
            else if (SEITO_IGAI.equals(type)) return "F210";
            else return "F220";
        }

        private String getTypeName() {
            if (NAIKA.equals(_type)) {
                return NAIKA_NAME;
            } else if (GEKA.equals(_type)) {
                return GEKA_NAME;
            } else if (SONOTA.equals(_type)) {
                return SONOTA_NAME;
            } else if (SEITO_IGAI.equals(_type)) {
                return SEITO_IGAI_NAME;
            } else  if (KENKO_SODAN_KATSUDO.equals(_type)) {
                return KENKO_SODAN_KATSUDO_NAME;
            } else {
                return "";
            }
        }

        private String getNameMst(
                final DB2UDB db2,
                final String nameCd1,
                final String nameCd2
        ) throws SQLException {
            String retVal = "";
            final String sql = "SELECT VALUE(NAME1, '') AS NAME1 FROM NAME_MST WHERE NAMECD1 = '" + nameCd1 + "' AND NAMECD2 = '" + nameCd2 + "'";
            ResultSet rs = null;
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retVal = rs.getString("NAME1");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retVal;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 67515 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final boolean _printNaika;
        final boolean _printGeka;
        final boolean _printSonota;
        final boolean _printSeitoIgai;
        final boolean _printKenkoSodanKatsudo;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _sDate;
        final String _eDate;
        final String _prgid;
        final boolean _isPageChangeType;
        final String[] _categorySelected;
        final String _output;
        final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _printNaika = null != request.getParameter("NAIKA") ? true : false;
            _printGeka = null != request.getParameter("GEKA") ? true : false;
            _printSonota = null != request.getParameter("SONOTA") ? true : false;
            _printSeitoIgai = null != request.getParameter("SEITO_IGAI") ? true : false;
            _printKenkoSodanKatsudo = null != request.getParameter("KENKO_SODAN") ? true : false;
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _sDate = request.getParameter("DATE1").replace('/', '-');
            _eDate = request.getParameter("DATE2").replace('/', '-');
            _prgid = request.getParameter("PRGID");
            _isPageChangeType = null != request.getParameter("CHECK1") ? true : false;

            _output = request.getParameter("OUTPUT");
            _categorySelected = request.getParameterValues("category_selected");
            _schoolName = getNameMst(db2, "NAME1", "Z010", "00");
        }

        public String changePrintDateGengou(final DB2UDB db2, final String date) {
            final String youbi = KNJ_EditDate.h_format_W(date) != null ? "（" + KNJ_EditDate.h_format_W(date) + "）" : "";
            return KNJ_EditDate.h_format_JP(db2, date) + youbi;
        }

        public String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof
