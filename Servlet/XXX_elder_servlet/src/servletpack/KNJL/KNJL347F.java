package servletpack.KNJL;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 学校教育システム 賢者 [入試管理]
 **/

public class KNJL347F {

    private static final Log log = LogFactory.getLog(KNJL347F.class);

    private boolean _hasData;

    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        log.fatal("$Revision: 72976 $ $Date: 2020-03-16 08:58:03 +0900 (月, 16 3 2020) $"); // CVSキーワードの取り扱いに注意

//        Vrw32alp svf = null; // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; // Databaseクラスを継承したクラス

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

//        PrintWriter outstrm = new PrintWriter(response.getOutputStream());
        try {
            KNJServletUtils.debugParam(request, log);
            _param = new Param(db2, request);

            if ("csv".equals(_param._cmd)) {
                final String filename = getTitle(db2) + ".csv";
                CsvUtils.outputLines(log, response, filename, getCsvOutputLine(db2));
            } else {
//                response.setContentType("application/pdf");
//
//                svf = new Vrw32alp();
//                svf.VrInit();
//                svf.VrSetSpoolFileStream(response.getOutputStream());
//
//                printMain(svf, dataList);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if ("csv".equals(_param._cmd)) {
            } else {
//                if (!_hasData) {
//                    svf.VrSetForm("MES001.frm", 0);
//                    svf.VrsOut("note", "note");
//                    svf.VrEndPage();
//                }
//                svf.VrQuit();
//                outstrm.close(); // ストリームを閉じる
            }

            // 終了処理
            db2.commit();
            db2.close(); // DBを閉じる
        }
    }

    private List getCsvOutputLine(final DB2UDB db2) {
        final List lines = new ArrayList();
        if ("1".equals(_param._output)) {

            List<Entexam> applList = new ArrayList();
            List<D> headerList = new ArrayList();

            if ("1".equals(_param._output1div)) {
                if ("1".equals(_param._applicantdiv)) {
                    createShiganKisoJList(db2, headerList, applList);

                } else if ("2".equals(_param._applicantdiv)) {
                    createShiganKisoHList(db2, headerList, applList);
                }
            } else if ("2".equals(_param._output1div)) {
                if ("1".equals(_param._applicantdiv)) {
                    createShiganGouhiJList(db2, headerList, applList);

                } else if ("2".equals(_param._applicantdiv)) {
                    createShiganGouhiHList(db2, headerList, applList);
                }
            }

            if (!headerList.isEmpty()) {
                final List<String> headerLine = CsvUtils.newLine(lines);
                for (int i = 0; i < headerList.size(); i++) {
                    final D d = headerList.get(i);
                    headerLine.add(d._title);
                }

                for (final Entexam r : applList) {

                    final List<String> line = CsvUtils.newLine(lines);

                    for (int i = 0; i < headerList.size(); i++) {
                        final D d = headerList.get(i);
                        line.add(StringUtils.replace(StringUtils.replace(d.val(r), "\r", ""), "\n", ""));
                    }
                }
            }

        } else if ("2".equals(_param._output)) {

            final List recruitList = Recruit.getRecruitList(db2, _param);
            final List headerList = createRecruitHeaderList(db2, recruitList);

            final List headerLine = CsvUtils.newLine(lines);
            for (int i = 0; i < headerList.size(); i++) {
                final D d = (D) headerList.get(i);
                headerLine.add(d._title);
            }

            for (final Iterator it = recruitList.iterator(); it.hasNext();) {
                final Recruit r = (Recruit) it.next();

                final List line = CsvUtils.newLine(lines);

                for (int i = 0; i < headerList.size(); i++) {
                    final D d = (D) headerList.get(i);
                    line.add(StringUtils.replace(StringUtils.replace(d.val(r), "\r", ""), "\n", ""));
                }
            }
        }
        return lines;
    }

    private static abstract class D<T> {
        String _title;
        final String[] _field;
        D(final String title, final String[] field) {
            _title = title;
            _field = field;
        }
        abstract String val(final T o);
    }

    private static abstract class Convert {
        abstract String convert(final String[] vs);
    }

    private static abstract class D1Fact {
        abstract Map getMap(Entexam r);

        D<Entexam> newD(final String header, final String[] field) {
            return newD(header, field, null);
        }

        D<Entexam> newD(final String header, final String[] field, final Convert argConvert) {
            return new D<Entexam>(header, field) {
                String val(final Entexam r) {
                    final String[] vs = new String[_field.length];
                    final Map m = getMap(r);
                    for (int i = 0; i < _field.length; i++) {
                        if (null != _field[i] && _field[i].startsWith("SUBCLASS")) {
                            String v = null == m ? null : (String) m.get(_field[i]);
                            vs[i] = v;
                        } else {
                            String v = getString(m, _field[i]);
                            vs[i] = v;
                        }
                    }
                    if (null != argConvert) {
                        return argConvert.convert(vs);
                    }
                    return concat(vs);
                }
            };
        }
    }

    private static abstract class D2Fact {
        abstract Map getMap(Recruit r);

        D newD(final String header, final String[] field) {
            return newD(header, field, null);
        }

        D newD(final String header, final String[] field, final Convert argConvert) {
            return new D(header, field) {
                String val(final Object o) {
                    final Recruit r = (Recruit) o;
                    final String[] vs = new String[_field.length];
                    for (int i = 0; i < _field.length; i++) {
                        String v = getString(getMap(r), _field[i]);
                        vs[i] = v;
                    }
                    if (null != argConvert) {
                        return argConvert.convert(vs);
                    }
                    return concat(vs);
                }
            };
        }
    }

    private static String concat(final String[] vs) {
        final StringBuffer concat = new StringBuffer();
        for (int i = 0; i < vs.length; i++) {
            if (null != vs[i]) {
                concat.append(vs[i]);
            }
        }
        return concat.toString();
    }

    private static String[] a(final String s) {
        return new String[] {s};
    }

    private static String defstr(final String s) {
        return StringUtils.defaultString(s);
    }


    private List createShiganKisoJList(final DB2UDB db2, final List headerList, final List applList) {

        applList.addAll(Entexam.getEntexamList(db2, Entexam.sqlShiganKisoJ_KNJL010F(_param), _param));

        final Convert dateFormat = convertDateFormat(db2);

        final D1Fact rec = new D1Fact() { Map getMap(final Entexam e) { return e._result;}};

        final Convert notNullCheck = new Convert() { String convert(final String[] vs) { return null != vs[0] ? "レ" : "";}};
        final Convert examcoursecdConvert = new Convert() {
            String convert(final String[] course) {
                if (null == course[0] && null == course[1]) {
                    return null;
                }
                final String examcoursecdname = getString(_param._examCoursecodeMap, defstr(course[0]));
                return defstr(examcoursecdname);
            }
        };

        headerList.add(rec.newD("入試年度", a("ENTEXAMYEAR")));
        headerList.add(rec.newD("入試制度", a("APPLICANTDIVNAME")));
        headerList.add(rec.newD("受験番号", a("EXAMNO")));
        headerList.add(rec.newD("受付日付", a("RECEPTDATE"), dateFormat));
        headerList.add(rec.newD("志望区分", a("EXAMCOURSE"), examcoursecdConvert));

        for (int i = 0; i < _param._testdivList.size(); i++) {
            final TestDivDat testdivDat = (TestDivDat) _param._testdivList.get(i);

            headerList.add(rec.newD(testdivDat._name1 + "受験", a("TESTDIV" + testdivDat._testdiv), notNullCheck));
            headerList.add(rec.newD(testdivDat._name1 + "受験番号", a("RECEPTNO" + testdivDat._testdiv)));
        }

        headerList.add(rec.newD("受験料振込", a("EXAM_PAY_DIV"), convertMaruIf1()));
        headerList.add(rec.newD("受験料窓口", a("EXAM_PAY_DIV"), convertMaruIf2()));
        headerList.add(rec.newD("受験料入金日", a("EXAM_PAY_DATE"), dateFormat));
        headerList.add(rec.newD("受験料着金日", a("EXAM_PAY_CHAK_DATE"), dateFormat));
        headerList.add(rec.newD("事前番号", a("RECRUIT_NO")));

        headerList.add(rec.newD("志願者情報:ふりがな", a("NAME_KANA")));
        headerList.add(rec.newD("志願者情報:氏名", a("NAME")));
        headerList.add(rec.newD("志願者情報:生年月日", a("BIRTHDAY"), dateFormat));
        headerList.add(rec.newD("志願者情報:郵便番号", a("ZIPCD")));
        headerList.add(rec.newD("志願者情報:住所", a("ADDRESS1")));
        headerList.add(rec.newD("志願者情報:方書", a("ADDRESS2")));
        headerList.add(rec.newD("志願者情報:電話番号", a("TELNO")));
        headerList.add(rec.newD("志願者情報:出身学校", a("FS_NAME")));
        headerList.add(rec.newD("志願者情報:卒業年月", new String[] {"FS_ERACD", "FS_Y", "FS_M", "FS_GRDDIV"}, new Convert() {String convert(final String[] params) { return defstr(getString(_param._nameMstL007Map, params[0])) + defstr(params[1]) + "年" + defstr(params[2]) + "月" + defstr(getString(_param._nameMstL016Map, params[3])); }}));
        headerList.add(rec.newD("志願者情報:特別措置者", a("SPECIAL_REASON_DIV"), new Convert() { String convert(final String[] flg) { return "1".equals(flg[0]) ? "レ" : ""; }}));
        headerList.add(rec.newD("志願者情報:願書郵送", a("GANSHO_YUUSOU"), new Convert() { String convert(final String[] flg) { return "1".equals(flg[0]) ? "レ" : ""; }}));

        headerList.add(rec.newD("保護者情報:ふりがな", a("GKANA")));
        headerList.add(rec.newD("保護者情報:氏名", a("GNAME")));
        headerList.add(rec.newD("保護者情報:続柄", a("RELATIONSHIP"), new Convert() { String convert(final String[] relationShip) { return getString(_param._nameMstH201Map, relationShip[0]); }}));
        headerList.add(rec.newD("保護者情報:郵便番号", a("GZIPCD")));
        headerList.add(rec.newD("保護者情報:住所", a("GADDRESS1")));
        headerList.add(rec.newD("保護者情報:方書", a("GADDRESS2")));
        headerList.add(rec.newD("保護者情報:電話番号", a("GTELNO")));

        headerList.add(rec.newD("本校または併設大学に姉妹が在学中の方:続柄", a("SISTER_DIV")));
        headerList.add(rec.newD("本校または併設大学に姉妹が在学中の方:氏名", a("SISTER_NAME")));
        headerList.add(rec.newD("本校または併設大学に姉妹が在学中の方:現在", new String[] {"SISTER_SCHOOL_KIND", "SISTER_HR_CLASS"}, new Convert() { String convert(final String[] hrClass) { return _param.getA023(hrClass[0]) + defstr((String) _param._hrnameMap.get(hrClass[1])); }}));
        headerList.add(rec.newD("本校または併設大学に姉妹が在学中の方:大学・学部", a("SISTER_COLLEGE")));

        headerList.add(rec.newD("鏡友会校友会:氏名", a("MOTHER_NAME")));
        headerList.add(rec.newD("鏡友会校友会:続柄", a("KOUYUU_DIV")));
        headerList.add(rec.newD("鏡友会校友会:生年月日", a("MOTHER_BIRTHDAY"), dateFormat));
        headerList.add(rec.newD("鏡友会校友会:卒業年度", a("MOTHER_NENDO")));
        headerList.add(rec.newD("鏡友会校友会:卒業クラス", new String[] {"KOUYUU_SCHOOL_KIND", "MOTHER_HR_CLASS"}, new Convert() { String convert(final String[] hrClass) { return _param.getA023(hrClass[0]) + defstr((String) _param._hrnameMap.get(hrClass[1])); }}));
        headerList.add(rec.newD("鏡友会校友会:卒業大学・学部", a("KOUYUU_COLLEGE")));

        return headerList;
    }

    public Convert convertSchoolKindAbbv() {
        return new Convert() { String convert(final String[] schoolKind) { return getString(_param._nameMstA023Map, schoolKind[0]); }};
    }

    private List createShiganKisoHList(final DB2UDB db2, final List headerList, final List applList) {

        applList.addAll(Entexam.getEntexamList(db2, Entexam.sqlShiganKisoH_KNJL011F(_param), _param));

        final Convert dateFormat = convertDateFormat(db2);

        final D1Fact rec = new D1Fact() { Map getMap(final Entexam e) { return e._result;}};

        headerList.add(rec.newD("入試年度", a("ENTEXAMYEAR")));
        headerList.add(rec.newD("入試制度", a("APPLICANTDIVNAME")));
        headerList.add(rec.newD("受験番号", a("EXAMNO")));
        headerList.add(rec.newD("受付日付", a("RECEPTDATE"), dateFormat));
        headerList.add(rec.newD("志望区分", a("EXAMCOURSE"), convertExamcoursecd()));
        headerList.add(rec.newD("入試区分", a("TESTDIV"), new Convert() { String convert(final String[] testdiv) { return getString(_param._nameMstL004Map, testdiv[0]); }}));
        headerList.add(rec.newD("入試回数", a("TESTDIV0"), new Convert() { String convert(final String[] testdiv0) { return getString(_param._nameMstL034Map, testdiv0[0]); }}));
        headerList.add(rec.newD("学力診断テスト対象者", a("GAKU_TEST_FLG"), new Convert() { String convert(final String[] vs) { return !StringUtils.isBlank(vs[0]) ? "学力診断テスト対象者" : "";}}));

        headerList.add(rec.newD("受験料振込", a("EXAM_PAY_DIV"), convertMaruIf1()));
        headerList.add(rec.newD("受験料窓口", a("EXAM_PAY_DIV"), convertMaruIf2()));
        headerList.add(rec.newD("受験料入金日", a("EXAM_PAY_DATE"), dateFormat));
        headerList.add(rec.newD("受験料着金日", a("EXAM_PAY_CHAK_DATE"), dateFormat));
        headerList.add(rec.newD("事前番号", a("RECRUIT_NO")));

        headerList.add(rec.newD("志願者情報:ふりがな", a("NAME_KANA")));
        headerList.add(rec.newD("志願者情報:氏名", a("NAME")));
        headerList.add(rec.newD("志願者情報:生年月日", a("BIRTHDAY"), dateFormat));
        headerList.add(rec.newD("志願者情報:郵便番号", a("ZIPCD")));
        headerList.add(rec.newD("志願者情報:住所", a("ADDRESS1")));
        headerList.add(rec.newD("志願者情報:方書", a("ADDRESS2")));
        headerList.add(rec.newD("志願者情報:電話番号", a("TELNO")));
        headerList.add(rec.newD("志願者情報:出身学校", a("FS_NAME")));
        headerList.add(rec.newD("志願者情報:卒業年月", new String[] {"FS_ERACD", "FS_Y", "FS_M", "FS_GRDDIV"}, new Convert() {String convert(final String[] params) { return defstr(getString(_param._nameMstL007Map, params[0])) + defstr(params[1]) + "年" + defstr(params[2]) + "月" + defstr(getString(_param._nameMstL016Map, params[3])); }}));
        headerList.add(rec.newD("志願者情報:特別措置者", a("SPECIAL_REASON_DIV"), new Convert() { String convert(final String[] flg) { return "1".equals(flg[0]) ? "レ" : ""; }}));
        headerList.add(rec.newD("志願者情報:願書郵送", a("GANSHO_YUUSOU"), new Convert() { String convert(final String[] flg) { return "1".equals(flg[0]) ? "レ" : ""; }}));

        headerList.add(rec.newD("本校が第１志望ですか", a("SHDIV"), new Convert() { String convert(final String[] shdiv) { return getString(_param._nameMstL006Map, shdiv[0]); }}));
        headerList.add(rec.newD("入学手続の延期願", a("SHIFT_DESIRE_FLG"), new Convert() { String convert(final String[] shiftDesireFlg) { return "1".equals(shiftDesireFlg[0]) ? "提出する" : ""; }}));
        headerList.add(rec.newD("延期願:併願校名", a("SH_SCHOOLNAME")));
        headerList.add(rec.newD("延期願:合格発表日", a("SH_JUDGEMENT_DATE"), dateFormat));

        headerList.add(rec.newD("アドバンストへの変更", a("SLIDE_FLG"), new Convert() { String convert(final String[] slideFlg) { return "1".equals(slideFlg[0]) ? "希望する" : ""; }}));
        headerList.add(rec.newD("受験科目", a("SELECT_SUBCLASS_DIV"), new Convert() { String convert(final String[] selectSubclassDiv) { return getString(_param._nameMstL009Map, selectSubclassDiv[0]); }}));

        headerList.add(rec.newD("保護者情報:ふりがな", a("GKANA")));
        headerList.add(rec.newD("保護者情報:氏名", a("GNAME")));
        headerList.add(rec.newD("保護者情報:続柄", a("RELATIONSHIP"), new Convert() { String convert(final String[] relationShip) { return getString(_param._nameMstH201Map, relationShip[0]); }}));
        headerList.add(rec.newD("保護者情報:郵便番号", a("GZIPCD")));
        headerList.add(rec.newD("保護者情報:住所", a("GADDRESS1")));
        headerList.add(rec.newD("保護者情報:方書", a("GADDRESS2")));
        headerList.add(rec.newD("保護者情報:電話番号", a("GTELNO")));


//        headerList.add(rec.newD("本校に姉妹が在学中の方:姉妹", a("SISTER_DIV")));
//        headerList.add(rec.newD("本校に姉妹が在学中の方:氏名", a("SISTER_NAME")));
//        headerList.add(rec.newD("本校に姉妹が在学中の方:現在", new String[] {"SISTER_SCHOOL_KIND", "SISTER_HR_CLASS"}, new Convert() { String convert(final String[] hrClass) { return defstr(getString(_param._nameMstA023Map, hrClass[0])) + defstr(getString(_param._hrnameMap, hrClass[1])); }}));

        headerList.add(rec.newD("本校または併設大学に姉妹が在学中の方:続柄", a("SISTER_DIV")));
        headerList.add(rec.newD("本校または併設大学に姉妹が在学中の方:氏名", a("SISTER_NAME")));
        headerList.add(rec.newD("本校または併設大学に姉妹が在学中の方:現在", new String[] {"SISTER_SCHOOL_KIND", "SISTER_HR_CLASS"}, new Convert() { String convert(final String[] hrClass) { return _param.getA023(hrClass[0]) + defstr((String) _param._hrnameMap.get(hrClass[1])); }}));
        headerList.add(rec.newD("本校または併設大学に姉妹が在学中の方:大学・学部", a("SISTER_COLLEGE")));

//        headerList.add(rec.newD("お母様が本校の卒業生の方:卒業時氏名(旧姓)", a("MOTHER_NAME")));
//        headerList.add(rec.newD("お母様が本校の卒業生の方:卒業年度・クラス", new String[] {"MOTHER_NENDO", "MOTHER_HR_CLASS"}, new Convert() { String convert(final String[] hrClass) { return defstr(hrClass[0]) + defstr(getString(_param._hrnameMap, hrClass[1])); }}));
//        headerList.add(rec.newD("お母様が本校の卒業生の方:生年月日", a("MOTHER_BIRTHDAY"), dateFormat));

        headerList.add(rec.newD("鏡友会校友会:氏名", a("MOTHER_NAME")));
        headerList.add(rec.newD("鏡友会校友会:続柄", a("KOUYUU_DIV")));
        headerList.add(rec.newD("鏡友会校友会:生年月日", a("MOTHER_BIRTHDAY"), dateFormat));
        headerList.add(rec.newD("鏡友会校友会:卒業年度", a("MOTHER_NENDO")));
        headerList.add(rec.newD("鏡友会校友会:卒業クラス", new String[] {"KOUYUU_SCHOOL_KIND", "MOTHER_HR_CLASS"}, new Convert() { String convert(final String[] hrClass) { return _param.getA023(hrClass[0]) + defstr((String) _param._hrnameMap.get(hrClass[1])); }}));
        headerList.add(rec.newD("鏡友会校友会:卒業大学・学部", a("KOUYUU_COLLEGE")));

        for (int subi = 0; subi < _param._nameMstL008List.size(); subi++) {
            final Map l008 = (Map) _param._nameMstL008List.get(subi);
            final String cd = getString(l008, "NAMECD2");
            final String subclassname = getString(l008, "NAME1");

            headerList.add(rec.newD("内申:" + defstr(subclassname), a("CONFIDENTIAL_RPT" + cd)));
        }

        headerList.add(rec.newD("内申:3科計", a("TOTAL3")));
        headerList.add(rec.newD("内申:5科計", a("TOTAL5")));
        headerList.add(rec.newD("内申:9科計", a("TOTAL_ALL")));
        headerList.add(rec.newD("内申:段階", a("KASANTEN_ALL")));

        return headerList;
    }

    private Convert convertMaruIf2() {
        final Convert maruIf2 = new Convert() { String convert(final String[] vs) { return "2".equals(vs[0]) ? "○" : "";}};
        return maruIf2;
    }

    private Convert convertMaruIf1() {
        final Convert maruIf1 = new Convert() { String convert(final String[] vs) { return "1".equals(vs[0]) ? "○" : "";}};
        return maruIf1;
    }

    private Convert convertExamcoursecd() {
        final Convert examcoursecdConvert = new Convert() {
            String convert(final String[] course) {
                if (null == course[0]) {
                    return null;
                }
                final String examcoursecdname = getString(_param._examCoursecodeMap, defstr(course[0]));
                return defstr(examcoursecdname);
            }
        };
        return examcoursecdConvert;
    }

    private List createShiganGouhiJList(final DB2UDB db2, final List headerList, final List applList) {

        applList.addAll(Entexam.getEntexamList(db2, Entexam.sqlShiganGouhiJ_KNJL090F(_param), _param));

        final Convert dateFormat = convertDateFormat(db2);

        final D1Fact rec = new D1Fact() { Map getMap(final Entexam e) { return e._result;}};

        headerList.add(rec.newD("入試年度", a("ENTEXAMYEAR")));
        headerList.add(rec.newD("入試制度", a("APPLICANTDIVNAME")));
        headerList.add(rec.newD("受験番号", a("EXAMNO")));
        headerList.add(rec.newD("氏名", a("NAME")));
        headerList.add(rec.newD("ふりがな", a("NAME_KANA")));

        headerList.add(rec.newD("志願者情報出身学校", a("FS_NAME")));
        headerList.add(rec.newD("事前番号", a("RECRUIT_NO")));
        headerList.add(rec.newD("塾名", a("PRISCHOOL_NAME")));
        headerList.add(rec.newD("教室名 ", a("PRISCHOOL_CLASS_NAME")));

        headerList.add(rec.newD("志望区分", a("EXAMCOURSE_NAME")));
        headerList.add(rec.newD("合否", a("JUDGEMENT_NAME")));
        headerList.add(rec.newD("特待生情報", a("JUDGE_KIND_NAME")));
        headerList.add(rec.newD("合格コース", a("COURSEMAJOR"), convertExamcoursecd()));
        headerList.add(rec.newD("入学金振込", a("ENT_PAY_DIV"), convertMaruIf1()));
        headerList.add(rec.newD("入学金窓口", a("ENT_PAY_DIV"), convertMaruIf2()));
        headerList.add(rec.newD("入学金入金日", a("ENT_PAY_DATE"), dateFormat));
        headerList.add(rec.newD("入学金着金日", a("ENT_PAY_CHAK_DATE"), dateFormat));
        headerList.add(rec.newD("入学金手続日", a("PROCEDUREDATE"), dateFormat));
        headerList.add(rec.newD("入学金手続区分", a("PROCEDUREDIV"), new Convert() { String convert(final String[] vs) { return getString(_param._nameMstL011Map, vs[0]); }}));
        headerList.add(rec.newD("諸費振込", a("EXP_PAY_DIV"), convertMaruIf1()));
        headerList.add(rec.newD("諸費窓口", a("EXP_PAY_DIV"), convertMaruIf2()));
        headerList.add(rec.newD("諸費入金日", a("EXP_PAY_DATE"), dateFormat));
        headerList.add(rec.newD("諸費着金日", a("EXP_PAY_CHAK_DATE"), dateFormat));
        headerList.add(rec.newD("入学区分", a("ENTDIV"), new Convert() { String convert(final String[] vs) { return getString(_param._nameMstL012Map, vs[0]); }}));
        headerList.add(rec.newD("辞退日", a("ENTDIV2_DATE"), dateFormat));

        for (final Iterator it = _param._testdivList.iterator(); it.hasNext();) {
            final TestDivDat testdivDat = (TestDivDat) it.next();

            final String testdivname = defstr(testdivDat._name1) + ":";
            final D1Fact recRecept = new D1Fact() { Map getMap(final Entexam e) { return e.getReceptMap(_param, testdivDat);}};
            final D1Fact recBaseDetail12 = new D1Fact() { Map getMap(final Entexam e) { return e._baseDetail012Map;}};

            headerList.add(recBaseDetail12.newD(testdivname + "入試区分", a("RECEPTNO" + testdivDat._testdiv), new Convert() { String convert(final String[] receptno) { return null == receptno[0] ? "" : testdivDat._name1; }}));
            headerList.add(recRecept.newD(testdivname + "合否区分", a("JUDGEDIV"), new Convert() { String convert(final String[] judgediv) { return getString(_param._nameMstL013Map, judgediv[0]); }}));
            headerList.add(recBaseDetail12.newD(testdivname + "受験番号", a("RECEPTNO" + testdivDat._testdiv)));
            for (int i = 0; i < _param._nameMstL009List.size(); i++) {
                final Map dat = (Map) _param._nameMstL009List.get(i);
                final String name1 = getString(dat, "NAME1"); // 中学はNAME1
                if (null == name1) {
                    continue;
                }
                final String cd = getString(dat, "NAMECD2");
                headerList.add(recRecept.newD(testdivname + name1, a("SUBCLASS" + cd)));
            }
            headerList.add(recRecept.newD(testdivname + "合計", a("TOTAL4")));
        }

        return headerList;
    }

    private Convert convertDateFormat(final DB2UDB db2) {
        final Convert dateFormat = new Convert() { String convert(final String[] vs) { return KNJ_EditDate.h_format_JP(db2, vs[0]); }};
        return dateFormat;
    }

    private List<D> createShiganGouhiHList(final DB2UDB db2, final List<D> headerList, final List<Entexam> applList) {

        applList.addAll(Entexam.getEntexamList(db2, Entexam.sqlShiganGouhiH_KNJL091F(_param), _param));

        final Convert dateFormat = convertDateFormat(db2);

        final D1Fact rec = new D1Fact() { Map getMap(final Entexam e) { return e._result;}};

        headerList.add(rec.newD("入試年度", a("ENTEXAMYEAR")));
        headerList.add(rec.newD("入試制度", a("APPLICANTDIVNAME")));
        headerList.add(rec.newD("受験番号", a("EXAMNO")));
        headerList.add(rec.newD("氏名", a("NAME")));
        headerList.add(rec.newD("ふりがな", a("NAME_KANA")));

        headerList.add(rec.newD("志願者情報出身学校", a("FS_NAME")));
        headerList.add(rec.newD("事前番号", a("RECRUIT_NO")));
        headerList.add(rec.newD("塾名", a("PRISCHOOL_NAME")));
        headerList.add(rec.newD("教室名 ", a("PRISCHOOL_CLASS_NAME")));

        headerList.add(rec.newD("志望区分", a("EXAMCOURSE_NAME")));
        headerList.add(rec.newD("合否", a("JUDGEMENT_NAME")));
        headerList.add(rec.newD("特待生情報", a("JUDGE_KIND_NAME")));
        headerList.add(rec.newD("合格コース", a("COURSEMAJOR"), convertExamcoursecd()));
        headerList.add(rec.newD("入学支度金貸付", a("ENTRANCE_FLG"), new Convert() { String convert(final String[] vs) { return "1".equals(vs[0]) ? "利用する" : ""; }}));
        headerList.add(rec.newD("入学金振込", a("ENT_PAY_DIV"), convertMaruIf1()));
        headerList.add(rec.newD("入学金窓口", a("ENT_PAY_DIV"), convertMaruIf2()));
        headerList.add(rec.newD("入学金入金日", a("ENT_PAY_DATE"), dateFormat));
        headerList.add(rec.newD("入学金着金日", a("ENT_PAY_CHAK_DATE"), dateFormat));
        headerList.add(rec.newD("入学金手続日", a("PROCEDUREDATE"), dateFormat));
        headerList.add(rec.newD("入学金手続区分", a("PROCEDUREDIV"), new Convert() { String convert(final String[] vs) { return getString(_param._nameMstL011Map, vs[0]); }}));
        headerList.add(rec.newD("諸費振込", a("EXP_PAY_DIV"), convertMaruIf1()));
        headerList.add(rec.newD("諸費窓口", a("EXP_PAY_DIV"), convertMaruIf2()));
        headerList.add(rec.newD("諸費入金日", a("EXP_PAY_DATE"), dateFormat));
        headerList.add(rec.newD("諸費着金日", a("EXP_PAY_CHAK_DATE"), dateFormat));
        headerList.add(rec.newD("入学区分", a("ENTDIV"), new Convert() { String convert(final String[] vs) { return getString(_param._nameMstL012Map, vs[0]); }}));
        headerList.add(rec.newD("辞退日", a("ENTDIV2_DATE"), dateFormat));
        headerList.add(rec.newD("入学コース", a("ENTER_COURSEMAJOR")));

        for (final TestDivDat testdivDat : _param._testdivList) {
        	
            final String testdivname = defstr(testdivDat._name1) + ":";
            final D1Fact recRecept = new D1Fact() { Map getMap(final Entexam e) { return e.getReceptMap(_param, testdivDat); }};

            headerList.add(recRecept.newD(testdivname + "入試区分", a("TESTDIV"), new Convert() { String convert(final String[] testdiv) { return null == testdiv[0] ? "" : testdivDat._name1; }}));
            headerList.add(recRecept.newD(testdivname + "入試回数", a("TESTDIV0"), new Convert() { String convert(final String[] testdiv0) { return getString(_param._nameMstL034Map, testdiv0[0]); }}));
            for (final Map dat : _param._nameMstL009List) {
                final String name2 = getString(dat, "NAME2"); // 高校はNAME2
                if (null == name2) {
                    continue;
                }
                final String cd = getString(dat, "NAMECD2");
                headerList.add(recRecept.newD(testdivname + name2, a("SUBCLASS" + cd)));
            }
            headerList.add(recRecept.newD(testdivname + "合計", a("TOTAL4")));
        }

        return headerList;
    }

    private String getTitle(final DB2UDB db2) {
        String title = "";
        if ("1".equals(_param._output)) {
            title = "志願者データ";
            if ("1".equals(_param._output1div)) {
                title += "（基礎データ）";
            } else if ("2".equals(_param._output1div)) {
                title += "（合否データ）";
            }
        } else if ("2".equals(_param._output)) {
            title = "募集企画データ";
        }
        title = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 " + defstr(_param._applicantdivname) + "入試 " + title;
        return title;
    }




    private static List<Map<String, String>> query(final DB2UDB db2, final String sql, final String name) {
    	log.info(" query " + name + " sql = " + sql);
    	final List<Map<String, String>> rowList = KnjDbUtils.query(db2, sql);
//            log.info(" query list size = " + rowList.size());
        return rowList;
    }

    private static Map queryAsMap(final DB2UDB db2, final String sql, final String key, final String value) {
        final Map map = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" queryAsMap sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                if (null != value) {
                    map.put(rs.getString(key), rs.getString(value));
                } else {
                    map.put(rs.getString(key), rsToMap(rs, meta));
                }
            }
