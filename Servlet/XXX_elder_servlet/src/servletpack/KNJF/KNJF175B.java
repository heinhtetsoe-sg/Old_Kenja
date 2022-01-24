// kanji=漢字
/*
 * $Id: dfffb7e1514db213b23b2e52ab2ee3599e9f2e26 $
 *
 * 作成日: 2009/06/18 15:47:43 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: dfffb7e1514db213b23b2e52ab2ee3599e9f2e26 $
 */
public class KNJF175B {

    private static final Log log = LogFactory.getLog("KNJF175.class");
    private static final String FORM_NAME1 = "KNJF175B_1.frm";
    private static final String FORM_NAME1_2 = "KNJF175B_1_2.frm";
    private static final String FORM_NAME2 = "KNJF175B_2.frm";
    private static final String IS_JUNIOR = "J";
    private static final String IS_HIGH = "H";
    private static final String SSEME = "1";
    private static final String ESEME = "3";

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(request, db2);

            _hasData = false;

            final List dateList = getDateInfo(db2);

            printMain(svf, dateList);

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

    /**
     * 日付情報の取得
     */
    private List getDateInfo(final DB2UDB db2) throws SQLException, ParseException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getDateSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Date date = new Date(rs.getString("DATE"));
                date.setDiary(db2);
                date.setVisitrec(db2);
//                date.setVisitrecCnt(db2);
                date.setAttendCnt(db2);
                if (_param._isPrintAttend) {
                    date.setAttend(db2);
                }
                rtnList.add(date);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    /**
     * 対象日付の取得
     * ・保健室日誌データと保健室来室データのどちらかに登録されている日付が対象。
     */
    private String getDateSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     DATE ");
        stb.append(" FROM ");
        stb.append("     NURSEOFF_DIARY_DAT ");
        stb.append(" WHERE ");
        stb.append("     DATE BETWEEN '" + _param._diaryDateFrom + "' AND '" + _param._diaryDateTo + "' ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            stb.append("   AND SCHOOL_KIND = '" + _param._schKind + "' ");
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOL_KIND)) {
            stb.append("   AND SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
        }
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T1.VISIT_DATE AS DATE ");
        stb.append(" FROM ");
        stb.append("     NURSEOFF_VISIT_TEXT_DAT T1 ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            stb.append("     INNER JOIN (SELECT DISTINCT SCHREGNO ");
            stb.append("                 FROM SCHREG_REGD_DAT T1 ");
            stb.append("                 INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = '" + _param._schKind + "' ");
            stb.append("                 WHERE T1.YEAR = '" + _param._year + "' ");
            stb.append("                 ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOL_KIND)) {
            stb.append("     INNER JOIN (SELECT DISTINCT SCHREGNO ");
            stb.append("                 FROM SCHREG_REGD_DAT T1 ");
            stb.append("                 INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
            stb.append("                 WHERE T1.YEAR = '" + _param._year + "' ");
            stb.append("                 ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.VISIT_DATE BETWEEN '" + _param._diaryDateFrom + "' AND '" + _param._diaryDateTo + "' ");
        stb.append(" ORDER BY ");
        stb.append("     DATE ");
        return stb.toString();
    }

    /**
     * 印刷処理（メイン）
     */
    private void printMain(final Vrw32alp svf, final List dateList) {
        for (final Iterator iter = dateList.iterator(); iter.hasNext();) {
            final Date date = (Date) iter.next();
            log.debug("日付 = "+date);
            //日誌
            svf.VrSetForm(FORM_NAME1, 1);
            printStamp(svf);
            printGradeName(svf);
            printDiary(svf, date);
//            printVisitrecCnt(svf, date);
            printAttendCnt(svf, date);
            printVisitrec(svf, date);
            svf.VrEndPage();
            //欠席者一覧
            if (_param._isPrintAttend) {
                svf.VrSetForm(FORM_NAME2, 4);
                printAttend(svf, date);
            }
            _hasData = true;
        }
    }

    /**
     * 保健室日誌
     */
    private void printDiary(final Vrw32alp svf, final Date date) {
        svf.VrsOut("NENDO", KNJ_EditDate.h_format_S(date._date, "yyyy年MM月dd日"));
        svf.VrsOut("WEEK", KNJ_EditDate.h_format_W(date._date) + "曜日");
        final int lineCnt = 15;
        for (final Iterator iter = date._diaryList.iterator(); iter.hasNext();) {
            final Diary diary = (Diary) iter.next();
            if (null != diary._weatherText && !"".equals(diary._weatherText)) {
                svf.VrsOut("WEATHER", diary._weatherText);
            } else {
                svf.VrsOut("WEATHER", diary._weather);
            }
            svf.VrsOut("DEGREE", diary._temperature);
            printReport(svf, "EVENT", diary._event, 24, 6);
            printReport(svf, "DIARY", diary._diary, 86, lineCnt);
        }
    }

    /**
     * 保健室日誌（行事・日誌）
     */
    private void printReport(
            final Vrw32alp svf,
            final String fieldName,
            final String str,
            final int size,
            final int lineCnt
    ) {
        final List list = KNJ_EditKinsoku.getTokenList(str, size, lineCnt);
        if ( list != null ) {
            for (int i = 0; i < list.size(); i++) {
                svf.VrsOutn(fieldName, i+1,  (String)list.get(i) );
            }
        }
    }

    /**
     * 保健室来室（傷病記録）
     */
    private void printVisitrec(final Vrw32alp svf, final Date date) {
        int gyoMax = 10;
        int gyo = 0;
        for (final Iterator iter = date._visitrecList.iterator(); iter.hasNext();) {
            final Visitrec visitrec = (Visitrec) iter.next();
            gyo++;
            if (gyoMax < gyo) {
                svf.VrEndPage();
                svf.VrSetForm(FORM_NAME1_2, 1);
                gyo -= gyoMax;
                gyoMax = 25;
            }
            svf.VrsOutn("HR_NAME", gyo,  visitrec._hrAbbv + "-" + Integer.parseInt(visitrec._attendno));
            svf.VrsOutn("NAME", gyo,  visitrec._name);
            svf.VrsOutn("SEX", gyo,  visitrec._sex);
            svf.VrsOutn("TIME", gyo,  visitrec._hourMinute);
//            svf.VrsOutn(getFieldName("PROCESS", visitrec._reason, 12), gyo,  visitrec._reason);
            getFieldName(svf, "PLACE", gyo, visitrec._place);
            getFieldName(svf, "REASON", gyo, visitrec._cause);
            getFieldName(svf, "TREAT", gyo, visitrec._treatment);
        }
    }

	private void getFieldName(final Vrw32alp svf, final String field, int gyo, final String data) {
		if (KNJ_EditEdit.getMS932ByteLength(data) <= 20) {
		    svf.VrsOutn(field, gyo, data);
		} else {
			final List token = KNJ_EditKinsoku.getTokenList(data, 20);
			for (int i = 0; i < token.size(); i++) {
		        svf.VrsOutn(field + String.valueOf(2 + i) , gyo, (String) token.get(i));
			}
		}
	}


//    /**
//     * 保健室来室（件数）
//     */
//    private void printVisitrecCnt(final Vrw32alp svf, final Date date) {
//        for (final Iterator iter = date._visitrecCntList.iterator(); iter.hasNext();) {
//            final VisitrecCnt visitrecCnt = (VisitrecCnt) iter.next();
//            int gradeInt = Integer.parseInt(visitrecCnt._grade);
//            int gradepos = _param.getGradePos(gradeInt);
//            if (gradeInt == 0) {
//                //計
//                svf.VrsOut("TOTAL_TYPE" + visitrecCnt._type,  visitrecCnt._cnt);
//            } else {
//                //各学年
//                svf.VrsOutn("TYPE" + visitrecCnt._type,  gradepos,  visitrecCnt._cnt);
//            }
//        }
//    }

    /**
     * 学年名称
     */
    private void printGradeName(final Vrw32alp svf) {
        //各学年
        for (final Iterator it = _param._gradeNames.keySet().iterator(); it.hasNext();) {
            final Integer gradeInteger = (Integer) it.next();
            final int gradeInt = gradeInteger.intValue();
            final int gradepos = _param.getGradePos(gradeInt);
            svf.VrsOutn("GRADE",  gradepos,  _param.getGradeName(gradeInt));
        }
    }

    /**
     * 印鑑
     */
    private void printStamp(final Vrw32alp svf) {
        for (Iterator iterator = _param._stampData.keySet().iterator(); iterator.hasNext();) {
            final String key = (String) iterator.next();
            final StampData stampData = (StampData) _param._stampData.get(key);
            svf.VrsOut("JOB_NAME" + (Integer.parseInt(stampData._seq) + 5), stampData._title);
            if (_param._printStamp01 && "1".equals(stampData._seq)) {
                svf.VrsOut("STAFFBTM_1", _param.getStampImageFile(stampData._stampName));
            }
            if (_param._printStamp02 && "2".equals(stampData._seq)) {
                svf.VrsOut("STAFFBTM_2", _param.getStampImageFile(stampData._stampName));
            }
            if (_param._printStamp03 && "3".equals(stampData._seq)) {
                svf.VrsOut("STAFFBTM_3", _param.getStampImageFile(stampData._stampName));
            }
        }
        svf.VrsOut("STAFFBTM_4", _param.getStampImageFile(_param._remark4StampNo));
    }

    private void printStamp2(final Vrw32alp svf) {
        for (Iterator iterator = _param._stampData.keySet().iterator(); iterator.hasNext();) {
            final String key = (String) iterator.next();
            final StampData stampData = (StampData) _param._stampData.get(key);
            svf.VrsOut("JOB_NAME" + (Integer.parseInt(stampData._seq) + 5), stampData._title);
            if (_param._printStamp01 && "1".equals(stampData._seq)) {
                svf.VrsOut("STAFFBTM_6", _param.getStampImageFile(stampData._stampName));
            }
            if (_param._printStamp02 && "2".equals(stampData._seq)) {
                svf.VrsOut("STAFFBTM_7", _param.getStampImageFile(stampData._stampName));
            }
            if (_param._printStamp03 && "3".equals(stampData._seq)) {
                svf.VrsOut("STAFFBTM_8", _param.getStampImageFile(stampData._stampName));
            }
            if (_param._printStamp04 && "4".equals(stampData._seq)) {
                svf.VrsOut("STAFFBTM_9C", _param.getStampImageFile(stampData._stampName));
            }
            svf.VrsOut("STAFFBTM_4", _param.getStampImageFile(_param._remark4StampNo));
        }
    }

    /**
     * 出欠（件数）
     */
    private void printAttendCnt(final Vrw32alp svf, final Date date) {
        //各学年
        for (final Iterator iter = date._attendCntList.iterator(); iter.hasNext();) {
            final AttendCnt attendCnt = (AttendCnt) iter.next();
            int gradeInt = Integer.parseInt(attendCnt._grade);
            int gradepos = _param.getGradePos(gradeInt);
            svf.VrsOutn("SICK",  gradepos,  attendCnt._sick);
            svf.VrsOutn("NOTICE",  gradepos,  attendCnt._notice);
            svf.VrsOutn("NONOTICE",  gradepos,  attendCnt._nonotice);
            svf.VrsOutn("SUSPEND",  gradepos,  attendCnt._suspend);
            svf.VrsOutn("MOURNING",  gradepos,  attendCnt._mourning);
        }
        //計
        svf.VrsOut("TOTAL_SICK",  date._sickT);
        svf.VrsOut("TOTAL_NOTICE",  date._noticeT);
        svf.VrsOut("TOTAL_NONOTICE",  date._nonoticeT);
        svf.VrsOut("TOTAL_SUSPEND",  date._suspendT);
        svf.VrsOut("TOTAL_MOURNING",  date._mourningT);
    }

    /**
     * 出欠（欠席者一覧）
     */
    private void printAttend(final Vrw32alp svf, final Date date) {
        svf.VrsOut("NENDO", KNJ_EditDate.h_format_S(date._date, "yyyy年MM月dd日"));
        for (final Iterator iter = date._attendList.iterator(); iter.hasNext();) {
            final Attend attend = (Attend) iter.next();
            svf.VrsOut("NO",  attend._number);
            svf.VrsOut("HR_NAME",  attend._hrAbbv + "-" + Integer.parseInt(attend._attendno));
            svf.VrsOut("NAME",  attend._name);
            svf.VrsOut("SEX",  attend._sex);
            svf.VrsOut("ATTEND_NAME",  attend._diName);
            svf.VrsOut("ABSENCE_NAME",  attend._diRemark);
            svf.VrEndRecord();
        }
    }

    /**
     * 出欠に使用する校時コードの取得
     */
    public static String getPeiodValue(
            final DB2UDB db2,
            final KNJDefineSchool definecode,
            final String year,
            final String sSemester,
            final String eSemester
    ) throws SQLException {
    //  校時名称
        StringBuffer stb2 = null;
        ResultSet rs = null;
        final int periodnum;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("    NAMECD2, ");
            stb.append("    NAME1, ");
            if (definecode != null && definecode.usefromtoperiod) {
                stb.append("    CASE WHEN NAMECD2 BETWEEN S_PERIODCD AND E_PERIODCD THEN 1 ELSE 0 END AS ONPERIOD ");
            } else {
                stb.append("    1 AS ONPERIOD ");
            }
            if (definecode != null && definecode.usefromtoperiod) {
                stb.append(" FROM ");
                stb.append("    NAME_MST W1, ");
                stb.append("    COURSE_MST W2 ");
                stb.append(" WHERE ");
                stb.append("    NAMECD1 = 'B001' ");
                stb.append("    AND COURSECD IN(SELECT ");
                stb.append("                        MIN(COURSECD) ");
                stb.append("                    FROM ");
                stb.append("                        SCHREG_REGD_DAT W3 ");
                stb.append("                    WHERE ");
                stb.append("                        W3.YEAR = '" + year + "' ");
                stb.append("                        AND W3.SEMESTER BETWEEN '" + sSemester + "' AND '" + eSemester + "' ");
                stb.append("                    ) ");
            } else {
                stb.append(" FROM ");
                stb.append("    NAME_MST W1 ");
                stb.append(" WHERE ");
                stb.append("    NAMECD1 = 'B001' ");
            }
            stb.append("    AND EXISTS(SELECT ");
            stb.append("                   'X' ");
            stb.append("               FROM ");
            stb.append("                   NAME_YDAT W2 ");
            stb.append("               WHERE ");
            stb.append("                   W2.YEAR = '" + year + "' ");
            stb.append("                   AND W2.NAMECD1 = 'B001' ");
            stb.append("                   AND W2.NAMECD2 = W1.NAMECD2) ");
            stb.append(" ORDER BY ");
            stb.append("    NAMECD2 ");

            db2.query(stb.toString());
            rs = db2.getResultSet();

            List periodlist = new ArrayList();
            String sep = "";
            for (int i = 0; i < 16  &&  rs.next(); i++) {
                if (rs.getInt("ONPERIOD") == 1) {
                    periodlist.add( rs.getString("NAME1") );
                }
                if (rs.getInt("ONPERIOD") == 1) {
                    if (stb2 == null) {
                        stb2 = new StringBuffer();
                        stb2.append("(");
                    }
                    stb2.append(sep + "'").append( rs.getString("NAMECD2") ).append("'");
                    sep = ",";
                }
            }
            periodnum = ( periodlist.size() <= 9 ) ? 9: 16;

            if (stb2 != null) {
                stb2.append(")");
            } else if (periodnum == 9) {
                stb2 = new StringBuffer("('1','2','3','4','5','6','7','8','9')");
            } else {
                stb2 = new StringBuffer("('1','2','3','4','5','6','7','8','9','A','B','C','D','E','F''G')");
            }
        } finally{
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
        return stb2.toString();
    }

    /**
     * 出欠データSQLを返す
     * -- 開始日付の端数可
     * -- 終了日付の端数可
     * @param year          年度
     * @param sSemester     対象学期範囲From
     * @param eSemester     対象学期範囲To
     * @param periodInState 対象校時
     * @param aftDayFrom    終了日付の端数用From
     * @param aftDayTo      終了日付の端数用To
     * @param groupByDiv    グループ化区分：HR_CLASS(クラス単位)、GRADE(学年単位)、SCHREGNO(学籍単位)、SEMESTER(生徒学期単位)
     * @return 出欠データSQL<code>String</code>を返す
     */
    private String getAttendSemesSql(
            final KNJDefineSchool definecode,
            final String year,
            final String sSemester,
            final String eSemester,
            final String periodInState,
            final String aftDayFrom,
            final String aftDayTo,
            final String groupByDiv
    ) {
        final StringBuffer stb = new StringBuffer();

        //対象生徒
        stb.append("WITH SCHNO AS( ");
        stb.append(" SELECT ");
        stb.append("    W1.SCHREGNO, ");
        stb.append("    W1.GRADE, ");
        stb.append("    W1.SEMESTER, ");
        stb.append("    W1.HR_CLASS, ");
        stb.append("    W3.HR_NAME, ");
        stb.append("    W3.HR_NAMEABBV, ");
        stb.append("    W1.ATTENDNO, ");
        stb.append("    W4.NAME, ");
        stb.append("    Z002.NAME2 AS SEX ");
        stb.append(" FROM ");
        stb.append("    SCHREG_REGD_DAT W1 ");
        stb.append("    INNER JOIN SCHREG_REGD_HDAT W3 ON W3.YEAR = W1.YEAR ");
        stb.append("                                  AND W3.SEMESTER = W1.SEMESTER ");
        stb.append("                                  AND W3.GRADE = W1.GRADE ");
        stb.append("                                  AND W3.HR_CLASS = W1.HR_CLASS ");
        stb.append("    INNER JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
        stb.append("    LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("                           AND Z002.NAMECD2 = W4.SEX ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.GRADE ");
            stb.append("   AND T2.SCHOOL_KIND = '" + _param._schKind + "' ");
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOL_KIND)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.GRADE ");
            stb.append("   AND T2.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
        }
        stb.append(" WHERE ");
        stb.append("    W1.YEAR = '" + year + "' ");
        stb.append("    AND W1.SEMESTER BETWEEN '" + sSemester + "' AND '" + eSemester + "' ");

            //対象生徒の時間割データ
            stb.append(" ), SCHEDULE_SCHREG_R AS( ");
            stb.append(" SELECT ");
            stb.append("    T2.SCHREGNO, ");
            stb.append("    T1.SEMESTER, ");
            stb.append("    T1.EXECUTEDATE, ");
            stb.append("    T1.PERIODCD ");
            stb.append(" FROM ");
            stb.append("    SCH_CHR_DAT T1, ");
            stb.append("    CHAIR_STD_DAT T2 ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + year + "' ");
            stb.append("    AND T1.SEMESTER BETWEEN '" + sSemester + "' AND '" + eSemester + "' ");
            stb.append("    AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
                stb.append("    AND T1.EXECUTEDATE BETWEEN '" + aftDayFrom + "' AND '" + aftDayTo + "' ");
            stb.append("    AND T1.YEAR = T2.YEAR ");
            stb.append("    AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("    AND T1.CHAIRCD = T2.CHAIRCD ");
            if (definecode != null && definecode.usefromtoperiod)
                stb.append("    AND T1.PERIODCD IN " + periodInState + " ");
            stb.append("    AND T2.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO GROUP BY SCHREGNO) ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       SCHREG_BASE_MST T3 ");
            stb.append("                   WHERE ");
            stb.append("                       T3.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND ((ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE) ");
            stb.append("                             OR (GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE)) ");
            stb.append("                  ) ");
            stb.append(" GROUP BY ");
            stb.append("    T2.SCHREGNO, ");
            stb.append("    T1.SEMESTER, ");
            stb.append("    T1.EXECUTEDATE, ");
            stb.append("    T1.PERIODCD ");

            stb.append(" ), SCHEDULE_SCHREG AS( ");
            stb.append(" SELECT ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T1.SEMESTER, ");
            stb.append("    T1.EXECUTEDATE, ");
            stb.append("    T1.PERIODCD ");
            stb.append(" FROM ");
            stb.append("    SCHEDULE_SCHREG_R T1 ");
            stb.append(" WHERE ");
            stb.append("    NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       SCHREG_TRANSFER_DAT T3 ");
            stb.append("                   WHERE ");
            stb.append("                       T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("                       AND TRANSFERCD IN('1','2') ");
            stb.append("                       AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ");
            stb.append("                  ) ");
            stb.append(" GROUP BY ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T1.SEMESTER, ");
            stb.append("    T1.EXECUTEDATE, ");
            stb.append("    T1.PERIODCD ");

            //対象生徒の出欠データ
            stb.append(" ), T_ATTEND_DAT AS( ");
            stb.append(" SELECT ");
            stb.append("    T0.SCHREGNO, ");
            stb.append("    T1.SEMESTER, ");
            stb.append("    T0.ATTENDDATE, ");
            stb.append("    T0.PERIODCD, ");
            stb.append("    L1.REP_DI_CD AS DI_CD ");
            stb.append(" FROM ");
            stb.append("    ATTEND_DAT T0 ");
            stb.append("    LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T0.YEAR AND L1.DI_CD = T0.DI_CD, ");
            stb.append("    SCHEDULE_SCHREG T1 ");
            stb.append(" WHERE ");
            stb.append("    T0.YEAR = '" + year + "' ");
                stb.append("    AND T0.ATTENDDATE BETWEEN '" + aftDayFrom + "' AND '" + aftDayTo + "' ");
            stb.append("    AND T0.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T0.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("    AND T0.PERIODCD = T1.PERIODCD ");

            //対象生徒の出欠データ（忌引・出停した日）
            stb.append(" ), T_ATTEND_DAT_B AS( ");
            stb.append(" SELECT ");
            stb.append("    T0.SCHREGNO, ");
            stb.append("    T0.SEMESTER, ");
            stb.append("    T0.ATTENDDATE, ");
            stb.append("    MIN(T0.PERIODCD) AS FIRST_PERIOD, ");
            stb.append("    COUNT(DISTINCT T0.PERIODCD) AS PERIOD_CNT ");
            stb.append(" FROM ");
            stb.append("    T_ATTEND_DAT T0 ");
            stb.append(" WHERE ");
            stb.append("    DI_CD IN('2','3','9','10' ");
            if ("true".equals(_param._useVirus)) {
                stb.append("    ,'19','20'");
            }
            if ("true".equals(_param._useKoudome)) {
                stb.append("    ,'25','26'");
            }
            stb.append("    ) ");
            stb.append(" GROUP BY ");
            stb.append("    T0.SCHREGNO, ");
            stb.append("    T0.SEMESTER, ");
            stb.append("    T0.ATTENDDATE ");

            //対象生徒の日単位の最小校時・最大校時・校時数
            stb.append(" ), T_PERIOD_CNT AS( ");
            stb.append(" SELECT ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T1.SEMESTER, ");
            stb.append("    T1.EXECUTEDATE, ");
            stb.append("    MIN(T1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append("    MAX(T1.PERIODCD) AS LAST_PERIOD, ");
            stb.append("    COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ");
            stb.append(" FROM ");
            stb.append("    SCHEDULE_SCHREG T1 ");
            stb.append(" GROUP BY ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T1.SEMESTER, ");
            stb.append("    T1.EXECUTEDATE ");

            if ("1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                //対象生徒の日単位のデータ（忌引・出停した日）
                stb.append(" ), T_PERIOD_SUSPEND_MOURNING AS( ");
                stb.append(" SELECT ");
                stb.append("    T0.SCHREGNO, ");
                stb.append("    T0.EXECUTEDATE ");
                stb.append(" FROM ");
                stb.append("    T_PERIOD_CNT T0, ");
                stb.append("    T_ATTEND_DAT_B T1 ");
                stb.append(" WHERE ");
                stb.append("        T0.SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND T0.EXECUTEDATE = T1.ATTENDDATE ");
                stb.append("    AND T0.FIRST_PERIOD = T1.FIRST_PERIOD ");
                stb.append("    AND T0.PERIOD_CNT = T1.PERIOD_CNT ");
            }
        stb.append(     ") ");

        //メイン表
        stb.append(" SELECT ");
        if (groupByDiv == "GRADE") {
            stb.append("    TT0.GRADE, ");
        } else if (groupByDiv == "SCHREGNO") {
            stb.append("    TT0.HR_NAME, ");
            stb.append("    TT0.HR_NAMEABBV, ");
            stb.append("    TT0.ATTENDNO, ");
            stb.append("    TT0.NAME, ");
            stb.append("    TT0.SEX, ");
            stb.append("    TT0.GRADE, ");
            stb.append("    TT0.HR_CLASS, ");
            stb.append("    TT0.SCHREGNO, ");
        }
            stb.append("    MAX(TT5.DINAME) AS DINAME, ");
            stb.append("    MAX(TT5.DIREMARK) AS DIREMARK, ");
            stb.append("    SUM(VALUE(TT5.SICK,0)) AS SICK1, ");
            stb.append("    SUM(VALUE(TT5.NOTICE,0)) AS SICK2, ");
            stb.append("    SUM(VALUE(TT5.NONOTICE,0)) AS SICK3, ");
            stb.append("    SUM(VALUE(TT3.SUSPEND,0)) ");
            if ("true".equals(_param._useVirus)) {
                stb.append("        + SUM(VALUE(TT3_2.VIRUS,0)) ");
            }
            if ("true".equals(_param._useKoudome)) {
                stb.append("        + SUM(VALUE(TT3_3.KOUDOME,0)) ");
            }
            stb.append("    AS SUSPEND, ");
            stb.append("    SUM(VALUE(TT4.MOURNING,0)) AS MOURNING ");
        stb.append(" FROM ");
        stb.append("    SCHNO TT0 ");
            //個人別出停日数
            stb.append(" LEFT OUTER JOIN( ");
            stb.append("    SELECT ");
            stb.append("        W1.SCHREGNO, ");
            stb.append("        VALUE(W1.SEMESTER, '9') AS SEMESTER, ");
            stb.append("        COUNT(DISTINCT W1.ATTENDDATE) AS SUSPEND ");
            stb.append("    FROM ");
            stb.append("        T_ATTEND_DAT W1 ");
            stb.append("    WHERE ");
            stb.append("        W1.DI_CD IN ('2','9') ");
            if ("1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("    AND (W1.SCHREGNO, W1.ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append("    GROUP BY ");
            stb.append("        GROUPING SETS ((W1.SCHREGNO, W1.SEMESTER), (W1.SCHREGNO)) ");
            stb.append("    ) TT3 ON TT0.SCHREGNO = TT3.SCHREGNO ");
            stb.append("          AND TT0.SEMESTER = TT3.SEMESTER ");
            //個人別伝染病日数
            stb.append(" LEFT OUTER JOIN( ");
            stb.append("    SELECT ");
            stb.append("        W1.SCHREGNO, ");
            stb.append("        VALUE(W1.SEMESTER, '9') AS SEMESTER, ");
            stb.append("        COUNT(DISTINCT W1.ATTENDDATE) AS VIRUS ");
            stb.append("    FROM ");
            stb.append("        T_ATTEND_DAT W1 ");
            stb.append("    WHERE ");
            stb.append("        W1.DI_CD IN ('19','20') ");
            if ("1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("    AND (W1.SCHREGNO, W1.ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append("    GROUP BY ");
            stb.append("        GROUPING SETS ((W1.SCHREGNO, W1.SEMESTER), (W1.SCHREGNO)) ");
            stb.append("    ) TT3_2 ON TT0.SCHREGNO = TT3_2.SCHREGNO ");
            stb.append("          AND TT0.SEMESTER = TT3_2.SEMESTER ");
            //個人別交止日数
            stb.append(" LEFT OUTER JOIN( ");
            stb.append("    SELECT ");
            stb.append("        W1.SCHREGNO, ");
            stb.append("        VALUE(W1.SEMESTER, '9') AS SEMESTER, ");
            stb.append("        COUNT(DISTINCT W1.ATTENDDATE) AS KOUDOME ");
            stb.append("    FROM ");
            stb.append("        T_ATTEND_DAT W1 ");
            stb.append("    WHERE ");
            stb.append("        W1.DI_CD IN ('25','26') ");
            if ("1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("    AND (W1.SCHREGNO, W1.ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append("    GROUP BY ");
            stb.append("        GROUPING SETS ((W1.SCHREGNO, W1.SEMESTER), (W1.SCHREGNO)) ");
            stb.append("    ) TT3_3 ON TT0.SCHREGNO = TT3_3.SCHREGNO ");
            stb.append("          AND TT0.SEMESTER = TT3_3.SEMESTER ");
            //個人別忌引日数
            stb.append(" LEFT OUTER JOIN( ");
            stb.append("    SELECT ");
            stb.append("        W1.SCHREGNO, ");
            stb.append("        VALUE(W1.SEMESTER, '9') AS SEMESTER, ");
            stb.append("        COUNT(DISTINCT W1.ATTENDDATE) AS MOURNING ");
            stb.append("    FROM ");
            stb.append("        T_ATTEND_DAT W1 ");
            stb.append("    WHERE ");
            stb.append("        W1.DI_CD IN ('3','10') ");
            if ("1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("    AND (W1.SCHREGNO, W1.ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append("    GROUP BY ");
            stb.append("        GROUPING SETS ((W1.SCHREGNO, W1.SEMESTER), (W1.SCHREGNO)) ");
            stb.append("    ) TT4 ON TT0.SCHREGNO = TT4.SCHREGNO ");
            stb.append("          AND TT0.SEMESTER = TT4.SEMESTER ");
            //個人別欠席日数
            stb.append(" LEFT OUTER JOIN( ");
            stb.append("    SELECT ");
            stb.append("        W0.SCHREGNO, ");
            stb.append("        VALUE(W1.SEMESTER, '9') AS SEMESTER, ");
            stb.append("        MAX(CASE WHEN ATTEND_DI.REP_DI_CD IN ('4','5','6','11','12','13') THEN ATTEND_DI.DI_NAME1 END) AS DINAME, ");
            stb.append("        MAX(CASE WHEN ATTEND_DI.REP_DI_CD IN ('4','5','6','11','12','13') THEN W0.DI_REMARK END) AS DIREMARK, ");
            stb.append("        SUM(CASE ATTEND_DI.REP_DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS SICK, ");
            stb.append("        SUM(CASE ATTEND_DI.REP_DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS NOTICE, ");
            stb.append("        SUM(CASE ATTEND_DI.REP_DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS NONOTICE ");
            stb.append("    FROM ");
            stb.append("        ATTEND_DAT W0 ");
            stb.append("        LEFT JOIN ATTEND_DI_CD_DAT ATTEND_DI ON ATTEND_DI.YEAR = W0.YEAR ");
            stb.append("                               AND ATTEND_DI.DI_CD = W0.DI_CD, ");
            stb.append("        (SELECT ");
            stb.append("             T0.SCHREGNO, ");
            stb.append("             T0.SEMESTER, ");
            stb.append("             T0.EXECUTEDATE, ");
            if ("1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("         T2.FIRST_PERIOD ");
            } else {
                stb.append("         T0.FIRST_PERIOD ");
            }
            stb.append("         FROM ");
            stb.append("             T_PERIOD_CNT T0, ");
            stb.append("             ( ");
            stb.append("              SELECT ");
            stb.append("                  W1.SCHREGNO, W1.ATTENDDATE, ");
            stb.append("                  MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append("                  COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append("              FROM ");
            stb.append("                  T_ATTEND_DAT W1 ");
            stb.append("              WHERE ");
            if ("1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("              W1.DI_CD IN ('4','5','6','11','12','13','2','9','3','10' ");
                if ("true".equals(_param._useVirus)) {
                    stb.append("    ,'19','20'");
                }
                if ("true".equals(_param._useKoudome)) {
                    stb.append("    ,'25','26'");
                }
                stb.append("              ) ");
            } else {
                stb.append("              W1.DI_CD IN ('4','5','6','11','12','13') ");
            }
            stb.append("              GROUP BY ");
            stb.append("                  W1.SCHREGNO, ");
            stb.append("                  W1.ATTENDDATE ");
            stb.append("             ) T1 ");
            if ("1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("         INNER JOIN ( ");
                stb.append("              SELECT ");
                stb.append("                  W1.SCHREGNO, W1.ATTENDDATE, ");
                stb.append("                  MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
                stb.append("                  COUNT(W1.PERIODCD) AS PERIOD_CNT ");
                stb.append("              FROM ");
                stb.append("                  T_ATTEND_DAT W1 ");
                stb.append("              WHERE ");
                stb.append("                  W1.DI_CD IN ('4','5','6','11','12','13') ");
                stb.append("              GROUP BY ");
                stb.append("                  W1.SCHREGNO, ");
                stb.append("                  W1.ATTENDDATE ");
                stb.append("             ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.ATTENDDATE ");
            }
            stb.append("         WHERE ");
            stb.append("             T0.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T0.EXECUTEDATE = T1.ATTENDDATE ");
            stb.append("             AND T0.FIRST_PERIOD = T1.FIRST_PERIOD ");
            stb.append("             AND T0.PERIOD_CNT = T1.PERIOD_CNT ");
            stb.append("        ) W1 ");
            stb.append("    WHERE ");
            stb.append("        W0.SCHREGNO = W1.SCHREGNO ");
            stb.append("        AND W0.ATTENDDATE = W1.EXECUTEDATE ");
            stb.append("        AND W0.PERIODCD = W1.FIRST_PERIOD ");
            if ("1".equals(_param._knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("    AND (W1.SCHREGNO, W1.EXECUTEDATE) NOT IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append("    GROUP BY ");
            stb.append("        GROUPING SETS ((W0.SCHREGNO, W1.SEMESTER), (W0.SCHREGNO)) ");
            stb.append("    )TT5 ON TT0.SCHREGNO = TT5.SCHREGNO ");
            stb.append("         AND TT0.SEMESTER = TT5.SEMESTER ");

        stb.append(" GROUP BY ");
        if (groupByDiv == "GRADE") {
            stb.append("    TT0.GRADE ");
            stb.append("    HAVING 0 < SUM(VALUE(TT5.SICK,0)) ");
            stb.append("        OR 0 < SUM(VALUE(TT5.NOTICE,0)) ");
            stb.append("        OR 0 < SUM(VALUE(TT5.NONOTICE,0)) ");
            stb.append("        OR 0 < SUM(VALUE(TT3.SUSPEND,0)) ");
            if ("true".equals(_param._useVirus)) {
                stb.append("        + SUM(VALUE(TT3_2.VIRUS,0)) ");
            }
            if ("true".equals(_param._useKoudome)) {
                stb.append("        + SUM(VALUE(TT3_3.KOUDOME,0)) ");
            }
            stb.append("        OR 0 < SUM(VALUE(TT4.MOURNING,0)) ");
        } else if (groupByDiv == "SCHREGNO") {
            stb.append("    TT0.HR_NAME, ");
            stb.append("    TT0.HR_NAMEABBV, ");
            stb.append("    TT0.ATTENDNO, ");
            stb.append("    TT0.NAME, ");
            stb.append("    TT0.SEX, ");
            stb.append("    TT0.GRADE, ");
            stb.append("    TT0.HR_CLASS, ");
            stb.append("    TT0.SCHREGNO ");
            stb.append("    HAVING 0 < SUM(VALUE(TT5.SICK,0)) ");
            stb.append("        OR 0 < SUM(VALUE(TT5.NOTICE,0)) ");
            stb.append("        OR 0 < SUM(VALUE(TT5.NONOTICE,0)) ");
        }

        stb.append(" ORDER BY ");
        if (groupByDiv == "GRADE") {
            stb.append("    TT0.GRADE ");
        } else if (groupByDiv == "SCHREGNO") {
            stb.append("    TT0.GRADE, ");
            stb.append("    TT0.HR_CLASS, ");
            stb.append("    TT0.ATTENDNO ");
        }

        return stb.toString();
    }

    public static String getNurseoffAttendDat(final Param param, final String date) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TMP AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     NURSEOFF_ATTEND_DAT T1 ");
        if ("1".equals(param._use_prg_schoolkind)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = '" + param._year + "' AND T2.GRADE = T1.GRADE ");
            stb.append("   AND T2.SCHOOL_KIND = '" + param._schKind + "' ");
        } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOL_KIND)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = '" + param._year + "' AND T2.GRADE = T1.GRADE ");
            stb.append("   AND T2.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ");
        }
        stb.append(" WHERE ");
        stb.append("     DATE = '" + date + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.GRADE ");
        stb.append("     ,T02.CNT AS SUSPEND ");
        stb.append("     ,T03.CNT AS MOURNING ");
        stb.append("     ,T04.CNT AS SICK1  ");
        stb.append("     ,T05.CNT AS SICK2 ");
        stb.append("     ,T06.CNT AS SICK3 ");
        stb.append("     FROM (SELECT DISTINCT GRADE FROM TMP) T1 ");
        stb.append("     LEFT JOIN TMP T02 ON T02.GRADE = T1.GRADE AND T02.DI_CD = '2' ");
        stb.append("     LEFT JOIN TMP T03 ON T03.GRADE = T1.GRADE AND T03.DI_CD = '3' ");
        stb.append("     LEFT JOIN TMP T04 ON T04.GRADE = T1.GRADE AND T04.DI_CD = '4' ");
        stb.append("     LEFT JOIN TMP T05 ON T05.GRADE = T1.GRADE AND T05.DI_CD = '5' ");
        stb.append("     LEFT JOIN TMP T06 ON T06.GRADE = T1.GRADE AND T06.DI_CD = '6' ");
        return stb.toString();
    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) throws Exception {
        final Param param = new Param(request, db2);
        log.fatal("$Revision: 66852 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _loginDate;
        private final String _diaryDateFrom;
        private final String _diaryDateTo;
        private final boolean _isPrintAttend;
        private final String _useVirus;
        private final String _useKoudome;
        final String _useSchool_KindField;
        final String _SCHOOL_KIND;
        final String _useNurseoffAttend;
        final String _use_prg_schoolkind;
        final String _schKind;
        private String _knjf175PrintSchoolKind;

        private KNJDefineSchool _definecode;

        private KNJSchoolMst _knjSchoolMst;

        private String _periodInState;
        private Map _gradeNames;
        private TreeMap _gradePosition;
        /** 写真データ格納フォルダ */
        private final String _imageDir;
        /** 写真データの拡張子 */
        private final String _imageExt;
        /** 陰影保管場所(陰影出力に関係する) */
        private final String _documentRoot;
        private String _printStampKouchou;
        private String _printStampKyoutou;
        private String _printStampYougoKyouyu;
        private String _remark4StampNo;
        private String _remark6StampNo;
        private String _remark7StampNo;
        private String _remark8StampNo;
        private String _remark9StampNo;
        private final Map _stampData;
        private boolean _printStamp01 = false;
        private boolean _printStamp02 = false;
        private boolean _printStamp03 = false;
        private boolean _printStamp04 = false;

        Param(final HttpServletRequest request, final DB2UDB db2) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _diaryDateFrom = request.getParameter("SDATE").replace('/', '-');
            _diaryDateTo = request.getParameter("EDATE").replace('/', '-');
            _isPrintAttend = request.getParameter("PRINT") != null;
            _printStampKouchou = request.getParameter("PRINT_STAMP_KOUCHOU");
            _printStampKyoutou = request.getParameter("PRINT_STAMP_KYOUTOU");
            _printStampYougoKyouyu = request.getParameter("PRINT_STAMP_YOUGOKYOUYU");
            _documentRoot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所 NO001
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOL_KIND = request.getParameter("SCHKIND");
            _useNurseoffAttend = request.getParameter("useNurseoffAttend");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _schKind = request.getParameter("SCHKIND");
            if (!StringUtils.isBlank(request.getParameter("knjf175PrintSchoolKind"))) {
            	final String[] split = StringUtils.split(request.getParameter("knjf175PrintSchoolKind"), ",");
            	if (null != split) {
            		final StringBuffer stb = new StringBuffer("(");
            		String comma = "";
            		for (int i = 0; i < split.length; i++) {
            			stb.append(comma).append("'").append(StringUtils.trim(split[i])).append("'");
            			comma = ", ";
            		}
            		_knjf175PrintSchoolKind = stb.append(")").toString();
            	}
            }
            _imageDir = "image/stamp";
            _imageExt = "bmp";

            setPeriodIn(db2);
            setKNJSchoolMst(db2);
            setGradeNames(db2);
            setCertifSchoolDat(db2);
            _stampData = getStampData(db2);
            for (Iterator iterator = _stampData.keySet().iterator(); iterator.hasNext();) {
                final String key = (String) iterator.next();
                if ("1".equals(key)) {
                    _printStamp01 = null != request.getParameter("PRINT_STAMP_" + key);
                }
                if ("2".equals(key)) {
                    _printStamp02 = null != request.getParameter("PRINT_STAMP_" + key);
                }
                if ("3".equals(key)) {
                    _printStamp03 = null != request.getParameter("PRINT_STAMP_" + key);
                }
                if ("4".equals(key)) {
                    _printStamp04 = null != request.getParameter("PRINT_STAMP_" + key);
                }
            }
        }

        private Map getStampData(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            PreparedStatement psStampNo = null;
            ResultSet rsStampNo = null;
            final String stamPSql = getStampData();
            try {
                ps = db2.prepareStatement(stamPSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String stampNoSql = getMaxStampNo(rs.getString("FILE_NAME"));
                    psStampNo = db2.prepareStatement(stampNoSql);
                    rsStampNo = psStampNo.executeQuery();
                    rsStampNo.next();
                    final String stampNo = rsStampNo.getString("STAMP_NO");
                    final StampData stampData = new StampData(rs.getString("SEQ"), rs.getString("TITLE"), stampNo);
                    retMap.put(rs.getString("SEQ"), stampData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                DbUtils.closeQuietly(null, psStampNo, rsStampNo);
                db2.commit();
            }
            if (retMap.isEmpty()) {
                retMap.put("1", new StampData("1", "校長", _remark6StampNo));
                retMap.put("2", new StampData("2", "教頭", _remark7StampNo));
                retMap.put("3", new StampData("3", "養護教諭", _remark8StampNo));
            }
            return retMap;
        }

        private String getStampData() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEQ, ");
            stb.append("     FILE_NAME, ");
            stb.append("     TITLE ");
            stb.append(" FROM ");
            stb.append("     PRG_STAMP_DAT ");
            stb.append(" WHERE ");
            stb.append("         YEAR        = '" + _year + "' ");
            stb.append("     AND SEMESTER    = '9' ");
            if ("1".equals(_use_prg_schoolkind)) {
                stb.append("     AND SCHOOLCD    = '000000000000' ");
                stb.append("     AND SCHOOL_KIND = '" + _schKind + "' ");
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOL_KIND)) {
                stb.append("     AND SCHOOLCD    = '000000000000' ");
                stb.append("     AND SCHOOL_KIND = '" + _SCHOOL_KIND + "' ");
            }
            stb.append("     AND PROGRAMID   = 'KNJF175' ");
            return stb.toString();
        }

        private String getMaxStampNo(final String staffcd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     MAX(STAMP_NO) AS STAMP_NO ");
            stb.append(" FROM ");
            stb.append("     ATTEST_INKAN_DAT ");
            stb.append(" WHERE ");
            stb.append("     STAFFCD = '" + staffcd + "' ");

            return stb.toString();
        }

        /**
         * 出欠に使用する校時コードの取得
         */
        private void setPeriodIn(final DB2UDB db2) throws SQLException, ParseException {
            try {
                _definecode = new KNJDefineSchool();
                _definecode.defineCode(db2, _year);
                _periodInState = getPeiodValue(db2, _definecode, _year, _semester, _semester);
            } catch (SQLException e) {
                log.warn("校時コード取得でエラー", e);
            }
        }

        private void setKNJSchoolMst(final DB2UDB db2) throws SQLException, ParseException {
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
        }

        /**
         * 学年名の取得
         */
        private void setGradeNames(final DB2UDB db2) throws SQLException {
            _gradeNames = new HashMap();
            _gradePosition = new TreeMap();
            try {
                String sql = " SELECT INT(GRADE) AS GRADE_INT, GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' ";
                if ("1".equals(_use_prg_schoolkind)) {
                    sql += " AND SCHOOL_KIND = '" + _schKind + "' ";
                } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOL_KIND)) {
                    sql += " AND SCHOOL_KIND = '" + _SCHOOL_KIND + "' ";
                } else if (!StringUtils.isBlank(_knjf175PrintSchoolKind)) {
                    sql += " AND SCHOOL_KIND IN " + _knjf175PrintSchoolKind;
                } else {
                    sql += " AND SCHOOL_KIND IN ('J', 'H') ";
                }
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    final int i;
                    if (_gradePosition.isEmpty()) {
                        i = 1;
                    } else {
                        i = ((Integer) _gradePosition.get(_gradePosition.lastKey())).intValue() + 1;
                    }
                    _gradePosition.put(Integer.valueOf(rs.getString("GRADE_INT")), new Integer(i));
                    _gradeNames.put(Integer.valueOf(rs.getString("GRADE_INT")), rs.getString("GRADE_NAME1"));
                }
            } catch (SQLException e) {
                log.warn("学年名称取得でエラー", e);
            }
        }


        private void setCertifSchoolDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH T_INKAN AS ( ");
                stb.append("     SELECT ");
                stb.append("         MAX(STAMP_NO) AS STAMP_NO, ");
                stb.append("         STAFFCD ");
                stb.append("     FROM ");
                stb.append("         ATTEST_INKAN_DAT ");
                stb.append("     GROUP BY ");
                stb.append("         STAFFCD ");
                stb.append(" ) ");
                stb.append(" SELECT T1.YEAR, ");
                stb.append("        T2.STAMP_NO AS REMARK4_STAMP_NO, ");
                stb.append("        T3.STAMP_NO AS REMARK6_STAMP_NO, ");
                stb.append("        T4.STAMP_NO AS REMARK7_STAMP_NO, ");
                stb.append("        T5.STAMP_NO AS REMARK8_STAMP_NO, ");
                stb.append("        T6.STAMP_NO AS REMARK9_STAMP_NO ");
                stb.append(" FROM CERTIF_SCHOOL_DAT T1 ");
                stb.append(" LEFT JOIN T_INKAN T2 ON T2.STAFFCD = T1.REMARK4 ");
                stb.append(" LEFT JOIN T_INKAN T3 ON T3.STAFFCD = T1.REMARK6 ");
                stb.append(" LEFT JOIN T_INKAN T4 ON T4.STAFFCD = T1.REMARK7 ");
                stb.append(" LEFT JOIN T_INKAN T5 ON T5.STAFFCD = T1.REMARK8 ");
                stb.append(" LEFT JOIN T_INKAN T6 ON T6.STAFFCD = T1.REMARK9 ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' AND T1.CERTIF_KINDCD = '124' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _remark4StampNo = rs.getString("REMARK4_STAMP_NO");
                    _remark6StampNo = rs.getString("REMARK6_STAMP_NO");
                    _remark7StampNo = rs.getString("REMARK7_STAMP_NO");
                    _remark8StampNo = rs.getString("REMARK8_STAMP_NO");
                    _remark9StampNo = rs.getString("REMARK9_STAMP_NO");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private int getGradePos(int gradeInt) {
            final Integer pos = (Integer) _gradePosition.get(Integer.valueOf(String.valueOf(gradeInt)));
            if (null == pos) {
                log.warn("no grade position:" + gradeInt);
                return -1;
            }
            return pos.intValue();
        }

        /**
         * 学年名を得る
         */
        private String getGradeName(int gradeInt) {
            return (String) _gradeNames.get(Integer.valueOf(String.valueOf(gradeInt)));
        }


        /**
         * 写真データファイルの取得
         */
        private String getStampImageFile(final String filename) {
            if (null == filename) {
                return null;
            }
            if (null == _documentRoot) {
                return null;
            } // DOCUMENTROOT
            if (null == _imageDir) {
                return null;
            }
            if (null == _imageExt) {
                return null;
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(_documentRoot);
            stb.append("/");
            stb.append(_imageDir);
            stb.append("/");
            stb.append(filename);
            stb.append(".");
            stb.append(_imageExt);
            File file1 = new File(stb.toString());
            if (!file1.exists()) {
                return null;
            } // 写真データ存在チェック用
            return stb.toString();
        }
    }

    private class Date {
        private final String _date;
        private List _diaryList = new ArrayList();
        private List _visitrecList = new ArrayList();
        private List _visitrecCntList = new ArrayList();
        private List _attendCntList = new ArrayList();
        private String _sickT;
        private String _noticeT;
        private String _nonoticeT;
        private String _suspendT;
        private String _mourningT;
        private List _attendList = new ArrayList();

        private Date(final String date) {
            _date = date;
        }

        /**
         * 保健室日誌
         */
        private void setDiary(final DB2UDB db2) throws SQLException, ParseException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getDiarySql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Diary diary = new Diary(
                            rs.getString("WEATHER"),
                            rs.getString("WEATHER_TEXT"),
                            rs.getString("TEMPERATURE"),
                            rs.getString("EVENT"),
                            rs.getString("DIARY")
                    );
                    _diaryList.add(diary);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 保健室日誌
         */
        private String getDiarySql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     A006.NAME1 AS WEATHER, ");
            stb.append("     T1.WEATHER_TEXT, ");
            stb.append("     T1.TEMPERATURE, ");
            stb.append("     T1.EVENT, ");
            stb.append("     T1.DIARY ");
            stb.append(" FROM ");
            stb.append("     NURSEOFF_DIARY_DAT T1 ");
            stb.append("     LEFT JOIN NAME_MST A006 ON A006.NAMECD1 = 'A006' ");
            stb.append("                            AND A006.NAMECD2 = T1.WEATHER ");
            stb.append(" WHERE ");
            stb.append("     T1.DATE = '" + _date + "' ");
            if ("1".equals(_param._use_prg_schoolkind)) {
                stb.append("   AND T1.SCHOOL_KIND = '" + _param._schKind + "' ");
            } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOL_KIND)) {
                stb.append("   AND T1.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
            }
            return stb.toString();
        }

        /**
         * 保健室来室（傷病記録）
         */
        private void setVisitrec(final DB2UDB db2) throws SQLException, ParseException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getVisitrecSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Visitrec visitrec = new Visitrec(
                            rs.getString("HR_NAME"),
                            rs.getString("HR_NAMEABBV"),
                            rs.getString("ATTENDNO"),
                            rs.getString("NAME"),
                            rs.getString("SEX"),
                            "",
                            StringUtils.defaultString(rs.getString("VISIT_HOUR"), "  ") + ":" + StringUtils.defaultString(rs.getString("VISIT_MINUTE"), "  "),
                            "",
                            rs.getString("OCCUR_PLACE"),
                            rs.getString("OCCUR_CAUSE"),
                            rs.getString("TREATMENT")
                    );
                    _visitrecList.add(visitrec);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 保健室来室（傷病記録）
         */
        private String getVisitrecSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHREG_SEM AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, ");
            stb.append("         max(SEMESTER) as SEMESTER ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT ");
            stb.append("     WHERE ");
            stb.append("         YEAR = '" + _param._year + "' ");
            stb.append("     GROUP BY ");
            stb.append("         SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , T_SCHREG AS ( ");
            stb.append("     SELECT ");
            stb.append("         W1.SCHREGNO, ");
            stb.append("         W1.GRADE, ");
            stb.append("         W1.HR_CLASS, ");
            stb.append("         W3.HR_NAME, ");
            stb.append("         W3.HR_NAMEABBV, ");
            stb.append("         W1.ATTENDNO, ");
            stb.append("         W4.NAME, ");
            stb.append("         Z002.NAME2 AS SEX ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT W1 ");
            stb.append("         INNER JOIN SCHREG_SEM W2 ON W2.SCHREGNO = W1.SCHREGNO ");
            stb.append("                                 AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         INNER JOIN SCHREG_REGD_HDAT W3 ON W3.YEAR = W1.YEAR ");
            stb.append("                                       AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("                                       AND W3.GRADE = W1.GRADE ");
            stb.append("                                       AND W3.HR_CLASS = W1.HR_CLASS ");
            stb.append("         INNER JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("         LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
            stb.append("                                AND Z002.NAMECD2 = W4.SEX ");
            if ("1".equals(_param._use_prg_schoolkind)) {
                stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.GRADE ");
                stb.append("   AND T2.SCHOOL_KIND = '" + _param._schKind + "' ");
            } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOL_KIND)) {
                stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.GRADE ");
                stb.append("   AND T2.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
            }
            stb.append("     WHERE ");
            stb.append("         W1.YEAR = '" + _param._year + "' ");
            stb.append("     ) ");

            stb.append(" SELECT ");
            stb.append("     T2.HR_NAME, ");
            stb.append("     T2.HR_NAMEABBV, ");
            stb.append("     T2.ATTENDNO, ");
            stb.append("     T2.NAME, ");
            stb.append("     T2.SEX, ");
            stb.append("     T1.VISIT_HOUR, ");
            stb.append("     T1.VISIT_MINUTE, ");
            stb.append("     F206.NAME1 AS OCCUR_PLACE, ");
            stb.append("     OCCUR_CAUSE, ");
            stb.append("     TREATMENT, ");
            stb.append("     T2.GRADE, ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     NURSEOFF_VISIT_TEXT_DAT T1 ");
            stb.append("     INNER JOIN T_SCHREG T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST F206 ");
            stb.append("       ON F206.NAMECD1 = 'F206' ");
            stb.append("      AND F206.NAMECD2 = T1.OCCUR_PLACE ");
            stb.append(" WHERE ");
            stb.append("     T1.VISIT_DATE = '" + _date + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T2.GRADE, ");
            stb.append("     T2.HR_CLASS, ");
            stb.append("     T2.ATTENDNO, ");
            stb.append("     T1.VISIT_HOUR, ");
            stb.append("     T1.VISIT_MINUTE ");
            return stb.toString();
        }

//        /**
//         * 保健室来室（件数）
//         */
//        private void setVisitrecCnt(final DB2UDB db2) throws SQLException, ParseException {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                final String sql = getVisitrecCntSql();
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    final VisitrecCnt visitrecCnt = new VisitrecCnt(
//                            rs.getString("GRADE"),
//                            rs.getString("CNT")
//                    );
//                    _visitrecCntList.add(visitrecCnt);
//                }
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }

//        /**
//         * 保健室来室（件数）
//         */
//        private String getVisitrecCntSql() {
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" WITH SCHREG_SEM AS ( ");
//            stb.append("     SELECT ");
//            stb.append("         SCHREGNO, ");
//            stb.append("         max(SEMESTER) as SEMESTER ");
//            stb.append("     FROM ");
//            stb.append("         SCHREG_REGD_DAT ");
//            stb.append("     WHERE ");
//            stb.append("         YEAR = '" + _param._year + "' ");
//            stb.append("     GROUP BY ");
//            stb.append("         SCHREGNO ");
//            stb.append("     ) ");
//            stb.append(" , T_SCHREG AS ( ");
//            stb.append("     SELECT ");
//            stb.append("         W1.SCHREGNO, ");
//            stb.append("         W1.GRADE, ");
//            stb.append("         W1.HR_CLASS, ");
//            stb.append("         W3.HR_NAME, ");
//            stb.append("         W3.HR_NAMEABBV, ");
//            stb.append("         W1.ATTENDNO, ");
//            stb.append("         W4.NAME, ");
//            stb.append("         Z002.NAME2 AS SEX ");
//            stb.append("     FROM ");
//            stb.append("         SCHREG_REGD_DAT W1 ");
//            stb.append("         INNER JOIN SCHREG_SEM W2 ON W2.SCHREGNO = W1.SCHREGNO ");
//            stb.append("                                 AND W2.SEMESTER = W1.SEMESTER ");
//            stb.append("         INNER JOIN SCHREG_REGD_HDAT W3 ON W3.YEAR = W1.YEAR ");
//            stb.append("                                       AND W3.SEMESTER = W1.SEMESTER ");
//            stb.append("                                       AND W3.GRADE = W1.GRADE ");
//            stb.append("                                       AND W3.HR_CLASS = W1.HR_CLASS ");
//            stb.append("         INNER JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
//            stb.append("         LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
//            stb.append("                                AND Z002.NAMECD2 = W4.SEX ");
//            if ("1".equals(_param._use_prg_schoolkind)) {
//                stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.GRADE ");
//                stb.append("   AND T2.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
//            } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOL_KIND)) {
//                stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.GRADE ");
//                stb.append("   AND T2.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
//            }
//            stb.append("     WHERE ");
//            stb.append("         W1.YEAR = '" + _param._year + "' ");
//            stb.append("     ) ");
//
//            stb.append(" , T_VISITREC AS ( ");
//            stb.append("     SELECT ");
//            stb.append("         SCHREGNO, ");
//            stb.append("         VISIT_DATE, ");
//            stb.append("         VISIT_HOUR, ");
//            stb.append("         VISIT_MINUTE ");
//            stb.append("     FROM ");
//            stb.append("         NURSEOFF_VISIT_TEXT_DAT ");
//            stb.append("     WHERE ");
//            stb.append("         VISIT_DATE = '" + _date + "' ");
//            stb.append("     ) ");
//
//            stb.append(" SELECT ");
//            stb.append("     T2.GRADE, ");
//            stb.append("     COUNT(*) AS CNT ");
//            stb.append(" FROM ");
//            stb.append("     T_VISITREC T1 ");
//            stb.append("     INNER JOIN T_SCHREG T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//            stb.append(" WHERE ");
//            stb.append("     T1.VISIT_DATE = '" + _date + "' ");
//            stb.append(" GROUP BY ");
//            stb.append("     T2.GRADE ");
//            return stb.toString();
//        }

        /**
         * 出欠（件数）
         */
        private void setAttendCnt(final DB2UDB db2) throws SQLException, ParseException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int sickT = 0;
                int noticeT = 0;
                int nonoticeT = 0;
                int suspendT = 0;
                int mourningT = 0;
                final String sql;
                if ("1".equals(_param._useNurseoffAttend)) {
                    sql = getNurseoffAttendDat(_param, _date);
                } else {
                    sql = getAttendSemesSql(_param._definecode,
                            _param._year,
                            _param._semester,
                            _param._semester,
                            _param._periodInState,
                            _date,
                            _date,
                            "GRADE");
                }

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final int sick = rs.getInt("SICK1");
                    final int notice = rs.getInt("SICK2");
                    final int nonotice = rs.getInt("SICK3");
                    final int suspend = rs.getInt("SUSPEND");
                    final int mourning = rs.getInt("MOURNING");

                    sickT += sick;
                    noticeT += notice;
                    nonoticeT += nonotice;
                    suspendT += suspend;
                    mourningT += mourning;

                    final AttendCnt attendCnt = new AttendCnt(
                            grade,
                            String.valueOf(sick),
                            String.valueOf(notice),
                            String.valueOf(nonotice),
                            String.valueOf(suspend),
                            String.valueOf(mourning)
                    );
                    _attendCntList.add(attendCnt);
                }
                //計
                if (0 < sickT) _sickT = String.valueOf(sickT);
                if (0 < noticeT) _noticeT = String.valueOf(noticeT);
                if (0 < nonoticeT) _nonoticeT = String.valueOf(nonoticeT);
                if (0 < suspendT) _suspendT = String.valueOf(suspendT);
                if (0 < mourningT) _mourningT = String.valueOf(mourningT);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 出欠（欠席者一覧）
         */
        private void setAttend(final DB2UDB db2) throws SQLException, ParseException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int number = 0;
                final String sql = getAttendSemesSql(_param._definecode,
                _param._year,
                _param._semester,
                _param._semester,
                _param._periodInState,
                _date,
                _date,
                "SCHREGNO");

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    number++;
                    final Attend attend = new Attend(
                            String.valueOf(number),
                            rs.getString("HR_NAME"),
                            rs.getString("HR_NAMEABBV"),
                            rs.getString("ATTENDNO"),
                            rs.getString("NAME"),
                            rs.getString("SEX"),
                            rs.getString("DINAME"),
                            rs.getString("DIREMARK")
                    );
                    _attendList.add(attend);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    /**
     * 保健室日誌の内部クラス
     */
    private class Diary {
        private final String _weather;
        private final String _weatherText;
        private final String _temperature;
        private final String _event;
        private final String _diary;

        private Diary(
                final String weather,
                final String weatherText,
                final String temperature,
                final String event,
                final String diary
        ) {
            _weather = weather;
            _weatherText = weatherText;
            _temperature = temperature;
            _event = event;
            _diary = diary;
        }
    }

    /**
     * 保健室来室（傷病記録）の内部クラス
     */
    private class Visitrec {
        private final String _hrName;
        private final String _hrAbbv;
        private final String _attendno;
        private final String _name;
        private final String _sex;
        private final String _reason;
        private final String _hourMinute;
        private final String _leaveHourMinute;
        private final String _place;
        private final String _cause;
        private final String _treatment;

        private Visitrec(
                final String hrName,
                final String hrAbbv,
                final String attendno,
                final String name,
                final String sex,
                final String reason,
                final String hourMinute,
                final String leaveHourMinute,
                final String place,
                final String cause,
                final String treatment
        ) {
            _hrName = hrName;
            _hrAbbv = hrAbbv;
            _attendno = attendno;
            _name = name;
            _sex = sex;
            _reason = reason;
            _hourMinute = hourMinute;
            _leaveHourMinute = leaveHourMinute;
            _place = place;
            _cause = cause;
            _treatment = treatment;
        }
    }

    /**
     * 保健室来室（件数）の内部クラス
     */
    private class VisitrecCnt {
        private final String _grade;
        private final String _cnt;

        private VisitrecCnt(
                final String grade,
                final String cnt
        ) {
            _grade = grade;
            _cnt = cnt;
        }
    }

    /**
     * 出欠（件数）の内部クラス
     */
    private static class AttendCnt {
        private final String _grade;
        private final String _sick;
        private final String _notice;
        private final String _nonotice;
        private final String _suspend;
        private final String _mourning;

        private AttendCnt(
                final String grade,
                final String sick,
                final String notice,
                final String nonotice,
                final String suspend,
                final String mourning
        ) {
            _grade = grade;
            _sick = getVal(sick);
            _notice = getVal(notice);
            _nonotice = getVal(nonotice);
            _suspend = getVal(suspend);
            _mourning = getVal(mourning);
        }

        /**
         * ゼロは印字しない。
         */
        public static String getVal(final String val) {
            return "0".equals(val) ? null : val;
        }
    }

    private class StampData {
        private final String _seq;
        private final String _title;
        private final String _stampName;
        public StampData(
                final String seq,
                final String title,
                final String stampName
                ) {
            _seq = seq;
            _title = title;
            _stampName = stampName;
        }
    }

    /**
     * 出欠（欠席者一覧）の内部クラス
     */
    private class Attend {
        private final String _number;
        private final String _hrName;
        private final String _hrAbbv;
        private final String _attendno;
        private final String _name;
        private final String _sex;
        private final String _diName;
        private final String _diRemark;

        private Attend(
                final String number,
                final String hrName,
                final String hrAbbv,
                final String attendno,
                final String name,
                final String sex,
                final String diName,
                final String diRemark
        ) {
            _number = number;
            _hrName = hrName;
            _hrAbbv = hrAbbv;
            _attendno = attendno;
            _name = name;
            _sex = sex;
            _diName = diName;
            _diRemark = diRemark;
        }
    }
}

// eof
