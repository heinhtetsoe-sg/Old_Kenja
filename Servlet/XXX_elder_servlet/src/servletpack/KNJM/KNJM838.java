/*
 * $Id: 13a8c38a958ecc263027dfe84d2ed0c6d7114f2a $
 *
 * 作成日: 2012/12/12
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;


import java.math.BigDecimal;
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

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 試験統計
 */
public class KNJM838 {

    private static final Log log = LogFactory.getLog(KNJM838.class);

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
    
    private static int getMS932ByteLength(final String name) {
        int len = 0;
        if (null != name) {
            try {
                len = name.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return len;
    }
    
    private List getPageList(final List list, final int size) {
        final List pageList = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= size) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
        }
        return pageList;
    }
    
    private void svfVrsOutInt(final Vrw32alp svf, final String field, final Integer i) {
        if (null != i) {
            svf.VrsOut(field, i.toString());
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final int maxLine = 26 * 2;
        
		final List subclassAllList = getSubclassList(db2);
		final List pageList = getPageList(subclassAllList, maxLine);
		
		final Integer zero = new Integer(0);
		SubclassStat[] semTotal = new SubclassStat[] {
		        new SubclassStat("1", zero, zero, zero, zero),
		        new SubclassStat("2", zero, zero, zero, zero),
		};
		
		for (int pi = 0; pi < pageList.size(); pi++) {
		    final List subclassList = (List) pageList.get(pi);
		    
	        svf.VrSetForm("KNJM838.frm", 4);
	        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度 試験統計"); // タイトル
	        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 日付

	        for (int line = 0; line < subclassList.size(); line++) {
	            final Subclass subclass = (Subclass) subclassList.get(line);
	            
	            svf.VrsOut("SUBCLASS_NAME" + (getMS932ByteLength(subclass._subclassname) > 20 ? "2" : "1"), subclass._subclassname); // 科目名
	            if (null != subclass._classname) {
	                svf.VrsOut("CLASS_NAME", subclass._classname);
	            }
	            final String[] semes = new String[] {"1", "2"};
	            for (int si = 0; si < semes.length; si++) {
	                final String i = semes[si];
                    final SubclassStat stat = (SubclassStat) subclass._statMap.get(i);
	                if (null != stat) {
	                    
	                    svfVrsOutInt(svf, "ATTEND_SUM" + i, stat._chairStdCount); // 受講者数
	                    svfVrsOutInt(svf, "EXAM_SUM" + i, stat._recordDatCount); // 受験者数
	                    svf.VrsOut("EXAM_PER" + i, stat.getJukenRitsu()); // 受験率
	                    svfVrsOutInt(svf, "SUBTOTAL_SCORE" + i, stat._sum); // 総合点
	                    svf.VrsOut("AERAGE" + i, stat.getAvg()); // 平均点
	                    svfVrsOutInt(svf, "FAIL_SUM" + i, stat._fugoukakuCount); // 不合格者数
	                    svf.VrsOut("PASS_PER" + i, stat.getGoukakuRitsu()); // 合格率

	                    semTotal[si] = semTotal[si].add(stat);
	                    if (pi == pageList.size() - 1) {
	                        svfVrsOutInt(svf, "TOTAL_ATTEND_SUM" + i, semTotal[si]._chairStdCount); // 受講者数
	                        svfVrsOutInt(svf, "TOTAL_EXAM_SUM" + i, semTotal[si]._recordDatCount); // 受験者数
	                        svf.VrsOut("TOTAL_EXAM_PER" + i, semTotal[si].getJukenRitsu()); // 受験率
	                        svfVrsOutInt(svf, "TOTAL_SUBTOTAL_SCORE" + i, semTotal[si]._sum); // 総合点
	                        svf.VrsOut("TOTAL_AERAGE" + i, semTotal[si].getAvg()); // 平均点
	                        svfVrsOutInt(svf, "TOTAL_FAIL_SUM" + i, semTotal[si]._fugoukakuCount); // 不合格者数
	                        svf.VrsOut("TOTAL_PASS_PER" + i, semTotal[si].getGoukakuRitsu()); // 合格率
	                    }
	                }
	            }
	            svf.VrEndRecord();
	        }
	        for (int i = subclassList.size(); i < maxLine; i++) {
                svf.VrsOut("SUBCLASS_NAME1", "\n"); // 科目名
                svf.VrEndRecord();
	        }
	        _hasData = true;
		}
    }

    private static class Subclass {
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _classname;
        final String _subclassname;
        final Map _statMap = new HashMap();

        Subclass(
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String classname,
                final String subclassname
        ) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
        }
    }

    private static class SubclassStat {
        final String _semester;
        final Integer _chairStdCount;
        final Integer _recordDatCount;
        final Integer _sum;
        final Integer _fugoukakuCount;

