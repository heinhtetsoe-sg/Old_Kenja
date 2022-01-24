// kanji=漢字
/*
 * $Id: 29d4e0d279e907ec4069a2a6e8fc23b1d6565328 $
 *
 * 作成日: 2007/10/19 00:00:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWA;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
 * 生徒名簿（所属別）。A4版。
 * @author nakada
 * @version $Id: 29d4e0d279e907ec4069a2a6e8fc23b1d6565328 $
 */
public class KNJWA165 {
    /*pkg*/static final Log log = LogFactory.getLog(KNJWA165.class);

    private static final String FORM_FILE = "KNJWA165.frm";

    /** 文字数による出力項目切り分け基準 */
    private static final int NAME1_LENG = 15;           
    private static final int NAME2_LENG = 20;

    /*
     * 入学区分
     */
    /** 入学辞退 */
    private static final int ENT_DIV_REFUSAL = 7;           

    /** 印字件数/ページ */
    private static final int StudentNumMax = 50;

    private Form _form;
    private DB2UDB db2;

    private boolean _hasData;

    Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        dumpParam(request);
        _param = createParam(request);

        _form = new Form(FORM_FILE, response);
        db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param.load(db2);

            for (int i = 0; i < _param._belongingDiv.length; i++) {
            	log.debug(">>所属=" + _param._belongingDiv[i]);

            	final List students = createStudents(db2, _param._belongingDiv[i]);
            	printMain(students, i);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

	private void printMain(final List students, final int i) {
		int j = 0;	// １ページあたり件数
		int no = 0; // 所属＋組あたり件数
        
        Student student = null;

        String oldHrClass = null;
        String oldStaff = null;

        if (!students.isEmpty()) {
            student = (Student) students.get(0);
            oldHrClass = student._hrClass;
            oldStaff = student._staffcd;
        }

        for (Iterator it = students.iterator(); it.hasNext();) {
			student = (Student) it.next();

			j++;
			no++;

            if ((!student._hrClass.equals(oldHrClass)) ||
                    j > StudentNumMax) {

                printDate();
                printHeader(oldStaff, i);

                _form._svf.VrEndPage();
                _hasData = true;

                j = 1;
                oldHrClass = student._hrClass;
                oldStaff = student._staffcd;
            }

            printStudent(j, no, student);
		}

        if (j > 0) {
		    printDate();
		    printHeader(oldStaff, i);

		    _form._svf.VrEndPage();        			
		    _hasData = true;
		}
	}

	private void printHeader(String staffcd, final int i) {
		// 年度 
		int year = Integer.valueOf(_param._year).intValue();
		String date = nao_package.KenjaProperties.gengou(year);
		_form._svf.VrsOut("NENDO", date.toString() + "年度");

		// 所属
		_form._svf.VrsOut("BELONGING_NAME", _param._belongingMapString(_param._belongingDiv[i]));

		// 担任
		_form._svf.VrsOut("STAFFNAME", _param._staffMapString(staffcd));
	}

	/**
	 * @param student
	 */
	private void printStudent(int j, int k, Student student) {
        _form._svf.VrsOutn("NUMBER", j, String.valueOf(k));
	    _form._svf.VrsOutn("SCHREGNO", j, student._schregno);		

        String name = student._name;
        final String label;
        if (name != null && name.length() <= NAME1_LENG) {
            label = "NAME1";
         } else {
            label = "NAME2";
         }
         _form._svf.VrsOutn(label, j, name);
    }

    private void printDate() {
        _form._svf.VrsOut("DATE", getJDate(_param._date));
    }

    private String getJDate(String entDate) {
        try {
            final Calendar cal = KNJServletUtils.parseDate(entDate);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int dom = cal.get(Calendar.DAY_OF_MONTH);
            
            return nao_package.KenjaProperties.gengou(year, month, dom);

        } catch (final Exception e) {
            return null;
        }
    }

    private boolean openDb(final DB2UDB db2) {
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return true;
        }
        return false;
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /**
     * NULL値を""として返す。
     */
    private String nvlT(String val) {

        if (val == null) {
            return "";
        } else {
            return val;
        }
    }

    // ======================================================================
    private class Param {
        private final String _year;
        private final String _semester;
        private final String[] _belongingDiv; // 所属
        private final String _date;
        private final String _loginDate;

        private Map _belongingMap;
        private Map _staffMap;

        public Param(
                final String year,
                final String semester,
                final String staffcd,
                final String[] belongingDiv,
                final String date,
                final String loginDate
        ) {
            _year = year;
            _semester = semester;
            _belongingDiv = belongingDiv;
            _date = date;
            _loginDate = loginDate;
        }

        public void load(DB2UDB db2) throws SQLException {
            _belongingMap = createBelongingDat(db2);
            _staffMap = createStaffMap(db2);

            return;
        }

        public String _belongingMapString(String code) {
            return (String) nvlT((String)_belongingMap.get(code));
        }

        public String _staffMapString(String code) {
            return (String) nvlT((String)_staffMap.get(code));
        }
    }

    private Param createParam(final HttpServletRequest request) {
    	final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String staffcd = request.getParameter("STAFFCD");
        final String[] belongingDiv = request.getParameterValues("CATEGORY_SELECTED");
        final String date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
        final String loginDate = request.getParameter("LOGIN_DATE");

        final Param param = new Param(
                year,
                semester,
                staffcd,
                belongingDiv,
                date,
                loginDate
        );
        return param;
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    // ======================================================================

    private class Form {
        private Vrw32alp _svf;

        public Form(final String file, final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();
            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _svf.VrSetForm(file, 1);
        }

        private void closeSvf() {
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }

            final int ret = _svf.VrQuit();
            log.info("===> VrQuit():" + ret);
        }
    }

    // ======================================================================
    /**
     * 生徒。 
     */
    private class Student {
        private final String _hrClass;
        private final String _schregno;   // 学籍番号
        private final String _name;     // 氏名
        private final String _staffcd;

        Student(
              final String hrClass,
              final String schregno,
              final String name,
              final String staffcd
        ) {
            _hrClass = hrClass;
            _schregno = schregno;
            _name = name;
            _staffcd = staffcd;
        }

        public String toString() {
            return _schregno + ":" + _name;
        }
    }

    private List createStudents(final DB2UDB db2, String belongingDiv) throws SQLException {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sqlStudents(belongingDiv));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String hrClass = rs.getString("hrClass");
                final String schregno = rs.getString("schregno");
                final String name = rs.getString("name");
                final String staffcd = rs.getString("staffcd");
  
                final Student student = new Student(hrClass, schregno, name, staffcd);
                rtn.add(student);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String sqlStudents(String belongingDiv) {
        return " select"
                + "    T1.HR_CLASS as hrClass,"
                + "    T2.SCHREGNO as schregno,"
                + "    T3.NAME as name,"
                + "    T1.TR_CD1 as staffcd"
                + " from"
                + "    SCHREG_REGD_HDAT T1 left join SCHREG_REGD_DAT T2 on T1.YEAR = T2.YEAR and"
                + "    T1.SEMESTER = T2.SEMESTER and"
                + "    T1.GRADE = T2.GRADE and"
                + "    T1.HR_CLASS = T2.HR_CLASS"
                + "    left join SCHREG_BASE_MST T3 on T2.SCHREGNO = T3.SCHREGNO"
                + " where"
                + "    T1.YEAR = '" + _param._year + "'"
                + "    and T1.SEMESTER = '" + _param._semester + "'"
                + "    and T1.GRADE = '" + belongingDiv + "'"
                + "    and T3.ENT_DIV <> '" + ENT_DIV_REFUSAL + "'"
                + "    and (T3.GRD_DIV IS NULL"
                + "    or (T3.GRD_DIV IS NOT NULL"
                + "    and (T3.GRD_DATE IS NULL or ((T3.GRD_DATE IS NOT NULL) and (T3.GRD_DATE >= '" + _param._date + "')))))"
                + " order by"
                + "    T1.HR_CLASS, T2.SCHREGNO";
    }

    // ======================================================================
    /**
     * 所属データ。
     */
    public Map createBelongingDat(DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlBelongingDat());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String code = rs.getString("belonging_div");
            final String name = rs.getString("schoolname1");

            rtn.put(code, name);
        }

        return rtn;
    }

    private String sqlBelongingDat() {
        return " select"
                + "    BELONGING_DIV as belonging_div,"
                + "    SCHOOLNAME1 as schoolname1"
                + " from"
                + "    BELONGING_MST"
                ;
    }

    private Map createStaffMap(DB2UDB db2) throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlStaff());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String code = rs.getString("staffcd");
            final String name = rs.getString("staffname");

            rtn.put(code, name);
        }

        return rtn;
    }

    private String sqlStaff() {
        return " select"
                + "    STAFFCD as staffcd,"
                + "    STAFFNAME as staffname"
                + " from"
                + "    STAFF_MST"
                ;
    }
} // KNJWA165

// eof
