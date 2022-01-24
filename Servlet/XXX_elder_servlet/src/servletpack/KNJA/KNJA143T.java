/*
 * $Id: 46853c69e89432e0714b95699af5f7f1389e78ce $
 *
 * 作成日: 2017/11/28
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_Get_Info;

public class KNJA143T {

    private static final Log log = LogFactory.getLog(KNJA143T.class);

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
        svf.VrSetForm("KNJA143T_1.frm", 1);
        List schregNoList = new ArrayList();
        boolean secondFlg = false;

        final int poRowMax = 5;
        int poRow = Integer.parseInt(_param._poRow); //行
        String poCol = ("1".equals(_param._poCol)) ? "L": "R"; //列

        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final StudentDataClass printData = (StudentDataClass) iterator.next();
            if (poRow > poRowMax) {
                svf.VrEndPage();
                printUra(svf, schregNoList, secondFlg);//裏印刷
                schregNoList = new ArrayList();
                secondFlg = true;
                svf.VrSetForm("KNJA143T_1.frm", 1);
                poRow = 1;
            }
            schregNoList.add(printData._schregNo);//学籍番号セット(裏プリント用)
            final String schregimg = _param.getImageFilePath(printData._photoName + "." + _param._extension); //写真データ存在チェック用
            if (null != schregimg) {
                svf.VrsOutn("PHOTO_" + poCol, poRow, schregimg);//顔写真
            }
            svf.VrsOutn("SCHREGNO_" + poCol, poRow, printData._schregNo);//氏名
            final String nameSize = (KNJ_EditEdit.getMS932ByteLength(printData._name)) > 28 ? "2": "1";
            svf.VrsOutn("NAME" + nameSize + "_" + poCol, poRow, printData._name);//氏名
            final String setBithDay = KNJ_EditDate.h_format_JP(printData._birthday);
            final String[] partsBirth = KNJ_EditDate.tate_format(setBithDay);
            svf.VrsOutn("BIRTHDAY1_" + poCol, poRow, partsBirth[0]);//和暦//生年月日
            svf.VrsOutn("BIRTHDAY2_" + poCol, poRow, partsBirth[1]);//年
            svf.VrsOutn("BIRTHDAY3_" + poCol, poRow, partsBirth[2]);//月
            svf.VrsOutn("BIRTHDAY4_" + poCol, poRow, partsBirth[3]);//日
            final List addr1TokenList = KNJ_EditKinsoku.getTokenList(printData._addr1, 30);
            if (addr1TokenList.size() > 0) {
                svf.VrsOutn("ADDRESS1_1_" + poCol, poRow, (String) addr1TokenList.get(0));
                if (addr1TokenList.size() > 1) {
                	svf.VrsOutn("ADDRESS2_1_" + poCol, poRow, (String) addr1TokenList.get(1));
                }
            }
            final List addr2TokenList = KNJ_EditKinsoku.getTokenList(printData._addr2, 30);
            if (addr2TokenList.size() > 0) {
            	svf.VrsOutn("ADDRESS3_1_" + poCol, poRow, (String) addr2TokenList.get(0));
            }
            final String setSDate = KNJ_EditDate.h_format_JP(_param._issuedDate);
            final String[] partsSDate = KNJ_EditDate.tate_format(setSDate);
            svf.VrsOutn("SDATE1_" + poCol, poRow, partsSDate[0]);//和暦//発行日
            svf.VrsOutn("SDATE2_" + poCol, poRow, partsSDate[1]);//年
            svf.VrsOutn("SDATE3_" + poCol, poRow, partsSDate[2]);//月
            svf.VrsOutn("SDATE4_" + poCol, poRow, partsSDate[3]);//日
            final String schAddrSize = (KNJ_EditEdit.getMS932ByteLength(_param._certifSchool._schoolAddrTel)) > 80 ? "5":
                                       (KNJ_EditEdit.getMS932ByteLength(_param._certifSchool._schoolAddrTel)) > 70 ? "4":
                                       (KNJ_EditEdit.getMS932ByteLength(_param._certifSchool._schoolAddrTel)) > 60 ? "3":
                                       (KNJ_EditEdit.getMS932ByteLength(_param._certifSchool._schoolAddrTel)) > 50 ? "2": "1";
            svf.VrsOutn("SCHOOLADDRESS" + schAddrSize + "_" + poCol, poRow, _param._certifSchool._schoolAddrTel);//学校所在地＋学校電話番号
            svf.VrsOutn("SCHOOLNAME_" + poCol, poRow, _param._certifSchool._schoolName);//学校名
            svf.VrsOutn("PRINCIPAL_" + poCol, poRow, _param._certifSchool._principalName);//校長
            svf.VrsOutn("JOB_NAME_" + poCol, poRow, _param._certifSchool._jobName);//職名
            if (null != _param._staffStampFilePath) {
                svf.VrsOutn("STAMP_" + poCol, poRow, _param._staffStampFilePath);
            }

            if ("R".equals(poCol)) {
                poRow++;
            }
            poCol = ("L".equals(poCol)) ? "R": "L";
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
            printUra(svf, schregNoList, secondFlg);
        }
    }

    private void printUra(final Vrw32alp svf, final List schregNo, final boolean secondFlg) {
        svf.VrSetForm("KNJA143T_2.frm", 1);

        final int poRowMax = 5;
        int poRow = Integer.parseInt(_param._poRow); //行
        String poCol = ("1".equals(_param._poCol)) ? "L": "R"; //列
        if (secondFlg) {
            poRow = 1;//2枚目は先頭から印刷するようセット
            poCol = "L";
        }

        for (Iterator iterator = schregNo.iterator(); iterator.hasNext();) {
            final String schregNoDat = (String) iterator.next();
            if (poRow > poRowMax) {
                svf.VrEndPage();
                poRow = 1;
            }
            svf.VrsOutn("BARCODE_" + poCol, poRow, "A" + schregNoDat + "B");//バーコード
            svf.VrsOutn("YEAR_" + poCol, poRow, _param._yukouKigen.substring(0, 5));//有効期限（年）
            svf.VrsOutn("MONTH_" + poCol, poRow, String.valueOf(Integer.parseInt(_param._yukouKigen.substring(5))));//有効期限（月）


            if ("R".equals(poCol)) {
                poRow++;
            }
            poCol = ("L".equals(poCol)) ? "R": "L";
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
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
                final String schregNo = rs.getString("SCHREGNO");
                final String name     = rs.getString("NAME");
                final String birthday = rs.getString("BIRTHDAY");
                final String addr1    = rs.getString("ADDR1");
                final String addr2    = rs.getString("ADDR2");

                final StudentDataClass sutudentData = new StudentDataClass(schregNo, name, birthday, addr1, addr2);
                retList.add(sutudentData);
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
        stb.append(" WITH ADDRESS_DAT AS ( ");
        stb.append("     SELECT ");
        stb.append("         A1.SCHREGNO, ");
        stb.append("         A1.ADDR1, ");
        stb.append("         A1.ADDR2 ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ADDRESS_DAT A1 ");
        stb.append("     INNER JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             SCHREGNO, ");
        stb.append("             MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("         FROM ");
        stb.append("             SCHREG_ADDRESS_DAT ");
        stb.append("         GROUP BY ");
        stb.append("             SCHREGNO ");
        stb.append("         ) A2 ON A2.SCHREGNO  = A1.SCHREGNO ");
        stb.append("             AND A2.ISSUEDATE = A1.ISSUEDATE ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.BIRTHDAY, ");
        stb.append("     ADD.ADDR1, ");
        stb.append("     ADD.ADDR2 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REG ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REG.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN ADDRESS_DAT ADD ON REG.SCHREGNO = ADD.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("         REG.YEAR     = '" + _param._year + "' ");
        stb.append("     AND REG.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND REG.SCHREGNO IN " + _param._inSchregNo + " ");
        stb.append(" ORDER BY ");
        stb.append("     REG.SCHREGNO ");

        return stb.toString();
    }

    private class StudentDataClass {
        final String _schregNo;
        final String _name;
        final String _birthday;
        final String _addr1;
        final String _addr2;
        final String _photoName;
        public StudentDataClass(
                final String schregNo,
                final String name,
                final String birthday,
                final String addr1,
                final String addr2
        ) {
            _schregNo  = schregNo;
            _name      = name;
            _birthday  = birthday;
            _addr1     = addr1;
            _addr2     = addr2;
            _photoName =  "P" + schregNo;
        }
    }

    private class CertifSchool {
        final String _schoolAddrTel;
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        public CertifSchool(
                final String schoolAddrTel,
                final String schoolName,
                final String jobName,
                final String principalName
        ) {
            _schoolAddrTel = schoolAddrTel;
            _schoolName    = schoolName;
            _jobName       = jobName;
            _principalName = principalName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 59910 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _inSchregNo;
        private final String _issuedDate;
        private final String _yukouKigen;
        private final String _poRow;
        private final String _poCol;
        private final String _documentroot;
        private String _imagepath;
        private String _extension;
        final CertifSchool _certifSchool;
        final String _staffStampFilePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year           = request.getParameter("YEAR");
            _semester       = request.getParameter("GAKKI");
            _issuedDate     = request.getParameter("ISSUED_DATE");
            _yukouKigen     = request.getParameter("YUKOU_KIGEN");
            _poRow          = request.getParameter("POROW");
            _poCol          = request.getParameter("POCOL");
            _documentroot   = request.getParameter("DOCUMENTROOT");
            _imagepath      = request.getParameter("IMAGEPATH");
            _certifSchool   = getCertifSchool(db2);
            _staffStampFilePath = getImageFilePath("SCHOOLSTAMP.bmp");//校長印

            final String schregno[] = request.getParameterValues("CATEGORY_SELECTED");
            String sep = "";
            final StringBuffer stb = new StringBuffer();
            stb.append("(");
            for( int i=0 ; i<schregno.length ; i++ ){
                stb.append(sep + "'" + schregno[i] + "'");
                sep = ",";
            }
            stb.append(")");
            _inSchregNo = stb.toString();

            //  写真データ
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            try {
                returnval = getinfo.Control(db2);
                _extension = returnval.val5;      //拡張子
            } catch (Exception e) {
                log.error("setHeader set error!", e);
            } finally {
                getinfo = null;
                returnval = null;
            }
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

        private CertifSchool getCertifSchool(final DB2UDB db2) {
            CertifSchool certifSchool = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '101' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String schoolAddrTel = rs.getString("REMARK1") + "　TEL　" + rs.getString("REMARK3");
                    final String schoolName    = rs.getString("SCHOOL_NAME");
                    final String jobName       = rs.getString("JOB_NAME");
                    final String principalName = rs.getString("PRINCIPAL_NAME");
                    certifSchool = new CertifSchool(schoolAddrTel, schoolName, jobName, principalName);
                }
            } catch (SQLException ex) {
                log.debug("getCertif101 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return certifSchool;
        }

    }
}

// eof
