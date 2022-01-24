// kanji=漢字
/*
 * $Id: 68cbafa743a8f336a394299930aec58aa7afe3e3 $
 *
 * 作成日: 2007/12/18 10:11:28 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2007-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

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

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 68cbafa743a8f336a394299930aec58aa7afe3e3 $
 */
public class KNJZ041K {
    private static final Log log = LogFactory.getLog(KNJZ041K.class);

    private static final String FORM_FILE = "KNJZ041K.frm";
    private static final String SENGAN = "1";
    private static final String HEIGAN = "2";

    Param _param;

    /**
     * KNJZ.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;

        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            boolean hasData = false;
            hasData = printMain(db2, svf);
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
     * ヘッダセット
     */
    private void setHead(Vrw32alp svf) {
        svf.VrsOut("NENDO", _param._title);
        svf.VrsOutn("JUDG1", 1, "判定");
        int cnt = 1;
        for (final Iterator iter = _param._course.keySet().iterator(); iter.hasNext();) {
            final String key = (String) iter.next();
            final String courseName = (String) _param._course.get(key);
            svf.VrsOutn("COUSE1", cnt, courseName);
            svf.VrsOutn("COUSE2", cnt, courseName);
            cnt++;
        }
        cnt = 1;
        for (final Iterator iter = _param._courseSh.keySet().iterator(); iter.hasNext();) {
            final String key = (String) iter.next();
            final MainCourse mc = (MainCourse) _param._courseSh.get(key);
            svf.VrsOutn("ITEM1", cnt, mc._senheiName);
            svf.VrsOutn("ITEM2_" + cnt, 1, mc._senheiName);
            cnt++;
        }
    }

    /**
     * 印刷メイン
     */
    private boolean printMain(DB2UDB db2, Vrw32alp svf) throws SQLException {
        boolean rtnFlg = false;

        final Map outPutData = getOutPutData(db2);

        svf.VrSetForm(FORM_FILE, 1);
        setHead(svf);

        int gyo = 2;
        int judge = 0;
        for (final Iterator iter = outPutData.keySet().iterator(); iter.hasNext();) {
            judge = printJudge(svf, judge, gyo);

            final CourseKey ck = (CourseKey) iter.next();
            final Course c = (Course) outPutData.get(ck);
            for (final Iterator cit = c._courseData.keySet().iterator(); cit.hasNext();) {
                final String cKey = (String) cit.next();
                final String[] cVal = (String[]) c._courseData.get(cKey);
                svf.VrsOutn("ITEM2_" + _param.getField(cKey + HEIGAN), gyo, cVal[0]);
                svf.VrsOutn("ITEM2_" + _param.getField(cKey + SENGAN), gyo, cVal[1]);
            }
            gyo++;
            rtnFlg = true;
        }
        svf.VrEndPage();
        return rtnFlg;
    }

    private int printJudge(final Vrw32alp svf, final int judge, final int gyo) {
        String val = "";
        switch (judge) {
        case 0:
            val = "S";
            break;
        case 1:
            val = "A";
            break;
        case 2:
            val = "B";
            break;
        case 3:
            val = "C";
            break;
        default:
            break;
        }
        svf.VrsOutn("JUDG1", gyo, val);
        final int rtnJudge = judge < 3 ? judge + 1 : 0;

        return rtnJudge;
    }

    /**
     * 出力データ
     */
    private Map getOutPutData(final DB2UDB db2) throws SQLException {
        final Map rtnCourseMap = new TreeMap();
        ResultSet initRs = null;
        ResultSet mainRs = null;
        try {
            db2.query(getPrintIniDataSql());
            initRs = db2.getResultSet();
            while (initRs.next()) {
                final String totalcd = initRs.getString("totalcd");
                final String shdiv = initRs.getString("shdiv");
                final String judge = initRs.getString("judge");
                final CourseKey ck = new CourseKey(totalcd, shdiv, judge);

                rtnCourseMap.put(ck, new Course());
            }

            db2.query(getPrintDataSql());
            mainRs = db2.getResultSet();
            while (mainRs.next()) {
                final String totalcd   = mainRs.getString("totalcd");
                final String shdiv     = mainRs.getString("shdiv");
                final String judgement = mainRs.getString("judgement");
                final CourseKey ck = new CourseKey(totalcd, shdiv, judgement);
                final Course c = (Course) rtnCourseMap.get(ck);

                final String cmpTotalcd = mainRs.getString("cmp_totalcd");
                final String heiVal = mainRs.getString("h_judge_name");
                final String senVal = mainRs.getString("s_judge_name");

                final String name = mainRs.getString("examcourse_name");
                c.setData(cmpTotalcd, heiVal, senVal, name);
            }
        } finally {
            DbUtils.closeQuietly(mainRs);
            db2.commit();
        }

        return rtnCourseMap;
    }

