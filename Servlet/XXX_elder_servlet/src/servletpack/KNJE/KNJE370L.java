/*
 * $Id: eb179997a8eb487c5edb407343b5a0a77aa298d5 $
 *
 * 作成日: 2019/08/20
 * 作成者: yogi
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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJE370L {

    private static final Log log = LogFactory.getLog(KNJE370L.class);

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
        if ("2".equals(_param._output)) {
        	printMain2(db2, svf);
        } else {
        	printMain1(db2, svf);
        }
    }

    private void printMain1(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJE370L_1.frm", 1);
        final Map printMap = getPrintInf(db2);
        int pageNo = 1;
        for (Iterator iterator = printMap.keySet().iterator(); iterator.hasNext();) {
        	final String keyStr = (String)iterator.next();
            final List printList = (List)printMap.get(keyStr);
            int cnt = 1;
            int male = 0;
            int female = 0;
            for (Iterator ite = printList.iterator();ite.hasNext();) {
            	final PrintData1 prtdat = (PrintData1)ite.next();
            	if (cnt > 25) {
                    svf.VrEndPage();
            		pageNo++;
            		cnt = 1;
            	}
                if (cnt == 1) {
                	setTitle1(db2, svf, prtdat, pageNo);
                }
                svf.VrsOutn("SCHREGNO", cnt, String.valueOf(Integer.parseInt(prtdat._schregNo)));//学籍番号
                final String hrClass = StringUtils.defaultString(prtdat._hr_Name, "");
                final String attendNo = StringUtils.defaultString(prtdat._attendNo, "");
                final String hrStr = hrClass + ("".equals(attendNo) ? "" : attendNo + "番");
                final int hrlen = KNJ_EditEdit.getMS932ByteLength(hrStr);
                final String hridx = hrlen > 15 ? "2" : "";
                svf.VrsOutn("HR_NAME" + hridx, cnt, hrStr);//年組版
                svf.VrsOutn("NAME_SHOW", cnt, prtdat._name);//氏名
                svf.VrsOutn("SEX", cnt, prtdat._sexName);//性別
                svf.VrsOutn("PUBPRIV_KIND", cnt, prtdat._school_Group_Name);//設置区分
                final int pflen = KNJ_EditEdit.getMS932ByteLength(prtdat._pref_Name);
                final String pfidx = pflen > 40 ? "4" : pflen > 30 ? "3" : pflen > 20 ? "2" : "1";
                if ("4".equals(pfidx)) {
                	String[] cutAddr = KNJ_EditEdit.get_token(prtdat._pref_Name, 40, 2);
                    svf.VrsOutn("ADDR" + pfidx + "_1", cnt, cutAddr[0]);//所在地
                    svf.VrsOutn("ADDR" + pfidx + "_2", cnt, cutAddr[1]);//所在地
                } else {
                    svf.VrsOutn("ADDR" + pfidx, cnt, prtdat._pref_Name);//所在地
                }
                svf.VrsOutn("FACULTY", cnt, prtdat._facultyName);//学部
                final int dnlen = KNJ_EditEdit.getMS932ByteLength(prtdat._departmentName);
                final String dnidx = dnlen > 18 ? "3" : dnlen > 12 ? "2" : "";
                if ("3".equals(dnidx)) {
                	String[] cutDn = KNJ_EditEdit.get_token(prtdat._departmentName, 40, 2);
                    svf.VrsOutn("MAJORCD" + dnidx + "_1", cnt, cutDn[0]);//学科
                    svf.VrsOutn("MAJORCD" + dnidx + "_2", cnt, cutDn[1]);//学科
                } else {
                    svf.VrsOutn("MAJORCD" + dnidx, cnt, prtdat._departmentName);//学科
                }
                svf.VrsOutn("SCHEDULE", cnt, prtdat._examSchedule);//日程
                svf.VrsOutn("REMARK", cnt, prtdat._examType);//方式

                final int helen = KNJ_EditEdit.getMS932ByteLength(prtdat._howToExam_Name);
                final String heidx = helen > 18 ? "3" : helen > 12 ? "2" : "";
                if ("3".equals(heidx)) {
                	String[] cutHe = KNJ_EditEdit.get_token(prtdat._howToExam_Name, 40, 2);
                    svf.VrsOutn("EXAM_METHOD" + heidx + "_1", cnt, cutHe[0]);//受験方式
                    svf.VrsOutn("EXAM_METHOD" + heidx + "_2", cnt, cutHe[1]);//受験方式
                } else {
                    svf.VrsOutn("EXAM_METHOD" + heidx, cnt, prtdat._howToExam_Name);//受験方式
                }

                svf.VrsOutn("RESULT2", cnt, prtdat._decisionName);//合否
                svf.VrsOutn("COURSE_AHEAD", cnt, prtdat._planStat_Name);//進路状況
                svf.VrsOutn("EXAM_NO", cnt, prtdat._examNo);//受験番号
                if ("2".equals(prtdat._sex)) {
                	female += 1;
                } else {
                	male += 1;
                }
                cnt++;
            }
            //合計を出力
            svf.VrsOut("TOTAL", "男  "+ paddingSp(male, 3) + " 女  " + paddingSp(female, 3) + " 合計  " + paddingSp(printList.size(), 3));
            _hasData = true;
            svf.VrEndPage();
        }
    }

    private String paddingSp(final int padVal, final int padSize) {
    	//埋めるサイズを超えた文字列であれば元データが切れるので、元データのまま返す。
    	if (String.valueOf(padVal).length() > padSize) {
    		return String.valueOf(padVal);
    	}
    	final String padsp = StringUtils.repeat(" ", padSize);
    	final int pad_rad = String.valueOf(padVal).length() + padSize;
    	return (padsp + String.valueOf(padVal)).substring(pad_rad-padSize);
    }

    private void setTitle1(final DB2UDB db2, final Vrw32alp svf, final PrintData1 prtData, final int pageNo) {
        //TITLE(和暦)
        svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear))+"年度 進路先別名簿");
    	//進路先コード
        svf.VrsOut("SCHOOL_CD", prtData._stat_Cd);
        //進路先名称
        svf.VrsOut("SCHOOL_NAME", (String)_param._collegeMst.get(prtData._stat_Cd));
    	//年月日(和暦)
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));
        //ページ
        svf.VrsOut("PAGE", String.valueOf(pageNo));
    }

    private Map getPrintInf(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            List addList = new ArrayList();
            while (rs.next()) {
            	final String schregNo = rs.getString("SCHREGNO");
            	final String grade = rs.getString("GRADE");
            	final String grade_Cd = rs.getString("GRADE_CD");
            	final String hr_Class = rs.getString("HR_CLASS");
            	final String hr_Name = rs.getString("HR_NAME");
            	final String attendNo = rs.getString("ATTENDNO");
            	final String name = rs.getString("NAME");
            	final String sex = rs.getString("SEX");
            	final String sexName = rs.getString("SEXNAME");
            	final String stat_Cd = rs.getString("STAT_CD");
            	final String facultyCd = rs.getString("FACULTYCD");
            	final String facultyName = rs.getString("FACULTYNAME");
            	final String departmentCd = rs.getString("DEPARTMENTCD");
            	final String departmentName = rs.getString("DEPARTMENTNAME");
            	final String examSchedule = rs.getString("EXAMSCHEDULE");
            	final String examType = rs.getString("EXAMTYPE");
            	final String decision = rs.getString("DECISION");
            	final String decisionName = rs.getString("DECISIONNAME");
            	final String school_Group_Name = rs.getString("SCHOOL_GROUP_NAME");
            	final String pref_Name = rs.getString("PREF_NAME");
            	final String howToExam = rs.getString("HOWTOEXAM");
            	final String howToExam_Name = rs.getString("HOWTOEXAM_NAME");
            	final String planStat = rs.getString("PLANSTAT");
            	final String planStat_Name = rs.getString("PLANSTAT_NAME");
            	final String examNo = rs.getString("EXAMNO");

                final PrintData1 addObj = new PrintData1(schregNo, grade, grade_Cd, hr_Class, hr_Name, attendNo, name,
                		                                sex, sexName, stat_Cd, facultyCd, facultyName,
                		                                departmentCd, departmentName, examSchedule, examType,
                		                                decision, decisionName, school_Group_Name, pref_Name,
                		                                howToExam, howToExam_Name, planStat, planStat_Name, examNo);
                if (!retMap.containsKey(stat_Cd)) {
                	addList = new ArrayList();
                	retMap.put(stat_Cd, addList);
                } else {
                	addList = (List)retMap.get(stat_Cd);
                }
                addList.add(addObj);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        // その生徒の最終学年(学期)を割り出す。
        // 少なくとも、選択対象が大学なので、'H'は条件。
        stb.append(" WITH MAXSEMES_DAT AS ( ");
        stb.append(" SELECT ");
        stb.append("   TW1.SCHREGNO, ");
        stb.append("   MAX(TW1.GRADE || '-' || TW1.SEMESTER) AS MAXKEY ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT TW1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT TW2 ");
        stb.append("     ON TW2.YEAR = TW1.YEAR ");
        stb.append("    AND TW2.GRADE = TW1.GRADE ");
        stb.append(" WHERE ");
        stb.append("   TW2.SCHOOL_KIND IN ('H') ");
        stb.append(" GROUP BY ");
        stb.append("   TW1.SCHREGNO ");
        // 最終学年(&学期)のデータから当年度分だけを取得
        stb.append(" ), BASE_SCHREG_A AS ( ");
        stb.append(" SELECT ");
        stb.append("   TK1.* ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT TK1 ");
        stb.append("   INNER JOIN MAXSEMES_DAT TK2 ");
        stb.append("     ON TK2.MAXKEY = TK1.GRADE || '-' || TK1.SEMESTER ");
        stb.append("    AND TK2.SCHREGNO = TK1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   TK1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append(" ) ");
        stb.append(" select ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T4.GRADE, ");
        stb.append("   T5.GRADE_CD, ");
        stb.append("   T4.HR_CLASS, ");
        stb.append("   T6.HR_NAME, ");
        stb.append("   T4.ATTENDNO, ");
        stb.append("   T3.NAME, ");
        stb.append("   T3.SEX, ");
        stb.append("   Z002.ABBV1 AS SEXNAME, ");
        stb.append("   E012.NAME1 as SCHOOL_GROUP_NAME, ");
        stb.append("   L4.PREF_NAME, ");
        stb.append("   T1.STAT_CD, ");
        stb.append("   T1.FACULTYCD, ");
        stb.append("   M2.FACULTYNAME, ");
        stb.append("   T1.DEPARTMENTCD, ");
        stb.append("   M1.DEPARTMENTNAME, ");
        stb.append("   T2.REMARK2 AS EXAMSCHEDULE, ");
        stb.append("   T2.REMARK5 AS EXAMTYPE, ");
        stb.append("   T1.HOWTOEXAM, ");
        stb.append("   E002.NAME1 as HOWTOEXAM_NAME, ");
        stb.append("   T1.DECISION, ");
        stb.append("   E005.NAME1 AS DECISIONNAME, ");
        stb.append("   T1.PLANSTAT, ");
        stb.append("   E006.NAME1 as PLANSTAT_NAME, ");
        stb.append("   AFT_GRAD_D.REMARK9 AS EXAMNO ");
        stb.append(" FROM ");
        stb.append("   AFT_GRAD_COURSE_DAT T1 ");
        stb.append("   LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEQ = T1.SEQ ");
        stb.append("    AND T2.DETAIL_SEQ = '001' ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T3 ");
        stb.append("     ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN COLLEGE_DEPARTMENT_MST M1 ");
        stb.append("     ON M1.SCHOOL_CD = T1.STAT_CD ");
        stb.append("    AND M1.FACULTYCD = T1.FACULTYCD ");
        stb.append("    AND M1.DEPARTMENTCD = T1.DEPARTMENTCD ");
        stb.append("   LEFT JOIN COLLEGE_FACULTY_MST M2 ");
        stb.append("     ON M2.SCHOOL_CD = T1.STAT_CD ");
        stb.append("    AND M2.FACULTYCD = T1.FACULTYCD ");
        stb.append("   LEFT JOIN BASE_SCHREG_A T4 ");
        stb.append("     ON T4.YEAR = T1.YEAR ");
        stb.append("    AND T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN NAME_MST Z002 ");
        stb.append("     ON Z002.NAMECD1 = 'Z002' ");
        stb.append("    AND Z002.NAMECD2 = T3.SEX ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT T5 ");
        stb.append("     ON T5.YEAR = T4.YEAR ");
        stb.append("    AND T5.GRADE = T4.GRADE ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT T6 ");
        stb.append("     ON T6.YEAR = T4.YEAR ");
        stb.append("    AND T6.SEMESTER = T4.SEMESTER ");
        stb.append("    AND T6.GRADE = T4.GRADE ");
        stb.append("    AND T6.HR_CLASS = T4.HR_CLASS ");
        stb.append("   LEFT JOIN NAME_MST E012 ");
        stb.append("     ON E012.NAMECD1 = 'E012' ");
        stb.append("    AND E012.NAMECD2 = T1.SCHOOL_GROUP ");
        stb.append("   LEFT JOIN PREF_MST L4 ");
        stb.append("     ON L4.PREF_CD = T1.PREF_CD ");
        stb.append("   LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT AFT_GRAD_D ");
        stb.append("     ON T1.YEAR = AFT_GRAD_D.YEAR ");
        stb.append("    AND T1.SEQ = AFT_GRAD_D.SEQ ");
        stb.append("    AND AFT_GRAD_D.DETAIL_SEQ = 1 ");
        stb.append("   LEFT JOIN NAME_MST E002 ");
        stb.append("     ON E002.NAMECD1 = 'E002' ");
        stb.append("    AND E002.NAMECD2 = T1.HOWTOEXAM ");
        stb.append("   LEFT JOIN NAME_MST E005 ");
        stb.append("     ON E005.NAMECD1 = 'E005' ");
        stb.append("    AND E005.NAMECD2 = T1.DECISION ");
        stb.append("   LEFT JOIN NAME_MST E006 ");
        stb.append("     ON E006.NAMECD1 = 'E006' ");
        stb.append("    AND E006.NAMECD2 = T1.PLANSTAT ");
        stb.append(" WHERE ");
        stb.append("    T1.YEAR = '" + _param._ctrlYear + "' ");
        if ("2".equals(_param._output)) {
            stb.append("    AND T1.SENKOU_KIND = '1' ");
        } else {
            stb.append("    AND T1.SENKOU_KIND = '0' ");
        }
        stb.append("    AND E005.NAMESPARE1 = '3' ");
        if ("2".equals(_param._filter)) {
            stb.append("    AND T1.PLANSTAT = '1' ");
        }
        stb.append("    AND T1.STAT_CD IN " + _param._categorySelectedIn + " ");
        stb.append(" ORDER BY ");
        stb.append("   T1.STAT_CD ");

        return stb.toString();
    }

    private class PrintData1 {
        final String _schregNo;
        final String _grade;
        final String _grade_Cd;
        final String _hr_Class;
        final String _hr_Name;
        final String _attendNo;
        final String _name;
        final String _sex;
        final String _sexName;
        final String _stat_Cd;
        final String _facultyCd;
        final String _facultyName;
        final String _departmentCd;
        final String _departmentName;
        final String _examSchedule;
        final String _examType;
        final String _decision;
        final String _decisionName;
        final String _school_Group_Name;
        final String _pref_Name;
        final String _howToExam;
        final String _howToExam_Name;
        final String _planStat;
        final String _planStat_Name;
        final String _examNo;
        public PrintData1(
        		final String schregNo, final String grade, final String grade_Cd, final String hr_Class,
        		final String hr_Name, final String attendNo, final String name, final String sex, final String sexName,
        		final String stat_Cd, final String facultyCd, final String facultyName, final String departmentCd,
        		final String departmentName, final String examSchedule, final String examType,
        		final String decision, final String decisionName, final String school_Group_Name,
        		final String pref_Name, final String howToExam, final String howToExam_Name,
        		final String planStat, final String planStat_Name, final String examNo
        ) {
            _schregNo = schregNo;
            _grade = grade;
            _grade_Cd = grade_Cd;
            _hr_Class = hr_Class;
            _hr_Name = hr_Name;
            _attendNo = attendNo;
            _name = name;
            _sex = sex;
            _sexName = sexName;
            _stat_Cd = stat_Cd;
            _facultyCd = facultyCd;
            _facultyName = facultyName;
            _departmentCd = departmentCd;
            _departmentName = departmentName;
            _examSchedule = examSchedule;
            _examType = examType;
            _decision = decision;
            _decisionName = decisionName;
            _school_Group_Name = school_Group_Name;
            _pref_Name = pref_Name;
            _howToExam = howToExam;
            _howToExam_Name = howToExam_Name;
            _planStat = planStat;
            _planStat_Name = planStat_Name;
            _examNo = examNo;
        }
    }

    private void printMain2(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJE370L_2.frm", 1);
        final Map printMap = getPrintShushoku(db2);
        int pageNo = 1;
        for (Iterator iterator = printMap.keySet().iterator(); iterator.hasNext();) {
        	final String keyStr = (String)iterator.next();
            final List printList = (List)printMap.get(keyStr);
            int cnt = 1;
            int male = 0;
            int female = 0;
            for (Iterator ite = printList.iterator();ite.hasNext();) {
            	final PrintData2 prtdat = (PrintData2)ite.next();
            	if (cnt > 25) {
                    svf.VrEndPage();
            		pageNo++;
            		cnt = 1;
            	}
                if (cnt == 1) {
                	setTitle2(db2, svf, prtdat, pageNo);
                }
                svf.VrsOutn("SCHREGNO", cnt, String.valueOf(Integer.parseInt(prtdat._schregno))); //学籍番号
                final String hrClass = StringUtils.defaultString(prtdat._hrName, "");
                final String attendNo = StringUtils.defaultString(prtdat._attendno, "");
                final String hrStr = hrClass + ("".equals(attendNo) ? "" : attendNo + "番");
                final int hrlen = KNJ_EditEdit.getMS932ByteLength(hrStr);
                final String hridx = hrlen > 15 ? "2" : "";
                svf.VrsOutn("HR_NAME" + hridx, cnt, hrStr);//年組版
                svf.VrsOutn("NAME_SHOW", cnt, prtdat._name);           //生徒氏名
                svf.VrsOutn("SEX", cnt, prtdat._sexname);        //性別
                svf.VrsOutn("INDUST_KIND", cnt, prtdat._industryLName);  //産業種別
                svf.VrsOutn("LOCATION", cnt, prtdat._prefName);       //就業場所
                svf.VrsOutn("APPLI_METHOD", cnt, prtdat._introductionDivName);  //応募方法
                svf.VrsOutn("RESULT2", cnt, prtdat._decisionName);   //合否
                svf.VrsOutn("COURSE_AHEAD", cnt, prtdat._planstatName);   //進路状況
                if ("2".equals(prtdat._sex)) {
                	female += 1;
                } else {
                	male += 1;
                }
                cnt++;
            }
            //合計を出力
            svf.VrsOut("TOTAL", "男  "+ paddingSp(male, 3) + " 女  " + paddingSp(female, 3) + " 合計  " + paddingSp(printList.size(), 3));
            _hasData = true;
            svf.VrEndPage();
        }
    }

    private void setTitle2(final DB2UDB db2, final Vrw32alp svf, final PrintData2 prtData, final int pageNo) {
        //TITLE(和暦)
        svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear))+"年度 進路先別名簿");
    	//会社コード
        svf.VrsOut("COMPANY_CD", prtData._statCd);
        //会社名称
        svf.VrsOut("COMPANY_NAME", prtData._industryLName);
    	//年月日(和暦)
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));
        //ページ
        svf.VrsOut("PAGE", String.valueOf(pageNo));
    }

    private Map getPrintShushoku(final DB2UDB db2) {
        final Map rtnMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String shushokuSql = getShushokuSql();
        log.debug("sql = " + shushokuSql);
        List addList = new ArrayList();
        try {
            ps = db2.prepareStatement(shushokuSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String gradHrName = rs.getString("G_HR_NAME");
                final String hrName = StringUtils.defaultString(rs.getString("HR_NAME"));
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String sexname = rs.getString("SEXNAME");
                final String senkouKind = rs.getString("SENKOU_KIND");
                final String statCd = rs.getString("STAT_CD");
                final String industryLCd = rs.getString("INDUSTRY_LCD");
                final String prefCd = rs.getString("PREF_CD");
                final String introductionDiv = rs.getString("INTRODUCTION_DIV");
                final String introductionDivName = rs.getString("INTRODUCTION_DIVNAME");
                final String howtoexam = rs.getString("HOWTOEXAM");
                final String decision = rs.getString("DECISION");
                final String planstat = rs.getString("PLANSTAT");
                final String statName = rs.getString("STAT_NAME");
                final String industryLName = rs.getString("INDUSTRY_LNAME");
                final String prefName = rs.getString("PREF_NAME");
                final String howtoexamName = rs.getString("HOWTOEXAM_NAME");
                final String decisionName = rs.getString("DECISION_NAME");
                final String planstatName = rs.getString("PLANSTAT_NAME");
                final PrintData2 shushoku = new PrintData2(schregno, grade, hrClass, attendno, gradHrName,
                                                        hrName, name, sex, sexname, senkouKind, statCd, industryLCd, prefCd,
                                                        introductionDiv, introductionDivName, howtoexam, decision, planstat, statName, industryLName,
                                                        prefName, howtoexamName, decisionName, planstatName);

                if (!rtnMap.containsKey(statCd)) {
                	addList = new ArrayList();
                	rtnMap.put(statCd, addList);
                } else {
                	addList = (List)rtnMap.get(statCd);
                }
                addList.add(shushoku);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnMap;
    }

    private String getShushokuSql() {
        final StringBuffer stb = new StringBuffer();
        // その生徒の最終学年(学期)を割り出す。
        // 少なくとも、選択対象が大学なので、'H'は条件。
        stb.append(" WITH MAXSEMES_DAT AS ( ");
        stb.append(" SELECT ");
        stb.append("   TW1.SCHREGNO, ");
        stb.append("   MAX(TW1.GRADE || '-' || TW1.SEMESTER) AS MAXKEY ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT TW1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT TW2 ");
        stb.append("     ON TW2.YEAR = TW1.YEAR ");
        stb.append("    AND TW2.GRADE = TW1.GRADE ");
        stb.append(" WHERE ");
        stb.append("   TW2.SCHOOL_KIND IN ('H') ");
        stb.append(" GROUP BY ");
        stb.append("   TW1.SCHREGNO ");
        // 最終学年(&学期)のデータから当年度分だけを取得
        stb.append(" ), BASE_SCHREG_A AS ( ");
        stb.append(" SELECT ");
        stb.append("   TK1.* ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT TK1 ");
        stb.append("   INNER JOIN MAXSEMES_DAT TK2 ");
        stb.append("     ON TK2.MAXKEY = TK1.GRADE || '-' || TK1.SEMESTER ");
        stb.append("    AND TK2.SCHREGNO = TK1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   TK1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append(" ), KISOTSU AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     '' AS GRADE, ");
        stb.append("     '' AS HR_CLASS, ");
        stb.append("     '' AS ATTENDNO, ");
        stb.append("     '2' AS SORT_DIV, ");
        stb.append("     '既卒' AS G_HR_NAME, ");
//        stb.append("     VALUE(FISCALYEAR(G_BASE.GRD_DATE),'') || '年度卒' || G_HDAT.HR_NAME AS HR_NAME, ");
        stb.append("     '' AS HR_NAME, ");
        stb.append("     I1.NAME, ");
        stb.append("     I1.SEX, ");
        stb.append("     Z002.ABBV1 AS SEXNAME, ");
        stb.append("     T1.SENKOU_KIND, ");
        stb.append("     T1.STAT_CD, ");
        stb.append("     L1.COMPANY_NAME as STAT_NAME, ");
        stb.append("     L1.INDUSTRY_LCD, ");
        stb.append("     L2.INDUSTRY_LNAME, ");
        stb.append("     T1.PREF_CD, ");
        stb.append("     L4.PREF_NAME, ");
        stb.append("     T1.INTRODUCTION_DIV, ");
        stb.append("     CASE WHEN T1.INTRODUCTION_DIV = '1' THEN '学校紹介' ");
        stb.append("          WHEN T1.INTRODUCTION_DIV = '2' THEN '自己・縁故' ");
        stb.append("          WHEN T1.INTRODUCTION_DIV = '3' THEN '公務員' ");
        stb.append("          ELSE '' END AS INTRODUCTION_DIVNAME, ");
        stb.append("     T1.HOWTOEXAM, ");
        stb.append("     E002.NAME1 as HOWTOEXAM_NAME, ");
        stb.append("     T1.DECISION, ");
        stb.append("     E005.NAME1 as DECISION_NAME, ");
        stb.append("     T1.PLANSTAT, ");
        stb.append("     VALUE(E006.NAME2, E006.NAME1) as PLANSTAT_NAME ");
        stb.append(" FROM ");
        stb.append("     AFT_GRAD_COURSE_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST I1 ON I1.SCHREGNO = T1.SCHREGNO ");
//        stb.append("     LEFT JOIN GRD_BASE_MST G_BASE ON T1.SCHREGNO = G_BASE.SCHREGNO ");
        stb.append("     LEFT JOIN GRD_REGD_HDAT G_HDAT ON T1.YEAR = G_HDAT.YEAR ");
        stb.append("                                   AND G_BASE.GRD_SEMESTER = G_HDAT.SEMESTER");
        stb.append("                                   AND G_BASE.GRD_GRADE    = G_HDAT.GRADE");
        stb.append("                                   AND G_BASE.GRD_HR_CLASS = G_HDAT.HR_CLASS");
        stb.append("     LEFT JOIN BASE_SCHREG_A I2 ON I2.SCHREGNO = T1.SCHREGNO AND I2.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     INNER JOIN COMPANY_MST L1 ON L1.COMPANY_CD = T1.STAT_CD ");
        stb.append("     LEFT JOIN INDUSTRY_L_MST L2 ON L2.INDUSTRY_LCD = L1.INDUSTRY_LCD ");
        stb.append("     LEFT JOIN PREF_MST L4 ON L4.PREF_CD = T1.PREF_CD ");
        stb.append("     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = T1.HOWTOEXAM ");
        stb.append("     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = T1.DECISION ");
        stb.append("     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = T1.PLANSTAT ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = I1.SEX ");
        stb.append(" WHERE ");
        stb.append("         T1.YEAR = '" + _param._ctrlYear + "' ");
        if ("2".equals(_param._output)) {
            stb.append("    AND T1.SENKOU_KIND = '1' ");
        } else {
            stb.append("    AND T1.SENKOU_KIND = '0' ");
        }
        stb.append("     AND I2.SCHREGNO IS NULL ");
        stb.append("     AND E005.NAMESPARE1 = '3' ");
        if ("2".equals(_param._filter)) {
            stb.append("     AND T1.PLANSTAT = '1' ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     I2.GRADE, ");
        stb.append("     I2.HR_CLASS, ");
        stb.append("     I2.ATTENDNO, ");
        stb.append("     '1' AS SORT_DIV, ");
        stb.append("     '' AS G_HR_NAME, ");
        stb.append("     I3.HR_NAME, ");
        stb.append("     I1.NAME, ");
        stb.append("     I1.SEX, ");
        stb.append("     Z002.ABBV1 AS SEXNAME, ");
        stb.append("     T1.SENKOU_KIND, ");
        stb.append("     T1.STAT_CD, ");
        stb.append("     L1.COMPANY_NAME as STAT_NAME, ");
        stb.append("     L1.INDUSTRY_LCD, ");
        stb.append("     L2.INDUSTRY_LNAME, ");
        stb.append("     T1.PREF_CD, ");
        stb.append("     L4.PREF_NAME, ");
        stb.append("     T1.INTRODUCTION_DIV, ");
        stb.append("     CASE WHEN T1.INTRODUCTION_DIV = '1' THEN '学校紹介' ");
        stb.append("          WHEN T1.INTRODUCTION_DIV = '2' THEN '自己・縁故' ");
        stb.append("          WHEN T1.INTRODUCTION_DIV = '3' THEN '公務員' ");
        stb.append("          ELSE '' END AS INTRODUCTION_DIVNAME, ");
        stb.append("     T1.HOWTOEXAM, ");
        stb.append("     E002.NAME1 as HOWTOEXAM_NAME, ");
        stb.append("     T1.DECISION, ");
        stb.append("     E005.NAME1 as DECISION_NAME, ");
        stb.append("     T1.PLANSTAT, ");
        stb.append("     VALUE(E006.NAME2, E006.NAME1) as PLANSTAT_NAME ");
        stb.append(" FROM ");
        stb.append("     AFT_GRAD_COURSE_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST I1 ON I1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN BASE_SCHREG_A I2 ON I2.SCHREGNO = T1.SCHREGNO AND I2.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT I3 ON I3.YEAR = I2.YEAR AND I3.SEMESTER = I2.SEMESTER AND I3.GRADE = I2.GRADE AND I3.HR_CLASS = I2.HR_CLASS ");
        stb.append("     INNER JOIN COMPANY_MST L1 ON L1.COMPANY_CD = T1.STAT_CD ");
        stb.append("     LEFT JOIN INDUSTRY_L_MST L2 ON L2.INDUSTRY_LCD = L1.INDUSTRY_LCD ");
        stb.append("     LEFT JOIN PREF_MST L4 ON L4.PREF_CD = T1.PREF_CD ");
        stb.append("     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = T1.HOWTOEXAM ");
        stb.append("     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = T1.DECISION ");
        stb.append("     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = T1.PLANSTAT ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = I1.SEX ");
        stb.append(" WHERE ");
        stb.append("         T1.YEAR = '" + _param._ctrlYear + "' ");
        if ("2".equals(_param._output)) {
            stb.append("    AND T1.SENKOU_KIND = '1' ");
        } else {
            stb.append("    AND T1.SENKOU_KIND = '0' ");
        }
        stb.append("     AND E005.NAMESPARE1 = '3' ");
        if ("2".equals(_param._filter)) {
            stb.append("     AND T1.PLANSTAT = '1' ");
        }
        stb.append("     AND T1.STAT_CD IN " + _param._categorySelectedIn + " ");

        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     KISOTSU ");
        stb.append(" ORDER BY ");
        stb.append("     SORT_DIV, ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     ATTENDNO, ");
        stb.append("     SEQ ");

        return stb.toString();
    }

    private class PrintData2 {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _gradHrName;
        final String _hrName;
        final String _name;
        final String _sex;
        final String _sexname;
        final String _senkouKind;
        final String _statCd;
        final String _industryLCd;
        final String _prefCd;
        final String _introductionDiv;
        final String _introductionDivName;
        final String _howtoexam;
        final String _decision;
        final String _planstat;
        final String _statName;
        final String _industryLName;
        final String _prefName;
        final String _howtoexamName;
        final String _decisionName;
        final String _planstatName;

        PrintData2(final String schregno,
                final String grade,
                final String hrClass,
                final String attendno,
                final String gradHrName,
                final String hrName,
                final String name,
                final String sex,
                final String sexname,
                final String senkouKind,
                final String statCd,
                final String industryLCd,
                final String prefCd,
                final String introductionDiv,
                final String introductionDivName,
                final String howtoexam,
                final String decision,
                final String planstat,
                final String statName,
                final String industryLName,
                final String prefName,
                final String howtoexamName,
                final String decisionName,
                final String planstatName
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _gradHrName = gradHrName;
            _hrName = hrName;
            _name = name;
            _sex = sex;
            _sexname = sexname;
            _senkouKind = senkouKind;
            _statCd = statCd;
            _industryLCd = industryLCd;
            _prefCd = prefCd;
            _introductionDiv = introductionDiv;
            _introductionDivName = introductionDivName;
            _howtoexam = howtoexam;
            _decision = decision;
            _planstat = planstat;
            _statName = statName;
            _industryLName = industryLName;
            _prefName = prefName;
            _howtoexamName = howtoexamName;
            _decisionName = decisionName;
            _planstatName = planstatName;
        }
    }
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 69335 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlDate;
        private final String _filter;
        private final String _output;
        private final String _categorySelectedIn;

        private Map _collegeMst;
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _filter = request.getParameter("FILTER");
            _output = request.getParameter("OUTPUT");
            final String[] categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _categorySelectedIn = getCategorySelectedIn(categorySelected);
            _collegeMst = getCollegeMst(db2);
        }

        private String getCategorySelectedIn(final String[] categorySelected) {
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < categorySelected.length; i++) {
                if (0 < i) stb.append(",");
                stb.append("'" + categorySelected[i] + "'");
            }
            stb.append(")");
            return stb.toString();
        }
        private Map getCollegeMst(final DB2UDB db2) {
        	Map retMap = new LinkedMap();
        	StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("   T1.SCHOOL_CD, ");
        	stb.append("   T1.SCHOOL_NAME ");
        	stb.append(" FROM ");
        	stb.append("   COLLEGE_MST T1 ");
        	stb.append(" WHERE ");
        	stb.append("   T1.SCHOOL_CD IN "+_categorySelectedIn+" ");
        	stb.append(" ORDER BY ");
        	stb.append("   T1.SCHOOL_CD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = stb.toString();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                	retMap.put(rs.getString("SCHOOL_CD"), rs.getString("SCHOOL_NAME"));
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        	return retMap;
        }
    }
}

// eof
