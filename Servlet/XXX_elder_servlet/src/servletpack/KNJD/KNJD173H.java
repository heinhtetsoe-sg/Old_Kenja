/**
 *
 *  学校教育システム 賢者 [成績管理]  通知票(広島国際)
 *
 *  2006/03/28 o-naka 新規作成--->KNJD173をコピー
 *  2006/04/04 nakamoto ・10段階評定は、GRAD_VALUE2にセットして、5段階評定は、GRAD_VALUEにセットする仕様変更に伴う修正
 *  2006/04/07 yamashiro・出欠集計用テーブルの変更および出欠集計処理の変更に対応 --NO001
 *                          KNJD173を参照
 *                          ○学籍不在時および留学・休学中の出欠データはカウントしない
 *                          ○出停・忌引がある日は遅刻・早退をカウントしない
 *  2007/02/09 nakamoto ・学年評定欄のみ　”／”表記を外す。（修正依頼メール：2007/02/09参照）
 *  2007/12/19 nakasone ・特別活動欄を追加
 *  						設定内容として、部クラブ名と委員会名を全角カンマ区切りで設定
 */

package servletpack.KNJD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJC.KNJC053_BASE;
import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJSvfFieldModify;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;


public class KNJD173H {

    private static final Log log = LogFactory.getLog(KNJD173H.class);
    private int skekka1;                //欠課時数(出欠状況)
    private int skekka2;                //欠課時数(出欠状況)
    private int skekka9;                //欠課時数(出欠状況)
    private KNJObjectAbs knjobj = new KNJEditString();            //編集用クラス

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定
    private KNJSvfFieldModify svfobj = new KNJSvfFieldModify();   //フォームのフィールド属性変更
    
    private Param _param;

