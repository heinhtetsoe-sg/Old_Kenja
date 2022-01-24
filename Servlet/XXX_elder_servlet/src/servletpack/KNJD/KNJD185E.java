/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 3d6cf98ce6600478a7f14017c25363df39610fe4 $
 *
 * 作成日: 2019/03/06
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import static servletpack.KNJZ.detail.KnjDbUtils.getString;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
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
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD185E {

    private static final Log log = LogFactory.getLog(KNJD185E.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "89";
    private static final String SELECT_CLASSCD_TOTALSTDY = "90";  // 総合的な学習の時間;

    private static final String SCORE010101 = "010101";
    private static final String SCORE990008 = "990008";
    private static final String SCORE990009 = "990009";

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

    private static final String PRINT_PATERN_A = "1";
    private static final String PRINT_PATERN_B = "2";
    private static final String PRINT_PATERN_C = "3";
    private static final String PRINT_PATERN_D = "4";
    private static final String PRINT_PATERN_E = "5";

    private static final String TITLE = "通知票";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf0) {
        final List<Student> studentList = getList(db2);
        //下段の出欠
        for (final DateRange range : _param._attendRanges.values()) {
            Attendance.load(db2, _param, studentList, range);
        }
        for (final DateRange range : _param._attendRanges.values()) {
            SubclassAttendance.load(db2, _param, studentList, range);
        }
        final Form svf = new Form(_param, svf0);
        if (PRINT_PATERN_A.equals(_param._seq001_r1)) {
            printPaternA(db2, svf, studentList);
        } else if (PRINT_PATERN_B.equals(_param._seq001_r1)) {
            printPaternB(db2, svf, studentList);
        } else if (PRINT_PATERN_C.equals(_param._seq001_r1)) {
            printPaternC(db2, svf, studentList);
        } else if (PRINT_PATERN_D.equals(_param._seq001_r1) || PRINT_PATERN_E.equals(_param._seq001_r1)) {
            printPaternDE(db2, svf, studentList);
        }
    }

    private static class Form {
        final Param _param;
        private Vrw32alp _svf;
        String _currentFormName;
        private Map<String, Map<String, SvfField>> _fieldMap = new HashMap();
        public Form(final Param param, final Vrw32alp svf) {
            _param = param;
            _svf = svf;
        }

        public void VrSetForm(final String formName, final int i) {
            _svf.VrSetForm(formName, i);
            if (_param._isOutputDebug || _param._isOutputDebugSvf) {
                log.info(" formName = " + formName);
            }
            _currentFormName = formName;
            if (!_fieldMap.containsKey(_currentFormName)) {
                _fieldMap.put(_currentFormName, SvfField.getSvfFormFieldInfoMapGroupByName(_svf));
            }
        }

        public boolean hasField(final String fieldname) {
            return null != getMappedMap(_fieldMap, _currentFormName).get(fieldname);
        }

        public void VrsOut(final String field, final String data) {
            if (!hasField(field)) {
                if (_param._isOutputDebug || _param._isOutputDebugSvf) {
                    _param.logOnce("no such field : " + field + " (data =  " + data + ")");
                }
            } else if (_param._isOutputDebugSvf) {
                log.info(" " + field + " = " + data);
            }
            _svf.VrsOut(field, data);
        }

        public void VrsOutn(final String field, final int gyo, final String data) {
            if (!hasField(field)) {
                if (_param._isOutputDebug || _param._isOutputDebugSvf) {
                    _param.logOnce("no such field : " + field + " (gyo = " + gyo + ", data =  " + data + ")");
                }
            } else if (_param._isOutputDebugSvf) {
                log.info(" " + field + ", " + gyo + " = " + data);
            }
            _svf.VrsOutn(field, gyo, data);
        }

        public void VrEndPage() {
            if (_param._isOutputDebugSvf) {
                log.info(" VrEndPage.");
            }
            _svf.VrEndPage();
        }

        public void VrEndRecord() {
            if (_param._isOutputDebugSvf) {
                log.info(" VrEndRecord.");
            }
            _svf.VrEndRecord();
        }

        public void VrAttribute(final String field, final String attr) {
            _svf.VrAttribute(field, attr);
        }

    }

    private void printPaternA(final DB2UDB db2, final Form svf, final List<Student> studentList) {
        final String formName = "KNJD185E_1.frm";

        for (final Student student : studentList) {
            svf.VrSetForm(formName, 4);

            svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoImagePath);
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName);
            svf.VrsOut("NENDO", _param._nendo);
            svf.VrsOut("TITLE", TITLE);
            svf.VrsOut("TOTAL_STUDY_NAME", _param._classColumnName);

            final String priField = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 18 ? "2" : "1";
            svf.VrsOut("PR_NAME1_" + priField, _param._certifSchoolPrincipalName);
            printStaffName(svf, student);

            svf.VrsOut("HR_NAME", student._hrname + student._attendno);
            svf.VrsOut("NAME1", student._name);

            //出欠記録
            printAttend(svf, student);

            //備考欄
            printHaveNewLine(svf, _param._HREPORTREMARK_DAT_COMMUNICATION_SIZE, student._communication, "REMARK", 74, 3);

            //明細部分
            final List<SubclassMst> subclassList = subclassListRemoveD026(student);
            //総合的な学習の時間
            if ("1".equals(_param._seq010_r1)) {
                svf.VrsOut("MASK", _param._whiteSpaceImagePath);
            } else {
                printHaveNewLine(svf, _param._HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE, student._totalstudytime, "SP_ACT", 74, 4);
                printHaveNewLine(svf, _param._HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE, student._specialactremark, "SP_ACT_VAL", 74, 4);
            }

            //評価合計、平均
            for (int semei = 1; semei <= Integer.parseInt(_param._semester); semei++) {
                final String semester = String.valueOf(semei);
                final ScoreData scoreData = student.getTotalScoreData(_param, semester, SCORE990008);
                if (null != scoreData) {
                    if (!"1".equals(_param._seq003_r1)) {
                        svf.VrsOut("VAL" + semester + "_TOTAL", scoreData._score);
                    }
                    if (!"1".equals(_param._seq004_r1)) {
                        svf.VrsOut("VAL" + semester + "_AVERAGE", sishaGonyu(scoreData._avg));
                    }
                }
            }

            //修得単位合計
            if (!"1".equals(_param._seq005_r1) && SEMEALL.equals(_param._semester)) {
                svf.VrsOut("GET_CREDIT_TOTAL", student.getCreditTotal());
            }

            int subclassCnt = 0;
            for (final SubclassMst subclassMst : subclassList) {
                final String subclassCd = subclassMst._subclasscd;
                subclassCnt++;
                final PrintSubclass printSubclass = student._printSubclassMap.get(subclassCd);

                svf.VrsOut("CLASS_NAME", subclassMst._classname);
                final int subNameLen = KNJ_EditEdit.getMS932ByteLength(subclassMst._subclassname);
                final String subNameField = subNameLen > 26 ? "3" : subNameLen > 18 ? "2" : "1";
                svf.VrsOut("SUBCLASS_NAME" + subNameField, subclassMst._subclassname);
                svf.VrsOut("CREDIT2", _param.getCredits(student._grade, student._course, subclassCd));

                for (int semei = 1; semei <= Integer.parseInt(_param._semester); semei++) {
                    final String semester = String.valueOf(semei);

                    if ("1".equals(_param._seq002Map.get(semester))) {
                        final ScoreData scoreData = printSubclass.getScoreData(semester, SCORE990008);
                        if (null != scoreData) {
                            svf.VrsOut("VAL" + semester, student.getKekkaOverCheckedScore(_param, subclassCd, semester, scoreData._score));
                            if (SEMEALL.equals(semester)) {
                                svf.VrsOut("GET_CREDIT", scoreData._getCredit);
                            }
                        }
                        svf.VrsOut("ABSENCE" + semester, student.getSubclassSick(subclassCd, semester));
                    }
                }

                svf.VrEndRecord();
            }
            _hasData = true;
            if (subclassCnt == 0) {
                svf.VrEndRecord();
            }
        }
    }

    private void printPaternB(final DB2UDB db2, final Form svf, final List<Student> studentList) {
        final String formName = "KNJD185E_2.frm";

        for (final Student student : studentList) {
            svf.VrSetForm(formName, 4);

            svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoImagePath);
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName);
            svf.VrsOut("NENDO", _param._nendo);
            svf.VrsOut("TITLE", TITLE);
            svf.VrsOut("TOTAL_STUDY_NAME", _param._classColumnName);

            final String priField = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 18 ? "2" : "1";
            svf.VrsOut("PR_NAME1_" + priField, _param._certifSchoolPrincipalName);
            printStaffName(svf, student);

            svf.VrsOut("HR_NAME", student._hrname + student._attendno);
            svf.VrsOut("NAME1", student._name);

            //出欠記録
            printAttend(svf, student);

            //備考欄
            printHaveNewLine(svf, _param._HREPORTREMARK_DAT_COMMUNICATION_SIZE, student._communication, "REMARK", 74, 3);

            //明細部分
            final List<SubclassMst> subclassList = subclassListRemoveD026(student);
            //総合的な学習の時間
            if ("1".equals(_param._seq010_r1)) {
                svf.VrsOut("MASK", _param._whiteSpaceImagePath);
            } else {
                printHaveNewLine(svf, _param._HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE, student._totalstudytime, "SP_ACT", 74, 4);
                printHaveNewLine(svf, _param._HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE, student._specialactremark, "SP_ACT_VAL", 74, 4);
            }

            //評価合計、平均
            for (int semei = 1; semei <= Integer.parseInt(_param._semester); semei++) {
                final String semester = String.valueOf(semei);
                final ScoreData scoreData = student.getTotalScoreData(_param, semester, SCORE990008);
                if (null != scoreData) {
                    if (!"1".equals(_param._seq003_r1)) {
                        svf.VrsOut("VAL" + semester + "_TOTAL", scoreData._score);
                    }
                    if (!"1".equals(_param._seq004_r1)) {
                        svf.VrsOut("VAL" + semester + "_AVERAGE", sishaGonyu(scoreData._avg));
                    }
                }
            }

            if (!"1".equals(_param._seq005_r1) && SEMEALL.equals(_param._semester)) {
                svf.VrsOut("GET_CREDIT_TOTAL", student.getCreditTotal());
            }

            final Map<String, List<PrintSubclass>> classcdSubclassListMap = new HashMap();
            final List<PrintSubclass> printSubclassList = new ArrayList();
            for (final SubclassMst subclassMst : subclassList) {
                final String subclassCd = subclassMst._subclasscd;
                printSubclassList.add(student._printSubclassMap.get(subclassCd));
                getMappedList(classcdSubclassListMap, subclassMst._classcd).add(student._printSubclassMap.get(subclassCd));
            }
            final int classnameKetaPerLine = 6;
            int subclassCnt = 0;
            for (final PrintSubclass printSubclass : printSubclassList) {
                final SubclassMst subclassMst = _param._subclassMstMap.get(printSubclass._subclassCd);
                subclassCnt++;

                final int classCnt = classcdSubclassListMap.get(subclassMst._classcd).indexOf(printSubclass);
                final int sameClassSubclassCount = classcdSubclassListMap.get(subclassMst._classcd).size();
                svf.VrsOut("GRPCD", subclassMst._classcd);
                final String classname = StringUtils.replace(StringUtils.repeat(" ", (sameClassSubclassCount * classnameKetaPerLine - KNJ_EditEdit.getMS932ByteLength(subclassMst._classname)) / 2), "  ", "　") + StringUtils.defaultString(subclassMst._classname);
                final String[] setClassName = KNJ_EditEdit.get_token(classname, classnameKetaPerLine, 5);
                if (setClassName.length > classCnt) {
                    if (null != setClassName[classCnt]) {
                        svf.VrsOut("CLASS_NAME", setClassName[classCnt]);
                        if (setClassName[classCnt].startsWith("　")) {
                            svf.VrAttribute("CLASS_NAME", "Hensyu=1");
                        }
                    }
                }

                final String[] setSubClassName = KNJ_EditEdit.get_token(subclassMst._subclassname, 32, 5);
                svf.VrsOut("SUBCLASS_NAME1_1", setSubClassName[0]);
                svf.VrsOut("CREDIT1", _param.getCredits(student._grade, student._course, printSubclass._subclassCd));

                for (int semei = 1; semei <= Integer.parseInt(_param._semester); semei++) {
                    final String semester = String.valueOf(semei);

                    if ("1".equals(_param._seq002Map.get(semester))) {
                        final ScoreData scoreData = printSubclass.getScoreData(semester, SCORE990008);
                        if (null != scoreData) {
                            svf.VrsOut("VAL1_" + semester, student.getKekkaOverCheckedScore(_param, printSubclass._subclassCd, semester, scoreData._score));
                        }
                        svf.VrsOut("ABSENCE1_" + semester, student.getSubclassSick(printSubclass._subclassCd, semester));
                    }
                }

                svf.VrEndRecord();
            }
            _hasData = true;
            if (subclassCnt == 0) {
                svf.VrEndRecord();
            }
        }
    }

    private void printPaternC(final DB2UDB db2, final Form svf, final List<Student> studentList) {
        final String formName = "KNJD185E_3.frm";

        for (final Student student : studentList) {
            svf.VrSetForm(formName, 4);

            svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoImagePath);
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName);
            svf.VrsOut("NENDO", _param._nendo);
            svf.VrsOut("TITLE", TITLE);
            svf.VrsOut("TOTAL_STUDY_NAME", _param._classColumnName);

            final String priField = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 18 ? "2" : "1";
            svf.VrsOut("PR_NAME1_" + priField, _param._certifSchoolPrincipalName);
            printStaffName(svf, student);

            svf.VrsOut("HR_NAME", student._hrname + student._attendno);
            svf.VrsOut("NAME1", student._name);

