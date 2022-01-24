/*
 * $Id: 5e98291502fb7d837fce6807b519a933fb245e56 $
 *
 * 作成日: 2019/04/26
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
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
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJM490E {

    private static final Log log = LogFactory.getLog(KNJM490E.class);

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

    /**
     *  svf print スクーリング＋レポート
     */
    private void printMain(DB2UDB db2, Vrw32alp svf) {
        PreparedStatement psSchAttend2 = null;
        PreparedStatement psHrAttend   = null;
        PreparedStatement psReport   = null;

        final boolean isPrintBoth = _param._output.equals("1");
        final boolean isPrintSchooling = _param._output.equals("2");
        final String outputdiv;
        if (isPrintBoth){
            outputdiv = "スクーリング＆レポート";
        } else if (isPrintSchooling){
            outputdiv = "スクーリングのみ";
        } else {
            outputdiv = "レポートのみ";
        }
        String p1reStat = p1reStat();
        final List<Map<String, String>> rowList = KnjDbUtils.query(db2, p1reStat);

        final int maxLine;
        if (isPrintBoth) {
            maxLine = 21;
            svf.VrSetForm("KNJM490E_2.frm", 4);            //セットフォーム
        } else {
            maxLine = 42;
            svf.VrSetForm("KNJM490E_1.frm", 4);            //セットフォーム
        }
        boolean schchange = false;
        boolean strchange = true ;
        int gyo = 1;
        int schcnt = 0;
        String befschregno = "*";
        try {
            for (final Map row : rowList) {
                svf.VrsOut("TEST_NAME2", "単認");
                if (gyo > maxLine){
                    schcnt = 0;
                    gyo = 1;
                    schchange = true;
                }
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                if (!befschregno.equals(schregno) && gyo > 1) {
                    schcnt++;
                    schchange = true;
                }

                //結合用マスクフィールド
                svf.VrsOut("MASK1", String.valueOf(schcnt));
                svf.VrsOut("MASK2", String.valueOf(schcnt));
                svf.VrsOut("MASK3", String.valueOf(schcnt));
                svf.VrsOut("MASK4", String.valueOf(schcnt));
                svf.VrsOut("MASK5", String.valueOf(schcnt));

                //ヘッダ
                svf.VrsOut("NENDO", _param._ctrlYear + "年度");         //年度
                svf.VrsOut("DATE", _param._ctrlDate);         //作成日

                //科目
                if (_param._subclass.equals("0")) {
                    svf.VrsOut("SUBCLASS1", "全科目");                     //科目
                } else {
                    svf.VrsOut("SUBCLASS1", KnjDbUtils.getString(row, "SUBCLASSNAME")); //科目
                }

                //出力順序
                if (_param._sort.equals("1")) {
                    svf.VrsOut("ORDER", "クラス出席番号順");   //出力順
                } else {
                    svf.VrsOut("ORDER", "学籍番号順");         //出力順
                }

                //出力形式
                svf.VrsOut("OUTPUTDIV", outputdiv); //出力形式
                svf.VrsOut("SIKEN_NAME", "試験");  //前期

                svf.VrsOut("SUBCLASS2", KnjDbUtils.getString(row, "SUBCLASSABBV"));     //科目名(略称)
                svf.VrsOut("GRAD_VALUE", KnjDbUtils.getString(row, "GRAD_VALUE"));       //評定
                svf.VrsOut("CREDITS", KnjDbUtils.getString(row, "CREDITS"));          //認定
                if (!StringUtils.isEmpty(KnjDbUtils.getString(row, "SEM1_TERM_SCORE_2"))) {
                    svf.VrsOut("SCORE1", "*" + KnjDbUtils.getString(row, "SEM1_TERM_SCORE_2"));  //前期
                } else {
                    svf.VrsOut("SCORE1", KnjDbUtils.getString(row, "SEM1_TERM_SCORE"));  //前期
                }
                if (!StringUtils.isEmpty(KnjDbUtils.getString(row, "SEM2_TERM_SCORE_2"))) {
                    svf.VrsOut("SCORE2", "*" + KnjDbUtils.getString(row, "SEM2_TERM_SCORE_2"));  //単認
                } else {
                    svf.VrsOut("SCORE2", KnjDbUtils.getString(row, "SEM2_TERM_SCORE"));  //単認
                }

                if (isPrintBoth) {
                    svf.VrsOut("SEQ1_1", KnjDbUtils.getString(row, "SCH_SEQ_MIN"));      //規定数スクーリング
                    svf.VrsOut("SEQ1_2", KnjDbUtils.getString(row, "REP_SEQ_ALL"));      //規定数レポート
                } else if (isPrintSchooling) {
                    svf.VrsOut("SEQ1", KnjDbUtils.getString(row, "SCH_SEQ_MIN"));      //規定数
                } else {
                    svf.VrsOut("SEQ1", KnjDbUtils.getString(row, "REP_SEQ_ALL"));      //規定数
                }

                // 出欠
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                if (isPrintBoth || isPrintSchooling) {
                    log.debug(schregno + " " + subclasscd);
                    if (null == psSchAttend2) {
                        final String attendSeq = getSchAttendDatSql();
                        log.info(" attendSeq = " + attendSeq);
                        psSchAttend2 = db2.prepareStatement(attendSeq);
                    }
                    final List<Map<String, String>> attendRowList = KnjDbUtils.query(db2, psSchAttend2, new Object[] {schregno, subclasscd, schregno, subclasscd});
                    final BigDecimal _1 = BigDecimal.ONE;
                    BigDecimal schCount = new BigDecimal(0);
                    for (int kindi = 0; kindi < Math.min(25, attendRowList.size()); kindi++) {
                        final Map<String, String> attendRow = attendRowList.get(kindi);
                        final String executedate = KnjDbUtils.getString(attendRow, "EXECUTEDATE");
                        if (executedate == null) {
                            continue;
                        }
                        final String flg = KnjDbUtils.getString(attendRow, "FLG");
                        final String seqdate;
                        if ("6".equals(KnjDbUtils.getString(attendRow, "SCHOOLINGKINDCD"))) {
                            seqdate = "放送";
                        } else {
                            seqdate = executedate.substring(5,7) + executedate.substring(8,10);
                        }
                        log.debug("seqdate" + String.valueOf(seqdate));
                        String field;
                        if (isPrintBoth) {
                            field = "DATE";
                        } else {
                            field = "DATE_VALUE";
                        }
                        svf.VrsOut(field + String.valueOf(kindi + 1), String.valueOf(seqdate));
                        if ("SP".equals(flg)) {
                            final BigDecimal creditTime = KnjDbUtils.getBigDecimal(attendRow, "CREDIT_TIME", null);
                            if (null != creditTime) {
                                schCount = schCount.add(creditTime);
                            }
                        } else {
                            schCount = schCount.add(_1);
                        }
                    }
                    String field;
                    if (isPrintBoth) {
                        field = "SEQ2_1";
                    } else {
                        field = "SEQ2";
                    }
                    svf.VrsOut(field, getDispNum(schCount));       //実数
                }

                // レポート
                if (isPrintBoth || !isPrintSchooling) {
                    if (null == psReport) {
                        final String reportSql = getReportSql();
                        psReport = db2.prepareStatement(reportSql);
                    }
                    log.debug(schregno + " " + subclasscd);
                    final List<Map<String, String>> reportRowList = KnjDbUtils.query(db2, psReport, new Object[] {subclasscd, schregno, subclasscd, schregno});
                    int befiseq = -1;            //回数判定用
                    String befgrdvalue   = "*";             //前回評定
                    boolean setflg = false;          //回数判定用
                    String field;
                    if (isPrintBoth) {
                        field = "VALUE";
                    } else {
                        field = "DATE_VALUE";
                    }
                    int seqcnt = 0;
                    for (final Map<String, String> reportRow : reportRowList) {
                        final String standardSeq = KnjDbUtils.getString(reportRow, "STANDARD_SEQ");
                        final String grdvalue = KnjDbUtils.getString(reportRow, "GRDVALUE");
                        final int iseq = Integer.parseInt(standardSeq);
                        if (iseq > 0 && iseq < 25){
                            if (befiseq == iseq && !setflg) {
                                if (befgrdvalue.equals("受")) {
                                    svf.VrsOut(field + standardSeq, "無".equals(grdvalue) ? "受" : "再");
                                }
                                setflg = true;
                            } else {
                                if (befiseq != iseq) {
                                    svf.VrsOut(field + standardSeq, grdvalue);
                                    if ("1".equals(KnjDbUtils.getString(reportRow, "JUDGE"))) {
                                        seqcnt++;
                                    }
                                    setflg = false;
                                }
                            }
                        }
                        befiseq = iseq;
                        befgrdvalue = grdvalue;
                    }
                    String field2 = "";
                    if (isPrintBoth) {
                        field2 = "SEQ2_2";
                    } else {
                        field2 = "SEQ2";
                    }
                    svf.VrsOut(field2, String.valueOf(seqcnt));       //実数
                }

                //生徒単位での最初のデータのみ出力
                if (schchange || strchange) {
                    if (null == psHrAttend) {
                        final String sql = getHrAttendDatSql();
                        psHrAttend = db2.prepareStatement(sql);
                    }

                    svf.VrsOut("SCHREGNO"     ,schregno);         //学籍番号
                    svf.VrsOut("NAME"         ,KnjDbUtils.getString(row, "NAME"));             //生徒氏名
                    svf.VrsOut("HR_NAME"      ,KnjDbUtils.getString(row, "HR_NAMEABBV"));      //クラス

                    final Map<String, String> attendRow = KnjDbUtils.firstRow(KnjDbUtils.query(db2, psHrAttend, new Object[] {schregno}));
                    final String hrAttend = KnjDbUtils.getString(attendRow, "HR_ATTEND");   //特別活動

                    svf.VrsOut("HR_ATTEND", hrAttend);
                }

                gyo++;
                befschregno = schregno;
                svf.VrEndRecord();
                schchange = false;
                strchange = false;
                _hasData = true;
            }
        } catch (Exception ex) {
            log.error("setSvfMain set error!", ex);
        }
        DbUtils.closeQuietly(psSchAttend2);
        DbUtils.closeQuietly(psHrAttend);
        DbUtils.closeQuietly(psReport);
        db2.commit();
    }

    private static String getDispNum(final BigDecimal bd) {
        if (null == bd) {
            return null;
        }
        if (bd.setScale(0, BigDecimal.ROUND_UP).equals(bd.setScale(0, BigDecimal.ROUND_DOWN))) {
            // 切り上げでも切り下げでも値が変わらない = 小数点以下が0
            return bd.setScale(0).toString();
        }
        return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**データ　取得**/
    private String p1reStat() {
        StringBuffer stb = new StringBuffer();
        stb.append("WITH SCHTABLE AS ( ");
        stb.append("SELECT ");
        stb.append("    t1.SCHREGNO,t1.GRADE,t1.HR_CLASS,t1.ATTENDNO,t2.NAME,t3.HR_NAMEABBV, ");
        stb.append("    t1.COURSECD, t1.MAJORCD, t1.COURSECODE ");
        stb.append("FROM ");
        stb.append("    SCHREG_REGD_DAT t1 ");
        stb.append("    LEFT JOIN SCHREG_BASE_MST t2 ON t1.SCHREGNO = t2.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_REGD_HDAT t3 ON t1.GRADE = t3.GRADE AND t1.HR_CLASS = t3.HR_CLASS ");
        stb.append("    AND t3.YEAR = t1.YEAR ");
        stb.append("    AND t3.SEMESTER = t1.SEMESTER ");
        stb.append("WHERE ");
        stb.append("    t1.YEAR = '"+_param._ctrlYear+"' ");
        stb.append("    AND t1.SEMESTER = '"+_param._ctrlSemester+"' ");
        stb.append("    AND t1.GRADE || t1.HR_CLASS in "+_param._gadeHrClassIn+" ");
        stb.append(") ");
        stb.append("SELECT ");
        stb.append("    t1.*, ");
        stb.append("    t3.CLASSCD || t3.SCHOOL_KIND || t3.CURRICULUM_CD || t3.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("    t3.TAKESEMES,t4.SUBCLASSABBV,t4.SUBCLASSNAME, ");
        stb.append("    HIST1.SCORE as SEM1_TERM_SCORE, ");
        stb.append("    HIST1_2.SCORE as SEM1_TERM_SCORE_2, ");
        stb.append("    HIST2.SCORE as SEM2_TERM_SCORE, ");
        stb.append("    HIST2_2.SCORE as SEM2_TERM_SCORE_2, ");
        stb.append("    HIST9.VALUE as GRAD_VALUE, ");
        if (_param._output.equals("1")) {
            stb.append("    t6.SCH_SEQ_MIN, ");
            stb.append("    t6.REP_SEQ_ALL, ");
        } else if (_param._output.equals("2")) {
            stb.append("    t6.SCH_SEQ_MIN, ");
        } else {
            stb.append("    t6.REP_SEQ_ALL, ");
        }
        stb.append("    t8.CREDITS ");
        stb.append("FROM ");
        stb.append("    SCHTABLE t1 ");
        stb.append("    LEFT JOIN CHAIR_STD_DAT t2 ON t1.SCHREGNO = t2.SCHREGNO ");
        stb.append("    AND t2.YEAR = '"+_param._ctrlYear+"' ");
        if ("sagaken".equals(_param._z010)) {
            stb.append("    AND t2.SEMESTER <= '"+_param._ctrlSemester+"' ");
        } else {
            stb.append("    AND t2.SEMESTER = '"+_param._ctrlSemester+"' ");
        }
        stb.append("    AND t2.CHAIRCD NOT LIKE '92%' ");
        stb.append("    LEFT JOIN CHAIR_DAT t3 ON t2.CHAIRCD = t3.CHAIRCD ");
        stb.append("    AND t3.YEAR = t2.YEAR ");
        stb.append("    AND t3.SEMESTER = t2.SEMESTER ");
        stb.append("    LEFT JOIN SUBCLASS_MST t4 ON t3.SUBCLASSCD = t4.SUBCLASSCD ");
        stb.append("    AND t3.CLASSCD = t4.CLASSCD ");
        stb.append("    AND t3.SCHOOL_KIND = t4.SCHOOL_KIND ");
        stb.append("    AND t3.CURRICULUM_CD = t4.CURRICULUM_CD ");
        stb.append("    LEFT JOIN RECORD_SCORE_HIST_DAT HIST1 ON HIST1.YEAR          = '"+_param._ctrlYear+"' ");
        stb.append("                                         AND HIST1.SEMESTER      = '1' "); // 前期
        stb.append("                                         AND HIST1.TESTKINDCD    = '01' ");
        stb.append("                                         AND HIST1.TESTITEMCD    = '01' ");
        stb.append("                                         AND HIST1.SCORE_DIV     = '01' ");
        stb.append("                                         AND HIST1.CLASSCD       = t3.CLASSCD ");
        stb.append("                                         AND HIST1.SCHOOL_KIND   = t3.SCHOOL_KIND ");
        stb.append("                                         AND HIST1.CURRICULUM_CD = t3.CURRICULUM_CD ");
        stb.append("                                         AND HIST1.SUBCLASSCD    = t3.SUBCLASSCD ");
        stb.append("                                         AND HIST1.SCHREGNO      = t1.SCHREGNO ");
        stb.append("                                         AND HIST1.SEQ           = '1' "); // 一回目
        stb.append("    LEFT JOIN RECORD_SCORE_HIST_DAT HIST1_2 ON HIST1_2.YEAR          = '"+_param._ctrlYear+"' ");
        stb.append("                                         AND HIST1_2.SEMESTER      = '1' "); // 前期
        stb.append("                                         AND HIST1_2.TESTKINDCD    = '01' ");
        stb.append("                                         AND HIST1_2.TESTITEMCD    = '01' ");
        stb.append("                                         AND HIST1_2.SCORE_DIV     = '01' ");
        stb.append("                                         AND HIST1_2.CLASSCD       = t3.CLASSCD ");
        stb.append("                                         AND HIST1_2.SCHOOL_KIND   = t3.SCHOOL_KIND ");
        stb.append("                                         AND HIST1_2.CURRICULUM_CD = t3.CURRICULUM_CD ");
        stb.append("                                         AND HIST1_2.SUBCLASSCD    = t3.SUBCLASSCD ");
        stb.append("                                         AND HIST1_2.SCHREGNO      = t1.SCHREGNO ");
        stb.append("                                         AND HIST1_2.SEQ           = '2' "); // 2回目
        stb.append("    LEFT JOIN RECORD_SCORE_HIST_DAT HIST2 ON HIST2.YEAR          = '"+_param._ctrlYear+"' ");
        stb.append("                                         AND HIST2.SEMESTER      = '2' "); // 前期
        stb.append("                                         AND HIST2.TESTKINDCD    = '01' ");
        stb.append("                                         AND HIST2.TESTITEMCD    = '01' ");
        stb.append("                                         AND HIST2.SCORE_DIV     = '01' ");
        stb.append("                                         AND HIST2.CLASSCD       = t3.CLASSCD ");
        stb.append("                                         AND HIST2.SCHOOL_KIND   = t3.SCHOOL_KIND ");
        stb.append("                                         AND HIST2.CURRICULUM_CD = t3.CURRICULUM_CD ");
        stb.append("                                         AND HIST2.SUBCLASSCD    = t3.SUBCLASSCD ");
        stb.append("                                         AND HIST2.SCHREGNO      = t1.SCHREGNO ");
        stb.append("                                         AND HIST2.SEQ           = '1' "); // 一回目
        stb.append("    LEFT JOIN RECORD_SCORE_HIST_DAT HIST2_2 ON HIST2_2.YEAR          = '"+_param._ctrlYear+"' ");
        stb.append("                                         AND HIST2_2.SEMESTER      = '2' "); // 前期
        stb.append("                                         AND HIST2_2.TESTKINDCD    = '01' ");
        stb.append("                                         AND HIST2_2.TESTITEMCD    = '01' ");
        stb.append("                                         AND HIST2_2.SCORE_DIV     = '01' ");
        stb.append("                                         AND HIST2_2.CLASSCD       = t3.CLASSCD ");
        stb.append("                                         AND HIST2_2.SCHOOL_KIND   = t3.SCHOOL_KIND ");
        stb.append("                                         AND HIST2_2.CURRICULUM_CD = t3.CURRICULUM_CD ");
        stb.append("                                         AND HIST2_2.SUBCLASSCD    = t3.SUBCLASSCD ");
        stb.append("                                         AND HIST2_2.SCHREGNO      = t1.SCHREGNO ");
        stb.append("                                         AND HIST2_2.SEQ           = '2' "); // 2回目
        stb.append("    LEFT JOIN RECORD_SCORE_HIST_DAT HIST9 ON HIST9.YEAR          = '"+_param._ctrlYear+"' ");
        stb.append("                                         AND HIST9.SEMESTER      = '9' ");
        stb.append("                                         AND HIST9.TESTKINDCD    = '99' ");
        stb.append("                                         AND HIST9.TESTITEMCD    = '00' ");
        stb.append("                                         AND HIST9.SCORE_DIV     = '09' ");
        stb.append("                                         AND HIST9.CLASSCD       = t3.CLASSCD ");
        stb.append("                                         AND HIST9.SCHOOL_KIND   = t3.SCHOOL_KIND ");
        stb.append("                                         AND HIST9.CURRICULUM_CD = t3.CURRICULUM_CD ");
        stb.append("                                         AND HIST9.SUBCLASSCD    = t3.SUBCLASSCD ");
        stb.append("                                         AND HIST9.SCHREGNO      = t1.SCHREGNO ");
        stb.append("                                         AND HIST9.SEQ           = '1' "); // 一回目
        stb.append("    LEFT JOIN CHAIR_CORRES_DAT t6 ON t2.CHAIRCD = t6.CHAIRCD ");
        stb.append("    AND t6.YEAR = '"+_param._ctrlYear+"' ");
        stb.append("    AND t6.CLASSCD = t3.CLASSCD ");
        stb.append("    AND t6.SCHOOL_KIND = t3.SCHOOL_KIND ");
        stb.append("    AND t6.CURRICULUM_CD = t3.CURRICULUM_CD ");
        stb.append("    AND t6.SUBCLASSCD = t3.SUBCLASSCD ");
        stb.append("    LEFT JOIN CREDIT_MST t8 ON t8.YEAR = '"+_param._ctrlYear+"' ");
        stb.append("    AND t8.COURSECD = t1.COURSECD ");
        stb.append("    AND t8.MAJORCD = t1.MAJORCD ");
        stb.append("    AND t8.GRADE = t1.GRADE ");
        stb.append("    AND t8.COURSECODE = t1.COURSECODE ");
        stb.append("    AND t8.CLASSCD = t3.CLASSCD ");
        stb.append("    AND t8.SCHOOL_KIND = t3.SCHOOL_KIND ");
        stb.append("    AND t8.CURRICULUM_CD = t3.CURRICULUM_CD ");
        stb.append("    AND t8.SUBCLASSCD = t3.SUBCLASSCD ");
        stb.append("    LEFT JOIN SCH_ATTEND_DAT t7 ON t1.SCHREGNO = t7.SCHREGNO ");
        stb.append("        AND t7.YEAR = '"+_param._ctrlYear+"' ");
        stb.append("        AND t7.CHAIRCD = t2.CHAIRCD ");
        if (!_param._subclass.equals("0")) {
            stb.append("WHERE ");
            stb.append("    t3.CLASSCD || '-' || t3.SCHOOL_KIND || '-' || t3.CURRICULUM_CD || '-' || t3.SUBCLASSCD = '" + _param._subclass.substring(0,13) + "' ");
        }
        stb.append("GROUP BY ");
        stb.append("   t1.SCHREGNO,t1.GRADE,t1.HR_CLASS,t1.ATTENDNO,t1.NAME,t1.HR_NAMEABBV, t1.COURSECD, t1.MAJORCD, t1.COURSECODE, ");
        stb.append("    t3.CLASSCD, t3.SCHOOL_KIND, t3.CURRICULUM_CD, t3.SUBCLASSCD, ");
        stb.append("   t3.TAKESEMES,t4.SUBCLASSABBV,t4.SUBCLASSNAME, ");
        stb.append("   HIST1.SCORE, HIST1_2.SCORE, HIST2.SCORE,HIST2_2.SCORE, HIST9.VALUE, ");
        if (_param._output.equals("1")) {
            stb.append("   t6.SCH_SEQ_MIN,t6.REP_SEQ_ALL ");
        } else if (_param._output.equals("2")) {
            stb.append("   t6.SCH_SEQ_MIN ");
        } else {
            stb.append("   t6.REP_SEQ_ALL ");
        }
        stb.append("   , t8.CREDITS ");
        stb.append("ORDER BY ");
        if ("1".equals(_param._sort)) {
            stb.append("   t1.GRADE,t1.HR_CLASS,t1.ATTENDNO ");
        } else {
            stb.append("   t1.SCHREGNO ");
        }
        stb.append("   , t3.CLASSCD, t3.SCHOOL_KIND, t3.CURRICULUM_CD, t3.SUBCLASSCD ");
        log.debug(stb);
        return stb.toString();

    }//preStat()の括り

    /**データ　取得**/
    private String getSchAttendDatSql() {
        StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    'SCH' AS FLG, T1.EXECUTEDATE, T1.SCHOOLINGKINDCD, CAST(NULL AS DECIMAL(3,1)) AS CREDIT_TIME ");
        stb.append("FROM ");
        stb.append("    SCH_ATTEND_DAT T1 ");
        stb.append("    INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = T1.YEAR AND SEME.SEMESTER <> '9' AND T1.EXECUTEDATE BETWEEN SEME.SDATE AND SEME.EDATE ");
        stb.append("    INNER JOIN CHAIR_DAT CHR ON CHR.YEAR = T1.YEAR AND CHR.SEMESTER = SEME.SEMESTER AND CHR.CHAIRCD = T1.CHAIRCD ");
        stb.append("WHERE ");
        stb.append("    T1.YEAR = '"+_param._ctrlYear+"' ");
        stb.append("    AND T1.SCHREGNO = ? ");
        stb.append("    AND CHR.CLASSCD || CHR.SCHOOL_KIND || CHR.CURRICULUM_CD || CHR.SUBCLASSCD = ? ");
        stb.append("UNION ALL ");
        stb.append("SELECT ");
        stb.append("    'SP' AS FLG, T2.ATTENDDATE AS EXECUTEDATE, CAST(NULL AS VARCHAR(1)) AS SCHOOLINGKINDCD, T2.CREDIT_TIME ");
        stb.append("FROM ");
        stb.append("    SPECIALACT_ATTEND_DAT T2 ");
        stb.append(" INNER JOIN V_NAME_MST NM_M027 ON NM_M027.YEAR = T2.YEAR ");
        stb.append("     AND NM_M027.NAMECD1 = 'M027' ");
        stb.append("     AND NM_M027.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
        stb.append(" LEFT JOIN V_NAME_MST NM_M026 ON NM_M026.YEAR = T2.YEAR ");
        stb.append("     AND NM_M026.NAMECD1 = 'M026' ");
        stb.append("     AND NM_M026.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
        stb.append("WHERE ");
        stb.append("    T2.YEAR = '"+_param._ctrlYear+"' ");
        stb.append("    AND T2.SCHREGNO = ? ");
        stb.append("    AND T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD = ? ");
        stb.append("ORDER BY ");
        stb.append("    EXECUTEDATE ");
        return stb.toString();

    }//preStat()の括り

    /**データ　取得**/
    private String getHrAttendDatSql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     INT(SUM(CAST(T2.CREDIT_TIME AS DECIMAL(5,1)))) AS HR_ATTEND ");
        stb.append(" FROM SCHREG_BASE_MST T1 ");
        stb.append(" INNER JOIN SPECIALACT_ATTEND_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T2.YEAR = '" + _param._ctrlYear + "' ");
        stb.append(" INNER JOIN V_NAME_MST NM_M027 ON NM_M027.YEAR = T2.YEAR ");
        stb.append("     AND NM_M027.NAMECD1 = 'M027' ");
        stb.append("     AND NM_M027.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
        stb.append(" LEFT JOIN V_NAME_MST NM_M026 ON NM_M026.YEAR = T2.YEAR ");
        stb.append("     AND NM_M026.NAMECD1 = 'M026' ");
        stb.append("     AND NM_M026.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     VALUE(NM_M026.NAMESPARE2, '') <> '1' ");
        stb.append("     AND T1.SCHREGNO = ? ");
        return stb.toString();

    }//preStat()の括り

    /**データ　取得**/
    private String getReportSql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_REP_PRESENT_DAT_SEQ AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, STANDARD_SEQ,MAX(REPRESENT_SEQ) AS REPRESENT_SEQ ");
        stb.append(" FROM ");
        stb.append("     REP_PRESENT_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '"+_param._ctrlYear+"' AND ");
        stb.append("     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = ? AND ");
        stb.append("     SCHREGNO = ? ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, STANDARD_SEQ ");
        stb.append(" ), MAX_REP_PRESENT_DAT AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.STANDARD_SEQ, T1.REPRESENT_SEQ, MAX(T1.RECEIPT_DATE) AS RECEIPT_DATE ");
        stb.append(" FROM ");
        stb.append("     REP_PRESENT_DAT T1 ");
        stb.append("     INNER JOIN MAX_REP_PRESENT_DAT_SEQ T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND T2.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("         AND T2.REPRESENT_SEQ = T1.REPRESENT_SEQ ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '"+_param._ctrlYear+"' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.STANDARD_SEQ, T1.REPRESENT_SEQ ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     t1.STANDARD_SEQ,t1.RECEIPT_DATE, ");
        stb.append("     CASE WHEN VALUE(t1.GRAD_VALUE, '') = '' THEN '受' ELSE ABBV1 END AS GRDVALUE, ");
        stb.append("     CASE WHEN NAMESPARE1 = '1' THEN '1' ELSE '0' END AS JUDGE ");
        stb.append(" FROM ");
        stb.append("     REP_PRESENT_DAT t1 ");
        stb.append("     LEFT JOIN NAME_MST ON GRAD_VALUE = NAMECD2 AND NAMECD1 = 'M003' ");
        stb.append("     INNER JOIN MAX_REP_PRESENT_DAT t2 ON t1.STANDARD_SEQ = t2.STANDARD_SEQ AND ");
        stb.append("     t1.REPRESENT_SEQ = t2.REPRESENT_SEQ AND ");
        stb.append("     t1.RECEIPT_DATE = t2.RECEIPT_DATE ");
        stb.append(" WHERE ");
        stb.append("     t1.YEAR = '"+_param._ctrlYear+"' AND ");
        stb.append("     t1.CLASSCD || t1.SCHOOL_KIND || t1.CURRICULUM_CD || t1.SUBCLASSCD = ? AND ");
        stb.append("     t1.SCHREGNO = ? ");
        stb.append(" ORDER BY ");
        stb.append("     t1.STANDARD_SEQ,t1.RECEIPT_DATE DESC ");

        log.debug(stb);
        return stb.toString();
    }//preStat()の括り

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75158 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _subclass;
        private final String _sort;
        private final String _output;
        private final String _useCurriculumcd;
        private final String _gadeHrClassIn;
        private final String _z010;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _ctrlYear           = request.getParameter("CTRL_YEAR");
            _ctrlSemester       = request.getParameter("CTRL_SEMESTER");
            _ctrlDate           = StringUtils.replace(request.getParameter("CTRL_DATE"), "-", "/");
            _subclass           = request.getParameter("SUBCLASS");
            _sort               = request.getParameter("SORT");
            _output             = request.getParameter("OUTPUT");
            _useCurriculumcd    = request.getParameter("useCurriculumcd");

            final String[] classSelected = request.getParameterValues("CATEGORY_NAME");
            _gadeHrClassIn = getGradeHrClassIn(classSelected);

            _z010 = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }

        private String getGradeHrClassIn(final String[] classSelected) {
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < classSelected.length; i++) {
                if (0 < i) stb.append(",");
                stb.append("'" + classSelected[i] + "'");
            }
            stb.append(")");
            return stb.toString();
        }
    }
}

// eof
