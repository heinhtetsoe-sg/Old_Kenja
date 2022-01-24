/*
 * $Id: 16d869e6a56232f4850bde7167f34fccb2b41c27 $
 *
 * 作成日: 2013/05/07
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * KNJF305P インフルエンザ等による臨時休業報告書（速報）
 * @version $Id: 16d869e6a56232f4850bde7167f34fccb2b41c27 $
 */
public class KNJF305P {

    private static final Log log = LogFactory.getLog(KNJF305P.class);

    private static final String PRGID_KNJF305 = "KNJF305";
    private static final String PRGID_KNJF305P = "KNJF305P";
    
    private static final String HEISA_GAKUNEN = "2";
    private static final String HEISA_GAKKYU = "1";
    private static final String HEISA_GAKKO = "3";
    
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

            printMain2(db2, svf);
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

    private int getMS932ByteLength(final String str) {
        int len = 0;
        if (null != str) {
            try {
                len = str.getBytes("MS932").length;
            } catch (final Exception e) {
                log.debug("exception!", e);
            }
        }
        return len;
    }

    private void printMain2(final DB2UDB db2, final Vrw32alp svf) {
		final List list = getMedexamDiseaseAddition4List(db2);
		if (list.isEmpty()) {
		    return;
		}
        
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final MedexamDiseaseAddition4 mda4 = (MedexamDiseaseAddition4) it.next();
            if (PRGID_KNJF305P.equals(_param._prgid)) {
                if (!"1".equals(mda4._c900namespare3)) {
                    continue;
                }
                if ("99".equals(mda4._grade) && "999".equals(mda4._hrClass)) {
                    // 学校閉鎖
                    if (!HEISA_GAKKO.equals(_param._outDiv)) continue;
                } else if (!"99".equals(mda4._grade) && "999".equals(mda4._hrClass)) {
                    // 学年閉鎖
                    if (!HEISA_GAKUNEN.equals(_param._outDiv)) continue;
                } else if (!"99".equals(mda4._grade) && !"999".equals(mda4._hrClass)) {
                    // 学級閉鎖
                    if (!"1".equals(_param._outDiv)) continue;
                }
            } 
            
            svf.VrSetForm("KNJF305P.frm", 1);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 日付
            svf.VrsOut("SCHOOL_NAME", _param._knjSchoolMst._schoolName1); // 学校名
            if ("1".equals(_param._useAddrField2) && getMS932ByteLength(_param._knjSchoolMst._schoolAddr1) > 60) {
                final String[] token = KNJ_EditEdit.get_token(_param._knjSchoolMst._schoolAddr1, 60, 2);
                if (null != token) {
                    for (int i = 0; i < token.length; i++) {
                        svf.VrsOut("SCHOOL_ADDRESS2_" + String.valueOf(i + 1), token[i]); // 学校名
                    }
                }
            } else {
                svf.VrsOut("SCHOOL_ADDRESS", _param._knjSchoolMst._schoolAddr1); // 学校名
            }
            svf.VrsOut("PRINCIPAL_NAME", _param._principalName); // 校長名
            svf.VrsOut("SCHOOL_NUM", _param._regdTotalCount); // 全校在籍数

            if ("99".equals(mda4._grade) && "999".equals(mda4._hrClass)) {
                // 学校閉鎖
                svf.VrsOut("TEMP_DIV1", "○"); // 臨時休業区分
            } else if (!"99".equals(mda4._grade) && "999".equals(mda4._hrClass)) {
                // 学年閉鎖
                svf.VrsOut("TEMP_DIV2", "○"); // 臨時休業区分
                svf.VrsOut("TEMP_GRADE1_1", mda4._gradeName1); // 対象学年
                svf.VrsOut("TEMP_NUM1", mda4._count); // 対象人数
                svf.VrsOut("TEMP_GRADE1_2", mda4._gradeName1); // 対象学年
                svf.VrsOut("TEMP_CLASS1", mda4._hrCount); // 対象学級
            } else if (!"99".equals(mda4._grade) && !"999".equals(mda4._hrClass)) {
                // 学級閉鎖
                svf.VrsOut("TEMP_DIV3", "○"); // 臨時休業区分
                svf.VrsOut("TEMP_GRADE2_1", mda4._hrName);
                svf.VrsOut("TEMP_NUM2", mda4._count); // 対象人数
                svf.VrsOut("TEMP_GRADE2_2", mda4._gradeName1); // 対象学年
                svf.VrsOut("TEMP_CLASS2", mda4._hrCount); // 対象学級
            }
            svf.VrsOut("TODAY_NUM1", plus(mda4._patientCount, mda4._absenceCount)); // 欠席者数
            svf.VrsOut("TODAY_NUM2", mda4._patientCount); // 欠席者数インフルエンザ
            svf.VrsOut("TODAY_NUM3", mda4._presenceCount); // 登校かせ症状人数
            
            if ("1".equals(mda4._symptom01)) {
                svf.VrsOut("DISEASE1", "○"); // 主な症状
            }
            if ("1".equals(mda4._symptom08)) {
                svf.VrsOut("DISEASE2", "○"); // 主な症状
            }
            if ("1".equals(mda4._symptom12)) {
                svf.VrsOut("DISEASE_ANOTHER", mda4._symptom12Remark); // 主な症状その他
            }
            svf.VrsOut("PERIOD1", formatDate(mda4._actionSDate) + " 〜 " + formatDate(mda4._actionEDate) + "　( " + calcTerm(mda4._actionSDate, mda4._actionEDate) + " 日間)"); // 臨時休業時間
            // svf.VrsOut("PERIOD2", ""); // 臨時休業時間
            // svf.VrsOut("TEMP_CLOSE1", ""); // 臨時休業期間
            // svf.VrsOut("TEMP_CLOSE2", ""); // 臨時休業期間
            
            final String[] token = KNJ_EditEdit.get_token(mda4._remark, 66, 3);
            if (null != token) {
                for (int i = 0; i < token.length; i++) {
                    svf.VrsOut("REMARK" + (i + 1), token[i]); // 備考
                }
            }
            
            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    private String formatDate(final String date) {
        if (null == date) {
            return "      　 ";
        }
        String formatDate = null;
        try {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(java.sql.Date.valueOf(date));
            final String dayOfWeek = String.valueOf("×日月火水木金土".charAt(cal.get(Calendar.DAY_OF_WEEK)));
            formatDate = (1 + cal.get(Calendar.MONTH)) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "(" + dayOfWeek + ")";
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return formatDate;
    }
    
    
    private String calcTerm(final String date1, final String date2) {
        if (null == date1 || null == date2) {
            return "   ";
        }
        String formatDate = null;
        try {
            final Calendar cal1 = Calendar.getInstance();
            cal1.setTime(java.sql.Date.valueOf(date1));
            final int year1 = cal1.get(Calendar.YEAR);
            
            final Calendar cal2 = Calendar.getInstance();
            cal2.setTime(java.sql.Date.valueOf(date2));
            final int year2 = cal2.get(Calendar.YEAR);
            
            if (year2 == year1) {
                formatDate = String.valueOf(cal2.get(Calendar.DAY_OF_YEAR) - cal1.get(Calendar.DAY_OF_YEAR) + 1);
            } else {
                int days = 0;
                days += getYearTotalDays(year1) - cal1.get(Calendar.DAY_OF_YEAR);
                for (int year = year1 + 1; year < year2; year++) {
                    days += getYearTotalDays(year);
                }
                days += cal2.get(Calendar.DAY_OF_YEAR);
                formatDate = String.valueOf(days + 1);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return formatDate;
    }

    // 指定年の日数
    private int getYearTotalDays(final int year) {
        // うるう年か
        boolean isLeapYear = year % 400 == 0 ? true : year % 100 == 0 ? false : year % 4 == 0 ? true : false;
        // うるう年なら366日
        return isLeapYear ? 366 : 365;
    }

    private String plus(final String num1, final String num2) {
        if (null == num1) {
            return num2;
        } else if (null == num2) {
            return num1;
        }
        return String.valueOf(Integer.parseInt(num1) + Integer.parseInt(num2));
    }
    
    private class MedexamDiseaseAddition4 {
        final String _year;
        final String _diseasecd;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _count;
        final String _hrCount;
        final String _actionSDate;
        final String _actionEDate;
        final String _patientCount;
        final String _absenceCount;
        final String _presenceCount;
        final String _symptom01;
        final String _symptom01Remark;
        final String _symptom02;
        final String _symptom03;
        final String _symptom04;
        final String _symptom05;
        final String _symptom06;
        final String _symptom07;
        final String _symptom08;
        final String _symptom09;
        final String _symptom10;
        final String _symptom11;
        final String _symptom12;
        final String _symptom12Remark;
        final String _remark;
        final String _c900name1;
        final String _c900namespare3;
        final String _gradeName1;

        MedexamDiseaseAddition4(
            final String year,
            final String diseasecd,
            final String grade,
            final String hrClass,
            final String hrName,
            final String count,
            final String hrCount,
            final String actionSDate,
            final String actionEDate,
            final String patientCount,
            final String absenceCount,
            final String presenceCount,
            final String symptom01,
            final String symptom01Remark,
            final String symptom02,
            final String symptom03,
            final String symptom04,
            final String symptom05,
            final String symptom06,
            final String symptom07,
            final String symptom08,
            final String symptom09,
            final String symptom10,
            final String symptom11,
            final String symptom12,
            final String symptom12Remark,
            final String remark,
            final String c900name1,
            final String c900namespare3,
            final String gradeName1
        ) {
            _year = year;
            _diseasecd = diseasecd;
            _grade = grade;
            _hrClass = hrClass;
            _count = count;
            _hrCount = hrCount;
            _hrName = hrName;
            _actionSDate = actionSDate;
            _actionEDate = actionEDate;
            _patientCount = patientCount;
            _absenceCount = absenceCount;
            _presenceCount = presenceCount;
            _symptom01 = symptom01;
            _symptom01Remark = symptom01Remark;
            _symptom02 = symptom02;
            _symptom03 = symptom03;
            _symptom04 = symptom04;
            _symptom05 = symptom05;
            _symptom06 = symptom06;
            _symptom07 = symptom07;
            _symptom08 = symptom08;
            _symptom09 = symptom09;
            _symptom10 = symptom10;
            _symptom11 = symptom11;
            _symptom12 = symptom12;
            _symptom12Remark = symptom12Remark;
            _remark = remark;
            _c900name1 = c900name1;
            _c900namespare3 = c900namespare3;
            _gradeName1 = gradeName1;
        }
    }

    private List getMedexamDiseaseAddition4List(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String year = rs.getString("YEAR");
                final String diseasecd = rs.getString("DISEASECD");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String count = rs.getString("COUNT");
                final String hrCount = rs.getString("HR_COUNT");
                final String actionSDate = rs.getString("ACTION_S_DATE");
                final String actionEDate = rs.getString("ACTION_E_DATE");
                final String patientCount = rs.getString("PATIENT_COUNT");
                final String absenceCount = rs.getString("ABSENCE_COUNT");
                final String presenceCount = rs.getString("PRESENCE_COUNT");
                final String symptom01 = rs.getString("SYMPTOM01");
                final String symptom01Remark = rs.getString("SYMPTOM01_REMARK");
                final String symptom02 = rs.getString("SYMPTOM02");
                final String symptom03 = rs.getString("SYMPTOM03");
                final String symptom04 = rs.getString("SYMPTOM04");
                final String symptom05 = rs.getString("SYMPTOM05");
                final String symptom06 = rs.getString("SYMPTOM06");
                final String symptom07 = rs.getString("SYMPTOM07");
                final String symptom08 = rs.getString("SYMPTOM08");
                final String symptom09 = rs.getString("SYMPTOM09");
                final String symptom10 = rs.getString("SYMPTOM10");
                final String symptom11 = rs.getString("SYMPTOM11");
                final String symptom12 = rs.getString("SYMPTOM12");
                final String symptom12Remark = rs.getString("SYMPTOM12_REMARK");
                final String remark = rs.getString("REMARK");
                final String c900name1 = rs.getString("C900NAME1");
                final String c900namespare3 = rs.getString("C900NAMESPARE3");
                final String gradeName1 = rs.getString("GRADE_NAME1");
                final MedexamDiseaseAddition4 medexamdiseaseaddition4 = new MedexamDiseaseAddition4(year, diseasecd, grade, hrClass, hrName, count, hrCount, actionSDate, actionEDate, patientCount, absenceCount, presenceCount, symptom01, symptom01Remark, symptom02, symptom03, symptom04, symptom05, symptom06, symptom07, symptom08, symptom09, symptom10, symptom11, symptom12, symptom12Remark, remark, c900name1, c900namespare3, gradeName1);
                list.add(medexamdiseaseaddition4);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    public String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH REGD_TOTAL AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, GRADE, HR_CLASS ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT  ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' AND SEMESTER = '" + _param._semester + "' ");
        stb.append(" ), REGD_H_COUNT(GRADE, HR_COUNT) AS ( ");
        stb.append("     SELECT ");
        stb.append("         GRADE, COUNT(*) AS HR_COUNT ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_HDAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' AND SEMESTER = '" + _param._semester + "' ");
        stb.append("     GROUP BY ");
        stb.append("         GRADE ");
        stb.append(" ), REGD_COUNT(GRADE, HR_CLASS, COUNT) AS ( ");
        stb.append("     SELECT ");
        stb.append("         VALUE(GRADE, '99'), VALUE(HR_CLASS, '999'), COUNT(*) AS COUNT ");
        stb.append("     FROM ");
        stb.append("         REGD_TOTAL ");
        stb.append("     GROUP BY ");
        stb.append("         GROUPING SETS ((GRADE, HR_CLASS), (GRADE), ()) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.DISEASECD, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T6.HR_NAME, ");
        stb.append("     T4.COUNT, ");
        stb.append("     T5.HR_COUNT, ");
        stb.append("     T1.ACTION_S_DATE, ");
        stb.append("     T1.ACTION_E_DATE, ");
        stb.append("     T1.PATIENT_COUNT, ");
        stb.append("     T1.ABSENCE_COUNT, ");
        stb.append("     T1.PRESENCE_COUNT, ");
        stb.append("     T1.SYMPTOM01, ");
        stb.append("     T1.SYMPTOM01_REMARK, ");
        stb.append("     T1.SYMPTOM02, ");
        stb.append("     T1.SYMPTOM03, ");
        stb.append("     T1.SYMPTOM04, ");
        stb.append("     T1.SYMPTOM05, ");
        stb.append("     T1.SYMPTOM06, ");
        stb.append("     T1.SYMPTOM07, ");
        stb.append("     T1.SYMPTOM08, ");
        stb.append("     T1.SYMPTOM09, ");
        stb.append("     T1.SYMPTOM10, ");
        stb.append("     T1.SYMPTOM11, ");
        stb.append("     T1.SYMPTOM12, ");
        stb.append("     T1.SYMPTOM12_REMARK, ");
        stb.append("     T1.REMARK, ");
        stb.append("     T2.NAME1 AS C900NAME1, ");
        stb.append("     T2.NAMESPARE3 AS C900NAMESPARE3, ");
        stb.append("     T3.GRADE_NAME1 ");
        stb.append(" FROM MEDEXAM_DISEASE_ADDITION4_DAT T1 ");
        stb.append(" LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'C900' ");
        stb.append("     AND T2.NAMECD2 = T1.DISEASECD ");
        stb.append(" LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("     AND T3.GRADE = T1.GRADE ");
        stb.append(" LEFT JOIN REGD_COUNT T4 ON T4.GRADE = T1.GRADE ");
        stb.append("     AND T4.HR_CLASS = T1.HR_CLASS ");
        stb.append(" LEFT JOIN REGD_H_COUNT T5 ON T5.GRADE = T1.GRADE ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR = T1.YEAR ");
        stb.append("     AND T6.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T6.GRADE = T1.GRADE ");
        stb.append("     AND T6.HR_CLASS = T1.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        if (PRGID_KNJF305P.equals(_param._prgid)) {
            stb.append("     AND T1.ACTION_S_DATE BETWEEN '" + _param._sdate + "' AND '" + _param._edate + "' ");
        }
        if (PRGID_KNJF305.equals(_param._prgid)) {
            stb.append("     AND T1.ACTION_S_DATE = '" + _param._sdate + "' ");
            stb.append("     AND T1.DISEASECD = '" + _param._diseasecd + "' ");
            if (HEISA_GAKKYU.equals(_param._outDiv)) {
                stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
                stb.append("     AND T1.HR_CLASS = '" + _param._hrclass + "' ");
            } else if (HEISA_GAKUNEN.equals(_param._outDiv)) {
                stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
                stb.append("     AND T1.HR_CLASS = '999' ");
            } else if (HEISA_GAKKO.equals(_param._outDiv)) {
                stb.append("     AND T1.GRADE = '99' ");
                stb.append("     AND T1.HR_CLASS = '999' ");
            }
            
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.ACTION_S_DATE, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.DISEASECD ");
        return stb.toString();
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
        final String _year;
        final String _semester;
        final String _loginDate;
        final String _sdate;
        final String _edate;
        final String _diseasecd;
        final String _outDiv;
        final String _prgid;
        final String _grade;
        final String _hrclass;
        final KNJSchoolMst _knjSchoolMst;
        final String _regdTotalCount;
        final String _principalName;
        final String _useAddrField2;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _prgid = request.getParameter("PRGID");
            if (PRGID_KNJF305.equals(_prgid)) {
                _semester = request.getParameter("CTRL_SEMESTER");
                _sdate = request.getParameter("ACTION_S_DATE").replace('/', '-');
                _edate = request.getParameter("ACTION_S_DATE").replace('/', '-');
                _diseasecd = request.getParameter("DISEASECD");
                _outDiv = request.getParameter("HEISA_DIV");
                _loginDate = request.getParameter("CTRL_DATE");
                _grade = request.getParameter("GRADE");
                _hrclass = request.getParameter("HR_CLASS");
            } else {
                _semester = request.getParameter("SEMESTER");
                _sdate = request.getParameter("SDATE").replace('/', '-');
                _edate = request.getParameter("EDATE").replace('/', '-');
                _diseasecd = null;
                _outDiv = request.getParameter("OUT_DIV");
                _loginDate = request.getParameter("LOGIN_DATE");
                _grade = null;
                _hrclass = null;
            }
            _knjSchoolMst = new KNJSchoolMst(db2, _year);
            _regdTotalCount = getRegdTotalCount(db2, _year, _semester);
            _principalName = getPrincipalName(db2, _year);
            _useAddrField2 = request.getParameter("useAddrField2");
        }
        
        String getPrincipalName(final DB2UDB db2, final String year) {
            String principalName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "";
                sql += " SELECT T1.STAFFNAME ";
                sql += " FROM STAFF_MST T1 ";
                sql += " INNER JOIN STAFF_YDAT T2 ON T2.STAFFCD = T1.STAFFCD ";
                sql += " WHERE T1.JOBCD = '0001' AND T2.YEAR = '" + year + "'";
                sql += " ORDER BY T1.STAFFCD ";
                
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    principalName = rs.getString("STAFFNAME");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return principalName;
        }
        
        
        String getRegdTotalCount(final DB2UDB db2, final String year, final String semester) {
            String count = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "";
                sql += " SELECT COUNT(*) AS COUNT ";
                sql += " FROM SCHREG_REGD_DAT T1 ";
                sql += " WHERE T1.YEAR = '" + _year + "' AND T1.SEMESTER = '" + semester + "'";
                
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    count = rs.getString("COUNT");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return count;
        }
    }
}

// eof

