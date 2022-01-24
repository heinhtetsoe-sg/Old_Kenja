// kanji=漢字
/*
 * $Id: 2bdc89e8573dbf08ee1c4e466a2099a8aef3ae0f $
 *
 * 作成日: 2005/03/30 14:27:31 - JST
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
/**
 * http://ktest/servlet_kin/KNJD?DBNAME=j050825&PRGID=KNJD171&YEAR=2005&GAKKI=2&GRADE_HR_CLASS=01J01&TESTKINDCD=01&DATE=2005/09/26&DATE2=2005/09/26&OUTPUT=1&category_selected=20051120
 *
 *  学校教育システム 賢者 [成績管理]  中学校成績通知票（ＰＤＦ）
 *
 *  2005/05/18 yamashiro 新規作成
 *  2005/05/27 nakamoto  学期成績は、公欠(KK)・欠席(KS)はみない。//---NO001
 *                       ５教科全部点数が入力されていないのに席次が印字されている。→→→"( )"を印字する。//---NO002
 *                       クラス指定出力で、複数コースをまたぐ出力指定を行った場合、通信欄の内容がコース毎に区別されず一律に出力される。//---NO003
 *  2005/06/22 yamashiro 成績が未入力の場合でも「（）」を表示する
 *                       指示画面で指定した学期および成績種別までの成績を出力する
 *  2005/07/09 yamashiro 成績は全て教科でグループ化する
 *  2005/10/11 yamashiro 編入（データ仕様の変更による）について修正、および学籍異動に転学を追加
 *                       忌引または出停の日は遅刻および早退をカウントしない
 *                       留学・休学・不在日数の算出において、ATTEND_SEMES_DATもみる。
 *                           <change specification of 05/09/28>
 *  2005/10/13 nakamoto 席次は、テーブル(RECORD_RANK_DAT)から参照するように修正---NO024
 *  2005/10/20 yamashiro 2学期-中間テストを指定して出力すると1学期期末試験成績および1学期成績が出力されない不具合を修正
 *                       学年評定が出力されない不具合を修正
 *  2005/10/21 yamashiro 2学期-中間テストを指定して出力すると1学期期末席次および1学期席次が出力されない不具合を修正
 *  2005/12/13 nakamoto 平均行は、テーブル(RECORD_CLASS_AVERAGE_DAT)から参照するように修正---NO025
 *  2005/12/15 nakamoto 席次がNULLなのに（）が表示されない不具合を修正---NO026
 *  2006/02/26 m-yama   NO027:学期成績が2つ以下の場合学年成績'-'表示。中間期末共にKK又は、KSの場合1/2学期成績()表示。
 *  2006/10/16 m-yama   NO028:NO027の1/2学期成績()表示は、遡及入力フラグ(1)の場合に変更。
 */

package servletpack.KNJD;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.dao.AttendAccumulate;


public class KNJD171K_Laser {

    private static final Log log = LogFactory.getLog(KNJD171K_Laser.class);
    //private boolean nonedata;
    private String printname;               //プリンタ名
    private PrintWriter outstrm;
    private DecimalFormat dmf1 = new DecimalFormat("0");
    private DecimalFormat dmf2 = new DecimalFormat("0.0");

    private static final int KIJUN_YEAR = 2019;


    /**
      *
      *  KNJD.classから最初に起動されるクラス
      *
      **/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        boolean nonedata = false;

