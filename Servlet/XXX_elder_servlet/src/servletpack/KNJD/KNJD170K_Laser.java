/**
 http://ktest/servlet_kin/KNJL?DBNAME=H051101&PRGID=KNJD170&YEAR=2005&GAKKI=2&GRADE_HR_CLASS=03J04&TESTKINDCD=01&DATE=2005/10/06&OUTPUT=1&category_selected=20035103
 http://ktest/servlet_kin/KNJL?DBNAME=H051101&PRGID=KNJD170&YEAR=2005&GAKKI=2&GRADE_HR_CLASS=03J03&TESTKINDCD=01&DATE=2005/10/06&OUTPUT=1&category_selected=20035169
 http://ktest/servlet_kin/KNJL?DBNAME=H051101&PRGID=KNJD170&YEAR=2004&GAKKI=2&GRADE_HR_CLASS=03J01&TESTKINDCD=99&DATE=2004/10/06&OUTPUT=1&category_selected=20025001
 *
 *  学校教育システム 賢者 [成績管理]
 *
 *                  ＜ＫＮＪＤ１７０Ｋ＞  成績通知票
 *
 *  2004/09/07 yamashiro 新規作成
 *  2004/10/20 nakamoto 「対象データはありません。」文字化け表示を修正。
 *  2004/10/26 yamashiro
 *  2004/11/01 yamashiro 席次は平均点を丸めない値で算出する
 *                       出欠集計テーブルATTEND_SEMES_DAT,_DATの変更に伴う修正
 *  2004/11/02 yamashiro 出欠項目の出力は取り敢えず除外！
 *  2004/11/03 yamashiro １科目でも欠席がある者は平均点と席次の対象外とし出力しない
 *                       単位数は履修ではなく単位マスタに該当する単位があれば出力する
 *                       期末成績、学期成績、出欠の状況は学期末成績(指示画面より)の場合だけ出力する
 *  2004/11/04 yamashiro 組平均は、中間成績のときは中間の組平均を、その他は学期成績の組平均を出力する
 *  2004/11/07 yamashiro 受験者数、組平均は直接成績データをカウントして出力する
 *  2004/11/08 yamashiro 成績組平均は異動者以外のすべての成績を平均して出力する
 *                       個人の平均、席次および受験者数は異動者も対象外とする
 *  2004/11/09 yamashiro 科目別組平均は異動者以外のすべての成績を平均して出力する
 *                       単位は生徒ごとに履修科目のみ出力する
 *                       試験の公欠・欠席は()を出力する => クラス別成績一覧と同様に得点は出力しない
 *                       席次順番の不具合を修正 => 順位付けの成績は表記科目だけではなくすべての科目を含める
 *  2004/12/08 yamashiro 成績一覧表KNJD060と同仕様にする
 *                       ・出欠の基準日を、印刷指示画面で入力した日とする
 *                       ・出欠集計を、ATTEND_DATから出力する
 *                       視力を４文字とする
 *  2004/12/10 yamashiro 出欠項目は、２００４年度の１学期は出欠累積データより、以降は出欠データを集計する
 *  2004/12/15 yamashiro う歯(=>乳歯＋永久歯)が０の場合は印字しない
 *                       視力の少数第二位が０の場合、０は印字しない
 *                       １年国語総合の単位を印字
 *  2005/02/04 yamashiro ・３年の学年の項目の段がずれる不具合を修正。
 *                       ・評定の平均を小数点第二位で四捨五入して出力。
 *                       ・評定の段に、評定の科目が無ければ’合’と出力。
 *                       ・試験で欠席でも、補点済みなら成績を出力する。
 *                       ・評価読替え科目について、評定を出力する。
 *  2005/02/08 yamashiro 成績データの試験成績、学期成績、学年成績の取得仕様を変更
 *  2005/02/09 yamashiro 上記と同様の理由で合計、平均、席次、受験者数の取得仕様を変更
 *  2005/02/20 yamashiro 出欠の記録において取得仕様を変更 KNJDivideAttendDate.classを使用
 *  2005/02/25 yamashiro・最初の校時の忌引は遅刻にカウントしない、最後の校時の忌引は早退にカウントしない、
 *  2005/03/04 yamashiro  一日遅刻・早退をカウントする際、遅刻・早退を出席としてカウントするように修正
 *  2005/03/07 yamashiro 科目コード'41700'と'41800'を同一列('41700')に出力する
 *                       ３学期期末成績が出力されない不具合を修正
 *  2005/03/09 yamashiro '1J01','2J01','3J01'等は'4J1','5J1','6J1'と表記する
 *  2005/03/12 yamashiro・短期留学生対応（３学期に留学した生徒は対象とする）
 *                      ・出席すべき日数から休学日数を除外する
 *  2005/03/14 yamashiro・評定読替科目の仕様変更
 *  2005/03/15 yamasihro・総合学習は評定マスターからアルファベットを出力、また組平均を出力しない
 *  2005/03/16 yamashiro・転学、退学において異動日は在籍とみなす
 *                      ・留学および休学が各学期終了日（学期終了日が異動基準日より大きい場合は異動基準日）にかからない場合は対象とする
 *                      ・全科目組平均を全生徒全得点を全科目数で割った数値とする <= 修正前は生徒別全科目平均の平均を取っていた
 *  2005/03/17 yamasihro・欠課時数超過科目の評定を出力しない
 *                      　また、１科目でも欠課時数超過科目があれば席次の対象外とし、平均および席次を出力しない
 *                      ・評定読替元で１科目でも欠課時数超過があれば評定読替科目も超過として扱う
 *  2005/03/18 yamashiro・欠課時数超過科目の評定を合計から除外する
 *  2005/05/31 yamashiro 欠課時数は、学年末は３学期期末のみ出力
 *  2005/05/31 yamashiro 組平均は、中間成績の場合は中間試験を出力する
 *  2005/06/08 yamashiro 学年成績以外は、留学生は出力しない
 *  2005/07/01 yamashiro 出欠状況の留学日数に休学日数を含めない
 *  2005/07/03 yamashiro ３年生は総合学習の評定を学年末の評定欄に常時印字する
 *  2005/07/13 yamashiro ３年生は学年末の評定欄を出力する、また学期成績および組平均を出力する
 *  2005/08/24 yamashiro ＳＱＬステートメント作成メソッドprestatementTotalの不具合を修正
 *  2005/10/07 yamashiro 編入（データ仕様の変更による）について修正、および学籍異動に転学を追加
 *                       忌引または出停の日は遅刻および早退をカウントしない
 *                       留学・休学・不在日数の算出において、ATTEND_SEMES_DATもみる。
 *                           <change specification of 05/09/28>
 *  2005/10/27 yamashiro ２学期における出力で１学期の合計欄が出力されない不具合等を修正
 *  2005/11/03 yamashiro 総合学習は(３年生の)中間試験では出力しない、期末試験では出力する
 *                       prestatementTotalメソッドにおけるRECORD_DAT_KK表を修正 <= ２学期の中間試験成績が入った後１学期末成績を出力する際の不具合
 *  2005/11/04 yamashiro ２学期の中間成績が入った状態で１学期期末の通知表を出す場合、KIN_RECORD_DATのJUDGHE_PATTERNが入っていないことによる不具合を解消(暫定措置)
 *                       =>評定は学期ごとに持っていないので過去の時点の評定を出せない。従って、現時点では矛盾がある。
 *  2005/11/05 yamashiro 評価読替科目の各学期成績・組平均は、その時点までのKIN_RECORD_DATの学期成績の平均およびその組平均を出力する
 *                       =>これまでGRADE_RECORDを学年成績として１学期にも出力していたが、学年末のみGRADE_RECORDで、１・２学期はSEM?_RECの平均を取る
 *                       05/07/13〜05/07/15の暫定措置を正規処理へ変更
 *                       ３年生は２学期において学年末を出力
 *  2005/11/17 yamashiro 1年生の成績通知表を1学期期末や2学期中間を指定して出力した場合に読替後科目の1学期成績欄と組平均欄に数値が表示される不具合を修正
 *                       3年生の成績通知表を2学期中間で指定して出力した場合に読替後科目の組平均欄に数値が表示される不具合を修正
 *                          => 但し、各学期末における3年生の読替後科目の出力仕様は(この時点で)未定
 *  2005/11/21 yamashiro 学年末の学年成績合計欄において評価読替元科目が加算されていない不具合を修正( =>07/15の正常化の残り )
 *  2005/12/02 yamashiro 読替え科目の学年成績・組平均等について仕様決定
 *                       ・1，2年生
 *                       　各学期欄には読替え科目の成績・組平均は印字しません。これは各学期の中間・期末、何れの場合でもです。読替え科目を印字するのは3学期期末指定の場合のみ学　年末欄に印字します。
 *                       ・3年生
 *                       　1学期期末指定で出力した時に、読替え科目の成績(学期or学年？)・組平均は1学　期欄に印字します。2学期末(学年末)の指定では1学期・2学期欄共に読替え科目は　印字せず、学年末欄のみに印字します。また期末時には学年末欄に評定を印字しま　す。中間試験では学年末欄の評定は印字しません。
 *  2005/12/09 yamashiro 欠課時数集計におけるペナルティ欠課算出の不具合を修正
 *  2006/03/07 yamashiro ○残作業?117の未対応について対応 --NO101
 *  2006/03/10 yamashiro ○「ATTEND_SEMES_DATの授業日数は「留学・休学」を除外しない」に対応  NO101の修正
 */

package servletpack.KNJD;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.dao.AttendAccumulate;


public class KNJD170K_Laser {

    private static final Log log = LogFactory.getLog(KNJD170K_Laser.class);
    private String printname;               //プリンタ名
    private PrintWriter outstrm;

    private Param _param;

    private static final int SEMESTER3 = 3;
    private static final String GRADE03 = "03";
    
    /**
     *
     *  KNJD.classから最初に起動されるクラス
     *
     **/
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        boolean nonedata = false;

