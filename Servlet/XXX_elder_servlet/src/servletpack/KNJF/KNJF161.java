// kanji=漢字
/*
 * $Id: 1e0443f800c5ec012c3ecf0643d1a9f5df8f7ae3 $
 *
 * 作成日: 2010/06/04 13:38:26 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * 新入生調査票
 * @author nakamoto
 * @version $Id: 1e0443f800c5ec012c3ecf0643d1a9f5df8f7ae3 $
 */
public class KNJF161 {

    private static final Log log = LogFactory.getLog("KNJF161.class");

    private boolean _hasData;

    Param _param;

    private static final String FORM_SINNYUSEI   = "KNJF161_1.frm";
    private static final String FORM_HOKEN_PAGE1 = "KNJF161_2_1.frm";
    private static final String FORM_HOKEN_PAGE2 = "KNJF161_2_2.frm";
    private static final String FORM_HOKEN_PAGE3 = "KNJF161_2_3.frm";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        final List students = createStudents(db2);
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            //新入生調査票
            if (_param.isPrintSinnyusei()) printSinnyusei(svf, student);
            //保健調査票
            if (_param.isPrintHoken()) printHoken(svf, student);
        }
    }

    private void printSinnyusei(final Vrw32alp svf, final Student student) {
        svf.VrSetForm(FORM_SINNYUSEI, 1);
        svf.VrsOut("NENDO", _param._nendo);
        svf.VrsOut("EXAMNO", student.getExamno());
        svf.VrsOut("NAME", student._name);
        svf.VrsOut("KANA", student._kana);
        svf.VrsOut("BIRTHDAY", student.getBirthday() + student.getAge());
        svf.VrEndPage();
        _hasData = true;
    }

    private void printHoken(final Vrw32alp svf, final Student student) {
        //1枚目
        svf.VrSetForm(FORM_HOKEN_PAGE1, 1);
        svf.VrsOut("EXAMNO", student.getExamno());
        svf.VrsOut("NAME", student._name);
        svf.VrEndPage();
        //2枚目
        svf.VrSetForm(FORM_HOKEN_PAGE2, 1);
        svf.VrsOut("EXAMNO", student.getExamno()); //マスク
        svf.VrEndPage();
        //3枚目
        svf.VrSetForm(FORM_HOKEN_PAGE3, 1);
        svf.VrsOut("EXAMNO", student.getExamno()); //マスク
        svf.VrEndPage();
        _hasData = true;
    }

    private List createStudents(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlStudents();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String kana = rs.getString("NAME_KANA");
                final String birthday = rs.getString("BIRTHDAY");
                final String age = rs.getString("AGE");

                final Student student = new Student(examno, name, kana, birthday, age);
                rtn.add(student);
            }
        } catch (final Exception ex) {
            log.error("生徒のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }
    
    private String sqlStudents() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     T1.BIRTHDAY, ");
        stb.append("     CASE WHEN T1.BIRTHDAY IS NOT NULL ");
        stb.append("          THEN YEAR(T1.ENTEXAMYEAR || '-04-01' - T1.BIRTHDAY) ");
        stb.append("     END AS AGE ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST L1 ON  L1.NAMECD2 = T1.JUDGEMENT ");
        stb.append("                           AND L1.NAMECD1 = 'L013' ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR  = '" + _param._entexamyear + "' AND ");
        stb.append("     T1.PROCEDUREDIV = '1' AND ");  //手続済み
        stb.append("     T1.ENTDIV       = '1' AND ");  //入学済み
        stb.append("     L1.NAMESPARE1   = '1' ");      //合格
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ");
        return stb.toString();
    }

    private class Student {
        private final String _examno;
        private final String _name;
        private final String _kana;
        private final String _birthday;
        private final String _age;

        public Student(
                final String examno,
                final String name,
                final String kana,
                final String birthday,
                final String age
        ) {
            _examno = examno;
            _name = name;
            _kana = kana;
            _birthday = birthday;
            _age = age;
        }

        private String getExamno() {
            return String.valueOf(Integer.parseInt(_examno));
        }

        private String getBirthday() {
            if (null == _birthday) return "平成    年    月    日 生 ";
            return KNJ_EditDate.h_format_JP_Bth(_birthday);
        }

        private String getAge() {
            if (null == _age) return "（    歳）";
            return "（" + _age + "歳）";
        }

        public String toString() {
            return _examno + ":" + _name;
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
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
        private final String _entexamyear;
        private final String _printDiv;
        private final String _nendo;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_entexamyear));
            _nendo = gengou + "年度";
            _printDiv = request.getParameter("PRINT_DIV");
        }

        private boolean isPrintSinnyusei() {
            return "1".equals(_printDiv);
        }

        private boolean isPrintHoken() {
            return "2".equals(_printDiv);
        }
    }
}

// eof
