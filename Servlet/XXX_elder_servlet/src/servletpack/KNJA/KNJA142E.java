// kanji=漢字
/*
 * $Id: 21b19a4ae8cb86893761592da8c8cb02a3dbcb06 $
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;

public class KNJA142E {

    private static final Log log = LogFactory.getLog(KNJA142E.class);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
        log.fatal("$Revision: 71967 $ $Date: 2020-01-24 18:21:00 +0900 (金, 24 1 2020) $"); // CVSキーワードの取り扱いに注意

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
		    if ("1".equals(param._output)) {
		        ps1 = db2.prepareStatement(preStat1(param));		//生徒情報
		    }

			if (!param._hasCertifSchoolDatRecord) {
	            ps2 = db2.prepareStatement(preStat2(param));        //職名・職員名
	            ps3 = db2.prepareStatement(preStat3(param));        //学校名・学校住所
			}
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!", ex);
		}

		if("1".equals(param._output)) {
			//表面印刷
	        nonedata = setSvfout(db2,svf,param,ps1);	    //帳票出力のメソッド
		}else {
			//裏面印刷
	        nonedata = setSvfout2(db2,svf,param);	    //帳票出力のメソッド
		}


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
	        param._imagepath = returnval.val4;      //格納フォルダ
	        param._extension = returnval.val5;      //拡張子
		} catch( Exception e ){
			log.error("setHeader set error!");
		}

    //  名称マスタ（学校区分）
        param._schoolNameZ010 = "";
        try {
            param._schoolstampExtension = ".bmp";
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00'";
            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            while (rs.next()) {
                param._schoolNameZ010 = rs.getString("NAME1");
                if ("jisyukan".equals(param._schoolNameZ010) || "tokyoto".equals(param._schoolNameZ010)) {
                    param._schoolstampExtension = ".jpg";
                }
            }
            rs.close();
        } catch( Exception e ){
            log.error("setHeader name_mst error!", e);
        } finally {
            db2.commit();
        }

        String frmName = "1".equals(param._output) ? "KNJA142E_1.frm" : "KNJA142E_2.frm";
        if("1".equals(param._output) && "mieken".equals(param._schoolNameZ010)) frmName = "KNJA142E_1_2.frm";
        if("2".equals(param._output) && "mieken".equals(param._schoolNameZ010)) frmName = "KNJA142E_2_2.frm";
        svf.VrSetForm(frmName, 1);
		getinfo = null;
		returnval = null;
	}

	/**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
    	return KNJ_EditEdit.getMS932ByteLength(str);
    }

	/**帳票出力**/
	private boolean setSvfout(
		DB2UDB db2,
		Vrw32alp svf,
		Param param,
		PreparedStatement ps1
	) {
		boolean nonedata = false;
		final int maxRowCnt = 2;
		final int maxCnt = 5;
		int cnt = 1;
		int rowCnt = 1;
		try {
			ResultSet rs = ps1.executeQuery();

            //画像--------------
            String photo = "";                      //顔写真
            String photo_check = "";
            String stamp = "SCHOOLSTAMP" + param._schoolstampExtension;  //学校印
            String stamp_check = param._documentRoot + "/" + param._imagepath + "/" + stamp;
            log.info(" stamp = " + stamp_check);

            while( rs.next() ){
                if(cnt>maxCnt) {
                    svf.VrEndPage();
                    cnt = 1;
                    rowCnt = 1;
                }

                final String row = rowCnt == 1 ? "": "_" + String.valueOf(rowCnt);

                final String title = "sagaken".equals(param._schoolNameZ010) ? "身分証明書" : param._title;
                String sentence = "sagaken".equals(param._schoolNameZ010) ? "佐賀北高校通信制" : param._sentence;
                if(!"".equals(sentence)) sentence = sentence + "の生徒";

                //ヘッダー
                svf.VrsOutn("TITLE" + row, cnt, title); //タイトル
                svf.VrsOutn("SENTENCE" + row, cnt, sentence); //証明文言


                //画像--------------
                if("on".equals(param._printImage)) {
                    //顔写真
                    photo = "P" + rs.getString("SCHREGNO") + "." + param._extension.toLowerCase();
                    photo_check = param._documentRoot + "/" + param._imagepath + "/" + photo;
                    File f1 = new File(photo_check); //写真データ存在チェック用
                    if (f1.exists()) {
                        svf.VrsOutn("PHOTO_BMP" + row, cnt, photo_check );//顔写真
                    }else {
                        photo = "P" + rs.getString("SCHREGNO") + "." + param._extension.toUpperCase();
                        photo_check = param._documentRoot + "/" + param._imagepath + "/" + photo;
                        File f2 = new File(photo_check); //写真データ存在チェック用
                        if (f2.exists()) {
                            svf.VrsOutn("PHOTO_BMP" + row, cnt, photo_check );//顔写真
                        }
                    }
                }
                //学校印
                File f2 = new File(stamp_check); //学校長印データ存在チェック用
                if (f2.exists()) {
                    svf.VrsOutn("STAMP_BMP" + row, cnt, stamp_check );//学校印
                }

                //生徒情報--------------
                String coursename = "sagaken".equals(param._schoolNameZ010) ? StringUtils.defaultString(rs.getString("COURSECODENAME")) : StringUtils.defaultString(rs.getString("COURSENAME")) + "課程";
                svf.VrsOutn("COURSE_NAME" + row, cnt, coursename ); //課程名
                svf.VrsOutn("HR_NAME" + row, cnt, rs.getString("HR_NAME") ); //クラス名
                svf.VrsOutn("SCHREGNO" + row, cnt, rs.getString("SCHREGNO") ); //学籍番号
                svf.VrsOutn("NAME" + row, cnt, rs.getString("NAME")); //氏名
                svf.VrsOutn("BARCODE" + row, cnt, rs.getString("SCHREGNO") ); //バーコード

                String birth = KNJ_EditDate.h_format_JP(db2, rs.getString("BIRTHDAY"));
                String birthday[] = KNJ_EditDate.tate_format(birth);
                svf.VrsOutn("BIRTHDAY1" + row, cnt, birthday[0]); //生年月日 元号
                svf.VrsOutn("BIRTHDAY2" + row, cnt, birthday[1]); //生年月日 年
                svf.VrsOutn("BIRTHDAY3" + row, cnt, birthday[2]); //生年月日 月
                svf.VrsOutn("BIRTHDAY4" + row, cnt, birthday[3]); //生年月日 日

                svf.VrsOutn("SEX" + row, cnt, rs.getString("SEX")); //性別
                if("mieken".equals(param._schoolNameZ010)) {

                    final String addr1 = StringUtils.defaultString(rs.getString("ADDR1")); //住所1
                    final String addr2 = StringUtils.defaultString(rs.getString("ADDR2")); //住所2
                    final int addrSize = getMS932ByteLength(addr1) > getMS932ByteLength(addr2) ? getMS932ByteLength(addr1) : getMS932ByteLength(addr2);

                    //住所1
                    final String addr1Field = addrSize > 50 ? "7" : addrSize > 40 ? "5" : addrSize > 30 ? "3" : "1" ;
                    svf.VrsOutn("ADDRESS" + addr1Field + row, cnt, addr1);
                    //住所2
                    final String addr2Field = addrSize > 50 ? "8" : addrSize > 40 ? "6" : addrSize > 30 ? "4" : "2" ;
                    svf.VrsOutn("ADDRESS" + addr2Field + row, cnt, addr2);

                } else {
                	//住所
                    final String addr1 = StringUtils.defaultString(rs.getString("ADDR1"));
                    final String addr2 = StringUtils.defaultString(rs.getString("ADDR2"));
                    final int addrSize = getMS932ByteLength(addr1) > getMS932ByteLength(addr2) ? getMS932ByteLength(addr1) : getMS932ByteLength(addr2);
                    final String addrField = addrSize > 50 ? "5" : addrSize > 30 ? "3" : "1" ;
                    svf.VrsOutn("ADDRESS" + addrField + row, cnt, addr1 + addr2);
                }

                if(!"".equals(param._sdate)) {
                    final String sdateRow = rowCnt == 1 ? "1": String.valueOf(rowCnt);
                    svf.VrsOutn("SDATE" + sdateRow, cnt, KNJ_EditDate.h_format_JP(db2,param._sdate) + "　発行"); //発行日付
                }
                svf.VrsOutn("EDATE1" + row, cnt, KNJ_EditDate.h_format_JP(db2,param._edate) + "まで有効"); //有効期限


                //発行者情報--------------
                svf.VrsOutn("SCHOOLADDRESS1" + row, cnt, param._schoolAddr1 + param._schoolAddr2 );  //学校住所
                svf.VrsOutn("SCHOOLNAME1" + row, cnt, param._schoolName );  //学校名
                svf.VrsOutn("JOBNAME" + row, cnt, param._jobName );         //職名
                svf.VrsOutn("STAFFNAME" + row, cnt, param._principalName ); //職員名

                cnt = rowCnt == maxRowCnt ? cnt + 1 : cnt;
                rowCnt = rowCnt == maxRowCnt ? 1 : rowCnt + 1;
                nonedata = true;
            }
            svf.VrEndPage();
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfout set error!", ex);
        }
        return nonedata;
    }

	/**帳票出力(裏面)**/
	private boolean setSvfout2(
		DB2UDB db2,
		Vrw32alp svf,
		Param param
	) {
		boolean nonedata = false;
		final int maxRowCnt = 2;
		final int maxCnt = 5;
		int cnt = 1;
		int rowCnt = 1;
		try {

            for (int i = 0; i < Integer.parseInt(param._maisuu); i++) {
                if(i != 0) svf.VrEndPage();
                cnt = 1;
                rowCnt = 1;

                while(cnt <= maxCnt) {

                    final String row = rowCnt == 1 ? "": "_" + String.valueOf(rowCnt);

                    svf.VrsOutn("EDATE1" + row, cnt, "※ 有効期限は" + KNJ_EditDate.h_format_JP(db2,param._edate) + "までとする。"); //有効期限

                    cnt = rowCnt == maxRowCnt ? cnt + 1 : cnt;
                    rowCnt = rowCnt == maxRowCnt ? 1 : rowCnt + 1;
                    nonedata = true;
                }
            }
            svf.VrEndPage();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfout set error!", ex);
        }
        return nonedata;
    }

    /**生徒情報**/
    private String preStat1(Param param)
    {
        StringBuffer stb = new StringBuffer();
        try {
            if("on".equals(param._sendAddr)) {
                stb.append(" WITH SEND_ADDRESS AS ( ");
                stb.append("   SELECT  ");
                stb.append("      T1.*  ");
                stb.append("   FROM  ");
                stb.append("      SCHREG_SEND_ADDRESS_DAT T1  ");
                stb.append("      INNER JOIN ( SELECT SCHREGNO, MAX(DIV) AS DIV FROM SCHREG_SEND_ADDRESS_DAT GROUP BY SCHREGNO ) T2 ");
                stb.append("              ON T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("             AND T2.DIV      = T1.DIV ");
                stb.append(" ) ");
            }
            //メイン
            stb.append("SELECT ");
            stb.append("       T1.GRADE, ");
            stb.append("       T1.HR_CLASS, ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T4.HR_NAMEABBV AS HR_NAME, ");
            stb.append("       COURSE.COURSENAME, ");
            stb.append("       T3.COURSECODENAME, ");
            stb.append("       T2.NAME, ");
            stb.append("       T2.BIRTHDAY, ");
            stb.append("       Z002.ABBV1 AS SEX, ");
            if("on".equals(param._sendAddr)) {
                stb.append("       SEND.SEND_ADDR1 AS ADDR1, ");
                stb.append("       SEND.SEND_ADDR2 AS ADDR2 ");
            } else {
                stb.append("       ADDR.ADDR1, ");
                stb.append("       ADDR.ADDR2 ");
            }
            stb.append("FROM   SCHREG_REGD_DAT T1 ");
            stb.append("       INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO =T1.SCHREGNO ");
            stb.append("       LEFT JOIN COURSECODE_MST T3 ON T3.COURSECODE = T1.COURSECODE ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT T4 ");
            stb.append("         ON T1.YEAR     = T4.YEAR ");
            stb.append("        AND T1.SEMESTER = T4.SEMESTER ");
            stb.append("        AND T1.GRADE    = T4.GRADE ");
            stb.append("        AND T1.HR_CLASS = T4.HR_CLASS ");
            stb.append("       LEFT JOIN NAME_MST Z002 ");
            stb.append("         ON T2.SEX       = Z002.NAMECD2 ");
            stb.append("        AND Z002.NAMECD1 = 'Z002' ");
            stb.append("       LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT GROUP BY SCHREGNO) ADDR_MAX ");
            stb.append("         ON ADDR_MAX.SCHREGNO = T1.SCHREGNO  ");
            stb.append("       LEFT JOIN SCHREG_ADDRESS_DAT ADDR ");
            stb.append("         ON ADDR.SCHREGNO  = ADDR_MAX.SCHREGNO ");
            stb.append("        AND ADDR.ISSUEDATE = ADDR_MAX.ISSUEDATE ");
            stb.append("       LEFT JOIN COURSE_MST COURSE ON COURSE.COURSECD = T1.COURSECD ");
            if("on".equals(param._sendAddr)) {
                stb.append("       LEFT JOIN SEND_ADDRESS SEND ON SEND.SCHREGNO = T1.SCHREGNO ");
            }
            stb.append("WHERE  T1.YEAR='"+param._year+"' AND ");
            stb.append("       T1.SEMESTER='"+param._gakki+"' AND ");
            if("1".equals(param._disp)) {
                //クラス指定
                stb.append("       T1.GRADE||T1.HR_CLASS IN "+param._categorySelected+" ");
            }else {
            	//個人指定
                stb.append("       T1.GRADE||T1.HR_CLASS||T1.SCHREGNO IN "+param._categorySelected+" ");
            }
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
			stb.append("WHERE  YEAR='"+param._year+"' AND JOBCD='0001' ");//学校長
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
			stb.append("WHERE  YEAR='"+param._year+"' ");
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
        private final String _year;
	    private final String _gakki;
        private final String _schoolKind;
	    private final String _disp;
	    private final String _output;
	    private final String _categorySelected;
	    private final String _sdate;
	    private final String _edate;
	    private final String _maisuu;
	    private String _jobName;
	    private final String _documentRoot;
	    private String _imagepath;
	    private String _extension;
	    private String _principalName;
	    private String _schoolName;
	    private String _schoolAddr1;
	    private String _schoolAddr2;
	    private String _schoolstampExtension;
	    private String _sentence;
	    private String _title;
	    private final String _printImage;
	    private final String _sendAddr;
        private boolean _hasCertifSchoolDatRecord = false;
        private String _schoolNameZ010;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");                       //年度
            _gakki = request.getParameter("GAKKI");                     //学期
            _schoolKind = request.getParameter("SCHOOL_KIND");          //校種
            _disp = request.getParameter("DISP");                       //1:クラス指定、2：個人指定
            _output = request.getParameter("OUTPUT");                   //1:表面,2:裏面
            if("mieken".equals(request.getParameter("SCHOOLNAME"))) {
                _sdate = request.getParameter("SDATE").replace('/','-');    //発行日付
            } else {
                _sdate = "";  //発行日付
            }
            _edate = request.getParameter("EDATE").replace('/','-');    //有効期限
            _maisuu = request.getParameter("MAISUU");                   //出力枚数
            _documentRoot = request.getParameter("DOCUMENTROOT");       // '/usr/local/deve_oomiya/src'
            _printImage = request.getParameter("PRINT_IMAGE");          //顔写真を表示
            _sendAddr = request.getParameter("SEND_ADDR");              //その他(送付先)
            // 学籍番号の指定
            String schno[] = request.getParameterValues("CATEGORY_SELECTED"); //学年+組 or 学年+組+学籍番号
            int i = 0;
            String str = "(";
            if ("1".equals(_output)) {
                while(i < schno.length){
                    if(schno[i] == null ) break;
                    if(i > 0) str = str + ",";
                    //str = str + "'" + schno[i] + "'";
                    str = str + "'" + schno[i] + "'";
                    i++;
                }
            }
            str = str + ")";
            _categorySelected = str;

            loadCertifSchoolDat(db2, "101");
        }

        public void loadCertifSchoolDat(final DB2UDB db2, final String certifKind) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            if (null != _schoolKind) {
                try {
                    final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + certifKind + "'";
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                    	_jobName =  rs.getString("JOB_NAME");
                    	_principalName = rs.getString("PRINCIPAL_NAME");
                    	_schoolName = rs.getString("SCHOOL_NAME");
                    	_schoolAddr1 = StringUtils.defaultString(rs.getString("REMARK4")); // 学校住所1
                    	_schoolAddr2 = StringUtils.defaultString(rs.getString("REMARK5")); // 学校住所2
                        _hasCertifSchoolDatRecord = true;
                        _sentence = StringUtils.defaultString(rs.getString("REMARK8")); // 学校名(証明文言)
                        _title = StringUtils.defaultString(rs.getString("REMARK9")); // 帳票タイトル

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
