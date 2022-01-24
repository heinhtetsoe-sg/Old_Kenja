/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: ec0f540ced0a46d5b72bfe38df3f89fa07bbedd2 $
 *
 * 作成日: 2018/10/30
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJE156 {
    private static final Log log = LogFactory.getLog(KNJE156.class);

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

            printMain(db2, svf);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String form = "KNJE156.frm";
        int colcntmax = 10;


        int pagecntmax = 45;
        svf.VrSetForm(form, 1);

        for (int lpcnt = 0;lpcnt < _param._grhr_ClassSelected.length;lpcnt++) {
        	//タイトル出力
        	setTitle(db2, svf, _param._grhr_ClassSelected[lpcnt], colcntmax);
        	int rowcnt = 0;

            final List studentList = Student.getStudentList(db2, _param, _param._grhr_ClassSelected[lpcnt]);
            for (int pi = 0; pi < studentList.size(); pi++) {
                final Student student = (Student) studentList.get(pi);

                if (pagecntmax <= rowcnt) {
                	//改ページ処理
                    svf.VrEndPage();
                    svf.VrSetForm(form, 1);
                }
                svf.VrsOutn("NO", rowcnt+1, String.valueOf(rowcnt + 1));

                final int namelen = KNJ_EditEdit.getMS932ByteLength(student._name);
                final String namefield = namelen > 30 ? "3" : namelen > 20 ? "2" : "1";
                svf.VrsOutn("NAME" + namefield, rowcnt+1, student._name);

                //観点毎にループ
                int colcnt = 1;
                for (Iterator ite = _param._behaviorSubMstMap.keySet().iterator();ite.hasNext();) {
                    final String kcode = (String)ite.next();
                    //取得できれば出力処理を行う
                    final String recval = (String)student._recordMap.get(kcode);
                    if (recval != null) {
                    	svf.VrsOutn("RECORD" + colcnt, rowcnt+1, recval);
                    }
                    colcnt++;
                }
                svf.VrEndRecord();
                rowcnt++;
                _hasData = true;
            }
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final String hrsel, final int colcntmax) {
    	svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "　行動の記録"); //年度+タイトル

    	HrClassInfo putwk = (HrClassInfo)_param._hrInfoMap.get(hrsel);

    	svf.VrsOut("HR_NAME", putwk._hrname); //クラス名

    	int tnamelen = KNJ_EditEdit.getMS932ByteLength(putwk._teacher);
    	String tnamefield = tnamelen > 30 ? "3" : tnamelen > 20 ? "2" : "1";
    	svf.VrsOut("TR_NAME" + tnamefield , putwk._teacher); //担任名

        //表のタイトル(観点毎にループ)

    	int colNoCnt = 0;
        for (Iterator ite = _param._behaviorSubMstMap.keySet().iterator();ite.hasNext();) {
            final String kcode = (String)ite.next();
        	if (colNoCnt >= colcntmax) {
        		continue;
        	}
            final String kname = (String)_param._behaviorSubMstMap.get(kcode);
            final String kanmefield = "1";
            svf.VrsOut("ITEM_NAME" + String.valueOf(colNoCnt+1) + "_" + kanmefield, kname);
            colNoCnt++;
        }

        return;
    }

    private static class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _name;
        final Map _recordMap;

        Student(
            final String schregno,
            final String grade,
            final String hrClass,
            final String attendno,
            final String name
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _name = name;

            _recordMap = new HashMap();
        }

        public static List getStudentList(final DB2UDB db2, final Param param, final String hrsel) {
            final List list = new ArrayList();
            final Map studentMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param, hrsel);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    if (null == studentMap.get(schregno)) {
                        final String grade = rs.getString("GRADE");
                        final String hrClass = rs.getString("HR_CLASS");
                        final String attendno = rs.getString("ATTENDNO");
                        final String name = rs.getString("NAME");
                        final Student student = new Student(schregno, grade, hrClass, attendno, name);
                        list.add(student);
                        studentMap.put(schregno, student);
                    }

                    final Student student = (Student) studentMap.get(schregno);
                    student._recordMap.put(rs.getString("CODE"), rs.getString("RECORD"));

                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param, final String hrsel) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   REGD.SCHREGNO, ");
            stb.append("   REGD.GRADE, ");
            stb.append("   REGD.HR_CLASS, ");
            stb.append("   REGD.ATTENDNO, ");
            stb.append("   BASE.NAME, ");
            stb.append("   BSD.CODE, ");
            stb.append("   BSD.RECORD ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT REGD ");
            stb.append("   LEFT JOIN BEHAVIOR_DAT BSD ");
            stb.append("      ON REGD.YEAR = BSD.YEAR ");
            stb.append("     AND REGD.SCHREGNO = BSD.SCHREGNO ");
            stb.append("     AND BSD.DIV = '3' ");
            stb.append("   LEFT JOIN SCHREG_BASE_MST BASE ");
            stb.append("      ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("   REGD.YEAR = '" + param._year + "' ");
            stb.append("   AND REGD.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND REGD.GRADE || REGD.HR_CLASS = '" + hrsel + "' ");
            stb.append(" ORDER BY ");
            stb.append("   REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO, BSD.CODE ");

            return stb.toString();
        }
    }

    private static class Semester {
        private final String _semester;
        private final String _name;
        private final String _sDate;
        private final String _eDate;

        public Semester(
                final String code,
                final String name,
                final String sDate,
                final String eDate
        ) {
            _semester = code;
            _name = name;
            _sDate = sDate;
            _eDate = eDate;
        }

        public String toString() {
            return _semester + "/" + _name + "/" + _sDate + "/" + _eDate;
        }
    }

    private static class HrClassInfo {
        private final String _hrclass;
        private final String _hrname;
        private final String _teacher;
        public HrClassInfo(
                final String hrclass,
                final String hrname,
                final String teacher
        ) {
        	_hrclass = hrclass;
        	_hrname = hrname;
        	_teacher = teacher;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 65506 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _grade;
        final String _schoolkind;

        private String _sDate;

        final String[] _grhr_ClassSelected;

        final Map _behaviorSubMstMap;
        final Map _hrInfoMap;

        private Map _semesterMap = new HashMap();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _schoolkind = getSchregRegdGdat(db2, "SCHOOL_KIND");//request.getParameter("SCHOOL_KIND");

            _grhr_ClassSelected = request.getParameterValues("CLASS_SELECTED");

            _behaviorSubMstMap = getbehaviorSubMst(db2);
            _hrInfoMap = getHrInfo(db2);
            loadSemester(db2, _year);
        }

        private Map getHrInfo(final DB2UDB db2) {
            Map rtnMap = new LinkedMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
            	final StringBuffer stb = new StringBuffer();
            	stb.append(" SELECT ");
            	stb.append("   HDAT.GRADE || HDAT.HR_CLASS AS HR_CLASS, HDAT.HR_NAME, SM.STAFFNAME ");
            	stb.append(" FROM ");
            	stb.append("   SCHREG_REGD_HDAT HDAT ");
            	stb.append("   LEFT JOIN STAFF_MST SM ");
            	stb.append("   ON SM.STAFFCD = HDAT.TR_CD1 ");
            	stb.append(" WHERE ");
            	stb.append("   HDAT.YEAR = '" + _year + "' ");
            	stb.append("   AND HDAT.SEMESTER = '" + _semester + "' ");
            	stb.append("   AND HDAT.GRADE || HDAT.HR_CLASS IN " + SQLUtils.whereIn(true, _grhr_ClassSelected) + " ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                	rtnMap.put(rs.getString("HR_CLASS"), new HrClassInfo(rs.getString("HR_CLASS"), rs.getString("HR_NAME"), rs.getString("STAFFNAME")));
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtnMap;
        }

        private Map getbehaviorSubMst(final DB2UDB db2) {
            Map rtnMap = new LinkedMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT STUDYREC_CODE, STUDYREC_CODENAME FROM BEHAVIOR_SUB_MST WHERE SCHOOL_KIND = '" + _schoolkind + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	rtnMap.put(rs.getString("STUDYREC_CODE"), rs.getString("STUDYREC_CODENAME"));
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtnMap;
        }

        /**
         * 年度の開始日を取得する
         */
        private void loadSemester(final DB2UDB db2, String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List list = new ArrayList();
            try {
                ps = db2.prepareStatement(sqlSemester(year));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semesterCd = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    final String sDate = rs.getString("SDATE");
                    final String eDate = rs.getString("EDATE");
                    final Semester semester = new Semester(semesterCd, name, sDate, eDate);
                    _semesterMap.put(semesterCd, semester);

                    list.add(sDate);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            if (!list.isEmpty()) {
                _sDate = (String) list.get(0);
            }
            log.debug("年度の開始日=" + _sDate);
            return;
        }

        private String sqlSemester(String year) {
            final String sql;
            sql = "select"
                + "   * "
                + " from"
                + "   SEMESTER_MST"
                + " where"
                + "   YEAR= '" + year + "'"
                + " order by SEMESTER"
            ;
            return sql;
        }

        private String getSchregRegdGdat(final DB2UDB db2, final String field) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT " + field + " FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (SQLException ex) {
                log.error("SCHREG_REGD_GDAT exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

    }
}

// eof
