/*
 * $Id: 4b86c348051ffa2bdce2b39e76319007a2a4120c $
 *
 * 作成日: 2016/10/25
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL421F {

    private static final Log log = LogFactory.getLog(KNJL421F.class);

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
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static int getMS932ByteCount(final String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("EncodingException!", e);
                count = str.length();
            }
        }
        return count;
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List receptAllList = Recept.getReceptList(db2, _param);
        if (receptAllList.isEmpty()) {
            return;
        }
        final String form;

        final int maxLine;
        if ("J".equals(_param._schoolKind)) {
            form = "KNJL421F_1.frm";
            maxLine = 35;
        } else {
            form = "KNJL421F_2.frm";
            maxLine = 95;
        }
        svf.VrSetForm(form, 1);
        final List pageList = getPageList(receptAllList, maxLine);
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List receptList = (List) pageList.get(pi);
            
            if ("J".equals(_param._schoolKind)) {
                svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度 " + " 募集企画データ資料(中学)"); // タイトル
                svf.VrsOut("SUBTITLE", ""); // サブタイトル
//                svf.VrsOut("SORT_NAME", "1".equals(_param._sort) ? "(成績順)" : "(受験番号順)"); // ソート名
                svf.VrsOut("DATE", _param._currentTime); // 日付
                svf.VrsOut("SUBCLASS_NAME1", "国語"); // 科目名称
                svf.VrsOut("SUBCLASS_NAME2", "算数"); // 科目名称
                svf.VrsOut("SUBCLASS_NAME3", "理科"); // 科目名称
                svf.VrsOut("SUBCLASS_NAME4", "社会"); // 科目名称
                svf.VrsOut("SUBCLASS_NAME5", "英語"); // 科目名称
            } else if ("H".equals(_param._schoolKind)) {
                svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度 " + " 募集企画データ資料(高校)"); // タイトル
                svf.VrsOut("DATE", _param._currentTime); // 印刷日
//                svf.VrsOut("RECOMMEND_TOTAL_NAME", "" + "合計"); // 推薦合計名称
//                svf.VrsOut("RECOMMEND_TOTAL", String.valueOf(receptAllList.size())); // 推薦合計
                svf.VrsOut("APT_NAME1", "数学"); // 適正検査名称
                svf.VrsOut("APT_NAME2", "英語"); // 適正検査名称
                svf.VrsOut("APT_NAME3", "作文"); // 適正検査名称
                svf.VrsOut("APT_NAME4", "合計"); // 適正検査名称
                svf.VrsOut("APT_NAME5", "面接"); // 適正検査名称
                svf.VrsOut("FORCE_NAME1", "国語"); // 質力診断テスト名称
                svf.VrsOut("FORCE_NAME2", "英語"); // 質力診断テスト名称
                svf.VrsOut("FORCE_NAME3", "数学"); // 質力診断テスト名称
                svf.VrsOut("FORCE_NAME4", "合計"); // 質力診断テスト名称
            }
            for (int i = 0; i < receptList.size(); i++) {
                final Recept recept = (Recept) receptList.get(i);
                
                final int line = i + 1;
                if ("J".equals(_param._schoolKind)) {

                    svf.VrsOutn("MANAGE_NO", line, recept._recruitNo); // 管理番号
//                    svf.VrsOutn("EXAN_DIV", line, recept._examTypeName); // 受験型
//                    svf.VrsOutn("EXAN_NO", line, recept._receptno); // 受験番号
                    
                    final int namelen = getMS932ByteCount(recept._name);
                    svf.VrsOutn("NAME" + (namelen > 20 ? "2" : "1"), line, recept._name); // 氏名
                    final int nameKanalen = getMS932ByteCount(recept._nameKana);
                    svf.VrsOutn("KANA" + (nameKanalen > 20 ? "2" : "1"), line, recept._nameKana); // フリガナ

//                    svf.VrsOutn("ABSENCE", line, "4".equals(recept._judgediv) ? recept._judgedivName : ""); // 欠席
//                    svf.VrsOutn("CHARGE_NAME", line, null); // 担当者
//                    svf.VrsOutn("TOTAL", line, recept._total4); // 合計
//                    final Map subclassScoreMap = recept.getPrintSubclassMap(_param);
//                    for (final Iterator it = subclassScoreMap.keySet().iterator(); it.hasNext();) {
//                        final String location = (String) it.next();
//                        final String score = (String) subclassScoreMap.get(location);
//                        svf.VrsOutn("SCORE" + location, line, score); // 点数
//                    }
//                    svf.VrsOutn("SISTER", line, "1".equals(recept._sisterFlg) ? "姉" : "2".equals(recept._sisterFlg) ? "妹" : ""); // 姉妹
//                    svf.VrsOutn("MOTHER", line, null != recept._motherFlg ? "母" : ""); // 母卒業
//                    svf.VrsOutn("POST", line, null); // 郵送
//                    svf.VrsOutn("JUDGE", line, recept._judgedivName); // 合否
//                    svf.VrsOutn("HOPE", line, "1".equals(recept._shFlg) ? "レ" : ""); // 第１志望でない

//                    for (int k = 0; k < 6; k++) {
//                        final String shschoolname = new String[] {recept._shSchoolname1, recept._shSchoolname2, recept._shSchoolname3, recept._shSchoolname4, recept._shSchoolname5, recept._shSchoolname6}[k];
//                        final String judge = new String[] {recept._shJudgement1, recept._shJudgement2, recept._shJudgement3, recept._shJudgement4, recept._shJudgement5, recept._shJudgement6}[k];
//                        final int shschoolnamelen = getMS932ByteCount(shschoolname);
//                        svf.VrsOutn("ANOTHER_SCHOOL_NAME" + String.valueOf(k + 1) + "_" + (shschoolnamelen > 16 ? "3" : shschoolnamelen > 12 ? "2" : "1"), line, shschoolname); // 併願校
//                    }

                    final int jukunamelen = getMS932ByteCount(recept._prischoolName);
                    svf.VrsOutn("CRAMML_NAME" + (jukunamelen > 16 ? "3" : jukunamelen > 12 ? "2" : "1"), line, recept._prischoolName); // 塾名

                    final int jukuclassnamelen = getMS932ByteCount(recept._prischoolClassName);
                    svf.VrsOutn("CRAMM_CLASSROOM_NAME" + (jukuclassnamelen > 16 ? "3" : jukuclassnamelen > 12 ? "2" : "1"), line, recept._prischoolClassName); // 教室名

//                    for (int i = 0; i < 5; i++) {
//                        final String testdiv = String.valueOf(i + 1);
//                        svf.VrsOutn("EXAN_NO" + testdiv, line, (String) recept._testdivExamnoMap.get(testdiv)); // 受験番号
//                    }

                } else if ("H".equals(_param._schoolKind)) {
                    
                    svf.VrsOutn("NO", line, String.valueOf(pi * maxLine + line)); // NO
                    svf.VrsOutn("EXAN_NO", line, recept._recruitNo); // 受験番号
                    final int citynamelen = getMS932ByteCount(recept._finschoolDistcdname);
                    svf.VrsOutn("CITY_NAME" + (citynamelen > 14 ? "3" : citynamelen > 10 ? "2" : "1"), line, recept._finschoolDistcdname); // 市・町
                    final int schoolnamelen = getMS932ByteCount(recept._finschoolName);
                    svf.VrsOutn("SCHOOL_NAME" + (schoolnamelen > 14 ? "3" : schoolnamelen > 10 ? "2" : "1"), line, recept._finschoolName); // 中学校
                    final int namelen = getMS932ByteCount(recept._name);
                    svf.VrsOutn("NAME" + (namelen > 20 ? "2" : "1"), line, recept._name); // 氏名
                    final int nameKanalen = getMS932ByteCount(recept._nameKana);
                    svf.VrsOutn("KANA" + (nameKanalen > 20 ? "2" : "1"), line, recept._nameKana); // フリガナ
                    svf.VrsOutn("HOPE_COURSE", line, recept._examcourseName); // 希望コース
//                    final Map scoreMap1 = _param.getScoreMap(recept, 1);
//                    svf.VrsOutn("APT1", line, (String) scoreMap1.get("2")); // 適正検査 数学
//                    svf.VrsOutn("APT2", line, (String) scoreMap1.get("5")); // 適正検査 英語
//                    svf.VrsOutn("APT3", line, (String) scoreMap1.get("6")); // 適正検査 作文
//                    svf.VrsOutn("APT4", line, (String) scoreMap1.get("TOTAL4")); // 適正検査 合計
//                    svf.VrsOutn("APT5", line, null); // 適正検査 面接
//                    final Map scoreMap2 = _param.getScoreMap(recept, 2);
//                    svf.VrsOutn("FORCE1", line, (String) scoreMap2.get("1")); // 質力診断テスト 国語
//                    svf.VrsOutn("FORCE2", line, (String) scoreMap2.get("5")); // 質力診断テスト 英語
//                    svf.VrsOutn("FORCE3", line, (String) scoreMap2.get("2")); // 質力診断テスト 数学
//                    svf.VrsOutn("FORCE4", line, (String) scoreMap2.get("TOTAL4")); // 質力診断テスト 合計
                    svf.VrsOutn("TOTAL1", line, recept._recrVisitScoreTotal3); // 科目合計
                    svf.VrsOutn("TOTAL2", line, recept._recrVisitScoreTotal5); // 科目合計
                    svf.VrsOutn("TOTAL3", line, recept._recrVisitScoreTotal9); // 科目合計
                    String remark = "";
                    if (null != recept._judgeKindName) remark += recept._judgeKindName; 
                    if (null != recept._recrVisitMockTopAvg) {
                        if (!"".equals(remark)) remark += ", "; 
                        remark += "SS " + recept._recrVisitMockTopAvg; 
                    }
                    if (null != recept._recrVisitActivePoint) {
                        if (!"".equals(remark)) remark += ", "; 
                        remark += "諸活動" + recept._recrVisitActivePoint + "点"; 
                    }
                    if (null != recept._recrWrapupDatRemark) {
                        if (!"".equals(remark)) remark += ", "; 
                        remark += recept._recrWrapupDatRemark; 
                    }
                    final int remarklen = getMS932ByteCount(remark);
                    svf.VrsOutn("RENARK1" + (remarklen > 40 ? "_2" : remarklen > 30 ? "" : remarklen > 20 ? "_0" : "_4"), line, remark); // 備考
//                    svf.VrsOutn("RENARK2", line, null); // 備考
//                    svf.VrsOutn("PASS_COURSE", line, null); // 合格コース
                }
            }
            _hasData = true;
            
            svf.VrEndPage();
        }
    }
    
    private static class Recept {
        final String _recruitNo;
        final String _finschoolName;
        final String _finschoolDistcdname;
        final String _name;
        final String _nameKana;
        final String _examcourseName;
        final String _recrVisitScoreTotal3;
        final String _recrVisitScoreTotal5;
        final String _recrVisitScoreTotal9;
        final String _judgeKindName;
        final String _recrVisitMockTopAvg;
        final String _recrVisitActivePoint;
        final String _recrWrapupDatRemark;
        final Map _scoreMap = new HashMap();
        final Map _testdiv3testcount2scoreMap = new HashMap();
        final String _prischoolName;
        final String _prischoolClassName;

        Recept(
            final String recruitNo,
            final String finschoolName,
            final String finschoolDistcdname,
            final String name,
            final String nameKana,
            final String examcourseName,
            final String recrVisitScoreTotal3,
            final String recrVisitScoreTotal5,
            final String recrVisitScoreTotal9,
            final String judgeKindName,
            final String recrVisitMockTopAvg,
            final String recrVisitActivePoint,
            final String recrWrapupDatRemark,
            final String prischoolName,
            final String prischoolClassName
        ) {
            _recruitNo = recruitNo;
            _finschoolName = finschoolName;
            _finschoolDistcdname = finschoolDistcdname;
            _name = name;
            _nameKana = nameKana;
            _examcourseName = examcourseName;
            _recrVisitScoreTotal3 = recrVisitScoreTotal3;
            _recrVisitScoreTotal5 = recrVisitScoreTotal5;
            _recrVisitScoreTotal9 = recrVisitScoreTotal9;
            _judgeKindName = judgeKindName;
            _recrVisitMockTopAvg = recrVisitMockTopAvg;
            _recrVisitActivePoint = recrVisitActivePoint;
            _recrWrapupDatRemark = recrWrapupDatRemark;
            _prischoolName = prischoolName;
            _prischoolClassName = prischoolClassName;
        }

        public static List getReceptList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
//                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String recruitNo = rs.getString("RECRUIT_NO");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String finschoolDistcdname = rs.getString("FINSCHOOL_DISTCDNAME");
                    final String name = rs.getString("NAME");
                    final String nameKana = rs.getString("NAME_KANA");
                    final String examcourseName = rs.getString("EXAMCOURSE_NAME");
                    final String recrVisitScoreTotal3 = rs.getString("RECR_VISIT_SCORE_TOTAL3");
                    final String recrVisitScoreTotal5 = rs.getString("RECR_VISIT_SCORE_TOTAL5");
                    final String recrVisitScoreTotal9 = rs.getString("RECR_VISIT_SCORE_TOTAL9");
                    final String judgeKindName = rs.getString("JUDGE_KIND_NAME");
                    final String recrVisitMockTopAvg = rs.getString("RECR_VISIT_MOCK_TOP_AVG");
                    final String recrVisitActivePoint = rs.getString("RECR_VISIT_ACTIVE_POINT");
                    final String recrWrapupDatRemark = rs.getString("RECR_WRAPUP_DAT_REMARK");
                    final String prischoolName = rs.getString("PRISCHOOL_NAME");
                    final String prischoolClassName = rs.getString("PRISCHOOL_CLASS_NAME");
                    final Recept recept = new Recept(recruitNo, finschoolName, finschoolDistcdname, name, nameKana, examcourseName, recrVisitScoreTotal3, recrVisitScoreTotal5, recrVisitScoreTotal9, judgeKindName, recrVisitMockTopAvg, recrVisitActivePoint, recrWrapupDatRemark, prischoolName, prischoolClassName);
                    list.add(recept);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TRECR.RECRUIT_NO ");
            stb.append("   , TFIN.FINSCHOOL_NAME ");
            stb.append("   , NML001.NAME1 AS FINSCHOOL_DISTCDNAME ");
            stb.append("   , TRECR.NAME ");
            stb.append("   , TRECR.NAME_KANA ");

            stb.append("   , CRSM.EXAMCOURSE_ABBV AS EXAMCOURSE_NAME ");

            stb.append("   , TRVS.TOTAL3 AS RECR_VISIT_SCORE_TOTAL3 ");
            stb.append("   , TRVS.TOTAL5 AS RECR_VISIT_SCORE_TOTAL5 ");
            stb.append("   , TRVS.TOTAL9 AS RECR_VISIT_SCORE_TOTAL9 ");

            stb.append("   , NML025.NAME2 AS JUDGE_KIND_NAME ");
            stb.append("   , TRVM.TOP_AVG AS RECR_VISIT_MOCK_TOP_AVG ");
            stb.append("   , TRVA.POINT   AS RECR_VISIT_ACTIVE_POINT ");
            stb.append("   , TRCW.REMARK  AS RECR_WRAPUP_DAT_REMARK ");
            
            stb.append("   , TPSM.PRISCHOOL_NAME ");
            stb.append("   , TPSCM.PRISCHOOL_NAME AS PRISCHOOL_CLASS_NAME ");

            stb.append("     FROM RECRUIT_DAT TRECR ");
            stb.append("     LEFT JOIN FINSCHOOL_MST TFIN ON TFIN.FINSCHOOLCD = TRECR.FINSCHOOLCD ");
            stb.append("     LEFT JOIN NAME_MST NML001 ON NML001.NAMECD1 = 'L001' ");
            stb.append("         AND NML001.NAMECD2 = TFIN.FINSCHOOL_DISTCD ");
            stb.append("     LEFT JOIN RECRUIT_VISIT_DAT TRECV ON TRECV.YEAR = TRECR.YEAR ");
            stb.append("         AND TRECV.RECRUIT_NO = TRECR.RECRUIT_NO ");
            
            stb.append("     LEFT JOIN ( ");
            stb.append("       SELECT DISTINCT ");
            stb.append("           T1.COURSECD ");
            stb.append("         , T1.MAJORCD ");
            stb.append("         , T1.EXAMCOURSECD ");
            stb.append("         , T1.EXAMCOURSE_ABBV ");
            stb.append("       FROM ENTEXAM_COURSE_MST T1 ");
            stb.append("       WHERE T1.ENTEXAMYEAR = '" + param._year + "' ");
            stb.append("         AND T1.APPLICANTDIV = '2') CRSM ON ");
            stb.append("             CRSM.COURSECD = TRECV.HOPE_COURSECD ");
            stb.append("         AND CRSM.MAJORCD = TRECV.HOPE_MAJORCD ");
            stb.append("         AND CRSM.EXAMCOURSECD = TRECV.HOPE_COURSECODE ");

            stb.append("     LEFT JOIN (SELECT T1.* ");
            stb.append("                   FROM RECRUIT_VISIT_SCORE_DAT T1 ");
            stb.append("                   INNER JOIN (SELECT YEAR, RECRUIT_NO, MAX(SEMESTER) AS SEMESTER ");
            stb.append("                                    FROM RECRUIT_VISIT_SCORE_DAT T2 ");
            stb.append("                                    WHERE YEAR = '" + param._year + "' ");
            stb.append("                                      AND SELECT_DIV = '1' ");
            stb.append("                                    GROUP BY YEAR, RECRUIT_NO ");
            stb.append("                                  ) T2 ON T2.YEAR = T1.YEAR  ");
            stb.append("                               AND T2.RECRUIT_NO = T1.RECRUIT_NO ");
            stb.append("                               AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("                 ) TRVS ON TRVS.YEAR = TRECR.YEAR ");
            stb.append("         AND TRVS.RECRUIT_NO = TRECR.RECRUIT_NO ");
            stb.append("         AND TRECV.SCORE_CHK = '1' ");
            stb.append("         LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025' ");
            stb.append("             AND NML025.NAMECD2 = TRECV.JUDGE_KIND ");
            stb.append("         LEFT JOIN RECRUIT_VISIT_MOCK_DAT TRVM ON TRVM.YEAR = TRECR.YEAR ");
            stb.append("             AND TRVM.RECRUIT_NO = TRECR.RECRUIT_NO ");
            stb.append("             AND TRVM.MONTH = '99' ");
            stb.append("         LEFT JOIN ( ");
            stb.append("             SELECT ");
            stb.append("                 T1.RECRUIT_NO, ");
            stb.append("                 SUM(SMALLINT(N1.NAMESPARE1)) AS POINT ");
            stb.append("             FROM ");
            stb.append("                 RECRUIT_VISIT_ACTIVE_DAT T1 ");
            stb.append("                 LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L408' AND N1.NAMECD2 = T1.SEQ ");
            stb.append("             WHERE ");
            stb.append("                 YEAR = '" + param._year + "' ");
            stb.append("                 AND SEQ_DIV = '1' ");
            stb.append("                 AND REMARK1 = '1' ");
            stb.append("             GROUP BY ");
            stb.append("                 T1.RECRUIT_NO ");
            stb.append("         ) TRVA ON TRVA.RECRUIT_NO = TRECR.RECRUIT_NO ");
            stb.append("     LEFT JOIN RECRUIT_CONSULT_WRAPUP_DAT TRCW ON TRCW.YEAR = TRECR.YEAR ");
            stb.append("         AND TRCW.RECRUIT_NO = TRECR.RECRUIT_NO ");
            
            stb.append("     LEFT JOIN PRISCHOOL_MST TPSM ON TPSM.PRISCHOOLCD = TRECR.PRISCHOOLCD ");
            stb.append("     LEFT JOIN PRISCHOOL_CLASS_MST TPSCM ON TPSCM.PRISCHOOLCD = TRECR.PRISCHOOLCD ");
            stb.append("         AND TPSCM.PRISCHOOL_CLASS_CD = TRECR.PRISCHOOL_CLASS_CD ");

            stb.append(" WHERE ");
            stb.append("     TRECR.YEAR = '" + param._year + "' ");
            stb.append("     AND TRECR.SCHOOL_KIND = '" + param._schoolKind + "' ");
            if ("1".equals(param._outputKakutei)) {
                stb.append("     AND TRECV.KAKUTEI_DATE IS NOT NULL ");
            }
            stb.append(" ORDER BY ");
            stb.append("     TRECR.RECRUIT_NO ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {

        final String _year;
        final String _schoolKind;
        final String _currentTime;
        final String _loginDate;
        final String _outputKakutei;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _loginDate = request.getParameter("LOGIN_DATE");
            _currentTime = currentTime();
            _outputKakutei = request.getParameter("OUTPUT_KAKUTEI");
        }

        private static String currentTime() {
            final Calendar cal = Calendar.getInstance();
            final int year = cal.get(Calendar.YEAR);
            final int month = cal.get(Calendar.MONTH) + 1;
            final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            final String dow = String.valueOf(" 日月火水木金土".charAt(cal.get(Calendar.DAY_OF_WEEK)));
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int min = cal.get(Calendar.MINUTE);
            final DecimalFormat df = new DecimalFormat("00");
            return KNJ_EditDate.h_format_JP(year + "-" + month + "-" + dayOfMonth) + "(" + dow + ") " + df.format(hour) + ":" + df.format(min); 
        }
    }
    
}

// eof

