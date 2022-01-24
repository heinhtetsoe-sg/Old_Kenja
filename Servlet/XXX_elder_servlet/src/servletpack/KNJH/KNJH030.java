// kanji=漢字
/*
 * $Id: 27f7fe16944c6813541e3552731ae8712d0583a9 $
 *
 * Copyright(C) 2007-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJH;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Schoolinfo;
import servletpack.KNJZ.detail.KNJ_Semester;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [指導情報管理]
 *
 *					＜ＫＮＪＨ０３０＞  生徒環境調査票（高校用）
 *
 *		＊高校用と中学校用との相違個所	param[17]〜[20]	
 *
 * 2004/02/13 nakamoto パソコンの時間
 * 2005/10/13 m-yama   NO002 SCHREG_ADDRESS_DAT,SCHREG_BASE_MST,SCHREG_ENVIR_DATの変更に伴う修正 
 * 2006/06/20 m-yama   NO003 SCHREG_ENVIR_DATの変更に伴う修正
 * 2006/06/26 m-yama   NO004 SCHREG_ENVIR_DATの変更に伴う修正(2枚目の出力を現在年月のみ出力)
 * 2006/07/04 m-yama   NO005 出身学校の固定文字を中高で切替える。
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJH030 extends HttpServlet {
    private Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
	private DB2UDB    db2;					// Databaseクラスを継承したクラス
	boolean nonedata; 			// 該当データなしフラグ

    private static final Log log = LogFactory.getLog(KNJH030.class);
    
    private String _useAddrField2;
    private String _isPrintSchregno;

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

					/*
							DOCUMENTROOT 		/usr/local/development/src

						[2]作成日 [3][4]学期範囲 [5]処理学籍番号 [6]〜[13][15]SVF-formへ受渡し用 [14]写真格納場所
						[16]学年
					*/

	// パラメータの取得
	    String param[] = new String[24];
		String schno[] = request.getParameterValues("category_selected"); // 学籍番号
		try {
			param[14] = request.getParameter("DOCUMENTROOT");      		// 写真格納場所パス
            param[23] = request.getParameter("GRADE_HR_CLASS");
            param[23] = param[23].substring(0,2);                       // 学年
            param[22] = request.getParameter("OUTPUT");                 // フォーム選択 1:Ａ４縦,2:Ａ４横
	        param[0] = request.getParameter("YEAR");         			// 年度
	        param[1] = request.getParameter("SEMESTER");         		// 学期
			StringBuffer stbx = new StringBuffer();
			stbx.append(request.getParameter("DATE"));         			// 調査日付
			stbx.replace(4,5,"-");
			stbx.replace(7,8,"-");
	        param[2] = stbx.toString();
            _useAddrField2 = request.getParameter("useAddrField2");
            _isPrintSchregno = request.getParameter("PRINT_SCHREGNO");
		} catch( Exception ex ) {
            log.error("[KNJH030H]parameter error!");
            log.error(ex);
		}


	// print設定
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

	// svf設定
		svf.VrInit();							//クラスの初期化
		svf.VrSetSpoolFileStream(outstrm);   	//PDFファイル名の設定

	// ＤＢ接続
		db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
		try {
			db2.open();
		} catch( Exception ex ) {
            log.warn("[KNJH030H]DB2 open error!");
            return;
		}

	// ＤＢ検索（コントロールマスター）
		/* 写真データ格納フォルダの取得 */
		try {
			KNJ_Control control = new KNJ_Control();							//クラスのインスタンス作成
			KNJ_Control.ReturnVal returnval = control.Control(db2);
			param[14] = param[14] + "/" + returnval.val4;						//写真データ格納フォルダのパス
			param[21] = returnval.val5;											//写真データの拡張子
		} catch( Exception e ){
            log.error("[KNJH030H]ctrl_date get error!", e);
		}

		/* 学期範囲日付の取得 */
		try {
			KNJ_Semester semester = new KNJ_Semester();						//クラスのインスタンス作成
			KNJ_Semester.ReturnVal returnval = semester.Semester(db2,param[0],param[1]);
			param[3] = returnval.val2;											//学期開始日
			param[4] = returnval.val3;											//学期終了日
		} catch (Exception e) {
            log.error("[KNJH030H]semester date error!", e);
		}

	//	学校名・学校住所・校長名の取得
		try {
			KNJ_Schoolinfo schoolinfo = new KNJ_Schoolinfo(param[0]);	//取得クラスのインスタンス作成
			KNJ_Schoolinfo.ReturnVal returnval = schoolinfo.get_info(db2);
			param[16] = returnval.SCHOOL_NAME1;											//学校名１

		} catch (Exception e) {
            log.error("[KNJH030H]schoolinfo error!", e);
		}

        log.fatal("$Revision: 74201 $");
		for(int ia=0 ; ia<param.length ; ia++) log.debug("[KNJH030H]param[" + ia + "]=" + param[ia]);

    //中高判定 NO005
        try {
            if (getJorH(param)) {
                param[17] = "小学校";
            } else {
                param[17] = "中学校";
            }
        } catch (Exception e) {
            log.error("[KNJH030H]schoolinfo error!", e);
        }
	/*-----------------------------------------------------------------------------
	    ＳＶＦ作成処理       
	  -----------------------------------------------------------------------------*/
		nonedata = false; 		// 該当データなしフラグ(MES001.frm出力用)

		for(int ia=0 ; ia<schno.length ; ia++){
			if(schno[ia] == null)	break;
			param[5] = schno[ia];	//学籍番号
            if (param[22] != null && param[22].equals("1")) {
                printSvfTate(param); //Ａ４縦出力
            } else {
                set_detail1(param); //表出力
                set_detail2(param); //裏出力
            }
		}

	// ＳＶＦフォーム出力
		/*該当データ無し*/
		if(nonedata == false){
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "note");
			svf.VrEndRecord();
			svf.VrEndPage();
		}

	// 終了処理
		db2.close();		// DBを閉じる
		svf.VrQuit();
		outstrm.close();	// ストリームを閉じる 

	}	//doGetの括り

	private int getMS932ByteLength(final String str) {
        int len = 0;
        if (null != str) {
            try {
                len = str.getBytes("MS932").length;
            } catch (final Exception e) {
                log.debug("exception!", e);
            }
        }
        return len;
    }

	/**
     * 中高判定
     */
    public boolean getJorH(String param[]){
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean jhflg = false;
        try {
            String jhsql = "select schoolname1 from school_mst where year = '" + param[0] + "' and schoolname1 like '%中学%' ";
            ps = db2.prepareStatement(jhsql);
            rs = ps.executeQuery();
            while ( rs.next() ){
                jhflg = true;
            }

        } catch(Exception e) {
            log.error("[KNJH030]getJorH error!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return jhflg;
    }

    /** 
     *  Ａ４縦出力処理 
     */
    private void printSvfTate(
            String param[]
    ) {
        setHead(param); //SVF-FORMセット
        for (int hokan = 0; hokan < 2; hokan++) {
            printSvfSchregRegd(param, hokan); //生徒証番号情報
        }
    }

    /** 
     *  SVF-FORMセット
     */
    private void setHead(
            String param[]
    ) {
        svf.VrSetForm("KNJH030_3.frm", 1);
    }

    /** 
     *  学校情報＆見出し項目出力処理 
     */
    private void printSvfSchool(
            String param[],
            int hokan
    ) {
        //定義
        PreparedStatement ps = null;
        ResultSet rs = null;

        //RecordSet作成
        try {
            svf.VrsOut("KEEPING" , (0 < hokan) ? "担任保管用" : "学校保管用" );
            svf.VrsOut("INPUT_DAY"    , "　　年　月　日" );
            if (param[2] != null) {
                svf.VrsOut("INPUT_DAY" , param[2].substring(0,4) + "年"
                                             + String.valueOf(Integer.parseInt(param[2].substring(5,7))) + "月"
                                             + String.valueOf(Integer.parseInt(param[2].substring(8,10))) + "日" );
            }

            String sql = "SELECT SCHOOLNAME2 FROM SCHOOL_MST WHERE YEAR='" + param[0] + "'";
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while( rs.next() ){
                svf.VrsOut("SCHOOL_NAME"    , rs.getString("SCHOOLNAME2") );
            }
        } catch (Exception ex) {
            log.error("printSvfSchool read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /** 
     *  顔写真情報出力処理 
     */
    private void printSvfSchregPhoto(
            String param[],
            int photo_gyo
    ) {
        //RecordSet作成
        try {
            for (int i = 1; i < 4; i++) {
                svf.VrsOut("PHOTO" + String.valueOf(i) + "_1",  "上半身・正面・脱帽" );
                svf.VrsOut("PHOTO" + String.valueOf(i) + "_2",  "のもの3cm×4cm大" );
                svf.VrsOut("PHOTO" + String.valueOf(i) + "_3",  "の写真を（名刺版" );
                svf.VrsOut("PHOTO" + String.valueOf(i) + "_4",  "はこの大きさに切" );
                svf.VrsOut("PHOTO" + String.valueOf(i) + "_5",  "って）枠内にのり" );
                svf.VrsOut("PHOTO" + String.valueOf(i) + "_6",  "づけして下さい。" );
            }

            if (photo_gyo > 0) {
                String photo_check = param[14] + "/P" + param[5] + "." + param[21];
                File f1 = new File(photo_check);
                if (f1.exists()) {
                    svf.VrsOut("BITMAP" + String.valueOf(photo_gyo)    , photo_check );
                    for (int i = 1; i < 7; i++) {
                        svf.VrsOut("PHOTO" + String.valueOf(photo_gyo) + "_" + String.valueOf(i),  "" );
                    }
                }
            }
        } catch( Exception ex ) { log.error("printSvfSchregPhoto read error! ", ex);  }
    }

    /** 
     *  生徒証番号情報出力処理 
     */
    private void printSvfSchregRegd(
            String param[],
            int hokan
    ) {
        //定義
        PreparedStatement ps = null;
        ResultSet rs = null;

        //RecordSet作成
        try {
            String sql = sqlSchregRegd(param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            boolean nonedata2 = false;
            int photo_gyo = 0;
            while( rs.next() ){
                int gyo = rs.getInt("GYO");
                if (nonedata2 == true && gyo == 1) {
                    printSvfSchool(param, hokan); //学校情報＆見出し項目
                    printSvfSchregPhoto(param, photo_gyo); //顔写真情報
                    printSvfSchregRela(param); //家族関係情報
                    printSvfGuardian(param); //保護者情報
                    printSvfSchregAddress(param); //生徒情報（住所）
                    printSvfSchregBase(param); //生徒情報（氏名）
                    svf.VrEndPage();
                    nonedata2 = false;
                }

                svf.VrsOutn("GRADE1",     gyo,  rs.getString("GRADE_NAME1"));
                svf.VrsOutn("GRADE2",     gyo,  rs.getString("GRADE_NAME1"));
                final String hrName;
                if (null != rs.getString("HR_CLASS_NAME1")) {
                    hrName = rs.getString("HR_CLASS_NAME1") + "組";
                } else if (StringUtils.isNumeric(rs.getString("HR_CLASS"))) {
                    hrName = String.valueOf(Integer.parseInt(rs.getString("HR_CLASS"))) + "組";
                } else {
                    hrName = rs.getString("HR_CLASS") + "組";
                }
                svf.VrsOutn("HR_NAME", gyo, hrName + String.valueOf(rs.getInt("ATTENDNO")));
                if (null != _isPrintSchregno) {
                    svf.VrsOutn("SCHREG_NO", gyo, rs.getString("SCHREGNO"));
                }

                if (param[23].equals(rs.getString("GRADE"))) {
                    photo_gyo = gyo;
                }

                nonedata2 = true;
            }
            //ページ出力
            if (nonedata2 == true) {
                printSvfSchool(param, hokan); //学校情報＆見出し項目
                printSvfSchregPhoto(param, photo_gyo); //顔写真情報
                printSvfSchregRela(param); //家族関係情報
                printSvfGuardian(param); //保護者情報
                printSvfSchregAddress(param); //生徒情報（住所）
                printSvfSchregBase(param); //生徒情報（氏名）
                svf.VrEndPage();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.error("printSvfSchregRegd read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /** 
     *  家族関係情報出力処理 
     */
    private void printSvfSchregRela(
            String param[]
    ) {
        //定義
        PreparedStatement ps = null;
        ResultSet rs = null;

        //RecordSet作成
        try {
            String sql = sqlSchregRela(param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int gyo_cnt = 0;
            while( rs.next() ){
                gyo_cnt++;
                String gyo = String.valueOf(gyo_cnt);
                if (getMS932ByteLength(rs.getString("RELANAME")) > 28) {
                    svf.VrsOut("SCHREG_RELA_NAME_SHOW1" + gyo,  rs.getString("RELANAME") );
                } else {
                    svf.VrsOut("SCHREG_RELA_NAME_SHOW" + gyo,  rs.getString("RELANAME") );
                }
                svf.VrsOut("SCHREG_RELATIONSHIP" + gyo,  rs.getString("RELATIONSHIP") );
                String birthday = "　　・　・　";
                if (rs.getString("RELABIRTHDAY") != null) {
                    birthday = rs.getString("RELABIRTHDAY").substring(0,4) + "・"
                             + rs.getString("RELABIRTHDAY").substring(5,7) + "・"
                             + rs.getString("RELABIRTHDAY").substring(8,10);
                }
                svf.VrsOut("F_BIRTHDAY" + gyo,  birthday );
                String occupation = "";
                if (rs.getString("OCCUPATION") != null) occupation = rs.getString("OCCUPATION");
                if (rs.getString("HR_NAME") != null) occupation = occupation + rs.getString("HR_NAME");
                if (rs.getString("ATTENDNO") != null) occupation = occupation + String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番";
                if (12 < occupation.length()) {
                    svf.VrsOut("OCCUPATION" + String.valueOf(gyo_cnt * 2),  occupation );
                } else {
                    svf.VrsOut("OCCUPATION" + String.valueOf((gyo_cnt * 2) - 1),  occupation );
                }
            }
            while( gyo_cnt < 6 ){
                gyo_cnt++;
                svf.VrsOut("F_BIRTHDAY" + String.valueOf(gyo_cnt),  "　　・　・　" );
            }
        } catch (Exception ex) {
            log.error("printSvfSchregRela read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /** 
     *  保護者情報出力処理 
     */
    private void printSvfGuardian(
            String param[]
    ) {
        //定義
        PreparedStatement ps = null;
        ResultSet rs = null;

        //RecordSet作成
        try {
            svf.VrsOut("birthday2", "　　年　月　日生" );
            svf.VrsOut("OFFICE_TEL", "(    )" );
            svf.VrsOut("hogosya1tel", "(    )" );
            String sql = sqlGuardian(param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while( rs.next() ){
                svf.VrsOut("kana2",  rs.getString("GUARD_KANA") );
                if (getMS932ByteLength(rs.getString("GUARD_NAME")) >  28) {
                    svf.VrsOut("GUARD_NAME_2",  rs.getString("GUARD_NAME") );
                } else {
                    svf.VrsOut("GUARD_NAME",  rs.getString("GUARD_NAME") );
                }
                if (rs.getString("GUARD_BIRTH_YEAR") != null) {
                    String birthday  = rs.getString("GUARD_BIRTH_YEAR") + "年"
                                     + rs.getString("GUARD_BIRTH_MONTH") + "月"
                                     + rs.getString("GUARD_BIRTH_DAY") + "日生";
                    svf.VrsOut("birthday2",  birthday );
                }
                svf.VrsOut("GUARD_RELATIONSHIP",  rs.getString("RELATIONSHIP") );
                if (rs.getString("GUARD_WORK_NAME") != null) {
                    if (10 < rs.getString("GUARD_WORK_NAME").length()) {
                        svf.VrsOut("OFFICE2",  rs.getString("GUARD_WORK_NAME") );
                    } else {
                        svf.VrsOut("OFFICE1",  rs.getString("GUARD_WORK_NAME") );
                    }
                }
                if (rs.getString("GUARD_WORK_TELNO") != null) 
                    svf.VrsOut("OFFICE_TEL",  rs.getString("GUARD_WORK_TELNO") );
                String guard_addr1 = rs.getString("GUARD_ADDR1");
                String guard_addr2 = rs.getString("GUARD_ADDR2");
                //住所１あり、住所２あり
                if (guard_addr1 != null && guard_addr2 != null) {
                    if ("1".equals(_useAddrField2) && 24 < guard_addr1.length() && 24 < guard_addr2.length()) {
                        svf.VrsOut("GUARD3_ADDRESS1",  guard_addr1.substring(0,15) );
                        svf.VrsOut("GUARD3_ADDRESS2",  guard_addr1.substring(15) );
                        svf.VrsOut("GUARD3_ADDRESS3",  guard_addr2.substring(0,15) );
                        svf.VrsOut("GUARD3_ADDRESS4",  guard_addr2.substring(15) );
                    } else if ("1".equals(_useAddrField2) && 24 < guard_addr1.length()) {
                        svf.VrsOut("GUARD3_ADDRESS1",  guard_addr1.substring(0,15) );
                        svf.VrsOut("GUARD3_ADDRESS2",  guard_addr1.substring(15) );
                        svf.VrsOut("GUARD3_ADDRESS3",  guard_addr2 );
                    } else if ("1".equals(_useAddrField2) && 24 < guard_addr2.length()) {
                        svf.VrsOut("GUARD3_ADDRESS1",  guard_addr1);
                        svf.VrsOut("GUARD3_ADDRESS3",  guard_addr2.substring(0,15) );
                        svf.VrsOut("GUARD3_ADDRESS4",  guard_addr2.substring(15) );
                    } else if (12 < guard_addr1.length() && 12 < guard_addr2.length()) {
                        svf.VrsOut("GUARD2_ADDRESS1",  guard_addr1.substring(0,12) );
                        svf.VrsOut("GUARD2_ADDRESS2",  guard_addr1.substring(12) );
                        svf.VrsOut("GUARD2_ADDRESS3",  guard_addr2.substring(0,12) );
                        svf.VrsOut("GUARD2_ADDRESS4",  guard_addr2.substring(12) );
                    } else if (12 < guard_addr1.length()) {
                        svf.VrsOut("GUARD1_ADDRESS1",  guard_addr1.substring(0,12) );
                        svf.VrsOut("GUARD1_ADDRESS2",  guard_addr1.substring(12) );
                        svf.VrsOut("GUARD1_ADDRESS3",  guard_addr2 );
                    } else if (12 < guard_addr2.length()) {
                        svf.VrsOut("GUARD1_ADDRESS1",  guard_addr1 );
                        svf.VrsOut("GUARD1_ADDRESS2",  guard_addr2.substring(0,12) );
                        svf.VrsOut("GUARD1_ADDRESS3",  guard_addr2.substring(12) );
                    } else {
                        svf.VrsOut("GUARD2_ADDRESS2",  guard_addr1 );
                        svf.VrsOut("GUARD2_ADDRESS3",  guard_addr2 );
                    }
                //住所１あり、住所２なし
                } else if (guard_addr1 != null) {
                    if (12 < guard_addr1.length()) {
                        svf.VrsOut("GUARD2_ADDRESS2",  guard_addr1.substring(0,12) );
                        svf.VrsOut("GUARD2_ADDRESS3",  guard_addr1.substring(12) );
                    } else {
                        svf.VrsOut("GUARD1_ADDRESS2",  guard_addr1 );
                    }
                //住所１なし、住所２あり
                } else if (guard_addr2 != null) {
                    if (12 < guard_addr2.length()) {
                        svf.VrsOut("GUARD2_ADDRESS2",  guard_addr2.substring(0,12) );
                        svf.VrsOut("GUARD2_ADDRESS3",  guard_addr2.substring(12) );
                    } else {
                        svf.VrsOut("GUARD1_ADDRESS2",  guard_addr2 );
                    }
                }
                if (rs.getString("GUARD_TELNO") != null) 
                    svf.VrsOut("hogosya1tel",  rs.getString("GUARD_TELNO") );
            }
        } catch (Exception ex) {
            log.error("printSvfGuardian read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /** 
     *  生徒情報（住所）出力処理 
     */
    private void printSvfSchregAddress(
            String param[]
    ) {
        //定義
        PreparedStatement ps = null;
        ResultSet rs = null;

        //RecordSet作成
        try {
            svf.VrsOut("TELNO2",  "(    )" );
            svf.VrsOut("TELNO",  "(    )" );
            String sql = sqlSchregAddress(param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while( rs.next() ){
                svf.VrsOut("zipcode",  rs.getString("ZIPCD") );
                if (rs.getString("TELNO") != null) 
                    svf.VrsOut("TELNO",  rs.getString("TELNO") );
                svf.VrsOut("add1",  rs.getString("ADDR1") );
                svf.VrsOut("add2",  rs.getString("ADDR2") );
            }
        } catch (Exception ex) {
            log.error("printSvfSchregAddress read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /** 
     *  生徒情報（氏名）出力処理 
     */
    private void printSvfSchregBase(
            String param[]
    ) {
        //定義
        PreparedStatement ps = null;
        ResultSet rs = null;

        //RecordSet作成
        try {
            svf.VrsOut("birthday",  "　　年　月　日生" );
            String sql = sqlSchregBase(param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while( rs.next() ){
                svf.VrsOut("kana",  rs.getString("NAME_KANA") );
                if (getMS932ByteLength(rs.getString("NAME")) > 28) {
                    svf.VrsOut("NAME_SHOW_2",  rs.getString("NAME") );
                } else {
                    svf.VrsOut("NAME_SHOW",  rs.getString("NAME") );
                }
                svf.VrsOut("SEX1",  rs.getString("SEX") );
                svf.VrsOut("NENDO",  rs.getString("ENT_YEAR") + " " );
                if (rs.getString("BIRTH_YEAR") != null) {
                    String birthday  = rs.getString("BIRTH_YEAR") + "年"
                                     + rs.getString("BIRTH_MONTH") + "月"
                                     + rs.getString("BIRTH_DAY") + "日生";
                    svf.VrsOut("birthday",  birthday );
                }
            }
        } catch (Exception ex) {
            log.error("printSvfSchregBase read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /** 
     *  顔写真情報のクエリー
     */
    private String sqlSchregPhoto(final String param[]) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  SCHREGNO,MIN(GRADE) AS GRADE ");
        stb.append(" FROM    SCHREG_REGD_DAT ");
        stb.append(" WHERE   SCHREGNO = '" + param[5] + "' AND ");
        stb.append("         SMALLINT(YEAR) IN ( ");
        stb.append("             SELECT  CASE WHEN MONTH(ENT_DATE) < 4 ");
        stb.append("                          THEN YEAR(ENT_DATE) + 1 ");
        stb.append("                          ELSE YEAR(ENT_DATE) END AS ENT_YEAR ");
        stb.append("             FROM    SCHREG_BASE_MST ");
        stb.append("             WHERE   SCHREGNO = '" + param[5] + "' ");
        stb.append("                 ) ");
        stb.append(" GROUP BY SCHREGNO ");
        return stb.toString();
    }

    /** 
     *  生徒証番号情報のクエリー
     */
    private String sqlSchregRegd(final String param[]) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  T1.YEAR, T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T3.GRADE_NAME1, T2.HR_CLASS_NAME1, ");
        stb.append("         CASE WHEN MOD(SMALLINT(T3.GRADE_CD),3) = 0 THEN 3 ELSE MOD(SMALLINT(T3.GRADE_CD),3) END AS GYO ");
        stb.append(" FROM    SCHREG_REGD_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR AND ");
        stb.append("           T2.SEMESTER = T1.SEMESTER AND ");
        stb.append("           T2.GRADE = T1.GRADE AND ");
        stb.append("           T2.HR_CLASS = T1.HR_CLASS ");
        stb.append("         LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND ");
        stb.append("           T3.GRADE = T1.GRADE ");
        stb.append(" WHERE   T1.SCHREGNO = '" + param[5] + "' AND ");
        stb.append("         T1.YEAR <= '" + param[0] + "' AND ");
        stb.append("         T1.SEMESTER = '" + param[1] + "' ");
        stb.append("         AND T3.SCHOOL_KIND IN (SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + param[0] + "' AND GRADE = '" + param[23] + "') ");
        stb.append(" ORDER BY T1.YEAR ");
        return stb.toString();
    }

    /** 
     *  家族関係情報のクエリー
     */
    private String sqlSchregRela(final String param[]) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  T1.SCHREGNO,T1.RELANO,T1.RELANAME,T1.OCCUPATION, T1.RELABIRTHDAY, ");
        stb.append("         T3.HR_NAME,T3.ATTENDNO, ");
        stb.append("         T2.NAME1 AS RELATIONSHIP ");
        stb.append(" FROM    SCHREG_RELA_DAT T1 ");
        stb.append("         LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'H201' AND T2.NAMECD2 = T1.RELATIONSHIP ");
        stb.append("         LEFT JOIN ( ");
        stb.append("             SELECT  W1.SCHREGNO,W1.ATTENDNO,W2.HR_NAME ");
        stb.append("             FROM    SCHREG_REGD_DAT W1, SCHREG_REGD_HDAT W2 ");
        stb.append("             WHERE   W1.YEAR = '" + param[0] + "' AND ");
        stb.append("                     W1.SEMESTER = '" + param[1] + "' AND ");
        stb.append("                     W2.YEAR = W1.YEAR AND ");
        stb.append("                     W2.SEMESTER = W1.SEMESTER AND ");
        stb.append("                     W2.GRADE = W1.GRADE AND ");
        stb.append("                     W2.HR_CLASS = W1.HR_CLASS ");
        stb.append("         ) T3 ON T3.SCHREGNO = T1.RELA_SCHREGNO ");
        stb.append(" WHERE   T1.SCHREGNO = '" + param[5] + "' ");
        stb.append(" ORDER BY T1.RELANO ");
        return stb.toString();
    }

    /** 
     *  保護者情報のクエリー
     */
    private String sqlGuardian(final String param[]) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  T1.SCHREGNO,T1.GUARD_NAME,T1.GUARD_KANA, ");
        stb.append("         YEAR(T1.GUARD_BIRTHDAY) AS GUARD_BIRTH_YEAR, ");
        stb.append("         MONTH(T1.GUARD_BIRTHDAY) AS GUARD_BIRTH_MONTH, ");
        stb.append("         DAY(T1.GUARD_BIRTHDAY) AS GUARD_BIRTH_DAY, ");
        stb.append("         T1.GUARD_WORK_NAME,T1.GUARD_WORK_TELNO, ");
        stb.append("         CASE WHEN T1.GUARD_ADDR1 = T3.ADDR1 AND T1.GUARD_ADDR2 = T3.ADDR2 THEN NULL ELSE T1.GUARD_ADDR1 END AS GUARD_ADDR1, ");
        stb.append("         CASE WHEN T1.GUARD_ADDR1 = T3.ADDR1 AND T1.GUARD_ADDR2 = T3.ADDR2 THEN NULL ELSE T1.GUARD_ADDR2 END AS GUARD_ADDR2, ");
        stb.append("         CASE WHEN T1.GUARD_TELNO = T3.TELNO THEN NULL ELSE T1.GUARD_TELNO END AS GUARD_TELNO, ");
        stb.append("         T2.NAME1 AS RELATIONSHIP ");
        stb.append(" FROM    GUARDIAN_DAT T1 ");
        stb.append("         LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'H201' AND T2.NAMECD2 = T1.RELATIONSHIP ");
        stb.append("         LEFT JOIN ( ");
        stb.append("             SELECT  SCHREGNO,TELNO,ADDR1,ADDR2 ");
        stb.append("             FROM    SCHREG_ADDRESS_DAT ");
        stb.append("             WHERE   SCHREGNO = '" + param[5] + "' AND ");
        stb.append("                     ISSUEDATE IN ( ");
        stb.append("                         SELECT MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("                         FROM   SCHREG_ADDRESS_DAT ");
        stb.append("                         WHERE  SCHREGNO = '" + param[5] + "' ");
        stb.append("                     ) ");
        stb.append("         ) T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE   T1.SCHREGNO = '" + param[5] + "' ");
        return stb.toString();
    }

    /** 
     *  生徒情報（住所）のクエリー
     */
    private String sqlSchregAddress(final String param[]) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT SCHREGNO,ZIPCD,TELNO,ADDR1,ADDR2 ");
        stb.append(" FROM   SCHREG_ADDRESS_DAT ");
        stb.append(" WHERE  SCHREGNO = '" + param[5] + "' AND ");
        stb.append("        ISSUEDATE IN ( ");
        stb.append("         SELECT MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("         FROM   SCHREG_ADDRESS_DAT ");
        stb.append("         WHERE  SCHREGNO = '" + param[5] + "' ");
        stb.append("        ) ");
        return stb.toString();
    }

    /** 
     *  生徒情報（氏名）のクエリー
     */
    private String sqlSchregBase(final String param[]) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  T1.SCHREGNO,T1.NAME,T1.NAME_KANA, ");
        stb.append("         YEAR(T1.BIRTHDAY) AS BIRTH_YEAR, ");
        stb.append("         MONTH(T1.BIRTHDAY) AS BIRTH_MONTH, ");
        stb.append("         DAY(T1.BIRTHDAY) AS BIRTH_DAY, ");
        stb.append("         T2.NAME2 AS SEX, ");
        stb.append("         CASE WHEN MONTH(T1.ENT_DATE) < 4 ");
        stb.append("              THEN YEAR(T1.ENT_DATE) + 1 ");
        stb.append("              ELSE YEAR(T1.ENT_DATE) END AS ENT_YEAR ");
        stb.append(" FROM    SCHREG_BASE_MST T1 ");
        stb.append("         LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'Z002' AND T2.NAMECD2 = SEX ");
        stb.append(" WHERE   T1.SCHREGNO = '" + param[5] + "' ");
        return stb.toString();
    }

	/*----------------------------*
	 * 表出力           		  *
	 *----------------------------*/
	public void set_detail1(final String param[])
	                 throws ServletException, IOException
	{
        PreparedStatement ps = null;
	    ResultSet rs = null;
		try {
			String sql = new String();
			sql = "SELECT "
					+ "T1.SCHREGNO,"
					+ "T1.NAME AS SCH_NAME,"
					+ "T1.NAME_KANA AS SCH_KANA,"
					+ "T1.BIRTHDAY,"
					+ "(SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'Z002' AND NAMECD2 = T1.SEX) AS SEX,"
					+ "T2.ZIPCD,"
					+ "T2.ADDRESS,"
					+ "T2.TELNO,"
					+ "T2.FAXNO,"
					+ "T2.EMAIL,"
					+ "(SELECT FINSCHOOL_NAME FROM FINSCHOOL_MST ST2 WHERE T1.FINSCHOOLCD = ST2.FINSCHOOLCD) AS JUNIOR_SCHOOLNAME,"
					+ "T1.EMERGENCYCALL,"
					+ "T1.EMERGENCYTELNO,"
					+ "T3.GUARD_NAME,"
					+ "T3.GUARD_SEX,"
					+ "T3.GUARD_ADDRESS1,"
					+ "T3.GUARD_ADDRESS2,"
					+ "T3.GUARD_TELNO,"
					+ "T3.GUARD_RELATIONSHIP,"
					+ "T3.GUARD_JOBNAME,"
						//guarantor_XXXXXXは追加項目 2003/02/07
					+ "T3.GUARANTOR_NAME,"
					+ "T3.GUARANTOR_ADDRESS1,"
					+ "T3.GUARANTOR_ADDRESS2,"
					+ "T3.GUARANTOR_TELNO,"
					+ "T3.GUARANTOR_JOB,"
					+ "T3.GUARANTOR_RELATIONSHIP,"
					+ "T3.PUBLIC_OFFICE,"
					+ "T4.RELA_RELATIONSHIP,"
					+ "T4.RELA_GRADE,"
					+ "T4.RELA_HR_CLASS,"
					+ "T4.RELA_ATTENDNO,"
					+ "T4.RELA_NAME,"
					+ "T4.RELA_RELATIONSHIPNAME,"
					+ "YEAR('" + param[2] + "' - T4.RELA_BIRTHDAY) AS RELA_AGE,"
					+ "VALUE(T4.RELA_REGIDENTIALCD,'0') AS RELA_REGIDENTIALCD,"
					+ "T4.RELA_OCCUPATION "
				
				+ "FROM "
					+ "SCHREG_BASE_MST T1 "
				
					+ "LEFT JOIN("
						+ "SELECT "
							+ "ST1.SCHREGNO,"
							+ "ST1.ZIPCD,"
							+ "ST1.ADDR1 || value(ST1.ADDR2,'') AS ADDRESS,"
							+ "ST1.TELNO,"
							+ "ST1.FAXNO,"
							+ "ST1.EMAIL "
						+ "FROM "
							+ "SCHREG_ADDRESS_DAT ST1,"
							+ "("
								+ "SELECT "
									+ "SCHREGNO,"
									+ "MAX(ISSUEDATE) AS ISSUEDATE "
								+ "FROM "
									+ "SCHREG_ADDRESS_DAT "
								+ "WHERE "
										+ "ISSUEDATE <= '" + param[4] + "' "
									+ "AND EXPIREDATE >= '" + param[3] + "' "
								+ "GROUP BY "
									+ "SCHREGNO "
							+ ")ST2 "
						+ "WHERE "
								+ "ST1.SCHREGNO = '" + param[5] + "' "
							+ "AND ST1.SCHREGNO = ST2.SCHREGNO "
							+ "AND ST1.ISSUEDATE = ST2.ISSUEDATE "
					+ ")T2 ON T2.SCHREGNO = T1.SCHREGNO "
				
					+ "LEFT JOIN ("
						+ "SELECT "
							+ "ST1.SCHREGNO,"
							+ "ST1.GUARD_NAME,"
							+ "(SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'Z002' "
								+ "AND NAMECD2 = ST1.GUARD_SEX) AS GUARD_SEX,"
							+ "ST1.GUARD_ADDR1 AS GUARD_ADDRESS1,"
							+ "ST1.GUARD_ADDR2 AS GUARD_ADDRESS2,"
							+ "ST1.GUARD_TELNO,"
							+ "(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'H202' "
									+ "AND NAMECD2 = ST1.GUARD_JOBCD) AS GUARD_JOBNAME,"
							+ "(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'H201' "
									+ "AND NAMECD2 = ST1.RELATIONSHIP) AS GUARD_RELATIONSHIP,"
							+ "ST1.GUARANTOR_NAME,"
							+ "ST1.GUARANTOR_ADDR1 AS GUARANTOR_ADDRESS1,"
							+ "ST1.GUARANTOR_ADDR2 AS GUARANTOR_ADDRESS2,"
							+ "ST1.GUARANTOR_TELNO,"
							+ "(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'H202' "
									+ "AND NAMECD2 = ST1.GUARANTOR_JOBCD) AS GUARANTOR_JOB,"
							+ "(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'H201' "
									+ "AND NAMECD2 = ST1.GUARANTOR_RELATIONSHIP) AS GUARANTOR_RELATIONSHIP,"
							+ "ST1.PUBLIC_OFFICE "
						+ "FROM "
							+ "GUARDIAN_DAT ST1 "
						+ "WHERE "
							+ "ST1.SCHREGNO = '" + param[5] + "' "
					+ ")T3 ON T3.SCHREGNO = T1.SCHREGNO "
				
					+ "LEFT JOIN("
						+ "SELECT "
							+ "ST1.SCHREGNO,"
							+ "ST3.GRADE AS RELA_GRADE,"
							+ "ST3.HR_CLASS AS RELA_HR_CLASS,"
							+ "ST3.ATTENDNO AS RELA_ATTENDNO,"
							+ "ST1.RELANAME AS RELA_NAME,"
							+ "ST1.RELATIONSHIP AS RELA_RELATIONSHIP,"
							+ "ST2.NAME1 AS RELA_RELATIONSHIPNAME,"
							+ "ST1.RELABIRTHDAY AS RELA_BIRTHDAY,"
							+ "VALUE(ST1.REGIDENTIALCD,'0') AS RELA_REGIDENTIALCD,"
							+ "ST1.OCCUPATION AS RELA_OCCUPATION "
						+ "FROM "
				 			+ "SCHREG_RELA_DAT ST1 "
							+ "LEFT JOIN NAME_MST ST2 ON ST2.NAMECD1 = 'H201' AND ST2.NAMECD2 = ST1.RELATIONSHIP "
							+ "LEFT JOIN SCHREG_REGD_DAT ST3 ON ST3.YEAR = '" + param[0] + "' "
																+ "AND ST3.SEMESTER = '" + param[1] + "' "
																+ "AND ST3.SCHREGNO = ST1.RELA_SCHREGNO "
						+ "WHERE "
							+ "ST1.SCHREGNO = '" + param[5] + "' "
					+ ")T4 ON T4.SCHREGNO = T1.SCHREGNO "
				
				+ "WHERE "
					+ "T1.SCHREGNO = '" + param[5] + "' "
				+ "ORDER BY "
					+ "T1.SCHREGNO,"
					+ "T4.RELA_RELATIONSHIP";

			ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            log.debug("[KNJH030H]set_detail1 sql ok!");

  	  	   /** 照会結果の取得とsvf_formへ出力 **/
			svf.VrSetForm("KNJH030_1.frm", 1);	   	//SuperVisualFormadeで設計したレイアウト定義態の設定

			boolean nonedata2 = false;
			int ia = 0;	//家族欄の行PHOTO1
            int namecheck = 0;
            String nameSet;
			while (rs.next()) {
				//最初のみ出力
				if(nonedata2 == false){
					svf.VrsOut("ymd"  	 	, "("+KNJ_EditDate.h_format_JP_M(param[2])+"調査)");		//調査年月日
					svf.VrsOut("schoolname"  	, param[16]);					//学校名

                    svf.VrsOut("PHOTO1",  "入学後、学校で、" );
                    svf.VrsOut("PHOTO2",  "撮影します。" );
					String strx = param[14] + "/P" + param[5] + "." + param[21];
                    File f1 = new File(strx);
                    if (f1.exists()) {
                        svf.VrsOut("BITMAP"         , strx);                    //写真
                        for (int i = 1; i < 3; i++) {
                            svf.VrsOut("PHOTO" + String.valueOf(i),  "" );
                        }
                    }

					svf.VrsOut("kana"  	 	, rs.getString("SCH_KANA"));	//ふりがな
					if (getMS932ByteLength(rs.getString("SCH_NAME")) > 28) {
					    svf.VrsOut("NAME_SHOW_2"  	, rs.getString("SCH_NAME"));	//氏名
					} else {
                        svf.VrsOut("NAME_SHOW"      , rs.getString("SCH_NAME"));    //氏名
					}
					svf.VrsOut("SEX1"  	 	, rs.getString("SEX"));			//性別
					svf.VrsOut("birthday"  	, KNJ_EditDate.h_format_JP_Bth(rs.getString("BIRTHDAY")));	//生年月日
					svf.VrsOut("zipcode"  	, rs.getString("ZIPCD"));		//郵便番号
					if ("1".equals(_useAddrField2) && getMS932ByteLength(rs.getString("ADDRESS")) > 100) {
	                    svf.VrsOut("add1_2"       , rs.getString("ADDRESS"));     //住所1
					} else {
	                    svf.VrsOut("add1"       , rs.getString("ADDRESS"));     //住所1
					}
					svf.VrsOut("TELNO"  	 	, rs.getString("TELNO"));		//ＴＥＬ
					svf.VrsOut("FAXNO"  	 	, rs.getString("FAXNO"));		//ＦＡＸ
					svf.VrsOut("E_MAIL"  	 	, rs.getString("EMAIL"));		//Ｅ＿ＭＡＩＬ
					svf.VrsOut("SCHOOL"     			, param[17]);
					if(rs.getString("JUNIOR_SCHOOLNAME") != null) {
                        nameSet = rs.getString("JUNIOR_SCHOOLNAME");
                        namecheck = rs.getString("JUNIOR_SCHOOLNAME").indexOf("　");
                        if ((namecheck + 1) <= 6) {
                            nameSet = rs.getString("JUNIOR_SCHOOLNAME").substring(namecheck + 1);
                        }
						svf.VrsOut("J_NAME", nameSet + param[17]);	//出身小・中学校
                    }
					svf.VrsOut("EMERGENCYCALL"		, rs.getString("EMERGENCYCALL"));		//緊急連絡先
					svf.VrsOut("EMERGENCYCALL_TEL"  	, rs.getString("EMERGENCYTELNO"));		//緊急連絡先
					if (getMS932ByteLength(rs.getString("GUARD_NAME")) > 28) {
	                    svf.VrsOut("GUARD_NAME_2"           , rs.getString("GUARD_NAME"));          //保護者氏名
					} else {
	                    svf.VrsOut("GUARD_NAME"             , rs.getString("GUARD_NAME"));          //保護者氏名
					}
					svf.VrsOut("SEX2"  	 			, rs.getString("GUARD_SEX"));			//保護者性別
					svf.VrsOut("GUARD_RELATIONSHIP"	, rs.getString("GUARD_RELATIONSHIP"));	//保護者続柄
					svf.VrsOut("GUARD_ADDRESS1"		, rs.getString("GUARD_ADDRESS1"));		//保護者住所
					svf.VrsOut("GUARD_ADDRESS2"		, rs.getString("GUARD_ADDRESS2"));		//保護者住所
					svf.VrsOut("hogosya1tel"  		, rs.getString("GUARD_TELNO"));			//保護者電話番号
					param[6] = rs.getString("SCH_NAME");
					param[7] = rs.getString("GUARANTOR_NAME");
					param[8] = rs.getString("GUARANTOR_ADDRESS1");
					param[15] = rs.getString("GUARANTOR_ADDRESS2");
					param[9] = rs.getString("GUARANTOR_TELNO");
					param[10] = rs.getString("GUARANTOR_JOB");
					param[11] = rs.getString("GUARANTOR_RELATIONSHIP");
					param[12] = rs.getString("PUBLIC_OFFICE");
					nonedata2 = true;
				}
				//家族欄の出力
				ia++;
				if (ia > 9) {
				    break;
				}
				if (rs.getString("RELA_NAME") != null) {
	                if (getMS932ByteLength(rs.getString("RELA_NAME")) >  28) {
	                    svf.VrsOut("SCHREG_RELA_NAME_SHOW2"+ ia , rs.getString("RELA_NAME"));			//氏名
	                } else if (getMS932ByteLength(rs.getString("RELA_NAME")) >  20) {
                        svf.VrsOut("SCHREG_RELA_NAME_SHOW1"+ ia , rs.getString("RELA_NAME"));            //氏名
	                } else {
                        svf.VrsOut("SCHREG_RELA_NAME_SHOW"+ ia , rs.getString("RELA_NAME"));            //氏名
	                }
					svf.VrsOut("SCHREG_RELATIONSHIP" 	+ ia , rs.getString("RELA_RELATIONSHIPNAME"));	//続柄
					svf.VrsOut("AGE" 					+ ia , rs.getString("RELA_AGE"));			//年齢
					StringBuffer sbx = new StringBuffer();
					if(rs.getString("RELA_OCCUPATION") != null){
						sbx.append(rs.getString("RELA_OCCUPATION"));
						sbx.append(" ");
					}
					if(rs.getString("RELA_GRADE") != null){
						sbx.append(rs.getInt("RELA_GRADE"));
						sbx.append("年");
					}
					if(rs.getString("RELA_HR_CLASS") != null){
						sbx.append(rs.getInt("RELA_HR_CLASS"));
						sbx.append("組");
					}
					if(rs.getString("RELA_ATTENDNO") != null){
						sbx.append(rs.getInt("RELA_ATTENDNO"));
						sbx.append("番");
					}
					svf.VrsOut("OCCUPATION" 	+ ia , sbx.toString());			//職業・学校

					if(Integer.parseInt(rs.getString("RELA_REGIDENTIALCD")) == 1)
						svf.VrsOut("CHECK" 				+ ia , "○");		//同居区分
				}
			}
			if (nonedata2 == true) {
				svf.VrEndRecord();
				svf.VrEndPage();
				nonedata = true;
			}
            log.debug("[KNJH030H]set_detail1 read ok!");
		} catch (Exception ex) {
            log.error("[KNJH030H]set_detail1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
		}

	}	//set_detail1の括り


	/*----------------------------*
	 * 裏出力           		  *
	 *----------------------------*/
	public void set_detail2(final String param[])
	                 throws ServletException, IOException
	{
        PreparedStatement ps = null;
	    ResultSet rs = null;
		try {
			String sql = new String();
			sql = "SELECT "
					+ "T2.SCHREGNO,"
					+ "T2.GRADE,"
					+ "T2.HR_CLASS,"
					+ "T2.ATTENDNO,"
                    + "T3.HR_CLASS_NAME1,"
					+ "T3.STAFF_NAME, "
                    + "T4.GRADE_NAME1,"
                    + "CASE WHEN MOD(SMALLINT(T4.GRADE_CD),3) = 0 THEN 3 ELSE MOD(SMALLINT(T4.GRADE_CD),3) END AS GYO "
				+ "FROM "
					//各学年の最新年度・学期を取得
					+ "("
						+ "SELECT "
							+ "SCHREGNO,"
							+ "MAX(st2.YEAR || st2.SEMESTER) AS YEAR_SEMESTER "
						+ "FROM "
							+ "SCHREG_REGD_DAT ST2,"
							+ "SCHREG_REGD_HDAT ST3, "
                            + "SCHREG_REGD_GDAT ST4 "
						+ "WHERE "
								+ "ST2.SCHREGNO = '" + param[5] + "' "
	                        + "AND ST2.YEAR || ST2.SEMESTER <= '" + param[0] + param[1] + "' "
							+ "AND ST2.YEAR = ST3.YEAR "
							+ "AND ST2.SEMESTER = ST3.SEMESTER "
							+ "AND ST2.GRADE = ST3.GRADE "
							+ "AND ST2.HR_CLASS = ST3.HR_CLASS "
                            + "AND ST4.YEAR = ST2.YEAR "
                            + "AND ST4.GRADE = ST2.GRADE "
                            + "AND ST4.SCHOOL_KIND IN (SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + param[0] + "' AND GRADE = '" + param[23] + "') "
						+ "GROUP BY "
							+ "SCHREGNO,"
							+ "ST2.GRADE "
					+ ")T1 "
				
					+ "INNER JOIN("
						+ "SELECT "
							+ "SCHREGNO,"
							+ "YEAR,"
							+ "SEMESTER,"
							+ "GRADE,"
							+ "HR_CLASS,"
							+ "ATTENDNO "
						+ "FROM "
							+ "SCHREG_REGD_DAT "
						+ "WHERE "
							+ "SCHREGNO = '" + param[5] + "' "
					+ ")T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR || T2.SEMESTER = T1.YEAR_SEMESTER "
				
					+ "LEFT JOIN("
						+ "SELECT "
							+ "ST1.YEAR,"
							+ "ST1.SEMESTER,"
							+ "ST1.GRADE,"
							+ "HR_CLASS,"
                            + "HR_CLASS_NAME1,"
							+ "ST2.STAFFNAME AS STAFF_NAME "
						+ "FROM "
							+ "SCHREG_REGD_HDAT ST1,"
							+ "STAFF_MST ST2 "
						+ "WHERE "
							+ "ST1.TR_CD1 = ST2.STAFFCD "
					+ ")T3 ON T3.YEAR = T2.YEAR AND T3.SEMESTER = T2.SEMESTER "
								+ "AND T3.GRADE = T2.GRADE AND T3.HR_CLASS = T2.HR_CLASS "

					+ "LEFT JOIN SCHREG_REGD_GDAT T4 ON T4.YEAR = T2.YEAR AND T4.GRADE = T2.GRADE "

					+ "ORDER BY "
						+ "T2.SCHREGNO,"
						+ "T2.GRADE";

			ps = db2.prepareStatement(sql);
			rs = ps.executeQuery();
            log.debug("[KNJH030H]set_detail2 sql ok!");

  	  	   /** 照会結果の取得とsvf_formへ出力 **/
			svf.VrSetForm("KNJH030_2.frm", 1);	   	//SuperVisualFormadeで設計したレイアウト定義態の設定

			boolean nonedata2 = false;
			while (rs.next()) {
				int ia = Integer.parseInt(rs.getString("GYO"));	//学年の印字行
				if (nonedata2 == true && ia == 1) {
				    svf.VrEndRecord();
				    svf.VrEndPage();
				    nonedata2 = false;
				}
				//最初のみ出力
				if (nonedata2 == false) {
                    if (getMS932ByteLength(param[6]) > 28) { 
                        svf.VrsOut("NAME_SHOW_3"        , param[6]);    //生徒氏名
                    } else if (getMS932ByteLength(param[6]) > 20) { 
                        svf.VrsOut("NAME_SHOW_2"        , param[6]);    //生徒氏名
                    } else {
                        svf.VrsOut("NAME_SHOW"  		, param[6]);	//生徒氏名
                    }
					if (getMS932ByteLength(param[7]) > 28) { 
					    svf.VrsOut("GUARA_NAME_2"  		, param[7]);	//保証人氏名
					} else {
                        svf.VrsOut("GUARA_NAME"         , param[7]);    //保証人氏名
					}
                    if ("1".equals(_useAddrField2) && (getMS932ByteLength(param[8]) > 54 || getMS932ByteLength(param[15]) > 54)) { 
                        svf.VrsOut("GUARA_ADDRESS1_2" , param[8]);    //保証人住所
                        svf.VrsOut("GUARA_ADDRESS2_2" , param[15]);   //保証人住所
                    } else {
                        svf.VrsOut("GUARA_ADDRESS1" , param[8]);    //保証人住所
                        svf.VrsOut("GUARA_ADDRESS2" , param[15]);   //保証人住所
                    }
					svf.VrsOut("call_name"  		, param[9]);	//保証人電話番号
					svf.VrsOut("OCCUPATION"  		, param[10]);	//保証人職業
					svf.VrsOut("relation"  		, param[11]);	//保証人生徒との関係
					svf.VrsOut("public1"  		, param[12]);	//保証人兼任公職
				}
				//学年ごとのデータ
				svf.VrsOutn("GRADE1" 			,ia , rs.getString("GRADE_NAME1"));		//学年

				final String hrClass;
				if (null != rs.getString("HR_CLASS_NAME1")) {
				    hrClass = rs.getString("HR_CLASS_NAME1");
				} else if (StringUtils.isNumeric(rs.getString("HR_CLASS"))){
                    hrClass = String.valueOf(Integer.parseInt(rs.getString("HR_CLASS")));
				} else {
                    hrClass = rs.getString("HR_CLASS");
				}
                svf.VrsOutn("HR_CLASS" 		,ia , hrClass);		//学級
				svf.VrsOutn("ATTENDNO"		,ia , rs.getString("ATTENDNO"));		//番号
				svf.VrsOutn("TR_NAME3" 		,ia , rs.getString("STAFF_NAME"));		//学級担任名

				nonedata2 = true;
			}

			printEnvir(db2,svf,param);
			if (nonedata2 == true) {
				svf.VrEndRecord();
				svf.VrEndPage();
				nonedata = true;
			}
            log.debug("[KNJH030H]set_detail2 read ok!");
		} catch (Exception ex) {
            log.error("[KNJH030H]set_detail2 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
		}

	}	//set_detail2の括り


	/**生徒環境データ印字処理*/
	private void printEnvir(final DB2UDB db2, final Vrw32alp svf, final String param[])
	{
	    PreparedStatement ps = null;
	    ResultSet rs = null;
		try {
			int ia = 1;
			ps = db2.prepareStatement(sqlEnvir(param));
			rs = ps.executeQuery();
			svf.VrsOutn("GRADE2" 			,ia , "値");
			svf.VrsOutn("GRADE3" 			,ia , "値");
			while (rs.next()) {

				svf.VrsOutn("DISEASE1" 		,ia , rs.getString("DISEASE"));			//既往の疾患
				svf.VrsOutn("HEALTHCONDITION1",ia , rs.getString("HEALTHCONDITION"));	//現在の健康状態

				set_toknizer(rs.getString("MERITS")			,"MERITS"			,ia,2);		//長所
				set_toknizer(rs.getString("DEMERITS")		,"DEMERITS"			,ia,2);		//短所
				set_toknizer(rs.getString("GOOD_SUBJECT")	,"GOOD_SUBJECT"		,ia,2);		//得意学科
				set_toknizer(rs.getString("BAD_SUBJECT")	,"BAD_SUBJECT"		,ia,2);		//不得意学科
				set_toknizer(rs.getString("HOBBY")			,"HOBBY"			,ia,2);		//趣味・娯楽
				set_toknizer(rs.getString("READING")		,"READINGS"			,ia,2);		//読書傾向
				set_toknizer(rs.getString("SPORTS")			,"SPORTS"			,ia,2);		//スポーツ
				set_toknizer(rs.getString("FRIENDSHIP")		,"FRIENDSHIP"		,ia,2);		//交友
				set_toknizer(rs.getString("PLANUNIV")		,"shingaku"			,ia,2);		//志望：進学
				set_toknizer(rs.getString("PLANJOB")		,"shushiku"			,ia,2);		//志望：就職
				set_toknizer(rs.getString("ED_ACT")			,"SPECIALACTIVITY"	,ia,2);		//特別教育活動
				set_toknizer(rs.getString("OLD_CRAM")		,"PRIVATE_SCHOOL1_"	,ia,2);		//小学校：塾
				set_toknizer(rs.getString("PRIZES")			,"reward"			,ia,4);		//検定・賞罰・その他
				set_toknizer(rs.getString("REMARK")			,"note4_"			,ia,4);		//備考

				svf.VrsOutn("PRIVATE_SCHOOL2_1"	,ia ,rs.getString("CUR_CRAM_NAME"));	//現在：塾
				svf.VrsOutn("PRIVATE_SCHOOL2_2"	,ia ,rs.getString("CUR_CRAM"));			//現在：塾
				svf.VrsOutn("STUDY" 				,ia ,rs.getString("STUDYTIME_NAME"));	//学習

				if (rs.getString("POCKETMONEY") != null) {
					StringBuffer sbx = new StringBuffer();
					if(rs.getString("POCKETMONEY_NAME") != null) {
					    sbx.append(rs.getString("POCKETMONEY_NAME"));
					}
					sbx.append("(");
					if(rs.getString("POCKETMONEY") != null){
						sbx.append(rs.getString("POCKETMONEY"));
					} else{
						sbx.append("    ");
					}
					sbx.append("円)");
					svf.VrsOutn("MONEY"	,ia	,sbx.toString());							//こづかい
				}

				svf.VrsOutn("METHOD1" 		,ia, rs.getString("HOWTOCOM_NAME"));		//通学方法
				svf.VrsOutn("METHOD2" 		,ia, rs.getString("OTHERHOWTOCOMMUTE"));	//通学方法その他

				if (rs.getString("COMMUTE_HOURS") != null) {
					svf.VrsOutn("TIMESTOCOME" 	,ia, String.valueOf(rs.getInt("COMMUTE_HOURS"))+":"+rs.getString("COMMUTE_MINUTES"));	//通学時間
				}
				svf.VrsOutn("LESSON1" 		,ia, rs.getString("LESSON_NAME"));			//けいこごと
				svf.VrsOutn("LESSON2" 		,ia, rs.getString("LESSON"));				//けいこごと

				String sleep = new String();
				if (rs.getString("BEDTIME_HOURS") != null) {
					sleep = rs.getString("BEDTIME_HOURS") + ":" + rs.getString("BEDTIME_MINUTES") + "頃\uFF5E";
				}
				if (rs.getString("RISINGTIME_HOURS") != null) {
					sleep = sleep + rs.getString("RISINGTIME_HOURS") + ":" + rs.getString("RISINGTIME_MINUTES") + "頃まで";
				}
				svf.VrsOutn("SLEEP" 		,ia , sleep);									//就寝起床

				if (rs.getString("SLEEPTIME") != null) {
					String strx = rs.getString("SLEEPTIME");
					int hh = Integer.parseInt(strx.substring(8,10));
					int mm = Integer.parseInt(strx.substring(10,12));
					StringBuffer sbx = new StringBuffer();
					if(hh > 0){
						sbx.append(hh);
						sbx.append("時間");
					}
					if(mm > 0){
						sbx.append(mm);
						sbx.append("分");
					}
					svf.VrsOutn("SLEEP3" 			,ia , sbx.toString());					//睡眠時間
				}

				svf.VrsOutn("TV_VIEWING1" 	,ia , rs.getString("TVVIEWINGHOURS_NAME"));	//テレビ視聴時間
				svf.VrsOutn("TV_VIEWING2" 	,ia , rs.getString("TVPROGRAM"));			//主に見る番組
				svf.VrsOutn("PC_TIME" 		,ia , rs.getString("PC_HOURS_NAME"));		//パソコン時間

			}

		} catch (Exception ex) {
            log.error("[KNJH030H]printEnvir error!", ex);
		} finally {
		    DbUtils.closeQuietly(null, ps, rs);
		    db2.commit();
		}

	}//printEnvir()の括り

	/**
	 *	生徒環境データを抽出
	 *
	 */
	private String sqlEnvir(final String param[])
	{
	    final StringBuffer stb = new StringBuffer();
		try {

			stb.append("SELECT ");
			stb.append("	DISEASE, ");
			stb.append("	HEALTHCONDITION, ");
			stb.append("	MERITS, ");
			stb.append("	DEMERITS, ");
			stb.append("	GOOD_SUBJECT, ");
			stb.append("	BAD_SUBJECT, ");
			stb.append("	HOBBY, ");
			stb.append("	OLD_CRAM, ");
			stb.append("	(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'H101' ");
			stb.append("			AND INT(NAMECD2) = INT(VALUE(CUR_CRAMCD,'0'))) AS CUR_CRAM_NAME, ");
			stb.append("	CUR_CRAM, ");
			stb.append("	(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'H103' ");
			stb.append("			AND INT(NAMECD2) = INT(VALUE(STUDYTIME,'0'))) AS STUDYTIME_NAME, ");
			stb.append("	(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'H104' ");
			stb.append("			AND INT(NAMECD2) = INT(VALUE(POCKETMONEYCD,'0'))) AS POCKETMONEY_NAME, ");
			stb.append("	POCKETMONEY, ");
			stb.append("	PRIZES, ");
			stb.append("	READING, ");
			stb.append("	SPORTS, ");
			stb.append("	FRIENDSHIP, ");
			stb.append("	PLANUNIV, ");
			stb.append("	PLANJOB, ");
			stb.append("	ED_ACT, ");
			stb.append("	(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'H100' ");
			stb.append("			AND INT(NAMECD2) = INT(VALUE(HOWTOCOMMUTECD,'0'))) AS HOWTOCOM_NAME, ");
			stb.append("	OTHERHOWTOCOMMUTE, ");
			stb.append("	COMMUTE_HOURS, ");
			stb.append("	COMMUTE_MINUTES, ");
			stb.append("	(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'H102' ");
			stb.append("			AND INT(NAMECD2) = INT(VALUE(LESSONCD,'0'))) AS LESSON_NAME, ");
			stb.append("	LESSON, ");
			stb.append("	BEDTIME_HOURS, ");
			stb.append("	BEDTIME_MINUTES, ");
			stb.append("	RISINGTIME_HOURS, ");
			stb.append("	RISINGTIME_MINUTES, ");
			stb.append("	CHAR(CASE WHEN RISINGTIME_HOURS || RISINGTIME_MINUTES < BEDTIME_HOURS || BEDTIME_MINUTES THEN ");
			stb.append("		TIMESTAMP(CURRENT DATE , RISINGTIME_HOURS || ':' || RISINGTIME_MINUTES || ':00') - TIMESTAMP(CURRENT DATE - 1 DAY , BEDTIME_HOURS || ':' || BEDTIME_MINUTES || ':00') ");
			stb.append("		ELSE ");
			stb.append("		TIMESTAMP(CURRENT DATE , RISINGTIME_HOURS || ':' || RISINGTIME_MINUTES || ':00') - TIMESTAMP(CURRENT DATE , BEDTIME_HOURS || ':' || BEDTIME_MINUTES || ':00') ");
			stb.append("		END) AS SLEEPTIME, ");
			stb.append("	(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'H105' ");
			stb.append("			AND INT(NAMECD2) = INT(VALUE(TVVIEWINGHOURSCD,'0'))) AS TVVIEWINGHOURS_NAME, ");
			stb.append("	TVPROGRAM, ");
			stb.append("	(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'H105' ");
			stb.append("			AND INT(NAMECD2) = INT(VALUE(PC_HOURS,'0'))) AS PC_HOURS_NAME, ");
			stb.append("	REMARK ");
			stb.append("FROM ");
			stb.append("	SCHREG_ENVIR_DAT ");
			stb.append("WHERE ");
			stb.append("	SCHREGNO = '" + param[5] + "' ");

		} catch (Exception ex) {
            log.error("[KNJH030H]sqlEnvir error!", ex);
		}
		return stb.toString();

	}//sqlEnvir()の括り

	/*----------------------*
	 * 改行編集後ＳＶＦ出力 *
	 *----------------------*/
	public void set_toknizer(final String strx, final String stry, final int ia, final int ib)
	                 throws ServletException, IOException
	{
		try {
			int ic;
			StringTokenizer	stkx;

			if (strx != null) {
				stkx = new StringTokenizer(strx, "\r\n", false);
				ic = 0;
				while (stkx.hasMoreTokens() & ic < ib) {
					svf.VrsOutn(stry+(ic+1)	,ia , stkx.nextToken());
					ic++;
				}
			}

		} catch (Exception ex) {
            log.error("[KNJH030H]set_toknizer error!", ex);
		}
	}  //set_toknizerの括り

}	//クラスの括り

