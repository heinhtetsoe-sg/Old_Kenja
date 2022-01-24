package servletpack.KNJG;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *  学校教育システム 賢者 [事務管理] ＜ＫＮＪＧ１０１＞ 入学通知書・入校証・進級通知書
 */

public class KNJG101 {

    private static final Log log = LogFactory.getLog(KNJG101.class);

    /**
     * HTTP Get リクエストの処理
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        final Vrw32alp svf = new Vrw32alp(); // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; // Databaseクラスを継承したクラス

        // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        // svf設定
        svf.VrInit(); // クラスの初期化
        svf.VrSetSpoolFileStream(outstrm); // PDFファイル名の設定

        boolean nonedata = false; // 該当データなしフラグ
        try {
            // ＤＢ接続
            try {
                db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
                db2.open();
            } catch (Exception ex) {
                log.error("DB2 open error!", ex);
                return;
            }
            log.fatal("$Revision: 72995 $ $Date: 2020-03-16 15:15:56 +0900 (月, 16 3 2020) $"); // CVSキーワードの取り扱いに注意
            KNJServletUtils.debugParam(request, log);
            final Param param = new Param(request, db2);

            // SVF出力
            final List studentList = getStudentList(db2, param);
            if (printMain(svf, param, studentList)) {
                nonedata = true; // 生徒出力のメソッド
            }

        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            // 該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            // 終了処理
            svf.VrQuit();
            db2.commit();
            db2.close(); // DBを閉じる
        }
    }

    /** SVF-FORM* */
    private boolean printMain(final Vrw32alp svf, final Param param, final List studentAllList) {
        boolean nonedata = false;

        if ("2".equals(param._form)) {

            final List groupList = getGroupList(studentAllList, 5); // 5行
            for (int pi = 0; pi < groupList.size(); pi++) {
                final List studentList = (List) groupList.get(pi);

                svf.VrSetForm("KNJG101_2.frm", 1);

                for (int i = 0; i < studentList.size(); i++) {
                    final int gyo = i + 1;
                    final Student student = (Student) studentList.get(i);
                    for (int retu = 1; retu <= 2; retu++) { // 1人の生徒を1行に2列表示
                        svf.VrsOutn("YEAR" + retu, gyo, student._gradNendo);
                        svf.VrsOutn("SCHREGNO" + retu, gyo, student._schregno);
                    }
                    svf.VrsOutn("NAME", gyo, student._name);
                }
                svf.VrEndPage(); // SVFフィールド出力
                nonedata = true;
            }

        } else {

            final int max = 2;
            final List groupList = getGroupList(studentAllList, max);

            for (int pi = 0; pi < groupList.size(); pi++) {
                final List studentList = (List) groupList.get(pi);

                svf.VrSetForm("KNJG101_1.frm", 1);

                for (int i = 0; i < studentList.size(); i++) {
                    final int gyo = (i % max) + 1;
                    final Student student = (Student) studentList.get(i);
                    final String nyuugakuNyuuen = "K".equals(student._schoolKind) ? "入園" : "入学";
                    final String title = "1".equals(param._form) ? nyuugakuNyuuen + "通知書" : "3".equals(param._form) ? "進級通知書" : "";
                    final String subtitle = "1".equals(param._form) ? nyuugakuNyuuen + "おめでとうございます" : "3".equals(param._form) ? "進級おめでとうございます" : "";

                    svf.VrsOutn("TITLE", gyo, title);
                    svf.VrsOutn("SUB_TITLE", gyo, subtitle);
                    if ("K".equals(student._schoolKind)) {
                        svf.VrsOutn("NAME", gyo, student._hrName);
                        svf.VrsOutn("CLASS_NAME", gyo, student._nameKana);
                        svf.VrsOutn("NAME_HEADER", gyo, "クラス");
                        svf.VrsOutn("CLASS_NAME_HEADER", gyo, "氏　名");
                    } else {
                        svf.VrsOutn("NAME", gyo, student._name);
                        svf.VrsOutn("CLASS_NAME", gyo, student._hrName);
                        svf.VrsOutn("NAME_HEADER", gyo, "氏　名");
                        svf.VrsOutn("CLASS_NAME_HEADER", gyo, "クラス");
                    }
                    svf.VrsOutn("TEACHER1", gyo, student._staffName1);
                    svf.VrsOutn("TEACHER1_HEADER", gyo, "担　任");
                    if ("1".equals(param._noPrintFuku)) {
                    } else {
                        svf.VrsOutn("FUKU", gyo, "副担任");
                        svf.VrsOutn("TEACHER2", gyo, student._staffName2);
                        svf.VrsOutn("TEACHER3", gyo, student._staffName3);
                    }
                    svf.VrsOutn("SCHOOL_NAME", gyo, param._certifSchoolname);
                    if ("K".equals(student._schoolKind)) {
                        svf.VrsOutn("SCHOOL_NAME2", gyo, param._certifSchoolremark1);
                    }
                }

                svf.VrEndPage(); // SVFフィールド出力
                nonedata = true;
            }
        }

        return nonedata;
    }

