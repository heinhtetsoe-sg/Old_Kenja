/*
 * $Id: 3ee51e61f61f531fce22c24c878b033d68159170 $
 *
 * 作成日: 2015/08/27
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;


/**
 *
 *    学校教育システム 賢者 [時間割管理]
 *
 *                    ＜ＫＮＪＢ１３１＞  授業内容一覧
 *
 */
public class KNJB131 {

    private static final Log log = LogFactory.getLog(KNJB131.class);

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
    
    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
		final List list = Chair.getScheduleList(db2, _param);
		final int scheduleMax = 35;
		
		for (int chi = 0; chi < list.size(); chi++) {
			final Chair chair = (Chair) list.get(chi);
			
			//chair.setExecuteDateMap();
            final List staffList = getChairStaffList(db2, chair._chaircd);
			
			final List pageList = getPageList(chair._scheduleList, scheduleMax);
			if (pageList.size() == 0) {
			    pageList.add(new ArrayList());
			}
			
			for (int pi = 0; pi < pageList.size(); pi++) {
			    final List scheduleList = (List) pageList.get(pi);

                final String form = "KNJB131.frm";
                svf.VrSetForm(form, 1);
                   
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate)); // 日付
                svf.VrsOut("TITLE", "授業内容一覧"); // タイトル
                svf.VrsOut("SUBCLASS_CD", chair._subclasscd); // 科目コード
                svf.VrsOut("SUBCLASS_NAME", chair._subclassname); // 科目名称
                svf.VrsOut("CHAIR_CD", chair._chaircd); // 講座コード
                svf.VrsOut("CHAIR_NAME", chair._chairname); // 講座名称
                svf.VrsOut("CREDIT", chair._maxCredits); // 単位数
                svf.VrsOut("TIME", chair.getJisshiJikanSuu()); // 実施時間数
                
                for (int stfi = 0; stfi < staffList.size(); stfi++) {
                    final String i = String.valueOf(stfi + 1);
                    final String staffname = (String) staffList.get(stfi);
                    final int len = getMS932ByteLength(staffname);
                    svf.VrsOut("TEACHER_NAME" + i + "_" + (len > 30 ? "3" : len > 20 ? "2" : "1"), staffname); // 教科担当
                }
                
                for (int j = 0; j < scheduleList.size(); j++) {
                    final Schedule sch = (Schedule) scheduleList.get(j);
                    final int line = j + 1;
                    svf.VrsOutn("EXE_DATE", line, sch.dateString()); // 実施日付
                    svf.VrsOutn("EXE_PERIOD", line, sch._periodName); // 実施校時
                    svf.VrsOutn("EXE_DIV", line, sch._execuedivName); // 実施区分
                    svf.VrsOutn("UNIT_NO", line, null); // ユニット番号
                    svf.VrsOutn("EXE_TIME", line, "1".equals(sch._executediv) ? "1" : ""); // 実施時間数
                    
                    final String[] token = KNJ_EditEdit.get_token(sch._remark, 80, 2);
                    if (null != token) {
                        for (int i = 0; i < token.length; i++) {
                            svf.VrsOutn("STUDY_CONTENT" + String.valueOf(i + 1), line, token[i]); // 授業内容
                        }
                    }
                }
                
