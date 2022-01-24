/**
 *
 *    学校教育システム 賢者 [出欠管理] 出欠統計（月別）
 *
 *    2005/01/24 yamashiro
 *  2005/02/01 yamashiro  留学・休学期間は出席すべき日数に含めない
 *  2005/02/25 yamashiro  最初の校時の忌引は遅刻にカウントしない、最後の校時の忌引は早退にカウントしない、
 *  2005/03/04 yamashiro  一日遅刻・早退をカウントする際、遅刻・早退を出席としてカウントするように修正
 *  2005/03/09 yamashiro  転学、退学において異動日は在籍とみなす
 *  2005/05/01 yamadhiro  集計カウントフラグの条件を追加
 *  2005/05/11 yamashiro  05/01の変更を元に戻す（１日出欠において集計カウントフラグは非参照と仕様確定）
 *  2005/05/12 yamashiro  処理を考慮してＳＱＬを改善
 *                        出欠集計の月別集計範囲日付の仕様変更による修正（name_mst 'Z006'に範囲をもつ)　＝＞保留
 *                        中学用の処理を挿入（高校用と異なる箇所がある）　＝＞保留
 *                        休学者が休学者数および在籍者数に二重カウントされる不具合を修正
 *  2005/05/13 yamashiro  中学はクラスをそのまま表記およびコース計は出力しない
 *  2005/05/17 yamashiro  出欠集計の月別集計範囲日付の取得方法を変更 => KNJAttendTermを使用
 *  2005/10/08 yamashiro  編入（データ仕様の変更による）について修正、および学籍異動に転学を追加
 *                        忌引または出停の日は遅刻および早退をカウントしない
 *                        他    <change specification of 05/09/28>
 *
 * @version $Id: 134333eba80eed4d05ab996c308cb1476f732804 $
 */

package servletpack.KNJC;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJAttendTerm;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;


public class KNJC160K {

    private static final Log log = LogFactory.getLog(KNJC160K.class);

    private Param param;

    private boolean nonedata;
    private int hrclassnum;
    private int count1;
    private int count2;

    /**
     *
     *  KNJD.classから最初に起動されるクラス
     *
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        log.debug(" $Revision: 69721 $ $Date: 2019-09-17 10:34:26 +0900 (火, 17 9 2019) $");

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        param = new Param(request);

        // print svf設定
        setSvfInit(response, svf);

        // ＤＢ接続
        db2 = setDb(request);
        if( openDb(db2) ){
            log.error("db open error");
            return;
        }
        param.loadSchoolMst(db2, param._year);

        // 印刷処理
        printSvf(db2, svf, param);

        // 終了処理
        closeSvf(svf);
        closeDb(db2);

    }   //doGetの括り


    /**
     *
     *  印刷処理
     *
     */
    private void printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        try {
            if (param._definecode == null) {
                param.setClasscode(db2, param._year);        //教科コード・学校区分・学期制等定数取得 05/05/12
            }
            setTargetMonth(db2, param);               //出欠集計日範囲設定
            setHead(db2, svf, param);                    //見出し出力のメソッド
            printSvfMain(db2, svf, param);            //出欠統計出力のメソッド
        } catch (Exception ex) {
            log.error("printSvf error!", ex);
        }
    }


    /**
     *  見出し項目等
     **/
    private void setHead(final DB2UDB db2, final Vrw32alp svf, final Param param) {

        svf.VrSetForm("KNJC160.frm", 4);                //共通フォーム
        svf.VrsOut("NENDO",   KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度");//年度
        svf.VrsOut("MONTH",   String.valueOf(Integer.parseInt(param._month)) + "月分");
        svf.VrsOut("TOTAL_PAGE",   "3");     //総頁数

        //    ＳＶＦ属性変更--->改ページ
        svf.VrAttribute("GRADE",   "FF=1");    //学年

        //    作成日(現在処理日)の取得
        try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = getinfo.Control(db2);
            svf.VrsOut("DATE",  KNJ_EditDate.h_format_JP(returnval.val3));        //作成日
        } catch (Exception ex) {
            log.error("error! " , ex);
        }

        //  クラス数取得
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(prestatementHrclass(param));
            rs = ps.executeQuery();
            if (rs.next()) {
                hrclassnum = rs.getInt(1);
            }
        } catch (Exception ex) {
            log.warn("hrclassnum-get error!", ex);
        } finally{
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

    }//setHead()の括り


    /**
     *  対象月を保存
     *      印刷指示画面から受取るパラメーターmonth'9-99-9'において、
     *        左端が学期、真中が月、右端が１は学期開始月２は学期終了月０は以外の月となっている
     *    2005/05/12 出欠集計の月別集計範囲日付の仕様変更による修正 => 中学は月末締め
     */
    private void setTargetMonth(final DB2UDB db2, final Param param) {

        KNJAttendTerm obj = new KNJAttendTerm();    //05/05/17
        boolean bjunior = (param._definecode.schoolmark.equals("KIN")) ? false : true;    //05/05/17
        try {
            //出欠集計の月別集計範囲日付を取得 05/05/17
            obj.setMonthTermDateK(db2, param._year, Integer.parseInt(param._month), bjunior);
            param._sdate = obj.sdate;
            param._edate = obj.edate;

        } catch (Exception ex) {
            log.error("error! ", ex);
        }
        log.info(" obj sdate = " + param._sdate);
        log.info(" obj edate = " + param._edate);
    }


