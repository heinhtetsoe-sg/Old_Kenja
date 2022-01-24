// kanji=漢字
/*
 * $Id: fcfafcccbfc2747797266009f7bf0edc9f04011d $
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
 * @version $Id: fcfafcccbfc2747797266009f7bf0edc9f04011d $
 */
public class KNJA143U_6 {

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
            log.fatal("$Revision: 73976 $");
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
        
        int line = 1;
        final int maxLine = 5;
        final String form = ("1".equals(_param._formKind)) ? _param._useFormNameA143U + ".frm" : "KNJA143U_6_2.frm";
        svf.VrSetForm(form, 1);
        for (int j = 0; j < studentList.size(); j++) {
        	final Student student = (Student) studentList.get(j);

            final String schoolKind = student._schoolKind;			
            _param.setCertifSchoolDat(schoolKind);

            if ("1".equals(_param._formKind)) {
            	//生徒証明書を出力
            	printStdCertificate(db2, svf, student);
            } else {            	
                if (line > maxLine) {
                    line = 1;
                    svf.VrEndPage();
                    svf.VrSetForm(form, 1);
                }
                //通学証明書を出力
            	printTugakuCertificate(db2, svf, student, line);
            }
            line++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    //生徒証明書
    private void printStdCertificate(final DB2UDB db2, final Vrw32alp svf, Student student) {
        final String schoollogoJPath = _param.getImageFilePath("SCHOOLNAME_J.jpg");
        final String schoollogoHPath = _param.getImageFilePath("SCHOOLNAME_H.jpg");

    	final String schoolKind = student._schoolKind;
        svf.VrsOut("SCHREGNO", student._schregno); // 学籍番号
        svf.VrsOut("NAME" + (getMS932ByteLength(student._name) <= 22 ? "1" : getMS932ByteLength(student._name) <= 30 ? "2" : "3"), student._name); // 氏名
        svf.VrsOut("ENG" + (getMS932ByteLength(student._nameEng) <= 22 ? "1" : getMS932ByteLength(student._nameEng) <= 30 ? "2" : "3"), student._nameEng); // 英語氏名
        svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(db2, student._birthday)); // 生年月日

        svf.VrsOut("SCHOOLADDRESS", StringUtils.defaultString(_param._addr1) + StringUtils.defaultString(_param._telno));

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
        } else if ("H".equals(schoolKind)) {
        	logo = schoollogoHPath;
        }
        if (null != logo) {
            svf.VrsOut("SCHOOL_NAME", logo); // ロゴ
        }
        svf.VrsOut("BARCODE", student._schregno); // バーコード(学籍番号)
        
