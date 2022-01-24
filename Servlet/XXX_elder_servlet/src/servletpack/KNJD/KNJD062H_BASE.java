// kanji=漢字
/*
 * $Id: a713824ce234df73b88643ae795703dca724180f $
 *
 * 作成日: 2006/03/28
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJC.KNJC053_BASE;
import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
  *http://tokio/servlet/KNJD?DBNAME=SY0403&PRGID=KNJD062H&YEAR=2005&SEMESTER=9&GRADE=01&CLASS_SELECTED=01002&DATE=2006/03/31&TESTKINDCD=0&SEME_FLG=2&OUTPUT5=on
  *
  *  学校教育システム 賢者 [成績管理] 成績一覧 (広島国際用)
  *  2006/03/28 yamashiro 東京都版を複写して作成 => 以前の注釈はKNJD062_BASE.javaを参照
  *                       主な相違点（東京都版と）
  *                         ○KNJD062H.classからKNJD062H_BASEのメソッドをCALL。KNJD062H_INTER,KNJD062H_TERM,KNJD062H_GAKKI,KNJD062H_GRADEは内部クラスとして作成
  *                       別仕様（東京都版と）
  *                         ○遅刻度数の表記
  *                         ○SCH_CHR_TESTに欠席がある場合、成績(中間・期末)・評定欄(学期・学年)に'/'を付加する
  *  2006/04/07 yamashiro 出欠集計用テーブルの変更および出欠集計処理の変更に対応 --NO025
  *                         KNJD062_BASEを参照
  *                         ○学籍不在時および留学・休学中の出欠データはカウントしない
  *                         ○出停・忌引がある日は遅刻・早退をカウントしない
  */

class KNJD062H_BASE {
    private static final Log log = LogFactory.getLog(KNJD062H_BASE.class);

    private Vrw32alp svf;                   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private DB2UDB db2;                     //Databaseクラスを継承したクラス
    private String fieldname;
    private String fieldname2;
    private int subclasslinecount;          //科目列カウント
    
    private int hr_total;                                   //クラス平均総合点
    private int hr_lesson[] = new int[2];                   //クラス授業日数{最大、最小}  04/11/01Add
    private int hr_attend[] = new int[10];                  //クラス平均出欠格納
    private int hr_seitosu[] = new int[12];                 //クラス平均対象生徒数格納
    private int sch_rcredits[] = new int[50];               //生徒ごとの履修単位数 05/01/31
    private int sch_ccredits[] = new int[50];               //生徒ごとの修得単位数 05/01/31
    private int hr_credits;                                 //クラスの履修単位数   05/01/31
    private int subclasstotalcnt;                           //科目別件数     05/03/09
    private int subclasstotalnum;                           //科目別得点累積 05/03/09
    private int hrtotalnum;                                 //全科目得点累積 05/03/10
    private int hrtotalcnt;                                 //全科目件数     05/03/10
    private KNJD062H_COMMON hobj= null;

    private Param _param;

    /**
     *  コンストラクター
     */
    KNJD062H_BASE(DB2UDB db2, Vrw32alp svf, String[] param) {
        this.db2 = db2;
        this.svf = svf;
        _param = new Param(this.db2, param);
    }


