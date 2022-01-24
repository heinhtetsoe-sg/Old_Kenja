// kanji=漢字
/*
 * $Id: 7c698481823e8d2b6a3287d58b04d4ae9f9bb06c $
 *
 * 作成日: 2005/04/13
 * 作成者: m-yama
 *
 * Copyright(C) 2005-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;
import java.io.OutputStream;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
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
 *					＜ＫＮＪＨ１３０＞  都道府県別人数一覧
 *
 *  2005/04/13 m-yama 新規作成
 *  2005/11/29 yamashiro タイトルの項目追加
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJH130KenItiran {

    private static final Log log = LogFactory.getLog(KNJH130KenItiran.class);

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
		String param[]  = new String[11];		//05/02/18Modify yamashiro

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         						//年度
			param[1] = request.getParameter("GAKKI");   							//学期
			param[4] = request.getParameter("OUTPUTA");   							//出力対象
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
		} catch( Exception ex ) {
			log.warn("parameter error!");
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
			log.error("DB2 open error!");
			return;
		}

	//	ＳＶＦ作成処理
		PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
		boolean nonedata = false; 								//該当データなしフラグ
        if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
            setGradeMaxMin(db2, param);

            //SQL作成
            try {
                ps1 = db2.prepareStatement(Pre_Stat1(param, _minGrade, _maxGrade));       //生徒及び公欠・欠席者
            } catch( Exception ex ) {
                log.warn("DB2 open error!");
            }
            //SVF出力
            final boolean hischoolFlg = "H".equals(_SCHOOLKIND);
            Set_Head(db2, svf, param, hischoolFlg);                                //見出し出力のメソッド
            if (setSvfMain(db2, svf, param, ps1)) nonedata = true;  //帳票出力のメソッド
        } else {
            //SQL作成
            try {
                ps1 = db2.prepareStatement(Pre_Stat1(param, "01", "03"));       //生徒及び公欠・欠席者
                ps2 = db2.prepareStatement(Pre_Stat1(param, "04", "06"));       //生徒及び公欠・欠席者
            } catch( Exception ex ) {
                log.warn("DB2 open error!");
            }
            //SVF出力
            final boolean hischoolFlg = getJh(db2, param);
            Set_Head(db2, svf, param, false);                                //見出し出力のメソッド
            if (setSvfMain(db2, svf, param, ps1)) nonedata = true;  //帳票出力のメソッド
            if (hischoolFlg) {
                Set_Head(db2, svf, param, hischoolFlg);                                //見出し出力のメソッド
                if (setSvfMain(db2, svf, param, ps2)) nonedata = true; //帳票出力のメソッド
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
		Pre_Stat_f(ps1);	//preparestatementを閉じる
        Pre_Stat_f(ps2);    //preparestatementを閉じる
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
            String param[],
            boolean hischoolFlg
     ){

		svf.VrSetForm("KNJH130.frm", 1);				//共通フォーム
		param[2] = KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度";
		svf.VrsOut("nendo"			,String.valueOf(param[2]));
        if (hischoolFlg) {
            svf.VrsOut("GRADE1", "４");
            svf.VrsOut("GRADE2", "５");
            svf.VrsOut("GRADE3", "６");
        } else {
            svf.VrsOut("GRADE1", "１");
            svf.VrsOut("GRADE2", "２");
            svf.VrsOut("GRADE3", "３");
        }
		/*作成日取得*/
		try {
			KNJ_Control date = new KNJ_Control();								//取得クラスのインスタンス作成
			KNJ_Control.ReturnVal returnval = date.Control(db2);
			param[3] = returnval.val3;											//作成日
		} catch( Exception e ){
			System.out.println("[KNJH130KenItiran]DB2 CONTROL_MST query error!");
			System.out.println( e );
		}
		svf.VrsOut("DATE"			,KNJ_EditDate.h_format_JP(param[3]));

        //05/11/29 Build yamashiro タイトルの項目追加
		try {
            svf.VrsOut("AREA_DIV", "都道府県");
            svf.VrsOut("DEF_DIV",  (param[4].equals("1"))? "保護者住所": (param[4].equals("2"))? "負担者住所": "生徒住所" );
log.debug("param[4]="+param[4]);
		} catch( Exception e ){
			System.out.println("[KNJH130KenItiran]DB2 CONTROL_MST query error!");
			System.out.println( e );
		}


	}//Set_Head()の括り

	/**
     *  svf print 印刷処理
     */
	private boolean setSvfMain(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		PreparedStatement ps1
	) {
		boolean nonedata = false;
		int gyo     = 48;			//行数カウント用
		int man1kei   = 0;				//1年男子計
		int woman1kei = 0;				//1年女子計
		int manwoman1 = 0;				//1年男女計
		int man2kei   = 0;				//2年男子計
		int woman2kei = 0;				//2年女子計
		int manwoman2 = 0;				//2年男女計
		int man3kei   = 0;				//3年男子計
		int woman3kei = 0;				//3年女子計
		int manwoman3 = 0;				//3年男女計
		int kenman    = 0;				//男子計
		int kenwoman  = 0;				//女子計
		int kenkei    = 0;				//県毎計
		int allkenman   = 0;			//全男子計
		int allkenwoman = 0;			//全女子計
		int allkenkei   = 0;			//全県毎計
		int allmanwoman1 = 0;			//全1年男女計
		int allmanwoman2 = 0;			//全2年男女計
		int allmanwoman3 = 0;			//全3年男女計
		String bfdata  ;				//前回コード
		String afdata  ;				//現在コード
		bfdata  = String.valueOf("00");
		afdata  = String.valueOf("00");
		try {
			ResultSet rs = ps1.executeQuery();
			while( rs.next() ){
				bfdata = String.valueOf(afdata);
				afdata = rs.getString("KENCD").substring(0,2);
				//都道府県が変われば、次の行
//log.debug("df"+bfdata);
//log.debug("af"+afdata);
				if (!bfdata.equalsIgnoreCase(afdata)) {
					gyo++;
					kenkei    = 0;
					kenman    = 0;
					kenwoman  = 0;
					manwoman1 = 0;
					manwoman2 = 0;
					manwoman3 = 0;
				}
				if (gyo > 47 ){
					gyo = 1;
				}
				//データセット
				svf.VrsOutn("area"			,gyo,rs.getString("KENCD").substring(2));	//都道府県名
				if ((rs.getString("GRADE").equals("01") && rs.getString("SEX").equals("1")) ||
                        (rs.getString("GRADE").equals("04") && rs.getString("SEX").equals("1"))){
					svf.VrsOutn("man1"			,gyo,rs.getString("ZIPCNT"));			//1年男子
					man1kei = man1kei + Integer.parseInt(rs.getString("ZIPCNT"));
					kenman = kenman + Integer.parseInt(rs.getString("ZIPCNT"));
					kenkei  = kenkei  + Integer.parseInt(rs.getString("ZIPCNT"));
					manwoman1 = manwoman1 + Integer.parseInt(rs.getString("ZIPCNT"));
					allmanwoman1 = allmanwoman1 + Integer.parseInt(rs.getString("ZIPCNT"));
					allkenman = allkenman + Integer.parseInt(rs.getString("ZIPCNT"));
					allkenkei = allkenkei  + Integer.parseInt(rs.getString("ZIPCNT"));
				}
				if ((rs.getString("GRADE").equals("01") && rs.getString("SEX").equals("2")) ||
                        (rs.getString("GRADE").equals("04") && rs.getString("SEX").equals("2"))){
					svf.VrsOutn("woman1"			,gyo,rs.getString("ZIPCNT"));			//1年女子
					woman1kei = woman1kei + Integer.parseInt(rs.getString("ZIPCNT"));
					kenwoman = kenwoman + Integer.parseInt(rs.getString("ZIPCNT"));
					kenkei  = kenkei  + Integer.parseInt(rs.getString("ZIPCNT"));
					manwoman1 = manwoman1 + Integer.parseInt(rs.getString("ZIPCNT"));
					allmanwoman1 = allmanwoman1 + Integer.parseInt(rs.getString("ZIPCNT"));
					allkenwoman = allkenwoman + Integer.parseInt(rs.getString("ZIPCNT"));
					allkenkei = allkenkei  + Integer.parseInt(rs.getString("ZIPCNT"));
				}
				if ((rs.getString("GRADE").equals("02") && rs.getString("SEX").equals("1")) ||
                        (rs.getString("GRADE").equals("05") && rs.getString("SEX").equals("1"))) {
					svf.VrsOutn("man2"			,gyo,rs.getString("ZIPCNT"));			//1年男子
					man2kei = man2kei + Integer.parseInt(rs.getString("ZIPCNT"));
					kenman = kenman + Integer.parseInt(rs.getString("ZIPCNT"));
					kenkei  = kenkei  + Integer.parseInt(rs.getString("ZIPCNT"));
					manwoman2 = manwoman2 + Integer.parseInt(rs.getString("ZIPCNT"));
					allmanwoman2 = allmanwoman2 + Integer.parseInt(rs.getString("ZIPCNT"));
					allkenman = allkenman + Integer.parseInt(rs.getString("ZIPCNT"));
					allkenkei = allkenkei  + Integer.parseInt(rs.getString("ZIPCNT"));
				}
				if ((rs.getString("GRADE").equals("02") && rs.getString("SEX").equals("2")) ||
                        (rs.getString("GRADE").equals("05") && rs.getString("SEX").equals("2"))){
					svf.VrsOutn("woman2"			,gyo,rs.getString("ZIPCNT"));			//1年女子
					woman2kei = woman2kei + Integer.parseInt(rs.getString("ZIPCNT"));
					kenwoman = kenwoman + Integer.parseInt(rs.getString("ZIPCNT"));
					kenkei  = kenkei  + Integer.parseInt(rs.getString("ZIPCNT"));
					manwoman2 = manwoman2 + Integer.parseInt(rs.getString("ZIPCNT"));
					allmanwoman2 = allmanwoman2 + Integer.parseInt(rs.getString("ZIPCNT"));
					allkenwoman = allkenwoman + Integer.parseInt(rs.getString("ZIPCNT"));
					allkenkei = allkenkei  + Integer.parseInt(rs.getString("ZIPCNT"));
				}
				if ((rs.getString("GRADE").equals("03") && rs.getString("SEX").equals("1")) ||
                        (rs.getString("GRADE").equals("06") && rs.getString("SEX").equals("1"))){
					svf.VrsOutn("man3"			,gyo,rs.getString("ZIPCNT"));			//1年男子
					man3kei = man3kei + Integer.parseInt(rs.getString("ZIPCNT"));
					kenman = kenman + Integer.parseInt(rs.getString("ZIPCNT"));
					kenkei  = kenkei  + Integer.parseInt(rs.getString("ZIPCNT"));
					manwoman3 = manwoman3 + Integer.parseInt(rs.getString("ZIPCNT"));
					allmanwoman3 = allmanwoman3 + Integer.parseInt(rs.getString("ZIPCNT"));
					allkenman = allkenman + Integer.parseInt(rs.getString("ZIPCNT"));
					allkenkei = allkenkei  + Integer.parseInt(rs.getString("ZIPCNT"));
				}
				if ((rs.getString("GRADE").equals("03") && rs.getString("SEX").equals("2")) ||
                        (rs.getString("GRADE").equals("06") && rs.getString("SEX").equals("2"))){
					svf.VrsOutn("woman3"			,gyo,rs.getString("ZIPCNT"));			//1年女子
					woman3kei = woman3kei + Integer.parseInt(rs.getString("ZIPCNT"));
					kenwoman = kenwoman + Integer.parseInt(rs.getString("ZIPCNT"));
					kenkei  = kenkei  + Integer.parseInt(rs.getString("ZIPCNT"));
					manwoman3 = manwoman3 + Integer.parseInt(rs.getString("ZIPCNT"));
					allmanwoman3 = allmanwoman3 + Integer.parseInt(rs.getString("ZIPCNT"));
					allkenwoman = allkenwoman + Integer.parseInt(rs.getString("ZIPCNT"));
					allkenkei = allkenkei  + Integer.parseInt(rs.getString("ZIPCNT"));
				}
				if (manwoman1 > 0){
					svf.VrsOutn("syokei1"		,gyo,String.valueOf(manwoman1));			//1年計
				}
				if (manwoman2 > 0){
					svf.VrsOutn("syokei2"		,gyo,String.valueOf(manwoman2));			//2年計
				}
				if (manwoman3 > 0){
					svf.VrsOutn("syokei3"		,gyo,String.valueOf(manwoman3));			//3年計
				}
				if (kenman > 0){
					svf.VrsOutn("total_man"	,gyo,String.valueOf(kenman));				//男子計
				}
				if (kenwoman > 0){
					svf.VrsOutn("total_woman"	,gyo,String.valueOf(kenwoman));				//女子計
				}
				if (kenkei > 0){
					svf.VrsOutn("total_syokei",gyo,String.valueOf(kenkei));				//県別合計
				}

				if (man1kei > 0){
					svf.VrsOut("man1kei"		,String.valueOf(man1kei));				//1年男子合計
				}
				if (woman1kei > 0){
					svf.VrsOut("woman1kei"	,String.valueOf(woman1kei));			//1年女子合計
				}
				if (allmanwoman1 > 0){
					svf.VrsOut("total1kei"	,String.valueOf(allmanwoman1));			//1年男女合計
				}
				if (man2kei > 0){
					svf.VrsOut("man2kei"		,String.valueOf(man2kei));				//2年男子合計
				}
				if (woman2kei > 0){
					svf.VrsOut("woman2kei"	,String.valueOf(woman2kei));			//2年女子合計
				}
				if (allmanwoman2 > 0){
					svf.VrsOut("total2kei"	,String.valueOf(allmanwoman2));			//2年男女合計
				}
				if (man3kei > 0){
					svf.VrsOut("man3kei"		,String.valueOf(man3kei));				//3年男子合計
				}
				if (woman3kei > 0){
					svf.VrsOut("woman3kei"	,String.valueOf(woman3kei));			//3年女子合計
				}
				if (allmanwoman3 > 0){
					svf.VrsOut("total3kei"	,String.valueOf(allmanwoman3));			//3年男女合計
				}
				if (allkenman > 0){
					svf.VrsOut("allman"		,String.valueOf(allkenman));			//全男子合計
				}
				if (allkenwoman > 0){
					svf.VrsOut("allwoman"	,String.valueOf(allkenwoman));			//全女子合計
				}
				if (allkenkei > 0){
					svf.VrsOut("totalall"	,String.valueOf(allkenkei));			//全合計
				}

				nonedata = true;
			}
			if (nonedata) svf.VrEndPage();
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.error("setSvfMain set error!");
		}
		return nonedata;
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
            String tableName = "GUARDIAN_DAT";
            if (param[4].equals("1")){
                fieldZip = "GUARD_ZIPCD";
            } else if (param[4].equals("2")) {
                fieldZip = "GUARANTOR_ZIPCD";
			} else {
                fieldZip = "ZIPCD";
                tableName = makeStudentSql(param);
            }
		    /* 2005/02/18Modify yamasihro 異動者を除外 */
			stb.append("with ziptable as ( ");
			stb.append("SELECT ");
	        stb.append("    " + fieldZip + ", ");
	        stb.append("    case when L1.PREF = '北海道' then '00北海道' ");
	        stb.append("         when L1.PREF = '青森県' then '01青森' ");
	        stb.append("         when L1.PREF = '岩手県' then '02岩手' ");
	        stb.append("         when L1.PREF = '宮城県' then '03宮城' ");
	        stb.append("         when L1.PREF = '秋田県' then '04秋田' ");
	        stb.append("         when L1.PREF = '山形県' then '05山形' ");
			stb.append("         when L1.PREF = '福島県' then '06福島' ");
			stb.append("         when L1.PREF = '茨城県' then '07茨城' ");
			stb.append("         when L1.PREF = '栃木県' then '08栃木' ");
			stb.append("         when L1.PREF = '群馬県' then '09群馬' ");
            stb.append("         when L1.PREF = '埼玉県' then '10埼玉' ");
			stb.append("         when L1.PREF = '千葉県' then '11千葉' ");
			stb.append("         when L1.PREF = '東京都' then '12東京' ");
	        stb.append("         when L1.PREF = '神奈川県' then '13神奈川' ");
	        stb.append("         when L1.PREF = '新潟県' then '14新潟' ");
	        stb.append("         when L1.PREF = '富山県' then '15富山' ");
	        stb.append("         when L1.PREF = '石川県' then '16石川' ");
	        stb.append("         when L1.PREF = '福井県' then '17福井' ");
			stb.append("         when L1.PREF = '山梨県' then '18山梨' ");
            stb.append("         when L1.PREF = '長野県' then '19長野' ");
			stb.append("         when L1.PREF = '岐阜県' then '20岐阜' ");
			stb.append("         when L1.PREF = '静岡県' then '21静岡' ");
            stb.append("         when L1.PREF = '愛知県' then '22愛知' ");
			stb.append("         when L1.PREF = '三重県' then '23三重' ");
			stb.append("         when L1.PREF = '滋賀県' then '24滋賀' ");
			stb.append("         when L1.PREF = '京都府' then '25京都' ");
			stb.append("         when L1.PREF = '大阪府' then '26大阪' ");
	        stb.append("         when L1.PREF = '兵庫県' then '27兵庫' ");
	        stb.append("         when L1.PREF = '奈良県' then '28奈良' ");
	        stb.append("         when L1.PREF = '和歌山県' then '29和歌山' ");
	        stb.append("         when L1.PREF = '鳥取県' then '30鳥取' ");
	        stb.append("         when L1.PREF = '島根県' then '31島根' ");
	        stb.append("         when L1.PREF = '岡山県' then '32岡山' ");
	        stb.append("         when L1.PREF = '広島県' then '33広島' ");
            stb.append("         when L1.PREF = '山口県' then '34山口' ");
			stb.append("         when L1.PREF = '徳島県' then '35徳島' ");
			stb.append("         when L1.PREF = '香川県' then '36香川' ");
			stb.append("         when L1.PREF = '愛媛県' then '37愛媛' ");
            stb.append("         when L1.PREF = '高知県' then '38高知' ");
            stb.append("         when L1.PREF = '福岡県' then '39福岡' ");
			stb.append("         when L1.PREF = '佐賀県' then '40佐賀' ");
			stb.append("         when L1.PREF = '長崎県' then '41長崎' ");
			stb.append("         when L1.PREF = '熊本県' then '42熊本' ");
			stb.append("         when L1.PREF = '大分県' then '43大分' ");
			stb.append("         when L1.PREF = '宮崎県' then '44宮崎' ");
			stb.append("         when L1.PREF = '鹿児島県' then '45鹿児島' else '46沖縄' end KENCD ");
			stb.append("FROM ");
			stb.append("    " + tableName + " t1 ");
            stb.append("    LEFT JOIN ZIPCD_MST L1 ON t1." + fieldZip + " = L1.NEW_ZIPCD ");
			stb.append("GROUP BY ");
            stb.append("    " + fieldZip + ", ");
            stb.append("    L1.PREF ");
			stb.append(") ");
	        stb.append("SELECT ");
	        stb.append("    t4.KENCD, ");
	        stb.append("    t1.GRADE, ");
	        stb.append("    t3.SEX, ");
	        stb.append("    count(*) as ZIPCNT ");
	        stb.append("FROM ");
	        stb.append("    SCHREG_REGD_DAT t1, ");
	        stb.append("    " + tableName + " t2, ");
	        stb.append("    SCHREG_BASE_MST t3, ");
	        stb.append("    ziptable t4 ");
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
			stb.append("    t2." + fieldZip + " = t4." + fieldZip + " ");
            if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append(" AND T6.YEAR = T1.YEAR AND T6.GRADE = T1.GRADE AND T6.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
			stb.append("GROUP BY ");
			stb.append("    t1.GRADE, ");
            stb.append("    t3.SEX, ");
            stb.append("    t4.KENCD ");
			stb.append("ORDER BY ");
			stb.append("    t4.KENCD, ");
			stb.append("    t1.GRADE, ");
			stb.append("    t3.SEX ");
//log.debug(stb);
		} catch( Exception e ){
			log.warn("Pre_Stat1 error!");
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
