// kanji=漢字
/*
 * $Id: de2859b22fc0dab8813a97f61c16de94b209885d $
 *
 * 作成日: 2007/08/17 14:25:35 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2007-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: de2859b22fc0dab8813a97f61c16de94b209885d $
 */
public class KNJA139 {

    private static final String FORM_NAME = "KNJA139.frm";

    private static final Log log = LogFactory.getLog(KNJA139.class);

    private Param _param;

    /**
     * KNJA.classから呼ばれる処理
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
        log.fatal("$Revision: 67774 $"); // CVSキーワードの取り扱いに注意
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _nendo;
        private final String[] _hrClass;
        private final List _homeroom;
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";
            _hrClass = request.getParameterValues("CLASS_SELECTED");

            // DBより取得
            _homeroom = createHomeRooms(db2, _year, _semester, _hrClass);
            Collections.sort(_homeroom);
        }
    }

    private List createHomeRooms(
            final DB2UDB db2,
            final String year,
            final String semester,
            final String[] hrClass
    ) throws Exception {
        final List rtn = new ArrayList();

        final StringBuffer inState = new StringBuffer();
        String seq = "";
        for (int i = 0; i < hrClass.length; i++) {
            inState.append(seq + "'" + hrClass[i] + "'");
            seq = ",";
        }

        final String sql = getHrSql(inState.toString(), year, semester);

        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final String hrnameabbv = rs.getString("HR_NAME");
                final String hrclass = rs.getString("HR_CLASS");
                final String grade = rs.getString("GRADE");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String gradename1 = rs.getString("GRADE_NAME1");
                final String className1 = rs.getString("HR_CLASS_NAME1");
                final String major = rs.getString("MAJORNAME");
                final HomeRoom hr = new HomeRoom(grade, schoolKind, gradename1, hrclass, hrnameabbv, className1, major);
                rtn.add(hr);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtn;
    }

    private String getHrSql(final String inState, final String year, final String semester) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.HR_NAME, ");
        stb.append("     T1.HR_CLASS_NAME1, ");
        stb.append("     L1.MAJORNAME, ");
        stb.append("     L2.SCHOOL_KIND, ");
        stb.append("     L2.GRADE_NAME1 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT T1 ");
        stb.append("     LEFT JOIN (SELECT ");
        stb.append("                    W1.GRADE, ");
        stb.append("                    W1.HR_CLASS, ");
        stb.append("                    MAX(WL1.MAJORNAME) AS MAJORNAME ");
        stb.append("                FROM ");
        stb.append("                    SCHREG_REGD_DAT W1 ");
        stb.append("                    LEFT JOIN MAJOR_MST WL1 ON WL1.COURSECD = W1.COURSECD ");
        stb.append("                         AND WL1.MAJORCD = W1.MAJORCD ");
        stb.append("                WHERE ");
        stb.append("                    W1.YEAR = '" + year + "' ");
        stb.append("                    AND W1.GRADE || W1.HR_CLASS IN (" + inState + ") ");
        stb.append("                GROUP BY ");
        stb.append("                    W1.GRADE, ");
        stb.append("                    W1.HR_CLASS ");
        stb.append("               ) L1 ON L1.GRADE || L1.HR_CLASS = T1.GRADE || T1.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR AND L2.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SEMESTER = '" + semester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS IN (" + inState + ") ");
        stb.append("  ");

        return stb.toString();
    }

    /** 年組データ */
    private class HomeRoom implements Comparable {
        private final String _grade;
        private final String _schoolKind;
        private final String _gradename1;
        private final String _hrclass;
        private final String _name;
        private final String _className1;
        private final String _major;

