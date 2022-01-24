package servletpack.KNJA;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

/**
 *  学校教育システム 賢者 [事務管理] ＜ＫＮＪＧ１０１＞ 入学通知書・入校証・進級通知書
 */

public class KNJA191 {

    private static final Log log = LogFactory.getLog(KNJA191.class);

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
            log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
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

    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
    }

    /** SVF-FORM* */
    private boolean printMain(final Vrw32alp svf, final Param param, final List studentAllList) {
        boolean nonedata = false;

        final List groupList = getGroupList(studentAllList, 1);
        for (int pi = 0; pi < groupList.size(); pi++) {
            final List studentList = (List) groupList.get(pi);

            svf.VrSetForm("KNJA191.frm", 1);

            for (int i = 0; i < studentList.size(); i++) {
                final Student student = (Student) studentList.get(i);
                String fieldNo = "";

                svf.VrsOut("ZIPNO", student._zipcd);
                final String setAddr = student._addr1 + (student._addr2.length() > 0 ? "\n" + student._addr2 : "");
                final List addr28List = KNJ_EditKinsoku.getTokenList(setAddr, 28);
                final List addr36List = KNJ_EditKinsoku.getTokenList(setAddr, 36);
                if (addr28List.size() <= 5) {
                    printAddr(svf, addr28List, "1");
                } else {
                    printAddr(svf, addr36List, "2");
                }
                final String setName = student._name + "　様";
                fieldNo = getMS932ByteLength(setName) <= 20 ? "1" : getMS932ByteLength(setName) <= 30 ? "2" : "3";
                svf.VrsOut("NAME1_" + fieldNo, setName);
                fieldNo = getMS932ByteLength(student._staffName1) <= 20 ? "1" : "2";
                svf.VrsOut("TEACHER_NAME1_" + fieldNo, student._staffName1);
                fieldNo = getMS932ByteLength(student._staffName2) <= 20 ? "1" : "2";
                svf.VrsOut("TEACHER_NAME2_" + fieldNo, student._staffName2);
            }

            svf.VrEndPage(); // SVFフィールド出力
            nonedata = true;
        }

        return nonedata;
    }

    private void printAddr(final Vrw32alp svf, final List addr28List, final String fieldName) {
        int addrLine = 1;
        for (Iterator iterator = addr28List.iterator(); iterator.hasNext();) {
            String printAddr = (String) iterator.next();
            svf.VrsOut("ADDR" + addrLine + "_" + fieldName, printAddr);
            addrLine++;
        }
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
            final String sql = getSql(param);
            //log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                list.add(student);
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._staffName1 = rs.getString("STAFFNAME");
                student._staffName2 = rs.getString("STAFFNAME2");
                student._zipcd = rs.getString("ZIPCD");
                student._addr1 = rs.getString("ADDR1");
                student._addr2 = rs.getString("ADDR2");
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
        stb.append("    STF1.STAFFNAME, ");
        stb.append("    STF2.STAFFNAME AS STAFFNAME2, ");
        stb.append("    ADDR.ZIPCD,");
        stb.append("    VALUE(ADDR.ADDR1, '') AS ADDR1, ");
        stb.append("    VALUE(ADDR.ADDR2, '') AS ADDR2 ");
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
        stb.append(" LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
        stb.append(" LEFT JOIN STAFF_MST STF2 ON STF2.STAFFCD = REGDH.SUBTR_CD1 ");
        stb.append(" LEFT JOIN (");
        stb.append("   SELECT");
        stb.append("     W1.SCHREGNO,");
        stb.append("     W1.ZIPCD,");
        stb.append("     W1.ADDR1,");
        stb.append("     W1.ADDR2");
        stb.append("   FROM");
        stb.append("     SCHREG_ADDRESS_DAT W1");
        stb.append("     INNER JOIN (");
        stb.append("       SELECT SCHREGNO,");
        stb.append("              MAX(ISSUEDATE) AS ISSUEDATE");
        stb.append("       FROM   SCHREG_ADDRESS_DAT");
        stb.append("       WHERE  ISSUEDATE <= '" + param._edate + "'");
        stb.append("         AND (EXPIREDATE IS NULL " + " OR EXPIREDATE >= '" + param._sdate + "')");
        stb.append("       GROUP BY SCHREGNO ");
        stb.append("     ) W2 ON W2.SCHREGNO = W1.SCHREGNO AND W2.ISSUEDATE = W1.ISSUEDATE");
        stb.append(" ) ADDR ON ADDR.SCHREGNO = REGD.SCHREGNO");
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
        String _staffName1;
        String _staffName2;
        String _zipcd;
        String _addr1;
        String _addr2;
    }

    private static class Param {

        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _semester;
        final String _choice; // 1:クラス指定 2:個人指定
        final String _hrClassType; // 1:法定クラス 2:複式クラス
        final String[] _categorySelected;
        String _sdate;
        String _edate;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _ctrlYear = request.getParameter("CTRL_YEAR"); // 年度
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _semester = request.getParameter("SEMESTER"); // 学期
            _choice = request.getParameter("CHOICE");
            _hrClassType = request.getParameter("HR_CLASS_TYPE");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED"); // 学年・組
            loadSemesterMst(db2);
        }

        private void loadSemesterMst(final DB2UDB db2) {
            _sdate = null;
            _edate = null;

            final String sql = "SELECT SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _semester + "'";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _sdate = rs.getString("SDATE");
                    _edate = rs.getString("EDATE");
                }
            } catch (final SQLException e) {
                log.error("学期マスタ取得エラー:" + sql, e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }

}// クラスの括り
