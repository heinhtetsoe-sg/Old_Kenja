// kanji=漢字
/*
 * $Id: 915c7a0cdcc3c11bb6311e80fa3ee46b6d16221b $
 *
 * 作成日: 2020/01/30 17:30:30 - JST
 * 作成者: yogi
 *
 * Copyright(C) 2004-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 定期試験度数分布表
 * @author m-yama
 * @version $Id: 915c7a0cdcc3c11bb6311e80fa3ee46b6d16221b $
 */
public class KNJD614C {

    private static final Log log = LogFactory.getLog(KNJD614C.class);

    private static final String FORM_FILE = "KNJD614C.frm";

    private static final String SEMEALL = "9";
    Param _param;

    private boolean _hasData = false;

    /**
     * KNJD.classから呼ばれる処理。
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
        final HttpServletRequest request,
        final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();    //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        try {
            init(response, svf);

            // DB接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);  // パラメータ作成

            // 印字メイン
            _hasData = false;   // 該当データ無しフラグ
            printMain(db2, svf);
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            close(svf, db2);
        }
    }

    private void init(final HttpServletResponse response, final Vrw32alp svf) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void close(final Vrw32alp svf, final DB2UDB db2) {
        if (_param._psMap.size() > 0) {
            for (Iterator ite = _param._psMap.values().iterator();ite.hasNext();) {
                final PreparedStatement psctl = (PreparedStatement)ite.next();
                DbUtils.closeQuietly(psctl);
            }
        }
        if (null != svf) {
            svf.VrQuit();
        }
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** 印刷処理メイン */
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws Exception {

        boolean rtnflg = false;

        // 出力データ取得
        final Map schregData = getSchregData(db2);
        if (schregData == null) {
        	return false;
        }
        final Map ygMap = new LinkedMap();
        getDetailData(db2, schregData, ygMap);
        getResultsData(db2, schregData);
        for (Iterator iti = ygMap.keySet().iterator();iti.hasNext();) {
            final String yearg = (String)iti.next();
            final String ygrade = (String)ygMap.get(yearg);
            getAttendSubclass(db2, yearg, ygrade, schregData);
        }

        for (Iterator ite = schregData.keySet().iterator();ite.hasNext();) {
            final String schregno = (String)ite.next();
            final SchregData schInfo = (SchregData)schregData.get(schregno);
            svf.VrSetForm(FORM_FILE, 4);

            //レコード以外を出力
            ////年組番
            final String attNoStr = String.valueOf(Integer.parseInt(schInfo._attendno));
            svf.VrsOut("HR_NAME", schInfo._hr_Name + (attNoStr.length() > 2 ? "" : attNoStr.length() == 1 ? " " : "  ") + String.valueOf(Integer.parseInt(schInfo._attendno)) + "番");
            ////氏名
            final int nlen = KNJ_EditEdit.getMS932ByteLength(schInfo._name);
            final String nfield = nlen > 30 ? "2" : "1";
            svf.VrsOut("NAME" + nfield, schInfo._name);
            ////合計列
            CalcData totalSumInfo = new CalcData();
            for (int cnt = 1;cnt <= 3;cnt++) {
                CalcData totalInfo = schInfo.getCalcData("0"+cnt);
                svf.VrsOut("TOTAL_SUBCLASS"+cnt, String.valueOf(totalInfo._subclsCnt));
                svf.VrsOut("TOTAL_DIV"+cnt, String.valueOf(totalInfo._hyoteiKei));
                svf.VrsOut("AVE_DIV"+cnt, totalInfo._hyoteiAvg.toString());
                svf.VrsOut("GET_CREDIT"+cnt, String.valueOf(totalInfo._creditCnt));
                if (schInfo._resultsInfo.containsKey("0"+cnt)) {
                    ResultsData rd = (ResultsData)schInfo._resultsInfo.get("0"+cnt);
                    svf.VrsOut("RANK" + cnt + "_1", cnt == 1 ? rd._grade_Deviation_Rank : rd._course_Deviation_Rank);
                    svf.VrsOut("RANK" + cnt + "_2", rd._cntAll);
                    if (cnt > 1) {
                        svf.VrsOut("RANK" + cnt + "_3", rd._coursecodeabbv1);
                    }
                    ////出席情報
                    Attendance att = (Attendance)getAttendSemes(db2, rd._year, schregno);
                    if (att != null) {
                        svf.VrsOutn("LESSON",cnt, String.valueOf(att._lesson));
                        svf.VrsOutn("MUST",cnt, String.valueOf(att._mLesson));
                        svf.VrsOutn("MOURNING",cnt, String.valueOf(att._mourning + att._suspend));
                        svf.VrsOutn("ATTEND",cnt, String.valueOf(att._sick));
                        svf.VrsOutn("PRESENT",cnt, String.valueOf(att._present));
                        svf.VrsOutn("LATE",cnt, String.valueOf(att._late));
                        svf.VrsOutn("EARLY",cnt, String.valueOf(att._early));
                    }
                }
                totalSumInfo.add(totalInfo);
            }
            svf.VrsOut("TOTAL_GET_CREDIT", String.valueOf(totalSumInfo._creditCnt));
            svf.VrsOut("TOTAL_AVE_DIV", totalSumInfo.calc());

            //レコードを出力
            if (schInfo._clsSumInfo != null) {
                for (Iterator its = schInfo._clsSumInfo.keySet().iterator();its.hasNext();) {
                    final String classCd = (String)its.next();
                    if ("99".equals(classCd)) continue;
                    final CalcData totalObj = (CalcData)schInfo._clsSumInfo.get(classCd);
                    final String colFld;
                    if ("1".equals(totalObj._electDiv)) {
                        //選択科目
                        colFld = "2";
                    } else {
                        //必須科目
                        colFld = "1";
                    }

                    List lObj1 = null;
                    int l1Cnt = 0;
                    if (schInfo._subclsInfo.containsKey("01")) {
                        Map swkMap = (Map)schInfo._subclsInfo.get("01");
                        if (swkMap.containsKey(classCd)) {
                            lObj1 = (List)swkMap.get(classCd);
                            l1Cnt = lObj1.size();
                        }
                    }
                    List lObj2 = null;
                    int l2Cnt = 0;
                    if (schInfo._subclsInfo.containsKey("02")) {
                        Map swkMap = (Map)schInfo._subclsInfo.get("02");
                        if (swkMap.containsKey(classCd)) {
                            lObj2 = (List)swkMap.get(classCd);
                            l2Cnt = lObj2.size();
                        }
                    }
                    List lObj3 = null;
                    int l3Cnt = 0;
                    if (schInfo._subclsInfo.containsKey("03")) {
                        Map swkMap = (Map)schInfo._subclsInfo.get("03");
                        if (swkMap.containsKey(classCd)) {
                            lObj3 = (List)swkMap.get(classCd);
                            l3Cnt = lObj3.size();
                        }
                    }

                    int maxcol = Math.max(l3Cnt, Math.max(l1Cnt, l2Cnt));

                    for (int cnt = 0;cnt < maxcol;cnt++) {
                        if (cnt < l1Cnt) {
                            printDetail(svf, "1", lObj1, cnt, colFld);
                        }
                        if (cnt < l2Cnt) {
                            printDetail(svf, "2", lObj2, cnt, colFld);
                        }
                        if (cnt < l3Cnt) {
                            printDetail(svf, "3", lObj3, cnt, colFld);
                        }
                           if ("1".equals(totalObj._electDiv)) {
                               //選択科目
                               svf.VrsOut("CLASS_NAME2_1", totalObj._className);
                           } else {
                               //必須科目
                               svf.VrsOut("CLASS_NAME1", totalObj._className);
                           }
                           svf.VrsOut("SUBCLASS_AVE_DIV" + colFld, totalObj.calc());
                        svf.VrEndRecord();
                    }
                    //データが無くても、教科タイトルだけは出力。
                    if (maxcol == 0) {
                        if ("1".equals(totalObj._electDiv)) {
                            //選択科目
                            svf.VrsOut("CLASS_NAME2_1", totalObj._className);
                        } else {
                            //必須科目
                            svf.VrsOut("CLASS_NAME1", totalObj._className);
                        }
                        svf.VrEndRecord();
                    }
                }
                _hasData = true;
            }
            svf.VrEndPage();
        }


        return rtnflg;
    }