        // print svf設定
        setSvfInit(response, svf);
        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("db new error:", ex);
            if (db2 != null) {
                db2.close();
            }
            return;
        }

        try {
            _param = createParam(request, db2);
            // 印刷処理
//            ttotals = System.currentTimeMillis();
            nonedata = printSvfMain(db2, svf);
//            ttotal = System.currentTimeMillis() - ttotals;
            
//            log.debug(" t3 = " + (t3 / 1000.0) + "[s]");
//            log.debug(" t4 = " + (t4 / 1000.0) + "[s]");
//            log.debug(" t5 = " + (t5 / 1000.0) + "[s]");
//            log.debug(" total = " + (ttotal / 1000.0) + "[s]");
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            // 終了処理
            closeSvf(svf, nonedata);
            try {
                if (null != _param) {
                    for (final Iterator it = _param._psMap.values().iterator(); it.hasNext();) {
                        final PreparedStatement ps = (PreparedStatement) it.next();
                        DbUtils.closeQuietly(ps);
                    }
                }

                db2.commit();
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }//try-cathの括り
        }
    }
    
    /** print設定 */
    private void setSvfInit(final HttpServletResponse response, final Vrw32alp svf) {

        try {
            outstrm = new PrintWriter(response.getOutputStream());
            if (printname != null) {
                response.setContentType("text/html");
            } else {
                response.setContentType("application/pdf");
            }
            svf.VrInit();                                   //クラスの初期化

            if (printname != null) {
                int ret = svf.VrSetPrinter("", printname);            //プリンタ名の設定
                if (ret < 0) {
                    log.info("printname ret = " + ret);
                }
            } else {
                svf.VrSetSpoolFileStream(response.getOutputStream());          //PDFファイル名の設定
            }
        } catch(java.io.IOException ex) {
            log.error("db new error:", ex);
        }
    }

    /** svf close */
    private void closeSvf(final Vrw32alp svf, final boolean nonedata) {
        if (printname != null) {
            outstrm.println("<HTML>");
            outstrm.println("<HEAD>");
            outstrm.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=euc-jp\">");
            outstrm.println("</HEAD>");
            outstrm.println("<BODY>");
            if (!nonedata) {
                outstrm.println("<H1>対象データはありません。</h1>");
            } else {
                outstrm.println("<H1>印刷しました。</h1>");
            }
            outstrm.println("</BODY>");
            outstrm.println("</HTML>");
        } else if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }
        int ret = svf.VrQuit();
        if (ret == 0) {
            log.info("===> VrQuit():" + ret);
        }
        outstrm.close();            //ストリームを閉じる
    }

    /**
     *
     * SVF-OUT 印刷処理
     */
    private boolean printSvfMain(final DB2UDB db2, final Vrw32alp svf) {
        boolean nonedata = false;

        final String form = _param.isTotalStudyShokenForm() ? "KNJD170K_5.frm" : "KNJD170K_4.frm";
        log.info(" form = " + form);
		svf.VrSetForm(form, 1);

        //  ＳＶＦ属性変更--->改ページ
        svf.VrAttribute("ATTENDNO","FF=1");                 //出席番号で改ページ

        for (int i = 0 ; i < _param.hrclass.length ; i++) {
            String gradeHrClass;
            if (Integer.parseInt(_param._output) == 2) {
                gradeHrClass = _param.hrclass[i];
            } else {
            	gradeHrClass = _param._gradeHrClass;
            }
            log.debug("PRINT OF " + _param._gradeHrClass);
            if (printSvfMainHrclass(db2, svf, _param, gradeHrClass)) {
                nonedata = true;
            }
        }
        
        return nonedata;
    }


    /**
     *
     * SVF-OUT 学級単位の印刷処理
     *    Stirng param[]について ==> param._gradeHrClass:学年・組 param._category_selected:該当生徒の学籍番号
     */
    private boolean printSvfMainHrclass(final DB2UDB db2, final Vrw32alp svf, final Param param, final String gradeHrClass) {
        boolean nonedata = false;

        final List studentList = Student.getStudentList(db2, param, gradeHrClass);

        //ＳＶＦ出力
        log.info(" print.");
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            //log.info(" schregno = " + student._schregno);
            printSvfRegdOut(svf, gradeHrClass, student, param);      //学籍・健康診断データ出力
            printSvfSubclassOut(svf, student);       //科目および単位印刷
            //科目別欠課時数および授業時数をセット
            printSvfSubclassAttendOut(svf, param, student);
            printSvfRecordTable(svf, param, student);
            //出欠データをセット
            printSvfAttend(svf, student);  //出欠データ印刷

            if (param.isTotalStudyShokenForm()) {
                // 総合的な学習の時間の所見を印刷
                printSvfTotalstudyTimeDat(svf, student);
            }
            
            svf.VrEndPage();
            nonedata = true;
        }

        return nonedata;
    }

    // 成績出力
	private void printSvfRecordTable(final Vrw32alp svf, final Param param, final Student student) {

	    final int IDX15_GAKUNENMATSU_HYOTEI = 15;
	    final int IDX16_GAKUNENMATSU_KUMIHEIKIN = 16;
		
		//成績明細データをセット
	    if (param._isOutputDebug) {
	    	log.info(" student.meisaiList size = " + student.meisaiList.size());
	    }
		for (final Iterator mit = student.meisaiList.iterator(); mit.hasNext();) {
		    final Map meisai = (Map) mit.next();
		    final String subclasscd = getString("SUBCLASSCD", meisai);
		    
		    /**
		     *
		     * SVF-OUT 成績明細印刷 04/11/03Modify
		     *         １回の処理で１学期４行を出力
		     *         中間処理の場合は中間成績と組平均だけを出力
		     *    2005/02/08 Modify 成績データの試験成績、学期成績、学年成績の取得仕様変更により修正
		     */

	    	if (param._isOutputDebug) {
	    		log.info(" meisai: " + meisai);
	    	}
	        try {
	            final Subclass subclass = (Subclass) student.hmm.get(subclasscd);   //科目コードより列番号を取得
	            if (subclass == null) {
	                continue;
	            }
	            final int rsSemesInt = Integer.parseInt(getString("SEMESTER", meisai));
	            final int IDX_SEISEKI1, IDX_SEISEKI2, IDX_SEISEKI3, iDX_KUMIHEIKIN;
	            if (rsSemesInt == 1) {
	                IDX_SEISEKI1 = 1;
	                IDX_SEISEKI2 = 2;
	                IDX_SEISEKI3 = 3;
	                iDX_KUMIHEIKIN = 4;
	            } else if (rsSemesInt == 2) {
	                IDX_SEISEKI1 = 6;
	                IDX_SEISEKI2 = 7;
	                IDX_SEISEKI3 = 8;
	                iDX_KUMIHEIKIN = 9;
	            } else if (rsSemesInt == SEMESTER3) {
	                IDX_SEISEKI1 = 11;
	                IDX_SEISEKI2 = 14;
	                IDX_SEISEKI3 = IDX15_GAKUNENMATSU_HYOTEI;
	                iDX_KUMIHEIKIN = 12;
	            } else {
	            	continue;
	            }

				if (param._d065Name1List.contains(subclasscd)) {
	                if (rsSemesInt == SEMESTER3) {
	                    final String hyotei = (String) param._d001Abbv1Map.get(getString("SCORE2", meisai));
	                    svf.VrsOutn("POINT" + subclass._line, IDX_SEISEKI3, hyotei);
	                }
	                continue;
	            }

	            //３年の３学期以外の場合処理 ==> ３年は３学期期末試験成績が無いので否処理！
	            if (rsSemesInt == SEMESTER3 && param._grade.equals(GRADE03)) {
	            } else {
					svf.VrsOutn("POINT" + subclass._line, IDX_SEISEKI1, StringUtils.defaultString(getString("SCORE1", meisai)));   //中間・中間・期末
	            }
	            //期末成績および学期成績は当学期中間成績では出力しない！
	            final String classCd = subclasscd.substring(4,6);
	            if (param.isKimatsu() || rsSemesInt < param._gakkiInt) {
					svf.VrsOutn("POINT" + subclass._line, IDX_SEISEKI2, StringUtils.defaultString(getString("SCORE2", meisai)));    //期末・期末・学年
	                //総合学習以外を出力
	                if (! classCd.equals("90") || param._gakkiInt == SEMESTER3) {
	                    final String score3 = getString("SCORE3", meisai);
	                    if (rsSemesInt == SEMESTER3 && NumberUtils.isDigits(score3) && Integer.parseInt(score3) == 0) {
	                        // 学年評定が0は印字しない
	                    } else {
	                        svf.VrsOutn("POINT" + subclass._line, IDX_SEISEKI3, StringUtils.defaultString(score3));    //学期・学期・評定
	                    }
//	                  log.debug("score3="+score3);
	                    if (score3 != null && rsSemesInt == SEMESTER3 && !classCd.equals("90")) {
	                        if (score3.equals("( )")) {
	                            student.schassesscnt = -1;
	                        } else {
	                            if (0 < score3.length()) {
	                                student.schassesstotal += Integer.parseInt(score3);
	                                if (0 <= student.schassesscnt) {
	                                    student.schassesscnt++;
	                                }
	                            }
	                        }
	                    }
	                }
	                // 欠課時数超過者対応
	                if ((param._grade.equals(GRADE03) && param._gakkiInt != 1) || (! param._grade.equals(GRADE03) && param._gakkiInt == SEMESTER3)) {
	                    if (getString("ATTENDOVER", meisai) != null && 0 < Integer.parseInt(getString("ATTENDOVER", meisai))) {
	                        if (!subclass._isMotokamoku) {
	                            if (subclass._isSakikamoku) {
	                                if (null != subclass._semesterAbsent2.get("4")) {
	                                    final int absent2 = Integer.parseInt((String) subclass._semesterAbsent2.get("4"));
	                                    final int absenceHigh = Integer.parseInt(StringUtils.defaultString(subclass._credits, "0")) * (GRADE03.equals(param._grade) ? 8 : 10) + 1;
	                                    // log.debug(subclass._subclasscd + ", absent2 = " + absent2 + ", credits = " + credits + ", absenceHigh = " + absenceHigh);
	                                    if (absent2 >= absenceHigh) {
	                                        svf.VrsOutn("POINT" + subclass._line, IDX_SEISEKI3, "");     //学期・学期・評定
	                                        student.attendover = true;
	                                    }
	                                }
	                            } else {
	                                svf.VrsOutn("POINT" + subclass._line, IDX_SEISEKI3, "");     //学期・学期・評定
	                                student.attendover = true;
	                            }
	                        }
	                    }
	                }

	                //必須科目で評定１があれば gohihanteiフラグをtrueとする
	                if (rsSemesInt == SEMESTER3 && !"1".equals(getString("ELECTDIV", meisai)) && null != getString("SCORE3", meisai)) {
	                    final String score3 = getString("SCORE3", meisai);
	                    if (score3.equals("( )") || score3.equals("")  ||  score3.equals("1")) {
	                        student.gohihantei = true;
	                    }
	                }
	            }

	            //３年生は総合学習の評定を学年末の評定欄に常時印字する
	            // 中間試験・・・出力しない 期末試験・・・出力する
	            if (classCd.equals("90") && param._grade.equals(GRADE03) && param._gakkiInt < SEMESTER3 && param.isKimatsu()) {
	                svf.VrsOutn("POINT" + subclass._line, IDX15_GAKUNENMATSU_HYOTEI, StringUtils.defaultString(getString("SCORE3", meisai)));   //学期・学期・評定
	            }

	            boolean printKumiheikin = true;
	            if (NumberUtils.isDigits(student._entDateSemester) && rsSemesInt < Integer.parseInt(student._entDateSemester)) {
	                // 生徒が年度途中に転入してきた際、転入以前の学期の組平均を表示しない
	                printKumiheikin = false;
	            }
	            if (printKumiheikin) {
	                //組平均を出力
	                // 科目別組平均は異動者を除外する
	                if (!(param._grade.equals(GRADE03) && rsSemesInt == SEMESTER3)) {
						svf.VrsOutn("POINT" + subclass._line, iDX_KUMIHEIKIN, StringUtils.defaultString(getString("AVG_HR", meisai)));       //組平均
	                }
	            }
	            if (rsSemesInt == SEMESTER3) {
	                if (param._gakkiInt == SEMESTER3 || (param._gakkiInt == 2 && param._grade.equals(GRADE03) && param.isKimatsu())) {
	                    svf.VrsOutn("POINT" + subclass._line, IDX16_GAKUNENMATSU_KUMIHEIKIN, StringUtils.defaultString(getString("GRADE_RECORD_AVG_HR", meisai)));      //学年末組平均
	                }
	            }

	        } catch (Exception ex) {
	        	log.error("error! ", ex);
	        }
		}
	    if (param._isOutputDebug) {
	    	log.info(" student.goukeiList size = " + student.goukeiList.size());
	    }
		//成績合計・平均・席次・受験者数データをセット
		for (final Iterator git = student.goukeiList.iterator(); git.hasNext();) {
		    final Map goukei = (Map) git.next();
		    
		    /**
		     *
		     * SVF-OUT 成績合計・平均・席次・受験者数印刷 04/11/03Modify
		     *         １回の処理で１学期４行を出力
		     *         中間処理の場合は中間成績と組平均だけを出力
		     *    2005/02/09 Modify 成績データの試験成績、学期成績、学年成績の取得仕様変更により修正
		     */

	    	if (param._isOutputDebug) {
	    		log.info(" goukei: " + goukei);
	    	}
            final int rsSemesInt = Integer.parseInt(getString("SEMESTER", goukei));
            final int IDX_SEISEKI1, IDX_SEISEKI2, IDX_SEISEKI3, IDX_KUMIHEIKIN;
            if (rsSemesInt == 1) {
                //ia = new int[] { 1, 2, 3, 4}; // 1学期中間成績、1学期期末成績、1学期学期成績、1学期組平均
                IDX_SEISEKI1 = 1;
                IDX_SEISEKI2 = 2;
                IDX_SEISEKI3 = 3;
                IDX_KUMIHEIKIN = 4;
            } else if (rsSemesInt == 2) {
                //ia = new int[] { 6, 7, 8, 9}; // 2学期中間成績、2学期期末成績、2学期学期成績、2学期組平均
                IDX_SEISEKI1 = 6;
                IDX_SEISEKI2 = 7;
                IDX_SEISEKI3 = 8;
                IDX_KUMIHEIKIN = 9;
            } else if (rsSemesInt == SEMESTER3) {
                //ia = new int[] {11,14,IDX15_GAKUNENMATSU_HYOTEI,12}; // 3学期期末成績、学年末学年成績、学年末評定、  3学期組平均
                IDX_SEISEKI1 = 11;
                IDX_SEISEKI2 = 14;
                IDX_SEISEKI3 = IDX15_GAKUNENMATSU_HYOTEI;
                IDX_KUMIHEIKIN = 12;
            } else {
            	continue;
            }

            // 1学期中間、2学期中間、3学期期末
            //３年は３学期の項目欄が無い(学年の欄がある)ので出力しない！
            final String TOTAL = "TOTAL";
            final String AVERAGE = "AVERAGE";
            final String ORDER = "ORDER";
            final String EXAMINEE = "EXAMINEE";
			if (rsSemesInt == SEMESTER3 && param._grade.equals(GRADE03)) {
            } else {
                //１・２学期では中間成績を出力、３学期(３年は無し)では期末成績を出力
				svf.VrsOutn(TOTAL,     IDX_SEISEKI1, getString("SUM_1", goukei));            //合計
                svf.VrsOutn(AVERAGE,   IDX_SEISEKI1, getString("AVG_1", goukei));            //平均
                if (getString("RANK_1", goukei) != null) {
                    svf.VrsOutn(ORDER,     IDX_SEISEKI1, getString("RANK_1", goukei));  //席次
                }
                final String count1 = getString("COUNT_1", goukei);
				if (count1 != null && Integer.parseInt(count1) != 0) {
                    svf.VrsOutn(EXAMINEE,  IDX_SEISEKI1, count1); //人数
                }
            }

            //期末成績および学期成績は当学期中間成績では出力しない！
            if (param.isKimatsu() ||  rsSemesInt < param._gakkiInt) {
                // 1学期期末、2学期期末、学年末学年成績
                //３学期(３年は学年)では学年成績を出力、以外は期末成績を出力
				svf.VrsOutn(TOTAL,     IDX_SEISEKI2, getString("SUM_2", goukei));        //合計
                svf.VrsOutn(AVERAGE,   IDX_SEISEKI2, getString("AVG_2", goukei));        //平均
                if (getString("RANK_2", goukei) != null) {
                    svf.VrsOutn(ORDER,     IDX_SEISEKI2, getString("RANK_2", goukei));  //席次
                }
                final String count2 = getString("COUNT_2", goukei);
				if (count2 != null && Integer.parseInt(count2) != 0) {
                    svf.VrsOutn(EXAMINEE,  IDX_SEISEKI2, count2); //人数
                }

                //３学期(３年は学年)では評定を出力、以外は学期成績を出力
                //評定の場合、席次と受験者数は出力しない！

				if (rsSemesInt != SEMESTER3) {
                    svf.VrsOutn(TOTAL,     IDX_SEISEKI3, getString("SUM_3", goukei));        //合計
                    svf.VrsOutn(AVERAGE,   IDX_SEISEKI3, getString("AVG_3", goukei));        //平均
                } else {
                    if (0 < student.schassesstotal) {
                        svf.VrsOutn(TOTAL,     IDX_SEISEKI3, String.valueOf(student.schassesstotal));           //合計
                        if (0 < student.schassesscnt) {
                            svf.VrsOutn(AVERAGE,   IDX_SEISEKI3, String.valueOf((float) Math.round((float) student.schassesstotal / (float) student.schassesscnt * 10) / 10));          //平均
                        }
                    }
                }

                if (rsSemesInt != SEMESTER3) {
                    final String rank3 = getString("RANK_3", goukei);
					if (rank3 != null) {
                        svf.VrsOutn(ORDER,     IDX_SEISEKI3, rank3);               //席次
                    }
                    final String count_3 = getString("COUNT_3", goukei);
					if (count_3 != null && Integer.parseInt(count_3) != 0) {
                        svf.VrsOutn(EXAMINEE,  IDX_SEISEKI3, count_3);              //人数
                    }
                } else {
                    if (param._gakkiInt == SEMESTER3 || (param._grade.equals(GRADE03) && param.isKimatsu() && param._gakkiInt == 2)) {
                        if (!student.gohihantei && !student.attendover) {
                            final String mark = _param.isNewSogoPrint() ? "認" : "合";
                            svf.VrsOutn(EXAMINEE, IDX_SEISEKI3, mark);               //合否判定
                        }
                    }
                    // 欠席日数、欠課時数超過者対応
                    if (param._gakkiInt == SEMESTER3 || (param._grade.equals(GRADE03) && param.isKimatsu())) {
                        if (getString("ATTENDOVER", goukei) != null &&  0 < Integer.parseInt(getString("ATTENDOVER", goukei))) {
                            svf.VrsOutn(EXAMINEE, IDX_SEISEKI3, "");               //合否判定
                        }
                        if (student.attendover) {
                            svf.VrsOutn(AVERAGE,   IDX_SEISEKI3, "");      //平均
                        }
                    }
                }
            }
            
            boolean printKumiheikin = true;
            if (NumberUtils.isDigits(student._entDateSemester) && rsSemesInt < Integer.parseInt(student._entDateSemester)) {
                // 生徒が年度途中に転入してきた際、転入以前の学期の組平均を表示しない
                printKumiheikin = false;
            }
            if (printKumiheikin) {
                //組平均を出力
				if (Integer.parseInt(param._grade) != 3 && rsSemesInt == SEMESTER3) {
                    svf.VrsOutn(AVERAGE, IDX_KUMIHEIKIN, StringUtils.defaultString(getString("SEM3REC_GR_RECORD_AVG", goukei)));      //組平均
                }
                if (rsSemesInt == SEMESTER3) {
                    svf.VrsOutn(AVERAGE, IDX16_GAKUNENMATSU_KUMIHEIKIN, StringUtils.defaultString(getString("GR_RECORD_AVG", goukei)));      //組平均
                } else {
                    svf.VrsOutn(AVERAGE, IDX_KUMIHEIKIN, StringUtils.defaultString(getString("GR_RECORD_AVG", goukei)));      //組平均
                }
            }
		}
	}

    /**
     *
     * SVF-OUT 科目および単位印刷
     */
    private static void setSubclass(final DB2UDB db2, final Param param, final List studentList) {
        ResultSet rs = null;        //明細データのRecordSet
        try {
            final String pskey = "ps4";
            if (null == param._psMap.get(pskey)) {
                final String sql4 = prestatementBeStudy(param);
                //log.debug("prestatementBeStudy sql = " + sql4);
                param._psMap.put(pskey, db2.prepareStatement(sql4));      //生徒別科目履修単位情報 04/11/09Add
            }
            final PreparedStatement ps4 = (PreparedStatement) param._psMap.get(pskey);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                
                int pp = 0;
                ps4.setString(++pp, student._schregno);  //学籍番号
                ps4.setString(++pp, student._schregno);  //学籍番号
//                t4s = System.currentTimeMillis();
                rs = ps4.executeQuery();            //明細データのRecordSet
//                t4 += System.currentTimeMillis() - t4s;
                student._subclassList = new ArrayList();
                for (int i = 0 ; rs.next() ; i++) {
                    final Subclass subclass = new Subclass(i + 1, rs.getString("SUBCLASSCD"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSNAME"), rs.getString("CREDITS"), "1".equals(rs.getString("IS_MOTOKAMOKU")), "1".equals(rs.getString("IS_SAKIKAMOKU")));
                    student.hmm.put(subclass._subclasscd, subclass);
                    student._subclassList.add(subclass);
                }
                DbUtils.closeQuietly(rs);
            }
        } catch (SQLException ex) {
            log.error("[KNJD170K]printSvfRegdOut error!", ex);
        } finally{
            DbUtils.closeQuietly(rs);
        }
    }

    /**
     *
     * SVF-OUT 科目および単位印刷
     */
    private void printSvfSubclassOut(final Vrw32alp svf, final Student student) {
        for (int i = 0; i < student._subclassList.size(); i++) {
            final Subclass subclass = (Subclass) student._subclassList.get(i);
            
            svf.VrsOut("CLASS" + (i+1), subclass._classname);
            
            final int keta = getMS932ByteCount(subclass._subclassname);
            if (30 < keta) {
                svf.VrsOut("SUBCLASS" + (i+1) + "_4", subclass._subclassname);
            } else if (24 < keta) {
                svf.VrsOut("SUBCLASS" + (i+1) + "_3", subclass._subclassname);
            } else if (14 < keta) {
                svf.VrsOut("SUBCLASS" + (i+1) + "_2", subclass._subclassname);
            } else {
                svf.VrsOut("SUBCLASS" + (i+1) + "_1", subclass._subclassname);
            }
            svf.VrsOut("CREDIT" + (i+1), subclass._credits);
        }
    }


    /**
     *
     * SVF-OUT 学籍、健康診断印刷
     *         04/11/03 単位出力を追加
     *         04/12/15 う歯が０の場合は印字しない
     *                  視力の少数第二位が０の場合、０は印字しない
     *         05/03/09 '1J01','2J01','3J01'等は'4J1','5J1','6J1'と表記する
     */
    private void printSvfRegdOut(final Vrw32alp svf, final String gradeHrClass, final Student student, final Param param) {
        svf.VrsOut("NENDO",     param._nendo);                                   //年度
        svf.VrsOut("GRADE",     String.valueOf(Integer.parseInt(param._grade)));   //学年

        if (!(gradeHrClass.substring(2,3).equals("J"))) {
            svf.VrsOut("HR_CLASS",  gradeHrClass.substring(2));                   //組
        } else {
            svf.VrsOut("HR_CLASS",  String.valueOf(Integer.parseInt(param._grade) + 3) + "J" + String.valueOf(Integer.parseInt(gradeHrClass.substring(3))));  //組
        }

        svf.VrsOut("ATTENDNO",  String.valueOf(student._attendno));   //出席番号
        final String printName = "1".equals(student._useRealName) ? student._realName : student._name;
        svf.VrsOut("NAME" + (getMS932ByteCount(printName) > 26 ? "2" : ""), printName);                  //生徒名
        svf.VrsOut("STAFFNAME", student._staffname);                              //学級担任
        svf.VrsOut("MAJOR",     student._majorname);               //学科名 05/05/23

        svf.VrsOut("HEIGHT",      student._height);          //身長
        svf.VrsOut("WEIGHT",      student._weight);          //体重
        svf.VrsOut("SITHEIGHT",   student._sitheight) ;      //座高
        svf.VrsOut("R_EYESIGHT1", printSvfRegdOutVision(student._rBarevision));//視力（右）
        svf.VrsOut("R_EYESIGHT2", printSvfRegdOutVision(student._rVision));  //視力（右・矯正）
        svf.VrsOut("L_EYESIGHT1", printSvfRegdOutVision(student._lBareVision));//視力（左）
        svf.VrsOut("L_EYESIGHT2", printSvfRegdOutVision(student._lVision));  //視力（左・矯正）
        if (student._remainadulttooth != null  &&  0 < Integer.parseInt(student._remainadulttooth)) {
            svf.VrsOut("TEETH",       student._remainadulttooth);        //う歯
        }

    }//printSvfRegdOut()の括り


    /**
     *
     * SVF-OUT 健康診断印刷 視力出力
     *         04/12/15
     */
    private String printSvfRegdOutVision(final String vision) {

        String str = null;
        if (vision != null) {
            if (3 < vision.length()) {
                if (vision.substring(vision.length() - 1, vision.length()).equals("0")) {
                    str = vision.substring(0, vision.length() - 1);
                } else {
                    str = vision;
                }
            } else {
                str = vision;
            }
        } else {
            str = "";
        }

        return str;
    }

    private static String getString(final String key, final Map m) {
        if (!m.containsKey(key)) {
            throw new IllegalStateException("フィールドがありません: key = "+ key + ", data = " + m);
        }
        return (String) m.get(key);
    }
    
    private static int getInt(final String key, final Map m) {
        final String v = getString(key, m);
        if (v == null) {
            return 0;
        }
        return Integer.parseInt(v);
    }
    
    private static Map createMap(final ResultSet rs) throws SQLException {
        final Map m = new HashMap();
        final ResultSetMetaData meta = rs.getMetaData();
        for (int col = 1; col <= meta.getColumnCount(); col++) {
            final String key = meta.getColumnName(col);
            final String val = rs.getString(col);
            m.put(key, val);
        }
        return m;
    }

    /**
     *
     * SVF-OUT 出欠明細印刷処理
     */
    private static void setAttendSemes(final DB2UDB db2, final Param param, final List studentList) {
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            //------------------printSvfAttendOut(-, -, -, 集計開始学期, 集計終了学期,出力列)
            ResultSet rs = null;
            try {
                final String pskey = "ps3";
                if (null == param._psMap.get(pskey)) {
                    final String sql = AttendAccumulate.getAttendSemesSql(
                    		param._year,
                    		param._gakki,
                            null,
                            param._date,
                            param._attendParamMap
                    );
                    //log.debug(" *********************** " + prestatementAttendSql);
                    param._psMap.put(pskey, db2.prepareStatement(sql)); //出欠データ
                }
                final PreparedStatement ps3 = (PreparedStatement) param._psMap.get(pskey);

                int pp = 0;
                ps3.setString(++pp, student._schregno);                 //生徒番号
//                t3s = System.currentTimeMillis();
                rs = ps3.executeQuery();
//                t3 += System.currentTimeMillis() - t3s;
                while (rs.next()) {
                    if ("9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    student._attendSemesMap.put(rs.getString("SEMESTER"), createMap(rs));
                }
            } catch (Exception ex) {
                log.error("[KNJD171K]printSvfAttendOut error!", ex);
            } finally{
                DbUtils.closeQuietly(rs);
            }
        }
    }

    /**
     *
     * SVF-OUT 出欠明細印刷処理
     */
    private void printSvfAttend(final Vrw32alp svf, final Student student) {
        int arrattend[] = {0,0,0,0,0,0,0,0,0};
        
        for (final Iterator it = student._attendSemesMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next();
            final Map rsMap = (Map) student._attendSemesMap.get(semester);
            
            final int k = getInt("SEMESTER", rsMap);
            svf.VrsOutn("LESSON1",  k,   getString("LESSON", rsMap));      //授業日数
            svf.VrsOutn("KIBIKI",   k,   getString("MOURNING", rsMap));        //忌引日数
            svf.VrsOutn("SUSPEND",  k,   String.valueOf(getInt("SUSPEND", rsMap) + getInt("VIRUS", rsMap) + getInt("KOUDOME", rsMap)));        //出停日数
            svf.VrsOutn("ABROAD",   k,   getString("TRANSFER_DATE", rsMap));   //留学日数
            svf.VrsOutn("LESSON2",  k,   getString("MLESSON", rsMap));     //出席すべき日数
            svf.VrsOutn("ATTEND",   k,   getString("PRESENT", rsMap));     //出席日数
            svf.VrsOutn("ABSENCE",  k,   getString("SICK", rsMap));        //欠席日数
            svf.VrsOutn("LATE",     k,   getString("LATE", rsMap));            //遅刻回数
            svf.VrsOutn("LEAVE",    k,   getString("EARLY", rsMap));           //早退回数
            
            arrattend[0] += Integer.parseInt(getString("LESSON", rsMap));
            arrattend[1] += Integer.parseInt(getString("MOURNING", rsMap));
            arrattend[2] += getInt("SUSPEND", rsMap) + getInt("VIRUS", rsMap) + getInt("KOUDOME", rsMap);
            arrattend[3] += Integer.parseInt(getString("MLESSON", rsMap));
            arrattend[4] += Integer.parseInt(getString("PRESENT", rsMap));
            arrattend[5] += Integer.parseInt(getString("SICK", rsMap));
            arrattend[6] += Integer.parseInt(getString("LATE", rsMap));
            arrattend[7] += Integer.parseInt(getString("EARLY", rsMap));
            arrattend[8] += Integer.parseInt(getString("TRANSFER_DATE", rsMap));
        }
        
        svf.VrsOutn("LESSON1",  4,   String.valueOf(arrattend[0]));        //授業日数
        svf.VrsOutn("KIBIKI",   4,   String.valueOf(arrattend[1]));        //忌引日数
        svf.VrsOutn("SUSPEND",  4,   String.valueOf(arrattend[2]));        //出停日数
        svf.VrsOutn("ABROAD",   4,   String.valueOf(arrattend[8]));        //留学日数
        svf.VrsOutn("LESSON2",  4,   String.valueOf(arrattend[3]));        //出席すべき日数
        svf.VrsOutn("ATTEND",   4,   String.valueOf(arrattend[4]));        //出席日数
        svf.VrsOutn("ABSENCE",  4,   String.valueOf(arrattend[5]));        //欠席日数
        svf.VrsOutn("LATE",     4,   String.valueOf(arrattend[6]));        //遅刻回数
        svf.VrsOutn("LEAVE",    4,   String.valueOf(arrattend[7]));        //早退回数
    }

    private static void setSubclassAttend(final DB2UDB db2, final Param param, final List studentList) {
        ResultSet rs = null;

        try {
            
            final String pskey = "ps52";
            if (null == param._psMap.get(pskey)) {
                final String sql52 = AttendAccumulate.getAttendSubclassSql(
                		param._year,
                		param._gakki,
                        null,
                        param._date,
                        param._attendParamMap
                );

                param._psMap.put(pskey, db2.prepareStatement(sql52)); //生徒別科目別欠課時数・授業時数
            }
            final PreparedStatement ps52 = (PreparedStatement) param._psMap.get(pskey);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                
                int pp = 0;
                ps52.setString(++pp, student._schregno);                    //生徒番号
                
//                t5s = System.currentTimeMillis();
                rs = ps52.executeQuery();
//                t5 += System.currentTimeMillis() - t5s;
                while (rs.next()) {
                    final Subclass subclass = (Subclass) student.hmm.get(StringUtils.replace(rs.getString("SUBCLASSCD"), "-", "")); //科目コードより列番号を取得
                    if (subclass == null) {
                        continue;
                    }
                    final String semester = "9".equals(rs.getString("SEMESTER")) ? "4" : rs.getString("SEMESTER");
                    subclass._semesterLesson.put(semester, rs.getString("LESSON"));
                    if ("1".equals(rs.getString("IS_COMBINED_SUBCLASS"))) {
                        subclass._semesterAbsent2.put(semester, rs.getString("REPLACED_SICK"));
                    } else {
                        subclass._semesterAbsent2.put(semester, rs.getString("SICK2"));
                    }
                }
                DbUtils.closeQuietly(rs);
            }
        } catch (Exception ex) {
            log.error("[KNJD171K]printSvfSubclassAttendOut! ", ex);
        } finally{
            DbUtils.closeQuietly(rs);
        }
    }


    /**
     *
     * SVF-OUT 欠課時数および授業時数印刷
     * 2005/05/31 Modify 欠課時数は、学年末は３学期期末のみ出力
     */
    private void printSvfSubclassAttendOut(final Vrw32alp svf, final Param param, final Student student) {
        for (final Iterator it = student.hmm.values().iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            
            for (final Iterator its = subclass._semesterLesson.keySet().iterator(); its.hasNext();) {
                final String semester = (String) its.next();
                
                final String lesson = (String) subclass._semesterLesson.get(semester);
                final int idx;
                final int semesi = Integer.parseInt(semester);
                if (semesi == 1) {
                    idx = 18;
                } else if (semesi == 2) {
                	idx = 19;
                } else if (semesi == SEMESTER3) {
                	idx = 20;
                } else if (semesi == 4) {
                	idx = 21;
                } else {
                	continue;
                }
                svf.VrsOutn("POINT" + subclass._line, idx, (lesson != null  &&  0 < Integer.parseInt(lesson)) ? lesson  : ""); //授業時数
            }
            for (final Iterator its = subclass._semesterAbsent2.keySet().iterator(); its.hasNext();) {
                final String semester = (String) its.next();
                
                final String absent2 = (String) subclass._semesterAbsent2.get(semester);
                final int idx;
                final int semesi = Integer.parseInt(semester);
                if (semesi == 1) {
                    idx = 5;
                } else if (semesi == 2) {
                    idx = 10;
                } else if (semesi == SEMESTER3) {
                	idx = 13;
                } else if (semesi == 4) {
                    idx = 17;
                } else {
                	continue;
                }
                
                //if (Integer.parseInt(semester) == 4  &&
                //    (3 > param._gakkiInt  ||
                //      Integer.parseInt(param._testkindcd) == 1)) continue;   //05/05/31 学年末は３学期期末のみ出力
                //05/11/09 条件を変更
                if (Integer.parseInt(semester) == 4) {
                    if (param.isChukan())continue;
                    if (param._grade.equals("01")  &&  param._gakkiInt != SEMESTER3)continue;
                    if (param._grade.equals("02")  &&  param._gakkiInt != SEMESTER3)continue;
                    if (param._grade.equals(GRADE03)  &&  param._gakkiInt == 1)continue;
                }
                svf.VrsOutn("POINT" + subclass._line, idx, (absent2 != null &&  0 < Integer.parseInt(absent2)) ? absent2 : ""); //欠課時数
            }
        }
    }

    /**
     * SVF-OUT 総合的な学習の時間の所見を印刷
     */
    private static void setTotalstudyTimeDat(final DB2UDB db2, final Param param, final List studentList) {
        ResultSet rs = null;
        try {
            final String pskey = "ps6";
            if (null == param._psMap.get(pskey)) {
                final String sql = prestatementRecordTotalstudytimeDat(param); // 文字評価
                param._psMap.put(pskey, db2.prepareStatement(sql));
            }
            final PreparedStatement ps6 = (PreparedStatement) param._psMap.get(pskey);
            
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                
                int pp = 0;
                ps6.setString(++pp, student._schregno);                    //生徒番号
                rs = ps6.executeQuery();
                
                if (rs.next()) {
                    student._totalstudyact = rs.getString("TOTALSTUDYACT");
                    student._totalstudytime = rs.getString("TOTALSTUDYTIME");
                }
                DbUtils.closeQuietly(rs);
            }
        } catch (Exception ex) {
            log.error("[KNJD171K]printSvfTotalstudyTimeDat", ex);
        } finally{
            DbUtils.closeQuietly(rs);
        }
    }

    /**
     * SVF-OUT 総合的な学習の時間の所見を印刷
     */
    private void printSvfTotalstudyTimeDat(final Vrw32alp svf, final Student student) {

        final String[] totalstudyact = get_token_1(student._totalstudyact, 11 * 2, 4);
        if (null != totalstudyact) {
            for (int i = 0; i < totalstudyact.length; i++) {
                svf.VrsOutn("TOTALSTUDYACT", i + 1, totalstudyact[i]);
            }
        }

        final String[] totalstudytime = get_token_1(student._totalstudytime, 11 * 2, 6);
        if (null != totalstudytime) {
            for (int i = 0; i < totalstudytime.length; i++) {
                svf.VrsOutn("TOTALSTUDYVAL", i + 1, totalstudytime[i]);
            }
        }
    }
    
    private static int getMS932ByteCount(final String s) {
    	return KNJ_EditEdit.getMS932ByteLength(s);
    }

    private static String[] get_token_1(final String strx0, final int f_len, final int f_cnt) {

        if (strx0 == null || strx0.length() == 0) {
            return null;
        }
        final String strx = StringUtils.replace(strx0, "\r\n", "\n");
        final String stoken[] = new String[f_cnt];        //分割後の文字列の配列
        int slen = 0;                               //文字列のバイト数カウント
        int s_sta = 0;                              //文字列の開始位置
        int ib = 0;
        for (int s_cur = 0; s_cur < strx.length() && ib < f_cnt; s_cur++) {
            //改行マークチェック
            if (strx.charAt(s_cur) == '\r') {
                continue;
            }
            if (strx.charAt(s_cur) == '\n') {
                stoken[ib++] = strx.substring(s_sta, s_cur);
                slen = 0;
                s_sta = s_cur + 1;
            } else {
                //文字数チェック
                final int sbytelen = getMS932ByteCount(strx.substring(s_cur, s_cur + 1));                  //1文字byteカウント用
                slen += sbytelen;
                if (slen > f_len) {
                    stoken[ib++] = strx.substring(s_sta, s_cur);
                    slen = sbytelen;
                    s_sta = s_cur;
                }
            }
        }
        if (slen > 0 && ib < f_cnt) {
            stoken[ib] = strx.substring(s_sta);
        }
//        for (int i = 0; i < stoken.length; i++) {
//            log.debug("#### stoken[" + i + "] = " + stoken[i]);
//        }
        return stoken;

    }//String get_token()の括り

    /**
     *  PrepareStatement作成
     *  学籍・健康診断データ-->生徒別
     *    健康診断データは期末成績の場合( param._testkindcd=="99" )だけ出力
     */
    private static String prestatementRegd(final Param param) {
        final StringBuffer stb = new StringBuffer();

        //異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH SCHNO AS(");
        stb.append(    "SELECT  T1.SCHREGNO, T1.ATTENDNO, T1.GRADE, T1.COURSECD, T1.MAJORCD ");
        stb.append(    "FROM    SCHREG_REGD_DAT T1, V_SEMESTER_GRADE_MST T2 ");
        stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
        stb.append(        "AND T1.SEMESTER = '"+param._gakki+"' ");
        stb.append(        "AND T1.YEAR = T2.YEAR ");
        stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
        stb.append(        "AND T1.GRADE = T2.GRADE ");
        stb.append(        "AND T1.GRADE = ? ");
        stb.append(        "AND T1.HR_CLASS = ? ");
        if (param._category_selected != null) {
            stb.append(    "AND T1.SCHREGNO IN" + param._category_selected + " ");
        }
        //在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
        //05/10/07Build 転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append(                           "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
        stb.append(                             "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END))) ");

        if (param.isKimatsu()  &&  param._gakkiInt == SEMESTER3) {
            //学年成績における異動情報チェック:留学(1)・休学(2)・停学(3)・編入(4)
            //05/02/10Modify 停学をチェックから除外
            //05/03/12Modify 留学開始日が３学期の場合(短期留学)は対象とする
            //05/03/16Modify 留学および休学が各学期終了日（学期終了日が異動基準日より大きい場合は異動基準日）にかからない場合は対象とする
            stb.append(    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                   "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(                       "AND ((S1.TRANSFERCD IN ('2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(                         "OR (S1.TRANSFERCD IN ('1') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE AND S1.TRANSFER_EDATE < '" + param._gakki3Sdate + "' ))) ");
        } else {
            //学期成績における異動情報チェック:留学(1)・休学(2)・停学(3)・編入(4)
            //05/06/08Modiy 学年成績以外は留学生は出力しない
            stb.append(    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                   "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(                       "AND ((S1.TRANSFERCD IN ('2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(                         "OR (S1.TRANSFERCD IN ('1') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE))) ");
        }
        stb.append(")");

        //メイン表
        stb.append("SELECT  W1.ATTENDNO, W1.SCHREGNO, W2.NAME, W5.MAJORNAME ");
        stb.append(    ", W3.HEIGHT, W3.WEIGHT, W3.SITHEIGHT, W3.R_BAREVISION, W3.L_BAREVISION, W3.R_VISION, W3.L_VISION,");
        stb.append(    "W4.ADULTTOOTH, ");
        stb.append(    "VALUE(W4.REMAINADULTTOOTH,0) + VALUE(W4.REMAINBABYTOOTH,0) AS REMAINADULTTOOTH, ");
        stb.append(       "W2.REAL_NAME, ");
        stb.append(       "(CASE WHEN W6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
        stb.append(       "ENT_SEME.SEMESTER AS ENT_DATE_SEMESTER "); // 入学日付が指定年度の場合のみ取得

        stb.append("FROM    SCHNO W1 ");
        stb.append(        "INNER JOIN SCHREG_BASE_MST W2 ON W1.SCHREGNO = W2.SCHREGNO ");
        stb.append(        "LEFT JOIN MAJOR_MST W5 ON W5.COURSECD = W1.COURSECD AND W5.MAJORCD = W1.MAJORCD ");
        stb.append(    "LEFT JOIN MEDEXAM_DET_DAT   W3 ON W3.YEAR = '" + param._year + "' AND W3.SCHREGNO = W1.SCHREGNO ");
        stb.append(    "LEFT JOIN MEDEXAM_TOOTH_DAT W4 ON W4.YEAR = '" + param._year + "' AND W4.SCHREGNO = W1.SCHREGNO ");
        stb.append(    "LEFT JOIN SCHREG_NAME_SETUP_DAT W6 ON W6.SCHREGNO = W1.SCHREGNO AND W6.DIV = '03' ");
        stb.append(    "LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = '" + param._year + "' AND GDAT.GRADE = '" + param._grade + "' ");
        stb.append(    "LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = W1.SCHREGNO AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        stb.append(    "LEFT JOIN V_SEMESTER_GRADE_MST ENT_SEME ON ENT_SEME.YEAR = '" + param._year + "' AND ENT_SEME.SEMESTER <> '9' AND ENT_SEME.GRADE = W1.GRADE AND ENTGRD.ENT_DATE BETWEEN ENT_SEME.SDATE AND ENT_SEME.EDATE ");
        stb.append("ORDER BY 1");
        return stb.toString();

    }//prestatementRegd()の括り


    /**
     *
     *  prestatementTotal PrepareStatement
     *  成績集計欄用statement作成
     *  2005/02/09 Modify 成績データの試験成績、学期成績、学年成績の取得仕様を変更
     *  2005/05/31 Modify 組平均は、中間成績の場合は中間試験を出力する
     */
    private static String sqlGoukei(final Param param) {
        final StringBuffer stb = new StringBuffer();

        //異動者を除外した学籍の表
        //   => 組平均において異動者の除外に使用
        stb.append("WITH T_GRADE_HR_CLASS (GRADE, HR_CLASS) AS(");
        stb.append(" VALUES(?, ?) ");
        stb.append(") , SCHNO AS(");
        for (int i = 1; i <= param._gakkiInt; i++) {
            if (i > 1) {
                stb.append("UNION ");
            }
            stb.append(prestatementTotalSchno(param, i));
        }
        stb.append(")");

        //SCHNO表より出力指定の生徒だけを抽出した学籍の表
        stb.append(",SCHNO_B AS(");
        stb.append(    "SELECT  SCHREGNO, ATTENDNO, SEMESTER ");
        stb.append(    "FROM    SCHREG_REGD_DAT W2 ");
        stb.append(    "WHERE   YEAR = '" + param._year + "' AND ");
        stb.append(            "(GRADE, HR_CLASS) IN (SELECT GRADE, HR_CLASS FROM T_GRADE_HR_CLASS) ");
        if (param._category_selected != null)
            stb.append(    "AND SCHREGNO IN " + param._category_selected);
        //  ３年生は仮評定を表記するので、１・２学期の出力では３学期のSCHNO表を作成しておく
        if (param._gakkiInt < SEMESTER3  &&  param._grade.equals(GRADE03)) {
            stb.append("UNION ");
            stb.append("SELECT  SCHREGNO, ATTENDNO, '3' AS SEMESTER ");
            stb.append("FROM    SCHREG_REGD_DAT W2 ");
            stb.append("WHERE   YEAR = '" + param._year + "' AND ");
            stb.append(        "(GRADE, HR_CLASS) IN (SELECT GRADE, HR_CLASS FROM T_GRADE_HR_CLASS) ");
            if (param._category_selected != null) {
                stb.append("AND SCHREGNO IN " + param._category_selected);
            }
        }
        stb.append( " ) ");

        //KIN_RECORD_DAT成績の表
        stb.append(",RECORD_DAT AS(");
        stb.append(    "SELECT T1.* ");
        stb.append(    "FROM   KIN_RECORD_DAT T1 ");
        stb.append(    "LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'D065' AND NAME1 = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
        stb.append(    "WHERE  T1.YEAR = '" + param._year + "' AND ");
        stb.append(           "SUBSTR(SUBCLASSCD,1,2) < '90' ");  //総合学習を除く
        stb.append(           "AND L1.NAME1 IS NULL ");
        stb.append(    ") ");

        //成績明細の表 RECORD_DAT表を学期別にレコード化
        //各フィールドには次の値を入れる
        //  １・２学期は SCORE1:中間素点  SCORE2:期末素点  SCORE3:学期成績  SEM3REC:なし
        //    ３  学期は SCORE1:期末素点  SCORE2:学年成績  SCORE3:学年評定  SEM3REC:３学期成績
        stb.append(",RECORD_DAT_A AS(");
        stb.append(    "SELECT '1' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, ");
        stb.append(    "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD,");
        stb.append(            "CASE WHEN SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS') THEN 'KK' ");
        stb.append(                 "WHEN SEM1_INTER_REC IS NOT NULL THEN RTRIM(CHAR(SEM1_INTER_REC)) ");
        stb.append(                 "ELSE NULL END AS SCORE1, ");
        stb.append(            "CASE WHEN SEM1_TERM_REC IS NULL AND SEM1_TERM_REC_DI IN('KK','KS') THEN 'KK' ");
        stb.append(                 "WHEN SEM1_TERM_REC IS NOT NULL THEN RTRIM(CHAR(SEM1_TERM_REC)) ");
        stb.append(                 "ELSE NULL END AS SCORE2, ");
        stb.append(            "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
        stb.append(                       "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM1_REC_FLG,'0') = '0' THEN 'KK' ");
        stb.append(                 "WHEN SEM1_REC IS NOT NULL THEN RTRIM(CHAR(SEM1_REC)) ");
        stb.append(                 "ELSE NULL END AS SCORE3 ");
        stb.append(            ",'' AS SEM3REC ");
        stb.append(    "FROM    RECORD_DAT W1 ");
        stb.append(            "INNER JOIN SCHNO W2 ON W2.SEMESTER = '1' AND W1.SCHREGNO = W2.SCHREGNO ");

        if (1 < param._gakkiInt) {
            stb.append("UNION SELECT '2' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO,");
            stb.append(    "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD,");
            stb.append(       "CASE WHEN SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS') THEN 'KK' ");
            stb.append(            "WHEN SEM2_INTER_REC IS NOT NULL THEN RTRIM(CHAR(SEM2_INTER_REC)) ");
            stb.append(            "ELSE NULL END AS SCORE1, ");
            stb.append(       "CASE WHEN SEM2_TERM_REC IS NULL AND SEM2_TERM_REC_DI IN('KK','KS') THEN 'KK' ");
            stb.append(            "WHEN SEM2_TERM_REC IS NOT NULL THEN RTRIM(CHAR(SEM2_TERM_REC)) ");
            stb.append(            "ELSE NULL END AS SCORE2, ");
            stb.append(       "CASE WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
            stb.append(                  "(SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
            stb.append(                  "VALUE(SEM2_REC_FLG,'0') = '0' THEN 'KK' ");
            stb.append(            "WHEN SEM2_REC IS NOT NULL THEN RTRIM(CHAR(SEM2_REC)) ");
            stb.append(            "ELSE NULL END AS SCORE3 ");
            stb.append(            ",'' AS SEM3REC ");
            stb.append("FROM   RECORD_DAT W1 ");
            stb.append(       "INNER JOIN SCHNO W2 ON W2.SEMESTER = '2' AND W1.SCHREGNO = W2.SCHREGNO ");
        }

        //  ３年生は仮評定を表記するので、１・２学期の出力でも３学期のRECORD_DAT表を作成しておく
        if (2 < param._gakkiInt  ||  param._grade.equals(GRADE03)) {
            stb.append("UNION SELECT '3' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, ");
            stb.append(    "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD,");
            if (param._grade.equals(GRADE03)) {
                //３年生は３学期期末の成績はないのでSCORE1をNULLとしておく
                stb.append(   "NULLIF(W1.SCHREGNO,W2.SCHREGNO) AS SCORE1, ");
            } else {
                stb.append(   "CASE WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND VALUE(SEM3_TERM_REC_FLG,'0') = '0' THEN 'KK' ");
                stb.append(        "WHEN SEM3_TERM_REC IS NOT NULL THEN RTRIM(CHAR(SEM3_TERM_REC)) ");
                stb.append(        "ELSE NULL END AS SCORE1, ");
            }

            stb.append(   "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR (SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM1_REC_FLG,'0') = '0' THEN 'KK' ");
            stb.append(        "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR (SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM2_REC_FLG,'0') = '0' THEN 'KK' ");
            if (!param._grade.equals(GRADE03)) {
                stb.append(    "WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND VALUE(SEM3_TERM_REC_FLG,'0') = '0' THEN 'KK' ");
            }
            stb.append(        "WHEN GRADE_RECORD IS NOT NULL THEN RTRIM(CHAR(GRADE_RECORD)) ");
            stb.append(        "ELSE NULL END AS SCORE2, ");

            stb.append(   "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR (SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM1_REC_FLG,'0') = '0' THEN 'KK' ");
            stb.append(        "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR (SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM2_REC_FLG,'0') = '0' THEN 'KK' ");
            if (!param._grade.equals(GRADE03)) {
                stb.append(    "WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND VALUE(SEM3_TERM_REC_FLG,'0') = '0' THEN 'KK' ");
            }
            stb.append(        "WHEN GRADE_RECORD IS NOT NULL THEN ");
            stb.append(                 "CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS ");
            stb.append(                                    "WHEN 'B' THEN B_PATTERN_ASSESS ");
            stb.append(                                    "WHEN 'C' THEN C_PATTERN_ASSESS ELSE NULL END ");
            stb.append(        "ELSE NULL END AS SCORE3 ");

            if (param._grade.equals(GRADE03)) {
                //３年生は３学期成績はないのでSEM3RECを''としておく
                stb.append(   ",'' AS SEM3REC ");
            } else {
                stb.append(   ",CASE WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND VALUE(SEM3_TERM_REC_FLG,'0') = '0' THEN 'KK' ");
                stb.append(         "WHEN SEM3_TERM_REC IS NOT NULL THEN RTRIM(CHAR(SEM3_TERM_REC)) ");
                stb.append(        "ELSE NULL END AS SEM3REC ");
            }

            stb.append("FROM   RECORD_DAT W1 ");
            stb.append("INNER JOIN SCHNO W2 ON W2.SEMESTER = '" + param._gakki + "' AND W1.SCHREGNO = W2.SCHREGNO ");
        }
        stb.append( ") ");


        //RECORD_DAT_A表より評価読替(元)科目を抽出　05/03/14 Build
        //  評価読替科目が参照されるのは学年末だけなので、SEMESTER='3'とする
        stb.append(",REPLACE_REC AS(");
        stb.append(     "SELECT  '3' AS SEMESTER, ATTENDNO, SCHREGNO, ");
        stb.append(             "W2.COMBINED_CLASSCD || W2.COMBINED_SCHOOL_KIND || W2.COMBINED_CURRICULUM_CD || W2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
        stb.append(             "W2.ATTEND_CLASSCD || W2.ATTEND_SCHOOL_KIND || W2.ATTEND_CURRICULUM_CD || W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
        stb.append(             "SCORE2 AS GRADE_RECORD ");  //SCORE2: RECORD_A表は３学期はGRADE_RECORDが入り、以外はSEM?_TERM_RECが入っている
        stb.append(     "FROM    RECORD_DAT_A W1, SUBCLASS_REPLACE_COMBINED_DAT W2 ");
        stb.append(     "WHERE ");
        stb.append(     "W1.SUBCLASSCD = W2.ATTEND_CLASSCD || W2.ATTEND_SCHOOL_KIND || W2.ATTEND_CURRICULUM_CD || W2.ATTEND_SUBCLASSCD AND ");
        stb.append(             "W2.YEAR = '" + param._year + "' AND ");
        stb.append(             "W2.REPLACECD = '1' AND ");
        if (param._gakkiInt == SEMESTER3) {
            stb.append(         "W1.SEMESTER = '3' ");
        } else {
            stb.append(         "W1.SEMESTER = '" + param._gakki + "' ");
        }
        stb.append(     ") ");

        //REPLACE_REC表より評価(元)科目に欠席が１つ以上ある生徒別評価(先)科目別表
        stb.append(",REPLACE_REC_CNT AS(");
        stb.append(     "SELECT  SCHREGNO, SUBCLASSCD, SUM(CASE WHEN GRADE_RECORD IN ('KS','( )','欠') THEN 1 ELSE 0 END) AS KS ");
        stb.append(     "FROM    REPLACE_REC W1 ");
        stb.append(     "WHERE   W1.GRADE_RECORD IN('KK','KS','( )','欠','公') ");
        stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
        stb.append(     "HAVING 0 < COUNT(*) ");
        stb.append(     ") ");

        //REPLACE_REC表より科目読替(先)科目の表(評定付き)
        //  評定は科目読替(元)科目の平均で類型グループより求める
        //  REPLACEMOTOフィールドは評価読替フラグとし、-1を入れる
        stb.append(",REPLACE_REC_B AS(");
        stb.append(     "SELECT  '3' AS SEMESTER, W1.ATTENDNO, W1.SCHREGNO, ");
        stb.append(             "W1.SUBCLASSCD, ");
        stb.append(             "CASE WHEN SCHREGNO IS NOT NULL THEN NULL ELSE '0' END AS SCORE1, ");
        stb.append(             "CASE WHEN SCHREGNO IS NOT NULL THEN NULL ELSE '0' END AS SCORE2, ");
        stb.append(             "(SELECT  TYPE_ASSES_LEVEL ");
        stb.append(              "FROM    TYPE_ASSES_MST W1 ");
        stb.append(              "WHERE   W1.YEAR = '" + param._year + "' AND ");
        stb.append(                      "W1.TYPE_ASSES_CD = W2.GRADE_RECORD_TYPE_ASSES_CD AND ");
        stb.append(                      "GRADE_RECORD BETWEEN TYPE_ASSES_LOW AND TYPE_ASSES_HIGH) AS SCORE3, ");
        stb.append(             "-1 AS REPLACEMOTO ");
        stb.append(     "FROM(");
        stb.append(             "SELECT  ATTENDNO, SCHREGNO, SUBCLASSCD, ");
        stb.append(                     "INT(ROUND(AVG(FLOAT(INT(GRADE_RECORD))),0)) AS GRADE_RECORD ");
        stb.append(             "FROM    REPLACE_REC W1 ");
        if (param._gakkiInt == SEMESTER3) {
            stb.append(         "WHERE   EXISTS(SELECT 'X' FROM SCHNO W3 WHERE W3.SCHREGNO = W1.SCHREGNO AND W3.SEMESTER = '3') ");
        } else {
            stb.append(         "WHERE   EXISTS(SELECT 'X' FROM SCHNO W3 WHERE W3.SCHREGNO = W1.SCHREGNO AND W3.SEMESTER = '" + param._gakki + "') ");
        }

        stb.append(                     "AND W1.GRADE_RECORD NOT IN('KK','KS','( )','欠','公') AND W1.GRADE_RECORD IS NOT NULL ");
        stb.append(                     "AND NOT EXISTS(SELECT 'X' FROM REPLACE_REC_CNT W2 WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.SUBCLASSCD = W1.SUBCLASSCD) ");
        stb.append(             "GROUP BY ATTENDNO, SCHREGNO, SUBCLASSCD ");
        stb.append(         ")W1 ");
        stb.append(     "LEFT JOIN (");
        stb.append(               "SELECT  GRADE_RECORD_TYPE_ASSES_CD,  CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD ");
        stb.append(               "FROM    TYPE_GROUP_MST S1,TYPE_GROUP_HR_DAT S2 ");
        stb.append(               "WHERE   S1.YEAR = '" + param._year + "' AND S1.YEAR = S2.YEAR AND ");
        stb.append(                       "S1.TYPE_GROUP_CD = S2.TYPE_GROUP_CD AND ");
        stb.append(                       "(S2.GRADE, S2.HR_CLASS) IN (SELECT GRADE, HR_CLASS FROM T_GRADE_HR_CLASS) ");
        stb.append(         ")W2 ON W1.SUBCLASSCD = W2.SUBCLASSCD ");
        stb.append(    ")");

        //RECORD_DAT_A表 REPLACE_REC_B表 より抽出した本表　05/03/14
        stb.append(",RECORD_DAT_B AS (");
        //RECORD_DAT_A表より評価読替(先)(元)科目を除外した表
        stb.append(       "SELECT  W1.SEMESTER, W1.SUBCLASSCD, W1.ATTENDNO, W1.SCHREGNO, ");
        stb.append(               "W1.SCORE1, W1.SCORE2, W1.SCORE3, W1.SEM3REC ");
        stb.append(       "FROM    RECORD_DAT_A W1 ");
        stb.append(       "LEFT JOIN REPLACE_REC W21 ON W1.SUBCLASSCD = W21.SUBCLASSCD ");
        stb.append(       "LEFT JOIN REPLACE_REC W22 ON W1.SUBCLASSCD = W22.ATTEND_SUBCLASSCD ");
        stb.append(       "WHERE W21.SUBCLASSCD IS NULL AND W22.ATTEND_SUBCLASSCD IS NULL ");
        //RECORD_DAT_A表より1・２学期の評価読替(元)科目の表
        stb.append(       "UNION ALL ");
        stb.append(       "SELECT  W1.SEMESTER, W1.SUBCLASSCD, W1.ATTENDNO, W1.SCHREGNO, ");
        stb.append(               "W1.SCORE1, ");
        stb.append(               "CASE WHEN W1.SEMESTER <> '3' THEN W1.SCORE2 END AS SCORE2, ");
        stb.append(               "CASE WHEN W1.SEMESTER < '3' THEN W1.SCORE3 WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '0' END AS SCORE3, W1.SEM3REC ");
        stb.append(       "FROM    RECORD_DAT_A W1 ");
        stb.append(              " INNER JOIN (SELECT DISTINCT ATTEND_SUBCLASSCD FROM REPLACE_REC) W2 ON W1.SUBCLASSCD = W2.ATTEND_SUBCLASSCD ");
        // 学年成績 (SEMESTER=3、SCORE2)は合併元ではなく合併先を加算する
        stb.append(       "UNION ALL ");
        stb.append(       "SELECT  W1.SEMESTER, W1.SUBCLASSCD, W1.ATTENDNO, W1.SCHREGNO, ");
        stb.append(               "CAST(NULL AS VARCHAR(3)) AS SCORE1, ");
        stb.append(               "CASE WHEN W1.SEMESTER = '3' THEN W1.SCORE2 END AS SCORE2, ");
        stb.append(               "CAST(NULL AS VARCHAR(3)) AS SCORE3, CAST(NULL AS VARCHAR(3)) AS SEM3REC ");
        stb.append(       "FROM    RECORD_DAT_A W1 ");
        stb.append(              " INNER JOIN (SELECT DISTINCT SUBCLASSCD FROM REPLACE_REC) W2 ON W1.SUBCLASSCD = W2.SUBCLASSCD ");
        stb.append(       "WHERE W1.SEMESTER = '3' ");
        //REPLACE_REC_B表 評価読替(先)科目
        stb.append(       "UNION ALL ");
        stb.append(       "SELECT  SEMESTER, SUBCLASSCD, ATTENDNO, SCHREGNO, ");
        stb.append(               "CASE WHEN SCHREGNO IS NOT NULL THEN NULL ELSE '0' END AS SCORE1, ");
        stb.append(               "CASE WHEN SCHREGNO IS NOT NULL THEN NULL ELSE '0' END AS SCORE2, ");
        stb.append(               "SCORE3, '' AS SEM3REC ");
        stb.append(       "FROM    REPLACE_REC_B ");
        stb.append(    "), ");

        //RECORD_DAT_B表より学期別に欠席数をカウントした表 => 生徒別合計・平均・席次の算出に使用する
        //  RECORD_DAT_A表において、欠席はSCORE?に'KK'を入れている
        stb.append("RECORD_DAT_KK AS(");
        stb.append(    "SELECT W1.SEMESTER, SCHREGNO, ");
        stb.append(           "SUM(CASE SCORE1 WHEN  'KK' THEN 1 ELSE 0 END) AS KK1, ");
        stb.append(           "SUM(CASE SCORE2 WHEN  'KK' THEN 1 ELSE 0 END) AS KK2, ");
        stb.append(           "SUM(CASE SCORE3 WHEN  'KK' THEN 1 ELSE 0 END) AS KK3 ");
        stb.append(    "FROM   RECORD_DAT_B W1 ");
        stb.append(    "WHERE  W1.SEMESTER IN ('1', '2', '3') ");
        stb.append(    "GROUP BY W1.SEMESTER, W1.SCHREGNO ");
        stb.append(    "), ");

        //生徒別平均点の表
        stb.append("RECORD_DAT_AVG AS(");
        stb.append(    "SELECT '1' AS SEMESTER, W1.SCHREGNO, ");
        stb.append(           "AVG(CASE WHEN 0 < KK1 THEN NULL ELSE FLOAT(INT(SCORE1)) END) AS AVG1, ");
        stb.append(           "AVG(CASE WHEN 0 < KK2 THEN NULL ELSE FLOAT(INT(SCORE2)) END) AS AVG2, ");
        stb.append(           "AVG(CASE WHEN 0 < KK3 THEN NULL ELSE FLOAT(INT(SCORE3)) END) AS AVG3 ");
        stb.append(    "FROM   RECORD_DAT_B W1, ");
        stb.append(           "RECORD_DAT_KK W2 ");
        stb.append(    "WHERE  W1.SEMESTER='1' AND W1.SEMESTER=W2.SEMESTER AND ");
        stb.append(           "W1.SCHREGNO = W2.SCHREGNO ");
        stb.append(    "GROUP BY W1.SCHREGNO ");
        stb.append(    "UNION ");
        stb.append(    "SELECT '2' AS SEMESTER, W1.SCHREGNO, ");
        stb.append(           "AVG(CASE WHEN 0 < KK1 THEN NULL ELSE FLOAT(INT(SCORE1)) END) AS AVG1, ");
        stb.append(           "AVG(CASE WHEN 0 < KK2 THEN NULL ELSE FLOAT(INT(SCORE2)) END) AS AVG2, ");
        stb.append(           "AVG(CASE WHEN 0 < KK3 THEN NULL ELSE FLOAT(INT(SCORE3)) END) AS AVG3 ");
        stb.append(    "FROM   RECORD_DAT_B W1, ");
        stb.append(           "RECORD_DAT_KK W2 ");
        stb.append(    "WHERE W1.SEMESTER='2' AND W1.SEMESTER=W2.SEMESTER AND ");
        stb.append(           "W1.SCHREGNO = W2.SCHREGNO ");
        stb.append(    "GROUP BY W1.SCHREGNO ");
        stb.append(    "UNION ");
        stb.append(    "SELECT '3' AS SEMESTER, W1.SCHREGNO, ");

        if (param._gakkiInt == SEMESTER3  ||  ( param._grade.equals(GRADE03) && param._gakkiInt == 2)) {
            stb.append(           "AVG(CASE WHEN 0 < KK1 THEN NULL ELSE FLOAT(INT(SCORE1)) END) AS AVG1, ");
            stb.append(           "AVG(CASE WHEN 0 < KK2 THEN NULL ELSE FLOAT(INT(SCORE2)) END) AS AVG2, ");
        } else {
            stb.append(           "case when w1.schregno is not null then null else 0 end AS AVG1, ");
            stb.append(           "case when w1.schregno is not null then null else 0 end AS AVG2, ");
        }
        stb.append(           "AVG(CASE WHEN 0 < KK3 THEN NULL ELSE FLOAT(INT(SCORE3)) END) AS AVG3 ");
        stb.append(    "FROM   RECORD_DAT_B W1, ");
        stb.append(           "RECORD_DAT_KK W2 ");
        stb.append(    "WHERE  W1.SEMESTER='3' AND ");
        stb.append(           "W1.SEMESTER=W2.SEMESTER AND ");
        stb.append(           "W1.SCHREGNO = W2.SCHREGNO ");
        stb.append(    "GROUP BY W1.SCHREGNO ");
        stb.append(    "), ");

        //席次の表
        stb.append("RECORD_DAT_RANK AS(");
        stb.append(    "SELECT '1' AS SEMESTER, SCHREGNO, ");
        stb.append(           "CASE WHEN AVG1 IS NULL THEN NULL ELSE RANK() OVER(ORDER BY CASE WHEN AVG1 IS NULL THEN -1 ELSE AVG1 END DESC) END AS RANK_1, ");
        stb.append(           "CASE WHEN AVG2 IS NULL THEN NULL ELSE RANK() OVER(ORDER BY CASE WHEN AVG2 IS NULL THEN -1 ELSE AVG2 END DESC) END AS RANK_2, ");
        stb.append(           "CASE WHEN AVG3 IS NULL THEN NULL ELSE RANK() OVER(ORDER BY CASE WHEN AVG3 IS NULL THEN -1 ELSE AVG3 END DESC) END AS RANK_3 ");
        stb.append(    "FROM   RECORD_DAT_AVG ");
        stb.append(    "WHERE  SEMESTER = '1' ");
        stb.append(    "UNION ");
        stb.append(    "SELECT '2' AS SEMESTER, SCHREGNO, ");
        stb.append(           "CASE WHEN AVG1 IS NULL THEN NULL ELSE RANK() OVER(ORDER BY CASE WHEN AVG1 IS NULL THEN -1 ELSE AVG1 END DESC) END AS RANK_1, ");
        stb.append(           "CASE WHEN AVG2 IS NULL THEN NULL ELSE RANK() OVER(ORDER BY CASE WHEN AVG2 IS NULL THEN -1 ELSE AVG2 END DESC) END AS RANK_2, ");
        stb.append(           "CASE WHEN AVG3 IS NULL THEN NULL ELSE RANK() OVER(ORDER BY CASE WHEN AVG3 IS NULL THEN -1 ELSE AVG3 END DESC) END AS RANK_3 ");
        stb.append(    "FROM   RECORD_DAT_AVG ");
        stb.append(    "WHERE  SEMESTER = '2' ");

        if (param._gakkiInt == SEMESTER3  ||  ( param._grade.equals(GRADE03) && param._gakkiInt == 2)) {
            stb.append(    "UNION ");
            stb.append(    "SELECT '3' AS SEMESTER, SCHREGNO, ");
            stb.append(           "CASE WHEN AVG1 IS NULL THEN NULL ELSE RANK() OVER(ORDER BY CASE WHEN AVG1 IS NULL THEN -1 ELSE AVG1 END DESC) END AS RANK_1, ");
            stb.append(           "CASE WHEN AVG2 IS NULL THEN NULL ELSE RANK() OVER(ORDER BY CASE WHEN AVG2 IS NULL THEN -1 ELSE AVG2 END DESC) END AS RANK_2, ");
            stb.append(           "CASE WHEN AVG3 IS NULL THEN NULL ELSE RANK() OVER(ORDER BY CASE WHEN AVG3 IS NULL THEN -1 ELSE AVG3 END DESC) END AS RANK_3 ");
            stb.append(    "FROM   RECORD_DAT_AVG ");
            stb.append(    "WHERE  SEMESTER = '3' ");
        }
        stb.append(    "), ");

        //生徒別合計の表
        stb.append("RECORD_DAT_SUM AS(");
        stb.append(    "SELECT '1' AS SEMESTER, W1.SCHREGNO, ");
        stb.append(           "SUM(CASE SCORE1 WHEN  '' THEN NULL WHEN 'KK' THEN NULL ELSE INT(SCORE1) END) AS SUM_1, ");
        stb.append(           "SUM(CASE SCORE2 WHEN  '' THEN NULL WHEN 'KK' THEN NULL ELSE INT(SCORE2) END) AS SUM_2, ");
        stb.append(           "SUM(CASE SCORE3 WHEN  '' THEN NULL WHEN 'KK' THEN NULL ELSE INT(SCORE3) END) AS SUM_3 ");
        stb.append(    "FROM   RECORD_DAT_B W1 ");
        stb.append(           "INNER JOIN RECORD_DAT_KK W2 ON W1.SEMESTER = W2.SEMESTER AND W1.SCHREGNO = W2.SCHREGNO ");
        stb.append(    "WHERE  W1.SEMESTER = '1' ");
        if (param._category_selected != null) {
            stb.append(   "AND W1.SCHREGNO IN" + param._category_selected + " ");
        }
        stb.append(    "GROUP BY W1.SCHREGNO ");
        stb.append(    "UNION ");
        stb.append(    "SELECT '2' AS SEMESTER, W1.SCHREGNO, ");
        stb.append(           "SUM(CASE SCORE1 WHEN  '' THEN NULL WHEN 'KK' THEN NULL ELSE INT(SCORE1) END) AS SUM_1, ");
        stb.append(           "SUM(CASE SCORE2 WHEN  '' THEN NULL WHEN 'KK' THEN NULL ELSE INT(SCORE2) END) AS SUM_2, ");
        stb.append(           "SUM(CASE SCORE3 WHEN  '' THEN NULL WHEN 'KK' THEN NULL ELSE INT(SCORE3) END) AS SUM_3 ");
        stb.append(    "FROM   RECORD_DAT_B W1 ");
        stb.append(           "INNER JOIN RECORD_DAT_KK W2 ON W1.SEMESTER = W2.SEMESTER AND W1.SCHREGNO = W2.SCHREGNO ");
        stb.append(    "WHERE  W1.SEMESTER = '2' ");
        if (param._category_selected != null) {
            stb.append(   "AND W1.SCHREGNO IN" + param._category_selected + " ");
        }
        stb.append(    "GROUP BY W1.SCHREGNO ");
        stb.append(    "UNION ");
        stb.append(    "SELECT '3' AS SEMESTER, W1.SCHREGNO, ");

        if (param._gakkiInt == SEMESTER3  ||  (param._grade.equals(GRADE03) && param._gakkiInt == 2)) {
            stb.append(           "SUM(CASE SCORE1 WHEN  '' THEN NULL WHEN 'KK' THEN NULL ELSE INT(SCORE1) END) AS SUM_1, ");
            stb.append(           "SUM(CASE SCORE2 WHEN  '' THEN NULL WHEN 'KK' THEN NULL ELSE INT(SCORE2) END) AS SUM_2, ");
        } else {
            stb.append(           "case when W1.schregno is not null then null else 0 end  AS SUM_1, ");
            stb.append(           "case when W1.schregno is not null then null else 0 end  AS SUM_2, ");
        }

        stb.append(           "SUM(CASE when SCORE3 = '' THEN NULL WHEN score3 = 'KK' THEN NULL ELSE INT(SCORE3) END) AS SUM_3 ");
        stb.append(    "FROM   RECORD_DAT_B W1 ");
        stb.append(           "INNER JOIN RECORD_DAT_KK W2 ON W1.SEMESTER = W2.SEMESTER AND W1.SCHREGNO = W2.SCHREGNO ");

//        stb.append(           "left join(");
//        stb.append(               "select  schregno, ");
//        stb.append(               "CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD ");
//        stb.append(               "from    attend_subclass_over_dat s1 ");
//        stb.append(               "where   s1.year = '" + param._year + "' and ");
//        stb.append(                       "s1.grade = '" + param._grade + "' ");
//        stb.append(               "group by schregno, ");
//        stb.append(               "CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ");
//        stb.append(               "union ");
//        stb.append(               "select  schregno, ");
//        stb.append(               "COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD AS SUBCLASSCD ");
//        stb.append(               "from    attend_subclass_over_dat s1,SUBCLASS_REPLACE_COMBINED_DAT s2 ");
//        stb.append(               "where   s1.year = '" + param._year + "' and ");
//        stb.append(                       "s1.grade = '" + param._grade + "' and ");
//        stb.append(                       "s1.year = s2.year and ");
//        stb.append(                       "s1.CLASSCD = s2.ATTEND_CLASSCD and ");
//        stb.append(                       "s1.SCHOOL_KIND = s2.ATTEND_SCHOOL_KIND and ");
//        stb.append(                       "s1.CURRICULUM_CD = s2.ATTEND_CURRICULUM_CD AND ");
//        stb.append(                       "s1.subclasscd = s2.attend_subclasscd ");
//        stb.append(               "group by schregno, ");
//        stb.append(               "COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD ");
//        stb.append(               ")w3 on w3.schregno = w1.schregno and w3.SUBCLASSCD = w1.SUBCLASSCD  ");
        stb.append(    "WHERE  W1.SEMESTER = '3' ");
        if (param._category_selected != null) {
            stb.append(   "AND W1.SCHREGNO IN" + param._category_selected + " ");
        }
        stb.append(    "GROUP BY W1.SCHREGNO ");
        stb.append(    "), ");

        //受験者数の表
        stb.append("SCHNUM AS(");
        stb.append(    "SELECT '1' AS SEMESTER, ");
        stb.append(           "SUM(CASE WHEN W1.AVG1 IS NULL THEN NULL ELSE 1 END) AS COUNT_1, ");
        stb.append(           "SUM(CASE WHEN W1.AVG2 IS NULL THEN NULL ELSE 1 END) AS COUNT_2, ");
        stb.append(           "SUM(CASE WHEN W1.AVG3 IS NULL THEN NULL ELSE 1 END) AS COUNT_3 ");
        stb.append(    "FROM   RECORD_DAT_AVG W1 ");
        stb.append(    "WHERE  W1.SEMESTER = '1' ");
        stb.append(    "UNION ");
        stb.append(    "SELECT '2' AS SEMESTER, ");
        stb.append(           "SUM(CASE WHEN W1.AVG1 IS NULL THEN NULL ELSE 1 END) AS COUNT_1, ");
        stb.append(           "SUM(CASE WHEN W1.AVG2 IS NULL THEN NULL ELSE 1 END) AS COUNT_2, ");
        stb.append(           "SUM(CASE WHEN W1.AVG3 IS NULL THEN NULL ELSE 1 END) AS COUNT_3 ");
        stb.append(    "FROM   RECORD_DAT_AVG W1 ");
        stb.append(    "WHERE  W1.SEMESTER = '2' ");

        //if (param._gakkiInt == SEMESTER3) {
        if (param._gakkiInt == SEMESTER3  ||  (param._grade.equals(GRADE03) && param._gakkiInt == 2)) {
            stb.append(    "UNION ");
            stb.append(    "SELECT '3' AS SEMESTER, ");
            stb.append(           "SUM(CASE WHEN W1.AVG1 IS NULL THEN NULL ELSE 1 END) AS COUNT_1, ");
            stb.append(           "SUM(CASE WHEN W1.AVG2 IS NULL THEN NULL ELSE 1 END) AS COUNT_2, ");
            stb.append(           "SUM(CASE WHEN W1.AVG3 IS NULL THEN NULL ELSE 1 END) AS COUNT_3 ");
            stb.append(    "FROM   RECORD_DAT_AVG W1 ");
            stb.append(    "WHERE  W1.SEMESTER = '3' ");
        }
        stb.append(    ") ");

        //RECORD_DAT_B表より作成した組平均の表
        //中間成績では当学期の組平均を中間素点から取る、その他は学年成績から取る。
        stb.append(",HRCLASSAVG AS(");
        stb.append(    "SELECT  '1' AS SEMESTER, ");
        if (param._gakkiInt == 1  &&  param.isChukan()) {
            stb.append(        "INT(ROUND(AVG(FLOAT(CASE WHEN SCORE1 IN('','KK') THEN NULL ELSE INT(SCORE1) END)),0)) AS HRCLASSAVG1 ");
        } else {
            stb.append(        "INT(ROUND(AVG(FLOAT(CASE WHEN SCORE3 IN('','KK') THEN NULL ELSE INT(SCORE3) END)),0)) AS HRCLASSAVG1 ");
        }
        stb.append(           ",0 AS SEM3REC_HRCLASSAVG1 ");
        stb.append(    "FROM    RECORD_DAT_B W1 ");
        stb.append(    "WHERE   W1.SEMESTER = '1' ");

        stb.append(    "UNION ");
        stb.append(    "SELECT  '2' AS SEMESTER, ");
        if (param._gakkiInt == 2  &&  param.isChukan()) {
            stb.append(        "INT(ROUND(AVG(FLOAT(CASE WHEN SCORE1 IN('','KK') THEN NULL ELSE INT(SCORE1) END)),0)) AS HRCLASSAVG1 ");
        } else {
            stb.append(        "INT(ROUND(AVG(FLOAT(CASE WHEN SCORE3 IN('','KK') THEN NULL ELSE INT(SCORE3) END)),0)) AS HRCLASSAVG1 ");
        }
        stb.append(           ",0 AS SEM3REC_HRCLASSAVG1 ");
        stb.append(    "FROM   RECORD_DAT_B W1 ");
        stb.append(    "WHERE  W1.SEMESTER = '2' ");

        if (param._gakkiInt == SEMESTER3  ||  (param._grade.equals(GRADE03) && param.isKimatsu() && param._gakkiInt == 2)) {
            //３学期のRECORD_DAT_B表は学年成績はSCORE2に入っている。RECORD_DAT_B表のSEM3RECはSEM3_TERM_REC(但し３年生は'')
            stb.append("UNION ");
            stb.append("SELECT  '3' AS SEMESTER, ");
            stb.append(        "INT(ROUND(AVG(FLOAT(CASE WHEN SCORE2 IN('','KK') THEN NULL ELSE INT(SCORE2) END)),0)) AS HRCLASSAVG1 ");
            stb.append(       ",INT(ROUND(AVG(FLOAT(CASE WHEN SEM3REC IN('','KK') THEN NULL ELSE INT(SEM3REC) END)),0)) AS SEM3REC_HRCLASSAVG1 ");
            stb.append("FROM    RECORD_DAT_B W1 ");
            stb.append("WHERE   W1.SEMESTER = '3' ");
        }
        stb.append(    ") ");

        //メイン表
        stb.append("SELECT  W5.SEMESTER, W5.SCHREGNO, ");
        stb.append(        "W1.SUM_1, ");
        stb.append(        "W1.SUM_2, ");
        stb.append(        "W1.SUM_3, ");
        stb.append(        "INT(ROUND(W4.AVG1,0)) AS AVG_1, ");
        stb.append(        "INT(ROUND(W4.AVG2,0)) AS AVG_2, ");
        stb.append(        "CASE WHEN W1.SEMESTER IN('1','2') THEN ");
        stb.append(        "          RTRIM(CHAR(INT(    ROUND(W4.AVG3,0)))) ");
        stb.append(             "ELSE RTRIM(CHAR(DECIMAL(ROUND(W4.AVG3*10,0)/10 ,2 ,1))) ");
        stb.append(        "END AS AVG_3, ");
        stb.append(        "W2.RANK_1, ");
        stb.append(        "W2.RANK_2, ");
        stb.append(        "W2.RANK_3, ");
        stb.append(        "W3.COUNT_1, ");
        stb.append(        "W3.COUNT_2, ");
        stb.append(        "W3.COUNT_3, ");
        stb.append(        "W6.HRCLASSAVG1 AS GR_RECORD_AVG, ");
        stb.append(        "W6.SEM3REC_HRCLASSAVG1 AS SEM3REC_GR_RECORD_AVG, ");
        stb.append(        "W5.ATTENDNO ");
        stb.append(        ",(select count(*) from attend_semes_over_dat s1 where s1.year = '" + param._year + "' and s1.grade = '" + param._grade + "' and s1.schregno = w5.schregno group by schregno) as attendover ");
        stb.append("FROM    SCHNO_B W5 ");
        stb.append(        "left join RECORD_DAT_SUM W1 on W1.SCHREGNO = W5.SCHREGNO AND W1.SEMESTER = W5.SEMESTER ");
        stb.append(        "left join RECORD_DAT_RANK W2 on W2.SCHREGNO = W1.SCHREGNO AND W2.SEMESTER = W1.SEMESTER ");
        stb.append(        "left join RECORD_DAT_AVG W4 on W4.SCHREGNO = W1.SCHREGNO AND W4.SEMESTER = W1.SEMESTER  ");
        stb.append(        "left join SCHNUM W3  on W3.SEMESTER = W1.SEMESTER ");
        stb.append(        "left join HRCLASSAVG W6  on W6.SEMESTER = W1.SEMESTER ");
        stb.append("ORDER BY W5.ATTENDNO, W1.SEMESTER ");
        return stb.toString();

    }//String prestatementTotal(String)


    /**
     *  PrepareStatement作成 成績集計欄用statement作成 SCHNO表の内容
     *                       成績明細欄用statement作成 SCHNO表の内容
     *  2005/10/27 Build
     */
    private static String prestatementTotalSchno(final Param param, final int s) {
        final StringBuffer stb = new StringBuffer();

        //異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append(    "SELECT T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.ATTENDNO, ");
        stb.append(           "T1.GRADE, T1.HR_CLASS, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
        stb.append(    "FROM   SCHREG_REGD_DAT T1, V_SEMESTER_GRADE_MST T2 ");
        stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
        stb.append(        "AND T1.SEMESTER = '" + s + "' ");
        stb.append(        "AND T1.YEAR = T2.YEAR ");
        stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
        stb.append(        "AND T1.GRADE = T2.GRADE ");
        stb.append(        "AND (T1.GRADE, T1.HR_CLASS) IN (SELECT GRADE, HR_CLASS FROM T_GRADE_HR_CLASS) ");
        //在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
        //             転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append(                           "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
        stb.append(                             "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");

        if (param.isKimatsu()  &&  param._gakkiInt == SEMESTER3) {
            //学年成績における異動情報チェック:留学(1)・休学(2)・停学(3)・編入(4)
            // 停学をチェックから除外
            // 留学開始日が３学期の場合(短期留学)は対象とする
            // 留学および休学が各学期終了日（学期終了日が異動基準日より大きい場合は異動基準日）にかからない場合は対象とする
            stb.append(    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                   "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(                       "AND ((S1.TRANSFERCD IN ('2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(                         "OR (S1.TRANSFERCD IN ('1') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE AND S1.TRANSFER_EDATE < '" + param._gakki3Sdate + "' ))) ");
        } else {
            //学期成績における異動情報チェック:留学(1)・休学(2)・停学(3)・編入(4)
            // 学年成績以外は留学生は出力しない
            stb.append(    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                   "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(                       "AND ((S1.TRANSFERCD IN ('2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(                         "OR (S1.TRANSFERCD IN ('1') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE))) ");
        }
        return stb.toString();
    }


    /**
     *  PrepareStatement作成 成績明細-->生徒別科目別
     *     2005/02/04 評価読替え科目の出力を追加
     *     2005/02/08 成績データの試験成績、学期成績、学年成績の取得仕様を変更
     *     2005/03/12 留学開始日が３学期の場合(短期留学)は対象とする
     *     2005/03/14 評定読替科目の仕様変更による
     *     2005/07/03 ３年生の３学期以外の総合学習評定抽出を追加
     */
    private static String sqlMeisai(final Param param) {
        final StringBuffer stb = new StringBuffer();

        //組平均はTYPE_GROUP_HR_DATから取得不可

        //異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH T_GRADE_HR_CLASS (GRADE, HR_CLASS) AS(");
        stb.append(" VALUES(?, ?) ");
        stb.append(") , SCHNO AS(");
        for (int i = 1; i <= param._gakkiInt; i++) {
            if (i > 1) {
                stb.append("UNION ");
            }
            stb.append(prestatementTotalSchno(param, i));
        }
        stb.append(")");

        stb.append(",SCHNO_B0 AS(");
        stb.append(    "SELECT SCHREGNO, YEAR, GRADE, HR_CLASS, MAX(ATTENDNO) AS ATTENDNO ");
        stb.append(    "FROM   SCHREG_REGD_DAT W2 ");
        stb.append(    "WHERE  YEAR = '" + param._year + "' AND ");
        stb.append(           "(GRADE, HR_CLASS) IN (SELECT GRADE, HR_CLASS FROM T_GRADE_HR_CLASS) ");
        if (param._category_selected != null) {
            stb.append(   "AND SCHREGNO IN" + param._category_selected);
        }
        stb.append(   " GROUP BY SCHREGNO, YEAR, GRADE, HR_CLASS ");
        stb.append(  " ) ");

        stb.append(",SCHNO_B AS(");
        stb.append(    "SELECT B0.SCHREGNO, B0.ATTENDNO, W2.SEMESTER ");
        stb.append(    "FROM   SCHNO_B0 B0 ");
        stb.append(    "INNER JOIN (SELECT DISTINCT YEAR, SEMESTER, GRADE, HR_CLASS FROM SCHREG_REGD_DAT) W2 ON B0.YEAR = W2.YEAR ");
        stb.append(    "   AND B0.GRADE = W2.GRADE ");
        stb.append(    "   AND B0.HR_CLASS = W2.HR_CLASS ");
        stb.append(    "WHERE  W2.YEAR = '" + param._year + "' ");
        stb.append(  " ) ");

        //KIN_RECORD_DATより組全員を抽出
        stb.append(",RECORD_DAT AS(");
        stb.append(    "SELECT * ");
        stb.append(    "FROM   KIN_RECORD_DAT W1 ");
        stb.append(    "WHERE  YEAR = '" + param._year + "' AND EXISTS(SELECT 'X' FROM SCHNO W3 WHERE W3.SCHREGNO = W1.SCHREGNO GROUP BY W3.SCHREGNO) ");
        stb.append(    ") ");

        //ATTEND_SUBCLASS_OVER_DATテーブルより欠課時数超過者を抽出
        //    ATTEND_SUBCLASS_OVER_DATは欠課時数超過者リストで作成される
        //    評価読替(先)科目も評価読替(元)科目から作成しておく
        stb.append(",ATTENDOVER AS(");
        stb.append(    "SELECT SCHREGNO, ");
        stb.append(    "CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD ");
        stb.append(    "FROM   ATTEND_SUBCLASS_OVER_DAT S1 ");
        stb.append(    "WHERE  S1.YEAR = '" + param._year + "' AND S1.GRADE = '" + param._grade + "' ");
        stb.append(    "GROUP BY SCHREGNO, CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ");
        stb.append(    "UNION ");
        stb.append(    "SELECT SCHREGNO, ");
        stb.append(    "COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD AS SUBCLASSCD ");
        stb.append(    "FROM   ATTEND_SUBCLASS_OVER_DAT S1 INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT S2 ON S1.YEAR = S2.YEAR AND S1.CLASSCD = S2.COMBINED_CLASSCD AND S1.SCHOOL_KIND = S2.COMBINED_SCHOOL_KIND AND S1.CURRICULUM_CD = S2.COMBINED_CURRICULUM_CD AND S1.SUBCLASSCD = S2.ATTEND_SUBCLASSCD ");
        stb.append(    "WHERE  S1.YEAR = '" + param._year + "' AND S1.GRADE = '" + param._grade + "' ");
        stb.append(    "GROUP BY SCHREGNO, COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD ");
        stb.append(    ") ");

        //該当学年の評価読替科目の表
        stb.append(",SUBCLASS_REPLACE_REC AS(");
        stb.append(    "SELECT ");
        stb.append(           "COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ");
        stb.append(           "ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
        stb.append(    "FROM   SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append(    "WHERE  YEAR='" + param._year + "' AND ");
        stb.append(           "REPLACECD='1' ");
        stb.append(     ") ");

        //REPLACE_REC表より評価(元)科目に欠席が１つ以上ある生徒別評価(先)科目別表
        //  REPLACE_REC表のSUBCLASSCDはCOMBINED_SUBCLASSCD
        //REPLACE_REC表の参照を無しとし、各学期(累積)の(試験)出欠情報を持つ
        stb.append(",REPLACE_REC_CNT AS(");
        stb.append(    "SELECT  SCHREGNO,SUBCLASSCD ");
        stb.append(           ",SUM(CASE WHEN GRADE_RECORD1 IS NOT NULL THEN 1 ELSE 0 END) AS KS1 ");
        stb.append(           ",SUM(CASE WHEN GRADE_RECORD2 IS NOT NULL THEN 1 ELSE 0 END) AS KS2 ");
        stb.append(           ",SUM(CASE WHEN GRADE_RECORD  IS NOT NULL THEN 1 ELSE 0 END) AS KS  ");
        stb.append(    "FROM ( ");
        stb.append(       "SELECT  SCHREGNO, ");
        stb.append(               "W2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
        stb.append(               "W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
        stb.append(              ",CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR (SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM1_REC_FLG,'0') = '0' THEN '1' ");
        stb.append(                    "ELSE NULL END AS GRADE_RECORD1 ");
        stb.append(              ",CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR (SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM1_REC_FLG,'0') = '0' THEN '1' ");
        stb.append(                    "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR (SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM2_REC_FLG,'0') = '0' THEN '1' ");
        stb.append(                    "ELSE NULL END AS GRADE_RECORD2 ");
        stb.append(              ",CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR (SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM1_REC_FLG,'0') = '0' THEN '1' ");
        stb.append(                    "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR (SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM2_REC_FLG,'0') = '0' THEN '1' ");
        stb.append(                    "WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND VALUE(SEM3_TERM_REC_FLG,'0') = '0' THEN '1' ");
        stb.append(                    "ELSE NULL END AS GRADE_RECORD ");
        stb.append(       "FROM   RECORD_DAT W1 ");
        stb.append(              "INNER JOIN SUBCLASS_REPLACE_REC W2 ON W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD = W2.ATTEND_SUBCLASSCD ");
        stb.append(    ")W1 ");
        stb.append(    "WHERE W1.GRADE_RECORD1 IS NOT NULL OR W1.GRADE_RECORD2 IS NOT NULL OR W1.GRADE_RECORD IS NOT NULL ");
        stb.append(    "GROUP BY SCHREGNO, SUBCLASSCD ");
        stb.append(    "HAVING 0 < COUNT(*) ");
        stb.append(    ") ");

        //RECORD_DAT表から評価読替先科目を抽出
        stb.append(",REPLACE_RECORD_DAT AS(");
        stb.append(    "SELECT  W1.* ");
        stb.append(    "FROM    RECORD_DAT W1 ");
        stb.append(    "WHERE   EXISTS(SELECT 'X' ");
        stb.append(                   "FROM    SUBCLASS_REPLACE_REC W2 ");
        stb.append(                   "WHERE W2.COMBINED_SUBCLASSCD = W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD ");
        stb.append(                   ") ");
        stb.append(    ") ");

        //REPLACE_RECORD_DAT表から評価読替先科目の学期成績・学年評定を算出
        stb.append(",REPLACE_RECORD_DAT_A AS(");
        stb.append(    "SELECT  '1' AS SEMESTER, W1.SCHREGNO, ");
        stb.append(    "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append(            "CASE WHEN W3.SCHREGNO IS NOT NULL AND 0 < W3.KS1 THEN '( )' ELSE RTRIM(CHAR(SEM1_REC)) END AS SCORE ");
        stb.append(           ",'' AS ASSESS ");
        stb.append(    "FROM    REPLACE_RECORD_DAT W1 ");
        stb.append(    "LEFT JOIN REPLACE_REC_CNT W3 ON W1.SCHREGNO = W3.SCHREGNO ");
        stb.append(         "AND W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD = W3.SUBCLASSCD ");
        stb.append(         "AND 0 < W3.KS1 ");

        stb.append(    "UNION ");
        stb.append(    "SELECT  '2' AS SEMESTER, W1.SCHREGNO, W1.SUBCLASSCD, ");
        stb.append(            "CASE WHEN W3.SCHREGNO IS NOT NULL AND 0 < W3.KS2 THEN '( )' ELSE RTRIM(CHAR(SCORE)) END AS SCORE ");
        stb.append(           ",'' AS ASSESS ");
        stb.append(    "FROM (");
        stb.append(        "SELECT  W1.SCHREGNO, W1.SUBCLASSCD, ");
        stb.append(                "INT(ROUND(AVG(FLOAT(SCORE))*10,0)/10) AS SCORE ");
        stb.append(               ",'' AS ASSESS ");
        stb.append(        "FROM (");
        stb.append(            "SELECT  W1.SCHREGNO, W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, SEM1_REC AS SCORE ");
        stb.append(            "FROM    REPLACE_RECORD_DAT W1 ");
        stb.append(            "UNION ");
        stb.append(            "SELECT  W1.SCHREGNO, W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, SEM2_REC AS SCORE ");
        stb.append(            "FROM    REPLACE_RECORD_DAT W1 ");
        stb.append(        ")W1 ");
        stb.append(        "GROUP BY W1.SCHREGNO, W1.SUBCLASSCD ");
        stb.append(    ")W1 ");
        stb.append(    "LEFT JOIN REPLACE_REC_CNT W3 ON W1.SCHREGNO = W3.SCHREGNO AND W1.SUBCLASSCD = W3.SUBCLASSCD AND 0 < W3.KS2 ");

        stb.append(    "UNION ");
        stb.append(    "SELECT  '3' AS SEMESTER, W1.SCHREGNO, ");
        stb.append(         "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append(            "CASE WHEN W3.SCHREGNO IS NOT NULL AND 0 < W3.KS THEN '( )' ELSE RTRIM(CHAR(GRADE_RECORD)) END AS SCORE ");
        stb.append(           ",CASE WHEN W3.SCHREGNO IS NOT NULL AND 0 < W3.KS THEN '( )' ");
        stb.append(                 "WHEN GRADE_RECORD IS NULL THEN NULL ");
        stb.append(                 "ELSE CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS ");
        stb.append(                                         "WHEN 'B' THEN B_PATTERN_ASSESS ");
        stb.append(                                         "WHEN 'C' THEN C_PATTERN_ASSESS ELSE NULL END END AS ASSESS ");
        stb.append(    "FROM    REPLACE_RECORD_DAT W1 ");
        stb.append(    "LEFT JOIN REPLACE_REC_CNT W3 ON W1.SCHREGNO = W3.SCHREGNO ");
        stb.append(         "AND W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD = W3.SUBCLASSCD ");
        stb.append(         "AND 0 < W3.KS ");
        stb.append(    ") ");

        //REPLACE_RECORD_DAT_A表から評価読替先科目の学期別組平均を算出
        stb.append(",REPLACE_HR_AVG AS(");
        stb.append(    "SELECT  SEMESTER, SUBCLASSCD, ");
        stb.append(            "INT(ROUND(AVG(FLOAT(INT(SCORE))),0))AS AVG_HR ");
        stb.append(    "FROM    REPLACE_RECORD_DAT_A ");
        stb.append(    "WHERE   SCORE IS NOT NULL AND SCORE NOT IN('( )') ");
        stb.append(    "GROUP BY SEMESTER, SUBCLASSCD ");
        stb.append(    ") ");

        //成績明細の表 RECORD_DAT表を学期別にレコード化
        //  評価読替(先)科目は除外  05/03/14
        //各フィールドには次の値を入れる
        //  １・２学期は SCORE1:中間素点  SCORE2:期末素点  SCORE3:学期成績
        //    ３  学期は SCORE1:期末素点  SCORE2:学年成績  SCORE3:学年評定
        stb.append(",RECORD_DAT_A AS(");
        //１学期のデータ => 中間試験成績・期末試験成績・学期成績
        stb.append("SELECT '1' AS SEMESTER, W1.SCHREGNO, ");
        stb.append(         "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append(       "CASE WHEN SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS') THEN '( )' ");
        stb.append(            "WHEN SEM1_INTER_REC IS NOT NULL THEN RTRIM(CHAR(SEM1_INTER_REC)) ");
        stb.append(            "ELSE '' END AS SCORE1, ");
        stb.append(       "CASE WHEN SEM1_TERM_REC IS NULL AND SEM1_TERM_REC_DI IN('KK','KS') THEN '( )' ");
        stb.append(            "WHEN SEM1_TERM_REC IS NOT NULL THEN RTRIM(CHAR(SEM1_TERM_REC)) ");
        stb.append(            "ELSE '' END AS SCORE2, ");
        stb.append(       "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
        stb.append(                  "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
        stb.append(                  "VALUE(SEM1_REC_FLG,'0') = '0' THEN '( )' ");
        stb.append(            "WHEN SEM1_REC IS NOT NULL THEN RTRIM(CHAR(SEM1_REC)) ");
        stb.append(            "ELSE '' END AS SCORE3 ");
        stb.append("FROM   RECORD_DAT W1 ");
        stb.append("WHERE  NOT EXISTS( SELECT 'X' FROM SUBCLASS_REPLACE_REC S2 ");
        stb.append(                   "WHERE W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD = S2.COMBINED_SUBCLASSCD ");
        stb.append(                   "GROUP BY S2.COMBINED_SUBCLASSCD) ");
        //２学期のデータ => 中間試験成績・期末試験成績・学期成績
        if (1 < param._gakkiInt) {
            stb.append("UNION SELECT '2' AS SEMESTER, W1.SCHREGNO, ");
            stb.append(         "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append(       "CASE WHEN SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS') THEN '( )' ");
            stb.append(            "WHEN SEM2_INTER_REC IS NOT NULL THEN RTRIM(CHAR(SEM2_INTER_REC)) ");
            stb.append(            "ELSE '' END AS SCORE1, ");
            stb.append(       "CASE WHEN SEM2_TERM_REC IS NULL AND SEM2_TERM_REC_DI IN('KK','KS') THEN '( )' ");
            stb.append(            "WHEN SEM2_TERM_REC IS NOT NULL THEN RTRIM(CHAR(SEM2_TERM_REC)) ");
            stb.append(            "ELSE '' END AS SCORE2, ");
            stb.append(       "CASE WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
            stb.append(                  "(SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
            stb.append(                  "VALUE(SEM2_REC_FLG,'0') = '0' THEN '( )' ");
            stb.append(            "WHEN SEM2_REC IS NOT NULL THEN RTRIM(CHAR(SEM2_REC)) ");
            stb.append(            "ELSE '' END AS SCORE3 ");
            stb.append("FROM   RECORD_DAT W1 ");
            stb.append("WHERE  NOT EXISTS( SELECT 'X' FROM SUBCLASS_REPLACE_REC S2 ");
            stb.append(                   "WHERE W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD = S2.COMBINED_SUBCLASSCD ");
            stb.append(                   "GROUP BY S2.COMBINED_SUBCLASSCD) ");
        }
        //３学期のデータ     ３年生 => なし　　・学年成績・学年評定
        //               １・２年生 => 期末試験・学年成績・学年評定
        //if (2 < param._gakkiInt) {
        if (2 < param._gakkiInt  ||  param._grade.equals(GRADE03)) {
            stb.append("UNION SELECT '3' AS SEMESTER, W1.SCHREGNO, ");
            stb.append(         "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, ");
            if (param._grade.equals(GRADE03)) {
                stb.append(   "'' AS SCORE1, ");
            } else {
                stb.append(   "CASE WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND VALUE(SEM3_TERM_REC_FLG,'0') = '0' THEN '( )' ");
                stb.append(        "WHEN SEM3_TERM_REC IS NOT NULL THEN RTRIM(CHAR(SEM3_TERM_REC)) ");
                stb.append(        "ELSE '' END AS SCORE1, ");
            }

            stb.append(   "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR (SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM1_REC_FLG,'0') = '0' THEN '( )' ");
            stb.append(        "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR (SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM2_REC_FLG,'0') = '0' THEN '( )' ");
            if (!param._grade.equals(GRADE03)) {
                stb.append(        "WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND ");
                stb.append(              "VALUE(SEM3_TERM_REC_FLG,'0') = '0' THEN '( )' ");
            }
            stb.append(        "WHEN GRADE_RECORD IS NOT NULL THEN RTRIM(CHAR(GRADE_RECORD)) ");
            stb.append(        "ELSE '' END AS SCORE2, ");

            stb.append(   "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR (SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM1_REC_FLG,'0') = '0' THEN '( )' ");
            stb.append(        "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR (SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM2_REC_FLG,'0') = '0' THEN '( )' ");
            if (!param._grade.equals(GRADE03)) {
                stb.append(        "WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND VALUE(SEM3_TERM_REC_FLG,'0') = '0' THEN '( )' ");
            }
            stb.append(        "WHEN GRADE_RECORD IS NOT NULL THEN ");
            stb.append(                 "CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS ");
            stb.append(                                    "WHEN 'B' THEN B_PATTERN_ASSESS ");
            stb.append(                                    "WHEN 'C' THEN C_PATTERN_ASSESS ELSE '' END ");
            stb.append(        "ELSE '' END AS SCORE3 ");
            stb.append("FROM   RECORD_DAT W1 ");
        }
        stb.append(     ") ");


        // ======== メイン表 ========

        //１学期のデータ  ==> SCORE1:中間 SCORE2:期末 SCORE3:学期
        //                ==> AVG_HR:１学期の中間成績印刷では中間の組平均、以外は１学期成績の組平均
        stb.append("SELECT  '1' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, ");
        stb.append(         "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append(        "W9.AVG_HR, ");
        stb.append(        "CASE WHEN SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS') THEN '( )' ");
        stb.append(             "WHEN SEM1_INTER_REC IS NOT NULL THEN RTRIM(CHAR(SEM1_INTER_REC)) ");
        stb.append(             "ELSE '' END AS SCORE1, ");
        stb.append(        "CASE WHEN SEM1_TERM_REC IS NULL AND SEM1_TERM_REC_DI IN('KK','KS') THEN '( )' ");
        stb.append(             "WHEN SEM1_TERM_REC IS NOT NULL THEN RTRIM(CHAR(SEM1_TERM_REC)) ");
        stb.append(             "ELSE '' END AS SCORE2, ");
        stb.append(        "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
        stb.append(                   "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
        stb.append(                  "VALUE(SEM1_REC_FLG,'0') = '0' THEN '( )' ");
        stb.append(             "WHEN SEM1_REC IS NOT NULL THEN RTRIM(CHAR(SEM1_REC)) ");
        stb.append(             "ELSE '' END AS SCORE3, ");
        stb.append(        "0  AS GRADE_RECORD_AVG_HR, ");
        stb.append(        "'' AS ELECTDIV ");
        stb.append(        ",0 AS ATTENDOVER ");
        stb.append("FROM    RECORD_DAT W1 ");
        stb.append(        "INNER JOIN SCHNO_B W2 ON W2.SEMESTER = '1' AND W1.SCHREGNO = W2.SCHREGNO ");
        //科目別組平均点の表 param._testkindcd=="01"は中間成績
        stb.append(        "LEFT JOIN(");
        stb.append(           "SELECT  SUBCLASSCD,");
        if (param._gakkiInt == 1 && param.isChukan()) {
            stb.append(               "INT( ROUND( AVG( FLOAT( INT( SCORE1 ) ) ), 0 ) )AS AVG_HR ");
        } else {
            stb.append(               "INT( ROUND( AVG( FLOAT( INT( SCORE3 ) ) ), 0 ) )AS AVG_HR ");
        }
        stb.append(           "FROM    RECORD_DAT_A W1 ");
        stb.append(                   "INNER  JOIN SCHNO W2 ON W2.SCHREGNO = W1.SCHREGNO ");
        if (param._gakkiInt == 1 && param.isChukan()) {
            stb.append(       "WHERE   SCORE1 IS NOT NULL AND SCORE1 NOT IN('KK','KS','( )','') ");
        } else {
            stb.append(       "WHERE   SCORE3 IS NOT NULL AND SCORE3 NOT IN('KK','KS','( )','') ");
        }
        stb.append(               "AND W1.SEMESTER = '1' AND W1.SEMESTER = W2.SEMESTER ");
        stb.append(           "GROUP BY SUBCLASSCD");
        stb.append(        ")W9 ON ");
        stb.append(         "W9.SUBCLASSCD = W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD ");
        //評価読替科目を除外
        stb.append("WHERE  NOT EXISTS( SELECT 'X' FROM SUBCLASS_REPLACE_REC S2 ");
        stb.append(                   "WHERE ");
        stb.append(                        "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD = S2.COMBINED_SUBCLASSCD ");
        stb.append(                   "GROUP BY S2.COMBINED_SUBCLASSCD) ");
        if (param._category_selected != null) {
            stb.append("AND W1.SCHREGNO IN" + param._category_selected + " ");
        }

        //評定読替科目の成績の表（１学期）
        if (param._grade.equals(GRADE03)  &&  param.isKimatsu()  &&  1 == param._gakkiInt) {
            stb.append("UNION ");
            stb.append("SELECT  W1.SEMESTER, W2.ATTENDNO, W1.SCHREGNO, W1.SUBCLASSCD, ");
            stb.append(        "W3.AVG_HR, ");
            stb.append(        "'' AS SCORE1, ");
            stb.append(        "'' AS SCORE2, ");
            stb.append(        "SCORE AS SCORE3, ");
            stb.append(        "0  AS GRADE_RECORD_AVG_HR, ");
            stb.append(        "'' AS ELECTDIV ");
            stb.append(       ",0 AS ATTENDOVER ");
            stb.append("FROM    REPLACE_RECORD_DAT_A W1 ");
            stb.append("INNER JOIN SCHNO_B W2 ON W2.SEMESTER = '" + param._gakki + "' AND W2.SCHREGNO = W1.SCHREGNO ");
            stb.append("LEFT JOIN REPLACE_HR_AVG W3 ON W3.SUBCLASSCD = W1.SUBCLASSCD AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("WHERE   W1.SEMESTER = '1' ");
        }

        //２学期のデータ  ==> SCORE1:中間 SCORE2:期末 SCORE3:学期
        //                ==> AVG_HR:２学期の中間成績印刷では中間の組平均、以外は２学期成績の組平均
        if (1 < param._gakkiInt) {
            stb.append("UNION SELECT '2' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, ");
            stb.append(         "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append(        "W9.AVG_HR, ");
            stb.append(        "CASE WHEN SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS') THEN '( )' ");
            stb.append(             "WHEN SEM2_INTER_REC IS NOT NULL THEN RTRIM(CHAR(SEM2_INTER_REC)) ");
            stb.append(             "ELSE '' END AS SCORE1, ");
            stb.append(        "CASE WHEN SEM2_TERM_REC IS NULL AND SEM2_TERM_REC_DI IN('KK','KS') THEN '( )' ");
            stb.append(             "WHEN SEM2_TERM_REC IS NOT NULL THEN RTRIM(CHAR(SEM2_TERM_REC)) ");
            stb.append(             "ELSE '' END AS SCORE2, ");
            stb.append(        "CASE WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
            stb.append(                   "(SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
            stb.append(                   "VALUE(SEM2_REC_FLG,'0') = '0' THEN '( )' ");
            stb.append(             "WHEN SEM2_REC IS NOT NULL THEN RTRIM(CHAR(SEM2_REC)) ");
            stb.append(             "ELSE '' END AS SCORE3, ");
            stb.append(        "0  AS GRADE_RECORD_AVG_HR, ");
            stb.append(        "'' AS ELECTDIV ");
            stb.append(       ",0 AS ATTENDOVER ");
            stb.append("FROM   RECORD_DAT W1 ");
            stb.append(       "INNER JOIN SCHNO_B W2 ON W2.SEMESTER = '2' AND W1.SCHREGNO = W2.SCHREGNO ");
            //科目別組平均点の表 param._testkindcd=="01"は中間成績
            stb.append(        "LEFT JOIN(");
            stb.append(           "SELECT  SUBCLASSCD,");
            if (param._gakkiInt == 2 && param.isChukan()) {
                stb.append(               "INT(ROUND(AVG(FLOAT(INT(SCORE1))),0))AS AVG_HR ");
            } else {
                stb.append(               "INT(ROUND(AVG(FLOAT(INT(SCORE3))),0)) AS AVG_HR ");
            }
            stb.append(           "FROM    RECORD_DAT_A W1 ");
            stb.append(                   "INNER  JOIN SCHNO W2 ON W2.SCHREGNO = W1.SCHREGNO ");
            if (param._gakkiInt == 2 && param.isChukan()) {
                stb.append(       "WHERE   SCORE1 IS NOT NULL AND SCORE1 NOT IN('KK','KS','( )','') ");
            } else {
                stb.append(       "WHERE   SCORE3 IS NOT NULL AND SCORE3 NOT IN('KK','KS','( )','') ");
            }
            stb.append(               "AND W1.SEMESTER = '2' AND W1.SEMESTER = W2.SEMESTER ");
            stb.append(           "GROUP BY SUBCLASSCD");
            stb.append(        ")W9 ON ");
            stb.append(         "W9.SUBCLASSCD = W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD ");
            //評価読替科目を除外
            stb.append("WHERE  NOT EXISTS( SELECT 'X' FROM SUBCLASS_REPLACE_REC S2 ");
            stb.append(                   "WHERE ");
            stb.append(                        "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD = S2.COMBINED_SUBCLASSCD ");
            stb.append(                   "GROUP BY S2.COMBINED_SUBCLASSCD) ");
            if (param._category_selected != null) {
                stb.append(   "AND W1.SCHREGNO IN" + param._category_selected + " ");
            }
        }

        //評定読替科目の成績の表（２学期） 05/11/05 Build
        //３学期のデータ  ==> SCORE1:３年生は''、以外は期末
        //                    SCORE2:学年成績
        //                    SCORE3:学年評定
        //                ==> AVG_HR:３学期成績の組平均
        //                ==> GRADE_RECORD_AVG_HR:学年成績の組平均
        //
        if (2 < param._gakkiInt  ||  param._grade.equals(GRADE03)) {
            stb.append("UNION ");
            stb.append("SELECT  '3' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, ");
            stb.append(         "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, ");
            if (param._grade.equals(GRADE03)) {
                //３年生の場合
                stb.append(    "0  AS AVG_HR, ");
                stb.append(    "'' AS SCORE1, ");
            } else {
                //１／２年生の場合
                stb.append(    "W9.AVG_HR, ");
                stb.append(    "CASE WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND VALUE(SEM3_TERM_REC_FLG,'0') = '0' THEN '( )' ");
                stb.append(         "WHEN SEM3_TERM_REC IS NOT NULL THEN RTRIM(CHAR(SEM3_TERM_REC)) ");
                stb.append(         "ELSE '' END AS SCORE1, ");
            }

            if (param._gakkiInt == 1 && param._grade.equals(GRADE03)) {
                stb.append("'' AS SCORE2, ");
            } else {
                stb.append(    "CASE WHEN SUBSTR(W1.SUBCLASSCD,1,2) = '90' THEN '' ");
                stb.append(         "WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR (SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM1_REC_FLG,'0') = '0' THEN '( )' ");
                stb.append(         "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR (SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM2_REC_FLG,'0') = '0' THEN '( )' ");
                if (!param._grade.equals(GRADE03)) {
                    //１／２年生の場合
                    stb.append(         "WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND VALUE(SEM3_TERM_REC_FLG,'0') = '0' THEN '( )' ");
                }
                stb.append(         "WHEN GRADE_RECORD IS NOT NULL THEN RTRIM(CHAR(GRADE_RECORD)) ");
                stb.append(         "ELSE '' END AS SCORE2, ");
            }

            stb.append(   "CASE ");
            stb.append(        "WHEN W4.ATTEND_SUBCLASSCD IS NOT NULL THEN NULL ");
            stb.append(        "WHEN SUBSTR(W1.SUBCLASSCD,1,2) = '90' THEN ");
            if (param.isNewSogoPrint()) {
                stb.append(             " CASE WHEN GRADE_RECORD IS NOT NULL THEN '合' END ");
            } else {
                stb.append(             "(SELECT  ASSESSMARK FROM RELATIVEASSESS_MST S1 ");
                stb.append(              "WHERE   S1.GRADE = '" + param._grade + "' AND ");
                stb.append(                      "S1.ASSESSCD = '3' AND ");
                stb.append(                      "S1.CLASSCD = W1.CLASSCD AND ");
                stb.append(                      "S1.SCHOOL_KIND = W1.SCHOOL_KIND AND ");
                stb.append(                      "S1.CURRICULUM_CD = W1.CURRICULUM_CD AND ");
                stb.append(                      "S1.SUBCLASSCD = W1.SUBCLASSCD AND ");
                stb.append(                      "ASSESSLOW <= GRADE_RECORD AND GRADE_RECORD <= ASSESSHIGH ) ");
            }
            stb.append(        "WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR (SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM1_REC_FLG,'0') = '0' THEN '( )' ");
            stb.append(        "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR (SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND VALUE(SEM2_REC_FLG,'0') = '0' THEN '( )' ");
            if (!param._grade.equals(GRADE03)) {
                //１／２年生の場合
                stb.append(        "WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND VALUE(SEM3_TERM_REC_FLG,'0') = '0' THEN '( )' ");
            }
            stb.append(        "WHEN GRADE_RECORD IS NOT NULL THEN ");
            stb.append(                 "CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS ");
            stb.append(                                    "WHEN 'B' THEN B_PATTERN_ASSESS ");
            stb.append(                                    "WHEN 'C' THEN C_PATTERN_ASSESS ELSE '' END ");
            stb.append(        "ELSE '' END AS SCORE3, ");
            if (param._gakkiInt == SEMESTER3  ||  ( param._grade.equals(GRADE03) && param._gakkiInt == 2)) {
                stb.append(   "W8.GRADE_RECORD_AVG_HR, ");
            } else {
                stb.append(   "case when w1.schregno is not null then null else 0 end as GRADE_RECORD_AVG_HR, ");
            }

            stb.append(       "(SELECT ELECTDIV FROM SUBCLASS_MST W3 ");
            stb.append(        "WHERE ");
            stb.append(            "W3.CLASSCD = W1.CLASSCD AND ");
            stb.append(            "W3.SCHOOL_KIND = W1.SCHOOL_KIND AND ");
            stb.append(            "W3.CURRICULUM_CD = W1.CURRICULUM_CD AND ");
            stb.append(            "W3.SUBCLASSCD = W1.SUBCLASSCD) AS ELECTDIV ");
            stb.append(       ",(SELECT COUNT(*) FROM ATTENDOVER S1 ");
            stb.append(        " WHERE S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(             "AND S1.SUBCLASSCD = W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD ");
            stb.append(        " GROUP BY SCHREGNO)AS ATTENDOVER ");
            stb.append("FROM    RECORD_DAT W1 ");

            stb.append(        "INNER JOIN SCHNO W2 ON W2.SEMESTER = '" + param._gakki + "' AND W1.SCHREGNO = W2.SCHREGNO ");

            //評価読替科目
            stb.append(        "LEFT JOIN(");
            stb.append(          "SELECT  ATTEND_SUBCLASSCD, COUNT(*) ");
            stb.append(          "FROM    SUBCLASS_REPLACE_REC ");
            stb.append(          "GROUP BY ATTEND_SUBCLASSCD ");
            stb.append(        ")W4 ON W4.ATTEND_SUBCLASSCD = W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD ");

            //科目別３学期期末成績および学年成績の組平均点
            stb.append(        "LEFT JOIN(");
            stb.append(           "SELECT  SUBCLASSCD,");
            stb.append(                   "INT(ROUND(AVG(FLOAT(INT(SCORE1))),0))AS AVG_HR ");
            stb.append(           "FROM    RECORD_DAT_A W1 ");
            stb.append(           "INNER   JOIN SCHNO W2 ON W2.SEMESTER = '" + param._gakki + "' AND W2.SCHREGNO = W1.SCHREGNO ");
            stb.append(           "WHERE   SCORE1 IS NOT NULL AND SCORE1 NOT IN('KK','KS','( )','') ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(               "AND SUBSTR(SUBCLASSCD,5,2) <> '90' ");
            } else {
                stb.append(               "AND SUBSTR(SUBCLASSCD,1,2) <> '90' ");
            }
            if (param._gakkiInt == SEMESTER3  ||  ( param._grade.equals(GRADE03) && param._gakkiInt == 2)) {
                stb.append(            "AND W1.SEMESTER = '3' ");
            } else {
                stb.append(            "AND W1.SEMESTER = '" + param._gakki + "' ");
            }
            stb.append(           "GROUP BY SUBCLASSCD ");
            stb.append(        ")W9 ON W9.SUBCLASSCD = W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD ");

            //科目別３学期期末成績および学年成績の組平均点
            stb.append(        "LEFT JOIN(");
            stb.append(           "SELECT  SUBCLASSCD,");
            stb.append(                   "INT(ROUND(AVG(FLOAT(INT(SCORE2))),0))AS GRADE_RECORD_AVG_HR ");
            stb.append(           "FROM    RECORD_DAT_A W1 ");
            stb.append(           "INNER   JOIN SCHNO W2 ON W2.SEMESTER = '" + param._gakki + "' AND W2.SCHREGNO = W1.SCHREGNO ");
            stb.append(           "WHERE   SCORE2 IS NOT NULL AND SCORE2 NOT IN('KK','KS','( )','') ");
            stb.append(               "AND SUBSTR(SUBCLASSCD,5,2) <> '90' ");  // 総合学習のクラス平均は出力しない
            if (param._gakkiInt == SEMESTER3  ||  ( param._grade.equals(GRADE03) && param._gakkiInt == 2)) {
                stb.append(            "AND W1.SEMESTER = '3' ");
            } else {
                stb.append(            "AND W1.SEMESTER = '" + param._gakki + "' ");
            }
            stb.append(           "GROUP BY SUBCLASSCD ");
            stb.append(        ")W8 ON W8.SUBCLASSCD = W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD ");
            stb.append("WHERE  NOT EXISTS( SELECT 'X' FROM SUBCLASS_REPLACE_REC S2 ");
            stb.append(                   "WHERE ");
            stb.append(                        "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD = S2.COMBINED_SUBCLASSCD ");
            stb.append(                   "GROUP BY S2.COMBINED_SUBCLASSCD) ");
            if (param._category_selected != null) {
                stb.append("AND W1.SCHREGNO IN" + param._category_selected + " ");
            }

            //評定読替科目の成績の表（３学期）
            stb.append("UNION ");
            stb.append("SELECT  W1.SEMESTER, W2.ATTENDNO, W1.SCHREGNO, W1.SUBCLASSCD, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE 0 END AS AVG_HR, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS SCORE1, ");
            if (param._gakkiInt == SEMESTER3  ||  ( param._grade.equals(GRADE03) && param._gakkiInt == 2)) {
                stb.append(    "SCORE AS SCORE2, ");
            } else {
                stb.append(    "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS SCORE2, ");
            }
            stb.append(        "ASSESS AS SCORE3, ");
            stb.append(        "W3.AVG_HR AS GRADE_RECORD_AVG_HR, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ELECTDIV ");
            stb.append(       ",(SELECT COUNT(*) FROM ATTENDOVER S1 WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.SUBCLASSCD = W1.SUBCLASSCD GROUP BY SCHREGNO)AS ATTENDOVER ");
            stb.append("FROM    REPLACE_RECORD_DAT_A W1 ");
            stb.append("INNER JOIN SCHNO_B W2 ON W2.SEMESTER = '" + param._gakki + "' AND W2.SCHREGNO = W1.SCHREGNO ");
            stb.append("LEFT JOIN REPLACE_HR_AVG W3 ON W3.SUBCLASSCD = W1.SUBCLASSCD AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("WHERE   W1.SEMESTER = '3' ");
        }



        //総合学習（３年生の３学期以外）
        //                ==> SCORE1:３年生は''、以外は期末
        //                    SCORE2:学年成績
        //                    SCORE3:学年評定
        //                ==> AVG_HR:３学期成績の組平均           
        //                ==> GRADE_RECORD_AVG_HR:学年成績の組平均
        //
        if (param._gakkiInt < SEMESTER3  &&  param._grade.equals(GRADE03)  &&  param.isKimatsu()) {
            stb.append("UNION ");
            stb.append("SELECT  '" + param._gakki + "' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, ");
            stb.append(               "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append(        "case when w1.schregno is not null then null else 0 end AS AVG_HR, ");
            stb.append(        "'' AS SCORE1, ");
            stb.append(        "'' AS SCORE2, ");
            if (param.isNewSogoPrint()) {
                stb.append(        " CASE WHEN GRADE_RECORD IS NOT NULL THEN '合' END ");
            } else {
                stb.append(        "(SELECT  ASSESSMARK FROM RELATIVEASSESS_MST S1 ");
                stb.append(         "WHERE   S1.GRADE = '" + param._grade + "' AND ");
                stb.append(                 "S1.ASSESSCD = '3' AND ");
                stb.append(                 "S1.CLASSCD = W1.CLASSCD AND ");
                stb.append(                 "S1.SCHOOL_KIND = W1.SCHOOL_KIND AND ");
                stb.append(                 "S1.CURRICULUM_CD = W1.CURRICULUM_CD AND ");
                stb.append(                 "S1.SUBCLASSCD = W1.SUBCLASSCD AND ");
                stb.append(                 "ASSESSLOW <= GRADE_RECORD AND GRADE_RECORD <= ASSESSHIGH ) ");
            }
            stb.append(        "AS SCORE3, ");
            stb.append(        "case when w1.schregno is not null then null else 0 end as GRADE_RECORD_AVG_HR, ");
            stb.append(       "(SELECT ELECTDIV FROM SUBCLASS_MST W3 ");
            stb.append(        "WHERE ");
            stb.append(            "W3.CLASSCD = W1.CLASSCD AND ");
            stb.append(            "W3.SCHOOL_KIND = W1.SCHOOL_KIND AND ");
            stb.append(            "W3.CURRICULUM_CD = W1.CURRICULUM_CD AND ");
            stb.append(            "W3.SUBCLASSCD = W1.SUBCLASSCD) AS ELECTDIV ");
            stb.append(       ",(SELECT COUNT(*) FROM ATTENDOVER S1 ");
            stb.append(        " WHERE S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(             "AND S1.SUBCLASSCD = W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD ");
            stb.append(        " GROUP BY SCHREGNO)AS ATTENDOVER ");
            stb.append("FROM    RECORD_DAT W1 ");
            stb.append(        "INNER JOIN SCHNO_B W2 ON W2.SEMESTER = '" + param._gakki + "' AND W1.SCHREGNO = W2.SCHREGNO ");
            stb.append("where   SUBSTR(W1.SUBCLASSCD,5,2) = '90' ");
        }
        
        if (param._gakkiInt < SEMESTER3 && param._d065Name1List.size() > 0) {
            stb.append("UNION ");
            stb.append("SELECT  '3' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, ");
            stb.append(               "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append(        "case when w1.schregno is not null then null else 0 end AS AVG_HR, ");
            stb.append(        "'' AS SCORE1, ");
            stb.append(        "RTRIM(CHAR(GRADE_RECORD)) AS SCORE2, ");
            stb.append(        "'' AS SCORE3, ");
            stb.append(        "case when w1.schregno is not null then null else 0 end as GRADE_RECORD_AVG_HR, ");
            stb.append(       "'0' AS ELECTDIV, ");
            stb.append(       "(SELECT COUNT(*) FROM ATTENDOVER S1 ");
            stb.append(        " WHERE S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(             "AND S1.SUBCLASSCD = W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD ");
            stb.append(        " GROUP BY SCHREGNO)AS ATTENDOVER ");
            stb.append("FROM    RECORD_DAT W1 ");
            stb.append(        "INNER JOIN SCHNO_B W2 ON W2.SEMESTER = '" + param._gakki + "' AND W1.SCHREGNO = W2.SCHREGNO ");
            stb.append("where   W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD IN ( ");
            String comma = "";
            for (int i = 0; i < param._d065Name1List.size(); i++) {
                final String subclasscd = (String) param._d065Name1List.get(i);
                stb.append(comma + " '" + subclasscd + "' ");
                comma = ", ";
            }
            stb.append(")");
        }
        stb.append("ORDER BY 2,1");
        return stb.toString();

    }//prestatementSubclass()の括り

    /**
     *
     *  PrepareStatement作成 生徒別科目履修科目（履修科目か否か）
     *    2004/12/15 評価読み替え科目を追加 => １年国語総合の単位出力のため
     *
     **/
    private static String prestatementBeStudy(final Param param) {
        final StringBuffer stb = new StringBuffer();

        //任意の生徒の履修科目の表
        stb.append("WITH TSUBCLASS AS(");
        stb.append(    "SELECT ");
        stb.append(    "    CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD ");
        stb.append(    "FROM  (SELECT  SCHREGNO,CHAIRCD,SEMESTER,APPDATE,APPENDDATE ");
        stb.append(           "FROM    CHAIR_STD_DAT S1 ");
        stb.append(           "WHERE   YEAR = '" + param._year + "' AND SCHREGNO = ? ) W1, ");
        stb.append(           "CHAIR_DAT W2 ");
        stb.append(    "WHERE  W2.YEAR = '" + param._year + "' AND ");
        stb.append(           "W1.SEMESTER = W2.SEMESTER AND ");
        stb.append(           "W1.CHAIRCD = W2.CHAIRCD AND ");
        stb.append(           "SUBSTR(W2.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_T + "' AND ");
        stb.append(           "EXISTS(SELECT  'X' FROM SCH_CHR_DAT S1 ");
        stb.append(                  "WHERE   S1.YEAR = '" + param._year + "' AND ");
        stb.append(                          "S1.SEMESTER = W2.SEMESTER AND ");
        stb.append(                          "S1.CHAIRCD = W2.CHAIRCD AND ");
        stb.append(                          "S1.EXECUTEDATE BETWEEN APPDATE AND APPENDDATE) ");
        stb.append(    "GROUP BY ");
        stb.append(    "    CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ");
        stb.append(    ") ");

        //任意の生徒の科目単位の表
        stb.append(",CREDIT AS(");
        stb.append(    "SELECT ");
        stb.append(    "    CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD, ");
        stb.append(     " CREDITS ");
        stb.append(       "FROM   CREDIT_MST W1 ");
        stb.append(       "WHERE  W1.YEAR = '" + param._year + "' AND ");
        stb.append(              "EXISTS(SELECT  'X' ");
        stb.append(                  "FROM    SCHREG_REGD_DAT W2 ");
        stb.append(                  "WHERE   W2.YEAR = '" + param._year + "' AND ");
        stb.append(                          "W2.SEMESTER = '" + param._gakki + "' AND ");
        stb.append(                          "W2.SCHREGNO = ? AND ");
        stb.append(                          "W2.GRADE = W1.GRADE AND ");
        stb.append(                          "W2.COURSECD = W1.COURSECD AND ");
        stb.append(                          "W2.MAJORCD = W1.MAJORCD AND ");
        stb.append(                          "W2.COURSECODE = W1.COURSECODE)) ");

        //合併元/先科目
        stb.append(",ATTEND_SUBCLASS AS(");
        stb.append(    "SELECT DISTINCT ");
        stb.append(    "    ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
        stb.append(       "FROM   SUBCLASS_REPLACE_COMBINED_DAT W1 ");
        stb.append(       "WHERE  W1.YEAR = '" + param._year + "' AND ");
        stb.append(           "REPLACECD='1' ");
        stb.append("),COMBINED_SUBCLASS AS(");
        stb.append(    "SELECT DISTINCT ");
        stb.append(    "    COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ");
        stb.append(       "FROM   SUBCLASS_REPLACE_COMBINED_DAT W1 ");
        stb.append(       "WHERE  W1.YEAR = '" + param._year + "' AND ");
        stb.append(           "REPLACECD='1' ");
        stb.append(") ");

        //メイン表
        stb.append("SELECT  W1.SUBCLASSCD, W5.CLASSNAME, W4.SUBCLASSNAME, W3.CREDITS, ");
        stb.append(        "CASE WHEN W6.ATTEND_SUBCLASSCD IS NOT NULL THEN '1' ELSE '0' END as IS_MOTOKAMOKU, ");
        stb.append(        "CASE WHEN W7.COMBINED_SUBCLASSCD IS NOT NULL THEN '1' ELSE '0' END as IS_SAKIKAMOKU ");
        stb.append("FROM    TSUBCLASS W1 ");
        stb.append(        "INNER JOIN SUBCLASS_MST W4 ON ");
        stb.append(          " W4.CLASSCD || W4.SCHOOL_KIND || W4.CURRICULUM_CD || W4.SUBCLASSCD = W1.SUBCLASSCD ");
        stb.append(        "INNER JOIN CLASS_MST W5 ON ");
        stb.append(          " W5.CLASSCD = SUBSTR(W1.SUBCLASSCD,5,2) ");
        stb.append(          " AND W5.SCHOOL_KIND = SUBSTR(W1.SUBCLASSCD,3,1) ");
        stb.append(        "LEFT JOIN CREDIT W3 ON W1.SUBCLASSCD = W3.SUBCLASSCD ");
        stb.append(        "LEFT JOIN ATTEND_SUBCLASS W6 ON W1.SUBCLASSCD = W6.ATTEND_SUBCLASSCD ");
        stb.append(        "LEFT JOIN COMBINED_SUBCLASS W7 ON W1.SUBCLASSCD = W7.COMBINED_SUBCLASSCD ");

        //メイン表（評価読替科目）
        stb.append("UNION ");
        stb.append("SELECT  DISTINCT ");
        stb.append(    "    W2.COMBINED_CLASSCD || W2.COMBINED_SCHOOL_KIND || W2.COMBINED_CURRICULUM_CD || W2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
        stb.append(        " W5.CLASSNAME, W4.SUBCLASSNAME, ");
        stb.append(        "CREDITS, ");
        stb.append(        "CASE WHEN W6.ATTEND_SUBCLASSCD IS NOT NULL THEN '1' ELSE '0' END as IS_MOTOKAMOKU, ");
        stb.append(        "CASE WHEN W7.COMBINED_SUBCLASSCD IS NOT NULL THEN '1' ELSE '0' END as IS_SAKIKAMOKU ");
        stb.append("FROM    TSUBCLASS W1 ");
        stb.append(        "INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT W2 ON W2.YEAR = '" + param._year + "' AND ");
        stb.append(                                        "W2.ATTEND_CLASSCD || W2.ATTEND_SCHOOL_KIND || W2.ATTEND_CURRICULUM_CD || W2.ATTEND_SUBCLASSCD = W1.SUBCLASSCD ");
        stb.append(        "INNER JOIN SUBCLASS_MST W4 ON W4.SUBCLASSCD = W2.COMBINED_SUBCLASSCD ");
        stb.append(          " AND W4.CLASSCD = W2.COMBINED_CLASSCD ");
        stb.append(          " AND W4.SCHOOL_KIND = W2.COMBINED_SCHOOL_KIND ");
        stb.append(          " AND W4.CURRICULUM_CD = W2.COMBINED_CURRICULUM_CD ");
        stb.append(        "INNER JOIN CLASS_MST W5 ON ");
        stb.append(          " W5.CLASSCD = SUBSTR(W2.COMBINED_SUBCLASSCD,1,2) ");
        stb.append(          " AND W5.SCHOOL_KIND = W2.COMBINED_SCHOOL_KIND ");
        stb.append("LEFT JOIN CREDIT W3 ON ");
        stb.append(          " W3.SUBCLASSCD = W2.COMBINED_CLASSCD || W2.COMBINED_SCHOOL_KIND || W2.COMBINED_CURRICULUM_CD || W2.COMBINED_SUBCLASSCD ");
        stb.append("LEFT JOIN ATTEND_SUBCLASS W6 ON ");
        stb.append("W2.COMBINED_CLASSCD || W2.COMBINED_SCHOOL_KIND || W2.COMBINED_CURRICULUM_CD || W2.COMBINED_SUBCLASSCD = W6.ATTEND_SUBCLASSCD ");
        stb.append("LEFT JOIN COMBINED_SUBCLASS W7 ON ");
        stb.append("W2.COMBINED_CLASSCD || W2.COMBINED_SCHOOL_KIND || W2.COMBINED_CURRICULUM_CD || W2.COMBINED_SUBCLASSCD = W7.COMBINED_SUBCLASSCD ");
        stb.append("ORDER BY SUBCLASSCD ");
        return stb.toString();

    }//prestatementBeStudy()の括り

    /*
     * 所見のテーブル
     * 2012年度の1学年、2013年度の1学年・2学年、2014年度以上の総合的な学習の時間の文章評価
     */
    private static String prestatementRecordTotalstudytimeDat(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, T1.TOTALSTUDYTIME, T1.TOTALSTUDYACT ");
        stb.append(" FROM ");
        stb.append("     RECORD_TOTALSTUDYTIME_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '9' ");
        stb.append("     AND T1.SCHREGNO = ? ");
        stb.append("     AND T1.SUBCLASSCD LIKE '90%' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD ");
        return stb.toString();
    }

    private static class Student {
        final String _schregno;
        final int _attendno;
        final String _useRealName;
        final String _realName;
        final String _name;
        final String _majorname;
        final String _height;
        final String _weight;
        final String _sitheight;
        final String _rBarevision;
        final String _rVision;
        final String _lBareVision;
        final String _lVision;
        final String _remainadulttooth;
        final String _entDateSemester;
        String _staffname;
        
        private boolean gohihantei;             //合否判定
        private boolean attendover;             //欠席日数超過または欠課時数超過

        private int schassesstotal;
        private int schassesscnt;
        
        final Map hmm = new HashMap();
        final List meisaiList = new ArrayList();
        final List goukeiList = new ArrayList();
        final Map _attendSemesMap = new HashMap();
        private String _totalstudyact;
        private String _totalstudytime;
        private List _subclassList = Collections.EMPTY_LIST;

        public Student(
                final String schregno,
                final int attendno,
                final String useRealName,
                final String realName,
                final String name,
                final String majorname,
                final String height,
                final String weight,
                final String sitheight,
                final String rBarevision,
                final String rVision,
                final String lBareVision,
                final String lVision,
                final String remainadulttooth,
                final String entDateSemester) {
            _schregno = schregno;
            _attendno = attendno;
            _useRealName = useRealName;
            _realName = realName;
            _name = name;
            _majorname = majorname;
            _height = height;
            _weight = weight;
            _sitheight = sitheight;
            _rBarevision = rBarevision;
            _rVision = rVision;
            _lBareVision = lBareVision;
            _lVision = lVision;
            _remainadulttooth = remainadulttooth;
            _entDateSemester = entDateSemester;
        }
        
        private static Student getStudent(final List list, final String schregno) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }
        
        private static Student createStudent(final ResultSet rs) throws SQLException {
            final String schregno = rs.getString("SCHREGNO");
            final int attendno = rs.getInt("ATTENDNO");
            final String useRealName = rs.getString("USE_REAL_NAME");
            final String realName = rs.getString("REAL_NAME");
            final String name = rs.getString("NAME");
            final String majorname = rs.getString("MAJORNAME");
            final String height = rs.getString("HEIGHT");
            final String weight = rs.getString("WEIGHT");
            final String sitheight = rs.getString("SITHEIGHT");
            final String rBarevision = rs.getString("R_BAREVISION");
            final String rVision = rs.getString("R_VISION");
            final String lBareVision = rs.getString("L_BAREVISION");
            final String lVision = rs.getString("L_VISION");
            final String remainadulttooth = rs.getString("REMAINADULTTOOTH");
            final String entDateSemester = rs.getString("ENT_DATE_SEMESTER");
            return new Student(schregno, attendno, useRealName, realName, name, majorname, height, weight, sitheight, rBarevision, rVision, lBareVision, lVision, remainadulttooth, entDateSemester);
        }
        
        private static List getStudentList(final DB2UDB db2, final Param param, final String gradeHrClass) {
        	
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            String staffname = null;
            try {
                returnval = getinfo.Staff_name(db2, param._year, param._gakki, gradeHrClass, "");
                staffname = returnval.val1;                                              //学級担任名
            } catch (Exception ex) {
                log.error("[KNJD171K]setHeadHrclass get staff error! ", ex);
            }

            final List studentList = new ArrayList();
            //RecordSet作成
            ResultSet rs0 = null;
            try {
                
                final String pskey = "ps0";
                if (null == param._psMap.get(pskey)) {
                    final String sql0 = prestatementRegd(param);
                    param._psMap.put(pskey, db2.prepareStatement(sql0));           //学籍・健康診断データ
                }
                final PreparedStatement ps0 = (PreparedStatement) param._psMap.get(pskey);

                ps0.setString(1, gradeHrClass.substring(0, 2));  //学年
                ps0.setString(2, gradeHrClass.substring(2));     //組
                rs0 = ps0.executeQuery();                        //学籍データのResultSet
                
                while (rs0.next()) {
                    final Student student = Student.createStudent(rs0);
                    student._staffname = staffname;
                    studentList.add(student);
                }
                log.info(" set student size = " + studentList.size());
            } catch (SQLException ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(rs0);
            }

            log.info(" setGoukei.");
            ResultSet rs1 = null;
            try {
                
                final String pskey = "ps1";
                if (null == param._psMap.get(pskey)) {
                    final String sql1 = sqlGoukei(param);
                    if (param._isOutputDebugQuery) {
                        log.info("goukei sql = " + sql1);
                    }
                    param._psMap.put(pskey, db2.prepareStatement(sql1));       //成績合計・平均・席次・受験者数データ
                }
                final PreparedStatement ps1 = (PreparedStatement) param._psMap.get(pskey);

                ps1.setString(1, gradeHrClass.substring(0, 2));  //学年
                ps1.setString(2, gradeHrClass.substring(2));     //組
                rs1 = ps1.executeQuery();                        //成績合データのResultSet
                
                while (rs1.next()) {
                    final String schregno = rs1.getString("SCHREGNO");
                    final Student student = Student.getStudent(studentList, schregno);
                    if (null == student) {
                        continue;
                    }
                    student.goukeiList.add(createMap(rs1));
                }
            } catch (SQLException ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(rs1);
            }

            log.info(" setMeisai.");
            ResultSet rs2 = null;
            try {
                final String pskey = "ps2";
                if (null == param._psMap.get(pskey)) {
                    final String sql2 = sqlMeisai(param);
                    if (param._isOutputDebugQuery) {
                        log.info("meisai sql = " + sql2);
                    }
                    param._psMap.put(pskey, db2.prepareStatement(sql2));       //成績明細データ
                }
                final PreparedStatement ps2 = (PreparedStatement) param._psMap.get(pskey);

                ps2.setString(1,  gradeHrClass.substring(0, 2)); //学年
                ps2.setString(2,  gradeHrClass.substring(2));    //組
                rs2 = ps2.executeQuery();                        //成績明細データのResultSet
                
                while (rs2.next()) {
                    final String schregno = rs2.getString("SCHREGNO");
                    final Student student = Student.getStudent(studentList, schregno);
                    if (null == student) {
                        continue;
                    }
                    student.meisaiList.add(createMap(rs2));
                }

            } catch (SQLException ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(rs2);
            }
            
            log.info(" setAttendSemes.");
            setAttendSemes(db2, param, studentList);
            if (param.isTotalStudyShokenForm()) {
                log.info(" setTotalstudyTimeDat.");
                setTotalstudyTimeDat(db2, param, studentList);
            }
            log.info(" setSubclass.");
            setSubclass(db2, param, studentList);
            log.info(" setSubclassAttend.");
            setSubclassAttend(db2, param, studentList);
            return studentList;
        }
    }

    private static class Subclass {
        final int _line;
        final String _subclasscd;
        final String _classname;
        final String _subclassname;
        final String _credits;
        final boolean _isMotokamoku;
        final boolean _isSakikamoku;
        final Map _semesterLesson;
        final Map _semesterAbsent2;
        public Subclass(final int line, final String subclasscd, final String classname, final String subclassname, final String credits, final boolean isMotokamoku, final boolean isSakikamoku) {
            _line = line;
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
            _credits = credits;
            _isMotokamoku = isMotokamoku;
            _isSakikamoku = isSakikamoku;
            _semesterLesson = new HashMap();
            _semesterAbsent2 = new HashMap();
        }
    }
    
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        printname = request.getParameter("PRINTNAME");                   //プリンタ名
        log.fatal("$Revision: 68782 $");
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    private static class Param {
        final String _year;
        final String _gakki;
        final int _gakkiInt;
        final String _gradeHrClass;
        final String _grade;
        String _category_selected;
        final String _testkindcd;
        final String _date;
        final String _nendo;
//        String _attendDateDate;
//        String _attendDateMonth;
        String _gakki3Sdate;
        final String _output;
        final String _useCurriculumcd;

        private KNJSchoolMst _knjSchoolMst;

        private final String[] hrclass;

        private final Map _psMap = new HashMap();
        private final List _d065Name1List;
        private final Map _d001Abbv1Map;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugQuery;
        
        final Map _attendParamMap;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");                         //年度
            _gakki = request.getParameter("GAKKI");                       //1-3:学期
            _gakkiInt = Integer.parseInt(_gakki);
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");              //学年・組
            _testkindcd = request.getParameter("TESTKINDCD");                  //中間:01,期末:99
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));    //異動基準日
            _output = request.getParameter("OUTPUT");                      //1:個人指定  2:クラス指定
            _useCurriculumcd = request.getParameter("useCurriculumcd");             //教育課程

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            if (Integer.parseInt(_output) == 1) {
                _category_selected = setGetSchregno(request.getParameterValues("category_selected"));  //学籍番号の編集
                _grade = request.getParameter("GRADE_HR_CLASS").substring(0, 2);           //学年
                hrclass = request.getParameterValues("GRADE_HR_CLASS");                         //学年・組
            } else {
                _grade = request.getParameter("GRADE_HR_CLASS");                             //学年
                hrclass = request.getParameterValues("category_selected");                      //学年・組
            }
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度"; //年度
            setHead(db2);            //見出し項目
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG");
            
            _attendParamMap.put("schregno", "?");
            
            _d065Name1List = getD065Name1List(db2);
            _d001Abbv1Map = getD001Abbv1Map(db2);
            final String[] outputDebug = StringUtils.split(getDbPrginfoProperties(db2, "outputDebug"));
            _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugQuery = ArrayUtils.contains(outputDebug, "query");
        }
        
        public boolean isChukan() {
        	return "01".equals(_testkindcd);
        }
        
        public boolean isKimatsu() {
        	return "99".equals(_testkindcd);
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            String value = null;
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD170K' AND NAME = '" + propName + "' ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    value = rs.getString("VALUE");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return value;
        }

        private boolean isNewSogoPrint() {
            final int newformStartyear = 2012;
            final int paramYearInt = Integer.parseInt(_year);
            return (paramYearInt == (newformStartyear + 0) && "01".equals(_grade)) ||
            (paramYearInt == (newformStartyear + 1) && ("01".equals(_grade) || "02".equals(_grade))) ||
            (paramYearInt >= (newformStartyear + 2));
        }

        private boolean isTotalStudyShokenForm() {
            return isNewSogoPrint() && SEMESTER3 == _gakkiInt;
        }

        private List getD065Name1List(final DB2UDB db2) {
            final List list = new ArrayList();
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D065' AND NAME1 IS NOT NULL ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(StringUtils.replace(rs.getString("NAME1"), "-", ""));
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private Map getD001Abbv1Map(final DB2UDB db2) {
            final Map list = new HashMap();
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAMECD2, ABBV1 FROM NAME_MST WHERE NAMECD1 = 'D001' AND ABBV1 IS NOT NULL ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    list.put(rs.getString("NAMECD2"), rs.getString("ABBV1"));
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        /**
         *
         *  SVF-FORM 見出し項目取得（１回だけ処理）
         *
         **/
        private void setHead(final DB2UDB db2) {

            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;

            //  ３学期の開始日取得
            try {
                returnval = getinfo.Semester(db2,_year,"3");
                _gakki3Sdate = returnval.val2;                                 //学期期間FROM
            } catch (Exception ex) {
                log.warn("term1 svf-out error!",ex);
            } finally {
                if (_gakki3Sdate == null) _gakki3Sdate = (Integer.parseInt(_year) + 1) + "-03-31";
            }

            getinfo = null;
            returnval = null;
        }

        /**
         *  対象生徒学籍番号編集(SQL用)
         */
        private String setGetSchregno(final String[] schno) {

            final StringBuffer stb = new StringBuffer();
            for (int ia=0 ; ia<schno.length ; ia++) {
                if (ia == 0) {
                    stb.append("('");
                } else {
                    stb.append("','");
                }
                stb.append(schno[ia]);
            }
            stb.append("')");
            return stb.toString();
        }
    }

}//クラスの括り
