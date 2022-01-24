/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * 作成日: 2021/01/28
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD627C {

    private static final Log log = LogFactory.getLog(KNJD627C.class);

    private static final String csv = "csv";

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "90";

    private static final String SDIV9990008 = "9990008"; //学年末評価

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";

    private static final int AKATEN_BORDER = 49;

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

        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            if (csv.equals(_param._cmd)) {
                final String title = _param._nendo + "　再試験対象者一覧";
                final List<List<String>> outputLines = getCsvOutput(db2);

                final Map csvParam = new HashMap();
                csvParam.put("HttpServletRequest", request);

                final Map map = new HashMap();
                map.put("TITLE", title);
                map.put("OUTPUT_LINES", outputLines);

                CsvUtils.outputJson(log, request, response, CsvUtils.toJson(map), csvParam);
            } else {
                svf = new Vrw32alp();

                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                printMain(db2, svf);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != svf) {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
                svf.VrQuit();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List studentList = getList(db2);
        //欠課
        SubclassAttendance.load(db2, _param, studentList);

        //通知票
        printSvfMain(db2, svf, studentList);
    }

    private String add(final String num1, final String num2) {
        if (!NumberUtils.isDigits(num1)) { return num2; }
        if (!NumberUtils.isDigits(num2)) { return num1; }
        return String.valueOf(Integer.parseInt(num1) + Integer.parseInt(num2));
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final List studentList) {

        //印字する科目リスト
        final List outSubclassList = new ArrayList();
        final List outSubclassList2 = new ArrayList(); //欠課時数超過のみ

        getOutSubclassList(studentList, outSubclassList, outSubclassList2);

        if(outSubclassList.size() > 0 || outSubclassList2.size() > 0 ) {
            final String form = "KNJD627C.frm";
            String semestername = "";
            Map nameD039 = _param.getNameMst(db2, "D039");
            if(nameD039 != null) {
                Map map = (Map) nameD039.get(_param._semester);
                if(map != null) semestername = (String)map.get("NAME1"); //学期名称
            }
            String[] date = _param._date.split("-", 0); //出力日
            boolean outFlg = false;
            boolean outFlg2 = false;

            //再試験受験資格あり
            if(outSubclassList.size() > 0) {
                svf.VrSetForm(form , 4);
                svf.VrsOut("TITLE", semestername); //タイトル

                //出力日
                if (date != null && date.length == 3) {
                    date[1] = String.valueOf(Integer.parseInt(date[1]));
                    date[2] = String.valueOf(Integer.parseInt(date[2]));
                    svf.VrsOut("DATE", date[0] + "．" + date[1] + "．" + date[2]);
                }

                svf.VrsOut("HEADER", semestername + "再試験対象者一覧（科目別）"); //ヘッダ
                svf.VrEndRecord();

                //対象者一覧
                for (Iterator itSubclass = outSubclassList.iterator(); itSubclass.hasNext();) {
                    final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
                    final String subclassCd = subclassMst._subclasscd;
                    for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
                        final Student student = (Student) iterator.next();
                        if (student._printSubclassMap.containsKey(subclassCd)) {
                            final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);

                            //年組番氏名
                            svf.VrsOut("GRADE1", String.valueOf(Integer.parseInt(student._current_grade)));
                            svf.VrsOut("HR1", student._current_hrClassName1);
                            svf.VrsOut("ATTENDNO", student._current_attendNo);
                            svf.VrsOut("NAME1", student._name);
                            svf.VrsOut("KANA1", student._kana);

                            //科目名
                            final int length = KNJ_EditEdit.getMS932ByteLength(subclassMst._subclassname);
                            final String field = length > 26 ? "3" : length > 20 ? "2" : "1";
                            svf.VrsOut("SUBCLASS_NAME" + field, subclassMst._subclassname);

                            //単位数
                            svf.VrsOut("CREDIT1", scoreData._credits);

                            //得点
                            String score = "";
                            if(scoreData.slumpSubclass(SDIV9990008)) {
                                score = scoreData.slumpScore(SDIV9990008);
                            } else {
                                score = scoreData.score(SDIV9990008);
                            }
                            svf.VrsOut("SCORE1", score);
                            if(Integer.parseInt(score) <= AKATEN_BORDER) {
                                //学年末評価が49点以下の場合、点数欄を赤く表示
                                svf.VrAttribute("SCORE1", "Palette=9");
                            }

                            svf.VrEndRecord();
                            outFlg = true;
                        }
                    }
                }
                if(outFlg) {
                    svf.VrEndPage();
                }
            }

            //再試験受験資格なし(欠課時数超過のみ)
            if(outSubclassList2.size() > 0) {
                svf.VrSetForm(form, 4);
                svf.VrsOut("TITLE", semestername); //タイトル

                //出力日
                if (date != null && date.length == 3) {
                    date[1] = String.valueOf(Integer.parseInt(date[1]));
                    date[2] = String.valueOf(Integer.parseInt(date[2]));
                    svf.VrsOut("DATE", date[0] + "．" + date[1] + "．" + date[2]);
                }

                //修得下限(上限から逆算)
                final int bunbo = Integer.parseInt((String)_param._schoolInfoMap.get("SYUTOKU_BUNBO")); //修得上限（分母）
                final int bunsi = bunbo - Integer.parseInt((String)_param._schoolInfoMap.get("SYUTOKU_BUNSI")); //修得上限（分母） - 修得上限（分子）
                svf.VrsOut("HEADER", String.valueOf(bunsi) + "/" + String.valueOf(bunbo) + "該当再受験資格なし"); //ヘッダ
                svf.VrEndRecord();

                //対象者一覧
                for (Iterator itSubclass = outSubclassList2.iterator(); itSubclass.hasNext();) {
                    final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
                    final String subclassCd = subclassMst._subclasscd;
                    for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
                        final Student student = (Student) iterator.next();
                        if (student._printSubclassMap.containsKey(subclassCd)) {
                            final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);

                            //年組番氏名
                            svf.VrsOut("GRADE1", student._current_grade);
                            svf.VrsOut("HR1", student._current_hrClassName1);
                            svf.VrsOut("ATTENDNO", student._current_attendNo);
                            svf.VrsOut("NAME1", student._name);
                            svf.VrsOut("KANA1", student._kana);

                            //科目名
                            final int length = KNJ_EditEdit.getMS932ByteLength(subclassMst._subclassname);
                            final String field = length > 26 ? "3" : length > 20 ? "2" : "1";
                            svf.VrsOut("SUBCLASS_NAME" + field, subclassMst._subclassname);

                            //得点
                            svf.VrsOut("RESULT", scoreData.slumpScore(SDIV9990008));

                            //単位数
                            svf.VrsOut("CREDIT1", scoreData._credits);

                            //修得下限
                            svf.VrsOut("OVER1", String.valueOf(bunsi) + "/" + String.valueOf(bunbo));

                            svf.VrEndRecord();
                            outFlg2 = true;
                        }
                    }
                }
                if(outFlg2) {
                    svf.VrEndPage();
                }
            }

            if(outFlg || outFlg2) {
                _hasData = true;
            }
        }
    }

    private List<List<String>> getCsvOutput(final DB2UDB db2) {
        final List studentList = getList(db2);

        //欠課
        SubclassAttendance.load(db2, _param, studentList);

        //通知票
        return getCsvContents(db2, studentList);
    }

    private List<List<String>> getCsvContents(final DB2UDB db2, final List<Student> studentList) {
        final List<List<String>> csvLines = new ArrayList<List<String>>();

        //印字する科目リスト
        final List<SubclassMst> outSubclassList = new ArrayList();
        final List<SubclassMst> outSubclassList2 = new ArrayList(); //欠課時数超過のみ

        getOutSubclassList(studentList, outSubclassList, outSubclassList2);

        if(outSubclassList.size() > 0 || outSubclassList2.size() > 0 ) {
            String semestername = "";
            Map nameD039 = _param.getNameMst(db2, "D039");
            if(nameD039 != null) {
                Map map = (Map) nameD039.get(_param._semester);
                if(map != null) semestername = (String)map.get("NAME1"); //学期名称
            }
            String[] date = _param._date.split("-", 0); //出力日
            String outputDate = "";
            //出力日
            if (date != null && date.length == 3) {
                date[1] = String.valueOf(Integer.parseInt(date[1]));
                date[2] = String.valueOf(Integer.parseInt(date[2]));
                outputDate = (date[0] + "．" + date[1] + "．" + date[2]);
            }

            final List<String> itemHeader = Arrays.asList("年", "組", "番", "再履修年", "再履修組", "再履修番", "氏名", "フリガナ", "未履修科目", "点数", "単位数", "");

            //再試験受験資格あり
            if(outSubclassList.size() > 0) {

                CsvUtils.newLine(csvLines).add(semestername); //タイトル
                CsvUtils.newLine(csvLines).addAll(Arrays.asList("", outputDate)); //出力日

                CsvUtils.newLine(csvLines); // 空行
                CsvUtils.newLine(csvLines).add(semestername + "再試験対象者一覧（科目別）"); //ヘッダ

                CsvUtils.newLine(csvLines).addAll(itemHeader);

                //対象者一覧
                for (final SubclassMst subclassMst : outSubclassList) {
                    final String subclassCd = subclassMst._subclasscd;
                    for (final Student student : studentList) {
                        if (student._printSubclassMap.containsKey(subclassCd)) {
                            final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);

                            //年組番氏名
                            final List<String> studentLine = CsvUtils.newLine(csvLines);
                            studentLine.add(String.valueOf(Integer.parseInt(student._current_grade)));
                            studentLine.add(student._current_hrClassName1);
                            studentLine.add(student._current_attendNo);
                            studentLine.add(String.valueOf(Integer.parseInt(student._grade)));
                            studentLine.add(student._hrClassName1);
                            studentLine.add(student._attendno);
                            studentLine.add(student._name);
                            studentLine.add(student._kana);

                            //科目名
                            studentLine.add(subclassMst._subclassname);

                            //得点
                            String score = "";
                            if(scoreData.slumpSubclass(SDIV9990008)) {
                                score = scoreData.slumpScore(SDIV9990008);
                            } else {
                                score = scoreData.score(SDIV9990008);
                            }
                            studentLine.add(score);

                            //単位数
                            studentLine.add(scoreData._credits);
                        }
                    }
                }
            }

            //再試験受験資格なし(欠課時数超過のみ)
            if(outSubclassList2.size() > 0) {
                if (outSubclassList.size() > 0) {
                    CsvUtils.newLine(csvLines); // 空行
                    CsvUtils.newLine(csvLines); // 空行
                }
                CsvUtils.newLine(csvLines).add(semestername); //タイトル
                CsvUtils.newLine(csvLines).addAll(Arrays.asList("", outputDate)); //出力日

                //修得下限(上限から逆算)
                final int bunbo = Integer.parseInt((String)_param._schoolInfoMap.get("SYUTOKU_BUNBO")); //修得上限（分母）
                final int bunsi = bunbo - Integer.parseInt((String)_param._schoolInfoMap.get("SYUTOKU_BUNSI")); //修得上限（分母） - 修得上限（分子）
                CsvUtils.newLine(csvLines).add(String.valueOf(bunsi) + "/" + String.valueOf(bunbo) + "該当再受験資格なし"); //ヘッダ

                CsvUtils.newLine(csvLines).addAll(itemHeader);

                //対象者一覧
                for (final SubclassMst subclassMst : outSubclassList2) {
                    final String subclassCd = subclassMst._subclasscd;
                    for (final Student student : studentList) {
                        if (student._printSubclassMap.containsKey(subclassCd)) {
                            final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);

                            //年組番氏名
                            final List<String> studentLine = CsvUtils.newLine(csvLines);
                            studentLine.add(String.valueOf(Integer.parseInt(student._current_grade)));
                            studentLine.add(student._current_hrClassName1);
                            studentLine.add(student._current_attendNo);
                            studentLine.add(String.valueOf(Integer.parseInt(student._grade)));
                            studentLine.add(student._hrClassName1);
                            studentLine.add(student._attendno);
                            studentLine.add(student._name);
                            studentLine.add(student._kana);

                            //科目名
                            studentLine.add(subclassMst._subclassname);

                            //得点
                            studentLine.add(scoreData.slumpScore(SDIV9990008));

                            //単位数
                            studentLine.add(scoreData._credits);

                            //修得下限
                            studentLine.add(String.valueOf(bunsi) + "/" + String.valueOf(bunbo));
                        }
                    }
                }
            }
        }
        return csvLines;
    }

    private void getOutSubclassList(final List studentList, final List outSubclassList, final List outSubclassList2) {
        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        //印字する科目の設定
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclasscd;

            //対象科目が選択されている場合、対象科目以外は不要
            if(!"".equals(_param._subclasscd) && subclassCd.indexOf(_param._subclasscd) < 0) continue;

            for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
                final Student student = (Student) iterator.next();

                final boolean isPrint = student._printSubclassMap.containsKey(subclassCd);
                if (!isPrint) {
                    continue;
                }

                //欠課時数超過の科目
                if (student._attendSubClassMap.containsKey(subclassCd)) {
                    final SubclassAttendance attendance = student._attendSubClassMap.get(subclassCd);
                    if(attendance._isOver) {
                        outSubclassList2.add(subclassMst); //対象生徒が1人以上存在する科目
                        student._attendSubClassMap.remove(subclassCd);
                        break;
                    }
                }

                outSubclassList.add(subclassMst); //対象生徒が1人以上存在する科目
                break;
            }
        }
    }

    private List subclassListRemoveD026() {
        final List retList = new ArrayList(_param._subclassMstMap.values());
        for (final Iterator it = retList.iterator(); it.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) it.next();
            if (_param._d026List.contains(subclassMst._subclasscd)) {
                it.remove();
            }
            if (_param._isNoPrintMoto &&  subclassMst.isMoto() || !_param._isPrintSakiKamoku &&  subclassMst.isSaki()) {
                it.remove();
            }
        }
        return retList;
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final String[] value) {
        if (null != value) {
            for (int i = 0 ; i < value.length; i++) {
                svf.VrsOutn(field, i + 1, value[i]);
            }
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(" getStudentSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                student._year = rs.getString("YEAR");
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._kana = rs.getString("NAME_KANA");
                student._attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");
                student._grade = rs.getString("GRADE");
                student._hrClass = rs.getString("HR_CLASS");
                student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                student._current_year = rs.getString("CURRENT_YEAR");
                student._current_grade = rs.getString("CURRENT_GRADE");
                student._current_hrClass = rs.getString("CURRENT_HR_CLASS");
                student._current_attendNo = NumberUtils.isDigits(rs.getString("CURRENT_ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("CURRENT_ATTENDNO"))) : rs.getString("CURRENT_ATTENDNO");
                student._current_hrClassName1 = rs.getString("CURRENT_HR_CLASS_NAME1");

                student.setSubclass(db2);
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
        //選択された年組の生徒
        stb.append("WITH SCHNO_A AS( ");
        stb.append("    SELECT DISTINCT ");
        stb.append("            T1.SCHREGNO, ");
        stb.append("            T1.YEAR AS CURRENT_YEAR, ");
        stb.append("            T1.SEMESTER AS CURRENT_SEMESTER, ");
        stb.append("            T1.GRADE AS CURRENT_GRADE, ");
        stb.append("            T1.HR_CLASS AS CURRENT_HR_CLASS, ");
        stb.append("            T1.ATTENDNO AS CURRENT_ATTENDNO ");
        stb.append("    FROM    SCHREG_REGD_DAT T1 ");
        stb.append("            LEFT JOIN SCHREG_REGD_GDAT REGDG ");
        stb.append("                   ON REGDG.YEAR  = T1.YEAR ");
        stb.append("                  AND REGDG.GRADE = T1.GRADE ");
        stb.append("            LEFT JOIN SCHREG_REGD_HDAT REGDH ");
        stb.append("                   ON REGDH.YEAR     = T1.YEAR ");
        stb.append("                  AND REGDH.SEMESTER = T1.SEMESTER ");
        stb.append("                  AND REGDH.GRADE    = T1.GRADE ");
        stb.append("                  AND REGDH.HR_CLASS = T1.HR_CLASS ");
        stb.append("    WHERE   T1.YEAR     = '" + _param._loginYear     + "' ");
        stb.append("        AND T1.SEMESTER = '" + _param._loginSemester + "' ");
        stb.append("        AND T1.GRADE    = '" + _param._grade         + "' ");
        stb.append("        AND T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        //対象生徒の過年度
        stb.append(" ) , SCHNO_HIST_A AS( ");
        stb.append("    SELECT ");
        stb.append("            REGD.YEAR, ");
        stb.append("            MAX(REGD.SEMESTER) AS MAX_SEMESTER, ");
        stb.append("            REGD.GRADE, ");
        stb.append("            REGD.HR_CLASS, ");
        stb.append("            REGD.ATTENDNO, ");
        stb.append("            REGD.SCHREGNO, ");
        stb.append("            T2.CURRENT_YEAR, ");
        stb.append("            T2.CURRENT_SEMESTER, ");
        stb.append("            T2.CURRENT_GRADE, ");
        stb.append("            T2.CURRENT_HR_CLASS, ");
        stb.append("            T2.CURRENT_ATTENDNO, ");
        stb.append("            REGDG.GRADE_CD ");
        stb.append("    FROM    SCHREG_REGD_DAT REGD ");
        stb.append("            INNER JOIN SCHNO_A T2 ");
        stb.append("                    ON T2.SCHREGNO = REGD.SCHREGNO ");
        stb.append("            INNER JOIN SCHREG_REGD_GDAT REGDG ");
        stb.append("                   ON REGDG.YEAR  = REGD.YEAR ");
        stb.append("                  AND REGDG.GRADE = REGD.GRADE ");
        stb.append("    WHERE   REGD.YEAR     < '" + _param._loginYear   + "' ");
        stb.append("      AND   REGD.GRADE    = '" + _param._targetGrade + "' ");
        stb.append("    GROUP BY ");
        stb.append("            REGD.YEAR, ");
        stb.append("            REGD.GRADE, ");
        stb.append("            REGD.HR_CLASS, ");
        stb.append("            REGD.ATTENDNO, ");
        stb.append("            REGD.SCHREGNO, ");
        stb.append("            T2.CURRENT_YEAR, ");
        stb.append("            T2.CURRENT_SEMESTER, ");
        stb.append("            T2.CURRENT_GRADE, ");
        stb.append("            T2.CURRENT_HR_CLASS, ");
        stb.append("            T2.CURRENT_ATTENDNO, ");
        stb.append("            REGDG.GRADE_CD ");
        stb.append(" ) , SCHNO_HIST AS( ");
        stb.append("    SELECT DISTINCT ");
        stb.append("            T1.* ");
        stb.append("    FROM    SCHNO_HIST_A T1 ");
        stb.append("    WHERE   T1.YEAR = (SELECT MAX(YEAR) FROM SCHNO_HIST_A T2 WHERE T2.GRADE_CD = T1.GRADE_CD AND T2.SCHREGNO = T1.SCHREGNO) ");
        //再試験結果が存在する生徒
        stb.append(" ) , SCHNO AS(");
        stb.append("    SELECT DISTINCT ");
        stb.append("            T1.* ");
        stb.append("    FROM    SCHNO_HIST T1 ");
        stb.append("            INNER JOIN RECORD_SCORE_DAT T2 ");
        stb.append("                    ON T2.YEAR       = T1.YEAR ");
        stb.append("                   AND T2.SEMESTER   = '" + SDIV9990008.substring(0, 1) + "' ");
        stb.append("                   AND T2.TESTKINDCD = '" + SDIV9990008.substring(1, 3) + "' ");
        stb.append("                   AND T2.TESTITEMCD = '" + SDIV9990008.substring(3, 5) + "' ");
        stb.append("                   AND T2.SCORE_DIV  = '" + SDIV9990008.substring(5) + "' ");
        stb.append("                   AND T2.SCHREGNO   = T1.SCHREGNO ");
        stb.append("                   AND T2.SCORE     <= '" + _param._borderScore + "' ");
        stb.append("                   AND T2.CLASSCD   <= '" + SELECT_CLASSCD_UNDER +"' ");
        if(!"".equals(_param._subclasscd)) {
            stb.append("               AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '" + _param._subclasscd + "' ");
        }
        stb.append("            INNER JOIN CLASS_MST T3 ");
        stb.append("                    ON T3.CLASSCD       = T2.CLASSCD ");
        stb.append("                   AND T3.SCHOOL_KIND   = T2.SCHOOL_KIND ");
        stb.append(" ) ");
        //メイン表
        stb.append("     SELECT  REGD.YEAR ");
        stb.append("            ,REGD.SCHREGNO ");
        stb.append("            ,BASE.NAME ");
        stb.append("            ,BASE.NAME_KANA ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,REGD.GRADE ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("            ,HDAT.HR_CLASS_NAME1 AS HR_CLASS_NAME1 ");
        stb.append("            ,REGD.CURRENT_YEAR ");
        stb.append("            ,REGD.CURRENT_GRADE ");
        stb.append("            ,REGD.CURRENT_HR_CLASS ");
        stb.append("            ,REGD.CURRENT_ATTENDNO ");
        stb.append("            ,HDAT_C.HR_CLASS_NAME1 AS CURRENT_HR_CLASS_NAME1 ");
        stb.append("     FROM    SCHNO REGD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ");
        stb.append("                   ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT_C ");
        stb.append("                   ON HDAT_C.YEAR     = REGD.CURRENT_YEAR ");
        stb.append("                  AND HDAT_C.SEMESTER = REGD.CURRENT_SEMESTER ");
        stb.append("                  AND HDAT_C.GRADE    = REGD.CURRENT_GRADE ");
        stb.append("                  AND HDAT_C.HR_CLASS = REGD.CURRENT_HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ");
        stb.append("                   ON HDAT.YEAR     = REGD.YEAR ");
        stb.append("                  AND HDAT.SEMESTER = REGD.MAX_SEMESTER ");
        stb.append("                  AND HDAT.GRADE    = REGD.GRADE ");
        stb.append("                  AND HDAT.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     ORDER BY ");
        stb.append("         REGD.CURRENT_GRADE, ");
        stb.append("         REGD.CURRENT_HR_CLASS, ");
        stb.append("         REGD.CURRENT_ATTENDNO ");

        return stb.toString();
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private class Student {
        String _year;
        String _schregno;
        String _name;
        String _kana;
        String _attendno;
        String _grade;
        String _hrClass;
        String _hrClassName1;
        String _current_year;
        String _current_grade;
        String _current_hrClass;
        String _current_attendNo;
        String _current_hrClassName1;
        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map<String, SubclassAttendance> _attendSubClassMap = new HashMap();

        private void setSubclass(final DB2UDB db2) {
            final String scoreSql = prestatementSubclass();
            log.debug(" scoreSql = " + scoreSql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String credits = rs.getString("CREDITS");

                    String ySubclass = rs.getString("SUBCLASSCD");
                    if (!"999999".equals(ySubclass)) {
                        ySubclass = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    }
                    ySubclass = rs.getString("YEAR") + "-" + ySubclass;

                    final String key = ySubclass;
                    if (!_printSubclassMap.containsKey(key)) {
                        _printSubclassMap.put(key, new ScoreData(classcd, classname, ySubclass, subclassname, credits));
                    }
                    if (null == rs.getString("SEMESTER")) {
                        continue;
                    }

                    final ScoreData scoreData = (ScoreData) _printSubclassMap.get(key);
                    final String testcd = rs.getString("SEMESTER") + rs.getString("TESTKINDCD") + rs.getString("TESTITEMCD") + rs.getString("SCORE_DIV");

                    final int score = Integer.parseInt(StringUtils.defaultString(rs.getString("SCORE"),"0")); //追試前
                    final int slumpScore = Integer.parseInt(StringUtils.defaultString(rs.getString("SLUMP_SCORE"),"0")); //追試1回目
                    scoreData._scoreMap.put(testcd, String.valueOf(score)); //追試前
                    scoreData._slumpScoreMap.put(testcd, String.valueOf(slumpScore)); //追試前・追試を含めた最高点

                    final boolean slumpSubclass = !"".equals(StringUtils.defaultString(rs.getString("SLUMP_SUBCLASSCD")));
                    scoreData._slumpSubclassMap.put(testcd, slumpSubclass); //追試を受けた科目
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private String prestatementSubclass() {
            final StringBuffer stb = new StringBuffer();

            final String[] sdivs = {SDIV9990008};
            final StringBuffer divStr = divStr("T1.", sdivs);

            stb.append(" WITH SCHNO AS( ");
            //学籍の表
            stb.append(" SELECT ");
            stb.append("     T2.* ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T2 ");
            stb.append(" WHERE ");
            stb.append("         T2.YEAR     = '" + _year     + "'  ");
            stb.append("     AND T2.GRADE    = '" + _grade    + "'  ");
            stb.append("     AND T2.HR_CLASS = '" + _hrClass  + "'  ");
            stb.append("     AND T2.SCHREGNO = '" + _schregno + "'  ");
            stb.append("     AND T2.SEMESTER = (SELECT ");
            stb.append("                          MAX(SEMESTER) ");
            stb.append("                        FROM ");
            stb.append("                          SCHREG_REGD_DAT W2 ");
            stb.append("                        WHERE ");
            stb.append("                          W2.YEAR = '" + _year + "'  ");
            stb.append("                          AND W2.SCHREGNO = T2.SCHREGNO ");
            stb.append("                     ) ");
            //成績明細データの表
            stb.append(" ) ,RECORD00 AS( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("     FROM RECORD_SCORE_DAT T1 ");
            stb.append("     INNER JOIN SCHNO T2 ");
            stb.append("             ON T2.YEAR     = T1.YEAR ");
            stb.append("            AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("         (" + divStr + ") ");
            stb.append("         AND T1.CLASSCD    <= '" + SELECT_CLASSCD_UNDER + "' ");
            stb.append("         AND T1.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");
            stb.append("         AND T1.SCORE      <= '" + _param._borderScore + "' "); //基準点以下の科目のみ取得
            if(!"".equals(_param._subclasscd)) {
                stb.append("         AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + _param._subclasscd + "' ");
            }
            stb.append(" ) ,RECORD0 AS( ");
            stb.append("     SELECT ");
            stb.append("              T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("            , L1.CREDITS ");
            stb.append("            , T3.SCORE ");
            stb.append("            , RSSD.SCORE AS SLUMP_SCORE ");
            stb.append("            , RSSD.SEMESTER || RSSD.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV AS SLUMP_SUBCLASSCD ");
            stb.append("     FROM RECORD00 T1 ");
            stb.append("     INNER JOIN SCHNO T2 ");
            stb.append("             ON T2.YEAR     = T1.YEAR ");
            stb.append("            AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN CREDIT_MST L1 ");
            stb.append("            ON L1.YEAR          = T2.YEAR ");
            stb.append("           AND L1.COURSECD      = T2.COURSECD ");
            stb.append("           AND L1.MAJORCD       = T2.MAJORCD ");
            stb.append("           AND L1.COURSECODE    = T2.COURSECODE ");
            stb.append("           AND L1.GRADE         = T2.GRADE ");
            stb.append("           AND L1.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND L1.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("     INNER JOIN RECORD_SCORE_DAT T3 ");
            stb.append("            ON T3.YEAR          = T1.YEAR ");
            stb.append("           AND T3.SEMESTER      = T1.SEMESTER ");
            stb.append("           AND T3.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("           AND T3.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("           AND T3.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("           AND T3.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND T3.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND T3.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_SLUMP_SDIV_DAT RSSD ");
            stb.append("            ON RSSD.YEAR          = T1.YEAR ");
            stb.append("           AND RSSD.SEMESTER      = T1.SEMESTER ");
            stb.append("           AND RSSD.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("           AND RSSD.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("           AND RSSD.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("           AND RSSD.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND RSSD.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND RSSD.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND RSSD.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND RSSD.SCHREGNO      = T1.SCHREGNO ");
            stb.append("           AND RSSD.SCORE        <= '" + _param._borderScore + "' "); //基準点以下の科目のみ取得
            stb.append(" ) ,RECORD AS( ");
            stb.append("     SELECT ");
            stb.append("       T3.CLASSNAME, ");
            stb.append("       T4.SUBCLASSNAME, ");
            stb.append("       T1.* ");
            stb.append("     FROM RECORD0 T1 ");
            stb.append("     INNER JOIN CLASS_MST T3 ");
            stb.append("        ON T3.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("     INNER JOIN SUBCLASS_MST T4 ");
            stb.append("        ON T4.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("       AND T4.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     INNER JOIN SUBCLASS_YDAT SUBY ");
            stb.append("        ON SUBY.YEAR          = T1.YEAR ");
            stb.append("       AND SUBY.SUBCLASSCD    = T4.SUBCLASSCD ");
            stb.append("       AND SUBY.CLASSCD       = T4.CLASSCD ");
            stb.append("       AND SUBY.SCHOOL_KIND   = T4.SCHOOL_KIND ");
            stb.append("       AND SUBY.CURRICULUM_CD = T4.CURRICULUM_CD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM RECORD T1 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD,  ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.TESTKINDCD, ");
            stb.append("     T1.TESTITEMCD, ");
            stb.append("     T1.SCORE_DIV  ");

            return stb.toString();
        }

        /**
         * 学期+テスト種別のWHERE句を作成
         * @param tab テーブル別名
         * @param sdivs 学期+テスト種別
         * @return 作成した文字列
         */
        private StringBuffer divStr(final String tab, final String[] sdivs) {
            final StringBuffer divStr = new StringBuffer();
            divStr.append(" ( ");
            String or = "";
            for (int i = 0; i < sdivs.length; i++) {
                final String semester = sdivs[i].substring(0, 1);
                final String testkindcd = sdivs[i].substring(1, 3);
                final String testitemcd = sdivs[i].substring(3, 5);
                final String scorediv = sdivs[i].substring(5);
                divStr.append(or).append(" " + tab + "SEMESTER = '" + semester + "' AND " + tab + "TESTKINDCD = '" + testkindcd + "' AND " + tab + "TESTITEMCD = '" + testitemcd + "' AND " + tab + "SCORE_DIV = '" + scorediv + "' ");
                or = " OR ";
            }
            divStr.append(" ) ");
            return divStr;
        }

        /**
         * 年度の開始日を取得する
         */
        private Semester loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Semester rtnSeme = null;
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR         = '" + _year    + "'"
                        + "   AND SEMESTER = '" + SEMEALL + "'"
                        + "   AND GRADE    = '" + _grade   + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnSeme = new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE"));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtnSeme;
        }
    }

    private static class ScoreData {
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final String _credits;
        final Map _scoreMap = new HashMap(); // 得点
        final Map _slumpScoreMap = new HashMap(); // 追試得点
        final Map _slumpSubclassMap = new HashMap(); // 追試を受けた科目

        private ScoreData(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String subclassname,
                final String credits
        ) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _credits = credits;
        }

        public String score(final String sdiv) {
            return StringUtils.defaultString((String) _scoreMap.get(sdiv), "");
        }

        public String slumpScore(final String sdiv) {
           return StringUtils.defaultString((String) _slumpScoreMap.get(sdiv), "");
        }

        public Boolean slumpSubclass(final String sdiv) {
            return (Boolean) _slumpSubclassMap.get(sdiv);
         }

        public String toString() {
            return "ScoreData(" + _subclasscd + ":" + _subclassname + ", scoreMap = " + _scoreMap + ")";
        }
    }

    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _attend;
        final BigDecimal _sick;
        final BigDecimal _late;
        final BigDecimal _early;
        boolean _isOver;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick, final BigDecimal late, final BigDecimal early) {
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
            _late = late;
            _early = early;
        }

        public String toString() {
            return "SubclassAttendance(" + _sick == null ? null : sishaGonyu(_sick.toString())  + "/" + _lesson + ")";
        }

        private static void load(final DB2UDB db2,
                final Param param,
                final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            for (final Iterator it3 = studentList.iterator(); it3.hasNext();) {
                final Student student = (Student) it3.next();
                Semester seme = student.loadSemester(db2);

                try {
                    param._attendParamMap.put("schregno", "?");

                    final String sql = AttendAccumulate.getAttendSubclassSql(
                                student._year,
                                seme._semester,
                                seme._sdate,
                                seme._edate,
                                param._attendParamMap
                            );

                    ps = db2.prepareStatement(sql);

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                            continue;
                        }
                        final String subclasscd = student._year + "-" + rs.getString("SUBCLASSCD");

                        final SubclassMst mst = (SubclassMst) param._subclassMstMap.get(subclasscd);
                        if (null == mst) {
                            log.warn("no subclass : " + subclasscd);
                            continue;
                        }
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if (Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && rs.getBigDecimal("MLESSON").intValue() > 0) {

                            final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                            final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                            final BigDecimal sick = rs.getBigDecimal("SICK2");
                            final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                            final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");
                            final BigDecimal late = rs.getBigDecimal("LATE");
                            final BigDecimal early = rs.getBigDecimal("EARLY");

                            final BigDecimal sick1 = mst.isSaki() ? rawReplacedSick : rawSick;
                            final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                            final BigDecimal sick2 = mst.isSaki() ? replacedSick : sick;

                            final BigDecimal absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");

                            final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2, late, early);

                            //欠課時数上限
                            final Double absent = Double.valueOf(mst.isSaki() ? rs.getString("REPLACED_SICK"): rs.getString("SICK2"));
                            subclassAttendance._isOver = subclassAttendance.judgeOver(absent, absenceHigh);

                            student._attendSubClassMap.put(subclasscd, subclassAttendance);
                        }

                    }

                    DbUtils.closeQuietly(rs);
                } catch (Exception e) {
                    log.fatal("exception!", e);
                } finally {
                    DbUtils.closeQuietly(ps);
                    db2.commit();
                }
            }
        }

        /**
         * 欠課時数超過ならTrueを戻します。
         * @param absent 欠課時数
         * @param absenceHigh 超過対象欠課時数（CREDIT_MST）
         * @return
         */
        private boolean judgeOver(final Double absent, final BigDecimal absenceHigh) {
            if (null == absent || null == absenceHigh) {
                return false;
            }
            if (0.1 > absent.floatValue() || 0.0 == absenceHigh.doubleValue()) {
                return false;
            }
            if (absenceHigh.doubleValue() < absent.doubleValue()) {
                return true;
            }
            return false;
        }
    }


    private static class Semester {
        final String _semester;
        final String _semestername;
        final String _sdate;
        final String _edate;

        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _sdate = sdate;
            _edate = edate;
        }
    }

    private static class SubclassMst implements Comparable<SubclassMst> {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final String _calculateCreditFlg;
        SubclassMst _combined = null;
        List<SubclassMst> _attendSubclassList = new ArrayList();
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public boolean isMoto() {
            return null != _combined;
        }
        public boolean isSaki() {
            return !_attendSubclassList.isEmpty();
        }
        public int compareTo(final SubclassMst mst) {
            int rtn;
            rtn = _classShoworder3.compareTo(mst._classShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _classcd && null == mst._classcd) {
                return 0;
            } else if (null == _classcd) {
                return 1;
            } else if (null == mst._classcd) {
                return -1;
            }
            rtn = _classcd.compareTo(mst._classcd);
            if (0 != rtn) { return rtn; }
            rtn = _subclassShoworder3.compareTo(mst._subclassShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _subclasscd && null == mst._subclasscd) {
                return 0;
            } else if (null == _subclasscd) {
                return 1;
            } else if (null == mst._subclasscd) {
                return -1;
            }
            return _subclasscd.compareTo(mst._subclasscd);
        }
        public String toString() {
            return "SubclassMst(subclasscd = " + _subclasscd + ")";
        }
    }


    private static class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _cmd;
        final String[] _categorySelected;
        final String _grade;
        final String _targetGrade;
        final String _semester;
        final String _borderScore;
        final String _subclasscd;

        final String _loginSemester;
        final String _loginYear;
        final String _date;
        final String _nendo;
        final String _schoolKind;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private Map<String, SubclassMst> _subclassMstMap;
        private List _d026List = new ArrayList();

        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;

        private Map _schoolInfoMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {

            _cmd = request.getParameter("cmd");
            _categorySelected = csv.equals(_cmd) ? StringUtils.split(request.getParameter("CLASS_SELECTED"), ",") : request.getParameterValues("CLASS_SELECTED");
            _grade = request.getParameter("GRADE"); //現在の学年
            _targetGrade = request.getParameter("TARGET_GRADE"); //再試験対象年度
            _semester = request.getParameter("TERM"); //対象学期
            _borderScore = request.getParameter("BORDER_SCORE");
            _subclasscd = request.getParameter("TARGET_SUBJECT");

            _loginSemester = request.getParameter("HID_SEMESTER");
            _loginYear = request.getParameter("HID_YEAR");
            _date = request.getParameter("LOGIN_DATE").replace('/', '-');

            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_loginYear)) + "年度";
            loadNameMstD026(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);
            _schoolKind = getSchoolKind(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _targetGrade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            setSubclassMst(db2);

            _schoolInfoMap = getSchoolInfo(db2);
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD187N' AND NAME = '" + propName + "' "));
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += " T1.CLASSCD, ";
                sql += " YDAT.YEAR || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS Y_SUBCLASSCD, ";
                sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " COMB1.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3, ";
                sql += " ATT1.COMBINED_CLASSCD || '-' || ATT1.COMBINED_SCHOOL_KIND || '-' || ATT1.COMBINED_CURRICULUM_CD || '-' || ATT1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
                sql += " COMB1.ATTEND_CLASSCD || '-' || COMB1.ATTEND_SCHOOL_KIND || '-' || COMB1.ATTEND_CURRICULUM_CD || '-' || COMB1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " INNER JOIN SUBCLASS_YDAT YDAT ";
                sql += "         ON YDAT.YEAR          <= '" + _loginYear + "' ";
                sql += "        AND YDAT.CLASSCD       = T1.CLASSCD ";
                sql += "        AND YDAT.SCHOOL_KIND   = T1.SCHOOL_KIND ";
                sql += "        AND YDAT.CURRICULUM_CD = T1.CURRICULUM_CD ";
                sql += "        AND YDAT.SUBCLASSCD    = T1.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT ATT1 ON ATT1.YEAR = YDAT.YEAR AND ATT1.ATTEND_CLASSCD = T1.CLASSCD AND ATT1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ATT1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ATT1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
                sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT COMB1 ON COMB1.YEAR = YDAT.YEAR AND COMB1.COMBINED_CLASSCD = T1.CLASSCD AND COMB1.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND AND COMB1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD AND COMB1.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!_subclassMstMap.containsKey(rs.getString("Y_SUBCLASSCD"))) {
                        final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("Y_SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), rs.getString("CALCULATE_CREDIT_FLG"));
                        _subclassMstMap.put(rs.getString("Y_SUBCLASSCD"), mst);
                    }
                    final SubclassMst combined = _subclassMstMap.get(rs.getString("COMBINED_SUBCLASSCD"));
                    if (null != combined) {
                        final SubclassMst mst = _subclassMstMap.get(rs.getString("Y_SUBCLASSCD"));
                        mst._combined = combined;
                    }
                    final SubclassMst attend = _subclassMstMap.get(rs.getString("ATTEND_SUBCLASSCD"));
                    if (null != attend) {
                        final SubclassMst mst = _subclassMstMap.get(rs.getString("Y_SUBCLASSCD"));
                        mst._attendSubclassList.add(attend);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }


        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _loginYear + "' AND GRADE = '" + _grade + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private Map getStampNoMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     STAFFCD, ");
            stb.append("     MAX(STAMP_NO) AS STAMP_NO ");
            stb.append(" FROM ");
            stb.append("     ATTEST_INKAN_DAT ");
            stb.append(" GROUP BY ");
            stb.append("     STAFFCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map retMap = new HashMap();
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String staffcd = rs.getString("STAFFCD");
                    final String stampNo = rs.getString("STAMP_NO");
                    retMap.put(staffcd, stampNo);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _loginYear + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        /**
         * 合併先科目を印刷するか
         */
        private void setPrintSakiKamoku(final DB2UDB db2) {
            // 初期値：印刷する
            _isPrintSakiKamoku = true;
            // 名称マスタ「D021」「01」から取得する
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR='" + _loginYear+ "' AND NAMECD1 = 'D021' AND NAMECD2 = '01' "));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE3"))) {
                _isPrintSakiKamoku = false;
            }
            log.debug("合併先科目を印刷するか：" + _isPrintSakiKamoku);
        }

        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _loginYear + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");

            _d026List.clear();
            _d026List.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD"));
            log.info("非表示科目:" + _d026List);
        }

        private Map getNameMst(final DB2UDB db2, final String namecd1) {
            Map rtnMap = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT * ");
            stb.append("   FROM NAME_MST ");
            stb.append("  WHERE NAMECD1 = '"+ namecd1 +"' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map map = new HashMap();
                    map.put("NAME1", StringUtils.defaultString(rs.getString("NAME1")));
                    map.put("ABBV1", StringUtils.defaultString(rs.getString("ABBV1")));
                    map.put("NAMESPARE1", StringUtils.defaultString(rs.getString("NAMESPARE1")));
                    map.put("NAMESPARE2", StringUtils.defaultString(rs.getString("NAMESPARE2")));
                    map.put("NAMESPARE3", StringUtils.defaultString(rs.getString("NAMESPARE3")));

                    final String key = StringUtils.defaultString(rs.getString("NAMECD2"));
                    rtnMap.put(key, map);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtnMap;
        }

        private Map getSchoolInfo(final DB2UDB db2) {
            Map rtnMap = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   VALUE(L1.SCHOOL_REMARK4, 0) AS SYUTOKU_BUNSI, ");
            stb.append("   VALUE(L1.SCHOOL_REMARK5, 0) AS SYUTOKU_BUNBO ");
            stb.append(" FROM ");
            stb.append("   SCHOOL_MST T1 ");
            stb.append("   LEFT JOIN SCHOOL_DETAIL_DAT L1 ");
            stb.append("          ON T1.YEAR        = L1.YEAR ");
            stb.append("         AND L1.SCHOOL_SEQ  = '001' ");
            stb.append("         AND L1.SCHOOLCD    = T1.SCHOOLCD ");
            stb.append("         AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append(" WHERE T1.YEAR        = '"+ _loginYear +"' ");
            stb.append("   AND T1.SCHOOL_KIND = '"+ _schoolKind +"' ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnMap.put("SYUTOKU_BUNSI", StringUtils.defaultString(rs.getString("SYUTOKU_BUNSI")));
                    rtnMap.put("SYUTOKU_BUNBO", StringUtils.defaultString(rs.getString("SYUTOKU_BUNBO")));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtnMap;
        }
    }
}

// eof
