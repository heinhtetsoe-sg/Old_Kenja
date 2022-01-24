// kanji=漢字
package servletpack.KNJC;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
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

/*
 *  学校教育システム 賢者 [出欠管理] 欠課状況集計表
 */

public class KNJC165B {

    private static final Log log = LogFactory.getLog(KNJC165B.class);

    private boolean _hasdata = false;

    Param _param = null;

    public PrintWriter outstrm;

    private static final String TYPE_GHR = "2";

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        // ＤＢ接続
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("db2 instancing exception! ", ex);
            return;
        }

        _hasdata = false;
        try {
            log.debug(" $Revision: 77416 $ $Date: 2020-10-13 20:53:22 +0900 (火, 13 10 2020) $ ");
            KNJServletUtils.debugParam(request, log);
            _param = new Param(request, db2);

            svf.VrInit();                             //クラスの初期化
            if (_param._testFlg) {
                final String fname = _param._year + "_" + _param._semesId + StringUtils.defaultString(_param._devName) + "_" +  StringUtils.defaultString(_param._courseCdName) + ".pdf";
                log.info("output FNAME :" + fname);
                //PDFの替わりにHtmlを出力する。KNJServletpacksvfANDdb2.closeSvfより
                response.setContentType("text/html");
                outstrm = new PrintWriter (response.getOutputStream());
                outstrm.println("<HTML>");
                outstrm.println("<HEAD>");
                outstrm.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
                outstrm.println("</HEAD>");
                outstrm.println("<BODY>");
                outstrm.println("<H1>保存しました。ファイル名【" + fname + "】</h1>");
                outstrm.println("</BODY>");
                outstrm.println("</HTML>");
                outstrm.close();            //ストリームを閉じる
                svf.VrSetSpoolFileStream(new FileOutputStream(new File(_param._filePath + "/" + fname)));         //PDFファイル名の設定
            } else {
                svf.VrSetSpoolFileStream(response.getOutputStream());  //dbg
            }
        } catch (java.io.IOException ex) {
            log.error("svf instancing exception! ", ex);
        }

        try {
            // 印刷処理
            printMain(db2, svf);
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            // 終了処理
            if (!_hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    /**
     *  印刷処理
     */
    private void printMain(
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        //学籍のSQL
        Map hrMap = getHrClasses(db2);

        final String form = "KNJC165B.frm";
        final int onePageMax = 34;
        int calcPageVal = 0;
        for (Iterator ite = hrMap.keySet().iterator();ite.hasNext();) {
            final String ymStr = (String)ite.next();
            final List wkLst = (List)hrMap.get(ymStr);
            String bakGrade = "";
            int gradeCnt = 0;

            for (Iterator itc = wkLst.iterator();itc.hasNext();) {
                final HrClass prtObj = (HrClass)itc.next();
                if (!"".equals(bakGrade) && !bakGrade.equals(prtObj._grade)) {
                    gradeCnt++;
                }
            }
            calcPageVal += (new BigDecimal((wkLst.size() + gradeCnt + 1) / (onePageMax * 1.0))).setScale(0, BigDecimal.ROUND_CEILING).intValue();
        }

        final int maxPage = calcPageVal;
        String bakMonth = "";

        int fultotal_calc_A = 0;
        int fultotal_calc_B = 0;
        int fultotal_calc_C = 0;
        int fultotal_calc_D = 0;
        int fultotal_calc_E = 0;
        int fultotal_calc_F = 0;
        int fultotal_lesson = 0;
        int total_calc_A = 0;
        int total_calc_B = 0;
        int total_calc_C = 0;
        int total_calc_D = 0;
        int total_calc_E = 0;
        int total_calc_F = 0;
        int total_lesson = 0;
        int pageCnt = 1;
        int lineCnt = 0;
        String bakGradeName = "";
        HrClass bakPrtObj = null;
        for (Iterator ite = hrMap.keySet().iterator();ite.hasNext();) {
            final String ymStr = (String)ite.next();
            final List wkLst = (List)hrMap.get(ymStr);
            for (Iterator itu = wkLst.iterator();itu.hasNext();) {
                final HrClass prtObj = (HrClass)itu.next();

                if (!"".equals(bakMonth) && !bakMonth.equals(prtObj._month)) {
                    printTotal(svf, "subTotal", bakGradeName, total_lesson, total_calc_A, total_calc_B, total_calc_C, total_calc_D, total_calc_E, total_calc_F);
                    lineCnt++;
                    if (lineCnt+1 == onePageMax) {
                        pageCnt++;
                        lineCnt = 0;
                        svf.VrEndPage();
                        svf.VrSetForm(form, 4);
                        setTitle(db2, svf, pageCnt, maxPage, prtObj._school_Kind, prtObj._normYear, prtObj._month);
                    }
                    printTotal(svf, "fullTotal", "", fultotal_lesson, fultotal_calc_A, fultotal_calc_B, fultotal_calc_C, fultotal_calc_D, fultotal_calc_E, fultotal_calc_F);
                    lineCnt++;
                    if (lineCnt == onePageMax) {
                        pageCnt++;
                        lineCnt = 0;
                        svf.VrEndPage();
                        svf.VrSetForm(form, 4);
                        setTitle(db2, svf, pageCnt, maxPage, prtObj._school_Kind, prtObj._normYear, prtObj._month);
                    }
                       pageCnt++;
                       lineCnt = 0;
                       bakGradeName = "";
                    total_calc_A = 0;
                    total_calc_B = 0;
                    total_calc_C = 0;
                    total_calc_D = 0;
                    total_calc_E = 0;
                    total_calc_F = 0;
                    total_lesson = 0;
                    fultotal_lesson = 0;
                    fultotal_calc_A = 0;
                    fultotal_calc_B = 0;
                    fultotal_calc_C = 0;
                    fultotal_calc_D = 0;
                    fultotal_calc_E = 0;
                    fultotal_calc_F = 0;
                } else if (!"".equals(bakGradeName) && !bakGradeName.equals(prtObj._grade_Name2)) {
                    printTotal(svf, "subTotal", bakGradeName, total_lesson, total_calc_A, total_calc_B, total_calc_C, total_calc_D, total_calc_E, total_calc_F);
                    lineCnt++;
                    if (lineCnt+1 == onePageMax) {
                        pageCnt++;
                        lineCnt = 0;
                        svf.VrEndPage();
                        svf.VrSetForm(form, 4);
                        setTitle(db2, svf, pageCnt, maxPage, prtObj._school_Kind, prtObj._normYear, prtObj._month);
                    }
                    total_lesson = 0;
                    total_calc_A = 0;
                    total_calc_B = 0;
                    total_calc_C = 0;
                    total_calc_D = 0;
                    total_calc_E = 0;
                    total_calc_F = 0;
                } else if (lineCnt == onePageMax) {
                    //printTotal
                    pageCnt++;
                    lineCnt = 0;
                }
                if (lineCnt == 0) {
                    svf.VrEndPage();
                    svf.VrSetForm(form, 4);
                    setTitle(db2, svf, pageCnt, maxPage, prtObj._school_Kind, prtObj._normYear, prtObj._month);
                }
                lineCnt++;
                svf.VrsOut("GRADE", prtObj._grade_Name2);
                svf.VrsOut("HR_NAME", prtObj._hr_Name);
                svf.VrsOut("PRESENT", prtObj._calc_A);
                total_lesson += Integer.parseInt(prtObj._lesson);
                fultotal_lesson += Integer.parseInt(prtObj._lesson);
                total_calc_A += Integer.parseInt(prtObj._calc_A);
                fultotal_calc_A += Integer.parseInt(prtObj._calc_A);
                svf.VrsOut("LEAVE", prtObj._calc_B);
                total_calc_B += Integer.parseInt(prtObj._calc_B);
                fultotal_calc_B += Integer.parseInt(prtObj._calc_B);
                svf.VrsOut("ATTEND", "0".equals(prtObj._lesson) ? "--" : prtObj._calc_C);
                total_calc_C += Integer.parseInt(prtObj._calc_C);
                fultotal_calc_C += Integer.parseInt(prtObj._calc_C);
                svf.VrsOut("NOTICE", "0".equals(prtObj._lesson) ? "--" : prtObj._calc_D);
                total_calc_D += Integer.parseInt(prtObj._calc_D);
                fultotal_calc_D += Integer.parseInt(prtObj._calc_D);
                if (!"".equals(StringUtils.defaultString(prtObj._calc_F, "")) && !"".equals(StringUtils.defaultString(prtObj._calc_E, ""))) {
                    BigDecimal putVal = (Integer.parseInt(prtObj._calc_E) == 0 ? new BigDecimal("0.0") : (new BigDecimal((Integer.parseInt(prtObj._calc_F) / (Integer.parseInt(prtObj._calc_E) * 1.0))*100.0)).setScale(2, BigDecimal.ROUND_HALF_UP));
                    svf.VrsOut("ATTEND_PER", "0".equals(prtObj._lesson) ? "--" : putVal.toString());
                    total_calc_E += Integer.parseInt(prtObj._calc_E);
                    fultotal_calc_E += Integer.parseInt(prtObj._calc_E);
                    total_calc_F += Integer.parseInt(prtObj._calc_F);
                    fultotal_calc_F += Integer.parseInt(prtObj._calc_F);
                }
                svf.VrsOut("REMARK", prtObj._remark1);
                bakMonth = prtObj._month;
                bakGradeName = prtObj._grade_Name2;
                bakPrtObj = prtObj;
                svf.VrEndRecord();
                _hasdata = true;
            }
            svf.VrEndPage();
        }
        if (_hasdata && bakPrtObj != null) {
            printTotal(svf, "subTotal", bakGradeName, total_lesson, total_calc_A, total_calc_B, total_calc_C, total_calc_D, total_calc_E, total_calc_F);
            lineCnt++;
            if (lineCnt+1 == onePageMax) {
                pageCnt++;
                lineCnt = 0;
                svf.VrEndPage();
                svf.VrSetForm(form, 4);
                setTitle(db2, svf, pageCnt, maxPage, bakPrtObj._school_Kind, bakPrtObj._normYear, bakPrtObj._month);
            }
            printTotal(svf, "fullTotal", "", fultotal_lesson, fultotal_calc_A, fultotal_calc_B, fultotal_calc_C, fultotal_calc_D, fultotal_calc_E, fultotal_calc_F);
        }
    }

    private void printTotal(final Vrw32alp svf, final String totalType, final String gradeName, final int total_lesson, final int total_calc_A, final int total_calc_B, final int total_calc_C, final int total_calc_D, final int total_calc_E, final int total_calc_F) {
        svf.VrsOut("GRADE", gradeName);
        svf.VrsOut("HR_NAME", "fullTotal".equals(totalType) ? "総計" : "小計");
        svf.VrsOut("PRESENT", String.valueOf(total_calc_A));
        svf.VrsOut("LEAVE", String.valueOf(total_calc_B));
        svf.VrsOut("ATTEND", 0 == total_lesson ? "--" : String.valueOf(total_calc_C));
        svf.VrsOut("NOTICE", 0 == total_lesson ? "--" : String.valueOf(total_calc_D));
        BigDecimal putVal = (total_calc_E == 0 ? new BigDecimal("0.0") : (new BigDecimal((total_calc_F / (total_calc_E * 1.0))*100.0)).setScale(2, BigDecimal.ROUND_HALF_UP));
        svf.VrsOut("ATTEND_PER", 0 == total_lesson ? "--" : putVal.toString());
        svf.VrEndRecord();
    }
    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final int nowPage, final int maxPage, final String schoolKind, final String datYear, final String datMonth) {
        svf.VrsOut("DATE", "(" + KNJ_EditDate.h_format_JP_M(db2, datYear + "-" + datMonth + "-01") + "分)");
        svf.VrsOut("TITLE", "児童生徒出欠席状況報告書");
        String schName = "";
        if (_param._schoolNameInfo.containsKey(schoolKind)) {
            schName = (String)_param._schoolNameInfo.get(schoolKind);
        }
        svf.VrsOut("SCHOOL_NAME", schName + "　" + StringUtils.defaultString(_param._courseCdName));
        svf.VrsOut("PAGE", nowPage + "/" + maxPage);
    }

    private Map getHrClasses(final DB2UDB db2) {
        Map retMap = new LinkedMap();
        List subList = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sqlRegdH = getSql();
        log.debug("getHrClasses sql:"+ sqlRegdH);

        try {

            ps = db2.prepareStatement(sqlRegdH);
            rs = ps.executeQuery();

            while(rs.next()) {
                final String school_Kind = rs.getString("SCHOOL_KIND");
                final String abbv1 = rs.getString("ABBV1");
                final String normYear = rs.getString("NORMYEAR");
                final String grade = rs.getString("GRADE");
                final String grade_Name2 = rs.getString("GRADE_NAME2");
                final String hr_Class = rs.getString("HR_CLASS");
                final String hr_Name = rs.getString("HR_NAME");
                final String month = rs.getString("MONTH");
                final String calc_A = rs.getString("CALC_A");
                final String calc_B = rs.getString("CALC_B");
                final String calc_C = rs.getString("CALC_C");
                final String calc_D = rs.getString("CALC_D");
                final String calc_E = rs.getString("CALC_E");
                final String calc_F = rs.getString("CALC_F");
                final String lesson = rs.getString("LESSON");
                final String offdays = rs.getString("OFFDAYS");
                final String absent = rs.getString("ABSENT");
                final String suspend = rs.getString("SUSPEND");
                final String mourning = rs.getString("MOURNING");
                final String abroad = rs.getString("ABROAD");
                final String sick = rs.getString("SICK");
                final String notice = rs.getString("NOTICE");
                final String nonotice = rs.getString("NONOTICE");
                final String late = rs.getString("LATE");
                final String early = rs.getString("EARLY");
                final String kekka_Jisu = rs.getString("KEKKA_JISU");
                final String kekka = rs.getString("KEKKA");
                final String virus = rs.getString("VIRUS");
                final String koudome = rs.getString("KOUDOME");
                final String remark1 = rs.getString("REMARK1");

                final HrClass hrClass = new HrClass(school_Kind, abbv1, normYear, grade, grade_Name2, hr_Class, hr_Name, month, calc_A, calc_B, calc_C, calc_D, calc_E, calc_F, lesson, offdays, absent, suspend, mourning, abroad, sick, notice, nonotice, late, early, kekka_Jisu, kekka, virus, koudome, remark1);

                if (!retMap.containsKey(normYear + month)) {
                    subList = new ArrayList();
                    retMap.put(normYear + month, subList);
                }
                subList.add(hrClass);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        }

        return retMap;
    }

    private String getSql() {
        final int nextYear = Integer.parseInt(_param._year) + 1;
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH BASE_ATT AS ( ");
        stb.append(" SELECT ");
        stb.append("     T3.SCHOOL_KIND, ");
        stb.append("     T1.SEMESTER, ");
        if (TYPE_GHR.equals(_param._hrClsType)) {
            stb.append("     'X' AS GRADE, ");  //何も無いのは処理として不味い(学年の切り替わり判定がある)ので、文字で代用。
            stb.append("     T5.GHR_CD, ");
        } else {
            stb.append("     T2.GRADE, ");
            stb.append("     T2.HR_CLASS, ");
        }
        stb.append("     T1.YEAR AS NORMYEAR, ");
        stb.append("     T1.MONTH, ");
        stb.append("     SUM(CASE WHEN ( ");
        stb.append("                T1.YEAR || '-' || T1.MONTH || '-01' <= T4.TRANSFER_SDATE AND T4.TRANSFER_SDATE <= LAST_DAY(T1.YEAR || '-' || T1.MONTH || '-01') ");  //開始月判定
        stb.append("               ) OR ( ");
        stb.append("                T4.TRANSFER_SDATE <= T1.YEAR || '-' || T1.MONTH || '-01' AND LAST_DAY(T1.YEAR || '-' || T1.MONTH || '-01') <= T4.TRANSFER_EDATE ");  //中間～終了月判定
        stb.append("               ) ");
        stb.append("          THEN 1 ELSE 0 END) AS TRANSFLG, ");
        stb.append("     T2.SCHREGNO, ");
        stb.append("     SUM(VALUE(T1.LESSON, 0)) AS LESSON, ");
        stb.append("     SUM(VALUE(T1.OFFDAYS, 0)) AS OFFDAYS, ");
        stb.append("     SUM(VALUE(T1.ABSENT, 0)) AS ABSENT, ");
        stb.append("     SUM(VALUE(T1.SUSPEND, 0)) AS SUSPEND, ");
        stb.append("     SUM(VALUE(T1.MOURNING, 0)) AS MOURNING, ");
        stb.append("     SUM(VALUE(T1.ABROAD, 0)) AS ABROAD, ");
        stb.append("     SUM(VALUE(T1.SICK, 0)) AS SICK, ");
        stb.append("     SUM(VALUE(T1.NOTICE, 0)) AS NOTICE, ");
        stb.append("     SUM(VALUE(T1.NONOTICE, 0)) AS NONOTICE, ");
        stb.append("     SUM(VALUE(T1.LATE, 0)) AS LATE, ");
        stb.append("     SUM(VALUE(T1.EARLY, 0)) AS EARLY, ");
        stb.append("     SUM(VALUE(T1.KEKKA_JISU, 0)) AS KEKKA_JISU, ");
        stb.append("     SUM(VALUE(T1.KEKKA, 0)) AS KEKKA, ");
        stb.append("     SUM(VALUE(T1.VIRUS, 0)) AS VIRUS, ");
        stb.append("     SUM(VALUE(T1.KOUDOME, 0)) AS KOUDOME ");
        stb.append(" FROM ");
        stb.append("     ATTEND_SEMES_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("       ON T2.YEAR = T1.YEAR ");
        stb.append("      AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
        if (TYPE_GHR.equals(_param._hrClsType)) {
            stb.append("     INNER JOIN SCHREG_REGD_GHR_DAT T5 ");
            stb.append("       ON T5.YEAR = T1.YEAR ");
            stb.append("      AND T5.SEMESTER = T1.SEMESTER ");
            stb.append("      AND T5.SCHREGNO = T1.SCHREGNO ");
        }
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT T3 ");
        stb.append("       ON T3.YEAR = T2.YEAR ");
        stb.append("      AND T3.GRADE = T2.GRADE ");
        stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT T4 ");
        stb.append("      ON T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("     AND T4.TRANSFERCD = '2' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR || T1.MONTH BETWEEN '" + _param.getSDateYM() + "' AND '" + _param.getEDateYM() + "' ");
        stb.append("     AND T1.COPYCD = '0' ");
        stb.append("     AND T3.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("     AND T2.COURSECD = '" + _param._courseCd + "' ");
        stb.append(" AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append(                           " AND S1.GRD_DIV <> '4' ");
        stb.append(                           " AND ((S1.GRD_DATE < (CASE WHEN T1.MONTH BETWEEN '01' AND '03' THEN '" + nextYear + "' ELSE T1.YEAR END) || '-' || T1.MONTH || '-01') ");
        stb.append(                           " OR (S1.ENT_DATE > LAST_DAY((CASE WHEN T1.MONTH BETWEEN '01' AND '03' THEN '" + nextYear + "' ELSE T1.YEAR END) || '-' || T1.MONTH || '-01'))) ) ");
        stb.append(" GROUP BY ");
        stb.append("     T3.SCHOOL_KIND, ");
        stb.append(" T1.YEAR, T1.SEMESTER, T2.GRADE, ");
        if (TYPE_GHR.equals(_param._hrClsType)) {  //複式はGHRCD
            stb.append("     T5.GHR_CD, ");
        } else {
            stb.append("     T2.HR_CLASS, ");
        }
        stb.append("     T1.MONTH, T2.SCHREGNO ");
        stb.append(" ), ATT_SAMMARY_BASE AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     NORMYEAR, ");
        stb.append("     SEMESTER, ");
        stb.append("     GRADE, ");
        if (TYPE_GHR.equals(_param._hrClsType)) {
            stb.append("     GHR_CD, ");
        } else {
            stb.append("     HR_CLASS, ");
        }
        stb.append("     MONTH, ");
        stb.append("     COUNT(SCHREGNO) AS CNT, ");
        stb.append("     SUM(CASE WHEN (OFFDAYS > 0 OR TRANSFLG > 0) THEN 1 ELSE 0 END) AS OFFCNT, ");
        stb.append("     SUM(CASE WHEN (OFFDAYS > 0 OR TRANSFLG > 0) OR ABROAD > 0 THEN 1 ELSE 0 END) AS OFFCNT2, ");
        stb.append("     SUM(CASE WHEN VALUE(LESSON, 0) <= VALUE(SICK, 0) + VALUE(NOTICE, 0) + VALUE(NONOTICE, 0) THEN 1 ELSE 0 END) AS FULLABSENT, ");
        stb.append("     SUM(LESSON) AS LESSON, ");
        stb.append("     SUM(OFFDAYS) AS OFFDAYS, ");
        stb.append("     SUM(ABSENT) AS ABSENT, ");
        stb.append("     SUM(SUSPEND) AS SUSPEND, ");
        stb.append("     SUM(MOURNING) AS MOURNING, ");
        stb.append("     SUM(ABROAD) AS ABROAD, ");
        stb.append("     SUM(SICK) AS SICK, ");
        stb.append("     SUM(NOTICE) AS NOTICE, ");
        stb.append("     SUM(NONOTICE) AS NONOTICE, ");
        stb.append("     SUM(LATE) AS LATE, ");
        stb.append("     SUM(EARLY) AS EARLY, ");
        stb.append("     SUM(KEKKA_JISU) AS KEKKA_JISU, ");
        stb.append("     SUM(KEKKA) AS KEKKA, ");
        stb.append("     SUM(VIRUS) AS VIRUS, ");
        stb.append("     SUM(KOUDOME) AS KOUDOME ");
        stb.append(" FROM ");
        stb.append("     BASE_ATT ");
        stb.append(" GROUP BY ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     NORMYEAR, ");
        stb.append("     SEMESTER, ");
        stb.append("     GRADE, ");
        if (TYPE_GHR.equals(_param._hrClsType)) {
            stb.append("     GHR_CD, ");
        } else {
            stb.append("     HR_CLASS, ");
        }
        stb.append("     MONTH ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     TF1.SCHOOL_KIND, ");
        stb.append("     TF1.NORMYEAR, ");
        stb.append("     TF4.ABBV1, ");
        stb.append("     TF1.GRADE, ");
        if (TYPE_GHR.equals(_param._hrClsType)) {
            stb.append("     '' AS GRADE_NAME2, ");
            stb.append("     TF1.GHR_CD AS HR_CLASS, ");
            stb.append("     TF2.GHR_NAME AS HR_NAME, ");
        } else {
            stb.append("     TF3.GRADE_NAME2, ");
            stb.append("     TF1.HR_CLASS, ");
            stb.append("     TF2.HR_NAME, ");
        }

        stb.append("     TF1.MONTH, ");
        stb.append("     TF1.CNT AS CALC_A, ");
        stb.append("     TF1.OFFCNT AS CALC_B, ");
        stb.append("     (TF1.CNT - TF1.OFFCNT2) AS CALC_C, ");
        stb.append("     TF1.FULLABSENT AS CALC_D, ");
        stb.append("     VALUE(TF1.LESSON, 0) - (VALUE(TF1.OFFDAYS, 0) + VALUE(TF1.ABROAD, 0) + VALUE(TF1.SUSPEND, 0) + VALUE(TF1.MOURNING, 0)) AS CALC_E, ");
        stb.append("     VALUE(TF1.LESSON, 0) - (VALUE(TF1.OFFDAYS, 0) + VALUE(TF1.ABROAD, 0) + VALUE(TF1.SUSPEND, 0) + VALUE(TF1.MOURNING, 0)) - (VALUE(TF1.SICK, 0) + VALUE(TF1.NOTICE, 0) + VALUE(TF1.NONOTICE, 0)) AS CALC_F, ");
        stb.append("     TF1.LESSON, ");
        stb.append("     TF1.OFFDAYS, ");
        stb.append("     TF1.ABSENT, ");
        stb.append("     TF1.SUSPEND, ");
        stb.append("     TF1.MOURNING, ");
        stb.append("     TF1.ABROAD, ");
        stb.append("     TF1.SICK, ");
        stb.append("     TF1.NOTICE, ");
        stb.append("     TF1.NONOTICE, ");
        stb.append("     TF1.LATE, ");
        stb.append("     TF1.EARLY, ");
        stb.append("     TF1.KEKKA_JISU, ");
        stb.append("     TF1.KEKKA, ");
        stb.append("     TF1.VIRUS, ");
        stb.append("     TF1.KOUDOME ");
        if (TYPE_GHR.equals(_param._hrClsType)) {
            stb.append("     ,'' AS REMARK1 ");
        } else {
            stb.append("     ,TF5.REMARK1 ");
        }
        stb.append(" FROM ");
        stb.append("     ATT_SAMMARY_BASE TF1 ");
        if (TYPE_GHR.equals(_param._hrClsType)) {
            stb.append("     LEFT JOIN SCHREG_REGD_GHR_HDAT TF2 ");
            stb.append("       ON TF2.YEAR = '" + _param._year + "' ");
            stb.append("      AND TF2.SEMESTER = TF1.SEMESTER ");
            stb.append("      AND TF2.GHR_CD = TF1.GHR_CD ");
        } else {
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT TF2 ");
            stb.append("       ON TF2.YEAR = '" + _param._year + "' ");
            stb.append("      AND TF2.SEMESTER = TF1.SEMESTER ");
            stb.append("      AND TF2.GRADE = TF1.GRADE ");
            stb.append("      AND TF2.HR_CLASS = TF1.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT TF3 ");
            stb.append("       ON TF3.YEAR = TF1.NORMYEAR ");
            stb.append("      AND TF3.GRADE = TF1.GRADE ");
        }
        stb.append("     LEFT JOIN NAME_MST TF4 ");
        stb.append("       ON TF4.NAMECD1 = 'A023' ");
        stb.append("      AND TF4.NAME1 = TF1.SCHOOL_KIND ");
        if (!TYPE_GHR.equals(_param._hrClsType)) {
            stb.append("     LEFT JOIN ATTEND_SEMES_REMARK_HR_SENDPREF_DAT TF5 ");
            stb.append("       ON TF5.YEAR  = TF1.NORMYEAR ");
            stb.append("      AND TF5.MONTH = TF1.MONTH ");
            stb.append("      AND TF5.SEMESTER = TF1.SEMESTER ");
            stb.append("      AND TF5.GRADE = TF1.GRADE ");
            stb.append("      AND TF5.HR_CLASS = TF1.HR_CLASS ");
        }
        stb.append(" ORDER BY ");
        stb.append("     TF1.NORMYEAR, ");
        stb.append("     TF1.MONTH, ");
        stb.append("     TF1.GRADE, ");
        if (TYPE_GHR.equals(_param._hrClsType)) {
            stb.append("     TF1.GHR_CD ");
        } else {
            stb.append("     TF1.HR_CLASS ");
        }
        return stb.toString();
    }

    private class HrClass {
        final String _school_Kind;
        final String _abbv1;
        final String _normYear;
        final String _grade;
        final String _grade_Name2;
        final String _hr_Class;
        final String _hr_Name;
        final String _month;
        final String _calc_A;
        final String _calc_B;
        final String _calc_C;
        final String _calc_D;
        final String _calc_E;
        final String _calc_F;
        final String _lesson;
        final String _offdays;
        final String _absent;
        final String _suspend;
        final String _mourning;
        final String _abroad;
        final String _sick;
        final String _notice;
        final String _nonotice;
        final String _late;
        final String _early;
        final String _kekka_Jisu;
        final String _kekka;
        final String _virus;
        final String _koudome;
        final String _remark1;
        public HrClass (final String school_Kind, final String abbv1, final String normYear, final String grade, final String grade_Name2, final String hr_Class, final String hr_Name, final String month, final String calc_A, final String calc_B, final String calc_C, final String calc_D, final String calc_E, final String calc_F, final String lesson, final String offdays, final String absent, final String suspend, final String mourning, final String abroad, final String sick, final String notice, final String nonotice, final String late, final String early, final String kekka_Jisu, final String kekka, final String virus, final String koudome, final String remark1)
        {
            _school_Kind = school_Kind;
            _abbv1 = abbv1;
            _normYear = normYear;
            _grade = grade;
            _grade_Name2 = grade_Name2;
            _hr_Class = hr_Class;
            _hr_Name = hr_Name;
            _month = month;
            _calc_A = calc_A;
            _calc_B = calc_B;
            _calc_C = calc_C;
            _calc_D = calc_D;
            _calc_E = calc_E;
            _calc_F = calc_F;
            _lesson = lesson;
            _offdays = offdays;
            _absent = absent;
            _suspend = suspend;
            _mourning = mourning;
            _abroad = abroad;
            _sick = sick;
            _notice = notice;
            _nonotice = nonotice;
            _late = late;
            _early = early;
            _kekka_Jisu = kekka_Jisu;
            _kekka = kekka;
            _virus = virus;
            _koudome = koudome;
            _remark1 = remark1;
        }
    }

    private static class Param {
        final String _year;
        final String _semesId;  //集計期間(3学期ベース)
        final String _date;
        final String _devName;
        final String _courseCd;
        final String _schoolKind;
        String _sdate;
        String _edate;
        final String _courseCdName;
        final Map _schoolNameInfo;
        final String _schoolCd;
        final String _filePath;
        final boolean _testFlg;
        final String _hrClsType;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semesId  = request.getParameter("SEMES_ID");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _schoolCd = "000000000000";
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _courseCd = request.getParameter("COURSECD");
            _testFlg = "upload".equals(request.getParameter("cmd"));
            _hrClsType = request.getParameter("HR_CLASS_TYPE");
            _sdate = "";
            _edate = "";
            setSemesterDate(db2);
            _schoolNameInfo = getSchoolName(db2);
            _filePath = getFilePath(db2);
            _courseCdName = getCourseName(db2);

            _devName = (String)_schoolNameInfo.get(_schoolKind);
        }

        private String getCourseName(final DB2UDB db2) {
            final String sql = " SELECT COURSENAME from COURSE_MST WHERE COURSECD = '" + _courseCd + "' ";
            log.debug("getCourseName sql:"+ sql);
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
        }
        private String getFilePath(final DB2UDB db2) {
            final String sql = " SELECT FILE_PATH from UPLOAD_FILE_PATH_DAT WHERE SEQ = '1' ";
            log.debug("getFilePath sql:"+ sql);
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
        }
        private void setSemesterDate(final DB2UDB db2) {
            final List semesId_SInf = new ArrayList();  //集計開始日
            final List semesId_EInf = new ArrayList();  //集計終了日
            //設定している固定値は、後の処理で文字の切り取り位置を揃えるために"0"付きでないといけないので注意。
            semesId_SInf.add(_year + "/04/01");
            semesId_EInf.add(_year + "/08/31");
            semesId_SInf.add(_year + "/09/01");
            semesId_EInf.add(_year + "/12/31");
            semesId_SInf.add(_year + "/01/01");
            semesId_EInf.add(_year + "/03/31");
            _sdate = (String)semesId_SInf.get(Integer.parseInt(_semesId) - 1);
            _edate = (String)semesId_EInf.get(Integer.parseInt(_semesId) - 1);
              return;
        }
        private String getSDateYM() {
            String retStr = "";
            if (!"".equals(_sdate) && _sdate.length() > 0) {
                retStr = StringUtils.replace(_sdate, "-", "");
                retStr = StringUtils.replace(retStr, "/", "");
                if (retStr.length() > 6) {
                    retStr = retStr.substring(0,6);
                }
            }
            return retStr;

        }
        private String getEDateYM() {
            String retStr = "";
            if (!"".equals(_edate) && _edate.length() > 0) {
                retStr = StringUtils.replace(_edate, "-", "");
                retStr = StringUtils.replace(retStr, "/", "");
                if (retStr.length() > 6) {
                    retStr = retStr.substring(0,6);
                }
            }
            return retStr;
        }

        private Map getSchoolName(final DB2UDB db2) {
            Map retSchoolNameMap = new LinkedMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.SCHOOL_KIND, ");
            stb.append("   T1.SCHOOLNAME1 ");
            stb.append(" FROM ");
            stb.append("   SCHOOL_MST T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _year + "' ");
            stb.append("   AND T1.SCHOOLCD = '" + _schoolCd + "' ");

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    retSchoolNameMap.put((String)rs.getString("SCHOOL_KIND"), (String)rs.getString("SCHOOLNAME1"));
                }
            } catch (SQLException ex) {
                log.debug("getSchoolName exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolNameMap;
        }
    }
}
