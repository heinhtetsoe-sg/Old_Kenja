/*
 * $Id: fdf66389cdb650f50437c1aa362688a641df1f8f $
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.pdf.AlpPdf;
import servletpack.pdf.IPdf;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD186V_AlpPdf extends KNJD186V {

    private static final Log log = LogFactory.getLog(KNJD186V.class);

    private Map getFieldTransMap() {

        final Map fieldTransMap = new TreeMap();
        if (true) {
            return fieldTransMap;
        }
        fieldTransMap.put("ATTENDNO", "出席番号");
        fieldTransMap.put("ATTEND", "出席日数");
        fieldTransMap.put("TOTAL_SCORE1_010108", "テスト1合計");
        fieldTransMap.put("TOTAL_SCORE1_990008", "テスト2合計");
        fieldTransMap.put("TOTAL_SCORE2_010108", "テスト3合計");
        fieldTransMap.put("TOTAL_SCORE2_990008", "テスト4合計");
        fieldTransMap.put("TOTAL_SCORE9_990008", "テスト5合計");
        fieldTransMap.put("TOTAL_SCORE9_990009", "評定合計");
        fieldTransMap.put("SCORE_AVERAGE1_010108", "テスト1平均");
        fieldTransMap.put("SCORE_AVERAGE1_990008", "テスト2平均");
        fieldTransMap.put("SCORE_AVERAGE2_010108", "テスト3平均");
        fieldTransMap.put("SCORE_AVERAGE2_990008", "テスト4平均");
        fieldTransMap.put("SCORE_AVERAGE9_990008", "テスト5平均");
        fieldTransMap.put("SCORE_AVERAGE9_990009", "評定平均");
        fieldTransMap.put("AVERAGE1_010108", "テスト1平均");
        fieldTransMap.put("AVERAGE1_990008", "テスト2平均");
        fieldTransMap.put("AVERAGE2_010108", "テスト3平均");
        fieldTransMap.put("AVERAGE2_990008", "テスト4平均");
        fieldTransMap.put("AVERAGE9_990008", "テスト5平均");
        fieldTransMap.put("COMMUNICATION", "通信欄");
        fieldTransMap.put("EARLY", "早退日数");
        fieldTransMap.put("GRADE_RANK1_010108", "テスト1指定順位");
        fieldTransMap.put("GRADE_RANK1_990008", "テスト2指定順位");
        fieldTransMap.put("GRADE_RANK2_010108", "テスト3指定順位");
        fieldTransMap.put("GRADE_RANK2_990008", "テスト4指定順位");
        fieldTransMap.put("GRADE_RANK9_990008", "テスト5指定順位");
        fieldTransMap.put("GRADE_RANK9_990009", "評定指定順位");
        fieldTransMap.put("HR_CLASS_RANK1_010108", "テスト1組順位");
        fieldTransMap.put("HR_CLASS_RANK1_990008", "テスト2組順位");
        fieldTransMap.put("HR_CLASS_RANK2_010108", "テスト3組順位");
        fieldTransMap.put("HR_CLASS_RANK2_990008", "テスト4組順位");
        fieldTransMap.put("HR_CLASS_RANK9_990008", "テスト5組順位");
        fieldTransMap.put("HR_CLASS_RANK9_990009", "評定組順位");
        fieldTransMap.put("HR_NAME", "HR名称");
        fieldTransMap.put("ITEMNAME", "順位名称");
        fieldTransMap.put("TOTAL_KEKKA", "欠課数");
        fieldTransMap.put("KEKKA", "欠課数");
        fieldTransMap.put("KETTEN_CNT1_010108", "テスト1欠点科目数");
        fieldTransMap.put("KETTEN_CNT1_990008", "テスト2欠点科目数");
        fieldTransMap.put("KETTEN_CNT2_010108", "テスト3欠点科目数");
        fieldTransMap.put("KETTEN_CNT2_990008", "テスト4欠点科目数");
        fieldTransMap.put("KETTEN_CNT9_990008", "テスト5欠点科目数");
        fieldTransMap.put("KETTEN_CNT9_990009", "評定欠点科目数");
        fieldTransMap.put("LATE", "遅刻日数");
        fieldTransMap.put("LESSON", "授業日数");
        fieldTransMap.put("MAJORNAME", "学科名");
        fieldTransMap.put("MAX_VALUE1_010108", "テスト1最高点");
        fieldTransMap.put("MAX_VALUE1_990008", "テスト2最高点");
        fieldTransMap.put("MAX_VALUE2_010108", "テスト3最高点");
        fieldTransMap.put("MAX_VALUE2_990008", "テスト4最高点");
        fieldTransMap.put("MAX_VALUE9_990008", "テスト5最高点");
        fieldTransMap.put("MOCK_NAME", "実力テスト名称");
        fieldTransMap.put("NENDO", "年度");
        fieldTransMap.put("PRESENT", "出席すべき日数");
        fieldTransMap.put("PROF_AVERAGE1", "実力テスト1平均点");
        fieldTransMap.put("PROF_AVERAGE2", "実力テスト2平均点");
        fieldTransMap.put("PROF_AVERAGE3", "実力テスト3平均点");
        fieldTransMap.put("PROF_CNT1", "実力テスト1順位人数");
        fieldTransMap.put("PROF_CNT2", "実力テスト2順位人数");
        fieldTransMap.put("PROF_CNT3", "実力テスト3順位人数");
        fieldTransMap.put("PROF_RANK1", "実力テスト1順位");
        fieldTransMap.put("PROF_RANK2", "実力テスト2順位");
        fieldTransMap.put("PROF_RANK3", "実力テスト3順位");
        fieldTransMap.put("PROF_SCORE1", "実力テスト1科目点数");
        fieldTransMap.put("PROF_SCORE2", "実力テスト2科目点数");
        fieldTransMap.put("PROF_SCORE3", "実力テスト3科目点数");
        fieldTransMap.put("PROF_SUBCLASS", "実力テスト科目名");
        fieldTransMap.put("PROF_TESTNAME1", "実力テスト1名称");
        fieldTransMap.put("PROF_TESTNAME2", "実力テスト2名称");
        fieldTransMap.put("PROF_TESTNAME3", "実力テスト3名称");
        fieldTransMap.put("PROF_TOTALSCORE1", "実力テスト1総点");
        fieldTransMap.put("PROF_TOTALSCORE2", "実力テスト2総点");
        fieldTransMap.put("PROF_TOTALSCORE3", "実力テスト3総点");
        fieldTransMap.put("SCHOOLNAME", "学校名");
        fieldTransMap.put("SCH_INFO1", "学科HR");
        fieldTransMap.put("SCH_INFO2", "コメント補足");
        fieldTransMap.put("SCORE1_010108", "テスト1得点");
        fieldTransMap.put("SCORE1_990008", "テスト2得点");
        fieldTransMap.put("SCORE2_010108", "テスト3得点");
        fieldTransMap.put("SCORE2_990008", "テスト4得点");
        fieldTransMap.put("SCORE9_990008", "テスト5得点");
        fieldTransMap.put("SCORE9_990009", "評定");
        fieldTransMap.put("SEMESTER", "学期");
        fieldTransMap.put("SICK", "欠席日数");
        fieldTransMap.put("STAFFNAME1", "担任1氏名");
        fieldTransMap.put("STAFFNAME2", "担任2氏名");
        fieldTransMap.put("SUSPEND", "出停忌引日数");
        fieldTransMap.put("TESTNAME1_010108_MAX", "テスト1最高名称");
        fieldTransMap.put("TESTNAME1_010108", "テスト1名称");
        fieldTransMap.put("TESTNAME1_990008_MAX", "テスト2最高名称");
        fieldTransMap.put("TESTNAME1_990008", "テスト2名称");
        fieldTransMap.put("TESTNAME2_010108_MAX", "テスト3最高名称");
        fieldTransMap.put("TESTNAME2_010108", "テスト3名称");
        fieldTransMap.put("TESTNAME2_990008_MAX", "テスト4最高名称");
        fieldTransMap.put("TESTNAME2_990008_2", "テスト4名称2");
        fieldTransMap.put("TESTNAME2_990008", "テスト4名称");
        fieldTransMap.put("TESTNAME9_990008_MAX2", "テスト5最高名称2");
        fieldTransMap.put("TESTNAME9_990008_MAX", "テスト5最高名称");
        fieldTransMap.put("TESTNAME9_990008_2", "テスト5名称2");
        fieldTransMap.put("TESTNAME9_990008_AVE", "評定名称");
        fieldTransMap.put("TESTNAME9_990008", "テスト5名称");
        fieldTransMap.put("SUBCLASSNAME", "科目名");
        fieldTransMap.put("CLASSNAME", "教科名");
        fieldTransMap.put("NAME", "生徒氏名");
        fieldTransMap.put("GET_CREDIT", "修得単位数");
        fieldTransMap.put("CREDIT", "単位数");
        return fieldTransMap;
    }

    public Boolean print(final String basePath, final HttpServletRequest request, final OutputStream pdfresponse) throws Exception {

        AlpPdf alppdf = null;
        Boolean rtn = Boolean.FALSE;
        try {
            //outputDebug(request);
            
            alppdf = new AlpPdf(basePath, pdfresponse);

            outputPdf(alppdf, request);
            
        } catch (final Exception ex) {
            log.error("exception!", ex);
            rtn = Boolean.TRUE;
        } finally {
            if (null != alppdf) {
                alppdf.close();
            }
        }
        return rtn;
    }
    
    public void outputPdf(
            final IPdf ipdf,
            final HttpServletRequest request
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            Param param = createParam(request, db2);

            if (PATTERN_A.equals(param._patarnDiv)) {
                ((AlpPdf) ipdf).setFieldTranslateMap(getFieldTransMap());
            }

            printMain(db2, ipdf, param);

        } catch (final Exception ex) {
            log.error("error! ", ex);
            throw ex;
        } finally {
            try {
                db2.commit();
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }
        }
    }
    
}
