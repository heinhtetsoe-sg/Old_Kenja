/*
 * $Id: 09d36890bf844dfc132f3ab7deb6b39f6ca50a87 $
 *
 * 作成日: 2015/08/14
 * 作成者: maesiro
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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJF331A {

    private static final Log log = LogFactory.getLog(KNJF331A.class);

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
        log.fatal("$Revision: 56595 $");
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

        printCover(svf);
        final String form;
        if ("2".equals(_param._dataDiv)) {
            form = "1".equals(_param._dataDiv2) ? "KNJF331_2.frm" : "KNJF331A_2.frm";
        } else {
            form = "1".equals(_param._dataDiv2) ? "KNJF331_1.frm" : "KNJF331A_1.frm";
        }
        svf.VrSetForm(form, 4);
        if ("1".equals(_param._dataDiv2)) {
            printOut(db2, svf, null, 0);
        } else {
            int frmGrpCd = 0;
            for (Iterator itEdbord = _param._schoolList.iterator(); itEdbord.hasNext();) {
                EdboardSchool edboardSchool = (EdboardSchool) itEdbord.next();
                printOut(db2, svf, edboardSchool, frmGrpCd);
                frmGrpCd++;
            }
        }
    }

    private void printOut(final DB2UDB db2, final Vrw32alp svf, final EdboardSchool edboardSchool, final int frmGrpCd) {
        final List dataList = Data.getDataList(db2, _param, edboardSchool);
        if (dataList.size() == 0) {
            return;
        }
        final String[] suffx;
        final Map fieldMap = new HashMap();
        if ("2".equals(_param._dataDiv)) {
            suffx = new String[] {
                    "000_01", "000_02", "001_01", "001_02", "001_03", "001_04", "001_05", "002_01", "002_02", "002_03", "002_04", "002_05", "002_06", "002_07", "002_08", "002_09", "002_10"};

            fieldMap.put("000_01", "000_03");
            fieldMap.put("000_02", "000_04");
            fieldMap.put("001_01", "008_01");
            fieldMap.put("001_02", "008_02");
            fieldMap.put("001_03", "008_03");
            fieldMap.put("001_04", "008_04");
            fieldMap.put("001_05", "008_05");
            fieldMap.put("002_01", "009_01");
            fieldMap.put("002_02", "009_02");
            fieldMap.put("002_03", "009_03");
            fieldMap.put("002_04", "009_04");
            fieldMap.put("002_05", "009_05");
            fieldMap.put("002_06", "009_06");
            fieldMap.put("002_07", "009_07");
            fieldMap.put("002_08", "009_08");
            fieldMap.put("002_09", "009_09");
            fieldMap.put("002_10", "009_10");

        } else {
            suffx = new String[] {
                    "000_01", "000_02", "001_01", "001_02", "002_01", "002_02", "003_01", "003_02", "003_03", "003_04", "003_05", "004_01", "004_02", "004_03", "005_01", "006_01", "006_02", "006_03", "007_01",
                    "008_01", "009_01", "009_02", "010_01", "011_01", "012_01", "012_02", "013_01", "014_01"};

            fieldMap.put("008_01", "010_01");
            fieldMap.put("009_01", "011_01");
            fieldMap.put("009_02", "011_02");
            fieldMap.put("010_01", "012_01");
            fieldMap.put("011_01", "013_01");
            fieldMap.put("012_01", "014_01");
            fieldMap.put("012_02", "014_02");
            fieldMap.put("013_01", "015_01");
            fieldMap.put("014_01", "016_01");
        }

        svf.VrsOut("TITLE", "健康診断結果　入力Ａシート（" + ("1".equals(_param._dataDiv) ? "一般" : "2".equals(_param._dataDiv) ? "歯・口腔" : "")  + "）"); // タイトル
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate)); // 作成日

        for (int i = 0; i < dataList.size(); i++) {
            final Data data = (Data) dataList.get(i);
            for (int sfi = 0; sfi < suffx.length; sfi++) {
                final String dataField = StringUtils.defaultString((String) fieldMap.get(suffx[sfi]), suffx[sfi]);
                final String field2;
                final int sxiMax;
                if ("99".equals(data._ageData)) {
                    sxiMax = 3;
                    field2 = "TOTAL_DATA";
                } else {
                    svf.VrsOut("GRP", String.valueOf(frmGrpCd));
                    if (null != edboardSchool) {
                        svf.VrsOut("SCHOOL_NAME1_1",  edboardSchool._schoolName);
                    }
                    svf.VrsOut("GRADE_NAME", data._ageName); // 学年名
                    sxiMax = 2;
                    field2 = "DATA";
                }
                for (int sxi = 0; sxi < sxiMax; sxi++) {
                    final String sex = String.valueOf(sxi + 1);
                    final Map dataMap = getMappedMap(data._sexData, "3".equals(sex) ? "9" : sex);
                    svf.VrsOut(field2 + suffx[sfi] + "_" + String.valueOf(sxi + 1), StringUtils.defaultString(getString(dataField, dataMap)) + "\n"); // 栄養不良 肥満傾向 側わん その他 Ａ Ｂ Ｃ Ｄ Ｂ+Ｃ+Ｄ 感染性眼疾患 色覚異常の疑い その他の眼疾患 難聴 耳疾患 鼻副鼻腔疾患 口腔咽喉頭疾患異常 栄養状態 感染性皮膚疾患 結核 対策委員会での要検討 要精密検査 心臓の疾病異常 心電図異常 蛋白検出 糖検出 寄生虫卵保有 その他の疾病異常
                }
            }
            svf.VrEndRecord();
            _hasData = true;
        }
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

    private void printCover(final Vrw32alp svf) {
        svf.VrSetForm("KNJE_COVER.frm", 1);
        svf.VrsOut("TITLE",  nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　健康診断統計報告Ａ");
        svf.VrsOut("DATE",  "作成日：" + KNJ_EditDate.h_format_JP(_param._ctrlDate));
        int fieldName = 1;
        int hyousiGyo = 1;
        for (Iterator itEdbord = _param._schoolList.iterator(); itEdbord.hasNext();) {
            if (hyousiGyo > 30) {
                hyousiGyo = 1;
                fieldName++;
            }
            EdboardSchool edboardSchool = (EdboardSchool) itEdbord.next();
            svf.VrsOutn("SCHOOL_NAME" + fieldName, hyousiGyo, edboardSchool._schoolName);
            svf.VrsOutn("DATE" + fieldName, hyousiGyo, edboardSchool._executeDate);
            hyousiGyo++;
        }
        svf.VrEndPage();
    }

    private static class Data {
        final String _ageData;
        final String _ageName;
        final List _sexList = new ArrayList();
        final Map _sexNameMap = new HashMap();
        final Map _sexData = new HashMap();

        Data(
                final String ageData,
                final String ageName
        ) {
            _ageData = ageData;
            _ageName = ageName;
        }

        public static List getDataList(final DB2UDB db2, final Param param, final EdboardSchool edboardSchool) {
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
                    final String ageData = rs.getString("AGE_DATA");
                    if (null == dataMap.get(ageData)) {
                        final String ageName = rs.getString("AGE_NAME");
                        final Data data = new Data(ageData, ageName);
                        dataMap.put(ageData, data);
                        list.add(data);
                    }
                    final Data data = (Data) dataMap.get(ageData);
                    final String sex = rs.getString("SEX");
                    data._sexList.add(sex);
                    data._sexNameMap.put(sex, rs.getString("SEX_NAME"));

                    for (int i = 0; i < param._dataField.size(); i++) {
                        final String field = (String) param._dataField.get(i);
                        getMappedMap(data._sexData, sex).put(field, rs.getString("DATA" + field));
                    }
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
            //学年・性別
            stb.append(" WITH MAX_DATE AS ( ");
            stb.append(" SELECT ");
            stb.append("     REP1.EDBOARD_SCHOOLCD, ");
            stb.append("     REP1.FIXED_DATE ");
            stb.append(" FROM ");
            stb.append("     REPORT_DISEASE_KENSIN_DAT REP1, ");
            stb.append("     ( ");
            stb.append("     SELECT ");
            stb.append("         REP.EDBOARD_SCHOOLCD, ");
            stb.append("         REP.YEAR, ");
            stb.append("         MAX(REP.EXECUTE_DATE) AS EXECUTE_DATE ");
            stb.append("          ");
            stb.append("     FROM ");
            stb.append("         REPORT_DISEASE_KENSIN_DAT REP ");
            stb.append("     WHERE ");
            stb.append("         REP.YEAR = '" + param._year + "' ");
            stb.append("     GROUP BY ");
            stb.append("         REP.EDBOARD_SCHOOLCD, ");
            stb.append("         REP.YEAR ");
            stb.append("     ) REP_MAX ");
            stb.append(" WHERE ");
            stb.append("     REP1.EDBOARD_SCHOOLCD = REP_MAX.EDBOARD_SCHOOLCD ");
            stb.append("     AND REP1.YEAR = REP_MAX.YEAR ");
            stb.append("     AND REP1.EXECUTE_DATE = REP_MAX.EXECUTE_DATE ");
            stb.append(" ), T_AGE (AGE_DATA, AGE_NAME) AS ( ");
            stb.append("     SELECT ");
            stb.append("         CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('" + param._year + "-04-01' - BASE.BIRTHDAY) END AS AGE_DATA, ");
            stb.append("         RTRIM(CAST(CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('" + param._year + "-04-01' - BASE.BIRTHDAY) END AS CHAR(4))) AS AGE_NAME ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT REGD ");
            if ("1".equals(param._useSchool_KindField)) {
                stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
                stb.append("         AND REGD.GRADE = GDAT.GRADE ");
                stb.append("         AND GDAT.SCHOOL_KIND = '" + param._schoolKind + "' ");
            }
            stb.append("         INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("               AND BASE.BIRTHDAY IS NOT NULL ");
            stb.append("     WHERE ");
            stb.append("         REGD.YEAR     = '" + param._year + "' ");
            stb.append("         AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
            stb.append("     GROUP BY ");
            stb.append("         CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('" + param._year + "-04-01' - BASE.BIRTHDAY) END ");
            stb.append("     UNION ALL ");
            stb.append("     VALUES(99, '合計') ");
            stb.append("     ) ");
            stb.append(" , T_SEX (SEX, SEX_NAME) AS ( ");
            stb.append("     SELECT ");
            stb.append("         NAMECD2, ");
            stb.append("         ABBV1 ");
            stb.append("     FROM ");
            stb.append("         NAME_MST ");
            stb.append("     WHERE ");
            stb.append("         NAMECD1 = 'Z002' ");
            stb.append("     UNION ALL ");
            stb.append("     VALUES('9', '合計') ");
            stb.append("     ) ");
            stb.append(" , T_AGE_SEX AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.*, ");
            stb.append("         T2.* ");
            stb.append("     FROM ");
            stb.append("         T_AGE T1, ");
            stb.append("         T_SEX T2 ");
            stb.append("     WHERE ");
            stb.append("         T1.AGE_DATA = 99 OR T2.SEX != '9' ");
            stb.append("     ) ");
            //登録テーブル
            stb.append(" , T_ADDITION1 AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.* ");
            stb.append("     FROM ");
            stb.append("         MEDEXAM_DISEASE_KENSIN_FIXED_DAT T1, ");
            stb.append("         MAX_DATE ");
            stb.append("     WHERE ");
            if ("1".equals(param._dataDiv2)) {
                stb.append("         T1.EDBOARD_SCHOOLCD IN (" + param._schoolInState + ") ");
            } else {
                stb.append("         T1.EDBOARD_SCHOOLCD = '" + edboardSchool._schoolCd + "' ");
            }
            stb.append("         AND T1.YEAR     = '" + param._year + "' ");
            stb.append("         AND T1.EDBOARD_SCHOOLCD = MAX_DATE.EDBOARD_SCHOOLCD ");
            stb.append("         AND T1.FIXED_DATE = MAX_DATE.FIXED_DATE ");
            stb.append("     ) ");

            //メイン
            stb.append(" SELECT ");
            stb.append("     T1.AGE_DATA, ");
            stb.append("     T1.SEX, ");
            stb.append("     T1.AGE_NAME, ");
            stb.append("     T1.SEX_NAME ");
            for (int i = 0; i < param._dataField.size(); i++) {
                final String divSeq = (String) param._dataField.get(i);
                stb.append("     ,SUM(CASE WHEN T_ADDITION1.DATA_DIV || '_' || T_ADDITION1.SEQ = '" + divSeq + "' THEN T_ADDITION1.INT_VAL ELSE 0 END) AS DATA" + divSeq + " ");
            }
            stb.append(" FROM ");
            stb.append("     T_AGE_SEX T1 ");
            stb.append("     LEFT JOIN T_ADDITION1 ON T_ADDITION1.AGE = T1.AGE_DATA ");
            stb.append("          AND T_ADDITION1.SEX = T1.SEX ");
            stb.append(" GROUP BY ");
            stb.append("     T1.AGE_DATA, ");
            stb.append("     T1.SEX, ");
            stb.append("     T1.AGE_NAME, ");
            stb.append("     T1.SEX_NAME ");
            stb.append(" ORDER BY ");
            stb.append("     T1.AGE_DATA, ");
            stb.append("     T1.SEX ");
            return stb.toString();
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlDate;
        private final String _ctrlSemester;
        private final List _schoolList;
        private String _schoolInState;
        final String _dataDiv;
        final String _dataDiv2;
        final List _dataField;
        final String _schoolKind;
        final String _useSchool_KindField;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _dataDiv = request.getParameter("DATA_DIV");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            String schoolCd[] = request.getParameterValues("CATEGORY_SELECTED");
            _schoolList = getSchoolList(db2, schoolCd);
            _dataDiv2 = request.getParameter("DATA_DIV2");

            if (!"2".equals(_dataDiv)) {
                _dataField = Arrays.asList(new String[] {
                        "000_01",
                        "000_02",
                        "001_01",
                        "001_02",
                        "002_01",
                        "002_02",
                        "003_01",
                        "003_02",
                        "003_03",
                        "003_04",
                        "003_05",
                        "004_01",
                        "004_02",
                        "004_03",
                        "005_01",
                        "006_01",
                        "006_02",
                        "006_03",
                        "007_01",
                        "010_01",
                        "011_01",
                        "011_02",
                        "012_01",
                        "013_01",
                        "014_01",
                        "014_02",
                        "015_01",
                        "016_01"});
            } else {
                _dataField = Arrays.asList(new String[] {
                        "000_03",
                        "000_04",
                        "008_01",
                        "008_02",
                        "008_03",
                        "008_04",
                        "008_05",
                        "009_01",
                        "009_02",
                        "009_03",
                        "009_04",
                        "009_05",
                        "009_06",
                        "009_07",
                        "009_08",
                        "009_09",
                        "009_10"});
            }
        }

        private List getSchoolList(final DB2UDB db2, final String[] schoolCd) throws SQLException {
            final List retList = new ArrayList();
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
                    retList.add(edboardSchool);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
                _schoolInState = "".equals(_schoolInState) ? "''" : _schoolInState;
            }
            return retList;
        }

        private String getSchoolSql(final String schoolCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.EDBOARD_SCHOOLNAME, ");
            stb.append("     MAX(L1.EXECUTE_DATE) AS EXECUTE_DATE ");
            stb.append(" FROM ");
            stb.append("     EDBOARD_SCHOOL_MST T1 ");
            stb.append("     LEFT JOIN REPORT_AFT_DISEASE_ADDITION410_DAT L1 ON T1.EDBOARD_SCHOOLCD = L1.EDBOARD_SCHOOLCD ");
            stb.append("          AND L1.YEAR = '" + _year + "' ");
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

