// kanji=漢字
/*
 * $Id: 156012e7239ff396cbd38a96ce4d423bf6575c0b $
 *
 * 作成日: 2007/06/07
 * 作成者: m-yama
 *
 * Copyright(C) 2007-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 156012e7239ff396cbd38a96ce4d423bf6575c0b $
 */
public class KNJH131KenMeibo {

    private static final Log log = LogFactory.getLog(KNJH131KenMeibo.class);
    private String _useSchool_KindField;
    private String _SCHOOLKIND;

    /*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
        log.fatal("$Revision: 75815 $");
        KNJServletUtils.debugParam(request, log);

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[]  = new String[6];		//05/02/18Modify yamashiro

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         						//年度
			param[1] = request.getParameter("SEMESTER");   							//学期
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

		  //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        boolean nonedata = false;                               //該当データなしフラグ

		Set_Head(db2,svf,param);								//見出し出力のメソッド
		//SQL作成
		try {
			ps1 = db2.prepareStatement(Pre_Stat1(param));		//生徒及び公欠・欠席者
		} catch( Exception ex ) {
			log.warn("DB2 open error!", ex);
		}
		//SVF出力
		if (setSvfMain(db2,svf,param,ps1)) nonedata = true;	//帳票出力のメソッド
	//	該当データ無し
		if( !nonedata ){
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "note");
			svf.VrEndPage();
		}

	// 	終了処理
		svf.VrQuit();
		Pre_Stat_f(ps1);	//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り


	/** SVF-FORM **/
	private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

		//KNJ_EditDate editdate = new KNJ_EditDate();		//和暦変換取得クラスのインスタンス作成
		svf.VrSetForm("KNJH130_2.frm", 1);				//共通フォーム
		param[2] = KNJ_EditDate.gengou(db2, Integer.parseInt(param[0])) + "年度";

