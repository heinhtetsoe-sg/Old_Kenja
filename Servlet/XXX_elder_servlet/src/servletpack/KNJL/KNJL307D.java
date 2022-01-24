// kanji=漢字
/*
 * $Id: ba7b5b0181d9dd931f844d443a9b0ce92416c11c $
 *
 * 作成日: 2011/03/08 15:37:18 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.AbstractXlsExt;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: ba7b5b0181d9dd931f844d443a9b0ce92416c11c $
 */
public class KNJL307D extends AbstractXlsExt {

    private static final Log log = LogFactory.getLog("KNJL307D.class");

    private boolean _hasData;

    Param _param;

    private int _sheetno;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void xls_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        _param = createParam(_db2, request);
        //テンプレートの読込&初期化
        setIniTmpBook(_param._templatePath);

        final List subjectstrlist = getSubjectString(_db2);
        _strtline = 3;
        _strtcol = 0;
        for (_sheetno = 0; _sheetno < 6; _sheetno++) {
            log.debug(" __headData__");
            //ヘッダデータ取得
            _headList = setHeadData(subjectstrlist);

            log.debug(" __getData__");
            //出力データ取得
            _dataList = getXlsDataList();
            if (!_dataList.isEmpty()) {
            	_hasData = true;
            }

            final String shtname = getSheetName(_sheetno);
            log.debug(" __outPutSht_STRT__");
            outPutSht(_sheetno, shtname, false);
            log.debug(" __outPutSht_END__");
        }

