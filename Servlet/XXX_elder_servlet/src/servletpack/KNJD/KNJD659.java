// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2011/06/03 10:04:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
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

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id$
 */
public class KNJD659 {

    private static final Log log = LogFactory.getLog("KNJD659.class");

    private boolean _hasData;
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TAISYOUGAI_KYOUKA = "90";
    private static final String SEMEALL = "9";

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            if (_param._isChiyoda) {
                printMainChiyoda(db2, svf);
            } else {
                printMain(db2, svf);
            }

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMainChiyoda(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        final String form;
        final int maxLine;
        if ("2".equals(_param._formSelect)) {
            form = "KNJD659_3.frm";
            maxLine = 50;
        } else {
            form = "KNJD659_4.frm";
            maxLine = 45;
        }
        final int maxSubclass = 9;

        for (int i = 0; i < _param._hrclasses.length; i++) {
            final HrClass hrClass = new HrClass(_param._hrclasses[i]);
            hrClass.load(db2);
            final List studentPageList = getPageList(hrClass._students, maxLine);
            final List subclassPageList = getPageList(hrClass._subclassMap.values(), maxSubclass);

            final List printSubclasscdList = new ArrayList();
            for (final Iterator it = hrClass._subclassMap.values().iterator(); it.hasNext();) {
                final SubclassData sd = (SubclassData) it.next();
                printSubclasscdList.add(sd._subclassCd);
            }

            for (int sbpi = 0; sbpi < subclassPageList.size(); sbpi++) {
                final List subclassList = (List) subclassPageList.get(sbpi);

                // 1ページ単位の生徒分
                for (int stpi = 0; stpi < studentPageList.size(); stpi++) {
                    final List studentList = (List) studentPageList.get(stpi);

                    svf.VrSetForm(form, 4);

                    svf.VrsOut("PAGE1", String.valueOf(stpi + 1)); // ページ
                    svf.VrsOut("PAGE2", String.valueOf(studentPageList.size())); // ページ

                    svf.VrsOut("year2", _param._nendo); // 年度
                    svf.VrsOut("ymd1", _param._printDate); // 作成日
                    final Semester seme = (Semester) _param._semesterMap.get(_param._semester);
                    svf.VrsOut("TITLE", StringUtils.defaultString(seme._name) + "評定一覧表"); // タイトル
                    int ryugakuCount = 0;
                    int kyugakuCount = 0;
                    String coursenameMajorname = "";
                    for (final Iterator it = hrClass._students.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();
                        if ("1".equals(student._transfercd)) {
                            ryugakuCount += 1;
                        } else if ("2".equals(student._transfercd)) {
                            kyugakuCount += 1;
                        }
                        if (StringUtils.isBlank(coursenameMajorname)) {
                            coursenameMajorname = StringUtils.defaultString(student._coursename) + " " + StringUtils.defaultString(student._majorname);
                        }
                    }
                    svf.VrsOut("COURSE_NAME", coursenameMajorname); // コース名
                    svf.VrsOut("HR_NAME", hrClass._hrName); // クラス名
                    svf.VrsOut("teacher", hrClass._staffName); // 担任氏名
                    svf.VrsOut("STUDENT_SUM", "在籍人数：" + String.valueOf(hrClass._students.size()) + "名 内休学：" + String.valueOf(kyugakuCount) + "名 内留学：" + String.valueOf(ryugakuCount) + "名"); // 在籍人数
                    svf.VrsOut("SCHOOL_NAME", _param._schoolname1); // 学校名

                    for (int seq = 1; seq <= 8; seq++) {
                        final String job = (String) _param._jobnameMap.get(String.valueOf(seq));
                        if (null != job) {
                            svf.VrsOut("JOB" + String.valueOf(8 + 1 - seq) + "_" + (KNJ_EditEdit.getMS932ByteLength(job) <= 8 ? "1" : "2"), job); // 役職名
                            if (null != _param._inkanWakuPath) {
                                svf.VrsOut("STAMP" + String.valueOf(8 + 1 - seq), _param._inkanWakuPath); //検印枠
                            }
                        }
                    }

                    for (int j = 0; j < studentList.size(); j++) {
                        final int line = j + 1;
                        final Student student = (Student) studentList.get(j);
                        svf.VrsOutn("NUMBER", line, student._printAttendNo);
                        svf.VrsOutn("name1", line, student._name);

                        svf.VrsOutn("NUMBER2", line, student._printAttendNo); // 番号
                        svf.VrsOutn("AVERAGE", line, student.getHyoteiHeikin(printSubclasscdList)); // 平均点
                        if (null != student._attendInfo) {
                            svf.VrsOutn("LESSON", line, String.valueOf(student._attendInfo._lesson)); // 授業日数
                            svf.VrsOutn("SUSPEND", line, String.valueOf(student._attendInfo._suspend + student._attendInfo._mourning)); // 停止・忌引数
                            svf.VrsOutn("ABROAD", line, String.valueOf(student._attendInfo._transDays)); // 留学等実績
                            svf.VrsOutn("PRESENT", line, String.valueOf(student._attendInfo._mLesson)); // 出席しなければならない日数
                            svf.VrsOutn("ATTEND", line, String.valueOf(student._attendInfo._present)); // 出席日数
                            svf.VrsOutn("ABSENCE", line, String.valueOf(student._attendInfo._sick)); // 欠席日数
                            if ("1".equals(_param._lateEarlySemes)) {
                                svf.VrsOutn("TOTAL_LATE", line, String.valueOf(student._attendInfo._late)); // 遅刻回数
                                svf.VrsOutn("LEAVE", line, String.valueOf(student._attendInfo._early)); // 早退回数
                            } else {
                                svf.VrsOutn("TOTAL_LATE", line, String.valueOf(student._attendInfo._jyugyouTikoku)); // 遅刻回数
                                svf.VrsOutn("LEAVE", line, String.valueOf(student._attendInfo._jyugyouSoutai)); // 早退回数
                            }
                        }

                        svf.VrsOutn("REMARK", line, student.getTransInfo(db2));
                    }

                    // 科目
                    for (final Iterator itSub = subclassList.iterator(); itSub.hasNext();) {
                        final SubclassData subclassData = (SubclassData) itSub.next();

                        svf.VrsOut("CLASS_NAME", subclassData._className); // 教科
                        svf.VrsOut("SUBCLASS", subclassData._subclassName); // 科目

                        String maxCredits = null;
                        for (int j = 0; j < studentList.size(); j++) {
                            final Student student = (Student) studentList.get(j);
                            final String credits = (String) _param.getCreditMstCredit(student._course, subclassData._subclassCd);
                            if (NumberUtils.isDigits(credits)) {
                                if (NumberUtils.isDigits(maxCredits)) {
                                    maxCredits = String.valueOf(Math.max(Integer.parseInt(credits), Integer.parseInt(maxCredits)));
                                } else {
                                    maxCredits = credits;
                                }
                            }
                        }
                        svf.VrsOut("CREDIT", maxCredits); // 単位数

                        String maxLesson = null;
                        for (int j = 0; j < studentList.size(); j++) {
                            final Student student = (Student) studentList.get(j);
                            final String lesson = (String) student._subclassLessonMap.get(subclassData._subclassCd);
                            if (NumberUtils.isDigits(lesson)) {
                                if (NumberUtils.isDigits(maxLesson)) {
                                    maxLesson = String.valueOf(Math.max(Integer.parseInt(lesson), Integer.parseInt(maxLesson)));
                                } else {
                                    maxLesson = lesson;
                                }
                            }
                        }
                        svf.VrsOut("SUBCLASS_LESSON", maxLesson); // 授業時数

                        svf.VrsOut("SCORE_NAME", "評定"); // 成績名称
                        svf.VrsOut("KEKKA_NAME", "欠時"); // 欠時名称

                        // 観点名称
                        for (int vi = 0; vi < subclassData._viewList.size(); vi++) {
                            final JviewData jviewData = (JviewData) subclassData._viewList.get(vi);
                            final int nameMax = 6;
                            final String viewName = null != jviewData._viewName && jviewData._viewName.length() > nameMax ? jviewData._viewName.substring(0, nameMax) : jviewData._viewName;
                            if (!"HYOUKA".equals(jviewData._viewCd)) {
                                svf.VrsOut("JVIEW" + String.valueOf(vi + 1), viewName); // 観点
                            }
                        }
                        // 観点・評定
                        final List hyoteiList = new ArrayList();
                        for (int j = 0; j < studentList.size(); j++) {
                            final int line = j + 1;
                            final Student student = (Student) studentList.get(j);

                            for (int vi = 0; vi < subclassData._viewList.size(); vi++) {
                                final JviewData jviewData = (JviewData) subclassData._viewList.get(vi);
                                final String val = student.getSetVal(subclassData._subclassCd, jviewData._viewCd, subclassData._mojiHyouka);
                                if ("HYOUKA".equals(jviewData._viewCd)) {
                                    svf.VrsOutn("SCORE1", line, val); // 評定
                                    if (NumberUtils.isNumber(val)) {
                                        hyoteiList.add(new BigDecimal(val));
                                    }
                                } else {
                                    svf.VrsOutn("VALUE" + String.valueOf(vi + 1), line, val); // 観点
                                }
                            }
                            final String kekka = (String) student._subclassKekkaMap.get(subclassData._subclassCd);
                            svf.VrsOutn("KEKKA", line, kekka); // 欠時
                        }

                        svf.VrsOut("AVE_SCORE", average(hyoteiList)); // 評定平均
                        svf.VrEndRecord();
                        _hasData = true;
                    }
                }
            }
        }
    }

