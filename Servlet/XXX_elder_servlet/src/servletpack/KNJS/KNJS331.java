/*
 * $Id: 99b6eadbf95d12d71cc4c16c5bc8e84d0d0f04eb $
 *
 * 作成日: 2019/09/26
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJS;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
 *  学校教育システム 賢者 [小学校プログラム] 指導計画表
 *
 */
public class KNJS331 {

    private static final Log log = LogFactory.getLog(KNJS331.class);

    private static final String[] MONTH_ARRAY = {"04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03"};
    private static final String[] KBN_ARRAY = {"PLAN", "RESULT"};

    private static final String PLAN = "PLAN";
    private static final String RESULT = "RESULT";
    private static final String TOTAL = "TOTAL";

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJS331.frm", 4);
        svfHeader(db2, svf);
        if (_hasData) svfPrintList(svf);
    }

    private void svfHeader(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrsOut("NENDO", _param.getNendo(db2) + "　年間授業時数・計画実施集計表");

        int idx = 1;
        for (final Iterator it = _param._chairMap.keySet().iterator(); it.hasNext();) {
            final String code = (String) it.next();
            final ClassData classData = (ClassData) _param._chairMap.get(code);
            if (null != classData) {
                String strIdx = String.format("%02d", idx);
                final String classField = getMS932Bytecount(classData._name) > 6 ? "CLASSNAME1"+ strIdx +"_2" : "CLASSNAME1"+ strIdx +"_1";
                svf.VrsOut(classField, classData._name); //科目
                _hasData = true;
            }
            idx++;
        }
    }

    private void svfPrintList(final Vrw32alp svf) {

        //月ごと
        for (int i = 0; i < MONTH_ARRAY.length; i++) {
            final String month = MONTH_ARRAY[i];
            svf.VrsOut("MONTH_SEMESTER1", month); //月・学期
            svfPrintJisu(svf, month);
            svf.VrEndRecord();
        }

        //学期ごと
        for (int semes = 1; semes <=3; semes++) {
            final String key = "SEMES-" + semes;
            svf.VrsOut("MONTH_SEMESTER1", semes + "学期"); //月・学期
            svfPrintJisu(svf, key);
            svf.VrEndRecord();
        }

        //年間
        svfPrintJisu(svf, TOTAL);
        svf.VrEndRecord();

    }

    private void svfPrintJisu(final Vrw32alp svf, final String key) {

        int subTotalPlan1 = 0;
        int subTotalResult1 = 0;
        int subTotalPlan2 = 0;
        int subTotalResult2 = 0;
        int subTotalStandard1 = 0;
        int subTotalContrast1 = 0;
        int subTotalStandard2 = 0;
        int subTotalContrast2 = 0;
        int idx = 1;
        for (final Iterator it = _param._chairMap.keySet().iterator(); it.hasNext();) {
            final String code = (String) it.next();
            final ClassData classData = (ClassData) _param._chairMap.get(code);
            if (null != classData) {
                String planCnt = "0";
                String resultCnt = "0";

                if(classData._planMap.containsKey(key)) {
                    final JisuData jisuData = (JisuData) classData._planMap.get(key);
                    planCnt = jisuData._count;
                    if(classData.parseIntClasscd() >= 90) {
                        subTotalPlan2 = subTotalPlan2 + Integer.parseInt(planCnt);
                    } else {
                        subTotalPlan1 = subTotalPlan1 + Integer.parseInt(planCnt);
                    }
                }

                if(classData._resultMap.containsKey(key)) {
                    final JisuData jisuData = (JisuData) classData._resultMap.get(key);
                    resultCnt = jisuData._count;
                    if(classData.parseIntClasscd() >= 90) {
                        subTotalResult2 = subTotalResult2 + Integer.parseInt(resultCnt);
                    } else {
                        subTotalResult1 = subTotalResult1 + Integer.parseInt(resultCnt);
                    }
                }

                final String nenkan = key.equals(TOTAL) ? "NENKAN_" : "";
                final String field = classData.parseIntClasscd() >= 90 ? "2" : "1";
                String strIdx = String.format("%02d", idx);
                svf.VrsOut(nenkan + "PLAN" + field + strIdx, planCnt); //予定授業数
                svf.VrsOut(nenkan + "RESULT" + field + strIdx, resultCnt); //実績授業数

                if(key.equals(TOTAL)) {
                    svf.VrsOut("NENKAN_STANDARD" + field + strIdx, classData._standardTime); //標準時数
                    final int contrast = Integer.parseInt(resultCnt) - Integer.parseInt(classData._standardTime);
                    svf.VrsOut("NENKAN_CONTRAST" + field + strIdx, String.valueOf(contrast)); //対比

                    if(classData.parseIntClasscd() >= 90) {
                        subTotalStandard2 = subTotalStandard2 + Integer.parseInt(classData._standardTime);
                        subTotalContrast2 = subTotalContrast2 + contrast;
                    } else {
                        subTotalStandard1 = subTotalStandard1 + Integer.parseInt(classData._standardTime);
                        subTotalContrast1 = subTotalContrast1 + contrast;
                    }

                }

            }
            idx++;
        }

        final String nenkan = key.equals(TOTAL) ? "NENKAN_" : "";
        svf.VrsOut("SUBTOTAL_"+ nenkan +"PLAN1", String.valueOf(subTotalPlan1));     //小計・予定
        svf.VrsOut("SUBTOTAL_"+ nenkan +"RESULT1", String.valueOf(subTotalResult1)); //小計・実績
        svf.VrsOut("SUBTOTAL_"+ nenkan +"PLAN2", String.valueOf(subTotalPlan2));     //小計・予定
        svf.VrsOut("SUBTOTAL_"+ nenkan +"RESULT2", String.valueOf(subTotalResult2)); //小計・実績
        svf.VrsOut("TOTAL_"+ nenkan +"PLAN", String.valueOf(subTotalPlan1 + subTotalPlan2));       //総計・予定
        svf.VrsOut("TOTAL_"+ nenkan +"RESULT", String.valueOf(subTotalResult1 + subTotalResult2)); //総計・実績

        String dayCntPlan   = "0";
        String dayCntResult = "0";
        if(_param._dayCntMap.containsKey(key)) {
            Map dayCntMap = (Map) _param._dayCntMap.get(key);
            if(dayCntMap.containsKey(PLAN)) {
                dayCntPlan = (String) dayCntMap.get(PLAN);
            }
            if(dayCntMap.containsKey(RESULT)) {
                dayCntResult = (String) dayCntMap.get(RESULT);
            }
        }
        svf.VrsOut("DAYCNT_"+ nenkan +"PLAN", dayCntPlan);   //日数・予定
        svf.VrsOut("DAYCNT_"+ nenkan +"RESULT", dayCntResult); //日数・実績

        if(key.equals(TOTAL)) {
            svf.VrsOut("SUBTOTAL_NENKAN_STANDARD1", String.valueOf(subTotalStandard1)); //小計・標準時数（年間）
            svf.VrsOut("SUBTOTAL_NENKAN_CONTRAST1", String.valueOf(subTotalContrast1)); //小計・対比（年間）
            svf.VrsOut("SUBTOTAL_NENKAN_STANDARD2", String.valueOf(subTotalStandard2)); //小計・標準時数（年間）
            svf.VrsOut("SUBTOTAL_NENKAN_CONTRAST2", String.valueOf(subTotalContrast2)); //小計・対比（年間）
            svf.VrsOut("TOTAL_NENKAN_STANDARD", String.valueOf(subTotalStandard1 + subTotalStandard2)); //総計・標準時数（年間）
            svf.VrsOut("TOTAL_NENKAN_CONTRAST", String.valueOf(subTotalContrast1 + subTotalContrast2)); //総計・対比（年間）

            dayCntPlan   = "0";
            dayCntResult = "0";
            if(_param._dayCntMap.containsKey(key)) {
                Map dayCntMap = (Map) _param._dayCntMap.get(key);
                if(dayCntMap.containsKey(PLAN)) {
                    dayCntPlan = (String) dayCntMap.get(PLAN);
                }
                if(dayCntMap.containsKey(RESULT)) {
                    dayCntResult = (String) dayCntMap.get(RESULT);
                }
                svf.VrsOut("DAYCNT_NENKAN_STANDARD", dayCntPlan); //日数・標準時数（年間）
                svf.VrsOut("DAYCNT_NENKAN_CONTRAST", dayCntResult); //日数・対比（年間）
            }
        }

    }


    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }


    private class ClassData {
        final String _code;
        final String _name;
        final String _classCd;
        final String _standardTime;
        final Map _planMap;
        final Map _resultMap;

        ClassData(final String code, final String name, final String classCd, final String standardTime) {
            _code = code;
            _name = name;
            _classCd = classCd;
            _standardTime = standardTime;
            _planMap = new HashMap();
            _resultMap = new HashMap();
        }

        private int parseIntClasscd() {
            final String classcd;
            if ("1".equals(_param._useCurriculumcd)) {
                classcd = StringUtils.split(_classCd, "-")[0];
            } else {
                classcd = _classCd;
            }
            return Integer.parseInt(classcd);
        }

    }

    private class JisuData {
        final String _semester;
        final String _count;

        JisuData(final String semester, final String count) {
            _semester = semester;
            _count = count;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 69897 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _schoolKind;       //校種
        final String _grade;            //学年
        final String _useCurriculumcd;  //教育課程コード

        final boolean _seirekiFlg;
        final TreeMap _chairMap = new TreeMap();
        final TreeMap _dayCntMap = new TreeMap();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _grade = request.getParameter("GRADE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            _seirekiFlg = getSeirekiFlg(db2);
            setChairMap(db2);
            String codeIn = "";
            String sep = "";
            for (final Iterator it = _chairMap.keySet().iterator(); it.hasNext();) {
                final String code = (String) it.next();
                setPlanMap(db2, code);
                setResultMap(db2, code);
                codeIn = codeIn + sep  + "'" + code + "'";
                sep = ",";
            }
            setDayCntMap(db2, codeIn);
        }

        private void setChairMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getChair();
//                log.debug("getChair = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = StringUtils.defaultString(rs.getString("CHAIRCD"));
                    final String name = StringUtils.defaultString(rs.getString("SUBCLASSABBV"));
                    final String classCd = StringUtils.defaultString(rs.getString("CLASSCD"));
                    final String standardTime = StringUtils.defaultString(rs.getString("STANDARD_TIME"), "0");

                    if (!_chairMap.containsKey(code)) {
                        final ClassData classData = new ClassData(code, name, classCd, standardTime);
                        _chairMap.put(code, classData);
                    }
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getChair() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     CSTD.CHAIRCD, ");
            stb.append("     SUBD.SUBCLASSABBV, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND AS CLASSCD, ");
            } else {
                stb.append("     substr(CHAIR.SUBCLASSCD, 1, 2) AS CLASSCD, ");
            }
            stb.append("     CLASS_M.CLASSABBV, ");
            stb.append("     UNIT.STANDARD_TIME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD, ");
            stb.append("     CHAIR_STD_DAT CSTD ");
            stb.append("     INNER JOIN CHAIR_DAT CHAIR ON CHAIR.YEAR = CSTD.YEAR ");
            stb.append("          AND CHAIR.SEMESTER = CSTD.SEMESTER ");
            stb.append("          AND CHAIR.CHAIRCD  = CSTD.CHAIRCD ");
            stb.append("          AND CHAIR.SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUBD ON SUBD.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("         AND SUBD.CLASSCD = CHAIR.CLASSCD ");
                stb.append("         AND SUBD.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
                stb.append("         AND SUBD.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN CLASS_MST CLASS_M ON ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("         CLASS_M.CLASSCD = CHAIR.CLASSCD ");
                stb.append("         AND CLASS_M.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            } else {
                stb.append("         substr(CHAIR.SUBCLASSCD, 1, 2) = CLASS_M.CLASSCD ");
            }
            stb.append("     LEFT JOIN UNIT_CLASS_LESSON_SCHOOL_DAT UNIT ON UNIT.YEAR = CHAIR.YEAR ");
            stb.append("          AND UNIT.SEMESTER   = CHAIR.SEMESTER ");
            stb.append("          AND UNIT.GRADE      = '" + _grade + "' ");
            stb.append("          AND UNIT.TIME_DIV   = '1' ");
            stb.append("          AND UNIT.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("         AND UNIT.CLASSCD = CHAIR.CLASSCD ");
                stb.append("         AND UNIT.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
                stb.append("         AND UNIT.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            }
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER IN ( ");
            stb.append("         SELECT ");
            stb.append("             II1.SEMESTER ");
            stb.append("         FROM ");
            stb.append("             SEMESTER_MST II1 ");
            stb.append("         WHERE ");
            stb.append("             II1.YEAR = '" + _ctrlYear + "' ");
            stb.append("             AND II1.SEMESTER < '9' ");
            stb.append("     ) ");
            stb.append("     AND REGD.GRADE = '" + _grade + "' ");
            stb.append("     AND REGD.YEAR = CSTD.YEAR ");
            stb.append("     AND REGD.SEMESTER = CSTD.SEMESTER ");
            stb.append("     AND REGD.SCHREGNO = CSTD.SCHREGNO ");
            stb.append("     AND EXISTS (SELECT ");
            stb.append("                     'x' ");
            stb.append("                 FROM ");
            stb.append("                     UNIT_STUDY_TEXT_BOOK_DAT UNIT_TEXT ");
            stb.append("                 WHERE ");
            stb.append("                     UNIT_TEXT.YEAR = CHAIR.YEAR ");
            stb.append("                     AND UNIT_TEXT.GRADE = REGD.GRADE ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("                     AND UNIT_TEXT.CLASSCD = CHAIR.CLASSCD ");
                stb.append("                     AND UNIT_TEXT.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
                stb.append("                     AND UNIT_TEXT.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            }
            stb.append("                     AND UNIT_TEXT.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            stb.append("     ) ");
            stb.append(" ORDER BY ");
            stb.append("     CLASSCD, ");
            stb.append("     CHAIRCD  ");
            return stb.toString();
        }

        //予定授業数の設定
        private void setPlanMap(final DB2UDB db2, final String code) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getJisu(code, PLAN);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                final ClassData classData = (ClassData) _chairMap.get(code);
                int totalCnt = 0;
                if (null == classData) {
                    log.error("classData無し :" + code);
                } else {
                    while (rs.next()) {
                        String month = rs.getString("MONTH");
                        month = String.format("%02d", Integer.parseInt(month));
                        final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                        final String count = StringUtils.defaultString(rs.getString("COUNT"));

                        //月ごと
                        if (!classData._planMap.containsKey(month)) {
                            final JisuData jisuData = new JisuData(semester, count);
                            classData._planMap.put(month, jisuData);
                            totalCnt = totalCnt + Integer.parseInt(count);
                        }

                        //学期合計
                        final String key = "SEMES-" + semester;
                        if (!classData._planMap.containsKey(key)) {
                            final JisuData jisuData = new JisuData(semester, count);
                            classData._planMap.put(key, jisuData);
                        } else {
                            final JisuData date = (JisuData) classData._planMap.get(key);
                            final int semesCnt = Integer.parseInt(date._count) + Integer.parseInt(count);
                            final JisuData jisuData = new JisuData(semester, String.valueOf(semesCnt));
                            classData._planMap.put(key, jisuData);
                        }
                    }

                    //年間
                    final JisuData jisuData = new JisuData("", String.valueOf(totalCnt));
                    classData._planMap.put(TOTAL, jisuData);
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        //実績授業数の設定
        private void setResultMap(final DB2UDB db2, final String code) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getJisu(code, RESULT);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                final ClassData classData = (ClassData) _chairMap.get(code);
                int totalCnt = 0;
                if (null == classData) {
                    log.error("classData無し :" + code);
                } else {
                    while (rs.next()) {
                        String month = rs.getString("MONTH");
                        month = String.format("%02d", Integer.parseInt(month));
                        final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                        final String count = StringUtils.defaultString(rs.getString("COUNT"));

                        //月ごと
                        if (!classData._resultMap.containsKey(month)) {
                            final JisuData jisuData = new JisuData(semester, count);
                            classData._resultMap.put(month, jisuData);
                            totalCnt = totalCnt + Integer.parseInt(count);
                        }

                        //学期合計
                        final String key = "SEMES-" + semester;
                        if (!classData._resultMap.containsKey(key)) {
                            final JisuData jisuData = new JisuData(semester, count);
                            classData._resultMap.put(key, jisuData);
                        } else {
                            final JisuData date = (JisuData) classData._resultMap.get(key);
                            final int semesCnt = Integer.parseInt(date._count) + Integer.parseInt(count);
                            final JisuData jisuData = new JisuData(semester, String.valueOf(semesCnt));
                            classData._resultMap.put(key, jisuData);
                        }
                    }

                    //年間
                    final JisuData jisuData = new JisuData("", String.valueOf(totalCnt));
                    classData._resultMap.put(TOTAL, jisuData);
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        //予定、実績授業数の取得
        private String getJisu(final String code, final String kbn) {
            final String year =  _ctrlYear;
            final String nextYear =  String.valueOf(Integer.parseInt(_ctrlYear) + 1);
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CHAIRCD, ");
            stb.append("     MONTH(EXECUTEDATE) AS MONTH, ");
            stb.append("     SEMESTER, ");
            stb.append("     COUNT(*) AS COUNT ");
            stb.append(" FROM  ");
            stb.append("     SCH_CHR_DAT ");
            stb.append(" WHERE  ");
            stb.append("     EXECUTEDATE BETWEEN '" + year + "-04-01' AND '" + nextYear + "-03-31' ");
            stb.append("     AND CHAIRCD = '" + code + "' ");
            if(RESULT.equals(kbn)) {
                stb.append("     AND ATTESTOR = '1' ");
            }
            stb.append(" GROUP BY  ");
            stb.append("     CHAIRCD, ");
            stb.append("     MONTH(EXECUTEDATE), ");
            stb.append("     SEMESTER ");
            stb.append(" ORDER BY  ");
            stb.append("     CHAIRCD, ");
            stb.append("     MONTH(EXECUTEDATE), ");
            stb.append("     SEMESTER ");

            return stb.toString();
        }

        //予定、実績授業日数の設定
        private void setDayCntMap(final DB2UDB db2, final String codeIn) {
            for (int i = 0; i < KBN_ARRAY.length; i++) {
                final String kbn = KBN_ARRAY[i];

                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    final String sql = getDayCnt(codeIn, kbn);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    int totalPlanCnt = 0;
                    int totalResultCnt = 0;
                    while (rs.next()) {
                        String month = rs.getString("MONTH");
                        month = String.format("%02d", Integer.parseInt(month));
                        final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                        final String count = StringUtils.defaultString(rs.getString("COUNT"));

                        //月ごと
                        if (!_dayCntMap.containsKey(month)) {
                            //月:不一致
                            final TreeMap map = new TreeMap();
                            map.put(kbn, count);
                            _dayCntMap.put(month, map);
                            if(kbn.equals(PLAN)) {
                                totalPlanCnt = totalPlanCnt + Integer.parseInt(count);
                            } else {
                                totalResultCnt = totalResultCnt + Integer.parseInt(count);
                            }
                        } else {
                            //月:一致
                            String planCnt = "0";
                            String resultCnt = "0";

                            final Map map = (Map) _dayCntMap.get(month);
                            if(kbn.equals(PLAN)) {
                                planCnt = count;
                                if (map.containsKey(RESULT)) resultCnt = (String) map.get(RESULT);
                            } else {
                                resultCnt = count;
                                if (map.containsKey(PLAN)) planCnt = (String) map.get(PLAN);
                            }
                            final TreeMap addMap = new TreeMap();
                            addMap.put(PLAN, planCnt);
                            addMap.put(RESULT, resultCnt);
                            _dayCntMap.put(month, addMap);
                        }

                        //学期合計
                        final String key = "SEMES-" + semester;
                        if (!_dayCntMap.containsKey(key)) {
                            //学期:不一致
                            final TreeMap map = new TreeMap();
                            map.put(kbn, count);
                            _dayCntMap.put(key, map);
                        } else {
                            //学期:一致
                            int planCnt = 0;
                            int resultCnt = 0;

                            final Map map = (Map) _dayCntMap.get(key);
                            if(kbn.equals(PLAN)) {
                                if (map.containsKey(PLAN)) {
                                    final String cnt = (String) map.get(PLAN);
                                    planCnt = Integer.parseInt(cnt) + Integer.parseInt(count);
                                }
                                if (map.containsKey(RESULT)) resultCnt = Integer.parseInt((String) map.get(RESULT));
                            } else {
                                if (map.containsKey(RESULT)) {
                                    final String cnt = (String) map.get(RESULT);
                                    resultCnt = Integer.parseInt(cnt) + Integer.parseInt(count);
                                }
                                if (map.containsKey(PLAN)) planCnt = Integer.parseInt((String) map.get(PLAN));
                            }

                            final TreeMap addMap = new TreeMap();
                            addMap.put(PLAN, String.valueOf(planCnt));
                            addMap.put(RESULT, String.valueOf(resultCnt));
                            _dayCntMap.put(key, addMap);
                        }
                    }

                    //年間
                    if (!_dayCntMap.containsKey(TOTAL)) {
                        //年間:不一致
                        final TreeMap map = new TreeMap();
                        map.put(PLAN, String.valueOf(totalPlanCnt));
                        map.put(RESULT, String.valueOf(totalResultCnt));
                        _dayCntMap.put(TOTAL, map);
                    } else {
                        //年間:一致
                        int planCnt = 0;
                        int resultCnt = 0;
                        final Map map = (Map) _dayCntMap.get(TOTAL);
                        if(kbn.equals(PLAN)) {
                            if (map.containsKey(PLAN)) {
                                final String cnt = (String) map.get(PLAN);
                                planCnt = Integer.parseInt(cnt) + totalPlanCnt;
                            }
                            if (map.containsKey(RESULT)) resultCnt = Integer.parseInt((String) map.get(RESULT));
                        } else {
                            if (map.containsKey(RESULT)) {
                                final String cnt = (String) map.get(RESULT);
                                resultCnt = Integer.parseInt(cnt) + totalResultCnt;
                            }
                            if (map.containsKey(PLAN)) planCnt = Integer.parseInt((String) map.get(PLAN));
                        }

                        final TreeMap addMap = new TreeMap();
                        addMap.put(PLAN, String.valueOf(planCnt));
                        addMap.put(RESULT, String.valueOf(resultCnt));
                        _dayCntMap.put(TOTAL, addMap);
                    }

                } catch (SQLException ex) {
                    log.debug("Exception:", ex);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
        }

        //予定、実績授業日数の取得
        private String getDayCnt(final String codeIn, final String kbn) {
            final String year =  _ctrlYear;
            final String nextYear =  String.valueOf(Integer.parseInt(_ctrlYear) + 1);
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("     MONTH(EXECUTEDATE) AS MONTH, ");
            stb.append("     SEMESTER, ");
            stb.append("     COUNT(*) AS COUNT ");
            stb.append(" FROM ");
            stb.append(" ( ");
            stb.append("   SELECT DISTINCT ");
            stb.append("       EXECUTEDATE, ");
            stb.append("       SEMESTER ");
            stb.append("   FROM  ");
            stb.append("       SCH_CHR_DAT ");
            stb.append("   WHERE  ");
            stb.append("       EXECUTEDATE BETWEEN '" + year + "-04-01' AND '" + nextYear + "-03-31' ");
            stb.append("       AND CHAIRCD IN (" + codeIn + " ) ");
            if("RESULT".equals(kbn)) {
                stb.append("     AND ATTESTOR = '1' ");
            }
            stb.append(" ) ");
            stb.append(" GROUP BY  ");
            stb.append("     MONTH(EXECUTEDATE), ");
            stb.append("     SEMESTER ");
            stb.append(" ORDER BY  ");
            stb.append("     MONTH(EXECUTEDATE), ");
            stb.append("     SEMESTER ");

            return stb.toString();
        }

        private boolean getSeirekiFlg(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean seirekiFlg = false;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if ("2".equals(rs.getString("NAME1"))) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return seirekiFlg;
        }

        private String getCtrlDate(final DB2UDB db2) {
            if (_seirekiFlg) {
                return _ctrlYear + "年" + KNJ_EditDate.h_format_JP_N(db2, _ctrlDate);
            } else {
                return KNJ_EditDate.h_format_JP(db2, _param._ctrlDate);
            }
        }

        public String getNendo(final DB2UDB db2) {
            if (_seirekiFlg) {
                return _ctrlYear + "年度";
            } else {
                final String[] gengou = KNJ_EditDate.tate_format4(db2, _ctrlDate);
                return gengou[0] + gengou[1] + "年度";
            }
        }
    }
}

// eof