        if (_hasData) {
        	sendXlsFile(response, "成績一覧.xlsx");
        } else {
        	setWarning(response, "MSG303", null);
        }
    }

    private List getSubjectString(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
     	    final String subjectsql = getSubjectSql();
            log.debug(" subjectsql =" + subjectsql);
            ps = db2.prepareStatement(subjectsql);
            rs = ps.executeQuery();
            while (rs.next()) {
                retList.add(rs.getString("NAME1"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSubjectSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L008' AND NAMECD2 IN ('01', '02', '03', '04', '09') ORDER BY NAMECD2 ");
        return stb.toString();
    }

    private String getSheetName(final int shtno) {
    	String retstr = "";
    	switch (shtno) {
    	case 0:
        	retstr = "合格類型別総合成績順";
    		break;
    	case 1:
        	retstr = "中学校別受験番号順";
    		break;
    	case 2:
        	retstr = "総合成績順";
    		break;
    	case 3:
        	retstr = "志望類型成績順";
    		break;
    	case 4:
        	retstr = "内申点順";
    		break;
    	case 5:
        	retstr = "受験番号順";
    		break;
    	default:
    		break;
    	}
    	return retstr;
    }

	protected MultiHashMap getHeadData() {
        return _headList;
	}

	protected MultiHashMap setHeadData(final List setlist) {
        MultiHashMap retList = new MultiHashMap();
        int ttlcnt = 0;

        if (setlist.size() >= 1) {
            final String[] List1Info1 = {"3","6",(String)setlist.get(0)};
            retList.put(String.valueOf(ttlcnt++), List1Info1);
            final String[] List1Info2 = {"3","10",(String)setlist.get(0)};
            retList.put(String.valueOf(ttlcnt++), List1Info2);
        }
        if (setlist.size() >= 2) {
            final String[] List1Info1 = {"3","11",(String)setlist.get(1)};
            retList.put(String.valueOf(ttlcnt++), List1Info1);
        }
        if (setlist.size() >= 3) {
            final String[] List1Info1 = {"3","7",(String)setlist.get(2)};
            retList.put(String.valueOf(ttlcnt++), List1Info1);
            final String[] List1Info2 = {"3","12",(String)setlist.get(2)};
            retList.put(String.valueOf(ttlcnt++), List1Info2);
        }
        if (setlist.size() >= 4) {
            final String[] List1Info1 = {"3","13",(String)setlist.get(3)};
            retList.put(String.valueOf(ttlcnt++), List1Info1);
        }
        if (setlist.size() >= 5) {
            final String[] List1Info1 = {"3","8",(String)setlist.get(4)};
            retList.put(String.valueOf(ttlcnt++), List1Info1);
            final String[] List1Info2 = {"3","14",(String)setlist.get(4)};
            retList.put(String.valueOf(ttlcnt++), List1Info2);
        }

        return retList;
    }

    protected String[] getCols() {

    	final String[] cols = {
        		"TOTALRANK1",
                "TOTALRANK2",
                "TOTALRANK3",
                "TOTALSCORE1",
                "TOTALSCORE2",
                "ASSESSMARK",
                "J_LANG_SCORE",
                "MATH_SCORE",
                "E_LANG_SCORE",
                "SCORE_TOTAL",
                "J_LANG_REPSCORE",
                "SOCIAL_REPSCORE",
                "MATH_REPSCORE",
                "SCIENCE_REPSCORE",
                "E_LANG_REPSCORE",
                "TOTAL5",
                "TOTAL_ALL",
                "INTERVIEW_VALUE",
                "ACTIONREPORT",
                "ABSENCE_DAYS",
                "ABSENCE_DAYS2",
                "ABSENCE_DAYS3",
                "SPECIALACTION",
                "DESIREDIVNM",
                "JUDGEMENTNM",
                "SCHOLARSHIP",
                "ENTDIVNM",
                "EXAMNO",
                "NAME",
                "SEXNM",
                "FSNM_ABBV",
                "ABSENCE_DIVNM",
                "SHDIVNM",
                "DECISIONNM",
                "JGDKINDNM",
                "CLUBNM",
                "RONIN",
                "SELECTION1",
                "SELECTION2",
                "REMARK"
                };
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH ");
        stb.append("LEVEL_TBL AS ( ");
        stb.append("SELECT T1.EXAMNO, T1.ENTEXAMYEAR, T1.APPLICANTDIV, T1.TESTDIV, R1.TOTAL2, ASS1.ASSESSMARK ");
        stb.append("FROM ");
        stb.append(" ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append(" LEFT JOIN ENTEXAM_RECEPT_DAT R1 ");
        stb.append("   ON T1.EXAMNO = R1.RECEPTNO ");
        stb.append("   AND T1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("   AND T1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("   AND T1.TESTDIV = R1.TESTDIV ");
        stb.append("   AND R1.EXAM_TYPE = '1' ");
        stb.append(" LEFT JOIN ENTEXAM_ASSESS_MST ASS1 ");
        stb.append("   ON T1.ENTEXAMYEAR = ASS1.ENTEXAMYEAR ");
        stb.append("WHERE R1.TOTAL2 >= ASS1.ASSESSLOW AND R1.TOTAL2 <= ASS1.ASSESSHIGH ");
        stb.append("ORDER BY T1.EXAMNO ASC ");
        stb.append(") ");
        stb.append(" SELECT ");
        //順位(総合)
        stb.append(" RANK() OVER(ORDER BY R1.TOTAL_RANK2) AS TOTALRANK1, ");
        //順位(学科試験)
        stb.append(" RANK() OVER(ORDER BY R1.TOTAL_RANK4) AS TOTALRANK2, ");
        //順位(内申点)
        stb.append(" RANK() OVER(ORDER BY R1.TOTAL_RANK1) AS TOTALRANK3, ");
        //総合得点(得点)
        stb.append(" R1.TOTAL2 AS TOTALSCORE1, ");
        //総合得点(偏差値)
        stb.append(" R1.JUDGE_DEVIATION AS TOTALSCORE2, ");
        //総合得点(レンジ)
        stb.append(" LVL_TBL.ASSESSMARK AS ASSESSMARK, ");
        //学科得点(国語)
        stb.append(" D1_J_LANG.SCORE AS J_LANG_SCORE, ");
        //学科得点(数学)
        stb.append(" D1_MATH.SCORE AS MATH_SCORE, ");
        //学科得点(英語)
        stb.append(" D1_E_LANG.SCORE AS E_LANG_SCORE, ");
        //学科得点(合計)
        stb.append("    (D1_J_LANG.SCORE + D1_MATH.SCORE + D1_E_LANG.SCORE) AS SCORE_TOTAL, ");
        //内申点(国語)
        stb.append(" (INTEGER(T4_01.REMARK1) + INTEGER(T4_02.REMARK1) + INTEGER(T3.CONFIDENTIAL_RPT01)) AS J_LANG_REPSCORE, ");
        //内申点(社会)
        stb.append(" (INTEGER(T4_01.REMARK2) + INTEGER(T4_02.REMARK2) + INTEGER(T3.CONFIDENTIAL_RPT02)) AS SOCIAL_REPSCORE, ");
        //内申点(数学)
        stb.append(" (INTEGER(T4_01.REMARK3) + INTEGER(T4_02.REMARK3) + INTEGER(T3.CONFIDENTIAL_RPT03)) AS MATH_REPSCORE, ");
        //内申点(理科)
        stb.append(" (INTEGER(T4_01.REMARK4) + INTEGER(T4_02.REMARK4) + INTEGER(T3.CONFIDENTIAL_RPT04)) AS SCIENCE_REPSCORE, ");
        //内申点(英語)
        stb.append(" (INTEGER(T4_01.REMARK9) + INTEGER(T4_02.REMARK9) + INTEGER(T3.CONFIDENTIAL_RPT09)) AS E_LANG_REPSCORE, ");
        //内申点(5教科合計)
        stb.append(" (INTEGER(T4_01.REMARK10) + INTEGER(T4_02.REMARK10) + T3.TOTAL5) AS TOTAL5, ");
        //内申点(9教科合計)
        stb.append(" (INTEGER(T4_01.REMARK11) + INTEGER(T4_02.REMARK11) + T3.TOTAL_ALL) AS TOTAL_ALL, ");
        //面談
        stb.append(" INTVIEW1.INTERVIEW_VALUE AS INTERVIEW_VALUE, ");
        //行動の記録
        stb.append(" T4_03.REMARK1 AS ACTIONREPORT, ");
        //欠席日数(1年)
        stb.append(" T3.ABSENCE_DAYS AS ABSENCE_DAYS, ");
        //欠席日数(2年)
        stb.append(" T3.ABSENCE_DAYS2 AS ABSENCE_DAYS2, ");
        //欠席日数(3年)
        stb.append(" T3.ABSENCE_DAYS3 AS ABSENCE_DAYS3, ");
        //特別活動の記録
        stb.append(" T4_03.REMARK5 AS SPECIALACTION, ");
        //志望類型
        stb.append(" NM_DESIRE.NAME1 AS DESIREDIVNM, ");
        //合格類型
        stb.append(" NM_JUDGEMENT.NAME1 AS JUDGEMENTNM, ");
        //特待生採用
        //stb.append(" CASE WHEN T1.JUDGE_KIND IS NOT NULL THEN '〇' ELSE '' END AS SCHOLARSHIP, ");
        stb.append(" L025.ABBV1 AS SCHOLARSHIP, ");
        //入学類型
        stb.append(" NM_ENTDIV.NAME1 AS ENTDIVNM, ");
        //受験番号
        stb.append(" T1.EXAMNO AS EXAMNO, ");
        //氏名
        if (!"1".equals(_param._nameflg)) {
            stb.append(" T1.NAME AS NAME, ");
        } else {
            stb.append(" '' AS NAME, ");
        }
        //性別
        stb.append(" NM_SEX.NAME1 AS SEXNM, ");
        //出身中学(略称)
        stb.append(" FSM.FINSCHOOL_NAME_ABBV AS FSNM_ABBV, ");
        //試験欠席
        stb.append(" CASE WHEN T1.JUDGEMENT = '4' THEN NM_ABSENCE.NAME1 ELSE '' END AS ABSENCE_DIVNM, ");
        //志望専願併願
        stb.append(" NM_SHDIV.NAME1 AS SHDIVNM, ");
        //内部専願併願
        stb.append(" DEC_MST.DECISION_NAME AS DECISIONNM, ");
        //特待生種別
        stb.append(" NM_JGDKIND.NAME1 AS JGDKINDNM, ");
        //教科クラブ名
        stb.append(" CLUB_M.CLUBNAME AS CLUBNM, ");
        //浪人年数
        stb.append(" (CASE WHEN " + _param._loginYear + " <= INTEGER(T1.FS_GRDYEAR) THEN '' ELSE CHAR(" + _param._loginYear + " - INTEGER(T1.FS_GRDYEAR)) END) AS RONIN, ");
        //選抜Ⅰ
        stb.append(" TD1.REMARK1 AS SELECTION1, ");
        //選抜Ⅱ
        stb.append(" TD1.REMARK2 AS SELECTION2, ");
        //備考
        stb.append(" '' AS REMARK");
        stb.append(" FROM ");
        stb.append("    ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ");
        stb.append("      ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("      AND T1.EXAMNO = T2.EXAMNO ");
        stb.append("      AND T2.SEQ = '019' ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T3 ");
        stb.append("      ON T1.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = T3.APPLICANTDIV ");
        stb.append("      AND T1.EXAMNO = T3.EXAMNO ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T4_01 ");
        stb.append("      ON T1.ENTEXAMYEAR = T4_01.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = T4_01.APPLICANTDIV ");
        stb.append("      AND T1.EXAMNO = T4_01.EXAMNO ");
        stb.append("      AND T4_01.SEQ = '001' ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T4_02 ");
        stb.append("      ON T1.ENTEXAMYEAR = T4_02.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = T4_02.APPLICANTDIV ");
        stb.append("      AND T1.EXAMNO = T4_02.EXAMNO ");
        stb.append("      AND T4_02.SEQ = '002' ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T4_03 ");
        stb.append("      ON T1.ENTEXAMYEAR = T4_03.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = T4_03.APPLICANTDIV ");
        stb.append("      AND T1.EXAMNO = T4_03.EXAMNO ");
        stb.append("      AND T4_03.SEQ = '003' ");
        stb.append("    LEFT JOIN NAME_MST NM_SHDIV ON NM_SHDIV.NAMECD1 = 'L006' AND NM_SHDIV.NAMECD2 = T1.SHDIV ");
        stb.append("    LEFT JOIN NAME_MST NM_ENTDIV ON NM_ENTDIV.NAMECD1 = 'L012' AND NM_ENTDIV.NAMECD2 = T1.ENTDIV ");
        stb.append("    LEFT JOIN NAME_MST NM_ABSENCE ON NM_ABSENCE.NAMECD1 = 'L013' AND NM_ABSENCE.NAMECD2 = '4' ");
        stb.append("    LEFT JOIN NAME_MST NM_SEX ON NM_SEX.NAMECD1 = 'Z002' AND NM_SEX.NAMECD2 = T1.SEX ");
        stb.append("    LEFT JOIN NAME_MST NM_DESIRE ON NM_DESIRE.NAMECD1 = 'L058' AND NM_DESIRE.NAMECD2 = T1.DESIREDIV ");
        stb.append("    LEFT JOIN NAME_MST NM_JUDGEMENT ON NM_JUDGEMENT.NAMECD1 = 'L013' AND NM_JUDGEMENT.NAMECD2 = T1.JUDGEMENT ");
        stb.append("    LEFT JOIN NAME_MST NM_JGDKIND ON NM_SHDIV.NAMECD1 = 'L025' AND NM_SHDIV.NAMECD2 = T1.SHDIV ");
        stb.append("    LEFT JOIN FINSCHOOL_MST FSM ON T1.FS_CD = FSM.FINSCHOOLCD ");
        stb.append("    LEFT JOIN ENTEXAM_INTERNAL_DECISION_MST AS DEC_MST ON DEC_MST.DECISION_CD = T1.SUB_ORDER ");
        stb.append("    LEFT JOIN CLUB_MST CLUB_M ");
        stb.append("      ON T2.REMARK1 = CLUB_M.SCHOOLCD ");
        stb.append("      AND T2.REMARK2 = CLUB_M.SCHOOL_KIND ");
        stb.append("      AND T2.REMARK3 = CLUB_M.CLUBCD ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT TD1 ");
        stb.append("      ON T1.EXAMNO = TD1.EXAMNO ");
        stb.append("      AND T1.ENTEXAMYEAR = TD1.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = TD1.APPLICANTDIV ");
        stb.append("      AND TD1.SEQ = '005' ");
        stb.append("    LEFT JOIN ENTEXAM_INTERVIEW_DAT AS INTVIEW1 ");
        stb.append("      ON T1.EXAMNO = INTVIEW1.EXAMNO ");
        stb.append("      AND T1.ENTEXAMYEAR = INTVIEW1.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = INTVIEW1.APPLICANTDIV ");
        stb.append("      AND T1.TESTDIV = INTVIEW1.TESTDIV ");
        stb.append("    LEFT JOIN ENTEXAM_RECEPT_DAT AS R1 ");
        stb.append("      ON T1.EXAMNO = R1.RECEPTNO ");
        stb.append("      AND T1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("      AND T1.TESTDIV = R1.TESTDIV ");
        stb.append("      AND R1.EXAM_TYPE = '1' ");
        stb.append("    LEFT JOIN ENTEXAM_SCORE_DAT D1_J_LANG ");
        stb.append("      ON T1.ENTEXAMYEAR = D1_J_LANG.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = D1_J_LANG.APPLICANTDIV ");
        stb.append("      AND T1.TESTDIV = D1_J_LANG.TESTDIV ");
        stb.append("      AND T1.EXAMNO = D1_J_LANG.RECEPTNO ");
        stb.append("      AND D1_J_LANG.TESTSUBCLASSCD = '1' ");
        stb.append("    LEFT JOIN ENTEXAM_SCORE_DAT D1_MATH ");
        stb.append("      ON T1.ENTEXAMYEAR = D1_MATH.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = D1_MATH.APPLICANTDIV ");
        stb.append("      AND T1.TESTDIV = D1_MATH.TESTDIV ");
        stb.append("      AND T1.EXAMNO = D1_MATH.RECEPTNO ");
        stb.append("      AND D1_MATH.TESTSUBCLASSCD = '2' ");
        stb.append("    LEFT JOIN ENTEXAM_SCORE_DAT D1_E_LANG ");
        stb.append("      ON T1.ENTEXAMYEAR = D1_E_LANG.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = D1_E_LANG.APPLICANTDIV ");
        stb.append("      AND T1.TESTDIV = D1_E_LANG.TESTDIV ");
        stb.append("      AND T1.EXAMNO = D1_E_LANG.RECEPTNO ");
        stb.append("      AND D1_E_LANG.TESTSUBCLASSCD = '3' ");
        stb.append("    LEFT JOIN LEVEL_TBL LVL_TBL");
        stb.append("      ON T1.ENTEXAMYEAR = LVL_TBL.ENTEXAMYEAR ");
        stb.append("      AND T1.APPLICANTDIV = LVL_TBL.APPLICANTDIV ");
        stb.append("      AND T1.TESTDIV = LVL_TBL.TESTDIV ");
        stb.append("      AND T1.EXAMNO = LVL_TBL.EXAMNO ");
        stb.append("    LEFT JOIN NAME_MST L025 ");
        stb.append("      ON L025.NAMECD1 = 'L025' ");
        stb.append("      AND L025.NAMECD2 = T1.JUDGE_KIND ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + _param._examYear +  "'  ");
        stb.append("   AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("   AND T1.TESTDIV ='" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        if (0 == _sheetno) {
            stb.append("      T1.JUDGEMENT, R1.TOTAL_RANK2, T1.EXAMNO ");
        } else if (1 == _sheetno) {
            stb.append("      T1.FS_CD, T1.EXAMNO ");
        } else if (2 == _sheetno) {
            stb.append("      R1.TOTAL_RANK2, T1.EXAMNO");
        } else if (3 == _sheetno) {
            stb.append("      T1.DESIREDIV, R1.TOTAL_RANK2, T1.EXAMNO");
        } else if (4 == _sheetno) {
            stb.append("      R1.TOTAL_RANK1, T1.EXAMNO");
        } else if (5 == _sheetno) {
            stb.append("      T1.EXAMNO");
        }

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71704 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _examYear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _loginYear;
        private final String _templatePath;
        private final String _nameflg;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _examYear      = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _nameflg       = request.getParameter("NAMEFLAG");
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _templatePath  = request.getParameter("EXCEL_TEMPLATE_PATH");
        }
    }
}

// eof