        HomeRoom(
                final String grade,
                final String schoolKind,
                final String gradename1,
                final String room,
                final String name,
                final String className1,
                final String major
        ) {
            _grade = grade == null ? "" : grade;
            _schoolKind = schoolKind == null ? "" : schoolKind;
            _gradename1 = gradename1 == null ? "" : gradename1;
            _hrclass = room == null ? "" : room;
            _name = name == null ? "" : name;
            _className1 = className1 == null ? _hrclass : className1;
            _major = major == null ? "" : major;
        }
        public String toString() {
            return _grade + "-" + _hrclass + ":" + _name + ":" + _className1 + ":" + _major;
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
        log.debug(sql);
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();
            Student student = new Student(db2,
            		                KnjDbUtils.getString(row, "ATTENDNO"),
                                    KnjDbUtils.getString(row, "NAME"),
                                    KnjDbUtils.getString(row, "IDOU"),
                                    KnjDbUtils.getString(row, "IDOU_SDATE"),
                                    KnjDbUtils.getString(row, "IDOU_EDATE")
                                    );
			rtnList.add(student);
        }
        return rtnList;
    }

    // CSOFF: ExecutableStatementCount
    // CSOFF: MethodLength
    private String getStudentSql(final HomeRoom key) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_T AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     L1.NAME, ");
        stb.append("     L1.NAME_KANA, ");
        stb.append("     CASE WHEN VALUE(L1.GRD_DIV, '0') = '2' OR VALUE(L1.GRD_DIV, '0') = '3' ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '0' END AS IDOU, ");
        stb.append("     L1.GRD_DATE, ");
        stb.append("     L2.NAME1 AS GRD_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'A003' ");
        stb.append("          AND L2.NAMECD2 = L1.GRD_DIV ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.GRADE = '" + key._grade + "' ");
        stb.append("     AND T1.HR_CLASS = '" + key._hrclass + "' ");
        stb.append(" ) ");

        stb.append(" , TRANSFER_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.TRANSFERCD, ");
        stb.append("     T1.TRANSFER_SDATE, ");
        stb.append("     T1.TRANSFER_EDATE, ");
        stb.append("     L1.NAME1 AS TRANSFER_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_TRANSFER_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A004' ");
        stb.append("          AND L1.NAMECD2 = T1.TRANSFERCD ");
        stb.append(" WHERE ");
        stb.append("     SCHREGNO || CAST(TRANSFER_SDATE AS CHAR(10)) IN (SELECT ");
        stb.append("                                                          SCHREGNO || CAST(MAX(TRANSFER_SDATE) AS CHAR(10)) AS MAX_DATE ");
        stb.append("                                                      FROM ");
        stb.append("                                                          SCHREG_TRANSFER_DAT ");
        stb.append("                                                      WHERE ");
        stb.append("                                                          SCHREGNO IN (SELECT SCHREGNO FROM SCH_T WHERE IDOU = '0') ");
        stb.append("                                                          AND TRANSFERCD IN ('1', '2') ");
        stb.append("                                                      GROUP BY ");
        stb.append("                                                          SCHREGNO ");
        stb.append("                                                     ) ");
        stb.append("     AND TRANSFERCD IN ('1', '2') ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     CASE WHEN T1.IDOU = '1' ");
        stb.append("          THEN T1.GRD_DATE ");
        stb.append("          ELSE L1.TRANSFER_SDATE END AS IDOU_SDATE, ");
        stb.append("     CASE WHEN T1.IDOU = '1' ");
        stb.append("          THEN '' ");
        stb.append("          ELSE CAST(L1.TRANSFER_EDATE AS CHAR(10)) END AS IDOU_EDATE, ");
        stb.append("     CASE WHEN T1.IDOU = '1' ");
        stb.append("          THEN T1.GRD_NAME ");
        stb.append("          ELSE L1.TRANSFER_NAME END AS IDOU ");
        stb.append(" FROM ");
        stb.append("     SCH_T T1 ");
        stb.append("     LEFT JOIN TRANSFER_T L1 ON L1.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     T1.ATTENDNO ");