    /**
     * 出力データのキークラス
     * コースコード+専併区分+判定
     */
    private class CourseKey implements Comparable {
        final String _totalCd;
        final String _shDiv;
        final String _judgement;
        CourseKey(final String totalCd, final String shDiv, final String judgement) {
            _totalCd = totalCd;
            _shDiv = shDiv;
            _judgement = judgement;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(Object o) {
            if (!(o instanceof CourseKey)) {
                return -1;
            }
            final CourseKey that = (CourseKey) o;
            if (!this._totalCd.equals(that._totalCd)) {
                //降順ソートなので-を付けて逆転させる。
                final int ans = this._totalCd.compareTo(that._totalCd);
                if ("1".equals(_param._courseOrder)) {
                    return ans;
                } else {
                    return -ans;
                }
            }
            if (!this._shDiv.equals(that._shDiv)) {
                //降順ソートなので-を付けて逆転させる。
                final int ans = this._shDiv.compareTo(that._shDiv);
                return -ans;
            }

            return this._judgement.compareTo(that._judgement);
        }

        public String toString() {
            return "コースコード = " + _totalCd
                  + " 専併区分 = " + _shDiv
                  + " 判定 = " + _judgement;
        }
    }

    /**
     * @return
     */
    private String getPrintIniDataSql() {
        final String order = "1".equals(_param._courseOrder) ? "" : "DESC";
        final String sql = " SELECT "
            + "        coursecd || majorcd || examcoursecd as totalcd, "
            + "        shdiv, "
            + "        '0' AS judge "
            + "    FROM "
            + "        entexam_judgecomp_mst "
            + "    WHERE "
            + "        entexamyear = '" + _param._year + "' "
            + "    GROUP BY "
            + "        coursecd || majorcd || examcoursecd, "
            + "        shdiv "
            + "    UNION "
            + "    SELECT "
            + "        coursecd || majorcd || examcoursecd as totalcd, "
            + "        shdiv, "
            + "        '1' AS judge "
            + "    FROM "
            + "        entexam_judgecomp_mst "
            + "    WHERE "
            + "        entexamyear = '" + _param._year + "' "
            + "    GROUP BY "
            + "        coursecd || majorcd || examcoursecd, "
            + "        shdiv "
            + "    UNION "
            + "    SELECT "
            + "        coursecd || majorcd || examcoursecd as totalcd, "
            + "        shdiv, "
            + "        '2' AS judge "
            + "    FROM "
            + "        entexam_judgecomp_mst "
            + "    WHERE "
            + "        entexamyear = '" + _param._year + "' "
            + "    GROUP BY "
            + "        coursecd || majorcd || examcoursecd, "
            + "        shdiv "
            + "    UNION "
            + "    SELECT "
            + "        coursecd || majorcd || examcoursecd as totalcd, "
            + "        shdiv, "
            + "        '3' AS judge "
            + "    FROM "
            + "        entexam_judgecomp_mst "
            + "    WHERE "
            + "        entexamyear = '" + _param._year + "' "
            + "    GROUP BY "
            + "        coursecd || majorcd || examcoursecd, "
            + "        shdiv "
            + "    ORDER BY "
            + "        totalcd " + order + ", "
            + "        shdiv DESC, "
            + "        judge ";

        return sql;
    }

