/*
 * $Id: 6e44116bc18f59497c6e2419748ed977d78994ea $
 *
 * 作成日: 2014/10/29
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

public class KNJE451 {

    private static final Log log = LogFactory.getLog(KNJE451.class);

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

        final List formList = new ArrayList();
        formList.add(new KNJE451_0(_param));

        final String studentSql = getStudentSql(_param);
        //log.debug(" studentSql = " + studentSql);
        final List studentList = KNJE451_0.getRowList(db2, studentSql);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Map student = (Map) it.next();

            log.info(" student = " + student.get("SCHREGNO"));

            for (final Iterator fit = formList.iterator(); fit.hasNext();) {
                final KNJE451_0 form = (KNJE451_0) fit.next();
                form.printMain(svf, db2, student);
                _hasData = true;
            }
        }

    }

    private static class KNJE451_0 {

        final String FROM_TO_MARK = "\uFF5E";
        final String checkedBox = "レ";
        final String noCheckedBox = "　";
        final DecimalFormat df2 = new DecimalFormat("00");

        final Param _param;
        KNJE451_0(final Param param) {
            _param = param;
        }


        private String getAssessmentTempMstRemark(final DB2UDB db2, final String dataDiv) {
            final String sql = " SELECT REMARK FROM ASSESSMENT_TEMP_MST WHERE YEAR = '" + _param._year + "' AND GRADE = '00' AND DATA_DIV = '" + dataDiv + "' ";

            return getOne(db2, sql);
        }

        private List getAssessmentQMstList(final DB2UDB db2, final String assessDiv, final String assessCd) {
            final String sql = " SELECT * FROM ASSESSMENT_Q_MST WHERE YEAR = '" + _param._year + "' AND ASSESS_DIV = '" + assessDiv + "' " + (null == assessCd ? "" : " AND ASSESS_CD = '" + assessCd + "' ") + " ORDER BY INT(ASSESS_CD) ";

            return getRowList(db2, sql);
        }

        private Map getAssessmentAnsDat(final DB2UDB db2, final String schregno, final String assessDiv, final String day1) {
            final String sql = " SELECT * FROM ASSESSMENT_ANS_DAT WHERE WRITING_DATE = '" + day1 + "' AND SCHREGNO = '" + schregno + "' AND ASSESS_DIV = '" + assessDiv + "' ";

            return getFirstRow(getRowList(db2, sql));
        }

        private Map hoge(final DB2UDB db2, final String assessDiv, final Map ansDatRow) {
            final StringBuffer q = new StringBuffer();
            final StringBuffer ans = new StringBuffer();
            for (final Iterator it = getAssessmentQMstList(db2, assessDiv, null).iterator(); it.hasNext();) {
                final Map qmst = (Map) it.next();
                final String question = getString("QUESTION", qmst);
                if (!StringUtils.isBlank(question)) {
                    if (q.length() != 0) {
                        q.append("\n");
                        ans.append("\n");
                    }
                    q.append("□").append(question);
                    ans.append(null != getString("QUESTION" + getString("ASSESS_CD", qmst), ansDatRow) ? checkedBox : noCheckedBox);
                }
            }

            final Map rtn = new HashMap();
            rtn.put("1", q.toString());
            rtn.put("2", ans.toString());
            return rtn;
        }

        public void printMain(final Vrw32alp svf, final DB2UDB db2, final Map student) {

            final Form form = new Form(svf);
            form._form1 = "KNJE451_1.frm";
            form._form2 = "KNJE451_2.frm";
            form._recMax1 = 42;
            form.setForm1();

            final String schregno = getString("SCHREGNO", student);
            String day1 = getString("WRITING_DATE1", student);
            String day2 = getString("WRITING_DATE2", student);
            final String printDate = null != _param._writingDate && !"".equals(_param._writingDate) ? _param._writingDate : null != day1 ? day1 : day2;
            day1 = null != _param._writingDate && !"".equals(_param._writingDate) ? _param._writingDate : null != day1 && !"".equals(day1) ? day1 : "9999-12-31";
            day2 = null != _param._writingDate && !"".equals(_param._writingDate) ? _param._writingDate : null != day2 && !"".equals(day2) ? day2 : "9999-12-31";
            final String sakuseiDate = KNJ_EditDate.h_format_JP(printDate);
            final String staffname = getOne(db2, "SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + _param._staffcd + "' ");
            form.VrsOut("DATE", "作成日：" + StringUtils.defaultString(sakuseiDate) + " （作成者：" + StringUtils.defaultString(staffname) + "）"); // 作成日
            form.VrsOut("SCHOOL_NAME", getOne(db2, "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _param._year + "' ")); // 学校名
            final int namelen = Util.getMS932ByteCount(getString("NAME", student));
            final int kanalen = Util.getMS932ByteCount(getString("NAME_KANA", student));
            final int gnamelen = Util.getMS932ByteCount(getString("GUARD_NAME", student));
            final int gkanalen = Util.getMS932ByteCount(getString("GUARD_KANA", student));
            form.VrsOut("NAME" + (namelen > 30 ? "3" : namelen > 20 ? "2" : "1"), getString("NAME", student)); // 氏名
            form.VrsOut("KANA" + (kanalen > 30 ? "2" : "1"), getString("NAME_KANA", student)); // かな
            form.VrsOut("GUARD_NAME" + (gnamelen > 30 ? "3" : gnamelen > 20 ? "2" : "1"), getString("GUARD_NAME", student)); // 保護者氏名
            form.VrsOut("GUARD_KANA" + (gkanalen > 30 ? "2" : "1"), getString("GUARD_KANA", student)); // 保護者かな

            final int maxLine = 4;
            for (int j = 0; j < maxLine; j++) {
                final int line = j + 1;
                form.VrsOutn("HR_NAME", line, getString("REGD_HR_NAME" + String.valueOf(line), student)); // 年組名称
                form.VrsOutn("TEACHER_NAME", line, getString("REGD_STAFFNAME" + String.valueOf(line), student)); // 担任名
            }

            form.VrsOut("BIRTHDAY", StringUtils.defaultString(KNJ_EditDate.h_format_JP(getString("BIRTHDAY", student))) + "（" + getString("AGE", student) + "才）"); // 生年月日

            form.VrsOut("TITLE", "●アセスメント"); // タイトル
            form.VrEndRecord();

            final List dataGroupList1 = new ArrayList();
            {
                // 支援が必要な項目
                final String assessDiv = "01";
                final Map row = getAssessmentAnsDat(db2, schregno, assessDiv, day1);

                final FieldDataGroup dg = newFieldDataGroup(dataGroupList1);
                final FieldData d = dg.newFieldData();

//                d.add("ITEM1_1_1", null); // 項目
//                d.add("ITEM1_1_2", null); // 項目
//                d.add("ITEM1_1_3", null); // 項目
                d.add("ITEM1_2_1", singleton("支援が")); // 項目
                d.add("ITEM1_2_2", singleton("必要な")); // 項目
                d.add("ITEM1_2_3", singleton("項目")); // 項目

                for (int i = 1; i <= 10; i++) {

                    d.add("ITEM1_3_" + String.valueOf(i), singleton(null != getString("QUESTION" + String.valueOf(i), row) ? "レ" : "")); // レ点
                }
                d.add("ITEM1_OTHER", singleton(getString("REMARK2", row))); // その他
                d._fieldDivNameMap.put("DIV1", "ITEM1_1_2");
            }

            {
                // 学習面
                final String assessDiv = "02";
                final Map row = getAssessmentAnsDat(db2, schregno, assessDiv, day1);

                final FieldDataGroup dg = newFieldDataGroup(dataGroupList1);
                dg.addCenter("ITEM2_2", asList(new String[] {"学習面"})); // 区分2


                final FieldData d = dg.newFieldData();
                d.addAll("GRP2_1", "G"); // グループ2
                d.addAll("GRP2_2", assessDiv); // グループ7
                for (int i = 3; i <= 15; i++) {
                    d.addAll("GRP2_" + String.valueOf(i), assessDiv); // グループ3
                }
                final Map m = hoge(db2, assessDiv, row);
                d.add("ITEM2_3_1", Util.getTokenList((String) m.get("1"), 62));
                d.add("ITEM2_3_2", Util.getTokenList((String) m.get("2"), 3));
                d.add("ITEM2_4", Util.getTokenList(getString("REMARK1", row), 50));
                d._fieldDivNameMap.put("DIV1", "ITEM2_1");
            }

            {
                final FieldDataGroup dg = newFieldDataGroup(dataGroupList1);

                final FieldData d = dg.newFieldData();

                //d.add("ITEM3_1_1", null); // 項目
                //d.add("ITEM3_1_2", null); // 項目
                //d.add("ITEM3_1_3", null); // 項目
                final String semestername = getOne(db2, "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _param._year + "' AND SEMESTER = '" + _param._semester + "' ");
                d.add("ITEM3_2", singleton("最近の学期末成績※（" + StringUtils.defaultString(semestername)  + "）")); // 項目

                final StringBuffer recordQuery = new StringBuffer();
                recordQuery.append(" SELECT ");
                recordQuery.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                recordQuery.append("     T2.SUBCLASSNAME, ");
                recordQuery.append("     T1.SCORE ");
                recordQuery.append(" FROM RECORD_RANK_DAT T1 ");
                recordQuery.append(" INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
                recordQuery.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                recordQuery.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                recordQuery.append("     AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
                recordQuery.append(" WHERE T1.YEAR = '" + _param._year + "' ");
                recordQuery.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
                recordQuery.append("   AND T1.TESTKINDCD || T1.TESTITEMCD = '9900' ");
                recordQuery.append("   AND T1.SCHREGNO = '" + schregno + "' ");
                recordQuery.append(" ORDER BY T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");

                final List rowList = getRowList(db2, recordQuery.toString());

                final int subclassLineMax = 13;
                for (int j = 0; j < subclassLineMax; j++) {
                    final int line = j + 1;
                    Map row = null;
                    if (j < rowList.size()) {
                        row = (Map) rowList.get(j);
                        String subclassname = getString("SUBCLASSNAME", row);
                        d.add("SUBJECT" + (Util.getMS932ByteCount(subclassname) > 6 ? "2" : "1"), line, singleton(subclassname)); // 科目
                        d.add("VALUE", line, singleton(getString("SCORE", row))); // 成績
                    }
                    d.add("GRP3", line, singleton("S" + String.valueOf(line))); // 成績
                }
                d._fieldDivNameMap.put("DIV1", "ITEM3_1_2");
            }

            for (int iti = 3; iti <= 4; iti++) {
                final String assessDiv = df2.format(iti);
                final Map row = getAssessmentAnsDat(db2, schregno, assessDiv, day1);
                final FieldDataGroup dg = newFieldDataGroup(dataGroupList1);
                final FieldData d = dg.newFieldData();
                String[] title = null;
                if (iti == 3) {
                    title = new String[] {"生活・", "行動面"};
                } else if (iti == 4) {
                    title = new String[] {"社会性", "・対人", "関係"};
                }

                d.addAll("GRP2_1", assessDiv); // グループ7
                d.addCenter("ITEM2_2", asList(title));
                d.addAll("GRP2_2", assessDiv); // グループ7

                final Map m = hoge(db2, assessDiv, row);
                d.add("ITEM2_3_1", Util.getTokenList((String) m.get("1"), 62));
                d.add("ITEM2_3_2", Util.getTokenList((String) m.get("2"), 3));
                for (int i = 3; i <= 15; i++) {
                    d.addAll("GRP2_" + String.valueOf(i), assessDiv); // グループ3
                }
                d.add("ITEM2_4", Util.getTokenList(getString("REMARK1", row), 50));
                d._fieldDivNameMap.put("DIV1", "ITEM2_1");
            }

            for (int iti = 5; iti <= 9; iti++) {
                if (iti == 7 || iti == 8) {
                    continue;
                }
                final String assessDiv = df2.format(iti);
                final Map row = getAssessmentAnsDat(db2, schregno, assessDiv, day1);

                final FieldDataGroup dg = newFieldDataGroup(dataGroupList1);
                final FieldData d = dg.newFieldData();
                d.addAll("GRP4_1", assessDiv); // グループ7

                String[] title = null;
                String remark = null;
                if (iti == 5) {
                    title = new String[] {"総合", "所見１"};
                    remark = getAssessmentTempMstRemark(db2, "01");
                } else if (iti == 6) {
                    title = new String[] {"総合", "所見２"};
                    remark = getAssessmentTempMstRemark(db2, "02");
                } else if (iti == 7) {
                    title = null; // new String[] {"本人が困っ", "ていること", "・解決した", "いこと"};
                } else if (iti == 8) {
                    title = null; // new String[] {"本人が自分", "で努力でき", "ること・し", "ようとして", "いること"};
                } else if (iti == 9) {
                    title = new String[] {"保護者", "の希望"};
                }
                final List tokenList = new ArrayList();
                if (!StringUtils.isBlank(remark)) {
                    tokenList.addAll(Util.getTokenList(remark, 100));
                }
                tokenList.addAll(Util.getTokenList(getString("REMARK1", row), 100));

                d.addCenter("ITEM4_2_1", asList(title));
                //d.addCenter("ITEM4_2_2", asList(title)); // 10桁;
                d.addAll("GRP4_2", assessDiv); // グループ3
                d.addAll("GRP4_3", assessDiv); // グループ3
                d.add("ITEM4_3", tokenList);
                d._fieldDivNameMap.put("DIV1", "ITEM4_1");
            }

            {
                final String assessDiv = "10";

                final Map row = getAssessmentAnsDat(db2, schregno, assessDiv, day1);

                final FieldDataGroup dg = newFieldDataGroup(dataGroupList1);
                final FieldData d = dg.newFieldData();
                d.addAll("GRP16_1", assessDiv); // グループ3
                d.add("ITEM16_2", singleton("<進路について>"));
                d.add("ITEM16_3_1", singleton("1".equals(getString("QUESTION1", row)) ? checkedBox : noCheckedBox));
                d.add("ITEM16_3_2", singleton("1".equals(getString("QUESTION2", row)) ? checkedBox : noCheckedBox));
                d.add("ITEM16_3_3", singleton("1".equals(getString("QUESTION3", row)) ? checkedBox : noCheckedBox));
                d.add("ITEM16_3_4", singleton("1".equals(getString("QUESTION4", row)) ? checkedBox : noCheckedBox));
                d.add("ITEM16_3_5", singleton("1".equals(getString("QUESTION5", row)) ? checkedBox : noCheckedBox));
                d.add("ITEM16_3_6", singleton("1".equals(getString("QUESTION6", row)) ? checkedBox : noCheckedBox));
                d.add("ITEM16_3_7", singleton("1".equals(getString("QUESTION7", row)) ? checkedBox : noCheckedBox));
                d.add("ITEM16_3_8", singleton("1".equals(getString("QUESTION8", row)) ? checkedBox : noCheckedBox));
                d.add("ITEM16_3_9", singleton("1".equals(getString("QUESTION9", row)) ? checkedBox : noCheckedBox));
                final List remark2TokenList = Util.getTokenList(getString("REMARK2", row), 20);
                if (remark2TokenList.size() > 0) {
                    d.add("ITEM16_OTHER", singleton((String) remark2TokenList.get(0)));
                }
                d._fieldDivNameMap.put("DIV1", "ITEM16_1_1");
            }

            {
                final List page1Line = new ArrayList();
                for (int i = 0; i < dataGroupList1.size(); i++) {
                    final FieldDataGroup dg = (FieldDataGroup) dataGroupList1.get(i);
                    page1Line.addAll(dg.getPrintRecordList());
                }

                Util.setRecordFieldDataList(page1Line, "DIV1", Util.extendStringList(Util.charStringList("本人の状況"), page1Line.size(), true));

                FieldData.svfPrintRecordList(page1Line, form);
            }

            //////////////////////////////////////////////////////////////////////////////////////////////////////////
            if (form._isForm1) {
                form.setForm2();
            }

            form.VrsOut("TITLE", "●諸機関との連携歴等"); // タイトル
            form.VrEndRecord();

            final List dataGroupList2 = new ArrayList();

            {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT T1.*, T2.NAME AS MEDICAL_CENTER_NAME ");
                sql.append(" FROM ASSESSMENT_ANS_INSTITUTES_DAT T1 ");
                sql.append(" LEFT JOIN MEDICAL_CENTER_MST T2 ON T2.CENTERCD = T1.INSTITUTES_CD ");
                sql.append(" WHERE ");
                sql.append("   T1.WRITING_DATE = '" + day2 + "' ");
                sql.append("   AND T1.SCHREGNO = '" + schregno + "' ");
                final Map row = getFirstRow(getRowList(db2, sql.toString()));

                final FieldDataGroup dg = newFieldDataGroup(dataGroupList2);

                dg.addHeader("DIV1", Util.charStringList("医療連携"));

                for (int i = 0; i < 4; i++) {
                    final String si = String.valueOf(i);
                    final FieldData d = dg.newFieldData();
                    if (i == 0) {
                        d.add("GRP4_1", singleton(si)); // グループコード
                        d.add("ITEM4_2_1", singleton("障害名")); // 項目
                        //d.add("ITEM4_2_2", null); // 項目
                        d.add("GRP4_2", singleton(si)); // グループコード
                        d.add("ITEM4_3", Util.getTokenList(getString("HANDICAP", row), 100)); // 項目
                        d._fieldDivNameMap.put("DIV1", "ITEM4_1");
                    } else if (i == 1) {
                        d.add("GRP4_1", singleton(si)); // グループコード
                        //d.add("ITEM4_2_1", null); // 項目
                        d.add("ITEM4_2_2", singleton("診断時期")); // 項目
                        d.add("GRP4_2", singleton(si)); // グループコード
                        d.add("ITEM4_3", Util.getTokenList(formatNentuki(getString("DIAGNOSIS_DATE", row)), 100)); // 項目
                        d._fieldDivNameMap.put("DIV1", "ITEM4_1");

                    } else if (i == 2) {
                        d.addAll("GRP5_1", si);
                        d.addAll("GRP5_2", si);
                        d.addAll("GRP5_3", si);
                        d.addAll("GRP5_4", si);
                        d.add("ITEM5_2", singleton("機関")); // 項目
                        d.add("ITEM5_3", Util.getTokenList(getString("MEDICAL_CENTER_NAME", row), 60)); // 項目
                        d.add("ITEM5_4", singleton("主治医")); // 項目
                        d.add("ITEM5_5", Util.getTokenList(getString("ATTENDING_DOCTOR", row), 40)); // 項目
                        d._fieldDivNameMap.put("DIV1", "ITEM5_1");

                    } else if (i == 3) {
                        d.addAll("GRP5_1", si);
                        d.addAll("GRP5_2", si);
                        d.addAll("GRP5_3", si);
                        d.addAll("GRP5_4", si);
                        d.add("ITEM5_2", singleton("服薬")); // 項目

                        final String flg = getString("MEDICINE_FLG", row);
                        final String fukuyeki = ("1".equals(flg) ? "◎" : "○") + "有 " + ("2".equals(flg) ? "◎" : "○") + "無  薬剤名 （" + StringUtils.defaultString(getString("MEDICINE_NAME", row)) + "）";

                        d.add("ITEM5_3", Util.getTokenList(fukuyeki, 60)); // 項目
                        d.add("ITEM5_4", singleton("備考")); // 項目
                        d.add("ITEM5_5", Util.getTokenList(getString("REMARK", row), 40)); // 項目
                        d._fieldDivNameMap.put("DIV1", "ITEM5_1");
                    }
                }

                FieldDataGroup.svfPrintFieldDataGroup(dg, form);
            }

            {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT T1.*, T2.NAME AS CHECK_CENTER_NAME ");
                sql.append(" FROM ASSESSMENT_ANS_EXAMINATION_DAT T1 ");
                sql.append(" LEFT JOIN CHECK_CENTER_MST T2 ON T2.CENTERCD = T1.INSTITUTES_CD ");
                sql.append(" WHERE ");
                sql.append("   T1.WRITING_DATE = '" + day2 + "' ");
                sql.append("   AND T1.SCHREGNO = '" + schregno + "' ");
                final Map rowMap = getRowMap(db2, sql.toString(), "EXAMINATION_CD");

                final Map row1 = Util.getMappedMap(rowMap, "1");
                final Map row2 = Util.getMappedMap(rowMap, "2");

                final FieldDataGroup dg = newFieldDataGroup(dataGroupList2);

                dg.addHeader("DIV1", Util.charStringList("諸検査の結果"));

                for (int i = 0; i < 7; i++) {
                    final FieldData d = dg.newFieldData();
                    final String si = String.valueOf(i);

                    if (i == 0) {
                        final String roman4 = "\u2163";
                        d.add("ITEM6_2", Util.getTokenList("新版K式（" + formatNentuki(getString("EXAMINATION_DATE", row1)) + "実施）", 50)); // 項目
                        d.add("ITEM6_3", Util.getTokenList("WISC-" + roman4 + "（" + formatNentuki(getString("EXAMINATION_DATE", row2)) + "実施）", 50)); // 項目
                        d._fieldDivNameMap.put("DIV1", "ITEM6_1");

                    } else if (i == 1) {
                        d.addAll("GRP5_1", si);
                        d.addAll("GRP5_2", si);
                        d.addAll("GRP5_3", si);
                        d.addAll("GRP5_4", si);

                        d.add("ITEM5_2", singleton("機関")); // 項目
                        final String checkCenterName1 = getString("CHECK_CENTER_NAME", row1);
                        final int keta1 = Util.getMS932ByteCount(checkCenterName1);
                        if (keta1 <= 40) {
                            d.add("ITEM5_3", singleton(checkCenterName1)); // 項目
                        } else if (keta1 <= 60) {
                            d.add("ITEM5_6", singleton(checkCenterName1)); // 項目
                        } else {
                            d.add("ITEM5_7_1", singleton(checkCenterName1)); // 項目
                        }
                        d.add("ITEM5_4", singleton("機関")); // 項目
                        d.add("ITEM5_5", Util.getTokenList(getString("CHECK_CENTER_NAME", row2), 40)); // 項目
                        d._fieldDivNameMap.put("DIV1", "ITEM5_1");

                    } else if (i == 2) {
                        d.addAll("GRP5_1", si);
                        d.addAll("GRP5_2", si);
                        d.addAll("GRP5_3", si);
                        d.addAll("GRP5_4", si);

                        d.add("ITEM5_2", singleton("検査者")); // 項目
                        final String testerName1 = getString("TESTER_NAME", row1);
                        final int keta1 = Util.getMS932ByteCount(testerName1);
                        if (keta1 <= 40) {
                            d.add("ITEM5_3", singleton(testerName1)); // 項目
                        } else if (keta1 <= 60) {
                            d.add("ITEM5_6", singleton(testerName1)); // 項目
                        } else {
                            d.add("ITEM5_7_1", singleton(testerName1)); // 項目
                        }
                        d.add("ITEM5_4", singleton("検査者")); // 項目
                        d.add("ITEM5_5", Util.getTokenList(getString("TESTER_NAME", row2), 40)); // 項目
                        d._fieldDivNameMap.put("DIV1", "ITEM5_1");

                    } else if (i == 3) {
                        d.add("ITEM7_2", singleton("認知・適用(CA)")); // 項目
                        d.add("ITEM7_3", Util.getTokenList(getString("REMARK1", row1), 34)); // 項目
                        final String title1 = "全検査IQ";
                        d.add("ITEM7_4_" + (Util.getMS932ByteCount(title1) > 10 ? "2" : "1"), singleton(title1)); // 項目
                        d.add("ITEM7_5", Util.getTokenList(getString("REMARK3", row2), 4)); // 項目
                        //d.add("ITEM7_8_1", null); // 項目
                        //d.add("ITEM7_8_2", singleton("")); // 項目
                        //d.add("ITEM7_9", Util.getTokenList("", 4)); // 項目
                        //d.add("ITEM7_10_1", null); // 項目
                        //d.add("ITEM7_10_2", null); // 項目
                        //d.add("ITEM7_11", null); // 項目
                        d._fieldDivNameMap.put("DIV1", "ITEM7_1");

                    } else if (i == 4) {

                        d.add("ITEM7_2", singleton("言語・社会(LS)")); // 項目
                        d.add("ITEM7_3", Util.getTokenList(getString("REMARK2", row1), 34)); // 項目
                        final String title1 = "言語理解";
                        d.add("ITEM7_4_" + (Util.getMS932ByteCount(title1) > 10 ? "2" : "1"), singleton(title1)); // 項目
                        d.add("ITEM7_5", Util.getTokenList(getString("REMARK4", row2), 4)); // 項目
                        final String title2 = "知覚推理";
                        d.add("ITEM7_6_" + (Util.getMS932ByteCount(title2) > 10 ? "2" : "1"), singleton(title2)); // 項目
                        d.add("ITEM7_7", Util.getTokenList(getString("REMARK5", row2), 4)); // 項目
                        //d.add("ITEM7_8_1", null); // 項目
                        d._fieldDivNameMap.put("DIV1", "ITEM7_1");

                    } else if (i == 5) {
                        d.add("ITEM7_2", singleton("全領域(DQ)")); // 項目
                        d.add("ITEM7_3", Util.getTokenList(getString("REMARK3", row1), 34)); // 項目
                        final String title1 = "ワーキングメモリ";
                        d.add("ITEM7_4_" + (Util.getMS932ByteCount(title1) > 10 ? "2" : "1"), singleton(title1)); // 項目
                        d.add("ITEM7_5", Util.getTokenList(getString("REMARK6", row2), 4)); // 項目
                        final String title2 = "処理速度";
                        d.add("ITEM7_6_" + (Util.getMS932ByteCount(title2) > 10 ? "2" : "1"), singleton(title2)); // 項目
                        d.add("ITEM7_7", Util.getTokenList(getString("REMARK7", row2), 4)); // 項目
                        d._fieldDivNameMap.put("DIV1", "ITEM7_1");

                    } else if (i == 6) {
                        d.addAll("GRP9_1", si); // グループコード
                        d.add("ITEM9_2", addLineIfLessThanCount(4, Util.getTokenList("＜解釈・特記事項＞\n" + StringUtils.defaultString(getString("OTHER_TEXT", row1)), 52))); // 項目
                        d.addAll("GRP9_2", si); // グループコード
                        d.addAll("GRP9_3", si); // グループコード
                        d.addAll("GRP9_4", si); // グループコード
                        d.addAll("GRP9_5", si); // グループコード
                        d.addAll("GRP9_6", si); // グループコード
                        d.add("ITEM9_3", addLineIfLessThanCount(4, Util.getTokenList("＜解釈・特記事項＞\n" + StringUtils.defaultString(getString("OTHER_TEXT", row2)), 52))); // 項目
                        d._fieldDivNameMap.put("DIV1", "ITEM9_1");
                    }
                }

                FieldDataGroup.svfPrintFieldDataGroup(dg, form);
            }

            {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT * ");
                sql.append(" FROM ASSESSMENT_ANS_EDUCATION_DAT T1 ");
                sql.append(" WHERE ");
                sql.append("   T1.WRITING_DATE = '" + day2 + "' ");
                sql.append("   AND T1.SCHREGNO = '" + schregno + "' ");
                final Map row = getFirstRow(getRowList(db2, sql.toString()));

                final FieldDataGroup dg = newFieldDataGroup(dataGroupList2);

                dg.addAll("GRP10_1", "KR");
                dg.addHeader("ITEM10_1", Util.charStringList("教育歴"));

                for (int i = 0; i< 3; i++) {
                    final FieldData d = dg.newFieldData();
                    d.addAll("GRP10_2", "KR"); // 項目
                    d.addAll("GRP10_3", "KR"); // 項目
                    String item = "";
                    String item2 = "　　　　　　　 　　　　　 　　　　　　□　　 □　　　　　　 □";
                    if (i == 0) {
                        final String ym1 = formatNentuki(getString("P_S_YM", row));
                        final String ym2 = formatNentuki(getString("P_E_YM", row));
                        final String flg1 = null != getString("P_PASSING_GRADE_FLG", row) ? checkedBox : noCheckedBox;
                        final String flg2 = null != getString("P_SUPPORT_FLG", row) ? checkedBox : noCheckedBox;
                        final String flg3 = null != getString("P_ETC_FLG", row) ? checkedBox : noCheckedBox;
                        final String etc = StringUtils.defaultString(getString("P_ETC", row)) + StringUtils.repeat(" ", 6 * 2 - Util.getMS932ByteCount(getString("P_ETC", row)));
                        final String ym3 = formatNentuki(getString("P_DATE_S_YM", row));
                        final String ym4 = formatNentuki(getString("P_DATE_E_YM", row));
                        item = "小学校：" + ym1 +" " + FROM_TO_MARK + " " + ym2 + "（" + flg1 + "通級 " + flg2 + "特別支援学級 " + flg3 + "その他（" + etc + "） 時期 " + ym3 + " " + FROM_TO_MARK + " " + ym4 + "）";
                    } else if (i == 1) {
                        final String ym1 = formatNentuki(getString("J_S_YM", row));
                        final String ym2 = formatNentuki(getString("J_E_YM", row));
                        final String flg1 = null != getString("J_PASSING_GRADE_FLG", row) ? checkedBox : noCheckedBox;
                        final String flg2 = null != getString("J_SUPPORT_FLG", row) ? checkedBox : noCheckedBox;
                        final String flg3 = null != getString("J_ETC_FLG", row) ? checkedBox : noCheckedBox;
                        final String etc = StringUtils.defaultString(getString("J_ETC", row)) + StringUtils.repeat(" ", 6 * 2 - Util.getMS932ByteCount(getString("J_ETC", row)));
                        final String ym3 = formatNentuki(getString("J_DATE_S_YM", row));
                        final String ym4 = formatNentuki(getString("J_DATE_E_YM", row));
                        item = "中学校：" + ym1 +" " + FROM_TO_MARK + " " + ym2 + "（" + flg1 + "通級 " + flg2 + "特別支援学級 " + flg3 + "その他（" + etc + "） 時期 " + ym3 + " " + FROM_TO_MARK + " " + ym4 + "）";
                    } else if (i == 2) {
                        item = getString("EDUCATION_TEXT", row);
                        item2 = "";
                    }
                    if (i == 0 || i == 1) {
                        d.add("ITEM10_2", Util.getTokenList(item, 120));
                        d.add("ITEM10_3", Util.getTokenList(item2, 120));
                    } else {
                        d.add("ITEM10_4", Util.getTokenList(item, 100));
                    }
                }

                FieldDataGroup.svfPrintFieldDataGroup(dg, form);
            }

            {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT T1.*, T2.NAME AS WELFARE_ADVICE_CENTER_NAME ");
                sql.append(" FROM ASSESSMENT_ANS_CONSULT_DAT T1 ");
                sql.append(" LEFT JOIN WELFARE_ADVICE_CENTER_MST T2 ON T2.CENTERCD = T1.INSTITUTES_CD ");
                sql.append(" WHERE ");
                sql.append("   T1.WRITING_DATE = '" + day2 + "' ");
                sql.append("   AND T1.SCHREGNO = '" + schregno + "' ");
                sql.append(" ORDER BY CONSULT_CD ");
                final List rowList = getRowList(db2, sql.toString());
                for (int i = rowList.size(); i < 3; i++) {
                    rowList.add(createRow());
                }

                final FieldDataGroup dg = newFieldDataGroup(dataGroupList2);

                dg.addHeader("ITEM11_1", Util.charStringList("相談歴"));
                dg.addAll("GRP11_1", "SR");

                int j = 0;
                for (final Iterator it = rowList.iterator(); it.hasNext();) {
                    final Map row = (Map) it.next();
                    j += 1;
                    for (int i = 0; i < 2; i++) {
                        final FieldData d = dg.newFieldData();
                        final String si = String.valueOf(i);
                        if (i == 0) {
                            d.addAll("GRP11_2", si); // グループコード
                            d.add("ITEM11_2", singleton("機関")); // 項目
                            d.addAll("GRP11_3", si); // グループコード
                            final String welfareCenterName = getString("WELFARE_ADVICE_CENTER_NAME", row);
                            final int keta = Util.getMS932ByteCount(welfareCenterName);
                            if (keta <= 40) {
                                d.add("ITEM11_3", singleton(welfareCenterName)); // 項目
                            } else if (keta <= 60) {
                                d.add("ITEM11_5", singleton(welfareCenterName)); // 項目
                            } else {
                                d.add("ITEM11_6_1", singleton(welfareCenterName)); // 項目
                            }
                            d.addAll("GRP11_4", "S" + String.valueOf(j)); // グループコード
                            d.add("ITEM11_4", singleton("＜相談内容＞")); // 項目

                        } else if (i == 1) {
                            d.addAll("GRP11_2", si); // グループコード
                            d.add("ITEM11_2", singleton("時期")); // 項目
                            d.addAll("GRP11_3", si); // グループコード
                            d.add("ITEM11_3", Util.getTokenList(formatNentuki(getString("CONSULT_DATE", row)), 40)); // 項目
                            d.addAll("GRP11_4", "S" + String.valueOf(j)); // グループコード
                            d.add("ITEM11_4", Util.getTokenList(getString("CONSULT_TEXT", row), 50)); // 項目
                        }
                    }
                }

                FieldDataGroup.svfPrintFieldDataGroup(dg, form);
            }
        }

        protected static String getString(final String field, final Map m) {
            if (StringUtils.isBlank(field)) {
                throw new IllegalArgumentException("フィールドがブランク:" + field);
            }
            if (m.isEmpty()) {
                return null;
            }
            if (!m.containsKey(field)) {
                throw new IllegalStateException("フィールドなし:" + field + ", " + m);
            }
            return (String) m.get(field);
        }

        protected static Map createRow() {
            return new HashMap();
        }

        protected static Map getFirstRow(final List list) {
            if (list.size() == 0) {
                return createRow();
            }
            return (Map) list.get(0);
        }

        protected static List withDummy(final List list) {
            if (list.isEmpty()) {
                list.add(createRow());
            }
            return list;
        }

        protected static Map getRowMap(final DB2UDB db2, final String sql, final String keyField) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                final Integer[] idxs = new Integer[meta.getColumnCount() + 1];
                for (int col = 1; col <= meta.getColumnCount(); col++) {
                    idxs[col] = new Integer(col);
                }
                while (rs.next()) {
                    final Map m = createRow();
                    for (int col = 1; col <= meta.getColumnCount(); col++) {
                        final String val = rs.getString(col);
                        m.put(meta.getColumnLabel(col), val);
                        m.put(idxs[col], val);
                    }
                    rtn.put(rs.getString(keyField), m);
                }
            } catch (SQLException e) {
                log.error("exception! sql = " + sql, e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        protected static List getRowList(final DB2UDB db2, final String sql) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                final Integer[] idxs = new Integer[meta.getColumnCount() + 1];
                for (int col = 1; col <= meta.getColumnCount(); col++) {
                    idxs[col] = new Integer(col);
                }
                while (rs.next()) {
                    final Map m = createRow();
                    for (int col = 1; col <= meta.getColumnCount(); col++) {
                        final String val = rs.getString(col);
                        m.put(meta.getColumnLabel(col), val);
                        m.put(idxs[col], val);
                    }
                    rtn.add(m);
                }
            } catch (SQLException e) {
                log.error("exception! sql = " + sql, e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        protected static Map createCondMap(final String[] keyval) {
            if (keyval.length % 2 != 0) {
                throw new IllegalArgumentException("引数の個数が奇数:" + ArrayUtils.toString(keyval));
            }
            final Map m = new HashMap();
            for (int i = 0; i < keyval.length; i += 2) {
                m.put(keyval[i], keyval[i + 1]);
            }
            return m;
        }

        protected static List filterList(final List rowList, final Map cond) {
            final List rtn = new ArrayList();
            for (final Iterator it = rowList.iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                boolean notMatched = false;
                for (final Iterator ci = cond.entrySet().iterator(); ci.hasNext();) {
                    final Map.Entry e = (Map.Entry) ci.next();
                    if (e.getValue().equals(getString((String) e.getKey(), row))) {
                    } else {
                        notMatched = true;
                    }
                }
                if (!notMatched) {
                    rtn.add(row);
                }
            }
            return rtn;
        }

        protected static String getOne(final DB2UDB db2, final String sql) {
            final Map row = getFirstRow(getRowList(db2, sql));
            if (row.isEmpty()) {
                return null;
            }
            return (String) row.get(new Integer(1));
        }

        protected static String formatDate(final String date) {
            if (null == date) {
                return null;
            }
            final Map nengoMap = new HashMap();
            nengoMap.put("平成", "H");
            nengoMap.put("昭和", "S");
            nengoMap.put("大正", "T");
            nengoMap.put("明治", "M");
            final String[] tateFormat = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(date));
            final String nengo = StringUtils.defaultString((String) nengoMap.get(tateFormat[0]), " ");
            return nengo + tateFormat[1] + "." + tateFormat[2] + "." + tateFormat[3];
        }

        protected static String formatNentuki(final String yearMonth) {
            if (null == yearMonth) {
                return "　　　年　月";
            }
            final String[] tateFormat = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(yearMonth + "-01"));
            return tateFormat[0] + (Util.getMS932ByteCount(tateFormat[1]) == 1 ? " " : "") + tateFormat[1] + "年" + (Util.getMS932ByteCount(tateFormat[2]) == 1 ? " " : "") + tateFormat[2] + "月";
        }

        protected static LinkedList singleton(final String s) {
            final LinkedList l = new LinkedList();
            l.add(s);
            return l;
        }

        protected static LinkedList asList(final String[] array) {
            final LinkedList l = new LinkedList();
            for (int i = 0; i < array.length; i++) {
                l.add(array[i]);
            }
            return l;
        }

        protected static List seq(final int startInclusive, final int endExcludive) {
            final List l = new ArrayList();
            for (int i = startInclusive; i < endExcludive; i++) {
                l.add(String.valueOf(i));
            }
            return l;
        }

        protected static List groupByCount(final List list, final int max) {
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

        protected static String yearMonthYear(final String yearMonth) {
            return StringUtils.defaultString(null == yearMonth ? null : String.valueOf(Integer.parseInt(yearMonth.substring(0, 4))), "　　");
        }

        protected static String yearMonthMonth(final String yearMonth) {
            return StringUtils.defaultString(null == yearMonth ? null : String.valueOf(Integer.parseInt(StringUtils.split(yearMonth, "-")[1])), "　　");
        }

        protected List addLineIfLessThanCount(final int count, final List tokenList) {
            final List list = new ArrayList();
            if (null != tokenList) {
                list.addAll(tokenList);
            }
            for (int i = 0; i < count - tokenList.size(); i++) {
                list.add("");
            }
            return list;
        }

        private static FieldDataGroup newFieldDataGroup(final List dataGroupList) {
            final FieldDataGroup fdg = new FieldDataGroup();
            dataGroupList.add(fdg);
            return fdg;
        }
    }

    private static class Util {
        private static List extendStringList(final List list, final int size, final boolean centering) {
            final LinkedList rtn = new LinkedList();
            final int msize = Math.max(list.size(), size);
            if (centering) {
                final int blankCount = (msize - list.size()) / 2;
                for (int i = 0; i < blankCount; i++) {
                    rtn.add(null);
                }
            }
            rtn.addAll(list);
            for (int i = rtn.size(); i < msize; i++) {
                rtn.add(null);
            }
            return rtn;
        }

        private static void setRecordFieldDataAll(final List printRecordList, final String fieldDivName, final String data) {
            setRecordFieldDataList(printRecordList, fieldDivName, repeat(data, printRecordList.size()));
        }

        private static void setRecordFieldDataList(final List printRecordList, final String fieldDivName, final List dataList) {
            for (int j = 0, max = printRecordList.size(); j < max; j++) {
                final Map record = (Map) printRecordList.get(j);
                final Map fieldDivNameMap = getMappedMap(record, FieldData.FIELD_DIV_NAME);
                record.put(StringUtils.defaultString((String) fieldDivNameMap.get(fieldDivName), fieldDivName), dataList.get(j));
            }
        }

        private static List charStringList(final String s) {
            final LinkedList rtn = new LinkedList();
            if (s == null) {
                return rtn;
            }
            for (int i = 0; i < s.length(); i++) {
                rtn.add(String.valueOf(s.charAt(i)));
            }
            return rtn;
        }

        private static Map getMappedMap(final Map map, final String key) {
            if (null == map.get(key)) {
                map.put(key, new TreeMap());
            }
            return (Map) map.get(key);
        }

        private static List getMappedList(final Map map, final String key) {
            if (null == map.get(key)) {
                map.put(key, new ArrayList());
            }
            return (List) map.get(key);
        }

        private static List repeat(final String data, final int count) {
            final List list = new ArrayList();
            for (int i = 0; i < count; i++) {
                list.add(data);
            }
            return list;
        }

        protected static int getMS932ByteCount(final String str) {
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

        /**
         * @param source 元文字列
         * @param bytePerLine 1行あたりのバイト数
         * @return bytePerLineのバイト数ごとの文字列リスト
         */
        protected static List getTokenList(final String source0, final int bytePerLine) {

            if (source0 == null || source0.length() == 0) {
                return Collections.EMPTY_LIST;
            }
            return KNJ_EditKinsoku.getTokenList(source0, bytePerLine);
        }

        protected static void svfVrListOut(
                final Form form,
                final String field,
                final List tokenList
        ) {
            svfVrListOutWithStart(form, field, 1, tokenList);
        }

        protected static void svfVrListOutWithStart(
                final Form form,
                final String field,
                final int start,
                final List tokenList
        ) {
            for (int j = 0; j < tokenList.size(); j++) {
                if (null == tokenList.get(j)) {
                    continue;
                }
                form.VrsOut(field + String.valueOf(start + j), (String) tokenList.get(j));
            }
        }

        protected static void svfVrListOutn(
                final Form form,
                final String field,
                final List tokenList
        ) {
            for (int j = 0; j < tokenList.size(); j++) {
                if (null == tokenList.get(j)) {
                    continue;
                }
                form.VrsOutn(field, j + 1, (String) tokenList.get(j));
            }
        }

        protected static void svfVrListOutn(
                final Form form,
                final String field,
                final int n,
                final List tokenList
        ) {
            for (int j = 0; j < tokenList.size(); j++) {
                if (null == tokenList.get(j)) {
                    continue;
                }
                form.VrsOutn(field + String.valueOf(j + 1), n, (String) tokenList.get(j));
            }
        }

        protected static void svfVrsOutWithSize(
                final Form form,
                final String fieldHead,
                final String[] field,
                final int[] byteSize,
                final String data
        ) {
            final int bsize = getMS932ByteCount(data);
            boolean out = false;
            for (int i = 0; i < field.length - 1; i++) {
                if (bsize <= byteSize[i]) {
                    form.VrsOut(fieldHead + field[i], data);
                    out = true;
                    break;
                }

            }
            if (!out) {
                form.VrsOut(fieldHead + field[field.length - 1], data);
            }
        }

        protected static void svfVrsOutnWithSize(
                final Form form,
                final String fieldHead,
                final String[] field,
                final int[] byteSize,
                final int n,
                final String data
        ) {
            final int bsize = getMS932ByteCount(data);
            boolean out = false;
            for (int i = 0; i < field.length - 1; i++) {
                if (bsize <= byteSize[i]) {
                    form.VrsOutn(fieldHead + field[i], n, data);
                    out = true;
                    break;
                }

            }
            if (!out) {
                form.VrsOutn(fieldHead + field[field.length - 1], n, data);
            }
        }
    }

    private static class Form {
        final Vrw32alp _svf;
        String _form1;
        String _form2;
        int _recMax2 = Integer.MAX_VALUE;
        int _recMax1 = Integer.MAX_VALUE;
        int _recMax;
        int recLine;
        boolean _isForm1;

        private void VrsOut(final String field, final String data) {
            _svf.VrsOut(field, data);
        }

        private void VrsOutn(final String field, final int gyo, final String data) {
            _svf.VrsOutn(field, gyo, data);
        }

        private Form(final Vrw32alp svf) {
            _svf = svf;
        }

        private void setForm1() {
            _svf.VrSetForm(_form1, 4);
            _recMax = _recMax1;
            recLine = 0;
            _isForm1 = true;
        }

        private void setForm2() {
            _svf.VrSetForm(_form2, 4);
            _recMax = _recMax2;
            recLine = 0;
            _isForm1 = false;
        }

        private void VrEndRecord() {
            _svf.VrEndRecord();
            recLine += 1;
            if (recLine >= _recMax && null != _form2) {
                setForm2();
            }
        }
    }

    private static class FieldData {
        static final String FIELD_DIV_NAME = "FIELD_NAME";
        final Map _fieldDivNameMap = new HashMap();
        final List _recordDataList = new LinkedList();
        final Map _addAllMap = new HashMap();
        final Map _addCenterMap = new HashMap();

        private void create(final int min) {
            for (int i = _recordDataList.size(); i < min; i++) {
                _recordDataList.add(newRecord());
            }
        }

        public static void changeField(final Map record, final Map changeFieldMap) {
            final Map newContents = newRecord0();
            for (final Iterator it = record.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                if (FIELD_DIV_NAME.equals(e.getKey())) {
                    final Map fieldDivNameMap = (Map) e.getValue();
                    final Map newFieldDivNameMap = new HashMap();
                    for (final Iterator fdIt = fieldDivNameMap.entrySet().iterator(); fdIt.hasNext();) {
                        final Map.Entry divEntry = (Map.Entry) fdIt.next();
                        newFieldDivNameMap.put(divEntry.getKey(), changeFieldMap.get(divEntry.getValue()));
                    }
                    newContents.put(FIELD_DIV_NAME, newFieldDivNameMap);
                    continue;
                }
                if (null == changeFieldMap.get(e.getKey())) {
                    try {
                        throw new IllegalStateException("変換先無し! key = " + e.getKey());
                    } catch (Exception ex) {
                        log.warn("変換先無し! src = " + record + ", change = " + changeFieldMap, ex);
                    }
                    newContents.put(e.getKey(), e.getValue());
                } else {
                    newContents.put(changeFieldMap.get(e.getKey()), e.getValue());
                }
            }
            record.clear();
            record.putAll(newContents);
        }

        public void add(final String fieldname, final int gyo, final List dataLines) { // 文言等、dataLinesの各行のfieldnameにdataをセット
            add(fieldname + "," + String.valueOf(gyo), dataLines);
        }

        public void add(final String fieldname, final List dataLines) { // 文言等、dataLinesの各行のfieldnameにdataをセット
            create(dataLines.size());
            for (int i = 0; i < dataLines.size(); i++) {
                final Map record = (Map) _recordDataList.get(i);
                record.put(fieldname, dataLines.get(i));
            }
        }

        public List getPrintRecordList() {
            create(1);
            for (final Iterator cit = _addCenterMap.keySet().iterator(); cit.hasNext();) {
                final String fieldname = (String) cit.next();
                final List dataLines = (List) _addCenterMap.get(fieldname);
                add(fieldname, center(dataLines, _recordDataList.size()));
            }
            for (final Iterator cit = _addAllMap.keySet().iterator(); cit.hasNext();) {
                final String fieldname = (String) cit.next();
                final String data = (String) _addAllMap.get(fieldname);
                add(fieldname, repeat(data, _recordDataList.size()));
            }
            for (final Iterator rit = _recordDataList.iterator(); rit.hasNext();) {
                final Map record = (Map) rit.next();
                ((Map) record.get(FIELD_DIV_NAME)).putAll(_fieldDivNameMap);
            }
            //log.debug(" recordDataList = " + _recordDataList);
            return _recordDataList;
        }

        private Map getRecord(final List recordList, final int i) {
            return (Map) recordList.get(i);
        }

        public static void svfPrintRecordList(final List recordList, final Form form) {
//            form._svf.debug = true;
            for (final Iterator it = recordList.iterator(); it.hasNext();) {
                final Map record = (Map) it.next();
                svfPrintRecord(record, form);
                form.VrEndRecord();
            }
//            form._svf.debug = false;
        }

        private static void svfPrintRecord(final Map record, final Form form) {
            for (final Iterator fit = record.keySet().iterator(); fit.hasNext();) {
                final String field = (String) fit.next();
                final Object data = record.get(field);
                if (data instanceof String) {
                    if (null != field && field.indexOf(",") != -1) {
                        final String[] split = StringUtils.split(field, ",");
                        form.VrsOutn(split[0], Integer.parseInt(split[1]), data.toString());

                    } else {
                        form.VrsOut(field, data.toString());
                    }
                }
            }
        }

        public void addAll(final String fieldname, final String data) { // DIV等、全ての行のfieldnameにdataをセット
            _addAllMap.put(fieldname, data);
        }
        public void addCenter(final String fieldname, final List dataLines) { // タイトル等、全ての行の中央にdataLinesをセット
            _addCenterMap.put(fieldname, dataLines);
        }

        public String toString() {
            return "FieldData(" + _recordDataList + ", all = " + _addAllMap + ", center = " + _addCenterMap + ")";
        }

        private static final Map newRecord0() {
            return new TreeMap();
        }
        protected final Map newRecord() {
            final Map r = newRecord0();
            r.put(FIELD_DIV_NAME, _fieldDivNameMap);
            return r;
        }

        static List center(final List s, final int size) { // lをsize行の中央行にセット
            final LinkedList l = new LinkedList(s);
            for (int i = 0, max = (size - l.size()) / 2; i < max; i++) {
                l.addFirst("");
            }
            for (int i = 0; i < size - l.size(); i++) {
                l.addLast("");
            }
            return l;
        }

        static List repeat(final String s, final int size) {
            final List l = new ArrayList();
            for (int i = 0; i < size; i++) {
                l.add(s);
            }
            return l;
        }
    }

    private static class FieldDataGroup extends FieldData {
        final List _fieldDataList = new ArrayList();
        final Map _centringHeaders = new HashMap();

        FieldDataGroup() {
            super();
        }

        public FieldData newFieldData() {
            final FieldData fieldData = new FieldData();
            _fieldDataList.add(fieldData);
            return fieldData;
        }

        public FieldDataGroup newFieldDataGroup() {
            final FieldDataGroup fieldData = new FieldDataGroup();
            _fieldDataList.add(fieldData);
            return fieldData;
        }

        private void mergeRecordDataList() {
            final List allChidrenDataList = new ArrayList();
            for (final Iterator fit = _fieldDataList.iterator(); fit.hasNext();) {
                final FieldData fd = (FieldData) fit.next();
                fd._fieldDivNameMap.putAll(_fieldDivNameMap);
                allChidrenDataList.addAll(fd.getPrintRecordList());
            }
            for (int i = 0, max = allChidrenDataList.size() - _recordDataList.size(); i < max; i++) {
                _recordDataList.add(newRecord());
            }
            for (int i = 0; i < allChidrenDataList.size(); i++) {
                ((Map) _recordDataList.get(i)).putAll((Map) allChidrenDataList.get(i));
            }
        }

        public List getPrintRecordList() {
            mergeRecordDataList();
            for (final Iterator cit = _addCenterMap.keySet().iterator(); cit.hasNext();) {
                final String fieldname = (String) cit.next();
                final List dataLines = (List) _addCenterMap.get(fieldname);
                add(fieldname, center(dataLines, _recordDataList.size()));
            }
            for (final Iterator cit = _addAllMap.keySet().iterator(); cit.hasNext();) {
                final String fieldname = (String) cit.next();
                final String data = (String) _addAllMap.get(fieldname);
                for (final Iterator fit = _recordDataList.iterator(); fit.hasNext();) {
                    final Map record = (Map) fit.next();
                    record.put(fieldname, data);
                }
            }
//            log.debug(" # grouped record dataList size = " + _recordDataList.size());
//            for (int i = 0; i < _recordDataList.size(); i++) {
//                log.debug("  # grouped record i = " + i + ", " + _recordDataList.get(i));
//            }

            return _recordDataList;
        }

        public void addHeader(final String field, final List headerTitle) {
            _centringHeaders.put(field, headerTitle);
        }

        public static void svfPrintFieldDataGroup(final FieldDataGroup fdg, final Form form) {
            final List recordList = fdg.getPrintRecordList();
            for (final Iterator it = fdg._centringHeaders.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final String field = (String) e.getKey();
                final List headerTitle = (List) e.getValue();
                final boolean centering = true;
                Util.setRecordFieldDataList(recordList, field, Util.extendStringList(headerTitle, recordList.size(), centering));
            }
            for (final Iterator it = recordList.iterator(); it.hasNext();) {
                final Map record = (Map) it.next();
                FieldData.svfPrintRecord(record, form);
                form.VrEndRecord();
            }
      }
    }

    private static String getStudentSql(final Param param) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH SCHREGNOS(SCHREGNO) AS ( ");
        String unionAll = "";
        for (int i = 0; i < param._schregSelected.length; i++) {
            stb.append(unionAll).append(" VALUES('" + param._schregSelected[i] + "') ");
            unionAll = " UNION ALL ";
        }
        stb.append(" ), WRITING_DAY1 AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     MAX(WRITING_DATE) AS WRITING_DATE1 ");
        stb.append(" FROM ");
        stb.append("     ASSESSMENT_ANS_DAT ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");
        stb.append(" ), WRITING_DAY2 AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     MAX(WRITING_DATE) AS WRITING_DATE2 ");
        stb.append(" FROM ");
        stb.append("     ASSESSMENT_ANS_INSTITUTES_DAT ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");
        stb.append(" ), ADDRESS AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ZIPCD, ");
        stb.append("     T1.ADDR1, ");
        stb.append("     T1.ADDR2 ");
        stb.append(" FROM  SCHREG_ADDRESS_DAT T1 ");
        stb.append(" INNER JOIN (SELECT T1.SCHREGNO, MAX(T1.ISSUEDATE) AS ISSUEDATE ");
        stb.append("             FROM SCHREG_ADDRESS_DAT T1 ");
        stb.append("             WHERE FISCALYEAR(T1.ISSUEDATE) <= '" + param._year + "' ");
        stb.append("             GROUP BY T1.SCHREGNO ");
        stb.append("            ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ISSUEDATE = T1.ISSUEDATE ");
        stb.append(" WHERE T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREGNOS) ");
        stb.append(" ), GUARD_ADDRESS AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GUARD_ZIPCD, ");
        stb.append("     T1.GUARD_ADDR1, ");
        stb.append("     T1.GUARD_ADDR2, ");
        stb.append("     T1.GUARD_TELNO, ");
        stb.append("     T1.GUARD_ADDR_FLG ");
        stb.append(" FROM  GUARDIAN_ADDRESS_DAT T1 ");
        stb.append(" INNER JOIN (SELECT T1.SCHREGNO, MAX(T1.ISSUEDATE) AS ISSUEDATE ");
        stb.append("             FROM GUARDIAN_ADDRESS_DAT T1 ");
        stb.append("             WHERE FISCALYEAR(T1.ISSUEDATE) <= '" + param._year + "' ");
        stb.append("             GROUP BY T1.SCHREGNO ");
        stb.append("            ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ISSUEDATE = T1.ISSUEDATE ");
        stb.append(" WHERE T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREGNOS) ");
        stb.append(" ), REGD_INFO AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T3.HR_NAME, ");
        stb.append("     ST1.STAFFNAME, ");
        stb.append("     ROW_NUMBER() OVER(PARTITION BY T1.SCHREGNO ORDER BY T1.YEAR) AS ORDER  ");
        stb.append(" FROM  SCHREG_REGD_DAT T1 ");
        stb.append(" INNER JOIN (SELECT T1.SCHREGNO, T1.YEAR, MAX(T1.SEMESTER) AS SEMESTER ");
        stb.append("             FROM SCHREG_REGD_DAT T1 ");
        stb.append("             WHERE (T1.YEAR < '" + param._year + "' OR T1.YEAR = '" + param._year + "' AND T1.SEMESTER <= '" + param._semester + "') ");
        stb.append("             GROUP BY T1.SCHREGNO, T1.YEAR ");
        stb.append("            ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
        stb.append(" INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("     AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T3.GRADE = T1.GRADE ");
        stb.append("     AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append(" INNER JOIN STAFF_MST ST1 ON ST1.STAFFCD = T3.TR_CD1  ");
        stb.append(" WHERE T1.YEAR <= '" + param._year + "' ");
        stb.append("       AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREGNOS) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T5.HR_NAME, ");
        stb.append("     STFHR.STAFFNAME AS HR_STAFFNAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     GD.GRADE_CD, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.NAME_KANA, ");
        stb.append("     Z002.NAME2 AS SEX_NAME, ");
        stb.append("     T2.BIRTHDAY, ");
        stb.append("     AD1.ZIPCD, ");
        stb.append("     AD1.ADDR1, ");
        stb.append("     AD1.ADDR2, ");
        stb.append("     GUARD.GUARD_NAME, ");
        stb.append("     GUARD.GUARD_KANA, ");
        stb.append("     GAD1.GUARD_ZIPCD, ");
        stb.append("     GAD1.GUARD_ADDR1, ");
        stb.append("     GAD1.GUARD_ADDR2, ");
        stb.append("     GAD1.GUARD_TELNO, ");
        stb.append("     GAD1.GUARD_ADDR_FLG, ");
        stb.append("     H201.NAME1 AS GUARD_RELATIONSHIP_NAME, ");
        stb.append("     T2.EMERGENCYCALL, ");
        stb.append("     T2.EMERGENCYTELNO, ");
        stb.append("     T2.EMERGENCYCALL2, ");
        stb.append("     T2.EMERGENCYTELNO2, ");
        stb.append("     T2.EMERGENCYCALL3, ");
        stb.append("     T2.EMERGENCYTELNO3, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T6.COURSENAME, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T7.MAJORNAME, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T8.COURSECODENAME, ");
        stb.append("     A023.ABBV1 AS SCHOOL_KIND_NAME, ");
        stb.append("     JFIN.FINSCHOOL_NAME AS JUNIOR_FINSCHOOL_NAME, ");
        stb.append("     T2.BLOODTYPE, ");
        stb.append("     RI1.HR_NAME AS REGD_HR_NAME1, RI1.STAFFNAME AS REGD_STAFFNAME1, ");
        stb.append("     RI2.HR_NAME AS REGD_HR_NAME2, RI2.STAFFNAME AS REGD_STAFFNAME2, ");
        stb.append("     RI3.HR_NAME AS REGD_HR_NAME3, RI3.STAFFNAME AS REGD_STAFFNAME3, ");
        stb.append("     RI4.HR_NAME AS REGD_HR_NAME4, RI4.STAFFNAME AS REGD_STAFFNAME4, ");
        stb.append("     CASE WHEN T2.BIRTHDAY IS NOT NULL THEN YEAR(DATE('" + param._year + "' || '-04-01') - T2.BIRTHDAY) END AS AGE, ");
        stb.append("     DAY1.WRITING_DATE1, ");
        stb.append("     DAY2.WRITING_DATE2 ");
        stb.append(" FROM  SCHREG_REGD_DAT T1 ");
        stb.append(" INNER JOIN V_SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT T5 ON T5.YEAR = T1.YEAR ");
        stb.append("     AND T5.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T5.GRADE = T1.GRADE ");
        stb.append("     AND T5.HR_CLASS = T1.HR_CLASS ");
        stb.append(" LEFT JOIN COURSE_MST T6 ON T6.COURSECD = T1.COURSECD ");
        stb.append(" LEFT JOIN MAJOR_MST T7 ON T7.COURSECD = T1.COURSECD ");
        stb.append("     AND T7.MAJORCD = T1.MAJORCD ");
        stb.append(" LEFT JOIN SCHREG_REGD_GDAT GD ON GD.YEAR = T1.YEAR ");
        stb.append("     AND GD.GRADE = T1.GRADE ");
        stb.append(" LEFT JOIN COURSECODE_MST T8 ON T8.COURSECODE = T1.COURSECODE ");
        stb.append(" LEFT JOIN GUARDIAN_DAT GUARD ON GUARD.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = T2.SEX ");
        stb.append(" LEFT JOIN NAME_MST H201 ON H201.NAMECD1 = 'H201' AND H201.NAMECD2 = GUARD.RELATIONSHIP ");
        stb.append(" LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' AND A023.NAME1 = GD.SCHOOL_KIND ");
        stb.append(" LEFT JOIN ADDRESS AD1 ON AD1.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN GUARD_ADDRESS GAD1 ON GAD1.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD_H ON ENTGRD_H.SCHREGNO = T1.SCHREGNO AND ENTGRD_H.SCHOOL_KIND = 'H' ");
        stb.append(" LEFT JOIN FINSCHOOL_MST JFIN ON JFIN.FINSCHOOLCD = ENTGRD_H.FINSCHOOLCD ");
        stb.append(" LEFT JOIN STAFF_MST STFHR ON STFHR.STAFFCD = T5.TR_CD1 ");
        stb.append(" LEFT JOIN REGD_INFO RI1 ON RI1.SCHREGNO = T1.SCHREGNO AND RI1.ORDER = 1 ");
        stb.append(" LEFT JOIN REGD_INFO RI2 ON RI2.SCHREGNO = T1.SCHREGNO AND RI2.ORDER = 2 ");
        stb.append(" LEFT JOIN REGD_INFO RI3 ON RI3.SCHREGNO = T1.SCHREGNO AND RI3.ORDER = 3 ");
        stb.append(" LEFT JOIN REGD_INFO RI4 ON RI4.SCHREGNO = T1.SCHREGNO AND RI4.ORDER = 4 ");
        stb.append(" LEFT JOIN WRITING_DAY1 DAY1 ON DAY1.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN WRITING_DAY2 DAY2 ON DAY2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + param._semester + "'  ");
        stb.append("     AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREGNOS) ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _loginDate;
        final String _staffcd;
        final String _writingDate;
        final String[] _schregSelected;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _staffcd = request.getParameter("STAFFCD");
            final String paraWritingDate = request.getParameter("WRITING_DATE");
            _writingDate = StringUtils.replace(paraWritingDate, "/", "-");
            _schregSelected = request.getParameterValues("SCHREG_SELECTED");
        }
    }
}

// eof

