/*
 * $Id: 05ac3cf046971448bedbbf3e6345b7b6a5020dec $
 *
 * 作成日: 2013/08/29
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

/**
 * 定期考査欠点者一覧
 */
public class KNJM837 {

    private static final Log log = LogFactory.getLog(KNJM837.class);

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
        final List entYearList = getEntYearList();
        for (final Iterator it = entYearList.iterator(); it.hasNext();) {
            final EntYear entYearClass = (EntYear) it.next();

            printJuken(db2, svf, entYearClass);
            printJoui(db2, svf, entYearClass);
            printKetten(db2, svf, entYearClass);
        }
    }

    private void printJuken(final DB2UDB db2, final Vrw32alp svf, final EntYear entYearClass) {
        svf.VrSetForm("KNJM837_1.frm", 1);

        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._loginYear + "-04-01") + "度 "+ StringUtils.defaultString(_param._semesterName) + " 成績会議資料");
        if (entYearClass._lastYearFlg) {
            svf.VrsOut("NENDO", entYearClass._entYear + "年度生以前");
        } else {
            svf.VrsOut("NENDO", entYearClass._entYear + "年度生");
        }
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
        svf.VrsOut("PAGE", "1");
        svf.VrsOut("TOTAL_PAGE", "1");

        final List jukenList = getJukenList(db2, entYearClass);
        for (final Iterator it = jukenList.iterator(); it.hasNext();) {
            final Juken juken = (Juken) it.next();
            svf.VrsOutn("GEN_ST", juken._gyo, juken._cntIppann);
            svf.VrsOutn("TRANS_ST", juken._gyo, juken._cntTenHen);
            svf.VrsOutn("TOTAL", juken._gyo, juken._cntTotal);
            svf.VrsOutn("REMARK0", juken._gyo, juken._remark);
        }

        _hasData = true;
        svf.VrEndPage();
    }

    private void printJoui(final DB2UDB db2, final Vrw32alp svf, final EntYear entYearClass) {
        svf.VrSetForm("KNJM837_2.frm", 4);
        final int maxLine = 36 * 2;
        final List jouiStudentList = getJouiStudentList(db2, entYearClass);
        final int totalPage = getPage(maxLine, jouiStudentList.size());

        int printLine = 0;
        for (final Iterator it = jouiStudentList.iterator(); it.hasNext();) {
            final JouiStudent student = (JouiStudent) it.next();

            printLine++;
            final int page = getPage(maxLine, printLine);
            svf.VrsOut("PAGE", String.valueOf(page));
            svf.VrsOut("TOTAL_PAGE", String.valueOf(totalPage));

            svf.VrsOut("SCHREG_NO", student._schregno);
            final String suf = getMS932ByteLength(student._name) > 30 ? "4" : getMS932ByteLength(student._name) > 20 ? "3" : getMS932ByteLength(student._name) > 14 ? "2" : "1";
            svf.VrsOut("NAME" + suf, student._name);
            svf.VrsOut("AVERAGE", student._avg);
            svf.VrsOut("NUM", student._cnt);

            svf.VrEndRecord();
        }
        while (printLine < maxLine) {
            printLine++;
            final int page = getPage(maxLine, printLine);
            svf.VrsOut("PAGE", String.valueOf(page));
            svf.VrsOut("TOTAL_PAGE", String.valueOf(totalPage));
            svf.VrEndRecord();
        }
    }

    private void printKetten(final DB2UDB db2, final Vrw32alp svf, final EntYear entYearClass) {
        svf.VrSetForm("KNJM837_3.frm", 4);
        final int maxLine = 36;
        final List kettenStudentList = getKettenStudentList(db2, entYearClass);
        final int totalPage = getPage(maxLine, kettenStudentList.size());

        int printLine = 0;
        for (final Iterator it = kettenStudentList.iterator(); it.hasNext();) {
            final KettenStudent student = (KettenStudent) it.next();

            printLine++;
            final int page = getPage(maxLine, printLine);
            svf.VrsOut("PAGE", String.valueOf(page));
            svf.VrsOut("TOTAL_PAGE", String.valueOf(totalPage));

            svf.VrsOut("SCHREG_NO", student._schregno);
            final String suf = getMS932ByteLength(student._name) > 30 ? "4" : getMS932ByteLength(student._name) > 20 ? "3" : getMS932ByteLength(student._name) > 14 ? "2" : "1";
            svf.VrsOut("NAME" + suf, student._name);
            svf.VrsOut("AVERAGE", student._avg);
            svf.VrsOut("NUM", student._cnt);

            String seq = "";
            String subNameAndScore = "";
            for (final Iterator it2 = student._kettenKamokuList.iterator(); it2.hasNext();) {
                final KettenKamoku kettenKamoku = (KettenKamoku) it2.next();

                subNameAndScore = subNameAndScore + seq + kettenKamoku._subclassabbv + " " + kettenKamoku._score + "(" + kettenKamoku._scoreHoju + ")";
                seq = "  ";
            }
            final String suf2 = getMS932ByteLength(subNameAndScore) > 50 ? "2_1" : "1";
            svf.VrsOut("SUBCLASS" + suf2, subNameAndScore);

            svf.VrEndRecord();
        }
        while (printLine < maxLine) {
            printLine++;
            final int page = getPage(maxLine, printLine);
            svf.VrsOut("PAGE", String.valueOf(page));
            svf.VrsOut("TOTAL_PAGE", String.valueOf(totalPage));
            svf.VrEndRecord();
        }
    }
    
    private static int getMS932ByteLength(final String name) {
        int len = 0;
        if (null != name) {
            try {
                len = name.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return len;
    }

    private int getPage(final int maxLine, final int printLine) {
        return printLine / maxLine + (printLine % maxLine == 0 ? 0 : 1);
    }

    private List getEntYearList() {
        final List list = new ArrayList();
        try {
            for (int i = 0; i <= 3; i++) {
                final String entYear = String.valueOf(Integer.parseInt(_param._loginYear) - i);
                final boolean lastYearFlg = (i == 3) ? true : false;
                 final EntYear entYearClass = new EntYear(entYear, lastYearFlg);
                 list.add(entYearClass);
            }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        }
        return list;
    }

    private List getJukenList(final DB2UDB db2, final EntYear entYearClass) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
             final String sql = getJukenSql(entYearClass);
             ps = db2.prepareStatement(sql);
             rs = ps.executeQuery();

             //行配列
             int arrIppann[] = new int[11];
             int arrTenHen[] = new int[11];
             int arrTotal[]  = new int[11];
             String arrRemark[][] = new String[11][5];
             boolean dataFlg = false;

             while (rs.next()) {
                 final String cntDiv = rs.getString("CNT_DIV");
                 final String cnt = rs.getString("CNT");
                 final int cntInt = Integer.parseInt(cnt);
                 
                 //一般生
                 if ("02".equals(cntDiv)) arrIppann[1] = cntInt;
                 if ("04".equals(cntDiv)) arrIppann[2] = cntInt;
                 if ("06".equals(cntDiv)) arrIppann[3] = cntInt;
                 if ("13".equals(cntDiv)) arrIppann[5] = cntInt;
                 if ("16".equals(cntDiv)) arrIppann[6] = cntInt;
                 if ("19".equals(cntDiv)) arrIppann[7] = cntInt;
                 if ("22".equals(cntDiv)) arrIppann[8] = cntInt;
                 //転編入生
                 if ("03".equals(cntDiv)) arrTenHen[1] = cntInt;
                 if ("05".equals(cntDiv)) arrTenHen[2] = cntInt;
                 if ("07".equals(cntDiv)) arrTenHen[3] = cntInt;
                 if ("14".equals(cntDiv)) arrTenHen[5] = cntInt;
                 if ("17".equals(cntDiv)) arrTenHen[6] = cntInt;
                 if ("20".equals(cntDiv)) arrTenHen[7] = cntInt;
                 if ("23".equals(cntDiv)) arrTenHen[8] = cntInt;
                 //備考
                 if ("08-4".equals(cntDiv)) arrRemark[2][1] = "転入生(" + cnt + ")";
                 if ("08-5".equals(cntDiv)) arrRemark[2][2] = "編入生(" + cnt + ")";
                 if ("09-3".equals(cntDiv)) arrRemark[3][1] = "転学(" + cnt + ")";
                 if ("09-2".equals(cntDiv)) arrRemark[3][2] = "退学(" + cnt + ")";
                 if ("09-9".equals(cntDiv)) arrRemark[3][3] = "除籍(" + cnt + ")";
                 if ("09-1".equals(cntDiv)) arrRemark[3][4] = "卒業(" + cnt + ")";
                 if ("15-4".equals(cntDiv)) arrRemark[5][1] = "転入生(" + cnt + ")";
                 if ("15-5".equals(cntDiv)) arrRemark[5][2] = "編入生(" + cnt + ")";
                 if ("18-3".equals(cntDiv)) arrRemark[6][1] = "転学(" + cnt + ")";
                 if ("18-2".equals(cntDiv)) arrRemark[6][2] = "退学(" + cnt + ")";
                 if ("18-9".equals(cntDiv)) arrRemark[6][3] = "除籍(" + cnt + ")";
                 if ("18-1".equals(cntDiv)) arrRemark[6][4] = "卒業(" + cnt + ")";
                 if ("21-1".equals(cntDiv)) arrRemark[7][1] = "(内 卒業予定 " + cnt + " 名)";
                 if ("28-1".equals(cntDiv)) arrRemark[8][1] = "(内 卒業予定 " + cnt + " 名)";
                 
                 dataFlg = true;
             }
             if (dataFlg) {
                 //本年度始めの在籍者数
                 arrIppann[4] = arrIppann[1] + arrIppann[2] - arrIppann[3];
                 arrTenHen[4] = arrTenHen[1] + arrTenHen[2] - arrTenHen[3];
                 //合計
                 for (int gyo = 1; gyo <= 8; gyo++) {
                     arrTotal[gyo] = arrIppann[gyo] + arrTenHen[gyo];
                 }
                 //受験率
                 //arrIppann[9] = (arrIppann[7] > 0) ? arrIppann[8] / arrIppann[7] * 100 : 0.0;
                 //arrTenHen[9] = (arrTenHen[7] > 0) ? arrTenHen[8] / arrTenHen[7] * 100 : 0.0;
                 //arrTotal[9]  = ( arrTotal[7] > 0) ?  arrTotal[8] /  arrTotal[7] * 100 : 0.0;
                 //未受験者数
                 arrIppann[10] = arrIppann[7] - arrIppann[8];
                 arrTenHen[10] = arrTenHen[7] - arrTenHen[8];
                 arrTotal[10]  =  arrTotal[7] -  arrTotal[8];
                 //項目
                 for (int gyo = 1; gyo <= 10; gyo++) {
                     final String cntIppann = (gyo == 9) ? percentage(arrIppann[8], arrIppann[7]) : String.valueOf(arrIppann[gyo]);
                     final String cntTenHen = (gyo == 9) ? percentage(arrTenHen[8], arrTenHen[7]) : String.valueOf(arrTenHen[gyo]);
                     final String cntTotal = (gyo == 9) ? percentage(arrTotal[8], arrTotal[7]) : String.valueOf(arrTotal[gyo]);
                     String remark = "";
                     String seq = "";
                     for (int no = 1; no <= 4; no++) {
                         if (arrRemark[gyo][no] != null) {
                             remark = remark + seq + arrRemark[gyo][no];
                             seq = "、";
                         }
                     }

                     final Juken juken = new Juken(gyo, cntIppann, cntTenHen, cntTotal, remark);
                     list.add(juken);
                 }
             }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }
        return list;
    }

    private String percentage(final int num1, final int num2) {
        if (0 == num2) {
            return "0.0";
        }
        final BigDecimal bd1 = new BigDecimal(num1);
        final BigDecimal bd2 = new BigDecimal(num2);
        return bd1.multiply(new BigDecimal(100)).divide(bd2, 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private List getJouiStudentList(final DB2UDB db2, final EntYear entYearClass) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
             final String jouiSql = getJouiAndKettenSql("joui", entYearClass);
             ps = db2.prepareStatement(jouiSql);
             rs = ps.executeQuery();
             while (rs.next()) {
                 final String schregno = rs.getString("SCHREGNO");
                 final String name = rs.getString("NAME");
                 final String avg = rs.getString("AVG");
                 final String cnt = rs.getString("CNT");
                 final JouiStudent student = new JouiStudent(schregno, name, avg, cnt);
                 list.add(student);
             }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }
        return list;
    }

    private List getKettenStudentList(final DB2UDB db2, final EntYear entYearClass) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
             final String kettenSql = getJouiAndKettenSql("ketten", entYearClass);
             ps = db2.prepareStatement(kettenSql);
             rs = ps.executeQuery();
             while (rs.next()) {
                 final String schregno = rs.getString("SCHREGNO");
                 KettenStudent student = getKetten(list, schregno);
                 if (null == student) {
                     final String name = rs.getString("NAME");
                     final String avg = rs.getString("AVG");
                     final String cnt = rs.getString("CNT");
                     student = new KettenStudent(schregno, name, avg, cnt);
                     list.add(student);
                 }
                 
                 final String classcd = rs.getString("CLASSCD");
                 final String schoolKind = rs.getString("SCHOOL_KIND");
                 final String curriculumCd = rs.getString("CURRICULUM_CD");
                 final String subclasscd = rs.getString("SUBCLASSCD");
                 final String subclassabbv = rs.getString("SUBCLASSABBV");
                 final String score = rs.getString("SCORE");
                 final String scoreHoju = rs.getString("SIDOU") != null ? rs.getString("SIDOU") : "  ";
                 final KettenKamoku kettenKamoku = new KettenKamoku(classcd, schoolKind, curriculumCd, subclasscd, subclassabbv, score, scoreHoju);
                 student.addKettenKamoku(kettenKamoku);;
             }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }
        return list;
    }
    
    private KettenStudent getKetten(final List list, final String schregno) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final KettenStudent ketten = (KettenStudent) it.next();
            if (ketten._schregno.equals(schregno)) {
                return ketten;
            }
        }
        return null;
    }

    private String getJukenSql(final EntYear entYearClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SCHOOL AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, ");
        stb.append("         ENTRANCE_DATE ");
        stb.append("     FROM ");
        stb.append("         SCHOOL_MST ");
        stb.append("     WHERE ");
        if (entYearClass._lastYearFlg) {
            stb.append("         YEAR <= '" + entYearClass._entYear + "' ");
        } else {
            stb.append("         YEAR  = '" + entYearClass._entYear + "' ");
        }
        stb.append(" ) ");
        stb.append(" , T_BASE AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         INOUTCD, ");
        stb.append("         ENT_DATE, ");
        stb.append("         VALUE(ENT_DIV,'1') AS ENT_DIV, ");
        stb.append("         GRD_DATE, ");
        stb.append("         GRD_DIV, ");
        stb.append("         FISCALYEAR(ENT_DATE) AS ENT_YEAR, ");
        stb.append("         FISCALYEAR(GRD_DATE) AS GRD_YEAR ");
        stb.append("     FROM ");
        stb.append("         SCHREG_BASE_MST ");
        stb.append("     WHERE ");
        stb.append("         VALUE(INOUTCD, '') NOT IN ('8', '9') ");
        if (entYearClass._lastYearFlg) {
            stb.append("         AND FISCALYEAR(ENT_DATE) <= '" + entYearClass._entYear + "' ");
        } else {
            stb.append("         AND FISCALYEAR(ENT_DATE)  = '" + entYearClass._entYear + "' ");
        }
        stb.append(" ) ");
        //履修科目
        stb.append(" , SUBCLASS_STD_SELECT AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         TSL.YEAR, ");
        stb.append("         TSL.SEMESTER, ");
        stb.append("         TSL.CLASSCD, ");
        stb.append("         TSL.SCHOOL_KIND, ");
        stb.append("         TSL.CURRICULUM_CD, ");
        stb.append("         TSL.SUBCLASSCD, ");
        stb.append("         TSL.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SUBCLASS_STD_SELECT_DAT TSL ");
        stb.append("     WHERE ");
        stb.append("         TSL.YEAR = '" + _param._loginYear + "' ");
        stb.append("         AND TSL.SEMESTER = '" + _param._semester + "' ");
        stb.append(" ) ");
        //本年度１科目でも履修登録された生徒
        stb.append(" , STD_SELECT AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         TSL.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SUBCLASS_STD_SELECT TSL ");
        stb.append(" ) ");
        //本年度１科目でも成績入力された生徒
        stb.append(" , RECORD AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         TSL.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SUBCLASS_STD_SELECT TSL ");
        stb.append("         INNER JOIN RECORD_DAT TRC ON TRC.YEAR = TSL.YEAR ");
        stb.append("             AND TRC.CLASSCD = TSL.CLASSCD ");
        stb.append("             AND TRC.SCHOOL_KIND = TSL.SCHOOL_KIND ");
        stb.append("             AND TRC.CURRICULUM_CD = TSL.CURRICULUM_CD ");
        stb.append("             AND TRC.SUBCLASSCD = TSL.SUBCLASSCD ");
        stb.append("             AND TRC.TAKESEMES = '0' ");
        stb.append("             AND TRC.SCHREGNO = TSL.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         TRC.SEM" + _param._semester + "_INTR_VALUE IS NOT NULL ");
        stb.append(" ) ");

        //一般生
        stb.append(" SELECT ");
        stb.append("     '02' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '04' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '06' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     (( ");
        stb.append("     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3') ");
        stb.append("     ) OR ( ");
        stb.append("     BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3') ");
        stb.append("     )) ");
        stb.append("     AND BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND BASE.GRD_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND BASE.GRD_DIV IS NOT NULL ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '13' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENT_YEAR = '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND DATE('" + _param._loginDate + "') >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '16' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     (( ");
        stb.append("     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3') ");
        stb.append("     ) OR ( ");
        stb.append("     BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3') ");
        stb.append("     )) ");
        stb.append("     AND BASE.GRD_YEAR = '" + _param._loginYear + "' ");
        stb.append("     AND DATE('" + _param._loginDate + "') > BASE.GRD_DATE ");
        stb.append("     AND BASE.GRD_DIV IS NOT NULL ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '19' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append("     INNER JOIN STD_SELECT TSL ON TSL.SCHREGNO = BASE.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     DATE('" + _param._loginDate + "') >= BASE.ENT_DATE ");
        stb.append("     AND (DATE('" + _param._loginDate + "') <  BASE.GRD_DATE OR BASE.GRD_DATE IS NULL) ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '22' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append("     INNER JOIN RECORD TRC ON TRC.SCHREGNO = BASE.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     DATE('" + _param._loginDate + "') >= BASE.ENT_DATE ");
        stb.append("     AND (DATE('" + _param._loginDate + "') <  BASE.GRD_DATE OR BASE.GRD_DATE IS NULL) ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3') ");
        //転編入生
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '03' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('4','5') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '05' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('4','5') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '07' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     (( ");
        stb.append("     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('4','5') ");
        stb.append("     ) OR ( ");
        stb.append("     BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('4','5') ");
        stb.append("     )) ");
        stb.append("     AND BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND BASE.GRD_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND BASE.GRD_DIV IS NOT NULL ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '14' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENT_YEAR = '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND DATE('" + _param._loginDate + "') >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('4','5') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '17' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     (( ");
        stb.append("     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('4','5') ");
        stb.append("     ) OR ( ");
        stb.append("     BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('4','5') ");
        stb.append("     )) ");
        stb.append("     AND BASE.GRD_YEAR = '" + _param._loginYear + "' ");
        stb.append("     AND DATE('" + _param._loginDate + "') > BASE.GRD_DATE ");
        stb.append("     AND BASE.GRD_DIV IS NOT NULL ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '20' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append("     INNER JOIN STD_SELECT TSL ON TSL.SCHREGNO = BASE.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     DATE('" + _param._loginDate + "') >= BASE.ENT_DATE ");
        stb.append("     AND (DATE('" + _param._loginDate + "') <  BASE.GRD_DATE OR BASE.GRD_DATE IS NULL) ");
        stb.append("     AND BASE.ENT_DIV in ('4','5') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '23' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append("     INNER JOIN RECORD TRC ON TRC.SCHREGNO = BASE.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     DATE('" + _param._loginDate + "') >= BASE.ENT_DATE ");
        stb.append("     AND (DATE('" + _param._loginDate + "') <  BASE.GRD_DATE OR BASE.GRD_DATE IS NULL) ");
        stb.append("     AND BASE.ENT_DIV in ('4','5') ");
        //備考
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '08-4' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('4') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '08-5' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('5') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '09-3' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     (( ");
        stb.append("     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append("     ) OR ( ");
        stb.append("     BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append("     )) ");
        stb.append("     AND BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND BASE.GRD_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND BASE.GRD_DIV in ('3') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '09-2' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     (( ");
        stb.append("     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append("     ) OR ( ");
        stb.append("     BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append("     )) ");
        stb.append("     AND BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND BASE.GRD_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND BASE.GRD_DIV in ('2') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '09-9' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     (( ");
        stb.append("     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append("     ) OR ( ");
        stb.append("     BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append("     )) ");
        stb.append("     AND BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND BASE.GRD_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND BASE.GRD_DIV in ('9') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '09-1' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     (( ");
        stb.append("     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append("     ) OR ( ");
        stb.append("     BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append("     )) ");
        stb.append("     AND BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND BASE.GRD_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND BASE.GRD_DIV in ('1') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '15-4' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENT_YEAR = '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND DATE('" + _param._loginDate + "') >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('4') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '15-5' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENT_YEAR = '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND DATE('" + _param._loginDate + "') >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('5') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '18-3' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     (( ");
        stb.append("     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append("     ) OR ( ");
        stb.append("     BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append("     )) ");
        stb.append("     AND BASE.GRD_YEAR = '" + _param._loginYear + "' ");
        stb.append("     AND DATE('" + _param._loginDate + "') > BASE.GRD_DATE ");
        stb.append("     AND BASE.GRD_DIV in ('3') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '18-2' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     (( ");
        stb.append("     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append("     ) OR ( ");
        stb.append("     BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append("     )) ");
        stb.append("     AND BASE.GRD_YEAR = '" + _param._loginYear + "' ");
        stb.append("     AND DATE('" + _param._loginDate + "') > BASE.GRD_DATE ");
        stb.append("     AND BASE.GRD_DIV in ('2') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '18-9' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     (( ");
        stb.append("     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append("     ) OR ( ");
        stb.append("     BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append("     )) ");
        stb.append("     AND BASE.GRD_YEAR = '" + _param._loginYear + "' ");
        stb.append("     AND DATE('" + _param._loginDate + "') > BASE.GRD_DATE ");
        stb.append("     AND BASE.GRD_DIV in ('9') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '18-1' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append(" WHERE ");
        stb.append("     (( ");
        stb.append("     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append("     ) OR ( ");
        stb.append("     BASE.ENT_YEAR < '" + _param._loginYear + "' ");
        stb.append("     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append("     )) ");
        stb.append("     AND BASE.GRD_YEAR = '" + _param._loginYear + "' ");
        stb.append("     AND DATE('" + _param._loginDate + "') > BASE.GRD_DATE ");
        stb.append("     AND BASE.GRD_DIV in ('1') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '21-1' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append("     INNER JOIN STD_SELECT TSL ON TSL.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_BASE_YEAR_DETAIL_MST BSYR ");
        stb.append("         ON  BSYR.SCHREGNO = BASE.SCHREGNO ");
        stb.append("         AND BSYR.YEAR = '" + _param._loginYear + "' ");
        stb.append("         AND BSYR.BASE_SEQ = '001' ");
        stb.append("         AND BSYR.BASE_REMARK1 = '1' ");
        stb.append(" WHERE ");
        stb.append("     DATE('" + _param._loginDate + "') >= BASE.ENT_DATE ");
        stb.append("     AND (DATE('" + _param._loginDate + "') <  BASE.GRD_DATE OR BASE.GRD_DATE IS NULL) ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '28-1' AS CNT_DIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHOOL SCL ");
        stb.append("     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ");
        stb.append("     INNER JOIN RECORD TRC ON TRC.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_BASE_YEAR_DETAIL_MST BSYR ");
        stb.append("         ON  BSYR.SCHREGNO = BASE.SCHREGNO ");
        stb.append("         AND BSYR.YEAR = '" + _param._loginYear + "' ");
        stb.append("         AND BSYR.BASE_SEQ = '001' ");
        stb.append("         AND BSYR.BASE_REMARK1 = '1' ");
        stb.append(" WHERE ");
        stb.append("     DATE('" + _param._loginDate + "') >= BASE.ENT_DATE ");
        stb.append("     AND (DATE('" + _param._loginDate + "') <  BASE.GRD_DATE OR BASE.GRD_DATE IS NULL) ");
        stb.append("     AND BASE.ENT_DIV in ('1','2','3','4','5') ");
        stb.append(" ORDER BY ");
        stb.append("     CNT_DIV ");
        return stb.toString();
    }

    private String getJouiAndKettenSql(final String kettenFlg, final EntYear entYearClass) {
        final StringBuffer stb = new StringBuffer();
        //履修科目
        stb.append(" WITH SUBCLASS_STD_SELECT AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         TSL.YEAR, ");
        stb.append("         TSL.SEMESTER, ");
        stb.append("         TSL.CLASSCD, ");
        stb.append("         TSL.SCHOOL_KIND, ");
        stb.append("         TSL.CURRICULUM_CD, ");
        stb.append("         TSL.SUBCLASSCD, ");
        stb.append("         TSL.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SUBCLASS_STD_SELECT_DAT TSL ");
        stb.append("     WHERE ");
        stb.append("         TSL.YEAR = '" + _param._loginYear + "' ");
        stb.append("         AND TSL.SEMESTER = '" + _param._semester + "' ");
        stb.append(" ) ");
        //成績入力された成績
        stb.append(" , RECORD AS ( ");
        stb.append("     SELECT ");
        stb.append("         TRC.CLASSCD, ");
        stb.append("         TRC.SCHOOL_KIND, ");
        stb.append("         TRC.CURRICULUM_CD, ");
        stb.append("         TRC.SUBCLASSCD, ");
        stb.append("         TRC.SCHREGNO, ");
        stb.append("         TRC.SEM" + _param._semester + "_INTR_VALUE AS SCORE, ");
        stb.append("         TRC.SEM" + _param._semester + "_TERM_VALUE AS SIDOU ");
        stb.append("     FROM ");
        stb.append("         SUBCLASS_STD_SELECT TSL ");
        stb.append("         INNER JOIN RECORD_DAT TRC ON TRC.YEAR = TSL.YEAR ");
        stb.append("             AND TRC.CLASSCD = TSL.CLASSCD ");
        stb.append("             AND TRC.SCHOOL_KIND = TSL.SCHOOL_KIND ");
        stb.append("             AND TRC.CURRICULUM_CD = TSL.CURRICULUM_CD ");
        stb.append("             AND TRC.SUBCLASSCD = TSL.SUBCLASSCD ");
        stb.append("             AND TRC.TAKESEMES = '0' ");
        stb.append("             AND TRC.SCHREGNO = TSL.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         TRC.SEM" + _param._semester + "_INTR_VALUE IS NOT NULL ");
        stb.append(" ) ");
        //成績入力された成績の平均点・科目数
        stb.append(" , T_AVG_CNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         TRC.SCHREGNO, ");
        stb.append("         DECIMAL(ROUND(AVG(FLOAT(TRC.SCORE))*10,0)/10,5,1) AS AVG, ");
        stb.append("         COUNT(TRC.SCORE) AS CNT ");
        stb.append("     FROM ");
        stb.append("         RECORD TRC ");
        stb.append("     GROUP BY ");
        stb.append("         TRC.SCHREGNO ");
        stb.append(" ) ");

        //メイン
        if ("ketten".equals(kettenFlg)) {
            stb.append(" SELECT ");
            stb.append("     TAC.SCHREGNO, ");
            stb.append("     BASE.NAME_SHOW AS NAME, ");
            stb.append("     TAC.AVG, ");
            stb.append("     TAC.CNT, ");
            stb.append("     '' AS REMARK, ");
            stb.append("     TRC.CLASSCD, ");
            stb.append("     TRC.SCHOOL_KIND, ");
            stb.append("     TRC.CURRICULUM_CD, ");
            stb.append("     TRC.SUBCLASSCD, ");
            stb.append("     TSB.SUBCLASSABBV, ");
            stb.append("     TRC.SCORE, ");
            stb.append("     TRC.SIDOU ");
            stb.append(" FROM ");
            stb.append("     T_AVG_CNT TAC ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = TAC.SCHREGNO ");
            stb.append("     INNER JOIN RECORD TRC ON TRC.SCHREGNO = TAC.SCHREGNO ");
            stb.append("     INNER JOIN SUBCLASS_MST TSB ");
            stb.append("          ON TSB.CLASSCD = TRC.CLASSCD ");
            stb.append("         AND TSB.SCHOOL_KIND = TRC.SCHOOL_KIND ");
            stb.append("         AND TSB.CURRICULUM_CD = TRC.CURRICULUM_CD ");
            stb.append("         AND TSB.SUBCLASSCD = TRC.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     VALUE(BASE.INOUTCD, '') NOT IN ('8', '9') ");
            if (entYearClass._lastYearFlg) {
                stb.append("     AND FISCALYEAR(BASE.ENT_DATE) <= '" + entYearClass._entYear + "' ");
            } else {
                stb.append("     AND FISCALYEAR(BASE.ENT_DATE)  = '" + entYearClass._entYear + "' ");
            }
            stb.append("     AND TRC.SCORE <= 39 ");
            stb.append(" ORDER BY ");
            stb.append("     SUBSTR(TAC.SCHREGNO, 1, 4) DESC, ");
            stb.append("     SUBSTR(TAC.SCHREGNO, 5, 4) ASC, ");
            stb.append("     TRC.CLASSCD, ");
            stb.append("     TRC.SCHOOL_KIND, ");
            stb.append("     TRC.CURRICULUM_CD, ");
            stb.append("     TRC.SUBCLASSCD ");
        } else {
            stb.append(" SELECT ");
            stb.append("     TAC.SCHREGNO, ");
            stb.append("     BASE.NAME_SHOW AS NAME, ");
            stb.append("     TAC.AVG, ");
            stb.append("     TAC.CNT, ");
            stb.append("     '' AS REMARK ");
            stb.append(" FROM ");
            stb.append("     T_AVG_CNT TAC ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = TAC.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     VALUE(BASE.INOUTCD, '') NOT IN ('8', '9') ");
            if (entYearClass._lastYearFlg) {
                stb.append("     AND FISCALYEAR(BASE.ENT_DATE) <= '" + entYearClass._entYear + "' ");
            } else {
                stb.append("     AND FISCALYEAR(BASE.ENT_DATE)  = '" + entYearClass._entYear + "' ");
            }
            stb.append("     AND  4 <= TAC.CNT ");
            stb.append("     AND 80 <= TAC.AVG ");
            stb.append(" ORDER BY ");
            stb.append("     SUBSTR(TAC.SCHREGNO, 1, 4) DESC, ");
            stb.append("     SUBSTR(TAC.SCHREGNO, 5, 4) ASC ");
        }
        return stb.toString();
    }

    private static class EntYear {
        final String _entYear;
        final boolean _lastYearFlg;
        EntYear(
                final String entYear,
                final boolean lastYearFlg
        ) {
            _entYear = entYear;
            _lastYearFlg = lastYearFlg;
        }
    }

    private static class Juken {
        final int _gyo;
        final String _cntIppann;
        final String _cntTenHen;
        final String _cntTotal;
        final String _remark;
        Juken(
                final int gyo,
                final String cntIppann,
                final String cntTenHen,
                final String cntTotal,
                final String remark
        ) {
            _gyo = gyo;
            _cntIppann = cntIppann;
            _cntTenHen = cntTenHen;
            _cntTotal = cntTotal;
            _remark = remark;
        }
    }
    
    private static class JouiStudent {
        final String _schregno;
        final String _name;
        final String _avg;
        final String _cnt;
        JouiStudent(
                final String schregno,
                final String name,
                final String avg,
                final String cnt
        ) {
            _schregno = schregno;
            _name = name;
            _avg = avg;
            _cnt = cnt;
        }
    }
    
    private static class KettenStudent {
        final String _schregno;
        final String _name;
        final String _avg;
        final String _cnt;
        final List _kettenKamokuList = new ArrayList();
        KettenStudent(
                final String schregno,
                final String name,
                final String avg,
                final String cnt
        ) {
            _schregno = schregno;
            _name = name;
            _avg = avg;
            _cnt = cnt;
        }
        public void addKettenKamoku(final KettenKamoku kettenKamoku) {
            _kettenKamokuList.add(kettenKamoku);
            kettenKamoku._student = this;
        }
    }
    
    private static class KettenKamoku {
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassabbv;
        final String _score;
        final String _scoreHoju;
        KettenStudent _student = null;
        KettenKamoku(
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassabbv,
                final String score,
                final String scoreHoju
        ) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassabbv = subclassabbv;
            _score = score;
            _scoreHoju = scoreHoju;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _semester;
        private final String _prgid;
        private final String _scoreField;
        private final String _loginDate;
        private final String _semesterName;
        private String _scoreFieldHoju = null;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear = request.getParameter("LOGIN_YEAR");
            _semester = request.getParameter("GAKKI");
            _prgid = request.getParameter("PRGID");
            _loginDate = request.getParameter("LOGIN_DATE");
            if ("2".equals(_semester)) {
                _scoreField = "SEM2_INTR_VALUE";
                _scoreFieldHoju = "SEM2_TERM_VALUE";
            } else {
                _scoreField = "SEM1_INTR_VALUE";
                _scoreFieldHoju = "SEM1_TERM_VALUE";
            }
            _semesterName = getSemesterName(db2);
        }
        

        public String getSemesterName(final DB2UDB db2) {
            String semesterName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT VALUE(SEMESTERNAME, '') AS SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _loginYear + "' AND SEMESTER = '" + _semester + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    semesterName = rs.getString("SEMESTERNAME");
                }
            } catch (Exception ex) {
                 log.fatal("exception!", ex);
            } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
            }
            return semesterName;
        }
    }
}

// eof

