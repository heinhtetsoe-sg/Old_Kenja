// kanji=漢字
/*
 * $Id: 11bf6369cce314f8c1d734f3128233992ce44b7e $
 *
 * 作成日: 2018/08/16 14:11:30 - JST
 * 作成者: yogi
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
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
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＡ１４３W＞  生徒・職員証明書(土佐女子)
 **/

public class KNJA143W {

    private static final Log log = LogFactory.getLog(KNJA143W.class);

    private boolean nonedata = false;                               //該当データなしフラグ


    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
                     throws ServletException, IOException
    {
        final Vrw32alp svf = new Vrw32alp();            //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        PrintWriter outstrm = null;
        try {
            //  print設定
            response.setContentType("application/pdf");
            outstrm = new PrintWriter (response.getOutputStream());

            //  svf設定
            svf.VrInit();                               //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());       //PDFファイル名の設定

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            final Param param = getParam(db2, request);

            if ("1".equals(param._taishousha)) {
                printSvfMainStudent(db2, svf, param);
            } else {
                printSvfMainStaff(db2, svf, param);
            }

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();                //DBを閉じる
            outstrm.close();            //ストリームを閉じる
        }

    }//doGetの括り

    private static int getMS932ByteLength(final String s) {
    	return KNJ_EditEdit.getMS932ByteLength(s);
    }

    /** 帳票出力 **/
    private void printSvfMainStudent(
        final DB2UDB db2,
        final Vrw32alp svf,
        final Param param
    ) {
        final int maxLine = 1;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql(param);
            log.debug("sql = "+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            svf.VrSetForm("KNJA143W_2_1.frm", 1);
            int line = 1;

            while (rs.next()) {

                if (line > maxLine) {
                    svf.VrEndPage();
                    line = 1;

                    svf.VrSetForm("KNJA143W_2_2.frm", 1);
                    //有効期限
                    String[] edsplit = StringUtils.split(param._termEdate, "-");
                    svf.VrsOut("EDATE", edsplit[0] + "年"
                                         + String.valueOf(Integer.parseInt(edsplit[1])) + "月"
                                         + String.valueOf(Integer.parseInt(edsplit[2])) + "日まで有効");
                    svf.VrEndPage();
                    svf.VrSetForm("KNJA143W_2_1.frm", 1);
                }

                final String schregno = rs.getString("SCHREGNO");  //学籍番号
                final String name = rs.getString("NAME");          //名前
                final String entdate = StringUtils.replace(rs.getString("ENT_DATE"), "/", "-");      //入学日
                final String birthday = rs.getString("BIRTHDAY");  //生年月日
                final String addr1 = rs.getString("ADDR1");        //住所1
                final String addr2 = rs.getString("ADDR2");        //住所2

                //学籍番号
                svf.VrsOut("SCHREGNO", schregno);

                //名前
                final int nameLen = getMS932ByteLength(name);
                final String fieldname = "NAME" + (nameLen > 30 ? "3" : nameLen > 22 ? "2" : "");
                svf.VrsOut(fieldname, name);

                //生年月日
                String[] bdsplit = StringUtils.split(birthday, "-");
                svf.VrsOut("BIRTHDAY1", bdsplit[0] + "年"
                                         + String.valueOf(Integer.parseInt(bdsplit[1])) + "月"
                                         + String.valueOf(Integer.parseInt(bdsplit[2])) + "日");

                //住所1,2
                int addr1len = getMS932ByteLength(addr1);
                int fieldaddr1 = addr1len > 50 ? 5 : addr1len > 40 ? 4 : addr1len > 36 ? 3 : addr1len > 28 ? 2 : 1;
                int addr2len = getMS932ByteLength(addr2);
                int fieldaddr2 = addr2len > 50 ? 5 : addr2len > 40 ? 4 : addr2len > 36 ? 3 : addr2len > 28 ? 2 : 1;
                final String fieldaddr;
                if (fieldaddr1 > fieldaddr2) {
                	fieldaddr = String.valueOf(fieldaddr1);
                } else {
                	fieldaddr = String.valueOf(fieldaddr2);
                }
                svf.VrsOut("ADDRESS1_" + fieldaddr, addr1);
                svf.VrsOut("ADDRESS2_" + fieldaddr, addr2);

                //バーコード
                svf.VrsOut("BARCODE", schregno.substring(schregno.length() - 6));

                //写真
                final File schregimg = param.getImageFile("P" + schregno + "." + param._extension); //写真データ存在チェック用
                if (null != schregimg && schregimg.exists()) {
                    svf.VrsOut("PHOTO_BMP", schregimg.getPath());
                } else {
                	log.warn("PHOTO_BMP:P" + schregno + "." + param._extension + " is not exist.");
                }

                //学校住所
                int schaddrlen = getMS932ByteLength(param._addr1);
                String schaddrfield = "SCHOOLADDRESS" + (schaddrlen > 50 ? "5" : schaddrlen > 40 ? "4" : schaddrlen > 36 ? "3" : schaddrlen > 28 ? "2" : "1");
                svf.VrsOut(schaddrfield, param._addr1);

                //学校名
                svf.VrsOut("SCHOOL_NAME", param._schoolname);

                //入学年月日
                if (entdate != null) {
                    svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(entdate) + "発行");
                }

                //印鑑
                String stamp = "SCHOOLSTAMP" + (null == param._schoolKind ? "" : ("_" + param._schoolKind)) + "." + param._extension;  //学校印
                final File stmpFile = param.getImageFile(stamp);
                if (stmpFile != null && stmpFile.exists()) {
                    svf.VrsOut("STAMP_BMP", stmpFile.getPath());
                } else {
                	log.warn("STAMP_BMP is not exists.");
                }

                //学校証
                final File logoFile = param.getImageFile("SCHOOLLOGO" + ".jpg");
                if (logoFile != null && logoFile.exists()) {
                    svf.VrsOut("SCHOOL_LOGO", logoFile.getPath());
                } else {
                	log.warn("SCHOOL_LOGO is not exists.");
                }

                line++;
                nonedata = true;
            }

            if (nonedata) {
                svf.VrEndPage();
                svf.VrSetForm("KNJA143W_2_2.frm", 1);
                //有効期限
                String[] edsplit = StringUtils.split(param._termEdate, "-");
                svf.VrsOut("EDATE", edsplit[0] + "年"
                        + String.valueOf(Integer.parseInt(edsplit[1])) + "月"
                        + String.valueOf(Integer.parseInt(edsplit[2])) + "日まで有効");
                svf.VrEndPage();
            }

        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (nonedata) {
            svf.VrEndPage();
        }
    }

