/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 0fdb6e3f501f7b55a0b84f05e6d4bb084893234c $
 *
 * 作成日: 2019/03/29
 * 作成者: yamashiro
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
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

public class KNJD426N_3 {

    private static final Log log = LogFactory.getLog(KNJD426N_3.class);

    private boolean _hasData;
    private final String HOUTEI = "1";
    private final String JITSU = "2";

    int LINE_CNT = 0;
    final String FRM_A4 = "1";
    final String FRM_A3 = "2";

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

            if (FRM_A4.equals(_param._formYoshiki2)) {
                svf.VrSetForm("KNJD426N_3_1.frm", 4);
            } else if (FRM_A3.equals(_param._formYoshiki2)) {
                svf.VrSetForm("KNJD426N_3_2.frm", 4);
            }
            LINE_CNT = 0;

            //Title
            //タイトル
            svf.VrsOut("TITLE", "個別の指導計画");
            //学校名
            svf.VrsOut("SCHOOL_NAME", _param._schoolName);

            //Record1
            printRec1(svf, student);

            //Record0・・・支援計画の目標(項目名)、支援計画の目標(データ)、配慮事項(項目名)、配慮事項(データ)
            if (FRM_A3.equals(_param._formYoshiki2)) {
                printRec0(svf, student);
            }

            //Record2・・・BLANK、BLANK、目標設定理由(項目名)
            printRec2(svf, student);

            //Record3・・・重点目標(項目名)、重点目標(データ)、目標設定理由(データ)
            printRec3(svf, student);

            //Record4・・・BLANK、長期目標(項目名)、短期目標(項目名)、手立て　学習場面(項目名)
            printRec4(svf, student);

            //Record5・・・わかる(項目名)、長期目標(データ)、短期目標(データ)、手立て　学習場面(データ)
            //Record5・・・かかわり(項目名)、長期目標(データ)、短期目標(データ)、手立て　学習場面(データ)
            //Record5・・・からだ(項目名)、長期目標(データ)、短期目標(データ)、手立て　学習場面(データ)
            //Record5・・・けんこう(項目名)、長期目標(データ)、短期目標(データ)、手立て　学習場面(データ)
            printRec5(svf, student);