    private void printDetail(final Vrw32alp svf, final String gradeCd, final List lObj, final int cnt, final String colFld) {
        DetailData dd = (DetailData)lObj.get(cnt);
        if (dd == null) return ;
        //科目名称
        final int subclassFieldVal = dd._subclassabbv == null || dd._subclassabbv.length() <= 5 ? 1 : (dd._subclassabbv.length() <= 7 ? 2 : 3); //フィールド判定用
        if (subclassFieldVal == 1) {
        	svf.VrsOut("SUBCLASS_NAME" + colFld + "_" + gradeCd + "_1", dd._subclassabbv);
        } else if (subclassFieldVal == 2) {
        	svf.VrsOut("SUBCLASS_NAME" + colFld + "_" + gradeCd + "_2", dd._subclassabbv);
        } else {
        	final String[] token = get_token(dd._subclassabbv, 7, 2);
        	svf.VrsOut("SUBCLASS_NAME" + colFld + "_" + gradeCd + "_3_1", token[0]);
        	svf.VrsOut("SUBCLASS_NAME" + colFld + "_" + gradeCd + "_3_2", token[1]);
        }

        //成績
        svf.VrsOut("SCORE"+ colFld + "_" + gradeCd, dd._score1);
        //評定
        svf.VrsOut("DIV"+ colFld + "_" + gradeCd, dd._score2);
        //欠課時数
        svf.VrsOut("KEKKA"+ colFld + "_" + gradeCd, dd._kekka);
        //修得単位
        svf.VrsOut("CREDIT"+ colFld + "_" + gradeCd, dd._credits);
    }
    //縦文字列分割用(半角全角どちらも文字数でカウント)
    private String[] get_token(String strx,int f_len,int f_cnt) {

        if( strx==null || strx.length()==0 )return null;

        String stoken[] = new String[f_cnt];        //分割後の文字列の配列
        int strLen = 0;                              //文字数カウント
        int slen = 0;                               //文字列のバイト数カウント
        int s_sta = 0;                              //文字列の開始位置
        int ib = 0;
        for( int s_cur=0 ; s_cur<strx.length() && ib<f_cnt ; s_cur++ ){
            //改行マークチェック
            if( strx.charAt(s_cur)=='\r' )continue;
            if( strx.charAt(s_cur)=='\n' ){
                stoken[ib++] = strx.substring(s_sta,s_cur);
                slen = 0;
                s_sta = s_cur+1;
            } else{
            //文字数チェック
                try{
                	strLen = (strx.substring(s_cur,s_cur+1)).length();
                } catch( Exception e ){
                    System.out.println("[KNJ_EditEdit]exam_out error!"+e );
                }
                slen+=strLen;
                if( slen>f_len ){
                    stoken[ib++] = strx.substring(s_sta,s_cur);     // 04/09/22Modify
                    slen = strLen;
                    s_sta = s_cur;
                }
            }
        }
        if( slen>0 && ib<f_cnt )stoken[ib] = strx.substring(s_sta);

        return stoken;

    }//String get_token()の括り