    /**
     * collを最大数ごとにグループ化したリストを得る
     * @param coll
     * @param max 最大数
     * @return collを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final Collection coll, final int max) {
        final List pageList = new ArrayList();
        List current = null;
        for (final Iterator it = coll.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
        }
        return pageList;
    }

    /**
     * subclassesをページごとにグループ化したリストを得る
     * @param subclasses 科目のコレクション
     * @param max 1ページ当たり最大列数
     * @return subclassesをページごとにグループ化したリスト
     */
    private static List getSubclassPageList(final Collection subclasses, final int max) {
        final List pageList = new ArrayList();
        List current = null;
        int currentViewSize = 0;
        for (final Iterator it = subclasses.iterator(); it.hasNext();) {
            final SubclassData o = (SubclassData) it.next();
            if (null == current || currentViewSize != 0 && currentViewSize + o._viewList.size() > max) {
                current = new ArrayList();
                currentViewSize = 0;
                pageList.add(current);
            }
            current.add(o);
            currentViewSize += o._viewList.size();
        }
        return pageList;
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static String average(final List list) {
        BigDecimal total = new BigDecimal(0);
        int count = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final BigDecimal hyotei = (BigDecimal) it.next();
            total = total.add(hyotei);
            count += 1;
        }
        if (count == 0) {
            return null;
        }
        final BigDecimal avg = total.divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP);
        //log.info(" average of " + list + " = " + avg);
        return avg.toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final String frmName = "1".equals(_param._formSelect) ? "KNJD659_2.frm" : "KNJD659_1.frm" ;
        for (int i = 0; i < _param._hrclasses.length; i++) {
            final HrClass hrClass = new HrClass(_param._hrclasses[i]);
            hrClass.load(db2);
            AttendInfo total = new AttendInfo(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            for (final Iterator iter = hrClass._students.iterator(); iter.hasNext();) {
                final Student student = (Student) iter.next();
                total = total.plus(student._attendInfo);
            }
            final List subclassDataPageList = getSubclassPageList(hrClass._subclassMap.values(), 54); // 1ページ列
            final List studentPageList = getPageList(hrClass._students, _param._formMaxLine);
            // 1ページ単位の生徒分
            for (int stpi = 0; stpi < studentPageList.size(); stpi++) {
                final List printStudentList = (List) studentPageList.get(stpi);

                // 科目
                for (int sbpi = 0; sbpi < subclassDataPageList.size(); sbpi++) {
                	final List subclassDataList = (List) subclassDataPageList.get(sbpi);

                    setHead(svf, frmName, hrClass);
                    printAttendInfo(svf, 51, total);

                    for (final Iterator itSub = subclassDataList.iterator(); itSub.hasNext();) {
                        final SubclassData subclassData = (SubclassData) itSub.next();
                        final String subclassCd = subclassData._subclassCd;
                        final List subClassName = new ArrayList();
                        String subclassNameField = "course";
                        if (null != subclassData._subclassName) {
                        	String printSubclassName = StringUtils.defaultString(subclassData._subclassName);
                        	int mojisu = 1;
                        	if (subclassData._viewList.size() * 2 < printSubclassName.length() && _param.formHasField(frmName, "course3_1")) {
                        		mojisu = 3;
                        		subclassNameField = "course3_";
                        	} else if (subclassData._viewList.size() < printSubclassName.length() && _param.formHasField(frmName, "course2_1")) {
                        		mojisu = 2;
                        		subclassNameField = "course2_";
                        	}
                        	final int wakuMaxLen = subclassData._viewList.size() * mojisu * 2;
                        	final int subclassnameLen = KNJ_EditEdit.getMS932ByteLength(printSubclassName);
                        	if (subclassnameLen < wakuMaxLen) {
                        		final int hankakuSpaceCount = (wakuMaxLen - subclassnameLen) / 2;
                        		final int zenkakuSpaceCount = hankakuSpaceCount / 2;
                        		printSubclassName = StringUtils.repeat("　",  zenkakuSpaceCount) + printSubclassName; // センタリング
                        	}
                    		for (int j = 0; j < printSubclassName.length(); j += mojisu) {
                    			subClassName.add(printSubclassName.substring(j, Math.min(printSubclassName.length(), j + mojisu)));
                    		}
                        }
                        final Map commentMap = StatusCount.accumulate(hrClass, subclassCd).getPercentageListMap(3, subclassData._viewList.size());
                        // 観点
                        for (int vi = 0; vi < subclassData._viewList.size(); vi++) {
                            final JviewData jviewData = (JviewData) subclassData._viewList.get(vi);
                            if (vi < subClassName.size()) {
                                final String setCourse = "HYOUKA".equals(jviewData._viewCd) ? "2" : "1";
                                svf.VrsOut(subclassNameField + setCourse, (String) subClassName.get(vi));
                            }
                            if ("HYOUKA".equals(jviewData._viewCd)) {
                                svf.VrsOut("JVIEW2", jviewData._viewName);
                                svf.VrsOut("COURSE_DIV2", subclassData._classCd);
                            } else {
                                if (null != jviewData._viewName && jviewData._viewName.length() > 16) {
                                    svf.VrsOut("JVIEW1_2", jviewData._viewName);
                                } else {
                                    svf.VrsOut("JVIEW1", jviewData._viewName);
                                }
                                svf.VrsOut("COURSE_DIV", subclassData._classCd);
                            }
                            // 生徒
                            for (int j = 0; j < printStudentList.size(); j++) {
                                final Student student = (Student) printStudentList.get(j);
                                final int fieldCnt = j + 1;
                                svf.VrsOutn("NUMBER", fieldCnt, student._attendNo);
                                svf.VrsOut("name" + fieldCnt, student._name);
                                printAttendInfo(svf, fieldCnt, student._attendInfo);
                                svf.VrsOut("REMARK" + fieldCnt, student.getTransInfo(db2));
                                final String field;
                                if ("HYOUKA".equals(jviewData._viewCd)) {
                                	if("H".equals(_param._schoolKind) && _param._isNaraken) { //奈良かつ高校の場合は3桁表示
                                		field = "VALUE" + fieldCnt + "_2";
                                	} else {
                                		field = "VALUE" + fieldCnt;
                                	}
                                } else {
                                	field = "SCORE" + fieldCnt;
                                }
                                svf.VrsOut(field, student.getSetVal(subclassCd, jviewData._viewCd, subclassData._mojiHyouka));
                            }
                            for (int si = 0; si < 5; si++) {
                                final String status = String.valueOf(5 - si);
                                final String commentField, groupField;
                                if ("HYOUKA".equals(jviewData._viewCd)) {
                                	commentField = "RANKNUM" + (si + 1) + "_2";
                                	groupField = "RANK_DIV" + (si + 1) + "_2";
                                } else {
                                	commentField = "RANKNUM" + (si + 1) + "_1";
                                	groupField = "RANK_DIV" + (si + 1) + "_1";
                                }
                                final List comment = (List) commentMap.get(status);
                                if (vi < comment.size()) {
                                	svf.VrsOut(commentField, (String) comment.get(vi));
                                }
                                svf.VrsOut(groupField, subclassCd);
                            }
                            svf.VrEndRecord();
                            _hasData = true;
                        }
                    }
                }
            }
        }
    }

