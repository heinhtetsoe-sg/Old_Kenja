/*
 * $Id: 2920115d92db25804c9c44063b76b0b8836806e8 $
 *
 * 作成日: 2016/08/17
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJF323 {

    private static final Log log = LogFactory.getLog(KNJF323.class);

    private boolean _hasData;
    private boolean _hasDataPage3;

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

    private static int getMS932ByteLength(final String s) {
    	return KNJ_EditEdit.getMS932ByteLength(s);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        if (_param._isAllergy) {
            printAllergy(db2, svf);
        } else {
            printAll(db2, svf);
        }
    }

    private void printAll(final DB2UDB db2, final Vrw32alp svf) {

        svf.VrSetForm("KNJF323_1.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            boolean changeFrm = false;
            setHeadData(svf);
            while (rs.next()) {
                final String careDiv = rs.getString("CARE_DIV");
                final String careFlg = rs.getString("CARE_FLG");
                final String emeName1 = rs.getString("EMERGENCYNAME");
                final String tel1 = rs.getString("EMERGENCYTELNO");
                final String emeName2 = rs.getString("EMERGENCYNAME2");
                final String tel2 = rs.getString("EMERGENCYTELNO2");
                final String date = rs.getString("DATE");
                final String doctor = rs.getString("DOCTOR");
                final String hospital = rs.getString("HOSPITAL");
                final String remark = rs.getString("REMARK");
                final String careKind = rs.getString("CARE_KIND");
                final String careItem = rs.getString("CARE_ITEM");
                final String careSeq = rs.getString("CARE_SEQ");
                final String careRemark1 = rs.getString("CARE_REMARK1");
                final String careRemark2 = rs.getString("CARE_REMARK2");
                //フォーム変更
                if (!changeFrm && Integer.parseInt(careDiv) >= 4) {
                    svf.VrEndPage();
                    svf.VrSetForm("KNJF323_2.frm", 1);
                    setHeadData(svf);
                    changeFrm = true;
                }

                //HDATの出力
                String setFieldName = "";
                if ("01".equals(careDiv)) {
                    setFieldName = "ASTHMA_";
                } else if ("02".equals(careDiv)) {
                    setFieldName = "ATOPIC_DER_";
                } else if ("03".equals(careDiv)) {
                    setFieldName = "ALLERGY_CON_";
                } else if ("04".equals(careDiv)) {
                    setFieldName = "ALLERGY_FOOD_";
                } else if ("05".equals(careDiv)) {
                    setFieldName = "ANAPHYLAXIS_";
                } else if ("06".equals(careDiv)) {
                    setFieldName = "ALLERGY_RHIN_";
                }
                svf.VrsOut(setFieldName + careDiv + "_" + careFlg, "○");
                svf.VrsOut("GURD_TELNO_" + careDiv, tel1);
                svf.VrsOut("GUARD_NAME_" + careDiv, emeName1);
                svf.VrsOut("MED_AGENCY_NAME_" + careDiv, emeName2);
                svf.VrsOut("MED_AGENCY_TELNO_" + careDiv, tel2);
                svf.VrsOut("MEMTION_DATE_" + careDiv, KNJ_EditDate.getAutoFormatDate(db2, date));
                svf.VrsOut("DOCTOR_NAME_" + careDiv, doctor);
                svf.VrsOut("MED_AGENCY_" + careDiv, hospital);

                if ("00".equals(careSeq)) {
                    int f_len = 0;
                    int f_cnt = 0;
                    if ("01010500".equals(careDiv + careKind + careItem + careSeq)) {
                        f_len = 40;
                        f_cnt = 15;
                    } else if ("01020400".equals(careDiv + careKind + careItem + careSeq)) {
                        f_len = 80;
                        f_cnt = 7;
                    } else if ("02020400".equals(careDiv + careKind + careItem + careSeq)) {
                        f_len = 40;
                        f_cnt = 7;
                    } else if ("03020300".equals(careDiv + careKind + careItem + careSeq)) {
                        f_len = 80;
                        f_cnt = 2;
                    } else if ("04020500".equals(careDiv + careKind + careItem + careSeq)) {
                        f_len = 50;
                        f_cnt = 7;
                    } else if ("06020200".equals(careDiv + careKind + careItem + careSeq)) {
                        f_len = 50;
                        f_cnt = 7;
                    }
                    final String[] remarkArray = KNJ_EditEdit.get_token(careRemark1, f_len, f_cnt);
                    if (null != remarkArray) {
                        for (int i = 0; i < remarkArray.length; i++) {
                            svf.VrsOutn(setFieldName + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq, i + 1, remarkArray[i]);
                        }
                    }
                } else {
                    svf.VrsOut(setFieldName + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq, "○");
                    svf.VrsOut("ALLERGY_FOOD_" + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq, "○");
                    svf.VrsOut("ALLERGY_FOOD_" + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq + "_01", careRemark1);
                    if (Integer.parseInt(careDiv) <= 4) {
                        if ("02010204".equals(careDiv + careKind + careItem + careSeq)) {
                            final String nameField = getMS932ByteLength(careRemark1) > 20 ? "_1" : "";
                            svf.VrsOut(setFieldName + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq + "_REM" + nameField, careRemark1);
                        } else {
                            int f_len = 0;
                            int f_cnt = 0;
                            if ("02010302".equals(careDiv + careKind + careItem + careSeq)) {
                                f_len = 20;
                                f_cnt = 4;
                            } else if ("02020203".equals(careDiv + careKind + careItem + careSeq)) {
                                f_len = 20;
                                f_cnt = 4;
                            }
                            if (f_len == 0) {
                                svf.VrsOut(setFieldName + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq + "_REM", careRemark1);
                            } else {
                                final String[] remarkArray = KNJ_EditEdit.get_token(careRemark1, f_len, f_cnt);
                                if (null != remarkArray) {
                                    for (int i = 0; i < remarkArray.length; i++) {
                                        svf.VrsOutn(setFieldName + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq + "_REM", i + 1, remarkArray[i]);
                                    }
                                }
                            }
                        }
                    }
                    if ("06010102".equals(careDiv + careKind + careItem + careSeq) && null != careRemark1) {
                        final String[] siki = StringUtils.split(careRemark1, ',');
                        for (int sikiCnt = 0; sikiCnt < siki.length; sikiCnt++) {
                            final String setFieldSoeji = "春".equals(siki[sikiCnt]) ? "1" : "夏".equals(siki[sikiCnt]) ? "2" : "秋".equals(siki[sikiCnt]) ? "3" : "4";
                            svf.VrsOut(setFieldName + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq + "_" + setFieldSoeji, "○");
                        }
                    }
                    if ("06".equals(careDiv)) {
                        svf.VrsOut(setFieldName + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq + "_REM", careRemark1);
                    } else if (null != careRemark2) {
                        svf.VrsOut("ALLERGY_FOOD_" + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq + "_REM", careRemark2);
                    }
                }

                _hasData = true;
            }

            if (_hasData) {
                svf.VrEndPage();
                setPage3(db2, svf);
                if (_hasDataPage3) {
                    svf.VrEndPage();
                } else {
                    svf.VrSetForm("KNJF323_4.frm", 1);
                    setHeadDataPage3(svf);
                    svf.VrEndPage();
                }
            } else {
                svf.VrSetForm("KNJF323_1.frm", 1);
                setHeadData(svf);
                svf.VrEndPage();
                svf.VrSetForm("KNJF323_2.frm", 1);
                setHeadData(svf);
                svf.VrEndPage();
                setPage3(db2, svf);
                if (_hasDataPage3) {
                    svf.VrEndPage();
                } else {
                    svf.VrSetForm("KNJF323_4.frm", 1);
                    setHeadDataPage3(svf);
                    svf.VrEndPage();
                }
                _hasData = true;
            }
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void setPage3(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm("KNJF323_4.frm", 1);
        setHeadDataPage3(svf);
        final String sqlP3 = sqlPage3();
        log.debug(" sql =" + sqlP3);
        PreparedStatement psPage3 = null;
        ResultSet rsPage3 = null;
        psPage3 = db2.prepareStatement(sqlP3);
        rsPage3 = psPage3.executeQuery();

        String setClubName = "";
        String sep = "";
        while (rsPage3.next()) {
            final String diagnosisName = rsPage3.getString("DIAGNOSIS_NAME");
            final String guideDiv = rsPage3.getString("GUIDE_DIV");
            final String clubName = rsPage3.getString("CLUBNAME");

            final String diagField = KNJ_EditEdit.getMS932ByteLength(diagnosisName) > 70 ? "2": "1";
            svf.VrsOut("DIAG" + diagField, diagnosisName);
            String setGdivField = "";
            if ("A".equals(guideDiv)) {
                setGdivField = "1";
            } else if ("B".equals(guideDiv)) {
                setGdivField = "2";
            } else if ("C".equals(guideDiv)) {
                setGdivField = "3";
            } else if ("D".equals(guideDiv)) {
                setGdivField = "4";
            } else if ("E".equals(guideDiv)) {
                setGdivField = "5";
            }
            svf.VrsOut("LEAD" + setGdivField, "○");
            setClubName += sep + clubName;
            final String clubNameField = KNJ_EditEdit.getMS932ByteLength(setClubName) > 10 ? "2": "1";
            svf.VrsOut("CLUB_NAME" + clubNameField, setClubName);
            if ("2".equals(clubNameField)) {
                svf.VrsOut("CLUB_NAME1", "");
            }

            sep = ",";
            _hasDataPage3 = true;
        }
    }

    private void printAllergy(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJF323_3.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            setHeadData(svf);
            while (rs.next()) {
                final String careDiv = rs.getString("CARE_DIV");
                final String careFlg = rs.getString("CARE_FLG");
                final String emeName1 = rs.getString("EMERGENCYNAME");
                final String tel1 = rs.getString("EMERGENCYTELNO");
                final String emeName2 = rs.getString("EMERGENCYNAME2");
                final String tel2 = rs.getString("EMERGENCYTELNO2");
                final String date = rs.getString("DATE");
                final String doctor = rs.getString("DOCTOR");
                final String hospital = rs.getString("HOSPITAL");
                final String remark = rs.getString("REMARK");
                final String careKind = rs.getString("CARE_KIND");
                final String careItem = rs.getString("CARE_ITEM");
                final String careSeq = rs.getString("CARE_SEQ");
                final String careRemark1 = rs.getString("CARE_REMARK1");
                final String careRemark2 = rs.getString("CARE_REMARK2");

                //HDATの出力
                String setFieldName = "";
                if ("04".equals(careDiv)) {
                    setFieldName = "ALLERGY_FOOD_";
                } else if ("05".equals(careDiv)) {
                    setFieldName = "ANAPHYLAXIS_";
                }
                svf.VrsOut(setFieldName + careDiv + "_" + careFlg, "○");
                svf.VrsOut("GURD_TELNO_" + careDiv, tel1);
                svf.VrsOut("GUARD_NAME_" + careDiv, emeName1);
                svf.VrsOut("MED_AGENCY_NAME_" + careDiv, emeName2);
                svf.VrsOut("MED_AGENCY_TELNO_" + careDiv, tel2);
                svf.VrsOut("MEMTION_DATE_" + careDiv, KNJ_EditDate.getAutoFormatDate(db2, date));
                svf.VrsOut("DOCTOR_NAME_" + careDiv, doctor);
                svf.VrsOut("MED_AGENCY_" + careDiv, hospital);
                final String[] emergencyArray = KNJ_EditEdit.get_token(remark, 80, 17);
                if (null != emergencyArray) {
                    for (int i = 0; i < emergencyArray.length; i++) {
                        svf.VrsOutn("EMERGENCY_PLAN", i + 1, emergencyArray[i]);
                    }
                }

                if ("00".equals(careSeq)) {
                    int f_len = 0;
                    int f_cnt = 0;
                    if ("04020500".equals(careDiv + careKind + careItem + careSeq)) {
                        f_len = 50;
                        f_cnt = 7;
                    } else if ("04020600".equals(careDiv + careKind + careItem + careSeq)) {
                        f_len = 80;
                        f_cnt = 7;
                    }
                    final String[] remarkArray = KNJ_EditEdit.get_token(careRemark1, f_len, f_cnt);
                    if (null != remarkArray) {
                        for (int i = 0; i < remarkArray.length; i++) {
                            svf.VrsOutn(setFieldName + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq, i + 1, remarkArray[i]);
                        }
                    }
                } else {
                    svf.VrsOut(setFieldName + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq, "レ");
                    svf.VrsOut("ALLERGY_FOOD_" + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq, "レ");
                    svf.VrsOut("ALLERGY_FOOD_" + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq + "_01", careRemark1);
                    if ("040103".equals(careDiv + careKind + careItem)) {
                        final String[] careRemark1Array = StringUtils.split(careRemark1, ',');
                        if (null != careRemark1Array) {
                            for (int remarkCnt = 0; remarkCnt < careRemark1Array.length; remarkCnt++) {
                                svf.VrsOut("ALLERGY_FOOD_" + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq + "_" + careRemark1Array[remarkCnt], "レ");
                            }
                        }
                        svf.VrsOut("ALLERGY_FOOD_" + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq + "_REM", careRemark2);
                    } else if (Integer.parseInt(careDiv) <= 4) {
                        svf.VrsOut(setFieldName + careDiv + "_" + careKind + "_" + careItem + "_" + careSeq + "_REM", careRemark1);
                    }
                    if (null != careRemark1) {
                        int f_len = 0;
                        int f_cnt = 0;
                        if ("04020102".equals(careDiv + careKind + careItem + careSeq)) {
                            f_len = 80;
                            f_cnt = 2;
                        } else if ("04020202".equals(careDiv + careKind + careItem + careSeq)) {
                            f_len = 80;
                            f_cnt = 2;
                        } else if ("04020302".equals(careDiv + careKind + careItem + careSeq)) {
                            f_len = 80;
                            f_cnt = 2;
                        } else if ("04020402".equals(careDiv + careKind + careItem + careSeq)) {
                            f_len = 80;
                            f_cnt = 2;
                        }
                        final String[] remarkArray = KNJ_EditEdit.get_token(careRemark1, f_len, f_cnt);
                        if (null != remarkArray) {
                            for (int i = 0; i < remarkArray.length; i++) {
                                svf.VrsOutn(setFieldName + careDiv + "_" + careKind + "_" + careItem + "_00", i + 1, remarkArray[i]);
                            }
                        }
                    }
                }
                _hasData = true;
            }
            if (_hasData) {
                svf.VrEndPage();
            } else {
                svf.VrSetForm("KNJF323_3.frm", 1);
                setHeadData(svf);
                svf.VrEndPage();
                _hasData = true;
            }
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void setHeadData(final Vrw32alp svf) {
        svf.VrsOut("TITLE", _param._nendo);
        final String nameField = getMS932ByteLength(_param._student._name) > 30 ? "3" : getMS932ByteLength(_param._student._name) > 24 ? "2" : "1";
        if (_param._isAllergy) {
            svf.VrsOut("SCHREGNO", _param._student._schregno);
            svf.VrsOut("NAME", _param._student._name);
            svf.VrsOut("TEACHER_NAME", _param._student._staffName);
        } else {
            svf.VrsOut("NAME" + nameField, _param._student._name);
        }
        svf.VrsOut("SEX" + _param._student._sex, "○");
        svf.VrsOut("BIRTHDAY", _param._student._formatBirthday + "(" + _param._student._age + "歳)");
        svf.VrsOut("SCHOOL_NAME", _param._schoolName + "　" + _param._student._hrName);
        svf.VrsOut("HR_NAME", _param._student._hrName + " " + _param._student._attendno + "番");
        svf.VrsOut("FILING_DATE", "　　  年  月  日");
        svf.VrsOut("DATE", _param._formatCtrlDate);
    }

    private void setHeadDataPage3(final Vrw32alp svf) {
        final String nameField = getMS932ByteLength(_param._student._name) > 30 ? "2": "1";
        svf.VrsOut("NAME" + nameField, _param._student._name);
        final String setSexField = "1".equals(_param._student._sex) ? "MALE": "FEMALE";
        svf.VrsOut(setSexField, "○");
        svf.VrsOut("BIRTHDAY", _param._student._formatBirthday + "(" + _param._student._age + ")才");
        svf.VrsOut("FINSCHOOL_NAME", _param._schoolName);
        svf.VrsOut("HR_NAME", _param._student._hrName);
        svf.VrsOut("DATE", "　　  年  月  日");
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     CARE_H.CARE_DIV, ");
        stb.append("     CARE_H.CARE_FLG, ");
        stb.append("     CARE_H.EMERGENCYNAME, ");
        stb.append("     CARE_H.EMERGENCYTELNO, ");
        stb.append("     CARE_H.EMERGENCYNAME2, ");
        stb.append("     CARE_H.EMERGENCYTELNO2, ");
        stb.append("     CARE_H.DATE, ");
        stb.append("     CARE_H.DOCTOR, ");
        stb.append("     CARE_H.HOSPITAL, ");
        stb.append("     CARE_H.REMARK, ");
        stb.append("     CARE_D.CARE_KIND, ");
        stb.append("     CARE_D.CARE_ITEM, ");
        stb.append("     CARE_D.CARE_SEQ, ");
        stb.append("     CARE_D.CARE_REMARK1, ");
        stb.append("     CARE_D.CARE_REMARK2 ");
        stb.append(" FROM ");
        stb.append("     MEDEXAM_CARE_HDAT CARE_H ");
        stb.append("     LEFT JOIN MEDEXAM_CARE_DAT CARE_D ON CARE_H.YEAR = CARE_D.YEAR ");
        stb.append("          AND CARE_H.SCHREGNO = CARE_D.SCHREGNO ");
        stb.append("          AND CARE_H.CARE_DIV = CARE_D.CARE_DIV ");
        stb.append(" WHERE ");
        stb.append("     CARE_H.YEAR = '" + _param._year + "' ");
        stb.append("     AND CARE_H.SCHREGNO = '" + _param._schregNo + "' ");
        if (_param._isAllergy) {
            stb.append("     AND CARE_H.CARE_DIV IN ('04', '05') ");
        }
        stb.append(" ORDER BY ");
        stb.append("     CARE_DIV, ");
        stb.append("     CARE_KIND, ");
        stb.append("     CARE_ITEM, ");
        stb.append("     CARE_SEQ ");

        return stb.toString();
    }

    private String sqlPage3() {
        final StringBuffer stb = new StringBuffer();
        final int intYear = Integer.parseInt(_param._ctrlYear);
        //final String setYear = ("03".equals(_param._student._gradeCd)) ? String.valueOf(intYear + 1): ("02".equals(_param._student._gradeCd)) ? String.valueOf(intYear + 2): String.valueOf(intYear + 3);
        final int setYear = ("03".equals(_param._student._gradeCd)) ? intYear + 1: ("02".equals(_param._student._gradeCd)) ? intYear + 2: intYear + 3;
        stb.append(" SELECT ");
        stb.append("     VALUE(MEDX.DIAGNOSIS_NAME, '') AS DIAGNOSIS_NAME, ");
        stb.append("     VALUE(F141.ABBV1, '') AS GUIDE_DIV, ");
        stb.append("     VALUE(CLUB.CLUBNAME, '') AS CLUBNAME ");
        stb.append(" FROM ");
        stb.append("     MEDEXAM_DET_DAT MEDX ");
        stb.append("     LEFT JOIN V_NAME_MST F141 ON MEDX.YEAR      = F141.YEAR ");
        stb.append("                              AND F141.NAMECD1   = 'F141' ");
        stb.append("                              AND MEDX.GUIDE_DIV = F141.NAMECD2 ");
        stb.append("     LEFT JOIN SCHREG_CLUB_HIST_DAT HIST ON HIST.SCHREGNO    = MEDX.SCHREGNO ");
        if ("1".equals(_param._useSchool_KindField)) {
            stb.append("                                        AND HIST.SCHOOLCD    = '" + _param._schoolCd + "' ");
            stb.append("                                        AND HIST.SCHOOL_KIND = '" + _param._student._schoolKind + "' ");
        }
        stb.append("     LEFT JOIN CLUB_MST CLUB ON HIST.CLUBCD      = CLUB.CLUBCD ");
        if ("1".equals(_param._useSchool_KindField)) {
            stb.append("                            AND HIST.SCHOOLCD    = CLUB.SCHOOLCD ");
            stb.append("                            AND HIST.SCHOOL_KIND = CLUB.SCHOOL_KIND ");
        }
        stb.append(" WHERE ");
        stb.append("         MEDX.YEAR     = '"+ _param._ctrlYear +"' ");
        stb.append("     AND MEDX.SCHREGNO = '"+ _param._schregNo +"' ");
        stb.append("     AND (HIST.EDATE IS NULL OR HIST.EDATE IN ('9999-12-31', '"+ setYear +"-03-31')) ");
        stb.append(" ORDER BY ");
        stb.append("     HIST.SDATE, ");
        stb.append("     CLUB.CLUBCD ");


        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64315 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
		private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schregNo;
        private final String _useSchool_KindField;
        private final String _schoolCd;
        private final boolean _careFlg01;
        private final boolean _careFlg02;
        private final boolean _careFlg03;
        private final boolean _careFlg04;
        private final boolean _careFlg05;
        private final boolean _careFlg06;
        private final boolean _isAllergy;
        private final Student _student;
        private final String _schoolName;
        private final String _nendo;
        private final String _formatCtrlDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("LOGIN_YEAR");
            _ctrlSemester = request.getParameter("LOGIN_SEMESTER");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _year = null != request.getParameter("YEAR") && "".equals(request.getParameter("YEAR")) ? request.getParameter("YEAR") : _ctrlYear;
            _schregNo = request.getParameter("SCHREGNO");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _schoolCd = request.getParameter("SCHOOLCD");
            _careFlg01 = "1".equals(request.getParameter("CARE_FLG01"));
            _careFlg02 = "1".equals(request.getParameter("CARE_FLG02"));
            _careFlg03 = "1".equals(request.getParameter("CARE_FLG03"));
            _careFlg04 = "1".equals(request.getParameter("CARE_FLG04"));
            _careFlg05 = "1".equals(request.getParameter("CARE_FLG05"));
            _careFlg06 = "1".equals(request.getParameter("CARE_FLG06"));
            _isAllergy = null != request.getParameter("YEAR");
            _student = setStudentInfo(db2);
            _schoolName = getSchoolName(db2);
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";
            _formatCtrlDate = KNJ_EditDate.getAutoFormatDate(db2, _ctrlDate);
        }

        private String getSchoolName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            String schoolSql = "SELECT * FROM SCHOOL_MST WHERE YEAR = '" + _ctrlYear + "'";
            if (!"K".equals(_student._schoolKind)) {
                final String getFieldName = "H".equals(_student._schoolKind) ? "SCHOOL_NAME" : "J".equals(_student._schoolKind) ? "REMARK1" : "REMARK4";
                schoolSql = "SELECT " + getFieldName + " AS SCHOOLNAME1 FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '125' ";
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(schoolSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("SCHOOLNAME1");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private Student setStudentInfo(final DB2UDB db2) throws SQLException {
            Student student = null;
            final String studentSql = getStudentSql();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(studentSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String name = rs.getString("NAME");
                    final String sex = rs.getString("SEX");
                    final String birthday = rs.getString("BIRTHDAY");
                    final String age = rs.getString("AGE");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String grade = rs.getString("GRADE");
                    final String gradeCd = rs.getString("GRADE_CD");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String staffName = rs.getString("STAFFNAME");
                    final String attendno = rs.getString("ATTENDNO");
                    student = new Student(schregno, name, sex, birthday, age, schoolKind, grade, gradeCd, hrClass, hrName, staffName, attendno);
                }
                if (null != student) {
                	student._formatBirthday = KNJ_EditDate.getAutoFormatDate(db2, student._birthday);
                	if (!StringUtils.isBlank(student._formatBirthday)) {
                		student._formatBirthday += "生";
                	}
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return student;
        }

        private String getStudentSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     BASE.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX, ");
            stb.append("     BASE.BIRTHDAY, ");
            stb.append("     CASE WHEN BASE.BIRTHDAY IS NOT NULL THEN YEAR('" + _ctrlDate + "' - BASE.BIRTHDAY) END AS AGE, ");
            stb.append("     GDAT.SCHOOL_KIND, ");
            stb.append("     REGH.GRADE, ");
            stb.append("     GDAT.GRADE_CD, ");
            stb.append("     REGH.HR_CLASS, ");
            stb.append("     REGH.HR_NAME, ");
            stb.append("     STAFF.STAFFNAME, ");
            stb.append("     REGD.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_BASE_MST BASE ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '" + _ctrlYear + "' ");
            stb.append("          AND REGD.SEMESTER = '" + _ctrlSemester + "' ");
            stb.append("          AND REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
            stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
            stb.append("          AND REGD.GRADE = REGH.GRADE ");
            stb.append("          AND REGD.HR_CLASS= REGH.HR_CLASS ");
            stb.append("     LEFT JOIN STAFF_MST STAFF ON REGH.TR_CD1 = STAFF.STAFFCD ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
            stb.append("          AND REGD.GRADE = GDAT.GRADE ");
            stb.append(" WHERE ");
            stb.append("     BASE.SCHREGNO = '" + _schregNo + "' ");

            return stb.toString();
        }

    }
    private class Student {
        final String _schregno;
        final String _name;
        final String _sex;
        final String _birthday;
        final String _age;
        final String _schoolKind;
        final String _grade;
        final String _gradeCd;
        final String _hrClass;
        final String _hrName;
        final String _staffName;
        final String _attendno;
        String _formatBirthday;
        public Student(
                final String schregno,
                final String name,
                final String sex,
                final String birthday,
                final String age,
                final String schoolKind,
                final String grade,
                final String gradeCd,
                final String hrClass,
                final String hrName,
                final String staffName,
                final String attendno
        ) {
            _schregno   = schregno;
            _name       = name;
            _sex        = sex;
            _birthday   = birthday;
            _age        = age;
            _schoolKind = schoolKind;
            _grade      = grade;
            _gradeCd    = gradeCd;
            _hrClass    = hrClass;
            _hrName     = hrName;
            _staffName  = staffName;
            _attendno   = attendno;
        }
    }
}

// eof

