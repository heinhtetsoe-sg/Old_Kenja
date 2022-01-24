// kanji=漢字
/*
 * $Id: c728c968415a947d8a299f1dbbd554490a5a0e1b $
 *
 * 作成日: 2010/01/25 15:31:41 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.SvfField;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: c728c968415a947d8a299f1dbbd554490a5a0e1b $
 */
public class KNJA240A {

    private static final Log log = LogFactory.getLog("KNJA240A.class");

    private final int MAX_COUNT = 15;

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

            _param = createParam(db2, request);

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

    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printHrAll = getPrintHr(db2);

        List current;
        final List printHrGradeAll = new ArrayList();
        current = null;
        String oldGrade = null;
        for (final Iterator it = printHrAll.iterator(); it.hasNext();) {
            final Hr hr = (Hr) it.next();
            if (null == current || (null == oldGrade && null != hr._grade || null != oldGrade && !oldGrade.equals(hr._grade))) {
                current = new ArrayList();
                printHrGradeAll.add(current);
            }
            current.add(hr);
            oldGrade = hr._grade;
        }
        // 1ページあたりの学年の数
        final int maxGrade = "2".equals(_param._form) ? 6 : "3".equals(_param._form) ? 3 : 4;
        final List printStudentsPage = getPageList(printHrGradeAll, maxGrade);

        final String form;
        if ("2".equals(_param._form)) {
            if ("1".equals(_param._noSeibetsu)) {
                // 6年用性別欄無し
                form = "KNJA240A_6_2.frm";
            } else {
                // 6年用
                form = "KNJA240A_6_1.frm";
            }
        } else if ("3".equals(_param._form)) {
            if ("1".equals(_param._noSeibetsu)) {
                // 3年用性別欄無し
                form = "KNJA240A_3_2.frm";
            } else {
                // 3年用
                form = "KNJA240A_3_1.frm";
            }
        } else {
            // 4年用
            if ("1".equals(_param._noSeibetsu)) {
                // 4年用性別欄無し
                form = "KNJA240A_2.frm";
            } else {
                // 4年用
                form = "KNJA240A.frm";
            }
        }

        //休学
        final List kyugakuList = getPrintKyugaku(db2, svf);
        final List kyugakuPageList = getPageList(kyugakuList, "1".equals(_param._noSeibetsu) ? 30 : 27);

        svf.VrSetForm(form, 1);

