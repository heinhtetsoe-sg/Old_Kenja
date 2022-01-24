// kanji=漢字
/*
 * $Id: 60a5198f150459d800cae3956ba9b18ee11d29f4 $
 *
 * 作成日: 2005/03/25 16:07:30 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *					＜ＫＮＪＡ１４３Ｅ＞  身分証明書（宮城県美田園）
 **/

public class KNJA143E {

    private static final Log log = LogFactory.getLog(KNJA143E.class);

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
    private boolean nonedata = false;                               //該当データなしフラグ


	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
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
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (final Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

	/** 帳票出力 **/
	private void printSvfMain(
		final DB2UDB db2,
		final Vrw32alp svf,
		final Param param
	) {
        svf.VrSetForm("KNJA143E.frm", 1);//カード
	    
	    PreparedStatement ps = null;
	    ResultSet rs = null;
	    
		try {
	        final String sql = preStat1(param);
	        ps = db2.prepareStatement(sql);
			rs = ps.executeQuery();

//            final String stamp_check = param._documentRoot + "/" + param._imagepath + "/" + ("SCHOOLSTAMP" + param._schoolStampExtension);
//		      final File f2 = new File(stamp_check);        //学校長印データ存在チェック用

			while (rs.next()) {
                
//			    final String photo_check = param._documentRoot + "/" + param._imagepath + "/" + ("P" + rs.getString("SCHREGNO") + "." + param._extension);
//			    final File f1 = new File(photo_check);//写真データ存在チェック用
//		        if (f1.exists()) { 
//		            svf.VrsOut("PHOTO_BMP"    , photo_check );//顔写真
//		        }
//
//		        //学校印
//		        if (f2.exists()) { 
//    		        svf.VrsOut("STAMP_BMP"    , stamp_check );//学校印
//		        }
		        
			    //学籍番号
				svf.VrsOut("SCH_NO", rs.getString("SCHREGNO"));
				//氏名(漢字)
				final String[] name = KNJ_EditEdit.get_token("1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME"), 22, 2);
				if (null != name) {
				    for (int i = 0; i < name.length; i++) {
	                    svf.VrsOut("NAME" + (i + 1), name[i]);
				    }
				}
				
				// 住所
//				final String[] addr = split3ByMS932Byte(rs.getString("ADDR"), 18, 22);
//				for (int i = 0; i < addr.length; i++) {
//	                svf.VrsOut("ADDRESS" + (i + 1), addr[i]);
//				}
				
                String[] addr;
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                boolean useField2 = false;
                if ("1".equals(param._useAddrField2) && (getMS932ByteLength(addr1) + getMS932ByteLength(addr2) > 60)) {
                    useField2 = true;
                    addr = KNJ_EditEdit.get_token(addr1 + addr2, 40, 2);
                    if (null == addr) {
                        addr = new String[] {"", ""};
                    }
                } else if (getMS932ByteLength(addr1) > 36 || getMS932ByteLength(addr2) > 36) {
				    addr = KNJ_EditEdit.get_token(addr1 + addr2, 36, 3);
				    if (null == addr) {
				        addr = new String[] {"", ""};
				    }
				} else {
				    addr = new String[] {addr1, addr2};
				}
                if (useField2) {
                    for (int i = 0; i < addr.length; i++) {
                        svf.VrsOut("ADDRESS1_" + String.valueOf(i + 1) + "_2", addr[i]);
                    }
                } else {
                    for (int i = 0; i < addr.length; i++) {
                        svf.VrsOut("ADDRESS1_" + String.valueOf(i + 1), addr[i]);
                    }
                }

				// 生年月日
				svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY")));
				// 発行日
        		svf.VrsOut("PRINTDAY", KNJ_EditDate.h_format_JP(param._termSdate));
        		// 有効期限
                svf.VrsOut("LIMITDAY", KNJ_EditDate.h_format_JP(param._termEdate));

        		// 発行者情報
                // 職名
                svf.VrsOut("JOB_NAME", param._jobname);
                // 職員名
                svf.VrsOut("PRINCIPAL", param._staffname);
                // 住所
                String[] schaddr;
                final String schaddr1 = param._remark4;
                final String schaddr2 = param._remark5;
                boolean useSchField2 = false;
                if ("1".equals(param._useAddrField2) && (getMS932ByteLength(schaddr1) + getMS932ByteLength(schaddr2) > 60)) {
                    useSchField2 = true;
                    schaddr = KNJ_EditEdit.get_token(schaddr1 + schaddr2, 40, 2);
                    if (null == schaddr) {
                        schaddr = new String[] {"", ""};
                    }
                } else if (getMS932ByteLength(schaddr1) > 30 || getMS932ByteLength(schaddr2) > 30) {
                    schaddr = KNJ_EditEdit.get_token(schaddr1 + schaddr2, 30, 2);
                    if (null == schaddr) {
                        schaddr = new String[] {"", ""};
                    }
                } else {
                    schaddr = new String[] {schaddr1, schaddr2};
                }
                if (useSchField2) {
                    for (int i = 0; i < schaddr.length; i++) {
                        svf.VrsOut("ADDRESS2_" + String.valueOf(i + 1) + "_2", schaddr[i]);
                    }
                } else {
                    for (int i = 0; i < schaddr.length; i++) {
                        svf.VrsOut("ADDRESS2_" + String.valueOf(i + 1), schaddr[i]);
                    }
                }
                svf.VrsOut("ADD_TITLE", param._remark6);    // 「発行者所在地」
                svf.VrsOut("SCHOOL_TITLE", param._remark7);    // 「学校名」
				svf.VrsOut("FIELD5", param._schoolname);    // 学校名

				svf.VrEndPage();
				nonedata = true;
			}
		} catch (Exception ex) {
			log.error("setSvfout set error!");
		} finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
		}
	}

	/**生徒情報**/
	private String preStat1(final Param param)
	{
		final StringBuffer stb = new StringBuffer();
        stb.append("SELECT T1.SCHREGNO, ");
        stb.append("       value(T2.INOUTCD,'') as INOUTCD, ");
        stb.append("       T2.ENT_SCHOOL, ");
        stb.append("       T2.NAME, ");
        stb.append("       T2.REAL_NAME, ");
        stb.append("       (CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
        stb.append("       VALUE(T7.SEND_ADDR1, '') AS ADDR1, ");
        stb.append("       VALUE(T7.SEND_ADDR2, '') AS ADDR2, ");
//        stb.append("       VALUE(T7.SEND_ADDR1, '') || VALUE(T7.SEND_ADDR2, '') AS ADDR, ");
        stb.append("       T2.BIRTHDAY ");
        stb.append("FROM   SCHREG_REGD_DAT T1 ");
        stb.append("       INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("       LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO ");
        stb.append("            AND T6.DIV = '01' ");
        stb.append("       LEFT JOIN SCHREG_SEND_ADDRESS_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO ");
        stb.append("            AND T7.DIV = '1' ");
        stb.append("WHERE  T1.YEAR = '" + param._year + "' AND ");
        stb.append("       T1.SEMESTER = '" + param._gakki + "' AND ");
        stb.append("       T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._schregnos) + " ");
        stb.append("ORDER BY ");
        stb.append("       T1.SCHREGNO ");
		return stb.toString();
	}
	
    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        Param param = new Param(db2, request);
        return param;
    }

	private static class Param {
        private final String _year;
	    private final String _gakki;
        private final String _gradeHrclass;
	    private final String[] _schregnos;
	    private final String _termSdate;
        private final String _termEdate;

        private String _jobname;
	    private String _staffname;
	    private String _schoolname;
//	    private String _remark1;
	    private String _remark4;
        private String _remark5;
        private String _remark6;
        private String _remark7;
//        private boolean _hasCertifSchoolDatRecord = false;

//      private final String _documentRoot;
//      private String _imagepath;
//      private String _extension;
//      private String _schoolStampExtension;
        private final String _useAddrField2;
        
        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");                        //年度
            _gakki = request.getParameter("GAKKI");                      //学期
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");      //学年＋組
            _termSdate = request.getParameter("TERM_SDATE").replace('/','-');  // 発行日
            _termEdate = request.getParameter("TERM_EDATE").replace('/','-');  // 有効期限
            
            // 学籍番号の指定
            _schregnos = request.getParameterValues("category_selected"); //学籍番号
            loadCertifSchoolDat(db2);
            
//            if (!_hasCertifSchoolDatRecord) {
//                //SVF出力
//                setStaffJobName(db2);                     //職名・職員名取得メソッド
//                setSchoolName(db2);                       //学校名取得メソッド
//            }
            
//            _documentRoot = request.getParameter("DOCUMENTROOT");                // '/usr/local/deve_oomiya/src'
//            //  写真データ
//            KNJ_Get_Info getinfo = new KNJ_Get_Info();
//            KNJ_Get_Info.ReturnVal returnval = null;
//            try {
//                returnval = getinfo.Control(db2);
//                _imagepath = returnval.val4;      //格納フォルダ
//                _extension = returnval.val5;      //拡張子
//            } catch( Exception e ){
//                log.error("setHeader set error!");
//            } finally {
//                getinfo = null;
//                returnval = null;
//            }
//            _schoolStampExtension = ".bmp";
            _useAddrField2 = request.getParameter("useAddrField2");
        }
        
        public void loadCertifSchoolDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            
//            String certifKindcd = null;
//            try {
//                final String sql = "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _gradeHrclass.substring(0, 2) + "' ";
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    if ("J".equals(rs.getString("SCHOOL_KIND"))) {
//                        certifKindcd = "102";
//                    } else if ("H".equals(rs.getString("SCHOOL_KIND"))) {
//                        certifKindcd = "101";
//                    }
//                }
//            } catch (Exception e) {
//                log.error("setHeader name_mst error!", e);
//            } finally {
//                db2.commit();
//                DbUtils.closeQuietly(null, ps, rs);
//            }
            String certifKindcd = "101";
            if (null != certifKindcd) {
                try {
                    final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + certifKindcd + "'";
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        _jobname =  rs.getString("JOB_NAME");
                        _staffname = rs.getString("PRINCIPAL_NAME");
                        _schoolname = rs.getString("SCHOOL_NAME");
//                        _remark1 = rs.getString("REMARK1");
                        _remark4 = StringUtils.defaultString(rs.getString("REMARK4"));
                        _remark5 = StringUtils.defaultString(rs.getString("REMARK5"));
                        _remark6 = StringUtils.defaultString(rs.getString("REMARK6"));
                        _remark7 = StringUtils.defaultString(rs.getString("REMARK7"));
//                        _hasCertifSchoolDatRecord = true;
                    }
                } catch (Exception e) {
                    log.error("setHeader name_mst error!", e);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
            }
        }

//        /** 職名・職員名を取得 **/
//        private void setStaffJobName(final DB2UDB db2) {
//            
//            final StringBuffer stb = new StringBuffer();
//            stb.append("SELECT T1.STAFFNAME, L1.JOBNAME ");
//            stb.append("FROM   V_STAFF_MST T1 ");
//            stb.append("LEFT JOIN JOB_MST L1 ON L1.JOBCD = T1.JOBCD ");
//            stb.append("WHERE  YEAR = '" + _year + "' AND JOBCD = '0001' "); //学校長
//
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                ps = db2.prepareStatement(stb.toString());
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    _jobname  = rs.getString("JOBNAME");    //職名
//                    _staffname = rs.getString("STAFFNAME");  //職員名
//                }
//            } catch (Exception ex) {
//                log.error("setStaffJobName set error!", ex);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }

//        /** 学校名を取得 **/
//        private void setSchoolName(final DB2UDB db2) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                ps = db2.prepareStatement(" SELECT SCHOOLNAME1, SCHOOLADDR1 FROM   SCHOOL_MST WHERE  YEAR = '" + _year + "' ");
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    _schoolname = rs.getString("SCHOOLNAME1");    //学校名
//                    _remark1 = rs.getString("SCHOOLADDR1");    //学校住所
//                }
//            } catch (Exception ex) {
//                log.error("setSchoolName set error!");
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }
	}

}//クラスの括り
