// kanji=漢字
/*
 * $Id: 79c0248dfb85c6410bc4761893db243e67b38881 $
 *
 * 作成日: 2009/10/21 8:52:18 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * <<クラスの説明>>。
 *
 * @author nakamoto
 * @version $Id: 79c0248dfb85c6410bc4761893db243e67b38881 $
 */
public class KNJD183A {

    private static final Log log = LogFactory.getLog(KNJD183A.class);

    private static final String SEMEALL = "9";
    private static final String VALUE_DI_KESSHI = "*";
    private static final String VALUE_DI_KOUKETSU = "**";

    private boolean _hasData = false;

    private Param _param;

    private static String _010101_CHUKAN = "010101";  // 中間
    private static String _020101_KIMATSU = "020101"; // 期末
    private static String _990002_HEIJO = "990002";   // 平常点
    private static String _990008_HYOKA = "990008";   // 学期末
    private static String _990009_HYOTEI = "990009";  // 到達度

    private static int GAPPEISAKI = -1;
    private static int GAPPEIMOTO = 1;
    private static int GAPPEINASI = 0;

    private static final String SELECT_CLASSCD_UNDER = "89";

    /**
     * @param request
     *            リクエスト
     * @param response
     *            レスポンス
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        log.fatal("$Id: 79c0248dfb85c6410bc4761893db243e67b38881 $");
        KNJServletUtils.debugParam(request, log);

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;

        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);
            log.debug("年組：" + _param._classSelected);

            final Form form = new Form(svf, _param);

            for (int i = 0; i < _param._classSelected.length; i++) {

                final List<Student> studentList = Student.load(db2, _param, _param._classSelected[i]);

                log.info(" gradeHrclass " + _param._classSelected[i] + " size = " + studentList.size());

                // 印刷処理
                if (_param._isTate) {
                    form.printMainTate(db2, studentList);
                } else {
                    form.printMainYoko(db2, studentList);
                }
            }

            if (form._hasData) {
                _hasData = true;
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != _param) {
                _param.close();
            }
            closeDb(db2);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }

    }

    private static class Form {

        //考査名称設定
        final String _testnameA = "中間試験";
        final String _testnameB = "期末試験";
        final String _testnameC = "平常点";
        final String _testnameD = "学期成績";

        final Vrw32alp svf;
        final Param _param;
        boolean _hasData;
        String _currentForm;
        Map<String, SvfField> _fieldMap;
        Form(final Vrw32alp svf, final Param param) {
            this.svf = svf;
            _param = param;
        }

        private void setForm(String form, final int n) {
            form = modifyForm0(form);
            svf.VrSetForm(form, 4); //縦型
            if (null == _currentForm || !_currentForm.equals(form)) {
                _fieldMap = SvfField.getSvfFormFieldInfoMapGroupByName(svf);
                _currentForm = form;
                log.info(" set form " + form);
            }
        }

        private String modifyForm0(final String form) {

            String formCreateFlg = "8KETA";
            if (_param._isOutputDebug) {
                log.info(" form config Flg = " + formCreateFlg);
            }
            if (StringUtils.isEmpty(formCreateFlg)) {
                return form;
            }
            formCreateFlg = form + "::" + formCreateFlg;
            if (null != _param._createFormFileMap.get(formCreateFlg)) {
                return _param._createFormFileMap.get(formCreateFlg).getName();
            }
            try {
                final SvfForm svfForm = new SvfForm(new File(svf.getPath(form)));
                if (svfForm.readFile()) {

                    modifySvfForm(svfForm);

                    final File newFormFile = svfForm.writeTempFile();
                    _param._createFormFileMap.put(formCreateFlg, newFormFile);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }

            File newFormFile = _param._createFormFileMap.get(formCreateFlg);
            if (null != newFormFile) {
                return newFormFile.getName();
            }
            return form;
        }

        protected boolean modifySvfForm(final SvfForm svfForm) {
            for (final SvfForm.Field field : svfForm.getElementList(SvfForm.Field.class)) {
                if (field._fieldname.startsWith("SCORE")) {
                    if (field._fieldLength < 8) {
                        svfForm.addField(field.copyTo(field._fieldname + "_8KETA").setFieldLength(8).setCharPoint10(field._charPoint10 - 1));
                    }
                }
            }
            return true;
        }

        private int VrsOutScore(final String fieldname, final String data) {
            if (null != fieldname) {
                SvfField field = _fieldMap.get(fieldname);
                if (null != field && field._fieldLength < KNJ_EditEdit.getMS932ByteLength(data) && fieldname.startsWith("SCORE")) {
                    field = _fieldMap.get(fieldname + "_8KETA");
                    if (null != field) {
                        return VrsOut(fieldname + "_8KETA", data);
                    }
                }
            }
            return VrsOut(fieldname, data);
        }

        private int VrsOut(final String fieldname, final String data) {
            if (null == fieldname) {
                return -1;
            }
            if (!_fieldMap.containsKey(fieldname)) {
                if (!_param.logOnce.contains(fieldname + data)) {
                    log.warn("no such field : " + fieldname + ", data = " + data);
                    _param.logOnce.add(fieldname + data);
                }
                return -1;
            }
            return svf.VrsOut(fieldname, data);
        }

        private int VrsOutn(final String fieldname, final int gyo, final String data) {
            if (null == fieldname) {
                return -1;
            }
            if (!_fieldMap.containsKey(fieldname)) {
                if (!_param.logOnce.contains(fieldname + gyo)) {
                    log.warn("no such field : " + fieldname + "(" + gyo + ")");
                    _param.logOnce.add(fieldname + gyo);
                }
            }
            return svf.VrsOutn(fieldname, gyo, data);
        }

        //縦型
        private void printMainTate(final DB2UDB db2, final List<Student> studentList) {
            //平均点・偏差値
            final Map<String, TestAvg> testAvgs = TestAvg.setTestAvg(db2, _param);

            final String form;
            final int maxGyo;
            if (_param._isH) {
                form = "KNJD183A_2_1.frm";
                maxGyo = 15;
            } else {
                form = "KNJD183A_1_1.frm";
                maxGyo = 10;
            }

            for (final Student student : studentList) {

                final Map<String, SubClass> printSubclasses = getPrintSubclassMap(db2, student);

                log.info(" schregno = " + student._schregno + ", subclass size = " + printSubclasses.size() + " / " + student._scoreDetails.size());

                if (printSubclasses.size() == 0) {
                    continue;
                }
                _hasData = true;

                setForm(form, 4); //縦型
                VrsOut("SCHOOL_NAME", _param._schoolname); // 学校名

                VrsOut("HR_NAME", student._hrName + student._attendno + "番"); // 年組番
                VrsOut("NAME", student._name); // 名前
                VrsOut("NENDO", _param._year + " 年度"); // 年度
                VrsOut("SEMESTER", _param._semesterName); // 学期

                if (_param._isGakunenmatsu) {
                    if (_param._outputHeijou.equals("1")) {
                        VrsOut("TEST_NAME3", _testnameC);
                    }
                    VrsOut("TEST_NAME4", _testnameD); // 考査名称
                } else {
                    if (!_param._semester.equals("3")) {
                        VrsOut("TEST_NAME1", _testnameA); // 考査名称
                    }
                    if (_param._testDiv >= 2) {
                        VrsOut("TEST_NAME2", _testnameB); // 考査名称
                    }
                    if (_param._testDiv >= 3) {
                        if (_param._outputHeijou.equals("1")) {
                            VrsOut("TEST_NAME3", _testnameC);
                        }// 考査名称
                        VrsOut("TEST_NAME4", _testnameD); // 考査名称
                    }
                }
                VrsOut("TEST_NAME5", _param._isH ? "欠課時数" : "到達度"); // 考査名称

                //出欠席
                final AttendInfo attendInfo = student._attendInfo.get(_param._semester);
                if (null != attendInfo) {
                    VrsOut("LESSON", String.valueOf(attendInfo._lesson)); // 授業日数
                    VrsOut("ATTEND", String.valueOf(attendInfo._present)); // 出席日数
                    VrsOut("MUST", String.valueOf(attendInfo._mLesson)); // 出席すべき日数
                    VrsOut("LATE", String.valueOf(attendInfo._late)); // 遅刻回数
                    VrsOut("MOURNING", String.valueOf(attendInfo._mourning + attendInfo._suspend)); // 忌引・出停
                    VrsOut("EARLY", String.valueOf(attendInfo._early)); // 早退回数
                    VrsOut("ABSENT", String.valueOf(attendInfo._absent)); // 欠席日数
                }

                //順位
                VrsOut("GRADE_RANK1", student._scoreRank._010101GradeRank); // 学年中間
                VrsOut("GRADE_RANK2", student._scoreRank._020101GradeRank); // 学年期末
                VrsOut("GRADE_RANK3", student._scoreRank._990008GradeRank); // 学年学期
                VrsOut("HR_RANK1", student._scoreRank._010101ClassRank); // クラス中間
                VrsOut("HR_RANK2", student._scoreRank._020101ClassRank); // クラス期末
                VrsOut("HR_RANK3", student._scoreRank._990008ClassRank); // クラス学期

                if (_param._isH) {
                    VrsOut("COURSE_RANK1", student._scoreRank._010101CourseRank); // コース中間
                    VrsOut("COURSE_RANK2", student._scoreRank._020101CourseRank); // コース期末
                    VrsOut("COURSE_RANK3", student._scoreRank._990008CourseRank); // コース学期
                }
                int gyou = 0;
                //科目ごとのレコード
                final List<String> printSubclassKeys = new ArrayList<String>(printSubclasses.keySet());
                for (int idx = 0; idx < printSubclassKeys.size(); idx++) {
                    final String printSubclassKey = printSubclassKeys.get(idx);
                    final SubClass subclass = printSubclasses.get(printSubclassKey);

                    if (subclass._replacemoto != GAPPEINASI) {
                        log.info(" replacemoto " + subclass._replacemoto + ", " + subclass._subclasscd + " : " + subclass._subclassname);
                    }

                    if (subclass._replacemoto == GAPPEISAKI) {
                        VrsOut("CLASS_NAME1", subclass._subclassname);
                        if (_param._isH) {
                            VrsOut("CREDIT1", zeroToBlank(subclass._credit)); // 単位
                        }
                        if (_param._isH) {
                            VrsOut("SUBCLASS_NAME1_3","換算点");
                        }else {
                            VrsOut("SUBCLASS_NAME1_3","合計");
                        }
                        int combGyou = 1; //合併科目用
                        for (final String attendsubclasscd : subclass._attendSubclasses.keySet()) {
                            log.info(" attend subclass = " + attendsubclasscd);
                            if (combGyou > 2) {
                                continue;
                            }
                            final SubClass attendSubclass = subclass._attendSubclasses.get(attendsubclasscd);
                            if (attendSubclass._replacemoto == GAPPEIMOTO) {
                                VrsOut("SUBCLASS_NAME1_" + combGyou, attendSubclass._subclassname);
                            }
                            if (_param._isH) {
                                VrsOutScore("SCORE1_5_" + combGyou, subclass._semesterAbsentMap.get(_param._semester)); // 欠課
                            }

                            printTestTate(db2, student, combGyou, attendSubclass);
                            if (attendSubclass._replacemoto == GAPPEISAKI) {
                            }
                            combGyou++;
                        }
                        combGyou = 3;
                        printTestTate(db2, student, combGyou, subclass);
                        final TestAvg testAvg = testAvgs.get(subclass._subclasscd);
                        printAvg(student, testAvg, 1, subclass);
                    } else {
                        printTestTate(db2, student, 0, subclass);
                        final TestAvg testAvg = testAvgs.get(subclass._subclasscd);
                        if (_param._isH) {
                            VrsOut(KNJ_EditEdit.getMS932ByteLength(subclass._subclassname) > 18 ? "CLASS_NAME2_2" : "CLASS_NAME2_1", subclass._subclassname); // 教科名
                            VrsOut("CREDIT2", zeroToBlank(subclass._credit)); // 単位
                            VrsOutScore("SCORE2_5", subclass._semesterAbsentMap.get(_param._semester));
                            printAvg(student, testAvg, 2, subclass);
                        } else {
                            VrsOut("CLASS_NAME3", subclass._subclassname); // 教科名
                            printAvg(student, testAvg, 3, subclass);
                        }
                    }
                    svf.VrEndRecord();
                    student._iskouketsuFlg = false;
                    student._iskessekiFlg = false;
                    gyou++;
                }

                for (int i = gyou; i < maxGyo; i++) {
                    svf.VrEndRecord();
                }
                svf.VrEndPage();
            }
        }

        private void printTestTate(final DB2UDB db2, final Student student, final int combGyou, final SubClass subclass) {
            //中間試験
            if (_param._testDiv >= 1) {
                printScoreTate(student, student._scoreDetails.get(_param.scoreDetailKey(_param._semester, _010101_CHUKAN, subclass._subclasscd)), _010101_CHUKAN, 1, combGyou, subclass);
            }
            //期末試験
            if (_param._testDiv >= 2) {
                printScoreTate(student, student._scoreDetails.get(_param.scoreDetailKey(_param._semester, _020101_KIMATSU, subclass._subclasscd)), _020101_KIMATSU, 2, combGyou, subclass);
            }
            if (_param._testDiv >= 3) {
                printScoreTate(student, student._scoreDetails.get(_param.scoreDetailKey(_param._semester, _990008_HYOKA, subclass._subclasscd)), _990008_HYOKA, 4, combGyou, subclass);

                //平常点
                if ("1".equals(_param._outputHeijou)) {
                    final String field;
                    if (subclass._replacemoto != GAPPEINASI) {
                        field = "SCORE1_3_" + combGyou;
                    } else {
                        if (_param._isH) {
                            field = "SCORE2_3";
                        } else {
                            field = "SCORE3_3_1";
                        }
                    }
                    VrsOutScore(field, showScore(student._scoreDetails.get(_param.scoreDetailKey(_param._semester, _990002_HEIJO, subclass._subclasscd))));
                }
            }

            //中学のみ到達度
            if (_param._isJ && _param._testDiv >= 3) {
                final ScoreDetail hyotei = student._scoreDetails.get(_param.scoreDetailKey(_param._semester, _990009_HYOTEI, subclass._subclasscd));
                if (null != hyotei) {
                    String field = "";
                    if (subclass._replacemoto == GAPPEISAKI) {
                        field = "SCORE1_5";
                    } else if (subclass._replacemoto == GAPPEINASI) {
                        field = "SCORE3_5";
                    }
                    VrsOutScore(field, hyotei._score); // 到達
                }
            }
        }

        //出力用に科目を整形する
        private Map<String, SubClass> getPrintSubclassMap(final DB2UDB db2, final Student student) {

            final Map<String, SubClass> printSubclasses = new TreeMap();

            for (final String scoreDetailKey : student._scoreDetails.keySet()) {

                final String semester = scoreDetailKey.substring(0, 1);
                final String testno = scoreDetailKey.substring(2, 3);
                final String subclasscd = scoreDetailKey.substring(4);

                //log.info(" seme = " + semester + " / testno = " + testno + " / subclasscd = " + subclasscd + " <= " + scoreDetailKey);

                if (!(_param._semester.equals(semester))) {
                    continue;
                }

                String skip = null;
                if (_param._testDiv == 1 && !(testno.equals("1"))) {
                    skip = " 1: testDiv " + _param._testDiv + ", testno " + testno;
                } else if (_param._testDiv == 2 && !(testno.equals("2"))) {
                    skip = " 2: testDiv " + _param._testDiv + ", testno " + testno;
                } else if (_param._testDiv == 2 && !(Integer.parseInt(testno) <= 2)) {
                    skip = " 3: testDiv " + _param._testDiv + ", testno " + testno;
                } else if (_param._testDiv == 3) {
                    if ("3".equals(_param._semester)) {
                        if (!(testno.equals("2") ||(testno.equals("3") || testno.equals("4") || testno.equals("5")))) {
                            skip = " 4: seme 3 testDiv " + _param._testDiv + ", testno " + testno;
                        }
                    } else if (!(testno.equals("3") || testno.equals("4") || testno.equals("5"))) {
                        skip = " 5: testDiv " + _param._testDiv + ", testno " + testno;
                    }
                }
                if (null == skip) {
                    if (_param._isOutputDebug) {
                        log.info(" print " + subclasscd);
                    }
                } else {
                    if (_param._isOutputDebug) {
                        log.info(" skip " + subclasscd + " : " + skip);
                    }
                    continue;
                }
                final ScoreDetail detail = student._scoreDetails.get(scoreDetailKey);
                if (printSubclasses.containsKey(subclasscd)) {
                    continue;
                }
                if (detail._subClass._replacemoto == GAPPEINASI) {
                    printSubclasses.put(subclasscd, detail._subClass);
                } else {
                    //合併科目なら
                    String combinedSubclasscd = "";
                    if (detail._subClass._replacemoto == GAPPEIMOTO) {
                        combinedSubclasscd = detail._subClass._combinedSubclasscd;
                    }else {
                        combinedSubclasscd = detail._subClass._subclasscd;
                    }
                    //log.info(" !!! det " + detail._subclass._subclasscd + ", replacemoto = " + detail._subclass._replacemoto + ", combinedSubclasscd = " + combinedSubclasscd);

                    if (printSubclasses.containsKey(combinedSubclasscd)) {
                        continue;
                    } else {
                        final String argSubclasscd = detail._subClass._replacemoto == GAPPEISAKI ? detail._subClass._subclasscd : detail._subClass._combinedSubclasscd;
                        final StringBuffer stb = new StringBuffer();
                        stb.append(" WITH COMB AS ");
                        stb.append(" (SELECT ");
                        stb.append("     YEAR, ");
                        stb.append("     COMBINED_CLASSCD, ");
                        stb.append("     COMBINED_SCHOOL_KIND, ");
                        stb.append("     COMBINED_CURRICULUM_CD, ");
                        stb.append("     COMBINED_SUBCLASSCD, ");
                        stb.append("     ATTEND_CLASSCD, ");
                        stb.append("     ATTEND_SCHOOL_KIND, ");
                        stb.append("     ATTEND_CURRICULUM_CD, ");
                        stb.append("     ATTEND_SUBCLASSCD ");
                        stb.append(" FROM ");
                        stb.append("     SUBCLASS_REPLACE_COMBINED_DAT ");
                        stb.append(" WHERE ");
                        stb.append("     COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD = '" + argSubclasscd + "' AND ");
                        stb.append("     YEAR = '" + _param._year + "' ");
                        stb.append(" ) SELECT ");
                        stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ");
                        stb.append("     CASE WHEN T2.SUBCLASSORDERNAME2 IS NULL THEN T2.SUBCLASSNAME ELSE T2.SUBCLASSORDERNAME2 END AS SUBCLASSNAME, ");
                        stb.append("     CASE WHEN T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '" + argSubclasscd + "' THEN '" + GAPPEISAKI + "' ELSE '" + GAPPEIMOTO + "' END AS VALUE ");
                        stb.append(" FROM COMB T1 ");
                        stb.append(" LEFT JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.ATTEND_CLASSCD AND T2.CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD AND T2.SCHOOL_KIND = T1.ATTEND_SCHOOL_KIND AND T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ");
                        stb.append(" UNION ");
                        stb.append(" SELECT ");
                        stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                        stb.append("     CASE WHEN T1.SUBCLASSORDERNAME2 IS NULL THEN T1.SUBCLASSNAME ELSE T1.SUBCLASSORDERNAME2 END AS SUBCLASSNAME, ");
                        stb.append("     CASE WHEN T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + argSubclasscd + "' THEN '" + GAPPEISAKI + "' ELSE '" + GAPPEIMOTO + "' END AS VALUE ");
                        stb.append(" FROM ");
                        stb.append("     SUBCLASS_MST T1 ");
                        stb.append(" WHERE  ");
                        stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + argSubclasscd + "' ");
                        stb.append(" ORDER BY VALUE, SUBCLASSCD ");

                        PreparedStatement ps = null;
                        ResultSet rs = null;
                        int credit = 0;
                        String  combinedSubclassname = "";

                        final SubClass combinedSubclass = new SubClass(combinedSubclasscd, "", 0, GAPPEISAKI, "", "");
                        try {
                            // log.info(" replacemoto = " + detail._subclass._replacemoto + ", combinedSubclasscd sql = " + stb.toString());

                            ps = db2.prepareStatement(stb.toString());
                            rs = ps.executeQuery();

                            while (rs.next()) {
                                final String subclasscd1 = rs.getString("SUBCLASSCD");
                                final int replacemoto = rs.getInt("VALUE");
                                if (argSubclasscd.equals(subclasscd1)) {
                                    combinedSubclassname = rs.getString("SUBCLASSNAME");
                                } else {
                                    if (student._subclasses.containsKey(subclasscd1)) {
                                        final SubClass subclass2 = student._subclasses.get(subclasscd1);
                                        combinedSubclass._attendSubclasses.put(subclasscd1, subclass2);
                                        credit += subclass2._credit;
                                    } else {
                                        final SubClass subclass2 = new SubClass(subclasscd, rs.getString("SUBCLASSNAME"), 0, replacemoto, "", "");
                                        combinedSubclass._attendSubclasses.put(subclasscd1, subclass2);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("Exception", e);
                        } finally {
                            DbUtils.closeQuietly(null, ps, rs);
                            db2.commit();
                        }
                        combinedSubclass._credit = credit;
                        combinedSubclass._subclassname = combinedSubclassname;
                        printSubclasses.put(combinedSubclasscd, combinedSubclass);
                        if (_param._isOutputDebug) {
                            log.info(" combinedSubclasscd = " + combinedSubclasscd + ", name = " + combinedSubclassname + " / " + combinedSubclass._attendSubclasses);
                        }
                    }
                }
            }
            return printSubclasses;
        }

        //中間・期末・学期成績の平均
        private void printAvg(final Student student, final TestAvg testAvg, final int field, final SubClass subclass) {

            if (_param._isGakunenmatsu) {
                VrsOut("AVE_TEST_NAME" + field + "_1_3", _testnameD); // 平均考査名
            }else {
                if (!_param._semester.equals("3")) {
                    VrsOut("AVE_TEST_NAME" + field + "_1_1", _testnameA); // 平均考査
                }
                if (_param._testDiv >= 2) {
                    VrsOut("AVE_TEST_NAME" + field + "_1_2", _testnameB); // 平均考査名
                }
                if (_param._testDiv >= 3) {
                    VrsOut("AVE_TEST_NAME" + field + "_1_3", _testnameD); // 平均考査名
                }
            }

            if (testAvg != null) {
                if (subclass._replacemoto != GAPPEIMOTO) {
                    if (_param._isGakunenmatsu) {
                        VrsOut("AVE" + field + "_1_3", testAvg._990008Avg); // 平均点

                        final String sdkey = _param.scoreDetailKey("9", "990008", subclass._subclasscd);
                        if (student._scoreDetails.containsKey(sdkey)) {
                            final ScoreDetail detail = student._scoreDetails.get(sdkey);
                            svf.VrsOut("AVE" + field + "_2_3", detail._gradeDeviation); // 偏差値
                        }

                    } else {
                        VrsOut("AVE" + field + "_1_1", testAvg._010101Avg); // 平均点

                        final String sdKey1 = _param.scoreDetailKey(_param._semester, "010101", subclass._subclasscd);
                        if (student._scoreDetails.containsKey(sdKey1)) {
                            final ScoreDetail detail = student._scoreDetails.get(sdKey1);
                            svf.VrsOut("AVE" + field + "_2_1", detail._gradeDeviation); // 偏差値
                        }

                        if (_param._testDiv >= 2) {
                            VrsOut("AVE" + field + "_1_2", testAvg._020101Avg); // 平均点

                            final String sdKey2 = _param.scoreDetailKey(_param._semester, "020101", subclass._subclasscd);
                            if (student._scoreDetails.containsKey(sdKey2)) {
                                final ScoreDetail detail = student._scoreDetails.get(sdKey2);
                                svf.VrsOut("AVE" + field + "_2_2", detail._gradeDeviation); // 偏差値
                            }
                        }
                        if (_param._testDiv >= 3) {
                            VrsOut("AVE" + field + "_1_3", testAvg._990008Avg); // 平均点

                            final String sdKey3 = _param.scoreDetailKey(_param._semester, "990008", subclass._subclasscd);
                            if (student._scoreDetails.containsKey(sdKey3)) {
                                final ScoreDetail detail = student._scoreDetails.get(sdKey3);
                                svf.VrsOut("AVE" + field + "_2_3", detail._gradeDeviation); // 偏差値
                            }
                        }
                    }
                }
            }
        }

        //中間・期末・学期成績
        private void printScoreTate(final Student student, final ScoreDetail detail, final String testcd, final int testNo, final int combGyou, final SubClass subclass) {
            if (null == detail || (StringUtils.isEmpty(detail._score) && StringUtils.isEmpty(detail._valueDi))) {
                return;
            }
            //スコア

            if (_param._isOutputDebug) {
                log.info(" print (replacemoto = " + subclass._replacemoto + ") testcd " + testcd + ", detail "+ detail + ", showScore = " + showScore(detail));
            }
            if (subclass._replacemoto != GAPPEINASI) { //合併科目
                if (subclass._replacemoto == GAPPEIMOTO && testNo != 4) { //合併科目
                    VrsOutScore("SCORE1_" + testNo + "_" + combGyou, showScore(detail));
                } else if (subclass._replacemoto == GAPPEISAKI) {
                    student._iskessekiFlg = false;
                    student._iskouketsuFlg = false;
                    mikomiFlg(student, subclass, _param._semester, testcd);
                    String val = "";
                    if (student._iskessekiFlg == false && student._iskouketsuFlg == false) {
                        val = detail._score;
//                    } else if (student._iskessekiFlg == true && student._iskouketsuFlg == true) {
//                        val = "{" + detail._score + "}";
//                    } else if (student._iskessekiFlg == true && student._iskouketsuFlg == false) {
//                        val = "(" + detail._score + ")";
//                    } else if (student._iskessekiFlg == false && student._iskouketsuFlg == true) {
//                        val = "[" + detail._score + "]";
                    } else if (student._iskessekiFlg == true) {
                        val = "（欠）";
                    } else if (student._iskouketsuFlg == true) {
                        val = "（公欠）";
                    }
                    VrsOutScore("SCORE1_" + testNo + (testNo == 4 ? "": "_" + combGyou), val); // スコア
                }
            } else {
                String field;
                if (_param._isH) {
                    field = "SCORE2_" + testNo;
                } else {
                    field = "SCORE3_" + testNo + (testNo == 4 ? "": "_1");
                }
                VrsOutScore(field, showScore(detail)); // スコア
            }
        }

        private String zeroToBlank(final int n) {
            return n == 0 ? "" : String.valueOf(n);
        }

        //横型
        private void printMainYoko(final DB2UDB db2, final List<Student> studentList) {

            final String form;
            if (_param._isH) {
                form = "KNJD183A_2_2.frm";
            } else {
                form = "KNJD183A_1_2.frm";
            }

            for (final Student student : studentList) {
                int taniSum = 0;

                setForm(form, 4); //横型
                VrsOut("SCHOOL_NAME", _param._schoolname); // 学校名

                VrsOut("HR_NAME", student._hrName + student._attendno + "番"); // 年組番
                VrsOut("NAME", student._name); // 名前
                VrsOut("NENDO", _param._year + "年度"); // 年度

                VrsOut("TEST_NAME1_1", _testnameA); // 中間
                VrsOut("TEST_NAME1_2", _testnameB); // 期末
                if (_param._outputHeijou.equals("1")) {VrsOut("TEST_NAME1_3", _testnameC);}// 平常
                VrsOut("TEST_NAME2_1", _testnameA); // 中間
                VrsOut("TEST_NAME2_2", _testnameB); // 期末
                if (_param._outputHeijou.equals("1")) {VrsOut("TEST_NAME2_3", _testnameC);}// 平常
                VrsOut("TEST_NAME3_1", _testnameB); // 期末
                if (_param._outputHeijou.equals("1")) {VrsOut("TEST_NAME3_2", _testnameC);}// 平常

                //順位
                VrsOut("RANK1_1", student._scoreRank._990008GradeRank); // 学年順位
                VrsOut("RANK1_2", student._scoreRank._990008GradeCount); // 学年人数
                if (_param._isH) {
                    VrsOut("RANK2_1", student._scoreRank._990008CourseRank); // コース順位
                    VrsOut("RANK2_2", student._scoreRank._990008CourseCount); // コース人数
                    VrsOut("RANK3_1", student._scoreRank._990008ClassRank); // クラス順位
                    VrsOut("RANK3_2", student._scoreRank._990008ClassCount); // クラス人数
                } else {
                    VrsOut("RANK2_1", student._scoreRank._990008ClassRank); // クラス順位
                    VrsOut("RANK2_2", student._scoreRank._990008ClassCount); // クラス人数
                }

                //出欠
                final int iseme = Integer.parseInt(_param._semester);
                for(int cnt = 1; cnt <= iseme; cnt++) {
                    final AttendInfo attendInfo = student._attendInfo.get(String.valueOf(cnt));
                    if (null != attendInfo) {
                        final int gyo = cnt == 9 ? 4 : cnt;
                        VrsOutn("LESSON", gyo, String.valueOf(attendInfo._lesson)); // 授業日数
                        VrsOutn("ATTEND", gyo, String.valueOf(attendInfo._absent)); // 欠席日数
                        VrsOutn("PRESENT", gyo, String.valueOf(attendInfo._present)); // 出席日数
                        VrsOutn("MUST", gyo,String.valueOf(attendInfo._mLesson)); // 出席すべき日数
                        VrsOutn("LATE", gyo,String.valueOf(attendInfo._late)); // 遅刻回数
                        VrsOutn("MOURNING", gyo,String.valueOf(attendInfo._mourning + attendInfo._suspend)); // 忌引・出停
                        VrsOutn("EARLY", gyo,String.valueOf(attendInfo._early)); // 早退回数
                    }
                }

                VrsOut("TOTAL_AVE_DIV", student._sumHyoutei); // 1・2年評定平均

                //1,2年次科目別評定平均
                int gyou = 1;
                for (final String key : student._kakoHyoutei.keySet()) {
                    final List<String> list = student._kakoHyoutei.get(key);
                    VrsOutn("CLASS_NAME1",gyou, list.get(0)); // 教科名
                    VrsOutn("CLASS_AVE_DIV1",gyou, list.get(1)); // 評定平均
                    gyou++;
                }

                final Map<String, SubClass> printSubclasses = getPrintSubclassMap(db2, student);

                log.info(" " + student._schregno + ", scoreDetail size = " + student._scoreDetails.size() + ", printSubclass size = " + printSubclasses.size());

                if (printSubclasses.size() == 0) {
                    continue;
                }
                _hasData = true;
                // 科目ごとのレコード
                for (final String key : printSubclasses.keySet()) {
                    final SubClass subclass = printSubclasses.get(key);
                    if (_param._isH) {
                        VrsOut("CREDIT", zeroToBlank(subclass._credit)); // 単位数
                        final int namelen = subclass._subclassname.length();
                        VrsOut(namelen <= 6 ? "SUBCLASS_NAME1_1_1" : namelen == 7 ? "SUBCLASS_NAME1_1_2" : "SUBCLASS_NAME1_1_3_1", subclass._subclassname); // 科目名
                    } else {
                        VrsOut("SUBCLASS_NAME1", subclass._subclassname); // 科目名
                    }

                    // 指示画面で指定した学期分繰り返し
                    for (int seme = 1; seme <= iseme; seme++) {
                        final String sseme = String.valueOf(seme);

                        final List<String> printTestcds = new ArrayList();
                        printTestcds.add(_010101_CHUKAN);
                        if (seme < iseme  || seme == iseme && _param._testDiv >= 2 || _param._isGakunenmatsu) {
                            printTestcds.add(_020101_KIMATSU);
                        }

                        if (seme <= 3 && (seme < iseme || seme == iseme && _param._testDiv == 3) || _param._isGakunenmatsu) {
                            printTestcds.add(_990008_HYOKA);
                        }
                        if (seme <= 3) {
                            if (seme < iseme || seme == iseme && _param._testDiv == 3 || _param._isGakunenmatsu) {
                                if ("1".equals(_param._outputHeijou)) {
                                    printTestcds.add(_990002_HEIJO);
                                }
                            }
                        }
                        if (_param._isGakunenmatsu || _param._testDiv == 3) {
                            printTestcds.add(_990009_HYOTEI); // 学年末評定もしくは中学は各学期の到達点
                        }

                        for (int ti = 0; ti < printTestcds.size(); ti++) {
                            final String testcd = printTestcds.get(ti);
                            final int testNo = _param._testcdTestnoMap.get(testcd);
                            final String sdKey = _param.scoreDetailKey(sseme, testcd, key);
                            String score = "";
                            final ScoreDetail detail = student._scoreDetails.get(sdKey);
                            if (null == detail) {
                                log.info(" no detail " + sseme + ", " + testcd + " , " + key);
                            }
                            if (null != detail) {
                                if (_990008_HYOKA.equals(testcd) || _990009_HYOTEI.equals(testcd)) {
                                    score = detail._score;
                                } else {
                                    final boolean isGappeiSaki = subclass._replacemoto == GAPPEISAKI;
                                    if (isGappeiSaki) {
                                        // 合併科目の場合
                                        student._iskouketsuFlg = false;
                                        student._iskessekiFlg = false;
                                        mikomiFlg(student, subclass, sseme, testcd);

                                        if (student._iskessekiFlg == false && student._iskouketsuFlg == false) {
                                            score = detail._score;
//                                        } else if (student._iskessekiFlg == true && student._iskouketsuFlg == false) {
//                                            score = "(" + detail._score + ")";
//                                        } else if (student._iskessekiFlg == false && student._iskouketsuFlg == true) {
//                                            score = "[" + detail._score + "]";
//                                        } else if (student._iskessekiFlg == true && student._iskouketsuFlg == true) {
//                                            score = "{" + detail._score + "}";
                                        } else if (student._iskessekiFlg == true) {
                                            score = "(欠)";
                                        } else if (student._iskouketsuFlg == true) {
                                            score = "(公欠)";
                                        }
                                    } else {
                                        score = showScore(detail);
                                    }
                                }
                            }
                            String field = null;
                            if (testNo <= 2) {// 中間・期末
                                field = "SCORE" + seme + "_" + testNo;
                            } else if (_990008_HYOKA.equals(testcd)) {// 学期成績
                                if (seme <= 3) {
                                    field = "DIV" + seme; // スコア
                                } else {
                                    field = "SCORE9"; // 学年末成績
                                }
                            } else if (_990002_HEIJO.equals(testcd)) {// 平常点
                                field = "SCORE" + seme + "_" + testNo; // スコア
                            } else if (_990009_HYOTEI.equals(testcd)) {
                                if (_param._isJ) {
                                    field = "ATTAIN" + seme;
                                } else if (_param._isGakunenmatsu) {
                                    field = "DIV9";
                                }
                            } // key
                            VrsOutScore(field, score); // 評定
                        } // fortest
                        VrsOut("KEKKA" + seme, subclass._semesterAbsentMap.get(sseme)); // 欠課

                        if (_param._isH && _param._isGakunenmatsu) {
                            if (student._kamokuGetCredit.containsKey(key)) {
                                VrsOut("GET_CREDIT9", zeroToBlank(student._kamokuGetCredit.get(key))); // 修得単位
                                taniSum += student._kamokuGetCredit.get(key);
                                VrsOut("TOTAL_GET_CREDIT", String.valueOf(taniSum)); // 修得単位合計
                            }
                        }

                    } //forcnt
                    svf.VrEndRecord();
                }
                svf.VrEndPage();
            }
        }

        //合併元科目に見込み点があるかチェック
        private void mikomiFlg(final Student student, final SubClass subclass, final String sem, final String testcd) {

            for (String combkey : subclass._attendSubclasses.keySet()) {
                final SubClass combSubclass = student._subclasses.get(combkey);
                final ScoreDetail combdetail = student._scoreDetails.get(_param.scoreDetailKey(sem, testcd, combkey));
                if (null != combSubclass && null != combdetail) {
                    if (combSubclass._replacemoto == GAPPEISAKI) {
                        continue;
                    }

                    if (VALUE_DI_KESSHI.equals(combdetail._valueDi)) {
                        student._iskessekiFlg = true;
                    } else if (VALUE_DI_KOUKETSU.equals(combdetail._valueDi)) {
                        student._iskouketsuFlg = true;
                    }
                }
            }
        }

        private String showScore(final ScoreDetail detail) {
            if (null == detail) {
                return null;
            }
            final String rtn;
            if (null == detail._valueDi) {
                rtn = detail._score; // スコア
            } else if (VALUE_DI_KESSHI.equals(detail._valueDi)) {
//                rtn = "(" + detail._score + ")"; // ()スコア
                rtn = "（欠）";
            } else if (VALUE_DI_KOUKETSU.equals(detail._valueDi)) {
//                rtn = "[" + detail._score + "]"; // []スコア
                rtn = "（公欠）";
            } else {
                rtn = null;
            }
//            if (_param._isOutputDebug) {
//                log.info(" detail " + detail._subClass + ", rtn = " + rtn + " / " + detail._score + ", " + detail._valueDi);
//            }
            return rtn;
        }
    }

    private static class Student {
        final String _schregno;  // 学籍番号
        final String _gradehrclass;
        final String _attendno;
        final String _name;
        final String _hrName;
        final String _staffName;
        private final Map<String, ScoreDetail> _scoreDetails = new TreeMap();
        private String _combFlg = ""; //合併科目
        private boolean _isTaniFlg = true;
        private boolean _iskessekiFlg = false;
        private boolean _iskouketsuFlg = false;
        private final Map<String, AttendInfo> _attendInfo = new TreeMap();
        private final Map<String, Integer> _kamokuGetCredit = new TreeMap(); //科目別単位
        private final Map<String, List<String>> _kakoHyoutei = new TreeMap(); //1・2年評定
        private String _sumHyoutei;
        private ScoreRank _scoreRank = new ScoreRank(null, null, null, null, null, null, null, null, null);
        final Map<String, SubClass> _subclasses = new TreeMap();

        Student(final String schregno, final String gradeHrclass, final String attendno, final String name, final String hrName, final String staffName) {
            _schregno = schregno;
            _gradehrclass = gradeHrclass;
            _attendno = attendno;
            _name = name;
            _hrName = hrName;
            _staffName = staffName;
        }

        private static List<Student> load(final DB2UDB db2, final Param param, final String gradeHrclass) {
            final List<Student> studentList = new LinkedList();

            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, BASE.NAME, HDAT.HR_NAME, STF.STAFFNAME ");
            stb.append("FROM SCHREG_REGD_DAT T1 ");
            stb.append("INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append("LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = T1.YEAR ");
            stb.append("    AND HDAT.SEMESTER = T1.SEMESTER ");
            stb.append("    AND HDAT.GRADE = T1.GRADE ");
            stb.append("    AND HDAT.HR_CLASS = T1.HR_CLASS ");
            stb.append("LEFT JOIN STAFF_MST STF ON STF.STAFFCD = HDAT.TR_CD1 ");
            stb.append("WHERE   T1.YEAR = '" + param._year + "' ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append(    "AND T1.SEMESTER = '" + param._semester + "' ");
            } else {
                stb.append(    "AND T1.SEMESTER = '" + param._semeFlg + "' ");
            }
            stb.append(    "AND T1.GRADE || T1.HR_CLASS = '" + gradeHrclass + "' ");
            if (null != param._schregnoSelected) {
                stb.append(    "AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._schregnoSelected) + " ");
            }
            stb.append("ORDER BY T1.ATTENDNO");

            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                final Student student = new Student(
                        KnjDbUtils.getString(row, "SCHREGNO"),
                        KnjDbUtils.getString(row, "GRADE") + KnjDbUtils.getString(row, "HR_CLASS"),
                        KnjDbUtils.getString(row, "ATTENDNO"),
                        KnjDbUtils.getString(row, "NAME"),
                        KnjDbUtils.getString(row, "HR_NAME"),
                        KnjDbUtils.getString(row, "STAFFNAME"));
                studentList.add(student);
            }

            final Map<String, Student> studentMap = new HashMap<String, Student>();
            for (final Student student : studentList) {
                studentMap.put(student._schregno, student);
            }
            loadAttend(db2, param, studentMap);
            loadScoreDetail(db2, param, gradeHrclass, studentMap);
            if (param._isYoko) {
                if (param._isH) {
                    loadGetCredit(db2, param, gradeHrclass, studentMap); // 修得単位
                    loadHyoutei(db2, param, gradeHrclass, studentMap); // 1・2年時評定
                }
            }

            ScoreRank.getJuni(db2, param, studentMap);

            return studentList;
        }

        private static void loadAttend(final DB2UDB db2, final Param param, final Map<String, Student> studentMap) {
            final String psKey = "ATTENDSEMES";
            if (!param._psMap.containsKey(psKey)) {
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        param._sDate,
                        param._date,
                        param._attendParamMap
                );
                log.debug(" attendsemes sql = " + sql);

                try {
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                } catch (Exception e) {
                    log.error("exception!", e);
                    return;
                }
            }

            final Integer zero = Integer.valueOf(0);

            for (final String schregno : studentMap.keySet()) {
                final Student student = studentMap.get(schregno);

                for (final Map<String, String> row : KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {schregno})) {
                    final AttendInfo attendInfo = new AttendInfo(
                            KnjDbUtils.getInt(row, "LESSON", zero),
                            KnjDbUtils.getInt(row, "MLESSON", zero),
                            KnjDbUtils.getInt(row, "SUSPEND", zero),
                            KnjDbUtils.getInt(row, "MOURNING", zero),
                            KnjDbUtils.getInt(row, "SICK", zero),
                            KnjDbUtils.getInt(row, "PRESENT", zero),
                            KnjDbUtils.getInt(row, "LATE", zero),
                            KnjDbUtils.getInt(row, "EARLY", zero),
                            KnjDbUtils.getInt(row, "TRANSFER_DATE", zero)
                            );
                    final String semester = KnjDbUtils.getString(row, "SEMESTER");
                    if (!student._attendInfo.containsKey(semester)) {
                        student._attendInfo.put(semester , attendInfo);
                    }
                }
            }
        }

        private static void loadScoreDetail(final DB2UDB db2, final Param param, final String hrclassCd, final Map<String, Student> studentMap) {

            {
                final String sql = sqlStdSubclassDetail(param, hrclassCd);
                if (param._isOutputDebug) {
                    log.info(" subclass sql " + hrclassCd + " = " + sql);
                }

                for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                    final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                    if (student == null) {
                        continue;
                    }

                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    final String classcd = subclasscd == null ? "" : subclasscd.substring(0, 2);
                    if (classcd.compareTo(KNJDefineSchool.subject_U) <= 0 || classcd.equals(KNJDefineSchool.subject_T)) {

                        if (!student._subclasses.containsKey(subclasscd)) {
                            final int credit = KnjDbUtils.getInt(row, "CREDITS", Integer.valueOf(0));
                            //科目クラスのインスタンスを作成して返す
                            final String subclassname = StringUtils.defaultString(KnjDbUtils.getString(row, "SUBCLASSORDERNAME2"), KnjDbUtils.getString(row, "SUBCLASSNAME"));
                            final String combined = KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD");
                            final String attend = KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD");
                            final int replacemoto = KnjDbUtils.getInt(row, "REPLACEMOTO", GAPPEINASI);
                            student._subclasses.put(subclasscd, new SubClass(subclasscd, subclassname, credit, replacemoto, combined, attend));
                        }
                        final SubClass subclass = student._subclasses.get(subclasscd);

                        final String score;
                        if (KnjDbUtils.getString(row, "VALUE_DI") != null) {
                            score = null; // KnjDbUtils.getString(row, "SCORE2");
                        } else {
                            score = KnjDbUtils.getString(row, "SCORE");
                        }
                        final ScoreDetail detail = new ScoreDetail(
                                subclass,
                                StringUtils.defaultString(score),
                                KnjDbUtils.getString(row, "VALUE_DI"),
                                KnjDbUtils.getString(row, "GRADE_DEVIATION"),
                                KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD"),
                                KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD")
                        );
                        final String testcd = StringUtils.defaultString(KnjDbUtils.getString(row, "TESTKINDCD")) + StringUtils.defaultString(KnjDbUtils.getString(row, "TESTITEMCD")) + StringUtils.defaultString(KnjDbUtils.getString(row, "SCORE_DIV"));
                        String key = param.scoreDetailKey(KnjDbUtils.getString(row, "SEMESTER"), testcd, subclasscd);
                        if(("".equals(testcd))) {
                            Integer testno = param._testcdTestnoMap.get(param._testCd);
                            key = param._semester + "_" + testno + "_" + subclasscd;
                        }
                        student._scoreDetails.put(key, detail);
                    }
                }
            }

            final String psKey = "attend_subclass";
            if (!param._psMap.containsKey(psKey)) {
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
                        SEMEALL,
                        param._sDate,
                        param._date,
                        param._attendParamMap
                        );
                log.debug(" attend sql = " + sql);
                try {
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }

            for (final String schregno : studentMap.keySet()) {
                final Student student = studentMap.get(schregno);

                for (final Map<String, String> row : KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {schregno})) {

                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");

                    final SubClass subclass = student._subclasses.get(subclasscd);
                    if (null != subclass) {
                        String val;
                        if (subclass._replacemoto == GAPPEISAKI) {
                            val = KnjDbUtils.getString(row, "REPLACED_SICK");
                        } else {
                            val = KnjDbUtils.getString(row, "SICK2");
                        }
                        if (NumberUtils.isNumber(val) && Double.parseDouble(val) > 0.0) {
                            subclass._semesterAbsentMap.put(KnjDbUtils.getString(row, "SEMESTER"), val.equals("0") ? "" : val);
                        }
                    }
                }
            }
        }

        /**
         *  PrepareStatement作成 --> 成績・評定
         */
        private static String sqlStdSubclassDetail(final Param _param, final String hrclassCd) {

            //対象生徒の表 クラスの生徒
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHNO_A AS(SELECT ");
            stb.append("     W1.SCHREGNO, ");
            stb.append("     W1.YEAR, ");
            stb.append("     W1.SEMESTER, ");
            stb.append("     W1.GRADE, ");
            stb.append("     W1.HR_CLASS, ");
            stb.append("     W1.COURSECD, ");
            stb.append("     W1.MAJORCD, ");
            stb.append("     W1.COURSECODE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN SEMESTER_MST W2 ON W2.YEAR = '"+ _param._year + "' AND W2.SEMESTER = W1.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("     W1.YEAR = '" + _param._year + "' AND ");
            stb.append("     W1.SEMESTER = '" +_param._semeFlg + "' AND ");
            stb.append("     W1.GRADE || W1.HR_CLASS = '" + hrclassCd + "'" );
            if (null != _param._schregnoSelected) {
                stb.append(    "AND W1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._schregnoSelected) + " ");
            }

            //講座の表
            stb.append(" ) , CHAIR_A AS( ");
            stb.append(" SELECT ");
            stb.append("     S1.SCHREGNO, ");
            stb.append("     S2.CLASSCD, ");
            stb.append("     S2.SCHOOL_KIND, ");
            stb.append("     S2.CURRICULUM_CD, ");
            stb.append("     S2.SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT S1, ");
            stb.append("     CHAIR_DAT S2 ");
            stb.append(" WHERE ");
            stb.append("     S1.YEAR         = '" + _param._year + "' ");
            stb.append("     AND S1.SEMESTER <= '" + _param._semester + "' ");
            stb.append("     AND S2.YEAR     = S1.YEAR          ");
            stb.append("     AND S2.SEMESTER <= '" + _param._semester + "' ");
            stb.append("     AND S2.SEMESTER = S1.SEMESTER          ");
            stb.append("     AND S2.CHAIRCD  = S1.CHAIRCD          ");
            stb.append("     AND EXISTS(SELECT ");
            stb.append("                  'X' ");
            stb.append("                FROM ");
            stb.append("                  SCHNO_A S3 ");
            stb.append("                WHERE ");
            stb.append("                  S3.SCHREGNO = S1.SCHREGNO ");
            stb.append("                GROUP BY ");
            stb.append("                  SCHREGNO ");
            stb.append("             )          ");
            stb.append("     AND SUBCLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
            stb.append("     AND SUBCLASSCD NOT LIKE '50%' ");
            stb.append(" GROUP BY ");
            stb.append("     S1.SCHREGNO, ");
            stb.append("     S2.CLASSCD, ");
            stb.append("     S2.SCHOOL_KIND, ");
            stb.append("     S2.CURRICULUM_CD, ");
            stb.append("     S2.SUBCLASSCD ");

            //合併科目の表
            stb.append(" ),COMBINED_A AS(SELECT ");
            stb.append("     T1.COMBINED_CLASSCD, T1.COMBINED_SCHOOL_KIND, T1.COMBINED_CURRICULUM_CD, T1.COMBINED_SUBCLASSCD, T2.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append("     INNER JOIN CHAIR_A T2 ON T2.CLASSCD = T1.COMBINED_CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.COMBINED_SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "'");
            stb.append(" GROUP BY ");
            stb.append("     T1.COMBINED_CLASSCD, T1.COMBINED_SCHOOL_KIND, T1.COMBINED_CURRICULUM_CD, T1.COMBINED_SUBCLASSCD, T2.SCHREGNO ");
            stb.append(" ),COMBINED_B AS(  SELECT ");
            stb.append("     T1.ATTEND_CLASSCD, T1.ATTEND_SCHOOL_KIND, T1.ATTEND_CURRICULUM_CD, T1.ATTEND_SUBCLASSCD, T2.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append("     INNER JOIN CHAIR_A T2 ON T2.CLASSCD = T1.ATTEND_CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.ATTEND_SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "'");
            stb.append(" GROUP BY ");
            stb.append("     T1.ATTEND_CLASSCD, T1.ATTEND_SCHOOL_KIND, T1.ATTEND_CURRICULUM_CD, T1.ATTEND_SUBCLASSCD, T2.SCHREGNO ");

            //テスト種別の表
            stb.append(" ),SCHREG_TESTCDS AS(SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.TESTKINDCD, ");
            stb.append("     T1.TESTITEMCD, ");
            stb.append("     T1.SCORE_DIV, ");
            stb.append("     T0.SCHREGNO ");
            stb.append(" FROM CHAIR_A T0 ");
            stb.append("     INNER JOIN RECORD_RANK_SDIV_DAT T1  ");
            stb.append("        ON T1.SUBCLASSCD    = T0.SUBCLASSCD ");
            stb.append("       AND T1.SCHREGNO      = T0.SCHREGNO ");
            stb.append("       AND T1.CLASSCD       = T0.CLASSCD ");
            stb.append("       AND T1.SCHOOL_KIND   = T0.SCHOOL_KIND ");
            stb.append("       AND T1.CURRICULUM_CD = T0.CURRICULUM_CD ");
            stb.append("       AND T1.YEAR = '" + _param._year + "' ");
            stb.append("  UNION ");
            stb.append("  SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.TESTKINDCD, ");
            stb.append("     T1.TESTITEMCD, ");
            stb.append("     T1.SCORE_DIV, ");
            stb.append("     T0.SCHREGNO ");
            stb.append(" FROM CHAIR_A T0 ");
            stb.append("     INNER JOIN RECORD_SCORE_DAT T1  ");
            stb.append("        ON T1.SUBCLASSCD    = T0.SUBCLASSCD ");
            stb.append("       AND T1.SCHREGNO      = T0.SCHREGNO ");
            stb.append("       AND T1.CLASSCD       = T0.CLASSCD ");
            stb.append("       AND T1.SCHOOL_KIND   = T0.SCHOOL_KIND ");
            stb.append("       AND T1.CURRICULUM_CD = T0.CURRICULUM_CD ");
            stb.append("       AND T1.YEAR = '" + _param._year + "' ");
            stb.append("       AND T1.VALUE_DI IS NOT NULL ");

            //メイン表
            stb.append(") SELECT ");
            stb.append("     TCDS.SEMESTER, ");
            stb.append("     TCDS.TESTKINDCD, ");
            stb.append("     TCDS.TESTITEMCD, ");
            stb.append("     TCDS.SCORE_DIV, ");
            stb.append("     T0.CLASSCD, ");
            stb.append("     T0.SCHOOL_KIND, ");
            stb.append("     T0.CURRICULUM_CD, ");
            stb.append("     T0.CLASSCD || '-' || T0.SCHOOL_KIND || '-' || T0.CURRICULUM_CD || '-' ||  T0.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T0.SCHREGNO, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.GRADE_DEVIATION, ");
            stb.append("     T2.SUBCLASSNAME, ");
            stb.append("     T2.SUBCLASSORDERNAME2, ");
            stb.append("     T3.VALUE_DI, ");
//            stb.append("     T9.SCORE AS SCORE2, ");
            stb.append("     T5.CREDITS, ");
            stb.append("     CASE WHEN T6.COMBINED_CLASSCD IS NOT NULL THEN " + GAPPEISAKI + " ");
            stb.append("          WHEN T7.ATTEND_CLASSCD IS NOT NULL THEN " + GAPPEIMOTO + " ELSE " + GAPPEINASI + " END AS REPLACEMOTO, ");
            stb.append("     T8.CLASSNAME, ");
            stb.append("     T10.COMBINED_CLASSCD || '-' || T10.COMBINED_SCHOOL_KIND || '-' || T10.COMBINED_CURRICULUM_CD || '-' || T10.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ");
            stb.append("     T10.ATTEND_CLASSCD || '-' || T10.ATTEND_SCHOOL_KIND || '-' || T10.ATTEND_CURRICULUM_CD || '-' || T10.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
            stb.append(" FROM CHAIR_A T0 ");
            stb.append("     INNER JOIN SCHREG_TESTCDS TCDS ");
            stb.append("        ON TCDS.SCHREGNO = T0.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T1  ");
            stb.append("        ON T1.YEAR          = TCDS.YEAR ");
            stb.append("       AND T1.SEMESTER      = TCDS.SEMESTER ");
            stb.append("       AND T1.TESTKINDCD    = TCDS.TESTKINDCD ");
            stb.append("       AND T1.TESTITEMCD    = TCDS.TESTITEMCD ");
            stb.append("       AND T1.SCORE_DIV     = TCDS.SCORE_DIV ");
            stb.append("       AND T1.CLASSCD       = T0.CLASSCD ");
            stb.append("       AND T1.SCHOOL_KIND   = T0.SCHOOL_KIND ");
            stb.append("       AND T1.CURRICULUM_CD = T0.CURRICULUM_CD ");
            stb.append("       AND T1.SUBCLASSCD    = T0.SUBCLASSCD ");
            stb.append("       AND T1.SCHREGNO      = TCDS.SCHREGNO ");
            stb.append(" LEFT JOIN  SUBCLASS_MST T2 ON T0.CLASSCD = T2.CLASSCD AND T0.SCHOOL_KIND = T2.SCHOOL_KIND AND T0.CURRICULUM_CD = T2.CURRICULUM_CD AND T0.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append(" LEFT JOIN RECORD_SCORE_DAT T3  ");
            stb.append("        ON T3.YEAR          = TCDS.YEAR ");
            stb.append("       AND T3.SEMESTER      = TCDS.SEMESTER ");
            stb.append("       AND T3.TESTKINDCD    = TCDS.TESTKINDCD ");
            stb.append("       AND T3.TESTITEMCD    = TCDS.TESTITEMCD ");
            stb.append("       AND T3.SCORE_DIV     = TCDS.SCORE_DIV ");
            stb.append("       AND T3.CLASSCD       = T0.CLASSCD ");
            stb.append("       AND T3.SCHOOL_KIND   = T0.SCHOOL_KIND ");
            stb.append("       AND T3.CURRICULUM_CD = T0.CURRICULUM_CD ");
            stb.append("       AND T3.SUBCLASSCD    = T0.SUBCLASSCD ");
            stb.append("       AND T3.SCHREGNO      = TCDS.SCHREGNO ");
//            stb.append(" LEFT JOIN SUPP_EXA_SDIV_DAT T9 ON T1.YEAR = T9.YEAR AND T1.SEMESTER = T9.SEMESTER AND T1.TESTKINDCD = T9.TESTKINDCD AND T1.TESTITEMCD = T9.TESTITEMCD AND T1.SCORE_DIV = T9.SCORE_DIV AND T0.CLASSCD = T9.CLASSCD AND T0.SCHOOL_KIND = T9.SCHOOL_KIND AND T0.CURRICULUM_CD = T9.CURRICULUM_CD AND T0.SUBCLASSCD = T9.SUBCLASSCD AND T1.SCHREGNO = T9.SCHREGNO ");
            stb.append(" INNER JOIN SCHNO_A T4 ON T0.SCHREGNO = T4.SCHREGNO ");
            stb.append(" LEFT JOIN CREDIT_MST T5 ON  T4.YEAR = T5.YEAR AND T4.COURSECD = T5.COURSECD AND T4.COURSECODE = T5.COURSECODE AND T4.MAJORCD = T5.MAJORCD AND T4.GRADE = T5.GRADE AND T0.CURRICULUM_CD = T5.CURRICULUM_CD AND T0.CLASSCD = T5.CLASSCD AND T0.SUBCLASSCD = T5.SUBCLASSCD AND T5.SCHOOL_KIND = '" + _param._schoolKind + "'");
            stb.append(" LEFT JOIN COMBINED_A T6 ON T0.CLASSCD = T6.COMBINED_CLASSCD AND T0.SCHOOL_KIND = T6.COMBINED_SCHOOL_KIND AND T0.CURRICULUM_CD = T6.COMBINED_CURRICULUM_CD AND T0.SUBCLASSCD = T6.COMBINED_SUBCLASSCD AND T0.SCHREGNO = T6.SCHREGNO ");
            stb.append(" LEFT JOIN COMBINED_B T7 ON T0.CLASSCD = T7.ATTEND_CLASSCD AND T0.SCHOOL_KIND = T7.ATTEND_SCHOOL_KIND AND T0.CURRICULUM_CD = T7.ATTEND_CURRICULUM_CD AND T0.SUBCLASSCD = T7.ATTEND_SUBCLASSCD AND T0.SCHREGNO = T7.SCHREGNO ");
            stb.append(" LEFT JOIN CLASS_MST T8 ON T0.CLASSCD = T8.CLASSCD AND T0.SCHOOL_KIND = T8.SCHOOL_KIND ");
            stb.append(" LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T10 ON T4.YEAR = T10.YEAR AND T0.CLASSCD = T10.ATTEND_CLASSCD AND T0.SCHOOL_KIND = T10.ATTEND_SCHOOL_KIND AND T0.CURRICULUM_CD = T10.ATTEND_CURRICULUM_CD AND T0.SUBCLASSCD = T10.ATTEND_SUBCLASSCD ");
            stb.append("                                            AND (T0.SCHREGNO, T10.COMBINED_CLASSCD, T10.COMBINED_SCHOOL_KIND, T10.COMBINED_CURRICULUM_CD, T10.COMBINED_SUBCLASSCD)  ");
            stb.append("                                              IN (SELECT TA.SCHREGNO, TA.CLASSCD, TA.SCHOOL_KIND, TA.CURRICULUM_CD, TA.SUBCLASSCD FROM CHAIR_A TA)  ");
            stb.append(" WHERE T2.SUBCLASSNAME IS NOT NULL ");
            stb.append("       AND TCDS.YEAR = '" + _param._year + "' ");
            stb.append("       AND TCDS.SEMESTER <= '" + _param._semester + "' ");
            stb.append("       AND (TCDS.TESTKINDCD, TCDS.TESTITEMCD, TCDS.SCORE_DIV) IN ");
            stb.append("                                              (VALUES ('01', '01', '01') ");   // 中間
            stb.append("                                                    , ('02', '01', '01') ");   // 期末
            stb.append("                                                    , ('99', '00', '02') ");   // 平常点
            stb.append("                                                    , ('99', '00', '08') ");   // 評価
            stb.append("                                                    , ('99', '00', '09')) ");  // 評定
            stb.append("       AND (T1.YEAR IS NOT NULL OR T3.VALUE_DI IS NOT NULL) ");
            stb.append(" ORDER BY T0.SCHREGNO,T1.SEMESTER,T1.TESTKINDCD,T1.TESTITEMCD,T1.SCORE_DIV,T0.CLASSCD,T0.SUBCLASSCD,T0.CURRICULUM_CD ASC,REPLACEMOTO DESC");
            return stb.toString();
        }

        private static void loadGetCredit(final DB2UDB db2, final Param param, final String _hrclassCd, final Map<String, Student> studentMap) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHNO_A AS(SELECT ");
            stb.append("     W1.SCHREGNO, ");
            stb.append("     W1.YEAR, ");
            stb.append("     W1.SEMESTER, ");
            stb.append("     W1.GRADE, ");
            stb.append("     W1.HR_CLASS, ");
            stb.append("     W1.COURSECD, ");
            stb.append("     W1.MAJORCD, ");
            stb.append("     W1.COURSECODE ");
            stb.append(" FROM ");
            stb.append(
                    "     SCHREG_REGD_DAT W1 INNER JOIN SEMESTER_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("     W1.YEAR = '" + param._year + "' AND ");
            stb.append("     W1.SEMESTER = '" + param._semeFlg + "' AND ");
            stb.append("     W1.GRADE || W1.HR_CLASS = '" + _hrclassCd + "' ");
            if (null != param._schregnoSelected) {
                stb.append(    "AND W1.SCHREGNO IN " + SQLUtils.whereIn(true, param._schregnoSelected) + " ");
            }
            stb.append(" )SELECT ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' ||  T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GET_CREDIT ");
            stb.append(" FROM ");
            stb.append("     RECORD_SCORE_DAT T1,SCHNO_A T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = T2.YEAR AND ");
            stb.append("     T1.SCHREGNO = T2.SCHREGNO AND ");
            stb.append("     T1.TESTKINDCD = '99' AND ");
            stb.append("     T1.TESTITEMCD = '00' AND ");
            stb.append("     T1.SCORE_DIV = '09' AND");
            stb.append("     T1.SEMESTER = '9' ");
            stb.append(" ORDER BY T1.SCHREGNO,T1.SUBCLASSCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Student student = studentMap.get(rs.getString("SCHREGNO"));
                    if (student == null) {
                        continue;
                    }
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    student._kamokuGetCredit.put(subclasscd, rs.getInt("GET_CREDIT")
                            );
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        //評定平均を取得
        private static void loadHyoutei(final DB2UDB db2, final Param param, final String gradeHrclass, final Map<String, Student> studentMap) {

            //1・2年次教科別評定平均
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHNO_A AS(SELECT ");
            stb.append("     W1.SCHREGNO, ");
            stb.append("     W1.YEAR, ");
            stb.append("     W1.SEMESTER, ");
            stb.append("     W1.GRADE, ");
            stb.append("     W1.HR_CLASS, ");
            stb.append("     W1.COURSECD, ");
            stb.append("     W1.MAJORCD, ");
            stb.append("     W1.COURSECODE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT W1 INNER JOIN SEMESTER_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("     W1.YEAR = '" + param._year + "' AND ");
            stb.append("     W1.SEMESTER = '" + param._semeFlg + "' AND ");
            stb.append("     W1.GRADE || W1.HR_CLASS = '" + gradeHrclass + "' ");
            if (null != param._schregnoSelected) {
                stb.append(    "AND W1.SCHREGNO IN " + SQLUtils.whereIn(true, param._schregnoSelected) + " ");
            }
            stb.append(" ), KAMOKUHYOUTEI AS");
            stb.append("(SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     SUM(T1.VALUATION) AS VALUATION, ");
            stb.append("     COUNT(T1.SCHREGNO) AS COUNT ");
            stb.append(" FROM ");
            stb.append("     SCHREG_STUDYREC_DAT T1, ");
            stb.append("     SCHNO_A T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = T2.SCHREGNO AND ");
            stb.append("     T1.ANNUAL IN ('04','05') AND ");
            stb.append("     (T1.VALUATION IS NOT NULL OR T1.VALUATION <> 0) ");
            stb.append(" GROUP BY T1.SCHREGNO,T1.CLASSCD,T1.SCHOOL_KIND ");
            stb.append(" ) ");
            stb.append(" SELECT T2.SCHREGNO,T2.CLASSCD,T2.SCHOOL_KIND,T2.VALUATION,T2.COUNT,T3.CLASSABBV ");
            stb.append(" FROM KAMOKUHYOUTEI T2 ");
            stb.append(" INNER JOIN CLASS_MST T3 ON T2.SCHOOL_KIND = T3.SCHOOL_KIND AND T2.CLASSCD = T3.CLASSCD ");
            stb.append(" ORDER BY T2.SCHREGNO ");


            //1・2年次評定平均値
            final StringBuffer stb1 = new StringBuffer();
            stb1.append(" WITH SCHNO_A AS(SELECT ");
            stb1.append("     W1.SCHREGNO, ");
            stb1.append("     W1.YEAR, ");
            stb1.append("     W1.SEMESTER, ");
            stb1.append("     W1.GRADE, ");
            stb1.append("     W1.HR_CLASS, ");
            stb1.append("     W1.COURSECD, ");
            stb1.append("     W1.MAJORCD, ");
            stb1.append("     W1.COURSECODE ");
            stb1.append(" FROM ");
            stb1.append("     SCHREG_REGD_DAT W1 INNER JOIN SEMESTER_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER ");
            stb1.append(" WHERE ");
            stb1.append("     W1.YEAR = '" + param._year + "' AND ");
            stb1.append("     W1.SEMESTER = '" + param._semeFlg + "' AND ");
            stb1.append("     W1.GRADE || W1.HR_CLASS = '" + gradeHrclass + "' ");
            if (null != param._schregnoSelected) {
                stb1.append(    "AND W1.SCHREGNO IN " + SQLUtils.whereIn(true, param._schregnoSelected) + " ");
            }
            stb1.append(" ), KAKOHYOUTEI AS  ");
            stb1.append(" (SELECT ");
            stb1.append("     T1.SCHREGNO, ");
            stb1.append("     T1.CLASSCD, ");
            stb1.append("     T1.SCHOOL_KIND, ");
            stb1.append("     SUM(T1.VALUATION) AS VALUATION, ");
            stb1.append("     COUNT(T1.SCHREGNO) AS COUNT ");
            stb1.append(" FROM ");
            stb1.append("     SCHREG_STUDYREC_DAT T1, ");
            stb1.append("     SCHNO_A T2 ");
            stb1.append(" WHERE ");
            stb1.append("     T1.SCHREGNO = T2.SCHREGNO AND ");
            stb1.append("     T1.ANNUAL IN ('04','05') AND ");
            stb1.append("     (T1.VALUATION IS NOT NULL OR T1.VALUATION <> 0) ");
            stb1.append(" GROUP BY T1.SCHREGNO,T1.CLASSCD,T1.SCHOOL_KIND ");
            stb1.append(" )SELECT ");
            stb1.append("     T1.SCHREGNO, ");
            stb1.append("     SUM(T1.VALUATION) AS VALUATION, ");
            stb1.append("     SUM(T1.COUNT) AS COUNT ");
            stb1.append(" FROM KAKOHYOUTEI T1 ");
            stb1.append(" GROUP BY T1.SCHREGNO ");
            stb1.append(" ORDER BY T1.SCHREGNO ");

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Student student = studentMap.get(rs.getString("SCHREGNO"));
                    if (student == null) {
                        continue;
                    }

                    final BigDecimal b1 = new BigDecimal(rs.getInt("VALUATION"));
                    final BigDecimal b2 = new BigDecimal(rs.getInt("COUNT"));
                    final BigDecimal b3 = b1.divide(b2,1,BigDecimal.ROUND_HALF_UP);
                    final List<String> list = new ArrayList();
                    list.add(rs.getString("CLASSABBV"));
                    list.add(b3.toString());
                    student._kakoHyoutei.put(rs.getString("CLASSCD"), list);
                }

                DbUtils.closeQuietly(null, ps, rs);

                ps = db2.prepareStatement(stb1.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Student student = studentMap.get(rs.getString("SCHREGNO"));
                    if (student == null) {
                        continue;
                    }
                    final BigDecimal b1 = new BigDecimal(rs.getInt("VALUATION"));
                    final BigDecimal b2 = new BigDecimal(rs.getInt("COUNT"));
                    final BigDecimal b3 = b1.divide(b2,1,BigDecimal.ROUND_HALF_UP);

                    student._sumHyoutei = b3.toString() ;
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    private static class AttendInfo {
        private final int _lesson;
        private final int _mLesson;
        private final int _suspend;
        private final int _mourning;
        private final int _absent;
        private final int _present;
        private final int _late;
        private final int _early;
        private final int _transDays;

        private AttendInfo(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int transDays
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _transDays = transDays;
        }
    }

    private void init(final HttpServletResponse response, final Vrw32alp svf) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70043 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _ctrlSemester;
        final String _year;
        final String _ctrlDate;
        final String _semester;
        final String _semeFlg;
        final String _grade;
        final boolean _isGakunenmatsu;

        final String _outputHeijou;
        final boolean _isTate;
        final boolean _isYoko;
        final String _date;
        final String[] _classSelected;  //印刷対象HR組
        String[] _schregnoSelected;
        final String _schoolKind;
        final boolean _isH;
        final boolean _isJ;
        String _schoolname;

        private String _sDate;

        private String _semesterName;
        private String _testItemName;
        private final Map<String, Integer> _testcdTestnoMap = new HashMap<String, Integer>();

        final Map<String, PreparedStatement> _psMap = new HashMap<String, PreparedStatement>();
        private final Map _attendParamMap;
        private final Set<String> logOnce = new HashSet<String>();
        private final String _testCd;
        private int _testDiv;

        private KNJSchoolMst _knjSchoolMst;

        final boolean _isOutputDebug;
        private Map<String, File> _createFormFileMap = new TreeMap();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _isGakunenmatsu = SEMEALL.equals(_semester);
            _ctrlDate = request.getParameter("CTRL_DATE");
            _date = request.getParameter("LOGIN_DATE");
            if ("2".equals(request.getParameter("DISP"))) {
                _classSelected = new String[] {request.getParameter("GRADE_HR_CLASS")};
                _grade = request.getParameter("GRADE_HR_CLASS").substring(0, 2);
                _schregnoSelected = request.getParameterValues("CLASS_SELECTED");
                if (null != _schregnoSelected) {
                    for (int i = 0; i < _schregnoSelected.length; i++) {
                        _schregnoSelected[i] = _schregnoSelected[i].split("-")[0];
                    }
                }
            } else {
                _classSelected = request.getParameterValues("CLASS_SELECTED");
                _grade = request.getParameter("GRADE");
            }
            _semeFlg = request.getParameter("SEME_FLG");

            _schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
            _isH = "H".equals(_schoolKind);
            _isJ = "J".equals(_schoolKind);
            if (_isH) {
                _schoolname = "日本大学第二高等学校";
            } else if (_isJ) {
                _schoolname = "日本大学第二中学校";
            }

            _testcdTestnoMap.put(_010101_CHUKAN, 1);
            _testcdTestnoMap.put(_020101_KIMATSU, 2);
            _testcdTestnoMap.put(_990002_HEIJO, 3);
            _testcdTestnoMap.put(_990008_HYOKA, 4);
            _testcdTestnoMap.put(_990009_HYOTEI, 5);

            _outputHeijou = request.getParameter("OUTPUT_HEIJOU") == null ? "0" : request.getParameter("OUTPUT_HEIJOU");

            final String _frmDiv = request.getParameter("FRM_DIV");
            _isTate = "1".equals(_frmDiv);
            _isYoko = "2".equals(_frmDiv);
            _sDate = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ORDER BY SEMESTER "));
            log.debug("年度の開始日=" + _sDate);

            //1:中間 、2:期末、3:学期末
            _testCd = request.getParameter("TEST_CD");
            _testDiv = _testCd.equals(_010101_CHUKAN) ? 1 : _testCd.equals(_020101_KIMATSU) ? 2 : 3;

            final KNJ_Get_Info getinfo = new KNJ_Get_Info();
            //  学期名称、範囲の取得
            final KNJ_Get_Info.ReturnVal returnval = getinfo.Semester(db2, _year, _semester);
            _semesterName = returnval.val1;  //学期名称

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);

            final String[] outputDebug = StringUtils.split(KnjDbUtils.getDbPrginfoProperties(db2, "KNJD183A", "outputDebug"));
            _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
        }

        private String scoreDetailKey(final String semester, final String testcd, final String subclasscd) {
            Integer testno = _testcdTestnoMap.get(testcd);
            final String key = semester + "_" + testno + "_" + subclasscd;
            //log.info(" score detail key = " + key);
            return key;
        }

        private void close() {
            for (final Iterator<PreparedStatement> it = _psMap.values().iterator(); it.hasNext();) {
                final PreparedStatement ps = it.next();
                DbUtils.closeQuietly(ps);
                it.remove();
            }
            for (final File file : _createFormFileMap.values()) {
                if (null != file && file.exists()) {
                    file.delete();
                }
            }
        }

        private String getTestName(final DB2UDB db2, final String year, final String semester, final String testcd) {
            final String sql = "SELECT TESTITEMNAME "
                    +   "FROM TESTITEM_MST_COUNTFLG_NEW_SDIV "
                    +  "WHERE YEAR = '" + year + "' "
                    +    "AND SEMESTER = '" + semester + "' "
                    +    "AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + testcd + "' ";
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
        }
    }

    /**
     * <<科目のクラスです>>。
     */
    private static class SubClass {
        private final String _subclasscd;
        private String _subclassname;
        private final String _combinedSubclasscd;
        private final String _attendSubclasscd;
        private final Map<String, SubClass> _attendSubclasses = new TreeMap();
        private int _credit;  // 単位
        private final Integer _replacemoto;
        private final Map<String, String> _semesterAbsentMap = new TreeMap();

        SubClass(
                final String subclasscd,
                final String subclassname,
                final int credit,
                final int replacemoto,
                final String combinedSubclasscd,
                final String attendSubclasscd
        ) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _credit = credit;  // 単位
            _replacemoto = replacemoto;
            _combinedSubclasscd = combinedSubclasscd;
            _attendSubclasscd = attendSubclasscd;
        }

        public String toString() {
            return "[" + _subclasscd + " , " + _subclassname + ", " +_credit +"]";
        }
    }



    /**
     * <<生徒別科目別データのクラスです>>。
     */
    private static class ScoreDetail {
        private final SubClass _subClass;
        private final String _score;
        private final String _valueDi;
        private final String _gradeDeviation;
        private final String _combinedCd;
        private final String _attendCd;

        ScoreDetail(
                final SubClass subClass,
                final String score,
                final String valueDi,
                final String gradeDeviation,
                final String combinedCd,
                final String attendCd
        ) {
            _subClass = subClass;
            _score = score;
            _valueDi = valueDi;
            _gradeDeviation = gradeDeviation;
            _combinedCd = combinedCd;
            _attendCd = attendCd;
        }


        public String toString() {
            return (_subClass + " , " + _score + " (valueDi = " + _valueDi + ")");
        }
    }

    /**
     * <<順位用データクラス>>。
     */
    private static class ScoreRank {
        private final String _010101GradeRank;
        private final String _010101ClassRank;
        private final String _010101CourseRank;
        private final String _020101GradeRank;
        private final String _020101ClassRank;
        private final String _020101CourseRank;
        private final String _990008GradeRank;
        private final String _990008ClassRank;
        private final String _990008CourseRank;

        private String _990008GradeCount;
        private String _990008CourseCount;
        private String _990008ClassCount;

        ScoreRank(
                final String _010101GradeRank_,
                final String _010101ClassRank_,
                final String _010101CourseRank_,
                final String _020101GradeRank_,
                final String _020101ClassRank_,
                final String _020101CourseRank_,
                final String _990008GradeRank_,
                final String _990008ClassRank_,
                final String _990008CourseRank_
        ) {
            _010101GradeRank = _010101GradeRank_;
            _010101ClassRank = _010101ClassRank_;
            _010101CourseRank = _010101CourseRank_;
            _020101GradeRank = _020101GradeRank_;
            _020101ClassRank = _020101ClassRank_;
            _020101CourseRank = _020101CourseRank_;
            _990008GradeRank = _990008GradeRank_;
            _990008ClassRank = _990008ClassRank_;
            _990008CourseRank = _990008CourseRank_;
        }

        //生徒の学年、コース、クラス順位設定
        private static void getJuni(final DB2UDB db2, final Param _param, final Map<String, Student> studentMap) {
            PreparedStatement ps = null;

            try {
                //順位
                final StringBuffer stb = new StringBuffer();

                stb.append(" WITH REGD AS ( ");
                stb.append(" SELECT ");
                stb.append("           T1.SCHREGNO ");
                stb.append("         , T1.GRADE ");
                stb.append("         , T1.HR_CLASS ");
                stb.append("         , T1.COURSECD ");
                stb.append("         , T1.MAJORCD ");
                stb.append("         , T1.COURSECODE ");
                stb.append("     FROM SCHREG_REGD_DAT T1 ");
                stb.append("      WHERE ");
                stb.append("          T1.YEAR = '" + _param._year + "' ");
                if (SEMEALL.equals(_param._semester)) {
                    stb.append("     AND T1.SEMESTER = '" + _param._semeFlg + "' ");
                } else {
                    stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
                }
                stb.append("        AND T1.GRADE = '" + _param._grade + "' ");
                stb.append(" ) ");
                stb.append(" , DEVIATION_SUM AS ( ");
                stb.append("     SELECT  ");
                stb.append("           T1.SCHREGNO ");
                stb.append("         , L1.GRADE ");
                stb.append("         , L1.HR_CLASS ");
                stb.append("         , L1.COURSECD ");
                stb.append("         , L1.MAJORCD ");
                stb.append("         , L1.COURSECODE ");
                stb.append("         , T1.TESTKINDCD ");
                stb.append("         , T1.TESTITEMCD ");
                stb.append("         , T1.SCORE_DIV ");
                stb.append("         , SUM(T1.GRADE_DEVIATION) AS GRADE_DEVIATION_SUM "); // 各科目の学年偏差値の合計
                stb.append("         , SUM(T1.COURSE_DEVIATION) AS COURSE_DEVIATION_SUM "); // 各科目のコース偏差値の合計
                stb.append("     FROM RECORD_RANK_SDIV_DAT T1 ");
                stb.append("     INNER JOIN REGD L1 ON ");
                stb.append("              L1.SCHREGNO = T1.SCHREGNO ");
                stb.append("     INNER JOIN SUBCLASS_MST L2 ON "); // 合計科目を含めない
                stb.append("            L2.CLASSCD = T1.CLASSCD ");
                stb.append("        AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("        AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("        AND L2.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("     INNER JOIN RECORD_RANK_SDIV_DAT L3 ON "); // 科目999999のある生徒が母集団
                stb.append("            L3.YEAR = T1.YEAR ");
                stb.append("        AND L3.SEMESTER = T1.SEMESTER ");
                stb.append("        AND L3.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("        AND L3.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("        AND L3.SCORE_DIV = T1.SCORE_DIV ");
                stb.append("        AND L3.SUBCLASSCD = '999999' ");
                stb.append("        AND L3.SCHREGNO = T1.SCHREGNO ");
                stb.append("      WHERE ");
                stb.append("          T1.YEAR = '" + _param._year + "' ");
                stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
                stb.append("         AND (T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV) IN ");
                stb.append("                                            (VALUES ");  // 学期
                if (_param._isTate) {
                    stb.append("                                                    ('01', '01', '01'), ");   // 中間
                    stb.append("                                                    ('02', '01', '01'), ");   // 期末
                }
                stb.append("                                                    ('99', '00', '08')) ");  // 学期
                stb.append("        AND (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD) NOT IN (SELECT ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ");
                stb.append("          FROM SUBCLASS_REPLACE_COMBINED_DAT  ");
                stb.append("          WHERE YEAR = '" + _param._year  + "' ");
                stb.append("            ) ");
                stb.append("     GROUP BY ");
                stb.append("       T1.SCHREGNO ");
                stb.append("     , L1.GRADE ");
                stb.append("     , L1.HR_CLASS ");
                stb.append("     , L1.COURSECD ");
                stb.append("     , L1.MAJORCD ");
                stb.append("     , L1.COURSECODE ");
                stb.append("     , T1.TESTKINDCD ");
                stb.append("     , T1.TESTITEMCD ");
                stb.append("     , T1.SCORE_DIV ");
                stb.append(" ) ");
                stb.append(" , DEVIATION_SUM_RANK AS ( ");
                stb.append("    SELECT  ");
                stb.append("       T1.SCHREGNO ");
                stb.append("     , T1.HR_CLASS ");
                stb.append("     , T1.COURSECD ");
                stb.append("     , T1.MAJORCD ");
                stb.append("     , T1.COURSECODE ");
                stb.append("     , T1.TESTKINDCD ");
                stb.append("     , T1.TESTITEMCD ");
                stb.append("     , T1.SCORE_DIV ");
                stb.append("     , T1.GRADE_DEVIATION_SUM ");
                stb.append("     , RANK() OVER(PARTITION BY T1.GRADE                                                     , T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ORDER BY T1.GRADE_DEVIATION_SUM DESC) AS GRADE_DEVIATION_SUM_RANK ");    // 各科目の学年偏差値の合計で学年順位
                stb.append("     , RANK() OVER(PARTITION BY T1.GRADE, T1.HR_CLASS                                        , T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ORDER BY T1.GRADE_DEVIATION_SUM DESC) AS GRADE_DEVIATION_SUM_HR_RANK "); // 各科目の学年偏差値の合計でHR順位
                stb.append("     , T1.COURSE_DEVIATION_SUM ");
                stb.append("     , RANK() OVER(PARTITION BY T1.GRADE,              T1.COURSECD, T1.MAJORCD, T1.COURSECODE, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ORDER BY T1.COURSE_DEVIATION_SUM DESC) AS COURSE_DEVIATION_SUM_RANK ");    // 各科目のコース偏差値の合計でコース順位
                stb.append("     , RANK() OVER(PARTITION BY T1.GRADE, T1.HR_CLASS, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ORDER BY T1.COURSE_DEVIATION_SUM DESC) AS COURSE_DEVIATION_SUM_HR_RANK "); // 各科目のコース偏差値の合計でHR順位
                stb.append("   FROM DEVIATION_SUM T1 ");
                stb.append(" ) ");

                stb.append(" SELECT ");
                stb.append("     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.GRADE_DEVIATION_SUM_RANK AS GRADE_RANK, ");
                if (_param._isH) {
                    stb.append("     T1.COURSE_DEVIATION_SUM_HR_RANK AS CLASS_RANK, ");
                } else {
                    stb.append("     T1.GRADE_DEVIATION_SUM_HR_RANK AS CLASS_RANK, ");
                }
                stb.append("     T1.COURSE_DEVIATION_SUM_RANK AS COURSE_RANK ");
                stb.append(" FROM ");
                stb.append("     DEVIATION_SUM_RANK T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.SCHREGNO = ? ");

                ps = db2.prepareStatement(stb.toString());

                for (final Student student : studentMap.values()) {
                    String _010101GradeRank = "";
                    String _010101CourseRank = "";
                    String _010101ClassRank = "";
                    String _020101GradeRank = "";
                    String _020101CourseRank = "";
                    String _020101ClassRank = "";
                    String _990008GradeRank = "";
                    String _990008CourseRank = "";
                    String _990008ClassRank = "";

                    for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] { student._schregno})) {
                        final String testcd = KnjDbUtils.getString(row, "TESTCD");
                        if (_010101_CHUKAN.equals(testcd)) {
                            _010101GradeRank = KnjDbUtils.getString(row, "GRADE_RANK");
                            _010101ClassRank = KnjDbUtils.getString(row, "CLASS_RANK");
                            _010101CourseRank = KnjDbUtils.getString(row, "COURSE_RANK");
                        } else if (_020101_KIMATSU.equals(testcd)) {
                            _020101GradeRank = KnjDbUtils.getString(row, "GRADE_RANK");
                            _020101ClassRank = KnjDbUtils.getString(row, "CLASS_RANK");
                            _020101CourseRank = KnjDbUtils.getString(row, "COURSE_RANK");
                        } else if (_990008_HYOKA.equals(testcd)) {
                            _990008GradeRank = KnjDbUtils.getString(row, "GRADE_RANK");
                            _990008ClassRank = KnjDbUtils.getString(row, "CLASS_RANK");
                            _990008CourseRank = KnjDbUtils.getString(row, "COURSE_RANK");
                        }
                    }

                    final ScoreRank scoreRank = new ScoreRank(
                            _010101GradeRank,
                            _010101ClassRank,
                            _010101CourseRank,
                            _020101GradeRank ,
                            _020101ClassRank,
                            _020101CourseRank,
                            _990008GradeRank,
                            _990008ClassRank,
                            _990008CourseRank
                    );
                    student._scoreRank = scoreRank;
                }

            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

            if (_param._isYoko) {
                final StringBuffer stb4 = new StringBuffer();
                //人数
                stb4.append(" WITH SCHNO_A AS(SELECT ");
                stb4.append("     W1.SCHREGNO, ");
                stb4.append("     W1.YEAR, ");
                stb4.append("     W1.SEMESTER, ");
                stb4.append("     W1.GRADE, ");
                stb4.append("     W1.HR_CLASS, ");
                stb4.append("     W1.COURSECD, ");
                stb4.append("     W1.MAJORCD, ");
                stb4.append("     W1.COURSECODE ");
                stb4.append(" FROM ");
                stb4.append("     SCHREG_REGD_DAT W1 INNER JOIN SEMESTER_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER ");
                stb4.append(" WHERE ");
                stb4.append("     W1.YEAR = '" + _param._year + "' AND ");
                stb4.append("     W1.SEMESTER = '" + _param._semeFlg + "' AND ");
                stb4.append("     W1.SCHREGNO = ? ");
                stb4.append(" ), COURSE AS(SELECT ");
                stb4.append("     COUNT AS COURSE_COUNT ");
                stb4.append(" FROM ");
                stb4.append("     RECORD_AVERAGE_SDIV_DAT W3, SCHNO_A W1 ");
                stb4.append(" WHERE ");
                stb4.append("     W3.YEAR = W1.YEAR   AND ");
                stb4.append("     W3.SEMESTER = '" + _param._semester + "' AND ");
                stb4.append("     W3.GRADE = W1.GRADE AND ");
                stb4.append("     W3.AVG_DIV= '3' AND ");
                stb4.append("     W3.COURSECD = W1.COURSECD AND ");
                stb4.append("     W3.MAJORCD = W1.MAJORCD AND ");
                stb4.append("     W3.COURSECODE = W1.COURSECODE AND ");
                stb4.append("     W3.TESTKINDCD = '99' AND ");
                stb4.append("     W3.TESTITEMCD = '00' AND ");
                stb4.append("     W3.SCORE_DIV = '08' AND ");
                stb4.append("     W3.SCHOOL_KIND = '" + _param._schoolKind + "' AND ");
                stb4.append("     W3.CLASSCD = '99' AND ");
                stb4.append("     W3.SUBCLASSCD = '999999' ");
                stb4.append(" ), GRADE AS (SELECT ");
                stb4.append("     COUNT AS GRADE_COUNT ");
                stb4.append(" FROM ");
                stb4.append("     RECORD_AVERAGE_SDIV_DAT W4, SCHNO_A W1 ");
                stb4.append(" WHERE ");
                stb4.append("     W4.YEAR = W1.YEAR   AND ");
                stb4.append("     W4.SEMESTER = '" + _param._semester + "' AND ");
                stb4.append("     W4.GRADE = W1.GRADE AND ");
                stb4.append("     W4.HR_CLASS = '000' AND ");
                stb4.append("     W4.AVG_DIV= '1' AND ");
                stb4.append("     W4.COURSECD = '0' AND ");
                stb4.append("     W4.MAJORCD = '000' AND ");
                stb4.append("     W4.COURSECODE = '0000' AND ");
                stb4.append("     W4.TESTKINDCD = '99' AND ");
                stb4.append("     W4.TESTITEMCD = '00' AND ");
                stb4.append("     W4.SCORE_DIV = '08' AND ");
                stb4.append("     W4.SCHOOL_KIND = '" + _param._schoolKind + "' AND ");
                stb4.append("     W4.CLASSCD = '99' AND ");
                stb4.append("     W4.SUBCLASSCD = '999999' ");
                stb4.append(" ), CLASS AS(SELECT ");
                stb4.append("     COUNT AS CLASS_COUNT ");
                stb4.append(" FROM ");
                stb4.append("     RECORD_AVERAGE_SDIV_DAT W5, SCHNO_A W1 ");
                stb4.append(" WHERE ");
                stb4.append("     W5.YEAR = W1.YEAR   AND ");
                stb4.append("     W5.SEMESTER = '" + _param._semester + "' AND ");
                stb4.append("     W5.GRADE = W1.GRADE AND ");
                stb4.append("     W5.HR_CLASS = W1.HR_CLASS AND ");
                stb4.append("     W5.AVG_DIV= '2' AND ");
                stb4.append("     W5.COURSECD = '0' AND ");
                stb4.append("     W5.MAJORCD = '000' AND ");
                stb4.append("     W5.COURSECODE = '0000' AND ");
                stb4.append("     W5.TESTKINDCD = '99' AND ");
                stb4.append("     W5.TESTITEMCD = '00' AND ");
                stb4.append("     W5.SCORE_DIV = '08' AND ");
                stb4.append("     W5.SCHOOL_KIND = '" + _param._schoolKind + "' AND ");
                stb4.append("     W5.CLASSCD = '99' AND ");
                stb4.append("     W5.SUBCLASSCD = '999999' ");
                stb4.append(" )SELECT ");
                stb4.append(" * ");
                stb4.append(" FROM GRADE,COURSE,CLASS ");

                try {
                    ps = db2.prepareStatement(stb4.toString());

                    for (final Student student : studentMap.values()) {
                        for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] { student._schregno})) {

                            student._scoreRank._990008GradeCount = KnjDbUtils.getString(row, "GRADE_COUNT");
                            student._scoreRank._990008CourseCount = KnjDbUtils.getString(row, "COURSE_COUNT");
                            student._scoreRank._990008ClassCount = KnjDbUtils.getString(row, "CLASS_COUNT");
                        }
                    }

                } catch (Exception e) {
                    log.error("Exception", e);
                } finally {
                    DbUtils.closeQuietly(ps);
                    db2.commit();
                }
            }
        }
    }


    /**
     * <<試験用データクラス>>。
     */
    private static class TestAvg {
        private String _010101Avg;
        private String _010101StdDev;
        private String _020101Avg;
        private String _020101StdDev;
        private String _990008Avg;
        private String _990008StdDev;

        private static Map<String, TestAvg> setTestAvg(final DB2UDB db2, final Param _param) {

            //平均点、偏差値
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTCD, ");
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' ||  SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     ROUND(AVG,1) AS AVG, ");
            stb.append("     ROUND(STDDEV,1) AS STDDEV, ");
            stb.append("     COUNT AS COUNT ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "'  AND ");
            stb.append("     SEMESTER = '" + _param._semester + "' AND ");
            stb.append("     GRADE = '" + _param._grade + "' AND ");
            stb.append("     HR_CLASS = '000' AND ");
            stb.append("     COURSECD = '0' AND ");
            stb.append("     MAJORCD = '000' AND ");
            stb.append("     COURSECODE = '0000' AND ");
            stb.append("     (TESTKINDCD, TESTITEMCD, SCORE_DIV) IN ");
            stb.append("                                            (VALUES ('01', '01', '01') ");       // 中間
            stb.append("                                                  , ('02', '01', '01') ");       // 期末
            stb.append("                                                  , ('99', '00', '08')) AND ");  // 学期
            stb.append("     SCHOOL_KIND = '"+ _param._schoolKind + "' ");

            final Map<String, TestAvg> testAvgs = new TreeMap();
            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                BigDecimal b1 = KnjDbUtils.getBigDecimal(row, "AVG", null);
                BigDecimal b2 = KnjDbUtils.getBigDecimal(row, "STDDEV", null);
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");

                if (!testAvgs.containsKey(subclasscd)) {
                    testAvgs.put(subclasscd, new TestAvg());
                }
                final TestAvg testAvg = testAvgs.get(subclasscd);

                final String testcd = KnjDbUtils.getString(row, "TESTCD");
                if (_010101_CHUKAN.equals(testcd)) {
                    testAvg._010101Avg = b1.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    testAvg._010101StdDev = b2.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                } else if (_020101_KIMATSU.equals(testcd)) {
                    testAvg._020101Avg = b1.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    testAvg._020101StdDev = b2.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                } else if (_990008_HYOKA.equals(testcd)) {
                    testAvg._990008Avg = b1.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    testAvg._990008StdDev = b2.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                }
            }
            return testAvgs;
        }
    }
}

// eof