    private List getGroupList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = (Object) it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private List getStudentList(final DB2UDB db2, final Param param) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List list = new ArrayList();
        try {
            final DecimalFormat df = new DecimalFormat("00");
            final String sql = getSql(param);
            //log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                list.add(student);
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._nameKana = rs.getString("NAME_KANA");
                student._schoolKind = rs.getString("SCHOOL_KIND");
                if ("K".equals(student._schoolKind)) {
                    final String grade = StringUtils.defaultString(rs.getString("GRADE_CD"), rs.getString("GRADE"));
                    if (NumberUtils.isDigits(grade)) {
                        student._gradNendo = "K" + df.format((5 - Integer.parseInt(grade) + Integer.parseInt(param._ctrlYear)) % 100);
                    }
                } else {
                    final String grade = StringUtils.defaultString(rs.getString("GRADE_CD"), rs.getString("GRADE"));
                    if (NumberUtils.isDigits(grade)) {
                        student._gradNendo = df.format((6 - Integer.parseInt(grade) + Integer.parseInt(param._ctrlYear)) % 100);
                    }
                }
                student._hrName = rs.getString("HR_NAME");
                student._staffName1 = rs.getString("STAFFNAME");
                student._staffName2 = rs.getString("STAFFNAME2");
                student._staffName3 = rs.getString("STAFFNAME3");
            }
        } catch (Exception ex) {
            log.error("getStudentList error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    /** PrepareStatement作成* */
    private String getSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("    REGD.SCHREGNO,");
        stb.append("    BASE.NAME,");
        stb.append("    BASE.NAME_KANA,");
        stb.append("    REGD.GRADE,");
        stb.append("    REGDH.HR_NAMEABBV,");
        stb.append("    REGDH.HR_NAME,");
        stb.append("    REGDG.SCHOOL_KIND,");
        stb.append("    REGDG.GRADE_CD,");
        stb.append("    STF1.STAFFNAME, ");
        stb.append("    STF2.STAFFNAME AS STAFFNAME2, ");
        stb.append("    STF3.STAFFNAME AS STAFFNAME3 ");
        stb.append(" FROM ");
        if ("2".equals(param._hrClassType)) {
            stb.append(" SCHREG_REGD_FI_DAT REGD ");
        } else {
            stb.append(" SCHREG_REGD_DAT REGD ");
        }
        stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        if ("2".equals(param._hrClassType)) {
            stb.append(" INNER JOIN SCHREG_REGD_FI_HDAT REGDH ON ");
        } else {
            stb.append(" INNER JOIN SCHREG_REGD_HDAT REGDH ON ");
        }
        stb.append("    REGDH.YEAR = REGD.YEAR AND ");
        stb.append("    REGDH.SEMESTER = REGD.SEMESTER AND ");
        stb.append("    REGDH.GRADE = REGD.GRADE AND ");
        stb.append("    REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append(" LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR AND ");
        stb.append("    REGDG.GRADE = REGD.GRADE ");
        stb.append(" LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
        stb.append(" LEFT JOIN STAFF_MST STF2 ON STF2.STAFFCD = REGDH.SUBTR_CD1 ");
        stb.append(" LEFT JOIN STAFF_MST STF3 ON STF3.STAFFCD = REGDH.SUBTR_CD2 ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR='" + param._ctrlYear + "' AND ");
        stb.append("     REGD.SEMESTER='" + param._semester + "' AND ");
        if ("1".equals(param._choice)) {
            stb.append("     REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
        } else {
            stb.append("     REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
        }
        stb.append(" ORDER BY REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO");
        return stb.toString();
    }

    private static class Student {
        String _schregno;
        String _name;
        String _nameKana;
        String _gradNendo;
        String _hrName;
        String _schoolKind;
        String _staffName1;
        String _staffName2;
        String _staffName3;
    }

    private static class Param {

        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _semester;
        final String _form;
        final String _choice; // 1:クラス指定 2:個人指定
        final String _hrClassType; // 1:法定クラス 2:複式クラス
        final String[] _categorySelected;
        final String _noPrintFuku; // 副担任を印刷しない
        final String _certifSchoolname;
        final String _certifSchoolremark1;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _ctrlYear = request.getParameter("CTRL_YEAR"); // 年度
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _semester = request.getParameter("SEMESTER"); // 学期
            _form = request.getParameter("FORM"); // 名票種類
            _choice = request.getParameter("CHOICE");
            _hrClassType = request.getParameter("HR_CLASS_TYPE");
            _noPrintFuku = request.getParameter("NO_PRINT_FUKU");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED"); // 学年・組
            _certifSchoolname = loadSchoolName(db2, "SCHOOL_NAME");
            _certifSchoolremark1 = loadSchoolName(db2, "REMARK1");
        }

        private String loadSchoolName(final DB2UDB db2, final String field) {
            String rtn = null;

            final String certifKindCd = "114";
            final String sql = "SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '" + certifKindCd + "'";
            rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
            log.debug("CERTIF_SCHOOL_DAT の学校名称=[" + rtn + "]");
            return rtn;
        }
    }

}// クラスの括り