//            //出欠記録
//            printAttend(form, student);

            //備考欄
            printHaveNewLine(svf, _param._HREPORTREMARK_DAT_COMMUNICATION_SIZE, student._communication, "REMARK", 74, 3);

            //明細部分
            final List<SubclassMst> subclassList = subclassListRemoveD026(student);
            //総合的な学習の時間
            if ("1".equals(_param._seq010_r1)) {
                svf.VrsOut("MASK", _param._whiteSpaceImagePath);
            } else {
                printHaveNewLine(svf, _param._HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE, student._totalstudytime, "SP_ACT", 74, 4);
                printHaveNewLine(svf, _param._HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE, student._specialactremark, "SP_ACT_VAL", 74, 4);
            }

            // 合計点、平均点
            for (int semei = 1; semei <= Math.min(Integer.parseInt(_param._semester), 2); semei++) {
                final String semester = String.valueOf(semei);
                final ScoreData scoreData08 = student.getTotalScoreData(_param, semester, SCORE990008);
                if (null != scoreData08) {
                    if (!"1".equals(_param._seq003_r1)) {
                        svf.VrsOutn("TOTAL_VAL" + semester, 1, scoreData08._score);
                    }
                    if (!"1".equals(_param._seq004_r1)) {
                        svf.VrsOutn("TOTAL_VAL" + semester, 2, sishaGonyu(scoreData08._avg));
                    }
                }
                final ScoreData scoreData01 = student.getTotalScoreData(_param, semester, SCORE010101);
                if (null != scoreData01) {
                    if (!"1".equals(_param._seq003_r1)) {
                        svf.VrsOutn("TOTAL_SCORE" + semester, 1, scoreData01._score);
                    }
                    if (!"1".equals(_param._seq004_r1)) {
                        svf.VrsOutn("TOTAL_SCORE" + semester, 2, sishaGonyu(scoreData01._avg));
                    }
                }
            }

            // 各科目
            int subclassCnt = 0;
            for (final SubclassMst subclassMst : subclassList) {
                final String subclassCd = subclassMst._subclasscd;
                subclassCnt++;
                final PrintSubclass printSubclass = student._printSubclassMap.get(subclassCd);

                final int subNameLen = KNJ_EditEdit.getMS932ByteLength(subclassMst._subclassname);
                final String subNameField = subNameLen > 26 ? "3" : subNameLen > 18 ? "2" : "1";
                svf.VrsOut("SUBCLASS_NAME2_" + subNameField, subclassMst._subclassname);
                svf.VrsOut("CREDIT2", _param.getCredits(student._grade, student._course, subclassCd));

                for (int semei = 1; semei <= Math.min(Integer.parseInt(_param._semester), 2); semei++) {
                    final String semester = String.valueOf(semei);

                    final ScoreData scoreData08 = printSubclass.getScoreData(semester, SCORE990008);
                    String val = null;
                    if (null != scoreData08) {
                        val = student.getKekkaOverCheckedScore(_param, subclassCd, semester, scoreData08._score);
                    }
                    svf.VrsOut("VAL2_" + semester, val);
                    final ScoreData scoreData01 = printSubclass.getScoreData(semester, SCORE010101);
                    String score = null;
                    if (null != scoreData01) {
                        score = scoreData01._score01;
                    }
                    svf.VrsOut("SCORE2_" + semester, score);
//                    log.info(" subclasscd " + subclassCd + ", seme = " + semester + ", score = " + score + ", val = " + val);
                }

                final SubclassAttendance attendance = student.getSubclassAttendance(subclassCd, "9");
                if (null != attendance) {
                    svf.VrsOut("NOTICE", attendance._sick.toString());
                    svf.VrsOut("LATE", attendance._late.toString());
                    svf.VrsOut("EARLY", attendance._early.toString());
                }

                svf.VrEndRecord();
            }
            _hasData = true;
            if (subclassCnt == 0) {
                svf.VrEndRecord();
            }
        }
    }

    private void printPaternDE(final DB2UDB db2, final Form svf, final List<Student> studentList) {
        String formName = null;
        if (PRINT_PATERN_D.equals(_param._seq001_r1)) {
            formName = "KNJD185E_4.frm";
        } else if (PRINT_PATERN_E.equals(_param._seq001_r1)) {
            formName = "KNJD185E_5.frm";
        }

        for (final Student student : studentList) {
            svf.VrSetForm(formName, 4);

            if ("1".equals(_param._knjd185ePatternDGakkkiTitle)) {
                svf.VrsOut("SEMESTERNAME1", "前期");
                svf.VrsOut("SEMESTERNAME2", "後期");
            }

            svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoImagePath);
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName);
            svf.VrsOut("NENDO", _param._nendo);
            svf.VrsOut("TITLE", TITLE);

            if (PRINT_PATERN_E.equals(_param._seq001_r1)) {
                svf.VrsOut("TOTAL_STUDY_NAME", _param._classColumnName);

                //総合的な学習の時間
                if ("1".equals(_param._seq010_r1)) {
                    svf.VrsOut("MASK", _param._whiteSpaceImagePath);
                } else {
                    printHaveNewLine(svf, _param._HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE, student._totalstudytime, "SP_ACT", 74, 4);
                    printHaveNewLine(svf, _param._HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE, student._specialactremark, "SP_ACT_VAL", 74, 4);
                }
            }

            final String priField = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 18 ? "2" : "1";
            svf.VrsOut("PR_NAME1_" + priField, _param._certifSchoolPrincipalName);
            printStaffName(svf, student);

            svf.VrsOut("HR_NAME", student._hrname + student._attendno);
            svf.VrsOut("NAME1", student._name);

            //出欠記録
            printAttend(svf, student);

            //備考欄
            printHaveNewLine(svf, _param._HREPORTREMARK_DAT_COMMUNICATION_SIZE, student._communication, "REMARK", 74, 3);

            //明細部分
            final List<SubclassMst> subclassList = subclassListRemoveD026(student);

            //平均点を出力(フォームがRecordTypeなので、事前出力が必要)
            for (int semei = 1; semei <= Integer.parseInt(_param._semester); semei++) {
                final String semester = String.valueOf(semei);
                final ScoreData score08 = student.getTotalScoreData(_param, semester, SCORE990008);
                if (null != score08) {
                    svf.VrsOut("TOTAL_VAL" + semester, sishaGonyu(score08._avg));
                }
                if (PRINT_PATERN_E.equals(_param._seq001_r1)) {
                    final ScoreData score01 = student.getTotalScoreData(_param, semester, SCORE010101);
                    if (null != score01) {
                        svf.VrsOut("TOTAL_SCORE" + semester, sishaGonyu(score01._avg));
                    }
                }
            }

            int subclassCnt = 0;

            for (final SubclassMst subclassMst : subclassList) {
                final String subclassCd = subclassMst._subclasscd;
                subclassCnt++;
                final PrintSubclass printSubclass = student._printSubclassMap.get(subclassCd);

                svf.VrsOut("CLASS_NAME2", subclassMst._classname);
                final int subNameLen = KNJ_EditEdit.getMS932ByteLength(subclassMst._subclassname);
                final String subNameField = subNameLen > 26 ? "3" : subNameLen > 18 ? "2" : "1";
                svf.VrsOut("SUBCLASS_NAME2_" + subNameField, subclassMst._subclassname);
                svf.VrsOut("CREDIT2", _param.getCredits(student._grade, student._course, subclassCd));

                for (int semei = 1; semei <= Integer.parseInt(_param._semester); semei++) {
                    final String semester = String.valueOf(semei);

                    final Map<String, ScoreData> printScoreMap = printSubclass._semesScoreMap.get(semester);

                    String score = null;
                    String hyotei = null;
                    if (null != printScoreMap) {
                        if (PRINT_PATERN_E.equals(_param._seq001_r1)) {
                            final ScoreData score01 = printScoreMap.get(SCORE010101);
                            if (null != score01) {
                                score = score01._score;
                            }
                        }
                        final ScoreData score08 = printScoreMap.get(SCORE990008);
                        if (null != score08) {
                            hyotei = score08._score;
                        }
                    }
                    if (PRINT_PATERN_E.equals(_param._seq001_r1)) {
                        svf.VrsOut("SCORE2_" + semester, score);
                    }
                    svf.VrsOut("VAL2_" + semester, hyotei);

                    //観点
                    final List<JviewGrade> jviewGradeList = student._subclasscdJviewGradeListMap.get(subclassCd);
                    int jvieCnt = 1;
                    final boolean hasSemesJviewMap = printSubclass._semesJviewMap.containsKey(semester);
                    final Map<String, JviewRecord> printJviewRecordMap = printSubclass._semesJviewMap.get(semester);

                    if (hasSemesJviewMap) {
                        for (final JviewGrade jviewGrade : jviewGradeList) {
                            final JviewRecord jviewRecord = printJviewRecordMap.get(jviewGrade._viewCd);
                            final String val;
                            if (null == jviewRecord || StringUtils.isEmpty(jviewRecord._statusName)) {
                                val = "-";
                            } else {
                                val = jviewRecord._statusName;
                            }
                            svf.VrsOut("STATUS2_" + semester + "_" + jvieCnt, val);
                            jvieCnt++;
                        }
                    } else {
                        if (null != jviewGradeList) {
                            for (Iterator itPrintJview = jviewGradeList.iterator(); itPrintJview.hasNext();) {
                                itPrintJview.next();
                                svf.VrsOut("STATUS2_" + semester + "_" + jvieCnt, "-");
                                jvieCnt++;
                            }
                        }
                    }

                    for (int bgslCnt = jvieCnt; bgslCnt <= 5; bgslCnt++) {
                        svf.VrsOut("VIEW_SLASH" + semester + "_" + bgslCnt, _param._backSlashImagePath);
                    }
                }

                if (PRINT_PATERN_D.equals(_param._seq001_r1)) {

                    for (int semei = 1; semei <= Integer.parseInt(_param._semester); semei++) {
                        final String semester = String.valueOf(semei);

                        final boolean hasSemesJviewMap = printSubclass._semesJviewMap.containsKey(semester);

                        final Map<String, ScoreData> printScoreMap = printSubclass._semesScoreMap.get(semester);

                        if (!hasSemesJviewMap && null == printScoreMap) {
                            svf.VrsOut("ABSENCE2_" + semester, "-");
                        } else {
                            final String sick = student.getSubclassSick(subclassCd, semester);
                            if (null != sick) {
                                svf.VrsOut("ABSENCE2_" + semester, sick);
                            }
                        }
                    }
                } else if (PRINT_PATERN_E.equals(_param._seq001_r1)) {
                    final SubclassAttendance attendance = student.getSubclassAttendance(subclassCd, "9");
                    if (null != attendance) {
                        svf.VrsOut("NOTICE", attendance._sick.toString());
                        svf.VrsOut("LATE", attendance._late.toString());
                        svf.VrsOut("EARLY", attendance._early.toString());
                    }
                }

                svf.VrEndRecord();
            }
            _hasData = true;
            if (subclassCnt == 0) {
                svf.VrEndRecord();
            }
            if (!"1".equals(_param._seq011_r1)) {
                printClassKanten(db2, svf, student);
            }
        }
    }

    private List<SubclassMst> subclassListRemoveD026(final Student student) {
        final Map<String, PrintSubclass> printSubclassMap = student._printSubclassMap;
        final List<SubclassMst> retList = new ArrayList<SubclassMst>();
        for (final String subclasscd : printSubclassMap.keySet()) {
            final SubclassMst subclassMst = _param._subclassMstMap.get(subclasscd);
            if (null == subclassMst) {
                if (_param._isOutputDebug) {
                    _param.logOnce(" no SUBCLASS_MST : " + subclasscd);
                }
                continue;
            }
            if (_param._d026List.contains(subclassMst._subclasscd)) {
                continue;
            }
            if (_param._isNoPrintMoto &&  subclassMst._isMoto || !_param._isPrintSakiKamoku &&  subclassMst._isSaki) {
                continue;
            }
            retList.add(subclassMst);
        }
        Collections.sort(retList);
        if (_param._isOutputDebug) {
            log.info(" student (grade, hr_class, attendno) = (" + student._grade + ", " + student._hrClass + ", " + student._attendno + "),  schregno = " + student._schregno + ", subclass size = " + retList.size());
            for (final SubclassMst m : retList) {
                log.info(" " + m);
            }
        }
        return retList;
    }

    // 出欠記録
    private void printAttend(final Form svf, final Student student) {
        for (final String semester : _param._semesterMap.keySet()) {
            final int line = getSemeLine(semester);
            if (!_param._seq006Map.containsKey(semester)) {
                continue;
            } else if (!"1".equals(_param._seq006Map.get(semester))) {
                continue;
            }
            if (SEMEALL.equals(semester)) {
                // 学年末の出欠は最後の学期か「学年末」以外は表示しない
                if (!(_param._isLastSemester || SEMEALL.equals(_param._semester))) {
                    continue;
                }
            } else {
                // 指定学期を超える学期は表示しない
                if (NumberUtils.isDigits(semester) && Integer.parseInt(_param._semester) < Integer.parseInt(semester)) {
                    continue;
                }
            }
            final Attendance att = student._attendMap.get(semester);
            if (null != att) {
                svf.VrsOutn("LESSON", line, String.valueOf(att._lesson));   // 授業日数
                svf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend + att._mourning)); // 忌引出停日数
                printAttendOrBackSlash(svf, line, String.valueOf(att._abroad), _param._seq007_r1, "ABROAD", "ABROAD_SLASH");     // 留学日数
                svf.VrsOutn("MUST", line, String.valueOf(att._mLesson));    // 出席しなければならない日数
                svf.VrsOutn("NOTICE", line, String.valueOf(att._absent));   // 欠席日数
                svf.VrsOutn("APPOINT", line, String.valueOf(att._present)); // 出席日数
                svf.VrsOutn("LATE", line, String.valueOf(att._late));       // 遅刻
                svf.VrsOutn("EARLY", line, String.valueOf(att._early));     // 早退
                printAttendOrBackSlash(svf, line, null == att._lhr ? "0" : att._lhr.toString(), _param._seq008_r1, "LHR_NOTICE", "LHR_SLASH");    // LHR欠課時数
                printAttendOrBackSlash(svf, line, null == att._event ? "0" : att._event.toString(), _param._seq009_r1, "EVENT_NOTICE", "EVENT_SLASH");// 行事欠課時数
            }
        }
    }

    //備考欄印刷
    private void printHaveNewLine(final Form svf, final String propertie, final String printText, final String fieldName, final int defLen, final int defRow) {
        if (!StringUtils.isEmpty(printText)) {
            final String[] nums = StringUtils.split(StringUtils.replace(propertie, "+", " "), " * ");
            int rLen = defLen;
            int rRow = defRow;
            if (null != nums && nums.length == 2) {
                rLen = Integer.parseInt(nums[0]) * 2;
                rRow = Integer.parseInt(nums[1]);
            }
            final String[] remarkArray = KNJ_EditEdit.get_token(printText, rLen, rRow);
            for (int i = 0; i < remarkArray.length; i++) {
                final String setRemark = remarkArray[i];
                svf.VrsOut(fieldName + (i + 1), setRemark);
            }
        }
    }

    /** 各教科の観点 */
    private void printClassKanten(final DB2UDB db2, final Form svf, final Student student) {
        svf.VrSetForm("KNJD185E_6.frm", 1);
        svf.VrsOut("TITLE", "各教科の観点");
        String befClassCd = "";
        int rowCnt = 1;
        int lineCnt = 1;
        for (final String subclassCd : student._printSubclassMap.keySet()) {
            final SubclassMst subclassMst = _param._subclassMstMap.get(subclassCd);
            if(subclassMst == null) continue;
            if (!befClassCd.equals(subclassMst._classcd) ||
                _param._d082.containsKey(subclassCd)
            ) {
                if (lineCnt > 4) {
                    rowCnt++;
                    lineCnt = 1;
                }
                if (rowCnt > 4) {
                    svf.VrEndPage();
                    rowCnt = 1;
                    lineCnt = 1;
                    svf.VrSetForm("KNJD185E_6.frm", 1);
                    svf.VrsOut("TITLE", "各教科の観点");
                }
                final String setClassName = _param._d082.containsKey(subclassCd) ? subclassMst._subclassname : subclassMst._classname;
                svf.VrsOutn("SUBCLASS_NAME" + lineCnt, rowCnt, setClassName);
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    final String sql = getJviewGradeSql(student._grade, subclassCd);
                    log.debug(" sql =" + sql);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();

                    int printLine = 1;
                    while (rs.next()) {
                        final String setName = StringUtils.defaultString(rs.getString("VIEWNAME"));
                        if (StringUtils.isEmpty(setName)) {
                            svf.VrsOutn("VIEW_SLASH" + lineCnt + "_" + printLine, rowCnt, _param._backSlashImagePath);
                        } else {
                            final String setField = setName.length() > 11 ? "2" : "1";
                            svf.VrsOutn("VIEW" + lineCnt + "_" + printLine + "_" + setField, rowCnt, setName);
                        }
                        printLine++;
                    }
                    for (int bgslCnt = printLine; bgslCnt <= 5; bgslCnt++) {
                        svf.VrsOutn("VIEW_SLASH" + lineCnt + "_" + bgslCnt, rowCnt, _param._backSlashImagePath);
                    }

                } catch (SQLException ex) {
                    log.debug("Exception:", ex);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
                lineCnt++;
            }
            befClassCd = subclassMst._classcd;
        }
        svf.VrEndPage();
    }

    private String getJviewGradeSql(final String grade, final String sbuclassCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     J2.VIEWNAME ");
        stb.append(" FROM ");
        stb.append("     JVIEWNAME_GRADE_YDAT J1, ");
        stb.append("     JVIEWNAME_GRADE_MST J2 ");
        stb.append(" WHERE ");
        stb.append("     J1.YEAR = '" + _param._loginYear + "' ");
        stb.append("     AND J1.GRADE = '" + grade + "' ");
        stb.append("     AND J1.CLASSCD || '-' || J1.SCHOOL_KIND || '-' || J1.CURRICULUM_CD || '-' || J1.SUBCLASSCD = '" + sbuclassCd + "' ");
        stb.append("     AND J2.GRADE = J1.GRADE ");
        stb.append("     AND J2.CLASSCD = J1.CLASSCD ");
        stb.append("     AND J2.SCHOOL_KIND = J1.SCHOOL_KIND ");
        stb.append("     AND J2.CURRICULUM_CD = J1.CURRICULUM_CD ");
        stb.append("     AND J2.SUBCLASSCD = J1.SUBCLASSCD ");
        stb.append("     AND J2.VIEWCD = J1.VIEWCD ");
        stb.append(" ORDER BY ");
        stb.append("     J2.VIEWCD ");
        return stb.toString();
    }

    private int getSemeLine(final String semester) {
        final int line;
        if (SEMEALL.equals(semester)) {
            line = 3;
        } else {
            line = Integer.parseInt(semester);
        }
        return line;
    }

    private void printAttendOrBackSlash(final Form svf, final int line, final String setVal, final String setBackSlash, final String attendField, final String bkSlashField) {
        if ("1".equals(setBackSlash)) {
            svf.VrsOut(bkSlashField + line, _param._backSlashImagePath);
        } else {
            svf.VrsOutn(attendField, line, setVal);   // 留学日数
        }
    }

    private void printStaffName(final Form svf, final Student student) {
        final String[] staffnames = {student._staffname, student._staffname2};
        int staffCnt = 1;
        for (int i = 0; i < staffnames.length; i++) {
            final String staffname = staffnames[i];
            if (!StringUtils.isBlank(staffname)) {
                svf.VrsOut("TR_TITLE" + staffCnt, _param._certifSchoolHrJobName);
                final String stfField = KNJ_EditEdit.getMS932ByteLength(staffname) > 14 ? "2" : "1";
                svf.VrsOut("TR_NAME" + staffCnt + "_" + stfField, staffname);
                svf.VrsOut("STAMP_CIRCLE" + staffCnt, "〇");
                //svf.VrsOut("STAMP_NAME" + staffCnt, "印");
                staffCnt++;
            }
        }
    }

    private List<Student> getList(final DB2UDB db2) {
        final List<Student> retList = new ArrayList();
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);

            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final Student student = new Student();
                student._schregno = getString(row, "SCHREGNO");
                student._name = getString(row, "NAME");
                student._hrname = getString(row, "HR_NAME");
                student._staffname = StringUtils.defaultString(getString(row, "STAFFNAME"));
                student._staffname2 = StringUtils.defaultString(getString(row, "STAFFNAME2"));
                student._attendno = NumberUtils.isDigits(getString(row, "ATTENDNO")) ? String.valueOf(Integer.parseInt(getString(row, "ATTENDNO"))) + "番" : getString(row, "ATTENDNO");
                student._grade = getString(row, "GRADE");
                student._hrClass = getString(row, "HR_CLASS");
                student._coursecd = getString(row, "COURSECD");
                student._majorcd = getString(row, "MAJORCD");
                student._course = getString(row, "COURSE");
                student._majorname = getString(row, "MAJORNAME");
                student._hrClassName1 = getString(row, "HR_CLASS_NAME1");
                student._entyear = getString(row, "ENT_YEAR");
                student._schoolRefusal = getString(row, "REFUSAL");
                student.setHreport(db2, _param);
                retList.add(student);
            }

            Student.setSeiseki(db2, _param, retList);

        } catch (Exception ex) {
            log.error("Exception:", ex);
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("     SELECT  REGD.SCHREGNO");
        stb.append("            ,REGD.SEMESTER ");
        stb.append("            ,BASE.NAME ");
        stb.append("            ,REGDH.HR_NAME ");
        stb.append("            ,STF1.STAFFNAME ");
        stb.append("            ,STF2.STAFFNAME AS STAFFNAME2 ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,REGD.GRADE ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("            ,REGD.COURSECD ");
        stb.append("            ,REGD.MAJORCD ");
        stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
        stb.append("            ,MAJOR.MAJORNAME ");
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
        stb.append("            ,REFUS.SCHREGNO AS REFUSAL ");
        stb.append("     FROM    SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
        stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON MAJOR.COURSECD = REGD.COURSECD ");
        stb.append("                  AND MAJOR.MAJORCD = REGD.MAJORCD ");
        stb.append("     LEFT JOIN STAFF_MST STF2 ON STF2.STAFFCD = REGDH.TR_CD2 ");
        stb.append("     LEFT JOIN SCHREG_SCHOOL_REFUSAL_DAT REFUS ON REFUS.YEAR = REGD.YEAR ");
        stb.append("          AND REFUS.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     WHERE   REGD.YEAR = '" + _param._loginYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND REGD.SEMESTER = '" + _param._loginSemester + "' ");
        } else {
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        }
        if ("1".equals(_param._disp)) {
            stb.append("         AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }
        stb.append("     ORDER BY ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO ");
        final String sql = stb.toString();
        log.debug(" student sql = " + sql);

        return stb.toString();
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    public static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<B>());
        }
        return map.get(key1);
    }

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return map.get(key1);
    }

    private static String sum(final Collection<String> numList) {
        if (numList.isEmpty()) {
            return null;
        }
        String sum = null;
        for (final String num : numList) {
            sum = add(sum, num);
        }
        return sum;
    }

    private static String avg(final Collection<String> numList) {
        if (numList.isEmpty()) {
            return null;
        }
        int size = 0;
        String sum = null;
        for (final String num : numList) {
            sum = add(sum, num);
            if (!NumberUtils.isNumber(num)) {
                continue;
            }
            size += 1;
        }
        if (size == 0) {
            return null;
        }
        return new BigDecimal(sum).divide(new BigDecimal(size), 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    public static String add(final String s1, final String s2) {
        if (!NumberUtils.isNumber(s1)) { return s2; }
        if (!NumberUtils.isNumber(s2)) { return s1; }
        return new BigDecimal(s1).add(new BigDecimal(s2)).toString();
    }

    private static class Student {
        String _schregno;
        String _name;
        String _hrname;
        String _staffname;
        String _staffname2;
        String _attendno;
        String _grade;
        String _hrClass;
        String _coursecd;
        String _majorcd;
        String _course;
        String _majorname;
        String _hrClassName1;
        String _entyear;
        String _schoolRefusal;
        String _communication;
        String _totalstudytime;
        String _specialactremark;
        final Map<String, Attendance> _attendMap = new TreeMap();
        final Map<String, PrintSubclass> _printSubclassMap = new TreeMap();
        final Map<String, Map<String, SubclassAttendance>> _attendSubClassMap = new TreeMap();
        final Map<String, List<JviewGrade>> _subclasscdJviewGradeListMap = new TreeMap();

        public Student() {
        }

        private ScoreData getTotalScoreData(final Param param, final String semester, final String testkey) {
            PrintSubclass printSubclass999999 = null;

            for (final String subclassCd : _printSubclassMap.keySet()) {
                final PrintSubclass printSubclass =  _printSubclassMap.get(subclassCd);
                final String[] subclassArray = StringUtils.split(subclassCd, "-");
                if (ALL9.equals(subclassArray[3])) {
                    printSubclass999999 = printSubclass;
                    break;
                }
            }
            // 合計科目がなければnull
            if (null == printSubclass999999) {
                return null;
            }
            ScoreData scoreData999999 = getMappedMap(printSubclass999999._semesScoreMap, semester).get(testkey);
            // 合計点がなければnull
            if (null == scoreData999999) {
                return null;
            }

            // チェック
            final String testcd = semester + testkey;
            final List<String> scores = new ArrayList<String>();
            final List<ScoreData> kekkaOverScoreDatas = new ArrayList<ScoreData>();
            final List<ScoreData> kekkaNotOverScoreDatas = new ArrayList<ScoreData>();
            final List<ScoreData> d070excludesMoto = new ArrayList<ScoreData>();
            final List<ScoreData> d070excludesSaki = new ArrayList<ScoreData>();
            final List<ScoreData> electdivExcludes = new ArrayList<ScoreData>();
            final List<ScoreData> d017Excludes = new ArrayList<ScoreData>();
            final Map<String, Map<String, String>> recordRankExecSdivDatMap = new HashMap<String, Map<String, String>>();
            for (final String subclassCd : _printSubclassMap.keySet()) {
                final PrintSubclass printSubclass =  _printSubclassMap.get(subclassCd);
                final String[] subclassArray = StringUtils.split(subclassCd, "-");
                if (ALL9.equals(subclassArray[3])) {
                    continue;
                }
                final ScoreData scoreData = printSubclass.getScoreData(semester, testkey);
                if (null == scoreData) {
                    continue;
                }
                final String namespare1 = param._d070_testcdNamespare1Map.get(testcd);
                if ("1".equals(namespare1)) {
                    // 合計点から合併元除く
                    if (null != printSubclass._mst && printSubclass._mst._isMoto) {
                        d070excludesMoto.add(scoreData);
                        continue;
                    }
                } else if ("2".equals(namespare1)) {
                    // 合計点から合併先除く
                    if (null != printSubclass._mst && printSubclass._mst._isSaki) {
                        d070excludesSaki.add(scoreData);
                        continue;
                    }
                }
                final String execKey = testcd + _grade + "-" + (null == scoreData._updated ? "0" : "1");
                if (!recordRankExecSdivDatMap.containsKey(execKey)) {
                    final Map<String, String> recordRankExecSdivDat = param.getRecordRankExecSdivDat(testcd, _grade, scoreData._updated);
                    if (param._isOutputDebug) {
                        param.logOnce(" recordRankExecSdivDat (" + _grade + ", " + testcd + ") = " + getString(recordRankExecSdivDat, "CALC_DATE") + " " + getString(recordRankExecSdivDat, "CALC_TIME") + " / " + testcd + ", " + _grade);
                    }
                    recordRankExecSdivDatMap.put(execKey, recordRankExecSdivDat);
                }


                final Map<String, String> recordRankExecSdivDat = recordRankExecSdivDatMap.get(execKey);
                if ("1".equals(getString(recordRankExecSdivDat, "ELECTDIV_FLG"))) {
                    // 合計点から選択科目除く
                    if (null != printSubclass._mst && "1".equals(printSubclass._mst._electdiv)) {
                        electdivExcludes.add(scoreData);
                        continue;
                    }
                }
                if (param._d017OrD065List.contains(subclassCd)) {
                    // 合計点からD017除く
                    d017Excludes.add(scoreData);
                    continue;
                }

                if (NumberUtils.isDigits(scoreData._score)) {
                    scores.add(scoreData._score);
                    if (isKekkaOver(subclassCd, semester)) {
                        kekkaOverScoreDatas.add(scoreData);
                    } else {
                        kekkaNotOverScoreDatas.add(scoreData);
                    }
                }
            }
            if (!kekkaOverScoreDatas.isEmpty()) {
                final String scoresSum = sum(scores);
                log.info(" 欠課超過 (grade, hr_class, attendno) = (" + _grade + ", " + _hrClass + ", " + _attendno + "), schregno = " + _schregno + ", testcd = " + testcd);
                if (param._isOutputDebug) {
                    final String scoresAvg = avg(scores);
                    log.info(" 999999 = " + scoreData999999);
                    log.info(" scores = " + scores + ", sum = " + scoresSum + ", avg = " + scoresAvg);
                    log.info("   D070 excludes moto = " + d070excludesMoto);
                    log.info("   D070 excludes saki = " + d070excludesSaki);
                    log.info("   electdiv excludes = " + electdivExcludes);
                    log.info("   D017 excludes = " + d017Excludes);
                    log.info(" ・欠課超過科目数 = " + kekkaOverScoreDatas.size());
                    final List<String> kekkaOverScores = new ArrayList<String>();
                    for (final ScoreData sd : kekkaOverScoreDatas) {
                        log.info("  " + sd);
                        kekkaOverScores.add(sd._score);
                    }
                    log.info("  計 = " + sum(kekkaOverScores));
                }
                if (NumberUtils.isNumber(scoreData999999._score) && NumberUtils.isNumber(scoresSum)) {
                    if (param._isOutputDebug) {
                        log.info(" ・欠課非超過科目数 = " + kekkaNotOverScoreDatas.size());
                    }
                    final List<String> kekkaNotOverScores = new ArrayList<String>();
                    for (final ScoreData sd : kekkaNotOverScoreDatas) {
                        if (param._isOutputDebug) {
                            log.info("  " + sd);
                        }
                        kekkaNotOverScores.add(sd._score);
                    }
                    final String calcSum = sum(kekkaNotOverScores);
                    if (param._isOutputDebug) {
                        log.info("  計 = " + calcSum);
                    }
                    if (Integer.parseInt(scoreData999999._score) == Integer.parseInt(scoresSum)) {
                        final String calcAvg = avg(kekkaNotOverScores);
                        final ScoreData newScoreData999999 = new ScoreData(scoreData999999._subclassCd, semester, scoreData999999._testKey, calcSum, calcAvg, null, scoreData999999._getCredit, scoreData999999._score01);
                        log.warn("超過科目数 " + kekkaOverScoreDatas.size() + " 合計点を帳票算出 :  " + newScoreData999999);
                        scoreData999999 = newScoreData999999;
                    } else {
                        log.warn("序列確定の合計点、帳票の合計点が不一致 :  " + scoreData999999._score + ", " + scoresSum);
                    }
                }
            }

            return scoreData999999;
        }

        private String getCreditTotal() {
            int totalCredit = 0;
            for (final String subclassCd : _printSubclassMap.keySet()) {
                final PrintSubclass printSubclass = _printSubclassMap.get(subclassCd);
                totalCredit += addCredit(printSubclass);
            }
            return String.valueOf(totalCredit);
        }

        private int addCredit(final PrintSubclass printSubclass) {
            final SubclassMst mst = printSubclass._mst;
            int totalCredit = 0;
            if (null != mst) {
                boolean add = false;
                if (mst._isSaki && "1".equals(mst._calculateCreditFlg)) {
                    add = true;
                } else if (mst._isMoto && "2".equals(mst._calculateCreditFlg)) {
                    add = true;
                } else if (!mst._isSaki && !mst._isMoto) {
                    add = true;
                } else {
                    log.info(" no addCredit " + mst);
                }
                if (add) {
                    final ScoreData scoreData = printSubclass.getScoreData(SEMEALL, SCORE990008);
                    if (null != scoreData) {
                        totalCredit += StringUtils.isEmpty(scoreData._getCredit) ? 0 : Integer.parseInt(scoreData._getCredit);
                    }
                }
            }
            return totalCredit;
        }

        private SubclassAttendance getSubclassAttendance(final String subclassCd, final String semester) {
            SubclassAttendance att = null;
            if (_attendSubClassMap.containsKey(subclassCd)) {
                final Map<String, SubclassAttendance> atSubSemeMap = _attendSubClassMap.get(subclassCd);
                att = atSubSemeMap.get(semester);
            }
            return att;
        }

        private String getSubclassSick(final String subclassCd, final String semester) {
            String sick = null;
            if (_attendSubClassMap.containsKey(subclassCd)) {
                final Map<String, SubclassAttendance> atSubSemeMap = _attendSubClassMap.get(subclassCd);
                if (atSubSemeMap.containsKey(semester)) {
                    final SubclassAttendance attendance = atSubSemeMap.get(semester);
                    sick = attendance._sick.toString();
                }
            }
            return sick;
        }

        private boolean isKekkaOver(final String subclassCd, final String semester) {
            if (_attendSubClassMap.containsKey(subclassCd)) {
                final Map<String, SubclassAttendance> atSubSemeMap = _attendSubClassMap.get(subclassCd);
                if (atSubSemeMap.containsKey(semester)) {
                    final SubclassAttendance attendance = atSubSemeMap.get(semester);
                    return attendance._isOver;
                }
            }
            return false;
        }

        private String getKekkaOverCheckedScore(final Param param, final String subclassCd, final String semester, final String score) {
            String rtn = score;
            if(isKekkaOver(subclassCd, semester) && !StringUtils.isBlank(score)) {
                if (param._isOutputDebug) {
                    log.info(" kekka over score : " + _schregno + ", " + subclassCd + ", semester " + semester + ", score = " + score);
                }
                rtn = "";
            }
            return rtn;
        }

        private static Timestamp getTimestamp(final String val) {
            return null == val ? null : Timestamp.valueOf(val);
        }

        private static void setSeiseki(final DB2UDB db2, final Param param, final List<Student> studentList) throws Exception {
            final String scoreSql = getRankSdivSql(param);
            if (param._isOutputDebugQuery) {
                if (PRINT_PATERN_D.equals(param._seq001_r1) || PRINT_PATERN_E.equals(param._seq001_r1)) {
                    log.info(" scoreSql 2 = " + scoreSql);
                } else {
                    log.info(" scoreSql 1 = " + scoreSql);
                }
            }
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(scoreSql);
            } catch (Exception e) {
                log.error("exception!", e);
                DbUtils.closeQuietly(ps);
                throw e;
            }

            for (final Student student : studentList) {

//            	final List<String> subclasscdList = new ArrayList<String>();
                Object[] arg;
                if (PRINT_PATERN_D.equals(param._seq001_r1) || PRINT_PATERN_E.equals(param._seq001_r1)) {
                    arg = new Object[] {student._schregno, student._schregno};
                } else {
                    if ("1".equals(param._knjd185eNotPrintClass90)) {
                        arg = new Object[] {student._schregno, student._schregno};
                    } else {
                        arg = new Object[] {student._schregno, student._schregno, student._schregno, student._schregno};
                    }
                }
                for (final Map<String, String> row : KnjDbUtils.query(db2, ps, arg)) {
                    final String subclassCd = getString(row, "SUBCLASSCD");
                    final String semester = getString(row, "SEMESTER");

                    if (!student._printSubclassMap.containsKey(subclassCd)) {
                        student._printSubclassMap.put(subclassCd, new PrintSubclass(subclassCd, param));
                    }
                    final PrintSubclass printSubclass = student._printSubclassMap.get(subclassCd);

                    final String testKey = getString(row, "TESTKEY");
                    final String score = getString(row, "SCORE");
                    final String avg = getString(row, "AVG");
                    final Timestamp updated = getTimestamp(getString(row, "UPDATED"));

                    if (PRINT_PATERN_D.equals(param._seq001_r1) || PRINT_PATERN_E.equals(param._seq001_r1)) {

                        getMappedMap(printSubclass._semesScoreMap, semester).put(testKey, new ScoreData(subclassCd, semester, testKey, score, avg, updated, null, null));

                    } else {

                        final String getCredit = getString(row, "GET_CREDIT");
                        final String score01 = getString(row, "SCORE01");

                        getMappedMap(printSubclass._semesScoreMap, semester).put(testKey, new ScoreData(subclassCd, semester, testKey, score, avg, updated, getCredit, score01));
                    }
                }
            }
            DbUtils.closeQuietly(ps);

            if (PRINT_PATERN_D.equals(param._seq001_r1) || PRINT_PATERN_E.equals(param._seq001_r1)) {
                for (final Student student : studentList) {

                    final String jviewSql = getJviewStatRecordSql(param, student._schregno);

                    for (final Map<String, String> row : KnjDbUtils.query(db2, jviewSql)) {
                        final String subclassCd = getString(row, "SUBCLASSCD");
                        final String semester = getString(row, "SEMESTER");

                        if (!student._printSubclassMap.containsKey(subclassCd)) {
                            student._printSubclassMap.put(subclassCd, new PrintSubclass(subclassCd, param));
                        }
                        final PrintSubclass printSubclass = student._printSubclassMap.get(subclassCd);

                        final String viewCd = getString(row, "VIEWCD");
                        final String status = getString(row, "STATUS");
                        final String statusName = getString(row, "NAME1");
                        getMappedMap(printSubclass._semesJviewMap, semester).put(viewCd, new JviewRecord(subclassCd, semester, viewCd, status, statusName));

                        final List<JviewGrade> jviewGradeList = getMappedList(student._subclasscdJviewGradeListMap, subclassCd);
                        boolean chkflg = true;
                        for (final JviewGrade cmpwk : jviewGradeList) {
                            if (cmpwk._viewCd.equals(viewCd)) {
                                chkflg = false;
                                break;
                            }
                        }
                        if (chkflg) {
                            final JviewGrade jviewGrade = new JviewGrade(subclassCd, viewCd, "");
                            jviewGradeList.add(jviewGrade);
                        }

                    }
                }
            }
        }

        private static String getRankSdivSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            final String _q = "?";

            if (PRINT_PATERN_D.equals(param._seq001_r1) || PRINT_PATERN_E.equals(param._seq001_r1)) {

                stb.append(" WITH RANK_SEMESTER_SUBCLASSCD AS ( ");
                stb.append("     SELECT ");
                stb.append("         RANK_SDIV.YEAR, ");
                stb.append("         RANK_SDIV.SCHREGNO, ");
                stb.append("         RANK_SDIV.CLASSCD, RANK_SDIV.SCHOOL_KIND, RANK_SDIV.CURRICULUM_CD, RANK_SDIV.SUBCLASSCD, ");
                stb.append("         RANK_SDIV.SEMESTER ");
                stb.append("     FROM ");
                stb.append("         RECORD_RANK_SDIV_DAT RANK_SDIV ");
                stb.append("     WHERE ");
                stb.append("         RANK_SDIV.YEAR = '" + param._loginYear + "' ");
                stb.append("         AND ( ");
                stb.append("               RANK_SDIV.TESTKINDCD = '" + SCORE990008.substring(0, 2) + "' AND RANK_SDIV.TESTITEMCD = '" + SCORE990008.substring(2, 4) + "' AND RANK_SDIV.SCORE_DIV = '" + SCORE990008.substring(4) + "' ");
                stb.append("            OR RANK_SDIV.TESTKINDCD = '" + SCORE010101.substring(0, 2) + "' AND RANK_SDIV.TESTITEMCD = '" + SCORE010101.substring(2, 4) + "' AND RANK_SDIV.SCORE_DIV = '" + SCORE010101.substring(4) + "' ");
                stb.append("             ) ");
                stb.append("         AND RANK_SDIV.SCHREGNO = " + _q + " ");
                stb.append("         AND (RANK_SDIV.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' OR RANK_SDIV.SUBCLASSCD = '" + ALL9 + "') ");
                stb.append("         AND RANK_SDIV.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");
                stb.append("     UNION ");
                stb.append("     SELECT ");
                stb.append("         REC.YEAR, ");
                stb.append("         REC.SCHREGNO, ");
                stb.append("         REC.CLASSCD, REC.SCHOOL_KIND, REC.CURRICULUM_CD, REC.SUBCLASSCD, ");
                stb.append("         REC.SEMESTER ");
                stb.append("     FROM ");
                stb.append("         RECORD_SCORE_DAT REC ");
                stb.append("     WHERE ");
                stb.append("         REC.YEAR = '" + param._loginYear + "' ");
                stb.append("         AND ( ");
                stb.append("               REC.TESTKINDCD = '" + SCORE990008.substring(0, 2) + "' AND REC.TESTITEMCD = '" + SCORE990008.substring(2, 4) + "' AND REC.SCORE_DIV = '" + SCORE990008.substring(4) + "' ");
                stb.append("            OR REC.TESTKINDCD = '" + SCORE010101.substring(0, 2) + "' AND REC.TESTITEMCD = '" + SCORE010101.substring(2, 4) + "' AND REC.SCORE_DIV = '" + SCORE010101.substring(4) + "' ");
                stb.append("             ) ");
                stb.append("         AND REC.SCHREGNO = " + _q + " ");
                stb.append("         AND REC.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     REC.CLASSCD || '-' || REC.SCHOOL_KIND || '-' || REC.CURRICULUM_CD || '-' || REC.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     REC.SEMESTER, ");
                stb.append("     '" + SCORE010101 + "' AS TESTKEY, ");
                stb.append("     RANK_SDIV.SCORE, ");
                stb.append("     RANK_SDIV.AVG, ");
                stb.append("     RANK_SDIV.UPDATED ");
                stb.append(" FROM ");
                stb.append("     RANK_SEMESTER_SUBCLASSCD REC ");
                stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT RANK_SDIV ON REC.YEAR = RANK_SDIV.YEAR ");
                stb.append("          AND REC.SEMESTER = RANK_SDIV.SEMESTER ");
                stb.append("          AND RANK_SDIV.TESTKINDCD = '01' ");
                stb.append("          AND RANK_SDIV.TESTITEMCD = '01' ");
                stb.append("          AND RANK_SDIV.SCORE_DIV = '01' ");
                stb.append("          AND REC.CLASSCD = RANK_SDIV.CLASSCD AND REC.SCHOOL_KIND = RANK_SDIV.SCHOOL_KIND AND REC.CURRICULUM_CD = RANK_SDIV.CURRICULUM_CD AND REC.SUBCLASSCD = RANK_SDIV.SUBCLASSCD ");
                stb.append("          AND REC.SCHREGNO = RANK_SDIV.SCHREGNO ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     REC.CLASSCD || '-' || REC.SCHOOL_KIND || '-' || REC.CURRICULUM_CD || '-' || REC.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     REC.SEMESTER, ");
                stb.append("     '" + SCORE990008 + "' AS TESTKEY, ");
                stb.append("     RANK_SDIV.SCORE, ");
                stb.append("     RANK_SDIV.AVG, ");
                stb.append("     RANK_SDIV.UPDATED ");
                stb.append(" FROM ");
                stb.append("     RANK_SEMESTER_SUBCLASSCD REC ");
                stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT RANK_SDIV ON REC.YEAR = RANK_SDIV.YEAR ");
                stb.append("          AND REC.SEMESTER = RANK_SDIV.SEMESTER ");
                stb.append("          AND RANK_SDIV.TESTKINDCD = '99' ");
                stb.append("          AND RANK_SDIV.TESTITEMCD = '00' ");
                stb.append("          AND RANK_SDIV.SCORE_DIV = '08' ");
                stb.append("          AND REC.CLASSCD = RANK_SDIV.CLASSCD AND REC.SCHOOL_KIND = RANK_SDIV.SCHOOL_KIND AND REC.CURRICULUM_CD = RANK_SDIV.CURRICULUM_CD AND REC.SUBCLASSCD = RANK_SDIV.SUBCLASSCD ");
                stb.append("          AND REC.SCHREGNO = RANK_SDIV.SCHREGNO ");

            } else {

                stb.append(" WITH RANK_SEMESTER_SUBCLASSCD AS ( ");
                stb.append("     SELECT ");
                stb.append("         RANK_SDIV.YEAR, ");
                stb.append("         RANK_SDIV.CLASSCD, RANK_SDIV.SCHOOL_KIND, RANK_SDIV.CURRICULUM_CD, RANK_SDIV.SUBCLASSCD, ");
                stb.append("         RANK_SDIV.SEMESTER, ");
                stb.append("         RANK_SDIV.SCHREGNO ");
                stb.append("     FROM RECORD_RANK_SDIV_DAT RANK_SDIV ");
                stb.append("     WHERE ");
                stb.append("             RANK_SDIV.YEAR = '" + param._loginYear + "' ");
                stb.append("         AND RANK_SDIV.TESTKINDCD = '" + SCORE990008.substring(0, 2) + "' AND RANK_SDIV.TESTITEMCD = '" + SCORE990008.substring(2, 4) + "' AND RANK_SDIV.SCORE_DIV = '" + SCORE990008.substring(4) + "' ");
                stb.append("         AND RANK_SDIV.SCHREGNO = " + _q + " ");
                stb.append("         AND (RANK_SDIV.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' OR RANK_SDIV.SUBCLASSCD = '" + ALL9 + "' OR RANK_SDIV.CLASSCD = '" + SELECT_CLASSCD_TOTALSTDY + "') ");
                stb.append("         AND RANK_SDIV.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");
                stb.append("     UNION ");
                stb.append("     SELECT ");
                stb.append("         REC.YEAR, ");
                stb.append("         REC.CLASSCD, REC.SCHOOL_KIND, REC.CURRICULUM_CD, REC.SUBCLASSCD, ");
                stb.append("         REC.SEMESTER, ");
                stb.append("         REC.SCHREGNO ");
                stb.append("     FROM RECORD_SCORE_DAT REC ");
                stb.append("     WHERE ");
                stb.append("             REC.YEAR = '" + param._loginYear + "' ");
                stb.append("         AND REC.TESTKINDCD = '" + SCORE990008.substring(0, 2) + "' AND REC.TESTITEMCD = '" + SCORE990008.substring(2, 4) + "' AND REC.SCORE_DIV = '" + SCORE990008.substring(4) + "' ");
                stb.append("         AND REC.SCHREGNO = " + _q + " ");
                stb.append("         AND (REC.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' OR REC.CLASSCD = '" + SELECT_CLASSCD_TOTALSTDY + "') ");
                if (!"1".equals(param._knjd185eNotPrintClass90)) {
                    stb.append("     UNION ");
                    stb.append("     SELECT ");
                    stb.append("         CHR.YEAR, ");
                    stb.append("         CHR.CLASSCD, CHR.SCHOOL_KIND, CHR.CURRICULUM_CD, CHR.SUBCLASSCD, ");
                    stb.append("         CHR.SEMESTER, ");
                    stb.append("         STD.SCHREGNO ");
                    stb.append("     FROM CHAIR_DAT CHR ");
                    stb.append("     INNER JOIN CHAIR_STD_DAT STD ON STD.YEAR = CHR.YEAR ");
                    stb.append("         AND STD.SEMESTER = CHR.SEMESTER ");
                    stb.append("         AND STD.CHAIRCD = CHR.CHAIRCD ");
                    stb.append("     WHERE ");
                    stb.append("             CHR.YEAR = '" + param._loginYear + "' ");
                    stb.append("         AND STD.SCHREGNO = " + _q + " ");
                    stb.append("         AND CHR.CLASSCD = '" + SELECT_CLASSCD_TOTALSTDY + "' ");
                    stb.append("     UNION ");
                    stb.append("     SELECT ");
                    stb.append("         REC.YEAR, ");
                    stb.append("         REC.CLASSCD, REC.SCHOOL_KIND, REC.CURRICULUM_CD, REC.SUBCLASSCD, ");
                    stb.append("         REC.SEMESTER, ");
                    stb.append("         REC.SCHREGNO ");
                    stb.append("     FROM RECORD_SCORE_DAT REC ");
                    stb.append("     WHERE ");
                    stb.append("             REC.YEAR = '" + param._loginYear + "' ");
                    stb.append("         AND REC.TESTKINDCD = '" + SCORE990009.substring(0, 2) + "' AND REC.TESTITEMCD = '" + SCORE990009.substring(2, 4) + "' AND REC.SCORE_DIV = '" + SCORE990009.substring(4) + "' ");
                    stb.append("         AND REC.SCHREGNO = " + _q + " ");
                    stb.append("         AND REC.CLASSCD = '" + SELECT_CLASSCD_TOTALSTDY + "' ");
                }
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     SUBS.CLASSCD || '-' || SUBS.SCHOOL_KIND || '-' || SUBS.CURRICULUM_CD || '-' || SUBS.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     SUBS.SEMESTER, ");
                stb.append("     '" + SCORE990008 + "' AS TESTKEY, ");
                stb.append("     RANK_SDIV.SCORE, ");
                stb.append("     RANK_SDIV.AVG, ");
                stb.append("     RANK_SDIV.UPDATED, ");
                stb.append("     CAST(NULL AS SMALLINT) AS SCORE01, ");
                stb.append("     SCORE09.GET_CREDIT ");
                stb.append(" FROM RANK_SEMESTER_SUBCLASSCD SUBS ");
                stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT RANK_SDIV ON RANK_SDIV.YEAR = SUBS.YEAR ");
                stb.append("          AND RANK_SDIV.SEMESTER = SUBS.SEMESTER ");
                stb.append("          AND RANK_SDIV.TESTKINDCD = '" + SCORE990008.substring(0, 2) + "' ");
                stb.append("          AND RANK_SDIV.TESTITEMCD = '" + SCORE990008.substring(2, 4) + "' ");
                stb.append("          AND RANK_SDIV.SCORE_DIV = '" + SCORE990008.substring(4) + "' ");
                stb.append("          AND RANK_SDIV.CLASSCD = SUBS.CLASSCD ");
                stb.append("          AND RANK_SDIV.SCHOOL_KIND = SUBS.SCHOOL_KIND ");
                stb.append("          AND RANK_SDIV.CURRICULUM_CD = SUBS.CURRICULUM_CD ");
                stb.append("          AND RANK_SDIV.SUBCLASSCD = SUBS.SUBCLASSCD ");
                stb.append("          AND RANK_SDIV.SCHREGNO = SUBS.SCHREGNO ");
                stb.append("     LEFT JOIN RECORD_SCORE_DAT SCORE09 ON SUBS.YEAR = SCORE09.YEAR ");
                stb.append("          AND SUBS.SEMESTER = SCORE09.SEMESTER ");
                stb.append("          AND SCORE09.TESTKINDCD = '" + SCORE990009.substring(0, 2) + "' AND SCORE09.TESTITEMCD = '" + SCORE990009.substring(2, 4) + "' AND SCORE09.SCORE_DIV = '" + SCORE990009.substring(4) + "' ");
                stb.append("          AND SUBS.CLASSCD = SCORE09.CLASSCD AND SUBS.SCHOOL_KIND = SCORE09.SCHOOL_KIND AND SUBS.CURRICULUM_CD = SCORE09.CURRICULUM_CD AND SUBS.SUBCLASSCD = SCORE09.SUBCLASSCD ");
                stb.append("          AND SUBS.SCHREGNO = SCORE09.SCHREGNO ");
                if (PRINT_PATERN_C.equals(param._seq001_r1)) {
                    stb.append(" UNION ALL ");
                    stb.append(" SELECT ");
                    stb.append("     SUBS.CLASSCD || '-' || SUBS.SCHOOL_KIND || '-' || SUBS.CURRICULUM_CD || '-' || SUBS.SUBCLASSCD AS SUBCLASSCD, ");
                    stb.append("     SUBS.SEMESTER, ");
                    stb.append("     '" + SCORE010101 + "' AS TESTKEY, ");
                    stb.append("     RANK_SDIV.SCORE, ");
                    stb.append("     RANK_SDIV.AVG, ");
                    stb.append("     RANK_SDIV.UPDATED, ");
                    stb.append("     SCORE01.SCORE AS SCORE01, ");
                    stb.append("     CAST(NULL AS SMALLINT) AS GET_CREDIT ");
                    stb.append(" FROM RANK_SEMESTER_SUBCLASSCD SUBS ");
                    stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT RANK_SDIV ON RANK_SDIV.YEAR = SUBS.YEAR ");
                    stb.append("          AND RANK_SDIV.SEMESTER = SUBS.SEMESTER ");
                    stb.append("          AND RANK_SDIV.TESTKINDCD = '" + SCORE010101.substring(0, 2) + "' ");
                    stb.append("          AND RANK_SDIV.TESTITEMCD = '" + SCORE010101.substring(2, 4) + "' ");
                    stb.append("          AND RANK_SDIV.SCORE_DIV = '" + SCORE010101.substring(4) + "' ");
                    stb.append("          AND RANK_SDIV.CLASSCD = SUBS.CLASSCD ");
                    stb.append("          AND RANK_SDIV.SCHOOL_KIND = SUBS.SCHOOL_KIND ");
                    stb.append("          AND RANK_SDIV.CURRICULUM_CD = SUBS.CURRICULUM_CD ");
                    stb.append("          AND RANK_SDIV.SUBCLASSCD = SUBS.SUBCLASSCD ");
                    stb.append("          AND RANK_SDIV.SCHREGNO = SUBS.SCHREGNO ");
                    stb.append("     LEFT JOIN RECORD_SCORE_DAT SCORE01 ON RANK_SDIV.YEAR = SCORE01.YEAR ");
                    stb.append("          AND RANK_SDIV.SEMESTER = SCORE01.SEMESTER ");
                    stb.append("          AND SCORE01.TESTKINDCD = '" + SCORE010101.substring(0, 2) + "' AND SCORE01.TESTITEMCD = '" + SCORE010101.substring(2, 4) + "' AND SCORE01.SCORE_DIV = '" + SCORE010101.substring(4) + "' ");
                    stb.append("          AND RANK_SDIV.CLASSCD = SCORE01.CLASSCD AND RANK_SDIV.SCHOOL_KIND = SCORE01.SCHOOL_KIND AND RANK_SDIV.CURRICULUM_CD = SCORE01.CURRICULUM_CD AND RANK_SDIV.SUBCLASSCD = SCORE01.SUBCLASSCD ");
                    stb.append("          AND RANK_SDIV.SCHREGNO = SCORE01.SCHREGNO ");
                }
                stb.append(" ORDER BY ");
                stb.append("     SUBCLASSCD, ");
                stb.append("     SEMESTER, ");
                stb.append("     TESTKEY ");
            }

            return stb.toString();
        }

        private static String getJviewStatRecordSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     REC.CLASSCD || '-' || REC.SCHOOL_KIND || '-' || REC.CURRICULUM_CD || '-' || REC.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     REC.SEMESTER, ");
            stb.append("     REC.VIEWCD, ");
            stb.append("     REC.STATUS, ");
            stb.append("     D029.NAME1 ");
            stb.append(" FROM ");
            stb.append("     JVIEWSTAT_RECORD_DAT REC ");
            stb.append("     LEFT JOIN NAME_MST D029 ON D029.NAMECD1 = 'D029' ");
            stb.append("          AND REC.STATUS = D029.ABBV1 ");
            stb.append(" WHERE ");
            stb.append("     REC.YEAR = '" + param._loginYear + "' ");
            stb.append("     AND REC.SCHREGNO = " + schregno + " ");
            stb.append(" ORDER BY ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     VIEWCD, ");
            stb.append("     SEMESTER ");

            return stb.toString();
        }

        private void setHreport(final DB2UDB db2, final Param param) {
            _communication = "";
            final String scoreSql = getHreportSql(param, _schregno);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _totalstudytime = rs.getString("TOTALSTUDYTIME");
                    _specialactremark = rs.getString("SPECIALACTREMARK");
                    _communication = rs.getString("COMMUNICATION");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

        }

        private static String getHreportSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TOTALSTUDYTIME, ");
            stb.append("     SPECIALACTREMARK, ");
            stb.append("     COMMUNICATION ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._loginYear + "' ");
            stb.append("     AND SEMESTER = '9' ");
            stb.append("     AND SCHREGNO = '" + schregno + "' ");

            return stb.toString();
        }
    }

    private static class PrintSubclass {
        final String _subclassCd;
        final SubclassMst _mst;
        final Map<String, Map<String, JviewRecord>> _semesJviewMap;
//        final Map _semesHyoukaMap;
        /**
         * _semesScoreMap[学期]scoreMap[テストコード]ScoreData
         */
        final Map<String, Map<String, ScoreData>> _semesScoreMap;
        private PrintSubclass(
                final String subclassCd,
                final Param param
        ) {
            _subclassCd = subclassCd;
            _mst = param._subclassMstMap.get(subclassCd);
            _semesJviewMap = new TreeMap();
//            _semesHyoukaMap = new TreeMap();
            _semesScoreMap = new TreeMap();
        }

        ScoreData getScoreData(final String semester, final String testcd) {
            if (_semesScoreMap.containsKey(semester)) {
                final Map<String, ScoreData> scoreMap = _semesScoreMap.get(semester);
                if (scoreMap.containsKey(testcd)) {
                    final ScoreData scoreData = scoreMap.get(testcd);
                    return scoreData;
                }
            }
            return null;
        }
    }

    private static class JviewRecord {
        final String _subclassCd;
        final String _semester;
        final String _viewCd;
        final String _status;
        final String _statusName;
        private JviewRecord(
                final String subclassCd,
                final String semester,
                final String viewCd,
                final String status,
                final String statusName
        ) {
            _subclassCd = subclassCd;
            _semester = semester;
            _viewCd = viewCd;
            _status = status;
            _statusName = statusName;
        }
    }