    private Map getSchregData(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        final String sql = getSchregDataSql();
        log.debug("getSchregData sql:"+sql);
        if ("".equals(sql)) {
        	return null;
        }
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String grade_Name1 = rs.getString("GRADE_NAME1");
                final String hr_Class = rs.getString("HR_CLASS");
                final String hr_Name = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                SchregData addwk = new SchregData(schregno, grade, grade_Name1, hr_Class, hr_Name, attendno, name);
                retMap.put(schregno, addwk);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getSchregDataSql() {
        final StringBuffer stb = new StringBuffer();
        final Map semeWk = _param.getSpecifyYearSemes(_param._year);
        if (semeWk == null) {
        	log.warn("NOT EXISTED NOW YEAR's INFO in SEMESTER_MST. ");
        	return "";
        }
        final String sDate = (String)semeWk.get("SDATE");
        final String eDate = _param._edate.compareTo((String)semeWk.get("EDATE")) > 0 ? (String)semeWk.get("EDATE") : _param._edate;
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.SCHREGNO, T1.GRADE, T4.GRADE_NAME1, T1.HR_CLASS, TH.HR_NAME, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, T3.NAME ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_REGD_HDAT TH ");
        stb.append("     ON TH.YEAR = T1.YEAR ");
        stb.append("    AND TH.SEMESTER = T1.SEMESTER ");
        stb.append("    AND TH.GRADE = T1.GRADE ");
        stb.append("    AND TH.HR_CLASS = T1.HR_CLASS ");
        stb.append("   LEFT JOIN SEMESTER_MST T2  ");
        stb.append("     ON T1.YEAR = T2.YEAR  ");
        stb.append("    AND T1.SEMESTER = T2.SEMESTER  ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T3 ");
        stb.append("     ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT T4 ");
        stb.append("     ON T4.YEAR = T1.YEAR ");
        stb.append("    AND T4.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("   AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._hrClasses));
        stb.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1  ");
        stb.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("                    AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + eDate + "' THEN T2.EDATE ELSE '" + sDate + "' END)  ");
        stb.append("                    OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + eDate + "' THEN T2.EDATE ELSE '" + sDate + "' END)) )  ");
        stb.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1  ");
        stb.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("                    AND S1.TRANSFERCD IN ('1','2') ");
        stb.append("                    AND CASE WHEN T2.EDATE < '" + sDate + "' THEN T2.EDATE ELSE '" + sDate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)  ");
        stb.append(" ORDER BY ");
        stb.append("   T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        return stb.toString();
    }

    private void getDetailData(final DB2UDB db2, final Map schregData, final Map ygMap) {
        final String sql = getDetailDataSql();
        log.debug("getDetailData sql:"+sql);
        ResultSet rs = null;
        PreparedStatement ps = null;
        SchregData schObj = null;
        Map addWkMap = null;
        List subWkLst = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String year = rs.getString("YEAR");
                final String grade = rs.getString("GRADE");
                final String gradeCd = rs.getString("GRADE_CD");
                final String schregno = rs.getString("SCHREGNO");
                final String classcd = rs.getString("CLASSCD");
                final String electDiv = rs.getString("ELECTDIV");
                final String classname = rs.getString("CLASSNAME");
                final String classabbv = rs.getString("CLASSABBV");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String subclassabbv = rs.getString("SUBCLASSABBV");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String score1 = rs.getString("SCORE1");
                final String score2 = rs.getString("SCORE2");
                final String credits = rs.getString("CREDITS");
                DetailData addwk = new DetailData(year, grade, gradeCd, schregno, classcd, electDiv, classname, classabbv, subclassname, subclassabbv, subclasscd, score1, score2, credits);

                if (schregData.containsKey(schregno)) {
                    //科目成績をMap登録
                    schObj = (SchregData)schregData.get(schregno);
                    if (schObj._subclsInfo == null) {
                        schObj._subclsInfo = new LinkedMap();
                    }
                    if (!schObj._subclsInfo.containsKey(gradeCd)) {
                        addWkMap = new LinkedMap();
                        schObj._subclsInfo.put(gradeCd, addWkMap);
                    } else {
                        addWkMap = (Map)schObj._subclsInfo.get(gradeCd);
                    }
                    if (!addWkMap.containsKey(classcd)) {
                        subWkLst = new ArrayList();
                        addWkMap.put(classcd, subWkLst);
                    } else {
                        subWkLst = (List)addWkMap.get(classcd);
                    }
                    subWkLst.add(addwk);

                    //教科コードマップを作製
                    if (schObj._clsSumInfo == null) {
                        schObj._clsSumInfo = new LinkedMap();
                    }
                    if (!schObj._clsSumInfo.containsKey(classcd)) {
                        CalcData calWk = new CalcData();
                        schObj._clsSumInfo.put(classcd, calWk);
                    }
                    CalcData calWk = (CalcData)schObj._clsSumInfo.get(classcd);
                    if (score2 != null) {
                        calWk._subclsCnt++;
                        calWk._hyoteiKei += Integer.parseInt(score2);
                    }
                    if (!"".equals(classabbv)) {
                        calWk._electDiv = electDiv;
                        calWk._className = classabbv;
                    }
                    //年度学年対応マップを登録
                    if (!ygMap.containsKey(year)) {
                        ygMap.put(year, gradeCd);
                    }
                }
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    private String getDetailDataSql() {
        final StringBuffer stb = new StringBuffer();
        // MAXSEMES_Y -> MAXGRADE_SCHREG -> PASTMIX_SCHREG ┬ メイン
        //                                  IGNORE_SUBCLASS┘
        stb.append("  WITH MAXSEMES_Y AS ( ");
        stb.append("    SELECT ");
        stb.append("      T3.YEAR ");
        stb.append("      , MAX(T3.SEMESTER) AS SEMESTER ");
        stb.append("    FROM ");
        stb.append("      SEMESTER_MST T3 ");
        stb.append("    WHERE ");
        stb.append("      T3.YEAR <= '" + _param._year + "' ");
        stb.append("      AND T3.SEMESTER <> '9' ");
        stb.append("    GROUP BY ");
        stb.append("      T3.YEAR ");
        stb.append("  ) ");
        stb.append("  , MAXGRADE_SCHREG AS ( ");
        stb.append("    SELECT ");
        stb.append("      MAX(S1.YEAR) AS YEAR ");
        stb.append("      , S1.SEMESTER ");
        stb.append("      , S1.GRADE ");
        stb.append("      , S1.SCHREGNO ");
        stb.append("    FROM ");
        stb.append("      SCHREG_REGD_DAT S1 ");
        stb.append("      INNER JOIN MAXSEMES_Y S2 ");
        stb.append("        ON S2.YEAR = S1.YEAR ");
        stb.append("        AND S2.SEMESTER = S1.SEMESTER ");
        stb.append("    GROUP BY ");
        stb.append("      S1.GRADE ");
        stb.append("      , S1.SCHREGNO ");
        stb.append("      , S1.SEMESTER ");
        stb.append("  ) ");
        stb.append("  , CURRENT_SCH AS ( ");
        stb.append("    SELECT ");
        stb.append("      T1.YEAR ");
        stb.append("      , T1.SCHREGNO ");
        stb.append("      , T1.GRADE ");
        stb.append("      , T1.COURSECD ");
        stb.append("      , T1.MAJORCD ");
        stb.append("      , T1.COURSECODE ");
        stb.append("    FROM ");
        stb.append("      SCHREG_REGD_DAT T1 ");
        stb.append("    WHERE ");
        stb.append("      T1.YEAR = '" + _param._year + "' ");
        stb.append("      AND T1.SEMESTER = '1' ");
        stb.append("      AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._hrClasses));
        stb.append("  ) ");
        stb.append("  , PAST_SCH AS ( ");
        stb.append("    SELECT ");
        stb.append("      T1.YEAR ");
        stb.append("      , T1.SCHREGNO ");
        stb.append("      , T1.GRADE ");
        stb.append("      , T1.COURSECD ");
        stb.append("      , T1.MAJORCD ");
        stb.append("      , T1.COURSECODE ");
        stb.append("    FROM ");
        stb.append("      SCHREG_REGD_DAT T1 ");
        stb.append("      INNER JOIN CURRENT_SCH T2 ");
        stb.append("        ON T2.YEAR > T1.YEAR ");
        stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("      INNER JOIN MAXGRADE_SCHREG T3 ");
        stb.append("        ON T3.YEAR = T1.YEAR ");
        stb.append("        AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("        AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("      INNER JOIN SCHREG_REGD_GDAT T4 ");
        stb.append("        ON T4.YEAR = T1.YEAR ");
        stb.append("        AND T4.GRADE = T1.GRADE ");
        stb.append("        AND T4.SCHOOL_KIND = 'H' ");
        stb.append("  ) , PASTMIX_SCHREG AS ( ");
        stb.append("  SELECT ");
        stb.append("    T1.* ");
        stb.append("  FROM ");
        stb.append("    CURRENT_SCH T1 ");
        stb.append("  UNION ALL ");
        stb.append("  SELECT ");
        stb.append("    T2.* ");
        stb.append("  FROM ");
        stb.append("    PAST_SCH T2 ");
        stb.append(" ), IGNORE_SUBCLASS AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD AS SUBCLASSCD ");
        stb.append(" FROM ");
        stb.append("   SUBCLASS_RANK_REPLACE_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append(" ) ");
        stb.append(" select ");
        stb.append("  T1.YEAR, ");
        stb.append("  T1.GRADE, ");
        stb.append("  T7.GRADE_CD, ");
        stb.append("  T1.SCHREGNO, ");
        stb.append("  T2.CLASSCD, ");
        stb.append("  T5.ELECTDIV, ");
        stb.append("  T5.CLASSNAME, ");
        stb.append("  T5.CLASSABBV, ");
        stb.append("  T6.SUBCLASSNAME, ");
        stb.append("  T6.SUBCLASSABBV, ");
        stb.append("  T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("  T2.SCORE AS SCORE1, ");
        stb.append("  CASE WHEN (T2.SCHOOL_KIND = 'H' AND T7.GRADE_CD = '03' AND T3.SCORE = 1)THEN 2 ELSE T3.SCORE END AS SCORE2, ");
        stb.append("  T4.CREDITS ");
        stb.append(" FROM ");
        stb.append("  PASTMIX_SCHREG T1 ");
        stb.append("  LEFT JOIN RECORD_RANK_SDIV_DAT T2 ");
        stb.append("    ON T2.YEAR = T1.YEAR ");
        stb.append("   AND ((T2.SEMESTER = '9' AND T2.YEAR < '" + _param._year + "') OR (T2.SEMESTER = '1' AND T2.YEAR = '" + _param._year + "')) ");
        stb.append("   AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '990008' ");
        stb.append("  LEFT JOIN RECORD_RANK_SDIV_DAT T3 ");
        stb.append("    ON T3.YEAR = T2.YEAR ");
        stb.append("   AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("   AND T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '990009' ");
        stb.append("   AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
        stb.append("   AND T3.SCHREGNO = T2.SCHREGNO ");
        stb.append("  LEFT JOIN CREDIT_MST T4 ");
        stb.append("    ON T4.YEAR = T2.YEAR ");
        stb.append("   AND T4.COURSECD = T1.COURSECD ");
        stb.append("   AND T4.MAJORCD = T1.MAJORCD ");
        stb.append("   AND T4.COURSECODE = T1.COURSECODE ");
        stb.append("   AND T4.GRADE = T1.GRADE ");
        stb.append("   AND T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
        stb.append("  LEFT JOIN CLASS_MST T5 ");
        stb.append("    ON T5.CLASSCD = T2.CLASSCD ");
        stb.append("   AND T5.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("  LEFT JOIN SUBCLASS_MST T6 ");
        stb.append("    ON T6.CLASSCD = T2.CLASSCD ");
        stb.append("   AND T6.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("   AND T6.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("   AND T6.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("  LEFT JOIN SCHREG_REGD_GDAT T7 ");
        stb.append("    ON T7.YEAR = T1.YEAR ");
        stb.append("   AND T7.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("  T2.SUBCLASSCD NOT IN ('333333', '555555', '99999A', '99999B') ");
        stb.append("  AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD NOT IN (SELECT SUBCLASSCD FROM IGNORE_SUBCLASS) ");
        stb.append(" ORDER BY ");
        stb.append("   YEAR,GRADE,SCHREGNO,T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
        return stb.toString();
    }

    private Map getResultsData(final DB2UDB db2, final Map schregData) {
        final Map retMap = new LinkedMap();
        final String sql = getResultsDataSql();
        log.debug("getResultsData sql:"+sql);
        ResultSet rs = null;
        PreparedStatement ps = null;
        SchregData schObj = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String year = rs.getString("YEAR");
                final String grade = rs.getString("GRADE");
                final String school_Kind = rs.getString("SCHOOL_KIND");
                final String grade_Cd = rs.getString("GRADE_CD");
                final String schregno = rs.getString("SCHREGNO");
                final String cntAll = rs.getString("CNTALL");
                final String grade_Deviation_Rank = rs.getString("GRADE_DEVIATION_RANK");
                final String course_Deviation_Rank = rs.getString("COURSE_DEVIATION_RANK");
                final String coursecodeabbv1 = rs.getString("COURSECODEABBV1");
                ResultsData addwk = new ResultsData(year, grade, school_Kind, grade_Cd, schregno, grade_Deviation_Rank, course_Deviation_Rank, coursecodeabbv1, cntAll);
                final String mapFstKey = schregno;
                if (schregData.containsKey(mapFstKey)) {
                    schObj = (SchregData)schregData.get(mapFstKey);
                    if (schObj._resultsInfo == null) {
                        schObj._resultsInfo = new LinkedMap();
                    }
                    schObj._resultsInfo.put(grade_Cd, addwk);
                }
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }
    private String getResultsDataSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAXSEMES_Y AS ( ");
        stb.append(" SELECT ");
        stb.append("  T3.YEAR, ");
        stb.append("  MAX(T3.SEMESTER) AS SEMESTER ");
        stb.append(" FROM ");
        stb.append("  SEMESTER_MST T3 ");
        stb.append(" WHERE ");
        stb.append("  T3.YEAR <= '" + _param._year + "' ");
        stb.append("  AND T3.SEMESTER <> '9' ");
        stb.append(" GROUP BY ");
        stb.append("  T3.YEAR ");
        stb.append(" ), MAXGRADE_SCHREG AS ( ");
        stb.append(" SELECT ");
        stb.append("   MAX(S1.YEAR) AS YEAR, ");
        stb.append("   S1.SEMESTER, ");
        stb.append("   S1.GRADE, ");
        stb.append("   S1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT S1 ");
        stb.append("   INNER JOIN MAXSEMES_Y S2 ");
        stb.append("     ON S2.YEAR = S1.YEAR ");
        stb.append("    AND S2.SEMESTER = S1.SEMESTER ");
        stb.append(" GROUP BY ");
        stb.append("   S1.GRADE,S1.SCHREGNO,S1.SEMESTER ");
        stb.append(" ), PASTMIX_SCHREG_BASE AS ( ");
        stb.append(" select ");
        stb.append("   T1.* ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("  T1.YEAR = '" + _param._year + "' ");
        stb.append("  AND T1.SEMESTER = '1' ");
        stb.append("  AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._hrClasses));
        stb.append(" ), PASTMIX_SCHREG AS ( ");
        stb.append(" select ");
        stb.append("   T1.* ");
        stb.append(" FROM ");
        stb.append("   PASTMIX_SCHREG_BASE T1 ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("   T2.* ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("  T2.YEAR < '" + _param._year + "' ");
        stb.append("  AND T2.SEMESTER <> '9' ");
        stb.append("  AND T2.SEMESTER = (SELECT T3.SEMESTER FROM MAXGRADE_SCHREG T3 WHERE T3.YEAR = T2.YEAR AND T3.SCHREGNO = T2.SCHREGNO) ");
        stb.append("  AND T2.SCHREGNO IN (SELECT SCHREGNO FROM PASTMIX_SCHREG_BASE)");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.GRADE, ");
        stb.append("   T3.SCHOOL_KIND, ");
        stb.append("   T3.GRADE_CD, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T2.GRADE_DEVIATION_RANK, ");
        stb.append("   T2.COURSE_DEVIATION_RANK, ");
        stb.append("   T8.COURSECODEABBV1, ");
        stb.append("   CASE WHEN T3.GRADE_CD = '01' THEN T9_1.COUNT ");
        stb.append("        WHEN T3.GRADE_CD IN ('02', '03') THEN T9_2.COUNT ");
        stb.append("        ELSE NULL END AS CNTALL ");
        stb.append(" FROM ");
        stb.append("   PASTMIX_SCHREG T1 ");
        stb.append("  LEFT JOIN RECORD_RANK_SDIV_DAT T2 ");
        stb.append("    ON T2.YEAR = T1.YEAR ");
        stb.append("   AND ((T2.YEAR < '" + _param._year + "' AND T2.SEMESTER = '9') OR (T2.YEAR = '" + _param._year + "' AND T2.SEMESTER = '1')) ");
        stb.append("   AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '990008' ");
        stb.append("   AND T2.SUBCLASSCD = '999999' ");
        stb.append("  LEFT JOIN RECORD_AVERAGE_SDIV_DAT T9_1 ");
        stb.append("    ON T9_1.YEAR = T2.YEAR ");
        stb.append("   AND T9_1.SEMESTER = T2.SEMESTER ");
        stb.append("   AND T9_1.TESTKINDCD || T9_1.TESTITEMCD || T9_1.SCORE_DIV = T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV ");
        stb.append("   AND T9_1.CLASSCD = T2.CLASSCD ");
        stb.append("   AND T9_1.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("   AND T9_1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("   AND T9_1.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("   AND T9_1.AVG_DIV = '1' ");
        stb.append("   AND T9_1.GRADE = T1.GRADE ");
        stb.append("   AND T9_1.HR_CLASS = '000' ");
        stb.append("   AND T9_1.COURSECD = '0' ");
        stb.append("   AND T9_1.MAJORCD = '000' ");
        stb.append("   AND T9_1.COURSECODE = '0000' ");
        stb.append("  LEFT JOIN RECORD_AVERAGE_SDIV_DAT T9_2 ");
        stb.append("    ON T9_2.YEAR = T2.YEAR ");
        stb.append("   AND T9_2.SEMESTER = T2.SEMESTER ");
        stb.append("   AND T9_2.TESTKINDCD || T9_2.TESTITEMCD || T9_2.SCORE_DIV = T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV ");
        stb.append("   AND T9_2.CLASSCD = T2.CLASSCD ");
        stb.append("   AND T9_2.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("   AND T9_2.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("   AND T9_2.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("   AND T9_2.AVG_DIV = '3' ");
        stb.append("   AND T9_2.GRADE = T1.GRADE ");
        stb.append("   AND T9_2.HR_CLASS = '000' ");
        stb.append("   AND T9_2.COURSECD = T1.COURSECD ");
        stb.append("   AND T9_2.MAJORCD = T1.MAJORCD ");
        stb.append("   AND T9_2.COURSECODE = T1.COURSECODE ");
        stb.append("  LEFT JOIN SCHREG_REGD_GDAT T3 ");
        stb.append("    ON T3.YEAR = T1.YEAR ");
        stb.append("   AND T3.GRADE = T1.GRADE ");
        stb.append("  LEFT JOIN COURSECODE_MST T8 ");
        stb.append("    ON T8.COURSECODE = T1.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("  T3.SCHOOL_KIND = 'H' ");

        return stb.toString();
    }

    private Attendance getAttendSemes(final DB2UDB db2, final String year, final String schregno) {
        if (!_param._semesMap.containsKey(year)) {
            return null;
        }
        final Map semInfo = _param.getSpecifyYearSemes(year);
        if (semInfo == null) {
            return null;
        }
        final String yStr = (String)semInfo.get("YEAR");
        final String semes = (String)semInfo.get("SEMESTER");
        final String sDate = (String)semInfo.get("SDATE");
        final String eDate = year.equals(_param._year) ? _param._edate : (String)semInfo.get("EDATE");

        if (semInfo == null || null == sDate || null == eDate || sDate.compareTo(eDate) > 0) {
            return null;
        }
        PreparedStatement psAtSeme = null;
        ResultSet rsAtSeme = null;

        _param._attendParamMap.put("schregno", "?");
        Attendance attendance = null;
        try {
            if (!_param._psMap.containsKey(yStr)) {
                final String sql = AttendAccumulate.getAttendSemesSql(
                        yStr,
                        semes,
                        sDate,
                        eDate,
                        _param._attendParamMap
                );
                log.info("get getAttendSemes sql(" + yStr + ") = " + sql);
                _param._psMap.put(yStr, db2.prepareStatement(sql));
            }

            psAtSeme = (PreparedStatement)_param._psMap.get(yStr);
            psAtSeme.setString(1, schregno);                 //生徒番号
            rsAtSeme = psAtSeme.executeQuery();
            while (rsAtSeme.next()) {
                if (!SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {
                    continue;
                }
                attendance = new Attendance(
                        SEMEALL, //期間の集計値なので、固定値で指定。
                        rsAtSeme.getInt("LESSON"),
                        rsAtSeme.getInt("MLESSON"),
                        rsAtSeme.getInt("SUSPEND"),
                        rsAtSeme.getInt("MOURNING"),
                        rsAtSeme.getInt("SICK"),
                        rsAtSeme.getInt("SICK_ONLY"),
                        rsAtSeme.getInt("NOTICE_ONLY"),
                        rsAtSeme.getInt("NONOTICE_ONLY"),
                        rsAtSeme.getInt("PRESENT"),
                        rsAtSeme.getInt("LATE"),
                        rsAtSeme.getInt("EARLY"),
                        rsAtSeme.getInt("TRANSFER_DATE")
                );
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            db2.commit();
//            DbUtils.closeQuietly(rsAtSeme);
        }
        return attendance;
    }
    private void getAttendSubclass(
            final DB2UDB db2,
            final String year,
            final String gradeCd,
            final Map schregData
    ) {
        if (!_param._semesMap.containsKey(year)) {
            return;
        }
        final Map semInfo = _param.getSpecifyYearSemes(year);
        if (semInfo == null) {
            return;
        }
        final String semes = (String)semInfo.get("SEMESTER");
        final String sDate = (String)semInfo.get("SDATE");
        final String eDate = year.equals(_param._year) ? _param._edate : (String)semInfo.get("EDATE");

        if (semInfo == null || null == sDate || null == eDate || sDate.compareTo(eDate) > 0) {
            return;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        _param._attendSubclsParamMap.put("absenceDiv", SEMEALL.equals(semes) ? "1" : "2");
        _param._attendSubclsParamMap.put("sSemester", "1");
        if (sDate.compareTo(eDate) < 0) {
            final String sqlAttendSubclass = AttendAccumulate.getAttendSubclassSql(
                    year,
                    semes,
                    (String)sDate,
                    (String)eDate,
                    _param._attendSubclsParamMap
                    );
            log.debug("getAttendSubclass sql:" + sqlAttendSubclass);
            try {
                ps = db2.prepareStatement(sqlAttendSubclass);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    if (!semes.equals(semester)) {
                        continue;
                    }
                    final String schregno = rs.getString("SCHREGNO");
                    if (!schregData.containsKey(schregno)) {
                        continue;
                    }
                    SchregData sd = (SchregData)schregData.get(schregno);
                    if (!sd._subclsInfo.containsKey(gradeCd)) {
                        continue;
                    }
                    Map sclst = (Map)sd._subclsInfo.get(gradeCd);

                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String classCd = subclassCd.substring(0, 2);
                    if (!sclst.containsKey(classCd)) {
                        continue;
                    }
                    List scLst = (List)sclst.get(classCd);
                    for (Iterator ite = scLst.iterator();ite.hasNext();) {
                        final DetailData dd = (DetailData)ite.next();
                        if (subclassCd.equals(dd._subclasscd)) {
                            dd._kekka = rs.getString("SICK2");
                        }
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        return;
    }


    private class Attendance {
        final String _semester;
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _sickOnly;
        final int _noticeOnly;
        final int _nonoticeOnly;
        final int _present;
        final int _late;
        final int _early;
        final int _transDays;
        Attendance(
                final String semester,
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int sickOnly,
                final int noticeOnly,
                final int nonoticeOnly,
                final int present,
                final int late,
                final int early,
                final int transDays
        ) {
            _semester = semester;
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _sickOnly = sickOnly;
            _noticeOnly = noticeOnly;
            _nonoticeOnly = nonoticeOnly;
            _present = present;
            _late = late;
            _early = early;
            _transDays = transDays;
        }
        private BigDecimal calcAttRate() {
            BigDecimal calcWk = new BigDecimal((double)((_mLesson - (_sickOnly + _noticeOnly + _nonoticeOnly)) / _mLesson)).setScale(1, BigDecimal.ROUND_HALF_UP);
            return calcWk;
        }
    }

    private class DetailData {
        final String _year;
        final String _grade;
        final String _gradeCd;
        final String _schregno;
        final String _classcd;
        final String _electDiv;
        final String _classname;
        final String _classabbv;
        final String _subclassname;
        final String _subclassabbv;
        final String _subclasscd;
        final String _score1;
        final String _score2;
        final String _credits;
        String _kekka;
        public DetailData (final String year, final String grade, final String gradeCd, final String schregno, final String classcd, final String electDiv, final String classname, final String classabbv, final String subclassname, final String subclassabbv, final String subclasscd, final String score1, final String score2, final String credits)
        {
            _year = year;
            _grade = grade;
            _gradeCd = gradeCd;
            _schregno = schregno;
            _classcd = classcd;
            _electDiv = electDiv;
            _classname = classname;
            _classabbv = classabbv;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
            _subclasscd = subclasscd;
            _score1 = score1;
            _score2 = score2;
            _credits = credits;
            _kekka = "";
        }

    }
    private class TotalData {
        final String _semester;
        final String _score_Div;
        final String _testik;
        final String _classcd;
        final String _subclasscd;
        final String _grade;
        final String _hr_Class;
        final String _attendno;
        final String _schregno;
        final String _total_3Gakki;
        final String _kimatu_Score;
        final String _absent;
        final String _last_Toutatudo;
        final String _last_Gakusyutaido;
        public TotalData (final String semester, final String score_Div, final String testik, final String classcd, final String subclasscd, final String grade, final String hr_Class, final String attendno, final String schregno, final String total_3Gakki, final String kimatu_Score, final String absent, final String last_Toutatudo, final String last_Gakusyutaido)
        {
            _semester = semester;
            _score_Div = score_Div;
            _testik = testik;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _grade = grade;
            _hr_Class = hr_Class;
            _attendno = attendno;
            _schregno = schregno;
            _total_3Gakki = total_3Gakki;
            _kimatu_Score = kimatu_Score;
            _absent = absent;
            _last_Toutatudo = last_Toutatudo;
            _last_Gakusyutaido = last_Gakusyutaido;
        }
    }
    private class SchregData {
        final String _schregno;
        final String _grade;
        final String _grade_Name1;
        final String _hr_Class;
        final String _hr_Name;
        final String _attendno;
        final String _name;
        Map _clsSumInfo;
        Map _subclsInfo;
        Map _resultsInfo;
        public SchregData (final String schregno, final String grade, final String grade_Name1, final String hr_Class, final String hr_Name, final String attendno, final String name)
        {
            _schregno = schregno;
            _grade = grade;
            _grade_Name1 = grade_Name1;
            _hr_Class = hr_Class;
            _hr_Name = hr_Name;
            _attendno = attendno;
            _name = name;
        }
        private CalcData getCalcData(final String gradeCd) {
            CalcData retObj = new CalcData();
            if (_subclsInfo != null && _subclsInfo.containsKey(gradeCd)) {
                final Map subMap = (Map)_subclsInfo.get(gradeCd);
                for (Iterator its = subMap.keySet().iterator();its.hasNext();) {
                    final String kStr = (String)its.next();
                    if ("99".equals(kStr)) continue;
                    final List dList = (List)subMap.get(kStr);
                    for (Iterator ite = dList.iterator();ite.hasNext();) {
                        final DetailData dd = (DetailData)ite.next();
                        retObj._subclsCnt++;
                        retObj._hyoteiKei += Integer.parseInt(StringUtils.defaultString(dd._score2, "0"));
                        retObj._creditCnt += Integer.parseInt(StringUtils.defaultString(dd._credits, "0"));
                    }
                }
            }
            retObj._hyoteiAvg = new BigDecimal("0.0");
            if (retObj._subclsCnt > 0) {
                retObj._hyoteiAvg = new BigDecimal(retObj._hyoteiKei * 1.0 / retObj._subclsCnt * 1.0).setScale(1, BigDecimal.ROUND_HALF_UP);
            }
            return retObj;
        }
    }
    private class ResultsData {
        final String _year;
        final String _grade;
        final String _school_Kind;
        final String _grade_Cd;
        final String _schregno;
        final String _grade_Deviation_Rank;
        final String _course_Deviation_Rank;
        final String _coursecodeabbv1;
        final String _cntAll;
        public ResultsData(final String year, final String grade, final String school_Kind, final String grade_Cd, final String schregno, final String grade_Deviation_Rank, final String course_Deviation_Rank, final String coursecodeabbv1, final String cntAll)
        {
            _year = year;
            _grade = grade;
            _school_Kind = school_Kind;
            _grade_Cd = grade_Cd;
            _schregno = schregno;
            _grade_Deviation_Rank = grade_Deviation_Rank;
            _course_Deviation_Rank = course_Deviation_Rank;
            _coursecodeabbv1 = coursecodeabbv1;
            _cntAll = cntAll;
        }
    }
    private class CalcData {
        int _subclsCnt;
        int _hyoteiKei;
        int _creditCnt;
        BigDecimal _hyoteiAvg;
        String _className;
        String _electDiv;
        public CalcData () {
            _subclsCnt = 0;
            _hyoteiKei = 0;
            _creditCnt = 0;
            _hyoteiAvg = new BigDecimal("0.0");
            _className = "";
            _electDiv = "";
        }
        void add(CalcData wk) {
            _subclsCnt += wk._subclsCnt;
            _hyoteiKei += wk._hyoteiKei;
            _creditCnt += wk._creditCnt;
        }
        private String calc() {
            return _subclsCnt == 0 ? "" : (new BigDecimal((_hyoteiKei * 1.0) / (_subclsCnt * 1.0)).setScale(1, BigDecimal.ROUND_HALF_UP)).toString();
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 75957 $ $Date: 2020-08-12 10:52:42 +0900 (水, 12 8 2020) $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
//        log.debug("学校種別=" + param._schooldiv + " テスト=" + param._testname + " 日付=" + param._date + " Form-File=" + FORM_FILE);
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _ctrlSemester;
        private final String _grade;
        private final String[] _hrClasses;
        private final String _date;
        private final String _edate;
        private final String _useCurriculumcd;
        private final Map _attendParamMap;
        private final Map _attendSubclsParamMap;
        private final Map _psMap;

        private Map _semesMap;
        private final List _classCdList;
        private String _lastSemester;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) throws Exception {
            _year = request.getParameter("CTRL_YEAR");
            _semester = "1";  //3年の1学期までなので、固定。
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _hrClasses = request.getParameterValues("CATEGORY_SELECTED");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            final KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            _date = request.getParameter("DATE") != null ? request.getParameter("DATE").replace('/', '-') : "";//KNJ_EditDate.h_format_US_Y(returnval.val3) + "年" + KNJ_EditDate.h_format_JP_MD(returnval.val3);
            _edate = request.getParameter("EDATE") != null ? request.getParameter("EDATE").replace('/', '-') : "";
            _semesMap = getSemesterMap(db2);
            _classCdList = getClassCdList(db2);
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", _useCurriculumcd);

            _attendSubclsParamMap = new HashMap();
            _attendSubclsParamMap.put("DB2UDB", db2);
            _attendSubclsParamMap.put("HttpServletRequest", request);
            _attendSubclsParamMap.put("useCurriculumcd", _useCurriculumcd);

            _psMap = new HashMap();
        }

        private List getClassCdList(final DB2UDB db2) {
            final List retList = new ArrayList();
            final StringBuffer stb = new StringBuffer();
            stb.append(" select DISTINCT ");
            stb.append("   T3.CLASSCD ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   LEFT JOIN CHAIR_STD_DAT T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN CHAIR_DAT T3 ");
            stb.append("     ON T3.YEAR = T2.YEAR ");
            stb.append("    AND T3.SEMESTER = T2.SEMESTER ");
            stb.append("    AND T3.CHAIRCD = T2.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _year + "' ");
            stb.append("   AND T1.SEMESTER <= '" + _semester + "' ");
            stb.append("   AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(false, _hrClasses) + " ");
            stb.append("   AND T3.CLASSCD < '90' ");
            stb.append("   AND T3.CLASSCD NOT IN ('00', '33', '55', '99') ");
            log.debug("getClassCdList sql:"+ stb.toString());
            final List wkList = KnjDbUtils.query(db2, stb.toString());

            for (Iterator ite = wkList.iterator();ite.hasNext();) {
                final Map row = (Map)ite.next();
                retList.add((String)row.get("CLASSCD"));
            }
            return retList;
        }

        private Map getSemesterMap(final DB2UDB db2) {
            final String sql = " SELECT YEAR, SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE YEAR < '" + _year + "' AND SEMESTER = '9' UNION ALL SELECT YEAR, SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER <= '" + _semester + "' ORDER BY YEAR, SEMESTER ";
            log.debug("getSemesterMap sql:"+ sql);
            final List Lst = KnjDbUtils.query(db2, sql);
            final Map rtn = new LinkedMap();
            String maxStr = "";
            for (Iterator ite = Lst.iterator();ite.hasNext();) {
                final Map row = (Map)ite.next();
                final String wkSems = (String)row.get("SEMESTER");
                if ("".equals(maxStr)) {
                    maxStr = wkSems;
                } else if (Integer.parseInt(wkSems) > Integer.parseInt(maxStr)) {
                    maxStr = wkSems;
                }
                rtn.put(row.get("YEAR"), row);
            }
            _lastSemester = maxStr;
            return rtn;
        }
        //指定年度の通年学期情報(!!当年度はCTRL_SEMESTERまで!!)を取得する。※当年度は、SDATEが
        private Map getSpecifyYearSemes(final String year) {
            Map retMap = null;
            String fstSDate = "";
            for (Iterator ite = _semesMap.keySet().iterator();ite.hasNext();) {
                final String kStr = (String)ite.next();
                if (kStr.length() >= 4) {
                    final String yStr = kStr.substring(0,4);
                    if (year.equals(yStr)) {
                        retMap = (Map)_semesMap.get(yStr);
                        if (!year.equals(_year)) {
                            break;
                        } else {
                            if ("".equals(fstSDate)) {
                                fstSDate = (String)retMap.get("SDATE");
                            }
                        }
                    }
                }
            }
            //_semesMapはソートされているので、当年度であれば、ループの最後がCTRL_SEMESTERのデータ。ただし、開始日は最初のSDATEに変える
            if (year.equals(_year) && retMap != null) {
                retMap.put("SDATE", fstSDate);
            }
            return retMap;
        }
    }

}
