// kanji=漢字
/*
 * $Id$
 *
 */
package servletpack.KNJC;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
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
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.StaffInfo;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [出欠管理] 出欠統計処理
 */

public class KNJC163 {

    private static final Log log = LogFactory.getLog(KNJC163.class);

    private static final String cmd_csv = "csv";
    private static final String cmd_csvContents = "csvContents";

    private Param _param;

    private boolean _hasdata;

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        log.info("$Revision: 71501 $ $Date: 2019-12-26 22:21:39 +0900 (木, 26 12 2019) $ ");
        KNJServletUtils.debugParam(request, log);

        // ＤＢ接続
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("db2 instancing exception! ", ex);
            return;
        }

        Vrw32alp svf = null;

        try {
            _param = new Param(request, db2);
            _hasdata = false;
            if (!Arrays.asList(cmd_csv, cmd_csvContents).contains(_param._cmd)) {
                response.setContentType("application/pdf");
                svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
                svf.VrInit();                             //クラスの初期化
                svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
                printMain(null, svf, db2);
            } else {
                final LinkedList<List<String>> csvLines = new LinkedList<List<String>>();
                printMain(csvLines, null, db2);

                final Map parameterMap = new HashMap();
                parameterMap.put("HttpServletRequest", request);
                parameterMap.put("DB2UDB", db2);
                if (cmd_csvContents.equals(_param._cmd)) {
                    // csvContents
                    final Map map = new HashMap();
                    map.put("OUTPUT_LINES", csvLines);
                    map.put("TITLE", "出欠統計処理");
                    CsvUtils.outputJson(log, request, response, CsvUtils.toJson(map), parameterMap);
                } else {
                    // csv
                    CsvUtils.outputLines(log, response, "出欠統計処理.csv", csvLines, parameterMap);
                }
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            if (null != _param) {
                DbUtils.closeQuietly(_param._psAttendance);
            }
            // 終了処理
            if (null != svf) {
                if (!_hasdata) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note" , "note");
                    svf.VrEndPage();
                }
                svf.VrQuit();
            }
        }
    }

    private void printMain(final LinkedList<List<String>> csvLines, final Vrw32alp svf, final DB2UDB db2) {
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";
        final String loginDateFormat = KNJ_EditDate.h_format_JP(db2, _param._loginDate);
        final String sdateFormat = KNJ_EditDate.h_format_JP(db2, _param._sdate);
        final String edateFormat = KNJ_EditDate.h_format_JP(db2, _param._edate);

        final String title = "出欠統計処理";

        String subtitle = null;
        if ("1".equals(_param._output)) {
            subtitle = "（欠席日数　" + _param._kessekiNissu + "日以上）";
        } else if ("2".equals(_param._output)) {
            String tyuiTyoukaStr = "";
            if ("1".equals(_param._tyuiTyouka)) {
                tyuiTyoukaStr = "注意";
            } else if ("2".equals(_param._tyuiTyouka)) {
                tyuiTyoukaStr = "超過";
            }
            subtitle = "（指定した割合　" + tyuiTyoukaStr + "（" + _param._bunshi + " / " + _param._bunbo + "））";
        }

        final int MAX_LINE = 50;
        int page = 0;
        for (int i = 0; i < _param._gradeHrclass.length; i++) {
            final List<Student> outputList = getOutputList(db2, _param._gradeHrclass[i]);
            if (outputList.size() != 0) {
                page += 1;

                if (null != svf) {
                    svf.VrSetForm("KNJC163.frm", 4);
                } else if (null != csvLines) {
                    CsvUtils.newLine(csvLines);
                    CsvUtils.newLine(csvLines).addAll(Arrays.asList(outputList.get(0)._hrName));
                    CsvUtils.newLine(csvLines).addAll(Arrays.asList("出席番号", "氏名", "欠席", "遅刻", "早退", "出停・忌引等", "公欠"));
                }

                int line = 0;
                for (final Student student : outputList) {
                    final String attendno = NumberUtils.isDigits(student._attendNo) ? String.valueOf(Integer.parseInt(student._attendNo)) : student._attendNo;
                    String showName = null;
                    try {
                        showName = _param._staffInfo.getStrEngOrJp(student._name, student._nameEng);
                    } catch (Throwable t) {
                    }
                    final int suspendMourning = student._shuttei + student._kibiki + ("true".equals(_param._useVirus) ? student._virus : 0);

                    if (null != svf) {
                        line += 1;
                        if (line > MAX_LINE) {
                            page += 1;
                            line -= MAX_LINE;
                        }

                        svf.VrsOut("NENDO", nendo + " " + title);
                        svf.VrsOut("SUBTITLE", subtitle); // サブタイトル
                        svf.VrsOut("DATE", loginDateFormat); // 印刷日
                        svf.VrsOut("PERIOD", sdateFormat + " ～ " + edateFormat); // 期間

                        svf.VrsOut("PAGE", String.valueOf(page)); // ページ
                        svf.VrsOut("HR_NAME", student._hrName); // 年組
                        svf.VrsOut("ATTEND_NO", attendno); // 出席番号

                        if ("1".equals(_param._use_SchregNo_hyoji)) {
                            svf.VrsOut("SCHREGNO", student._schregno); // 学籍番号
                            svf.VrsOut("NAME2", showName); // 名前
                        } else  {
                            svf.VrsOut("NAME", showName); // 名前
                        }
                        svf.VrsOut("GRPCD", String.valueOf(line)); // グループサプレス

                        svf.VrsOut("ABSENT", String.valueOf(student._kesseki)); // 欠席
                        svf.VrsOut("LATE", String.valueOf(student._late)); // 遅刻
                        svf.VrsOut("EARLY", String.valueOf(student._early)); // 早退
                        svf.VrsOut("SUS_MOURN", String.valueOf(suspendMourning)); // 出停・忌引等
                        svf.VrsOut("AUTH_AB", String.valueOf(student._kouketsu)); // 公欠
                        svf.VrEndRecord();
                        _hasdata = true;
                    } else if (null != csvLines) {
                        CsvUtils.newLine(csvLines).addAll(Arrays.asList(attendno, showName, student._kesseki, student._late, student._early, suspendMourning, student._kouketsu));
                    }
                }
            }
        }

        if (null != csvLines) {
            if (!csvLines.isEmpty()) {
                csvLines.add(0, Arrays.asList("", "", nendo + " " + title));
                csvLines.add(1, Arrays.asList("", "", subtitle));
                csvLines.add(2, Arrays.asList("出欠集計範囲：" + sdateFormat + " ～ " + edateFormat));
            }
        }
    }

    /**
     * 生徒と1日出欠、科目別出欠のデータを取得する。
     * @param db2
     * @return 生徒データのリスト
     */
    private List<Student> getOutputList(final DB2UDB db2, final String gradeHrclass) {
        final List<Student> studentList = new ArrayList<Student>();
        final Map<String, Student> studentMap = new HashMap<String, Student>();
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T3.HR_NAME, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T2.NAME, ");
            stb.append("     T2.NAME_ENG, ");
            stb.append("     T2.SEX ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON ");
            stb.append("         T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON ");
            stb.append("         T1.YEAR = T3.YEAR ");
            stb.append("         AND T1.SEMESTER = T3.SEMESTER ");
            stb.append("         AND T1.GRADE = T3.GRADE ");
            stb.append("         AND T1.HR_CLASS = T3.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.GRADE = '" + gradeHrclass.substring(0, 2) + "' ");
            stb.append("     AND T1.HR_CLASS = '" + gradeHrclass.substring(2) + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ATTENDNO ");

            // HRの生徒を取得
            for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                final Student st = new Student(
                        KnjDbUtils.getString(row, "SCHREGNO"),
                        KnjDbUtils.getString(row, "HR_NAME"),
                        KnjDbUtils.getString(row, "ATTENDNO"),
                        KnjDbUtils.getString(row, "NAME"),
                        KnjDbUtils.getString(row, "NAME_ENG"),
                        KnjDbUtils.getString(row, "SEX"),
                        KnjDbUtils.getString(row, "GRADE"));
                studentList.add(st);
                studentMap.put(st._schregno, st);
            }
        } catch (Exception ex) {
            log.error("SQL exception!", ex);
        }

        try {
            final Map smParamMap = new HashMap();
            if (_param._hasSCHOOL_MST_SCHOOL_KIND) {
                final String schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _param._year + "' AND GRADE = '" + gradeHrclass.substring(0, 2) + "' "));
                smParamMap.put("SCHOOL_KIND", schoolKind);
            }
            final KNJSchoolMst _knjSchoolMst = new KNJSchoolMst(db2, _param._year, smParamMap);
            if (null == _param._psAttendance) {
                final String sql;
                if ("1".equals(_param._knjc163useAttendSemesDat) || "1".equals(_param._hibiNyuuryokuNasi)) {
                    sql = AttendAccumulate.getAttendSemesSql(
                            ((Boolean) _param.hasuuMap.get("semesFlg")).booleanValue(), // 累積データを使用しない
                            _param.defineSchool,
                            _knjSchoolMst,
                            _param._year,
                            _param.SSEMESTER,
                            _param.ESEMESTER,
                            (String) _param.hasuuMap.get("attendSemesInState"),
                            _param.periodInState,
                            (String) _param.hasuuMap.get("befDayFrom"),
                            (String) _param.hasuuMap.get("befDayTo"),
                            (String) _param.hasuuMap.get("aftDayFrom"),
                            (String) _param.hasuuMap.get("aftDayTo"),
                            "?",
                            "?",
                            null,
                            "SEMESTER",
                            _param._attendParamMap
                            );
                } else {
                    sql = AttendAccumulate.getAttendSemesSql(
                            false, // 累積データを使用しない
                            _param.defineSchool,
                            _knjSchoolMst,
                            _param._year,
                            _param.SSEMESTER,
                            _param.ESEMESTER,
                            (String) _param.hasuuMap.get("attendSemesInState"),
                            _param.periodInState,
                            null, // (String) hasuuMap.get("befDayFrom"),
                            null, // (String) hasuuMap.get("befDayTo"),
                            _param._sdate, // (String) hasuuMap.get("aftDayFrom"),
                            _param._edate, // (String) hasuuMap.get("aftDayTo"),
                            "?",
                            "?",
                            null,
                            "SEMESTER",
                            _param._attendParamMap
                            );
                }
                //log.info(" sql = " + sql);
                _param._psAttendance = db2.prepareStatement(sql);
            }

            if ("2".equals(_param._output)) {
                _param._bunbo = 1;
                _param._bunshi = 0;
                if ("1".equals(_param._tyuiTyouka)) { // 注意
                    log.info(" warn bunbo = " + _knjSchoolMst._kessekiWarnBunbo + ", bunshi = " + _knjSchoolMst._kessekiWarnBunsi);
                    _param._bunbo = NumberUtils.isDigits(_knjSchoolMst._kessekiWarnBunbo) ? Integer.parseInt(_knjSchoolMst._kessekiWarnBunbo) : 1;
                    _param._bunshi = NumberUtils.isDigits(_knjSchoolMst._kessekiWarnBunsi) ? Integer.parseInt(_knjSchoolMst._kessekiWarnBunsi) : 0;
                } else if ("2".equals(_param._tyuiTyouka)) { // 超過
                    log.info(" out bunbo = " + _knjSchoolMst._kessekiOutBunbo + ", bunshi = " + _knjSchoolMst._kessekiOutBunsi);
                    _param._bunbo = NumberUtils.isDigits(_knjSchoolMst._kessekiOutBunbo) ? Integer.parseInt(_knjSchoolMst._kessekiOutBunbo) : 1;
                    _param._bunshi = NumberUtils.isDigits(_knjSchoolMst._kessekiOutBunsi) ? Integer.parseInt(_knjSchoolMst._kessekiOutBunsi) : 0;
                }
                _param._bunbo = 0 >= _param._bunbo ? 1 : _param._bunbo; // 除算対応
                _param._bunshi = 0 > _param._bunshi ? 0 : _param._bunshi; // 除算対応
            }

            final Integer zero = new Integer(0);
            for (final Map row : KnjDbUtils.query(db2, _param._psAttendance, new Object[] {gradeHrclass.substring(0, 2), gradeHrclass.substring(2)})) {
                if (!"9".equals(KnjDbUtils.getString(row, "SEMESTER")) ) {
                    continue;
                }
                final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                if (null == student) {
                    continue;
                }
                student._shussekisubekiNissu = KnjDbUtils.getInt(row, "MLESSON", zero).intValue();
                student._kesseki = KnjDbUtils.getInt(row, "SICK", zero).intValue();
                student._late = KnjDbUtils.getInt(row, "LATE", zero).intValue();
                student._early = KnjDbUtils.getInt(row, "EARLY", zero).intValue();
                student._shuttei = KnjDbUtils.getInt(row, "SUSPEND", zero).intValue();
                student._kibiki = KnjDbUtils.getInt(row, "MOURNING", zero).intValue();
                student._kouketsu = KnjDbUtils.getInt(row, "ABSENT", zero).intValue();
                student._virus = KnjDbUtils.getInt(row, "VIRUS", zero).intValue();
                student._koudome = KnjDbUtils.getInt(row, "KOUDOME", zero).intValue();
            }
        } catch (Exception ex) {
            log.error("SQL exception!", ex);
        }

        final List<Student> outputList = new ArrayList<Student>();
        for (final Student student : studentList) {
            int limit = 9999;
            if ("1".equals(_param._output)) {
                limit = _param._kessekiNissu.intValue();
            } else if ("2".equals(_param._output)) {
                limit = student._shussekisubekiNissu * _param._bunshi / _param._bunbo;
            }
            if (limit >= 0 && student._kesseki >= limit) {
                //log.info(" student " + student._schregno + " " + student._name + ", kesseki = " + student._kesseki + ", limit = " + limit);
                outputList.add(student);
            }
        }
        return outputList;
    }

    private static class Student {
        final String _schregno;
        final String _hrName;
        final String _attendNo;
        final String _name;
        final String _nameEng;
        final String _sex;
        final String _grade;

        int _shussekisubekiNissu;
        int _kesseki;
        int _late;
        int _early;
        int _shuttei;
        int _kibiki;
        int _kouketsu;
        int _virus;
        int _koudome;

        public Student(
                final String schregno,
                final String hrName,
                final String attendNo,
                final String name,
                final String nameEng,
                final String sex,
                final String grade) {
            _schregno = schregno;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
            _nameEng = nameEng;
            _sex = sex;
            _grade = grade;
        }
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _sdate;
        final String _edate;
        final String _loginDate;
        final String[] _gradeHrclass;
        final String _output;
        final Integer _kessekiNissu;
        final String _tyuiTyouka;
        final String _knjc163useAttendSemesDat;
        final String _hibiNyuuryokuNasi;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;

        private int _bunbo = 1;
        private int _bunshi = 0;

        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;
        final Map _attendParamMap;
        /** 生徒氏名（英語・日本語）切替処理用 */
        final String _staffCd;
        final String _cmd;
        StaffInfo _staffInfo = null;
        // 出欠の情報
        PreparedStatement _psAttendance;
        KNJDefineSchool definecode0;
        String z010Name1;
        final String SSEMESTER = "1";
        final String ESEMESTER = "9";
        String periodInState;
        Map attendSemesMap;
        Map hasuuMap;
        KNJDefineSchool defineSchool;
        final boolean _hasSCHOOL_MST_SCHOOL_KIND;

        Param(final HttpServletRequest request, final DB2UDB db2) throws Exception {
            _cmd = request.getParameter("cmd");
            _year = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("CTRL_SEMESTER");
            _sdate = request.getParameter("SDATE").replace('/', '-');
            _edate = request.getParameter("EDATE").replace('/', '-');
            _loginDate = request.getParameter("CTRL_DATE").replace('/', '-');
            if (cmd_csvContents.equals(_cmd)) {
                _gradeHrclass = StringUtils.split(request.getParameter("CATEGORY_SELECTED"), ",");
            } else {
                _gradeHrclass = request.getParameterValues("CATEGORY_SELECTED");
            }
            _output = request.getParameter("OUTPUT");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _kessekiNissu = (NumberUtils.isDigits(request.getParameter("KESSEKI_NISSU")) ? new Integer(request.getParameter("KESSEKI_NISSU")) : new Integer(0));
            _tyuiTyouka = request.getParameter("TYUI_TYOUKA");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            _knjc163useAttendSemesDat = request.getParameter("knjc163useAttendSemesDat");
            _hibiNyuuryokuNasi = request.getParameter("hibiNyuuryokuNasi");
            _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            _attendParamMap = new HashMap();
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("DB2UDB", db2);
            setAttendSemes(db2);
            _staffCd = request.getParameter("PRINT_LOG_STAFFCD");
            try {
                _staffInfo = new StaffInfo(db2, _staffCd);
            } catch (Throwable t) {
                log.error(t);
            }
        }

        private void setAttendSemes(final DB2UDB db2) throws Exception, ParseException {
            // 出欠の情報
            definecode0 = new KNJDefineSchool();
            definecode0.defineCode(db2, _year);         //各学校における定数等設定
            z010Name1 = getZ010Name1(db2);
            periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, ESEMESTER);
            attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
            hasuuMap = AttendAccumulate.getHasuuMap(attendSemesMap,  _sdate, _edate);
            log.debug(" hasuuMap = " + hasuuMap);

            defineSchool = new KNJDefineSchool();
            defineSchool.defineCode(db2, _year);
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String getZ010Name1(DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }
    }
}
