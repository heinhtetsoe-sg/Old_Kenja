/*
 * $Id: d9b2407e6ee82a40047cb7a2637cad419dc975eb $
 *
 * 作成日: 2016/09/28
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJF015 {

    private static final Log log = LogFactory.getLog(KNJF015.class);

    private static final String csv = "csv";

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
            
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            response.setContentType("application/pdf");
            
            _param = createParam(db2, request);
            
            if (csv.equals(_param._cmd)) {
                final String filename = getTitle() + ".csv";
                CsvUtils.outputLines(log, response, filename, getCsvOutputLines(db2));
            } else {
                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());
                
                _hasData = false;
                
                printMain(svf, db2);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (csv.equals(_param._cmd)) {
            } else {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
                svf.VrQuit();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }

    }

    private void printMain(final Vrw32alp svf, final DB2UDB db2) {

        final int maxLine = 45;
        final String form = "KNJF015.frm";
        
        final List pageList = getPageList(Student.getStudentList(db2, _param), maxLine);

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List studentList = (List) pageList.get(pi);
            svf.VrSetForm(form, 1);
            
            svf.VrsOut("TITLE", getTitle()); // タイトル
            svf.VrsOut("DATE", "印刷日：" + KNJ_EditDate.h_format_JP(_param._ctrlDate)); // 印刷日
            
            for (int j = 0; j < studentList.size(); j++) {
                final Student student = (Student) studentList.get(j);
                svf.VrsOut("HR_NAME", student._hrName); // 年組
                final int line = j + 1;
                svf.VrsOutn("NAME", line, student._name); // 氏名
                svf.VrsOutn("ATTENDNO", line, NumberUtils.isDigits(student._attendno) ? Integer.valueOf(student._attendno).toString() : student._attendno); // 出席番号
                svf.VrsOutn("SCHREGNO", line, student._schregno); // 学籍番号
                svf.VrsOutn("HIGHT", line, student._height); // 身長
                svf.VrsOutn("WEIGHT", line, student._weight); // 体重
            }
            
            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    private List getCsvOutputLines(final DB2UDB db2) {
        final List hrList = getPageList(Student.getStudentList(db2, _param), 99999999);

        final List lines = new ArrayList();
        
        final List headerLine = Arrays.asList(new String[] {
                "出席番号",
                "学籍番号",
                "氏名",
                "身長",
                "体重",
        });
        final List blankLine = Arrays.asList(new String[] {});
        
        for (int pi = 0; pi < hrList.size(); pi++) {
            final List studentList = (List) hrList.get(pi);
            
            final Student student0 = (Student) studentList.get(0);
            lines.add(Arrays.asList(new String[] {student0._hrName}));
            lines.add(headerLine);
            
            for (int j = 0; j < studentList.size(); j++) {
                final Student student = (Student) studentList.get(j);
                lines.add(Arrays.asList(new String[] {
                                NumberUtils.isDigits(student._attendno) ? Integer.valueOf(student._attendno).toString() : student._attendno,
                                student._schregno,
                                student._name,
                                student._height,
                                student._weight,
                }));
            }
            
            lines.add(blankLine);
            _hasData = true;
        }

        return lines;
    }

    private String getTitle() {
        return KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度 身長体重計測一覧表（" + (NumberUtils.isDigits(_param._month) ? Integer.valueOf(_param._month).toString() : _param._month) + "月）";
    }

    /**
     * ページのリストを得る
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        String currentHr = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Student o = (Student) it.next();
            if (null == current || current.size() >= max || null == currentHr || !currentHr.equals(o._grade + o._hrClass)) {
                current = new ArrayList();
                currentHr = o._grade + o._hrClass;
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendno;
        final String _name;
        final String _height;
        final String _weight;

        Student(
            final String schregno,
            final String grade,
            final String hrClass,
            final String hrName,
            final String attendno,
            final String name,
            final String height,
            final String weight
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendno = attendno;
            _name = name;
            _height = height;
            _weight = weight;
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String attendno = rs.getString("ATTENDNO");
                    final String name = rs.getString("NAME");
                    final String height = rs.getString("HEIGHT");
                    final String weight = rs.getString("WEIGHT");
                    final Student student = new Student(schregno, grade, hrClass, hrName, attendno, name, height, weight);
                    list.add(student);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO ");
            stb.append("    ,T1.GRADE ");
            stb.append("    ,T1.HR_CLASS ");
            stb.append("    ,T2.HR_NAME ");
            stb.append("    ,T1.ATTENDNO ");
            stb.append("    ,BASE.NAME ");
            stb.append("    ,L1.HEIGHT ");
            stb.append("    ,L1.WEIGHT ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("         AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN MEDEXAM_DET_MONTH_DAT L1 ON L1.YEAR = T1.YEAR ");
            stb.append("         AND L1.SEMESTER = T1.SEMESTER ");
            stb.append("         AND L1.MONTH = '" + param._month + "' ");
            stb.append("         AND L1.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("    AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE ");
            stb.append("    ,T1.HR_CLASS ");
            stb.append("    ,T1.ATTENDNO ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _ctrlYear;
        final String _semester;
        final String _month;
        final String _ctrlDate;
        final String[] _categorySelected;
        final String _cmd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _month = request.getParameter("MONTH");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _cmd = request.getParameter("cmd");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
        }
    }
}

// eof