//            log.info(" map size = " + map.size());
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return map;
    }

    private static Map rsToMap(ResultSet rs, final ResultSetMetaData meta) throws SQLException {
        final Map m = new HashMap();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            m.put(meta.getColumnLabel(i), rs.getString(meta.getColumnLabel(i)));
        }
        return m;
    }

    private static String getString(final Map m, final String field) {
    	return KnjDbUtils.getString(m, field);
    }

    private List createRecruitHeaderList(final DB2UDB db2, final List recruitList) {
        int eventSize = 0;
        int consultSize = 0;
        for (final Iterator it = recruitList.iterator(); it.hasNext();) {
            final Recruit r = (Recruit) it.next();
            eventSize = Math.max(eventSize, r._eventList.size());
            consultSize = Math.max(consultSize, r._consultList.size());
        }
        eventSize = Math.max(eventSize, 1);
        consultSize = Math.max(consultSize, 1);


        final Convert dateFormat = convertDateFormat(db2);

        final List headerList = new ArrayList();
        final D2Fact rec = new D2Fact() { Map getMap(final Recruit r) { return r._recruitDat;}};

        headerList.add(rec.newD("管理番号", a("RECRUIT_NO")));
        headerList.add(rec.newD("受付校種", a("SCHOOL_KIND"), convertSchoolKindAbbv()));
        headerList.add(rec.newD("氏名", a("NAME")));
        headerList.add(rec.newD("氏名かな", a("NAME_KANA")));
        headerList.add(rec.newD("保護者氏名", a("GUARD_NAME")));
        headerList.add(rec.newD("保護者かな", a("GUARD_KANA")));
        headerList.add(rec.newD("性別", a("SEX_NAME")));
        headerList.add(rec.newD("生年月日", a("BIRTHDAY"), dateFormat));
        headerList.add(rec.newD("郵便番号", a("ZIPCD")));
        headerList.add(rec.newD("住所１", a("ADDR1")));
        headerList.add(rec.newD("住所２", a("ADDR2")));
        headerList.add(rec.newD("電話番号", a("TELNO")));
        headerList.add(rec.newD("携帯番号", a("TELNO2")));
        headerList.add(rec.newD("FAX", a("FAXNO")));
        headerList.add(rec.newD("E-mail", a("EMAIL")));
        headerList.add(rec.newD("出身学校", a("FINSCHOOL_NAME")));
        headerList.add(rec.newD("学年担任", a("GRADE_NAME")));
        headerList.add(rec.newD("先生", a("SCHOOL_TEACHER")));
        headerList.add(rec.newD("塾　教室", new String[] {"PRISCHOOL_NAME", "PRISCHOOL_CLASS_NAME"}));
        headerList.add(rec.newD("塾　先生", a("PRISCHOOL_TEACHER")));
        headerList.add(rec.newD("備考", a("REMARK")));
        final Convert courseConvert = new Convert() {
            String convert(final String[] course) {
                if (null == course[0] && null == course[1]) {
                    return null;
                }
                final String coursename = getString(_param._coursemajorMap, defstr(course[0]) + defstr(course[1]));
                final String examcoursecdname = (String) _param._examCoursecodeMap.get(defstr(course[0]) + defstr(course[1]) + defstr(course[2]));
                return defstr(coursename) + defstr(examcoursecdname);
            }
        };
        for (int i = 0; i < eventSize; i++) {
            final int ii = i;
            final D2Fact recEve = new D2Fact() {
                Map getMap(final Recruit r) { return ii < r._eventList.size() ? (Map) r._eventList.get(ii) : null ;}
            };

            final String event = "イベント" + String.valueOf(i + 1) + ":";
            headerList.add(recEve.newD(event + "登録日付", a("TOUROKU_DATE"), dateFormat));
            headerList.add(recEve.newD(event + "分類", a("EVENT_CLASS_NAME")));
            headerList.add(recEve.newD(event + "イベント", a("EVENT_NAME")));
            headerList.add(recEve.newD(event + "媒体", a("MEDIA_NAME")));
            headerList.add(recEve.newD(event + "参加状況", a("STATE_NAME")));
            headerList.add(recEve.newD(event + "希望課程学科コース１", new String[] {"HOPE_COURSECD1", "HOPE_MAJORCD1", "HOPE_COURSECODE1"}, courseConvert));
            headerList.add(recEve.newD(event + "希望課程学科コース２", new String[] {"HOPE_COURSECD2", "HOPE_MAJORCD2", "HOPE_COURSECODE2"}, courseConvert));
            headerList.add(recEve.newD(event + "希望課程学科コース３", new String[] {"HOPE_COURSECD3", "HOPE_MAJORCD3", "HOPE_COURSECODE3"}, courseConvert));
            headerList.add(recEve.newD(event + "希望課程学科コース４", new String[] {"HOPE_COURSECD4", "HOPE_MAJORCD4", "HOPE_COURSECODE4"}, courseConvert));
            headerList.add(recEve.newD(event + "希望課程学科コース５", new String[] {"HOPE_COURSECD5", "HOPE_MAJORCD5", "HOPE_COURSECODE5"}, courseConvert));
            headerList.add(recEve.newD(event + "備考", a("REMARK")));
        }

        headerList.add(rec.newD("来校者要約", a("CONSULT_WRAPUP_REMARK")));
        for (int i = 0; i < consultSize; i++) {
            final int ii = i;
            final D2Fact recConsult = new D2Fact() {
                Map getMap(final Recruit r) { return ii < r._consultList.size() ? (Map) r._consultList.get(ii) : null ;}
            };
            final String consult = "来校者相談" + String.valueOf(i + 1) + ":";
            headerList.add(recConsult.newD(consult + "登録日付", a("TOUROKU_DATE")));
            headerList.add(recConsult.newD(consult + "相談者", a("CONSULT_NAME")));
            headerList.add(recConsult.newD(consult + "方法", a("METHOD_NAME")));
            headerList.add(recConsult.newD(consult + "面談者", a("STAFFNAME")));
            headerList.add(recConsult.newD(consult + "相談内容", a("CONTENTS")));
        }

        final D2Fact recVisit = new D2Fact() { Map getMap(final Recruit r) { return r._visitDat;}};

        final Convert courseConvert2 = new Convert() {
            String convert(final String[] course) {
                if (null == course[0] && null == course[1] && null == course[2]) {
                    return null;
                }
                final String examcoursecdname = getString(_param._examCoursecodeMap, course[0] + course[1] + course[2]);
                return examcoursecdname;
            }
        };

        if ("2".equals(_param._applicantdiv)) {

            final String visit = "来校者情報:";
            headerList.add(recVisit.newD(visit + "登録日付", a("TOUROKU_DATE")));
            headerList.add(recVisit.newD(visit + "確定日付", a("KAKUTEI_DATE")));
            headerList.add(recVisit.newD(visit + "希望コース", new String[] {"HOPE_COURSECD", "HOPE_MAJORCD", "HOPE_COURSECODE"}, courseConvert2));
            headerList.add(recVisit.newD(visit + "担当", a("STAFFNAME")));
            headerList.add(recVisit.newD(visit + "受験種別", a("TESTDIV"), new Convert () { String convert(final String[] testdiv) { return getString(_param._nameMstL407Map, testdiv[0]); }}));
            headerList.add(recVisit.newD(visit + "備考", a("REMARK1")));

            for (int si = 0; si < 2; si++) {
                final String semester = String.valueOf(si + 1);
                final String semestername = new String[] {"1学期（前期）", "2学期（後期）"}[si];

                final D2Fact recVisitScore = new D2Fact() {
                    Map getMap(final Recruit r) { return r.getVisitScoreMap(semester);}
                };

                for (int subi = 0; subi < _param._nameMstL008List.size(); subi++) {
                    final Map l008 = (Map) _param._nameMstL008List.get(subi);
                    final String cd = getString(l008, "NAMECD2");
                    final String subclassname = getString(l008, "NAME1");

                    headerList.add(recVisitScore.newD("通知票評定:" + semestername + defstr(subclassname), a("SUBCLASSCD" + cd)));
                }

                headerList.add(recVisitScore.newD("通知票評定:" + semestername + "３科計", a("TOTAL3")));
                headerList.add(recVisitScore.newD("通知票評定:" + semestername + "５科計", a("TOTAL5")));
                headerList.add(recVisitScore.newD("通知票評定:" + semestername + "９科計", a("TOTAL9")));
            }

            final Convert convertCompany = new Convert() {
                String convert(final String[] vs) {
                    final String companycd = vs[0];
                    if ("9999".equals(companycd)) {
                        return vs[1];
                    }
                    if (null == vs[0] || vs[0].length() <= 4) {
                        return null;
                    }
                    return getString(_param._nameMstL406Map, vs[0].substring(4));
                }
            };

            final DecimalFormat df = new DecimalFormat("00");
            for (int imonth = 4; imonth <= 12; imonth++) {
                final String month = df.format(imonth);
                final String monthname = String.valueOf(imonth) + "月";

                final D2Fact recVisitMock = new D2Fact() {
                    Map getMap(final Recruit r) { return r.getVisitMockMap(month);}
                };

                for (int subi = 0; subi < 5; subi++) {
                    final String cd = df.format(subi + 1);
                    final String subclassname = new String[] {"国語", "数学", "英語", "社会", "理科"}[subi];

                    headerList.add(recVisitMock.newD("模試偏差値:" + monthname + defstr(subclassname), a("SUBCLASSCD" + cd)));
                }

                headerList.add(recVisitMock.newD("模試偏差値:" + monthname + "３科", a("AVG3")));
                headerList.add(recVisitMock.newD("模試偏差値:" + monthname + "５科", a("AVG5")));
                headerList.add(recVisitMock.newD("模試偏差値:" + monthname + "模試名", new String[] {"COMPANYCD", "COMPANY_TEXT"}, convertCompany));

            }
            final D2Fact recVisitMock = new D2Fact() {
                Map getMap(final Recruit r) { return r.getVisitMockMap("99");}
            };
            headerList.add(recVisitMock.newD("模試偏差値:TOP2（同一月不可）３科_1", a("TOP1_AVG3")));
            headerList.add(recVisitMock.newD("模試偏差値:TOP2（同一月不可）５科_1", a("TOP1_AVG5")));
            headerList.add(recVisitMock.newD("模試偏差値:TOP2（同一月不可）模試名_1", new String[] {"TOP1_COMPANYCD", "TOP1_COMPANY_TEXT"}, convertCompany));

            headerList.add(recVisitMock.newD("模試偏差値:TOP2（同一月不可）３科_2", a("TOP2_AVG3")));
            headerList.add(recVisitMock.newD("模試偏差値:TOP2（同一月不可）５科_2", a("TOP2_AVG5")));
            headerList.add(recVisitMock.newD("模試偏差値:TOP2（同一月不可）模試名_2", new String[] {"TOP2_COMPANYCD", "TOP2_COMPANY_TEXT"}, convertCompany));

            headerList.add(recVisitMock.newD("模試偏差値:TOP2の偏差値平均", a("TOP_AVG")));

            headerList.add(recVisit.newD(visit + "特待生", a("JUDGE_KIND"), new Convert () { String convert(final String[] judgeKind) { return getString(_param._nameMstL025Name2Map, judgeKind[0]); }}));
            headerList.add(recVisit.newD(visit + "志望校", new String[] {"SCHOOL_DIV", "SCHOOL_NAME"}, new Convert () { String convert(final String[] schooldivSchoolname) { return defstr(getString(_param._nameMstL015Map, schooldivSchoolname[0])) + defstr(schooldivSchoolname[1]); }}));

            final Convert convertCheck = new Convert() { String convert(final String[] vs) { return "1".equals(vs[0]) ? "レ" : "";}};
            for (int i = 0; i < _param._nameMstL408List.size(); i++) {
                final Map l408 = (Map) _param._nameMstL408List.get(i);
                final D2Fact recVisitActive1 = new D2Fact() {
                    Map getMap(final Recruit r) { return r.getVisitActiveMap("1", getString(l408, "NAMECD2"));}
                };
                headerList.add(recVisitActive1.newD("諸活動:" + defstr(getString(l408, "NAME1")), a("REMARK1"), convertCheck));
            }
            headerList.add(rec.newD("諸活動:合計", a("SHOKATSUDOU_POINT_TOTAL")));

            for (int i = 0; i < 2; i++) {
                final String seq = String.valueOf(i + 1);
                final String name = new String[] {"中学時代の部活動", "学校外での諸活動"}[i];
                final D2Fact recVisitActive2 = new D2Fact() {
                    Map getMap(final Recruit r) { return r.getVisitActiveMap("2", seq);}
                };
                headerList.add(recVisitActive2.newD(name, a("REMARK1")));
            }
        }

        return headerList;
    }

    private static class Entexam {

        private static String EXAMNO = "EXAMNO";

        Map<String, String> _result;
        final Map<String, Map<String, String>> _receptMap = new HashMap();
        final Map<String, String> _baseDetail012Map = new HashMap(); // 志願受験番号

        public static List<Entexam> getEntexamList(final DB2UDB db2, final String sql, final Param param) {
            final Map<String, Entexam> examnoEntexamMap = new HashMap();
            final List<Entexam> entexamList = new ArrayList();
            for (final Map<String, String> data : query(db2, sql, "entexam")) {
                final Entexam entexam = new Entexam();
                entexam._result = data;
                entexamList.add(entexam);
                examnoEntexamMap.put(getString(entexam._result, EXAMNO), entexam);
            }
            final Map<String, Map<String, String>> receptnoReceptMap = new HashMap();
            for (final Map recept : query(db2, receptSql(param), "recept")) {
                final Entexam entexam = examnoEntexamMap.get(getString(recept, EXAMNO));
                entexam._receptMap.put(getString(recept, "TESTDIV"), recept);
                receptnoReceptMap.put(getString(recept, "TESTDIV") + "-" + getString(recept, "RECEPTNO"), recept);
            }
            for (final Map<String, String> baseDetail012 : query(db2, sqlShiganGouhiJ2(param), "shigan-gouhij2")) {
                final Entexam entexam = examnoEntexamMap.get(getString(baseDetail012, EXAMNO));
                if (null == entexam) {
                	continue;
                }
                for (final Map<String, String> codeArray : param._testdivArray) {
                    final String testdiv = getString(codeArray, "NAMECD2");
                    entexam._baseDetail012Map.put("RECEPTNO" + testdiv, getString(baseDetail012, "RECEPTNO" + testdiv));
                }
            }

            for (final Map<String, String> scoreMap : query(db2, scoreSql(param), "score")) {
                final Map recept = receptnoReceptMap.get(getString(scoreMap, "TESTDIV") + "-" + getString(scoreMap, "RECEPTNO"));
                if (null == recept) {
                    continue;
                }
                recept.put("SUBCLASS" + getString(scoreMap, "TESTSUBCLASSCD"), "0".equals(getString(scoreMap, "ATTEND_FLG")) ? "＊" : getString(scoreMap, "SCORE"));
            }

            return entexamList;
        }

        private static String sqlShiganGouhiJ2(final Param param) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     T1.entexamyear,");
            sql.append("     T1.APPLICANTDIV, ");
            sql.append("     T1.EXAMNO ");
            //事前番号
            //012:受験番号
            for (final Map codeArray : param._testdivArray) {
                final String testdiv = getString(codeArray, "NAMECD2");
                sql.append("     , BASE_D_012.REMARK" + testdiv +" AS RECEPTNO" + testdiv +" ");
            }
            sql.append(" FROM ");
            sql.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            //受験番号(RECEPTNO)
            sql.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BASE_D_012 ");
            sql.append("      ON BASE_D_012.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("     AND BASE_D_012.EXAMNO = T1.EXAMNO ");
            sql.append("     AND BASE_D_012.SEQ = '012' ");
            sql.append(" WHERE ");
            sql.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' AND ");
            sql.append("     T1.APPLICANTDIV = '1' ");//1:中学
            sql.append(" ORDER BY T1.EXAMNO ");
            return sql.toString();
        }

        public Map getReceptMap(final Param param, final TestDivDat testdiv) {
            final Map recept = _receptMap.get(testdiv._testdiv);
            if ("1".equals(param._applicantdiv)) {
                return recept;
            }
            if (null != recept && null != getString(recept, "TESTDIV0") && getString(recept, "TESTDIV0").equals(testdiv._testdiv0)) {
                return recept;
            }
            return null;
        }

        private static String sqlShiganKisoJ_KNJL010F(final Param param) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     T1.entexamyear,");
            sql.append("     T1.APPLICANTDIV, ");
            sql.append("      N0.name1 AS applicantdivname,");
            sql.append("     T1.TESTDIV AS TEST_L_DIV, ");
            sql.append("     T1.EXAMNO, ");
            sql.append("     T5.REMARK8 || T5.REMARK9 || T5.REMARK10 AS EXAMCOURSE, ");
            //受験料
            sql.append("     M1.EXAM_PAY_DIV, ");
            sql.append("     M1.EXAM_PAY_DATE, ");
            sql.append("     M1.EXAM_PAY_CHAK_DATE, ");
            //特別入試対象者
            sql.append("     T1.GENERAL_FLG AS TOKU_TEST_FLG, ");
            //事前番号
            sql.append("     BASE_D_002.REMARK1 AS RECRUIT_NO, ");
            //010:入試区分,011:受験型,012:受験番号,013:帰国生Ｂ方式選択科目
            for (final Map codeArray : param._testdivArray) {
                final String testdiv = getString(codeArray, "NAMECD2");
                sql.append("     BASE_D_010.REMARK" + testdiv +" AS TESTDIV" + testdiv +", ");
                sql.append("     BASE_D_011.REMARK" + testdiv +" AS EXAM_TYPE" + testdiv +", ");
                sql.append("     BASE_D_012.REMARK" + testdiv +" AS RECEPTNO" + testdiv +", ");
                sql.append("     BASE_D_013.REMARK" + testdiv +" AS TESTSUBCLASSCD" + testdiv +", ");
            }
            //姉妹
            //母
            sql.append("     NML056.NAME1       AS SISTER_DIV, ");
            sql.append("     BASE_D_014.REMARK2 AS SISTER_NAME, ");
            sql.append("     BASE_D_014.REMARK3 AS SISTER_SCHOOL_KIND, ");
            sql.append("     BASE_D_014.REMARK4 AS SISTER_HR_CLASS, ");
            sql.append("     BASE_D_014.REMARK6 AS SISTER_COLLEGE, ");
            sql.append("     BASE_D_015.REMARK1 AS MOTHER_NAME, ");
            sql.append("     BASE_D_015.REMARK2 AS MOTHER_NENDO, ");
            sql.append("     BASE_D_015.REMARK3 AS MOTHER_HR_CLASS, ");
            sql.append("     BASE_D_015.REMARK4 AS MOTHER_BIRTHDAY, ");
            sql.append("     BASE_D_015.REMARK5 AS KOUYUU_DIV, ");
            sql.append("     BASE_D_015.REMARK6 AS KOUYUU_SCHOOL_KIND, ");
            sql.append("     BASE_D_015.REMARK7 AS KOUYUU_COLLEGE, ");
            sql.append("     T1.RECEPTDATE, ");
            sql.append("     T1.TESTDIV1 AS GANSHO_YUUSOU, ");
            sql.append("     T1.SPECIAL_REASON_DIV, ");
            sql.append("     T1.NAME, ");
            sql.append("     T1.NAME_KANA, ");
            sql.append("     T1.BIRTHDAY, ");
            sql.append("     T1.FS_CD, ");
            sql.append("     FIN.FINSCHOOL_NAME AS FS_NAME, ");
            sql.append("     T1.FS_GRDYEAR, ");
            sql.append("     T1.FS_Y, ");
            sql.append("     T1.FS_M, ");
            sql.append("     T1.FS_GRDDIV, ");
            sql.append("     T1.FS_DAY, ");
            sql.append("     T2.ZIPCD, ");
            sql.append("     T2.ADDRESS1, ");
            sql.append("     T2.ADDRESS2, ");
            sql.append("     T2.TELNO, ");
            sql.append("     T2.GNAME, ");
            sql.append("     T2.GKANA, ");
            sql.append("     T2.RELATIONSHIP, ");
            sql.append("     T2.GZIPCD, ");
            sql.append("     T2.GADDRESS1, ");
            sql.append("     T2.GADDRESS2, ");
            sql.append("     T2.GTELNO, ");
            sql.append("     T3.NAME1, ");
            sql.append("     T4.REMARK1, ");
            sql.append("     T4.REMARK2, ");
            sql.append("     T4.REMARK3, ");
            sql.append("     T4.REMARK4, ");
            sql.append("     T4.REMARK5, ");
            sql.append("     T4.REMARK6, ");
            sql.append("     T4.REMARK7 AS SHOUGAKU1, ");
            sql.append("     T4.REMARK8 AS SHOUGAKU5, ");
            sql.append("     T4.REMARK9 AS SOUDAN, ");
            sql.append("     L1.CONFIDENTIAL_RPT01, ");
            sql.append("     L1.CONFIDENTIAL_RPT02, ");
            sql.append("     L1.CONFIDENTIAL_RPT03, ");
            sql.append("     L1.CONFIDENTIAL_RPT04, ");
            sql.append("     L1.CONFIDENTIAL_RPT05, ");
            sql.append("     L1.CONFIDENTIAL_RPT06, ");
            sql.append("     L1.CONFIDENTIAL_RPT07, ");
            sql.append("     L1.CONFIDENTIAL_RPT08, ");
            sql.append("     L1.CONFIDENTIAL_RPT09, ");
            sql.append("     L1.CONFIDENTIAL_RPT10, ");
            sql.append("     L1.TOTAL_ALL, ");
            sql.append("     L1.TOTAL5, ");
            sql.append("     L1.KASANTEN_ALL, ");
            sql.append("     L1.ABSENCE_DAYS, ");
            sql.append("     L1.ABSENCE_DAYS2, ");
            sql.append("     L1.ABSENCE_DAYS3, ");
            sql.append("     L1.ABSENCE_REMARK, ");
            sql.append("     L1.ABSENCE_REMARK2, ");
            sql.append("     L1.ABSENCE_REMARK3, ");
            sql.append("     L1.REMARK1 AS CONFRPT_REMARK1, ");
            sql.append("     D4.REMARK1 AS DETAIL4_REMARK1, ");
            sql.append("     L013.NAME1 AS JUDGEMENT_INFO, ");
            sql.append("     T1.FS_ERACD ");
            sql.append(" FROM ");
            sql.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            sql.append(" LEFT OUTER JOIN ");
            sql.append("     ENTEXAM_APPLICANTADDR_DAT T2 ");
            sql.append(" ON ");
            sql.append("     T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ");
            sql.append("     T1.EXAMNO = T2.EXAMNO ");
            sql.append(" LEFT OUTER JOIN ");
            sql.append("     V_NAME_MST T3 ");
            sql.append(" ON ");
            sql.append("     T1.ENTEXAMYEAR = T3.YEAR AND ");
            sql.append("     T1.ERACD = T3.NAMECD2 AND ");
            sql.append("     T3.NAMECD1 = 'L007' ");
            sql.append(" LEFT OUTER JOIN ");
            sql.append("     ENTEXAM_APPLICANTBASE_DETAIL_DAT T4 ");
            sql.append(" ON ");
            sql.append("     T1.ENTEXAMYEAR = T4.ENTEXAMYEAR AND ");
            sql.append("     T1.EXAMNO = T4.EXAMNO AND ");
            sql.append("     T4.SEQ = '009' ");
            sql.append(" LEFT OUTER JOIN ");
            sql.append("     ENTEXAM_APPLICANTBASE_DETAIL_DAT T5 ");
            sql.append(" ON ");
            sql.append("     T1.ENTEXAMYEAR = T5.ENTEXAMYEAR AND ");
            sql.append("     T1.EXAMNO = T5.EXAMNO AND ");
            sql.append("     T5.SEQ = '001' ");
            //入試区分(TESTDIV)
            sql.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BASE_D_010 ");
            sql.append("      ON BASE_D_010.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("     AND BASE_D_010.EXAMNO = T1.EXAMNO ");
            sql.append("     AND BASE_D_010.SEQ = '010' ");
            //受験型(EXAM_TYPE)
            sql.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BASE_D_011 ");
            sql.append("      ON BASE_D_011.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("     AND BASE_D_011.EXAMNO = T1.EXAMNO ");
            sql.append("     AND BASE_D_011.SEQ = '011' ");
            //受験番号(RECEPTNO)
            sql.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BASE_D_012 ");
            sql.append("      ON BASE_D_012.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("     AND BASE_D_012.EXAMNO = T1.EXAMNO ");
            sql.append("     AND BASE_D_012.SEQ = '012' ");
            //帰国生Ｂ方式選択科目(TESTSUBCLASSCD)
            sql.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BASE_D_013 ");
            sql.append("      ON BASE_D_013.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("     AND BASE_D_013.EXAMNO = T1.EXAMNO ");
            sql.append("     AND BASE_D_013.SEQ = '013' ");
            //姉妹
            //母
            sql.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_014 ");
            sql.append("      ON BASE_D_014.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("     AND BASE_D_014.EXAMNO = T1.EXAMNO ");
            sql.append("     AND BASE_D_014.SEQ = '014' ");
            sql.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_015 ");
            sql.append("      ON BASE_D_015.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("     AND BASE_D_015.EXAMNO = T1.EXAMNO ");
            sql.append("     AND BASE_D_015.SEQ = '015' ");
            sql.append(" LEFT JOIN NAME_MST NML056 ON NML056.NAMECD1 = 'L056' ");
            sql.append("                          AND NML056.NAMECD2 = BASE_D_014.REMARK1 ");
            //事前番号
            sql.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_002 ");
            sql.append("      ON BASE_D_002.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("     AND BASE_D_002.EXAMNO = T1.EXAMNO ");
            sql.append("     AND BASE_D_002.SEQ = '002' ");
            //受験料
            sql.append(" LEFT JOIN ENTEXAM_MONEY_DAT M1 ");
            sql.append("      ON M1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("     AND M1.APPLICANTDIV = T1.APPLICANTDIV ");
            sql.append("     AND M1.EXAMNO = T1.EXAMNO ");
            sql.append(" LEFT OUTER JOIN ");
            sql.append("     ENTEXAM_APPLICANTBASE_DETAIL_DAT D4 ");
            sql.append(" ON ");
            sql.append("     T1.ENTEXAMYEAR = D4.ENTEXAMYEAR AND ");
            sql.append("     T1.EXAMNO = D4.EXAMNO AND ");
            sql.append("     D4.SEQ = '004' ");
            sql.append(" LEFT OUTER JOIN ");
            sql.append("     FINSCHOOL_MST FIN ");
            sql.append(" ON ");
            sql.append("     T1.FS_CD = FIN.FINSCHOOLCD ");
            sql.append(" LEFT OUTER JOIN ");
            sql.append("     ENTEXAM_APPLICANTCONFRPT_DAT L1 ");
            sql.append(" ON ");
            sql.append("     T1.ENTEXAMYEAR = L1.ENTEXAMYEAR AND ");
            sql.append("     T1.EXAMNO = L1.EXAMNO ");
            sql.append("     LEFT JOIN V_NAME_MST L013 ");
            sql.append("                  ON  L013.YEAR = T1.ENTEXAMYEAR ");
            sql.append("                  AND L013.NAMECD1 = 'L013' ");
            sql.append("                  AND L013.NAMECD2 = T1.JUDGEMENT ");
            sql.append("                  AND L013.NAMECD2 IN ('4','5') ");
            sql.append("      LEFT JOIN v_name_mst N0 ");
            sql.append("                   ON N0.year = T1.entexamyear AND N0.namecd2 = T1.applicantdiv AND N0.namecd1 = 'L003'");
            sql.append(" WHERE ");
            sql.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' AND ");
            sql.append("     T1.APPLICANTDIV = '1' ");//1:中学
            sql.append(" ORDER BY T1.EXAMNO ");

            return sql.toString();
        }

        private static String sqlShiganKisoH_KNJL011F(final Param param) {
            final StringBuffer sql = new StringBuffer();

            sql.append(" SELECT ");
            sql.append("     T1.entexamyear,");
            sql.append("     T1.APPLICANTDIV, ");
            sql.append("      N0.name1 AS applicantdivname,");
            sql.append("     T1.EXAMNO, ");
            sql.append("     T5.REMARK8 || T5.REMARK9 || T5.REMARK10 AS EXAMCOURSE, ");
            sql.append("     T1.TESTDIV, ");
            sql.append("     T1.TESTDIV0, ");
            //受験料
            sql.append("     M1.EXAM_PAY_DIV, ");
            sql.append("     M1.EXAM_PAY_DATE, ");
            sql.append("     M1.EXAM_PAY_CHAK_DATE, ");
            //学力診断テスト対象者
            sql.append("     T1.GENERAL_FLG AS GAKU_TEST_FLG, ");
            //事前番号
            sql.append("     BASE_D_002.REMARK1 AS RECRUIT_NO, ");
            //姉妹
            //母
            sql.append("     NML056.NAME1       AS SISTER_DIV, ");
            sql.append("     BASE_D_014.REMARK2 AS SISTER_NAME, ");
            sql.append("     BASE_D_014.REMARK3 AS SISTER_SCHOOL_KIND, ");
            sql.append("     BASE_D_014.REMARK4 AS SISTER_HR_CLASS, ");
            sql.append("     BASE_D_014.REMARK6 AS SISTER_COLLEGE, ");
            sql.append("     BASE_D_015.REMARK1 AS MOTHER_NAME, ");
            sql.append("     BASE_D_015.REMARK2 AS MOTHER_NENDO, ");
            sql.append("     BASE_D_015.REMARK3 AS MOTHER_HR_CLASS, ");
            sql.append("     BASE_D_015.REMARK4 AS MOTHER_BIRTHDAY, ");
            sql.append("     BASE_D_015.REMARK5 AS KOUYUU_DIV, ");
            sql.append("     BASE_D_015.REMARK6 AS KOUYUU_SCHOOL_KIND, ");
            sql.append("     BASE_D_015.REMARK7 AS KOUYUU_COLLEGE, ");
            sql.append("     T1.SHDIV, ");
            sql.append("     T1.SHIFT_DESIRE_FLG, ");
            sql.append("     T1.SLIDE_FLG, ");
            sql.append("     T1.SELECT_SUBCLASS_DIV, ");
            //併願校名、併願校合格発表日
            sql.append("     BASE_D_016.REMARK1 AS SH_SCHOOLNAME, ");
            sql.append("     BASE_D_016.REMARK2 AS SH_JUDGEMENT_DATE, ");
            sql.append("     T1.RECEPTDATE, ");
            sql.append("     T1.TESTDIV1 AS GANSHO_YUUSOU, ");
            sql.append("     T1.SPECIAL_REASON_DIV, ");
            sql.append("     T1.NAME, ");
            sql.append("     T1.NAME_KANA, ");
            sql.append("     T1.SEX, ");
            sql.append("     T1.BIRTHDAY, ");
            sql.append("     T1.FS_CD, ");
            sql.append("     FIN.FINSCHOOL_NAME AS FS_NAME, ");
            sql.append("     T1.FS_GRDYEAR, ");
            sql.append("     T1.FS_Y, ");
            sql.append("     T1.FS_M, ");
            sql.append("     T1.FS_GRDDIV, ");
            sql.append("     T1.FS_DAY, ");
            sql.append("     T2.ZIPCD, ");
            sql.append("     T2.ADDRESS1, ");
            sql.append("     T2.ADDRESS2, ");
            sql.append("     T2.TELNO, ");
            sql.append("     T2.GNAME, ");
            sql.append("     T2.GKANA, ");
            sql.append("     T2.RELATIONSHIP, ");
            sql.append("     T2.GZIPCD, ");
            sql.append("     T2.GADDRESS1, ");
            sql.append("     T2.GADDRESS2, ");
            sql.append("     T2.GTELNO, ");
            sql.append("     T3.NAME1, ");
            sql.append("     T4.REMARK1, ");
            sql.append("     T4.REMARK2, ");
            sql.append("     T4.REMARK3, ");
            sql.append("     T4.REMARK4, ");
            sql.append("     T4.REMARK5, ");
            sql.append("     T4.REMARK6, ");
            sql.append("     T4.REMARK7 AS SHOUGAKU1, ");
            sql.append("     T4.REMARK8 AS SHOUGAKU5, ");
            sql.append("     T4.REMARK9 AS SOUDAN, ");
            sql.append("     L1.CONFIDENTIAL_RPT01, ");
            sql.append("     L1.CONFIDENTIAL_RPT02, ");
            sql.append("     L1.CONFIDENTIAL_RPT03, ");
            sql.append("     L1.CONFIDENTIAL_RPT04, ");
            sql.append("     L1.CONFIDENTIAL_RPT05, ");
            sql.append("     L1.CONFIDENTIAL_RPT06, ");
            sql.append("     L1.CONFIDENTIAL_RPT07, ");
            sql.append("     L1.CONFIDENTIAL_RPT08, ");
            sql.append("     L1.CONFIDENTIAL_RPT09, ");
            sql.append("     L1.CONFIDENTIAL_RPT10, ");
            sql.append("     L1.TOTAL3, ");
            sql.append("     L1.TOTAL5, ");
            sql.append("     L1.TOTAL_ALL, ");
            sql.append("     L1.KASANTEN_ALL, ");
            sql.append("     L1.ABSENCE_DAYS, ");
            sql.append("     L1.ABSENCE_DAYS2, ");
            sql.append("     L1.ABSENCE_DAYS3, ");
            sql.append("     L1.ABSENCE_REMARK, ");
            sql.append("     L1.ABSENCE_REMARK2, ");
            sql.append("     L1.ABSENCE_REMARK3, ");
            sql.append("     L1.REMARK1 AS CONFRPT_REMARK1, ");
            sql.append("     D4.REMARK1 AS DETAIL4_REMARK1, ");
            sql.append("     L013.NAME1 AS JUDGEMENT_INFO, ");
            sql.append("     T1.FS_ERACD ");
            sql.append(" FROM ");
            sql.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            sql.append(" LEFT OUTER JOIN ");
            sql.append("     ENTEXAM_APPLICANTADDR_DAT T2 ");
            sql.append(" ON ");
            sql.append("     T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ");
            sql.append("     T1.EXAMNO = T2.EXAMNO ");
            sql.append(" LEFT OUTER JOIN ");
            sql.append("     V_NAME_MST T3 ");
            sql.append(" ON ");
            sql.append("     T1.ENTEXAMYEAR = T3.YEAR AND ");
            sql.append("     T1.ERACD = T3.NAMECD2 AND ");
            sql.append("     T3.NAMECD1 = 'L007' ");
            sql.append(" LEFT OUTER JOIN ");
            sql.append("     ENTEXAM_APPLICANTBASE_DETAIL_DAT T4 ");
            sql.append(" ON ");
            sql.append("     T1.ENTEXAMYEAR = T4.ENTEXAMYEAR AND ");
            sql.append("     T1.EXAMNO = T4.EXAMNO AND ");
            sql.append("     T4.SEQ = '009' ");
            sql.append(" LEFT OUTER JOIN ");
            sql.append("     ENTEXAM_APPLICANTBASE_DETAIL_DAT T5 ");
            sql.append(" ON ");
            sql.append("     T1.ENTEXAMYEAR = T5.ENTEXAMYEAR AND ");
            sql.append("     T1.EXAMNO = T5.EXAMNO AND ");
            sql.append("     T5.SEQ = '001' ");
            //姉妹
            //母
            sql.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_014 ");
            sql.append("      ON BASE_D_014.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("     AND BASE_D_014.EXAMNO = T1.EXAMNO ");
            sql.append("     AND BASE_D_014.SEQ = '014' ");
            sql.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_015 ");
            sql.append("      ON BASE_D_015.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("     AND BASE_D_015.EXAMNO = T1.EXAMNO ");
            sql.append("     AND BASE_D_015.SEQ = '015' ");
            sql.append(" LEFT JOIN NAME_MST NML056 ON NML056.NAMECD1 = 'L056' ");
            sql.append("                          AND NML056.NAMECD2 = BASE_D_014.REMARK1 ");
            //併願校名、併願校合格発表日
            sql.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_016 ");
            sql.append("      ON BASE_D_016.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("     AND BASE_D_016.EXAMNO = T1.EXAMNO ");
            sql.append("     AND BASE_D_016.SEQ = '016' ");
            //事前番号
            sql.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_002 ");
            sql.append("      ON BASE_D_002.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("     AND BASE_D_002.EXAMNO = T1.EXAMNO ");
            sql.append("     AND BASE_D_002.SEQ = '002' ");
            //受験料
            sql.append(" LEFT JOIN ENTEXAM_MONEY_DAT M1 ");
            sql.append("      ON M1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("     AND M1.APPLICANTDIV = T1.APPLICANTDIV ");
            sql.append("     AND M1.EXAMNO = T1.EXAMNO ");
            sql.append(" LEFT OUTER JOIN ");
            sql.append("     ENTEXAM_APPLICANTBASE_DETAIL_DAT D4 ");
            sql.append(" ON ");
            sql.append("     T1.ENTEXAMYEAR = D4.ENTEXAMYEAR AND ");
            sql.append("     T1.EXAMNO = D4.EXAMNO AND ");
            sql.append("     D4.SEQ = '004' ");
            sql.append(" LEFT OUTER JOIN ");
            sql.append("     FINSCHOOL_MST FIN ");
            sql.append(" ON ");
            sql.append("     T1.FS_CD = FIN.FINSCHOOLCD ");
            sql.append(" LEFT OUTER JOIN ");
            sql.append("     ENTEXAM_APPLICANTCONFRPT_DAT L1 ");
            sql.append(" ON ");
            sql.append("     T1.ENTEXAMYEAR = L1.ENTEXAMYEAR AND ");
            sql.append("     T1.EXAMNO = L1.EXAMNO ");
            sql.append("     LEFT JOIN V_NAME_MST L013 ");
            sql.append("                  ON  L013.YEAR = T1.ENTEXAMYEAR ");
            sql.append("                  AND L013.NAMECD1 = 'L013' ");
            sql.append("                  AND L013.NAMECD2 = T1.JUDGEMENT ");
            sql.append("                  AND L013.NAMECD2 = '4' ");
            sql.append("      LEFT JOIN v_name_mst N0 ");
            sql.append("                   ON N0.year = T1.entexamyear AND N0.namecd2 = T1.applicantdiv AND N0.namecd1 = 'L003'");
            sql.append(" WHERE ");
            sql.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' AND ");
            sql.append("     T1.APPLICANTDIV = '2' ");//2:高校

            sql.append(" ORDER BY ");
            sql.append("     T1.EXAMNO ");

            return sql.toString();
        }

        private static String sqlShiganGouhiJ_KNJL090F(final Param param) {
            final StringBuffer sql = new StringBuffer();
            sql.append("  SELECT");
            sql.append("      T1.entexamyear,");
            sql.append("      T1.examno,");
            sql.append("      T1.name,");
            sql.append("      T1.name_kana,");

            sql.append("       /* 出身学校 */ ");
            sql.append("       FIN.FINSCHOOL_NAME AS FS_NAME, ");
            sql.append("       /* 事前番号 */ ");
            sql.append("       BD_002.REMARK1 AS RECRUIT_NO,  ");
            sql.append("       /* 塾名 */ ");
            sql.append("       TPSM.PRISCHOOL_NAME, ");
            sql.append("       /* 教室名 */ ");
            sql.append("       TPSCM.PRISCHOOL_NAME AS PRISCHOOL_CLASS_NAME, ");

            sql.append("      T1.birth_y,");
            sql.append("      T1.birth_m,");
            sql.append("      T1.birth_d,");
            sql.append("      T1.birthday,");
            sql.append("      T1.sex,");
            sql.append("      T1.applicantdiv,");
            sql.append("      N0.name1 AS applicantdivname,");
            sql.append("      T1.testdiv,");
            sql.append("      N1.name1 AS testdivname,");
            sql.append("      C1.REMARK8 || C1.REMARK9 || C1.REMARK10 AS EXAMCOURSE, ");
            sql.append("      C2.EXAMCOURSE_NAME AS EXAMCOURSE_NAME, ");
            sql.append("      T1.suc_coursecd || T1.suc_majorcd || T1.suc_coursecode as COURSEMAJOR,");
            sql.append("      E1.REMARK1 || E1.REMARK2 || E1.REMARK3 as ENTER_COURSEMAJOR,");
            sql.append("      E2.REMARK1 as ENTDIV2_DATE,");
            sql.append("      T1.judgement,");
            sql.append("      T6.name1 AS judgement_name,");
            sql.append("      T1.JUDGE_KIND,");
            sql.append("      N2.NAME1 AS JUDGE_KIND_NAME,");
            sql.append("      T1.SPECIAL_REASON_DIV, ");
            sql.append("      M1.ENTRANCE_FLG, ");
            sql.append("      M1.ENTRANCE_PAY_DIV, ");
            sql.append("      M1.ENTRANCE_PAY_DATE, ");
            sql.append("      M1.ENT_PAY_DIV, ");
            sql.append("      M1.ENT_PAY_DATE, ");
            sql.append("      M1.ENT_PAY_CHAK_DATE, ");
            sql.append("      M1.EXP_PAY_DIV, ");
            sql.append("      M1.EXP_PAY_DATE, ");
            sql.append("      M1.EXP_PAY_CHAK_DATE, ");
            sql.append("      T1.PROCEDUREDATE,");
            sql.append("      T1.procedurediv,");
            sql.append("      T1.entdiv,");
            sql.append("      T2.name1 AS sexname ");
            sql.append("  FROM");
            sql.append("      entexam_applicantbase_dat T1 ");

            sql.append("      /* 出身学校 */ ");
            sql.append("      LEFT JOIN FINSCHOOL_MST FIN  ");
            sql.append("                    ON T1.FS_CD = FIN.FINSCHOOLCD  ");
            sql.append("      /* 事前番号 */ ");
            sql.append("      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_002  ");
            sql.append("           ON BD_002.ENTEXAMYEAR  = T1.ENTEXAMYEAR  ");
            sql.append("           AND BD_002.EXAMNO      = T1.EXAMNO  ");
            sql.append("           AND BD_002.SEQ         = '002' ");
            sql.append("      /* 塾名 */ ");
            sql.append("      LEFT JOIN RECRUIT_DAT TRECR  ");
            sql.append("          ON TRECR.YEAR = BD_002.ENTEXAMYEAR  ");
            sql.append("          AND TRECR.RECRUIT_NO = BD_002.REMARK1  ");
            sql.append("      LEFT JOIN PRISCHOOL_MST TPSM ON TPSM.PRISCHOOLCD = TRECR.PRISCHOOLCD  ");
            sql.append("       ");
            sql.append("      /* 教室名 */ ");
            sql.append("      LEFT JOIN PRISCHOOL_CLASS_MST TPSCM ON TPSCM.PRISCHOOLCD = TRECR.PRISCHOOLCD  ");
            sql.append("          AND TPSCM.PRISCHOOL_CLASS_CD = TRECR.PRISCHOOL_CLASS_CD  ");

            sql.append("      LEFT JOIN v_name_mst T2 ");
            sql.append("                   ON T2.year = T1.entexamyear AND T2.namecd2 = T1.sex   AND T2.namecd1 = 'Z002' ");
            sql.append("      LEFT JOIN v_name_mst T6 ");
            sql.append("                   ON T6.year = T1.entexamyear AND T6.namecd2 = T1.judgement AND T6.namecd1 = 'L013'");
            sql.append("      LEFT JOIN v_name_mst N0 ");
            sql.append("                   ON N0.year = T1.entexamyear AND N0.namecd2 = T1.applicantdiv AND N0.namecd1 = 'L003'");
            sql.append("      LEFT JOIN v_name_mst N1 ");
            sql.append("                   ON N1.year = T1.entexamyear AND N1.namecd2 = T1.testdiv AND N1.namecd1 = 'L024'");
            sql.append("      LEFT JOIN v_name_mst N2 ");
            sql.append("                   ON N2.year = T1.entexamyear AND N2.namecd2 = T1.JUDGE_KIND AND N2.namecd1 = 'L025'");
            sql.append("      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT C1 ");
            sql.append("                   ON  C1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("                   AND C1.EXAMNO = T1.EXAMNO ");
            sql.append("                   AND C1.SEQ = '001' ");
            sql.append("      LEFT JOIN ENTEXAM_COURSE_MST C2 ");
            sql.append("                   ON  C2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("                   AND C2.APPLICANTDIV = T1.APPLICANTDIV ");
            sql.append("                   AND C2.TESTDIV = '1' ");
            sql.append("                   AND C2.COURSECD || C2.MAJORCD || C2.EXAMCOURSECD = C1.REMARK8 || C1.REMARK9 || C1.REMARK10 ");
            sql.append("      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT E1 ");
            sql.append("                   ON  E1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("                   AND E1.EXAMNO = T1.EXAMNO ");
            sql.append("                   AND E1.SEQ = '007' ");
            sql.append("      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT E2 ");
            sql.append("                   ON  E2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("                   AND E2.EXAMNO = T1.EXAMNO ");
            sql.append("                   AND E2.SEQ = '022' ");
            sql.append("      LEFT JOIN ENTEXAM_MONEY_DAT M1 ");
            sql.append("           ON M1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("          AND M1.APPLICANTDIV = T1.APPLICANTDIV ");
            sql.append("          AND M1.EXAMNO = T1.EXAMNO ");
            sql.append("  WHERE");
            sql.append("      T1.entexamyear = '" + param._entexamyear + "' AND ");
            sql.append("      T1.APPLICANTDIV = '1' ");//1:中学
            sql.append("  ORDER BY T1.EXAMNO ");
            return sql.toString();
        }

        public static String sqlShiganGouhiH_KNJL091F(final Param param) {
            final StringBuffer sql = new StringBuffer();
            sql.append("   SELECT ");
            sql.append("       T1.entexamyear, ");
            sql.append("       T1.examno, ");
            sql.append("       T1.name, ");
            sql.append("       T1.name_kana, ");

            sql.append("       /* 出身学校 */ ");
            sql.append("       FIN.FINSCHOOL_NAME AS FS_NAME, ");
            sql.append("       /* 事前番号 */ ");
            sql.append("       BD_002.REMARK1 AS RECRUIT_NO,  ");
            sql.append("       /* 塾名 */ ");
            sql.append("       TPSM.PRISCHOOL_NAME, ");
            sql.append("       /* 教室名 */ ");
            sql.append("       TPSCM.PRISCHOOL_NAME AS PRISCHOOL_CLASS_NAME, ");

            sql.append("       T1.birth_y, ");
            sql.append("       T1.birth_m, ");
            sql.append("       T1.birth_d, ");
            sql.append("       T1.birthday, ");
            sql.append("       T1.sex, ");
            sql.append("       T1.applicantdiv, ");
            sql.append("       N0.name1 AS applicantdivname, ");
            sql.append("       T1.testdiv, ");
            sql.append("       N1.name1 AS testdivname, ");
            sql.append("       C1.REMARK8 || C1.REMARK9 || C1.REMARK10 AS EXAMCOURSE,  ");
            sql.append("       C2.EXAMCOURSE_NAME AS EXAMCOURSE_NAME,  ");
            sql.append("       T1.suc_coursecd || T1.suc_majorcd || T1.suc_coursecode as COURSEMAJOR, ");
            sql.append("       E1.REMARK1 || E1.REMARK2 || E1.REMARK3 as ENTER_COURSEMAJOR, ");
            sql.append("       E2.REMARK1 as ENTDIV2_DATE, ");
            sql.append("       T1.judgement, ");
            sql.append("       T6.name1 AS judgement_name, ");
            sql.append("       T1.JUDGE_KIND, ");
            sql.append("       N2.NAME2 AS JUDGE_KIND_NAME, ");
            sql.append("       T1.SPECIAL_REASON_DIV,  ");
            sql.append("       M1.ENTRANCE_FLG,  ");
            sql.append("       M1.ENT_PAY_DIV,  ");
            sql.append("       M1.ENT_PAY_DATE,  ");
            sql.append("       M1.ENT_PAY_CHAK_DATE,  ");
            sql.append("       M1.EXP_PAY_DIV,  ");
            sql.append("       M1.EXP_PAY_DATE,  ");
            sql.append("       M1.EXP_PAY_CHAK_DATE,  ");
            sql.append("       T1.PROCEDUREDATE, ");
            sql.append("       T1.procedurediv, ");
            sql.append("       T1.entdiv, ");
            sql.append("       T2.name1 AS sexname  ");
            sql.append("   FROM ");
            sql.append("       entexam_applicantbase_dat T1  ");

            sql.append("      /* 出身学校 */ ");
            sql.append("      LEFT JOIN FINSCHOOL_MST FIN  ");
            sql.append("                    ON T1.FS_CD = FIN.FINSCHOOLCD  ");
            sql.append("      /* 事前番号 */ ");
            sql.append("      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_002  ");
            sql.append("           ON BD_002.ENTEXAMYEAR  = T1.ENTEXAMYEAR  ");
            sql.append("           AND BD_002.EXAMNO      = T1.EXAMNO  ");
            sql.append("           AND BD_002.SEQ         = '002' ");
            sql.append("            ");
            sql.append("      /* 塾名 */ ");
            sql.append("      LEFT JOIN RECRUIT_DAT TRECR  ");
            sql.append("          ON TRECR.YEAR = BD_002.ENTEXAMYEAR  ");
            sql.append("          AND TRECR.RECRUIT_NO = BD_002.REMARK1  ");
            sql.append("      LEFT JOIN PRISCHOOL_MST TPSM ON TPSM.PRISCHOOLCD = TRECR.PRISCHOOLCD  ");
            sql.append("       ");
            sql.append("      /* 教室名 */ ");
            sql.append("      LEFT JOIN PRISCHOOL_CLASS_MST TPSCM ON TPSCM.PRISCHOOLCD = TRECR.PRISCHOOLCD  ");
            sql.append("          AND TPSCM.PRISCHOOL_CLASS_CD = TRECR.PRISCHOOL_CLASS_CD  ");

            sql.append("       LEFT JOIN v_name_mst T2  ");
            sql.append("                    ON T2.year = T1.entexamyear AND T2.namecd2 = T1.sex   AND T2.namecd1 = 'Z002'  ");
            sql.append("       LEFT JOIN v_name_mst T6  ");
            sql.append("                    ON T6.year = T1.entexamyear AND T6.namecd2 = T1.judgement AND T6.namecd1 = 'L013' ");
            sql.append("       LEFT JOIN v_name_mst N0  ");
            sql.append("                    ON N0.year = T1.entexamyear AND N0.namecd2 = T1.applicantdiv AND N0.namecd1 = 'L003' ");
            sql.append("       LEFT JOIN v_name_mst N1  ");
            sql.append("                    ON N1.year = T1.entexamyear AND N1.namecd2 = T1.testdiv AND N1.namecd1 = 'L004' ");
            sql.append("       LEFT JOIN v_name_mst N2  ");
            sql.append("                    ON N2.year = T1.entexamyear AND N2.namecd2 = T1.JUDGE_KIND AND N2.namecd1 = 'L025' ");
            sql.append("       LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT C1  ");
            sql.append("                    ON  C1.ENTEXAMYEAR = T1.ENTEXAMYEAR  ");
            sql.append("                    AND C1.EXAMNO = T1.EXAMNO  ");
            sql.append("                    AND C1.SEQ = '001'  ");
            sql.append("       LEFT JOIN ENTEXAM_COURSE_MST C2  ");
            sql.append("                    ON  C2.ENTEXAMYEAR = T1.ENTEXAMYEAR  ");
            sql.append("                    AND C2.APPLICANTDIV = T1.APPLICANTDIV  ");
            sql.append("                    AND C2.TESTDIV = '1'  ");
            sql.append("                    AND C2.COURSECD || C2.MAJORCD || C2.EXAMCOURSECD = C1.REMARK8 || C1.REMARK9 || C1.REMARK10  ");
            sql.append("       LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT E1  ");
            sql.append("                    ON  E1.ENTEXAMYEAR = T1.ENTEXAMYEAR  ");
            sql.append("                    AND E1.EXAMNO = T1.EXAMNO  ");
            sql.append("                    AND E1.SEQ = '007'  ");
            sql.append("       LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT E2  ");
            sql.append("                    ON  E2.ENTEXAMYEAR = T1.ENTEXAMYEAR  ");
            sql.append("                    AND E2.EXAMNO = T1.EXAMNO  ");
            sql.append("                    AND E2.SEQ = '022'  ");
            sql.append("       LEFT JOIN ENTEXAM_MONEY_DAT M1  ");
            sql.append("            ON M1.ENTEXAMYEAR = T1.ENTEXAMYEAR  ");
            sql.append("           AND M1.APPLICANTDIV = T1.APPLICANTDIV  ");
            sql.append("           AND M1.EXAMNO = T1.EXAMNO  ");
            sql.append("   WHERE ");
            sql.append("      T1.entexamyear = '" + param._entexamyear + "' AND ");
            sql.append("      T1.APPLICANTDIV = '2' ");//2:高校
            sql.append("   ORDER BY T1.EXAMNO  ");

            return sql.toString();
        }

        private static String receptSql(final Param param) {
            final StringBuffer sql = new StringBuffer();
            sql.append("  SELECT");
            sql.append("      T1.* ");
            sql.append("      , TRDET003.REMARK1 AS TESTDIV0 ");
            sql.append("  FROM");
            sql.append("      V_ENTEXAM_RECEPT_DAT T1 ");
            sql.append("      LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT TRDET003 ON TRDET003.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            sql.append("         AND TRDET003.APPLICANTDIV = T1.APPLICANTDIV ");
            sql.append("         AND TRDET003.TESTDIV = T1.TESTDIV ");
            sql.append("         AND TRDET003.EXAM_TYPE = T1.EXAM_TYPE ");
            sql.append("         AND TRDET003.RECEPTNO = T1.RECEPTNO ");
            sql.append("         AND TRDET003.SEQ = '003' ");
            sql.append("  WHERE");
            sql.append("      T1.entexamyear = '" + param._entexamyear + "' AND ");
            sql.append("      T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            return sql.toString();
        }

        //得点データ取得
        private static String scoreSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.RECEPTNO, ");
            stb.append("     T2.TESTDIV, ");
            stb.append("     T2.TESTSUBCLASSCD, ");
            stb.append("     T2.ATTEND_FLG, ");
            stb.append("     T2.SCORE ");
            stb.append(" FROM ");
            stb.append("     V_ENTEXAM_RECEPT_DAT T1 ");
            stb.append("     INNER JOIN ENTEXAM_SCORE_DAT T2 ");
            stb.append("         ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
            stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T2.TESTDIV      = T1.TESTDIV ");
            stb.append("         AND T2.EXAM_TYPE    = T1.EXAM_TYPE ");
            stb.append("         AND T2.RECEPTNO     = T1.RECEPTNO ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR  = '" + param._entexamyear + "' AND ");
            stb.append("     T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T2.TESTSUBCLASSCD ");
            return stb.toString();
        }

    }

    private static class Recruit {

        private static String RECRUIT_NO = "RECRUIT_NO";

        String _recruitNo;

        Map _recruitDat;
        List _eventList = new ArrayList();
        List _consultList = new ArrayList();
        Map _visitDat; // 高校のみ
        List _visitScoreList;  // 高校のみ
        List _visitMockList; // 高校のみ
        List _visitActiveList; // 高校のみ

        private static List getRecruitList(final DB2UDB db2, final Param param) {
            final List recruitList = new ArrayList();
            final Map recruitMap = new HashMap();

            for (final Iterator it = query(db2, sqlRecuitDat(param), "recruit").iterator(); it.hasNext();) {
                final Map dat = (Map) it.next();
                final Recruit r = new Recruit();
                r._recruitNo = getString(dat, RECRUIT_NO);
                r._recruitDat = dat;
                r._recruitDat.put("CONSULT_WRAPUP_REMARK", null);
                r._recruitDat.put("SHOKATSUDOU_POINT_TOTAL", null);
                recruitList.add(r);
                recruitMap.put(r._recruitNo, r);
            }

            for (final Iterator it = query(db2, sqlRecuitEventDat(param), "recruit-event").iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                final Recruit r = (Recruit) recruitMap.get(getString(m, RECRUIT_NO));
                if (null != r) {
                    r._eventList.add(m);
                }
            }
            for (final Iterator it = queryAsMap(db2, sqlRecuitConsultWrapupDat(param), "RECRUIT_NO", "REMARK").entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final String recruitNo = (String) e.getKey();
                final Recruit r = (Recruit) recruitMap.get(recruitNo);
                if (null != r) {
                    r._recruitDat.put("CONSULT_WRAPUP_REMARK", e.getValue());
                }
            }

            for (final Iterator it = query(db2, sqlRecuitConsultDat(param), "recruit-consult").iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                final Recruit r = (Recruit) recruitMap.get(getString(m, RECRUIT_NO));
                if (null != r) {
                    r._consultList.add(m);
                }
            }

            if ("2".equals(param._applicantdiv)) {

                for (final Iterator it = recruitMap.values().iterator(); it.hasNext();) {
                    final Recruit r = (Recruit) it.next();
                    r._visitDat = new HashMap();
                    r._visitActiveList = new ArrayList();
                    r._visitScoreList = new ArrayList();
                    r._visitMockList = new ArrayList();
                }

                for (final Iterator it = queryAsMap(db2, sqlRecuitVisitDat(param), "RECRUIT_NO", null).entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final String recruitNo = (String) e.getKey();
                    final Recruit r = (Recruit) recruitMap.get(recruitNo);
                    if (null != r) {
                        r._visitDat = (Map) e.getValue();
                    }
                }

                for (final Iterator it = query(db2, sqlRecuitVisitScoreDat(param), "visit-score").iterator(); it.hasNext();) {
                    final Map m = (Map) it.next();
                    final Recruit r = (Recruit) recruitMap.get(getString(m, RECRUIT_NO));
                    if (null != r) {
                        r._visitScoreList.add(m);
                    }
                }

                for (final Iterator it = query(db2, sqlRecuitVisitMockDat(param), "visit-mock").iterator(); it.hasNext();) {
                    final Map m = (Map) it.next();
                    final Recruit r = (Recruit) recruitMap.get(getString(m, RECRUIT_NO));
                    if (null != r) {
                        r._visitMockList.add(m);
                    }
                }

                for (final Iterator it = query(db2, sqlRecuitVisitActiveDat(param), "visit-active").iterator(); it.hasNext();) {
                    final Map m = (Map) it.next();
                    final Recruit r = (Recruit) recruitMap.get(getString(m, RECRUIT_NO));
                    if (null != r) {
                        r._visitActiveList.add(m);
                    }
                }

                final List d2List = new ArrayList();
                for (int i = 0; i < param._nameMstL408List.size(); i++) {
                    final Map l408 = (Map) param._nameMstL408List.get(i);
                    final D2Fact recVisitActive1 = new D2Fact() {
                        Map getMap(final Recruit r) { return r.getVisitActiveMap("1", getString(l408, "NAMECD2"));}
                    };
                    Convert convert = new Convert() { String convert(final String[] vs) { return (null != vs[0] ? getString(l408, "NAMESPARE1") : null);}};
                    d2List.add(recVisitActive1.newD(null, a("REMARK1"), convert));
                }

                for (final Iterator it = recruitMap.values().iterator(); it.hasNext();) {
                    final Recruit r = (Recruit) it.next();
                    String pointTotalStr = null;
                    for (final Iterator d2it = d2List.iterator(); d2it.hasNext();) {
                        final D d2 = (D) d2it.next();
                        final String point = (String) d2.val(r);
                        if (NumberUtils.isDigits(point)) {
                            int pointTotal = null == pointTotalStr ? 0 : Integer.parseInt(pointTotalStr);
                            pointTotalStr = String.valueOf(pointTotal + Integer.parseInt(point));
                        }
                    }
                    r._recruitDat.put("SHOKATSUDOU_POINT_TOTAL", pointTotalStr);
                }
            }

            return recruitList;
        }

        public Map getVisitActiveMap(final String seqDiv, final String seq) {
            if (null == _visitActiveList) {
                return null;
            }
            for (int i = 0; i < _visitActiveList.size(); i++) {
                final Map visitActive = (Map) _visitActiveList.get(i);
                if (getString(visitActive, "SEQ_DIV").equals(seqDiv) && getString(visitActive, "SEQ").equals(seq)) {
                    return visitActive;
                }
            }
            return null;
        }

        public Map getVisitMockMap(final String month) {
            if (null == _visitMockList) {
                return null;
            }
            for (int i = 0; i < _visitMockList.size(); i++) {
                final Map visitMock = (Map) _visitMockList.get(i);
                if (getString(visitMock, "MONTH").equals(month)) {
                    return visitMock;
                }
            }
            return null;
        }

        public Map getVisitScoreMap(final String semester) {
            if (null == _visitScoreList) {
                return null;
            }
            for (int i = 0; i < _visitScoreList.size(); i++) {
                final Map visitScore = (Map) _visitScoreList.get(i);
                if (getString(visitScore, "SEMESTER").equals(semester)) {
                    return visitScore;
                }
            }
            return null;
        }

        private static String sqlRecuitDat(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("  T1.* ");
            stb.append("  , T2.FINSCHOOL_NAME ");
            stb.append("  , T3.PRISCHOOL_NAME ");
            stb.append("  , T4.PRISCHOOL_NAME AS PRISCHOOL_CLASS_NAME ");
            stb.append("  , CASE T1.GRADE WHEN '01' THEN '小1' ");
            stb.append("                  WHEN '02' THEN '小2' ");
            stb.append("                  WHEN '03' THEN '小3' ");
            stb.append("                  WHEN '04' THEN '小4' ");
            stb.append("                  WHEN '05' THEN '小5' ");
            stb.append("                  WHEN '06' THEN '小6' ");
            stb.append("                  WHEN '07' THEN '中1' ");
            stb.append("                  WHEN '08' THEN '中2' ");
            stb.append("                  WHEN '09' THEN '中3' ");
            stb.append("                  WHEN '10' THEN '高1' ");
            stb.append("                  WHEN '11' THEN '高2' ");
            stb.append("                  WHEN '12' THEN '高3' ");
            stb.append("    END AS GRADE_NAME ");
            stb.append("  , NMZ002.ABBV1 AS SEX_NAME ");
            stb.append(" FROM RECRUIT_DAT T1  ");
            stb.append(" LEFT JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T1.FINSCHOOLCD ");
            stb.append(" LEFT JOIN PRISCHOOL_MST T3 ON T3.PRISCHOOLCD = T1.PRISCHOOLCD ");
            stb.append(" LEFT JOIN PRISCHOOL_CLASS_MST T4 ON T4.PRISCHOOLCD = T1.PRISCHOOLCD ");
            stb.append("     AND T4.PRISCHOOL_CLASS_CD = T1.PRISCHOOL_CLASS_CD ");
            stb.append(" LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T1.SEX ");
            stb.append(" WHERE T1.YEAR = '" + param._entexamyear + "' ");
            stb.append("     AND T1.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.RECRUIT_NO ");
            return stb.toString();
        }

        private static String sqlRecuitEventDat(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("       T1.* ");
            stb.append("     , T2.EVENT_CLASS_NAME  ");
            stb.append("     , T3.EVENT_NAME  ");
            stb.append("     , NML401.NAME1 AS MEDIA_NAME  ");
            stb.append("     , NML402.NAME1 AS STATE_NAME  ");
            stb.append(" FROM RECRUIT_EVENT_DAT T1 ");
            stb.append(" INNER JOIN RECRUIT_DAT T0 ON T0.YEAR = T1.YEAR ");
            stb.append("     AND T0.RECRUIT_NO = T1.RECRUIT_NO ");
            stb.append("     AND T0.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append(" INNER JOIN RECRUIT_CLASS_MST T2 ON T2.EVENT_CLASS_CD = T1.EVENT_CLASS_CD ");
            stb.append(" INNER JOIN RECRUIT_EVENT_YMST T3 ON T3.YEAR = T1.YEAR ");
            stb.append("     AND T3.EVENT_CLASS_CD = T1.EVENT_CLASS_CD ");
            stb.append("     AND T3.EVENT_CD = T1.EVENT_CD ");
            stb.append(" LEFT JOIN NAME_MST NML401 ON NML401.NAMECD1 = 'L401' ");
            stb.append("     AND NML401.NAMECD2 = T1.MEDIA_CD ");
            stb.append(" LEFT JOIN NAME_MST NML402 ON NML402.NAMECD1 = 'L402' ");
            stb.append("     AND NML402.NAMECD2 = T1.STATE_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._entexamyear + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.RECRUIT_NO ");
            stb.append("   , T1.TOUROKU_DATE ");
            stb.append("   , T1.EVENT_CLASS_CD ");
            stb.append("   , T1.EVENT_CD ");
            stb.append("   , T1.MEDIA_CD ");
            stb.append("   , T1.STATE_CD ");
            return stb.toString();
        }

        private static String sqlRecuitConsultWrapupDat(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("       T1.* ");
            stb.append(" FROM RECRUIT_CONSULT_WRAPUP_DAT T1 ");
            stb.append(" INNER JOIN RECRUIT_DAT T0 ON T0.YEAR = T1.YEAR ");
            stb.append("     AND T0.RECRUIT_NO = T1.RECRUIT_NO ");
            stb.append("     AND T0.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._entexamyear + "' ");
            return stb.toString();
        }

        private static String sqlRecuitConsultDat(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append("   , T2.STAFFNAME ");
            stb.append("   , T3.NAME1 AS CONSULT_NAME ");
            stb.append("   , T4.NAME1 AS METHOD_NAME ");
            stb.append(" FROM RECRUIT_CONSULT_DAT T1 ");
            stb.append(" INNER JOIN RECRUIT_DAT T0 ON T0.YEAR = T1.YEAR ");
            stb.append("     AND T0.RECRUIT_NO = T1.RECRUIT_NO ");
            stb.append("     AND T0.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append(" LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
            stb.append(" LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'L404' AND T3.NAMECD2 = T1.CONSULT_CD ");
            stb.append(" LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'L405' AND T4.NAMECD2 = T1.METHOD_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._entexamyear + "' ");
            stb.append(" ORDER BY ");
            stb.append("      T1.RECRUIT_NO ");
            stb.append("    , T1.TOUROKU_DATE ");
            stb.append("    , T1.CONSULT_CD ");
            stb.append("    , T1.METHOD_CD ");

            return stb.toString();
        }

        private static String sqlRecuitVisitDat(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append("   , T2.STAFFNAME ");
            stb.append(" FROM RECRUIT_VISIT_DAT T1 ");
            stb.append(" INNER JOIN RECRUIT_DAT T0 ON T0.YEAR = T1.YEAR ");
            stb.append("     AND T0.RECRUIT_NO = T1.RECRUIT_NO ");
            stb.append("     AND T0.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append(" LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._entexamyear + "' ");
            return stb.toString();
        }

        private static String sqlRecuitVisitActiveDat(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("       T1.* ");
            stb.append(" FROM RECRUIT_VISIT_ACTIVE_DAT T1 ");
            stb.append(" INNER JOIN RECRUIT_DAT T0 ON T0.YEAR = T1.YEAR ");
            stb.append("     AND T0.RECRUIT_NO = T1.RECRUIT_NO ");
            stb.append("     AND T0.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._entexamyear + "' ");
            stb.append("     AND (T1.REMARK1 IS NOT NULL OR T1.REMARK2 IS NOT NULL) ");
            return stb.toString();
        }

        private static String sqlRecuitVisitScoreDat(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("       T1.* ");
            stb.append(" FROM RECRUIT_VISIT_SCORE_DAT T1 ");
            stb.append(" INNER JOIN RECRUIT_DAT T0 ON T0.YEAR = T1.YEAR ");
            stb.append("     AND T0.RECRUIT_NO = T1.RECRUIT_NO ");
            stb.append("     AND T0.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._entexamyear + "' ");
            return stb.toString();
        }

        private static String sqlRecuitVisitMockDat(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("       T1.* ");
            stb.append(" FROM RECRUIT_VISIT_MOCK_DAT T1 ");
            stb.append(" INNER JOIN RECRUIT_DAT T0 ON T0.YEAR = T1.YEAR ");
            stb.append("     AND T0.RECRUIT_NO = T1.RECRUIT_NO ");
            stb.append("     AND T0.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._entexamyear + "' ");
            return stb.toString();
        }

    }

    private static class TestDivDat {
        final String _testdiv;
        final String _testdiv0;
        final String _name1;
        final String _date;
        public TestDivDat(final String testDiv, final String testdiv0, final String name1, final String date) {
            _testdiv = testDiv;
            _testdiv0 = testdiv0;
            _name1 = name1;
            _date = date;
        }
    }

    private static class Param {
        final String _entexamyear;
        final String _loginDate;
        final String _applicantdiv;
        final String _output; // 1:志願者データ 2:募集企画データ
        final String _output1div; // 志願者データ 1:基礎データ 2:合否データ
        final String _cmd;
        final String _ctrlYear;
        final String _ctrlSemester;

        final String _schoolKind;
        final String _applicantdivname;

        final List<Map<String, String>> _testdivArray;
        final List<TestDivDat> _testdivList;

        final Map _coursemajorMap;
        final Map _examCoursecodeMap;
        final Map _nameMstA023Map;
        final Map _nameMstH201Map;
        final Map _nameMstL004Map;
        final Map _nameMstL006Map;
        final Map _nameMstL007Map;
        final List _nameMstL008List;
        final List<Map<String, String>> _nameMstL009List;
        final Map _nameMstL009Map;
        final Map _nameMstL011Map;
        final Map _nameMstL012Map;
        final Map _nameMstL013Map;
        final Map _nameMstL015Map;
        final Map _nameMstL016Map;
        final Map _nameMstL025Name2Map;
        final Map _nameMstL034Map;
        final Map _nameMstL406Map;
        final Map _nameMstL407Map;
        final List _nameMstL408List;
        final Map _hrnameMap;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _output = request.getParameter("OUTPUT");
            _output1div = request.getParameter("OUTPUT1DIV");
            _cmd = request.getParameter("cmd");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");

            _schoolKind = "1".equals(_applicantdiv) ? "J" : "H";
            _applicantdivname = getApplicantdivName(db2);

            _testdivArray = query(db2, getVNamemst("L024"), "L024");
            _testdivList = getTestdivList(db2);

            _coursemajorMap = queryAsMap(db2, getCourseMajorMst(), "VALUE", "LABEL");
            _examCoursecodeMap = queryAsMap(db2, getExamCoursecdMst(), "VALUE", "LABEL");
            _nameMstA023Map = queryAsMap(db2, getVNamemst("A023"), "NAME1", "ABBV1");
            _nameMstH201Map = queryAsMap(db2, getVNamemst("H201"), "NAMECD2", "NAME1");
            _nameMstL004Map = queryAsMap(db2, getVNamemst("L004"), "NAMECD2", "NAME1");
            _nameMstL006Map = queryAsMap(db2, getVNamemst("L006"), "NAMECD2", "NAME1");
            _nameMstL007Map = queryAsMap(db2, getVNamemst("L007"), "NAMECD2", "NAME1");
            _nameMstL008List = query(db2, getVNamemst("L008"), "L008");
            _nameMstL009List = query(db2, getVNamemst("L009"), "L009");
            _nameMstL009Map = queryAsMap(db2, getVNamemst("L009"), "NAMECD2", "NAME1");
            _nameMstL011Map = queryAsMap(db2, getVNamemst("L011"), "NAMECD2", "NAME1");
            _nameMstL012Map = queryAsMap(db2, getVNamemst("L012"), "NAMECD2", "NAME1");
            _nameMstL013Map = queryAsMap(db2, getVNamemst("L013"), "NAMECD2", "NAME1");
            _nameMstL015Map = queryAsMap(db2, getVNamemst("L015"), "NAMECD2", "NAME1");
            _nameMstL016Map = queryAsMap(db2, getVNamemst("L016"), "NAMECD2", "NAME1");
            _nameMstL025Name2Map = queryAsMap(db2, getVNamemst("L025"), "NAMECD2", "NAME2");
            _nameMstL034Map = queryAsMap(db2, getVNamemst("L034"), "NAMECD2", "NAME1");
            _nameMstL406Map = queryAsMap(db2, getVNamemst("L406"), "NAMECD2", "NAME1");
            _nameMstL407Map = queryAsMap(db2, getVNamemst("L407"), "NAMECD2", "NAME1");
            _nameMstL408List = query(db2, getVNamemst("L408"), "L408");
            _hrnameMap = queryAsMap(db2, sqlHrname(), "CD", "LABEL");
        }

		public String getA023(String schoolKind) {
        	if (null == schoolKind) {
        		return "";
        	}
        	String val = defstr(getString(_nameMstA023Map, schoolKind));
        	if (null == val) {
        		log.warn(" no schoolKind in " + _nameMstA023Map);
        	} else {
        		log.debug(" schoolKind (" + schoolKind + ") = " + val);
        	}
			return val;
        }

		private String sqlHrname() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT GRADE || '-' || HR_CLASS AS CD, HR_NAME AS LABEL FROM SCHREG_REGD_HDAT WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _ctrlSemester + "' ");
            return stb.toString();
        }

        private String getApplicantdivName(DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantdiv + "'");
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


        private String getVNamemst(final String namecd1) {
            final String orderBy = "L024".equals(namecd1) ? "int(NAMECD2)" : "NAMECD2";
            final String sql = " SELECT * FROM V_NAME_MST WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = '" + namecd1 + "' ORDER BY " + orderBy;
            return sql;
        }

        private String getCourseMajorMst() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COURSECD || MAJORCD AS VALUE, ");
            stb.append("     COURSENAME || MAJORNAME AS LABEL ");
            stb.append(" FROM ");
            stb.append("     V_COURSE_MAJOR_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _entexamyear + "' ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE ");
            return stb.toString();
        }

        //コース取得
        private String getExamCoursecdMst() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD AS VALUE, ");
            stb.append("     T1.EXAMCOURSE_NAME AS LABEL ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_COURSE_MST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND ");
            stb.append("     '" + _applicantdiv + "' = T1.APPLICANTDIV AND ");
            stb.append("     T1.TESTDIV      = '1' ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE ");

            return stb.toString();
        }

        /* 入試区分、入試回数（高校のみ）  */
        private List getTestdivList(final DB2UDB db2) {
            String sql = "2".equals(_applicantdiv) ? getTestdivListHSql() : getTestdivListJSql();

            final List testdivList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                log.info(" testdiv sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final TestDivDat d = new TestDivDat(rs.getString("TESTDIV"), rs.getString("TESTDIV0"), rs.getString("NAME1"), rs.getString("DATE"));
                    testdivList.add(d);
                    log.info(" testdiv =" + d._testdiv + ", testdate = (" + rs.getString("DATE") + ")");
                }
            } catch (Exception e) {
                log.error("exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testdivList;
        }

        private String getTestdivListHSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_DATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         NAMECD2 AS TESTDIV, ");
            stb.append("         '1' AS TESTDIV0, ");
            stb.append("         NAME1, ");
            stb.append("         NAMESPARE1 AS DATE ");
            stb.append("     FROM ");
            stb.append("         V_NAME_MST ");
            stb.append("     WHERE ");
            stb.append("         YEAR        = '" + _entexamyear + "' ");
            stb.append("         AND NAMECD1 = 'L004' ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT ");
            stb.append("         NAMECD2 AS TESTDIV, ");
            stb.append("         '2' AS TESTDIV0, ");
            stb.append("         NAME1, ");
            stb.append("         NAME3 AS DATE ");
            stb.append("     FROM ");
            stb.append("         V_NAME_MST ");
            stb.append("     WHERE ");
            stb.append("         YEAR        = '" + _entexamyear + "' ");
            stb.append("         AND NAMECD1 = 'L004' ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT ");
            stb.append("         NAMECD2 AS TESTDIV, ");
            stb.append("         '3' AS TESTDIV0, ");
            stb.append("         NAME1, ");
            stb.append("         NAMESPARE1 AS DATE ");
            stb.append("     FROM ");
            stb.append("         V_NAME_MST ");
            stb.append("     WHERE ");
            stb.append("         YEAR = '" + _entexamyear + "' ");
            stb.append("         AND NAMECD1 = 'L044' ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT ");
            stb.append("         NAMECD2 AS TESTDIV, ");
            stb.append("         '4' AS TESTDIV0, ");
            stb.append("         NAME1, ");
            stb.append("         NAME3 AS DATE ");
            stb.append("     FROM ");
            stb.append("         V_NAME_MST ");
            stb.append("     WHERE ");
            stb.append("         YEAR = '" + _entexamyear + "' ");
            stb.append("         AND NAMECD1 = 'L044' ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT ");
            stb.append("         NAMECD2 AS TESTDIV, ");
            stb.append("         '5' AS TESTDIV0, ");
            stb.append("         NAME1, ");
            stb.append("         NAMESPARE1 AS DATE ");
            stb.append("     FROM ");
            stb.append("         V_NAME_MST ");
            stb.append("     WHERE ");
            stb.append("         YEAR = '" + _entexamyear + "' ");
            stb.append("         AND NAMECD1 = 'L059' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     TESTDIV, ");
            stb.append("     TESTDIV0, ");
            stb.append("     NAME1, ");
            stb.append("     DATE ");
            stb.append(" FROM ");
            stb.append("     T_DATE ");
            stb.append(" WHERE ");
            stb.append("     DATE IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("     TESTDIV, ");
            stb.append("     TESTDIV0 ");
            return stb.toString();
        }

        private String getTestdivListJSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_DATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         NAMECD2 AS TESTDIV, ");
            stb.append("         '1' AS TESTDIV0, ");
            stb.append("         NAME1, ");
            stb.append("         NAMESPARE1 AS DATE ");
            stb.append("     FROM ");
            stb.append("         V_NAME_MST ");
            stb.append("     WHERE ");
            stb.append("         YEAR        = '" + _entexamyear + "' ");
            stb.append("         AND NAMECD1 = 'L024' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     TESTDIV, ");
            stb.append("     TESTDIV0, ");
            stb.append("     NAME1, ");
            stb.append("     DATE ");
            stb.append(" FROM ");
            stb.append("     T_DATE ");
            stb.append(" WHERE ");
            stb.append("     DATE IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("     int(TESTDIV) ");
            return stb.toString();
        }

    }
}// クラスの括り
