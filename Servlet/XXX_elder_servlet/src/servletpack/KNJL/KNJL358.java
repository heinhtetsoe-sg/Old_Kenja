// kanji=漢字
/*
 * $Id: ff001cc6dbcf6b02ad365b0f420b5e7d58444087 $
 *
 * 作成日: 2006/01/05
 * 作成者: m-yama
 *
 * Copyright(C) 2006-2009 ALP Okinawa Co.,Ltd. All rights reserved.
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
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３５８＞  結果集計表
 *
 *  2006/01/05 m-yama 作成日
 *  2006/01/10 m-yama NO001 繰上合格SQL修正
 *  2006/01/11 m-yama NO002 繰上合格SQL修正(繰上は、繰上欄と入学者欄のみ対象)
 *                          標準合格の医薬繰上は、オール0
 *  2006/01/13 o-naka NO003 繰上合格人数を同じ行の合格者の発表段階・合計に加えた。
 *                    NO004 繰上合格の18,19行目が出力されない不具合を修正
 *                    NO005 繰上合格人数を同じ行の入学手続完了者または入学手続辞退者または入学辞退者に加えた。
 *                          ○NO003,NO005の補足説明
 *                              ○合格者と入学手続と入学辞退者は、発表段階のコースでカウント
 *                              ○繰上合格と入学者は、最終合格コースでカウント
 * @author m-yama
 * @version $Id: ff001cc6dbcf6b02ad365b0f420b5e7d58444087 $
 */

public class KNJL358 {

    private static final String COURSE_IYAKU   = "11001000";
    private static final String COURSE_TOKUSIN = "11002000";
    private static final String COURSE_HYOUJUN = "11003000";

    private static final String UP_PASS1 = "UP_PASS1";
    private static final String UP_PASS2 = "UP_PASS2";
    private static final String NORMAL = "NORMAL";

    private static final int UPPER_MAX_LINE = 9;

    private static final Log log = LogFactory.getLog(KNJL358.class);

    StringBuffer _sqlInIyakuSyoukei = new StringBuffer();
    StringBuffer _sqlInTokusinSyoukei = new StringBuffer();
    StringBuffer _sqlInSingakuSyoukei = new StringBuffer();
    String _commaIyakuSyou = "";
    String _commaTokusinSyou = "";
    String _commaSingakuSyou = "";

    StringBuffer _sqlInIyakuGoukei = new StringBuffer();
    StringBuffer _sqlInTokusinGoukei = new StringBuffer();
    StringBuffer _sqlInSingakuGoukei = new StringBuffer();
    String _commaIyakuGou = "";
    String _commaTokusinGou = "";
    String _commaSingakuGou = "";

    Param _param;

    /**
     * KNJL.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        try {
            init(response, svf);

            //  ＤＢ接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            //  ＳＶＦ作成処理
            boolean hasData = false; //該当データなしフラグ

            //SVF出力
            if (printMain(db2, svf)) {
                hasData = true;
            }

            //  該当データ無し
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            close(db2, svf);
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

    /**
     * 合否判定
     */
    // CSOFF: Interface
    private interface JudgeMent {
        /** 第一志望合格 */
        String PASS1      = "1";

        /** 第二志望合格 */
        String PASS2      = "2";

        /** 第三志望合格 */
        String PASS3      = "3";

        /** 推薦合格 */
        String SUISEN     = "4";

        /** 追加合格 */
        String ADDPASS    = "5";

        /** コース変更合格 */
        String CHANGEPASS = "6";

        /** 不合格 */
        String UNPASS     = "7";

        /** 未受験(欠席) */
        String MIJUKEN    = "8";

