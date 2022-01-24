// kanji=漢字
/*
 * $Id: f406a2ea326f12f136e33692b580fbbbee2c2f02 $
 *
 * 作成日: 2007/07/06 11:50:26 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2007-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: f406a2ea326f12f136e33692b580fbbbee2c2f02 $
 */
public class KNJD633 {

    private static final String FORM_NAME = "KNJD633.frm";

    private static final String VALUE_A = "A";
    private static final String VALUE_G = "G";

    private static final String COURSE_NO = "0";
    private static final String COURSE_BUNKEI = "1";
    private static final String COURSE_RIKEI = "2";

    private static final String DATA_TANNEN = "0";
    private static final String DATA_RUISEKI = "1";

    private static final Log log = LogFactory.getLog(KNJD633.class);

    Param _param;

    /**
     * KNJD.classから呼ばれる処理
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
        private final String _semester;
        private final String _grade;
        private final String[] _hrClass;
        private final String _dataDiv;
        private final String _dataYear;
        private final String _sort;
        private final String _date;
        private final List _homeroom;
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _gengou = _year + "年度";
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameterValues("CLASS_SELECTED");
            _dataDiv = request.getParameter("DATA_DIV").equals("1") ? VALUE_A : VALUE_G;
            _dataYear = request.getParameter("DATA_YEAR");
            _sort = request.getParameter("SORT");

            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(String.valueOf(Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日");
            stb.append(sdf.format(date));
            _date = stb.toString();

            // DBより取得
            _homeroom = createHomeRooms(db2, _year, _semester, _grade, _hrClass);
            Collections.sort(_homeroom);
        }
    }

    /** HomeRoom作成 */
    private List createHomeRooms(
            final DB2UDB db2,
            final String year,
            final String semester,
            final String grade,
            final String[] hrClass
    ) throws Exception {
        final List rtn = new ArrayList();

        final StringBuffer inState = new StringBuffer();
        String seq = "";
        for (int i = 0; i < hrClass.length; i++) {
            inState.append(seq + "'" + hrClass[i] + "'");
            seq = ",";
        }

        final String sql = "SELECT HR_CLASS,HR_NAME FROM SCHREG_REGD_HDAT WHERE YEAR = '" + year + "'"
                + " AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS IN (" + inState.toString() + ")";
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final String hrnameabbv = rs.getString("HR_NAME");
                final String hrclass = rs.getString("HR_CLASS");
                final HomeRoom hr = new HomeRoom(grade, hrclass, hrnameabbv);
                rtn.add(hr);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtn;
    }

    /** 年組データ */
    private class HomeRoom implements Comparable {
        private final String _grade;
        private final String _hrclass;
        private final String _name;

        HomeRoom(
                final String grade,
                final String room,
                final String name
        ) {
            _grade = grade;
            _hrclass = room;
            _name = name;
        }
        public String toString() {
            return _grade + "-" + _hrclass + ":" + _name;
        }

        public int compareTo(final Object o) {
            if (!(o instanceof HomeRoom)) {
                return -1;
            }
            final HomeRoom that = (HomeRoom) o;

            // 学年
            if (!this._grade.equals(that._grade)) {
                return this._grade.compareTo(that._grade);
            }

            // 組（学年が同じだった場合）
            return this._hrclass.compareTo(that._hrclass);
        }
    }

    /** 印刷処理メイン */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        final Map outputData = new HashMap();

        for (final Iterator it = _param._homeroom.iterator(); it.hasNext();) {
            final HomeRoom key = (HomeRoom) it.next();
            final List studentData = createStuedent(db2, key);
            outputData.put(key, studentData);
        }

        final boolean rtnFlg = outPutPrint(svf, outputData);

