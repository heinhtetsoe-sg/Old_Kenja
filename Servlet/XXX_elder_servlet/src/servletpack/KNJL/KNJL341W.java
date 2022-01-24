/*
 * $Id: f733556e3b52edf281d516a04883c72b2f129f6d $
 *
 * 作成日: 2017/11/20
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

public class KNJL341W {

    private static final Log log = LogFactory.getLog(KNJL341W.class);

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
        svf.VrSetForm("KNJL341W.frm", 4);
        final List printMieList    = getMiekenList(db2);
        final List printKengaiList = getKengaiList(db2);
        final List printTotalList  = getTotalList(db2);
        int printLine = 1;
        int gpCd =1;
        String befKengaiCtiyCd = "";
        String befCtiyCd = "";

        setTitle(svf);//タイトル
        for (Iterator iterator = _param._cousreMajorList.iterator(); iterator.hasNext();) {
            final PrintCmData cmData = (PrintCmData) iterator.next();
            svf.VrsOutn("MAJOR_NAME1", printLine, cmData._cmName);//学科・コース名

            printLine++;
            _hasData = true;
        }

        //三重県内データ
        for (Iterator iterator = printMieList.iterator(); iterator.hasNext();) {
            final PrintMainData kennaiData = (PrintMainData) iterator.next();
            if (!"".equals(befCtiyCd) && !befCtiyCd.equals(kennaiData._cityName)) {
                gpCd++;
            }
            svf.VrsOut("GRP", String.valueOf(gpCd));
            if (!befCtiyCd.equals(kennaiData._cityName)) {
                final String citySizeNo = KNJ_EditEdit.getMS932ByteLength(kennaiData._cityName) > 10 ? "2" : "1";
                svf.VrsOut("CITY_NAME" + citySizeNo, kennaiData._cityName);//市町名
            }
            final String finSizeNo = KNJ_EditEdit.getMS932ByteLength(kennaiData._finSchoolName) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(kennaiData._finSchoolName) > 20 ? "2" : "1";
            svf.VrsOut("FINSCHOOL_NAME1_" + finSizeNo, kennaiData._finSchoolName);//中学校名
            int colCnt =1;
            for (Iterator iterator2 = _param._cousreMajorList.iterator(); iterator2.hasNext();) {
                final PrintCmData cmData = (PrintCmData) iterator2.next();
                final EntCntClass entCntClass = (EntCntClass) kennaiData._entCntMap.get(cmData._cmCd);
                svf.VrsOut("ENT1_" + colCnt + "_1", entCntClass._entCnt);//コース毎の入学者数
                svf.VrsOut("ENT1_" + colCnt + "_2", entCntClass._grdDivCnt);//コース毎の（内）過年度
                colCnt++;
            }
            svf.VrsOut("ENT1_9_1", kennaiData._allEntCnt);//合計、総入学者数
            svf.VrsOut("ENT1_9_2", kennaiData._allGrdDivCnt);//合計、（内）過年度

            befCtiyCd = kennaiData._cityName;
            svf.VrEndRecord();
            _hasData = true;
        }
        //三重県外データ
        for (Iterator iterator = printKengaiList.iterator(); iterator.hasNext();) {
            final PrintMainData kengaiData = (PrintMainData) iterator.next();
            svf.VrsOut("GRP", "99");
            if (!befKengaiCtiyCd.equals(kengaiData._cityName)) {
                final String citySizeNo = KNJ_EditEdit.getMS932ByteLength(kengaiData._cityName) > 10 ? "2" : "1";
                svf.VrsOut("CITY_NAME" + citySizeNo, kengaiData._cityName);//"県外"固定
            }
            final String finSizeNo = KNJ_EditEdit.getMS932ByteLength(kengaiData._finSchoolName) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(kengaiData._finSchoolName) > 20 ? "2" : "1";
            svf.VrsOut("FINSCHOOL_NAME1_" + finSizeNo, kengaiData._finSchoolName);//都道府県名
            int colCnt =1;
            for (Iterator iterator2 = _param._cousreMajorList.iterator(); iterator2.hasNext();) {
                final PrintCmData cmData = (PrintCmData) iterator2.next();
                final EntCntClass entCntClass = (EntCntClass) kengaiData._entCntMap.get(cmData._cmCd);
                svf.VrsOut("ENT1_" + colCnt + "_1", entCntClass._entCnt);//コース毎の入学者数
                svf.VrsOut("ENT1_" + colCnt + "_2", entCntClass._grdDivCnt);//コース毎の（内）過年度
                colCnt++;
            }
            svf.VrsOut("ENT1_9_1", kengaiData._allEntCnt);//合計、総入学者数
            svf.VrsOut("ENT1_9_2", kengaiData._allGrdDivCnt);//合計、（内）過年度

            befKengaiCtiyCd = kengaiData._cityName;
            svf.VrEndRecord();
            _hasData = true;
        }
        //合計データ
        for (Iterator iterator = printTotalList.iterator(); iterator.hasNext();) {
            final PrintMainData totalData = (PrintMainData) iterator.next();
            final String subNameSizeNo = KNJ_EditEdit.getMS932ByteLength(totalData._finSchoolName) > 20 ? "2" : "1";
            svf.VrsOut("SUBTOTAL_NAME" + subNameSizeNo, totalData._finSchoolName);//合計
            int colCnt =1;
            for (Iterator iterator2 = _param._cousreMajorList.iterator(); iterator2.hasNext();) {
                final PrintCmData cmData = (PrintCmData) iterator2.next();
                final EntCntClass entCntClass = (EntCntClass) totalData._entCntMap.get(cmData._cmCd);
                svf.VrsOut("ENT2_" + colCnt + "_1", entCntClass._entCnt);//コース毎の入学者数合計
                svf.VrsOut("ENT2_" + colCnt + "_2", entCntClass._grdDivCnt);//コース毎の（内）過年度合計
                colCnt++;
            }
            svf.VrsOut("ENT2_9_1", totalData._allEntCnt);//合計、総入学者数
            svf.VrsOut("ENT2_9_2", totalData._allGrdDivCnt);//合計、（内）過年度

            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private void setTitle(final Vrw32alp svf) {
        String setYear = KNJ_EditDate.h_format_JP_N(_param._entExamYear + "/04/01");
        svf.VrsOut("TITLE", setYear + "度　" + "三重県立高等学校入学者調べ");
        svf.VrsOut("AREA", _param._areaName);
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
        svf.VrsOut("COURSE", _param._courseName);
    }

    private List getMiekenList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getMieKennaiSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String cityName      = rs.getString("CITYNAME");
                final String finSchoolName = rs.getString("FINSCHOOL_NAME");
                final Map entCntMap = new HashMap();
                int cnt = 1;
                for (Iterator iterator = _param._cousreMajorList.iterator(); iterator.hasNext();) {
                    final PrintCmData cmData = (PrintCmData) iterator.next();
                    final String entCnt      = rs.getString("ENTCNT"+ cnt);
                    final String grdDivCnt   = rs.getString("GRDDIVCNT"+ cnt);
                    final EntCntClass entCntClass = new EntCntClass(entCnt, grdDivCnt);
                    entCntMap.put(cmData._cmCd, entCntClass);
                    cnt++;
                }
                final String allEntCnt     = rs.getString("ALLENTCNT");
                final String allGrdDivCnt  = rs.getString("ALLGRDDIVCNT");

                final PrintMainData printMainData = new PrintMainData(cityName, finSchoolName, entCntMap, allEntCnt, allGrdDivCnt);
                retList.add(printMainData);

            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    //三重県内データ
    private String getMieKennaiSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH BASEDATA AS ( ");
        stb.append("                     SELECT ");
        stb.append("                         B1.FS_CD ");
        stb.append("                     FROM ");
        stb.append("                         ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("                         INNER JOIN NAME_MST N1 ");
        stb.append("                                              ON N1.NAMECD1   = 'L013' ");
        stb.append("                                             AND B1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                         LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ");
        stb.append("                         LEFT JOIN PREF_MST P1      ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ");
        stb.append("                     WHERE ");
        stb.append("                             B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                         AND B1.JUDGEMENT   != '5' ");
        stb.append("                         AND B1.ENTDIV       = '1' ");
        stb.append("                         AND P1.PREF_CD      = '24' ");
        stb.append("                         AND N1.NAMESPARE1   = '1' ");
        stb.append("                     GROUP BY ");
        stb.append("                         B1.FS_CD ");
        int cnt = 1;
        for (Iterator iterator = _param._cousreMajorList.iterator(); iterator.hasNext();) {
            final PrintCmData cmData = (PrintCmData) iterator.next();
            stb.append(" ), FINSCHCNT" + cnt + " AS ( ");
            stb.append("                     SELECT ");
            stb.append("                         B1.FS_CD, ");
            stb.append("                         COUNT(*) AS ENTCNT" + cnt + " ");
            stb.append("                     FROM ");
            stb.append("                         ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("                         INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
            stb.append("                                               AND B1.JUDGEMENT = N1.NAMECD2 ");
            stb.append("                         LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ");
            stb.append("                         LEFT JOIN PREF_MST P1      ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ");
            stb.append("                     WHERE ");
            stb.append("                             B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
            stb.append("                         AND B1.JUDGEMENT   != '5' ");
            stb.append("                         AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE = '" + cmData._cmCd + "' ");
            stb.append("                         AND B1.ENTDIV       = '1' ");
            stb.append("                         AND P1.PREF_CD      = '24' ");
            stb.append("                         AND N1.NAMESPARE1   = '1' ");
            stb.append("                     GROUP BY ");
            stb.append("                         B1.FS_CD ");
            stb.append(" ), KANENDOCNT" + cnt + " AS ( ");
            stb.append("                     SELECT ");
            stb.append("                         B1.FS_CD, ");
            stb.append("                         COUNT(*) AS GRDDIVCNT" + cnt + " ");
            stb.append("                     FROM ");
            stb.append("                         ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("                         INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
            stb.append("                                               AND B1.JUDGEMENT = N1.NAMECD2 ");
            stb.append("                         LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ");
            stb.append("                         LEFT JOIN PREF_MST P1      ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ");
            stb.append("                     WHERE ");
            stb.append("                             B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
            stb.append("                         AND B1.JUDGEMENT   != '5' ");
            stb.append("                         AND B1.ENTDIV       = '1' ");
            stb.append("                         AND B1.FS_GRDDIV    = '2' ");
            stb.append("                         AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE = '" + cmData._cmCd + "' ");
            stb.append("                         AND P1.PREF_CD      = '24' ");
            stb.append("                         AND N1.NAMESPARE1   = '1' ");
            stb.append("                     GROUP BY ");
            stb.append("                         B1.FS_CD ");
            cnt++;
        }
        stb.append(" ), ALLFINSCHCNT AS ( ");
        stb.append("                     SELECT ");
        stb.append("                         B1.FS_CD, ");
        stb.append("                         COUNT(*) AS ALLENTCNT ");
        stb.append("                     FROM ");
        stb.append("                         ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("                         INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
        stb.append("                                               AND B1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                         LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ");
        stb.append("                         LEFT JOIN PREF_MST P1      ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ");
        stb.append("                     WHERE ");
        stb.append("                             B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                         AND B1.JUDGEMENT   != '5' ");
        stb.append("                         AND B1.ENTDIV       = '1' ");
        stb.append("                         AND P1.PREF_CD      = '24' ");
        stb.append("                         AND N1.NAMESPARE1   = '1' ");
        stb.append("                     GROUP BY ");
        stb.append("                         B1.FS_CD ");
        stb.append(" ), ALLKANENDOCNT AS ( ");
        stb.append("                     SELECT ");
        stb.append("                         B1.FS_CD, ");
        stb.append("                         COUNT(*) AS ALLGRDDIVCNT ");
        stb.append("                     FROM ");
        stb.append("                         ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("                         INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
        stb.append("                                               AND B1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                         LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ");
        stb.append("                         LEFT JOIN PREF_MST P1      ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ");
        stb.append("                     WHERE ");
        stb.append("                             B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                         AND B1.JUDGEMENT   != '5' ");
        stb.append("                         AND B1.ENTDIV       = '1' ");
        stb.append("                         AND B1.FS_GRDDIV    = '2' ");
        stb.append("                         AND P1.PREF_CD      = '24' ");
        stb.append("                         AND N1.NAMESPARE1   = '1' ");
        stb.append("                     GROUP BY ");
        stb.append("                         B1.FS_CD ");
        stb.append(" ) ");
        stb.append(" SELECT  ");
        stb.append("     Z003.NAME1 AS CITYNAME, ");
        stb.append("     F1.FINSCHOOL_NAME, ");
        cnt = 1;
        for (Iterator iterator = _param._cousreMajorList.iterator(); iterator.hasNext();) {
            iterator.next();
            stb.append("     FCNT" + cnt + ".ENTCNT" + cnt + ", ");
            stb.append("     KCNT" + cnt + ".GRDDIVCNT" + cnt + ", ");
            cnt++;
        }
        stb.append("     ALLFC.ALLENTCNT, ");
        stb.append("     ALLKC.ALLGRDDIVCNT ");
        stb.append(" FROM ");
        stb.append("     BASEDATA B1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST F1 ");
        stb.append("                          ON B1.FS_CD = F1.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST Z003 ");
        stb.append("                          ON Z003.NAMECD1  = 'Z003' ");
        stb.append("                         AND F1.DISTRICTCD = Z003.NAMECD2 ");
        cnt = 1;
        for (Iterator iterator = _param._cousreMajorList.iterator(); iterator.hasNext();) {
            iterator.next();
            stb.append("     LEFT JOIN FINSCHCNT" + cnt + " FCNT" + cnt + " ");
            stb.append("                          ON B1.FS_CD = FCNT" + cnt + ".FS_CD ");
            stb.append("     LEFT JOIN KANENDOCNT" + cnt + " KCNT" + cnt + " ");
            stb.append("                          ON B1.FS_CD = KCNT" + cnt + ".FS_CD ");
            cnt++;
        }
        stb.append("     LEFT JOIN ALLFINSCHCNT ALLFC ");
        stb.append("                          ON B1.FS_CD = ALLFC.FS_CD ");
        stb.append("     LEFT JOIN ALLKANENDOCNT ALLKC ");
        stb.append("                          ON B1.FS_CD = ALLKC.FS_CD ");
        stb.append(" ORDER BY ");
        stb.append("     B1.FS_CD ");

        return  stb.toString();
    }

    private List getKengaiList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getKengaiSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String cityName      = "県外";
                final String finSchoolName = rs.getString("PREF_NAME");
                final Map entCntMap = new HashMap();
                int cnt = 1;
                for (Iterator iterator = _param._cousreMajorList.iterator(); iterator.hasNext();) {
                    final PrintCmData cmData = (PrintCmData) iterator.next();
                    final String entCnt      = rs.getString("NOTMCNT"+ cnt);
                    final String grdDivCnt   = rs.getString("KANENDOCNT"+ cnt);
                    final EntCntClass entCntClass = new EntCntClass(entCnt, grdDivCnt);
                    entCntMap.put(cmData._cmCd, entCntClass);
                    cnt++;
                }
                final String allEntCnt     = rs.getString("NOTMCNTALL");
                final String allGrdDivCnt  = rs.getString("KANENDOCNTALL");

                final PrintMainData printKengaiData = new PrintMainData(cityName, finSchoolName, entCntMap, allEntCnt, allGrdDivCnt);
                retList.add(printKengaiData);

            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    //三重県外データ
    private String getKengaiSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH  B_ALL(PREF_CD, PREF_NAME) AS ( ");
        stb.append("             VALUES('99', '県外合計') ");
        stb.append(" ), BASEDATA AS ( ");
        stb.append("                 SELECT ");
        stb.append("                     P1.PREF_CD, ");
        stb.append("                     P1.PREF_NAME ");
        stb.append("                 FROM ");
        stb.append("                     ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
        stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ");
        stb.append("                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD  ");
        stb.append("                 WHERE ");
        stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND B1.JUDGEMENT   != '5' ");
        stb.append("                     AND B1.ENTDIV       = '1' ");
        stb.append("                     AND P1.PREF_CD     != '24' ");
        stb.append("                     AND N1.NAMESPARE1   = '1' ");
        stb.append("                 GROUP BY ");
        stb.append("                     P1.PREF_CD, ");
        stb.append("                     P1.PREF_NAME ");
        stb.append("                 UNION ");
        stb.append("                 SELECT ");
        stb.append("                     PREF_CD, ");
        stb.append("                     PREF_NAME ");
        stb.append("                 FROM ");
        stb.append("                     B_ALL ");
        int cnt = 1;
        for (Iterator iterator = _param._cousreMajorList.iterator(); iterator.hasNext();) {
            final PrintCmData cmData = (PrintCmData) iterator.next();
            stb.append(" ), NOTMIECNT" + cnt + " AS ( ");
            stb.append("                 SELECT ");
            stb.append("                     P1.PREF_CD, ");
            stb.append("                     COUNT(*) AS NOTMCNT" + cnt + " ");
            stb.append("                 FROM ");
            stb.append("                     ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
            stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
            stb.append("                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ");
            stb.append("                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ");
            stb.append("                 WHERE ");
            stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
            stb.append("                     AND B1.JUDGEMENT   != '5' ");
            stb.append("                     AND B1.ENTDIV       = '1' ");
            stb.append("                     AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE = '" + cmData._cmCd + "' ");
            stb.append("                     AND P1.PREF_CD     != '24' ");
            stb.append("                     AND N1.NAMESPARE1   = '1' ");
            stb.append("                 GROUP BY ");
            stb.append("                     P1.PREF_CD ");
            stb.append(" ), NMKANENCNT" + cnt + " AS ( ");
            stb.append("                 SELECT ");
            stb.append("                     P1.PREF_CD, ");
            stb.append("                     COUNT(*) AS KANENDOCNT" + cnt + " ");
            stb.append("                 FROM ");
            stb.append("                     ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
            stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
            stb.append("                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ");
            stb.append("                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ");
            stb.append("                 WHERE ");
            stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
            stb.append("                     AND B1.JUDGEMENT   != '5' ");
            stb.append("                     AND B1.FS_GRDDIV    = '2' ");
            stb.append("                     AND B1.ENTDIV       = '1' ");
            stb.append("                     AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE = '" + cmData._cmCd + "' ");
            stb.append("                     AND P1.PREF_CD     != '24' ");
            stb.append("                     AND N1.NAMESPARE1   = '1' ");
            stb.append("                 GROUP BY ");
            stb.append("                     P1.PREF_CD ");
            stb.append(" ), CM_NOTMIE_ALL" + cnt + " AS ( ");
            stb.append("                 SELECT ");
            stb.append("                     '99' AS PREF_CD, ");
            stb.append("                     COUNT(*) AS CM_NM_ALL" + cnt + " ");
            stb.append("                 FROM ");
            stb.append("                     ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
            stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
            stb.append("                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ");
            stb.append("                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ");
            stb.append("                 WHERE ");
            stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
            stb.append("                     AND B1.JUDGEMENT   != '5' ");
            stb.append("                     AND B1.ENTDIV       = '1' ");
            stb.append("                     AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE = '" + cmData._cmCd + "' ");
            stb.append("                     AND P1.PREF_CD     != '24' ");
            stb.append("                     AND N1.NAMESPARE1   = '1' ");
            stb.append(" ), CM_KNOTMIE_ALL" + cnt + " AS ( ");//県外合計過年度（学科・コース別）
            stb.append("                 SELECT ");
            stb.append("                     '99' AS PREF_CD, ");
            stb.append("                     COUNT(*) AS CM_KNM_ALL" + cnt + " ");
            stb.append("                 FROM ");
            stb.append("                     ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
            stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
            stb.append("                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ");
            stb.append("                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ");
            stb.append("                 WHERE ");
            stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
            stb.append("                     AND B1.JUDGEMENT   != '5' ");
            stb.append("                     AND B1.ENTDIV       = '1' ");
            stb.append("                     AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE = '" + cmData._cmCd + "' ");
            stb.append("                     AND B1.FS_GRDDIV    = '2' ");
            stb.append("                     AND P1.PREF_CD     != '24' ");
            stb.append("                     AND N1.NAMESPARE1   = '1' ");
            cnt++;
        }
        stb.append(" ), PR_NOTMIE_ALL AS ( ");
        stb.append("                 SELECT ");
        stb.append("                     P1.PREF_CD, ");
        stb.append("                     COUNT(*) AS P_CNT_ALL ");
        stb.append("                 FROM ");
        stb.append("                     ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
        stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ");
        stb.append("                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ");
        stb.append("                 WHERE ");
        stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND B1.JUDGEMENT   != '5' ");
        stb.append("                     AND B1.ENTDIV       = '1' ");
        stb.append("                     AND P1.PREF_CD     != '24' ");
        stb.append("                     AND N1.NAMESPARE1   = '1' ");
        stb.append("                 GROUP BY ");
        stb.append("                     P1.PREF_CD ");
        stb.append(" ), PR_KNOTMIE_ALL AS ( ");
        stb.append("                 SELECT ");
        stb.append("                     P1.PREF_CD, ");
        stb.append("                     COUNT(*) AS P_KCNT_ALL ");
        stb.append("                 FROM ");
        stb.append("                     ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
        stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ");
        stb.append("                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ");
        stb.append("                 WHERE ");
        stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND B1.JUDGEMENT   != '5' ");
        stb.append("                     AND B1.ENTDIV       = '1' ");
        stb.append("                     AND B1.FS_GRDDIV    = '2' ");
        stb.append("                     AND P1.PREF_CD     != '24' ");
        stb.append("                     AND N1.NAMESPARE1   = '1' ");
        stb.append("                 GROUP BY ");
        stb.append("                     P1.PREF_CD ");
        stb.append(" ), NOTMIECNTALL AS ( ");
        stb.append("                 SELECT ");
        stb.append("                     '99' AS PREF_CD, ");
        stb.append("                     COUNT(*) AS NOTMCNTALL ");
        stb.append("                 FROM ");
        stb.append("                     ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
        stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ");
        stb.append("                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ");
        stb.append("                 WHERE ");
        stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND B1.JUDGEMENT   != '5' ");
        stb.append("                     AND B1.ENTDIV       = '1' ");
        stb.append("                     AND P1.PREF_CD     != '24' ");
        stb.append("                     AND N1.NAMESPARE1   = '1' ");
        stb.append(" ), NMKANENCNTALL AS ( ");
        stb.append("                 SELECT ");
        stb.append("                     '99' AS PREF_CD, ");
        stb.append("                     COUNT(*) AS KANENDOALL ");
        stb.append("                 FROM ");
        stb.append("                     ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
        stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ");
        stb.append("                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ");
        stb.append("                 WHERE ");
        stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND B1.JUDGEMENT   != '5' ");
        stb.append("                     AND B1.ENTDIV       = '1' ");
        stb.append("                     AND B1.FS_GRDDIV    = '2' ");
        stb.append("                     AND P1.PREF_CD     != '24' ");
        stb.append("                     AND N1.NAMESPARE1   = '1' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     B1.PREF_NAME, ");
        cnt = 1;
        for (Iterator iterator = _param._cousreMajorList.iterator(); iterator.hasNext();) {
            iterator.next();
            stb.append("     CASE WHEN B1.PREF_CD = '99' THEN CMNA" + cnt + ".CM_NM_ALL" + cnt + "  ELSE NMC" + cnt + ".NOTMCNT" + cnt + " END AS NOTMCNT" + cnt + ", ");
            stb.append("     CASE WHEN B1.PREF_CD = '99' THEN CMKA" + cnt + ".CM_KNM_ALL" + cnt + " ELSE KANEN" + cnt + ".KANENDOCNT" + cnt + " END AS KANENDOCNT" + cnt + ", ");
            cnt++;
        }
        stb.append("     CASE WHEN B1.PREF_CD = '99' THEN NMCA.NOTMCNTALL ELSE P_CA.P_CNT_ALL END AS NOTMCNTALL, ");
        stb.append("     CASE WHEN B1.PREF_CD = '99' THEN KALL.KANENDOALL ELSE P_KCA.P_KCNT_ALL END AS KANENDOCNTALL ");
        stb.append(" FROM ");
        stb.append("     BASEDATA B1 ");
        cnt = 1;
        for (Iterator iterator = _param._cousreMajorList.iterator(); iterator.hasNext();) {
            iterator.next();
            stb.append("     LEFT JOIN NOTMIECNT" + cnt + "       NMC" + cnt + " ON B1.PREF_CD = NMC" + cnt + ".PREF_CD ");
            stb.append("     LEFT JOIN NMKANENCNT" + cnt + "    KANEN" + cnt + " ON B1.PREF_CD = KANEN" + cnt + ".PREF_CD ");
            stb.append("     LEFT JOIN CM_NOTMIE_ALL" + cnt + "  CMNA" + cnt + " ON B1.PREF_CD = CMNA" + cnt + ".PREF_CD ");
            stb.append("     LEFT JOIN CM_KNOTMIE_ALL" + cnt + " CMKA" + cnt + " ON B1.PREF_CD = CMKA" + cnt + ".PREF_CD ");
            cnt++;
        }
        stb.append("     LEFT JOIN NOTMIECNTALL  NMCA ON B1.PREF_CD = NMCA.PREF_CD ");
        stb.append("     LEFT JOIN NMKANENCNTALL KALL ON B1.PREF_CD = KALL.PREF_CD ");
        stb.append("     LEFT JOIN PR_NOTMIE_ALL  P_CA  ON B1.PREF_CD = P_CA.PREF_CD ");
        stb.append("     LEFT JOIN PR_KNOTMIE_ALL P_KCA ON B1.PREF_CD = P_KCA.PREF_CD ");
        stb.append(" ORDER BY ");
        stb.append("     B1.PREF_CD ");

        return  stb.toString();
    }

    private List getTotalList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getTotalSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String cityName      = "";
                final String finSchoolName = rs.getString("SORT");
                final Map entCntMap = new HashMap();
                int cnt = 1;
                for (Iterator iterator = _param._cousreMajorList.iterator(); iterator.hasNext();) {
                    final PrintCmData cmData = (PrintCmData) iterator.next();
                    final String entCnt      = rs.getString("CM_ALLCNT"+ cnt);
                    final String grdDivCnt   = rs.getString("CM_KANEN_CNT"+ cnt);
                    final EntCntClass entCntClass = new EntCntClass(entCnt, grdDivCnt);
                    entCntMap.put(cmData._cmCd, entCntClass);
                    cnt++;
                }
                final String allEntCnt     = rs.getString("ENT_ALL_CNT");
                final String allGrdDivCnt  = rs.getString("ENT_K_ALL_CNT");

                final PrintMainData printKengaiData = new PrintMainData(cityName, finSchoolName, entCntMap, allEntCnt, allGrdDivCnt);
                retList.add(printKengaiData);

            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    //合計データ
    private String getTotalSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH  BASE_ALL(ENTEXAMYEAR) AS ( ");
        stb.append("             VALUES('" + _param._entExamYear + "') ");
        int cnt = 1;
        for (Iterator iterator = _param._cousreMajorList.iterator(); iterator.hasNext();) {
            final PrintCmData cmData = (PrintCmData) iterator.next();
            stb.append(" ), CM_ALL" + cnt + " AS ( ");
            stb.append("                 SELECT ");
            stb.append("                     B1.ENTEXAMYEAR, ");
            stb.append("                     COUNT(*) AS CM_ALLCNT" + cnt + " ");
            stb.append("                 FROM ");
            stb.append("                     ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
            stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
            stb.append("                 WHERE ");
            stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
            stb.append("                     AND B1.JUDGEMENT   != '5' ");
            stb.append("                     AND B1.ENTDIV       = '1' ");
            stb.append("                     AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE = '" + cmData._cmCd + "' ");
            stb.append("                     AND N1.NAMESPARE1   = '1' ");
            stb.append("                 GROUP BY ");
            stb.append("                     B1.ENTEXAMYEAR ");
            stb.append(" ), CM_KANEN_ALL" + cnt + " AS ( ");
            stb.append("                 SELECT ");
            stb.append("                     B1.ENTEXAMYEAR, ");
            stb.append("                     COUNT(*) AS CM_KANEN_CNT" + cnt + " ");
            stb.append("                 FROM ");
            stb.append("                     ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
            stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
            stb.append("                 WHERE ");
            stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
            stb.append("                     AND B1.JUDGEMENT   != '5' ");
            stb.append("                     AND B1.ENTDIV       = '1' ");
            stb.append("                     AND B1.FS_GRDDIV    = '2' ");
            stb.append("                     AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE = '" + cmData._cmCd + "' ");
            stb.append("                     AND N1.NAMESPARE1   = '1' ");
            stb.append("                 GROUP BY ");
            stb.append("                     B1.ENTEXAMYEAR ");
            cnt++;
        }
        stb.append(" ), ENT_ALL AS ( ");
        stb.append("                 SELECT ");
        stb.append("                     B1.ENTEXAMYEAR, ");
        stb.append("                     COUNT(*) AS ENT_ALL_CNT ");
        stb.append("                 FROM ");
        stb.append("                     ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
        stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                 WHERE ");
        stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND B1.JUDGEMENT   != '5' ");
        stb.append("                     AND B1.ENTDIV       = '1' ");
        stb.append("                     AND N1.NAMESPARE1   = '1' ");
        stb.append("                 GROUP BY ");
        stb.append("                     B1.ENTEXAMYEAR ");
        stb.append(" ), ENT_KANEN_ALL AS ( ");
        stb.append("                 SELECT ");
        stb.append("                     B1.ENTEXAMYEAR, ");
        stb.append("                     COUNT(*) AS ENT_K_ALL_CNT ");
        stb.append("                 FROM ");
        stb.append("                     ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
        stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                 WHERE ");
        stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND B1.JUDGEMENT   != '5' ");
        stb.append("                     AND B1.ENTDIV       = '1' ");
        stb.append("                     AND B1.FS_GRDDIV    = '2' ");
        stb.append("                     AND N1.NAMESPARE1   = '1' ");
        stb.append("                 GROUP BY ");
        stb.append("                     B1.ENTEXAMYEAR ");
        cnt = 1;
        for (Iterator iterator = _param._cousreMajorList.iterator(); iterator.hasNext();) {
            final PrintCmData cmData = (PrintCmData) iterator.next();
            stb.append(" ), JCM_ALL" + cnt + " AS ( ");
            stb.append("                 SELECT ");
            stb.append("                     B1.ENTEXAMYEAR, ");
            stb.append("                     COUNT(*) AS JCM_ALLCNT" + cnt + " ");
            stb.append("                 FROM ");
            stb.append("                     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
            stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
            stb.append("                 WHERE ");
            stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
            stb.append("                     AND B1.JUDGEMENT    = '2' ");
            stb.append("                     AND B1.DAI1_COURSECD || B1.DAI1_MAJORCD || B1.DAI1_COURSECODE = '" + cmData._cmCd + "' ");
            stb.append("                     AND B1.ENTDIV       = '2' ");
            stb.append("                 GROUP BY ");
            stb.append("                     B1.ENTEXAMYEAR ");
            stb.append(" ), JCM_KANEN_ALL" + cnt + " AS ( ");
            stb.append("                 SELECT ");
            stb.append("                     B1.ENTEXAMYEAR, ");
            stb.append("                     COUNT(*) AS JCM_KANEN_CNT" + cnt + " ");
            stb.append("                 FROM ");
            stb.append("                     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
            stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
            stb.append("                 WHERE ");
            stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
            stb.append("                     AND B1.JUDGEMENT    = '2' ");
            stb.append("                     AND B1.FS_GRDDIV    = '2' ");
            stb.append("                     AND B1.DAI1_COURSECD || B1.DAI1_MAJORCD || B1.DAI1_COURSECODE = '" + cmData._cmCd + "' ");
            stb.append("                     AND B1.ENTDIV       = '2' ");
            stb.append("                 GROUP BY ");
            stb.append("                     B1.ENTEXAMYEAR ");
            cnt++;
        }
        stb.append(" ), JENT_ALL AS ( ");
        stb.append("                 SELECT ");
        stb.append("                     B1.ENTEXAMYEAR, ");
        stb.append("                     COUNT(*) AS JENT_ALL_CNT ");
        stb.append("                 FROM ");
        stb.append("                     ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
        stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                 WHERE ");
        stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND B1.JUDGEMENT    = '2' ");
        stb.append("                     AND B1.ENTDIV       = '2' ");
        stb.append("                 GROUP BY ");
        stb.append("                     B1.ENTEXAMYEAR ");
        stb.append(" ), JENT_KANEN_ALL AS ( ");
        stb.append("                 SELECT ");
        stb.append("                     B1.ENTEXAMYEAR, ");
        stb.append("                     COUNT(*) AS JENT_K_ALL_CNT ");
        stb.append("                 FROM ");
        stb.append("                     ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ");
        stb.append("                                           AND B1.JUDGEMENT = N1.NAMECD2 ");
        stb.append("                 WHERE ");
        stb.append("                         B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("                     AND B1.JUDGEMENT    = '2' ");
        stb.append("                     AND B1.FS_GRDDIV    = '2' ");
        stb.append("                     AND B1.ENTDIV       = '2' ");
        stb.append("                 GROUP BY ");
        stb.append("                     B1.ENTEXAMYEAR ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     'A 入学者 合計' AS SORT, ");
        cnt = 1;
        for (Iterator iterator = _param._cousreMajorList.iterator(); iterator.hasNext();) {
            iterator.next();
            stb.append("     CASE WHEN CM_A" + cnt + ".CM_ALLCNT" + cnt + " IS NULL THEN 0 ELSE CM_A" + cnt + ".CM_ALLCNT" + cnt + " END AS CM_ALLCNT" + cnt + ", ");
            stb.append("     CASE WHEN CM_K" + cnt + ".CM_KANEN_CNT" + cnt + " IS NULL THEN 0 ELSE CM_K" + cnt + ".CM_KANEN_CNT" + cnt + " END AS CM_KANEN_CNT" + cnt + ", ");
            cnt++;
        }
        stb.append("     CASE WHEN EALL.ENT_ALL_CNT IS NULL THEN 0 ELSE EALL.ENT_ALL_CNT END AS ENT_ALL_CNT, ");
        stb.append("     CASE WHEN KALL.ENT_K_ALL_CNT IS NULL THEN 0 ELSE KALL.ENT_K_ALL_CNT END AS ENT_K_ALL_CNT ");
        stb.append(" FROM ");
        stb.append("     BASE_ALL B1 ");
        cnt = 1;
        for (Iterator iterator = _param._cousreMajorList.iterator(); iterator.hasNext();) {
            iterator.next();
            stb.append("     LEFT JOIN CM_ALL" + cnt + "        CM_A" + cnt + " ON B1.ENTEXAMYEAR = CM_A" + cnt + ".ENTEXAMYEAR ");
            stb.append("     LEFT JOIN CM_KANEN_ALL" + cnt + "  CM_K" + cnt + " ON B1.ENTEXAMYEAR = CM_K" + cnt + ".ENTEXAMYEAR ");
            cnt++;
        }
        stb.append("     LEFT JOIN ENT_ALL       EALL ON B1.ENTEXAMYEAR = EALL.ENTEXAMYEAR ");
        stb.append("     LEFT JOIN ENT_KANEN_ALL KALL ON B1.ENTEXAMYEAR = KALL.ENTEXAMYEAR ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     'B 合格後の辞退者数' AS SORT, ");
        cnt = 1;
        for (Iterator iterator = _param._cousreMajorList.iterator(); iterator.hasNext();) {
            iterator.next();
            stb.append("     CASE WHEN JCM_A" + cnt + ".JCM_ALLCNT" + cnt + " IS NULL THEN 0 ELSE JCM_A" + cnt + ".JCM_ALLCNT" + cnt + " END AS CM_ALLCNT" + cnt + ", ");
            stb.append("     CASE WHEN JCM_K" + cnt + ".JCM_KANEN_CNT" + cnt + " IS NULL THEN 0 ELSE JCM_K" + cnt + ".JCM_KANEN_CNT" + cnt + " END AS CM_KANEN_CNT" + cnt + ", ");
            cnt++;
        }
        stb.append("     CASE WHEN JEALL.JENT_ALL_CNT IS NULL THEN 0 ELSE JEALL.JENT_ALL_CNT END AS ENT_ALL_CNT, ");
        stb.append("     CASE WHEN JKALL.JENT_K_ALL_CNT IS NULL THEN 0 ELSE JKALL.JENT_K_ALL_CNT END AS ENT_K_ALL_CNT ");
        stb.append(" FROM ");
        stb.append("     BASE_ALL B1 ");
        cnt = 1;
        for (Iterator iterator = _param._cousreMajorList.iterator(); iterator.hasNext();) {
            iterator.next();
            stb.append("     LEFT JOIN JCM_ALL" + cnt + "        JCM_A" + cnt + " ON B1.ENTEXAMYEAR = JCM_A" + cnt + ".ENTEXAMYEAR ");
            stb.append("     LEFT JOIN JCM_KANEN_ALL" + cnt + "  JCM_K" + cnt + " ON B1.ENTEXAMYEAR = JCM_K" + cnt + ".ENTEXAMYEAR ");
            cnt++;
        }
        stb.append("     LEFT JOIN JENT_ALL       JEALL ON B1.ENTEXAMYEAR = JEALL.ENTEXAMYEAR ");
        stb.append("     LEFT JOIN JENT_KANEN_ALL JKALL ON B1.ENTEXAMYEAR = JKALL.ENTEXAMYEAR ");
        stb.append(" ORDER BY ");
        stb.append("     SORT ");

        return  stb.toString();
    }

    private class EntCntClass {
        final String _entCnt;
        final String _grdDivCnt;
        public EntCntClass(
                final String entCnt,
                final String grdDivCnt
        ) {
            _entCnt = entCnt;
            _grdDivCnt = grdDivCnt;
        }
    }

    private class PrintMainData {
        final String _cityName;
        final String _finSchoolName;
        final Map    _entCntMap;
        final String _allEntCnt;
        final String _allGrdDivCnt;
        public PrintMainData(
                final String cityName,
                final String finSchoolName,
                final Map    entCntMap,
                final String allEntCnt,
                final String allGrdDivCnt
                ) {
            _cityName      = StringUtils.defaultString(cityName);
            _finSchoolName = finSchoolName;
            _entCntMap     = entCntMap;
            _allEntCnt     = allEntCnt;
            _allGrdDivCnt  = allGrdDivCnt;
        }
    }

    private class PrintCmData {
        final String _cmCd;
        final String _cmName;
        public PrintCmData(
                final String cmCd,
                final String cmName
        ) {
            _cmCd   = cmCd;
            _cmName = cmName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 59436 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _entExamYear;
        private final String _schoolCd;
        private final String _schoolKind;
        private final String _useSchool_KindField;
        private final String _areaName;
        private final String _schoolName;
        private final String _courseName;
        private final List _cousreMajorList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year         = request.getParameter("YEAR");
            _ctrlYear     = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate     = request.getParameter("CTRL_DATE");
            _entExamYear  = request.getParameter("ENTEXAM_YEAR");
            _schoolKind   = request.getParameter("SCHOOLKIND");
            _schoolCd     = request.getParameter("SCHOOLCD");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _areaName     = getSchoolInfo(db2, _ctrlYear, 1);
            _schoolName   = getSchoolInfo(db2, _ctrlYear, 2);
            _courseName   = getcourseName(db2, _ctrlYear);
            _cousreMajorList = getCouseMajorList(db2);
        }

        private String getSchoolInfo(final DB2UDB db2, final String year, final int kind) {
            String setAreaName   = null;
            String setSchoolName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     L1.NAME1 AS AERA_NAME, ");
                stb.append("     T1.SCHOOLNAME1 ");
                stb.append(" FROM ");
                stb.append("     V_SCHOOL_MST T1, ");
                stb.append("     FINSCHOOL_MST T2 ");
                stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1           = 'Z015' ");
                stb.append("                          AND T2.FINSCHOOL_DISTCD2 = L1.NAMECD2 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + year + "' ");
                stb.append("     AND T1.KYOUIKU_IINKAI_SCHOOLCD = T2.FINSCHOOLCD ");
                if ("1".equals(_useSchool_KindField)) {
                    stb.append(" AND T1.SCHOOLCD    = '" + _schoolCd + "' ");
                    stb.append(" AND T1.SCHOOL_KIND = '" + _schoolKind + "' ");
                }
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    setAreaName   = rs.getString("AERA_NAME");
                    setSchoolName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (1 == kind) {
                return setAreaName;
            } else {
                return setSchoolName;
            }
        }

        private String getcourseName(final DB2UDB db2, final String year) {
            String setcourseName   = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     COURSENAME ");
                stb.append(" FROM ");
                stb.append("     COURSE_MST ");
                stb.append(" WHERE ");
                stb.append("     COURSECD IN (SELECT ");
                stb.append("                     MIN(COURSECD) AS COURSECD ");
                stb.append("                 FROM ");
                stb.append("                     V_COURSE_MST ");
                stb.append("                 WHERE ");
                stb.append("                     YEAR = '" + year + "' ");
                stb.append("                 ) ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    setcourseName   = rs.getString("COURSENAME");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return setcourseName;
        }

        private List getCouseMajorList(final DB2UDB db2) {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSqlgetCouseMajor();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String cmCd    = rs.getString("CM_CD");
                    final String _cmName = rs.getString("CM_NAME");

                    final PrintCmData PrintCmData = new PrintCmData(cmCd, _cmName);
                    retList.add(PrintCmData);
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        private String getSqlgetCouseMajor() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE AS CM_CD, ");
            stb.append("     V1.MAJORNAME || '・' || V2.COURSECODENAME AS CM_NAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("     LEFT JOIN V_COURSE_MAJOR_MST V1 ON B1.ENTEXAMYEAR  = V1.YEAR ");
            stb.append("                                    AND B1.SUC_COURSECD = V1.COURSECD ");
            stb.append("                                    AND B1.SUC_MAJORCD  = V1.MAJORCD ");
            stb.append("     LEFT JOIN V_COURSECODE_MST V2 ON B1.ENTEXAMYEAR    = V2.YEAR ");
            stb.append("                                  AND B1.SUC_COURSECODE = V2.COURSECODE ");
            stb.append(" WHERE ");
            stb.append("         B1.ENTEXAMYEAR  = '" + _entExamYear + "' ");
            stb.append("     AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("     CM_CD ");

            return stb.toString();
        }
    }
}

// eof
