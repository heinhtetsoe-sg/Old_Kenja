/*
 * $Id: 6153ef622374f7ded13d4b879658ac637fe58d10 $
 *
 * 作成日: 2014/10/28
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 *  学校教育システム 賢者 [特別支援学校]  指導計画（通知票）印刷
 */
public class KNJD421 {

    private static final Log log = LogFactory.getLog(KNJD421.class);

    private static final String PATTERN1 = "1";
    private static final String PATTERN2 = "2";
    private static final String PATTERN3 = "3";
    private static final String PATTERN4 = "4";
    private static final String PATTERN5 = "5";
    private static final String PATTERN6 = "6";
    private static final String PATTERN7 = "7";
    private static final String PATTERN7_2 = "7_2";
    private static final String PATTERN8 = "8";
    private static final String PATTERN_A = "A";
    private static final String PATTERN_B = "B";

    private static final String PRGID_KNJD418 = "KNJD418"; // 指導計画項目名設定 印刷
    private static final String PRGID_KNJD419 = "KNJD419"; // グループ別指導のねらい等登録 印刷
    private static final String PRGID_KNJD420 = "KNJD420"; // 個人別指導計画入力

    private static final String REMARK = "REMARK";
    private static final String REMARK_VALUE = "REMARK_VALUE";
    private static final String ASSESS_MARK = "ASSESS_MARK";
    private static final String GROUP_REMARK = "GROUP_REMARK";
    private static final String YEAR_TARGET = "YEAR_TARGET";
    private static final String NERAI_OR_HYOKA_KOUMOKU = "NERAI_OR_HYOKA_KOUMOKU";
    private static final String NAIYO = "NAIYO"; // 単元名称
    private static final String PROCEDURE = "PROCEDURE"; // 手立て
    private static final String VALUE_TEXT = "VALUE_TEXT"; // 文言評価
    private static final String VALUE_TEXT2 = "VALUE_TEXT2"; // 文言評価2（備考）
    private static final String VALUE = "VALUE"; // 評定
    private static final String VALUE_TEXT_PATTERN_A = "VALUE_TEXT_PATTERN_A"; // 文言評価(パターンASEQごとor単元ごと)
    private static final String[] SOGOTEKINA_GAKUSHUNO_JIKAN_SPLIT = {"総合的な", "学習の", "時間"};

    private final String kbn = "kbn";
    private final String classRemark1 = "divClassRemark1";
    private final String kyouka = "kyouka";
    private final String divGrp = "divGrp";
    private final String divSubclasscd = "divSubclasscd";
    private final String divNaiyogrp = "divNaiyogrp";
    private final String divUnitgrp = "divUnitgrp";

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        final Form form = new Form();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            form._param = createParam(db2, request);