//    private class RankSdiv {
//        final String _score;
//        final String _hyouka;
//        private RankSdiv(
//                final String score,
//                final String hyouka
//        ) {
//            _score = score;
//            _hyouka = hyouka;
//        }
//    }

    private static class ScoreData {
        final String _subclassCd;
        final String _semester;
        final String _testKey;
        final String _score;
        final String _avg;
        final Timestamp _updated;
        final String _getCredit;
        /** Cパターン用 */
        final String _score01;
        private ScoreData(
                final String subclassCd,
                final String semester,
                final String testKey,
                final String score,
                final String avg,
                final Timestamp updated,
                final String getCredit,
                final String score01
        ) {
            _subclassCd = subclassCd;
            _semester = semester;
            _testKey = testKey;
            _score = score;
            _avg = avg;
            _updated = updated;
            _getCredit = getCredit;
            _score01 = score01;
        }
        public String toString() {
            return "Score(subclassCd = " + _subclassCd + ", score = " + _score + ", avg = " + _avg + ")";
        }
    }

    private static class Attendance {
        static final String GROUP_LHR = "001";
        static final String GROUP_EVENT = "002";

        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _abroad;
        BigDecimal _lhr;
        BigDecimal _event;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int abroad
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _abroad = abroad;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List<Student> studentList,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement psAtSeme = null;
            ResultSet rsAtSeme = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._loginYear,
                        param._semester,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
                log.debug(" attend sql = " + sql);
                psAtSeme = db2.prepareStatement(sql);

                for (final Student student : studentList) {

                    psAtSeme.setString(1, student._schregno);
                    rsAtSeme = psAtSeme.executeQuery();

                    while (rsAtSeme.next()) {
                        if (!SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {
                            continue;
                        }

                        final Attendance attendance = new Attendance(
                                rsAtSeme.getInt("LESSON"),
                                rsAtSeme.getInt("MLESSON"),
                                rsAtSeme.getInt("SUSPEND"),
                                rsAtSeme.getInt("MOURNING"),
                                rsAtSeme.getInt("SICK"),
                                rsAtSeme.getInt("PRESENT"),
                                rsAtSeme.getInt("LATE"),
                                rsAtSeme.getInt("EARLY"),
                                rsAtSeme.getInt("TRANSFER_DATE")
                        );
                        student._attendMap.put(dateRange._key, attendance);
                    }
                    DbUtils.closeQuietly(rsAtSeme);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(psAtSeme);
                db2.commit();
            }
        }
    }

    private static class SubclassAttendance {
        final String _subclasscd;
        final BigDecimal _lesson;
        final BigDecimal _attend;
        final BigDecimal _sick;
        final BigDecimal _late;
        final BigDecimal _early;
        boolean _isOver;

        public SubclassAttendance(final String subclasscd, final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick, final BigDecimal late, final BigDecimal early) {
            _subclasscd = subclasscd;
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
                final List<Student> studentList,
                final DateRange dateRange) {
            log.info(" subclass attendance dateRange = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._loginYear,
                        SEMEALL,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );

                ps = db2.prepareStatement(sql);

                for (final Student student : studentList) {

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    final Map<String, Map<String, String>> specialGroupKekkaMinutes = new HashMap();

                    while (rs.next()) {
                        if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                            continue;
                        }
                        final String subclasscd = rs.getString("SUBCLASSCD");

                        final SubclassMst mst = param._subclassMstMap.get(subclasscd);
                        if (null == mst) {
                            log.warn("no subclass : " + subclasscd);
                            continue;
                        }
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                            if (null != student._printSubclassMap.get(subclasscd)) {
                                final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                                final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                                final BigDecimal sick = rs.getBigDecimal("SICK2");
                                final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                                final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");
                                final BigDecimal late = rs.getBigDecimal("LATE");
                                final BigDecimal early = rs.getBigDecimal("EARLY");

                                final BigDecimal sick1 = mst._isSaki ? rawReplacedSick : rawSick;
                                final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                                final BigDecimal sick2 = mst._isSaki ? replacedSick : sick;
                                final Integer credits = (Integer) rs.getObject("CREDITS");

                                final SubclassAttendance subclassAttendance = new SubclassAttendance(subclasscd, lesson, attend, sick2, late, early);

                                //欠課時数上限
                                final Double absent = Double.valueOf(mst._isSaki ? rs.getString("REPLACED_SICK"): rs.getString("SICK2"));
                                if (SEMEALL.equals(dateRange._key)) {
                                    subclassAttendance._isOver = subclassAttendance.isNarakenKekkaOver(param, student, credits, absent);
                                } else {
                                    subclassAttendance._isOver = subclassAttendance.judgeOver(absent, lesson);
                                }

                                getMappedMap(student._attendSubClassMap, subclasscd).put(dateRange._key, subclassAttendance);
                            }
                        }

                        final String specialGroupCd = rs.getString("SPECIAL_GROUP_CD");
                        if (null != specialGroupCd) {
                            // 特別活動科目の処理 (授業分数と結果数の加算)
                            final String subclassCd = rs.getString("SUBCLASSCD");
                            final String kekkaMinutes = rs.getString("SPECIAL_SICK_MINUTES1");

                            getMappedMap(specialGroupKekkaMinutes, specialGroupCd).put(subclassCd, kekkaMinutes);
                        }
                    }


                    for (final Map.Entry<String, Map<String, String>> e : specialGroupKekkaMinutes.entrySet()) {
                        final String specialGroupCd = e.getKey();
                        final Map<String, String> subclassKekkaMinutesMap = e.getValue();

                        int totalMinutes = 0;
                        for (final Map.Entry<String, String> subMinutes : subclassKekkaMinutesMap.entrySet()) {
                            final String minutes = subMinutes.getValue();
                            if (NumberUtils.isDigits(minutes)) {
                                totalMinutes += Integer.parseInt(minutes);
                            }
                        }

                        final BigDecimal spGroupKekkaJisu = getSpecialAttendExe(totalMinutes, param);

                        if (null == student._attendMap.get(dateRange._key)) {
                            student._attendMap.put(dateRange._key, new Attendance(0, 0, 0, 0, 0, 0, 0, 0, 0));
                        }
                        final Attendance attendance = student._attendMap.get(dateRange._key);

                        if (param._isOutputDebug) {
                            if (null != spGroupKekkaJisu && spGroupKekkaJisu.doubleValue() > 0.0) {
                                log.info(" set " + specialGroupCd + ":" + dateRange._key + ",  kekkaJisu = " + spGroupKekkaJisu);
                            }
                        }
                        if (Attendance.GROUP_LHR.equals(specialGroupCd)) {
                            attendance._lhr = spGroupKekkaJisu;
                        } else if (Attendance.GROUP_EVENT.equals(specialGroupCd)) {
                            attendance._event= spGroupKekkaJisu;
                        }
                    }

                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }


        /**
         * 欠課時分を欠課時数に換算した値を得る
         * @param kekka 欠課時分
         * @return 欠課時分を欠課時数に換算した値
         */
        private static BigDecimal getSpecialAttendExe(final int kekka, final Param param) {
            final int jituJifun = (param._knjSchoolMst._jituJifunSpecial == null) ? 50 : Integer.parseInt(param._knjSchoolMst._jituJifunSpecial);
            final BigDecimal bigD = new BigDecimal(kekka).divide(new BigDecimal(jituJifun), 10, BigDecimal.ROUND_DOWN);
            final BigDecimal rtn;
            if ("1".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：二捨三入 (五捨六入)
                int hasu = 0;
                final String retSt = bigD.toString();
                final int retIndex = retSt.indexOf(".");
                if (retIndex > 0) {
                    hasu = Integer.parseInt(retSt.substring(retIndex + 1, retIndex + 2));
                }
                rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
            } else if ("2".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：四捨五入
                rtn = bigD.setScale(0, BigDecimal.ROUND_UP);
            } else if ("3".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り上げ
                rtn = bigD.setScale(0, BigDecimal.ROUND_CEILING);
            } else { // if ("4".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り下げ
                rtn = bigD.setScale(0, BigDecimal.ROUND_FLOOR);
            }
            return rtn;
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

        private boolean isNarakenKekkaOver(final Param param, final Student student, final Integer _credits, final Double absent) {
            if (null == absent || absent.doubleValue() == 0.0) {
                return false;
            }
            int credits = 0;
            int week = 0;
            int jisu = 0;
            if (param._knjSchoolMst.isHoutei()) {
                if (null == _credits) {
                    if (param._isOutputDebug) {
                        log.warn("単位無し：" + student._schregno + ", " + _subclasscd + ", kekka = " + absent);
                    }
                } else {
                    final String sWeek;
                    if (SEMEALL.equals(param._semester) || param._isLastSemester) {
                        sWeek = KnjDbUtils.getString(param._vSchoolMst, "JITU_SYUSU");
                    } else {
                        sWeek = KnjDbUtils.getString(param._vSchoolMst, "HOUTEI_SYUSU_SEMESTER" + param._semester);
                    }
                    if (!NumberUtils.isDigits(sWeek)) {
                        log.warn("週数無し： semester = " + param._semester);
                    } else {
                        credits = _credits.intValue();
                        week = Integer.parseInt(sWeek);
                        jisu = credits * week;
                        if (param._isOutputDebug) {
                            param.logOnce(" subclass " + _subclasscd + ", 単位数 " + credits + ", 週数 " + week + ", 時数 " + jisu);
                        }
                    }
                }
            } else { // param._knjSchoolMst.isJitsu()
                jisu = null == _lesson ? 0 : _lesson.intValue();
                param.logOnce(" subclass " + _subclasscd + ", 時数 " + jisu);
            }
            boolean rtn = false;
            if (jisu != 0) {
                int jougenti = 0;
                final boolean isFutoukouTaiou = (SEMEALL.equals(param._semester) || param._isLastSemester) && null != student._schoolRefusal;
                if (isFutoukouTaiou) {
                    jougenti = jisu / 2; // 端数切捨て
                } else {
                    //jougenti = jisu / 3; // 端数切捨て
                    if (NumberUtils.isNumber(param._knjSchoolMst._syutokuBunsi) && NumberUtils.isNumber(param._knjSchoolMst._syutokuBunbo) && new BigDecimal(param._knjSchoolMst._syutokuBunbo).doubleValue() != 0.0) {
                        jougenti = new BigDecimal(jisu).multiply(new BigDecimal(param._knjSchoolMst._syutokuBunsi)).divide(new BigDecimal(param._knjSchoolMst._syutokuBunbo), 0, BigDecimal.ROUND_DOWN).intValue(); // 端数切捨て
                    } else {
                        param.logOnce(" 上限値算出エラー 分子 = " + param._knjSchoolMst._syutokuBunsi + ", 分母 = " + param._knjSchoolMst._syutokuBunbo);
                    }
                }
                if (jougenti > 0) {
                    rtn = jougenti < absent.doubleValue();
                    if (rtn) {
                        if (param._isOutputDebug) {
                            log.info(" 欠課超過 student " + student._attendno +  ":" + student._schregno + ", subclass " + _subclasscd + ", 時数 " + jisu + ", 上限 " + jougenti + ", 欠席 " + absent + (isFutoukouTaiou ? " (不登校傾向対応)" : ""));
                        }
                    }
                }
            }
            return rtn;
        }
    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
        }
    }

    private static class SubclassMst implements Comparable<SubclassMst> {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final String _electdiv;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final boolean _isSaki;
        final boolean _isMoto;
        final String _calculateCreditFlg;
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final String electdiv,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final boolean isSaki, final boolean isMoto, final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _electdiv = electdiv;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _isSaki = isSaki;
            _isMoto = isMoto;
            _calculateCreditFlg = calculateCreditFlg;
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
            return "SubclassMst(subclasscd = " + _subclasscd + ", subclassname = " + _subclassname + (_isSaki ? " [合併先]" : "") + (_isMoto ? " [合併元]" : "") + (null != _calculateCreditFlg ? (", calculateCreditFlg = " + _calculateCreditFlg) : "") + ")";
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

    private static class JviewGrade {
        final String _subclassCd;
        final String _viewCd;
        final String _viewName;
        public JviewGrade(final String subclassCd, final String viewCd, final String viewName) {
            _subclassCd = subclassCd;
            _viewCd = viewCd;
            _viewName = viewName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75955 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _disp;
        final String[] _categorySelected;
        final String _date;
        final String _gradeHrClass;
        final String _grade;
        final String _schoolKind;
        final String _loginDate;
        final String _loginSemester;
        final String _loginYear;
        final String _prgid;
        final String _printLogRemoteAddr;
        final String _printLogRemoteIdent;
        final String _printLogStaffcd;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE;
        final String _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE;
        final String _HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE;
        final String _semester;
        /** 帳票パターン 1:A 2:B 3:C 4:D 5:E */
        final String _seq001_r1;
        /** 成績の表示項目(A,Bのみ) REMARK1～8 : 1～8学期 REMARK9：学年末 */
        final Map<String, String> _seq002Map;
        /** 合計点(A,B,Cのみ) 1:表記なし */
        final String _seq003_r1;
        /** 平均点 REMARK1(1:表記なし 2:表記あり) */
        final String _seq004_r1;
        /** 平均点種類 REMARK2(1:クラス 2:コース 3:学年) 【REMARK1(2:表記あり)の時】 */
        final String _seq004_r2;
        /** 修得単位合計(A,Bのみ) 1:表記なし */
        final String _seq005_r1;
        /** 出欠の表示項目(A,B,Dのみ) REMARK1～8 : 1～8学期 REMARK9：学年末 */
        final Map<String, String> _seq006Map;
        /** 修得単位合計(A,B,Dのみ) 1:表記なし */
        final String _seq007_r1;
        /** 修得単位合計(A,B,Dのみ) 1:表記なし */
        final String _seq008_r1;
        /** 修得単位合計(A,B,Dのみ) 1:表記なし */
        final String _seq009_r1;
        /** 総合学習の時間(A,B,C,Eのみ) 1:表記なし */
        final String _seq010_r1;
        /** 各教科の観点(D,Eのみ) 1:出力なし */
        final String _seq011_r1;
        /** 担任項目名ラジオボタン 1:担任 2:チューター */
        final String _seq012_r1;
        /** 教科名ラジオボタン 1:総合的な学習(探求)の時間 2:課題研究 */
        final String _seq013_r1;

        final String _knjd185eNotPrintClass90;
        final String _knjd185ePatternDGakkkiTitle;
        final Map _d082;

        final String _nendo;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolHrJobName;

        private String _classColumnName; //教科項目名称(総合学習用)

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;
        private final KNJSchoolMst _knjSchoolMst;
        private boolean _isHibinyuryoku;
        private Map<String, String> _vSchoolMst;

        private Map<String, Semester> _semesterMap;
        private boolean _isLastSemester;
        private Map<String, SubclassMst> _subclassMstMap;
//        private final Map _jviewGradeMap;
        private List _d026List = new ArrayList();
        Map<String, DateRange> _attendRanges;
        final List<String> _d017OrD065List;
        /** Map<テスト種別, D070.NAMESPARE1(1:序列確定KNJD210Vで合併元科目を合計点に含めない, 2:序列確定KNJD210Vで合併先科目を合計点に含めない)>  */
        final Map<String, String> _d070_testcdNamespare1Map;
        /** Map<テスト種別, 学年, 序列確定実行履歴>  */
        final Map<String, Map<String, List<Map<String, String>>>> _testcdRecordRankExecSdivDat;
        /** Map<学年-コース-科目コード, 単位>  */
        final Map<String, String> _creditMstCreditMap;

        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _backSlashImagePath;
        final String _whiteSpaceImagePath;
        final Set<String> _logOnce = new HashSet<String>();

        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugQuery;
        final boolean _isOutputDebugSvf;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _disp = request.getParameter("DISP");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            if ("1".equals(_disp)) {
                _grade = _gradeHrClass;
            } else {
                _grade = _gradeHrClass.substring(0, 2);
            }
            _loginDate = request.getParameter("LOGIN_DATE");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _prgid = request.getParameter("PRGID");
            _printLogRemoteAddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");
            _printLogRemoteIdent = request.getParameter("PRINT_LOG_REMOTE_IDENT");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _HREPORTREMARK_DAT_COMMUNICATION_SIZE = request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE");
            _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE = request.getParameter("HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE");
            _HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE = request.getParameter("HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE");
            _semester = request.getParameter("SEMESTER");
            _seq001_r1 = request.getParameter("SEQ001_R1");
            log.info(" pattern = " + _seq001_r1);
            _seq002Map = new HashMap();
            for (int i = 1; i <= 10; i++) {
                _seq002Map.put(String.valueOf(i), request.getParameter("SEQ002_R" + i));
            }
            _seq003_r1 = request.getParameter("SEQ003_R1");
            _seq004_r1 = request.getParameter("SEQ004_R1");
            _seq004_r2 = request.getParameter("SEQ004_R2");
            _seq005_r1 = request.getParameter("SEQ005_R1");
            _seq006Map = new HashMap();
            for (int i = 1; i <= 10; i++) {
                _seq006Map.put(String.valueOf(i), request.getParameter("SEQ006_R" + i));
            }
            _seq007_r1 = request.getParameter("SEQ007_R1");
            _seq008_r1 = request.getParameter("SEQ008_R1");
            _seq009_r1 = request.getParameter("SEQ009_R1");
            _seq010_r1 = request.getParameter("SEQ010_R1");
            _seq011_r1 = request.getParameter("SEQ011_R1");
            _seq012_r1 = request.getParameter("SEQ012_R1");
            _seq013_r1 = request.getParameter("SEQ013_R1");
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_loginYear)) + "年度";

            _knjd185eNotPrintClass90 = request.getParameter("knjd185eNotPrintClass90");
            _knjd185ePatternDGakkkiTitle = request.getParameter("knjd185ePatternDGakkkiTitle");
            _d017OrD065List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _loginYear + "' AND NAMECD1 IN ('D017', 'D065') ORDER BY NAME1 "), "NAME1");
            _schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _loginYear + "' AND GRADE = '" + _grade + "' "));
            final Map<String, String> dx70Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAME1, NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _loginYear + "' AND NAMECD1 = 'D" + _schoolKind + "70' ORDER BY NAME1 "), "NAME1", "NAMESPARE1");
            final Map<String, String> d070Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAME1, NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _loginYear + "' AND NAMECD1 = 'D" + "0"         + "70' ORDER BY NAME1 "), "NAME1", "NAMESPARE1");
            if (!dx70Map.isEmpty()) {
                _d070_testcdNamespare1Map = dx70Map;
            } else {
                _d070_testcdNamespare1Map = d070Map;
            }
            _testcdRecordRankExecSdivDat = new HashMap<String, Map<String, List<Map<String, String>>>>();
            for (final Map<String, String> row : KnjDbUtils.query(db2, " SELECT * FROM RECORD_RANK_EXEC_SDIV_DAT WHERE YEAR = '"+ _loginYear + "' AND SUBCLASSCD = '000000' ORDER BY CALC_DATE, CALC_TIME ")) {
                final String testcd = getString(row, "SEMESTER") + getString(row, "TESTKINDCD") + getString(row, "TESTITEMCD") + getString(row, "SCORE_DIV");
                getMappedList(getMappedMap(_testcdRecordRankExecSdivDat, testcd), getString(row, "GRADE")).add(row);
            }
            _creditMstCreditMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT GRADE || '-' || COURSECD || MAJORCD || COURSECODE || '-' || CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS KEY, CREDITS FROM CREDIT_MST WHERE YEAR = '" + _loginYear + "' AND GRADE = '" + _grade + "' "), "KEY", "CREDITS");

            final String[] outputDebug = StringUtils.split(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD185E' AND NAME = '" + "outputDebug" + "' ")));
            _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugQuery = ArrayUtils.contains(outputDebug, "query");
            _isOutputDebugSvf = ArrayUtils.contains(outputDebug, "svf");
            if (_isOutputDebug) {
                log.info(" dx70 = " + dx70Map + ", d070 = " + d070Map);
            }

            _d082 = getD082(db2);
            loadNameMstD026(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);

            setCertifSchoolDat(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            final Map knjSchoolMstParam = new HashMap();
            if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
                knjSchoolMstParam.put("SCHOOL_KIND", KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _loginYear + "' AND GRADE = '" + _grade + "' ")));
            }
            _knjSchoolMst = new KNJSchoolMst(db2, _loginYear, knjSchoolMstParam);
            _semesterMap = loadSemester(db2);
            final TreeMap lastSemesterCheckMap = new TreeMap(_semesterMap);
            lastSemesterCheckMap.remove(SEMEALL);
            _isLastSemester = !lastSemesterCheckMap.isEmpty() && _semester.equals(lastSemesterCheckMap.lastKey());
            _attendRanges = new HashMap();
            for (final String semester : _semesterMap.keySet()) {
                final Semester oSemester = _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }
            setSubclassMst(db2);
