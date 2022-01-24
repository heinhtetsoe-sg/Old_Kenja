/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * 作成日: 2021/04/06
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJB104D {

    private static final Log log = LogFactory.getLog(KNJB104D.class);

    private boolean _hasData;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {

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
        final List facLayoutList = getList(db2);
        for (Iterator iterator = facLayoutList.iterator(); iterator.hasNext();) {
            final FacLayout facLayout = (FacLayout) iterator.next();

            //帳票印刷
            printSvfMain(db2, svf, facLayout);
            svf.VrEndPage();

            _hasData = true;
        }
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final FacLayout facLayout) {
        final String form = "KNJB104D.frm";
        svf.VrSetForm(form, 4);

        //明細部以外を印字
        printTitle(db2, svf, facLayout);

        //明細
        int male = 0;
        int female = 0;
        int line = 1;
        int maxLine = Integer.parseInt(facLayout._rows);
        for (Iterator iterator = facLayout._facLayoutSchregList.iterator(); iterator.hasNext();) {
            final FacLayoutSchreg facLayoutSchreg = (FacLayoutSchreg) iterator.next();
            final String columns = String.valueOf(Integer.parseInt(facLayoutSchreg._columns));
            final String field = facLayout._columns + "_" + columns;

            //空行出力
            final int rows = Integer.parseInt(facLayoutSchreg._rows);
            for (; line < rows; line++) {
                svf.VrsOut("BLANK" + facLayout._columns, "BLANK");
                svf.VrEndRecord();
            }

            //座席番号
            svf.VrsOut("NO" + field, String.valueOf(Integer.parseInt(facLayoutSchreg._seatNo)));

            //年組番
            svf.VrsOut("HR_" + field, String.valueOf(Integer.parseInt(facLayoutSchreg._grade)) + "-" + String.valueOf(Integer.parseInt(facLayoutSchreg._hr_class)) + "-" + facLayoutSchreg._attendno);

            //氏名
            final String nameLengthField = KNJ_EditEdit.getMS932ByteLength(facLayoutSchreg._name) > 30 ? "_3" : KNJ_EditEdit.getMS932ByteLength(facLayoutSchreg._name) > 20 ? "_2" : "_1";
            svf.VrsOut("NAME" + field + nameLengthField, facLayoutSchreg._name);

            if("1".equals(facLayoutSchreg._sex)) {
                male++;
            } else {
                female++;
            }
        }

        //空行出力(最大行数になるよう)
        for (; line <= maxLine; line++) {
            svf.VrsOut("BLANK" + facLayout._columns, "BLANK");
            svf.VrEndRecord();
        }

        //フッタ
        svf.VrsOut("SUBCLASS_NAME", facLayout._subclassname); //科目名
        svf.VrsOut("CHAIR_NAME", facLayout._chairname); //講座名
        svf.VrsOut("HR_NAME", facLayout._hr_name); //年組
        svf.VrsOut("MALE", String.valueOf(male)); //男子
        svf.VrsOut("FEMALE", String.valueOf(female)); //女子
        svf.VrsOut("TOTAL", String.valueOf(male + female)); //合計
        svf.VrEndRecord();
    }

    private void printTitle(final DB2UDB db2, final Vrw32alp svf, final FacLayout facLayout) {
        //明細部以外を印字

        //ヘッダ
        svf.VrsOut("PRINT_DATE", KNJ_EditDate.getAutoFormatDate(db2, _param._loginDate)); //出力日
        final String title = _param._nendo + "（" + _param.getTestItemname(_param._testcd) + "）試験座席表";
        svf.VrsOut("TITLE", title); //タイトル
        final String nameField = KNJ_EditEdit.getMS932ByteLength(facLayout._facilityname) > 20 ? "_2" : "_1";
        svf.VrsOut("FACILITY_NAME1" + nameField , facLayout._facilityname); //施設名
        String execDate = KNJ_EditDate.getAutoFormatDate(db2, facLayout._executedate);
        execDate += "（" + KNJ_EditDate.h_format_W(facLayout._executedate) + "）" ;
        execDate += facLayout._periodname;
        svf.VrsOut("EXEC_DATE", execDate); //時間割日付
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final String[] value) {
        if (null != value) {
            for (int i = 0; i < value.length; i++) {
                svf.VrsOutn(field, i + 1, value[i]);
            }
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getFacLayoutSql();
            log.fatal(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final FacLayout facLayout = new FacLayout();
                facLayout._executedate = StringUtils.defaultString(rs.getString("EXECUTEDATE"));
                facLayout._chaircd = StringUtils.defaultString(rs.getString("CHAIRCD"));
                facLayout._chairname = StringUtils.defaultString(rs.getString("CHAIRNAME"));
                facLayout._subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                facLayout._subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                facLayout._periodcd = StringUtils.defaultString(rs.getString("PERIODCD"));
                facLayout._periodname = StringUtils.defaultString(rs.getString("PERIODNAME"));
                facLayout._faccd = StringUtils.defaultString(rs.getString("FACCD"));
                facLayout._facilityname = StringUtils.defaultString(rs.getString("FACILITYNAME"));
                facLayout._hr_name = StringUtils.defaultString(rs.getString("HR_NAME"));
                facLayout._rows = StringUtils.defaultString(rs.getString("ROWS"));
                facLayout._columns = StringUtils.defaultString(rs.getString("COLUMNS"));

                facLayout.setSubclass(db2);
                retList.add(facLayout);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getFacLayoutSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T4.CHAIRCD, ");
        stb.append("     T4.CHAIRNAME AS CHAIRNAME, ");
        stb.append("     T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T5.SUBCLASSNAME, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     B004.NAME1 AS PERIODNAME, ");
        stb.append("     FAC_DAT.FACCD, ");
        stb.append("     FACILITY.FACILITYNAME, ");
        stb.append("     LISTAGG(T7.HR_NAME, '　') WITHIN GROUP(ORDER BY T1.EXECUTEDATE, T7.GRADE, T7.HR_CLASS) AS HR_NAME, ");
        stb.append("     FAC_HDAT.ROWS, ");
        stb.append("     FAC_HDAT.COLUMNS ");
        stb.append(" FROM ");
        stb.append("     SCH_CHR_TEST T1 ");
        stb.append("     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ");
        stb.append("             ON T2.YEAR       = T1.YEAR ");
        stb.append("            AND T2.SEMESTER   = T1.SEMESTER ");
        stb.append("            AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("            AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append("            AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '"+ _param._testcd +"' ");
        stb.append("     INNER JOIN CHAIR_DAT T4 ");
        stb.append("             ON T4.YEAR     = T1.YEAR ");
        stb.append("            AND T4.SEMESTER = T1.SEMESTER ");
        stb.append("            AND T4.CHAIRCD  = T1.CHAIRCD ");
        stb.append("     INNER JOIN SUBCLASS_MST T5 ");
        stb.append("             ON T5.CLASSCD       = T4.CLASSCD ");
        stb.append("            AND T5.SCHOOL_KIND   = T4.SCHOOL_KIND ");
        stb.append("            AND T5.CURRICULUM_CD = T4.CURRICULUM_CD ");
        stb.append("            AND T5.SUBCLASSCD    = T4.SUBCLASSCD ");
        stb.append("     INNER JOIN CHAIR_CLS_DAT AS T6 ");
        stb.append("             ON T6.YEAR     = T1.YEAR ");
        stb.append("            AND T6.SEMESTER = T1.SEMESTER ");
        stb.append("            AND T6.CHAIRCD  = T1.CHAIRCD ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T7 ");
        stb.append("             ON T7.YEAR     = T1.YEAR ");
        stb.append("            AND T7.SEMESTER = T1.SEMESTER ");
        stb.append("            AND T7.GRADE    = T6.TRGTGRADE ");
        stb.append("            AND T7.HR_CLASS = T6.TRGTCLASS ");
        stb.append("            AND T7.GRADE    = '"+ _param._grade +"' ");
        stb.append("     INNER JOIN CHAIR_TEST_FAC_DAT FAC_DAT ");
        stb.append("             ON FAC_DAT.YEAR     = T1.YEAR ");
        stb.append("            AND FAC_DAT.SEMESTER = T1.SEMESTER ");
        stb.append("            AND FAC_DAT.CHAIRCD  = T1.CHAIRCD ");
        stb.append("     INNER JOIN CHAIR_TEST_FAC_LAYOUT_HDAT FAC_HDAT ");
        stb.append("             ON FAC_HDAT.YEAR     = FAC_DAT.YEAR ");
        stb.append("            AND FAC_HDAT.SEMESTER = FAC_DAT.SEMESTER ");
        stb.append("            AND FAC_HDAT.CHAIRCD  = FAC_DAT.CHAIRCD ");
        stb.append("            AND FAC_HDAT.FACCD    = FAC_DAT.FACCD ");
        stb.append("     INNER JOIN (SELECT DISTINCT EXECUTEDATE, PERIODCD, CHAIRCD ");
        stb.append("                   FROM CHAIR_TEST_FAC_LAYOUT_SCHREG_DAT ");
        stb.append("                ) FAC_SCH ");
        stb.append("             ON FAC_SCH.EXECUTEDATE = T1.EXECUTEDATE ");
        stb.append("            AND FAC_SCH.PERIODCD    = T1.PERIODCD ");
        stb.append("            AND FAC_SCH.CHAIRCD     = T1.CHAIRCD ");
        stb.append("     LEFT JOIN V_NAME_MST B004 ");
        stb.append("            ON B004.YEAR    = T1.YEAR ");
        stb.append("           AND B004.NAMECD1 = 'B004' ");
        stb.append("           AND B004.NAMECD2 = T1.PERIODCD ");
        stb.append("     LEFT JOIN V_FACILITY_MST FACILITY ");
        stb.append("            ON FACILITY.YEAR  = FAC_DAT.YEAR ");
        stb.append("           AND FACILITY.FACCD = FAC_DAT.FACCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '"+ _param._loginYear +"' ");
        stb.append("     AND T1.SEMESTER = '"+ _param._semester +"' ");
        stb.append("     AND T1.EXECUTEDATE = '"+ _param._executedate +"' ");
        stb.append("     AND T1.CHAIRCD IN " + SQLUtils.whereIn(true, _param._categorySelected));
        if(!"ALL".equals(_param._periodcd)) {
            stb.append("     AND T1.PERIODCD = '"+ _param._periodcd +"' ");
        }
        stb.append(" GROUP BY ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T4.CHAIRCD, ");
        stb.append("     T4.CHAIRNAME, ");
        stb.append("     T5.CLASSCD, ");
        stb.append("     T5.SCHOOL_KIND, ");
        stb.append("     T5.CURRICULUM_CD, ");
        stb.append("     T5.SUBCLASSCD, ");
        stb.append("     T5.SUBCLASSNAME, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     B004.NAME1, ");
        stb.append("     FAC_DAT.FACCD, ");
        stb.append("     FACILITY.FACILITYNAME, ");
        stb.append("     FAC_HDAT.ROWS, ");
        stb.append("     FAC_HDAT.COLUMNS ");
        stb.append(" ORDER BY ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T4.CHAIRCD, ");
        stb.append("     T1.PERIODCD ");
        return stb.toString();
    }

    private class FacLayout {
        String _executedate;
        String _chaircd;
        String _chairname;
        String _subclasscd;
        String _subclassname;
        String _periodcd;
        String _periodname;
        String _faccd;
        String _facilityname;
        String _hr_name;
        String _rows;
        String _columns;
        List _facLayoutSchregList = new ArrayList();

        private void setSubclass(final DB2UDB db2) {
            final String sql = getFacLayoutSchregInfo();
            log.fatal(" getFacLayoutSchregInfo = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String executedate = StringUtils.defaultString(rs.getString("EXECUTEDATE"));
                    final String periodcd = StringUtils.defaultString(rs.getString("PERIODCD"));
                    final String chaircd = StringUtils.defaultString(rs.getString("CHAIRCD"));
                    final String columns = StringUtils.defaultString(rs.getString("COLUMNS"));
                    final String rows = StringUtils.defaultString(rs.getString("ROWS"));
                    final String seatNo= StringUtils.defaultString(rs.getString("SEAT_NO"));
                    final String grade = StringUtils.defaultString(rs.getString("GRADE"));
                    final String hr_class = StringUtils.defaultString(rs.getString("HR_CLASS"));
                    final String attendno = StringUtils.defaultString(rs.getString("ATTENDNO"));
                    final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                    final String name = StringUtils.defaultString(rs.getString("NAME"));
                    final String sex = StringUtils.defaultString(rs.getString("SEX"));
                    final FacLayoutSchreg facLayoutSchreg = new FacLayoutSchreg(executedate, periodcd, chaircd, columns, rows, seatNo, grade, hr_class, attendno, schregno, name, sex);
                    _facLayoutSchregList.add(facLayoutSchreg);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private String getFacLayoutSchregInfo() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   FAC_SCH.EXECUTEDATE, ");
            stb.append("   FAC_SCH.PERIODCD, ");
            stb.append("   FAC_SCH.CHAIRCD, ");
            stb.append("   FAC_SCH.COLUMNS,  ");
            stb.append("   FAC_SCH.ROWS, ");
            stb.append("   FAC_SCH.SEAT_NO, ");
            stb.append("   REGD.GRADE, ");
            stb.append("   REGD.HR_CLASS, ");
            stb.append("   REGD.ATTENDNO, ");
            stb.append("   REGD.SCHREGNO, ");
            stb.append("   BASE.NAME, ");
            stb.append("   BASE.SEX ");
            stb.append(" FROM  ");
            stb.append("   CHAIR_TEST_FAC_LAYOUT_SCHREG_DAT FAC_SCH ");
            stb.append("   INNER JOIN CHAIR_TEST_FAC_DAT FAC_DAT ");
            stb.append("           ON FAC_DAT.YEAR     = '"+ _param._loginYear +"' ");
            stb.append("          AND FAC_DAT.SEMESTER = '"+ _param._semester +"' ");
            stb.append("          AND FAC_DAT.CHAIRCD  = FAC_SCH.CHAIRCD ");
            stb.append("   INNER JOIN SCHREG_REGD_DAT REGD ");
            stb.append("           ON REGD.YEAR     = FAC_DAT.YEAR ");
            stb.append("          AND REGD.SEMESTER = FAC_DAT.SEMESTER ");
            stb.append("          AND REGD.SCHREGNO = FAC_SCH.SCHREGNO ");
            stb.append("          AND REGD.GRADE = '"+ _param._grade +"' ");
            stb.append("   INNER JOIN SCHREG_BASE_MST BASE ");
            stb.append("           ON BASE.SCHREGNO = FAC_SCH.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     FAC_SCH.EXECUTEDATE  = '"+ _executedate +"' ");
            stb.append("     AND FAC_SCH.PERIODCD = '"+ _periodcd +"' ");
            stb.append("     AND FAC_SCH.CHAIRCD  = '"+ _chaircd +"' ");
            stb.append(" ORDER BY  ");
            stb.append("   FAC_SCH.ROWS, ");
            stb.append("   FAC_SCH.COLUMNS ");
            return stb.toString();
        }

    }

    private static class FacLayoutSchreg {
        final String _executedate;
        final String _periodcd;
        final String _chaircd;
        final String _columns;
        final String _rows;
        final String _seatNo;
        final String _grade;
        final String _hr_class;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _sex;

        private FacLayoutSchreg(
                final String executedate,
                final String periodcd,
                final String chaircd,
                final String columns,
                final String rows,
                final String seatNo,
                final String grade,
                final String hr_class,
                final String attendno,
                final String schregno,
                final String name,
                final String sex
        ) {
            _executedate = executedate;
            _periodcd = periodcd;
            _chaircd = chaircd;
            _columns = columns;
            _rows = rows;
            _seatNo = seatNo;
            _grade = grade;
            _hr_class = hr_class;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _sex = sex;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75874 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {

        final String[] _categorySelected;
        final String _semester;
        final String _grade;
        final String _testcd;
        final String _executedate;
        final String _periodcd;

        final String _prgid;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final String _nendo;
        final String _schoolKind;
        final String _schoolKindName;
        private final Map _testItemMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _testcd = request.getParameter("TESTCD");
            _executedate = request.getParameter("EXECUTEDATE").replace('/', '-');
            _periodcd = request.getParameter("PERIODCD");

            _prgid = request.getParameter("PRGID");
            _loginYear = request.getParameter("CTRL_YEAR");
            _loginSemester = request.getParameter("CTRL_SEMESTER");
            _loginDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_loginYear)) + "年度";
            _schoolKind = getSchoolKind(db2);
            _schoolKindName = getSchoolKindName(db2);
            _testItemMap = settestItemMap(db2);
        }

        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _loginYear + "' AND GRADE = '" + _grade + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private String getSchoolKindName(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = 'A023' AND NAME1 = '" + _schoolKind + "' ");
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private Map settestItemMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTITEM, TESTITEMNAME ");
                sql.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ");
                sql.append(" WHERE YEAR = '" + _loginYear + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String testitem = rs.getString("TESTITEM");
                    final String testitemname = StringUtils.defaultString(rs.getString("TESTITEMNAME"));
                    if (!map.containsKey(testitem)) {
                        map.put(testitem, testitemname);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return map;
        }

        private String getTestItemname(final String testitem) {
            String rtnStr = "";
            final String key = StringUtils.remove(testitem, "-");
            if(_testItemMap.containsKey(key)) {
                rtnStr = (String) _testItemMap.get(key);
            }
            return rtnStr;
        }
    }
}

// eof