    private void printAttendInfo(final Vrw32alp svf, final int fieldCnt, final AttendInfo attendInfo) {
        svf.VrsOut("SUSPEND" + fieldCnt, String.valueOf(attendInfo._suspend + attendInfo._mourning));
        if (51 != fieldCnt) {
            svf.VrsOut("PRESENT" + fieldCnt, String.valueOf(attendInfo._mLesson));
        }

        final Map map = _param.getCX01Map(_param._schoolKind);
        int cntMax = 2;
        int cnt = 0;
        int kei = 0;
        String fieldName = "";
        if (map.containsKey("4") && cnt < cntMax) {
            kei += attendInfo._sickOnly;
            cnt++;
            if (cnt == 1) fieldName = "SICK";
            svf.VrsOut(fieldName + fieldCnt, String.valueOf(attendInfo._sickOnly));
        }
        if (map.containsKey("5") && cnt < cntMax) {
            kei += attendInfo._noticeOnly;
            cnt++;
            if (cnt == 1) fieldName = "SICK";
            if (cnt == 2) fieldName = "NOTICE";
            svf.VrsOut(fieldName + fieldCnt, String.valueOf(attendInfo._noticeOnly));
        }
        if (map.containsKey("6") && cnt < cntMax) {
            kei += attendInfo._nonoticeOnly;
            cnt++;
            if (cnt == 1) fieldName = "SICK";
            if (cnt == 2) fieldName = "NOTICE";
            if (cnt == 3) fieldName = "NONOTICE";
            svf.VrsOut(fieldName + fieldCnt, String.valueOf(attendInfo._nonoticeOnly));
        }
        svf.VrsOut("ABSENCE" + fieldCnt, String.valueOf(kei));

        if (51 != fieldCnt) {
            svf.VrsOut("ATTEND" + fieldCnt, String.valueOf(attendInfo._present));
        }
        if ("1".equals(_param._lateEarlySemes)) {
            svf.VrsOut("LATE" + fieldCnt, String.valueOf(attendInfo._late));
            svf.VrsOut("EARLY" + fieldCnt, String.valueOf(attendInfo._early));
        } else {
            svf.VrsOut("LATE" + fieldCnt, String.valueOf(attendInfo._jyugyouTikoku));
            svf.VrsOut("EARLY" + fieldCnt, String.valueOf(attendInfo._jyugyouSoutai));
        }
        svf.VrsOut("KEKKA" + fieldCnt, String.valueOf(attendInfo._kekkaJisu));
    }

    private void setHead(final Vrw32alp svf, final String frmName, final HrClass hrClass) {
        svf.VrSetForm(frmName, 4);
        try {
        	if (!_param._formFieldMap.containsKey(frmName)) {
        		_param._formFieldMap.put(frmName, SvfField.getSvfFormFieldInfoMapGroupByName(svf));
        	}
        } catch (Throwable e) {
        	log.error("error message = " + e.getMessage());
        }
        svf.VrsOut("year2", _param._nendo);
        final Semester semes = (Semester) _param._semesterMap.get(_param._semester);
        svf.VrsOut("TITLE", semes._name + "観点別成績一覧表");
        if ("H".equals(_param._schoolKind)) {
            svf.VrsOut("CLASS_TITLE", "科目");
        }
        svf.VrsOut("ymd1", _param._printCtrlDate);
        svf.VrsOut("teacher", hrClass._staffName);
        svf.VrsOut("lesson20", String.valueOf(hrClass._maxLesson));
        svf.VrsOut("HR_NAME", hrClass._hrName);
        svf.VrsOut("DATE", _param._printDateFromTo);
        for (int i = 0; i < 5; i++) {
            svf.VrsOut("RANKNAME" + String.valueOf(i + 1), String.valueOf(5 - i));
        }

        final Map map = _param.getCX01Map(_param._schoolKind);
        int cntMax = 2;
        int cnt = 0;
        if (map.containsKey("4") && cnt < cntMax) {
            cnt++;
            svf.VrsOut("ATTEND_NAME" + cnt, "" + map.get("4"));
        }
        if (map.containsKey("5") && cnt < cntMax) {
            cnt++;
            svf.VrsOut("ATTEND_NAME" + cnt, "" + map.get("5"));
        }
        if (map.containsKey("6") && cnt < cntMax) {
            cnt++;
            svf.VrsOut("ATTEND_NAME" + cnt, "" + map.get("6"));
        }
        if ("kyoto".equals(_param._z010Name1)) {
            svf.VrsOut("JOB_NAME1_2", "首席");
            svf.VrsOut("JOB_NAME2_2", "副校長");
            svf.VrsOut("JOB_NAME3_2", "検印");
        } else {
            svf.VrsOut("JOB_NAME1_1", "教頭");
            svf.VrsOut("JOB_NAME2_1", "検印");
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class HrClass {
        private final String _code;
        private String _grade;
        private String _hrClass;
        private String _hrName;
        private String _hrNameAbbv;
        private String _staffName;
        private int _maxLesson;
        private final Map _subclassMap = new LinkedMap();
        private final List _students = new LinkedList();

        public HrClass(final String code) {
            _code = code;
        }

        public void load(final DB2UDB db2) throws SQLException {
            loadInfo(db2);
            loadStudent(db2);
            loadJview(db2);
            loadAttend(db2);
        }

        private void loadInfo(final DB2UDB db2) throws SQLException {
            final String infoSql = getHrInfo();
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, infoSql));
            _grade = KnjDbUtils.getString(row, "GRADE");
            _hrClass = KnjDbUtils.getString(row, "HR_CLASS");
            _hrName = KnjDbUtils.getString(row, "HR_NAME");
            _hrNameAbbv = KnjDbUtils.getString(row, "HR_NAMEABBV");
            _staffName = KnjDbUtils.getString(row, "STAFFNAME");
        }

        private String getHrInfo() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     HR_NAME, ");
            stb.append("     HR_NAMEABBV, ");
            stb.append("     STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.TR_CD1 ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' ");
            stb.append("     AND GRADE || HR_CLASS = '" + _code + "' ");
            stb.append("     AND SEMESTER = '" + _param.getSeme() + "' ");
            return stb.toString();
        }

