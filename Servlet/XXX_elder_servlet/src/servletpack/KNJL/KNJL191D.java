/*
 * $Id: 4b011564d43d0997f3950c0521e8b3b706decda4 $
 *
 * 作成日: 2019/12/10
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL191D {

    private static final Log log = LogFactory.getLog(KNJL191D.class);

    //送付先
    private static final String SEND_TO_SEITO = "1";  //1:生徒
    private static final String SEND_TO_SCHOOL = "2"; //2:出身校

    private static final int MAX_COL = 2; //最終行数
    private static final int MAX_ROW = 6; //最終列数

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

        //合格者を取得し、処理を行う
        List gouakulist = getList(db2);

        svf.VrSetForm("KNJL191D.frm", 1);

        int col = _param._poCol;
        int row = _param._poRow;
        for (Iterator iterator = gouakulist.iterator(); iterator.hasNext();) {
            PrintData goukakuInfo = (PrintData)iterator.next();

            if(col > MAX_COL) {
                col = 1;
                row++;
                if(row > MAX_ROW) {
                    row = 1;
                    svf.VrEndPage();
                    svf.VrSetForm("KNJL191D.frm", 1);
                }
            }

            final String zipcd = (SEND_TO_SEITO.equals(_param._sendTo)) ? goukakuInfo._zipcd : goukakuInfo._finSchoolZipcd;
            final String addr1 = (SEND_TO_SEITO.equals(_param._sendTo)) ? goukakuInfo._addr1 : goukakuInfo._finSchoolAddr1;
            final String addr2 = (SEND_TO_SEITO.equals(_param._sendTo)) ? goukakuInfo._addr2 : goukakuInfo._finSchoolAddr2;
            final String name =  (SEND_TO_SEITO.equals(_param._sendTo)) ? goukakuInfo._name : goukakuInfo._finSchoolName;
            final String honorific =  ("".equals(name)) ? "" : (SEND_TO_SEITO.equals(_param._sendTo)) ? " 様" : " 校長 様";
            final String examno =  (SEND_TO_SEITO.equals(_param._sendTo)) ? goukakuInfo._examNo : "";

            svf.VrsOutn("ZIPCODE" + col, row, zipcd); //郵便番号
            final String addr1Field = KNJ_EditEdit.getMS932ByteLength(addr1) > 50 ? "_3" : KNJ_EditEdit.getMS932ByteLength(addr1) > 40 ? "_2" : "_1";
            final String addr2Field = KNJ_EditEdit.getMS932ByteLength(addr2) > 50 ? "_3" : KNJ_EditEdit.getMS932ByteLength(addr2) > 40 ? "_2" : "_1";
            svf.VrsOutn("ADDRESS" + col + "_1" + addr1Field, row, addr1); //住所1
            svf.VrsOutn("ADDRESS" + col + "_2" + addr2Field, row, addr2); //住所2

            final String printName = name + honorific;
            final String nameField = KNJ_EditEdit.getMS932ByteLength(printName) > 44 ? "_6" : KNJ_EditEdit.getMS932ByteLength(printName) > 34 ? "_5" : KNJ_EditEdit.getMS932ByteLength(printName) > 24 ? "_4" : "_1";
            svf.VrsOutn("NAME" + col + nameField, row, printName); //氏名

            svf.VrsOutn("NAME" + col + "_3", row, examno); //受験番号
            svf.VrsOutn("NAME" + col + "_2", row, "");

            col++;
            _hasData = true;
        }
        svf.VrEndPage();
    }

    /**
     * 学校情報セット
     * @param db2
     * @param svf
     * @param isLogoPrint 「true：校長名の空白をカット　false：しない」
     */
    private void schoolInfoPrint(final DB2UDB db2, final Vrw32alp svf, final boolean isLogoPrint) {
        //校証
        if (isLogoPrint && null != _param._schoollogoFilePath) {
            svf.VrsOut("SCHOOL_LOGO", _param._schoollogoFilePath);
        }

        //学校名
        svf.VrsOut("SCHOOL_NAME", _param._certifSchool._schoolName);

        //郵便番号
        svf.VrsOut("ZIP_NO", _param._certifSchool._schoolZipCd);

        //学校住所
        final String addr = StringUtils.defaultString(_param._certifSchool._schoolAddr1) + StringUtils.defaultString(_param._certifSchool._schoolAddr2);
        svf.VrsOut("SCHOOL_ADDR", addr);

        //電話番号
        svf.VrsOut("TEL1", "TEL " + _param._certifSchool._schoolTel);

        //FAX番号
        svf.VrsOut("TEL2", "FAX " + _param._certifSchool._schoolFax);

        //職名
        svf.VrsOut("JOB_NAME", _param._certifSchool._jobName);

        //校長名
        if (isLogoPrint) {
            svf.VrsOut("STAFF_NAME", StringUtils.deleteWhitespace(_param._certifSchool._principalName));
        } else {
            svf.VrsOut("STAFF_NAME", _param._certifSchool._principalName);
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examNo = StringUtils.defaultString(rs.getString("EXAMNO"));
                final String name = StringUtils.defaultString(rs.getString("NAME"));
                final String zipcd = StringUtils.defaultString(rs.getString("ZIPCD"));
                final String addr1 = StringUtils.defaultString(rs.getString("ADDRESS1"));
                final String addr2 = StringUtils.defaultString(rs.getString("ADDRESS2"));
                final String finSchoolCd = StringUtils.defaultString(rs.getString("FS_CD"));
                final String finSchoolName = StringUtils.defaultString(rs.getString("FS_NAME"));
                final String finSchoolZipcd = StringUtils.defaultString(rs.getString("FS_ZIPCD"));
                final String finSchoolAddr1 = StringUtils.defaultString(rs.getString("FS_ADDR1"));
                final String finSchoolAddr2 = StringUtils.defaultString(rs.getString("FS_ADDR2"));

                final PrintData printData = new PrintData(examNo, name, zipcd, addr1, addr2, finSchoolCd, finSchoolName, finSchoolZipcd, finSchoolAddr1, finSchoolAddr2);
                retList.add(printData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        if(SEND_TO_SEITO.equals(_param._sendTo)) {
            //1:生徒
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.NAME, ");
            stb.append("     ADDR.ZIPCD, ");
            stb.append("     ADDR.ADDRESS1, ");
            stb.append("     ADDR.ADDRESS2, ");
            stb.append("     '' AS FS_CD, ");
            stb.append("     '' AS FS_NAME, ");
            stb.append("     '' AS FS_ZIPCD, ");
            stb.append("     '' AS FS_ADDR1, ");
            stb.append("     '' AS FS_ADDR2 ");
        } else {
            //2:出身校
            stb.append("     '' AS EXAMNO, ");
            stb.append("     '' AS NAME, ");
            stb.append("     '' AS ZIPCD, ");
            stb.append("     '' AS ADDRESS1, ");
            stb.append("     '' AS ADDRESS2, ");
            stb.append("     FIN.FINSCHOOLCD AS FS_CD, ");
            stb.append("     FIN.FINSCHOOL_NAME  AS FS_NAME, ");
            stb.append("     FIN.FINSCHOOL_ZIPCD AS FS_ZIPCD, ");
            stb.append("     FIN.FINSCHOOL_ADDR1 AS FS_ADDR1, ");
            stb.append("     FIN.FINSCHOOL_ADDR2 AS FS_ADDR2 ");
        }
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ");
        stb.append("            ON ADDR.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("           AND ADDR.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("           AND ADDR.EXAMNO       = T1.EXAMNO ");
        if(SEND_TO_SCHOOL.equals(_param._sendTo)) {
            if("1".equals(_param._desireDiv)) {
                stb.append("     INNER JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = T1.FS_CD ");
            } else {
                stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DT033 ");
                stb.append("             ON DT033.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
                stb.append("            AND DT033.APPLICANTDIV = T1.APPLICANTDIV ");
                stb.append("            AND DT033.EXAMNO       = T1.EXAMNO ");
                stb.append("            AND DT033.SEQ          = '033' ");
                stb.append("     INNER JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = DT033.REMARK2 ");
            }
        }
        stb.append(" WHERE ");
        stb.append("         T1.ENTEXAMYEAR  = '"+ _param._entExamYear +"' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append("     AND T1.DESIREDIV    = '" + _param._desireDiv +"' ");
        if(SEND_TO_SEITO.equals(_param._sendTo)) {
            //1:生徒
            stb.append("     AND T1.EXAMNO IN " + SQLUtils.whereIn(true, _param._examNos));
        } else {
            //2:出身校
            if("1".equals(_param._desireDiv)) {
                stb.append("     AND T1.FS_CD IN " + SQLUtils.whereIn(true, _param._examNos));
            } else {
                stb.append("     AND DT033.REMARK2 IN " + SQLUtils.whereIn(true, _param._examNos));
            }
        }
        stb.append(" ORDER BY ");
        stb.append("     FS_CD, ");
        stb.append("     EXAMNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _examNo;
        final String _name;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _finSchoolCd;
        final String _finSchoolName;
        final String _finSchoolZipcd;
        final String _finSchoolAddr1;
        final String _finSchoolAddr2;

        public PrintData(
                final String examNo,
                final String name,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String finSchoolCd,
                final String finSchoolName,
                final String finSchoolZipcd,
                final String finSchoolAddr1,
                final String finSchoolAddr2
        ) {
            _examNo = examNo;
            _name = name;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _finSchoolCd = finSchoolCd;
            _finSchoolName = finSchoolName;
            _finSchoolZipcd = finSchoolZipcd;
            _finSchoolAddr1 = finSchoolAddr1;
            _finSchoolAddr2 = finSchoolAddr2;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71301 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    //証明書学校データ
    private class CertifSchool {
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        final String _schoolZipCd;
        final String _schoolAddr1;
        final String _schoolAddr2;
        final String _schoolTel;
        final String _schoolFax;
        public CertifSchool(
                final String schoolName,
                final String jobName,
                final String principalName,
                final String schoolZipCd,
                final String schoolAddr1,
                final String schoolAddr2,
                final String schoolTel,
                final String schoolFax
        ) {
            _schoolName     = schoolName;
            _jobName        = jobName;
            _principalName  = principalName;
            _schoolZipCd    = schoolZipCd;
            _schoolAddr1    = schoolAddr1;
            _schoolAddr2    = schoolAddr2;
            _schoolTel      = schoolTel;
            _schoolFax      = schoolFax;
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;
        private final String _entExamYear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _desireDiv;
        private final String _sendTo;
        private final String[] _examNos;
        final int _poRow;
        final int _poCol;

        private final String _documentroot;
        private final String _imagepath;
        final String _schoollogoFilePath;
        final CertifSchool _certifSchool;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear      = request.getParameter("LOGIN_YEAR");
            _loginSemester  = request.getParameter("LOGIN_SEMESTER");
            _loginDate      = request.getParameter("LOGIN_DATE");
            _entExamYear    = request.getParameter("ENTEXAMYEAR");
            _applicantDiv   = request.getParameter("APPLICANTDIV");
            _testDiv        = request.getParameter("TESTDIV");          //入試区分
            _desireDiv      = request.getParameter("DESIREDIV");        //志望区分
            _sendTo         = request.getParameter("SEND_TO");          //送付先(1:生徒 2:出身校)
            _examNos        = request.getParameterValues("SPORT_SELECTED");  //出力対象 生徒or出身校
            _poRow = Integer.parseInt(request.getParameter("POROW"));
            _poCol = Integer.parseInt(request.getParameter("POCOL"));

            _documentroot       = request.getParameter("DOCUMENTROOT");
            _imagepath          = request.getParameter("IMAGEPATH");
            _schoollogoFilePath = getImageFilePath("SCHOOLLOGO.jpg");
            _certifSchool       = getCertifSchool(db2);

        }

        /** 証明書学校データ */
        private CertifSchool getCertifSchool(final DB2UDB db2) {
            CertifSchool certifSchool = new CertifSchool(null, null, null, null, null, null, null, null);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {

                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entExamYear + "' AND CERTIF_KINDCD = '106' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String schoolName     = rs.getString("SCHOOL_NAME");
                    final String jobName        = rs.getString("JOB_NAME");
                    final String principalName  = rs.getString("PRINCIPAL_NAME");
                    final String schoolZipCd    = rs.getString("REMARK2");
                    final String schoolAddr1    = rs.getString("REMARK4");
                    final String schoolAddr2    = rs.getString("REMARK5");
                    final String schoolTel      = rs.getString("REMARK1");
                    final String schoolFax      = rs.getString("REMARK10");
                    certifSchool = new CertifSchool(schoolName, jobName, principalName, schoolZipCd, schoolAddr1, schoolAddr2, schoolTel, schoolFax);
                }
            } catch (SQLException ex) {
                log.debug("getCertif exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return certifSchool;
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFilePath(final String filename) {
            if (null == _documentroot || null == _imagepath || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentroot).append("/").append(_imagepath).append("/").append(filename);
            final File file = new File(path.toString());
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }

    }
}

// eof
