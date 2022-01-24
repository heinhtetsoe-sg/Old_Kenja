/**
http://tokio/serv_ktest/KNJD?DBNAME=KINHDB&PRGID=KNJD060&YEAR=2005&SEMESTER=3&CLASS_SELECTED=01J01&GRADE=01&DATE=2006/03/31&OUTPUT3=on&TESTKINDCD=0&SEME_FLG=3
http://ktest/servlet_kin/KNJD?DBNAME=KINH0916&PRGID=KNJD060&YEAR=2005&SEMESTER=1&CLASS_SELECTED=01J01&GRADE=01&DATE=2005/09/16&OUTPUT3=on&TESTKINDCD=02&SEME_FLG=1
http://ktest/servlet_kin/KNJD?DBNAME=H051101&PRGID=KNJD060&YEAR=2004&SEMESTER=2&CLASS_SELECTED=03J01&GRADE=03&DATE=2004/10/06&OUTPUT3=on&TESTKINDCD=01&SEME_FLG=2
*
 *  学校教育システム 賢者 [成績管理] 成績一覧
 *
 *  2004/12/06 yamashiro 学期末成績および学年成績の順位の不具合を修正 => KNJD060_GAKKI.classを修正
 *                       出欠集計範囲の出力を追加
 *  2004/12/14 yamashiro 出席日数の不具合を修正
 *  2005/01/04 yamashiro 科目別の欠課時数および出欠の記録において取得仕様を変更
 *  2005/01/31 yamashiro 履修単位、修得単位の取得仕様変更
 *  2005/02/01 yamashiro 科目別の欠課時数および出欠の記録において異動した生徒も出力する => (取り敢えず)合計からは除外
 *  2005/02/02 yamashiro 学年末において評価読替え科目の処理を追加
 *                       出席すべき日数から留学日数を除算する処理を追加
 *  2005/02/10 yamashiro KIN_RECORD_DATの試験成績、学期成績、学年成績の取得仕様を変更
 *  2005/02/25 yamashiro・最初の校時の忌引は遅刻にカウントしない、最後の校時の忌引は早退にカウントしない
 *  2005/03/01 yamashiro・評価読替え科目を学年末と同様に学期成績においても出力する
 *                      ・欠課記号を学期成績および学年末においても出力する（試験成績の欠課記号とは別仕様）
 *                      ・総合的な学習の時間の評定は相対評定マスターからアルファベットを出力する
 *  2005/03/03 yamashiro 総合学習の評定仕様変更により修正
 *  2005/03/04 yamashiro  一日遅刻・早退をカウントする際、遅刻・早退を出席としてカウントするように修正
 *  2005/03/09 yamashiro 評定読み替え科目の欠課が、異動者の場合出力されない不具合を修正
 *                       転学、退学において異動日は在籍とみなす
 *                       授業日数が０の科目は授業日数を出力しない（=>評価読み替え科目対応のため）
 *  2005/03/10 yamashiro 評定読替元には欠点マークは表示せず、読替科目に付ける
 *                       評価読替科目の評定は、読替元科目の成績の平均で評定マスターより取得する
 *  2005/03/16 yamashiro 最後の科目において科目別平均点および合計点が出力されない不具合を修正
 *  2005/05/22 yamashiro 科目名は略称を出力 <== 元は正式名称
 *                       欠課集計において講座出欠カウントフラグの参照を追加
 *                       作成日はシステム日付・時間を出力 <== 元は学籍処理日
 *                       科目数がページを超えた場合の不具合を修正
 *  2005/05/30 yamashiro 一括出力対応
 *                       現在学期を指示画面より取得 <= SEME_FLG
 *  2005/07/04 yamashiro 備考欄に「皆勤者」の出力を追加
 *  2005/09/07 yamashiro 学年成績において、留学期間中であるが「皆勤者」と表記される不具合を修正
 *                       学期成績・学年成績では、読替先科目の授業時数に読替元の合計を代入して出力
 *  2005/10/05 yamashiro 編入（データ仕様の変更による）について修正、および学籍異動に転学を追加
 *                       忌引または出停の日は遅刻および早退をカウントしない
 *                       留学・休学・不在日数の算出において、ATTEND_SEMES_DATもみる。
 *                           <change specification of 05/09/28>
 *  2005/11/11〜2005/11/25 yamashiro
 *                       残作業?221,223,224,225,226,227,228,229,230,241に対応
 *  2005/12/09 yamashiro 欠課時数集計におけるペナルティ欠課算出の不具合を修正
 *  2006/01/21 yamashiro [wise_man:02273] 高校の成績一覧表に於ける追試後の読替科目の成績における不具合を修正  --NO010
 *                         =>読替科目は読替元科目の平均を出力していたが、通知表と同様に読替科目の学年成績および評定を出力するように修正
 *                           追試入力処理では読替科目元は入力不可。また、学年成績以外は入力不可。（宮城さんより）
 *  2006/02/21 yamashiro ○出欠集計範囲の表記を修正 --NO100
 *                         =>欠課集計は、学期成績の場合も学年成績と同様に累計となっているため
 *                       ○学期成績の場合、欠課以外の出欠集計においても年度始めから累計を出力する --NO100
 *                       ○学年成績の欠課時数超過網掛けは評価読替元には除外する --NO100
 *  2006/03/07 yamashiro ○残作業?117の未対応について対応 --NO101
 *                       ○総合的な学習の時間の学年成績における欠課時数オーバーの不具合を修正 --NO102(KNJD060K_GRADE)
 *                       ○学年成績の欠課時数オーバー表示（網掛け）について学期成績の成績一覧表と同じ仕様で、読替元科目と読替先科目の両方伴に網掛けが必要 --NO102(KNJD060K_GRADE)
 *                       ○中間・期末は履修単位数は単位マスタから出力（欠課時数超過による引き算は行わない）--NO103(KNJD060K_INTER)
 *                       ○中間・期末のときは、履修単位は科目読替え元の単位を集計 --NO103(KNJD060K_INTER)
 *                       ○中間・期末のときは、科目読替え先の科目は表示しない --NO104
 *                       ○クラス単位の履修単位数算出において連続していないグループコードに対応 --NO105
 *  2006/03/08 yamashiro ○クラス単位の履修単位数は、個人の(欠課時数超過を引く前の)履修単位の最大値とする --NO106
 *  2006/03/10 yamashiro ○「ATTEND_SEMES_DATの授業日数は「留学・休学」を除外しない」に対応  NO101の修正
 */

package servletpack.KNJD;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.dao.AttendAccumulate;


public class KNJD060K_BASE {

    private Vrw32alp svf;                   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private Param param;

    private String fieldname;

    private static final Log log = LogFactory.getLog(KNJD060K_BASE.class);

    //KNJ_ClassCodeImp ccimp;                           //教科コード等定数設定 04/07/28ADD

    private String sqlStdName;
    private String sqlStdTotalRec;
    private String sqlSubclassInfo;
    private String sqlStdSubclassDetail;
    private String sqlAttendKaikin;

    private int FLG;
    private int KNJD060K_GAKKI = 1;
    private int KNJD060K_GRADE = 2;
    private int KNJD060K_INTER = 3;
    private int KNJD060K_TERM = 4;

    public boolean printSvf(final HttpServletRequest request, final DB2UDB db2, final Vrw32alp svf) {

        svf.VrSetForm("KNJD060K.frm", 4);
        boolean nonedata = false;
        this.svf = svf;
        param = null;
        try {
            param = getParam(db2, request);

            if (param._testkindcd.equals("01")) {
                if (param._semester.equals("3")) {
                    return false;                  //05/02/20Modify
                }
                FLG = KNJD060K_INTER;
            }else if (param._testkindcd.equals("02")) {
                FLG = KNJD060K_TERM;
            } else if (!param._semester.equals("9")) {
                FLG = KNJD060K_GAKKI;
            } else if (param._semester.equals("9")) {
                FLG = KNJD060K_GRADE;
            } else {
                return false;
            }
            set_head2();                                     //見出し出力のメソッド
            //クラスごとの印刷処理
            final String[] hrclass = request.getParameterValues("CLASS_SELECTED"); //学年・組  05/05/30
            for (int i = 0; i < hrclass.length; i++) {
                final HrClass hr = new HrClass(hrclass[i]);
                log.info("hrclass=" + hr._gradeHrclass);
                if (printSvfMainHrclass(db2, hr)) {
                    nonedata = true;
                }
            }
        } catch (Exception ex) {
            log.error("printSvf error!",ex);
        } finally {
            if (null != param) {
                DbUtils.closeQuietly(param.arrps0);
                DbUtils.closeQuietly(param.arrps1);
                DbUtils.closeQuietly(param.arrps2);
                DbUtils.closeQuietly(param.arrps3);
                DbUtils.closeQuietly(param.arrps4);
                DbUtils.closeQuietly(param.arrps5);
            }
        }
        return nonedata;
    }

    /**
     *  get parameter doGet()パラメータ受け取り
     *       2005/01/05 最終更新日付を追加(param._15)
     *       2005/05/22 学年・組を配列で受け取る
     */
    private Param getParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal(" $Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /**
     *  ページ見出し
     *  中間、期末、学期、学年末で設定
     */
    private void set_head2(){
        if (FLG == KNJD060K_INTER) {
            svf.VrsOut("TITLE" , param._semesterName + "  中間 成績一覧表");    //タイトル
            svf.VrsOut("MARK"  , "/"                     );           //表下部の文言の一部に使用
            fieldname = "SEM" + param._semester + "_INTER_REC";
            if (param._semeFlg == null) {
                param._semeFlg = param._semester;
            }
        } else if (FLG == KNJD060K_TERM) {
            svf.VrsOut("TITLE" , param._semesterName + "  期末 成績一覧表");    //タイトル
            svf.VrsOut("MARK"  , "/"                     );           //表下部の文言の一部に使用
            fieldname = "SEM" + param._semester + "_TERM_REC";
            if (param._semeFlg == null) {
                param._semeFlg = param._semester;
            }
        } else if (FLG == KNJD060K_GRADE) {
            //現在学期を取得
            if (param._semeFlg == null) {
                param._semeFlg = param._semester;
            }

            svf.VrsOut("TITLE",  "  成績一覧表（評定）");    //タイトル 05/01/29Modify
            svf.VrsOut("MARK",   "/"                     );              //表下部の文言の一部に使用

            //一覧表枠外の文言 04/12/02追加
            svf.VrAttribute("NOTE1",  "Paint=(1,70,1),Bold=1");
            svf.VrsOut("NOTE1",  " " );
            if (param._grade.equals("03")) {  //05/05/22
                svf.VrsOut("NOTE2",  "：単位数×８時間＋１以上の者" );
            } else{
                svf.VrsOut("NOTE2",  "：単位数×１０時間＋１以上の者" );
            }

            fieldname = "GRADE_RECORD";
        } else if (FLG == KNJD060K_GAKKI) {
            svf.VrsOut("TITLE" , param._semesterName + "成績一覧表");     //タイトル
            svf.VrsOut("MARK"  , "/"                     );     //表下部の文言の一部に使用
            //３学期は期末試験と期末成績が同一データ！
            if (param._semester.equals("3")) {
                fieldname = "SEM3_TERM_REC";
            } else {
                fieldname = "SEM" + param._semester + "_REC";
            }
            if (param._semeFlg == null) {
                param._semeFlg = param._semester;
            }

            //一覧表枠外の文言を追加 --NO100
            svf.VrAttribute("NOTE1",  "Paint=(1,70,1),Bold=1");
            svf.VrsOut("NOTE1",  " ");
            if (param._grade.equals("03")) {  //05/05/22
                if (param._semeFlg.equals("1")) {
                    svf.VrsOut("NOTE2",  "：単位数×８時間×１／２＋１以上の者");
                } else {
                    svf.VrsOut("NOTE2",  "：単位数×８時間＋１以上の者");
                }
            } else{
                if (param._semeFlg.equals("1")) {
                    svf.VrsOut("NOTE2",  "：単位数×１０時間×１／３＋１以上の者");
                } else if (param._semeFlg.equals("2")) {
                    svf.VrsOut("NOTE2",  "：単位数×１０時間×２／３＋１以上の者");
                } else {
                    svf.VrsOut("NOTE2",  "：単位数×１０時間＋１以上の者");
                }
            }
        }
    }


    /**
     *
     * SVF-OUT 学級単位の印刷処理
     *    Stirng param[]について ==> param._3:学年・組
     */
    private boolean printSvfMainHrclass(final DB2UDB db2, final HrClass hr)
    {
        boolean nonedata = false;

        final List nokaikin = getAttendNoKaikin(db2, hr);                //非皆勤者をリストに保管
        final List studentList = getStudentList(db2, hr);
        int schno = 0;                          //行番号
        final List pageList = new ArrayList();
        List current = null;
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Map rs0 = (Map) it.next();
            final int attendno = Integer.parseInt(getString(rs0, "ATTENDNO"));
            schno = (attendno % 50 != 0) ? attendno % 50 : 50;
            if (null == current || schno == 1) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(rs0);
        }
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List list = (List) pageList.get(pi);
            String attendnoS = null;
            String attendnoE = null;
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Map rs0 = (Map) it.next();
                final int attendno = Integer.parseInt(getString(rs0, "ATTENDNO"));
                schno = (attendno % 50 != 0) ? attendno % 50 : 50;
                if (attendnoS == null) {
                    attendnoS = getString(rs0, "ATTENDNO");    //開始生徒
                }
                attendnoE = getString(rs0, "ATTENDNO");                        //終了生徒
                saveSchInfo(rs0, schno, hr, nokaikin);               //生徒情報の保存処理 2005/05/22
            }

            clearSvfField(hr);                         //生徒名等出力のメソッド

            final Map rs2Map = getSubclassDetailMap(db2, hr, attendnoS, attendnoE);

            final List rs1List = getSubclassInfoList(db2, hr);

