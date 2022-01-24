package servletpack.KNJG;

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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *  学校教育システム 賢者 [事務管理] ＜ＫＮＪＧ１０１＞ 入学通知書・入校証・進級通知書
 */

public class KNJG106 {

    private static final Log log = LogFactory.getLog(KNJG106.class);

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
            log.fatal("$Revision: 61400 $ $Date: 2018-07-23 17:57:27 +0900 (月, 23 7 2018) $"); // CVSキーワードの取り扱いに注意
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
        
        final int max = 2;
        final List groupList = getGroupList(studentAllList, max);
        
        for (int pi = 0; pi < groupList.size(); pi++) {
            final List studentList = (List) groupList.get(pi);
            
            svf.VrSetForm("KNJG106.frm", 1);
            
            for (int i = 0; i < studentList.size(); i++) {
                final int gyo = (i % max) + 1;
                final Student student = (Student) studentList.get(i);
                
                svf.VrsOutn("NENDO", gyo, param._nendo + "　" + param._semestername);
                
                String fieldNo = KNJ_EditEdit.getMS932ByteLength(student._name) <= 26 ? "1" : KNJ_EditEdit.getMS932ByteLength(student._name) <= 32 ? "2" : "3";
                svf.VrsOutn("NAME" + fieldNo, gyo, student._name);
                svf.VrsOutn("HR_NAME", gyo, student._hrName);
                svf.VrsOutn("COURSE_NAME", gyo, student._coursecodename);
                svf.VrsOutn("SCHOOL_NAME", gyo, param._certifSchoolname);
                svf.VrsOutn("REMARK", gyo, param._certifSchoolRemark4);
            }
            
            svf.VrEndPage(); // SVFフィールド出力
            nonedata = true;
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
            final String sql = getSql(param);
            //log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                list.add(student);
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._gradeName1 = rs.getString("GRADE_NAME1");
                student._hrName = rs.getString("HR_NAME");
                student._coursecodename = rs.getString("COURSECODENAME");
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
        stb.append("    REGDG.GRADE_NAME1,");
        stb.append("    REGDH.HR_NAME,");
        stb.append("    CRS.COURSECODENAME");
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
        stb.append(" INNER JOIN SCHREG_REGD_DAT REGD2 ON REGD2.YEAR = REGD.YEAR AND ");
        stb.append("    REGD2.SEMESTER = REGD.SEMESTER AND ");
        stb.append("    REGD2.SCHREGNO = REGD.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR AND ");
        stb.append("    REGDG.GRADE = REGD.GRADE ");
        stb.append(" LEFT JOIN COURSECODE_MST CRS ON CRS.COURSECODE = REGD2.COURSECODE ");
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
        String _gradeName1;
        String _hrName;
        String _coursecodename;
    }
    
    private static class Param {
        
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _semester;
        final String _choice; // 1:クラス指定 2:個人指定
        final String _hrClassType; // 1:法定クラス 2:複式クラス
        final String[] _categorySelected;
        final String _certifSchoolname;
        final String _certifSchoolRemark4;
        final boolean _isSeireki;
        final String _nendo;
        String _semestername;
        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            _ctrlYear = request.getParameter("CTRL_YEAR"); // 年度
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _semester = request.getParameter("SEMESTER"); // 学期
            _choice = request.getParameter("CHOICE");
            _hrClassType = request.getParameter("HR_CLASS_TYPE");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED"); // 学年・組
            _certifSchoolname = loadSchoolName(db2, "SCHOOL_NAME");
            _certifSchoolRemark4 = loadSchoolName(db2, "REMARK4");
            loadSemesterMst(db2);
            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' ")));
            if (_isSeireki) {
            	_nendo = StringUtils.defaultString(_ctrlYear) + "年度";
            } else {
            	_nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_ctrlYear)) + "年度";
            }
        }

        private String loadSchoolName(final DB2UDB db2, final String field) {
            final String certifKindCd = "114";
            final String sql = "SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '" + certifKindCd + "'";
            final String rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
            log.debug("CERTIF_SCHOOL_DAT." + field + "=[" + rtn + "]");
            return rtn;
        }

        private void loadSemesterMst(final DB2UDB db2) {
            _semestername = null;

            final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _semester + "'";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _semestername = rs.getString("SEMESTERNAME");
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