        // print svf設定
        setSvfInit(response, svf);

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME") , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("db new error:", ex);
            if (db2 != null) db2.close();
        }

        // パラメータの取得
        final Param param = getParam(request, db2);

        // 印刷処理
        nonedata = printSvf(request, db2, svf, param);

        // 終了処理
        closeSvf(svf, nonedata);
        try {
            db2.commit();
            db2.close();
        } catch (Exception ex) {
            log.error("db close error!", ex);
        }//try-cathの括り

    }   //doGetの括り


    /**
     *  印刷処理
     */
    private boolean printSvf(final HttpServletRequest request, final DB2UDB db2, final Vrw32alp svf, final Param param)
    {
        boolean nonedata = false;
        String[] hrclass = null;
        try {
            if (Integer.parseInt(param._output) == 1) {
                param._schregno = setGetSchregno(request.getParameterValues("category_selected"));   //学籍番号の編集
                hrclass = request.getParameterValues("GRADE_HR_CLASS");                         //学年・組
            } else {
                hrclass = request.getParameterValues("category_selected");                      //学年・組
            }

            final String frm = Integer.parseInt(param._year) >= KIJUN_YEAR ? "KNJD171_4.frm" : "KNJD171_3.frm";
            svf.VrSetForm(frm, 1);            //SVF-FORM
            svf.VrAttribute("ATTENDNO","FF=1");           //ＳＶＦ属性変更--->改ページ（ 出席番号）
            param._nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(param._year)) + "年度";     //年度

            if (printSvfMain(db2, svf, param, hrclass)) {
                nonedata = true;      //SVF-FORM出力処理
            }
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
        return nonedata;
    }

    /**
     *  対象生徒学籍番号編集(SQL用)
     */
    private String setGetSchregno(final String[] schno) {

        final StringBuffer stb = new StringBuffer();

        for (int ia = 0; ia < schno.length; ia++) {
            if (ia == 0) stb.append("('");
            else        stb.append("','");
            stb.append(schno[ia]);
        }
        stb.append("')");

        return stb.toString();
    }


    /**
     *
     * SVF-OUT 印刷処理
     */
    private boolean printSvfMain(final DB2UDB db2, final Vrw32alp svf, final Param param, final String[] hrclass)
    {
        boolean nonedata = false;
        final PreparedStatement[] arrps = new PreparedStatement[5];
        final KNJD171K_Laser.ReturnVal returnval = new KNJD171K_Laser.ReturnVal();  //ＳＶＦ出力位置およびフィールド名を取得するメソッド
        final KNJ_Get_Info getinfo = new KNJ_Get_Info();
        final KNJ_Get_Info.ReturnVal returnvalh = null;

        try {
            arrps[0] = db2.prepareStatement(prestatementRegd(param));             //学籍データ
            arrps[1] = db2.prepareStatement(prestatementCommitAndClub(param));    //委員会・クラブ活動データ
            arrps[2] = db2.prepareStatement(prestatementSubclass(param));         //成績明細データ
            // 1日単位
            param._attendParamMap.put("schregno", "?");
            final String sqlAttend = AttendAccumulate.getAttendSemesSql(
                    param._year,
                    Param.ESEMESTER,
                    null,
                    param._date,
                    param._attendParamMap
            );
            arrps[3] = db2.prepareStatement(sqlAttend);           //出欠データ
        } catch (Exception ex) {
            log.error("[KNJD171K]boolean printSvfMain prepareStatement error! ", ex);
            return nonedata;
        }

        for (int i = 0; i < hrclass.length; i++) {
            log.debug("hrclass=" + hrclass[i]);
            try {
                if (Integer.parseInt(param._output) == 2) {
                    param._hrclass = hrclass[i];
                }
                if (printSvfMainHrclass(db2, svf, param, arrps, returnval, getinfo, returnvalh)) {
                    nonedata = true;
                }
            } catch (Exception ex) {
                log.error("[KNJD171K]boolean printSvfMain printSvfMainHrclass() error! ", ex);
            }
        }

        for (int i = 0; i < arrps.length; i++) {
            DbUtils.closeQuietly(arrps[i]);
        }

        return nonedata;

    }//boolean printSvfMain()の括り


    /**
     *
     * SVF-OUT 学級単位の印刷処理
     *    Stirng param[]について ==> param._2:学年・組 param._5:該当生徒の学籍番号
     */
    private boolean printSvfMainHrclass(final DB2UDB db2, Vrw32alp svf, final Param param
                                            ,final PreparedStatement[] arrps
                                            ,final KNJD171K_Laser.ReturnVal returnval
                                            ,final KNJ_Get_Info getinfo
                                            ,final KNJ_Get_Info.ReturnVal returnvalh
                                             )
    {
        boolean nonedata = false;
        ResultSet arrrs[] = new ResultSet[2];
        setHeadHrclass(db2, svf, param, getinfo, returnvalh);  //学級ごとの項目セット

        try {
            int pp = 0;
            arrps[0].setString(++pp,  param._hrclass);                      //学年・組
            arrrs[0] = arrps[0].executeQuery();                         //学籍データのResultSet
            pp = 0;
            arrps[2].setString(++pp,  param._hrclass.substring(0,2));       //学年
            arrps[2].setString(++pp,  param._hrclass);                      //学年・組
            arrps[2].setString(++pp,  param._hrclass);                      //学年・組
            arrps[2].setString(++pp,  param._hrclass);                      //学年・組---NO025
            arrrs[1] = arrps[2].executeQuery();                         //成績明細データResultSet
//log.debug("ps="+arrps[2].toString());
        } catch (SQLException ex) {
            log.error("[KNJD171K]boolean printSvfMainHrclass executeQuery error! ", ex);
            return nonedata;
        }

        try {
            int arrschno[] = new int[3];   //学生番号の保管 ==> { arrschno[0]学籍, arrschno[1]成績明細 }

            while (arrrs[0].next()) {
                if (arrschno[0] != 0) {
                    svf.VrEndPage();   //初回を除いて印刷！
                    nonedata = true;
                    log.debug("schno1=" + arrschno[0]);
                }

                arrschno[0] = arrrs[0].getInt("ATTENDNO");                //学籍データの出席番号を保存
                printSvfRegdOut(db2, svf, arrrs[0], param);   //学籍データ印刷

                for (; -1 < arrschno[1]  &&  arrschno[1] <= arrschno[0];) {  // ==> 成績明細の学籍番号が学籍の生徒番号以内の間繰り返す
                    if (arrschno[1] == arrschno[0]) {
                        printSvfRecDetailOut(svf, arrrs[1], param, returnval);  //成績明細データ印刷
                    }
                    if (arrrs[1].next()) {
                        arrschno[1] = arrrs[1].getInt("ATTENDNO");                           //成績明細データの出席番号を保存
                    } else {
                        arrschno[1] = -1;
                    }
                }
                printSvfAttend(svf, param, arrps[3], arrrs[0].getString("SCHREGNO"), Integer.parseInt(param._gakki));  //出欠データ印刷
                printSvfCommitAndClub(svf, arrps[1], arrrs[0].getString("SCHREGNO"));  //委員会・クラブ活動データを印刷
            }
            if (arrschno[0] != 0) {
                svf.VrEndPage();   //初回を除いて印刷！
                nonedata = true;
                log.debug("schno2=" + arrschno[0]);
            }
        } catch (Exception ex) {
            log.error("[KNJD171K]printSvfMainHrclass read error! ", ex);
        }

        for (int i = 0; i < arrrs.length; i++) {
            DbUtils.closeQuietly(arrrs[i]);
        }

        return nonedata;
    }


    /**
     *  SVF-FORMセット＆見出し項目（学級単位）
     */
    private void setHeadHrclass(final DB2UDB db2, Vrw32alp svf, final Param param,
                                     final KNJ_Get_Info getinfo,
                                     KNJ_Get_Info.ReturnVal returnval)
    {
        try {
            returnval = getinfo.Staff_name(db2, param._year, param._gakki, param._hrclass, "");
            param._staffname = returnval.val1;                                              //学級担任名
        } catch (Exception ex) {
            log.error("[KNJD171K]setHeadHrclass get staff error! ", ex);
        }
    }


    /**
     *
     * SVF-OUT 学籍
     *
     */
    private void printSvfRegdOut(final DB2UDB db2, final Vrw32alp svf, final ResultSet rs, final Param param) {

        try {
            final String nendo = (Integer.parseInt(param._year) >= KIJUN_YEAR) ? param._nendo+"定期試験の記録" : param._nendo;
            svf.VrsOut("NENDO",     nendo); //年度
            svf.VrsOut("GRADE",     String.valueOf(Integer.parseInt(param._hrclass.substring(0,2))));   //学年
            svf.VrsOut("HR_CLASS",  param._hrclass.substring(2));                                       //組
            svf.VrsOut("ATTENDNO",  String.valueOf(rs.getInt("ATTENDNO")));                       //出席番号
            svf.VrsOut("NAME",      "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME")); //生徒名
            svf.VrsOut("STAFFNAME", param._staffname);                                                    //学級担任
            svf.VrsOut("DATE",      param._date2);                                                    //処理日

            //---NO003
            //if (param._11 == null) getCoursecodeName(db2, rs.getString("COURSECODE"), param);
            getCoursecodeName(db2, rs.getString("COURSECODE"), param);
            svf.VrsOut("NOTE1",  "上記の平均点は" + param._coursecodename  + "の平均です。");

            svf.VrsOut("HEIGHT",      (rs.getString("HEIGHT") != null) ? rs.getString("HEIGHT") : "");      //身長
            svf.VrsOut("WEIGHT",      (rs.getString("WEIGHT") != null) ? rs.getString("WEIGHT") : "");       //体重
            svf.VrsOut("SITHEIGHT",   (rs.getString("SITHEIGHT") != null) ? rs.getString("SITHEIGHT") : ""); //座高
            svf.VrsOut("R_EYESIGHT1", printSvfRegdOutVision(rs.getString("R_BAREVISION")));//視力（右）
            svf.VrsOut("R_EYESIGHT2", printSvfRegdOutVision(rs.getString("R_VISION")));    //視力（右・矯正）
            svf.VrsOut("L_EYESIGHT1", printSvfRegdOutVision(rs.getString("L_BAREVISION")));//視力（左）
            svf.VrsOut("L_EYESIGHT2", printSvfRegdOutVision(rs.getString("L_VISION")));    //視力（左・矯正）

            //道徳
            final String moral1 = rs.getString("MORAL1"); //1学期
            final String moral2 = rs.getString("MORAL2"); //2学期
            final String moral3 = rs.getString("MORAL3"); //3学期

            //道徳 1学期
            boolean moralFlg = KNJ_EditEdit.getMS932ByteLength(moral1) > 60 ? true : false;
            if(moralFlg) {
                final String[] val = KNJ_EditEdit.get_token(moral1, 60, 2);
                svf.VrsOut("MORAL1_1", val[0]);    //道徳 1学期
                svf.VrsOut("MORAL1_2", val[1]);    //道徳 1学期
            }else {
                svf.VrsOut("MORAL1_1", moral1);    //道徳 1学期
            }

            if("2".equals(param._gakki)) {
                //道徳 2学期
                moralFlg = KNJ_EditEdit.getMS932ByteLength(moral2) > 60 ? true : false;
                if(moralFlg) {
                    final String[] val = KNJ_EditEdit.get_token(moral2, 60, 2);
                    svf.VrsOut("MORAL2_1", val[0]);    //道徳 2学期
                    svf.VrsOut("MORAL2_2", val[1]);    //道徳 2学期
                }else {
                    svf.VrsOut("MORAL2_1", moral2);    //道徳 2学期
                }
            }else if("3".equals(param._gakki)) {
                //道徳 2学期
                moralFlg = KNJ_EditEdit.getMS932ByteLength(moral2) > 60 ? true : false;
                if(moralFlg) {
                    final String[] val = KNJ_EditEdit.get_token(moral2, 60, 2);
                    svf.VrsOut("MORAL2_1", val[0]);    //道徳 2学期
                    svf.VrsOut("MORAL2_2", val[1]);    //道徳 2学期
                }else {
                    svf.VrsOut("MORAL2_1", moral2);    //道徳 2学期
                }

                //道徳 3学期
                moralFlg = KNJ_EditEdit.getMS932ByteLength(moral3) > 60 ? true : false;
                if(moralFlg) {
                    final String[] val = KNJ_EditEdit.get_token(moral3, 60, 2);
                    svf.VrsOut("MORAL3_1", val[0]);    //道徳 3学期
                    svf.VrsOut("MORAL3_2", val[1]);    //道徳 3学期
                }else {
                    svf.VrsOut("MORAL3_1", moral3);    //道徳 3学期
                }
            }


        } catch (SQLException ex) {
            log.error("[KNJD171K]printSvfRegdOut error!", ex);
        }

    }//printSvfRegdOut()の括り


    /**
     *
     * SVF-OUT 健康診断印刷 視力出力
     *         04/12/15
     */
    private String printSvfRegdOutVision(final String vision) {

        String str = null;
        if (vision != null)
            if (3 < vision.length())
                if (vision.substring(vision.length() - 1, vision.length()).equals("0"))
                    str = vision.substring(0, vision.length() - 1);
                else
                    str = vision;
            else
                str = vision;
        else str = "";

        return str;
    }


    /**
     *
     * SVF-OUT 備考のコース名設定
     * １学年、２学年の医薬・特進はグループ化
     *
     */
    private void getCoursecodeName(final DB2UDB db2, final String coursecode, final Param param)
    {
        if (param._coursecodename != null && param._coursecode != null && coursecode.equals(param._coursecode)) return;//---NO003
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(    "SELECT  COURSECODE,COURSECODENAME ");
            stb.append(    "FROM    COURSECODE_MST ");
            if (coursecode.equals("JOIN1")) {
                stb.append("WHERE   COURSECODE IN('1002','2004') ");
            } else if (coursecode.equals("JOIN2")) {
                if (Integer.parseInt(param._year) < 2013) {
                    stb.append("WHERE   COURSECODE IN('1001','2001') ");
                } else {
                    stb.append("WHERE   COURSECODE IN('1002','2004') ");
                }
            } else {
                stb.append("WHERE   COURSECODE = '" + coursecode + "' ");
            }
            stb.append("ORDER BY COURSECODE ");
            db2.query(stb.toString());
            stb = new StringBuffer();
            ResultSet rs = db2.getResultSet();
            while(rs.next()) {
                if (0 < stb.length())stb.append("・");
                stb.append(rs.getString("COURSECODENAME"));
            }
            param._coursecodename = stb.toString();
            param._coursecode = coursecode;//---NO003
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
    }


    /**
     *
     * SVF-OUT 成績明細印刷
     */
    private void printSvfRecDetailOut(final Vrw32alp svf, final ResultSet rs, final Param param, final KNJD171K_Laser.ReturnVal returnval) {

        try {
            final String classCd = rs.getString("SUBCLASSCD").substring(0,2);
            if (Integer.parseInt(rs.getString("KIND")) == 1  &&
                classCd.equals("X9")) return;   //９教科の中間試験はない

        } catch (SQLException ex) {
            log.error("error! ", ex);
        }

        try {

            final int kind = Integer.parseInt(rs.getString("KIND"));
            //KIJUN_YEAR以降の場合、学期成績は出力しない
            if(Integer.parseInt(param._year) >= KIJUN_YEAR && (kind == 1 || kind == 2) || !(Integer.parseInt(param._year) >= KIJUN_YEAR)) {

                returnval.getSvfOutPoint(param, Integer.parseInt(rs.getString("SEMESTER")), kind);
                returnval.getSvfOutField(rs.getString("SUBCLASSCD"), Integer.parseInt(rs.getString("SEMESTER")), kind);
                svf.VrsOutn(returnval.field,  returnval.point,      getOutputDat(rs));            //得点
                svf.VrsOutn(returnval.field,  returnval.point + 1,  getOutputHrDat(rs));          //得点
            }

            //学年成績
            if(!(Integer.parseInt(param._year) >= KIJUN_YEAR)) {
                if (rs.getString("SEMESTER").equals("3")  &&  Integer.parseInt(rs.getString("KIND")) == 3  &&  rs.getString("ASSESS") != null) {
                    svf.VrsOutn(returnval.field,  17,  rs.getString("ASSESS"));                     //学年評定
                }else if(rs.getString("SEMESTER").equals("3")  &&  Integer.parseInt(rs.getString("KIND")) == 3  &&  rs.getString("ASSESS") == null) {
                    final String classCd = rs.getString("SUBCLASSCD").substring(0,1);
                    if (!classCd.equals("X") &&
                        ((rs.getString("REC_MARK") != null && rs.getInt("REC_MARK") < 2) ||
                          rs.getString("REC_MARK") == null)) {
                        svf.VrsOutn(returnval.field,  17,  "-");                        //学年評定
                    }
                }
            }

//log.debug("semes="+Integer.parseInt(rs.getString("SEMESTER")) + "   kind="+ Integer.parseInt(rs.getString("KIND")) + "  assess =" + rs.getString("ASSESS"));
//log.debug("field="+returnval.field + "   point="+returnval.point + "   getOutputDat(rs)="+getOutputDat(rs));

        } catch (SQLException ex) {
            log.error("error! ", ex);
        }

    }//printSvfRecDetailOut()の括り


    /**
     *
     * SVF-FORM 生徒成績データをセット
     */
    private String getOutputDat(final ResultSet rs)
    {
        String retval = null;
        try {
            if (rs.getString("KK_SCORE") == null) {
                if (rs.getString("SCORE") != null) {
                    if (rs.getString("SUBCLASSCD").charAt(rs.getString("SUBCLASSCD").length() - 1) == 'H') {
                        retval = setScoreFormat(2, rs.getString("SCORE"));        //小数点第一位まで
                    }else {
//NO028-->
                        if (Integer.parseInt(rs.getString("SEMESTER")) < 3) {

                            if (null != rs.getString("REC_FLG") && rs.getString("REC_FLG").equals("1")) {
                                    retval = "(" + setScoreFormat(1, rs.getString("SCORE")) + ")";        //整数
                            }else {
                                retval = setScoreFormat(1, rs.getString("SCORE"));        //整数
                            }
//NO028<--
//NO027-->
                        }else {
                            retval = setScoreFormat(1, rs.getString("SCORE"));        //整数
                        }
//NO027<--
                    }
                }
            } else{
                retval = rs.getString("KK_SCORE");                                      //欠席
            }
        } catch (Exception ex) {
            log.error("getOutputDate error! ", ex);
        }
        if (retval == null) retval = "";
        return retval;
    }


    /**
     *
     * SVF-FORM クラス平均データをセット
     */
    private String getOutputHrDat(final ResultSet rs)
    {
        String retval = null;
        try {
            if (rs.getString("HR_AVG_SCORE") != null) {
                if (rs.getString("SUBCLASSCD").charAt(rs.getString("SUBCLASSCD").length() - 1) != 'R') {
                    retval = setScoreFormat(2, rs.getString("HR_AVG_SCORE"));     //小数点第一位まで
                } else {
                    retval = setScoreFormat(1, rs.getString("HR_AVG_SCORE"));     //整数
                }
            }
        } catch (Exception ex) {
            log.error("getOutputDate error! ", ex);
        }
        if (retval == null) retval = "";
        return retval;
    }


    /**
     *
     * SVF-FORM 数値編集
     */
    private String setScoreFormat(final int div, final String score)
    {
        String retval = null;
        if (score == null) return "";
        try {
            if (div == 1) {
                retval = String.valueOf(dmf1.format(Float.parseFloat(score)));
            } else {
                retval = String.valueOf(dmf2.format(Float.parseFloat(score)));
            }
        } catch (Exception ex) {
            log.error("getOutputDate error! ", ex);
        }
        if (retval == null) retval = "";
        return retval;
    }


    /**
     *
     * SVF-OUT 出欠明細印刷処理
     */
    private void printSvfAttend(final Vrw32alp svf, final Param param, final PreparedStatement ps, final String schregno, final int sem)
    {
        int arrattend[] = {0,0,0,0,0,0,0,0};
        try {
            //------------------printSvfAttendOut(-, -, -, 集計開始学期, 集計終了学期,出力列)
            printSvfAttendOut(svf, param, ps, schregno, sem, arrattend);
            printSvfAttendOutT(svf, arrattend);
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
    }


    /**
     *
     * SVF-OUT 出欠明細印刷
     */
    private void printSvfAttendOut(final Vrw32alp svf, final Param param, final PreparedStatement ps, final String schregno, final int sem, final int[] arrattend)
    {
        ResultSet rs = null;
        try {
            int pp = 0;
            ps.setString(++pp, schregno);                 //生徒番号
//log.debug("ps="+ps.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                if ("9".equals(rs.getString("SEMESTER"))) {
                    continue;
                }
                final int k = rs.getInt("SEMESTER");
                if (k > sem) {
                    continue;
                }
                svf.VrsOutn("LESSON1",  k,   rs.getString("LESSON"));        //授業日数
                svf.VrsOutn("KIBIKI",   k,   rs.getString("MOURNING"));      //忌引日数
                svf.VrsOutn("SUSPEND",  k,   String.valueOf(rs.getInt("SUSPEND") + rs.getInt("VIRUS") + rs.getInt("KOUDOME")));       //出停日数
                svf.VrsOutn("LESSON2",  k,   rs.getString("MLESSON"));      //出席すべき日数
                svf.VrsOutn("ATTEND",   k,   rs.getString("PRESENT"));       //出席日数
                svf.VrsOutn("ABSENCE",  k,   rs.getString("SICK"));        //欠席日数
                svf.VrsOutn("LATE",     k,   rs.getString("LATE"));          //遅刻回数
                svf.VrsOutn("LEAVE",    k,   rs.getString("EARLY"));         //早退回数

                arrattend[0] += Integer.parseInt(rs.getString("LESSON"));
                arrattend[1] += Integer.parseInt(rs.getString("MOURNING"));
                arrattend[2] += rs.getInt("SUSPEND") + rs.getInt("VIRUS") + rs.getInt("KOUDOME");
                arrattend[3] += Integer.parseInt(rs.getString("MLESSON"));
                arrattend[4] += Integer.parseInt(rs.getString("PRESENT"));
                arrattend[5] += Integer.parseInt(rs.getString("SICK"));
                arrattend[6] += Integer.parseInt(rs.getString("LATE"));
                arrattend[7] += Integer.parseInt(rs.getString("EARLY"));
            }
        } catch (Exception ex) {
            log.error("[KNJD171K]printSvfAttendOut error!", ex);
        } finally{
            DbUtils.closeQuietly(rs);
        }

    }//printSvfAttendOut()の括り


    /**
     *
     * SVF-OUT 出欠合計印刷
     */
    private void printSvfAttendOutT(final Vrw32alp svf, final int[] arrattend)
    {
        try {
            svf.VrsOutn("LESSON1",  4,   String.valueOf(arrattend[0]));        //授業日数
            svf.VrsOutn("KIBIKI",   4,   String.valueOf(arrattend[1]));        //忌引日数
            svf.VrsOutn("SUSPEND",  4,   String.valueOf(arrattend[2]));        //出停日数
            svf.VrsOutn("LESSON2",  4,   String.valueOf(arrattend[3]));        //出席すべき日数
            svf.VrsOutn("ATTEND",   4,   String.valueOf(arrattend[4]));        //出席日数
            svf.VrsOutn("ABSENCE",  4,   String.valueOf(arrattend[5]));        //欠席日数
            svf.VrsOutn("LATE",     4,   String.valueOf(arrattend[6]));        //遅刻回数
            svf.VrsOutn("LEAVE",    4,   String.valueOf(arrattend[7]));        //早退回数
        } catch (Exception ex) {
            log.error("[KNJD171K]printSvfAttendOut error!", ex);
        }
    }


    /**
     *
     * SVF-OUT 委員会・クラブ活動印刷処理
     */
    private void printSvfCommitAndClub(final Vrw32alp svf, final PreparedStatement ps, final String schregno)
    {
        ResultSet rs = null;
        try {
            int pp = 0;
            ps.setString(++pp, schregno);                 //生徒番号
            ps.setString(++pp, schregno);                 //生徒番号
            ps.setString(++pp, schregno);                 //生徒番号
            rs = ps.executeQuery();

            while (rs.next()) {
                if (rs.getString("FLG").equals("1")) {
                    svf.VrsOut("COMMITTEENAME1",   rs.getString("NAME"));    //生徒会
                } else if (rs.getString("FLG").equals("2")) {
                    svf.VrsOut("COMMITTEENAME2",   rs.getString("NAME"));    //学級活動
                } else if (rs.getString("FLG").equals("3")) {
                    svf.VrsOut("CLUBNAME",         rs.getString("NAME"));    //クラブ活動
                }
            }
        } catch (Exception ex) {
            log.error("error! ", ex);
        } finally{
            DbUtils.closeQuietly(rs);
        }
    }


    /**
     *  PrepareStatement作成
     *  学籍
     */
    String prestatementRegd(final Param param)
    {
        final StringBuffer stb = new StringBuffer();

        try {
            //異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.ATTENDNO, ");
            stb.append(            "CASE WHEN T1.GRADE IN('01') AND T1.COURSECODE <> '3002' THEN 'JOIN1'  ");
            if (Integer.parseInt(param._year) < 2013) {
                stb.append(            "     WHEN T1.GRADE IN('02') AND T1.COURSECODE <> '3001' THEN 'JOIN2'  ");
            } else {
                stb.append(            "     WHEN T1.GRADE IN('02') AND T1.COURSECODE <> '3002' THEN 'JOIN2'  ");
            }
            stb.append(            " ELSE T1.COURSECODE END AS COURSECODE ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1,V_SEMESTER_GRADE_MST T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' AND ");
            stb.append(            "T1.SEMESTER = '"+param._gakki+"' AND ");
            stb.append(            "T1.YEAR = T2.YEAR AND ");
            stb.append(            "T1.GRADE = T2.GRADE AND ");
            stb.append(            "T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(            "T1.GRADE||T1.HR_CLASS = ? AND ");
            if (param._schregno != null)
                stb.append(        "T1.SCHREGNO IN" + param._schregno + " AND ");

            stb.append(            "NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");     //05/10/11Modify
            stb.append(                             "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END))) ");  //05/10/11Modify

            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                               "(S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)) ");   // 04/11/09Modify
            stb.append(    ")");

            stb.append("SELECT  W1.ATTENDNO, W1.SCHREGNO, W2.NAME, W2.REAL_NAME, W1.COURSECODE, ");
            stb.append(       "(CASE WHEN W4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
            stb.append(        "W3.HEIGHT, W3.WEIGHT, W3.SITHEIGHT, W3.R_BAREVISION, W3.L_BAREVISION, W3.R_VISION, W3.L_VISION, ");
            stb.append(        "W5_1.REMARK1 AS MORAL1, W5_2.REMARK1 AS MORAL2, W5_3.REMARK1 AS MORAL3 ");
            stb.append("FROM    SCHNO W1 ");
            stb.append(        "INNER JOIN SCHREG_BASE_MST W2 ON W1.SCHREGNO = W2.SCHREGNO ");
            stb.append(        "LEFT JOIN MEDEXAM_DET_DAT   W3 ON W3.YEAR = '" + param._year + "' AND W3.SCHREGNO = W1.SCHREGNO ");
            stb.append(        "LEFT JOIN SCHREG_NAME_SETUP_DAT W4 ON W4.SCHREGNO = W1.SCHREGNO AND W4.DIV = '03' ");
            stb.append(        "LEFT JOIN HREPORTREMARK_DETAIL_DAT W5_1 ON W5_1.YEAR = '" + param._year + "' AND W5_1.SEMESTER = '1' AND W5_1.SCHREGNO = W1.SCHREGNO AND W5_1.DIV = '08' AND W5_1.CODE = '01' "); //1学期
            stb.append(        "LEFT JOIN HREPORTREMARK_DETAIL_DAT W5_2 ON W5_2.YEAR = '" + param._year + "' AND W5_2.SEMESTER = '2' AND W5_2.SCHREGNO = W1.SCHREGNO AND W5_2.DIV = '08' AND W5_2.CODE = '01' "); //2学期
            stb.append(        "LEFT JOIN HREPORTREMARK_DETAIL_DAT W5_3 ON W5_3.YEAR = '" + param._year + "' AND W5_3.SEMESTER = '3' AND W5_3.SCHREGNO = W1.SCHREGNO AND W5_3.DIV = '08' AND W5_3.CODE = '01' "); //3学期
            stb.append("ORDER BY 1");

        } catch (Exception ex) {
            log.error("[KNJD171K]prestatementRegd error!", ex);
        }
        return stb.toString();

    }//prestatementRegd()の括り



    /**
     *  PrepareStatement作成 成績明細-->生徒別科目別 ５教科 ９教科
     *  05/07/09 Modify 教科でグループ化する
     */
    private String prestatementSubclass(final Param param) {

        StringBuffer stb = new StringBuffer();

        try {
            //学籍の表（学年）
            stb.append("WITH SCHNO_C AS(");
            stb.append(    "SELECT  T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.ATTENDNO, ");
            stb.append(            "T1.GRADE, T1.HR_CLASS, T1.COURSECD, T1.MAJORCD, ");
            stb.append(            "CASE WHEN T1.GRADE IN('01') AND T1.COURSECODE <> '3001' THEN 'JOIN'  ELSE T1.COURSECODE END AS COURSECODE ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1, V_SEMESTER_GRADE_MST T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' AND ");
            stb.append(            "T1.SEMESTER <= '" + param._gakki + "' AND ");
            stb.append(            "T1.YEAR = T2.YEAR AND ");
            stb.append(            "T1.GRADE = T2.GRADE AND ");
            stb.append(            "T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(            "T1.GRADE = ? AND ");

            stb.append(            "NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(                           "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");     //05/10/11Modify
            stb.append(                             "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END))) ");  //05/10/11Modify

            stb.append(        "AND NOT EXISTS(SELECT  'X' ");
            stb.append(                       "FROM    SCHREG_TRANSFER_DAT S1 ");
            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                               "(S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)) ");
            stb.append(    ")");

            //学籍の表（クラス）
            stb.append(",SCHNO_B AS(");
            stb.append(    "SELECT  T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.ATTENDNO, ");
            stb.append(            "T1.GRADE, T1.HR_CLASS, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append(    "FROM    SCHNO_C T1 ");
            stb.append(    "WHERE   T1.GRADE||T1.HR_CLASS = ? ");
            stb.append(    ")");

            //学籍の表（コース）
            stb.append(",SCHNO_A AS(");
            stb.append(    "SELECT  T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.ATTENDNO, ");
            stb.append(            "T1.GRADE, T1.HR_CLASS, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append(    "FROM    SCHNO_C T1 ");
            stb.append(    "WHERE   (T1.SEMESTER,T1.COURSECODE) IN(SELECT  T2.SEMESTER,T2.COURSECODE ");
            stb.append(                                           "FROM    SCHNO_C T2 ");
            stb.append(                                           "WHERE   T2.GRADE||T2.HR_CLASS = ?) ");
            stb.append(    ")");

            //成績データの表 対象生徒はコース
            //    成績がNULLで出欠が'KK','KS'の場合出欠データ在り
            //    成績がNOT NULLの場合成績データ在り
//NO027-->
            stb.append(", CHR_TEST_TABLE_SUB AS ( ");
            stb.append(" SELECT ");
            stb.append("     CHAIRCD, ");
            stb.append("     TESTKINDCD, ");
            stb.append("     YEAR, ");
            stb.append("     SEMESTER ");
            stb.append(" FROM ");
            stb.append("     SCH_CHR_TEST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER < '3' ");
            stb.append(" GROUP BY ");
            stb.append("     CHAIRCD, ");
            stb.append("     TESTKINDCD, ");
            stb.append("     YEAR, ");
            stb.append("     SEMESTER ");
            stb.append(" ), CHR_TEST_TABLE AS ( ");
            stb.append(" SELECT ");
            stb.append("     CHAIRCD AS CHACD, ");
            stb.append("     SEMESTER AS SEMES, ");
            stb.append("     SUM(SMALLINT(TESTKINDCD)) AS TESTSUM ");
            stb.append(" FROM ");
            stb.append("     CHR_TEST_TABLE_SUB ");
            stb.append(" GROUP BY ");
            stb.append("     CHAIRCD, ");
            stb.append("     SEMESTER ");
            stb.append(" ) ");
//NO027<--
            stb.append(",RECORD_DAT_A AS(");
            stb.append(    "SELECT  '1' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "W1.SCHOOL_KIND, ");
                stb.append(            "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append(            "W1.SUBCLASSCD, ");
            }
            stb.append(            "CASE WHEN SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS') THEN '( )' ");
            stb.append(                 "ELSE NULL END AS KK_SCORE1, ");
            stb.append(            "CASE WHEN SEM1_TERM_REC IS NULL AND SEM1_TERM_REC_DI IN('KK','KS') THEN '( )' ");
            stb.append(                 "ELSE NULL END AS KK_SCORE2, ");
            stb.append(            "CASE WHEN w1.schregno IS NOT NULL THEN NULL ELSE '( )' END AS KK_SCORE3, ");

            stb.append(            "CASE WHEN SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS') THEN NULL ");
            stb.append(                 "WHEN SEM1_INTER_REC IS NOT NULL THEN SEM1_INTER_REC ");
            stb.append(                 "ELSE NULL END AS SCORE1, ");
            stb.append(            "CASE WHEN SEM1_TERM_REC IS NULL AND SEM1_TERM_REC_DI IN('KK','KS') THEN NULL ");
            stb.append(                 "WHEN SEM1_TERM_REC IS NOT NULL THEN SEM1_TERM_REC ");
            stb.append(                 "ELSE NULL END AS SCORE2, ");
            stb.append(            "CASE WHEN SEM1_REC IS NOT NULL THEN SEM1_REC ");
            stb.append(                 "ELSE NULL END AS SCORE3, ");
            stb.append(            "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
            stb.append(            "W1.SEM1_REC_FLG AS REC_FLG, ");    //NO028
//NO027-->
            stb.append(            "CASE WHEN SEM1_REC IS NULL THEN 0 ELSE 1 END AS SEM1_REC_MARK, ");
            stb.append(            "CASE WHEN SEM2_REC IS NULL THEN 0 ELSE 1 END AS SEM2_REC_MARK, ");
            stb.append(            "CASE WHEN SEM3_REC IS NULL THEN 0 ELSE 1 END AS SEM3_REC_MARK, ");
            stb.append(            "SEM1_INTER_REC_DI,SEM1_TERM_REC_DI, ");
            stb.append(            "SEM2_INTER_REC_DI,SEM2_TERM_REC_DI, ");
            stb.append(            "L1.TESTSUM AS TESTSUM1,L2.TESTSUM AS TESTSUM2 ");
//NO027<--
            stb.append(    "FROM    KIN_RECORD_DAT W1 ");
            stb.append(            "INNER JOIN SCHNO_A W2 ON W2.SEMESTER = '1' AND ");
            stb.append(                                    "W1.SCHREGNO = W2.SCHREGNO ");
//NO027-->
            stb.append(            "LEFT JOIN CHR_TEST_TABLE L1 ON L1.CHACD = W1.CHAIRCD AND ");
            stb.append(                                    "L1.SEMES = '1' ");
            stb.append(            "LEFT JOIN CHR_TEST_TABLE L2 ON L2.CHACD = W1.CHAIRCD AND ");
            stb.append(                                    "L2.SEMES = '2' ");
//NO027<--
            stb.append(    "WHERE   W1.YEAR = '" + param._year + "' ");

        if (1 < Integer.parseInt( param._gakki)) {   //05/06/22if〜Build
            stb.append(    "UNION ");
            stb.append(    "SELECT  '2' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "W1.SCHOOL_KIND, ");
                stb.append(            "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append(            "W1.SUBCLASSCD, ");
            }
            stb.append(            "CASE WHEN SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS') THEN '( )' ");
            stb.append(                 "ELSE NULL END AS KK_SCORE1, ");
            stb.append(            "CASE WHEN SEM2_TERM_REC IS NULL AND SEM2_TERM_REC_DI IN('KK','KS') THEN '( )' ");
            stb.append(                 "ELSE NULL END AS KK_SCORE2, ");
            stb.append(            "CASE WHEN w1.schregno IS NOT NULL THEN NULL ELSE '( )' END AS KK_SCORE3, ");

            stb.append(            "CASE WHEN SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS') THEN NULL ");
            stb.append(                 "WHEN SEM2_INTER_REC IS NOT NULL THEN SEM2_INTER_REC ");
            stb.append(                 "ELSE NULL END AS SCORE1, ");
            stb.append(            "CASE WHEN SEM2_TERM_REC IS NULL AND SEM2_TERM_REC_DI IN('KK','KS') THEN NULL ");
            stb.append(                 "WHEN SEM2_TERM_REC IS NOT NULL THEN SEM2_TERM_REC ");
            stb.append(                 "ELSE NULL END AS SCORE2, ");
            stb.append(            "CASE WHEN SEM2_REC IS NOT NULL THEN SEM2_REC ");
            stb.append(                 "ELSE NULL END AS SCORE3, ");
            stb.append(            "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
            stb.append(            "W1.SEM2_REC_FLG AS REC_FLG, ");    //NO028
//NO027-->
            stb.append(            "CASE WHEN SEM1_REC IS NULL THEN 0 ELSE 1 END AS SEM1_REC_MARK, ");
            stb.append(            "CASE WHEN SEM2_REC IS NULL THEN 0 ELSE 1 END AS SEM2_REC_MARK, ");
            stb.append(            "CASE WHEN SEM3_REC IS NULL THEN 0 ELSE 1 END AS SEM3_REC_MARK, ");
            stb.append(            "SEM1_INTER_REC_DI,SEM1_TERM_REC_DI, ");
            stb.append(            "SEM2_INTER_REC_DI,SEM2_TERM_REC_DI, ");
            stb.append(            "L1.TESTSUM AS TESTSUM1,L2.TESTSUM AS TESTSUM2 ");
//NO027<--
            stb.append(    "FROM    KIN_RECORD_DAT W1 ");
            stb.append(            "INNER JOIN SCHNO_A W2 ON W2.SEMESTER = '2' AND ");
            stb.append(                                     "W1.SCHREGNO = W2.SCHREGNO ");
//NO027-->
            stb.append(            "LEFT JOIN CHR_TEST_TABLE L1 ON L1.CHACD = W1.CHAIRCD AND ");
            stb.append(                                    "L1.SEMES = '1' ");
            stb.append(            "LEFT JOIN CHR_TEST_TABLE L2 ON L2.CHACD = W1.CHAIRCD AND ");
            stb.append(                                    "L2.SEMES = '2' ");
//NO027<--
            stb.append(    "WHERE   W1.YEAR = '" + param._year + "' ");
        }

        if (2 < Integer.parseInt( param._gakki)) {   //05/06/22if〜Build
            stb.append(    "UNION ");
            stb.append(    "SELECT  '3' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "W1.SCHOOL_KIND, ");
                stb.append(            "W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append(            "W1.SUBCLASSCD, ");
            }
            stb.append(            "NULLIF(W1.SCHREGNO,W2.SCHREGNO) AS KK_SCORE1, ");
            stb.append(            "CASE WHEN SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI IN('KK','KS') THEN '( )' ");
            stb.append(                 "ELSE NULL END AS KK_SCORE2, ");
            stb.append(            "CASE WHEN w1.schregno IS NOT NULL THEN NULL ELSE '( )' END AS KK_SCORE3, ");

            stb.append(            "CASE WHEN W1.SCHREGNO = W2.SCHREGNO THEN NULL ELSE 0 END AS SCORE1, ");
            stb.append(            "CASE WHEN SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI IN('KK','KS') THEN NULL ");
            stb.append(                 "WHEN SEM3_TERM_REC IS NOT NULL THEN SEM3_TERM_REC ");
            stb.append(                 "ELSE NULL END AS SCORE2, ");
            stb.append(            "CASE WHEN SEM3_REC IS NOT NULL THEN SEM3_REC ");
            stb.append(                 "ELSE NULL END AS SCORE3, ");
            stb.append(            "GRADE_ASSESS AS ASSESS, ");
            stb.append(            "W1.SEM3_REC_FLG AS REC_FLG, ");    //NO028
//NO027-->
            stb.append(            "CASE WHEN SEM1_REC IS NULL THEN 0 ELSE 1 END AS SEM1_REC_MARK, ");
            stb.append(            "CASE WHEN SEM2_REC IS NULL THEN 0 ELSE 1 END AS SEM2_REC_MARK, ");
            stb.append(            "CASE WHEN SEM3_REC IS NULL THEN 0 ELSE 1 END AS SEM3_REC_MARK, ");
            stb.append(            "SEM1_INTER_REC_DI,SEM1_TERM_REC_DI, ");
            stb.append(            "SEM2_INTER_REC_DI,SEM2_TERM_REC_DI, ");
            stb.append(            "L1.TESTSUM AS TESTSUM1,L2.TESTSUM AS TESTSUM2 ");
//NO027<--
            stb.append(    "FROM    KIN_RECORD_DAT W1 ");
            stb.append(            "INNER JOIN SCHNO_A W2 ON W2.SEMESTER = '3' AND ");
            stb.append(                                     "W1.SCHREGNO = W2.SCHREGNO ");
//NO027-->
            stb.append(            "LEFT JOIN CHR_TEST_TABLE L1 ON L1.CHACD = W1.CHAIRCD AND ");
            stb.append(                                    "L1.SEMES = '1' ");
            stb.append(            "LEFT JOIN CHR_TEST_TABLE L2 ON L2.CHACD = W1.CHAIRCD AND ");
            stb.append(                                    "L2.SEMES = '2' ");
//NO027<--
            stb.append(    "WHERE   W1.YEAR = '" + param._year + "' ");
        }
            stb.append(    ")");

            //成績データの表 学期・成績種別で分解 => 学期・成績種別ごとで１レコードとする
            //05/07/09 Modify 教科でグループ化する
            //05/07/09 Modify 教科別で、すべての成績がNULLの場合は()表示
            stb.append(",RECORD_DAT_B AS(");
            stb.append(    "SELECT  SEMESTER, 1 AS KIND, ATTENDNO, SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "SCHOOL_KIND, ");
                stb.append(            "SUBSTR(SUBCLASSCD,5,2) as SUBCLASSCD, ");
            } else {
                stb.append(            "SUBSTR(SUBCLASSCD,1,2) as SUBCLASSCD, ");
            }
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "CASE WHEN '05' < SUBSTR(SUBCLASSCD,5,2) THEN NULL WHEN SUM(SCORE1) IS NULL THEN '( )' ELSE MIN(KK_SCORE1) END AS KK_SCORE, ");
            } else {
                stb.append(            "CASE WHEN '05' < SUBSTR(SUBCLASSCD,1,2) THEN NULL WHEN SUM(SCORE1) IS NULL THEN '( )' ELSE MIN(KK_SCORE1) END AS KK_SCORE, ");
            }
            stb.append(            "SUM(SCORE1) AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_A ");
            stb.append(    "where   semester <= '" + param._gakki + "' ");  //05/10/20Modify
            stb.append(    "GROUP BY SCHREGNO, ATTENDNO, SEMESTER, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "SCHOOL_KIND, ");
                stb.append(            "SUBSTR(SUBCLASSCD,5,2) ");
            } else {
                stb.append(            "SUBSTR(SUBCLASSCD,1,2) ");
            }

            stb.append(    "UNION ");
            stb.append(    "SELECT  SEMESTER, 2 AS KIND, ATTENDNO, SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "SCHOOL_KIND, ");
                stb.append(            "SUBSTR(SUBCLASSCD,5,2) as SUBCLASSCD, ");
            } else {
                stb.append(            "SUBSTR(SUBCLASSCD,1,2) as SUBCLASSCD, ");
            }
            stb.append(            "CASE WHEN SUM(SCORE2) IS NULL THEN '( )' ELSE MIN(KK_SCORE2) END AS KK_SCORE, ");
            stb.append(            "SUM(SCORE2) AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_A ");

            if (param._testkindcd.equals("99"))
                stb.append("where   semester <= '" + param._gakki + "' ");  //05/10/20Modify
            else
                stb.append("where   semester <  '" + param._gakki + "' ");  //05/10/20Modify

            stb.append(    "GROUP BY SCHREGNO, ATTENDNO, SEMESTER, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "SCHOOL_KIND, ");
                stb.append(            "SUBSTR(SUBCLASSCD,5,2) ");
            } else {
                stb.append(            "SUBSTR(SUBCLASSCD,1,2) ");
            }

            stb.append(    "UNION ");
            stb.append(    "SELECT  SEMESTER, 3 AS KIND, ATTENDNO, SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "SCHOOL_KIND, ");
                stb.append(            "SUBSTR(SUBCLASSCD,5,2) as SUBCLASSCD, ");
            } else {
                stb.append(            "SUBSTR(SUBCLASSCD,1,2) as SUBCLASSCD, ");
            }
            stb.append(            "CASE WHEN SUM(SCORE3) IS NULL THEN '( )' ELSE MIN(KK_SCORE3) END AS KK_SCORE, ");
            stb.append(            "SUM(SCORE3) AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_A ");

            if (param._testkindcd.equals("99"))
                stb.append("where   semester <= '" + param._gakki + "' ");  //05/10/20Modify
            else
                stb.append("where   semester <  '" + param._gakki + "' ");  //05/10/20Modify

            stb.append(    "GROUP BY SCHREGNO, ATTENDNO, SEMESTER, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "SCHOOL_KIND, ");
                stb.append(            "SUBSTR(SUBCLASSCD,5,2) ");
            } else {
                stb.append(            "SUBSTR(SUBCLASSCD,1,2) ");
            }

            stb.append(    ")");

            //成績データの表 生徒別学期別成績種別合計
            stb.append(",SCH_SUM AS(");
            stb.append(    "SELECT  SCHREGNO, SEMESTER, KIND,'X5G' AS SUBCLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "SCHOOL_KIND, ");
            }
            stb.append(            "SUM(SCORE) AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_B ");
            stb.append(    "WHERE ");
            stb.append(            "SUBSTR(SUBCLASSCD,1,2) <= '05' ");
            stb.append(    "GROUP BY SCHREGNO,SEMESTER,KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", SCHOOL_KIND ");
            }
            stb.append(    "UNION ");
            stb.append(    "SELECT  SCHREGNO,SEMESTER,KIND,'X9G' AS SUBCLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "SCHOOL_KIND, ");
            }
            stb.append(            "SUM(SCORE) AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_B ");
            stb.append(    "WHERE   SUBSTR(SUBCLASSCD,1,2) <= '09' ");
            stb.append(    "GROUP BY SCHREGNO,SEMESTER,KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", SCHOOL_KIND ");
            }
            stb.append(    ")");

            //成績データの表 生徒別学期別成績種別平均
            stb.append(",SCH_AVG AS(");
            stb.append(    "SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, 'X5H' AS SUBCLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "SCHOOL_KIND, ");
            }
            stb.append(            "ROUND(AVG(FLOAT(SCORE)) * 10, 0)/10 AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_B W1 ");
            stb.append(    "WHERE   SUBSTR(SUBCLASSCD,1,2) <= '05' ");
            stb.append(    "GROUP BY W1.SCHREGNO, W1.SEMESTER, W1.KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", SCHOOL_KIND ");
            }
            stb.append(    "UNION ");
            stb.append(    "SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, 'X9H' AS SUBCLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "SCHOOL_KIND, ");
            }
            stb.append(            "ROUND(AVG(FLOAT(SCORE)) * 10, 0)/10 AS SCORE ");
            stb.append(    "FROM    RECORD_DAT_B W1 ");
            stb.append(    "WHERE   SUBSTR(SUBCLASSCD,1,2) <= '09' ");
            stb.append(    "GROUP BY W1.SCHREGNO, W1.SEMESTER, W1.KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", SCHOOL_KIND ");
            }
            stb.append(    ")");

            //成績データの表 生徒別学期別成績種別 (教科全て受験していないデータ)---NO002
            stb.append(",SCH_NOTALL AS(");

            stb.append(     "SELECT  SCHREGNO,SEMESTER,KIND,'X5' AS SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", SCHOOL_KIND ");
            }
            stb.append(     "FROM    RECORD_DAT_B ");
            stb.append(     "WHERE   SCORE IS NOT NULL AND ");
            stb.append(             "SUBSTR(SUBCLASSCD,1,2) <= '05' ");
            stb.append(     "GROUP BY SCHREGNO,SEMESTER,KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", SCHOOL_KIND ");
            }
            stb.append(     "HAVING COUNT(*) < 5 ");

            stb.append(     "UNION ");
            stb.append(     "SELECT  SCHREGNO,SEMESTER,KIND,'X5' AS SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", SCHOOL_KIND ");
            }
            stb.append(     "FROM    RECORD_DAT_B ");
            stb.append(     "WHERE   KK_SCORE IS NOT NULL AND ");
            stb.append(             "SUBSTR(SUBCLASSCD,1,2) <= '05' ");
            stb.append(     "GROUP BY SCHREGNO,SEMESTER,KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", SCHOOL_KIND ");
            }

            stb.append(     "UNION ");
            stb.append(     "SELECT  SCHREGNO,SEMESTER,KIND,'X9' AS SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", SCHOOL_KIND ");
            }
            stb.append(     "FROM    RECORD_DAT_B ");
            stb.append(     "WHERE   SCORE IS NOT NULL AND ");
            stb.append(             "SUBSTR(SUBCLASSCD,1,2) <= '09' ");
            stb.append(     "GROUP BY SCHREGNO,SEMESTER,KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", SCHOOL_KIND ");
            }
            stb.append(     "HAVING COUNT(*) < 9 ");

            stb.append(     "UNION ");
            stb.append(     "SELECT  SCHREGNO,SEMESTER,KIND,'X9' AS SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", SCHOOL_KIND ");
            }
            stb.append(     "FROM    RECORD_DAT_B ");
            stb.append(     "WHERE   KK_SCORE IS NOT NULL AND ");
            stb.append(             "SUBSTR(SUBCLASSCD,1,2) <= '09' ");
            stb.append(     "GROUP BY SCHREGNO,SEMESTER,KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            ", SCHOOL_KIND ");
            }
            stb.append(     ")");

            //NO024----------↓----------
            //席次データの表A 学期・成績種別で分解 => 学期・成績種別ごとで１レコードとする
            stb.append(",RECORD_RANK_A AS( ");
            stb.append("    SELECT  '1' AS SEMESTER, W1.SCHREGNO, W1.RANK_DIV, ");
            stb.append("            SEM1_INTER_REC_RANK AS SCORE1, ");
            stb.append("            SEM1_TERM_REC_RANK AS SCORE2, ");
            stb.append("            SEM1_REC_RANK AS SCORE3 ");
            stb.append("    FROM    RECORD_RANK_DAT W1 ");
            stb.append("    WHERE   W1.YEAR = '"+param._year+"' AND W1.RANK_DIV IN('2','3') ");

        if (1 < Integer.parseInt( param._gakki)) {
            stb.append("    UNION ");
            stb.append("    SELECT  '2' AS SEMESTER, W1.SCHREGNO, W1.RANK_DIV, ");
            stb.append("            SEM2_INTER_REC_RANK AS SCORE1, ");
            stb.append("            SEM2_TERM_REC_RANK AS SCORE2, ");
            stb.append("            SEM2_REC_RANK AS SCORE3 ");
            stb.append("    FROM    RECORD_RANK_DAT W1 ");
            stb.append("    WHERE   W1.YEAR = '"+param._year+"' AND W1.RANK_DIV IN('2','3') ");
        }

        if (2 < Integer.parseInt( param._gakki)) {
            stb.append("    UNION ");
            stb.append("    SELECT  '3' AS SEMESTER, W1.SCHREGNO, W1.RANK_DIV, ");
            stb.append("            CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE 0 END AS SCORE1, ");
            stb.append("            SEM3_TERM_REC_RANK AS SCORE2, ");
            stb.append("            SEM3_REC_RANK AS SCORE3 ");
            stb.append("    FROM    RECORD_RANK_DAT W1 ");
            stb.append("    WHERE   W1.YEAR = '"+param._year+"' AND W1.RANK_DIV IN('2','3') ");
        }
            stb.append("    ) ");

            //席次データの表B 学期・成績種別で分解 => 学期・成績種別ごとで１レコードとする
            stb.append(",RECORD_RANK_B AS( ");
            stb.append("    SELECT  SEMESTER, 1 AS KIND, SCHREGNO, RANK_DIV, SCORE1 AS SCORE ");
            stb.append("    FROM    RECORD_RANK_A ");
            stb.append(    "where   semester <= '" + param._gakki + "' ");  //05/10/21Modify

        //if (Integer.parseInt( param._6) != 01) {
            stb.append("    UNION ");
            stb.append("    SELECT  SEMESTER, 2 AS KIND, SCHREGNO, RANK_DIV, SCORE2 AS SCORE ");
            stb.append("    FROM    RECORD_RANK_A ");
            if (param._testkindcd.equals("99"))
                stb.append("where   semester <= '" + param._gakki + "' ");  //05/10/21Modify
            else
                stb.append("where   semester <  '" + param._gakki + "' ");  //05/10/21Modify

            stb.append("    UNION ");
            stb.append("    SELECT  SEMESTER, 3 AS KIND, SCHREGNO, RANK_DIV, SCORE3 AS SCORE ");
            stb.append("    FROM    RECORD_RANK_A ");
            if (param._testkindcd.equals("99"))
                stb.append("where   semester <= '" + param._gakki + "' ");  //05/10/21Modify
            else
                stb.append("where   semester <  '" + param._gakki + "' ");  //05/10/21Modify
        //}
            stb.append("    ) ");

            //席次
            stb.append(",SCH_RANK AS ( ");
            stb.append("    SELECT  SCHREGNO, SEMESTER, KIND, 'X5R' AS SUBCLASSCD, SCORE AS RANK ");
            stb.append("    FROM    RECORD_RANK_B ");
            stb.append("    WHERE   SCORE IS NOT NULL AND RANK_DIV = '2' ");//５科
            stb.append("    UNION ");
            stb.append("    SELECT  SCHREGNO, SEMESTER, KIND, 'X9R' AS SUBCLASSCD, SCORE AS RANK ");
            stb.append("    FROM    RECORD_RANK_B ");
            stb.append("    WHERE   SCORE IS NOT NULL AND RANK_DIV = '3' ");//９科
            stb.append("    ) ");
            //NO024----------↑----------

            //NO025----------↓----------
            //平均データの表
            stb.append(",RECORD_CLASS_AVG AS ( ");
            stb.append("    SELECT * ");
            stb.append("    FROM   RECORD_CLASS_AVERAGE_DAT ");
            stb.append("    WHERE  YEAR = '" + param._year + "' AND GRADE||HR_CLASS = ? AND ");
            stb.append("           (CLASSCD <= '09' OR '5A' <= CLASSCD) AND CALC_DIV = '3' ");//3:コース平均
            stb.append("    ) ");

            //平均データの表A 学期・成績種別で分解 => 学期・成績種別ごとで１レコードとする
            stb.append(",RECORD_CLASS_AVG_A AS ( ");
            stb.append("    SELECT '1' AS SEMESTER, CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    , SCHOOL_KIND ");
            }
            stb.append("           ,SEM1_INTER_REC AS SCORE1 ");
            stb.append("           ,SEM1_TERM_REC AS SCORE2 ");
            stb.append("           ,SEM1_REC AS SCORE3 ");
            stb.append("    FROM   RECORD_CLASS_AVG ");

        if (1 < Integer.parseInt( param._gakki)) {
            stb.append("    UNION ");
            stb.append("    SELECT '2' AS SEMESTER, CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    , SCHOOL_KIND ");
            }
            stb.append("           ,SEM2_INTER_REC AS SCORE1 ");
            stb.append("           ,SEM2_TERM_REC AS SCORE2 ");
            stb.append("           ,SEM2_REC AS SCORE3 ");
            stb.append("    FROM   RECORD_CLASS_AVG ");
        }

        if (2 < Integer.parseInt( param._gakki)) {
            stb.append("    UNION ");
            stb.append("    SELECT '3' AS SEMESTER, CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    , SCHOOL_KIND ");
            }
            stb.append("           ,CASE WHEN CLASSCD IS NOT NULL THEN NULL ELSE 0 END AS SCORE1 ");
            stb.append("           ,SEM3_TERM_REC AS SCORE2 ");
            stb.append("           ,SEM3_REC AS SCORE3 ");
            stb.append("    FROM   RECORD_CLASS_AVG ");
        }
            stb.append("    ) ");

            //平均データの表B 学期・成績種別で分解 => 学期・成績種別ごとで１レコードとする
            stb.append(",RECORD_CLASS_AVG_B AS ( ");
            stb.append("    SELECT SEMESTER, 1 AS KIND, CLASSCD, SCORE1 AS SCORE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    , SCHOOL_KIND ");
            }
            stb.append("    FROM   RECORD_CLASS_AVG_A ");
            stb.append(    "where  semester <= '" + param._gakki + "' ");

            stb.append("    UNION ");
            stb.append("    SELECT SEMESTER, 2 AS KIND, CLASSCD, SCORE2 AS SCORE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    , SCHOOL_KIND ");
            }
            stb.append("    FROM   RECORD_CLASS_AVG_A ");
            if (param._testkindcd.equals("99"))
                stb.append("where  semester <= '" + param._gakki + "' ");
            else
                stb.append("where  semester <  '" + param._gakki + "' ");

            stb.append("    UNION ");
            stb.append("    SELECT SEMESTER, 3 AS KIND, CLASSCD, SCORE3 AS SCORE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    , SCHOOL_KIND ");
            }
            stb.append("    FROM   RECORD_CLASS_AVG_A ");
            if (param._testkindcd.equals("99"))
                stb.append("where  semester <= '" + param._gakki + "' ");
            else
                stb.append("where  semester <  '" + param._gakki + "' ");
            stb.append("    ) ");

            //平均データの表 学期別成績種別のコース平均 ---> 内容：HR_SUMと旧HR_AVERAGEを統合
            stb.append(",HR_AVERAGE AS ( ");
                            //各教科
            stb.append("    SELECT SEMESTER, KIND, CLASSCD AS SUBCLASSCD, SCORE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    , SCHOOL_KIND ");
            }
            stb.append("    FROM   RECORD_CLASS_AVG_B ");
            stb.append("    WHERE  CLASSCD <= '09' ");
                            //５科合計
            stb.append("    UNION ");
            stb.append("    SELECT SEMESTER, KIND, 'X5G' AS SUBCLASSCD, SCORE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    , SCHOOL_KIND ");
            }
            stb.append("    FROM   RECORD_CLASS_AVG_B ");
            stb.append("    WHERE  CLASSCD = '5T' ");
                            //５科平均
            stb.append("    UNION ");
            stb.append("    SELECT SEMESTER, KIND, 'X5H' AS SUBCLASSCD, SCORE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    , SCHOOL_KIND ");
            }
            stb.append("    FROM   RECORD_CLASS_AVG_B ");
            stb.append("    WHERE  CLASSCD = '5A' ");
                            //５科受験者数
            stb.append("    UNION ");
            stb.append("    SELECT SEMESTER, KIND, 'X5R' AS SUBCLASSCD, SCORE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    , SCHOOL_KIND ");
            }
            stb.append("    FROM   RECORD_CLASS_AVG_B ");
            stb.append("    WHERE  CLASSCD = '5C' ");
                            //９科合計
            stb.append("    UNION ");
            stb.append("    SELECT SEMESTER, KIND, 'X9G' AS SUBCLASSCD, SCORE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    , SCHOOL_KIND ");
            }
            stb.append("    FROM   RECORD_CLASS_AVG_B ");
            stb.append("    WHERE  CLASSCD = '9T' ");
                            //９科平均
            stb.append("    UNION ");
            stb.append("    SELECT SEMESTER, KIND, 'X9H' AS SUBCLASSCD, SCORE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    , SCHOOL_KIND ");
            }
            stb.append("    FROM   RECORD_CLASS_AVG_B ");
            stb.append("    WHERE  CLASSCD = '9A' ");
                            //９科受験者数
            stb.append("    UNION ");
            stb.append("    SELECT SEMESTER, KIND, 'X9R' AS SUBCLASSCD, SCORE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    , SCHOOL_KIND ");
            }
            stb.append("    FROM   RECORD_CLASS_AVG_B ");
            stb.append("    WHERE  CLASSCD = '9C' ");
            stb.append("    ) ");
            //NO025----------↑----------

            //メイン表 教科別学期別成績種別の成績（３学期は評定付き）
            stb.append("SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, W1.SUBCLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " W1.SCHOOL_KIND, ");
            }
            stb.append(        "W1.KK_SCORE, W1.SCORE, ");
            stb.append(        "W3.SCORE AS HR_AVG_SCORE, ");
            stb.append(        "W4.ASSESS, ");
            stb.append(        "(SELECT ATTENDNO FROM SCHNO_B W5 WHERE W5.SEMESTER = W1.SEMESTER AND W5.SCHREGNO = W1.SCHREGNO) AS ATTENDNO ");
