// kanji=漢字
/*
 * $Id: 6d8fd040041372318e51127d6b2e20dca62514ff $
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
import java.text.SimpleDateFormat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *					＜ＫＮＪＡ１４３＞  身分証明書（東京都）
 *
 *	2005/03/25 nakamoto 作成日
 *	2005/03/31 nakamoto 生年月日・発行日を元号・年・月・日と分けてフィールドにセットするように変更 ---NO001
 *	2005/04/05 nakamoto 所属：所属名＋固定文字「課程」を追加 ---NO002
 *  2007/03/16 nakamoto NO003:内外区分(inoutcd)が（'9'の時、'9'以外の時）の「タイトル・所属・文」の表示を変更した。
 *  2007/03/30 nakamoto NO004:パラメータ"GRADE_HR_CLASS"は、使用しないよう変更した。（不具合例：クラスコードに漢字）
 *
 **/

public class KNJA143 {

    private static final Log log = LogFactory.getLog(KNJA143.class);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		Param param = null;

	//	print設定
		PrintWriter outstrm = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");

	//	svf設定
		svf.VrInit();						   		//クラスの初期化
		svf.VrSetSpoolFileStream(response.getOutputStream());  		//PDFファイル名の設定

	//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch( Exception ex ) {
			log.error("DB2 open error!", ex);
			return;
		}

	    //  パラメータの取得
        try {
            KNJServletUtils.debugParam(request, log);
            param = new Param(db2, request);
        } catch( Exception ex ) {
            log.error("parameter error!", ex);
        }


	//	ＳＶＦ作成処理
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		setHeader(db2,svf,param);
		//SQL作成
		try {
			ps1 = db2.prepareStatement(preStat1(param));		//生徒情報
			
			if (!param._hasCertifSchoolDatRecord) {
	            ps2 = db2.prepareStatement(preStat2(param));        //職名・職員名
	            ps3 = db2.prepareStatement(preStat3(param));        //学校名・学校住所
	            //SVF出力
	            setStaffJobName(db2, param, ps2);                     //職名・職員名取得メソッド
	            setSchoolName(db2, param, ps3);                       //学校名取得メソッド
			}
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!", ex);
		}
   		if( setSvfout(db2,svf,param,ps1) ) nonedata = true;	    //帳票出力のメソッド

