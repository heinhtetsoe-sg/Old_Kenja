// kanji=漢字
/*
 * $Id: 1c82accf2d2603f4a63e5e34654f1a5c1d62668b $
 *
 * 作成日: 2008/01/11 14:20:26 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 * 合否判定原簿(KNJL340と同等)
 * @author m-yama
 * @version $Id: 1c82accf2d2603f4a63e5e34654f1a5c1d62668b $
 */
public class KNJL339K {


    private static final Log log = LogFactory.getLog(KNJL339K.class);

    Param _param;
    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws Exception
    {
        Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            //  ＳＶＦ作成処理
            boolean hasData = false; //該当データなしフラグ

            //SVF出力
            hasData = printMain(db2, svf);

            log.debug("hasData=" + hasData);

            //  該当データ無し
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            svf.VrQuit();
            db2.commit();
            db2.close();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void close(final DB2UDB db2, final Vrw32alp svf) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
        if (null != svf) {
            svf.VrQuit();
        }
    }


    public void setInfluenceName(DB2UDB db2, Vrw32alp svf) {
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + _param._specialReasonDiv + "' ");
            rs = ps.executeQuery();
            while (rs.next()) {
                String name2 = rs.getString("NAME2");
                svf.VrsOut("VIRUS", name2);
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            log.error(e);
        } finally {
            db2.commit();
        }
    }
    
    /**印刷処理メイン*/
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf)
    {
        if (_param.isJuniorHiSchool()) {
            svf.VrSetForm("KNJL340_1.frm", 1);
        } else {
            svf.VrSetForm("KNJL340_2.frm", 1);
        }

        boolean rtnFlg = false;

        try {
            //総ページ数
            int total_page[] = new int[2];//2
            getTotalPage(db2, svf, total_page);

            //明細データ
            if( printMeisai(db2, svf, total_page) ) rtnFlg = true;
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return rtnFlg;

    }


    /**試験区分毎の総ページ数*/
    private void getTotalPage(final DB2UDB db2, final Vrw32alp svf, final int total_page[])
    {
        try {
log.debug("TotalPage start!");
            db2.query(statementTotalPage());
            ResultSet rs = db2.getResultSet();
log.debug("TotalPage end!");

            int cnt = 0;
            while( rs.next() ){
                total_page[cnt] = rs.getInt("COUNT");
                cnt++;
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("getTotalPage read error!",ex);
        }

    }


    /**明細データ印刷処理*/
    private boolean printMeisai(final DB2UDB db2, final Vrw32alp svf, final int total_page[]) throws SQLException
    {
        boolean nonedata = false;
        try {

            final Map outPutData = getOutPutData(db2);

            int examno_limit = (_param.isJuniorHiSchool()) ? 49 : 24 ;

            int gyo = 0;
            int sex_cnt = 0;    //合計
            int sex1_cnt = 0;   //男
            int sex2_cnt = 0;   //女
            int page_cnt = 1;   //ページ数
            int page_arr = 0;   //総ページ数配列No
            String testdiv = "d";
            for (final Iterator iter = _param._examSort.iterator(); iter.hasNext();) {
                final String examno = (String) iter.next();
                final OutPutData data = (OutPutData) outPutData.get(examno);
                //１ページ印刷
                if (examno_limit < gyo || 
                    (!testdiv.equals("d") && !testdiv.equals(data._testdiv)) ) {
                    //合計印刷
                    if ((!testdiv.equals("d") && !testdiv.equals(data._testdiv)) ) {
                        printTotal(svf, sex1_cnt, sex2_cnt, sex_cnt);     //合計出力のメソッド
                    }
                    svf.VrEndPage();
                    page_cnt++;
                    gyo = 0;
                    if ((!testdiv.equals("d") && !testdiv.equals(data._testdiv)) ) {
                        sex_cnt = 0;sex1_cnt = 0;sex2_cnt = 0;page_cnt = 1;page_arr++;
                    }
                }
                //見出し
                printHeader(db2, svf, page_cnt, total_page, page_arr);
                //明細データ
                printScoreH(svf, data, gyo);
                //性別
                if( (data._sex).equals("1") ) sex1_cnt++;
                if( (data._sex).equals("2") ) sex2_cnt++;
                sex_cnt = sex1_cnt + sex2_cnt;

                testdiv = data._testdiv;
                gyo++;
                nonedata = true;
            }
            //最終ページ印刷
            if (nonedata) {
                printTotal(svf, sex1_cnt, sex2_cnt, sex_cnt);     //合計出力のメソッド
                svf.VrEndPage();
            }
        } finally {
            db2.commit();
        }
        return nonedata;

    }

    private Map getOutPutData(final DB2UDB db2) throws SQLException {
        db2.query(statementMeisaiH());

        ResultSet rsData = db2.getResultSet();

        final Map rtn = new HashMap();
        try {
            while (rsData.next()) {
                OutPutData data = new OutPutData(rsData.getString("TESTDIV"),
                        rsData.getString("EXAMNO"),
                        rsData.getString("NAME"),
                        rsData.getString("NAME_KANA"),
                        rsData.getString("SEX"),
                        rsData.getString("SEX_NAME"),
                        rsData.getString("DESIREDIV"),
                        rsData.getString("ABBV1_1"),
                        rsData.getString("ABBV1_2"),
                        rsData.getString("ABBV1_3"),
                        rsData.getString("ABBV1_4"),
                        rsData.getString("ABBV2"),
                        rsData.getString("ABBV3"),
                        rsData.getString("SHDIV"),
                        rsData.getString("SHDIV_NAME"),
                        rsData.getString("JUDGE1"),
                        rsData.getString("JUDGE2"),
                        rsData.getString("JUDGE3"),
                        rsData.getString("JUKEN_KUBUN"),
                        rsData.getString("TOTAL1"),
                        rsData.getString("TOTAL2"),
                        rsData.getString("SCORE1_1"),
                        rsData.getString("SCORE1_2"),
                        rsData.getString("SCORE1_3"),
                        rsData.getString("SCORE1_4"),
                        rsData.getString("SCORE1_5"),
                        rsData.getString("SCORE2_3"),
                        rsData.getString("SCORE2_5"),
                        rsData.getString("LOCATIONCD"),
                        rsData.getString("LOC_NAME"),
                        rsData.getString("FS_CD"),
                        rsData.getString("FS_NAME"),
                        rsData.getString("APPLICANT")
                        );
                rtn.put(rsData.getString("EXAMNO"), data);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsData);
        }
        return rtn;
    }

    private class OutPutData
    {
        private final String _testdiv;
        private final String _examno;
        private final String _name;
        private final String _name_kana;
        private final String _sex;
        private final String _sex_name;
        private final String _desirediv;
        private final String _abbv1_1;
        private final String _abbv1_2;
        private final String _abbv1_3;
        private final String _abbv1_4;
        private final String _abbv2;
        private final String _abbv3;
        private final String _shdiv;
        private final String _shdiv_name;
        private final String _judge1;
        private final String _judge2;
        private final String _judge3;
        private final String _juken_kubun;
        private final String _total1;
        private final String _total2;
        private final String _score1_1;
        private final String _score1_2;
        private final String _score1_3;
        private final String _score1_4;
        private final String _score1_5;
        private final String _score2_3;
        private final String _score2_5;
        private final String _locationcd;
        private final String _loc_name;
        private final String _fs_cd;
        private final String _fs_name;
        private final String _applicant;

        OutPutData(final String testdiv,
                final String examno,
                final String name,
                final String name_kana,
                final String sex,
                final String sex_name,
                final String desirediv,
                final String abbv1_1,
                final String abbv1_2,
                final String abbv1_3,
                final String abbv1_4,
                final String abbv2,
                final String abbv3,
                final String shdiv,
                final String shdiv_name,
                final String judge1,
                final String judge2,
                final String judge3,
                final String juken_kubun,
                final String total1,
                final String total2,
                final String score1_1,
                final String score1_2,
                final String score1_3,
                final String score1_4,
                final String score1_5,
                final String score2_3,
                final String score2_5,
                final String locationcd,
                final String loc_name,
                final String fs_cd,
                final String fs_name,
                final String applicant
        ) {
            _testdiv = testdiv;
            _examno = examno;
            _name = name;
            _name_kana = name_kana;
            _sex = sex;
            _sex_name = sex_name;
            _desirediv = desirediv;
            _abbv1_1 = abbv1_1;
            _abbv1_2 = abbv1_2;
            _abbv1_3 = abbv1_3;
            _abbv1_4 = abbv1_4;
            _abbv2 = abbv2;
            _abbv3 = abbv3;
            _shdiv = shdiv;
            _shdiv_name = shdiv_name;
            _judge1 = judge1;
            _judge2 = judge2;
            _judge3 = judge3;
            _juken_kubun = juken_kubun;
            _total1 = total1;
            _total2 = total2;
            _score1_1 = score1_1;
            _score1_2 = score1_2;
            _score1_3 = score1_3;
            _score1_4 = score1_4;
            _score1_5 = score1_5;
            _score2_3 = score2_3;
            _score2_5 = score2_5;
            _locationcd = locationcd;
            _loc_name = loc_name;
            _fs_cd = fs_cd;
            _fs_name = fs_name;
            _applicant = applicant;
        }
        
        public String toString() {
            return "受験番号 = " + _examno
                  + " 氏名 = " + _name
                  + " 性別 = " + _sex;
        }
    }

    /**ヘッダーデータをセット*/
    private void printHeader(
            final DB2UDB db2,
            final Vrw32alp svf,
            final int page_cnt,
            final int total_page[],
            final int page_arr
    ) {
        try {
            svf.VrsOut("NENDO"        , _param._gengou );
            svf.VrsOut("SCHOOLDIV"    , _param._title );
            svf.VrsOut("DATE"         , _param._date );

            svf.VrsOut("PAGE"         , String.valueOf(page_cnt) );
            svf.VrsOut("TOTAL_PAGE"   , String.valueOf(total_page[page_arr]) );
            setInfluenceName(db2, svf);
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }

    /**(高校)明細データをセット---2005.09.01*/
    private void printScoreH(final Vrw32alp svf, final OutPutData data, final int gyo)
    {
        String len2 = "0";
        String len3 = "0";
        try {
            len2 = (10 < (data._name).length()) ? "2" : "1" ;
            len3 = (10 < (data._fs_name).length()) ? "2" : "1" ;

            svf.VrsOutn("EXAMNO"      ,gyo+1  , data._examno);     //受験番号
            svf.VrsOutn("KANA"        ,gyo+1  , data._name_kana);  //ふりがな
            svf.VrsOutn("NAME"+len2   ,gyo+1  , data._name);       //氏名
            svf.VrsOutn("SEX"         ,gyo+1  , data._sex_name);   //性別

            if (data._shdiv != null && data._shdiv.equals("1")){
                svf.VrsOutn("SHDIV"       ,gyo+1  , "専" ); //専
            }else {
                svf.VrsOutn("SHDIV"       ,gyo+1  , "併" ); //併
            }
            svf.VrsOutn("DESIREDIV1_1",gyo+1  , data._abbv1_1);    //第１志望(理)
            svf.VrsOutn("DESIREDIV1_2",gyo+1  , data._abbv1_2);    //第１志望(国)
            svf.VrsOutn("DESIREDIV1_3",gyo+1  , data._abbv1_3);    //第１志望(特)
            svf.VrsOutn("DESIREDIV1_4",gyo+1  , data._abbv1_4);    //第１志望(進)
            svf.VrsOutn("DESIREDIV2"  ,gyo+1  , data._abbv2);      //第２志望
            svf.VrsOutn("DESIREDIV3"  ,gyo+1  , data._abbv3);      //第３志望
            svf.VrsOutn("JUDGEMENT1"  ,gyo+1  , data._judge1);     //判定結果(合)

            String judge2;
            if (data._juken_kubun.equals("2") && _param._cntF != null) {
                judge2 = data._judge2;
            } else {
                judge2 = data._judge3;
            }

            if (null == data._judge1 || data._judge1.equals("")){
                svf.VrsOutn("JUDGEMENT2"  ,gyo+1  , judge2 );     //判定結果(否)
            }

            if (!data._juken_kubun.equals("2")){ //中高一貫者以外
                if (null == data._judge1 || data._judge1.equals("")){
                    svf.VrsOutn("JUDGEMENT2"  ,gyo+1  , judge2 );     //判定結果(否)
                }
                svf.VrsOutn("TOTAL1"      ,gyo+1  , data._total1);     //成績Ａ(合計)
                svf.VrsOutn("POINT1_1"    ,gyo+1  , data._score1_1);   //成績Ａ(国語)
                svf.VrsOutn("POINT1_2"    ,gyo+1  , data._score1_2);   //成績Ａ(社会)
                svf.VrsOutn("POINT1_3"    ,gyo+1  , data._score1_3);   //成績Ａ(数学)
                svf.VrsOutn("POINT1_4"    ,gyo+1  , data._score1_4);   //成績Ａ(理科)
                svf.VrsOutn("POINT1_5"    ,gyo+1  , data._score1_5);   //成績Ａ(英語)
                svf.VrsOutn("TOTAL2"      ,gyo+1  , data._total2);     //成績Ｂ(合計)
                svf.VrsOutn("POINT2_3"    ,gyo+1  , data._score2_3);   //成績Ｂ(数学)
                svf.VrsOutn("POINT2_5"    ,gyo+1  , data._score2_5);   //成績Ｂ(英語)
                svf.VrsOutn("LOCATION"    ,gyo+1  , data._loc_name);   //市区町村
                svf.VrsOutn("FINSCHOOL"+len3  ,gyo+1  , data._fs_name);//出身中学校
            }
            if (data._juken_kubun.equals("1")) //一般受験者
                svf.VrsOutn("REMARK"  ,gyo+1  , data._applicant);  //備考
        } catch( Exception ex ) {
            log.warn("printScoreH read error!",ex);
        }

    }


    /**
     *  市町村をセット
     *
     * ※最後の文字で判断---2005.09.01
     * 市：大阪市             ⇒ 大阪市
     * 区：大阪市中央区       ⇒ 大阪市
     * 町：泉南郡岬町         ⇒ 泉南郡
     * 村：南河内郡千早赤阪村 ⇒ 南河内郡
     * その他：XXX            ⇒ XXX
     */
    private String setCityName(ResultSet rs)
    {
        String ret_val = "";
        try {
            if (rs.getString("ZIP_CITY") != null) {
                String city_nam = rs.getString("ZIP_CITY");
                String city_flg = rs.getString("ZIP_CITY_FLG");

                if (city_flg.equals("市")) {
                    ret_val = city_nam;

                } else if (city_flg.equals("区")) {
                    ret_val = (-1 < city_nam.indexOf("市")) ? city_nam.substring(0,city_nam.indexOf("市"))+"市" : city_nam;

                } else if (city_flg.equals("町") || city_flg.equals("村")) {
                    ret_val = (-1 < city_nam.indexOf("郡")) ? city_nam.substring(0,city_nam.indexOf("郡"))+"郡" : city_nam;

                } else {
                    ret_val = city_nam;
                }
            }
        } catch( Exception ex ) {
            log.warn("setCityName read error!",ex);
        }
        return ret_val;

    }


    /**合計をセット*/
    private void printTotal(final Vrw32alp svf, final int sex1_cnt, final int sex2_cnt, final int sex_cnt)
    {
        try {
            svf.VrsOut("TOTAL_MEMBER" , "男" + String.valueOf(sex1_cnt) + "名、" + 
                                              "女" + String.valueOf(sex2_cnt) + "名、" + 
                                              "合計" + String.valueOf(sex_cnt) + "名" );
        } catch( Exception ex ) {
            log.warn("printTotal read error!",ex);
        }

    }

    /**
     *  明細データを抽出(高校)
     *
     */
    private String statementMeisaiH()
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT TESTDIV,EXAMNO,NAME,NAME_KANA,SEX,DESIREDIV,A_TOTAL,B_TOTAL,SHDIV,FS_CD,APPLICANTDIV, ");//---2005.09.04
            stb.append("           JUDGEMENT,SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE,LOCATIONCD, ");//2005.10.27
            stb.append("           CASE WHEN EXAMNO < '5000' OR '6000' <= EXAMNO ");
            stb.append("                THEN '1' ");
            stb.append("                ELSE '2' ");
            stb.append("           END AS JUKEN_KUBUN ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '" + _param._year + "' AND ");
            if (!"9".equals(_param._specialReasonDiv)) {
                stb.append("           SPECIAL_REASON_DIV = '" + _param._specialReasonDiv + "' AND ");
            }
            stb.append("           EXAMNO IN " + _param._instate + " ");
            stb.append("    ) ");
            //志願者得点データ
            stb.append(",EXAM_SCORE AS ( ");
            stb.append("    SELECT EXAMNO,TESTSUBCLASSCD,A_SCORE,B_SCORE ");
            stb.append("    FROM   ENTEXAM_SCORE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("    ) ");
            //成績：（素点）
            stb.append(",SCORE AS ( ");
            stb.append("    SELECT EXAMNO, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '1' THEN A_SCORE ELSE NULL END) AS SCORE1_1, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '2' THEN A_SCORE ELSE NULL END) AS SCORE1_2, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '3' THEN A_SCORE ELSE NULL END) AS SCORE1_3, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '4' THEN A_SCORE ELSE NULL END) AS SCORE1_4, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '5' THEN A_SCORE ELSE NULL END) AS SCORE1_5, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '1' THEN B_SCORE ELSE NULL END) AS SCORE2_1, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '2' THEN B_SCORE ELSE NULL END) AS SCORE2_2, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '3' THEN B_SCORE ELSE NULL END) AS SCORE2_3, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '4' THEN B_SCORE ELSE NULL END) AS SCORE2_4, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '5' THEN B_SCORE ELSE NULL END) AS SCORE2_5 ");
            stb.append("    FROM   EXAM_SCORE ");
            stb.append("    GROUP BY EXAMNO ");
            stb.append("    ) ");
            //志望区分マスタ・受験コースマスタ
            stb.append(",EXAM_WISH AS ( ");
            stb.append("    SELECT W1.DESIREDIV, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '1' THEN W2.EXAMCOURSE_ABBV ELSE NULL END) AS ABBV1, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '2' THEN W2.EXAMCOURSE_ABBV ELSE NULL END) AS ABBV2, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '3' THEN W2.EXAMCOURSE_ABBV ELSE NULL END) AS ABBV3, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '1' THEN W2.EXAMCOURSE_MARK ELSE NULL END) AS MARK1, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '2' THEN W2.EXAMCOURSE_MARK ELSE NULL END) AS MARK2, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '3' THEN W2.EXAMCOURSE_MARK ELSE NULL END) AS MARK3 ");
            stb.append("    FROM   ENTEXAM_WISHDIV_MST W1, ENTEXAM_COURSE_MST W2 ");
            stb.append("    WHERE  W1.ENTEXAMYEAR = '" + _param._year + "' AND ");
            stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
            stb.append("           W1.COURSECD = W2.COURSECD AND ");
            stb.append("           W1.MAJORCD = W2.MAJORCD AND ");
            stb.append("           W1.EXAMCOURSECD = W2.EXAMCOURSECD ");
            stb.append("    GROUP BY W1.DESIREDIV ");
            stb.append("    ) ");
            //郵便番号マスタ
            stb.append(",ZIPCD AS ( ");
            stb.append("    SELECT NEW_ZIPCD,MAX(ZIPNO) AS ZIPNO_MAX ");
            stb.append("    FROM   ZIPCD_MST ");
            stb.append("    GROUP BY NEW_ZIPCD ");
            stb.append("    ) ");
            stb.append(",ZIPCD2 AS ( ");
            stb.append("    SELECT NEW_ZIPCD,CITY ");
            stb.append("    FROM   ZIPCD_MST ");
            stb.append("    WHERE  ZIPNO IN (SELECT ZIPNO_MAX FROM ZIPCD) ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.TESTDIV, ");
            stb.append("       T1.EXAMNO,T1.NAME,T1.NAME_KANA, ");
            stb.append("       VALUE(T1.SEX,'0') AS SEX,N1.ABBV1 AS SEX_NAME, ");
            stb.append("       T1.DESIREDIV, ");
            stb.append("       CASE WHEN T3.MARK1 = 'S' THEN T3.ABBV1 ELSE NULL END AS ABBV1_1, ");
            stb.append("       CASE WHEN T3.MARK1 = 'K' THEN T3.ABBV1 ELSE NULL END AS ABBV1_2, ");
            stb.append("       CASE WHEN T3.MARK1 = 'T' THEN T3.ABBV1 ELSE NULL END AS ABBV1_3, ");
            stb.append("       CASE WHEN T3.MARK1 = 'P' THEN T3.ABBV1 ELSE NULL END AS ABBV1_4, ");
            stb.append("       T3.ABBV2,T3.ABBV3, ");
            stb.append("       T1.SHDIV,N2.ABBV1 AS SHDIV_NAME, ");
            stb.append("       CASE WHEN (T1.JUDGEMENT > '0' AND T1.JUDGEMENT < '7') OR T1.JUDGEMENT = '9' THEN T4.EXAMCOURSE_ABBV ELSE NULL END AS JUDGE1, ");

            //附属でテストありの者の場合
            stb.append("       CASE WHEN (T1.JUDGEMENT > '0' AND T1.JUDGEMENT < '7') OR T1.JUDGEMENT = '9' THEN NULL ");
            stb.append("            WHEN T1.JUDGEMENT = '7' AND (T1.A_TOTAL IS NOT NULL OR T1.B_TOTAL IS NOT NULL) THEN '否' ");
            stb.append("            WHEN T1.JUDGEMENT = '7' AND T1.A_TOTAL IS NULL AND T1.B_TOTAL IS NULL THEN '欠' ");
            stb.append("            ELSE NULL END AS JUDGE2, ");//判定結果：否
            //以外の者の場合
            stb.append("       CASE WHEN (T1.JUDGEMENT > '0' AND T1.JUDGEMENT < '7') OR T1.JUDGEMENT = '9' THEN NULL ");
            stb.append("            WHEN T1.JUDGEMENT = '7' THEN '否' ");
            stb.append("            WHEN T1.A_TOTAL IS NULL AND T1.B_TOTAL IS NULL THEN '欠' ");
            stb.append("            ELSE NULL END AS JUDGE3, ");//判定結果：否

            stb.append("        T1.JUKEN_KUBUN, ");

            stb.append("       T1.A_TOTAL AS TOTAL1,T1.B_TOTAL AS TOTAL2, ");
            stb.append("       T2.SCORE1_1,T2.SCORE1_2,T2.SCORE1_3,T2.SCORE1_4,T2.SCORE1_5, ");
            stb.append("       CASE WHEN T3.MARK1 = 'S' THEN T2.SCORE2_3 ELSE NULL END AS SCORE2_3, ");
            stb.append("       CASE WHEN T3.MARK1 = 'K' THEN T2.SCORE2_5 ELSE NULL END AS SCORE2_5, ");
            stb.append("       T1.LOCATIONCD, N4.NAME1 AS LOC_NAME, ");//市区町村
            stb.append("       T1.FS_CD,VALUE(T5.FINSCHOOL_NAME,'') AS FS_NAME ");
            stb.append("       ,CASE WHEN T1.APPLICANTDIV = '3' THEN N3.NAME1 ELSE NULL END AS APPLICANT ");
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("       LEFT JOIN SCORE T2 ON T2.EXAMNO=T1.EXAMNO ");
            stb.append("       LEFT JOIN EXAM_WISH T3 ON T3.DESIREDIV=T1.DESIREDIV ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST T4 ON T4.ENTEXAMYEAR='" + _param._year + "' AND  ");
            stb.append("                                    T4.COURSECD||T4.MAJORCD||T4.EXAMCOURSECD = T1.SUC_COURSE ");
            stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='Z002' AND N1.NAMECD2=T1.SEX ");
            stb.append("       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L006' AND N2.NAMECD2=T1.SHDIV ");
            stb.append("       LEFT JOIN NAME_MST N3 ON N3.NAMECD1='L005' AND N3.NAMECD2=T1.APPLICANTDIV ");
            stb.append("       LEFT JOIN NAME_MST N4 ON N4.NAMECD1='L007' AND N4.NAMECD2=T1.LOCATIONCD ");
            stb.append("       LEFT JOIN FINSCHOOL_MST T5 ON T5.FINSCHOOLCD=T1.FS_CD ");
            stb.append("ORDER BY T1.TESTDIV,T1.EXAMNO ");
        } catch( Exception e ){
            log.warn("statementMeisaiH error!",e);
        }
        return stb.toString();

    }


    /**
     *  試験区分毎の総ページ数を取得(共通)---2005.09.01
     *
     */
    private String statementTotalPage()
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT TESTDIV,EXAMNO ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '" + _param._year + "' ");
            if (!"9".equals(_param._specialReasonDiv)) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + _param._specialReasonDiv + "' ");
            }
            stb.append("           AND EXAMNO IN " + _param._instate + " ");
            //中学
            if (_param.isJuniorHiSchool()) {
                stb.append("       AND TESTDIV = '" + _param._testdiv + "'");
            }
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.TESTDIV, ");
            if (_param.isJuniorHiSchool()) {
                stb.append("   CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END AS COUNT ");
            } else {
                stb.append("   CASE WHEN 0 < MOD(COUNT(*),25) THEN COUNT(*)/25 + 1 ELSE COUNT(*)/25 END AS COUNT ");
            }
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("GROUP BY T1.TESTDIV ");
            stb.append("ORDER BY T1.TESTDIV ");
        } catch( Exception e ){
            log.warn("statementTotalPage error!",e);
        }
        return stb.toString();

    }


    /**
     *  附属で１人でも得点データがある場合（テストあり：試験区分毎）
     *
     */
    private String statementFuzokuScore(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT TESTDIV,COUNT(*) AS CNT_F ");
            stb.append("FROM   ENTEXAM_SCORE_DAT ");
            stb.append("WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            //中学
            if (param[5].equals("1")) {
                stb.append("   TESTDIV = '"+param[1]+"' AND ");
                stb.append("   EXAMNO BETWEEN '3000' AND '3999' AND ");
            }
            //高校
            if (param[5].equals("2")) {
                stb.append("   EXAMNO BETWEEN '5000' AND '5999' AND ");
            }
            stb.append("       A_SCORE IS NOT NULL ");
            stb.append("GROUP BY TESTDIV ");
        } catch( Exception e ){
            log.warn("statementFuzokuScore error!",e);
        }
        return stb.toString();

    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _testdiv;
        private final String _jhFlg;
        private final String _title;
        private final String _gengou;
        private final String _date;
        private final List _examSort;
        private final String _instate;
        private String _cntF;
        private String _specialReasonDiv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _testdiv = request.getParameter("TESTDIV");
            _jhFlg = request.getParameter("JHFLG");
            _title = isJuniorHiSchool() ? "中学校" : "高等学校";
            _gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";

            _examSort = new ArrayList();
            String[] examin = request.getParameterValues("DATA_SELECTED");
            String inSep = "";
            StringBuffer inState  = new StringBuffer();
            inState.append(" (");
            for (int exacnt = 0; exacnt < examin.length; exacnt++) {
                inState.append(inSep + "'" + examin[exacnt] + "'");
                inSep = ",";
                _examSort.add(examin[exacnt]);
            }
            inState.append(") ");
            _instate = inState.toString();

            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            _date = KNJ_EditDate.h_format_JP(returnval.val3);
            getinfo = null;
            returnval = null;
            
            _specialReasonDiv = request.getParameter("SPECIAL_REASON_DIV");

            db2.query(statementFuzokuScore(_year, _testdiv, _jhFlg));
            ResultSet rs = db2.getResultSet();
            while( rs.next() ){
                _cntF = rs.getString("CNT_F");
            }
            rs.close();
            db2.commit();
        }

        private boolean isJuniorHiSchool() {
            if (_jhFlg.equals("1")) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     *  附属で１人でも得点データがある場合（テストあり：試験区分毎）
     *
     */
    private String statementFuzokuScore(final String year, final String testDiv, final String jhFlg)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT TESTDIV,COUNT(*) AS CNT_F ");
            stb.append("FROM   ENTEXAM_SCORE_DAT ");
            stb.append("WHERE  ENTEXAMYEAR = '" + year + "' AND ");
            //中学
            if (jhFlg.equals("1")) {
                stb.append("   TESTDIV = '" + testDiv + "' AND ");
                stb.append("   EXAMNO BETWEEN '3000' AND '3999' AND ");
            }
            //高校
            if (jhFlg.equals("2")) {
                stb.append("   EXAMNO BETWEEN '5000' AND '5999' AND ");
            }
            stb.append("       A_SCORE IS NOT NULL ");
            stb.append("GROUP BY TESTDIV ");
        } catch( Exception e ){
            log.warn("statementFuzokuScore error!",e);
        }
        return stb.toString();

    }

}//クラスの括り