        return stb.toString();
    }
    // CSON: MethodLength
    // CSON: ExecutableStatementCount

    /** 生徒クラス */
    private class Student {
        final String _attendno;
        final String _name;
        final String _idou;
        final String _idouSdate;
        final String _idouEdate;

        Student(
        		final DB2UDB db2,
                final String attendno,
                final String name,
                final String idou,
                final String idouSdate,
                final String idouEdate
        ) {
            _attendno = attendno;
            _name = name;
            _idou = StringUtils.defaultString(idou);

            _idouSdate = StringUtils.defaultString(KNJ_EditDate.getAutoFormatDate(db2, idouSdate));
            _idouEdate = StringUtils.defaultString(KNJ_EditDate.getAutoFormatDate(db2, idouEdate));
        }

        public boolean isTransfer() {
            if (_idou.equals("休学") || _idou.equals("留学")) {
                return true;
            }
            return false;
        }

        public String toString() {
            return "出席番号 = " + _attendno
                  + " 氏名 = " + _name
                  + " 異動 = " + _idou + _idouSdate + "\uFF5E" + _idouEdate;
        }
    }

    private boolean outPutPrint(final Vrw32alp svf, final Map outputData) {
        boolean rtnFlg = false;
        svf.VrSetForm(FORM_NAME, 1);

        final List homeroomSorted = new ArrayList(outputData.keySet());
        Collections.sort(homeroomSorted);

        int line = 1;
        for (final Iterator itHomeRoom = homeroomSorted.iterator(); itHomeRoom.hasNext();) {

            final HomeRoom homeRoom = (HomeRoom) itHomeRoom.next();

            setHead(svf, homeRoom);

            final List listStudent = (List) outputData.get(homeRoom);

            int fieldCnt = 1;
            for (final Iterator itStudent = listStudent.iterator(); itStudent.hasNext();) {

                if (line > 30) {
                    line = 1;
                    fieldCnt = dataPrintField(svf, fieldCnt);
                }

                final Student student = (Student) itStudent.next();
                outPutStudent(svf, student, fieldCnt, line);

                line++;

                rtnFlg = true;

            }

            line = dataPrintLine(svf, line);
        }

        line = dataPrintLine(svf, line);

        return rtnFlg;
    }

    private int dataPrintField(final Vrw32alp svf, final int fieldCnt) {
        int rtnFieldCnt = fieldCnt;
        if (rtnFieldCnt == 2) {
            svf.VrEndPage();
            rtnFieldCnt = 1;
        } else {
            rtnFieldCnt = 2;
        }
        return rtnFieldCnt;
    }

    private int dataPrintLine(final Vrw32alp svf, final int line) {
        int rtnLine = line;
        if (rtnLine > 1) {
            svf.VrEndPage();
            rtnLine = 1;
        }
        return rtnLine;
    }

    private void setHead(final Vrw32alp svf, final HomeRoom homeRoom) {
        svf.VrsOut("NENDO", _param._nendo);
        String title = "";
        if ("K".equals(homeRoom._schoolKind)) {
            title = homeRoom._major + " " + homeRoom._className1 + "組　園児氏名索引表";
        } else {
            title = homeRoom._major + " " + homeRoom._gradename1 + " " + homeRoom._className1 + "組　生徒氏名索引表";
        }
        svf.VrsOut("GRADE", title);
    }

    private void outPutStudent(
            final Vrw32alp svf,
            final Student student,
            final int fieldCnt,
            final int line
    ) {
        svf.VrsOutn("NUMBER" + fieldCnt, line, String.valueOf(Integer.parseInt(student._attendno)));

        if (null != student._name) {
            final String fieldNo = (17 < student._name.length()) ? "_3" : (10 < student._name.length()) ? "_2" : "";
            svf.VrsOutn("NAME" + fieldCnt + fieldNo, line, student._name);
        }

        if (!StringUtils.isBlank(student._idou)) {
            if (student.isTransfer()) {
                svf.VrsOutn("REMARK" + fieldCnt + "_1", line, student._idou + " " + student._idouSdate);
                svf.VrsOutn("REMARK" + fieldCnt + "_2", line, "\uFF5E" + student._idouEdate);
            } else {
                svf.VrsOutn("REMARK" + fieldCnt, line, student._idou + " " + student._idouSdate);
            }
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
 // KNJA139

// eof