//NO027-->
            stb.append(            ",L1.SEM1_REC_MARK+L1.SEM2_REC_MARK+L1.SEM3_REC_MARK AS REC_MARK, ");
            stb.append(            "L1.SEM1_INTER_REC_DI AS SEM1_INTER_DI, ");
            stb.append(            "L1.SEM1_TERM_REC_DI AS SEM1_TERM_DI, ");
            stb.append(            "L1.SEM2_INTER_REC_DI AS SEM2_INTER_DI, ");
            stb.append(            "L1.SEM2_TERM_REC_DI AS SEM2_TERM_DI, ");
            stb.append(            "L1.TESTSUM1 AS TESTSUM1, ");
            stb.append(            "L1.TESTSUM2 AS TESTSUM2, ");
            stb.append(            "L1.REC_FLG AS REC_FLG ");    //NO028
//NO027<--
            stb.append("FROM    RECORD_DAT_B W1 ");
            stb.append(        "LEFT JOIN HR_AVERAGE W3 ON W1.SEMESTER = W3.SEMESTER AND W1.KIND = W3.KIND AND W1.SUBCLASSCD = W3.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " AND W1.SCHOOL_KIND = W3.SCHOOL_KIND ");
            }
            stb.append(        "LEFT JOIN RECORD_DAT_A W4 ON W1.SCHREGNO = W4.SCHREGNO AND W1.SEMESTER = W4.SEMESTER AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                                     "W1.SCHOOL_KIND = W3.SCHOOL_KIND AND ");
                stb.append(                                     "W1.SUBCLASSCD = substr(W4.SUBCLASSCD,5,2) AND W4.SEMESTER = '3' ");   //05/10/20Modify
            } else {
                stb.append(                                     "W1.SUBCLASSCD = substr(W4.SUBCLASSCD,1,2) AND W4.SEMESTER = '3' ");   //05/10/20Modify
            }
            stb.append(        "LEFT JOIN RECORD_DAT_A L1 ON W1.SCHREGNO = L1.SCHREGNO AND W1.SEMESTER = L1.SEMESTER AND ");        //NO027
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                                     "W1.SCHOOL_KIND = W3.SCHOOL_KIND AND ");
                stb.append(                                     "W1.SUBCLASSCD = substr(L1.SUBCLASSCD,5,2) AND L1.SEMESTER < '3' ");    //NO027
            } else {
                stb.append(                                     "W1.SUBCLASSCD = substr(L1.SUBCLASSCD,1,2) AND L1.SEMESTER < '3' ");    //NO027
            }
            stb.append("WHERE ");
            if (param._schregno != null)
                stb.append(    "W1.SCHREGNO IN " + param._schregno + " AND ");
            stb.append(        "(W1.KK_SCORE IS NOT NULL OR W1.SCORE IS NOT NULL) ");

            //メイン表 教科別学期別成績種別の合計
            stb.append("UNION ");
            stb.append("SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, W1.SUBCLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " W1.SCHOOL_KIND, ");
            }
            stb.append(        "CASE WHEN W4.SCHREGNO IS NOT NULL AND W1.SCORE IS NULL THEN '( )' ELSE NULL END AS KK_SCORE, ");
            stb.append(        "W1.SCORE, ");
            stb.append(        "W3.SCORE AS HR_AVG_SCORE, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
            stb.append(        "(SELECT ATTENDNO FROM SCHNO_B W5 WHERE W5.SEMESTER = W1.SEMESTER AND W5.SCHREGNO = W1.SCHREGNO) AS ATTENDNO ");
//NO027-->
            stb.append(            ",CAST (NULL as SMALLINT) AS REC_MARK, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM1_INTER_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM1_TERM_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM2_INTER_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM2_TERM_DI, ");
            stb.append(            "CAST (NULL as SMALLINT) AS TESTSUM1, ");
            stb.append(            "CAST (NULL as SMALLINT) AS TESTSUM2, ");
            stb.append(            "CAST (NULL as VARCHAR(1)) AS REC_FLG ");    //NO028
//NO027<--
            stb.append("FROM    SCH_SUM W1 ");
            stb.append(        "LEFT JOIN HR_AVERAGE W3 ON W1.SEMESTER = W3.SEMESTER AND W1.KIND = W3.KIND AND W3.SUBCLASSCD = 'X5G' ");//NO025
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(               " AND W1.SCHOOL_KIND = W3.SCHOOL_KIND ");
            }
            stb.append(        "LEFT JOIN( SELECT  SCHREGNO,SEMESTER,KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " , SCHOOL_KIND ");
            }
            stb.append(                   "FROM    SCH_NOTALL W2 ");
            stb.append(                   "WHERE   SUBCLASSCD = 'X5' ");
            stb.append(                 ") W4 ON W1.SCHREGNO = W4.SCHREGNO AND W1.SEMESTER = W4.SEMESTER AND W1.KIND = W4.KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(               " AND W1.SCHOOL_KIND = W4.SCHOOL_KIND ");
            }
            stb.append("WHERE   W1.SUBCLASSCD = 'X5G' ");
            if (param._schregno != null)
                stb.append(    "AND W1.SCHREGNO IN " + param._schregno + " ");

            //メイン表 ５教科学期別成績種別の平均
            stb.append("UNION ");
            stb.append("SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, W1.SUBCLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " W1.SCHOOL_KIND, ");
            }
            stb.append(        "CASE WHEN W4.SCHREGNO IS NOT NULL AND W1.SCORE IS NULL THEN '( )' ELSE NULL END AS KK_SCORE, ");
            stb.append(        "W1.SCORE, ");
            stb.append(        "W3.SCORE AS HR_AVG_SCORE, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
            stb.append(        "(SELECT ATTENDNO FROM SCHNO_B W5 WHERE W5.SEMESTER = W1.SEMESTER AND W5.SCHREGNO = W1.SCHREGNO) AS ATTENDNO ");
//NO027-->
            stb.append(            ",CAST (NULL as SMALLINT) AS REC_MARK, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM1_INTER_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM1_TERM_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM2_INTER_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM2_TERM_DI, ");
            stb.append(            "CAST (NULL as SMALLINT) AS TESTSUM1, ");
            stb.append(            "CAST (NULL as SMALLINT) AS TESTSUM2, ");
            stb.append(            "CAST (NULL as VARCHAR(1)) AS REC_FLG ");    //NO028
//NO027<--
            stb.append("FROM    SCH_AVG W1 ");
            stb.append(        "LEFT JOIN HR_AVERAGE W3 ON W1.SEMESTER = W3.SEMESTER AND W1.KIND = W3.KIND AND W3.SUBCLASSCD = 'X5H' ");//NO025
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(               " AND W1.SCHOOL_KIND = W3.SCHOOL_KIND ");
            }
            stb.append(        "LEFT JOIN( SELECT  SCHREGNO,SEMESTER,KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " , SCHOOL_KIND ");
            }
            stb.append(                   "FROM    SCH_NOTALL W2 ");
            stb.append(                   "WHERE   SUBCLASSCD = 'X5' ");
            stb.append(                 ") W4 ON W1.SCHREGNO = W4.SCHREGNO AND W1.SEMESTER = W4.SEMESTER AND W1.KIND = W4.KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(               " AND W1.SCHOOL_KIND = W4.SCHOOL_KIND ");
            }
            stb.append("WHERE   W1.SUBCLASSCD = 'X5H' ");
            if (param._schregno != null)
                stb.append(    "AND W1.SCHREGNO IN " + param._schregno + " ");

            //メイン表 ５教科学期別成績種別の席次
            stb.append("UNION ");
            stb.append("SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, 'X5R' AS SUBCLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " W1.SCHOOL_KIND, ");
            }
            stb.append(        "CASE WHEN W2.RANK IS NULL THEN '( )' ELSE NULL END AS KK_SCORE, ");//NO026
            stb.append(        "W2.RANK AS SCORE, ");
            stb.append(        "W3.SCORE AS HR_AVG_SCORE, ");//NO025
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
            stb.append(        "(SELECT ATTENDNO FROM SCHNO_B W5 WHERE W5.SEMESTER = W1.SEMESTER AND W5.SCHREGNO = W1.SCHREGNO) AS ATTENDNO ");
