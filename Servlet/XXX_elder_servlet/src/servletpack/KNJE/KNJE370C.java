/*
 * $Id: a19ac5e807889c9e9bbbb121434a545481109661 $
 *
 * 作成日: 2016/12/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJE370C {

    private static final Log log = LogFactory.getLog(KNJE370C.class);

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

    public void printMain(final Vrw32alp svf, final DB2UDB db2) throws SQLException {
        Map schregMap = new HashMap();
        final List printSingakus = getPrintSingaku(db2, schregMap);
        svf.VrSetForm("KNJE370C.frm", 1);
        String befHrClass = "";
        int fieldCnt = 1;
        int maxline = 50;
        Date date = new Date();
        SimpleDateFormat sdf_t = new SimpleDateFormat("HH:mm:ss");
        sdf_t.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
        final String prtTimeStr = sdf_t.format(date);
        String dStr = _param._ctrlDate;
        if (_param._ctrlDate != null) {
            SimpleDateFormat sdf_d = new SimpleDateFormat();
            sdf_d.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
            sdf_d.applyPattern("yy-MM-dd");
            dStr = sdf_d.format(java.sql.Date.valueOf(_param._ctrlDate));
        }


        for (final Iterator it = printSingakus.iterator(); it.hasNext();) {
            final Singaku singaku = (Singaku) it.next();
            if (!befHrClass.equals(singaku._grade + singaku._hrClass) && _hasData) {
                svf.VrEndPage();
                fieldCnt = 1;
            } else if (fieldCnt > maxline) {
                svf.VrEndPage();
                fieldCnt = 1;
            }
            if (fieldCnt == 1) {
                //ヘッダ
                svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度" + _param._semesterName + "　進路指導一覧表");
                svf.VrsOut("SUB_TITLE", "( クラス別 出席番号順 "+ ("1".equals(_param._passOnly) ? "合格者のみ" : "") + ")");
                svf.VrsOut("DATE", "日付 " + dStr);
                svf.VrsOut("TIME", "時刻 " + prtTimeStr);
            }

            //明細
            // #
            svf.VrsOutn("NO", fieldCnt, singaku._renban);
            //出席番号
            svf.VrsOutn("HR_NAME", fieldCnt, singaku._hrName + " " + singaku._attendno + "番");
            //氏名
            svf.VrsOutn("NAME1", fieldCnt, singaku._name);
            AvgHyoutei getWk = (AvgHyoutei)schregMap.get(singaku._schregNo);
            //評定
            svf.VrsOutn("RATE_AVE", fieldCnt, getWk._avg);
            //ランク
            svf.VrsOutn("RANK", fieldCnt, getWk._hyotei);
            //学校名
            svf.VrsOutn("COLLEGE_NAME", fieldCnt, singaku._stat_Abbv);
            //学部名
            svf.VrsOutn("FACULTY_NAME", fieldCnt, singaku._facultyAbbv);
            //学科名
            svf.VrsOutn("DEPARTMENT_NAME", fieldCnt, singaku._departmentAbbv);
            //STS
            svf.VrsOutn("STS", fieldCnt, singaku._acceptanceCriterion);
            //類別
            svf.VrsOutn("DIV", fieldCnt, singaku._howtoexam_Abbv);
            //推薦
            svf.VrsOutn("RECOMMEND", fieldCnt, singaku._recommend_Abbv);
            //合否
            svf.VrsOutn("JUDGE", fieldCnt, singaku._decision_Abbv);
            //入学
            svf.VrsOutn("ENT", fieldCnt, singaku._planstat_Abbv);
            //志望
            svf.VrsOutn("HOPE", fieldCnt, singaku._hope_Val);

            //試験日
            if (!"".equals(singaku._stat_Date1)) {
                final String[] sd1Str = KNJ_EditDate.tate_format4(db2, singaku._stat_Date1);
                final String sd1Wk1Str = sd1Str[2].length() > 1 ? sd1Str[2] : (" " + sd1Str[2]);
                final String sd1Wk2Str = sd1Str[3].length() > 1 ? sd1Str[3] : (" " + sd1Str[3]);
                svf.VrsOutn("EXAM_DATE", fieldCnt, sd1Wk1Str + "/" + sd1Wk2Str);
            }
            //発表日
            if (!"".equals(singaku._stat_Date3)) {
	            final String[] sd3Str = KNJ_EditDate.tate_format4(db2, singaku._stat_Date3);
	            final String sd3Wk1Str = sd3Str[2].length() > 1 ? sd3Str[2] : (" " + sd3Str[2]);
	            final String sd3Wk2Str = sd3Str[3].length() > 1 ? sd3Str[3] : (" " + sd3Str[3]);
	            svf.VrsOutn("PRESENT_DATE", fieldCnt, sd3Wk1Str + "/" + sd3Wk2Str);
            }

            _hasData = true;
            befHrClass = singaku._grade + singaku._hrClass;
            fieldCnt++;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
        return;
    }

    private List getPrintSingaku(final DB2UDB db2, final Map schregMap) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String singakuSql = getSingakuSql();
        log.debug(singakuSql);
        try {
            ps = db2.prepareStatement(singakuSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String schregno = rs.getString("SCHREGNO");
                final String seq = rs.getString("SEQ");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String acceptanceCriterion = rs.getString("ACCEPTANCE_CRITERION_B");
                final String renban = rs.getString("RENBAN");
                final String school_Group = StringUtils.defaultString(rs.getString("SCHOOL_GROUP"), "");
                final String school_Group_Name = StringUtils.defaultString(rs.getString("SCHOOL_GROUP_NAME"),"");
                final String schoolCd = StringUtils.defaultString(rs.getString("SCHOOL_CD"), "");
                final String stat_Abbv = StringUtils.defaultString(rs.getString("STAT_ABBV"), "");
                final String facultycd = StringUtils.defaultString(rs.getString("FACULTYCD"), "");
                final String facultyAbbv = StringUtils.defaultString(rs.getString("FACULTYABBV"), "");
                final String departmentcd = StringUtils.defaultString(rs.getString("DEPARTMENTCD"), "");
                final String departmentAbbv = StringUtils.defaultString(rs.getString("DEPARTMENTABBV"), "");
                final String howtoexam = StringUtils.defaultString(rs.getString("HOWTOEXAM"), "");
                final String howtoexam_Abbv = StringUtils.defaultString(rs.getString("HOWTOEXAM_ABBV"), "");
                final String recommend = StringUtils.defaultString(rs.getString("RECOMMEND"), "");
                final String recommend_Abbv = StringUtils.defaultString(rs.getString("RECOMMEND_ABBV"), "");
                final String decision = StringUtils.defaultString(rs.getString("DECISION"), "");
                final String decision_Abbv = StringUtils.defaultString(rs.getString("DECISION_ABBV"), "");
                final String planstat = StringUtils.defaultString(rs.getString("PLANSTAT"), "");
                final String planstat_Abbv = StringUtils.defaultString(rs.getString("PLANSTAT_ABBV"), "");
                final String hope_Val = StringUtils.defaultString(rs.getString("HOPE_VAL"), "");
                final String stat_Date1 = StringUtils.defaultString(rs.getString("STAT_DATE1"), "");
                final String stat_Date3 = StringUtils.defaultString(rs.getString("STAT_DATE3"), "");
                final Singaku singaku = new Singaku(grade, hrClass, hrName,
                        schregno, seq, attendno, name, acceptanceCriterion, renban, school_Group, school_Group_Name, schoolCd, stat_Abbv,
                        facultycd, facultyAbbv, departmentcd, departmentAbbv, howtoexam, howtoexam_Abbv,
                        recommend, recommend_Abbv, decision, decision_Abbv, planstat, planstat_Abbv, hope_Val,
                        stat_Date1, stat_Date3);
                rtnList.add(singaku);
                if (!schregMap.containsKey(schregno)) {
                    schregMap.put(schregno, new AvgHyoutei());
                }
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        gethyoteiRankData(db2, schregMap);
        return rtnList;
    }
    private void gethyoteiRankData(final DB2UDB db2, final Map schregMap) {
    	
        KNJDefineSchool defineSchool = new KNJDefineSchool();
        defineSchool.defineCode(db2, _param._ctrlYear);
        final KNJE070_1 knje070_1 = new KNJE070_1(db2, (Vrw32alp) null, defineSchool, (String) null);

        boolean isFail = false;
        try {
            for(Iterator ite = schregMap.keySet().iterator();ite.hasNext();) {
                String schregNo = (String)ite.next();
                AvgHyoutei setWk = (AvgHyoutei)schregMap.get(schregNo);

                final List hyoteiHeikinList = knje070_1.getHyoteiHeikinList(schregNo, _param._ctrlYear, _param._ctrlSemester, _param._knje070Paramap);
                for (int i = 0; i < hyoteiHeikinList.size(); i++) {
                	final KNJE070_1.HyoteiHeikin heikin = (KNJE070_1.HyoteiHeikin) hyoteiHeikinList.get(i);
                	if ("TOTAL".equals(heikin.classkey())) {
                		setWk._avg = heikin.avg();
                		setWk._hyotei = heikin.gaihyo();
                		//log.info(" schregNo " + schregNo +" heikin = " + heikin);
                	}
                }
            }
        	
        } catch (Throwable t) {
        	isFail = true;
        	log.warn("KNJE070 failed.", t);
        }
        if (isFail) {
            for(Iterator ite = schregMap.keySet().iterator();ite.hasNext();) {
                String schregNo = (String)ite.next();
                AvgHyoutei setWk = (AvgHyoutei)schregMap.get(schregNo);
                List getList = new ArrayList();
                getStudyRecList(db2, _param, schregNo, getList);
                setStudyRecAvgANDGaihyou(db2, getList, setWk);
                int i = 0;
            }
        }
        knje070_1.pre_stat_f();
    }
    public void getStudyRecList(final DB2UDB db2, final Param param, final String schregno, final List getList) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudyRecSql(param, schregno);
            //log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {

                final String year = rs.getString("YEAR");
                final String fieldNo = rs.getString("FIELD_NO");
                final String gradeName = rs.getString("GRADE_NAME1");
                final String classcd = rs.getString("CLASSCD");
                final String className = rs.getString("CLASSNAME");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String value = rs.getString("VALUE");
                final StudyRec studyRec = new StudyRec(year, fieldNo, gradeName, classcd, className, subclasscd, subclassname, value);

                getList.add(studyRec);
            }
        } catch (SQLException e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return;
    }
    private static String getStudyRecSql(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH YEAR_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     REGD.GRADE, ");
        stb.append("     GDAT.GRADE_NAME1, ");
        stb.append("     MAX(REGD.YEAR) AS YEAR, ");
        stb.append("     ROW_NUMBER() OVER (ORDER BY REGD.GRADE) AS FIELD_NO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
        stb.append("           AND REGD.GRADE = GDAT.GRADE ");
        stb.append("           AND GDAT.SCHOOL_KIND = 'H' ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR <= '" + param._ctrlYear + "' ");
        stb.append("     AND REGD.SCHREGNO = '" + schregno + "' ");
        stb.append(" GROUP BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     GDAT.GRADE_NAME1 ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE ");
        stb.append(" FETCH FIRST 3 ROWS ONLY ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     STUDYREC.YEAR, ");
        stb.append("     YEAR_T.GRADE_NAME1, ");
        stb.append("     YEAR_T.FIELD_NO, ");
        stb.append("     STUDYREC.CLASSCD AS CLASSCD, ");
        stb.append("     STUDYREC.CLASSCD || STUDYREC.SCHOOL_KIND || STUDYREC.CURRICULUM_CD || STUDYREC.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     CASE WHEN STUDYREC.SUBCLASSNAME IS NOT NULL ");
        stb.append("          THEN STUDYREC.SUBCLASSNAME ");
        stb.append("          ELSE VALUE(SUBM.SUBCLASSORDERNAME2, SUBM.SUBCLASSNAME) ");
        stb.append("     END AS SUBCLASSNAME, ");
        stb.append("     CASE WHEN STUDYREC.CLASSNAME IS NOT NULL ");
        stb.append("          THEN STUDYREC.CLASSNAME ");
        stb.append("          ELSE VALUE(CLAM.CLASSORDERNAME2, CLAM.CLASSNAME) ");
        stb.append("     END AS CLASSNAME, ");
        stb.append("     STUDYREC.VALUATION AS VALUE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_STUDYREC_DAT STUDYREC ");
        stb.append("     INNER JOIN YEAR_T ON YEAR_T.YEAR = STUDYREC.YEAR ");
        stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON SUBM.SUBCLASSCD = STUDYREC.SUBCLASSCD ");
        stb.append("          AND SUBM.CLASSCD = STUDYREC.CLASSCD ");
        stb.append("          AND SUBM.SCHOOL_KIND = STUDYREC.SCHOOL_KIND ");
        stb.append("          AND SUBM.CURRICULUM_CD = STUDYREC.CURRICULUM_CD ");
        stb.append("     LEFT JOIN CLASS_MST CLAM ON CLAM.CLASSCD = STUDYREC.CLASSCD ");
        stb.append("          AND CLAM.SCHOOL_KIND = STUDYREC.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("     STUDYREC.YEAR <= '" + param._ctrlYear + "' ");
        stb.append("     AND STUDYREC.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND STUDYREC.CLASSCD < '" + KNJDefineSchool.subject_T + "' ");
        stb.append("     AND STUDYREC.VALUATION IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("     STUDYREC.CLASSCD || STUDYREC.SCHOOL_KIND || STUDYREC.CURRICULUM_CD || STUDYREC.SUBCLASSCD, ");
        stb.append("     STUDYREC.YEAR ");

        return stb.toString();
    }
    private void setStudyRecAvgANDGaihyou(final DB2UDB db2, final List studyRecList, AvgHyoutei setWk) {
    	final List valueList = new ArrayList();
    	for (Iterator itStudyRec = studyRecList.iterator(); itStudyRec.hasNext();) {
    		final StudyRec studyRec = (StudyRec) itStudyRec.next();
    		if (!StringUtils.isEmpty(studyRec._value)) {
    			valueList.add(studyRec._value);
    		}
    	}
    	if (valueList.size() > 0) {
    		setWk._avg = average(valueList);
    		if (NumberUtils.isNumber(setWk._avg)) {
    			final String assessMstSql = getAssessMstSql(setWk._avg);
    			PreparedStatement ps = null;
    			ResultSet rs = null;
    			try {
    				ps = db2.prepareStatement(assessMstSql);
    				rs = ps.executeQuery();
    				while (rs.next()) {
    					setWk._hyotei = rs.getString("ASSESSMARK");
    				}
    			} catch (Exception e) {
    				log.error("Exception", e);
    			} finally {
    				DbUtils.closeQuietly(ps);
    				db2.commit();
    			}
    		}
    	}
    }
    private String getAssessMstSql(final String score) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     ASSESSMARK ");
        stb.append(" FROM ");
        stb.append("     ASSESS_MST ASSESS ");
        stb.append(" WHERE ");
        stb.append("     ASSESS.ASSESSCD = '4' ");
        stb.append("     AND " + score + " BETWEEN ASSESS.ASSESSLOW AND ASSESS.ASSESSHIGH ");

        return stb.toString();
    }
    private String average(final List scoreList) {
        BigDecimal sum = new BigDecimal(0);
        int count = 0;
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final String score = (String) it.next();
            if (NumberUtils.isNumber(score)) {
                sum = sum.add(new BigDecimal(score));
                count += 1;
            }
        }
        //log.info(" scoreList = " + scoreList + ", sum = " + sum + ", count = " + count + ", avg = " + sum.divide(new BigDecimal(count), 5, BigDecimal.ROUND_HALF_UP));
        return count == 0 ? null : sum.divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP).toString();
    }
    /*
     * AvgHyoutei
     */
    private class AvgHyoutei {
        String _avg;
        String _hyotei;
        AvgHyoutei() {
            _avg = new String();
            _hyotei = new String();
        }
    }
    /**
     * StudyRec
     */
    private class StudyRec {
        final String _year;
        final String _fieldNo;
        final String _gradeName;
        final String _classcd;
        final String _className;
        final String _subclasscd;
        final String _subclassname;
        final String _value;
        StudyRec(
                final String year,
                final String fieldNo,
                final String gradeName,
                final String classcd,
                final String className,
                final String subclasscd,
                final String subclassname,
                final String value) {
            _year = year;
            _fieldNo = fieldNo;
            _gradeName = gradeName;
            _classcd = classcd;
            _className = className;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _value = value;
        }
    }

    private String getSingakuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        // 年組
        stb.append("   I2.GRADE, ");
        stb.append("   I2.HR_CLASS, ");
        stb.append("   I3.HR_NAME, ");
        // 出席番号
        stb.append("   I2.ATTENDNO, ");
        stb.append("   T1.SCHREGNO, ");
        // グループ
        stb.append("   T1.SCHOOL_GROUP, ");
        stb.append("   E012.NAME1 AS SCHOOL_GROUP_NAME, ");
        // 連番 ※単純な連番
        stb.append("   ROW_NUMBER() over (ORDER BY I2.GRADE, I2.HR_CLASS, I2.ATTENDNO, T1.SEQ) AS RENBAN, ");
        stb.append("   T1.SEQ, ");
        // 氏名
        stb.append("   I1.NAME, ");
        // 評定平均 ->別口で取得
        // ランク   ->別口で取得
        // S-TS
        stb.append("   C1.ACCEPTANCE_CRITERION_B, ");
        // 受験(予定)学校名
        stb.append("   L1.SCHOOL_CD AS SCHOOL_CD, ");
        stb.append("   L1.SCHOOL_NAME_SHOW1 AS STAT_ABBV, ");
        // 学部名
        stb.append("   T1.FACULTYCD, ");
        stb.append("   L2.FACULTYNAME_SHOW1 AS FACULTYABBV, ");
        // 学科名
        stb.append("   T1.DEPARTMENTCD, ");
        stb.append("   L3.DEPARTMENTNAME_SHOW1 AS DEPARTMENTABBV, ");
        // 類別
        stb.append("   T1.HOWTOEXAM, ");
        stb.append("   E002.ABBV1 AS HOWTOEXAM_ABBV, ");
        // 推薦
        stb.append("   T3.REMARK1 AS RECOMMEND, ");
        stb.append("   E054.ABBV1 as RECOMMEND_ABBV, ");
        // 合否
        stb.append("   T1.DECISION, ");
        stb.append("   E005.ABBV1 as DECISION_ABBV, ");
        // 入学
        stb.append("   T1.PLANSTAT, ");
        stb.append("   E006.ABBV1 as PLANSTAT_ABBV, ");
        // 志望
        stb.append("   T3.REMARK3 AS HOPE_VAL, ");
        // 試験日
        stb.append("   T1.STAT_DATE1, ");
        // 発表日
        stb.append("   T1.STAT_DATE3 ");
        stb.append(" FROM ");
        stb.append("   AFT_GRAD_COURSE_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_BASE_MST I1 ON I1.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT I2 ON I2.SCHREGNO = T1.SCHREGNO AND I2.YEAR = T1.YEAR AND I2.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT I3 ON I3.YEAR = I2.YEAR AND I3.SEMESTER = I2.SEMESTER AND I3.GRADE = I2.GRADE AND I3.HR_CLASS = I2.HR_CLASS ");
        stb.append("   LEFT JOIN COLLEGE_MST L1 ON L1.SCHOOL_CD = T1.STAT_CD ");
        stb.append("   LEFT JOIN COLLEGE_FACULTY_MST L2 ON L2.SCHOOL_CD = T1.STAT_CD AND L2.FACULTYCD = T1.FACULTYCD ");
        stb.append("   LEFT JOIN COLLEGE_DEPARTMENT_MST L3 ON L3.SCHOOL_CD = T1.STAT_CD AND L3.FACULTYCD = T1.FACULTYCD AND L3.DEPARTMENTCD = T1.DEPARTMENTCD ");
        stb.append("   LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = T1.HOWTOEXAM ");
        stb.append("   LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = T1.DECISION ");
        stb.append("   LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = T1.PLANSTAT ");
        stb.append("   LEFT JOIN NAME_MST E012 ON E012.NAMECD1 = 'E012' AND E012.NAMECD2 = T1.SCHOOL_GROUP ");
        stb.append("   LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT T2 ");
        stb.append("        ON T2.YEAR          = T1.YEAR ");
        stb.append("       AND T2.SEQ           = T1.SEQ ");
        stb.append("       AND T2.DETAIL_SEQ    = 1 ");
        stb.append("   LEFT JOIN COLLEGE_EXAM_CALENDAR C1 ");
        stb.append("        ON C1.YEAR          = T1.YEAR ");
        stb.append("       AND C1.SCHOOL_CD     = T1.STAT_CD ");
        stb.append("       AND C1.FACULTYCD     = T1.FACULTYCD ");
        stb.append("       AND C1.DEPARTMENTCD  = T1.DEPARTMENTCD ");
        stb.append("       AND C1.ADVERTISE_DIV = T2.REMARK1 ");
        stb.append("       AND C1.PROGRAM_CD    = T2.REMARK2 ");
        stb.append("       AND C1.FORM_CD       = T2.REMARK3 ");
        stb.append("       AND C1.L_CD1         = T2.REMARK4 ");
        stb.append("       AND C1.S_CD          = T2.REMARK5 ");
        stb.append("   LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT T3 ");
        stb.append("        ON T3.YEAR          = T1.YEAR ");
        stb.append("       AND T3.SEQ           = T1.SEQ ");
        stb.append("       AND T3.DETAIL_SEQ    = 6 ");
        stb.append("   LEFT JOIN NAME_MST E054 ON E054.NAMECD1 = 'E054' AND E054.NAMECD2 = T3.REMARK1 ");
        stb.append("   LEFT JOIN NAME_MST E055 ON E055.NAMECD1 = 'E055' AND E055.NAMECD2 = T3.REMARK2 ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("   AND I2.GRADE || I2.HR_CLASS IN " + SQLUtils.whereIn(true,  _param._categorySelectedIn) + " ");
        if ("1".equals(_param._passOnly)) {
            stb.append("  AND T1.DECISION = '1' ");
        }
        stb.append(" ORDER BY ");
        stb.append("   I2.GRADE, ");
        stb.append("   I2.HR_CLASS, ");
        stb.append("   I2.ATTENDNO, ");
        stb.append("   T1.SEQ ");

        return stb.toString();
    }

    private class Singaku {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _schregNo;
        final String _seq;
        final String _attendno;
        final String _name;
        final String _acceptanceCriterion;
        final String _renban;
        final String _school_Group;
        final String _school_Group_Name;
        final String _schoolCd;
        final String _stat_Abbv;
        final String _facultycd;
        final String _facultyAbbv;
        final String _departmentcd;
        final String _departmentAbbv;
        final String _howtoexam;
        final String _howtoexam_Abbv;
        final String _recommend;
        final String _recommend_Abbv;
        final String _decision;
        final String _decision_Abbv;
        final String _planstat;
        final String _planstat_Abbv;
        final String _hope_Val;
        final String _stat_Date1;
        final String _stat_Date3;

        Singaku(final String grade,
                final String hrClass,
                final String hrName,
                final String schregNo,
                final String seq,
                final String attendno,
                final String name,
                final String acceptanceCriterion,
                final String renban,
                final String school_Group,
                final String school_Group_Name,
                final String schoolCd,
                final String stat_Abbv,
                final String facultycd,
                final String facultyAbbv,
                final String departmentcd,
                final String departmentAbbv,
                final String howtoexam,
                final String howtoexam_Abbv,
                final String recommend,
                final String recommend_Abbv,
                final String decision,
                final String decision_Abbv,
                final String planstat,
                final String planstat_Abbv,
                final String hope_Val,
                final String stat_Date1,
                final String stat_Date3
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _schregNo = schregNo;
            _school_Group = school_Group;
            _school_Group_Name = school_Group_Name;
            _renban = renban;
            _seq = seq;
            _attendno = attendno;
            _name = name;
            _acceptanceCriterion = acceptanceCriterion;
            _schoolCd = schoolCd;
            _stat_Abbv = stat_Abbv;
            _facultycd = facultycd;
            _facultyAbbv = facultyAbbv;
            _departmentcd = departmentcd;
            _departmentAbbv = departmentAbbv;
            _howtoexam = howtoexam;
            _howtoexam_Abbv = howtoexam_Abbv;
            _recommend = recommend;
            _recommend_Abbv = recommend_Abbv;
            _decision = decision;
            _decision_Abbv = decision_Abbv;
            _planstat = planstat;
            _planstat_Abbv = planstat_Abbv;
            _hope_Val = hope_Val;
            _stat_Date1 = stat_Date1;
            _stat_Date3 = stat_Date3;
        }

        /**
         * {@inheritDoc}
         */
        public String getAttendNo() {
            if (null == _attendno) return "  番";
            return String.valueOf(Integer.parseInt(_attendno)) + "番";
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _schregNo + " = " + _name;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
    	final Param param = new Param(db2, request);
        log.fatal("$Revision: 69178 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _semesterName;
        final String _ctrlDate;
        final String[] _categorySelectedIn;

        final String _schoolCd;
        final String _schoolKind;
        final String _disp;
        final String _grade;
        final String _passOnly;

        final String _selectSchoolKind;
        final String _useSchoolKindField;
        final String _useprgschoolkind;
        
        final Map _knje070Paramap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");

            _categorySelectedIn = request.getParameterValues("CLASS_SELECTED");

            _schoolCd = request.getParameter("SCHOOLCD");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _disp = request.getParameter("DISP");
            _grade = request.getParameter("GRADE");
            _passOnly = request.getParameter("PASS_ONLY");

            _selectSchoolKind = request.getParameter("selectSchoolKind");
            _useSchoolKindField = request.getParameter("useSchool_KindField");
            _useprgschoolkind = request.getParameter("use_prg_schoolkind");
            _semesterName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _ctrlSemester + "' "));
            
            _knje070Paramap = new KNJE070().createParamMap(request);
        }
    }
}

// eof

