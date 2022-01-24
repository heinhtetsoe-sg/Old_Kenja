// kanji=漢字
/*
 * $Id: e94f78af87682177e49c123ac3ca335d8c98e81a $
 *
 * 作成日: 2005/03/25 16:07:30 - JST
 * 作成者: nakamoto
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *					＜ＫＮＪＡ１４３Ｆ＞  身分証明書（宮城県）
 **/

public class KNJA143F {

    private static final Log log = LogFactory.getLog(KNJA143F.class);

    private boolean nonedata = false;                               //該当データなしフラグ


	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
	                 throws ServletException, IOException
	{
		final Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス

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

            printSvfMain(db2, svf, param);

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
	    int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (final Exception e) {
                log.error("exception!", e);
                rtn = s.length();
            }
        }
        return rtn;
    }

	/** 帳票出力 **/
	private void printSvfMain(
		final DB2UDB db2,
		final Vrw32alp svf,
		final Param param
	) {
        svf.VrSetForm("KNJA143F.frm", 1);//カード
        final int maxCol = 3;
        final int maxLine = 4;

        final File stamp = param.getImageFile("SCHOOLSTAMP_H.bmp"); //学校長印データ存在チェック用
        final int remark1len = getMS932ByteLength(param._remark1);
        final String remark1suf = remark1len <= 30 ? "1" : remark1len <= 40 ? "2" : "3";
        final int schoolnamelen = getMS932ByteLength(param._schoolname);
        final String schoolnamesuf = schoolnamelen <= 24 ? "1" : "2";

        PreparedStatement ps = null;
	    ResultSet rs = null;

		try {
	        final String sql = sql(param);
	        ps = db2.prepareStatement(sql);
			rs = ps.executeQuery();

			int line = 1;
			int col = 0;

			for (int line1 = 1; line1 <= maxLine; line1++) {
			    for (int col1 = 1; col1 <= maxCol; col1++) {
	                final String scol = String.valueOf(col1);
	                svf.VrsOutn("TITLE" + scol, line1, "身分証明書"); // タイトル
	                svf.VrsOutn("SENTENCE" + scol, line1, "生徒"); // 証明文言
			    }
			}

			while (rs.next()) {
                col += 1;

			    if (col > maxCol) {
			        line += 1;
			        col = 1;
			    }
			    if (line > maxLine) {
			        svf.VrEndPage();
		            for (int line1 = 1; line1 <= maxLine; line1++) {
		                for (int col1 = 1; col1 <= maxCol; col1++) {
		                    final String scol = String.valueOf(col1);
		                    svf.VrsOutn("TITLE" + scol, line1, "身分証明書"); // タイトル
		                    svf.VrsOutn("SENTENCE" + scol, line1, "生徒"); // 証明文言
		                }
		            }
			        line = 1;
			    }

			    final String schregno = rs.getString("SCHREGNO");
                final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                final String birthday = rs.getString("BIRTHDAY");
                final String majorname = StringUtils.defaultString(rs.getString("MAJORNAME"));
                final String hrname = StringUtils.defaultString(rs.getString("HR_NAME"));
                final String grade1 = null == rs.getString("GRADE") ? "" : rs.getString("GRADE").length() <= 1 ? rs.getString("GRADE") : rs.getString("GRADE").substring(rs.getString("GRADE").length() - 1);
                final String hrclass1 = null == rs.getString("HR_CLASS") ? "" : rs.getString("HR_CLASS").length() <= 1 ? rs.getString("HR_CLASS") : rs.getString("HR_CLASS").substring(rs.getString("HR_CLASS").length() - 1);
                final String attendno2 = null == rs.getString("ATTENDNO") ? "" : rs.getString("ATTENDNO").length() <= 2 ? rs.getString("ATTENDNO") : rs.getString("ATTENDNO").substring(rs.getString("ATTENDNO").length() - 2);

                final String scol = String.valueOf(col);
                final String scol1 = col == 1 ? "" : "_" + scol;

                final File schregimg = param.getImageFile("P" + schregno + "." + param._extension); //写真データ存在チェック用
		        if (null != schregimg) { svf.VrsOutn("PHOTO_BMP" + scol1, line, schregimg.getPath()); } //顔写真

		        //学校印
		        if (null != stamp) { svf.VrsOutn("STAMP_BMP" + scol1, line, stamp.getPath()); } //学校印

			    //学籍番号
                if ("1".equals(param._only_OutSchreg)) {
                    svf.VrsOutn("ENT_SCHOOL" + scol, line, majorname + "　　" + schregno); // 入学学校
                } else {
                    svf.VrsOutn("ENT_SCHOOL" + scol, line, majorname + "　　" + hrname); // 入学学校
                }
                final int namelen = getMS932ByteLength(name);
                if (!"1".equals(param._only_OutSchreg)) {
                    svf.VrsOutn("NO" + scol, line, "No." + (grade1 + hrclass1 + attendno2)); // 番号
                }
                svf.VrsOutn("NAME" + scol + "_" + (namelen <= 20 ? "1" : namelen <= 30 ? "2" : "3"), line, name); // 生徒氏名
                if (!"1".equals(param._only_OutSchreg)) {
                    svf.VrsOutn("SCHREGNO" + scol, line, schregno); // 学籍番号
                }
                if (null != param._termSdate) {
                    svf.VrsOutn("SDATE" + scol, line, KNJ_EditDate.h_format_JP(param._termSdate) + "発行"); // 発行日
                }
                if (null != birthday) {
                    svf.VrsOutn("BIRTHDAY" + scol, line, KNJ_EditDate.h_format_JP(birthday) + "生"); // 生年月日
                }
                svf.VrsOutn("SCHOOLADDRESS" + remark1suf + "_" + scol, line, param._remark1); // 学校所在地
                svf.VrsOutn("TELNO" + scol, line, param._remark3); // 電話番号
                svf.VrsOutn("SCHOOLNAME" + scol + "_" + schoolnamesuf, line, param._schoolname); // 学校名
                svf.VrsOutn("JOBNAME" + scol, line, param._jobname); // 役職・氏名
                svf.VrsOutn("STAFFNAME" + scol, line, param._staffname); // 役職・氏名

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

	/**生徒情報**/
	private String sql(final Param param)
	{
		final StringBuffer stb = new StringBuffer();
        stb.append("SELECT T1.SCHREGNO, ");
        stb.append("       T2.NAME, ");
        stb.append("       T5.MAJORNAME, ");
        stb.append("       T4.HR_NAME, ");
        stb.append("       T1.GRADE, ");
        stb.append("       T1.HR_CLASS, ");
        stb.append("       T1.ATTENDNO, ");
        stb.append("       T2.REAL_NAME, ");
        stb.append("       (CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
        stb.append("       T2.BIRTHDAY ");
        stb.append("FROM   SCHREG_REGD_DAT T1 ");
        stb.append("       INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("       LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO ");
        stb.append("            AND T6.DIV = '01' ");
        stb.append("       LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T1.YEAR ");
        stb.append("            AND T4.SEMESTER = T1.SEMESTER ");
        stb.append("            AND T4.GRADE = T1.GRADE ");
        stb.append("            AND T4.HR_CLASS = T1.HR_CLASS ");
        stb.append("       LEFT JOIN MAJOR_MST T5 ON T5.COURSECD = T1.COURSECD ");
        stb.append("            AND T5.MAJORCD = T1.MAJORCD ");
        stb.append("WHERE  T1.YEAR = '" + param._year + "' AND ");
        stb.append("       T1.SEMESTER = '" + param._gakki + "' AND ");
        stb.append("       T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._schregnos) + " ");
        stb.append("ORDER BY ");
        if ("1".equals(param._printordertype)) {
            stb.append("       T1.ATTENDNO ");
        } else {
            stb.append("       T1.SCHREGNO ");
        }
		return stb.toString();
	}

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision: 70601 $ $Date: 2019-11-11 10:40:09 +0900 (月, 11 11 2019) $"); // CVSキーワードの取り扱いに注意
        Param param = new Param(db2, request);
        return param;
    }

	private static class Param {
        private final String _year;
	    private final String _gakki;
        private final String _gradeHrclass;
	    private final String[] _schregnos;
	    private final String _termSdate;
        private final String _only_OutSchreg;

        private String _jobname;
	    private String _staffname;
	    private String _schoolname;
	    private String _remark1;
        private String _remark3;

        private final String _documentRoot;
        private String _imagepath;
        private String _extension;
        private final String _useAddrField2;
        private final String _printordertype;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");                        //年度
            _gakki = request.getParameter("GAKKI");                      //学期
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");      //学年＋組
            _termSdate = request.getParameter("TERM_SDATE").replace('/','-');  // 発行日

            // 学籍番号の指定
            _schregnos = request.getParameterValues("category_selected"); //学籍番号
            loadCertifSchoolDat(db2, _year);

            _documentRoot = request.getParameter("DOCUMENTROOT");
            _printordertype = request.getParameter("PRINT_ORDER_TYPE");
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
            _useAddrField2 = request.getParameter("useAddrField2");
            _only_OutSchreg = StringUtils.defaultString(request.getParameter("ONLY_OUTSCHREG"), "");
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

            String certifKindcd = "101";
            try {
                final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + year + "' AND CERTIF_KINDCD = '" + certifKindcd + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _jobname =  rs.getString("JOB_NAME");
                    _staffname = rs.getString("PRINCIPAL_NAME");
                    _schoolname = rs.getString("SCHOOL_NAME");
                    _remark1 = rs.getString("REMARK1");
                    _remark3 = rs.getString("REMARK3");
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
