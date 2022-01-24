/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 0b1d7bf9cf00ab1b29c3ebb44b5a4561b3f636bd $
 *
 * 作成日: 2020/05/18
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD184S {

    private static final Log log = LogFactory.getLog(KNJD184S.class);

    private static final String SEMEALL = "9";

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";
    private static final String ALL9A = "99999A";
    private static final String ALL9B = "99999B";

    private static final String HYOTEI_TESTCD = "9990009";

    private static final String DISP1 = "1";
    private static final String DISP2 = "2";

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

        _hasData = false;
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);


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
    	final String FormName = _param._isPrimary ? "KNJD184S_1.frm" : "KNJD184S_2.frm";
    	svf.VrSetForm(FormName, 1);

    	final Map schInfo =  getSchregInfo(db2);
    	final List schList = new ArrayList();
    	for (Iterator ite = schInfo.keySet().iterator();ite.hasNext();) {
    	    schList.add((String)ite.next());
    	}
    	final Map hyoteiInfo = getHyoteiInfo(db2, schList);
    	final Map remarkInfo = getRemarkInfo(db2, schList);

        //下段の出欠
    	final Map attendMap = new LinkedMap();
        for (final Iterator rit = _param._attendRanges.values().iterator(); rit.hasNext();) {
            final DateRange range = (DateRange) rit.next();
            Attendance.load(db2, _param, schList, range, attendMap);
        }

    	for (Iterator itr = schList.iterator();itr.hasNext();) {
    		final String schregno = (String)itr.next();
    		final SchregInfo schInf = (SchregInfo)schInfo.get(schregno);
        	svf.VrsOut("SCHOOL_NAME_ENG", "Kaichi Comprehensive School " + (_param._isPrimary ? "Primary" : "Secondary"));  //学校名(英)
        	//svf.VrsOut("SCHOOL_NAME_ENG",_param._certifSchoolEngSchoolName);
        	svf.VrsOut("NENDO", _param._loginYear + "年度");  //年度
        	if (_param._schoolLogoImagePath != null) {
        	    svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoImagePath);
        	}

        	svf.VrsOut("GRADE_NAME", schInf._grade_Name2);  //学年
        	svf.VrsOut("HR_NAME", "Team  " + schInf._hr_Class_Name1);  //組名
        	svf.VrsOut("ATENDNO", String.valueOf(Integer.parseInt(schInf._attendno)) + "番");  //出席番号
        	final int nlen = KNJ_EditEdit.getMS932ByteLength(schInf._name);
        	final String nfield = nlen > 34 ? "2" : "1";
        	svf.VrsOut("NAME" + nfield, schInf._name);  //氏名

        	//評定
        	if (hyoteiInfo.containsKey(schregno)) {
            	final Map subMap = (Map) hyoteiInfo.get(schregno);
            	int sc_colidx = 1;
        		for (Iterator itw = subMap.keySet().iterator();itw.hasNext();) {
        			final String subclassCd = (String)itw.next();
                    final Map detailMap = (Map)subMap.get(subclassCd);
                    for (Iterator iti = detailMap.keySet().iterator();iti.hasNext();) {
                    	final String semester = (String)iti.next();
                    	final HyoteiInfo prtWk = (HyoteiInfo)detailMap.get(semester);
                    	final String putIdx = SEMEALL.equals(semester) ? "3" : semester;  //"9学期"なら添え字を3で指定
                    	if (!"".equals(StringUtils.defaultString(prtWk._subclassordername2, ""))) {
                    	    svf.VrsOutn("CLASS_NAME", sc_colidx, prtWk._subclassordername2);
                    	}
                    	if (prtWk._score != null) {
                    	    svf.VrsOutn("VAL" + putIdx, sc_colidx, prtWk._score);
                    	}
                    }
                    sc_colidx++;
        		}
        	}

        	if (remarkInfo.containsKey(schregno)) {
            	final Map subRemarkMap = (Map)remarkInfo.get(schregno);
            	boolean remarkPrtFlg = false;
            	for (Iterator ito = subRemarkMap.keySet().iterator();ito.hasNext();) {
            		final String semester = (String)ito.next();
            		final RemarkInfo prtWk = (RemarkInfo)subRemarkMap.get(semester);
                	final String putIdxStr = SEMEALL.equals(semester) ? "3" : semester;  //"9学期"なら添え字を3で指定
                	if (!remarkPrtFlg) {
                		//プロジェクト型学習の記録
                		////パーソナル or 探究テーマ
                		final int plen = 1;
                		final String pfield = plen > 80 ? "2" : "1";
                		svf.VrsOut("PROJECT1_" + pfield, prtWk._remark01_01);
                		////体験学習テーマ
                		svf.VrsOut("PROJECT2", prtWk._remark01_02);
                		////フィールドワーク
                		svf.VrsOut("PROJECT3", prtWk._remark01_03);
                		////異学年齢活動・表現
                		svf.VrsOut("SP_ACT2", prtWk._remark02_02);
                		remarkPrtFlg = true;
                	}
                	if (!SEMEALL.equals(semester)) {
                		//特別活動の記録
                		final int putIdx = Integer.parseInt(putIdxStr);
                		if (!"".equals(StringUtils.defaultString(prtWk._remark02_01, ""))) {
                    		final String[] putStrWk1 = KNJ_EditEdit.get_token(prtWk._remark02_01, 20, 3);
                    		////委員会活動・係活動
                    		for (int pCnt = 0;pCnt < putStrWk1.length;pCnt++) {
                                svf.VrsOutn("SP_ACT1_" + (pCnt+1), putIdx, putStrWk1[pCnt]);
                    		}
                		}
                		////学校行事
                        svf.VrsOutn("SP_ACT3", putIdx, prtWk._remark02_03);
                        //その他
                		if (!"P".equals(_param._schKind) && !"".equals(StringUtils.defaultString(prtWk._remark02_04, ""))) {
                		    final String[] putStrWk3 = KNJ_EditEdit.get_token(prtWk._remark02_04, 20, 3);
                		    for (int sCnt = 0;sCnt < putStrWk3.length;sCnt++) {
                		        svf.VrsOutn("SP_ACT4_" + (sCnt+1), putIdx, putStrWk3[sCnt]);
                		    }
                		}
                        ////所見
                		if (!"".equals(StringUtils.defaultString(prtWk._communication, ""))) {
                    		final String[] putStrWk2 = KNJ_EditEdit.get_token(prtWk._communication, 36, 7);
                            //prtWk._communication;
                    		for (int cCnt = 0;cCnt < putStrWk2.length;cCnt++) {
                                svf.VrsOutn("VIEW" + semester, cCnt+1, putStrWk2[cCnt]);
                    		}
                		}

                        //出欠の記録(備考)
                        final int arlen = KNJ_EditEdit.getMS932ByteLength(prtWk._attendrec_Remark);
                        final String arfield = arlen > 46 ? "2" : "1";
                        svf.VrsOutn("ATTEND_REMARK"+ arfield, putIdx, prtWk._attendrec_Remark);
                	}
            	}
        	}

        	//出欠席の記録
        	final Map subAttMap = (Map)attendMap.get(schregno);
        	for (Iterator itp = subAttMap.keySet().iterator();itp.hasNext();) {
        	    final String semester = (String)itp.next();
        	    //"9"学期のデータは、学年末以外出力しない。
        	    if (SEMEALL.equals(semester) && !_param._isLastSemester) continue;
                if (!SEMEALL.equals(semester) && Integer.parseInt(_param._semester) < Integer.parseInt(semester)) continue;
        	    final String putLine = SEMEALL.equals(semester) ? "4" : semester;    //"9学期"なら添え字を4で指定
        	    final Attendance att = (Attendance)subAttMap.get(semester);
        	    svf.VrsOutn("LESSON", Integer.parseInt(putLine), String.valueOf(att._lesson));
        	    svf.VrsOutn("SUSPEND", Integer.parseInt(putLine), String.valueOf(att._suspend + att._mourning));
        	    svf.VrsOutn("MUST", Integer.parseInt(putLine), String.valueOf(att._mLesson));
        	    svf.VrsOutn("NOTICE", Integer.parseInt(putLine), String.valueOf(att._absent));
        	    svf.VrsOutn("LATE", Integer.parseInt(putLine), String.valueOf(att._late));
        	    svf.VrsOutn("EARLY", Integer.parseInt(putLine), String.valueOf(att._early));
        	}
        	//担任印
            svf.VrsOut("STAFFBTM", _param.getImageStampFilePath(schInf._trCd1));

        	//フッター部
        	if (_param._isLastSemester) {
        		svf.VrsOut("END_GRADE", String.valueOf(Integer.parseInt(_param._grade)));
        		svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(_param._certifDate));
        		svf.VrsOut("JOB_NAME", _param._certifSchoolSchoolName + " " + _param._certifSchoolJobName + " " + _param._certifSchoolPrincipalName);
        		svf.VrsOut("SCHOOL_STAMP", _param._schoolStampPath);
        	} else {
        		svf.VrsOut("MASK", _param._whiteSpaceImagePath);
        	}
        	_hasData = true;
    		svf.VrEndPage();
    	}
    }


    private Map getSchregInfo(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        final String sql = getSchregInfoSql();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String grade = rs.getString("GRADE");
            	final String grade_Name2 = rs.getString("GRADE_NAME2");
            	final String hr_Class = rs.getString("HR_CLASS");
            	final String hr_Class_Name1 = rs.getString("HR_CLASS_NAME1");
            	final String trCd1 = rs.getString("TR_CD1");
            	final String attendno = rs.getString("ATTENDNO");
            	final String schregno = rs.getString("SCHREGNO");
            	final String name = rs.getString("NAME");
            	SchregInfo addwk = new SchregInfo(grade, grade_Name2, hr_Class, hr_Class_Name1, trCd1, attendno, schregno, name);
            	retMap.put(schregno, addwk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }
    private String getSchregInfoSql() {
    	StringBuffer stb = new StringBuffer();

    	final Semester fstSemes = (Semester)_param._semesterMap.get("1");  //1学期情報を取得する
    	final String sDate = fstSemes._dateRange._sdate;
    	final String eDate = _param._date;

        stb.append(" WITH SCHNO_A AS ( ");  // 在籍者数
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE  ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_REGD_HDAT TH ");
        stb.append("     ON TH.YEAR = T1.YEAR ");
        stb.append("    AND TH.SEMESTER = T1.SEMESTER ");
        stb.append("    AND TH.GRADE = T1.GRADE ");
        stb.append("    AND TH.HR_CLASS = T1.HR_CLASS ");
        stb.append("   LEFT JOIN SEMESTER_MST T2  ");
        stb.append("     ON T1.YEAR = T2.YEAR  ");
        stb.append("    AND T1.SEMESTER = T2.SEMESTER  ");
        stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._loginYear + "' ");
    	stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1  ");
        stb.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("                    AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + eDate + "' THEN T2.EDATE ELSE '" + sDate + "' END)  ");
        stb.append("                    OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + sDate + "' THEN T2.EDATE ELSE '" + sDate + "' END)) )  ");
        stb.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1  ");
        stb.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("                    AND S1.TRANSFERCD IN ('1','2') ");
        stb.append("                    AND CASE WHEN T2.EDATE < '" + sDate + "' THEN T2.EDATE ELSE '" + sDate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)  ");
        stb.append(" ) ");
    	stb.append(" SELECT ");
    	stb.append("  T1.GRADE, ");
    	stb.append("  T4.GRADE_NAME2, ");
    	stb.append("  T1.HR_CLASS, ");
    	stb.append("  T3.HR_CLASS_NAME1, ");
    	stb.append("  T3.TR_CD1, ");
    	stb.append("  T1.ATTENDNO, ");
    	stb.append("  T1.SCHREGNO, ");
    	stb.append("  T2.NAME ");
    	stb.append(" FROM ");
    	stb.append("  SCHNO_A T1 ");
    	stb.append("  LEFT JOIN SCHREG_BASE_MST T2 ");
    	stb.append("    ON T2.SCHREGNO = T1.SCHREGNO ");
    	stb.append("  LEFT JOIN SCHREG_REGD_HDAT T3 ");
    	stb.append("    ON T3.YEAR = T1.YEAR ");
    	stb.append("   AND T3.SEMESTER = T1.SEMESTER ");
    	stb.append("   AND T3.GRADE = T1.GRADE ");
    	stb.append("   AND T3.HR_CLASS = T1.HR_CLASS ");
    	stb.append("  LEFT JOIN SCHREG_REGD_GDAT T4 ");
    	stb.append("    ON T4.YEAR = T1.YEAR ");
    	stb.append("   AND T4.GRADE = T1.GRADE ");
    	stb.append(" WHERE ");
    	stb.append("  T1.YEAR = '" + _param._loginYear + "' ");
    	stb.append("  AND T1.SEMESTER = '" + _param._semester + "' ");
    	if (DISP2.equals(_param._disp)) {
    	    stb.append("  AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
    	} else {
    	    stb.append("  AND T1.GRADE || T1.HR_CLASS = " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
    	}
    	stb.append(" ORDER BY ");
    	stb.append("  T1.GRADE, ");
    	stb.append("  T1.HR_CLASS, ");
    	stb.append("  T1.ATTENDNO ");
    	return stb.toString();
    }
    private Map getHyoteiInfo(final DB2UDB db2, final List schregNos) {
        final Map retMap = new LinkedMap();
        Map subMap = null;
        Map detailMap = null;
        final String sql = getHyoteiInfoSql(schregNos);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String schregno = rs.getString("SCHREGNO");
            	final String semester = rs.getString("SEMESTER");
            	final String classcd = rs.getString("CLASSCD");
            	final String school_Kind = rs.getString("SCHOOL_KIND");
            	final String curriculum_Cd = rs.getString("CURRICULUM_CD");
            	final String subclasscd = rs.getString("SUBCLASSCD");
            	final String subclassordername2 = rs.getString("SUBCLASSORDERNAME2");
            	final String score = rs.getString("SCORE");
            	HyoteiInfo addwk = new HyoteiInfo(semester, classcd, school_Kind, curriculum_Cd, subclasscd, subclassordername2, score);
            	final String fstKey = schregno;
            	if (!retMap.containsKey(fstKey)) {
            		subMap = new LinkedMap();
            		retMap.put(fstKey, subMap);
            	}
            	subMap = (Map)retMap.get(fstKey);
            	final String sndKey = addwk.getSubclassCd();
            	if (!subMap.containsKey(sndKey)) {
            		detailMap = new LinkedMap();
                	subMap.put(sndKey, detailMap);
            	}
            	detailMap = (Map)subMap.get(sndKey);
            	detailMap.put(semester, addwk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }
    private String getHyoteiInfoSql(final List schregNos) {
    	final StringBuffer stb = new StringBuffer();
    	stb.append(" SELECT ");
    	stb.append("   T1.SCHREGNO, ");
    	stb.append("   T1.SEMESTER, ");
    	stb.append("   T1.CLASSCD, ");
    	stb.append("   T1.SCHOOL_KIND, ");
    	stb.append("   T1.CURRICULUM_CD, ");
    	stb.append("   T1.SUBCLASSCD, ");
    	stb.append("   T2.SUBCLASSORDERNAME2, ");
    	stb.append("   T1.SCORE ");
    	stb.append(" FROM ");
    	stb.append("   RECORD_RANK_SDIV_DAT T1 ");
    	stb.append("   LEFT JOIN SUBCLASS_MST T2 ");
    	stb.append("     ON T2.CLASSCD = T1.CLASSCD ");
    	stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	stb.append("    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
    	stb.append("    AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._loginYear + "' ");
    	stb.append("   AND T1.SEMESTER <= '" + (_param._isLastSemester ? SEMEALL : _param._semester) + "' ");
    	stb.append("   AND T1.SEMESTER <> '3' ");  //3学期データは不要
    	stb.append("   AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '990008' ");
    	stb.append("   AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, (String[])schregNos.toArray(new String[schregNos.size()])) + " ");
    	stb.append("   AND T1.SUBCLASSCD NOT IN ('"+ALL3+"', '" + ALL5 + "', '" + ALL9 + "', '" + ALL9A + "', '" + ALL9B + "') ");
    	stb.append(" ORDER BY ");
    	stb.append("   T1.SEMESTER, ");
    	stb.append("   T1.CLASSCD, ");
    	stb.append("   T1.SCHOOL_KIND, ");
    	stb.append("   T1.CURRICULUM_CD, ");
    	stb.append("   T1.SUBCLASSCD ");
    	return stb.toString();
    }
    private Map getRemarkInfo(final DB2UDB db2, final List schregNos) {
        final Map retMap = new LinkedMap();
        final String sql = getRemarkInfoSql(schregNos);
        Map subMap = null;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String schregno = rs.getString("SCHREGNO");
            	final String semester = rs.getString("SEMESTER");
            	final String remark01_01 = rs.getString("REMARK01_01");
            	final String remark01_02 = rs.getString("REMARK01_02");
            	final String remark01_03 = rs.getString("REMARK01_03");
            	final String remark02_02 = rs.getString("REMARK02_02");
            	final String remark02_01 = rs.getString("REMARK02_01");
            	final String remark02_03 = rs.getString("REMARK02_03");
            	final String remark02_04 = rs.getString("REMARK02_04");
            	final String attendrec_Remark = rs.getString("ATTENDREC_REMARK");
            	final String communication = rs.getString("COMMUNICATION");
            	RemarkInfo addwk = new RemarkInfo(schregno, semester, remark01_01, remark01_02, remark01_03, remark02_02, remark02_01, remark02_03, remark02_04, attendrec_Remark, communication);
            	if (!retMap.containsKey(schregno)) {
            		subMap = new LinkedMap();
            		retMap.put(schregno, subMap);
            	}
            	subMap = (Map)retMap.get(schregno);
            	subMap.put(semester, addwk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }
    private String getRemarkInfoSql(final List schregNos) {
    	final StringBuffer stb = new StringBuffer();
    	stb.append(" SELECT ");
    	stb.append("   T1.SCHREGNO, ");
    	stb.append("   T1.SEMESTER AS SEMESTER, ");
    	stb.append("   T2_R0101.REMARK1 AS REMARK01_01, ");
    	stb.append("   T2_R0102.REMARK1 AS REMARK01_02, ");
    	stb.append("   T2_R0103.REMARK1 AS REMARK01_03, ");
    	stb.append("   T2_R0202.REMARK1 AS REMARK02_02, ");
    	stb.append("   T2_R0201.REMARK1 AS REMARK02_01, ");
    	stb.append("   T2_R0203.REMARK1 AS REMARK02_03, ");
    	stb.append("   T2_R0204.REMARK1 AS REMARK02_04, ");
    	stb.append("   T1.ATTENDREC_REMARK AS ATTENDREC_REMARK, ");
    	stb.append("   T1.COMMUNICATION AS COMMUNICATION ");
    	stb.append(" FROM ");
    	stb.append("   HREPORTREMARK_DAT T1 ");
    	stb.append("   LEFT JOIN HREPORTREMARK_DETAIL_DAT T2_R0101 ");
    	stb.append("     ON T2_R0101.YEAR = T1.YEAR ");
    	stb.append("    AND T2_R0101.SEMESTER = '9' ");
    	stb.append("    AND T2_R0101.SCHREGNO = T1.SCHREGNO ");
    	stb.append("    AND T2_R0101.DIV = '01' ");
    	stb.append("    AND T2_R0101.CODE = '01' ");
    	stb.append("   LEFT JOIN HREPORTREMARK_DETAIL_DAT T2_R0102 ");
    	stb.append("     ON T2_R0102.YEAR = T1.YEAR ");
    	stb.append("    AND T2_R0102.SEMESTER = '9' ");
    	stb.append("    AND T2_R0102.SCHREGNO = T1.SCHREGNO ");
    	stb.append("    AND T2_R0102.DIV = '01' ");
    	stb.append("    AND T2_R0102.CODE = '02' ");
    	stb.append("   LEFT JOIN HREPORTREMARK_DETAIL_DAT T2_R0103 ");
    	stb.append("     ON T2_R0103.YEAR = T1.YEAR ");
    	stb.append("    AND T2_R0103.SEMESTER = '9' ");
    	stb.append("    AND T2_R0103.SCHREGNO = T1.SCHREGNO ");
    	stb.append("    AND T2_R0103.DIV = '01' ");
    	stb.append("    AND T2_R0103.CODE = '03' ");
    	stb.append("   LEFT JOIN HREPORTREMARK_DETAIL_DAT T2_R0202 ");
    	stb.append("     ON T2_R0202.YEAR = T1.YEAR ");
    	stb.append("    AND T2_R0202.SEMESTER = '9' ");
    	stb.append("    AND T2_R0202.SCHREGNO = T1.SCHREGNO ");
    	stb.append("    AND T2_R0202.DIV = '02' ");
    	stb.append("    AND T2_R0202.CODE = '02' ");
    	stb.append("   LEFT JOIN HREPORTREMARK_DETAIL_DAT T2_R0201 ");
    	stb.append("     ON T2_R0201.YEAR = T1.YEAR ");
    	stb.append("    AND T2_R0201.SEMESTER = T1.SEMESTER ");
    	stb.append("    AND T2_R0201.SEMESTER <= '" + _param._semester + "' ");
    	stb.append("    AND T2_R0201.SCHREGNO = T1.SCHREGNO ");
    	stb.append("    AND T2_R0201.DIV = '02' ");
    	stb.append("    AND T2_R0201.CODE = '01' ");
    	stb.append("   LEFT JOIN HREPORTREMARK_DETAIL_DAT T2_R0203 ");
    	stb.append("     ON T2_R0203.YEAR = T1.YEAR ");
    	stb.append("    AND T2_R0203.SEMESTER = T1.SEMESTER ");
    	stb.append("    AND T2_R0203.SEMESTER <= '" + _param._semester + "' ");
    	stb.append("    AND T2_R0203.SCHREGNO = T1.SCHREGNO ");
    	stb.append("    AND T2_R0203.DIV = '02' ");
    	stb.append("    AND T2_R0203.CODE = '03' ");
    	stb.append("   LEFT JOIN HREPORTREMARK_DETAIL_DAT T2_R0204 ");
    	stb.append("     ON T2_R0204.YEAR = T1.YEAR ");
    	stb.append("    AND T2_R0204.SEMESTER = T1.SEMESTER ");
    	stb.append("    AND T2_R0204.SEMESTER <= '" + _param._semester + "' ");
    	stb.append("    AND T2_R0204.SCHREGNO = T1.SCHREGNO ");
    	stb.append("    AND T2_R0204.DIV = '02' ");
    	stb.append("    AND T2_R0204.CODE = '04' ");
    	stb.append("   LEFT JOIN SCHREG_REGD_DAT T3 ");
    	stb.append("     ON T3.YEAR = T1.YEAR ");
    	stb.append("    AND T3.SEMESTER = T1.SEMESTER ");
    	stb.append("    AND T3.SCHREGNO = T1.SCHREGNO ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._loginYear + "' ");
    	stb.append("   AND T1.SEMESTER <= '" + _param._semester + "' ");
    	stb.append("   AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, (String[])schregNos.toArray(new String[schregNos.size()])) + " ");
    	stb.append(" ORDER BY ");
    	stb.append("   T3.GRADE, ");
    	stb.append("   T3.HR_CLASS, ");
    	stb.append("   T3.ATTENDNO ");
    	return stb.toString();
    }


    private class SchregInfo {
        final String _grade;
        final String _grade_Name2;
        final String _hr_Class;
        final String _hr_Class_Name1;
        final String _trCd1;
        final String _attendno;
        final String _schregno;
        final String _name;
        public SchregInfo (final String grade, final String grade_Name2, final String hr_Class, final String hr_Class_Name1, final String trCd1, final String attendno, final String schregno, final String name)
        {
            _grade = grade;
            _grade_Name2 = grade_Name2;
            _hr_Class = hr_Class;
            _hr_Class_Name1 = hr_Class_Name1;
            _trCd1 = trCd1;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
        }
    }

    private class HyoteiInfo {
        final String _semester;
        final String _classCd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclassCd;
        final String _subclassordername2;
        final String _score;
        public HyoteiInfo (final String semester, final String classcd, final String school_Kind, final String curriculum_Cd, final String subclasscd, final String subclassordername2, final String score)
        {
            _semester = semester;
            _classCd = classcd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclassCd = subclasscd;
            _subclassordername2 = subclassordername2;
            _score = score;
        }
        public String getSubclassCd() {
        	return _classCd + "-" + _school_Kind + "-" + _curriculum_Cd + "-" + _subclassCd;
        }
    }

    private class RemarkInfo {
        final String _schregno;
        final String _semester;
        final String _remark01_01;
        final String _remark01_02;
        final String _remark01_03;
        final String _remark02_02;
        final String _remark02_01;
        final String _remark02_03;
        final String _remark02_04;
        final String _attendrec_Remark;
        final String _communication;
        public RemarkInfo (final String schregno, final String semester,final String remark01_01, final String remark01_02, final String remark01_03, final String remark02_02, final String remark02_01, final String remark02_03, final String remark02_04, final String attendrec_Remark, final String communication)
        {
        	_schregno = schregno;
        	_semester = semester;
            _remark01_01 = remark01_01;
            _remark01_02 = remark01_02;
            _remark01_03 = remark01_03;
            _remark02_02 = remark02_02;
            _remark02_01 = remark02_01;
            _remark02_03 = remark02_03;
            _remark02_04 = remark02_04;
            _attendrec_Remark = attendrec_Remark;
            _communication = communication;
        }
    }

    private static class Attendance {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _abroad;
        final int _det006;
        final int _det007;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int abroad,
                final int det006,
                final int det007
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _abroad = abroad;
            _det006 = det006;
            _det007 = det007;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange,
                final Map setMap
        ) {
        	Map subMap = null;
            log.info(" attendance = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement psAtSeme = null;
            ResultSet rsAtSeme = null;
            PreparedStatement psAtDetail = null;
            ResultSet rsAtDetail = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._loginYear,
                        param._semester,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
//                log.debug(" attend sql = " + sql);
                psAtSeme = db2.prepareStatement(sql);

                final String detailSql = getDetailSql(param, dateRange);
                psAtDetail = db2.prepareStatement(detailSql);
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final String schregno = (String) it.next();

                    psAtDetail.setString(1, schregno);
                    psAtDetail.setString(2, schregno);
                    rsAtDetail = psAtDetail.executeQuery();

                    int set006 = 0;
                    int set007 = 0;
                    while (rsAtDetail.next()) {
                        set006 = rsAtDetail.getInt("CNT006");
                        set007 = rsAtDetail.getInt("CNT007");
                    }
                    DbUtils.closeQuietly(rsAtSeme);

                    psAtSeme.setString(1, schregno);
                    rsAtSeme = psAtSeme.executeQuery();

                    while (rsAtSeme.next()) {
                        if (!SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {
                            continue;
                        }

                        final Attendance attendance = new Attendance(
                                rsAtSeme.getInt("LESSON"),
                                rsAtSeme.getInt("MLESSON"),
                                rsAtSeme.getInt("SUSPEND"),
                                rsAtSeme.getInt("MOURNING"),
                                rsAtSeme.getInt("SICK"),
                                rsAtSeme.getInt("PRESENT"),
                                rsAtSeme.getInt("LATE"),
                                rsAtSeme.getInt("EARLY"),
                                rsAtSeme.getInt("TRANSFER_DATE"),
                                set006,
                                set007
                        );
                        if (!setMap.containsKey(schregno)) {
                        	subMap = new LinkedMap();
                        	setMap.put(schregno, subMap);
                        }
                        subMap = (Map)setMap.get(schregno);
                        subMap.put(dateRange._key, attendance);
                    }
                    DbUtils.closeQuietly(rsAtSeme);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(psAtSeme);
                db2.commit();
            }
        }

        private static String getDetailSql(final Param param, final DateRange dateRange) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCH_T(SCHREGNO) AS ( ");
            stb.append("     VALUES(CAST(? AS VARCHAR(8))) ");
            stb.append(" ), DET_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     SEQ, ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(CNT) AS CNT ");
            stb.append(" FROM ");
            stb.append("     ATTEND_SEMES_DETAIL_DAT ");
            stb.append(" WHERE ");
            stb.append("     COPYCD = '0' ");
            stb.append("     AND YEAR || MONTH BETWEEN '" + param._loginYear + "04' AND '" + (Integer.parseInt(param._loginYear) + 1) + "03' ");
            stb.append("     AND SEMESTER = '" + dateRange._key + "' ");
            stb.append("     AND SCHREGNO = ? ");
            stb.append("     AND SEQ IN ('006', '007') ");
            stb.append(" GROUP BY ");
            stb.append("     SEQ, ");
            stb.append("     SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     VALUE(DET006.CNT, 0) AS CNT006, ");
            stb.append("     VALUE(DET007.CNT, 0) AS CNT007 ");
            stb.append(" FROM ");
            stb.append("     SCH_T ");
            stb.append("     LEFT JOIN DET_T DET006 ON SCH_T.SCHREGNO = DET006.SCHREGNO ");
            stb.append("          AND DET006.SEQ = '006' ");
            stb.append("     LEFT JOIN DET_T DET007 ON SCH_T.SCHREGNO = DET007.SCHREGNO ");
            stb.append("          AND DET007.SEQ = '007' ");

            return stb.toString();
        }

    }






    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        final List _testItemList;
        final List _semesterDetailList;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
            _testItemList = new ArrayList();
            _semesterDetailList = new ArrayList();
        }
        public int getTestItemIdx(final TestItem testItem) {
            return _testItemList.indexOf(testItem);
        }
        public int getSemesterDetailIdx(final SemesterDetail semesterDetail) {
          log.debug(" semesterDetail = " + semesterDetail + " , " + _semesterDetailList.indexOf(semesterDetail));
          return _semesterDetailList.indexOf(semesterDetail);
        }

        public int compareTo(final Object o) {
        	if (!(o instanceof Semester)) {
        		return 0;
        	}
        	Semester s = (Semester) o;
        	return _semester.compareTo(s._semester);
        }
    }

    private static class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static class SemesterDetail implements Comparable {
        final Semester _semester;
        final String _cdSemesterDetail;
        final String _semestername;
        final String _sdate;
        final String _edate;
        public SemesterDetail(Semester semester, String cdSemesterDetail, String semestername, final String sdate, final String edate) {
            _semester = semester;
            _cdSemesterDetail = cdSemesterDetail;
            _semestername = StringUtils.defaultString(semestername);
            _sdate = sdate;
            _edate = edate;
        }
        public int compareTo(final Object o) {
        	if (!(o instanceof SemesterDetail)) {
        		return 0;
        	}
    		SemesterDetail sd = (SemesterDetail) o;
    		int rtn;
        	rtn = _semester.compareTo(sd._semester);
        	if (rtn != 0) {
        		return rtn;
        	}
        	rtn = _cdSemesterDetail.compareTo(sd._cdSemesterDetail);
        	return rtn;
        }
        public String toString() {
            return "SemesterDetail(" + _semester._semester + ", " + _cdSemesterDetail + ", " + _semestername + ")";
        }
    }

    private static class TestItem {
        final String _year;
        final Semester _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _testitemname;
        final String _testitemabbv1;
        final String _sidouInput;
        final String _sidouInputInf;
        final SemesterDetail _semesterDetail;
        boolean _isGakunenKariHyotei;
        int _printKettenFlg; // -1: 表示しない（仮評定）、1: 値が1をカウント（学年評定）、2:換算した値が1をカウント（評価等）
        public TestItem(final String year, final Semester semester, final String testkindcd, final String testitemcd, final String scoreDiv,
                final String testitemname, final String testitemabbv1, final String sidouInput, final String sidouInputInf,
                final SemesterDetail semesterDetail) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _testitemname = testitemname;
            _testitemabbv1 = testitemabbv1;
            _sidouInput = sidouInput;
            _sidouInputInf = sidouInputInf;
            _semesterDetail = semesterDetail;
        }
        public String getTestcd() {
            return _semester._semester +_testkindcd +_testitemcd + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + _semester._semester + _testkindcd + _testitemcd + "(" + _scoreDiv + "))";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75361 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _disp;  //クラス/個人選択ラジオ
        final String[] _categorySelected;  //選択情報
        final String _date;          //生徒の転校/退学チェック日
        final String _certifDate;    //修了日(フッターの日付)
        final String _gradeHrClass;  //年組(個人選択時のみ)
        final String _grade;         //学年(クラス/個人どちらでも利用)
        final String _gradeCd;       //学年(上記の_gradeは学年コードなので、チェック時はこちらを利用)
        final String _loginSemester; //CTRL_SEMESTER
        final String _loginYear;     //YEAR
        final String _prgid;
        final String _semester;      //学期
        final boolean _isLastSemester; //最終学期か?
        final boolean _isPrimary;      //プライマリー(学年が小学1～4年)か?
        final boolean _isJuniorHigh;   //"07"学年以上(中学)か?
        final String _schKind;          //校種

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolEngSchoolName;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private final List _semesterList;
        private Map _semesterMap;
        private final Map _semesterDetailMap;
        private List _d026List = new ArrayList();
        private Map _attendRanges;

        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _backSlashImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolStampPath;

        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;

        private final List _attendTestKindItemList;

        final Map _stampMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
        	_disp = request.getParameter("DISP");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _date = request.getParameter("DATE").replace('/', '-');

            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            if ("1".equals(_disp)) {
                _grade = request.getParameter("GRADE");
            } else {
                _grade = _gradeHrClass.substring(0, 2);
            }
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginYear = request.getParameter("YEAR");
            _prgid = request.getParameter("PRGID");
            _semester = request.getParameter("SEMESTER");
            _schKind = request.getParameter("SCHOOLKIND");

            _gradeCd = getGradeCd(db2);
            loadNameMstD026(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);

            setCertifSchoolDat(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");


            _semesterList = getSemesterList(db2);
            _semesterMap = loadSemester(db2);
            _semesterDetailMap = new HashMap();
            _attendRanges = new HashMap();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final Semester oSemester = (Semester) _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }

            _isPrimary = _grade.compareTo("05") < 0;
            _isJuniorHigh = _grade.compareTo("07") >= 0;
            _isLastSemester = _semester.equals(getLastSemester());
            String setDateWk = "";
            if (_isLastSemester) {
            	setDateWk = request.getParameter("CERTIF_DATE");
            }
            _certifDate = setDateWk;

            _documentroot = request.getParameter("DOCUMENTROOT");
            _stampMap = getStampNoMap(db2);
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            final String stampSchKind = _isJuniorHigh ? "J" : "P";
            _schoolStampPath = getImageFilePath("SCHOOLSTAMP_" + stampSchKind + ".bmp");

            _attendTestKindItemList = getTestKindItemList(db2, false, false);
        }

        private String getGradeCd(final DB2UDB db2) {
            final String sql = " SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _loginYear + "' AND  GRADE = '" + _grade + "' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            final String getStr = KnjDbUtils.getString(row, "GRADE_CD");
            return "J".equals(_schKind) ? "0" + (Integer.parseInt(getStr) + 6) : getStr;
        }
        private String getLastSemester() {
        	int retSemes = 0;
        	for (Iterator ite = _semesterList.iterator();ite.hasNext();) {
        		Semester semeWk = (Semester)ite.next();
        		int chkSemes = Integer.parseInt(semeWk._semester);
        		if (retSemes < chkSemes && chkSemes != 9) {
        			retSemes = chkSemes;
        		}
        	}
        	return String.valueOf(retSemes);
        }
        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='" + _loginYear + "'"
                        + "   AND GRADE='" + _grade + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }


        private void setCertifSchoolDat(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK6 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _loginYear + "' AND CERTIF_KINDCD = '"+(_isJuniorHigh ? "117" : "103")+"' ");

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
            _certifSchoolEngSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK6"));
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        private Map getStampNoMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     STAFFCD, ");
            stb.append("     MAX(STAMP_NO) AS STAMP_NO ");
            stb.append(" FROM ");
            stb.append("     ATTEST_INKAN_DAT ");
            stb.append(" GROUP BY ");
            stb.append("     STAFFCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map retMap = new HashMap();
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String staffcd = rs.getString("STAFFCD");
                    final String stampNo = rs.getString("STAMP_NO");
                    retMap.put(staffcd, stampNo);
                }
            } catch (Exception e) {
                log.error("exception!", e);
                //ATTEST_INKAN_DATテーブルを利用していない学校では、エラーになる。別にテーブルが定義されていなくてもプログラム側で暫定処理があるので、
                //ATTEST_INKAN_DATテーブルを利用していない学校では、(SQL文法的なエラーでない(存在すればエラーにならない)なら)このエラーは無視可能。
                log.warn("THIS SCHOOL IS NOT USE ATTEST_INKAN_DAT. IF THIS SCHOOL IS NO USE ATTEST_INKAN_DAT, YOU CAN IGNORE ERROR.");
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        public String getImageStampFilePath(final String name) {
        	String path = "";
        	String useName = name;
        	if (_stampMap != null && _stampMap.containsKey(name)) {
        		useName = (String)_stampMap.get(name);
        	}
            path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/stamp/") + useName + ".bmp";
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (!exists) {
                return null;
            }
            return path;
        }

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _loginYear + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        /**
         * 合併先科目を印刷するか
         */
        private void setPrintSakiKamoku(final DB2UDB db2) {
            // 初期値：印刷する
            _isPrintSakiKamoku = true;
            // 名称マスタ「D021」「01」から取得する
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR='" + _loginYear+ "' AND NAMECD1 = 'D021' AND NAMECD2 = '01' "));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE3"))) {
                _isPrintSakiKamoku = false;
            }
            log.debug("合併先科目を印刷するか：" + _isPrintSakiKamoku);
        }

        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _loginYear + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");

            _d026List.clear();
            _d026List.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD"));
            log.info("非表示科目:" + _d026List);
        }

        private List getSemesterList(DB2UDB db2) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _loginYear + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    Semester semes = new Semester(semester, semestername, sdate, edate);
                    list.add(semes);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private List getTestKindItemList(DB2UDB db2, final boolean useSubclassControl, final boolean addSemester) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map semesterMap = new HashMap();
                for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                    final String seme = (String) it.next();
                    if(!_semesterMap.containsKey(seme)) continue;
                    final Semester semester = (Semester) _semesterMap.get(seme);
                    semesterMap.put(semester._semester, semester);
                }
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH ADMIN_CONTROL_SDIV_SUBCLASSCD AS (");
                if (useSubclassControl) {
                    stb.append("   SELECT DISTINCT ");
                    stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                    stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                    stb.append("   WHERE T1.YEAR = '" + _loginYear + "' ");
                    stb.append("   UNION ALL ");
                }
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                stb.append("   WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '00-00-00-000000' ");
                stb.append("   UNION ALL ");
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                stb.append("   WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("     AND T1.CLASSCD = '00' ");
                stb.append("     AND T1.CURRICULUM_CD = '00' ");
                stb.append("     AND T1.SUBCLASSCD = '000000' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.SCORE_DIV, ");
                stb.append("     T1.TESTITEMNAME, ");
                stb.append("     T1.TESTITEMABBV1, ");
                stb.append("     T1.SIDOU_INPUT, ");
                stb.append("     T1.SIDOU_INPUT_INF, ");
                stb.append("     T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T2.SEMESTER_DETAIL, ");
                stb.append("     T2.SEMESTER AS SEMESTER_DETAIL_SEMESTER, ");
                stb.append("     T2.SEMESTERNAME AS SEMESTERDETAILNAME, ");
                stb.append("     T2.SDATE, ");
                stb.append("     T2.EDATE ");
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                    stb.append(" INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                stb.append("    AND T11.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T11.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("    AND T11.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("    AND T11.SCORE_DIV = T1.SCORE_DIV ");
                stb.append(" LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ");
                stb.append(" WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("   AND T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD IN "); // 指定の科目が登録されていれば登録された科目、登録されていなければ00-00-00-000000を使用する
                stb.append("    (SELECT MAX(SUBCLASSCD) FROM ADMIN_CONTROL_SDIV_SUBCLASSCD) ");
                stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                String adminSdivSubclasscd = null;
                while (rs.next()) {
                    adminSdivSubclasscd = rs.getString("SUBCLASSCD");
                    final String year = rs.getString("YEAR");
                    final String testkindcd = rs.getString("TESTKINDCD");
                    final String testitemcd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String sidouInput = rs.getString("SIDOU_INPUT");
                    final String sidouInputInf = rs.getString("SIDOU_INPUT_INF");
                    Semester semester = (Semester) semesterMap.get(rs.getString("SEMESTER"));
                    if (null == semester) {
                        continue;
                    }
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final String testitemabbv1 = rs.getString("TESTITEMABBV1");
                    SemesterDetail semesterDetail = null;
                    final String cdsemesterDetail = rs.getString("SEMESTER_DETAIL");
                    if (null != cdsemesterDetail) {
                        if (_semesterDetailMap.containsKey(cdsemesterDetail)) {
                            semesterDetail = (SemesterDetail) _semesterDetailMap.get(cdsemesterDetail);
                        } else {
                            final String semesterdetailname = rs.getString("SEMESTERDETAILNAME");
                            final String sdate = rs.getString("SDATE");
                            final String edate = rs.getString("EDATE");
                            semesterDetail = new SemesterDetail(semester, cdsemesterDetail, semesterdetailname, sdate, edate);
                            Semester semesDetailSemester = (Semester) semesterMap.get(rs.getString("SEMESTER_DETAIL_SEMESTER"));
                            if (null != semesDetailSemester) {
                                semesDetailSemester._semesterDetailList.add(semesterDetail);
                            }
                            _semesterDetailMap.put(semesterDetail._cdSemesterDetail, semesterDetail);
                        }
                    }
                    final TestItem testItem = new TestItem(
                            year, semester, testkindcd, testitemcd, scoreDiv, testitemname, testitemabbv1, sidouInput, sidouInputInf,
                            semesterDetail);
                    final boolean isGakunenHyotei = HYOTEI_TESTCD.equals(testItem.getTestcd());
                    if (isGakunenHyotei) {
                        testItem._printKettenFlg = 1;
                    } else if (!SEMEALL.equals(semester._semester) && "09".equals(scoreDiv)) { // 9学期以外の09=9学期以外の仮評定
                        testItem._printKettenFlg = -1;
                    } else {
                        testItem._printKettenFlg = 2;
                    }
                    if (isGakunenHyotei) {
                        final TestItem testItemKari = new TestItem(
                                year, semester, testkindcd, testitemcd, scoreDiv, "仮評定", "仮評定", sidouInput, sidouInputInf,
                                semesterDetail);
                        testItemKari._printKettenFlg = -1;
                        testItemKari._isGakunenKariHyotei = true;
                        if (addSemester) {
                            semester._testItemList.add(testItemKari);
                        }
                        list.add(testItemKari);
                    }
                    if (addSemester) {
                        semester._testItemList.add(testItem);
                    }
                    list.add(testItem);
                }
                log.debug(" testitem admin_control_sdiv_dat subclasscd = " + adminSdivSubclasscd);

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.debug(" testcd = " + list);
            return list;
        }
    }
}

// eof
