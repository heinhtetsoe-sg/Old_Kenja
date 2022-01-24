// kanji=漢字
/*
 * $Id: 3e24a9d9680f3292c61045cb04c105c78e067fd3 $
 *
 * 作成日: 2007/11/26
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
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

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * Ａ値順位表
 * @author nakamoto
 * @version $Id: 3e24a9d9680f3292c61045cb04c105c78e067fd3 $
 */
public class KNJD643 {

    private static final Log log = LogFactory.getLog(KNJD643.class);

    public String FORM_FILE = "KNJD643.frm";

    public int MAX_RETU = 42;
    public int MAX_GYOU = 50;

    Param _param;

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
        dumpParam(request);
        _param = createParam(request);

        final Vrw32alp svf = new Vrw32alp();    //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        try {
            init(response, svf);

            // DB接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // 印字メイン
            boolean hasData = false;   // 該当データ無しフラグ
            hasData = printMain(db2, svf);
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            log.debug("hasData = " + hasData);
        } finally {
            close(svf, db2);
        }
    }

    /** 印刷処理メイン */
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws Exception {
        boolean rtnflg = false;

        svf.VrSetForm(FORM_FILE, 4);
        log.debug("印刷するフォーム:" + FORM_FILE);
        
        final List bukaheaders = createBukaHeaders(db2);
        log.debug("指定学部・学科数=" + bukaheaders.size());

        final List bukanames = createBukanames(db2);
        log.debug("学部・学科列数=" + bukanames.size());
        
        for (final Iterator it = bukaheaders.iterator(); it.hasNext();) {
            
            final Bukaheader bukaheader = (Bukaheader) it.next();

            final List students = createStudents(db2, bukaheader);
            log.debug("生徒数=" + students.size());

            int pagecnt = 0;
            int gyoucnt = 0;
            final Map studentGyo = new HashMap();
            final StringBuffer studentIn = new StringBuffer();
            String seq = "";
            for (final Iterator itS = students.iterator(); itS.hasNext();) {
                if (gyoucnt == MAX_GYOU) {
                    pagecnt++;
                    printBukaheader(svf, bukaheader, pagecnt);

                    rtnflg = printRetu(db2, svf, rtnflg, bukanames, studentGyo, studentIn.toString());

                    studentGyo.clear();
                    int mojisuu = studentIn.length();
                    studentIn.delete(0, mojisuu);
                    seq = "";
                    printStudentBlank(svf);
                    gyoucnt = 0;
                }
                final Student student = (Student) itS.next();
                gyoucnt++;
                printStudent(svf, student, gyoucnt);
                studentGyo.put(student._schregno, String.valueOf(gyoucnt));
                studentIn.append(seq + "'" + student._schregno + "'");
                seq = ",";
            }

            if (0 < gyoucnt) {
                pagecnt++;
                printBukaheader(svf, bukaheader, pagecnt);

                rtnflg = printRetu(db2, svf, rtnflg, bukanames, studentGyo, studentIn.toString());

                studentGyo.clear();
                int mojisuu = studentIn.length();
                studentIn.delete(0, mojisuu);
                seq = "";
                printStudentBlank(svf);
                gyoucnt = 0;
            }
            
        }
        
        return rtnflg;
    }

    private boolean printRetu(
            final DB2UDB db2,
            final Vrw32alp svf, 
            boolean rtnflg, 
            final List bukanames, 
            final Map studentGyo,
            final String studentIn
    ) throws Exception {
        int retucnt = 0;
        int kaCount = 0;
        int setCount = 0;
        int setFlg = 0;
        String bucd = "";
        for (final Iterator it = bukanames.iterator(); it.hasNext();) {
//            if (retucnt == MAX_RETU) break; // MAX_RETUまで表示
            final Bukaname bukaname = (Bukaname) it.next();
            printWishRank(db2, svf, bukaname, studentIn, studentGyo);
//          ---------------------------------------
//          printBukaname(svf, bukaname);
//            log.debug(bukaname._buCd + "：" + bukaname.getBuAbbv() + "、学部文字数：" + bukaname.getBuAbbvLen() + "、学科数：" + bukaname._kaCount + " >>> " + bukaname._kaCd + "：" + bukaname.getKaAbbv());
            if (!bukaname.isEqualBucd(bucd)) {
                bucd = bukaname._buCd;
                kaCount = 0;
                setCount = 0;
                setFlg = 0;
            }
            svf.VrsOut("CLASSCD", bukaname._buCd);
            svf.VrsOut("KA_NAME2_1", bukaname.getKaAbbv());
            if (bukaname.isOverKaCount()) {
                if (bukaname.isSeiKeisan1()) {
                    if (bukaname.isPatturn1Name1(setCount, kaCount, setFlg)) {
                        svf.VrsOut("BU_NAME2_1", bukaname.getBuAbbv(setCount));
                        setCount++;
                        setFlg = 1;
                    }
                    if (bukaname.isPatturn1Name2(setCount, kaCount, setFlg)) {
                        svf.VrsOut("BU_NAME2_2", bukaname.getBuAbbv(setCount));
                        setCount++;
                        setFlg = 2;
                    }
//                    log.debug("パターン１");
                } else {
                    if (bukaname.isPatturn2Name1(setCount, kaCount)) {
                        svf.VrsOut("BU_NAME2_1", bukaname.getBuAbbv(setCount));
                        setCount++;
                    }
                    if (bukaname.isPatturn2Name2(setCount, kaCount)) {
                        svf.VrsOut("BU_NAME2_2", bukaname.getBuAbbv(setCount));
                        setCount++;
                    }
//                    log.debug("パターン２");
                }
            } else {
                svf.VrsOut("BU_NAME2_1", bukaname.getBuAbbv(setCount));
                setCount++;
                svf.VrsOut("BU_NAME2_2", bukaname.getBuAbbv(setCount));
                setCount++;
//                log.debug("パターン３");
            }
//          ---------------------------------------
            svf.VrEndRecord();
            retucnt++;
            kaCount++;
            rtnflg = true;
        }
        while (retucnt < MAX_RETU) {
            printBukanameBlank(svf, retucnt);
            svf.VrEndRecord();
            retucnt++;
        }
        return rtnflg;
    }

    private void printWishRank(final DB2UDB db2, final Vrw32alp svf, final Bukaname bukaname, final String studentIn, final Map studentGyo) throws SQLException {
        final String sql = sqlWishRank(bukaname, studentIn);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("CHOICE_NO" + studentGyo.get(rs.getString("SCHREGNO")), rs.getString("WISH_RANK") );
            }
        } catch (final Exception ex) {
            log.error("推薦希望順位のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlWishRank(final Bukaname bukaname, final String studentIn) {
        return " SELECT "
                + "     SCHREGNO, "
                + "     WISH_RANK "
                + " FROM "
                + "     SCHREG_RECOMMENDATION_WISH_DAT "
                + " WHERE "
                + "     YEAR = '" + _param._year + "' AND "
                + "     SCHREGNO IN (" + studentIn + ") AND "
                + "     BU_CD = '" + bukaname._buCd + "' AND "
                + "     KA_CD = '" + bukaname._kaCd + "' ";
    }

    private List createBukaHeaders(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();

        final StringBuffer inState = new StringBuffer();
        String seq = "";
        for (int i = 0; i < _param._bukaCd.length; i++) {
            if (_param._bukaCd[i].equals("991")) {
                final String bukaCd = _param._bukaCd[i];
                final String bukaName = "文系全体";
                final Bukaheader bukaheader = new Bukaheader(bukaCd, bukaName);
                rtn.add(bukaheader);
            } else if (_param._bukaCd[i].equals("992")) {
                final String bukaCd = _param._bukaCd[i];
                final String bukaName = "理系全体";
                final Bukaheader bukaheader = new Bukaheader(bukaCd, bukaName);
                rtn.add(bukaheader);
            } else if (_param._bukaCd[i].equals("999")) {
                final String bukaCd = _param._bukaCd[i];
                final String bukaName = "全体";
                final Bukaheader bukaheader = new Bukaheader(bukaCd, bukaName);
                rtn.add(bukaheader);

            } else {
                inState.append(seq + "'" + _param._bukaCd[i] + "'");
                seq = ",";
            }
        }

        if (seq.equals("")) return rtn;

        final String sql = sqlBukaheaders(inState.toString());
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String bukaCd = rs.getString("BUKA_CD");
                final String bukaName = rs.getString("BUKA_NAME");

                final Bukaheader bukaheader = new Bukaheader(
                        bukaCd,
                        bukaName
                );

                rtn.add(bukaheader);
            }
        } catch (final Exception ex) {
            log.error("指定学部・学科名称のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String sqlBukaheaders(final String bukacd) {
        return " SELECT "
                + "     BU_CD || KA_CD AS BUKA_CD, "
                + "     BU_NAME || '　' || KA_NAME AS BUKA_NAME "
                + " FROM "
                + "     COLLEGE_RECOMMENDATION_DAT "
                + " WHERE "
                + "     YEAR='" + _param._year + "' AND "
                + "     BU_CD || KA_CD IN (" + bukacd + ") "
                + " ORDER BY "
                + "     BU_CD, "
                + "     KA_CD ";
    }

    private void printBukaheader(
            final Vrw32alp svf,
            final Bukaheader bukaheader,
            int pagecnt
    ) {
        svf.VrsOut("BU_NAME1", bukaheader.getBukaName());
        svf.VrsOut("NENDO", _param._year + "年度");
        svf.VrsOut("GRADE", String.valueOf(_param.getGrade()) + "年");
        svf.VrsOut("CHOICE_RANK", _param._wishRank);
        svf.VrsOut("DATE", _param._date);
        svf.VrsOut("PAGE", String.valueOf(pagecnt) );
    }

    private List createBukanames(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlBukanames();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schoolCd = rs.getString("SCHOOL_CD");
                final String buCd = rs.getString("BU_CD");
                final String kaCd = rs.getString("KA_CD");
                final String buName = rs.getString("BU_NAME");
                final String buAbbv = rs.getString("BU_ABBV");
                final String kaName = rs.getString("KA_NAME");
                final String kaAbbv = rs.getString("KA_ABBV");
                final String div = rs.getString("DIV");
                final int kaCount = rs.getInt("KA_COUNT");

                final Bukaname bukaname = new Bukaname(
                        schoolCd,
                        buCd,
                        kaCd,
                        buName,
                        buAbbv,
                        kaName,
                        kaAbbv,
                        div,
                        kaCount
                );

                rtn.add(bukaname);
            }
        } catch (final Exception ex) {
            log.error("学部・学科略称のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String sqlBukanames() {
        return " SELECT "
                + "     SCHOOL_CD, "
                + "     T1.BU_CD, "
                + "     KA_CD, "
                + "     BU_NAME, "
                + "     BU_ABBV, "
                + "     KA_NAME, "
                + "     KA_ABBV, "
                + "     DIV, "
                + "     KA_COUNT "
                + " FROM "
                + "     COLLEGE_RECOMMENDATION_DAT T1 "
                + "     LEFT JOIN ( "
                + "         SELECT "
                + "             BU_CD, "
                + "             COUNT(*) AS KA_COUNT "
                + "         FROM "
                + "             COLLEGE_RECOMMENDATION_DAT "
                + "         WHERE "
                + "             YEAR='" + _param._year + "' "
                + "         GROUP BY "
                + "             BU_CD "
                + "     ) T2 ON T2.BU_CD = T1.BU_CD "
                + " WHERE "
                + "     YEAR='" + _param._year + "' "
                + " ORDER BY "
                + "     DIV, "
                + "     T1.BU_CD, "
                + "     KA_CD ";
    }

    private void printBukaname(
            final Vrw32alp svf,
            final Bukaname bukaname
    ) {
        svf.VrsOut("CLASSCD", bukaname._buCd);
        svf.VrsOut("BU_NAME2_1", bukaname.getBuAbbv());
        svf.VrsOut("BU_NAME2_2", bukaname.getBuAbbv());
        svf.VrsOut("KA_NAME2_1", bukaname.getKaAbbv());
    }

    private void printBukanameBlank(
            final Vrw32alp svf,
            final int retucnt
    ) {
        svf.VrsOut("CLASSCD", String.valueOf(retucnt));
        svf.VrsOut("BU_NAME2_1", "");
        svf.VrsOut("BU_NAME2_2", "");
        svf.VrsOut("KA_NAME2_1", "");
    }

    private List createStudents(final DB2UDB db2, final Bukaheader bukaheader) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlStudents(bukaheader);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String rank = rs.getString("RANK_A");
                final String score = rs.getString("SCORE_A");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String sexName = rs.getString("SEX_NAME");
                final String heigan = rs.getString("HEIGAN");

                final Student student = new Student(
                        schregno,
                        rank,
                        score,
                        hrClass,
                        attendno,
                        name,
                        sexName,
                        heigan
                );

                rtn.add(student);
            }
        } catch (final Exception ex) {
            log.error("生徒のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String sqlStudents(final Bukaheader bukaheader) {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHNO AS ( ");
        stb.append("     SELECT  T1.SCHREGNO, ");
        stb.append("             T1.GRADE, ");
        stb.append("             T1.HR_CLASS, ");
        stb.append("             T1.ATTENDNO, ");
        stb.append("             T2.NAME, ");
        stb.append("             N1.NAME2 AS SEX_NAME ");
        stb.append("       FROM  SCHREG_REGD_DAT T1, ");
        stb.append("             SCHREG_BASE_MST T2 ");
        stb.append("             LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T2.SEX ");
        stb.append("      WHERE  T1.YEAR = '" + _param._year + "' ");
        stb.append("        AND  T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("        AND  T1.GRADE = '" + _param._grade + "' ");
        stb.append("        AND  T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     ) ");
        if (bukaheader.isBunRiAll()) {
            stb.append(" , SCH_SCORE AS ( ");
            stb.append("     SELECT  T3.SCHREGNO, ");
            stb.append("             T3.SCORE3 AS SCORE_A, ");
            stb.append("             RANK() OVER (ORDER BY T3.SCORE3 DESC) AS RANK_A ");
            stb.append("       FROM  RECORD_MOCK_RANK_DAT T3 ");
            stb.append("      WHERE  T3.YEAR = '" + _param._year + "' ");
            stb.append("        AND  T3.DATA_DIV = '1' ");
            stb.append("        AND  T3.COURSE_DIV = '" + bukaheader.getCouseDiv() + "' ");// 1:文系,2:理系,3:学年
            stb.append("        AND  T3.SUBCLASSCD = '333333' ");
            stb.append("        AND  T3.GRADE = '" + _param._grade + "' ");
            stb.append("     ) ");
        } else {
            stb.append(" , SCH_RECOM_WISH AS ( ");
            stb.append("     SELECT  SCHREGNO, ");
            stb.append("             WISH_RANK, ");
            stb.append("             SCHOOL_CD, ");
            stb.append("             BU_CD, ");
            stb.append("             KA_CD, ");
            stb.append("             RECOMMENDATION_FLG ");
            stb.append("       FROM  SCHREG_RECOMMENDATION_WISH_DAT ");
            stb.append("      WHERE  YEAR = '" + _param._year + "' ");
            stb.append("        AND  WISH_RANK = '" + _param._wishRank + "' ");
            stb.append("        AND  BU_CD || KA_CD = '" + bukaheader.getBukaCd() + "' ");
            if (!_param.isWishRank1()) {
                stb.append("    AND  SCHREGNO NOT IN ( ");
                stb.append("             SELECT  SCHREGNO ");
                stb.append("               FROM  SCHREG_RECOMMENDATION_WISH_DAT ");
                stb.append("              WHERE  YEAR = '" + _param._year + "' ");
                stb.append("                AND  RECOMMENDATION_FLG = '1' ");
                stb.append("             GROUP BY SCHREGNO) ");
            }
            stb.append("     ) ");
            stb.append(" , SCH_SCORE AS ( ");
            stb.append("     SELECT  T1.SCHREGNO, ");
            stb.append("             T1.BU_CD, ");
            stb.append("             T1.KA_CD, ");
            stb.append("             T1.RECOMMENDATION_FLG, ");
            stb.append("             T2.BU_NAME, ");
            stb.append("             T2.KA_NAME, ");
            stb.append("             T3.SCORE3 AS SCORE_A, ");
            stb.append("             RANK() OVER (ORDER BY T3.SCORE3 DESC) AS RANK_A ");
            stb.append("       FROM  SCH_RECOM_WISH T1, ");
            stb.append("             COLLEGE_RECOMMENDATION_DAT T2, ");
            stb.append("             RECORD_MOCK_RANK_DAT T3 ");
            stb.append("      WHERE  T2.YEAR = '" + _param._year + "' ");
            stb.append("        AND  T2.SCHOOL_CD = T1.SCHOOL_CD ");
            stb.append("        AND  T2.BU_CD = T1.BU_CD ");
            stb.append("        AND  T2.KA_CD = T1.KA_CD ");
            stb.append("        AND  T3.YEAR = '" + _param._year + "' ");
            stb.append("        AND  T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("        AND  T3.DATA_DIV = '1' ");
            stb.append("        AND  T3.COURSE_DIV = T2.DIV ");
            stb.append("        AND  T3.SUBCLASSCD = '333333' ");
            stb.append("        AND  T3.GRADE = '" + _param._grade + "' ");
            stb.append("     ) ");
        }
        stb.append(" , SCH_HEIGAN AS ( ");
        stb.append("     SELECT  SCHREGNO ");
        stb.append("       FROM  SCHREG_RECOMMENDATION_WISH_DAT ");
        stb.append("      WHERE  YEAR = '" + _param._year + "' ");
        stb.append("        AND  BU_CD = '99' ");
        stb.append("        AND  KA_CD = '0' ");
        stb.append("      GROUP BY SCHREGNO ");
        stb.append("     ) ");

        stb.append(" SELECT  TT2.SCHREGNO, ");
        stb.append("         TT1.RANK_A, ");
        stb.append("         TT1.SCORE_A, ");
        stb.append("         TT2.HR_CLASS, ");
        stb.append("         TT2.ATTENDNO, ");
        stb.append("         TT2.NAME, ");
        stb.append("         TT2.SEX_NAME, ");
        stb.append("         CASE WHEN TT3.SCHREGNO IS NOT NULL THEN '*' END AS HEIGAN ");
        stb.append("   FROM  SCHNO TT2 ");
        if (bukaheader.isBunRiAll()) {
            stb.append("         INNER JOIN SCH_SCORE TT1 ON TT1.SCHREGNO = TT2.SCHREGNO ");
        } else {
            stb.append("         INNER JOIN SCH_RECOM_WISH TT0 ON TT0.SCHREGNO = TT2.SCHREGNO ");
            stb.append("         LEFT JOIN SCH_SCORE TT1 ON TT1.SCHREGNO = TT2.SCHREGNO ");
        }
        stb.append("         LEFT JOIN SCH_HEIGAN TT3 ON TT3.SCHREGNO = TT2.SCHREGNO ");
        stb.append(" ORDER BY TT1.RANK_A, TT2.HR_CLASS, TT2.ATTENDNO ");
        return stb.toString();
    }

    private void printStudent(
            final Vrw32alp svf,
            final Student student,
            final int gyo
    ) {
        svf.VrsOutn("RANK", gyo, student._rank);
        svf.VrsOutn("APOINT", gyo, student._score);
        svf.VrsOutn("HR_CLASS", gyo, String.valueOf(student.getHrClass()));
        svf.VrsOutn("ATTENDNO", gyo, String.valueOf(student.getAttendno()));
        svf.VrsOutn("NAME", gyo, student._name);
        svf.VrsOutn("SEX", gyo, student._sexName);
        svf.VrsOutn("SIGN", gyo, student._heigan);
    }

    private void printStudentBlank(final Vrw32alp svf) {
        for (int gyo = 1; gyo < MAX_GYOU + 1; gyo++ ) {
            svf.VrsOutn("RANK", gyo, "");
            svf.VrsOutn("APOINT", gyo, "");
            svf.VrsOutn("HR_CLASS", gyo, "");
            svf.VrsOutn("ATTENDNO", gyo, "");
            svf.VrsOutn("NAME", gyo, "");
            svf.VrsOutn("SEX", gyo, "");
            svf.VrsOutn("SIGN", gyo, "");
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String grade = request.getParameter("GRADE");
        final String wishRank = request.getParameter("WISH_RANK");
        final String[] bukaCd = request.getParameterValues("CATEGORY_SELECTED");

        return new Param(
                year,
                semester,
                grade,
                wishRank,
                bukaCd);
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    private void init(final HttpServletResponse response, final Vrw32alp svf) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void close(final Vrw32alp svf, final DB2UDB db2) {
        if (null != svf) {
            svf.VrQuit();
        }
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * パラメータクラス
     */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _grade;
        private final String _wishRank;
        private final String[] _bukaCd;

        private final String _date;

        Param(
                final String year,
                final String semester,
                final String grade,
                final String wishRank,
                final String[] bukaCd
        ) {
            _year = year;
            _semester = semester;
            _grade = grade;
            _wishRank = wishRank;
            _bukaCd = bukaCd;
            _date = createDate();
        }

        public String createDate() {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(String.valueOf(Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日");
            stb.append(sdf.format(date));
            log.debug("日付=" + stb.toString());
            return stb.toString();
        }

        public int getGrade() {
            return Integer.parseInt(_grade);
        }

        public boolean isWishRank1() {
            return _wishRank.equals("1");
        }
    }
    
    private class Bukaheader {
        private final String _bukaCd;
        private final String _bukaName;

        Bukaheader(
                final String bukaCd,
                final String bukaName
        ) {
            _bukaCd = bukaCd;
            _bukaName = bukaName;
        }

        public String getBukaCd() {
            return _bukaCd;
        }

        public String getBukaName() {
            return _bukaName;
        }

        public String getCouseDiv() {
            return isBunkei() ? "1" : isRikei() ? "2" : isBunkeiRikei() ? "3" : "";
        }

        public boolean isBunkei() {
            return _bukaCd.equals("991");
        }

        public boolean isRikei() {
            return _bukaCd.equals("992");
        }

        public boolean isBunkeiRikei() {
            return _bukaCd.equals("999");
        }

        public boolean isBunRiAll() {
            return isBunkei() || isRikei() || isBunkeiRikei();
        }

        public String toString() {
            return _bukaCd + ":" + _bukaName;
        }
    }
    
    private class Bukaname {
        private final String _schoolCd;
        private final String _bukaCd;
        private final String _buCd;
        private final String _kaCd;
        private final String _buName;
        private final String _buAbbv;
        private final String _kaName;
        private final String _kaAbbv;
        private final String _div;
        private final int _kaCount;

        Bukaname(
                final String schoolCd,
                final String buCd,
                final String kaCd,
                final String buName,
                final String buAbbv,
                final String kaName,
                final String kaAbbv,
                final String div,
                final int kaCount
        ) {
            _schoolCd = schoolCd;
            _bukaCd = buCd + kaCd;
            _buCd = buCd;
            _kaCd = kaCd;
            _buName = buName;
            _buAbbv = buAbbv;
            _kaName = kaName;
            _kaAbbv = kaAbbv;
            _div = div;
            _kaCount = kaCount;
        }

        public String getBuAbbv() {
            return _buAbbv;
        }

        public String getBuAbbv(final int setCount) {
            return _buAbbv.substring(setCount);
        }

        public String getKaAbbv() {
            return 5 < getKaAbbvLen() ? _kaAbbv.substring(0, 5) : _kaAbbv;
        }

        public int getBuAbbvLen() {
            return _buAbbv.length();
        }

        public int getKaAbbvLen() {
            return _kaAbbv.length();
        }

        public int getKaCount() {
            return _kaCount * 2;
        }

        public int getKeisan1() {
            return (getKaCount() - (getBuAbbvLen() + getBuAbbvLen() - 1)) / 2;
        }

        public int getKeisan2() {
            return (getKaCount() - getBuAbbvLen()) / 2;
        }

        public boolean isEqualBucd(final String bucd) {
            return _buCd.equals(bucd);
        }

        public boolean isOverKaCount() {
            return getBuAbbvLen() < getKaCount();
        }

        public boolean isSeiKeisan1() {
            return 0 < getKeisan1();
        }

        public boolean isPatturn1Name1(final int setCount, final int kaCount, final int setFlg) {
            return setCount < getBuAbbvLen() && 
                    getKeisan1() <= (kaCount * 2) && 
                    setFlg != 2;
        }

        public boolean isPatturn1Name2(final int setCount, final int kaCount, final int setFlg) {
            return setCount < getBuAbbvLen() && 
                    getKeisan1() <= (kaCount * 2 + 1) && 
                    setFlg != 1;
        }

        public boolean isPatturn2Name1(final int setCount, final int kaCount) {
            return setCount < getBuAbbvLen() && 
                    getKeisan2() <= (kaCount * 2);
        }

        public boolean isPatturn2Name2(final int setCount, final int kaCount) {
            return setCount < getBuAbbvLen() && 
                    getKeisan2() <= (kaCount * 2 + 1);
        }

        public String toString() {
            return _buCd + ":" + _buAbbv + "、" + _kaCd + ":" + _kaAbbv;
        }
    }

    private class Student {
        final String _schregno;
        final String _rank;
        final String _score;
        final String _hrClass;
        final String _attendno;
        final String _name;
        final String _sexName;
        final String _heigan;

        Student(
                final String schregno,
                final String rank,
                final String score,
                final String hrClass,
                final String attendno,
                final String name,
                final String sexName,
                final String heigan
        ) {
            _schregno = schregno;
            _rank = rank;
            _score = score;
            _hrClass = hrClass;
            _attendno = attendno;
            _name = name;
            _sexName = sexName;
            _heigan = heigan;
        }

        public int getHrClass() {
            return Integer.parseInt(_hrClass);
        }

        public int getAttendno() {
            return Integer.parseInt(_attendno);
        }

        public String toString() {
            return "学籍番号 = " + _schregno
                  + " 氏名 = " + _name
                  + " 得点 = " + _score;
        }
    }
}
