package servletpack.KNJA;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *					＜ＫＮＪＡ２２０Ｋ＞  ＨＲ別名列
 *
 *	2004/07/26 nakamoto 作成日
 *	2004/08/10 nakamoto ----- 出力件数を追加。列は全て使用。自動改ページ。
 *	2004/09/06 nakamoto ----- フォームＩＤを変更
 *	2004/09/09 nakamoto ----- 欠番の行はあける。
 *	2004/11/30 nakamoto ----- 50人以上は、次の列に表示
 *	2006/01/19 m-yama   NO001 パラメータ追加（東京都のみ）
 *	2006/05/25 o-naka   NO002 氏名の前に女性は'*'を表示する(男:空白、女:'*')
 */

public class KNJA220K {

    private static final Log log = LogFactory.getLog(KNJA220K.class);
	private int len = 0; //列数カウント用

	private String _paramYear;
	private String _paramGakki;
	private String _paramOutput;
	private String _paramKensuu;
	private String _paramOutput2;
	
	/**
	 * HTTP Get リクエストの処理
	 */
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス

		//	パラメータの取得
		String classcd[] = request.getParameterValues("CLASS_SELECTED");   			//学年・組
		try {
			_paramYear = request.getParameter("YEAR");         						//年度
	        _paramGakki = request.getParameter("GAKKI");   							//学期
			_paramOutput = request.getParameter("OUTPUT");   							//名票種類
			_paramKensuu = request.getParameter("KENSUU");   							//出力件数
			_paramOutput2 = request.getParameter("OUTPUT2");   							//空白行なし
		} catch( Exception ex ) {
			log.error("parameter error!", ex);
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
		} catch (Exception ex) {
			log.error("DB2 open error!", ex);
		}

