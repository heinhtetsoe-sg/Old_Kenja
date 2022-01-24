package servletpack.KNJA;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * 一覧
 */
public class KNJA226A {

    private static final Log log = LogFactory.getLog(KNJA226A.class);
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

            printMain(db2, svf);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        final List hrClassList = createHrClassInfo(db2);

        svf.VrSetForm("KNJA226A.frm", 1);

        final int MAX_LEN = 8;
        int len = 0;
        String keepGrade = "";
        for (final Iterator it = hrClassList.iterator(); it.hasNext();) {
            final HrClass hrClassInfo = (HrClass) it.next();
            //改ページ
            if (len == MAX_LEN || ("1".equals(_param._kaipageGrade) && !keepGrade.equals(hrClassInfo._grade) && !"".equals(keepGrade))) {
                svf.VrEndPage();
                len = 0;
            }
            len++;
            keepGrade = hrClassInfo._grade;
            //タイトル
            svf.VrsOut("TITLE", _param.getNendo() + "　長子関係一覧");
            svf.VrsOut("DATE", _param.getDate(_param._ctrlDate));
            //クラス
            svf.VrsOut("HR_NAME"+len, hrClassInfo._hrName);
            svf.VrsOut("STAFF_NAME"+len, hrClassInfo._staffname);
            svf.VrsOut("BROSIS_NO"+len, String.valueOf(hrClassInfo._firstChildCnt));
            svf.VrsOut("BOY"+len, String.valueOf(hrClassInfo._boyCnt));
            svf.VrsOut("GIRL"+len, String.valueOf(hrClassInfo._girlCnt));
            svf.VrsOut("TOTAL"+len, String.valueOf(hrClassInfo._totalCnt));
            //生徒
            int gyo = 0;
            for (final Iterator it2 = hrClassInfo._studentList.iterator(); it2.hasNext();) {
                final Student student = (Student) it2.next();
                gyo++;
                svf.VrsOutn("ATTENDNO"+len, gyo, student._attendno);
                if ("1".equals(_param._printFurigana)) {
                    svf.VrsOutn("KANA"+len, gyo, student._nameKana);
                }
                svf.VrsOutn("NAME"+len, gyo, student._name);
                if ("1".equals(student._firstChild)) {
                    svf.VrsOutn("FIRST"+len, gyo, "*");
                } else {
                    svf.VrsOutn("BROSIS_HR_NAME"+len, gyo, student._broSisHrNameabbv);
                    final String[] seimeiName = getSeiMei(student._broSisName);
                    final String mei = seimeiName[1];
                    final String no = getMS932ByteLength(mei) <= 4 ? "_1" : getMS932ByteLength(mei) <= 6 ? "_2" : "_3";
                    svf.VrsOutn("BROSIS_NAME"+len+no, gyo, mei);
                }
            }

            _hasData = true;
        }
        if (0 < len) {
            svf.VrEndPage();
        }
    }

    private String[] getSeiMei(final String name) {
        final String[] split = StringUtils.split(StringUtils.replace(name, " ", "　"), "　");
        String sei = null;
        String mei = null;
        if (split != null) {
            sei = (split.length > 0) ? split[0] : null;
            mei = (split.length > 1) ? split[1] : null;
            int i = 2;
            while (i < split.length) {
                mei += split[i];
                i++;
            }
        }
        final String[] seimei = new String[2];
        seimei[0] = sei;
        seimei[1] = mei;
        return seimei;
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

    private List createHrClassInfo(final DB2UDB db2) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getHrClassInfoSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final HrClass hrClassInfo = new HrClass(
                        rs.getString("GRADE"),
                        rs.getString("HR_CLASS"),
                        rs.getString("HR_NAME"),
                        rs.getString("STAFFNAME")
                        );
                hrClassInfo.createStudent(db2);
                rtnList.add(hrClassInfo);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getHrClassInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T3.HR_NAME, ");
        stb.append("     L1.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("                                  AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T3.TR_CD1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T2.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        if (!"99".equals(_param._grade)) {
            stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS ");
        return stb.toString();
    }

    private class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _staffname;
        final List _studentList = new ArrayList();
        int _firstChildCnt = 0;
        int _boyCnt = 0;
        int _girlCnt = 0;
        int _totalCnt = 0;

        HrClass(
                final String grade,
                final String hrClass,
                final String hrName,
                final String staffname
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _staffname = staffname;
        }

        private void createStudent(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentSql(_grade, _hrClass);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    //長子カウント
                    final String firstChild = rs.getString("FIRST_CHILD");
                    if ("1".equals(firstChild)) _firstChildCnt++;
                    //男女計カウント
                    final String sex = rs.getString("SEX");
                    if ("1".equals(sex)) _boyCnt++;
                    if ("2".equals(sex)) _girlCnt++;
                    _totalCnt++;
                    final Student student = new Student(
                            rs.getString("SCHREGNO"),
                            rs.getString("ATTENDNO"),
                            rs.getString("NAME"),
                            rs.getString("NAME_KANA"),
                            firstChild
                            );
                    student.setBroSis(db2);
                    _studentList.add(student);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }

    }

    private String getStudentSql(final String grade, final String hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     L1.NAME, ");
        stb.append("     L1.NAME_KANA, ");
        stb.append("     L1.SEX, ");
        if ("1".equals(_param._useFamilyDat)) {
            stb.append("     FD.TYOUSHI_FLG AS FIRST_CHILD ");
        } else {
            stb.append("     L2.BASE_REMARK2 AS FIRST_CHILD ");
        }
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ");
        if ("1".equals(_param._useFamilyDat)) {
            stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST L2 ON L2.SCHREGNO = T1.SCHREGNO AND L2.BASE_SEQ = '009' ");
            stb.append("     LEFT JOIN FAMILY_DAT FD ON FD.FAMILY_NO = L2.BASE_REMARK1 AND FD.RELA_SCHREGNO = t1.SCHREGNO ");
        } else {
            stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST L2 ON L2.SCHREGNO = T1.SCHREGNO AND L2.BASE_SEQ = '007' ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + grade + "' ");
        stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _attendno;
        final String _name;
        final String _nameKana;
        final String _firstChild;
        String _broSisName = "";
        String _broSisHrNameabbv = "";
        
        Student(
                final String schregno,
                final String attendno,
                final String name,
                final String nameKana,
                final String firstChild
        ) {
            _schregno = schregno;
            _attendno = attendno;
            _name = name;
            _nameKana = nameKana;
            _firstChild = firstChild;
        }

        private void setBroSis(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getBroSisSql(_schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _broSisName = rs.getString("NAME");
                    _broSisHrNameabbv = rs.getString("HR_NAMEABBV");
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }
    }

    private String getBroSisSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        if ("1".equals(_param._useFamilyDat)) {
            stb.append(" WITH T_RELA AS ( ");
            stb.append("     SELECT ");
            stb.append("         FD2.RELA_SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         SCHREG_BASE_MST T1 ");
            stb.append("         INNER JOIN SCHREG_BASE_DETAIL_MST L2 ON L2.SCHREGNO = T1.SCHREGNO AND L2.BASE_SEQ = '009' ");
            stb.append("         INNER JOIN FAMILY_DAT FD1 ON FD1.FAMILY_NO = L2.BASE_REMARK1 AND FD1.RELA_SCHREGNO = t1.SCHREGNO ");
            stb.append("         INNER JOIN FAMILY_DAT FD2 ON FD2.FAMILY_NO = L2.BASE_REMARK1 AND FD2.RELA_SCHREGNO <> t1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("         T1.SCHREGNO = '" + schregno + "' ");
            stb.append("         AND VALUE(FD1.TYOUSHI_FLG, '') <> '1' ");
            stb.append("         AND VALUE(FD2.TYOUSHI_FLG, '') = '1' "); //兄弟が長子の時、表示
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     L1.NAME, ");
            stb.append("     L3.HR_NAMEABBV ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN T_RELA T2 ON T2.RELA_SCHREGNO = T1.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT L3 ON L3.YEAR = T1.YEAR AND L3.SEMESTER = T1.SEMESTER ");
            stb.append("                                   AND L3.GRADE = T1.GRADE AND L3.HR_CLASS = T1.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
        } else {
            stb.append(" WITH T_RELA AS ( ");
            stb.append("     SELECT ");
            stb.append("         RELA_SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         SCHREG_RELA_DAT ");
            stb.append("     WHERE ");
            stb.append("         SCHREGNO = '" + schregno + "' ");
            stb.append("         AND RELA_SCHREGNO IS NOT NULL ");
            stb.append("     GROUP BY ");
            stb.append("         RELA_SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     L1.NAME, ");
            stb.append("     L3.HR_NAMEABBV ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN T_RELA T2 ON T2.RELA_SCHREGNO = T1.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_BASE_DETAIL_MST L2 ON L2.SCHREGNO = T1.SCHREGNO AND L2.BASE_SEQ = '007' ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT L3 ON L3.YEAR = T1.YEAR AND L3.SEMESTER = T1.SEMESTER ");
            stb.append("                                   AND L3.GRADE = T1.GRADE AND L3.HR_CLASS = T1.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND L2.BASE_REMARK2 = '1' "); //兄弟が長子の時、表示
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
        }
        return stb.toString();
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlDate;
        final String _schoolKind;
        final String _grade; //99:全学年
        final String _printFurigana;
        final String _kaipageGrade;
        final String _useFamilyDat;

        private boolean _seirekiFlg;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _grade = request.getParameter("GRADE");
            _printFurigana = request.getParameter("PRINT_FURIGANA");
            _kaipageGrade = request.getParameter("KAIPAGE_GRADE");
            _useFamilyDat = request.getParameter("useFamilyDat");

            try {
                _seirekiFlg = getSeirekiFlg(db2);
            } catch (Exception e) {
                log.debug("getSeirekiFlg exception", e);
            }
        }

        private String getNendo() {
            return _seirekiFlg ? _year + "年度" : KNJ_EditDate.h_format_JP_N(_year + "-01-01") + "度";
        }

        private String getDate(final String date) {
            if (date == null) {
                return null;
            }
            return _seirekiFlg ? date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date) : KNJ_EditDate.h_format_JP(date) ;
        }

        private boolean getSeirekiFlg(final DB2UDB db2) {
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return seirekiFlg;
        }
    }

}// クラスの括り