//            _jviewGradeMap = getJviewGradeMap(db2);

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            _classColumnName = "2".equals(_seq013_r1) ? "課題研究" : "総合的な学習(探究)の時間";

            setNarakenKekkaOverparameter(db2);

            if (_isOutputDebug) {
                log.info(" テーブル HREPORT_CONDITION_DAT ");
                final Set<String> notPrintScoreSemesters = new TreeSet<String>();
                for (final String seme : _seq002Map.keySet()) {
                    if (_semesterMap.containsKey(seme)) {
                        if (!"1".equals(_seq002Map.get(seme))) {
                            notPrintScoreSemesters.add(seme);
                        }
                    }
                }
                if (!notPrintScoreSemesters.isEmpty()) {
                    log.info("★ 成績を表示しない学期(SEQ 002): " + notPrintScoreSemesters);
                }

                if ("1".equals(_seq003_r1)) {
                    log.info("★ 合計点を表示しない(SEQ 003)");
                }
                if ("1".equals(_seq004_r1)) {
                    log.info("★ 平均点を表示しない(SEQ 0042)");
                }
                if ("1".equals(_seq005_r1)) {
                    log.info("★ 修得単位数を表示しない(SEQ 005)");
                }

                final Set<String> notPrintAttendSemesters = new TreeSet<String>();
                for (final String seme : _seq006Map.keySet()) {
                    if (_semesterMap.containsKey(seme)) {
                        if (!"1".equals(_seq006Map.get(seme))) {
                            notPrintAttendSemesters.add(seme);
                        }
                    }
                }
                if (!notPrintAttendSemesters.isEmpty()) {
                    log.info("★ 出欠を表示しない学期(SEQ 006): " + notPrintAttendSemesters);
                }

                if (null != _knjSchoolMst) {
                    if (NumberUtils.isNumber(_knjSchoolMst._syutokuBunsi) && NumberUtils.isNumber(_knjSchoolMst._syutokuBunbo) && new BigDecimal(_knjSchoolMst._syutokuBunbo).doubleValue() > 0.0) {
                        log.info(" 学校マスタ 修得分子 = " + _knjSchoolMst._syutokuBunsi + " / 修得分母 = " + _knjSchoolMst._syutokuBunbo + " = " + new BigDecimal(_knjSchoolMst._syutokuBunsi).divide(new BigDecimal(_knjSchoolMst._syutokuBunbo), 4, BigDecimal.ROUND_HALF_DOWN));
                    }
                }
            }
        }

        public String getCredits(final String grade, final String course, final String subclasscd) {
            final String key = grade + "-" + course + "-" + subclasscd;
            return _creditMstCreditMap.get(key);
        }

        /**
         * 指定Timestampに直近のRECORD_RANK_EXEC_SDIV_DATを得る
         * KNJD210VはRECORD_RANK_SDIV_DAT作成より前にRECORD_RANK_EXEC_SDIV_DATを作成する
         * 指定Timestampより1個前を返す
         */
        private Map<String, String> getRecordRankExecSdivDat(final String testcd, final String grade, final Timestamp ts) {
            if (null == ts) {
                return null;
            }
            Map<String, String> before1 = null;
            for (final Map<String, String> row : getMappedList(getMappedMap(_testcdRecordRankExecSdivDat, testcd), grade)) {
                if (!grade.equals(KnjDbUtils.getString(row, "GRADE"))) {
                    continue;
                }
                final Timestamp updated = Timestamp.valueOf(getString(row, "UPDATED"));
                if (null != updated && (updated.after(ts) || updated.equals(ts))) {
                    break;
                }
                before1 = row;
            }
            return before1;
        }

        private void logOnce(final String s) {
            if (!_logOnce.contains(s)) {
                log.info(s);
                _logOnce.add(s);
            }
        }

        private Map getD082(final DB2UDB db2) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     NAME_MST D082 ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = 'D082' ");

            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "NAME1", "NAME1");
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            final Map map = new HashMap();
            final String sql = "select"
                    + "   SEMESTER,"
                    + "   SEMESTERNAME,"
                    + "   SDATE,"
                    + "   EDATE"
                    + " from"
                    + "   V_SEMESTER_GRADE_MST"
                    + " where"
                    + "   YEAR='" + _loginYear + "'"
                    + "   AND GRADE='" + _grade + "'"
                    + " order by SEMESTER"
                ;

            for (final Map row : KnjDbUtils.query(db2, sql)) {
                map.put(getString(row, "SEMESTER"), new Semester(getString(row, "SEMESTER"), getString(row, "SEMESTERNAME"), getString(row, "SDATE"), getString(row, "EDATE")));
            }
            return map;
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new LinkedMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _loginYear + "' GROUP BY COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
                sql += " UNION ";
                sql += " SELECT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _loginYear + "' GROUP BY ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += " T1.CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " T1.ELECTDIV, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO, ";
                sql += " L1.CALCULATE_CREDIT_FLG AS CALCULATE_CREDIT_FLG_SAKI, ";
                sql += " L2.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3 ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                if (_isOutputDebugQuery) {
                    log.info(" subclassMst sql = " + sql);
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final String calculateCreditFlg = isSaki ? rs.getString("CALCULATE_CREDIT_FLG_SAKI") : rs.getString("CALCULATE_CREDIT_FLG");
                    final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), rs.getString("ELECTDIV"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), isSaki, isMoto, calculateCreditFlg);
                    _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

