// kanji=漢字
/*
 * $Id: 29e3acc4083989fb2ea74428bb9a0512f7af0309 $
 *
 * 作成日: 2009/12/24 1:37:07 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.util.ArrayList;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 29e3acc4083989fb2ea74428bb9a0512f7af0309 $
 */
public class KNJL373M {

    private static final Log log = LogFactory.getLog("KNJL373M.class");

    private boolean _hasData;
    private static final String FORMNAME = "KNJL373M.frm";
    private static final int MAX_LINE = 50;
    private static final int MAX_RETU = 5;
    private static final int MAX_KESSEKI_LINE = 20;
    private static final int MAX_KESSEKI_RETU = 2;

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
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

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printDataPass = getPrintData(db2, "PASS");
        final List printDataKouho = getPrintData(db2, "KOUHO");
        final List printDataKesseki = getPrintData(db2, "KESSEKI");
        svf.VrSetForm(FORMNAME, 1);
        printPass(svf, printDataPass);
        printToukei(db2, svf, printDataKesseki);
        printKouho(svf, printDataKouho, printDataPass);
        if (_hasData) svf.VrEndPage();
    }

    private void printPass(final Vrw32alp svf, final List printDataPass) {
        svf.VrsOut("POINT1", _param._passScore);
        svf.VrsOut("SUM1", String.valueOf(printDataPass.size()));
        int lineCnt = 1;
        int retuCnt = 1;
        for (final Iterator itPrint = printDataPass.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            if (lineCnt > MAX_LINE) {
                lineCnt = 1;
                retuCnt++;
            }
            if (retuCnt > MAX_RETU) {
                break;
            }
            svf.VrsOutn("EXAM_NO" + retuCnt, lineCnt, student._examNo);
            lineCnt++;
            _hasData = true;
        }
    }

    private void printKouho(final Vrw32alp svf, final List printDataKouho, final List printDataPass) {
        printHeaderKouho(svf, printDataKouho);
        String haifun = "--------------------";
        int haifunCnt = printDataPass.size();
        int lineCnt = 1;
        for (final Iterator itPrint = printDataKouho.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            if (lineCnt > MAX_LINE) {
                svf.VrEndPage();
                printHeaderKouho(svf, printDataKouho);
                lineCnt = 1;
            }
            svf.VrsOutn("RANK", lineCnt, student._totalRank4);
            svf.VrsOutn("EXAM_NO", lineCnt, student._examNo);
            svf.VrsOutn("TOTAL_SCORE", lineCnt, student._total4);
            for (final Iterator itScore = student._scoreDatas.keySet().iterator(); itScore.hasNext();) {
                final String subclassCd = (String) itScore.next();
                final ScoreData scoreData = (ScoreData) student._scoreDatas.get(subclassCd);
                svf.VrsOutn("SCORE" + scoreData._subclassCd, lineCnt, scoreData._score);
            }
            svf.VrsOutn("DIV", lineCnt, student._natpubpriName);
            svf.VrsOutn("FIELD2", lineCnt, student._fsName);
            svf.VrsOutn("FIELD3", lineCnt, student.getFsAreaName());
            lineCnt++;
            haifunCnt++;
            //５行毎に'-'を表示する
            if (haifunCnt % 5 == 0) {
                svf.VrsOutn("NUMBER", lineCnt, String.valueOf(haifunCnt)); //表１から数えた人数
                svf.VrsOutn("RANK", lineCnt, haifun);
                svf.VrsOutn("EXAM_NO", lineCnt, haifun);
                svf.VrsOutn("TOTAL_SCORE", lineCnt, haifun);
                for (final Iterator itScore = student._scoreDatas.keySet().iterator(); itScore.hasNext();) {
                    final String subclassCd = (String) itScore.next();
                    final ScoreData scoreData = (ScoreData) student._scoreDatas.get(subclassCd);
                    svf.VrsOutn("SCORE" + scoreData._subclassCd, lineCnt, haifun);
                }
                svf.VrsOutn("DIV", lineCnt, haifun);
                svf.VrsOutn("FIELD2", lineCnt, haifun);
                svf.VrsOutn("FIELD3", lineCnt, haifun);
                lineCnt++;
            }
            _hasData = true;
        }
    }

    private void printHeaderKouho(final Vrw32alp svf, final List printDataKouho) {
        svf.VrsOut("YEAR", _param.changeYear(_param._year) + "度");
        svf.VrsOut("POINT2", String.valueOf(Integer.parseInt(_param._passScore) - 1));
        svf.VrsOut("POINT3", _param._kouhoScore);
        svf.VrsOut("SUM2", String.valueOf(printDataKouho.size()));
    }

    private void printToukei(final DB2UDB db2, final Vrw32alp svf, final List printDataPass) throws SQLException {
        final int sigan = Integer.parseInt(getTotalSigan(db2));
        final int kesseki = printDataPass.size();
        final int juken = sigan - kesseki;
        svf.VrsOutn("TOTAL", 1, String.valueOf(sigan));
        svf.VrsOutn("TOTAL", 2, String.valueOf(juken));
        svf.VrsOutn("TOTAL", 3, String.valueOf(kesseki));
        int lineCnt = 1;
        int retuCnt = 1;
        for (final Iterator itPrint = printDataPass.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            if (lineCnt > MAX_KESSEKI_LINE) {
                lineCnt = 1;
                retuCnt++;
            }
            if (retuCnt > MAX_KESSEKI_RETU) {
                break;
            }
            svf.VrsOutn("ABSENCE_EXAM_NO" + retuCnt, lineCnt, student._examNo);
            lineCnt++;
            _hasData = true;
        }
    }

    private String getTotalSigan(DB2UDB db2) throws SQLException {
        final String sql = "SELECT "
                         + "    COUNT(*) AS CNT "
                         + " FROM "
                         + "    ENTEXAM_APPLICANTBASE_DAT "
                         + " WHERE "
                         + "    ENTEXAMYEAR = '" + _param._year + "' "
                         + "    AND APPLICANTDIV = '" + _param._applicantDiv + "' ";
        PreparedStatement ps = null;
        ResultSet rs = null;
        String retVal = "0";
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                retVal = rs.getString("CNT");
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retVal;
    }

    private String getTotalJuken(DB2UDB db2) throws SQLException {
        final String sql = "SELECT "
                         + "    COUNT(*) AS CNT "
                         + " FROM "
                         + "    ENTEXAM_DESIRE_DAT "
                         + " WHERE "
                         + "    ENTEXAMYEAR = '" + _param._year + "' "
                         + "    AND APPLICANTDIV = '" + _param._applicantDiv + "' "
                         + "    AND TESTDIV = '1' "
                         + "    AND EXAM_TYPE = '1' ";
        PreparedStatement ps = null;
        ResultSet rs = null;
        String retVal = "0";
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
               retVal = rs.getString("CNT");
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retVal;
    }

    private List getPrintData(final DB2UDB db2, final String dataDiv) throws SQLException {
        final List retList = new ArrayList();
        final String studentSql = getStudentSql(dataDiv);
        PreparedStatement psStudent = null;
        ResultSet rsStudent = null;
        try {
            psStudent = db2.prepareStatement(studentSql);
            rsStudent = psStudent.executeQuery();
            while (rsStudent.next()) {
                final String examNo = rsStudent.getString("EXAMNO");
                final String name = rsStudent.getString("NAME");
                final String nameKana = rsStudent.getString("NAME_KANA");
                final String fsCd = rsStudent.getString("FS_CD");
                final String fsName = rsStudent.getString("FS_NAME");
                final String natpubpriDiv = rsStudent.getString("FS_NATPUBPRIDIV");
                final String natpubpriName = rsStudent.getString("NATPUBPRI_NAME");
                final String fsAreaDiv = rsStudent.getString("FS_AREA_DIV");
                final String fsAreaDivName = rsStudent.getString("AREA_DIV_NAME");
                final String fsAreaCd = rsStudent.getString("FS_AREA_CD");
                final String fsAreaCdName = rsStudent.getString("AREA_NAME");
                final String remark = rsStudent.getString("REMARK");
                final String total4 = rsStudent.getString("TOTAL4");
                final String totalRank4 = rsStudent.getString("TOTAL_RANK4");
                final Student student = new Student(examNo,
                                                    name,
                                                    nameKana,
                                                    fsCd,
                                                    fsName,
                                                    natpubpriDiv,
                                                    natpubpriName,
                                                    fsAreaDiv,
                                                    fsAreaDivName,
                                                    fsAreaCd,
                                                    fsAreaCdName,
                                                    remark,
                                                    total4,
                                                    totalRank4);
                if ("KOUHO".equals(dataDiv)) {
                    student.setScoreDatas(db2);
                }
                retList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(null, psStudent, rsStudent);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql(final String dataDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     T1.FS_CD, ");
        stb.append("     T1.FS_NAME, ");
        stb.append("     T1.FS_NATPUBPRIDIV, ");
        stb.append("     NATP.NATPUBPRI_NAME, ");
        stb.append("     T1.FS_AREA_DIV, ");
        stb.append("     AREA_D.AREA_DIV_NAME, ");
        stb.append("     T1.FS_AREA_CD, ");
        stb.append("     ARE_M.AREA_NAME, ");
        stb.append("     T1.REMARK1 AS REMARK, ");
        stb.append("     RECEPT.TOTAL4, ");
        stb.append("     RECEPT.TOTAL_RANK4 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON T1.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND T1.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND T1.TESTDIV = RECEPT.TESTDIV ");
        stb.append("          AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("          AND T1.EXAMNO = RECEPT.RECEPTNO ");
        stb.append("          AND T1.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_NATPUBPRI_MST NATP ON T1.FS_NATPUBPRIDIV = NATP.NATPUBPRI_CD ");
        stb.append("     LEFT JOIN ENTEXAM_AREA_DIV_MST AREA_D ON T1.FS_NATPUBPRIDIV = AREA_D.NATPUBPRI_CD ");
        stb.append("          AND T1.FS_AREA_DIV = AREA_D.AREA_DIV_CD ");
        stb.append("     LEFT JOIN ENTEXAM_AREA_MST ARE_M ON T1.FS_NATPUBPRIDIV = ARE_M.NATPUBPRI_CD ");
        stb.append("          AND T1.FS_AREA_DIV = ARE_M.AREA_DIV_CD ");
        stb.append("          AND T1.FS_AREA_CD = ARE_M.AREA_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '1' ");
        stb.append("     AND T1.SHDIV = '1' ");
        stb.append("     AND T1.DESIREDIV = '1' ");
        if ("PASS".equals(dataDiv)) {
            stb.append("          AND RECEPT.TOTAL4 >= " + _param._passScore + " ");
            stb.append(" ORDER BY ");
            stb.append("     T1.EXAMNO ");
        } else if ("KOUHO".equals(dataDiv)) {
            stb.append("          AND RECEPT.TOTAL4 < " + _param._passScore + " ");
            stb.append("          AND RECEPT.TOTAL4 >= " + _param._kouhoScore + " ");
            stb.append(" ORDER BY ");
            stb.append("     RECEPT.TOTAL_RANK4, ");
            stb.append("     T1.EXAMNO ");
        } else {
            stb.append("          AND EXISTS( ");
            stb.append("                SELECT ");
            stb.append("                    'x' ");
            stb.append("                FROM ");
            stb.append("                    ENTEXAM_DESIRE_DAT E1 ");
            stb.append("                WHERE ");
            stb.append("                    T1.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
            stb.append("                    AND T1.APPLICANTDIV = E1.APPLICANTDIV ");
            stb.append("                    AND T1.TESTDIV = E1.TESTDIV ");
            stb.append("                    AND E1.EXAM_TYPE = '1' ");
            stb.append("                    AND T1.EXAMNO = E1.EXAMNO ");
            stb.append("                    AND E1.EXAMINEE_DIV = '2' ");
            stb.append("          ) ");
            stb.append(" ORDER BY ");
            stb.append("     T1.EXAMNO ");
        }

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    public class Student {
        final String _examNo;
        final String _name;
        final String _nameKana;
        final String _fsCd;
        final String _fsName;
        final String _natpubpriDiv;
        final String _natpubpriName;
        final String _fsAreaDiv;
        final String _fsAreaDivName;
        final String _fsAreaCd;
        final String _fsAreaCdName;
        final String _remark;
        final String _total4;
        final String _totalRank4;
        final Map _scoreDatas;

        public Student(
                final String examNo,
                final String name,
                final String nameKana,
                final String fsCd,
                final String fsName,
                final String natpubpriDiv,
                final String natpubpriName,
                final String fsAreaDiv,
                final String fsAreaDivName,
                final String fsAreaCd,
                final String fsAreaCdName,
                final String remark,
                final String total4,
                final String totalRank4
        ) {
            _examNo = examNo;
            _name = name;
            _nameKana = nameKana;
            _fsCd = fsCd;
            _fsName = fsName;
            _natpubpriDiv = natpubpriDiv;
            _natpubpriName = natpubpriName;
            _fsAreaDiv = fsAreaDiv;
            _fsAreaDivName = fsAreaDivName;
            _fsAreaCd = fsAreaCd;
            _fsAreaCdName = fsAreaCdName;
            _remark = remark;
            _scoreDatas = new HashMap();
            _total4 = total4;
            _totalRank4 = totalRank4;
        }

        private String getFsAreaName() {
            String str = "";
            String name1 = _fsAreaDivName; //区分名
            String name2 = _fsAreaCdName;  //所在地名
            //所在地区分コードが下記の場合、所在地区分名は下記の通り表記する
            if ("01".equals(_fsAreaDiv)) {
                name1 = null;
            } else if ("02".equals(_fsAreaDiv)) {
                name1 = "東京都";
// TODO：コメント部分は川村さん仕様。これだと他県が追加された場合を考慮するとまずいので「99:その他」以外というif文条件にした。
//            } else if ("03".equals(_fsAreaDiv)) {
//                name1 = "埼玉県";
//            } else if ("04".equals(_fsAreaDiv)) {
//                name1 = "千葉県";
//            } else if ("05".equals(_fsAreaDiv)) {
//                name1 = "神奈川県";
            } else if (!"99".equals(_fsAreaDiv) && null != name1) {
                name1 = name1 + "県";
            }
            //所在地区分コード(99)または所在地コード(99)の場合、「所在地欄」は「その他備考」の内容だけを表記する
            if ("99".equals(_fsAreaDiv) || "99".equals(_fsAreaCd)) {
                name1 = _remark;
                name2 = null;
            }
            if (null != name1 && null != name2) {
                str = name1 + name2;
            } else if (null != name1) {
                str = name1;
            } else if (null != name2) {
                str = name2;
            }
            return str;
        }

        private void setScoreDatas(final DB2UDB db2) throws SQLException {
            final String sql = getScoreDataSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd = rs.getString("TESTSUBCLASSCD");
                    final String attendFlg = rs.getString("ATTEND_FLG");
                    final String score = rs.getString("SCORE");
                    final String rank = rs.getString("RANK");
                    final ScoreData scoreData = new ScoreData(subclassCd, attendFlg, score, rank);
                    _scoreDatas.put(subclassCd, scoreData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getScoreDataSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TESTSUBCLASSCD, ");
            stb.append("     ATTEND_FLG, ");
            stb.append("     SCORE, ");
            stb.append("     RANK ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_DAT ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND TESTDIV = '1' ");
            stb.append("     AND EXAM_TYPE = '1' ");
            stb.append("     AND RECEPTNO = '" + _examNo + "' ");
            return stb.toString();
        }
    }

    private class ScoreData {
        final String _subclassCd;
        final String _attendFlg;
        final String _score;
        final String _rank;

        public ScoreData(
                final String subclassCd,
                final String attendFlg,
                final String score,
                final String rank
        ) {
            _subclassCd = subclassCd;
            _attendFlg = attendFlg;
            _score = score;
            _rank = rank;
        }
    }
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _applicantDiv;
        private final String _passScore;
        private final String _kouhoScore;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _passScore = request.getParameter("PASS_SCORE");
            _kouhoScore = request.getParameter("KOUHO_SCORE");
        }

        private String changeYear(final String year) {
            return KNJ_EditDate.h_format_JP_N(year + "-01-01");
        }
    }
}

// eof
