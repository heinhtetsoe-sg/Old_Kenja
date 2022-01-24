/*
 * $Id: bdef264e7c77a7d934a0bb399e075c0e8d7f5528 $
 *
 * 作成日: 2019/10/04
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJF334 {

    private static final Log log = LogFactory.getLog(KNJF334.class);

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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70089 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final String[] suffx = (String[]) _param._dataField.toArray(new String[_param._dataField.size()]);
        final List dataList = getDataList(db2, _param, suffx);
        if (dataList.size() == 0) {
            return;
        }
        final String form = "KNJF334.frm";

        svf.VrSetForm(form, 4);

        svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
        svf.VrsOut("PRINCIPAL_NAME", _param._principalName); // 学校長名
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate)); // 作成日

//        String bakSchoolKind = "";
//        Map totalSumMap = new LinkedMap();
        for (int i = 0; i < dataList.size(); i++) {
            final PrintData data = (PrintData) dataList.get(i);
            final String tfieldStr = "99".equals(data._grade) ? "_TOTAL" : "";
//            if (!"".equals(bakSchoolKind) && (!bakSchoolKind.equals(data._schoolKind) || "99".equals(data._grade))) {
//            	final String tsfieldStr = "_TOTAL";
//                svf.VrsOut("GRADE"  + tsfieldStr,  "小計");
//                svf.VrsOut("ENROLL" + tsfieldStr, (String)totalSumMap.get(suffx[0]));
//                svf.VrsOut("VISIT1" + tsfieldStr, (String)totalSumMap.get(suffx[1]));
//                svf.VrsOut("VISIT2" + tsfieldStr, (String)totalSumMap.get(suffx[2]));
//                svf.VrsOut("VISIT3" + tsfieldStr, (String)totalSumMap.get(suffx[3]));
//                svf.VrsOut("EXAM1"  + tsfieldStr, (String)totalSumMap.get(suffx[4]));
//                svf.VrsOut("EXAM2"  + tsfieldStr, (String)totalSumMap.get(suffx[5]));
//                svf.VrsOut("EXAM3"  + tsfieldStr, (String)totalSumMap.get(suffx[6]));
//                svf.VrsOut("TOTAL"  + tsfieldStr, (String)totalSumMap.get(suffx[7]));
//                svf.VrEndRecord();
//                totalSumMap = new LinkedMap();
//            }
//            totalSumMap.put(suffx[0], summaryCalc(totalSumMap, data._addDataMap, suffx, 0));
//            totalSumMap.put(suffx[1], summaryCalc(totalSumMap, data._addDataMap, suffx, 1));
//            totalSumMap.put(suffx[2], summaryCalc(totalSumMap, data._addDataMap, suffx, 2));
//            totalSumMap.put(suffx[3], summaryCalc(totalSumMap, data._addDataMap, suffx, 3));
//            totalSumMap.put(suffx[4], summaryCalc(totalSumMap, data._addDataMap, suffx, 4));
//            totalSumMap.put(suffx[5], summaryCalc(totalSumMap, data._addDataMap, suffx, 5));
//            totalSumMap.put(suffx[6], summaryCalc(totalSumMap, data._addDataMap, suffx, 6));
//            totalSumMap.put(suffx[7], summaryCalc(totalSumMap, data._addDataMap, suffx, 7));

            svf.VrsOut("GRADE"  + tfieldStr,  data._gradeName);
            svf.VrsOut("ENROLL" + tfieldStr, (String)data._addDataMap.get(suffx[0]));
            svf.VrsOut("VISIT1" + tfieldStr, (String)data._addDataMap.get(suffx[1]));
            svf.VrsOut("VISIT2" + tfieldStr, (String)data._addDataMap.get(suffx[2]));
            svf.VrsOut("VISIT3" + tfieldStr, (String)data._addDataMap.get(suffx[3]));
            svf.VrsOut("EXAM1"  + tfieldStr, (String)data._addDataMap.get(suffx[4]));
            svf.VrsOut("EXAM2"  + tfieldStr, (String)data._addDataMap.get(suffx[5]));
            svf.VrsOut("EXAM3"  + tfieldStr, (String)data._addDataMap.get(suffx[6]));
            svf.VrsOut("TOTAL"  + tfieldStr, (String)data._addDataMap.get(suffx[7]));
            svf.VrEndRecord();
//            bakSchoolKind = data._schoolKind;
            _hasData = true;
        }
    }

//    private String summaryCalc(final Map totalSumMap, final Map addDataMap, final String[] suffx, final int idx) {
//    	String retStr = "";
//    	final String dataWk = (String)addDataMap.get(suffx[idx]);
//    	final String sumVal = (totalSumMap.containsKey(suffx[idx]) ? (String)totalSumMap.get(suffx[idx]) : "0");
//    	retStr = String.valueOf(Integer.parseInt(sumVal) + Integer.parseInt(dataWk));
//    	return retStr;
//    }


    private List getDataList(final DB2UDB db2, final Param param, final String[] suffx) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql(param, suffx);
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String grade = rs.getString("GRADE");
                final String gradeName = rs.getString("GRADE_NAME1");
                final Map addDataMap = new LinkedMap();
                for (int sfi = 0; sfi < suffx.length; sfi++) {
                    final String divSeq = suffx[sfi];
                    addDataMap.put(divSeq, rs.getString("DATA" + divSeq));
            	}

                PrintData addObj = new PrintData(schoolKind, grade, gradeName, addDataMap);
                list.add(addObj);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    public String sql(final Param param, final String[] suffx) {
        final StringBuffer stb = new StringBuffer();
        // 他のKNJF331等の真似をしようとしたが、そもそも利用が年齢ではなく学年になるので、扱いが全く違う。
        // 人数として途中学期から転校してきた生徒も対象にする?
        // ->対象と考えて、当年度最小学期のデータを引っ張る。(当年度の学年は、その年の最初の学期の学年で検診のはず)
        stb.append(" WITH SCHREG_MINSEMES_TBL AS ( ");
        stb.append("   SELECT ");
        stb.append("     YEAR, ");
        stb.append("     MIN(SEMESTER) AS M_SEMES, ");
        stb.append("     SCHREGNO, ");
        stb.append("     GRADE ");
        stb.append("   FROM ");
        stb.append("     SCHREG_REGD_DAT ");
        stb.append("   WHERE ");
        stb.append("     YEAR = '2006' ");
        stb.append("   GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     GRADE ");
        stb.append(" ), T_GRADE AS ( ");
        stb.append("   SELECT ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     GRADE, ");
        stb.append("     GRADE_NAME1 ");
        stb.append("   FROM ");
        stb.append("     SCHREG_REGD_GDAT ");
        stb.append("   WHERE ");
        stb.append("     YEAR = '" + param._ctrlYear + "' ");
        if ("1".equals(param._use_prg_schoolkind)) {
            if (!"".equals(param._selectSchoolKind)) {
                stb.append("           AND SCHOOL_KIND IN " + SQLUtils.whereIn(true, StringUtils.split(param._selectSchoolKind, ':')) + " ");
            }
        } else if ("1".equals(param._useSchool_KindField)) {
            stb.append("           AND SCHOOL_KIND = '" + param._schoolKind +"' ");
        }
        stb.append(" UNION ");
        stb.append(" SELECT DISTINCT ");
        stb.append("   '' AS SCHOOL_KIND, ");
        stb.append("   TV1.GRADE, ");
        stb.append("   '合計' AS GRADE_NAME1 ");
        stb.append(" FROM ");
        if (!"".equals(StringUtils.defaultString(param._fixedData, ""))) {
            stb.append("         MEDEXAM_DISEASE_ADDITION334_FIXED_DAT TV1 ");
        } else {
            stb.append("         MEDEXAM_DISEASE_ADDITION334_DAT TV1 ");
        }
        stb.append(" WHERE ");
        stb.append("   TV1.YEAR = '" + param._ctrlYear + "' ");
        stb.append("   AND TV1.GRADE = '99' ");

        stb.append(" ) ");
        //登録テーブル
        stb.append(" , T_ADDITION1 AS ( ");
        stb.append("     SELECT ");
        stb.append("         * ");
        stb.append("     FROM ");
        if (!"".equals(StringUtils.defaultString(param._fixedData, ""))) {
            stb.append("         MEDEXAM_DISEASE_ADDITION334_FIXED_DAT ");
        } else {
            stb.append("         MEDEXAM_DISEASE_ADDITION334_DAT ");
        }
        stb.append("     WHERE ");
        stb.append("         EDBOARD_SCHOOLCD = '" + param._edSchoolCd + "' ");
        stb.append("         AND YEAR = '" + param._ctrlYear + "' ");
        if (!"".equals(StringUtils.defaultString(param._fixedData, ""))) {
            stb.append("         AND FIXED_DATE = '" + StringUtils.replace(param._fixedData, "/", "-") + "' ");
        }
        stb.append("     ) ");

        //メイン
        stb.append(" SELECT ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.GRADE_NAME1 ");
        for (int sfi = 0; sfi < suffx.length; sfi++) {
            final String divSeq = suffx[sfi];
            stb.append("     ,L"+divSeq+".INT_VAL AS DATA"+divSeq+" ");
        }
        stb.append(" FROM ");
        stb.append("     T_GRADE T1 ");
        for (int sfi = 0; sfi < suffx.length; sfi++) {
            final String divSeq = suffx[sfi];
            final String[] cutwk = StringUtils.split(divSeq, "_");
            final String seq = cutwk[1];
            stb.append("     LEFT JOIN T_ADDITION1 L"+divSeq+" ON L"+divSeq+".GRADE = T1.GRADE ");
            stb.append("          AND L"+divSeq+".SEQ = '"+seq+"' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHOOL_KIND DESC, ");
        stb.append("     T1.GRADE ");

        return stb.toString();
    }

    private class PrintData {
    	final String _schoolKind;
    	final String _grade;
    	final String _gradeName;
    	final Map _addDataMap;
    	PrintData(
    	    	final String schoolKind,
    	    	final String grade,
    	    	final String gradeName,
    	    	final Map adddataMap
    			) {
    		_schoolKind = schoolKind;
    		_grade = grade;
    		_gradeName = gradeName;
    		_addDataMap = adddataMap;
    	}
    }

    /** パラメータクラス */
    private static class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _edSchoolCd;
        final String _schoolCd;
        final String _fixedData;
        final List _dataField;
        final String _schoolKind;
        final String _useSchool_KindField;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;
        final String _schoolKindInState;
        String _schoolName;
        String _principalName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolCd = request.getParameter("SCHOOLCD");
            _edSchoolCd = getSchoolcd(db2);
            getCertifSchoolMst(db2);
            _fixedData = StringUtils.isBlank(request.getParameter("FIXED_DATA")) ? null : request.getParameter("FIXED_DATA");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            _schoolKindInState = getSchoolKindInState();

            _dataField = Arrays.asList(new String[] {
                    "000_01",
                    "001_02",
                    "001_03",
                    "001_04",
                    "010_05",
                    "011_06",
                    "011_07",
                    "011_08"});
        }

        private String getSchoolKindInState() {
            String retStr = "";
            if (!"1".equals(_use_prg_schoolkind)) {
                return retStr;
            }
            if (null == _selectSchoolKind || "".equals(_selectSchoolKind)) {
                return retStr;
            }
            final String[] strSplit = StringUtils.split(_selectSchoolKind, ":");
            String sep = "";
            for (int i = 0; i < strSplit.length; i++) {
                retStr += sep + "'" + strSplit[i] + "'";
                sep = ",";
            }
            return retStr;
        }

        private String getSchoolcd(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT distinct KYOUIKU_IINKAI_SCHOOLCD FROM V_SCHOOL_MST WHERE YEAR    = '" + _ctrlYear + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	if (rs.getString("KYOUIKU_IINKAI_SCHOOLCD") != null) {
                        rtn = rs.getString("KYOUIKU_IINKAI_SCHOOLCD");
                	}
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        private void getCertifSchoolMst(final DB2UDB db2) {
        	StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("   T1.SCHOOL_NAME, ");
        	stb.append("   T2.STAFFNAME ");
        	stb.append(" FROM ");
        	stb.append("  CERTIF_SCHOOL_DAT T1 ");
        	stb.append("  LEFT JOIN STAFF_MST T2 ");
        	stb.append("    ON T2.STAFFCD = T1.REMARK6 ");
        	stb.append(" WHERE ");
        	stb.append("  T1.YEAR = '" + _ctrlYear + "' ");
        	stb.append("  AND T1.CERTIF_KINDCD = '124' ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                	_schoolName = rs.getString("SCHOOL_NAME");
                	_principalName = rs.getString("STAFFNAME");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return;
        }
    }
}

// eof