//NO027-->
            stb.append(            ",CAST (NULL as SMALLINT) AS REC_MARK, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM1_INTER_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM1_TERM_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM2_INTER_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM2_TERM_DI, ");
            stb.append(            "CAST (NULL as SMALLINT) AS TESTSUM1, ");
            stb.append(            "CAST (NULL as SMALLINT) AS TESTSUM2, ");
            stb.append(            "CAST (NULL as VARCHAR(1)) AS REC_FLG ");    //NO028
//NO027<--
            stb.append("FROM    SCH_SUM W1 ");
            stb.append(        "LEFT JOIN SCH_RANK W2 ON W2.SCHREGNO = W1.SCHREGNO AND W2.SEMESTER = W1.SEMESTER AND W2.KIND = W1.KIND AND W2.SUBCLASSCD = 'X5R' ");
            stb.append(        "LEFT JOIN HR_AVERAGE W3 ON W1.SEMESTER = W3.SEMESTER AND W1.KIND = W3.KIND AND W3.SUBCLASSCD = 'X5R' ");//NO025
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(               " AND W1.SCHOOL_KIND = W3.SCHOOL_KIND ");
            }
            stb.append(        "LEFT JOIN( SELECT  SCHREGNO,SEMESTER,KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " , SCHOOL_KIND ");
            }
            stb.append(                   "FROM    SCH_NOTALL W2 ");
            stb.append(                   "WHERE   SUBCLASSCD = 'X5' ");
            stb.append(                 ") W4 ON W1.SCHREGNO = W4.SCHREGNO AND W1.SEMESTER = W4.SEMESTER AND W1.KIND = W4.KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(               " AND W1.SCHOOL_KIND = W4.SCHOOL_KIND ");
            }
            stb.append("WHERE   W1.SUBCLASSCD = 'X5G' ");
            if (param._schregno != null)
                stb.append(    "AND W1.SCHREGNO IN " + param._schregno + " ");

            //メイン表 ９教科学期別成績種別の合計
            stb.append("UNION ");
            stb.append("SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, W1.SUBCLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " W1.SCHOOL_KIND, ");
            }
            stb.append(        "CASE WHEN W4.SCHREGNO IS NOT NULL AND W1.SCORE IS NULL THEN '( )' ELSE NULL END AS KK_SCORE, ");
            stb.append(        "W1.SCORE, ");
            stb.append(        "W3.SCORE AS HR_AVG_SCORE, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
            stb.append(        "(SELECT ATTENDNO FROM SCHNO_B W5 WHERE W5.SEMESTER = W1.SEMESTER AND W5.SCHREGNO = W1.SCHREGNO) AS ATTENDNO ");
//NO027-->
            stb.append(            ",CAST (NULL as SMALLINT) AS REC_MARK, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM1_INTER_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM1_TERM_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM2_INTER_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM2_TERM_DI, ");
            stb.append(            "CAST (NULL as SMALLINT) AS TESTSUM1, ");
            stb.append(            "CAST (NULL as SMALLINT) AS TESTSUM2, ");
            stb.append(            "CAST (NULL as VARCHAR(1)) AS REC_FLG ");    //NO028
//NO027<--
            stb.append("FROM    SCH_SUM W1 ");
            stb.append(        "LEFT JOIN HR_AVERAGE W3 ON W1.SEMESTER = W3.SEMESTER AND W1.KIND = W3.KIND AND W3.SUBCLASSCD = 'X9G' ");//NO025
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(               " AND W1.SCHOOL_KIND = W3.SCHOOL_KIND ");
            }
            stb.append(        "LEFT JOIN( SELECT  SCHREGNO,SEMESTER,KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " , SCHOOL_KIND ");
            }
            stb.append(                   "FROM    SCH_NOTALL W2 ");
            stb.append(                   "WHERE   SUBCLASSCD = 'X9' ");
            stb.append(                 ") W4 ON W1.SCHREGNO = W4.SCHREGNO AND W1.SEMESTER = W4.SEMESTER AND W1.KIND = W4.KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(               " AND W1.SCHOOL_KIND = W4.SCHOOL_KIND ");
            }
            stb.append("WHERE   W1.SUBCLASSCD = 'X9G' ");
            if (param._schregno != null)
                stb.append(    "AND W1.SCHREGNO IN " + param._schregno + " ");

            //メイン表 ９教科学期別成績種別の平均
            stb.append("UNION ");
            stb.append("SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, W1.SUBCLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " W1.SCHOOL_KIND, ");
            }
            stb.append(        "CASE WHEN W4.SCHREGNO IS NOT NULL AND W1.SCORE IS NULL THEN '( )' ELSE NULL END AS KK_SCORE, ");
            stb.append(        "W1.SCORE, ");
            stb.append(        "W3.SCORE AS HR_AVG_SCORE, ");
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
            stb.append(        "(SELECT ATTENDNO FROM SCHNO_B W5 WHERE W5.SEMESTER = W1.SEMESTER AND W5.SCHREGNO = W1.SCHREGNO) AS ATTENDNO ");
