/*
 * $Id: 8304a28a0a490ad07ade70f4a0c5caa862e7a1cf $
 *
 * 作成日: 2018/05/25
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;

public class KNJA143V {

    private static final Log log = LogFactory.getLog(KNJA143V.class);

    private static final String SCHOOL_KIND_J = "J";
    private static final String SCHOOL_KIND_H = "H";

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
        final List printList = getSchregInfoList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final SchregInfo schInfo = (SchregInfo) iterator.next();

            final String setForm = (SCHOOL_KIND_J.equals(schInfo._shoolKind)) ? "KNJA143V_1.frm": "KNJA143V_2.frm";
            svf.VrSetForm(setForm, 1);

            final String schregImg = _param.getImageFilePath(schInfo._photoName + "." + _param._extension); //写真データ存在チェック用
            if (null != schregImg) {
                svf.VrsOut("PHOTO_BMP", schregImg); // 顔写真
            }
            final String nameField    = KNJ_EditEdit.getMS932ByteLength(schInfo._name) > 30    ? "3": KNJ_EditEdit.getMS932ByteLength(schInfo._name) > 22    ? "2": "";
            final String nameEngField = KNJ_EditEdit.getMS932ByteLength(schInfo._nameEng) > 30 ? "3": KNJ_EditEdit.getMS932ByteLength(schInfo._nameEng) > 22 ? "2": "";
            final String setAddr      = schInfo._addr1 + schInfo._addr2;
            final String addrField    = KNJ_EditEdit.getMS932ByteLength(setAddr) > 96 ? "2": "1";
            final String setBirthDay  = KNJ_EditDate.h_format_SeirekiJP(schInfo._birthDay);
            final String sMonth       = KNJ_EditDate.h_format_S(_param._termSdate, "M").length() < 2 ? " "  + KNJ_EditDate.h_format_S(_param._termSdate, "M"): KNJ_EditDate.h_format_S(_param._termSdate, "M");
            final String sDay         = KNJ_EditDate.h_format_S(_param._termSdate, "d").length() < 2 ? "  " + KNJ_EditDate.h_format_S(_param._termSdate, "d"): " " + KNJ_EditDate.h_format_S(_param._termSdate, "d");
            final String setSDate     = KNJ_EditDate.h_format_Seireki_N(_param._termSdate) + sMonth + "月" + sDay + "日";
            final String eMonth       = KNJ_EditDate.h_format_S(_param._termEdate, "M").length() < 2 ? " "  + KNJ_EditDate.h_format_S(_param._termEdate, "M"): KNJ_EditDate.h_format_S(_param._termEdate, "M");
            final String eDay         = KNJ_EditDate.h_format_S(_param._termEdate, "d").length() < 2 ? "  " + KNJ_EditDate.h_format_S(_param._termEdate, "d"): " " + KNJ_EditDate.h_format_S(_param._termEdate, "d");
            final String setEDate     = KNJ_EditDate.h_format_Seireki_N(_param._termEdate) + eMonth + "月" + eDay + "日";

            svf.VrsOut("SCHREGNO", schInfo._schregNo);              // 学籍番号
            svf.VrsOut("NAME" + nameField, schInfo._name);          // 氏名
            svf.VrsOut("NAME_ENG" + nameEngField, schInfo._nameEng);// 氏名英字
            svf.VrsOut("BIRTHDAY1", setBirthDay);                   // 生年月日
            svf.VrsOut("ADDRESS1_" + addrField, setAddr);           // 住所+方書き

            svf.VrsOut("SDATE", setSDate); // 発行
            svf.VrsOut("EDATE", setEDate); // 有効期限

            final String setSchoolAddress = (SCHOOL_KIND_J.equals(schInfo._shoolKind)) ? _param._schoolAddrJ    : _param._schoolAddrH;
            final String setTelNo         = (SCHOOL_KIND_J.equals(schInfo._shoolKind)) ? _param._schoolTelJ     : _param._schoolTelH;
            final String setSchoolName    = (SCHOOL_KIND_J.equals(schInfo._shoolKind)) ? _param._schoolNameJ    : _param._schoolNameH;
            final String setStampBmp      = (SCHOOL_KIND_J.equals(schInfo._shoolKind)) ? _param._principalStampJ: _param._principalStampH;
            final String schAddrField = KNJ_EditEdit.getMS932ByteLength(setSchoolAddress) > 50 ? "5": KNJ_EditEdit.getMS932ByteLength(setSchoolAddress) > 40 ? "4": KNJ_EditEdit.getMS932ByteLength(setSchoolAddress) > 34 ? "3": KNJ_EditEdit.getMS932ByteLength(setSchoolAddress) > 28 ? "2": "1";

            svf.VrsOut("SCHOOLADDRESS" + schAddrField, setSchoolAddress);   // 学校所在地
            svf.VrsOut("TELNO", setTelNo);                                  // TEL
            svf.VrsOut("SCHOOL_NAME", setSchoolName);                       // 学校名
            if (null != setStampBmp) {
                svf.VrsOut("STAMP_BMP", setStampBmp);                       // 校長印
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List getSchregInfoList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSchregInfoSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String shoolKind  = rs.getString("SCHOOL_KIND");
                final String schregNo   = rs.getString("SCHREGNO");
                final String name       = rs.getString("NAME");
                final String nameEng    = rs.getString("NAME_ENG");
                final String birthDay   = rs.getString("BIRTHDAY");
                final String addr1      = rs.getString("ADDR1");
                final String addr2      = rs.getString("ADDR2");

                final SchregInfo schInfo = new SchregInfo(shoolKind, schregNo, name, nameEng, birthDay, addr1, addr2);
                retList.add(schInfo);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSchregInfoSql() {
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
        stb.append("     GDAT.SCHOOL_KIND, ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     value(BASE.NAME_ENG, '') as NAME_ENG, ");
        stb.append("     BASE.BIRTHDAY, ");
        stb.append("     value(ADDR.ADDR1, '') as ADDR1, ");
        stb.append("     value(ADDR.ADDR2, '') as ADDR2 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN ADDRESS_DAT ADDR ON REGD.SCHREGNO = ADDR.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR     = HDAT.YEAR ");
        stb.append("                                    AND REGD.SEMESTER = HDAT.SEMESTER ");
        stb.append("                                    AND REGD.GRADE    = HDAT.GRADE ");
        stb.append("                                    AND REGD.HR_CLASS = HDAT.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR  = GDAT.YEAR ");
        stb.append("                                    AND REGD.GRADE = GDAT.GRADE ");
        stb.append(" WHERE ");
        stb.append("         REGD.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND REGD.SCHREGNO IN " + _param._inSchregNo + " ");
        stb.append(" ORDER BY ");
        stb.append("       REGD.GRADE, ");
        stb.append("       REGD.HR_CLASS, ");
        stb.append("       REGD.ATTENDNO ");

        return stb.toString();
    }

    private class SchregInfo {
        final String _shoolKind;
        final String _schregNo;
        final String _name;
        final String _nameEng;
        final String _birthDay;
        final String _addr1;
        final String _addr2;
        final String _photoName;
        public SchregInfo(
                final String shoolKind,
                final String schregNo,
                final String name,
                final String nameEng,
                final String birthDay,
                final String addr1,
                final String addr2
        ) {
            _shoolKind  = shoolKind;
            _schregNo   = schregNo;
            _name       = name;
            _nameEng    = nameEng;
            _birthDay   = birthDay;
            _addr1      = addr1;
            _addr2      = addr2;
            _photoName  = "P" + schregNo;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65742 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _termSdate;
        private final String _termEdate;
        private final String _schoolAddrH;
        private final String _schoolTelH;
        private final String _schoolNameH;
        private final String _schoolAddrJ;
        private final String _schoolTelJ;
        private final String _schoolNameJ;
        private final String _inSchregNo;
        private final String _documentroot;
        private String _imagepath;
        private String _extension;
        final String _principalStampH;
        final String _principalStampJ;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear           = request.getParameter("CTRL_YEAR");
            _ctrlSemester       = request.getParameter("CTRL_SEMESTER");
            _termSdate          = StringUtils.replace(request.getParameter("TERM_SDATE"), "/", "-"); // 発行日
            _termEdate          = StringUtils.replace(request.getParameter("TERM_EDATE"), "/", "-"); // 有効期限
            _schoolAddrH        = getCertifSchoolDat(db2, _ctrlYear, SCHOOL_KIND_H, "REMARK1");     // （高校）学校住所
            _schoolTelH         = getCertifSchoolDat(db2, _ctrlYear, SCHOOL_KIND_H, "REMARK3");     // （高校）学校電話番号
            _schoolNameH        = getCertifSchoolDat(db2, _ctrlYear, SCHOOL_KIND_H, "SCHOOL_NAME"); // （高校）学校名
            _schoolAddrJ        = getCertifSchoolDat(db2, _ctrlYear, SCHOOL_KIND_J, "REMARK1");     // （中学）学校住所
            _schoolTelJ         = getCertifSchoolDat(db2, _ctrlYear, SCHOOL_KIND_J, "REMARK3");     // （中学）学校電話番号
            _schoolNameJ        = getCertifSchoolDat(db2, _ctrlYear, SCHOOL_KIND_J, "SCHOOL_NAME"); // （中学）学校名
            _documentroot       = request.getParameter("DOCUMENTROOT");
            _imagepath          = request.getParameter("IMAGEPATH");
            _principalStampH    = getImageFilePath("SCHOOLSTAMP_H.bmp"); // 校長印（高校）
            _principalStampJ    = getImageFilePath("SCHOOLSTAMP_J.bmp"); // 校長印（中学）

            final String gHrAtNoSchNo[] = request.getParameterValues("category_selected");
            String sep = "";
            final StringBuffer stb = new StringBuffer();
            stb.append("(");
            for( int i=0 ; i<gHrAtNoSchNo.length ; i++ ){
                final String setSchregNo = StringUtils.split(gHrAtNoSchNo[i], "-")[1];
                stb.append(sep + "'" + setSchregNo + "'");
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

        private String getCertifSchoolDat(final DB2UDB db2, final String year, final String schoolKind, final String fieldName) {
            String retFieldName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String setKindCd = (SCHOOL_KIND_J.equals(schoolKind)) ? "102": "101";
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("    " + fieldName + " ");
                stb.append(" FROM ");
                stb.append("    CERTIF_SCHOOL_DAT ");
                stb.append(" WHERE ");
                stb.append("        YEAR          = '" + year + "' ");
                stb.append("    AND CERTIF_KINDCD = '"+ setKindCd +"' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retFieldName = rs.getString(fieldName);
                }
            } catch (SQLException ex) {
                log.debug("getCertifSchoolDat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retFieldName;
        }

    }
}

// eof
