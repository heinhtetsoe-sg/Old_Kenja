/*
 * 作成日: 2020/10/07
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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL860H {

    private static final Log log = LogFactory.getLog(KNJL860H.class);

    private boolean _hasData;

    private Param _param;

    private static final String ABSENT = "5";

    private static final String SAITAMA = "11";

    private static final String CHIBA = "12";

    private static final String TOKYO = "13";

    private static final String KANAGAWA = "14";

    private static final String MALE = "1";

    private static final String FEMALE = "2";

    private static final String NAIBU_MAX_SCORE = "47";

    private static final String NAIBU_MIN_SCORE = "35";

    private static final String NAIBU_MORE_SCORE = String.valueOf(Integer.parseInt(NAIBU_MAX_SCORE) - 1);

    private static final String NAIBU_LESS_SCORE = String.valueOf(Integer.parseInt(NAIBU_MIN_SCORE) + 1);

    private static final String MAX_SCORE = "45";

    private static final String MIN_SCORE = "35";

    private static final String LESS_SCORE = String.valueOf(Integer.parseInt(MIN_SCORE) + 1);

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
            SQLUtils.whereIn(true, new String[] {});
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String outputDate = sdf.format(new Date());

        if ("1".equals(_param._output)) {
            printAdmissionRecommendationTestInfo(db2, svf, outputDate);
        } else if ("2".equals(_param._output)) {
            printAdmissionRecommendationTestResults(db2, svf, outputDate);
        } else if ("3".equals(_param._output)) {
            printAdmissionRecommendationTestResultsForShool(db2, svf, outputDate);
        } else if ("4".equals(_param._output)) {
            printFsschoolTackSealForPrincipal(db2, svf);
        }

        if (_hasData) {
            svf.VrEndPage();
        }
    }

    /**
     * 推薦入学試験資料（内部資料）を出力する。
     *
     * @param db2
     * @param svf
     * @param outputDate yyyy/MM/dd形式の現在日付
     */
    private void printAdmissionRecommendationTestInfo(final DB2UDB db2, final Vrw32alp svf, String outputDate) {
        svf.VrSetForm("KNJL860H_1.frm", 1);

        svf.VrsOut("DATE", outputDate);
        svf.VrsOut("TITLE", _param._entexamyear + "年度　" + _param._testDivName + "入学試験資料（内部資料）");

        String bosyuNinzu = getBosyuNinzu(db2);
        String shigansyaSu = getShigansyaSu(db2);
        ShigansyaSuKizyungai shigansyaSuKizyungai = getShigansyaSuKizyungaiPref(db2);
        Map<String, Map<String, String>> zyukenNinzuMap = getZyukenNinzuPref(db2);
        String kesekisyaSu = getKesekisyaSu(db2);
        MensetsuFugoukakusyaSu mensetsuFugoukakusyaSu = getMensetsuFugoukakusyaSu(db2);
        Map<String, String> tokutenBunpuMaleHeaderMap = getTokutenBunpuMaleHeaderMap(db2);
        Map<String, Map<String, Map<String, Integer>>> tokutenBunpuMap = getTokutenBunpuMap(db2);

        svf.VrsOut("RECRUIT_NUM", bosyuNinzu);
        svf.VrsOut("HOPE_NUM", shigansyaSu);
        svf.VrsOut("NON_ST1", shigansyaSuKizyungai.getMaleCount());
        svf.VrsOut("NON_ST2", shigansyaSuKizyungai.getFemaleCount());
        svf.VrsOut("NON_ST3", shigansyaSuKizyungai.getCount());

        svf.VrsOut("EXAM_NUM", zyukenNinzuMap.get("ALL").get("ALL"));

        svf.VrsOut("DETAIL_NUM1", zyukenNinzuMap.get(MALE).get("ALL"));
        svf.VrsOut("DETAIL_NUM2", zyukenNinzuMap.get(FEMALE).get("ALL"));
        svf.VrsOut("DETAIL_NUM3", zyukenNinzuMap.get("ALL").get("ALL"));

        svf.VrsOut("ABSENCE_NUM", kesekisyaSu);

        svf.VrsOut("INTERVIEW_NUM1", mensetsuFugoukakusyaSu.getMaleCount());
        svf.VrsOut("INTERVIEW_NUM2", mensetsuFugoukakusyaSu.getFemaleCount());

        svf.VrsOut("KESEKISYA_SU", kesekisyaSu);
        svf.VrsOut("MENSETSU_FUGOUKAKUSYA_SU1", mensetsuFugoukakusyaSu._maleCount);
        svf.VrsOut("MENSETSU_FUGOUKAKUSYA_SU2", mensetsuFugoukakusyaSu._femaleCount);

        String pref1 = tokutenBunpuMaleHeaderMap.get(TOKYO);
        String pref2 = tokutenBunpuMaleHeaderMap.get(SAITAMA);
        String pref3 = tokutenBunpuMaleHeaderMap.get(KANAGAWA);
        String pref4 = tokutenBunpuMaleHeaderMap.get(CHIBA);
        svf.VrsOut("DISTRI_PREF1_1", pref1);
        svf.VrsOut("DISTRI_PREF1_2", pref2);
        svf.VrsOut("DISTRI_PREF1_3", pref3);
        svf.VrsOut("DISTRI_PREF1_4", pref4);
        svf.VrsOut("DISTRI_PREF2_1", pref1);
        svf.VrsOut("DISTRI_PREF2_2", pref2);
        svf.VrsOut("DISTRI_PREF2_3", pref3);
        svf.VrsOut("DISTRI_PREF2_4", pref4);
        svf.VrsOut("DISTRI_PREF3_1", pref1);
        svf.VrsOut("DISTRI_PREF3_2", pref2);
        svf.VrsOut("DISTRI_PREF3_3", pref3);
        svf.VrsOut("DISTRI_PREF3_4", pref4);

        svf.VrsOutn("EXAM_PREF1_1", 1, pref1);
        svf.VrsOutn("EXAM_PREF1_1", 2, pref2);
        svf.VrsOutn("EXAM_PREF1_1", 3, pref3);
        svf.VrsOutn("EXAM_PREF1_1", 4, pref4);
        svf.VrsOutn("EXAM_PREF1_2", 1, pref1);
        svf.VrsOutn("EXAM_PREF1_2", 2, pref2);
        svf.VrsOutn("EXAM_PREF1_2", 3, pref3);
        svf.VrsOutn("EXAM_PREF1_2", 4, pref4);
        svf.VrsOutn("EXAM_PREF1_3", 1, pref1);
        svf.VrsOutn("EXAM_PREF1_3", 2, pref2);
        svf.VrsOutn("EXAM_PREF1_3", 3, pref3);
        svf.VrsOutn("EXAM_PREF1_3", 4, pref4);

        for (String sex : zyukenNinzuMap.keySet()) {
            String filedName1 = MALE.equals(sex) ? "1" : FEMALE.equals(sex) ? "2" : "ALL".equals(sex) ? "3" : "";

            Map<String, String> prefMap = zyukenNinzuMap.get(sex);
            for (String prefCd : prefMap.keySet()) {
                int filedCnt = TOKYO.equals(prefCd) ? 1 : SAITAMA.equals(prefCd) ? 2 : KANAGAWA.equals(prefCd) ? 3 : CHIBA.equals(prefCd) ? 4 : 0;

                String count = prefMap.get(prefCd);
                svf.VrsOutn("DETAIL_PREF_NUM" + filedName1, filedCnt, count);
            }
        }

        for (String sex : tokutenBunpuMap.keySet()) {
            String filedName1 = MALE.equals(sex) ? "1" : FEMALE.equals(sex) ? "2" : "ALL".equals(sex) ? "3" : "";

            int cumulative = 0;
            Map<String, Map<String, Integer>> prefMap = tokutenBunpuMap.get(sex);
            for (String prefCd : prefMap.keySet()) {
                String filedName2 = TOKYO.equals(prefCd) ? "1" : SAITAMA.equals(prefCd) ? "2" : KANAGAWA.equals(prefCd) ? "3" : CHIBA.equals(prefCd) ? "4" : "ALL".equals(prefCd) ? "5" : "";

                Map<String, Integer> scoreMap = prefMap.get(prefCd);
                for (int i = Integer.parseInt(NAIBU_MAX_SCORE); i >= Integer.parseInt(NAIBU_MIN_SCORE); i--) {
                    Integer count = scoreMap.get(String.valueOf(i));
                    int lineCnt = Math.abs(i - (Integer.parseInt(NAIBU_MAX_SCORE) + 1));

                    if (scoreMap.containsKey(String.valueOf(i))) {
                        svf.VrsOutn("DISTRI_NUM" + filedName1 + "_" + filedName2, lineCnt, count.toString());
                        cumulative += count.intValue();
                    }

                    if ("ALL".equals(prefCd)) {
                        svf.VrsOutn("DISTRI_NUM" + filedName1 + "_6", lineCnt, String.valueOf(cumulative));
                    }
                }

                Integer count = scoreMap.get("ALL");
                count = count == null ? 0 : count;
                int lineCnt = Integer.parseInt(NAIBU_MAX_SCORE) - Integer.parseInt(NAIBU_MIN_SCORE) + 2;
                svf.VrsOutn("DISTRI_NUM" + filedName1 + "_" + filedName2, lineCnt, String.valueOf(count));
            }
        }

        _hasData = true;
    }

    /**
     * 推薦入学試験結果（資料）を出力する。
     *
     * @param db2
     * @param svf
     * @param outputDate yyyy/MM/dd形式の現在日付
     */
    private void printAdmissionRecommendationTestResults(final DB2UDB db2, final Vrw32alp svf, String outputDate) {
        svf.VrSetForm("KNJL860H_2.frm", 1);

        svf.VrsOut("DATE", outputDate);
        svf.VrsOut("TITLE", _param._entexamyear + "年度　" + _param._testDivName + "入学試験結果（資料）");
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);

        String bosyuNinzu = getBosyuNinzu(db2);
        String shigansyaSu = getShigansyaSu(db2);
        String shigansyaSuKizyungai = getShigansyaSuKizyungai(db2);
        String zyukenNinzu = getZyukenNinzu(db2);
        String goukakusyaSu = getGoukakusyaSu(db2);
        Map<String, Integer> hyouteiGoukeiBunpuMap = getHyouteiGoukeiBunpuMap(db2);

        svf.VrsOut("RECRUIT_NUM", bosyuNinzu);
        svf.VrsOut("HOPE_NUM", shigansyaSu);
        svf.VrsOut("EXAM_NUM", zyukenNinzu);
        svf.VrsOut("NON_ST_NUM", shigansyaSuKizyungai);

        svf.VrsOut("PASS_NUM", goukakusyaSu);

        int goukei = 0;
        for (int i = Integer.parseInt(MAX_SCORE); i >= Integer.parseInt(MIN_SCORE); i--) {
            String score = String.valueOf(i);
            int lineCnt = Math.abs(i - (Integer.parseInt(MAX_SCORE) + 1));

            Integer count = hyouteiGoukeiBunpuMap.get(score);
            count = count == null ? count = 0 : count;
            svf.VrsOutn("DISTRI_NUM1_1", lineCnt, count.toString());
            goukei += count.intValue();
        }
        svf.VrsOutn("DISTRI_NUM1_1", Integer.parseInt(MAX_SCORE) - Integer.parseInt(MIN_SCORE) + 2, String.valueOf(goukei));

        _hasData = true;
    }

    /**
     * 推薦入学試験結果（学校宛て）を出力する。
     *
     * @param db2
     * @param svf
     * @param outputDate yyyy/MM/dd形式の現在日付
     */
    private void printAdmissionRecommendationTestResultsForShool(final DB2UDB db2, final Vrw32alp svf, String outputDate) {
        svf.VrSetForm("KNJL860H_3.frm", 1);

        final int maxLine = 10;
        int lineCnt = 1;
        Map<String, List<AdmissionRecommendationTestResultsForShoolData>> artrfsMap = getArtrfsMap(db2);
        String principalName = getPrincipalName(db2);

        for(List<AdmissionRecommendationTestResultsForShoolData> artrfsList : artrfsMap.values()) {
            String finschoolName = artrfsList.get(0)._finschoolName;
            setArtrfsTitle(svf, outputDate, finschoolName, principalName);

            for (AdmissionRecommendationTestResultsForShoolData artrfs : artrfsList) {
                // 改ページの制御
                if (lineCnt > maxLine) {
                    svf.VrEndPage();
                    lineCnt = 1;
                    setArtrfsTitle(svf, outputDate, finschoolName, principalName);
                }

                svf.VrsOutn("EXAM_NO", lineCnt, artrfs._examNo);

                final int nameByte = KNJ_EditEdit.getMS932ByteLength(artrfs._name);
                final String nameFieldStr = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
                svf.VrsOutn("NAME" + nameFieldStr, lineCnt, artrfs._name);

                final String judgement = "1".equals(artrfs._judgement) ? "合格" : "不合格";
                svf.VrsOutn("RESULT", lineCnt, judgement);

                lineCnt++;
                _hasData = true;
            }

            svf.VrEndPage();
            lineCnt = 1;
        }
    }

    private void setArtrfsTitle(final Vrw32alp svf, final String outputDate, final String finschoolName, final String principalName) {
        svf.VrsOut("DATE", outputDate);

        svf.VrsOut("FINSCHOOL_NAME", StringUtils.defaultString(finschoolName) + "長殿");
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
        svf.VrsOut("PRINCIPAL_NAME", principalName);
    }

    /**
     * 出身学校長宛名タックシールを出力する。
     *
     * @param db2
     * @param svf
     */
    private void printFsschoolTackSealForPrincipal(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL860H_4.frm", 1);

        Map<String, List<FsschoolTackSealForPrincipalData>> ftsfpMap = getFtsfpList(db2);

        int FSSCHOOL_TACK_SEAL_FOR_PRINCIPAL_LINE_MAX = 5;
        int FSSCHOOL_TACK_SEAL_FOR_PRINCIPAL_COL_MAX = 2;
        int lineMax = FSSCHOOL_TACK_SEAL_FOR_PRINCIPAL_LINE_MAX * FSSCHOOL_TACK_SEAL_FOR_PRINCIPAL_COL_MAX;
        int EXAM_LINE_MAX = 2;
        int EXAM_LINE_CNT_MAX = 5;
        int lineCnt = 0;

        for (List<FsschoolTackSealForPrincipalData> ftsfpList : ftsfpMap.values()) {
            String examNoList = "";
            int examNoLine = 1;
            int examCnt = 1;

            for (FsschoolTackSealForPrincipalData ftsfdData : ftsfpList) {
                // 次のセルに書き込むかの制御
                if (examCnt > EXAM_LINE_CNT_MAX) {
                    if (examNoLine >= EXAM_LINE_MAX) {
                        examNoList = "";
                        examNoLine = 1;
                        examCnt = 1;
                        lineCnt++;
                    } else {
                        examNoList = "";
                        examNoLine++;
                        examCnt = 1;
                    }
                }

                // 改ページの制御
                if (lineCnt >= lineMax) {
                    svf.VrEndPage();
                    lineCnt = 0;
                }

                int lineFiledCnt = (lineCnt / FSSCHOOL_TACK_SEAL_FOR_PRINCIPAL_COL_MAX) + 1;
                String lineFiledStr = String.valueOf((lineCnt % FSSCHOOL_TACK_SEAL_FOR_PRINCIPAL_COL_MAX) + 1);

                String zipCd = "〒" + StringUtils.defaultString(ftsfdData._finschoolZipCd);
                svf.VrsOutn("ZIP_NO" + lineFiledStr, lineFiledCnt, zipCd);

                final String addr = StringUtils.defaultString(ftsfdData._finschoolAddr1) +  StringUtils.defaultString(ftsfdData._finschoolAddr2);
                final int address1Byte = KNJ_EditEdit.getMS932ByteLength(addr);
                final String address1FieldStr = address1Byte > 50 ? "3" : address1Byte > 40 ? "2" : "1";
                svf.VrsOutn("ADDR" + lineFiledStr + "_1_" + address1FieldStr, lineFiledCnt, addr);

                String name = StringUtils.defaultString(ftsfdData._finschoolName) + "　御中";
                final int nameByte = KNJ_EditEdit.getMS932ByteLength(name);
                final String nameFieldStr = nameByte > 50 ? "3" : nameByte > 40 ? "2" : "1";
                svf.VrsOutn("NAME" + lineFiledStr + "_" + nameFieldStr, lineFiledCnt, name);

                examNoList += "(" + ftsfdData._examNo + ")";
                svf.VrsOutn("EXAM_NO" + lineFiledStr + "_" + String.valueOf(examNoLine), lineFiledCnt, examNoList);

                examCnt++;
                _hasData = true;
            }

            lineCnt++;
        }
    }

    private String getBosyuNinzu(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        String rtn = null;

        try {
            final String bosyuNinzuSql = getBosyuNinzuSql();
            log.debug(" sql =" + bosyuNinzuSql);
            ps = db2.prepareStatement(bosyuNinzuSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                rtn = rs.getString("COUNT");
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return rtn;
    }

    private String getShigansyaSu(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        String rtn = null;

        try {
            final String shigansyaSuSql = getShigansyaSuSql();
            log.debug(" sql =" + shigansyaSuSql);
            ps = db2.prepareStatement(shigansyaSuSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                rtn = rs.getString("COUNT");
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return rtn;
    }

    private ShigansyaSuKizyungai getShigansyaSuKizyungaiPref(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ShigansyaSuKizyungai shigansyaSuKizyungai = new ShigansyaSuKizyungai();
        Map<String, String> shigansyaSuKizyungaiMap = new LinkedHashMap<String, String>();

        try {
            final String shigansyaSuKizyungaiPrefSql = getShigansyaSuKizyungaiPrefSql();
            log.debug(" sql =" + shigansyaSuKizyungaiPrefSql);
            ps = db2.prepareStatement(shigansyaSuKizyungaiPrefSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String sex = rs.getString("SEX");
                final String count = rs.getString("COUNT");

                shigansyaSuKizyungaiMap.put(sex, count);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        shigansyaSuKizyungai.setMaleCount(StringUtils.defaultString(shigansyaSuKizyungaiMap.get(MALE), "0"));
        shigansyaSuKizyungai.setFemaleCount(StringUtils.defaultString(shigansyaSuKizyungaiMap.get(FEMALE), "0"));

        return shigansyaSuKizyungai;
    }

    private Map<String, Map<String, String>> getZyukenNinzuPref(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, Map<String, String>> zyukenNinzuMap = new LinkedHashMap<String, Map<String, String>>();
        Map<String, String> prefMap = new LinkedHashMap<String, String>();
        String sexCount = "0";

        try {
            final String zyukenNinzuSql = getZyukenNinzuPrefSql();
            log.debug(" sql =" + zyukenNinzuSql);
            ps = db2.prepareStatement(zyukenNinzuSql);
            rs = ps.executeQuery();

            prefMap.put("ALL", "0");
            zyukenNinzuMap.put("ALL", prefMap);

            prefMap = new LinkedHashMap<String, String>();
            prefMap.put("ALL", "0");
            zyukenNinzuMap.put(MALE, prefMap);

            prefMap = new LinkedHashMap<String, String>();
            prefMap.put("ALL", "0");
            zyukenNinzuMap.put(FEMALE, prefMap);

            while (rs.next()) {
                String sex = rs.getString("SEX");
                String prefCd = rs.getString("PREF_CD");
                String count = StringUtils.defaultIfEmpty(rs.getString("COUNT"), "0");

                if (zyukenNinzuMap.containsKey(sex)) {
                    prefMap = zyukenNinzuMap.get(sex);
                } else {
                    prefMap = new LinkedHashMap<String, String>();
                    zyukenNinzuMap.put(sex, prefMap);
                }
                prefMap.put(prefCd, count);

                sexCount = String.valueOf(Integer.parseInt(prefMap.get("ALL")) + Integer.parseInt(count));
                prefMap.put("ALL", sexCount);

                prefMap = zyukenNinzuMap.get("ALL");
                if (prefMap.containsKey(prefCd)) {
                    count = String.valueOf(Integer.parseInt(prefMap.get(prefCd)) + Integer.parseInt(count));
                }
                prefMap.put(prefCd, count);
            }

            String mailCount = zyukenNinzuMap.get(MALE).get("ALL");
            String femailCount = zyukenNinzuMap.get(FEMALE).get("ALL");
            String allCount = String.valueOf(Integer.parseInt(mailCount) + Integer.parseInt(femailCount));
            zyukenNinzuMap.get("ALL").put("ALL", allCount);
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return zyukenNinzuMap;
    }

    private String getKesekisyaSu(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        String rtn = null;

        try {
            final String kesekisyaSuSql = getKesekisyaSuSql();
            log.debug(" sql =" + kesekisyaSuSql);
            ps = db2.prepareStatement(kesekisyaSuSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                rtn = rs.getString("COUNT");
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return rtn;
    }

    private MensetsuFugoukakusyaSu getMensetsuFugoukakusyaSu(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        MensetsuFugoukakusyaSu mensetsuFugoukakusyaSu = new MensetsuFugoukakusyaSu();
        Map<String, String> mensetsuFugoukakusyaSuMap = new LinkedHashMap<String, String>();

        try {
            final String mensetsuFugoukasyaSuSql = getMensetsuFugoukasyaSuSql();
            log.debug(" sql =" + mensetsuFugoukasyaSuSql);
            ps = db2.prepareStatement(mensetsuFugoukasyaSuSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String sex = rs.getString("SEX");
                final String count = rs.getString("COUNT");

                mensetsuFugoukakusyaSuMap.put(sex,  count);
            }

            mensetsuFugoukakusyaSu.setMaleCount(StringUtils.defaultIfEmpty(mensetsuFugoukakusyaSuMap.get(MALE), "0"));
            mensetsuFugoukakusyaSu.setFemaleCount(StringUtils.defaultIfEmpty(mensetsuFugoukakusyaSuMap.get(FEMALE), "0"));
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return mensetsuFugoukakusyaSu;
    }

    private Map<String, String> getTokutenBunpuMaleHeaderMap(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, String> tokutenBunpuHeaderMap = new LinkedHashMap<String, String>();

        try {
            final String tokutenBunpuSql = getTokutenBunpuHeaderSql();
            log.debug(" sql =" + tokutenBunpuSql);
            ps = db2.prepareStatement(tokutenBunpuSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                String prefCd = rs.getString("PREF_CD");
                String prefName = rs.getString("PREF_NAME");

                tokutenBunpuHeaderMap.put(prefCd, prefName);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return tokutenBunpuHeaderMap;
    }

    private Map<String, Map<String, Map<String, Integer>>> getTokutenBunpuMap(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, Map<String, Map<String, Integer>>> tokutenBunpuMap = new LinkedHashMap<String, Map<String, Map<String, Integer>>>();
        Map<String, Map<String, Integer>> prefMap = null;
        Map<String, Integer> scoreMap = null;

        try {
            final String tokutenBunpuSql = getTokutenBunpuSql();
            log.debug(" sql =" + tokutenBunpuSql);
            ps = db2.prepareStatement(tokutenBunpuSql);
            rs = ps.executeQuery();

            prefMap = new LinkedHashMap<String, Map<String, Integer>>();
            prefMap.put("ALL", new LinkedHashMap<String, Integer>());
            tokutenBunpuMap.put(MALE, prefMap);

            prefMap = new LinkedHashMap<String, Map<String, Integer>>();
            prefMap.put("ALL", new LinkedHashMap<String, Integer>());
            tokutenBunpuMap.put(FEMALE, prefMap);

            prefMap = new LinkedHashMap<String, Map<String, Integer>>();
            prefMap.put("ALL", new LinkedHashMap<String, Integer>());
            tokutenBunpuMap.put("ALL", prefMap);

            while (rs.next()) {
                String sex = rs.getString("SEX");
                String prefCd = rs.getString("PREF_CD");
                String score = rs.getString("SCORE");
                Integer count = rs.getInt("COUNT");

                prefMap = tokutenBunpuMap.get(sex);
                if (prefMap.containsKey(prefCd)) {
                    scoreMap = prefMap.get(prefCd);
                } else {
                    scoreMap = new LinkedHashMap<String, Integer>();
                    prefMap.put(prefCd, scoreMap);
                }
                scoreMap.put(score, count);

                // 計行の対応
                putPrefALL(scoreMap, count);

                // 計列の対応
                scoreMap = putScoreALL(prefMap, score, count);

                // 計列の計行の対応
                putPrefALL(scoreMap, count);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return tokutenBunpuMap;
    }

    private Map<String, Integer> putScoreALL(Map<String, Map<String, Integer>> prefMap, String score, Integer count) {
        Integer countScoreALL = 0;

        Map<String, Integer> scoreMap = prefMap.get("ALL");
        if (scoreMap.containsKey(score)) {
            countScoreALL = count.intValue() + scoreMap.get(score).intValue();
        } else {
            countScoreALL = count;
        }
        scoreMap.put(score, countScoreALL);

        return scoreMap;
    }

    private void putPrefALL(Map<String, Integer> scoreMap, Integer count) {
        Integer countPrefALL = 0;

        if (scoreMap.containsKey("ALL")) {
            countPrefALL = count.intValue() + scoreMap.get("ALL").intValue();
        } else {
            countPrefALL = count;
        }

        scoreMap.put("ALL", countPrefALL);
    }

    private String getZyukenNinzu(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        String rtn = null;

        try {
            final String zyukenNinzuSql = getZyukenNinzuSql();
            log.debug(" sql =" + zyukenNinzuSql);
            ps = db2.prepareStatement(zyukenNinzuSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                rtn = StringUtils.defaultIfEmpty(rs.getString("COUNT"), "0");
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return rtn;
    }

    private String getShigansyaSuKizyungai(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        String rtn = null;

        try {
            final String shigansyaSuKizyungaiSql = getShigansyaSuKizyungaiSql();
            log.debug(" sql =" + shigansyaSuKizyungaiSql);
            ps = db2.prepareStatement(shigansyaSuKizyungaiSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                rtn = rs.getString("COUNT");
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return rtn;
    }

    private String getGoukakusyaSu(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        String rtn = null;

        try {
            final String goukakusyaSuSql = getGoukakusyaSuSql();
            log.debug(" sql =" + goukakusyaSuSql);
            ps = db2.prepareStatement(goukakusyaSuSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                rtn = rs.getString("COUNT");
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return rtn;
    }

    private Map<String, Integer> getHyouteiGoukeiBunpuMap(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, Integer> hyouteiGoukeiBunpuMap = new LinkedHashMap<String, Integer>();

        try {
            final String hyouteiGoukeiBunpuSql = getHyouteiGoukeiBunpuSql();
            log.debug(" sql =" + hyouteiGoukeiBunpuSql);
            ps = db2.prepareStatement(hyouteiGoukeiBunpuSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                String score = rs.getString("SCORE");
                Integer count = rs.getInt("COUNT");

                hyouteiGoukeiBunpuMap.put(score, count);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return hyouteiGoukeiBunpuMap;
    }

    private Map<String, List<AdmissionRecommendationTestResultsForShoolData>> getArtrfsMap(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, List<AdmissionRecommendationTestResultsForShoolData>> artrfsMap = new LinkedHashMap<String, List<AdmissionRecommendationTestResultsForShoolData>>();
        List<AdmissionRecommendationTestResultsForShoolData> artrfsList = new ArrayList<AdmissionRecommendationTestResultsForShoolData>();

        try {
            final String artrfsSql = getArtrfsSql();
            log.debug(" sql =" + artrfsSql);
            ps = db2.prepareStatement(artrfsSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                String finschoolCd = rs.getString("FINSCHOOLCD");
                String finschoolName = rs.getString("FINSCHOOL_NAME");
                String examNo = rs.getString("EXAMNO");
                String name = rs.getString("NAME");
                String judgement = rs.getString("JUDEGEMENT");

                if (artrfsMap.containsKey(finschoolCd)) {
                    artrfsList = artrfsMap.get(finschoolCd);
                } else {
                    artrfsList = new ArrayList<AdmissionRecommendationTestResultsForShoolData>();
                    artrfsMap.put(finschoolCd, artrfsList);
                }

                AdmissionRecommendationTestResultsForShoolData artrfs = new AdmissionRecommendationTestResultsForShoolData(finschoolName, examNo, name, judgement);
                artrfsList.add(artrfs);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return artrfsMap;
    }

    private Map<String, List<FsschoolTackSealForPrincipalData>> getFtsfpList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, List<FsschoolTackSealForPrincipalData>> ftsfpMap = new LinkedHashMap<String, List<FsschoolTackSealForPrincipalData>>();
        List<FsschoolTackSealForPrincipalData> ftsfpList = new ArrayList<FsschoolTackSealForPrincipalData>();

        try {
            final String ftsfpSql = getFtsfpSql();
            log.debug(" sql =" + ftsfpSql);
            ps = db2.prepareStatement(ftsfpSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                String finschoolZipCd = rs.getString("FINSCHOOL_ZIPCD");
                String finschoolAddr1 = rs.getString("FINSCHOOL_ADDR1");
                String finschoolAddr2 = rs.getString("FINSCHOOL_ADDR2");
                String finschoolCd = rs.getString("FINSCHOOLCD");
                String finschoolName = rs.getString("FINSCHOOL_NAME");
                String examNo = rs.getString("EXAMNO");

                if (ftsfpMap.containsKey(finschoolCd)) {
                    ftsfpList = ftsfpMap.get(finschoolCd);
                } else {
                    ftsfpList = new ArrayList<FsschoolTackSealForPrincipalData>();
                    ftsfpMap.put(finschoolCd, ftsfpList);
                }

                FsschoolTackSealForPrincipalData ftsfp = new FsschoolTackSealForPrincipalData(finschoolZipCd, finschoolAddr1, finschoolAddr2, finschoolName, examNo);
                ftsfpList.add(ftsfp);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return ftsfpMap;
    }

    private String getPrincipalName(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        String rtn = null;

        try {
            final String principalNameSql = getPrincipalNameSql();
            log.debug(" sql =" + principalNameSql);
            ps = db2.prepareStatement(principalNameSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                String jobName = rs.getString("JOB_NAME");
                String principalName = rs.getString("PRINCIPAL_NAME");

                rtn = StringUtils.defaultString(jobName) + "　" + StringUtils.defaultString(principalName);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return rtn;
    }

    private String getBosyuNinzuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VALUE(REMARK5, 0) + VALUE(REMARK6, 0) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_TESTDIV_DETAIL_MST ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND TESTDIV = '" + _param._testDiv + "' ");
        return stb.toString();
    }

    private String getShigansyaSuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     COUNT(EXAMNO) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND TESTDIV = '" + _param._testDiv + "' ");
        return stb.toString();
    }

    private String getShigansyaSuKizyungaiPrefSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.SEX, ");
        stb.append("     COUNT(BASE.EXAMNO) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND CONF.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND CONF.EXAMNO = BASE.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND (CONF.TOTAL_ALL < 35 OR CONF.TOTAL5 < 20) ");
        stb.append(" GROUP BY ");
        stb.append("     BASE.SEX ");
        return stb.toString();
    }

    private String getZyukenNinzuPrefSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.SEX, ");
        stb.append("     PREF.PREF_CD, ");
        stb.append("     PREF.PREF_NAME, ");
        stb.append("     COUNT(BASE.EXAMNO) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FS ON FS.FINSCHOOLCD = BASE.FS_CD ");
        stb.append("     LEFT JOIN PREF_MST PREF ON PREF.PREF_CD = FS.FINSCHOOL_PREF_CD ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND VALUE(BASE.JUDGEMENT, '0') <> '" + ABSENT + "' ");
        stb.append("     AND PREF.PREF_CD IN ('" + SAITAMA + "', '" + CHIBA + "', '" + TOKYO + "', '" + KANAGAWA + "') ");
        stb.append(" GROUP BY ");
        stb.append("     BASE.SEX, ");
        stb.append("     PREF.PREF_CD, ");
        stb.append("     PREF.PREF_NAME ");
        stb.append(" ORDER BY ");
        stb.append("     PREF.PREF_CD ");
        return stb.toString();
    }

    private String getKesekisyaSuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     COUNT(EXAMNO) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND JUDGEMENT = '" + ABSENT + "' ");
        return stb.toString();
    }

    private String getMensetsuFugoukasyaSuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.SEX, ");
        stb.append("     COUNT(BASE.EXAMNO) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ON DETAIL.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND DETAIL.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND DETAIL.EXAMNO = BASE.EXAMNO ");
        stb.append("     AND DETAIL.SEQ = '033' ");
        stb.append("     AND DETAIL.REMARK2 = '1' ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND VALUE(BASE.JUDGEMENT, '0') <> '" + ABSENT + "' ");
        stb.append(" GROUP BY ");
        stb.append("     BASE.SEX ");
        return stb.toString();
    }

    private String getTokutenBunpuHeaderSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     PREF_CD, ");
        stb.append("     PREF_NAME ");
        stb.append(" FROM ");
        stb.append("     PREF_MST ");
        stb.append(" WHERE ");
        stb.append("     PREF_CD IN ('" + SAITAMA + "', '" + CHIBA + "', '" + TOKYO + "', '" + KANAGAWA + "') ");
        return stb.toString();
    }

    private String getTokutenBunpuSql() {
        final StringBuffer stb = new StringBuffer();

        // 都道府県、内申点毎に人数を集計するSQL
        stb.append(" WITH EXAMNO_DAT AS ( ");
        stb.append("     SELECT ");
        stb.append("         BASE.SEX, ");
        stb.append("         PREF.PREF_CD, ");
        stb.append("         BASE.EXAMNO, ");
        stb.append("         VALUE(SUM(CONF.TOTAL_ALL), 0) + VALUE(SUM(RECEPT_D.REMARK3), 0) + VALUE(SUM(BASE_D.REMARK10), 0) AS SCORE ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("         LEFT JOIN FINSCHOOL_MST FS ON FS.FINSCHOOLCD = BASE.FS_CD ");
        stb.append("         LEFT JOIN PREF_MST PREF ON PREF.PREF_CD = FS.FINSCHOOL_PREF_CD ");
        stb.append("         LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("         AND CONF.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("         AND CONF.EXAMNO = BASE.EXAMNO ");
        stb.append("         LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("         AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("         AND RECEPT.TESTDIV = BASE.TESTDIV ");
        stb.append("         AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("         AND RECEPT.EXAMNO = BASE.EXAMNO ");
        stb.append("         LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D ON RECEPT_D.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("         AND RECEPT_D.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("         AND RECEPT_D.TESTDIV = RECEPT.TESTDIV ");
        stb.append("         AND RECEPT_D.EXAM_TYPE = RECEPT.EXAM_TYPE ");
        stb.append("         AND RECEPT_D.RECEPTNO = RECEPT.RECEPTNO ");
        stb.append("         AND RECEPT_D.SEQ = '009' ");
        stb.append("         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D ON BASE_D.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("         AND BASE_D.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("         AND BASE_D.EXAMNO = BASE.EXAMNO ");
        stb.append("         AND BASE_D.SEQ = '031' ");
        stb.append("     WHERE ");
        stb.append("         BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("         AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("         AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("         AND VALUE(BASE.JUDGEMENT, '0') <> '" + ABSENT + "' ");
        stb.append("         AND PREF.PREF_CD IN ('" + SAITAMA + "', '" + CHIBA + "', '" + TOKYO + "', '" + KANAGAWA + "') ");
        stb.append("     GROUP BY ");
        stb.append("         BASE.SEX, ");
        stb.append("         PREF.PREF_CD, ");
        stb.append("         BASE.EXAMNO ");
        stb.append("     ORDER BY ");
        stb.append("         BASE.SEX, ");
        stb.append("         PREF.PREF_CD, ");
        stb.append("         BASE.EXAMNO, ");
        stb.append("         SCORE DESC ");
        stb.append(" ), ");
        stb.append(" PREF_DAT AS ( ");
        stb.append("     SELECT ");
        stb.append("         SEX, ");
        stb.append("         PREF_CD, ");
        stb.append("         SCORE, ");
        stb.append("         COUNT(EXAMNO) AS COUNT ");
        stb.append("     FROM ");
        stb.append("         EXAMNO_DAT ");
        stb.append("     GROUP BY ");
        stb.append("         SEX, ");
        stb.append("         PREF_CD, ");
        stb.append("         SCORE ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         'ALL' AS SEX, ");
        stb.append("         PREF_CD, ");
        stb.append("         SCORE, ");
        stb.append("         COUNT(EXAMNO) AS COUNT ");
        stb.append("     FROM ");
        stb.append("         EXAMNO_DAT ");
        stb.append("     GROUP BY ");
        stb.append("         PREF_CD, ");
        stb.append("         SCORE ");
        stb.append("     ORDER BY ");
        stb.append("         SEX, ");
        stb.append("         PREF_CD, ");
        stb.append("         SCORE DESC ");
        stb.append(" ), ");

        // 都道府県毎で内申点が47点以上の人数を集計するSQL
        stb.append(" MORESCORE_PREF AS ( ");
        stb.append("     SELECT ");
        stb.append("         SEX, ");
        stb.append("         PREF_CD, ");
        stb.append("         " + NAIBU_MAX_SCORE + " AS SCORE, ");
        stb.append("         SUM(COUNT) AS COUNT ");
        stb.append("     FROM ");
        stb.append("         PREF_DAT ");
        stb.append("     WHERE ");
        stb.append("         SCORE > " + NAIBU_MORE_SCORE + " ");
        stb.append("     GROUP BY ");
        stb.append("         SEX, ");
        stb.append("         PREF_CD ");
        stb.append("     ORDER BY ");
        stb.append("         SEX, ");
        stb.append("         PREF_CD ");
        stb.append(" ), ");

        // 都道府県、内申点毎で内申点が36点～46点の人数を集計するSQL
        stb.append(" MORE_LESSSCORE_PREF AS ( ");
        stb.append("     SELECT ");
        stb.append("         SEX, ");
        stb.append("         PREF_CD, ");
        stb.append("         SCORE, ");
        stb.append("         COUNT ");
        stb.append("     FROM ");
        stb.append("         PREF_DAT ");
        stb.append("     WHERE ");
        stb.append("         SCORE BETWEEN " + NAIBU_LESS_SCORE + " AND " + NAIBU_MORE_SCORE + " ");
        stb.append("     ORDER BY ");
        stb.append("         SEX, ");
        stb.append("         PREF_CD, ");
        stb.append("         SCORE DESC ");
        stb.append(" ), ");

        // 都道府県毎で内申点が35点以下の人数を集計するSQL
        stb.append(" LESSSCORE_PREF AS ( ");
        stb.append("     SELECT ");
        stb.append("         SEX, ");
        stb.append("         PREF_CD, ");
        stb.append("         " + NAIBU_MIN_SCORE + " AS SCORE, ");
        stb.append("         SUM(COUNT) AS COUNT ");
        stb.append("     FROM ");
        stb.append("         PREF_DAT ");
        stb.append("     WHERE ");
        stb.append("         SCORE < " + NAIBU_LESS_SCORE + " ");
        stb.append("     GROUP BY ");
        stb.append("         SEX, ");
        stb.append("         PREF_CD ");
        stb.append("     ORDER BY ");
        stb.append("         SEX, ");
        stb.append("         PREF_CD ");
        stb.append(" ) ");

        stb.append(" SELECT SEX, PREF_CD, SCORE, COUNT FROM MORESCORE_PREF ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT SEX, PREF_CD, SCORE, COUNT FROM MORE_LESSSCORE_PREF ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT SEX, PREF_CD, SCORE, COUNT FROM LESSSCORE_PREF ");
        stb.append(" ORDER BY SEX, PREF_CD, SCORE DESC ");
        return stb.toString();
    }

    private String getShigansyaSuKizyungaiSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     COUNT(BASE.EXAMNO) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND CONF.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND CONF.EXAMNO = BASE.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND (CONF.TOTAL_ALL < 35 OR CONF.TOTAL5 < 20) ");
        return stb.toString();
    }

    private String getZyukenNinzuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     COUNT(EXAMNO) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND VALUE(JUDGEMENT, '0') <> '" + ABSENT + "' ");
        return stb.toString();
    }

    private String getGoukakusyaSuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     COUNT(EXAMNO) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     INNER JOIN ENTEXAM_SETTING_MST L013 ON L013.SETTING_CD = 'L013' ");
        stb.append("     AND L013.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND L013.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND L013.SEQ = BASE.JUDGEMENT ");
        stb.append("     AND L013.NAMESPARE1 = '1' ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        return stb.toString();
    }

    private String getHyouteiGoukeiBunpuSql() {
        final StringBuffer stb = new StringBuffer();

        // 都道府県、内申点毎に人数を集計するSQL
        stb.append(" WITH EXAMNO_DAT AS ( ");
        stb.append("     SELECT ");
        stb.append("         BASE.EXAMNO, ");
        stb.append("         VALUE(SUM(CONF.TOTAL_ALL), 0) AS SCORE ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("         LEFT JOIN FINSCHOOL_MST FS ON FS.FINSCHOOLCD = BASE.FS_CD ");
        stb.append("         LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("         AND CONF.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("         AND CONF.EXAMNO = BASE.EXAMNO ");
        stb.append("         LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("         AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("         AND RECEPT.TESTDIV = BASE.TESTDIV ");
        stb.append("         AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("         AND RECEPT.EXAMNO = BASE.EXAMNO ");
        stb.append("         LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_D ON RECEPT_D.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("         AND RECEPT_D.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("         AND RECEPT_D.TESTDIV = RECEPT.TESTDIV ");
        stb.append("         AND RECEPT_D.EXAM_TYPE = RECEPT.EXAM_TYPE ");
        stb.append("         AND RECEPT_D.RECEPTNO = RECEPT.RECEPTNO ");
        stb.append("         AND RECEPT_D.SEQ = '009' ");
        stb.append("         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D ON BASE_D.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("         AND BASE_D.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("         AND BASE_D.EXAMNO = BASE.EXAMNO ");
        stb.append("         AND BASE_D.SEQ = '031' ");
        stb.append("     WHERE ");
        stb.append("         BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("         AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("         AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("         AND VALUE(BASE.JUDGEMENT, '0') <> '" + ABSENT + "' ");
        stb.append("     GROUP BY ");
        stb.append("         BASE.SEX, ");
        stb.append("         BASE.EXAMNO ");
        stb.append("     ORDER BY ");
        stb.append("         BASE.EXAMNO, ");
        stb.append("         SCORE DESC ");
        stb.append(" ), ");
        stb.append(" PREF_DAT AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCORE, ");
        stb.append("         COUNT(EXAMNO) AS COUNT ");
        stb.append("     FROM ");
        stb.append("         EXAMNO_DAT ");
        stb.append("     GROUP BY ");
        stb.append("         SCORE ");
        stb.append("     ORDER BY ");
        stb.append("         SCORE DESC ");
        stb.append(" ), ");

        // 内申点毎で内申点が36点～45点の人数を集計するSQL
        stb.append(" MAX_LESSSCORE AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCORE, ");
        stb.append("         COUNT ");
        stb.append("     FROM ");
        stb.append("         PREF_DAT ");
        stb.append("     WHERE ");
        stb.append("         SCORE BETWEEN " + LESS_SCORE + " AND " + MAX_SCORE + " ");
        stb.append("     ORDER BY ");
        stb.append("         SCORE DESC ");
        stb.append(" ), ");

        // 内申点が35点以下の人数を集計するSQL
        stb.append(" MINSCORE AS ( ");
        stb.append("     SELECT ");
        stb.append("         " + MIN_SCORE + " AS SCORE, ");
        stb.append("         SUM(COUNT) AS COUNT ");
        stb.append("     FROM ");
        stb.append("         PREF_DAT ");
        stb.append("     WHERE ");
        stb.append("         SCORE <= " + MIN_SCORE + " ");
        stb.append(" ) ");

        stb.append(" SELECT SCORE, COUNT FROM MAX_LESSSCORE ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT SCORE, COUNT FROM MINSCORE ");
        stb.append(" ORDER BY SCORE DESC ");
        return stb.toString();
    }

    private String getArtrfsSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     FS.FINSCHOOLCD, ");
        stb.append("     FS.FINSCHOOL_NAME, ");
        stb.append("     EXAMNO, ");
        stb.append("     NAME, ");
        stb.append("     L013.NAMESPARE1 AS JUDEGEMENT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FS ON FS.FINSCHOOLCD = BASE.FS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L013 ON L013.SETTING_CD = 'L013' ");
        stb.append("     AND L013.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND L013.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND L013.SEQ = BASE.JUDGEMENT ");
        stb.append("     AND L013.NAMESPARE1 = '1' ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     FS.FINSCHOOLCD, ");
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    private String getPrincipalNameSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     JOB_NAME, ");
        stb.append("     PRINCIPAL_NAME ");
        stb.append(" FROM ");
        stb.append("     CERTIF_SCHOOL_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._entexamyear + "' ");
        // 入試制度（APPLICANTDIV） が 1:中学、2:高校 の場合のみ名称を取得する。
        if ("1".equals(_param._applicantDiv)) {
            stb.append("     AND CERTIF_KINDCD = '105' ");
        } else if ("2".equals(_param._applicantDiv)) {
            stb.append("     AND CERTIF_KINDCD = '106' ");
        }
        return stb.toString();
    }

    private String getFtsfpSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     FS.FINSCHOOLCD, ");
        stb.append("     FS.FINSCHOOL_ZIPCD, ");
        stb.append("     FS.FINSCHOOL_ADDR1, ");
        stb.append("     FS.FINSCHOOL_ADDR2, ");
        stb.append("     FS.FINSCHOOL_NAME, ");
        stb.append("     BASE.EXAMNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FS ON FS.FINSCHOOLCD = BASE.FS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L013 ON L013.SETTING_CD = 'L013' ");
        stb.append("     AND L013.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND L013.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND L013.SEQ = BASE.JUDGEMENT ");
        stb.append("     AND L013.NAMESPARE1 = '1' ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     FS.FINSCHOOLCD, ");
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class ShigansyaSuKizyungai {
        private String _maleCount;
        private String _femaleCount;

        ShigansyaSuKizyungai () {}

        void setMaleCount(String maleCount) {
            _maleCount = maleCount;
        }

        String getMaleCount() {
            return _maleCount;
        }

        void setFemaleCount(String femaleCount) {
            _femaleCount = femaleCount;
        }

        String getFemaleCount() {
            return _femaleCount;
        }

        String getCount() {
            return String.valueOf(Integer.parseInt(_maleCount) + Integer.parseInt(_femaleCount));
        }
    }

    private class MensetsuFugoukakusyaSu {
        private String _maleCount;
        private String _femaleCount;

        MensetsuFugoukakusyaSu () {}

        void setMaleCount(String maleCount) {
            _maleCount = maleCount;
        }

        String getMaleCount() {
            return _maleCount;
        }

        void setFemaleCount(String femaleCount) {
            _femaleCount = femaleCount;
        }

        String getFemaleCount() {
            return _femaleCount;
        }
    }

    private class AdmissionRecommendationTestResultsForShoolData {
        private String _finschoolName;
        private String _examNo;
        private String _name;
        private String _judgement;

        AdmissionRecommendationTestResultsForShoolData (
                String finschoolName,
                String examNo,
                String name,
                String judgement
        ) {
            _finschoolName = finschoolName;
            _examNo = examNo;
            _name = name;
            _judgement = judgement;
        }
    }

    private class FsschoolTackSealForPrincipalData {
        private String _finschoolZipCd;
        private String _finschoolAddr1;
        private String _finschoolAddr2;
        private String _finschoolName;
        private String _examNo;

        FsschoolTackSealForPrincipalData (
                String finschoolZipCd,
                String finschoolAddr1,
                String finschoolAddr2,
                String finschoolName,
                String examNo
        ) {
            _finschoolZipCd = finschoolZipCd;
            _finschoolAddr1 = finschoolAddr1;
            _finschoolAddr2 = finschoolAddr2;
            _finschoolName = finschoolName;
            _examNo = examNo;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _output;
        private final String _schoolName;
        private final String _testDivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _output = request.getParameter("OUTPUT");
            _schoolName = getSchoolName(db2);
            _testDivName = getTestDivName(db2);
        }

        private String getSchoolName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String rtn = null;

            // 入試制度（APPLICANTDIV） が 1:中学、2:高校 の場合のみ名称を取得する。
            String sqlwk = " SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "'";
            String sql = "1".equals(_applicantDiv) ? (sqlwk + " AND CERTIF_KINDCD = '105' ") : "2".equals(_applicantDiv) ? (sqlwk + " AND CERTIF_KINDCD = '106' ") : "";
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOL_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getTestDivName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String rtn = null;

            String sql = " SELECT TESTDIV_ABBV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR='" + _entexamyear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' ";
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTDIV_ABBV");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
    }
}

// eof