		param[5] = (param[4].equals("1"))? "保護者": (param[4].equals("2"))? "負担者": "生徒";
		//ret = svf.VrsOut("nendo"			,String.valueOf(param[2]));
		/*作成日取得*/
		try {
			KNJ_Control date = new KNJ_Control();								//取得クラスのインスタンス作成
			KNJ_Control.ReturnVal returnval = date.Control(db2);
			param[3] = returnval.val3;											//作成日
		} catch( Exception e ){
			log.error("[KNJH131KenMeibo]DB2 CONTROL_MST query error!", e );
		}
		//ret = svf.VrsOut("DATE"			,editdate.h_format_JP(param[3]));

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
		try {
			ResultSet rs = ps1.executeQuery();
            int i = 0;              //１ページの明細件数
            int cd = 0;             //都道府県コード
            String hclass = null;   //学年＋組
			while( rs.next() ){
				//都道府県のブレイク => 改ページ
                if( Integer.parseInt(rs.getString("KENCD")) != cd ){
                    if( 0 < cd ) svf.VrEndPage();
                    cd = Integer.parseInt(rs.getString("KENCD"));         //都道府県コードの保管
                    printsvfHead(db2, svf, param, rs.getString("KENNAME") );  //ページ見出し印刷
                    i = 0;
                    hclass = null;
                }
                //学年＋組のブレイク => 列変え
                if( hclass == null  ||  ! rs.getString("GRADEHR_CLASS").equals( hclass ) ){
                    if( 0 < i  &&  i <= 50 )i = 50;
                    else 
                    if( 0 < i  &&  i <= 100 )i = 100;
                    else
                    if( 0 < i ){
                        svf.VrEndPage();
                        printsvfHead(db2, svf, param, rs.getString("KENNAME") );  //ページ見出し印刷
                        i = 0;
                    }
                    hclass = rs.getString("GRADEHR_CLASS");     //学年＋組の保管
                }

                printsvfMeisai( svf, rs, ++i );  //明細データ印刷
			}
            if( 0 < cd ){
                nonedata = true;
                svf.VrEndPage();
            }
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.error("setSvfMain set error!", ex );
		}
		return nonedata;
	}


	/** 
     *  SVF-FORM ページ見出し印刷
     */
	private void printsvfHead(DB2UDB db2, Vrw32alp svf, String param[], String kenname ){

		try {
    		//ret = svf.VrsOut("nendo",  KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");
log.debug("param[5]="+param[5]);
    		svf.VrsOut("nendo",  param[2] + "  " + param[5]);
    		svf.VrsOut("DATE",   KNJ_EditDate.h_format_JP(db2, param[3]) );
    		svf.VrsOut("AREA",   kenname );
		} catch( Exception e ){
			log.error("printsvfHead error!", e );
		}
	}


	/** 
     *  SVF-FORM 明細データ印刷
     */
	private void printsvfMeisai( Vrw32alp svf, ResultSet rs, int i ){

		try {
            int j = ( i <= 50 )? 1: ( i <= 100 )? 2: 3;   //列番号
    		svf.VrsOutn("ATTENDNO" + j,  ( ( 0 < i % 50 )? i % 50: 50 ),  rs.getString("ATTENDNO") );
    		svf.VrsOutn("NAME"     + j,  ( ( 0 < i % 50 )? i % 50: 50 ),  rs.getString("NAME") );

            if( i % 50 == 1 ) svf.VrsOut("HR_CLASS" + j,  rs.getString("HR_NAME") );
		} catch( Exception e ){
			log.error("printsvfMeisai error!", e );
		}
	}


	/**PrepareStatement作成**/
	private String Pre_Stat1(String param[]){

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
	        stb.append("    "+fieldZip+", ");
	        stb.append("    case when substr(" + fieldZip + ",1,2) IN('00','04','05','06','07','08','09') then '00北海道' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('03') then '01青森' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('02') then '02岩手' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('98') then '03宮城' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('01') then '04秋田' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('99') then '05山形' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('96','97') then '06福島' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('30','31') then '07茨城' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('32') then '08栃木' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('37') then '09群馬' ");
            stb.append("         when substr(" + fieldZip + ",1,2) IN('33','34','35','36') then '10埼玉' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('26','27','28','29') then '11千葉' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('10','11','12','13','14','15','16','17','18','19','20') then '12東京' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('21','22','23','24','25') then '13神奈川' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('94','95') then '14新潟' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('93') then '15富山' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('92') then '16石川' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('91') then '17福井' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('40') then '18山梨' ");
            stb.append("         when substr(" + fieldZip + ",1,2) IN('38','39') then '19長野' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('50') then '20岐阜' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('41','42','43') then '21静岡' ");
            stb.append("         when substr(" + fieldZip + ",1,2) IN('44','45','46','47','48','49') then '22愛知' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('51') then '23三重' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('52') then '24滋賀' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('60','61','62') then '25京都' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('53','54','55','56','57','58','59') then '26大阪' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('65','66','67') then '27兵庫' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('63') then '28奈良' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('64') then '29和歌山' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('68') then '30鳥取' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('69') then '31島根' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('70','71') then '32岡山' ");
	        stb.append("         when substr(" + fieldZip + ",1,2) IN('72','73') then '33広島' ");
            stb.append("         when substr(" + fieldZip + ",1,2) IN('74','75') then '34山口' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('77') then '35徳島' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('76') then '36香川' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('79') then '37愛媛' ");
            stb.append("         when substr(" + fieldZip + ",1,2) IN('78') then '38高知' ");
            stb.append("         when substr(" + fieldZip + ",1,2) IN('80','81','82','83') then '39福岡' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('84') then '40佐賀' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('85') then '41長崎' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('86') then '42熊本' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('87') then '43大分' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('88') then '44宮崎' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('89') then '45鹿児島' ");
			stb.append("         when substr(" + fieldZip + ",1,2) IN('90') then '46沖縄' ELSE NULL END KENCD ");
			stb.append("FROM ");
			stb.append("    " + tableName + " t1 ");
			stb.append("GROUP BY ");
			stb.append("    " + fieldZip + " ");
			stb.append(") ");

	        stb.append("SELECT ");
	        stb.append("    SUBSTR(T4.KENCD,1,2) AS KENCD, ");
            stb.append("    SUBSTR(T4.KENCD,3) AS KENNAME, ");
	        stb.append("    T1.GRADE || T1.HR_CLASS AS GRADEHR_CLASS, ");
            stb.append("    T5.HR_NAME, ");
	        stb.append("    T1.ATTENDNO, ");
	        stb.append("    T3.NAME ");
	        stb.append("FROM ");
	        stb.append("    SCHREG_REGD_DAT t1, ");
	        stb.append("    " + tableName + " t2, ");
	        stb.append("    SCHREG_BASE_MST t3, ");
	        stb.append("    ziptable t4, ");
	        stb.append("    SCHREG_REGD_HDAT T5 ");
            if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append(" , SCHREG_REGD_GDAT GDAT ");
            }
	        stb.append("WHERE ");
	        stb.append("    t1.YEAR = '"+param[0]+"' AND ");
	        stb.append("    t1.SEMESTER = '"+param[1]+"' AND ");
			stb.append("    t1.SCHREGNO = t2.SCHREGNO AND ");
            stb.append("    t1.SCHREGNO = t3.SCHREGNO AND ");
			stb.append("    t2." + fieldZip + " IS NOT NULL AND ");
			stb.append("    t2." + fieldZip + " = t4." + fieldZip + " AND ");
	        stb.append("    T5.YEAR = '"+param[0]+"' AND ");
	        stb.append("    T5.SEMESTER = '"+param[1]+"' AND ");
	        stb.append("    T5.GRADE = T1.GRADE AND ");
	        stb.append("    T5.HR_CLASS = T1.HR_CLASS ");
            if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append(" AND GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE AND GDAT.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
			stb.append("ORDER BY ");
	        stb.append("    T4.KENCD, ");
	        stb.append("    T1.GRADE, ");
	        stb.append("    T1.HR_CLASS, ");
	        stb.append("    T1.ATTENDNO ");
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
