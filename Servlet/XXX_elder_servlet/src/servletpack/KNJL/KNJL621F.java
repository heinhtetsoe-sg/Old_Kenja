/*
 * $Id: 223fda7d6360827323fee78c3dc41ac7ec943e1b $
 *
 * 作成日: 2019/12/20
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL621F {

    private static final Log log = LogFactory.getLog(KNJL621F.class);

    private static final String PRINT_FORM_1 = "KNJL621F_1.frm";
    private static final String PRINT_FORM_2 = "KNJL621F_2.frm";
    private static final String PRINT_FORM_3 = "KNJL621F_3.frm";
    private static final String PRINT_FORM_4 = "KNJL621F_4.frm";
    private static final String PRINT_FORM_5 = "KNJL621F_5.frm";
    private static final String PRINT_FORM_6 = "KNJL621F_6.frm";

    private static final int MAX_LINE = 20;

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

            //入試結果
            printMain(db2, svf);

            //志望者一覧、成績一覧
            printMainList(db2, svf);

            _hasData = true;

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

    public void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final String year = _param._entexamyear;
        final String lastYear = String.valueOf(Integer.parseInt(year) - 1);
        String lastNendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)-1) + "年度";

        final String title1 = "入試結果　";
        final String title2 = "1".equals(_param._testdiv) ? "内部・" + _param._testdivName : _param._testdivName;
        svf.VrSetForm(PRINT_FORM_1, 1);
        svf.VrsOut("TITLE", title1 + title2); // タイトル

        //入学試験結果
        svf.VrsOut("EXAM_TYPE", _param._testdivName); // タイトル
        final List testResultList = Result.load(db2, _param, year, _param._testdiv, false);
        for (int pi = 0; pi < testResultList.size(); pi++) {
            final Result  result = (Result) testResultList.get(pi);
            printResult(db2, svf, result, "1", "");
        }

        //昨年度入学試験結果
        svf.VrsOut("LAST_YEAR", lastNendo); // タイトル
        final List lastYearTestResult = Result.load(db2, _param, lastYear, _param._testdiv, false);
        for (int pi = 0; pi < lastYearTestResult.size(); pi++) {
            final Result  result = (Result) lastYearTestResult.get(pi);
            printResult(db2, svf, result, "1", "LAST_");
        }

        if("1".equals(_param._testdiv)){
             //認定試験結果
            final List certificationResult = Result.load(db2, _param, year, "2", false);
            for (int pi = 0; pi < certificationResult.size(); pi++) {
                final Result  result = (Result) certificationResult.get(pi);
                printResult(db2, svf, result, "2", "");
            }
            //昨年度認定試験結果
            svf.VrsOut("LAST_YEAR2", lastNendo); // タイトル
            final List lastYearCertificationResult = Result.load(db2, _param, lastYear, "2", false);
            for (int pi = 0; pi < lastYearCertificationResult.size(); pi++) {
                final Result  result = (Result) lastYearCertificationResult.get(pi);
                printResult(db2, svf, result, "2", "LAST_");
            }


            //総合成績
            final List overallGrade = Result.load(db2, _param, year, _param._testdiv, true);
            for (int pi = 0; pi < overallGrade.size(); pi++) {
                final Result  result = (Result) overallGrade.get(pi);
                printResult(db2, svf, result, "3", "");
            }

            //昨年度総合成績
            svf.VrsOut("LAST_YEAR3", lastNendo); // タイトル
            final List lastYearOverallGrade = Result.load(db2, _param, lastYear, _param._testdiv, true);
            for (int pi = 0; pi < lastYearOverallGrade.size(); pi++) {
                final Result  result = (Result) lastYearOverallGrade.get(pi);
                printResult(db2, svf, result, "3", "LAST_");
            }
        }

        //プレテスト平均点
        final List preTestList = PreTest.load(db2, _param);
        for (int pi = 0; pi < preTestList.size(); pi++) {
            final PreTest  preTest = (PreTest) preTestList.get(pi);
            //第1回
            svf.VrsOutn("PRE_RESULT1", 1, confValue(preTest._pre1_avg_kokugo)); // 国語
            svf.VrsOutn("PRE_RESULT2", 1, confValue(preTest._pre1_avg_sansuu)); // 算数
            svf.VrsOutn("PRE_RESULT3", 1, confValue(preTest._pre1_avg_eigo)); // 英語
            svf.VrsOutn("PRE_RESULT4", 1, confValue(preTest._pre1_avg_2ka)); // 200点換算
            //第2回
            svf.VrsOutn("PRE_RESULT1", 2, confValue(preTest._pre2_avg_kokugo)); // 国語
            svf.VrsOutn("PRE_RESULT2", 2, confValue(preTest._pre2_avg_sansuu)); // 算数
            svf.VrsOutn("PRE_RESULT3", 2, confValue(preTest._pre2_avg_eigo)); // 英語
            svf.VrsOutn("PRE_RESULT4", 2, confValue(preTest._pre2_avg_2ka)); // 200点換算
        }

        //合格最低点
        final List passInfoList = PassInfo.load(db2, _param);
        for (int pi = 0; pi < passInfoList.size(); pi++) {
            final PassInfo passInfo = (PassInfo) passInfoList.get(pi);
            if("".equals(passInfo._entexamyear)) continue;
            final int idx = Integer.parseInt(year) - Integer.parseInt(passInfo._entexamyear);
            final String year_name = KNJ_EditDate.gengou(db2, Integer.parseInt(passInfo._entexamyear)) + "年度";
            svf.VrsOutn("LAST_YEAR_NAME", idx, year_name); // 過年度名称
            svf.VrsOutn("LAST_YEAR_MIN", idx, passInfo._year_min); // 過年度最低点
            svf.VrsOutn("LAST_YEAR_AVE", idx, confValue(passInfo._year_avg)); // 過年度平均点
        }

        svf.VrsOut("EXAM_TYPE2", _param._testdivName); // 入試結果タイトル

        svf.VrEndPage();
        _hasData = true;
    }


    public void printResult(final DB2UDB db2, final Vrw32alp svf, final Result result, final String ren, final String last) {
        //資料① 各表(プレテスト表・合格最低表は除く)
        final String field1 = last + "RESULT"+ren+"_1";
        final String field2 = last + "RESULT"+ren+"_2";
        final String field3 = last + "RESULT"+ren+"_3";
        final String field4 = last + "RESULT"+ren+"_4";

        //受験者数
        svf.VrsOutn(field1, 1, result._cnt_kokugo); // 国語
        svf.VrsOutn(field2, 1, result._cnt_sansuu); // 算数
        svf.VrsOutn(field3, 1, result._cnt_eigo); // 英語
        svf.VrsOutn(field4, 1, result._cnt_2ka); // 200点換算
        //平均点
        svf.VrsOutn(field1, 2, confValue(result._avg_kokugo)); // 国語
        svf.VrsOutn(field2, 2, confValue(result._avg_sansuu)); // 算数
        svf.VrsOutn(field3, 2, confValue(result._avg_eigo)); // 英語
        svf.VrsOutn(field4, 2, confValue(result._avg_2ka)); // 200点換算
        //最高点
        svf.VrsOutn(field1, 3, result._max_kokugo); // 国語
        svf.VrsOutn(field2, 3, result._max_sansuu); // 算数
        svf.VrsOutn(field3, 3, result._max_eigo); // 英語
        svf.VrsOutn(field4, 3, result._max_2ka); // 200点換算
        //最低点
        svf.VrsOutn(field1, 4, result._min_kokugo); // 国語
        svf.VrsOutn(field2, 4, result._min_sansuu); // 算数
        svf.VrsOutn(field3, 4, result._min_eigo); // 英語
        svf.VrsOutn(field4, 4, result._min_2ka); // 200点換算
    }


    public void printResultInit(final DB2UDB db2, final Vrw32alp svf) {
        //資料① 0埋め
        for(int i=1; i<=2; i++) {
            final String last = i == 2 ? "LAST_":"";
            for(int j=1; j<=4; j++) {
                for(int x=1; x<=3; x++) {
                    if(!"1".equals(_param._testdiv) && x!=1) continue;
                    final String field1 = last + "RESULT"+x+"_1";
                    final String field2 = last + "RESULT"+x+"_2";
                    final String field3 = last + "RESULT"+x+"_3";
                    final String field4 = last + "RESULT"+x+"_4";
                    //各表
                    svf.VrsOutn(field1, j, "0"); // 国語
                    svf.VrsOutn(field2, j, "0"); // 算数
                    svf.VrsOutn(field3, j, "0"); // 英語
                    svf.VrsOutn(field4, j, "0"); // 200点換算
                }
            }
        }
        for(int i=1; i<=2; i++) {
            //プレテスト
            svf.VrsOutn("PRE_RESULT1", i, "0"); // 国語
            svf.VrsOutn("PRE_RESULT2", i, "0"); // 算数
            svf.VrsOutn("PRE_RESULT3", i, "0"); // 英語
            svf.VrsOutn("PRE_RESULT4", i, "0"); // 200点換算
        }
        for(int i=1; i<=4; i++) {
            //合格最低表
            svf.VrsOutn("LAST_YEAR_NAME", i, ""); // 過年度名称
            svf.VrsOutn("LAST_YEAR_MIN", i, "0"); // 過年度最低点
            svf.VrsOutn("LAST_YEAR_AVE", i, "0"); // 過年度平均点
        }
    }

    public String confValue(final String value) {
        String rtnVal = "";
        if (value != null && !"".equals(value)) {
            //SQLで小数1位で四捨五入しているので、小数1位より下の余計な文字列を切り取る。
            int valueLen = KNJ_EditEdit.getMS932ByteLength(value);
            String valueStr = value;
            int dotidx = value.indexOf('.');
            if (dotidx >= 0 && valueLen - dotidx + 1 > 1) {
                valueStr = valueStr.substring(0, dotidx+2);
            } else {
                //ドットが無い場合
                valueStr = "0.0";
            }
            rtnVal = valueStr;
        }
        return rtnVal;
    }

    public void printMainList(final DB2UDB db2, final Vrw32alp svf) {
        //資料②～⑥
        if("1".equals(_param._testdiv)) printList(db2, svf, PRINT_FORM_2);
        printList(db2, svf, PRINT_FORM_3);
        if("1".equals(_param._testdiv)) printList(db2, svf, PRINT_FORM_4);
        printList(db2, svf, PRINT_FORM_5);
        printList(db2, svf, PRINT_FORM_6);
    }

    public void printList(final DB2UDB db2, final Vrw32alp svf, final String form) {

        String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度";
        final String title1 = form.equals(PRINT_FORM_6) ? "奨学区分別一覧(" + nendo + "中学入試" : "志願者一覧(" + nendo + "中学入試";
        final String title2 = form.equals(PRINT_FORM_2) ? "内部)" : form.equals(PRINT_FORM_3) ? _param._testdivName + ")" : "内部・" + _param._testdivName + ")";
        final String title3 = form.equals(PRINT_FORM_4) ? "(3教科成績順)" : (form.equals(PRINT_FORM_5) || form.equals(PRINT_FORM_6)) ? "(2教科成績順)" : "(受験番号順)";

        svf.VrSetForm(form, 1);

        String befShougaku = "";
        int line = 1;
        final List outputList = OutPutList.load(db2, _param, form);
        for (int j = 0; j < outputList.size(); j++) {
            final OutPutList output = (OutPutList) outputList.get(j);

            if(form.equals(PRINT_FORM_6)) {
                if(!output._shougaku.equals(befShougaku) && !"".equals(befShougaku)) {
                    //改ページ
                    svf.VrEndPage();
                    svf.VrSetForm(form, 1);
                    line = 1;
                }
            }

            if(line > MAX_LINE) {
                //改ページ
                svf.VrEndPage();
                svf.VrSetForm(form, 1);
                line = 1;
            }

            //タイトル
            if(line == 1) {
                svf.VrsOut("TITLE", title1 + title2 + title3); // タイトル
                if(form.equals(PRINT_FORM_6)) svf.VrsOut("SCHOLARSHIP_NAME", output._shougaku_name); // 奨学区分名称
            }

            //メイン
            svf.VrsOutn("COURSE_NAME", line, output._examcourse_name); // コース
            svf.VrsOutn("DIV", line, output._senpei); // 専併
            svf.VrsOutn("EXAM_DIV", line, output._exam_type_name); // 受験型
            svf.VrsOutn("EXAN_NO", line, output._receptno); // 受験番号
            if(form.equals(PRINT_FORM_6)) {
                svf.VrsOutn("NAME", line, output._name); // 氏名
                svf.VrsOutn("FINSCHOOL_NAME1", line, output._fs_name); // 出身校
            } else {
                final String nameField = KNJ_EditEdit.getMS932ByteLength(output._name) > 20 ? "2_1" : "1";
                svf.VrsOutn("NAME" + nameField, line, output._name); // 氏名
                final String fsNameField = KNJ_EditEdit.getMS932ByteLength(output._fs_name) > 10 ? "2_1" : "1";
                svf.VrsOutn("FINSCHOOL_NAME" + fsNameField, line, output._fs_name); // 出身校
            }

            //入試成績
            svf.VrsOutn("SCORE1", line, output._kokugo); // 入試成績 国語
            svf.VrsOutn("SCORE2", line, output._sansuu); // 入試成績 算数
            svf.VrsOutn("SCORE3", line, output._eigo); // 入試成績 英語
            svf.VrsOutn("SCORE4", line, output._minasi_tokuten); // 入試成績 見なし
            svf.VrsOutn("TOTAL1", line, output._total2); // 入試成績 2教科
            svf.VrsOutn("TOTAL2", line, output._total4); // 入試成績 3教科200点換算 (資料⑥は見なし含めない)
            final String total3 = (form.equals(PRINT_FORM_5)) ? output._total1 : output._total3;
            svf.VrsOutn("TOTAL3", line, total3); // 入試成績 合計 (資料⑥は見なし含めない)

            //プレテスト①
            svf.VrsOutn("PRE_SCORE1_1", line, output._pre_kokugo38); // プレテスト① 国語
            svf.VrsOutn("PRE_SCORE1_2", line, output._pre_sansuu38); // プレテスト① 算数
            svf.VrsOutn("PRE_SCORE1_3", line, output._pre_eigo38); // プレテスト① 英語
            svf.VrsOutn("PRE_TOTAL1_1", line, output._pre_2ka38); // プレテスト① 2教科
            svf.VrsOutn("PRE_TOTAL1_2", line, output._pre_3ka38); // プレテスト① 3教科200点換算
            svf.VrsOutn("PRE_TOTAL1_3", line, output._pre_total38); // プレテスト① 合計

            //プレテスト②
            svf.VrsOutn("PRE_SCORE2_1", line, output._pre_kokugo39); // プレテスト② 国語
            svf.VrsOutn("PRE_SCORE2_2", line, output._pre_sansuu39); // プレテスト② 算数
            svf.VrsOutn("PRE_SCORE2_3", line, output._pre_eigo39); // プレテスト② 英語
            svf.VrsOutn("PRE_TOTAL2_1", line, output._pre_2ka39); // プレテスト② 2教科
            svf.VrsOutn("PRE_TOTAL2_2", line, output._pre_3ka39); // プレテスト② 3教科200点換算
            svf.VrsOutn("PRE_TOTAL2_3", line, output._pre_total39); // プレテスト② 合計

            svf.VrsOutn("PASS", line, ""); // 合格
            final String bikoField = KNJ_EditEdit.getMS932ByteLength(output._biko) > 40 ? "2_1" : "1";
            svf.VrsOutn("CONDITION" + bikoField, line, output._biko); // 判定
            svf.VrsOutn("FAILURE", line, ""); // 不合格

            befShougaku = output._shougaku;
            line++;
        }
        svf.VrEndPage();
        _hasData = true;
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

    //入試結果,認定試験結果,総合成績
    private static class Result {
        final String _cnt_kokugo;
        final String _cnt_sansuu;
        final String _cnt_eigo;
        final String _cnt_2ka;
        final String _avg_kokugo;
        final String _avg_sansuu;
        final String _avg_eigo;
        final String _avg_2ka;
        final String _max_kokugo;
        final String _max_sansuu;
        final String _max_eigo;
        final String _max_2ka;
        final String _min_kokugo;
        final String _min_sansuu;
        final String _min_eigo;
        final String _min_2ka;

        private Result(
                final String cnt_kokugo,
                final String cnt_sansuu,
                final String cnt_eigo,
                final String cnt_2ka,
                final String avg_kokugo,
                final String avg_sansuu,
                final String avg_eigo,
                final String avg_2ka,
                final String max_kokugo,
                final String max_sansuu,
                final String max_eigo,
                final String max_2ka,
                final String min_kokugo,
                final String min_sansuu,
                final String min_eigo,
                final String min_2ka
        ) {
            _cnt_kokugo = cnt_kokugo;
            _cnt_sansuu = cnt_sansuu;
            _cnt_eigo = cnt_eigo;
            _cnt_2ka = cnt_2ka;
            _avg_kokugo = avg_kokugo;
            _avg_sansuu = avg_sansuu;
            _avg_eigo = avg_eigo;
            _avg_2ka = avg_2ka;
            _max_kokugo = max_kokugo;
            _max_sansuu = max_sansuu;
            _max_eigo = max_eigo;
            _max_2ka = max_2ka;
            _min_kokugo = min_kokugo;
            _min_sansuu = min_sansuu;
            _min_eigo = min_eigo;
            _min_2ka = min_2ka;
        }

        public static List load(final DB2UDB db2, final Param param, final String year, final String testDiv, final boolean gassan) {
            final List list = new ArrayList();
            final String sql = getResultSql(param, year, testDiv, gassan);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cnt_kokugo = StringUtils.defaultString(rs.getString("CNT_KOKUGO"), "0");
                    final String cnt_sansuu = StringUtils.defaultString(rs.getString("CNT_SANSUU"), "0");
                    final String cnt_eigo = StringUtils.defaultString(rs.getString("CNT_EIGO"), "0");
                    final String cnt_2ka = StringUtils.defaultString(rs.getString("CNT_2KA"), "0");
                    final String avg_kokugo = StringUtils.defaultString(rs.getString("AVG_KOKUGO"), "0.0");
                    final String avg_sansuu = StringUtils.defaultString(rs.getString("AVG_SANSUU"), "0.0");
                    final String avg_eigo = StringUtils.defaultString(rs.getString("AVG_EIGO"), "0.0");
                    final String avg_2ka = StringUtils.defaultString(rs.getString("AVG_2KA"), "0.0");
                    final String max_kokugo = StringUtils.defaultString(rs.getString("MAX_KOKUGO"), "0");
                    final String max_sansuu = StringUtils.defaultString(rs.getString("MAX_SANSUU"), "0");
                    final String max_eigo = StringUtils.defaultString(rs.getString("MAX_EIGO"), "0");
                    final String max_2ka = StringUtils.defaultString(rs.getString("MAX_2KA"), "0");
                    final String min_kokugo = StringUtils.defaultString(rs.getString("MIN_KOKUGO"), "0");
                    final String min_sansuu = StringUtils.defaultString(rs.getString("MIN_SANSUU"), "0");
                    final String min_eigo = StringUtils.defaultString(rs.getString("MIN_EIGO"), "0");
                    final String min_2ka = StringUtils.defaultString(rs.getString("MIN_2KA"), "0");
                    Result result = new Result(cnt_kokugo, cnt_sansuu, cnt_eigo,cnt_2ka,
                                                avg_kokugo,avg_sansuu,avg_eigo,avg_2ka,
                                                max_kokugo,max_sansuu,max_eigo,max_2ka,
                                                min_kokugo,min_sansuu,min_eigo,min_2ka);
                    list.add(result);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return list;
        }

        public static String getResultSql(final Param param, final String year, final String testDiv, final boolean gassan) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.ENTEXAMYEAR, ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     COUNT(SCORE1.RECEPTNO) AS CNT_KOKUGO, ");
            stb.append("     COUNT(SCORE3.RECEPTNO) AS CNT_SANSUU, ");
            stb.append("     COUNT(SCORE5.RECEPTNO) AS CNT_EIGO, ");
            stb.append("     COUNT(REC.RECEPTNO) AS CNT_2KA, ");
            stb.append("     ROUND(AVG(SCORE1.SCORE+0.0),1) AS AVG_KOKUGO, ");
            stb.append("     ROUND(AVG(SCORE3.SCORE+0.0),1) AS AVG_SANSUU, ");
            stb.append("     ROUND(AVG(SCORE5.SCORE+0.0),1) AS AVG_EIGO, ");
            stb.append("     ROUND(AVG(REC.TOTAL2+0.0),1) AS AVG_2KA, ");
            stb.append("     MAX(SCORE1.SCORE) AS MAX_KOKUGO, ");
            stb.append("     MAX(SCORE3.SCORE) AS MAX_SANSUU, ");
            stb.append("     MAX(SCORE5.SCORE) AS MAX_EIGO, ");
            stb.append("     MAX(REC.TOTAL2)   AS MAX_2KA, ");
            stb.append("     MIN(SCORE1.SCORE) AS MIN_KOKUGO, ");
            stb.append("     MIN(SCORE3.SCORE) AS MIN_SANSUU, ");
            stb.append("     MIN(SCORE5.SCORE) AS MIN_EIGO, ");
            stb.append("     MIN(REC.TOTAL2)   AS MIN_2KA ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(" INNER JOIN ENTEXAM_RECEPT_DAT REC ");
            stb.append("         ON REC.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
            stb.append("        AND REC.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("        AND REC.EXAMNO       = T1.EXAMNO ");
            if(gassan) {
                //内部・外部全体
                stb.append("     AND REC.TESTDIV IN ('" + testDiv + "', '2') ");
            } else {
                //指定入試区分
                stb.append("     AND REC.TESTDIV = '" + testDiv + "' ");
            }
            stb.append(" LEFT JOIN ENTEXAM_SCORE_DAT SCORE1 ");
            stb.append("        ON SCORE1.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
            stb.append("       AND SCORE1.APPLICANTDIV   = REC.APPLICANTDIV ");
            stb.append("       AND SCORE1.TESTDIV        = REC.TESTDIV ");
            stb.append("       AND SCORE1.EXAM_TYPE      = REC.EXAM_TYPE ");
            stb.append("       AND SCORE1.RECEPTNO       = REC.RECEPTNO ");
            stb.append("       AND SCORE1.TESTSUBCLASSCD = '1' "); //1:国語
            stb.append(" LEFT JOIN ENTEXAM_SCORE_DAT SCORE3 ");
            stb.append("        ON SCORE3.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
            stb.append("       AND SCORE3.APPLICANTDIV   = REC.APPLICANTDIV ");
            stb.append("       AND SCORE3.TESTDIV        = REC.TESTDIV ");
            stb.append("       AND SCORE3.EXAM_TYPE      = REC.EXAM_TYPE ");
            stb.append("       AND SCORE3.RECEPTNO       = REC.RECEPTNO ");
            stb.append("       AND SCORE3.TESTSUBCLASSCD = '3' "); //3:算数
            stb.append(" LEFT JOIN ENTEXAM_SCORE_DAT SCORE5 ");
            stb.append("        ON SCORE5.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
            stb.append("       AND SCORE5.APPLICANTDIV   = REC.APPLICANTDIV ");
            stb.append("       AND SCORE5.TESTDIV        = REC.TESTDIV ");
            stb.append("       AND SCORE5.EXAM_TYPE      = REC.EXAM_TYPE ");
            stb.append("       AND SCORE5.RECEPTNO       = REC.RECEPTNO ");
            stb.append("       AND SCORE5.TESTSUBCLASSCD = '5' "); //5:英語
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + year + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND VALUE(REC.JUDGEDIV, '') <> '4' ");
            stb.append(" GROUP BY  ");
            stb.append("     T1.ENTEXAMYEAR, ");
            stb.append("     T1.APPLICANTDIV ");
            return stb.toString();
        }
    }

    //プレテスト
    private static class PreTest {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _pre1_avg_kokugo;
        final String _pre1_avg_sansuu;
        final String _pre1_avg_eigo;
        final String _pre1_avg_2ka;
        final String _pre2_avg_kokugo;
        final String _pre2_avg_sansuu;
        final String _pre2_avg_eigo;
        final String _pre2_avg_2ka;

        PreTest(
                final String entexamyear,
                final String applicantdiv,
                final String testdiv,
                final String pre1_avg_kokugo,
                final String pre1_avg_sansuu,
                final String pre1_avg_eigo,
                final String pre1_avg_2ka,
                final String pre2_avg_kokugo,
                final String pre2_avg_sansuu,
                final String pre2_avg_eigo,
                final String pre2_avg_2ka
        ) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _pre1_avg_kokugo = pre1_avg_kokugo;
            _pre1_avg_sansuu = pre1_avg_sansuu;
            _pre1_avg_eigo = pre1_avg_eigo;
            _pre1_avg_2ka = pre1_avg_2ka;
            _pre2_avg_kokugo = pre2_avg_kokugo;
            _pre2_avg_sansuu = pre2_avg_sansuu;
            _pre2_avg_eigo = pre2_avg_eigo;
            _pre2_avg_2ka = pre2_avg_2ka;
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = preTestSql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String entexamyear = StringUtils.defaultString(rs.getString("ENTEXAMYEAR"));
                    final String applicantdiv = StringUtils.defaultString(rs.getString("APPLICANTDIV"));
                    final String testdiv = StringUtils.defaultString(rs.getString("TESTDIV"));
                    final String pre1_avg_kokugo = StringUtils.defaultString(rs.getString("PRE1_AVG_KOKUGO"), "0.0");
                    final String pre1_avg_sansuu = StringUtils.defaultString(rs.getString("PRE1_AVG_SANSUU"), "0.0");
                    final String pre1_avg_eigo = StringUtils.defaultString(rs.getString("PRE1_AVG_EIGO"), "0.0");
                    final String pre1_avg_2ka = StringUtils.defaultString(rs.getString("PRE1_AVG_2KA"), "0.0");
                    final String pre2_avg_kokugo = StringUtils.defaultString(rs.getString("PRE2_AVG_KOKUGO"), "0.0");
                    final String pre2_avg_sansuu = StringUtils.defaultString(rs.getString("PRE2_AVG_SANSUU"), "0.0");
                    final String pre2_avg_eigo = StringUtils.defaultString(rs.getString("PRE2_AVG_EIGO"), "0.0");
                    final String pre2_avg_2ka = StringUtils.defaultString(rs.getString("PRE2_AVG_2KA"), "0.0");

                    final PreTest preTest = new PreTest(entexamyear, applicantdiv, testdiv,
                                                        pre1_avg_kokugo, pre1_avg_sansuu, pre1_avg_eigo, pre1_avg_2ka,
                                                        pre2_avg_kokugo, pre2_avg_sansuu, pre2_avg_eigo, pre2_avg_2ka);
                    list.add(preTest);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String preTestSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.ENTEXAMYEAR, ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     T1.TESTDIV, ");
            //プレテスト①
            stb.append("     ROUND(AVG(BASE_D_38.REMARK1+0.0),1) AS PRE1_AVG_KOKUGO, ");
            stb.append("     ROUND(AVG(BASE_D_38.REMARK2+0.0),1) AS PRE1_AVG_SANSUU, ");
            stb.append("     ROUND(AVG(BASE_D_38.REMARK3+0.0),1) AS PRE1_AVG_EIGO, ");
            stb.append("     ROUND(AVG(BASE_D_38.REMARK4+0.0),1) AS PRE1_AVG_2KA, ");
            //プレテスト②
            stb.append("     ROUND(AVG(BASE_D_39.REMARK1+0.0),1) AS PRE2_AVG_KOKUGO, ");
            stb.append("     ROUND(AVG(BASE_D_39.REMARK2+0.0),1) AS PRE2_AVG_SANSUU, ");
            stb.append("     ROUND(AVG(BASE_D_39.REMARK3+0.0),1) AS PRE2_AVG_EIGO, ");
            stb.append("     ROUND(AVG(BASE_D_39.REMARK4+0.0),1) AS PRE2_AVG_2KA ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(" INNER JOIN ENTEXAM_RECEPT_DAT REC ");
            stb.append("         ON REC.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
            stb.append("        AND REC.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("        AND REC.EXAMNO       = T1.EXAMNO ");
            if("1".equals(param._testdiv)) {
                stb.append("        AND REC.TESTDIV IN ('" + param._testdiv + "', '2') ");
            } else {
                stb.append("        AND REC.TESTDIV = '" + param._testdiv + "' ");
            }
            //プレテスト①
            stb.append(" INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_38 ");
            stb.append("        ON BASE_D_38.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("       AND BASE_D_38.APPLICANTDIV= T1.APPLICANTDIV ");
            stb.append("       AND BASE_D_38.EXAMNO      = T1.EXAMNO ");
            stb.append("       AND BASE_D_38.SEQ         = '038' ");
            //プレテスト②
            stb.append(" INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_39 ");
            stb.append("        ON BASE_D_39.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("       AND BASE_D_39.APPLICANTDIV= T1.APPLICANTDIV ");
            stb.append("       AND BASE_D_39.EXAMNO      = T1.EXAMNO ");
            stb.append("       AND BASE_D_39.SEQ         = '039' ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR      = '" + param._entexamyear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND VALUE(REC.JUDGEDIV, '') <> '4' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.ENTEXAMYEAR, ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     T1.TESTDIV ");
            return stb.toString();
        }
    }

    //合格情報
    private static class PassInfo {
        final String _entexamyear;
        final String _year_min;
        final String _year_avg;

        PassInfo(
                final String entexamyear,
                final String year_min,
                final String year_avg
        ) {
            _entexamyear = entexamyear;
            _year_min = year_min;
            _year_avg = year_avg;
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = passInfoSql(param);
                log.debug("sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String entexamyear = StringUtils.defaultString(rs.getString("ENTEXAMYEAR"));
                    final String year_min = StringUtils.defaultString(rs.getString("YEAR_MIN"), "0");
                    final String year_avg = StringUtils.defaultString(rs.getString("YEAR_AVG"), "0.0");
                    final PassInfo passInfo = new PassInfo(entexamyear, year_min, year_avg);
                    list.add(passInfo);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String passInfoSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REC.ENTEXAMYEAR, ");
            stb.append("     MIN(REC.TOTAL4) AS YEAR_MIN, ");
            stb.append("     ROUND(AVG(REC.TOTAL4+0.0),1) AS YEAR_AVG ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(" INNER JOIN ENTEXAM_RECEPT_DAT REC ");
            stb.append("         ON REC.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
            stb.append("        AND REC.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("        AND REC.EXAMNO       = T1.EXAMNO ");
            if("1".equals(param._testdiv)) {
                stb.append("        AND REC.TESTDIV IN ('" + param._testdiv + "', '2') ");
            } else {
                stb.append("        AND REC.TESTDIV = '" + param._testdiv + "' ");
            }
            //合格者が対象
            stb.append(" INNER JOIN V_NAME_MST L013 ");
            stb.append("         ON L013.YEAR       = REC.ENTEXAMYEAR ");
            stb.append("        AND L013.NAMECD1    = 'L013' ");
            stb.append("        AND L013.NAMECD2    = REC.JUDGEDIV ");
            stb.append("        AND L013.NAMESPARE1 = '1' ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR < '" + param._entexamyear + "' "); //過年度
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append(" GROUP BY REC.ENTEXAMYEAR ");
            stb.append(" ORDER BY REC.ENTEXAMYEAR DESC ");
            stb.append(" FETCH FIRST 4 ROWS ONLY "); //過去4年分を取得
            return stb.toString();
        }
    }


    //志願者一覧、成績一覧、奨学区分別一覧
    private static class OutPutList {
        final String _applicantdiv;
        final String _examno;
        final String _testdiv;
        final String _testdiv_name;
        final String _exam_type;
        final String _exam_type_name;
        final String _receptno;
        final String _shdiv;
        final String _senpei;
        final String _examcourse;
        final String _examcourse_name;
        final String _name;
        final String _fs_cd;
        final String _fs_name;
        final String _kokugo;
        final String _sansuu;
        final String _eigo;
        final String _total1;
        final String _total2;
        final String _total3;
        final String _total4;
        final String _pre_kokugo38;
        final String _pre_sansuu38;
        final String _pre_eigo38;
        final String _pre_2ka38;
        final String _pre_3ka38;
        final String _pre_total38;
        final String _pre_kokugo39;
        final String _pre_sansuu39;
        final String _pre_eigo39;
        final String _pre_2ka39;
        final String _pre_3ka39;
        final String _pre_total39;
        final String _eiken_shutoku_kyuu;
        final String _minasi_tokuten;
        final String _biko;
        final String _shougaku;
        final String _shougaku_name;

        OutPutList(
                final String applicantdiv,
                final String examno,
                final String testdiv,
                final String testdiv_name,
                final String exam_type,
                final String exam_type_name,
                final String receptno,
                final String shdiv,
                final String senpei,
                final String examcourse,
                final String examcourse_name,
                final String name,
                final String fs_cd,
                final String fs_name,
                final String kokugo,
                final String sansuu,
                final String eigo,
                final String total1,
                final String total2,
                final String total3,
                final String total4,
                final String pre_kokugo38,
                final String pre_sansuu38,
                final String pre_eigo38,
                final String pre_2ka38,
                final String pre_3ka38,
                final String pre_total38,
                final String pre_kokugo39,
                final String pre_sansuu39,
                final String pre_eigo39,
                final String pre_2ka39,
                final String pre_3ka39,
                final String pre_total39,
                final String eiken_shutoku_kyuu,
                final String minasi_tokuten,
                final String biko,
                final String shougaku,
                final String shougaku_name
        ) {
            _applicantdiv = applicantdiv;
            _examno = examno;
            _testdiv = testdiv;
            _testdiv_name = testdiv_name;
            _exam_type = exam_type;
            _exam_type_name = exam_type_name;
            _receptno = receptno;
            _shdiv = shdiv;
            _senpei = senpei;
            _examcourse = examcourse;
            _examcourse_name = examcourse_name;
            _name = name;
            _fs_cd = fs_cd;
            _fs_name = fs_name;
            _kokugo = kokugo;
            _sansuu = sansuu;
            _eigo = eigo;
            _total1 = total1;
            _total2 = total2;
            _total3 = total3;
            _total4 = total4;
            _pre_kokugo38 = pre_kokugo38;
            _pre_sansuu38 = pre_sansuu38;
            _pre_eigo38 = pre_eigo38;
            _pre_2ka38 = pre_2ka38;
            _pre_3ka38 = pre_3ka38;
            _pre_total38 = pre_total38;
            _pre_kokugo39 = pre_kokugo39;
            _pre_sansuu39 = pre_sansuu39;
            _pre_eigo39 = pre_eigo39;
            _pre_2ka39 = pre_2ka39;
            _pre_3ka39 = pre_3ka39;
            _pre_total39 = pre_total39;
            _eiken_shutoku_kyuu = eiken_shutoku_kyuu;
            _minasi_tokuten = minasi_tokuten;
            _biko = biko;
            _shougaku = shougaku;
            _shougaku_name = shougaku_name;
        }

        public static List load(final DB2UDB db2, final Param param, final String form) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = outputListSql(param, form);
                log.debug("sql " + form + " = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String applicantdiv = StringUtils.defaultString(rs.getString("APPLICANTDIV"));
                    final String examno = StringUtils.defaultString(rs.getString("EXAMNO"));
                    final String testdiv = StringUtils.defaultString(rs.getString("TESTDIV"));
                    final String testdiv_name = StringUtils.defaultString(rs.getString("TESTDIV_NAME"));
                    final String exam_type = StringUtils.defaultString(rs.getString("EXAM_TYPE"));
                    final String exam_type_name = StringUtils.defaultString(rs.getString("EXAM_TYPE_NAME"));
                    final String receptno = StringUtils.defaultString(rs.getString("RECEPTNO"));
                    final String shdiv = StringUtils.defaultString(rs.getString("SHDIV"));
                    final String senpei = StringUtils.defaultString(rs.getString("SENPEI"));
                    final String examcourse = StringUtils.defaultString(rs.getString("EXAMCOURSE"));
                    final String examcourse_name = StringUtils.defaultString(rs.getString("EXAMCOURSE_NAME"));
                    final String name = StringUtils.defaultString(rs.getString("NAME"));
                    final String fs_cd = StringUtils.defaultString(rs.getString("FS_CD"));
                    final String fs_name = StringUtils.defaultString(rs.getString("FS_NAME"));
                    final String kokugo = StringUtils.defaultString(rs.getString("KOKUGO"));
                    final String sansuu = StringUtils.defaultString(rs.getString("SANSUU"));
                    final String eigo = StringUtils.defaultString(rs.getString("EIGO"));
                    final String total1 = StringUtils.defaultString(rs.getString("TOTAL1"));
                    final String total2 = StringUtils.defaultString(rs.getString("TOTAL2"));
                    final String total3 = StringUtils.defaultString(rs.getString("TOTAL3"));
                    final String total4 = StringUtils.defaultString(rs.getString("TOTAL4"));
                    final String pre_kokugo38 = StringUtils.defaultString(rs.getString("PRE_KOKUGO38"));
                    final String pre_sansuu38 = StringUtils.defaultString(rs.getString("PRE_SANSUU38"));
                    final String pre_eigo38 = StringUtils.defaultString(rs.getString("PRE_EIGO38"));
                    final String pre_2ka38 = StringUtils.defaultString(rs.getString("PRE_2KA38"));
                    final String pre_3ka38 = StringUtils.defaultString(rs.getString("PRE_3KA38"));
                    final String pre_total38 = StringUtils.defaultString(rs.getString("PRE_TOTAL38"));
                    final String pre_kokugo39 = StringUtils.defaultString(rs.getString("PRE_KOKUGO39"));
                    final String pre_sansuu39 = StringUtils.defaultString(rs.getString("PRE_SANSUU39"));
                    final String pre_eigo39 = StringUtils.defaultString(rs.getString("PRE_EIGO39"));
                    final String pre_2ka39 = StringUtils.defaultString(rs.getString("PRE_2KA39"));
                    final String pre_3ka39 = StringUtils.defaultString(rs.getString("PRE_3KA39"));
                    final String pre_total39 = StringUtils.defaultString(rs.getString("PRE_TOTAL39"));
                    final String eiken_shutoku_kyuu = StringUtils.defaultString(rs.getString("EIKEN_SHUTOKU_KYUU"));
                    final String minasi_tokuten = StringUtils.defaultString(rs.getString("MINASI_TOKUTEN"));
                    final String biko = StringUtils.defaultString(rs.getString("BIKO"));
                    final String shougaku = StringUtils.defaultString(rs.getString("SHOUGAKU"));
                    final String shougaku_name = StringUtils.defaultString(rs.getString("SHOUGAKU_NAME"));

                    final OutPutList output = new OutPutList(applicantdiv, examno, testdiv, testdiv_name, exam_type, exam_type_name, receptno, shdiv, senpei, examcourse, examcourse_name, name, fs_cd, fs_name,
                                                                kokugo, sansuu, eigo, total1, total2, total3, total4,
                                                                pre_kokugo38, pre_sansuu38, pre_eigo38, pre_2ka38, pre_3ka38, pre_total38,
                                                                pre_kokugo39, pre_sansuu39, pre_eigo39, pre_2ka39, pre_3ka39, pre_total39,
                                                                eiken_shutoku_kyuu, minasi_tokuten, biko, shougaku, shougaku_name);
                    list.add(output);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String outputListSql(final Param param, final String form) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     REC.TESTDIV, ");
            stb.append("     L024.NAME1 AS TESTDIV_NAME, ");
            stb.append("     REC.EXAM_TYPE, ");
            stb.append("     L005.NAME1 AS EXAM_TYPE_NAME, ");
            stb.append("     REC.RECEPTNO, ");
            stb.append("     T1.SHDIV, ");
            stb.append("     L006.NAME1 AS SENPEI, ");
            stb.append("     BASE_D_001.REMARK8 || '-' || BASE_D_001.REMARK9 || '-' || BASE_D_001.REMARK10 AS EXAMCOURSE, ");
            stb.append("     COURSE.EXAMCOURSE_ABBV AS EXAMCOURSE_NAME, ");
            stb.append("     T1.NAME, ");
            stb.append("     T1.FS_CD, ");
            stb.append("     FIN.FINSCHOOL_NAME AS FS_NAME, ");
            //入試成績
            stb.append("     SCORE1.SCORE AS KOKUGO, ");
            stb.append("     SCORE3.SCORE AS SANSUU, ");
            stb.append("     SCORE5.SCORE AS EIGO, ");
            if(form.equals(PRINT_FORM_6)) {
                stb.append("     REC.TOTAL1, "); //「2教科合計」と「3教科合計」の良い方
                stb.append("     REC.TOTAL2, "); // 2教科合計
                stb.append("     REC_D_012.REMARK1 AS TOTAL3, "); // 3教科合計(見なし含めない)
                stb.append("     REC_D_011.REMARK1 AS TOTAL4, "); // 3教科200点数換算(見なし含めない)
            } else {
                stb.append("     REC.TOTAL1, "); //「2教科合計」と「3教科合計」の良い方
                stb.append("     REC.TOTAL2, "); // 2教科合計
                stb.append("     REC.TOTAL3, "); // 3教科合計(見なし含む)
                stb.append("     REC.TOTAL4, "); // 3教科200点数換算(見なし含む)
            }
            //プレテスト①
            stb.append("     BASE_D_38.REMARK1 AS PRE_KOKUGO38, ");
            stb.append("     BASE_D_38.REMARK2 AS PRE_SANSUU38, ");
            stb.append("     BASE_D_38.REMARK3 AS PRE_EIGO38, ");
            stb.append("     BASE_D_38.REMARK4 AS PRE_2KA38, ");
            stb.append("     BASE_D_38.REMARK5 AS PRE_3KA38, ");
            stb.append("     BASE_D_38.REMARK6 AS PRE_TOTAL38, ");
            //プレテスト②
            stb.append("     BASE_D_39.REMARK1 AS PRE_KOKUGO39, ");
            stb.append("     BASE_D_39.REMARK2 AS PRE_SANSUU39, ");
            stb.append("     BASE_D_39.REMARK3 AS PRE_EIGO39, ");
            stb.append("     BASE_D_39.REMARK4 AS PRE_2KA39, ");
            stb.append("     BASE_D_39.REMARK5 AS PRE_3KA39, ");
            stb.append("     BASE_D_39.REMARK6 AS PRE_TOTAL39, ");
            //見なし得点、奨学区分、特別活動
            stb.append("     BASE_D_005.REMARK1 AS EIKEN_SHUTOKU_KYUU, ");
            stb.append("     L055.NAMESPARE2 AS MINASI_TOKUTEN, ");
            stb.append("     BASE_D_005.REMARK4 AS BIKO, ");
            stb.append("     BASE_D_005.REMARK2 AS SHOUGAKU, ");
            stb.append("     L025.NAME1 AS SHOUGAKU_NAME");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(" INNER JOIN ENTEXAM_RECEPT_DAT REC ");
            stb.append("         ON REC.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
            stb.append("        AND REC.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("        AND REC.EXAMNO       = T1.EXAMNO ");
            if(form.equals(PRINT_FORM_2)) {
                stb.append("        AND REC.TESTDIV = '2' ");
            } else if(form.equals(PRINT_FORM_4) || ("1".equals(param._testdiv) && (form.equals(PRINT_FORM_5) || form.equals(PRINT_FORM_6)))) {
                stb.append("        AND REC.TESTDIV IN ('" + param._testdiv + "', '2') ");
            } else {
                stb.append("        AND REC.TESTDIV = '" + param._testdiv + "' ");
            }
            stb.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_001 ");
            stb.append("        ON BASE_D_001.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("       AND BASE_D_001.APPLICANTDIV= T1.APPLICANTDIV ");
            stb.append("       AND BASE_D_001.EXAMNO      = T1.EXAMNO ");
            stb.append("       AND BASE_D_001.SEQ         = '001' ");
            //入試成績
            stb.append(" LEFT JOIN ENTEXAM_SCORE_DAT SCORE1 ");
            stb.append("        ON SCORE1.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
            stb.append("       AND SCORE1.APPLICANTDIV   = REC.APPLICANTDIV ");
            stb.append("       AND SCORE1.TESTDIV        = REC.TESTDIV ");
            stb.append("       AND SCORE1.EXAM_TYPE      = REC.EXAM_TYPE ");
            stb.append("       AND SCORE1.RECEPTNO       = REC.RECEPTNO ");
            stb.append("       AND SCORE1.TESTSUBCLASSCD = '1' "); //1:国語
            stb.append(" LEFT JOIN ENTEXAM_SCORE_DAT SCORE3 ");
            stb.append("        ON SCORE3.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
            stb.append("       AND SCORE3.APPLICANTDIV   = REC.APPLICANTDIV ");
            stb.append("       AND SCORE3.TESTDIV        = REC.TESTDIV ");
            stb.append("       AND SCORE3.EXAM_TYPE      = REC.EXAM_TYPE ");
            stb.append("       AND SCORE3.RECEPTNO       = REC.RECEPTNO ");
            stb.append("       AND SCORE3.TESTSUBCLASSCD = '3' "); //3:算数
            stb.append(" LEFT JOIN ENTEXAM_SCORE_DAT SCORE5 ");
            stb.append("        ON SCORE5.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
            stb.append("       AND SCORE5.APPLICANTDIV   = REC.APPLICANTDIV ");
            stb.append("       AND SCORE5.TESTDIV        = REC.TESTDIV ");
            stb.append("       AND SCORE5.EXAM_TYPE      = REC.EXAM_TYPE ");
            stb.append("       AND SCORE5.RECEPTNO       = REC.RECEPTNO ");
            stb.append("       AND SCORE5.TESTSUBCLASSCD = '5' "); //5:英語
            //プレテスト①
            stb.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_38 ");
            stb.append("        ON BASE_D_38.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("       AND BASE_D_38.APPLICANTDIV= T1.APPLICANTDIV ");
            stb.append("       AND BASE_D_38.EXAMNO      = T1.EXAMNO ");
            stb.append("       AND BASE_D_38.SEQ         = '038' ");
            //プレテスト②
            stb.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_39 ");
            stb.append("        ON BASE_D_39.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("       AND BASE_D_39.APPLICANTDIV= T1.APPLICANTDIV ");
            stb.append("       AND BASE_D_39.EXAMNO      = T1.EXAMNO ");
            stb.append("       AND BASE_D_39.SEQ         = '039' ");
            //見なし得点、奨学区分、特別活動
            if(form.equals(PRINT_FORM_6)) {
                //資料⑥の場合、奨学区分が条件
                stb.append(" INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_005 ");
                stb.append("         ON BASE_D_005.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
                stb.append("        AND BASE_D_005.APPLICANTDIV= T1.APPLICANTDIV ");
                stb.append("        AND BASE_D_005.EXAMNO      = T1.EXAMNO ");
                stb.append("        AND BASE_D_005.SEQ         = '005' ");
                stb.append(" INNER JOIN V_NAME_MST L025 ");
                stb.append("         ON L025.YEAR       = BASE_D_005.ENTEXAMYEAR ");
                stb.append("        AND L025.NAMECD1    = 'L025' ");
                stb.append("        AND L025.NAMECD2    = BASE_D_005.REMARK2  ");
                stb.append("        AND L025.NAMESPARE1 = T1.APPLICANTDIV ");
                //資料⑥の場合のみ参照
                stb.append(" LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT REC_D_011 ");
                stb.append("        ON REC_D_011.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
                stb.append("       AND REC_D_011.APPLICANTDIV = REC.APPLICANTDIV ");
                stb.append("       AND REC_D_011.TESTDIV      = REC.TESTDIV ");
                stb.append("       AND REC_D_011.EXAM_TYPE    = REC.EXAM_TYPE ");
                stb.append("       AND REC_D_011.RECEPTNO     = REC.RECEPTNO ");
                stb.append("       AND REC_D_011.SEQ          = '011'");
                stb.append(" LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT REC_D_012 ");
                stb.append("        ON REC_D_012.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
                stb.append("       AND REC_D_012.APPLICANTDIV = REC.APPLICANTDIV ");
                stb.append("       AND REC_D_012.TESTDIV      = REC.TESTDIV ");
                stb.append("       AND REC_D_012.EXAM_TYPE    = REC.EXAM_TYPE ");
                stb.append("       AND REC_D_012.RECEPTNO     = REC.RECEPTNO ");
                stb.append("       AND REC_D_012.SEQ          = '012'");
            } else {
                //資料⑥以外の場合、奨学区分は条件外
                stb.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_005 ");
                stb.append("        ON BASE_D_005.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
                stb.append("       AND BASE_D_005.APPLICANTDIV= T1.APPLICANTDIV ");
                stb.append("       AND BASE_D_005.EXAMNO      = T1.EXAMNO ");
                stb.append("       AND BASE_D_005.SEQ         = '005' ");
                stb.append(" LEFT JOIN V_NAME_MST L025 ");
                stb.append("         ON L025.YEAR       = BASE_D_005.ENTEXAMYEAR ");
                stb.append("        AND L025.NAMECD1    = 'L025' ");
                stb.append("        AND L025.NAMECD2    = BASE_D_005.REMARK2  ");
                stb.append("        AND L025.NAMESPARE1 = T1.APPLICANTDIV ");
            }
            //各種名称
            stb.append(" LEFT JOIN ENTEXAM_COURSE_MST COURSE ");
            stb.append("        ON COURSE.ENTEXAMYEAR  = BASE_D_001.ENTEXAMYEAR ");
            stb.append("       AND COURSE.COURSECD     = BASE_D_001.REMARK8 ");
            stb.append("       AND COURSE.MAJORCD      = BASE_D_001.REMARK9 ");
            stb.append("       AND COURSE.EXAMCOURSECD = BASE_D_001.REMARK10 ");
            stb.append(" LEFT JOIN FINSCHOOL_MST FIN ON T1.FS_CD = FIN.FINSCHOOLCD ");
            stb.append(" LEFT JOIN V_NAME_MST L006 ");
            stb.append("        ON L006.YEAR       = T1.ENTEXAMYEAR ");
            stb.append("       AND L006.NAMECD1    = 'L006' ");
            stb.append("       AND L006.NAMECD2    = T1.SHDIV ");
            stb.append(" LEFT JOIN V_NAME_MST L024 ");
            stb.append("        ON L024.YEAR       = REC.ENTEXAMYEAR ");
            stb.append("       AND L024.NAMECD1    = 'L024' ");
            stb.append("       AND L024.NAMECD2    = REC.TESTDIV ");
            stb.append(" LEFT JOIN V_NAME_MST L005 ");
            stb.append("        ON L005.YEAR       = REC.ENTEXAMYEAR ");
            stb.append("       AND L005.NAMECD1    = 'L005' ");
            stb.append("       AND L005.NAMECD2    = REC.EXAM_TYPE ");
            stb.append(" LEFT JOIN V_NAME_MST L055 ");
            stb.append("        ON L055.YEAR       = BASE_D_005.ENTEXAMYEAR ");
            stb.append("       AND L055.NAMECD1    = 'L055' ");
            stb.append("       AND L055.NAMECD2    = BASE_D_005.REMARK1 ");
            stb.append("       AND L055.NAMESPARE1 = T1.APPLICANTDIV ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            if(form.equals(PRINT_FORM_4)) {
                stb.append("     AND REC.EXAM_TYPE = '2' "); //Ⅱ型受験生のみ
            }
            stb.append("       AND VALUE(REC.JUDGEDIV, '') <> '4' ");
            stb.append(" ORDER BY ");
            if(form.equals(PRINT_FORM_6)) {
                stb.append("     BASE_D_005.REMARK2, ");
                stb.append("     VALUE(REC.TOTAL2,0) DESC, "); //2教科合計
            } else if(form.equals(PRINT_FORM_5)) {
                stb.append("     VALUE(REC.TOTAL2,0) DESC, "); //2教科合計
            } else if(form.equals(PRINT_FORM_4)) {
                stb.append("     VALUE(REC.TOTAL3,0) DESC, "); //3教科合名(見なし含める)
            } else if(form.equals(PRINT_FORM_2) || form.equals(PRINT_FORM_3)) {
                stb.append("     REC.RECEPTNO, "); // 受験番号
            }
            stb.append("     EXAMNO ");
            return stb.toString();
        }

        }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71853 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;

        final String _dateStr;
        final String _applicantdivName;
        final String _testdivName;
        final List _nameMstL007;
        final List _nameMstL024;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');

            _dateStr = getDateStr(db2, _date);
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testdivName = getNameMst(db2, "NAME1", "1".equals(_applicantdiv) ? "L024" : "L004", _testdiv);
            _nameMstL007 = getNameMstL007(db2);
            _nameMstL024 = getNameMstL024(db2);
        }

        private String getDateStr(final DB2UDB db2, final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(db2, date);
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = StringUtils.defaultString(rs.getString(field));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        public String formatDateMarkDot(final String date) {
            if (null != date) {
                final DateFormat df = new SimpleDateFormat("yyyy-mm-dd");
                try {
                    final Date d = java.sql.Date.valueOf(date);
                    final Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    int nen = -1;
                    final int tuki = cal.get(Calendar.MONTH) + 1;
                    final int hi = cal.get(Calendar.DAY_OF_MONTH);

                    String mark = " ";
                    for (final Iterator it = _nameMstL007.iterator(); it.hasNext();) {
                        final Map m = (Map) it.next();
                        final String namespare2 = (String) m.get("NAMESPARE2");
                        if (null != namespare2) {
                            final Calendar dcal = Calendar.getInstance();
                            dcal.setTime(df.parse(namespare2.replace('/', '-')));
                            if (dcal.before(cal)) {
                                mark = StringUtils.defaultString((String) m.get("ABBV1"), " ");
                                nen = cal.get(Calendar.YEAR) - dcal.get(Calendar.YEAR) + 1;
                                break;
                            }
                        }
                    }
                    return mark + keta(nen, 2) + "." + keta(tuki, 2) + "." + keta(hi, 2);
                } catch (Exception e) {
                    log.error("format exception! date = " + date, e);
                }
            }
            return null;
        }

        public String keta(final int n, final int keta) {
            return StringUtils.repeat(" ", keta - String.valueOf(n).length()) + String.valueOf(n);
        }

        private List getNameMstL007(final DB2UDB db2) {
            final List list = new ArrayList();
            final String sql = " SELECT * FROM NAME_MST WHERE NAMECD1 = 'L007' ORDER BY NAMECD2 DESC ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();

                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnName(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    list.add(m);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }


        private List getNameMstL024(final DB2UDB db2) {
            final List list = new ArrayList();
            final String sql = " SELECT * FROM NAME_MST WHERE NAMECD1 = 'L024' ORDER BY NAMECD2 ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();

                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnName(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    list.add(m);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
    }
}

// eof