//NO027-->
            stb.append(            ",CAST (NULL as SMALLINT) AS REC_MARK, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM1_INTER_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM1_TERM_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM2_INTER_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM2_TERM_DI, ");
            stb.append(            "CAST (NULL as SMALLINT) AS TESTSUM1, ");
            stb.append(            "CAST (NULL as SMALLINT) AS TESTSUM2, ");
            stb.append(            "CAST (NULL as VARCHAR(1)) AS REC_FLG ");    //NO028
//NO027<--
            stb.append("FROM    SCH_AVG W1 ");
            stb.append(        "LEFT JOIN HR_AVERAGE W3 ON W1.SEMESTER = W3.SEMESTER AND W1.KIND = W3.KIND AND W3.SUBCLASSCD = 'X9H' ");//NO025
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(               " AND W1.SCHOOL_KIND = W3.SCHOOL_KIND ");
            }
            stb.append(        "LEFT JOIN( SELECT  SCHREGNO,SEMESTER,KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " , SCHOOL_KIND ");
            }
            stb.append(                   "FROM    SCH_NOTALL W2 ");
            stb.append(                   "WHERE   SUBCLASSCD = 'X9' ");
            stb.append(                 ") W4 ON W1.SCHREGNO = W4.SCHREGNO AND W1.SEMESTER = W4.SEMESTER AND W1.KIND = W4.KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(               " AND W1.SCHOOL_KIND = W4.SCHOOL_KIND ");
            }
            stb.append("WHERE   W1.SUBCLASSCD = 'X9H' ");
            if (param._schregno != null)
                stb.append(    "AND W1.SCHREGNO IN " + param._schregno + " ");

            //メイン表 ９教科学期別成績種別の席次
            stb.append("UNION ");
            stb.append("SELECT  W1.SCHREGNO, W1.SEMESTER, W1.KIND, 'X9R' AS SUBCLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " W1.SCHOOL_KIND, ");
            }
            stb.append(        "CASE WHEN W2.RANK IS NULL THEN '( )' ELSE NULL END AS KK_SCORE, ");//NO026
            stb.append(        "W2.RANK AS SCORE, ");
            stb.append(        "W3.SCORE AS HR_AVG_SCORE, ");//NO025
            stb.append(        "CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
            stb.append(        "(SELECT ATTENDNO FROM SCHNO_B W5 WHERE W5.SEMESTER = W1.SEMESTER AND W5.SCHREGNO = W1.SCHREGNO) AS ATTENDNO ");
//NO027-->
            stb.append(            ",CAST (NULL as SMALLINT) AS REC_MARK, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM1_INTER_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM1_TERM_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM2_INTER_DI, ");
            stb.append(            "CAST (NULL as VARCHAR(2)) AS SEM2_TERM_DI, ");
            stb.append(            "CAST (NULL as SMALLINT) AS TESTSUM1, ");
            stb.append(            "CAST (NULL as SMALLINT) AS TESTSUM2, ");
            stb.append(            "CAST (NULL as VARCHAR(1)) AS REC_FLG ");    //NO028
//NO027<--
            stb.append("FROM    SCH_SUM W1 ");
            stb.append(        "LEFT JOIN SCH_RANK W2 ON W2.SCHREGNO = W1.SCHREGNO AND W2.SEMESTER = W1.SEMESTER AND W2.KIND = W1.KIND AND W2.SUBCLASSCD = 'X9R' ");
            stb.append(        "LEFT JOIN HR_AVERAGE W3 ON W1.SEMESTER = W3.SEMESTER AND W1.KIND = W3.KIND AND W3.SUBCLASSCD = 'X9R' ");//NO025
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(               " AND W1.SCHOOL_KIND = W3.SCHOOL_KIND ");
            }
            stb.append(        "LEFT JOIN( SELECT  SCHREGNO,SEMESTER,KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " , SCHOOL_KIND ");
            }
            stb.append(                   "FROM    SCH_NOTALL W2 ");
            stb.append(                   "WHERE   SUBCLASSCD = 'X9' ");
            stb.append(                 ") W4 ON W1.SCHREGNO = W4.SCHREGNO AND W1.SEMESTER = W4.SEMESTER AND W1.KIND = W4.KIND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(               " AND W1.SCHOOL_KIND = W4.SCHOOL_KIND ");
            }
            stb.append("WHERE   W1.SUBCLASSCD = 'X9G' ");
            if (param._schregno != null)
                stb.append(    "AND W1.SCHREGNO IN " + param._schregno + " ");

            stb.append("ORDER BY ATTENDNO, SCHREGNO, SEMESTER, KIND, SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            " , SCHOOL_KIND ");
            }

        } catch (Exception ex) {
            log.error("[KNJD171K]prestatementSubclass error!", ex);
        }
