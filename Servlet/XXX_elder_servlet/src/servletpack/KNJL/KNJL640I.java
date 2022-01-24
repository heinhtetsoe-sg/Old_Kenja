/*
 * 作成日: 2020/12/14
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

public class KNJL640I {

    private static final Log log = LogFactory.getLog(KNJL640I.class);

    private boolean _hasData;

    private Param _param;

    private static final int NYUGAKU_TETSUDUKI_SYURYOUSYA_LINE_MAX = 50;

    private static final String GENERAL = "GENERAL";

    private static final String TECHNICAL = "TECHNICAL";

    private static final int SUBTOTAL_LINE_MAX = 5;

    private static final int NYURYOU_KIBOUSYA_LINE_MAX = 50;

    private static final Map<String, String> SUBJECT_MAP;

    private static final Map<String, String> OUTPUT_TYP_SUB_MAP;

    private static final int NYURYOU_KIBOUSYA_ANNAIYOU_LABEL_ROW_MAX = 6;

    private static final int NYURYOU_KIBOUSYA_ANNAIYOU_LABEL_COL_MAX = 2;

    private static final int NYURYOU_KIBOUSYA_ANNAIYOU_LABEL_LINE_MAX = NYURYOU_KIBOUSYA_ANNAIYOU_LABEL_ROW_MAX * NYURYOU_KIBOUSYA_ANNAIYOU_LABEL_COL_MAX;

    private static final String MAIL = "1";

    private static final String SUBJECT_MAP_GENERALCD = "1";

    private static final String SUBJECT_MAP_TECHNICALCD = "2";

    static {
        SUBJECT_MAP = new HashMap<String, String>();
        SUBJECT_MAP.put("1", "普通科");
        SUBJECT_MAP.put("2", "工業科");

        OUTPUT_TYP_SUB_MAP = new HashMap<String, String>();
        OUTPUT_TYP_SUB_MAP.put("1", "施設設備費");
        OUTPUT_TYP_SUB_MAP.put("2", "入学申込金");
    }

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
        if ("1".equals(_param._outputTyp)) {
            // 入学手続終了者リスト
            printNyugakuTetsudukiSyuryousyaList(db2, svf);
        } else if ("2".equals(_param._outputTyp)) {
            // 入学手続終了者数
            printNyugakuTetsudukiSyuryousyaSu(db2, svf);
        } else if ("3".equals(_param._outputTyp)) {
            // 未入金者リスト
            printNyugakuTetsudukiSyuryousyaList(db2, svf);
        } else if ("4".equals(_param._outputTyp)) {
            // 入寮希望者リスト
            printNyuryouKibousyaList(db2, svf);
        } else if ("5".equals(_param._outputTyp)) {
            // 入寮希望者案内用ラベル
            printNyuryouKibousyaAnnaiyouLabel(db2, svf);
        } else {
            // エラー
        }
    }

    /**
     * 入学手続終了者リストを出力する。
     *
     * @param db2
     * @param svf
     */
    private void printNyugakuTetsudukiSyuryousyaList(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL640I_3.frm", 1);

        int cumulative = 0;

        List<List<NyugakuTetsudukiSyuryousya>> ntsLists = getNyugakuTetsudukiSyuryousyaList(db2);
        for (List<NyugakuTetsudukiSyuryousya> nyugakuTetsudukiSyuryousyaList : ntsLists) {
            setNyugakuTetsudukiSyuryousyaListTitle(svf);
            int lineCnt = 1;

            // 一覧の表示
            for (NyugakuTetsudukiSyuryousya nyugakuTetsudukiSyuryousya : nyugakuTetsudukiSyuryousyaList) {
                svf.VrsOutn("NO", lineCnt, nyugakuTetsudukiSyuryousya._no);
                svf.VrsOutn("DEPARTMENT_NAME", lineCnt, SUBJECT_MAP.get(nyugakuTetsudukiSyuryousya._subject));
                svf.VrsOutn("EXAM_NO", lineCnt, nyugakuTetsudukiSyuryousya._receptno);

                final int nameByte = KNJ_EditEdit.getMS932ByteLength(nyugakuTetsudukiSyuryousya._name);
                final String nameFieldStr = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
                svf.VrsOutn("NAME" + nameFieldStr, lineCnt, nyugakuTetsudukiSyuryousya._name);

                final int nameKanaByte = KNJ_EditEdit.getMS932ByteLength(nyugakuTetsudukiSyuryousya._nameKana);
                final String nameKanaFieldStr = nameKanaByte > 30 ? "3" : nameKanaByte > 20 ? "2" : "1";
                svf.VrsOutn("KANA1_" + nameKanaFieldStr, lineCnt, nyugakuTetsudukiSyuryousya._nameKana);

                svf.VrsOutn("SEX", lineCnt, nyugakuTetsudukiSyuryousya._sex);

                svf.VrsOutn("FINSCHOOL_CD", lineCnt, nyugakuTetsudukiSyuryousya._fsCd);

                final int finschoolNameByte = KNJ_EditEdit.getMS932ByteLength(nyugakuTetsudukiSyuryousya._finschoolNameAbbv);
                final String finschoolNameFieldStr = finschoolNameByte > 30 ? "3" : finschoolNameByte > 20 ? "2" : "1";
                svf.VrsOutn("FINSCHOOL_NAME" + finschoolNameFieldStr, lineCnt, nyugakuTetsudukiSyuryousya._finschoolNameAbbv);

                svf.VrsOutn("MONEY1", lineCnt, nyugakuTetsudukiSyuryousya._deposit);

                int deposit = Integer.parseInt(StringUtils.defaultString(nyugakuTetsudukiSyuryousya._deposit, "0"));
                cumulative += deposit;
                svf.VrsOutn("TOTAL_MONEY", lineCnt, String.valueOf(cumulative)); // 累計

                if (nyugakuTetsudukiSyuryousya._depositDate != null) {
                    String depositMonth = KNJ_EditEdit.Ret_Num_Str(nyugakuTetsudukiSyuryousya._depositDate.substring(5, 7));
                    String depositDay = KNJ_EditEdit.Ret_Num_Str(nyugakuTetsudukiSyuryousya._depositDate.substring(8, 10));
                    String depositMonthDay = depositMonth + "月" + depositDay + "日";
                    svf.VrsOutn("DATE", lineCnt, depositMonthDay);
                }

                svf.VrsOutn("REMARK", lineCnt, nyugakuTetsudukiSyuryousya._remark);

                lineCnt++;
                _hasData = true;
            }

            svf.VrEndPage();
        }
    }

    /**
     * 入学手続終了者数を出力する。
     *
     * @param db2
     * @param svf
     */
    private void printNyugakuTetsudukiSyuryousyaSu(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL640I_2.frm", 4);

        int cumulative = 0;
        int cumulativeTotal = 0;

        Map<String, List<NyugakuTetsudukiSyuryousyaSu>> ntssMap = getNyugakuTetsudukiSyuryousyaSuMap(db2);
        Map<String, String> geneTechTotalMap = getGeneTechTotalMap(db2);

        setNyugakuTetsudukiSyuryousyaSuTitle(db2, svf);
        setNyugakuTetsudukiSyuryousyaSuHeader(svf);

        String generalTotal = geneTechTotalMap.get(GENERAL);
        String technicalTotal = geneTechTotalMap.get(TECHNICAL);

        svf.VrsOut("TOTAL_NUM1", generalTotal);
        svf.VrsOut("TOTAL_NUM2", technicalTotal);
        svf.VrsOut("TOTAL_NUM3", String.valueOf(Integer.parseInt(generalTotal)+ Integer.parseInt(technicalTotal)));

        for (String depositDate : ntssMap.keySet()) {
            String depositYear = depositDate.substring(0, 4);
            String depositMonth = depositDate.substring(5, 7);
            String depositDay = depositDate.substring(8, 10);
            String depositMonthDay = depositMonth + "月" + depositDay + "日";

            Calendar cal = Calendar.getInstance();
            cal.set(Integer.parseInt(depositYear), Integer.parseInt(depositMonth) - 1, Integer.parseInt(depositDay));
            String dayOfWeek = "";
            switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                dayOfWeek = "日";
                break;
            case Calendar.MONDAY:
                dayOfWeek = "月";
                break;
            case Calendar.TUESDAY:
                dayOfWeek = "火";
                break;
            case Calendar.WEDNESDAY:
                dayOfWeek = "水";
                break;
            case Calendar.THURSDAY:
                dayOfWeek = "木";
                break;
            case Calendar.FRIDAY:
                dayOfWeek = "金";
                break;
            case Calendar.SATURDAY:
                dayOfWeek = "土";
                break;
            }

            svf.VrsOut("DATE", depositMonthDay);
            svf.VrsOut("WEEK", "（" + dayOfWeek + "）");

            int generalCumulative = 0;
            int technicalCumulative = 0;

            List<NyugakuTetsudukiSyuryousyaSu> ntssList = ntssMap.get(depositDate);
            for (NyugakuTetsudukiSyuryousyaSu ntss : ntssList) {
                svf.VrsOut("SEX1", ntss._sex);
                svf.VrsOut("NUM1_!", KNJ_EditEdit.Ret_Num_Str(ntss._generalCnt));
                svf.VrsOut("NUM1_2", KNJ_EditEdit.Ret_Num_Str(ntss._techinicalCnt));
                svf.VrsOut("SUB_TOTAL_NUM1_1", String.valueOf(ntss.getSum()));

                generalCumulative += Integer.parseInt(ntss._generalCnt);
                technicalCumulative += Integer.parseInt(ntss._techinicalCnt);
                cumulative += ntss.getSum();
                cumulativeTotal += ntss.getSum();

                _hasData = true;
                svf.VrEndRecord();
            }

            svf.VrsOut("SEX2", "計");
            svf.VrsOut("NUM2_1", KNJ_EditEdit.Ret_Num_Str(String.valueOf(generalCumulative)));
            svf.VrsOut("NUM2_2", KNJ_EditEdit.Ret_Num_Str(String.valueOf(technicalCumulative)));
            svf.VrsOut("SUB_TOTAL_NUM2_1", String.valueOf(cumulative));
            svf.VrsOut("SUB_TOTAL_NUM2_2", String.valueOf(cumulativeTotal));

            cumulative = 0;
            svf.VrEndRecord();
        }
    }

    /**
     * 未入金者リストを出力する。
     *
     * @param db2
     * @param svf
     */
    private void printMinyukinsyaList(final DB2UDB db2, final Vrw32alp svf) {
    }

    /**
     * 入寮希望者リストを出力する。
     *
     * @param db2
     * @param svf
     */
    private void printNyuryouKibousyaList(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL640I_4.frm", 1);

        List<List<NyuryouKibousya>> nkLists = getNyuryouKibousyaList(db2);

        for (List<NyuryouKibousya> nyuryouKibousyaList : nkLists) {
            setNyuryouKibousyaTitle(svf);

            int lineCnt = 1;

            for (NyuryouKibousya nyuryouKibousya : nyuryouKibousyaList) {
                svf.VrsOutn("NO", lineCnt, nyuryouKibousya._no);
                svf.VrsOutn("HR", lineCnt, nyuryouKibousya._hrName);
                svf.VrsOutn("ATTEND_NO", lineCnt, nyuryouKibousya._attendno);
                svf.VrsOutn("EXAM_NO", lineCnt, nyuryouKibousya._examno);
                svf.VrsOutn("SCHREG_NO", lineCnt, nyuryouKibousya._schregno);

                final int nameByte = KNJ_EditEdit.getMS932ByteLength(nyuryouKibousya._name);
                final String nameFieldStr = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
                svf.VrsOutn("NAME" + nameFieldStr, lineCnt, nyuryouKibousya._name);

                final int nameKanaByte = KNJ_EditEdit.getMS932ByteLength(nyuryouKibousya._nameKana);
                final String nameKanaFieldStr = nameKanaByte > 30 ? "3" : nameKanaByte > 20 ? "2" : "1";
                svf.VrsOutn("KANA1_" + nameKanaFieldStr, lineCnt, nyuryouKibousya._nameKana);

                svf.VrsOutn("SEX", lineCnt, nyuryouKibousya._sex);

                final int finschoolNameByte = KNJ_EditEdit.getMS932ByteLength(nyuryouKibousya._finschoolNameAbbv);
                final String finschoolNameFieldStr = finschoolNameByte > 30 ? "3" : finschoolNameByte > 20 ? "2" : "1";
                svf.VrsOutn("FINSCHOOL_NAME" + finschoolNameFieldStr, lineCnt, nyuryouKibousya._finschoolNameAbbv);

                svf.VrsOutn("SP_REASON", lineCnt, nyuryouKibousya._honorReason);
                svf.VrsOutn("ZIP_NO", lineCnt, nyuryouKibousya._zipcd);

                final int addr1NameByte = KNJ_EditEdit.getMS932ByteLength(nyuryouKibousya._address1);
                final String addr1NameFieldStr = addr1NameByte > 50 ? "3" : addr1NameByte > 40 ? "2" : "1";
                svf.VrsOutn("ADDR1_" + addr1NameFieldStr, lineCnt, nyuryouKibousya._address1);

                final int addr2NameByte = KNJ_EditEdit.getMS932ByteLength(nyuryouKibousya._address1);
                final String addr2NameFieldStr = addr2NameByte > 50 ? "3" : addr2NameByte > 40 ? "2" : "1";
                svf.VrsOutn("ADDR2_" + addr2NameFieldStr, lineCnt, nyuryouKibousya._address2);

                svf.VrsOutn("TEL_NO", lineCnt, nyuryouKibousya._telno);
                svf.VrsOutn("DORMITORY_NAME", lineCnt, nyuryouKibousya._dormitoriesName);

                lineCnt++;
                _hasData = true;
            }

            svf.VrEndPage();
        }
    }

    /**
     * 入寮希望者案内用ラベルを出力する。
     *
     * @param db2
     * @param svf
     * @param outputDate yyyy/MM/dd形式の現在日時
     */
    private void printNyuryouKibousyaAnnaiyouLabel(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL640I_5.frm", 1);

        List<List<NyuryouKibousya>> nkLists = getNyuryouKibousyaList(db2);

        for (List<NyuryouKibousya> nyuryouKibousyaList : nkLists) {
            int colCnt = 1;
            int lineCnt = 1;

            for (NyuryouKibousya nyuryouKibousya : nyuryouKibousyaList) {
                if (colCnt > NYURYOU_KIBOUSYA_ANNAIYOU_LABEL_COL_MAX) {
                    colCnt = 1;
                    lineCnt ++;
                }

                svf.VrsOutn("EXAM_NO" + colCnt, lineCnt, nyuryouKibousya._examno);

                final int finschoolNameByte = KNJ_EditEdit.getMS932ByteLength(nyuryouKibousya._finschoolNameAbbv);
                final String finschoolNameFieldStr = finschoolNameByte > 30 ? "3" : finschoolNameByte > 20 ? "2" : "1";
                svf.VrsOutn("FINSCHOOL_NAME" + colCnt + "_" + finschoolNameFieldStr, lineCnt, nyuryouKibousya._finschoolNameAbbv);


                final int nameByte = KNJ_EditEdit.getMS932ByteLength(nyuryouKibousya._name);
                final String nameFieldStr = nameByte > 38 ? "3" : nameByte > 28 ? "2" : "1";
                svf.VrsOutn("NAME" + colCnt + "_" + nameFieldStr, lineCnt, nyuryouKibousya._name);

                svf.VrsOutn("DOMITORY" + colCnt, lineCnt, nyuryouKibousya._dormitoriesName);
                svf.VrsOutn("SCHOLAR" + colCnt, lineCnt, nyuryouKibousya._honorReason);

                colCnt++;
                _hasData = true;
            }

            svf.VrEndPage();
        }
    }

    private void setNyugakuTetsudukiSyuryousyaListTitle(final Vrw32alp svf) {
        String titleStr = "1".equals(_param._outputTyp) ? "" : "未";
        String outputTypSub = "1".equals(_param._outputTyp) ? _param._outputTyp1Sub : _param._outputTyp3Sub;
        svf.VrsOut("TITLE", _param._entexamyear + "年度　" + OUTPUT_TYP_SUB_MAP.get(outputTypSub) + "　" + titleStr + "入金者リスト（" + _param._testDivName + "）");
    }

    private void setNyugakuTetsudukiSyuryousyaSuTitle(final DB2UDB db2, final Vrw32alp svf) {
        String year = KNJ_EditDate.h_format_JP_N(db2, (_param._entexamyear + "/01/01"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String outputDate = sdf.format(new Date());

        svf.VrsOut("TITLE", year + "度　入学手続者（" + _param._testDivName + "）　" + OUTPUT_TYP_SUB_MAP.get(_param._outputTyp2Sub));
        svf.VrsOut("DATE", outputDate + "　現在");

    }

    private void setNyugakuTetsudukiSyuryousyaSuHeader(final Vrw32alp svf) {
        svf.VrsOut("FACULTY_NAME1", SUBJECT_MAP.get(SUBJECT_MAP_GENERALCD));
        svf.VrsOut("FACULTY_NAME2", SUBJECT_MAP.get(SUBJECT_MAP_TECHNICALCD));
    }

    private void setNyuryouKibousyaTitle(final Vrw32alp svf) {
        svf.VrsOut("TITLE", _param._entexamyear + "年度　入寮者リスト（" + _param._testDivName + "）");
    }

    private List<List<NyugakuTetsudukiSyuryousya>> getNyugakuTetsudukiSyuryousyaList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<List<NyugakuTetsudukiSyuryousya>> ntsLists = new ArrayList<List<NyugakuTetsudukiSyuryousya>>();
        List<NyugakuTetsudukiSyuryousya> nyugakuTetsudukiSyuryousyaList = new ArrayList<NyugakuTetsudukiSyuryousya>();

        try {
            final String nyugakuTetsudukiSyuryousyaSql = getNyugakuTetsudukiSyuryousyaSql();
            log.debug(" sql =" + nyugakuTetsudukiSyuryousyaSql);
            ps = db2.prepareStatement(nyugakuTetsudukiSyuryousyaSql);
            rs = ps.executeQuery();

            int lineCnt = 1;

            while (rs.next()) {
                //特待生かを判定するためのコード
                final String psCd = rs.getString("SP_CD");
                final String psReasonCd = rs.getString("SP_REASON_CD");
                //特待以外の受験者が支払う費用
                final String defaultCostKey = ("1".equals(_param._outputTyp) && "1".equals(_param._outputTyp1Sub) || "3".equals(_param._outputTyp) && "1".equals(_param._outputTyp3Sub)) ? "EQUIPMENT_COST" : "ENTRY_FEE";
                final String defaultCost = (String)_param._getDefaultCostMap.get(defaultCostKey);

                final String no = rs.getString("ROW_NUMBER");
                final String subject = rs.getString("SUBJECT");
                final String receptno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String fsCd = rs.getString("FS_CD");
                final String finschoolNameAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String payment = (psCd != null && psReasonCd != null) ? rs.getString("DEPOSIT") : defaultCost;
                final String depositDate = rs.getString("DEPOSIT_DATE");
                final String remark = rs.getString("REMARK");

                final NyugakuTetsudukiSyuryousya nyugakuTetsudukiSyuryousya =
                    new NyugakuTetsudukiSyuryousya(no, subject, receptno, name, nameKana, sex, fsCd, finschoolNameAbbv, payment, depositDate, remark);

                nyugakuTetsudukiSyuryousyaList.add(nyugakuTetsudukiSyuryousya);
                if (lineCnt == 1) {
                    ntsLists.add(nyugakuTetsudukiSyuryousyaList);
                    lineCnt++;
                } else if (lineCnt >= NYUGAKU_TETSUDUKI_SYURYOUSYA_LINE_MAX) {
                    nyugakuTetsudukiSyuryousyaList = new ArrayList<NyugakuTetsudukiSyuryousya>();
                    lineCnt = 1;
                } else {
                    lineCnt++;
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return ntsLists;
    }

    private Map<String, List<NyugakuTetsudukiSyuryousyaSu>> getNyugakuTetsudukiSyuryousyaSuMap(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, List<NyugakuTetsudukiSyuryousyaSu>> ntssMap = new LinkedHashMap<String, List<NyugakuTetsudukiSyuryousyaSu>>();
        List<NyugakuTetsudukiSyuryousyaSu> nyugakuTetsudukiSyuryousyaList = null;

        try {
            final String nyugakuTetsudukiSyuryousyaSuSql = getNyugakuTetsudukiSyuryousyaSuSql();
            log.debug(" sql =" + nyugakuTetsudukiSyuryousyaSuSql);
            ps = db2.prepareStatement(nyugakuTetsudukiSyuryousyaSuSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String depositDate = rs.getString("DEPOSIT_DATE");
                final String sex = rs.getString("SEX");
                final String generalCnt = rs.getString("GENERAL_CNT");
                final String technicalCnt = rs.getString("TECHNICAL_CNT");

                final NyugakuTetsudukiSyuryousyaSu nyugakuTetsudukiSyuryousyaSu =
                    new NyugakuTetsudukiSyuryousyaSu(depositDate, sex, generalCnt, technicalCnt);

                if (ntssMap.containsKey(depositDate)) {
                    ntssMap.get(depositDate).add(nyugakuTetsudukiSyuryousyaSu);
                } else {
                    nyugakuTetsudukiSyuryousyaList = new ArrayList<NyugakuTetsudukiSyuryousyaSu>();
                    nyugakuTetsudukiSyuryousyaList.add(nyugakuTetsudukiSyuryousyaSu);
                    ntssMap.put(depositDate, nyugakuTetsudukiSyuryousyaList);
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return ntssMap;
    }

    private Map<String, String> getGeneTechTotalMap(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, String> geneTechTotalMap = new LinkedHashMap<String, String>();

        try {
            final String geneTechTotalSql = getGeneTechTotalSql();
            log.debug(" sql =" + geneTechTotalSql);
            ps = db2.prepareStatement(geneTechTotalSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                geneTechTotalMap.put(GENERAL, rs.getString("GENERAL_CNT"));
                geneTechTotalMap.put(TECHNICAL, rs.getString("TECHNICAL_CNT"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return geneTechTotalMap;
    }

    private List<List<NyuryouKibousya>> getNyuryouKibousyaList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<List<NyuryouKibousya>> nkLists = new ArrayList<List<NyuryouKibousya>>();
        List<NyuryouKibousya> nyuryouKibousyaList = new ArrayList<NyuryouKibousya>();

        try {
            final String nyuryouKibousyaSql = getNyuryouKibousyaListSql();
            log.debug(" sql =" + nyuryouKibousyaSql);
            ps = db2.prepareStatement(nyuryouKibousyaSql);
            rs = ps.executeQuery();

            int lineCnt = 1;

            while (rs.next()) {
                final String no = rs.getString("ROW_NUMBER");
                final String hrName = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String examno = rs.getString("EXAMNO");
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String finschoolNameAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String honorReason = rs.getString("HONOR_REASON");
                final String zipcd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String telno = rs.getString("TELNO");
                final String dormitoriesName = rs.getString("DORMITORIES_NAME");

                //1ページMAX件数
                final int pageMaxNum = ("4".equals(_param._outputTyp)) ? NYURYOU_KIBOUSYA_LINE_MAX : NYURYOU_KIBOUSYA_ANNAIYOU_LABEL_LINE_MAX;

                final NyuryouKibousya nyuryouKibousya =
                    new NyuryouKibousya(no, hrName, attendno, examno, schregno, name, nameKana, sex, finschoolNameAbbv, honorReason, zipcd, address1, address2, telno, dormitoriesName);

                nyuryouKibousyaList.add(nyuryouKibousya);
                if (lineCnt == 1) {
                    nkLists.add(nyuryouKibousyaList);
                    lineCnt++;
                } else if (lineCnt >= pageMaxNum) {
                    nyuryouKibousyaList = new ArrayList<NyuryouKibousya>();
                    lineCnt = 1;
                } else {
                    lineCnt++;
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return nkLists;
    }

    private String getNyugakuTetsudukiSyuryousyaSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     ROWNUMBER() OVER() AS ROW_NUMBER, ");
        stb.append("     BASE.TESTDIV0 AS SUBJECT, ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     FS.FINSCHOOL_NAME_ABBV, ");
        if ("1".equals(_param._outputTyp)) {
            // 1:入学手続修了者リストの場合
            if ("1".equals(_param._outputTyp1Sub)) {
                // 1:施設設備費の場合
                stb.append("     DIV04.REMARK2 AS DEPOSIT, ");
                stb.append("     RECEPT_D020.REMARK4 AS DEPOSIT_DATE, ");
            } else {
                // 2:入学申込金の場合
                stb.append("     DIV04.REMARK1 AS DEPOSIT, ");
                stb.append("     RECEPT_D020.REMARK2 AS DEPOSIT_DATE, ");
            }
        } else {
            // 2:未入金者リストの場合
            if ("1".equals(_param._outputTyp3Sub)) {
                // 1:施設設備費の場合
                stb.append("     DIV04.REMARK2 AS DEPOSIT, ");
                stb.append("     RECEPT_D020.REMARK4 AS DEPOSIT_DATE, ");
            } else {
                // 2:入学申込金の場合
                stb.append("     DIV04.REMARK1 AS DEPOSIT, ");
                stb.append("     RECEPT_D020.REMARK2 AS DEPOSIT_DATE, ");
            }
        }
        stb.append("     DIV04.GENERAL_MARK AS REMARK, ");
        stb.append("     DIV04.GENERAL_CD AS SP_CD, "); //特待コード
        stb.append("     DIV05.GENERAL_CD AS SP_REASON_CD "); //特待理由コード

        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");

        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L013 ON ");
        stb.append("               L013.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND L013.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND L013.SETTING_CD   = 'L013' ");
        stb.append("           AND L013.SEQ          = RECEPT.JUDGEDIV ");

        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ");
        stb.append("               BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND BASE.EXAMNO       = RECEPT.EXAMNO ");
        stb.append("           AND BASE.TESTDIV      = RECEPT.TESTDIV ");

        stb.append("     LEFT JOIN V_NAME_MST Z002 ON ");
        stb.append("               Z002.YEAR    = BASE.ENTEXAMYEAR ");
        stb.append("           AND Z002.NAMECD1 = 'Z002' ");
        stb.append("           AND Z002.NAMECD2 = BASE.SEX ");

        stb.append("     LEFT JOIN FINSCHOOL_MST AS FS ON ");
        stb.append("               FS.FINSCHOOLCD = BASE.FS_CD ");

        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D015 ON ");
        stb.append("               RECEPT_D015.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND RECEPT_D015.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND RECEPT_D015.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT_D015.EXAM_TYPE    = RECEPT.EXAM_TYPE ");
        stb.append("           AND RECEPT_D015.RECEPTNO     = RECEPT.RECEPTNO ");
        stb.append("           AND RECEPT_D015.SEQ          = '015' ");

        stb.append("     LEFT JOIN ENTEXAM_GENERAL_MST DIV04 ON ");
        stb.append("               DIV04.ENTEXAMYEAR  = RECEPT_D015.ENTEXAMYEAR ");
        stb.append("           AND DIV04.APPLICANTDIV = RECEPT_D015.APPLICANTDIV ");
        stb.append("           AND DIV04.TESTDIV      = '0' "); // '0' 固定
        stb.append("           AND DIV04.GENERAL_DIV  = '04' ");
        stb.append("           AND DIV04.GENERAL_CD   = RECEPT_D015.REMARK4 ");

        stb.append("     LEFT JOIN ENTEXAM_GENERAL_MST DIV05 ON ");
        stb.append("               DIV05.ENTEXAMYEAR  = RECEPT_D015.ENTEXAMYEAR ");
        stb.append("           AND DIV05.APPLICANTDIV = RECEPT_D015.APPLICANTDIV ");
        stb.append("           AND DIV05.TESTDIV      = '0' "); // '0' 固定
        stb.append("           AND DIV05.GENERAL_DIV  = '05' ");
        stb.append("           AND DIV05.GENERAL_CD   = RECEPT_D015.REMARK5 ");

        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D020 ON ");
        stb.append("               RECEPT_D020.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND RECEPT_D020.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND RECEPT_D020.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT_D020.EXAM_TYPE    = RECEPT.EXAM_TYPE ");
        stb.append("           AND RECEPT_D020.RECEPTNO     = RECEPT.RECEPTNO ");
        stb.append("           AND RECEPT_D020.SEQ          = '020' ");

        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append(" AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append(" AND RECEPT.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append(" AND L013.NAMESPARE1     = '1' "); // 1:合格者
        stb.append(" AND (BASE.ENTDIV <> '2' OR BASE.ENTDIV IS NULL) "); // 2:辞退者 以外
        // ALL:全て 以外の場合に条件に加える
        if (!"ALL".equals(_param._dept)) {
            stb.append(" AND BASE.TESTDIV0       = '" + _param._dept + "' ");
        }
        if ("1".equals(_param._outputTyp)) {
            // 1:入学手続修了者リストの場合
            if ("1".equals(_param._outputTyp1Sub)) {
                // 1:施設設備費の場合
                stb.append(" AND RECEPT_D020.REMARK3 = '1' ");
            } else {
                // 2:入学申込金の場合
                stb.append(" AND RECEPT_D020.REMARK1 = '1' ");
            }
        } else {
            // 2:未入金者リストの場合
            if ("1".equals(_param._outputTyp3Sub)) {
                // 1:施設設備費の場合
                stb.append(" AND (RECEPT_D020.REMARK3 <> '1' OR RECEPT_D020.REMARK3 IS NULL) ");
            } else {
                // 2:入学申込金の場合
                stb.append(" AND (RECEPT_D020.REMARK1 <> '1' OR RECEPT_D020.REMARK1 IS NULL) ");
            }
        }

        stb.append(" ORDER BY ");
        if ("2".equals(_param._orderBy)) {
            stb.append("     BASE.NAME_KANA, ");
        }
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    private String getNyugakuTetsudukiSyuryousyaSuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TEMP AS ( ");
        stb.append(" SELECT ");
        if ("1".equals(_param._outputTyp2Sub)) {
            // 1:施設設備費の場合
            stb.append("     RECEPT_D020.REMARK4 AS DEPOSIT_DATE, ");
        } else {
            // 2:入学申込金の場合
            stb.append("     RECEPT_D020.REMARK2 AS DEPOSIT_DATE, ");
        }
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     CASE WHEN BASE.TESTDIV0 = '1' THEN 1 ELSE 0 END AS GENERAL_CNT, ");
        stb.append("     CASE WHEN BASE.TESTDIV0 = '2' THEN 1 ELSE 0 END AS TECHNICAL_CNT ");

        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");

        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L013 ON ");
        stb.append("               L013.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND L013.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND L013.SETTING_CD   = 'L013' ");
        stb.append("           AND L013.SEQ          = RECEPT.JUDGEDIV ");

        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ");
        stb.append("               BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND BASE.EXAMNO       = RECEPT.EXAMNO ");
        stb.append("           AND BASE.TESTDIV      = RECEPT.TESTDIV ");

        stb.append("     LEFT JOIN V_NAME_MST Z002 ON ");
        stb.append("               Z002.YEAR    = BASE.ENTEXAMYEAR ");
        stb.append("           AND Z002.NAMECD1 = 'Z002' ");
        stb.append("           AND Z002.NAMECD2 = BASE.SEX ");

        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D015 ON ");
        stb.append("               RECEPT_D015.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND RECEPT_D015.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND RECEPT_D015.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT_D015.EXAM_TYPE    = RECEPT.EXAM_TYPE ");
        stb.append("           AND RECEPT_D015.RECEPTNO     = RECEPT.RECEPTNO ");
        stb.append("           AND RECEPT_D015.SEQ          = '015' ");

        stb.append("     LEFT JOIN ENTEXAM_GENERAL_MST DIV04 ON ");
        stb.append("               DIV04.ENTEXAMYEAR  = RECEPT_D015.ENTEXAMYEAR ");
        stb.append("           AND DIV04.APPLICANTDIV = RECEPT_D015.APPLICANTDIV ");
        stb.append("           AND DIV04.TESTDIV      = '0' "); // '0' 固定
        stb.append("           AND DIV04.GENERAL_DIV  = '04' ");
        stb.append("           AND DIV04.GENERAL_CD   = RECEPT_D015.REMARK4 ");

        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D020 ON ");
        stb.append("               RECEPT_D020.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND RECEPT_D020.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND RECEPT_D020.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT_D020.EXAM_TYPE    = RECEPT.EXAM_TYPE ");
        stb.append("           AND RECEPT_D020.RECEPTNO     = RECEPT.RECEPTNO ");
        stb.append("           AND RECEPT_D020.SEQ          = '020' ");

        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append(" AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append(" AND RECEPT.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append(" AND L013.NAMESPARE1     = '1' "); // 1:合格者
        stb.append(" AND (BASE.ENTDIV <> '2' OR BASE.ENTDIV IS NULL) "); // 2:辞退者 以外
        // ALL:全て 以外の場合に条件に加える
        if (!"ALL".equals(_param._dept)) {
            stb.append(" AND BASE.TESTDIV0       = '" + _param._dept + "' ");
        }
        if ("1".equals(_param._outputTyp2Sub)) {
            // 1:施設設備費の場合
            stb.append(" AND RECEPT_D020.REMARK3 = '1' ");
        } else {
            // 2:入学申込金の場合
            stb.append(" AND RECEPT_D020.REMARK1 = '1' ");
        }

        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     DEPOSIT_DATE, ");
        stb.append("     SEX, ");
        stb.append("     SUM(GENERAL_CNT) AS GENERAL_CNT, ");
        stb.append("     SUM(TECHNICAL_CNT) AS TECHNICAL_CNT ");
        stb.append(" FROM ");
        stb.append("     TEMP ");
        stb.append(" GROUP BY ");
        stb.append("     DEPOSIT_DATE, ");
        stb.append("     SEX ");

        stb.append(" ORDER BY ");
        stb.append("     DEPOSIT_DATE, ");
        stb.append("     SEX DESC ");
        return stb.toString();
    }

    private String getGeneTechTotalSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TEMP AS ( ");
        stb.append(" SELECT ");
        stb.append("     CASE WHEN BASE.TESTDIV0 = '1' THEN 1 ELSE 0 END AS GENERAL_CNT, ");
        stb.append("     CASE WHEN BASE.TESTDIV0 = '2' THEN 1 ELSE 0 END AS TECHNICAL_CNT ");

        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");

        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L013 ON ");
        stb.append("               L013.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND L013.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND L013.SETTING_CD   = 'L013' ");
        stb.append("           AND L013.SEQ          = RECEPT.JUDGEDIV ");

        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ");
        stb.append("               BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND BASE.EXAMNO       = RECEPT.EXAMNO ");
        stb.append("           AND BASE.TESTDIV      = RECEPT.TESTDIV ");

        stb.append("     LEFT JOIN V_NAME_MST Z002 ON ");
        stb.append("               Z002.YEAR    = BASE.ENTEXAMYEAR ");
        stb.append("           AND Z002.NAMECD1 = 'Z002' ");
        stb.append("           AND Z002.NAMECD2 = BASE.SEX ");

        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D015 ON ");
        stb.append("               RECEPT_D015.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND RECEPT_D015.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND RECEPT_D015.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT_D015.EXAM_TYPE    = RECEPT.EXAM_TYPE ");
        stb.append("           AND RECEPT_D015.RECEPTNO     = RECEPT.RECEPTNO ");
        stb.append("           AND RECEPT_D015.SEQ          = '015' ");

        stb.append("     LEFT JOIN ENTEXAM_GENERAL_MST DIV04 ON ");
        stb.append("               DIV04.ENTEXAMYEAR  = RECEPT_D015.ENTEXAMYEAR ");
        stb.append("           AND DIV04.APPLICANTDIV = RECEPT_D015.APPLICANTDIV ");
        stb.append("           AND DIV04.TESTDIV      = '0' "); // '0' 固定
        stb.append("           AND DIV04.GENERAL_DIV  = '04' ");
        stb.append("           AND DIV04.GENERAL_CD   = RECEPT_D015.REMARK4 ");

        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D020 ON ");
        stb.append("               RECEPT_D020.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND RECEPT_D020.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND RECEPT_D020.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT_D020.EXAM_TYPE    = RECEPT.EXAM_TYPE ");
        stb.append("           AND RECEPT_D020.RECEPTNO     = RECEPT.RECEPTNO ");
        stb.append("           AND RECEPT_D020.SEQ          = '020' ");

        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append(" AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append(" AND RECEPT.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append(" AND L013.NAMESPARE1     = '1' "); // 1:合格者
        stb.append(" AND (BASE.ENTDIV <> '2' OR BASE.ENTDIV IS NULL) "); // 2:辞退者 以外
        // ALL:全て 以外の場合に条件に加える
        if (!"ALL".equals(_param._dept)) {
            stb.append(" AND BASE.TESTDIV0       = '" + _param._dept + "' ");
        }
        if ("1".equals(_param._outputTyp2Sub)) {
            // 1:施設設備費の場合
            stb.append(" AND RECEPT_D020.REMARK3 = '1' ");
        } else {
            // 2:入学申込金の場合
            stb.append(" AND RECEPT_D020.REMARK1 = '1' ");
        }

        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SUM(GENERAL_CNT) AS GENERAL_CNT, ");
        stb.append("     SUM(TECHNICAL_CNT) AS TECHNICAL_CNT ");
        stb.append(" FROM ");
        stb.append("     TEMP ");
        return stb.toString();
    }

    private String getNyuryouKibousyaListSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     ROWNUMBER() OVER() AS ROW_NUMBER, ");
        stb.append("     REGD_H.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE_D026.REMARK1 AS SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     FS.FINSCHOOL_NAME_ABBV, ");
        stb.append("     DIV05.GENERAL_NAME AS HONOR_REASON, ");
        stb.append("     ADDR.ZIPCD, ");
        stb.append("     ADDR.ADDRESS1, ");
        stb.append("     ADDR.ADDRESS2, ");
        stb.append("     ADDR.TELNO, ");
        stb.append("     L042.NAME1 AS DORMITORIES_NAME ");

        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");

        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D020 ON ");
        stb.append("               RECEPT_D020.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND RECEPT_D020.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND RECEPT_D020.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT_D020.EXAM_TYPE    = RECEPT.EXAM_TYPE ");
        stb.append("           AND RECEPT_D020.RECEPTNO     = RECEPT.RECEPTNO ");
        stb.append("           AND RECEPT_D020.SEQ          = '020' ");

        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L013 ON ");
        stb.append("               L013.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND L013.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND L013.SETTING_CD   = 'L013' ");
        stb.append("           AND L013.SEQ          = RECEPT.JUDGEDIV ");

        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ");
        stb.append("               BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND BASE.EXAMNO       = RECEPT.EXAMNO ");
        stb.append("           AND BASE.TESTDIV      = RECEPT.TESTDIV ");

        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D026 ON ");
        stb.append("               BASE_D026.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("           AND BASE_D026.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND BASE_D026.EXAMNO       = BASE.EXAMNO ");
        stb.append("           AND BASE_D026.SEQ          = '026' ");

        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON ");
        stb.append("               REGD.YEAR     = BASE_D026.ENTEXAMYEAR ");
        stb.append("           AND REGD.SEMESTER = '1' ");
        stb.append("           AND REGD.SCHREGNO = BASE_D026.REMARK1 ");

        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON ");
        stb.append("               REGD_H.YEAR     = REGD.YEAR ");
        stb.append("           AND REGD_H.SEMESTER = REGD.SEMESTER ");
        stb.append("           AND REGD_H.GRADE    = REGD.GRADE ");
        stb.append("           AND REGD_H.HR_CLASS = REGD.HR_CLASS ");

        stb.append("     LEFT JOIN V_NAME_MST Z002 ON ");
        stb.append("               Z002.YEAR    = BASE.ENTEXAMYEAR ");
        stb.append("           AND Z002.NAMECD1 = 'Z002' ");
        stb.append("           AND Z002.NAMECD2 = BASE.SEX ");

        stb.append("     LEFT JOIN FINSCHOOL_MST AS FS ON ");
        stb.append("               FS.FINSCHOOLCD = BASE.FS_CD ");

        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D015 ON ");
        stb.append("               RECEPT_D015.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND RECEPT_D015.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND RECEPT_D015.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT_D015.EXAM_TYPE    = RECEPT.EXAM_TYPE ");
        stb.append("           AND RECEPT_D015.RECEPTNO     = RECEPT.RECEPTNO ");
        stb.append("           AND RECEPT_D015.SEQ          = '015' ");

        stb.append("     LEFT JOIN ENTEXAM_GENERAL_MST DIV05 ON ");
        stb.append("               DIV05.ENTEXAMYEAR  = RECEPT_D015.ENTEXAMYEAR ");
        stb.append("           AND DIV05.APPLICANTDIV = RECEPT_D015.APPLICANTDIV ");
        stb.append("           AND DIV05.TESTDIV      = '0' "); // '0' 固定
        stb.append("           AND DIV05.GENERAL_DIV  = '05' ");
        stb.append("           AND DIV05.GENERAL_CD   = RECEPT_D015.REMARK5 ");

        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON ");
        stb.append("               ADDR.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("           AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND ADDR.EXAMNO       = BASE.EXAMNO ");

        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L042 ON ");
        stb.append("               L042.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("           AND L042.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND L042.SETTING_CD   = 'L042' ");
        stb.append("           AND L042.SEQ          = BASE.SEX ");

        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append(" AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append(" AND RECEPT.TESTDIV      = '" + _param._testDiv + "' ");
        // ALL:全て 以外の場合に条件に加える
        if (!"ALL".equals(_param._dept)) {
            stb.append(" AND BASE.TESTDIV0       = '" + _param._dept + "' ");
        }
        stb.append(" AND BASE.DORMITORY_FLG  = '1' "); // 1:入寮希望者
        stb.append(" AND L013.NAMESPARE1     = '1' "); // 1:合格者
        stb.append(" AND (BASE.ENTDIV <> '2' OR BASE.ENTDIV IS NULL) "); // 2:辞退者 以外
        stb.append(" AND BASE.PROCEDUREDIV   = '1' "); // 1:手続き終了

        stb.append(" ORDER BY ");
        if ("2".equals(_param._orderBy)) {
            stb.append("     BASE.NAME_KANA, ");
        }
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class NyugakuTetsudukiSyuryousya {
        private final String _no;
        private final String _subject;
        private final String _receptno;
        private final String _name;
        private final String _nameKana;
        private final String _sex;
        private final String _fsCd;
        private final String _finschoolNameAbbv;
        private final String _deposit;
        private final String _depositDate;
        private final String _remark;

        NyugakuTetsudukiSyuryousya (
            final String no,
            final String subject,
            final String receptno,
            final String name,
            final String nameKana,
            final String sex,
            final String fsCd,
            final String finschoolName,
            final String deposit,
            final String depositDate,
            final String remark
        ) {
            _no = no;
            _subject = subject;
            _receptno = receptno;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _fsCd = fsCd;
            _finschoolNameAbbv = finschoolName;
            _deposit = deposit;
            _depositDate = depositDate;
            _remark = remark;
        }
    }

    private class NyugakuTetsudukiSyuryousyaSu {
        private final String _depositDate;
        private final String _sex;
        private final String _generalCnt;
        private final String _techinicalCnt;

        NyugakuTetsudukiSyuryousyaSu (
            final String depositDate,
            final String sex,
            final String generalCnt,
            final String techinicalCnt
        ) {
            _sex = sex;
            _depositDate = depositDate;
            _generalCnt = generalCnt;
            _techinicalCnt = techinicalCnt;
        }

        private int getSum() {
            return Integer.parseInt(_generalCnt) + Integer.parseInt(_techinicalCnt);
        }
    }

    private class NyuryouKibousya {
        private final String _no;
        private final String _hrName;
        private final String _attendno;
        private final String _examno;
        private final String _schregno;
        private final String _name;
        private final String _nameKana;
        private final String _sex;
        private final String _finschoolNameAbbv;
        private final String _honorReason;
        private final String _zipcd;
        private final String _address1;
        private final String _address2;
        private final String _telno;
        private final String _dormitoriesName;

        NyuryouKibousya (
            final String no,
            final String hrName,
            final String attendno,
            final String examno,
            final String schregno,
            final String name,
            final String nameKana,
            final String sex,
            final String finschoolNameAbbv,
            final String honorReason,
            final String zipcd,
            final String address1,
            final String address2,
            final String telno,
            final String dormitoriesName
        ) {
            _no = no;
            _hrName = hrName;
            _attendno = attendno;
            _examno = examno;
            _schregno = schregno;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _finschoolNameAbbv = finschoolNameAbbv;
            _honorReason = honorReason;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
            _telno = telno;
            _dormitoriesName = dormitoriesName;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _dept;
        private final String _testDiv;
        private final String _orderBy;
        private final String _outputTyp;
        private final String _outputTyp1Sub;
        private final String _outputTyp2Sub;
        private final String _outputTyp3Sub;
        private final String _testDivName;

        private Map _getDefaultCostMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = "2"; // 2:高校 固定
            _dept = request.getParameter("DEPT");
            _testDiv = request.getParameter("TESTDIV");
            _orderBy = request.getParameter("ORDER_BY");
            _outputTyp = request.getParameter("OUTPUT_TYP");
            _outputTyp1Sub = request.getParameter("OUTPUT_TYP1_SUB");
            _outputTyp2Sub = request.getParameter("OUTPUT_TYP2_SUB");
            _outputTyp3Sub = request.getParameter("OUTPUT_TYP3_SUB");
            _testDivName = getTestDivName(db2);
            _getDefaultCostMap = getDefaultCost(db2);
        }

        private String getTestDivName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String rtn = null;

            String sql = " SELECT TESTDIV_NAME FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR='" + _entexamyear + "' AND APPLICANTDIV='" + _applicantDiv + "' AND TESTDIV='" + _testDiv + "' ";
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTDIV_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        //特待生以外の入学申込金、施設設備費
        private Map getDefaultCost(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            Map rtnMap = new HashMap();

            StringBuffer stb = new StringBuffer();
            stb.append("   SELECT ");
            stb.append("       REMARK3 AS ENTRY_FEE, ");
            stb.append("       REMARK6 AS EQUIPMENT_COST ");
            stb.append("   FROM ");
            stb.append("       ENTEXAM_TESTDIV_MST TDM ");
            stb.append("       LEFT JOIN ENTEXAM_TESTDIV_DETAIL_SEQ_MST TDD001 ");
            stb.append("           ON TDD001.ENTEXAMYEAR   = TDM.ENTEXAMYEAR ");
            stb.append("           AND TDD001.APPLICANTDIV = TDM.APPLICANTDIV ");
            stb.append("           AND TDD001.TESTDIV      = TDM.TESTDIV ");
            stb.append("           AND TDD001.SEQ          = '001' ");
            stb.append("   WHERE ");
            stb.append("       TDM.ENTEXAMYEAR      = '" + _entexamyear + "' ");
            stb.append("       AND TDM.APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("       AND TDM.TESTDIV      = '" + _testDiv + "' ");

            final String sql = stb.toString();
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnMap.put("ENTRY_FEE", StringUtils.defaultIfEmpty(rs.getString("ENTRY_FEE"), ""));
                    rtnMap.put("EQUIPMENT_COST", StringUtils.defaultIfEmpty(rs.getString("EQUIPMENT_COST"), ""));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtnMap;
        }
    }
}

// eof

