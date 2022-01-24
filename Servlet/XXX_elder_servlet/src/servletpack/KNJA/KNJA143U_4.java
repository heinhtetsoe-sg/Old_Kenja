// kanji=漢字
/*
 * $Id: 21d01c5a253eac0a77d8a3aa1d5e2baac11bdebd $
 *
 * 作成日: 2019/03/05 11:28:50 - JST
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
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
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＡ１４３H＞  生徒・職員証明書（三重県）
 **/

public class KNJA143U_4 {

    private static final Log log = LogFactory.getLog(KNJA143U_4.class);

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

            printSvfMainStudent(db2, svf, param);

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

    public static String getString(final Map row, String field) {
        if (null == row || row.isEmpty()) {
            return null;
        }
        field = field.toUpperCase();
        if (!row.containsKey(field)) {
            throw new IllegalStateException("no such field : " + field + " / " + row);
        }
        return (String) row.get(field);
    }

    private String getFormatSeirekiZeroBlk(final String dayStr, final String delimStr) {
        final String[] dArray = StringUtils.split(dayStr, delimStr);
        final String monthsp = Integer.parseInt(dArray[1]) < 10 ? " " : "";
        final String daysp = Integer.parseInt(dArray[2]) < 10 ?  " " : "";
    	final String retDayStr =  dArray[0] + "年" + monthsp + Integer.parseInt(dArray[1]) + "月" + daysp+ Integer.parseInt(dArray[2]) + "日";
    	return retDayStr;
    }