//log.debug(stb);
        return stb.toString();

    }//prestatementSubclass()の括り

    /**
     *  PrepareStatement作成 委員会、クラブ活動
     *  2005/05/27Modify クラブは異動指定日が属するデータを出力
     */
    String prestatementCommitAndClub(final Param param)
    {
        final StringBuffer stb = new StringBuffer();

        try {
            stb.append("WITH COMMITTEE_DAT AS(");
            stb.append(    "SELECT  SCHREGNO, COMMITTEE_FLG, MAX(SEQ) AS SEQ ");
            stb.append(    "FROM    SCHREG_COMMITTEE_HIST_DAT ");
            stb.append(    "WHERE   YEAR='" + param._year + "' AND ");
            stb.append(            "SCHREGNO = ? ");
            stb.append(    "GROUP BY SCHREGNO, COMMITTEE_FLG) ");
                        //05/05/27Modify
            stb.append(",CLUB_DAT AS(");
            stb.append(    "SELECT  SCHREGNO,CLUBCD ");
            stb.append(    "FROM    SCHREG_CLUB_HIST_DAT S1 ");
            stb.append(    "WHERE   SCHREGNO =  ? ");
            stb.append(        "AND SDATE = (SELECT MAX(SDATE) AS SDATE ");
            stb.append(                     "FROM    SCHREG_CLUB_HIST_DAT ");
            stb.append(                     "WHERE   SCHREGNO =  ? AND ");
            stb.append(                             "SDATE <= '" + param._date + "' ");
            stb.append(                     "GROUP BY SCHREGNO) ");
            stb.append(        "and '" + param._date + "' <= value(edate, '" + param._date + "')) ");  //05/05/27


            stb.append("SELECT  T3.COMMITTEE_FLG AS FLG, ");
            stb.append(        "T4.COMMITTEENAME AS NAME ");
            stb.append("FROM    SCHREG_COMMITTEE_HIST_DAT T3 ");
            stb.append(        "INNER JOIN COMMITTEE_MST T4 ON T4.COMMITTEECD = T3.COMMITTEECD AND ");
            stb.append(                                       "T4.COMMITTEE_FLG = T3.COMMITTEE_FLG ");
            stb.append("WHERE   T3.YEAR =  '" + param._year + "' AND ");
            stb.append(        "EXISTS(SELECT 'X' FROM COMMITTEE_DAT T1 WHERE T1.SEQ=T3.SEQ AND T1.SCHREGNO = T3.SCHREGNO AND ");
            stb.append(                                             "T1.COMMITTEE_FLG = T3.COMMITTEE_FLG) ");

                        //05/05/27Modify
            stb.append("UNION ");
            stb.append("SELECT  '3' AS FLG, CLUBNAME AS NAME ");
            stb.append("FROM    CLUB_DAT T1 ");
            stb.append(        "INNER JOIN CLUB_MST T2 ON T1.CLUBCD = T2.CLUBCD ");

        } catch (Exception ex) {
            log.error("error! ", ex);
        }
        return stb.toString();

    }

    /**
     *  get parameter doGet()パラメータ受け取り
     */
    private Param getParam(final HttpServletRequest request, final DB2UDB db2) throws Exception {
        log.fatal("$Revision: 75385 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }


    /** print設定 */
    private void setSvfInit(final HttpServletResponse response, final Vrw32alp svf) {

        try {
            outstrm = new PrintWriter (response.getOutputStream());
            if (printname != null) {
                response.setContentType("text/html");
            } else {
                response.setContentType("application/pdf");
            }
            int ret = svf.VrInit();                             //クラスの初期化

            if (printname != null) {
                ret = svf.VrSetPrinter("", printname);          //プリンタ名の設定
                if (ret < 0) log.info("printname ret = " + ret);
            } else {
                ret = svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
            }
        } catch (java.io.IOException ex) {
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
            if (!nonedata) outstrm.println("<H1>対象データはありません。</h1>");
            else            outstrm.println("<H1>印刷しました。</h1>");
            outstrm.println("</BODY>");
            outstrm.println("</HTML>");
        } else if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }
        int ret = svf.VrQuit();
        if (ret == 0)log.info("===> VrQuit():" + ret);
        outstrm.close();            //ストリームを閉じる
    }

    /**
     *  印刷位置を設定する
     *
     */
    private static class ReturnVal{

        int point;
        String field;

        /**
         *  学期・成績種別から出力行を設定する
         *
         */
        void getSvfOutPoint(final Param param, final int semes, final int kind)
        {
            if(Integer.parseInt(param._year) >= KIJUN_YEAR) {
                if (semes == 1)
                    point = ( kind == 1) ? 1 : 3;
                else if (semes == 2)
                    point = ( kind == 1) ? 5 : 7;
                else
                    point = 9;
            } else {
                if (semes == 1)
                    point = ( kind == 1) ? 1 : ( kind == 2) ? 3 : 5;
                else if (semes == 2)
                    point = ( kind == 1) ? 7 : ( kind == 2) ? 9 : 11;
                else
                    point = ( kind == 2) ? 13 : 15;
            }
        }

        /**
         *  教科コード等から出力フィールドを設定する
         *
         */
        void getSvfOutField(final String subclasscd, final int semes, final int kind) {
            if (semes <= 3) {
                field = ( subclasscd.equals("X5G")) ? "5TOTAL" :
                        ( subclasscd.equals("X5H")) ? "5AVERAGE" :
                        ( subclasscd.equals("X5R")) ? "5ORDER" :
                        ( subclasscd.equals("X9G")) ? "9TOTAL" :
                        ( subclasscd.equals("X9H")) ? "9AVERAGE" :
                        ( subclasscd.equals("X9R")) ? "9ORDER" :
                        "SUBCLASS" + Integer.parseInt(subclasscd.substring(0,2));
            }
        }
    }

    private static class Param {
        final String _year;
        final String _gakki;
        String _hrclass;
        String _nendo;
        String _staffname;
        String _schregno;
        final String _testkindcd;
        final String _date;
        final String _date2;
        String _coursecodename;
        final String _output;
        String _coursecode;
        final String _useCurriculumcd;
        private KNJSchoolMst _knjSchoolMst;

        static final String SSEMESTER = "1";
        static final String ESEMESTER = "9";

        final Map _attendParamMap = new HashMap();

        Param(final HttpServletRequest request, final DB2UDB db2) throws Exception {
            _year = request.getParameter("YEAR");                        //年度
            _gakki = request.getParameter("GAKKI");                       //1-3:学期
            _hrclass = request.getParameter("GRADE_HR_CLASS");              //学年・組
            _testkindcd = request.getParameter("TESTKINDCD");                  //中間:01,期末:99 04/11/03Add
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));    //異動基準日 04/11/30
            _date2 = KNJ_EditDate.h_format_JP(db2, request.getParameter("DATE2"));       //処理日
            _output = request.getParameter("OUTPUT");                     //1:個人指定  2:クラス指定
            _useCurriculumcd = request.getParameter("useCurriculumcd");            //教育課程

            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG");
        }
    }

}//クラスの括り