        SubclassStat(
                final String semester,
                final Integer chairStdCount,
                final Integer recordDatCount,
                final Integer sum,
                final Integer fugoukakuCount
        ) {
            _semester = semester;
            _chairStdCount = chairStdCount;
            _recordDatCount = recordDatCount;
            _sum = sum;
            _fugoukakuCount = fugoukakuCount;
        }

        public String getAvg() {
            if (null == _sum || null == _recordDatCount || _recordDatCount.intValue() == 0) {
                return null;
            }
            return new BigDecimal(_sum.intValue()).divide(new BigDecimal(_recordDatCount.intValue()), 1, BigDecimal.ROUND_HALF_UP).toString();
        }
        
        static int toInt(final Integer i) {
            return null == i ? 0 : i.intValue();
        }

        public String getGoukakuRitsu() {
            return percentage(new Integer(toInt(_recordDatCount) - toInt(_fugoukakuCount)), _recordDatCount);
        }

        public String getJukenRitsu() {
            return percentage(_recordDatCount, _chairStdCount);
        }
        
        public SubclassStat add(final SubclassStat stat) {
            if (null == stat) {
                return this;
            }
            return new SubclassStat(_semester, 
                    addInt(_chairStdCount, stat._chairStdCount),
                    addInt(_recordDatCount, stat._recordDatCount),
                    addInt(_sum, stat._sum),
                    addInt(_fugoukakuCount, stat._fugoukakuCount));
        }
        
        private static Integer addInt(final Integer num1, final Integer num2) {
            if (null == num1) return num2;
            return new Integer(num1.intValue() + (null == num2 ? 0 : num2.intValue())); 
        }

        private static String percentage(final Integer num1, final Integer num2) {
            if (null == num1 || null == num2 || 0 == num2.intValue()) {
                return null;
            }
            final BigDecimal bd1 = new BigDecimal(num1.intValue());
            final BigDecimal bd2 = new BigDecimal(num2.intValue());
            return bd1.multiply(new BigDecimal(100)).divide(bd2, 1, BigDecimal.ROUND_HALF_UP).toString();
        }
    }
    
