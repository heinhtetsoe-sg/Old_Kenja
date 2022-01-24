/*
 * $Id: 50fed01a891730570b9984af3ed191e079e38f09 $
 *
 * 作成日: 2019/09/24
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJG;


import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
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
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

/*
 *  学校教育システム 賢者 [事務管理] 証明書
 */
public class KNJG046E {

    private static final Log log = LogFactory.getLog(KNJG046E.class);

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

            printMain(svf, db2);
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

    private void printMain(final Vrw32alp svf, final DB2UDB db2) throws ParseException {

        final List mergeDaysList = new ArrayList();
        final Map chairList = getChairListMap(db2, mergeDaysList);
        final Map absentList = getAbsentListMap(db2, mergeDaysList);
        final Map getschChrStfDiaryMap = getschChrStfDiaryMap(db2, mergeDaysList);

        Collections.sort(mergeDaysList);

        for (Iterator itd = mergeDaysList.iterator();itd.hasNext();) {
        	String keyDate = (String)itd.next();
        	if (keyDate == null || "".equals(keyDate)) continue;
        	svf.VrSetForm("KNJG046E.frm", 1);
        	final List pd1List;
        	if (chairList.containsKey(keyDate)) {
        		pd1List = (List)chairList.get(keyDate);
        	} else {
        		pd1List = null;
        	}
        	final List pd2List;
        	if (absentList.containsKey(keyDate)) {
        		pd2List = (List)absentList.get(keyDate);
        	} else {
        		pd2List = null;
        	}
        	final SchChrStfDiaryData scsd;
        	if (getschChrStfDiaryMap.containsKey(keyDate)) {
        		scsd = (SchChrStfDiaryData)getschChrStfDiaryMap.get(keyDate);
        	} else {
        		scsd = null;
        	}
        	printDate(svf, db2, keyDate, pd1List, pd2List, scsd);
        	svf.VrEndPage();
        	_hasData = true;
        }

    }

