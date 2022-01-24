// kanji=漢字
/*
 * $Id: 0593761a743ab501cecc6c81d7bb7b4af2f42cd2 $
 *
 * 作成日: 2009/10/08 16:46:14 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 0593761a743ab501cecc6c81d7bb7b4af2f42cd2 $
 */
public class KNJF100A {

    private static final Log log = LogFactory.getLog("KNJF100A.class");

    private boolean _hasData;

    public final String FORM_NAME = "KNJF100A.frm";

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

    Param _param;

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printData = getPritnData(db2);
        svf.VrSetForm(FORM_NAME, 4);
        svf.VrsOut("DATE1", KNJ_EditDate.h_format_JP(_param._ctrlDate));
        svf.VrsOut("PERIOD", _param.changePrintDateGengou(_param._sDate) + "〜" + _param.changePrintDateGengou(_param._eDate));
        String befType = "";
        String befClass = "";
        for (final Iterator itPrint = printData.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            final String type = student._nurseoffVisitrecDat._type;
            final String gradeHr = student._grade + student._hrClass;
            if (_param._isPageChangeType && !befType.equals("") && !befType.equals(type)) {
                svf.VrSetForm(FORM_NAME, 4);
                svf.VrsOut("DATE1", KNJ_EditDate.h_format_JP(_param._ctrlDate));
                svf.VrsOut("PERIOD", _param.changePrintDateGengou(_param._sDate) + "〜" + _param.changePrintDateGengou(_param._eDate));
            }
            if (_param._isPageChangeClass && !befClass.equals("") && !befClass.equals(gradeHr)) {
                svf.VrSetForm(FORM_NAME, 4);
                svf.VrsOut("DATE1", KNJ_EditDate.h_format_JP(_param._ctrlDate));
                svf.VrsOut("PERIOD", _param.changePrintDateGengou(_param._sDate) + "〜" + _param.changePrintDateGengou(_param._eDate));
            }
            svf.VrsOut("HR_NAME", student._hrName);
            svf.VrsOut("ATTENDNO", student._attendno);
            svf.VrsOut("NAME", student._name);
            svf.VrsOut("SCHREGNO", student._schregno);
            svf.VrsOut("VISIT_DATE", _param.changePrintDateGengou(student._nurseoffVisitrecDat._visitDate));
            svf.VrsOut("VISIT_HOUR", student._nurseoffVisitrecDat._visitHour);
            svf.VrsOut("VISIT_MINUTE", student._nurseoffVisitrecDat._visitMinute);
            svf.VrsOut("LEAVE_HOUR", student._nurseoffVisitrecDat._leaveHour);
            svf.VrsOut("LEAVE_MINUTE", student._nurseoffVisitrecDat._leaveMinute);
            svf.VrsOut("FIELD1", student._nurseoffVisitrecDat._typeName);
            final String feildNo = student._nurseoffVisitrecDat._visitReasonName1.length() > 7 ? "" : "_1";
            svf.VrsOut("VISIT_REASON1" + feildNo, student._nurseoffVisitrecDat._visitReasonName1);
            svf.VrsOut("OCCUR_DATE", _param.changePrintDateKigou(student._nurseoffVisitrecDat._occurDate));
            svf.VrsOut("OCCUR_HOUR", student._nurseoffVisitrecDat._occurHour);
            svf.VrsOut("OCCUR_MINUTE", student._nurseoffVisitrecDat._occurMinute);
            svf.VrsOut("OCCUR_PLACE", student._nurseoffVisitrecDat._occurPlaceName);
            svf.VrsOut("SLEEPTIME", student._nurseoffVisitrecDat._sleeptime);
            svf.VrsOut("CONDITION1", student._nurseoffVisitrecDat._conditionName1);
            svf.VrsOut("CONDITION2", student._nurseoffVisitrecDat._conditionName2);
            svf.VrsOut("CONDITION3", student._nurseoffVisitrecDat._conditionName4);
            svf.VrsOut("TREATMENT", student._nurseoffVisitrecDat._treatmentName1);
            svf.VrsOut("RESTTIME", student._nurseoffVisitrecDat._resttimeName);
            svf.VrEndRecord();
            befType = type;
            befClass = gradeHr;
            _hasData = true;
        }
    }

    private List getPritnData(DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String nurseOffSql = getNurseOffSql();
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
                final String attendno = rsNurs.getString("ATTENDNO");
                final String name = rsNurs.getString("NAME");
                final String sex = rsNurs.getString("SEX");
                final String visitDate = rsNurs.getString("VISIT_DATE");
                final String visitHour = rsNurs.getString("VISIT_HOUR");
                final String visitMinute = rsNurs.getString("VISIT_MINUTE");
                final String type = rsNurs.getString("TYPE");
                final String visitReason1 = rsNurs.getString("VISIT_REASON1");
                final String visitReason2 = rsNurs.getString("VISIT_REASON2");
                final String visitReason3 = rsNurs.getString("VISIT_REASON3");
                final String occurDate = rsNurs.getString("OCCUR_DATE");
                final String occurHour = rsNurs.getString("OCCUR_HOUR");
                final String occurMinute = rsNurs.getString("OCCUR_MINUTE");
                final String occurPlace = rsNurs.getString("OCCUR_PLACE");
                final String occurPlaceName = rsNurs.getString("OCCUR_PLACE_NAME");
                final String sleeptime = rsNurs.getString("SLEEPTIME");
                final String condition1 = rsNurs.getString("CONDITION1");
                final String condition2 = rsNurs.getString("CONDITION2");
                final String condition4 = rsNurs.getString("CONDITION4");
                final String treatment1 = rsNurs.getString("TREATMENT1");
                final String treatment2 = rsNurs.getString("TREATMENT2");
                final String treatment3 = rsNurs.getString("TREATMENT3");
                final String resttime = rsNurs.getString("RESTTIME");
                final String resttimeName = rsNurs.getString("RESTTIME_NAME");
                final String leaveHour = rsNurs.getString("LEAVE_HOUR");
                final String leaveMinute = rsNurs.getString("LEAVE_MINUTE");
                final NurseoffVisitrecDat nurseoffVisitrecDat = new NurseoffVisitrecDat(db2,
                                                                                        visitDate,
                                                                                        visitHour,
                                                                                        visitMinute,
                                                                                        type,
                                                                                        visitReason1,
                                                                                        visitReason2,
                                                                                        visitReason3,
                                                                                        occurDate,
                                                                                        occurHour,
                                                                                        occurMinute,
                                                                                        occurPlace,
                                                                                        occurPlaceName,
                                                                                        sleeptime,
                                                                                        condition1,
                                                                                        condition2,
                                                                                        condition4,
                                                                                        treatment1,
                                                                                        treatment2,
                                                                                        treatment3,
                                                                                        resttime,
                                                                                        resttimeName,
                                                                                        leaveHour,
                                                                                        leaveMinute);

                final Student student = new Student(schregno, grade, hrClass, hrName, attendno, name, sex, nurseoffVisitrecDat);
                retList.add(student);
            }
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
        stb.append(" WITH SCH_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     L1.NAME, ");
        stb.append("     L3.NAME2 AS SEX, ");
        stb.append("     L2.HR_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT L2 ON T1.YEAR = L2.YEAR ");
        stb.append("          AND T1.SEMESTER = L2.SEMESTER ");
        stb.append("          AND T1.GRADE = L2.GRADE ");
        stb.append("          AND T1.HR_CLASS = L2.HR_CLASS ");
        stb.append("     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'Z002' ");
        stb.append("          AND L1.SEX = L3.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS IN " + _param._classIn + " ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     I1.VISIT_DATE, ");
        stb.append("     I1.VISIT_HOUR, ");
        stb.append("     I1.VISIT_MINUTE, ");
        stb.append("     I1.TYPE, ");
        stb.append("     I1.VISIT_REASON1, ");
        stb.append("     I1.VISIT_REASON2, ");
        stb.append("     I1.VISIT_REASON3, ");
        stb.append("     I1.OCCUR_DATE, ");
        stb.append("     I1.OCCUR_HOUR, ");
        stb.append("     I1.OCCUR_MINUTE, ");
        stb.append("     I1.OCCUR_PLACE, ");
        stb.append("     L1.NAME1 AS OCCUR_PLACE_NAME, ");
        stb.append("     I1.SLEEPTIME, ");
        stb.append("     I1.CONDITION1, ");
        stb.append("     I1.CONDITION2, ");
        stb.append("     I1.CONDITION4, ");
        stb.append("     I1.TREATMENT1, ");
        stb.append("     I1.TREATMENT2, ");
        stb.append("     I1.TREATMENT3, ");
        stb.append("     I1.RESTTIME, ");
        stb.append("     L2.NAME1 AS RESTTIME_NAME, ");
        stb.append("     I1.LEAVE_HOUR, ");
        stb.append("     I1.LEAVE_MINUTE ");
        stb.append(" FROM ");
        stb.append("     SCH_T T1 ");
        stb.append("     INNER JOIN NURSEOFF_VISITREC_DAT I1 ON T1.SCHREGNO = I1.SCHREGNO ");
        stb.append("           AND I1.VISIT_DATE BETWEEN '" + _param._sDate + "' AND '" + _param._eDate + "' ");
        stb.append("           AND I1.TYPE IN " + typeIn.toString() + " ");
        stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'F206' ");
        stb.append("          AND I1.OCCUR_PLACE = L1.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'F212' ");
        stb.append("          AND I1.RESTTIME = L2.NAMECD2 ");
        stb.append(" ORDER BY ");
        stb.append("     " + _param._sortSql + " ");

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
        final String _attendno;
        final String _name;
        final String _sex;
        final NurseoffVisitrecDat _nurseoffVisitrecDat;

        Student(final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String name,
                final String sex,
                final NurseoffVisitrecDat nurseoffVisitrecDat
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendno = attendno;
            _name = name;
            _sex = sex;
            _nurseoffVisitrecDat = nurseoffVisitrecDat;
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
        final String _visitReason2;
        final String _visitReasonName2;
        final String _visitReason3;
        final String _visitReasonName3;
        final String _occurDate;
        final String _occurHour;
        final String _occurMinute;
        final String _occurPlace;
        final String _occurPlaceName;
        final String _sleeptime;
        final String _condition1;
        final String _conditionName1;
        final String _condition2;
        final String _conditionName2;
        final String _condition4;
        final String _conditionName4;
        final String _treatment1;
        final String _treatmentName1;
        final String _treatment2;
        final String _treatmentName2;
        final String _treatment3;
        final String _treatmentName3;
        final String _resttime;
        final String _resttimeName;
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
                final String visitReason2,
                final String visitReason3,
                final String occurDate,
                final String occurHour,
                final String occurMinute,
                final String occurPlace,
                final String occurPlaceName,
                final String sleeptime,
                final String condition1,
                final String condition2,
                final String condition4,
                final String treatment1,
                final String treatment2,
                final String treatment3,
                final String resttime,
                final String resttimeName,
                final String leaveHour,
                final String leaveMinute
        ) throws SQLException {
            _visitDate = visitDate;
            _visitHour = visitHour;
            _visitMinute = visitMinute;
            _type = type;
            _typeName = getTypeName();
            _visitReason1 = visitReason1;
            final String visit = NAIKA.equals(_type) ? "F200" : GEKA.equals(_type) ? "F201" : SONOTA.equals(_type) ? "F202" : "F203";
            _visitReasonName1 = getNameMst(db2, visit, _visitReason1);
            _visitReason2 = visitReason2;
            _visitReasonName2 = getNameMst(db2, visit, _visitReason2);
            _visitReason3 = visitReason3;
            _visitReasonName3 = getNameMst(db2, visit, _visitReason3);
            _occurDate = occurDate;
            _occurHour = occurHour;
            _occurMinute = occurMinute;
            _occurPlace = occurPlace;
            _occurPlaceName = occurPlaceName;
            _sleeptime = sleeptime;
            _condition1 = condition1;
            _conditionName1 = null != _condition1 && _condition1.equals("1") ? "はい" : "いいえ";
            _condition2 = condition2;
            _conditionName2 = null != _condition2 && _condition2.equals("1") ? "いつもよく眠れる" : "余り眠れない";
            _condition4 = condition4;
            _conditionName4 = null != _condition4 && _condition4.equals("1") ? "食べた" : null != _condition4 && _condition4.equals("2") ? "食べていない" : "いつも食べない";
            _treatment1 = treatment1;
            final String treat = NAIKA.equals(_type) ? "F208" : GEKA.equals(_type) ? "F209" : "F210";
            _treatmentName1 = getNameMst(db2, treat, _treatment1);
            _treatment2 = treatment2;
            _treatmentName2 = getNameMst(db2, treat, _treatment2);
            _treatment3 = treatment3;
            _treatmentName3 = getNameMst(db2, treat, _treatment3);
            _resttime = resttime;
            _resttimeName = resttimeName;
            _leaveHour = leaveHour;
            _leaveMinute = leaveMinute;
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
            } else if (KENKO_SODAN_KATSUDO.equals(_type)) {
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
        log.fatal("$Revision: 56595 $");
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
        final String _semester;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _sDate;
        final String _eDate;
        final String _prgid;
        final boolean _isPageChangeType;
        final boolean _isPageChangeClass;
        final String[] _classSelected;
        final String _classIn;
        final String[] _sortSelected;
        final String _sortSql;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _printNaika = null != request.getParameter("NAIKA") ? true : false;
            _printGeka = null != request.getParameter("GEKA") ? true : false;
            _printSonota = null != request.getParameter("SONOTA") ? true : false;
            _printSeitoIgai = null != request.getParameter("SEITO_IGAI") ? true : false;
            _printKenkoSodanKatsudo = null != request.getParameter("KENKO_SODAN") ? true : false;
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _sDate = request.getParameter("DATE1").replace('/', '-');
            _eDate = request.getParameter("DATE2").replace('/', '-');
            _prgid = request.getParameter("PRGID");
            _isPageChangeType = null != request.getParameter("CHECK1") ? true : false;
            _isPageChangeClass = null != request.getParameter("CHECK2") ? true : false;
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            String sep = "";
            final StringBuffer stbClass = new StringBuffer();
            stbClass.append("(");
            for (int i = 0; i < _classSelected.length; i++) {
                stbClass.append(sep + "'" + _classSelected[i] + "'");
                sep = ",";
            }
            stbClass.append(")");
            _classIn = stbClass.toString();

            _sortSelected = request.getParameterValues("SORT_SELECTED");
            sep = "";
            final StringBuffer stbSort = new StringBuffer();
            if (null != _sortSelected) {
                for (int i = 0; i < _sortSelected.length; i++) {
                    String sortName = "T1.GRADE, T1.HR_CLASS, T1.ATTENDNO";
                    if (_sortSelected[i].equals("SCHREGNO")) {
                        sortName = "T1." + _sortSelected[i];
                    } else if (_sortSelected[i].equals("TYPE")) {
                        sortName = "(CASE WHEN I1." + _sortSelected[i] + " = '5' THEN 3 WHEN I1." + _sortSelected[i] + " >= '3' THEN INT(I1." + _sortSelected[i] + ") + 1 ELSE INT(I1." + _sortSelected[i] + ") END)";
                    } else if (!_sortSelected[i].equals("NEN_KUMI_BAN")) {
                        sortName = "I1." + _sortSelected[i];
                    }
                    stbSort.append(sep + sortName);
                    sep = ",";
                }
            } else {
                stbSort.append("T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, I1.VISIT_DATE");
            }
            _sortSql = stbSort.toString();
        }

        public String changePrintDateGengou(final String date) {
            final String youbi = KNJ_EditDate.h_format_W(date) != null ? "（" + KNJ_EditDate.h_format_W(date) + "）" : "";
            return KNJ_EditDate.h_format_JP(date) + youbi;
        }

        public String changePrintDateKigou(final String date) {
            if (null != date) {
                final String jpFormat = KNJ_EditDate.h_format_JP(date);
                final String[] formatArray = KNJ_EditDate.tate_format(jpFormat);
                String gengou = "H";
                if (formatArray[0].equals("大正")) {
                    gengou = "T";
                } else if (formatArray[0].equals("昭和")) {
                    gengou = "S";
                }
                return gengou + formatArray[1] + "." + formatArray[2] + "." + formatArray[3];
            } else {
                return "";
            }
        }
    }
}

// eof
