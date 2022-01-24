/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 2c98ae28a88390c786c343cc88350f650509ca5d $
 *
 * 作成日: 2020/04/02
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

public class KNJD426N_2 {

    private static final Log log = LogFactory.getLog(KNJD426N_2.class);

    private boolean _hasData;
    private final String HOUTEI = "1";
    private final String JITSU = "2";

    int LINE_CNT = 0;
    final String FRM_1 = "1";
    final String FRM_2 = "2";
    final String FRM_3 = "3";
    final String FRM_4 = "4";
    final String FRM_5 = "5";
    final String FRM_RI = "6";
    final String FRM_SEN = "7";

    final String FRM_RI1 = "1";
    final String FRM_RI2 = "2";

    private Param _param;

    /**
     * @param request
     *            リクエスト
     * @param response
     *            レスポンス
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

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
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();

            if (FRM_RI.equals(_param._formYoshiki1)) {
                //(教科名)(データ)(観点)
                printOut(db2, svf, student, FRM_RI1);
                //(教科名)(データ)
                printOut(db2, svf, student, FRM_RI2);
            } else {
                //(教科名)(データ)
                printOut(db2, svf, student, null);
            }

           _hasData = true;
        }
    }

    //Title
    private void printTitle(final DB2UDB db2, final Vrw32alp svf, final Student student, final String flg) {
        //form
        svf.VrSetForm(_param.getFormId(flg), 4);
        LINE_CNT = 0;

        //学期 タイトル
        svf.VrsOut("TITLE", _param._semesterName + " 個別の指導計画");
        //学校名
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
        //作成者(欄外)
        svf.VrsOut("TR_NAME", student._trName);
        //作成日
        svf.VrsOut("MAKE_DATE", formatDate(db2, student._makeDate));
        //作成者
        svf.VrsOut("MAKER1", student._maker);
        //学部
        svf.VrsOut("DEPARTMENT_NAME", student._gakubuName);
        //年組
        svf.VrsOut("GRADE_NAME", student.getHrName());
        //氏名
        int nameLen = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nameField = nameLen > 40 ? "3" : nameLen > 24 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name);
        //かな
        int kanaLen = KNJ_EditEdit.getMS932ByteLength(student._nameKana);
        final String kanaField = kanaLen > 30 ? "2" : "1";
        svf.VrsOut("KANA" + kanaField, student._nameKana);
        svf.VrEndRecord();
        LINE_CNT++;
    }

    //Record2・・・(項目名)
    //1 様式１　「指導内容・学習目標・支援の手立て・評価(文言)」
    //2 様式２　「指導内容・学習目標・支援の手立て」
    //3 様式３　「指導内容・支援の手立て・評価(文言)」
    //4 様式４　「指導内容・支援の手立て」
    //5 様式５　「指導内容・支援の手立て・観点項目・評価(観点)・評定」
    //6 保険理療科　「RI1:指導内容・支援の手立て・観点項目・評価(観点)・評定」「RI2:指導内容・支援の手立て・評定」
    //7 専攻科　「指導内容・支援の手立て・評定」
    private void printHeader(final Vrw32alp svf, final Student student) {
        svf.VrsOut("ITEM1_1", "教科等");
        svf.VrsOut("ITEM1_2", (String) _param._kindNameMap.get("1"));
        svf.VrsOut("ITEM1_3", (String) _param._kindNameMap.get("2"));
        svf.VrsOut("ITEM1_4", (String) _param._kindNameMap.get("3"));
        svf.VrsOut("ITEM1_5", (String) _param._kindNameMap.get("4"));
        svf.VrsOut("ITEM1_6", "観点項目");
        svf.VrsOut("ITEM1_7", "評価");
        svf.VrsOut("ITEM1_8", "評定");
        svf.VrEndRecord();
        LINE_CNT++;
    }

    //(教科名)(データ)
    private void printOut(final DB2UDB db2, final Vrw32alp svf, final Student student, final String flg) {
        printTitle(db2, svf, student, flg);
        printHeader(svf, student);

        final int firstPageMaxline = 50;

        final int subclassLen = 2;
        final int remarkLen1 = FRM_3.equals(_param._formYoshiki1) || FRM_4.equals(_param._formYoshiki1) || FRM_SEN.equals(_param._formYoshiki1) || FRM_RI2.equals(flg) ? 40 : 20;
        final int remarkLen2 = 20;
        final int remarkLen3 = FRM_2.equals(_param._formYoshiki1) || FRM_4.equals(_param._formYoshiki1) || FRM_SEN.equals(_param._formYoshiki1) || FRM_RI2.equals(flg) ? 40 : 20;
        final int remarkLen4 = 20;

        boolean isPrintRemark2 = FRM_1.equals(_param._formYoshiki1) || FRM_2.equals(_param._formYoshiki1) ? true : false;
        boolean isPrintRemark4 = FRM_1.equals(_param._formYoshiki1) || FRM_3.equals(_param._formYoshiki1) ? true : false;
        boolean isPrintJview = FRM_5.equals(_param._formYoshiki1) || FRM_RI1.equals(flg) ? true : false;

        int grp = 1;
        int grp2 = 1;
        for (Iterator itSub = student._subclassMap.keySet().iterator(); itSub.hasNext();) {
            final String subclassCd = (String) itSub.next();
            final Subclass subclass = (Subclass) student._subclassMap.get(subclassCd);

            if (FRM_SEN.equals(_param._formYoshiki1) && !"72".equals(subclass._classCd)) {
                continue;
            } else if (FRM_RI1.equals(flg) && "71".equals(subclass._classCd)) {
                continue;
            } else if (FRM_RI2.equals(flg) && !"71".equals(subclass._classCd)) {
                continue;
            }

            //科目名
            final List setSubclassList = KNJ_EditKinsoku.getTokenList(subclass._subclassName, subclassLen);
            //登録データ
            final String remark1 = (String) subclass._remarkSeqMap.get("1");
            final String remark2 = (String) subclass._remarkSeqMap.get("2");
            final String remark3 = (String) subclass._remarkSeqMap.get("3");
            final String remark4 = (String) subclass._remarkSeqMap.get("4");
            final List setRemarkList1 = KNJ_EditKinsoku.getTokenList(remarkLen1 == 40 ? getReplaceLF(remark1) : remark1, remarkLen1);
            final List setRemarkList2 = KNJ_EditKinsoku.getTokenList(remark2, remarkLen2);
            final List setRemarkList3 = KNJ_EditKinsoku.getTokenList(remarkLen3 == 40 ? getReplaceLF(remark3) : remark3, remarkLen3);
            final List setRemarkList4 = KNJ_EditKinsoku.getTokenList(remark4, remarkLen4);
            //観点データ
            final List jviewList = new ArrayList();
            for (Iterator itJview = subclass._jviewMap.keySet().iterator(); itJview.hasNext();) {
                final String viewCd = (String) itJview.next();
                final Jview jview = (Jview) subclass._jviewMap.get(viewCd);
                jviewList.add(jview);
            }

            int maxLine = setSubclassList.size();
            if (setRemarkList1.size() > maxLine) {
                maxLine = setRemarkList1.size();
            }
            if (setRemarkList2.size() > maxLine && isPrintRemark2) {
                maxLine = setRemarkList2.size();
            }
            if (setRemarkList3.size() > maxLine) {
                maxLine = setRemarkList3.size();
            }
            if (setRemarkList4.size() > maxLine && isPrintRemark4) {
                maxLine = setRemarkList4.size();
            }
            if (jviewList.size() > maxLine && isPrintJview) {
                maxLine = jviewList.size();
            }

            boolean page2Print = false;
            int ii = 0;
            for (int i = 0; i < maxLine; i++) {
                if (LINE_CNT == firstPageMaxline) {
                    printTitle(db2, svf, student, flg);
                    printHeader(svf, student); //2ページ目の1行目に出力
                    page2Print = true;
                }
                svf.VrsOut("GRP2_1", String.valueOf(grp));
                svf.VrsOut("GRP2_2", String.valueOf(grp));
                svf.VrsOut("GRP2_3", String.valueOf(grp));
                svf.VrsOut("GRP2_4", String.valueOf(grp));
                svf.VrsOut("GRP2_5", String.valueOf(grp));
                svf.VrsOut("GRP2_6", String.valueOf(grp2));
                svf.VrsOut("GRP2_7", String.valueOf(grp2));
                svf.VrsOut("GRP2_8", String.valueOf(grp));
                if (page2Print) {
                    if (ii < setSubclassList.size()) {
                        svf.VrsOut("ITEM2_1", (String) setSubclassList.get(ii)); //2ページ目用
                    }
                    ii++;
                } else {
                    if (i < setSubclassList.size()) {
                        svf.VrsOut("ITEM2_1", (String) setSubclassList.get(i)); //1ページ目用
                    }
                }
                if (i < setRemarkList1.size()) {
                    svf.VrsOut("ITEM2_2", (String) setRemarkList1.get(i));
                }
                if (i < setRemarkList2.size() && isPrintRemark2) {
                    svf.VrsOut("ITEM2_3", (String) setRemarkList2.get(i));
                }
                if (i < setRemarkList3.size()) {
                    svf.VrsOut("ITEM2_4", (String) setRemarkList3.get(i));
                }
                if (i < setRemarkList4.size() && isPrintRemark4) {
                    svf.VrsOut("ITEM2_5", (String) setRemarkList4.get(i));
                }
                if (i < jviewList.size() && isPrintJview) {
                    final Jview jview = (Jview) jviewList.get(i);
                    svf.VrsOut("ITEM2_6", jview._viewName);
                    svf.VrsOut("ITEM2_7", jview._status);
                    grp2++;
                }
                svf.VrsOut("ITEM2_8", subclass._score);
                svf.VrEndRecord();
                LINE_CNT++;
            }
            grp++;
            grp2++;
        }
    }

    private String formatDate(final DB2UDB db2, final String date) {
        if (null == date || "".equals(date)) {
            return date;
        }
        final String[] tateFormat = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, date));
        final String nengo = KNJ_EditDate.gengouAlphabetMarkOfDate(db2, StringUtils.replace(date, "-", "/"));
        return nengo + ("元".equals(tateFormat[1]) ? "1" : tateFormat[1]) + "." + tateFormat[2] + "." + tateFormat[3];
    }

    private String getReplaceLF(final String text) {
        if (null != text) {
            return StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(text, "\u000b", ""), "\r\n", ""), "\r", ""), "\n", "");
        } else {
            return text;
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String studentSql = getStudentSql();
            log.debug(" sql =" + studentSql);
            ps = db2.prepareStatement(studentSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String gakubuName = rs.getString("GAKUBU_NAME");
                final String hrName = rs.getString("HR_NAME");
                final String ghrName = rs.getString("GHR_NAME");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String trName = rs.getString("TR_NAME");
                final String makeDate = rs.getString("MAKE_DATE");
                final String maker = rs.getString("MAKER");

                final Student student = new Student(schregNo, grade, gakubuName, hrName, ghrName, name, nameKana,
                        trName, makeDate, maker);

                student.setSubclassSeqMap(db2);
                student.setSubclassJviewMap(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TOKUSHI AS (  ");
        stb.append("     SELECT ");
        stb.append("         * ");
        stb.append("     FROM ");
        stb.append("         HREPORT_TOKUSHI_SCHREG_SUBCLASS_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._ctrlYear + "' ");
        stb.append("         AND SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("         AND UNITCD = '00' ");
        stb.append("         AND SEQ BETWEEN 1 AND 4 ");
        stb.append(" ), MAX_TOKUSHI AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         MAX(UPDATED) AS UPDATED ");
        stb.append("     FROM ");
        stb.append("         TOKUSHI ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO ");
        stb.append(" ), MAKE_TOKUSHI AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         DATE(T1.UPDATED) AS MAKE_DATE, ");
        stb.append("         S1.STAFFNAME AS MAKER ");
        stb.append("     FROM ");
        stb.append("         TOKUSHI T1 ");
        stb.append("         INNER JOIN MAX_TOKUSHI T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.UPDATED = T1.UPDATED ");
        stb.append("         LEFT JOIN STAFF_MST S1 ON S1.STAFFCD = T1.REGISTERCD ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     COUR.COURSENAME AS GAKUBU_NAME, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     GHRH.GHR_NAME, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     H001.REMARK1 AS TR_NAME, ");
        stb.append("     MAKE.MAKE_DATE, ");
        stb.append("     MAKE.MAKER ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST BASE ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("         AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("         AND REGDH.GRADE = REGD.GRADE ");
        stb.append("         AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN COURSE_MST COUR ON COUR.COURSECD = REGD.COURSECD ");
        stb.append("     LEFT JOIN SCHREG_REGD_GHR_DAT GHR ON GHR.SCHREGNO = REGD.SCHREGNO ");
        stb.append("         AND GHR.YEAR = REGD.YEAR ");
        stb.append("         AND GHR.SEMESTER = REGD.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_GHR_HDAT GHRH ON GHRH.YEAR = GHR.YEAR ");
        stb.append("         AND GHRH.SEMESTER = GHR.SEMESTER ");
        stb.append("         AND GHRH.GHR_CD = GHR.GHR_CD ");
        stb.append("     LEFT JOIN HREPORT_GUIDANCE_SCHREG_DETAIL_DAT H001 ON H001.YEAR = REGD.YEAR ");
        stb.append("         AND H001.SEMESTER = REGD.SEMESTER ");
        stb.append("         AND H001.SCHREGNO = REGD.SCHREGNO ");
        stb.append("         AND H001.SEQ = '001' ");
        stb.append("     LEFT JOIN MAKE_TOKUSHI MAKE ON MAKE.SCHREGNO = REGD.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     BASE.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("     AND REGD.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append(" ORDER BY ");
        stb.append("     GHR.GHR_CD, ");
        stb.append("     GHR.GHR_ATTENDNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        return stb.toString();
    }

    private class Student {
        final String _schregNo;
        final String _grade;
        final String _gakubuName;
        final String _hrName;
        final String _ghrName;
        final String _name;
        final String _nameKana;
        final String _trName;
        final String _makeDate;
        final String _maker;
        final Map _subclassMap;

        public Student(final String schregNo, final String grade, final String gakubuName, final String hrName, final String ghrName, final String name, final String nameKana,
                final String trName, final String makeDate, final String maker) {
            _schregNo = schregNo;
            _grade = grade;
            _gakubuName = gakubuName;
            _hrName = hrName;
            _ghrName = ghrName;
            _name = name;
            _nameKana = nameKana;
            _trName = trName;
            _makeDate = makeDate;
            _maker = maker;
            _subclassMap = new TreeMap();
        }

        private String getHrName() {
            if (HOUTEI.equals(_param._hukusikiRadio)) {
                return _hrName;
            } else {
                return _ghrName;
            }
        }

        private void setSubclassSeqMap(final DB2UDB db2) {
            final String subclassSql = getSubclassSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(subclassSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String seq = rs.getString("SEQ");
                    final String remark = rs.getString("REMARK");
                    final Subclass subclass;
                    final String key = classCd + schoolKind + curriculumCd + subclassCd;
                    if (_subclassMap.containsKey(key)) {
                        subclass = (Subclass) _subclassMap.get(key);
                    } else {
                        subclass = new Subclass(classCd, schoolKind, curriculumCd, subclassCd, subclassName);
                    }
                    subclass.setRemarkSeqMap(seq, remark);
                    _subclassMap.put(key, subclass);
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getSubclassSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     S1.SUBCLASSNAME, ");
            stb.append("     T1.SEQ, ");
            stb.append("     T1.REMARK ");
            stb.append(" FROM ");
            stb.append("     HREPORT_TOKUSHI_SCHREG_SUBCLASS_DAT T1 ");
            stb.append("     LEFT JOIN SUBCLASS_MST S1 ");
            stb.append("           ON S1.CLASSCD = T1.CLASSCD ");
            stb.append("          AND S1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("          AND S1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          AND S1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append("     AND T1.UNITCD = '00' ");
            stb.append("     AND T1.SEQ BETWEEN 1 AND 4 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SEQ ");
            return stb.toString();
        }

        private void setSubclassJviewMap(final DB2UDB db2) {
            final String sql = getJviewSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String viewCd = rs.getString("VIEWCD");
                    final String viewName = rs.getString("VIEWNAME");
                    final String status = rs.getString("STATUS");
                    final String score = rs.getString("SCORE");
                    final Subclass subclass;
                    final String key = classCd + schoolKind + curriculumCd + subclassCd;
                    if (_subclassMap.containsKey(key)) {
                        subclass = (Subclass) _subclassMap.get(key);
                    } else {
                        subclass = new Subclass(classCd, schoolKind, curriculumCd, subclassCd, subclassName);
                    }
                    subclass.setJviewMap(viewCd, viewName, status);
                    subclass.setScore(score);
                    _subclassMap.put(key, subclass);
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getJviewSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     S1.SUBCLASSNAME, ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     J1.VIEWNAME, ");
            stb.append("     T1.STATUS, ");
            stb.append("     R1.SCORE ");
            stb.append(" FROM ");
            stb.append("     JVIEWSTAT_RECORD_DAT T1 ");
            stb.append("     LEFT JOIN SUBCLASS_MST S1 ");
            stb.append("          ON S1.CLASSCD = T1.CLASSCD ");
            stb.append("         AND S1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND S1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND S1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN JVIEWNAME_GRADE_MST J1 ");
            stb.append("          ON J1.GRADE = '" + _grade + "' ");
            stb.append("         AND J1.CLASSCD = T1.CLASSCD ");
            stb.append("         AND J1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND J1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND J1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND J1.VIEWCD = T1.VIEWCD ");
            stb.append("     LEFT JOIN RECORD_SCORE_DAT R1 ");
            stb.append("          ON R1.YEAR = T1.YEAR ");
            stb.append("         AND R1.SEMESTER = T1.SEMESTER ");
            stb.append("         AND R1.TESTKINDCD = '99' ");
            stb.append("         AND R1.TESTITEMCD = '00' ");
            stb.append("         AND R1.SCORE_DIV = '09' ");
            stb.append("         AND R1.CLASSCD = T1.CLASSCD ");
            stb.append("         AND R1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND R1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND R1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND R1.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.VIEWCD ");
            return stb.toString();
        }
    }

    private class Subclass {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final Map _remarkSeqMap;
        final Map _jviewMap;
        String _score;

        public Subclass(final String classCd, final String schoolKind, final String curriculumCd,
                final String subclassCd, final String subclassName) {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _remarkSeqMap = new HashMap();
            _jviewMap = new TreeMap();
            _score = null;
        }

        public void setRemarkSeqMap(final String seq, final String remark) {
            _remarkSeqMap.put(seq, remark);
        }

        public void setJviewMap(final String viewCd, final String viewName, final String status) {
            final Jview jview = new Jview(viewCd, viewName, status);
            _jviewMap.put(viewCd, jview);
        }

        public void setScore(final String score) {
            _score = score;
        }
    }

    private class Jview {
        final String _viewCd;
        final String _viewName;
        final String _status;

        public Jview(final String viewCd, final String viewName, final String status) {
            _viewCd = viewCd;
            _viewName = viewName;
            _status = status;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 73551 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _semester;
        final String _hukusikiRadio;        //クラス種別(1:法定クラス 2:実クラス)
        final String _schoolKind;
        final String _ghrCd;
        final String _outputPtrn;           //帳票パターン(2:準ずる教育)
        final String[] _categorySelected;
        final String _moveDate;
        final String _printDate;
        final String _formYoshiki1;         //様式パターン「1,2,3,4,5:様式1,2,3,4,5」「6:保険理療科」「7:専攻科」
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _schoolName;
        final String _semesterName;
        final Map _kindNameMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _hukusikiRadio = request.getParameter("HUKUSIKI_RADIO");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _ghrCd = request.getParameter("GHR_CD");
            _outputPtrn = request.getParameter("OUTPUT_PTRN");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _moveDate = request.getParameter("MOVE_DATE");
            _printDate = request.getParameter("PRINT_DATE");
            _formYoshiki1 = request.getParameter("FORM_YOSHIKI1");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolName = getSchoolName(db2);
            _semesterName = getSemesterName(db2);
            _kindNameMap = getKindNameMap();
        }

        private String getFormId(final String flg) {
            String rtnId = null;
            if (FRM_1.equals(_formYoshiki1)) {
                rtnId = "KNJD426N_2_1.frm";
            } else if (FRM_2.equals(_formYoshiki1)) {
                rtnId = "KNJD426N_2_2.frm";
            } else if (FRM_3.equals(_formYoshiki1)) {
                rtnId = "KNJD426N_2_3.frm";
            } else if (FRM_4.equals(_formYoshiki1)) {
                rtnId = "KNJD426N_2_4.frm";
            } else if (FRM_5.equals(_formYoshiki1)) {
                rtnId = "KNJD426N_2_5.frm";
            } else if (FRM_RI.equals(_formYoshiki1)) {
                rtnId = FRM_RI2.equals(flg) ? "KNJD426N_2_RI2.frm" : "KNJD426N_2_RI1.frm";
            } else if (FRM_SEN.equals(_formYoshiki1)) {
                rtnId = "KNJD426N_2_SEN1.frm";
            }
            return rtnId;
        }

        private String getSchoolName(final DB2UDB db2) {
            final String sql = " SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _ctrlYear + "' AND SCHOOLCD = '000000000000' AND SCHOOL_KIND = '" + _schoolKind + "' ";
            String retName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.error("school_mst exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retName;
        }

        private String getSemesterName(final DB2UDB db2) {
            final String sql = " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _semester + "' ";
            String retName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retName = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException ex) {
                log.error("semester_mst exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retName;
        }

        private Map getKindNameMap() {
            Map rtnMap = new HashMap();
            //準ずる教育
            rtnMap.put("1", "指導内容");
            rtnMap.put("2", "学習目標");
            rtnMap.put("3", "支援の手立て");
            rtnMap.put("4", "評価");
            return rtnMap;
        }

        private String getKindNameSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     KIND_SEQ, ");
            stb.append("     KIND_REMARK ");
            stb.append(" FROM ");
            stb.append("     HREPORT_GUIDANCE_KIND_NAME_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND KIND_NO = '30' ");
            stb.append(" ORDER BY ");
            stb.append("     KIND_SEQ ");

            return stb.toString();
        }

    }
}

// eof
