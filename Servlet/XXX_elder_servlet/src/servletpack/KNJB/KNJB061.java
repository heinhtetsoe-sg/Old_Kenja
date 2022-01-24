package servletpack.KNJB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJB061 {

    private static final Log log = LogFactory.getLog(KNJB061.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String form = _param._isSeito ? "KNJB061_1.frm" : _param._isShokuin ? "KNJB061_2.frm" : "KNJB061_3.frm";
        final String nendo = KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
        final String title = _param._isSeito ? "生徒" : _param._isShokuin ? "職員" : "施設";
        final String subtitle = _param._isKihon ? _param._titleKihon : KNJ_EditDate.h_format_JP(_param._sDate) + " \uFF5E " + KNJ_EditDate.h_format_JP(_param._eDate);
        final String printDate = KNJ_EditDate.h_format_JP(_param._ctrlDate);
        final String dayName[] = {"月","火","水","木","金","土","日"};

        final List printCdList = getList(db2, getPrintCdSql());
        for (int i = 0; i < printCdList.size(); i++) {
            final Map printCdMap = (Map) printCdList.get(i);
            final String printCd = getString(printCdMap, "PRINT_CD");

            //ヘッダ
            svf.VrSetForm(form, 4);
            svf.VrsOut("TITLE", nendo + "　" + title + "別時間割");
            svf.VrsOut("SUBTITLE", "(" + subtitle + ")");
            svf.VrsOut("PRINT_DATE", printDate);
            if (_param._isSeito) {
                svf.VrsOut("HR_NAME", getString(printCdMap, "HR_NAME"));
                final String setAttendno = String.valueOf(Integer.parseInt(getString(printCdMap, "ATTENDNO")));
                svf.VrsOut("NO", setAttendno + "番");
                final String setName = getString(printCdMap, "NAME");
                svf.VrsOut("NAME" + (getMS932Bytecount(setName) <= 30 ? "1" : "2"), setName);
            }
            if (_param._isShokuin) {
                svf.VrsOut("HR_NAME", getString(printCdMap, "SECTIONCD") + "　" + getString(printCdMap, "SECTIONNAME"));
                svf.VrsOut("NO", getString(printCdMap, "STAFFCD"));
                final String setName = getString(printCdMap, "STAFFNAME");
                svf.VrsOut("NAME" + (getMS932Bytecount(setName) <= 30 ? "1" : "2"), setName);
            }
            if (_param._isSisetu) {
                svf.VrsOut("HR_NAME", getString(printCdMap, "FACCD") + "　" + getString(printCdMap, "FACILITYNAME"));
            }

            int lineCnt = 0;

            //講座を取得
            final List chairCdList = getList(db2, getChairCdSql(printCd));
            for (int ii = 0; ii < chairCdList.size(); ii++) {
                final Map chairCdMap = (Map) chairCdList.get(ii);
                final String chairCd = getString(chairCdMap, "CHAIRCD");

                //時間割が無い場合、出力しない。
                final List schDatList = getList(db2, sqlSchDat(chairCd));
                if (schDatList.size() == 0) continue;

                final List stfFacList = getList(db2, sqlChairStfFac(chairCd));
                final List creditsList = getList(db2, sqlCredits(printCd, chairCd));
                final List studentCntList = getList(db2, sqlStudentCnt(chairCd));
                final List chairClsList = getList(db2, sqlChairCls(chairCd));

                //職員名・施設名　あるだけ複数行出力。他項目は同じものを複数行出力
                for (int isf = 0; isf < stfFacList.size(); isf++) {
                    final Map stfFacMap = (Map) stfFacList.get(isf);

                    //時間割
                    String setDayPeriod = "";
                    String comma = "";
                    for (int iii = 0; iii < schDatList.size(); iii++) {
                        final Map schDatMap = (Map) schDatList.get(iii);
                        int dayInt = Integer.parseInt(getString(schDatMap, "DAYCD"));
                        setDayPeriod += comma + dayName[dayInt-2] + getString(schDatMap, "PERIOD_ABBV");
                        comma = ",";
                    }
                    svf.VrsOut("PERIOD", setDayPeriod);
                    //講座コード・講座名
                    svf.VrsOut("HOPE_CHAIR_CD", getString(chairCdMap, "CHAIRCD"));
                    svf.VrsOut("HOPE_CHAIR_NAME1", getString(chairCdMap, "CHAIRNAME"));
                    //単位数
                    for (int iii = 0; iii < creditsList.size(); iii++) {
                        final Map creditsMap = (Map) creditsList.get(iii);
                        svf.VrsOut("CREDIT", getString(creditsMap, "MAX_CREDITS"));
                    }
                    //職員名・施設名
                    if (_param._isSeito) {
                        svf.VrsOut("TEACHER_NAME", getString(stfFacMap, "STAFFNAME_SHOW"));
                        svf.VrsOut("FACILITY_NAME", getString(stfFacMap, "FACILITYABBV"));
                    }
                    if (_param._isShokuin) {
                        svf.VrsOut("FACILITY_NAME", getString(stfFacMap, "FACILITYABBV"));
                    }
                    if (_param._isSisetu) {
                        svf.VrsOut("TEACHER_NAME", getString(stfFacMap, "STAFFNAME_SHOW"));
                    }
                    //生徒数
                    for (int iii = 0; iii < studentCntList.size(); iii++) {
                        final Map studentCntMap = (Map) studentCntList.get(iii);
                        svf.VrsOut("STUDENT_NUM", getString(studentCntMap, "STUDENT_CNT"));
                    }
                    //受講クラス
                    String setHrNameabbv = "";
                    String comma2 = "";
                    for (int iii = 0; iii < chairClsList.size(); iii++) {
                        final Map chairClsMap = (Map) chairClsList.get(iii);
                        setHrNameabbv += comma2 + getString(chairClsMap, "HR_NAMEABBV");
                        comma2 = ",";
                    }
                    svf.VrsOut("CHAIR_CLASS_NAME" + (getMS932Bytecount(setHrNameabbv) <= 20 ? "1" : "2"), setHrNameabbv);

                    lineCnt++;
                    svf.VrEndRecord();
                    if (lineCnt == 30) {
                        //空行（最終行の太枠下線）
                        svf.VrsOut("SPACE", "SPACE");
                        svf.VrEndRecord();
                    }
                    _hasData = true;
                }
            }//講座

            if (0 < lineCnt) {
                //空行（最終行の太枠下線）
                svf.VrsOut("SPACE", "SPACE");
                svf.VrEndRecord();
                //備考
                svf.VrsOut("DUMMY", "DUMMY");
                svf.VrEndRecord();
            }
        }//printCd
    }

    private static String getString(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private List getList(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
                }
                list.add(m);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String getPrintCdSql() {
        final StringBuffer stb = new StringBuffer();
        if (_param._isSeito) {
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO AS PRINT_CD, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T2.NAME, ");
            stb.append("     T3.HR_NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T3.GRADE = T1.GRADE ");
            stb.append("         AND T3.HR_CLASS = T1.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS BETWEEN '" + _param._gradeHrClass1 + "' AND '" + _param._gradeHrClass2 + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
        }
        if (_param._isShokuin) {
            stb.append(" SELECT ");
            stb.append("     T1.STAFFCD AS PRINT_CD, ");
            stb.append("     T1.SECTIONCD, ");
            stb.append("     T1.STAFFCD, ");
            stb.append("     T1.STAFFNAME, ");
            stb.append("     T2.SECTIONNAME, ");
            stb.append("     T2.SECTIONABBV ");
            stb.append(" FROM ");
            stb.append("     V_STAFF_MST T1 ");
            stb.append("     INNER JOIN V_SECTION_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.SECTIONCD = T1.SECTIONCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SECTIONCD BETWEEN '" + _param._sectionCdName1 + "' AND '" + _param._sectionCdName2 + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SECTIONCD, ");
            stb.append("     T1.STAFFCD ");
        }
        if (_param._isSisetu) {
            stb.append(" SELECT ");
            stb.append("     T1.FACCD AS PRINT_CD, ");
            stb.append("     T1.FACCD, ");
            stb.append("     T1.FACILITYNAME, ");
            stb.append("     T1.FACILITYABBV ");
            stb.append(" FROM ");
            stb.append("     V_FACILITY_MST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.FACCD BETWEEN '" + _param._faccdName1 + "' AND '" + _param._faccdName2 + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.FACCD ");
        }
        return stb.toString();
    }

    private String getChairCdSql(final String printCd) {
        final StringBuffer stb = new StringBuffer();
        if (_param._isSeito) {
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     T2.CHAIRNAME ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT T1 ");
            stb.append("     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = '" + printCd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CHAIRCD ");
        }
        if (_param._isShokuin) {
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     T2.CHAIRNAME ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STF_DAT T1 ");
            stb.append("     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.STAFFCD = '" + printCd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CHAIRCD ");
        }
        if (_param._isSisetu) {
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     T2.CHAIRNAME ");
            stb.append(" FROM ");
            stb.append("     CHAIR_FAC_DAT T1 ");
            stb.append("     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.FACCD = '" + printCd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CHAIRCD ");
        }
        return stb.toString();
    }

    private String sqlSchDat(final String chairCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH PERIOD AS ( ");
        stb.append("     SELECT ");
        stb.append("         NAMECD2 AS PERIODCD, ");
        stb.append("         ABBV1 AS PERIOD_ABBV, ");
        stb.append("         NAMESPARE2 ");
        stb.append("     FROM ");
        stb.append("         V_NAME_MST ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' ");
        stb.append("         AND NAMECD1 = 'B001' ");
        stb.append(" ) ");

        stb.append(" SELECT DISTINCT ");
        if (_param._isKihon) {
            stb.append("     T1.DAYCD AS DAYCD, ");
        } else {
            stb.append("     DAYOFWEEK(T1.EXECUTEDATE) AS DAYCD, ");
        }
        stb.append("     T1.PERIODCD, ");
        stb.append("     P1.PERIOD_ABBV ");
        stb.append(" FROM ");
        if (_param._isKihon) {
            stb.append("     SCH_PTRN_DAT T1 ");
        } else {
            stb.append("     SCH_CHR_DAT T1 ");
        }
        stb.append("     INNER JOIN PERIOD P1 ON P1.PERIODCD = T1.PERIODCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        if (_param._isKihon) {
            stb.append("     AND T1.BSCSEQ = " + _param._seq + " ");
        } else {
            stb.append("     AND T1.EXECUTEDATE BETWEEN '"+_param._sDate+"' AND '"+_param._eDate+"' ");
        }
        stb.append("     AND T1.CHAIRCD = '" + chairCd + "' ");
        stb.append("     AND T1.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD WHERE NAMESPARE2 IS NOT NULL) ");
        stb.append(" ORDER BY ");
        stb.append("     DAYCD, ");
        stb.append("     T1.PERIODCD ");
        return stb.toString();
    }

    private String sqlChairStfFac(final String chairCd) {
        final StringBuffer stb = new StringBuffer();
        if (_param._isSeito) {
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     S1.STAFFCD, ");
            stb.append("     VALUE(S1.STAFFNAME_SHOW,'') AS STAFFNAME_SHOW, ");
            stb.append("     F1.FACCD, ");
            stb.append("     VALUE(F1.FACILITYABBV,'') AS FACILITYABBV ");
            stb.append(" FROM ");
            stb.append("     CHAIR_DAT T1 ");
            stb.append("     LEFT JOIN ( ");
            stb.append("         SELECT ");
            stb.append("             T1.CHAIRCD, ");
            stb.append("             T1.STAFFCD, ");
            stb.append("             T2.STAFFNAME_SHOW ");
            stb.append("         FROM ");
            stb.append("             CHAIR_STF_DAT T1 ");
            stb.append("             INNER JOIN V_STAFF_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("                 AND T2.STAFFCD = T1.STAFFCD ");
            stb.append("         WHERE ");
            stb.append("             T1.YEAR = '" + _param._year + "' ");
            stb.append("             AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("             AND T1.CHAIRCD = '" + chairCd + "' ");
            stb.append("     ) S1 ON S1.CHAIRCD = T1.CHAIRCD ");
            stb.append("     LEFT JOIN ( ");
            stb.append("         SELECT ");
            stb.append("             T1.CHAIRCD, ");
            stb.append("             T1.FACCD, ");
            stb.append("             T2.FACILITYABBV ");
            stb.append("         FROM ");
            stb.append("             CHAIR_FAC_DAT T1 ");
            stb.append("             INNER JOIN V_FACILITY_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("                 AND T2.FACCD = T1.FACCD ");
            stb.append("         WHERE ");
            stb.append("             T1.YEAR = '" + _param._year + "' ");
            stb.append("             AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("             AND T1.CHAIRCD = '" + chairCd + "' ");
            stb.append("     ) F1 ON F1.CHAIRCD = T1.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.CHAIRCD = '" + chairCd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     S1.STAFFCD, ");
            stb.append("     F1.FACCD ");
        }
        if (_param._isShokuin) {
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     F1.FACCD, ");
            stb.append("     VALUE(F1.FACILITYABBV,'') AS FACILITYABBV ");
            stb.append(" FROM ");
            stb.append("     CHAIR_DAT T1 ");
            stb.append("     LEFT JOIN ( ");
            stb.append("         SELECT ");
            stb.append("             T1.CHAIRCD, ");
            stb.append("             T1.FACCD, ");
            stb.append("             T2.FACILITYABBV ");
            stb.append("         FROM ");
            stb.append("             CHAIR_FAC_DAT T1 ");
            stb.append("             INNER JOIN V_FACILITY_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("                 AND T2.FACCD = T1.FACCD ");
            stb.append("         WHERE ");
            stb.append("             T1.YEAR = '" + _param._year + "' ");
            stb.append("             AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("             AND T1.CHAIRCD = '" + chairCd + "' ");
            stb.append("     ) F1 ON F1.CHAIRCD = T1.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.CHAIRCD = '" + chairCd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     F1.FACCD ");
        }
        if (_param._isSisetu) {
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     S1.STAFFCD, ");
            stb.append("     VALUE(S1.STAFFNAME_SHOW,'') AS STAFFNAME_SHOW ");
            stb.append(" FROM ");
            stb.append("     CHAIR_DAT T1 ");
            stb.append("     LEFT JOIN ( ");
            stb.append("         SELECT ");
            stb.append("             T1.CHAIRCD, ");
            stb.append("             T1.STAFFCD, ");
            stb.append("             T2.STAFFNAME_SHOW ");
            stb.append("         FROM ");
            stb.append("             CHAIR_STF_DAT T1 ");
            stb.append("             INNER JOIN V_STAFF_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("                 AND T2.STAFFCD = T1.STAFFCD ");
            stb.append("         WHERE ");
            stb.append("             T1.YEAR = '" + _param._year + "' ");
            stb.append("             AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("             AND T1.CHAIRCD = '" + chairCd + "' ");
            stb.append("     ) S1 ON S1.CHAIRCD = T1.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.CHAIRCD = '" + chairCd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     S1.STAFFCD ");
        }
        return stb.toString();
    }

    private String sqlChairCls(final String chairCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T3.GRADE, ");
        stb.append("     T3.HR_CLASS, ");
        stb.append("     T3.HR_NAMEABBV ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1 ");
        stb.append("     INNER JOIN CHAIR_CLS_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T2.GROUPCD = T1.GROUPCD ");
        stb.append("         AND (T2.CHAIRCD = T1.CHAIRCD OR T2.CHAIRCD = '0000000') ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T2.YEAR ");
        stb.append("         AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("         AND T3.GRADE = T2.TRGTGRADE ");
        stb.append("         AND T3.HR_CLASS = T2.TRGTCLASS ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.CHAIRCD = '" + chairCd + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T3.GRADE, ");
        stb.append("     T3.HR_CLASS ");
        return stb.toString();
    }

    private String sqlStudentCnt(final String chairCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     COUNT(DISTINCT T1.SCHREGNO) AS STUDENT_CNT ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STD_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.CHAIRCD = '" + chairCd + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.CHAIRCD ");
        return stb.toString();
    }

    private String sqlCredits(final String printCd, final String chairCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     MIN(T4.CREDITS) AS MIN_CREDITS, ");
        stb.append("     MAX(T4.CREDITS) AS MAX_CREDITS ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T3.CHAIRCD = T1.CHAIRCD ");
        stb.append("     INNER JOIN CREDIT_MST T4 ON T4.YEAR = T2.YEAR ");
        stb.append("         AND T4.COURSECD = T2.COURSECD ");
        stb.append("         AND T4.MAJORCD = T2.MAJORCD ");
        stb.append("         AND T4.GRADE = T2.GRADE ");
        stb.append("         AND T4.COURSECODE = T2.COURSECODE ");
        stb.append("         AND T4.CLASSCD = T3.CLASSCD ");
        stb.append("         AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("         AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("         AND T4.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.CHAIRCD = '" + chairCd + "' ");
        if (_param._isSeito) {
            stb.append("     AND T1.SCHREGNO = '" + printCd + "' ");
        }
        stb.append(" GROUP BY ");
        stb.append("     T1.CHAIRCD ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * パラメータクラス
     */
    private class Param {
        //時間割種別・指定日付 1:基本時間割 2:通常時間割
        final private boolean _isKihon;
        //基本時間割
        private String _seq;
        private String _semester;
        //通常時間割
        private String _date;
        //帳票区分 1:生徒 2:職員 3:施設
        final private boolean _isSeito;
        final private boolean _isShokuin;
        final private boolean _isSisetu;
        //年組cd
        final String _gradeHrClass1;
        final String _gradeHrClass2;
        //所属cd
        final String _sectionCdName1;
        final String _sectionCdName2;
        //施設cd
        final String _faccdName1;
        final String _faccdName2;
        //その他
        private String _ctrlYear;
        private String _ctrlSeme;
        private String _ctrlDate;
        //年度・週開始日・週終了日
        private String _year;
        private String _sDate;
        private String _eDate;
        private String _sDateWeek;
        private String _eDateWeek;
        //基本・タイトル
        private String _titleKihon;
        //校時のカウント
        private int _cntPeriod;
        private final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _isKihon = "1".equals(request.getParameter("RADIO"));
            if (_isKihon) {
                _year = request.getParameter("T_YEAR");
                _seq = request.getParameter("T_BSCSEQ");
                _semester = request.getParameter("T_SEMESTER");
            } else {
                final String date = request.getParameter("DATE");
                _date = date.substring(0,4) + "-" + date.substring(5,7) + "-" + date.substring(8);
                setYear();
            }
            final String kubun = request.getParameter("KUBUN");
            _isSeito = "1".equals(kubun);
            _isShokuin = "2".equals(kubun);
            _isSisetu = "3".equals(kubun);
            _gradeHrClass1 = request.getParameter("GRADE_HR_CLASS1");
            _gradeHrClass2 = request.getParameter("GRADE_HR_CLASS2");
            _sectionCdName1 = request.getParameter("SECTION_CD_NAME1");
            _sectionCdName2 = request.getParameter("SECTION_CD_NAME2");
            _faccdName1 = request.getParameter("FACCD_NAME1");
            _faccdName2 = request.getParameter("FACCD_NAME2");

            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSeme = request.getParameter("CTRL_SEMESTER");
            final String ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlDate = ctrlDate.substring(0,4) + "-" + ctrlDate.substring(5,7) + "-" + ctrlDate.substring(8);
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            if (_isKihon) {
                loadTitleKihon(db2);
            } else {
                loadSemesterTuujou(db2);
            }
        }

        private void setYear() {
            try {
                //年度
                int nen  = Integer.parseInt(_date.substring(0,4));
                int tuki = Integer.parseInt(_date.substring(5,7));
                int hi   = Integer.parseInt(_date.substring(8));
                _year = (tuki <= 3) ? String.valueOf(nen - 1) : String.valueOf(nen);
                //週開始日を取得
                Calendar cals = Calendar.getInstance();
                cals.set(nen,tuki-1,hi);
                while(cals.get(Calendar.DAY_OF_WEEK) != 2){
                    cals.add(Calendar.DATE,-1);
                }
                nen  = cals.get(Calendar.YEAR);
                tuki = cals.get(Calendar.MONTH);
                tuki++;
                hi   = cals.get(Calendar.DATE);
                _sDate = Integer.toString(nen) + "-" + h_tuki(tuki) + "-" + h_tuki(hi);
                _sDateWeek = _sDate;
                //週終了日の取得
                cals.add(Calendar.DATE,+6);
                nen  = cals.get(Calendar.YEAR);
                tuki = cals.get(Calendar.MONTH);
                tuki++;
                hi   = cals.get(Calendar.DATE);
                _eDate = Integer.toString(nen) + "-" + h_tuki(tuki) + "-" + h_tuki(hi);
                _eDateWeek = _eDate;
            } catch (final Exception ex) {
                log.error("年度・週開始日・週終了日の取得でエラー:", ex);
            } finally {
            }
            log.debug("年度:" + _year);
            log.debug("週開始日:" + _sDateWeek);
            log.debug("週終了日:" + _eDateWeek);
        }

        private String h_tuki(int intx) {
            String strx = null;
            try {
                strx = "00" + String.valueOf(intx);
                strx = strx.substring(strx.length()-2);
            } catch( Exception ex ) {
                log.debug("h_tuki error!", ex);
            }
            return strx;
        }

        private void loadTitleKihon(final DB2UDB db2) {
            _titleKihon = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT TITLE "
                             + "FROM SCH_PTRN_HDAT "
                             + "WHERE YEAR = '" + _year + "' AND BSCSEQ = " + _seq + " AND SEMESTER = '" + _semester + "'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _titleKihon = rs.getString("TITLE");
                }
            } catch (final Exception ex) {
                log.error("基本・タイトルのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("基本・タイトル:" + _titleKihon);
        }

        private void loadSemesterTuujou(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT SEMESTER "
                             + "      ,case when '"+_sDate+"' < SDATE then SDATE else null end as SDATE "
                             + "      ,case when '"+_eDate+"' > EDATE then EDATE else null end as EDATE "
                             + "FROM   SEMESTER_MST "
                             + "WHERE  SDATE <= date('"+_date+"') AND EDATE >= date('"+_date+"') AND YEAR = '"+_year+"' AND SEMESTER <> '9'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _semester = rs.getString("SEMESTER");
                    if (rs.getString("SDATE") != null) _sDate = rs.getString("SDATE"); //学期開始日
                    if (rs.getString("EDATE") != null) _eDate = rs.getString("EDATE"); //学期終了日
                }
            } catch (final Exception ex) {
                log.error("通常・学期のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("通常・学期:" + _semester);
            log.debug("開始日:" + _sDate);
            log.debug("終了日:" + _eDate);
        }
    }


}  //クラスの括り
