/*
 * $Id: ff2a6b74d16b52ae21c0713e850d1f03bcc74d50 $
 *
 * 作成日: 2012/11/29
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 科目受講者数
 */
public class KNJM826 {

    private static final Log log = LogFactory.getLog(KNJM826.class);

    private static final String INOUTCD_MULTI = "9";    // 併修生
    private static final String INOUTCD_LECTURE = "8";  // 聴講生

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
    
    private static String count(final Collection col) {
        return col.isEmpty() ? null : String.valueOf(col.size());
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final int subcMaxLine = 58;
        
        final List subclassAllList = getSubclassList(db2);
        final List subclassPageList = getPageList(subclassAllList, subcMaxLine);

        final Collection curriculumYearAllList = getCurriculumYearCol(subclassAllList);
        final List curriculumYearPageList = getPageList(curriculumYearAllList, 20);
        
        final int subcPage = (subclassAllList.size() + 2) / subcMaxLine + ((subclassAllList.size() + 2) % subcMaxLine != 0 ? 1 : 0);
        final int p = (subclassAllList.size() + 3) % subcMaxLine != 0 && (subclassAllList.size() + 3) % subcMaxLine == 1 ? 1 : 0;
        final String totalPage = String.valueOf(subcPage * curriculumYearPageList.size() + p);

        int page = 0;
        for (int i = 0; i <  subclassPageList.size(); i++) {
            final List subclassList = (List) subclassPageList.get(i);
            
            for (int j = 0; j < curriculumYearPageList.size(); j++) {
                final List curriculumYearList = (List) curriculumYearPageList.get(j);
                final boolean isLastCurriculumYearPage = j == curriculumYearPageList.size() - 1;
                
                page += 1;
                
                svf.VrSetForm("KNJM826.frm", 4);
                svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
                svf.VrsOut("PAGE", String.valueOf(page));
                svf.VrsOut("TOTAL_PAGE", totalPage);
                
                
                for (final Iterator shit = subclassList.iterator(); shit.hasNext();) {
                    final Subclass subclass = (Subclass) shit.next();
                    final String suf = subclass._islastClass ? "2" : "1";

                    svf.VrsOut("CLASS_CD" + suf, subclass._subclassCd);
                    svf.VrsOut("SUBCLASS_NAME" + suf + "_" + (getMS932ByteLength(subclass._subclassname) > 20 ? "2" : "1"), subclass._subclassname);

                    for (int ycol = 0; ycol < curriculumYearList.size(); ycol++) {
                        final String curriculumYear = (String) curriculumYearList.get(ycol);
                        final String nendo = KNJ_EditDate.tate2_format(StringUtils.replace(KNJ_EditDate.h_format_JP(curriculumYear + "-04-01"), "元年", "1年"))[1];
                        svf.VrsOutn("ENT_YEAR", (ycol + 1), nendo);
                        svf.VrsOutn("ATTEND_SUM" + suf, (ycol + 1), count(subclass.getColCurriculumYear(curriculumYear)));
                    }
                    if (isLastCurriculumYearPage) {
                        // 合計、併修生、聴講生、横計
                        svf.VrsOut("TOTAL" + suf, count(subclass.getColCurriculumYear(null)));
                        svf.VrsOut("MULTI" + suf, count(subclass.getColInOutCd(INOUTCD_MULTI)));
                        svf.VrsOut("LECTURE" + suf, count(subclass.getColInOutCd(INOUTCD_LECTURE)));
                        svf.VrsOut("SUB_TOTAL" + suf, add(count(subclass.getColInOutCd(INOUTCD_LECTURE)), count(subclass.getColInOutCd(INOUTCD_MULTI))));
                    }
                    svf.VrEndRecord();
                    _hasData = true;
                }

                if (i == subclassPageList.size() - 1) {
                    final int printedLine = subclassList.size();
                    int line = 0;
                    
                    // のべ登録科目数
                    if (isLastCurriculumYearPage) {
                        printTatekei2Nobe(svf, "のべ登録科目数", subclassAllList, curriculumYearAllList);
                        line += 1;
                        if (printedLine + line > subcMaxLine) {
                            page += 1;
                            svf.VrsOut("PAGE", String.valueOf(page));
                            line -= subcMaxLine;
                        }
                        svf.VrEndRecord();
                    }
                    // 登録者数
                    printTatekei(svf, "登録者数", subclassAllList, curriculumYearList, false);
                    if (isLastCurriculumYearPage) {
                        printTatekei2(svf, subclassAllList, curriculumYearAllList, false);
                    }
                    line += 1;
                    if (printedLine + line > subcMaxLine) {
                        page += 1;
                        svf.VrsOut("PAGE", String.valueOf(page));
                        line -= subcMaxLine;
                    }
                    svf.VrEndRecord();
                    
                    // 卒業予定者
                    printTatekei(svf, "うち卒業予定生", subclassAllList, curriculumYearList, true);
                    if (isLastCurriculumYearPage) {
                        printTatekei2(svf, subclassAllList, curriculumYearAllList, true);
                    }
                    line += 1;
                    if (printedLine + line > subcMaxLine) {
                        page += 1;
                        svf.VrsOut("PAGE", String.valueOf(page));
                        line -= subcMaxLine;
                    }
                    svf.VrEndRecord();
                }
            }
        }
    }

