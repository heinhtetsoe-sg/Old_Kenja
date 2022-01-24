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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;


/**
 * クラス別受講講座一覧
 */
public class KNJB1309 {

    private static final Log log = LogFactory.getLog(KNJB1309.class);
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

            for (int i = 0; i < _param._categorySelected.length; i++) {

                // 生徒データを取得
                final List studentList = createStudentInfoData(db2, _param._categorySelected[i]);
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
        boolean hasData = false;
        svf.VrSetForm("KNJB1309.frm", 4);

        final int maxLine = 55;
        int line = 1;
        int grp = 0; //グループコード
        String defGradeHrClass = "";
        String defSchregno = "";
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            final boolean flg1 = (!"".equals(defGradeHrClass) && !student._gradeHrClass.equals(defGradeHrClass));
            final boolean flg2 = (line > maxLine);
            if(flg1 || flg2) {
                line = 1;
                svf.VrEndPage();
                svf.VrSetForm("KNJB1309.frm", 4);
            }
            //ヘッダー
            printHeader(svf, student);

            String name = student._name;
            if(!student._schregno.equals(defSchregno)) {
                grp += 1;
            } else {
                name = "";
            }
            svf.VrsOut("GRPCD", String.valueOf(grp)); //グループコード
            svf.VrsOut("NO", (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : ""); //出席番号
            final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 30 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, name);           //氏名
            svf.VrsOut("SEX", student._sex);                //性別
            svf.VrsOut("CHAIR_NAME", student._chairname);   //講座名
            final String appdate = "".equals(student._appdate) ? "" : student._appdate.replace('-', '/');
            svf.VrsOut("SDATE", appdate);                   //運用開始日
            final String appenddate = "".equals(student._appenddate) ? "" : student._appenddate.replace('-', '/');
            svf.VrsOut("FDATE", appenddate);                //運用終了日

            defGradeHrClass = student._gradeHrClass;
            defSchregno = student._schregno;
            line++;
            hasData = true;
            svf.VrEndRecord();
        }
        svf.VrEndPage();
        return  hasData;
    }

    private void printHeader(final Vrw32alp svf, final Student student) {
        final String title = _param._nendo + "　　" +_param._semesterName + "　クラス別受講講座一覧表";
        svf.VrsOut("TITLE", title);
        svf.VrsOut("HR_NAME", student._hrName);
        svf.VrsOut("SUBCLASS_NAME", student._subclassname);
        svf.VrsOut("DATE", _param._outDate);
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
            final String sql = getOutDataSql(hrClass);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student studentInfo = new Student(
                        StringUtils.defaultString(rs.getString("GRADE_HR_CLASS")),
                        StringUtils.defaultString(rs.getString("HR_NAME")),
                        StringUtils.defaultString(rs.getString("SUBCLASSCD")),
                        StringUtils.defaultString(rs.getString("SUBCLASSNAME")),
                        StringUtils.defaultString(rs.getString("SCHREGNO")),
                        StringUtils.defaultString(rs.getString("ATTENDNO")),
                        StringUtils.defaultString(rs.getString("NAME")),
                        StringUtils.defaultString(rs.getString("SEX")),
                        StringUtils.defaultString(rs.getString("CHAIRCD")),
                        StringUtils.defaultString(rs.getString("CHAIRNAME")),
                        StringUtils.defaultString(rs.getString("APPDATE")),
                        StringUtils.defaultString(rs.getString("APPENDDATE"))
                );
                rtnList.add(studentInfo);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getOutDataSql(final String hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_CHAIR AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T2.CHAIRCD, ");
        stb.append("         T2.CHAIRNAME, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append(" T2.SUBCLASSCD AS SUBCLASSCD, ");
        }
        stb.append("         S1.SUBCLASSNAME, ");
        stb.append("         T1.APPDATE, ");
        stb.append("         T1.APPENDDATE ");
        stb.append("     FROM ");
        stb.append("         CHAIR_STD_DAT T1 ");
        stb.append("         INNER JOIN CHAIR_DAT T2 ");
        stb.append("                 ON T2.YEAR     = T1.YEAR ");
        stb.append("                AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("                AND T2.CHAIRCD  = T1.CHAIRCD ");
        stb.append("         LEFT JOIN SUBCLASS_MST S1 ");
        stb.append("                ON S1.SUBCLASSCD    = T2.SUBCLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("               AND S1.CLASSCD       = T2.CLASSCD ");
            stb.append("               AND S1.SCHOOL_KIND   = T2.SCHOOL_KIND ");
            stb.append("               AND S1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '" + _param._subclass+ "' ");
        } else {
            stb.append("         AND T2.SUBCLASSCD = '" + _param._subclass + "' ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.GRADE || T1.HR_CLASS AS GRADE_HR_CLASS, ");
        stb.append("     HDAT.HR_NAME, ");
        stb.append("     T2.SUBCLASSCD, ");
        stb.append("     T2.SUBCLASSNAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     T2.CHAIRCD, ");
        stb.append("     T2.CHAIRNAME, ");
        stb.append("     T2.APPDATE, ");
        stb.append("     T2.APPENDDATE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN T_CHAIR T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT HDAT ");
        stb.append("             ON HDAT.YEAR     = T1.YEAR ");
        stb.append("            AND HDAT.SEMESTER = T1.SEMESTER ");
        stb.append("            AND HDAT.GRADE    = T1.GRADE ");
        stb.append("            AND HDAT.HR_CLASS = T1.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD2 = BASE.SEX AND Z002.NAMECD1 = 'Z002'");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR         = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        if ("1".equals(_param._disp)) {
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + hrClass + "' ");
        } else {
            stb.append("     AND T1.GRADE = '" + hrClass + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     GRADE_HR_CLASS, ");
        stb.append("     ATTENDNO, ");
        stb.append("     SCHREGNO, ");
        stb.append("     SUBCLASSCD, ");
        stb.append("     APPDATE, ");
        stb.append("     APPENDDATE, ");
        stb.append("     CHAIRCD ");
        return stb.toString();
    }

    /** 生徒データクラス */
    private class Student {
    	final String _gradeHrClass;
        final String _hrName;
        final String _subclasscd;
        final String _subclassname;
        final String _schregno;
        final String _attendno;
        final String _name;
        final String _sex;
        final String _chaircd;
        final String _chairname;
        final String _appdate;
        final String _appenddate;

        Student(
                final String gradeHrClass,
                final String hrName,
                final String subclasscd,
                final String subclassname,
                final String schregno,
                final String attendno,
                final String name,
                final String sex,
                final String chaircd,
                final String chairname,
                final String appdate,
                final String appenddate
        ) {
            _gradeHrClass = gradeHrClass;
            _hrName = hrName;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _schregno = schregno;
            _attendno = attendno;
            _name = name;
            _sex = sex;
            _chaircd = chaircd;
            _chairname = chairname;
            _appdate = appdate;
            _appenddate = appenddate;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 72068 $ $Date: 2020-01-31 10:23:50 +0900 (金, 31 1 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {

        final String _year;
        final String _disp;
        final String _semester;
        final String _subclass;
        final String[] _categorySelected; //学年・組
        final String _ctrlDate;
        final String _useCurriculumcd;

        final String _nendo;
        final String _semesterName;
        final String _outDate;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year      = request.getParameter("YEAR");
            _disp  = request.getParameter("DISP");
            _semester  = request.getParameter("SEMESTER");
            _subclass  = request.getParameter("SUBCLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _semesterName = getSemesterName(db2);
            _outDate = _ctrlDate == null ? null : _ctrlDate.replace('-', '/');
        }

        private String getSemesterName(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

    }

}// クラスの括り
