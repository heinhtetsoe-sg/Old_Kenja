// kanji=漢字
/*
 * $Id: 21dbcb8695ea82376e038e6a648e56584fbb213d $
 *
 * 作成日: 2011/06/03 10:04:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 21dbcb8695ea82376e038e6a648e56584fbb213d $
 */
public class KNJE155 {

    private static final Log log = LogFactory.getLog("KNJE155.class");

    private boolean _hasData;
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TAISYOUGAI_KYOUKA = "90";

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            createDefineCode(db2);

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

    private KNJDefineSchool createDefineCode(final DB2UDB db2) {
        final KNJDefineSchool definecode = new KNJDefineSchool();

        // 各学校における定数等設定
        definecode.defineCode(db2, _param._year);
        log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);

        return definecode;
    }

    /**
     * collを最大数ごとにグループ化したリストを得る
     * @param coll
     * @param max 最大数
     * @return collを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final Collection coll, final int max) {
        final List pageList = new ArrayList();
        List current = null;
        for (final Iterator it = coll.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
        }
        return pageList;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        for (int i = 0; i < _param._hrclasses.length; i++) {
            final HrClass hrClass = new HrClass(_param._hrclasses[i]);
            hrClass.load(db2);
            final List pageList = getPageList(hrClass._students, _param._formMaxLine);
            // 1ページ単位の生徒分
            for (final Iterator iter = pageList.iterator(); iter.hasNext();) {
                final List printList = (List) iter.next();
                setHead(db2, svf, hrClass);
                // 科目
                String bksubclasscd = "";
                int bkgrpcnt = 0;
                for (final Iterator itSub = hrClass._subclassMap.keySet().iterator(); itSub.hasNext();) {
                    final String subclassCd = (String) itSub.next();
                    final SubclassData subclassData = (SubclassData) hrClass._subclassMap.get(subclassCd);
                    final char[] className = StringUtils.defaultString(subclassData._subclassName).toCharArray();
                    int subNmCnt = 0;
                    // 観点
                    for (final Iterator itView = subclassData._viewList.iterator(); itView.hasNext();) {
                        final JviewData jviewData = (JviewData) itView.next();
                    	int classabbvlen = subclassData._subclassName.length();
                        if (subNmCnt < classabbvlen) {
                            if (subclassData._viewList.size() < classabbvlen) {
                            //上記判定で、ここに来るパターンは少なくとも1列に2文字以上入れる場合のみ
                            //1列に3文字入れるかを判定(1列2文字以外は3文字フィールドに格納)
                                int cafieldid = classabbvlen == 2 ? 2 : 3;
                                if (cafieldid == 2) {
                            	    final String outstr;
                            	    if (subNmCnt == 0) {//1列2文字の場合は最初だけ出力
                            	        if (subNmCnt*2+2 <= classabbvlen) {
                            		        outstr = String.valueOf(className[subNmCnt*2]) + String.valueOf(className[subNmCnt*2+1]);
                            	        } else {
                            		        outstr = String.valueOf(className[subNmCnt*2]);
                            	        }
                                        svf.VrsOut("course1_" + cafieldid, outstr);
                            	    }
                                } else if (cafieldid == 3) {
                            	    final String outstr;
                            	    if (subNmCnt*3 < classabbvlen) {
                            	        if (subNmCnt*3+3 <= classabbvlen) {
                            		        outstr = String.valueOf(className[subNmCnt*3]) + String.valueOf(className[subNmCnt*3+1]) + String.valueOf(className[subNmCnt*3+2]);
                            	        } else if (subNmCnt*3+2 <= classabbvlen) {
                            		        outstr = String.valueOf(className[subNmCnt*3]) + String.valueOf(className[subNmCnt*3+1]);
                            	        } else {
                            		        outstr = String.valueOf(className[subNmCnt*3]);
                            	        }
                                        svf.VrsOut("course1_" + cafieldid, outstr);
                                	}
                              	}
                            } else {
                                svf.VrsOut("course1", String.valueOf(className[subNmCnt]));
                            }
                        }
                        svf.VrsOut("JVIEW1", jviewData._viewName);
                        if (!bksubclasscd.equals(subclassData._subclassCd)) {
                        	bkgrpcnt++;
                        }
                        svf.VrsOut("COURSE_DIV", String.valueOf(bkgrpcnt));
                        // 生徒
                        int fieldCnt = 1;
                        for (final Iterator itPrint = printList.iterator(); itPrint.hasNext();) {
                            final Student student = (Student) itPrint.next();

                            svf.VrsOutn("NUMBER", fieldCnt, student._attendNo);
                            svf.VrsOut("name" + fieldCnt, student._name);
                            svf.VrsOut("REMARK" + fieldCnt, student.getTransInfo(db2));
                            svf.VrsOut("SCORE" + fieldCnt, student.getSetVal(subclassCd, jviewData._viewCd, subclassData._mojiHyouka));
                            fieldCnt++;
                        }
                        svf.VrEndRecord();
                        subNmCnt++;
                        _hasData = true;
                        bksubclasscd = subclassData._subclassCd;
                    }
                }
                svf.VrEndPage();
            }
        }
    }

    private void setHead(final DB2UDB db2, final Vrw32alp svf, final HrClass hrClass) {
        svf.VrSetForm(_param._frmName, 4);
        svf.VrsOut("year2", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度");
        svf.VrsOut("TITLE", "観点別学習状況一覧表");
        svf.VrsOut("teacher", hrClass._staffName);
        svf.VrsOut("HR_NAME", hrClass._hrName);
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class HrClass {
        private final String _code;
        private String _grade;
        private String _hrClass;
        private String _hrName;
        private String _hrNameAbbv;
        private String _staffName;
        private final Map _subclassMap = new LinkedMap();
        private final List _students = new LinkedList();

        public HrClass(final String code) {
            _code = code;
        }

        public void load(final DB2UDB db2) throws SQLException {
            loadInfo(db2);
            loadStudent(db2);
            loadJview(db2);
        }

        private void loadInfo(final DB2UDB db2) throws SQLException {
            final String infoSql = getHrInfo();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(infoSql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _grade = rs.getString("GRADE");
                    _hrClass = rs.getString("HR_CLASS");
                    _hrName = rs.getString("HR_NAME");
                    _hrNameAbbv = rs.getString("HR_NAMEABBV");
                    _staffName = rs.getString("STAFFNAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getHrInfo() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     HR_NAME, ");
            stb.append("     HR_NAMEABBV, ");
            stb.append("     STAFFNAME, ");
            stb.append("     CLASSWEEKS, ");
            stb.append("     CLASSDAYS ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.TR_CD1 ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' ");
            stb.append("     AND GRADE || HR_CLASS = '" + _code + "' ");
            stb.append("     AND SEMESTER = '" + _param.getSeme() + "' ");
            return stb.toString();
        }

        private void loadStudent(final DB2UDB db2) throws SQLException {
            final String infoSql = getStudentInfo();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(infoSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregNo = rs.getString("SCHREGNO");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String name = rs.getString("NAME");
                    final String entDate = rs.getString("ENT_DATE");
                    final String entDiv = rs.getString("ENT_DIV");
                    final String entReason = rs.getString("ENT_REASON");
                    final String grdDate = rs.getString("GRD_DATE");
                    final String grdDiv = rs.getString("GRD_DIV");
                    final String grdReason = rs.getString("GRD_REASON");
                    final String course = rs.getString("COURSE");
                    final String coursename = rs.getString("COURSENAME");
                    final String majorname = rs.getString("MAJORNAME");
                    final Student student = new Student(schregNo, attendNo, name, entDate, entDiv, entReason, grdDate, grdDiv, grdReason, course, coursename, majorname);
                    _students.add(student);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                student.setJview(db2, student._schregNo);
            }
            loadTransfer(db2);
        }

        private String getStudentInfo() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     EG.ENT_DATE, ");
            stb.append("     EG.ENT_DIV, ");
            stb.append("     EG.ENT_REASON, ");
            stb.append("     EG.GRD_DATE, ");
            stb.append("     EG.GRD_DIV, ");
            stb.append("     EG.GRD_REASON, ");
            stb.append("     REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE, ");
            stb.append("     CM.COURSENAME, ");
            stb.append("     MM.MAJORNAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EG ON EG.SCHREGNO = REGD.SCHREGNO ");
            //画面で指定している物なので、useprgチェック不要。
            stb.append("         AND EG.SCHOOL_KIND = '" + _param._schoolKind +"' ");
            stb.append("     LEFT JOIN COURSE_MST CM ON CM.COURSECD = REGD.COURSECD ");
            stb.append("     LEFT JOIN MAJOR_MST MM ON MM.COURSECD = REGD.COURSECD AND MM.MAJORCD = REGD.MAJORCD ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + _param.getSeme() + "' ");
            stb.append("     AND REGD.GRADE || REGD.HR_CLASS = '" + _code + "' ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.ATTENDNO ");
            return stb.toString();

        }

        private void loadTransfer(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final String sql = sqlTrans();

            try {
                ps = db2.prepareStatement(sql);
                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregNo);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String code = rs.getString("transfercd");
                        final String sDate = rs.getString("transfer_sdate");
                        if (null != code) {
                            student._transfercd = code;
                            student._transferSDate = sDate;
                        }
                    }
                }
            } catch (final SQLException e) {
                log.error("異動情報の取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlTrans() {
            final String sql;
            sql = "SELECT t1.transfercd, t1.transfer_sdate"
                + " FROM schreg_transfer_dat t1 "
                + " inner join semester_mst t2 on t2.year = '" + _param._year + "' AND t2.semester = '" + _param._semester + "'"
                + " AND t2.edate BETWEEN t1.transfer_sdate AND t1.transfer_edate"
                + " WHERE t1.schregno= ? "
                + " ORDER BY t1.transfercd, t1.transfer_sdate, t1.transfer_edate "
                ;
            return sql;
        }

        private void loadJview(final DB2UDB db2) throws SQLException {
            final String jviewSql = getJviewSql();
            log.debug("ldjview sql = " + jviewSql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(jviewSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String subclassAbbv = rs.getString("SUBCLASSABBV");
                    final String classCd = rs.getString("CLASSCD");
                    final String className = rs.getString("CLASSNAME");
                    final String classAbbv = rs.getString("CLASSABBV");
                    final String viewCd = rs.getString("VIEWCD");
                    final String viewName = rs.getString("VIEWNAME");
                    final String viewAbbv = rs.getString("VIEWABBV");
                    final boolean mojiHyouka = "1".equals(rs.getString("ELECTDIV")) ? true : false;
                    SubclassData subclassData = null;
                    if (_subclassMap.containsKey(subclassCd)) {
                        subclassData = (SubclassData) _subclassMap.get(subclassCd);
                    } else {
                        subclassData = new SubclassData(subclassCd, StringUtils.defaultString(subclassName, ""),
                        		                         StringUtils.defaultString(subclassAbbv, ""), classCd,
                        		                         StringUtils.defaultString(className, ""), StringUtils.defaultString(classAbbv, ""),
                        		                         mojiHyouka);
                    }
                    subclassData.setJview(viewCd, viewName, viewAbbv, "");
                    _subclassMap.put(subclassCd, subclassData);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getJviewSql() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     L2.SUBCLASSNAME, ");
            stb.append("     L2.SUBCLASSABBV, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     L3.CLASSNAME, ");
            stb.append("     L3.CLASSABBV, ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     T2.VIEWNAME, ");
            stb.append("     T2.VIEWABBV, ");
            stb.append("     L3.ELECTDIV ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_SUB_YDAT T1 ");
            stb.append("     LEFT JOIN JVIEWNAME_SUB_MST T2 ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       ON T1.CLASSCD = T2.CLASSCD ");
                stb.append("      AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("      AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("      AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("      AND T1.VIEWCD = T2.VIEWCD ");
            stb.append("     LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("          AND L2.CLASSCD = T1.CLASSCD ");
                stb.append("          AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("          AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN CLASS_MST L3 ON L3.CLASSCD = T1.CLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("          AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            //画面で指定している物なので、useprgチェック不要。
            stb.append("     AND T1.SCHOOL_KIND   = '" + _param._schoolKind + "' ");
            stb.append("     AND EXISTS (SELECT 'X' ");
            stb.append("                 FROM SCHREG_REGD_GDAT T4 ");
            stb.append("                 WHERE ");
            stb.append("      T1.YEAR = T4.YEAR AND ");
            stb.append("      T2.SCHOOL_KIND = T4.SCHOOL_KIND AND ");
            stb.append("      T4.GRADE = '" + _param._grade + "' ");
            stb.append("     ) ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(L3.ELECTDIV, '0'), ");
            stb.append("     L3.SHOWORDER4, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.VIEWCD ");

            return stb.toString();
        }
    }

    private class Student {
        final String _schregNo;
        final String _attendNo;
        final String _name;
        final String _entDate;
        final String _entDiv;
        final String _entReason;
        final String _grdDate;
        final String _grdDiv;
        final String _grdReason;
        final String _course;
        final String _coursename;
        final String _majorname;
        private String _transferSDate;
        private String _transfercd;
        private final Map _subclassMap = new HashMap();

        public Student(
                final String schregNo,
                final String attendNo,
                final String name,
                final String entDate,
                final String entDiv,
                final String entReason,
                final String grdDate,
                final String grdDiv,
                final String grdReason,
                final String course,
                final String coursename,
                final String majorname
        ) {
            _schregNo = schregNo;
            _attendNo = attendNo;
            _name = name;
            _entDate = entDate;
            _entDiv = entDiv;
            _entReason = entReason;
            _grdDate = grdDate;
            _grdDiv = grdDiv;
            _grdReason = grdReason;
            _course = course;
            _coursename = coursename;
            _majorname = majorname;
        }

        private void setJview(final DB2UDB db2, final String schregno) throws SQLException {
            final String sql = getJviewSql(schregno);
            log.debug("jview sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String viewCd = rs.getString("VIEWCD");
                    final String status = rs.getString("STATUS");
                    SubclassData subclassData = null;
                    if (_subclassMap.containsKey(subclassCd)) {
                        subclassData = (SubclassData) _subclassMap.get(subclassCd);
                    } else {
                        subclassData = new SubclassData(subclassCd, "", "", "", "", "", false);
                    }
                    subclassData.setJview(viewCd, "", "", status);
                    _subclassMap.put(subclassCd, subclassData);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getJviewSql(final String schregno) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     T1.VIEWCD, ");
            stb.append("     T2.STATUS ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_SUB_YDAT T1 ");
            stb.append("     LEFT JOIN JVIEWSTAT_SUB_DAT T2 ");
            stb.append("      ON T1.VIEWCD = T2.VIEWCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("      AND T1.CLASSCD = T2.CLASSCD ");
                stb.append("      AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("      AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("      AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("     AND T2.SEMESTER = '9' ");
            stb.append("     AND T2.SCHREGNO = '" + schregno + "' ");

            return stb.toString();
        }

        private String getSetVal(final String subclassCd, final String viewCd, final boolean mojiHyouka) {
            final SubclassData subclassData = (SubclassData) _subclassMap.get(subclassCd);
            if (null != subclassData) {
                for (final Iterator iter = subclassData._viewList.iterator(); iter.hasNext();) {
                    final JviewData jviewData = (JviewData) iter.next();
                    if (viewCd.equals(jviewData._viewCd)) {
                      	String outstatstr = jviewData._status;
                        if (outstatstr != null && _param._useNotHyouji != null) {
                            if (outstatstr.equals(_param._useNotHyouji)) outstatstr = "";
                        }
                        return outstatstr;
                    }
                }
            }

            return "";
        }

        private String getTransInfo(final DB2UDB db2) {
            final String rtn;
            if (enableTrs()) {
                final Map map = (Map) _param._meisyouMap.get("A004");
                rtn = KNJ_EditDate.h_format_JP(db2, _transferSDate) + map.get(_transfercd);
            } else if (enableGrd()) {
                final Map map = (Map) _param._meisyouMap.get("A003");
                final String hoge = _grdDate.toString();
                rtn = KNJ_EditDate.h_format_JP(db2, hoge) + map.get(_grdDiv);
            } else if (enableEnt()) {
                final Map map = (Map) _param._meisyouMap.get("A002");
                final String hoge = _entDate.toString();
                rtn = KNJ_EditDate.h_format_JP(db2, hoge) + map.get(_entDiv);
            } else {
                return null;
            }

            log.debug(_schregNo + " 入学:" + _entDate + "(区分" + _entDiv + ") / 卒業:" + _grdDate + "(区分" + _grdDiv + ") / 異動:" + _transferSDate + "(" + _transfercd + ")");
            return rtn;
        }

        /**
         * 入学データは有効か?
         * @return 有効ならtrue
         */
        private boolean enableEnt() {
            if (null == _entDate) {
                return false;
            }
            if (!"4".equals(_entDiv) && !"5".equals(_entDiv)) {
                return false;
            }

            final Semester semes = (Semester) _param._semesterMap.get(_param._semester);
            if (_entDate.compareTo(semes._sDate) < 0) { // _entDate < aaa
                return false;
            }

            return true;
        }

        /**
         * 卒業データは有効か?
         * @return 有効ならtrue
         */
        private boolean enableGrd() {
            if (null == _grdDate) {
                return false;
            }
            if (!"2".equals(_grdDiv) && !"3".equals(_grdDiv)) {
                return false;
            }

            final Semester semes = (Semester) _param._semesterMap.get(_param._semester);
            if (_grdDate.compareTo(semes._eDate) > 0) { // _grdDate > aaa
                return false;
            }

            return true;
        }

        /**
         * 異動データは有効か?
         * @return 有効ならtrue
         */
        private boolean enableTrs() {
            if (null == _transferSDate) {
                return false;
            }
            if (!"1".equals(_transfercd) && !"2".equals(_transfercd)) {
                return false;
            }
            return true;
        }
    }

    private class SubclassData {
        final String _subclassCd;
        final String _subclassName;
        final String _subclassAbbv;
        final String _classCd;
        final String _className;
        final String _classAbbv;
        final boolean _mojiHyouka;
        final List _viewList = new ArrayList();

        SubclassData (
                final String subclassCd,
                final String subclassName,
                final String subclassAbbv,
                final String classCd,
                final String className,
                final String classAbbv,
                final boolean mojiHyouka
        ) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _subclassAbbv = subclassAbbv;
            _classCd = classCd;
            _className = className;
            _classAbbv = classAbbv;
            _mojiHyouka = mojiHyouka;
        }

        private void setJview(final String viewCd, final String viewName, final String viewAbbv, final String status) {
            _viewList.add(new JviewData(viewCd, viewName, viewAbbv, status));
        }

        public String toString() {
        	return "SubclassData(" + _subclassCd + ", viewList = " + _viewList + ")";
        }
    }

    private class JviewData {
        private final String _viewCd;
        private final String _viewName;
        private final String _viewAbbv;
        private final String _status;

        JviewData (
                final String viewCd,
                final String viewName,
                final String viewAbbv,
                final String status
        ) {
            _viewCd = viewCd;
            _viewName = viewName;
            _viewAbbv = viewAbbv;
            _status = status;
        }
        public String toString() {
        	return "View(" + _viewCd + ", " + _status + ")";
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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 65505 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _grade;
        final String[] _hrclasses;
        final int _formMaxLine;
        final String _ctrlSemester;
        final String _frmName;
        private Map _semesterMap = new HashMap();
        private Map _meisyouMap = new HashMap();
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _schoolKind;

        /** 出欠状況取得引数 */
        private Map _attendParamMap = new HashMap();
        private String _sDate;
        private Map _jobnameMap;
        private String _useNotHyouji;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");

            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _hrclasses = request.getParameterValues("CLASS_SELECTED");
            _formMaxLine = 45 ;

            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            _schoolKind = getSchregRegdGdat(db2, "SCHOOL_KIND");

            final String usenothyoujiid = request.getParameter("USENOTHYOUJI");
            if (usenothyoujiid != null && !"".equals(usenothyoujiid)) {
            	if (!"9".equals(_semester)) {
            		_useNotHyouji = setD029Name1(db2, usenothyoujiid);
            	} else {
            		_useNotHyouji = setD028Name1(db2, usenothyoujiid);
            	}
            }

            _frmName = "KNJE155.frm";

            loadSemester(db2, _year);
            setMeisyouMap(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);

            _jobnameMap = getPrgStampDat(db2);
            log.info(" jobnameMap = " + _jobnameMap);
        }

        public void setMeisyouMap(final DB2UDB db2) {
            final String[] namecd1 = {
                    "A002",
                    "A003",
                    "A004",
                    "C001",
            };
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                for (int i = 0; i < namecd1.length; i++) {
                    final Map map = new HashMap();

                    final String sql = "SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + namecd1[i] + "'";
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String namecd2 = rs.getString("namecd2");
                        final String name1 = rs.getString("name1");
                        map.put(namecd2, name1);
                    }

                    _meisyouMap.put(namecd1[i], map);
                }
            } catch (final SQLException e) {
                log.error("名称マスタの読込みでエラー", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
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

        private String setD028Name1(DB2UDB db2, final String namecd2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = 'D028' AND NAMECD2 = '" + namecd2 + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("ABBV1");
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        private String setD029Name1(DB2UDB db2, final String namecd2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = 'D029' AND NAMECD2 = '" + namecd2 + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("ABBV1");
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        private Map getPrgStampDat(final DB2UDB db2) throws SQLException {

            final Map seqTitleMap = new HashMap();

            if (setTableColumnCheck(db2, "PRG_STAMP_DAT", null)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT  ");
                stb.append("    T1.SEQ ");
                stb.append("  , T1.TITLE ");
                stb.append(" FROM PRG_STAMP_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + _year + "' ");
                stb.append("   AND T1.SEMESTER = '9' ");
                stb.append("   AND T1.PROGRAMID = 'KNJE155' ");

                seqTitleMap.putAll(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "SEQ", "TITLE"));
            }

            return seqTitleMap;
        }

        private static boolean setTableColumnCheck(final DB2UDB db2, final String tabname, final String colname) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT 1 FROM ");
            if (StringUtils.isBlank(colname)) {
                stb.append("SYSCAT.TABLES");
            } else {
                stb.append("SYSCAT.COLUMNS");
            }
            stb.append(" WHERE TABNAME = '" + tabname + "' ");
            if (!StringUtils.isBlank(colname)) {
                stb.append(" AND COLNAME = '" + colname + "' ");
            }
            final String sql = stb.toString();
            boolean hasTableColumn = KnjDbUtils.query(db2, sql).size() > 0;
            log.fatal(" hasTableColumn " + tabname + (null == colname ? "" :  "." + colname) + " = " + hasTableColumn);
            return hasTableColumn;
        }

        /**
         * 年度の開始日を取得する
         */
        private String loadSemester(final DB2UDB db2, String year) {
        	String retstr = "";
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
                    if (!"9".equals(semesterCd)) {
                    	retstr = semesterCd;
                    }

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
            return retstr;
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

        public String getSeme() {
            return "9".equals(_semester) ? _ctrlSemester : _semester;
        }

    }
}

// eof
