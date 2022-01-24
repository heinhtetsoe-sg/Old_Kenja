/*
 * $Id: 8eb26f2b23c443373cedaef48e3175c0d7700083 $
 *
 * 作成日: 2010/11/09
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２５Ｙ＞  判定結果一覧
 **/
public class KNJL325Y {

    private static final Log log = LogFactory.getLog(KNJL325Y.class);

    private boolean _hasData;

    private final String NEWBLOCK_JUDGE_KIND1 = "NEWBLOCK_JUDGE_KIND1";
    private final String NEWBLOCK_JUDGE_KIND2 = "NEWBLOCK_JUDGE_KIND2";
    private final String NEWBLOCK_EXAMCOURSECD = "NEWBLOCK_EXAMCOURSECD";
    private final String NEWBLOCK_RECOMKIND = "NEWBLOCK_RECOMKIND";

    Param _param;
    
    private String K_ATTEND_FLG = "K_ATTEND_FLG";
    private String K_JUDGE_KIND1 = "K_JUDGE_KIND1";
    private String K_JUDGE_KIND2 = "K_JUDGE_KIND2";
    private String K_JUDGEMENT = "K_JUDGEMENT";
    private String K_JUDGEMENT_NULL = "K_JUDGEMENT_NULL";
    
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
    
    private int getMS932count(String str) {
        int count = 0;
        try {
            count = str.getBytes("MS932").length;
        } catch (Exception e) {
            log.error(e);
        }
        return count;
    }
    
    private Map getMajorNameMap(DB2UDB db2) {
        final Map map = new TreeMap();
        map.put("001", "普通科");
        map.put("002", "英語科");
        return map;
    }
    