        /** 保留 */
        String HORYUU     = "9";
    }
    // CSON: Interface

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
        private final String _gengou;
        private final String _output;
        private final String _schooldiv;
        private final String _title;
        private final String _schoolName;
        private final String _date;
        private final List _courseList;
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {

            _year = request.getParameter("YEAR");
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_year));
            _gengou = gengou + "年度";
            _output = request.getParameter("OUTPUT");
            _schooldiv = request.getParameter("JHFLG");

            if (_output.equals("1")) {
                _title = "　前期入学試験　";
            } else if (_output.equals("2")) {
                _title = "　後期入学試験　";
            } else if (_output.equals("3")) {
                _title = "　附属推薦　";
            } else {
                _title = "　入学試験　";
            }

            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            _date = KNJ_EditDate.h_format_JP(returnval.val3);
            getinfo = null;
            returnval = null;

            // DBより取得
            _schoolName = getSchoolName(db2);
            _courseList = setCourseList(db2, _year);
        }

        private String getSchoolName(final DB2UDB db2) throws Exception {
            String rtnSchoolName = "";

            db2.query("SELECT T1.SCHOOLNAME1 FROM SCHOOL_MST T1 WHERE T1.YEAR = (SELECT MAX(T2.YEAR) FROM SCHOOL_MST T2)");
            final ResultSet rs = db2.getResultSet();
            try {
                while (rs.next()) {
                    rtnSchoolName = rs.getString("SCHOOLNAME1");
                }
            } finally {
                DbUtils.closeQuietly(rs);
            }
            return rtnSchoolName;
        }

        /**コースリストセット*/
        private List setCourseList(final DB2UDB db2, final String year) throws Exception {
            final List rtnList = new ArrayList();
            db2.query("SELECT COURSECD || MAJORCD || EXAMCOURSECD AS COURSE FROM ENTEXAM_COURSE_MST "
                    + "WHERE ENTEXAMYEAR = '" + year + "' ORDER BY EXAMCOURSECD");
            final ResultSet rs = db2.getResultSet();
            try {
                while (rs.next()) {
                    rtnList.add(rs.getString("COURSE"));
                }
            } finally {
                DbUtils.closeQuietly(rs);
            }
            return rtnList;
        }
    }

    /**印刷処理メイン*/
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        boolean rtnflg = false;

        //フォーム
        svf.VrSetForm("KNJL358.frm", 1);
        printHeder(svf);
        if (upperMeisai(db2, svf)) {
            rtnflg = true;
        }
        lowerMeisai(db2, svf);

        svf.VrEndPage();

        return rtnflg;

    }

    /**ヘッダデータをセット*/
    private void printHeder(final Vrw32alp svf) {
        svf.VrsOut("NENDO"          , _param._gengou);
        svf.VrsOut("SCHOOLNAME"     , _param._schoolName);
        svf.VrsOut("TITLEDIV"       , _param._title);
        svf.VrsOut("DATE"           , _param._date + "現在");
    }

    /**上段明細処理*/
    private boolean upperMeisai(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        boolean rtnflg = false;
        int gyo = 1;
        String commaSyou = "";
        String commaGou = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        final StringBuffer sqlInGoukei = new StringBuffer();
        try {
            sqlInGoukei.append("(");
            for (final Iterator it = _param._courseList.iterator(); it.hasNext();) {
                final StringBuffer sqlInSyoukei = new StringBuffer();
                sqlInSyoukei.append("(");
                final String course = (String) it.next();
                ps = db2.prepareStatement(sqlDesireGet(course));
                rs = ps.executeQuery();
                while (rs.next()) {

                    //IN文作成
                    sqlInSyoukei.append(commaSyou + "'" + rs.getString("DESIREDIV") + "'");
                    sqlInGoukei.append(commaGou + "'" + rs.getString("DESIREDIV") + "'");
                    commaSyou = ",";
                    commaGou  = ",";

                    //明細データをセット
                    printUpperMeisai(db2, svf, "('" + rs.getString("DESIREDIV") + "')", gyo);
                    rtnflg = true;
                    gyo++;
                }
                db2.commit();

                //明細データをセット
                if (gyo == UPPER_MAX_LINE) {
                    sqlInGoukei.append(")");
                    commaGou = printUpperMeisai(db2, svf, sqlInGoukei.toString(), gyo);
                } else {
                    sqlInSyoukei.append(")");
                    commaSyou = printUpperMeisai(db2, svf, sqlInSyoukei.toString(), gyo);
                    gyo++;
                }

            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtnflg;

    }

    /**上段明細データ印刷処理*/
    private String printUpperMeisai(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String desirediv,
            final int gyo
    ) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {

            ps = db2.prepareStatement(sqlUpperMeisaiGet(desirediv, gyo));
            rs = ps.executeQuery();

            while (rs.next()) {
                svf.VrsOutn("APPLICATION1"  , gyo, rs.getString("BASEBOY"));
                svf.VrsOutn("APPLICATION2"  , gyo, rs.getString("BASEGIL"));
                svf.VrsOutn("APPLICATION3"  , gyo, rs.getString("BASEALL"));
                svf.VrsOutn("EXEMINEE1"     , gyo, rs.getString("JUKENBOY"));
                svf.VrsOutn("EXEMINEE2"     , gyo, rs.getString("JUKENGIL"));
                svf.VrsOutn("EXEMINEE3"     , gyo, rs.getString("JUKENALL"));
                svf.VrsOutn("ABSENTEE1"     , gyo, rs.getString("MIJUKENBOY"));
                svf.VrsOutn("ABSENTEE2"     , gyo, rs.getString("MIJUKENGIL"));
                svf.VrsOutn("ABSENTEE3"     , gyo, rs.getString("MIJUKENALL"));
                svf.VrsOutn("FAIL1"         , gyo, rs.getString("UNPASSBOY"));
                svf.VrsOutn("FAIL2"         , gyo, rs.getString("UNPASSGIL"));
                svf.VrsOutn("FAIL3"         , gyo, rs.getString("UNPASSALL"));
            }

        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return "";
    }

    /**下段明細処理*/
    // CSOFF: ExecutableStatementCount
    private boolean lowerMeisai(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        boolean rtnflg = false;
        int gyo = 1;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String dDiv = "";
        final StringBuffer sqlInGoukei = new StringBuffer();
        String commaGou = "";
        final Map printData = new HashMap();
        try {
            sqlInGoukei.append("(");
            sqlSetClear("set", "1", "(");
            for (final Iterator it = _param._courseList.iterator(); it.hasNext();) {
                sqlSetClear("set", "2", "(");
                final String course = (String) it.next();

                ps = db2.prepareStatement(sqlDesireGetLower(course, gyo));
                rs = ps.executeQuery();

                while (rs.next()) {
                    dDiv = rs.getString("DESIREDIV");

                    final String rsCourse = rs.getString("COURSE");
                    setSqlInState(rsCourse, dDiv);

                    sqlInGoukei.append(commaGou + "'" + dDiv + rsCourse + "'");
                    commaGou  = ",";

                    //明細データをセット
                    final Integer line = new Integer(gyo);
                    final String desirediv = "('" + dDiv + rsCourse + "')";
                    if (!printData.containsKey(line)) {
                        printData.put(line, new LineData(db2, gyo, desirediv, dDiv, course, false));
                    }
                    if (UP_PASS1.equals(isUpPassLine(gyo))) {
                        final LineData linedata = (LineData) printData.get(line);
                        linedata.setMain(db2, gyo, desirediv, dDiv, course, true);
                    }
                    gyo++;

                    if (gyo == 7) {
                        //明細データをセット
                        printData.put(new Integer(gyo), new LineData(db2, gyo, desirediv, dDiv, course, true));
                        gyo++;
                    }

                    rtnflg = true;
                }
                db2.commit();

                //明細データをセット
                if (gyo != 22) {
                    gyo = printKei(db2, gyo, dDiv, course, printData);
                }
                if (gyo == 22) {
                    sqlInGoukei.append(")");
                    setPrintKei(db2, sqlInGoukei.toString(), dDiv, course, printData, gyo, new Integer(gyo), true);
                    commaGou = "";
                }

            }
            outputPrintData(svf, printData);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtnflg;

    }
    // CSON: ExecutableStatementCount

    /**
     * データ出力
     */
    private void outputPrintData(final Vrw32alp svf, final Map printData) {
        addKuriageData(printData);
        for (final Iterator itPrintData = printData.keySet().iterator(); itPrintData.hasNext();) {
            final Integer key = (Integer) itPrintData.next();
            final LineData linedata = (LineData) printData.get(key);

            for (final Iterator itLine = linedata._data.keySet().iterator(); itLine.hasNext();) {
                final String keyLine = (String) itLine.next();
                final DistributeValue dist = (DistributeValue) linedata._data.get(keyLine);
                svf.VrsOutn(dist._mfield, key.intValue(), String.valueOf(dist._mcnt));
                svf.VrsOutn(dist._lfield, key.intValue(), String.valueOf(dist._lcnt));
                svf.VrsOutn(dist._tfield, key.intValue(), String.valueOf(dist._tcnt));
            }
            log.debug(key + " = " + linedata);
        }
    }

    /**
     * 繰上データを足し込む
     */
    private void addKuriageData(final Map printData) {
        for (final Iterator itPrintData = printData.keySet().iterator(); itPrintData.hasNext();) {
            final Integer key = (Integer) itPrintData.next();
            final LineData linedata = (LineData) printData.get(key);

            if (key.intValue() == 22 || UP_PASS1.equals(isUpPassLine(key.intValue()))) {
                for (final Iterator itLine = linedata._kuriageData.keySet().iterator(); itLine.hasNext();) {
                    final String keyKuri = (String) itLine.next();
                    final DistributeValue distKuri = (DistributeValue) linedata._kuriageData.get(keyKuri);
                    final DistributeValue distData = (DistributeValue) linedata._data.get(keyKuri);

                    addKuriageDataExe(distKuri, distData);
                }
            }
            if (UP_PASS2.equals(isUpPassLine(key.intValue()))) {
                for (final Iterator itLine = linedata._kuriageData.keySet().iterator(); itLine.hasNext();) {
                    final String keyKuri = (String) itLine.next();
                    final DistributeValue distKuri = (DistributeValue) linedata._kuriageData.get(keyKuri);

                    final LineData addLine = (LineData) printData.get(new Integer(key.intValue() - 1));
                    final DistributeValue distData = (DistributeValue) addLine._data.get(keyKuri);

                    addKuriageDataExe(distKuri, distData);
                }
            }
        }
    }

    private void addKuriageDataExe(final DistributeValue distKuri, final DistributeValue distData) {
        distData._mcnt += distKuri._mcnt;
        distData._lcnt += distKuri._lcnt;
        distData._tcnt += distKuri._tcnt;
    }

    private int printKei(
            final DB2UDB db2,
            final int gyo,
            final String dDiv,
            final String course,
            final Map printData
    ) throws Exception {
        int rtngyo = gyo;
        sqlSetClear("set", "2", ")");
        if (rtngyo == 8) {
            printData.put(new Integer(rtngyo), new LineData(db2, rtngyo, _sqlInIyakuSyoukei.toString(), dDiv, course, false));
            rtngyo++;
        }
        if (rtngyo >= 18) {
            sqlSetClear("set", "1", ")");

            rtngyo = setPrintKei(db2, _sqlInIyakuGoukei.toString(), dDiv, course, printData, rtngyo, new Integer(rtngyo), false);

            rtngyo = setPrintKei(db2, _sqlInTokusinGoukei.toString(), dDiv, course, printData, rtngyo, new Integer(rtngyo), true);

            rtngyo = setPrintKei(db2, _sqlInSingakuGoukei.toString(), dDiv, course, printData, rtngyo, new Integer(rtngyo), true);

            rtngyo = setPrintKei(db2, _sqlInSingakuGoukei.toString(), dDiv, course, printData, rtngyo, new Integer(rtngyo), true);

            sqlSetClear("clear", "1", "");
        } else {

            rtngyo = setPrintKei(db2, _sqlInTokusinSyoukei.toString(), dDiv, course, printData, rtngyo, new Integer(rtngyo), false);

            rtngyo = setPrintKei(db2, _sqlInSingakuSyoukei.toString(), dDiv, course, printData, rtngyo, new Integer(rtngyo), false);

            if (rtngyo == 11) {
                //明細データをセット
                printData.put(new Integer(rtngyo), new LineData(db2, rtngyo, _sqlInSingakuSyoukei.toString(), dDiv, course, true));
                rtngyo++;
            }
        }
        sqlSetClear("clear", "2", "");
        return rtngyo;
    }

    private int setPrintKei(
            final DB2UDB db2,
            final String desirediv,
            final String dDiv,
            final String course,
            final Map printData,
            final int gyo,
            final Integer keyGyo,
            final boolean noCheck
    ) throws Exception {
        int rtngyo = gyo;

        if (gyo != 21) {
            printData.put(keyGyo, new LineData(db2, rtngyo, desirediv, dDiv, course, false));
        }

        if (noCheck || (!NORMAL.equals(isUpPassLine(rtngyo)) && rtngyo != 15 && rtngyo != 18)) {
            if (printData.containsKey(keyGyo)) {
                final LineData linedata = (LineData) printData.get(keyGyo);
                linedata.setMain(db2, rtngyo, desirediv, dDiv, course, true);
            } else {
                printData.put(keyGyo, new LineData(db2, rtngyo, desirediv, dDiv, course, true));
            }
        }

        rtngyo++;

        return rtngyo;
    }

    private void setSqlInState(final String course, final String dDiv) {
        //IN文作成
        if (COURSE_IYAKU.equals(course)) {
            _sqlInIyakuSyoukei.append(_commaIyakuSyou + "'" + dDiv + course + "'");
            _commaIyakuSyou = ",";
            _sqlInIyakuGoukei.append(_commaIyakuGou + "'" + dDiv + course + "'");
            _commaIyakuGou = ",";
        } else if (COURSE_TOKUSIN.equals(course)) {
            _sqlInTokusinSyoukei.append(_commaTokusinSyou + "'" + dDiv + course + "'");
            _commaTokusinSyou = ",";
            _sqlInTokusinGoukei.append(_commaTokusinGou + "'" + dDiv + course + "'");
            _commaTokusinGou = ",";
        } else if (COURSE_HYOUJUN.equals(course)) {
            _sqlInSingakuSyoukei.append(_commaSingakuSyou + "'" + dDiv + course + "'");
            _commaSingakuSyou = ",";
            _sqlInSingakuGoukei.append(_commaSingakuGou + "'" + dDiv + course + "'");
            _commaSingakuGou = ",";
        }
    }

    /**IN分のデータをセット・クリアする*/
    private void sqlSetClear(final String setclear, final String gousyouFlg, final String setData) {
        if (gousyouFlg.equals("1")) {
            if (setclear.equals("set")) {
                _sqlInIyakuGoukei.append(setData);
                _sqlInTokusinGoukei.append(setData);
                _sqlInSingakuGoukei.append(setData);
            } else {
                _sqlInIyakuGoukei.delete(0, _sqlInIyakuGoukei.length());
                _sqlInTokusinGoukei.delete(0, _sqlInTokusinGoukei.length());
                _sqlInSingakuGoukei.delete(0, _sqlInSingakuGoukei.length());
                _commaIyakuGou = setData;
                _commaTokusinGou = setData;
                _commaSingakuGou = setData;
            }
        } else {
            if (setclear.equals("set")) {
                _sqlInIyakuSyoukei.append(setData);
                _sqlInTokusinSyoukei.append(setData);
                _sqlInSingakuSyoukei.append(setData);
            } else {
                _sqlInIyakuSyoukei.delete(0, _sqlInIyakuSyoukei.length());
                _sqlInTokusinSyoukei.delete(0, _sqlInTokusinSyoukei.length());
                _sqlInSingakuSyoukei.delete(0, _sqlInSingakuSyoukei.length());
                _commaIyakuSyou = setData;
                _commaTokusinSyou = setData;
                _commaSingakuSyou = setData;
            }
        }
    }

    /**
     * SQL抽出条件設定
     */
    // CSOFF: Cyclomatic
    private String makeDesireCourse(
            final DB2UDB db2,
            final int gyo,
            final String dDivVal,
            final String coursecd
    ) throws Exception {
        PreparedStatement ps;
        ResultSet rs;
        final StringBuffer desirecourse = new StringBuffer();

        String desirecomma = "";

        if (gyo == 3 || gyo == 5 || gyo == 6) {
            desirecourse.append("('" + dDivVal + COURSE_IYAKU + "')");
        } else if (gyo == 9 || gyo == 10 || gyo == 19 || gyo == 20) {
            ps = db2.prepareStatement(sqlDesireGetLower(coursecd, gyo));
            rs = ps.executeQuery();
            desirecourse.append("(");
            while (rs.next()) {
                desirecourse.append(desirecomma + "'" + rs.getString("DESIREDIV") + COURSE_IYAKU + "'");
                desirecomma = ",";
            }
            desirecourse.append(")");
        } else if (gyo == 7 || gyo == 14) {
            desirecourse.append("('" + dDivVal + COURSE_TOKUSIN + "')");
        } else if (gyo == 11 || gyo == 16 || gyo == 21) {
            ps = db2.prepareStatement(sqlDesireGetLower(coursecd, gyo));
            rs = ps.executeQuery();
            desirecourse.append("(");
            while (rs.next()) {
                desirecourse.append(desirecomma + "'" + rs.getString("DESIREDIV") + COURSE_TOKUSIN + "'");
                desirecomma = ",";
            }
            desirecourse.append(")");
        } else if (gyo == 22) {
            ps = db2.prepareStatement(sqlDesireGetLower(coursecd, gyo));
            rs = ps.executeQuery();
            desirecourse.append("(");
            while (rs.next()) {
                desirecourse.append(desirecomma + "'" + rs.getString("DESIREDIV") + COURSE_IYAKU + "','"
                                  + rs.getString("DESIREDIV") + COURSE_TOKUSIN + "'");
                desirecomma = ",";
            }
            desirecourse.append(")");
            log.debug(desirecourse.toString());
        }

        return desirecourse.toString();
    }
    // CSON: Cyclomatic

    /**
     * 行データ
     */
    private class LineData {
        private final Map _data = new HashMap();
        private final Map _kuriageData = new HashMap();

        LineData(
                final DB2UDB db2,
                final int line,
                final String desirediv,
                final String dDiv,
                final String course,
                final boolean isKuriage
        ) throws Exception {
            setMain(db2, line, desirediv, dDiv, course, isKuriage);
        }

        private void setMain(
                final DB2UDB db2,
                final int line,
                final String desirediv,
                final String dDiv,
                final String course,
                final boolean isKuriage
        ) throws Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer desirecourse = new StringBuffer();
            try {

                desirecourse.append(makeDesireCourse(db2, line, dDiv, course));

                if (isKuriage) {
                    ps = db2.prepareStatement(sqlLowerKuriageMeisaiGet(desirediv, line, desirecourse.toString()));
                } else {
                    ps = db2.prepareStatement(sqlLowerMeisaiGet(desirediv, line, desirecourse.toString()));
                }
                rs = ps.executeQuery();

                while (rs.next()) {
                    if (!isKuriage) {
                        setNormal(rs);
                    } else {
                        setUpPass(rs);
                    }
                }

            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        // CSOFF: Cyclomatic
        private void setNormal(final ResultSet rs) throws Exception {
            _data.put("SUCCESS1", new DistributeValue("SUCCESS1_", rs.getInt("PASSBOY"), rs.getInt("PASSGIL"), rs.getInt("PASSALL")));
            _data.put("SUCCESS2", new DistributeValue("SUCCESS2_", rs.getInt("ADDPASSBOY"), rs.getInt("ADDPASSGIL"), rs.getInt("ADDPASSALL")));
            _data.put("SUCCESS3", new DistributeValue("SUCCESS3_", rs.getInt("TOTALPASSBOY"), rs.getInt("TOTALPASSGIL"), rs.getInt("TOTALPASSALL")));
            _data.put("PROCEDURE1", new DistributeValue("PROCEDURE1_", rs.getInt("UNPROCEDUREBOY"), rs.getInt("UNPROCEDUREGIL"), rs.getInt("UNPROCEDUREALL")));
            _data.put("PROCEDURE2", new DistributeValue("PROCEDURE2_", rs.getInt("PROCEDUREBOY"), rs.getInt("PROCEDUREGIL"), rs.getInt("PROCEDUREALL")));
            _data.put("DECLINE", new DistributeValue("DECLINE", rs.getInt("UNENTBOY"), rs.getInt("UNENTGIL"), rs.getInt("UNENTALL")));
            _data.put("ENTRANCE", new DistributeValue("ENTRANCE", rs.getInt("ENTBOY"), rs.getInt("ENTGIL"), rs.getInt("ENTALL")));
        }
        // CSON: Cyclomatic

        private void setUpPass(final ResultSet rs) throws Exception {
            _data.put("ADVANCE", new DistributeValue("ADVANCE", rs.getInt("UPPASSBOY"), rs.getInt("UPPASSGIL"), rs.getInt("UPPASSALL")));

            _kuriageData.put("SUCCESS1", new DistributeValue("A", rs.getInt("UPPASSBOY"), rs.getInt("UPPASSGIL"), rs.getInt("UPPASSALL")));
            _kuriageData.put("SUCCESS3", new DistributeValue("B", rs.getInt("UPPASSBOY"), rs.getInt("UPPASSGIL"), rs.getInt("UPPASSALL")));
            _kuriageData.put("PROCEDURE1", new DistributeValue("C", rs.getInt("UNPRO_UPPASSBOY"), rs.getInt("UNPRO_UPPASSGIL"), rs.getInt("UNPRO_UPPASSALL")));
            _kuriageData.put("PROCEDURE2", new DistributeValue("D", rs.getInt("PRO_UPPASSBOY"), rs.getInt("PRO_UPPASSGIL"), rs.getInt("PRO_UPPASSALL")));
            _kuriageData.put("DECLINE", new DistributeValue("E", rs.getInt("UNENT_UPPASSBOY"), rs.getInt("UNENT_UPPASSGIL"), rs.getInt("UNENT_UPPASSALL")));
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            final StringBuffer stb = new StringBuffer();
            for (final Iterator it = _data.keySet().iterator(); it.hasNext();) {
                final String key = (String) it.next();
                final DistributeValue dist = (DistributeValue) _data.get(key);
                stb.append(" " + dist);
            }
            return stb.toString();
        }
    }

    private String isUpPassLine(final int line) {
        String rtnSt = "";
        if (line == 7 || line == 11 || line == 21) {
            rtnSt = UP_PASS2;
        } else if (line == 3 || line == 5 || line == 6 || line == 9 || line == 10 || line == 14 || line == 16 || line == 19 || line == 20) {
            rtnSt = UP_PASS1;
        } else {
            rtnSt = NORMAL;
        }
        return rtnSt;
    }

    /** データの最小単位 */
    private class DistributeValue {
        private final String _mfield;
        private final String _lfield;
        private final String _tfield;
        private int _mcnt;
        private int _lcnt;
        private int _tcnt;

        DistributeValue(final String field, final int mcnt, final int lcnt, final int tcnt) {
            _mfield = field + "1";
            _lfield = field + "2";
            _tfield = field + "3";
            _mcnt = mcnt;
            _lcnt = lcnt;
            _tcnt = tcnt;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _tfield + "/" + _mcnt + "/" + _lcnt + "/" + _tcnt;
        }
    }

    /**
     *  志願コースを抽出
     */
    private String sqlDesireGet(final String course) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT DISTINCT ");
        stb.append("    DESIREDIV ");
        stb.append("FROM ");
        stb.append("    ENTEXAM_WISHDIV_MST ");
        stb.append("WHERE ");
        stb.append("    ENTEXAMYEAR = '" + _param._year + "' ");
        if (_param._output.equals("1") || _param._output.equals("2")) {
            stb.append("    AND TESTDIV = '" + _param._output + "' ");
        }
        stb.append("    AND COURSECD || MAJORCD || EXAMCOURSECD = '" + course + "' ");
        stb.append("    AND WISHNO = '1' ");
        stb.append("ORDER BY ");
        stb.append("    DESIREDIV DESC ");

//      log.debug(stb);
        return stb.toString();
    }

    /**
     *  下段志願コースを抽出
     */
    private String sqlDesireGetLower(final String course, final int gyo) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT DISTINCT ");
        stb.append("    DESIREDIV, ");
        stb.append("    WISHNO,COURSECD || MAJORCD || EXAMCOURSECD AS COURSE ");
        stb.append("FROM ");
        stb.append("    ENTEXAM_WISHDIV_MST ");
        stb.append("WHERE ");
        stb.append("    ENTEXAMYEAR = '" + _param._year + "' ");
        if (_param._output.equals("1") || _param._output.equals("2")) {
            stb.append("    AND TESTDIV = '" + _param._output + "' ");
        }
        stb.append("    AND DESIREDIV IN (SELECT DISTINCT ");
        stb.append("                         DESIREDIV ");
        stb.append("                     FROM ");
        stb.append("                         ENTEXAM_WISHDIV_MST ");
        stb.append("                     WHERE ");
        stb.append("                         ENTEXAMYEAR = '" + _param._year + "' ");
        if (_param._output.equals("1") || _param._output.equals("2")) {
            stb.append("                         AND TESTDIV = '" + _param._output + "' ");
        }

        if (gyo < 18) {
            stb.append("                         AND COURSECD || MAJORCD || EXAMCOURSECD = '" + course + "' ");
        }
        stb.append("                         AND WISHNO = '1') ");
        stb.append("ORDER BY ");
        stb.append("    DESIREDIV DESC, ");
        stb.append("    WISHNO ");

//      log.debug(stb);
        return stb.toString();
    }

    /**
     *  上段明細データを抽出
     */
    // CSOFF: ExecutableStatementCount
    // CSOFF: MethodLength
    private String sqlUpperMeisaiGet(final String desirediv, final int gyo) {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH EXAMBASE_ALL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "BASEALL", "", "UPPER"));

        stb.append("), EXAMBASE_BOY AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "BASEBOY", "1", "UPPER"));

        stb.append("), EXAMBASE_GIL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "BASEGIL", "2", "UPPER"));

        stb.append("), EXAMBASE_JUKEN_ALL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "JUKENALL", "", "UPPER"));
        stb.append("    AND JUDGEMENT <> '" + JudgeMent.MIJUKEN + "' ");

        stb.append("), EXAMBASE_JUKEN_BOY AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "JUKENBOY", "1", "UPPER"));
        stb.append("    AND JUDGEMENT <> '" + JudgeMent.MIJUKEN + "' ");

        stb.append("), EXAMBASE_JUKEN_GIL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "JUKENGIL", "2", "UPPER"));
        stb.append("    AND JUDGEMENT <> '" + JudgeMent.MIJUKEN + "' ");

        stb.append("), EXAMBASE_MIJUKEN_ALL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "MIJUKENALL", "", "UPPER"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.MIJUKEN + "' ");

        stb.append("), EXAMBASE_MIJUKEN_BOY AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "MIJUKENBOY", "1", "UPPER"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.MIJUKEN + "' ");

        stb.append("), EXAMBASE_MIJUKEN_GIL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "MIJUKENGIL", "2", "UPPER"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.MIJUKEN + "' ");

        stb.append("), EXAMBASE_UNPASS_ALL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "UNPASSALL", "", "UPPER"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.UNPASS + "' ");

        stb.append("), EXAMBASE_UNPASS_BOY AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "UNPASSBOY", "1", "UPPER"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.UNPASS + "' ");

        stb.append("), EXAMBASE_UNPASS_GIL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "UNPASSGIL", "2", "UPPER"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.UNPASS + "' ");

        stb.append(") ");

        stb.append("SELECT ");
        stb.append("    t1.FLG, ");
        stb.append("    BASEALL, ");
        stb.append("    BASEBOY, ");
        stb.append("    BASEGIL, ");
        stb.append("    JUKENALL, ");
        stb.append("    JUKENBOY, ");
        stb.append("    JUKENGIL, ");
        stb.append("    MIJUKENALL, ");
        stb.append("    MIJUKENBOY, ");
        stb.append("    MIJUKENGIL, ");
        stb.append("    UNPASSALL, ");
        stb.append("    UNPASSBOY, ");
        stb.append("    UNPASSGIL ");
        stb.append("FROM ");
        stb.append("    EXAMBASE_ALL t1 ");
        stb.append("    LEFT JOIN EXAMBASE_BOY t2 ON t1.FLG = t2.FLG ");
        stb.append("    LEFT JOIN EXAMBASE_GIL t3 ON t1.FLG = t3.FLG ");
        stb.append("    LEFT JOIN EXAMBASE_JUKEN_ALL t4 ON t1.FLG = t4.FLG ");
        stb.append("    LEFT JOIN EXAMBASE_JUKEN_BOY t5 ON t1.FLG = t5.FLG ");
        stb.append("    LEFT JOIN EXAMBASE_JUKEN_GIL t6 ON t1.FLG = t6.FLG ");
        stb.append("    LEFT JOIN EXAMBASE_MIJUKEN_ALL t7 ON t1.FLG = t7.FLG ");
        stb.append("    LEFT JOIN EXAMBASE_MIJUKEN_BOY t8 ON t1.FLG = t8.FLG ");
        stb.append("    LEFT JOIN EXAMBASE_MIJUKEN_GIL t9 ON t1.FLG = t9.FLG ");
        stb.append("    LEFT JOIN EXAMBASE_UNPASS_ALL t10 ON t1.FLG = t10.FLG ");
        stb.append("    LEFT JOIN EXAMBASE_UNPASS_BOY t11 ON t1.FLG = t11.FLG ");
        stb.append("    LEFT JOIN EXAMBASE_UNPASS_GIL t12 ON t1.FLG = t12.FLG ");

//      log.debug(stb);
        return stb.toString();
    }
    // CSON: MethodLength
    // CSON: ExecutableStatementCount

    /**
     * 明細データ取得(共通)
     */
    private String makeExamSql(
            final String desirediv,
            final int gyo,
            final String name,
            final String sex,
            final String callDiv
    ) {
        final StringBuffer stb = new StringBuffer();

        stb.append("SELECT ");
        stb.append("    '" + gyo + "' AS FLG, ");
        stb.append("    COUNT(*) AS " + name + " ");
        stb.append("FROM ");
        stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
        stb.append("WHERE ");
        stb.append("    ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("    AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ");
        if (_param._output.equals("1") || _param._output.equals("2")) {
            stb.append("    AND TESTDIV = '" + _param._output + "' ");
            stb.append("    AND EXAMNO NOT BETWEEN '3000' AND '3999' ");
        } else if (_param._output.equals("3")) {
            stb.append("    AND EXAMNO BETWEEN '3000' AND '3999' ");
        }
        if (callDiv.equals("UPPER")) {
            stb.append("    AND DESIREDIV IN " + desirediv + " ");
        } else {
            stb.append("    AND DESIREDIV || SUC_COURSECD || SUC_MAJORCD || SUC_COURSECODE IN " + desirediv + " ");
        }
        if (!sex.equals("")) {
            stb.append("    AND SEX = '" + sex + "' ");
        }

        return stb.toString();
    }

    /**
     *  下段明細データを抽出
     */
    // CSOFF: ExecutableStatementCount
    // CSOFF: MethodLength
    private String sqlLowerMeisaiGet(
            final String desirediv,
            final int gyo,
            final String desirecourse
    ) {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH PASS_ALL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "PASSALL", "", "LOWER"));
        stb.append("    AND JUDGEMENT <= '" + JudgeMent.SUISEN + "' ");

        stb.append("), PASS_BOY AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "PASSBOY", "1", "LOWER"));
        stb.append("    AND JUDGEMENT <= '" + JudgeMent.SUISEN + "' ");

        stb.append("), PASS_GIL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "PASSGIL", "2", "LOWER"));
        stb.append("    AND JUDGEMENT <= '" + JudgeMent.SUISEN + "' ");

        stb.append("), ADDPASS_ALL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "ADDPASSALL", "", "LOWER"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.ADDPASS + "' ");

        stb.append("), ADDPASS_BOY AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "ADDPASSBOY", "1", "LOWER"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.ADDPASS + "' ");

        stb.append("), ADDPASS_GIL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "ADDPASSGIL", "2", "LOWER"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.ADDPASS + "' ");

        stb.append("), TOTALPASS_ALL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "TOTALPASSALL", "", "LOWER"));
        stb.append("    AND JUDGEMENT <= '" + JudgeMent.ADDPASS + "' ");

        stb.append("), TOTALPASS_BOY AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "TOTALPASSBOY", "1", "LOWER"));
        stb.append("    AND JUDGEMENT <= '" + JudgeMent.ADDPASS + "' ");

        stb.append("), TOTALPASS_GIL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "TOTALPASSGIL", "2", "LOWER"));
        stb.append("    AND JUDGEMENT <= '" + JudgeMent.ADDPASS + "' ");

        stb.append("), UNPROCEDURE_ALL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "UNPROCEDUREALL", "", "LOWER"));
        stb.append("    AND JUDGEMENT <= '" + JudgeMent.ADDPASS + "' ");
        stb.append("    AND ((VALUE(PROCEDUREDIV,'0') < '2') OR (PROCEDUREDIV = '2' AND ENTDIV IS NULL)) ");

        stb.append("), UNPROCEDURE_BOY AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "UNPROCEDUREBOY", "1", "LOWER"));
        stb.append("    AND JUDGEMENT <= '" + JudgeMent.ADDPASS + "' ");
        stb.append("    AND ((VALUE(PROCEDUREDIV,'0') < '2') OR (PROCEDUREDIV = '2' AND ENTDIV IS NULL)) ");

        stb.append("), UNPROCEDURE_GIL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "UNPROCEDUREGIL", "2", "LOWER"));
        stb.append("    AND JUDGEMENT <= '" + JudgeMent.ADDPASS + "' ");
        stb.append("    AND ((VALUE(PROCEDUREDIV,'0') < '2') OR (PROCEDUREDIV = '2' AND ENTDIV IS NULL)) ");

        stb.append("), PROCEDURE_ALL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "PROCEDUREALL", "", "LOWER"));
        stb.append("    AND JUDGEMENT <= '" + JudgeMent.ADDPASS + "' ");
        stb.append("    AND PROCEDUREDIV = '2' AND ENTDIV <= '2' ");

        stb.append("), PROCEDURE_BOY AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "PROCEDUREBOY", "1", "LOWER"));
        stb.append("    AND JUDGEMENT <= '" + JudgeMent.ADDPASS + "' ");
        stb.append("    AND PROCEDUREDIV = '2' AND ENTDIV <= '2' ");

        stb.append("), PROCEDURE_GIL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "PROCEDUREGIL", "2", "LOWER"));
        stb.append("    AND JUDGEMENT <= '" + JudgeMent.ADDPASS + "' ");
        stb.append("    AND PROCEDUREDIV = '2' AND ENTDIV <= '2' ");

        stb.append("), UNENT_ALL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "UNENTALL", "", "LOWER"));
        stb.append("    AND JUDGEMENT <= '" + JudgeMent.ADDPASS + "' ");
        stb.append("    AND ENTDIV = '1' AND PROCEDUREDIV = '2' ");

        stb.append("), UNENT_BOY AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "UNENTBOY", "1", "LOWER"));
        stb.append("    AND JUDGEMENT <= '" + JudgeMent.ADDPASS + "' ");
        stb.append("    AND ENTDIV = '1' AND PROCEDUREDIV = '2' ");

        stb.append("), UNENT_GIL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "UNENTGIL", "2", "LOWER"));
        stb.append("    AND JUDGEMENT <= '" + JudgeMent.ADDPASS + "' ");
        stb.append("    AND ENTDIV = '1' AND PROCEDUREDIV = '2' ");

        stb.append("), ENT_ALL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "ENTALL", "", "LOWER"));
        stb.append("    AND ENTDIV = '2' AND PROCEDUREDIV = '2' ");

        stb.append("), ENT_BOY AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "ENTBOY", "1", "LOWER"));
        stb.append("    AND ENTDIV = '2' AND PROCEDUREDIV = '2' ");

        stb.append("), ENT_GIL AS ( ");
        stb.append(makeExamSql(desirediv, gyo, "ENTGIL", "2", "LOWER"));
        stb.append("    AND ENTDIV = '2' AND PROCEDUREDIV = '2' ");
        stb.append(") ");

        stb.append("SELECT ");
        stb.append("    t1.FLG, ");
        stb.append("    PASSALL, ");
        stb.append("    PASSBOY, ");
        stb.append("    PASSGIL, ");
        stb.append("    ADDPASSALL, ");
        stb.append("    ADDPASSBOY, ");
        stb.append("    ADDPASSGIL, ");
        stb.append("    TOTALPASSALL, ");
        stb.append("    TOTALPASSBOY, ");
        stb.append("    TOTALPASSGIL, ");
        stb.append("    UNPROCEDUREALL, ");
        stb.append("    UNPROCEDUREBOY, ");
        stb.append("    UNPROCEDUREGIL, ");
        stb.append("    PROCEDUREALL, ");
        stb.append("    PROCEDUREBOY, ");
        stb.append("    PROCEDUREGIL, ");
        stb.append("    UNENTALL, ");
        stb.append("    UNENTBOY, ");
        stb.append("    UNENTGIL, ");
        stb.append("    ENTALL, ");
        stb.append("    ENTBOY, ");
        stb.append("    ENTGIL ");
        stb.append("FROM ");
        stb.append("    PASS_ALL t1 ");
        stb.append("    LEFT JOIN PASS_BOY t2 ON t1.FLG = t2.FLG ");
        stb.append("    LEFT JOIN PASS_GIL t3 ON t1.FLG = t3.FLG ");
        stb.append("    LEFT JOIN ADDPASS_ALL t4 ON t1.FLG = t4.FLG ");
        stb.append("    LEFT JOIN ADDPASS_BOY t5 ON t1.FLG = t5.FLG ");
        stb.append("    LEFT JOIN ADDPASS_GIL t6 ON t1.FLG = t6.FLG ");
        stb.append("    LEFT JOIN TOTALPASS_ALL t7 ON t1.FLG = t7.FLG ");
        stb.append("    LEFT JOIN TOTALPASS_BOY t8 ON t1.FLG = t8.FLG ");
        stb.append("    LEFT JOIN TOTALPASS_GIL t9 ON t1.FLG = t9.FLG ");
        stb.append("    LEFT JOIN UNPROCEDURE_ALL t10 ON t1.FLG = t10.FLG ");
        stb.append("    LEFT JOIN UNPROCEDURE_BOY t11 ON t1.FLG = t11.FLG ");
        stb.append("    LEFT JOIN UNPROCEDURE_GIL t12 ON t1.FLG = t12.FLG ");
        stb.append("    LEFT JOIN PROCEDURE_ALL t13 ON t1.FLG = t13.FLG ");
        stb.append("    LEFT JOIN PROCEDURE_BOY t14 ON t1.FLG = t14.FLG ");
        stb.append("    LEFT JOIN PROCEDURE_GIL t15 ON t1.FLG = t15.FLG ");
        stb.append("    LEFT JOIN UNENT_ALL t16 ON t1.FLG = t16.FLG ");
        stb.append("    LEFT JOIN UNENT_BOY t17 ON t1.FLG = t17.FLG ");
        stb.append("    LEFT JOIN UNENT_GIL t18 ON t1.FLG = t18.FLG ");
        stb.append("    LEFT JOIN ENT_ALL t22 ON t1.FLG = t22.FLG ");
        stb.append("    LEFT JOIN ENT_BOY t23 ON t1.FLG = t23.FLG ");
        stb.append("    LEFT JOIN ENT_GIL t24 ON t1.FLG = t24.FLG ");

        return stb.toString();
    }
    // CSON: MethodLength
    // CSON: ExecutableStatementCount

    /**
     *  下段明細データを抽出(繰上用)
     */
    // CSOFF: ExecutableStatementCount
    // CSOFF: MethodLength
    private String sqlLowerKuriageMeisaiGet(
            final String desirediv,
            final int gyo,
            final String desirecourse
    ) {
        final StringBuffer stb = new StringBuffer();

        stb.append("WITH COURSE_HIST AS ( ");
        stb.append(" SELECT ");
        stb.append("    EXAMNO AS HIST_EXAM ");
        stb.append(" FROM ");
        stb.append("    ENTEXAM_COURSE_HIST_DAT ");
        stb.append(" WHERE ");
        stb.append("    ENTEXAMYEAR = '" + _param._year + "' ");
        if (_param._output.equals("1") || _param._output.equals("2")) {
            stb.append("    AND TESTDIV = '" + _param._output + "' ");
            stb.append("    AND EXAMNO NOT BETWEEN '3000' AND '3999' ");
        } else if (_param._output.equals("3")) {
            stb.append("    AND EXAMNO BETWEEN '3000' AND '3999' ");
        }
        stb.append("    AND SEQ = 1 ");
        stb.append("    AND JUDGEMENT <= '4' ");
        if (gyo == 22) {
            stb.append("    AND SUC_COURSECD || SUC_MAJORCD || SUC_COURSECODE <= '" + getHistCourse(gyo) + "' ");
        } else {
            stb.append("    AND SUC_COURSECD || SUC_MAJORCD || SUC_COURSECODE = '" + getHistCourse(gyo) + "' ");
        }

        final String courseWhere = "    AND EXAMNO IN (SELECT HIST_EXAM FROM COURSE_HIST) ";

        stb.append("), UPPASS_ALL AS ( ");
        stb.append(makeExamSql(desirecourse, gyo, "UPPASSALL", "", "LOWER2"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.CHANGEPASS + "' ");
        stb.append(courseWhere);

        stb.append("), UPPASS_BOY AS ( ");
        stb.append(makeExamSql(desirecourse, gyo, "UPPASSBOY", "1", "LOWER2"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.CHANGEPASS + "' ");
        stb.append(courseWhere);

        stb.append("), UPPASS_GIL AS ( ");
        stb.append(makeExamSql(desirecourse, gyo, "UPPASSGIL", "2", "LOWER2"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.CHANGEPASS + "' ");
        stb.append(courseWhere);

        stb.append("), UNPRO_UPPASS_ALL AS ( ");
        stb.append(makeExamSql(desirecourse, gyo, "UNPRO_UPPASSALL", "", "LOWER2"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.CHANGEPASS + "' ");
        stb.append(courseWhere);
        stb.append("    AND ((VALUE(PROCEDUREDIV,'0') < '2') OR (PROCEDUREDIV = '2' AND ENTDIV IS NULL)) ");

        stb.append("), UNPRO_UPPASS_BOY AS ( ");
        stb.append(makeExamSql(desirecourse, gyo, "UNPRO_UPPASSBOY", "1", "LOWER2"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.CHANGEPASS + "' ");
        stb.append(courseWhere);
        stb.append("    AND ((VALUE(PROCEDUREDIV,'0') < '2') OR (PROCEDUREDIV = '2' AND ENTDIV IS NULL)) ");

        stb.append("), UNPRO_UPPASS_GIL AS ( ");
        stb.append(makeExamSql(desirecourse, gyo, "UNPRO_UPPASSGIL", "2", "LOWER2"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.CHANGEPASS + "' ");
        stb.append(courseWhere);
        stb.append("    AND ((VALUE(PROCEDUREDIV,'0') < '2') OR (PROCEDUREDIV = '2' AND ENTDIV IS NULL)) ");

        stb.append("), PRO_UPPASS_ALL AS ( ");
        stb.append(makeExamSql(desirecourse, gyo, "PRO_UPPASSALL", "", "LOWER2"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.CHANGEPASS + "' ");
        stb.append(courseWhere);
        stb.append("    AND PROCEDUREDIV = '2' AND ENTDIV <= '2' ");

        stb.append("), PRO_UPPASS_BOY AS ( ");
        stb.append(makeExamSql(desirecourse, gyo, "PRO_UPPASSBOY", "1", "LOWER2"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.CHANGEPASS + "' ");
        stb.append(courseWhere);
        stb.append("    AND PROCEDUREDIV = '2' AND ENTDIV <= '2' ");

        stb.append("), PRO_UPPASS_GIL AS ( ");
        stb.append(makeExamSql(desirecourse, gyo, "PRO_UPPASSGIL", "2", "LOWER2"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.CHANGEPASS + "' ");
        stb.append(courseWhere);
        stb.append("    AND PROCEDUREDIV = '2' AND ENTDIV <= '2' ");

        stb.append("), UNENT_UPPASS_ALL AS ( ");
        stb.append(makeExamSql(desirecourse, gyo, "UNENT_UPPASSALL", "", "LOWER2"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.CHANGEPASS + "' ");
        stb.append(courseWhere);
        stb.append("    AND ENTDIV = '1' AND PROCEDUREDIV = '2' ");

        stb.append("), UNENT_UPPASS_BOY AS ( ");
        stb.append(makeExamSql(desirecourse, gyo, "UNENT_UPPASSBOY", "1", "LOWER2"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.CHANGEPASS + "' ");
        stb.append(courseWhere);
        stb.append("    AND ENTDIV = '1' AND PROCEDUREDIV = '2' ");

        stb.append("), UNENT_UPPASS_GIL AS ( ");
        stb.append(makeExamSql(desirecourse, gyo, "UNENT_UPPASSGIL", "2", "LOWER2"));
        stb.append("    AND JUDGEMENT = '" + JudgeMent.CHANGEPASS + "' ");
        stb.append(courseWhere);
        stb.append("    AND ENTDIV = '1' AND PROCEDUREDIV = '2' ");
        stb.append(") ");

        stb.append("SELECT ");
        stb.append("    t1.FLG, ");
        stb.append("    UPPASSALL, ");
        stb.append("    UPPASSBOY, ");
        stb.append("    UPPASSGIL, ");
        stb.append("    UNPRO_UPPASSALL, ");
        stb.append("    UNPRO_UPPASSBOY, ");
        stb.append("    UNPRO_UPPASSGIL, ");
        stb.append("    PRO_UPPASSALL, ");
        stb.append("    PRO_UPPASSBOY, ");
        stb.append("    PRO_UPPASSGIL, ");
        stb.append("    UNENT_UPPASSALL, ");
        stb.append("    UNENT_UPPASSBOY, ");
        stb.append("    UNENT_UPPASSGIL ");
        stb.append("FROM ");
        stb.append("    UPPASS_ALL t1 ");
        stb.append("    LEFT JOIN UPPASS_BOY t20 ON t1.FLG = t20.FLG ");
        stb.append("    LEFT JOIN UPPASS_GIL t21 ON t1.FLG = t21.FLG ");
        stb.append("    LEFT JOIN UNPRO_UPPASS_ALL u1 ON t1.FLG = u1.FLG ");
        stb.append("    LEFT JOIN UNPRO_UPPASS_BOY u2 ON t1.FLG = u2.FLG ");
        stb.append("    LEFT JOIN UNPRO_UPPASS_GIL u3 ON t1.FLG = u3.FLG ");
        stb.append("    LEFT JOIN PRO_UPPASS_ALL u4 ON t1.FLG = u4.FLG ");
        stb.append("    LEFT JOIN PRO_UPPASS_BOY u5 ON t1.FLG = u5.FLG ");
        stb.append("    LEFT JOIN PRO_UPPASS_GIL u6 ON t1.FLG = u6.FLG ");
        stb.append("    LEFT JOIN UNENT_UPPASS_ALL u7 ON t1.FLG = u7.FLG ");
        stb.append("    LEFT JOIN UNENT_UPPASS_BOY u8 ON t1.FLG = u8.FLG ");
        stb.append("    LEFT JOIN UNENT_UPPASS_GIL u9 ON t1.FLG = u9.FLG ");

        return stb.toString();
    }
    // CSON: MethodLength
    // CSON: ExecutableStatementCount

    private String getHistCourse(final int gyo) {
        String rtnCourse = COURSE_HYOUJUN;
        if (gyo == 3 || gyo == 5 || gyo == 9 || gyo == 19) {
            rtnCourse = COURSE_TOKUSIN;
        }
        return rtnCourse;
    }

}
