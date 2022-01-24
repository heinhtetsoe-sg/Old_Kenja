// kanji=漢字
/*
 * $Id: 60b353b75ff7ecb0fac3a621c7194155428685f1 $
 *
 * 作成日: 2019/12/24 19:00:15 - JST
 * 作成者: yogi
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 60b353b75ff7ecb0fac3a621c7194155428685f1 $
 */
public class KNJA143U_5 {

    private static final Log log = LogFactory.getLog("KNJA143U.class");

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
            log.fatal("$Revision: 73652 $");
            KNJServletUtils.debugParam(request, log);

            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            final Param param = new Param(db2, request);

            _param = param;

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

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes( "MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List studentList = Student.getStudentList(db2, _param);

        final String schoolstampHPath = _param.getImageFilePath("SCHOOLSTAMP_H.bmp");
        final String schoolstampJPath = _param.getImageFilePath("SCHOOLSTAMP_J.bmp");
        final String schoollogoJPath = _param.getImageFilePath("SCHOOLLOGO_J.jpg");
        final String schoollogoHPath = _param.getImageFilePath("SCHOOLLOGO_H.jpg");

        for (int j = 0; j < studentList.size(); j++) {
        	final Student student = (Student) studentList.get(j);

            final String form;
            final String schoolKind = student._schoolKind;
			if ("J".equals(schoolKind)) {
            	form = _param._useFormNameA143U + "_1.frm";
            } else if ("H".equals(schoolKind)) {
            	form = _param._useFormNameA143U + "_2.frm";
            } else {
            	continue;
            }
            _param.setCertifSchoolDat(schoolKind);

            svf.VrSetForm(form, 1);

            svf.VrsOut("SCHREGNO", student._schregno); // 学籍番号
            //svf.VrsOut("GRADE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　第" + digitsToZenkaku(String.valueOf(Integer.parseInt(student._gradeCd))) + "学年"); // 年度
            svf.VrsOut("GRADE", "J".equals(student._schoolKind) ? "中学校" : "高等学校 普通科"); // 年度
            svf.VrsOut("NAME" + (getMS932ByteLength(student._name) <= 20 ? "" : getMS932ByteLength(student._name) <= 30 ? "_2" : "_3"), student._name); // 氏名
            svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(db2, student._birthday)); // 生年月日
//            final int addrlen = Math.max(getMS932ByteLength(student._addr1), getMS932ByteLength(student._addr2));
//            if (addrlen <= 30) {
//                svf.VrsOut("ADDRESS1_1", student._addr1); // 住所
//                svf.VrsOut("ADDRESS2_1", student._addr2); // 住所
//            } else if (addrlen <= 40) {
//                svf.VrsOut("ADDRESS1_2", student._addr1); // 住所
//                svf.VrsOut("ADDRESS2_2", student._addr2); // 住所
//            } else {
//                svf.VrsOut("ADDRESS1_3", student._addr1); // 住所
//                svf.VrsOut("ADDRESS2_3", student._addr2); // 住所
//            }

            svf.VrsOut("SCHOOLADDRESS", StringUtils.defaultString(_param._addr1) + StringUtils.defaultString(_param._telno));
            svf.VrsOut("SCHOOLNAME", _param._schoolname);
            svf.VrsOut("JOBNAME", _param._jobname);
            svf.VrsOut("STAFFNAME", _param._principalName);

            svf.VrsOut("SDATE", KNJ_EditDate.h_format_JP(db2, _param._issueDate)); // 発行日
            svf.VrsOut("LDATE", KNJ_EditDate.h_format_JP(db2, _param._limitDate)); // 有効期限
            final String photoBmp = _param.getImageFilePath("P" + student._schregno + "." + _param._extension);
            if (null != photoBmp) {
                svf.VrsOut("PHOTO", photoBmp); //顔写真
            }
            String stamp = null;
            String logo = null;
            if ("J".equals(schoolKind)) {
            	logo = schoollogoJPath;
            	stamp = schoolstampJPath;
            } else if ("H".equals(schoolKind)) {
            	logo = schoollogoHPath;
            	stamp = schoolstampHPath;
            }
            if (null != logo) {
                svf.VrsOut("LOGO", logo); // ロゴ
            }
            if (null != stamp) {
                svf.VrsOut("STAMP", stamp); // 校印
            }
            svf.VrsOut("BARCODE", student._schregno); // バーコード(学籍番号)

            svf.VrEndPage();

            svf.VrSetForm(_param._useFormNameA143U + "_3.frm", 1);

            svf.VrsOut("SCHREGNO", student._schregno); // 学籍番号(白文字)
            final String addrStr = StringUtils.defaultString(student._addr1, "") + ("1".equals(student._addrFlg) ? StringUtils.defaultString(student._addr2, "") : "");
            final int addr2plen = getMS932ByteLength(addrStr);
            if (addr2plen > 50) {
                svf.VrsOut("ADDRESS2", addrStr); // 住所
            } else {
                svf.VrsOut("ADDRESS1", addrStr); // 住所
            }
//            svf.VrsOut("SECTION1_1_1", student._josya);
//            svf.VrsOut("SECTION1_2_1", student._gesya);
            svf.VrsOutn("SECTION1_1_1", 1, student._josya);
            svf.VrsOutn("SECTION1_2_1", 1, student._gesya);
            svf.VrsOutn("SECTION1_1_1", 2, student._josya7);
            svf.VrsOutn("SECTION1_2_1", 2, student._gesya7);
            svf.VrEndPage();

            _hasData = true;
        }
    }

	private static class Student {
        String _schregno;
        String _grade;
        String _hrClass;
        String _attendno;
        String _schoolKind;
        String _gradeCd;
        String _gradeName1;
        String _name;
        String _nameKana;
        String _birthday;
        String _zipcd;
        String _addrFlg;
        String _addr1;
        String _addr2;
        String _josya;
        String _gesya;
        String _josya7;
        String _gesya7;

        public static List getStudentList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = new Student();
                    student._schregno = rs.getString("SCHREGNO");
                    student._grade = rs.getString("GRADE");
                    student._hrClass = rs.getString("HR_CLASS");
                    student._attendno = rs.getString("ATTENDNO");
                    student._schoolKind = rs.getString("SCHOOL_KIND");
                    student._gradeCd = rs.getString("GRADE_CD");
                    student._gradeName1 = rs.getString("GRADE_NAME1");
                    student._name = rs.getString("NAME");
                    student._nameKana = rs.getString("NAME_KANA");
                    student._birthday = rs.getString("BIRTHDAY");
                    student._zipcd = rs.getString("ZIPCD");
                    student._addrFlg = rs.getString("ADDR_FLG");
                    student._addr1 = rs.getString("ADDR1");
                    student._addr2 = rs.getString("ADDR2");
                    student._josya = StringUtils.defaultString(rs.getString("JOSYA_1"), "");
//                    student._gesya = findLastStation(rs.getString("GESYA_1"), rs.getString("GESYA_2"), rs.getString("GESYA_3"), rs.getString("GESYA_4"), rs.getString("GESYA_5"), rs.getString("GESYA_6"), rs.getString("GESYA_7"));
                    student._gesya = StringUtils.defaultString(rs.getString("GESYA_1"), rs.getString("GESYA_7"));
                    student._josya7 = StringUtils.defaultString(rs.getString("JOSYA_7"), "");
                    student._gesya7 = ("".equals(student._josya7)) ? "" : StringUtils.defaultString(rs.getString("GESYA_7"), "");
                    list.add(student);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String findLastStation (final String gesya_1, final String gesya_2, final String gesya_3, final String gesya_4, final String gesya_5, final String gesya_6, final String gesya_7) {
        	String retStr = "";

        	if (!"".equals(StringUtils.defaultString(gesya_7, ""))) {
        		retStr = gesya_7;
        	} else if (!"".equals(StringUtils.defaultString(gesya_6, ""))) {
        		retStr = gesya_6;
        	} else if (!"".equals(StringUtils.defaultString(gesya_5, ""))) {
        		retStr = gesya_5;
        	} else if (!"".equals(StringUtils.defaultString(gesya_4, ""))) {
        		retStr = gesya_4;
        	} else if (!"".equals(StringUtils.defaultString(gesya_3, ""))) {
        		retStr = gesya_3;
        	} else if (!"".equals(StringUtils.defaultString(gesya_2, ""))) {
        		retStr = gesya_2;
        	} else if (!"".equals(StringUtils.defaultString(gesya_1, ""))) {
        		retStr = gesya_1;
        	}
        	return retStr;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     BASE.SCHREGNO, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGDG.SCHOOL_KIND, ");
            stb.append("     REGDG.GRADE_CD, ");
            stb.append("     REGDG.GRADE_NAME1, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     BASE.BIRTHDAY, ");
            stb.append("     CASE WHEN L5.FLG_1 = '1' THEN G1.STATION_NAME ELSE L5.GESYA_1 END AS GESYA_1, ");
            stb.append("     CASE WHEN L5.FLG_2 = '1' THEN G2.STATION_NAME ELSE L5.GESYA_2 END AS GESYA_2, ");
            stb.append("     CASE WHEN L5.FLG_3 = '1' THEN G3.STATION_NAME ELSE L5.GESYA_3 END AS GESYA_3, ");
            stb.append("     CASE WHEN L5.FLG_4 = '1' THEN G4.STATION_NAME ELSE L5.GESYA_4 END AS GESYA_4, ");
            stb.append("     CASE WHEN L5.FLG_5 = '1' THEN G5.STATION_NAME ELSE L5.GESYA_5 END AS GESYA_5, ");
            stb.append("     CASE WHEN L5.FLG_6 = '1' THEN G6.STATION_NAME ELSE L5.GESYA_6 END AS GESYA_6, ");
            stb.append("     CASE WHEN L5.FLG_7 = '1' THEN G7.STATION_NAME ELSE L5.GESYA_7 END AS GESYA_7, ");
            stb.append("     CASE WHEN L5.FLG_1 = '1' THEN J1.STATION_NAME ELSE L5.JOSYA_1 END AS JOSYA_1, ");
            stb.append("     CASE WHEN L5.FLG_7 = '1' THEN J7.STATION_NAME ELSE L5.JOSYA_7 END AS JOSYA_7, ");
            stb.append("     ADDR.ZIPCD, ");
            stb.append("     ADDR.ADDR_FLG, ");
            stb.append("     ADDR.ADDR1, ");
            stb.append("     ADDR.ADDR2 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
            stb.append("         AND REGDG.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN ( ");
            stb.append("         SELECT ");
            stb.append("             SCHREGNO, ");
            stb.append("             MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("         FROM ");
            stb.append("             SCHREG_ADDRESS_DAT ");
            stb.append("         GROUP BY ");
            stb.append("             SCHREGNO ");
            stb.append("     ) ADDR_MAX ON ADDR_MAX.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_ADDRESS_DAT ADDR ON ADDR.SCHREGNO = ADDR_MAX.SCHREGNO ");
            stb.append("         AND ADDR.ISSUEDATE = ADDR_MAX.ISSUEDATE ");
            stb.append("       LEFT JOIN SCHREG_ENVIR_DAT L5 ON L5.SCHREGNO = REGD.SCHREGNO ");
            stb.append("       LEFT JOIN STATION_NETMST J1 ON J1.STATION_CD = L5.JOSYA_1 ");
            stb.append("       LEFT JOIN STATION_NETMST J7 ON J7.STATION_CD = L5.JOSYA_7 ");
            stb.append("       LEFT JOIN STATION_NETMST G1 ON G1.STATION_CD = L5.GESYA_1 ");
            stb.append("       LEFT JOIN STATION_NETMST G2 ON G2.STATION_CD = L5.GESYA_2 ");
            stb.append("       LEFT JOIN STATION_NETMST G3 ON G3.STATION_CD = L5.GESYA_3 ");
            stb.append("       LEFT JOIN STATION_NETMST G4 ON G4.STATION_CD = L5.GESYA_4 ");
            stb.append("       LEFT JOIN STATION_NETMST G5 ON G5.STATION_CD = L5.GESYA_5 ");
            stb.append("       LEFT JOIN STATION_NETMST G6 ON G6.STATION_CD = L5.GESYA_6 ");
            stb.append("       LEFT JOIN STATION_NETMST G7 ON G7.STATION_CD = L5.GESYA_7 ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            if ("1".equals(param._disp)) {
                stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
            } else if ("2".equals(param._disp)) {
                stb.append("     AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO ");
            return stb.toString();
        }
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _disp; // 1:クラス 2:個人
        final String[] _category_selected;
        final String _issueDate;
        final String _limitDate;
        final String _documentroot;
        final String _useAddrField2;

        private String _101jobname;
        private String _101principalName;
        private String _101schoolname;
        private String _101addr1;
        private String _101telno;
        private String _102jobname;
        private String _102principalName;
        private String _102schoolname;
        private String _102addr1;
        private String _102telno;

        private String _jobname;
        private String _principalName;
        private String _schoolname;
        private String _addr1;
        private String _telno;

        private String _extension;
        private String _imagepass;

        private final String _useFormNameA143U;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _disp = request.getParameter("DISP");
            _category_selected = request.getParameterValues("CATEGORY_SELECTED");
            for (int i = 0; i < _category_selected.length; i++) {
                _category_selected[i] = StringUtils.split(_category_selected[i], "-")[0];
            }
            _issueDate = StringUtils.defaultString(request.getParameter("ISSUE_DATE")).replace('/', '-');
            _limitDate = StringUtils.defaultString(request.getParameter("LIMIT_DATE")).replace('/', '-');
            _documentroot = request.getParameter("DOCUMENTROOT");
            _useAddrField2 = request.getParameter("useAddrField2");

            setControlMst(db2);
            loadCertifSchoolDat(db2);

        	_useFormNameA143U = request.getParameter("USEFORMNAMEA143U");
        }

        private void setControlMst(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                _extension = "";
                String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _imagepass = rs.getString("IMAGEPATH");
                    _extension = rs.getString("EXTENSION");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }


        /**
         * 写真データファイルの取得
         */
        private String getImageFilePath(final String filename) {
            if (null == _documentroot || null == _imagepass || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentroot).append("/").append(_imagepass).append("/").append(filename);
            final File file = new File(path.toString());
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }

        private void setCertifSchoolDat(final String schoolKind) {
            _jobname = null;
            _principalName = null;
            _schoolname = null;
            _addr1 = null;
            _telno = null;
        	if ("H".equals(schoolKind)) {
                _jobname = _101jobname;
                _principalName = _101principalName;
                _schoolname = _101schoolname;
                _addr1 = _101addr1;
                _telno = _101telno;
        	} else if ("J".equals(schoolKind)) {
                _jobname = _102jobname;
                _principalName = _102principalName;
                _schoolname = _102schoolname;
                _addr1 = _102addr1;
                _telno = _102telno;
        	}
        	//log.info(" certif school " + schoolKind + ":" + _jobname + ", " + _principalName + ", " + _schoolname + ", " + _addr1);
        }

        public void loadCertifSchoolDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '101'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _101jobname =  rs.getString("JOB_NAME");
                    _101principalName = rs.getString("PRINCIPAL_NAME");
                    _101schoolname = rs.getString("SCHOOL_NAME");
                    _101addr1 = rs.getString("REMARK1"); // 学校住所
                    _101telno = rs.getString("REMARK3"); // 学校電話番号
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            try {
                final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '102'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _102jobname =  rs.getString("JOB_NAME");
                    _102principalName = rs.getString("PRINCIPAL_NAME");
                    _102schoolname = rs.getString("SCHOOL_NAME");
                    _102addr1 = rs.getString("REMARK1"); // 学校住所
                    _102telno = rs.getString("REMARK3"); // 学校電話番号
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }
}

// eof