    private String alt(final String str, final String alt) {
        return (str == null) ? alt : str;
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final String frm = "2".equals(_param._applicantDiv) ? "KNJL325Y_2.frm" : "KNJL325Y_1.frm";
        final Map majorMap = getMajorNameMap(db2);
        final Set majorcds = majorMap.keySet();
        final String[][] flgs;
        if ("1".equals(_param._applicantDiv)) {
            flgs = new String[][]{
                    new String[]{K_ATTEND_FLG, null},
                    new String[]{K_JUDGEMENT, NEWBLOCK_EXAMCOURSECD},
                    new String[]{K_JUDGEMENT_NULL, NEWBLOCK_EXAMCOURSECD},
                };
        } else {
            flgs = new String[][]{
                    new String[]{K_ATTEND_FLG, null},
                    new String[]{K_JUDGE_KIND1, NEWBLOCK_JUDGE_KIND1},
                    new String[]{K_JUDGE_KIND2, NEWBLOCK_JUDGE_KIND2},
                    new String[]{K_JUDGEMENT, ("2".equals(_param._testDiv)) ? NEWBLOCK_RECOMKIND : NEWBLOCK_EXAMCOURSECD},
                    new String[]{K_JUDGEMENT_NULL, ("2".equals(_param._testDiv)) ? NEWBLOCK_RECOMKIND : NEWBLOCK_EXAMCOURSECD},
                };
        }
        final List added = new ArrayList();
        for (final Iterator it0 = majorcds.iterator(); it0.hasNext(); ) {
            svf.VrSetForm(frm, 4);
            final String majorCd = (String) it0.next();
            final String majorName = alt((String) majorMap.get(majorCd), "");
            
            List printBlockList = new ArrayList();
            try {
                boolean isFirst = true;
                for (int i = 0; i < flgs.length; i++) {
                    final String kindFlg = flgs[i][0];
                    final String newBlockFlg = flgs[i][1];
                    final List applicants = getApplicants(db2, kindFlg, newBlockFlg, majorCd);
//                    log.debug(" ★ kind = " + kindFlg + " , block = " + newBlockFlg + " , applicants size = " + applicants.size());
                    if (0 == applicants.size()) {
                        continue;
                    }
                    String attendFlg = "_";
                    String nml025Namespare1 = "_";
                    String nml025Namespare2 = "_";
                    String recomKind = "_";
                    String tCourseCd = "_";
                    
                    PrintBlock pb = null;
                    for (final Iterator it = applicants.iterator(); it.hasNext(); ) {
                        final Applicant applicant = (Applicant) it.next();
                        if (added.contains(applicant._examno)) continue;
                        if (K_ATTEND_FLG.equals(kindFlg) && null == applicant._attendFlg) continue;
                        if (NEWBLOCK_RECOMKIND.equals(newBlockFlg) && null == applicant._recomKind) continue;
                        if ("2".equals(_param._applicantDiv)) {
                            if ((K_JUDGE_KIND1.equals(kindFlg) || K_JUDGE_KIND2.equals(kindFlg)) && null == applicant._judgeKind) continue;
                            if (NEWBLOCK_EXAMCOURSECD.equals(newBlockFlg) && null == applicant._tCourseCd) continue;
                        }
                        
                        final boolean isNewBlock = isNewBlock(newBlockFlg, kindFlg,
                                attendFlg, applicant._attendFlg,
                                nml025Namespare1, applicant._nml025Namespare1,
                                nml025Namespare2, applicant._nml025Namespare2,
                                tCourseCd, applicant._tCourseCd,
                                recomKind, applicant._recomKind);
//                        log.debug(" isNewBlock? " + isNewBlock);
                        if (pb == null || isNewBlock) {
                            final String recomName = !NEWBLOCK_RECOMKIND.equals(newBlockFlg) ? "" : "3".equals(applicant._recomKind) ? "英検" : "2".equals(applicant._recomKind) ? "スポーツ等" : "学力";
                            pb = new PrintBlock(isFirst, majorName, getBlockTitle(majorName + recomName, kindFlg, newBlockFlg, applicant));
                            isFirst = false;
                            printBlockList.add(pb);
                        }
                        pb._applicants.add(applicant);
                        added.add(applicant._examno);
                        
                        _hasData = true;
                        
                        attendFlg = applicant._attendFlg;
                        nml025Namespare1 = applicant._nml025Namespare1;
                        nml025Namespare2 = applicant._nml025Namespare2;
                        recomKind = applicant._recomKind;
                        tCourseCd = applicant._tCourseCd;
                    }
                }
                
                int maxLine = 52;
                final int totalPage = getTotalPage(printBlockList, maxLine);
                int lines = 0;
                int page = 1;
                for (final Iterator it = printBlockList.iterator(); it.hasNext();) {
                    final PrintBlock pb = (PrintBlock) it.next();
//                    log.debug(" pb = " + pb.toString());
                    if (0 != pb._applicants.size()) {
                        if (!pb._isFirst) {
                            svf.VrsOut("COURSE_HEADER", "*");
                            lines += 1;
                            svf.VrEndRecord();
                        }
                        svf.VrsOut("COURSE", alt(pb._title, "") + "（" + pb._applicants.size() + "名）");
                        lines += 1;
                        svf.VrEndRecord();
                        printHeader(svf);
                        lines += 1;
                        svf.VrEndRecord();
                        for (int k = 0; k < pb._applicants.size(); k++) {
                            lines += 1;
                            if (maxLine < lines) {
                                lines -= maxLine;
                                page += 1;
                            }
                            final Applicant pa = (Applicant) pb._applicants.get(k);
                            printApplicant(svf, (k + 1), pa, pb._majorName, page, totalPage);
                            svf.VrEndRecord();
                        }
                    }
                }
                
            } catch (Exception ex) {
                log.error("Exception:", ex);
            }
        }
    }
    
    private int getTotalPage(final List printBlockList, final int maxLine) {
        int lines = 0;
        for (final Iterator it = printBlockList.iterator(); it.hasNext();) {
            final PrintBlock pb = (PrintBlock) it.next();
            if (0 != pb._applicants.size()) {
                lines += pb.getLineCount();
            }
        }
        return lines / maxLine + (lines % maxLine == 0 ? 0 : 1);
    }

