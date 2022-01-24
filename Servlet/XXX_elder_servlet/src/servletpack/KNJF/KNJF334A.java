/*
 * $Id: f4060955cd0748f90b4e644abb8e805888999c5c $
 *
 * 作成日: 2019/10/07
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
import java.util.Iterator;
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

public class KNJF334A {

    private static final Log log = LogFactory.getLog(KNJF334A.class);

    private final String[] suffx = new String[] {
            "01", "02", "03", "04", "05", "06", "07", "08"};

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
        log.fatal("$Revision: 70091 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
        }
        return (Map) map.get(key1);
    }

    private static String getString(final String field, final Map map) {
        try {
            if (null == field || !map.containsKey(field)) {
                // フィールド名が間違い
                throw new RuntimeException("指定されたフィールドのデータがありません。フィールド名：'" + field + "' :" + map);
            }
        } catch (final Exception e) {
            log.error("exception!", e);
        }
        if (null == field) {
            return null;
        }
        return (String) map.get(field);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final String form;
        form = "KNJF334A.frm";
        svf.VrSetForm(form, 4);
        if ("1".equals(_param._dataDiv2)) {
            printOut(db2, svf, null, 0);
        } else {
            int frmGrpCd = 0;
            final int maxline = 47;
            int lineCnt = 0;
            for (Iterator itEdbord = _param._schoolMap.keySet().iterator(); itEdbord.hasNext();) {
            	final String fkey = (String)itEdbord.next();
                EdboardSchool edboardSchool = (EdboardSchool) _param._schoolMap.get(fkey);
                lineCnt += printOut(db2, svf, edboardSchool, frmGrpCd);
                if (itEdbord.hasNext()) {
                	svf.VrsOut("BLANK", "a");
                	svf.VrEndRecord();
                }
                frmGrpCd++;
            }
        }
        svf.VrEndPage();
    }

    private int printOut(final DB2UDB db2, final Vrw32alp svf, final EdboardSchool edboardSchool, final int frmGrpCd) {
    	int retLineCnt = 0;
        final List dataList = getDataList(db2, _param, edboardSchool);
        if (dataList.size() == 0) {
            return 0;
        }
        final Map fieldMap = new HashMap();

        fieldMap.put("01", "ENROLL");
        fieldMap.put("02", "VISIT1");
        fieldMap.put("03", "VISIT2");
        fieldMap.put("04", "VISIT3");
        fieldMap.put("05", "EXAM1");
        fieldMap.put("06", "EXAM2");
        fieldMap.put("07", "EXAM3");
        fieldMap.put("08", "TOTAL");

        svf.VrsOut("TITLE",  KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度　結核検診実施報告集計表");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate)); // 作成日

        for (int i = 0; i < dataList.size(); i++) {
            final PrintData data = (PrintData) dataList.get(i);
            if (i == 0) {//最初だけ出力
            	if ("1".equals(_param._dataDiv2)) {
            	    svf.VrsOut("SCHOOL_NAME", "統計");
                    svf.VrEndRecord();
            	} else {//学校毎の出力
            	    svf.VrsOut("SCHOOL_NAME", edboardSchool._schoolName);
                    svf.VrEndRecord();
            	}
                retLineCnt = retLineCnt + 3; //タイトル1行+ヘッダ2行
            }
            svf.VrsOut("GRADE", data._gradeName);
            for (int sfi = 0; sfi < suffx.length; sfi++) {
            	final String vofield = (String)fieldMap.get(suffx[sfi]);
            	svf.VrsOut(vofield, (String)data._dataMap.get(suffx[sfi]));
            }
            svf.VrEndRecord();
            retLineCnt++;
            _hasData = true;
        }
        return retLineCnt;
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

//    private void printCover(final DB2UDB db2, final Vrw32alp svf) {
//        svf.VrSetForm("KNJE_COVER.frm", 1);
//        svf.VrsOut("TITLE",  KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度　結核検診実施報告集計表");
//        svf.VrsOut("DATE",  "作成日：" + KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));
//        int fieldName = 1;
//        int hyousiGyo = 1;
//        for (Iterator itEdbord = _param._schoolList.iterator(); itEdbord.hasNext();) {
//            if (hyousiGyo > 30) {
//                hyousiGyo = 1;
//                fieldName++;
//            }
//            EdboardSchool edboardSchool = (EdboardSchool) itEdbord.next();
//            svf.VrsOutn("SCHOOL_NAME" + fieldName, hyousiGyo, edboardSchool._schoolName);
//            svf.VrsOutn("DATE" + fieldName, hyousiGyo, edboardSchool._executeDate);
//            hyousiGyo++;
//        }
//        svf.VrEndPage();
//    }

    public List getDataList(final DB2UDB db2, final Param param, final EdboardSchool edboardSchool) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Map dataMap = new HashMap();
        try {
            final String sql = sql(param, edboardSchool);
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String gradeCd = rs.getString("GRADE_CD");
                final String gradeName = rs.getString("GRADE_NAME1");
                final Map addDataMap = new LinkedMap();
                for (int sfi = 0; sfi < suffx.length; sfi++) {
                    final String divSeq = suffx[sfi];
                    addDataMap.put(divSeq, rs.getString("DATA" + divSeq));
            	}
                PrintData addwk;
                addwk = new PrintData(schoolKind, gradeCd, gradeName, addDataMap);
                list.add(addwk);
//                final String ageData = rs.getString("AGE_DATA");
//                if (null == dataMap.get(ageData)) {
//                    final String ageName = rs.getString("AGE_NAME");
//                    final Data data = new Data(ageData, ageName);
//                    dataMap.put(ageData, data);
//                    list.add(data);
//                }
//                final Data data = (Data) dataMap.get(ageData);
//                final String sex = rs.getString("SEX");
//                data._sexList.add(sex);
//                //data._sexNameMap.put(sex, rs.getString("SEX_NAME"));
//
//                for (int i = 0; i < param._dataField.size(); i++) {
//                    final String field = (String) param._dataField.get(i);
//                    getMappedMap(data._sexData, sex).put(field, rs.getString("DATA" + field));
//                }
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    public static String sql(final Param param, final EdboardSchool edboardSchool) {
        final StringBuffer stb = new StringBuffer();
        //学年・性別MEDEXAM_DISEASE_ADDITION334_FIXED_DAT
        stb.append(" WITH MAX_DATE AS ( ");
        stb.append(" SELECT ");
        stb.append("     REP1.EDBOARD_SCHOOLCD, ");
        stb.append("     REP1.FIXED_DATE ");
        stb.append(" FROM ");
        stb.append("     REPORT_DISEASE_ADDITION334_DAT REP1, ");
        stb.append("     ( ");
        stb.append("     SELECT ");
        stb.append("         REP.EDBOARD_SCHOOLCD, ");
        stb.append("         REP.YEAR, ");
        stb.append("         MAX(REP.EXECUTE_DATE) AS EXECUTE_DATE ");
        stb.append("          ");
        stb.append("     FROM ");
        stb.append("         REPORT_DISEASE_ADDITION334_DAT REP ");
        stb.append("     WHERE ");
        stb.append("         REP.YEAR = '" + param._ctrlYear + "' ");
        stb.append("     GROUP BY ");
        stb.append("         REP.EDBOARD_SCHOOLCD, ");
        stb.append("         REP.YEAR ");
        stb.append("     ) REP_MAX ");
        stb.append(" WHERE ");
        stb.append("     REP1.EDBOARD_SCHOOLCD = REP_MAX.EDBOARD_SCHOOLCD ");
        stb.append("     AND REP1.YEAR = REP_MAX.YEAR ");
        stb.append("     AND REP1.EXECUTE_DATE = REP_MAX.EXECUTE_DATE ");
        stb.append(" ), T_GRADE AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         GDAT.SCHOOL_KIND, ");
        stb.append("         GDAT.GRADE_CD, ");
        stb.append("         GDAT.GRADE_NAME1 ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_GDAT GDAT ");
        stb.append("     WHERE ");
        stb.append("         GDAT.YEAR = '" + param._ctrlYear + "' ");
//        if (!"".equals(param._usePrgSchoolKind)) {
//            stb.append("         AND GDAT.SCHOOL_KIND IN " + SQLUtils.whereIn(true, StringUtils.split(param._selectSchoolKind, ':')) + " ");
//        } else if ("1".equals(param._useSchool_KindField)) {
//            stb.append("         AND GDAT.SCHOOL_KIND = '" + param._schoolKind + "' ");
//        }
        stb.append("     GROUP BY ");
        stb.append("         GDAT.SCHOOL_KIND, ");
        stb.append("         GDAT.GRADE_CD, ");
        stb.append("         GDAT.GRADE_NAME1 ");
        stb.append("     UNION ALL ");
        stb.append("        (SELECT ");
        stb.append("           'ZZ' AS SCHOOL_KIND, ");
        stb.append("           '99' AS GRADE_CD, ");
        stb.append("           '合計' AS GRADE_NAME1 ");
        stb.append("         FROM ");
        stb.append("           SCHREG_REGD_GDAT GDWK ");
        stb.append("         WHERE ");
        stb.append("           GDWK.YEAR = '" + param._ctrlYear + "' ");
        stb.append("           FETCH FIRST 1 ROWS ONLY ");
        stb.append("        ) ");
        stb.append("     ) ");
        //登録テーブル
        stb.append(" , T_ADDITION1 AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.* ");
        stb.append("     FROM ");
        stb.append("         MEDEXAM_DISEASE_ADDITION334_FIXED_DAT T1, ");
        stb.append("         MAX_DATE ");
        stb.append("     WHERE ");
        if ("1".equals(param._dataDiv2)) {
            stb.append("         T1.EDBOARD_SCHOOLCD IN (" + param._schoolInState + ") ");
        } else {
            stb.append("         T1.EDBOARD_SCHOOLCD = '" + edboardSchool._schoolCd + "' ");
        }
        stb.append("         AND T1.YEAR     = '" + param._ctrlYear + "' ");
        stb.append("         AND T1.EDBOARD_SCHOOLCD = MAX_DATE.EDBOARD_SCHOOLCD ");
        stb.append("         AND T1.FIXED_DATE = MAX_DATE.FIXED_DATE ");
        stb.append("     ) ");

        //メイン(※SCHOOL_KIND、GRADE_CDを利用しているのは、学校間でGRADEの値に違いがあるため。)
        stb.append(" SELECT ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.GRADE_CD, ");
        stb.append("     T1.GRADE_NAME1 ");
        for (int i = 0; i < param._dataField.size(); i++) {
            final String divSeq = (String) param._dataField.get(i);
            stb.append("     ,SUM(CASE WHEN T_ADDITION1.SEQ = '" + divSeq + "' THEN T_ADDITION1.INT_VAL ELSE 0 END) AS DATA" + divSeq + " ");
        }
        stb.append(" FROM ");
        stb.append("     T_GRADE T1 ");
        stb.append("     LEFT JOIN T_ADDITION1 ON T_ADDITION1.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("          AND T_ADDITION1.GRADE_CD = T1.GRADE_CD ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.GRADE_CD, ");
        stb.append("     T1.GRADE_NAME1 ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHOOL_KIND = 'ZZ' asc, ");
        stb.append("     T1.SCHOOL_KIND DESC, ");
        stb.append("     T1.GRADE_CD ");
        return stb.toString();
    }

    private class PrintData {
        final String _schoolKind;
        final String _gradeCd;
        final String _gradeName;
        //final List _sexList = new ArrayList();
        final Map _dataMap;
        //final Map _sexData = new HashMap();

        PrintData(
                final String schoolKind,
                final String gradeCd,
                final String gradeName,
                final Map dataMap
        ) {
        	_schoolKind = schoolKind;
        	_gradeCd = gradeCd;
        	_gradeName = gradeName;
        	_dataMap = dataMap;
        }

    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlDate;
        private final String _ctrlSemester;
        private final Map _schoolMap;
        private String _schoolInState;
        final String _dataDiv2;
        final List _dataField;
        final String _schoolKind;
        final String _useSchool_KindField;
        final String _selectSchoolKind;
        final String _usePrgSchoolKind;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _usePrgSchoolKind = request.getParameter("use_prg_schoolkind");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            String schoolCd[] = request.getParameterValues("CATEGORY_SELECTED");
            _schoolMap = getSchoolMap(db2, schoolCd);
            _dataDiv2 = request.getParameter("DATA_DIV2");

            _dataField = Arrays.asList(new String[] {
                    "01",
                    "02",
                    "03",
                    "04",
                    "05",
                    "06",
                    "07",
                    "08",});
        }

        private Map getSchoolMap(final DB2UDB db2, final String[] schoolCd) throws SQLException {
            final Map retMap = new HashMap();
            _schoolInState = "";
            String sep = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            for (int ia = 0 ; ia < schoolCd.length ; ia++) {
                final String setSchoolCd = schoolCd[ia].substring(1);
                final String schoolSql = getSchoolSql(setSchoolCd);
                try {
                    _schoolInState += sep + "'" + setSchoolCd + "'";
                    sep = ",";
                    ps = db2.prepareStatement(schoolSql);
                    rs = ps.executeQuery();
                    String schoolName = "";
                    String executeDate = "";
                    while (rs.next()) {
                        schoolName = rs.getString("EDBOARD_SCHOOLNAME");
                        executeDate = rs.getString("EXECUTE_DATE");
                    }
                    final EdboardSchool edboardSchool = new EdboardSchool(setSchoolCd, schoolName, executeDate);
                    retMap.put(setSchoolCd, edboardSchool);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
                _schoolInState = "".equals(_schoolInState) ? "''" : _schoolInState;
            }
            return retMap;
        }

        private String getSchoolSql(final String schoolCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.EDBOARD_SCHOOLNAME, ");
            stb.append("     MAX(L1.EXECUTE_DATE) AS EXECUTE_DATE ");
            stb.append(" FROM ");
            stb.append("     EDBOARD_SCHOOL_MST T1 ");
            stb.append("     LEFT JOIN REPORT_AFT_DISEASE_ADDITION410_DAT L1 ON T1.EDBOARD_SCHOOLCD = L1.EDBOARD_SCHOOLCD ");
            stb.append("          AND L1.YEAR = '" + _ctrlYear + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.EDBOARD_SCHOOLNAME ");

            return stb.toString();
        }
    }

    private class EdboardSchool {
        private final String _schoolCd;
        private final String _schoolName;
        private final String _executeDate;
        public EdboardSchool(
            final String schoolCd,
            final String schoolName,
            final String executeDate
        ) {
            _schoolCd = schoolCd;
            _schoolName = schoolName;
            _executeDate = null != executeDate && !"".equals(executeDate) ? executeDate.replace('-', '/') : "";
        }
    }
}

// eof