    /** 帳票出力(生徒証) **/
    private void printSvfMainStudent(
        final DB2UDB db2,
        final Vrw32alp svf,
        final Param param
    ) {
        final int maxLine;
        if ("KNJA143U_4".equals(param._useFormNameA143U)) {
            maxLine = 3;
        } else {
            maxLine = 4;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql(param);
            log.debug("sql:"+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            svf.VrSetForm(param._useFormNameA143U + ".frm", 1);
            int line = 1;

            while (rs.next()) {

                if (line > maxLine) {
                    svf.VrEndPage();
                    line = 1;
                }

                final String schregno   = rs.getString("SCHREGNO");
                final String name       = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                final String birthday   = rs.getString("BIRTHDAY");
                final String addr1      = rs.getString("ADDR1");       //生徒住所1
                final String addr2      = rs.getString("ADDR2");       //生徒住所2
                final String entYear =  rs.getString("ENTYEAR");       //入学年度

                final File schregimg = param.getImageFile("P" + schregno + "." + param._extension); //写真データ存在チェック用
                if (null != schregimg) {
                    svf.VrsOutn("PHOTO_BMP", line, schregimg.getPath());
                }

                if ("J".equals(param._schoolkind)) {
                	svf.VrsOutn("COURSE1", line, "中等部");
                	svf.VrsOutn("GRADE_COURSE", line, "義務教育");
                } else {
                	svf.VrsOutn("COURSE1", line, "高等部");
                	svf.VrsOutn("GRADE_COURSE", line, "たの２０　高等課程");
                }
                svf.VrsOutn("SCHREGNO", line, schregno);
                svf.VrsOutn("ENT_YEAR", line, entYear + "年度入学生");

                final int nameLen = KNJ_EditEdit.getMS932ByteLength(name);
                final String nfield = nameLen > 32 ? "_3" : nameLen > 26 ? "_2" : "_1";
                //氏名
                svf.VrsOutn("NAME1" + nfield, line, name);
                //生年月日
                if (null != birthday) {
                	final String bdayStr = getFormatSeirekiZeroBlk(birthday, "-") + "生";
                	svf.VrsOutn("BIRTHDAY", line, bdayStr);
                }

                //フォントサイズを統一するため、最大文字数のサイズに合わせる。
                final String outaddrstr = addr1 + ("".equals(addr2) ? "" : " " + addr2);
                final int addr1Len = KNJ_EditEdit.getMS932ByteLength(addr1);
                final int addr2Len = KNJ_EditEdit.getMS932ByteLength(addr2);
                final int addrLen = KNJ_EditEdit.getMS932ByteLength(outaddrstr);
                final int maxaddrlen = addr1Len < addr2Len ? addr2Len : addr1Len;
                //折り返しを考慮する。フィールドは倍の数で判定。
                final int addrcutsize;
                final String addrfield;
                final String addrstr[];

                //生徒住所
                if (addr1Len <= 60 && addr2Len <= 60) {
                	//1列目、2列目の文字数合計がそれぞれで収まる場合、大きい文字数のフォントサイズに合わせる。
                    if (addrLen <= 34 ) {  //合計して1行で収まる
                        svf.VrsOutn("ADDRESS1", line, outaddrstr);         //住所
                        addrfield   = null;
                    } else if (maxaddrlen > 50) {
                        addrfield   = "_4";
                    } else if (maxaddrlen > 42) {
                        addrfield   = "_3";
                    } else if (maxaddrlen > 34) {
                        addrfield   = "_2";
                    } else {
                        addrfield   = "";
                    }
                    if (null != addrfield) {
                    	svf.VrsOutn("ADDRESS1" + addrfield, line, addr1);   //住所
                    	svf.VrsOutn("ADDRESS2" + addrfield, line, addr2);   //住所
                    }
                } else {
                	//収まらない場合、詰める処理を行う。
                    addrcutsize = addrLen > 100 ? 60 : addrLen > 82 ? 50   : addrLen > 68 ? 42 : 34;
                    addrfield   = addrLen > 50 ? "_4" : addrLen > 42 ? "_3" : addrLen > 34 ? "_2" : "_1";
                    addrstr = KNJ_EditEdit.get_token(outaddrstr, addrcutsize, 2);
                    svf.VrsOutn("ADDRESS1" + addrfield, line, addrstr[0]);         //住所
                    svf.VrsOutn("ADDRESS2" + addrfield, line, addrstr[1]);         //住所
                }

            	final String sdatOutStr = getFormatSeirekiZeroBlk(param._issueDate, "-") + "発行";
                svf.VrsOutn("PRINT_DATE", line, sdatOutStr);

                svf.VrsOutn("LIMIT_DATE", line, KNJ_EditDate.h_format_SeirekiJP(param._limitDate) + "まで有効"); // 有効期限
                svf.VrsOutn("JOB_NAME", line, param._jobname); //役職名
                final String pnfield = KNJ_EditEdit.getMS932ByteLength(param._principalName) < 24 ? "1" : "2";
                svf.VrsOutn("STAFF_NAME" + pnfield, line, param._principalName); //校長名

                line++;
                nonedata = true;
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

    /**生徒又は職員情報**/
    private String sql(final Param param)
    {
        final StringBuffer stb = new StringBuffer();
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
            stb.append("SELECT T1.SCHREGNO, ");        //学年
            stb.append("       CASE WHEN MONTH(T2.ENT_DATE) < 4 THEN INT(YEAR(T2.ENT_DATE)) - 1 ELSE INT(YEAR(T2.ENT_DATE)) END AS ENTYEAR, ");  //入学年度
            stb.append("       T2.NAME, ");            //氏名
            stb.append("       T5.MAJORNAME, ");       //学科
            stb.append("       T4.HR_NAME, ");         //クラス名
            stb.append("       T4.HR_NAMEABBV, ");     //クラス名略称
            stb.append("       T4.HR_CLASS_NAME1, ");  //クラス名
            stb.append("       T1.GRADE, ");           //学年(コード)
            stb.append("       T6.GRADE_CD, ");        //学年(コード)
            stb.append("       T1.HR_CLASS, ");        //組(コード)
            stb.append("       T1.ATTENDNO, ");        //出席番号
            stb.append("       T2.REAL_NAME, ");       //氏名
            stb.append("       (CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");  //氏名どっち使うフラグ
            stb.append("       T2.BIRTHDAY, ");        //誕生日
            stb.append("       CASE WHEN T2.BIRTHDAY IS NOT NULL THEN YEAR('" + param._issueDate + "' - T2.BIRTHDAY) END AS AGE, "); //年齢
            stb.append("       CASE WHEN L5.FLG_1 = '1' THEN G1.STATION_NAME ELSE L5.GESYA_1 END AS GESYA_1, ");
            stb.append("       CASE WHEN L5.FLG_2 = '1' THEN G2.STATION_NAME ELSE L5.GESYA_2 END AS GESYA_2, ");
            stb.append("       CASE WHEN L5.FLG_3 = '1' THEN G3.STATION_NAME ELSE L5.GESYA_3 END AS GESYA_3, ");
            stb.append("       CASE WHEN L5.FLG_4 = '1' THEN G4.STATION_NAME ELSE L5.GESYA_4 END AS GESYA_4, ");
            stb.append("       CASE WHEN L5.FLG_1 = '1' THEN J1.STATION_NAME ELSE L5.JOSYA_1 END AS JOSYA_1, ");
            stb.append("       CASE WHEN L5.FLG_2 = '1' THEN J2.STATION_NAME ELSE L5.JOSYA_2 END AS JOSYA_2, ");
            stb.append("       CASE WHEN L5.FLG_3 = '1' THEN J3.STATION_NAME ELSE L5.JOSYA_3 END AS JOSYA_3, ");
            stb.append("       CASE WHEN L5.FLG_4 = '1' THEN J4.STATION_NAME ELSE L5.JOSYA_4 END AS JOSYA_4, ");
            stb.append("       VALUE(ADDR.ADDR1, '') AS ADDR1, ");   //住所1
            stb.append("       VALUE(ADDR.ADDR2, '') AS ADDR2, ");   //住所2
            stb.append("       T6.SCHOOL_KIND ");                    //学年校種
            stb.append("FROM   SCHREG_REGD_DAT T1 ");
            stb.append("       INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("       LEFT JOIN SCHREG_ADDRESS ADDR ON ADDR.SCHREGNO = T1.SCHREGNO ");
            stb.append("       LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO ");
            stb.append("            AND T6.DIV = '01' ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("            AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("            AND T4.GRADE = T1.GRADE ");
            stb.append("            AND T4.HR_CLASS = T1.HR_CLASS ");
            stb.append("       LEFT JOIN MAJOR_MST T5 ON T5.COURSECD = T1.COURSECD ");
            stb.append("            AND T5.MAJORCD = T1.MAJORCD ");
            stb.append("       LEFT JOIN SCHREG_REGD_GDAT T6 ");
            stb.append("            ON T6.YEAR = T1.YEAR ");
            stb.append("            AND T6.GRADE = T1.GRADE ");
            stb.append("       LEFT JOIN SCHREG_ENVIR_DAT L5 ON L5.SCHREGNO = T1.SCHREGNO ");
            stb.append("       LEFT JOIN STATION_NETMST J1 ON J1.STATION_CD = L5.JOSYA_1 ");
            stb.append("       LEFT JOIN STATION_NETMST J2 ON J2.STATION_CD = L5.JOSYA_2 ");
            stb.append("       LEFT JOIN STATION_NETMST J3 ON J3.STATION_CD = L5.JOSYA_3 ");
            stb.append("       LEFT JOIN STATION_NETMST J4 ON J4.STATION_CD = L5.JOSYA_4 ");
            stb.append("       LEFT JOIN STATION_NETMST G1 ON G1.STATION_CD = L5.GESYA_1 ");
            stb.append("       LEFT JOIN STATION_NETMST G2 ON G2.STATION_CD = L5.GESYA_2 ");
            stb.append("       LEFT JOIN STATION_NETMST G3 ON G3.STATION_CD = L5.GESYA_3 ");
            stb.append("       LEFT JOIN STATION_NETMST G4 ON G4.STATION_CD = L5.GESYA_4 ");
            stb.append("WHERE  T1.YEAR = '" + param._year + "' AND ");
            stb.append("       T1.SEMESTER = '" + param._semester + "' AND ");
            stb.append("       T1.SCHREGNO IN (" + param._findschreg + ") ");
            stb.append("ORDER BY ");
            stb.append("       T1.ATTENDNO ");
        return stb.toString();
    }

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision: 66617 $ $Date: 2019-03-29 10:08:18 +0900 (金, 29 3 2019) $"); // CVSキーワードの取り扱いに注意
        Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _disp;
        private final String[] _gradeHrclass;
        private final String[] _schregnos;

        private final String _issueDate;
        private final String _limitDate;

        private final String _documentRoot;
        private String _imagepath;
        private String _extension;
        private final String _useFormNameA143U;

        private final String _schoolkind;

        private final String _findgr_hr;
        private final String _findschreg;

        private String _jobname;
        private String _principalName;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("CTRL_YEAR");                   //年度
            _semester = request.getParameter("SEMESTER");                   //学期

            _disp = request.getParameter("DISP");

            if ("2".equals(_disp)) {
            	_gradeHrclass = new String[1];
            	_gradeHrclass[0] = request.getParameter("GRADE_HR_CLASS");    //学年＋組
                _findgr_hr = conv_arystr_to_str(_gradeHrclass, ",");
            	_schregnos = request.getParameterValues("CATEGORY_SELECTED"); //学籍番号
            } else {
                _gradeHrclass = request.getParameterValues("CATEGORY_SELECTED"); //学籍番号
                _findgr_hr = conv_arystr_to_str(_gradeHrclass, ",");
                _schregnos = getSchregnos(db2);
            }
            _findschreg = conv_arystr_to_str(_schregnos, ",", "-", 1);

            _issueDate = request.getParameter("ISSUE_DATE").replace('/','-');
            _limitDate = StringUtils.defaultString(request.getParameter("LIMIT_DATE")).replace('/', '-');

            _schoolkind = request.getParameter("SCHOOL_KIND");

        	_useFormNameA143U = request.getParameter("USEFORMNAMEA143U");

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

            loadCertifSchoolDat(db2, _year);
        }

        private String conv_arystr_to_str(final String[] strary, final String sep) {
            return conv_arystr_to_str(strary, sep, "", 0);
        }
        private String conv_arystr_to_str(final String[] strary, final String sep, final String delim, final int cutno) {
        	String convgr_hr = "";
        	String sepwk = "";
        	for (int ii = 0;ii < strary.length;ii++) {
        		String cutwkstr[];
        		int idx = 0;
        		if (!"".equals(delim) && cutno > 0) {
        			cutwkstr = StringUtils.split(strary[ii], delim);
        			idx = cutno - 1;
        		} else {
        			cutwkstr = new String[1];
        			cutwkstr[0] = strary[ii];
        		}
        		convgr_hr += sepwk + "'" + cutwkstr[idx] + "'";
        		sepwk = sep;
        	}
        	return convgr_hr;
        }

        private String[] getSchregnos(final DB2UDB db2) {
        	String[] retstrlist;
        	List retwklist = new ArrayList();

        	String schregno_get_sql = "SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '"+_year+"' AND SEMESTER = '"+_semester+"' AND GRADE || HR_CLASS IN ("+_findgr_hr+") ";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(schregno_get_sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	retwklist.add(rs.getString("SCHREGNO"));
                }
            } catch (Exception ex) {
                log.error("setSvfout set error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

        	retstrlist = (String[])retwklist.toArray(new String[retwklist.size()]);
        	return retstrlist;
        }
        public File getImageFile(final String name) {
            final File file = new File(_documentRoot + "/" + _imagepath + "/" + name);
            if (file.exists()) {
                return file;
            }
            return null;
        }

        public void loadCertifSchoolDat(final DB2UDB db2, final String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String certifKindcd = "H".equals(_schoolkind) ? "101" : "102";
            try {
                final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + year + "' AND CERTIF_KINDCD = '" + certifKindcd + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _jobname =  rs.getString("JOB_NAME");
                    _principalName = StringUtils.defaultString(rs.getString("PRINCIPAL_NAME"));

                }
            } catch (Exception e) {
                log.error("setHeader name_mst error!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

    }

}//クラスの括り