    /**
     * @return
     */
    private String getPrintDataSql() {
        final String order = "1".equals(_param._courseOrder) ? "" : "DESC";
        final String sql = " SELECT "
            + "        T1.coursecd || T1.majorcd || T1.examcoursecd as totalcd, "
            + "        T2.examcourse_name, "
            + "        T1.shdiv, "
            + "        T4.name1 as s_name, "
            + "        T1.judgement, "
            + "        T5.name1 as h_name, "
            + "        T1.cmp_coursecd || T1.cmp_majorcd || T1.cmp_examcoursecd as cmp_totalcd, "
            + "        T3.examcourse_name as cmp_examcourse_name, "
            + "        VALUE(T7.name1, '') as h_judge_name, "
            + "        VALUE(T6.name1, '') as s_judge_name "
            + "    FROM "
            + "        entexam_judgecomp_mst T1 "
            + "    LEFT OUTER JOIN "
            + "        entexam_course_mst T2 "
            + "    ON "
            + "        T1.entexamyear = T2.entexamyear AND "
            + "        T1.coursecd = T2.coursecd AND "
            + "        T1.majorcd = T2.majorcd AND "
            + "        T1.examcoursecd = T2.examcoursecd "
            + "    LEFT OUTER JOIN "
            + "        entexam_course_mst T3 "
            + "    ON "
            + "        T1.entexamyear = T3.entexamyear AND "
            + "        T1.cmp_coursecd = T3.coursecd AND "
            + "        T1.cmp_majorcd = T3.majorcd AND "
            + "        T1.cmp_examcoursecd = T3.examcoursecd "
            + "    LEFT OUTER JOIN "
            + "        v_name_mst T4 "
            + "    ON "
            + "        T1.entexamyear = T4.year AND "
            + "        T1.shdiv = T4.namecd2 AND "
            + "        T4.namecd1 = 'L006' "
            + "    LEFT OUTER JOIN "
            + "        v_name_mst T5 "
            + "    ON "
            + "        T1.entexamyear = T5.year AND "
            + "        T1.judgement = T5.namecd2 AND "
            + "        T5.namecd1 = 'L002' "
            + "    LEFT OUTER JOIN "
            + "        v_name_mst T6 "
            + "    ON "
            + "        T1.entexamyear = T6.year AND "
            + "        T1.s_judgement = T6.namecd2 AND "
            + "        T6.namecd1 = 'L002' "
            + "    LEFT OUTER JOIN "
            + "        v_name_mst T7 "
            + "    ON "
            + "        T1.entexamyear = T7.year AND "
            + "        T1.h_judgement = T7.namecd2 AND "
            + "        T7.namecd1 = 'L002' "
            + "    WHERE "
            + "        T1.entexamyear = '" + _param._year + "' "
            + "    ORDER BY "
            + "        totalcd " + order + ", "
            + "        T1.shdiv DESC, "
            + "        cmp_totalcd " + order + " ";

        return sql;
    }

    /**
     * 出力コースクラス(行単位)
     */
    private class Course
    {
        final Map _courseData = new TreeMap();
        String _name = "";

        void setData(final String cmp_totalcd, final String heiVal, final String senVal, final String name) {
            _name = name;
            String[] heiSen = new String[2];
            heiSen[0] = heiVal;
            heiSen[1] = senVal;
            _courseData.put(cmp_totalcd, heiSen);
        }
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
        private final String _gengou;
        private final String _title;
        private final String _date;
        private final String _prgId;
        private final String _courseOrder;
        private final Map _courseSh;
        private final Map _course;
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_year));
            _gengou = gengou + "年度";
            _prgId = request.getParameter("PRGID");
            _courseOrder = request.getParameter("COURSE_ORDER");
            _title = _year + "年度";

            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日");
            stb.append(sdf.format(date));
            _date = stb.toString();