    /**
     * のべ人数出力
     */
    private void printTatekei2Nobe(final Vrw32alp svf, final String fullName, final List subclassAllList, final Collection curriculumYearAllList) {
        svf.VrsOut("FULL_NAME", fullName);
        List sumSubcCurYear = new ArrayList();
        List sumSubcInOutLecture = new ArrayList();
        List sumSubcInOutMulti = new ArrayList();
        List sumSubcInOutLecMul = new ArrayList();
        for (final Iterator shit = subclassAllList.iterator(); shit.hasNext();) {
            final Subclass subclass = (Subclass) shit.next();
            
            for (final Iterator pit = curriculumYearAllList.iterator(); pit.hasNext();) {
                final String curriculumYear = (String) pit.next();
                sumSubcCurYear.addAll(subclass.getColCurriculumYear(curriculumYear));
            }
            sumSubcInOutLecture.addAll(subclass.getColInOutCd(INOUTCD_LECTURE));
            sumSubcInOutMulti.addAll(subclass.getColInOutCd(INOUTCD_MULTI));
            sumSubcInOutLecMul.addAll(subclass.getColInOutCd(INOUTCD_LECTURE));
            sumSubcInOutLecMul.addAll(subclass.getColInOutCd(INOUTCD_MULTI));
        }
        // 合計、併修生、聴講生、横計
        svf.VrsOut("FULL_TOTAL", count(sumSubcCurYear));
        svf.VrsOut("FULL_MULTI", count(sumSubcInOutMulti));
        svf.VrsOut("FULL_LECTURE", count(sumSubcInOutLecture));
        svf.VrsOut("FULL_SUB_TOTAL", count(sumSubcInOutLecMul));
    }

    /**
     * 年度ごとの縦計出力
     */
    private void printTatekei(final Vrw32alp svf, final String totalName, final List subclassAllList, final List curriculumYearList, final boolean checkGrdYotei) {
        svf.VrsOut("TOTAL_NAME", totalName);
        for (int i = 0; i < curriculumYearList.size(); i++) {
            final String curriculumYear = (String) curriculumYearList.get(i);
            // 縦計
            final Collection sumSubcCurYear = new HashSet();
            for (final Iterator shit = subclassAllList.iterator(); shit.hasNext();) {
                final Subclass subclass = (Subclass) shit.next();
                
                sumSubcCurYear.addAll(subclass.getColCurriculumYear(curriculumYear, checkGrdYotei));
            }
            svf.VrsOutn("ATTEND_SUM" + "3", (i + 1), count(sumSubcCurYear));
        }
    }

    /**
     * 併修生・聴講生のすべての科目の縦計出力
     */
    private void printTatekei2(final Vrw32alp svf, final List subclassAllList, final Collection curriculumYearAllList, final boolean checkGrdYotei) {
        final Collection sumSubcCurYear = new HashSet(); // 横計
        final Collection sumSubcInOutLecture = new HashSet();
        final Collection sumSubcInOutMulti = new HashSet();
        final Collection sumSubcInOutLecMul = new HashSet();
        for (final Iterator shit = subclassAllList.iterator(); shit.hasNext();) {
            final Subclass subclass = (Subclass) shit.next();
            
            for (final Iterator pit = curriculumYearAllList.iterator(); pit.hasNext();) {
                final String curriculumYear = (String) pit.next();
                sumSubcCurYear.addAll(subclass.getColCurriculumYear(curriculumYear, checkGrdYotei));
            }
            sumSubcInOutLecture.addAll(subclass.getColInOutCd(INOUTCD_LECTURE, checkGrdYotei));
            sumSubcInOutMulti.addAll(subclass.getColInOutCd(INOUTCD_MULTI, checkGrdYotei));
            sumSubcInOutLecMul.addAll(subclass.getColInOutCd(INOUTCD_LECTURE, checkGrdYotei));
            sumSubcInOutLecMul.addAll(subclass.getColInOutCd(INOUTCD_MULTI, checkGrdYotei));
        }
        // 合計、併修生、聴講生、横計
        svf.VrsOut("TOTAL" + "3", count(sumSubcCurYear));
        svf.VrsOut("MULTI" + "3", count(sumSubcInOutMulti));
        svf.VrsOut("LECTURE" + "3", count(sumSubcInOutLecture));
        svf.VrsOut("SUB_TOTAL" + "3", count(sumSubcInOutLecMul));
    }

