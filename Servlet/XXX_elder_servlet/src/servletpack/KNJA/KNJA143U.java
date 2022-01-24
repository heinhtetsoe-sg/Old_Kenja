// kanji=漢字
/*
 * $Id: 29ce9373f033ed5b33d4b4de54cd576274bbca47 $
 *
 * 作成日: 2017/08/14 10:24:21 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 29ce9373f033ed5b33d4b4de54cd576274bbca47 $
 */
public class KNJA143U {

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
            log.fatal("$Revision: 58584 $");
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

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
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
            	form = "KNJA143U_1.frm";
            } else if ("H".equals(schoolKind)) {
            	form = "KNJA143U_2.frm";
            } else {
            	continue;
            }
            _param.setCertifSchoolDat(schoolKind);
            
            svf.VrSetForm(form, 1);
            
            svf.VrsOut("SCHREGNO", student._schregno); // 学籍番号
            svf.VrsOut("GRADE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　第" + digitsToZenkaku(String.valueOf(Integer.parseInt(student._gradeCd))) + "学年"); // 年度
            svf.VrsOut("NAME" + (getMS932ByteLength(student._name) <= 20 ? "" : getMS932ByteLength(student._name) <= 30 ? "2" : "3"), student._name); // 氏名
            svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_SeirekiJP(student._birthday)); // 生年月日
            final int addrlen = Math.max(getMS932ByteLength(student._addr1), getMS932ByteLength(student._addr2));
            if (addrlen <= 30) {
                svf.VrsOut("ADDRESS1_1", student._addr1); // 住所
                svf.VrsOut("ADDRESS2_1", student._addr2); // 住所
            } else if (addrlen <= 40) {
                svf.VrsOut("ADDRESS1_2", student._addr1); // 住所
                svf.VrsOut("ADDRESS2_2", student._addr2); // 住所
            } else {
                svf.VrsOut("ADDRESS1_3", student._addr1); // 住所
                svf.VrsOut("ADDRESS2_3", student._addr2); // 住所
            }
            
            svf.VrsOut("SCHOOLADDRESS", StringUtils.defaultString(_param._addr1) + StringUtils.defaultString(_param._telno));
            svf.VrsOut("SCHOOLNAME", _param._schoolname);
            svf.VrsOut("JOBNAME", _param._jobname);
            svf.VrsOut("STAFFNAME", _param._principalName);

            svf.VrsOut("SDATE", KNJ_EditDate.h_format_SeirekiJP(_param._issueDate)); // 発行日
            svf.VrsOut("LDATE", KNJ_EditDate.h_format_SeirekiJP(_param._limitDate)); // 有効期限
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
            _hasData = true;
        }
    }

    private String digitsToZenkaku(final String s) {
    	final StringBuffer stb = new StringBuffer();
    	for (int i = 0; i < s.length(); i++) {
    		char ch = s.charAt(i);
    		if ('0' <= ch && ch <= '9') {
    			ch = (char) (ch - '0' + (int) '\uFF10');
    		}
    		stb.append(ch);
    	}
		return stb.toString();
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
        String _addr1;
        String _addr2;

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
                    student._addr1 = rs.getString("ADDR1");
                    student._addr2 = rs.getString("ADDR2");
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
            stb.append("     ADDR.ZIPCD, ");
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
