/*
 * 作成日: 2020/09/14
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

public class KNJL414I {

    private static final Log log = LogFactory.getLog(KNJL414I.class);

    private boolean _hasData;

    private int MAX_LINE = 10;

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

        final List studentList = getList(db2);
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();

            //メイン
            printSvfMain(db2, svf, student);
            svf.VrEndPage();
        }
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        final String form = "KNJL414I.frm";
        svf.VrSetForm(form , 1);

        svf.VrsOut("DATE", _param._outputDate); //日付

        svf.VrsOut("FINSCHOOL_NAME", student._finschool_name + "長"); //出身学校

        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName); //学校名
        svf.VrsOut("PRINCIPAL_NAME", _param._certifSchoolPrincipalName); //校長名

        svf.VrsOut("TITLE", _param._documentTitle); //タイトル

        VrsOutnRenban(svf, "NOTE1", KNJ_EditEdit.get_token(_param._documentText, 76, 12)); //文言

        svf.VrsOut("NAME_TITLE", "児童氏名："); //氏名タイトル
        svf.VrsOut("NAME", student._name); //氏名
        final String examno = student._examno.substring(student._examno.length() - 4);
        svf.VrsOut("EXAM_NO", examno); //受験番号

        if(!student._birthday.equals("")) {
            final String[] birthday = student._birthday.split("-");
            svf.VrsOut("BIRTHDAY", birthday[0] + "年" + birthday[1] + "月" + birthday[2] + "日"); //生年月日
        }
        svf.VrsOut("DOC_NAME", "①小学校指導要録抄本（または写し）"); //送付希望書類

        final String[] date = _param._limitDate.split("-");
        final String limitDate = date[0] + "年" + date[1] + "月" + date[2] + "日";
        svf.VrsOut("LIMIT", limitDate + "までにお送りいただければ幸いです"); //送付期限


        svf.VrsOut("ZIP_NO", "〒 " + _param._certifSchoolSchoolZipCd); //郵便番号

        String addr = _param._certifSchoolSchoolAddr1 + _param._certifSchoolSchoolAddr2;
        addr = addr + "（TEL " + _param._certifSchoolSchoolTelNo;
        addr = addr + " FAX " + _param._certifSchoolSchoolFaxNo + "）";
        svf.VrsOut("ADDR1", addr); //住所

        svf.VrEndRecord();
        _hasData = true;
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
        final String sql = getStudnetSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
               final String examno = StringUtils.defaultString(rs.getString("EXAMNO"));
               final String name = StringUtils.defaultString(rs.getString("NAME"));
               final String birthday = StringUtils.defaultString(rs.getString("BIRTHDAY"));
               final String fs_cd = StringUtils.defaultString(rs.getString("FS_CD"));
               final String finschool_name = StringUtils.defaultString(rs.getString("FINSCHOOL_NAME"));
               final Student student = new Student(examno, name, birthday, fs_cd, finschool_name);
               retList.add(student);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudnetSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("   T1.FS_CD, ");
        stb.append("   T1.EXAMNO, ");
        stb.append("   T1.NAME, ");
        stb.append("   T1.BIRTHDAY, ");
        stb.append("   T1.FS_CD, ");
        stb.append("   FS.FINSCHOOL_NAME ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   INNER JOIN ENTEXAM_RECEPT_DAT T2 ");
        stb.append("           ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("          AND T2.APPLICANTDIV = T1.APPLICANTDIV  ");
        stb.append("          AND T2.EXAM_TYPE    = '1' ");
        stb.append("          AND T2.EXAMNO       = T1.EXAMNO ");
        stb.append("   LEFT JOIN FINSCHOOL_MST FS ");
        stb.append("          ON FS.FINSCHOOLCD  = T1.FS_CD ");
        stb.append(" WHERE ");
        stb.append("       T1.ENTEXAMYEAR  = '"+ _param._entexamyear +"' ");
        stb.append("   AND T1.APPLICANTDIV = '"+ _param._applicantDiv +"' ");
        stb.append("   AND T1.FS_CD IS NOT NULL ");
        stb.append("   AND T1.ENTDIV       = '1' "); //入学者のみ
        stb.append(" ORDER BY ");
        stb.append("   T1.EXAMNO ");

        return stb.toString();
    }

    private static class Student {
        final String _examno;
        final String _name;
        final String _birthday;
        final String _fs_cd;
        final String _finschool_name;

        private Student(
                final String examno,
                final String name,
                final String birthday,
                final String fs_cd,
                final String finschool_name
        ) {
            _examno = examno;
            _name = name;
            _birthday = birthday;
            _fs_cd = fs_cd;
            _finschool_name = finschool_name;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 76932 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _loginYear;
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _preamble;
        private final String _limitDate;

        private final String _outputDate;
        private final String _documentTitle;
        private final String _documentText;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolSchoolZipCd;
        private String _certifSchoolSchoolAddr1;
        private String _certifSchoolSchoolAddr2;
        private String _certifSchoolSchoolTelNo;
        private String _certifSchoolSchoolFaxNo;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear = request.getParameter("LOGIN_YEAR");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _preamble = request.getParameter("PREAMBLE");
            _limitDate = request.getParameter("LIMIT_DATE").replace('/', '-');

            //作成日時
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            final String date = sdf.format(new Date());
            final String[] outoutDate = date.split("/");
            _outputDate = outoutDate[0] + "年" + outoutDate[1] + "月" + outoutDate[2] + "日";

            _documentTitle = getDocument(db2, "TITLE");
            _documentText = getDocument(db2, "TEXT");

            setCertifSchoolDat(db2);
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindcd = "105";
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT * FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '"+ certifKindcd +"' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK7"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
            _certifSchoolSchoolZipCd = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK2"));
            _certifSchoolSchoolAddr1 = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK4"));
            _certifSchoolSchoolAddr2 = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK5"));
            _certifSchoolSchoolTelNo = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK1"));
            _certifSchoolSchoolFaxNo = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK3"));
        }

        private String getDocument(final DB2UDB db2, final String field) {
            String rtnStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   * ");
            stb.append(" FROM ");
            stb.append("   DOCUMENT_MST ");
            stb.append(" WHERE ");
            stb.append("   DOCUMENTCD = '"+ _preamble +"' ");

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnStr = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtnStr;
        }

    }
}

// eof