    private String getBlockTitle(final String majorName, final String kindFlg, final String newBlockFlg, final Applicant applicant) {
        final String blockTitle;
        if ("1".equals(_param._applicantDiv)) {
            if (K_ATTEND_FLG.equals(kindFlg)) {
                blockTitle = "欠席"; 
            } else {
                blockTitle = (K_JUDGEMENT.equals(kindFlg) ? "合格者" : "不合格者 "); 
            }
        } else {
            if (K_ATTEND_FLG.equals(kindFlg)) {
                blockTitle = majorName + "欠席"; 
            } else if (NEWBLOCK_JUDGE_KIND1.equals(newBlockFlg)) {
                blockTitle = majorName + "学業 特別奨学生";
            } else if (NEWBLOCK_JUDGE_KIND2.equals(newBlockFlg)) {
                blockTitle = majorName + "スポーツ等優秀生 特別奨学生";
            } else if (NEWBLOCK_EXAMCOURSECD.equals(newBlockFlg)) {
                blockTitle = applicant._tCourseName + (K_JUDGEMENT.equals(kindFlg) ? "合格者" : "不合格者 "); 
            } else if (NEWBLOCK_RECOMKIND.equals(newBlockFlg)) {
                blockTitle = majorName + "推薦" + (K_JUDGEMENT.equals(kindFlg) ? "合格者" : "不合格者 "); 
            } else {
                blockTitle = "";
            }
        }
        return blockTitle;
    }

    public boolean isNewBlock(final String flg, final String kindFlg,
            final String attendFlg, final String newAttendFlg,
            final String nml025Namespare1, final String newNml025Namespare1,
            final String nml025Namespare2, final String newNml025Namespare2,
            final String tCourseCd, final String tExamCourseCd,
            final String recomKind, final String newRecomKind) {
        final boolean isNewAttendFlg = K_ATTEND_FLG.equals(kindFlg) && "_".equals(attendFlg) || null != newAttendFlg && !attendFlg.equals(newAttendFlg);
        final boolean isNewJk1 = NEWBLOCK_JUDGE_KIND1.equals(flg) && ("_".equals(nml025Namespare1) || null != nml025Namespare1 && !nml025Namespare1.equals(newNml025Namespare1));
        final boolean isNewJk2 = NEWBLOCK_JUDGE_KIND2.equals(flg) && ("_".equals(nml025Namespare2) || null != nml025Namespare2 && !nml025Namespare2.equals(newNml025Namespare2));
        final boolean isNewCourse = NEWBLOCK_EXAMCOURSECD.equals(flg) && ("_".equals(tCourseCd) || null != tCourseCd && !tCourseCd.equals(tExamCourseCd));
        final boolean isNewRecomKind = NEWBLOCK_RECOMKIND.equals(flg) && ("_".equals(recomKind) || null != recomKind && !recomKind.equals(newRecomKind));
        return isNewAttendFlg || isNewJk1 || isNewJk2 || isNewCourse || isNewRecomKind;
    }

    private void printApplicant(final Vrw32alp svf, final int lineno, final Applicant applicant, final String subTitle, final int page, final int maxPage) {
        svf.VrsOut("NENDO",     _param._entexamYear + "年度");
        svf.VrsOut("TITLE",     _param._title + _param._testdivName);
        svf.VrsOut("PAGE1",     String.valueOf(page));
        svf.VrsOut("PAGE2",     String.valueOf(maxPage));
        if ("2".equals(_param._applicantDiv)) {
            svf.VrsOut("SUBTITLE",  "（" + alt(subTitle, "") + "）");
        }
        final int nameLen = getMS932count(applicant._name);
        final String nameField = "NAME" + (30 < nameLen ? "3" : 20 < nameLen ? "2" : "1");
        final int nameKanaLen = getMS932count(applicant._nameKana);
        final String nameKanaField = "NAME_KANA" + (30 < nameKanaLen ? "3" : 20 < nameKanaLen ? "2" : "1");
        
        svf.VrsOut("SEATNO", String.valueOf(lineno));
        svf.VrsOut("JUDGE", null != applicant._judgeKind && "1".equals(applicant._namespare1) ? applicant._judgeKindName : applicant._judgedivName);
        svf.VrsOut("JUDGE_COURSE", applicant._tCourseAbbv);
        svf.VrsOut("EXAMNO", applicant._examno);
        svf.VrsOut(nameField, applicant._name);
        svf.VrsOut(nameKanaField, applicant._nameKana);
        svf.VrsOut("SEX", applicant._sexname);
        svf.VrsOut("FINSCHOOL_ABBV", applicant._finschoolNameAbbv);
        svf.VrsOut("CONFRPT", applicant._averageAll);
        svf.VrsOut("DESIRE", applicant._shdivName);
        svf.VrsOut("SH_SCHOOL", applicant._shschoolNameAbbv);
        svf.VrsOut("EXAMCOURSE_NAME1", applicant._desiredCoursesAbbv);
        svf.VrsOut("DORMITORY", applicant._dormitoryFlg);
        svf.VrsOut("REMARK3", "5".equals(applicant._judgediv) ? "特別判定" : "");
    }
    