    /**
     *  印刷処理
     */
    boolean printSvf(String[] hrclass) {

        boolean nonedata = false;               //該当データなしフラグ
        try {
            _param.getParam2(db2);  //KNJD062H
            setKnjd062HObj();  //KNJD062H
            _param.set_head(db2);                                     //見出し出力のメソッド
            hobj.set_head2();  //KNJD062H
            if (printSvfMain(hrclass)) {
            	nonedata = true;   //印刷処理
            }
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
        return nonedata;
    }

    /**
     * メイン印刷処理
     *   対象生徒(Sql=>prestatStdNameList)を読込み、５０名ごとに印刷(printSvfStdDetail)する
     */
    private boolean printSvfMain(final String[] hrclass) {
        boolean nonedata = false;
        String lschname[] = new String[50];     //生徒名の保管 05/05/22
        _param.hm1 = new HashMap(50);              //学籍番号と行番号の保管
        _param.hm2 = new HashMap();                //行番号(出席番号)と備考の保管 05/05/22
        _param.hm3 = new HashMap();                //科目別件数     05/06/07
        _param.hm4 = new HashMap();                //科目別得点累積 05/06/07
//      definecode.absent_cov = 2;  // 試し
        final String absentDiv = "1"; // 1:年間 2:随時

        try {
            _param._psStudent = db2.prepareStatement( prestatStdNameList()       );      //学籍データ
            _param._psTotalRec = db2.prepareStatement( prestatStdTotalRec()       );      //成績累計データ
            _param._psSubclassInfo = db2.prepareStatement( prestatSubclassInfo()      );      //科目名等データ
            _param._psSubclassDetail = db2.prepareStatement( prestatStdSubclassDetail(absentDiv));      //明細データ
            _param._psShrLate = db2.prepareStatement( prestatStdTotalShrLate()   );      //遅刻度数 KNJD062H
        } catch (Exception ex) {
            log.error("[KNJD171K]boolean printSvfMain prepareStatement error! ", ex);
            return nonedata;
        }

        for (int i = 0; i < hrclass.length; i++) {
        	log.info("hrclass = " + hrclass[i]);
            try {
                _param._gradeHrclass = hrclass[i];
                if (printSvfMainHrclass(lschname)) {
                	nonedata = true;
                }
                clearMapValue(lschname);
                clearValue();
            } catch (Exception ex) {
                log.error("[KNJD062]boolean printSvfMainHrclass() error! ", ex);
            }
        }
        DbUtils.closeQuietly(_param._psStudent);
        DbUtils.closeQuietly(_param._psTotalRec);
        DbUtils.closeQuietly(_param._psSubclassInfo);
        DbUtils.closeQuietly(_param._psSubclassDetail);
        DbUtils.closeQuietly(_param._psAttendSemesSql);
        DbUtils.closeQuietly(_param._psShrLate);

        return nonedata;
    }


    /**
     *  マップ初期化
     *  2005/06/07
     */
    private void clearMapValue(final String[] lschname) {
        try {
            if (_param.hm1 != null) _param.hm1.clear();                       //行番号情報を削除
            if (_param.hm2 != null) _param.hm2.clear();                       //備考情報を削除
            if (_param.hm3 != null) _param.hm3.clear();                           //備考情報を削除
            if (_param.hm4 != null) _param.hm4.clear();                           //備考情報を削除
            if (lschname != null) {
            	for (int j = 0 ; j < lschname.length ; j++) lschname[j] = null;
            }
            _param._attendnoFrom = null;
            _param._attendnoTo = null;
        } catch (Exception ex) {
            log.warn("clearMapValue error!", ex);
        }
    }


    /**
     *
     * SVF-OUT 学級単位の印刷処理
     *    Stirng param[]について ==> _param._3:学年・組
     */
    private boolean printSvfMainHrclass(final String[] lschname) {
        boolean nonedata = false;
        ResultSet rs = null;
        int schno = 0;                          //行番号
        int schno2 = 0;                         //行番号
        boolean bout = false;                   //05/06/07Build

        //学級単位の印刷処理
        try {
            int pp = 0;
            _param._psStudent.setString(++pp, _param._gradeHrclass);                      //学年・組
            rs = _param._psStudent.executeQuery();                         //生徒名の表
            while (rs.next()) {
                //05/06/06Modify 行を詰めて出力する処理を追加
                if (_param._19 != null) {
                    ++schno;                                            //05/06/06
                    if (50 <= schno) {
                    	bout = true;
                    }
                } else{
                    schno = (rs.getInt("ATTENDNO") % 50 != 0) ? rs.getInt("ATTENDNO") % 50 : 50;
                    if (schno < schno2) {
                    	bout = true;
                    }
                    schno2 = schno;
                }
                //改ページ処理
                if (bout) {
//log.debug("check schno="+schno);
                    if (printSvfStdDetail(lschname, false)) {
                    	nonedata = true;          //成績・評定・欠課出力のメソッド
                    }
                    clearMapValue(lschname);
                    if (_param._19 != null ) {
                    	schno = 1;
                    } else {
                    	schno = (rs.getInt("ATTENDNO") % 50 != 0) ? rs.getInt("ATTENDNO") % 50 : 50;
                    }
                    _param._attendnoFrom = null;                                    //開始生徒
                    _param._attendnoTo = null;                                   //終了生徒
                    bout = false;
                    clearValue2();
                }

                saveSchInfo(rs, schno, lschname);     //生徒情報の保存処理 2005/05/22

            }
        } catch (Exception ex) {
            log.warn("ResultSet-read error!",ex);
        } finally{
            db2.commit();
        }

        //成績・評定・欠課の印刷処理
//log.debug("hm1 size="+hm1.size());
        if (0 < _param.hm1.size()) {
            if (printSvfStdDetail(lschname, true)) {
            	nonedata = true;     //成績・評定・欠課出力のメソッド OKならインスタンス変数nonedataをtrueとする
            }
            clearMapValue(lschname);
            clearValue2();
        }
        DbUtils.closeQuietly(rs);
        return nonedata;
    }


    /**
     *  生徒情報の保存処理
     *    2005/05/22
     *    2005/06/07 Modify String lschname[]の前３桁に出席番号を挿入
     */
    private void saveSchInfo(final ResultSet rs, final int schno, final String[] lschname) {
        try {
            _param.hm1.put(rs.getString("SCHREGNO"), new Integer(schno));    //行番号に学籍番号を付ける
            if (_param._attendnoFrom == null) _param._attendnoFrom = rs.getString("ATTENDNO"); //開始生徒
            _param._attendnoTo = rs.getString("ATTENDNO");                       //終了生徒
            //lschname[ schno-1 ] = rs.getString("NAME");                   //生徒名
            String str = "000" + rs.getString("ATTENDNO");
            lschname[ schno-1 ] = str.substring( str.length() - 3 , str.length()) + rs.getString("NAME");  //05/06/07

            //  文言をセットする。（除籍日付＋'付'＋除籍区分名称）04/11/08Add
            if (rs.getString("KBN_DATE1") != null) {
                StringBuffer stb = new StringBuffer();
                stb.append(KNJ_EditDate.h_format_JP(db2, rs.getString("KBN_DATE1")));
                if (rs.getString("KBN_NAME1") != null) stb.append(rs.getString("KBN_NAME1"));
                _param.hm2.put(new Integer(schno), stb.toString());  //備考に行番号を付ける
            } else if (rs.getString("KBN_DATE2") != null) {
                StringBuffer stb = new StringBuffer();
                stb.append(KNJ_EditDate.h_format_JP(db2, rs.getString("KBN_DATE2")));
                if (rs.getString("KBN_NAME2") != null) stb.append(rs.getString("KBN_NAME2"));
                _param.hm2.put(new Integer(schno), stb.toString());  //備考に行番号を付ける
            }
        } catch (Exception ex) {
            log.warn("name1 svf-out error!", ex);
        }

    }


    /**
     *  生徒名等出力 
     *    2005/05/22 Modify
     *    2005/06/07 Modify 
     */
    private void printSvfStdNameOut(String[] lschname) {

        try {
            for (int i = 0 ; i < lschname.length; i++) {
                //svf.VrsOut("name" + (i+1) ,lschname[i] );
                if (lschname[i] == null) {
                	continue;
                }
                svf.VrsOut("name" + (i+1) ,   lschname[i].substring( 3 ) );
                if (_param._19 != null ) {
                    svf.VrsOutn("NUMBER", (i+1) , String.valueOf( Integer.parseInt( lschname[i].substring( 0, 3 ) ) ) );
                } else {
                    svf.VrsOutn("NUMBER", (i+1) , String.valueOf( Integer.parseInt( lschname[i].substring( 0, 3 ) ) ) );
                }
                if (_param.hm2.containsKey(new Integer(i + 1))) {
                	svf.VrsOut("REMARK" + (i+1) , (String) _param.hm2.get( new Integer(i + 1)));
                }
            }
        } catch (Exception ex) {
            log.warn("name1 svf-out error!", ex);
        }
    }


    /**
     *  明細出力処理 
     *    科目別明細(成績・評定・欠課)(SQL=>prestatStdSubclassDetail)を読込み出力する
     *    科目名の変わり目で科目情報(教科名・科目名・単位数・授業時数)(SQL=>prestatSubclassInfo)を出力する()
     *    最後に生徒別総合＆合計(SQL=>printSvfStdTotalRec,printSvfStdTotalAttend)を出力する
     *      2005/01/04 SQL変更に伴い修正
     *      2005/06/07 Modify boolean blast追加
     */
    private boolean printSvfStdDetail(final String[] lschname, final boolean blast) {
        boolean nonedata = false;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        int assesspattern = 0;          //科目の評定類型 A:0 B:1 C:2
        for( int i = 0 ; i < sch_rcredits.length ; i++ )sch_rcredits[i] = 0;   //履修単位数 05/01/31
        for( int i = 0 ; i < sch_rcredits.length ; i++ )sch_ccredits[i] = 0;   //修得単位数 05/01/31
        int subclass1 = 0;              //科目コードの保存
        int subclass2 = 0;              //科目コードの保存
        int groupcd = 0;                //群コードの保存 05/01/31
        if (false && 0 != groupcd) { groupcd = 0; }
        int replaceflg = 0;             //評価読替元科目:1  評価読替先科目:-1  NO007 Build
        clearSvfField(lschname);                         //生徒名等出力のメソッド

        try {
            int p = 0;
            _param._psSubclassDetail.setString( ++p, _param._gradeHrclass  );
            _param._psSubclassDetail.setString( ++p, _param._attendnoFrom  );       //生徒番号
            _param._psSubclassDetail.setString( ++p, _param._attendnoTo );       //生徒番号
            _param._psSubclassDetail.setString( ++p, _param._gradeHrclass  );
            if (log.isDebugEnabled()) { log.debug("prestatStdSubclassDetail executeQuery() start"); }
            rs2 = _param._psSubclassDetail.executeQuery();                  //明細データのRecordSet
            if (log.isDebugEnabled()) { log.debug("prestatStdSubclassDetail executeQuery() end"); }
        } catch (Exception ex) {
            log.warn("ResultSet-read error!",ex);
        }

        try {
            int p = 0;
            _param._psSubclassInfo.setString( ++p, _param._gradeHrclass );                //学年・組
            _param._psSubclassInfo.setString( ++p, _param._gradeHrclass );                //学年・組
            _param._psSubclassInfo.setDate( ++p, java.sql.Date.valueOf(_param._divideAttendDate));  // 出欠データ集計開始日付 2005/01/04
            _param._psSubclassInfo.setDate( ++p, java.sql.Date.valueOf(_param._edate));  // 出欠データ集計終了日付 2005/01/04
            _param._psSubclassInfo.setString( ++p, KNJC053_BASE.retSemesterMonthValue (_param._divideAttendMonth ) );     //出欠集計データ終了学期＋月 NO004
            _param._psSubclassInfo.setString( ++p, _param._gradeHrclass );                //学年・組
            if (log.isDebugEnabled()) { log.debug("prestatSubclassInfo executeQuery() start"); }
            rs1 = _param._psSubclassInfo.executeQuery();                                  //科目名等のRecordSet
            if (log.isDebugEnabled()) { log.debug("prestatSubclassInfo executeQuery() end"); }
        } catch (Exception ex) {
            log.warn("ResultSet-read error!",ex);
        }

        boolean rs1read = false; //NO009
        try {
            while (rs2.next()) {
                //科目コードのブレイク
                final int rs2subclass;
                if ("1".equals(_param._useCurriculumcd)) {
                    rs2subclass = Integer.parseInt(StringUtils.split(rs2.getString("SUBCLASSCD"), "-")[3]);
                } else {
                    rs2subclass = rs2.getInt("SUBCLASSCD");
                }
                if (subclass2 != rs2subclass) {
                    if (subclass2 != 0) {
                        printSvfsubclasscnt_num(subclass1, blast);
                        svf.VrEndRecord();
                        nonedata = true;
                        subclasslinecount++;
                        subclasstotalcnt = 0;  //05/03/09
                        subclasstotalnum = 0;  //05/03/09
                        replaceflg = 0;  //NO007 Build
                    }
                    subclass2 = rs2subclass;           //科目コードの保存
                    //科目名をセット
                    for (; subclass1 < subclass2;) {
                        if (19 <= subclasslinecount) {
                        	subclasslinecount = clearSvfField(lschname);
                        }
                        if (rs1.next()) {
                            if (!rs1read) {
                            	rs1read = true;  //NO009
                            }
                            replaceflg = getValueReplaceflg(rs1);  //評価読替科目の判断 NO007 Build
                            final int rs1subclass;
                            if ("1".equals(_param._useCurriculumcd)) {
                                rs1subclass = Integer.parseInt(StringUtils.split(rs1.getString("SUBCLASSCD"), "-")[3]);
                            } else {
                                rs1subclass = rs1.getInt("SUBCLASSCD");
                            }
                            subclass1 = rs1subclass;
                            assesspattern = 0;
                            printSvfSubclassInfoOut(rs1);       //科目名等出力のメソッド
                            groupcd = rs1.getInt("GROUPCD");    //群コードの保存 05/01/31
                            if (subclass1 < subclass2) {          //データ出力用にない科目列を出力する
                                printSvfsubclasscnt_num(subclass1, blast);      //05/03/16Modify
                                svf.VrEndRecord(); 
                                nonedata = true; 
                                subclasslinecount++;
                                subclasstotalcnt = 0;  //05/03/09
                                subclasstotalnum = 0;  //05/03/09
                            }
                            if (subclass1 == subclass2) {
                            	break;        //科目名出力用とデータ出力用の科目コードが一致なら抜ける
                            }
                        } else {
                        	rs1read = false;  //NO009
                        }
                    }
                }
                //明細データの出力 05/03/03Modify 総合学習を別処理とする
                hobj.printSvfStdDetailOut(rs2, assesspattern ,replaceflg); //総合学習以外 NO007Modify  KNJD062H
            }

            //NO009
            if (rs1read) {
                for (int i = 0; i < 20; i++) {
                    printSvfsubclasscnt_num(subclass1, blast);      //05/03/16Modify
                    svf.VrEndRecord(); 
                    nonedata = true; 
                    subclasslinecount++;
                    subclasstotalcnt = 0;  //05/03/09
                    subclasstotalnum = 0;  //05/03/09
                    if (rs1.next()) {
                        if (19 <= subclasslinecount) subclasslinecount = clearSvfField(lschname);  //NO0019
                        replaceflg = getValueReplaceflg(rs1);  //評価読替科目の判断 NO007 Build
                        if ("1".equals(_param._useCurriculumcd)) {
                            subclass1 = Integer.parseInt(StringUtils.split(rs1.getString("SUBCLASSCD"), "-")[3]);
                        } else {
                            subclass1 = rs1.getInt("SUBCLASSCD");
                        }
                        assesspattern = 0;
                        printSvfSubclassInfoOut(rs1);       //科目名等出力のメソッド
                        groupcd = rs1.getInt("GROUPCD");    //群コードの保存 05/01/31
                    } else {
                    	break;
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("ResultSet-read error!", ex);
        } finally{
            db2.commit();
            DbUtils.closeQuietly(rs1);
            DbUtils.closeQuietly(rs2);
        }

        //総合欄の印刷処理
        if (subclass2 != 0) {
            printSvfsubclasscnt_num(subclass1, blast);   //05/03/16
            printSvfStdTotalRec();
            printSvfStdTotalAttend();
            printSvfStdTotalShrLate();  //KNJD062H
            if (blast) {
            	printSvfTotalOut();
            }
            svf.VrEndRecord();
            nonedata = true;
            subclasslinecount++;
        }

        return nonedata;
    }


    /**
     *  科目名等出力 
     */
    private void printSvfSubclassInfoOut(ResultSet rs) {

        try {
            boolean amikake = false;
            //NO009 if (rs.getString("SUBCLASSCD").substring(0,2).equals("90") ){
            //NO009     printSvfOutSpace();
            //NO009     subclasslinecount = 18;
            //NO009 }

            svf.VrsOut("course1" , rs.getString("CLASSABBV"));                //教科名
//log.debug("subclasslinecount="+subclasslinecount+"   subclassname="+rs.getString("SUBCLASSNAME"));
            if (rs.getString("ELECTDIV") != null  &&  ! rs.getString("ELECTDIV").equals("0") ) amikake = true;
            if (amikake ) svf.VrAttributen("subject1", subclasslinecount+1, "Paint=(2,70,2),Bold=1"); //網掛け
            svf.VrsOutn("subject1", subclasslinecount+1, rs.getString("SUBCLASSNAME"));   //科目名

            svf.VrsOutn("credit1",  subclasslinecount+1, setSubclassCredits(rs));    //単位数

            if (rs.getString("JISU") != null  &&  0 < Integer.parseInt( rs.getString("JISU") ) ) //05/03/09
            svf.VrsOutn("lesson1",  subclasslinecount+1, rs.getString("JISU"));           //授業時数
            if (amikake ) svf.VrAttributen("subject1", subclasslinecount+1, "Paint=(0,0,0),Bold=0");  //網掛け

        } catch (Exception ex) {
            log.warn("course1... svf-out error!",ex);
        }

    }//printSvfSubclassInfoOut()の括り

    
    // クラスの科目別単位数を編集して返す。
    private String setSubclassCredits(final ResultSet rs) {
        int credits_a = 0;
        int credits_b = 0;
        try {
            credits_a = rs.getInt("MAXCREDITS");
            credits_b = rs.getInt("MINCREDITS");
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
        if (credits_a == 0) { return ""; }
        if (credits_a == credits_b) { return String.valueOf(credits_a); }
        return String.valueOf(credits_b) + " \uFF5E " + String.valueOf(credits_a);
    }

    
    /**
     *  科目名等出力  評価読替科目の判断      NO007 Build
     */
    private int getValueReplaceflg(ResultSet rs) {
        int ret = 0;
        try {
            final String replaceFlg = rs.getString("REPLACEFLG");
            if (replaceFlg != null ) {
                if (replaceFlg.equals("SAKI")) ret = -1;
                else if (replaceFlg.equals("MOTO")) ret = 1;
            }
        } catch (Exception ex) {
            log.warn("ResultSet-read error!", ex);
        }
        return ret;
    }

    /**
     *  科目名等出力   総合的な学習を最後列に出力するための処理  --NO009
     */
    private void printSvfOutSpace2() {
        if (subclasslinecount != 0) {
            svf.VrAttribute("course1",  "Meido=100" );    //教科名を白文字設定
            svf.VrsOut("course1", String.valueOf( 1 ) );  //教科名
            log.debug("subclasslinecount="+subclasslinecount);
            svf.VrEndRecord();
            subclasslinecount++;
        }
    }




    /**
     *  科目別および全科目の平均・合計累積処理
     *      学期および学年成績において ( _param._11=='0' ) 評定読替科目は含めない
                                                            (SQLのSELECTで'REPLACEMOTO'がマイナスなら評定読替科目)
     *  2005/03/09
     *  NO023
     */
    private void subclasscnt_num( String score, int replacemoto) {
        try {
            if (score != null  &&
                ! score.equals("")  &&
                ! score.equals("-")  &&
                ! score.equals("=") )
            {
                    subclasstotalnum += Integer.parseInt( score );
                    subclasstotalcnt ++;

                    if (_param._11.equals("0")) {
                        if (0 <= replacemoto) {
                            hrtotalnum += Integer.parseInt( score );
                            hrtotalcnt ++;
                        }
                    } else {
                        hrtotalnum += Integer.parseInt( score );
                        hrtotalcnt ++;
                    }
            }
        } catch (Exception ex) {
            log.warn("subclasscnt_num error!",ex);
        }
    }

    /**
     *  科目別平均・合計
     *  2005/03/09
     *  2005/06/07
     */
    private void printSvfsubclasscnt_num( int subclass1, boolean blast) {
//log.debug("subclass1="+subclass1);
//log.debug("subclasstotalnum="+subclasstotalnum);
//log.debug("subclasstotalcnt="+subclasstotalcnt);

        Integer inti = null;
        int intj = 0;
        int intk = 0;

        try {
            if (0 < subclasstotalcnt) {
                inti = (Integer) _param.hm3.get( String.valueOf( subclass1 ) );
                if (inti != null )intj = inti.intValue();
//log.debug("1  intj="+intj+"   inti="+inti+ "  subclasstotalcnt="+subclasstotalcnt);
                intj += subclasstotalcnt;
                _param.hm3.remove( String.valueOf( subclass1 ) );
                _param.hm3.put( String.valueOf( subclass1 ), new Integer( intj ) );

                intj = 0;
                inti = (Integer) _param.hm4.get( String.valueOf( subclass1 ) );
                if (inti != null )intj = inti.intValue();
                intj += subclasstotalnum;
                _param.hm4.remove( String.valueOf( subclass1 ) );
                _param.hm4.put( String.valueOf( subclass1 ), new Integer( intj ) );
            }
        } catch (Exception ex) {
            log.warn("term1 svf-out error!",ex);
        }

        if (blast )
            try {
                intj = 0;
                intk = 0;
                inti = (Integer) _param.hm3.get( String.valueOf( subclass1 ) );
                if (inti != null )intj = inti.intValue();
                inti = (Integer) _param.hm4.get( String.valueOf( subclass1 ) );
                if (inti != null )intk = inti.intValue();
                if (0 < intj) {
                    svf.VrsOutn("TOTAL_SUBCLASS", subclasslinecount+1, String.valueOf( intk ) );  //ＨＲ合計 04/11/03Add 04/11/08Modify
                    //svf.VrsOutn("AVE_CLASS",      subclasslinecount+1, String.valueOf( Math.round( (float)intk / (float)intj ) ) ); //ＨＲ平均             04/11/08Modify
                    svf.VrsOutn("AVE_CLASS",      subclasslinecount+1, String.valueOf( (float)Math.round( (float)intk / (float)intj * 10 ) / 10 ) );  //ＨＲ平均 05/09/28Modify
                }
            } catch (Exception ex) {
                log.warn("term1 svf-out error!",ex);
            }
    }

    /** 
     *  明細出力 
     *  生徒の科目別欠課時数を出力する
     */
    private boolean printSvfStdDetailOutAbsence( ResultSet rs, int i, int j ) {
        boolean amikake = false;
        try {
            if (rs.getString("ABSENT1") != null  &&  Integer.parseInt( rs.getString("ABSENT1") ) != 0) {
                int absence_high = 0;
                if (rs.getString("ABSENCE_HIGH") != null) {
                	absence_high = rs.getInt("ABSENCE_HIGH");
                }
                if (0 < absence_high && rs.getString("ABSENT1") != null && absence_high < Integer.parseInt( rs.getString("ABSENT1"))) {
                	amikake = true;   //05/09/16Modify
                }
                if (amikake )svf.VrAttributen("kekka" + i, j, "Paint=(2,70,1),Bold=1");
                svf.VrsOutn("kekka" + i, j, rs.getString("ABSENT1"));                 //欠課
                if (amikake )svf.VrAttributen("kekka" + i, j, "Paint=(0,0,0),Bold=0");   //網掛けクリア
            }
        } catch (Exception ex) {
            log.warn("attend... svf-out error!", ex);
        }
        return amikake;
    }


    /**
     *  累計出力処理 
     */
    private boolean printSvfStdTotalRec()
    {
        boolean nonedata = false;
        ResultSet rs = null;
//for(int ia=0 ; ia<param.length ; ia++)log.trace("param["+ia+"]=" + param[ia]);

        try {
//log.debug("_param._arrps1="+_param._arrps1.toString());           
            int pp = 0;
            _param._psTotalRec.setString( ++pp,  _param._gradeHrclass );                      //学年・組
            _param._psTotalRec.setString( ++pp,  _param._gradeHrclass );                      //学年・組 NO024
log.debug("prestatStdTotalRec executeQuery() start");
            rs = _param._psTotalRec.executeQuery();                               //成績累計データのRecordSet
log.debug("prestatStdTotalRec executeQuery() end");

            while (rs.next()) {
                //明細データの出力
                printSvfStdTotalRecOut(rs);
            }
        } catch (Exception ex) {
            log.warn("ResultSet-read error!", ex);
        } finally{
            db2.commit();
            DbUtils.closeQuietly(rs);
        }

        return nonedata;

    }//printSvfStdTotalRec()の括り


    /**
     *  累計出力処理 
     *      2005/01/04 SQL変更に伴い修正
     *      NO014により変更
     */
    private boolean printSvfStdTotalAttend() {
        boolean nonedata = false;

        try {
        	if (null == _param._psAttendSemesSql) {
                _param.attendParamMap.put("useTestCountflg", "RECORD_DAT");
                _param.attendParamMap.put("useCurriculumcd", _param._useCurriculumcd);
                _param.attendParamMap.put("useVirus", _param._useVirus);
                _param.attendParamMap.put("useKoudome", _param._useKoudome);

        		final String sql = AttendAccumulate.getAttendSemesSql(
        				_param._semesFlg,
        				_param.definecode,
        				_param._knjSchoolMst,
        				_param._year,
        				_param.SSEMESTER,
        				_param._semester,
        				(String) _param._hasuuMap.get("attendSemesInState"),
        				_param._periodInState,
        				(String) _param._hasuuMap.get("befDayFrom"),
        				(String) _param._hasuuMap.get("befDayTo"),
        				(String) _param._hasuuMap.get("aftDayFrom"),
        				(String) _param._hasuuMap.get("aftDayTo"),
        				_param._grade,
        				"?",
        				"?",
        				"SEMESTER",
        				_param.attendParamMap
        				);
        		_param._psAttendSemesSql = db2.prepareStatement(sql); // 出欠累計データ
        		//log.info(" attend sql = " + sql);
        	}

        	for (final String schregno : _param.hm1.keySet()) {
//log.trace("prestatStdTotalAttend executeQuery() start ");
                final List<Map<String, String>> rowList = KnjDbUtils.query(db2, _param._psAttendSemesSql, new Object[] {schregno, _param._gradeHrclass.substring(2, 5)});
            	//log.info(" attend arg = " + ArrayUtils.toString(new Object[] {schregno, _param._gradeHrclass.substring(2, 5)}));
//log.trace("prestatStdTotalAttend executeQuery() end ");
                for (final Map<String, String> row : rowList) {
                    if (_param._semester != null && !_param._semester.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                        continue;
                    }
                    //学籍番号（生徒）に対応した行にデータをセットする。
                    Integer int1 = _param.hm1.get(KnjDbUtils.getString(row, "SCHREGNO"));
                    if (int1 != null) {
                    	printSvfStdTotalAttendOut(row, int1.intValue());  //明細データの出力
                    }

                }
            }
        } catch (Exception ex) {
            log.warn("ResultSet-read error!", ex);
        }

        return nonedata;

    }//printSvfStdTotalAttend()の括り

    /** 
     *  明細出力 
     */
    private void printSvfStdTotalRecOut(ResultSet rs) {
        try {
            //学籍番号（生徒）に対応した行にデータをセットする。
            Integer int1 = _param.hm1.get(rs.getString("SCHREGNO"));
            if (int1 == null) {
            	return;
            }

            if (rs.getString("SUM_REC") != null) {
                svf.VrsOut("TOTAL" + int1.intValue(), rs.getString("SUM_REC"));           //総合点
                hr_total += rs.getInt("SUM_REC");
                hr_seitosu[8]++;
            }

            if (rs.getString("AVG_REC") != null) {
                //svf.VrsOut("AVERAGE" + int1.intValue(), String.valueOf(Math.round(rs.getFloat("AVG_REC"))));    //平均点 04/11/08Modify
                svf.VrsOut("AVERAGE" + int1.intValue(), String.valueOf((float)Math.round(rs.getFloat("AVG_REC")*10)/10)); //平均点 05/09/28Modify
//log.debug("average="+rs.getFloat("AVG_REC") + "    " +(float)Math.round(rs.getFloat("AVG_REC")*10)/10);
                //if (hr_average == 0 ) {      // 04/11/08Modify
                //    hr_average = rs.getInt("TOTAL_REC_AVG");
                //    hr_seitosu[9] = rs.getInt("TOTAL_REC_SUM");
                //}
            }

            if (rs.getString("RANK") != null) {
                svf.VrsOut("RANK" + int1.intValue(), rs.getString("RANK"));               //順位
            }

            // 04/12/16処理を止めておく  05/01/31Modify 学年末の処理で、３年生は2学期、以外の学年は3学期に出力
            if (_param._semester.equals("9")  &&  0 < sch_rcredits[ int1.intValue() ] )
                svf.VrsOut("R_CREDIT" + int1.intValue(), String.valueOf( sch_rcredits[ int1.intValue() ] ) ); //履修単位数
            if (_param._semester.equals("9")  &&  0 < sch_ccredits[ int1.intValue() ] )
            //NO010 if (_param._1.equals("9")  &&  0 < sch_rcredits[ int1.intValue() ] )
                svf.VrsOut("C_CREDIT" + int1.intValue(), String.valueOf( sch_ccredits[ int1.intValue() ] ) ); //修得単位数
/* ************
            if (rs.getString("ABROAD_CLASSDAYS")!=null ) {
                if (rs.getInt("ABROAD_CLASSDAYS")!=0 ) {
                    svf.VrsOut("ABROAD" + int1.intValue(), rs.getString("ABROAD_CLASSDAYS")); //留学日数
                    hr_attend[2] += rs.getInt("ABROAD_CLASSDAYS");
                }
                hr_seitosu[2]++;
            }
************** */
        } catch (Exception ex) {
            log.warn("total svf-out error!", ex);
        }

    }//printSvfStdTotalRecOut()の括り



    /** 
     *  出欠の記録 明細出力 
     *      2005/01/04 SQL変更に伴い修正
     *      2005/02/01 出欠の記録について異動した生徒も出力する。(取り敢えず)合計からは除外。
     */
    private void printSvfStdTotalAttendOut(final Map<String, String> row, final int int1) {
        try {
            if (KnjDbUtils.getString(row, "LESSON") != null  &&  KnjDbUtils.getInt(row, "LESSON", 0) != 0) {
                //intx = KnjDbUtils.getInt(row, "LESSON");
                if (hr_lesson[0] < KnjDbUtils.getInt(row, "LESSON", 0) ) 
                    hr_lesson[0] = KnjDbUtils.getInt(row, "LESSON", 0);                 //最大授業日数 04/11/01Add
                if (hr_lesson[1] == 0 )                  
                    hr_lesson[1] = KnjDbUtils.getInt(row, "LESSON", 0);
                else if (KnjDbUtils.getInt(row, "LESSON", 0) < hr_lesson[1] ) 
                    hr_lesson[1] = KnjDbUtils.getInt(row, "LESSON", 0);                 //最小授業日数 04/11/01Add
            }

            if (KnjDbUtils.getString(row, "SUSPEND") != null || "true".equals(_param._useVirus) && KnjDbUtils.getString(row, "VIRUS") != null || "true".equals(_param._useKoudome) && KnjDbUtils.getString(row, "KOUDOME") != null) {
                int value = 0;
                boolean hasvalue = false;
                if (KnjDbUtils.getString(row, "SUSPEND") != null) {
                    if (KnjDbUtils.getInt(row, "SUSPEND", 0) != 0) {
                        hasvalue = true;
                        value += KnjDbUtils.getInt(row, "SUSPEND", 0);
                    }
                }
                if ("true".equals(_param._useVirus) && KnjDbUtils.getString(row, "VIRUS") != null) {
                    if (KnjDbUtils.getInt(row, "VIRUS", 0) != 0) {
                        hasvalue = true;
                        value += KnjDbUtils.getInt(row, "VIRUS", 0);
                    }
                }
                if ("true".equals(_param._useKoudome) && KnjDbUtils.getString(row, "KOUDOME") != null) {
                    if (KnjDbUtils.getInt(row, "KOUDOME", 0) != 0) {
                        hasvalue = true;
                        value += KnjDbUtils.getInt(row, "KOUDOME", 0);
                    }
                }

                hr_attend[0] += value;         //合計 05/02/01Modify
                if (hasvalue) {
                    svf.VrsOut( "SUSPEND" + int1, String.valueOf(value) );   //出停
                }
                hr_seitosu[0]++;                                  //生徒数 05/02/01Modify
            }

            if (KnjDbUtils.getString(row, "MOURNING") != null) {
                if (KnjDbUtils.getInt(row, "MOURNING", 0) != 0) {
                    //intx -= KnjDbUtils.getInt(row, "MOURNING");
                    svf.VrsOut( "KIBIKI" + int1, KnjDbUtils.getString(row, "MOURNING") );   //忌引
                    //if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_attend[1] += KnjDbUtils.getInt(row, "MOURNING");        //合計 05/02/01Modify
                    hr_attend[1] += KnjDbUtils.getInt(row, "MOURNING", 0);        //合計 05/02/01Modify
                }
                //if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_seitosu[1]++;                                  //生徒数 05/02/01Modify
                hr_seitosu[1]++;                                  //生徒数 05/02/01Modify
            }

            if (KnjDbUtils.getString(row, "MLESSON") != null) {
                if (KnjDbUtils.getInt(row, "MLESSON", 0) != 0) {
                    svf.VrsOut( "PRESENT" + int1, KnjDbUtils.getString(row, "MLESSON") );   //出席すべき日数
                    //if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_attend[3] += KnjDbUtils.getInt(row, "MLESSON");         //合計 05/02/01Modify
                    hr_attend[3] += KnjDbUtils.getInt(row, "MLESSON", 0);         //合計 05/02/01Modify
                }
                //if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_seitosu[3]++;                                  //生徒数 05/02/01Modify
                hr_seitosu[3]++;                                  //生徒数 05/02/01Modify
            }

/* ******************
            if (0 < intx) {
                svf.VrsOut( "PRESENT" + int1, String.valueOf(intx) );          //出席すべき日数
                if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_attend[3] += intx;                             //合計 05/02/01Modify
                if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_seitosu[3]++;                                  //生徒数 05/02/01Modify
            }
******************** */

            if (KnjDbUtils.getString(row, "SICK") != null) {
                if (KnjDbUtils.getInt(row, "SICK", 0) != 0) {
                    //intx -= KnjDbUtils.getInt(row, "SICK");
                    svf.VrsOut( "ABSENCE" + int1, KnjDbUtils.getString(row, "SICK") );    //欠席日数
                    //if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_attend[4] += KnjDbUtils.getInt(row, "SICK");          //合計 05/02/01Modify
                    hr_attend[4] += KnjDbUtils.getInt(row, "SICK", 0);          //合計 05/02/01Modify
                }
                //if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_seitosu[4]++;                                  //生徒数 05/02/01Modify
                hr_seitosu[4]++;                                  //生徒数 05/02/01Modify
            }

            if (KnjDbUtils.getString(row, "PRESENT") != null) {
                if (KnjDbUtils.getInt(row, "PRESENT", 0) != 0) {
                    svf.VrsOut( "ATTEND" + int1, KnjDbUtils.getString(row, "PRESENT") );    //出席日数
                    //if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_attend[5] += KnjDbUtils.getInt(row, "PRESENT");         //合計 05/02/01Modify
                    hr_attend[5] += KnjDbUtils.getInt(row, "PRESENT", 0);         //合計 05/02/01Modify
                }
                //if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_seitosu[5]++;                                  //生徒数 05/02/01Modify
                hr_seitosu[5]++;                                  //生徒数 05/02/01Modify
            }

/* *******************
            if (0 < intx) {
                svf.VrsOut( "ATTEND" + int1, String.valueOf(intx) );           //出席日数
                if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_attend[5] += intx;                             //合計 05/02/01Modify
                if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_seitosu[5]++;                                  //生徒数 05/02/01Modify
            }
********************* */
            if (KnjDbUtils.getString(row, "EARLY") != null) {
                if (KnjDbUtils.getInt(row, "EARLY", 0) != 0) {
                    svf.VrsOut( "LEAVE" + int1, KnjDbUtils.getString(row, "EARLY") );       //早退回数
                    //if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_attend[6] += KnjDbUtils.getInt(row, "EARLY");           //合計 05/02/01Modify
                    hr_attend[6] += KnjDbUtils.getInt(row, "EARLY", 0);           //合計 05/02/01Modify
                }
                //if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_seitosu[6]++;                                  //生徒数 05/02/01Modify
                hr_seitosu[6]++;                                  //生徒数 05/02/01Modify
            }

            if (KnjDbUtils.getString(row, "LATE") != null) {
                if (KnjDbUtils.getInt(row, "LATE", 0) != 0) {
                    svf.VrsOut( "TOTAL_LATE" + int1, KnjDbUtils.getString(row, "LATE") );   //遅刻回数
                    //if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_attend[7] += KnjDbUtils.getInt(row, "LATE");            //合計 05/02/01Modify
                    hr_attend[7] += KnjDbUtils.getInt(row, "LATE", 0);            //合計 05/02/01Modify
                }
                //if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_seitosu[7]++;                                  //生徒数 05/02/01Modify
                hr_seitosu[7]++;                                  //生徒数 05/02/01Modify
            }

            if (KnjDbUtils.getString(row, "TRANSFER_DATE") != null) {  // 05/02/02ココへ移動
                if (KnjDbUtils.getInt(row, "TRANSFER_DATE", 0) != 0) {
                    svf.VrsOut("ABROAD" + int1, KnjDbUtils.getString(row, "TRANSFER_DATE"));    //留学日数
                    //if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_attend[2] += KnjDbUtils.getInt(row, "TRANSFER_DATE");
                    hr_attend[2] += KnjDbUtils.getInt(row, "TRANSFER_DATE", 0);
                }
                //if (KnjDbUtils.getInt(row, "LEAVE") == 0 ) hr_seitosu[2]++;
                hr_seitosu[2]++;
            }

        } catch (Exception ex) {
            log.warn("total svf-out error!", ex);
        }

    }//printSvfStdTotalAttendOut()の括り



    /** 
     *  クラス総合・平均を出力
     *      総合点から遅刻回数
     */
    private void printSvfTotalOut() {
        try {
            if (0 < hr_seitosu[8] )
                svf.VrsOut( "TOTAL51",    String.valueOf( Math.round( (float)hr_total / hr_seitosu[8] ) ) );  //総合点 04/11/10Modify => 小数点第１位で四捨五入
log.debug("hrtotalnum="+hrtotalnum+"  hrtotalcnt="+hrtotalcnt);
            //if (0 < hr_seitosu[9] ) {
            if (0 < hrtotalcnt ) {
                //svf.VrsOut( "AVERAGE51", String.valueOf( hr_average ) );        //平均点 04/11/04Modify 04/11/08Modify
                //svf.VrsOut( "TOTAL53",   String.valueOf( hr_seitosu[9] ) ); //総合点 04/11/08Add
                svf.VrsOut( "AVERAGE51", String.valueOf( Math.round( (float)hrtotalnum / hrtotalcnt )) );     //平均点 04/11/04Modify 04/11/08Modify 05/03/10Moidfy
                svf.VrsOut( "TOTAL53",   String.valueOf( hrtotalnum ) );  //総合点 04/11/08Add 05/03/10Modify
            }

            if (0 < hr_seitosu[0] ) svf.VrsOut( "SUSPEND52", String.valueOf( hr_attend[0] ) );    //出停
            if (0 < hr_seitosu[1] ) svf.VrsOut( "KIBIKI52",  String.valueOf( hr_attend[1] ) );    //忌引
            if (0 < hr_seitosu[3] ) svf.VrsOut( "PRESENT52", String.valueOf( hr_attend[3] ) );    //出席すべき日数
            if (0 < hr_seitosu[4] ) svf.VrsOut( "ABSENCE52", String.valueOf( hr_attend[4] ) );    //欠席日数
            if (0 < hr_seitosu[5] ) svf.VrsOut( "ATTEND52",  String.valueOf( hr_attend[5] ) );    //出席日数
            if (0 < hr_seitosu[6] ) svf.VrsOut( "LEAVE52",   String.valueOf( hr_attend[6] ) );    //早退回数
            if (0 < hr_seitosu[7] ) svf.VrsOut( "TOTAL_LATE52",  String.valueOf( hr_attend[7] ) );    //遅刻回数
            if (0 < hr_seitosu[9] ) svf.VrsOut( "LATE_FREQUENCY52",  String.valueOf( hr_attend[9] ) );    //遅刻度数 KNJD062H

            if (0 < hr_attend[3] ) {
                svf.VrsOut( "PER_ATTEND",  String.valueOf( (float)Math.round( ((float)hr_attend[5] / (float)hr_attend[3]) * 1000 ) / 10 ) );  //出席率
                svf.VrsOut( "PER_ABSENCE", String.valueOf( (float)Math.round( ((float)hr_attend[4] / (float)hr_attend[3]) * 1000 ) / 10 ) );  //欠席率
            }

            // 05/01/31Modify  学年末の処理で単位の合計を出力
            //NO013 if (_param._1.equals("9") &&  0 < hr_credits )
            //NO013     svf.VrsOut( "credit20", hr_credits + "単位" );
            //NO022 if (_param._1.equals("9") )svf.VrsOut( "credit20", "---" );  //NO013
            getRCreditsHr();  //NO022
            if (_param._semester.equals("9") )svf.VrsOut( "credit20", hr_credits + "単位" );  //NO022

            // 05/01/31Modify  授業日数最大値を出力 => 出欠統計の仕様に合わせる
            // NO013Delete  NO021Revival
            if (0 < hr_lesson[0] )       //授業日数最大値があれば出力 04/11/01Add
                svf.VrsOut( "lesson20", hr_lesson[0] + "日" );
            // NO021 svf.VrsOut( "lesson20", "---" );  //NO013

        } catch (Exception ex) {
            log.warn("group-average svf-out error!", ex);
        }

    }//printSvfTotalOut()の括り


    /** 
     * SQLStatement作成 ＨＲクラス生徒名の表(生徒名) 04/11/08Modify 
     *   SEMESTER_MATは_param._1で検索 => 学年末'9'有り
     *   SCHREG_REGD_DATは_param._13で検索 => 学年末はSCHREG_REGD_HDATの最大学期
     */
    private String prestatStdNameList() {

        StringBuffer stb = new StringBuffer();
        try {

            stb.append("SELECT  W1.SCHREGNO,W1.ATTENDNO,W3.NAME,");
            //stb.append(        "W4.GRD_DATE AS KBN_DATE1,");
            //stb.append(        "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A003' AND S1.NAMECD2 = W4.GRD_DIV) AS KBN_NAME1,");
            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");   //05/09/30MODIFY
            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A003' AND S1.NAMECD2 = W4.GRD_DIV) ");  //05/09/30Modify
            stb.append(             "ELSE (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A002' AND S1.NAMECD2 = W4.ENT_DIV) END AS KBN_NAME1, ");             //05/09/30Modify

            stb.append(        "W5.TRANSFER_SDATE AS KBN_DATE2,");
            stb.append(        "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS KBN_NAME2 ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER  JOIN SEMESTER_MST    W2 ON W2.SEMESTER = '" + _param._semester + "' AND W1.YEAR = W2.YEAR ");
            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("LEFT   JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            //stb.append(                            "AND W4.GRD_DIV IN ('2','3') AND W4.GRD_DATE <= EDATE ");
            //stb.append(                            "AND W4.GRD_DIV IN ('2','3') AND W4.GRD_DATE < EDATE ");  //05/03/09Modify
            //stb.append(                              "AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < CASE WHEN W2.EDATE < '" + _param._4 + "' THEN W2.EDATE ELSE '" + _param._4 + "' END) ");    //05/09/30Modify
            //stb.append(                                "OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + _param._4 + "' THEN W2.EDATE ELSE '" + _param._4 + "' END)) ");   //05/09/30Modify
            stb.append(                              "AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < W2.EDATE) ");    //05/09/30Modify
            stb.append(                                "OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE > W2.SDATE)) ");   //05/09/30Modify
            stb.append("LEFT   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            //stb.append(                                "AND ((W5.TRANSFERCD IN ('1','2') AND W5.TRANSFER_SDATE <= W2.EDATE  AND W2.EDATE <= W5.TRANSFER_EDATE ) ");
            //stb.append(                                  " OR(W5.TRANSFERCD IN ('4') AND W2.EDATE < W5.TRANSFER_SDATE) ) ");
            //stb.append(                                  "AND (W5.TRANSFERCD IN ('1','2') AND CASE WHEN W2.EDATE < '" + _param._4 + "' THEN W2.EDATE ELSE '" + _param._4 + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");  //05/09/30Modify
            stb.append(                                  "AND (W5.TRANSFERCD IN ('1','2') AND W2.EDATE BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");  //05/09/30Modify
            stb.append("WHERE   W1.YEAR = '" + _param._year + "' ");
            stb.append(    "AND W1.GRADE||W1.HR_CLASS = ? ");       //05/05/30
            stb.append(    "AND W1.SEMESTER = '" + _param._regdSemester + "' ");
            stb.append("ORDER BY W1.ATTENDNO");

        } catch (Exception ex) {
            log.warn("sql-statement error!", ex);
        }
        return stb.toString();

    }//prestatStdNameListの括り


    /** 
     *  SQLStatement作成 ＨＲ履修科目の表(教科名・科目名・単位・授業時数・平均) 
     *  2005/02/02 学年末では評価読替え科目を出力する
     *  2005/05/22 科目名の略称を表記
     *  2005/08/17 時数取得の仕方を変更するとともに、対象科目取得の表(CHAIR_A)を簡略化
     */
    private String prestatSubclassInfo() {

        StringBuffer stb = new StringBuffer();

        try {
            //学籍の表 04/11/08Add
            stb.append("WITH SCHNO AS(");
            stb.append(     "SELECT  W1.YEAR, W1.SEMESTER, W1.SCHREGNO, W1.ATTENDNO,");
            stb.append(             "W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append(     "FROM   SCHREG_REGD_DAT W1 ");
            stb.append(     "INNER  JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + _param._year + "' ");  //05/09/30Build
            stb.append(     "WHERE   W1.YEAR = '" + _param._year + "' ");
            stb.append(         "AND W1.SEMESTER = '" + _param._regdSemester + "' ");
            stb.append(         "AND W1.GRADE||W1.HR_CLASS = ? ");  //05/05/30

            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + _param._edate + "' THEN W2.EDATE ELSE '" + _param._edate + "' END) ");     //05/09/30Modify
            stb.append(                              "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + _param._edate + "' THEN W2.EDATE ELSE '" + _param._edate + "' END)) ) ");  //05/09/30Modify
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN W2.EDATE < '" + _param._edate + "' THEN W2.EDATE ELSE '" + _param._edate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //05/09/30Modify
            /* ***
            stb.append(             "NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                        "WHERE  S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN ('2','3') AND S1.GRD_DATE < '" + _param._4 + "' ) AND ");    //05/03/09Modify異動日は在籍とする
            stb.append(             "NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                        "WHERE  S1.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(                               "((S1.TRANSFERCD IN ('1','2') AND '" + _param._4 + "' BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) OR ");  // 05/02/10停学を除外
            stb.append(                                "(S1.TRANSFERCD IN ('4') AND '" + _param._4 + "' < S1.TRANSFER_SDATE)) ) ");
            *** */
            stb.append(     ") ");

            //講座の表 05/01/31Modify 
            //05/08/17Modify 時間割の存在チェックを外し学期の条件を加え、科目・群別の表に変更
            //05/08/18Modify 科目の表に変更
            stb.append(",CHAIR_A AS(");
            //stb.append(   "SELECT  K2.CHAIRCD,K2.SUBCLASSCD,K2.SEMESTER,K2.GROUPCD ");
            //stb.append(   "SELECT  K2.SUBCLASSCD,K2.GROUPCD ");
            stb.append(     "SELECT  ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    K2.CLASSCD || '-' || K2.SCHOOL_KIND AS CLASSCDS, ");
                stb.append("    K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
            }
            stb.append(             "K2.SUBCLASSCD AS SUBCLASSCD,MAX(K2.GROUPCD)AS GROUPCD ");
            stb.append(     "FROM    CHAIR_CLS_DAT K1,CHAIR_DAT K2 ");
            stb.append(     "WHERE   K1.YEAR = K2.YEAR ");
            stb.append(         "AND K1.SEMESTER = K2.SEMESTER ");
            stb.append(         "AND (K1.CHAIRCD = '0000000' OR K1.CHAIRCD = K2.CHAIRCD) ");
            stb.append(         "AND K1.YEAR = '" + _param._year + "' ");
            stb.append(         "AND K2.YEAR = '" + _param._year + "' ");
            stb.append(         "AND K1.TRGTGRADE||K1.TRGTCLASS = ? ");  //05/05/30
            stb.append(         "AND K1.GROUPCD = K2.GROUPCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(         "AND (K2.CLASSCD <= '" + KNJDefineCode.subject_U + "' ");
                stb.append(             "OR  K2.CLASSCD = '" + KNJDefineCode.subject_T + "') ");
            } else {
                stb.append(         "AND (SUBSTR(SUBCLASSCD,1,2) <= '" + KNJDefineCode.subject_U + "' ");
                stb.append(             "OR  SUBSTR(SUBCLASSCD,1,2) = '" + KNJDefineCode.subject_T + "') ");
            }
            if (! _param._semester.equals("9") ) {
                stb.append(     "AND K1.SEMESTER = '" + _param._semester + "' ");
                stb.append(     "AND K2.SEMESTER = '" + _param._semester + "' ");
            }
            /* ****************
            stb.append(            "(K2.CHAIRCD,K2.SEMESTER) IN(");
            stb.append(                     "SELECT CHAIRCD,SEMESTER FROM SCH_CHR_DAT ");
            stb.append(                     "WHERE  EXECUTEDATE BETWEEN '" + _param._6 + "' AND '" + _param._7 + "' ");
            stb.append(                     "GROUP BY CHAIRCD,SEMESTER) ");
            ****************** */
            //stb.append(   "GROUP BY K2.CHAIRCD,K2.SUBCLASSCD,K2.SEMESTER,K2.GROUPCD ");
            //stb.append(   "GROUP BY K2.SUBCLASSCD,K2.GROUPCD ");
            stb.append(     "GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    K2.CLASSCD || '-' || K2.SCHOOL_KIND, ");
                stb.append("    K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
            }
            stb.append(         " K2.SUBCLASSCD ");
            stb.append(     ")");

            //評価読替前科目の表 NO008 Build
            stb.append(",REPLACE_REC_MOTO AS(");
            stb.append(        "SELECT  ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(                 "ATTEND_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(        "FROM    SUBCLASS_REPLACE_DAT T1 ");
            stb.append(        "WHERE   YEAR ='" + _param._year + "' AND ANNUAL = '" + _param._grade + "' AND REPLACECD = '1' ");
            stb.append(            "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE T2.SUBCLASSCD = ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(               "T1.ATTEND_SUBCLASSCD) ");  //NO0017
            stb.append(        "GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(               "ATTEND_SUBCLASSCD ");
            stb.append(     ") ");
            //評価読替後科目の表 NO008 Build
            stb.append(",REPLACE_REC_SAKI AS(");
            stb.append(        "SELECT  ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.GRADING_CLASSCD || '-' || T1.GRADING_SCHOOL_KIND AS CLASSCDS, ");
                stb.append("    T1.GRADING_CLASSCD || '-' || T1.GRADING_SCHOOL_KIND || '-' || T1.GRADING_CURRICULUM_CD || '-' || ");
            }
            stb.append(                 "GRADING_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(        "FROM    SUBCLASS_REPLACE_DAT T1 ");
            stb.append(        "WHERE   YEAR ='" + _param._year + "' AND ANNUAL = '" + _param._grade + "' AND REPLACECD = '1' ");
            stb.append(            "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE T2.SUBCLASSCD = ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(                 "T1.ATTEND_SUBCLASSCD) ");  //NO0017
            stb.append(        "GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.GRADING_CLASSCD || '-' || T1.GRADING_SCHOOL_KIND, ");
                stb.append("    T1.GRADING_CLASSCD || '-' || T1.GRADING_SCHOOL_KIND || '-' || T1.GRADING_CURRICULUM_CD || '-' || ");
            }
            stb.append(            "GRADING_SUBCLASSCD ");
            stb.append(     ") ");
            //評価読替え科目を抽出 05/02/02
            /* NO008 
            //if (_param._1.equals("9") ) {
            if (_param._11.equals("0") ) {       //05/03/01Modify
                stb.append(",REPLACE_REC AS(");
                stb.append(     "SELECT W2.GRADING_SUBCLASSCD AS SUBCLASSCD ");
                stb.append(     "FROM   RECORD_DAT W1, SUBCLASS_REPLACE_DAT W2 ");
                stb.append(     "WHERE  W1.YEAR = '" + _param._0 + "' AND ");
                stb.append(            "W1.SUBCLASSCD=W2.ATTEND_SUBCLASSCD AND ");
                stb.append(            "EXISTS(SELECT 'X' FROM SCHNO W3 WHERE W3.SCHREGNO = W1.SCHREGNO) AND ");
                stb.append(            "W2.YEAR ='" + _param._0 + "' AND ANNUAL = '" + _param._2 + "' AND REPLACECD = '1' ");  //05/05/22
                stb.append(     "GROUP BY GRADING_SUBCLASSCD ");
                stb.append(     ") ");
            }
            */

            //NO010
            stb.append(",CREDITS_UNCONDITION AS(");
            stb.append(    "SELECT  ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCDS, ");
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(             "SUBCLASSCD AS SUBCLASSCD, CREDITS ");
            stb.append(    "FROM    CREDIT_MST T1 ");
            stb.append(    "WHERE   T1.YEAR = '" + _param._year + "' ");
            stb.append(        "AND EXISTS(SELECT  'X' FROM SCHNO T2 ");
            stb.append(                   "WHERE   T1.GRADE = T2.GRADE ");
            stb.append(                       "AND T1.COURSECD = T2.COURSECD ");
            stb.append(                       "AND T1.MAJORCD = T2.MAJORCD ");
            stb.append(                       "AND T1.COURSECODE = T2.COURSECODE) ");
            stb.append(         "AND VALUE(T1.COMP_UNCONDITION_FLG,'0') = '1' ");
            stb.append(         "AND NOT EXISTS(SELECT 'X' FROM CHAIR_A T3 WHERE T3.SUBCLASSCD = ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(         " T1.SUBCLASSCD) ");
            stb.append(") ");

            //メイン表
            stb.append("SELECT W2.SUBCLASSCD,W2.GROUPCD,W4.SUBCLASSABBV AS SUBCLASSNAME,W4.ELECTDIV,W5.CLASSABBV,");
            stb.append(       "W7.MAXCREDITS,W7.MINCREDITS,");
            stb.append(       "W9.AVG_HR,W9.SUM_HR,");    // 04/11/08Add
            //stb.append(         "ASSESS_GR,");
            stb.append(       "VALUE(W6.JISU,0) + VALUE(W10.LESSON,0) AS JISU ");
            stb.append(      ",CASE WHEN W11.SUBCLASSCD IS NOT NULL THEN 'MOTO' WHEN W12.SUBCLASSCD IS NOT NULL THEN 'SAKI' ELSE NULL END AS REPLACEFLG ");   //NO007Build / NO008Modify
            stb.append("FROM(");
            stb.append(    "SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    CLASSCDS, ");
            }
            stb.append(        " SUBCLASSCD, GROUPCD ");
            stb.append(        "FROM    CHAIR_A w1 ");
            stb.append(        "WHERE   NOT EXISTS( SELECT 'X' FROM REPLACE_REC_SAKI W2 WHERE W2.SUBCLASSCD = W1.SUBCLASSCD) ");  //NO005
            //NO008 if (_param._11.equals("0") )  //NO005
            //NO008     stb.append(    "WHERE   NOT EXISTS( SELECT 'X' FROM REPLACE_REC W2 WHERE W2.SUBCLASSCD = W1.SUBCLASSCD) ");  //NO005
            //NO005 stb.append(        "SELECT SUBCLASSCD, groupcd FROM CHAIR_A "); // 05/08/17Modify
            //stb.append(        "SELECT SUBCLASSCD, max(groupcd)as groupcd FROM CHAIR_A group by subclasscd "); // 05/01/31Modify
            //評価読替え科目を追加 05/02/02
            if (_param._semester.equals("9") ) {
            //NO008 if (_param._11.equals("0") ) {         //05/03/01Modify
                stb.append(    "UNION ALL ");
                stb.append(    "SELECT ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("    CLASSCDS, ");
                }
                stb.append(    " SUBCLASSCD, '0000' AS GROUPCD FROM REPLACE_REC_SAKI ");
                //NO008 stb.append(    "SELECT SUBCLASSCD, '0000' AS GROUPCD FROM REPLACE_REC ");
            }
                //NO010
            stb.append("UNION ALL ");
            stb.append("SELECT  ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    W1.CLASSCDS, ");
            }
            stb.append("        W1.SUBCLASSCD, '0000' AS GROUPCD ");
            stb.append("FROM    CREDITS_UNCONDITION W1 ");

            stb.append(    ") W2 ");

            stb.append(    "INNER JOIN SUBCLASS_MST W4 ON ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    W4.CLASSCD || '-' || W4.SCHOOL_KIND || '-' || W4.CURRICULUM_CD || '-' || ");
            }
            stb.append(                " W4.SUBCLASSCD = W2.SUBCLASSCD ");
            stb.append(    "INNER JOIN CLASS_MST W5 ON W5.CLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    || '-' || W5.SCHOOL_KIND ");
            }
            stb.append(               " = ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    W2.CLASSCDS ");
            } else {
                stb.append("    SUBSTR(W2.SUBCLASSCD,1,2)");
            }

            //授業時数(集計漏れ)の表 05/08/17Modify 生徒毎の時数のMAX値をとる
            stb.append(    "LEFT JOIN( ");
            stb.append(       "SELECT ");
            stb.append(             "T1.SUBCLASSCD, MAX(JISU) AS JISU ");
            stb.append(       "FROM(   SELECT  T2.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append(                "T3.SUBCLASSCD AS SUBCLASSCD,COUNT(*) AS JISU ");
            stb.append(               "FROM    SCH_CHR_DAT T1, CHAIR_STD_DAT T2,CHAIR_DAT T3 ");
            stb.append(               "WHERE   T1.YEAR = '" + _param._year + "' ");
            stb.append(                   "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(                   "AND T1.EXECUTEDATE BETWEEN ? AND ? ");
            stb.append(                   "AND T1.YEAR = T2.YEAR ");
            stb.append(                   "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(                   "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(                   "AND T2.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) ");
            stb.append(                   "AND T3.YEAR='" + _param._year + "' ");
            stb.append(                   "AND T3.SEMESTER = T1.SEMESTER ");
            stb.append(                   "AND T3.CHAIRCD = T1.CHAIRCD ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + _param._year + "' AND L1.DI_CD = T4.DI_CD ");
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
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + _param._year + "' AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append(               "GROUP BY T2.SCHREGNO,");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append(                        "T3.SUBCLASSCD ");
            stb.append(           ")T1 ");
            stb.append(       "GROUP BY T1.SUBCLASSCD ");
            /* *******************
            stb.append(        "SELECT  SUBCLASSCD,COUNT(PERIODCD)AS JISU ");
            stb.append(        "FROM    CHAIR_A W1,SCH_CHR_DAT W2 ");
            stb.append(        "WHERE   W2.YEAR = '" + _param._0 + "' AND ");
            stb.append(                "W2.EXECUTEDATE BETWEEN ? AND ? AND ");
            stb.append(                "W1.CHAIRCD = W2.CHAIRCD AND W1.SEMESTER = W2.SEMESTER ");
            stb.append(        "GROUP BY SUBCLASSCD");
            ********************* */
            stb.append(    ")W6 ON W6.SUBCLASSCD = W2.SUBCLASSCD ");

            //授業時数(集計済)の表 05/08/17Modify 生徒毎の時数のMAX値をとる => 元は月毎のMAX値を集計
            stb.append(    "LEFT JOIN(");
            stb.append(        "SELECT  S1.SUBCLASSCD,");
            stb.append(                "MAX(LESSON) AS LESSON ");
            stb.append(        "FROM(");
            stb.append(             "SELECT  schregno, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                      "W1.SUBCLASSCD AS SUBCLASSCD,");
            stb.append(                     "SUM(LESSON) AS LESSON ");
            stb.append(             "FROM    ATTEND_SUBCLASS_DAT W1 ");
            stb.append(             "WHERE   YEAR = '" + _param._year + "' AND ");
            if (! _param._semester.equals("9") )
                stb.append(                 "SEMESTER = '" + _param._semester + "' AND ");
            stb.append(                     "(CASE WHEN INT(W1.MONTH) < 4 THEN RTRIM(CHAR(INT(W1.SEMESTER) + 1 )) ELSE W1.SEMESTER END )||W1.MONTH <= ? AND ");   //--NO004 NO007
            //NO004 stb.append(                     "SEMESTER||MONTH <= ? AND ");
            stb.append(                     "EXISTS(");
            stb.append(                            "SELECT  'X' ");
            stb.append(                            "FROM    SCHNO W2 ");
            stb.append(                            "WHERE   W1.SCHREGNO = W2.SCHREGNO)");
            stb.append(             "GROUP BY schregno, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                     " W1.SUBCLASSCD ");
            stb.append(             ")S1 ");
            stb.append(        "GROUP BY S1.SUBCLASSCD ");
            stb.append(    ")W10 ON W10.SUBCLASSCD = W2.SUBCLASSCD ");

            //単位の表
            stb.append(    "LEFT JOIN( ");
            stb.append(       "SELECT  ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                "W1.SUBCLASSCD AS SUBCLASSCD,MAX(CREDITS) AS MAXCREDITS, MIN(CREDITS) AS MINCREDITS ");
            stb.append(       "FROM    CREDIT_MST W1 ");
            stb.append(       "WHERE   W1.YEAR = '" + _param._year + "' AND ");
            stb.append(               "W1.GRADE = '" + _param._grade + "' AND ");   //05/05/22
            stb.append(               "(W1.COURSECD, W1.MAJORCD, W1.COURSECODE) ");
            stb.append(                   "IN(SELECT COURSECD, MAJORCD, COURSECODE ");
            stb.append(                      "FROM   SCHREG_REGD_DAT ");
            stb.append(                      "WHERE  YEAR = '" + _param._year + "' AND ");
            stb.append(                             "GRADE||HR_CLASS = ? AND ");  //05/05/30
            stb.append(                             "SEMESTER = '" + _param._regdSemester + "')");
            stb.append(       "GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(               "W1.SUBCLASSCD");
            stb.append(    ")W7 ON W7.SUBCLASSCD = W2.SUBCLASSCD ");
            //科目別合計点および平均点の表 04/11/08Add
            stb.append(    "LEFT JOIN( ");
            stb.append(        "SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append("                W1.SUBCLASSCD AS SUBCLASSCD,");
            stb.append(                "INT(ROUND(AVG(FLOAT(" + fieldname + ")),0))AS AVG_HR,");
            stb.append(                "SUM(" + fieldname + ")AS SUM_HR ");
            stb.append(         "FROM   RECORD_DAT W1 ");
            stb.append(         "INNER  JOIN SCHNO W2 ON W2.SCHREGNO = W1.SCHREGNO ");
            stb.append(         "WHERE  W1.YEAR = '" + _param._year + "' AND W1.SUBCLASSCD <= '" + KNJDefineCode.subject_U + "' AND ");
            stb.append(                 fieldname + " IS NOT NULL ");
            stb.append(         "GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                " W1.SUBCLASSCD");
            stb.append(    ")W9 ON W9.SUBCLASSCD = W2.SUBCLASSCD ");
            //評価読替前科目の表 NO007 Build / NO008 Modify
            stb.append(    "LEFT JOIN REPLACE_REC_MOTO W11 ON W11.SUBCLASSCD = W2.SUBCLASSCD ");
            //評価読替後科目の表 NO007 Build / NO008 Modify
            stb.append(    "LEFT JOIN REPLACE_REC_SAKI W12 ON W12.SUBCLASSCD = W2.SUBCLASSCD ");

            stb.append("ORDER BY W2.SUBCLASSCD ");

        } catch (Exception ex) {
            log.warn("sql-statement error!", ex);
        }
//log.debug("sql="+stb.toString());
        return stb.toString();

    }//prestatSubclassInfo()の括り


    /**
     *  PrepareStatement作成 --> 成績・評定・欠課データの表 
     *  2005/06/20 Modify ペナルティ欠課の算出式を修正
     *  @param absentDiv 1:年間 2:随時
     */
    private String prestatStdSubclassDetail(final String absentDiv) {

        StringBuffer stb = new StringBuffer();

        try {
            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append("SCHNO_A AS(");
            stb.append(     "SELECT  W1.SCHREGNO,W1.SEMESTER ");
            stb.append(            ",W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");  //NO010
            stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
            stb.append(     "WHERE   W1.YEAR = '" + _param._year + "' AND ");
            stb.append(             "W1.SEMESTER = '" + _param._regdSemester + "' AND ");
            stb.append(             "W1.GRADE||W1.HR_CLASS = ? AND ");
            stb.append(             "W1.ATTENDNO BETWEEN ? AND ? ");
            stb.append(     ") ");

            //対象生徒の表 クラスの生徒から異動者を除外
            stb.append(",SCHNO_B AS(");
            stb.append(     "SELECT  W1.SCHREGNO,W1.SEMESTER ");
            stb.append(            ",W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");  //NO010
            stb.append(     "FROM    SCHNO_A W1 ");
            stb.append(     "INNER   JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + _param._year + "' ");  //05/09/30Build

            stb.append(     "WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + _param._edate + "' THEN W2.EDATE ELSE '" + _param._edate + "' END) ");     //05/09/30Modify
            stb.append(                              "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + _param._edate + "' THEN W2.EDATE ELSE '" + _param._edate + "' END)) ) ");  //05/09/30Modify
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN W2.EDATE < '" + _param._edate + "' THEN W2.EDATE ELSE '" + _param._edate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //05/09/30Modify
            stb.append(     ") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append(     "SELECT K1.SCHREGNO, K2.CHAIRCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "K2.SUBCLASSCD AS SUBCLASSCD, K2.SEMESTER, K1.APPDATE, K1.APPENDDATE ");
            stb.append(     "FROM   CHAIR_STD_DAT K1, CHAIR_DAT K2 ");
            stb.append(     "WHERE  K1.YEAR = '" + _param._year + "' ");
            stb.append(        "AND K1.SEMESTER <= '" + _param._semester + "' ");
            stb.append(        "AND K2.YEAR = K1.YEAR ");
            stb.append(        "AND K1.SEMESTER = K2.SEMESTER ");
            stb.append(        "AND (K1.CHAIRCD = K2.CHAIRCD OR ");
            stb.append(            " K1.CHAIRCD = '000000') ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(         "AND (K2.CLASSCD <= '" + KNJDefineCode.subject_U + "' ");
                stb.append(             "OR  K2.CLASSCD = '" + KNJDefineCode.subject_T + "') ");
            } else {
                stb.append(         "AND (SUBSTR(SUBCLASSCD,1,2) <= '" + KNJDefineCode.subject_U + "' ");
                stb.append(             "OR  SUBSTR(SUBCLASSCD,1,2) = '" + KNJDefineCode.subject_T + "') ");
            }
            stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = K1.SCHREGNO)");
            stb.append(     ")");

            stb.append(" ,TEST_COUNTFLG AS ( ");
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

            // 時間割(休学・留学を含む)
            stb.append(", SCHEDULE_SCHREG_R AS( ");
            stb.append(    "SELECT T2.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(    "FROM   SCH_CHR_DAT T1, CHAIR_A T2 ");
            stb.append(    "WHERE  T1.EXECUTEDATE BETWEEN '" + _param._divideAttendDate + "' AND '" + _param._edate + "' ");
            stb.append(        "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(        "AND T1.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T1.SEMESTER <= '" + _param._semester + "' ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T4 ");  //NO025
            stb.append(                        "WHERE   T4.SCHREGNO = T2.SCHREGNO ");  //NO025
            stb.append(                         "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");  //NO025
            stb.append(                           "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE )) ) ");  //NO025
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + _param._year + "' AND L1.DI_CD = T4.DI_CD ");
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
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + _param._year + "' AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            if (_param.definecode.useschchrcountflg) {
                stb.append(                "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
                stb.append(                               "WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE AND ");
                stb.append(                                       "T4.PERIODCD = T1.PERIODCD AND ");
                stb.append(                                       "T4.CHAIRCD = T1.CHAIRCD AND ");
                stb.append(                                       "T1.DATADIV IN ('0', '1') AND ");
                stb.append(                                       "T4.GRADE||T4.HR_CLASS = ? AND ");
                stb.append(                                       "T4.COUNTFLG = '0') ");
                stb.append(                "AND NOT EXISTS(SELECT  'X' FROM TEST_COUNTFLG TEST ");
                stb.append(                               "WHERE ");
                stb.append(                                       "TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
                stb.append(                                       "AND TEST.PERIODCD = T1.PERIODCD ");
                stb.append(                                       "AND TEST.CHAIRCD  = T1.CHAIRCD ");
                stb.append(                                       "AND TEST.DATADIV  = T1.DATADIV) ");
            }
            stb.append("GROUP BY T2.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(")");

            // 時間割(休学・留学を含まない)
            stb.append(", SCHEDULE_SCHREG AS( ");
            stb.append(    "SELECT T1.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(    "FROM SCHEDULE_SCHREG_R T1 ");
            stb.append(    "WHERE NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                       "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                         "AND TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            stb.append("  GROUP BY T1.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(")");

            // 休学時数
            stb.append(", OFFDAYS_SCHREG AS( ");
            stb.append(    "SELECT T1.SCHREGNO, SUBCLASSCD, T1.SEMESTER, COUNT(*) AS OFFDAYS ");
            stb.append(    "FROM SCHEDULE_SCHREG_R T1 ");
            stb.append(    "WHERE EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                       "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                         "AND TRANSFERCD = '2' AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            stb.append("  GROUP BY T1.SCHREGNO, SUBCLASSCD, T1.SEMESTER ");
            stb.append(")");

            //欠課数の表
            stb.append(",ATTEND_A AS(");
                                  //出欠データより集計
            stb.append(          "SELECT S1.SCHREGNO, SUBCLASSCD, SEMESTER,");
            stb.append(                 "COUNT(*) AS JISU, ");
            stb.append(                 "SUM(CASE WHEN (CASE WHEN L1.REP_DI_CD IN ('29','30','31') THEN VALUE(L1.ATSUB_REPL_DI_CD, L1.REP_DI_CD) ELSE L1.REP_DI_CD END) IN('4','5','6','14','11','12','13'");
            if ("1".equals(_param._knjSchoolMst._subAbsent)) {
                stb.append(                                   ",'1','8'");
            }
            if ("1".equals(_param._knjSchoolMst._subSuspend)) {
                stb.append(                                   ",'2','9'");
            }
            if ("1".equals(_param._knjSchoolMst._subMourning)) {
                stb.append(                                   ",'3','10'");
            }
            if ("true".equals(_param._useVirus)) {
                if ("1".equals(_param._knjSchoolMst._subVirus)) {
                    stb.append(                                   ",'19','20'");
                }
            }
            if ("true".equals(_param._useKoudome)) {
                if ("1".equals(_param._knjSchoolMst._subKoudome)) {
                    stb.append(                                   ",'25','26'");
                }
            }
            stb.append(                                        ") THEN 1 ELSE 0 END)AS ABSENT1, ");
            stb.append(                 "SUM(CASE WHEN (CASE WHEN L1.REP_DI_CD IN ('29','30','31') THEN VALUE(L1.ATSUB_REPL_DI_CD, L1.REP_DI_CD) ELSE L1.REP_DI_CD END) IN('15','16','23','24') THEN SMALLINT(VALUE(L1.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
            stb.append(          "FROM SCHEDULE_SCHREG S1 "); // 休学時数、留学時数を含まない
            stb.append(               "LEFT JOIN ATTEND_DAT S2 ON S2.YEAR = '" + _param._year + "' AND ");
            stb.append(                                      "S2.ATTENDDATE = S1.EXECUTEDATE AND ");
            stb.append(                                      "S2.PERIODCD = S1.PERIODCD AND ");
            stb.append(                                      "S1.SCHREGNO = S2.SCHREGNO ");
            stb.append("               LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + _param._year + "' AND L1.DI_CD = S2.DI_CD ");
            stb.append(          "GROUP BY S1.SCHREGNO, SUBCLASSCD, SEMESTER ");
            if ("1".equals(_param._knjSchoolMst._subOffDays)) {
                stb.append(      "UNION ALL ");
                stb.append(      "SELECT  T2.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER ");
                stb.append(           ",SUM(T2.OFFDAYS) AS JISU ");   // 授業時数に休学時数を減算する
                stb.append(           ",SUM(T2.OFFDAYS) AS ABSENT1 "); // 欠課時数に休学時数を加算する
                stb.append(           ",SUM(0) AS LATE_EARLY ");
                stb.append(      "FROM OFFDAYS_SCHREG T2 ");
                stb.append(      "GROUP BY T2.SCHREGNO, T2.SUBCLASSCD, T2.SEMESTER ");
            }
                                  //月別科目別出欠集計データより欠課を取得
            stb.append(          "UNION ALL ");
            stb.append(          "SELECT  W1.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append("         W1.SUBCLASSCD AS SUBCLASSCD, W1.SEMESTER, ");
            stb.append(                  "SUM(VALUE(LESSON,0) ");
            if (!"1".equals(_param._knjSchoolMst._subOffDays)) { 
                stb.append(                  " - VALUE(OFFDAYS,0) ");
            }
            stb.append(                  " - VALUE(ABROAD,0) ) AS JISU, ");
            stb.append(                  "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0) ");
            if ("1".equals(_param._knjSchoolMst._subAbsent))  {
                stb.append(                   "+ VALUE(ABSENT,0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subSuspend)) {
                stb.append(                   "+ VALUE(SUSPEND,0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subMourning)) {
                stb.append(                   "+ VALUE(MOURNING,0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subOffDays)) { 
                stb.append(                   "+ VALUE(OFFDAYS,0) ");
            }
            if ("true".equals(_param._useVirus)) {
                if ("1".equals(_param._knjSchoolMst._subVirus)) { 
                    stb.append(                   "+ VALUE(VIRUS,0) ");
                }
            }
            if ("true".equals(_param._useKoudome)) {
                if ("1".equals(_param._knjSchoolMst._subKoudome)) { 
                    stb.append(                   "+ VALUE(KOUDOME,0) ");
                }
            }
            stb.append(                    "   ) AS ABSENT1, ");
            stb.append(                  "SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
            stb.append(          "FROM    ATTEND_SUBCLASS_DAT W1, SCHNO_A W2 ");
            stb.append(          "WHERE   W1.YEAR = '" + _param._year + "' AND ");
            stb.append(                  "W1.SEMESTER <= '" + _param._semester + "' AND ");
            stb.append(                  "(CASE WHEN INT(W1.MONTH) < 4 THEN RTRIM(CHAR(INT(W1.SEMESTER) + 1 )) ELSE W1.SEMESTER END )||W1.MONTH <= '" + KNJC053_BASE.retSemesterMonthValue (_param._divideAttendMonth ) + "' AND ");   //--NO004 NO007
            stb.append(                  "W1.SCHREGNO = W2.SCHREGNO ");
            stb.append(          "GROUP BY W1.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append("                   W1.SUBCLASSCD, W1.SEMESTER ");
            stb.append(     ") ");

            //欠課数の表
            stb.append(",ATTEND_B AS(");
            //if (_param._5 == null ) {
            if (0 == _param.definecode.absent_cov ) {
                                //遅刻・早退を欠課換算しない
                stb.append(     "SELECT  W1.SCHREGNO, W1.SUBCLASSCD, SUM(ABSENT1)AS ABSENT1 ");
                stb.append(     "FROM    ATTEND_A W1 ");
                stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
            } else if (_param.definecode.absent_cov == 1 ) {
                                //遅刻・早退を学期で欠課換算する
                stb.append(     "SELECT  SCHREGNO, SUBCLASSCD, SUM(ABSENT1)AS ABSENT1 ");
                stb.append(     "FROM   (SELECT  SCHREGNO, SUBCLASSCD, ");
                stb.append(                     "VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _param.definecode.absent_cov_late + " AS ABSENT1 ");   //05/06/20Modify
                stb.append(             "FROM    ATTEND_A ");
                stb.append(             "GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
                stb.append(             ")W1 ");
                stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
            } else {
                                ////遅刻・早退を年間で欠課換算する
                stb.append(     "SELECT  SCHREGNO, SUBCLASSCD, ");
                stb.append(             "VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _param.definecode.absent_cov_late + " AS ABSENT1 ");   //05/06/20Modify
                stb.append(     "FROM    ATTEND_A ");
                stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
            }
            stb.append(     ") ");

            //NO010
            stb.append(",CREDITS_A AS(");
            stb.append(    "SELECT  T2.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("      T1.SUBCLASSCD AS SUBCLASSCD, T1.CREDITS, T1.COMP_UNCONDITION_FLG ");
            stb.append(    "FROM    CREDIT_MST T1 ");
            stb.append(     "INNER JOIN SCHNO_A T2 ON ");
            stb.append(        "T1.GRADE = T2.GRADE ");
            stb.append(        "AND T1.COURSECD = T2.COURSECD ");
            stb.append(        "AND T1.MAJORCD = T2.MAJORCD ");
            stb.append(        "AND T1.COURSECODE = T2.COURSECODE ");
            stb.append(    "WHERE   T1.YEAR = '" + _param._year + "' ");
            stb.append(") ");

            // 欠課数上限の表
            stb.append(",T_ABSENCE_HIGH AS(");
            stb.append(    "SELECT  SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("    SUBCLASSCD AS SUBCLASSCD, ");
            if (_param._knjSchoolMst.isJitu()) {
                stb.append(        " VALUE(T5.COMP_ABSENCE_HIGH, 99) ");
            } else {
                stb.append(        " VALUE(T1.ABSENCE_HIGH, 99) ");
            }
            stb.append(    "AS ABSENCE_HIGH ");
            stb.append(    "FROM ");
            if (_param._knjSchoolMst.isJitu()) {
                stb.append(    "SCHREG_ABSENCE_HIGH_DAT T5 ");
                stb.append(    "WHERE T5.YEAR = '" + _param._year + "' ");
                stb.append(      "AND T5.DIV = '" + absentDiv + "' ");
            } else {
                stb.append(    "CREDIT_MST T1 ");
                stb.append(      "INNER JOIN SCHNO_A T2 ON ");
                stb.append(          "T1.GRADE = T2.GRADE ");
                stb.append(          "AND T1.COURSECD = T2.COURSECD ");
                stb.append(          "AND T1.MAJORCD = T2.MAJORCD ");
                stb.append(          "AND T1.COURSECODE = T2.COURSECODE ");
                stb.append(    "WHERE T1.YEAR = '" + _param._year + "' ");
            }
            stb.append(") ");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append("    W3.SUBCLASSCD AS SUBCLASSCD ");
            if (! _param._11.equals("0") ) {
                //中間・期末成績  NO024 Modify
                // fieldname:SEM?_XXXX_SCORE / fieldname2:SEM?_XXXX
                stb.append(       ",CASE WHEN " + fieldname + " IS NOT NULL THEN RTRIM(CHAR(" + fieldname + ")) ");
                stb.append(             "WHEN " + fieldname + "_DI IS NOT NULL THEN " + fieldname + "_DI ");
                stb.append(             "ELSE NULL END AS SCORE ");
                stb.append(       ",CASE WHEN " + fieldname2 + "_VALUE IS NOT NULL THEN RTRIM(CHAR(" + fieldname2 + "_VALUE)) ");
                stb.append(             "WHEN " + fieldname2 + "_VALUE_DI IS NOT NULL THEN " + fieldname2 + "_VALUE_DI ");
                stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
                stb.append(        ",W3.COMP_CREDIT, W3.GET_CREDIT ");  //NO010
            } else if (Integer.parseInt( _param._semester ) != 9 ) {
                // fieldname:SEM?_VALUE
                //学期成績
                stb.append(       ",'' AS SCORE ");
                stb.append(       ",CASE WHEN " + fieldname + " IS NOT NULL THEN RTRIM(CHAR(" + fieldname + ")) ");
                stb.append(             "WHEN " + fieldname + "_DI IS NOT NULL THEN " + fieldname + "_DI ");
                stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
                stb.append(        ",W3.COMP_CREDIT, W3.GET_CREDIT ");  //NO010
            } else {
                //学年成績
                stb.append(       ",'' AS SCORE ");
                stb.append(       ",CASE WHEN " + fieldname + " IS NOT NULL THEN RTRIM(CHAR(" + fieldname + ")) ");
                stb.append(             "WHEN " + fieldname + "_DI IS NOT NULL THEN " + fieldname + "_DI ");
                stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
                stb.append(        ",W3.COMP_CREDIT, W3.GET_CREDIT ");  //NO010
            }
            stb.append(    "FROM    RECORD_DAT W3 ");
            stb.append(    "WHERE   W3.YEAR = '" + _param._year + "' AND ");
            stb.append(            "EXISTS(SELECT  'X' FROM SCHNO_B W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
            stb.append(     ") ");

            //NO010
            stb.append(",CREDITS_UNCONDITION AS(");
            stb.append(    "SELECT  SCHREGNO, SUBCLASSCD, CREDITS ");
            stb.append(    "FROM    CREDITS_A T1 ");
            stb.append(    "WHERE   VALUE(T1.COMP_UNCONDITION_FLG,'0') = '1' ");
            stb.append(        "AND NOT EXISTS(SELECT 'X' FROM RECORD_REC T3 WHERE T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO) ");
            stb.append(") ");

            //評価読替前科目の表 NO008 Build
            stb.append(",REPLACE_REC_MOTO AS(");
            stb.append(        "SELECT  ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("          ATTEND_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(        "FROM    SUBCLASS_REPLACE_DAT T1 ");
            stb.append(        "WHERE   YEAR ='" + _param._year + "' AND ANNUAL = '" + _param._grade + "' AND REPLACECD = '1' ");
            stb.append(            "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE T2.SUBCLASSCD = ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("                                        T1.ATTEND_SUBCLASSCD ");  //NO0017
            if (!_param._semester.equals("9")) {
                stb.append(                                                "AND T2.SEMESTER = '" + _param._semester + "' ");
            }
            stb.append(                      ")");
            
            stb.append(        "GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("          ATTEND_SUBCLASSCD ");
            stb.append(     ") ");
            //評価読替後科目の表 NO008 Build
            stb.append(",REPLACE_REC_SAKI AS(");
            stb.append(        "SELECT  ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.GRADING_CLASSCD || '-' || T1.GRADING_SCHOOL_KIND || '-' || T1.GRADING_CURRICULUM_CD || '-' || ");
            }
            stb.append("          GRADING_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(        "FROM    SUBCLASS_REPLACE_DAT T1 ");
            stb.append(        "WHERE   YEAR ='" + _param._year + "' AND ANNUAL = '" + _param._grade + "' AND REPLACECD = '1' ");
            stb.append(            "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE T2.SUBCLASSCD = ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("             T1.ATTEND_SUBCLASSCD ");  //NO0017
            if (!_param._semester.equals("9")) {
                stb.append(                                                "AND T2.SEMESTER = '" + _param._semester + "' ");
            }
            stb.append(                      ")");
            stb.append(        "GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.GRADING_CLASSCD || '-' || T1.GRADING_SCHOOL_KIND || '-' || T1.GRADING_CURRICULUM_CD || '-' || ");
            }
            stb.append("         GRADING_SUBCLASSCD ");
            stb.append(     ") ");

            //評定読替え科目評定の表
            if (_param._semester.equals("9") ) {   //NO008
                stb.append(",REPLACE_REC AS(");
                stb.append(     "SELECT SCHREGNO, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("    W2.GRADING_CLASSCD || '-' || W2.GRADING_SCHOOL_KIND || '-' || W2.GRADING_CURRICULUM_CD || '-' || ");
                }
                stb.append(            "W2.GRADING_SUBCLASSCD AS SUBCLASSCD, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("    W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append(            "W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
                stb.append(            "SCORE, ");
                stb.append(            "PATTERN_ASSESS ");
                stb.append(            ",COMP_CREDIT,GET_CREDIT ");  //NO010
                stb.append(     "FROM   RECORD_REC W1, SUBCLASS_REPLACE_DAT W2 ");
                stb.append(     "WHERE  W1.SUBCLASSCD = ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("    W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append("              W2.ATTEND_SUBCLASSCD AND ");
                stb.append(            "W2.YEAR='" + _param._year + "' AND ANNUAL='" + _param._grade + "' AND REPLACECD='1' ");  //05/05/22
                stb.append(     ") ");
                stb.append(",REPLACE_REC_ATTEND AS(");
                stb.append(     "SELECT SCHREGNO, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("    W2.GRADING_CLASSCD || '-' || W2.GRADING_SCHOOL_KIND || '-' || W2.GRADING_CURRICULUM_CD || '-' || ");
                }
                stb.append(           "W2.GRADING_SUBCLASSCD AS SUBCLASSCD, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("    W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append(           "W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
                stb.append(     "FROM   RECORD_DAT W1, SUBCLASS_REPLACE_DAT W2 ");
                stb.append(     "WHERE  W1.YEAR = '" + _param._year + "' AND ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
                }
                stb.append(     "       W1.SUBCLASSCD = ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("    W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append("              W2.ATTEND_SUBCLASSCD AND ");
                stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO) AND ");
                stb.append(            "W2.YEAR='" + _param._year + "' AND ANNUAL='" + _param._grade + "' AND REPLACECD='1' ");
                stb.append(     ") ");
            }

            //テスト欠席の表(明細)
            stb.append(",TEST_ATTEND_A AS (");
            stb.append(       "SELECT  T1.SEMESTER AS SEMES, T1.TESTKINDCD AS TESTKINDCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append("               T3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append(               "T2.SCHREGNO ");
            stb.append(       "FROM    SCH_CHR_TEST T1 ");
            stb.append("       INNER JOIN ATTEND_DAT T2 ON T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(           "AND T2.YEAR = '" + _param._year + "' ");
            stb.append(           "AND T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD ");
            stb.append("       INNER JOIN CHAIR_DAT T3 ON T3.YEAR = '" + _param._year + "' ");
            stb.append(           "AND T3.SEMESTER = T1.SEMESTER ");
            stb.append(           "AND T3.CHAIRCD = T2.CHAIRCD ");
            stb.append("       INNER JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + _param._year + "' AND L1.DI_CD = T2.DI_CD ");
            stb.append(       "WHERE   T1.YEAR = '" + _param._year + "' ");
            if (!_param._11.equals("0") )
                stb.append(       "AND T1.TESTKINDCD = '" + _param._11.substring( 0,2 ) + "' ");
            if (!_param._semester.equals("9") )
                stb.append(       "AND T1.SEMESTER = '" + _param._regdSemester + "' ");
            stb.append(           "AND EXISTS(SELECT 'X' FROM SCHNO_B W1 WHERE T2.SCHREGNO = W1.SCHREGNO GROUP BY W1.SCHREGNO) ");
            stb.append(           "AND L1.REP_DI_CD IN ('1','2','3','4','5','6','8','9','10','11','12','13','14') ");
            stb.append(") ");

            //テスト欠席の表(生徒別・科目別)
            stb.append(",TEST_ATTEND_B AS (");
            stb.append(       "SELECT  SCHREGNO, SUBCLASSCD ");
            stb.append(       "FROM    TEST_ATTEND_A ");
            stb.append(       "GROUP BY SCHREGNO, SUBCLASSCD ");
            stb.append(") ");


            //メイン表
            stb.append("SELECT  T1.SUBCLASSCD,T1.SCHREGNO ");
            stb.append(       ",T5.ABSENT1 ");
            stb.append(       ",T3.SCORE ");
                                    //教科コード'90'も同様に評定をそのまま出力 NO015
            stb.append(       ",T3.PATTERN_ASSESS ");
            if (_param._semester.equals("9") )  //NO008
                stb.append(   ",REPLACEMOTO ");
            stb.append(       ",CASE WHEN VALUE(T6.COMP_UNCONDITION_FLG,'0') = '1' AND T3.COMP_CREDIT IS NULL THEN T6.CREDITS ELSE T3.COMP_CREDIT END AS COMP_CREDIT ");  //NO0015  NO0018
            stb.append(       ",CASE WHEN VALUE(T6.COMP_UNCONDITION_FLG,'0') = '1' AND T3.GET_CREDIT IS NULL THEN T6.CREDITS ELSE T3.GET_CREDIT END AS GET_CREDIT ");  //NO0015  NO0018
            stb.append(       ",VALUE(T6.COMP_UNCONDITION_FLG,'0')AS COMP_UNCONDITION_FLG ");  //NO015
            stb.append(       ",T3.COMP_CREDIT AS ON_RECORD_COMP "); //NO0015 NO0018
            stb.append(       ",T3.GET_CREDIT AS ON_RECORD_GET "); //NO0015 NO0018
            stb.append(       ",T7.SCHREGNO AS ABSENT_SCH ");
            stb.append(       ",T8.ABSENCE_HIGH ");
    
            //対象生徒・講座の表
            stb.append("FROM(");
            stb.append(     "SELECT  W1.SCHREGNO,W2.SUBCLASSCD ");
            stb.append(     "FROM    CHAIR_STD_DAT W1, CHAIR_A W2, SCHNO_A W3 ");
            stb.append(     "WHERE   W1.YEAR = '" + _param._year + "' AND ");
            stb.append(             "W1.CHAIRCD = W2.CHAIRCD AND ");
            stb.append(             "W1.SEMESTER = W2.SEMESTER AND ");
            stb.append(             "W1.SCHREGNO = W3.SCHREGNO ");
            if (!_param._semester.equals("9")) {
                stb.append(     "AND W2.SEMESTER = '" + _param._semester + "' ");
            }
            stb.append(     "GROUP BY W1.SCHREGNO,W2.SUBCLASSCD ");

            if (_param._semester.equals("9") ) {
                stb.append( "UNION   SELECT SCHREGNO,SUBCLASSCD ");
                stb.append( "FROM    REPLACE_REC_ATTEND ");
                stb.append( "GROUP BY SCHREGNO,SUBCLASSCD ");
            }
            stb.append(     "UNION ");
            stb.append(     "SELECT  SCHREGNO, SUBCLASSCD ");
            stb.append(     "FROM    CREDITS_UNCONDITION S1 ");

            stb.append(")T1 ");

            //成績の表
            stb.append(  "LEFT JOIN(");
            //成績の表（通常科目）
            if (_param._semester.equals("9") ) {
                stb.append(     "SELECT W3.SCHREGNO, W3.SUBCLASSCD, W3.SCORE, W3.PATTERN_ASSESS, ");  //NO010
                stb.append(            "(SELECT  COUNT(*) ");
                stb.append(             "FROM    REPLACE_REC S1 ");
                stb.append(             "WHERE   S1.SCHREGNO = W3.SCHREGNO AND ");
                stb.append(                     "S1.ATTEND_SUBCLASSCD = W3.SUBCLASSCD ");
                stb.append(             "GROUP BY ATTEND_SUBCLASSCD) AS REPLACEMOTO ");
                stb.append(             ",COMP_CREDIT,GET_CREDIT ");  //NO010
                stb.append(     "FROM   RECORD_REC W3 ");
                stb.append(     "WHERE  NOT EXISTS( SELECT 'X' FROM REPLACE_REC S2 WHERE W3.SUBCLASSCD = S2.SUBCLASSCD) ");
            } else {
                stb.append(     "SELECT W3.SCHREGNO, W3.SUBCLASSCD, W3.SCORE, W3.PATTERN_ASSESS, ");  //NO010
                stb.append(            "0 AS REPLACEMOTO ");
                stb.append(           ",COMP_CREDIT,GET_CREDIT ");  //NO010
                stb.append(     "FROM   RECORD_REC W3 ");
            }
            if (_param._semester.equals("9") ) {
                                //評定読替科目 成績の表 NO006
                stb.append(     "UNION ALL ");
                stb.append(     "SELECT  W3.SCHREGNO, W3.SUBCLASSCD, ");
                stb.append(             "W3.SCORE AS SCORE, ");
                stb.append(             "W3.PATTERN_ASSESS, ");
                stb.append(             "-1 AS REPLACEMOTO ");
                stb.append(             ",COMP_CREDIT,GET_CREDIT ");  //NO010
                stb.append(     "FROM   RECORD_REC W3 ");
                stb.append(     "WHERE  EXISTS( SELECT 'X' FROM REPLACE_REC S2 WHERE W3.SUBCLASSCD = S2.SUBCLASSCD) ");
            }
            stb.append(     ")T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO ");

            //欠課数の表
            stb.append(  "LEFT JOIN(");
            stb.append(         "SELECT  SCHREGNO, SUBCLASSCD, ABSENT1 ");
            stb.append(         "FROM    ATTEND_B W1 ");

            if (_param._semester.equals("9") ) {
                                //評定読替科目 欠課数の表
                stb.append(     "UNION ");
                stb.append(     "SELECT  W1.SCHREGNO,W1.SUBCLASSCD,SUM(ABSENT1)AS ABSENT1 ");
                stb.append(     "FROM    REPLACE_REC_ATTEND W1, ATTEND_B W2 ");
                stb.append(     "WHERE   W1.SCHREGNO = W2.SCHREGNO AND W1.ATTEND_SUBCLASSCD = W2.SUBCLASSCD ");
                stb.append(         "AND NOT EXISTS(SELECT 'X' FROM ATTEND_B W3 WHERE W3.SUBCLASSCD = W1.SUBCLASSCD) ");  //NO0017
                stb.append(     "GROUP BY W1.SCHREGNO,W1.SUBCLASSCD ");
            }
            stb.append(  ")T5 ON T5.SUBCLASSCD = T1.SUBCLASSCD AND T5.SCHREGNO = T1.SCHREGNO ");

            stb.append("LEFT JOIN CREDITS_A T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.SUBCLASSCD = T1.SUBCLASSCD ");  //NO010
            stb.append("LEFT JOIN T_ABSENCE_HIGH T8 ON T8.SCHREGNO = T1.SCHREGNO AND T8.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("LEFT JOIN TEST_ATTEND_B T7 ON T7.SCHREGNO = T1.SCHREGNO AND T7.SUBCLASSCD = T1.SUBCLASSCD ");

            stb.append("ORDER BY T1.SUBCLASSCD, T1.SCHREGNO");

        } catch (Exception ex) {
            log.warn("sql-statement error!", ex);
            log.debug("ps="+stb.toString());
        }
        return stb.toString();

    }//prestatStdSubclassDetailの括り

    /** 
     *  PrepareStatement作成 成績総合データ 
     */
    private String prestatStdTotalRec()
    {
        StringBuffer stb = new StringBuffer();

        try {
            stb.append("WITH SCHNO_B AS(");
            stb.append(     "SELECT  W1.YEAR,W1.SEMESTER,W1.SCHREGNO,W1.ATTENDNO,");
            stb.append(             "W1.GRADE,W1.HR_CLASS,W1.COURSECD,W1.MAJORCD,W1.COURSECODE ");
            stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
            stb.append(     "INNER  JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + _param._year + "' ");  //05/09/30Build
            stb.append(     "WHERE   W1.YEAR = '" + _param._year + "' ");
            stb.append(         "AND W1.SEMESTER = '" + _param._regdSemester + "' ");
            stb.append(         "AND W1.GRADE||W1.HR_CLASS = ? ");          //05/05/30

            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN W2.EDATE < '" + _param._edate + "' THEN W2.EDATE ELSE '" + _param._edate + "' END) ");     //05/09/30Modify
            stb.append(                              "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN W2.EDATE < '" + _param._edate + "' THEN W2.EDATE ELSE '" + _param._edate + "' END)) ) ");  //05/09/30Modify
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO ");
            stb.append(                            "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN W2.EDATE < '" + _param._edate + "' THEN W2.EDATE ELSE '" + _param._edate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");  //05/09/30Modify

            stb.append(     ") ");

            //対象講座の表  NO024 
            stb.append(",CHAIR_A AS(");
            stb.append(     "SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
            }
            stb.append("       K2.SUBCLASSCD AS SUBCLASSCD ");
            stb.append(     "FROM   CHAIR_CLS_DAT K1,CHAIR_DAT K2 ");
            stb.append(     "WHERE  K1.YEAR = '" + _param._year + "' AND ");
            stb.append(            "K2.YEAR = '" + _param._year + "' AND ");
            if (!_param._semester.equals("9") ) {
                stb.append(        "K1.SEMESTER = '" + _param._semester + "' AND ");
                stb.append(        "K2.SEMESTER = '" + _param._semester + "' AND ");
            }
            stb.append(            "K1.YEAR = K2.YEAR AND ");
            stb.append(            "K1.SEMESTER = K2.SEMESTER AND ");
            stb.append(            "(K1.CHAIRCD = '0000000' OR K1.CHAIRCD = K2.CHAIRCD) AND ");
            stb.append(            "K1.TRGTGRADE||K1.TRGTCLASS = ? AND ");
            stb.append(            "K1.GROUPCD = K2.GROUPCD AND ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(            "(K2.CLASSCD <= '" + KNJDefineCode.subject_U + "' OR ");
                stb.append(             "K2.CLASSCD = '" + KNJDefineCode.subject_T + "') ");
            } else {
                stb.append(            "(SUBSTR(SUBCLASSCD,1,2) <= '" + KNJDefineCode.subject_U + "' OR ");
                stb.append(             "SUBSTR(SUBCLASSCD,1,2) = '" + KNJDefineCode.subject_T + "') ");
            }
            stb.append(     "GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
            }
            stb.append(       " K2.SUBCLASSCD ");
            stb.append(     ")");
            //評価読替前科目の表 NO024 
            stb.append(",REPLACE_REC_MOTO AS(");
            stb.append(        "SELECT  ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("          ATTEND_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(        "FROM    SUBCLASS_REPLACE_DAT T1 ");
            stb.append(        "WHERE   YEAR ='" + _param._year + "' AND ANNUAL = '" + _param._grade + "' AND REPLACECD = '1' ");
            stb.append(            "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE T2.SUBCLASSCD = ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("              T1.ATTEND_SUBCLASSCD) ");  //NO0017
            stb.append(        "GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("                 ATTEND_SUBCLASSCD ");
            stb.append(     ") ");
            //評価読替後科目の表 NO024 
            stb.append(",REPLACE_REC_SAKI AS(");
            stb.append(        "SELECT  ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.GRADING_CLASSCD || '-' || T1.GRADING_SCHOOL_KIND || '-' || T1.GRADING_CURRICULUM_CD || '-' || ");
            }
            stb.append("          GRADING_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(        "FROM    SUBCLASS_REPLACE_DAT T1 ");
            stb.append(        "WHERE   YEAR ='" + _param._year + "' AND ANNUAL = '" + _param._grade + "' AND REPLACECD = '1' ");
            stb.append(            "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE T2.SUBCLASSCD = ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("                          T1.ATTEND_SUBCLASSCD) ");  //NO0017
            stb.append(        "GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.GRADING_CLASSCD || '-' || T1.GRADING_SCHOOL_KIND || '-' || T1.GRADING_CURRICULUM_CD || '-' || ");
            }
            stb.append("          GRADING_SUBCLASSCD ");
            stb.append(     ") ");

            //成績データの表（通常科目）  読替科目は含めない
            stb.append(",RECORD_REC AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    W3.CLASSCD, ");
                stb.append("    W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD, ");
            if (! _param._11.equals("0") ) {
                //中間・期末成績
                stb.append( fieldname + " AS SCORE ");
                stb.append("FROM    RECORD_DAT W3 ");
                stb.append("WHERE   W3.YEAR = '" + _param._year + "' AND ");
                stb.append(        "EXISTS(SELECT  'X' FROM SCHNO_B W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
                stb.append(    "AND NOT EXISTS(SELECT 'X' FROM REPLACE_REC_SAKI W2 WHERE ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("    W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
                }
                stb.append("                                           W3.SUBCLASSCD = W2.SUBCLASSCD) ");
                //NO024 stb.append(        "CASE WHEN " + fieldname + " IS NULL AND " + fieldname + "_DI IN('KK','KS') THEN " + fieldname + "_DI ");
                //NO024 stb.append(             "WHEN " + fieldname + " IS NOT NULL THEN RTRIM(CHAR(" + fieldname + ")) ");
                //NO024 stb.append(             "ELSE NULL END AS SCORE ");
            } else if (Integer.parseInt( _param._semester ) != 9 ) {
                //学期成績
                stb.append( fieldname + " AS SCORE ");
                stb.append("FROM    RECORD_DAT W3 ");
                stb.append("WHERE   W3.YEAR = '" + _param._year + "' AND ");
                stb.append(        "EXISTS(SELECT  'X' FROM SCHNO_B W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
                stb.append(    "AND NOT EXISTS(SELECT 'X' FROM REPLACE_REC_SAKI W2 WHERE ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("    W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
                }
                stb.append("                                           W3.SUBCLASSCD = W2.SUBCLASSCD) ");
                //NO024 stb.append(       "RTRIM(CHAR(" + fieldname + ")) AS SCORE ");
            } else {
                //学年成績
                stb.append( fieldname + " AS SCORE ");
                stb.append("FROM    RECORD_DAT W3 ");
                stb.append("WHERE   W3.YEAR = '" + _param._year + "' AND ");
                stb.append(        "EXISTS(SELECT  'X' FROM SCHNO_B W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
                stb.append(    "AND NOT EXISTS(SELECT 'X' FROM REPLACE_REC_MOTO W2 WHERE ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("    W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
                }
                stb.append("                                           W3.SUBCLASSCD = W2.SUBCLASSCD) ");
                //NO024 stb.append(       "CASE WHEN SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS SCORE, ");
                //NO024 stb.append(       "RTRIM(CHAR(GRAD_VALUE)) AS PATTERN_ASSESS ");
            }
            /* NO024
            stb.append(    "FROM    RECORD_DAT W3 ");
            stb.append(    "WHERE   W3.YEAR = '" + _param._0 + "' AND ");
            stb.append(            "EXISTS(SELECT  'X' FROM SCHNO_B W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
            stb.append(        "AND NOT EXISTS(SELECT 'X' ");
            stb.append(                       "FROM   SUBCLASS_REPLACE_DAT W2 ");
            stb.append(                       "WHERE  W3.SUBCLASSCD = W2.GRADING_SUBCLASSCD AND ");
            stb.append(                              "W2.YEAR ='" + _param._0 + "' AND ");
            stb.append(                              "W2.ANNUAL = '" + _param._2 + "' AND REPLACECD = '1') ");  //05/05/22
            */
            stb.append(     ") ");

            //メイン表
            stb.append("SELECT T1.SCHREGNO,");
            stb.append(       "T5.SUM_REC, T5.AVG_REC, T5.RANK ");

            stb.append("FROM   SCHNO_B T1 ");

            //平均点、席次の表  04/11/03 T4から分離  05/02/14 結果表RECORD_RECを使用
            stb.append(     "LEFT JOIN(");
            stb.append(         "SELECT  W1.SCHREGNO,");
            stb.append(                 "SUM(INT(SCORE))AS SUM_REC, ");
            stb.append(                 "AVG(FLOAT(INT(SCORE))) AS AVG_REC,");                                 // 04/11/04Modify 04/11/08Modify
            stb.append(                 "CASE WHEN AVG(FLOAT(INT(SCORE))) IS NULL THEN NULL ELSE ");           // 04/12/06Modify
            stb.append(                 "RANK() OVER(ORDER BY CASE ");
            stb.append(                                "WHEN AVG(FLOAT(INT(SCORE))) IS NULL THEN -1 ");
            stb.append(                                "ELSE DECIMAL(ROUND(AVG(FLOAT(INT(SCORE)))*10,0)/10,5,1) END DESC)END AS RANK ");   // 04/11/01Modify 04/12/06Modify
            stb.append(         "FROM    RECORD_REC W1 ");
            stb.append(         "WHERE   SCORE IS NOT NULL AND ");  //05/03/01Modify
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(                 "W1.CLASSCD <= '" + KNJDefineCode.subject_U + "' ");
            } else {
                stb.append(                 "SUBSTR(W1.SUBCLASSCD,1,2) <= '" + KNJDefineCode.subject_U + "' ");
            }
            stb.append(         "GROUP BY W1.SCHREGNO");
            stb.append(     ")T5 ON T5.SCHREGNO = T1.SCHREGNO ");

        } catch (Exception ex) {
            log.warn("sql-statement error!", ex);
            log.debug("stb="+stb.toString());
        }
        return stb.toString();

    }//prestatStdTotalRec()の括り

    /** 
     *  PrepareStatement作成 出欠総合データ 遅刻度数 KNJD062H
     *  => ps5
     */
    private String prestatStdTotalShrLate() {
        StringBuffer stb = new StringBuffer();
        try {
            //学籍のデータ
            stb.append("WITH SCHNO AS(");
            stb.append(   "SELECT  W1.SCHREGNO ");
            stb.append(   "FROM    SCHREG_REGD_DAT W1 ");
            stb.append(   "WHERE   W1.YEAR = '" + _param._year + "' ");
            stb.append(       "AND W1.SEMESTER = '" + _param._regdSemester + "' ");
            stb.append(       "AND W1.GRADE||W1.HR_CLASS = ? ");
            stb.append(       "AND W1.ATTENDNO BETWEEN ? AND ? ");
            stb.append(") ");

            //ＳＨＲ出欠のデータ
            stb.append(",SHR_ATTEND_DATA AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.ATTENDDATE, T1.PERIODCD, L1.REP_DI_CD, L1.MULTIPLY ");
            stb.append(    "FROM    ATTEND_DAT T1 ");
            stb.append(           "LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + _param._year + "' AND L1.DI_CD = T1.DI_CD ");
            stb.append(           ",SEMESTER_MST T2 ");
            stb.append(           ",CHAIR_DAT T3 ");
            stb.append(    "WHERE   T1.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T1.ATTENDDATE BETWEEN '" + _param._semesterSdate + "' AND '" + _param._edate + "' ");
            stb.append(        "AND T2.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T3.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T1.ATTENDDATE BETWEEN T2.SDATE AND T2.EDATE ");
            stb.append(        "AND T2.SEMESTER = T3.SEMESTER ");
            stb.append(        "AND T3.CHAIRCD = T1.CHAIRCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        "AND T3.CLASSCD = '" + KNJDefineCode.subject_S + "' ");
            } else {
                stb.append(        "AND SUBSTR(T3.SUBCLASSCD,1,2) = '" + KNJDefineCode.subject_S + "' ");
            }
            stb.append(        "AND L1.REP_DI_CD IN('4','5','6','11','12','13','15','23','24') ");
            stb.append(        "AND EXISTS (SELECT SCHREGNO FROM SCHNO T3 WHERE T3.SCHREGNO = T1.SCHREGNO) ");
                                //NO025 学籍不在者および留学者・休学者を除外
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T4 ");  //NO025
            stb.append(                       "WHERE   T4.SCHREGNO = T1.SCHREGNO ");  //NO025
            stb.append(                           "AND (( ENT_DIV IN('4','5') AND ATTENDDATE < ENT_DATE ) ");  //NO025
            stb.append(                             "OR ( GRD_DIV IN('2','3') AND ATTENDDATE > GRD_DATE )) ) ");  //NO025
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");  //NO025
            stb.append(                       "WHERE   T4.SCHREGNO = T1.SCHREGNO ");  //NO025
            stb.append(                           "AND TRANSFERCD IN('1','2') AND ATTENDDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");  //NO025
            if (_param.definecode.usefromtoperiod )
                stb.append(    "AND T1.PERIODCD IN " + _param._20 + " ");
            stb.append(") ");

            //時間割のデータ
            stb.append(",SCHEDULE_SCHREG AS(");
            stb.append(    "SELECT  T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
            stb.append(    "FROM    SCH_CHR_DAT T1 ");
            stb.append(           ",CHAIR_STD_DAT T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T1.EXECUTEDATE BETWEEN '" + _param._semesterSdate + "' AND '" + _param._edate + "' ");
            stb.append(        "AND T2.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T1.CHAIRCD = T2.CHAIRCD ");
            if (_param.definecode.usefromtoperiod )
                stb.append(    "AND T1.PERIODCD IN " + _param._20 + " ");
            stb.append(        "AND EXISTS (SELECT 'X' FROM SHR_ATTEND_DATA T3 ");
            stb.append(                    "WHERE T3.SCHREGNO = T2.SCHREGNO AND T3.ATTENDDATE = T1.EXECUTEDATE) ");
                                //NO025 学籍不在者および留学者・休学者を除外
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T3 ");  //NO025
            stb.append(                       "WHERE   T3.SCHREGNO = T2.SCHREGNO ");  //NO025
            stb.append(                           "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");  //NO025
            stb.append(                             "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE )) ) ");  //NO025
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + _param._year + "' AND L1.DI_CD = T4.DI_CD ");
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
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + _param._year + "' AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");  //NO025
            stb.append(                       "WHERE   T4.SCHREGNO = T2.SCHREGNO ");  //NO025
            stb.append(                           "AND T4.TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE ) ");  //NO025
            stb.append(    "GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
            stb.append(") ");

            //出欠のデータ
            stb.append(",T_ATTEND_DAT AS(");
            stb.append(    "SELECT  T0.SCHREGNO, T0.ATTENDDATE, T0.PERIODCD, L1.REP_DI_CD ");
            stb.append(    "FROM    ATTEND_DAT T0 ");
            stb.append(           " INNER JOIN SCHEDULE_SCHREG T1 ON T0.SCHREGNO = T1.SCHREGNO ");
            stb.append(               "AND T0.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append(               "AND T0.PERIODCD = T1.PERIODCD ");
            stb.append("            LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + _param._year + "' AND L1.DI_CD = T0.DI_CD ");
            stb.append(    "WHERE   T0.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T0.ATTENDDATE BETWEEN '" + _param._semesterSdate + "' AND '" + _param._edate + "' ");
            stb.append(") ");

            //対象生徒の出欠データ（忌引・出停した日）NO025Build
            stb.append(", T_ATTEND_DAT_B AS(");
            stb.append(     "SELECT  T0.SCHREGNO, T0.ATTENDDATE ");
            stb.append(     "FROM    T_ATTEND_DAT T0 ");
            stb.append(     "WHERE   T0.REP_DI_CD IN('2','3','9','10' ");
            if ("true".equals(_param._useVirus)) {
                stb.append(     " , '19','20' ");
            }
            if ("true".equals(_param._useKoudome)) {
                stb.append(     " , '25','26' ");
            }
            stb.append(     ") GROUP BY T0.SCHREGNO, T0.ATTENDDATE ");
            stb.append(     ") ");

            //最小校時・最大校時のデータ
            stb.append(",T_PERIOD_CNT AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.EXECUTEDATE ");
            stb.append(           ",MIN(T1.PERIODCD) AS FIRST_PERIOD ");
            stb.append(           ",MAX(T1.PERIODCD) AS LAST_PERIOD ");
            stb.append(           ",COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ");
            stb.append(    "FROM    SCHEDULE_SCHREG T1 ");
            stb.append(    "GROUP BY T1.SCHREGNO, T1.EXECUTEDATE ");
            stb.append(") ");

            //出停・忌引のデータ
            stb.append(",SUSPEND_MOURNING_DATA AS(");
            stb.append(    "SELECT SCHREGNO, ATTENDDATE ");
            stb.append(    "FROM   T_ATTEND_DAT T1 ");
            stb.append(    "WHERE  T1.REP_DI_CD IN ('2','9','3','10' ");
            if ("true".equals(_param._useVirus)) {
                stb.append(     " , '19','20' ");
            }
            if ("true".equals(_param._useKoudome)) {
                stb.append(     " , '25','26' ");
            }
            stb.append(    ") ");
            stb.append(") ");
            
            //欠席のデータ
            stb.append(",ABSENT_DATA AS(");
            stb.append(    "SELECT  W0.SCHREGNO, W0.ATTENDDATE ");
            stb.append(    "FROM    ATTEND_DAT W0 ");
            stb.append(           "INNER JOIN (SELECT  T0.SCHREGNO, T0.EXECUTEDATE, T0.FIRST_PERIOD ");
            stb.append(            "FROM    T_PERIOD_CNT T0 ");
            stb.append(                  " INNER JOIN (SELECT  W1.SCHREGNO, W1.ATTENDDATE ");
            stb.append(                           ",MIN(W1.PERIODCD) AS FIRST_PERIOD ");
            stb.append(                           ",COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(                     "FROM   T_ATTEND_DAT W1 ");
            stb.append(                     "WHERE  W1.REP_DI_CD IN ('4','5','6','11','12','13') ");
            stb.append(                     "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
            stb.append(                   ") T1 ON T0.SCHREGNO = T1.SCHREGNO ");
            stb.append(                "AND T0.EXECUTEDATE = T1.ATTENDDATE ");
            stb.append(                "AND T0.FIRST_PERIOD = T1.FIRST_PERIOD ");
            stb.append(                "AND T0.PERIOD_CNT = T1.PERIOD_CNT ");
            stb.append(           ")W1 ON W0.SCHREGNO = W1.SCHREGNO ");
            stb.append(        "AND W0.ATTENDDATE = W1.EXECUTEDATE ");
            stb.append(        "AND W0.PERIODCD = W1.FIRST_PERIOD ");
            stb.append(") ");

            //早退のデータ
            stb.append(",EARLY_DATA1 AS(");
            stb.append(    "SELECT  T0.SCHREGNO, T1.ATTENDDATE ");
            stb.append(    "FROM    T_PERIOD_CNT T0 ");
            stb.append(    "INNER JOIN(");
            stb.append(         "SELECT  W1.SCHREGNO, W1.ATTENDDATE, COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(         "FROM    T_ATTEND_DAT W1 ");
            stb.append(         "WHERE   W1.REP_DI_CD NOT IN ('0','14','15','16','23','24') ");
            stb.append(             "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");  //NO025
            stb.append(                            "WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.ATTENDDATE = W1.ATTENDDATE) ");  //NO025
            stb.append(         "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
            stb.append(    ")T1 ON T0.SCHREGNO = T1.SCHREGNO AND T0.EXECUTEDATE = T1.ATTENDDATE AND T0.PERIOD_CNT != T1.PERIOD_CNT ");
            stb.append(    "INNER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT ");
            stb.append(         "WHERE   REP_DI_CD IN ('4','5','6') ");
            stb.append(    ")T3 ON T0.SCHREGNO= T3.SCHREGNO AND T0.EXECUTEDATE = T3.ATTENDDATE AND T0.LAST_PERIOD = T3.PERIODCD ");
            stb.append(") ");

            //早退のデータ
            stb.append(",EARLY_DATA2 AS(");
            stb.append(    "SELECT  T0.SCHREGNO, T2.ATTENDDATE ");
            stb.append(    "FROM    T_PERIOD_CNT T0 ");
            stb.append(    "INNER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT W1 ");  //NO025
            stb.append(         "WHERE   REP_DI_CD IN ('16') ");
            stb.append(             "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");  //NO025
            stb.append(                            "WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.ATTENDDATE = W1.ATTENDDATE) ");  //NO025
            stb.append(    ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND T0.LAST_PERIOD = T2.PERIODCD ");
            stb.append(") ");

            //メイン表
            stb.append("SELECT  T1.SCHREGNO, SUM(SMALLINT(VALUE(T1.MULTIPLY, '1'))) AS COUNT ");
            stb.append("FROM    SHR_ATTEND_DATA T1 ");
            stb.append("WHERE   NOT EXISTS(SELECT 'X' FROM SUSPEND_MOURNING_DATA T2 WHERE T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.ATTENDDATE) ");
            stb.append(    "AND NOT EXISTS(SELECT 'X' FROM ABSENT_DATA T2 WHERE T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.ATTENDDATE) ");
            stb.append(    "AND NOT EXISTS(SELECT 'X' FROM EARLY_DATA1 T2 WHERE T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.ATTENDDATE) ");
            stb.append(    "AND NOT EXISTS(SELECT 'X' FROM EARLY_DATA2 T2 WHERE T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.ATTENDDATE) ");
            stb.append("GROUP BY T1.SCHREGNO ");
            stb.append("ORDER BY SCHREGNO ");

        } catch (Exception ex) {
            log.warn("prestatStdTotalShrLate error!", ex);
        }
        return stb.toString();
    }


    /**
     *  SVF-FORM フィールドを初期化
     *    2005/05/22
     */
    private int clearSvfField(String[] lschname) {
        try {
            svf.VrSetForm("KNJD062_2.frm", 4);
            hobj.set_head2();  //KNJD062H

            svf.VrsOut( "year2", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度");    //年度          
            svf.VrsOut( "ymd1",  _param._18 );
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._17) + "\uFF5E" + KNJ_EditDate.h_format_JP(db2, _param._edate));  // 欠課数の集計範囲
            svf.VrsOut("DATE2", KNJ_EditDate.h_format_JP(db2, _param._semesterSdate) + "\uFF5E" + KNJ_EditDate.h_format_JP(db2, _param._edate));  // 出欠記録の集計範囲

            printSvfStdNameOut(lschname );                            //生徒名等出力のメソッド

            //  組名称及び担任名の取得
            try {
                KNJ_Get_Info getinfo = new KNJ_Get_Info();      //各情報取得用のクラス
                KNJ_Get_Info.ReturnVal returnval = null;        //各情報を返すためのクラス
                returnval = getinfo.Hrclass_Staff( db2, _param._year, _param._semester, _param._gradeHrclass, "" );
                svf.VrsOut("HR_NAME", returnval.val1);          //組名称
                svf.VrsOut("teacher", returnval.val3);          //担任名
            } catch (Exception ex) {
                log.warn("HR_NAME... svf-out error!", ex);
            }
        } catch (Exception ex) {
            log.warn("clearSvfField error! ", ex);
        }
        return 0;
    }


    /**
     *  SVF-FORM フィールドを初期化
     *    2005/05/30
     */
    private void clearValue() {
        try {
            for (int i = 0 ; i < hr_lesson.length ; i++) hr_lesson[i] = 0;
            for (int i = 0 ; i < hr_attend.length ; i++) hr_attend[i] = 0;
            for (int i = 0 ; i < hr_seitosu.length ; i++) hr_seitosu[i] = 0;
            for (int i = 0 ; i < sch_rcredits.length ; i++) sch_rcredits[i] = 0;
            for (int i = 0 ; i < sch_ccredits.length ; i++) sch_ccredits[i] = 0;
            hr_credits = 0;
            subclasstotalcnt = 0;
            subclasstotalnum = 0;
            hrtotalnum = 0;
            hrtotalcnt = 0;
            subclasslinecount = 0;
            hr_total = 0;
        } catch (Exception ex) {
            log.warn("clearSvfField error! ", ex);
        }
    }


    /**
     *  SVF-FORM フィールドを初期化
     *    2005/06/07
     */
    private void clearValue2() {
        try {
            for (int i = 0 ; i < sch_rcredits.length ; i++) sch_rcredits[i] = 0;
            for (int i = 0 ; i < sch_ccredits.length ; i++) sch_ccredits[i] = 0;
            //subclasstotalcnt = 0;
            //subclasstotalnum = 0;
            subclasslinecount = 0;
        } catch (Exception ex) {
            log.warn("clearSvfField error! ", ex);
        }
    }

    /** 
     *  クラス履修単位を取得
     *  NO022
     */
    private void getRCreditsHr() {
        try {
            for (int i = 0; i < sch_rcredits.length; i++) {
                if (0 < sch_rcredits[i]  &&  hr_credits < sch_rcredits[i]) {
                    hr_credits = sch_rcredits[i];
                }
            }
        } catch (Exception ex) {
            log.warn("group-average svf-out error!", ex);
        }
    }


    /** 
     *  各成績別処理クラス設定
     *  NO022
     */
    private void setKnjd062HObj() {
        try {
            if (hobj == null ) {
                if (_param._11.equals("01")) {
                    hobj = new KNJD062H_INTER();
                } else if (_param._11.equals("02")) {
                    hobj = new KNJD062H_TERM();
                } else if (!_param._semester.equals("9")) { 
                    hobj = new KNJD062H_GAKKI();
                } else if (_param._semester.equals("9")) {
                    hobj = new KNJD062H_GRADE();
                }
            }
        } catch (Exception ex) {
            log.error("setKnjd062HObj error!", ex);
        }

    }

    /**
     *  遅刻度数出力処理
     */
    private boolean printSvfStdTotalShrLate() {
        boolean nonedata = false;
        ResultSet rs = null;
        int p = 0;
        try {
            p = 0;
            _param._psShrLate.setString( ++p, _param._gradeHrclass );        //学年・組
            _param._psShrLate.setString( ++p, _param._attendnoFrom );        //出席番号FROM
            _param._psShrLate.setString( ++p, _param._attendnoTo  );      //出席番号TO
            rs = _param._psShrLate.executeQuery();               //出欠累計データのRecordSet
            while (rs.next()) {
            	//明細データの出力
            	try {
				    //学籍番号（生徒）に対応した行にデータをセットする。
				    Integer int1 = _param.hm1.get(rs.getString("SCHREGNO"));
				    if (int1 != null) {
				        final String count = rs.getString("COUNT");
				        if (count != null) {
				            if (Integer.parseInt(count) != 0) {
				            	svf.VrsOut("LATE_FREQUENCY" + int1.intValue(), count);  //遅刻度数
				            }
				            hr_attend[9] += Integer.parseInt( count );  //クラス合計
				            hr_seitosu[9]++;  //生徒数
				        }
				    }
				
				} catch (Exception ex) {
				    log.warn("total svf-out error!", ex);
				}
            }
        } catch (Exception ex) {
            log.warn("ResultSet-read error!",ex);
        } finally{
        	db2.commit();
        	DbUtils.closeQuietly(rs);
        }
        return nonedata;
    }
    
    /**
     * 中間試験成績の処理クラス
     */
    abstract private class KNJD062H_COMMON {
        abstract void set_head2();
        abstract void printSvfStdDetailOut(ResultSet rs, int assesspattern, int replaceflg);
    }


    /**
     * 中間試験成績の処理クラス
     */
    private class KNJD062H_INTER extends KNJD062H_COMMON {
        /**
         *  ページ見出し
         */
        void set_head2() {
            svf.VrsOut("TITLE" , _param._semesterName + "  " + _param._22 + " 成績一覧表");    //タイトル
            svf.VrsOut("MARK"  , "/"    );           //表下部の文言の一部に使用
            for( int i = 0 ; i < 19 ; i++ ) {
                svf.VrsOutn("ITEM1",  i + 1,   "素点" );
                svf.VrsOutn("ITEM2",  i + 1,   "評価" );
                svf.VrsOutn("ITEM3",  i + 1,   "欠課" );
            }
            //一覧表枠外の文言
            svf.VrAttribute("NOTE1",  "Paint=(1,70,1),Bold=1");
            svf.VrsOut("NOTE1",  " " );
            svf.VrsOut("NOTE2",  "：欠課時数超過者" );

            fieldname  = "SEM" + _param._semester + "_INTR_SCORE";
            fieldname2 = "SEM" + _param._semester + "_INTR";
            if (_param._regdSemester == null )_param._regdSemester = _param._semester;
        }


        /** 
         *  明細出力 
         *  生徒の科目別成績、評定、欠課を出力する
         */
        void printSvfStdDetailOut(ResultSet rs, int assesspattern, int replaceflg) {
            try {
                //学籍番号（生徒）に対応した行にデータをセットする。
                Integer int1 = (Integer) _param.hm1.get(rs.getString("SCHREGNO"));
                if (int1==null )return;
                int linex = subclasslinecount + 1;

                //成績
                String str = null;
                if ( rs.getString("ABSENT_SCH") != null )str = "/";   //欠席
                final String score = rs.getString("SCORE");
                if (str != null  &&  score != null )
                    svf.VrsOutn( "rate" + int1.intValue(), linex,  str + score );
                else if (str != null )
                    svf.VrsOutn( "rate" + int1.intValue(), linex,  str );
                else if (score != null )
                    svf.VrsOutn( "rate" + int1.intValue(), linex,  score );

                if ( score != null ) subclasscnt_num( score, 0 );  //平均・合計

                //評価
                str = null;
                final String patternAssess = rs.getString("PATTERN_ASSESS");
                if (_param._8.equals("1")  &&  patternAssess != null  &&  patternAssess.equals("1") )str = "/";   //評定１
                if (str != null  &&  patternAssess != null )
                    svf.VrsOutn("late" + int1.intValue(), linex,  str + patternAssess );
                else if ( str != null )
                    svf.VrsOutn("late" + int1.intValue(), linex,  str );
                else if ( patternAssess != null )
                    svf.VrsOutn("late" + int1.intValue(), linex,  patternAssess );

                //欠課時数
                printSvfStdDetailOutAbsence( rs, int1.intValue(), linex );

            } catch (Exception ex) {
                log.error("printSvfStdDetailOut error!", ex);
            }
        }
    }
    /**
     * 期末試験成績の処理クラス
     */
    private class KNJD062H_TERM extends KNJD062H_COMMON {
        /**
         *  ページ見出し
         */
        void set_head2()
        {
            svf.VrsOut("TITLE" , _param._semesterName + "  " + _param._22 + " 成績一覧表");    //タイトル
            svf.VrsOut("MARK"  , "/"                     );           //表下部の文言の一部に使用
            for( int i = 0 ; i < 19 ; i++ ) {
                svf.VrsOutn("ITEM1",  i + 1,   "素点" );
                svf.VrsOutn("ITEM2",  i + 1,   "評価" );
                svf.VrsOutn("ITEM3",  i + 1,   "欠課" );
            }
            //一覧表枠外の文言
            svf.VrAttribute("NOTE1",  "Paint=(1,70,1),Bold=1");
            svf.VrsOut("NOTE1",  " " );
            svf.VrsOut("NOTE2",  "：欠課時数超過者" );

            fieldname  = "SEM" + _param._semester + "_TERM_SCORE";
            fieldname2 = "SEM" + _param._semester + "_TERM";
            if (_param._regdSemester == null )_param._regdSemester = _param._semester;
        }


        /** 
         *  明細出力 
         *  生徒の科目別成績、評定、欠課を出力する
         */
        void printSvfStdDetailOut(final ResultSet rs, final int assesspattern, final int replaceflg) {
            try {
                //学籍番号（生徒）に対応した行にデータをセットする。
                Integer int1 = (Integer) _param.hm1.get(rs.getString("SCHREGNO"));
                if (int1==null )return;
                int linex = subclasslinecount + 1;

                //成績
                String str = null;
                if (rs.getString("ABSENT_SCH") != null )str = "/";   //欠席
                final String score = rs.getString("SCORE");
                if (str != null  &&  score != null )
                    svf.VrsOutn( "rate" + int1.intValue(), linex,  str + score );
                else if (str != null )
                    svf.VrsOutn( "rate" + int1.intValue(), linex,  str );
                else if (score != null )
                    svf.VrsOutn( "rate" + int1.intValue(), linex,  score );

                if (score != null ) subclasscnt_num( score, 0 );  //平均・合計

                //評価
                str = null;
                final String patternAssess = rs.getString("PATTERN_ASSESS");
                if (_param._8.equals("1")  &&  patternAssess != null  &&  patternAssess.equals("1") )str = "/";   //評定１
                if (str != null  &&  patternAssess != null )
                    svf.VrsOutn( "late" + int1.intValue(), linex,  str + patternAssess );
                else if (str != null )
                    svf.VrsOutn( "late" + int1.intValue(), linex,  str );
                else if (patternAssess != null )
                    svf.VrsOutn( "late" + int1.intValue(), linex,  patternAssess );

                //欠課時数
                printSvfStdDetailOutAbsence( rs, int1.intValue(), linex );

            } catch (Exception ex) {
                log.error("printSvfStdDetailOut error!", ex);
            }
        }
    }
    /**
     * 学期成績の処理クラス
     */
    private class KNJD062H_GAKKI extends KNJD062H_COMMON {
        /**
         *  ページ見出し
         */
        void set_head2()
        {
            svf.VrsOut("TITLE" , _param._semesterName + " 成績一覧表");     //タイトル
            svf.VrsOut("MARK"  , "/"                     );     //表下部の文言の一部に使用
            for( int i = 0 ; i < 19 ; i++ ) {
                svf.VrsOutn("ITEM1",  i + 1,   "成績" );
                svf.VrsOutn("ITEM2",  i + 1,   "評価" );
                svf.VrsOutn("ITEM3",  i + 1,   "欠課" );
            }
            //一覧表枠外の文言
            svf.VrAttribute("NOTE1",  "Paint=(1,70,1),Bold=1");
            svf.VrsOut("NOTE1",  " " );
            svf.VrsOut("NOTE2",  "：欠課時数超過者" );

            fieldname = "SEM" + _param._semester + "_VALUE";
            if (_param._regdSemester == null )_param._regdSemester = _param._semester;
        }


        /** 
         *  明細出力 
         *  生徒の科目別成績、評定、欠課を出力する
         */
        void printSvfStdDetailOut(final ResultSet rs, final int assesspattern, final int replaceflg) {
            try {
                //学籍番号（生徒）に対応した行にデータをセットする。
                Integer int1 = (Integer) _param.hm1.get(rs.getString("SCHREGNO"));
                if (int1==null )return;
                int linex = subclasslinecount + 1;

                //評価
                String str = null;
                final String patternAssess = rs.getString("PATTERN_ASSESS");
                if (rs.getString("ABSENT_SCH") != null )str = "/";   //欠席
                else if (_param._8.equals("1")  &&  patternAssess != null  &&  patternAssess.equals("1") )str = "*";

                if (str != null  &&  patternAssess != null )
                    svf.VrsOutn( "late" + int1.intValue(), linex,  str + patternAssess );
                else if (str != null )
                    svf.VrsOutn( "late" + int1.intValue(), linex,  str );
                else if (patternAssess != null )
                    svf.VrsOutn( "late" + int1.intValue(), linex,  patternAssess );

                if (patternAssess != null )subclasscnt_num( patternAssess, 0 );  //平均・合計

                //欠課時数
                printSvfStdDetailOutAbsence( rs, int1.intValue(), linex );

            } catch (Exception ex) {
                log.warn("attend... svf-out error!", ex);
            }
        }
    }
    /**
     * 学年成績の処理クラス
     */
    private class KNJD062H_GRADE extends KNJD062H_COMMON {
        /**
         *  ページ見出し
         */
        void set_head2()
        {
            if (_param._regdSemester == null )_param._regdSemester = _param._semester;

            svf.VrsOut("TITLE",   _param._semesterName + " 成績一覧表（評定）");
            svf.VrsOut("MARK",   "/"                     );              //表下部の文言の一部に使用
            for( int i = 0 ; i < 19 ; i++ ) {
                svf.VrsOutn("ITEM1",  i + 1,   "成績" );
                svf.VrsOutn("ITEM2",  i + 1,   "評定" );
                svf.VrsOutn("ITEM3",  i + 1,   "欠課" );
            }
            //一覧表枠外の文言
            svf.VrAttribute("NOTE1",  "Paint=(1,70,1),Bold=1");
            svf.VrsOut("NOTE1",  " " );
            svf.VrsOut("NOTE2",  "：欠課時数超過者" );

            fieldname = "GRAD_VALUE";
        }


        /** 
         *  明細出力 
         *  生徒の科目別成績、評定、欠課を出力する
         */
        void printSvfStdDetailOut(final ResultSet rs, final int assesspattern, final int replaceflg) {
            try {
                boolean amikake = false;
                //学籍番号（生徒）に対応した行にデータをセットする。
                Integer int1 = (Integer) _param.hm1.get(rs.getString("SCHREGNO"));
                if (int1==null )return;
                int linex = subclasslinecount + 1;

                //欠課の出力および網掛け設定
                if (rs.getString("ABSENT1") != null )
                    amikake = printSvfStdDetailOutAbsence( rs, int1.intValue(), linex );

                //履修単位の処理
                final String compUnconditionFlg = rs.getString("COMP_UNCONDITION_FLG");
                final String compCredit = rs.getString("COMP_CREDIT");
                if (null != compCredit) {
                    if (rs.getString("ON_RECORD_COMP") == null  &&  compUnconditionFlg.equals("1") ) {
                        if (! amikake)
                        if ( replaceflg < 1) sch_rcredits[ int1.intValue() ] += Integer.parseInt( compCredit );  //履修単位の加算処理  NO007Modify  NO010Modify
                    } else {
                        if (replaceflg < 1) sch_rcredits[ int1.intValue() ] += Integer.parseInt( compCredit );  //履修単位の加算処理  NO007Modify  NO010Modify
                    }
                }

                //修得単位の処理
                final String getCredit = rs.getString("GET_CREDIT");
                if (null != getCredit) {
                    if (rs.getString("ON_RECORD_GET") == null  &&  compUnconditionFlg.equals("1") ) {
                        if (! amikake) 
                        if (replaceflg < 1) sch_ccredits[ int1.intValue() ] += Integer.parseInt( getCredit  );  //修得単位の加算処理  NO007Modify  NO010Modify
                    } else {
                        if (replaceflg < 1) sch_ccredits[ int1.intValue() ] += Integer.parseInt( getCredit  );  //修得単位の加算処理  NO007Modify  NO010Modify
                    }
                }

                //評定
                String str = null;
                final String patternAssess = rs.getString("PATTERN_ASSESS");
                if (rs.getString("ABSENT_SCH") != null )str = "/";   //欠席
                else if (_param._8.equals("1")  &&  patternAssess != null  &&  patternAssess.equals("1")  &&  rs.getInt("REPLACEMOTO") <= 0 )str = "*";

                if (rs.getInt("REPLACEMOTO") <= 0 ) {
                    if (str != null  &&  patternAssess != null )
                        svf.VrsOutn( "late" + int1.intValue(), linex,  str + patternAssess );
                    else if (str != null )
                        svf.VrsOutn( "late" + int1.intValue(), linex,  str );
                    else if (patternAssess != null )
                        svf.VrsOutn( "late" + int1.intValue(), linex,  patternAssess );
                }

                if (patternAssess != null )subclasscnt_num( patternAssess, rs.getInt("REPLACEMOTO") );  //平均・合計

            } catch (Exception ex) {
                log.warn("attend... svf-out error!", ex);
            }
        }
    }
    
    private static class Param {
        final String _year;
        final String _semester;
        final String _grade;
        String _gradeHrclass;
        final String _edate;
        String _semesterSdate;
        String _semesterEdate;
        final String _8;
        String _attendnoFrom;
        String _attendnoTo;
        final String _11;
        String _semesterName;
        String _regdSemester;
        String _14;
        String _divideAttendDate;
        String _divideAttendMonth;
        String _17;
        String _18;
        final String _19;
        String _20;
        final String _21;
        final String _22;
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        
        PreparedStatement _psStudent;
        PreparedStatement _psTotalRec;
        PreparedStatement _psSubclassInfo;
        PreparedStatement _psSubclassDetail;
        PreparedStatement _psAttendSemesSql;
        PreparedStatement _psShrLate;
        
        private KNJDefineSchool definecode;       //各学校における定数等設定 05/05/22
        private KNJSchoolMst _knjSchoolMst;

        /** 出欠状況取得引数 */
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;
        private String _sDate;
        private final String SSEMESTER = "1";
		final Map attendParamMap = new HashMap();

		Map<String, Integer> hm1;
        Map hm2;
        Map hm3;
        Map hm4;

        Param(final DB2UDB db2, final String[] param) {
            _year = param[0];
            _semester = param[1];
            _grade = param[2];
            _gradeHrclass = param[3];
            _edate = param[4];
            _semesterSdate = param[6];
            _semesterEdate = param[7];
            _8 = param[8];
            _attendnoFrom = param[9];
            _attendnoTo = param[10];
            _11 = param[11];
            _semesterName = param[12];
            _regdSemester = param[13];
            _14 = param[14];
            _divideAttendDate = param[15];
            _divideAttendMonth = param[16];
            _17 = param[17];
            _18 = param[18];
            _19 = param[19];
            _20 = param[20];
            _21 = param[21];
            _22 = param[22];
            _useCurriculumcd = param[23];
            _useVirus = param[24];
            _useKoudome = param[25];
            
            loadAttendSemesArgument(db2);
    		attendParamMap.put("DB2UDB", db2);
        }
        
        /**
         *  クラス内で使用する定数設定
         *    2005/05/22
         */
        private void setClasscode(final DB2UDB db2) {
            try {
            	definecode = new KNJDefineSchool();
            	definecode.defineCode( db2, _year );         //各学校における定数等設定
    log.debug("semesdiv="+definecode.semesdiv + "   absent_cov="+definecode.absent_cov + "   absent_cov_late="+definecode.absent_cov_late);
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!",ex);
            }
        }

        /**
         *  ページ見出し・初期設定
         */
        private void set_head(final DB2UDB db2) {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();      //各情報取得用のクラス
            KNJ_Get_Info.ReturnVal returnval = null;        //各情報を返すためのクラス
            if (definecode == null) setClasscode(db2);         //教科コード・学校区分・学期制等定数取得 05/05/22
            try {
            	final Map paramMap = new HashMap();
            	paramMap.put("SCHOOL_KIND", KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ")));
                _knjSchoolMst = new KNJSchoolMst(db2, _year, paramMap);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

        //  学期名称、範囲の取得
            try {
                returnval = getinfo.Semester(db2, _year, _semester);
                _semesterName = returnval.val1;                                 //学期名称
                _semesterSdate = returnval.val2;                                  //学期期間FROM
                _semesterEdate = returnval.val3;                                  //学期期間TO
            } catch (Exception ex) {
                log.warn("term1 svf-out error!",ex);
            } finally {
                if (_semesterSdate == null ) _semesterSdate = _year + "-04-01";
                if (_semesterEdate == null ) _semesterEdate = ( Integer.parseInt(_year) + 1 ) + "-03-31";
            }

            returnval = getinfo.Semester(db2, _year, "9");
            _17 = returnval.val2;  // 年度期間FROM

                //  出欠データ集計用開始日取得 => 2004年度の１学期は累積データを使用する => 出欠データ集計は2004年度2学期以降
            try {
                if (!_year.equals("2004")) {
                    _14 = _semesterSdate;               //出欠集計用
                } else {
                    returnval = getinfo.Semester(db2,_year, "2" );
                    _14 = returnval.val2;                                 //学期期間FROM
                }
            } catch (Exception ex) {
                log.warn("term1 svf-out error!",ex);
            } finally {
                if (_14 == null ) _14 = _year + "-04-01";
            }

        //  作成日(現在処理日)・出欠集計範囲の出力 05/05/22Modify
            try {
                //システム時刻を表記 05/05/22
                StringBuffer stb = new StringBuffer();
                Date date = new Date();
                SimpleDateFormat sdf = null;    //05/05/22
                sdf = new SimpleDateFormat("yyyy");
                stb.append( KNJ_EditDate.gengou(db2, Integer.parseInt( sdf.format(date) )) );
                sdf = new SimpleDateFormat("年M月d日H時m分");
                stb.append( sdf.format(date) );
                _18 = stb.toString();
            } catch (Exception ex) {
                log.warn("ymd1 svf-out error!",ex);
            }

          //対象校時および名称取得  05/06/15
            setPeiodValue(db2);

    //for( int i = 0 ; i < param.length ; i++ )log.debug("param[" + i + "] = " + param[i]);
        }//set_headの括り

        /** 
         *  パラメータセット 2005/01/29
         *      _param._15:attend_semes_datの最終集計日の翌日をセット
         *      _param._16:attend_semes_datの最終集計学期＋月をセット
         *  2005/02/20 Modify getDivideAttendDateクラスより取得
         */
        private void getParam2(final DB2UDB db2) {
            KNJDivideAttendDate obj = new KNJDivideAttendDate();
            try {
                obj.getDivideAttendDate( db2, _year, _semester, _edate );
                _divideAttendDate = obj.date;
                _divideAttendMonth = obj.month;
            } catch (Exception ex) {
                log.error("error! ", ex);
            }
    log.debug("_divideAttendDate="+_divideAttendDate);
    log.debug("_divideAttendMonth="+_divideAttendMonth);
        }

        private void loadAttendSemesArgument(DB2UDB db2) {
            try {
                loadSemester(db2, _year);
                // 出欠の情報
                final KNJDefineSchool definecode0 = setClasscode0(db2);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap,_sDate, _edate); // _sDate: 年度開始日, _date: LOGIN_DATE
                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
                log.debug(" hasuuMap = " + _hasuuMap);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
        }
        
        private KNJDefineSchool setClasscode0(final DB2UDB db2) {
            KNJDefineSchool definecode = null;
            try {
                definecode = new KNJDefineSchool();
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }
        
        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
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
        
        /** 
         *  対象校時取得
         *    2005/06/15 Build
         */
        private void setPeiodValue(final DB2UDB db2) {
            StringBuffer stb2 = null;         //05/04/16
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer stb = new StringBuffer();
                stb.append(    "SELECT  NAMECD2 ");
                if (definecode.usefromtoperiod) {
                    stb.append("FROM    NAME_MST W1, COURSE_MST W2 ");
                    stb.append("WHERE   NAMECD1 = 'B001' ");
                    stb.append(        "AND S_PERIODCD <= NAMECD2 AND NAMECD2 <= E_PERIODCD ");
                    stb.append(        "AND COURSECD IN(SELECT MIN(COURSECD) FROM SCHREG_REGD_DAT W3 ");
                    stb.append(                        "WHERE  W3.YEAR = '" + _year + "' ");
                    stb.append(                               "AND W3.SEMESTER = '" + _regdSemester + "' ");
                    stb.append(                               "AND W3.GRADE || W3.HR_CLASS = '" + _gradeHrclass + "') ");
                } else {
                    stb.append("FROM    NAME_MST W1 ");
                    stb.append("WHERE   NAMECD1 = 'B001' ");
                }
                stb.append("ORDER BY NAMECD2");

                ps = db2.prepareStatement( stb.toString() );
                rs = ps.executeQuery();
                int i = 0;
                while (rs.next()) {
                    if (i++ == 0 ) {                                                     //05/04/16
                        stb2 = new StringBuffer();
                        stb2.append("(");
                    } else {
                        stb2.append(",");
                    }
                    stb2.append( "'" ).append( rs.getString("NAMECD2") ).append( "'" );
                }
            } catch (Exception ex) {
                log.warn("periodname-get error!",ex);
            } finally{
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
                if (stb2 != null ) stb2.append(")");
                if (0 < stb2.length() ) _20 = stb2.toString();
                else                    _20 = "('1','2','3','4','5','6','7','8','9')";
            }
        }
    }
}