                svf.VrEndPage();
                _hasData = true;
			}
		}
    }
    
    private List getChairStaffList(final DB2UDB db2, final String chaircd) {
        final List staffList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "";
            sql += " SELECT DISTINCT ";
            sql += "   T1.CHARGEDIV, ";
            sql += "   T1.STAFFCD, ";
            sql += "   T2.STAFFNAME ";
            sql += " FROM ";
            sql += "   CHAIR_STF_DAT T1 ";
            sql += " INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ";
            sql += " INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR ";
            sql += "   AND T3.SEMESTER = T1.SEMESTER ";
            sql += "   AND T3.CHAIRCD = T1.CHAIRCD ";
            sql += " WHERE ";
            sql += "   T1.YEAR = '" + _param._year + "' ";
            sql += "   AND T1.CHAIRCD = '" + chaircd + "' ";
            sql += "   AND ";
            if ("1".equals(_param._useCurriculumcd)) {
                sql += "   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ";
            }
            sql += "       T3.SUBCLASSCD = '" + _param._subclasscd + "' ";
            sql += " ORDER BY ";
            sql += "   T1.CHARGEDIV DESC, "; // '1'が正担任
            sql += "   T1.STAFFCD ";

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                staffList.add(rs.getString("STAFFNAME"));
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return staffList;
    }

    private static List getPageList(final List list, final int max) {
        final List pageList = new ArrayList();
        List current = null;
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
        }
        return pageList;
    }
    
    private static class Chair {
        final String _chaircd;
        final String _subclasscd;
        final String _subclassname;
        final String _chairname;
        final String _minCredits;
        final String _maxCredits;
        final List _scheduleList;
//        final Map _executeDateMap; // 日付ごとの時間割のリストのマップ

        Chair(
            final String chaircd,
            final String subclasscd,
            final String subclassname,
            final String chairname,
            final String minCredits,
            final String maxCredits
        ) {
            _chaircd = chaircd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _chairname = chairname;
            _minCredits = minCredits;
            _maxCredits = maxCredits;
            _scheduleList = new ArrayList();
//            _executeDateMap = new HashMap();
        }
        
//        public void setExecuteDateMap() {
//            _executeDateMap.clear();
//            for (final Iterator it = _scheduleList.iterator(); it.hasNext();) {
//                final Schedule sch = (Schedule) it.next();
//                if (null == _executeDateMap.get(sch._executedate)) {
//                    _executeDateMap.put(sch._executedate, new ArrayList());
//                }
//                ((List) _executeDateMap.get(sch._executedate)).add(sch);
//            }
//        }
//        
//        /**
//         * 指定日付の時間割の数
//         * @param date 日付
//         * @return
//         */
//        public String getJisshisuu(final String date) {
//            if (null == _executeDateMap.get(date)) {
//                return null;
//            }
//            return String.valueOf(((List) _executeDateMap.get(date)).size());
//        }

        /**
         * 実施時間数(SCH_CHR_DAT.EXECUTEDIV = '1'のカウント)
         * @return
         */
        public String getJisshiJikanSuu() {
            final List executediv1List = new ArrayList();
            for (final Iterator it = _scheduleList.iterator(); it.hasNext();) {
                final Schedule sch = (Schedule) it.next();
                if ("1".equals(sch._executediv)) {
                    executediv1List.add(sch);
                }
            }
            return String.valueOf(executediv1List.size());
        }

        private static List getScheduleList(final DB2UDB db2, final Param param) {
            final Map chairMap = new HashMap();
            final List chairList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    //log.debug(" map = " + getResultSetMap(rs));
                    final String chaircd = rs.getString("CHAIRCD");
                    if (null == chairMap.get(chaircd)) {
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String chairname = rs.getString("CHAIRNAME");
                        final String minCredits = rs.getString("MIN_CREDITS");
                        final String maxCredits = rs.getString("MAX_CREDITS");
                        final Chair chair = new Chair(chaircd, subclasscd, subclassname, chairname, minCredits, maxCredits);
                        chairList.add(chair);
                        chairMap.put(chaircd, chair);
                    }
                    final Chair chair = (Chair) chairMap.get(chaircd);

                    final String executedate = rs.getString("EXECUTEDATE");
                    final String periodcd = rs.getString("PERIODCD");
                    final String periodName = rs.getString("PERIOD_NAME");
                    final String executediv = rs.getString("EXECUTEDIV");
                    final String execuedivName = rs.getString("EXECUEDIV_NAME");
                    final String remark = rs.getString("REMARK");
                    final Schedule schedule = new Schedule(executedate, periodcd, periodName, executediv, execuedivName, remark);
                    chair._scheduleList.add(schedule);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return chairList;
        }
        
        /**
         * ResultSetのデータをマップにする
         * @param rs 元のResultSetデータ
         * @return コピーされたデータのマップ
         */
        private static Map getResultSetMap(final ResultSet rs) {
            final Map map = new TreeMap();
            if (null != rs) {
                try {
                    final ResultSetMetaData meta = rs.getMetaData();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        map.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
                    }
                } catch (SQLException e) {
                    log.error("exception!", e);
                }
            }
            return map;
        }
        
        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHREG_CHAIR_CREDITS AS ( ");
            stb.append("     SELECT ");
            stb.append("       T1.YEAR, ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T1.CHAIRCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append("       T3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("       T4.CREDITS ");
            stb.append("     FROM CHAIR_STD_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("     INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T3.CHAIRCD = T1.CHAIRCD ");
            stb.append("     INNER JOIN CREDIT_MST T4 ON T4.YEAR = T1.YEAR ");
            stb.append("         AND T4.COURSECD = T2.COURSECD ");
            stb.append("         AND T4.MAJORCD = T2.MAJORCD ");
            stb.append("         AND T4.COURSECODE = T2.COURSECODE ");
            stb.append("         AND T4.CLASSCD = T3.CLASSCD ");
            stb.append("         AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("         AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("         AND T4.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + param._year + "' ");
            stb.append("         AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append(" ), SCHREG_SUBCLASS_CREDITS0 AS ( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("       T1.YEAR, ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T1.SUBCLASSCD, ");
            stb.append("       T1.CREDITS ");
            stb.append("     FROM SCHREG_CHAIR_CREDITS T1 ");
            stb.append(" ), SCHREG_SUBCLASS_CREDITS AS ( ");
            stb.append("     SELECT  ");
            stb.append("       T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("       T2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("       SUM(T1.CREDITS) AS CREDITS ");
            stb.append("     FROM SCHREG_SUBCLASS_CREDITS0 T1 ");
            stb.append("     INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("         T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T2.CALCULATE_CREDIT_FLG = '2' ");
            stb.append("     GROUP BY ");
            stb.append("       T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("       T2.COMBINED_SUBCLASSCD ");
            stb.append("   UNION ALL ");
            stb.append("     SELECT  ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T1.SUBCLASSCD, ");
            stb.append("       T1.CREDITS ");
            stb.append("     FROM SCHREG_SUBCLASS_CREDITS0 T1 ");
            stb.append("     LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("         T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T2.CALCULATE_CREDIT_FLG = '2' ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR IS NULL ");
            stb.append(" ), CHAIR_CREDITS AS ( ");
            stb.append("     SELECT ");
            stb.append("       T1.CHAIRCD, ");
            stb.append("       MIN(T2.CREDITS) AS MIN_CREDITS, ");
            stb.append("       MAX(T2.CREDITS) AS MAX_CREDITS ");
            stb.append("     FROM SCHREG_CHAIR_CREDITS T1 ");
            stb.append("     LEFT JOIN SCHREG_SUBCLASS_CREDITS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     GROUP BY ");
            stb.append("       T1.CHAIRCD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T3.CHAIRCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T4.SUBCLASSNAME, ");
            stb.append("     T3.CHAIRNAME, ");
            stb.append("     T5.MIN_CREDITS, ");
            stb.append("     T5.MAX_CREDITS, ");
            stb.append("     T1.EXECUTEDATE, ");
            stb.append("     T1.PERIODCD, ");
            stb.append("     NMB001.ABBV1 AS PERIOD_NAME, ");
            stb.append("     T1.EXECUTEDIV, ");
            stb.append("     NMC009.NAME1 AS EXECUEDIV_NAME, ");
            stb.append("     T6.REMARK ");
            stb.append(" FROM CHAIR_DAT T3 ");
            stb.append(" INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T3.YEAR ");
            stb.append("     AND T2.SEMESTER = T3.SEMESTER ");
            stb.append(" INNER JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T3.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T4.CLASSCD = T3.CLASSCD ");
                stb.append("     AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("     AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ");
            }
            stb.append(" LEFT JOIN SCH_CHR_DAT T1 ON T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ");
            stb.append("     AND T1.CHAIRCD = T3.CHAIRCD  ");
            stb.append("     AND T1.EXECUTEDATE BETWEEN '" + param._date1 + "' AND '" + param._date2 + "' ");
            stb.append(" LEFT JOIN CHAIR_CREDITS T5 ON T5.CHAIRCD = T1.CHAIRCD ");
            stb.append(" LEFT JOIN SCH_CHR_REMARK_DAT T6 ON T6.EXECUTEDATE = T1.EXECUTEDATE ");
            stb.append("     AND T6.PERIODCD = T1.PERIODCD ");
            stb.append("     AND T6.CHAIRCD = T1.CHAIRCD ");
            stb.append("     AND T6.REMARK_DIV = '01' ");
            stb.append(" LEFT JOIN NAME_MST NMB001 ON NMB001.NAMECD1 = 'B001' AND NMB001.NAMECD2 = T1.PERIODCD ");
            stb.append(" LEFT JOIN NAME_MST NMC009 ON NMC009.NAMECD1 = 'C009' AND NMC009.NAMECD2 = T1.EXECUTEDIV ");
            stb.append(" WHERE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T3.SUBCLASSCD = '" + param._subclasscd + "' ");
            stb.append("     AND T3.CHAIRCD IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(" ORDER BY ");
            stb.append("     T3.CHAIRCD, ");
            stb.append("     T1.EXECUTEDATE, ");
            stb.append("     T1.PERIODCD ");
            return stb.toString();
        }
        
    }
    
    private static class Schedule {
        final String _executedate;
        final String _periodcd;
        final String _periodName;
        final String _executediv;
        final String _execuedivName;
        final String _remark;

        Schedule(
            final String executedate,
            final String periodcd,
            final String periodName,
            final String executediv,
            final String execuedivName,
            final String remark
        ) {
            _executedate = executedate;
            _periodcd = periodcd;
            _periodName = periodName;
            _executediv = executediv;
            _execuedivName = execuedivName;
            _remark = remark;
        }

        public String dateString() {
            if (null == _executedate) {
                return null;
            }
            final String[] split = StringUtils.split(_executedate, "-");
            return String.valueOf(Integer.parseInt(split[1])) + "月" + String.valueOf(Integer.parseInt(split[2])) + "日" + "(" + KNJ_EditDate.h_format_W(_executedate) + ")";
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
    private class Param {
        final String _year;
        final String _semester;
        final String[] _categorySelected;
        final String _classcd;
        final String _ctrlDate;
        final String _date1;
        final String _date2;
        final String _subclasscd;
        final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _classcd = request.getParameter("CLASSCD");
            _subclasscd = request.getParameter("SUBCLASSCD");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlDate = null == request.getParameter("CTRL_DATE") ? request.getParameter("CTRL_DATE") : request.getParameter("CTRL_DATE").replace('/', '-');
            _date1 = null == request.getParameter("DATE1") ? request.getParameter("DATE1") : request.getParameter("DATE1").replace('/', '-');
            _date2 = null == request.getParameter("DATE2") ? request.getParameter("DATE2") : request.getParameter("DATE2").replace('/', '-');
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }
    }
}

// eof

