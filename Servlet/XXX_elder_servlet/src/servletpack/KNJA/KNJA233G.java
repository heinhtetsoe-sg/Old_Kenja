// kanji=漢字
package servletpack.KNJA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 */
public class KNJA233G {

    private static final Log log = LogFactory.getLog("KNJA233G.class");

    private boolean _hasData;

    private Param _param;


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
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);
            _hasData = false;

            _hasData = printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final Map printMap = getStudentMap(db2); //クラス毎の生徒Map
        if (printMap.size() == 0) {
            return false;
        }

        final int maxGyo = 52;
        svf.VrSetForm("KNJA233G.frm", 1);
        //クラス毎のループ
        for (Iterator ite = printMap.keySet().iterator(); ite.hasNext();) {
            final String ghrKey = (String)ite.next();
            final GradeHrclass ghrclass = (GradeHrclass)printMap.get(ghrKey);
            ghrclass.setCount();
            int gyo = 1; //行
            int seq = 1; //連番
            setTitle(svf, ghrclass);
            //生徒毎のループ
            for (Iterator ite2 = ghrclass._studentMap.keySet().iterator(); ite2.hasNext();) {
                if (gyo > maxGyo) {
                    gyo = 1;
                    svf.VrEndPage();
                    setTitle(svf, ghrclass);
                }
                final String stuKey = (String)ite2.next();
                final Student student = (Student)ghrclass._studentMap.get(stuKey);
                svf.VrsOutn("SEQ", gyo, String.valueOf(seq));
                svf.VrsOutn("HR", gyo, student._hr_Name);
                svf.VrsOutn("NO", gyo, Integer.valueOf(student._attendno).toString());
                svf.VrsOutn("NAME", gyo, student._name);
                svf.VrsOutn("SEX", gyo, student._sex);

                int len = 1;
                //科目毎のループ
                for (Iterator ite3 = student._subclassMap.keySet().iterator(); ite3.hasNext();) {
                    final String subclassKey = (String)ite3.next();
                    final Subclass subclass = (Subclass)student._subclassMap.get(subclassKey);
                    svf.VrsOutn("SELECT_SUBCLASS_NAME" + len, gyo, subclass._subclassabbv);
                    svf.VrsOutn("CHAIR_CLASS_NAME" + len, gyo, subclass._s_Class);
                    svf.VrsOutn("CHAIR_KIND_NAME" + len, gyo, subclass._syujukudo);
                    len++;
                }
                gyo++;
                seq++;
            }
            svf.VrEndPage();
        }

        return true;
    }

    private void setTitle(final Vrw32alp svf, final GradeHrclass ghrclass) {
        svf.VrsOut("TITLE", "HR名票(選択科目一覧)");
        svf.VrsOut("PRINT_DATE", "出力日:" + _param._loginDate.replace("-", "/") );
        svf.VrsOut("TOTAL", "男子:" + ghrclass._boy + "人 女子:" + ghrclass._girl + "人 合計:" + (ghrclass._boy + ghrclass._girl) + "人");
    }

    private Map getStudentMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        GradeHrclass ghrclass = null;
        Student student = null;
        final StringBuffer stb = new StringBuffer();
        //対象生徒データ
        stb.append(" WITH SCHREG_DATA AS ( ");
        stb.append(" SELECT ");
        stb.append("   REGD.SCHREGNO, ");
        stb.append("   REGD.YEAR, ");
        stb.append("   REGD.SEMESTER, ");
        stb.append("   REGD.GRADE, ");
        stb.append("   REGD.HR_CLASS, ");
        stb.append("   BASE.NAME, ");
        stb.append("   BASE.NAME_KANA, ");
        stb.append("   CASE WHEN BASE.SEX = '1' THEN '○' END AS SEX, ");
        stb.append("   REGD.ATTENDNO, ");
        stb.append("   REGD.COURSECD, ");
        stb.append("   REGD.MAJORCD, ");
        stb.append("   REGD.COURSECODE, ");
        stb.append("   REGD_H.HR_NAME ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT REGD ");
        stb.append(" INNER JOIN ");
        stb.append("   SCHREG_REGD_HDAT REGD_H ");
        stb.append("    ON REGD_H.YEAR = REGD.YEAR ");
        stb.append("   AND REGD_H.SEMESTER = REGD.SEMESTER ");
        stb.append("   AND REGD_H.GRADE = REGD.GRADE ");
        stb.append("   AND REGD_H.HR_CLASS = REGD.HR_CLASS ");
        stb.append(" INNER JOIN ");
        stb.append("   SCHREG_BASE_MST BASE ");
        stb.append("    ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   REGD.YEAR = '" + _param._year + "' ");
        stb.append("   AND REGD.SEMESTER = '" + _param._loginSemester + "' ");
        if ("1".equals(_param._disp)) {
            stb.append("   AND REGD.GRADE || REGD.HR_CLASS IN" + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("   AND REGD.SCHREGNO IN" + SQLUtils.whereIn(true, _param._categorySelected));
        }
        stb.append(" ORDER BY ");
        stb.append("   REGD.HR_CLASS,REGD.ATTENDNO ");
        //講座データ
        stb.append(" ), CHAIR_DATA AS ( ");
        stb.append(" SELECT ");
        stb.append("   CHAIR_S.CHAIRCD, ");
        stb.append("   CHAIR_S.SCHREGNO, ");
        stb.append("   SUBCLASS.SUBCLASSCD, ");
        stb.append("   SUBCLASS.SUBCLASSABBV, ");
        stb.append("   SUBCLASS.ELECTDIV, ");
        stb.append("   CHAIR_D.REMARK1 AS RETSU_NAME, ");
        stb.append("   CHAIR_D.REMARK2 AS S_CLASS, ");
        stb.append("   CHAIR_D.REMARK3 AS SYUJUKUDO ");
        stb.append(" FROM ");
        stb.append("   CHAIR_STD_DAT CHAIR_S ");
        stb.append(" INNER JOIN ");
        stb.append("   CHAIR_DETAIL_DAT CHAIR_D ");
        stb.append("    ON CHAIR_D.YEAR = CHAIR_S.YEAR ");
        stb.append("   AND CHAIR_D.SEMESTER = CHAIR_S.SEMESTER ");
        stb.append("   AND CHAIR_D.CHAIRCD = CHAIR_S.CHAIRCD ");
        stb.append("   AND CHAIR_D.SEQ = '004' ");
        stb.append("   AND CHAIR_D.REMARK1 IS NOT NULL ");
        stb.append(" INNER JOIN ");
        stb.append("   CHAIR_DAT CHAIR ");
        stb.append("    ON CHAIR.YEAR = CHAIR_S.YEAR ");
        stb.append("   AND CHAIR.SEMESTER = CHAIR_S.SEMESTER ");
        stb.append("   AND CHAIR.CHAIRCD = CHAIR_S.CHAIRCD ");
        stb.append(" INNER JOIN ");
        stb.append("   SCHREG_DATA T1 ");
        stb.append("    ON T1.YEAR = CHAIR_S.YEAR ");
        stb.append("   AND T1.SEMESTER = CHAIR_S.SEMESTER ");
        stb.append("   AND T1.SCHREGNO = CHAIR_S.SCHREGNO ");
        stb.append(" INNER JOIN ");
        stb.append("   SUBCLASS_MST SUBCLASS ");
        stb.append("    ON SUBCLASS.CLASSCD = CHAIR.CLASSCD ");
        stb.append("   AND SUBCLASS.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
        stb.append("   AND SUBCLASS.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
        stb.append("   AND SUBCLASS.SUBCLASSCD = CHAIR.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("   CHAIR_S.YEAR = '" + _param._year + "' ");
        stb.append("   AND '" + _param._date + "' BETWEEN CHAIR_S.APPDATE AND CHAIR_S.APPENDDATE ");
        //列名称で絞り込み
        if (!"ALL".equals(_param._b023)) {
            stb.append("   AND CHAIR_D.REMARK1 = '" + _param._b023 + "' ");
        }
        //習熟度チェックがONの場合条件に含める
        if (_param._syujukudoFlg) {
            stb.append("   AND (CHAIR_D.REMARK3 IS NOT NULL OR SUBCLASS.ELECTDIV = '1') ");
        } else {
            stb.append("   AND (SUBCLASS.ELECTDIV = '1') ");
        }
        stb.append(" ORDER BY ");
        stb.append("   T1.HR_CLASS, T1.ATTENDNO, CHAIR_S.CHAIRCD, SUBCLASS.SUBCLASSCD ");
        stb.append(" ) ");
        //メイン表
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.HR_NAME, ");
        stb.append("   T1.ATTENDNO, ");
        stb.append("   T1.NAME, ");
        stb.append("   T1.SEX, ");
        stb.append("   T2.SUBCLASSCD, ");
        stb.append("   T2.SUBCLASSABBV, ");
        stb.append("   T2.S_CLASS, ");
        stb.append("   T2.SYUJUKUDO ");
        stb.append(" FROM ");
        stb.append("   SCHREG_DATA T1 ");
        stb.append(" LEFT JOIN ");
        stb.append("   CHAIR_DATA T2 ");
        stb.append("    ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("   T1.HR_CLASS, T1.ATTENDNO, T2.RETSU_NAME, T2.CHAIRCD ");

        log.debug(" sql =" + stb.toString());

        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hr_class = rs.getString("HR_CLASS");
                final String hr_Name = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String subclasscd= rs.getString("SUBCLASSCD");
                final String subclassabbv = rs.getString("SUBCLASSABBV");
                final String s_Class = rs.getString("S_CLASS");
                final String syujukudo = rs.getString("SYUJUKUDO");

                final String key = grade + hr_class;
                if (retMap.containsKey(key)) {
                    ghrclass = (GradeHrclass)retMap.get(key);
                   } else {
                       ghrclass = new GradeHrclass(grade, hr_class);
                       retMap.put(key, ghrclass);
                   }

                if (ghrclass._studentMap.containsKey(schregno)) {
                    student = (Student)ghrclass._studentMap.get(schregno);
                } else {
                    student = new Student(schregno, hr_Name, attendno, name, sex);
                    ghrclass._studentMap.put(schregno, student);
                }

                if (!student._subclassMap.containsKey(subclasscd) && subclasscd != null) {
                    final Subclass subclass = new Subclass(subclasscd, subclassabbv, s_Class, syujukudo);
                    student._subclassMap.put(subclasscd, subclass);
                }
            }

        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;

    }

    private class GradeHrclass {
        final String _grade;
        final String _hrclass;
        final Map _studentMap;
        int _boy = 0;
        int _girl = 0;

        public GradeHrclass(final String grade, final String hrclass) {
            _grade = grade;
            _hrclass = hrclass;
            _studentMap = new LinkedMap();
        }

        //男女カウント
        public void setCount() {
            for (Iterator ite = _studentMap.keySet().iterator(); ite.hasNext();) {
                final String key = (String)ite.next();
                final Student student = (Student)_studentMap.get(key);
                if ("○".equals(student._sex)){
                    _boy++;
                } else {
                    _girl++;
                }
            }
        }
    }

    private class Student {
        final String _schregno;
        final String _hr_Name;
        final String _attendno;
        final String _name;
        final String _sex;
        final Map _subclassMap;

        public Student(final String schregno, final String hr_Name, final String attendno, final String name, final String sex) {
            _schregno = schregno;
            _hr_Name = hr_Name;
            _attendno = attendno;
            _name = name;
            _sex = sex;
            _subclassMap = new LinkedMap();
        }
    }

    private class Subclass {
        final String _subclasscd;
        final String _subclassabbv;
        final String _s_Class;
        final String _syujukudo;

        public Subclass(final String subclasscd, final String subclassabbv, final String s_Class, final String syujukudo) {
            _subclasscd = subclasscd;
            _subclassabbv = subclassabbv;
            _s_Class = s_Class;
            _syujukudo = syujukudo;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _loginDate;
        final String _loginSemester;
        final String _disp; //1:クラス 2:個人
        final String _date; //講座名簿日付
        final String _categorySelected[];
        final boolean _syujukudoFlg; //習熟度クラスを含めるか
        final String _b023; //列名称

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("LOGIN_YEAR");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _loginDate = request.getParameter("LOGIN_DATE");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _disp = request.getParameter("DISP");
            _date = request.getParameter("DATE").replace("/", "-");
            _syujukudoFlg = request.getParameter("SYUJUKUDO") != null;
            _b023 = request.getParameter("B023");
        }
    }
}

// eof