    private List getSubclassList(final DB2UDB db2) {
        List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String classcd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String classname = rs.getString("CLASSNAME");
                final String subclassname = rs.getString("SUBCLASSNAME");
                
                if (null == getSubclass(list, classcd, schoolKind, curriculumCd, subclasscd)) {
                    list.add(new Subclass(classcd, schoolKind, curriculumCd, subclasscd, classname, subclassname));
                }
                final Subclass subclass = getSubclass(list, classcd, schoolKind, curriculumCd, subclasscd);
                
                final String semester = rs.getString("SEMESTER");
                final Integer chairStdCount = toInteger(rs.getString("CHAIR_STD_COUNT"));
                final Integer recordDatCount = toInteger(rs.getString("RECORD_DAT_COUNT"));
                final Integer sum = toInteger(rs.getString("SUM"));
                final Integer fugoukakuCount = (null == recordDatCount || recordDatCount.intValue() == 0) ? null : toInteger(rs.getString("FUGOUKAKU_COUNT"));
                final SubclassStat stat = new SubclassStat(semester, chairStdCount, recordDatCount, sum, fugoukakuCount);
                subclass._statMap.put(semester, stat);
            }
       } catch (Exception ex) {
            log.fatal("exception!", ex);
       } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
       }
       return list;
    }

    private Integer toInteger(final String s) throws SQLException {
        return null == s ? null : Integer.valueOf(s);
    }
    
    private Subclass getSubclass(final List list, final String classcd, final String schoolKind, final String curriculumCd, final String subclassCd) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            if (subclass._classcd.equals(classcd) && subclass._schoolKind.equals(schoolKind) && subclass._curriculumCd.equals(curriculumCd) && subclass._subclasscd.equals(subclassCd)) {
                return subclass;
            }
        }
        return null;
    }
    
    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CHAIR_STD_MAX AS ( ");
        stb.append("     SELECT YEAR, SEMESTER, CHAIRCD, MAX(APPENDDATE) AS APPENDDATE ");
        stb.append("     FROM CHAIR_STD_DAT ");
        stb.append("     WHERE YEAR = '" + _param._year + "'  ");
        stb.append("     GROUP BY YEAR, SEMESTER, CHAIRCD ");
        stb.append(" ) ");
        stb.append("     SELECT ");
        stb.append("         T2.YEAR, ");
        stb.append("         T2.SEMESTER, ");
        stb.append("         T2.CLASSCD, ");
        stb.append("         T2.SCHOOL_KIND, ");
        stb.append("         T2.CURRICULUM_CD, ");
        stb.append("         T2.SUBCLASSCD, ");
        stb.append("         CMST.CLASSNAME, ");
        stb.append("         SCMST.SUBCLASSNAME, ");
        stb.append("         COUNT(T1.SCHREGNO) AS CHAIR_STD_COUNT, ");
        stb.append("         CASE WHEN T2.SEMESTER = '1' THEN  ");
        stb.append("                 COUNT(T4.SCHREGNO) ");
        stb.append("             ELSE  ");
        stb.append("                 COUNT(T5.SCHREGNO) ");
        stb.append("         END AS RECORD_DAT_COUNT, ");
        stb.append("         CASE WHEN T2.SEMESTER = '1' THEN  ");
        stb.append("                 SUM(T4.SEM1_INTR_VALUE) ");
        stb.append("             ELSE  ");
        stb.append("                 SUM(T5.SEM2_INTR_VALUE) ");
        stb.append("         END AS SUM, ");
        stb.append("         CASE WHEN T2.SEMESTER = '1' THEN  ");
        stb.append("                 SUM(CASE WHEN T4.SEM1_INTR_VALUE < 40 THEN 1 ELSE 0 END) ");
        stb.append("             ELSE  ");
        stb.append("                 SUM(CASE WHEN T5.SEM2_INTR_VALUE < 40 THEN 1 ELSE 0 END) ");
        stb.append("         END AS FUGOUKAKU_COUNT ");
        stb.append("     FROM ");
        stb.append("         CHAIR_STD_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND VALUE(BASE.INOUTCD, '') NOT IN ('8', '9') "); // 聴講生、併修生は集計の対象外
        stb.append("             AND (T1.SEMESTER =  '1' AND VALUE(BASE.GRD_DATE, '9999-12-31') >= '" + _param._seisekiKaigiDate + "' "); // パラメータ日付時点で在籍している生徒が対象
        stb.append("               OR T1.SEMESTER <> '1' AND VALUE(BASE.GRD_DATE, '9999-12-31') >= '" + _param._loginDate + "') "); // ログイン日付時点で在籍している生徒が対象
        stb.append("         INNER JOIN CHAIR_STD_MAX T1_2 ON T1_2.YEAR = T1.YEAR ");
        stb.append("             AND T1_2.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T1_2.CHAIRCD = T1.CHAIRCD ");
        stb.append("             AND T1_2.APPENDDATE = T1.APPENDDATE ");
        stb.append("         INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("             AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("         LEFT JOIN RECORD_DAT T4 ON T4.YEAR = T2.YEAR ");
        stb.append("             AND T4.CLASSCD = T2.CLASSCD ");
        stb.append("             AND T4.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("             AND T4.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("             AND T4.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("             AND T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND T4.SEM1_INTR_VALUE IS NOT NULL ");
        stb.append("         LEFT JOIN RECORD_DAT T5 ON T5.YEAR = T2.YEAR ");
        stb.append("             AND T5.CLASSCD = T2.CLASSCD ");
        stb.append("             AND T5.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("             AND T5.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("             AND T5.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("             AND T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND T5.SEM2_INTR_VALUE IS NOT NULL ");
        stb.append("         LEFT JOIN CLASS_MST CMST ON CMST.CLASSCD = T2.CLASSCD ");
        stb.append("             AND CMST.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("         LEFT JOIN SUBCLASS_MST SCMST ON SCMST.CLASSCD = T2.CLASSCD ");
        stb.append("             AND SCMST.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("             AND SCMST.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("             AND SCMST.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("     GROUP BY ");
        stb.append("         T2.YEAR, ");
        stb.append("         T2.SEMESTER, ");
        stb.append("         T2.CLASSCD, ");
        stb.append("         T2.SCHOOL_KIND, ");
        stb.append("         T2.CURRICULUM_CD, ");
        stb.append("         T2.SUBCLASSCD, ");
        stb.append("         CMST.CLASSNAME, ");
        stb.append("         SCMST.SUBCLASSNAME ");
        stb.append("     ORDER BY ");
        stb.append("         T2.YEAR, ");
        stb.append("         T2.SCHOOL_KIND, ");
        stb.append("         T2.CLASSCD, ");
        stb.append("         T2.SUBCLASSCD, ");
        stb.append("         T2.CURRICULUM_CD, ");
        stb.append("         T2.SEMESTER ");
        return stb.toString();
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
        private final String _year;
        private final String _loginDate;
        private final String _seisekiKaigiDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _loginDate = request.getParameter("LOGIN_DATE").replace('/', '-');
            _seisekiKaigiDate = StringUtils.isBlank(request.getParameter("SEISEKI_KAIGI_DATE")) ? _loginDate : request.getParameter("SEISEKI_KAIGI_DATE").replace('/', '-');
            log.fatal(" seisekiKaigiDate = " + _seisekiKaigiDate);
            log.fatal("        loginDate = " + _loginDate);
        }
    }
}

// eof

