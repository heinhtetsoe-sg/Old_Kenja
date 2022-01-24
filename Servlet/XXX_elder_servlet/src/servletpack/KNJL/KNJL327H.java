package servletpack.KNJL;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_Get_Info;


/**
 *
 *	学校教育システム 賢者 [入試管理]
 *
 *					＜ＫＮＪＬ３２７Ｈ＞  
 *					＜１＞  合格通知書
 *					＜２＞  繰上合格候補者通知書
 *					＜３＞  入学許可通知書(中学/高校)
 *
 *	2007/11/22 RTS 作成日
 *
 **/

public class KNJL327H {

    private static final Log log = LogFactory.getLog(KNJL327H.class);

	private Map hmm = new HashMap();			//ひらがなからカタカナの変換用
	private static final char hSpace = ' ';	//全角半角混在フィードの前後空白削除用
	private static final char zSpace = '　'; //全角半角混在フィードの前後空白削除用

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[16];

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         				//年度
	        param[1] = request.getParameter("APDIV");         				//入試制度
	        param[2] = request.getParameter("TESTDV");         				//入試区分
	        param[3] = request.getParameter("NOTICEDAY");        			//通知日付

	        param[4] = request.getParameter("OUTPUT");         				//帳票種類 1,2,3

	        if (param[4].equals("1")) {
	        	param[5] = request.getParameter("OUTPUTA");        			//合格者指定
	        	param[6] = request.getParameter("EXAMNOA");        			//受験番号
	        }
	        if (param[4].equals("2")) {
	        	param[5] = request.getParameter("OUTPUTB");        			//繰上合格候補者指定
	        	param[6] = request.getParameter("EXAMNOB");        			//受験番号
	        	param[7] = request.getParameter("CONTACTDATE");        		//連絡日付
	        }
	        if (param[4].equals("3")) {
	        	param[5] = request.getParameter("OUTPUTC");        			//合格者指定
	        	param[6] = request.getParameter("EXAMNOC");        			//受験番号
	        }
            if( request.getParameter("DOCUMENTROOT") != null) {
                param[15] = request.getParameter("DOCUMENTROOT");           //DOCUMENT ROOT
            }

        } catch( Exception ex ) {
			log.error("parameter error!");
		}

	//	print設定
		PrintWriter outstrm = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");

	//	svf設定
		int ret = svf.VrInit();						   		//クラスの初期化
        if (false && 0 != ret) { ret = 0; }
		ret = svf.VrSetSpoolFileStream(response.getOutputStream());  		//PDFファイル名の設定

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
		PreparedStatement ps3 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		setHeader(db2,svf,param);
		for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
		//SQL作成
		try {
			ps1 = db2.prepareStatement(preStat1(param));		//各通知書preparestatement
			ps2 = db2.prepareStatement(preStat2(param));		//学校名、校長名取得preparestatement
			ps3 = db2.prepareStatement(preStat3(param));		//郵便番号、住所、電話番号取得preparestatement
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!");
		}
		//SVF出力
		if( setSvfout(db2,svf,param,ps1,ps2,ps3) ){				//帳票出力のメソッド
			nonedata = true;
		}

	//	該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}

	// 	終了処理
		ret = svf.VrQuit();
		preStatClose(ps1);		//preparestatementを閉じる
		preStatClose(ps2);		//preparestatementを閉じる
		preStatClose(ps3);		//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り



	/** 事前処理 **/
	private void setHeader(
		DB2UDB db2,
		Vrw32alp svf,
		String param[]
	) {
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
	//	帳票フォーム
		if (param[4].equals("1")) ret = svf.VrSetForm("KNJL327H_1.frm", 4);
		if (param[4].equals("2")) ret = svf.VrSetForm("KNJL327H_2.frm", 4);
		if (param[4].equals("3")){
			// 入試制度が中学の場合
			if(param[1].equals("1")){
				ret = svf.VrSetForm("KNJL327H_4.frm", 4);
			}
			// 入試制度が高校一般または高校推薦の場合
			if(param[1].equals("2") || param[1].equals("3")){
				ret = svf.VrSetForm("KNJL327H_3.frm", 4);
			}
		}

		try {
			//通知日付
			param[9] = fomatSakuseiDate("yyyy'年'M'月'd'日'", param[3]);
			//年度
	 		param[10] = param[0] + "年度";
	        if (param[4].equals("2")) {
	        	// 連絡日
				param[8] = fomatSakuseiDate("M'月'd'日'", param[7]);
	        }
	 		//通知年月
	        if (param[4].equals("3")) {
		 		param[10] = convZenkakuToHankaku(param[0]) + "年";
	        	String sYYYYMM = fomatSakuseiDate("yyyy'年'M'月'", param[3]);
				param[8] = convZenkakuToHankaku(sYYYYMM);
	        }

		} catch( Exception e ){
			log.error("setHeader set error!");
		}
	}



	/**帳票出力（各通知書をセット）**/
	private boolean setSvfout(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		PreparedStatement ps1,
		PreparedStatement ps2,
		PreparedStatement ps3
	) {
		boolean nonedata = false;
		String sPrincipal_Name = "";	// 校長名
		String sSchool_Name = "";		// 学校名
		String sJob_Name = "";			// 役職名
		String sSchool_zipcd = "";		// 郵便番号
		String sSchool_addr1 = "";		// 住所
		String sSchool_telno = "";		// 電話番号
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }

        String school_div = "1".equals(param[1]) ? "_J" : "_H";
        String stamp_check = getImagePath(db2, param) + "SCHOOLSTAMP"+school_div+"L.jpg";  //学校長印
        File stampFile = new File(stamp_check);
        log.debug("stamp file path="+stampFile.getAbsolutePath()+", exists="+stampFile.exists());

        try {
			ResultSet rs = ps1.executeQuery();
			ResultSet rs1 = ps2.executeQuery();
			ResultSet rs2 = ps3.executeQuery();

			// 証明書学校データより学校名・校長名・役職・電話番号を取得
            if( rs1.next() ){
            	sSchool_Name = rs1.getString("SCHOOL_NAME");
            	sPrincipal_Name = rs1.getString("PRINCIPAL_NAME");
            	sJob_Name = rs1.getString("JOB_NAME");
            	sSchool_telno = rs1.getString("REMARK1");
            }
			// 学校マスタより郵便番号・住所
            if( rs2.next() ){
            	sSchool_zipcd = "〒"+rs2.getString("SCHOOLZIPCD");
            	sSchool_addr1 = rs2.getString("SCHOOLADDR1");
            }

			while( rs.next() ){
				// 合格通知書を出力
		        if (param[4].equals("1")) {
					ret = svf.VrsOut("NAME",	rs.getString("NAME"));		// 名前
					// 受験番号
					ret = svf.VrsOut("EXAMNO",	rs.getString("EXAMNO"));
					if(param[1].equals("1")){
						ret = svf.VrsOut("SCHOOL","中学校");				// 学校名
					} else {
						ret = svf.VrsOut("SCHOOL","高等学校");				// 学校名
					}
					ret = svf.VrsOut("NENDO",param[10]);					// 年度
					ret = svf.VrsOut("DATE",param[9]);						// 通知日付
	                if( sSchool_Name != null){
	                    ret = svf.VrsOut("SCHOOL_NAME",sSchool_Name);		// 学校名
	                }
	                if( sJob_Name != null){
	                    ret = svf.VrsOut("JOB_NAME",sJob_Name);				// 役職名
	                }
	                if( sPrincipal_Name != null){
	                    ret = svf.VrsOut("PRINCIPAL_NAME",sPrincipal_Name);	// 校長名
	                }
                    if (stampFile.exists()) ret = svf.VrsOut("STAMP", stamp_check );
		        }
				// 繰上合格候補者通知書を出力
		        if (param[4].equals("2")) {
					ret = svf.VrsOut("NAME",rs.getString("NAME"));			// 名前
					// 受験番号
					ret = svf.VrsOut("EXAMNO",rs.getString("EXAMNO"));
					ret = svf.VrsOut("NENDO",param[10]);					// 年度
					if(param[1].equals("1")){
						ret = svf.VrsOut("SCHOOL","中学校");				// 学校名
					} else {
						ret = svf.VrsOut("SCHOOL","高等学校");				// 学校名
					}
					ret = svf.VrsOut("DATE1",param[8]);						// 連絡日
					ret = svf.VrsOut("DATE2",param[9]);						// 通知日付
	                if( sSchool_Name != null){
	                    ret = svf.VrsOut("SCHOOL_NAME",sSchool_Name);		// 学校名
	                }
	                if( sJob_Name != null){
	                    ret = svf.VrsOut("JOB_NAME",sJob_Name);				// 役職名
	                }
	                if( sPrincipal_Name != null){
	                    ret = svf.VrsOut("PRINCIPAL_NAME",sPrincipal_Name);	// 校長名
	                }
                    if (stampFile.exists()) ret = svf.VrsOut("STAMP", stamp_check );
		        }
				// 入学許可通知書を出力
		        if (param[4].equals("3")) {
					ret = svf.VrsOut("NAME" 		,rs.getString("NAME"));		// 名前
					// 受験番号(上段)
					ret = svf.VrsOut("EXAMNO1" 		,rs.getString("EXAMNO"));
					// 受験番号(下段)
					ret = svf.VrsOut("EXAMNO2" 		,rs.getString("EXAMNO"));
					ret = svf.VrsOut("NENDO" 		,param[10]);				// 年度
                    ret = svf.VrsOut("SCHOOLNAME" 	,Ztrim(sSchool_Name));		// 学校名
                    ret = svf.VrsOut("STAFF_NAME" 	,Ztrim(sPrincipal_Name));	// 校長名
	                if( sJob_Name != null){
	                    ret = svf.VrsOut("JOB_NAME",Ztrim(sJob_Name));			// 役職名
	                }
					ret = svf.VrsOut("DATE1"		,param[8]);					// 通知年月(上段)
					ret = svf.VrsOut("DATE2"		,param[8]);					// 通知年月(下段)
					if(nvlT(rs.getString("RITU_NAME")).length() > 0){
						ret = svf.VrsOut("RITU_NAME"	,nvlT(rs.getString("RITU_NAME")) + "立");// 立名称
					}
					String fin_school = nvlT(rs.getString("FINSCHOOL_NAME"));
					if(fin_school.length() > 0){
						ret = svf.VrsOut("FIN_SCHOOLNAME",fin_school);			// 出身学校名
					}
					ret = svf.VrsOut("ZIP" 			,sSchool_zipcd);			// 郵便番号
					ret = svf.VrsOut("SCHOOL_ADDRESS"	,sSchool_addr1);		// 住所
					ret = svf.VrsOut("TELNO" 		,sSchool_telno);			// 電話番号
                    if (stampFile.exists()) ret = svf.VrsOut("STAMP", stamp_check );
		        }

				ret = svf.VrEndRecord();//レコードを出力
				nonedata = true;
			}
			rs.close();
			rs1.close();
			rs2.close();
			db2.commit();
		} catch( Exception ex ) {
			log.error("setSvfout set error!");
		}
		return nonedata;
	}

    private String getImagePath(DB2UDB db2, String[] param) {
        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        String folderpath = null;
        try {
            returnval = getinfo.Control(db2);
            folderpath = returnval.val4;      //格納フォルダ
        } catch( Exception e ){
            log.error("setHeader set error!");
        }

        String path = param[15] + "/";
        if (folderpath != null) {
            path += folderpath + "/";
        }
        return path;
    }    

	/**各通知書を取得**/
	private String preStat1(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（なし）
		try {
			stb.append("SELECT ");
			stb.append("    T1.TESTDIV, ");
			stb.append("    T4.NAME1 AS TEST_NAME, ");
			stb.append("    T1.EXAMNO, ");
			stb.append("    T1.NAME, ");
			stb.append("    T1.NAME_KANA, ");
			stb.append("    T1.RITU_NAME, ");
			stb.append("    T1.FINSCHOOL_NAME ");
			stb.append("FROM ");
			// 入試制度が内部生の場合
			if (param[1].equals("4")){ 
				stb.append("    (SELECT W1.TESTDIV, W1.EXAMNO, W1.NAME, W1.NAME_KANA, T3.NAME1 AS RITU_NAME, T2.FINSCHOOL_NAME ");
				stb.append("     FROM   ENTEXAM_APPLICANTBASE_DAT W1 ");
				stb.append(      "      LEFT JOIN FINSCHOOL_MST T2 ON  ");
				stb.append(      "           T2.FINSCHOOLCD = W1.FS_CD  ");
				stb.append(      "      LEFT JOIN NAME_MST T3 ON  ");
				stb.append(      "           T3.NAMECD1 = 'L001'  ");
				stb.append(      "       AND T3.NAMECD2 = T2.FINSCHOOL_DISTCD ");
				stb.append("     WHERE  W1.ENTEXAMYEAR='"+param[0]+"'  ");
	        } else {
				stb.append("    (SELECT W1.TESTDIV, W1.EXAMNO, W2.NAME, W2.NAME_KANA, T3.NAME1 AS RITU_NAME, T2.FINSCHOOL_NAME ");
				stb.append("     FROM   ENTEXAM_RECEPT_DAT W1, ENTEXAM_APPLICANTBASE_DAT W2  ");
				stb.append(      "      LEFT JOIN FINSCHOOL_MST T2 ON  ");
				stb.append(      "           T2.FINSCHOOLCD = W2.FS_CD  ");
				stb.append(      "      LEFT JOIN NAME_MST T3 ON  ");
				stb.append(      "           T3.NAMECD1 = 'L001'  ");
				stb.append(      "       AND T3.NAMECD2 = T2.FINSCHOOL_DISTCD ");
				stb.append("     WHERE  W1.ENTEXAMYEAR='"+param[0]+"'  ");
				stb.append("            AND W1.ENTEXAMYEAR=W2.ENTEXAMYEAR  ");
				stb.append("            AND W1.EXAMNO=W2.EXAMNO  ");
	        }

			// 入試制度が内部生の場合
			if (param[1].equals("4")){
		        // '全て'以外の場合（入試制度）
				if (!param[1].equals("0")){
					stb.append("            AND W1.APPLICANTDIV='"+param[1]+"' ");
				}
				// 受験者指定
				if (param[5].equals("2")) {
					stb.append("        AND W1.EXAMNO = '"+param[6]+"' ");
				}
				// 合否判定が合格の受験者のみ
	            stb.append("            AND W1.JUDGEMENT = '1' ");
				stb.append("            ) T1  ");
				stb.append("    LEFT JOIN NAME_MST T4 ON T4.NAMECD1='L004' AND T4.NAMECD2='1' ");
				stb.append("ORDER BY  ");
				stb.append("    T1.EXAMNO ");
			} else {
		        // '全て'以外の場合（入試制度）
				if (!param[1].equals("0")) 
				stb.append("            AND W1.APPLICANTDIV='"+param[1]+"' ");
				// '全て'以外の場合（入試区分）
				if (!param[2].equals("0")) 
				stb.append("            AND W1.TESTDIV='"+param[2]+"' ");

				//受験者指定
				if (param[5].equals("2")) {
					stb.append("        AND W1.EXAMNO = '"+param[6]+"' ");
				}
				//合格通知書
				if (param[4].equals("1")) {
		            stb.append("    AND (( W1.JUDGEDIV = '1' ) OR ");
		            stb.append("        ( W1.JUDGEDIV = '3' AND W2.JUDGEMENT = '1' AND W2.SPECIAL_MEASURES = '1')) ");
				}
				//繰上合格候補者通知書
				if (param[4].equals("2")) {
					stb.append("    AND W1.JUDGEDIV =  '3' ");
				}
				//入学許可通知書
				if (param[4].equals("3")) {
		            stb.append("    AND (( W1.JUDGEDIV = '1' ) OR ");
		            stb.append("        ( W1.JUDGEDIV = '3' AND W2.JUDGEMENT = '1' AND W2.SPECIAL_MEASURES = '1')) ");
				}
				stb.append("            ) T1  ");
				stb.append("    LEFT JOIN NAME_MST T4 ON T4.NAMECD1='L004' AND T4.NAMECD2=T1.TESTDIV ");
				stb.append("ORDER BY  ");
				stb.append("    T1.TESTDIV, T1.EXAMNO ");
	        }
		} catch( Exception e ){
			log.error("preStat1 error!");
		}
log.debug(stb);
		return stb.toString();

	}//preStat1()の括り



	/**PrepareStatement close**/
	private void preStatClose(
		PreparedStatement ps1
	) {
		try {
			ps1.close();
		} catch( Exception e ){
			log.error("preStatClose error!");
		}
	}//preStatClose()の括り

    /**
     * 日付をフォーマットYYYY年MM月DD日に設定する
     * @param s
     * @return
     */
    private String fomatSakuseiDate(String sformat, String cnvDate) {

    	String retDate = "";
    	try {
			DateFormat foramt = new SimpleDateFormat("yyyy/MM/dd"); 
			//文字列よりDate型へ変換
			Date date1 = foramt.parse(cnvDate); 
			// 年月日のフォーマットを指定
			SimpleDateFormat sdf1 = new SimpleDateFormat(sformat);
			// Date型より文字列へ変換
			retDate = sdf1.format(date1);
		} catch( Exception e ){
			log.error("setHeader set error!");
		}
		return retDate;
    }

	/**
     *  校長名を取得
     **/
	private String preStat2(String param[])
	{
		StringBuffer stb = new StringBuffer();
		// 学校名および校長名を取得する際は前年度を取得する
		int iyear = Integer.valueOf(param[0]).intValue();
		String searchYear = String.valueOf(iyear - 1);
		try {
			stb.append("SELECT  SCHOOL_NAME, PRINCIPAL_NAME, JOB_NAME, REMARK1 ");
			stb.append("FROM    CERTIF_SCHOOL_DAT ");
			stb.append("WHERE   YEAR = '" + searchYear + "' AND ");
			if(param[1].equals("1")){
				stb.append(        "CERTIF_KINDCD = '105'");
			} else {
				stb.append(        "CERTIF_KINDCD = '106'");
			}
		} catch( Exception e ){
			log.error("getPrincipalNameAndSchoolName error!");
		}
		return stb.toString();

	}

	/**
     * 半角数字を全角数字に変換する
     * @param s
     * @return
     */
    private String convZenkakuToHankaku(String s) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < s.length(); i++) {
          char c = s.charAt(i);
          if (c >= '0' && c <= '9') {
            sb.setCharAt(i, (char) (c - '0' + 0xff10));
          }
        }
        return sb.toString();
    }

	/**
     * 郵便番号・住所・電話番号を取得する
     */
	private String preStat3(String param[])
	{
		StringBuffer stb = new StringBuffer();
		
		try {
			stb.append("SELECT SCHOOLZIPCD, SCHOOLADDR1 ");
   			stb.append("FROM   SCHOOL_MST ");
			stb.append("WHERE  YEAR='"+param[0]+"' ");
		} catch( Exception e ){
			log.error("preStat3 error!");
		}
		return stb.toString();

	}//preStat3()の括り

	
	/* 全角スペースも含めてtrimする */ 
	private static String Ztrim(String original){
		char c;
		int head = 0;
		int length = original.length();
		int tail = length - 1; //最後の文字を指す

		while (head < length 
		    && ((c = original.charAt(head)) <= hSpace || c == zSpace)){
		  ++head;
		}
		while (tail > head 
		    && ((c = original.charAt(tail)) <= hSpace || c == zSpace)){
		    --tail;
		}
		//substring()の第二引数は最後の文字の直後を指定する
		return original.substring(head, tail + 1); 
	}
	
	/**
	 * NULL値を""として返す。
	 */
	private String nvlT(String val) {

		if (val == null) {
			return "";
		} else {
			return val.trim();
		}
	}
	
}//クラスの括り