    /** 帳票出力 **/
    private void printSvfMainStaff(
        final DB2UDB db2,
        final Vrw32alp svf,
        final Param param
    ) {
        final int maxLine = 1;
        final int maxCol = 1;

        final int remark1len = getMS932ByteLength(param._addr1);
        //final String schoolAddrField = remark1len <= 46 ? "_1" : "_2";

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql(param);
            log.debug("sql = "+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            svf.VrSetForm("KNJA143W_1_1.frm", 1);

            int line = 1;
            int col = 1;

            while (rs.next()) {

                if (col > maxCol) {
                    col = 1;
                    line++;
                }
                if (line > maxLine) {
                    svf.VrEndPage();
                    line = 1;
                    svf.VrSetForm("KNJA143W_1_2.frm", 1);

                    //学校名+代表電話番号
                    svf.VrsOut("SCHOOL_NAME", StringUtils.defaultString(param._schoolname, "") + "　（代表電話）" + StringUtils.defaultString(param._remark3, ""));

                    svf.VrsOut("DUMMY", "　");
                    svf.VrEndPage();
                    svf.VrSetForm("KNJA143W_1_1.frm", 1);
                }

                final String staffCd = rs.getString("STAFFCD");     //教職員コード
                final String staffName = rs.getString("STAFFNAME"); //教職員氏名
                final String sectionname = rs.getString("SECTIONNAME"); //所属名

                //教職員コード
                svf.VrsOut("SCHREGNO", staffCd);

                //職名
                svf.VrsOut("JOB_NAME", sectionname);

                //教職員氏名
                final int nameLen = getMS932ByteLength(staffName);
                svf.VrsOut("NAME" + (nameLen > 30 ? "4" : nameLen > 20 ? "3" : nameLen > 12 ? "2" : ""), staffName);

                ////発行日
                //String[] sdsplit = StringUtils.split(param._termSdate, "-");
                //svf.VrsOut("SDATE", sdsplit[0] + "年"
                //                    + String.valueOf(Integer.parseInt(sdsplit[1])) + "月"
                //                    + String.valueOf(Integer.parseInt(sdsplit[2])) + "日");

                //写真
                final File schregimg = param.getImageFile("T" + staffCd + "." + param._extension); //写真データ存在チェック用
                if (null != schregimg && schregimg.exists()) {
                    svf.VrsOut("PHOTO_BMP", schregimg.getPath());
                } else {
                	log.warn("PHOTO_BMP:S" + staffCd + "." + param._extension + " is not exist.");
                }

                //バーコード
                svf.VrsOut("BARCODE", staffCd.substring(staffCd.length() - 6));

                ////学校住所
                //int schaddrlen = getMS932ByteLength(param._addr1);
                //String schaddrfield = "SCHOOLADDRESS" + (schaddrlen > 50 ? "5" : schaddrlen > 40 ? "4" : schaddrlen > 36 ? "3" : schaddrlen > 28 ? "2" : "1");
                //svf.VrsOut(schaddrfield, param._addr1);

                //学校名
                svf.VrsOut("SCHOOL_NAME", param._schoolname);

                ////学校長
                //svf.VrsOut("PRINCIPAL_NAME", param._principalName);

                //印鑑
                String stamp = "SCHOOLSTAMP" + (null == param._schoolKind ? "" : ("_" + param._schoolKind)) + "." + param._extension;  //学校印
                final File stmpFile = param.getImageFile(stamp);
                if (stmpFile != null && stmpFile.exists()) {
                    svf.VrsOut("STAMP_BMP", stmpFile.getPath());
                } else {
                	log.warn("STAMP_BMP is not exists.");
                }

                //学校証
                final File logoFile = param.getImageFile("SCHOOLLOGO" + ".jpg");
                if (logoFile != null && logoFile.exists()) {
                    svf.VrsOut("SCHOOL_LOGO", logoFile.getPath());
                } else {
                	log.warn("SCHOOL_LOGO is not exists.");
                }

                col++;
                nonedata = true;
            }

            if (nonedata) {
                svf.VrEndPage();
                svf.VrSetForm("KNJA143W_1_2.frm", 1);

                //学校名+代表電話番号
                svf.VrsOut("SCHOOL_NAME", StringUtils.defaultString(param._schoolname, "") + "　（代表電話）" + StringUtils.defaultString(param._remark3, ""));

                svf.VrsOut("DUMMY", "　");
                svf.VrEndPage();
            }

        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (nonedata) {
            svf.VrEndPage();
        }
    }

    private String getGesya(final String gesyaALL, final String gesya) {
        if (gesyaALL != "") return gesyaALL;
        if (gesya == null) return gesyaALL;
        return gesya;
    }

    /**生徒又は職員情報**/
    private String sql(final Param param)
    {
        final StringBuffer stb = new StringBuffer();
        if ("1".equals(param._taishousha)) {
            stb.append(" WITH SCHREG_ADDRESS_MAX AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, ");
            stb.append("         MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("     FROM ");
            stb.append("         SCHREG_ADDRESS_DAT ");
            stb.append("     GROUP BY ");
            stb.append("         SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , SCHREG_ADDRESS AS ( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.ZIPCD, ");
            stb.append("         P1.PREF_CD, ");
            stb.append("         P1.PREF_NAME, ");
            stb.append("         T1.AREACD, ");
            stb.append("         N1.NAME1 AS AREA_NAME, ");
            stb.append("         T1.ADDR1, ");
            stb.append("         T1.ADDR2, ");
            stb.append("         T1.TELNO ");
            stb.append("     FROM ");
            stb.append("         SCHREG_ADDRESS_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_ADDRESS_MAX T2 ");
            stb.append("             ON  T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T2.ISSUEDATE = T1.ISSUEDATE ");
            stb.append("         LEFT JOIN ZIPCD_MST Z1 ON Z1.NEW_ZIPCD = T1.ZIPCD ");
            stb.append("         LEFT JOIN PREF_MST P1 ON P1.PREF_CD = SUBSTR(Z1.CITYCD,1,2) ");
            stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A020' AND N1.NAMECD2 = T1.AREACD ");
            stb.append("     ) ");
        	stb.append(" SELECT ");
        	stb.append("   SBM.SCHREGNO, ");
        	stb.append("   SBM.NAME, ");
        	stb.append("   SBM.BIRTHDAY, ");
        	stb.append("   SBM.ENT_DATE, ");
        	stb.append("   SA.ZIPCD, ");
        	stb.append("   SA.ADDR1, ");
        	stb.append("   SA.ADDR2 ");
        	stb.append(" FROM ");
        	stb.append("   SCHREG_BASE_MST SBM ");
        	stb.append("   LEFT JOIN SCHREG_ADDRESS SA ");
        	stb.append("      ON SA.SCHREGNO = SBM.SCHREGNO ");
        	stb.append("   LEFT JOIN SCHREG_REGD_DAT SRD ");
        	stb.append("      ON SRD.SCHREGNO = SBM.SCHREGNO ");
        	stb.append("     AND SRD.YEAR = '" + param._year + "' ");
        	stb.append("     AND SRD.SEMESTER = '" + param._semester + "' ");

        	stb.append(" WHERE ");
        	stb.append("   SBM.SCHREGNO IN " + SQLUtils.whereIn(true, param._schregnos) + " ");
        	stb.append(" ORDER BY ");
        	stb.append("   SRD.GRADE, SRD.HR_CLASS, SRD.ATTENDNO ");
        } else {
        	stb.append(" SELECT ");
        	stb.append("   T1.STAFFCD, ");
        	stb.append("   T1.STAFFNAME, ");
        	stb.append("   T2.SECTIONNAME ");
            stb.append(" FROM ");
            stb.append("    V_STAFF_MST T1 ");
            stb.append("    LEFT JOIN SECTION_MST T2 ON T2.SECTIONCD = T1.SECTIONCD ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '" + param._year + "' ");
            stb.append("AND T1.STAFFCD IN " + SQLUtils.whereIn(true, param._schregnos) + " ");
            stb.append("ORDER BY ");
            stb.append("    T1.STAFFCD ");
        }
        return stb.toString();
    }

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision: 67039 $ $Date: 2019-04-17 09:54:39 +0900 (水, 17 4 2019) $"); // CVSキーワードの取り扱いに注意
        Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _grade;
        private final String _gradeHrclass;
        private final String[] _selectnos;
        private final String[] _schregnos;
        private final String _termSdate;
        private final String _termEdate;
        private final String _taishousha;
        private final String _selClassType;
        private final String _schoolKind;
        private final String _schoolCd;

        private String _jobname;
        private String _principalName;
        private String _schoolname;
        private String _schoolname2;
        private String _addr1;
        private String _addr2;
        private String _remark3;

        private final String _documentRoot;
        private String _imagepath;
        private String _extension;
//        private final String _useAddrField2;
//        private final String _useFormNameA143H;
//        private final String _usePrgSchoolkind;
//        private final String _selectschoolkind;
//        private final String _useschoolKindfield;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");                        //年度
            //_termEdate = null == request.getParameter("TERM_EDATE") ? null : request.getParameter("TERM_EDATE").replace('/','-');  // 有効期限
            _taishousha = request.getParameter("TAISHOUSHA");            //対象者 (1:生徒,2:教職員)
            _schoolCd = request.getParameter("SCHOOLCD");
        	_schoolKind = request.getParameter("SCHKIND");
            if ("1".equals(_taishousha) ) {
                _semester = request.getParameter("SEMESTER");                //学期
        		_grade = request.getParameter("GRADE");      //学年
            	_termSdate = "";
            	_termEdate = request.getParameter("TERM_EDATE").replace('/','-');
            	_selClassType = request.getParameter("SEL_CLASSTYPE");
            	if ("1".equals(_selClassType)) {
            		_gradeHrclass = "";
            		_selectnos = request.getParameterValues("category_selected"); //学年+学級
                    _schregnos = getSchregNos(db2, _year, _semester, _selectnos); //選択学級のSCHREGNO配列を取得。
            	} else {
                    _gradeHrclass = request.getParameter("GRADE_HR_CLASS");      //学年＋組
            		_selectnos = new String[0];
                    _schregnos = request.getParameterValues("category_selected"); //学籍番号
            	}
            	//_schoolKind = getSchoolKind(db2,_grade);
            } else {
            	_semester = "";
            	_selClassType = "";
        		_grade = request.getParameter("GRADE");      //学年
        		_gradeHrclass = "";
        		//_schoolKind = getSchoolKind(db2,_grade);
                _termSdate = request.getParameter("TERM_SDATE").replace('/','-');  // 発行日
                _termEdate = "";
        		_selectnos = new String[0];
                _schregnos = request.getParameterValues("category_selected"); //教職員コード
            }

            loadCertifSchoolDat(db2, _year, _schoolKind);
            if ("2".equals(_taishousha) ) {
                loadSchoolMst(db2, _year, _schoolKind, _schoolCd);
            }

            _documentRoot = request.getParameter("DOCUMENTROOT");
            //  写真データ
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            try {
                returnval = getinfo.Control(db2);
                _imagepath = returnval.val4;      //格納フォルダ
                _extension = returnval.val5;      //拡張子
            } catch (Exception e) {
                log.error("setHeader set error!", e);
            } finally {
                getinfo = null;
                returnval = null;
            }
            //_useAddrField2 = request.getParameter("useAddrField2");
            //_useFormNameA143H = request.getParameter("useFormNameA143H");
            //_usePrgSchoolkind = request.getParameter("use_prg_schoolkind");
            //_selectschoolkind = request.getParameter("selectSchoolKind");
            //_useschoolKindfield = request.getParameter("useSchool_KindField");
        }

        public File getImageFile(final String name) {
            final File file = new File(_documentRoot + "/" + _imagepath + "/" + name);
            //log.info(" file " + file.getAbsolutePath() + " exists? " + file.exists());
            if (file.exists()) {
                return file;
            }
            return null;
        }

//        private String getSchoolKind(final DB2UDB db2, final String grade) {
//            String retStr = "";
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//
//            try {
//                final String sql = getSchoolKind(grade);
//                log.debug(" sql =" + sql);
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//
//                while (rs.next()) {
//                    retStr = rs.getString("SCHOOL_KIND");
//                }
//
//            } catch (Exception e) {
//                log.error("getSchoolKind error!", e);
//            } finally {
//                db2.commit();
//                DbUtils.closeQuietly(null, ps, rs);
//            }
//            return retStr;
//        }

        private String getSchoolKind(final String grade) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GDAT.SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT GDAT ");
            stb.append(" WHERE ");
            stb.append("     GDAT.YEAR = '" + _year + "' ");
            stb.append("     AND GDAT.GRADE = '" + grade + "' ");
            return stb.toString();
        }