        if (log.isDebugEnabled()) {
            debugData(outputData);
        }
        return rtnFlg;
    }

    private List createStuedent(final DB2UDB db2, final HomeRoom key) throws Exception {
        final List rtnList = new ArrayList();
        final String sql = getStudentSql(key);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        while (rs.next()) {
            rtnList.add(new Student(rs.getString("ATTENDNO"),
                                    rs.getString("NAME"),
                                    rs.getString("SEX"),
                                    rs.getString("SCORE")
                                    ));
        }
        return rtnList;
    }

    private String getStudentSql(final HomeRoom key) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     L1.NAME, ");
        stb.append("     N1.NAME2 AS SEX, ");
        if (_param._dataDiv.equals(VALUE_G)) {
            stb.append(getScoreSql("1"));
        } else {
            stb.append(getScoreSql("3"));
        }
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' ");
        stb.append("          AND N1.NAMECD2 = L1.SEX ");
        stb.append(getRecordMockSql("1", DATA_RUISEKI, COURSE_NO));
        stb.append(getRecordMockSql("2", DATA_RUISEKI, COURSE_BUNKEI));
        stb.append(getRecordMockSql("3", DATA_RUISEKI, COURSE_RIKEI));
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + key._grade + "' ");
        stb.append("     AND T1.HR_CLASS = '" + key._hrclass + "' ");
        stb.append(" ORDER BY ");
        stb.append(getSortSql());

        return stb.toString();
    }

    /**
     * @param stb
     */
    private String getScoreSql(final String no) {
        final String rtnSql = " CASE WHEN (VALUE(R1.SCORE" + no + ", 0) >= VALUE(R2.SCORE" + no + ", 0) "
                            + "            AND VALUE(R1.SCORE" + no + ", 0) >= VALUE(R3.SCORE" + no + ", 0)) "
                            + "      THEN R1.SCORE" + no + " "
                            + "      ELSE CASE WHEN (VALUE(R2.SCORE" + no + ", 0) >= VALUE(R1.SCORE" + no + ", 0) "
                            + "                      AND VALUE(R2.SCORE" + no + ", 0) >= VALUE(R3.SCORE" + no + ", 0)) "
                            + "                THEN R2.SCORE" + no + " "
                            + "                ELSE R3.SCORE" + no + " END "
                            + "      END AS SCORE ";

        return rtnSql;
    }

    private String getRecordMockSql(final String rNo, final String dataDiv, final String courseDiv) {
        final String rtnSql = "     LEFT JOIN RECORD_MOCK_RANK_DAT R" + rNo + " ON R" + rNo + ".YEAR = '" + _param._dataYear + "' "
                            + "          AND R" + rNo + ".SCHREGNO = T1.SCHREGNO "
                            + "          AND R" + rNo + ".DATA_DIV = '" + dataDiv + "' "
                            + "          AND R" + rNo + ".COURSE_DIV = '" + courseDiv + "' ";

        return rtnSql;
    }

    private String getSortSql() {
        final String rtnSql;
        if (_param._sort.equals("1")) {
            rtnSql = "     VALUE(SCORE, -1) DESC, "
                   + "     T1.ATTENDNO ";
        } else if (_param._sort.equals("2")) {
            rtnSql = "     VALUE(SCORE, -1), "
                   + "     T1.ATTENDNO ";
        } else {
            rtnSql = "     T1.ATTENDNO ";
        }

        return rtnSql;
    }

    /** 生徒クラス */
    private class Student {
        final String _attendno;
        final String _name;
        final String _sex;
        final String _score;

        Student(
                final String attendno,
                final String name,
                final String sex,
                final String score
        ) {
            _attendno = attendno;
            _name = name;
            _sex = sex;
            _score = score;
        }

        public String toString() {
            return "出席番号 = " + _attendno
                  + " 氏名 = " + _name
                  + " 性別 = " + _sex
                  + " 得点 = " + _score;
        }
    }

    private boolean outPutPrint(final Vrw32alp svf, final Map outputData) {
        boolean rtnFlg = false;
        svf.VrSetForm(FORM_NAME, 1);

        final List homeroomSorted = new ArrayList(outputData.keySet());
        Collections.sort(homeroomSorted);

        int line = 1;
        for (final Iterator itHomeRoom = homeroomSorted.iterator(); itHomeRoom.hasNext();) {
            setHead(svf);
            final HomeRoom homeRoom = (HomeRoom) itHomeRoom.next();
            svf.VrsOutn("HR_NAME", line, homeRoom._name);
            final List listStudent = (List) outputData.get(homeRoom);

            int fieldCnt = 1;
            for (final Iterator itStudent = listStudent.iterator(); itStudent.hasNext();) {
                final Student student = (Student) itStudent.next();
                outPutStudent(svf, student, fieldCnt, line);
                fieldCnt++;
                rtnFlg = true;
            }
            line++;
            if (line > 6) {
                svf.VrEndPage();
                line = 1;
            }
        }

        if (line > 1) {
            svf.VrEndPage();
        }
        return rtnFlg;
    }

    private void setHead(final Vrw32alp svf) {
        svf.VrsOut("NENDO", _param._gengou);
        svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(_param._grade)));
        svf.VrsOut("T_VALUE", _param._dataDiv);
        svf.VrsOut("DATE", _param._date);
    }

    private void outPutStudent(
            final Vrw32alp svf,
            final Student student,
            final int fieldCnt,
            final int line
    ) {
        svf.VrsOutn("NUMBER" + fieldCnt, line, String.valueOf(Integer.parseInt(student._attendno)));
        svf.VrsOutn("SEX" + fieldCnt, line, student._sex);
        svf.VrsOutn("VALUE" + fieldCnt, line, student._score);

        if (null != student._name) {
            final String fieldNo = (20 < student._name.getBytes().length) ? "2" : "1";
            svf.VrsOutn("NAME" + fieldCnt + "_" + fieldNo, line, student._name);
        }
    }

    private void debugData(final Map outputData) {
        final List homeroomSorted = new ArrayList(outputData.keySet());
        Collections.sort(homeroomSorted);
        for (final Iterator it = homeroomSorted.iterator(); it.hasNext();) {
            final HomeRoom homeRoom = (HomeRoom) it.next();
            log.debug(homeRoom);
            final List listStudent = (List) outputData.get(homeRoom);
            for (final Iterator itStu = listStudent.iterator(); itStu.hasNext();) {
                final Student student = (Student) itStu.next();
                log.debug(student);
            }
        }
    }
}
 // KNJD633

// eof