        private void loadStudent(final DB2UDB db2) throws SQLException {
            final String infoSql = getStudentInfo();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(infoSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregNo = rs.getString("SCHREGNO");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String name = rs.getString("NAME");
                    final String entDate = rs.getString("ENT_DATE");
                    final String entDiv = rs.getString("ENT_DIV");
                    final String entReason = rs.getString("ENT_REASON");
                    final String grdDate = rs.getString("GRD_DATE");
                    final String grdDiv = rs.getString("GRD_DIV");
                    final String grdReason = rs.getString("GRD_REASON");
                    final String course = rs.getString("COURSE");
                    final String coursename = rs.getString("COURSENAME");
                    final String majorname = rs.getString("MAJORNAME");
                    final Student student = new Student(schregNo, attendNo, name, entDate, entDiv, entReason, grdDate, grdDiv, grdReason, course, coursename, majorname);
                    _students.add(student);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.info("stat");
            loadJviewStat(db2);
            log.info("hyouka");
            loadHyouka(db2);
            loadTransfer(db2);
        }

        private String getStudentInfo() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     EG.ENT_DATE, ");
            stb.append("     EG.ENT_DIV, ");
            stb.append("     EG.ENT_REASON, ");
            stb.append("     EG.GRD_DATE, ");
            stb.append("     EG.GRD_DIV, ");
            stb.append("     EG.GRD_REASON, ");
            stb.append("     REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE, ");
            stb.append("     CM.COURSENAME, ");
            stb.append("     MM.MAJORNAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EG ON EG.SCHREGNO = REGD.SCHREGNO ");
            stb.append("         AND EG.SCHOOL_KIND = '" + _param._schoolKind +"' ");
            stb.append("     LEFT JOIN COURSE_MST CM ON CM.COURSECD = REGD.COURSECD ");
            stb.append("     LEFT JOIN MAJOR_MST MM ON MM.COURSECD = REGD.COURSECD AND MM.MAJORCD = REGD.MAJORCD ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + _param.getSeme() + "' ");
            stb.append("     AND REGD.GRADE || REGD.HR_CLASS = '" + _code + "' ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.ATTENDNO ");
            return stb.toString();

        }

        private void loadTransfer(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;

            final String sql = sqlTrans();

            try {
                ps = db2.prepareStatement(sql);
                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregNo}).iterator(); rit.hasNext();) {
                    	final Map row = (Map) rit.next();
                        final String code = KnjDbUtils.getString(row, "transfercd");
                        final String sDate = KnjDbUtils.getString(row, "transfer_sdate");
                        if (null != code) {
                            student._transfercd = code;
                            student._transferSDate = sDate;
                        }
                    }
                }
            } catch (final SQLException e) {
                log.error("異動情報の取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(ps);
            }
        }

        private String sqlTrans() {
            final String sql;
            sql = "SELECT t1.transfercd, t1.transfer_sdate"
                + " FROM schreg_transfer_dat t1 "
                + " inner join semester_mst t2 on t2.year = '" + _param._year + "' AND t2.semester = '" + _param._semester + "'"
                + "                     AND t2.edate BETWEEN t1.transfer_sdate AND t1.transfer_edate"
                + " WHERE t1.schregno= ? "
                + " ORDER BY t1.transfercd, t1.transfer_sdate, t1.transfer_edate "
                ;
            return sql;
        }

        private void loadAttend(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;

            if (_param._isChiyoda || !_param._isKyoai) {
                try {
                    _param._attendParamMap.put("schregno", "?");

                    String attendSubclassSql = "";
                    if (_param._isChiyoda) {
                        attendSubclassSql = AttendAccumulate.getAttendSubclassSql(
                                _param._year,
                                SEMEALL,
                                _param._sDate,
                                _param._date,
                                _param._attendParamMap
                        );
                    } else {
                        attendSubclassSql = AttendAccumulate.getAttendSubclassSql(
                                _param._year,
                                _param._semester,
                                null,
                                _param._date,
                                _param._attendParamMap
                        );
                    }
                    ps = db2.prepareStatement(attendSubclassSql);

                    for (final Iterator it = _students.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();

                        for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregNo}).iterator(); rit.hasNext();) {
                        	final Map row = (Map) rit.next();
                            if (_param._isChiyoda) {
                                if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                                    continue;
                                }
                            } else {
                                if (!_param._semester.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                                    continue;
                                }
                            }
                            final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                            student._subclassLessonMap.put(subclasscd, KnjDbUtils.getString(row, "LESSON"));
                            student._subclassKekkaMap.put(subclasscd, KnjDbUtils.getString(row, "SICK2"));
                            if ("1".equals(KnjDbUtils.getString(row, "IS_COMBINED_SUBCLASS"))) {
                            	student._combinedSubclassSet.add(subclasscd);
                            }
                        }
                    }

                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(ps);
                }
            }

            try {
                _param._attendParamMap.put("schregno", "?");

                String attendSemesSql = "";

                if (_param._isChiyoda) {
                    attendSemesSql = AttendAccumulate.getAttendSemesSql(
                            _param._year,
                            SEMEALL,
                            _param._sDate,
                            _param._date,
                            _param._attendParamMap
                    );
                } else {
                    attendSemesSql = AttendAccumulate.getAttendSemesSql(
                            _param._year,
                            _param._semester,
                            null,
                            _param._date,
                            _param._attendParamMap
                    );
                }

                ps = db2.prepareStatement(attendSemesSql);

                final Integer _0 = new Integer(0);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregNo}).iterator(); rit.hasNext();) {
                    	final Map row = (Map) rit.next();
                        if (_param._isChiyoda) {
                            if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                                continue;
                            }
                        } else {
                            if (!_param._semester.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                                continue;
                            }
                        }
                        int kekkaJisu;
                        if (_param._isKyoai) {
                        	kekkaJisu = KnjDbUtils.getInt(row, "M_KEKKA_JISU", _0).intValue();
                        } else {
                        	BigDecimal sum = new BigDecimal(0);
                        	for (final Iterator sbit = student._subclassKekkaMap.entrySet().iterator(); sbit.hasNext();) {
                        		final Map.Entry e = (Map.Entry) sbit.next();
                        		final String subclasscd = (String) e.getKey();
                        		if (student._combinedSubclassSet.contains(subclasscd)) {
                        			continue;
                        		}
                        		final String kekka = (String) e.getValue();
                        		final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
								if (1 <= iclasscd && iclasscd < 90 && NumberUtils.isNumber(kekka)) {
									sum = sum.add(new BigDecimal(kekka));
                        		}
                        	}
                        	kekkaJisu = sum.intValue();
                        }
						final AttendInfo attendInfo = new AttendInfo(
                                KnjDbUtils.getInt(row, "LESSON", _0).intValue(),
                                KnjDbUtils.getInt(row, "MLESSON", _0).intValue(),
                                KnjDbUtils.getInt(row, "SUSPEND", _0).intValue() + ("true".equals(_param._useVirus) ? KnjDbUtils.getInt(row, "VIRUS", _0).intValue() : 0) + ("true".equals(_param._useKoudome) ? KnjDbUtils.getInt(row, "KOUDOME", _0).intValue() : 0),
                                KnjDbUtils.getInt(row, "MOURNING", _0).intValue(),
                                KnjDbUtils.getInt(row, "SICK", _0).intValue(),
                                KnjDbUtils.getInt(row, "SICK_ONLY", _0).intValue(),
                                KnjDbUtils.getInt(row, "NOTICE_ONLY", _0).intValue(),
                                KnjDbUtils.getInt(row, "NONOTICE_ONLY", _0).intValue(),
                                KnjDbUtils.getInt(row, "REIHAI_KEKKA", _0).intValue(),
                                kekkaJisu,
                                KnjDbUtils.getInt(row, "REIHAI_TIKOKU", _0).intValue(),
                                KnjDbUtils.getInt(row, "JYUGYOU_TIKOKU", _0).intValue(),
                                KnjDbUtils.getInt(row, "JYUGYOU_SOUTAI", _0).intValue(),
                                KnjDbUtils.getInt(row, "PRESENT", _0).intValue(),
                                KnjDbUtils.getInt(row, "LATE", _0).intValue(),
                                KnjDbUtils.getInt(row, "EARLY", _0).intValue(),
                                KnjDbUtils.getInt(row, "TRANSFER_DATE", _0).intValue()
                        );
                        student.setAttendInfo(attendInfo);
                        _maxLesson = Math.max(_maxLesson, student._attendInfo._lesson);
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(ps);
            }
        }

        private void loadJview(final DB2UDB db2) throws SQLException {
            final String jviewSql = getJviewSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(jviewSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String subclassAbbv = rs.getString("SUBCLASSABBV");
                    final String classCd = rs.getString("CLASSCD");
                    final String className = rs.getString("CLASSNAME");
                    final String classAbbv = rs.getString("CLASSABBV");
                    final String viewCd = rs.getString("VIEWCD");
                    final String viewName = rs.getString("VIEWNAME");
                    final String viewAbbv = rs.getString("VIEWABBV");
                    final boolean mojiHyouka = "1".equals(rs.getString("ELECTDIV")) ? true : false;
                    if (!_subclassMap.containsKey(subclassCd)) {
                    	_subclassMap.put(subclassCd, new SubclassData(subclassCd, subclassName, subclassAbbv, classCd, className, classAbbv, mojiHyouka));
                    }
                    final SubclassData subclassData = (SubclassData) _subclassMap.get(subclassCd);
                    subclassData.setJview(viewCd, viewName, viewAbbv, "");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            for (final Iterator iter = _subclassMap.keySet().iterator(); iter.hasNext();) {
                final String subclassCd = (String) iter.next();
                final SubclassData subclassData = (SubclassData) _subclassMap.get(subclassCd);
                subclassData.setJview("HYOUKA", "　　評　　価", "　　評　　価", "");
            }
        }

        private String getJviewSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     L2.SUBCLASSNAME, ");
            stb.append("     L2.SUBCLASSABBV, ");
            stb.append("     substr(T1.SUBCLASSCD, 1, 2) AS CLASSCD, ");
            stb.append("     L3.CLASSNAME, ");
            stb.append("     L3.CLASSABBV, ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     L1.VIEWNAME, ");
            stb.append("     L1.VIEWABBV, ");
            stb.append("     L3.ELECTDIV ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_YDAT T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_MST L1 ON L1.GRADE = T1.GRADE ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("          AND L1.CLASSCD = T1.CLASSCD ");
                stb.append("          AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("          AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("          AND L1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("          AND L1.VIEWCD = T1.VIEWCD ");
            stb.append("     LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("          AND L2.CLASSCD = T1.CLASSCD ");
                stb.append("          AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("          AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN CLASS_MST L3 ON L3.CLASSCD = substr(T1.SUBCLASSCD, 1, 2) ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("          AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.GRADE = '" + _grade + "' ");
            stb.append("     AND substr(T1.SUBCLASSCD, 1, 2) < '" + TAISYOUGAI_KYOUKA + "' ");
            stb.append(" ORDER BY ");
            if(!"chiyoda".equals(_param._z010Name1)){
                stb.append("     VALUE(L3.ELECTDIV, '0'), ");
            }
            stb.append("     L3.SHOWORDER4, ");
            stb.append("     L3.CLASSCD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     L1.SHOWORDER, ");
            stb.append("     T1.VIEWCD ");

            return stb.toString();
        }

        private void loadJviewStat(final DB2UDB db2) throws SQLException {
            final String sql = getJviewStatSql();
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregNo}).iterator(); rit.hasNext();) {
                    	final Map row = (Map) rit.next();
                        final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final String viewCd = KnjDbUtils.getString(row, "VIEWCD");
                        final String status = KnjDbUtils.getString(row, "STATUS");
                        if (!student._subclassScoreMap.containsKey(subclassCd)) {
                        	student._subclassScoreMap.put(subclassCd, new SubclassData(subclassCd, "", "", "", "", "", false));
                        }
                        final SubclassData subclassData = (SubclassData) student._subclassScoreMap.get(subclassCd);
                        subclassData.setJview(viewCd, "", "", status);
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(ps);
            }
        }

        private String getJviewStatSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     SUBCLASSCD, ");
            }
            stb.append("     VIEWCD, ");
            stb.append("     STATUS ");
            stb.append(" FROM ");
            stb.append("     JVIEWSTAT_RECORD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' ");
            stb.append("     AND SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND SCHREGNO = ? ");
            stb.append("     AND substr(SUBCLASSCD, 1, 2) < '" + TAISYOUGAI_KYOUKA + "' ");
            stb.append(" ORDER BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD, ");
            } else {
                stb.append("     SUBCLASSCD, ");
            }
            stb.append("     VIEWCD ");

            return stb.toString();
        }

        private void loadHyouka(final DB2UDB db2) throws SQLException {
            final String sql = getHyoukaSql();
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregNo}).iterator(); rit.hasNext();) {
                    	final Map row = (Map) rit.next();

                        final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final String viewCd = KnjDbUtils.getString(row, "VIEWCD");
                        final String status = KnjDbUtils.getString(row, "STATUS");
                        if (!student._subclassScoreMap.containsKey(subclassCd)) {
                        	student._subclassScoreMap.put(subclassCd, new SubclassData(subclassCd, "", "", "", "", "", false));
                        }
                        SubclassData subclassData = (SubclassData) student._subclassScoreMap.get(subclassCd);
                        subclassData.setJview(viewCd, "", "", status);
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(ps);
            }
        }

        private String getHyoukaSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     SUBCLASSCD, ");
            }
            stb.append("     'HYOUKA' AS VIEWCD, ");
            if ("KIN_RECORD_DAT".equals(_param._useRecordDat)) {
                if ("1".equals(_param._semester)) {
                    stb.append("     SEM1_ASSESS AS STATUS ");
                } else if ("2".equals(_param._semester)) {
                    stb.append("     SEM2_ASSESS AS STATUS ");
                } else if ("3".equals(_param._semester)) {
                    stb.append("     SEM3_ASSESS AS STATUS ");
                } else {
                    stb.append("     GRADE_ASSESS AS STATUS ");
                }
            } else {
                if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_param._useTestCountflg) || "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV".equals(_param._useTestCountflg)) {
                    stb.append("     SCORE AS STATUS ");
                } else {
                    stb.append("     VALUE AS STATUS ");
                }
            }
            stb.append(" FROM ");
            if ("KIN_RECORD_DAT".equals(_param._useRecordDat)) {
                stb.append("     KIN_RECORD_DAT ");
            } else {
                stb.append("     RECORD_SCORE_DAT ");
            }
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' ");
            if (!("KIN_RECORD_DAT".equals(_param._useRecordDat))) {
                stb.append("     AND SEMESTER = '" + _param._semester + "' ");
                stb.append("     AND TESTKINDCD || TESTITEMCD = '9900' ");
                if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_param._useTestCountflg) || "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV".equals(_param._useTestCountflg)) {
                    if ("9".equals(_param._semester)) {
                        stb.append("     AND SCORE_DIV = '09' ");
                    } else {
                        stb.append("     AND SCORE_DIV = '08' ");
                    }
                } else {
                    stb.append("     AND SCORE_DIV = '00' ");
                }
            }
            stb.append("     AND SCHREGNO = ? ");
            stb.append("     AND substr(SUBCLASSCD, 1, 2) < '" + TAISYOUGAI_KYOUKA + "' ");
            stb.append(" ORDER BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     CLASSCD || '-' || SCHOOL_KIND ||  '-' ||CURRICULUM_CD || '-' || SUBCLASSCD ");
            } else {
                stb.append("     SUBCLASSCD ");
            }

            return stb.toString();
        }
    }

    private class Student {
        final String _schregNo;
        final String _attendNo;
        final String _printAttendNo;
        final String _name;
        final String _entDate;
        final String _entDiv;
        final String _entReason;
        final String _grdDate;
        final String _grdDiv;
        final String _grdReason;
        final String _course;
        final String _coursename;
        final String _majorname;
        private String _transferSDate;
        private String _transfercd;
        private final Map _subclassScoreMap = new HashMap();
        private AttendInfo _attendInfo;
        private final Map _subclassKekkaMap = new HashMap();
        private final Map _subclassLessonMap = new HashMap();
        private final Set _combinedSubclassSet = new HashSet();

        public Student(
                final String schregNo,
                final String attendNo,
                final String name,
                final String entDate,
                final String entDiv,
                final String entReason,
                final String grdDate,
                final String grdDiv,
                final String grdReason,
                final String course,
                final String coursename,
                final String majorname
        ) {
            _schregNo = schregNo;
            _attendNo = attendNo;
            _printAttendNo = NumberUtils.isDigits(_attendNo) ? String.valueOf(Integer.parseInt(_attendNo)) : StringUtils.defaultString(_attendNo);
            _name = name;
            _entDate = entDate;
            _entDiv = entDiv;
            _entReason = entReason;
            _grdDate = grdDate;
            _grdDiv = grdDiv;
            _grdReason = grdReason;
            _course = course;
            _coursename = coursename;
            _majorname = majorname;
        }

        public String getHyoteiHeikin(final Collection printSubclasscdList) {
            final List hyoteiList = new ArrayList();
            for (final Iterator it = _subclassScoreMap.values().iterator(); it.hasNext();) {
                final SubclassData subclassData = (SubclassData) it.next();
                if (!printSubclasscdList.contains(subclassData._subclassCd)) {
                    continue;
                }
                for (final Iterator vit = subclassData._viewList.iterator(); vit.hasNext();) {
                    JviewData v = (JviewData) vit.next();
                    if ("HYOUKA".equals(v._viewCd) && NumberUtils.isNumber(v._status)) {
                        hyoteiList.add(new BigDecimal(v._status));
                    }
                }
            }
            return average(hyoteiList);
        }

        private void setAttendInfo(final AttendInfo attendInfo) {
            _attendInfo = attendInfo;
        }

        private String getSetVal(final String subclassCd, final String viewCd, final boolean mojiHyouka) {
            final SubclassData subclassData = (SubclassData) _subclassScoreMap.get(subclassCd);
            if (null != subclassData) {
                for (final Iterator iter = subclassData._viewList.iterator(); iter.hasNext();) {
                    final JviewData jviewData = (JviewData) iter.next();
                    if (viewCd.equals(jviewData._viewCd)) {
                        if ("HYOUKA".equals(jviewData._viewCd) && mojiHyouka) {
                            return "11".equals(jviewData._status) ? "A" : "22".equals(jviewData._status) ? "B" : "33".equals(jviewData._status) ? "C" : "" ;
                        } else {
                        	String outstatstr = jviewData._status;
                            if (outstatstr != null && _param._useNotHyouji != null) {
                                if (outstatstr.equals(_param._useNotHyouji)) outstatstr = "";
                            }
                            return outstatstr;
                        }
                    }
                }
            }

            return "";
        }

        private String getTransInfo(final DB2UDB db2) {
            final String rtn;
            if (enableTrs()) {
                final Map map = (Map) _param._meisyouMap.get("A004");
                rtn = KNJ_EditDate.getAutoFormatDate(db2, _transferSDate) + map.get(_transfercd);
            } else if (enableGrd()) {
                final Map map = (Map) _param._meisyouMap.get("A003");
                rtn = KNJ_EditDate.getAutoFormatDate(db2, _grdDate) + map.get(_grdDiv);
            } else if (enableEnt()) {
                final Map map = (Map) _param._meisyouMap.get("A002");
                rtn = KNJ_EditDate.getAutoFormatDate(db2, _entDate) + map.get(_entDiv);
            } else {
                return null;
            }

            log.debug(_schregNo + " 入学:" + _entDate + "(区分" + _entDiv + ") / 卒業:" + _grdDate + "(区分" + _grdDiv + ") / 異動:" + _transferSDate + "(" + _transfercd + ")");
            return rtn;
        }

        /**
         * 入学データは有効か?
         * @return 有効ならtrue
         */
        private boolean enableEnt() {
            if (null == _entDate) {
                return false;
            }
            if (!"4".equals(_entDiv) && !"5".equals(_entDiv)) {
                return false;
            }

            final Semester semes = (Semester) _param._semesterMap.get(_param._semester);
            if (_entDate.compareTo(semes._sDate) < 0) { // _entDate < aaa
                return false;
            }

            return true;
        }

        /**
         * 卒業データは有効か?
         * @return 有効ならtrue
         */
        private boolean enableGrd() {
            if (null == _grdDate) {
                return false;
            }
            if (!"2".equals(_grdDiv) && !"3".equals(_grdDiv)) {
                return false;
            }

            final Semester semes = (Semester) _param._semesterMap.get(_param._semester);
            if (_grdDate.compareTo(semes._eDate) > 0) { // _grdDate > aaa
                return false;
            }

            return true;
        }

        /**
         * 異動データは有効か?
         * @return 有効ならtrue
         */
        private boolean enableTrs() {
            if (null == _transferSDate) {
                return false;
            }
            if (!"1".equals(_transfercd) && !"2".equals(_transfercd)) {
                return false;
            }
            return true;
        }
    }

    private class SubclassData {
        final String _subclassCd;
        final String _subclassName;
        final String _subclassAbbv;
        final String _classCd;
        final String _className;
        final String _classAbbv;
        final boolean _mojiHyouka;
        final List _viewList = new ArrayList();

        SubclassData (
                final String subclassCd,
                final String subclassName,
                final String subclassAbbv,
                final String classCd,
                final String className,
                final String classAbbv,
                final boolean mojiHyouka
        ) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _subclassAbbv = subclassAbbv;
            _classCd = classCd;
            _className = className;
            _classAbbv = classAbbv;
            _mojiHyouka = mojiHyouka;
        }

        private void setJview(final String viewCd, final String viewName, final String viewAbbv, final String status) {
            _viewList.add(new JviewData(viewCd, viewName, viewAbbv, status));
        }

        public String toString() {
        	return "SubclassData(" + _subclassCd + ", viewList = " + _viewList + ")";
        }
    }

    private class JviewData {
        private final String _viewCd;
        private final String _viewName;
        private final String _viewAbbv;
        private final String _status;

        JviewData (
                final String viewCd,
                final String viewName,
                final String viewAbbv,
                final String status
        ) {
            _viewCd = viewCd;
            _viewName = viewName;
            _viewAbbv = viewAbbv;
            _status = status;
        }
        public String toString() {
        	return "View(" + _viewCd + ", " + _status + ")";
        }
    }

    private class AttendInfo {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _sickOnly;
        final int _noticeOnly;
        final int _nonoticeOnly;
        final int _reihaiKekka;
        final int _kekkaJisu;
        final int _reihaiTikoku;
        final int _jyugyouTikoku;
        final int _jyugyouSoutai;
        final int _present;
        final int _late;
        final int _early;
        final int _transDays;

        private AttendInfo(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int sickOnly,
                final int noticeOnly,
                final int nonoticeOnly,
                final int reihaiKekka,
                final int kekkaJisu,
                final int reihaiTikoku,
                final int jyugyouTikoku,
                final int jyugyouSoutai,
                final int present,
                final int late,
                final int early,
                final int transDays
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _sickOnly = sickOnly;
            _noticeOnly = noticeOnly;
            _nonoticeOnly = nonoticeOnly;
            _reihaiKekka = reihaiKekka;
            _kekkaJisu = kekkaJisu;
            _reihaiTikoku = reihaiTikoku;
            _jyugyouTikoku = jyugyouTikoku;
            _jyugyouSoutai = jyugyouSoutai;
            _present = present;
            _late = late;
            _early = early;
            _transDays = transDays;
        }

        private AttendInfo plus(
                final AttendInfo attendInfo
        ) {
            return new AttendInfo(
                    _lesson + attendInfo._lesson,
                    _mLesson + attendInfo._mLesson,
                    _suspend + attendInfo._suspend,
                    _mourning + attendInfo._mourning,
                    _sick + attendInfo._sick,
                    _sickOnly + attendInfo._sickOnly,
                    _noticeOnly + attendInfo._noticeOnly,
                    _nonoticeOnly + attendInfo._nonoticeOnly,
                    _reihaiKekka + attendInfo._reihaiKekka,
                    _kekkaJisu + attendInfo._kekkaJisu,
                    _reihaiTikoku + attendInfo._reihaiTikoku,
                    _jyugyouTikoku + attendInfo._jyugyouTikoku,
                    _jyugyouSoutai + attendInfo._jyugyouSoutai,
                    _present + attendInfo._present,
                    _late + attendInfo._late,
                    _early + attendInfo._early,
                    _transDays + attendInfo._transDays
            );
        }
    }

    private static class Semester {
        private final String _semester;
        private final String _name;
        private final String _sDate;
        private final String _eDate;

        public Semester(
                final String code,
                final String name,
                final String sDate,
                final String eDate
        ) {
            _semester = code;
            _name = name;
            _sDate = sDate;
            _eDate = eDate;
        }

        public String toString() {
            return _semester + "/" + _name + "/" + _sDate + "/" + _eDate;
        }
    }

    private static class StatusCount {
        private final Map _statusSchregnoMap = new HashMap(); // 評定と学籍番号のリストのマップ

        private List getStatusList(final String status) {
            if (null == _statusSchregnoMap.get(status)) {
                _statusSchregnoMap.put(status, new ArrayList());
            }
            return (List) _statusSchregnoMap.get(status);
        }

        public void add(final String status, final String schregNo) {
            getStatusList(status).add(schregNo);
        }

        public Map getPercentageListMap(final int keta, final int columns) {
            final Map map = new HashMap();
            for (int s = 1; s <= 5; s++) {
                final String status = String.valueOf(s);
                final String percentageString = StringUtils.center(getPercentageString(status), keta * columns - 1);
                map.put(status, split(percentageString, keta));
            }
            return map;
        }

        /**
         * 文字列を桁ごとに分割
         * @param str 対象文字列
         * @param keta 桁
         * @return
         */
        public List split(final String str, final int keta) {
            if (null == str || str.length() == 0) {
                return Collections.EMPTY_LIST;
            }
            final ArrayList list = new ArrayList();
            int st = 0;
            for (int i = 0; i < str.length(); i++) {
                int len = 0;
                try {
                    len = str.substring(st, i + 1).getBytes("MS932").length;
                } catch (Exception e) {
                }
                if (len > keta) {
                    list.add(str.substring(st,  i));
                    st = i;
                }
            }
            if (st != str.length()) {
                list.add(str.substring(st,  str.length()));
            }
            return list;
        }

        /**
         * 評定の百分率と人数の文字列
         * @param status 評定
         * @return
         */
        private String getPercentageString(final String status) {
            int total = 0;
            for (final Iterator it = _statusSchregnoMap.keySet().iterator(); it.hasNext();) {
                final String s = (String) it.next();
                total += getStatusList(s).size();
            }
            if (0 == total) {
                return "";
            }
            final int count = getStatusList(status).size();
            final String d = new BigDecimal(100 * count).divide(new BigDecimal(total), 1, BigDecimal.ROUND_HALF_UP).toString();
            final String percentage = (Double.parseDouble(d) < 10 ? " " : "") + d;
            final String scount = (count < 10 ? " " : "") + String.valueOf(count);
            return percentage + "%(" + scount + "人)";
        }

        private static StatusCount accumulate(final HrClass hrClass, final String subclassCd) {
            // 科目
            final StatusCount statusCount = new StatusCount();
            final SubclassData subclassData = (SubclassData) hrClass._subclassMap.get(subclassCd);
            // 観点
            for (final Iterator itView = subclassData._viewList.iterator(); itView.hasNext();) {
                final JviewData jviewData = (JviewData) itView.next();
                // 生徒
                for (final Iterator itPrint = hrClass._students.iterator(); itPrint.hasNext();) {
                    final Student student = (Student) itPrint.next();
                    if ("HYOUKA".equals(jviewData._viewCd)) {
                        final String status = student.getSetVal(subclassCd, jviewData._viewCd, subclassData._mojiHyouka);
                        statusCount.add(status, student._schregNo);
                    }
                }
            }
            return statusCount;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 72509 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
		final String _year;
        final String _semester;
        final String _grade;
        final String _date;
        final String[] _hrclasses;
        final String _formSelect;
        final String _documentroot;
        final int _formMaxLine;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _printCtrlDate;
        final String _printDateFromTo;
        final String _nendo;
        private Map _semesterMap = new HashMap();
        private Map _creditMap = new HashMap();
        private Map _meisyouMap = new HashMap();
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        final String _lateEarlySemes;
        final String _printDate;
        final boolean _hasSchoolMstSchoolcd;
        final String _schoolKind;
        final String _schoolname1;
        final String _inkanWakuPath;
        final String _useTestCountflg;
		final Map _formFieldMap = new HashMap();

        final String _z010Name1;
        final boolean _isChiyoda;
        final boolean _isKyoai;
        final boolean _isNaraken;
        final String _useRecordDat;
        /** 出欠状況取得引数 */
        private Map _attendParamMap = new HashMap();
        private String _sDate;
        private Map _jobnameMap;
        private String _useNotHyouji;
        private DecimalFormat _absentFmt;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");

            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _hrclasses = request.getParameterValues("CLASS_SELECTED");
            _formSelect = request.getParameter("FORM_SELECT");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _formMaxLine = "1".equals(_formSelect) ? 50 : 45 ;

            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _printCtrlDate = KNJ_EditDate.getAutoFormatDate(db2, _ctrlDate);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _lateEarlySemes = request.getParameter("LATE_EARLY_SEMES");
            _useTestCountflg = request.getParameter("useTestCountflg");

            _schoolKind = getSchregRegdGdat(db2, "SCHOOL_KIND");
            _hasSchoolMstSchoolcd = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            _z010Name1 = setZ010Name1(db2);
            _schoolname1 = getSchoolMst(db2, "SCHOOLNAME1");
            _isChiyoda = "chiyoda".equals(_z010Name1);
            _isKyoai = "kyoai".equals(_z010Name1);
            _isNaraken = "naraken".equals(_z010Name1);
            _useRecordDat = request.getParameter("useRecordDat");
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";

            final String usenothyoujiid = request.getParameter("USENOTHYOUJI");
            if (usenothyoujiid != null && !"".equals(usenothyoujiid)) {
            	if (!"9".equals(_semester)) {
            		_useNotHyouji = setD029Name1(db2, usenothyoujiid);
            	} else {
            		_useNotHyouji = setD028Name1(db2, usenothyoujiid);
            	}
            }

            loadSemester(db2, _year);
            if (_isChiyoda) {
                loadCredit(db2);
            }
            setMeisyouMap(db2);

            final DateFormat df = new SimpleDateFormat("h:m");
            _printDate = KNJ_EditDate.getAutoFormatDate(db2, _ctrlDate) + df.format(Calendar.getInstance().getTime());
            _printDateFromTo = KNJ_EditDate.getAutoFormatDate(db2, _sDate) + FROM_TO_MARK + KNJ_EditDate.getAutoFormatDate(db2, _date);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);

            _jobnameMap = getPrgStampDat(db2);
            if (_jobnameMap.isEmpty()) {
                if (_isChiyoda) {
                    _jobnameMap.put("1", "校長");
                    _jobnameMap.put("2", "副校長");
                    _jobnameMap.put("3", "副校長");
                    _jobnameMap.put("4", "教務主任");
                    _jobnameMap.put("5", "学年主任");
                    _jobnameMap.put("6", "担任");
                    _jobnameMap.put("7", "(担任)");
                }
            }
            log.info(" jobnameMap = " + _jobnameMap);

            _inkanWakuPath = checkFileExists(new File(_documentroot + "/image/KNJD615_keninwaku2.jpg"));

            createDefineCode(db2);
        }

        private KNJDefineSchool createDefineCode(final DB2UDB db2) {
            final KNJDefineSchool definecode = new KNJDefineSchool();

            // 各学校における定数等設定
            definecode.defineCode(db2, _year);
            log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);

            switch (definecode.absent_cov) {
                case 0:
                case 1:
                case 2:
                    _absentFmt = new DecimalFormat("0");
                    break;
                default:
                    _absentFmt = new DecimalFormat("0.0");
            }

            return definecode;
        }

        public boolean formHasField(final String frmName, final String fieldName) {
        	final Map fieldNameFieldMap = (Map) _formFieldMap.get(frmName);
        	if (null != fieldNameFieldMap && fieldNameFieldMap.containsKey(fieldName)) {
        		return true;
        	}
			return false;
		}

		private String checkFileExists(final File file) {
            final String path;
            if (!file.exists()) {
                path = null;
                log.warn("file not found:" + file.getPath());
            } else {
                path = file.getPath();
            }
            return path;
        }

		public Map getCX01Map(final String schoolKind) {
			Map c001Map = (Map) _meisyouMap.get("C" + schoolKind + "01");
			if (null == c001Map) {
				c001Map = (Map) _meisyouMap.get("C001");
			}
			if (null == c001Map) {
				c001Map = new HashMap();
			}
			return c001Map;
		}

        public void setMeisyouMap(final DB2UDB db2) {
            final String[] namecd1 = {
                    "A002",
                    "A003",
                    "A004",
            };
            for (int i = 0; i < namecd1.length; i++) {

                final String sql = "SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + namecd1[i] + "'";

                _meisyouMap.put(namecd1[i], KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "NAMECD2", "NAME1"));
            }

            final String sql = "SELECT NAMECD1, NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 LIKE 'C%01'";

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                getMappedMap(_meisyouMap, KnjDbUtils.getString(row, "NAMECD1")).put(KnjDbUtils.getString(row, "NAMECD2"), KnjDbUtils.getString(row, "NAME1"));
            }
        }

        private String getSchregRegdGdat(final DB2UDB db2, final String field) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT " + field + " FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
        }

        private String getSchoolMst(final DB2UDB db2, final String field) {
            String sql = "";
            sql += "SELECT " + field + " FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
            if (_hasSchoolMstSchoolcd) {
                sql += " AND SCHOOL_KIND = '" + _schoolKind + "' ";
            }
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }

        private String setD028Name1(DB2UDB db2, final String namecd2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = 'D028' AND NAMECD2 = '" + namecd2 + "' "));
        }

        private String setD029Name1(DB2UDB db2, final String namecd2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = 'D029' AND NAMECD2 = '" + namecd2 + "' "));
        }

        private Map getPrgStampDat(final DB2UDB db2) throws SQLException {

            final Map seqTitleMap = new HashMap();

            if (KnjDbUtils.setTableColumnCheck(db2, "PRG_STAMP_DAT", null)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT  ");
                stb.append("    T1.SEQ ");
                stb.append("  , T1.TITLE ");
                stb.append(" FROM PRG_STAMP_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + _year + "' ");
                stb.append("   AND T1.SEMESTER = '9' ");
                stb.append("   AND T1.PROGRAMID = 'KNJD659' ");

                seqTitleMap.putAll(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "SEQ", "TITLE"));
            }

            return seqTitleMap;
        }

        public String getCreditMstCredit(final String course, final String subclasscd) {
            return (String) getMappedMap(_creditMap, course).get(subclasscd);
        }

        private void loadCredit(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM CREDIT_MST WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String course = rs.getString("COURSECD") + rs.getString("MAJORCD") + rs.getString("COURSECODE");
                    final String subclasscd;
                    if ("1".equals(_useCurriculumcd)) {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    } else {
                        subclasscd = rs.getString("SUBCLASSCD");
                    }
                    getMappedMap(_creditMap, course).put(subclasscd, rs.getString("CREDITS"));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         * 年度の開始日を取得する
         */
        private void loadSemester(final DB2UDB db2, String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List list = new ArrayList();
            try {
                ps = db2.prepareStatement(sqlSemester(year));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semesterCd = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    final String sDate = rs.getString("SDATE");
                    final String eDate = rs.getString("EDATE");
                    final Semester semester = new Semester(semesterCd, name, sDate, eDate);
                    _semesterMap.put(semesterCd, semester);

                    list.add(sDate);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            if (!list.isEmpty()) {
                _sDate = (String) list.get(0);
            }
            log.debug("年度の開始日=" + _sDate);
        }

        private String sqlSemester(String year) {
            final String sql;
            sql = "select"
                + "   * "
                + " from"
                + "   SEMESTER_MST"
                + " where"
                + "   YEAR= '" + year + "'"
                + " order by SEMESTER"
            ;
            return sql;
        }

        public String getSeme() {
            return "9".equals(_semester) ? _ctrlSemester : _semester;
        }

    }
}

// eof
