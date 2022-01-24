/*
 * $Id: a6f8a931bc26c497e53eb14ebb0359889424b7e3 $
 *
 * 作成日: 2016/10/21
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL321F {

    private static final Log log = LogFactory.getLog(KNJL321F.class);

    private static final String IEE = "2";

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
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            final List receptAllList = Recept.getReceptList(db2, _param);

            if (_param._isCsv) {
                if (!receptAllList.isEmpty()) {
                    outputCsv(db2, response, receptAllList);
                }
            } else {

                response.setContentType("application/pdf");

                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                if (!receptAllList.isEmpty()) {
                    printMain(db2, svf, receptAllList);
                }
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (null != _param && _param._isCsv) {
            } else {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void outputCsv(final DB2UDB db2, final HttpServletResponse response, final List receptList) {
        final List lines = getCsvOutputLines(db2, receptList);

        CsvUtils.outputLines(log, response, getTitle(db2) + " " + getSubtitle() + " " + ("1".equals(_param._sort) ? "得点順" : "受験番号順") + ".csv" , lines);
    }

    private List getCsvOutputLines(final DB2UDB db2, final List receptList) {
        final List lines = new ArrayList();

        final boolean isForm3 = "6".equals(_param._examType) || "7".equals(_param._examType) || "8".equals(_param._examType);
        final boolean isForm4 = _param._isIee;

        newLine(lines).addAll(Arrays.asList(new String[] {getTitle(db2) + " " + getSubtitle() + " " + getOrder() + " " + getTestdiv5Output()}));
        newLine(lines).addAll(Arrays.asList(new String[] {_param._currentTime}));
        final List header1 = newLine(lines);
        header1.addAll(Arrays.asList(new String[] {"", "受験型", "受験番号", "氏名", "ふりがな", "担当者", "合否",}));
        header1.addAll(_param._subclassnameList);
        if (isForm4) {
            header1.addAll(Arrays.asList(new String[] {"備考",}));
        } else if (isForm3) {
            header1.addAll(Arrays.asList(new String[] {"面接", "面接備考",}));
        }
        header1.addAll(Arrays.asList(new String[] {"英検", "姉妹", "鏡友会校友会", "第一希望でない", "併願校", "",     "", "", "", "", "", "", "塾名", "教室", "受験番号\u2460", "受験番号\u2461", "受験番号\u2462", "受験番号\u2463", "受験番号\u2464", "受験番号\u2465", "受験番号(適正)", "受験番号(思考力A)", "受験番号(思考力B)", "受験番号(英語A)", "受験番号(英語B)", }));

        final List blank = new ArrayList();
        for (int i = 0; i < _param._subclassnameList.size(); i++) {
            blank.add("");
        }

        final List header2 = newLine(lines);
        header2.addAll(Arrays.asList(new String[] {"", "",       "",         "",     "",         "",       "",}));
        header2.addAll(blank);
        if (isForm4) {
            header2.addAll(Arrays.asList(new String[] {""}));
        } else if (isForm3) {
            header2.addAll(Arrays.asList(new String[] {"",     "",}));
        }
        header2.addAll(Arrays.asList(new String[] {"",     "",     "",             "",     "",               "",       "合否", "", "合否", "", "合否", "", "合否"}));

        for (int j = 0; j < receptList.size(); j++) {
            final Recept recept = (Recept) receptList.get(j);

            final List line = newLine(lines);

//            line.add(recept._examno); // 管理番号
            line.add(String.valueOf(j + 1)); // 行番号
            line.add(recept._examTypeName); // 受験型
            line.add(recept._receptno); // 受験番号
            line.add(recept._name); // 氏名
            line.add(recept._nameKana); // フリガナ

            line.add(recept._wrapupRemark); // 担当者
            if ("4".equals(recept._judgediv)) {
                line.add(recept._judgedivName); // 合否
            } else {
                line.add(recept._judgedivName); // 合否
            }
            final Map subclassScoreMap = recept.getPrintSubclassMap(_param, "CD");
            for (int i = 0; i < _param._subclassList.size(); i++) {
                final Subclass subclass = (Subclass) _param._subclassList.get(i);
                final String score = (String) subclassScoreMap.get(subclass._cd);
                line.add(score); // 点数
            }

            if (isForm4) {
                line.add(recept._interviewRemark2); // 備考
            } else if (isForm3) {
                line.add(recept._interviewValue); // 面接
                line.add(recept._interviewRemark); // 面接備考
            }
            line.add(recept._eikenName); // 英検

            line.add(recept.sisterFlg(true)); // 姉妹
            line.add(recept.motherFlg(true)); // 母卒業
            line.add("1".equals(recept._shFlg) ? "" : "レ"); // 第１志望でない

            for (int k = 0; k < 4; k++) {
                final String shschoolname = new String[] {recept._shSchoolname1, recept._shSchoolname2, recept._shSchoolname3, recept._shSchoolname4, recept._shSchoolname5, recept._shSchoolname6}[k];
                final String judge = new String[] {recept._shJudgement1, recept._shJudgement2, recept._shJudgement3, recept._shJudgement4, recept._shJudgement5, recept._shJudgement6}[k];
                line.add(shschoolname); // 併願校
                line.add(_param._nmL035Abbv2Map.get(judge)); // 併願校合否
            }

            line.add(recept._prischoolName); // 塾名

            line.add(recept._prischoolClassName); // 教室名

            String[] tdivStr = {"1", "16", "2", "3", "5", "17", "18"};
            for (int idx = 0;idx < tdivStr.length;idx++) {
                final String testdiv = tdivStr[idx];
                line.add(StringUtils.defaultString((String) recept._testdivExamnoMap.get(testdiv)) + " " + StringUtils.defaultString((String) _param._nmL013Abbv2Map.get(recept._testdivJudgedivMap.get(testdiv)))); // 受験番号 合否
            }
            String[] tdivStr2 = {"9", "10", "12", "13"}; // 思考力A, 思考力B, 英語A, 英語B
            for (int idx = 0;idx < tdivStr2.length;idx++) {
                final String testdiv = tdivStr2[idx];
                line.add(StringUtils.defaultString((String) recept._testdivExamnoMap.get(testdiv)) + " " + StringUtils.defaultString((String) _param._nmL013Abbv2Map.get(recept._testdivJudgedivMap.get(testdiv)))); // 受験番号 合否
            }
        }

        _hasData = true;

        return lines;
    }

    private List newLine(final List lines) {
        final List line = new ArrayList();
        lines.add(line);
        return line;
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
        return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private String getTitle(final DB2UDB db2) {
        final String title = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 " + StringUtils.defaultString(_param._applicantdivName) + StringUtils.defaultString(_param._testdivAbbv1) + " 入試判定会議資料";
        return title;
    }

    private String getSubtitle() {
        final String subtitle;
        if (_param._isIee) {
            subtitle = "";
        } else if ("X".equals(_param._examType)) {
            final String examTypeName1 = StringUtils.defaultString((String) _param._nmL005Name1Map.get("1"));
            final String examTypeName2 = StringUtils.defaultString((String) _param._nmL005Name1Map.get("2"));
            subtitle = "(" + examTypeName1 + "と" + examTypeName2 + ")";
        } else {
            subtitle = "(" + StringUtils.defaultString((String) _param._nmL005Name1Map.get(_param._examType)) + ")";
        }
        return subtitle;
    }

    private String getOrder() {
        return "1".equals(_param._sort) ? ("X".equals(_param._examType) ? "国算の得点順" : "得点順") : "受験番号順";
    }

    private String getTestdiv5Output() {
        if (!"5".equals(_param._testdiv)) {
            return "";
        }
        String s = "";
        if ("1".equals(_param._testdiv5out)) {
            s = StringUtils.defaultString(_param._testdivName1);
        } else if ("2".equals(_param._testdiv5out)) {
            s = "特別入試";
        } else if ("3".equals(_param._testdiv5out)) {
            s = StringUtils.defaultString(_param._testdivName1) + "+特別入試";
        }
        return s;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final List receptAllList) {

        final String subtitle = getSubtitle();

        final String form;

        final boolean isForm2 = "1".equals(_param._examType) || "A".equals(_param._examType);
        final boolean isForm3 = "6".equals(_param._examType) || "7".equals(_param._examType) || "8".equals(_param._examType);
        final boolean isForm4 = _param._isIee;
        if (isForm4) {
            form = "KNJL321F_4.frm";
        } else if (isForm3) {
            form = "KNJL321F_3.frm";
        } else if (isForm2) {
            form = "KNJL321F_2.frm";
        } else {
            form = "KNJL321F.frm";
        }

        final int maxLine = 35;

        svf.VrSetForm(form, 1);
        final List pageList = getPageList(receptAllList, maxLine);
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List receptList = (List) pageList.get(pi);

            svf.VrsOut("TITLE", getTitle(db2)); // タイトル
            svf.VrsOut("SUBTITLE", subtitle + getOrder() + " " + getTestdiv5Output()); // サブタイトル
//            svf.VrsOut("SORT_NAME", "(" + getOrder() + ")"); // ソート名
            svf.VrsOut("DATE", _param._currentTime); // 日付

            for (int i = 0; i < _param._subclassnameList.size(); i++) {
                svf.VrsOut("SUBCLASS_NAME" + String.valueOf(i), (String) _param._subclassnameList.get(i)); // 科目名称
            }

            if (isForm4) {
                svf.VrsOut("INTERVIEW_REMARK_NAME", "備考"); // 備考
            } else if (isForm3) {
                svf.VrsOut("INTERVIEW_VALUE_NAME", "面接"); // 面接
                svf.VrsOut("INTERVIEW_REMARK_NAME", "面接備考"); // 面接備考
            }

            for (int j = 0; j < receptList.size(); j++) {
                final Recept recept = (Recept) receptList.get(j);

                final int line = j + 1;
                svf.VrsOutn("NO", line, String.valueOf(maxLine * pi + j + 1)); // 行番号
                if (getMS932ByteCount(recept._examTypeName) > 6) {
                    svf.VrsOutn("EXAN_DIV2_1", line, recept._examTypeName); // 受験型
                } else {
                    svf.VrsOutn("EXAN_DIV", line, recept._examTypeName); // 受験型
                }
                svf.VrsOutn("EXAN_NO", line, recept._receptno); // 受験番号

                final int namelen = getMS932ByteCount(recept._name);
                svf.VrsOutn("NAME" + (namelen > 20 ? "2" : "1"), line, recept._name); // 氏名
                final int nameKanalen = getMS932ByteCount(recept._nameKana);
                svf.VrsOutn("KANA" + (nameKanalen > 20 ? "2" : "1"), line, recept._nameKana); // フリガナ

                svf.VrsOutn("ABSENCE", line, "4".equals(recept._judgediv) ? recept._judgedivName : ""); // 欠席
                svf.VrsOutn("CHARGE_NAME", line, recept._wrapupRemark); // 担当者
                if ("4".equals(recept._judgediv)) {
                    svf.VrsOutn("JUDGE2", line, recept._judgedivName); // 合否
                } else {
                    svf.VrsOutn("JUDGE", line, recept._judgedivName); // 合否
                }
                final Map subclassScoreMap = recept.getPrintSubclassMap(_param, "IDX");
//                log.info(" " + recept._examno + " / subclasscdList = " + subclassScoreMap);
                for (final Iterator it = subclassScoreMap.keySet().iterator(); it.hasNext();) {
                    final String location = (String) it.next();
                    final String score = (String) subclassScoreMap.get(location);
                    svf.VrsOutn("SCORE" + location, line, score); // 点数
                }
                if (isForm4) {
                    svf.VrsOutn("INTERVIEW_REMARK", line, recept._interviewRemark2); // 備考
                } else if (isForm3) {
                    svf.VrsOutn("INTERVIEW_VALUE", line, recept._interviewValue); // 面接
                    svf.VrsOutn("INTERVIEW_REMARK", line, recept._interviewRemark); // 面接備考
                }
                svf.VrsOutn("EIKEN", line, recept._eikenName); // 英検
                final String sisterFlg = recept.sisterFlg(false);
                if (getMS932ByteCount(sisterFlg) > 2) {
                    svf.VrsOutn("SISTER2", line, sisterFlg); // 姉妹
                } else {
                    svf.VrsOutn("SISTER", line, sisterFlg); // 姉妹
                }
                final String motherFlg = recept.motherFlg(false);
                if (getMS932ByteCount(motherFlg) > 2) {
                    svf.VrsOutn("MOTHER2", line, motherFlg); // 母卒業
                } else {
                    svf.VrsOutn("MOTHER", line, motherFlg); // 母卒業
                }
//                svf.VrsOutn("POST", line, null != recept._ganshoYuusou ? "レ" : ""); // 郵送
                svf.VrsOutn("HOPE", line, "1".equals(recept._shFlg) ? "" : "レ"); // 第１志望でない

                for (int k = 0; k < 4; k++) {
                    final String shschoolname = new String[] {recept._shSchoolname1, recept._shSchoolname2, recept._shSchoolname3, recept._shSchoolname4, recept._shSchoolname5, recept._shSchoolname6}[k];
                    final String judge = new String[] {recept._shJudgement1, recept._shJudgement2, recept._shJudgement3, recept._shJudgement4, recept._shJudgement5, recept._shJudgement6}[k];
                    svf.VrsOutn("ANOTHER_SCHOOL_JUDGE" + String.valueOf(k + 1), line, (String) _param._nmL035Abbv2Map.get(judge)); // 併願校合否
                    final int shschoolnamelen = getMS932ByteCount(shschoolname);
                    final String shSchoolField;
                    if (isForm2) {
                        shSchoolField = "ANOTHER_SCHOOL_NAME" + String.valueOf(k + 1) + "_" + (shschoolnamelen > 34 ? "4_1" : shschoolnamelen > 26 ? "3" : shschoolnamelen > 20 ? "2" : "1");
                    } else {
                        shSchoolField = "ANOTHER_SCHOOL_NAME" + String.valueOf(k + 1) + "_" + (shschoolnamelen > 20 ? "4_1" : shschoolnamelen > 16 ? "3" : shschoolnamelen > 12 ? "2" : "1");
                    }
                    svf.VrsOutn(shSchoolField, line, shschoolname); // 併願校
                }

                final int jukunamelen = getMS932ByteCount(recept._prischoolName);
                final String prischoolField;
                if (isForm2) {
                    prischoolField = "CRAMML_NAME" + (jukunamelen > 34 ? "4_1" : jukunamelen > 26 ? "3" : jukunamelen > 20 ? "2" : "1");
                } else {
                    prischoolField = "CRAMML_NAME" + (jukunamelen > 18 ? "4_1" : jukunamelen > 14 ? "3" : jukunamelen > 10 ? "2" : "1");
                }
                svf.VrsOutn(prischoolField, line, recept._prischoolName); // 塾名

                final int jukuclassnamelen = getMS932ByteCount(recept._prischoolClassName);
                final String prischoolField2;
                if (isForm2) {
                    prischoolField2 = "CRAMM_CLASSROOM_NAME" + (jukuclassnamelen > 34 ? "4_1" : jukuclassnamelen > 26 ? "3" : jukuclassnamelen > 20 ? "2" : "1");
                } else {
                    prischoolField2 = "CRAMM_CLASSROOM_NAME" + (jukuclassnamelen > 18 ? "4_1" : jukuclassnamelen > 14 ? "3" : jukuclassnamelen > 10 ? "2" : "1");
                }
                svf.VrsOutn(prischoolField2, line, recept._prischoolClassName); // 教室名

                String[] tdivStr = {"1", "16", "2", "3", "5", "17", "18"};
                for (int idx = 0;idx < tdivStr.length;idx++) {
                    final String testdiv;
                    testdiv = tdivStr[idx];
                    svf.VrsOutn("EXAN_NO" + (idx + 1), line, (String) recept._testdivExamnoMap.get(testdiv)); // 受験番号
                    svf.VrsOutn("EXAN_JUDGE" + (idx + 1), line, (String) _param._nmL013Abbv2Map.get(recept._testdivJudgedivMap.get(testdiv))); // 受験番号合否
                }
                String[] tdivStr2 = {"9", "10", "12", "13"}; // 思考力A, 思考力B, 英語A, 英語B
                for (int idx = 0;idx < tdivStr2.length;idx++) {
                    final String testdiv = tdivStr2[idx];
                    svf.VrsOutn("EXAN_NO" + testdiv, line, (String) recept._testdivExamnoMap.get(testdiv)); // 受験番号
                    svf.VrsOutn("EXAN_JUDGE" + testdiv, line, (String) _param._nmL013Abbv2Map.get(recept._testdivJudgedivMap.get(testdiv)));
                }
            }

            _hasData = true;

            svf.VrEndPage();
        }
    }

    private static class Recept {
        final String _examno;
        final String _examType;
        final String _examTypeName;
        final String _receptno;
        final String _testdiv;
        final String _name;
        final String _nameKana;
        final String _score1Subclasscd;
        final String _score2Subclasscd;
        final String _score1;
        final String _score2;
        final String _total2;
        final String _total4;
        final String _judgediv;
        final String _judgedivName;
        final String _judgeKind;
        final String _judgeKindAbbv;
        final String _interviewValue;
        final String _interviewRemark;
        final String _interviewRemark2;
        final String _eikenName;
        final String _sisterFlg;
        final String _sisterFlg2;
        final String _motherFlg;
        final String _prischoolName;
        final String _prischoolClassName;
        final String _staffname;
        final String _shFlg;
        final String _shSchoolname1;
        final String _shJudgement1;
        final String _shSchoolname2;
        final String _shJudgement2;
        final String _shSchoolname3;
        final String _shJudgement3;
        final String _shSchoolname4;
        final String _shJudgement4;
        final String _shSchoolname5;
        final String _shJudgement5;
        final String _shSchoolname6;
        final String _shJudgement6;
        final String _ganshoYuusou;
        final String _gokankei;
        final String _wrapupRemark;

        final Map _scoreMap = new HashMap();
        final Map _testdivExamnoMap = new HashMap();
        final Map _testdivJudgedivMap = new HashMap();
        final Map _honordivTotal4Map = new HashMap();

        Recept(
            final String examno,
            final String examType,
            final String examTypeName,
            final String receptno,
            final String testdiv,
            final String name,
            final String nameKana,
            final String score1Subclasscd,
            final String score2Subclasscd,
            final String score1,
            final String score2,
            final String total2,
            final String total4,
            final String judgediv,
            final String judgedivName,
            final String judgeKind,
            final String judgeKindAbbv,
            final String interviewValue,
            final String interviewRemark,
            final String interviewRemark2,
            final String eikenName,
            final String sisterFlg,
            final String sisterFlg2,
            final String motherFlg,
            final String prischoolName,
            final String prischoolClassName,
            final String staffname,
            final String shFlg,
            final String shSchoolname1,
            final String shJudgement1,
            final String shSchoolname2,
            final String shJudgement2,
            final String shSchoolname3,
            final String shJudgement3,
            final String shSchoolname4,
            final String shJudgement4,
            final String shSchoolname5,
            final String shJudgement5,
            final String shSchoolname6,
            final String shJudgement6,
            final String ganshoYuusou,
            final String gokankei,
            final String wrapupRemark
        ) {
            _examno = examno;
            _examType = examType;
            _examTypeName = examTypeName;
            _receptno = receptno;
            _testdiv = testdiv;
            _name = name;
            _nameKana = nameKana;
            _score1Subclasscd = score1Subclasscd;
            _score2Subclasscd = score2Subclasscd;
            _score1 = score1;
            _score2 = score2;
            _total2 = total2;
            _total4 = total4;
            _judgediv = judgediv;
            _judgedivName = judgedivName;
            _judgeKind = judgeKind;
            _judgeKindAbbv = judgeKindAbbv;
            _interviewValue = interviewValue;
            _interviewRemark = interviewRemark;
            _interviewRemark2 = interviewRemark2;
            _eikenName = eikenName;
            _sisterFlg = sisterFlg;
            _sisterFlg2 = sisterFlg2;
            _motherFlg = motherFlg;
            _prischoolName = prischoolName;
            _prischoolClassName = prischoolClassName;
            _staffname = staffname;
            _shFlg = shFlg;
            _shSchoolname1 = shSchoolname1;
            _shJudgement1 = shJudgement1;
            _shSchoolname2 = shSchoolname2;
            _shJudgement2 = shJudgement2;
            _shSchoolname3 = shSchoolname3;
            _shJudgement3 = shJudgement3;
            _shSchoolname4 = shSchoolname4;
            _shJudgement4 = shJudgement4;
            _shSchoolname5 = shSchoolname5;
            _shJudgement5 = shJudgement5;
            _shSchoolname6 = shSchoolname6;
            _shJudgement6 = shJudgement6;
            _ganshoYuusou = ganshoYuusou;
            _gokankei = gokankei;
            _wrapupRemark = wrapupRemark;
        }

        public String sisterFlg(boolean csv) {
            String rtn = null;
            if (!"".equals(_sisterFlg)) {
                rtn = _sisterFlg; // 姉妹
            } else {
                rtn = null != _sisterFlg2 ? (csv ? _sisterFlg2 : "有") : "";
            }
            return rtn;
        }

        public String motherFlg(boolean csv) {
            String rtn = null;
            if (null != _gokankei) {
                rtn = _gokankei;
            } else {
                rtn = null != _motherFlg ? (csv ? _motherFlg : "有") : "";
            }
            return rtn;
        }

        public Map getPrintSubclassMap(final Param param, final String mapKey) {
//            if (null == subclasscdList) {
//                final Map testsubclasscdScoreListMap = new HashMap();
//                for (final Iterator it = _scoreMap.keySet().iterator(); it.hasNext();) {
//                    final String testsubclasscd = (String) it.next();
//                    final String score = (String) _scoreMap.get(testsubclasscd);
//                    if (!NumberUtils.isDigits(score)) {
//                        continue;
//                    }
//                    if ("1".equals(param._applicantdiv) && "2".equals(_examType)) {
//                        // 2科+選択
//                        if ("1".equals(testsubclasscd) || "2".equals(testsubclasscd)) {
//                            getMappedList(testsubclasscdScoreListMap, testsubclasscd).add(score);
//                        } else if (null != _score1Subclasscd && _score1Subclasscd.equals(testsubclasscd) ||
//                                null != _score2Subclasscd && _score2Subclasscd.equals(testsubclasscd)) {
//                            final String location = (String) param._nmL009Namespare1Map.get(testsubclasscd);
//                            getMappedList(testsubclasscdScoreListMap, NumberUtils.isDigits(location) ? location : testsubclasscd).add(score);
//                        }
//                    } else {
//                        getMappedList(testsubclasscdScoreListMap, testsubclasscd).add(score);
//                    }
//                }
//                //log.debug(" receptno = " + _receptno + ", scoreListMap = " + testsubclasscdScoreListMap);
//                final Map rtn = new HashMap();
//                for (final Iterator it = testsubclasscdScoreListMap.keySet().iterator(); it.hasNext();) {
//                    final String location = (String) it.next();
//                    if (NumberUtils.isDigits(location)) {
//                        rtn.put(location, sum(getMappedList(testsubclasscdScoreListMap, location)));
//                    }
//                }
//                return rtn;
//            }

            final Map rtn = new HashMap();
            for (int i = 0; i < param._subclassList.size(); i++) {
                final Subclass subclass = (Subclass) param._subclassList.get(i);
                final String score;
                if ("TOTAL2".equals(subclass._cd)) {
                    score = _total2;
                } else if ("TOTAL4".equals(subclass._cd)) {
                    score = _total4;
                } else if ("JUDGE_KIND_SCORE".equals(subclass._cd)) {
                    score = (String) _honordivTotal4Map.get(_judgeKind);
                } else if ("JUDGE_KIND".equals(subclass._cd)) {
                    score = _judgeKindAbbv;
                } else if ("MAX1+MAX2".equals(subclass._cd)) {
//                    final String score1 = (String) _scoreMap.get(_score1Subclasscd);
//                    final String score2 = (String) _scoreMap.get(_score2Subclasscd);
//                    score = sum(Arrays.asList(new String[] {score1, score2}));
                    score = sum(Arrays.asList(new String[] {_score1, _score2}));
                } else if ("MAX1".equals(subclass._cd)) {
                    score = _score1;
                } else if ("MAX2".equals(subclass._cd)) {
                    score = _score2;
                } else {
//                    final String key;
//                    if ("MAX1".equals(subclass._cd)) {
//                        key = _score1Subclasscd;
//                    } else if ("MAX2".equals(subclass._cd)) {
//                        key = _score2Subclasscd;
//                    } else {
//                        key = subclass._cd;
//                    }
//                    score = (String) _scoreMap.get(key);
                    score = (String) _scoreMap.get(subclass._cd);
                }
                if (null != score) {
                    if ("CD".equals(mapKey)) {
                        rtn.put(subclass._cd, score);
                    } else if ("IDX".equals(mapKey)) {
                        rtn.put(String.valueOf(i), score);
                    }
                }
            }
            return rtn;
        }

        private static String sum(final List list) {
            if (null == list) {
                return null;
            }
            if (list.size() == 1) {
                return (String) list.get(0);
            }
            String rtn = null;
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final String score = (String) it.next();
                if (null == rtn) {
                    rtn = score;
                } else if (NumberUtils.isDigits(score)) {
                    rtn = String.valueOf(Integer.parseInt(rtn) + Integer.parseInt(score));
                }
            }
            return rtn;
        }

        public static List getReceptList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.info(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");
                    final String examType = rs.getString("EXAM_TYPE");
                    final String examTypeName = rs.getString("EXAM_TYPE_NAME");
                    final String receptno = rs.getString("RECEPTNO");
                    final String testdiv = rs.getString("TESTDIV");
                    final String name = rs.getString("NAME");
                    final String nameKana = rs.getString("NAME_KANA");
                    final String score1Subclasscd = rs.getString("SCORE1_SUBCLASSCD");
                    final String score2Subclasscd = rs.getString("SCORE2_SUBCLASSCD");
                    final String score1 = rs.getString("SCORE1");
                    final String score2 = rs.getString("SCORE2");
                    final String total2 = rs.getString("TOTAL2");
                    final String total4 = rs.getString("TOTAL4");
                    final String judgediv = rs.getString("JUDGEDIV");
                    final String judgedivName = rs.getString("JUDGEDIV_NAME");
                    final String judgeKind = rs.getString("JUDGE_KIND");
                    final String judgeKindAbbv = rs.getString("JUDGE_KIND_ABBV");
                    final String interviewValue = rs.getString("INTERVIEW_VALUE");
                    final String interviewRemark = rs.getString("INTERVIEW_REMARK");
                    final String interviewRemark2 = rs.getString("INTERVIEW_REMARK2");
                    final String eikenName = rs.getString("EIKEN_NAME");
                    final String sisterFlg = rs.getString("SISTER_FLG");
                    final String sisterFlg2 = rs.getString("SISTER_FLG2");
                    final String motherFlg = rs.getString("MOTHER_FLG");
                    final String prischoolName = rs.getString("PRISCHOOL_NAME");
                    final String prischoolClassName = rs.getString("PRISCHOOL_CLASS_NAME");
                    final String staffname = rs.getString("STAFFNAME");
                    final String shFlg = rs.getString("SH_FLG");
                    final String shSchoolname1 = rs.getString("SH_SCHOOLNAME1");
                    final String shJudgement1 = rs.getString("SH_JUDGEMENT1");
                    final String shSchoolname2 = rs.getString("SH_SCHOOLNAME2");
                    final String shJudgement2 = rs.getString("SH_JUDGEMENT2");
                    final String shSchoolname3 = rs.getString("SH_SCHOOLNAME3");
                    final String shJudgement3 = rs.getString("SH_JUDGEMENT3");
                    final String shSchoolname4 = rs.getString("SH_SCHOOLNAME4");
                    final String shJudgement4 = rs.getString("SH_JUDGEMENT4");
                    final String shSchoolname5 = rs.getString("SH_SCHOOLNAME5");
                    final String shJudgement5 = rs.getString("SH_JUDGEMENT5");
                    final String shSchoolname6 = rs.getString("SH_SCHOOLNAME6");
                    final String shJudgement6 = rs.getString("SH_JUDGEMENT6");
                    final String ganshoYuusou = rs.getString("GANSHO_YUUSOU");
                    final String gokankei = rs.getString("GOKANKEI");
                    final String wrapupRemark = rs.getString("WRAPUP_REMARK");
                    final Recept recept = new Recept(examno, examType, examTypeName, receptno, testdiv, name, nameKana, score1Subclasscd, score2Subclasscd, score1, score2, total2, total4, judgediv, judgedivName, judgeKind, judgeKindAbbv, interviewValue, interviewRemark, interviewRemark2, eikenName, sisterFlg, sisterFlg2, motherFlg, prischoolName, prischoolClassName, staffname, shFlg, shSchoolname1, shJudgement1, shSchoolname2, shJudgement2, shSchoolname3, shJudgement3, shSchoolname4, shJudgement4, shSchoolname5, shJudgement5, shSchoolname6, shJudgement6, ganshoYuusou, gokankei, wrapupRemark);
                    list.add(recept);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final String sql = testJudgedivSql(param);
                log.info(" judgediv sql = " + sql);
                ps = db2.prepareStatement(sql);

                final String[] testdivs = {"1", "16", "2", "3", "5", "17", "18", "9", "10", "11", "12", "13", "14"};

                for (final Iterator it = list.iterator(); it.hasNext();) {
                    final Recept recept = (Recept) it.next();

                    ps.setString(1, recept._examno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        for (int i = 0; i < testdivs.length; i++) {
                            final String testdiv = testdivs[i];
                            recept._testdivExamnoMap.put(testdiv, rs.getString("EXAMNO" + testdiv));
                            recept._testdivJudgedivMap.put(testdiv, rs.getString("JUDGEDIV" + testdiv));
                            //log.info(" set testdivJudgedivMap " + recept._examno + " testdiv " + testdiv + " " + recept._testdivJudgedivMap.get(testdiv));
                            if (!"5".equals(testdiv)) {
                                final String honordiv = rs.getString("HONORDIV" + testdiv);
                                if (null != honordiv) {
                                    recept._honordivTotal4Map.put(honordiv, rs.getString("TOTAL4_" + testdiv));
                                    log.info(" set honordivtotal4 " + recept._examno + " honordiv " + honordiv + " " + recept._honordivTotal4Map.get(honordiv));
                                }
                            }
                        }

                    }

                    DbUtils.closeQuietly(rs);

                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final String sql = scoreSql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = list.iterator(); it.hasNext();) {
                    final Recept recept = (Recept) it.next();

                    ps.setString(1, recept._receptno);

                    rs = ps.executeQuery();

                    while (rs.next()) {
                        recept._scoreMap.put(rs.getString("TESTSUBCLASSCD"), rs.getString("SCORE"));
                    }

                    DbUtils.closeQuietly(rs);

                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String scoreSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TREC.TESTSUBCLASSCD ");
            stb.append("   , TREC.SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_DAT TREC ");
            stb.append(" WHERE ");
            stb.append("     TREC.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND TREC.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND TREC.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND TREC.EXAM_TYPE = '1' ");
            stb.append("     AND TREC.RECEPTNO = ? ");
            return stb.toString();
        }

        private static String testJudgedivSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TREC.EXAMNO ");
            stb.append("   , TRDET012.REMARK1 AS EXAMNO1 ");
            stb.append("   , TRDET012.REMARK16 AS EXAMNO16 ");
            stb.append("   , TRDET012.REMARK2 AS EXAMNO2 ");
            stb.append("   , TRDET012.REMARK3 AS EXAMNO3 ");
            stb.append("   , TRDET012.REMARK5 AS EXAMNO5 ");
            stb.append("   , TRDET012.REMARK17 AS EXAMNO17 ");
            stb.append("   , TRDET012.REMARK18 AS EXAMNO18 ");
            stb.append("   , TRDET012.REMARK9 AS EXAMNO9 ");
            stb.append("   , TRDET012.REMARK10 AS EXAMNO10 ");
            stb.append("   , TRDET012.REMARK11 AS EXAMNO11 ");
            stb.append("   , TRDET012.REMARK12 AS EXAMNO12 ");
            stb.append("   , TRDET012.REMARK13 AS EXAMNO13 ");
            stb.append("   , TRDET012.REMARK14 AS EXAMNO14 ");
            stb.append("   , RECEP1.JUDGEDIV AS JUDGEDIV1 ");
            stb.append("   , RECEP16.JUDGEDIV AS JUDGEDIV16 ");
            stb.append("   , RECEP2.JUDGEDIV AS JUDGEDIV2 ");
            stb.append("   , RECEP3.JUDGEDIV AS JUDGEDIV3 ");
            stb.append("   , RECEP5.JUDGEDIV AS JUDGEDIV5 ");
            stb.append("   , RECEP17.JUDGEDIV AS JUDGEDIV17 ");
            stb.append("   , RECEP18.JUDGEDIV AS JUDGEDIV18 ");
            stb.append("   , RECEP9.JUDGEDIV AS JUDGEDIV9 ");
            stb.append("   , RECEP10.JUDGEDIV AS JUDGEDIV10 ");
            stb.append("   , RECEP11.JUDGEDIV AS JUDGEDIV11 ");
            stb.append("   , RECEP12.JUDGEDIV AS JUDGEDIV12 ");
            stb.append("   , RECEP13.JUDGEDIV AS JUDGEDIV13 ");
            stb.append("   , RECEP14.JUDGEDIV AS JUDGEDIV14 ");
            stb.append("   , RECEP1.HONORDIV AS HONORDIV1 ");
            stb.append("   , RECEP16.HONORDIV AS HONORDIV16 ");
            stb.append("   , RECEP2.HONORDIV AS HONORDIV2 ");
            stb.append("   , RECEP3.HONORDIV AS HONORDIV3 ");
            stb.append("   , RECEP17.HONORDIV AS HONORDIV17 ");
            stb.append("   , RECEP18.HONORDIV AS HONORDIV18 ");
            stb.append("   , RECEP9.HONORDIV AS HONORDIV9 ");
            stb.append("   , RECEP10.HONORDIV AS HONORDIV10 ");
            stb.append("   , RECEP11.HONORDIV AS HONORDIV11 ");
            stb.append("   , RECEP12.HONORDIV AS HONORDIV12 ");
            stb.append("   , RECEP13.HONORDIV AS HONORDIV13 ");
            stb.append("   , RECEP14.HONORDIV AS HONORDIV14 ");
            stb.append("   , RECEP1.TOTAL4 AS TOTAL4_1 ");
            stb.append("   , RECEP16.TOTAL4 AS TOTAL4_16 ");
            stb.append("   , RECEP2.TOTAL4 AS TOTAL4_2 ");
            stb.append("   , RECEP3.TOTAL4 AS TOTAL4_3 ");
            stb.append("   , RECEP17.TOTAL4 AS TOTAL4_17 ");
            stb.append("   , RECEP18.TOTAL4 AS TOTAL4_18 ");
            stb.append("   , RECEP9.TOTAL4 AS TOTAL4_9 ");
            stb.append("   , RECEP10.TOTAL4 AS TOTAL4_10 ");
            stb.append("   , RECEP11.TOTAL4 AS TOTAL4_11 ");
            stb.append("   , RECEP12.TOTAL4 AS TOTAL4_12 ");
            stb.append("   , RECEP13.TOTAL4 AS TOTAL4_13 ");
            stb.append("   , RECEP14.TOTAL4 AS TOTAL4_14 ");
            stb.append(" FROM ");
            stb.append("     V_ENTEXAM_RECEPT_DAT TREC ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT TAPPL ON TAPPL.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TAPPL.EXAMNO = TREC.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT TRDET010 ON TRDET010.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRDET010.EXAMNO = TREC.EXAMNO ");
            stb.append("         AND TRDET010.SEQ = '010' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT TRDET012 ON TRDET012.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRDET012.EXAMNO = TREC.EXAMNO ");
            stb.append("         AND TRDET012.SEQ = '012' ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP1 ON RECEP1.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP1.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP1.TESTDIV = TRDET010.REMARK1 AND RECEP1.EXAM_TYPE = '1' AND RECEP1.RECEPTNO = TRDET012.REMARK1 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP16 ON RECEP16.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP16.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP16.TESTDIV = TRDET010.REMARK16 AND RECEP16.EXAM_TYPE = '1' AND RECEP16.RECEPTNO = TRDET012.REMARK16 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP2 ON RECEP2.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP2.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP2.TESTDIV = TRDET010.REMARK2 AND RECEP2.EXAM_TYPE = '1' AND RECEP2.RECEPTNO = TRDET012.REMARK2 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP3 ON RECEP3.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP3.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP3.TESTDIV = TRDET010.REMARK3 AND RECEP3.EXAM_TYPE = '1' AND RECEP3.RECEPTNO = TRDET012.REMARK3 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP5 ON RECEP5.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP5.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP5.TESTDIV = TRDET010.REMARK5 AND RECEP5.EXAM_TYPE = '1' AND RECEP5.RECEPTNO = TRDET012.REMARK5 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP17 ON RECEP17.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP17.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP17.TESTDIV = TRDET010.REMARK17 AND RECEP17.EXAM_TYPE = '1' AND RECEP17.RECEPTNO = TRDET012.REMARK17 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP18 ON RECEP18.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP18.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP18.TESTDIV = TRDET010.REMARK18 AND RECEP18.EXAM_TYPE = '1' AND RECEP18.RECEPTNO = TRDET012.REMARK18 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP9 ON RECEP9.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP9.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP9.TESTDIV = TRDET010.REMARK9 AND RECEP9.EXAM_TYPE = '1' AND RECEP9.RECEPTNO = TRDET012.REMARK9 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP10 ON RECEP10.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP10.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP10.TESTDIV = TRDET010.REMARK10 AND RECEP10.EXAM_TYPE = '1' AND RECEP10.RECEPTNO = TRDET012.REMARK10 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP11 ON RECEP11.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP11.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP11.TESTDIV = TRDET010.REMARK11 AND RECEP11.EXAM_TYPE = '1' AND RECEP11.RECEPTNO = TRDET012.REMARK11 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP12 ON RECEP12.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP12.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP12.TESTDIV = TRDET010.REMARK12 AND RECEP12.EXAM_TYPE = '1' AND RECEP12.RECEPTNO = TRDET012.REMARK12 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP13 ON RECEP13.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP13.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP13.TESTDIV = TRDET010.REMARK13 AND RECEP13.EXAM_TYPE = '1' AND RECEP13.RECEPTNO = TRDET012.REMARK13 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP14 ON RECEP14.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP14.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP14.TESTDIV = TRDET010.REMARK14 AND RECEP14.EXAM_TYPE = '1' AND RECEP14.RECEPTNO = TRDET012.REMARK14 ");
            stb.append(" WHERE ");
            stb.append("     TREC.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND TREC.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND TREC.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND TREC.EXAM_TYPE = '1' ");
            stb.append("     AND TREC.EXAMNO = ? ");
            return stb.toString();
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TREC.EXAMNO ");
            stb.append("   , TRDET011.REMARK" + param._testdiv + " AS EXAM_TYPE ");
            stb.append("   , NML005.NAME1 AS EXAM_TYPE_NAME ");
            stb.append("   , TREC.RECEPTNO ");
            stb.append("   , TREC.TESTDIV ");
            stb.append("   , TAPPL.NAME ");
            stb.append("   , TAPPL.NAME_KANA ");
            stb.append("   , TREC.TOTAL2 ");
            stb.append("   , TREC.TOTAL4 ");
            stb.append("   , TREC.JUDGEDIV ");
            stb.append("   , NML013.NAME1 AS JUDGEDIV_NAME ");
            stb.append("   , TAPPL.JUDGE_KIND ");
            stb.append("   , NML025.ABBV1 AS JUDGE_KIND_ABBV ");
            stb.append("   , TADET002.REMARK1 AS RECRUIT_NO ");
            stb.append("   , TINT.INTERVIEW_VALUE ");
            stb.append("   , TINT.INTERVIEW_REMARK ");
            stb.append("   , TINT.INTERVIEW_REMARK2 ");
            stb.append("   , NML055.NAME1 AS EIKEN_NAME ");
            stb.append("   , VALUE(NML056.NAME1, '') AS SISTER_FLG ");
            stb.append("   , TADET014.REMARK2 AS SISTER_FLG2 ");
            stb.append("   , TADET015.REMARK1 AS MOTHER_FLG ");
            stb.append("   , TRDET004.REMARK1 AS SCORE1_SUBCLASSCD ");
            stb.append("   , TRDET004.REMARK2 AS SCORE2_SUBCLASSCD ");
            stb.append("   , TRDET004.REMARK3 AS SCORE1 ");
            stb.append("   , TRDET004.REMARK4 AS SCORE2 ");
            stb.append("   , TPSM.PRISCHOOL_NAME ");
            stb.append("   , TPSCM.PRISCHOOL_NAME AS PRISCHOOL_CLASS_NAME ");
            stb.append("   , TSTFM.STAFFNAME ");
            stb.append("   , TEQD.SH_FLG ");
            stb.append("   , TEQD.SH_SCHOOLNAME1 ");
            stb.append("   , TEQD.SH_JUDGEMENT1 ");
            stb.append("   , TEQD.SH_SCHOOLNAME2 ");
            stb.append("   , TEQD.SH_JUDGEMENT2 ");
            stb.append("   , TEQD.SH_SCHOOLNAME3 ");
            stb.append("   , TEQD.SH_JUDGEMENT3 ");
            stb.append("   , TEQD.SH_SCHOOLNAME4 ");
            stb.append("   , TEQD.SH_JUDGEMENT4 ");
            stb.append("   , TEQD.SH_SCHOOLNAME5 ");
            stb.append("   , TEQD.SH_JUDGEMENT5 ");
            stb.append("   , TEQD.SH_SCHOOLNAME6 ");
            stb.append("   , TEQD.SH_JUDGEMENT6 ");
            stb.append("   , TAPPL.TESTDIV1 AS GANSHO_YUUSOU ");
            stb.append("   , VALUE(NML054.NAME1, '') AS GOKANKEI ");
            stb.append("   , WRAPUP.REMARK AS WRAPUP_REMARK ");
            stb.append(" FROM ");
            stb.append("     V_ENTEXAM_RECEPT_DAT TREC ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT TAPPL ON TAPPL.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TAPPL.EXAMNO = TREC.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT TRDET001 ON TRDET001.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRDET001.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND TRDET001.TESTDIV = TREC.TESTDIV ");
            stb.append("         AND TRDET001.EXAM_TYPE = '1' ");
            stb.append("         AND TRDET001.RECEPTNO = TREC.RECEPTNO ");
            stb.append("         AND TRDET001.SEQ = '001' ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT TRDET004 ON TRDET004.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRDET004.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND TRDET004.TESTDIV = TREC.TESTDIV ");
            stb.append("         AND TRDET004.EXAM_TYPE = '1' ");
            stb.append("         AND TRDET004.RECEPTNO = TREC.RECEPTNO ");
            stb.append("         AND TRDET004.SEQ = '004' ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT TRDET011 ON TRDET011.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRDET011.EXAMNO = TREC.EXAMNO ");
            stb.append("         AND TRDET011.SEQ = '011' ");
            if ("X".equals(param._examType)) {
                stb.append("     AND (TRDET011.REMARK" + param._testdiv + " = '1' OR TRDET011.REMARK" + param._testdiv + " = '2') ");
            } else {
                stb.append("     AND TRDET011.REMARK" + param._testdiv + " = '" + param._examType + "' ");
            }
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT TADET002 ON TADET002.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TADET002.EXAMNO = TREC.EXAMNO ");
            stb.append("         AND TADET002.SEQ = '002' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT TADET014 ON TADET014.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TADET014.EXAMNO = TREC.EXAMNO ");
            stb.append("         AND TADET014.SEQ = '014' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT TADET015 ON TADET015.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TADET015.EXAMNO = TREC.EXAMNO ");
            stb.append("         AND TADET015.SEQ = '015' ");
            stb.append("     LEFT JOIN ENTEXAM_QUESTIONNAIRE_DAT TEQD ON TEQD.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TEQD.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND TEQD.EXAMNO = TREC.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT TINT ON TINT.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TINT.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND TINT.TESTDIV = TREC.TESTDIV ");
            stb.append("         AND TINT.EXAMNO = TREC.EXAMNO ");
            stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' ");
            stb.append("         AND NML013.NAMECD2 = TREC.JUDGEDIV ");
            stb.append("     LEFT JOIN RECRUIT_DAT TRECR ON TRECR.YEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRECR.RECRUIT_NO = TADET002.REMARK1 ");
            stb.append("     LEFT JOIN PRISCHOOL_MST TPSM ON TPSM.PRISCHOOLCD = TRECR.PRISCHOOLCD ");
            stb.append("     LEFT JOIN PRISCHOOL_CLASS_MST TPSCM ON TPSCM.PRISCHOOLCD = TRECR.PRISCHOOLCD ");
            stb.append("         AND TPSCM.PRISCHOOL_CLASS_CD = TRECR.PRISCHOOL_CLASS_CD ");
            stb.append("     LEFT JOIN NAME_MST NML005 ON NML005.NAMECD1 = 'L005' ");
            stb.append("         AND NML005.NAMECD2 = TRDET011.REMARK" + param._testdiv + " ");
            stb.append("     LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025' ");
            stb.append("         AND NML025.NAMECD2 = TAPPL.JUDGE_KIND ");
            stb.append("     LEFT JOIN NAME_MST NML054 ON NML054.NAMECD1 = 'L054' ");
            stb.append("                              AND NML054.NAMECD2 = TADET015.REMARK5 ");
            stb.append("     LEFT JOIN NAME_MST NML056 ON NML056.NAMECD1 = 'L056' ");
            stb.append("                              AND NML056.NAMECD2 = TADET014.REMARK1 ");
            stb.append("     LEFT JOIN NAME_MST NML055 ON NML055.NAMECD1 = 'L055' ");
            stb.append("                              AND NML055.NAMECD2 = TADET014.REMARK9 ");
            stb.append("     LEFT JOIN ( ");
            stb.append("         SELECT ");
            stb.append("             R1.YEAR, ");
            stb.append("             R1.RECRUIT_NO, ");
            stb.append("             R1.STAFFCD ");
            stb.append("         FROM ");
            stb.append("             RECRUIT_CONSULT_DAT R1 ");
            stb.append("             INNER JOIN ( ");
            stb.append("                 SELECT ");
            stb.append("                     YEAR, ");
            stb.append("                     RECRUIT_NO, ");
            stb.append("                     MAX(TOUROKU_DATE) AS TOUROKU_DATE ");
            stb.append("                 FROM ");
            stb.append("                     RECRUIT_CONSULT_DAT ");
            stb.append("                 WHERE ");
            stb.append("                     YEAR = '" + param._entexamyear + "' ");
            stb.append("                 GROUP BY ");
            stb.append("                     YEAR, ");
            stb.append("                     RECRUIT_NO ");
            stb.append("             ) R2 ON R1.YEAR = R2.YEAR ");
            stb.append("                 AND R1.RECRUIT_NO = R2.RECRUIT_NO ");
            stb.append("                 AND R1.TOUROKU_DATE = R2.TOUROKU_DATE ");
            stb.append("     ) TRECRC ON TRECRC.YEAR = TREC.ENTEXAMYEAR ");
            stb.append("             AND TRECRC.RECRUIT_NO = TADET002.REMARK1 ");
            stb.append("     LEFT JOIN STAFF_MST TSTFM ON TSTFM.STAFFCD = TRECRC.STAFFCD ");
            stb.append("     LEFT JOIN RECRUIT_CONSULT_WRAPUP_DAT WRAPUP ON WRAPUP.YEAR = TREC.ENTEXAMYEAR ");
            stb.append("             AND WRAPUP.RECRUIT_NO = TADET002.REMARK1 ");
            stb.append(" WHERE ");
            stb.append("     TREC.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND TREC.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND TREC.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND TREC.EXAM_TYPE = '1' ");
            if ("5".equals(param._testdiv)) {
                if ("1".equals(param._testdiv5out)) {
                    // 第5回
                    stb.append("     AND VALUE(TAPPL.GENERAL_FLG, '') <> '1' ");
                } else if ("2".equals(param._testdiv5out)) {
                    // 特別入試
                    stb.append("     AND VALUE(TAPPL.GENERAL_FLG, '') = '1' ");
                } else if ("3".equals(param._testdiv5out)) {
                    // 第5回+特別入試
                }
            }
            stb.append(" ORDER BY ");
            if ("1".equals(param._sort)) {
                stb.append(" CASE WHEN VALUE(TREC.JUDGEDIV, '') = '4' THEN 2 ELSE 1 END,");
                if ("X".equals(param._examType)) {
                    stb.append(" VALUE(TREC.TOTAL2, -1) DESC,");
                } else {
                    stb.append(" VALUE(TREC.TOTAL4, -1) DESC,");
                }
            }
            stb.append(" TREC.RECEPTNO ");
            return stb.toString();
        }
    }

    private static class Subclass {
        final String _cd;
        final String _name;
        Subclass(final String cd, final String name) {
            _cd = cd;
            _name = name;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 70832 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv; // 9:コース別思考力入試
        final String _examType; // 1:2科 2:2科+選択 3:4科 9:インタラクティブ X:2科と2科+選択 6:グローバル 7:サイエンス 8:スポーツ
        final String _sort;
        final String _loginDate;
        final String _cmd;
        final String _testdiv5out;
        final String _applicantdivName;
        final String _testdivName1;
        final String _testdivAbbv1;
        final String _currentTime;
        final boolean _isCsv;
        final Map _nmL013Abbv2Map;
        final Map _nmL035Abbv2Map;
        final Map _nmL005Name1Map;
        final Map _nmL009Namespare1Map;
        final boolean _isIee;

        final List _subclassList = new ArrayList();
        final List _subclassnameList = new ArrayList();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _examType = request.getParameter("EXAM_TYPE");
            _sort = request.getParameter("SORT");
            _loginDate = request.getParameter("LOGIN_DATE");
            _cmd = request.getParameter("cmd");
            _testdiv5out = request.getParameter("TESTDIV5OUT");
            _applicantdivName = getApplicantdivName(db2);
            _testdivName1 = getTestdivNameMst(db2, "NAME1");
            _testdivAbbv1 = getTestdivNameMst(db2, "ABBV1");
            _currentTime = currentTime(db2);
            _isCsv = "csv".equals(_cmd);
            _nmL013Abbv2Map = getNameMstAbbv2Map(db2, "L013");
            _nmL035Abbv2Map = getNameMstAbbv2Map(db2, "L035");
            _nmL005Name1Map = getL005Name1Map(db2);
            _nmL009Namespare1Map = getL009Namespare1Map(db2);
            _isIee = IEE.equals(getAbbv2(db2, _testdiv)) ;

            if ("3".equals(_examType)) { // 4科
                _subclassList.add(new Subclass("TOTAL4", "4科合計")); // 科目名称
                _subclassList.add(new Subclass("TOTAL2", "2科合計")); // 科目名称
                _subclassList.add(new Subclass("1", "国語")); // 科目名称
                _subclassList.add(new Subclass("2", "算数")); // 科目名称
                _subclassList.add(new Subclass("3", "理科")); // 科目名称
                _subclassList.add(new Subclass("4", "社会")); // 科目名称
//                _subclassnameList.add(new Subclass("5", "英語"); // 科目名称
                final Subclass blank = new Subclass("", "");
                if (!_isCsv) {
                    _subclassList.add(blank); // 科目名称
                    _subclassList.add(blank); // 科目名称
                    _subclassList.add(blank); // 科目名称
                    _subclassList.add(blank); // 科目名称
                }
                if ("5".equals(_testdiv) && "1".equals(_testdiv5out)) {
                    if (!_isCsv) {
                        // 特待区分表示しない
                        _subclassList.add(blank); // 科目名称
                        _subclassList.add(blank); // 科目名称
                    }
                } else {
                    _subclassList.add(new Subclass("JUDGE_KIND_SCORE", "特待得点")); // 科目名称
                    _subclassList.add(new Subclass("JUDGE_KIND", "特待情報")); // 科目名称
                }
            } else if ("1".equals(_examType)) {
                _subclassList.add(new Subclass("TOTAL4", "合計")); // 科目名称
                _subclassList.add(new Subclass("1", "国語")); // 科目名称
                _subclassList.add(new Subclass("2", "算数")); // 科目名称
            } else if ("6".equals(_examType) || "7".equals(_examType) || "8".equals(_examType)) { // コース別思考力
                if ("6".equals(_examType)) { // グローバル
                    _subclassList.add(new Subclass("G", "思考")); // 科目名称
                } else if ("7".equals(_examType) || "8".equals(_examType)) { // サイエンス、スポーツ
                    _subclassList.add(new Subclass("H", "レポ")); // 科目名称
                }
            } else if ("A".equals(_examType)) {
                _subclassList.add(new Subclass("TOTAL4", "合計")); // 科目名称
                _subclassList.add(new Subclass("J", "適正Ⅰ")); // 科目名称
                _subclassList.add(new Subclass("K", "適正Ⅱ")); // 科目名称
            } else {
                if (_isIee) {
                    _subclassList.add(new Subclass("I", "インタラクティブ英語")); // 科目名称
                } else {
                    if ("X".equals(_examType)) {
                        _subclassList.add(new Subclass("TOTAL2", "2科合計")); // 科目名称
                    } else {
                        _subclassList.add(new Subclass("TOTAL4", "合計")); // 科目名称
                    }
                    _subclassList.add(new Subclass("1", "国語")); // 科目名称
                    _subclassList.add(new Subclass("2", "算数")); // 科目名称
                    _subclassList.add(new Subclass("MAX1+MAX2", "選択合計")); // 科目名称
                    _subclassList.add(new Subclass("A", "理科1")); // 科目名称
                    _subclassList.add(new Subclass("B", "理科2")); // 科目名称
                    _subclassList.add(new Subclass("C", "社会1")); // 科目名称
                    _subclassList.add(new Subclass("D", "社会2")); // 科目名称
                    _subclassList.add(new Subclass("E", "英語1")); // 科目名称
                    _subclassList.add(new Subclass("F", "英語2")); // 科目名称
                    _subclassList.add(new Subclass("MAX1", "最高1")); // 科目名称
                    _subclassList.add(new Subclass("MAX2", "最高2")); // 科目名称
                }
            }
            for (int i = 0; i < _subclassList.size(); i++) {
                final Subclass subclass = (Subclass) _subclassList.get(i);
                _subclassnameList.add(subclass._name);
            }
        }

        private String getApplicantdivName(final DB2UDB db2) {
            String applicantdivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantdiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  applicantdivName = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return applicantdivName;
        }

        private String getTestdivNameMst(final DB2UDB db2, final String field) {
            String testDivName = "";
            final String namecd1 = "1".equals(_applicantdiv) ? "L024" : "L004";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testdiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString(field)) {
                  testDivName = rs.getString(field);
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDivName;
        }

        private Map getNameMstAbbv2Map(final DB2UDB db2, final String namecd1) {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2, ABBV2 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                  rtn.put(rs.getString("NAMECD2"), rs.getString("ABBV2"));
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private Map getL005Name1Map(final DB2UDB db2) {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'L005' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                  rtn.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private Map getL009Namespare1Map(final DB2UDB db2) {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2, NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'L009' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                  rtn.put(rs.getString("NAMECD2"), rs.getString("NAMESPARE1"));
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getAbbv2(final DB2UDB db2, final String testdiv) {
            String retName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT ABBV2 FROM NAME_MST WHERE NAMECD1 = 'L024' AND NAMECD2 = '" + testdiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("ABBV2")) {
                  retName = rs.getString("ABBV2");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retName;
        }

        private static String currentTime(final DB2UDB db2) {
            final Calendar cal = Calendar.getInstance();
            final int year = cal.get(Calendar.YEAR);
            final int month = cal.get(Calendar.MONTH) + 1;
            final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            final String dow = String.valueOf(" 日月火水木金土".charAt(cal.get(Calendar.DAY_OF_WEEK)));
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int min = cal.get(Calendar.MINUTE);
            final DecimalFormat df = new DecimalFormat("00");
            return KNJ_EditDate.h_format_JP(db2, year + "-" + month + "-" + dayOfMonth) + "(" + dow + ") " + df.format(hour) + ":" + df.format(min);
        }
    }
}

// eof