    private Map getChairListMap(final DB2UDB db2, final List mergeDaysList) {
        PreparedStatement ps = null;
        ResultSet rs = null;
    	final Map retMap = new LinkedMap();
    	List addwk = new ArrayList();
        try {
            final String sql = getChairInfoSql();
            //log.debug(" getChairListMap sql = "+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String executeDate = rs.getString("EXECUTEDATE");
            	final String periodCd = rs.getString("PERIODCD");
            	final String chairCd = rs.getString("CHAIRCD");
            	final String hr_NameAbbv = rs.getString("HR_NAMEABBV");

            	final String classCd = "1".equals(_param._useCurriculumcd) ? rs.getString("CLASSCD") : "";
            	final String school_Kind = "1".equals(_param._useCurriculumcd) ? rs.getString("SCHOOL_KIND") : "";
            	final String curriculum_Cd = "1".equals(_param._useCurriculumcd) ? rs.getString("CURRICULUM_CD") : "";
            	final String subclassCd = rs.getString("SUBCLASSCD");
            	final String subclassAbbv = rs.getString("SUBCLASSABBV");
            	final String chairName = rs.getString("CHAIRNAME");
            	final String remark = rs.getString("REMARK");
            	final String abs = rs.getString("ABS");
            	final String late = rs.getString("LATE");
            	final String early = rs.getString("EARLY");
            	final String comment = rs.getString("COMMENT");
            	PrintData1 addobj1 = new PrintData1(executeDate, periodCd, chairCd, hr_NameAbbv, classCd, school_Kind, curriculum_Cd, subclassCd, subclassAbbv, chairName, remark, abs, late, early, comment);
            	if (!retMap.containsKey(executeDate)) {
            	    addwk = new ArrayList();
            	    retMap.put(executeDate, addwk);
            	}
            	addwk.add(addobj1);
            	if (!mergeDaysList.contains(executeDate)) {
            		mergeDaysList.add(executeDate);
            	}
            }
        } catch (Exception ex) {
            log.error("printSvfMain read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    	return retMap;
    }

    private String getChairInfoSql() {
    	final StringBuffer stb = new StringBuffer();

    	stb.append("         WITH MAIN AS ( ");
    	stb.append("          SELECT ");
    	stb.append("              T1.EXECUTEDATE, ");
    	stb.append("              T1.periodcd, ");
    	stb.append("              T1.chaircd, ");
    	stb.append("              T1.groupcd, ");
    	stb.append("              T1.attendcd, ");
    	stb.append("              T1.chargediv, ");
    	stb.append("              CASE WHEN T5.staffcd is not null then T5.staffcd else T1.staffcd end as staffcd, ");
    	stb.append("              T4.hr_nameabbv, ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("              T1.CLASSCD, ");
    	    stb.append("              T1.SCHOOL_KIND, ");
    	    stb.append("              T1.CURRICULUM_CD, ");
    	}
    	stb.append("              T1.SUBCLASSCD, ");
    	stb.append("              T1.subclassabbv, ");
    	stb.append("              substr(T1.chairname,1,9) AS CHAIRNAME, ");
    	stb.append("              T3.trgtgrade, ");
    	stb.append("              T3.trgtclass ");
    	stb.append("          FROM ( SELECT t1.semester, t1.EXECUTEDATE, t1.periodcd, t1.executed AS attendcd, t1.DATADIV, ");
    	stb.append("                        t1.chaircd, t2.groupcd, t2.chairname, ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("                        t2.CLASSCD, t2.SCHOOL_KIND, t2.CURRICULUM_CD, ");
    	}
    	stb.append("                        t2.SUBCLASSCD, t5.subclassabbv, ");
    	stb.append("                        t3.staffcd, t3.chargediv ");
    	stb.append("                   FROM sch_chr_dat   t1 ");
    	stb.append("                        INNER JOIN chair_dat t2 ");
    	stb.append("                          ON t2.year        = t1.year ");
    	stb.append("                         AND t2.semester    = t1.semester ");
    	stb.append("                         AND t2.chaircd     = t1.chaircd ");
    	stb.append("                        LEFT OUTER JOIN subclass_mst t5 ");
    	stb.append("                          ON t2.SUBCLASSCD = t5.SUBCLASSCD ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("                         AND t2.CLASSCD = t5.CLASSCD ");
    	    stb.append("                         AND t2.SCHOOL_KIND = t5.SCHOOL_KIND ");
    	    stb.append("                         AND t2.CURRICULUM_CD = t5.CURRICULUM_CD ");
    	}
    	stb.append("                        INNER JOIN chair_stf_dat t3 ");
    	stb.append("                          ON t3.year        = t2.year ");
    	stb.append("                         AND t3.semester    = t2.semester ");
    	stb.append("                         AND t3.chaircd     = t2.chaircd ");
    	stb.append("                        INNER JOIN v_staff_mst t4 ");
    	stb.append("                          ON t3.year = t4.year ");
    	stb.append("                         AND t3.staffcd = t4.staffcd ");
    	stb.append("                         AND '1' = t4.chargeclasscd ");
    	stb.append("                  WHERE t1.executedate BETWEEN DATE('" + _param._sdate + "') AND DATE('" + _param._edate + "') ");
    	stb.append("                        AND t1.year        = '" + _param._year + "'  ");
    	// stb.append("                        AND t1.semester    = '"++"' ");   //EXECUTEDATEでチェックするので、SEMESTER不要。
    	stb.append("               ) T1 ");
    	stb.append("               LEFT OUTER JOIN ( SELECT t1.year, t1.semester, t2.chaircd, ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("                                 t2.CLASSCD, t2.SCHOOL_KIND, t2.CURRICULUM_CD, ");
    	}
    	stb.append("                                 t2.SUBCLASSCD, t2.groupcd, t1.trgtgrade, t1.trgtclass ");
    	stb.append("                                   FROM chair_cls_dat t1, chair_dat t2 ");
    	stb.append("                                  WHERE t2.year        = t1.year AND ");
    	stb.append("                                        t2.semester    = t1.semester AND ");
    	stb.append("                                        (t1.chaircd     = '0000000' OR t2.chaircd = t1.chaircd) AND ");
    	stb.append("                                        t2.groupcd     = t1.groupcd AND ");
    	stb.append("                                        t1.year        = '" + _param._year + "' ");
    	stb.append("                               ) T3 ON T1.chaircd = T3.chaircd AND T1.semester = T3.semester ");
    	stb.append("               LEFT JOIN schreg_regd_hdat T4 ");
    	stb.append("                 ON T3.year = T4.year ");
    	stb.append("                AND T3.semester = T4.semester ");
    	stb.append("                AND T3.trgtgrade = T4.grade ");
    	stb.append("                AND T3.trgtclass = T4.hr_class ");
    	stb.append("               LEFT JOIN SCH_STF_DAT T5 ");
    	stb.append("                 ON T1.PERIODCD = T5.PERIODCD ");
    	stb.append("                AND T1.CHAIRCD = T5.CHAIRCD ");
    	stb.append("                AND T1.executedate = T5.executedate ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("         WHERE T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
    	}
    	stb.append("          ) ");
    	stb.append("          SELECT ");
    	stb.append("              TT1.EXECUTEDATE, ");     //日付
    	stb.append("              TT1.periodcd, ");        // 時限目コード
    	stb.append("              TT1.chaircd,      ");    // 口座コード
    	stb.append("              TT1.hr_nameabbv,  ");    //クラス略称
    	if ("1".equals(_param._useCurriculumcd)) {
            stb.append("              TT1.CLASSCD,      ");    //教科コード
    	    stb.append("              TT1.SCHOOL_KIND, ");     //校種
    	    stb.append("              TT1.CURRICULUM_CD, ");   //
    	}
    	stb.append("              TT1.SUBCLASSCD,   ");    //科目コード
    	stb.append("              TT1.subclassabbv,   ");  //科目略称
    	stb.append("              TT1.CHAIRNAME,      ");  //講座名
    	stb.append("              TT2.REMARK, ");          //授業内容
    	stb.append("              COUNT(TT3_1.SCHREGNO) AS ABS, ");   //欠席
    	stb.append("              COUNT(TT3_2.SCHREGNO) AS LATE, ");  //遅刻
    	stb.append("              COUNT(TT3_3.SCHREGNO) AS EARLY, ");  //早退
    	stb.append("              TT4.REMARK1 AS COMMENT ");
    	stb.append("          FROM ");
    	stb.append("              MAIN TT1 ");
    	stb.append("              LEFT JOIN SCH_CHR_REMARK_DAT TT2 ");
    	stb.append("                ON TT2.EXECUTEDATE = TT1.EXECUTEDATE ");
    	stb.append("               AND TT2.PERIODCD = TT1.PERIODCD ");
    	stb.append("               AND TT2.chaircd = TT1.chaircd ");
    	stb.append("               AND TT2.REMARK_DIV = '01' ");
    	stb.append("              LEFT JOIN ATTEND_DAT TT3_1 ");
    	stb.append("                ON TT3_1.ATTENDDATE = TT1.EXECUTEDATE ");
    	stb.append("               AND TT3_1.PERIODCD = TT1.PERIODCD ");
    	stb.append("               AND TT3_1.DI_CD IN ('4', '5', '6') ");
    	stb.append("              LEFT JOIN ATTEND_DAT TT3_2 ");
    	stb.append("                ON TT3_2.ATTENDDATE = TT1.EXECUTEDATE ");
    	stb.append("               AND TT3_2.PERIODCD = TT1.PERIODCD ");
    	stb.append("               AND TT3_2.DI_CD IN ('15') ");
    	stb.append("              LEFT JOIN ATTEND_DAT TT3_3 ");
    	stb.append("                ON TT3_3.ATTENDDATE = TT1.EXECUTEDATE ");
    	stb.append("               AND TT3_3.PERIODCD = TT1.PERIODCD ");
    	stb.append("               AND TT3_3.DI_CD IN ('16') ");
    	stb.append("              LEFT JOIN SCH_CHR_STF_DIARY_DETAIL_DAT TT4 ");
    	stb.append("                ON TT4.SCHOOLCD = '" + _param._schoolCd + "' ");
    	stb.append("               AND TT4.SCHOOL_KIND = '" + _param._schoolKind + "' ");
    	stb.append("               AND TT4.STAFFCD = TT1.staffcd ");
    	stb.append("               AND TT4.DIARY_DATE = TT1.EXECUTEDATE ");
    	stb.append("               AND TT4.PERIODCD = TT1.PERIODCD ");
    	stb.append("          WHERE ");
    	stb.append("              TT1.staffcd = '" + _param._staffCd + "' ");
    	stb.append("          GROUP BY ");
    	stb.append("              TT1.EXECUTEDATE, ");
    	stb.append("              TT1.periodcd, ");
    	stb.append("              TT1.chaircd, ");
    	stb.append("              TT1.hr_nameabbv, ");
    	if ("1".equals(_param._useCurriculumcd)) {
    	    stb.append("              TT1.CLASSCD, ");
    	    stb.append("              TT1.SCHOOL_KIND, ");
    	    stb.append("              TT1.CURRICULUM_CD, ");
    	}
    	stb.append("              TT1.SUBCLASSCD, ");
    	stb.append("              TT1.subclassabbv, ");
    	stb.append("              TT1.CHAIRNAME, ");
    	stb.append("              TT1.trgtgrade, ");
    	stb.append("              TT1.trgtclass, ");
    	stb.append("              TT2.REMARK, ");
    	stb.append("              TT4.REMARK1 ");
    	stb.append("          ORDER BY ");
    	stb.append("              TT1.EXECUTEDATE, ");
    	stb.append("              TT1.periodcd, ");
    	stb.append("              TT1.chaircd, ");
    	stb.append("              TT1.trgtgrade || TT1.trgtclass || TT1.hr_nameabbv ");

    	return stb.toString();
    }

    private Map getAbsentListMap(final DB2UDB db2, final List mergeDaysList) {
        PreparedStatement ps = null;
        ResultSet rs = null;
    	final Map retMap = new LinkedMap();
    	List addwk = new ArrayList();
        try {
            final String sql = getAbsentInfoSql();
            //log.debug("getAbsentInfoSql sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String executeDate = rs.getString("EXECUTEDATE");
            	final String periodCd = rs.getString("periodcd");
            	final String chairCd = rs.getString("chaircd");
            	final String hr_NameAbbv = rs.getString("hr_nameabbv");
            	final String classCd = rs.getString("CLASSCD");
            	final String school_Kind = rs.getString("SCHOOL_KIND");
            	final String curriculum_Cd = rs.getString("CURRICULUM_CD");
            	final String subclassCd = rs.getString("SUBCLASSCD");
            	final String subclassAbbv = rs.getString("subclassabbv");
            	final String chairName = rs.getString("CHAIRNAME");
            	final String abs_Schregno = StringUtils.defaultString(rs.getString("ABS_SCHREGNO"), "");
            	PrintData2 addobj2 = new PrintData2(executeDate, periodCd, chairCd, hr_NameAbbv, classCd, school_Kind, curriculum_Cd, subclassCd, subclassAbbv, chairName, abs_Schregno);
            	if (!retMap.containsKey(executeDate)) {
            	    addwk = new ArrayList();
            	    retMap.put(executeDate, addwk);
            	}
                addwk.add(addobj2);
            	if (!mergeDaysList.contains(executeDate)) {
            		mergeDaysList.add(executeDate);
            	}
            }
        } catch (Exception ex) {
            log.error("printSvfMain read error! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    	return retMap;
    }

    private String getAbsentInfoSql() {
    	final StringBuffer stb = new StringBuffer();

    	stb.append("         WITH MAIN AS ( ");
    	stb.append("          SELECT ");
    	stb.append("              T1.EXECUTEDATE, ");
    	stb.append("              T1.periodcd, ");
    	stb.append("              T1.chaircd, ");
    	stb.append("              T1.groupcd, ");
    	stb.append("              T1.attendcd, ");
    	stb.append("              T1.chargediv, ");
    	stb.append("              CASE WHEN T5.staffcd is not null then T5.staffcd else T1.staffcd end as staffcd, ");
    	stb.append("              T4.hr_nameabbv, ");
    	stb.append("              T1.CLASSCD, ");
    	stb.append("              T1.SCHOOL_KIND, ");
    	stb.append("              T1.CURRICULUM_CD, ");
    	stb.append("              T1.SUBCLASSCD, ");
    	stb.append("              T1.subclassabbv, ");
    	stb.append("              substr(T1.chairname,1,9) AS CHAIRNAME, ");
    	stb.append("              T3.trgtgrade, ");
    	stb.append("              T3.trgtclass ");
    	stb.append("          FROM ( SELECT t1.semester, t1.EXECUTEDATE, t1.periodcd, t1.executed AS attendcd, t1.DATADIV, ");
    	stb.append("                        t1.chaircd, t2.groupcd, t2.chairname, ");
    	stb.append("                        t2.CLASSCD, t2.SCHOOL_KIND, t2.CURRICULUM_CD, ");
    	stb.append("                        t2.SUBCLASSCD, t5.subclassabbv, ");
    	stb.append("                        t3.staffcd, t3.chargediv ");
    	stb.append("                   FROM sch_chr_dat   t1 ");
    	stb.append("                        INNER JOIN chair_dat t2 ");
    	stb.append("                          ON t2.year        = t1.year ");
    	stb.append("                         AND t2.semester    = t1.semester ");
    	stb.append("                         AND t2.chaircd     = t1.chaircd ");
    	stb.append("                        LEFT OUTER JOIN subclass_mst t5 ");
    	stb.append("                          ON t2.SUBCLASSCD = t5.SUBCLASSCD ");
    	stb.append("                         AND t2.CLASSCD = t5.CLASSCD ");
    	stb.append("                         AND t2.SCHOOL_KIND = t5.SCHOOL_KIND ");
    	stb.append("                         AND t2.CURRICULUM_CD = t5.CURRICULUM_CD ");
    	stb.append("                        INNER JOIN chair_stf_dat t3 ");
    	stb.append("                          ON t3.year        = t2.year ");
    	stb.append("                         AND t3.semester    = t2.semester ");
    	stb.append("                         AND t3.chaircd     = t2.chaircd ");
    	stb.append("                        INNER JOIN v_staff_mst t4 ");
    	stb.append("                          ON t3.year = t4.year ");
    	stb.append("                         AND t3.staffcd = t4.staffcd ");
    	stb.append("                         AND '1' = t4.chargeclasscd ");
    	stb.append("                  WHERE t1.executedate BETWEEN DATE('" + _param._sdate + "') AND DATE('" + _param._edate + "') ");
    	stb.append("                        AND t1.year        = '" + _param._year + "'  ");
    	stb.append("               ) T1 ");
    	stb.append("               LEFT OUTER JOIN ( SELECT t1.year, t1.semester, t2.chaircd, ");
    	stb.append("                                 t2.CLASSCD, t2.SCHOOL_KIND, t2.CURRICULUM_CD, ");
    	stb.append("                                 t2.SUBCLASSCD, t2.groupcd, t1.trgtgrade, t1.trgtclass ");
    	stb.append("                                   FROM chair_cls_dat t1, chair_dat t2 ");
    	stb.append("                                  WHERE t1.year        = '" + _param._year + "' AND ");
    	stb.append("                                        (t1.chaircd     = '0000000' OR t2.chaircd     = t1.chaircd )AND ");
    	stb.append("                                        t2.year        = t1.year AND ");
    	stb.append("                                        t2.semester    = t1.semester AND ");
    	stb.append("                                        t2.groupcd     = t1.groupcd ");
    	stb.append("                               ) T3 ON T1.chaircd = T3.chaircd AND T1.semester = T3.semester ");
    	stb.append("               LEFT JOIN schreg_regd_hdat T4 ");
    	stb.append("                 ON T3.year = T4.year ");
    	stb.append("                AND T3.semester = T4.semester ");
    	stb.append("                AND T3.trgtgrade = T4.grade ");
    	stb.append("                AND T3.trgtclass = T4.hr_class ");
    	stb.append("               LEFT JOIN SCH_STF_DAT T5 ");
    	stb.append("                 ON T1.PERIODCD = T5.PERIODCD ");
    	stb.append("                AND T1.CHAIRCD = T5.CHAIRCD ");
    	stb.append("                AND T1.executedate = T5.executedate ");
    	stb.append("          WHERE T1.SCHOOL_KIND IN ('" + _param._schoolKind + "') ");
    	stb.append("          ) ");
    	stb.append("          SELECT ");
    	stb.append("              TT1.EXECUTEDATE, ");
    	stb.append("              TT1.periodcd, ");      //限目コード
    	stb.append("              TT1.chaircd, ");       //口座コード
    	stb.append("              TT1.hr_nameabbv, ");   //クラス略称
    	stb.append("              TT1.CLASSCD, ");       //教科コード
    	stb.append("              TT1.SCHOOL_KIND, ");
    	stb.append("              TT1.CURRICULUM_CD, ");
    	stb.append("              TT1.SUBCLASSCD, ");    //科目コード
    	stb.append("              TT1.subclassabbv, ");  //科目略称
    	stb.append("              TT1.CHAIRNAME, ");     //講座名
    	stb.append("              CASE WHEN TT3_1.DI_CD IN ('4', '5', '6') THEN TT3_1B.NAME || '(' || TT3_1N.ABBV1 || ')' ");
    	stb.append("                   WHEN TT3_2.DI_CD IN ('15') THEN TT3_2B.NAME || '(' || TT3_2N.ABBV1 || ')' ");
    	stb.append("                   WHEN TT3_3.DI_CD IN ('16') THEN TT3_3B.NAME || '(' || TT3_3N.ABBV1 || ')' ");
    	stb.append("                   ELSE '' END AS ABS_SCHREGNO ");
    	stb.append("          FROM ");
    	stb.append("              MAIN TT1 ");
    	stb.append("              LEFT JOIN ATTEND_DAT TT3_1 ");
    	stb.append("                ON TT3_1.ATTENDDATE = TT1.EXECUTEDATE ");
    	stb.append("               AND TT3_1.PERIODCD = TT1.PERIODCD ");
    	stb.append("               AND TT3_1.DI_CD IN ('4', '5', '6') ");
    	stb.append("              LEFT JOIN NAME_MST TT3_1N ");
    	stb.append("                ON TT3_1N.NAMECD1 = 'C001' ");
    	stb.append("               AND TT3_1N.NAMECD2 = TT3_1.DI_CD ");
    	stb.append("              LEFT JOIN SCHREG_BASE_MST TT3_1B ");
    	stb.append("                ON TT3_1B.SCHREGNO = TT3_1.SCHREGNO ");
    	stb.append("              LEFT JOIN ATTEND_DAT TT3_2 ");
    	stb.append("                ON TT3_2.ATTENDDATE = TT1.EXECUTEDATE ");
    	stb.append("               AND TT3_2.PERIODCD = TT1.PERIODCD ");
    	stb.append("               AND TT3_2.DI_CD IN ('15') ");
    	stb.append("              LEFT JOIN NAME_MST TT3_2N ");
    	stb.append("                ON TT3_2N.NAMECD1 = 'C001' ");
    	stb.append("               AND TT3_2N.NAMECD2 = TT3_2.DI_CD ");
    	stb.append("              LEFT JOIN SCHREG_BASE_MST TT3_2B ");
    	stb.append("                ON TT3_2B.SCHREGNO = TT3_2.SCHREGNO ");
    	stb.append("              LEFT JOIN ATTEND_DAT TT3_3 ");
    	stb.append("                ON TT3_3.ATTENDDATE = TT1.EXECUTEDATE ");
    	stb.append("               AND TT3_3.PERIODCD = TT1.PERIODCD ");
    	stb.append("               AND TT3_3.DI_CD IN ('16') ");
    	stb.append("              LEFT JOIN NAME_MST TT3_3N ");
    	stb.append("                ON TT3_3N.NAMECD1 = 'C001' ");
    	stb.append("               AND TT3_3N.NAMECD2 = TT3_3.DI_CD ");
    	stb.append("              LEFT JOIN SCHREG_BASE_MST TT3_3B ");
    	stb.append("                ON TT3_3B.SCHREGNO = TT3_3.SCHREGNO ");
    	stb.append("          WHERE ");
    	stb.append("              TT1.staffcd = '" + _param._staffCd + "' ");
    	stb.append("          GROUP BY ");
    	stb.append("              TT1.EXECUTEDATE, ");
    	stb.append("              TT1.periodcd, ");
    	stb.append("              TT1.chaircd, ");
    	stb.append("              TT1.hr_nameabbv, ");
    	stb.append("              TT1.CLASSCD, ");
    	stb.append("              TT1.SCHOOL_KIND, ");
    	stb.append("              TT1.CURRICULUM_CD, ");
    	stb.append("              TT1.SUBCLASSCD, ");
    	stb.append("              TT1.subclassabbv, ");
    	stb.append("              TT1.CHAIRNAME, ");
    	stb.append("              TT1.trgtgrade, ");
    	stb.append("              TT1.trgtclass, ");
    	stb.append("              TT3_1.DI_CD, ");
    	stb.append("              TT3_2.DI_CD, ");
    	stb.append("              TT3_3.DI_CD, ");
    	stb.append("              TT3_1.SCHREGNO, ");
    	stb.append("              TT3_2.SCHREGNO, ");
    	stb.append("              TT3_3.SCHREGNO, ");
    	stb.append("              TT3_1B.NAME, ");
    	stb.append("              TT3_2B.NAME, ");
    	stb.append("              TT3_3B.NAME, ");
    	stb.append("              TT3_1N.ABBV1, ");
    	stb.append("              TT3_2N.ABBV1, ");
    	stb.append("              TT3_3N.ABBV1 ");
    	stb.append("          ORDER BY ");
    	stb.append("              TT1.EXECUTEDATE, ");
    	stb.append("              TT1.periodcd, ");
    	stb.append("              TT1.chaircd, ");
    	stb.append("              TT1.trgtgrade || TT1.trgtclass || TT1.hr_nameabbv, ");
    	stb.append("              TT3_1.SCHREGNO, ");
    	stb.append("              TT3_2.SCHREGNO, ");
    	stb.append("              TT3_3.SCHREGNO ");

    	return stb.toString();
    }

    private Map getschChrStfDiaryMap(final DB2UDB db2, final List mergeDaysList) {
        final Map retMap = new LinkedMap();

    	final StringBuffer stb = new StringBuffer();

    	stb.append(" SELECT ");
    	stb.append("   T1.DIARY_DATE, ");
    	stb.append("   N1.NAME1 AS WEATHER, ");
    	stb.append("   N2.NAME1 AS WEATHER2, ");
    	stb.append("   T1.TEMPERATURE, ");
    	stb.append("   T1.REMARK ");
    	stb.append(" FROM ");
    	stb.append("   SCH_CHR_STF_DIARY_DAT T1 ");
    	stb.append("   LEFT JOIN NAME_MST N1 ");
    	stb.append("     ON N1.NAMECD1 = 'A006' ");
    	stb.append("    AND N1.NAMECD2 = T1.WEATHER ");
    	stb.append("   LEFT JOIN NAME_MST N2 ");
    	stb.append("     ON N2.NAMECD1 = 'A006' ");
    	stb.append("    AND N2.NAMECD2 = T1.WEATHER2 ");
    	stb.append(" WHERE ");
    	stb.append("   SCHOOLCD = '" + _param._schoolCd + "' ");
    	stb.append("     AND SCHOOL_KIND = '" + _param._schoolKind + "' ");
    	stb.append("     AND STAFFCD = '" + _param._staffCd + "' ");
    	stb.append("     AND DIARY_DATE BETWEEN '" + _param._sdate + "' AND '" + _param._edate + "' ");
    	stb.append(" ORDER BY ");
    	stb.append("   T1.DIARY_DATE ");

        //log.debug(" getschChrStfDiaryMap sql = " + stb.toString());
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String diaryDate = rs.getString("DIARY_DATE");
            	final String weather = rs.getString("WEATHER");
            	final String weather2 = rs.getString("WEATHER2");
            	final String temperature = rs.getString("TEMPERATURE");
            	final String remark = rs.getString("REMARK");
            	SchChrStfDiaryData addobj = new SchChrStfDiaryData(diaryDate, weather, weather2, temperature, remark);
            	retMap.put(diaryDate, addobj);   // = rs.getString("STAFFNAME");
            	if (!mergeDaysList.contains(diaryDate)) {
            		mergeDaysList.add(diaryDate);
            	}
            }

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private void printDate(final Vrw32alp svf, final DB2UDB db2, final String keyDate, final List pd1List, final List pd2List,  final SchChrStfDiaryData scsd) throws ParseException {
    	//タイトル
    	//日付
    	final String dStr = KNJ_EditDate.h_format_JP(db2, keyDate);
    	svf.VrsOut("DATE", dStr);
    	svf.VrsOut("WEEK", "(" + convWeek(keyDate) + ")");
    	if (scsd != null) {
        	//天気&気温
    	    svf.VrsOut("WEATHER1", scsd._weather);
    	    svf.VrsOut("WEATHER2", scsd._weather2);
    	    svf.VrsOut("TEMP", scsd._temperature);
    	    VrsOutnRenban(svf, "EVENT", KNJ_EditKinsoku.getTokenList(scsd._remark, 100, 10));
    	}
    	final int tnlen = KNJ_EditEdit.getMS932ByteLength(_param._staffname);
    	final String tnfield = tnlen > 30 ? "3" : tnlen > 20 ? "2" : "1";
    	svf.VrsOut("TR_NAME" + tnfield, _param._staffname);
        if (pd1List != null) {
            int lineCnt = 1;
            String bakKey = "";
            String nowKey = "";
    		List outStrList = new ArrayList();
            PrintData1 lastpd1 = null;
            for (Iterator itpd1 = pd1List.iterator();itpd1.hasNext();) {
    		    PrintData1 pd1 = (PrintData1)itpd1.next();
                nowKey = pd1._periodCd + pd1._chairCd + pd1._classCd + pd1._school_Kind + pd1._curriculum_Cd + pd1._subclassCd;
                if (!"".equals(bakKey) && !bakKey.equals(nowKey)) {
                	outputTbl1(svf, db2, lastpd1, lineCnt, outStrList);
                	outStrList = new ArrayList();
        	    	lineCnt++;
                }
              	outStrList.add(pd1._hr_NameAbbv);
    		    lastpd1 = pd1;
                bakKey = nowKey;
    	    }
            if (outStrList.size() > 0) {
            	outputTbl1(svf, db2, lastpd1, lineCnt, outStrList);
    	    	lineCnt++;
            }
    	}

    	if (pd2List != null) {
    		int lineCnt = 1;
    		String bakKey = "";
    		String nowKey = "";
    		String outputStr = "";
    		String sep = "";
    		PrintData2 lastpd2 = null;
    	    for (Iterator itpd2 = pd2List.iterator();itpd2.hasNext();) {
    		    PrintData2 pd2 = (PrintData2)itpd2.next();
    		    nowKey = pd2._periodCd + pd2._chairCd + pd2._classCd + pd2._school_Kind + pd2._curriculum_Cd + pd2._subclassCd;
    		    if (!"".equals(bakKey) && !bakKey.equals(nowKey)) {
    		    	outputTbl2(svf, db2, lastpd2, lineCnt, outputStr);
    	    	    outputStr = "";
    	    	    sep = "";
    	    	    lineCnt++;
                }
                if (!"".equals(pd2._abs_Schregno)) {
                    outputStr += sep + pd2._abs_Schregno;
                    sep = "、";
    		    }
    		    lastpd2 = pd2;
    		    bakKey = nowKey;
    	    }
    	    if (!"".equals(outputStr) && lastpd2 != null) {
		    	outputTbl2(svf, db2, lastpd2, lineCnt, outputStr);
	    	    outputStr = "";
	    	    sep = "";
    	    	lineCnt++;
    	    }
    	}
    }

    private void outputTbl1(final Vrw32alp svf, final DB2UDB db2, final PrintData1 pd1, final int lineCnt, final List outStrList) {
        //校時
	    svf.VrsOutn("PERIOD1", lineCnt, pd1._periodCd);
        //講座名
        svf.VrsOutn("CHAIR_CD1", lineCnt, pd1._chairCd);
        svf.VrsOutn("CHAIR_NAME1", lineCnt, pd1._chairName);
        //受講クラス
        VrsOutnCont(svf, "CHAIR_CLS", lineCnt, outStrList);
        //授業内容
        VrsOutnCont(svf, "CONTENT", lineCnt, KNJ_EditKinsoku.getTokenList(pd1._remark, 40, 4));
        //欠席数/遅刻/早退
        svf.VrsOutn("ATTEND", lineCnt, pd1._abs);
        svf.VrsOutn("LATE", lineCnt, pd1._late);
        svf.VrsOutn("EARLY", lineCnt, pd1._early);
        //コメント
        VrsOutnCont(svf, "COMMENT", lineCnt, KNJ_EditKinsoku.getTokenList(pd1._comment, 40, 4));
    }

    private void outputTbl2(final Vrw32alp svf, final DB2UDB db2, final PrintData2 pd2, final int lineCnt, final String outputStr) {
        //校時
	    svf.VrsOutn("PERIOD2", lineCnt, pd2._periodCd);
        //講座名
        svf.VrsOutn("CHAIR_CD2", lineCnt, pd2._chairCd);
	    svf.VrsOutn("CHAIR_NAME2", lineCnt, pd2._chairName);
	    //欠席数/遅刻/早退者
	    //svf.VrsOutn("ATTEND_STUDENT", lineCnt, outputStr);
	    VrsOutnCont(svf, "ATTEND_STUDENT", lineCnt, KNJ_EditKinsoku.getTokenList(outputStr, 76, 2));
    }

    private String convWeek(final String date) {
        if (null == date) {
            return date;
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTime(Date.valueOf(date));
        final String youbi = new String[] {null, "日", "月", "火", "水", "木", "金", "土"} [cal.get(Calendar.DAY_OF_WEEK)];
        return youbi + "曜日";
    }

    private void VrsOutnCont(final Vrw32alp svf, final String field, final int gyo, final List value) {
        for (int i = 0 ; i < value.size(); i++) {
        	svf.VrsOutn(field + (i + 1), gyo, (String) value.get(i));
        }
    }

    private void VrsOutnRenban(final Vrw32alp svf, final String field, final List value) {
        for (int i = 0 ; i < value.size(); i++) {
        	svf.VrsOutn(field, i + 1, (String) value.get(i));
        }
    }

    private class PrintData1 {
        final String _executeDate;
        final String _periodCd;
        final String _chairCd;
        final String _hr_NameAbbv;
        final String _classCd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclassCd;
        final String _subclassAbbv;
        final String _chairName;
        final String _remark;
        final String _abs;
        final String _late;
        final String _early;
        final String _comment;
        public PrintData1(final String executeDate, final String periodCd, final String chairCd, final String hr_NameAbbv,
        		final String classCd, final String school_Kind, final String curriculum_Cd, final String subclassCd,
        		final String subclassAbbv, final String chairName, final String remark, final String abs, final String late,
        		final String early, final String comment) {
            _executeDate = executeDate;
            _periodCd = periodCd;
            _chairCd = chairCd;
            _hr_NameAbbv = hr_NameAbbv;
            _classCd = classCd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclassCd = subclassCd;
            _subclassAbbv = subclassAbbv;
            _chairName = chairName;
            _remark = remark;
            _abs = abs;
            _late = late;
            _early = early;
            _comment = comment;
        }

    }
    private class PrintData2 {
        final String _executeDate;
        final String _periodCd;
        final String _chairCd;
        final String _hr_NameAbbv;
        final String _classCd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclassCd;
        final String _subclassAbbv;
        final String _chairName;
        final String _abs_Schregno;
        public PrintData2(final String executeDate, final String periodCd, final String chairCd, final String hr_NameAbbv,
        		           final String classCd, final String school_Kind, final String curriculum_Cd, final String subclassCd,
        		           final String subclassAbbv, final String chairName, final String abs_Schregno)
        {
            _executeDate = executeDate;
            _periodCd = periodCd;
            _chairCd = chairCd;
            _hr_NameAbbv = hr_NameAbbv;
            _classCd = classCd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclassCd = subclassCd;
            _subclassAbbv = subclassAbbv;
            _chairName = chairName;
            _abs_Schregno = abs_Schregno;
        }

    }

    private class SchChrStfDiaryData {
        final String _diaryDate;
        final String _weather;
        final String _weather2;
        final String _temperature;
        final String _remark;
        public SchChrStfDiaryData (final String diaryDate, final String weather, final String weather2, final String temperature, final String remark) {
        	_diaryDate = diaryDate;
        	_weather = weather;
        	_weather2 = weather2;
        	_temperature = temperature;
        	_remark = remark;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(request, db2);
        log.fatal("$Revision: 69865 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /**
     * パラメータクラス
     */
    private static class Param {

        final String _year;
        final String _loginDate;

        final String _sdate;
        final String _edate;
        final String _schoolCd;
        final String _schoolKind;
        final String _staffCd;
        final String _staffname;

        private KNJSchoolMst _knjSchoolMst;
        private KNJDefineSchool _definecode;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;

        public Param(final HttpServletRequest request, final DB2UDB db2) {
            //_prgId = request.getParameter("PRGID");
            if (null != request.getParameter("DATE_TO")) {
                _year = request.getParameter("YEAR");
                _edate = request.getParameter("DATE_TO").replace('/', '-');
                _sdate = request.getParameter("DATE_FROM").replace('/', '-');
            } else {
                _year = request.getParameter("CTRL_YEAR");
                _edate = request.getParameter("DIARY_DATE").replace('/', '-');
                _sdate = _edate;
            }
            _staffCd = request.getParameter("STAFFCD");
            _staffname = getStaffName(db2);
            _loginDate = request.getParameter("LOGIN_DATE").replace('/', '-');
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _schoolCd = request.getParameter("SCHOOLCD");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ読み込みエラー", e);
            }

            _definecode = new KNJDefineSchool();
            _definecode.defineCode(db2, _year);
        }

        private String getStaffName(final DB2UDB db2) {
            String retStr = "";

            final String sql = " SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + _staffCd + "' ";
            //log.debug(" semester sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                	retStr = rs.getString("STAFFNAME");
                }

            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }
    }
}

// eof