            // DBより取得
            _courseSh = getCourseSh(db2, _year, _courseOrder);
            _course = getCourse(db2, _year, _courseOrder);
        }

        /*
         * フォームの印字フィールド取得
         */
        private String getField(final String courseKey) {
            final MainCourse mc = (MainCourse) _param._courseSh.get(courseKey);
            return mc._field;
        }

        private Map getCourseSh(
                final DB2UDB db2,
                final String year,
                final String courseOrder
        ) throws Exception {
            final String order = "1".equals(courseOrder) ? "" : "DESC";
            final Map rtnMap = new TreeMap(Collections.reverseOrder());
            final String sql = " WITH H_COURSE AS ( "
                + "    SELECT "
                + "        '2' AS SHDIV, "
                + "        COURSECD || MAJORCD || EXAMCOURSECD AS COURSE, "
                + "        EXAMCOURSE_NAME "
                + "    FROM "
                + "        ENTEXAM_COURSE_MST "
                + "    WHERE "
                + "        ENTEXAMYEAR = '" + year + "' "
                + "        AND EXAMCOURSECD > '1000' "
                + "    ORDER BY "
                + "        COURSE " + order + " "
                + "    ), S_COURSE AS ( "
                + "    SELECT "
                + "        '1' AS SHDIV, "
                + "        COURSECD || MAJORCD || EXAMCOURSECD AS COURSE, "
                + "        EXAMCOURSE_NAME "
                + "    FROM "
                + "        ENTEXAM_COURSE_MST "
                + "    WHERE "
                + "        ENTEXAMYEAR = '" + year + "' "
                + "        AND EXAMCOURSECD > '1000' "
                + "    ORDER BY "
                + "        COURSE " + order + " "
                + "    ) "
                + "    SELECT "
                + "        H1.*, "
                + "        LH1.NAME1 "
                + "    FROM "
                + "        H_COURSE H1 "
                + "        LEFT JOIN NAME_MST LH1 ON LH1.NAMECD1 = 'L006' "
                + "             AND LH1.NAMECD2 = H1.SHDIV "
                + "    UNION "
                + "    SELECT "
                + "        S1.*, "
                + "        SH1.NAME1 "
                + "    FROM "
                + "        S_COURSE S1 "
                + "        LEFT JOIN NAME_MST SH1 ON SH1.NAMECD1 = 'L006' "
                + "             AND SH1.NAMECD2 = S1.SHDIV "
                + "    ORDER BY "
                + "        COURSE " + order + ", "
                + "        SHDIV DESC ";

            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            try {
                int field = 1;
                while (rs.next()) {
                    final MainCourse mainCourse = new MainCourse(rs.getString("EXAMCOURSE_NAME"),
                                                                 rs.getString("COURSE"),
                                                                 rs.getString("SHDIV"),
                                                                 rs.getString("NAME1"),
                                                                 String.valueOf(field));
                    rtnMap.put(rs.getString("COURSE") + rs.getString("SHDIV"), mainCourse);
                    field++;
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnMap;
        }

        private Map getCourse(
                final DB2UDB db2,
                final String year,
                final String courseOrder
        ) throws Exception {
            final String order = "1".equals(courseOrder) ? "" : "DESC";
            final Map rtnMap = new TreeMap();
            final String sql = " "
                + "    SELECT "
                + "        COURSECD || MAJORCD || EXAMCOURSECD AS COURSE, "
                + "        EXAMCOURSE_ABBV "
                + "    FROM "
                + "        ENTEXAM_COURSE_MST "
                + "    WHERE "
                + "        ENTEXAMYEAR = '" + year + "' "
                + "        AND EXAMCOURSECD > '1000' "
                + "    ORDER BY "
                + "        COURSE " + order + " ";

            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            try {
                while (rs.next()) {
                    rtnMap.put(rs.getString("COURSE"), rs.getString("EXAMCOURSE_ABBV"));
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnMap;
        }
    }

    /**
     * 列のタイトルコース
     */
    private class MainCourse
    {
        private final String _name;
        private final String _course;
        private final String _shdiv;
        private final String _senheiName;
        private final String _field;

        public MainCourse(
                final String name,
                final String course,
                final String shdiv,
                final String senheiName,
                final String field
        ) {
            _name = name;
            _course = course;
            _shdiv = shdiv;
            _senheiName = senheiName;
            _field = field;
        }
        
    }
}
 // KNJZ041K

// eof