           _hasData = true;
        }
    }

    //Record1
    private void printRec1(final Vrw32alp svf, final Student student) {
        //学期
        svf.VrsOut("SEMESTER_NAME", _param._semesterName);
        //学部
        svf.VrsOut("DEPARTMENT_NAME", student._gakubuName);
        //年組
        svf.VrsOut("GRADE_NAME", student.getHrName());
        //氏名
        if (FRM_A4.equals(_param._formYoshiki2)) {
            int nameLen = KNJ_EditEdit.getMS932ByteLength(student._name);
            final String nameField = nameLen > 40 ? "3" : nameLen > 26 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, student._name);
        } else if (FRM_A3.equals(_param._formYoshiki2)) {
            svf.VrsOut("NAME1", student._name);
        }
        //かな
        if (FRM_A4.equals(_param._formYoshiki2)) {
            int kanaLen = KNJ_EditEdit.getMS932ByteLength(student._nameKana);
            final String kanaField = kanaLen > 30 ? "2" : "1";
            svf.VrsOut("KANA" + kanaField, student._nameKana);
        } else if (FRM_A3.equals(_param._formYoshiki2)) {
            svf.VrsOut("KANA1", student._nameKana);
        }
        svf.VrEndRecord();
        LINE_CNT++;
    }

    //Record0・・・支援計画の目標(項目名)、支援計画の目標(データ)、配慮事項(項目名)、配慮事項(データ)
    private void printRec0(final Vrw32alp svf, final Student student) {
        if (!student.isPrintRec0()) return;

        int grp = 0;

        final List setTitleList1 = KNJ_EditKinsoku.getTokenList((String) _param._kindNameMap.get("001"), 10);
        final List setTitleList2 = KNJ_EditKinsoku.getTokenList((String) _param._kindNameMap.get("002"), 14);
        final List setRemarkList1 = KNJ_EditKinsoku.getTokenList(student._goals, 100);
        final List setRemarkList2 = KNJ_EditKinsoku.getTokenList(student._accommodation, 84);

        int maxLine = setTitleList1.size();
        if (setTitleList2.size() > maxLine) {
            maxLine = setTitleList2.size();
        }
        if (setRemarkList1.size() > maxLine) {
            maxLine = setRemarkList1.size();
        }
        if (setRemarkList2.size() > maxLine) {
            maxLine = setRemarkList2.size();
        }
        for (int i = 0; i < maxLine; i++) {
            svf.VrsOut("GRP0_1", String.valueOf(grp));
            svf.VrsOut("GRP0_2", String.valueOf(grp));
            svf.VrsOut("GRP0_3", String.valueOf(grp));
            svf.VrsOut("GRP0_4", String.valueOf(grp));
            if (i < setTitleList1.size()) {
                svf.VrsOut("ITEM0_1", (String) setTitleList1.get(i));
            }
            if (i < setRemarkList1.size()) {
                svf.VrsOut("ITEM0_2", (String) setRemarkList1.get(i));
            }
            if (i < setTitleList2.size()) {
                svf.VrsOut("ITEM0_3", (String) setTitleList2.get(i));
            }
            if (i < setRemarkList2.size()) {
                svf.VrsOut("ITEM0_4", (String) setRemarkList2.get(i));
            }
            svf.VrEndRecord();
            LINE_CNT++;
        }
    }

    //Record2・・・BLANK、BLANK、目標設定理由(項目名)
    private void printRec2(final Vrw32alp svf, final Student student) {
        if (!student.isPrintRec3()) return;

        svf.VrsOut("ITEM1_1", "");
        svf.VrsOut("ITEM1_2", "");
        svf.VrsOut("ITEM1_3", (String) _param._kindNameMap.get("004"));
        svf.VrEndRecord();
        LINE_CNT++;
    }

    //Record3・・・重点目標(項目名)、重点目標(データ)、目標設定理由(データ)
    private void printRec3(final Vrw32alp svf, final Student student) {
        if (!student.isPrintRec3()) return;

        int grp = 2;

        final int maxLen1 = FRM_A4.equals(_param._formYoshiki2) ? 8 : 10;
        final int maxLen2 = FRM_A4.equals(_param._formYoshiki2) ? 40 : 100;
        final int maxLen3 = FRM_A4.equals(_param._formYoshiki2) ? 40 : 100;

        final List setTitleList = KNJ_EditKinsoku.getTokenList((String) _param._kindNameMap.get("003"), maxLen1);
        final List setRemarkList1 = KNJ_EditKinsoku.getTokenList(student._keyGoals, maxLen2);
        final List setRemarkList2 = KNJ_EditKinsoku.getTokenList(student._goalsReason, maxLen3);

        int maxLine = setTitleList.size();
        if (setRemarkList1.size() > maxLine) {
            maxLine = setRemarkList1.size();
        }
        if (setRemarkList2.size() > maxLine) {
            maxLine = setRemarkList2.size();
        }
        for (int i = 0; i < maxLine; i++) {
            svf.VrsOut("GRP2_1", String.valueOf(grp));
            svf.VrsOut("GRP2_2", String.valueOf(grp));
            svf.VrsOut("GRP2_3", String.valueOf(grp));
            if (i < setTitleList.size()) {
                svf.VrsOut("ITEM2_1", (String) setTitleList.get(i));
            }
            if (i < setRemarkList1.size()) {
                svf.VrsOut("ITEM2_2", (String) setRemarkList1.get(i));
            }
            if (i < setRemarkList2.size()) {
                svf.VrsOut("ITEM2_3", (String) setRemarkList2.get(i));
            }
            svf.VrEndRecord();
            LINE_CNT++;
        }
    }

    //Record4・・・BLANK、長期目標(項目名)、短期目標(項目名)、手立て　学習場面(項目名)、評価(項目名)
    private void printRec4(final Vrw32alp svf, final Student student) {
        if (!student.isPrintRec5("009") && !student.isPrintRec5("010") && !student.isPrintRec5("011") && !student.isPrintRec5("012")) return;

        svf.VrsOut("ITEM3_1", "");
        svf.VrsOut("ITEM3_2", (String) _param._kindNameMap.get("005"));
        svf.VrsOut("ITEM3_3", (String) _param._kindNameMap.get("006"));
        svf.VrsOut("ITEM3_4", (String) _param._kindNameMap.get("007"));
        if (FRM_A3.equals(_param._formYoshiki2)) {
            svf.VrsOut("ITEM3_5", (String) _param._kindNameMap.get("008"));
        }
        svf.VrEndRecord();
        LINE_CNT++;
    }

    //Record5・・・わかる(項目名)、長期目標(データ)、短期目標(データ)、手立て　学習場面(データ)
    //Record5・・・かかわり(項目名)、長期目標(データ)、短期目標(データ)、手立て　学習場面(データ)
    //Record5・・・からだ(項目名)、長期目標(データ)、短期目標(データ)、手立て　学習場面(データ)
    //Record5・・・けんこう(項目名)、長期目標(データ)、短期目標(データ)、手立て　学習場面(データ)
    private void printRec5(final Vrw32alp svf, final Student student) {
        final int firstPageMaxline = FRM_A4.equals(_param._formYoshiki2) ? 57 : 51;

        final List seqList = new ArrayList();
        seqList.add("009");
        seqList.add("010");
        seqList.add("011");
        seqList.add("012");

        final int maxLen1 = FRM_A4.equals(_param._formYoshiki2) ? 8 : 10;
        final int maxLen2 = FRM_A4.equals(_param._formYoshiki2) ? 16 : 32;
        final int maxLen3 = FRM_A4.equals(_param._formYoshiki2) ? 32 : 56;
        final int maxLen4 = FRM_A4.equals(_param._formYoshiki2) ? 32 : 56;
        final int maxLen5 = 56;

        int grp = 4;
        for (Iterator itSeq = seqList.iterator(); itSeq.hasNext();) {
            final String seq = (String) itSeq.next();

            if (!student.isPrintRec5(seq)) continue;

            final List setTitleList = KNJ_EditKinsoku.getTokenList((String) _param._kindNameMap.get(seq), maxLen1);
            final List setRemarkList1 = KNJ_EditKinsoku.getTokenList(student.getLongGoals(seq), maxLen2);
            final List setRemarkList2 = KNJ_EditKinsoku.getTokenList(student.getShortGoals(seq), maxLen3);
            final List setRemarkList3 = KNJ_EditKinsoku.getTokenList(student.getMeans(seq), maxLen4);
            final List setRemarkList4 = KNJ_EditKinsoku.getTokenList(student.getEvaluation(seq), maxLen5);

            int maxLine = setTitleList.size();
            if (setRemarkList1.size() > maxLine) {
                maxLine = setRemarkList1.size();
            }
            if (setRemarkList2.size() > maxLine) {
                maxLine = setRemarkList2.size();
            }
            if (setRemarkList3.size() > maxLine) {
                maxLine = setRemarkList3.size();
            }
            if (FRM_A3.equals(_param._formYoshiki2)) {
                if (setRemarkList4.size() > maxLine) {
                    maxLine = setRemarkList4.size();
                }
            }

            boolean page2Print = false;
            int ii = 0;
            for (int i = 0; i < maxLine; i++) {
                if (LINE_CNT == firstPageMaxline) {
                    svf.VrsOut("TITLE", ""); //タイトル
                    svf.VrsOut("SCHOOL_NAME", ""); //学校名
                    printRec4(svf, student); //2ページ目の1行目に出力
                    page2Print = true;
                }
                svf.VrsOut("GRP4_1", String.valueOf(grp));
                svf.VrsOut("GRP4_2", String.valueOf(grp));
                svf.VrsOut("GRP4_3", String.valueOf(grp));
                svf.VrsOut("GRP4_4", String.valueOf(grp));
                if (FRM_A3.equals(_param._formYoshiki2)) {
                    svf.VrsOut("GRP4_5", String.valueOf(grp));
                }
                if (page2Print) {
                    if (ii < setTitleList.size()) {
                        svf.VrsOut("ITEM4_1", (String) setTitleList.get(ii)); //2ページ目用
                    }
                    ii++;
                } else {
                    if (i < setTitleList.size()) {
                        svf.VrsOut("ITEM4_1", (String) setTitleList.get(i)); //1ページ目用
                    }
                }
                if (i < setRemarkList1.size()) {
                    svf.VrsOut("ITEM4_2", (String) setRemarkList1.get(i));
                }
                if (i < setRemarkList2.size()) {
                    svf.VrsOut("ITEM4_3", (String) setRemarkList2.get(i));
                }
                if (i < setRemarkList3.size()) {
                    svf.VrsOut("ITEM4_4", (String) setRemarkList3.get(i));
                }
                if (FRM_A3.equals(_param._formYoshiki2)) {
                    if (i < setRemarkList4.size()) {
                        svf.VrsOut("ITEM4_5", (String) setRemarkList4.get(i));
                    }
                }
                svf.VrEndRecord();
                LINE_CNT++;
            }
            grp++;
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
                final String gakubuName = rs.getString("GAKUBU_NAME");
                final String hrName = rs.getString("HR_NAME");
                final String ghrName = rs.getString("GHR_NAME");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String goals = rs.getString("GOALS");
                final String accommodation = rs.getString("ACCOMMODATION");
                final String keyGoals = rs.getString("KEY_GOALS");
                final String goalsReason = rs.getString("GOALS_REASON");
                final String longGoals1 = rs.getString("LONG_GOALS1");
                final String shortGoals1 = rs.getString("SHORT_GOALS1");
                final String means1 = rs.getString("MEANS1");
                final String evaluation1 = rs.getString("EVALUATION1");
                final String longGoals2 = rs.getString("LONG_GOALS2");
                final String shortGoals2 = rs.getString("SHORT_GOALS2");
                final String means2 = rs.getString("MEANS2");
                final String evaluation2 = rs.getString("EVALUATION2");
                final String longGoals3 = rs.getString("LONG_GOALS3");
                final String shortGoals3 = rs.getString("SHORT_GOALS3");
                final String means3 = rs.getString("MEANS3");
                final String evaluation3 = rs.getString("EVALUATION3");
                final String longGoals4 = rs.getString("LONG_GOALS4");
                final String shortGoals4 = rs.getString("SHORT_GOALS4");
                final String means4 = rs.getString("MEANS4");
                final String evaluation4 = rs.getString("EVALUATION4");

                final Student student = new Student(schregNo, gakubuName, hrName, ghrName, name, nameKana,
                        goals, accommodation, keyGoals, goalsReason,
                        longGoals1, shortGoals1, means1, evaluation1,
                        longGoals2, shortGoals2, means2, evaluation2,
                        longGoals3, shortGoals3, means3, evaluation3,
                        longGoals4, shortGoals4, means4, evaluation4);

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
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     COUR.COURSENAME AS GAKUBU_NAME, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     GHRH.GHR_NAME, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     HRPT9.GOALS, ");
        stb.append("     HRPT9.ACCOMMODATION, ");
        stb.append("     HRPT9.KEY_GOALS, ");
        stb.append("     HRPT9.GOALS_REASON, ");
        stb.append("     HRPT.LONG_GOALS1, ");
        stb.append("     HRPT.SHORT_GOALS1, ");
        stb.append("     HRPT.MEANS1, ");
        stb.append("     HRPT.EVALUATION1, ");
        stb.append("     HRPT.LONG_GOALS2, ");
        stb.append("     HRPT.SHORT_GOALS2, ");
        stb.append("     HRPT.MEANS2, ");
        stb.append("     HRPT.EVALUATION2, ");
        stb.append("     HRPT.LONG_GOALS3, ");
        stb.append("     HRPT.SHORT_GOALS3, ");
        stb.append("     HRPT.MEANS3, ");
        stb.append("     HRPT.EVALUATION3, ");
        stb.append("     HRPT.LONG_GOALS4, ");
        stb.append("     HRPT.SHORT_GOALS4, ");
        stb.append("     HRPT.MEANS4, ");
        stb.append("     HRPT.EVALUATION4 ");
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
        stb.append("     LEFT JOIN V_HREPORT_SELFRELIANCE_SCHREG_REMARK_DAT HRPT9 ON HRPT9.YEAR = REGD.YEAR ");
        stb.append("         AND HRPT9.SEMESTER = '9' ");
        stb.append("         AND HRPT9.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN V_HREPORT_SELFRELIANCE_SCHREG_REMARK_DAT HRPT ON HRPT.YEAR = REGD.YEAR ");
        stb.append("         AND HRPT.SEMESTER = REGD.SEMESTER ");
        stb.append("         AND HRPT.SCHREGNO = REGD.SCHREGNO ");
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
        final String _gakubuName;
        final String _hrName;
        final String _ghrName;
        final String _name;
        final String _nameKana;
        final String _goals;
        final String _accommodation;
        final String _keyGoals;
        final String _goalsReason;
        final String _longGoals1;
        final String _shortGoals1;
        final String _means1;
        final String _evaluation1;
        final String _longGoals2;
        final String _shortGoals2;
        final String _means2;
        final String _evaluation2;
        final String _longGoals3;
        final String _shortGoals3;
        final String _means3;
        final String _evaluation3;
        final String _longGoals4;
        final String _shortGoals4;
        final String _means4;
        final String _evaluation4;

        public Student(final String schregNo, final String gakubuName, final String hrName, final String ghrName, final String name, final String nameKana,
                final String goals, final String accommodation, final String keyGoals, final String goalsReason,
                final String longGoals1, final String shortGoals1, final String means1, final String evaluation1,
                final String longGoals2, final String shortGoals2, final String means2, final String evaluation2,
                final String longGoals3, final String shortGoals3, final String means3, final String evaluation3,
                final String longGoals4, final String shortGoals4, final String means4, final String evaluation4) {
            _schregNo = schregNo;
            _gakubuName = gakubuName;
            _hrName = hrName;
            _ghrName = ghrName;
            _name = name;
            _nameKana = nameKana;
            _goals = getReplaceLF(goals);
            _accommodation = getReplaceLF(accommodation);
            _keyGoals = getReplaceLF(keyGoals);
            _goalsReason = getReplaceLF(goalsReason);
            _longGoals1 = getReplaceLF(longGoals1);
            _shortGoals1 = getReplaceLF(shortGoals1);
            _means1 = getReplaceLF(means1);
            _evaluation1 = getReplaceLF(evaluation1);
            _longGoals2 = getReplaceLF(longGoals2);
            _shortGoals2 = getReplaceLF(shortGoals2);
            _means2 = getReplaceLF(means2);
            _evaluation2 = getReplaceLF(evaluation2);
            _longGoals3 = getReplaceLF(longGoals3);
            _shortGoals3 = getReplaceLF(shortGoals3);
            _means3 = getReplaceLF(means3);
            _evaluation3 = getReplaceLF(evaluation3);
            _longGoals4 = getReplaceLF(longGoals4);
            _shortGoals4 = getReplaceLF(shortGoals4);
            _means4 = getReplaceLF(means4);
            _evaluation4 = getReplaceLF(evaluation4);
        }

        private String getHrName() {
            if (HOUTEI.equals(_param._hukusikiRadio)) {
                return _hrName;
            } else {
                return _ghrName;
            }
        }

        private String getReplaceLF(final String text) {
            if (FRM_A3.equals(_param._formYoshiki2) && null != text) {
                return StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(text, "\u000b", ""), "\r\n", ""), "\r", ""), "\n", "");
            } else {
                return text;
            }
        }

        private String getLongGoals(final String seq) {
            if ("009".equals(seq)) {
                return _longGoals1;
            } else if ("010".equals(seq)) {
                return _longGoals2;
            } else if ("011".equals(seq)) {
                return _longGoals3;
            } else if ("012".equals(seq)) {
                return _longGoals4;
            } else {
                return null;
            }
        }

        private String getShortGoals(final String seq) {
            if ("009".equals(seq)) {
                return _shortGoals1;
            } else if ("010".equals(seq)) {
                return _shortGoals2;
            } else if ("011".equals(seq)) {
                return _shortGoals3;
            } else if ("012".equals(seq)) {
                return _shortGoals4;
            } else {
                return null;
            }
        }

        private String getMeans(final String seq) {
            if ("009".equals(seq)) {
                return _means1;
            } else if ("010".equals(seq)) {
                return _means2;
            } else if ("011".equals(seq)) {
                return _means3;
            } else if ("012".equals(seq)) {
                return _means4;
            } else {
                return null;
            }
        }

        private String getEvaluation(final String seq) {
            if ("009".equals(seq)) {
                return _evaluation1;
            } else if ("010".equals(seq)) {
                return _evaluation2;
            } else if ("011".equals(seq)) {
                return _evaluation3;
            } else if ("012".equals(seq)) {
                return _evaluation4;
            } else {
                return null;
            }
        }

        private boolean isPrintRec0() {
            return null != _goals || null != _accommodation;
        }

        private boolean isPrintRec3() {
            return null != _keyGoals || null != _goalsReason;
        }

        private boolean isPrintRec5(final String seq) {
            if (FRM_A3.equals(_param._formYoshiki2)) {
                return null != getLongGoals(seq) || null != getShortGoals(seq) || null != getMeans(seq) || null != getEvaluation(seq);
            } else {
                return null != getLongGoals(seq) || null != getShortGoals(seq) || null != getMeans(seq);
            }
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 73712 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _semester;
        final String _hukusikiRadio;        //クラス種別(1:法定クラス 2:実クラス)
        final String _schoolKind;
        final String _ghrCd;
        final String _outputPtrn;           //帳票パターン(3:自立活動中心用)
        final String[] _categorySelected;
        final String _moveDate;
        final String _printDate;
        final String _formYoshiki2;         //帳票パターン「自立活動中心用」の様式「1:A4縦(配布用) 2:A3横(教員用)」
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
            _formYoshiki2 = request.getParameter("FORM_YOSHIKI2");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolName = getSchoolName(db2);
            _semesterName = getSemesterName(db2);
            _kindNameMap = getKindNameMap(db2);
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

        private Map getKindNameMap(final DB2UDB db2) {
            Map rtnMap = new HashMap();
            //初期値
            rtnMap.put("001", "支援計画の目標");
            rtnMap.put("002", "配慮事項");
            rtnMap.put("003", "重点目標");
            rtnMap.put("004", "目標設定理由");
            rtnMap.put("005", "長期目標");
            rtnMap.put("006", "短期目標");
            rtnMap.put("007", "手立て　学習場面");
            rtnMap.put("008", "評価");
            rtnMap.put("009", "わかる");
            rtnMap.put("010", "かかわり");
            rtnMap.put("011", "からだ");
            rtnMap.put("012", "けんこう");

            final String sql = getKindNameSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String kindSeq = rs.getString("KIND_SEQ");
                    final String kindRemark = rs.getString("KIND_REMARK");
                    if (rtnMap.containsKey(kindSeq) && kindRemark != null) {
                        rtnMap.put(kindSeq, kindRemark);
                    }
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
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
