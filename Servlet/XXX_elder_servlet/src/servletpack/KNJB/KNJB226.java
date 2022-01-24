package servletpack.KNJB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * クラス別希望状況一覧
 */
public class KNJB226 {

    private static final Log log = LogFactory.getLog(KNJB226.class);
    private boolean _hasData;
    private Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

        } catch (final Exception e) {
            log.error("Exception:", e);
            return;
        }

        _param = createParam(db2, request);

        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();

            if (svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            for (int i = 0; i < _param._classSelected.length; i++) {

                // 生徒データを取得
                final List studentList = createStudentInfoData(db2, _param._classSelected[i]);
                if (printMain(svf, studentList)) { // 生徒出力のメソッド
                    _hasData = true;
                }
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            final int ret = svf.VrQuit();
            log.info("===> VrQuit():" + ret);

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    /**
     * 生徒の出力
     */
    private boolean printMain(final Vrw32alp svf, final List studentList) throws Exception {
//        final int PAGE_MAX_LINE = 45;
        boolean hasData = false;
        if (printStudentList(svf, studentList, "KNJB226.frm")) {
            hasData = true;
        }
        return  hasData;
    }

    public boolean printStudentList(final Vrw32alp svf, final List studentList, final String form) {
        boolean refflg = false;
        svf.VrSetForm(form, 4);

        int gyo = 0;
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            gyo++;
            printStudent(svf, gyo, student);
            refflg = true;
            svf.VrEndRecord();
        }

        return refflg;
    }

    private static int getMS932ByteLength(final String str) {
        int len = 0;
        if (null != str) {
            try {
                len = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return len;
    }

    private void printStudent(final Vrw32alp svf, final int gyo, final Student student) {
        svf.VrsOut("TITLE", "クラス別希望状況一覧");
        svf.VrsOut("INVEST_YEAR", _param._year + "年度");
        svf.VrsOut("TAKE_YEAR", _param._rishuu_year + "年度");
        svf.VrsOut("PRINT_DATE", KNJ_EditDate.h_format_thi(_param._ctrlDate, 0));
        svf.VrsOut("HR_NAME", student._hrName);

        svf.VrsOut("NO", (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : "");
        svf.VrsOut("SCHREG_NO", student._schregno);
        svf.VrsOut("NAME" + ((30 < getMS932ByteLength(student._name)) ? "2" : "1"), student._name);
        svf.VrsOut("HOPE_SUBJECT", (NumberUtils.isDigits(student._sub_cnt)) ? student._sub_cnt : "");
        svf.VrsOut("CREDIT", (NumberUtils.isDigits(student._credits_sum)) ? student._credits_sum : "");
    }

    /**
     * 生徒データ取得処理
     * @param db2           ＤＢ接続オブジェクト
     * @return              帳票出力対象データリスト
     * @throws Exception
     */
    private List createStudentInfoData(final DB2UDB db2, final String hrClass) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getStudentInfoSql(hrClass);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student studentInfo = new Student(
                        rs.getString("HR_NAME"),
                        rs.getString("ATTENDNO"),
                        rs.getString("SCHREGNO"),
                        rs.getString("NAME"),
                        rs.getString("SUB_CNT"),
                        rs.getString("CREDITS_SUM")
                );
                rtnList.add(studentInfo);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentInfoSql(final String hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SUB AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T2.SCHREGNO, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     T2.SUBCLASSCD AS SUBCLASSCD, ");
        }
        stb.append("         L1.CREDITS ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1 ");
        stb.append("         INNER JOIN SUBCLASS_STD_SELECT_DAT T2 ");
        stb.append("                 ON  T2.YEAR = '" + _param._rishuu_year + "' ");
        stb.append("                 AND T2.SEMESTER = '" + _param._semester + "' ");
        stb.append("                 AND T2.RIREKI_CODE = '" + _param._rirekiCode + "' ");
        stb.append("                 AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         LEFT JOIN CREDIT_MST L1  ");
        stb.append("                 ON  L1.YEAR = T2.YEAR ");
        //履修年度とログイン年度と不一致の場合
        if (_param._year.equals(_param._rishuu_year)) {
            stb.append("             AND L1.GRADE = '" + _param._nextGrade + "' ");
        } else {
            stb.append("             AND L1.GRADE = '" + _param._grade + "' ");
        }
        stb.append("                 AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = T1.COURSECD || T1.MAJORCD || T1.COURSECODE ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("             AND L1.CLASSCD = T2.CLASSCD ");
            stb.append("             AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("             AND L1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("                 AND L1.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("         AND T1.GRADE || T1.HR_CLASS = '" + hrClass + "' ");
        stb.append(" ) ");
        stb.append(" , T_SUB_CNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         COUNT(T1.SUBCLASSCD) AS SUB_CNT, ");
        stb.append("         SUM(T1.CREDITS) AS CREDITS_SUM ");
        stb.append("     FROM ");
        stb.append("         T_SUB T1 ");
        stb.append("     GROUP BY ");
        stb.append("         T1.SCHREGNO ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T2.HR_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T3.SCHREGNO, ");
        stb.append("     T3.NAME, ");
        stb.append("     L1.SUB_CNT, ");
        stb.append("     L1.CREDITS_SUM ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T2 ");
        stb.append("             ON  T2.YEAR = T1.YEAR ");
        stb.append("             AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T2.GRADE = T1.GRADE ");
        stb.append("             AND T2.HR_CLASS = T1.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN T_SUB_CNT L1 ON L1.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + hrClass + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    /** 生徒データクラス */
    private class Student {
        final String _hrName;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _sub_cnt;
        final String _credits_sum;

        Student(
                final String hrName,
                final String attendno,
                final String schregno,
                final String name,
                final String sub_cnt,
                final String credits_sum
        ) {
            _hrName = hrName;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _sub_cnt = sub_cnt;
            _credits_sum = credits_sum;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {

        final String _year;
        final String _semester;
        final String _grade;
        final String[] _classSelected; //学年・組
        final String _rishuu_year;
        final String _nextGrade;
        final String _ctrlDate;
        final String _useCurriculumcd;
        private final String _rirekiCode;

//        private boolean _seirekiFlg;
//        private String _nendo;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year      = request.getParameter("YEAR");
            _semester  = request.getParameter("SEMESTER");
            _grade  = request.getParameter("GRADE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _rishuu_year = request.getParameter("RISHUU_YEAR");
            final int nextGradeInt = Integer.parseInt(_grade) + 1;
            _nextGrade = (10 <= nextGradeInt) ? String.valueOf(nextGradeInt) : "0" + String.valueOf(nextGradeInt);
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _rirekiCode = request.getParameter("RIREKI_CODE");
//            _seirekiFlg = getSeirekiFlg(db2);
//            _nendo = _seirekiFlg ? _year + "年度": KNJ_EditDate.h_format_JP_N(_year+"-01-01")+"度";
        }

//        private boolean getSeirekiFlg(final DB2UDB db2) {
//            boolean seirekiFlg = false;
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    if ("2".equals(rs.getString("NAME1"))) {
//                        seirekiFlg = true; //西暦
//                    }
//                }
//            } catch (Exception e) {
//                log.error("getSeirekiFlg Exception", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            return seirekiFlg;
//        }
    }

}// クラスの括り