//        private Map<String, List<JviewGrade>> getJviewGradeMap(final DB2UDB db2) {
//            final Map<String, List<JviewGrade>> retMap = new TreeMap();
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT ");
//            stb.append("     J2.CLASSCD || '-' || J2.SCHOOL_KIND || '-' || J2.CURRICULUM_CD || '-' || J2.SUBCLASSCD AS SUBCLASSCD, ");
//            stb.append("     J2.VIEWCD, ");
//            stb.append("     J2.VIEWNAME ");
//            stb.append(" FROM ");
//            stb.append("     JVIEWNAME_GRADE_YDAT J1, ");
//            stb.append("     JVIEWNAME_GRADE_MST J2 ");
//            stb.append(" WHERE ");
//            stb.append("     J1.YEAR = '" + _loginYear + "' ");
//            stb.append("     AND J1.GRADE = '" + _grade + "' ");
//            stb.append("     AND J2.GRADE = J1.GRADE ");
//            stb.append("     AND J2.CLASSCD = J1.CLASSCD ");
//            stb.append("     AND J2.SCHOOL_KIND = J1.SCHOOL_KIND ");
//            stb.append("     AND J2.CURRICULUM_CD = J1.CURRICULUM_CD ");
//            stb.append("     AND J2.SUBCLASSCD = J1.SUBCLASSCD ");
//            stb.append("     AND J2.VIEWCD = J1.VIEWCD ");
//            stb.append(" ORDER BY ");
//            stb.append("     SUBCLASSCD, ");
//            stb.append("     J2.VIEWCD ");
//
//            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
//                final String subclassCd = getString(row, "SUBCLASSCD");
//                final String viewCd = getString(row, "VIEWCD");
//                final String viewName = getString(row, "VIEWNAME");
//
//                getMappedList(retMap, subclassCd).add(new JviewGrade(subclassCd, viewCd, viewName));
//            }
//            return retMap;
//        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _loginYear + "' AND CERTIF_KINDCD = '104' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
            _certifSchoolHrJobName = "1".equals(_seq012_r1) ? "担任" : "チューター";
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
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
            log.info("合併先科目を印刷するか：" + _isPrintSakiKamoku);
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

        private void setNarakenKekkaOverparameter(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT COUNT(*) AS COUNT ");
            sql.append(" FROM SCH_CHR_DAT T1 ");
            sql.append(" INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.CHAIRCD = T1.CHAIRCD ");
            sql.append(" WHERE T1.YEAR = '" + _loginYear +"' ");

            final String countStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
            int count = 0;
            if (NumberUtils.isDigits(countStr)) {
                count = Integer.parseInt(countStr);
            }
            _isHibinyuryoku = count >= 10; // 時間割がある学校を日々入力とする

            final StringBuffer vSchoolMstSql = new StringBuffer();
            vSchoolMstSql.append(" SELECT * FROM V_SCHOOL_MST WHERE YEAR = '" + _loginYear + "' ");
            if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
                vSchoolMstSql.append(" AND SCHOOL_KIND = '" + "H" + "'  ");
            }
            _vSchoolMst = KnjDbUtils.firstRow(KnjDbUtils.query(db2, vSchoolMstSql.toString()));
        }
    }
}

// eof
