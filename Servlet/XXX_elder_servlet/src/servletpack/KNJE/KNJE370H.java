/*
 * $Id: 4d80a4c13297b5be4558f6012553b87cdceb469e $
 *
 * 作成日: 2016/12/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJE370H {

    private static final Log log = LogFactory.getLog(KNJE370H.class);
    private static final String paintStr = "PAINT=(2,70,2)";

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

            if ("2".equals(_param._output)) {
                printMain2(svf, db2, "1");
                printMain2(svf, db2, "2");
                printMain2(svf, db2, "3");
            } else {
                printMain1(svf, db2);
            }
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

    private void printMain1(final Vrw32alp svf, final DB2UDB db2) throws SQLException {
        final Map printSingakus = getPrintSingaku1(db2);
        svf.VrSetForm("KNJE370H_1.frm", 4);
        int psCnt = 0;

        //大学の出力
    	////ヘッダ出力
        String ycutwk = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear));
        svf.VrsOut("TITLE", String.valueOf(Integer.parseInt(_param._ctrlYear)+1) + "年度入試（" + ycutwk + "年）結果 ～" + _param._ctrlYear + "年度Ｓ３ 現役生～");
        svf.VrEndRecord();
        svf.VrsOut("HEADER", "1");
        svf.VrEndRecord();
        if (printSingakus.containsKey("1-01")) {
        	final List collegeList = (List)printSingakus.get("1-01");

	        //データ出力
            int getPsCnt[] = {0};
	        printSub1(svf, db2, collegeList, getPsCnt);
	        psCnt += getPsCnt[0];
	        _hasData = true;
        } else {
        	//空行と余白を出力
        	printSub1Nothing(svf, db2);
        }

        //短大の出力
    	////ヘッダ出力
        svf.VrsOut("HEADER", "1");
        svf.VrEndRecord();

        if (printSingakus.containsKey("1-04")) {
        	final List collegeList = (List)printSingakus.get("1-04");
	        //データ出力
            int getPsCnt[] = {0};
	        printSub1(svf, db2, collegeList, getPsCnt);
	        psCnt += getPsCnt[0];
	        _hasData = true;
        } else {
        	//空行と余白を出力
        	printSub1Nothing(svf, db2);
        }

        //専門学校の出力
    	////ヘッダ出力
        svf.VrsOut("HEADER", "1");
        svf.VrEndRecord();

        if (printSingakus.containsKey("1-02")) {
        	final List collegeList = (List)printSingakus.get("1-02");
	        //データ出力
            int getPsCnt[] = {0};
	        printSub1(svf, db2, collegeList, getPsCnt);
	        _hasData = true;
	        psCnt += getPsCnt[0];
        } else {
        	//空行と余白を出力
        	printSub1Nothing(svf, db2);
        }

        final String studentAllCnt = getStudentCountAll(db2);
        final String roninCnt = String.valueOf(Integer.parseInt(studentAllCnt) - psCnt);
	    svf.VrsOut("NOTE", _param._ctrlYear+"年度 浪人ほか" + roninCnt + "名");
        svf.VrEndRecord();
	    svf.VrsOut("NOTE", _param._ctrlYear+"年度 " + studentAllCnt + "名");
        svf.VrEndRecord();
        svf.VrsOut("BLANK", "a");
        svf.VrEndRecord();

        //過年度生の出力
    	////ヘッダ出力
        svf.VrsOut("TITLE", String.valueOf(Integer.parseInt(_param._ctrlYear)+1) + "年度入試（" + ycutwk + "年）結果 ～過年度生～");
        svf.VrEndRecord();
        svf.VrsOut("HEADER", "1");
        svf.VrEndRecord();

        if (printSingakus.containsKey("2")) {
        	final List collegeList = (List)printSingakus.get("2");
	        //データ出力
            int getPsCnt[] = {0};
	        printSub1(svf, db2, collegeList, getPsCnt);
	        _hasData = true;
        } else {
        	//空行と余白を出力
        	printSub1Nothing(svf, db2);
        }

        prtRemark(svf);

        if (_hasData) {
            svf.VrEndPage();
        }
        return;
    }

    private void prtRemark(final Vrw32alp svf) {
	    svf.VrsOut("NOTE", "※ コード：ベネッセ大学コード");
        svf.VrEndRecord();
        svf.VrsOut("BLANK", "a");
        svf.VrEndRecord();
        svf.VrsOut("NOTE", "※ 県コード：11埼玉、12千葉、13東京、14神奈川ほか");
        svf.VrEndRecord();
        svf.VrsOut("BLANK", "a");
        svf.VrEndRecord();
        svf.VrsOut("NOTE", "※ ss avg：その大学の全学部・全学科のベネッセ判定基準Ｂの平均");
        svf.VrEndRecord();
        svf.VrsOut("BLANK", "a");
        svf.VrEndRecord();
        svf.VrsOut("NOTE", "※ ベネッセ判定基準Ｂ：高３第３回ベネッセ・駿台マーク模試（１１月）における合格可能性６０％～８０％の偏差値");
        svf.VrEndRecord();
        svf.VrsOut("BLANK", "a");
        svf.VrEndRecord();
        svf.VrsOut("NOTE", "※ 一般：一般入試（センター、指定校、公募、ＡＯ・自己を除く）");
        svf.VrEndRecord();
        svf.VrsOut("BLANK", "a");
        svf.VrEndRecord();
        svf.VrsOut("NOTE", "※ センター：私大・短大でセンター試験を利用する入試（単独・併用含む）");
        svf.VrEndRecord();
        svf.VrsOut("BLANK", "a");
        svf.VrEndRecord();
        svf.VrsOut("NOTE", "※ 指定校：指定校推薦入試");
        svf.VrEndRecord();
        svf.VrsOut("BLANK", "a");
        svf.VrEndRecord();
        svf.VrsOut("NOTE", "※ 公募：公募推薦入試、全国キリスト者推薦入試、キリスト教学校教育同盟加盟校推薦入試");
        svf.VrEndRecord();
        svf.VrsOut("BLANK", "a");
        svf.VrEndRecord();
        svf.VrsOut("NOTE", "※ ＡＯ・自己：ＡＯ入試、自己推薦入試");
        svf.VrEndRecord();
        svf.VrsOut("BLANK", "a");
        svf.VrEndRecord();
    }

    private String getStudentCountAll(final DB2UDB db2) {
    	String retStr = "";
    	final String qry = getStudentCountSql("0");
    	retStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, qry));
    	return StringUtils.defaultString(retStr, "0");

    }
    // private String getStudentCountPass(final DB2UDB db2) {
    // 	String retStr = "";
    // 	final String qry = getStudentCountSql("1");
    // 	retStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, qry));
    // 	return StringUtils.defaultString(retStr, "0");
    //}
    private String getStudentCountSql(final String psFlg) {
    	StringBuffer stb = new StringBuffer();
    	stb.append(" WITH SCHREG_BASE AS ( ");
    	stb.append("  SELECT DISTINCT ");
    	stb.append("    SCHREGNO ");
    	stb.append("  FROM ");
    	stb.append("    AFT_GRAD_COURSE_DAT ");
    	stb.append("  WHERE ");
    	stb.append("    YEAR = '" + _param._ctrlYear + "' ");
    	stb.append("    AND SENKOU_KIND = '0' ");
    	if ("1".equals(psFlg)) {
    	    stb.append("    AND DECISION = '3' ");
    	    stb.append("    AND PLANSTAT = '1' ");
    	}
    	stb.append(" ) ");
    	stb.append(" SELECT ");
    	stb.append("   COUNT(*) ");
    	stb.append(" FROM ");
    	stb.append("   SCHREG_BASE ");
    	return stb.toString();
    }

    private void printSub1Nothing(final Vrw32alp svf, final DB2UDB db2) {
        svf.VrsOut("NO", " ");
        svf.VrsOut("COLLEGE_CD", "");
        svf.VrsOut("COLLEGE_NAME", "");
        svf.VrsOut("PREF_CD", "");
        svf.VrsOut("SS_AVG", "");
        svf.VrsOut("PASS", " ");
        svf.VrAttribute("PASS", paintStr);
        svf.VrsOut("PASS_GENERAL", "");
        svf.VrsOut("PASS_CENTER", "");
        svf.VrsOut("PASS_DESIGN", "");
        svf.VrsOut("PASS_RECOMMEND", "");
        svf.VrsOut("PASS_AO", "");
        svf.VrsOut("ENT", " ");
        svf.VrAttribute("ENT", paintStr);
        svf.VrsOut("ENTS_GENERAL", "");
        svf.VrsOut("ENT_CENTER", "");
        svf.VrsOut("ENT_DESIGN", "");
        svf.VrsOut("ENT_RECOMMEND", "");
        svf.VrsOut("ENT_AO", "");

        //合計出力
        svf.VrsOut("TOTAL_NAME", "** 合計 **");
        svf.VrsOut("TOTAL_PASS", "0");
        svf.VrAttribute("TOTAL_PASS", paintStr);
        svf.VrsOut("TOTAL_PASS_GENERAL", "0");
        svf.VrsOut("TOTAL_PASS_CENTER", "0");
        svf.VrsOut("TOTAL_PASS_DESIGN", "0");
        svf.VrsOut("TOTAL_PASS_RECOMMEND", "0");
        svf.VrsOut("TOTAL_PASS_AO", "0");
        svf.VrsOut("TOTAL_ENT", "0");
        svf.VrAttribute("TOTAL_ENT", paintStr);
        svf.VrsOut("TOTAL_ENTS_GENERAL", "0");
        svf.VrsOut("TOTAL_ENT_CENTER", "0");
        svf.VrsOut("TOTAL_ENT_DESIGN", "0");
        svf.VrsOut("TOTAL_ENT_RECOMMEND", "0");
        svf.VrsOut("TOTAL_ENT_AO", "0");
        svf.VrEndRecord();

        //余白出力
        svf.VrsOut("BLANK", "ab");
        svf.VrEndRecord();
    }

    private void printSub1(final Vrw32alp svf, final DB2UDB db2, final List prtList, int[] psCnt) throws SQLException {
        int recCnt = 1;
        int total_Decision = 0;
        int total_Dec_General = 0;
        int total_Dec_Center = 0;
        int total_Dec_Design = 0;
        int total_Dec_Recommend = 0;
        int total_Dec_AO = 0;
        int total_Planstat = 0;
        int total_Plan_General = 0;
        int total_Plan_Center = 0;
        int total_Plan_Design = 0;
        int total_Plan_Recommend = 0;
        int total_Plan_AO = 0;
    	//データ出力
        for (Iterator ite = prtList.iterator();ite.hasNext();) {
        	SummaryData outwk = (SummaryData)ite.next();
            svf.VrsOut("NO", String.valueOf(recCnt));
	        svf.VrsOut("COLLEGE_CD", outwk._school_Cd);
	        svf.VrsOut("COLLEGE_NAME", outwk._school_Name);
	        svf.VrsOut("PREF_CD", outwk._pref_Cd);
	        svf.VrsOut("SS_AVG", outwk._ssAvg);
	        if (!"".equals(StringUtils.defaultString(outwk._decision_Cnt))) {
                svf.VrsOut("PASS", outwk._decision_Cnt);
	            total_Decision += Integer.parseInt(outwk._decision_Cnt);
	        }
	        svf.VrAttribute("PASS", paintStr);
	        if (!"".equals(StringUtils.defaultString(outwk._decision_NormalCnt))) {
	            svf.VrsOut("PASS_GENERAL", outwk._decision_NormalCnt);
	            total_Dec_General += Integer.parseInt(outwk._decision_NormalCnt);
	        }
	        if (!"".equals(StringUtils.defaultString(outwk._decision_CenterCnt))) {
	            svf.VrsOut("PASS_CENTER", outwk._decision_CenterCnt);
		        total_Dec_Center += Integer.parseInt(outwk._decision_CenterCnt);
	        }
	        if (!"".equals(StringUtils.defaultString(outwk._decision_SpecCnt))) {
	            svf.VrsOut("PASS_DESIGN", outwk._decision_SpecCnt);
		        total_Dec_Design += Integer.parseInt(outwk._decision_SpecCnt);
	        }
	        if (!"".equals(StringUtils.defaultString(outwk._decision_KouboCnt))) {
	            svf.VrsOut("PASS_RECOMMEND", outwk._decision_KouboCnt);
		        total_Dec_Recommend += Integer.parseInt(outwk._decision_KouboCnt);
	        }
	        if (!"".equals(StringUtils.defaultString(outwk._decision_AoCnt))) {
	            svf.VrsOut("PASS_AO", outwk._decision_AoCnt);
		        total_Dec_AO += Integer.parseInt(outwk._decision_AoCnt);
	        }
	        if (!"".equals(StringUtils.defaultString(outwk._planstat_Cnt))) {
	            svf.VrsOut("ENT", outwk._planstat_Cnt);
		        total_Planstat += Integer.parseInt(outwk._planstat_Cnt);
	        }
	        svf.VrAttribute("ENT", paintStr);
	        if (!"".equals(StringUtils.defaultString(outwk._planstat_NormalCnt))) {
	            svf.VrsOut("ENTS_GENERAL", outwk._planstat_NormalCnt);
		        total_Plan_General += Integer.parseInt(outwk._planstat_NormalCnt);
	        }
	        if (!"".equals(StringUtils.defaultString(outwk._planstat_CenterCnt))) {
	            svf.VrsOut("ENT_CENTER", outwk._planstat_CenterCnt);
		        total_Plan_Center += Integer.parseInt(outwk._planstat_CenterCnt);
	        }
	        if (!"".equals(StringUtils.defaultString(outwk._planstat_SpecCnt))) {
	            svf.VrsOut("ENT_DESIGN", outwk._planstat_SpecCnt);
		        total_Plan_Design += Integer.parseInt(outwk._planstat_SpecCnt);
	        }
	        if (!"".equals(StringUtils.defaultString(outwk._planstat_KouboCnt))) {
	            svf.VrsOut("ENT_RECOMMEND", outwk._planstat_KouboCnt);
		        total_Plan_Recommend += Integer.parseInt(outwk._planstat_KouboCnt);
	        }
	        if (!"".equals(StringUtils.defaultString(outwk._planstat_AoCnt))) {
	            svf.VrsOut("ENT_AO", outwk._planstat_AoCnt);
		        total_Plan_AO += Integer.parseInt(outwk._planstat_AoCnt);
	        }

	        svf.VrEndRecord();
	        recCnt++;
        }

        //合計出力
        svf.VrsOut("TOTAL_NAME", "** 合計 **");
        svf.VrsOut("TOTAL_PASS", String.valueOf(total_Decision));
        svf.VrAttribute("TOTAL_PASS", paintStr);
        svf.VrsOut("TOTAL_PASS_GENERAL", String.valueOf(total_Dec_General));
        svf.VrsOut("TOTAL_PASS_CENTER", String.valueOf(total_Dec_Center));
        svf.VrsOut("TOTAL_PASS_DESIGN", String.valueOf(total_Dec_Design));
        svf.VrsOut("TOTAL_PASS_RECOMMEND", String.valueOf(total_Dec_Recommend));
        svf.VrsOut("TOTAL_PASS_AO", String.valueOf(total_Dec_AO));
        svf.VrsOut("TOTAL_ENT", String.valueOf(total_Planstat));
        psCnt[0] = total_Planstat;
        svf.VrAttribute("TOTAL_ENT", paintStr);
        svf.VrsOut("TOTAL_ENTS_GENERAL", String.valueOf(total_Plan_General));
        svf.VrsOut("TOTAL_ENT_CENTER", String.valueOf(total_Plan_Center));
        svf.VrsOut("TOTAL_ENT_DESIGN", String.valueOf(total_Plan_Design));
        svf.VrsOut("TOTAL_ENT_RECOMMEND", String.valueOf(total_Plan_Recommend));
        svf.VrsOut("TOTAL_ENT_AO", String.valueOf(total_Plan_AO));
        svf.VrEndRecord();

        //余白出力
        svf.VrsOut("BLANK", "ab");
        svf.VrEndRecord();
    }
    private Map getPrintSingaku1(final DB2UDB db2) throws SQLException {
        final Map rtnMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String singakuSql = getSingakuSql1();
        List addList = null;
        log.debug(singakuSql);
        try {
            ps = db2.prepareStatement(singakuSql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String oldschregtype = rs.getString("OLDSCHREGTYPE");
            	final String school_Group = rs.getString("SCHOOL_GROUP");
            	final String school_Category_Cd = rs.getString("SCHOOL_CATEGORY_CD");
            	final String school_Cd = rs.getString("STAT_CD");
            	final String school_Name = rs.getString("SCHOOL_NAME");
            	final String pref_Cd = rs.getString("PREF_CD");
            	final String ssAvg = rs.getString("SSAVG");
            	final String decision_Cnt = rs.getString("DECISION_CNT");
            	final String decision_NormalCnt = rs.getString("DECISION_NORMALCNT");
            	final String decision_CenterCnt = rs.getString("DECISION_CENTERCNT");
            	final String decision_SpecCnt = rs.getString("DECISION_SPECCNT");
            	final String decision_KouboCnt = rs.getString("DECISION_KOUBOCNT");
            	final String decision_AoCnt = rs.getString("DECISION_AOCNT");
            	final String planstat_Cnt = rs.getString("PLANSTAT_CNT");
            	final String planstat_NormalCnt = rs.getString("PLANSTAT_NORMALCNT");
            	final String planstat_CenterCnt = rs.getString("PLANSTAT_CENTERCNT");
            	final String planstat_SpecCnt = rs.getString("PLANSTAT_SPECCNT");
            	final String planstat_KouboCnt = rs.getString("PLANSTAT_KOUBOCNT");
            	final String planstat_AoCnt = rs.getString("PLANSTAT_AOCNT");
                final SummaryData sData = new SummaryData(oldschregtype, school_Group, school_Category_Cd, school_Cd,
                		                        school_Name, pref_Cd, ssAvg, decision_Cnt, decision_NormalCnt,
                		                        decision_CenterCnt,decision_SpecCnt, decision_KouboCnt, decision_AoCnt,
                		                        planstat_Cnt, planstat_NormalCnt, planstat_CenterCnt, planstat_SpecCnt,
                		                        planstat_KouboCnt, planstat_AoCnt);
                final String chkKey;
                if ("2".equals(oldschregtype)) {
                	chkKey = oldschregtype;
                } else {
                	chkKey = oldschregtype + "-" + school_Group;
                }
                if (!rtnMap.containsKey(chkKey)) {
                	addList = new ArrayList();
                	rtnMap.put(chkKey, addList);
                } else {
                	addList = (List)rtnMap.get(chkKey);
                }
                addList.add(sData);
            }
        } catch(Exception e) {
            log.error("getPrintSingaku1 Exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtnMap;
    }

    private String getSingakuSql1() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CALC_BASE AS ( ");
        stb.append(" select ");
        stb.append("   YEAR, ");
        stb.append("   SCHOOL_CD, ");
        stb.append("   sum(double(ACCEPTANCE_CRITERION_B)) / count(ACCEPTANCE_CRITERION_B) AS CRITERION_B, ");
        stb.append("   count(ACCEPTANCE_CRITERION_B) AS CNT ");
        stb.append(" from ");
        stb.append("   COLLEGE_EXAM_CALENDAR ");
        stb.append(" GROUP BY ");
        stb.append("   YEAR, ");
        stb.append("   SCHOOL_CD ");
        stb.append(" ), CALC_SND AS ( ");
        stb.append(" SELECT ");
        stb.append("   YEAR, ");
        stb.append("   SCHOOL_CD, ");
        stb.append("   decimal(int(CRITERION_B * 10 + 0.5)/10.0, 5,1) AS CRITERION_B, "); //四捨五入
        stb.append("   CNT ");
        stb.append(" FROM ");
        stb.append("   CALC_BASE ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   '1' AS OLDSCHREGTYPE, ");
        stb.append("   M1.SCHOOL_GROUP, ");
        stb.append("   M2.SCHOOL_CATEGORY_CD, ");
        stb.append("   T1.STAT_CD, ");
        stb.append("   M1.SCHOOL_NAME, ");
        stb.append("   T1.PREF_CD, ");
        stb.append("   sum(CS1.CRITERION_B) AS SSAVG, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' THEN 1 ELSE NULL END) AS DECISION_CNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.JUKEN_HOWTO = '1' THEN 1 ELSE NULL END) AS DECISION_NORMALCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.JUKEN_HOWTO = '2' THEN 1 ELSE NULL END) AS DECISION_CENTERCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.JUKEN_HOWTO = '3' THEN 1 ELSE NULL END) AS DECISION_SPECCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.JUKEN_HOWTO = '4' THEN 1 ELSE NULL END) AS DECISION_KOUBOCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.JUKEN_HOWTO = '5' THEN 1 ELSE NULL END) DECISION_AOCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.PLANSTAT = '1' THEN 1 ELSE NULL END) AS PLANSTAT_CNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.PLANSTAT = '1' AND T1.JUKEN_HOWTO = '1' THEN 1 ELSE NULL END) AS PLANSTAT_NORMALCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.PLANSTAT = '1' AND T1.JUKEN_HOWTO = '2' THEN 1 ELSE NULL END) AS PLANSTAT_CENTERCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.PLANSTAT = '1' AND T1.JUKEN_HOWTO = '3' THEN 1 ELSE NULL END) AS PLANSTAT_SPECCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.PLANSTAT = '1' AND T1.JUKEN_HOWTO = '4' THEN 1 ELSE NULL END) AS PLANSTAT_KOUBOCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.PLANSTAT = '1' AND T1.JUKEN_HOWTO = '5' THEN 1 ELSE NULL END) PLANSTAT_AOCNT ");
        stb.append(" FROM ");
        stb.append("   AFT_GRAD_COURSE_DAT T1 ");
        stb.append("   LEFT JOIN COLLEGE_MST M1 ON M1.SCHOOL_CD = T1.STAT_CD ");
        stb.append("   LEFT JOIN COLLEGE_FACULTY_SYSTEM_MST M2 ");
        stb.append("     ON M2.SCHOOL_CD = T1.STAT_CD ");
        stb.append("    AND M2.FACULTYCD = T1.FACULTYCD ");
        stb.append("   LEFT JOIN CALC_SND CS1 ON CS1.YEAR = T1.YEAR AND CS1.SCHOOL_CD = T1.STAT_CD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR='" + _param._ctrlYear + "' ");
        stb.append("   AND T1.SENKOU_KIND = '0' ");
        stb.append("   AND T1.SCHREGNO IN (SELECT distinct SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '" + _param._ctrlYear + "') ");
        stb.append(" GROUP BY ");
        stb.append("   M1.SCHOOL_GROUP, ");
        stb.append("   M2.SCHOOL_CATEGORY_CD, ");
        stb.append("   T1.STAT_CD, ");
        stb.append("   M1.SCHOOL_NAME, ");
        stb.append("   T1.PREF_CD ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("   '2' AS OLDSCHREGTYPE, ");
        stb.append("   M1.SCHOOL_GROUP, ");
        stb.append("   M2.SCHOOL_CATEGORY_CD, ");
        stb.append("   T1.STAT_CD, ");
        stb.append("   M1.SCHOOL_NAME, ");
        stb.append("   T1.PREF_CD, ");
        stb.append("   sum(CS2.CRITERION_B) AS SSAVG, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' THEN 1 ELSE NULL END) AS DECISION_CNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.HOWTOEXAM = '1' THEN 1 ELSE NULL END) AS DECISION_NORMALCNT,");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.HOWTOEXAM = '2' THEN 1 ELSE NULL END) AS DECISION_CENTERCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.HOWTOEXAM = '3' THEN 1 ELSE NULL END) AS DECISION_SPECCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.HOWTOEXAM = '4' THEN 1 ELSE NULL END) AS DECISION_KOUBOCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.HOWTOEXAM = '5' THEN 1 ELSE NULL END) DECISION_AOCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.PLANSTAT = '1' THEN 1 ELSE NULL END) AS PLANSTAT_CNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.PLANSTAT = '1' AND T1.HOWTOEXAM = '1' THEN 1 ELSE NULL END) AS PLANSTAT_NORMALCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.PLANSTAT = '1' AND T1.HOWTOEXAM = '2' THEN 1 ELSE NULL END) AS PLANSTAT_CENTERCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.PLANSTAT = '1' AND T1.HOWTOEXAM = '3' THEN 1 ELSE NULL END) AS PLANSTAT_SPECCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.PLANSTAT = '1' AND T1.HOWTOEXAM = '4' THEN 1 ELSE NULL END) AS PLANSTAT_KOUBOCNT, ");
        stb.append("   sum(CASE WHEN T1.DECISION = '3' AND T1.PLANSTAT = '1' AND T1.HOWTOEXAM = '5' THEN 1 ELSE NULL END) PLANSTAT_AOCNT ");
        stb.append(" FROM ");
        stb.append("   AFT_GRAD_COURSE_DAT T1 ");
        stb.append("   LEFT JOIN COLLEGE_MST M1 ON M1.SCHOOL_CD = T1.STAT_CD ");
        stb.append("   LEFT JOIN COLLEGE_FACULTY_SYSTEM_MST M2 ");
        stb.append("     ON M2.SCHOOL_CD = T1.STAT_CD ");
        stb.append("    AND M2.FACULTYCD = T1.FACULTYCD ");
        stb.append("   LEFT JOIN CALC_SND CS2 ON CS2.YEAR = T1.YEAR AND CS2.SCHOOL_CD = T1.STAT_CD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR='" + _param._ctrlYear + "' ");
        stb.append("   AND T1.SENKOU_KIND = '0' ");
        stb.append("   AND T1.SCHREGNO NOT IN (SELECT distinct SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '" + _param._ctrlYear + "') ");
        stb.append(" GROUP BY ");
        stb.append("   M1.SCHOOL_GROUP, ");
        stb.append("   M2.SCHOOL_CATEGORY_CD, ");
        stb.append("   T1.STAT_CD, ");
        stb.append("   M1.SCHOOL_NAME, ");
        stb.append("   T1.PREF_CD ");
        stb.append(" ORDER BY ");
        stb.append("   OLDSCHREGTYPE, ");
        stb.append("   SCHOOL_GROUP, ");
        stb.append("   PREF_CD ");

        return stb.toString();
    }

    public void printMain2(final Vrw32alp svf, final DB2UDB db2, final String schregFilterFlg) throws SQLException {
        final Map printSingakus = getPrintSingaku2(db2, schregFilterFlg);
        svf.VrSetForm("KNJE370H_2.frm", 4);

        String ttlStr = "";
        if ("1".equals(schregFilterFlg)) {
        	ttlStr = "現役生合格数(抜粋) " + _param._ctrlDate.replace('/', '.').replace('-', '.') + "現在";
        } else if ("2".equals(schregFilterFlg)) {
        	ttlStr = "過年度生合格数(抜粋) " + _param._ctrlDate.replace('/', '.').replace('-', '.') + "現在";
        } else {
        	ttlStr = "現役・過年度生合格数(抜粋) " + _param._ctrlDate.replace('/', '.').replace('-', '.') + "現在";
        }
        svf.VrsOut("TITLE", ttlStr);
        final String n1Str = String.valueOf(Integer.parseInt(_param._ctrlYear)+1);
        final String n2Str = _param._ctrlYear;
        final String n3Str = String.valueOf(Integer.parseInt(_param._ctrlYear)-1);
        final String n4Str = String.valueOf(Integer.parseInt(_param._ctrlYear)-2);
        final String n5Str = String.valueOf(Integer.parseInt(_param._ctrlYear)-3);
        svf.VrsOut("NENDO1", n1Str + "(" + KNJ_EditDate.gengou(db2, Integer.parseInt(n1Str)) + ")年");
        svf.VrsOut("NENDO1_2", n1Str + "(" + KNJ_EditDate.gengou(db2, Integer.parseInt(n1Str)) + ")年");
        svf.VrsOut("NENDO2", n2Str + "(" + KNJ_EditDate.gengou(db2, Integer.parseInt(n2Str)) + ")年");
        svf.VrsOut("NENDO2_2", n2Str + "(" + KNJ_EditDate.gengou(db2, Integer.parseInt(n2Str)) + ")年");
        svf.VrsOut("NENDO3", n3Str + "(" + KNJ_EditDate.gengou(db2, Integer.parseInt(n3Str)) + ")年");
        svf.VrsOut("NENDO3_2", n3Str + "(" + KNJ_EditDate.gengou(db2, Integer.parseInt(n3Str)) + ")年");
        svf.VrsOut("NENDO4", n4Str + "(" + KNJ_EditDate.gengou(db2, Integer.parseInt(n4Str)) + ")年");
        svf.VrsOut("NENDO4_2", n4Str + "(" + KNJ_EditDate.gengou(db2, Integer.parseInt(n4Str)) + ")年");
        svf.VrsOut("NENDO5", n5Str + "(" + KNJ_EditDate.gengou(db2, Integer.parseInt(n5Str)) + ")年");
        svf.VrsOut("NENDO5_2", n5Str + "(" + KNJ_EditDate.gengou(db2, Integer.parseInt(n5Str)) + ")年");

        int grpCnt = 0;
        int lDatCnt = 0;
        for (Iterator its = printSingakus.keySet().iterator();its.hasNext();) {
        	final String keyStr = (String)its.next();
        	List outList = (List)printSingakus.get(keyStr);
            printSub2(svf, db2, outList);
            lDatCnt += outList.size();
            grpCnt++;
        }
        //利用出力ラインから余白算出
        int outMaxLine = 58 * 2;
        int yohakuCnt = (lDatCnt + (grpCnt * 2)) % (outMaxLine);
        for (int nn = 0;nn < outMaxLine - yohakuCnt;nn++) {
//        if (0 < outMaxLine - yohakuCnt) {
            svf.VrsOut("BLANK", "1");
            svf.VrEndRecord();
        }

        if (_hasData) {
            svf.VrEndPage();
        }
        return;
    }

    private void printSub2(final Vrw32alp svf, final DB2UDB db2, final List prtList) throws SQLException {

    	long totalPass1 = 0;
    	long totalPass2 = 0;
    	long totalPass3 = 0;
    	long totalPass4 = 0;
    	long totalPass5 = 0;
        //合計算出
        for (Iterator itc = prtList.iterator();itc.hasNext();) {
        	SummaryData2 outwk = (SummaryData2)itc.next();
        	if (outwk != null) {
        		if (!"".equals(StringUtils.defaultString(outwk._nowyearcnt))) {
        	        totalPass1 += Integer.parseInt(outwk._nowyearcnt);
        		}
        		if (!"".equals(StringUtils.defaultString(outwk._oneyearcnt))) {
        	        totalPass2 += Integer.parseInt(outwk._oneyearcnt);
        		}
        		if (!"".equals(StringUtils.defaultString(outwk._twoyearcnt))) {
        	        totalPass3 += Integer.parseInt(outwk._twoyearcnt);
        		}
        		if (!"".equals(StringUtils.defaultString(outwk._threeyearcnt))) {
        	        totalPass4 += Integer.parseInt(outwk._threeyearcnt);
        		}
        		if (!"".equals(StringUtils.defaultString(outwk._fouryearcnt))) {
        	        totalPass5 += Integer.parseInt(outwk._fouryearcnt);
        		}
        	}
        }

        int recCnt = 1;
        //データ行出力
        for (Iterator ite = prtList.iterator();ite.hasNext();) {
        	SummaryData2 outwk = (SummaryData2)ite.next();
        	if (recCnt == 1) {
            	//グループ行出力
                svf.VrsOut("GRPL_NAME", "■" + outwk._college_Grp_Name);
                svf.VrAttribute("GRPL_NAME",paintStr );
                svf.VrsOut("TOTAL_PASS1", String.valueOf(totalPass1));
                svf.VrAttribute("TOTAL_PASS1", paintStr);
                svf.VrsOut("TOTAL_PASS2", String.valueOf(totalPass2));
                svf.VrAttribute("TOTAL_PASS2", paintStr);
                svf.VrsOut("TOTAL_PASS3", String.valueOf(totalPass3));
                svf.VrAttribute("TOTAL_PASS3", paintStr);
                svf.VrsOut("TOTAL_PASS4", String.valueOf(totalPass4));
                svf.VrAttribute("TOTAL_PASS4", paintStr);
                svf.VrsOut("TOTAL_PASS5", String.valueOf(totalPass5));
                svf.VrAttribute("TOTAL_PASS5", paintStr);
                //svf.VrEndRecord();
        	}
            svf.VrsOut("COLLEGE_CD", outwk._school_Cd);
            svf.VrsOut("COLLEGE_NAME", outwk._school_Name);
            svf.VrsOut("SS_AVG", outwk._acceptance_Criterion_B);
            svf.VrsOut("PASS1", outwk._nowyearcnt);
            svf.VrsOut("PASS2", outwk._oneyearcnt);
            svf.VrsOut("PASS3", outwk._twoyearcnt);
            svf.VrsOut("PASS4", outwk._threeyearcnt);
            svf.VrsOut("PASS5", outwk._fouryearcnt);
            svf.VrEndRecord();
            _hasData = true;
            recCnt++;
        }
    }

    private Map getPrintSingaku2(final DB2UDB db2, final String schregFilterFlg) throws SQLException {
        final Map rtnMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String singakuSql = getSingakuSql2(schregFilterFlg);
        List addList = null;
        log.debug(singakuSql);
        try {

            ps = db2.prepareStatement(singakuSql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String school_Group = rs.getString("COLLEGE_GRP_CD");
            	final String school_Cd = rs.getString("SCHOOL_CD");
            	final String school_Name = rs.getString("SCHOOL_NAME");
            	final String college_Grp_Name = rs.getString("COLLEGE_GRP_NAME");
            	final String acceptance_Criterion_B = rs.getString("ACCEPTANCE_CRITERION_B");
            	final String nowyearcnt = rs.getString("NOWYEARCNT");
            	final String oneyearcnt = rs.getString("ONEYEARCNT");
            	final String twoyearcnt = rs.getString("TWOYEARCNT");
            	final String threeyearcnt = rs.getString("THREEYEARCNT");
            	final String fouryearcnt = rs.getString("FOURYEARCNT");
            	final String chkKey = school_Group;
            	if (!rtnMap.containsKey(chkKey)) {
            		addList = new ArrayList();
            		rtnMap.put(chkKey, addList);
            	} else {
            		addList = (List)rtnMap.get(chkKey);
            	}
            	SummaryData2 addwk = new SummaryData2(school_Group, school_Cd, school_Name, college_Grp_Name, acceptance_Criterion_B, nowyearcnt, oneyearcnt, twoyearcnt, threeyearcnt, fouryearcnt);
            	addList.add(addwk);
            }
        } catch(Exception e) {
            log.error("getPrintSingaku2 Exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtnMap;
    }

    private String getSingakuSql2(final String schregFilterFlg) {
    	String pastStartYear = "";
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH AFT_GRAD_CDAT AS (");
        stb.append("   SELECT ");
        stb.append("     T1.* ");
        stb.append("   FROM ");
        stb.append("     AFT_GRAD_COURSE_DAT T1 ");
        stb.append("   WHERE ");
    	pastStartYear = String.valueOf(Integer.parseInt(_param._ctrlYear) - 4);
        stb.append("       T1.YEAR BETWEEN '" + pastStartYear + "' AND '" + _param._ctrlYear + "' ");
        stb.append("       AND T1.SENKOU_KIND = '0' ");
        stb.append("       AND T1.DECISION = '3' ");
        if ("1".equals(schregFilterFlg)) {
        	stb.append("   AND T1.SCHREGNO IN (SELECT DISTINCT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '" + _param._ctrlYear + "')");
        } else if ("2".equals(schregFilterFlg)) {
        	stb.append("   AND T1.SCHREGNO NOT IN (SELECT DISTINCT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '" + _param._ctrlYear + "')");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     M2.COLLEGE_GRP_CD, ");
        stb.append("     M1.SCHOOL_CD, ");
        stb.append("     M1.SCHOOL_NAME, ");
        stb.append("     M2.COLLEGE_GRP_NAME, ");
        stb.append("     C1.ACCEPTANCE_CRITERION_B, ");
        stb.append("     sum(CASE WHEN T1.YEAR = '" + _param._ctrlYear + "' THEN 1 ELSE NULL END) AS NOWYEARCNT, ");
    	pastStartYear = String.valueOf(Integer.parseInt(_param._ctrlYear) - 1);
        stb.append("     sum(CASE WHEN T1.YEAR = '" + pastStartYear + "' THEN 1 ELSE NULL END) AS ONEYEARCNT, ");
    	pastStartYear = String.valueOf(Integer.parseInt(_param._ctrlYear) - 2);
        stb.append("     sum(CASE WHEN T1.YEAR = '" + pastStartYear + "' THEN 1 ELSE NULL END) AS TWOYEARCNT, ");
    	pastStartYear = String.valueOf(Integer.parseInt(_param._ctrlYear) - 3);
        stb.append("     sum(CASE WHEN T1.YEAR = '" + pastStartYear + "' THEN 1 ELSE NULL END) AS THREEYEARCNT, ");
    	pastStartYear = String.valueOf(Integer.parseInt(_param._ctrlYear) - 4);
        stb.append("     sum(CASE WHEN T1.YEAR = '" + pastStartYear + "' THEN 1 ELSE NULL END) AS FOURYEARCNT ");
        stb.append(" FROM ");
        stb.append("    COLLEGE_MST M1 ");
        stb.append("    LEFT JOIN AFT_COLLEGE_GROUP_DAT D1 ");
        stb.append("      ON D1.YEAR = '2006' ");
        stb.append("     AND D1.SCHOOL_CD = M1.SCHOOL_CD ");
        stb.append("    LEFT JOIN AFT_COLLEGE_GROUP_MST M2 ");
        stb.append("      ON M2.YEAR = D1.YEAR ");
        stb.append("     AND M2.COLLEGE_GRP_CD = D1.COLLEGE_GRP_CD ");
        stb.append("    LEFT JOIN AFT_GRAD_CDAT T1 ");
        stb.append("      ON M1.SCHOOL_CD = T1.STAT_CD ");
        stb.append("    LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT T2 ");
        stb.append("      ON T2.YEAR          = T1.YEAR ");
        stb.append("     AND T2.SEQ           = T1.SEQ ");
        stb.append("     AND T2.DETAIL_SEQ    = 1 ");
        stb.append("    LEFT JOIN COLLEGE_EXAM_CALENDAR C1 ");
        stb.append("      ON C1.YEAR          = T1.YEAR ");
        stb.append("     AND C1.SCHOOL_CD     = T1.STAT_CD ");
        stb.append("     AND C1.FACULTYCD     = T1.FACULTYCD ");
        stb.append("     AND C1.DEPARTMENTCD  = T1.DEPARTMENTCD ");
        stb.append("     AND C1.ADVERTISE_DIV = T2.REMARK1 ");
        stb.append("     AND C1.PROGRAM_CD    = T2.REMARK2 ");
        stb.append("     AND C1.FORM_CD       = T2.REMARK3 ");
        stb.append("     AND C1.L_CD1         = T2.REMARK4 ");
        stb.append("     AND C1.S_CD          = T2.REMARK5 ");
        stb.append(" WHERE ");
        stb.append("   M2.COLLEGE_GRP_CD IN ('01', '02', '03', '04', '05', '06', '07', '08') ");
        stb.append(" GROUP BY ");
        stb.append("     M2.COLLEGE_GRP_CD, ");
        stb.append("     M1.SCHOOL_CD, ");
        stb.append("     M1.SCHOOL_NAME, ");
        stb.append("     M2.COLLEGE_GRP_NAME, ");
        stb.append("     C1.ACCEPTANCE_CRITERION_B ");
        stb.append(" ORDER BY ");
        stb.append("     M2.COLLEGE_GRP_CD, ");
        stb.append("     M1.SCHOOL_CD ");

        return stb.toString();
    }

    private class SummaryData {
        final String _oldschregtype;
        final String _school_Group;
        final String _school_Category_Cd;
        final String _school_Cd;
        final String _school_Name;
        final String _pref_Cd;
        final String _ssAvg;
        final String _decision_Cnt;
        final String _decision_NormalCnt;
        final String _decision_CenterCnt;
        final String _decision_SpecCnt;
        final String _decision_KouboCnt;
        final String _decision_AoCnt;
        final String _planstat_Cnt;
        final String _planstat_NormalCnt;
        final String _planstat_CenterCnt;
        final String _planstat_SpecCnt;
        final String _planstat_KouboCnt;
        final String _planstat_AoCnt;

        SummaryData(
            final String oldschregtype,
            final String school_Group,
            final String school_Category_Cd,
            final String school_Cd,
            final String school_Name,
            final String pref_Cd,
            final String ssAvg,
            final String decision_Cnt,
            final String decision_NormalCnt,
            final String decision_CenterCnt,
            final String decision_SpecCnt,
            final String decision_KouboCnt,
            final String decision_AoCnt,
            final String planstat_Cnt,
            final String planstat_NormalCnt,
            final String planstat_CenterCnt,
            final String planstat_SpecCnt,
            final String planstat_KouboCnt,
            final String planstat_AoCnt
        ) {
            _oldschregtype = oldschregtype;
            _school_Group = school_Group;
            _school_Category_Cd = school_Category_Cd;
            _school_Cd = school_Cd;
            _school_Name = school_Name;
            _pref_Cd = pref_Cd;
            _ssAvg = ssAvg;
            _decision_Cnt = decision_Cnt;
            _decision_NormalCnt = decision_NormalCnt;
            _decision_CenterCnt = decision_CenterCnt;
            _decision_SpecCnt = decision_SpecCnt;
            _decision_KouboCnt = decision_KouboCnt;
            _decision_AoCnt = decision_AoCnt;
            _planstat_Cnt = planstat_Cnt;
            _planstat_NormalCnt = planstat_NormalCnt;
            _planstat_CenterCnt = planstat_CenterCnt;
            _planstat_SpecCnt = planstat_SpecCnt;
            _planstat_KouboCnt = planstat_KouboCnt;
            _planstat_AoCnt = planstat_AoCnt;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _school_Cd + "("+ _school_Category_Cd + ")" + " = " + _school_Name;
        }
    }
    private class SummaryData2 {
        final String _school_Group;
        final String _school_Cd;
        final String _school_Name;
        final String _college_Grp_Name;
        final String _acceptance_Criterion_B;
        final String _nowyearcnt;
        final String _oneyearcnt;
        final String _twoyearcnt;
        final String _threeyearcnt;
        final String _fouryearcnt;
        private SummaryData2(final String school_Group, final String school_Cd, final String school_Name, final String college_Grp_Name,
        		            final String acceptance_Criterion_B, final String nowyearcnt, final String oneyearcnt,
        		            final String twoyearcnt, final String threeyearcnt, final String fouryearcnt) {
            _school_Group = school_Group;
            _school_Cd = school_Cd;
            _school_Name = school_Name;
            _college_Grp_Name = college_Grp_Name;
            _acceptance_Criterion_B = acceptance_Criterion_B;
            _nowyearcnt = nowyearcnt;
            _oneyearcnt = oneyearcnt;
            _twoyearcnt = twoyearcnt;
            _threeyearcnt = threeyearcnt;
            _fouryearcnt = fouryearcnt;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 69140 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _ctrlYear;
        final String _ctrlDate;
        final String _output;
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _output = request.getParameter("OUTPUT");
        }
    }
}

// eof