    /**
     *
     *  KNJD.classから最初に起動されるクラス
     *
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        boolean nonedata = false;

        // print svf設定
        sd.setSvfInit(request, response, svf);
        // ＤＢ接続
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error! ");
            return;
        }
        // パラメータの取得
        _param = createParam(request, db2);
        
        // 印刷処理
        nonedata = false;
        try {
            if (printSvfMain(db2, svf)) {
            	nonedata = true;        //SVF-FORM出力処理
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }

        // 終了処理
        sd.closeSvf(svf, nonedata);
        sd.closeDb(db2);
    }

    /** 
     * SVF-OUT 印刷処理
     */
    private boolean printSvfMain(final DB2UDB db2, final Vrw32alp svf) {
        boolean nonedata = false;

        final List<Student> rs1List = Student.loadStudent(db2, _param);

        //データ読み込み＆ＳＶＦ出力
        try {
            int schno1 = 0;     //学籍の出席番号
            for (final Student student : rs1List) {
                
                svf.VrSetForm( "KNJD173_5.frm", 1 );

//              svf.VrAttribute( "ATTENDNO", "FF=1" );    //出席番号で改ページ

                schno1 = KnjDbUtils.getInt(student._regdRow, "ATTENDNO", Integer.valueOf(0)).intValue();                    //学籍データの出席番号を保存

                //学籍情報等を出力
                printSvfRegdOut(svf, student._regdRow, _param);

                //成績明細データを出力(SVF-FORMへ出力）
                skekka1 = 0;
                skekka2 = 0;
                skekka9 = 0;
				if (printSvfRecDetail(svf, _param, student)) {
                	nonedata = true;
                }
				log.debug("skekka1="+skekka1+" skekka2="+skekka2+" skekka9="+skekka9);

				//出欠データを出力
                printSvfAttend(svf, student, Integer.parseInt( _param._gakki ), _param);

                //出欠データ(遅刻回数)を出力
                printSvfAttendSHR(svf, student, _param);

                //出欠データ(備考)を出力
                printSvfRemark(svf, student, _param);
                
                //出欠データの下部にコメントを出力
                printSvfComment(svf, _param);

                //部活動・委員会を出力
                printSvfCommitandClub(svf, db2, student, _param);
                
                //成績明細データ(順位)を出力
                printSvfRank(svf, student, Integer.parseInt( _param._gakki ), _param);

                svf.VrEndPage();
            }
            if (schno1 != 0) {
                //if( ! nonedata )svf.VrEndRecord();
                svf.VrPrint();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.error("printSvfMain read error! ", ex);
        }
        
        for (final PreparedStatement ps : _param._psMap.values()) {
        	DbUtils.closeQuietly(ps);
        }

        return nonedata;

    }//boolean printSvfMain()の括り

    /** 
     *  SVF-OUT 生徒のデータ等を印刷する処理
     *             学籍データ、通信欄
     *
     */
    private void printSvfRegdOut(final Vrw32alp svf, final Map<String, String> rs, final Param param) {

        try {
            svf.VrsOut("NENDO",        param._nendo);                         //年度
            svf.VrsOut("STAFFNAME",    param._staffname);                        //担任名
            svf.VrsOut("HR_NAME",      KnjDbUtils.getString(rs, "HR_NAME"));          //組名称
            svf.VrsOut("ATTENDNO",     KnjDbUtils.getString(rs, "ATTENDNO"));         //出席番号
            svf.VrsOut("NAME",         KnjDbUtils.getString(rs, "NAME"));             //生徒氏名
//log.debug("attendno="+rs.getString("ATTENDNO"));

            ArrayList arrlist;
            arrlist = knjobj.retDividString(KnjDbUtils.getString(rs, "COMM1"), 42, 4);
            if (arrlist != null) {
                for (int i = 0; i < arrlist.size(); i++) {
                    svf.VrsOutn("MESSAGE1", i + 1, (String) arrlist.get(i));           //通信欄(１学期)
                }
            }
            arrlist = knjobj.retDividString(KnjDbUtils.getString(rs, "COMM2"), 42, 4);
            if (arrlist != null) {
                for (int i = 0; i < arrlist.size(); i++) {
                    svf.VrsOutn("MESSAGE2", i + 1, (String) arrlist.get(i));           //通信欄(２学期)
                }
            }
            arrlist = knjobj.retDividString(KnjDbUtils.getString(rs, "COMM3"), 42, 4);
            if (arrlist != null) {
                for (int i = 0; i < arrlist.size(); i++) {
                    svf.VrsOutn("MESSAGE3", i + 1, (String) arrlist.get(i));           //通信欄(３学期)
                }
            }

        } catch (Exception ex) {
            log.error("printSvfRegdOut error! ", ex);
        }

    }//printSvfRegdOut()の括り


    /** 
     * SVF-OUT 成績明細印刷処理
     *
     */
    private boolean printSvfRecDetail(final Vrw32alp svf, final Param param, final Student student) {
        boolean nonedata = false;
        boolean bsubclass90 = false;
        if (true == bsubclass90) { bsubclass90 = true; }
		for (int i = 0; i < student._recordRowList.size(); i++) {
			final Map<String, String> rs = student._recordRowList.get(i);
			/***
            if( nonedata )svf.VrEndRecord();
                if( rs.getString("SUBCLASSCD").substring( 0, 2 ).equals(definecode.subject_T )  &&  !bsubclass90 ){
                    //総合的な学習の時間は１行空けて出力する
                    if( svfoutBlankLine( Integer.parseInt(rs.getString("NUM90")), i ) ){
                        i++;
                        svf.VrEndRecord();
                    }
                    bsubclass90 = true;
                }
***/
            if (printSvfRecDetailOut(svf, rs, param, (i + 1))) {
            	nonedata = true;
            }
        }
        return nonedata;
    }


    /** 
     *
     * SVF-OUT 成績明細印刷
     *
     */
    private boolean printSvfRecDetailOut(final Vrw32alp svf, final Map<String, String> rs, final Param param, int i) {
        boolean nonedata = false;        
        if (30 < i) {
        	i = (i % 30 == 0) ? 30 : i % 30;
        }

        try {
            svf.VrsOutn("CLASS"    ,i ,  KnjDbUtils.getString(rs, "CHAIRNAME") );     //教科
            svf.VrsOutn("SUBCLASS" ,i ,  KnjDbUtils.getString(rs, "SUBCLASSNAME") );  //科目
//            svfFieldAttribute_CLASS( svf, KnjDbUtils.getString(rs, "CHAIRNAME"), i );
//            svfFieldAttribute_SUBCLASS( svf, KnjDbUtils.getString(rs, "SUBCLASSNAME"), i );
//log.debug("gyo="+i+" replaceflg="+KnjDbUtils.getString(rs, "REPLACEFLG"));
//log.debug("CLASS="+KnjDbUtils.getString(rs, "CHAIRNAME")+" SUBCLASS="+KnjDbUtils.getString(rs, "SUBCLASSNAME"));

            //学期評価・欠課時数は評価読替科目は非表示
            int replaceflg = KnjDbUtils.getInt(rs, "REPLACEFLG", Integer.valueOf(0));
			if( replaceflg != 9 ){
//log.debug("sem1_attend="+KnjDbUtils.getString(rs, "SEM1_ATTEND")+" sem2_attend="+KnjDbUtils.getString(rs, "SEM2_ATTEND"));
                svf.VrsOutn( "RECORD1", i, setValue(param._gakki, KnjDbUtils.getString(rs, "SEM1_VALUE"), KnjDbUtils.getString(rs, "SEM1_ATTEND")));
                svf.VrsOutn( "KEKKA1" ,i ,  setzeroTOblank( KnjDbUtils.getString(rs, "ABSENT_SEM1")) );  //欠課時数
                if ( 1 < Integer.parseInt( param._gakki ) ) {
                    svf.VrsOutn( "RECORD2", i, setValue(param._gakki, KnjDbUtils.getString(rs, "SEM2_VALUE"), KnjDbUtils.getString(rs, "SEM2_ATTEND")));
                    svf.VrsOutn( "KEKKA2" ,i ,  setzeroTOblank( KnjDbUtils.getString(rs, "ABSENT_SEM2") ));  //欠課時数
                }
                if ( KnjDbUtils.getString(rs, "ABSENT_SEM1") != null ) skekka1 += Integer.parseInt( KnjDbUtils.getString(rs, "ABSENT_SEM1") );
                if ( KnjDbUtils.getString(rs, "ABSENT_SEM2") != null ) skekka2 += Integer.parseInt( KnjDbUtils.getString(rs, "ABSENT_SEM2") );
            }
            //学年評定・欠課時数・履修単位数・修得単位数は評価読替元科目は非表示
//log.debug(KnjDbUtils.getString(rs, "SUBCLASSNAME") + "   " + rs.getInt("REPLACEFLG") + "   " + KnjDbUtils.getString(rs, "GRAD_VALUE"));
                     //REPLACEFLG 9:評価読替先 1OR2:評価読替元 1:通常
            if( ( replaceflg == 9  &&  KnjDbUtils.getString(rs, "GRAD_VALUE") != null )  ||
                ( replaceflg != 9  &&  replaceflg != 2 ) ){
//log.debug("grad_attend="+KnjDbUtils.getString(rs, "GRAD_ATTEND"));
                if ("3".equals(param._gakki)) {
                    svf.VrsOutn( "RECORD3", i, setValue(param._gakki, KnjDbUtils.getString(rs, "GRAD_VALUE"), null));
                }
//log.debug("subclasscd="+KnjDbUtils.getString(rs, "SUBCLASSCD")+"  replaceflg"+rs.getInt("REPLACEFLG")+"  absent="+KnjDbUtils.getString(rs, "ABSENT_TOTAL"));
                if( replaceflg == 9  &&  KnjDbUtils.getString(rs, "REPLACE_ABSENT_TOTAL") != null ){
                    if ("3".equals(param._gakki)) {
                        svf.VrsOutn( "KEKKA3",i ,  setzeroTOblank( KnjDbUtils.getString(rs, "REPLACE_ABSENT_TOTAL")) );   //欠課時数合計
                    }
                    if ( KnjDbUtils.getString(rs, "REPLACE_ABSENT_TOTAL") != null ) skekka9 += Integer.parseInt( KnjDbUtils.getString(rs, "REPLACE_ABSENT_TOTAL") );
                } else
                if (checkTotalAbsent(rs, Integer.parseInt(param._gakki))) {
                    if ("3".equals(param._gakki)) {
                        svf.VrsOutn( "KEKKA3",i ,  setzeroTOblank( KnjDbUtils.getString(rs, "ABSENT_TOTAL")) );   //欠課時数合計
                    }
                    if ( KnjDbUtils.getString(rs, "ABSENT_TOTAL") != null ) skekka9 += Integer.parseInt( KnjDbUtils.getString(rs, "ABSENT_TOTAL") );
                }
            }
            doOutAddGet_Credits(svf, rs, Integer.parseInt( param._gakki ), i);  //修得単位数出力＆加算処理
            nonedata = true;
        } catch (Exception ex) {
        	log.error("error! ", ex);
        }
        return nonedata;
    }


    /**
     * 評定を印刷します。
     * @param semester 学期
     * @param value 評定
     * @param attend テスト欠席
     * @return
     */
    private String setValue(
            final String semester,
            final String value,
            final String attend
    ){
        if ("3".equals(semester)) {
            if (StringUtils.isNotBlank(value)) { return value; }
            return "";
        }
        
        if (StringUtils.isNotBlank(value) && StringUtils.isNotBlank(attend)) { return attend + value; }
        if (StringUtils.isNotBlank(value)) { return value; }
        if (StringUtils.isNotBlank(attend)) { return attend; }
        return "";
    }
    
    /** 
     *
     * SVF-OUT ゼロをブランクに変換
     */
    private String setzeroTOblank(String strx) {
        String retval = null;
        try {
            if( strx == null ) retval = "";
            else if( strx.equals("0") ) retval = "";
            else retval = strx;
        } catch( Exception ex ){
            log.error("printSvfRegdOut error! ", ex);
            retval = "";
        }
        return retval;
    }


    /**
     *  修得単位数出力＆加算処理  --NO010
     */
    private void doOutAddGet_Credits(final Vrw32alp svf, final Map<String, String> rs, final int semes, final int gyo) {
        boolean boo = false;
        try {
            //評価読替元科目は非表示
            final int replaceFlg = KnjDbUtils.getInt(rs, "REPLACEFLG", Integer.valueOf(0));
			if (replaceFlg == 9 && KnjDbUtils.getString(rs, "GRAD_VALUE") != null) {
            	boo = true;
            } else if (replaceFlg == 0) {
            	boo = true;
            }

            //成績データの修得単位数を出力
            if (KnjDbUtils.getString(rs, "GET_CREDIT") == null) {
            	boo = false;
            }

            //取り敢えずの処理
            if (KnjDbUtils.getString(rs, "ON_RECORD_GET") == null  &&  KnjDbUtils.getString(rs, "COMP_UNCONDITION_FLG").equals("1")) {
                int kekka = 0;
                if (KnjDbUtils.getString(rs, "ABSENT_SEM1") != null)  {
                	kekka += Integer.parseInt( KnjDbUtils.getString(rs, "ABSENT_SEM1") );
                }
                if (1 < semes  &&  KnjDbUtils.getString(rs, "ABSENT_SEM2") != null ) {
                	kekka += Integer.parseInt( KnjDbUtils.getString(rs, "ABSENT_SEM2") );
                }
                if (2 < semes  &&  KnjDbUtils.getString(rs, "ABSENT_SEM3") != null ) {
                	kekka += Integer.parseInt( KnjDbUtils.getString(rs, "ABSENT_SEM3") );
                }
                if (replaceFlg == 9  &&  KnjDbUtils.getString(rs, "REPLACE_ABSENT_TOTAL") != null ) {
                	kekka += Integer.parseInt( KnjDbUtils.getString(rs, "REPLACE_ABSENT_TOTAL") );
                }
                if (0 < kekka  &&  KnjDbUtils.getString(rs, "ABSENCE_HIGH") != null  &&  Double.parseDouble( KnjDbUtils.getString(rs, "ABSENCE_HIGH")) < kekka ) {
                	boo = false;
                }
            }

            if (boo == true  &&  !KnjDbUtils.getString(rs, "GET_CREDIT").equals("0") && 2 < semes) {
                svf.VrsOutn( "CREDIT", gyo ,  KnjDbUtils.getString(rs, "GET_CREDIT") );  //修得単位数
            }
        } catch (Exception ex) {
            log.error("doOutAddGet_Credits error! ", ex);
        }
    }


    /** 
     *
     * SVF-FORM 欠課時数合計のチェック
     *
     */
    private boolean checkTotalAbsent(final Map<String, String> rs, final int semes) {
        boolean check = false;
        try {
            if( KnjDbUtils.getString(rs, "ABSENT_SEM1") != null ) check = true;
            else if( 1 < semes  &&  KnjDbUtils.getString(rs, "ABSENT_SEM2") != null ) check = true;
            else if( 2 < semes  &&  KnjDbUtils.getString(rs, "ABSENT_SEM3") != null ) check = true;
        } catch (Exception ex) {
            log.error("checkTotalAbsent error! ", ex);
        }
        return check;
    }


    /** 
     *
     * SVF-OUT 出欠明細印刷処理 
     */
    private void printSvfAttend(final Vrw32alp svf, final Student student, final int sem, final Param param) {
        for (final Map<String, String> rs : student._attendSemesRowList) {
        	
            int i = Integer.parseInt( KnjDbUtils.getString(rs, "SEMESTER"));
            if( i == 9 )i = 4;
//          if( i == 9 )i = definecode.semesdiv + 1;

            if( 0 <= Integer.parseInt(KnjDbUtils.getString(rs, "LESSON"))) {
                svf.VrsOutn("LESSON",     i,   KnjDbUtils.getString(rs, "LESSON") );          //授業日数
            }
            svf.VrsOutn("KIBIKI",     i,   String.valueOf(KnjDbUtils.getInt(rs, "MOURNING", null) + KnjDbUtils.getInt(rs, "SUSPEND", null) + ("true".equals(param._useVirus) ? KnjDbUtils.getInt(rs, "VIRUS", null) : 0)  + ("true".equals(param._useKoudome) ? KnjDbUtils.getInt(rs, "KOUDOME", null) : 0) ) );    //出停・忌引日数
//            svf.VrsOutn("ABROAD",     i,   rs.getString("TRANSFER_DATE") );     //留学中の授業日数
            svf.VrsOutn("PRESENT",    i,   KnjDbUtils.getString(rs, "MLESSON") );             //出席しなければならない日数
            svf.VrsOutn("ABSENCE",    i,   KnjDbUtils.getString(rs, "SICK") );              //欠席日数
//            svf.VrsOutn("ATTEND",     i,   KnjDbUtils.getString(rs, "PRESENT") );               //出席日数

//            svf.VrsOutn("LATE1",      i,   KnjDbUtils.getString(rs, "LATE") );              //遅刻回数
            svf.VrsOutn("LEAVE",      i,   KnjDbUtils.getString(rs, "EARLY") );               //早退回数

            String kekka = "";
            if ( i == 1 ) kekka = String.valueOf( skekka1 );
            if ( i == 2 ) kekka = String.valueOf( skekka2 );
            if ( i == 3 ) kekka = String.valueOf( skekka9 - skekka1 - skekka2 );
            if ( i == 4 ) kekka = String.valueOf( skekka9 );
//            if ( i > definecode.semesdiv ) kekka = String.valueOf( skekka9 );
            svf.VrsOutn("KEKKA",      i,   kekka );               //欠課時数
        }
    }

    /** 
     *
     * SVF-OUT 出欠(遅刻回数)印刷処理 
     *
     * 朝夕のＳＨＲで遅刻及び欠課時数の合計とする。
     * １日欠席の時は含まない。
     * 但し、早退があったら除外する。
     */
    private void printSvfAttendSHR(final Vrw32alp svf, final Student student, final Param param) {
    	
        for (final Map<String, String> rs : student._attendShrRowList) {
            int i = Integer.parseInt( KnjDbUtils.getString(rs, "SEMESTER") );
            if( i == 9 )i = 4;
//          if( i == 9 )i = definecode.semesdiv + 1;

            svf.VrsOutn("LATE1",      i,   KnjDbUtils.getString(rs, "COUNT") );       //遅刻回数 ＳＨＲ
            svf.VrsOutn("LATE2",      i,   KnjDbUtils.getString(rs, "NOT_SHR") );     //遅刻回数 授業

        }
    }


    /** 
     *
     * SVF-OUT 出欠(備考)印刷処理 
     *
     * 各学期のＳＨＲに登録された最初の内容
     */
    private void printSvfRemark(final Vrw32alp svf, final Student student, final Param param) {

    	for (final Map<String, String> row : student._attendRemarkRowList) {
            svf.VrsOut("REMARK" + KnjDbUtils.getString(row, "SEMESTER"), KnjDbUtils.getString(row, "DI_REMARK"));                //備考
    	}
    }

    /** 
    *
    * SVF-OUT コメント印刷処理 
    */
   private void printSvfComment(final Vrw32alp svf, final Param param) {
       svf.VrsOutn("SYUKKETU_COMENT",1,   param._remark1 );
       svf.VrsOutn("SYUKKETU_COMENT",2,   param._remark2 );
   }

    /** 
    *
    * SVF-OUT 特別活動欄印刷処理 
    *
    */
	private void printSvfCommitandClub(
		final Vrw32alp svf, 
		final DB2UDB db2,
		final Student student, 
		final Param param )
	{
		String sClubName = "";
		String sCommitName = "";

    	final String ps1Key = "PS_CLUB";
    	if (!param._psMap.containsKey(ps1Key)) {
    		final String sql = prestatementClub(param);
    		try {
    			param._psMap.put(ps1Key, db2.prepareStatement(sql));
    		} catch (Exception e) {
    			log.error("exception!", e);
    		}
    	}
        final PreparedStatement ps1 = param._psMap.get(ps1Key);       //クラブ活動

		// 部活動名取得処理
		ResultSet rs = null;
		try {
			int pp = 0;
			ps1.setString( ++pp, student._schregno);
			rs = ps1.executeQuery();
            while ( rs.next() ) {
				sClubName = StringUtils.defaultString(rs.getString("CLUBNAME"));
            }
		} catch( Exception ex ){
			log.error( "error! ", ex );
		} finally{
	          DbUtils.closeQuietly(rs);
		}

    	final String ps2Key = "PS_COMMIT";
    	if (!param._psMap.containsKey(ps2Key)) {
    		final String sql = prestatementCommit(param);
    		try {
    			param._psMap.put(ps2Key, db2.prepareStatement(sql));
    		} catch (Exception e) {
    			log.error("exception!", e);
    		}
    	}
        final PreparedStatement ps2 = param._psMap.get(ps2Key);       //委員会

		// 委員会名取得処理
		rs = null;
		try {
			int pp = 0;
			ps2.setString( ++pp, student._schregno);
			rs = ps2.executeQuery();
            while ( rs.next() ) {
            	String Committeecd = StringUtils.defaultString(rs.getString("COMMITTEECD"));
            	if (Committeecd.equals("")) {
                	sCommitName = StringUtils.defaultString(rs.getString("CHARGENAME"));
            	} else {
                	sCommitName = StringUtils.defaultString(rs.getString("COMMITTEENAME"));
            	}
            }
		} catch (Exception ex) {
			log.error( "error! ", ex);
		} finally{
		    DbUtils.closeQuietly(rs);
		}

		// 帳票設定処理
		if (!sClubName.equals("") && !sCommitName.equals("")) {
			svf.VrsOut("ACTIVITY_MESSAGE",   sClubName + "，" +  sCommitName);
		} else {
			if (sClubName.equals("")) {
				svf.VrsOut("ACTIVITY_MESSAGE",   sCommitName);
			} else {
				svf.VrsOut("ACTIVITY_MESSAGE",   sClubName);
			}
		}
		
	}

    /** 
     *
     * SVF-OUT 成績データ(順位)印刷処理 
     *
     * 評定の総合点のクラス順位
     */
    private void printSvfRank(final Vrw32alp svf, final Student student, final int sem, final Param param) {
        for (final Map<String, String> rs : student._rankRowList) {
            //順位／人数
            if ( KnjDbUtils.getString(rs, "SEM1_RNK") != null ) 
                svf.VrsOut("PRECEDENCE1",   KnjDbUtils.getString(rs, "SEM1_RNK") + "／" + KnjDbUtils.getString(rs, "SEM1_CNT") );
            if ( KnjDbUtils.getString(rs, "SEM2_RNK") != null && 1 < sem ) 
                svf.VrsOut("PRECEDENCE2",   KnjDbUtils.getString(rs, "SEM2_RNK") + "／" + KnjDbUtils.getString(rs, "SEM2_CNT") );
            if ( KnjDbUtils.getString(rs, "GRAD_RNK") != null && 2 < sem ) 
                svf.VrsOut("PRECEDENCE3",   KnjDbUtils.getString(rs, "GRAD_RNK") + "／" + KnjDbUtils.getString(rs, "GRAD_CNT") );
        }
    }

    /** 
     *  PrepareStatement作成 クラブ活動
     */
    private String prestatementClub (final Param param) {
        final StringBuffer stb = new StringBuffer();
        //クラブの表
        stb.append("SELECT W1.CLUBCD, ");
        stb.append("       W1.SDATE, ");
        stb.append("       W2.CLUBNAME ");
        stb.append("FROM    SCHREG_CLUB_HIST_DAT W1 ");
        stb.append("LEFT OUTER JOIN CLUB_MST W2 ON W1.CLUBCD = W2.CLUBCD ");
        stb.append("    AND W2.SCHOOLCD = W1.SCHOOLCD ");
        stb.append("    AND W2.SCHOOL_KIND = W1.SCHOOL_KIND ");
        stb.append("WHERE   '" + param._date + "' BETWEEN W1.SDATE AND VALUE(W1.EDATE,'" + param._date + "') ");
        stb.append(     "AND W1.SCHREGNO = ? ");
        stb.append("ORDER BY SDATE, CLUBCD  fetch first 1 rows only");
        return stb.toString();
    }

    /** 
     *  PrepareStatement作成 委員会
     */
    private String prestatementCommit (final Param param) {
        final StringBuffer stb = new StringBuffer();
        //委員会の表
        stb.append("SELECT W1.SEQ, ");
        stb.append("       W1.COMMITTEECD, ");
        stb.append("       W1.CHARGENAME, ");
        stb.append("       W2.COMMITTEENAME ");
        stb.append("FROM    SCHREG_COMMITTEE_HIST_DAT W1 ");
        stb.append("LEFT OUTER JOIN COMMITTEE_MST W2 ON W1.COMMITTEECD = W2.COMMITTEECD ");
        stb.append(                           "AND W1.COMMITTEE_FLG = W2.COMMITTEE_FLG ");
        stb.append("    AND W2.SCHOOLCD = W1.SCHOOLCD ");
        stb.append("    AND W2.SCHOOL_KIND = W1.SCHOOL_KIND ");
        stb.append("WHERE   W1.YEAR = '" + param._year + "' ");
        stb.append(    "AND W1.SCHREGNO = ? ");
        stb.append("ORDER BY W1.SEQ DESC  fetch first 1 rows only");
        return stb.toString();
    }

    /** 
     *  get parameter doGet()パラメータ受け取り 
     */
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 69961 $ $Date: 2019-10-01 16:18:53 +0900 (火, 01 10 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }


    /**
     * ＳＶＦ−ＦＯＲＭフィールド属性変更(RECORD) => 文字数により文字ピッチ及びＹ軸を変更する
     * 2005/12/22 Build
     */
    private void svfFieldAttribute_CLASS( Vrw32alp svf, String name, int ln )
    {
        try {
            svfobj.width = 250;     //フィールドの幅(ドット)
            svfobj.height = 58;     //フィールドの高さ(ドット)
            svfobj.ystart = 1044;   //開始位置(ドット)
            svfobj.minnum = 10;     //最小設定文字数
            svfobj.maxnum = 40;     //最大設定文字数
            svfobj.setRetvalue( name, ln );
            svf.VrAttribute("CLASS" , "Y="+ svfobj.jiku );
            svf.VrAttribute("CLASS" , "Size=" + svfobj.size );
            svf.VrsOut("CLASS",  name );
        } catch( Exception e ){
            log.error("svf.VrAttribute error! ", e);
        }
    }


    /**
     * ＳＶＦ−ＦＯＲＭフィールド属性変更(RECORD) => 文字数により文字ピッチ及びＹ軸を変更する
     * 2005/12/22 Build
     */
    private void svfFieldAttribute_SUBCLASS( Vrw32alp svf, String name, int ln )
    {
        try {
            svfobj.width = 740;     //フィールドの幅(ドット)
            svfobj.height = 58;     //フィールドの高さ(ドット)
            svfobj.ystart = 1044;   //開始位置(ドット)
            svfobj.minnum = 30;     //最小設定文字数
            svfobj.maxnum = 40;     //最大設定文字数
            svfobj.setRetvalue( name, ln );
            svf.VrAttribute("SUBCLASS" , "Y="+ svfobj.jiku );
            svf.VrAttribute("SUBCLASS" , "Size=" + svfobj.size );
            svf.VrsOut("SUBCLASS",  name );
        } catch( Exception e ){
            log.error("svf.VrAttribute error! ", e);
        }
    }


    /**
     * 総合的な学習の時間における空行挿入処理
     */
    private boolean svfoutBlankLine( int num2, int ln )
    {
        boolean ret = false;
        try {
            int i = ( ln % 30 == 0 )? 30: ln % 30;
            if( 30 != i + num2 )ret = true;
        } catch( Exception e ){
            log.error("svfoutBlankLine error! ", e);
        }
        return ret;
    }
    
    public static class Student {
		private Map<String, String> _regdRow = Collections.emptyMap();
    	private String _schregno;
    	private List<Map<String, String>> _attendSemesRowList = new ArrayList<Map<String, String>>();
    	private List<Map<String, String>> _recordRowList = new ArrayList<Map<String, String>>();
    	private List<Map<String, String>> _rankRowList = new ArrayList<Map<String, String>>();
    	private List<Map<String, String>> _attendRemarkRowList = new ArrayList<Map<String, String>>();
    	private List<Map<String, String>> _attendShrRowList = new ArrayList<Map<String, String>>();
    	
    	public static List<Student> loadStudent(final DB2UDB db2, final Param param) {
    		final String regdSql = Student.prestatementRegd(param);          //学籍データ
            //log.info(" regdSql = " + regdSql);
    		final List<Student> studentList = new ArrayList<Student>();
    		final Map<String, Student> studentMap = new HashMap<String, Student>();
    		for (final Map<String, String> row : KnjDbUtils.query(db2, regdSql)) {
    			final Student student = new Student();
    			student._regdRow = row;
    			student._schregno = KnjDbUtils.getString(student._regdRow, "SCHREGNO");
    			studentList.add(student);
    			studentMap.put(student._schregno, student);
    		}
    		
        	//出欠データ
    		log.info("load attend.");
        	final String attendSemesSql = AttendAccumulate.getAttendSemesSql(
        			param._year,
        			param._gakki,
        			param._sDate,
        			param._date,
        			param._attendParamMap
        			);
        	for (final Map<String, String> row : KnjDbUtils.query(db2, attendSemesSql)) {
        		final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
        		if (null != student) {
        			student._attendSemesRowList.add(row);
        		}
    		}

        	PreparedStatement ps = null;
    		try {
    			log.info("load subclass.");
    			//成績明細データ
    			final String sql = prestatementSubclass(param);
    			ps = db2.prepareStatement(sql);
            	for (final Student student : studentList) {
            		student._recordRowList = KnjDbUtils.query(db2, ps, new Object[] {student._schregno, student._schregno});
            	}
    		} catch (Exception e) {
    			log.error("exception!", e);
    		} finally {
    			DbUtils.closeQuietly(ps);
    		}

        	//成績明細データ(順位)
    		log.info("load rank.");
        	final String rankSql = prestatementRank(param);
        	for (final Map<String, String> row : KnjDbUtils.query(db2, rankSql)) {
        		final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
        		if (null != student) {
        			student._rankRowList.add(row);
        		}
        	}

    		try {
            	log.info("load attend remark.");
        		//出欠データ(備考)
        		final String sql = prestatementRemark(param);
    			ps = db2.prepareStatement(sql);
            	for (final Student student : studentList) {
            		student._attendRemarkRowList = KnjDbUtils.query(db2, ps, new Object[] {student._schregno});
            	}
    		} catch (Exception e) {
    			log.error("exception!", e);
    		} finally {
    			DbUtils.closeQuietly(ps);
    		}
    		

    		try {
        		log.info("load attend shr.");
        		//出欠データ(遅刻回数)
        		final String sql = prestatementAttendSHR(param);
    			ps = db2.prepareStatement(sql);
            	for (final Student student : studentList) {
            		student._attendShrRowList = KnjDbUtils.query(db2, ps, new Object[] {student._schregno, student._schregno});
            	}
    		} catch (Exception e) {
    			log.error("exception!", e);
    		} finally {
    			DbUtils.closeQuietly(ps);
    		}
    		return studentList;
    	}

    	/** 
         *  PrepareStatement作成  学籍
         *     対象生徒全ての表
         */
        private static String prestatementRegd(Param param)
        {
            final StringBuffer stb = new StringBuffer();
            //異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO_A AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.ATTENDNO, T1.SEMESTER ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1 ");
            stb.append(    "INNER JOIN SEMESTER_MST T2 ON ");
            stb.append(        "    T1.YEAR = T2.YEAR ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(    "INNER JOIN SCHREG_REGD_GDAT GDAT ON ");
            stb.append(        "    GDAT.YEAR = T1.YEAR ");
            stb.append(        "AND GDAT.GRADE = T1.GRADE ");
            stb.append(    "LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ");
            stb.append(        "    ENTGRD.SCHREGNO = T1.SCHREGNO ");
            stb.append(        "AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(        "AND T1.SEMESTER = '"+param._gakki+"' ");
            stb.append(        "AND T1.GRADE||T1.HR_CLASS = '" + param._gradeHrClass + "' ");
            stb.append(        "AND T1.SCHREGNO IN" + param._categorySelected + " ");
                                    //在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
                                                 //転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合 
            stb.append(        "AND NOT ( ");
            stb.append(                           "    ((VALUE(ENTGRD.GRD_DIV, '-') IN('2','3') AND ENTGRD.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
            stb.append(                             "OR (VALUE(ENTGRD.ENT_DIV, '-') IN('4','5') AND ENTGRD.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
                                    //異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(    ") ");

            //通信欄の表
            stb.append(",T_HreportRemark AS ( ");
            stb.append(    "SELECT SEMESTER,SCHREGNO,COMMUNICATION ");
            stb.append(    "FROM   HREPORTREMARK_DAT ");
            stb.append(    "WHERE  YEAR = '" + param._year + "' AND SEMESTER <= '"+param._gakki+"' ");
            stb.append(    ") ");

            //メイン表
            stb.append("SELECT  T1.SCHREGNO, INT(T1.ATTENDNO) AS ATTENDNO, T2.HR_NAME, ");
            stb.append(        "T5.NAME, ");
            stb.append(        "T7.COMMUNICATION AS COMM1, T8.COMMUNICATION AS COMM2, T9.COMMUNICATION AS COMM3  ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append(        "INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append(        "INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = '" + param._year + "' AND ");
            stb.append(                                          "T2.SEMESTER = T1.SEMESTER AND ");
            stb.append(                                          "T2.GRADE || T2.HR_CLASS = '" + param._gradeHrClass + "' ");
            stb.append(        "LEFT JOIN T_HreportRemark T7 ON T7.SCHREGNO = T1.SCHREGNO AND T7.SEMESTER = '1' ");
            stb.append(        "LEFT JOIN T_HreportRemark T8 ON T8.SCHREGNO = T1.SCHREGNO AND T8.SEMESTER = '2' ");
            stb.append(        "LEFT JOIN T_HreportRemark T9 ON T9.SCHREGNO = T1.SCHREGNO AND T9.SEMESTER = '3' ");
            stb.append("ORDER BY ATTENDNO");
            return stb.toString();
        }
        
        /** 
         *  PrepareStatement作成 成績
         *     対象生徒全てのデータ表
         */
        private static String prestatementSubclass(final Param param)
        {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            //学籍の表（クラス）
            stb.append("HR_SCHNO AS(");
            stb.append(    "SELECT  T2.SCHREGNO, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO, ");
            stb.append(            "T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
            stb.append(    "FROM    SCHREG_REGD_DAT T2 ");
            stb.append(    "WHERE   T2.YEAR = '" + param._year + "' ");
            stb.append(        "AND T2.GRADE = '" + param._gradeHrClass.substring( 0, 2 ) + "' ");
            stb.append(        "AND T2.HR_CLASS = '" + param._gradeHrClass.substring( 2 ) + "' ");
            stb.append(        "AND T2.SEMESTER = (SELECT  MAX(SEMESTER) ");
            stb.append(                           "FROM    SCHREG_REGD_DAT W2 ");
            stb.append(                           "WHERE   W2.YEAR = '" + param._year + "' ");
            stb.append(                               "AND W2.SEMESTER <= '" + param._gakki + "' ");
            stb.append(                               "AND W2.SCHREGNO = T2.SCHREGNO) ");
            stb.append(") ");

            //学籍の表（生徒）
            stb.append(",SCHNO AS(");
            stb.append(    "SELECT  T2.SCHREGNO, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO, ");
            stb.append(            "T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
            stb.append(    "FROM    HR_SCHNO T2 ");
            stb.append(    "WHERE   T2.SCHREGNO = ? ");
            stb.append(") ");

            //講座の表（生徒別・学期別）
            stb.append(", HR_CHAIR_A AS(");
            stb.append(    "SELECT  S1.SCHREGNO,S2.SUBCLASSCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            ", S2.CLASSCD ");
            stb.append(            ", S2.SCHOOL_KIND ");
            stb.append(            ", S2.CURRICULUM_CD ");
        }
            stb.append(    "FROM    CHAIR_STD_DAT S1, ");
            stb.append(            "CHAIR_DAT S2 ");
            stb.append(    "WHERE   S1.YEAR = '" + param._year + "' ");
            stb.append(        "AND S1.SEMESTER <= '" + param._gakki + "' ");
            stb.append(        "AND S2.YEAR  = S1.YEAR ");
            stb.append(        "AND S2.SEMESTER = S1.SEMESTER ");
            stb.append(        "AND S2.CHAIRCD = S1.CHAIRCD ");
            stb.append(        "AND EXISTS( SELECT 'X' FROM HR_SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO) ");
            stb.append(        "AND (SUBSTR(SUBCLASSCD,1,2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR SUBSTR(SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "') ");
            stb.append(    "GROUP BY S1.SCHREGNO,S2.SUBCLASSCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            ", S2.CLASSCD ");
            stb.append(            ", S2.SCHOOL_KIND ");
            stb.append(            ", S2.CURRICULUM_CD ");
        }
            stb.append(") ");

            //講座の表（生徒）
            stb.append(", CHAIR_A AS(");
            stb.append(    "SELECT  SCHREGNO,SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", CLASSCD ");
                stb.append(            ", SCHOOL_KIND ");
                stb.append(            ", CURRICULUM_CD ");
            }
            stb.append(    "FROM    HR_CHAIR_A S1 ");
            stb.append(    "WHERE   EXISTS( SELECT 'X' FROM SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO) ");
            stb.append(") ");

            //読替先科目の表（名簿無し）
            stb.append(", NOT_CHAIR_REPLACE AS(");
            stb.append(    "SELECT  S1.GRADING_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", S1.GRADING_CLASSCD ");
                stb.append(            ", S1.GRADING_SCHOOL_KIND ");
                stb.append(            ", S1.GRADING_CURRICULUM_CD ");
            }
            stb.append(    "FROM    SUBCLASS_REPLACE_DAT S1 ");
            stb.append(    "INNER JOIN CHAIR_DAT S2 ON S2.YEAR = '" + param._year + "' ");
            stb.append(                           "AND S2.SEMESTER <= '" + param._gakki + "' ");
            stb.append(                           "AND S2.SUBCLASSCD = S1.ATTEND_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                           "AND S2.CLASSCD = S1.ATTEND_CLASSCD ");
                stb.append(                           "AND S2.SCHOOL_KIND = S1.ATTEND_SCHOOL_KIND ");
                stb.append(                           "AND S2.CURRICULUM_CD = S1.ATTEND_CURRICULUM_CD ");
            }
            stb.append(    "WHERE   S1.YEAR = '" + param._year + "' ");
            stb.append(        "AND S1.ANNUAL = '" + param._gradeHrClass.substring( 0, 2 ) + "' ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM CHAIR_STD_DAT S3 ");
            stb.append(                       "WHERE   S3.YEAR = '" + param._year + "' ");
            stb.append(                           "AND S3.SEMESTER = S2.SEMESTER ");
            stb.append(                           "AND S3.CHAIRCD = S2.CHAIRCD) ");
            stb.append(    "GROUP BY S1.GRADING_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", S1.GRADING_CLASSCD ");
                stb.append(            ", S1.GRADING_SCHOOL_KIND ");
                stb.append(            ", S1.GRADING_CURRICULUM_CD ");
            }
            stb.append(") ");

            //評価読替科目（生徒）
            stb.append(", SCHREG_SUBCLASS_REPLACE AS(");
            stb.append(    "SELECT  S1.GRADING_SUBCLASSCD, S1.ATTEND_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", S1.GRADING_CLASSCD ");
                stb.append(            ", S1.GRADING_SCHOOL_KIND ");
                stb.append(            ", S1.GRADING_CURRICULUM_CD ");
                stb.append(            ", S1.ATTEND_CLASSCD ");
                stb.append(            ", S1.ATTEND_SCHOOL_KIND ");
                stb.append(            ", S1.ATTEND_CURRICULUM_CD ");
            }
            stb.append(    "FROM    SUBCLASS_REPLACE_DAT S1 ");
            stb.append(    "WHERE   S1.YEAR = '" + param._year + "' ");
            stb.append(        "AND S1.ANNUAL = '" + param._gradeHrClass.substring( 0, 2 ) + "' ");
            stb.append(        "AND (EXISTS(SELECT  'X' FROM CHAIR_A S2 WHERE S2.SUBCLASSCD = S1.GRADING_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                   "AND S2.CLASSCD = S1.GRADING_CLASSCD ");
                stb.append(                   "AND S2.SCHOOL_KIND = S1.GRADING_SCHOOL_KIND ");
                stb.append(                   "AND S2.CURRICULUM_CD = S1.GRADING_CURRICULUM_CD ");
            }
            stb.append(                   ") ");
            stb.append(          "OR EXISTS(SELECT  'X' FROM NOT_CHAIR_REPLACE S2 WHERE S2.GRADING_SUBCLASSCD = S1.GRADING_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                   "AND S2.GRADING_CLASSCD = S1.GRADING_CLASSCD ");
                stb.append(                   "AND S2.GRADING_SCHOOL_KIND = S1.GRADING_SCHOOL_KIND ");
                stb.append(                   "AND S2.GRADING_CURRICULUM_CD = S1.GRADING_CURRICULUM_CD ");
            }
            stb.append(                   ") ");
            stb.append(            ") ");
            stb.append(") ");

            //成績明細データの表
            stb.append(",RECORD AS(");
            stb.append(    "SELECT  SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "CLASSCD, ");
                stb.append(            "SCHOOL_KIND, ");
                stb.append(            "CURRICULUM_CD, ");
            }
            stb.append(            "SUBCLASSCD, ");
            stb.append(            "CHAIRCD, ");
            stb.append(            "COMP_CREDIT, GET_CREDIT, ADD_CREDIT, ");
            stb.append(            "SEM1_INTR_SCORE, ");
            stb.append(            "SEM1_INTR_VALUE, ");
            stb.append(            "SEM1_TERM_SCORE, ");
            stb.append(            "SEM1_TERM_VALUE, ");
            stb.append(            "CASE WHEN SEM1_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM1_VALUE)) ELSE NULL END AS SEM1_VALUE, ");  
            stb.append(            "SEM2_INTR_SCORE, ");
            stb.append(            "SEM2_INTR_VALUE, ");
            stb.append(            "SEM2_TERM_SCORE, ");
            stb.append(            "SEM2_TERM_VALUE, ");
            stb.append(            "CASE WHEN SEM2_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM2_VALUE)) ELSE NULL END AS SEM2_VALUE, ");  
            stb.append(            "SEM3_INTR_SCORE, ");
            stb.append(            "SEM3_INTR_VALUE, ");
            stb.append(            "SEM3_TERM_SCORE, ");
            stb.append(            "SEM3_TERM_VALUE, ");
            stb.append(            "CASE WHEN SEM3_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM3_VALUE)) ELSE NULL END AS SEM3_VALUE, ");  
            stb.append(            "CASE WHEN GRAD_VALUE IS NOT NULL THEN RTRIM(CHAR(GRAD_VALUE)) ELSE NULL END AS GRAD_VALUE ");  
            stb.append(    "FROM    RECORD_DAT T1 ");
            stb.append(    "WHERE   YEAR = '" + param._year + "' ");
            stb.append(        "AND EXISTS(SELECT  'X' FROM SCHNO T2 WHERE T2.SCHREGNO = T1.SCHREGNO) ");
            stb.append(        "AND (SUBSTR(SUBCLASSCD,1,2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR SUBSTR(SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "') ");
            stb.append(") ");

            // テスト項目マスタの集計フラグ
            stb.append(" , TEST_COUNTFLG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.CHAIRCD, ");
            stb.append("         '2' AS DATADIV ");
            stb.append("     FROM ");
            stb.append("         SCH_CHR_TEST T1, ");
            stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR       = T1.YEAR ");
            stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
            stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("         AND T2.COUNTFLG   = '0' ");
            stb.append(" ) ");

            // 時間割データの表
            stb.append(", T_SCH_CHR_DAT AS(");
            stb.append(    "SELECT  T0.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.CHAIRCD, T1.DATADIV, T5.DI_CD, (CASE WHEN T4.SCHREGNO IS NOT NULL THEN '1' ELSE '0' END) AS IS_OFFDAYS ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", T2.CLASSCD ");
                stb.append(            ", T2.SCHOOL_KIND ");
                stb.append(            ", T2.CURRICULUM_CD ");
            }
            stb.append(    "FROM    SCH_CHR_DAT T1 ");
            stb.append(    "INNER JOIN SEMESTER_MST T3 ON T3.YEAR = T1.YEAR ");
            stb.append(        "AND T3.SEMESTER <> '9' ");
            stb.append(        "AND T1.EXECUTEDATE BETWEEN T3.SDATE AND T3.EDATE ");
            stb.append(    "INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append(        "AND T2.SEMESTER = T3.SEMESTER ");
            stb.append(        "AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append(    "INNER JOIN CHAIR_STD_DAT T0 ON T0.YEAR = T1.YEAR ");
            stb.append(        "AND T0.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T0.CHAIRCD = T2.CHAIRCD ");
            stb.append(        "AND T1.EXECUTEDATE BETWEEN T0.APPDATE AND T0.APPENDDATE ");
            stb.append(    "LEFT JOIN ATTEND_DAT T5 ON T5.SCHREGNO = T0.SCHREGNO ");
            stb.append(        "AND T1.EXECUTEDATE = T5.ATTENDDATE ");
            stb.append(        "AND T1.PERIODCD = T5.PERIODCD ");
            stb.append(        "AND T1.CHAIRCD = T5.CHAIRCD ");
            stb.append(    "LEFT JOIN SCHREG_TRANSFER_DAT T4 ON T4.SCHREGNO = T0.SCHREGNO ");
            stb.append(        "AND T1.EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE ");
            stb.append(        "AND T4.TRANSFERCD = '2' ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(        "AND T1.EXECUTEDATE BETWEEN '" + param._divideAttendDate + "' AND '" + param._date + "' ");
            stb.append(        "AND T3.SEMESTER <= '" + param._gakki + "' ");
            //                      学籍不在日を除外
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6 ");
            stb.append(                       "WHERE   T6.SCHREGNO = T0.SCHREGNO ");
            stb.append(                           "AND (( T6.ENT_DIV IN('4','5') AND T1.EXECUTEDATE < T6.ENT_DATE ) ");
            stb.append(                             "OR ( T6.GRD_DIV IN('2','3') AND T1.EXECUTEDATE > T6.GRD_DATE )) ) ");
            //                      留学日、休学日を除外
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ");
            stb.append(                       "WHERE   T7.SCHREGNO = T0.SCHREGNO ");
            stb.append(                           "AND T7.TRANSFERCD <> '2' AND T1.EXECUTEDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ");
            // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + param._year + "' AND ATDD.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T0.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND ATDD.REP_DI_CD = '27' ");
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + param._year + "' AND ATDD.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T0.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND ATDD.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append(") ");

            //生徒・科目・学期別欠課集計の表（出欠データと集計テーブルを合算）
            stb.append(", SCH_ATTEND_SUM AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", T1.CLASSCD ");
                stb.append(            ", T1.SCHOOL_KIND ");
                stb.append(            ", T1.CURRICULUM_CD ");
            }
            stb.append("           ,SUM(CASE WHEN (CASE WHEN ATDD.REP_DI_CD IN ('29','30','31') THEN VALUE(ATDD.ATSUB_REPL_DI_CD, ATDD.REP_DI_CD) ELSE ATDD.REP_DI_CD END) IN('4','5','6','14','11','12','13'");
            if ("1".equals(param._knjSchoolMst._subAbsent)) {
                stb.append(          ",'1','8'");
            }
            if ("1".equals(param._knjSchoolMst._subSuspend)) {
                stb.append(          ",'2','9'");
            }
            if ("1".equals(param._knjSchoolMst._subMourning)) {
                stb.append(          ",'3','10'");
            }
            if ("true".equals(param._useVirus)) {
                if ("1".equals(param._knjSchoolMst._subVirus)) {
                    stb.append(          ",'19','20'");
                }
            }
            if ("true".equals(param._useKoudome)) {
                if ("1".equals(param._knjSchoolMst._subKoudome)) {
                    stb.append(          ",'25','26'");
                }
            }
            stb.append(                ") ");
            if ("1".equals(param._knjSchoolMst._subOffDays)) {
                stb.append(            "OR (IS_OFFDAYS = '1')");
            }
            stb.append(            " THEN 1 ELSE 0 END)AS ABSENT1 ");            
            stb.append("           ,SUM(CASE WHEN ATDD.REP_DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(ATDD.MULTIPLY, '1')) ELSE 0 END) AS LATE_EARLY ");
            stb.append(    "FROM T_SCH_CHR_DAT T1 ");
            stb.append("               LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + param._year + "' AND ATDD.DI_CD = T1.DI_CD ");
            stb.append(         ", SCHNO T0 ");
            stb.append(    "WHERE   T1.SCHREGNO = T0.SCHREGNO ");
            if( param.definecode.useschchrcountflg ){
                stb.append(    "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
                stb.append(                    "WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE ");
                stb.append(                        "AND T4.PERIODCD = T1.PERIODCD ");
                stb.append(                        "AND T4.CHAIRCD = T1.CHAIRCD ");
                stb.append(                        "AND T1.DATADIV IN ('0', '1') ");
                stb.append(                        "AND T4.GRADE = '" + param._gradeHrClass.substring( 0, 2 ) + "' ");
                stb.append(                        "AND T4.HR_CLASS = T0.HR_CLASS ");
                stb.append(                        "AND T4.COUNTFLG = '0') ");
                stb.append("    AND NOT EXISTS(SELECT  'X' FROM TEST_COUNTFLG TEST ");
                stb.append("                       WHERE ");
                stb.append("                           TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
                stb.append("                           AND TEST.PERIODCD = T1.PERIODCD ");
                stb.append("                           AND TEST.CHAIRCD  = T1.CHAIRCD ");
                stb.append("                           AND TEST.DATADIV  = T1.DATADIV) ");
            }
            stb.append(    "GROUP BY T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", T1.CLASSCD ");
                stb.append(            ", T1.SCHOOL_KIND ");
                stb.append(            ", T1.CURRICULUM_CD ");
            }

            stb.append(    "UNION ALL ");
            stb.append(    "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, SEMESTER ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", T1.CLASSCD ");
                stb.append(            ", T1.SCHOOL_KIND ");
                stb.append(            ", T1.CURRICULUM_CD ");
            }
            stb.append(           ",SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0)");
            if ("1".equals(param._knjSchoolMst._subAbsent)) {
                stb.append(          "+ VALUE(ABSENT,0)");
            }
            if ("1".equals(param._knjSchoolMst._subSuspend)) {
                stb.append(          "+ VALUE(SUSPEND,0)");
            }
            if ("1".equals(param._knjSchoolMst._subMourning)) {
                stb.append(          "+ VALUE(MOURNING,0)");
            }
            if ("1".equals(param._knjSchoolMst._subOffDays)) {
                stb.append(          "+ VALUE(OFFDAYS,0)");
            }
            if ("true".equals(param._useVirus)) {
                if ("1".equals(param._knjSchoolMst._subVirus)) {
                    stb.append(          "+ VALUE(VIRUS,0)");
                }
            }
            if ("true".equals(param._useKoudome)) {
                if ("1".equals(param._knjSchoolMst._subKoudome)) {
                    stb.append(          "+ VALUE(KOUDOME,0)");
                }
            }
            stb.append(               ") AS ABSENT1 ");            
            stb.append(           ",SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
            stb.append(    "FROM    ATTEND_SUBCLASS_DAT T1 ");
            stb.append(    "WHERE   YEAR = '" + param._year + "' ");
            stb.append(        "AND SEMESTER <= '" + param._gakki + "' ");
            stb.append(        "AND (CASE WHEN INT(T1.MONTH) < 4 THEN RTRIM(CHAR(INT(T1.SEMESTER) + 1 )) ELSE T1.SEMESTER END )||T1.MONTH <= '" + KNJC053_BASE.retSemesterMonthValue (param._divideAttendMonth ) + "' ");   
            stb.append(        "AND EXISTS(SELECT  'X' FROM SCHNO T2 ");
            stb.append(                   "WHERE   T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(                   "GROUP BY SCHREGNO) ");
            stb.append(    "GROUP BY T1.SCHREGNO, T1.SEMESTER, T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", T1.CLASSCD ");
                stb.append(            ", T1.SCHOOL_KIND ");
                stb.append(            ", T1.CURRICULUM_CD ");
            }
            stb.append(") ");

            //ペナルティー欠課を加味した生徒欠課集計の表（出欠データと集計テーブルを合算）
            if( param.definecode.absent_cov == 1 ){
                //学期でペナルティ欠課を算出する場合
                stb.append(", ATTEND_B AS(");
                stb.append(       "SELECT  SCHREGNO, SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(            ", CLASSCD ");
                    stb.append(            ", SCHOOL_KIND ");
                    stb.append(            ", CURRICULUM_CD ");
                }
                stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM1 ");
                stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM2 ");
                stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM3 ");
                stb.append(              ",VALUE(SUM(ABSENT),0) AS ABSENT_SEM9 ");
                stb.append(       "FROM(   SELECT  SCHREGNO, SUBCLASSCD, SEMESTER ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(            ", CLASSCD ");
                    stb.append(            ", SCHOOL_KIND ");
                    stb.append(            ", CURRICULUM_CD ");
                }
                stb.append(                      ",VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + param.definecode.absent_cov_late + " AS ABSENT ");
                stb.append(               "FROM    SCH_ATTEND_SUM T1 ");
                stb.append(               "GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(            ", CLASSCD ");
                    stb.append(            ", SCHOOL_KIND ");
                    stb.append(            ", CURRICULUM_CD ");
                }
                stb.append(           ")T1 ");
                stb.append(       "GROUP BY SCHREGNO, SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(            ", CLASSCD ");
                    stb.append(            ", SCHOOL_KIND ");
                    stb.append(            ", CURRICULUM_CD ");
                }
                stb.append(") ");
            } else if( param.definecode.absent_cov == 2 ){
                //通年でペナルティ欠課を算出する場合 
                //学期の欠課時数は学期別で換算したペナルティ欠課を加算、学年の欠課時数は年間で換算する
                stb.append(", ATTEND_B AS(");
                stb.append(       "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.ABSENT_SEM9 ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(            ", T1.CLASSCD ");
                    stb.append(            ", T1.SCHOOL_KIND ");
                    stb.append(            ", T1.CURRICULUM_CD ");
                }
                stb.append(              ",T2.ABSENT_SEM1, T2.ABSENT_SEM2, T2.ABSENT_SEM3 ");
                stb.append(       "FROM (");
                stb.append(            "SELECT  SCHREGNO, SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(            ", CLASSCD ");
                    stb.append(            ", SCHOOL_KIND ");
                    stb.append(            ", CURRICULUM_CD ");
                }
                stb.append(                   ",VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + param.definecode.absent_cov_late + " AS ABSENT_SEM9 ");
                stb.append(            "FROM    SCH_ATTEND_SUM T1 ");
                stb.append(            "GROUP BY SCHREGNO, SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(            ", CLASSCD ");
                    stb.append(            ", SCHOOL_KIND ");
                    stb.append(            ", CURRICULUM_CD ");
                }
                stb.append(       ")T1, (");
                stb.append(            "SELECT  SCHREGNO, SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(            ", CLASSCD ");
                    stb.append(            ", SCHOOL_KIND ");
                    stb.append(            ", CURRICULUM_CD ");
                }
                stb.append(                   ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM1 ");
                stb.append(                   ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM2 ");
                stb.append(                   ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM3 ");
                stb.append(            "FROM(   SELECT  SCHREGNO, SUBCLASSCD, SEMESTER ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(            ", CLASSCD ");
                    stb.append(            ", SCHOOL_KIND ");
                    stb.append(            ", CURRICULUM_CD ");
                }
                stb.append(                           ",VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + param.definecode.absent_cov_late + " AS ABSENT ");
                stb.append(                    "FROM    SCH_ATTEND_SUM T1 ");
                stb.append(                    "GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(            ", CLASSCD ");
                    stb.append(            ", SCHOOL_KIND ");
                    stb.append(            ", CURRICULUM_CD ");
                }
                stb.append(                ")T1 ");
                stb.append(            "GROUP BY SCHREGNO, SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(            ", CLASSCD ");
                    stb.append(            ", SCHOOL_KIND ");
                    stb.append(            ", CURRICULUM_CD ");
                }
                stb.append(       ")T2 ");
                stb.append(       "WHERE T1.SCHREGNO = T2.SCHREGNO AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(            "AND T1.CLASSCD = T2.CLASSCD ");
                    stb.append(            "AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                    stb.append(            "AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
                }
                stb.append(") ");
            } else{
                //ペナルティ欠課なしの場合
                stb.append(", ATTEND_B AS(");
                stb.append(       "SELECT  SCHREGNO, SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(            ", CLASSCD ");
                    stb.append(            ", SCHOOL_KIND ");
                    stb.append(            ", CURRICULUM_CD ");
                }
                stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM1 ");
                stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM2 ");
                stb.append(              ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM3 ");
                stb.append(              ",VALUE(SUM(ABSENT1),0) AS ABSENT_SEM9 ");
                stb.append(       "FROM    SCH_ATTEND_SUM T1 ");
                stb.append(       "GROUP BY SCHREGNO, SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(            ", CLASSCD ");
                    stb.append(            ", SCHOOL_KIND ");
                    stb.append(            ", CURRICULUM_CD ");
                }
                stb.append(") ");
            }

            //読替先のペナルティー欠課を加味した生徒欠課集計の表
            stb.append(", ATTEND_B_REPLACE AS(");
            stb.append(    "SELECT  S2.SCHREGNO, S1.GRADING_SUBCLASSCD AS SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", S1.GRADING_CLASSCD AS CLASSCD ");
                stb.append(            ", S1.GRADING_SCHOOL_KIND AS SCHOOL_KIND ");
                stb.append(            ", S1.GRADING_CURRICULUM_CD AS CURRICULUM_CD ");
            }
            stb.append(           ",SUM(S2.ABSENT_SEM9) AS ABSENT_SEM9 ");
            stb.append(    "FROM    SUBCLASS_REPLACE_DAT S1, ATTEND_B S2 ");
            stb.append(    "WHERE   S1.ATTEND_SUBCLASSCD = S2.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(        "AND S1.ATTEND_CLASSCD = S2.CLASSCD ");
                stb.append(        "AND S1.ATTEND_SCHOOL_KIND = S2.SCHOOL_KIND ");
                stb.append(        "AND S1.ATTEND_CURRICULUM_CD = S2.CURRICULUM_CD ");
            }
            stb.append(        "AND S1.YEAR = '" + param._year + "' ");
            stb.append(        "AND S1.ANNUAL = '" + param._gradeHrClass.substring( 0, 2 ) + "' ");  
            stb.append(    "GROUP BY S2.SCHREGNO, S1.GRADING_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", S1.GRADING_CLASSCD ");
                stb.append(            ", S1.GRADING_SCHOOL_KIND ");
                stb.append(            ", S1.GRADING_CURRICULUM_CD ");
            }
            stb.append(") ");

            //無条件履修修得フラグがオンの科目の表
            stb.append(",CREDITS_UNCONDITION AS(");
            stb.append(    "SELECT  SCHREGNO, SUBCLASSCD, CREDITS ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", CLASSCD ");
                stb.append(            ", SCHOOL_KIND ");
                stb.append(            ", CURRICULUM_CD ");
            }
            stb.append(    "FROM    CREDIT_MST T1, SCHNO T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(        "AND T1.GRADE = T2.GRADE ");
            stb.append(        "AND T1.COURSECD = T2.COURSECD ");
            stb.append(        "AND T1.MAJORCD = T2.MAJORCD ");
            stb.append(        "AND T1.COURSECODE = T2.COURSECODE ");
            stb.append(        "AND VALUE(T1.COMP_UNCONDITION_FLG,'0') = '1' ");
            stb.append(        "AND NOT EXISTS(SELECT 'X' FROM RECORD T3 WHERE T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "AND T3.CLASSCD = T1.CLASSCD  ");
                stb.append(            "AND T3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append(            "AND T3.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append(                             "AND T3.SCHREGNO = T2.SCHREGNO) ");
            stb.append(") ");

            //科目数カウント
            stb.append(",SUBCLASSNUM AS(");
            stb.append(    "SELECT  SCHREGNO ");
            stb.append(           ",SUM(CASE WHEN SUBSTR(SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "' THEN 1 ELSE NULL END) AS NUM90 ");
            stb.append(           ",SUM(CASE WHEN SUBSTR(SUBCLASSCD,1,2) != '" + KNJDefineSchool.subject_T + "' THEN 1 ELSE NULL END) AS NUMTOTAL ");
            stb.append(    "FROM (");
            stb.append(          "SELECT  S1.SCHREGNO, S1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", S1.CLASSCD ");
                stb.append(            ", S1.SCHOOL_KIND ");
                stb.append(            ", S1.CURRICULUM_CD ");
            }
            stb.append(          "FROM    CHAIR_A S1 ");
            stb.append(          "WHERE   EXISTS(SELECT 'X' FROM SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(              "AND EXISTS(SELECT 'X' FROM RECORD S2 WHERE S2.SCHREGNO = S1.SCHREGNO AND S2.CLASSCD || S2.SCHOOL_KIND || S2.CURRICULUM_CD || S2.SUBCLASSCD = S1.CLASSCD || S1.SCHOOL_KIND || S1.CURRICULUM_CD || S1.SUBCLASSCD) ");
            } else {
                stb.append(              "AND EXISTS(SELECT 'X' FROM RECORD S2 WHERE S2.SCHREGNO = S1.SCHREGNO AND S2.SUBCLASSCD = S1.SUBCLASSCD) ");
            }
            stb.append(          "UNION ");
                                  //受講名簿が１件もない場合の評価読替科目
            stb.append(          "SELECT  SCHREGNO, GRADING_SUBCLASSCD AS SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", GRADING_CLASSCD AS CLASSCD ");
                stb.append(            ", GRADING_SCHOOL_KIND AS SCHOOL_KIND ");
                stb.append(            ", GRADING_CURRICULUM_CD AS CURRICULUM_CD ");
            }
            stb.append(          "FROM    NOT_CHAIR_REPLACE S1, SCHNO S2 ");
                                  //無条件履修取得の科目
            stb.append(          "UNION ");
            stb.append(          "SELECT  SCHREGNO, SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", CLASSCD ");
                stb.append(            ", SCHOOL_KIND ");
                stb.append(            ", CURRICULUM_CD ");
            }
            stb.append(          "FROM    CREDITS_UNCONDITION S1 ");
            stb.append(          ")T1 ");
            stb.append(    "GROUP BY SCHREGNO ");
            stb.append(") ");

            //定期考査の出欠の表１
            stb.append(",TEST_ATTEND_A AS ( ");
            stb.append(    "SELECT  T1.SEMESTER AS SEMES, T1.TESTKINDCD AS TESTKINDCD, T3.SUBCLASSCD,  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " T3.CLASSCD, ");
                stb.append(            " T3.SCHOOL_KIND, ");
                stb.append(            " T3.CURRICULUM_CD, ");
            }
            stb.append(            "T2.SCHREGNO, DI_CD  ");
            stb.append(    "FROM    SCH_CHR_TEST T1,ATTEND_DAT T2,CHAIR_DAT T3  ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' AND  ");
            stb.append(            "T1.SEMESTER <= '" + param._gakki + "' AND  ");
            stb.append(            "T1.CHAIRCD = T2.CHAIRCD AND  ");
            stb.append(            "T3.YEAR = '" + param._year + "' AND  ");
            stb.append(            "T3.SEMESTER = T1.SEMESTER AND  ");
            stb.append(            "T3.CHAIRCD = T2.CHAIRCD AND  ");
            stb.append(            "T2.SCHREGNO = ? AND  ");
            stb.append(            "T2.YEAR = '" + param._year + "' AND  ");
            stb.append(            "T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ");
            stb.append(            "T2.DI_CD IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ");
            stb.append(    ") ");

            //定期考査の出欠の表２
            stb.append(",TEST_ATTEND_B AS ( ");
            stb.append(    "SELECT  SCHREGNO, SUBCLASSCD,  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " CLASSCD, ");
                stb.append(            " SCHOOL_KIND, ");
                stb.append(            " CURRICULUM_CD, ");
            }
            stb.append(            "MIN(CASE WHEN SEMES = '1' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SEM1_INTR_ATTEND,  ");
            stb.append(            "MIN(CASE WHEN SEMES = '1' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SEM1_TERM_ATTEND,  ");
            stb.append(            "MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SEM2_INTR_ATTEND,  ");
            stb.append(            "MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SEM2_TERM_ATTEND,  ");
            stb.append(            "MIN(CASE WHEN SEMES = '3' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SEM3_INTR_ATTEND,  ");
            stb.append(            "MIN(CASE WHEN SEMES = '3' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SEM3_TERM_ATTEND  ");
            stb.append(    "FROM    TEST_ATTEND_A  ");
            stb.append(    "GROUP BY SCHREGNO, SUBCLASSCD  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", CLASSCD ");
                stb.append(            ", SCHOOL_KIND ");
                stb.append(            ", CURRICULUM_CD ");
            }
            stb.append(    ") ");


            //メイン表
            stb.append("SELECT  T2.ATTENDNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " T5.CLASSCD || T5.SCHOOL_KIND || T5.CURRICULUM_CD || T5.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append(            " T5.SUBCLASSCD, ");
            }
            stb.append(        "T4.SUBCLASSNAME, T7.CLASSNAME AS CHAIRNAME, ");  
            stb.append(        "T6.CREDITS, ");
            if (param._knjSchoolMst.isJitu()) {
                stb.append(        " VALUE(T11.COMP_ABSENCE_HIGH, 99) ");
            } else {
                stb.append(        " VALUE(T6.ABSENCE_HIGH, 99) ");
            }
            stb.append(            "AS ABSENCE_HIGH, ");
            stb.append(        "CASE WHEN VALUE(T6.COMP_UNCONDITION_FLG,'0') = '1' AND T1.COMP_CREDIT IS NULL THEN T6.CREDITS ELSE T1.COMP_CREDIT END AS COMP_CREDIT, ");  
            stb.append(        "CASE WHEN VALUE(T6.COMP_UNCONDITION_FLG,'0') = '1' AND T1.GET_CREDIT IS NULL THEN T6.CREDITS ELSE T1.GET_CREDIT END AS GET_CREDIT, ");  
            stb.append(        "T1.ADD_CREDIT, ");
            stb.append(        "T1.COMP_CREDIT AS ON_RECORD_COMP, "); 
            stb.append(        "T1.GET_CREDIT AS ON_RECORD_GET, "); 
            stb.append(        "VALUE(T6.COMP_UNCONDITION_FLG,'0')AS COMP_UNCONDITION_FLG, ");  //値が'1'の場合無条件に単位を与える
            stb.append(        "SEM1_INTR_SCORE, SEM1_INTR_VALUE, ");
            stb.append(        "SEM1_TERM_SCORE, SEM1_TERM_VALUE, ");
            stb.append(        "SEM1_VALUE, ");
            stb.append(        "SEM2_INTR_SCORE, SEM2_INTR_VALUE, ");
            stb.append(        "SEM2_TERM_SCORE, SEM2_TERM_VALUE, ");
            stb.append(        "SEM2_VALUE, ");
            stb.append(        "SEM3_INTR_SCORE, SEM3_INTR_VALUE, ");
            stb.append(        "SEM3_TERM_SCORE, SEM3_TERM_VALUE, ");
            stb.append(        "SEM3_VALUE, ");

            stb.append(        "CASE WHEN SEM1_INTR_ATTEND IS NULL AND SEM1_TERM_ATTEND IS NULL THEN '' ELSE '/' END AS SEM1_ATTEND, ");
            stb.append(        "CASE WHEN SEM2_INTR_ATTEND IS NULL AND SEM2_TERM_ATTEND IS NULL THEN '' ELSE '/' END AS SEM2_ATTEND, ");
            stb.append(        "CASE WHEN SEM1_INTR_ATTEND IS NULL AND SEM1_TERM_ATTEND IS NULL AND SEM2_INTR_ATTEND IS NULL AND SEM2_TERM_ATTEND IS NULL AND SEM3_TERM_ATTEND IS NULL THEN '' ELSE '/' END AS GRAD_ATTEND, ");

                                //GRAD_VALUEの記号対応 <= RECORD表でGRAD_VALUEはCHARとしている
            stb.append(        "T1.GRAD_VALUE, ");
            stb.append(        "ABSENT_SEM1, ABSENT_SEM2, ABSENT_SEM3, ");
            stb.append(        "T3.ABSENT_SEM9 AS ABSENT_TOTAL ");
            stb.append(       ",T5.REPLACEFLG, T8.ABSENT_SEM9 AS REPLACE_ABSENT_TOTAL "); 
            stb.append(       ",VALUE(T9.NUM90,0) AS NUM90, VALUE(T9.NUMTOTAL,0) AS NUMTOTAL ");  

            stb.append("FROM   SCHNO T2 ");

            stb.append("LEFT JOIN (");
                            //任意の生徒が受講している科目
            stb.append(    "SELECT  S1.SCHREGNO, S1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", S1.CLASSCD ");
                stb.append(            ", S1.SCHOOL_KIND ");
                stb.append(            ", S1.CURRICULUM_CD ");
            }
            stb.append(           ",CASE WHEN S2.SUBCLASSCD IS NOT NULL THEN 9 ");
            stb.append(                 "WHEN S4.SUBCLASSCD IS NOT NULL THEN 2 ");
            stb.append(                 "WHEN S3.SUBCLASSCD IS NOT NULL THEN 1 ");
            stb.append(                 "ELSE 0 END AS REPLACEFLG ");
            stb.append(    "FROM    CHAIR_A S1 ");
            stb.append(    "LEFT JOIN(");
                                //評価読替科目
            stb.append(        "SELECT  S1.GRADING_SUBCLASSCD AS SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", S1.GRADING_CLASSCD AS CLASSCD ");
                stb.append(            ", S1.GRADING_SCHOOL_KIND AS SCHOOL_KIND ");
                stb.append(            ", S1.GRADING_CURRICULUM_CD AS CURRICULUM_CD ");
            }
            stb.append(        "FROM    SCHREG_SUBCLASS_REPLACE S1 ");
            stb.append(        "GROUP BY S1.GRADING_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", S1.GRADING_CLASSCD ");
                stb.append(            ", S1.GRADING_SCHOOL_KIND ");
                stb.append(            ", S1.GRADING_CURRICULUM_CD ");
            }
            stb.append(    ")S2 ON S2.SUBCLASSCD = S1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " AND S2.CLASSCD = S1.CLASSCD ");
                stb.append(            " AND S2.SCHOOL_KIND = S1.SCHOOL_KIND ");
                stb.append(            " AND S2.CURRICULUM_CD = S1.CURRICULUM_CD ");
            }
            stb.append(    "LEFT JOIN(");
                                //評価読替元の科目
            stb.append(        "SELECT  S1.ATTEND_SUBCLASSCD AS SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", S1.ATTEND_CLASSCD AS CLASSCD ");
                stb.append(            ", S1.ATTEND_SCHOOL_KIND AS SCHOOL_KIND ");
                stb.append(            ", S1.ATTEND_CURRICULUM_CD AS CURRICULUM_CD ");
            }
            stb.append(        "FROM    SCHREG_SUBCLASS_REPLACE S1 ");
            stb.append(        "GROUP BY S1.ATTEND_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", S1.ATTEND_CLASSCD ");
                stb.append(            ", S1.ATTEND_SCHOOL_KIND ");
                stb.append(            ", S1.ATTEND_CURRICULUM_CD ");
            }
            stb.append(    ")S3 ON S3.SUBCLASSCD = S1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " AND S3.CLASSCD = S1.CLASSCD ");
                stb.append(            " AND S3.SCHOOL_KIND = S1.SCHOOL_KIND ");
                stb.append(            " AND S3.CURRICULUM_CD = S1.CURRICULUM_CD ");
            }
            stb.append(    "LEFT JOIN(");
                                //評価読替科目に学年評定がある評価読替元の科目 => 学年の値は非表示のため抽出
            stb.append(        "SELECT  S1.ATTEND_SUBCLASSCD AS SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", S1.ATTEND_CLASSCD AS CLASSCD ");
                stb.append(            ", S1.ATTEND_SCHOOL_KIND AS SCHOOL_KIND ");
                stb.append(            ", S1.ATTEND_CURRICULUM_CD AS CURRICULUM_CD ");
            }
            stb.append(        "FROM    SCHREG_SUBCLASS_REPLACE S1 ");
            stb.append(        "WHERE   EXISTS(SELECT  'X' FROM RECORD_DAT S2 ");
            stb.append(                       "WHERE   S2.SUBCLASSCD = S1.GRADING_SUBCLASSCD AND S2.GRAD_VALUE IS NOT NULL ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " AND S2.CLASSCD = S1.GRADING_CLASSCD ");
                stb.append(            " AND S2.SCHOOL_KIND = S1.GRADING_SCHOOL_KIND ");
                stb.append(            " AND S2.CURRICULUM_CD = S1.GRADING_CURRICULUM_CD ");
            }
            stb.append(                       "GROUP BY ");
            stb.append(                            "S2.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", S2.CLASSCD ");
                stb.append(            ", S2.SCHOOL_KIND ");
                stb.append(            ", S2.CURRICULUM_CD ");
            }
            stb.append(                            ") ");
            stb.append(        "GROUP BY S1.ATTEND_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", S1.ATTEND_CLASSCD ");
                stb.append(            ", S1.ATTEND_SCHOOL_KIND ");
                stb.append(            ", S1.ATTEND_CURRICULUM_CD ");
            }
            stb.append(    ")S4 ON S4.SUBCLASSCD = S1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " AND S4.CLASSCD = S1.CLASSCD ");
                stb.append(            " AND S4.SCHOOL_KIND = S1.SCHOOL_KIND ");
                stb.append(            " AND S4.CURRICULUM_CD = S1.CURRICULUM_CD ");
            }
            stb.append(    "WHERE   EXISTS(SELECT 'X' FROM SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO) ");
            stb.append(        "AND EXISTS(SELECT 'X' FROM RECORD S2 WHERE S2.SCHREGNO = S1.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                         "AND S2.CLASSCD = S1.CLASSCD ");
                stb.append(                         "AND S2.SCHOOL_KIND = S1.SCHOOL_KIND ");
                stb.append(                         "AND S2.CURRICULUM_CD = S1.CURRICULUM_CD ");
            }
            stb.append(                         "AND S2.SUBCLASSCD = S1.SUBCLASSCD) ");
            stb.append(    "UNION ");
                            //受講名簿が１件もない場合の評価読替科目
            stb.append(    "SELECT  SCHREGNO, GRADING_SUBCLASSCD AS SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                         ", GRADING_CLASSCD AS CLASSCD ");
                stb.append(                         ", GRADING_SCHOOL_KIND AS SCHOOL_KIND ");
                stb.append(                         ", GRADING_CURRICULUM_CD AS CURRICULUM_CD ");
            }
            stb.append(    ", 9 AS REPLACEFLG ");
            stb.append(    "FROM    NOT_CHAIR_REPLACE S1, SCHNO S2 ");

                            //無条件履修取得の科目
            stb.append(    "UNION ");
            stb.append(    "SELECT  SCHREGNO, SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                         ", CLASSCD ");
                stb.append(                         ", SCHOOL_KIND ");
                stb.append(                         ", CURRICULUM_CD ");
            }
            stb.append(    ", 0 AS REPLACEFLG ");
            stb.append(    "FROM    CREDITS_UNCONDITION S1 ");

            stb.append(")T5 ON T5.SCHREGNO = T2.SCHREGNO ");

            stb.append("LEFT JOIN RECORD T1 ON T1.SCHREGNO = T5.SCHREGNO AND T1.SUBCLASSCD = T5.SUBCLASSCD "); 
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(                  " AND T1.CLASSCD = T5.CLASSCD "); 
            stb.append(                  " AND T1.SCHOOL_KIND = T5.SCHOOL_KIND "); 
            stb.append(                  " AND T1.CURRICULUM_CD = T5.CURRICULUM_CD "); 
        }
            stb.append("LEFT JOIN ATTEND_B T3 ON T3.SCHREGNO = T5.SCHREGNO AND T3.SUBCLASSCD = T5.SUBCLASSCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(                  " AND T3.CLASSCD = T5.CLASSCD "); 
            stb.append(                  " AND T3.SCHOOL_KIND = T5.SCHOOL_KIND "); 
            stb.append(                  " AND T3.CURRICULUM_CD = T5.CURRICULUM_CD "); 
        }
            stb.append("LEFT JOIN ATTEND_B_REPLACE T8 ON T8.SCHREGNO = T5.SCHREGNO AND T8.SUBCLASSCD = T5.SUBCLASSCD "); 
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(                  " AND T8.CLASSCD = T5.CLASSCD "); 
            stb.append(                  " AND T8.SCHOOL_KIND = T5.SCHOOL_KIND "); 
            stb.append(                  " AND T8.CURRICULUM_CD = T5.CURRICULUM_CD "); 
        }

            stb.append("LEFT JOIN TEST_ATTEND_B T10 ON T10.SCHREGNO = T5.SCHREGNO AND T10.SUBCLASSCD = T5.SUBCLASSCD "); 
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(                  " AND T10.CLASSCD = T5.CLASSCD "); 
            stb.append(                  " AND T10.SCHOOL_KIND = T5.SCHOOL_KIND "); 
            stb.append(                  " AND T10.CURRICULUM_CD = T5.CURRICULUM_CD "); 
        }

            stb.append("LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T5.SUBCLASSCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(                  " AND T4.CLASSCD = T5.CLASSCD "); 
            stb.append(                  " AND T4.SCHOOL_KIND = T5.SCHOOL_KIND "); 
            stb.append(                  " AND T4.CURRICULUM_CD = T5.CURRICULUM_CD "); 
        }
            stb.append("LEFT JOIN CLASS_MST T7 ON T7.CLASSCD = SUBSTR(T5.SUBCLASSCD,1,2) ");  
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(                  " AND T7.SCHOOL_KIND = T5.SCHOOL_KIND "); 
        }
            stb.append("LEFT JOIN CREDIT_MST T6 ON T6.YEAR = '" + param._year + "' ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(                       "AND T6.CLASSCD = T5.CLASSCD ");
            stb.append(                       "AND T6.SCHOOL_KIND = T5.SCHOOL_KIND ");
            stb.append(                       "AND T6.CURRICULUM_CD = T5.CURRICULUM_CD ");
        }
            stb.append(                       "AND T6.SUBCLASSCD = T5.SUBCLASSCD ");
            stb.append(                       "AND T6.GRADE = T2.GRADE ");
            stb.append(                       "AND T6.COURSECD = T2.COURSECD ");
            stb.append(                       "AND T6.MAJORCD = T2.MAJORCD ");
            stb.append(                       "AND T6.COURSECODE = T2.COURSECODE ");
            if (param._knjSchoolMst.isJitu()) {
                stb.append("LEFT JOIN SCHREG_ABSENCE_HIGH_DAT T11 ON T11.YEAR = '" + param._year + "' ");
                stb.append(                       "AND T11.SCHREGNO = T2.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                       "AND T11.CLASSCD = T5.CLASSCD ");
                stb.append(                       "AND T11.SCHOOL_KIND = T5.SCHOOL_KIND ");
                stb.append(                       "AND T11.CURRICULUM_CD = T5.CURRICULUM_CD ");
            }
                stb.append(                       "AND T11.SUBCLASSCD = T5.SUBCLASSCD ");
                stb.append(                       "AND T11.DIV = '1' ");
            }

            stb.append("LEFT JOIN SUBCLASSNUM T9 ON T9.SCHREGNO = T5.SCHREGNO ");  

            stb.append("ORDER BY ATTENDNO ");  
            stb.append(            ", T5.CLASSCD || T5.SCHOOL_KIND || T5.CURRICULUM_CD || T5.SUBCLASSCD ");
            return stb.toString();
        }
        
        /** 
         *  PrepareStatement作成 生徒別成績明細データ(順位)作成
         *
         */
        private static String prestatementRank(final Param param)
        {
            final StringBuffer stb = new StringBuffer();
            //学籍の表（クラス）
            stb.append("WITH SCHNO AS( ");
            stb.append(    "SELECT  SCHREGNO ");
            stb.append(    "FROM    SCHREG_REGD_DAT ");
            stb.append(    "WHERE   YEAR = '" + param._year + "'  ");
            stb.append(        "AND SEMESTER <= '" + param._gakki + "'  ");
            stb.append(        "AND GRADE||HR_CLASS = '" + param._gradeHrClass + "'  ");
            stb.append(    "GROUP BY SCHREGNO ");
            stb.append(    ") ");

            //成績明細データ(順位)の表
            stb.append(",RECORD AS ( ");
            stb.append(    "SELECT  T1.SCHREGNO ");
            stb.append(           ",SUM(SEM1_VALUE) AS SEM1_SUM ");
            stb.append(           ",SUM(SEM2_VALUE) AS SEM2_SUM ");
            stb.append(           ",SUM(GRAD_VALUE) AS GRAD_SUM ");
            stb.append(           ",RANK() OVER (ORDER BY VALUE(ROUND(AVG(FLOAT(SEM1_VALUE)) * 10, 0) / 10, 0) DESC) AS SEM1_RNK ");
            stb.append(           ",RANK() OVER (ORDER BY VALUE(ROUND(AVG(FLOAT(SEM2_VALUE)) * 10, 0) / 10, 0) DESC) AS SEM2_RNK ");
            stb.append(           ",RANK() OVER (ORDER BY VALUE(ROUND(AVG(FLOAT(GRAD_VALUE)) * 10, 0) / 10, 0) DESC) AS GRAD_RNK ");
            stb.append(    "FROM    RECORD_DAT T1  ");
            stb.append(    "WHERE   YEAR = '" + param._year + "'  ");
            stb.append(        "AND EXISTS(SELECT  'X' FROM SCHNO T2 WHERE T2.SCHREGNO = T1.SCHREGNO)  ");
            stb.append(        "AND (SUBSTR(SUBCLASSCD,1,2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR SUBSTR(SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "') ");
            stb.append(    "GROUP BY T1.SCHREGNO ");
            stb.append(    ") ");

            //成績明細データ(人数)の表
            stb.append(",RECORD_CNT AS ( ");
            stb.append(    "SELECT  COUNT(SEM1_SUM) AS SEM1_CNT ");
            stb.append(           ",COUNT(SEM2_SUM) AS SEM2_CNT ");
            stb.append(           ",COUNT(GRAD_SUM) AS GRAD_CNT ");
            stb.append(    "FROM    RECORD ");
            stb.append(    ") ");

            //メインの表
            stb.append("SELECT  SCHREGNO ");
            stb.append(       ",(SELECT SEM1_CNT FROM RECORD_CNT) AS SEM1_CNT ");
            stb.append(       ",(SELECT SEM2_CNT FROM RECORD_CNT) AS SEM2_CNT ");
            stb.append(       ",(SELECT GRAD_CNT FROM RECORD_CNT) AS GRAD_CNT ");
            stb.append(       ",CASE WHEN SEM1_SUM IS NULL THEN NULL ELSE SEM1_RNK END AS SEM1_RNK ");
            stb.append(       ",CASE WHEN SEM2_SUM IS NULL THEN NULL ELSE SEM2_RNK END AS SEM2_RNK ");
            stb.append(       ",CASE WHEN GRAD_SUM IS NULL THEN NULL ELSE GRAD_RNK END AS GRAD_RNK ");
            stb.append("FROM    RECORD ");
//            stb.append("WHERE   SCHREGNO = ? ");
            return stb.toString();
        }

        /** 
         *  PrepareStatement作成 生徒別出欠データ(備考)作成
         *
         */
        private static String prestatementRemark(final Param param)
        {
            final StringBuffer stb = new StringBuffer();
            //対象生徒の時間割データ ＳＨＲ
            stb.append("WITH SCHEDULE_SCHREG AS( ");
            stb.append(     "SELECT  T2.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD  ");
            stb.append(     "FROM    SCH_CHR_DAT T1 ");
            stb.append(             "INNER JOIN CHAIR_STD_DAT T2 ON  ");
            stb.append(         "    T1.YEAR = T2.YEAR  ");
            stb.append(         "AND T1.SEMESTER = T2.SEMESTER  ");
            stb.append(         "AND T1.CHAIRCD = T2.CHAIRCD  ");
            stb.append(             "INNER JOIN CHAIR_DAT T3 ON  ");
            stb.append(         "    T1.YEAR = T3.YEAR  ");
            stb.append(         "AND T1.SEMESTER = T3.SEMESTER  ");
            stb.append(         "AND T1.CHAIRCD = T3.CHAIRCD  ");
            stb.append(             "INNER JOIN SCHREG_REGD_DAT REGD ON  ");
            stb.append(         "    REGD.SCHREGNO = T2.SCHREGNO ");
            stb.append(         "AND REGD.YEAR = T2.YEAR  ");
            stb.append(         "AND REGD.SEMESTER = T2.SEMESTER  ");
            stb.append(             "INNER JOIN SCHREG_REGD_GDAT GDAT ON  ");
            stb.append(         "    GDAT.YEAR = REGD.YEAR  ");
            stb.append(         "AND GDAT.GRADE = REGD.GRADE ");
            stb.append(             "LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON  ");
            stb.append(         "    ENTGRD.SCHREGNO = REGD.SCHREGNO  ");
            stb.append(         "AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
            stb.append(     "WHERE   T1.YEAR = '" + param._year + "'  ");
            stb.append(         "AND T1.SEMESTER <= '" + param._gakki + "'  ");
            stb.append(         "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE  ");
            stb.append(         "AND T1.EXECUTEDATE BETWEEN '" + param._year + "-04-01" + "' AND '" + param._date + "'  ");
            stb.append(         "AND T2.SCHREGNO = ? ");
            if( param.definecode.usefromtoperiod )
                stb.append(     "AND T1.PERIODCD IN " + param._periodcd + " ");
            stb.append(         "AND SUBSTR(T3.SUBCLASSCD,1,2) = '92' ");
                                 //NO001 学籍不在日、留学日、休学日を除外
            
            stb.append(          "AND NOT((( ENTGRD.ENT_DIV IN('4','5') AND T1.EXECUTEDATE < ENTGRD.ENT_DATE ) ");
            stb.append(                "OR ( ENTGRD.GRD_DIV IN('2','3') AND T1.EXECUTEDATE > ENTGRD.GRD_DATE )) ) ");

            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                        "WHERE   T4.SCHREGNO = T2.SCHREGNO ");
            stb.append(                            "AND TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
                                 //<--- NO001
            stb.append(     "GROUP BY T2.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD  ");
            stb.append(     ")  ");

            //メインの表
            //対象生徒の出欠データ(備考)
            stb.append(     "SELECT  T0.SCHREGNO, T1.SEMESTER, T0.ATTENDDATE, T0.PERIODCD, T0.DI_CD, T0.DI_REMARK ");
            stb.append(     "FROM    ATTEND_DAT T0  ");
            stb.append(             "INNER JOIN SCHEDULE_SCHREG T1 ON  ");
            stb.append(             "    T0.SCHREGNO = T1.SCHREGNO AND  ");
            stb.append(             "    T0.ATTENDDATE = T1.EXECUTEDATE AND  ");
            stb.append(             "    T0.PERIODCD = T1.PERIODCD  ");
            stb.append(     "WHERE   T0.YEAR = '" + param._year + "' AND  ");
            stb.append(             "T0.ATTENDDATE BETWEEN '" + param._year + "-04-01" + "' AND '" + param._date + "' AND  ");
            stb.append(             "T0.DI_REMARK IS NOT NULL AND  ");
            stb.append(             "T0.DI_REMARK <> '' ");
            stb.append(    "ORDER BY T1.SEMESTER, T0.ATTENDDATE DESC, T0.PERIODCD ");
            return stb.toString();
        }
        
        /** 
         *  PrepareStatement作成 生徒別出欠データ(遅刻回数)作成
         *
         */
        private static String prestatementAttendSHR(final Param param)
        {
            final StringBuffer stb = new StringBuffer();
            //対象生徒
            stb.append("WITH SCHNO (SCHREGNO, SEMESTER) AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.SEMESTER ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1, SEMESTER_MST T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(        "AND T1.SEMESTER <= '" + param._gakki + "' ");
            stb.append(        "AND T1.YEAR = T2.YEAR ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T1.GRADE||T1.HR_CLASS = '" + param._gradeHrClass + "' ");
            stb.append(        "AND T1.SCHREGNO = ? ");
            stb.append(    "GROUP BY SCHREGNO,T1.SEMESTER ");
            stb.append(    "UNION ");
            stb.append(    "VALUES( cast(? as varchar(8) ), '9') ");
            stb.append(    ")");

            //対象生徒の出欠データ(授業の遅刻) 
            stb.append(",shr_ATTEND_DATb AS( ");
            stb.append("    SELECT  T1.SCHREGNO, T1.ATTENDDATE, T1.PERIODCD, T1.DI_CD, T2.SEMESTER ");
            stb.append("    FROM    ATTEND_DAT T1 ");
            stb.append("           ,semester_mst t2 ");
            stb.append("           ,chair_dat t3 ");
            stb.append("    WHERE   T1.YEAR = '" + param._year + "'  ");
            stb.append("        and T1.ATTENDDATE BETWEEN '" + param._divideAttendDate + "' AND '" + param._date + "'  ");
            stb.append("        and t2.year = '" + param._year + "' ");
            stb.append("        and t2.semester <= '" + param._gakki + "' ");
            stb.append("        and t3.year = '" + param._year + "' ");
            stb.append("        and t1.attenddate between t2.sdate and t2.edate ");
            stb.append("        and t2.semester = t3.semester ");
            stb.append("        and t3.chaircd = t1.chaircd ");
            stb.append("        and substr(t3.subclasscd,1,2) <> '92' ");
            stb.append("        and t1.di_cd in('15','23','24') ");
            stb.append("        and exists (SELECT SCHREGNO FROM SCHNO t3 where t3.schregno = T1.SCHREGNO)    ");
                                //NO001 学籍不在日、留学日、休学日を除外
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T4 ");
            stb.append(                       "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND (( ENT_DIV IN('4','5') AND ATTENDDATE < ENT_DATE ) ");
            stb.append(                             "OR ( GRD_DIV IN('2','3') AND ATTENDDATE > GRD_DATE )) ) ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                       "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND TRANSFERCD IN('1','2') AND ATTENDDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
                                //<--- NO001
            stb.append("    ) ");

            //対象生徒の出欠データ(遅刻度数) 
            stb.append(",shr_ATTEND_DATa AS( ");
            stb.append("    SELECT  T1.SCHREGNO, T1.ATTENDDATE, T1.PERIODCD, T1.DI_CD, T2.SEMESTER ");
            stb.append("    FROM    ATTEND_DAT T1 ");
            stb.append("           ,semester_mst t2 ");
            stb.append("           ,chair_dat t3 ");
            stb.append("    WHERE   T1.YEAR = '" + param._year + "'  ");
            stb.append("        and T1.ATTENDDATE BETWEEN '" + param._year + "-04-01" + "' AND '" + param._date + "'  ");
            stb.append("        and t2.year = '" + param._year + "' ");
            stb.append("        and t2.semester <= '" + param._gakki + "' ");
            stb.append("        and t3.year = '" + param._year + "' ");
            stb.append("        and t1.attenddate between t2.sdate and t2.edate ");
            stb.append("        and t2.semester = t3.semester ");
            stb.append("        and t3.chaircd = t1.chaircd ");
            stb.append("        and substr(t3.subclasscd,1,2) = '92' ");
            stb.append("        and t1.di_cd in('4','5','6','11','12','13','15','23','24') ");
            stb.append("        and exists (SELECT SCHREGNO FROM SCHNO t3 where t3.schregno = T1.SCHREGNO)    ");
                                //NO001  学籍不在日、留学日、休学日を除外
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T4 ");
            stb.append(                       "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND (( ENT_DIV IN('4','5') AND ATTENDDATE < ENT_DATE ) ");
            stb.append(                             "OR ( GRD_DIV IN('2','3') AND ATTENDDATE > GRD_DATE )) ) ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                       "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND TRANSFERCD IN('1','2') AND ATTENDDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
                                //<--- NO001
            stb.append("    ) ");

            //対象生徒の時間割データ
            stb.append(",SCHEDULE_SCHREG AS( ");
            stb.append("    SELECT  T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD  ");
            stb.append("    FROM    SCH_CHR_DAT T1, CHAIR_STD_DAT T2  ");
            stb.append("    WHERE   T1.YEAR = '" + param._year + "' AND  ");
            stb.append("            T1.EXECUTEDATE BETWEEN '" + param._year + "-04-01" + "' AND '" + param._date + "' AND  ");
            stb.append("            T2.YEAR = '" + param._year + "' AND  ");
            stb.append("            T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND  ");
            stb.append("            T1.SEMESTER = T2.SEMESTER AND  ");
            stb.append("            T1.CHAIRCD = T2.CHAIRCD AND  ");
            stb.append("            exists (SELECT 'x' FROM shr_ATTEND_DATa t3  ");
            stb.append("                     where t3.schregno = T2.SCHREGNO  ");
            stb.append("                       and t3.attenddate = t1.executedate)    ");
                                //NO001  学籍不在日、留学日、休学日を除外
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T4 ");
            stb.append(                       "WHERE   T4.SCHREGNO = T2.SCHREGNO ");
            stb.append(                           "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");
            stb.append(                             "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE )) ) ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                       "WHERE   T4.SCHREGNO = T2.SCHREGNO ");
            stb.append(                           "AND TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
                                //<--- NO001
            stb.append("             ");
            stb.append("    GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD  ");
            stb.append("    ) ");

            //対象生徒の出欠データ
            stb.append(",T_ATTEND_DAT AS( ");
            stb.append("    SELECT  T0.SCHREGNO, T0.ATTENDDATE, T0.PERIODCD, T0.DI_CD  ");
            stb.append("    FROM    ATTEND_DAT T0,  ");
            stb.append("            SCHEDULE_SCHREG T1  ");
            stb.append("    WHERE   T0.YEAR = '" + param._year + "' AND  ");
            stb.append("            T0.ATTENDDATE BETWEEN '" + param._year + "-04-01" + "' AND '" + param._date + "' AND  ");
            stb.append("            T0.SCHREGNO = T1.SCHREGNO AND  ");
            stb.append("            T0.ATTENDDATE = T1.EXECUTEDATE AND  ");
            stb.append("            T0.PERIODCD = T1.PERIODCD  ");
            stb.append("    ) ");

            //NO001 対象生徒の出欠データ（忌引・出停した日）=> 該当日の遅刻・早退はカウントしない
            stb.append(", T_ATTEND_DAT_B AS(");
            stb.append(     "SELECT  T0.SCHREGNO, T0.ATTENDDATE ");
            stb.append(     "FROM    T_ATTEND_DAT T0 ");
            stb.append(     "WHERE   DI_CD IN('2','3','9','10') ");
            stb.append(     "GROUP BY T0.SCHREGNO, T0.ATTENDDATE ");
            stb.append(     ") ");

            //対象生徒の日単位の最小校時・最大校時・校時数
            stb.append(",T_PERIOD_CNT AS( ");
            stb.append("    SELECT  T1.SCHREGNO, T1.EXECUTEDATE,  ");
            stb.append("            MIN(T1.PERIODCD) AS FIRST_PERIOD,  ");
            stb.append("            MAX(T1.PERIODCD) AS LAST_PERIOD,  ");
            stb.append("            COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT  ");
            stb.append("    FROM    SCHEDULE_SCHREG T1  ");
            stb.append("    GROUP BY T1.SCHREGNO, T1.EXECUTEDATE  ");
            stb.append("    ) ");

            //個人別出停・忌引
            stb.append(",suspend_MOURNING_data as( ");
            stb.append("    SELECT SCHREGNO, attenddate ");
            stb.append("    FROM   T_ATTEND_DAT  ");
            stb.append("    WHERE  DI_CD IN ('2','9','3','10')  ");
            stb.append("    ) ");

            //個人別欠席
            stb.append(",absent_data as( ");
            stb.append("    SELECT  W0.SCHREGNO, w0.attenddate ");
            stb.append("    FROM    ATTEND_DAT W0,  ");
            stb.append("        ( ");
            stb.append("        SELECT  T0.SCHREGNO, T0.EXECUTEDATE, T0.FIRST_PERIOD  ");
            stb.append("        FROM    T_PERIOD_CNT T0,  ");
            stb.append("            ( ");
            stb.append("            SELECT  W1.SCHREGNO, W1.ATTENDDATE,  ");
            stb.append("                    MIN(W1.PERIODCD) AS FIRST_PERIOD,  ");
            stb.append("                    COUNT(W1.PERIODCD) AS PERIOD_CNT  ");
            stb.append("            FROM    T_ATTEND_DAT W1  ");
            stb.append("            WHERE   W1.DI_CD IN ('4','5','6','11','12','13')  ");
            stb.append("            GROUP BY W1.SCHREGNO, W1.ATTENDDATE  ");
            stb.append("            ) T1  ");
            stb.append("        WHERE   T0.SCHREGNO = T1.SCHREGNO AND  ");
            stb.append("                T0.EXECUTEDATE = T1.ATTENDDATE AND  ");
            stb.append("                T0.FIRST_PERIOD = T1.FIRST_PERIOD AND  ");
            stb.append("                T0.PERIOD_CNT = T1.PERIOD_CNT  ");
            stb.append("        ) W1  ");
            stb.append("    WHERE   W0.SCHREGNO = W1.SCHREGNO AND  ");
            stb.append("            W0.ATTENDDATE = W1.EXECUTEDATE AND  ");
            stb.append("            W0.PERIODCD = W1.FIRST_PERIOD  ");
            stb.append("    ) ");

            //個人別早退１
            stb.append(",early_data1 as( ");
            stb.append("    SELECT  T0.SCHREGNO, T1.ATTENDDATE ");
            stb.append("    FROM    T_PERIOD_CNT T0  ");
            stb.append("        INNER JOIN( ");
            stb.append("            SELECT  W1.SCHREGNO, W1.ATTENDDATE, COUNT(W1.PERIODCD) AS PERIOD_CNT  ");
            stb.append("            FROM    T_ATTEND_DAT W1  ");
            stb.append("            WHERE   W1.DI_CD NOT IN ('0','14','15','16','23','24')      ");
                                        //NO001 出停・忌引の日を除外
            stb.append(                "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");
            stb.append(                               "WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.ATTENDDATE = W1.ATTENDDATE) ");
                                        //<--- NO001
            stb.append("            GROUP BY W1.SCHREGNO, W1.ATTENDDATE  ");
            stb.append("            )T1 ON T0.SCHREGNO = T1.SCHREGNO AND T0.EXECUTEDATE = T1.ATTENDDATE AND  ");
            stb.append("                                                 T0.PERIOD_CNT != T1.PERIOD_CNT  ");
            stb.append("        INNER JOIN( ");
            stb.append("            SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD  ");
            stb.append("            FROM    T_ATTEND_DAT  ");
            stb.append("            WHERE   DI_CD IN ('4','5','6')                           ");
            stb.append("            )T3 ON T0.SCHREGNO= T3.SCHREGNO AND T0.EXECUTEDATE = T3.ATTENDDATE AND  ");
            stb.append("                                                T0.LAST_PERIOD = T3.PERIODCD  ");
            stb.append("    ) ");

            //個人別早退２
            stb.append(",early_data2 as( ");
            stb.append("    SELECT  T0.SCHREGNO, T2.ATTENDDATE ");
            stb.append("    FROM    T_PERIOD_CNT T0  ");
            stb.append("        INNER JOIN( ");
            stb.append("            SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD  ");
            stb.append("            FROM    T_ATTEND_DAT W1 ");  //NO001
            stb.append("            WHERE   DI_CD IN ('16')  ");
                                        //NO001 出停・忌引の日を除外
            stb.append(                "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");
            stb.append(                               "WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.ATTENDDATE = W1.ATTENDDATE) ");
                                        //<--- NO001
            stb.append("            )T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND  ");
            stb.append("                                                 T0.LAST_PERIOD = T2.PERIODCD  ");
            stb.append("    ) ");


            //メインの表
            stb.append(   "SELECT  TT0.SCHREGNO, TT0.SEMESTER, ");
            stb.append(           "VALUE(TT1.count,0) AS COUNT ");
            stb.append(          ",VALUE(TT2.NOT_SHR,0) + VALUE(TT7.NOT_SHR,0) AS NOT_SHR ");
            stb.append(   "FROM    SCHNO TT0 ");

            //個人別遅刻回数ＳＨＲ(遅刻度数)
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(         "select t1.schregno, value(t1.semester,'9') as semester, SUM(SMALLINT(VALUE(L1.ABBV2, '1'))) as count ");
            stb.append(         "from   shr_ATTEND_DATa t1 ");
            stb.append(                "LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'C001' AND L1.NAMECD2 = t1.DI_CD ");
            stb.append(         "where not exists(select 'x' from suspend_MOURNING_data t2 where t2.schregno = t1.schregno and t2.attenddate = t1.attenddate) ");
            stb.append(         "  and not exists(select 'x' from absent_data t2 where t2.schregno = t1.schregno and t2.attenddate = t1.attenddate) ");
            stb.append(         "  and not exists(select 'x' from early_data1 t2 where t2.schregno = t1.schregno and t2.attenddate = t1.attenddate) ");
            stb.append(         "  and not exists(select 'x' from early_data2 t2 where t2.schregno = t1.schregno and t2.attenddate = t1.attenddate) ");
            stb.append(         "group by grouping sets((t1.schregno,t1.semester),t1.schregno) ");
            stb.append(      ") TT1 ON TT0.SCHREGNO = TT1.SCHREGNO AND TT0.SEMESTER = TT1.SEMESTER ");

            //個人別遅刻回数授業
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(         "select t1.schregno, value(t1.semester,'9') as semester, SUM(SMALLINT(VALUE(L1.ABBV2, '1'))) as NOT_SHR ");
            stb.append(         "from   shr_ATTEND_DATb t1 ");
            stb.append(                "LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'C001' AND L1.NAMECD2 = t1.DI_CD ");
            stb.append(         "group by grouping sets((t1.schregno,t1.semester),t1.schregno) ");
            stb.append(      ") TT2 ON TT0.SCHREGNO = TT2.SCHREGNO AND TT0.SEMESTER = TT2.SEMESTER ");

            //科目別集計データから集計した表
            stb.append(   "LEFT JOIN(");
            stb.append(      "SELECT  SCHREGNO, VALUE(SEMESTER,'9')AS SEMESTER, ");
            stb.append(              "SUM(LATE) AS NOT_SHR ");
            stb.append(      "FROM    ATTEND_SUBCLASS_DAT W1 ");
            stb.append(      "WHERE   YEAR = '" + param._year + "' AND ");
            stb.append(              "SEMESTER <= '" + param._gakki + "' AND ");
            stb.append(              "CLASSCD <> '92' AND ");
            stb.append(              "(CASE WHEN INT(W1.MONTH) < 4 THEN RTRIM(CHAR(INT(W1.SEMESTER) + 1 )) ELSE W1.SEMESTER END )||W1.MONTH <= '" + KNJC053_BASE.retSemesterMonthValue (param._divideAttendMonth ) + "' AND ");
            stb.append(              "EXISTS(SELECT  'X' ");
            stb.append(                     "FROM    SCHNO W2 ");
            stb.append(                     "WHERE   W1.SCHREGNO = W2.SCHREGNO)");
            stb.append(      "GROUP BY GROUPING SETS((SCHREGNO,SEMESTER),SCHREGNO) ");
            stb.append(      ")TT7 ON TT0.SCHREGNO = TT7.SCHREGNO AND TT0.SEMESTER = TT7.SEMESTER ");

            stb.append("ORDER BY TT0.SCHREGNO ,TT0.SEMESTER");
            return stb.toString();
        }
    }

	private static class Param {
        final String _year;
        final String _gakki;
        final String _gradeHrClass;
        final String _date;
        final String _categorySelected;
        String _divideAttendDate;
        String _divideAttendMonth;
        String _nendo;
        String _staffname;
        String _periodcd;

        /** 教育課程コードを使用するか */
        private String _useCurriculumcd;
        private String _useVirus;
        private String _useKoudome;
        
        private List arrstaffname;              //担任
        private KNJDefineSchool definecode;       //各学校における定数等設定
        private KNJSchoolMst _knjSchoolMst;

//        private String _periodInState;
//        private Map _attendSemesMap;
//        private Map _hasuuMap;
//        private boolean _semesFlg;
        private String _sDate;
        final private String SSEMESTER = "1";
        final Map<String, PreparedStatement> _psMap = new HashMap<String, PreparedStatement>();
        
        final Map _attendParamMap;
        
        final String _remark1;
        final String _remark2;
        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");                        //年度
            _gakki = request.getParameter("GAKKI");                       //1-3:学期
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");              //学年・組
            _date = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE") ); //異動基準日
            
                   //学籍番号
            _categorySelected = Set_Schno( request.getParameterValues("category_selected") );      //学籍番号

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");

            // 出欠パラメータロード
            loadAttendSemesArgument(db2);
            
            setHead(db2);         //見出し項目
            getDivideAttendDate( db2 );  //出欠用日付等取得

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _gradeHrClass.substring(0, 2));
            _attendParamMap.put("hrClass", _gradeHrClass.substring(2));
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG");

            final Map<String, String> certifSchoolDat = KnjDbUtils.firstRow(KnjDbUtils.query(db2, prestatementgetComment()));
            _remark1 = KnjDbUtils.getString(certifSchoolDat, "REMARK1");
            _remark2 = KnjDbUtils.getString(certifSchoolDat, "REMARK2");
        }
        
        /**
         *  備考１・備考２を取得
         **/
    	private String prestatementgetComment()
    	{
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  REMARK1, REMARK2 ");
            stb.append("FROM    CERTIF_SCHOOL_DAT ");
            stb.append("WHERE   YEAR = '" + _year + "' AND ");
            stb.append(        "CERTIF_KINDCD = '104'");
    		return stb.toString();

    	}
        
        /**
         *  対象生徒学籍番号編集(SQL用) 
         */
        private String Set_Schno(String schno[]){

            final StringBuffer stb = new StringBuffer();

            for( int ia=0 ; ia<schno.length ; ia++ ){
                if( ia==0 ) stb.append("('");
                else        stb.append("','");
                stb.append(schno[ia]);
            }
            stb.append("')");

            return stb.toString();
        }
        
        /**
         *  出欠集計テーブルをみる最終月と出欠データをみる開始日を取得する
         */
        private void getDivideAttendDate( DB2UDB db2)
        {
            KNJDivideAttendDate obj = new KNJDivideAttendDate();
            try {
                obj.getDivideAttendDate( db2, _year, _gakki, _date );
                _divideAttendDate = obj.date;
                _divideAttendMonth = obj.month;
            } catch( Exception ex ){
                log.error("error! ",ex);
            }
        }

        private void setHead(DB2UDB db2){

        //  出力項目
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度"; //年度
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;

            //学級担任名を取得
            try {
                arrstaffname = getinfo.Staff_name( db2, _year, _gakki, _gradeHrClass );
                if( 0 < arrstaffname.size() )
                    _staffname = (String)arrstaffname.get(0);                        //学級担任名
            } catch( Exception ex ){
                log.error("getinfo.Staff_name() error! ", ex);
            }

        //  欠課数換算定数取得 => KNJDefineCodeImpを実装したオブジェクトを作成
            try {
                definecode = new KNJDefineSchool();
                definecode.defineCode( db2, _year );         //各学校における定数等設定
    log.debug("schoolmark=" + definecode.schoolmark + " *** semesdiv=" + definecode.semesdiv + " *** absent_cov=" + definecode.absent_cov + " *** absent_cov_late=" + definecode.absent_cov_late);
            } catch( Exception ex ){
                log.warn("definecode.defineCode() error! ", ex);
            }
            try {
            	final Map schoolMstMap = new HashMap();
            	if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
            		schoolMstMap.put("SCHOOL_KIND", KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _gradeHrClass.substring(0, 2) + "' ")));
            	}
                _knjSchoolMst = new KNJSchoolMst(db2, _year, schoolMstMap);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            //１日出欠集計対象校時を取得
            try {
                returnval = getinfo.getTargetPeriod( db2, _year, _gakki, _gradeHrClass, definecode.usefromtoperiod );
                _periodcd = returnval.val1;                     //１日出欠集計対象校時
            } catch( Exception ex ){
                log.error("getTargetPeriod() error! ", ex);
            }

            getinfo = null;
            returnval = null;
        }
        
//        private KNJDefineSchool setClasscode0(final DB2UDB db2, final String year) {
//        	KNJDefineSchool defineSchool = null;
//            try {
//                defineSchool = new KNJDefineSchool();
//                defineSchool.defineCode(db2, year);         //各学校における定数等設定
//            } catch (Exception ex) {
//                log.warn("semesterdiv-get error!", ex);
//            }
//            return defineSchool;
//        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }

        private void loadAttendSemesArgument(DB2UDB db2) {
            try {
                loadSemester(db2, _year);
                // 出欠の情報
//                final KNJDefineSchool definecode0 = setClasscode0(db2, _year);
//                final String z010Name1 = setZ010Name1(db2);
//                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, _gakki);
//                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
//                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, _date); // _sDate: 年度開始日, _date: LOGIN_DATE
//                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
        }
        
        /**
         * 年度の開始日を取得する 
         */
        private void loadSemester(final DB2UDB db2, String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            final List list = new ArrayList();
            try {
                ps = db2.prepareStatement(sqlSemester(year));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    map.put(semester, name);
                    final String sDate = rs.getString("SDATE");
                    list.add(sDate);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            if (!list.isEmpty()) {
                _sDate = (String) list.get(0);
            }
            log.debug("年度の開始日=" + _sDate);
        }
        
        private String sqlSemester(String year) {
            final String sql;
            sql = "select"
                + "   SEMESTER,"
                + "   SEMESTERNAME,"
                + "   SDATE"
                + " from"
                + "   SEMESTER_MST"
                + " where"
                + "   YEAR='" + year + "'"
                + " order by SEMESTER"
            ;
            return sql;
        }
    }
}