            if (printSvfStdDetail(db2, hr, rs2Map, rs1List)) {
                nonedata = true;           //成績・評定・欠課出力のメソッド
            }
            hr.hm1.clear();                           //行番号情報を削除
            schno = 0;
        }
        return nonedata;
    }

    private List getStudentList(final DB2UDB db2, final HrClass hr) {
        ResultSet rs = null;

        //学級単位の印刷処理
        final List studentList = new ArrayList();
        try {
            if (null == param.arrps0) {
                sqlStdName = getStdNameSql();
                param.arrps0 = db2.prepareStatement(sqlStdName);        //学籍データ
            }
            int pp = 0;
            param.arrps0.setString( ++pp,  hr._gradeHrclass );                      //学年・組
            rs = param.arrps0.executeQuery();                         //生徒名の表
            while (rs.next()) {
                studentList.add(createMap(rs));
            }
        } catch (Exception ex) {
            log.warn("ResultSet-read error!",ex);
        } finally{
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return studentList;
    }


    /**
     *  生徒情報の保存処理
     *    2005/05/22
     */
    private void saveSchInfo(final Map rs, final int schno, final HrClass hr, final List nokaikin)
    {
        final Student student = new Student(schno, getString(rs, "SCHREGNO"));
        hr.hm1.put(getString(rs, "SCHREGNO"), student);    //行番号に学籍番号を付ける
        student._name = getString(rs, "NAME");                 //生徒名

        //  文言をセットする。（除籍日付＋'付'＋除籍区分名称）04/11/08Add
        final StringBuffer stb = new StringBuffer();
        if (getString(rs, "KBN_DATE1") != null) {
            stb.append(KNJ_EditDate.h_format_JP(getString(rs, "KBN_DATE1")));
            if (getString(rs, "KBN_NAME1") != null) {
                stb.append(getString(rs, "KBN_NAME1"));
            }
        } else if (getString(rs, "KBN_DATE2") != null) {
            stb.append(KNJ_EditDate.h_format_JP(getString(rs, "KBN_DATE2")));
            if (getString(rs, "KBN_NAME2") != null) {
                stb.append(getString(rs, "KBN_NAME2"));
            }
        } else if (!nokaikin.contains(getString(rs, "SCHREGNO"))) {
            //「皆勤者」出力 05/07/04Build
            stb.append("皆勤者");
        }
        if (stb.length() > 0) {
            student._biko = stb.toString();
        }

    }


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

    private static String getString(final Map m, final String key) {
        if (!m.containsKey(key)) {
            throw new IllegalStateException("フィールドがありません: key = "+ key + ", data = " + m);
        }
        return (String) m.get(key);
    }

    private static int getInt(final Map m, final String key) {
        return getInt(getString(m, key));
    }

    private static int getInt(final String val) {
        return Integer.parseInt(StringUtils.defaultString(val, "0"));
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

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    /**
     *  明細出力処理
     *    科目別明細(成績・評定・欠課)(SQL=>prestatStdSubclassDetail)を読込み出力する
     *    科目名の変わり目で科目情報(教科名・科目名・単位数・授業時数)(SQL=>prestatSubclassInfo)を出力する()
     *    最後に生徒別総合＆合計(SQL=>printSvfStdTotalRec,printSvfStdTotalAttend)を出力する
     *      2005/01/04 SQL変更に伴い修正
     */
    private boolean printSvfStdDetail(final DB2UDB db2, final HrClass hr, final Map rs2Map, final List rs1List) {
        boolean nonedata = false;

        int subclasslinecount = 0;          //科目列カウント
        try {
            int d065Cnt = 0;
            for (int i = 0; i < rs1List.size(); i++) {
                final SubclassInfo rs1 = (SubclassInfo) rs1List.get(i);

                if (rs1._d065) {
                    d065Cnt++;
                }
            }

            int d065LineCnt = 19 - d065Cnt;
            boolean d065karaume = false;
            for (int i = 0; i < rs1List.size(); i++) {
                final SubclassInfo rs1 = (SubclassInfo) rs1List.get(i);

                if (19 <= subclasslinecount) {
                    clearSvfField(hr);
                    subclasslinecount = 0;
                }
                hr.subclasstotalnum.clear();  //05/03/09

                if (rs1._d065) {
                    if (!d065karaume) {
                        for (int j = subclasslinecount + 1; j < d065LineCnt; j++) {
                            svf.VrAttribute("course1", "Meido=100");
                            svf.VrsOut("course1", String.valueOf(j));
                            svf.VrEndRecord();
                        }
                        d065karaume = true;
                    }
                    subclasslinecount = d065LineCnt - 1;
                    d065LineCnt++;
                }

                if (StringUtils.split(rs1._subclasscd, "-")[0].equals("90")) {
                    if (!d065karaume) {
                        for (int j = subclasslinecount + 1; j < 19; j++) {
                            svf.VrAttribute("course1", "Meido=100");    //教科名を白文字設定
                            svf.VrsOut("course1", String.valueOf(j));    //教科名
                            svf.VrEndRecord();
                        }
                    }
                    subclasslinecount = 18;
                }
                printSvfSubclassInfoOut(rs1, subclasslinecount + 1);       //科目名等出力のメソッド

                for (final Iterator it = getMappedList(rs2Map, rs1._subclasscd).iterator(); it.hasNext();) {
                    final SubclassScore rs2 = (SubclassScore) it.next();
                    //明細データの出力 05/03/03Modify 総合学習を別処理とする
                    printSvfStdDetailOut(hr, rs2, rs1, subclasslinecount + 1); //総合学習以外
                }

                // 科目別平均・合計
                if (0 < hr.subclasstotalnum.size()) {
                    svf.VrsOutn("TOTAL_SUBCLASS", subclasslinecount + 1, String.valueOf(HrClass.sum(hr.subclasstotalnum)));    //ＨＲ合計 04/11/03Add 04/11/08Modify
                    svf.VrsOutn("AVE_CLASS",      subclasslinecount + 1, String.valueOf(HrClass.avg(hr.subclasstotalnum))); //ＨＲ平均             04/11/08Modify
                }
                if (i == rs1List.size() - 1) {
                    printSvfStdTotalRec(hr, db2);
                    printSvfStdTotalAttend(hr, db2);
                    printSvfTotalOut(hr);
                }
                svf.VrEndRecord();
                subclasslinecount++;
                nonedata = true;
            }

        } catch (Exception ex) {
            log.warn("ResultSet-read error!",ex);
        }

        return nonedata;

    }//boolean printSvfStdDetail()の括り

    private List getSubclassInfoList(final DB2UDB db2, final HrClass hr) {
        final List rs1List = new ArrayList();
        ResultSet rrs1 = null;
        try {
            if (null == param.arrps2) {
                sqlSubclassInfo = getSubclassInfoSql();
                param.arrps2 = db2.prepareStatement(sqlSubclassInfo);        //科目名等データ
            }
            int p = 0;

            final PreparedStatement ps2 = param.arrps2;
            ps2.setString(++p, hr._gradeHrclass);              //学年・組
            ps2.setString(++p, hr._gradeHrclass);              //学年・組
            ps2.setDate(++p, java.sql.Date.valueOf(param._divideAttendDate));             //出欠データ集計開始日付 2005/01/04
            ps2.setDate(++p, java.sql.Date.valueOf(param._date));             //出欠データ集計終了日付 2005/01/04
            if (param.definecode.useschchrcountflg) {
                ps2.setString(++p, hr._gradeHrclass);          //学年・組 05/11/25 Build
            }
            ps2.setString(++p, param._divideAttendMonth);             //出欠集計データ終了学期＋月   2005/01/04
            ps2.setString(++p, hr._gradeHrclass );              //学年・組
            ps2.setString(++p, hr._gradeHrclass );              //学年・組
log.debug("prestatSubclassInfo executeQuery() start");
            rrs1 = ps2.executeQuery();                                   //科目名等のRecordSet
            while (rrs1.next()) {
                rs1List.add(new SubclassInfo(createMap(rrs1)));
            }
log.debug("prestatSubclassInfo executeQuery() end");
        } catch (Exception ex) {
            log.warn("ResultSet-read error!", ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rrs1);
        }
        return rs1List;
    }

    private Map getSubclassDetailMap(final DB2UDB db2, final HrClass hr, final String attendnoS, final String attendnoE) {
        final Map rs2Map = new HashMap();
        ResultSet rrs2 = null;
        try {
            if (null == param.arrps3) {
                sqlStdSubclassDetail = getStdSubclassDetailSql();
                log.debug(" prestatStdSubclassDetailSql = " + sqlStdSubclassDetail);
                param.arrps3 = db2.prepareStatement(sqlStdSubclassDetail);        //明細データ
            }
            int p = 0;
            final PreparedStatement ps3 = param.arrps3;
            ps3.setString(++p, hr._gradeHrclass);     //学年・組
            ps3.setString(++p, attendnoS);        //生徒番号
            ps3.setString(++p, attendnoE);     //生徒番号
            ps3.setString(++p, hr._gradeHrclass);     //学年・組
            ps3.setDate(++p, java.sql.Date.valueOf(param._divideAttendDate));     //出欠データ集計開始日付 2005/01/04
            ps3.setDate(++p, java.sql.Date.valueOf(param._date));     //出欠データ集計終了日付 2005/01/04
            if (param.definecode.useschchrcountflg) {
                ps3.setString(++p, hr._gradeHrclass); //学年・組
            }
            ps3.setString(++p, param._divideAttendMonth);     //出欠集計データ終了学期＋月   2005/01/04
            if (! param._semester.equals("9")) {
                ps3.setString(++p, hr._gradeHrclass); //学年・組
            }


log.debug("prestatStdSubclassDetail executeQuery() start");
            rrs2 = ps3.executeQuery();                   //明細データのRecordSet
            while (rrs2.next()) {
                getMappedList(rs2Map, rrs2.getString("SUBCLASSCD")).add(new SubclassScore(param, createMap(rrs2)));
            }
log.debug("prestatStdSubclassDetail executeQuery() end");
        } catch (Exception ex) {
            log.warn("ResultSet-read error!", ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rrs2);
        }
        return rs2Map;
    }


    /**
     *  科目名等出力
     */
    private void printSvfSubclassInfoOut(final SubclassInfo rs, final int linex) {

        boolean amikake = false;
        svf.VrsOut("course1" , rs._classabbv);              //教科名

        if (rs._electdiv != null && !rs._electdiv.equals("0")) {
            amikake = true;
        }
        if (amikake) {
            svf.VrAttributen("subject1", linex, "Paint=(2,70,2),Bold=1"); //網掛け
        }
        svf.VrsOutn("subject1", linex, rs._subclassname); //科目名

        if (rs._credits != null) {
            svf.VrsOutn("credit1",  linex, rs._credits);  //単位数
        }
        if (rs._jisu != null && 0 < Integer.parseInt( rs._jisu)) { //05/03/09
            svf.VrsOutn("lesson1",  linex, rs._jisu);         //授業時数
        }
        //類型グループ平均ではなく類型を出力 04/10/17Modofy
        if (rs._assessGr != null) {
            svf.VrsOutn("AVE_GRP", linex, rs._assessGr);    //類型
        }
        if (amikake) {
            svf.VrAttributen("subject1", linex, "Paint=(0,0,0),Bold=0");  //網掛け
        }

    }//printSvfSubclassInfoOut()の括り

    /**
     *  科目別および全科目の平均・合計累積処理
     *      学期および学年成績において ( param._11=='0' ) 評定読替科目は含めない
                                                            (SQLのSELECTで'REPLACEMOTO'がマイナスなら評定読替科目)
     *  2005/03/09
     */
    private void subclasscnt_num(final HrClass hr, final SubclassScore rs) {
        if (rs._score != null         &&
                ! rs._score.equals("KK")  &&
                ! rs._score.equals("KS")  &&
                ! rs._score.equals("( )") &&
                ! rs._score.equals("欠")  &&
                ! rs._score.equals("公")     ) {

            hr.subclasstotalnum.add(rs._score);

            if (param._testkindcd.equals("0")) {
                if (0 <= getInt(rs._replaceMoto)) {
                    hr.hrtotalnum.add(rs._score);
                }
            } else {
                hr.hrtotalnum.add(rs._score);
            }
        }
    }


//    /**
//     *  2005/01/31 履修単位を加算する処理
//     *     科目欄の単位数の合計をクラス履修単位とする
//     *        => 体育等の扱いを考慮し取り敢えず群コードが０または連続しない群コードの単位を集計する
//     */
//    void addSubclassCredits(final HrClass hr, final Map rs, final int group, final int replaceflg) {    //05/11/24 Modify
//    //void addSubclassCredits( ResultSet rs, int group) {
//        if (FLG == KNJD060K_INTER || FLG == KNJD060K_TERM) {
//            // NO103 Build 中間・期末成績では　履修単位＝通常科目＋読替前科目
//            if (getString(rs, "CREDITS") == null) {
//                return;
//            }
//            if (-1 == replaceflg) {
//                return;
//            }
//
//            if (Integer.parseInt(getString(rs, "GROUPCD")) == 0) {
//                hr.hr_credits += Integer.parseInt(getString(rs, "CREDITS"));  //単位数
//            } else if (Integer.parseInt(getString(rs, "GROUPCD")) != group) {
//                hr.hr_credits += Integer.parseInt(getString(rs, "CREDITS"));  //単位数
//            }
//        } else {
//            log.debug("kamokuname="+getString(rs, "SUBCLASSNAME") + "  groupcd="+getString(rs, "GROUPCD")+"  credits="+getString(rs, "CREDITS")+"  replaceflg="+replaceflg);
//            if (getString(rs, "CREDITS") == null) {
//                return;
//            }
//            if (0 < replaceflg) {
//                return;  //05/11/24 Build
//            }
//
//            if (Integer.parseInt( getString(rs, "GROUPCD")) == 0) {
//                hr.hr_credits += Integer.parseInt( getString(rs, "CREDITS"));  //単位数
//            } else if (Integer.parseInt( getString(rs, "GROUPCD")) != group) {
//                hr.hr_credits += Integer.parseInt( getString(rs, "CREDITS"));  //単位数
//            }
//            log.debug("hr_credits="+hr.hr_credits);
//        }
//    }


    /**
     *  明細出力
     *  生徒の科目別成績、評定、欠課を出力する
     */
    private void printSvfStdDetailOut(final HrClass hr, final SubclassScore rs, final SubclassInfo si, final int linex) {
        //学籍番号をKEYにMap.hmmより出力行を取得
        final Student student = (Student) hr.hm1.get(rs._schregno); // 出力行
        if (student == null) {
            return;
        }
        final int ii = student._schno;
        if (FLG == KNJD060K_INTER || FLG == KNJD060K_TERM) {
            if (rs._subclasscd.substring(0,2).equals("90") || rs._d065) {
                // 総合学習明細出力
                //２通りの欠課時数(年度内通算と学期内通算)を持つため、学期内通算のフィールドを'ABSENT2'に変更 05/11/24 Modify
                if (rs._absent2 != null  &&  Integer.parseInt(rs._absent2) != 0) {
                    svf.VrsOutn("kekka" + ii, linex, rs._absent2);   //欠課
                }
                //履修単位の累積(<= 中間・期末成績にも適用): 05/11/24
                accumuStdRCredits(hr, rs, student, si);
            } else {
                if (rs._score != null) {
                    if (param._output4.equals("1")  &&  rs._patternAssess != null  &&  rs._patternAssess.equals("1")) {
                        svf.VrsOutn("rate"+ii , linex , "*" + rs._score);
                        subclasscnt_num(hr, rs);    //05/03/09
                    } else if (rs._scoreFlg != null  &&  0 < Integer.parseInt(rs._scoreFlg)) {
                        svf.VrsOutn("rate"+ii , linex , rs._score);  // 05/02/17Modify
                        svf.VrsOutn("MARK"+ii , linex , "/");                  // 05/02/17Modify
                        subclasscnt_num(hr, rs);    //05/03/09
                    } else if (rs._score.equals("KK")) {
                        svf.VrsOutn("rate"+ii , linex , "公");
                    } else if (rs._score.equals("KS")) {
                        svf.VrsOutn("rate"+ii , linex , "欠");
                    } else{
                        svf.VrsOutn("rate"+ii , linex , rs._score);
                        subclasscnt_num(hr, rs);    //05/03/09
                    }
                }

                //２通りの欠課時数(年度内通算と学期内通算)を持つため、学期内通算のフィールドを'ABSENT2'に変更 05/11/24 Modify
                if (rs._absent2 != null  &&  Integer.parseInt(rs._absent2) != 0) {
                    svf.VrsOutn("kekka" + ii, linex, rs._absent2);   //欠課
                }
                //履修単位の累積(<= 中間・期末成績にも適用): 05/11/24
                boolean rflg = true;
                if (si.replaceflgSaki()) {
                    rflg = false;
                }

                if (rflg) {
                    getMappedList(student.sch_rcredits, si._subclasscd).add(String.valueOf(si.subclassjisu)); //履修単位
                    getMappedList(student.sch_rcredits_hr, si._subclasscd).add(String.valueOf(si.subclassjisu)); //履修単位
                }
            }
        } else if (FLG == KNJD060K_GRADE) {
            //欠課時数の印刷を別メソッドとする( <= 網掛け処理を学期成績にも適用するため ): 05/11/16
            final boolean amikake = printSvfStdDetailKekkaGrade( rs, ii, linex, si);  //NO102 欠課時数要注意または超過
            if (rs._subclasscd.substring(0,2).equals("90") || rs._d065) {
                // 総合学習明細出力

                if (!amikake && rs._patternAssess != null) {
                    svf.VrsOutn("late" + ii, linex , rs._patternAssess);       //評定
                }

                //05/01/31 履修単位の加算処理  履修単位：欠課が授業時数の１／３を超えない場合
                if (! amikake) {
                    getMappedList(student.sch_rcredits, si._subclasscd).add(String.valueOf(si.subclassjisu)); //履修単位の加算処理
                }
                getMappedList(student.sch_rcredits_hr, si._subclasscd).add(String.valueOf(si.subclassjisu)); //履修単位の加算処理

                //05/01/31 修得単位の加算処理   修得単位：欠課が授業時数の１／３を超えず、評定が１でない場合
                //05/11/24 Modify
                if (! amikake  &&  rs._patternAssess != null) {
                    getMappedList(student.sch_ccredits, si._subclasscd).add(String.valueOf(si.subclassjisu)); //修得単位の加算処理
                }
            } else {
                //履修単位の累積処理をココへ移動 05/09/07 Modify
                //履修単位の累積を別メソッドとする(<= 中間・期末・学期成績にも適用するため): 05/11/16 Modify
                accumuStdRCredits(hr, rs, student, si);

                if (rs._score != null) {
                    svf.VrsOutn("rate" + ii, linex, rs._score); //成績
                    subclasscnt_num(hr, rs);    //05/03/09
                    if (! rs._score.equals("欠")  &&
                        ! rs._score.equals("公")  &&                 //05/03/10Modify
                          getInt(rs._replaceMoto) <= 0 &&                       //05/03/10Modify
                                  rs._patternAssess != null) {               //05/03/01Modify
                        if (param._output4.equals("1")  &&  rs._patternAssess.equals("1")) {         //05/03/01Modify
                            svf.VrsOutn("late" + ii, linex, "*" + rs._patternAssess);   //評定
                        } else {
                            svf.VrsOutn("late" + ii, linex, rs._patternAssess);     //評定
                        }
                        if (amikake) {
                            svf.VrsOutn("late" + ii, linex, "");     //評定
                        }
                    }
                }
                //修得単位の加算処理 => 修得単位：欠課が授業時数の１／３を超えず、評定が１でない場合表記 05/01/31
                //修得単位の累積処理変更 05/09/07 Modify
                //05/11/24 Modify
                if (! amikake  &&
                    !si.replaceflgMoto() &&
                    rs._patternAssess != null         &&
                    0 < rs._patternAssess.length()    &&    //05/07/27Build
                    ! rs._patternAssess.equals("()")  &&    //05/07/27Build
                    ! rs._patternAssess.equals("1")
              ) {
                    getMappedList(student.sch_ccredits, si._subclasscd).add(String.valueOf(si.subclassjisu)); //修得単位の加算処理
                }
            }
        } else if (FLG == KNJD060K_GAKKI) {
            if (rs._subclasscd.substring(0,2).equals("90") || rs._d065) {
                // 総合学習明細出力
                //履修単位の累積(<= 学期成績にも適用): 05/11/24
                accumuStdRCredits(hr, rs, student, si);

                //欠課時数の網掛け処理を学期成績にも適用: 05/11/16
                printSvfStdDetailKekka(rs, ii, linex, si);
            } else {
                printSvfStdDetailKekka(rs, ii, linex, si);

                if (rs._score != null) {
                    //学期成績には評定を出力しない。従って保留者マークは成績欄へ表記。 05/11/21 Modify
                    if (param._output4.equals("1")  &&  rs._patternAssess != null  &&  rs._patternAssess.equals("1")) {
                        svf.VrsOutn("rate" + ii, linex, "*" + rs._score);
                    } else if (rs._scoreFlg != null  &&  0 < Integer.parseInt(rs._scoreFlg)) { //補充・補点の印付け 05/02/17 Modify
                        svf.VrsOutn("MARK" + ii, linex, "/");                     // 05/02/17Modify
                        svf.VrsOutn("rate" + ii, linex, rs._score);
                    } else {
                        svf.VrsOutn("rate" + ii, linex, rs._score);
                    }

                    subclasscnt_num(hr, rs);    //科目別および全科目の平均・合計累積処理 05/03/09

                    //３年生以外の学期成績の評定は出力しない 05/09/07Modify
                    //学期成績には評定を出力しない。 05/11/21 Modify
                    if (Integer.parseInt(param._semester) == 9 )  //05/11/21 Modify
                        if (! rs._score.equals("欠")  &&
                            ! rs._score.equals("公")  &&
                              getInt(rs._replaceMoto) <= 0       &&               //05/03/10Modify
                                      rs._patternAssess != null) {             //05/03/01Modify
                                svf.VrsOutn("late"+ii, linex, rs._patternAssess);       //評定
                        }
                }
                //履修単位の累積(<= 学期成績にも適用): 05/11/24
                accumuStdRCredits(hr, rs, student, si);
            }
        }
    }

    /**
     *  欠課時数の印刷: 欠課時数要注意者または超過者は'true'を返す
     *  2005/11/16 Build 欠課時数の網掛け処理を学期成績にも適用する(学年成績のみであったが)ため処理を独立
     *  引数について
     *      int i: 出力行 / int j: 出力列 / int subclassjisu: 科目別単位数
     */
    private boolean printSvfStdDetailKekkaGrade(final SubclassScore rs, final int i, final int j, final SubclassInfo si)  //NO100
    {
        boolean amikake = false;
        //欠課出力/網掛け設定/履修単位の加算処理
        if (rs._absent1 != null) {
                if (Integer.parseInt( param._grade ) == 3) {   //05/05/22
                    //３年生の欠課時数網掛け設定
                    if (getInt(rs._absent1) >= si.subclassjisu * 8 + 1) {
                        amikake = true;
                    }
                }else{
                    //1・２年生の欠課時数網掛け設定
                    if (getInt(rs._absent1) >= si.subclassjisu * 10 + 1) {
                        amikake = true;
                    }
                }
                if (amikake) {
                    svf.VrAttributen( "kekka" + i, j, "Paint=(2,70,1),Bold=1" );
                }
            if (Integer.parseInt( rs._absent1) != 0) {
                svf.VrsOutn( "kekka" + i, j, rs._absent1); //欠課
            }
        }

        //網掛け解除
        if (amikake) {
            svf.VrAttributen("kekka" + i, j, "Paint=(0,0,0),Bold=0" );
        }
        return amikake;
    }

    /**
     *  累計出力処理
     */
    private void printSvfStdTotalRec(final HrClass hr, final DB2UDB db2)
    {
        ResultSet rrs = null;

        try {
            if (null == param.arrps1) {
                sqlStdTotalRec = getStdTotalRecSql();
                param.arrps1 = db2.prepareStatement(sqlStdTotalRec);        //成績累計データ
            }
            int pp = 0;
            param.arrps1.setString(++pp,  hr._gradeHrclass);                      //学年・組
log.debug("prestatStdTotalRec executeQuery() start");
            rrs = param.arrps1.executeQuery();                               //成績累計データのRecordSet
log.debug("prestatStdTotalRec executeQuery() end");

            while (rrs.next()) {
                //明細データの出力
                final Map rs = createMap(rrs);

                final Student student = (Student) hr.hm1.get(getString(rs, "SCHREGNO"));
                if (student == null) {
                    continue;
                }
                student.sumRec = getString(rs, "SUM_REC");
                student.avgRec = getString(rs, "AVG_REC");
                student.rank = getString(rs, "RANK");
            }
        } catch (Exception ex) {
            log.warn("ResultSet-read error!", ex);
        } finally{
            db2.commit();
            DbUtils.closeQuietly(rrs);
        }

        for (final Iterator it = hr.hm1.values().iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            printSvfStdTotalRecOut(hr, student);
        }

    }//printSvfStdTotalRec()の括り



    /**
     *  累計出力処理
     *      2005/01/04 SQL変更に伴い修正
     */
    private void printSvfStdTotalAttend(final HrClass hr, final DB2UDB db2)
    {
        ResultSet rrs = null;

        try {
            if (null == param.arrps4) {
                final Map paramMap = new HashMap();
                paramMap.put("useCurriculumcd", param._useCurriculumcd);
                paramMap.put("useVirus", param._useVirus);
                paramMap.put("useKoudome", param._useKoudome);
                paramMap.put("DB2UDB", db2);
                final String prestatStdTotalAttendSql = AttendAccumulate.getAttendSemesSql(
                        param._semesFlg,
                        param.definecode,
                        param._knjSchoolMst,
                        param._year,
                        param.SSEMESTER,
                        param._semester,
                        (String) param._hasuuMap.get("attendSemesInState"),
                        param._periodInState,
                        (String) param._hasuuMap.get("befDayFrom"),
                        (String) param._hasuuMap.get("befDayTo"),
                        (String) param._hasuuMap.get("aftDayFrom"),
                        (String) param._hasuuMap.get("aftDayTo"),
                        param._grade,
                        "?",
                        null,
                        "SEMESTER",
                        paramMap
                );
                log.debug("prestatStdTotalAttendSql = " + prestatStdTotalAttendSql);
                param.arrps4 = db2.prepareStatement(prestatStdTotalAttendSql); // 出欠累計データ
            }
            int p = 0;
            param.arrps4.setString(++p, hr._gradeHrclass.substring(2, 5));        //組

log.trace("prestatStdTotalAttend executeQuery() start ");
rrs = param.arrps4.executeQuery();                               //出欠累計データのRecordSet
log.trace("prestatStdTotalAttend executeQuery() end ");

            while (rrs.next()) {
                final Map rs = createMap(rrs);
                if (!param._semester.equals(getString(rs, "SEMESTER"))) {
                    continue;
                }
                final Student student = (Student) hr.hm1.get(getString(rs, "SCHREGNO"));        //学籍番号に対応した行にデータをセットする。
                if (student == null) {
                    continue;
                }

                //明細データの出力
                printSvfStdTotalAttendOut(hr, student._schno, rs);
            }
        } catch (Exception ex) {
            log.warn("ResultSet-read error!", ex);
        } finally{
            db2.commit();
            DbUtils.closeQuietly(rrs);
        }

    }//printSvfStdTotalAttend()の括り


    /**
     *  明細出力
     *  2005/05/24 Modify 履修単位数と修得単位数の表記仕様変更
     */
    private void printSvfStdTotalRecOut(final HrClass hr, final Student student)
    {
        final int ii = student._schno;
        if (student.sumRec != null) {
            svf.VrsOutn("TOTAL1", ii, student.sumRec);         //総合点
            hr.hr_total += getInt(student.sumRec);
            hr.hr_seitosu[8]++;
        }

        if (student.avgRec != null) {
            svf.VrsOutn("AVERAGE1", ii, String.valueOf(Math.round(Float.parseFloat(student.avgRec))));    //平均点 04/11/08Modify
        }

        if (student.rank != null) {
            svf.VrsOutn("RANK1", ii, student.rank);             //順位
        }

        //履修単位の(表記)処理を止めておく 04/12/16 Modify
        //学年末の処理で、３年生は2学期、以外の学年は3学期に出力 05/01/31 Modify
        //履修単位の表記を中間・期末・学期成績にも適用 05/11/24 Modify
        final int sch_rcredits = Student.sum(student.sch_rcredits);
        if (0 < sch_rcredits) {  //05/11/24 Modify
        //05/11/24 Delete if (param._1.equals("9")  &&  0 < sch_rcredits[ int1.intValue() ] )
            svf.VrsOutn("R_CREDIT1", ii, String.valueOf(sch_rcredits));   //履修単位数
        }

        //修得単位数の表記
        //履修単位数の表記を現在学期で制限 => １年生と２年生は３学期、３年生は２学期と３学期とする 05/11/24 Modify
        if (param._semester.equals("9")  &&  0 < sch_rcredits) {
            if (((hr._gradeHrclass.substring( 0,2 ).equals("03")) ? 1 : 2) < Integer.parseInt( param._semeFlg)) { //05/11/24 Modify
                //log.debug(" student = " + student._schregno + " / " + student._schno + ", sch_ccredits  = " + student.sch_ccredits);
                svf.VrsOutn("C_CREDIT1", ii, String.valueOf(Student.sum(student.sch_ccredits))); //修得単位数
            }
        }
    }//printSvfStdTotalRecOut()の括り



    /**
     *  出欠の記録 明細出力
     *      2005/01/04 SQL変更に伴い修正
     *      2005/02/01 出欠の記録について異動した生徒も出力する。(取り敢えず)合計からは除外。
     */
    private void printSvfStdTotalAttendOut(final HrClass hr, final int ii, final Map rs)
    {
        try {
            if (getString(rs, "LESSON") != null  &&  getInt(rs, "LESSON") != 0) {
                if (hr.hr_lesson[0] < getInt(rs, "LESSON")) {
                    hr.hr_lesson[0] = getInt(rs, "LESSON");                 //最大授業日数 04/11/01Add
                }
                if (hr.hr_lesson[1] == 0) {
                    hr.hr_lesson[1] = getInt(rs, "LESSON");
                } else if (getInt(rs, "LESSON") < hr.hr_lesson[1]) {
                    hr.hr_lesson[1] = getInt(rs, "LESSON");                 //最小授業日数 04/11/01Add
                }
            }

            if (getString(rs, "SUSPEND") != null) {
                if (getInt(rs, "SUSPEND") != 0) {
                    svf.VrsOutn("SUSPEND1", ii, getString(rs, "SUSPEND")); //出停
                    hr.hr_attend[0] += getInt(rs, "SUSPEND");         //合計 05/02/01Modify
                }
                hr.hr_seitosu[0]++;                                  //生徒数 05/02/01Modify
            }

            if (getString(rs, "MOURNING") != null) {
                if (getInt(rs, "MOURNING") != 0) {
                    svf.VrsOutn("KIBIKI1", ii, getString(rs, "MOURNING")); //忌引
                    hr.hr_attend[1] += getInt(rs, "MOURNING");        //合計 05/02/01Modify
                }
                hr.hr_seitosu[1]++;                                  //生徒数 05/02/01Modify
            }

            if (getString(rs, "MLESSON") != null) {
                if (getInt(rs, "MLESSON") != 0) {
                    svf.VrsOutn("PRESENT1", ii, getString(rs, "MLESSON")); //出席すべき日数
                    hr.hr_attend[3] += getInt(rs, "MLESSON");         //合計 05/02/01Modify
                }
                hr.hr_seitosu[3]++;                                  //生徒数 05/02/01Modify
            }

            if (getString(rs, "SICK") != null) {
                if (getInt(rs, "SICK") != 0) {
                    svf.VrsOutn("ABSENCE1", ii, getString(rs, "SICK"));    //欠席日数
                    hr.hr_attend[4] += getInt(rs, "SICK");          //合計 05/02/01Modify
                }
                hr.hr_seitosu[4]++;                                  //生徒数 05/02/01Modify
            }

            if (getString(rs, "PRESENT") != null) {
                if (getInt(rs, "PRESENT") != 0) {
                    svf.VrsOutn("ATTEND1", ii, getString(rs, "PRESENT"));  //出席日数
                    hr.hr_attend[5] += getInt(rs, "PRESENT");         //合計 05/02/01Modify
                }
                hr.hr_seitosu[5]++;                                  //生徒数 05/02/01Modify
            }

            if (getString(rs, "EARLY") != null) {
                if (getInt(rs, "EARLY") != 0) {
                    svf.VrsOutn("LEAVE1", ii, getString(rs, "EARLY"));     //早退回数
                    hr.hr_attend[6] += getInt(rs, "EARLY");           //合計 05/02/01Modify
                }
                hr.hr_seitosu[6]++;                                  //生徒数 05/02/01Modify
            }

            if (getString(rs, "LATE") != null) {
                if (getInt(rs, "LATE") != 0) {
                    svf.VrsOutn("TOTAL_LATE1", ii, getString(rs, "LATE")); //遅刻回数
                    hr.hr_attend[7] += getInt(rs, "LATE");            //合計 05/02/01Modify
                }
                hr.hr_seitosu[7]++;                                  //生徒数 05/02/01Modify
            }

            if (getString(rs, "TRANSFER_DATE") != null) {  // 05/02/02ココへ移動
                if (getInt(rs, "TRANSFER_DATE") != 0) {
                    svf.VrsOutn("ABROAD1", ii, getString(rs, "TRANSFER_DATE"));  //留学日数
                    hr.hr_attend[2] += getInt(rs, "TRANSFER_DATE");
                }
                hr.hr_seitosu[2]++;
            }

        } catch (Exception ex) {
            log.warn("total svf-out error!", ex);
        }

    }//printSvfStdTotalAttendOut()の括り



    /**
     *  クラス総合・平均を出力
     *      総合点から遅刻回数
     */
    private void printSvfTotalOut(final HrClass hr)
    {
        try {
            if (0 < hr.hr_seitosu[8]) {
                svf.VrsOutn("TOTAL1",    51, String.valueOf(Math.round((float) hr.hr_total / hr.hr_seitosu[8])));    //総合点 04/11/10Modify => 小数点第１位で四捨五入
            }

            if (0 < hr.hrtotalnum.size()) {
                svf.VrsOutn("AVERAGE1", 51, String.valueOf(HrClass.avg(hr.hrtotalnum)));       //平均点 04/11/04Modify 04/11/08Modify 05/03/10Moidfy
                svf.VrsOutn("TOTAL1",   53, String.valueOf(HrClass.sum(hr.hrtotalnum)));    //総合点 04/11/08Add 05/03/10Modify
            }

            if (0 < hr.hr_seitosu[0]) svf.VrsOutn("SUSPEND1", 53, String.valueOf(hr.hr_attend[0]));  //出停
            if (0 < hr.hr_seitosu[1]) svf.VrsOutn("KIBIKI1",  53, String.valueOf(hr.hr_attend[1]));  //忌引
            if (0 < hr.hr_seitosu[3]) svf.VrsOutn("PRESENT1", 53, String.valueOf(hr.hr_attend[3]));  //出席すべき日数
            if (0 < hr.hr_seitosu[4]) svf.VrsOutn("ABSENCE1", 53, String.valueOf(hr.hr_attend[4]));  //欠席日数
            if (0 < hr.hr_seitosu[5]) svf.VrsOutn("ATTEND1",  53, String.valueOf(hr.hr_attend[5]));  //出席日数
            if (0 < hr.hr_seitosu[6]) svf.VrsOutn("LEAVE1",   53, String.valueOf(hr.hr_attend[6]));  //早退回数
            if (0 < hr.hr_seitosu[7]) svf.VrsOutn("TOTAL_LATE1",    53, String.valueOf(hr.hr_attend[7]));    //遅刻回数

            if (0 < hr.hr_attend[3]) {
                svf.VrsOut( "PER_ATTEND",  String.valueOf((float) Math.round(((float) hr.hr_attend[5] / (float) hr.hr_attend[3]) * 1000 ) / 10));    //出席率
                svf.VrsOut( "PER_ABSENCE", String.valueOf((float) Math.round(((float) hr.hr_attend[4] / (float) hr.hr_attend[3]) * 1000 ) / 10));    //欠席率
            }

            //学年末の処理で単位の合計を出力 05/01/31Modify
            //中間・期末・学期も履修単位数を表記 05/11/24 Modify
            // クラス履修単位を取得
            for (final Iterator it = hr.hm1.values().iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final int sch_rcredits_hr = Student.sum(student.sch_rcredits_hr);
                if (0 < sch_rcredits_hr) {
                    hr.hr_credits = Math.max(hr.hr_credits, sch_rcredits_hr);
                }
            }
log.debug("hr_credits="+hr.hr_credits);
            svf.VrsOut( "credit20", hr.hr_credits + "単位" );

            // 05/01/31Modify  授業日数最大値を出力 => 出欠統計の仕様に合わせる
            if (0 < hr.hr_lesson[0]) {       //授業日数最大値があれば出力 04/11/01Add
                svf.VrsOut( "lesson20", hr.hr_lesson[0] + "日" );
            }

        } catch (Exception ex) {
            log.warn("group-average svf-out error!", ex);
        }

    }//printSvfTotalOut()の括り

    /**
     * SQLStatement作成 ＨＲクラス生徒名の表(生徒名) 04/11/08Modify
     *   SEMESTER_MATはparam._1で検索 => 学年末'9'有り
     *   SCHREG_REGD_DATはparam._13で検索 => 学年末はSCHREG_REGD_HDATの最大学期
     *   2005/09/07 Modify 学年成績の場合、学期は当学期をみる
     *   2005/10/05 Modify 編入（データ仕様の変更による）について修正、および転学を追加
     */
    private String getStdNameSql() {

        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT  W1.SCHREGNO,W1.ATTENDNO,W3.NAME,");
        stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");                                       //<change specification of 05/09/28>
        stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A003' AND S1.NAMECD2 = W4.GRD_DIV) ");  //<change specification of 05/09/28>
        stb.append(             "ELSE (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A002' AND S1.NAMECD2 = W4.ENT_DIV) END AS KBN_NAME1, ");             //<change specification of 05/09/28>
        stb.append(        "W5.TRANSFER_SDATE AS KBN_DATE2,");
        stb.append(        "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS KBN_NAME2 ");
        stb.append("FROM    SCHREG_REGD_DAT W1 ");
        stb.append("INNER   JOIN SEMESTER_MST    W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._year + "' ");  //05/09/07Modify    <change specification of 05/09/28>
        stb.append("INNER   JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
        stb.append("LEFT    JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
        stb.append(                               "AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < EDATE) ");   //<change specification of 05/09/28>
        stb.append(                                 "OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE >= SDATE)) "); //<change specification of 05/09/28>
        stb.append("LEFT    JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
        stb.append(                                  "AND (TRANSFERCD IN('1','2') AND CASE WHEN EDATE < '" + param._date + "' THEN EDATE ELSE '" + param._date + "' END BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE) ");  //<change specification of 05/09/28> 05/11/11Modify
        stb.append("WHERE   W1.YEAR = '" + param._year + "' ");
        stb.append(    "AND W1.GRADE||W1.HR_CLASS = ? ");       //05/05/30
        stb.append(    "AND W1.SEMESTER = '" + param._semeFlg + "' ");
        stb.append("ORDER BY W1.ATTENDNO");
        return stb.toString();

    }//prestatStdNameListの括り

    /**
     *  SQLStatement作成 ＨＲ履修科目の表(教科名・科目名・単位・授業時数・平均)
     *    2005/02/02 Modify 学年末では評価読替え科目を出力する
     *    2005/05/22 Modify 科目名の略称を表記
     *    2005/09/07 Modify 学期成績・学年成績では、読替先科目の授業時数に読替元の合計を代入して出力
     *    2005/10/05 Modify 編入（データ仕様の変更による）について修正、および転学を追加
     *    2005/11/15〜2005/11/25 Modify
     *                      メイン表に評価読替前または評価読替後のフラグを作成:'REPLACEFLG'
     *                      学期成績の授業時数は１学期からの累計とする(中間・期末は指定学期のみで変更なし)
     *                      評価読替後科目の講座は時間割作成時に作成されるのでSQLで(一時的に)作成しない仕様に変更
     */
    private String getSubclassInfoSql() {

        final StringBuffer stb = new StringBuffer();

        //学籍の表 04/11/08 Build
        stb.append("WITH SCHNO AS(");
        stb.append(    "SELECT  W1.YEAR,W1.SEMESTER,W1.SCHREGNO,W1.ATTENDNO,");
        stb.append(            "W1.GRADE,W1.HR_CLASS,W1.COURSECD,W1.MAJORCD,W1.COURSECODE ");
        stb.append(    "FROM    SCHREG_REGD_DAT W1 ");
        stb.append(    "INNER   JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._year + "' ");  //<change specification of 05/09/28>
        stb.append(    "WHERE   W1.YEAR = '" + param._year + "' ");
        stb.append(        "AND W1.SEMESTER = '" + param._semeFlg + "' ");
        stb.append(        "AND W1.GRADE||W1.HR_CLASS = ? ");  //05/05/30
        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                       "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
        stb.append(                           "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END) ");     //<change specification of 05/09/28>
        stb.append(                             "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END))) ");  //<change specification of 05/09/28>
        //05/03/09Modify 留学開始日が３学期の場合は成績も出力する
        stb.append(        "AND NOT EXISTS( SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append(                        "WHERE  S1.SCHREGNO = W1.SCHREGNO ");
        stb.append(                          "AND ((S1.TRANSFERCD IN ('2') AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //<change specification of 05/09/28>
        stb.append(                            "OR (S1.TRANSFERCD IN ('1') AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE AND S1.TRANSFER_EDATE < '" + param._transferDate + "' ))) ");  //<change specification of 05/09/28>
        //stb.append( prestateCommonRegd());
        stb.append(     "),");

        stb.append(" TEST_COUNTFLG AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         '2' AS DATADIV ");
        stb.append("     FROM ");
        stb.append("         SCH_CHR_TEST T1, ");
        stb.append("         TESTITEM_MST_COUNTFLG T2 ");
        stb.append("     WHERE ");
        stb.append("         T2.YEAR       = T1.YEAR ");
        stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append("         AND T2.COUNTFLG   = '0' ");
        stb.append(" ), ");

        //講座の表 05/01/31Modify
        //中間・期末は指定学期のみ集計、学期・学年(変更なし)は指定学期まで年度内累計 05/11/22 Modify
        stb.append("CHAIR_A AS(");
        stb.append(     "SELECT  K2.CHAIRCD, ");
        stb.append(        " K2.CLASSCD || '-' || K2.SCHOOL_KIND AS CLASSCD, ");
        stb.append(        " K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
        stb.append(        " K2.SUBCLASSCD AS SUBCLASSCD, K2.SEMESTER,K2.GROUPCD ");
        stb.append(     "FROM    CHAIR_CLS_DAT K1,CHAIR_DAT K2 ");
        stb.append(     "WHERE   K1.YEAR = '" + param._year + "' ");
        stb.append(        "AND  K2.YEAR = '" + param._year + "' ");
        if (! param._testkindcd.equals("0")) {
            stb.append(    "AND  K1.SEMESTER = '" + param._semeFlg + "' ");
            stb.append(    "AND  K2.SEMESTER = '" + param._semeFlg + "' ");
        }
        stb.append(        "AND  K1.SEMESTER = K2.SEMESTER ");
        stb.append(        "AND  K1.TRGTGRADE||K1.TRGTCLASS = ? ");   //05/05/30
        stb.append(        "AND  (K1.CHAIRCD = '0000000' OR K1.CHAIRCD = K2.CHAIRCD) ");
        stb.append(        "AND  K1.GROUPCD = K2.GROUPCD ");
        stb.append(        "AND  (SUBSTR(SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "' ");
        stb.append(           "OR SUBSTR(SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "') ");
        stb.append(     "GROUP BY K2.CHAIRCD, ");
        stb.append(        " K2.CLASSCD || '-' || K2.SCHOOL_KIND, ");
        stb.append(        " K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
        stb.append(        " K2.SUBCLASSCD,K2.SEMESTER,K2.GROUPCD ");
        stb.append(     ")");

        //科目別授業時数の表 05/09/07 Build
        //中間・期末は指定学期のみ集計、学期・学年は指定学期まで年度内累計
        stb.append(",SUBCLASS_JISU AS(");
        stb.append(    "SELECT  W2.SUBCLASSCD, VALUE(W6.JISU,0) + VALUE(W10.LESSON,0) AS JISU ");
        stb.append(    "FROM   (SELECT SUBCLASSCD FROM CHAIR_A GROUP BY SUBCLASSCD) W2 ");
        stb.append(    "LEFT JOIN( ");
        stb.append(        "SELECT  W1.SUBCLASSCD,COUNT(PERIODCD)AS JISU ");
        stb.append(        "FROM    CHAIR_A W1,SCH_CHR_DAT W2 ");
        stb.append(        "WHERE   W2.YEAR = '" + param._year + "' ");
        stb.append(            "AND W2.EXECUTEDATE BETWEEN ? AND ? ");
        stb.append(            "AND W1.CHAIRCD = W2.CHAIRCD AND W1.SEMESTER = W2.SEMESTER ");
        if (param.definecode.useschchrcountflg) {
            //05/11/25 Build -> 通知表に合わせて
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
            stb.append(                       "WHERE   T4.EXECUTEDATE = W2.EXECUTEDATE AND ");
            stb.append(                               "T4.PERIODCD = W2.PERIODCD AND ");
            stb.append(                               "T4.CHAIRCD = W2.CHAIRCD AND ");
            stb.append(                               "W2.DATADIV IN ('0', '1') AND ");
            stb.append(                               "T4.GRADE||T4.HR_CLASS = ? AND ");
            stb.append(                               "T4.COUNTFLG = '0') ");
            stb.append("    AND NOT EXISTS(SELECT  'X' FROM TEST_COUNTFLG TEST ");
            stb.append("                       WHERE ");
            stb.append("                           TEST.EXECUTEDATE  = W2.EXECUTEDATE ");
            stb.append("                           AND TEST.PERIODCD = W2.PERIODCD ");
            stb.append("                           AND TEST.CHAIRCD  = W2.CHAIRCD ");
            stb.append("                           AND TEST.DATADIV  = W2.DATADIV) ");
        }
        if (param._hasSchChrDatExecutediv) {
            stb.append("        AND VALUE(W2.EXECUTEDIV, '0') <> '2' "); // 休講は対象外
        }
        stb.append(        "GROUP BY W1.SUBCLASSCD");
        stb.append(    ")W6 ON W6.SUBCLASSCD = W2.SUBCLASSCD ");
        stb.append(    "LEFT JOIN(");
        stb.append(        "SELECT  SUBCLASSCD,");
        stb.append(                "SUM(LESSON) AS LESSON ");
        stb.append(        "FROM(");
        stb.append(             "SELECT ");
        stb.append(        " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(                 " SUBCLASSCD AS SUBCLASSCD,");
        stb.append(                     "MAX(LESSON) AS LESSON ");
        stb.append(             "FROM    ATTEND_SUBCLASS_DAT W1 ");
        stb.append(             "WHERE   YEAR = '" + param._year + "' ");
        if (! param._testkindcd.equals("0"))     //05/11/22 Modify
            stb.append(             "AND SEMESTER = '" + param._semester + "' ");
        stb.append(                 "AND SEMESTER||MONTH <= ? ");
        stb.append(                 "AND EXISTS(SELECT  'X' FROM  SCHNO W2 WHERE  W1.SCHREGNO = W2.SCHREGNO)");
        stb.append(             "GROUP BY MONTH, ");
        stb.append(        " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(                 " SUBCLASSCD ");
        stb.append(             ")S1 ");
        stb.append(        "GROUP BY SUBCLASSCD ");
        stb.append(    ")W10 ON W10.SUBCLASSCD = W2.SUBCLASSCD ");
        stb.append(") ");

        //授業時数の表(通常科目＋評価読替先科目) 05/09/07 Build
        //CHAIR_A表に読替後科目が存在する仕様に合わせる 05/11/24 Modify
        stb.append(",SUBCLASS_JISU_B AS(");
        stb.append(    "SELECT  T1.SUBCLASSCD, ");
        stb.append(            "CASE WHEN T2.SUBCLASSCD IS NOT NULL THEN T2.JISU ELSE T1.JISU END AS JISU "); //05/11/24 Modify
        stb.append(    "FROM    SUBCLASS_JISU T1 ");
            //学期成績・学年成績では、読替先科目の授業時数に読替元の合計を代入して出力
        stb.append(    "LEFT JOIN(");  //05/11/24
        stb.append(        "SELECT ");
        stb.append(        " T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || ");
        stb.append(          "T1.COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(JISU) AS JISU ");
        stb.append(        "FROM    SUBCLASS_REPLACE_COMBINED_DAT T1, SUBCLASS_JISU T2 ");
        stb.append(        "WHERE   T1.YEAR ='" + param._year + "' AND T1.REPLACECD = '1' ");
        stb.append(            "AND ");
        stb.append(        " T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
        stb.append(                "T1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append(        "GROUP BY ");
        stb.append(        " T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || ");
        stb.append(          "T1.COMBINED_SUBCLASSCD ");
        stb.append(    ")T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");  //05/11/24
        //}
        stb.append(") ");


        //メイン表
        stb.append("SELECT  W2.SUBCLASSCD,W2.GROUPCD,W4.SUBCLASSABBV AS SUBCLASSNAME,W4.ELECTDIV,W5.CLASSABBV,W7.CREDITS,"); // 05/01/31Modify 05/05/22Modify
        stb.append(        "W9.AVG_HR,W9.SUM_HR,");    // 04/11/08Add
        stb.append(        "ASSESS_GR,");
        stb.append(        "CASE WHEN D065.NAME1 IS NOT NULL THEN 1 WHEN W4.CLASSCD = '90' THEN 2 ELSE 0 END AS D065_ARI,");
        stb.append(        "W6.JISU ");      //05/09/07Modify
        stb.append(       ",CASE WHEN W10.ATTEND_SUBCLASSCD IS NOT NULL THEN 'MOTO' WHEN W11.COMBINED_SUBCLASSCD IS NOT NULL THEN 'SAKI' ELSE NULL END AS REPLACEFLG ");   //05/11/15 Build
        stb.append("FROM(");
        stb.append(    "SELECT  ");
        stb.append(        " CLASSCD, ");
        stb.append(        " SUBCLASSCD, MAX(GROUPCD)AS GROUPCD ");
        stb.append(    "FROM    CHAIR_A T1 ");
        if (! param._semester.equals("9"))                                //05/11/22 Build
            stb.append("WHERE   SEMESTER = '" + param._semeFlg + "' ");  //05/11/22 Build

        //NO104 再掲
        if (! param._testkindcd.equals("0")) {
            stb.append(    "AND NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT T2 " );
            stb.append(                   "WHERE  T2.YEAR ='" + param._year + "' ");
            stb.append(                      "AND REPLACECD = '1' ");  //05/05/22
            stb.append(                      "AND ");
            stb.append(        " T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            stb.append(                          "T2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(                   "GROUP BY COMBINED_SUBCLASSCD) ");
        }

        stb.append(    "GROUP BY ");
        stb.append(        " CLASSCD, ");
        stb.append(        "SUBCLASSCD "); // 05/01/31MODIFY
        stb.append(") W2 ");

        stb.append("INNER JOIN SUBCLASS_MST W4 ON ");
        stb.append(        " W4.CLASSCD || '-' || W4.SCHOOL_KIND || '-' || W4.CURRICULUM_CD || '-' || ");
        stb.append("  W4.SUBCLASSCD = W2.SUBCLASSCD ");
        stb.append("INNER JOIN CLASS_MST W5 ON ");
        stb.append("  W5.CLASSCD || '-' || W5.SCHOOL_KIND = W2.CLASSCD ");

        //D065
        stb.append("LEFT JOIN NAME_MST D065 ON D065.NAMECD1 = 'D065' ");
        stb.append("     AND W2.SUBCLASSCD = D065.NAME1 ");

        //授業時数の表 05/09/07 Modify
        stb.append("LEFT JOIN SUBCLASS_JISU_B W6 ON W6.SUBCLASSCD = W2.SUBCLASSCD ");

        //単位の表
        stb.append(    "LEFT JOIN( ");
        stb.append(       "SELECT ");
        stb.append("  W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(            " SUBCLASSCD AS SUBCLASSCD,CREDITS ");
        stb.append(       "FROM    CREDIT_MST W1 ");
        stb.append(       "WHERE   W1.YEAR = '" + param._year + "' ");
        stb.append(           "AND W1.GRADE = '" + param._grade + "' ");   //05/05/22
        stb.append(           "AND (W1.COURSECD, W1.MAJORCD, W1.COURSECODE) ");
        stb.append(                   "IN(SELECT  COURSECD, MAJORCD, COURSECODE ");
        stb.append(                      "FROM    SCHREG_REGD_DAT ");
        stb.append(                      "WHERE   YEAR = '" + param._year + "' ");
        stb.append(                          "AND GRADE||HR_CLASS = ? ");  //05/05/30
        stb.append(                          "AND SEMESTER = '" + param._semeFlg + "' ");
        stb.append(                      "GROUP BY COURSECD, MAJORCD, COURSECODE) ");  //05/11/25
        stb.append(    ")W7 ON W7.SUBCLASSCD = W2.SUBCLASSCD ");

        //類型の表
        stb.append(    "LEFT JOIN( ");
        stb.append(        "SELECT  ");
        stb.append("  W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
        stb.append(                "W2.SUBCLASSCD AS SUBCLASSCD,");
        stb.append(                "W2." + fieldname + "_TYPE_ASSES_CD AS ASSESS_GR ");
        stb.append(        "FROM    TYPE_GROUP_HR_DAT W1, TYPE_GROUP_MST W2 ");
        stb.append(        "WHERE   W1.YEAR = '" + param._year + "' ");
        stb.append(            "AND W1.GRADE||W1.HR_CLASS = ? ");  //05/05/30
        stb.append(            "AND W2.YEAR = '" + param._year + "' ");
        stb.append(            "AND W1.TYPE_GROUP_CD = W2.TYPE_GROUP_CD ");
        stb.append(    ")W8 ON W8.SUBCLASSCD = W2.SUBCLASSCD ");

        //科目別合計点および平均点の表 04/11/08 Build
        stb.append(    "LEFT JOIN( ");
        stb.append(        "SELECT ");
        stb.append("  W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(              " SUBCLASSCD AS SUBCLASSCD,");
        stb.append(                "INT(ROUND(AVG(FLOAT(" + fieldname + ")),0))AS AVG_HR,");
        stb.append(                "SUM(" + fieldname + ")AS SUM_HR ");
        stb.append(         "FROM   KIN_RECORD_DAT W1 ");
        stb.append(         "INNER  JOIN SCHNO W2 ON W2.SCHREGNO = W1.SCHREGNO ");
        stb.append(         "WHERE  W1.YEAR = '" + param._year + "' ");
        stb.append(            "AND W1.SUBCLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
        stb.append(            "AND " + fieldname + " IS NOT NULL ");
        stb.append(         "GROUP BY ");
        stb.append("  W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(              " SUBCLASSCD");
        stb.append(    ")W9 ON W9.SUBCLASSCD = W2.SUBCLASSCD ");

        //評価読替前科目の表 05/11/15 Build
        stb.append(    "LEFT JOIN (");
        stb.append(        "SELECT ");
        stb.append("  W1.ATTEND_CLASSCD || '-' || W1.ATTEND_SCHOOL_KIND || '-' || W1.ATTEND_CURRICULUM_CD || '-' || ");
        stb.append(                " ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
        stb.append(        "FROM SUBCLASS_REPLACE_COMBINED_DAT W1 ");
        stb.append(        "WHERE YEAR ='" + param._year + "' AND REPLACECD = '1' ");
        stb.append(        "GROUP BY ");
        stb.append("  W1.ATTEND_CLASSCD || '-' || W1.ATTEND_SCHOOL_KIND || '-' || W1.ATTEND_CURRICULUM_CD || '-' || ");
        stb.append(                " ATTEND_SUBCLASSCD ");
        stb.append(    ")W10 ON W10.ATTEND_SUBCLASSCD = W2.SUBCLASSCD ");
        //評価読替後科目の表 05/11/15 Build
        stb.append(    "LEFT JOIN (");
        stb.append(        "SELECT ");
        stb.append("  W1.COMBINED_CLASSCD || '-' || W1.COMBINED_SCHOOL_KIND || '-' || W1.COMBINED_CURRICULUM_CD || '-' || ");
        stb.append(                " COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ");
        stb.append(        "FROM SUBCLASS_REPLACE_COMBINED_DAT W1 ");
        stb.append(        "WHERE YEAR ='" + param._year + "' AND REPLACECD = '1' ");
        stb.append(        "GROUP BY ");
        stb.append("  W1.COMBINED_CLASSCD || '-' || W1.COMBINED_SCHOOL_KIND || '-' || W1.COMBINED_CURRICULUM_CD || '-' || ");
        stb.append(                " COMBINED_SUBCLASSCD ");
        stb.append(    ")W11 ON W11.COMBINED_SUBCLASSCD = W2.SUBCLASSCD ");

        stb.append("ORDER BY D065_ARI, W2.SUBCLASSCD ");
log.debug(stb.toString());
        return stb.toString();

    }//prestatSubclassInfo()の括り


    /**
     *  PrepareStatement作成 --> 成績・評定・欠課データの表
     *  => ps3
     *    2004/12/12 2004年度1学期の集計データはATTEND_SUBCLASS_DATに入っている。以外はATTEND_DAT、SCH_CHR_DATを集計する。
     *    2005/01/04 処理速度を考慮して出欠データから集計したデータと月別出欠集計データから集計したデータを混合して出力
     *               欠課および出欠の記録は異動した生徒の情報も出力する
     *    2005/02/02 学年末では評価読替え科目を出力する
     *    2005/05/22 講座出欠カウントフラグの参照を追加
     *    2005/10/05 Modify 編入（データ仕様の変更による）について修正、および転学を追加
     *    2005/11/22 Modify 学期成績の欠課時数は１学期からの累計とする
     *    2005/12/09 Modify ペナルティ欠課算出の不具合のため、ATTEND_B表を追加して学期の換算を行う
     */
    private String getStdSubclassDetailSql() {

        final StringBuffer stb = new StringBuffer();

        stb.append("WITH ");

        //対象生徒の表
        stb.append("SCHNO_A AS(");
        stb.append(     "SELECT  W1.SCHREGNO,W1.SEMESTER ");
        stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
        stb.append(     "WHERE   W1.YEAR = '" + param._year + "' AND ");
        stb.append(             "W1.SEMESTER = '" + param._semeFlg + "' AND ");
        stb.append(             "W1.GRADE||W1.HR_CLASS = ? AND ");  //05/05/30
        stb.append(             "W1.ATTENDNO BETWEEN ? AND ? ");
        stb.append(     ") ");

        stb.append(",SCHNO_B AS(");
        stb.append(     "SELECT  W1.SCHREGNO,W1.SEMESTER ");
        stb.append(     "FROM    SCHNO_A W1 ");
        stb.append(     "INNER   JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._year + "' ");  //<change specification of 05/09/28>
        stb.append(     "WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
        stb.append(                            "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END) ");     //<change specification of 05/09/28>
        stb.append(                              "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END))) ");  //<change specification of 05/09/28>
        //05/03/09Modify 留学開始日が３学期の場合は成績も出力する
        stb.append(         "AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
        stb.append(                            "AND ((S1.TRANSFERCD IN ('2') AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //<change specification of 05/09/28>
        stb.append(                              "OR (S1.TRANSFERCD IN ('1') AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE AND S1.TRANSFER_EDATE < '" + param._transferDate + "' ))) ");  //<change specification of 05/09/28>
        stb.append(     ") ");

        stb.append(" , TEST_COUNTFLG AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         '2' AS DATADIV ");
        stb.append("     FROM ");
        stb.append("         SCH_CHR_TEST T1, ");
        stb.append("         TESTITEM_MST_COUNTFLG T2 ");
        stb.append("     WHERE ");
        stb.append("         T2.YEAR       = T1.YEAR ");
        stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append("         AND T2.COUNTFLG   = '0' ");
        stb.append(" ) ");

        //対象講座の表
        //05/11/22 Modify (何れの表にも)欠課超過単位をみるため、指定学期までの講座を抽出する、
        stb.append(",CHAIR_A AS(");
        stb.append(     "SELECT  K2.CHAIRCD, ");
        stb.append("  K2.CLASSCD, ");
        stb.append("  K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
        stb.append(                         " K2.SUBCLASSCD AS SUBCLASSCD,K2.SEMESTER ");
        stb.append(     "FROM    CHAIR_CLS_DAT K1,CHAIR_DAT K2 ");
        stb.append(     "WHERE   K1.YEAR = '" + param._year + "' ");
        stb.append(        "AND  K2.YEAR = '" + param._year + "' ");
        stb.append(        "AND  K1.SEMESTER <= '" + param._semeFlg + "' ");
        stb.append(        "AND  K2.SEMESTER <= '" + param._semeFlg + "' ");
        stb.append(        "AND  K1.SEMESTER = K2.SEMESTER ");
        stb.append(        "AND  K1.TRGTGRADE||K1.TRGTCLASS = ? ");   //05/05/30
        stb.append(        "AND  (K1.CHAIRCD = '0000000' OR K1.CHAIRCD = K2.CHAIRCD) ");
        stb.append(        "AND  K1.GROUPCD = K2.GROUPCD ");
        stb.append(        "AND  (CLASSCD <= '" + KNJDefineSchool.subject_U + "' ");
        stb.append(           "OR CLASSCD = '" + KNJDefineSchool.subject_T + "') ");
        stb.append(     "GROUP BY K2.CHAIRCD,");
        stb.append("  K2.CLASSCD, ");
        stb.append("  K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
        stb.append(                         " K2.SUBCLASSCD,K2.SEMESTER ");
        stb.append(     ")");

        //対象生徒の時間割データ
        stb.append(" , SCHEDULE_SCHREG_R AS( ");
        stb.append(          "SELECT T2.SCHREGNO, T3.SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
        stb.append(                 "FROM   SCH_CHR_DAT T1,");
        stb.append(                        "CHAIR_STD_DAT T2, ");
        stb.append(                        "CHAIR_A T3 ");
        stb.append(                 "WHERE  T1.YEAR = '" + param._year + "' AND ");
        stb.append(                        "T1.SEMESTER = T3.SEMESTER AND ");
        stb.append(                        "T1.CHAIRCD = T3.CHAIRCD AND ");
        stb.append(                        "T1.EXECUTEDATE BETWEEN ? AND ? AND ");
        stb.append(                        "T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND ");
        stb.append(                        "T2.YEAR = '" + param._year + "' AND ");
        stb.append(                        "T2.SEMESTER = T3.SEMESTER AND ");
        stb.append(                        "T2.CHAIRCD = T3.CHAIRCD AND ");
        stb.append(                        "T1.SEMESTER = T2.SEMESTER AND ");
        stb.append(                        "T1.CHAIRCD = T2.CHAIRCD ");
        stb.append(                    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T4 ");    //<change specification of 05/09/28>
        stb.append(                                   "WHERE   T4.SCHREGNO = T2.SCHREGNO ");
        stb.append(                                       "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");
        stb.append(                                         "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE ))) ");
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._year + "' AND L1.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        stb.append("                  ) ");
        // 勤怠コード'28'は時間割にカウントしない
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._year + "' AND L1.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND L1.REP_DI_CD = '28' ");
        stb.append("                  ) ");

        if (param.definecode.useschchrcountflg) {
            stb.append(                "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
            stb.append(                               "WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE AND ");
            stb.append(                                       "T4.PERIODCD = T1.PERIODCD AND ");
            stb.append(                                       "T4.CHAIRCD = T1.CHAIRCD AND ");
            stb.append(                                       "T4.GRADE||T4.HR_CLASS = ? AND ");  //05/05/30
            stb.append(                                       "T4.COUNTFLG = '0') ");       //05/05/22
            stb.append(               " AND NOT EXISTS(SELECT  'X' FROM TEST_COUNTFLG TEST ");
            stb.append(                               "WHERE ");
            stb.append(                                       "TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
            stb.append(                                       "AND TEST.PERIODCD = T1.PERIODCD ");
            stb.append(                                       "AND TEST.CHAIRCD  = T1.CHAIRCD ");
            stb.append(                                       "AND TEST.DATADIV  = T1.DATADIV) ");
        }
        if (param._hasSchChrDatExecutediv) {
            stb.append("        AND VALUE(T1.EXECUTEDIV, '0') <> '2' "); // 休講は対象外
        }

        stb.append(                 "GROUP BY T2.SCHREGNO, T3.SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
        stb.append(" ) ");

        //対象生徒の時間割データ
        stb.append(" , SCHEDULE_SCHREG AS(");
        stb.append(     "SELECT  T1.* ");
        stb.append(     "FROM    SCHEDULE_SCHREG_R T1 ");
        stb.append(     "WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");    //--NO101 Build 'NOT EXISTS?'
        stb.append(                        "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
        stb.append(                            "AND T4.TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE ) ");
        stb.append(" ) ");

        // 休学中の授業日数
        stb.append(", SCH_TRANSFER AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     '00' AS DI_CD ");
        stb.append(" FROM ");
        stb.append("     SCHEDULE_SCHREG_R T1, ");
        stb.append("     SCHREG_TRANSFER_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("     T2.TRANSFERCD IN('2') ");
        stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     AND T1.EXECUTEDATE BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T1.SEMESTER ");
        stb.append(" ) ");

        //欠課数の表
        //param._5 != nul: 遅刻・早退を欠課に換算する
        //中間・期末における欠課表記は学期内累積、学期・学年における欠課表記は年度内累積とするため、２通りの欠課時数を持つようにする 05/11/24 Modify
        //05/12/09 列にSEMESTERを追加し、学期別の集計だけ行う
        stb.append(",ATTEND_A AS(");
        stb.append(     "SELECT  SCHREGNO, S1.SUBCLASSCD, SEMESTER, ");
        stb.append(             "SUM(ABSENT1) AS ABSENT1, ");        //05/12/09
        stb.append(             "SUM(LATE_EARLY) AS LATE_EARLY ");   //05/12/09
        //出欠データより集計
        stb.append(     "FROM ( ");
        stb.append(          "SELECT S1.SCHREGNO, S1.SUBCLASSCD, SEMESTER,");  //05/11/22 Modify

        stb.append(                 "SUM(CASE WHEN (CASE WHEN L1.REP_DI_CD IN ('29','30','31') THEN VALUE(L1.ATSUB_REPL_DI_CD, L1.REP_DI_CD) ELSE L1.REP_DI_CD END) IN( ");
        if (null != param._knjSchoolMst._subSuspend && param._knjSchoolMst._subSuspend.equals("1")) {
            stb.append(                 " '2', '9', ");
        }
        if (null != param._knjSchoolMst._subVirus && param._knjSchoolMst._subVirus.equals("1")) {
            stb.append(                 " '19', '20', ");
        }
        if (null != param._knjSchoolMst._subKoudome && param._knjSchoolMst._subKoudome.equals("1")) {
            stb.append(                 " '25', '26', ");
        }
        if (null != param._knjSchoolMst._subMourning && param._knjSchoolMst._subMourning.equals("1")) {
            stb.append(                 " '3', '10', ");
        }
        if (null != param._knjSchoolMst._subAbsent && param._knjSchoolMst._subAbsent.equals("1")) {
            stb.append(                 " '1', '8', ");
        }
        stb.append(                 "'4','5','6','14','11','12','13') THEN 1 ELSE 0 END)AS ABSENT1, ");
        stb.append(                 "SUM(CASE WHEN L1.REP_DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(L1.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
        stb.append(          "FROM SCHEDULE_SCHREG S1 ");
        stb.append(          "INNER JOIN ATTEND_DAT S2 ON S2.YEAR = '" + param._year + "' AND ");
        stb.append(                                      "S2.ATTENDDATE = S1.EXECUTEDATE AND ");
        stb.append(                                      "S2.PERIODCD = S1.PERIODCD AND ");
        stb.append(                                      "S1.SCHREGNO = S2.SCHREGNO ");
        stb.append("          INNER JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._year + "' AND L1.DI_CD = S2.DI_CD ");
        stb.append("                    AND L1.REP_DI_CD IN( ");
        if (null != param._knjSchoolMst._subSuspend && param._knjSchoolMst._subSuspend.equals("1")) {
            stb.append(                 " '2', '9', ");
        }
        if (null != param._knjSchoolMst._subVirus && param._knjSchoolMst._subVirus.equals("1")) {
            stb.append(                 " '19', '20', ");
        }
        if (null != param._knjSchoolMst._subKoudome && param._knjSchoolMst._subKoudome.equals("1")) {
            stb.append(                 " '25', '26', ");
        }
        if (null != param._knjSchoolMst._subMourning && param._knjSchoolMst._subMourning.equals("1")) {
            stb.append(                 " '3', '10', ");
        }
        if (null != param._knjSchoolMst._subAbsent && param._knjSchoolMst._subAbsent.equals("1")) {
            stb.append(                 " '1', '8', ");
        }
        stb.append(                                      "'4','5','6','14','15','16','11','12','13','23','24') ");
        stb.append(          "GROUP BY S1.SCHREGNO, S1.SUBCLASSCD, SEMESTER ");

        if (null != param._knjSchoolMst._subOffDays && param._knjSchoolMst._subOffDays.equals("1")) {
            stb.append("UNION ALL ");
            stb.append("SELECT ");
            stb.append("    SCHREGNO, ");
            stb.append("    SUBCLASSCD, ");
            stb.append("    SEMESTER, ");
            stb.append("    COUNT(*) AS ABSENT1, ");
            stb.append("    0 AS LATE_EARLY ");
            stb.append("FROM SCH_TRANSFER ");
            stb.append("GROUP BY ");
            stb.append("    SCHREGNO, ");
            stb.append("    SUBCLASSCD, ");
            stb.append("    SEMESTER ");
        }
                //月別科目別出欠集計データより欠課を取得  2005/01/04Modify
        stb.append(          "UNION ALL ");
        stb.append(             "SELECT  W1.SCHREGNO, ");
        stb.append("  W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(                                   " W1.SUBCLASSCD AS SUBCLASSCD, W1.SEMESTER, ");  //05/11/22 Modify
        stb.append(                 "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0) ");
        if (null != param._knjSchoolMst._subOffDays && param._knjSchoolMst._subOffDays.equals("1")) {
            stb.append(                 " + VALUE(OFFDAYS,0) ");
        }
        if (null != param._knjSchoolMst._subSuspend && param._knjSchoolMst._subSuspend.equals("1")) {
            stb.append("            + VALUE(SUSPEND, 0) ");
        }
        if (null != param._knjSchoolMst._subVirus && param._knjSchoolMst._subVirus.equals("1")) {
            stb.append("            + VALUE(VIRUS, 0) ");
        }
        if (null != param._knjSchoolMst._subKoudome && param._knjSchoolMst._subKoudome.equals("1")) {
            stb.append("            + VALUE(KOUDOME, 0) ");
        }
        if (null != param._knjSchoolMst._subMourning && param._knjSchoolMst._subMourning.equals("1")) {
            stb.append("            + VALUE(MOURNING, 0) ");
        }
        if (null != param._knjSchoolMst._subAbsent && param._knjSchoolMst._subAbsent.equals("1")) {
            stb.append("            + VALUE(ABSENT, 0) ");
        }
        stb.append(                 " ) AS ABSENT1, ");
        stb.append(                  "SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
        stb.append(             "FROM    ATTEND_SUBCLASS_DAT W1, SCHNO_A W2 ");
        stb.append(             "WHERE   W1.YEAR = '" + param._year + "' AND ");
        stb.append(                  "W1.SEMESTER <= '" + param._semeFlg + "' AND ");    //05/11/22 Modify
        stb.append(                  "W1.SEMESTER||W1.MONTH <= ? AND ");
        stb.append(                  "W1.SCHREGNO = W2.SCHREGNO ");
        stb.append(             "GROUP BY W1.SCHREGNO, ");
        stb.append("  W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(                                    " W1.SUBCLASSCD, W1.SEMESTER ");
        stb.append(          ")S1 ");
        stb.append(     "GROUP BY S1.SCHREGNO, SUBCLASSCD, SEMESTER ");
        stb.append(     ") ");

        //学期内のペナルティ欠課換算を行う 05/12/09 Build
        if (param._output3 != null) {
            stb.append(",ATTEND_A2 AS(");
            stb.append(     "SELECT SCHREGNO, SEMESTER, SUBCLASSCD, ");
            stb.append(     "SUM(ABSENT1) AS ABSENT1, ");
            stb.append(     "SUM(LATE_EARLY) AS LATE_EARLY ");
            stb.append(     "FROM   ATTEND_A ");
            stb.append(     "GROUP BY SCHREGNO, SEMESTER, SUBCLASSCD ");
            stb.append(     ") ");
        }

        stb.append(",ATTEND_B AS(");
        stb.append(     "SELECT SCHREGNO, SUBCLASSCD, ");
        if (param._output3 != null) {
            stb.append(        "SUM(VALUE(ABSENT1 ,0)) + SUM(VALUE(LATE_EARLY, 0) / 3) AS ABSENT1 "); //05/11/24 Modify
            stb.append(       ",SUM( CASE WHEN SEMESTER = '" + param._semeFlg + "' THEN VALUE(ABSENT1,0) + VALUE(LATE_EARLY, 0) / 3 ELSE 0 END ) AS ABSENT2 "); //05/15/24
            stb.append(     "FROM   ATTEND_A2 ");
        } else{
            stb.append(        "SUM(VALUE(ABSENT1, 0)) AS ABSENT1 "); //05/11/24 Modify
            stb.append(       ",SUM( CASE WHEN SEMESTER = '" + param._semeFlg + "' THEN VALUE(ABSENT1,0) ELSE 0 END ) AS ABSENT2 "); //05/15/24
            stb.append(     "FROM   ATTEND_A ");
        }
        stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
        stb.append(     ") ");


        //成績データの表（通常科目）
        stb.append(",RECORD_REC AS(");
        stb.append(prestatStdSubclassRecord(0));
        stb.append(     ") ");

        //評定読替え科目評定の表 05/03/09Modify
        if (param._testkindcd.equals("0")) {          //05/03/01Modify
            stb.append(",REPLACE_REC AS(");
            stb.append(     "SELECT SCHREGNO, ");
            stb.append("  W2.COMBINED_CLASSCD || '-' || W2.COMBINED_SCHOOL_KIND || '-' || W2.COMBINED_CURRICULUM_CD || '-' || ");
            stb.append(            "W2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("  W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
            stb.append(            "W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
            stb.append(            "SCORE, ");    //05/03/01Modify
            stb.append(            "PATTERN_ASSESS ");
            stb.append(     "FROM   RECORD_REC W1, SUBCLASS_REPLACE_COMBINED_DAT W2 ");
            stb.append(     "WHERE  W1.SUBCLASSCD = ");
            stb.append("  W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
            stb.append(            " W2.ATTEND_SUBCLASSCD AND ");
            stb.append(            "W2.YEAR='" + param._year + "' AND REPLACECD='1' ");  //05/05/22
            stb.append(     ") ");

            //05/03/09
            stb.append(",REPLACE_REC_CNT AS(");
            stb.append(     "SELECT SCHREGNO,SUBCLASSCD,");
            stb.append(            "sum(case when score in ('KS','( )','欠') then 1 else 0 end) as ks ");
            stb.append(     "FROM REPLACE_REC W1 ");
            stb.append(     "WHERE w1.score in('KK','KS','( )','欠','公') ");
            stb.append(     "GROUP BY SCHREGNO,SUBCLASSCD ");
            stb.append(     "having 0 < count(*) ");
            stb.append(     ") ");

            //05/03/09
            stb.append(",REPLACE_REC_ATTEND AS(");
            stb.append(     "SELECT SCHREGNO, ");
            stb.append("  W2.COMBINED_CLASSCD || '-' || W2.COMBINED_SCHOOL_KIND || '-' || W2.COMBINED_CURRICULUM_CD || '-' || ");
            stb.append(            "W2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("  W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
            stb.append(            "W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
            stb.append(            fieldname + " AS SCORE, ");    //05/03/01Modify
            stb.append(            "CASE VALUE(JUDGE_PATTERN,'X') WHEN 'A' THEN A_PATTERN_ASSESS ");
            stb.append(                                          "WHEN 'B' THEN B_PATTERN_ASSESS ");
            stb.append(                                          "WHEN 'C' THEN C_PATTERN_ASSESS ");
            stb.append(                                          "ELSE NULL END AS PATTERN_ASSESS ");
            stb.append(     "FROM   KIN_RECORD_DAT W1, SUBCLASS_REPLACE_COMBINED_DAT W2 ");
            stb.append(     "WHERE  W1.YEAR = '" + param._year + "' AND ");
            stb.append(            "W1.SUBCLASSCD = ");
            stb.append("  W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
            stb.append(            " W2.ATTEND_SUBCLASSCD AND ");
            stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO) AND ");  //05/03/09Modiyf
            stb.append(            "W2.YEAR='" + param._year + "' AND REPLACECD='1' ");  //05/05/22
            stb.append(     ") ");
         }
        //メイン表
        stb.append("SELECT  T1.SUBCLASSCD,T1.SCHREGNO,");
        stb.append(        "T5.ABSENT1,T5.ABSENT2,"); //'ABSENT2'を追加 05/11/24 Modiy
        stb.append(        "SCORE, SCORE_FLG, DICD, ");
        stb.append(        "CASE WHEN D065.NAME1 IS NOT NULL THEN 1 WHEN T1.CLASSCD = '90' THEN 2 ELSE 0 END AS D065_ARI,");

        stb.append(        "CASE WHEN ");
        stb.append(                   " T1.CLASSCD ");
        stb.append(                   " NOT IN ('90') ");
        stb.append(                   " AND T1.SUBCLASSCD NOT IN (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'D065') ");
        stb.append(                   " THEN PATTERN_ASSESS ");
        stb.append(             "ELSE (SELECT ASSESSMARK FROM RELATIVEASSESS_MST S1 ");
        stb.append(                   "WHERE  S1.GRADE = '" + param._grade + "' AND ");  //05/05/22
        stb.append(                          "S1.ASSESSCD = '3' AND ");
        stb.append("  S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || ");
        stb.append(                          "S1.SUBCLASSCD = T1.SUBCLASSCD AND ");
        stb.append(                          "ASSESSLOW <= INT(PATTERN_ASSESS) AND INT(PATTERN_ASSESS) <= ASSESSHIGH )END ");  //50/03/03Modify
        stb.append(             "AS PATTERN_ASSESS ");    //05/03/01Modify
        if (param._testkindcd.equals("0")) {
            stb.append(    ", REPLACEMOTO ");
        }
        //対象生徒・講座の表
        stb.append("FROM(");
        stb.append(     "SELECT  W1.SCHREGNO, ");
        stb.append("  W2.CLASSCD, ");
        stb.append(                           " W2.SUBCLASSCD ");
        stb.append(        "FROM    CHAIR_STD_DAT W1, CHAIR_A W2, SCHNO_A W3 ");
        stb.append(        "WHERE   W1.YEAR = '" + param._year + "' ");
        stb.append(        "AND  W1.CHAIRCD = W2.CHAIRCD ");
        if (! param._semester.equals("9")) {   //05/11/22 Modify
            stb.append(       "AND  W1.SEMESTER = '" + param._semeFlg + "' ");
            stb.append(       "AND  W2.SEMESTER = '" + param._semeFlg + "' ");
        }
        stb.append(        "AND  W1.SEMESTER = W2.SEMESTER ");
        stb.append(           "AND  W1.SCHREGNO = W3.SCHREGNO ");
        stb.append(     "GROUP BY W1.SCHREGNO, ");
        stb.append("  W2.CLASSCD, ");
        stb.append(                           " W2.SUBCLASSCD ");
        stb.append(")T1 ");

        //D065
        stb.append("LEFT JOIN NAME_MST D065 ON D065.NAMECD1 = 'D065' ");
        stb.append("     AND T1.SUBCLASSCD = D065.NAME1 ");

        //成績の表
        stb.append(  "LEFT JOIN(");
        //成績の表（通常科目）
        if (param._testkindcd.equals("0")) {
            stb.append(     "SELECT W3.*, ");
            stb.append(            "(SELECT COUNT(*) FROM REPLACE_REC S1 WHERE S1.SCHREGNO = W3.SCHREGNO AND S1.ATTEND_SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append(                                                    "GROUP BY ATTEND_SUBCLASSCD)AS REPLACEMOTO ");
            stb.append(     "FROM   RECORD_REC W3 ");
            stb.append(        "WHERE  NOT EXISTS( SELECT 'X' FROM REPLACE_REC S2 WHERE W3.SUBCLASSCD = S2.SUBCLASSCD) ");
        } else{
            stb.append(     "SELECT W3.* ");
            stb.append(     "FROM   RECORD_REC W3 ");
        }

        //if (param._1.equals("9")) {
        if (param._testkindcd.equals("0")) {         //05/03/01Modify
            //評定読替え科目の成績の表
            //NO010 Modify
            stb.append("UNION ALL ");
            stb.append("SELECT  W1.SCHREGNO, W1.SUBCLASSCD ");
            stb.append(       ",RTRIM(CHAR(SCORE)) AS SCORE ");
            stb.append(       ", PATTERN_ASSESS ");
            stb.append(       ", SCORE_FLG ");
            stb.append(       ", DICD ");
            stb.append(       ", -1 AS REPLACEMOTO ");
            stb.append("FROM   RECORD_REC W1 ");
            stb.append("WHERE  EXISTS(SELECT  'X'   FROM REPLACE_REC W2 ");
            stb.append(              "WHERE   W2.SCHREGNO = W1.SCHREGNO ");
            stb.append(                 "AND  W2.SUBCLASSCD = W1.SUBCLASSCD ");
            stb.append(              "GROUP BY W2.SUBCLASSCD) ");
            stb.append(   "AND NOT EXISTS(SELECT  'X'  ");
            stb.append(                  "FROM    REPLACE_REC_CNT W2 ");
            stb.append(                  "WHERE   W2.SCHREGNO = W1.SCHREGNO ");
            stb.append(                      "AND W2.SUBCLASSCD = W1.SUBCLASSCD) ");
            stb.append(   "AND W1.SCORE NOT IN('KK','KS','( )','欠','公') ");
            stb.append(   "AND W1.SCORE IS NOT NULL ");
            stb.append(   "AND EXISTS(SELECT 'X' FROM SCHNO_B W3 WHERE W3.SCHREGNO = W1.SCHREGNO) ");

            stb.append(     "UNION ALL ");
            stb.append(     "SELECT SCHREGNO,SUBCLASSCD, ");
            stb.append(            "case when 0 < ks then '欠' else '公' end AS SCORE, ");     //05/03/01Modify
            stb.append(            "'' AS PATTERN_ASSESS, ");
            stb.append(            "CASE WHEN SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS SCORE_FLG, ");  //05/03/01Modify
            stb.append(            "''  AS DICD,");
            stb.append(               "-1 AS REPLACEMOTO ");
            stb.append(     "FROM REPLACE_REC_cnt W1 ");
            stb.append(     "WHERE EXISTS(SELECT 'X' FROM SCHNO_B W3 WHERE W3.SCHREGNO = W1.SCHREGNO) ");  //05/03/09Modiyf
        }
        stb.append(        ")T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO ");
        //欠課数の表
        stb.append(  "LEFT JOIN(");
        stb.append(     "SELECT  W1.SCHREGNO, W1.SUBCLASSCD, ABSENT1, ABSENT2 "); //'ABSENT2'を追加 05/11/24 Modiy
        stb.append(     "FROM    ATTEND_B W1 ");   //05/12/09 A=>B
        //評定読替え科目の欠課数の表
        stb.append(     "UNION ");
        stb.append(     "SELECT  W2.SCHREGNO, ");
        stb.append("  W1.COMBINED_CLASSCD || '-' || W1.COMBINED_SCHOOL_KIND || '-' || W1.COMBINED_CURRICULUM_CD || '-' || ");
        stb.append(             "W1.COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(ABSENT1)AS ABSENT1, SUM(ABSENT2)AS ABSENT2 "); //'ABSENT2'を追加 05/11/24 Modiy
        stb.append(     "FROM    SUBCLASS_REPLACE_COMBINED_DAT W1, ATTEND_B W2 ");   //05/12/09 A=>B
        stb.append(     "WHERE   W1.YEAR ='" + param._year + "' AND W1.REPLACECD = '1' ");
        stb.append(         "AND ");
        stb.append("  W1.ATTEND_CLASSCD || '-' || W1.ATTEND_SCHOOL_KIND || '-' || W1.ATTEND_CURRICULUM_CD || '-' || ");
        stb.append(             "W1.ATTEND_SUBCLASSCD = W2.SUBCLASSCD ");
        stb.append(     "GROUP BY W2.SCHREGNO, ");
        stb.append("  W1.COMBINED_CLASSCD || '-' || W1.COMBINED_SCHOOL_KIND || '-' || W1.COMBINED_CURRICULUM_CD || '-' || ");
        stb.append(             "W1.COMBINED_SUBCLASSCD ");

        stb.append(  ")T5 ON T5.SUBCLASSCD = T1.SUBCLASSCD AND T5.SCHREGNO = T1.SCHREGNO ");

        stb.append("ORDER BY D065_ARI, T1.SUBCLASSCD, T1.SCHREGNO");
        return stb.toString();

    }//prestatStdSubclassDetailの括り


    /**
     *  PrepareStatement作成 --> 成績・評定・欠課データの表における成績データの再構築
     *    2005/02/10
     */
    private String prestatStdSubclassRecord(final int sdiv)
    {
        final StringBuffer stb = new StringBuffer();
        final StringBuffer join = new StringBuffer();
        stb.append("SELECT  W3.SCHREGNO, ");
        stb.append(        " W3.CLASSCD  || '-' || W3.SCHOOL_KIND || '-' ||  W3.CURRICULUM_CD  || '-' || W3.SUBCLASSCD AS SUBCLASSCD, ");
        if (FLG == KNJD060K_INTER || FLG == KNJD060K_TERM) {
            String field1 = "", field2 = "", field3 = "", field4 = "";
            if (FLG == KNJD060K_INTER) {
                field1 = "SEM" + param._semester + "_INTER_REC";
                field2 = "SEM" + param._semester + "_INTER_REC_DI";
                field3 = "SEM" + param._semester + "_INTER_REC_FLG";
                field4 = "SEM" + param._semester + "_INTER_REC_TYPE_ASSES_CD";
            } else if (FLG == KNJD060K_TERM) {
                field1 = "SEM" + param._semester + "_TERM_REC";
                field2 = "SEM" + param._semester + "_TERM_REC_DI";
                field3 = "SEM" + param._semester + "_TERM_REC_FLG";
                field4 = "SEM" + param._semester + "_TERM_REC_TYPE_ASSES_CD";
            }

            stb.append(        "CASE WHEN " + field1 + " IS NULL AND " + field2 + " IN('KK','KS') THEN " + field2 + " ");
            stb.append(             "WHEN " + field1 + " IS NOT NULL THEN RTRIM(CHAR(" + field1 + ")) ");
            stb.append(             "ELSE NULL END AS SCORE ");
            if (sdiv == 0) {
                if (FLG == KNJD060K_INTER) {
                    stb.append(   ",CASE WHEN ");
                    stb.append(             " W3.CLASSCD ");
                    stb.append(                   " NOT IN ('90') ");
                    stb.append(                   " AND W3.CLASSCD  || '-' || W3.SCHOOL_KIND || '-' ||  W3.CURRICULUM_CD  || '-' || W3.SUBCLASSCD NOT IN (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'D065') ");
                    stb.append(             "  THEN ");
                    stb.append(             "(SELECT  TYPE_ASSES_LEVEL ");
                    stb.append(              "FROM    TYPE_ASSES_MST W1 ");
                    stb.append(              "WHERE   W1.YEAR='" + param._year + "' AND ");
                    stb.append(                      "W1.TYPE_ASSES_CD = W2." + field4 + " AND ");
                    stb.append(                      field1 + " BETWEEN TYPE_ASSES_LOW AND TYPE_ASSES_HIGH) ");
                    stb.append(         "ELSE RTRIM(CHAR(GRADE_RECORD)) END AS PATTERN_ASSESS, ");
                } else if (FLG == KNJD060K_TERM) {
                    stb.append(    ",(SELECT  TYPE_ASSES_LEVEL ");
                    stb.append(      "FROM    TYPE_ASSES_MST W1 ");
                    stb.append(      "WHERE   W1.YEAR='" + param._year + "' AND ");
                    stb.append(              "W1.TYPE_ASSES_CD = W2." + field4 + " AND ");
                    stb.append(              field1 + " BETWEEN TYPE_ASSES_LOW AND TYPE_ASSES_HIGH) ");
                    stb.append(                                           "AS PATTERN_ASSESS, ");
                }
                stb.append(    field3 + "  AS SCORE_FLG, ");
                stb.append(    field2 + "   AS DICD ");
            }

            if (sdiv == 0) {
                join.append(    "LEFT JOIN(");
                join.append(        "SELECT  " + field4 + ", ");
                join.append(        " S1.CLASSCD  || '-' || S1.SCHOOL_KIND || '-' ||  S1.CURRICULUM_CD  || '-' || ");
                join.append(        "S1.SUBCLASSCD AS SUBCLASSCD ");
                join.append(        "FROM    TYPE_GROUP_MST S1,TYPE_GROUP_HR_DAT S2 ");
                join.append(        "WHERE   S1.YEAR = '" + param._year + "' AND S1.YEAR = S2.YEAR AND ");
                join.append(                "S1.TYPE_GROUP_CD = S2.TYPE_GROUP_CD AND ");
                join.append(                "S2.GRADE||S2.HR_CLASS = ? ");   //05/05/30
                join.append(        ") W2 ON ");
                join.append(        " W3.CLASSCD  || '-' || W3.SCHOOL_KIND || '-' ||  W3.CURRICULUM_CD  || '-' || W3.SUBCLASSCD = W2.SUBCLASSCD ");
            }
        } else if (FLG == KNJD060K_GAKKI) {
            String field3;
            if (param._semester.equals("1")  ||  param._semester.equals("2")) {
                field3 = "SEM" + param._semester + "_REC_TYPE_ASSES_CD";
            } else {
                field3 = "SEM" + param._semester + "_TERM_REC_TYPE_ASSES_CD";
            }
            if (param._semester.equals("1")  ||  param._semester.equals("2")) {
                //１・２学期仕様
                stb.append(        "CASE WHEN ((SEM" + param._semester + "_INTER_REC IS NULL AND SEM" + param._semester + "_INTER_REC_DI IN('KK','KS')) OR ");
                stb.append(                   "(SEM" + param._semester + "_TERM_REC  IS NULL AND SEM" + param._semester + "_TERM_REC_DI  IN('KK','KS'))) AND ");
                stb.append(                   "VALUE(SEM" + param._semester + "_REC_FLG,'0') = '0' THEN CASE WHEN (SEM" + param._semester + "_INTER_REC_DI = 'KS' OR SEM" + param._semester + "_TERM_REC_DI = 'KS') THEN '欠' ELSE '公' END ");  //05/03/01MODIFY
                stb.append(             "WHEN SEM" + param._semester + "_REC IS NOT NULL THEN RTRIM(CHAR(SEM" + param._semester + "_REC)) ");
                stb.append(             "ELSE NULL END AS SCORE ");
            } else {
                //３学期仕様
                stb.append(        "CASE WHEN (SEM" + param._semester + "_TERM_REC IS NULL AND SEM" + param._semester + "_TERM_REC_DI  IN('KK','KS')) AND ");
                stb.append(                   "VALUE(SEM" + param._semester + "_REC_FLG,'0') = '0' THEN CASE WHEN (SEM" + param._semester + "_TERM_REC_DI = 'KS') THEN '欠' ELSE '公' END ");  //05/03/01MODIFY
                stb.append(             "WHEN SEM" + param._semester + "_TERM_REC IS NOT NULL THEN RTRIM(CHAR(SEM" + param._semester + "_TERM_REC)) ");
                stb.append(             "ELSE NULL END AS SCORE ");
            }
            if (sdiv == 0) {
                stb.append(   ",CASE WHEN ");
                stb.append(                   " W3.CLASSCD NOT IN ('90') ");  //05/03/03Modify 総合学習を追加
                stb.append(                   " AND W3.CLASSCD  || '-' || W3.SCHOOL_KIND || '-' ||  W3.CURRICULUM_CD  || '-' || W3.SUBCLASSCD NOT IN (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'D065') ");
                stb.append(                   " THEN ");
                stb.append(             "(SELECT  TYPE_ASSES_LEVEL ");
                stb.append(              "FROM    TYPE_ASSES_MST W1 ");
                stb.append(              "WHERE   W1.YEAR='" + param._year + "' AND ");
                if (param._semester.equals("1")  ||  param._semester.equals("2")) {
                    stb.append(                      "W1.TYPE_ASSES_CD = W2.SEM" + param._semester + "_REC_TYPE_ASSES_CD AND ");
                    stb.append(                      "SEM" + param._semester + "_REC BETWEEN TYPE_ASSES_LOW AND TYPE_ASSES_HIGH) ");
                    stb.append(         "ELSE RTRIM(CHAR(GRADE_RECORD)) END AS PATTERN_ASSESS, ");

                    stb.append(    "SEM" + param._semester + "_REC_FLG  AS SCORE_FLG, ");
                    stb.append(    "''  AS DICD ");
                } else {
                    stb.append(                      "W1.TYPE_ASSES_CD = W2.SEM" + param._semester + "_TERM_REC_TYPE_ASSES_CD AND ");
                    stb.append(                      "SEM" + param._semester + "_TERM_REC BETWEEN TYPE_ASSES_LOW AND TYPE_ASSES_HIGH) ");
                    stb.append(         "ELSE RTRIM(CHAR(GRADE_RECORD)) END AS PATTERN_ASSESS, ");

                    stb.append(    "SEM" + param._semester + "_TERM_REC_FLG  AS SCORE_FLG, ");
                    stb.append(    "''  AS DICD ");
                }
            }

            if (sdiv == 0) {
                join.append(    "LEFT JOIN(");
                join.append(        "SELECT  " + field3 + ",");
                join.append(        " S1.CLASSCD  || '-' || S1.SCHOOL_KIND || '-' ||  S1.CURRICULUM_CD  || '-' || ");
                join.append(        "S1.SUBCLASSCD AS SUBCLASSCD ");
                join.append(        "FROM    TYPE_GROUP_MST S1,TYPE_GROUP_HR_DAT S2 ");
                join.append(        "WHERE   S1.YEAR = '" + param._year + "' AND S1.YEAR = S2.YEAR AND ");
                join.append(                "S1.TYPE_GROUP_CD = S2.TYPE_GROUP_CD AND ");
                join.append(                "S2.GRADE||S2.HR_CLASS = ? ");   //05/05/30
                join.append(  ") W2 ON ");
                join.append(        " W3.CLASSCD  || '-' || W3.SCHOOL_KIND || '-' ||  W3.CURRICULUM_CD  || '-' || ");
                join.append(        "  W3.SUBCLASSCD = W2.SUBCLASSCD ");
            }
        } else if (FLG == KNJD060K_GRADE) {
            stb.append(        "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
            stb.append(                   "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
            stb.append(                   "VALUE(SEM1_REC_FLG,'0') = '0' THEN '欠' ");     //05/03/01Modify
            if (param._semeFlg.equals("1")) {
                //１学期仕様
            } else if (param._semeFlg.equals("2") || param._grade.equals("03")) {
                //２学期仕様
                stb.append(             "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
                stb.append(                   "(SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
                stb.append(                   "VALUE(SEM2_REC_FLG,'0') = '0' THEN '欠' ");   //05/03/01Modify

            } else {
                //３学期仕様
                stb.append(             "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
                stb.append(                   "(SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
                stb.append(                   "VALUE(SEM2_REC_FLG,'0') = '0' THEN '欠' ");    //05/03/01Modify
                stb.append(             "WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND ");
                stb.append(                   "VALUE(SEM3_TERM_REC_FLG,'0') = '0' THEN '欠' ");    //05/03/01Modify
            }
            stb.append(             "WHEN GRADE_RECORD IS NOT NULL THEN RTRIM(CHAR(GRADE_RECORD)) ");
            stb.append(             "ELSE NULL END AS SCORE ");
            stb.append(       ",CASE WHEN ");
            stb.append(                   " W3.CLASSCD NOT IN ('90') ");  //05/03/03Modify 総合学習を追加
            stb.append(                   " AND W3.CLASSCD  || '-' || W3.SCHOOL_KIND || '-' ||  W3.CURRICULUM_CD  || '-' || W3.SUBCLASSCD NOT IN (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'D065') ");
            stb.append(                   " THEN ");
            stb.append(                  "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
            stb.append(                             "(SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
            stb.append(                             "VALUE(SEM1_REC_FLG,'0') = '0' THEN '()' ");

            if (param._semeFlg.equals("1")) {
                //１学期仕様
            } else if (param._semeFlg.equals("2") || param._grade.equals("03")) {
                //２学期仕様
                stb.append(                  "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
                stb.append(                       "(SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
                stb.append(                       "VALUE(SEM2_REC_FLG,'0') = '0' THEN '()' ");
            } else {
                //３学期仕様
                stb.append(                  "WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
                stb.append(                       "(SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
                stb.append(                       "VALUE(SEM2_REC_FLG,'0') = '0' THEN '()' ");
                stb.append(                  "WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND ");
                stb.append(                       "VALUE(SEM3_TERM_REC_FLG,'0') = '0' THEN '()' ");
            }
            stb.append(                  "WHEN GRADE_RECORD IS NOT NULL THEN ");
            stb.append(                       "CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS ");
            stb.append(                                          "WHEN 'B' THEN B_PATTERN_ASSESS ");
            stb.append(                                          "WHEN 'C' THEN C_PATTERN_ASSESS ELSE NULL END ");
            stb.append(                  "ELSE NULL END ");
            stb.append(             "ELSE RTRIM(CHAR(GRADE_RECORD)) END AS PATTERN_ASSESS ");
            if (sdiv == 0) {
                stb.append(    ", ''  AS SCORE_FLG, ");
                stb.append(    "''  AS DICD ");
            }
        }
        stb.append("FROM    KIN_RECORD_DAT W3 ");
        stb.append( join );
        stb.append("WHERE   W3.YEAR = '" + param._year + "' AND ");
        stb.append(        "EXISTS(SELECT  'X' FROM SCHNO_B W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
        return stb.toString();
    }

    /**
     *  PrepareStatement作成 成績総合データ
     *  => ps4
     *     2005/02/13Modify KIN_RECORD_DATの試験成績、学期成績、学年成績の取得仕様変更により修正
     */
    private String getStdTotalRecSql()
    {
        final StringBuffer stb = new StringBuffer();

        stb.append("WITH SCHNO_B AS(");
        stb.append(        "SELECT W1.YEAR,W1.SEMESTER,W1.SCHREGNO,W1.ATTENDNO,");
        stb.append(               "W1.GRADE,W1.HR_CLASS,W1.COURSECD,W1.MAJORCD,W1.COURSECODE ");
        stb.append(     "FROM    SCHREG_REGD_DAT   W1 ");
        stb.append(     "INNER   JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._year + "' ");  //<change specification of 05/09/28>
        stb.append(     "WHERE   W1.YEAR = '" + param._year + "' ");
        stb.append(         "AND W1.SEMESTER = '" + param._semeFlg + "' ");
        stb.append(         "AND W1.GRADE||W1.HR_CLASS = ? ");          //05/05/30
        stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
        stb.append(                            "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END) ");     //<change specification of 05/09/28>
        stb.append(                              "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END))) ");  //<change specification of 05/09/28>
        //05/03/09Modify 留学開始日が３学期の場合は成績も出力する
        //05/02/10Modify 停学を除外
        stb.append(        "AND NOT EXISTS( SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
        stb.append(                            "AND ((S1.TRANSFERCD IN ('2') AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //<change specification of 05/09/28>
        stb.append(                              "OR (S1.TRANSFERCD IN ('1') AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE AND S1.TRANSFER_EDATE < '" + param._transferDate + "' ))) ");  //<change specification of 05/09/28>
        stb.append(     "),");

        //成績データの表（通常科目）05/02/13  読替科目は含めない 05/03/10
        stb.append("RECORD_REC AS(");
        stb.append( prestatStdSubclassRecord( 1 ));
        stb.append("AND NOT EXISTS(SELECT 'X' ");
        stb.append(               "FROM   SUBCLASS_REPLACE_COMBINED_DAT W2 ");
        stb.append(               "WHERE  ");
        stb.append("  W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD = ");
        if (FLG == KNJD060K_GRADE) {
            stb.append("  W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || W2.ATTEND_SUBCLASSCD AND ");
        } else {
            stb.append("  W2.COMBINED_CLASSCD || '-' || W2.COMBINED_SCHOOL_KIND || '-' || W2.COMBINED_CURRICULUM_CD || '-' || W2.COMBINED_SUBCLASSCD AND ");
        }
        stb.append(                      "W2.YEAR ='" + param._year + "' AND ");
        stb.append(                      "REPLACECD = '1') ");  //05/05/22
        stb.append(     ") ");

        //メイン表
        stb.append("SELECT T1.SCHREGNO,");
        stb.append(          "T4.SUM_REC,T5.AVG_REC,T4.CREDITS,T4.CREDITS2,T5.RANK ");

        stb.append("FROM   SCHNO_B T1 ");
        //成績  04/11/03Modify  05/02/14 結果表RECORD_RECを使用
        stb.append(        "LEFT JOIN(");
        stb.append(            "SELECT  W1.SCHREGNO,");
        stb.append(                    "SUM(CASE WHEN D065.NAME1 IS NOT NULL THEN CAST(NULL AS INT) ELSE INT(SCORE) END)AS SUM_REC,");
        stb.append(                    "SUM(CREDITS)AS CREDITS,");
        if (param._semester.equals("9")) {
            stb.append(                  "SUM(CASE WHEN '1' < PATTERN_ASSESS THEN CREDITS ELSE 0 END)AS CREDITS2 ");
        } else {
            stb.append(             "0 AS CREDITS2 ");
        }
        stb.append(            "FROM    RECORD_REC W1 ");
        stb.append(                    "INNER JOIN SCHNO_B W2 ON W2.SCHREGNO = W1.SCHREGNO ");
        stb.append(                    "LEFT JOIN CREDIT_MST W3 ON W3.YEAR = '" + param._year + "' AND ");
        stb.append(                                               "W3.GRADE = '" + param._grade + "' AND ");   //05/05/22
        stb.append(                                               "W3.COURSECD = W2.COURSECD AND ");
        stb.append(                                               "W3.MAJORCD = W2.MAJORCD AND ");
        stb.append(                                               "W3.COURSECODE = W2.COURSECODE AND ");
        stb.append(                                             "  W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD = W1.SUBCLASSCD ");
        //D065
        stb.append(                    "LEFT JOIN NAME_MST D065 ON D065.NAMECD1 = 'D065' AND W1.SUBCLASSCD = D065.NAME1 ");

        stb.append(            "WHERE   SCORE IS NOT NULL AND SCORE NOT IN('KK','KS','( )','欠','公') AND ");  //05/03/01Modify
        stb.append(                 "SUBSTR(W1.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "' ");
        stb.append(            "GROUP BY W1.SCHREGNO");
        stb.append(     ")T4 ON T4.SCHREGNO = T1.SCHREGNO ");

        //平均点、席次の表  04/11/03 T4から分離  05/02/14 結果表RECORD_RECを使用
        stb.append(        "LEFT JOIN(");
        stb.append(            "SELECT  W1.SCHREGNO,");
        stb.append(                    "AVG(FLOAT(INT(SCORE))) AS AVG_REC,");                                 // 04/11/04Modify 04/11/08Modify
        stb.append(                 "CASE WHEN AVG(FLOAT(INT(SCORE))) IS NULL THEN NULL ELSE ");           // 04/12/06Modify
        stb.append(                    "RANK() OVER(ORDER BY CASE ");
        stb.append(                                   "WHEN AVG(FLOAT(INT(SCORE))) IS NULL THEN -1 ");
        stb.append(                                   "ELSE AVG(FLOAT(INT(SCORE))) END DESC)END AS RANK ");   // 04/11/01Modify 04/12/06Modify
        stb.append(            "FROM    RECORD_REC W1 ");
        //D065
        stb.append(                    "LEFT JOIN NAME_MST D065 ON D065.NAMECD1 = 'D065' AND W1.SUBCLASSCD = D065.NAME1 ");
        stb.append(            "WHERE   SCORE IS NOT NULL AND SCORE NOT IN('KK','KS','( )','欠','公') AND ");  //05/03/01Modify
        stb.append(                 "SUBSTR(W1.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "' AND ");
                                    //１科目でも欠席があれば除外 05/02/14Modify
        stb.append(                 "SCHREGNO NOT IN(SELECT SCHREGNO ");
        stb.append(                                 "FROM   RECORD_REC ");
        stb.append(                                 "WHERE  SCORE IS NOT NULL AND SCORE IN('KK', 'KS','( )','欠','公') AND ");  //05/03/01Modify
        stb.append(                                        "SUBSTR(SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "'  ");
        stb.append(                                 "GROUP BY SCHREGNO ");
        stb.append(                                 "HAVING 0 < COUNT(*) ");
        stb.append(                                 ") ");
        stb.append(                 "AND D065.NAME1 IS NULL ");

        stb.append(         "GROUP BY W1.SCHREGNO");
        stb.append(     ")T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        return stb.toString();

    }//prestatStdTotalRec()の括り

    /**
     *  SVF-FORM フィールドを初期化
     *    2005/05/22
     */
    private void clearSvfField(final HrClass hr)
    {
        log.debug("clearSvfField() check");
        try {
            svf.VrSetForm("KNJD060K.frm", 4);
            set_head2();

            svf.VrsOut("year2", KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度");    //年度
            svf.VrsOut("ymd1",  param._dateString);
            svf.VrsOut("DATE",  KNJ_EditDate.h_format_JP(param._attendSdate) + " \uFF5E " + KNJ_EditDate.h_format_JP(param._date));         //出欠集計範囲 04/12/06Add / NO100 Modify

            // 生徒名等出力
            for (final Iterator it = hr.hm1.values().iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final int len = getMS932ByteLength(student._name);
                final int i = student._schno - 1;
                svf.VrsOutn("name" + String.valueOf((i % 10) + 1) + (len > 24 ? "_3" : len > 20 ? "_2" : ""), (i / 10) + 1, student._name);
                svf.VrsOutn("REMARK1", i + 1, student._biko);
            }

            //    組名称及び担任名の取得
            svf.VrsOut("HR_NAME", (String) param._hrnameMap.get(hr._gradeHrclass));            //組名称
            svf.VrsOut("teacher", (String) param._staffnameMap.get(hr._gradeHrclass));            //担任名
        } catch (Exception ex) {
            log.warn("clearSvfField error! ", ex);
        }
    }

    private void arrayclear(final int[] arr) {
        if (null != arr) for (int i = 0; i < arr.length; i++) arr[i] = 0;
    }


    /**
     *  PrepareStatement作成 非皆勤者の抽出
     *   2005/07/04
     *   2005/10/05 編入（データ仕様の変更による）について修正、および転学を追加
     *   2005/10/05 学籍不在日数に編入と転入を追加
     *              忌引または出停の日は遅刻および早退をカウントしない
     *   2005/11/11 留学日数と休学日数のカウントを分ける => 留学は非皆勤の対象ではない
     */
    private String prestatAttendKaikin()
    {
        final StringBuffer stb = new StringBuffer();

        //対象生徒
        stb.append("WITH SCHNO AS(");
        stb.append(        "SELECT  W1.SCHREGNO ");
        stb.append(            ",CASE WHEN W4.SCHREGNO IS NOT NULL THEN 1 WHEN W5.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS LEAVE ");
        stb.append(     "FROM    SCHREG_REGD_DAT   W1 ");
        stb.append(     "INNER   JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param._year + "' ");  //<change specification of 05/09/28>

        stb.append(     "LEFT    JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
        stb.append(                                    "AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END) ");   //<change specification of 05/09/28>
        stb.append(                                      "OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END)) ");  //<change specification of 05/09/28>

        stb.append(     "LEFT    JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
        stb.append(                                        "AND ((W5.TRANSFERCD IN ('2') AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");   //<change specification of 05/09/28>
        stb.append(                                          "OR (W5.TRANSFERCD IN ('1') AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE)) ");  //<change specification of 05/09/28>

        stb.append(     "WHERE   W1.YEAR = '" + param._year + "' AND ");
        stb.append(             "W1.SEMESTER = '" + param._semeFlg + "' AND ");
        stb.append(             "W1.GRADE||W1.HR_CLASS = ? ");  //05/05/30
        stb.append(     "), ");

        stb.append(" TEST_COUNTFLG AS ( ");
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
        stb.append(" ), ");

        //対象講座の表
        stb.append("CHAIR_A AS(");
        stb.append(     "SELECT  K2.CHAIRCD,");
        stb.append("  K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
        stb.append(                         "K2.SUBCLASSCD AS SUBCLASSCD,K2.SEMESTER ");
        stb.append(     "FROM    CHAIR_CLS_DAT K1, CHAIR_DAT K2 ");
        stb.append(     "WHERE   K1.YEAR = '" + param._year + "' ");
        stb.append(         "AND K2.YEAR = '" + param._year + "' ");
        stb.append(         "AND K1.SEMESTER <= '" + param._semeFlg + "' ");  //05/11/25
        stb.append(         "AND K2.SEMESTER <= '" + param._semeFlg + "' ");  //05/11/25
        stb.append(         "AND K1.SEMESTER = K2.SEMESTER ");
        stb.append(         "AND (K1.CHAIRCD = '0000000' OR K1.CHAIRCD = K2.CHAIRCD) ");
        stb.append(         "AND K1.TRGTGRADE||K1.TRGTCLASS = ? ");                                    //05/05/30
        stb.append(         "AND K1.GROUPCD = K2.GROUPCD ");
        stb.append(     "GROUP BY K2.CHAIRCD, ");
        stb.append("  K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
        stb.append(                          "K2.SUBCLASSCD, K2.SEMESTER ");
        stb.append(     "),");

        //時間割の表
        stb.append("SCHEDULE_A AS(");
        stb.append(    "SELECT  T1.CHAIRCD, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV ");
        stb.append(    "FROM    SCH_CHR_DAT T1 ");
        stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
        stb.append(    "AND T1.EXECUTEDATE BETWEEN ? AND ? ");
        stb.append(       "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE T2.CHAIRCD = T1.CHAIRCD) ");  //05/10/07
        if (param._hasSchChrDatExecutediv) {
            stb.append("        AND VALUE(T1.EXECUTEDIV, '0') <> '2' "); // 休講は対象外
        }
        stb.append(     "),");

        stb.append(" SCHEDULE_R AS( ");
        stb.append(          "SELECT T2.SCHREGNO, T3.SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
        stb.append(                 "FROM   SCHEDULE_A T1,");
        stb.append(                        "CHAIR_STD_DAT T2, ");
        stb.append(                        "CHAIR_A T3 ");
        stb.append(                 "WHERE  T1.SEMESTER = T3.SEMESTER AND ");
        stb.append(                        "T1.CHAIRCD = T3.CHAIRCD AND ");
        stb.append(                        "T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND ");
        stb.append(                        "T2.YEAR = '" + param._year + "' AND ");
        stb.append(                        "T2.SEMESTER = T3.SEMESTER AND ");
        stb.append(                        "T2.CHAIRCD = T3.CHAIRCD AND ");
        stb.append(                        "T1.SEMESTER = T2.SEMESTER AND ");
        stb.append(                        "T1.CHAIRCD = T2.CHAIRCD ");
        stb.append(                    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T4 ");    //<change specification of 05/09/28>
        stb.append(                                   "WHERE   T4.SCHREGNO = T2.SCHREGNO ");
        stb.append(                                       "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");
        stb.append(                                         "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE ))) ");
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._year + "' AND L1.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        stb.append("                  ) ");
        // 勤怠コード'28'は時間割にカウントしない
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._year + "' AND L1.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND L1.REP_DI_CD = '28' ");
        stb.append("                  ) ");
        if (param.definecode.useschchrcountflg) {
                                        //SCHEDULE_A 表から条件を異動 05/11/25
            stb.append(                   "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
            stb.append(                                  "WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE ");
            stb.append(                                      "AND T4.PERIODCD = T1.PERIODCD ");
            stb.append(                                      "AND T4.CHAIRCD = T1.CHAIRCD ");
            stb.append(                                      "AND T4.GRADE||T4.HR_CLASS = ? "); //05/05/30
            stb.append(                                      "AND T4.COUNTFLG = '0')  ");       //05/05/22
            stb.append(                   "AND NOT EXISTS(SELECT  'X' FROM TEST_COUNTFLG TEST ");
            stb.append(                                  "WHERE ");
            stb.append(                                      "TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
            stb.append(                                      "AND TEST.PERIODCD = T1.PERIODCD ");
            stb.append(                                      "AND TEST.CHAIRCD  = T1.CHAIRCD ");
            stb.append(                                      "AND TEST.DATADIV  = T1.DATADIV) ");
        }
        stb.append(                 "GROUP BY T2.SCHREGNO, T3.SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
        stb.append(") ");

        //対象生徒の時間割データ   NO101 Modify
        stb.append(" , SCHEDULE_MAIN AS(");
        stb.append(     "SELECT  T1.* ");
        stb.append(     "FROM    SCHEDULE_R T1 ");
        stb.append(     "WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");    //--NO101 Build 'NOT EXISTS?'
        stb.append(                        "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
        stb.append(                            "AND T4.TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE ) ");
        stb.append(" ) ");

        // 休学中の授業日数
        stb.append(", SCH_TRANSFER AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     '00' AS DI_CD ");
        stb.append(" FROM ");
        stb.append("     SCHEDULE_R T1, ");
        stb.append("     SCHREG_TRANSFER_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("     T2.TRANSFERCD IN('2') ");
        stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     AND T1.EXECUTEDATE BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T1.SEMESTER ");
        stb.append(     ") ");

        //欠課数の表
        stb.append(", SUBCLASS_ATTEND_A AS(");
        stb.append(     "SELECT SCHREGNO, S1.SUBCLASSCD, ");
        stb.append(            "SUM(LATE_EARLY) AS LATE_EARLY, ");
        if (param._output3 == null) {
            stb.append(        "SUM(ABSENT1) AS ABSENT1 ");
        } else {
            stb.append(        "SUM( VALUE(ABSENT1,0) + VALUE(LATE_EARLY,0)/3 ) AS ABSENT1 ");
        }
                //出欠データより集計
        stb.append(     "FROM ( ");
        stb.append(          "SELECT S1.SCHREGNO,SUBCLASSCD,");
        stb.append(                 "SUM(CASE WHEN (CASE WHEN REP_DI_CD IN ('29','30','31') THEN VALUE(ATSUB_REPL_DI_CD, REP_DI_CD) ELSE REP_DI_CD END) IN( ");
        if (null != param._knjSchoolMst._subSuspend && param._knjSchoolMst._subSuspend.equals("1")) {
            stb.append(                 " '2', '9', ");
        }
        if (null != param._knjSchoolMst._subVirus && param._knjSchoolMst._subVirus.equals("1")) {
            stb.append(                 " '19', '20', ");
        }
        if (null != param._knjSchoolMst._subKoudome && param._knjSchoolMst._subKoudome.equals("1")) {
            stb.append(                 " '25', '26', ");
        }
        if (null != param._knjSchoolMst._subMourning && param._knjSchoolMst._subMourning.equals("1")) {
            stb.append(                 " '3', '10', ");
        }
        if (null != param._knjSchoolMst._subAbsent && param._knjSchoolMst._subAbsent.equals("1")) {
            stb.append(                 " '1', '8', ");
        }
        stb.append(                 "'4','5','6','14','11','12','13') THEN 1 ELSE 0 END)AS ABSENT1, ");
        stb.append(                 "SUM(CASE WHEN (CASE WHEN REP_DI_CD IN ('29','30','31') THEN VALUE(ATSUB_REPL_DI_CD, REP_DI_CD) ELSE REP_DI_CD END) IN('15','16','23','24') THEN SMALLINT(VALUE(L1.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
        stb.append(          "FROM SCHEDULE_MAIN S1 ");
        stb.append(          "INNER JOIN ATTEND_DAT S2 ON S2.YEAR = '" + param._year + "' AND ");
        stb.append(                                      "S2.ATTENDDATE = S1.EXECUTEDATE AND ");
        stb.append(                                      "S2.PERIODCD = S1.PERIODCD AND ");
        stb.append(                                      "S1.SCHREGNO = S2.SCHREGNO ");
        stb.append(          "INNER JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._year + "' AND L1.DI_CD = S2.DI_CD ");
        stb.append(                                      " AND L1.REP_DI_CD IN( ");
        if (null != param._knjSchoolMst._subSuspend && param._knjSchoolMst._subSuspend.equals("1")) {
            stb.append(                 " '2', '9', ");
        }
        if (null != param._knjSchoolMst._subVirus && param._knjSchoolMst._subVirus.equals("1")) {
            stb.append(                 " '19', '20', ");
        }
        if (null != param._knjSchoolMst._subKoudome && param._knjSchoolMst._subKoudome.equals("1")) {
            stb.append(                 " '25', '26', ");
        }
        if (null != param._knjSchoolMst._subMourning && param._knjSchoolMst._subMourning.equals("1")) {
            stb.append(                 " '3', '10', ");
        }
        if (null != param._knjSchoolMst._subAbsent && param._knjSchoolMst._subAbsent.equals("1")) {
            stb.append(                 " '1', '8', ");
        }
        stb.append(                                      "'4','5','6','14','15','16','11','12','13','23','24') ");
        stb.append(          "GROUP BY S1.SCHREGNO, SUBCLASSCD, SEMESTER ");

        if (null != param._knjSchoolMst._subOffDays && param._knjSchoolMst._subOffDays.equals("1")) {
            stb.append("UNION ALL ");
            stb.append("SELECT ");
            stb.append("    SCHREGNO, ");
            stb.append("    SUBCLASSCD, ");
            stb.append("    COUNT(*) AS ABSENT1, ");
            stb.append("    0 AS LATE_EARLY ");
            stb.append("FROM SCH_TRANSFER ");
            stb.append("GROUP BY ");
            stb.append("    SCHREGNO, ");
            stb.append("    SUBCLASSCD, ");
            stb.append("    SEMESTER ");
        }

        //月別科目別出欠集計データより欠課を取得  2005/01/04Modify
        stb.append(          "UNION ALL ");
        stb.append(             "SELECT  W1.SCHREGNO, ");
        stb.append("  W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(                                   " W1.SUBCLASSCD AS SUBCLASSCD , ");
        stb.append(                  "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0) ");
        if (null != param._knjSchoolMst._subOffDays && param._knjSchoolMst._subOffDays.equals("1")) {
            stb.append(                 " + VALUE(OFFDAYS,0) ");
        }
        if (null != param._knjSchoolMst._subSuspend && param._knjSchoolMst._subSuspend.equals("1")) {
            stb.append("            + VALUE(SUSPEND, 0) ");
        }
        if (null != param._knjSchoolMst._subVirus && param._knjSchoolMst._subVirus.equals("1")) {
            stb.append("            + VALUE(VIRUS, 0) ");
        }
        if (null != param._knjSchoolMst._subKoudome && param._knjSchoolMst._subKoudome.equals("1")) {
            stb.append("            + VALUE(KOUDOME, 0) ");
        }
        if (null != param._knjSchoolMst._subMourning && param._knjSchoolMst._subMourning.equals("1")) {
            stb.append("            + VALUE(MOURNING, 0) ");
        }
        if (null != param._knjSchoolMst._subAbsent && param._knjSchoolMst._subAbsent.equals("1")) {
            stb.append("            + VALUE(ABSENT, 0) ");
        }
        stb.append(                  ") AS ABSENT1, ");
        stb.append(                  "SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
        stb.append(             "FROM    ATTEND_SUBCLASS_DAT W1, SCHNO W2 ");
        stb.append(             "WHERE   W1.YEAR = '" + param._year + "' AND ");
        stb.append(                  "W1.SEMESTER <= '" + param._semester + "' AND ");
        stb.append(                  "W1.SEMESTER||W1.MONTH <= ? AND ");
        stb.append(                  "W1.SCHREGNO = W2.SCHREGNO ");
        stb.append(             "GROUP BY W1.SCHREGNO, ");
        stb.append("  W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        stb.append(                                    "W1.SUBCLASSCD, W1.SEMESTER ");
        stb.append(          ")S1 ");
        stb.append(     "GROUP BY S1.SCHREGNO, SUBCLASSCD ");
        stb.append(     "), ");

        stb.append("SUBCLASS_ATTEND_B AS(");
        stb.append(     "SELECT  SCHREGNO, SUM(ABSENT1) AS ABSENT1, SUM(LATE_EARLY) AS LATE_EARLY ");
        stb.append(        "FROM    SUBCLASS_ATTEND_A ");
        stb.append(        "GROUP BY SCHREGNO ");
        stb.append(        "HAVING 0 < SUM(ABSENT1) OR 0 < SUM(LATE_EARLY) ");
        stb.append(        "), ");

        //対象生徒の時間割データ   NO101 Modify SCHEDULE_SCHREGをRENAME
        stb.append("SCHEDULE_SCHREG_R AS(");
        stb.append(     "SELECT  T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
        stb.append(     "FROM    SCHEDULE_A T1, CHAIR_STD_DAT T2 ");    //05/10/07
        stb.append(     "WHERE   T2.YEAR = '" + param._year + "' ");
        stb.append(         "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
        stb.append(         "AND T1.SEMESTER = T2.SEMESTER ");
        stb.append(         "AND T1.CHAIRCD = T2.CHAIRCD ");
        stb.append(         "AND T2.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) ");
        stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T3 ");    //05/10/07 Build 'NOT EXISTS〜'
        stb.append(                        "WHERE   T3.SCHREGNO = T2.SCHREGNO ");
        stb.append(                            "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");
        stb.append(                              "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE ))) ");
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._year + "' AND L1.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        stb.append("                  ) ");
        // 勤怠コード'28'は時間割にカウントしない
        stb.append("    AND NOT EXISTS(SELECT ");
        stb.append("                       'X' ");
        stb.append("                   FROM ");
        stb.append("                       ATTEND_DAT T4 ");
        stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._year + "' AND L1.DI_CD = T4.DI_CD ");
        stb.append("                   WHERE ");
        stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
        stb.append("                       AND L1.REP_DI_CD = '28' ");
        stb.append("                  ) ");
        stb.append(     "GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
        stb.append(     "), ");

        //対象生徒の時間割データ   NO101 Modify
        stb.append("SCHEDULE_SCHREG AS(");
        stb.append(     "SELECT  T1.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
        stb.append(     "FROM    SCHEDULE_SCHREG_R T1 ");
        stb.append(     "WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");    //--NO101 Build 'NOT EXISTS〜'
        stb.append(                        "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
        stb.append(                            "AND T4.TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE ) ");
        stb.append(     "), ");

        //対象生徒の出欠データ
        stb.append("T_ATTEND_DAT AS(");
        stb.append(     "SELECT  T0.SCHREGNO, T0.ATTENDDATE, T0.PERIODCD, T2.REP_DI_CD ");
        stb.append(     "FROM    ATTEND_DAT T0 ");
        stb.append("             INNER JOIN ATTEND_DI_CD_DAT T2 ON T2.YEAR = '" + param._year + "' AND T2.DI_CD = T0.DI_CD,");
        stb.append(             "SCHEDULE_SCHREG T1 ");
        stb.append(     "WHERE   T0.YEAR = '" + param._year + "' AND ");
        stb.append(             "T0.ATTENDDATE BETWEEN ? AND ? AND ");
        stb.append(             "T0.SCHREGNO = T1.SCHREGNO AND ");
        stb.append(             "T0.ATTENDDATE = T1.EXECUTEDATE AND ");
        stb.append(             "T0.PERIODCD = T1.PERIODCD ");
        stb.append(     "), ");

        //対象生徒の出欠データ（忌引・出停した日）//<change specification of 05/09/28>
        stb.append("T_ATTEND_DAT_B AS(");
        stb.append(     "SELECT  T0.SCHREGNO, T0.ATTENDDATE ");
        stb.append(     "FROM    T_ATTEND_DAT T0 ");
        stb.append(     "WHERE   REP_DI_CD IN('2','3','9','10') ");
        stb.append(     "GROUP BY T0.SCHREGNO, T0.ATTENDDATE ");
        stb.append(     "), ");

        //対象生徒の日単位の最小校時・最大校時・校時数
        stb.append("T_PERIOD_CNT AS(");
        stb.append(     "SELECT  T1.SCHREGNO, T1.EXECUTEDATE, ");
        stb.append(             "MIN(T1.PERIODCD) AS FIRST_PERIOD, ");
        stb.append(             "MAX(T1.PERIODCD) AS LAST_PERIOD, ");
        stb.append(             "COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ");
        stb.append(     "FROM    SCHEDULE_SCHREG T1 ");
        stb.append(     "GROUP BY T1.SCHREGNO, T1.EXECUTEDATE ");
        stb.append(     "), ");

        //留学日数を算出 05/02/02 休学を含める 05/03/09
        stb.append("TRANSFER_SCHREG AS(");
        stb.append(     "SELECT  T3.SCHREGNO, T3.TRANSFERCD, COUNT(DISTINCT T1.EXECUTEDATE)AS TRANSFER_DATE "); //05/03/09
        stb.append(     "FROM    SCHREG_TRANSFER_DAT T3, SCHEDULE_SCHREG_R T1 ");   //<change specification of 05/09/28>  NO101
        stb.append(     "WHERE   T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(         "AND T3.TRANSFERCD IN('1','2') ");   //05/03/09 KYUGAKU
        stb.append(         "AND T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE ");
        stb.append(     "GROUP BY T3.SCHREGNO, T3.TRANSFERCD ");                                //05/03/09
        stb.append(     ") ");

        //メイン表
        stb.append(   "SELECT  TT0.SCHREGNO, ");
        stb.append(           "TT0.LEAVE, ");
                                //授業日数
                                //出席すべき日数
        stb.append(           "( VALUE(TT1.LESSON,0) - VALUE(TT3.SUSPEND,0) ");
        if ("true".equals(param._useVirus)) {
            stb.append(            " - VALUE(TT7.VIRUS,0) ");
        }
        if ("true".equals(param._useKoudome)) {
            stb.append(            " - VALUE(TT7.KOUDOME,0) ");
        }
        stb.append(             " - VALUE(TT4.MOURNING,0)) ");  //NO101
        if (null != param._knjSchoolMst._semOffDays && param._knjSchoolMst._semOffDays.equals("1")) {
            stb.append(                 " + VALUE(TT9.TRANSFER_DATE,0) ");
        }
        stb.append(            " + ( VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) ");
        if ("true".equals(param._useVirus)) {
            stb.append(            " - VALUE(TT7.VIRUS,0) ");
        }
        if ("true".equals(param._useKoudome)) {
            stb.append(            " - VALUE(TT7.KOUDOME,0) ");
        }
        stb.append(            " - VALUE(TT7.MOURNING,0)) AS MLESSON, "); //<change specification of 05/09/28>
                                //各種欠席日数
        stb.append(           "VALUE(TT3.SUSPEND,0) + VALUE(TT7.SUSPEND,0) ");
        if ("true".equals(param._useVirus)) {
            stb.append(            " + VALUE(TT3_1.VIRUS,0) + VALUE(TT7.VIRUS,0) ");
        }
        if ("true".equals(param._useKoudome)) {
            stb.append(            " + VALUE(TT3_2.KOUDOME,0) + VALUE(TT7.KOUDOME,0) ");
        }
        stb.append(           "AS SUSPEND, ");
        stb.append(           "VALUE(TT4.MOURNING,0) + VALUE(TT7.MOURNING,0) AS MOURNING, ");
        stb.append(           "VALUE(TT5.SICK,0) + VALUE(TT5.NOTICE,0) + VALUE(TT5.NONOTICE,0) + VALUE(TT7.ABSENT,0) ");
        if (null != param._knjSchoolMst._semOffDays && param._knjSchoolMst._semOffDays.equals("1")) {
            stb.append(                 " + VALUE(TT9.TRANSFER_DATE,0) ");
        }
        stb.append(           " AS ABSENT, ");
                                //出席日数
                                //遅刻・早退
        stb.append(           "VALUE(TT6.LATE,0) + VALUE(TT10.LATE,0) + VALUE(TT7.LATE,0) AS LATE, ");     //05/03/04Modify
        stb.append(           "VALUE(TT6.EARLY,0) + VALUE(TT11.EARLY,0) + VALUE(TT7.EARLY,0) AS EARLY, "); //05/03/04Modify
                                //留学日数
        stb.append(           "VALUE(TT8.TRANSFER_DATE,0) + VALUE(TT7.ABROAD,0) AS RTRANSFER_DATE, ");   //05/11/25 Modify
                                //休学日数
        stb.append(           "VALUE(TT9.TRANSFER_DATE,0) + VALUE(TT7.OFFDAYS,0) AS KTRANSFER_DATE, ");  //05/11/25 Modify
                                //不在日数
                                //欠課および授業の遅刻・早退
        stb.append(              "TT13.ABSENT1, TT13.LATE_EARLY ");
        stb.append(   "FROM    SCHNO TT0 ");
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
        stb.append(      "GROUP BY SCHREGNO ");
        stb.append(      ") TT3 ON TT0.SCHREGNO = TT3.SCHREGNO ");
        stb.append(   "LEFT OUTER JOIN(");
        stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS VIRUS ");
        stb.append(      "FROM   T_ATTEND_DAT ");
        stb.append(      "WHERE  REP_DI_CD IN ('19','20') ");
        stb.append(      "GROUP BY SCHREGNO ");
        stb.append(      ") TT3_1 ON TT0.SCHREGNO = TT3_1.SCHREGNO ");
        stb.append(   "LEFT OUTER JOIN(");
        stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS KOUDOME ");
        stb.append(      "FROM   T_ATTEND_DAT ");
        stb.append(      "WHERE  REP_DI_CD IN ('25','26') ");
        stb.append(      "GROUP BY SCHREGNO ");
        stb.append(      ") TT3_2 ON TT0.SCHREGNO = TT3_2.SCHREGNO ");
        //個人別忌引日数
        stb.append(   "LEFT OUTER JOIN(");
        stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS MOURNING ");
        stb.append(      "FROM   T_ATTEND_DAT ");
        stb.append(      "WHERE  REP_DI_CD IN ('3','10') ");
        stb.append(      "GROUP BY SCHREGNO ");
        stb.append(      ") TT4 ON TT0.SCHREGNO = TT4.SCHREGNO ");
        //個人別欠席日数
        stb.append(   "LEFT OUTER JOIN(");
        stb.append(      "SELECT  W0.SCHREGNO, ");
        stb.append(              "SUM(CASE W2.REP_DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS SICK, ");
        stb.append(              "SUM(CASE W2.REP_DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS NOTICE, ");
        stb.append(              "SUM(CASE W2.REP_DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS NONOTICE ");
        stb.append(      "FROM    ATTEND_DAT W0 ");
        stb.append(         "INNER JOIN (");
        stb.append(         "SELECT  T0.SCHREGNO, T0.EXECUTEDATE, T0.FIRST_PERIOD ");
        stb.append(         "FROM    T_PERIOD_CNT T0, ");
        stb.append(            "(");
        stb.append(            "SELECT  W1.SCHREGNO, W1.ATTENDDATE, ");
        stb.append(                    "MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
        stb.append(                    "COUNT(W1.PERIODCD) AS PERIOD_CNT ");
        stb.append(            "FROM    T_ATTEND_DAT W1 ");
        stb.append(            "WHERE   W1.REP_DI_CD IN ('4','5','6','11','12','13') ");
        stb.append(            "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
        stb.append(            ") T1 ");
        stb.append(         "WHERE   T0.SCHREGNO = T1.SCHREGNO AND ");
        stb.append(                 "T0.EXECUTEDATE = T1.ATTENDDATE AND ");
        stb.append(                 "T0.FIRST_PERIOD = T1.FIRST_PERIOD AND ");
        stb.append(                 "T0.PERIOD_CNT = T1.PERIOD_CNT ");
        stb.append(         ") W1 ON W0.SCHREGNO = W1.SCHREGNO AND ");
        stb.append(              "W0.ATTENDDATE = W1.EXECUTEDATE AND ");
        stb.append(              "W0.PERIODCD = W1.FIRST_PERIOD ");
        stb.append(         "INNER JOIN ATTEND_DI_CD_DAT W2 ON W2.YEAR = '" + param._year + "' AND W2.DI_CD = W0.DI_CD ");
        stb.append(      "GROUP BY W0.SCHREGNO ");
        stb.append(      ")TT5 ON TT0.SCHREGNO = TT5.SCHREGNO ");
        //個人別遅刻・早退回数
        stb.append(   "LEFT OUTER JOIN(");
        stb.append(      "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS LATE, COUNT(T3.ATTENDDATE) AS EARLY ");
        stb.append(      "FROM    T_PERIOD_CNT T0 ");
        stb.append(      "INNER JOIN(");
        stb.append(         "SELECT  W1.SCHREGNO, W1.ATTENDDATE, COUNT(W1.PERIODCD) AS PERIOD_CNT ");
        stb.append(         "FROM    T_ATTEND_DAT W1 ");
        stb.append(         "WHERE   W1.REP_DI_CD NOT IN ('0','14','15','16','23','24') ");    //05/03/04Modify
        stb.append(             "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");                                  //<change specification of 05/09/28>
        stb.append(                            "WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.ATTENDDATE = W1.ATTENDDATE) "); //<change specification of 05/09/28>
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

        //個人別遅刻回数 05/03/04
        stb.append(   "LEFT OUTER JOIN(");
        stb.append(      "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS LATE ");
        stb.append(      "FROM    T_PERIOD_CNT T0 ");
        stb.append(      "INNER JOIN(");
        stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
        stb.append(         "FROM    T_ATTEND_DAT W1 ");
        stb.append(         "WHERE   REP_DI_CD IN ('15','23','24') ");
        stb.append(             "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");                                  //<change specification of 05/09/28>
        stb.append(                            "WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.ATTENDDATE = W1.ATTENDDATE) "); //<change specification of 05/09/28>
        stb.append(         ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ");
        stb.append(                                              "T0.FIRST_PERIOD = T2.PERIODCD ");
        stb.append(      "GROUP BY T0.SCHREGNO ");
        stb.append(      ")TT10 ON TT0.SCHREGNO = TT10.SCHREGNO ");

        //個人別早退回数 05/03/04
        stb.append(   "LEFT OUTER JOIN(");
        stb.append(      "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS EARLY ");
        stb.append(      "FROM    T_PERIOD_CNT T0 ");
        stb.append(      "INNER JOIN(");
        stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
        stb.append(         "FROM    T_ATTEND_DAT W1 ");
        stb.append(         "WHERE   REP_DI_CD IN ('16') ");
        stb.append(             "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");                                  //<change specification of 05/09/28>
        stb.append(                            "WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.ATTENDDATE = W1.ATTENDDATE) "); //<change specification of 05/09/28>
        stb.append(         ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ");
        stb.append(                                              "T0.LAST_PERIOD = T2.PERIODCD ");
        stb.append(      "GROUP BY T0.SCHREGNO ");
        stb.append(      ")TT11 ON TT0.SCHREGNO = TT11.SCHREGNO ");

        //月別集計データから集計した表
        stb.append(   "LEFT JOIN(");
        stb.append(      "SELECT SCHREGNO, ");
        stb.append(                   "VALUE(SUM(LESSON),0) - VALUE(SUM(OFFDAYS),0) - VALUE(SUM(ABROAD),0) ");
        if (null != param._knjSchoolMst._semOffDays && param._knjSchoolMst._semOffDays.equals("1")) {
            stb.append(                   "+ VALUE(SUM(OFFDAYS),0) ");
        }
        stb.append("                  AS LESSON, ");  //05/10/07 Modify
        stb.append(                   "SUM(MOURNING) AS MOURNING, ");
        stb.append(                   "SUM(SUSPEND) AS SUSPEND, ");
        if ("true".equals(param._useVirus)) {
            stb.append(                   "SUM(VIRUS) AS VIRUS, ");
        } else {
            stb.append(                   "0 AS VIRUS, ");
        }
        if ("true".equals(param._useVirus)) {
            stb.append(                   "SUM(KOUDOME) AS KOUDOME, ");
        } else {
            stb.append(                   "0 AS KOUDOME, ");
        }
        stb.append(                   "SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
        if (null != param._knjSchoolMst._semOffDays && param._knjSchoolMst._semOffDays.equals("1")) {
            stb.append(                   "+ VALUE(OFFDAYS,0) ");
        }
        stb.append(                   ") AS ABSENT, ");
        stb.append(                   "SUM(LATE) AS LATE, ");
        stb.append(                   "SUM(EARLY) AS EARLY, ");
        stb.append(                   "SUM(ABROAD) AS ABROAD, ");   //<change specification of 05/09/28>
        stb.append(                   "SUM(OFFDAYS) AS OFFDAYS ");  //05 10 05Build
        stb.append(            "FROM   ATTEND_SEMES_DAT W1 ");
        stb.append(            "WHERE  YEAR = '" + param._year + "' AND ");
        stb.append(                   "SEMESTER <= '" + param._semester + "' AND ");
        stb.append(                   "SEMESTER||MONTH <= ? AND ");
        stb.append(                   "EXISTS(");
        stb.append(                       "SELECT  'X' ");
        stb.append(                       "FROM    SCHNO W2 ");
        stb.append(                       "WHERE   W1.SCHREGNO = W2.SCHREGNO)");
        stb.append(            "GROUP BY SCHREGNO ");
        stb.append(      ")TT7 ON TT0.SCHREGNO = TT7.SCHREGNO ");

        //留学日数の表
        //  05/11/11Modify 留学と休学を分ける
        stb.append(   "LEFT JOIN(");
        stb.append(      "SELECT SCHREGNO, SUM(TRANSFER_DATE) AS TRANSFER_DATE ");
        stb.append(      "FROM   TRANSFER_SCHREG ");
        stb.append(      "WHERE  TRANSFERCD = '1' ");
        stb.append(      "GROUP BY SCHREGNO ");
        stb.append(   ")TT8 ON TT8.SCHREGNO = TT0.SCHREGNO ");

        //休学日数の表
        //  05/11/11Modify 留学と休学を分ける
        stb.append(   "LEFT JOIN(");
        stb.append(      "SELECT SCHREGNO, SUM(TRANSFER_DATE) AS TRANSFER_DATE ");
        stb.append(      "FROM   TRANSFER_SCHREG ");
        stb.append(      "WHERE  TRANSFERCD = '2' ");
        stb.append(      "GROUP BY SCHREGNO ");
        stb.append(   ")TT9 ON TT9.SCHREGNO = TT0.SCHREGNO ");

        //欠課の表
        stb.append(   "LEFT JOIN SUBCLASS_ATTEND_B TT13 ON TT13.SCHREGNO = TT0.SCHREGNO ");

        stb.append("ORDER BY SCHREGNO");
        log.debug("statment=" + stb.toString());
        return stb.toString();
    }


    /**
     *  非皆勤者をリスト<List nokaikin>に保管
     *   2005/11/11 留学日数と休学日数のカウントを分ける => 留学は非皆勤の対象ではない
     */
    private List getAttendNoKaikin(final DB2UDB db2, final HrClass hr)
    {
        final List nokaikin = new ArrayList();
        ResultSet rrs = null;
        try {
            if (null == param.arrps5) {
                sqlAttendKaikin = prestatAttendKaikin();
                param.arrps5 = db2.prepareStatement(sqlAttendKaikin);        //非皆勤者 05/07/04Build
            }
            PreparedStatement ps = param.arrps5;
            int p = 0;
            ps.setString(++p, hr._gradeHrclass );            //学年・組
            ps.setString(++p, hr._gradeHrclass );            //学年・組
            ps.setDate(++p, java.sql.Date.valueOf(param._divideAttendDate));            //出欠データ集計開始日付
            ps.setDate(++p, java.sql.Date.valueOf(param._date ));            //出欠データ集計終了日付
            if (param.definecode.useschchrcountflg) {
                ps.setString(++p, hr._gradeHrclass );        //学年・組
            }
            ps.setString(++p, param._divideAttendMonth);            //出欠集計データ終了学期＋月
            ps.setDate(++p, java.sql.Date.valueOf(param._divideAttendDate));            //出欠データ集計開始日付
            ps.setDate(++p, java.sql.Date.valueOf(param._date ));            //出欠データ集計終了日付
            ps.setString(++p, param._divideAttendMonth);            //出欠集計データ終了学期＋月
log.debug("kaikin ps start");
            rrs = ps.executeQuery();
log.debug("kaikin ps end");

            while(rrs.next()) {
                final Map rs = createMap(rrs);
                if (getString(rs, "MLESSON") != null  &&  0 < Integer.parseInt(getString(rs, "MLESSON"))) {
                    if (getString(rs, "ABSENT")        != null  &&  0 < Integer.parseInt(getString(rs, "ABSENT"))  ||
                        getString(rs, "EARLY")         != null  &&  0 < Integer.parseInt(getString(rs, "EARLY"))  ||
                        getString(rs, "LATE")          != null  &&  0 < Integer.parseInt(getString(rs, "LATE"))  ||
                        getString(rs, "KTRANSFER_DATE") != null  &&  0 < Integer.parseInt(getString(rs, "KTRANSFER_DATE"))  ||    //05/11/11Modify 休学日数のみ対処とする
                        getString(rs, "LATE_EARLY")    != null  &&  0 < Integer.parseInt(getString(rs, "LATE_EARLY"))  ||
                        getString(rs, "ABSENT1")       != null  &&  0 < Integer.parseInt(getString(rs, "ABSENT1"))) {
                        if (! nokaikin.contains(getString(rs, "SCHREGNO"))) { //要素がない場合追加
                            nokaikin.add(getString(rs, "SCHREGNO"));
                        }
                    }
                }

                if (getString(rs, "LEAVE") != null  &&  Integer.parseInt(getString(rs, "LEAVE")) == 1) {
                        if (! nokaikin.contains(getString(rs, "SCHREGNO"))) {  //要素がない場合追加
                            nokaikin.add(getString(rs, "SCHREGNO"));
                        }
                }
            }

        } catch (Exception ex) {
            log.error("error! ", ex);
        }
        return nokaikin;
    }


    /**
     *  欠課時数の印刷: 欠課時数要注意者または超過者は'true'を返す
     *  2005/11/16 Build 欠課時数の網掛け処理を学期成績にも適用する(学年成績のみであったが)ため処理を独立
     *  引数について
     *      int i: 出力行 / int j: 出力列 / int subclassjisu: 科目別単位数
     */
    private boolean printSvfStdDetailKekka(final SubclassScore rs, final int i, final int j, final SubclassInfo si)
    {
        boolean amikake = false;

        try {
            //欠課出力/網掛け設定/履修単位の加算処理
            if (rs._absent1 != null) {
                if (Integer.parseInt(param._grade) == 3) {   //05/05/22
                    //３年生の欠課時数網掛け設定
                    if (Integer.parseInt(param._semeFlg) == 1) {
                         if (getInt(rs._absent1) >= si.subclassjisu * 8 * Integer.parseInt(param._semeFlg) / 2 + 1) {
                             amikake = true;
                         }
                    }else {
                         if (getInt(rs._absent1) >= si.subclassjisu * 8 + 1) {
                             amikake = true;
                         }
                    }
                } else {
                    //1・２年生の欠課時数網掛け設定
                    if (Integer.parseInt(param._semeFlg) < 3) {
                         if (getInt(rs._absent1) >= si.subclassjisu * 10 * Integer.parseInt(param._semeFlg) / 3 + 1) {
                             amikake = true;
                         }
                    } else {
                         if (getInt(rs._absent1) >= si.subclassjisu * 10 + 1) {
                             amikake = true;
                         }
                    }
                }
                if (amikake) {
                    svf.VrAttributen("kekka" + i, j, "Paint=(2,70,1),Bold=1");
                }
                if (Integer.parseInt(rs._absent1) != 0) {
                    svf.VrsOutn("kekka" + i, j, rs._absent1); //欠課
                }
            }

            //網掛け解除
            if (amikake) {
                svf.VrAttributen("kekka" + i, j, "Paint=(0,0,0),Bold=0");
            }

        } catch (Exception ex) {
            log.error("printSvfStdDetailKekka error! ", ex);
        }
        return amikake;
    }


    /**
     *  履修単位の累積処理: 履修単位＝通常科目＋読替後科目−欠課時数超過科目
     *  2005/11/16 Build 履修単位印刷を中間・期末・学期成績にも適用する(学年成績のみであったが)ため処理を独立
     *  引数について
     *      int i: 出力行 / int subclassjisu: 科目別単位数
     */
    private void accumuStdRCredits(final HrClass hr, final SubclassScore rs, final Student student, final SubclassInfo si)
    {
        boolean rflg = true;
        final int s = (Integer.parseInt( param._grade ) == 3) ? 8 : 10; //param._2: 学年

        if (si.replaceflgMoto()) {
            rflg = false;
        } else {
            if (rs._absent1 != null  &&  (si.subclassjisu * s + 1) <=  Integer.parseInt(rs._absent1)) {
                rflg = false;
            }
            getMappedList(student.sch_rcredits_hr, si._subclasscd).add(String.valueOf(si.subclassjisu)); //履修単位
        }

        if (rflg) {
            getMappedList(student.sch_rcredits, si._subclasscd).add(String.valueOf(si.subclassjisu)); //履修単位
        }
    }

    private static class HrClass {
        final String _gradeHrclass;

        int hr_total;                                   //クラス平均総合点
        //float hr_average;                             //クラス平均平均点
        int[] hr_lesson = new int[2];                   //クラス授業日数{最大、最小}  04/11/01Add
        int[] hr_attend = new int[9];                   //クラス平均出欠格納
        int[] hr_seitosu = new int[12];                 //クラス平均対象生徒数格納
        int hr_credits;                                 //クラスの履修単位数   05/01/31
        List subclasstotalnum = new ArrayList();        //科目別得点累積 05/03/09
        List hrtotalnum = new ArrayList();              //全科目得点累積     05/03/10
        //NO106 List subclassgroup = new ArrayList();           //NO105 ゼロではないグループコードを格納

        final Map hm1 = new HashMap(50);              //学籍番号と行番号の保管

        HrClass(final String gradeHrClass) {
            _gradeHrclass = gradeHrClass;
        }

        public static int avg(final List scoreList) {
            final int avg = Math.round((float) sum(scoreList) / (float) scoreList.size());
            return avg;
        }

        public static int sum(final List scoreList) {
            int sum = 0;
            for (final Iterator it = scoreList.iterator(); it.hasNext();) {
                final String score = (String) it.next();
                sum += Integer.parseInt(score);
            }
            return sum;
        }
    }

    private static class Student {
        final int _schno;
        final String _schregno;
        String _name;
        String _biko;

        String sumRec;
        String avgRec;
        String rank;

        Map sch_ccredits = new TreeMap();               //生徒ごとの修得単位数 05/01/31
        Map sch_rcredits = new TreeMap();               //生徒ごとの履修単位数 05/01/31
        Map sch_rcredits_hr = new TreeMap();            //欠課超過単位を除外しない生徒ごとの履修単位数 NO106

        Student(final int schno, final String schregno) {
            _schno = schno;
            _schregno = schregno;
        }
        private static int sum(final Map map) {
            int sum = 0;
            for (final Iterator it = map.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                //final String subclasscd = (String) e.getKey();
                final List creditList = (List) e.getValue();
                for (int i = 0; i < creditList.size(); i++) {
                    sum += Integer.parseInt((String) creditList.get(i));
                }
            }
            return sum;
        }
    }

    private static class SubclassInfo {
        final String _subclasscd;
        final String _groupcd;
        final String _subclassname;
        final String _electdiv;
        final String _classabbv;
        final String _credits;
        final String _abgHr;
        final String _sumHr;
        final String _assessGr;
        final String _jisu;
        final String _rsReplaceflg;
        //int replaceflg = 0;             //評価読替元科目:1  評価読替先科目:-1  05/11/16
        final int subclassjisu;           //科目の授業時数 -> 単位数へ変更
        final int assesspattern;          //科目の評定類型 A:0 B:1 C:2
        final boolean _d065;
        SubclassInfo(final Map rs) {
            _subclasscd = getString(rs, "SUBCLASSCD");
            _groupcd = getString(rs, "GROUPCD");
            _subclassname = getString(rs, "SUBCLASSNAME");
            _electdiv = getString(rs, "ELECTDIV");
            _classabbv = getString(rs, "CLASSABBV");
            _credits = getString(rs, "CREDITS");
            _abgHr = getString(rs, "AVG_HR");
            _sumHr = getString(rs, "SUM_HR");
            _assessGr = getString(rs, "ASSESS_GR");
            _jisu = getString(rs, "JISU");
            _rsReplaceflg = getString(rs, "REPLACEFLG");

            subclassjisu = Integer.parseInt(StringUtils.defaultString(_credits, "0"));
            assesspattern = ("C".equals(_assessGr)) ? 2 : ("B".equals(_assessGr)) ? 1 : 0;
            _d065 = 1 == getInt(rs, "D065_ARI") ? true : false;
        }

//        int replaceflg() {
//            int replaceflg = 0;        //05/11/16
//            if (_credits == null) {
//                return replaceflg;
//            }
//            //読替科目フラグを設定 0:通常科目 1:読替元 -1:読替先  05/11/16
//            if ("SAKI".equals(_rsReplaceflg)) {
//                replaceflg = -1;
//            } else if ("MOTO".equals(_rsReplaceflg)) {
//                replaceflg = 1;
//            }
//            return replaceflg;
//        }

        boolean replaceflgSaki() {
            if (_credits == null) {
                return false;
            }
            return "SAKI".equals(_rsReplaceflg);
        }

        boolean replaceflgMoto() {
            if (_credits == null) {
                return false;
            }
            return "MOTO".equals(_rsReplaceflg);
        }
    }

    private static class SubclassScore {
        final String _schregno;
        final String _subclasscd;
        final String _score;
        final String _absent1;
        final String _absent2;
        final String _patternAssess;
        final String _scoreFlg;
        final String _dicd;
        String _replaceMoto;
        final boolean _d065;
        SubclassScore(final Param param, final Map rs) {
            _schregno = getString(rs, "SCHREGNO");
            _subclasscd = getString(rs, "SUBCLASSCD");
            _score = getString(rs, "SCORE");
            _absent1 = getString(rs, "ABSENT1");
            _absent2 = getString(rs, "ABSENT2");
            _patternAssess = getString(rs, "PATTERN_ASSESS");
            _scoreFlg = getString(rs, "SCORE_FLG");
            _dicd = getString(rs, "DICD");
            if (param._testkindcd.equals("0")) {
                _replaceMoto = getString(rs, "REPLACEMOTO");
            }
            _d065 = 1 == getInt(rs, "D065_ARI") ? true : false;
        }
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _grade;
        final String _date;
        final String _output3;
        String _semeSdate;
        String _semeEdate;
        final String _output4;
        final String _testkindcd;
        String _semesterName;
        String _semeFlg;
        final String _divideAttendDate;
        final String _divideAttendMonth;
        String _transferDate;
        String _dateString;
        String _attendSdate;
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        PreparedStatement arrps0;
        PreparedStatement arrps1;
        PreparedStatement arrps2;
        PreparedStatement arrps3;
        PreparedStatement arrps4;
        PreparedStatement arrps5;
        KNJDefineSchool definecode;     //各学校における定数等設定 05/05/22
        KNJSchoolMst _knjSchoolMst;    //学校マスタ
        
        private Map _hrnameMap;
        private Map _staffnameMap;

        /** 出欠状況取得引数 */
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;
        private String _sDate;
        private final String SSEMESTER = "1";
        private boolean _hasSchChrDatExecutediv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {

            _year = request.getParameter("YEAR");                                    //年度
            _semester = (!(request.getParameter("SEMESTER").equals("4"))) ? request.getParameter("SEMESTER") : "9";                      //1-3:学期 9:学年末
            _grade = request.getParameter("GRADE");                                   //学年     05/05/30
            _output4 = request.getParameter("OUTPUT4") != null ? "1" : "0";              //単位保留
            _date = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE"));    //出欠集計日付

            _output3 = request.getParameter("OUTPUT3") != null ? "on" : null;                 //遅刻を欠課に換算 null:無

            if (request.getParameter("TESTKINDCD") != null && 2 <= request.getParameter("TESTKINDCD").length()) {
                _testkindcd = request.getParameter("TESTKINDCD").substring(0,2);          //テスト種別
            } else {
                _testkindcd = request.getParameter("TESTKINDCD");
            }

            _semeFlg = request.getParameter("SEME_FLG");          //LOG-IN時の学期（現在学期）05/05/30
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

            KNJDivideAttendDate obj = new KNJDivideAttendDate();
            obj.getDivideAttendDate( db2, _year, _semester, _date );
            _divideAttendDate = obj.date;
            _divideAttendMonth = obj.month;
            log.debug("_15="+_divideAttendDate);
            log.debug("_16="+_divideAttendMonth);
            _hasSchChrDatExecutediv = setTableColumnCheck(db2, "SCH_CHR_DAT", "EXECUTEDIV");

            //svf.VrsOut("year2", nao_package.KenjaProperties.gengou(Integer.parseInt(param._0)) + "年度");  //年度
            loadAttendSemesArgument(db2);

            KNJ_Get_Info getinfo = new KNJ_Get_Info();      //各情報取得用のクラス
            KNJ_Get_Info.ReturnVal returnval = null;        //各情報を返すためのクラス
            if (definecode == null) {
                try {
                    definecode = new KNJDefineSchool();
                    definecode.defineCode(db2, _year);         //各学校における定数等設定
                    log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);
                } catch (Exception ex) {
                    log.warn("semesterdiv-get error!", ex);
                }
            }
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            //  学期名称、範囲の取得
            try {
                returnval = getinfo.Semester(db2, _year, _semester);
                _semesterName = returnval.val1;                                   //学期名称
                _semeSdate = returnval.val2;                                  //学期期間FROM
                _semeEdate = returnval.val3;                                  //学期期間TO

                //学期成績の場合１学期開始日を取得 --NO100
                if (_testkindcd.equals("0")  &&  !_semester.equals("9")) {
                    returnval = getinfo.Semester(db2,_year,"9");
                    _attendSdate = returnval.val2;                                //学期期間FROM
                } else {
                    _attendSdate = _semeSdate;
                }

                //１学期の開始日を取得 05/07/04
            } catch (Exception ex) {
                log.warn("term1 svf-out error!",ex);
            } finally {
                if (_semeSdate == null) _semeSdate = _year + "-04-01";
                if (_semeEdate == null) _semeEdate = ( Integer.parseInt(_year) + 1 ) + "-03-31";
                if (_attendSdate == null) _attendSdate = _year + "-04-01";       //05/07/04Build
            }

            //  ３学期の開始日取得 05/03/09
            try {
                returnval = getinfo.Semester(db2, _year,"3");
                _transferDate = returnval.val2;                                   //学期期間FROM
            } catch (Exception ex) {
                log.warn("term1 svf-out error!",ex);
            } finally {
                if (_transferDate == null) {
                    _transferDate = (Integer.parseInt(_year) + 1) + "-03-31";
                }
            }

            //  出欠データ集計用開始日取得 => 2004年度の１学期は累積データを使用する => 出欠データ集計は2004年度2学期以降
            //  作成日(現在処理日)・出欠集計範囲の出力 05/05/22Modify
            try {
                //システム時刻を表記 05/05/22
                final StringBuffer stb = new StringBuffer();
                Date date = new Date();
                SimpleDateFormat sdf = null;    //05/05/22
                sdf = new SimpleDateFormat("yyyy");
                stb.append(KenjaProperties.gengou(Integer.parseInt(sdf.format(date))));
                sdf = new SimpleDateFormat("年M月d日H時m分");
                stb.append( sdf.format(date));
                _dateString = stb.toString();
                log.debug("date="+stb.toString());

            } catch (Exception ex) {
                log.warn("ymd1 svf-out error!", ex);
            }
            setHrInfo(db2);
        }

        private KNJDefineCode setClasscode0(final DB2UDB db2) {
            KNJDefineCode definecode = null;
            try {
                definecode = new KNJDefineCode();
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }

        /**
         * 出欠端数計算共通メソッドの引数をロードする
         */
        private void loadAttendSemesArgument(final DB2UDB db2) {
            try {
                loadSemester(db2, _year, _semester);
                String z010 = getNameMstZ010(db2);
                // 出欠の情報
                final KNJDefineCode definecode0 = setClasscode0(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, _date); // _sDate: 開始日, _date: LOGIN_DATE
                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
                log.debug("sDate = " + _sDate + " , date = " + _date );
                log.debug(" attendSemesMap = " + _attendSemesMap);
                log.debug(" hasuuMap = " + _hasuuMap);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
        }

        /*
         * 名称マスタ　NAMECD1=Z010の名称を取得する
         */
        private String getNameMstZ010(final DB2UDB db2) {
            String z010 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' ";
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    z010 = rs.getString("NAME1");
                }
            } catch (final Exception ex) {
                log.error("名称マスタロードエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("Z010 =" + z010);
            return z010;
        }

        /*
         * 年度の開始日を取得する
         */
        private void loadSemester(final DB2UDB db2, final String year, final String semes) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            final List list = new ArrayList();
            try {
                ps = db2.prepareStatement(sqlSemester(year, semes));
                log.debug(" sql = " + sqlSemester(year, semes));
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

        private String sqlSemester(final String year, final String semester) {
            final String sql;
            sql = "select"
                + "   SEMESTER,"
                + "   SEMESTERNAME,"
                + "   SDATE"
                + " from"
                + "   SEMESTER_MST"
                + " where"
                + "   YEAR='" + year + "' AND "
                + "   SEMESTER='" + semester + "' "
                + " order by SEMESTER"
            ;
            return sql;
        }
        
        /*
         * 年組の情報を取得する
         */
        private void setHrInfo(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _staffnameMap = new HashMap();
            _hrnameMap = new HashMap();
            try {
                String sql;
                sql = "SELECT "
                        + "W2.GRADE,"
                        + "W2.HR_CLASS,"
                        + "W2.HR_NAME,"
                        + "W1.STAFFNAME "
                    + "FROM "
                        + "SCHREG_REGD_HDAT W2 "
                        + "LEFT JOIN STAFF_MST W1 ON W1.STAFFCD = W2.TR_CD1 "
                    + "WHERE "
                            + "YEAR = '" + _year + "' "
                            + "AND GRADE = '" + _grade + "' ";
                if (!_semester.equals("9") ) sql = sql               //学期指定の場合
                        + "AND SEMESTER = '" + _semester + "'";
                else                        sql = sql               //学年指定の場合
                        + "AND SEMESTER = (SELECT "
                                            + "MAX(SEMESTER) "
                                        + "FROM "
                                            + "SCHREG_REGD_HDAT W3 "
                                        + "WHERE "
                                                + "W2.YEAR = W3.YEAR "
                                            + "AND W2.GRADE = W3.GRADE AND W2.HR_CLASS = W3.HR_CLASS)";

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _staffnameMap.put(rs.getString("GRADE") + rs.getString("HR_CLASS"), rs.getString("STAFFNAME"));
                    _hrnameMap.put(rs.getString("GRADE") + rs.getString("HR_CLASS"), rs.getString("HR_NAME"));
                }
            } catch (final Exception ex) {
                log.error("exception!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private static boolean setTableColumnCheck(final DB2UDB db2, final String tabname, final String colname) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT 1 FROM ");
            if (StringUtils.isBlank(colname)) {
                stb.append("SYSCAT.TABLES");
            } else {
                stb.append("SYSCAT.COLUMNS");
            }
            stb.append(" WHERE TABNAME = '" + tabname + "' ");
            if (!StringUtils.isBlank(colname)) {
                stb.append(" AND COLNAME = '" + colname + "' ");
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean hasTableColumn = false;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    hasTableColumn = true;
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.fatal(" hasTableColumn " + tabname + (null == colname ? "" :  "." + colname) + " = " + hasTableColumn);
            return hasTableColumn;
        }
    }
}//+++++ 以上、ハウルの動く城になっちまった通知表のプログラムソース