    private void printHeader(final Vrw32alp svf) {
        svf.VrsOut("HEADER_SEATNO",           "No.");
        svf.VrsOut("HEADER_JUDGE",            "判定");
        svf.VrsOut("HEADER_JUDGE_COURSE1",    "合否");
        svf.VrsOut("HEADER_JUDGE_COURSE2",    "コース");
        svf.VrsOut("HEADER_EXAMNO",           "受験番号");
        svf.VrsOut("HEADER_NAME1",            "氏名");
        svf.VrsOut("HEADER_NAME_KANA1",       "氏名かな");
        svf.VrsOut("HEADER_SEX",              "性別");
        svf.VrsOut("HEADER_FINSCHOOL_ABBV",   "出身学校");
        svf.VrsOut("HEADER_CONFRPT",          "評定合計");
        svf.VrsOut("HEADER_EXAMCOURSE_NAME1", "志望学科");
        svf.VrsOut("HEADER_DESIRE",           "専／併");
        svf.VrsOut("HEADER_SH_SCHOOL",        "併願校");
        svf.VrsOut("HEADER_DOMITORY",         "寮");
        svf.VrsOut("HEADER_REMARK",           "備考");
    }

    private List getApplicants(final DB2UDB db2, final String kindFlg, final String newBlockFlg, final String majorCd) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final List applicants = new ArrayList();
        try {
            final String sql = getApplicantSql(kindFlg, newBlockFlg, majorCd);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String judgediv = rs.getString("JUDGEDIV");
                final String attendFlg = rs.getString("ATTEND_FLG");
                final String averageAll = NumberUtils.isNumber(rs.getString("AVERAGE_ALL")) ? String.valueOf(new BigDecimal(rs.getString("AVERAGE_ALL")).setScale(0)) : null;
                final String sucexamcoursecd1 = rs.getString("SUC_EXAMCOURSECD");
                final String sucexamcourseName1 = rs.getString("SUC_EXAMCOURSE_NAME");
                final String sucexamcourseAbbv1 = rs.getString("SUC_EXAMCOURSE_ABBV");
                final String examcoursecd1 = rs.getString("EXAMCOURSECD1");
                final String examcourseName1 = rs.getString("EXAMCOURSE_NAME1");
                final String examcourseAbbv1 = rs.getString("EXAMCOURSE_ABBV1");
                final String examcoursecd2 = rs.getString("EXAMCOURSECD2");
                final String examcourseName2 = rs.getString("EXAMCOURSE_NAME2");
                final String examcourseAbbv2 = rs.getString("EXAMCOURSE_ABBV2");
                final String slideFlg = rs.getString("SLIDE_FLG");
                final String namespare1 = rs.getString("NAMESPARE1");
                final String nml025Namespare1 = rs.getString("NML025NAMESPARE1");
                final String nml025Namespare2 = rs.getString("NML025NAMESPARE2");
                final String tCourseCd;
                final String tCourseName;
                final String tCourseAbbv;
                final String desiredCoursesAbbv;
                if ("5".equals(judgediv)) { // 特別判定合格した場合
                	if (null != examcoursecd2) {
                        desiredCoursesAbbv = examcourseAbbv1 + "・" + examcourseAbbv2;
                	} else {
                        desiredCoursesAbbv = examcourseAbbv1;
                	}
                	tCourseCd = sucexamcoursecd1;
                	tCourseName = sucexamcourseName1; 
                	tCourseAbbv = sucexamcourseAbbv1;
                } else if ("1".equals(slideFlg) && null != examcoursecd2) { // スライド
                    desiredCoursesAbbv = examcourseAbbv1 + "・" + examcourseAbbv2;
                    if ("3".equals(judgediv) || !"4".equals(judgediv) && !"1".equals(namespare1)) { // スライドで合格した場合 / スライド希望で欠席以外の不合格した場合
                        tCourseCd = examcoursecd2;
                        tCourseName = examcourseName2;
                        tCourseAbbv = examcourseAbbv2;
                    } else {
                        tCourseCd = examcoursecd1;
                        tCourseName = examcourseName1;
                        tCourseAbbv = examcourseAbbv1;
                    }
                } else {
                    desiredCoursesAbbv = examcourseAbbv1;
                    tCourseCd = examcoursecd1;
                    tCourseName = examcourseName1; 
                    tCourseAbbv = examcourseAbbv1;
                }
                final String sportsFlg = rs.getString("SPORTS_FLG");
                final String dormitoryFlg = "1".equals(rs.getString("DORMITORY_FLG")) ? "有" : "";
                final String judgedivName = K_ATTEND_FLG.equals(kindFlg) ? "欠席" : rs.getString("JUDGEDIV_NAME");
                final String judgedivAbbv = K_ATTEND_FLG.equals(kindFlg) ? "欠席" : rs.getString("JUDGEDIV_ABBV");
                final String fsCd = rs.getString("FS_CD");
                final String sexname = rs.getString("SEXNAME");
                final String judgeKind = rs.getString("JUDGE_KIND");
                final String judgeKindName = K_ATTEND_FLG.equals(kindFlg) ? "欠席" : rs.getString("JUDGE_KIND_NAME");
                final String finschoolNameAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String shschoolNameAbbv = rs.getString("SHSCHOOL_NAME_ABBV");
                final String shdivName = rs.getString("SHDIVNAME");
                final String recomKind = rs.getString("RECOM_KIND");
                
                final Applicant applicant = new Applicant(examno, name, nameKana, judgediv, attendFlg, averageAll,
                        examcoursecd1, examcourseName1, examcourseAbbv1, examcoursecd2, examcourseName2, examcourseAbbv2, desiredCoursesAbbv,
                        tCourseCd, tCourseName, tCourseAbbv, slideFlg, sportsFlg, dormitoryFlg, namespare1, nml025Namespare1, nml025Namespare2,
                        judgedivName, judgedivAbbv, fsCd, sexname, judgeKind, judgeKindName, finschoolNameAbbv, shschoolNameAbbv, shdivName, recomKind);
                applicants.add(applicant);
            }
            
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return applicants;
    }
    
    private String getApplicantSql(final String kindFlg, final String newBlockFlg, final String majorCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO ");
        stb.append("     ,T1.NAME ");
        stb.append("     ,T1.NAME_KANA ");
        stb.append("     ,T13.JUDGEDIV ");
        stb.append("     ,CASE WHEN VALUE(T13.JUDGEDIV, '') = '4' THEN '1' ELSE '0' END AS ATTEND_FLG ");
        stb.append("     ,T9.AVERAGE_ALL ");
        stb.append("     ,T4.EXAMCOURSECD AS EXAMCOURSECD1 ");
        stb.append("     ,T5.EXAMCOURSE_NAME AS EXAMCOURSE_NAME1 ");
        stb.append("     ,T5.EXAMCOURSE_ABBV AS EXAMCOURSE_ABBV1 ");
        stb.append("     ,CASE WHEN T1.TESTDIV = '" + _param._testDiv + "' AND VALUE(T1.SLIDE_FLG, '') = '1' THEN T7.EXAMCOURSECD END AS EXAMCOURSECD2 ");
        stb.append("     ,CASE WHEN T1.TESTDIV = '" + _param._testDiv + "' AND VALUE(T1.SLIDE_FLG, '') = '1' THEN T8.EXAMCOURSE_NAME END AS EXAMCOURSE_NAME2 ");
        stb.append("     ,CASE WHEN T1.TESTDIV = '" + _param._testDiv + "' AND VALUE(T1.SLIDE_FLG, '') = '1' THEN T8.EXAMCOURSE_ABBV END AS EXAMCOURSE_ABBV2 ");
        stb.append("     ,T1.SLIDE_FLG ");
        stb.append("     ,T1.SPORTS_FLG ");
        stb.append("     ,T1.DORMITORY_FLG ");
        stb.append("     ,NML013.NAMESPARE1 ");
        stb.append("     ,NML013.NAME2 AS JUDGEDIV_NAME ");
        stb.append("     ,NML013.ABBV1 AS JUDGEDIV_ABBV ");
        stb.append("     ,T1.FS_CD ");
        stb.append("     ,NMZ002.NAME2 AS SEXNAME ");
        stb.append("     ,T2.FINSCHOOL_NAME_ABBV ");
        stb.append("     ,T1.JUDGE_KIND ");
        stb.append("     ,NML025.NAME2 AS JUDGE_KIND_NAME ");
        stb.append("     ,NML025.NAMESPARE1 AS NML025NAMESPARE1 ");
        stb.append("     ,NML025.NAMESPARE2 AS NML025NAMESPARE2 ");
        stb.append("     ,T11.FINSCHOOL_NAME_ABBV AS SHSCHOOL_NAME_ABBV ");
        stb.append("     ,T3.SHDIV ");
        stb.append("     ,NML006.NAME1 AS SHDIVNAME ");
        stb.append("     ,T3.RECOM_KIND ");
        stb.append("     ,CASE WHEN T1.TESTDIV = '" + _param._testDiv + "' AND VALUE(T1.SLIDE_FLG, '') = '1' AND T7.EXAMCOURSECD IS NOT NULL AND VALUE(T13.JUDGEDIV, '') IN ('3') THEN 1 ELSE 0 END AS SLIDE ");
        stb.append("     ,CASE WHEN VALUE(T1.SLIDE_FLG, '') <> '1' THEN 0 ELSE 1 END AS SLIDE2 ");
        stb.append("     ,T10.EXAMCOURSECD AS SUC_EXAMCOURSECD ");
        stb.append("     ,T10.EXAMCOURSE_NAME AS SUC_EXAMCOURSE_NAME ");
        stb.append("     ,T10.EXAMCOURSE_ABBV AS SUC_EXAMCOURSE_ABBV ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T13 ON T13.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T13.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T13.EXAM_TYPE = '1' ");
        stb.append("         AND T13.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTDESIRE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T3.EXAMNO = T1.EXAMNO ");
        stb.append("         AND T3.TESTDIV = T13.TESTDIV ");
        stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T4 ON T4.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
        stb.append("         AND T4.APPLICANTDIV = T3.APPLICANTDIV ");
        stb.append("         AND T4.TESTDIV = T3.TESTDIV ");
        stb.append("         AND T4.DESIREDIV = T3.DESIREDIV ");
        stb.append("         AND T4.WISHNO = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T5 ON T5.ENTEXAMYEAR = T4.ENTEXAMYEAR ");
        stb.append("         AND T5.APPLICANTDIV = T4.APPLICANTDIV ");
        stb.append("         AND T5.TESTDIV = T4.TESTDIV ");
        stb.append("         AND T5.COURSECD = T4.COURSECD ");
        stb.append("         AND T5.MAJORCD = T4.MAJORCD ");
        stb.append("         AND T5.EXAMCOURSECD = T4.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T7 ON T7.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
        stb.append("         AND T7.APPLICANTDIV = T3.APPLICANTDIV ");
        stb.append("         AND T7.TESTDIV = T3.TESTDIV ");
        stb.append("         AND T7.DESIREDIV = T3.DESIREDIV ");
        stb.append("         AND T7.WISHNO = '2' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T8 ON T8.ENTEXAMYEAR = T7.ENTEXAMYEAR ");
        stb.append("         AND T8.APPLICANTDIV = T7.APPLICANTDIV ");
        stb.append("         AND T8.TESTDIV = T7.TESTDIV ");
        stb.append("         AND T8.COURSECD = T7.COURSECD ");
        stb.append("         AND T8.MAJORCD = T7.MAJORCD ");
        stb.append("         AND T8.EXAMCOURSECD = T7.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T9 ON T9.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T9.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T10 ON T10.ENTEXAMYEAR = T13.ENTEXAMYEAR ");
        stb.append("         AND T10.APPLICANTDIV = T13.APPLICANTDIV ");
        stb.append("         AND T10.TESTDIV = T13.TESTDIV ");
        stb.append("         AND T10.COURSECD = T1.SUC_COURSECD ");
        stb.append("         AND T10.MAJORCD = T1.SUC_MAJORCD ");
        stb.append("         AND T10.EXAMCOURSECD = T1.SUC_COURSECODE ");
        stb.append("     LEFT JOIN FINHIGHSCHOOL_MST T11 ON T11.FINSCHOOLCD = T1.SH_SCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST NML006 ON NML006.NAMECD1 = 'L006' AND NML006.NAMECD2 = T3.SHDIV ");
        stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = T13.JUDGEDIV ");
        stb.append("     LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025' AND NML025.NAMECD2 = T1.JUDGE_KIND ");
        stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T1.SEX ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T13.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND T4.MAJORCD = '" + majorCd +"' ");
        if ("2".equals(_param._applicantDiv)) {
            if ("1".equals(_param._inout)) {
                stb.append("     AND SUBSTR(T1.EXAMNO, 1, 1) <> '6' ");
            } else if ("2".equals(_param._inout)) {
                stb.append("     AND SUBSTR(T1.EXAMNO, 1, 1) = '6' ");
            }
        }
        if (K_ATTEND_FLG.equals(kindFlg)) {
            stb.append("     AND T13.JUDGEDIV = '4' ");
        } else {
            stb.append("     AND VALUE(T13.JUDGEDIV, '') <> '4' ");
        }
        if (K_JUDGE_KIND1.equals(kindFlg)) {
            stb.append("     AND T13.JUDGEDIV IS NOT NULL AND NML013.NAMESPARE1 = '1' ");
            stb.append("     AND T1.JUDGE_KIND IS NOT NULL AND VALUE(NML025.NAMESPARE1, '') = '1' ");
        } else if (K_JUDGE_KIND2.equals(kindFlg)) {
            stb.append("     AND T13.JUDGEDIV IS NOT NULL AND NML013.NAMESPARE1 = '1' ");
            stb.append("     AND T1.JUDGE_KIND IS NOT NULL AND VALUE(NML025.NAMESPARE2, '') = '1' ");
        } else {
//            stb.append("     AND T1.JUDGE_KIND IS NULL ");
        }
        if (K_JUDGEMENT.equals(kindFlg)) {
            stb.append("     AND T13.JUDGEDIV IS NOT NULL AND NML013.NAMESPARE1 = '1' ");
        } else if (K_JUDGEMENT_NULL.equals(kindFlg)) {
            stb.append("     AND T13.JUDGEDIV IS NOT NULL AND VALUE(NML013.NAMESPARE1, '') <> '1' ");
        }
        stb.append(" ORDER BY ");
        if ("2".equals(_param._applicantDiv)) {
            if (K_JUDGE_KIND1.equals(kindFlg) || K_JUDGE_KIND2.equals(kindFlg)) {
                stb.append("     T1.JUDGE_KIND, ");
            } else if (K_JUDGEMENT.equals(kindFlg)) {
                if (NEWBLOCK_EXAMCOURSECD.equals(newBlockFlg)) {
                    stb.append("     SLIDE, ");
                    stb.append("     T4.EXAMCOURSECD, ");
                } else if (NEWBLOCK_RECOMKIND.equals(newBlockFlg)) {
                    stb.append("     T3.RECOM_KIND, ");
                }
            } else if (K_JUDGEMENT_NULL.equals(kindFlg)) {
                if (NEWBLOCK_EXAMCOURSECD.equals(newBlockFlg)) {
                    stb.append("     T3.DESIREDIV, ");
                    stb.append("     SLIDE2, ");
                } else if (NEWBLOCK_RECOMKIND.equals(newBlockFlg)) {
                    stb.append("     T3.RECOM_KIND, ");
                }
            }
        }
        stb.append("     T1.EXAMNO ");
        return stb.toString();
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72188 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }
    
    private static class PrintBlock {
        final boolean _isFirst;
        final String _majorName;
        final List _applicants = new ArrayList();
        final String _title;
        PrintBlock(final boolean isFirst, final String majorName, final String title) {
            _isFirst = isFirst;
            _majorName = majorName;
            _title = title;
        }
        public int getLineCount() {
            int line = 0;
            line += (_isFirst) ? 0 : 1; // 最初のブロック以外は1行空行
            line += 2;                  // ヘッダ用
            line += _applicants.size(); // 生徒数
            return line;
        }
        public String toString() {
            return " [PrintBlock : title = " + _title + " , isFirst = " + _isFirst + " , applicants size = " + _applicants.size() + "]";
        }
    }
    
    private static class Applicant {
        final String _examno;
        final String _name;
        final String _nameKana;
        final String _judgediv;
        final String _attendFlg;
        final String _averageAll;
        final String _examcoursecd1;
        final String _examcourseName1;
        final String _examcourseAbbv1;
        final String _examcoursecd2;
        final String _examcourseName2;
        final String _examcourseAbbv2;
        final String _desiredCoursesAbbv;
        final String _tCourseCd;
        final String _tCourseName;
        final String _tCourseAbbv;
        final String _slideFlg;
        final String _sportsFlg;
        final String _dormitoryFlg;
        final String _namespare1;
        final String _nml025Namespare1;
        final String _nml025Namespare2;
        final String _judgedivName;
        final String _judgedivAbbv;
        final String _fsCd;
        final String _sexname;
        final String _judgeKind;
        final String _judgeKindName;
        final String _finschoolNameAbbv;
        final String _shschoolNameAbbv;
        final String _shdivName;
        final String _recomKind;
        public Applicant(
                final String examno,
                final String name,
                final String nameKana,
                final String judgediv,
                final String attendFlg,
                final String averageAll,
                final String examcoursecd1,
                final String examcourseName1,
                final String examcourseAbbv1,
                final String examcoursecd2,
                final String examcourseName2,
                final String examcourseAbbv2,
                final String desiredCoursesAbbv,
                final String tCourseCd,
                final String tCourseName,
                final String tCourseAbbv,
                final String slideFlg,
                final String sportsFlg,
                final String dormitoryFlg,
                final String namespare1,
                final String nml025Namespare1,
                final String nml025Namespare2,
                final String judgedivName,
                final String judgedivAbbv,
                final String fsCd,
                final String sexname,
                final String judgeKind,
                final String judgeKindName,
                final String finschoolNameAbbv,
                final String shschoolNameAbbv,
                final String shdivName,
                final String recomKind) {
            _examno = examno;
            _name = name;
            _nameKana = nameKana;
            _judgediv = judgediv;
            _attendFlg = attendFlg;
            _averageAll = averageAll;
            _examcoursecd1 = examcoursecd1;
            _examcourseName1 = examcourseName1;
            _examcourseAbbv1 = examcourseAbbv1;
            _examcoursecd2 = examcoursecd2;
            _examcourseName2 = examcourseName2;
            _examcourseAbbv2 = examcourseAbbv2;
            _desiredCoursesAbbv = desiredCoursesAbbv;
            _tCourseCd = tCourseCd;
            _tCourseName = tCourseName;
            _tCourseAbbv = tCourseAbbv;
            _slideFlg = slideFlg;
            _sportsFlg = sportsFlg;
            _dormitoryFlg = dormitoryFlg;
            _namespare1 = namespare1;
            _nml025Namespare1 = nml025Namespare1;
            _nml025Namespare2 = nml025Namespare2;
            _judgedivName = judgedivName;
            _judgedivAbbv = judgedivAbbv;
            _fsCd = fsCd;
            _sexname = sexname;
            _judgeKind = judgeKind;
            _judgeKindName = judgeKindName;
            _finschoolNameAbbv = finschoolNameAbbv;
            _shschoolNameAbbv = shschoolNameAbbv;
            _shdivName = shdivName;
            _recomKind = recomKind;
        }
        public String toString() {
            return _examno + ": " + _name + " (" + _sexname + ") :" + _examcoursecd1 + " : " + _judgeKindName + " :  "+ _recomKind;
        }
    }
    
    /** パラメータクラス */
    private class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _title;
        final String _testdivName;
        final String _inout; // 1:外部生のみ、2:内部生のみ、3:全て
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _title = getApplicantdivName(db2);
            _testdivName = getTestDivName(db2);
            _inout = request.getParameter("INOUT");
        }
        
        private String getApplicantdivName(DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  schoolName = rs.getString("NAME1"); 
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }
        
        private String getTestDivName(DB2UDB db2) {
            String testDivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  testDivName = rs.getString("NAME1"); 
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDivName;
        }
    }
}

// eof

