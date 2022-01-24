// kanji=漢字
/*
 * 作成日: 2010/05/24 13:39:29 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJF150A {

    private static final Log log = LogFactory.getLog("KNJF150A.class");

    private boolean _hasData;

    Param _param;

    private static Map<String, Map<String, String>> CONDITION_MAP;
    private final static String KEY_SLEEPING  = "1";
    private final static String KEY_BOWEL_MOVEMENT = "3";
    private final static String KEY_BREAKFAST = "4";
    private final static String KEY_CAUSE  = "7";
    private final static String KEY_WANT  = "8";
    private final static String KEY_SCHOOL_LIFE = "9";
    private final static String KEY_FAMILY_LIFE = "10";
    private final static String KEY_PROBLEM = "11";
    private final static String KEY_PROBLEM_CAUSE = "12";

    static {
        CONDITION_MAP = new LinkedHashMap<String, Map<String, String>>();

        Map<String, String> keyVal = new LinkedHashMap<String, String>();
        CONDITION_MAP.put(KEY_SLEEPING, keyVal);
        keyVal.put("1", "よく眠れた");
        keyVal.put("2", "あまり眠れなかった");
        keyVal.put("3", "未記入");

        keyVal = new LinkedHashMap<String, String>();
        CONDITION_MAP.put(KEY_BOWEL_MOVEMENT, keyVal);
        keyVal.put("1", "普通");
        keyVal.put("2", "下痢");
        keyVal.put("3", "便秘");
        keyVal.put("4", "未記入");

        keyVal = new LinkedHashMap<String, String>();
        CONDITION_MAP.put(KEY_BREAKFAST, keyVal);
        keyVal.put("1", "食べた");
        keyVal.put("2", "少し食べた");
        keyVal.put("3", "食べていない");
        keyVal.put("4", "未記入");

        keyVal = new LinkedHashMap<String, String>();
        CONDITION_MAP.put(KEY_CAUSE, keyVal);
        keyVal.put("1", "生活習慣（食生活、睡眠、排便など）");
        keyVal.put("2", "寝冷え・風邪");
        keyVal.put("3", "疲れ");
        keyVal.put("4", "月経");
        keyVal.put("5", "心のストレス・心配事");
        keyVal.put("6", "運動したから");
        keyVal.put("7", "その他");
        keyVal.put("8", "わからない");
        keyVal.put("9", "未記入");

        keyVal = new LinkedHashMap<String, String>();
        CONDITION_MAP.put(KEY_WANT, keyVal);
        keyVal.put("1", "授業に出たい");
        keyVal.put("2", "湯たんぽを貸してほしい");
        keyVal.put("3", "相談したい");
        keyVal.put("4", "休養したい");
        keyVal.put("5", "病院に行きたい");
        keyVal.put("6", "早退したい");
        keyVal.put("7", "その他");
        keyVal.put("8", "未記入");

        keyVal = new LinkedHashMap<String, String>();
        CONDITION_MAP.put(KEY_SCHOOL_LIFE, keyVal);
        keyVal.put("1", "楽しい");
        keyVal.put("2", "普通");
        keyVal.put("3", "楽しくない");
        keyVal.put("4", "未記入");

        keyVal = new LinkedHashMap<String, String>();
        CONDITION_MAP.put(KEY_FAMILY_LIFE, keyVal);
        keyVal.put("1", "楽しい");
        keyVal.put("2", "普通");
        keyVal.put("3", "楽しくない");
        keyVal.put("4", "未記入");

        keyVal = new LinkedHashMap<String, String>();
        CONDITION_MAP.put(KEY_PROBLEM, keyVal);
        keyVal.put("1", "ある");
        keyVal.put("2", "ない");
        keyVal.put("3", "未記入");

        keyVal = new LinkedHashMap<String, String>();
        CONDITION_MAP.put(KEY_PROBLEM_CAUSE, keyVal);
        keyVal.put("1", "体や病気");
        keyVal.put("2", "勉強");
        keyVal.put("3", "受験や進路");
        keyVal.put("4", "友達やクラス");
        keyVal.put("5", "学校や先生のこと");
        keyVal.put("6", "部活動");
        keyVal.put("7", "男女交際");
        keyVal.put("8", "家庭のこと");
        keyVal.put("9", "自分のこと");
        keyVal.put("10", "その他");
        keyVal.put("11", "未記入");
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

    private void setSvfForm(final Vrw32alp svf) {
        String formFile = "KNJF150A_" + _param._type + ".frm";
        log.debug("フォーム：" + formFile);
        svf.VrSetForm(formFile, 1);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        final List students = createStudents(db2);
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            //フォーム
            setSvfForm(svf);
            //ヘッダ
            printHeader(svf, student);
            //保健室来室記録
            if (_param.isPrintNurseoff()) {
                printNurseoff(db2, svf, student);
            }

            _hasData = true;
            svf.VrEndPage();
        }
    }

    private void printHeader(final Vrw32alp svf, final Student student) {
        svf.VrsOut("DATE"       , _param.getDateWareki(_param._ctrlDate));
        svf.VrsOut("HR_NAME"    , student.getHrName());
        svf.VrsOut("ATTENDNO"   , student.getAttendNo());
        svf.VrsOut("NAME"       , student._name);
    }

    private void printNurseoff(final DB2UDB db2, final Vrw32alp svf, final Student student) throws SQLException {
        final String sql = sqlNurseoff(student);
        log.debug(" sql = " + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                printDeteil(svf, rs);
            }
        } catch (final Exception ex) {
            log.error("保健室来室記録のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlNurseoff(final Student student) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     DAT.*, ");
        stb.append("     D11.REMARK1 AS D_VISIT_PERIODCD, ");
        stb.append("     D12.REMARK1 AS D_CONDITION7, ");
        stb.append("     D13.REMARK1 AS D_CONDITION8, ");
        stb.append("     D14.REMARK1 AS D_CONDITION12, ");
        stb.append("     D15.REMARK1 AS D_CONTACT ");
        stb.append(" FROM ");
        stb.append("     NURSEOFF_VISITREC_DAT DAT ");
        stb.append("     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT D11 ");
        stb.append("       ON D11.SCHREGNO     = DAT.SCHREGNO ");
        stb.append("      AND D11.VISIT_DATE   = DAT.VISIT_DATE ");
        stb.append("      AND D11.VISIT_HOUR   = DAT.VISIT_HOUR ");
        stb.append("      AND D11.VISIT_MINUTE = DAT.VISIT_MINUTE ");
        stb.append("      AND D11.TYPE         = DAT.TYPE ");
        stb.append("      AND D11.SEQ          = '11' ");
        stb.append("     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT D12 ");
        stb.append("       ON D12.SCHREGNO     = DAT.SCHREGNO ");
        stb.append("      AND D12.VISIT_DATE   = DAT.VISIT_DATE ");
        stb.append("      AND D12.VISIT_HOUR   = DAT.VISIT_HOUR ");
        stb.append("      AND D12.VISIT_MINUTE = DAT.VISIT_MINUTE ");
        stb.append("      AND D12.TYPE         = DAT.TYPE ");
        stb.append("      AND D12.SEQ          = '12' ");
        stb.append("     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT D13 ");
        stb.append("       ON D13.SCHREGNO     = DAT.SCHREGNO ");
        stb.append("      AND D13.VISIT_DATE   = DAT.VISIT_DATE ");
        stb.append("      AND D13.VISIT_HOUR   = DAT.VISIT_HOUR ");
        stb.append("      AND D13.VISIT_MINUTE = DAT.VISIT_MINUTE ");
        stb.append("      AND D13.TYPE         = DAT.TYPE ");
        stb.append("      AND D13.SEQ          = '13' ");
        stb.append("     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT D14 ");
        stb.append("       ON D14.SCHREGNO     = DAT.SCHREGNO ");
        stb.append("      AND D14.VISIT_DATE   = DAT.VISIT_DATE ");
        stb.append("      AND D14.VISIT_HOUR   = DAT.VISIT_HOUR ");
        stb.append("      AND D14.VISIT_MINUTE = DAT.VISIT_MINUTE ");
        stb.append("      AND D14.TYPE         = DAT.TYPE ");
        stb.append("      AND D14.SEQ          = '14' ");
        stb.append("     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT D15 ");
        stb.append("       ON D15.SCHREGNO     = DAT.SCHREGNO ");
        stb.append("      AND D15.VISIT_DATE   = DAT.VISIT_DATE ");
        stb.append("      AND D15.VISIT_HOUR   = DAT.VISIT_HOUR ");
        stb.append("      AND D15.VISIT_MINUTE = DAT.VISIT_MINUTE ");
        stb.append("      AND D15.TYPE         = DAT.TYPE ");
        stb.append("      AND D15.SEQ          = '15' ");
        stb.append(" WHERE ");
        stb.append("     DAT.SCHREGNO = '" + student._schregno + "' AND ");
        stb.append("     DAT.VISIT_DATE = date('" + _param._visitDate + "') AND ");
        stb.append("     DAT.VISIT_HOUR = '" + _param._visitHour + "' AND ");
        stb.append("     DAT.VISIT_MINUTE = '" + _param._visitMinute + "' AND ");
        stb.append("     DAT.TYPE = '" + _param._type + "' ");
        return stb.toString();
    }

    private void printDeteil(final Vrw32alp svf, ResultSet rs) throws SQLException {
        //来室日時
        svf.VrsOut("VISIT_DATE"     , _param.getDateSeireki(rs.getString("VISIT_DATE")) );
        svf.VrsOut("NO"             , rs.getString("SERIAL_NUMBER") );
        svf.VrsOut("VISIT_HOUR"     , rs.getString("VISIT_HOUR") );
        svf.VrsOut("VISIT_MINUTE"   , rs.getString("VISIT_MINUTE") );
        svf.VrsOut("VISIT_SUBCLASS" , rs.getString("LESSON_CLASS") );
        svf.VrsOut("VISIT_TIME" , getNameMstArray("F700", rs.getString("VISIT_PERIODCD"), rs.getString("D_VISIT_PERIODCD")) );

        //来室理由
        String namecd = "";
        if (_param.isNaika()) namecd = "F200";
        else if (_param.isGeka()) namecd = "F201";
        else if (_param.isSonota()) namecd = "F203";
        else if (_param.isSeitoIgai()) namecd = "F202";
        else if (_param.isKenkouSoudan()) namecd = "F219";
        final String setReasonText1 = null == rs.getString("VISIT_REASON1_TEXT") ? "" : "(" + rs.getString("VISIT_REASON1_TEXT") + ")";
        final String setReasonText2 = null == rs.getString("VISIT_REASON2_TEXT") ? "" : "(" + rs.getString("VISIT_REASON2_TEXT") + ")";
        final String setReasonText3 = null == rs.getString("VISIT_REASON3_TEXT") ? "" : "(" + rs.getString("VISIT_REASON3_TEXT") + ")";
        svf.VrsOut("VISIT_REASON1"  , getNameMst(namecd, rs.getString("VISIT_REASON1")) + setReasonText1 );
        svf.VrsOut("VISIT_REASON2"  , getNameMst(namecd, rs.getString("VISIT_REASON2")) + setReasonText2 );
        svf.VrsOut("VISIT_REASON3"  , getNameMst(namecd, rs.getString("VISIT_REASON3")) + setReasonText3 );

        //いつから
        svf.VrsOut("OCCUR_SITUATION", getSinceWhen(rs.getString("SINCE_WHEN"), rs.getString("SINCE_WHEN_TEXT")) );

        //体調等
        svf.VrsOut("CONDITION1"     , getCondition(KEY_SLEEPING, rs.getString("CONDITION1")) );
        svf.VrsOut("SLEEPING_HOUR"      , rs.getString("SLEEPTIME") );
        svf.VrsOut("SLEEPING_MINUTE"    , rs.getString("SLEEPTIME_M") );
        svf.VrsOut("SLEEP_TIME_HOUR"    , rs.getString("BEDTIME_H") );
        svf.VrsOut("SLEEP_TIME_MINUTE"  , rs.getString("BEDTIME_M") );
        svf.VrsOut("CONDITION3"     , getCondition(KEY_BOWEL_MOVEMENT, rs.getString("CONDITION3")) );
        svf.VrsOut("CONDITION4"     , getCondition(KEY_BREAKFAST, rs.getString("CONDITION4")) );
        if (null != rs.getString("CONDITION7_TEXT") && !"".equals(rs.getString("CONDITION7_TEXT"))) {
            svf.VrsOut("CONDITION7_2_1"     , getConditionArray(KEY_CAUSE, rs.getString("CONDITION7"), rs.getString("D_CONDITION7")) );
            svf.VrsOut("CONDITION7_2_2"     , rs.getString("CONDITION7_TEXT") );
        } else {
            svf.VrsOut("CONDITION7_1"     , getConditionArray(KEY_CAUSE, rs.getString("CONDITION7"), rs.getString("D_CONDITION7")) );
        }
        if (null != rs.getString("CONDITION8_TEXT") && !"".equals(rs.getString("CONDITION8_TEXT"))) {
            svf.VrsOut("CONDITION8_2_1"     , getConditionArray(KEY_WANT, rs.getString("CONDITION8"), rs.getString("D_CONDITION8")) );
            svf.VrsOut("CONDITION8_2_2"     , rs.getString("CONDITION8_TEXT") );
        } else {
            svf.VrsOut("CONDITION8_1"     , getConditionArray(KEY_WANT, rs.getString("CONDITION8"), rs.getString("D_CONDITION8")) );
        }
        svf.VrsOut("CONDITION9"     , getCondition(KEY_SCHOOL_LIFE, rs.getString("CONDITION9")) );
        svf.VrsOut("CONDITION10"     , getCondition(KEY_FAMILY_LIFE, rs.getString("CONDITION10")) );
        svf.VrsOut("CONDITION11"     , getCondition(KEY_PROBLEM, rs.getString("CONDITION11")) );
        if (null != rs.getString("CONDITION12_TEXT") && !"".equals(rs.getString("CONDITION12_TEXT"))) {
            svf.VrsOut("CONDITION12_2_1"     , getConditionArray(KEY_PROBLEM_CAUSE, rs.getString("CONDITION12"), rs.getString("D_CONDITION12")) );
            svf.VrsOut("CONDITION12_2_2"     , rs.getString("CONDITION12_TEXT") );
        } else {
            svf.VrsOut("CONDITION12_1"     , getConditionArray(KEY_PROBLEM_CAUSE, rs.getString("CONDITION12"), rs.getString("D_CONDITION12")) );
        }
        //体温
        svf.VrsOut("TEMPERATURE1"       , rs.getString("TEMPERATURE1") );
        svf.VrsOut("MEASURE_HOUR"       , rs.getString("MEASURE_HOUR1") );
        svf.VrsOut("MEASURE_MINUTE"     , rs.getString("MEASURE_MINUTE1") );

        //脈拍
        svf.VrsOut("PULSE"              , rs.getString("PULSE") );

        //血圧
        svf.VrsOut("BLOOD_PRESSURE1"    , rs.getString("BLOOD_PRESSURE_L") );
        svf.VrsOut("BLOOD_PRESSURE2"    , rs.getString("BLOOD_PRESSURE_H") );

        //けがの部位
        final String setInjuryText1 = null == rs.getString("INJURY_PART1_TEXT") ? "" : "(" + rs.getString("INJURY_PART1_TEXT") + ")";
        final String setInjuryText2 = null == rs.getString("INJURY_PART2_TEXT") ? "" : "(" + rs.getString("INJURY_PART2_TEXT") + ")";
        final String setInjuryText3 = null == rs.getString("INJURY_PART3_TEXT") ? "" : "(" + rs.getString("INJURY_PART3_TEXT") + ")";
        svf.VrsOut("INJURY_PART1"     , getNameMst("F207", rs.getString("INJURY_PART1")) + setInjuryText1 );
        svf.VrsOut("INJURY_PART2"     , getNameMst("F207", rs.getString("INJURY_PART2")) + setInjuryText2 );
        svf.VrsOut("INJURY_PART3"     , getNameMst("F207", rs.getString("INJURY_PART3")) + setInjuryText3 );

        //発生時
        svf.VrsOut("OCCUR_DATE"      , _param.getDateSeireki(rs.getString("OCCUR_DATE")) );
        svf.VrsOut("OCCUR_HOUR"      , rs.getString("OCCUR_HOUR") );
        svf.VrsOut("OCCUR_MINUTE"    , rs.getString("OCCUR_MINUTE") );
        if (null != rs.getString("OCCUR_SITUATION") && !"".equals(rs.getString("OCCUR_SITUATION"))) {
            svf.VrsOut("OCCUR_SITUATION2_1"     , getNameMst("F216", rs.getString("OCCUR_ACT")) );
            svf.VrsOut("OCCUR_SITUATION2_2"     , rs.getString("OCCUR_SITUATION") );
        } else {
            svf.VrsOut("OCCUR_SITUATION1_1"     , getNameMst("F216", rs.getString("OCCUR_ACT")) );
        }
        if (null != rs.getString("OCCUR_PLACE_TEXT") && !"".equals(rs.getString("OCCUR_PLACE_TEXT"))) {
            svf.VrsOut("OCCUR_PLACE2_1"     , getNameMst("F206", rs.getString("OCCUR_PLACE")) );
            svf.VrsOut("OCCUR_PLACE2_2"     , rs.getString("OCCUR_PLACE_TEXT") );
        } else {
            svf.VrsOut("OCCUR_PLACE1"     , getNameMst("F206", rs.getString("OCCUR_PLACE")) );
        }

        //処置
        namecd = "";
        if (_param.isNaika()) namecd = "F208";
        else if (_param.isGeka()) namecd = "F209";
        else if (_param.isSonota()) namecd = "F210";
        else if (_param.isSeitoIgai()) namecd = "F210";
        else if (_param.isKenkouSoudan()) namecd = "F220";
        final String setTreatmentText1 = null == rs.getString("TREATMENT1_TEXT") ? "" : "(" + rs.getString("TREATMENT1_TEXT") + ")";
        final String setTreatmentText2 = null == rs.getString("TREATMENT2_TEXT") ? "" : "(" + rs.getString("TREATMENT2_TEXT") + ")";
        final String setTreatmentText3 = null == rs.getString("TREATMENT3_TEXT") ? "" : "(" + rs.getString("TREATMENT3_TEXT") + ")";
        svf.VrsOut("TREATMENT1"     , getNameMst(namecd, rs.getString("TREATMENT1")) + setTreatmentText1 );
        svf.VrsOut("TREATMENT2"     , getNameMst(namecd, rs.getString("TREATMENT2")) + setTreatmentText2 );
        svf.VrsOut("TREATMENT3"     , getNameMst(namecd, rs.getString("TREATMENT3")) + setTreatmentText3 );

        //在室時間
        svf.VrsOut("STAY"     , getNameMst("F212", rs.getString("RESTTIME")) );

        //連絡
        final String setContactText = null == rs.getString("CONTACT_TEXT") ? "" : "(" + rs.getString("CONTACT_TEXT") + ")";
        svf.VrsOut("CONTACT"     , getNameMstArray("F213", rs.getString("CONTACT"), rs.getString("D_CONTACT")) + setContactText );

        //連絡
        final int lineCnt = "1".equals(_param._type) ? 4 : 8;
        printLineData(svf, "SPECIAL_NOTE", rs.getString("SPECIAL_NOTE"), 88, lineCnt);
    }

    private void printLineData(
            final Vrw32alp svf,
            final String fieldName,
            final String str,
            final int size,
            final int lineCnt
    ) {
        KNJObjectAbs knjobj = new KNJEditString();
        ArrayList arrlist = knjobj.retDividString(str, size, lineCnt);
        if ( arrlist != null ) {
            for (int i = 0; i < arrlist.size(); i++) {
                svf.VrsOut(fieldName + (i + 1),  (String)arrlist.get(i) );
            }
        }
    }

    private String getNameMst(final String namecd1, final String namecd2) {
        final String namecd = namecd1 + "-" + namecd2;
        if (_param._nameMstMap.containsKey(namecd)) return (String) _param._nameMstMap.get(namecd);
        return "";
    }

    private String getNameMstArray(final String namecd1, final String namecd2, final String namecd2Array) {
        if (null == namecd1 || null == namecd2) return "";

        String namecd = "";
        if (null == namecd2Array) {
            namecd = namecd1 + "-" + namecd2;
            if (_param._nameMstMap.containsKey(namecd)) return (String) _param._nameMstMap.get(namecd);
        } else {
            final StringBuffer name = new StringBuffer();
            final String[] dNamecd2Array = StringUtils.split(namecd2Array, ",");
            for (String dNamecd3 : dNamecd2Array) {
                namecd = namecd1 + "-" + dNamecd3;
                if (_param._nameMstMap.containsKey(namecd)) {
                    name.append(_param._nameMstMap.get(namecd) + ",");
                }
            }
            name.delete(name.length() - 1, name.length()); //最後の "," を削除
            return name.toString();
        }
        return "";
    }

    private String getSinceWhen(final String cd, final String textData) {
        if ("1".equals(cd)) {
            return "数日前";
        }
        if ("2".equals(cd)) {
            return "昨日";
        }
        if ("3".equals(cd)) {
            return "今朝から";
        }
        if ("4".equals(cd)) {
            final String setText = null != textData ? textData : "";
            return "学校で" + setText + "限目頃から";
        }
        if ("5".equals(cd)) {
            final String setText = null != textData ? "(" + textData + ")" : "";
            return "その他" + setText;
        }
        if ("6".equals(cd)) {
            return "未記入";
        }
        return "";
    }

    private String getCondition(final String no, final String condition) {
        if (CONDITION_MAP.containsKey(no)) {
            Map<String, String> keyVal = CONDITION_MAP.get(no);
            if (keyVal.containsKey(condition)) {
                String val = keyVal.get(condition);
                return val;
            }
        }

        return "";
    }

    private String getConditionArray(final String no, final String condition, String dCondition2) {
        if (CONDITION_MAP.containsKey(no)) {
            Map<String, String> keyVal = CONDITION_MAP.get(no);
            if (dCondition2 == null) {
                if (keyVal.containsKey(condition)) {
                    String val = keyVal.get(condition);
                    return val;
                }
            } else {
                StringBuffer value = new StringBuffer();
                String[] dCondition2Array = StringUtils.split(dCondition2, ",");
                for (String dCondition3 : dCondition2Array) {
                    //ゼロサプレス
                    dCondition3 = String.valueOf(Integer.parseInt(dCondition3));

                    if (keyVal.containsKey(dCondition3)) {
                        String val = keyVal.get(dCondition3);
                        value.append(val + ",");
                    }
                }
                value.delete(value.length() - 1, value.length()); //最後の "," を削除
                return value.toString();
            }
        }

        return "";
    }

    private String getResult(final String result) {
        return null != result ? "レ" : "";
    }

    private String getOccurActDetail(final String occurAct, final String occurActDetail) {
        if ("1".equals(getNameSpare("F216", occurAct))) {
            return getNameMst("B001", occurActDetail);
        }
        if ("2".equals(getNameSpare("F216", occurAct))) {
            return getClubMst(occurActDetail);
        }
        return getNameMst("F217", occurActDetail);
    }

    private String getNameSpare(final String namecd1, final String namecd2) {
        if (null == namecd1 || null == namecd2) return "";
        final String namecd = namecd1 + "-" + namecd2;
        if (_param._nameSpareMap.containsKey(namecd)) return (String) _param._nameSpareMap.get(namecd);
        return "";
    }

    private String getClubMst(final String clubcd) {
        if (null == clubcd) return "";
        if (_param._clubMstMap.containsKey(clubcd)) return (String) _param._clubMstMap.get(clubcd);
        return "";
    }

    private List createStudents(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlStudents();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String attendNo = rs.getString("ATTENDNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");

                final Student student = new Student(schregno, name, attendNo, hrName, grade, hrClass);
                rtn.add(student);
            }
        } catch (final Exception ex) {
            log.error("生徒のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String sqlStudents() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T3.HR_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("                                   AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("                                   AND T3.GRADE = T1.GRADE ");
        stb.append("                                   AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = '" + _param._schregno + "' AND ");
        stb.append("     T1.YEAR = '" + _param._year + "' AND ");
        stb.append("     T1.SEMESTER = '" + _param._semester + "' ");
        return stb.toString();
    }

    private class Student {
        private final String _schregno;
        private final String _name;
        private final String _attendNo;
        private final String _hrName;
        private final String _grade;
        private final String _hrClass;

        public Student(
                final String schregno,
                final String name,
                final String attendNo,
                final String hrName,
                final String grade,
                final String hrClass
        ) {
            _schregno = schregno;
            _name = name;
            _attendNo = attendNo;
            _hrName = hrName;
            _grade = grade;
            _hrClass = hrClass;
        }

        public String getHrName() {
            return (_hrName != null) ? _hrName : "";
        }

        public String getAttendNo() {
            return String.valueOf(Integer.parseInt(_attendNo));
        }

        public String toString() {
            return _schregno + ":" + _name;
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
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _schregno;
        private final String _visitDate;
        private final String _visitHour;
        private final String _visitMinute;
        private final String _type;
        private final String _year;
        private final String _semester;
        private final String _ctrlDate;
        private final Map _nameMstMap;
        private final Map _clubMstMap;
        private Map _nameSpareMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _schregno = request.getParameter("SCHREGNO");
            String visitDate = request.getParameter("PRINT_VISIT_DATE");
            _visitDate = visitDate.replace('/', '-');
            _visitHour = request.getParameter("PRINT_VISIT_HOUR");
            _visitMinute = request.getParameter("PRINT_VISIT_MINUTE");
            _type = request.getParameter("TYPE");
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _nameMstMap = createNameMstMap(db2);
            _clubMstMap = createClubMstMap(db2);
        }

        private boolean isNaika() {
            return "1".equals(_type);
        }

        private boolean isGeka() {
            return "2".equals(_type);
        }

        private boolean isSonota() {
            return "3".equals(_type);
        }

        private boolean isSeitoIgai() {
            return "4".equals(_type);
        }

        private boolean isKenkouSoudan() {
            return "5".equals(_type);
        }

        private boolean isPrintNurseoff() {
            return null != _visitDate && !"".equals(_visitDate);
        }

        private String getDateWareki(final String date) {
            if (null == date || "".equals(date)) {
                return "";
            }
            return KNJ_EditDate.h_format_JP(date);
        }

        private String getDateSeireki(final String date) {
            if (null == date || "".equals(date)) {
                return "";
            }
            return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date) + "(" + KNJ_EditDate.h_format_W(date) + ")";
        }

        private Map createNameMstMap(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            _nameSpareMap = new HashMap();
            final String sql = sqlNameMst();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String namecd = rs.getString("NAMECD1") + "-" + rs.getString("NAMECD2");
                    final String name = rs.getString("NAME1");
                    final String namespare = rs.getString("NAMESPARE1");

                    rtn.put(namecd, name);
                    _nameSpareMap.put(namecd, namespare);
                }
            } catch (final Exception ex) {
                log.error("名称マスタのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlNameMst() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAMECD1, ");
            stb.append("     NAMECD2, ");
            stb.append("     NAME1, ");
            stb.append("     NAMESPARE1 ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' AND ");
            stb.append("     NAMECD1 in ('B001', ");
            stb.append("                 'F200', ");
            stb.append("                 'F201', ");
            stb.append("                 'F202', ");
            stb.append("                 'F203', ");
            stb.append("                 'F204', ");
            stb.append("                 'F206', ");
            stb.append("                 'F207', ");
            stb.append("                 'F208', ");
            stb.append("                 'F209', ");
            stb.append("                 'F210', ");
            stb.append("                 'F212', ");
            stb.append("                 'F213', ");
            stb.append("                 'F214', ");
            stb.append("                 'F215', ");
            stb.append("                 'F216', ");
            stb.append("                 'F217', ");
            stb.append("                 'F218', ");
            stb.append("                 'F219', ");
            stb.append("                 'F220', ");
            stb.append("                 'F700') ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD1, ");
            stb.append("     NAMECD2 ");
            return stb.toString();
        }

        private Map createClubMstMap(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            final String sql = sqlClubMst();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String clubcd = rs.getString("CLUBCD");
                    final String clubName = rs.getString("CLUBNAME");

                    rtn.put(clubcd, clubName);
                }
            } catch (final Exception ex) {
                log.error("部活マスタのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlClubMst() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CLUBCD, ");
            stb.append("     T2.CLUBNAME ");
            stb.append(" FROM ");
            stb.append("     CLUB_YDAT T1 ");
            stb.append("     INNER JOIN CLUB_MST T2 ON T1.CLUBCD = T2.CLUBCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLUBCD ");
            return stb.toString();
        }

    }
}

// eof
