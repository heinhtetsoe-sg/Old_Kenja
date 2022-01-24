/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * 作成日: 2021/02/10
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJB103F {

    private static final Log log = LogFactory.getLog(KNJB103F.class);

    private static final String SEMEALL = "9";

    private static final int MAX_COLUMN = 5;

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
        final List testtimeList = getList(db2);

        if(testtimeList.size() > 0) {
            //時間割
            printSvfMain(db2, svf, testtimeList);
            svf.VrEndPage();
        }
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, List testtimeList) {
        final String form = "KNJB103F.frm";

        //ページリストの作成
        final List pageList = new ArrayList<List>();
        List<String> pageDateList = new ArrayList<String>();
        int cnt = 1;
        for (Iterator it = _param._executeDateList.iterator(); it.hasNext();) {

            if(cnt > MAX_COLUMN) {
                pageList.add(pageDateList);
                pageDateList = new ArrayList<String>();
                cnt = 1;
            }
            final String dateList = (String) it.next();
            pageDateList.add(dateList);
            cnt++;
        }
        pageList.add(pageDateList);

        for (Iterator itPage = pageList.iterator(); itPage.hasNext();) {
            final List dateList = (List) itPage.next();
            svf.VrSetForm(form , 4);
            //明細部以外を印字
            printTitle(db2, svf);

            //時限
            for (Iterator iterator = testtimeList.iterator(); iterator.hasNext();) {
                final Testtime testtime = (Testtime) iterator.next();

                if("Z".equals(testtime._periodcd)) {
                    //礼拝
                    svf.VrsOut("PERIOD_NAME2", ""); //時限
                    svf.VrsOut("TIME2_1", testtime._starttime_hour + "：" + testtime._starttime_minute); //開始時間
                    svf.VrsOut("TIME2_2", testtime._endtime_hour + "：" + testtime._endtime_minute); //終了時間
                    svf.VrsOut("PRAY", "礼拝"); //科目名

                } else {
                    //礼拝以外
                    svf.VrsOut("PERIOD_NAME1", testtime._periodname); //時限
                    svf.VrsOut("TIME1_1", testtime._starttime_hour + "：" + testtime._starttime_minute); //開始時間
                    svf.VrsOut("TIME1_2", testtime._endtime_hour + "：" + testtime._endtime_minute); //終了時間

                    //日付
                    int column = 1;
                    for (Iterator itDate = dateList.iterator(); itDate.hasNext();) {
                        final String date = (String) itDate.next();
                        if(testtime._testtimeDateMap.containsKey(date)) {
                            final TesttimeDate testtimeDate = (TesttimeDate) testtime._testtimeDateMap.get(date);
                            svf.VrsOut("DAY" + column, KNJ_EditDate.h_format_JP_MD(testtimeDate._executedate) + "（" + KNJ_EditDate.h_format_W(testtimeDate._executedate) + "）"); //日付

                            //科目
                            int line = (testtimeDate._testtimeSubclassList.size() > 1) ? 1 : 2; // 科目名の印字位置  2科目以上の場合：上段から下段 , 1科目の場合：中央の段
                            for (Iterator itSubclass = testtimeDate._testtimeSubclassList.iterator(); itSubclass.hasNext();) {
                                final TesttimeSubclass testtimeSubclass = (TesttimeSubclass) itSubclass.next();

                                //科目名
                                final String field = KNJ_EditEdit.getMS932ByteLength(testtimeSubclass._subclassname) > 26 ? "2" : "1";
                                svf.VrsOutn("SUBCLASS_NAME" + column + "_" + field, line, testtimeSubclass._subclassname);

                                //選択
                                if("1".equals(testtimeSubclass._electdiv)) {
                                    svf.VrsOutn("SELECT" + column, line, "選択");
                                }
                                line++;
                            }
                        }
                        column++;
                    }
                }
                svf.VrEndRecord();
                _hasData = true;
            }
            svf.VrEndPage();
        }
    }

    private void printTitle(final DB2UDB db2, final Vrw32alp svf) {
        //明細部以外を印字
        final String title = _param._gradeName + "　" + _param._nendo + "（" + _param._testitemName + "）時間割";
        svf.VrsOut("TITLE", title); //タイトル
        svf.VrsOut("PRINT_DATE", "出力日：" + _param._ctrlDate); //印刷日
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final String[] value) {
        if (null != value) {
            for (int i = 0 ; i < value.length; i++) {
                svf.VrsOutn(field, i + 1, value[i]);
            }
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getTesttimeSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Testtime testtime = new Testtime();
                testtime._testkindcd = StringUtils.defaultString(rs.getString("TESTKINDCD"));
                testtime._testitemcd = StringUtils.defaultString(rs.getString("TESTITEMCD"));
                testtime._periodcd = StringUtils.defaultString(rs.getString("PERIODCD"));
                testtime._periodname = StringUtils.defaultString(rs.getString("PERIODNAME"));
                testtime._starttime_hour = StringUtils.defaultString(rs.getString("STARTTIME_HOUR"));
                testtime._starttime_minute = StringUtils.defaultString(rs.getString("STARTTIME_MINUTE"));
                testtime._endtime_hour = StringUtils.defaultString(rs.getString("ENDTIME_HOUR"));
                testtime._endtime_minute = StringUtils.defaultString(rs.getString("ENDTIME_MINUTE"));
                testtime.setTesttimeDate(db2);
                retList.add(testtime);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getTesttimeSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.TESTKINDCD, ");
        stb.append("   T1.TESTITEMCD, ");
        stb.append("   T1.PERIODCD, ");
        stb.append("   B001.NAME3 AS PERIODNAME, ");
        stb.append("   T1.STARTTIME_HOUR, ");
        stb.append("   T1.STARTTIME_MINUTE, ");
        stb.append("   T1.ENDTIME_HOUR, ");
        stb.append("   T1.ENDTIME_MINUTE ");
        stb.append(" FROM ");
        stb.append("   SCH_TESTTIME_DAT T1 ");
        stb.append("   LEFT JOIN NAME_MST B001 ");
        stb.append("          ON B001.NAMECD2 = T1.PERIODCD ");
        stb.append("         AND B001.NAMECD1 = 'B001' ");
        stb.append(" WHERE ");
        stb.append("       T1.YEAR       = '" + _param._ctrlYear + "' ");
        stb.append("   AND T1.SEMESTER   = '" + _param._semester + "' ");
        stb.append("   AND T1.GRADE      = '" + _param._grade + "' ");
        stb.append("   AND T1.TESTKINDCD = '" + _param._testkindcd + "' ");
        stb.append("   AND T1.TESTITEMCD = '" + _param._testitemcd + "' ");
        stb.append(" ORDER BY ");
        stb.append("   T1.STARTTIME_HOUR, ");
        stb.append("   T1.STARTTIME_MINUTE ");
        return stb.toString();
    }

    private class Testtime {
        String _testkindcd;
        String _testitemcd;
        String _periodcd;
        String _periodname;
        String _starttime_hour;
        String _starttime_minute;
        String _endtime_hour;
        String _endtime_minute;
        final Map _testtimeDateMap = new HashMap();

        private void setTesttimeDate(final DB2UDB db2) {
            final String sql = getTesttimeDate();
            log.fatal(" getTesttimeDate = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String periodcd = StringUtils.defaultString(rs.getString("PERIODCD"));
                    final String starttime_hour = StringUtils.defaultString(rs.getString("STARTTIME_HOUR"));
                    final String starttime_minute = StringUtils.defaultString(rs.getString("STARTTIME_MINUTE"));
                    final String executedate = StringUtils.defaultString(rs.getString("EXECUTEDATE"));
                    final TesttimeDate testtimeSubclass = new TesttimeDate(periodcd, starttime_hour, starttime_minute, executedate);
                    testtimeSubclass.setTesttimeDate(db2, _param);
                    _testtimeDateMap.put(executedate, testtimeSubclass);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private String getTesttimeDate() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCH_CHAIR AS ( ");
            //対象学年の生徒が受講する講座
            stb.append(" SELECT DISTINCT ");
            stb.append("     CHR.YEAR, ");
            stb.append("     CHR.SEMESTER, ");
            stb.append("     CHR.CHAIRCD, ");
            stb.append("     CHR.CLASSCD, ");
            stb.append("     CHR.SCHOOL_KIND, ");
            stb.append("     CHR.CURRICULUM_CD, ");
            stb.append("     CHR.SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT STD ");
            stb.append("     INNER JOIN CHAIR_DAT CHR ");
            stb.append("             ON CHR.YEAR     = STD.YEAR ");
            stb.append("            AND CHR.SEMESTER = STD.SEMESTER ");
            stb.append("            AND CHR.CHAIRCD  = STD.CHAIRCD ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ");
            stb.append("             ON REGD.YEAR     = STD.YEAR ");
            stb.append("            AND REGD.SEMESTER = STD.SEMESTER ");
            stb.append("            AND REGD.SCHREGNO = STD.SCHREGNO ");
            stb.append("            AND REGD.GRADE    = '"+ _param._grade +"' ");
            stb.append(" WHERE ");
            stb.append("       STD.YEAR       = '"+ _param._ctrlYear +"' ");
            stb.append("   AND STD.SEMESTER   = '"+ _param._semester  +"' ");
            stb.append(" ) ");
            //メイン
            stb.append(" SELECT DISTINCT ");
            stb.append("   T2.PERIODCD, ");
            stb.append("   T1.STARTTIME_HOUR, ");
            stb.append("   T1.STARTTIME_MINUTE, ");
            stb.append("   T2.EXECUTEDATE ");
            stb.append(" FROM ");
            stb.append("   SCH_TESTTIME_DAT T1 ");
            stb.append("   INNER JOIN SCH_CHR_TEST T2 ");
            stb.append("           ON T2.YEAR       = T1.YEAR ");
            stb.append("          AND T2.SEMESTER   = T1.SEMESTER ");
            stb.append("          AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("          AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("          AND T2.PERIODCD   = T1.PERIODCD ");
            stb.append("   INNER JOIN SCH_CHAIR CHAIR ");
            stb.append("           ON CHAIR.CHAIRCD  = T2.CHAIRCD ");
            stb.append("   INNER JOIN CLASS_MST CLASS ");
            stb.append("           ON CLASS.CLASSCD     = CHAIR.CLASSCD ");
            stb.append("          AND CLASS.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("   INNER JOIN SUBCLASS_MST SUBM ");
            stb.append("           ON SUBM.CLASSCD       = CHAIR.CLASSCD ");
            stb.append("          AND SUBM.SCHOOL_KIND   = CHAIR.SCHOOL_KIND ");
            stb.append("          AND SUBM.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            stb.append("          AND SUBM.SUBCLASSCD    = CHAIR.SUBCLASSCD ");
            stb.append("   INNER JOIN SUBCLASS_YDAT SUBY ");
            stb.append("          ON SUBY.YEAR          = CHAIR.YEAR ");
            stb.append("         AND SUBY.SUBCLASSCD    = SUBM.SUBCLASSCD ");
            stb.append("         AND SUBY.CLASSCD       = SUBM.CLASSCD ");
            stb.append("         AND SUBY.SCHOOL_KIND   = SUBM.SCHOOL_KIND ");
            stb.append("         AND SUBY.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            stb.append(" WHERE ");
            stb.append("       T1.YEAR       = '"+ _param._ctrlYear +"' ");
            stb.append("   AND T1.SEMESTER   = '"+ _param._semester  +"' ");
            stb.append("   AND T1.GRADE      = '"+ _param._grade +"' ");
            stb.append("   AND T1.TESTKINDCD = '"+ _param._testkindcd +"' ");
            stb.append("   AND T1.TESTITEMCD = '"+ _param._testitemcd +"' ");
            stb.append("   AND T1.PERIODCD   = '"+ _periodcd +"' ");
            stb.append(" ORDER BY ");
            stb.append("   T1.STARTTIME_HOUR, ");
            stb.append("   T1.STARTTIME_MINUTE, ");
            stb.append("   T2.EXECUTEDATE ");


            return stb.toString();
        }
    }

    private static class TesttimeDate {
        final String _periodcd;
        final String _starttime_hour;
        final String _starttime_minute;
        final String _executedate;
        final List _testtimeSubclassList = new ArrayList();

        private TesttimeDate(
                final String periodcd,
                final String starttime_hour,
                final String starttime_minute,
                final String executedate
        ) {
            _periodcd = periodcd;
            _starttime_hour = starttime_hour;
            _starttime_minute = starttime_minute;
            _executedate = executedate;
        }

        private void setTesttimeDate(final DB2UDB db2, Param _param) {
            final String sql = getTesttimeSubclass(_param);
            log.fatal(" getTesttimeSubclass = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String periodcd = StringUtils.defaultString(rs.getString("PERIODCD"));
                    final String executedate = StringUtils.defaultString(rs.getString("EXECUTEDATE"));
                    final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final String electdiv = StringUtils.defaultString(rs.getString("ELECTDIV"));
                    final TesttimeSubclass testtimeSubclass = new TesttimeSubclass(periodcd, executedate, subclasscd, subclassname, electdiv);
                    _testtimeSubclassList.add(testtimeSubclass);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private String getTesttimeSubclass(Param _param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCH_CHAIR AS ( ");
            //対象学年の生徒が受講する講座
            stb.append(" SELECT DISTINCT ");
            stb.append("     CHR.YEAR, ");
            stb.append("     CHR.SEMESTER, ");
            stb.append("     CHR.CHAIRCD, ");
            stb.append("     CHR.CLASSCD, ");
            stb.append("     CHR.SCHOOL_KIND, ");
            stb.append("     CHR.CURRICULUM_CD, ");
            stb.append("     CHR.SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT STD ");
            stb.append("     INNER JOIN CHAIR_DAT CHR ");
            stb.append("             ON CHR.YEAR     = STD.YEAR ");
            stb.append("            AND CHR.SEMESTER = STD.SEMESTER ");
            stb.append("            AND CHR.CHAIRCD  = STD.CHAIRCD ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ");
            stb.append("             ON REGD.YEAR     = STD.YEAR ");
            stb.append("            AND REGD.SEMESTER = STD.SEMESTER ");
            stb.append("            AND REGD.SCHREGNO = STD.SCHREGNO ");
            stb.append("            AND REGD.GRADE    = '"+_param._grade +"' ");
            stb.append(" WHERE ");
            stb.append("       STD.YEAR     = '"+ _param._ctrlYear +"' ");
            stb.append("   AND STD.SEMESTER = '"+ _param._semester +"' ");
            stb.append(" ) ");
            //メイン
            stb.append(" SELECT DISTINCT ");
            stb.append("   T1.PERIODCD, ");
            stb.append("   T1.EXECUTEDATE, ");
            stb.append("   CHAIR.SUBCLASSCD, ");
            stb.append("   SUBM.SUBCLASSNAME, ");
            stb.append("   SUBM.ELECTDIV ");
            stb.append(" FROM ");
            stb.append("   SCH_CHR_TEST T1 ");
            stb.append("   INNER JOIN SCH_CHAIR CHAIR ");
            stb.append("           ON CHAIR.CHAIRCD  = T1.CHAIRCD ");
            stb.append("   INNER JOIN CLASS_MST CLASS ");
            stb.append("           ON CLASS.CLASSCD     = CHAIR.CLASSCD ");
            stb.append("          AND CLASS.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("   INNER JOIN SUBCLASS_MST SUBM ");
            stb.append("           ON SUBM.CLASSCD       = CHAIR.CLASSCD ");
            stb.append("          AND SUBM.SCHOOL_KIND   = CHAIR.SCHOOL_KIND ");
            stb.append("          AND SUBM.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            stb.append("          AND SUBM.SUBCLASSCD    = CHAIR.SUBCLASSCD ");
            stb.append("   INNER JOIN SUBCLASS_YDAT SUBY ");
            stb.append("          ON SUBY.YEAR          = CHAIR.YEAR ");
            stb.append("         AND SUBY.SUBCLASSCD    = SUBM.SUBCLASSCD ");
            stb.append("         AND SUBY.CLASSCD       = SUBM.CLASSCD ");
            stb.append("         AND SUBY.SCHOOL_KIND   = SUBM.SCHOOL_KIND ");
            stb.append("         AND SUBY.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            stb.append(" WHERE ");
            stb.append("       T1.EXECUTEDATE = '"+ _executedate +"' ");
            stb.append("   AND T1.PERIODCD    = '"+ _periodcd +"' ");
            stb.append(" ORDER BY ");
            stb.append("   CHAIR.SUBCLASSCD ");
            return stb.toString();
        }
    }


    private static class TesttimeSubclass {
        final String _periodcd;
        final String _executedate;
        final String _subclasscd;
        final String _subclassname;
        final String _electdiv;

        private TesttimeSubclass(
                final String periodcd,
                final String executedate,
                final String subclasscd,
                final String subclassname,
                final String electdiv
        ) {
            _periodcd = periodcd;
            _executedate = executedate;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _electdiv = electdiv;
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
        final String _grade;
        final String _semester;
        final String _testkindcd;
        final String _testitemcd;

        final String _prgid;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;

        final String _nendo;
        final String _gradeName;
        final String _testitemName;
        final List _executeDateList;
        final String _schoolKind;
        final String _schoolKindName;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;

        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _backSlashImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolStampPath;

        private boolean _isOutputDebug;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _grade = request.getParameter("GRADE");
            _semester = request.getParameter("SEMESTER");
            final String testcd = request.getParameter("TESTCD");
            final String[] wkTestcd = testcd.split("-");
            _testkindcd = wkTestcd[1];
            _testitemcd = wkTestcd[2];

            _prgid = request.getParameter("PRGID");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE").replace('-', '/');

            _nendo = _ctrlYear + "年度";
            _gradeName = getGradeName(db2);
            _testitemName = getTestcdName(db2, testcd);
            _executeDateList = getExecuteDateList(db2);
            _schoolKind = getSchoolKind(db2);
            _schoolKindName = getSchoolKindName(db2);

            setCertifSchoolDat(db2);

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            _schoolStampPath = getImageFilePath("SCHOOLSTAMP.bmp");

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJB103F' AND NAME = '" + propName + "' "));
        }

        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _grade + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private String getSchoolKindName(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = 'A023' AND NAME1 = '" + _schoolKind + "' ");
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindcd = "J".equals(_schoolKind) ? "103" : "104";
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '"+ certifKindcd +"' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        //学年名称の取得
        private String getGradeName(DB2UDB db2) {
            String rtnStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   GRADE_NAME1 ");
                stb.append(" FROM ");
                stb.append("   SCHREG_REGD_GDAT ");
                stb.append(" WHERE ");
                stb.append("       YEAR     = '"+ _ctrlYear +"' ");
                stb.append("   AND GRADE    = '"+ _grade +"' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnStr = StringUtils.defaultString(rs.getString("GRADE_NAME1"));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnStr;
        }

        //考査名称の取得
        private String getTestcdName(DB2UDB db2, final String testcd) {
            String rtnStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   TESTITEMNAME ");
                stb.append(" FROM ");
                stb.append("   TESTITEM_MST_COUNTFLG_NEW_SDIV ");
                stb.append(" WHERE ");
                stb.append("       YEAR     = '"+ _ctrlYear +"' ");
                stb.append("   AND SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV = '"+ testcd +"' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnStr = StringUtils.defaultString(rs.getString("TESTITEMNAME"));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnStr;
        }


        //日付の取得
        private List getExecuteDateList(DB2UDB db2) {
            List rtnList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH SCH_CHAIR AS ( ");
                stb.append(" SELECT DISTINCT ");
                stb.append("     CHR.YEAR, ");
                stb.append("     CHR.SEMESTER, ");
                stb.append("     CHR.CHAIRCD, ");
                stb.append("     CHR.CLASSCD, ");
                stb.append("     CHR.SCHOOL_KIND, ");
                stb.append("     CHR.CURRICULUM_CD, ");
                stb.append("     CHR.SUBCLASSCD ");
                stb.append(" FROM ");
                stb.append("     CHAIR_STD_DAT STD ");
                stb.append("     INNER JOIN CHAIR_DAT CHR ");
                stb.append("             ON CHR.YEAR     = STD.YEAR ");
                stb.append("            AND CHR.SEMESTER = STD.SEMESTER ");
                stb.append("            AND CHR.CHAIRCD  = STD.CHAIRCD ");
                stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ");
                stb.append("             ON REGD.YEAR     = STD.YEAR ");
                stb.append("            AND REGD.SEMESTER = STD.SEMESTER ");
                stb.append("            AND REGD.SCHREGNO = STD.SCHREGNO ");
                stb.append("            AND REGD.GRADE    = '"+ _grade +"' ");
                stb.append(" WHERE ");
                stb.append("       STD.YEAR       = '"+ _ctrlYear +"' ");
                stb.append("   AND STD.SEMESTER   = '"+ _semester +"' ");
                stb.append(" ) ");
                stb.append(" SELECT DISTINCT ");
                stb.append("   T2.EXECUTEDATE ");
                stb.append(" FROM ");
                stb.append("   SCH_TESTTIME_DAT T1 ");
                stb.append("   INNER JOIN SCH_CHR_TEST T2 ");
                stb.append("           ON T2.YEAR       = T1.YEAR ");
                stb.append("          AND T2.SEMESTER   = T1.SEMESTER ");
                stb.append("          AND T2.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("          AND T2.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("          AND T2.PERIODCD   = T1.PERIODCD ");
                stb.append("   INNER JOIN SCH_CHAIR CHAIR ");
                stb.append("           ON CHAIR.CHAIRCD  = T2.CHAIRCD ");
                stb.append("   INNER JOIN CLASS_MST CLASS ");
                stb.append("           ON CLASS.CLASSCD     = CHAIR.CLASSCD ");
                stb.append("          AND CLASS.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
                stb.append("   INNER JOIN SUBCLASS_MST SUBM ");
                stb.append("           ON SUBM.CLASSCD       = CHAIR.CLASSCD ");
                stb.append("          AND SUBM.SCHOOL_KIND   = CHAIR.SCHOOL_KIND ");
                stb.append("          AND SUBM.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
                stb.append("          AND SUBM.SUBCLASSCD    = CHAIR.SUBCLASSCD ");
                stb.append("   INNER JOIN SUBCLASS_YDAT SUBY ");
                stb.append("          ON SUBY.YEAR          = CHAIR.YEAR ");
                stb.append("         AND SUBY.SUBCLASSCD    = SUBM.SUBCLASSCD ");
                stb.append("         AND SUBY.CLASSCD       = SUBM.CLASSCD ");
                stb.append("         AND SUBY.SCHOOL_KIND   = SUBM.SCHOOL_KIND ");
                stb.append("         AND SUBY.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
                stb.append(" WHERE ");
                stb.append("       T1.YEAR       = '"+ _ctrlYear +"' ");
                stb.append("   AND T1.SEMESTER   = '"+ _semester +"' ");
                stb.append("   AND T1.GRADE      = '"+ _grade +"' ");
                stb.append("   AND T1.TESTKINDCD = '"+ _testkindcd +"' ");
                stb.append("   AND T1.TESTITEMCD = '"+ _testitemcd +"' ");
                stb.append(" ORDER BY ");
                stb.append("   T2.EXECUTEDATE ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnList.add(StringUtils.defaultString(rs.getString("EXECUTEDATE")));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnList;
        }
    }
}
// eof
