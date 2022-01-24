/**
 *
 *  学校教育システム 賢者 [成績管理]  科目別高得点表
 *
 *  2004/07/21 yamashiro KNJA160から複写して作成
 *  2004/11/16 yamashiro・全科目平均の欄を追加、フォントはゴシック体
 *                      ・平均の網掛けは、平均と同得点がない場合は前後の行に網掛けをする
 *  2005/02/16 yamashiro KIN_RECORD_DATの試験成績、学期成績、学年成績の取得仕様を変更
 *  2005/02/17 yamashiro 異動対象日のパラメータを追加
 *  2005/03/10 yamashiro 短期留学生を対象とする
 *                       評定読替科目を追加
 *  2005/06/08 yamashiro 試験を受けていない生徒が先の順位になる不具合を修正
 *                       学年成績以外は、留学生は出力しない
 *  2005/10/07 yamashiro 編入（データ仕様の変更による）について修正、および学籍異動に転学を追加
 *                           <change specification of 05/09/28>
 *
 */

package servletpack.KNJD;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_ClassCodeImp;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_EditDate;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KNJD160K {

    private static final Log log = LogFactory.getLog(KNJD160K.class);

    private KNJ_ClassCodeImp ccimp;                 //教科コード等定数設定

    private final int maxline = 18;               //１ページ当たり列数
    private final int maxStudent = 50;

    private String INTER = "INTER";
    private String TERM = "TERM";
    private String GAKKI = "GAKKI";
    private String GRADE = "GRADE";

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

    //  print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

    //  svf設定
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);        //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.fatal("[KNJD160K]DB2 open error!", ex);
            return;
        }
        Param param = createParam(db2, request);

    //  ＳＶＦ作成処理
        Set_Head(db2,svf,param);                                //見出し出力のメソッド
        //SVF出力
        boolean nonedata = Set_Detail_1(db2, svf, param);     //帳票出力のメソッド

    //  該当データ無し
        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる

    }//doGetの括り



    /** SVF-FORM **/
    private void Set_Head(DB2UDB db2, Vrw32alp svf, Param param) {

        ccimp = new KNJ_ClassCodeImp(db2);              //教科コード・学校による設定区分

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        svf.VrSetForm("KNJD160K.frm", 4);
        svf.VrsOut("nendo", nao_package.KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度");       //年度

    //  ＳＶＦ属性変更
        svf.VrAttribute("number","FF=1");

    //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            svf.VrsOut("ymd",KNJ_EditDate.h_format_JP(returnval.val3));       //作成日
        } catch (Exception e) {
            log.fatal("[KNJD160K]Set_Head() ctrl_date get error!", e);
        }

    //  学期名称の取得
        try {
            returnval = getinfo.Semester(db2, param._year, param._semester);
            svf.VrsOut("term" ,returnval.val1);       //学期名称
        } catch (Exception e) {
            log.fatal("[KNJD160K]Set_Head() Semester name get error!", e);
        }

    //  ３学期の開始日取得 05/03/09
        try {
            returnval = getinfo.Semester(db2, param._year, "3");
            param._8 = returnval.val2;                                  //学期期間FROM
        } catch (Exception ex) {
            log.warn("term1 svf-out error!", ex);
        } finally {
            if (param._8 == null) {
                param._8 = (Integer.parseInt(param._year) + 1) + "-03-31";
            }
        }

    //  学年末の場合、対象学期を取得
        if (!param._semester.equals("9")) {
            param._semeFlg = param._semester;
        } else {
            param._semeFlg = Get_Semester(db2,param);
        }

    //  組名称及び担任名の取得
        try {
            returnval = getinfo.Hrclass_Staff(db2,param._year,param._semeFlg,param._gradeHrclass,"");
            svf.VrsOut("HR_NAME"    ,returnval.val1);   //組名称
            svf.VrsOut("teacher"    ,returnval.val3);   //担任名
        } catch (Exception e) {
            log.fatal("[KNJD160K]Set_Head() teacher name get error!", e);
        }
        getinfo = null;
        returnval = null;

        /*
         *   対象データ設定 => KIN_RECORD_DATの対象フィールド
         *   2004/11/16作成
         *   param._semester => 対象学期が入っている
         *   param._test => 対象テスト種別が入っている
         *   param._4 => KIN_RECORD_DATの対象フィールドを代入、但し３学期の中間はnullとする
         *
        if (param._semester.equals("9"))
            param._4 = "GRADE_RECORD";
        else
            if (!param._semester.equals("3") || !(param._test.substring(0,2).equals("01"))){
                StringBuffer stb = new StringBuffer();
                stb.append("SEM").append(param._semester).append("_");
                stb.append((param._test.equals("0"))? "REC" :
                                 ((param._test.substring(0,2)).equals("01"))? "INTER_REC" : "TERM_REC" ); //中間・期末・学期成績
                param._4 = stb.toString();
            }
*/
        if (param._test.length() > 2) {
            if (param._test.substring(0,2).equals("01")) {
                svf.VrsOut("TEST", "中間テスト");
            } else if (param._test.substring(0,2).equals("02")) {
                svf.VrsOut("TEST", "期末テスト");
            }
        }

    }//Set_Head()の括り



    /**
     *   印刷処理メインルーチン
     *   2005/02/16Modify
     */
    private boolean Set_Detail_1(DB2UDB db2, Vrw32alp svf, Param param) {

        boolean nonedata = false;
        String obj = null;
        if (2 < param._test.length()  &&  param._test.substring(0,2).equals("01")) {
            param._fieldname = "SEM" + param._semester + "_INTER_REC";
            obj = INTER;
        } else if (2 < param._test.length()  &&  param._test.substring(0,2).equals("02")) {
            param._fieldname = "SEM" + param._semester + "_TERM_REC";
            obj = TERM;
        } else if (! param._semester.equals("9")) {
            if (param._semester.equals("3")) {
                param._fieldname = "SEM3_TERM_REC";
            } else {
                param._fieldname = "SEM" + param._semester + "_REC";
            }
            obj = GAKKI;
        } else{
            param._fieldname = "GRADE_RECORD";
            obj = GRADE;
        }
log.debug("fieldname="+param._fieldname);

    //  値の保存用配列を作成
        int studentnum = Get_Sch_Cnt(db2, param);              //生徒数の取得
        if (studentnum == 0) {
            studentnum = 1;
        }
//        int subclassnum = Get_Subj_Cnt(db2, param);            //科目数の取得
//log.debug("subclassnum="+subclassnum);
//        if (subclassnum == 0 ) {
//            subclassnum = 1;
//        }
//        subclassnum++;                                        //全科目平均の分 04/11/16
//        subclassnum=18;

    //  SQL作成
        PreparedStatement ps2 = null;

        try {
            ps2 = db2.prepareStatement( Pre_Stat2(param, obj ));      //クラスの科目別平均点及び科目名の表 05/02/16Modify
        } catch (Exception ex) {
            log.fatal("[KNJD160K]Set_Detail_1 PreparedStatement error!", ex);
            return false;
        }
//log.debug("ps2="+ps2.toString());
    //  科目別平均点及び科目名を保存しながら学級の全得点を保存する
        final Map subclassMap = new HashMap();
        ResultSet rs2 = null;
        try {
            rs2 = ps2.executeQuery();
            while (rs2.next()){
                final Subclass subclass = new Subclass(rs2.getString("SUBCLASSCD"), rs2.getString("SUBCLASSABBV"), rs2.getInt("AVG_REC"));
                subclassMap.put(subclass._subclasscd, subclass);
            }
            log.debug("[KNJD160K]Set_Detail_1 read ok!");
        } catch (Exception ex) {
            log.fatal("[KNJD160K]Set_Detail_1 read error!", ex);
        } finally {
            //DbUtils.closeQuietly(rs1);
            DbUtils.closeQuietly(rs2);
            db2.commit();
        }

        PreparedStatement ps1 = null;
        PreparedStatement ps3 = null;
        try {
            ps1 = db2.prepareStatement( Pre_Stat1(param, obj, 1 ));   //クラスの科目別全得点表             05/02/16Modify
            ps3 = db2.prepareStatement( Pre_Stat1(param, obj, 0 ));   //クラスの全科目全得点表 04/11/15    05/02/16Modify
        } catch (Exception ex) {
            log.fatal("[KNJD160K]Set_Detail_1 PreparedStatement error!", ex);
            return false;
        }

        for (final Iterator it = subclassMap.values().iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            log.debug(" set subclass " + subclass._subclasscd);
            ResultSet rs1 = null;
            try {
                //科目別全得点を保存
                if ("0".equals(subclass._subclasscd)) {
                    rs1 = ps3.executeQuery();
                } else {
                    final String[] split = StringUtils.split(subclass._subclasscd, "-");
                    ps1.setString(1, split[0]);
                    ps1.setString(2, split[1]);
                    ps1.setString(3, split[2]);
                    ps1.setString(4, split[3]);
                    rs1 = ps1.executeQuery();
                }
                for (int ib=0 ; rs1.next() && ib<studentnum ; ib++) {
                    if (rs1.getString("SCORE") != null) {
                        subclass._scoreList.add(Integer.valueOf(rs1.getString("SCORE")));
                    }
                }
            } catch (Exception ex) {
                log.fatal("[KNJD160K]Set_Detail_1 read error!", ex);
            } finally {
                DbUtils.closeQuietly(rs1);
                db2.commit();
            }
        }
        log.debug("[KNJD160K]Set_Detail_2 read ok!");

    //  生徒別科目別順位位置のSQL作成
        PreparedStatement ps4 = null;
        try {
            final String sql4 = Pre_Stat3(param, obj);
            ps4 = db2.prepareStatement(sql4);  //生徒ごとの科目別の順位位置表（出席番号順） 05/02/16Modify
        } catch (Exception ex) {
            log.fatal("[KNJD160K]Set_Detail_1 PreparedStatement error!", ex);
            return false;
        }

       /*
        * 印刷
        *   Set_Detail_2において生徒毎に印刷を行う
        *       使用する配列  String student_subclasscd_array[] => 生徒の科目コードを入れる
        *                     int student_subclasscd_array[] => 生徒の得点を入れる
        */
        ResultSet rs = null;
        List studentList = new ArrayList();
        try {
            rs = ps4.executeQuery();
//log.debug("ps1="+ps1.toString());
            while( rs.next()){
                if (null == Student.getStudent(studentList, rs.getString("SCHREGNO"))) {
                    final Student student = new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("ATTENDNO"));
                    studentList.add(student);
                }
                final Student student = Student.getStudent(studentList, rs.getString("SCHREGNO"));
                student._subclasscdList.add(rs.getString("SUBCLASSCD"));
                student._subclassRowMap.put(rs.getString("SUBCLASSCD"), Integer.valueOf(rs.getString("ROW")));

            }
            log.debug("[KNJD160K]Set_Detail_1() Stat3 read ok!");
        } catch (Exception ex) {
            log.fatal("[KNJD160K]Set_Detail_1() Stat3 read error!", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            ///SVF出力
            if (Set_Detail_2(svf, student, studentnum, subclassMap)) {
                nonedata = true;
            }
        }

        DbUtils.closeQuietly(ps1);
        DbUtils.closeQuietly(ps2);
        DbUtils.closeQuietly(ps3);
        DbUtils.closeQuietly(ps4);

        return nonedata;

    }//Set_Detail_1()の括り

    private int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
    }

    /**
      *  SVF-FORM
      *  生徒毎の印刷
      *  2004/11/16Modify
      *  parameter  String student_subclasscd_array[] => 生徒の科目コードが入っている
      *             int student_row_array[] => 生徒の順位位置が入っている
      *             int studentnum => 生徒数
      *  2005/03/10Modify １ページ当たりの列数を14から18に変更（フォームが変更されている？）  インスタンス変数で定数として定義
      **/
    private boolean Set_Detail_2(Vrw32alp svf, Student student, int studentnum, final Map subclassMap) {
        boolean nonedata = false;
        int outcount = maxline;         //列残数出力用
        //boolean amikake = false;
        boolean second = false;

        try {
            svf.VrsOut("number", student._attendno);    //出席番号
            svf.VrsOut("name", "");        //生徒名
            svf.VrsOut("name2", "");        //生徒名
            svf.VrsOut("name3", "");        //生徒名
            svf.VrsOut("name" + (getMS932ByteLength(student._name) > 26 ? "3" : getMS932ByteLength(student._name) > 20 ? "2" : ""), student._name);        //生徒名


            //生徒個別の科目コードを読込む => １ページ１４列づつ
            for (int i = 0 ; i < student._subclasscdList.size(); i += maxline) {
                //生徒個別の得点を読込む => １列５０名づつ
                for (int j = 0 ; j < studentnum ; j += maxStudent) {
                    //１ページ１４科目を出力
                    for (int k = 0 ; k < maxline ; k++) {
                        final int arridx = i / maxline * maxline + k;
                        if (arridx > student._subclasscdList.size() - 1) {
                            break;          //生徒個別の科目数で出力を制限する！
                        }
                        final String subclasscd = (String) student._subclasscdList.get(arridx);
                        if (subclasscd == null) {
                            break;    // 04/11/17
                        }
                        final Subclass subclass = (Subclass) subclassMap.get(subclasscd);
                        if (null == subclass) {
                            continue;
                        }
                        //平均がない場合の処理
                        if (!second && !subclasscd.equals("0")) {
                            svfFieldAttribute(svf, "0");                    // SVF-FIELD-ATTRIBUTE 04/11/16
                            svf.VrsOut("subject"  ,"平均");         //科目名
                            svf.VrEndRecord();
                        }
                        second = true;
                        svfFieldAttribute(svf, subclasscd);           // SVF-FIELD-ATTRIBUTE 04/11/16
                        svf.VrsOut("subject"  ,subclass._abbv);            //科目名
                        //全科目得点平均および科目別得点５０個を出力
                        boolean booset = false;
                        int n = 0;
                        //５０個の得点をみていく。科目別平均点と同点なら網掛けをする。得点が平均点以下ならブレイク。
                        for (n = 0 ; n < subclass._scoreList.size(); n++) {
                            if (subclass.getScore(n) == subclass._avg) {
                                svf.VrAttribute("point" + (n+1) ,"Paint=(2,90,2),Bold=1");
                                booset = true;
                            }
                            if (subclass.getScore(n) < subclass._avg) {
                                break;
                            }
                        }

                        //平均点と同点がないなら、平均点の前後の行に網掛けをする。
                        if (!booset) {
                            if (n < subclass._scoreList.size()) {
                                svf.VrAttribute("point" + (n+1) ,"Paint=(2,90,2),Bold=1");
                            }
                            if (0 <= ( n -= 1 )) {
                                svf.VrAttribute("point" + (n+1) ,"Paint=(2,90,2),Bold=1");
                            }
                        }

                        for (int l = 0 ; l < maxStudent ; l++) {
                            if ((j / maxStudent * maxStudent + l) > subclass._scoreList.size() - 1) {
                                break;      //科目別の生徒数で出力を制限する！
                            }
                            svfAmikakeToPoint( svf, j / maxStudent * maxStudent + l, (Integer) student._subclassRowMap.get(subclasscd));
                            svf.VrsOut("point" + ( l+1 ), String.valueOf( subclass._scoreList.get(j / maxStudent * maxStudent + l)));  //科目別全得点
                        }
//log.debug("k="+k);
                        svf.VrEndRecord();
                        svfFieldInit(svf);   // 04/10/17Modify
                        nonedata = true;
                        outcount = k+1;
                    }
                    //科目列の出力が１４列に満たない場合、改ページのため空列を出力する！
                    for (; outcount < maxline ; outcount++ ) {
                        svf.VrEndRecord();
                    }
                    outcount = maxline;
                }
            }
        } catch (Exception ex) {
            log.fatal("[KNJD160K]Set_Detail_2() svf set error!", ex);
        }
        return nonedata;

    }//Set_Detail_2()の括り


    /**
      *  PrepareStatement作成 共通部品
      *  学籍データから対象生徒を抽出
      *      異動者は除外
      *  2005/02/14 停学を対象に含める
      *  2005/02/17 異動対象日を印刷指示画面のパラメータに変更
      *  2005/03/10Modify 留学開始日が３学期の場合（短期留学）は対象とする
      *  2005/06/08Modify 学年成績以外は、留学において基準日が期間内にあれば対象外とする
      **/
    private String preStatCommon(Param param){

        StringBuffer stb = new StringBuffer();
        stb.append("WITH SCHNO AS(");
        stb.append(     "SELECT W1.SCHREGNO,W1.ATTENDNO ");
        //stb.append(       "FROM   SCHREG_REGD_DAT W1 ");
        stb.append(     "FROM   SCHREG_REGD_DAT W1 ,SEMESTER_MST W2 ");   //<revive for change specification of 05/09/28>
        stb.append(     "WHERE  W1.YEAR = '" + param._year + "' AND ");
        stb.append(            "W1.SEMESTER = '"+param._semeFlg+"' AND ");
        stb.append(            "W1.GRADE||W1.HR_CLASS = '" + param._gradeHrclass + "' AND ");
        stb.append(            "W2.YEAR = '" + param._year + "' AND W1.SEMESTER = W2.SEMESTER AND ");   //<revive for change specification of 05/09/28>
        stb.append(            "NOT EXISTS( SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
        //stb.append(                            "AND S1.GRD_DIV IN ('2','3') AND S1.GRD_DATE < '" + param._date + "' ) AND ");  //05/03/09Modify
        stb.append(                            "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END) ");     //<change specification of 05/09/28>
        stb.append(                              "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END)) ) ");  //<change specification of 05/09/28>

        // 05/02/10停学を除外
        if (Integer.parseInt(param._test ) == 0  &&  Integer.parseInt(param._semester ) == 9) {
            //05/03/09Modify 留学開始日が３学期の場合は成績も出力する
            stb.append(    "AND NOT EXISTS( SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                    "WHERE  S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                      "AND ((S1.TRANSFERCD IN ('2') AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //<change specification of 05/09/28>
            stb.append(                        "OR (S1.TRANSFERCD IN ('1') AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE AND S1.TRANSFER_EDATE < '" + param._8 + "' ))) ");  //<change specification of 05/09/28>
            //stb.append(                               "((S1.TRANSFERCD IN ('2') AND '" + param._date + "' BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) OR ");
            //stb.append(                                "(S1.TRANSFERCD IN ('1') AND '" + param._date + "' BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE AND S1.TRANSFER_EDATE < '" + param._8 + "' ) OR ");
            //stb.append(                                "(S1.TRANSFERCD IN ('4') AND '" + param._date + "' < S1.TRANSFER_SDATE)) )) ");
        } else{
            //学年成績以外は、留学生は出力しない 05/06/08Modiy
            stb.append(    "AND NOT EXISTS( SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                    "WHERE  S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                       "AND ((S1.TRANSFERCD IN ('2') AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //<change specification of 05/09/28>
            stb.append(                         "OR (S1.TRANSFERCD IN ('1') AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE))) ");  //<change specification of 05/09/28>
            //stb.append(                               "((S1.TRANSFERCD IN ('2') AND '" + param._date + "' BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) OR ");
            //stb.append(                                "(S1.TRANSFERCD IN ('1') AND '" + param._date + "' BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) OR ");
            //stb.append(                                "(S1.TRANSFERCD IN ('4') AND '" + param._date + "' < S1.TRANSFER_SDATE)) )) ");
        }
        stb.append(")" );

        return stb.toString();
    }

    private String PreStatRecCommon(final Param param, final String obj) {
        final StringBuffer stb = new StringBuffer();

        stb.append(preStatCommon(param));

        if (! param._test.equals("0")){
            stb.append(",RECORD_REC AS(");
            stb.append( getRecordSql(param, obj));
            stb.append(     ") ");
        } else{
            stb.append(",RECORD_REC_A AS(");
            stb.append( getRecordSql(param, obj));
            stb.append(     ") ");

            //評価読替科目 05/03/09
            stb.append(",REPLACE_REC_A AS(");
            stb.append(     "SELECT SCHREGNO, ");
            stb.append(            "W2.COMBINED_CLASSCD AS CLASSCD, ");
            stb.append(            "W2.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ");
            stb.append(            "W2.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ");
            stb.append(            "W2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
            stb.append(            "W2.ATTEND_CLASSCD AS ATTEND_CLASSCD, ");
            stb.append(            "W2.ATTEND_SCHOOL_KIND AS ATTEND_SCHOOL_KIND, ");
            stb.append(            "W2.ATTEND_CURRICULUM_CD AS ATTEND_CURRICULUM_CD, ");
            stb.append(            "W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
            stb.append(            "SCORE ");
            stb.append(     "FROM   RECORD_REC_A W1, SUBCLASS_REPLACE_COMBINED_DAT W2 ");
            stb.append(     "WHERE  W1.SUBCLASSCD = W2.ATTEND_SUBCLASSCD AND ");
            stb.append(            "W1.CLASSCD = W2.ATTEND_SUBCLASSCD AND ");
            stb.append(            "W1.SCHOOL_KIND = W2.ATTEND_SCHOOL_KIND AND ");
            stb.append(            "W1.CURRICULUM_CD = W2.ATTEND_CURRICULUM_CD AND ");
            stb.append(            "W2.YEAR='" + param._year + "' AND REPLACECD='1' ");
            stb.append(     ") ");

            stb.append(",REPLACE_REC_CNT AS(");
            stb.append(     "SELECT SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD,");
            stb.append(            "SUM(CASE WHEN SCORE IN ('KS','( )','欠') THEN 1 ELSE 0 END) AS KS ");
            stb.append(     "FROM REPLACE_REC_A W1 ");
            stb.append(     "WHERE W1.SCORE IN('KK','KS','( )','欠','公') ");
            stb.append(     "GROUP BY SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
            stb.append(     "HAVING 0 < COUNT(*) ");
            stb.append(     ") ");

            stb.append(",REPLACE_REC_B AS(");
            stb.append(     "SELECT  SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, ");
            stb.append(             "RTRIM(CHAR(INT(ROUND(AVG(FLOAT(INT(SCORE))),0)))) AS SCORE ");
            stb.append(     "FROM    REPLACE_REC_A W1 ");
            stb.append(     "WHERE   W1.SCORE NOT IN('KK','KS','( )','欠','公') AND W1.SCORE IS NOT NULL ");
            stb.append(             "AND NOT EXISTS(SELECT 'X' FROM REPLACE_REC_CNT W2 WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.CLASSCD = W1.CLASSCD AND W2.SCHOOL_KIND = W1.SCHOOL_KIND AND W2.CURRICULUM_CD = W1.CURRICULUM_CD AND W2.SUBCLASSCD = W1.SUBCLASSCD) ");
            stb.append(     "GROUP BY SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
            stb.append(     ") ");

            //成績データ(通常科目＋評価読替科目) 05/03/09
            stb.append(",RECORD_REC AS(");
            stb.append(     "SELECT SCHREGNO, ");
            stb.append(            "CLASSCD, ");
            stb.append(            "SCHOOL_KIND, ");
            stb.append(            "CURRICULUM_CD, ");
            stb.append(            "SUBCLASSCD, ");
            stb.append(            "SCORE ");
            stb.append(     "FROM   RECORD_REC_A W1 ");
            stb.append(     "WHERE  NOT EXISTS( SELECT 'X' FROM REPLACE_REC_B W2 WHERE W1.CLASSCD = W2.CLASSCD AND W1.SCHOOL_KIND = W2.SCHOOL_KIND AND W1.CURRICULUM_CD = W2.CURRICULUM_CD AND W1.SUBCLASSCD = W2.SUBCLASSCD) ");
            stb.append(     "UNION  ALL ");
            stb.append(     "SELECT SCHREGNO, ");
            stb.append(            "CLASSCD, ");
            stb.append(            "SCHOOL_KIND, ");
            stb.append(            "CURRICULUM_CD, ");
            stb.append(            "SUBCLASSCD, ");
            stb.append(            "SCORE ");
            stb.append(     "FROM   REPLACE_REC_B W1 ");
            stb.append(     ") ");
        }
        return stb.toString();
    }



    protected StringBuffer getRecordSql(final Param param, final String obj) {
        final StringBuffer stb = new StringBuffer();
        if (INTER.equals(obj)) {
            // 中間試験用
            stb.append("SELECT  W3.SCHREGNO, W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD, ");
            stb.append(        "CASE WHEN SEM" + param._semester + "_INTER_REC IS NULL AND SEM" + param._semester + "_INTER_REC_DI IN('KK','KS') THEN SEM" + param._semester + "_INTER_REC_DI ");
            stb.append(             "WHEN SEM" + param._semester + "_INTER_REC IS NOT NULL THEN RTRIM(CHAR(SEM" + param._semester + "_INTER_REC)) ");
            stb.append(             "ELSE NULL END AS SCORE ");
            stb.append("FROM    KIN_RECORD_DAT W3 ");

        } else if (TERM.equals(obj)) {
            // 期末試験用
            stb.append("SELECT  W3.SCHREGNO, W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD, ");
            stb.append(        "CASE WHEN SEM" + param._semester + "_TERM_REC IS NULL AND SEM" + param._semester + "_TERM_REC_DI IN('KK','KS') THEN SEM" + param._semester + "_TERM_REC_DI ");
            stb.append(             "WHEN SEM" + param._semester + "_TERM_REC IS NOT NULL THEN RTRIM(CHAR(SEM" + param._semester + "_TERM_REC)) ");
            stb.append(             "ELSE NULL END AS SCORE ");
            stb.append("FROM    KIN_RECORD_DAT W3 ");
        } else if (GAKKI.equals(obj)) {
            // 学期成績用
            if (param._semester.equals("1")  ||  param._semester.equals("2")) {
                //１・２学期仕様
                stb.append("SELECT  W3.SCHREGNO, W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD, ");
                stb.append(        "CASE WHEN ((SEM" + param._semester + "_INTER_REC IS NULL AND SEM" + param._semester + "_INTER_REC_DI IN('KK','KS')) OR ");
                stb.append(                   "(SEM" + param._semester + "_TERM_REC  IS NULL AND SEM" + param._semester + "_TERM_REC_DI  IN('KK','KS'))) AND ");
                stb.append(                   "VALUE(SEM" + param._semester + "_REC_FLG,'0') = '0' THEN '( )' ");
                stb.append(            "WHEN SEM" + param._semester + "_REC IS NOT NULL THEN RTRIM(CHAR(SEM" + param._semester + "_REC)) ");
                stb.append(            "ELSE NULL END AS SCORE ");
                stb.append("FROM    KIN_RECORD_DAT W3 ");
            } else {
                //３学期仕様
                stb.append("SELECT  W3.SCHREGNO, W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD, ");
                stb.append(        "CASE WHEN (SEM" + param._semester + "_TERM_REC IS NULL AND SEM" + param._semester + "_TERM_REC_DI  IN('KK','KS')) AND ");
                stb.append(                  "VALUE(SEM" + param._semester + "_TERM_REC_FLG,'0') = '0' THEN '( )' ");
                stb.append(             "WHEN SEM" + param._semester + "_TERM_REC IS NOT NULL THEN RTRIM(CHAR(SEM" + param._semester + "_TERM_REC)) ");
                stb.append(             "ELSE NULL END AS SCORE ");
                stb.append("FROM    KIN_RECORD_DAT W3 ");
            }

        } else if (GRADE.equals(obj)){
            // 学年成績用
            if (param._semester.equals("1")) {
                //１学期仕様
                stb.append("SELECT  W3.SCHREGNO, W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD, ");
                stb.append(        "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
                stb.append(                   "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
                stb.append(                   "VALUE(SEM1_REC_FLG,'0') = '0' THEN '( )' ");
                stb.append(             "WHEN " + param._fieldname + " IS NOT NULL THEN RTRIM(CHAR( " + param._fieldname + " )) ");
                stb.append(             "ELSE NULL END AS SCORE ");
                stb.append(        ",CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
                stb.append(                   "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
                stb.append(                   "VALUE(SEM1_REC_FLG,'0') = '0' THEN '()' ");
                stb.append(             "WHEN  " + param._fieldname + "  IS NOT NULL THEN ");
                stb.append(                  "CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS ");
                stb.append(                                     "WHEN 'B' THEN B_PATTERN_ASSESS ");
                stb.append(                                     "WHEN 'C' THEN C_PATTERN_ASSESS ELSE NULL END ");
                stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
                stb.append("FROM    KIN_RECORD_DAT W3 ");

            } else if (param._semester.equals("2") || param._gradeHrclass.substring(0,2).equals("03")) {
                //２学期仕様
                stb.append("SELECT  W3.SCHREGNO, W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD, ");
                stb.append(        "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
                stb.append(                   "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
                stb.append(                   "VALUE(SEM1_REC_FLG,'0') = '0' THEN '( )' ");
                stb.append(             "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
                stb.append(                   "(SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
                stb.append(                   "VALUE(SEM2_REC_FLG,'0') = '0' THEN '( )' ");
                stb.append(             "WHEN  " + param._fieldname + "  IS NOT NULL THEN RTRIM(CHAR( " + param._fieldname + " )) ");
                stb.append(             "ELSE NULL END AS SCORE ");
                stb.append(        ",CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
                stb.append(                   "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
                stb.append(                   "VALUE(SEM1_REC_FLG,'0') = '0' THEN '()' ");
                stb.append(             "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
                stb.append(                   "(SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
                stb.append(                   "VALUE(SEM2_REC_FLG,'0') = '0' THEN '()' ");
                stb.append(             "WHEN  " + param._fieldname + "  IS NOT NULL THEN ");
                stb.append(                  "CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS ");
                stb.append(                                     "WHEN 'B' THEN B_PATTERN_ASSESS ");
                stb.append(                                     "WHEN 'C' THEN C_PATTERN_ASSESS ELSE NULL END ");
                stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
                stb.append("FROM    KIN_RECORD_DAT W3 ");
            } else {
                //３学期仕様
                stb.append("SELECT  W3.SCHREGNO, W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD, ");
                stb.append(        "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
                stb.append(                   "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
                stb.append(                   "VALUE(SEM1_REC_FLG,'0') = '0' THEN '( )' ");
                stb.append(             "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
                stb.append(                   "(SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
                stb.append(                   "VALUE(SEM2_REC_FLG,'0') = '0' THEN '( )' ");
                stb.append(             "WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND ");
                stb.append(                   "VALUE(SEM3_TERM_REC_FLG,'0') = '0' THEN '( )' ");
                stb.append(             "WHEN  " + param._fieldname + "  IS NOT NULL THEN RTRIM(CHAR( " + param._fieldname + " )) ");
                stb.append(             "ELSE NULL END AS SCORE ");
                stb.append(        ",CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
                stb.append(                   "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
                stb.append(                   "VALUE(SEM1_REC_FLG,'0') = '0' THEN '()' ");
                stb.append(             "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
                stb.append(                   "(SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
                stb.append(                   "VALUE(SEM2_REC_FLG,'0') = '0' THEN '()' ");
                stb.append(             "WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND ");
                stb.append(                   "VALUE(SEM3_TERM_REC_FLG,'0') = '0' THEN '()' ");
                stb.append(             "WHEN  " + param._fieldname + "  IS NOT NULL THEN ");
                stb.append(                  "CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS ");
                stb.append(                                     "WHEN 'B' THEN B_PATTERN_ASSESS ");
                stb.append(                                     "WHEN 'C' THEN C_PATTERN_ASSESS ELSE NULL END ");
                stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
                stb.append("FROM    KIN_RECORD_DAT W3 ");
            }
        }
        stb.append("WHERE   W3.YEAR = '" + param._year + "' AND ");
        stb.append(        "SUBSTR(SUBCLASSCD,1,2) <= '" + ccimp.subject_U + "' AND ");
        stb.append(        "CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '"+param._year+"' AND NAMECD1 = 'D065') AND ");
        stb.append(        "EXISTS(SELECT  'X' FROM SCHNO W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
        return stb;
    }

    /**
      *  PrepareStatement作成
      *  任意科目のクラス全員の得点表および生徒別全科目平均表
      *  parameter  int pdiv => 0:全科目   1:科目別
      *      異動者は除外
      *      試験の場合、１科目以上の公・欠があれば平均から除外???
      *      試験の場合、公・欠の科目は除外
      *  2005/02/16Modify
      *  2005/03/10Modify 評定読替科目を追加
      **/
    private String Pre_Stat1(Param param, String obj, int pdiv )
    {
        StringBuffer stb = new StringBuffer();

        stb.append(PreStatRecCommon(param, obj));

        if (pdiv == 0) {
            //生徒別全科目平均
            stb.append("SELECT  INT(ROUND(AVG(FLOAT(INT(SCORE))),0)) AS SCORE,");
            stb.append(        "AVG(FLOAT(INT(SCORE))) AS FULLSCORE ");

            if (! param._test.equals("0")) {
                stb.append("FROM    RECORD_REC W1 ");
            } else {
                stb.append("FROM    RECORD_REC_A W1 ");
            }
            stb.append("WHERE   SCORE IS NOT NULL AND SCORE NOT IN ('KK','KS','( )') AND ");
            stb.append(        "NOT EXISTS(SELECT 'X' ");
            stb.append(                   "FROM   SUBCLASS_REPLACE_COMBINED_DAT W2 ");
            stb.append(                   "WHERE  W1.SUBCLASSCD = W2.COMBINED_SUBCLASSCD AND ");
            stb.append(                          "W1.CLASSCD = W2.COMBINED_CLASSCD AND ");
            stb.append(                          "W1.SCHOOL_KIND = W2.COMBINED_SCHOOL_KIND AND ");
            stb.append(                          "W1.CURRICULUM_CD = W2.COMBINED_CURRICULUM_CD AND ");
            stb.append(                          "W2.YEAR ='" + param._year + "' AND ");
            stb.append(                          "W2.REPLACECD = '1') AND ");
            stb.append(        "SCHREGNO NOT IN(SELECT SCHREGNO ");
            stb.append(                        "FROM   RECORD_REC ");
            stb.append(                        "WHERE  SCORE IS NOT NULL AND SCORE IN('KK', 'KS','( )') ");
            stb.append(                        "GROUP BY SCHREGNO ");
            stb.append(                        "HAVING 0 < COUNT(*) ");
            stb.append(                        ") ");
            stb.append("GROUP BY SCHREGNO ");
            stb.append("ORDER BY FULLSCORE DESC");

        } else {
            //生徒別科目別得点
            stb.append("SELECT  INT(SCORE) AS SCORE ");
            stb.append("FROM    RECORD_REC ");
            stb.append("WHERE   SCORE IS NOT NULL AND SCORE NOT IN ('KK','KS','( )') AND ");
            stb.append(        " CLASSCD = ? ");
            stb.append(        "AND SCHOOL_KIND = ? ");
            stb.append(        "AND CURRICULUM_CD = ? ");
            stb.append(        "AND SUBCLASSCD = ? ");
            stb.append("ORDER BY INT(SCORE) DESC");
        }
        return stb.toString();
    }

    /**
      *  PrepareStatement作成
      *  クラスの全科目平均点・科目別平均点・科目名の表
      *      異動者は除外
      *      試験の場合、１科目以上の公・欠があれば平均から除外???
      *      試験の場合、公・欠の科目は科目別席次から除外
      *  2005/02/16Modify
      *  2005/03/10Modify 評定読替科目を追加
      *
      **/
    private String Pre_Stat2(Param param, String obj) {

        StringBuffer stb = new StringBuffer();

        stb.append(PreStatRecCommon(param, obj));

        stb.append("SELECT S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD ||'-' || S1.SUBCLASSCD AS SUBCLASSCD, SUBCLASSABBV, AVG_REC ");
        stb.append("FROM  (SELECT  SUBCLASSCD, ");
        stb.append(               " CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
        stb.append(               "INT(ROUND(AVG(FLOAT(INT(SCORE))),0))AS AVG_REC ");
        stb.append(       "FROM    RECORD_REC W1 ");
        stb.append(       "WHERE   SCORE IS NOT NULL AND SCORE NOT IN ('KK','KS','( )') ");
        stb.append(       "GROUP BY SUBCLASSCD,");
        stb.append(               " CLASSCD, SCHOOL_KIND, CURRICULUM_CD ");
        stb.append("      )S1 ");
        stb.append("      INNER JOIN SUBCLASS_MST S2 ON S1.CLASSCD = S2.CLASSCD ");
        stb.append("   AND S1.SCHOOL_KIND = S2.SCHOOL_KIND ");
        stb.append("   AND S1.CURRICULUM_CD = S2.CURRICULUM_CD ");
        stb.append("   AND S1.SUBCLASSCD = S2.SUBCLASSCD ");

        stb.append("UNION  SELECT  '0' AS SUBCLASSCD, ");
        stb.append(               "'平均' AS SUBCLASSABBV, ");
        stb.append(               "INT(ROUND(AVG(FLOAT(INT(SCORE))),0))AS AVG_REC ");
        stb.append(       "FROM    RECORD_REC W1 ");
        stb.append(       "WHERE   SCORE IS NOT NULL AND SCORE NOT IN ('KK','KS','( )') AND ");
                /*
        stb.append(               "SCHREGNO NOT IN(SELECT SCHREGNO ");
        stb.append(                               "FROM   RECORD_REC ");
        stb.append(                               "WHERE  SCORE IS NOT NULL AND SCORE IN('KK', 'KS','( )') ");
        stb.append(                               "GROUP BY SCHREGNO ");
        stb.append(                               "HAVING 0 < COUNT(*) ) AND ");
                */
        stb.append(               "NOT EXISTS(SELECT 'X' ");
        stb.append(                          "FROM   SUBCLASS_REPLACE_COMBINED_DAT W2 ");
        stb.append(                          "WHERE  W1.SUBCLASSCD = W2.COMBINED_SUBCLASSCD AND ");
        stb.append(                                 "W1.CLASSCD = W2.COMBINED_CLASSCD AND ");
        stb.append(                                 "W1.SCHOOL_KIND = W2.COMBINED_SCHOOL_KIND AND ");
        stb.append(                                 "W1.CURRICULUM_CD = W2.COMBINED_CURRICULUM_CD AND ");
        stb.append(                                 "W2.YEAR ='" + param._year + "' AND ");
        stb.append(                                 "REPLACECD = '1') ");
        stb.append("ORDER BY SUBCLASSCD");
        return stb.toString();

    }//Pre_Stat2()の括り


    /**
      *  PrepareStatement作成
      *  科目別の順位位置表（出席番号順）
      *      異動者は除外
      *      試験の場合、１科目以上の公・欠があれば平均席次から除外
      *      試験の場合、公・欠の科目は科目別席次から除外
      *      科目別席次は、得点・全科目平均点・出席番号の順
      *  2005/02/16Modify
      *  2005/03/10Modify 評定読替科目を追加
      *                   全科目平均には評定読替科目を含めない
      *
      **/
    private String Pre_Stat3(Param param, String obj) {

        StringBuffer stb = new StringBuffer();

        stb.append(PreStatRecCommon(param, obj));

        //生徒の学籍の表
        stb.append(",SCHNO_A AS(");
        stb.append(    "SELECT W1.SCHREGNO,ATTENDNO,NAME ");
        stb.append(    "FROM   SCHNO W1,");
        stb.append(           "SCHREG_BASE_MST W2 ");
        stb.append(    "WHERE  W1.SCHREGNO = W2.SCHREGNO), ");

        //生徒の全科目平均の表 04/11/16
        stb.append("TOTALAVERAGE AS(");
        stb.append(    "SELECT  W1.SCHREGNO, ");
        stb.append(            "AVG(FLOAT(INT(SCORE)))AS TOTALAVERAGESCORE ");
        stb.append(    "FROM    RECORD_REC W1 ");
        stb.append(    "LEFT JOIN (SELECT SCHREGNO ");
        stb.append(                            "FROM   RECORD_REC ");
        stb.append(                            "WHERE  SCORE IS NOT NULL AND SCORE IN('KK', 'KS','( )') ");
        stb.append(                            "GROUP BY SCHREGNO ");
        stb.append(                            "HAVING 0 < COUNT(*) ) W2 ON W2.SCHREGNO = W1.SCHREGNO ");
        stb.append(    "WHERE W2.SCHREGNO IS NULL AND ");
        stb.append(            "NOT EXISTS(SELECT 'X' ");
        stb.append(                       "FROM   SUBCLASS_REPLACE_COMBINED_DAT W2 ");
        stb.append(                       "WHERE  W1.SUBCLASSCD = W2.COMBINED_SUBCLASSCD AND ");
        stb.append(                              "W1.CLASSCD = W2.COMBINED_CLASSCD AND ");
        stb.append(                              "W1.SCHOOL_KIND = W2.COMBINED_SCHOOL_KIND AND ");
        stb.append(                              "W1.CURRICULUM_CD = W2.COMBINED_CURRICULUM_CD AND ");
        stb.append(                              "W2.YEAR ='" + param._year + "' AND ");
        stb.append(                              "REPLACECD = '1') ");
        stb.append(       "AND SCORE IS NOT NULL ");  //05/06/08
        stb.append(    "GROUP BY W1.SCHREGNO) ");

        //メイン表
        stb.append("SELECT  ATTENDNO, SCHREGNO, NAME, CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ROW ");
        stb.append("FROM  (SELECT ATTENDNO, T1.SCHREGNO, NAME, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, SCORE, ");
        stb.append(              "ROW_NUMBER() OVER(PARTITION BY CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ORDER BY SCORE DESC,VALUE(TOTALAVERAGESCORE,0) DESC,ATTENDNO)AS ROW ");
        stb.append(       "FROM  (SELECT  CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, W1.SCHREGNO, ATTENDNO, NAME, ");
        stb.append(                      "CASE WHEN SCORE IN('KK', 'KS','( )') THEN -1 ELSE INT(SCORE) END AS SCORE ");
        stb.append(              "FROM    RECORD_REC W1,");
        stb.append(                      "SCHNO_A W2 ");
        stb.append(              "WHERE   W2.SCHREGNO = W1.SCHREGNO AND ");
        stb.append(                      "SCORE IS NOT NULL ");
        stb.append(              ")T1 ");
        stb.append(              "LEFT JOIN TOTALAVERAGE T2 ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append(       ")AS S1 ");
        stb.append("WHERE SCHREGNO IN" + param._schregnos);

        stb.append("UNION SELECT  ATTENDNO, SCHREGNO, NAME, '0' AS SUBCLASSCD, ROW ");
        stb.append("FROM  (SELECT ATTENDNO, W2.SCHREGNO, NAME,");
        stb.append(              "CASE WHEN W1.SCHREGNO IS NULL THEN 50 ELSE ROW_NUMBER() OVER(ORDER BY case when w1.schregno is null then -1 else TOTALAVERAGESCORE end DESC,ATTENDNO) END AS ROW ");
        stb.append(       "FROM   SCHNO_A W2 ");
        stb.append(              "LEFT JOIN TOTALAVERAGE W1 ON W2.SCHREGNO = W1.SCHREGNO");
        stb.append(       ")T1 ");
        stb.append("WHERE SCHREGNO IN" + param._schregnos);
        stb.append("ORDER BY ATTENDNO,SUBCLASSCD");
        return stb.toString();

    }//Pre_Stat3()の括り


    /**
      *  学籍データの該当年度における最大学期を取得
      **/
    private String Get_Semester(DB2UDB db2,Param param) {

        PreparedStatement ps = null;
        ResultSet rs = null;
        String semester = "0";
        String sql = "SELECT MAX(SEMESTER) FROM SCHREG_REGD_HDAT WHERE YEAR='" +param._year+"' AND GRADE||HR_CLASS='"+param._gradeHrclass+"'";
        try{
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next())semester = rs.getString(1);
        } catch (Exception ex) {
            log.fatal("[KNJD160K]int Get_Sch_Cnt()  get count1 error!", ex );
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return semester;

    }//private int Get_Semester()


    /**
      *  生徒数を取得
      **/
    private int Get_Sch_Cnt(DB2UDB db2,Param param) {

        PreparedStatement ps = null;
        ResultSet rs = null;
        int sch_cnt = 0;
        String sql = "SELECT COUNT(DISTINCT SCHREGNO) "
                + "FROM   KIN_RECORD_DAT W1 "
                + "WHERE  YEAR = '" + param._year+"' AND "
                       + "EXISTS(SELECT 'X' "
                              + "FROM   SCHREG_REGD_DAT W2 "
                              + "WHERE  W2.YEAR = '" + param._year + "' AND "
                                     + "W2.SEMESTER = '"+param._semeFlg+ "' AND "
                                     + "W2.GRADE||W2.HR_CLASS = '" + param._gradeHrclass + "' AND "
                                     + "W2.SCHREGNO = W1.SCHREGNO )  AND "
                       //+  param._4 + " IS NOT NULL";
                       +  param._fieldname + " IS NOT NULL";
        try{
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next())sch_cnt = rs.getInt(1);
        } catch (Exception ex) {
            log.fatal("[KNJD160K]int Get_Sch_Cnt()  get count1 error!", ex );
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return sch_cnt;

    }//private int Get_Sch_Cnt()


    /**
      *  全科目数を取得
      *  2005/03/09Modify
      **/
    private int Get_Subj_Cnt(DB2UDB db2,Param param) {

        PreparedStatement ps = null;
        ResultSet rs = null;
        int subj_cnt = 0;
        StringBuffer stb = new StringBuffer();
        stb.append( preStatCommon(param ));
        stb.append("SELECT COUNT(*) ");
        stb.append("FROM(  SELECT CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
        stb.append(       "FROM   KIN_RECORD_DAT W1 ");
        stb.append(       "WHERE  YEAR = '" + param._year + "' AND ");
        stb.append(              "EXISTS(SELECT 'X' FROM SCHNO W2 WHERE W2.SCHREGNO = W1.SCHREGNO) AND ");
        stb.append(               param._fieldname + " IS NOT NULL ");
        stb.append(       "GROUP BY CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
        stb.append(     ") S1 ");
        try{
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            if (rs.next()) subj_cnt = rs.getInt(1);
        } catch (Exception ex) {
            log.fatal("[KNJD160K]int Get_Subj_Cnt()  get count1 error!", ex );
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return subj_cnt;

    }//private int Get_Subj_Cnt()


    /**
      *
      *  SVF-FORM FIELDの初期化
      *
      **/
    private void svfFieldInit(Vrw32alp svf){

        for (int i = 0 ; i < maxStudent ; i++) {
            svf.VrAttribute("point" + (i+1) , ",Bold=0");
            svf.VrsOut("point" + (i+1) , "");
        }

    }


    /**
      *
      *  SVF-FORM FIELD属性変更
      *  subclasscdにより、科目名および得点を、明朝体・ゴシック体に切り替える
      *  parameter  subclasscd => 科目コード  但し"0"は全科目平均
      **/
    private void svfFieldAttribute(Vrw32alp svf, String subclasscd) {

        if (subclasscd != null) {
            if (subclasscd.equals("0")) {
                svf.VrAttribute("subject", "ZenFont=1");
                //for (int i = 1 ; i <= maxStudent ; i++ ) svf.VrAttribute("point" + i  , "ZenFont=1");
            } else{
                svf.VrAttribute("subject", "ZenFont=0");
                //for (int i = 1 ; i <= maxStudent ; i++ ) svf.VrAttribute("point" + i  , "ZenFont=0");
            }
        }

    }

    /**
      *
      *  SVF-FORM FIELD属性変更
      *  生徒の得点に網掛けを設定する
      *  parameter  ichi => 帳票の行番号  row => 生徒個人の順位位置
      **/
    private void svfAmikakeToPoint(Vrw32alp svf, int ichi, Integer row){
//log.debug("ichi="+ichi+"   row="+row);
        //帳票の行が生徒個人の順位位置に等しいなら、網掛けをする。
        if (null != row && ichi+1 == row.intValue()) {
            svf.VrAttribute("point"+(ichi+1) ,"Paint=(2,50,2),Bold=1");
        }
    }

    private static class Subclass {
        String _subclasscd;
        String _abbv;
        int _avg;
        List _scoreList;
        public Subclass(final String subclasscd, final String abbv, final int avg) {
            _subclasscd = subclasscd;
            _abbv = abbv;
            _avg = avg;
            _scoreList = new ArrayList();
        }
        public int getScore(final int n) {
            return ((Integer) _scoreList.get(n)).intValue();
        }
    }

    private static class Student {
        private static Student getStudent(final List list, final String schregno) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }

        String _schregno;
        String _name;
        String _attendno;
        List _subclasscdList;
        Map _subclassRowMap;
        public Student(final String schregno, final String name, final String attendno) {
            _schregno = schregno;
            _name = name;
            _attendno = attendno;
            _subclasscdList = new ArrayList();
            _subclassRowMap = new HashMap();
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _gradeHrclass;
        final String _schregnos;
        String _semeFlg;
        final String _test;
        final String _date;
        String _8;

        String _fieldname;               //成績名称

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");            //年度
            _semester = request.getParameter("SEMESTER");        //1-3:学期 9:学年末
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");  //学年
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));    //出欠集計日付 05/02/17

            //学籍番号の編集
            final String schno[] = request.getParameterValues("category_name");   // 学籍番号
            final StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int ia = 0; ia < schno.length; ia++) {
                if (ia > 0) stb.append(",");
                stb.append("'");
                stb.append(schno[ia]);
                stb.append("'");
            }
            stb.append(")");
            _schregnos = stb.toString();

            _test = request.getParameter("TEST");            //テスト種別 (中間・期末) 04/11/16Add

        }
    }

}//クラスの括り