        svf.VrEndPage();    	
    }

    //通学証明書
    private void printTugakuCertificate(final DB2UDB db2, final Vrw32alp svf, Student student, int line) {

    	svf.VrsOutn("TITLE", line, "証明書");

        svf.VrsOutn("SCHREGNO", line, student._schregno);
        svf.VrsOutn("SENTENCE", line, "生徒");
        
        final int nameLen = getMS932ByteLength(student._name);
        svf.VrsOutn("NAME" + (nameLen <= 20 ? "" : nameLen <= 30 ? "2" : "3"), line, student._name);
        if (null != student._birthday) {
            final String[] birthArray = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, student._birthday));
            svf.VrsOutn("BIRTHDAY1", line, birthArray[0]);
            svf.VrsOutn("BIRTHDAY2", line, birthArray[1]);
            svf.VrsOutn("BIRTHDAY3", line, birthArray[2]);
            svf.VrsOutn("BIRTHDAY4", line, birthArray[3]);
        }

        if (null != _param._issueDate) {
            final String[] sdateArray = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, _param._issueDate));
            svf.VrsOutn("SDATE1", line, sdateArray[0]);
            svf.VrsOutn("SDATE2", line, sdateArray[1]);
            svf.VrsOutn("SDATE3", line, sdateArray[2]);
            svf.VrsOutn("SDATE4", line, sdateArray[3]);
        }

        //所在地
        final int remark1len = getMS932ByteLength(_param._addr1);
        final String remark1suf = remark1len <= 30 ? "" : remark1len <= 40 ? "_2" : remark1len <= 50 ? "_3" : "_4";
        svf.VrsOutn("SCHOOLADDRESS1" + remark1suf, line, _param._addr1); // 学校所在地
        
        //代表者名
    	 svf.VrsOutn("PRINCIPAL_NAME", line, _param._principalName);
    	//学校名画像
        final String logo = "SCHOOLNAME_" + student._schoolKind + "." + _param._extension;
    	final String schoolNameImg = _param.getImageFilePath(logo); //写真データ存在チェック用
        if (null != schoolNameImg) {
            svf.VrsOutn("SCHOOL_NAME", line, schoolNameImg);
        }
    	svf.VrsOutn("TITLE", line, "証明書");

        if (null != _param._limitDate) {
            svf.VrsOutn("LIMIT", line, KNJ_EditDate.h_format_JP(db2, _param._limitDate) + "まで有効");
        }
        final int gesyaKeta = getMS932ByteLength(student._gesyaALL);

        svf.VrsOutn("SECTION" + (gesyaKeta <= 14 ? "1" : gesyaKeta <= 20 ? "2" : "3"), line, student._gesyaALL);

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
        String _nameEng;
        String _birthday;
        String _gesyaALL;

        public static List getStudentList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = new Student();
                    student._schregno = StringUtils.defaultString(rs.getString("SCHREGNO"),"");
                    student._grade = StringUtils.defaultString(rs.getString("GRADE"),"");
                    student._hrClass = StringUtils.defaultString(rs.getString("HR_CLASS"),"");
                    student._attendno = StringUtils.defaultString(rs.getString("ATTENDNO"),"");
                    student._schoolKind = StringUtils.defaultString(rs.getString("SCHOOL_KIND"),"");
                    student._gradeCd = StringUtils.defaultString(rs.getString("GRADE_CD"),"");
                    student._gradeName1 = StringUtils.defaultString(rs.getString("GRADE_NAME1"),"");
                    student._name = StringUtils.defaultString(rs.getString("NAME"),"");
                    student._nameKana = StringUtils.defaultString(rs.getString("NAME_KANA"),"");
                    student._nameEng = StringUtils.defaultString(rs.getString("NAME_ENG"), "");
                    student._birthday = StringUtils.defaultString(rs.getString("BIRTHDAY"),"");
                    
                    if ("2".equals(param._formKind)) {
	                    String gesyaALL = "";
	                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_7"));
	                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_6"));
	                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_5"));
	                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_4"));
	                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_3"));
	                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_2"));
	                    gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_1"));
	                    gesyaALL = getGesya(gesyaALL, rs.getString("JOSYA_1"));
	                    gesyaALL += gesyaALL + '・' + (null == rs.getString("JOSYA_1") ? "" : rs.getString("JOSYA_1"));
	                    gesyaALL = "・".equals(gesyaALL) ? "" : gesyaALL;
	                    student._gesyaALL = gesyaALL;
                    }

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

        private static String getGesya(final String gesyaALL, final String gesya) {
            if (gesyaALL != "") return gesyaALL;
            if (gesya == null) return gesyaALL;
            return gesya;
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
            stb.append("     BASE.NAME_ENG, ");
            stb.append("     BASE.BIRTHDAY ");
            if ("2".equals(param._formKind)) {
            	stb.append(" , ");
                stb.append("       CASE WHEN L5.FLG_1 = '1' THEN G1.STATION_NAME ELSE L5.GESYA_1 END AS JOSYA_1, ");
                stb.append("       CASE WHEN L5.FLG_1 = '1' THEN G1.STATION_NAME ELSE L5.GESYA_1 END AS GESYA_1, ");
                stb.append("       CASE WHEN L5.FLG_2 = '1' THEN G2.STATION_NAME ELSE L5.GESYA_2 END AS GESYA_2, ");
                stb.append("       CASE WHEN L5.FLG_3 = '1' THEN G3.STATION_NAME ELSE L5.GESYA_3 END AS GESYA_3, ");
                stb.append("       CASE WHEN L5.FLG_4 = '1' THEN G4.STATION_NAME ELSE L5.GESYA_4 END AS GESYA_4, ");
                stb.append("       CASE WHEN L5.FLG_5 = '1' THEN G5.STATION_NAME ELSE L5.GESYA_5 END AS GESYA_5, ");
                stb.append("       CASE WHEN L5.FLG_6 = '1' THEN G6.STATION_NAME ELSE L5.GESYA_6 END AS GESYA_6, ");
                stb.append("       CASE WHEN L5.FLG_7 = '1' THEN G7.STATION_NAME ELSE L5.GESYA_7 END AS GESYA_7, ");
                stb.append("       CASE WHEN L5.FLG_1 = '1' THEN J1.STATION_NAME ELSE L5.JOSYA_1 END AS JOUSYA_1, ");
                stb.append("       CASE WHEN L5.FLG_2 = '1' THEN J2.STATION_NAME ELSE L5.JOSYA_2 END AS JOUSYA_2, ");
                stb.append("       CASE WHEN L5.FLG_3 = '1' THEN J3.STATION_NAME ELSE L5.JOSYA_3 END AS JOUSYA_3, ");
                stb.append("       CASE WHEN L5.FLG_4 = '1' THEN J4.STATION_NAME ELSE L5.JOSYA_4 END AS JOUSYA_4, ");
                stb.append("       CASE WHEN L5.FLG_5 = '1' THEN J5.STATION_NAME ELSE L5.JOSYA_5 END AS JOUSYA_5, ");
                stb.append("       CASE WHEN L5.FLG_6 = '1' THEN J6.STATION_NAME ELSE L5.JOSYA_6 END AS JOUSYA_6, ");
                stb.append("       CASE WHEN L5.FLG_7 = '1' THEN J7.STATION_NAME ELSE L5.JOSYA_7 END AS JOUSYA_7 ");
            }
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
            stb.append("         AND REGDG.GRADE = REGD.GRADE ");
            if ("2".equals(param._formKind)) {
	            stb.append("       LEFT JOIN SCHREG_ENVIR_DAT L5 ON L5.SCHREGNO = REGD.SCHREGNO ");
	            stb.append("       LEFT JOIN STATION_NETMST J1 ON J1.STATION_CD = L5.JOSYA_1 ");
	            stb.append("       LEFT JOIN STATION_NETMST J2 ON J2.STATION_CD = L5.JOSYA_2 ");
	            stb.append("       LEFT JOIN STATION_NETMST J3 ON J3.STATION_CD = L5.JOSYA_3 ");
	            stb.append("       LEFT JOIN STATION_NETMST J4 ON J4.STATION_CD = L5.JOSYA_4 ");
	            stb.append("       LEFT JOIN STATION_NETMST J5 ON J5.STATION_CD = L5.JOSYA_5 ");
	            stb.append("       LEFT JOIN STATION_NETMST J6 ON J6.STATION_CD = L5.JOSYA_6 ");
	            stb.append("       LEFT JOIN STATION_NETMST J7 ON J7.STATION_CD = L5.JOSYA_7 ");
	            stb.append("       LEFT JOIN STATION_NETMST G1 ON G1.STATION_CD = L5.GESYA_1 ");
	            stb.append("       LEFT JOIN STATION_NETMST G2 ON G2.STATION_CD = L5.GESYA_2 ");
	            stb.append("       LEFT JOIN STATION_NETMST G3 ON G3.STATION_CD = L5.GESYA_3 ");
	            stb.append("       LEFT JOIN STATION_NETMST G4 ON G4.STATION_CD = L5.GESYA_4 ");
	            stb.append("       LEFT JOIN STATION_NETMST G5 ON G5.STATION_CD = L5.GESYA_5 ");
	            stb.append("       LEFT JOIN STATION_NETMST G6 ON G6.STATION_CD = L5.GESYA_6 ");
	            stb.append("       LEFT JOIN STATION_NETMST G7 ON G7.STATION_CD = L5.GESYA_7 ");
            }
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
        final String _formKind;

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
            _formKind = request.getParameter("FROM_KIND");

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
                    _imagepass = StringUtils.defaultString(rs.getString("IMAGEPATH"),"");
                    _extension = StringUtils.defaultString(rs.getString("EXTENSION"),"");
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