		//	ＳＶＦ作成処理
		PreparedStatement ps1 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		Set_Head(db2, svf);								//見出し出力のメソッド
		KNJServletUtils.debugParam(request, log);
		//SQL作成
		try {
			ps1 = db2.prepareStatement(Pre_Stat1());		//生徒preparestatement
		} catch (Exception ex) {
			log.error("DB2 open error!", ex);
		}
		try {
			//SVF出力
			for (int ia = 0; ia < classcd.length; ia++) {
				for (int ib = 0; ib < Integer.parseInt(_paramKensuu); ib++) {
					if (Set_Detail_1(db2, svf, classcd[ia], ps1)) {
						nonedata = true;		//生徒出力のメソッド
					}
				}
			}
			if (nonedata) {
				svf.VrEndPage();					//SVFフィールド出力
			}
		} catch (Exception e) {
			log.error("exception!", e);
		} finally {
			DbUtils.closeQuietly(ps1);

			//	該当データ無し
			if (!nonedata) {
				svf.VrSetForm("MES001.frm", 0);
				svf.VrsOut("note" , "");
				svf.VrEndPage();
			}
			
			// 	終了処理
			svf.VrQuit();
			db2.commit();
			db2.close();				//DBを閉じる
			outstrm.close();			//ストリームを閉じる 
		}

    }//doGetの括り



	/** SVF-FORM **/
	private void Set_Head(DB2UDB db2, Vrw32alp svf){

		if(_paramOutput.equals("1")) 	svf.VrSetForm("KNJA220K_1.frm", 1);
		else 						svf.VrSetForm("KNJA220K_2.frm", 1);

	//	ＳＶＦ属性変更--->出力形式がクラス別の場合クラス毎に改ページ
		//svf.VrAttribute("HR_NAME1","FF=1");

	}//Set_Head()の括り



	/**SVF-FORM**/
	private boolean Set_Detail_1(DB2UDB db2, Vrw32alp svf, String classcd, PreparedStatement ps1) {
		boolean nonedata = false;
		ResultSet rs = null;
		try {
			int pp = 0;
			ps1.setString(++pp,classcd);	//学年・組
			rs = ps1.executeQuery();
			//log.debug("Set_Detail_1 sql ok!");
			
			String strx;
			String stry;
			String strz;
			int x;					//姓の文字数
			int y;					//名の文字数
			int z;					//空白文字の位置
			int gyo = 1;			//行数カウント用
			len++;
			final int maxline = 50;
			int atdno = 1;			//出席番号用
			boolean len_flg = false;
			
			while (rs.next()) {
				if (null == _paramOutput2) {
					gyo = rs.getInt("ATTENDNO");
					atdno = gyo;
					if (gyo > maxline) {
						gyo = gyo - maxline;
						if (!len_flg) len++;
						len_flg = true;
					}
				} else {
					if (gyo > maxline) {
						gyo = 1;
						len++;
					}
				}
				if (len > 3) {
					len = 1;
					svf.VrEndPage();					//SVFフィールド出力
				}
				//	組略称・担任名出力
				svf.VrsOut("HR_NAME" + String.valueOf(len) 	, rs.getString("HR_NAMEABBV") );
				svf.VrsOut("STAFFNAME" + String.valueOf(len) + "_1" , rs.getString("STAFFNAME") );

				//	出席番号・かな出力
				svf.VrsOutn("ATTENDNO" + String.valueOf(len) 	,gyo, String.valueOf(atdno) );
				final String nameKana = rs.getString("NAME_KANA");
				if (KNJ_EditEdit.getMS932ByteLength(nameKana) > 22) {
					svf.VrsOutn("KANA" + String.valueOf(len) + "_2" ,gyo, nameKana);
				} else {
					svf.VrsOutn("KANA" + String.valueOf(len) 		,gyo, nameKana);
				}
				svf.VrsOutn("MARK" + String.valueOf(len) 		,gyo, rs.getString("SEX") );//NO002 男:空白、女:'*'

				//	生徒漢字・規則に従って出力
				strz = rs.getString("NAME");
				z = strz.indexOf("　");
				boolean div = true;
				if (0 <= z) {
					final String n1 = strz.substring(0, z);
					final String n2 = strz.substring(z + 1);
					if (KNJ_EditEdit.getMS932ByteLength(n1) > 8 || KNJ_EditEdit.getMS932ByteLength(n2) > 8) {
						div = false;
					}
				}
				if (z < 0 || div == false) {
					if (KNJ_EditEdit.getMS932ByteLength(strz) > 18) {
						svf.VrsOutn("NAME" + String.valueOf(len) + "_2", gyo, strz); //空白がない
					} else {
						svf.VrsOutn("NAME" + String.valueOf(len) 	,gyo, strz); //空白がない
					}
				} else {
					strx = strz.substring(0,z);
					stry = strz.substring(z+1);
					x = strx.length();
					y = stry.length();
					if ( x == 1 ){
						svf.VrsOutn("LNAME" + String.valueOf(len) + "_2" 	,gyo, strx );		//姓１文字
					} else {
						svf.VrsOutn("LNAME" + String.valueOf(len) + "_1" 	,gyo, strx );		//姓２文字以上
					}
					if ( y == 1 ){
						svf.VrsOutn("FNAME" + String.valueOf(len) + "_2" 	,gyo, stry );		//名１文字
					} else {
						svf.VrsOutn("FNAME" + String.valueOf(len) + "_1" 	,gyo, stry );		//名２文字以上
					}
				}
				nonedata = true;
				if (null != _paramOutput2) {
					gyo++;
				}
			}
		} catch (Exception ex) {
			log.error("Set_Detail_2 read error!", ex);
		} finally {
			DbUtils.closeQuietly(rs);
			db2.commit();
		}
		return nonedata;
	}//Set_Detail_1()の括り

	/**PrepareStatement作成**/
	private String Pre_Stat1() {
		StringBuffer stb = new StringBuffer();
		stb.append("SELECT ");
		stb.append(    "value(w3.attendno,'') attendno,");
		stb.append(    "CASE WHEN w4.SEX = '2' THEN '*' ELSE '' END AS SEX, ");//NO002 男:空白、女:'*'
		stb.append(    "value(w4.name,'') name,");
		stb.append(    "value(w4.name_kana,'') name_kana,");
		stb.append(    "value(w1.hr_nameabbv,'') hr_nameabbv,");
		stb.append(    "value(w2.staffname,'') staffname ");
		stb.append("FROM ");
		stb.append(    "schreg_base_mst w4,");
		stb.append(    "schreg_regd_dat w3,");
		stb.append(    "schreg_regd_hdat w1 ");
		stb.append(    "left join staff_mst w2 on w1.tr_cd1=w2.staffcd ");
		stb.append("WHERE ");
		stb.append(    "w1.year='"+_paramYear+"' AND ");
		stb.append(    "w1.semester='"+_paramGakki+"' AND ");
		stb.append(    "w1.grade || w1.hr_class=? AND ");
		stb.append(    "w1.year=w3.year AND ");
		stb.append(    "w1.semester=w3.semester AND ");
		stb.append(    "w1.grade=w3.grade AND ");
		stb.append(    "w1.hr_class=w3.hr_class AND ");
		stb.append(    "w3.schregno=w4.schregno ");
		stb.append("order by w3.attendno");
		return stb.toString();

	}//Pre_Stat1()の括り

}//クラスの括り