        final String nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._year)) + "年度";
        final String loginDateFormat = KNJ_EditDate.getAutoFormatDate(db2, _param._loginDate);
        final String kijunDateFormat = KNJ_EditDate.getAutoFormatDate(db2, _param._kijunDate) + "現在";

        try {
        	_param._fieldMap = SvfField.getSvfFormFieldInfoMapGroupByName(svf);
        } catch (Throwable t) {
        	log.error("exceptioN!" + t);
        }
        //１学年から４学年まで
        //合計行
        int cntManAll = 0;
        int cntWomanAll = 0;
        int cntTotalAll = 0;
        int cntManSAll = 0;
        int cntWomanSAll = 0;
        int cntTotalSAll = 0;
        for (int pagei = 0; pagei < printStudentsPage.size(); pagei++) { // ページの学年のリスト
            final List printStudentsGrade = (List) printStudentsPage.get(pagei);
            //最大クラス数を取得
            int classMaxCnt = 0;
            for (int gi = 0; gi < printStudentsGrade.size(); gi++) { // 学年のHRリスト
                final List printStudents = (List) printStudentsGrade.get(gi);
                if (classMaxCnt < printStudents.size()) {
                    classMaxCnt = printStudents.size();
                }
            }
            //学年の最大ページ数を取得
            int loopCnt = (classMaxCnt / MAX_COUNT) + 1;

            String strGradeName[] = new String[maxGrade];
            int cntManSum[] = new int[maxGrade];
            int cntWomanSum[] = new int[maxGrade];
            int cntTotalSum[] = new int[maxGrade];
            int cntManSSum[] = new int[maxGrade];
            int cntWomanSSum[] = new int[maxGrade];
            int cntTotalSSum[] = new int[maxGrade];

            for (int li = 0; li < loopCnt; li++) {
                for (int gi = 0; gi < printStudentsGrade.size(); gi++) { // 学年のHRリスト
                    final List printStudents = (List) printStudentsGrade.get(gi);

                    int gyo = 0;
                    int cntMan = 0;
                    int cntWoman = 0;
                    int cntTotal = 0;
                    int cntManS = 0;
                    int cntWomanS = 0;
                    int cntTotalS = 0;

                    String len = String.valueOf(gi + 1);

                    for (int si = li * MAX_COUNT; si < (li + 1) * MAX_COUNT; si++) {
                        if (printStudents.size() - 1 < si) break;
                        final Hr hr = (Hr) printStudents.get(si);
                        //行と列
                        gyo += 1;

                        //学年
                        strGradeName[gi] = hr._gradeName;

                        //組
                        String strNo = getLengthName(svf, "CLASS" + len, hr.getHrClassName());
                        svf.VrsOutn("CLASS" + len + strNo, gyo, hr.getHrClassName());
                        //担任名
                        String strNo2 = getLengthName(svf, "TEACHER" + len, hr._staffname);
                        svf.VrsOutn("TEACHER" + len + strNo2, gyo, hr._staffname);
                        //男女計
                        svf.VrsOutn("CNT" + len + "_1",  gyo, hr._cnt1);
                        svf.VrsOutn("CNT" + len + "_1S", gyo, hr._cnt1S);
                        svf.VrsOutn("CNT" + len + "_2",  gyo, hr._cnt2);
                        svf.VrsOutn("CNT" + len + "_2S", gyo, hr._cnt2S);
                        svf.VrsOutn("CNT" + len + "_3",  gyo, hr._cnt3);
                        svf.VrsOutn("CNT" + len + "_3S", gyo, hr._cnt3S);
                        //ヘッダ
                        svf.VrsOut("nendo", nendo);
                        svf.VrsOut("TODAY", loginDateFormat);
                        svf.VrsOut("DATE",  kijunDateFormat);

                        //カウント
                        cntMan    += Integer.parseInt(hr._cnt1);
                        cntWoman  += Integer.parseInt(hr._cnt2);
                        cntTotal  += Integer.parseInt(hr._cnt3);
                        cntManS   += Integer.parseInt(hr._cnt1S);
                        cntWomanS += Integer.parseInt(hr._cnt2S);
                        cntTotalS += Integer.parseInt(hr._cnt3S);

                        //合計カウント
                        cntManAll    += Integer.parseInt(hr._cnt1);
                        cntWomanAll  += Integer.parseInt(hr._cnt2);
                        cntTotalAll  += Integer.parseInt(hr._cnt3);
                        cntManSAll   += Integer.parseInt(hr._cnt1S);
                        cntWomanSAll += Integer.parseInt(hr._cnt2S);
                        cntTotalSAll += Integer.parseInt(hr._cnt3S);
                    }

                    //学年
                    if ("2".equals(_param._form) && "1".equals(_param._noSeibetsu)) {
                        svf.VrsOutn("GRADE2", Integer.parseInt(len), strGradeName[gi]);
                    } else {
                        svf.VrsOutn("GRADE", Integer.parseInt(len), strGradeName[gi]);
                    }

                    cntManSum[gi] += cntMan;
                    cntWomanSum[gi] += cntWoman;
                    cntTotalSum[gi] += cntTotal;
                    cntManSSum[gi] += cntManS;
                    cntWomanSSum[gi] += cntWomanS;
                    cntTotalSSum[gi] += cntTotalS;

                    if (loopCnt - 1 == li) { // 最後のページに表示
                        //小計
                        svf.VrsOutn("CNT" + len + "_1",  16, String.valueOf(cntManSum[gi]));
                        svf.VrsOutn("CNT" + len + "_1S", 16, String.valueOf(cntManSSum[gi]));
                        svf.VrsOutn("CNT" + len + "_2",  16, String.valueOf(cntWomanSum[gi]));
                        svf.VrsOutn("CNT" + len + "_2S", 16, String.valueOf(cntWomanSSum[gi]));
                        svf.VrsOutn("CNT" + len + "_3",  16, String.valueOf(cntTotalSum[gi]));
                        svf.VrsOutn("CNT" + len + "_3S", 16, String.valueOf(cntTotalSSum[gi]));
                    }
                }
                if (printStudentsPage.size() - 1 == pagei) { // 最後のページに表示
                    if (loopCnt - 1 == li) {
                        //合計
                        svf.VrsOut("TOTALCNT1",  String.valueOf(cntManAll));
                        svf.VrsOut("TOTALCNT1S", String.valueOf(cntManSAll));
                        svf.VrsOut("TOTALCNT2",  String.valueOf(cntWomanAll));
                        svf.VrsOut("TOTALCNT2S", String.valueOf(cntWomanSAll));
                        svf.VrsOut("TOTALCNT3",  String.valueOf(cntTotalAll));
                        svf.VrsOut("TOTALCNT3S", String.valueOf(cntTotalSAll));
                    }

                    if (kyugakuPageList.size() > 0) {
                        printKyugaku(svf, (List) kyugakuPageList.get(0));
                    }
                }
                //最終行出力
                svf.VrEndPage();
                _hasData = true;
            }
        }

        if (kyugakuPageList.size() > 1) {
            for (int pi = 1; pi < kyugakuPageList.size(); pi++) {
                //ヘッダ
                svf.VrsOut("nendo", nendo);
                svf.VrsOut("TODAY", loginDateFormat);
                svf.VrsOut("DATE",  kijunDateFormat);

                //合計
                svf.VrsOut("TOTALCNT1",  String.valueOf(cntManAll));
                svf.VrsOut("TOTALCNT1S", String.valueOf(cntManSAll));
                svf.VrsOut("TOTALCNT2",  String.valueOf(cntWomanAll));
                svf.VrsOut("TOTALCNT2S", String.valueOf(cntWomanSAll));
                svf.VrsOut("TOTALCNT3",  String.valueOf(cntTotalAll));
                svf.VrsOut("TOTALCNT3S", String.valueOf(cntTotalSAll));

                printKyugaku(svf, (List) kyugakuPageList.get(pi));
                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

    public void printKyugaku(final Vrw32alp svf, final List kyugakuPage1) {
        for (int i = 0; i < kyugakuPage1.size(); i++) {
            final Map m = (Map) kyugakuPage1.get(i);
            for (final Iterator it = m.keySet().iterator(); it.hasNext();) {
                final String field = (String) it.next();
                final String val = (String) m.get(field);
                svf.VrsOutn(field, i + 1, val);
            }
        }
    }

    private String getLengthName(final Vrw32alp svf, final String field, final String str) {
    	final int dataKeta = KNJ_EditEdit.getMS932ByteLength(str);
    	String rtn = null;
		if (10 < dataKeta) {
    		if (null != _param._fieldMap) {
        		try {
        			final SvfField f2 = (SvfField) _param._fieldMap.get(field + "_2");
        			final SvfField f3 = (SvfField) _param._fieldMap.get(field + "_3");
        			if (null != f2 && null != f3) {
        				if (f2._fieldLength < f3._fieldLength && f2._fieldLength < dataKeta) {
        					rtn = "_3";
        				} else if (f2._fieldLength > f3._fieldLength && dataKeta <= f3._fieldLength) {
        					rtn = "_3";
        				}
        			}
        		} catch (Throwable t) {
        			log.error("exception! ", t);
        		}
    		}
    		if (null == rtn) {
    			rtn = "_2";
    		}
    	} else {
    		rtn = "_1";
    	}
    	return rtn;
    }

    private List getPrintKyugaku(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String studentSql = getKyugakuSql();
        //log.debug(" kyugaku sql = " + studentSql);
        final List list = new ArrayList();
        try {
            ps = db2.prepareStatement(studentSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Map m = new HashMap();
                m.put("HR_CLASS", "(" + rs.getString("HR_NAMEABBV") + ")");
                m.put("NAME", rs.getString("NAME_SHOW"));
                m.put("kubun", rs.getString("NAIYOU"));
                m.put("GRD_DATE", formatDate(rs.getString("DATE")));
                list.add(m);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String formatDate(String date) {
        if (null == date) {
            return null;
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTime(Date.valueOf(date));
        return (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH);
    }

    private String getKyugakuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     '1' AS ORDER_CD, ");
        stb.append("     T2.SCHOOL_KIND, ");
        stb.append("     Z.GRADE, ");
        stb.append("     Z.HR_CLASS, ");
        stb.append("     Z.SCHREGNO, ");
        stb.append("     M.NAME_SHOW, ");
        stb.append("     ST3.HR_NAMEABBV, ");
        stb.append("     CASE ");
        stb.append("         D.TRANSFERCD ");
        stb.append("             WHEN '1' THEN '留学:' ");
        stb.append("             WHEN '2' THEN '休学:' ");
        stb.append("             END ");
        stb.append("             AS NAIYOU, ");
        stb.append("     D.TRANSFER_SDATE AS DATE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT Z ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = Z.YEAR AND T2.GRADE = Z.GRADE ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                stb.append(" AND T2.SCHOOL_KIND IN (" + _param.getInState() + ") ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append("     AND T2.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append(" LEFT JOIN ");
        stb.append("     SCHREG_TRANSFER_DAT D ON ( Z.SCHREGNO = D.SCHREGNO ) ");
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_BASE_MST M ON ( Z.SCHREGNO = M.SCHREGNO ) ");
        stb.append(" INNER JOIN SCHREG_REGD_HDAT ST3 ON Z.YEAR = ST3.YEAR AND Z.SEMESTER = ST3.SEMESTER ");
        stb.append("                                 AND Z.GRADE = ST3.GRADE AND Z.HR_CLASS = ST3.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("         Z.YEAR       =  '" + _param._year + "' ");
        stb.append("     AND Z.SEMESTER   =  '" + _param._semester + "' ");
        stb.append("     AND T2.SCHOOL_KIND IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _param._year + "' AND NAMECD1 = 'A023') ");
        stb.append("     AND M.SEX        IN ('1','2') ");
        stb.append("     AND D.TRANSFERCD IN ('1','2') ");
        stb.append("     AND '" + _param._kijunDate + "'  BETWEEN D.TRANSFER_SDATE AND D.TRANSFER_EDATE ");

        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '2' AS ORDER_CD, ");
        stb.append("     T2.SCHOOL_KIND, ");
        stb.append("     Z.GRADE, ");
        stb.append("     Z.HR_CLASS, ");
        stb.append("     Z.SCHREGNO, ");
        stb.append("     M.NAME_SHOW, ");
        stb.append("     ST3.HR_NAMEABBV, ");
        stb.append("     NM.NAME1 AS NAIYOU, ");
        stb.append("     M.ENT_DATE AS DATE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT Z ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = Z.YEAR AND T2.GRADE = Z.GRADE ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                stb.append(" AND T2.SCHOOL_KIND IN (" + _param.getInState() + ") ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append("     AND T2.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_BASE_MST M ON ( Z.SCHREGNO = M.SCHREGNO ) ");
        stb.append(" INNER JOIN SCHREG_REGD_HDAT ST3 ON Z.YEAR = ST3.YEAR AND Z.SEMESTER = ST3.SEMESTER ");
        stb.append("                                 AND Z.GRADE = ST3.GRADE AND Z.HR_CLASS = ST3.HR_CLASS ");
        stb.append(" LEFT JOIN NAME_MST NM ON NM.NAMECD1 = 'A002' ");
        stb.append("      AND NM.NAMECD2 = M.ENT_DIV ");
        stb.append(" WHERE ");
        stb.append("         Z.YEAR       =  '" + _param._year + "' ");
        stb.append("     AND Z.SEMESTER   =  '" + _param._semester + "' ");
        stb.append("     AND T2.SCHOOL_KIND IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _param._year + "' AND NAMECD1 = 'A023') ");
        stb.append("     AND M.SEX        IN ('1','2') ");
        stb.append("     AND M.ENT_DIV IN ('4','5','7') ");
        stb.append("     AND M.ENT_DATE BETWEEN '" + _param._year + "-04-01' AND '" + _param._kijunDate + "' ");

        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '3' AS ORDER_CD, ");
        stb.append("     T2.SCHOOL_KIND, ");
        stb.append("     Z.GRADE, ");
        stb.append("     Z.HR_CLASS, ");
        stb.append("     Z.SCHREGNO, ");
        stb.append("     M.NAME_SHOW, ");
        stb.append("     ST3.HR_NAMEABBV, ");
        stb.append("     NM.NAME1 AS NAIYOU, ");
        stb.append("     M.GRD_DATE AS DATE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT Z ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = Z.YEAR AND T2.GRADE = Z.GRADE ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                stb.append(" AND T2.SCHOOL_KIND IN (" + _param.getInState() + ") ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append("     AND T2.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_BASE_MST M ON ( Z.SCHREGNO = M.SCHREGNO ) ");
        stb.append(" INNER JOIN SCHREG_REGD_HDAT ST3 ON Z.YEAR = ST3.YEAR AND Z.SEMESTER = ST3.SEMESTER ");
        stb.append("                                 AND Z.GRADE = ST3.GRADE AND Z.HR_CLASS = ST3.HR_CLASS ");
        stb.append(" LEFT JOIN NAME_MST NM ON NM.NAMECD1 = 'A003' ");
        stb.append("      AND NM.NAMECD2 = M.GRD_DIV ");
        stb.append(" WHERE ");
        stb.append("         Z.YEAR       =  '" + _param._year + "' ");
        stb.append("     AND Z.SEMESTER   =  '" + _param._semester + "' ");
        stb.append("     AND T2.SCHOOL_KIND IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _param._year + "' AND NAMECD1 = 'A023') ");
        stb.append("     AND M.SEX        IN ('1','2') ");
        stb.append("     AND M.GRD_DIV IN ('2','3','6','7') ");
        stb.append("     AND M.GRD_DATE BETWEEN '" + _param._year + "-04-01' AND '" + _param._kijunDate + "' ");

        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '4' AS ORDER_CD, ");
        stb.append("     T2.SCHOOL_KIND, ");
        stb.append("     Z.GRADE, ");
        stb.append("     Z.HR_CLASS, ");
        stb.append("     Z.SCHREGNO, ");
        stb.append("     M.NAME_SHOW, ");
        stb.append("     ST3.HR_NAMEABBV, ");
        stb.append("     '転籍' AS NAIYOU, ");
        stb.append("     M.ENT_DATE AS DATE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT Z ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = Z.YEAR AND T2.GRADE = Z.GRADE ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                stb.append(" AND T2.SCHOOL_KIND IN (" + _param.getInState() + ") ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append("     AND T2.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_BASE_MST M ON ( Z.SCHREGNO = M.SCHREGNO ) ");
        stb.append(" INNER JOIN SCHREG_REGD_HDAT ST3 ON Z.YEAR = ST3.YEAR AND Z.SEMESTER = ST3.SEMESTER ");
        stb.append("                                 AND Z.GRADE = ST3.GRADE AND Z.HR_CLASS = ST3.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("         Z.YEAR       =  '" + _param._year + "' ");
        stb.append("     AND Z.SEMESTER   =  '" + _param._semester + "' ");
        stb.append("     AND T2.SCHOOL_KIND IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _param._year + "' AND NAMECD1 = 'A023') ");
        stb.append("     AND M.SEX        IN ('1','2') ");
        stb.append("     AND Z.SCHREGNO IN ( ");
        stb.append("                    SELECT ");
        stb.append("                        I1.SCHREGNO ");
        stb.append("                    FROM ");
        stb.append("                        SCHREG_BASE_HIST_DAT I1 ");
        stb.append("                    WHERE ");
        stb.append("                        I1.ISSUEDATE BETWEEN '" + _param._year + "-04-01' AND '" + _param._kijunDate + "' ");
        stb.append("                        AND I1.COURSECD_FLG = '1' ");
        stb.append("                    ) ");

        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '5' AS ORDER_CD, ");
        stb.append("     T2.SCHOOL_KIND, ");
        stb.append("     Z.GRADE, ");
        stb.append("     Z.HR_CLASS, ");
        stb.append("     Z.SCHREGNO, ");
        stb.append("     M.NAME_SHOW, ");
        stb.append("     ST3.HR_NAMEABBV, ");
        stb.append("     '転科' AS NAIYOU, ");
        stb.append("     M.ENT_DATE AS DATE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT Z ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = Z.YEAR AND T2.GRADE = Z.GRADE ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                stb.append(" AND T2.SCHOOL_KIND IN (" + _param.getInState() + ") ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append("     AND T2.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_BASE_MST M ON ( Z.SCHREGNO = M.SCHREGNO ) ");
        stb.append(" INNER JOIN SCHREG_REGD_HDAT ST3 ON Z.YEAR = ST3.YEAR AND Z.SEMESTER = ST3.SEMESTER ");
        stb.append("                                 AND Z.GRADE = ST3.GRADE AND Z.HR_CLASS = ST3.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("         Z.YEAR       =  '" + _param._year + "' ");
        stb.append("     AND Z.SEMESTER   =  '" + _param._semester + "' ");
        stb.append("     AND T2.SCHOOL_KIND IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _param._year + "' AND NAMECD1 = 'A023') ");
        stb.append("     AND M.SEX        IN ('1','2') ");
        stb.append("     AND Z.SCHREGNO IN ( ");
        stb.append("                    SELECT ");
        stb.append("                        I1.SCHREGNO ");
        stb.append("                    FROM ");
        stb.append("                        SCHREG_BASE_HIST_DAT I1 ");
        stb.append("                    WHERE ");
        stb.append("                        I1.ISSUEDATE BETWEEN '" + _param._year + "-04-01' AND '" + _param._kijunDate + "' ");
        stb.append("                        AND I1.MAJORCD_FLG = '1' ");
        stb.append("                    ) ");
        stb.append(" ORDER BY ");
        stb.append("     ORDER_CD, ");
        stb.append("     SCHOOL_KIND DESC, ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     SCHREGNO ");
        return stb.toString();
    }

    private List getPrintHr(final DB2UDB db2) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String hrSql = getHrSql();
        try {
            ps = db2.prepareStatement(hrSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String gradeName = rs.getString("GRADE_NAME1");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrClassName = rs.getString("HR_CLASS_NAME1");
                final String cnt1 = rs.getString("CNT_1");
                final String cnt2 = rs.getString("CNT_2");
                final String cnt3 = rs.getString("CNT_3");
                final String cnt1S = rs.getString("CNT_1S");
                final String cnt2S = rs.getString("CNT_2S");
                final String cnt3S = rs.getString("CNT_3S");
                final String staffname = rs.getString("STAFFNAME");
                final Hr hr = new Hr(
                        grade,
                        gradeName,
                        hrClass,
                        hrClassName,
                        cnt1,
                        cnt2,
                        cnt3,
                        cnt1S,
                        cnt2S,
                        cnt3S,
                        staffname
                        );
                rtnList.add(hr);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getHrSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VALUE(nen_dan,0)             AS CNT_1, ");
        stb.append("     VALUE(nen_jo,0)              AS CNT_2, ");
        stb.append("     VALUE(nen_danjo_kei,0)       AS CNT_3, ");
        stb.append("     VALUE(nen_ya_dan,0)          AS CNT_1S, ");
        stb.append("     VALUE(nen_ya_jo,0)           AS CNT_2S, ");
        stb.append("     VALUE(nen_ya_danjo_kei,0)    AS CNT_3S, ");
        stb.append("     TBL3.SCHOOL_KIND, ");
        stb.append("     TBL3.GRADE_NAME1, ");
        stb.append("     TBL3.GRADE , ");
        stb.append("     TBL3.HR_CLASS , ");
        stb.append("     TBL3.HR_CLASS_NAME1, ");
        stb.append("     TBL4.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     (SELECT ");
        stb.append("         T2.SCHOOL_KIND, ");
        stb.append("         T2.GRADE_NAME1, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.HR_CLASS_NAME1 ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_HDAT T1 ");
        stb.append("         INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                stb.append("     AND T2.SCHOOL_KIND IN (" + _param.getInState() + ") ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append("         AND T2.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append("     WHERE ");
        stb.append("         T1.YEAR       =  '" + _param._year + "' AND ");
        stb.append("         T1.SEMESTER   =  '" + _param._semester + "' AND ");
        stb.append("         T2.SCHOOL_KIND IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _param._year + "' AND NAMECD1 = 'A023') ");
        stb.append("     ) TBL3  ");

        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             Sum(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS nen_dan, ");
        stb.append("             Sum(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS nen_jo, ");
        stb.append("             Sum(CASE WHEN SEX IN ('1','2') THEN 1 ELSE 0 END) AS nen_danjo_kei, ");
        stb.append("             Z.GRADE, ");
        stb.append("             Z.HR_CLASS ");
        stb.append("         FROM ");
        stb.append("             SCHREG_REGD_HDAT Z1, ");
        stb.append("             SCHREG_REGD_DAT Z, ");
        stb.append("             SCHREG_BASE_MST M ");
        stb.append("         WHERE ");
        stb.append("             Z.YEAR       =  '" + _param._year + "' AND ");
        stb.append("             Z.SEMESTER   =  '" + _param._semester + "' AND ");
        stb.append("             Z.YEAR       = Z1.YEAR AND ");
        stb.append("             Z.SEMESTER   = Z1.SEMESTER AND ");
        stb.append("             Z.GRADE      = Z1.GRADE AND ");
        stb.append("             Z.HR_CLASS   = Z1.HR_CLASS AND ");
        stb.append("             Z.SCHREGNO   = M.SCHREGNO AND ");
        stb.append("             M.SEX        IN ('1','2') AND ");
        stb.append("             NOT EXISTS (SELECT ");
        stb.append("                             'X' ");
        stb.append("                         FROM ");
        stb.append("                             SCHREG_BASE_MST T1 ");
        stb.append("                         WHERE ");
        stb.append("                             T1.SCHREGNO    = Z.SCHREGNO AND ");
        stb.append("                             T1.GRD_DATE    < '" + _param._kijunDate + "' AND ");
        if("hirokoudai".equals(_param._schoolName)) {
            stb.append("                             T1.GRD_DIV   IN ('1', '2','3','6','7') ");
        } else {
            stb.append("                             T1.GRD_DIV   IN ('1', '2','3','6') ");
        }
        stb.append("                         ) AND ");
        stb.append("             NOT EXISTS (SELECT ");
        stb.append("                             'X' ");
        stb.append("                         FROM ");
        stb.append("                             SCHREG_BASE_MST T1 ");
        stb.append("                         WHERE ");
        stb.append("                             T1.SCHREGNO      = Z.SCHREGNO AND ");
        stb.append("                             T1.ENT_DATE   > '" + _param._kijunDate + "' AND ");
        stb.append("                             T1.ENT_DIV   IN ('4','5') ");
        stb.append("                         ) ");
        stb.append("         GROUP BY ");
        stb.append("             Z.GRADE, ");
        stb.append("             Z.HR_CLASS ");
        stb.append("     )TBL1 ON TBL1.GRADE = TBL3.GRADE AND TBL1.HR_CLASS = TBL3.HR_CLASS ");

        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             Sum(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS nen_ya_dan, ");
        stb.append("             Sum(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS nen_ya_jo, ");
        stb.append("             Sum(CASE WHEN SEX IN ('1','2') THEN 1 ELSE 0 END) AS nen_ya_danjo_kei, ");
        stb.append("             Z.GRADE, ");
        stb.append("             Z.HR_CLASS ");
        stb.append("         FROM ");
        stb.append("             SCHREG_REGD_DAT Z ");
        stb.append("             LEFT JOIN SCHREG_TRANSFER_DAT D ON Z.SCHREGNO = D.SCHREGNO ");
        stb.append("             INNER JOIN SCHREG_BASE_MST M ON Z.SCHREGNO = M.SCHREGNO ");
        stb.append("             INNER JOIN SCHREG_REGD_HDAT Z1 ON Z.YEAR = Z1.YEAR AND Z.SEMESTER = Z1.SEMESTER AND Z.GRADE = Z1.GRADE AND Z.HR_CLASS = Z1.HR_CLASS ");
        stb.append("         WHERE ");
        stb.append("             Z.YEAR       =  '" + _param._year + "'AND ");
        stb.append("             Z.SEMESTER   =  '" + _param._semester + "' AND ");
        stb.append("             M.SEX        IN ('1','2') AND ");
        stb.append("             D.TRANSFERCD IN ('1','2') AND ");
        stb.append("             '" + _param._kijunDate + "'  BETWEEN D.TRANSFER_SDATE AND ");
        stb.append("             D.TRANSFER_EDATE ");
        stb.append("         GROUP BY ");
        stb.append("             Z.GRADE, ");
        stb.append("             Z.HR_CLASS ");
        stb.append("     )TBL2 ON TBL2.GRADE = TBL3.GRADE AND TBL2.HR_CLASS = TBL3.HR_CLASS ");

        stb.append("     LEFT JOIN  ( ");
        stb.append("         SELECT ");
        stb.append("             Z.GRADE, ");
        stb.append("             Z.HR_CLASS, ");
        stb.append("             (CASE WHEN T2.STAFFNAME IS NOT NULL THEN T2.STAFFNAME ");
        stb.append("                   WHEN T3.STAFFNAME IS NOT NULL THEN T3.STAFFNAME ");
        stb.append("                  ELSE T4.STAFFNAME END) AS STAFFNAME ");
        stb.append("         FROM ");
        stb.append("             SCHREG_REGD_HDAT Z ");
        stb.append("             LEFT JOIN STAFF_MST T2 ON Z.TR_CD1 = T2.STAFFCD ");
        stb.append("             LEFT JOIN STAFF_MST T3 ON Z.TR_CD2 = T3.STAFFCD ");
        stb.append("             LEFT JOIN STAFF_MST T4 ON Z.TR_CD3 = T4.STAFFCD ");
        stb.append("     WHERE ");
        stb.append("       Z.YEAR       =  '" + _param._year + "'AND ");
        stb.append("       Z.SEMESTER   =  '" + _param._semester + "' ");
        stb.append("     )TBL4 ON TBL4.GRADE = TBL3.GRADE AND TBL4.HR_CLASS = TBL3.HR_CLASS ");

        stb.append(" WHERE ");
        stb.append("     0 < VALUE(nen_danjo_kei,0) OR 0 < VALUE(nen_ya_danjo_kei,0) ");
        stb.append(" ORDER BY ");
        stb.append("     TBL3.SCHOOL_KIND DESC, ");
        stb.append("     TBL3.GRADE, ");
        stb.append("     TBL3.HR_CLASS ");
        return stb.toString();
    }

    private class Hr {
        final String _grade;
        final String _gradeName;
        final String _hrClass;
        final String _hrClassName;
        final String _cnt1;
        final String _cnt2;
        final String _cnt3;
        final String _cnt1S;
        final String _cnt2S;
        final String _cnt3S;
        final String _staffname;

        Hr(final String grade,
                final String gradeName,
                final String hrClass,
                final String hrClassName,
                final String cnt1,
                final String cnt2,
                final String cnt3,
                final String cnt1S,
                final String cnt2S,
                final String cnt3S,
                final String staffname
        ) {
            _grade = grade;
            _gradeName = gradeName;
            _hrClass = hrClass;
            _hrClassName = hrClassName;
            _cnt1 = cnt1;
            _cnt2 = cnt2;
            _cnt3 = cnt3;
            _cnt1S = cnt1S;
            _cnt2S = cnt2S;
            _cnt3S = cnt3S;
            _staffname = staffname;
        }

        private String getHrClassName() {
            if (null != _hrClassName) {
                return _hrClassName + "組";
            } else if (null != _hrClass) {
                return (StringUtils.isNumeric(_hrClass) ? String.valueOf(Integer.parseInt(_hrClass)) : _hrClass) + "組";
            } else {
                return "";
            }
        }

    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72965 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _kijunDate;
        private final String _loginDate;
        // private String _useSchregRegdHdat;
        private final String _form; // 1:4年用 2:6年用 3:3年用
        private final String _noSeibetsu; // 性別欄無し
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;
        final String[] _selectSchoolKinds;
        Map _fieldMap = null;
        final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _kijunDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _loginDate = request.getParameter("LOGIN_DATE");
            // _useSchregRegdHdat = request.getParameter("useSchregRegdHdat");
            _form = request.getParameter("FORM");
            _noSeibetsu = request.getParameter("NO_SEIBETSU");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            _selectSchoolKinds = StringUtils.split(_selectSchoolKind, ":");

            _schoolName = getNameMst(db2, "NAME1", "Z010", "00");
        }

        private String getInState() {
            final StringBuffer stb = new StringBuffer();
            String sep = "";
            for (int i = 0; i < _selectSchoolKinds.length; i++) {
                stb.append(sep + "'" + _selectSchoolKinds[i] + "'");
                sep = ",";
            }
            return stb.toString();
        }

        //名称マスタの取得
        public String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

    }
}

// eof
