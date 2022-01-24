/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 3a628f0f7b6d0e49b863cf4ee5024b3db7c98dea $
 *
 * 作成日: 2020/08/03
 * 作成者: ooshiro
 *
 * Copyright(C) 2004-2020 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJJ;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJJ144G {

    private static final Log log = LogFactory.getLog(KNJJ144G.class);

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
        final String form = "KNJJ144G.frm";
        svf.VrSetForm(form , 1);
        final int MAX_LINE = 2;
        final int MAX_RETU = 2;

        int line = 1;
        int idx = 1;
        final List studentList = getList(db2);
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            if(idx > MAX_RETU) {
                idx = 1;
                line++;
            }
            if(line > MAX_LINE) {
                svf.VrEndPage();
                svf.VrSetForm(form , 1);
                line = 1;
            }

            final Student student = (Student) iterator.next();

            svf.VrsOutn("NUM"+idx, line, student._numberOfTimes); //回数
            svf.VrsOutn("HR_NAME"+idx, line, student._hrName); //クラス
            //名前の桁数によって文字の大きさ変更
            final int keta1 = getMS932ByteLength(student._name);
            final String ketaName1 = keta1 <= 14 ? "_1" : keta1 <= 20 ? "_2" : keta1 <= 30 ? "_3" : "_4";
            svf.VrsOutn("NAME"+idx + ketaName1, line, student._name); //氏名
            svf.VrsOutn("DISTANCE"+idx, line, student._meters); //距離
            svf.VrsOutn("RANK"+idx, line, student._rank); //順位
            svf.VrsOutn("TIME"+idx, line, student._time); //タイム
            svf.VrsOutn("DATE"+idx, line, student._eventDate); //実施日
            //名前の桁数によって文字の大きさ変更
            final int keta2 = getMS932ByteLength(_param._certifSchoolPrincipalName);
            final String ketaName2 = keta2 <= 16 ? "_1" : keta2 <= 20 ? "_2" : keta2 <= 30 ? "_3" : "_4";
            svf.VrsOutn("PRINCIPAL_NAME"+idx + ketaName2, line, _param._certifSchoolPrincipalName); //校長名

            _hasData = true;
            idx++;
        }
        log.info(" SVF =" + svf.toString());
        svf.VrEndPage();
    }

    private static int getMS932ByteLength(final String s) {
        return KNJ_EditEdit.getMS932ByteLength(s);
    }

    private static String getFloatFormat(final float f) {
        if (f == (long)f) {
            return String.format("%.0f", f);
        }
        return String.format("%s", f);
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                student._schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));

                student._numberOfTimes = StringUtils.defaultString(rs.getString("NUMBER_OF_TIMES"));
                student._hrName = StringUtils.defaultString(rs.getString("HR_NAME"));
                student._name = StringUtils.defaultString(rs.getString("NAME"));
                //距離
                student._meters = getFloatFormat(rs.getFloat("MAN_METERS")) + "Km";
                if ("2".equals(rs.getString("SEX"))) {
                    student._meters = getFloatFormat(rs.getFloat("WOMEN_METERS")) + "Km";
                }
                //順位
                student._rank = StringUtils.defaultString(rs.getString("RANK")) + "位";
                //タイム
                String time = "";
                if (!"".equals(rs.getString("TIME_H")) && rs.getShort("TIME_H") > 0) {
                    time += String.format("%d", rs.getShort("TIME_H")) + "時間";
                }
                if (!"".equals(rs.getString("TIME_M"))) {
                    time += String.format("%02d", rs.getShort("TIME_M")) + "分";
                }
                if (!"".equals(rs.getString("TIME_S"))) {
                    time += String.format("%02d", rs.getShort("TIME_S")) + "秒";
                }
                student._time = time;
                //実施日
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                student._eventDate = StringUtils.defaultString(sdf.format(rs.getDate("EVENT_DATE")));

                retList.add(student);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("   T1.YEAR ");
        stb.append("   , T1.SEQ ");
        stb.append("   , T1.SCHREGNO ");
        stb.append("   , REGDB.NAME ");
        stb.append("   , T2.NUMBER_OF_TIMES ");
        stb.append("   , T2.EVENT_NAME ");
        stb.append("   , T2.EVENT_DATE ");
        stb.append("   , T2.MAN_METERS ");
        stb.append("   , T2.WOMEN_METERS ");
        stb.append("   , T1.SEX ");
        stb.append("   , T1.TIME_H ");
        stb.append("   , T1.TIME_M ");
        stb.append("   , T1.TIME_S ");
        stb.append("   , T1.GRADE_RANK_SEX RANK ");
        stb.append("   , REGDH.HR_NAME ");
        stb.append("   , CER.PRINCIPAL_NAME ");
        stb.append(" FROM ");
        stb.append("   MARATHON_EVENT_RANK_DAT T1 ");
        stb.append("   INNER JOIN MARATHON_EVENT_MST T2 ");
        stb.append("     ON T1.YEAR = T2.YEAR ");
        stb.append("     AND T1.SEQ = T2.SEQ ");
        stb.append("   INNER JOIN SCHREG_REGD_DAT REGD ");
        stb.append("     ON T1.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     AND T1.YEAR = REGD.YEAR ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append("   INNER JOIN SCHREG_REGD_HDAT REGDH ");
        stb.append("     ON REGD.YEAR = REGDH.YEAR ");
        stb.append("     AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("     AND REGD.GRADE = REGDH.GRADE ");
        stb.append("     AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("   INNER JOIN SCHREG_BASE_MST REGDB ");
        stb.append("     ON REGDB.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN CERTIF_SCHOOL_DAT CER ");
        stb.append("     ON CER.YEAR = T1.YEAR ");
        stb.append("     AND CER.CERTIF_KINDCD = '101' ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEX = '" + _param._sex + "' ");
        stb.append("   AND (REGD.GRADE || '-' || REGD.HR_CLASS) IN " + _param._selectedIn);

        stb.append(" ORDER BY ");
        stb.append("   T1.YEAR ");
        stb.append("   , T1.SEQ ");
        stb.append("   , T1.GRADE_RANK_SEX ");
        stb.append("   , T1.SCHREGNO ");

        return stb.toString();
    }


    private class Student {
        String _schregno;
        String _numberOfTimes;
        String _hrName;
        String _name;
        String _meters;
        String _rank;
        String _time;
        String _eventDate;
    }


    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75789 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {

        final String _year;
        final String _semester;
        final String[] _categorySelected;
        final String _sex;
        private String _selectedIn = "";

        private String _certifSchoolPrincipalName;

        final String _documentroot;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {

            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _sex = request.getParameter("SEX");

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");

            _selectedIn = "(";
            for (int i = 0; i < _categorySelected.length; i++) {
                if (_categorySelected[i] == null)
                    break;
                if (i > 0) _selectedIn = _selectedIn + ",";
                _selectedIn = _selectedIn + "'" + _categorySelected[i] + "'";
            }
            _selectedIn = _selectedIn + ")";

            setCertifSchoolDat(db2);

            _documentroot = request.getParameter("DOCUMENTROOT");
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '101' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
        }
    }
}

// eof