        private String[] getSchregNos(final DB2UDB db2, final String year, final String semester, final String[] selectnos) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String retstr = "";
        	String sep = "";
        	String nosInState = "";

        	for (int ii = 0;ii < selectnos.length;ii++) {
        		nosInState += sep + "'" + selectnos[ii] + "'";
        		sep = ",";
        	}
        	nosInState = "(" + nosInState + ")";

        	String sql = " SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS IN " + nosInState + " ORDER BY GRADE, HR_CLASS, ATTENDNO, SCHREGNO ";
        	log.debug("schregno sql = " + sql);
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                sep = "";
                while (rs.next()) {
                	retstr += sep + rs.getString("SCHREGNO");
                	sep = ",";
                }
            } catch (Exception e) {
                log.error("getSchregNos error!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

        	return StringUtils.split(retstr, ",");
        }
        public void loadCertifSchoolDat(final DB2UDB db2, final String year, final String schoolkind) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String certifKindcd;
            if ("H".equals(schoolkind)) {
                certifKindcd = "101";
            } else {
                certifKindcd = "102";
            }
            try {
                final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + year + "' AND CERTIF_KINDCD = '" + certifKindcd + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _jobname =  rs.getString("JOB_NAME");
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _schoolname = rs.getString("SCHOOL_NAME");
                    _addr1 = rs.getString("REMARK1");
                    _addr2 = rs.getString("REMARK2");
                    _remark3 = rs.getString("REMARK3");
                }
            } catch (Exception e) {
                log.error("loadCertifSchoolDat error!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public void loadSchoolMst(final DB2UDB db2, final String year, final String schoolkind, final String schoolcd) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT SCHOOLNAME2, SCHOOLTELNO FROM SCHOOL_MST WHERE YEAR = '" + year + "' AND SCHOOLCD = '" + schoolcd + "' AND SCHOOL_KIND = '" + schoolkind + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	_schoolname = rs.getString("SCHOOLNAME2");
                	_remark3 = rs.getString("SCHOOLTELNO");
                }
            } catch (Exception e) {
                log.error("loadCertifSchoolDat error!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

        }
    }

}//クラスの括り