log.debug("nonedata = "+nonedata);

	//	該当データ無し
		if( !nonedata ){
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "note");
			svf.VrEndPage();
		}

	// 	終了処理
		svf.VrQuit();
		preStatClose(ps1,ps2,ps3);	//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り



	/** 事前処理 **/
	private void setHeader(
		DB2UDB db2,
		Vrw32alp svf,
		Param param
	) {
		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;

	//	写真データ
		try {
			returnval = getinfo.Control(db2);
	        param._8 = returnval.val4;      //格納フォルダ
	        param._9 = returnval.val5;      //拡張子
		} catch( Exception e ){
			log.error("setHeader set error!");
		}

    //  名称マスタ（学校区分）
        param._schoolName = "";
        try {
            param._13 = ".bmp";
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00'";
            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            while (rs.next()) {
                param._schoolName = rs.getString("NAME1");
                if ("jisyukan".equals(param._schoolName) || "tokyoto".equals(param._schoolName)) {
                    param._13 = ".jpg";
                }
            }
            rs.close();
        } catch( Exception e ){
            log.error("setHeader name_mst error!", e);
        } finally {
            db2.commit();
        }

        if ("2".equals(param._3)) {
            svf.VrSetForm("KNJA143_1.frm", 4);//カード
        }
        else if ("1".equals(param._3) && "kumamoto".equals(param._schoolName)) {
            svf.VrSetForm("KNJA143_2KUMA.frm", 4);//Ａ４用紙(熊本)
        }
        else if ("1".equals(param._3)) {
            svf.VrSetForm("KNJA143_2.frm", 4);//Ａ４用紙
        }

		getinfo = null;
		returnval = null;
	}


	/**職名・職員名を取得**/
	private void setStaffJobName(
		DB2UDB db2,
		Param param,
		PreparedStatement ps2
	) {
		try {
			ResultSet rs = ps2.executeQuery();
			while( rs.next() ){
		        param._6  = rs.getString("JOBNAME");    //職名
		        param._10 = rs.getString("STAFFNAME");  //職員名
			}
			rs.close();
    		db2.commit();
		} catch( Exception ex ) {
			log.error("setStaffJobName set error!", ex);
		}
	}


	/**学校名を取得**/
	private void setSchoolName(
		DB2UDB db2,
		Param param,
		PreparedStatement ps3
	) {
		try {
			ResultSet rs = ps3.executeQuery();
			while( rs.next() ){
		        param._11 = rs.getString("SCHOOLNAME1");    //学校名
		        param._12 = rs.getString("SCHOOLADDR1");    //学校住所
			}
			rs.close();
    		db2.commit();
		} catch( Exception ex ) {
			log.error("setSchoolName set error!", ex);
		}
	}

	/**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

	/**帳票出力**/
	private boolean setSvfout(
		DB2UDB db2,
		Vrw32alp svf,
		Param param,
		PreparedStatement ps1
	) {
		boolean nonedata = false;
		try {
			ResultSet rs = ps1.executeQuery();

            //画像--------------
			String photo = "";                      //顔写真
			String stamp = "SCHOOLSTAMP" + param._13;  //学校印
			String photo_check = "";
			String stamp_check = param._7 + "/" + param._8 + "/" + stamp;
			log.info(" stamp = " + stamp_check);
		    File f2 = new File(stamp_check);        //学校長印データ存在チェック用

			while( rs.next() ){
                //画像--------------
                //顔写真
			    photo = "P" + rs.getString("SCHREGNO") + "." + param._9;
                photo_check = param._7 + "/" + param._8 + "/" + photo;
			    File f1 = new File(photo_check);//写真データ存在チェック用
		        if (f1.exists()) 
		            svf.VrsOut("PHOTO_BMP"    , photo_check );//顔写真
                //学校印
		        if (f2.exists()) {
		            if ("2".equals(param._3) || ("1".equals(param._3) && ("kumamoto".equals(param._schoolName) || "1".equals(param._printSchoolStamp)))) {
	                    svf.VrsOut("STAMP_BMP"    , stamp_check );//学校印
		            }
		        }
                //生徒情報--------------
                //---NO003
                if (rs.getString("INOUTCD").equals("9")) {
                    svf.VrsOut("COURSE"       ,"" );                              //所属
                    svf.VrsOut("ENT_SCHOOL"   ,rs.getString("ENT_SCHOOL") );      //所属
                    svf.VrsOut("TITLE"        ,"併修生証" );                      //タイトル
                    svf.VrsOut("SENTENCE"     ,"併修生" );                        //文
                } else {
                    String coursename = (rs.getString("COURSENAME") != null) ? rs.getString("COURSENAME")+"課程" : "";//---NO002
                    svf.VrsOut("COURSE"       ,coursename );                      //所属
                    svf.VrsOut("ENT_SCHOOL"   ,"" );                              //所属
                    svf.VrsOut("TITLE"        ,"生徒証" );                        //タイトル
                    svf.VrsOut("SENTENCE"     ,"生徒" );                          //文
                }
//---NO003		svf.VrsOut("TITLE"        ,"生徒証" );                            //タイトル
				svf.VrsOut("SCHREGNO"     ,rs.getString("SCHREGNO") );            //学籍番号
//---NO003      String coursename = (rs.getString("COURSENAME") != null) ? rs.getString("COURSENAME")+"課程" : "";//---NO002
//---NO003		svf.VrsOut("COURSE"       ,coursename );                          //所属
				final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                if (getMS932ByteLength(name) > 30) {
	                svf.VrsOut("NAME3"      ,name);                //氏名(漢字)
				} else if (getMS932ByteLength(name) > 20) {
                    svf.VrsOut("NAME2"      ,name);                //氏名(漢字)
				} else {
                    svf.VrsOut("NAME"       ,name);                //氏名(漢字)
				}
                svf.VrsOut("BARCODE"      ,rs.getString("SCHREGNO") );            //バーコード
            	/* 生年月日・発行日---NO001 */
                setDivisionDate(svf, param, rs);
   				//svf.VrsOut("BIRTHDAY"     ,KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY")) );//生年月日
        		//svf.VrsOut("SDATE"	    ,KNJ_EditDate.h_format_JP(param._5));   //発行日
                //発行者情報--------------
                if (null != param._12) {
                    final int addrSize = "1".equals(param._3) ? 30 : 26;
                    String field;
                    if ("1".equals(param._useAddrField2) && getMS932ByteLength(param._12) > 50) {
                        field = "1".equals(param._3) ? "SCHOOLADDRESS1_4" : "SCHOOLADDRESS4";
                    } else if ("1".equals(param._useAddrField2) && getMS932ByteLength(param._12) > 40) {
                        field = "1".equals(param._3) ? "SCHOOLADDRESS1_3" : "SCHOOLADDRESS3";
                    } else if ("1".equals(param._useAddrField2) && getMS932ByteLength(param._12) > addrSize) {
                        field = "1".equals(param._3) ? "SCHOOLADDRESS1_2" : "SCHOOLADDRESS2";
                    } else {
                        field = "SCHOOLADDRESS1";
                    }
                    svf.VrsOut(field   ,param._12 );                       //学校住所
                }
				svf.VrsOut("SCHOOLNAME1"  ,param._11 );                           //学校名
				svf.VrsOut("JOBNAME"      ,param._6 );                            //職名
				svf.VrsOut("STAFFNAME"    ,param._10 );                           //職員名

				svf.VrEndRecord();//１行出力
				nonedata = true;
			}
			rs.close();
    		db2.commit();
		} catch( Exception ex ) {
			log.error("setSvfout set error!", ex);
		}
		return nonedata;
	}


	/** 生年月日・発行日を元号・年・月・日と分ける---NO001 */
	private void setDivisionDate(
		Vrw32alp svf,
		Param param,
		ResultSet rs
	) {
		try {
           	//発行日
            if (param._5 != null) {
    			String sdate = KNJ_EditDate.h_format_JP(param._5);
    			String arr_sdate[] = KNJ_EditDate.tate_format(sdate);
                for (int i = 1; i < 5; i++) 
               		svf.VrsOut("SDATE"+String.valueOf(i)	,arr_sdate[i-1] );
            }
           	//生年月日
            if (rs.getString("BIRTHDAY") != null) {
    			String birth = KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY"));
//log.debug("birth = "+birth);
    			String arr_birth[] = KNJ_EditDate.tate_format(birth);
                for (int i = 1; i < 5; i++) {
                    if (arr_birth[1] == null) arr_birth[1] = (arr_birth[0]).substring(2);
               		svf.VrsOut("BIRTHDAY"+String.valueOf(i)	,arr_birth[i-1] );
//log.debug("arr_birth = "+arr_birth[i-1]);
                }
            }
		} catch( Exception ex ) {
			log.error("setDivisionDate error!", ex);
		}
	}



	/**生徒情報**/
	private String preStat1(Param param)
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（学籍番号）
		try {
            //メイン
			stb.append("SELECT T1.SCHREGNO, ");
			stb.append("       T5.COURSENAME, ");
			stb.append("       value(T2.INOUTCD,'') as INOUTCD, ");//---NO003
			stb.append("       T2.ENT_SCHOOL, ");//---NO003
			stb.append("       T2.NAME, ");
            stb.append("       T2.REAL_NAME, ");
            stb.append("       (CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
			stb.append("       T2.BIRTHDAY ");
   			stb.append("FROM   SCHREG_REGD_DAT T1 ");
   			stb.append("       INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ");
   			stb.append("       LEFT JOIN COURSE_MST T5 ON T5.COURSECD=T1.COURSECD ");
            stb.append("       LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO=T1.SCHREGNO ");
            stb.append("            AND T6.DIV='01' ");
			stb.append("WHERE  T1.YEAR='"+param._0+"' AND ");
			stb.append("       T1.SEMESTER='"+param._1+"' AND ");
//			stb.append("       T1.GRADE||T1.HR_CLASS = '"+param._2+"' AND ");
			stb.append("       T1.SCHREGNO IN "+param._4+" ");
			stb.append("ORDER BY ");
            stb.append("       T1.GRADE, ");
            stb.append("       T1.HR_CLASS, ");
            stb.append("       T1.ATTENDNO ");
		} catch( Exception e ){
			log.error("preStat1 error!", e);
		}
		return stb.toString();

	}//preStat1()の括り


	/**職名・職員名**/
	private String preStat2(Param param)
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT STAFFNAME, ");
			stb.append("       (SELECT JOBNAME FROM JOB_MST T2 WHERE T2.JOBCD=T1.JOBCD) AS JOBNAME ");
			stb.append("FROM   V_STAFF_MST T1 ");
			stb.append("WHERE  YEAR='"+param._0+"' AND JOBCD='0001' ");//学校長
		} catch( Exception e ){
			log.error("preStat2 error!");
		}
		return stb.toString();

	}//preStat2()の括り


	/**学校名**/
	private String preStat3(Param param)
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT SCHOOLNAME1,SCHOOLADDR1 ");
   			stb.append("FROM   SCHOOL_MST ");
			stb.append("WHERE  YEAR='"+param._0+"' ");
		} catch( Exception e ){
			log.error("preStat3 error!", e);
		}
		return stb.toString();

	}//preStat3()の括り


	/**PrepareStatement close**/
	private void preStatClose(
		PreparedStatement ps1,
		PreparedStatement ps2,
		PreparedStatement ps3
	) {
	    DbUtils.closeQuietly(ps1);
        DbUtils.closeQuietly(ps2);
        DbUtils.closeQuietly(ps3);
	}//preStatClose()の括り


	private static class Param {
        private final String _0;
	    private final String _1;
        private final String _2;
	    private final String _3;
	    private final String _4;
	    private final String _5;
	    private String _6;
	    private final String _7;
	    private String _8;
	    private String _9;
	    private String _10;
	    private String _11;
	    private String _12;
	    private String _13;
	    private final String _printSchoolStamp;
        private boolean _hasCertifSchoolDatRecord = false;
        private final String _useAddrField2;
        private String _schoolName;
        
        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _0 = request.getParameter("YEAR");                        //年度
            _1 = request.getParameter("GAKKI");                       //学期
            _2 = request.getParameter("GRADE_HR_CLASS");              //学年＋組
            _3 = request.getParameter("OUTPUT");                      //フォーム種別(1:Ａ４用紙,2:カード)
            String sdate = request.getParameter("TERM_SDATE");              //発行日
            _5 = sdate.replace('/','-');
            _7 = request.getParameter("DOCUMENTROOT");                // '/usr/local/deve_oomiya/src'
            _printSchoolStamp = request.getParameter("PRINT_SCHOOL_STAMP");
            // 学籍番号の指定
            String schno[] = request.getParameterValues("category_selected");//学籍番号
            int i = 0;
            String str = "(";
            while(i < schno.length){
                if(schno[i] == null ) break;
                if(i > 0) str = str + ",";
                str = str + "'" + schno[i] + "'";
                i++;
            }
            str = str + ")";
            _4 = str;
            
            loadCertifSchoolDat(db2);
            _useAddrField2 = request.getParameter("useAddrField2");
        }
        
        public void loadCertifSchoolDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            
            String certifKindcd = null;
            try {
                final String sql = "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _0 + "' AND GRADE = '" + _2.substring(0, 2) + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if ("J".equals(rs.getString("SCHOOL_KIND"))) {
                        certifKindcd = "102";
                    } else if ("H".equals(rs.getString("SCHOOL_KIND"))) {
                        certifKindcd = "101";
                    }
                }
            } catch( Exception e ){
                log.error("setHeader name_mst error!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            if (null != certifKindcd) {
                try {
                    final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _0 + "' AND CERTIF_KINDCD = '" + certifKindcd + "'";
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        _6 =  rs.getString("JOB_NAME");
                        _10 = rs.getString("PRINCIPAL_NAME");
                        _11 = rs.getString("SCHOOL_NAME");
                        _12 = rs.getString("REMARK1"); // 学校住所
                        _hasCertifSchoolDatRecord = true;
                    }
                } catch( Exception e ){
                    log.error("setHeader name_mst error!", e);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
            }
        }
	}

}//クラスの括り
