// kanji=漢字
/*
 * $Id: 5ab9b1c4a9ad66f3345268a2ae57807019e97bf3 $
 *
 * 作成日: 2005/11/28
 * 作成者: m-yama
 *
 * Copyright(C) 2005-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */package servletpack.KNJH;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [生徒指導情報システム]
 *
 *					＜ＫＮＪＨ１３０＞  地域別人数一覧
 *
 *  2005/11/28 m-yama 新規作成
 *  2005/11/30 yamashiro
 *  2005/12/13 yamashiro 東大阪市が集計されない不具合を修正
 *                       美原町を追加
 *  2005/12/16 yamashiro 12/13/の修正における不具合を修正
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJH130TiikiItiran {

    private static final Log log = LogFactory.getLog(KNJH130TiikiItiran.class);

    private String _useSchool_KindField;
    private String _SCHOOLKIND;
    private String _maxGrade;
    private String _minGrade;

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[]  = new String[7];		//05/02/18Modify yamashiro

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         						//年度
			param[1] = request.getParameter("GAKKI");   							//学期
			param[4] = request.getParameter("OUTPUTA");   							//出力対象
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
		} catch( Exception ex ) {
			log.warn("parameter error!", ex);
		}

	//	print設定
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

	//	svf設定
		svf.VrInit();						   	//クラスの初期化
		svf.VrSetSpoolFileStream(outstrm);   		//PDFファイル名の設定

	//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch( Exception ex ) {
			log.error("DB2 open error!", ex);
			return;
		}

	//	ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
		boolean nonedata = false; 								//該当データなしフラグ
        Set_Head(db2, svf, param);                              //見出し出力のメソッド
        
        if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
            setGradeMaxMin(db2, param);

            //SQL作成
            try {
                ps1 = db2.prepareStatement(Pre_Stat1(param, _minGrade, _maxGrade));       //生徒及び公欠・欠席者
            } catch( Exception ex ) {
                log.warn("DB2 open error!", ex);
            }
            //SVF出力
            final boolean hischoolFlg = "H".equals(_SCHOOLKIND);
            if (setSvfMain(db2, svf, param, ps1, hischoolFlg)) nonedata = true;   //帳票出力のメソッド
        } else {
            
            //SQL作成
            try {
                ps1 = db2.prepareStatement(Pre_Stat1(param, "01", "03"));       //生徒及び公欠・欠席者
                ps2 = db2.prepareStatement(Pre_Stat1(param, "04", "06"));       //生徒及び公欠・欠席者
            } catch( Exception ex ) {
                log.warn("DB2 open error!", ex);
            }
            //SVF出力
            final boolean hischoolFlg = getJh(db2, param);
            if (setSvfMain(db2, svf, param, ps1, false)) nonedata = true;   //帳票出力のメソッド
            if (hischoolFlg) {
                if (setSvfMain(db2, svf, param, ps2, hischoolFlg)) nonedata = true; //帳票出力のメソッド
            }
        }
        
	//	該当データ無し
		if( !nonedata ){
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "note");
			svf.VrEndPage();
		}

	// 	終了処理
		svf.VrQuit();
		DbUtils.closeQuietly(ps1);
        DbUtils.closeQuietly(ps2);
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる

    }//doGetの括り

    private void setGradeMaxMin(
            final DB2UDB db2,
            final String[] param
        ) {
        String sql = "SELECT MIN(GRADE) AS MIN, MAX(GRADE) AS MAX FROM SCHREG_REGD_GDAT WHERE YEAR = '" + param[0] + "' AND SCHOOL_KIND = '" + _SCHOOLKIND + "' ";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                _maxGrade = rs.getString("MAX");
                _minGrade = rs.getString("MIN");
            }
        } catch (final Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /** 中高一貫はTrue */
    private boolean getJh(
        final DB2UDB db2,
        final String[] param
    ) {
        boolean rtnflg = false;
        String jhsql = "SELECT COUNT(*) AS CNT FROM SCHREG_REGD_DAT WHERE YEAR = '"+param[0]+"' AND SEMESTER = '"+param[1]+"' AND GRADE > '03' ";
        try {
            db2.query(jhsql);
            ResultSet rs = db2.getResultSet();
            while (rs.next()) {
                if (0 < rs.getInt("CNT")) {
                    rtnflg = true;
                }
            }
        } catch (final Exception e) {
            db2.commit();
        }
        return rtnflg;
    }


	/** SVF-FORM **/
	private void Set_Head(
            DB2UDB db2,
            Vrw32alp svf,
            String param[]
    ){

		//KNJ_EditDate editdate = new KNJ_EditDate();		//和暦変換取得クラスのインスタンス作成
		svf.VrSetForm("KNJH130.frm", 1);				//共通フォーム
		param[2] = KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度";
		//ret = svf.VrsOut("nendo"			,String.valueOf(param[2]));
		/*作成日取得*/
		try {
			KNJ_Control date = new KNJ_Control();								//取得クラスのインスタンス作成
			KNJ_Control.ReturnVal returnval = date.Control(db2);
			param[3] = returnval.val3;											//作成日
		} catch( Exception e ){
			log.error("[KNJH130KenMeibo]DB2 CONTROL_MST query error!", e );
		}
        param[6] = (param[4].equals("1"))? "保護者住所": (param[4].equals("2"))? "負担者住所": "生徒住所";
		//ret = svf.VrsOut("DATE"			,editdate.h_format_JP(param[3]));

	}//Set_Head()の括り


	/**
     *  svf print 集計＆印刷処理
     */
	private boolean setSvfMain(
            DB2UDB db2,
            Vrw32alp svf,
            String param[],
            PreparedStatement ps1,
            boolean hischoolFlg
	) {
		boolean nonedata = false;
        Map maparea = new HashMap();  //地域マップ
        int arrarea[][][] = null;     //地域別集計用

		try {
            setAreaMap( db2, maparea );     //地域マップ作成
            arrarea = new int[ maparea.size() + 1 ][3][2];
            calcMain( db2, ps1, maparea, arrarea );    //集計処理
            nonedata = printMain( svf, param, maparea, arrarea, hischoolFlg );   //印刷処理
		} catch( Exception ex ) {
			log.error("setSvfMain set error!", ex );
		}
		return nonedata;
	}


	/**
     *  地域マップ作成
     */
	private void setAreaMap( DB2UDB db2, Map maparea )
    {
		try{
            int i = 0;
			maparea.put( new Integer( ++i ), new String("大阪市")     );  //1
			maparea.put( new Integer( ++i ), new String("堺市")       );  //2
			maparea.put( new Integer( ++i ), new String("岸和田市")   );  //3
			maparea.put( new Integer( ++i ), new String("豊中市")     );  //4
			maparea.put( new Integer( ++i ), new String("池田市")     );  //6
			maparea.put( new Integer( ++i ), new String("吹田市")     );  //6
			maparea.put( new Integer( ++i ), new String("泉大津市")   );  //7
			maparea.put( new Integer( ++i ), new String("高槻市")     );  //8
			maparea.put( new Integer( ++i ), new String("貝塚市")     );  //9
			maparea.put( new Integer( ++i ), new String("守口市")     );  //10
			maparea.put( new Integer( ++i ), new String("枚方市")     );  //11
			maparea.put( new Integer( ++i ), new String("茨木市")     );  //12
			maparea.put( new Integer( ++i ), new String("八尾市")     );  //13
			maparea.put( new Integer( ++i ), new String("泉佐野市")   );  //14
			maparea.put( new Integer( ++i ), new String("富田林市")   );  //15
			maparea.put( new Integer( ++i ), new String("寝屋川市")   );  //16
			maparea.put( new Integer( ++i ), new String("河内長野市") );  //17
			maparea.put( new Integer( ++i ), new String("松原市")     );  //18
			maparea.put( new Integer( ++i ), new String("大東市")     );  //19
			maparea.put( new Integer( ++i ), new String("和泉市")     );  //20
			maparea.put( new Integer( ++i ), new String("箕面市")     );  //21
			maparea.put( new Integer( ++i ), new String("柏原市")     );  //22
			maparea.put( new Integer( ++i ), new String("羽曳野市")   );  //23
			maparea.put( new Integer( ++i ), new String("門真市")     );  //24
			maparea.put( new Integer( ++i ), new String("摂津市")     );  //25
			maparea.put( new Integer( ++i ), new String("高石市")     );  //26
			maparea.put( new Integer( ++i ), new String("藤井寺市")   );  //27
			maparea.put( new Integer( ++i ), new String("東大阪市")   );  //28
			maparea.put( new Integer( ++i ), new String("泉南市")     );  //29
			maparea.put( new Integer( ++i ), new String("四條畷市")   );  //30
			maparea.put( new Integer( ++i ), new String("交野市")     );  //31
			maparea.put( new Integer( ++i ), new String("大阪狭山市") );  //32
			maparea.put( new Integer( ++i ), new String("阪南市")     );  //33
			maparea.put( new Integer( ++i ), new String("大阪市以外") );  //34
			maparea.put( new Integer( ++i ), new String("大阪都市部") );  //35
			maparea.put( new Integer( ++i ), new String("島本町")     );  //36
			maparea.put( new Integer( ++i ), new String("豊能町")     );  //37
			maparea.put( new Integer( ++i ), new String("能勢町")     );  //38
			maparea.put( new Integer( ++i ), new String("忠岡町")     );  //39
			maparea.put( new Integer( ++i ), new String("熊取町")     );  //40
			maparea.put( new Integer( ++i ), new String("田尻町")     );  //41
			maparea.put( new Integer( ++i ), new String("岬町")       );  //42
			maparea.put( new Integer( ++i ), new String("太子町")     );  //43
			maparea.put( new Integer( ++i ), new String("河南町")     );  //44

			maparea.put( new Integer( ++i ), new String("美原町")     );  //05/12/13 Build  45

			maparea.put( new Integer( ++i ), new String("千早赤阪村") );  //46
			maparea.put( new Integer( ++i ), new String("大阪郡部")   );  //47
			maparea.put( new Integer( ++i ), new String("大阪府")     );  //48
			maparea.put( new Integer( ++i ), new String("奈良市")     );  //49
			maparea.put( new Integer( ++i ), new String("大和高田市") );  //50
			maparea.put( new Integer( ++i ), new String("大和郡山市") );  //51
			maparea.put( new Integer( ++i ), new String("天理市")     );  //52
			maparea.put( new Integer( ++i ), new String("橿原市")     );  //53
			maparea.put( new Integer( ++i ), new String("桜井市")     );  //54
			maparea.put( new Integer( ++i ), new String("五條市")     );  //55
			maparea.put( new Integer( ++i ), new String("御所市")     );  //56
			maparea.put( new Integer( ++i ), new String("生駒市")     );  //57
			maparea.put( new Integer( ++i ), new String("香芝市")     );  //58
			maparea.put( new Integer( ++i ), new String("葛城市")     );  //59
			maparea.put( new Integer( ++i ), new String("他の奈良県") );  //60
			maparea.put( new Integer( ++i ), new String("奈良県")     );  //61
			maparea.put( new Integer( ++i ), new String("和歌山県")   );  //62
			maparea.put( new Integer( ++i ), new String("兵庫県")     );  //63
			maparea.put( new Integer( ++i ), new String("京都府")     );  //64
			maparea.put( new Integer( ++i ), new String("滋賀県")     );  //65
			maparea.put( new Integer( ++i ), new String("三重県")     );  //66
			maparea.put( new Integer( ++i ), new String("他府県")     );  //67
		} catch( Exception ex ){
			log.error("setAreaMap error!", ex );
		}
/*
		try{
			String sql = null;
			sql = "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'H203' ORDER BY NAMECD2 ";
			db2.query(sql);
			rs = db2.getResultSet();
			while ( rs.next() ){
				maparea.put( new Integer( rs.getInt("NAMECD2") ), rs.getString("NAME1") );
			}
			db2.commit();
			rs.close();
		} catch( Exception ex ){
			log.error("setAreaMap error!", ex );
		}
*/
	}


	/**
     *  集計処理
     */
	private void calcMain( DB2UDB db2, PreparedStatement ps1, Map maparea, int arrarea[][][] )
    {
        ResultSet rs = null;
		try {
			rs = ps1.executeQuery();
			while( rs.next() ){
                calcMeisai( rs, maparea, arrarea );  //集計明細処理
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.error("cakcMain error!", ex );
		}
	}


	/**
     *  svf print 印刷処理
     */
	private boolean printMain(
            Vrw32alp svf,
            String param[],
            Map maparea,
            int arrarea[][][],
            boolean hischoolFlg
    ) {
		boolean nonedata = false;

		try {
            int i = 0;
            int j = 0;
            printsvfHead( svf, param, hischoolFlg );
            for( i = 0; i < arrarea.length - 1 ; i++ ){
                j += getJump( maparea, i );
                if( 47 < j ){
                    if( 0 < i ) svf.VrEndPage();
                    printsvfHead( svf, param, hischoolFlg );
                    j = 1;
                }
                printsvfMeisai( svf, maparea, arrarea, i, j );
			}
            if( 0 < i ){
                printsvfTotal( svf, arrarea );
                nonedata = true;
                svf.VrEndPage();
            }
		} catch( Exception ex ) {
			log.error("setSvfMain set error!", ex );
		}
		return nonedata;
	}


	/**
     *  集計明細処理
     */
	private void calcMeisai( ResultSet rs, Map maparea, int arrarea[][][] ){
		try {
            int i = Integer.parseInt( rs.getString("GRADE") );
            if (i > 3) {
                i = i - 3;
            }
            int j = Integer.parseInt( rs.getString("SEX") );
            if (!NumberUtils.isDigits(rs.getString("KENCD"))) {
                log.warn(" not number: " + rs.getString("KENCD"));
                return;
            }
            int kencd = Integer.parseInt( rs.getString("KENCD") );
            String str = null;

            if( kencd == 26 ){   //大阪府
                str = rs.getString("ADDR1");
                if( -1 < str.indexOf( (String)maparea.get( new Integer( 28 ) ) ) ) //05/12/13 Modify 東大阪市を先に取り出す
                    calcDetail( arrarea, i, j, 28 );
                else
                //for( int k = 1; k <= 47; k++ ){
                for( int k = 1; k <= 48; k++ ){                     //05/12/13 Modify
                    if( -1 < str.indexOf( (String)maparea.get( new Integer( k ) ) ) ){
                        calcDetail( arrarea, i, j, k );
                        break;
                    }
                }
            } else if( kencd == 28 ){  //奈良県
                str = rs.getString("ADDR1");
                boolean boo = false;
                //for( int k = 48; k <= 58; k++ ){
                for( int k = 49; k <= 59; k++ ){                     //05/12/13 Modify
                    if( -1 < str.indexOf( (String)maparea.get( new Integer( k ) ) ) ){
                        calcDetail( arrarea, i, j, k );
                        boo = true;
                        break;
                    }
                }
                //if( ! boo )calcDetail( arrarea, i, j, 59 );  //その他の奈良県
                if( ! boo )calcDetail( arrarea, i, j, 60 );  //その他の奈良県 05/12/16 Modify
            } else{  //都道府県
                str = rs.getString("KENNAME");
                boolean boo = false;
                //for( int k = 61; k <= 65; k++ ){
                for( int k = 62; k <= 66; k++ ){                     //05/12/13 Modify
                    if( -1 < str.indexOf( (String)maparea.get( new Integer( k ) ) ) ){
                        calcDetail( arrarea, i, j, k );
                        boo = true;
                        break;
                    }
                }
                //if( ! boo )calcDetail( arrarea, i, j, 66 );  //他府県
                if( ! boo )calcDetail( arrarea, i, j, 67 );  //他府県 05/12/16 Modify
            }
		} catch( Exception e ){
            log.error("calcMeisai error!", e );
		}
	}


	/**
     *  集計明細処理 詳細
     */
	private void calcDetail( int arrarea[][][], int i, int j, int k ){

		try {
            if( k <= arrarea.length  &&  i <= arrarea[k-1].length  &&  j <= arrarea[k-1][i-1].length ){
                arrarea[k-1][i-1][j-1]++;
                if( 1 == k  )arrarea[ 35 - 1 ][i-1][j-1]++;  //大阪都市部
                if( 2  <= k  &&  k <= 33 )arrarea[ 34 - 1 ][i-1][j-1]++;  //大阪市以外
                /* ***  05/12/13 Delete
                if( 36 <= k  &&  k <= 45 )arrarea[ 46 - 1 ][i-1][j-1]++;  //大阪郡部
                if( 48 <= k  &&  k <= 59 )arrarea[ 60 - 1 ][i-1][j-1]++;  //奈良県
                if( 1  <= k  &&  k <= 45 )arrarea[ 47 - 1 ][i-1][j-1]++;  //大阪府
                *** */
                if( 36 <= k  &&  k <= 46 )arrarea[ 47 - 1 ][i-1][j-1]++;  //大阪郡部  05/12/13 Modify
                if( 49 <= k  &&  k <= 60 )arrarea[ 61 - 1 ][i-1][j-1]++;  //奈良県    05/12/13 Modify
                if( 1  <= k  &&  k <= 46 )arrarea[ 48 - 1 ][i-1][j-1]++;  //大阪府    05/12/13 Modify
                arrarea[ arrarea.length - 1 ][i-1][j-1]++;  //計
            }
		} catch( Exception e ){
			log.error("calcDetail error!", e );
		}
	}


	/**
     *  SVF-FORM ページ見出し印刷
     */
	private void printsvfHead(
            Vrw32alp svf,
            String param[],
            boolean hischoolFlg
    ) {

		try {
    		svf.VrsOut("nendo",    KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度" );
    		svf.VrsOut("DATE",     KNJ_EditDate.h_format_JP(param[3]) );
            svf.VrsOut("AREA_DIV", "地域");
            svf.VrsOut("DEF_DIV",  param[6] );
            if (hischoolFlg) {
                svf.VrsOut("GRADE1", "４");
                svf.VrsOut("GRADE2", "５");
                svf.VrsOut("GRADE3", "６");
            } else {
                svf.VrsOut("GRADE1", "１");
                svf.VrsOut("GRADE2", "２");
                svf.VrsOut("GRADE3", "３");
            }
		} catch( Exception e ){
			log.error("printsvfHead error!", e );
		}
	}


	/**
     *  SVF-FORM 明細データ印刷
     */
	private void printsvfMeisai( Vrw32alp svf, Map maparea, int arrarea[][][], int i, int k )
    {

		try {
            int kei[] = {0,0,0};
            for( int j = 0; j < arrarea[i].length; j++ ){
                svf.VrsOutn( "man"    + ( j+1 ), k, String.valueOf( arrarea[i][j][0] ) );	 //男子
                svf.VrsOutn( "woman"  + ( j+1 ), k, String.valueOf( arrarea[i][j][1] ) );	 //女子
                svf.VrsOutn( "syokei" + ( j+1 ), k, String.valueOf( arrarea[i][j][0] + arrarea[i][j][1] ) );  //計
                kei[0] += arrarea[i][j][0];
                kei[1] += arrarea[i][j][1];
                kei[2] += ( arrarea[i][j][0] + arrarea[i][j][1] );
            }
            svf.VrsOutn( "total_man",    k, String.valueOf( kei[0] ) );  //男子
            svf.VrsOutn( "total_woman",  k, String.valueOf( kei[1] ) );  //女子
            svf.VrsOutn( "total_syokei", k, String.valueOf( kei[2] ) );  //計

            svf.VrsOutn( "area", k, (String)maparea.get( new Integer( i + 1 ) ) );  //地域名

		} catch( Exception e ){
			log.error("printsvfMeisai error!", e );
		}
	}


	/**
     *  SVF-FORM 合計印刷
     */
	private void printsvfTotal( Vrw32alp svf, int arrarea[][][] )
    {
        int i = arrarea.length - 1;
		try {
            int kei[] = {0,0,0};
            for( int j = 0; j < arrarea[i].length; j++ ){
                svf.VrsOut( "man"    + ( j+1 ) + "kei", String.valueOf( arrarea[i][j][0] ) );	 //男子
                svf.VrsOut( "woman"  + ( j+1 ) + "kei", String.valueOf( arrarea[i][j][1] ) );	 //女子
                svf.VrsOut( "total"  + ( j+1 ) + "kei", String.valueOf( arrarea[i][j][0] + arrarea[i][j][1] ) );  //計
                kei[0] += arrarea[i][j][0];
                kei[1] += arrarea[i][j][1];
                kei[2] += ( arrarea[i][j][0] + arrarea[i][j][1] );
            }
            svf.VrsOut( "allman",    String.valueOf( kei[0] ) );  //男子
            svf.VrsOut( "allwoman",  String.valueOf( kei[1] ) );  //女子
            svf.VrsOut( "totalall",  String.valueOf( kei[2] ) );  //計

		} catch( Exception e ){
			log.error("printsvfTotal error!", e );
		}
	}


	/**
     *  SVF-FORM 合計行チェック
     */
	private int getJump( Map maparea, int i )
    {
        int ret = 1;
		try {
            String str = (String)maparea.get( new Integer( i + 1 ) );
            if( 2 < str.length() ){
                if( str.substring( str.length() - 2, str.length() ).equals("以外") )ret = 2;
                else
                if( str.substring( str.length() - 2, str.length() ).equals("郡部") )ret = 2;
                else
                if( str.equals("他の奈良県") )ret = 1;
                else
                if( str.substring( str.length() - 1, str.length() ).equals("県") )ret = 2;
                else
                if( str.substring( str.length() - 1, str.length() ).equals("府") )ret = 2;
                else
                {
                    if( 0 < i ){
                        str = (String)maparea.get( new Integer( i ) );
                        if( str.substring( str.length() - 2, str.length() ).equals("市部") )ret = 2;
                        else
                        if( str.equals("大阪府") )ret = 2;
                    }
                }
            }
		} catch( Exception e ){
			log.error("printsvfMeisai error!", e );
		}
        return ret;
	}


	/**PrepareStatement作成**/
	private String Pre_Stat1(
            String param[],
            String sGrade,
            String eGrade){

	//	生徒及び公欠・欠席者データ
		StringBuffer stb = new StringBuffer();
		try {
			String fieldZip = "";
            String fieldAdr = "";
            String tableName = "GUARDIAN_DAT";
            if (param[4].equals("1")){
                fieldZip = "GUARD_ZIPCD";
                fieldAdr = "GUARD_ADDR1";
			} else if (param[4].equals("2")) {
                fieldZip = "GUARANTOR_ZIPCD";
                fieldAdr = "GUARANTOR_ADDR1";
            } else {
                fieldZip = "ZIPCD";
                fieldAdr = "ADDR1";
                tableName = makeStudentSql(param);
            }
		    /* 2005/02/18Modify yamasihro 異動者を除外 */
			stb.append("with ziptable as ( ");
			stb.append("SELECT ");
	        stb.append("    " + fieldZip + ", ");
	        stb.append("    case when L1.PREF = '北海道' then '00北海道' ");
            stb.append("         when L1.PREF = '青森県' then '01青森県' ");
            stb.append("         when L1.PREF = '岩手県' then '02岩手県' ");
            stb.append("         when L1.PREF = '宮城県' then '03宮城県' ");
            stb.append("         when L1.PREF = '秋田県' then '04秋田県' ");
            stb.append("         when L1.PREF = '山形県' then '05山形県' ");
            stb.append("         when L1.PREF = '福島県' then '06福島県' ");
            stb.append("         when L1.PREF = '茨城県' then '07茨城県' ");
            stb.append("         when L1.PREF = '栃木県' then '08栃木県' ");
            stb.append("         when L1.PREF = '群馬県' then '09群馬県' ");
            stb.append("         when L1.PREF = '埼玉県' then '10埼玉県' ");
            stb.append("         when L1.PREF = '千葉県' then '11千葉県' ");
            stb.append("         when L1.PREF = '東京都' then '12東京都' ");
            stb.append("         when L1.PREF = '神奈川県' then '13神奈川県' ");
            stb.append("         when L1.PREF = '新潟県' then '14新潟県' ");
            stb.append("         when L1.PREF = '富山県' then '15富山県' ");
            stb.append("         when L1.PREF = '石川県' then '16石川県' ");
            stb.append("         when L1.PREF = '福井県' then '17福井県' ");
            stb.append("         when L1.PREF = '山梨県' then '18山梨県' ");
            stb.append("         when L1.PREF = '長野県' then '19長野県' ");
            stb.append("         when L1.PREF = '岐阜県' then '20岐阜県' ");
            stb.append("         when L1.PREF = '静岡県' then '21静岡県' ");
            stb.append("         when L1.PREF = '愛知県' then '22愛知県' ");
            stb.append("         when L1.PREF = '三重県' then '23三重県' ");
            stb.append("         when L1.PREF = '滋賀県' then '24滋賀県' ");
            stb.append("         when L1.PREF = '京都府' then '25京都府' ");
            stb.append("         when L1.PREF = '大阪府' then '26大阪府' ");
            stb.append("         when L1.PREF = '兵庫県' then '27兵庫県' ");
            stb.append("         when L1.PREF = '奈良県' then '28奈良県' ");
            stb.append("         when L1.PREF = '和歌山県' then '29和歌山県' ");
            stb.append("         when L1.PREF = '鳥取県' then '30鳥取県' ");
            stb.append("         when L1.PREF = '島根県' then '31島根県' ");
            stb.append("         when L1.PREF = '岡山県' then '32岡山県' ");
            stb.append("         when L1.PREF = '広島県' then '33広島県' ");
            stb.append("         when L1.PREF = '山口県' then '34山口県' ");
            stb.append("         when L1.PREF = '徳島県' then '35徳島県' ");
            stb.append("         when L1.PREF = '香川県' then '36香川県' ");
            stb.append("         when L1.PREF = '愛媛県' then '37愛媛県' ");
            stb.append("         when L1.PREF = '高知県' then '38高知県' ");
            stb.append("         when L1.PREF = '福岡県' then '39福岡県' ");
            stb.append("         when L1.PREF = '佐賀県' then '40佐賀県' ");
            stb.append("         when L1.PREF = '長崎県' then '41長崎県' ");
            stb.append("         when L1.PREF = '熊本県' then '42熊本県' ");
            stb.append("         when L1.PREF = '大分県' then '43大分県' ");
            stb.append("         when L1.PREF = '宮崎県' then '44宮崎県' ");
            stb.append("         when L1.PREF = '鹿児島県' then '45鹿児島県' ");
            stb.append("         when L1.PREF = '沖縄県' then '46沖縄県' ELSE NULL END KENCD ");
			stb.append("FROM ");
			stb.append("    " + tableName + " t1 ");
            stb.append("    LEFT JOIN ZIPCD_MST L1 ON t1." + fieldZip + " = L1.NEW_ZIPCD ");
            stb.append("GROUP BY ");
            stb.append("    " + fieldZip + ", ");
            stb.append("    L1.PREF ");
			stb.append(") ");

	        stb.append("SELECT ");
	        stb.append("    SUBSTR(T4.KENCD,1,2) AS KENCD, ");
            stb.append("    SUBSTR(T4.KENCD,3) AS KENNAME, ");
	        stb.append("    T1.GRADE, ");
	        stb.append("    T3.SEX, ");
	        stb.append("    T2." + fieldAdr + " AS ADDR1 ");
	        stb.append("FROM ");
	        stb.append("    SCHREG_REGD_DAT t1, ");
	        stb.append("    " + tableName + " t2, ");
	        stb.append("    SCHREG_BASE_MST t3, ");
	        stb.append("    ziptable t4, ");
	        stb.append("    SCHREG_REGD_HDAT T5 ");
            if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append(" ,SCHREG_REGD_GDAT T6 ");
            }
	        stb.append("WHERE ");
	        stb.append("    t1.YEAR = '"+param[0]+"' AND ");
	        stb.append("    t1.SEMESTER = '"+param[1]+"' AND ");
            stb.append("    t1.GRADE BETWEEN '" + sGrade + "' AND '" + eGrade + "' AND ");
			stb.append("    t1.SCHREGNO = t2.SCHREGNO AND ");
            stb.append("    t1.SCHREGNO = t3.SCHREGNO AND ");
			stb.append("    t2." + fieldZip + " IS NOT NULL AND ");
			stb.append("    t2." + fieldZip + " = t4." + fieldZip + " AND ");
	        stb.append("    T5.YEAR = '"+param[0]+"' AND ");
	        stb.append("    T5.SEMESTER = '"+param[1]+"' AND ");
	        stb.append("    T5.GRADE = T1.GRADE AND ");
	        stb.append("    T5.HR_CLASS = T1.HR_CLASS ");
            if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append(" AND T6.YEAR = T1.YEAR AND T6.GRADE = T1.GRADE AND T6.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
			stb.append("ORDER BY ");
	        stb.append(    "CASE SUBSTR(T4.KENCD,1,2) WHEN '26' THEN '1' WHEN '28' THEN '2' ELSE '3'END, ");
            stb.append(    "T4.KENCD, ");
	        stb.append(    "T2." + fieldAdr );
//log.debug(stb);
		} catch( Exception e ){
			log.warn("Pre_Stat1 error!" + e );
		}
		return stb.toString();

	}//Pre_Stat1()の括り

    /** 生徒用SQL */
    private String makeStudentSql(final String[] param) {
        final StringBuffer stb = new StringBuffer();
        stb.append("(SELECT ");
        stb.append("    T1.* ");
        stb.append("FROM ");
        stb.append("    SCHREG_ADDRESS_DAT T1, ");
        stb.append("    (SELECT ");
        stb.append("        SCHREGNO, ");
        stb.append("        MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("    FROM ");
        stb.append("        SCHREG_ADDRESS_DAT ");
        stb.append("    WHERE ");
        stb.append("        SCHREGNO IN (SELECT ");
        stb.append("                        SCHREGNO ");
        stb.append("                    FROM ");
        stb.append("                        SCHREG_REGD_DAT ");
        stb.append("                    WHERE ");
        stb.append("                        YEAR = '" + param[0] + "' ");
        stb.append("                        AND SEMESTER = '" + param[1] + "' ");
        stb.append("                    ) ");
        stb.append("    GROUP BY ");
        stb.append("        SCHREGNO ");
        stb.append("    ) T2 ");
        stb.append("WHERE ");
        stb.append("    T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("    AND T1.ISSUEDATE = T2.ISSUEDATE) ");

        return stb.toString();
    }

	/**PrepareStatement close**/
	private void Pre_Stat_f(PreparedStatement ps1)
	{
	    if (null == ps1) {
	        return;
	    }
		try {
			ps1.close();
		} catch( Exception e ){
			log.warn("Pre_Stat_f error!");
		}
	}//Pre_Stat_f()の括り

}//クラスの括り