            form.printMain(svf, db2);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != form._param) {
                form._param.psClose();
            }

            if (!form._hasData) {
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

    private class Form {

        private static final String TEXT_1_PATTERN_B = "TEXT_1";
        private static final String TEXT_2_PATTERN_B = "TEXT_2";
        private static final String TEXT_3_PATTERN_B = "TEXT_3";

        private Param _param;
        private boolean _hasData = false;

        private Map _formFieldMap = new HashMap();

        private String _currentForm;

        private void setForm(final Vrw32alp svf, final String formname, final int flg) throws Exception {
        	int rtn = svf.VrSetForm(formname, flg);
        	if (_param._isOutputDebugVrsout) {
        		log.info(" form " + formname + " " + (rtn < 0 ? String.valueOf(rtn) : ""));
        	}
        	if (rtn < 0) {
        		throw new FileNotFoundException(formname);
        	}
        	_currentForm = formname;
        	if (!_formFieldMap.containsKey(_currentForm)) {
        		_formFieldMap.put(_currentForm, SvfField.getSvfFormFieldInfoMapGroupByName(svf));
        	}
        }

        private void vrsOut(final Vrw32alp svf, final String fieldname, final String data) {
        	if (!hasField(fieldname)) {
        		log.warn(" no such field : " + fieldname);
        	} else {
        		svf.VrsOut(fieldname, data);
        		if (_param._isOutputDebugVrsout) {
        			log.info("VrsOut(\"" + fieldname + "\", \"" + data + "\")");
        		}
        	}
        }

        private void vrsOutn(final Vrw32alp svf, final String fieldname, final int n, final String data) {
        	if (!hasField(fieldname)) {
        		log.warn(" no such field : " + fieldname);
        	} else {
        		svf.VrsOutn(fieldname, n, data);
        		if (_param._isOutputDebugVrsout) {
        			log.info("VrsOutn(\"" + fieldname + "\", " + n + "\"" + data + "\")");
        		}
        	}
        }

        private void vrEndRecord(final Vrw32alp svf) {
        	svf.VrEndRecord();
        	if (_param._isOutputDebugVrsout) {
        		log.info("VrEndRecord");
        	}
        }

        private String trim(final String s) {
        	if (null == s) {
        		return s;
        	}
        	int i = 0;
        	for (i = 0; i < s.length(); i++) {
        		if (Character.isWhitespace(s.charAt(i)) || s.charAt(i) == '　') {
        			continue;
        		}
        		break;
        	}
        	return s.substring(i);

        }

        private boolean hasField(final String fieldname) {
        	try {
            	SvfField svfField = (SvfField) getMappedMap(_formFieldMap, _currentForm).get(fieldname);
            	return null != svfField;
        	} catch (Exception e) {
        		log.error("exception!", e);
        	}
        	return false;
        }

        private Integer fieldKeta(final String fieldname) {
        	int rtn = 9999;
        	try {
            	SvfField svfField = (SvfField) getMappedMap(_formFieldMap, _currentForm).get(fieldname);
            	rtn = svfField._fieldLength;
        	} catch (Exception e) {
        		log.error("exception!", e);
        	}
        	return new Integer(rtn);
        }

        private void printMain(final Vrw32alp svf, final DB2UDB db2) throws Exception {

            if (PRGID_KNJD418.equals(_param._prgId) || PRGID_KNJD419.equals(_param._prgId)) {
                final Student dummyStudent = new Student(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
                dummyStudent._gradeKind = new GradeKind(_param._gakubuSchoolKind, _param._ghrCd, _param._grade, _param._hrClass, _param._condition, _param._groupcd);
                GradeKindSubclass.setGradeKindSubclass(db2, _param, dummyStudent);
                if (PRGID_KNJD418.equals(_param._prgId)) {
                    dummyStudent._gradeKind._guidancePattern = _param._guidancePattern;
                    dummyStudent._gradeKind._tyohyoPattern = dummyStudent._gradeKind._guidancePattern;
                }
                if (PATTERN7.equals(dummyStudent._gradeKind._guidancePattern) && "1".equals(_param._outputDiv)) {
                    dummyStudent._gradeKind._tyohyoPattern = PATTERN7_2;
                }
                // dummyStudent._gradeKind._tyohyoPattern =
                printSeiseki(db2, svf, dummyStudent, true);
            } else {

                final List studentList = getStudentList(db2, _param);
                log.info(" studentList size = " + studentList.size());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    log.info(" schregno " + student._schregno);
                    final String semester;
                    if ("1".equals(_param._useGradeKindCompGroupSemester)) {
                    	semester = StringUtils.defaultString(_param._d078, _param._semester);
                    } else {
                    	semester = "9";
                    }
                    student._gradeKind = GradeKind.load(db2, _param._ctrlYear, semester, student._schregno);
                    GradeKindSubclass.setGradeKindSubclass(db2, _param, student);
                    if ("1".equals(_param._printSide2) || PRGID_KNJD420.equals(_param._prgId) || "1".equals(_param._printSide3)) {
                        Student.setShoken(db2, _param, student);
                    }

                    if ("1".equals(_param._printSide1)) {
                        // 表紙
                        printHyoshi(db2, svf, student);
                        _hasData = true;
                    }
                    if ("1".equals(_param._printSide2) || PRGID_KNJD420.equals(_param._prgId)) {
                        // 学習のようす等
                        if (null == student._gradeKind) {
                        	log.warn(" student gradeKind null");
                        } else {
                            printSeiseki(db2, svf, student, !"1".equals(_param._outputDiv) || PATTERN_B.equals(student._gradeKind._tyohyoPattern));
                            _hasData = true;
                        }
                    }
                    if ("1".equals(_param._printSide3)) {
                        // 出欠
                        student._attendSemesDatMap = AttendSemesDat.getAttendSemesDatMap(db2, _param, student._schregno);
                        printShukketsu(svf, student);
                        _hasData = true;
                    }
                    if (_param._isLastSemester && "1".equals(_param._printSide4)) {
                        // 修了証
                        printShuryo(db2, svf, student);
                        _hasData = true;
                    }
                }
            }
        }

        private boolean isYoko(final GradeKind gradeKind) {
            if (null == gradeKind || null == gradeKind._tyohyoPattern) {
                return false;
            }
            return (PATTERN1.equals(gradeKind._tyohyoPattern) || PATTERN2.equals(gradeKind._tyohyoPattern) || PATTERN4.equals(gradeKind._tyohyoPattern) || PATTERN_A.equals(gradeKind._tyohyoPattern)) ? true : false;
        }

        private final String semestername(final String schoolKind, final String d078, final String semester2) {
        	return StringUtils.defaultString((String) _param.getD78name1(schoolKind, d078), (String) _param._semesterNameMap.get(semester2));
        }

        /**
         * 表紙を印刷する
         * @param svf
         * @param student
         */
        private void printHyoshi(final DB2UDB db2, final Vrw32alp svf, final Student student) throws Exception {
        	if ("1".equals(_param._printSide1Attend)) {

        		student._attendSemesDatMonthMap = AttendSemesDat.getAttendSemesDatMonthMap(db2, _param, student._schregno);

        		final String form = _param._isFukuiken ? "KNJD421_1_4.frm": "KNJD421_1_3.frm";
                setForm(svf, form, 4);

                vrsOut(svf, "NENDO", _param._nendo + "　" + semestername(student._schoolKind, _param._d078, _param._semester));
                vrsOut(svf, "SCHOOL_NAME", _param._certifSchoolSchoolName);
                vrsOut(svf, "HR_NAME", StringUtils.defaultString(student._coursename, "　　") + StringUtils.defaultString(student._majorname) + "　第" + (NumberUtils.isDigits(student._gradecd) ? String.valueOf(Integer.parseInt(student._gradecd)) : StringUtils.defaultString(student._gradecd, " ")) + "学年");
                vrsOut(svf, "STUDENT_NAME", student._name);

                final String[] months = _param.getD78AttendSemesMonthArray(student._schoolKind, StringUtils.defaultString(_param._d078, _param._semester));

            	AttendSemesDat total = null;
                for (int i = 0; i < months.length; i++) {
                	final String month = months[i];
                	if (NumberUtils.isDigits(month)) {
                		vrsOut(svf, "MONTH", String.valueOf(Integer.parseInt(month)) + "月");
                	}

            		final AttendSemesDat semesMonth = (AttendSemesDat) student._attendSemesDatMonthMap.get(month);
            		if (null != semesMonth) {
            			vrsOut(svf, "LESSON1", shukketsuValue(semesMonth._lesson));
            			vrsOut(svf, "SUS_MOUR1", shukketsuValue(semesMonth._suspendMourning));
            			vrsOut(svf, "MUST1", shukketsuValue(semesMonth._mlesson));
            			vrsOut(svf, "SICK1", shukketsuValue(semesMonth._nonoticeOnly));
            			vrsOut(svf, "SICK2", shukketsuValue(semesMonth._noticeOnly));
            			vrsOut(svf, "ATTEND1", shukketsuValue(semesMonth._present));
            			final int remarkKeta = getMS932ByteCount(semesMonth._remark);
            			vrsOut(svf, remarkKeta <= 8 ? "REMARK1" : remarkKeta <= 12 ? "REMARK2" : "REMARK3_1", semesMonth._remark);
            			if (null == total) {
            				total = new AttendSemesDat("");
            			}
            			total.add(semesMonth);
            		}
                    svf.VrEndRecord();
                }
        		vrsOut(svf, "MONTH", "計");
        		if (null != total) {
        			vrsOut(svf, "LESSON1", shukketsuValue(total._lesson));
        			vrsOut(svf, "SUS_MOUR1", shukketsuValue(total._suspendMourning));
        			vrsOut(svf, "MUST1", shukketsuValue(total._mlesson));
        			vrsOut(svf, "SICK1", shukketsuValue(total._nonoticeOnly));
        			vrsOut(svf, "SICK2", shukketsuValue(total._noticeOnly));
        			vrsOut(svf, "ATTEND1", shukketsuValue(total._present));
        		}
        		svf.VrEndRecord();

        	} else {
                final String form = isYoko(student._gradeKind) ? "KNJD421_1_2.frm" : "KNJD421_1_1.frm";
                setForm(svf, form, 1);
                vrsOut(svf, "NENDO", _param._nendo);
                vrsOut(svf, "SCHOOL_NAME", _param._certifSchoolSchoolName);
                //vrsOut(svf, "LOGO", _param.getImagePath());
                vrsOut(svf, "HR_NAME", StringUtils.defaultString(student._schoolKindName, "　　") + "部　" + student._hrName);
                final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno;
                vrsOut(svf, "ATTENDNO", attendno);
                vrsOut(svf, "STUDENT_NAME", student._name);
                //svf.VrAttribute("STUDENT_NAME", "UnderLine=(0,3,1)");
                vrsOut(svf, "PRESIDENT_NAME", _param._certifSchoolPrincipalName);
                //svf.VrAttribute("PRESIDENT_NAME", "UnderLine=(0,3,1)");
                vrsOut(svf, "TEACHER_NAME", student._staffname);
                //svf.VrAttribute("TEACHER_NAME", "UnderLine=(0,3,1)");
                svf.VrEndPage();
        	}
        }

        private String shukketsuValue(int n) {
            return String.valueOf(n);
        }

        /**
         * 『出欠の記録』を印字する
         * @param svf
         * @param student
         */
        private void printShukketsu(final Vrw32alp svf, final Student student) throws Exception {
            final String form = isYoko(student._gradeKind) ? "KNJD421_3_2.frm" : "KNJD421_3_1.frm";
            setForm(svf, form, 4);

            vrsOut(svf, "STUDENT_NAME", student._name);

            // 総合的な学習の時間 学習内容
            svfVrsOutKurikaeshi(svf, "SP_CONENT", getTokenList(student._totalstudytime, ShokenSize.getSize(_param._HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE, 35, 2)));

            // 総合的な学習の時間 評価
            svfVrsOutKurikaeshi(svf, "SP_EVA", getTokenList(student._hreportremarkDetailDat0501Remark1, ShokenSize.getSize(_param._reportSpecialSize05_01, 35, 3)));

            // 特別活動
            svfVrsOutKurikaeshi(svf, "SPECIALACT", getTokenList(student._specialactremark, ShokenSize.getSize(_param._HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE, 40, 6)));

            // 総合所見
            svfVrsOutKurikaeshi(svf, "VIEW", getTokenList(student._hreportremarkDetailDat0502Remark1, ShokenSize.getSize(_param._reportSpecialSize05_02, 25, 6)));

            vrsOut(svf, "SEMESTER3", semestername(student._schoolKind, _param._d078, _param._semester));
            final AttendSemesDat total = new AttendSemesDat("9");
            final int d78Size = _param.getD78size(student._schoolKind);
            final int semesterDiv = d78Size > 0 ? d78Size : NumberUtils.isDigits(_param._knjSchoolMst._semesterDiv) ? Integer.parseInt(_param._knjSchoolMst._semesterDiv) : 2;
            final String namesfx = semesterDiv == 2 ? "1" : "2";
            final String sfx = semesterDiv == 2 ? "1" : "";
            final String remifx = semesterDiv == 2 ? "1_" : "";
            boolean addflg = false;
            for (int si = 0; si < semesterDiv; si++) {
                final String semester = String.valueOf(si + 1);
                final String semestername = semestername(student._schoolKind, semester, semester);
                vrsOut(svf, "SEMESTER" + namesfx, semestername);

                if (Integer.parseInt(semester) <= Integer.parseInt(_param._semester)) {

                    final AttendSemesDat attendSemesDat = (AttendSemesDat) student._attendSemesDatMap.get(semester);
                    if (null != attendSemesDat) {
                        vrsOut(svf, "LESSON" + sfx, shukketsuValue(attendSemesDat._lesson));
                        vrsOut(svf, "SUS_MOUR" + sfx, shukketsuValue(attendSemesDat._suspend + attendSemesDat._mourning + attendSemesDat._virus + attendSemesDat._koudome));
                        vrsOut(svf, "MUST" + sfx, shukketsuValue(attendSemesDat._mlesson));
                        vrsOut(svf, "SICK" + sfx, shukketsuValue(attendSemesDat._sick));
                        vrsOut(svf, "ATTEND" + sfx, shukketsuValue(attendSemesDat._present));
                        vrsOut(svf, "LATE" + sfx, shukketsuValue(attendSemesDat._late));
                        vrsOut(svf, "EARLY" + sfx, shukketsuValue(attendSemesDat._early));
                        total.add(attendSemesDat);
                        addflg = true;
                    }

                    final String remark = (String) student._hreportremarkDetailDatAttendrecRemarkMap.get(semester);
                    svfVrsOutKurikaeshi(svf, "REMARK" + remifx, getTokenList(remark, ShokenSize.getSize(_param._HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE, 13, 3)));
                }
                vrEndRecord(svf) ;
            }
            vrsOut(svf, "SEMESTER" + namesfx, "合計");
            if (_param._isLastSemester && addflg) {
                vrsOut(svf, "LESSON" + sfx, shukketsuValue(total._lesson));
                vrsOut(svf, "SUS_MOUR" + sfx, shukketsuValue(total._suspend + total._mourning + total._virus + total._koudome));
                vrsOut(svf, "MUST" + sfx, shukketsuValue(total._mlesson));
                vrsOut(svf, "SICK" + sfx, shukketsuValue(total._sick));
                vrsOut(svf, "ATTEND" + sfx, shukketsuValue(total._present));
                vrsOut(svf, "LATE" + sfx, shukketsuValue(total._late));
                vrsOut(svf, "EARLY" + sfx, shukketsuValue(total._early));
            }

            final String remark = (String) student._hreportremarkDetailDatAttendrecRemarkMap.get("9");
            svfVrsOutKurikaeshi(svf, "REMARK" + remifx, getTokenList(remark, ShokenSize.getSize(_param._HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE, 13, 3)));

            vrEndRecord(svf) ;
        }

        public void svfVrsOutKurikaeshi(final Vrw32alp svf, final String field, final List data) {
            for (int i = 0; i < data.size(); i++) {
                vrsOut(svf, field + String.valueOf(i + 1), (String) data.get(i));
            }
        }

        /**
         * 修了証を印刷する
         * @param svf
         * @param student
         */
        private void printShuryo(final DB2UDB db2, final Vrw32alp svf, final Student student) throws Exception {
            final String form = isYoko(student._gradeKind) ? "KNJD421_4_2.frm" : "KNJD421_4_1.frm";
            setForm(svf, form, 1);

            vrsOut(svf, "STUDENT_NAME", student._name);
            vrsOut(svf, "BIRTHDAY", KNJ_EditDate.h_format_JP(db2, student._birthday));

//            if (getMS932ByteCount(student._name) > 26) {
//                vrsOut(svf, "NAME2", student._name);
////                if (_param._semester.equals(_param._knjSchoolMst._semesterDiv)) {
////                    svf.VrAttribute("NAME2", "UnderLine=(0,3,1),keta=46");
////                }
//            } else {
//                vrsOut(svf, "NAME", student._name);
////                if (_param._semester.equals(_param._knjSchoolMst._semesterDiv)) {
////                    svf.VrAttribute("NAME", "UnderLine=(0,3,1),keta=26");
////                }
//            }
//            if (NumberUtils.isDigits(_param._gradeCdStr)) {
//                vrsOut(svf, "GRADE", String.valueOf(Integer.parseInt(_param._gradeCdStr)));
//            }
//            if (_param._semester.equals(_param._knjSchoolMst._semesterDiv)) {
//                vrsOut(svf, "DATE", KNJ_EditDate.h_format_JP(_param._descDate));
//            }
//            final String schoolField;
//            if (getMS932ByteCount(_param._certifSchoolRemark3) > 22) {
//                schoolField = "SCHOOLNAME1_2";
//            } else {
//                schoolField = "SCHOOLNAME1";
//            }
//            vrsOut(svf, schoolField, _param._certifSchoolRemark3);
//            vrsOut(svf, "JOB", _param._certifSchoolJobName);
//            vrsOut(svf, "STAFFNAME1_3", _param._certifSchoolPrincipalName);
            final String gradecd = NumberUtils.isDigits(student._gradecd) ? String.valueOf(Integer.parseInt(student._gradecd)) : " ";
            vrsOut(svf, "FIELD1", "本校" + StringUtils.defaultString(student._schoolKindName, "　　") + "部　第" + gradecd + "学年の課程を修了したことを証する");
            vrsOut(svf, "DATE", KNJ_EditDate.h_format_JP(db2, _param._descDate));
            vrsOut(svf, "SCHOOL_NAME", _param._certifSchoolSchoolName);
            vrsOut(svf, "JOB_NAME", _param._certifSchoolJobName);
            vrsOut(svf, "PRESIDENT_NAME", _param._certifSchoolPrincipalName);

            svf.VrEndPage();
        }



        public void printSeiseki(final DB2UDB db2, final Vrw32alp svf, final Student student, boolean printTitle) throws Exception {

            final GradeKind gradeKind = student._gradeKind;

            log.info(" gradeKind = (gakubuSchoolKind=" + gradeKind._gakubuSchoolKind + ", condition=" + gradeKind._condition + ", groupcd=" + gradeKind._groupcd  + ", guidancePattern=" + gradeKind._guidancePattern + ", tyohyoPattern=" + gradeKind._tyohyoPattern + ", student=" + student + ")");

            if (null == gradeKind._tyohyoPattern) {
                return;
            }

            final String form;
        	if (PATTERN_B.equals(gradeKind._tyohyoPattern)) {
                form = "KNJD421_2_" + gradeKind._tyohyoPattern + "C" + ".frm"; // 教科区分印字なし
        	} else {
                form = "KNJD421_2_" + gradeKind._tyohyoPattern + ("1".equals(_param._outputCategoryName2) ? "C" : "") + ".frm";
        	}
            setForm(svf, form, 4);

            boolean isPatternBgakushuNoKiroku = false;
            if (PATTERN_B.equals(gradeKind._tyohyoPattern)) {
            	Student.setShokenB(db2, _param, student);
            	isPatternBgakushuNoKiroku = "1".equals(_param._outputDiv);
            }
            final String semester;
            final String semestername = semestername(student._schoolKind, _param._d078, _param._semester);

//            final String kobetsunoSidoukeikaku = "KNJD421".equals(_param._prgId) ? "" : "　個別の指導計画";
            final String kobetsunoSidoukeikaku = "個別の指導計画";
        	if (printTitle) {
            	final String title;
            	if (PATTERN_B.equals(gradeKind._tyohyoPattern)) {
            		if ("1".equals(_param._printSide1Attend)) {
                		title = _param._nendo + "　" + semestername + "　" + StringUtils.defaultString(student._coursename) + "　" + "学習の記録";
            		} else {
                		title = _param._nendo + "　" + semestername + "　" + StringUtils.defaultString(student._coursename) + "　" + kobetsunoSidoukeikaku;
            		}
            	} else if (PATTERN7_2.equals(gradeKind._tyohyoPattern)) {
            		title = _param._nendo + "　" + "通知表";
            	} else {
            		title = _param._nendo + "　" + kobetsunoSidoukeikaku;
            	}
            	vrsOut(svf, "TITLE", title); // 学期名
            	if (PATTERN_B.equals(gradeKind._tyohyoPattern)) {
            		vrEndRecord(svf);
            	}
            }

            if (PATTERN7.equals(gradeKind._tyohyoPattern)) {
				semester = _param._nendo + "　" + semestername + "　" + kobetsunoSidoukeikaku;
            } else {
                semester = semestername;
            }
            vrsOut(svf, "SEMESTER", semester); // 学期名
            final String schoolName;
            if (_param.SCHOOL_MST_HAS_SCHOOL_KIND) {
            	schoolName = (String) _param._schoolNameMap.get(student._schoolKind);
            } else {
            	schoolName = (String) _param._schoolNameMap.get("DUMMY");
            }
        	if (PATTERN_B.equals(gradeKind._tyohyoPattern)) {
        		if (!isPatternBgakushuNoKiroku) {
                	vrsOut(svf, "BLANK_DUMMY", "1");
            		vrEndRecord(svf);
                	vrsOut(svf, "HEADER_STAFFNAME_FLG", "1");
            		if (null != student) {
            			vrsOut(svf, "ACHOOL_NAME", StringUtils.defaultString(student.getPrintNameB())); // 学年、生徒名
            			if (!StringUtils.isBlank(student._staffname)) {
            				vrsOut(svf, "TEACHER", "担任名　" + student._staffname); // 担任名
            			}
            		}
            		vrEndRecord(svf);
        		}
            	vrsOut(svf, "BLANK_DUMMY", "1");
        		vrEndRecord(svf);
        	} else {
        		vrsOut(svf, "ACHOOL_NAME", schoolName); // 学校名
        		if (null != student) {
        			vrsOut(svf, "NAME", student.getPrintName()); // 生徒名
        			if (!StringUtils.isBlank(student._staffname)) {
        				vrsOut(svf, "TEACHER", "担任　" + student._staffname); // 担任名
        			}
        		}
        	}

//            int maxline = 0;
//            if (PATTERN1.equals(pat)) {
//                maxline = 28;
//            } else if (PATTERN2.equals(pat)) {
//                maxline = 28;
//            } else if (PATTERN3.equals(pat) || PATTERN4.equals(pat)) {
//                maxline = 47;
//            } else if (PATTERN5.equals(pat)) {
//                maxline = 47;
//            } else if (PATTERN6.equals(pat)) {
//                maxline = 47;
//            }

            final Map ketaMap = new HashMap();
            if (PATTERN1.equals(gradeKind._tyohyoPattern)) {
                ketaMap.put(NERAI_OR_HYOKA_KOUMOKU, fieldKeta("AIM_1")); // ねらい
                ketaMap.put(REMARK_VALUE, fieldKeta("VAL_1"));  // 評価
                ketaMap.put(VALUE_TEXT, fieldKeta("WORD_VAL_1")); // 文言評価名称
                ketaMap.put(NAIYO, fieldKeta("COMMENT_1"));
            } else if (PATTERN2.equals(gradeKind._tyohyoPattern)) {
                ketaMap.put(NERAI_OR_HYOKA_KOUMOKU, fieldKeta("AIM_1")); // ねらい
                ketaMap.put(PROCEDURE, fieldKeta("METHOD")); // 手立て
                ketaMap.put(VALUE_TEXT, fieldKeta("VAL_1"));  // 文言評価
            } else if (PATTERN3.equals(gradeKind._tyohyoPattern)) {
                ketaMap.put(NAIYO, fieldKeta("COMMENT_1")); // 内容
                ketaMap.put(NERAI_OR_HYOKA_KOUMOKU, fieldKeta("AIM_1")); // ねらい
                ketaMap.put(VALUE_TEXT, fieldKeta("VAL_1"));  // 文言評価
            } else if (PATTERN4.equals(gradeKind._tyohyoPattern)) {
                ketaMap.put(NAIYO, fieldKeta("COMMENT_1")); // 単元
                ketaMap.put(NERAI_OR_HYOKA_KOUMOKU, fieldKeta("AIM_1")); // 評価項目名称
                ketaMap.put(PROCEDURE, fieldKeta("METHOD"));  // 手立て
                ketaMap.put(VALUE_TEXT, fieldKeta("VAL_1"));  // 評価
                ketaMap.put(VALUE_TEXT2, fieldKeta("REMARK"));  // 備考
            } else if (PATTERN5.equals(gradeKind._tyohyoPattern)) {
                ketaMap.put(NAIYO, fieldKeta("SUBJECT_1")); // 単元題材
                ketaMap.put(NERAI_OR_HYOKA_KOUMOKU, fieldKeta("ITEM_1")); // 評価項目名称
                ketaMap.put(REMARK_VALUE, fieldKeta("VAL_1"));  // 評価
            } else if (PATTERN6.equals(gradeKind._tyohyoPattern)) {
                ketaMap.put(NERAI_OR_HYOKA_KOUMOKU, fieldKeta("ITEM_1")); // 評価項目名称
                ketaMap.put(REMARK_VALUE, fieldKeta("VAL_1"));  // 評価名称
                ketaMap.put(VALUE, fieldKeta("EVAL_1"));  // 評定名称
            } else if (PATTERN7.equals(gradeKind._tyohyoPattern)) {
                ketaMap.put(YEAR_TARGET, fieldKeta("HOPE")); // 年間目標
                ketaMap.put(NAIYO, fieldKeta("STUDY")); // 手立て
                ketaMap.put(NERAI_OR_HYOKA_KOUMOKU, fieldKeta("METHOD")); // 評価項目名称
                ketaMap.put(REMARK_VALUE, fieldKeta("VAL"));  // 評価名称
                ketaMap.put(VALUE_TEXT, fieldKeta("WORD_VAL"));  // 文言評価
            } else if (PATTERN8.equals(gradeKind._tyohyoPattern)) {
                ketaMap.put(NERAI_OR_HYOKA_KOUMOKU, fieldKeta("SUBJECT_1")); // 評価項目名称
                ketaMap.put(REMARK_VALUE, fieldKeta("VAL_1"));  // 評価名称
                ketaMap.put(VALUE_TEXT, fieldKeta("ITEM_1"));  // 文言評価
            } else if (PATTERN7_2.equals(gradeKind._tyohyoPattern)) {
                ketaMap.put(NAIYO, fieldKeta("STUDY")); // 手立て
                ketaMap.put(VALUE_TEXT, fieldKeta("METHOD")); // 文言評価
            } else if (PATTERN_A.equals(gradeKind._tyohyoPattern)) {
                ketaMap.put(NERAI_OR_HYOKA_KOUMOKU, fieldKeta("AIM_1")); // ねらい
                ketaMap.put(REMARK_VALUE, fieldKeta("VAL_1"));  // 重点目標
                ketaMap.put(VALUE_TEXT_PATTERN_A, fieldKeta("WORD_VAL_1")); // 文言評価名称
                ketaMap.put(NAIYO, fieldKeta("COMMENT_1"));
            } else if (PATTERN_B.equals(gradeKind._tyohyoPattern)) {
                ketaMap.put(TEXT_1_PATTERN_B, fieldKeta("TEXT_1")); // 目標
                ketaMap.put(TEXT_2_PATTERN_B, fieldKeta("TEXT_2")); // 指導内容・方法
                ketaMap.put(TEXT_3_PATTERN_B, fieldKeta("TEXT_3")); // 学習の様子および所見
            }

            PrintData before = null;
            final List printDataList = getGradeKindPrintDataList(gradeKind, ketaMap, _param);
            centeringRemarkValue(printDataList);
            printHeader(svf, student, gradeKind, true);
            String fieldDiv1 = "";
            String fieldDiv2 = "";
            for (int di = 0; di < printDataList.size(); di++) {
                final PrintData data = (PrintData) printDataList.get(di);
                if (_param._isOutputDebug) {
                	log.info(" print[" + di + "]  data = " + data);
                }
                final boolean diffClassRemark = null != before && !data.div(classRemark1).equals(before.div(classRemark1));
                final boolean diffSubclass = null != before && (null != data.div(divSubclasscd) && !data.div(divSubclasscd).equals(before.div(divSubclasscd)) || null != before.div(divSubclasscd) && !before.div(divSubclasscd).equals(data.div(divSubclasscd)) );
                final boolean diffUnitGrp = null != before && (null != data.div(divUnitgrp) && !data.div(divUnitgrp).equals(before.div(divUnitgrp)) || null != before.div(divUnitgrp) && !before.div(divUnitgrp).equals(data.div(divUnitgrp)));

                printHeader(svf, student, gradeKind, false);

                if (PATTERN1.equals(gradeKind._tyohyoPattern)) {

                    final String f;
                    if (!diffSubclass && data._diffTangen) {
                        // 上に破線
                        f = "1";
                    } else {
                        // diffClassRemark ? 上線全部 : diffSubclass ? 上線教科まで : diffUnitGrp ? ねらい表示用線有 : 線無
                        f = diffClassRemark ? "2" : diffSubclass ? "4" : diffUnitGrp ? "7" : "3";
                    }
                    //log.info(" f = " + f + "/ " + data._isLastTangen + ":" + data._classRemark1Grp + ", " + data._div + ", " + data._subclasscd + ", " + data._kakuKyouka + ", " + data._remark[NAIYO]);
                    fieldDiv1 = "DIV_" + f;
                    vrsOut(svf, "DIV_" + f, data.div(kbn)); // 区分
                    fieldDiv2 = "CLASS_" + f;
                    vrsOut(svf, "CLASS_" + f, data.div(kyouka)); // 教科
                    vrsOut(svf, "COMMENT_" + f, data.remark(NAIYO)); // 内容
                    vrsOut(svf, "AIM_" + f, data.remark(NERAI_OR_HYOKA_KOUMOKU)); // ねらい
                    vrsOut(svf, "VAL_" + f, data.remark(REMARK_VALUE)); // 評価
                    vrsOut(svf, "WORD_VAL_" + f, data.remark(VALUE_TEXT)); // 文言評価

                    vrsOut(svf, "GRP1_" + f, data.div(classRemark1)); // グループ
                    vrsOut(svf, "GRP2_" + f, data.div(divGrp)); // グループ
                    vrsOut(svf, "GRP3_" + f, data.div(divNaiyogrp)); // グループ
                    vrsOut(svf, "GRP4_" + f, data.div(divUnitgrp)); // グループ
                    vrsOut(svf, "GRP5_" + f, data.div(divUnitgrp)); // グループ
                    vrsOut(svf, "GRP6_" + f, data.div(divGrp)); // グループ

                } else if (PATTERN2.equals(gradeKind._tyohyoPattern)) {

                    final boolean line2 = false; // 実線のみ
                    final String f = line2 ? "2" : "1";

                    fieldDiv1 = "DIV_" + f;
                    vrsOut(svf, "DIV_" + f, data.div(kbn)); // 区分
                    fieldDiv2 = "CLASS_" + f;
                    vrsOut(svf, "CLASS_" + f, data.div(kyouka)); // 教科

                    vrsOut(svf, "GRP1_" + f, data.div(classRemark1)); // グループ
                    vrsOut(svf, "GRP2_" + f, data.div(divGrp)); // グループ

                    if (line2) {
                        vrsOut(svf, "TOTAL_STUDY", null); // 総学
                    } else {
                        vrsOut(svf, "AIM_1", data.remark(NERAI_OR_HYOKA_KOUMOKU)); // ねらい
                        vrsOut(svf, "METHOD", data.remark(PROCEDURE)); // 手立て
                        vrsOut(svf, "VAL_1", data.remark(VALUE_TEXT)); // 評価

                        vrsOut(svf, "GRP3_" + f, data.div(divUnitgrp)); // グループ
                        vrsOut(svf, "GRP4_" + f, data.div(divGrp)); // グループ
                        vrsOut(svf, "GRP5_" + f, data.div(divGrp)); // グループ
                    }

                } else if (PATTERN3.equals(gradeKind._tyohyoPattern)) {

                    final boolean line2 = false; // 実線のみ
                    final String f = line2 ? "2" : "1";

                    fieldDiv1 = "DIV_" + f;
                    vrsOut(svf, "DIV_" + f, data.div(kbn)); // 区分
                    fieldDiv2 = "CLASS_" + f;
                    vrsOut(svf, "CLASS_" + f, data.div(kyouka)); // 教科

                    vrsOut(svf, "GRP1_" + f, data.div(classRemark1)); // グループ
                    vrsOut(svf, "GRP2_" + f, data.div(divGrp)); // グループ

                    if (line2) {
                        vrsOut(svf, "TOTAL_STUDY", null); // 総学
                    } else {
                        vrsOut(svf, "COMMENT_1", data.remark(NAIYO)); // 内容
                        vrsOut(svf, "AIM_1", data.remark(NERAI_OR_HYOKA_KOUMOKU)); // ねらい
                        vrsOut(svf, "VAL_1", data.remark(VALUE_TEXT)); // 評価

                        vrsOut(svf, "GRP3_" + f, data.div(divNaiyogrp)); // グループ
                        vrsOut(svf, "GRP4_" + f, data.div(divUnitgrp)); // グループ
                        vrsOut(svf, "GRP5_" + f, data.div(divGrp)); // グループ
                    }
                } else if (PATTERN4.equals(gradeKind._tyohyoPattern)) {

                    fieldDiv1 = "DIV_1";
                    vrsOut(svf, "DIV_1", data.div(kbn)); // 区分
                    fieldDiv2 = "CLASS_1";
                    vrsOut(svf, "CLASS_1", data.div(kyouka)); // 教科
                    vrsOut(svf, "COMMENT_1", data.remark(NAIYO)); // 単元
                    vrsOut(svf, "AIM_1", data.remark(NERAI_OR_HYOKA_KOUMOKU)); // ねらい
                    vrsOut(svf, "METHOD", data.remark(PROCEDURE)); // 手立て
                    vrsOut(svf, "VAL_1", data.remark(VALUE_TEXT)); // 評価
                    vrsOut(svf, "REMARK", data.remark(VALUE_TEXT2)); // 備考

                    vrsOut(svf, "GRP1_1", data.div(classRemark1)); // グループ1
                    vrsOut(svf, "GRP2_1", data.div(divGrp)); // グループ2
                    vrsOut(svf, "GRP3_1", data.div(divUnitgrp)); // グループ3
                    vrsOut(svf, "GRP4_1", data.div(divUnitgrp)); // グループ4
                    vrsOut(svf, "GRP5_1", data.div(divGrp)); // グループ5
                    vrsOut(svf, "GRP6_1", data.div(divGrp)); // グループ6
                    vrsOut(svf, "GRP7_1", data.div(divGrp)); // グループ7

                } else if (PATTERN5.equals(gradeKind._tyohyoPattern)) {

                    final String f;
                    if (!diffSubclass && data._diffTangen) {
                        // 上に破線
                        f = "1";
                    } else {
                        // diffClassRemark ? 上線全部 : diffSubclass ? 上線教科まで : diffUnitGrp ? ねらい表示用線有 : 線無
                        f = diffClassRemark ? "2" : diffSubclass ? "4" : diffUnitGrp ? "7" : "3";
                    }
                    //log.info(" f = " + f + "/ " + data._isLastTangen + ":" + data._classRemark1Grp + ", " + data._div + ", " + data._subclasscd + ", " + data._kakuKyouka + ", " + data._remark[NAIYO]);

                    fieldDiv1 = "DIV_" + f;
                    vrsOut(svf, "DIV_" + f, data.div(kbn)); // 区分
                    fieldDiv2 = "CLASS_" + f;
                    vrsOut(svf, "CLASS_" + f, data.div(kyouka)); // 教科
                    vrsOut(svf, "SUBJECT_" + f, data.remark(NAIYO)); // 題材
                    vrsOut(svf, "ITEM_" + f, data.remark(NERAI_OR_HYOKA_KOUMOKU)); // 評価項目
                    vrsOut(svf, "VAL_" + f, data.remark(REMARK_VALUE)); // 評価

                    vrsOut(svf, "GRP1_" + f, data.div(classRemark1)); // グループ
                    vrsOut(svf, "GRP2_" + f, data.div(divGrp)); // グループ
                    vrsOut(svf, "GRP3_" + f, data.div(divUnitgrp)); // グループ
                    vrsOut(svf, "GRP4_" + f, data.div(divUnitgrp)); // グループ
                    vrsOut(svf, "GRP5_" + f, data.div(divUnitgrp)); // グループ

                } else if (PATTERN6.equals(gradeKind._tyohyoPattern)) {

                    final boolean line2 = true; // 単元なし
                    final String f = line2 ? "2" : "1";

                    fieldDiv1 = "DIV_" + f;
                    vrsOut(svf, "DIV_" + f, data.div(kbn)); // 区分
                    fieldDiv2 = "CLASS_" + f;
                    vrsOut(svf, "CLASS_" + f, data.div(kyouka)); // 教科
                    vrsOut(svf, "ITEM_" + f, data.remark(NERAI_OR_HYOKA_KOUMOKU)); // 評価項目
                    vrsOut(svf, "VAL_" + f, data.remark(REMARK_VALUE)); // 評価
                    vrsOut(svf, "EVAL_" + f, data.remark(VALUE)); // 評定

                    vrsOut(svf, "GRP1_" + f, data.div(classRemark1)); // グループ
                    vrsOut(svf, "GRP2_" + f, data.div(divGrp)); // グループ
                    vrsOut(svf, "GRP3_" + f, data.div(divUnitgrp)); // グループ
                    vrsOut(svf, "GRP4_" + f, data.div(divUnitgrp)); // グループ
                    vrsOut(svf, "GRP5_" + f, data.div(divGrp)); // グループ

                } else if (PATTERN7.equals(gradeKind._tyohyoPattern)) {

                    fieldDiv1 = "DIV";
                    vrsOut(svf, "DIV", data.div(kbn)); // 区分
                    fieldDiv2 = "CLASS";
                    vrsOut(svf, "CLASS", data.div(kyouka)); // 教科
                    vrsOut(svf, "HOPE", data.remark(YEAR_TARGET)); // 年間目標
                    vrsOut(svf, "STUDY", data.remark(NAIYO)); // 学習内容
                    vrsOut(svf, "METHOD", data.remark(NERAI_OR_HYOKA_KOUMOKU)); // 評価の項目
                    vrsOut(svf, "VAL", data.remark(REMARK_VALUE)); // 評価
                    vrsOut(svf, "WORD_VAL", data.remark(VALUE_TEXT)); // 文言評価

                    vrsOut(svf, "GRP1", data.div(classRemark1)); // グループ1
                    vrsOut(svf, "GRP2", data.div(divGrp)); // グループ2
                    vrsOut(svf, "GRP3", data.div(divGrp)); // グループ3
                    vrsOut(svf, "GRP4", data.div(divNaiyogrp)); // グループ4
                    vrsOut(svf, "GRP5", data.div(divUnitgrp)); // グループ5
                    vrsOut(svf, "GRP6", data.div(divUnitgrp)); // グループ6
                    vrsOut(svf, "GRP7", data.div(divGrp)); // グループ7

                } else if (PATTERN8.equals(gradeKind._tyohyoPattern)) {

                    fieldDiv1 = "DIV_1";
                    vrsOut(svf, "DIV_1", data.div(kbn)); // 区分
                    fieldDiv2 = "CLASS_1";
                    vrsOut(svf, "CLASS_1", data.div(kyouka)); // 教科
                    vrsOut(svf, "SUBJECT_1", data.remark(NERAI_OR_HYOKA_KOUMOKU)); // 学習のめあて
                    vrsOut(svf, "VAL_1", data.remark(REMARK_VALUE)); // 評価
                    vrsOut(svf, "ITEM_1", data.remark(VALUE_TEXT)); // 所見

                    vrsOut(svf, "GRP1_1", data.div(classRemark1)); // グループ1
                    vrsOut(svf, "GRP2_1", data.div(divGrp)); // グループ2
                    vrsOut(svf, "GRP3_1", data.div(divUnitgrp)); // グループ3
                    vrsOut(svf, "GRP4_1", data.div(divUnitgrp)); // グループ4
                    vrsOut(svf, "GRP5_1", data.div(divGrp)); // グループ5


                } else if (PATTERN7_2.equals(gradeKind._tyohyoPattern)) {

                    fieldDiv1 = "DIV";
                    vrsOut(svf, "DIV", data.div(kbn)); // 区分
                    fieldDiv2 = "CLASS";
                    vrsOut(svf, "CLASS", data.div(kyouka)); // 教科
                    vrsOut(svf, "STUDY", data.remark(NAIYO)); // 学習内容
                    vrsOut(svf, "METHOD", data.remark(VALUE_TEXT)); // 文言評価

                    vrsOut(svf, "GRP1", data.div(classRemark1)); // グループ1
                    vrsOut(svf, "GRP2", data.div(divGrp)); // グループ2
                    vrsOut(svf, "GRP4", data.div(divNaiyogrp)); // グループ4
                    vrsOut(svf, "GRP5", data.div(divGrp)); // グループ5

                } else if (PATTERN_A.equals(gradeKind._tyohyoPattern)) {

                    final String f;
                    if (!diffSubclass && data._diffTangen) {
                        // 上に破線
                        f = "1";
                    } else {
                        // diffClassRemark ? 上線全部 : diffSubclass ? 上線教科まで : diffUnitGrp ? ねらい表示用線有 : 線無
                        f = diffClassRemark ? "2" : diffSubclass ? "4" : diffUnitGrp ? "7" : "3";
                    }
                    //log.info(" i = " + di + ", f = " + f + "/ " + data._diffTangen);

                    fieldDiv1 = "DIV_" + f;
                    vrsOut(svf, "DIV_" + f, data.div(kbn)); // 区分
                    fieldDiv2 = "CLASS_" + f;
                    vrsOut(svf, "CLASS_" + f, data.div(kyouka)); // 教科
                    vrsOut(svf, "COMMENT_" + f, data.remark(NAIYO)); // 内容
                    vrsOut(svf, "VAL_" + f, data.remark(REMARK_VALUE)); // 評価
                    vrsOut(svf, "AIM_" + f, data.remark(NERAI_OR_HYOKA_KOUMOKU)); // ねらい
                    vrsOut(svf, "WORD_VAL_" + f, data.remark(VALUE_TEXT_PATTERN_A)); // 文言評価

                    vrsOut(svf, "GRP1_" + f, data.div(classRemark1)); // グループ
                    vrsOut(svf, "GRP2_" + f, data.div(divGrp)); // グループ
                    vrsOut(svf, "GRP3_" + f, data.div(divNaiyogrp)); // グループ
                    vrsOut(svf, "GRP4_" + f, data.div(divUnitgrp)); // グループ
                    vrsOut(svf, "GRP5_" + f, data.div(divUnitgrp)); // グループ
                    vrsOut(svf, "GRP6_" + f, data.div(divUnitgrp)); // グループ

                } else if (PATTERN_B.equals(gradeKind._tyohyoPattern)) {

                	final boolean isTangen = null != data._remarkMap.get(NAIYO);
                	final String f;
                	if (isTangen) {
                		f = "_2";
                	} else {
                		f = "";
                	}

                	if (isTangen) {
                        vrsOut(svf, "UNITNAME_1_2", data.remark(NAIYO)); // 区分
                        vrsOut(svf, "GRP2_1_U" + f, data.div(divUnitgrp)); // 区分
                	} else {
                        vrsOut(svf, "GRP2_1_U" + f, data.div(divGrp)); // 区分
                	}
                    fieldDiv2 = "CLASS_1" + f;
                    vrsOut(svf, "CLASS_1" + f, data.div(kyouka)); // 教科
                    if (!isTangen) {
                    	vrsOut(svf, "CLASS_2_1", data.div(kyouka + "2_1")); // 教科
                    	vrsOut(svf, "CLASS_2_2", data.div(kyouka + "2_2")); // 教科
                    	vrsOut(svf, "CLASS_2_3", data.div(kyouka + "2_3")); // 教科
                    }
                    vrsOut(svf, TEXT_1_PATTERN_B + f, data.remark(TEXT_1_PATTERN_B)); // 目標
                    vrsOut(svf, TEXT_2_PATTERN_B + f, data.remark(TEXT_2_PATTERN_B)); // 指導内容・方法
                    vrsOut(svf, TEXT_3_PATTERN_B + f, data.remark(TEXT_3_PATTERN_B)); // 学習の様子および所見

                    vrsOut(svf, "GRP2_1" + f, data.div(divGrp)); // グループ
                    vrsOut(svf, "GRP3_1" + f, data.div(divUnitgrp)); // グループ
                    vrsOut(svf, "GRP4_1" + f, data.div(divUnitgrp)); // グループ
                    vrsOut(svf, "GRP5_1" + f, data.div(divUnitgrp)); // グループ

                }
                vrEndRecord(svf) ;
                _hasData = true;
                before = data;
            }
            log.debug(" _hasData = " + _hasData);
            if (isPatternBgakushuNoKiroku) {
                vrsOut(svf, "SOGO_TITLE" , "総合");
                vrsOut(svf, "SOGO_TITLE2", "所見");

                final List sogoTokenList = getTokenList(student._hreportremarkDetailDat0502Remark1, ShokenSize.getSize(_param._reportSpecialSize05_02, 44, 6));
                for (int i = 0; i < sogoTokenList.size(); i++) {
                    vrsOutn(svf, "SOGO", i + 1, (String) sogoTokenList.get(i));
                }

                vrsOut(svf, "PRINCIPAL_NAME", trim(_param._certifSchoolPrincipalName));
                if (null != student) {
                	vrsOut(svf, "STAFFNAME", student._staffname);
                }
                vrEndRecord(svf) ;
            } else if (!_hasData) {
            	if ("1".equals(_param._outputCategoryName2)) {
                    vrsOut(svf, fieldDiv2, "1"); // 区分
                    svf.VrAttribute(fieldDiv2, "X=10000");
            	} else {
                    vrsOut(svf, fieldDiv1, "1"); // 区分
                    svf.VrAttribute(fieldDiv1, "X=10000");
            	}
                vrEndRecord(svf) ;
            }
        }

        private void printHeader(final Vrw32alp svf, final Student student, final GradeKind gradeKind, final boolean isFirst) {

            final String kubun = "区分";
            final String kyoka = "教科";

            if (PATTERN_A.equals(gradeKind._tyohyoPattern)) {
                vrsOut(svf, "VAL_NAME_HEADER", "＜重点目標＞"); // 区分
                vrsOut(svf, "VAL_NOTE1", prepend(_param.getMaruSuuji("1"), gradeKind._impptDatRemark1)); // 区分
                vrsOut(svf, "VAL_NOTE2", prepend(_param.getMaruSuuji("2"), gradeKind._impptDatRemark2)); // 区分
                vrsOut(svf, "VAL_NOTE3", prepend(_param.getMaruSuuji("3"), gradeKind._impptDatRemark3)); // 区分
            }

            if (PATTERN1.equals(gradeKind._tyohyoPattern)) {

                vrsOut(svf, "DIV_NAME", kubun); // 区分名称
                vrsOut(svf, "CLASS_NAME", kyoka); // 教科名称
                vrsOut(svf, "COMMENT_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "1", "内容")); // 内容名称
                vrsOut(svf, "AIM_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "2", "ねらい")); // ねらい名称
                vrsOut(svf, "VAL_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "3", "評価")); // 評価名称
                vrsOut(svf, "WORD_VAL_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "4", "文言評価")); // 文言評価名称

            } else if (PATTERN2.equals(gradeKind._tyohyoPattern)) {

                vrsOut(svf, "DIV_NAME", kubun); // 区分名称
                vrsOut(svf, "CLASS_NAME", kyoka); // 教科名称
                vrsOut(svf, "AIM_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "2", "ねらい")); // ねらい名称
                vrsOut(svf, "METHOD_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "5", "手立て")); // 手立て名称
                vrsOut(svf, "VAL_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "4", "文言評価")); // 評価名称

            } else if (PATTERN3.equals(gradeKind._tyohyoPattern)) {

                vrsOut(svf, "DIV_NAME", kubun); // 区分名称
                vrsOut(svf, "CLASS_NAME", kyoka); // 教科名称
                vrsOut(svf, "COMMENT_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "1", "内容")); // 内容名称
                vrsOut(svf, "AIM_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "2", "ねらい")); // ねらい名称
                vrsOut(svf, "VAL_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "4", "文言評価")); // 評価名称

            } else if (PATTERN4.equals(gradeKind._tyohyoPattern)) {

                vrsOut(svf, "DIV_NAME", kubun); // 区分名称
                vrsOut(svf, "CLASS_NAME", kyoka); // 教科名称
                vrsOut(svf, "COMMENT_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "1", "単元")); // 単元名称
                vrsOut(svf, "AIM_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "2", "指導のねらい")); // ねらい名称
                vrsOut(svf, "METHOD_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "5", "手立て")); // 手立て名称
                vrsOut(svf, "VAL_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "3", "評価")); // 評価名称
                vrsOut(svf, "REMARK_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "8", "備考")); // 備考名称

            } else if (PATTERN5.equals(gradeKind._tyohyoPattern)) {

                vrsOut(svf, "DIV_NAME", kubun); // 区分名称
                vrsOut(svf, "CLASS_NAME", kyoka); // 教科名称
                vrsOut(svf, "SUBJECT_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "1", "単元題材")); // 単元題材名称
                vrsOut(svf, "ITEM_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "2", "評価項目")); // 評価項目名称
                vrsOut(svf, "VAL_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "3", "評価")); // 評価名称

            } else if (PATTERN6.equals(gradeKind._tyohyoPattern)) {

                vrsOut(svf, "DIV_NAME", kubun); // 区分名称
                vrsOut(svf, "CLASS_NAME", kyoka); // 教科名称
                vrsOut(svf, "ITEM_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "2", "評価項目")); // 評価項目名称
                vrsOut(svf, "VAL_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "3", "評価")); // 評価名称
                vrsOut(svf, "EVAL_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "6", "評定")); // 評定名称

            } else if (PATTERN7.equals(gradeKind._tyohyoPattern)) {

                vrsOut(svf, "DIV_NAME", kubun); // 区分名称
                vrsOut(svf, "CLASS_NAME", kyoka); // 教科名称
                vrsOut(svf, "HOPE_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "7", "年間目標")); // 年間目標名称
                vrsOut(svf, "STUDY_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "1", "単元・題材")); // 学習内容名称
                vrsOut(svf, "METHOD_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "2", "評価の項目")); // 手立て名称
                vrsOut(svf, "VAL_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "3", "評価")); // 評価名称
                final List wordValNameList = getTokenList(_param.getGuidanceNameDatItemRemark(gradeKind, "4", "文言評価"), 30);
                for (int i = 0; i < wordValNameList.size(); i++) {
                    vrsOut(svf, "WORD_VAL_NAME" + String.valueOf(i + 1), (String) wordValNameList.get(i)); // 文言評価名称
                }

            } else if (PATTERN8.equals(gradeKind._tyohyoPattern)) {

                vrsOut(svf, "DIV_NAME", kubun); // 区分名称
                vrsOut(svf, "CLASS_NAME", kyoka); // 教科名称
                vrsOut(svf, "SUBJECT_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "2", "学習のめあて")); // 学習のめあて名称
                vrsOut(svf, "VAL_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "3", "評価")); // 評価名称
                vrsOut(svf, "ITEM_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "4", "文言評価")); // 所見名称

            } else if (PATTERN7_2.equals(gradeKind._tyohyoPattern)) {

                vrsOut(svf, "DIV_NAME", kubun); // 区分名称
                vrsOut(svf, "CLASS_NAME", kyoka); // 教科名称
                vrsOut(svf, "STUDY_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "1", "単元・題材")); // 学習内容名称
                vrsOut(svf, "METHOD_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "4", "文言評価")); // 文言評価名称

            } else if (PATTERN_A.equals(gradeKind._tyohyoPattern)) {

                vrsOut(svf, "DIV_NAME", kubun); // 区分名称
                vrsOut(svf, "CLASS_NAME", kyoka); // 教科名称
                vrsOut(svf, "COMMENT_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "1", "指導目標")); // 内容名称
                vrsOut(svf, "VAL_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "2", "重点目標")); // ねらい名称
                vrsOut(svf, "AIM_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "3", "指導の具体的な手立て")); // 評価名称
                vrsOut(svf, "WORD_VAL_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "4", "結果及び課題")); // 文言評価名称

            } else if (PATTERN_B.equals(gradeKind._tyohyoPattern)) {
            	if (isFirst) {
            		setBheaderShoken(svf, "保護者の願い", "1", student._hogoshaNoNegai);
            		setBheaderShoken(svf, "年間指導目標", "2", student._nenkanSidouMokuhyou);

            		vrsOut(svf, "DIV_NAME", kubun); // 区分名称
            		vrsOut(svf, "CLASS_NAME", "教科等"); // 教科名称
            		vrsOut(svf, "TEXT_1_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "1", "目標"));
            		vrsOut(svf, "TEXT_2_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "2", "指導内容・方法"));
            		vrsOut(svf, "TEXT_3_NAME", _param.getGuidanceNameDatItemRemark(gradeKind, "3", "学習の様子および所見"));
            		vrEndRecord(svf);
            	}

            } else {
            	log.warn("ヘッダ設定なし: gradeKind tyohyoPattern = " + gradeKind._tyohyoPattern);
            }
        }

		private void setBheaderShoken(final Vrw32alp svf, final String title, final String groupSeq, final String shoken) {
			final List token = getTokenList(shoken, fieldKeta("SHOKEN_TEXT").intValue());
			final int titleIdx2 = token.size() / 2;
			for (int i = 0; i < Math.max(1, token.size()); i++) {
				vrsOut(svf, "SHOKEN_GRP1", groupSeq);
				vrsOut(svf, "SHOKEN_GRP2", groupSeq);
				if (i == titleIdx2) {
					vrsOut(svf, "SHOKEN_TITLE", title);
				}
				if (i < token.size()) {
					vrsOut(svf, "SHOKEN_TEXT", (String) token.get(i));
				}
				vrEndRecord(svf);
			}
		}

        private String prepend(final String a, final String b) {
        	if (StringUtils.isBlank(b)) {
        		return "";
        	}
			return StringUtils.defaultString(a) + b;
		}

		public void centeringRemarkValue(final List printDataList) {
            // REMARK_VALUEをキーでグループ化してセンタリング
            final String KEY = "KEY";
            final String START = "START";
            final String END = "END";
            final List remarkValueMapList = new ArrayList();
            for (int di = 0; di < printDataList.size(); di++) {
                final PrintData data = (PrintData) printDataList.get(di);
                final String key = data.div(divSubclasscd) + data.div(divUnitgrp);

                Map remarkValueMap = null;
                for (final Iterator it = remarkValueMapList.iterator(); it.hasNext();) {
                    final Map remarkValueMap0 = (Map) it.next();
                    if (remarkValueMap0.get(KEY).equals(key)) {
                        remarkValueMap = remarkValueMap0;
                        break;
                    }
                }
                if (null == remarkValueMap) {
                    remarkValueMap = new HashMap();
                    remarkValueMap.put(KEY, key);
                    remarkValueMap.put(START, String.valueOf(di));
                    remarkValueMapList.add(remarkValueMap);
                }
                if (null != data.remark(REMARK_VALUE)) {
                    remarkValueMap.put(REMARK_VALUE, data.remark(REMARK_VALUE)); // リストで保持するべき?でも「1行で表示できない」ことはないシネ
                    data._remarkMap.put(REMARK_VALUE, null);
                }
                remarkValueMap.put(END, String.valueOf(di));
                //log.debug(" i = " + di + ", " + key + " -> " + data.remark(REMARK_VALUE));
            }
            for (int i = 0, max = remarkValueMapList.size(); i < max; i++) {
                final Map remarkValueMap = (Map) remarkValueMapList.get(i);
                if (null != remarkValueMap.get(REMARK_VALUE)) {
                    final int startIdx = Integer.parseInt((String) remarkValueMap.get(START));
                    final int endIdx = Integer.parseInt((String) remarkValueMap.get(END));
                    final int centerIdx = startIdx + (endIdx - startIdx) / 2;
                    ((PrintData) printDataList.get(centerIdx))._remarkMap.put(REMARK_VALUE, remarkValueMap.get(REMARK_VALUE));
                    if (_param._isOutputDebug) {
                    	log.info(" centerizeRemarkValue " + remarkValueMap + " => " + remarkValueMap.get(REMARK_VALUE));
                    }
                }
            }
        }

        private List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            final String sql = Student.getStudentSql(param);
            //log.debug(" sql = " + sql);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final String ghrCd = KnjDbUtils.getString(row, "GHR_CD");
                final String hrName = param.isPrintHr() ? KnjDbUtils.getString(row, "HR_NAME") : StringUtils.defaultString(KnjDbUtils.getString(row, "GHR_NAME"), KnjDbUtils.getString(row, "HR_NAME"));
                final String hrAttendno = param.isPrintHr() ? KnjDbUtils.getString(row, "HR_ATTENDNO") : StringUtils.defaultString(KnjDbUtils.getString(row, "GHR_ATTENDNO"), KnjDbUtils.getString(row, "HR_ATTENDNO"));
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String gradecd = KnjDbUtils.getString(row, "GRADE_CD");
                final String gradeName1 = KnjDbUtils.getString(row, "GRADE_NAME1");
                final String schoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
                final String schoolKindName = KnjDbUtils.getString(row, "SCHOOL_KIND_NAME");
                final String coursename = KnjDbUtils.getString(row, "COURSENAME");
                final String majorname = KnjDbUtils.getString(row, "MAJORNAME");
                final String name = KnjDbUtils.getString(row, "NAME");
                final String trCd1 = param.isPrintHr() ? KnjDbUtils.getString(row, "HR_TR_CD1") : StringUtils.defaultString(KnjDbUtils.getString(row, "GHR_TR_CD1"), KnjDbUtils.getString(row, "HR_TR_CD1"));
                final String staffname = param.isPrintHr() ? KnjDbUtils.getString(row, "HR_STAFFNAME") : StringUtils.defaultString(KnjDbUtils.getString(row, "GHR_STAFFNAME"), KnjDbUtils.getString(row, "HR_STAFFNAME"));
                final String birthday = KnjDbUtils.getString(row, "BIRTHDAY");
                final Student student = new Student(schregno, ghrCd, hrName, hrAttendno, grade, gradecd, gradeName1, schoolKind, schoolKindName, coursename, majorname, name, trCd1, staffname, birthday);
                studentList.add(student);
            }
            return studentList;
        }

        private List getGradeKindPrintDataList(final GradeKind gradeKind, final Map ketaMap, final Param param) {
            final List list = new ArrayList();
            if (PRGID_KNJD418.equals(param._prgId)) {
                final PrintData data = new PrintData();
                data.setDataDiv(classRemark1, "00");
                list.add(data);
            } else {
                for (int cri = 0; cri < gradeKind._classRemark1list.size(); cri++) {
                    final ClassRemark1 cr1 = (ClassRemark1) gradeKind._classRemark1list.get(cri);

                    list.addAll(getClassRemark1PrintDataList(gradeKind, cr1, ketaMap, param));
                }
//                // ページ最大まで空行表示
//                int maxLine = 0;
//                for (int j = list.size(); j < maxLine; j++) {
//                    final PrintData data = new PrintData();
//                    data._classRemark1Grp = list.size() == 0 ? "00" : ((PrintData) list.get(list.size() - 1))._classRemark1Grp;
//                    list.add(data);
//                }
            }
            return list;
        }

        private List getClassRemark1PrintDataList(final GradeKind gradeKind, final ClassRemark1 cr1, final Map ketaMap, final Param param) {
            final List list = new ArrayList();

            final List eachSubclassDataList = new ArrayList();
//            GradeKindSubclass subclassBefore = null;
            for (final Iterator it = cr1._gradeKindSubclassList.iterator(); it.hasNext();) {
                final GradeKindSubclass subclass = (GradeKindSubclass) it.next();
                final List printDataList = getSubclassPrintDataList(gradeKind, subclass, ketaMap, param);
//                if (null != subclassBefore && null != subclassBefore._subclassname && subclassBefore._subclassname.equals(subclass._subclassname)) {
//                    final List dataList = (List) eachSubclassDataList.get(eachSubclassDataList.size() - 1);
//                    for (final Iterator pit = printDataList.iterator(); pit.hasNext();) {
//                        final PrintData d = (PrintData) pit.next();
//                        if (d._subclassnameExtends) { // 科目名再セットのため、科目名で伸張したデータは対象外とする
//                            continue;
//                        }
//                        d.setDataDiv(divGrp, String.valueOf(subclassBefore._classgrp));
//                        d.setDataDiv(divSubclasscd, subclassBefore._subclasscd); // 同じ科目名の科目コードをセット（線を表示しない）
//                        d.setDataDiv(kyouka, null); // 科目名を表示しない
//                        dataList.add(d);
//                    }
//                    //subclassBefore = subclass;
//                } else {
                    eachSubclassDataList.add(printDataList);
//                    subclassBefore = subclass;
//                }
            }

            for (final Iterator it = eachSubclassDataList.iterator(); it.hasNext();) {
                final List dataList = (List) it.next();
                // 教科名センタリング
                centeringDiv(dataList, kyouka, 1);
                if (PATTERN_B.equals(gradeKind._tyohyoPattern)) {
                    centeringDiv(dataList, kyouka + "2_1", 1);
                    centeringDiv(dataList, kyouka + "2_2", 1);
                    centeringDiv(dataList, kyouka + "2_3", 1);
                }
                list.addAll(dataList);
            }

            final String printName = cr1._name.length() < list.size() ? StringUtils.center(cr1._name, list.size()) : cr1._name;

            PrintData last = new PrintData();
            for (int i = 0; i < Math.min(printName.length(), list.size()); i++) {
                final PrintData printData = (PrintData) list.get(i);
                printData.setDataDiv(kbn, String.valueOf(printName.charAt(i)));
                last = printData;
            }
            for (int i = list.size(); i < printName.length(); i++) { // 「区分」の不足分
                final PrintData printData = new PrintData(last);
                printData.setDataDiv(divSubclasscd, "//"); // コメントにすると最後のブロックに境界線を表示しない
                list.add(printData);
                printData.setDataDiv(kbn, String.valueOf(printName.charAt(i)));
            }
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final PrintData printData = (PrintData) it.next();
                printData.setDataDiv(classRemark1, cr1._classRemark1);
            }
            return list;
        }

        private void debugPrintDataList(final String text, final List list) {
            for (int i = 0, max = list.size(); i < max; i++) {
                final PrintData d = (PrintData) list.get(i);
                //log.debug(" " + i + " = " + d);
                log.info(" " + text + " "+ i + ": subclasscd = " + d.div(divSubclasscd) + ", naiyogrp = " + d.div(divNaiyogrp) + ", unigrp = " + d.div(divUnitgrp));
            }
        }

        private List getSubclassPrintDataList(final GradeKind gradeKind, final GradeKindSubclass subclass, final Map ketaMap, final Param param) {

            final List hyoukaNaiyoPrintDataList = new ArrayList();

            if ("1".equals(subclass._unitAimDiv)) {
                for (int ri = 0; ri < subclass._unitRemarkList.size(); ri++) {
                    final GradeKindSubclass.UnitRemark uremark = (GradeKindSubclass.UnitRemark) subclass._unitRemarkList.get(ri);

                    final Map dataMap = createDataMap();
                    if (PATTERN_B.equals(gradeKind._tyohyoPattern)) {
                    	if (!"00".equals(uremark._unitcd)) {
                    		dataMap.put(NAIYO, getTokenList(uremark._unitname, 2));
                    	}
                		dataMap.put(TEXT_1_PATTERN_B, getTokenList(uremark._remark71, keta(ketaMap, TEXT_1_PATTERN_B)));
                		dataMap.put(TEXT_2_PATTERN_B, getTokenList(uremark._remark72, keta(ketaMap, TEXT_2_PATTERN_B)));
                		dataMap.put(TEXT_3_PATTERN_B, getTokenList(uremark._remark73, keta(ketaMap, TEXT_3_PATTERN_B)));

                    } else {
                    	if (PATTERN_A.equals(gradeKind._tyohyoPattern)) {
                    		final String tgtRemark = PRGID_KNJD419.equals(param._prgId) ? uremark._groupRemark2 : uremark._remark2;
                    		dataMap.put(VALUE_TEXT_PATTERN_A, getTokenList(tgtRemark, keta(ketaMap, VALUE_TEXT_PATTERN_A)));
                    	}
                    	final String tgtRemark = PRGID_KNJD419.equals(param._prgId) ? uremark._groupRemark1 : uremark._remark1;
                    	dataMap.put(NERAI_OR_HYOKA_KOUMOKU, getTokenList(tgtRemark, keta(ketaMap, NERAI_OR_HYOKA_KOUMOKU)));
                    	if (ketaMap.containsKey(REMARK_VALUE)) {
                    		dataMap.put(REMARK_VALUE, getTokenList(uremark._assessMark1, keta(ketaMap, REMARK_VALUE)));
                    	}
                    	if (ketaMap.containsKey(NAIYO)) {
                    		dataMap.put(NAIYO, getTokenList(uremark._unitname, keta(ketaMap, NAIYO)));
                    	}
                    }
                    final String naiyoGrp = uremark._unitcd; // String.valueOf(subclass._subgrp % 10) + uremark._unitcd.substring(uremark._unitcd.length() - 1);
                    final String unitGrp = naiyoGrp;

                    final List dataList = createDataList(subclass, naiyoGrp, unitGrp, dataMap);
                    if (PATTERN_B.equals(gradeKind._tyohyoPattern) && !"00".equals(uremark._unitcd) || !PATTERN_B.equals(gradeKind._tyohyoPattern)) {
                    	final PrintData data = (PrintData) dataList.get(0);
                    	data._diffTangen = true;
                    }

                    hyoukaNaiyoPrintDataList.addAll(dataList);
                }
            } else {

                final String remarkField = PRGID_KNJD419.equals(param._prgId) ? GROUP_REMARK : REMARK;
                final List neraiList = new ArrayList();
                if (subclass._remarkCount == -1) {
                    subclass._remarkCount = 1;
//                    for (int l = 10; l >= 1; l--) {
//
//                        final String tgtRemark = subclass.remark(remarkField + String.valueOf(l));
//                        if (!StringUtils.isBlank(tgtRemark)) {
//                            break;
//                        }
//                        subclass._remarkCount = l;
//                    }
                }

                for (int l = 1; l <= subclass._remarkCount; l++) {

                    final Map dataMap = createDataMap();
					if (PATTERN_B.equals(gradeKind._tyohyoPattern)) {
                    	final String tgtRemark1 = subclass.remark(remarkField + String.valueOf(71));
                    	final String tgtRemark2 = subclass.remark(remarkField + String.valueOf(72));
                    	final String tgtRemark3 = subclass.remark(remarkField + String.valueOf(73));
                		dataMap.put(TEXT_1_PATTERN_B, getTokenList(tgtRemark1, keta(ketaMap, TEXT_1_PATTERN_B)));
                		dataMap.put(TEXT_2_PATTERN_B, getTokenList(tgtRemark2, keta(ketaMap, TEXT_2_PATTERN_B)));
                		dataMap.put(TEXT_3_PATTERN_B, getTokenList(tgtRemark3, keta(ketaMap, TEXT_3_PATTERN_B)));

                    } else {
                    	if (PATTERN_A.equals(gradeKind._tyohyoPattern)) {
                    		final String tgtRemark = subclass.remark(remarkField + String.valueOf(60 + l));
                    		dataMap.put(VALUE_TEXT_PATTERN_A, getTokenList(tgtRemark, keta(ketaMap, VALUE_TEXT_PATTERN_A)));
                    	}
                    	final String tgtRemark = subclass.remark(remarkField + String.valueOf(l));
                    	dataMap.put(NERAI_OR_HYOKA_KOUMOKU, getTokenList(tgtRemark, keta(ketaMap, NERAI_OR_HYOKA_KOUMOKU)));
                    	final String assessMark = subclass.remark(ASSESS_MARK + String.valueOf(l));
                    	if (ketaMap.containsKey(REMARK_VALUE)) {
                    		dataMap.put(REMARK_VALUE, getTokenList(assessMark, keta(ketaMap, REMARK_VALUE)));
                    	}
                    }
                    final String naiyoGrp = String.valueOf(subclass._subgrp);
                    final String unitGrp = String.valueOf(subclass._subgrp % 10) + String.valueOf(l % 10);
                    neraiList.addAll(createDataList(subclass, naiyoGrp, unitGrp, dataMap));
                    //log.info(" createDataList(" + subclass._subgrp + ", " + naiyoGrp + ", " + unitGrp + ", " + subclass._subclasscd + ") = " + subDataList);
                }
            	//log.info(" neraiList00 = " + neraiList);

                final List unitNameList = new ArrayList();
                for (int ri = 0; ri < subclass._unitRemarkList.size(); ri++) {
                    final GradeKindSubclass.UnitRemark uremark = (GradeKindSubclass.UnitRemark) subclass._unitRemarkList.get(ri);
                    if (StringUtils.isBlank(uremark._unitname)) {
                        continue;
                    }
                    unitNameList.add(uremark._unitname);
                }
                final Map dataMap = createDataMap();
                if (ketaMap.containsKey(NAIYO)) {
                	dataMap.put(NAIYO, getTokenList(mkString(unitNameList, "\n"), keta(ketaMap, NAIYO)));
                }
                final String naiyoGrp = String.valueOf(subclass._subgrp % 10);
                final String unitGrp = String.valueOf(subclass._subgrp % 10) + naiyoGrp;
                final List tangenmeiList = createDataList(subclass, naiyoGrp, unitGrp, dataMap);

                hyoukaNaiyoPrintDataList.addAll(add(neraiList, tangenmeiList, new String[] {NERAI_OR_HYOKA_KOUMOKU, REMARK_VALUE, VALUE_TEXT_PATTERN_A, TEXT_1_PATTERN_B, TEXT_2_PATTERN_B, TEXT_3_PATTERN_B}));
                if (_param._isOutputDebug) {
                	log.info(" subclass = " + subclass._subclasscd + ":" + subclass._subclassname);
                	debugPrintDataList(" nerai -> ", neraiList);
                	log.info(" neraiList = " + neraiList);
                	debugPrintDataList(" tangenmei -> ", tangenmeiList);
                	log.info(" tangenmeiList = " + tangenmeiList);
                	debugPrintDataList(" hyokaNaiyo -> ", hyoukaNaiyoPrintDataList);
                }
            }

            final Map dataMap = createDataMap();
            loopPutData:
            for (final Iterator it = dataMap.keySet().iterator(); it.hasNext();) {
                final String key = (String) it.next();
                if (key.equals(NERAI_OR_HYOKA_KOUMOKU) || key.equals(REMARK_VALUE) || key.equals(NAIYO) || key.equals(VALUE_TEXT_PATTERN_A)) {
                    continue loopPutData;
                }
                // ???
                if ("1".equals(subclass._unitAimDiv)) {
                } else {
                    for (int l = 1; l <= 10; l++) {
                    	final String notTargetKey3 = REMARK + String.valueOf(l);
                    	if (notTargetKey3.equals(key)) {
                            continue loopPutData;
                    	}
                    	final String notTargetKey4 = REMARK_VALUE + String.valueOf(l);
                    	if (notTargetKey4.equals(key)) {
                            continue loopPutData;
                    	}
                    	final String notTargetKey2 = GROUP_REMARK + String.valueOf(l);
                    	if (notTargetKey2.equals(key)) {
                            continue loopPutData;
                    	}
                    }
                }
                if (ketaMap.keySet().contains(key)) {
                	dataMap.put(key, getTokenList(subclass.remark(key), keta(ketaMap, key)));
                }
            }

            final String naiyoGrp = "-";
            final String unitGrp = String.valueOf(subclass._subgrp % 10) + naiyoGrp;
            final List hyoukaNaiyoIgaiPrintDataSet = createDataList(subclass, naiyoGrp, unitGrp, dataMap);

            final List rtn = add(hyoukaNaiyoPrintDataList, hyoukaNaiyoIgaiPrintDataSet, new String[] {NERAI_OR_HYOKA_KOUMOKU, REMARK_VALUE, NAIYO, VALUE_TEXT_PATTERN_A, TEXT_1_PATTERN_B, TEXT_2_PATTERN_B, TEXT_3_PATTERN_B});
            if (_param._isOutputDebug) {
            	for (int i = 0; i < rtn.size(); i++) {
            		log.info(" subclass " + subclass._subclasscd + " printDataList[" + i + " / " + rtn.size() + "] = " + rtn.get(i));
            	}
            }
            if (PATTERN_B.equals(gradeKind._tyohyoPattern)) {
            	centeringDivGroupby(rtn, divUnitgrp, NAIYO);
            }

            // 科目名設定
            final int subclassnameketa = PATTERN_B.equals(gradeKind._tyohyoPattern) ? 2 : 6;
        	PrintData lastData = null;
            if (PATTERN_B.equals(gradeKind._tyohyoPattern) && concat(SOGOTEKINA_GAKUSHUNO_JIKAN_SPLIT).equals(subclass._subclassname)) {
            	// Bパターンの科目名"総合的な学習の時間"は3列で出力する
            	final int maxLength = maxLength(SOGOTEKINA_GAKUSHUNO_JIKAN_SPLIT);
				for (int i = 0; i < Math.min(rtn.size(), maxLength); i++) {
            		final PrintData data = (PrintData) rtn.get(i);
            		for (int j = 0; j < SOGOTEKINA_GAKUSHUNO_JIKAN_SPLIT.length; j++) {
            			data.setDataDiv(kyouka + "2_" + String.valueOf(j + 1), i < SOGOTEKINA_GAKUSHUNO_JIKAN_SPLIT[j].length() ? String.valueOf(SOGOTEKINA_GAKUSHUNO_JIKAN_SPLIT[j].charAt(i)) : "　");
            		}
            		lastData = data;
            	}
            	for (int i = Math.min(rtn.size(), maxLength); i < maxLength; i++) {
            		final PrintData data = new PrintData(lastData);
            		for (int j = 0; j < SOGOTEKINA_GAKUSHUNO_JIKAN_SPLIT.length; j++) {
            			data.setDataDiv(kyouka + "2_" + String.valueOf(j + 1), i < SOGOTEKINA_GAKUSHUNO_JIKAN_SPLIT[j].length() ? String.valueOf(SOGOTEKINA_GAKUSHUNO_JIKAN_SPLIT[j].charAt(i)) : "　");
            		}
            		data._subclassnameExtends = true;
            		rtn.add(data);
            	}
            } else {
            	final List subclassnameDivList = getTokenList(subclass._subclassname, subclassnameketa);
            	for (int i = 0; i < Math.min(rtn.size(), subclassnameDivList.size()); i++) {
            		final PrintData data = (PrintData) rtn.get(i);
            		data.setDataDiv(kyouka, (String) subclassnameDivList.get(i));
            		lastData = data;
            	}
            	for (int i = Math.min(rtn.size(), subclassnameDivList.size()); i < subclassnameDivList.size(); i++) {
            		final PrintData data = new PrintData(lastData);
            		data.setDataDiv(kyouka, (String) subclassnameDivList.get(i));
            		data._subclassnameExtends = true;
            		rtn.add(data);
            	}
            }
            // 評定をセンタリング
            centeringRemark(rtn, VALUE);
            return rtn;
        }
        
        private int maxLength(final String[] split) {
        	int maxLength = 0;
        	for (int i = 0; i < split.length; i++) {
        		maxLength = Math.max(maxLength, split[i].length());
        	}
        	return maxLength;
        }
        
        private String concat(final String[] split) {
        	final StringBuffer stb = new StringBuffer();
        	for (int i = 0; i < split.length; i++) {
        		stb.append(split[i]);
        	}
        	return stb.toString();
        }

        /**
         * センタリングする
         * @param list データのリスト
         * @param centeringDivKey センタリング対象のデータ
         * @param dataFlg センタリング対象のデータのマップ指定 1:div, 2:remark
         */
		private void centeringDiv(final List list, final String centeringDivKey, final int dataFlg) {
            final LinkedList centringDivList = new LinkedList();
            for (final Iterator nit = list.iterator(); nit.hasNext();) {
                final PrintData d = (PrintData) nit.next();
                Map data = null;
                if (dataFlg == 1) {
                	data = d._div;
                } else if (dataFlg == 2) {
                	data = d._remarkMap;
                }
                if (null != data.get(centeringDivKey)) {
                	centringDivList.add(data.get(centeringDivKey));
                	data.put(centeringDivKey, null);
                }
            }
            if (centringDivList.isEmpty()) {
            	if (_param._isOutputDebug) {
            		log.info(" centringDivList empty! key = " + centeringDivKey + ":" + list);
            	}
            }
            // centering
            for (int i = 0, len = (list.size() - centringDivList.size()) / 2; i < len; i++) {
                centringDivList.add(0, "　");
            }
            for (int i = 0, len = (list.size() - centringDivList.size()); i < len; i++) {
                centringDivList.add("　");
            }
        	if (_param._isOutputDebug) {
        		log.info(" centringDivList(" + centeringDivKey + ") = " + centringDivList);
        	}
            for (int i = 0, min = Math.min(list.size(), centringDivList.size()); i < min; i++) {
                final PrintData d = (PrintData) list.get(i);
                Map data = null;
                if (dataFlg == 1) {
                	data = d._div;
                } else if (dataFlg == 2) {
                	data = d._remarkMap;
                }
                data.put(centeringDivKey, centringDivList.get(i));
            }
        }

		private void centeringDivGroupby(final List list, final String groupByDiv, final String centeringDivKey) {
			final Map groupByDivMap = new HashMap();
			boolean hasDiffTangen = false;
            for (final Iterator nit = list.iterator(); nit.hasNext();) {
                final PrintData d = (PrintData) nit.next();
                hasDiffTangen = hasDiffTangen || d._diffTangen;
                final String group = d.div(groupByDiv);
            	if (null == groupByDivMap.get(group)) {
            		groupByDivMap.put(group, new LinkedList());
            	}
            	final List centringDivList = (List) groupByDivMap.get(group);
                centringDivList.add(d);
            }
            if (!hasDiffTangen) {
            	return;
            }
            for (final Iterator it = groupByDivMap.keySet().iterator(); it.hasNext();) {
            	final String key = (String) it.next();
            	final LinkedList groupList = (LinkedList) groupByDivMap.get(key);
//            	final String size = String.valueOf(groupList.size());
//            	if (_param._isOutputDebug) {
//            		for (int j = 0; j < groupList.size(); j++) {
//            			log.info(" key = " + key + " : before " + j + " / " + size + " : " + groupList.get(j));
//            		}
//            	}
            	centeringDiv(groupList, centeringDivKey, 2);
//            	if (_param._isOutputDebug) {
//            		for (int j = 0; j < groupList.size(); j++) {
//            			log.info(" key = " + key + " :  after " + j + " / " + size + " : " + groupList.get(j));
//            		}
//            	}
            }
        }

        private void centeringRemark(final List list, final String centeringRemarkKey) {
            final LinkedList centringRemarkList = new LinkedList();
            for (int i = 0; i < list.size(); i++) {
                final PrintData d = (PrintData) list.get(i);
                if (null != d._remarkMap.get(centeringRemarkKey)) {
                    centringRemarkList.add(d._remarkMap.get(centeringRemarkKey));
                    d._remarkMap.put(centeringRemarkKey, null);
                }
            }
            for (int i = 0, len = (list.size() - centringRemarkList.size()) / 2; i < len; i++) {
                centringRemarkList.add(0, null);
            }
            for (int i = 0, min = Math.min(list.size(), centringRemarkList.size()); i < min; i++) {
                final PrintData d = (PrintData) list.get(i);
                d._remarkMap.put(centeringRemarkKey, centringRemarkList.get(i));
            }
        }

        private Map createDataMap() {
            final Map m = new HashMap();
            for (int i = 1; i <= 10; i++) {
                m.put(REMARK + String.valueOf(i), Collections.EMPTY_LIST);
                m.put(REMARK_VALUE + String.valueOf(i), Collections.EMPTY_LIST);
                m.put(GROUP_REMARK + String.valueOf(i), Collections.EMPTY_LIST);
                m.put(REMARK + String.valueOf(60 + i), Collections.EMPTY_LIST);
                m.put(REMARK_VALUE + String.valueOf(60 + i), Collections.EMPTY_LIST);
                m.put(GROUP_REMARK + String.valueOf(60 + i), Collections.EMPTY_LIST);
            }
            m.put(NERAI_OR_HYOKA_KOUMOKU, Collections.EMPTY_LIST);
            m.put(NAIYO, Collections.EMPTY_LIST);
            m.put(PROCEDURE, Collections.EMPTY_LIST);
            m.put(VALUE_TEXT, Collections.EMPTY_LIST);
            m.put(VALUE_TEXT2, Collections.EMPTY_LIST);
            m.put(VALUE, Collections.EMPTY_LIST);
            m.put(YEAR_TARGET, Collections.EMPTY_LIST);
            m.put(VALUE_TEXT_PATTERN_A, Collections.EMPTY_LIST);
            return m;
        }

        private String mkString(final List strList, final String comma) {
            String c = "";
            final StringBuffer stb = new StringBuffer();
            for (final Iterator it = strList.iterator(); it.hasNext();) {
                final String v = (String) it.next();
                if (StringUtils.isBlank(v)) {
                    continue;
                }
                stb.append(c).append(v);
                c = comma;
            }
            return stb.toString();
        }

        private List add(final List seiseki1, final List seiseki2, final String[] jogai) {
            final List rtn = new ArrayList();
            final int min = Math.min(seiseki1.size(), seiseki2.size());
            final int max = Math.max(seiseki1.size(), seiseki2.size());
            PrintData lastd1 = null;
            for (int i = 0; i < min; i++) {
                final PrintData d1 = (PrintData) seiseki1.get(i);
                rtn.add(d1);
                if (i < seiseki2.size()) {
                    final PrintData d2 = (PrintData) seiseki2.get(i);
                    for (final Iterator it = d1._remarkMap.keySet().iterator(); it.hasNext();) {
                        final String key = (String) it.next();
                        if (ArrayUtils.contains(jogai, key)) {
                        	continue;
                        }
                        d1._remarkMap.put(key, d2.remark(key));
                    }
                }
                lastd1 = d1;
            }
            for (int i = min; i < max; i++) {
                PrintData d = null;
                if (i < seiseki1.size()) {
                    d = (PrintData) seiseki1.get(i);
                } else if (i < seiseki2.size()) {
                    d = (PrintData) seiseki2.get(i);
                    if (null != lastd1) {
                        d.setDataDiv(divNaiyogrp, lastd1.div(divNaiyogrp));
                        d.setDataDiv(divUnitgrp, lastd1.div(divUnitgrp));
                    }
                }
                rtn.add(d);
            }
            return rtn;
        }

        private int keta(final Map map, final String key) {
            final Integer k = (Integer) map.get(key);
            if (null == k) {
            	try {
            		throw new IllegalArgumentException(" not found keta : " + key + " in " + map); // スタックトレースをログに出力したいだけ
            	} catch (Exception e) {
            		log.error("exception!", e);
            	}
                return 999;
            }
            return k.intValue();
        }

        private List createDataList(final GradeKindSubclass subclass, final String naiyoGrp, final String unitGrp, final Map m) {
            final List subDataList = new ArrayList();
            final int maxline = maxline(m);
            for (int line = 0; line < maxline; line++) {
                final PrintData data = new PrintData();
                data.setDataDiv(divGrp, String.valueOf(subclass._subgrp));
                data.setDataDiv(divNaiyogrp, naiyoGrp);
                data.setDataDiv(divUnitgrp, unitGrp);
                data.setDataDiv(divSubclasscd, subclass._subclasscd);
                for (final Iterator it = m.keySet().iterator(); it.hasNext();) {
                    final String key = (String) it.next();
                    final List lst = (List) m.get(key);
                    data._remarkMap.put(key, line < lst.size() ? (String) lst.get(line) : null);
                }
                subDataList.add(data);
            }
            return subDataList;
        }

        private int maxline(final Map m) {
            int maxline = 1;
            for (final Iterator it = m.keySet().iterator(); it.hasNext();) {
                final String key = (String) it.next();
                final List lines = (List) m.get(key);
                maxline = Math.max(maxline, lines.size());
            }
            return maxline;
        }
    }

    private static class Student {
        final String _schregno;
        final String _ghrCd;
        final String _hrName;
        final String _attendno;
        final String _grade;
        final String _gradecd;
        final String _gradeName1;
        final String _schoolKind;
        final String _schoolKindName;
        final String _coursename;
        final String _majorname;
        final String _name;
        final String _trCd1;
        final String _staffname;
        final String _birthday;

        private GradeKind _gradeKind;
        private Map _attendSemesDatMap = Collections.EMPTY_MAP; // 出欠の記録
        private Map _attendSemesDatMonthMap = Collections.EMPTY_MAP; // 出欠の記録月ごと

        private String _totalstudytime;
        private String _specialactremark;
        private String _hreportremarkDetailDat0501Remark1;
        private String _hreportremarkDetailDat0502Remark1;
        private Map _hreportremarkDetailDatAttendrecRemarkMap = new HashMap();

        private String _hogoshaNoNegai;
        private String _nenkanSidouMokuhyou;

        Student(
            final String schregno,
            final String ghrCd,
            final String hrName,
            final String hrAttendno,
            final String grade,
            final String gradecd,
            final String gradeName1,
            final String schoolKind,
            final String schoolKindName,
            final String coursename,
            final String majorname,
            final String name,
            final String trCd1,
            final String staffname,
            final String birthday
        ) {
            _schregno = schregno;
            _ghrCd = ghrCd;
            _hrName = hrName;
            _attendno = hrAttendno;
            _grade = grade;
            _gradecd = gradecd;
            _gradeName1 = gradeName1;
            _schoolKind = schoolKind;
            _schoolKindName = schoolKindName;
            _coursename = coursename;
            _majorname = majorname;
            _name = name;
            _trCd1 = trCd1;
            _staffname = staffname;
            _birthday = birthday;
        }

        private String getPrintNameB() {
            final String hrName = "第" + (NumberUtils.isDigits(_gradecd) ? getZenkaku(String.valueOf(Integer.parseInt(_gradecd))) : "　")  + "学年";
            final String name = StringUtils.defaultString(_name);
            return hrName + "　" + name;
        }

        private String getPrintName() {
            final String hrName = StringUtils.defaultString(_hrName);
            final String attendno = StringUtils.isNumeric(_attendno) ? (String.valueOf(Integer.parseInt(_attendno)) + "番") : StringUtils.defaultString(_attendno);
            final String name = StringUtils.defaultString(_name);
            return hrName + "　" + attendno + "　" + name;
        }

        private static void setShoken(final DB2UDB db2, final Param param, final Student student) {
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT SEMESTER, TOTALSTUDYTIME, SPECIALACTREMARK, ATTENDREC_REMARK");
                sql.append(" FROM HREPORTREMARK_DAT ");
                sql.append(" WHERE YEAR = '" + param._ctrlYear + "' ");
                sql.append("   AND SCHREGNO = '" + student._schregno + "' ");

                for (final Iterator it = KnjDbUtils.query(db2, sql.toString()).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    if (StringUtils.defaultString(param._d078, param._semester).equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                        student._totalstudytime = KnjDbUtils.getString(row, "TOTALSTUDYTIME");
                        student._specialactremark = KnjDbUtils.getString(row, "SPECIALACTREMARK");
                    }
                    student._hreportremarkDetailDatAttendrecRemarkMap.put(KnjDbUtils.getString(row, "SEMESTER"), KnjDbUtils.getString(row, "ATTENDREC_REMARK"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT DIV, CODE, REMARK1 ");
                sql.append(" FROM HREPORTREMARK_DETAIL_DAT ");
                sql.append(" WHERE YEAR = '" + param._ctrlYear + "' ");
                sql.append("   AND SEMESTER = '" + StringUtils.defaultString(param._d078, param._semester) + "' ");
                sql.append("   AND SCHREGNO = '" + student._schregno + "' ");

                //log.info(" detail sql = " + sql);

                for (final Iterator it = KnjDbUtils.query(db2, sql.toString()).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    if ("05".equals(KnjDbUtils.getString(row, "DIV")) && "01".equals(KnjDbUtils.getString(row, "CODE"))) {
                        student._hreportremarkDetailDat0501Remark1 = KnjDbUtils.getString(row, "REMARK1");
                    } else if ("05".equals(KnjDbUtils.getString(row, "DIV")) && "02".equals(KnjDbUtils.getString(row, "CODE"))) {
                        student._hreportremarkDetailDat0502Remark1 = KnjDbUtils.getString(row, "REMARK1");
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }


        private static void setShokenB(final DB2UDB db2, final Param param, final Student student) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SEQ, REMARK ");
            sql.append(" FROM HREPORT_GUIDANCE_SCHREG_SEMESTER_DAT ");
            sql.append(" WHERE YEAR = '" + param._ctrlYear + "' ");
            sql.append("   AND SEMESTER = '9' ");
            sql.append("   AND SCHREGNO = '" + student._schregno + "' ");

            for (final Iterator it = KnjDbUtils.query(db2, sql.toString()).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
            	final String seq = KnjDbUtils.getString(row, "SEQ");
            	final String remark = KnjDbUtils.getString(row, "REMARK");
            	if ("1".equals(seq)) {
            		student._hogoshaNoNegai = remark;
            	} else if ("2".equals(seq)) {
            		student._nenkanSidouMokuhyou = remark;
            	}
            }
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     REGDH.HR_NAME, ");
            stb.append("     REGD.ATTENDNO AS HR_ATTENDNO, ");
            stb.append("     GHR.GHR_CD, ");
            stb.append("     GHR.GHR_ATTENDNO, ");
            stb.append("     GHRH.GHR_NAME, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     GDAT.GRADE_CD, ");
            stb.append("     GDAT.GRADE_NAME1, ");
            stb.append("     GDAT.SCHOOL_KIND, ");
            stb.append("     BASE.NAME, ");
            stb.append("     NMA023.ABBV1 AS SCHOOL_KIND_NAME, ");
            stb.append("     CM.COURSENAME, ");
            stb.append("     MAJ.MAJORNAME, ");
            stb.append("     GHRH.TR_CD1 AS GHR_TR_CD1, ");
            stb.append("     T4.STAFFNAME AS GHR_STAFFNAME, ");
            stb.append("     REGDH.TR_CD1 AS HR_TR_CD1, ");
            stb.append("     T8.STAFFNAME AS HR_STAFFNAME, ");
            stb.append("     BASE.BIRTHDAY ");
            stb.append(" FROM SCHREG_REGD_DAT REGD ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("     AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("     AND REGDH.GRADE = REGD.GRADE ");
            stb.append("     AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_REGD_GHR_DAT GHR ON GHR.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     AND GHR.YEAR = REGD.YEAR ");
            stb.append("     AND GHR.SEMESTER = REGD.SEMESTER ");
            stb.append(" LEFT JOIN SCHREG_REGD_GHR_HDAT GHRH ON GHRH.YEAR = GHR.YEAR ");
            stb.append("     AND GHRH.SEMESTER = GHR.SEMESTER ");
            stb.append("     AND GHRH.GHR_CD = GHR.GHR_CD ");
            stb.append(" LEFT JOIN COURSE_MST CM ON CM.COURSECD = REGD.COURSECD ");
            stb.append(" LEFT JOIN MAJOR_MST MAJ ON MAJ.COURSECD = REGD.COURSECD ");
            stb.append("     AND MAJ.MAJORCD = REGD.MAJORCD ");
            stb.append(" LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = GHRH.TR_CD1 ");
            stb.append(" LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
            stb.append("     AND GDAT.GRADE = REGD.GRADE ");
            stb.append(" LEFT JOIN STAFF_MST T8 ON T8.STAFFCD = REGDH.TR_CD1 ");
            stb.append(" LEFT JOIN NAME_MST NMA023 ON NMA023.NAMECD1 = 'A023' ");
            stb.append("     AND NMA023.NAME1 = GDAT.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "'  ");
            stb.append("     AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(" ORDER BY ");
            if (param.isPrintHr()) {
                stb.append("     REGD.GRADE, ");
                stb.append("     REGD.HR_CLASS, ");
                stb.append("     REGD.ATTENDNO, ");
            }
            stb.append("     GHR.GHR_CD, ");
            stb.append("     GHR.GHR_ATTENDNO ");
            return stb.toString();
        }

        public String toString() {
            return "Student(" + _schregno + ")";
        }
    }

    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {

        final String _key;
        int _lesson;
        int _suspend;
        int _mourning;
        int _suspendMourning;
        int _mlesson;
        int _sick;
        int _sickOnly;
        int _noticeOnly;
        int _nonoticeOnly;
        int _absent;
        int _present;
        int _late;
        int _early;
        int _transferDate;
        int _offdays;
        int _kekkaJisu;
        int _virus;
        int _koudome;
        String _remark;

        public AttendSemesDat(
                final String key
        ) {
            _key = key;
        }

        public void add(
                final AttendSemesDat o
        ) {
            _lesson += o._lesson;
            _suspend += o._suspend;
            _mourning += o._mourning;
            _suspendMourning += o._suspendMourning;
            _mlesson += o._mlesson;
            _sick += o._sick;
            _sickOnly += o._sickOnly;
            _noticeOnly += o._noticeOnly;
            _nonoticeOnly += o._nonoticeOnly;
            _absent += o._absent;
            _present += o._present;
            _late += o._late;
            _early += o._early;
            _transferDate += o._transferDate;
            _offdays += o._offdays;
            _kekkaJisu += o._kekkaJisu;
            _virus += o._virus;
            _koudome += o._koudome;
        }

        private static Map getAttendSemesDatMap(final DB2UDB db2, final Param param, final String schregno) {
            final Map map = new HashMap();
            try {
                PreparedStatement ps = (PreparedStatement) param._psMap.get("ATTEND_SEMES");

                final Integer zero = new Integer(0);

                for (final Iterator it = KnjDbUtils.query(db2, ps, new Object[] {schregno}).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    final String semester = KnjDbUtils.getString(row, "SEMESTER");

                    final AttendSemesDat attendSemesDat = new AttendSemesDat(semester);
                    attendSemesDat._lesson = KnjDbUtils.getInt(row, "LESSON", zero).intValue();
                    attendSemesDat._suspend = KnjDbUtils.getInt(row, "SUSPEND", zero).intValue();
                    attendSemesDat._mourning = KnjDbUtils.getInt(row, "MOURNING", zero).intValue();
                    attendSemesDat._mlesson = KnjDbUtils.getInt(row, "MLESSON", zero).intValue();
                    attendSemesDat._sick = KnjDbUtils.getInt(row, "SICK", zero).intValue();
                    attendSemesDat._absent = KnjDbUtils.getInt(row, "ABSENT", zero).intValue();
                    attendSemesDat._present = KnjDbUtils.getInt(row, "PRESENT", zero).intValue();
                    attendSemesDat._late = KnjDbUtils.getInt(row, "LATE", zero).intValue();
                    attendSemesDat._early = KnjDbUtils.getInt(row, "EARLY", zero).intValue();
                    attendSemesDat._transferDate = KnjDbUtils.getInt(row, "TRANSFER_DATE", zero).intValue();
                    attendSemesDat._offdays = KnjDbUtils.getInt(row, "OFFDAYS", zero).intValue();
                    attendSemesDat._kekkaJisu = KnjDbUtils.getInt(row, "KEKKA_JISU", zero).intValue();
                    attendSemesDat._virus = "true".equals(param._useVirus) ? KnjDbUtils.getInt(row, "VIRUS", zero).intValue() : 0;
                    attendSemesDat._koudome = "true".equals(param._useKoudome) ? KnjDbUtils.getInt(row, "KOUDOME", zero).intValue() : 0;

                    map.put(semester, attendSemesDat);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return map;
        }

        private static Map getAttendSemesDatMonthMap(final DB2UDB db2, final Param param, final String schregno) {
        	final Map map = new TreeMap();
            try {
                PreparedStatement ps = (PreparedStatement) param._psMap.get("ATTEND_SEMES_MONTH");

                final Integer zero = new Integer(0);

                for (final Iterator it = KnjDbUtils.query(db2, ps, new Object[] {schregno}).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    final String month = KnjDbUtils.getString(row, "MONTH");

                    if (null == map.get(month)) {
                    	map.put(month, new AttendSemesDat(month));
                    }
                    final AttendSemesDat semesMonth = (AttendSemesDat) map.get(month);

                    semesMonth._lesson += KnjDbUtils.getInt(row, "LESSON", zero).intValue();
                    semesMonth._suspendMourning += KnjDbUtils.getInt(row, "SUSP_MOURNING", zero).intValue();
                    semesMonth._mlesson += KnjDbUtils.getInt(row, "MLESSON", zero).intValue();
                    semesMonth._sickOnly += KnjDbUtils.getInt(row, "SICK_ONLY", zero).intValue();
                    semesMonth._noticeOnly += KnjDbUtils.getInt(row, "NOTICE_ONLY", zero).intValue();
                    semesMonth._nonoticeOnly += KnjDbUtils.getInt(row, "NONOTICE_ONLY", zero).intValue();
                    semesMonth._present += KnjDbUtils.getInt(row, "PRESENT", zero).intValue();
                    final String remark = KnjDbUtils.getString(row, "REMARK1");
					if (null == semesMonth._remark) {
                    	semesMonth._remark = remark;
                    } else if (null != remark) {
                    	semesMonth._remark += " " + remark;
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return map;
        }
    }

    private class PrintData {
        final Map _div = new HashMap();
        final Map _remarkMap = new HashMap();
        boolean _diffTangen = false;
        boolean _subclassnameExtends = false;

        public PrintData() {
            //this(null);
        }

        public PrintData(final PrintData src) {
            if (null != src) {
                setDataDiv(divGrp, src.div(divGrp));
                setDataDiv(divSubclasscd, src.div(divSubclasscd));
                setDataDiv(divNaiyogrp, src.div(divNaiyogrp));
                setDataDiv(divUnitgrp, src.div(divUnitgrp));
            }
        }

        private void setDataDiv(final String key, final String val) {
            _div.put(key, val);
        }

        private String div(final String key) {
            return (String) _div.get(key);
        }

        private String remark(final String key) {
            return (String) _remarkMap.get(key);
        }

        private Map forDebug(final Map m) {
            final Map nm = new HashMap();
            for (final Iterator it = m.keySet().iterator(); it.hasNext();) {
                final Object key = it.next();
                if (m.get(key) == null) { // 値がnullのペアをカット
                    continue;
                }
                nm.put(key, m.get(key));
            }
            return nm;
        }

        public String toString() {
            return "PrintData(" + div(kbn) + "," + div(kyouka) + ", " + ArrayUtils.toString(forDebug(_remarkMap)) + ")";
        }
    }

    private static class ClassRemark1 {
        final String _classRemark1;
        final String _name;
        final List _gradeKindSubclassList;
        ClassRemark1(final String classRemark1, final String name) {
            _classRemark1 = classRemark1;
            _name = name;
            _gradeKindSubclassList = new ArrayList();
        }

        private static ClassRemark1 getClassRemark1(final String classRemark1, final List list) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final ClassRemark1 cr1 = (ClassRemark1) it.next();
                if (cr1._classRemark1.equals(classRemark1)) {
                    return cr1;
                }
            }
            return null;
        }
    }

    private static class GradeKind {
        final String _gakubuSchoolKind;
        final String _ghrCd;
        final String _grade;
        final String _hrClass;
        final String _condition;
        final String _groupcd;
        final List _classRemark1list;
        String _groupname;
        String _guidancePattern;
        String _tyohyoPattern;
        String _impptDatRemark1;
        String _impptDatRemark2;
        String _impptDatRemark3;

        GradeKind(
            final String gakubuSchoolKind,
            final String ghrCd,
            final String grade,
            final String hrClass,
            final String condition,
            final String groupcd
        ) {
            _gakubuSchoolKind = gakubuSchoolKind;
            _ghrCd = ghrCd;
            _grade = grade;
            _hrClass = hrClass;
            _condition = condition;
            _groupcd = groupcd;
            _classRemark1list = new ArrayList();
        }

        public static GradeKind load(final DB2UDB db2, final String year, final String semester, final String schregno) {
            GradeKind rtn = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   T1.YEAR, ");
                stb.append("   T1.GAKUBU_SCHOOL_KIND, ");
                stb.append("   T1.GHR_CD, ");
                stb.append("   T1.GRADE, ");
                stb.append("   T1.HR_CLASS, ");
                stb.append("   T1.CONDITION, ");
                stb.append("   T1.GROUPCD, ");
                stb.append("   T1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("  GRADE_KIND_SCHREG_GROUP_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("  T1.YEAR = '" + year + "' ");
                stb.append("  AND T1.SEMESTER = '" + semester + "' ");
                stb.append("  AND T1.SCHREGNO = '" + schregno + "' ");

                for (final Iterator it = KnjDbUtils.query(db2, stb.toString()).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    final String gakubuSchoolKind = KnjDbUtils.getString(row, "GAKUBU_SCHOOL_KIND");
                    final String ghrCd = KnjDbUtils.getString(row, "GHR_CD");
                    final String grade = KnjDbUtils.getString(row, "GRADE");
                    final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                    final String condition = KnjDbUtils.getString(row, "CONDITION");
                    final String groupcd = KnjDbUtils.getString(row, "GROUPCD");
                    rtn = new GradeKind(gakubuSchoolKind, ghrCd, grade, hrClass, condition, groupcd);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            }
            return rtn;
        }
    }

    private static class GradeKindSubclass {
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final String _unitAimDiv;
        int _remarkCount = -1;
        int _classgrp;
        int _subgrp;

        final Map _remarkMap = new HashMap(); // unitAimDiv=1のみ使用する

        final List _unitRemarkList;

        GradeKindSubclass(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String subclassname,
                final String unitAimDiv
        ) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _unitAimDiv = unitAimDiv;
            _unitRemarkList = new ArrayList();
        }

        private String remark(final String key) {
            return (String) _remarkMap.get(key);
        }

        private static void setGradeKindSubclass(final DB2UDB db2, final Param param, final Student student) {
            if (null == student._gradeKind) {
                return;
            }

            final GradeKind gradeKind = student._gradeKind;
            try {
                final StringBuffer stb = new StringBuffer();

                final String[] classDivOrder = null == param._categorySelected2 ? new String[] { "0", "1", "2", "3"} : param._categorySelected2;

                stb.append(" WITH CLASS_REMARKS_DIV(DIV, ORDER) AS ( ");
                String union = "";
                for (int i = 0; i < classDivOrder.length; i++) {
                    stb.append(union);
                    stb.append("VALUES('" + classDivOrder[i] + "', " + String.valueOf(i) + ")");
                    union = " UNION ALL ";
                }
                stb.append(" ), ALL AS ( ");
                stb.append(" SELECT ");
                stb.append("   CGM.GAKUBU_SCHOOL_KIND, ");
                stb.append("   CGM.GHR_CD, ");
                stb.append("   CGM.GRADE, ");
                stb.append("   CGM.HR_CLASS, ");
                stb.append("   CGM.CONDITION, ");
                stb.append("   CGM.GROUPCD, ");
                stb.append("   CGM.GROUPNAME, ");
                stb.append("   CGM.GUIDANCE_PATTERN, ");
                stb.append("   CGD.YEAR, ");
                stb.append("   VALUE(L2.CLASS_REMARK1, '0') AS CLASS_REMARK1,  ");
                stb.append("   CGD.CLASSCD || '-' || CGD.SCHOOL_KIND AS CLASSCD, ");
                stb.append("   CGD.CLASSCD || '-' || CGD.SCHOOL_KIND || '-' || CGD.CURRICULUM_CD || '-' || CGD.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("  CGD.UNIT_AIM_DIV, ");
                stb.append("  CLM.CLASSNAME, ");
                stb.append("  VALUE(SUBM.SUBCLASSORDERNAME2, SUBM.SUBCLASSNAME) AS SUBCLASSNAME, ");
                stb.append("  VALUE(UNI.UNITCD, '00') AS UNITCD, ");
                stb.append("  VALUE(SUNI.UNITNAME, UNI.UNITNAME) AS UNITNAME, ");
                stb.append("  VALUE(CLM.SHOWORDER3, 9999) AS CLASS_SHOWORDER, ");
                stb.append("  VALUE(SUBM.SHOWORDER3, 9999) AS SUBCLASS_SHOWORDER ");
                stb.append(" FROM ");
                stb.append("  GRADE_KIND_COMP_GROUP_YMST CGM ");
                stb.append("  INNER JOIN GRADE_KIND_COMP_GROUP_DAT CGD ON CGD.YEAR = CGM.YEAR ");
                stb.append("   AND CGD.SEMESTER = CGM.SEMESTER ");
                stb.append("   AND CGD.GAKUBU_SCHOOL_KIND = CGM.GAKUBU_SCHOOL_KIND ");
                stb.append("   AND CGD.GHR_CD = CGM.GHR_CD ");
                stb.append("   AND CGD.GRADE = CGM.GRADE ");
                stb.append("   AND CGD.HR_CLASS = CGM.HR_CLASS ");
                stb.append("   AND CGD.CONDITION = CGM.CONDITION ");
                stb.append("   AND CGD.GROUPCD = CGM.GROUPCD ");
                stb.append("  LEFT JOIN CLASS_MST CLM ON CLM.CLASSCD = CGD.CLASSCD ");
                stb.append("   AND CLM.SCHOOL_KIND = CGD.SCHOOL_KIND ");
                stb.append("  LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = CGD.CLASSCD ");
                stb.append("   AND SUBM.SCHOOL_KIND = CGD.SCHOOL_KIND ");
                stb.append("   AND SUBM.CURRICULUM_CD = CGD.CURRICULUM_CD ");
                stb.append("   AND SUBM.SUBCLASSCD = CGD.SUBCLASSCD ");
                stb.append("  LEFT JOIN CLASS_DETAIL_MST L2 ON CGD.CLASSCD = L2.CLASSCD  ");
                stb.append("   AND CGD.SCHOOL_KIND = L2.SCHOOL_KIND  ");
                stb.append("   AND L2.CLASS_SEQ = '001'  ");
                stb.append("  LEFT JOIN GRADE_KIND_UNIT_GROUP_YMST UNI ON UNI.YEAR = CGD.YEAR ");
                stb.append("   AND UNI.SEMESTER = CGD.SEMESTER ");
                stb.append("   AND UNI.GAKUBU_SCHOOL_KIND = CGD.GAKUBU_SCHOOL_KIND ");
                stb.append("   AND UNI.GHR_CD = CGD.GHR_CD ");
                stb.append("   AND UNI.GRADE = CGD.GRADE ");
                stb.append("   AND UNI.HR_CLASS = CGD.HR_CLASS ");
                stb.append("   AND UNI.CONDITION = CGD.CONDITION ");
                stb.append("   AND UNI.GROUPCD = CGD.GROUPCD ");
                stb.append("   AND UNI.CLASSCD = CGD.CLASSCD ");
                stb.append("   AND UNI.SCHOOL_KIND = CGD.SCHOOL_KIND ");
                stb.append("   AND UNI.CURRICULUM_CD = CGD.CURRICULUM_CD ");
                stb.append("   AND UNI.SUBCLASSCD = CGD.SUBCLASSCD ");
                stb.append("  LEFT JOIN GRADE_KIND_SCHREG_UNIT_DAT SUNI ON SUNI.YEAR = CGD.YEAR ");
                stb.append("   AND SUNI.SEMESTER = CGD.SEMESTER ");
                stb.append("   AND SUNI.SCHREGNO = '" + StringUtils.defaultString(student._schregno) + "' ");
                stb.append("   AND SUNI.CLASSCD = CGD.CLASSCD ");
                stb.append("   AND SUNI.SCHOOL_KIND = CGD.SCHOOL_KIND ");
                stb.append("   AND SUNI.CURRICULUM_CD = CGD.CURRICULUM_CD ");
                stb.append("   AND SUNI.SUBCLASSCD = CGD.SUBCLASSCD ");
                stb.append("   AND SUNI.UNITCD = UNI.UNITCD ");
                stb.append(" WHERE ");
                stb.append("   CGM.YEAR = '" + param._ctrlYear + "' ");
                if ("1".equals(param._useGradeKindCompGroupSemester)) {
                    stb.append("   AND CGM.SEMESTER = '" + StringUtils.defaultString(param._d078, param._semester) + "' ");
                } else {
                    stb.append("   AND CGM.SEMESTER = '9' ");
                }
                stb.append("   AND CGM.GAKUBU_SCHOOL_KIND = '" + gradeKind._gakubuSchoolKind + "' ");
                stb.append("   AND CGM.GHR_CD = '" + gradeKind._ghrCd + "' ");
                stb.append("   AND CGM.GRADE = '" + gradeKind._grade + "' ");
                stb.append("   AND CGM.HR_CLASS = '" + gradeKind._hrClass + "' ");
                stb.append("   AND CGM.CONDITION = '" + gradeKind._condition + "' ");
                stb.append("   AND CGM.GROUPCD = '" + gradeKind._groupcd + "' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("   T1.* ");
                stb.append(" FROM ALL T1 ");
                stb.append("  INNER JOIN CLASS_REMARKS_DIV CDIV ON CDIV.DIV = T1.CLASS_REMARK1 ");
                stb.append(" ORDER BY ");
                stb.append("   CDIV.ORDER, ");
                stb.append("   T1.CLASS_REMARK1,  ");
                stb.append("   T1.CLASS_SHOWORDER, ");
                stb.append("   T1.CLASSCD, ");
                stb.append("   T1.SUBCLASS_SHOWORDER, ");
                stb.append("   T1.SUBCLASSCD, ");
                stb.append("   T1.UNITCD ");

                final String sqlSubclass = stb.toString();
                if (param._isOutputDebug) {
                	log.info(" sqlSubclass = " + sqlSubclass);
                }
                for (final Iterator it = KnjDbUtils.query(db2, sqlSubclass).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    gradeKind._groupname = KnjDbUtils.getString(row, "GROUPNAME");
                    gradeKind._guidancePattern = KnjDbUtils.getString(row, "GUIDANCE_PATTERN");
                    gradeKind._tyohyoPattern = gradeKind._guidancePattern;
                    if (PATTERN7.equals(gradeKind._guidancePattern) && "1".equals(param._outputDiv)) {
                        gradeKind._tyohyoPattern = PATTERN7_2;
                    }

                    final String classRemark1 = KnjDbUtils.getString(row, "CLASS_REMARK1");
                    if (null == ClassRemark1.getClassRemark1(classRemark1, gradeKind._classRemark1list)) {
                        final ClassRemark1 cr1 = new ClassRemark1(classRemark1, param.getClassRemark1Name(classRemark1));
                        gradeKind._classRemark1list.add(cr1);
                    }
                    final ClassRemark1 cr1 = ClassRemark1.getClassRemark1(classRemark1, gradeKind._classRemark1list);
                    //log.debug(" cr1 = " + cr1._classRemark1 + ", " + cr1._name);

                    final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");

                    if (null == subclasscd) {
                        continue;
                    }
                    if (null == GradeKindSubclass.getGradeKindSubclass(subclasscd, cr1._gradeKindSubclassList)) {
                        final String classname = KnjDbUtils.getString(row, "CLASSNAME");
                        final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                        final String unitAimDiv = KnjDbUtils.getString(row, "UNIT_AIM_DIV");
                        cr1._gradeKindSubclassList.add(new GradeKindSubclass(classcd, classname, subclasscd, subclassname, unitAimDiv));
                    }
                    final GradeKindSubclass gradekindsubclass = GradeKindSubclass.getGradeKindSubclass(subclasscd, cr1._gradeKindSubclassList);

                    final String unitcd = KnjDbUtils.getString(row, "UNITCD");
                    final String unitname = KnjDbUtils.getString(row, "UNITNAME");
                    if (param._isOutputDebug) {
                    	log.info(" subclasscd = " + subclasscd + ", unitcd = " + unitcd + " (unitAimDiv = " + gradekindsubclass._unitAimDiv + ")");
                    }
                    final UnitRemark uremark = new UnitRemark(unitcd, unitname);
                    gradekindsubclass._unitRemarkList.add(uremark);
                }

                int classgrp = 0;
                int subgrp = 0;
                String oldclasscd = null;
                for (final Iterator crit = gradeKind._classRemark1list.iterator(); crit.hasNext();) {
                    final ClassRemark1 cr1 = (ClassRemark1) crit.next();
                    for (final Iterator sit = cr1._gradeKindSubclassList.iterator(); sit.hasNext();) {
                        final GradeKindSubclass gradekindsubclass = (GradeKindSubclass) sit.next();
                        if (null != oldclasscd && !oldclasscd.equals(gradekindsubclass._classcd)) {
                            classgrp = (classgrp + 1) % 100;
                        }
                        subgrp = (subgrp + 1) % 100;
                        gradekindsubclass._classgrp = classgrp;
                        gradekindsubclass._subgrp = subgrp;
                        oldclasscd = gradekindsubclass._classcd;
                    }
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            }

            // KNJD419
            if (PRGID_KNJD419.equals(param._prgId)) {
                for (final Iterator cit = gradeKind._classRemark1list.iterator(); cit.hasNext();) {
                    final ClassRemark1 cr1 = (ClassRemark1) cit.next();

                    //log.info(" [" + gradeKind._gakubuSchoolKind + "-" + gradeKind._condition + "-" + gradeKind._groupcd + "]");

                    for (final Iterator git = cr1._gradeKindSubclassList.iterator(); git.hasNext();) {
                        final GradeKindSubclass gks = (GradeKindSubclass) git.next();

                        final String[] split = StringUtils.split(gks._subclasscd, "-");
                        final String classcd = split[0];
                        final String schoolKind = split[1];
                        final String curriculumCd = split[2];
                        final String subclasscd = split[3];

                        for (final Iterator uit = gks._unitRemarkList.iterator(); uit.hasNext();) {
                            final UnitRemark uremark = (UnitRemark) uit.next();

                            final StringBuffer stb = new StringBuffer();
                            stb.append(" SELECT ");
                            stb.append("  GROUP_REMARK1 ");
                            stb.append(" ,GROUP_REMARK2 ");
                            stb.append(" FROM ");
                            stb.append("  HREPORT_GUIDANCE_GROUP_UNIT_DAT ");
                            stb.append(" WHERE ");
                            stb.append("   YEAR = '" + param._ctrlYear + "' ");
                            stb.append("   AND SEMESTER = '" + StringUtils.defaultString(param._d078, param._semester) + "' ");
                            stb.append("   AND GAKUBU_SCHOOL_KIND = '" + gradeKind._gakubuSchoolKind + "' ");
                            stb.append("   AND GHR_CD = '" + gradeKind._ghrCd + "' ");
                            stb.append("   AND GRADE = '" + gradeKind._grade + "' ");
                            stb.append("   AND HR_CLASS = '" + gradeKind._hrClass + "' ");
                            stb.append("   AND CONDITION = '" + gradeKind._condition + "' ");
                            stb.append("   AND GROUPCD = '" + gradeKind._groupcd + "' ");
                            stb.append("   AND CLASSCD = '" + classcd + "' ");
                            stb.append("   AND SCHOOL_KIND = '" + schoolKind + "' ");
                            stb.append("   AND CURRICULUM_CD = '" + curriculumCd + "' ");
                            stb.append("   AND SUBCLASSCD = '" + subclasscd + "' ");
                            stb.append("   AND UNITCD = '" + uremark._unitcd + "' ");

                            final Map row1 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString()));

                            if (!row1.isEmpty()) {
                                uremark._groupRemark1 = KnjDbUtils.getString(row1, GROUP_REMARK + "1");
                                uremark._groupRemark2 = KnjDbUtils.getString(row1, GROUP_REMARK + "2");
                            }
                            //log.info("  " + classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd + ", " + unitcd + " ");
                        }

                        final StringBuffer stb = new StringBuffer();
                        stb.append(" SELECT ");
                        stb.append("  SEQ, ");
                        stb.append("  GROUP_REMARK ");
                        stb.append(" FROM ");
                        stb.append("  HREPORT_GUIDANCE_GROUP_DAT ");
                        stb.append(" WHERE ");
                        stb.append("   YEAR = '" + param._ctrlYear + "' ");
                        stb.append("   AND SEMESTER = '" + StringUtils.defaultString(param._d078, param._semester) + "' ");
                        stb.append("   AND GAKUBU_SCHOOL_KIND = '" + gradeKind._gakubuSchoolKind + "' ");
                        stb.append("   AND GHR_CD = '" + gradeKind._ghrCd + "' ");
                        stb.append("   AND GRADE = '" + gradeKind._grade + "' ");
                        stb.append("   AND HR_CLASS = '" + gradeKind._hrClass + "' ");
                        stb.append("   AND CONDITION = '" + gradeKind._condition + "' ");
                        stb.append("   AND GROUPCD = '" + gradeKind._groupcd + "' ");
                        stb.append("   AND CLASSCD = '" + classcd + "' ");
                        stb.append("   AND SCHOOL_KIND = '" + schoolKind + "' ");
                        stb.append("   AND CURRICULUM_CD = '" + curriculumCd + "' ");
                        stb.append("   AND SUBCLASSCD = '" + subclasscd + "' ");

                        for (final Iterator it = KnjDbUtils.query(db2, stb.toString()).iterator(); it.hasNext();) {
                        	final Map row = (Map) it.next();
                            final String seq = KnjDbUtils.getString(row, "SEQ");
                            if ("51".equals(seq)) {
                                gks._remarkMap.put(PROCEDURE, KnjDbUtils.getString(row, GROUP_REMARK));
                            } else {
                                gks._remarkMap.put(GROUP_REMARK + seq, KnjDbUtils.getString(row, GROUP_REMARK));
                            }
                            //log.info("   >> "  + ArrayUtils.toString(gks._groupRemark));
                        }

                        final StringBuffer stb3 = new StringBuffer();
                        stb3.append(" SELECT ");
                        stb3.append("  GROUP_YEAR_TARGET ");
                        stb3.append(" FROM ");
                        stb3.append("  HREPORT_GUIDANCE_GROUP_YDAT HREPG  ");
                        stb3.append(" WHERE ");
                        stb3.append("   YEAR = '" + param._ctrlYear + "' ");
                        stb3.append("   AND GAKUBU_SCHOOL_KIND = '" + gradeKind._gakubuSchoolKind + "' ");
                        stb3.append("   AND GHR_CD = '" + gradeKind._ghrCd + "' ");
                        stb3.append("   AND GRADE = '" + gradeKind._grade + "' ");
                        stb3.append("   AND HR_CLASS = '" + gradeKind._hrClass + "' ");
                        stb3.append("   AND CONDITION = '" + gradeKind._condition + "' ");
                        stb3.append("   AND GROUPCD = '" + gradeKind._groupcd + "' ");
                        stb3.append("   AND CLASSCD = '" + classcd + "' ");
                        stb3.append("   AND SCHOOL_KIND = '" + schoolKind + "' ");
                        stb3.append("   AND CURRICULUM_CD = '" + curriculumCd + "' ");
                        stb3.append("   AND SUBCLASSCD = '" + subclasscd + "' ");

                        final Map row3 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb3.toString()));

                        if (!row3.isEmpty()) {
                            gks._remarkMap.put(YEAR_TARGET, KnjDbUtils.getString(row3, "GROUP_YEAR_TARGET"));
                        }

                        if (param._isOutputDebug) {
                        	log.info(" remarkMap = " + gks._remarkMap);
                        }
                    }
                }
            }

            // 所見の数（Unit以外）
            for (final Iterator cit = gradeKind._classRemark1list.iterator(); cit.hasNext();) {
                final ClassRemark1 cr1 = (ClassRemark1) cit.next();

                for (final Iterator git = cr1._gradeKindSubclassList.iterator(); git.hasNext();) {
                    final GradeKindSubclass gks = (GradeKindSubclass) git.next();

                    final String[] split = StringUtils.split(gks._subclasscd, "-");
                    final String classcd = split[0];
                    final String schoolKind = split[1];
                    final String curriculumCd = split[2];
                    final String subclasscd = split[3];

                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT ");
                    stb.append("  GROUP_REMARK_CNT ");
                    stb.append(" FROM ");
                    stb.append("  HREPORT_GUIDANCE_GROUP_HDAT HREPG  ");
                    stb.append(" WHERE ");
                    stb.append("   YEAR = '" + param._ctrlYear + "' ");
                    stb.append("   AND SEMESTER = '" + StringUtils.defaultString(param._d078, param._semester) + "' ");
                    stb.append("   AND GAKUBU_SCHOOL_KIND = '" + gradeKind._gakubuSchoolKind + "' ");
                    stb.append("   AND GHR_CD = '" + gradeKind._ghrCd + "' ");
                    stb.append("   AND GRADE = '" + gradeKind._grade + "' ");
                    stb.append("   AND HR_CLASS = '" + gradeKind._hrClass + "' ");
                    stb.append("   AND CONDITION = '" + gradeKind._condition + "' ");
                    stb.append("   AND GROUPCD = '" + gradeKind._groupcd + "' ");
                    stb.append("   AND CLASSCD = '" + classcd + "' ");
                    stb.append("   AND SCHOOL_KIND = '" + schoolKind + "' ");
                    stb.append("   AND CURRICULUM_CD = '" + curriculumCd + "' ");
                    stb.append("   AND SUBCLASSCD = '" + subclasscd + "' ");

                    final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString()));

                    if (!row.isEmpty()) {
                        if (NumberUtils.isDigits(KnjDbUtils.getString(row, "GROUP_REMARK_CNT"))) {
                            gks._remarkCount = Integer.parseInt(KnjDbUtils.getString(row, "GROUP_REMARK_CNT"));
                        }
                        //log.info("   >> "  + ArrayUtils.toString(gks._groupRemark));
                    }
                }
            }

            if (!StringUtils.isBlank(student._schregno)) {

                final StringBuffer stb0 = new StringBuffer();
                stb0.append(" SELECT ");
                stb0.append("  REMARK1, ");
                stb0.append("  REMARK2, ");
                stb0.append("  REMARK3 ");
                stb0.append(" FROM ");
                stb0.append("  HREPORT_GUIDANCE_SCHREG_IMPPT_DAT ");
                stb0.append(" WHERE ");
                stb0.append("   YEAR = '" + param._ctrlYear + "' ");
                stb0.append("   AND SEMESTER = '" + StringUtils.defaultString(param._d078, param._semester) + "' ");
                stb0.append("   AND SCHREGNO = '" + student._schregno + "' ");

                final Map row0 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb0.toString()));

                if (!row0.isEmpty()) {
                	gradeKind._impptDatRemark1 = KnjDbUtils.getString(row0, "REMARK1");
                	gradeKind._impptDatRemark2 = KnjDbUtils.getString(row0, "REMARK2");
                	gradeKind._impptDatRemark3 = KnjDbUtils.getString(row0, "REMARK3");
                }

                for (final Iterator cit = gradeKind._classRemark1list.iterator(); cit.hasNext();) {
                    final ClassRemark1 cr1 = (ClassRemark1) cit.next();

                    for (final Iterator git = cr1._gradeKindSubclassList.iterator(); git.hasNext();) {
                        final GradeKindSubclass gks = (GradeKindSubclass) git.next();

                        final String[] split = StringUtils.split(gks._subclasscd, "-");
                        final String classcd = split[0];
                        final String schoolKind = split[1];
                        final String curriculumCd = split[2];
                        final String subclasscd = split[3];

                        for (final Iterator uit = gks._unitRemarkList.iterator(); uit.hasNext();) {
                            final UnitRemark uremark = (UnitRemark) uit.next();

                            final StringBuffer stb = new StringBuffer();
                            stb.append(" SELECT ");
                            stb.append("  REMARK1, ");
                            stb.append("  REMARK_VALUE1, ");
                            stb.append("  REMARK2 ");
                            stb.append(" FROM ");
                            stb.append("  HREPORT_GUIDANCE_SCHREG_UNIT_DAT ");
                            stb.append(" WHERE ");
                            stb.append("   YEAR = '" + param._ctrlYear + "' ");
                            stb.append("   AND SEMESTER = '" + StringUtils.defaultString(param._d078, param._semester) + "' ");
                            stb.append("   AND SCHREGNO = '" + student._schregno + "' ");
                            stb.append("   AND CLASSCD = '" + classcd + "' ");
                            stb.append("   AND SCHOOL_KIND = '" + schoolKind + "' ");
                            stb.append("   AND CURRICULUM_CD = '" + curriculumCd + "' ");
                            stb.append("   AND SUBCLASSCD = '" + subclasscd + "' ");
                            stb.append("   AND UNITCD = '" + uremark._unitcd + "' ");

                            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString()));

                            if (!row.isEmpty()) {
                                uremark._remark1 = KnjDbUtils.getString(row, REMARK + "1");
                                uremark._remarkValue1 = KnjDbUtils.getString(row, REMARK_VALUE + "1");
                                uremark._remark2 = KnjDbUtils.getString(row, REMARK + "2");
                                final String assessMark1;
                                if (PATTERN_A.equals(gradeKind._tyohyoPattern)) {
                                	assessMark1 = param.getMaruSuuji(uremark._remarkValue1);
                                } else {
                                	assessMark1 = param.getAssessMark(student, gradeKind, uremark._remarkValue1);
                                }
                                uremark._assessMark1 = assessMark1;
                            }

                            final StringBuffer sql2 = new StringBuffer();
                            sql2.append(" SELECT ");
                            sql2.append("  SEQ, ");
                            sql2.append("  REMARK ");
                            sql2.append(" FROM ");
                            sql2.append("  HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT ");
                            sql2.append(" WHERE ");
                            sql2.append("   YEAR = '" + param._ctrlYear + "' ");
                            sql2.append("   AND SEMESTER = '" + StringUtils.defaultString(param._d078, param._semester) + "' ");
                            sql2.append("   AND SCHREGNO = '" + student._schregno + "' ");
                            sql2.append("   AND CLASSCD = '" + classcd + "' ");
                            sql2.append("   AND SCHOOL_KIND = '" + schoolKind + "' ");
                            sql2.append("   AND CURRICULUM_CD = '" + curriculumCd + "' ");
                            sql2.append("   AND SUBCLASSCD = '" + subclasscd + "' ");
                            sql2.append("   AND UNITCD = '" + uremark._unitcd + "' ");
                            final Map row2 = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql2.toString()), "SEQ", "REMARK");
                            uremark._remark71 = (String) row2.get("71");
                            uremark._remark72 = (String) row2.get("72");
                            uremark._remark73 = (String) row2.get("73");

                            if (param._isOutputDebug) {
                            	log.info(" assessMark = " + uremark._assessMark1 + " | schregno = " + student._schregno + ", subclasscd = " + subclasscd + ", unitcd = " + uremark._unitcd);
                            }

                        }

                        // 生徒ごとの所見
                        final StringBuffer sql2 = new StringBuffer();
                        sql2.append(" SELECT ");
                        sql2.append("  SEQ, ");
                        sql2.append("  REMARK, ");
                        sql2.append("  REMARK_VALUE ");
                        sql2.append(" FROM ");
                        sql2.append("  HREPORT_GUIDANCE_SCHREG_DAT ");
                        sql2.append(" WHERE ");
                        sql2.append("   YEAR = '" + param._ctrlYear + "' ");
                        sql2.append("   AND SEMESTER = '" + StringUtils.defaultString(param._d078, param._semester) + "' ");
                        sql2.append("   AND SCHREGNO = '" + student._schregno + "' ");
                        sql2.append("   AND CLASSCD = '" + classcd + "' ");
                        sql2.append("   AND SCHOOL_KIND = '" + schoolKind + "' ");
                        sql2.append("   AND CURRICULUM_CD = '" + curriculumCd + "' ");
                        sql2.append("   AND SUBCLASSCD = '" + subclasscd + "' ");

                        for (final Iterator it = KnjDbUtils.query(db2, sql2.toString()).iterator(); it.hasNext();) {
                        	final Map row = (Map) it.next();
                            final String seq = KnjDbUtils.getString(row, "SEQ");
                            if ("51".equals(seq)) {
                                gks._remarkMap.put(PROCEDURE, KnjDbUtils.getString(row, REMARK));
                            } else if ("52".equals(seq)) {
                                gks._remarkMap.put(VALUE_TEXT, KnjDbUtils.getString(row, REMARK));
                            } else if ("53".equals(seq)) {
                                gks._remarkMap.put(VALUE_TEXT2, KnjDbUtils.getString(row, REMARK));
                            } else if ("54".equals(seq)) {
                                //gks._remarkMap.put(VALUE, KnjDbUtils.getString(row, REMARK));
                            } else { // 1~10
                                gks._remarkMap.put(REMARK + seq, KnjDbUtils.getString(row, REMARK));
                                final String remarkValueN = KnjDbUtils.getString(row, REMARK_VALUE);
                                gks._remarkMap.put(REMARK_VALUE + seq, remarkValueN);
                                final String assessMarkSeq;
                                if (PATTERN_A.equals(gradeKind._tyohyoPattern)) {
                                	assessMarkSeq = param.getMaruSuuji(remarkValueN);
                                } else {
                                	assessMarkSeq = param.getAssessMark(student, gradeKind, remarkValueN);
                                }
                                gks._remarkMap.put(ASSESS_MARK + seq, assessMarkSeq);
                            }
                        }

                        // 評定
                        final StringBuffer sql3 = new StringBuffer();
                        sql3.append(" SELECT ");
                        sql3.append("   VALUE ");
                        sql3.append(" FROM ");
                        sql3.append("   RECORD_SCORE_DAT ");
                        sql3.append(" WHERE ");
                        sql3.append("   YEAR = '" + param._ctrlYear + "' AND SEMESTER = '9' AND TESTKINDCD = '99' AND TESTITEMCD = '00' AND SCORE_DIV = '00' "); // ??
                        sql3.append("   AND CLASSCD = '" + classcd + "' ");
                        sql3.append("   AND SCHOOL_KIND = '" + schoolKind + "' ");
                        sql3.append("   AND CURRICULUM_CD = '" + curriculumCd + "' ");
                        sql3.append("   AND SUBCLASSCD = '" + subclasscd + "' ");
                        sql3.append("   AND SCHREGNO = '" + student._schregno + "' ");

                        final Map row3 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql3.toString()));

                        if (!row3.isEmpty()) {
                            gks._remarkMap.put(VALUE, KnjDbUtils.getString(row3, "VALUE"));
                        }

                        // 生徒の年度に1回の所見
                        final StringBuffer stb4 = new StringBuffer();
                        stb4.append(" SELECT ");
                        stb4.append("  YEAR_TARGET ");
                        stb4.append(" FROM ");
                        stb4.append("  HREPORT_GUIDANCE_SCHREG_YDAT  ");
                        stb4.append(" WHERE ");
                        stb4.append("   YEAR = '" + param._ctrlYear + "' ");
                        stb4.append("   AND SCHREGNO = '" + student._schregno + "' ");
                        stb4.append("   AND CLASSCD = '" + classcd + "' ");
                        stb4.append("   AND SCHOOL_KIND = '" + schoolKind + "' ");
                        stb4.append("   AND CURRICULUM_CD = '" + curriculumCd + "' ");
                        stb4.append("   AND SUBCLASSCD = '" + subclasscd + "' ");

                        final Map row4 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb4.toString()));

                        if (!row4.isEmpty()) {
                        	gks._remarkMap.put(YEAR_TARGET, KnjDbUtils.getString(row4, "YEAR_TARGET"));
                        }

                        if (param._isOutputDebug) {
                        	log.info(" remarkMap = " + gks._remarkMap);
                        }
                    }
                }
            }
        }

        private static GradeKindSubclass getGradeKindSubclass(final String subclasscd, final List list) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final GradeKindSubclass gks = (GradeKindSubclass) it.next();
                if (gks._subclasscd.equals(subclasscd)) {
                    return gks;
                }
            }
            return null;
        }

        private static class UnitRemark {
            final String _unitcd;
            final String _unitname;
            String _remark1;
            String _remarkValue1;
            String _remark2;
            String _assessMark1;
            String _groupRemark1;
            String _groupRemark2;
            String _remark71;
            String _remark72;
            String _remark73;

            UnitRemark(
                final String unitcd,
                final String unitname
            ) {
                _unitcd = unitcd;
                _unitname = unitname;
            }
        }
    }
    
    private static String getZenkaku(final String s) {
    	if (null == s) {
    		return s;
    	}
    	final StringBuffer stb = new StringBuffer();
    	for (int i = 0; i < s.length(); i++) {
    		final char ch = s.charAt(i);
    		if ('0' <= ch && '9' <= ch) {
    			final char zen = (char) (ch - '0' + '０');
    			stb.append(zen);
    		} else {
    			stb.append(ch);
    		}
    	}
    	return stb.toString();
    }

    private static int getMS932ByteCount(final String str) {
    	return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private static List getTokenList(final String text, final ShokenSize size) {
        return KNJ_EditKinsoku.getTokenList(text, size._mojisu * 2, size._gyo);
    }

    private static List getTokenList(final String text, final int ketamax) {
        return KNJ_EditKinsoku.getTokenList(text, ketamax);
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static class ShokenSize {
        int _mojisu;
        int _gyo;

        ShokenSize(final int mojisu, final int gyo) {
            _mojisu = mojisu;
            _gyo = gyo;
        }

        private static ShokenSize getSize(final String paramString, final int mojisuDefault, final int gyoDefault) {
            final int mojisu = ShokenSize.getParamSizeNum(paramString, 0);
            final int gyo = ShokenSize.getParamSizeNum(paramString, 1);
            if (-1 == mojisu || -1 == gyo) {
                return new ShokenSize(mojisuDefault, gyoDefault);
            }
            return new ShokenSize(mojisu, gyo);
        }

        /**
         * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
         * @param param サイズタイプのパラメータ文字列
         * @param pos split後のインデクス (0:w, 1:h)
         * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
         */
        private static int getParamSizeNum(final String param, final int pos) {
            int num = -1;
            if (StringUtils.isBlank(param)) {
                return num;
            }
            final String[] nums = StringUtils.split(StringUtils.replace(param, "+", " "), " * ");
            if (StringUtils.isBlank(param) || !(0 <= pos && pos < nums.length)) {
                num = -1;
            } else {
                try {
                    num = Integer.valueOf(nums[pos]).intValue();
                } catch (Exception e) {
                    log.error("Exception!", e);
                }
            }
            return num;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 62600 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {

        final String _ctrlYear;
        final String _semester;
        final boolean _isLastSemester;
        final boolean _is3Gakkisei;
        final String[] _categorySelected;
        private String[] _categorySelected2;
        private String _categoryGhrCd;
        final String _prgId;
        private String _selectGhr;
        private String _outputCategoryName2;
        private String _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE;
        private String _reportSpecialSize05_01;
        private String _HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE;
        private String _reportSpecialSize05_02;
        private String _HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE;
        private String _gakubuSchoolKind;
        private String _ghrCd;
        private String _grade;
        private String _hrClass;
        private String _groupcd;
        private String _condition;
        private String _guidancePattern;
        private String _printSide1;
        private String _printSide1Attend;
        private String _printSide2;
        final String _d078;
        private String _printSide3;
        private String _printSide4;
        private String _outputDiv;
        final String _useGradeKindCompGroupSemester;

        final String _nendo;
        final String _z010Name1;
        final Map _d78PrintGakkiSemesterNameMap;
        final Map _d78PrintGakkiMonthCsvMap;
        final boolean _isFukuiken;

        final Map _semesterNameMap;
        final Map _schoolNameMap;
        final Map _gradekindAssessMap;
        final Map _guidanceItemNameDat;
        final Map _attendParamMap;

        final String _date;
        final String _descDate;

        final String _certifSchoolSchoolName;
        final String _certifSchoolRemark3;
        final String _certifSchoolPrincipalName;
        final String _certifSchoolJobName;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;

       /** 各学校における定数等設定 */
        private KNJSchoolMst _knjSchoolMst;

        private boolean SCHOOL_MST_HAS_SCHOOL_KIND;

        private Map _psMap = new HashMap();

        private Map _kyoukaKubunMap = Collections.EMPTY_MAP;

        final boolean _isOutputDebug;
        final boolean _isOutputDebugVrsout;
        final boolean _isOutputDebugAll;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _prgId = request.getParameter("PRGID");
            _selectGhr = request.getParameter("SELECT_GHR");
            if (PRGID_KNJD418.equals(_prgId) || PRGID_KNJD419.equals(_prgId)) {
                _semester = request.getParameter("SEMESTER");
                _categorySelected = null;
                _gakubuSchoolKind = request.getParameter("GAKUBU_SCHOOL_KIND");
                _ghrCd = request.getParameter("GHR_CD");
                _grade = request.getParameter("GRADE");
                _hrClass = request.getParameter("HR_CLASS");
                _groupcd = request.getParameter("GROUPCD");
                _condition = request.getParameter("CONDITION");
                if (PRGID_KNJD418.equals(_prgId)) {
                    _guidancePattern = request.getParameter("GUIDANCE_PATTERN");
                }
                _d078 = null;
            } else if (PRGID_KNJD420.equals(_prgId)) {
                _semester = request.getParameter("SEMESTER");
                _categorySelected = new String[] { request.getParameter("SCHREGNO") };
                _d078 = request.getParameter("PRINT_GAKKI");
            } else {
                // KNJD421
                _semester = request.getParameter("SEMESTER");
                _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
                _categorySelected2 = request.getParameterValues("CATEGORY_SELECTED2");
                _categoryGhrCd = request.getParameter("GHR_CD");
                _outputDiv = request.getParameter("OUTPUT_DIV");
                _printSide1 = request.getParameter("PRINT_SIDE1");
                _printSide1Attend = request.getParameter("PRINT_SIDE1_ATTEND");
                _printSide2 = "2".equals(_outputDiv) ? "1" : request.getParameter("PRINT_SIDE2");
                _d078 = StringUtils.isBlank(request.getParameter("D078")) ? null : request.getParameter("D078");
                _printSide3 = request.getParameter("PRINT_SIDE3");
                _printSide4 = request.getParameter("PRINT_SIDE4");
                _outputCategoryName2 = request.getParameter("OUTPUT_CATEGORY_NAME2");

                _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE = request.getParameter("HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE");
                _reportSpecialSize05_01 = request.getParameter("reportSpecialSize05_01");
                _HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE = request.getParameter("HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE");
                _reportSpecialSize05_02 = request.getParameter("reportSpecialSize05_02");
                _HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE = request.getParameter("HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE");
            }
            _useGradeKindCompGroupSemester = request.getParameter("useGradeKindCompGroupSemester");
            SCHOOL_MST_HAS_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");

            _date = request.getParameter("DATE");
            _descDate = request.getParameter("DESC_DATE");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } catch (Exception e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _isLastSemester = null != _knjSchoolMst._semesterDiv && _knjSchoolMst._semesterDiv.equals(_semester);
            _is3Gakkisei = null != _knjSchoolMst._semesterDiv && "3".equals(_knjSchoolMst._semesterDiv);

            //表紙
            _useCurriculumcd = "1"; // request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_ctrlYear)) + "年度";
            _z010Name1 = setZ010Name1(db2);
            _d78PrintGakkiSemesterNameMap = new TreeMap();
            _d78PrintGakkiMonthCsvMap = new TreeMap();
            for (final Iterator it = KnjDbUtils.query(db2, " SELECT NAMECD1, NAMECD2, NAME1, ABBV2 FROM V_NAME_MST WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 LIKE 'D%78' ").iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
            	getMappedMap(_d78PrintGakkiSemesterNameMap, KnjDbUtils.getString(row, "NAMECD1")).put(KnjDbUtils.getString(row, "NAMECD2"), KnjDbUtils.getString(row, "NAME1"));
            	getMappedMap(_d78PrintGakkiMonthCsvMap, KnjDbUtils.getString(row, "NAMECD1")).put(KnjDbUtils.getString(row, "NAMECD2"), KnjDbUtils.getString(row, "ABBV2")); // 略称2に表示付きをカンマ区切り 例:"10,11,12,01,02,03"
            }
            _isFukuiken = "fukuiken".equals(_z010Name1);

            _semesterNameMap = getSemesterNameMap(db2, _ctrlYear);
            _schoolNameMap = getSchoolNameMap(db2, _ctrlYear);
            _gradekindAssessMap = getGradeKindAssessMap(db2, _ctrlYear);
            _guidanceItemNameDat = getGuidanceNameDatMap(db2, _ctrlYear);

            final Map certifSchool = getCertifSchoolDat(db2);
            _certifSchoolSchoolName = KnjDbUtils.getString(certifSchool, "SCHOOL_NAME");
            _certifSchoolRemark3 = KnjDbUtils.getString(certifSchool, "REMARK3");
            _certifSchoolPrincipalName = KnjDbUtils.getString(certifSchool, "PRINCIPAL_NAME");
            _certifSchoolJobName = KnjDbUtils.getString(certifSchool, "JOB_NAME");

            _attendParamMap  = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);

            final String[] outputDebug = StringUtils.split(getDbPrginfoProperties(db2, "outputDebug"));
            _isOutputDebugAll = ArrayUtils.contains(outputDebug, "all");
			_isOutputDebug = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "1");
			_isOutputDebugVrsout = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "vrsout");

            if ("1".equals(_printSide3)) {
                // 出欠の情報
                try {
                    _attendParamMap.put("schregno", "?");
                    final String sql = AttendAccumulate.getAttendSemesSql(
                    		_ctrlYear,
                            _semester,
                            null,
                            _date,
                            _attendParamMap
                    );

                    _psMap.put("ATTEND_SEMES", db2.prepareStatement(sql));

                } catch (Exception e) {
                    log.error("exception!", e);
                }
            } else if ("1".equals(_printSide1) && "1".equals(_printSide1Attend)) {

                try {
                	final StringBuffer sql = new StringBuffer();
                    sql.append(" SELECT ");
                	sql.append("    ATS.SEMESTER ");
                	sql.append("  , ATS.MONTH ");
                    sql.append("  , VALUE(LESSON,0) - VALUE(OFFDAYS,0) + CASE WHEN SM.SEM_OFFDAYS = '1' THEN VALUE(OFFDAYS, 0) ELSE 0 END AS LESSON ");
                    sql.append("  , VALUE(SUSPEND, 0) + VALUE(VIRUS, 0) + VALUE(KOUDOME, 0) + VALUE(MOURNING, 0) AS SUSP_MOURNING ");
                    sql.append("  , VALUE(LESSON,0) - VALUE(OFFDAYS,0) + CASE WHEN SM.SEM_OFFDAYS = '1' THEN VALUE(OFFDAYS, 0) ELSE 0 END - (VALUE(ATS.ABROAD,0) + VALUE(ATS.SUSPEND,0) + VALUE(ATS.VIRUS,0) + VALUE(ATS.KOUDOME,0) + VALUE(ATS.MOURNING,0)) AS MLESSON ");
                    sql.append("  , VALUE(SICK,0) AS SICK_ONLY ");
                    sql.append("  , VALUE(NOTICE,0) AS NOTICE_ONLY ");
                    sql.append("  , VALUE(NONOTICE,0) AS NONOTICE_ONLY ");
                    sql.append("  , VALUE(LESSON,0) - VALUE(OFFDAYS,0) + CASE WHEN SM.SEM_OFFDAYS = '1' THEN VALUE(OFFDAYS, 0) ELSE 0 END - (VALUE(ATS.ABROAD,0) + VALUE(ATS.SUSPEND,0) + VALUE(ATS.VIRUS,0) + VALUE(ATS.KOUDOME,0) + VALUE(ATS.MOURNING,0)) - (VALUE(SICK, 0) + VALUE(NOTICE, 0) + VALUE(NONOTICE, 0)) AS PRESENT ");
                    sql.append("  , REM.REMARK1 ");
                	sql.append(" FROM V_ATTEND_SEMES_DAT ATS ");
                	sql.append(" LEFT JOIN (SELECT SCHREGNO, YEAR, GRADE FROM SCHREG_REGD_DAT GROUP BY SCHREGNO, YEAR, GRADE) T2 ON T2.SCHREGNO = ATS.SCHREGNO AND T2.YEAR = ATS.YEAR ");
                	sql.append(" LEFT JOIN (SELECT YEAR, GRADE, SCHOOL_KIND FROM SCHREG_REGD_GDAT GROUP BY YEAR, GRADE, SCHOOL_KIND) T3 ON T3.YEAR = T2.YEAR AND T3.GRADE = T2.GRADE ");
                	sql.append(" LEFT JOIN SCHOOL_MST SM ON SM.YEAR = ATS.YEAR ");
                	if (SCHOOL_MST_HAS_SCHOOL_KIND) {
                    	sql.append(" AND SM.SCHOOL_KIND = T3.SCHOOL_KIND ");
                	}
                	sql.append(" LEFT JOIN ATTEND_SEMES_REMARK_DAT REM ON REM.COPYCD = ATS.COPYCD ");
                	sql.append("     AND REM.YEAR = ATS.YEAR ");
                	sql.append("     AND REM.MONTH = ATS.MONTH ");
                	sql.append("     AND REM.SEMESTER = ATS.SEMESTER ");
                	sql.append("     AND REM.SCHREGNO = ATS.SCHREGNO ");
                	sql.append(" WHERE ATS.YEAR = '" + _ctrlYear + "' ");
                	sql.append("  AND ATS.SCHREGNO = ? ");
                	sql.append("  AND DATE((CASE WHEN ATS.MONTH < '04' THEN CAST(INT(ATS.YEAR) AS CHAR(4)) ELSE ATS.YEAR END) || '-' || ATS.MONTH || '-' || CAST(ATS.APPOINTED_DAY AS CHAR(2))) <= '" + StringUtils.replace(_date, "/", "-") +  "' ");
                	sql.append(" ORDER BY ATS.SEMESTER, INT(ATS.MONTH) + CASE WHEN MONTH < '04' THEN 12 ELSE 0 END ");

                	if (_isOutputDebug) {
                		log.info(" sql attend_semes_month = " + sql.toString());
                	}

                    _psMap.put("ATTEND_SEMES_MONTH", db2.prepareStatement(sql.toString()));

                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }

            setZ052Name1(db2);
        }
        
        private int getD78size(final String schoolKind) {
        	final String key = "D" + schoolKind + "78";
        	if (null != key && !_d78PrintGakkiSemesterNameMap.containsKey(key)) {
        		return -1;
        	}
        	return getMappedMap(_d78PrintGakkiSemesterNameMap, key).size();
        }

        private String getD78name1(final String schoolKind, final String d078) {
        	final String key = "D" + schoolKind + "78";
        	if (null != key && !_d78PrintGakkiSemesterNameMap.containsKey(key)) {
        		return null;
        	}
        	return (String) getMappedMap(_d78PrintGakkiSemesterNameMap, key).get(d078);
        }

        private String[] getD78AttendSemesMonthArray(final String schoolKind, final String d078) {
        	final String key = "D" + schoolKind + "78";
        	if (null != key && !_d78PrintGakkiMonthCsvMap.containsKey(key)) {
        		return new String[] {"04", "05", "06", "07", "08"}; // デフォルト(1学期の表示月)
        	}
        	final String[] split = StringUtils.split((String) getMappedMap(_d78PrintGakkiMonthCsvMap, key).get(d078), ",");
        	if (null == split) {
        		return new String[] {"04", "05", "06", "07", "08"}; // デフォルト(1学期の表示月)
        	}
        	return split;
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD421' AND NAME = '" + propName + "' "));
        }

        public void psClose() {
            for (final Iterator it = _psMap.values().iterator(); it.hasNext();) {
                final PreparedStatement ps = (PreparedStatement) it.next();
                DbUtils.closeQuietly(ps);
            }
        }

        private boolean isPrintHr() {
        	if (null != _selectGhr && "".equals(_selectGhr)) {
        		return true;
        	}
            return null != _categoryGhrCd && _categoryGhrCd.startsWith("HR_");
        }

        private Map getCertifSchoolDat(final DB2UDB db2) {
            return KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '103' "));
        }

        private static Map getGradeKindAssessMap(final DB2UDB db2, final String year) {
        	final String sql = " SELECT GRADE || '-' || SCHOOL_KIND || '-' || CONDITION || '-' || RTRIM(CAST(ASSESSLEVEL AS CHAR(2))) AS KEY, ASSESSMARK FROM GRADE_KIND_ASSESS_DAT WHERE YEAR = '" + year + "' ";
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "KEY", "ASSESSMARK");
        }

//        public String getAssessMark(final String grade, final String schoolKind, final String condition, final String remark4) {
//            return (String) getMappedMap(getMappedMap(getMappedMap(_gradekindAssessMap, grade), schoolKind), condition).get(remark4);
//        }

        public String getAssessMark(final Student student, final GradeKind gradeKind, final String assessLevel) {
            final String assessKey = student._grade + "-" + gradeKind._gakubuSchoolKind + "-" + gradeKind._condition + "-" + assessLevel;
            return (String) _gradekindAssessMap.get(assessKey);
        }

        private static Map getSemesterNameMap(final DB2UDB db2, final String year) {
        	return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT SEMESTER, SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER <> '9' "), "SEMESTER", "SEMESTERNAME");
        }

        private Map getSchoolNameMap(final DB2UDB db2, final String year) {
            final List rowList = KnjDbUtils.query(db2, " SELECT * FROM SCHOOL_MST WHERE YEAR = '" + year + "' ");
			if (SCHOOL_MST_HAS_SCHOOL_KIND) {
            	return KnjDbUtils.getColumnValMap(rowList, "SCHOOL_KIND", "SCHOOLNAME1");
            }
            final String schoolname1 = KnjDbUtils.getString(KnjDbUtils.firstRow(rowList), "SCHOOLNAME1");
            final Map dummy = new HashMap();
            dummy.put("DUMMY", schoolname1);
            return dummy;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }

        /**
         * 名称マスタ NAMECD1='Z052'読込
         */
        private void setZ052Name1(DB2UDB db2) {
            _kyoukaKubunMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z052' ") ,"NAMECD2", "NAME1");
        }

        private String getMaruSuuji(final String suuji) {
        	final String maru1 = "\u2460";
        	final String maru2 = "\u2461";
        	final String maru3 = "\u2462";
        	final Map marunum = new HashMap();
        	marunum.put("1", maru1);
        	marunum.put("2", maru2);
        	marunum.put("3", maru3);
        	final String rtn = (String) marunum.get(suuji);
        	if (null != suuji && null == rtn) {
        		log.warn(" suuji " + suuji + " not found.");
        	}
			return rtn;
        }

        private String getClassRemark1Name(final String classRemark1) {
        	if ("1".equals(_outputCategoryName2)) {
        		return "";
        	}
            final String rtn;
            if (_kyoukaKubunMap.size() > 0 && _kyoukaKubunMap.keySet().contains(classRemark1)) {
                rtn = (String)_kyoukaKubunMap.get(classRemark1);
            } else {
                if ("1".equals(classRemark1)) {
                    rtn = "各教科等を合わせた指導";
                } else if ("2".equals(classRemark1)) {
                    rtn = "領域";
                } else if ("3".equals(classRemark1)) {
                    rtn = "総学";
                } else {
                    rtn = "教科";
                }
            }
            return rtn;
        }

        private String getGuidanceNameDatItemRemark(final GradeKind gradeKind, final String remarkNo, final String alt) {
            String remark = null;
            final String key = gradeKind._gakubuSchoolKind + "-" + gradeKind._condition + "-" + gradeKind._guidancePattern;
            if (null != _guidanceItemNameDat.get(key)) {
                remark = (String) ((Map) _guidanceItemNameDat.get(key)).get(remarkNo);
            }
            return StringUtils.isBlank(remark) ? alt : remark;
        }

        private Map getGuidanceNameDatMap(final DB2UDB db2, final String year) {
            final Map rtn = new HashMap();
            try {

            	final String sql = " SELECT * FROM HREPORT_GUIDANCE_ITEM_NAME_DAT WHERE YEAR = '" + year + "' " + (!"1".equals(_useGradeKindCompGroupSemester) ? " AND SEMESTER = '9' " : "");
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    final Map m = new HashMap();
                    for (int i = 1; i <= 7; i++) {
                        final String field = String.valueOf(i);
                        m.put(field, KnjDbUtils.getString(row, "ITEM_REMARK" + field));
                    }
                    final String key = KnjDbUtils.getString(row, "GAKUBU_SCHOOL_KIND") + "-" + KnjDbUtils.getString(row, "CONDITION") + "-" + KnjDbUtils.getString(row, "GUIDANCE_PATTERN");
                    rtn.put(key, m);
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            }
            return rtn;
        }
    }
}

// eof