    /**
     * 表示対象の入学年度を得る
     */
    private Collection getCurriculumYearCol(final List subclassList) {
        final Set s = new TreeSet();
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            for (final Iterator shit = subclass._studentList.iterator(); shit.hasNext();) {
                final Student student = (Student) shit.next();
                if (null != student._curriculumYear) {
                    s.add(student._curriculumYear);
                }
            }
        }
        return s;
    }

    private List getSubclassList(final DB2UDB db2) {
        final List subclassList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Set schregnoSet = new HashSet();
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                final String key = rs.getString("SCHOOL_KIND") + rs.getString("CLASSCD") + rs.getString("SUBCLASSCD") + rs.getString("CURRICULUM_CD");
                Subclass subclass = Subclass.getSubclass(subclassList, key);
                if (null == subclass) {
                    subclass = new Subclass(rs.getString("SUBCLASSNAME"), rs.getString("SCHOOL_KIND"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CURRICULUM_CD"));
                    subclassList.add(subclass);
                }
                subclass._studentList.add(new Student(rs.getString("SCHREGNO"), rs.getString("CURRICULUM_YEAR"), rs.getString("INOUTCD"), rs.getString("GRD_YOTEI")));
                schregnoSet.add(rs.getString("SCHREGNO"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        Subclass before = null;
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            if (null != before && !StringUtils.defaultString(before._classCd).equals(subclass._classCd)) {
                before._islastClass = true;
            }
            before = subclass;
        }
        if (null != before) {
            before._islastClass = true;
        }
//        log.debug(" schregnoSet = " + schregnoSet);
        return subclassList;
    }
    
    /**
     * listをsizeで区切ったリストのリストを得る
     */
    private List getPageList(final Collection list, final int size) {
        final List pageList = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= size) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
        }
        return pageList;
    }
    
    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("     SELECT DISTINCT T1.SCHREGNO, FISCALYEAR(T5.ENT_DATE) AS CURRICULUM_YEAR, T5.INOUTCD, SC_YD.BASE_REMARK1 AS GRD_YOTEI, ");
        stb.append("                     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T6.SUBCLASSNAME ");
        stb.append("     FROM SUBCLASS_STD_SELECT_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("         AND T2.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST SC_YD ON SC_YD.SCHREGNO = T2.SCHREGNO ");
        stb.append("          AND SC_YD.YEAR = '" + _param._year + "' ");
        stb.append("          AND SC_YD.BASE_SEQ = '001' ");
        stb.append("     LEFT JOIN SUBCLASS_MST T6 ON T6.CLASSCD = T1.CLASSCD ");
        stb.append("          AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("          AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("          AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     WHERE T1.YEAR = '" + _param._year + "' ");
        if (!_param._year.equals(_param._ctrlYear)) {
            stb.append("         AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        } else {
            stb.append("         AND T1.SEMESTER = '1' ");
        }
        // 次年度指定の場合はFRESHMAN_DATも対象
        if (!_param._year.equals(_param._ctrlYear)) {
            stb.append("     UNION ");
            stb.append("     SELECT DISTINCT T1.SCHREGNO, T2.ENTERYEAR AS CURRICULUM_YEAR, CAST(NULL AS VARCHAR(1)) AS INOUTCD, SC_YD.BASE_REMARK1 AS GRD_YOTEI, ");
            stb.append("                     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T6.SUBCLASSNAME ");
            stb.append("     FROM SUBCLASS_STD_SELECT_DAT T1 ");
            stb.append("     INNER JOIN FRESHMAN_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.ENTERYEAR = '" + _param._year + "' ");
            stb.append("     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST SC_YD ON SC_YD.SCHREGNO = T2.SCHREGNO ");
            stb.append("          AND SC_YD.YEAR = '" + _param._year + "' ");
            stb.append("          AND SC_YD.BASE_SEQ = '001' ");
            stb.append("     LEFT JOIN SUBCLASS_MST T6 ON T6.CLASSCD = T1.CLASSCD ");
            stb.append("          AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("          AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     WHERE T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SEMESTER = '1' ");
        }
        stb.append("     ORDER BY SCHOOL_KIND, CLASSCD, SUBCLASSCD, CURRICULUM_CD ");
        return stb.toString();
    }
    

    private static String add(final Object count1, final Object count2) {
        if (!NumberUtils.isNumber((String) count1) && !NumberUtils.isNumber((String) count2)) {
            return null;
        }
        final BigDecimal bd1 = NumberUtils.isNumber((String) count1) ? new BigDecimal((String) count1) : BigDecimal.valueOf(0);
        final BigDecimal bd2 = NumberUtils.isNumber((String) count2) ? new BigDecimal((String) count2) : BigDecimal.valueOf(0);
        return bd1.add(bd2).toString();
    }

    private static class Student {
        final String _schregno;
        final String _curriculumYear;
        final String _inoutcd;
        final String _grdYotei;
        Student(final String schregno, final String curriculumYear, final String inoutcd, final String grdYotei) {
            _schregno = schregno;
            _curriculumYear = curriculumYear;
            _inoutcd = inoutcd;
            _grdYotei = grdYotei;
        }
    }
    
    private static class Subclass {
        final String _subclassname;
        final String _schoolKind;
        final String _classCd;
        final String _subclassCd;
        final String _curriculumCd;
        final List _studentList = new ArrayList();
        boolean _islastClass;
        public Subclass(final String subclassname, final String schoolKind, final String classCd, final String subclassCd, final String curriculumCd) {
            _subclassname = subclassname;
            _schoolKind = schoolKind;
            _classCd = classCd;
            _subclassCd = subclassCd;
            _curriculumCd = curriculumCd;
        }
        public String getClassKey() {
            return _schoolKind + _classCd + _curriculumCd;
        }
        public String getKey() {
            return _schoolKind + _classCd + _subclassCd + _curriculumCd;
        }

        public Collection getColInOutCd(final String inoutcd) {
            return getColInOutCd(inoutcd, false);
        }

        /**
         * 指定条件に合う生徒を得る
         * @param inoutcd 内外区分。"8"なら併修生。"9"なら聴講生。
         * @param checkGrdYotei 卒業予定者かをチェックするか。trueなら卒業予定者のみを抽出対象とする。falseなら卒業予定者を参照しない。
         * @return 条件に合う生徒
         */
        public Collection getColInOutCd(final String inoutcd, final boolean checkGrdYotei) {
            final Collection col = new ArrayList();
            for (final Iterator it = _studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (inoutcd.equals(student._inoutcd) && (!checkGrdYotei || checkGrdYotei && null != student._grdYotei)) {
                    col.add(student._schregno);
                }
            }
            return col;
        }

        public Collection getColCurriculumYear(final String curriculumYear) {
            return getColCurriculumYear(curriculumYear, false);
        }

        /**
         * 指定条件に合う生徒を得る
         * @param curriculumYear 指定入学年度。nullなら入学年度を指定しない。
         * @param checkGrdYotei 卒業予定者かをチェックするか。trueなら卒業予定者のみを抽出対象とする。falseなら卒業予定者を参照しない。
         * @return 条件に合う生徒
         */
        public Collection getColCurriculumYear(final String curriculumYear, final boolean checkGrdYotei) {
            final Collection col = new ArrayList();
            for (final Iterator it = _studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null != student._curriculumYear && (null == curriculumYear || curriculumYear.equals(student._curriculumYear)) &&
                        (!checkGrdYotei || checkGrdYotei && null != student._grdYotei) &&
                        (!INOUTCD_LECTURE.equals(student._inoutcd) && !INOUTCD_MULTI.equals(student._inoutcd))) {
                    col.add(student._schregno);
                }
            }
            return col;
        }
        
        public static Subclass getSubclass(final List list, final String key) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                if (subclass.getKey().equals(key)) {
                    return subclass;
                }
            }
            return null;
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
        final String _year; // ログイン年度 もしくは ログイン年度 + 1
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
        }
    }
}

// eof