//    /**
//     *  学期の開始日・終了日取得
//     */
//    private String setSemesterDate( DB2UDB db2, Param param ) {
//
//        String strdate = null;
//        try {
//            ps.setString( 1, param._year );        //年度
//            ps.setString( 2, param._semester );        //学期
//            //log.debug("ps="+ps.toString());
//            rs = ps.executeQuery();
//            if( rs.next() ){
//                if( param._monthFlg.equals("1") ) strdate = rs.getString("SDATE");
//                else                       strdate = rs.getString("EDATE");
//            }
//        } catch( Exception ex ) {
//            log.error("error! ", ex );
//        } finally {
//            DbUtils.closeQuietly(rs);
//            db2.commit();
//        }
//        return strdate;
//    }


    /**
     *  統計出力処理
     *  2005/05/13 Modify コース別統計は高校を対象し、中学は出力なし
     *
     */
    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        String hrclass[] = new String[ hrclassnum ];    //学級
        int num[][] = new int[9][ hrclassnum ];         //学級別集計データ
        int ttnum[] = new int[9];                       //総合計集計データ
        int tgnum[] = new int[9];                       //学年計集計データ
        int tcnum[] = new int[9];                       //コース計集計データ
        for (int i = 0; i < num.length; i++) {
            for (int j = 0; j < num[i].length; j++) {
                num[i][j] = 0;
            }
        }
        for( int i = 0; i < ttnum.length; i++ ) ttnum[i] = 0;
        for( int i = 0; i < tgnum.length; i++ ) tgnum[i] = 0;
        for( int i = 0; i < tcnum.length; i++ ) tcnum[i] = 0;

        //学級別集計データを保存
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = prestatementAttendNum(param);
            ps = db2.prepareStatement(sql);    //学級別生徒欠課
            //学年ごとに処理
            int k = 0;
            for (int i = 1; i <= 3; i++) {
                ps.setString( 1, "0" + String.valueOf(i));  //学年
                rs = ps.executeQuery();
                while (rs.next()) {
                    printSvfSumDetail(db2, svf, rs, hrclass, num, k);    //学級別明細行
                    k++;
                }
            }
        } catch (Exception ex) {
            log.error("error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        //総合計を算出
        for (int i = 0; i < ttnum.length; i++) {
            for (int j = 0; j < num[i].length; j++) {
                ttnum[i] += num[i][j];
            }
        }

        //出力処理
        try {
            String course = null;   //コースの保存
            String grade = null;    //学年の保存
            for (int k = 0; k < hrclassnum; k++) {
                if (hrclass[k] == null) {
                    break;
                }
                //学年のブレイク
                if (grade == null || !hrclass[k].substring(0, 2).equals(grade)) {
                    if (grade != null) {
                        printSvfTotal(svf, course, tcnum, tgnum, 1, 0);      //コース別合計行
                        printSvfTotal(svf, grade, tgnum, null,  2, 1);       //学年別合計行
                    }
                    grade = hrclass[k].substring(0, 2);
                    course = hrclass[k].substring(3, 5);
                    printSvfOutHead(db2, svf, Integer.parseInt(grade), num, hrclass);             //学年別見出し
                    count2 = 0;
                } else if (course == null || !hrclass[k].substring(3, 5).equals(course)) {
                    if (course != null) {
                        printSvfTotal(svf, course, tcnum, tgnum, 1, 0);     //コース別合計行
                    }
                    course = hrclass[k].substring(3, 5);
                }
                printSvfOutDetail(svf, hrclass[k].substring(3), num, tcnum, k);    //学級別明細行
            }
            if (grade != null) {
                printSvfTotal(svf, course, tcnum, tgnum, 1, 0);      //コース別合計行
                printSvfTotal(svf, grade, tgnum, null,  2, 3);       //学年別合計行
                printSvfTotal(svf, null, ttnum, null, 3, 0);         //総合計行
            }
        } catch (Exception ex) {
            log.error("error! ", ex);
        }

    }


    /**
     *  学級別出欠統計集計
     *      引数について
     *  2005/05/13 Modify 1J01 => 4J01表記は高校を対象し、中学はそのまま表記
     */
    private void printSvfSumDetail(final DB2UDB db2, final Vrw32alp svf, final ResultSet rs, final String[] hrclass, final int[][] num, final int k) {
        try {
            int grade = Integer.parseInt(rs.getString("GRADE"));
            if (!param._definecode.schoolmark.equals("KINJUNIOR")) {       //中学でない場合 05/05/13
                if (rs.getString("HR_CLASS").substring(0, 1).equals("J")) {
                    grade += 3;
                }
            }
            hrclass[k] = rs.getString("GRADE") + "-" + String.valueOf( grade ) + rs.getString("HR_CLASS");  //学級
        } catch (Exception ex) {
            log.error("error! " , ex );
        }

        try {
            num[0][k] = ( rs.getString("SCHNUM")  != null )? rs.getInt("SCHNUM")   : 0;  //在籍者数
            num[1][k] = ( rs.getString("KYUGAKU") != null )? rs.getInt("KYUGAKU")  : 0;  //休学者数
            num[2][k] = ( rs.getString("CHOUKETU")!= null )? rs.getInt("CHOUKETU") : 0;  //長欠者数
            num[3][k] = ( rs.getString("MORNING") != null )? rs.getInt("MORNING")  : 0;  //忌引日数
            num[4][k] = ( rs.getString("ABSENT")  != null )? rs.getInt("ABSENT")   : 0;  //欠席日数
            num[5][k] = ( rs.getString("LATE")    != null )? rs.getInt("LATE")     : 0;  //遅刻回数
            num[6][k] = ( rs.getString("EARLY")   != null )? rs.getInt("EARLY")    : 0;  //早退回数
            num[7][k] = ( rs.getString("MLESSON") != null )? rs.getInt("MLESSON")  : 0;  //出席すべき日数
            num[8][k] = ( rs.getString("LESSON")  != null )? rs.getInt("LESSON")   : 0;  //授業日数
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
    }


    /**
     *  学年別出力処理
     */
    private void printSvfOutHead(final DB2UDB db2, final Vrw32alp svf, final int grade, final int num[][], final String[] hrclass) {
        int jnissu1 = 0;    //学年当りの最小授業日数
        int jnissu2 = 0;    //学年当りの最第授業日数
        //授業日数の取得
        try {
            for( int k = 0 ; k < hrclass.length ; k++ ){
                if(hrclass[k] != null  &&  grade < Integer.parseInt( hrclass[k].substring(0, 2) ) ) {
                    break;
                }
                if(hrclass[k] != null  &&  Integer.parseInt( hrclass[k].substring(0, 2) ) < grade ) {
                    continue;
                }
                if(jnissu1 == 0  ||  num[8][k] < jnissu1) {
                    jnissu1 = num[8][k];
                }
                if(jnissu2 < num[8][k]) {
                    jnissu2 = num[8][k];
                }
            }
        } catch (Exception ex) {
            log.error("error! ", ex);
        }

        svf.VrsOut( "GRADE",     String.valueOf( grade ) + "年");  //学年
        svf.VrsOut( "LESSON",     String.valueOf( jnissu2 ) + "日");  //最大授業日数を取る
    }


    /**
     *  学級別集計行出力
     */
    private void printSvfOutDetail(final Vrw32alp svf, final String str, final int[][] num, final int[] tcnum, final int i) {
        try {
            if (str.substring(2, 3).equals("0")) {
                svf.VrsOut( "HR_CLASS",     str.substring(0, 2) + " " + str.substring(3) );      //学級
            } else {
                svf.VrsOut( "HR_CLASS",     str);                                               //学級
            }

            svf.VrsOut( "REGISTER",     ( num[0][i] != 0 )? String.valueOf( num[0][i] ) : "" );  //在籍者数
            svf.VrsOut( "TEMP_ABSENCE", ( num[1][i] != 0 )? String.valueOf( num[1][i] ) : "" );  //休学者数
            svf.VrsOut( "LONG_ABSENCE", ( num[2][i] != 0 )? String.valueOf( num[2][i] ) : "" );  //長欠者数
            svf.VrsOut( "KIBIKI",       ( num[3][i] != 0 )? String.valueOf( num[3][i] ) : "" );  //忌引日数
            svf.VrsOut( "ABSENCE",      ( num[4][i] != 0 )? String.valueOf( num[4][i] ) : "" );  //欠席日数
            svf.VrsOut( "LATE",         ( num[5][i] != 0 )? String.valueOf( num[5][i] ) : "" );  //遅刻回数
            svf.VrsOut( "LEAVE",        ( num[6][i] != 0 )? String.valueOf( num[6][i] ) : "" );  //早退回数

            if (num[7][i] > 0) {
                svf.VrsOut( "PER_ATTEND", String.valueOf(
                                                    (float)Math.round(
                                                         ( (float)(num[7][i] - num[4][i])
                                                           / (float)num[7][i] ) * 10000
                                                         ) / 100 ) );                                  //出席率
            }

            svf.VrEndRecord();
            //if( ret == 0 ){
                nonedata = true;
                count1++;               //コース内件数カウント
                countNumCount2();       //１ページ出力件数カウント
            //}

        } catch(Exception ex) {
            log.error("error! ", ex);
        }


        try {
            for (int j = 0; j < tcnum.length; j++) {
                tcnum[j] += num[j][i];
            }
        } catch(Exception ex) {
            log.error("error! ", ex);
        }

    }


    /**
     *  合計集計行出力
     */
    private void printSvfTotal(final Vrw32alp svf, final String str, final int num[], final int tnum[],  final int i, final int d) {
        if (i == 1) {
            if (1 < count1) {
                printSvfOutTotal(svf, str, num, tnum, i);
            }
            svf.VrEndRecord();
            countNumCount2();       //１ページ出力件数カウント
            count1 = 0;      //コース内出力件数
        } else if (i == 2) {
            for (; count2 < 45 - d ; count2++) {
                svf.VrEndRecord();
            }
            printSvfOutTotal(svf, str, num, tnum, i);
            if (1 < d) {
                svf.VrEndRecord();
            }
            count2 = 0;
            countNumCount2();       //１ページ出力件数カウント
        } else {
            printSvfOutTotal(svf, str, num, tnum, i);
            count2 = 0;
            //countNumCount2();       //１ページ出力件数カウント
        }

        //集計用配列の初期化とコース合計を学年合計へ加算処理
        if( i < 3 ){
            try {
                for (int j = 0; j < num.length; j++) {
                    if (tnum != null) {
                        tnum[j] += num[j];
                    }
                    num[j] = 0;
                }
            } catch (Exception ex) {
                log.error("error! ", ex);
            }
        }

    }


    /**
     *  合計集計行出力
     *      引数について  int i  1:コース合計  2:学年合計  3:総合計
     *                    int num[]: 学級別集計値が入っている
     *      出席率 = ( 出席すべき日数合計 - 欠席数合計 ) / 出席すべき日数合計
     */
    private void printSvfOutTotal(final Vrw32alp svf, String str, int num[], int tnum[], int i)
    {
        if (param._definecode.schoolmark.equals("KINJUNIOR") && i == 1) {
            return;       //中学の場合 05/05/17
        }
        try {
            if (i == 1) {
                svf.VrsOut( "HR_CLASS",     str + " 計" );
            } else if (i == 2) {
                svf.VrsOut( "HR_CLASS",     String.valueOf(Integer.parseInt(str)) + "年 計" );
            } else {
                svf.VrsOut( "HR_CLASS",     "総合計" );
            }

            svf.VrsOut( "REGISTER",     ( num[0] != 0 )? String.valueOf( num[0] ) : "" );  //在籍者数
            svf.VrsOut( "TEMP_ABSENCE", ( num[1] != 0 )? String.valueOf( num[1] ) : "" );  //休学者数
            svf.VrsOut( "LONG_ABSENCE", ( num[2] != 0 )? String.valueOf( num[2] ) : "" );  //長欠者数
            svf.VrsOut( "KIBIKI",       ( num[3] != 0 )? String.valueOf( num[3] ) : "" );  //忌引日数
            svf.VrsOut( "ABSENCE",      ( num[4] != 0 )? String.valueOf( num[4] ) : "" );  //欠席日数
            svf.VrsOut( "LATE",         ( num[5] != 0 )? String.valueOf( num[5] ) : "" );  //遅刻回数
            svf.VrsOut( "LEAVE",        ( num[6] != 0 )? String.valueOf( num[6] ) : "" );  //早退回数

            if( num[7] > 0 )
                svf.VrsOut( "PER_ATTEND", String.valueOf(
                                                    (float)Math.round(
                                                         ( (float)(num[7] - num[4])
                                                           / (float)num[7] ) * 10000
                                                         ) / 100 ) );                            //出席率

            svf.VrEndRecord();
            countNumCount2();       //１ページ出力件数カウント

        } catch (Exception ex) {
            log.error("error! ", ex);
        }

    }


    /**
     *  ページ当り出力件数カウント
     *      １ページ４５行を超えた場合および改ページの場合は初期化を行う
     */
    private void countNumCount2() {
        if (count2 == 45) {
            count2 = 0;
        }
        count2++;
    }


    /**
     *  PrepareStatement作成  学年別(学年＆学級ごと)出欠統計取得
     *
     *  2005/02/15 転学・退学者も休学・留学者と同様に異動した日までは出席すべき日数等をカウントする。
     *               但し、在籍・休学・長欠等の人数にはカウントしない。
     *  2005/05/12 Modify 処理を考慮してＳＱＬを改善
     */
    private String prestatementAttendNum(Param param){

        final StringBuffer stb = new StringBuffer();
        try {
            //対象生徒 月開始日以前の退学・転学者は除外
            //         月終了日より後の編入者は除外
            stb.append("WITH SCHNO AS(");
            stb.append(        "SELECT  W1.YEAR,W1.SEMESTER,W1.SCHREGNO,W1.ATTENDNO, ");
            stb.append(                "W1.GRADE,W1.HR_CLASS ");
            stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
            stb.append(     "WHERE   W1.YEAR = '" + param._year + "' AND ");
            stb.append(             "W1.SEMESTER = '" + param._semester + "' AND ");
            stb.append(             "W1.GRADE = ? ");
            stb.append(     "), ");

            //対象生徒の時間割データ
            stb.append("SCHEDULE_SCHREG_R AS(");
            stb.append(     "SELECT  T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD, T4.DI_CD, T5.REP_DI_CD, (CASE WHEN T3.SCHREGNO IS NOT NULL THEN '1' ELSE '0' END) AS IS_OFFDAYS ");
            stb.append(     "FROM    SCH_CHR_DAT T1 ");
            stb.append(     "INNER JOIN CHAIR_STD_DAT T2 ON T1.YEAR = T2.YEAR ");
            stb.append(             "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(             "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(             "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(     "LEFT JOIN SCHREG_TRANSFER_DAT T3 ON T3.SCHREGNO = T2.SCHREGNO ");
            stb.append(             "AND T3.TRANSFERCD = '2' ");
            stb.append(             "AND T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE ");
            stb.append(     "LEFT JOIN ATTEND_DAT T4 ON T4.SCHREGNO = T2.SCHREGNO ");
            stb.append(             "AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append(             "AND T4.PERIODCD = T1.PERIODCD ");
            stb.append(             "AND T4.CHAIRCD = T1.CHAIRCD ");
            stb.append(     "LEFT JOIN ATTEND_DI_CD_DAT T5 ON T5.YEAR = '" + param._year + "' AND T5.DI_CD = T4.DI_CD ");
            stb.append(     "WHERE   T1.YEAR = '" + param._year + "' AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN '" + param._sdate + "' AND '" + param._edate + "' AND ");
            stb.append(             "EXISTS(SELECT 'X' FROM SCHNO T3 WHERE T3.SCHREGNO = T2.SCHREGNO GROUP BY T3.SCHREGNO) AND ");  //05/05/12Modify
                                // 05/02/15 Modify 転学・退学者において異動日以降（在籍しない日）を除外
                                // 05/10/08 Modify 転入・編入の除外条件を追加  <change specification of 05/09/28>
            stb.append(             "NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST T3 ");
            stb.append(                        "WHERE  T3.SCHREGNO = T2.SCHREGNO ");
            stb.append(                           "AND ((T3.GRD_DIV IN('2','3') AND T3.GRD_DATE < T1.EXECUTEDATE) ");  //05/03/09
            stb.append(                             "OR (T3.ENT_DIV IN('4','5') AND T3.ENT_DATE > T1.EXECUTEDATE)) ) ");  //<change specification of n-times>
            stb.append(     "GROUP BY T2.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD, T4.DI_CD, T5.REP_DI_CD, T3.SCHREGNO ");
            stb.append(     "), ");

            //対象生徒の時間割データ
            stb.append("SCHEDULE_SCHREG AS(");
            stb.append(     "SELECT  T1.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD, T1.DI_CD, T1.REP_DI_CD ");
            stb.append(     "FROM    SCHEDULE_SCHREG_R T1 ");
            stb.append(     "WHERE   T1.IS_OFFDAYS = '0' ");
            // 05/10/08 Modify 留学・休学の除外条件を追加  <change specification of 05/09/28>
            stb.append(         "AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT T3 ");
            stb.append(                        "WHERE  T3.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                               "T3.TRANSFERCD IN('1','2') AND ");
            stb.append(                               "T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE) "); //<change specification of n-times>
            stb.append(     "), ");

            //対象生徒の出欠データ
            stb.append("T_ATTEND_DAT AS(");
            stb.append(     "SELECT  T0.SCHREGNO, T0.ATTENDDATE, T0.PERIODCD, T0.DI_CD, T2.REP_DI_CD ");
            stb.append(     "FROM    ATTEND_DAT T0 ");
            stb.append("             INNER JOIN ATTEND_DI_CD_DAT T2 ON T2.YEAR = '" + param._year + "' AND T2.DI_CD = T0.DI_CD, ");
            stb.append(             "SCHEDULE_SCHREG T1 ");
            stb.append(     "WHERE   T0.YEAR = '" + param._year + "' AND ");
            stb.append(             "T0.ATTENDDATE BETWEEN '" + param._sdate + "' AND '" + param._edate + "' AND ");
            stb.append(             "T0.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(             "T0.ATTENDDATE = T1.EXECUTEDATE AND ");
            stb.append(             "T0.PERIODCD = T1.PERIODCD ");
            stb.append(     "), ");

            //対象生徒の出欠データ（忌引・出停した日）
            stb.append(" T_ATTEND_DAT_B AS( ");
            stb.append(" SELECT ");
            stb.append("    T0.SCHREGNO, ");
            stb.append("    T0.ATTENDDATE, ");
            stb.append("    MIN(T0.PERIODCD) AS FIRST_PERIOD, ");
            stb.append("    COUNT(DISTINCT T0.PERIODCD) AS PERIOD_CNT ");
            stb.append(" FROM ");
            stb.append("    T_ATTEND_DAT T0 ");
            stb.append(" WHERE ");
            stb.append("    REP_DI_CD IN('2','3','9','10' ");
            if ("true".equals(param._useVirus)) {
                stb.append("    , '19','20' ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append("    , '25','26' ");
            }
            stb.append("    ) ");
            stb.append(" GROUP BY ");
            stb.append("    T0.SCHREGNO, ");
            stb.append("    T0.ATTENDDATE ");
            stb.append(     "), ");

            //対象生徒の日単位の最小校時・最大校時・校時数
            stb.append("T_PERIOD_CNT AS(");
            stb.append(     "SELECT  T1.SCHREGNO, T1.EXECUTEDATE, ");
            stb.append(             "MIN(T1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(             "MAX(T1.PERIODCD) AS LAST_PERIOD, ");
            stb.append(             "COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ");
            stb.append(     "FROM    SCHEDULE_SCHREG T1 ");
            stb.append(     "GROUP BY T1.SCHREGNO, T1.EXECUTEDATE ");
            stb.append(     ") ");

            if ("1".equals(param._knjSchoolMst._syukessekiHanteiHou)) {
                //対象生徒の日単位のデータ（忌引・出停した日）
                stb.append(" , T_PERIOD_SUSPEND_MOURNING AS( ");
                stb.append(" SELECT ");
                stb.append("    T0.SCHREGNO, ");
                stb.append("    T0.EXECUTEDATE ");
                stb.append(" FROM ");
                stb.append("    T_PERIOD_CNT T0, ");
                stb.append("    T_ATTEND_DAT_B T1 ");
                stb.append(" WHERE ");
                stb.append("        T0.SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND T0.EXECUTEDATE = T1.ATTENDDATE ");
                stb.append("    AND T0.FIRST_PERIOD = T1.FIRST_PERIOD ");
                stb.append("    AND T0.PERIOD_CNT = T1.PERIOD_CNT ");
                stb.append(" ) ");
            }

            //メイン表
            stb.append("SELECT  T1.GRADE, T1.HR_CLASS, ");
            stb.append(        "COUNT(T1.SCHREGNO) AS SCHNUM, ");    // 05/02/15Modify
            stb.append(        "COUNT(T2.SCHREGNO)AS KYUGAKU, ");
            stb.append(        "SUM(CASE WHEN T1.SCHREGNO IS NULL THEN NULL ELSE CASE WHEN " + param._limit + " <= ABSENT THEN 1 ELSE NULL END END)AS CHOUKETU, ");  // 05/02/15Modify
            stb.append(        "MAX(LESSON) AS LESSON, ");
            stb.append(        "SUM(MLESSON) AS MLESSON, ");
            stb.append(        "SUM(MOURNING) AS MORNING, ");
            stb.append(        "SUM(ABSENT) AS ABSENT, ");
            stb.append(        "SUM(LATE) AS LATE, ");
            stb.append(        "SUM(EARLY) AS EARLY ");

            ////個人別出欠集計データ
            stb.append("FROM(");
            stb.append(   "SELECT  GRADE, HR_CLASS, TT7.SCHREGNO, ");    // 05/02/15Modify
            stb.append(           "VALUE(TT1.LESSON,0) ");
            if (param._knjSchoolMst != null && "1".equals(param._knjSchoolMst._semOffDays)) {
                stb.append(                " + VALUE(TT10.OFFDAYS_DATE, 0) ");
            }
            stb.append(           "AS LESSON, ");
            stb.append(           "VALUE(TT1.LESSON,0) - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0) ");
            if (param._knjSchoolMst != null && "1".equals(param._knjSchoolMst._semOffDays)) {
                stb.append(                " + VALUE(TT10.OFFDAYS_DATE, 0) ");
            }
            if ("true".equals(param._useVirus)) {
                stb.append(           " - VALUE(TT3_1.VIRUS,0) ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append(           " - VALUE(TT3_2.KOUDOME,0) ");
            }
            stb.append(           "AS MLESSON, ");
            stb.append(           "VALUE(TT3.SUSPEND,0) AS SUSPEND, ");
            stb.append(           "VALUE(TT4.MOURNING,0) AS MOURNING, ");
            stb.append(           "VALUE(TT5.SICK,0) + VALUE(TT5.NOTICE,0) + VALUE(TT5.NONOTICE,0) ");
            if (param._knjSchoolMst != null && "1".equals(param._knjSchoolMst._semOffDays)) {
                stb.append(                " + VALUE(TT10.OFFDAYS_DATE, 0) ");
            }
            stb.append(           " AS ABSENT, ");
            stb.append(           "VALUE(TT1.LESSON,0) - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0) - VALUE(TT5.SICK + TT5.NOTICE + TT5.NONOTICE,0) ");
            if ("true".equals(param._useVirus)) {
                stb.append(           " - VALUE(TT3_1.VIRUS,0) ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append(           " - VALUE(TT3_2.KOUDOME,0) ");
            }
            stb.append(           " AS PRESENT, ");
            //stb.append(           "VALUE(TT6.LATE,0) AS LATE, ");
            //stb.append(           "VALUE(TT6.EARLY,0) AS EARLY ");
            stb.append(           "VALUE(TT6.LATE,0) + VALUE(TT6_2.LATE,0) + VALUE(TT8.LATE,0) AS LATE, ");      //05/03/04Modify
            stb.append(           "VALUE(TT6.EARLY,0) + VALUE(TT6_2.EARLY,0) + VALUE(TT9.EARLY,0) AS EARLY ");    //05/03/04Modify
            stb.append(   "FROM    SCHNO TT0 ");

            //異動者を除外した学籍の表 05/02/15
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  SCHREGNO ");
            stb.append(      "FROM    SCHNO W1 ");
                                // 05/10/08Modify 転入・編入の除外条件を追加  <change specification of 05/09/28>
            stb.append(      "WHERE   NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                         "WHERE  S1.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(                                "((S1.GRD_DIV IN ('2','3') AND S1.GRD_DATE < '" + param._edate + "') OR ");  //05/03/09Modify
            stb.append(                                 "(S1.ENT_DIV IN ('4','5') AND S1.ENT_DATE > '" + param._edate + "')) AND ");  //<change specification of n-times>
                                // 05/10/08Modify 留学・休学の除外条件を追加  <change specification of 05/09/28>
            stb.append(              "NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT T3 ");
            stb.append(                         "WHERE  T3.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(                                "T3.TRANSFERCD IN('1','2') AND ");
            stb.append(                                "'" + param._edate + "' BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE) ) "); //<change specification of n-times>
            stb.append(      ") TT7 ON TT0.SCHREGNO = TT7.SCHREGNO ");

            //個人別授業日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  SCHREGNO, COUNT(DISTINCT EXECUTEDATE) AS LESSON ");
            stb.append(      "FROM    T_PERIOD_CNT ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT1 ON TT0.SCHREGNO = TT1.SCHREGNO ");
            //個人別出停日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS SUSPEND ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  REP_DI_CD IN ('2','9') ");
            if ("1".equals(param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("    AND (SCHREGNO, ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT3 ON TT0.SCHREGNO = TT3.SCHREGNO ");
            //個人別出停伝染病日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS VIRUS ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  REP_DI_CD IN ('25','26') ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT3_1 ON TT0.SCHREGNO = TT3_1.SCHREGNO ");
            //個人別出停交止日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS KOUDOME ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  REP_DI_CD IN ('19','20') ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT3_2 ON TT0.SCHREGNO = TT3_2.SCHREGNO ");
            //個人別忌引日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS MOURNING ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  REP_DI_CD IN ('3','10') ");
            if ("1".equals(param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("    AND (SCHREGNO, ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT4 ON TT0.SCHREGNO = TT4.SCHREGNO ");
            //個人別欠席日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  W0.SCHREGNO, ");
            stb.append(              "SUM(CASE W0.DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS SICK, ");
            stb.append(              "SUM(CASE W0.DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS NOTICE, ");
            stb.append(              "SUM(CASE W0.DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS NONOTICE ");
            stb.append(      "FROM    ATTEND_DAT W0 ");
            stb.append(         "INNER JOIN (");
            stb.append(         "SELECT  T0.SCHREGNO, T0.EXECUTEDATE, ");
            if ("1".equals(param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("         T2.FIRST_PERIOD ");
            } else {
                stb.append("         T0.FIRST_PERIOD ");
            }
            stb.append(         "FROM    T_PERIOD_CNT T0 ");
            stb.append(            "INNER JOIN (");
            stb.append(            "SELECT  W1.SCHREGNO, W1.EXECUTEDATE, ");
            stb.append(                    "MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(                    "COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(            "FROM    SCHEDULE_SCHREG W1 ");
            stb.append("              WHERE ");
            stb.append("                  W1.REP_DI_CD IN ('1', '4','5','6','11','12','13' ");
            if ("1".equals(param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("                          ,'2','9','3','10' ");
                if ("true".equals(param._useVirus)) {
                    stb.append("    , '19','20' ");
                }
                if ("true".equals(param._useKoudome)) {
                    stb.append("    , '25','26' ");
                }
            }
            stb.append("                              ) ");
            stb.append(            "GROUP BY W1.SCHREGNO, W1.EXECUTEDATE ");
            stb.append(            ") T1 ON T0.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                 "T0.EXECUTEDATE = T1.EXECUTEDATE AND ");
            stb.append(                 "T0.FIRST_PERIOD = T1.FIRST_PERIOD AND ");
            stb.append(                 "T0.PERIOD_CNT = T1.PERIOD_CNT ");
            if ("1".equals(param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("         INNER JOIN ( ");
                stb.append("              SELECT ");
                stb.append("                  W1.SCHREGNO, W1.ATTENDDATE, ");
                stb.append("                  MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
                stb.append("                  COUNT(W1.PERIODCD) AS PERIOD_CNT ");
                stb.append("              FROM ");
                stb.append("                  T_ATTEND_DAT W1 ");
                stb.append("              WHERE ");
                stb.append("                  W1.DI_CD IN ('4','5','6','11','12','13') ");
                stb.append("              GROUP BY ");
                stb.append("                  W1.SCHREGNO, ");
                stb.append("                  W1.ATTENDDATE ");
                stb.append("             ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.EXECUTEDATE ");
            }
            stb.append(         ") W1 ON W0.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(              "W0.ATTENDDATE = W1.EXECUTEDATE AND ");
            stb.append(              "W0.PERIODCD = W1.FIRST_PERIOD ");
            if ("1".equals(param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("    AND (W1.SCHREGNO, W1.EXECUTEDATE) NOT IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append(      "GROUP BY W0.SCHREGNO ");
            stb.append(      ")TT5 ON TT0.SCHREGNO = TT5.SCHREGNO ");

            //個人別遅刻・早退回数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS LATE, COUNT(T3.ATTENDDATE) AS EARLY ");
            stb.append(      "FROM    T_PERIOD_CNT T0 ");
            stb.append(      "INNER JOIN(");
            stb.append(         "SELECT  W1.SCHREGNO, W1.ATTENDDATE, COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(         "FROM    T_ATTEND_DAT W1 ");
            stb.append(           "WHERE   W1.REP_DI_CD NOT IN ('0','14','15','16','23','24') ");    //05/03/04Modify
            if (!"1".equals(param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append(               "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");  //<change specification of 05/09/28>
                stb.append(                              "WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.ATTENDDATE = W1.ATTENDDATE) ");
            }
            stb.append(         "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
            stb.append(         ")T1 ON T0.SCHREGNO = T1.SCHREGNO AND T0.EXECUTEDATE = T1.ATTENDDATE AND ");
            stb.append(                                              "T0.PERIOD_CNT != T1.PERIOD_CNT ");
            stb.append(      "LEFT OUTER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT ");
            stb.append(         "WHERE   REP_DI_CD IN ('4','5','6','11','12','13') ");         // 05/03/04Modify
            stb.append(         ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ");
            stb.append(                                              "T0.FIRST_PERIOD = T2.PERIODCD ");
            stb.append(      "LEFT OUTER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT ");
            stb.append(         "WHERE   REP_DI_CD IN ('4','5','6') ");                          // 05/03/03Modify
            stb.append(         ")T3 ON T0.SCHREGNO= T3.SCHREGNO AND T0.EXECUTEDATE = T3.ATTENDDATE AND ");
            stb.append(                                             "T0.LAST_PERIOD = T3.PERIODCD ");
            stb.append(      "GROUP BY T0.SCHREGNO ");
            stb.append(      ")TT6 ON TT0.SCHREGNO = TT6.SCHREGNO ");

            //個人別遅刻・早退回数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  T0.SCHREGNO ");
            stb.append(            ", COUNT(CASE WHEN T2.ATTENDDATE IS NOT NULL OR T4.ATTENDDATE IS NOT NULL THEN 1 END) AS LATE ");
            stb.append(            ", COUNT(CASE WHEN T3.ATTENDDATE IS NOT NULL OR T4.ATTENDDATE IS NOT NULL THEN 1 END) AS EARLY ");
            stb.append(      "FROM    T_PERIOD_CNT T0 ");
            stb.append(      "INNER JOIN(");
            stb.append(         "SELECT  W1.SCHREGNO, W1.ATTENDDATE, COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(         "FROM    T_ATTEND_DAT W1 ");
            stb.append(         "WHERE   W1.REP_DI_CD IN ('4','5','6','11','12','13','29','30','31','32') ");
            stb.append(         "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
            stb.append(         ")T1 ON T0.SCHREGNO = T1.SCHREGNO AND T0.EXECUTEDATE = T1.ATTENDDATE AND ");
            stb.append(                                              "T0.PERIOD_CNT = T1.PERIOD_CNT ");
            stb.append(      "LEFT JOIN T_ATTEND_DAT T2 ON T0.SCHREGNO = T2.SCHREGNO ");
            stb.append(                                  " AND T0.EXECUTEDATE  = T2.ATTENDDATE ");
            stb.append(                                  " AND T0.LAST_PERIOD = T2.PERIODCD ");
            stb.append(                                  " AND T2.REP_DI_CD = '29' ");
            stb.append(      "LEFT JOIN T_ATTEND_DAT T3 ON T0.SCHREGNO = T3.SCHREGNO ");
            stb.append(                                  " AND T0.EXECUTEDATE  = T3.ATTENDDATE ");
            stb.append(                                  " AND T0.FIRST_PERIOD = T3.PERIODCD ");
            stb.append(                                  " AND T3.REP_DI_CD = '30' ");
            stb.append(      "LEFT OUTER JOIN (");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ");
            stb.append(         "FROM    T_ATTEND_DAT ");
            stb.append(         "WHERE   REP_DI_CD IN ('31','32') ");
            stb.append(         "GROUP BY SCHREGNO ,ATTENDDATE ");
            stb.append(         ")T4 ON T0.SCHREGNO = T4.SCHREGNO AND T0.EXECUTEDATE  = T4.ATTENDDATE ");
            stb.append(      "GROUP BY T0.SCHREGNO ");
            stb.append(      ")TT6_2 ON TT0.SCHREGNO = TT6_2.SCHREGNO ");

            //個人別遅刻回数 05/03/04
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS LATE ");
            stb.append(      "FROM    T_PERIOD_CNT T0 ");
            stb.append(      "INNER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT W1 ");
            stb.append(         "WHERE   REP_DI_CD IN ('15','23','24') ");
            if (!"1".equals(param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append(             "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");  //<change specification of 05/09/28>
                stb.append(                            "WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.ATTENDDATE = W1.ATTENDDATE) ");
            }
            stb.append(         ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ");
            stb.append(                                              "T0.FIRST_PERIOD = T2.PERIODCD ");
            stb.append(      "GROUP BY T0.SCHREGNO ");
            stb.append(      ")TT8 ON TT0.SCHREGNO = TT8.SCHREGNO ");

            //個人別早退回数 05/03/04
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS EARLY ");
            stb.append(      "FROM    T_PERIOD_CNT T0 ");
            stb.append(      "INNER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT W1 ");
            stb.append(         "WHERE   REP_DI_CD IN ('16') ");
            if (!"1".equals(param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append(             "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");  //<change specification of 05/09/28>
                stb.append(                            "WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.ATTENDDATE = W1.ATTENDDATE) ");
            }
            stb.append(         ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ");
            stb.append(                                              "T0.LAST_PERIOD = T2.PERIODCD ");
            stb.append(      "GROUP BY T0.SCHREGNO ");
            stb.append(      ")TT9 ON TT0.SCHREGNO = TT9.SCHREGNO ");

            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  SCHREGNO, COUNT(DISTINCT EXECUTEDATE) AS OFFDAYS_DATE ");
            stb.append(      "FROM    SCHEDULE_SCHREG_R ");
            stb.append(      "WHERE   IS_OFFDAYS = '1' ");
            stb.append(      "GROUP BY  SCHREGNO ");
            stb.append(      ")TT10 ON TT0.SCHREGNO = TT10.SCHREGNO ");

            stb.append(")T1 ");

            stb.append("LEFT OUTER JOIN(");
            stb.append(       "SELECT  SCHREGNO ");
            stb.append(       "FROM    SCHREG_TRANSFER_DAT ");
            stb.append(       "WHERE   TRANSFERCD = '2' AND ");
            stb.append(               "(TRANSFER_SDATE BETWEEN '" + param._sdate + "' AND '" + param._edate + "' OR ");
            stb.append(               "TRANSFER_EDATE BETWEEN '" + param._sdate + "' AND '" + param._edate + "' OR ");
            stb.append(               "(TRANSFER_SDATE < '" + param._sdate + "' AND '" + param._edate + "'  < TRANSFER_EDATE))");
            stb.append(       "GROUP BY SCHREGNO ");
            stb.append(")T2 ON T2.SCHREGNO = T1.SCHREGNO ");

            stb.append("GROUP BY T1.GRADE, T1.HR_CLASS ");
            stb.append("ORDER BY T1.GRADE, T1.HR_CLASS ");

        } catch (Exception ex) {
            log.error("error! ", ex);
        }
        return stb.toString();

    }//prestatementAttendNum()の括り



    /** PrepareStatement作成-->学級数取得 **/
    private String prestatementHrclass(Param param){
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT COUNT(*) ");
        stb.append("FROM   SCHREG_REGD_HDAT W1 ");
        stb.append("WHERE  W1.YEAR='" + param._year + "' AND W1.SEMESTER='" + param._semester + "' ");
        return stb.toString();
    }//prestatementHrclass()の括り


    /**
     *  PrepareStatement作成 学期の開始日・終了日取得
     */
    private String prestatementSemesterDate() {
        return "SELECT SDATE,EDATE FROM SEMESTER_MST WHERE YEAR= ? AND SEMESTER= ? ";
    }

    /** print設定 */
    private void setSvfInit(final HttpServletResponse response, final Vrw32alp svf) {
        response.setContentType("application/pdf");
        svf.VrInit();                                            //クラスの初期化
        try {
            svf.VrSetSpoolFileStream(response.getOutputStream());       //PDFファイル名の設定
         } catch( java.io.IOException ex ){
            log.error("db new error:" , ex);
        }
   }


    /** svf close */
    private void closeSvf(final Vrw32alp svf) {
        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }
        svf.VrQuit();
    }


    /** DB set */
    private DB2UDB setDb(final HttpServletRequest request) throws ServletException, IOException {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
        } catch( Exception ex ){
            log.error("db new error:" , ex);
            if (db2 != null) {
                db2.close();
            }
        }
        return db2;
    }


    /** DB open */
    private boolean openDb(DB2UDB db2) {
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("db open error!", ex);
            return true;
        }//try-cathの括り
        return false;
    }//private boolean Open_db()


    /** DB close */
    private void closeDb(DB2UDB db2) {
        try {
            db2.commit();
            db2.close();
        } catch (Exception ex) {
            log.error("db close error!", ex);
        }//try-cathの括り
    }//private Close_Db()


    private static class Param {
        final String _year;
        String _semester;
        String _month;
        String _sdate;
        String _edate;
        String _monthFlg;
        final String _limit;

        final String _useVirus;
        final String _useKoudome;
        private KNJDefineSchool _definecode;     //各学校における定数等設定 05/05/12
        private KNJSchoolMst _knjSchoolMst;

        public Param(final HttpServletRequest request) {
            _year = request.getParameter("year");                        //年度
            if (request.getParameter("month") != null) {
                _semester = request.getParameter("month").substring(0, 1);   //学期
                _month = request.getParameter("month").substring(2, 4);   //月
                _monthFlg = request.getParameter("month").substring(5, 6);   //月の識別フラグ
            }
            _limit = ( request.getParameter("OUTPUT").equals("1") ) ? "15" : ( request.getParameter("OUTPUT").equals("2") )? "10" : "5";
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
        }

        /**
         *  クラス内で使用する定数設定
         *    2005/05/12
         */
        private void setClasscode(final DB2UDB db2, final String year) {
            try {
                _definecode = new KNJDefineSchool();
                _definecode.defineCode( db2, year );         //各学校における定数等設定
            } catch( Exception ex ){
                log.warn("semesterdiv-get error!",ex);
            }
        }

        /**
         * 学校マスタをロードする
         * @param db2 DB2
         * @param year 年度
         */
        public void loadSchoolMst(final DB2UDB db2, final String year) {
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, year);
                log.debug("semOffdays = " + _knjSchoolMst._semOffDays);
            } catch (SQLException ex) {
                log.error("loadSchoolMst exception!", ex);
            }
        }
    }

}//クラスの括り
