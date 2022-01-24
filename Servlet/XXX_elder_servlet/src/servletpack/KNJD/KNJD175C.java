/*
 * $Id: 4a861469f3ae48a63d8bc50d79d41acf1aba635f $
 *
 * 作成日: 2019/12/20
 * 作成者: yamashiro
 *
 * Copyright(C) 2019-2021 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJPropertiesShokenSize;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理] 中学通知票
 */

public class KNJD175C {

    private static final Log log = LogFactory.getLog(KNJD175C.class);
    private static final String SEMEALL = "9";

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List viewClassList = ViewClass.getViewClassList(db2, _param);
        final List viewClass90List = ViewClass.getViewClass90List(db2, _param);
        final List studentList = Student.getStudentList(db2, _param);
        log.info("viewClassList = " + viewClassList);
        log.info("viewClass90List = " + viewClass90List);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            log.info(" student = " + student._schregno);

            printStudent(db2, svf, viewClassList, student, viewClass90List);
        }
    }

    private void printStudent(final DB2UDB db2, final Vrw32alp svf, final List viewClassList, final Student student, final List viewClass90List) {
        final String form = "KNJD175C.frm";
        svf.VrSetForm(form, 1);

        int cnt = 0;
        for (Iterator ite = _param._semesNameList.iterator();ite.hasNext();) {
        	final String semesName = (String)ite.next();
        	cnt++;
            svf.VrsOut("SEMESTER1_" + cnt, semesName);
            svf.VrsOutn("SEMESTER2", cnt, semesName);
            svf.VrsOut("SEMESTER2_" + cnt, semesName);
        }

        svf.VrsOut("SEMESTER1_2", "1,2学期");
        svf.VrsOut("SEMESTER2_2", "1,2学期");
        svf.VrsOutn("SEMESTER2", 3, "3学期");
        svf.VrsOutn("SEMESTER2", 4, "学年");

//        svf.VrsOut("SEMESTER1_1", _param._semestername1); // 学期
//        svf.VrsOut("SEMESTER1_2", "学年"); // 学期
//        svf.VrsOutn("SEMESTER2", 1, _param._semestername1); // 学期
//        svf.VrsOutn("SEMESTER2", 2, "年間"); // 学期
//        svf.VrsOut("SEMESTER3_1", _param._semestername1); // 学期
//        svf.VrsOut("SEMESTER3_2", "学年"); // 学期
        svf.VrsOut("DATE", "出欠集計日:"+KNJ_EditDate.h_format_JP(db2, _param._date)); //出欠集計日

        svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度　" + _param._gradeName + "　成績通知表"); // タイトル
        final String grade = null == student._grade ? "" : (NumberUtils.isDigits(student._grade) ? String.valueOf(Integer.parseInt(student._grade)) : StringUtils.defaultString(student._grade));
        final String hrclass = null == student._hrclass ? "" : (NumberUtils.isDigits(student._hrclass) ? String.valueOf(Integer.parseInt(student._hrclass)) : StringUtils.defaultString(student._hrclass));
        final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno));
        //svf.VrsOut("HR_NAME", StringUtils.defaultString(student._hrName) + " " + attendno); // 年組番
        svf.VrsOut("GRADE", grade); // 年
        svf.VrsOut("HR", hrclass); // 組
        svf.VrsOut("NO", attendno); // 番
        svf.VrsOut("NAME1", student._name); // 氏名
        svf.VrsOut("PRINCIPAL_NAME", _param._certifSchoolPrincipalName); // 校長名
        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName); // 学校名
        if (student._staffname2 == null || "".equals(student._staffname2)) {
            svf.VrsOut("TEACHER_NAME", student._staffname); // 担任名
        } else {
            svf.VrsOut("TEACHER_NAME3", student._staffname); // 担任名
            svf.VrsOut("TEACHER_NAME2", student._staffname2); // 担任名
        }

        printShoken(db2, svf, student);  //☆☆

        printAttendance(svf, student);  //☆☆

        printSeiseki90(svf, viewClass90List, student);  //☆☆
        printSeiseki(svf, viewClassList, student);  //☆

        svf.VrEndPage();
        _hasData = true;
    }

    private void printSeiseki90(final Vrw32alp svf, final List viewClassList90, final Student student) {
        //final int maxClass = 10;
        for (int cli = 0; cli < viewClassList90.size(); cli++) {
        	if (cli > 1) continue;
            final int classline = cli + 1;
            final ViewClass viewClass = (ViewClass) viewClassList90.get(cli);

            svf.VrsOut("TOTAL_ACT_CONTENT", viewClass._subclassname); // 教科名  //★

            //総合的な学習の時間は、評定は不要。
//            final List viewValuationList = student.getValueList(viewClass._subclasscd);
//            for (final Iterator itv = viewValuationList.iterator(); itv.hasNext();) {
//                final ViewValuation viewValuation = (ViewValuation) itv.next();
//                final String field;
//                if (_param._gakki.equals(viewValuation._semester)) {
//                    field = "DIV1";
//                } else if (SEMEALL.equals(viewValuation._semester)) {
//                    field = "DIV2";
//                } else {
//                    continue;
//                }
//                final String value;
//                if ("1".equals(viewClass._electDiv)) {
//                    if ("11".equals(viewValuation._value)) {
//                        value = "A";
//                    } else if ("22".equals(viewValuation._value)) {
//                        value = "B";
//                    } else if ("33".equals(viewValuation._value)) {
//                        value = "C";
//                    } else {
//                        value = viewValuation._value;
//                    }
//                } else {
//                    value = viewValuation._value;
//                }
//                svf.VrsOutn(field, classline, value); // 評定
//            }

            for (int vi = 0; vi < viewClass.getViewSize(); vi++) {
                final int svi = vi + 1;

                final String viewname = viewClass.getViewName(vi);
                svf.VrsOutn("TOTAL_ACT_VIEW", svi, viewname); // 観点名称  //★

                if (!_param._isLastSemester) continue;  //画面で学年末の学期選択してないなら出力しない。  ※観点名称を出すために、観点名出力後にチェック。
                final List viewRecordList = student.getViewList(viewClass._subclasscd, viewClass.getViewCd(vi));
                for (final Iterator itv = viewRecordList.iterator(); itv.hasNext();) {
                    final ViewRecord viewRecord = (ViewRecord) itv.next();
                    if (!SEMEALL.equals(viewRecord._semester)) continue;  //学年末データのみ利用。
                    svf.VrsOutn("TOTAL_ACT_EVAL", svi, viewRecord._status); // 観点  //★
                }
            }
        }
    }

    private void printSeiseki(final Vrw32alp svf, final List viewClassList, final Student student) {
        //final int maxClass = 10;
        for (int cli = 0; cli < viewClassList.size(); cli++) {
            final int classline = cli + 1;
            final ViewClass viewClass = (ViewClass) viewClassList.get(cli);

            svf.VrsOutn("CLASS_NAME", classline, viewClass._subclassname); // 教科名  //★

            final List viewValuationList = student.getValueList(viewClass._subclasscd);
            for (final Iterator itv = viewValuationList.iterator(); itv.hasNext();) {
                final ViewValuation viewValuation = (ViewValuation) itv.next();
                if (SEMEALL.equals(viewValuation._semester)) continue;  //利用するのは1～3学期。9学期は不要。
                final String field = "DIV" + viewValuation._semester;
//                if (_param._gakki.equals(viewValuation._semester)) {
//                    field = "DIV1";
//                } else if (SEMEALL.equals(viewValuation._semester)) {
//                    field = "DIV2";
//                } else {
//                    continue;
//                }
                final String value;
                if ("1".equals(viewClass._electDiv)) {
                    if ("11".equals(viewValuation._value)) {
                        value = "A";
                    } else if ("22".equals(viewValuation._value)) {
                        value = "B";
                    } else if ("33".equals(viewValuation._value)) {
                        value = "C";
                    } else {
                        value = viewValuation._value;
                    }
                } else {
                    value = viewValuation._value;
                }
                svf.VrsOutn(field, classline, value); // 評定  //★
            }

            for (int vi = 0; vi < viewClass.getViewSize(); vi++) {
                final String svi = String.valueOf(vi + 1);

                final String viewname = viewClass.getViewName(vi);
                svf.VrsOutn("VIEW" + svi, classline, viewname); // 観点名称  //★

                final List viewRecordList = student.getViewList(viewClass._subclasscd, viewClass.getViewCd(vi));
                for (final Iterator itv = viewRecordList.iterator(); itv.hasNext();) {
                    final ViewRecord viewRecord = (ViewRecord) itv.next();
                    if (SEMEALL.equals(viewRecord._semester)) continue;  //利用するのは1～3学期。9学期は不要。
                    final String field = "VAL" + viewRecord._semester;
//                    if (_param._gakki.equals(viewRecord._semester)) {
//                        field = "VAL1";
//                    } else if (SEMEALL.equals(viewRecord._semester)) {
//                        field = "VAL2";
//                    } else {
//                        continue;
//                    }
                    svf.VrsOutn(field + "_" + svi, classline, viewRecord._status); // 観点  //★
                }
            }
        }
    }

    private void printAttendance(final Vrw32alp svf, final Student student) {
        final String[] seme = {"1", "2", "3", "9"};
        for (int semei = 0; semei < seme.length; semei++) {
            final int line = semei + 1;
            //"9"以外で選択学期が最終学期じゃなくて指定学期以降のデータは表示しない。
            if (!SEMEALL.equals(seme[semei]) && !_param._isLastSemester && Integer.parseInt(seme[semei]) > Integer.parseInt(_param._gakki)) {
                continue;
            }

            final AttendSemesDat att = (AttendSemesDat) student._attendSemesDatMap.get(seme[semei]);
            if (null != att) {
                svf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
                svf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend + att._mourning + att._virus + att._koudome)); // 出停・忌引
                svf.VrsOutn("MUST", line, String.valueOf(att._mlesson)); // 出席しなければならない日数
                svf.VrsOutn("ABSENT", line, String.valueOf(att._sick)); // 欠席日数
                svf.VrsOutn("ATTEND", line, String.valueOf(att._present)); // 出席日数
                svf.VrsOutn("LATE", line, String.valueOf(att._late)); // 遅刻
                svf.VrsOutn("EARLY", line, String.valueOf(att._early)); // 早退
            }
        }

        //備考
        svf.VrsOutn("FIELD1", 1, (String)(student._hReportRemDatAttendRecRemark.get("1")));
        if(Integer.parseInt(_param._gakki) >= Integer.parseInt("2")) svf.VrsOutn("FIELD1", 2, (String)(student._hReportRemDatAttendRecRemark.get("2")));
        if(Integer.parseInt(_param._gakki) >= Integer.parseInt("3")) svf.VrsOutn("FIELD1", 3, (String)(student._hReportRemDatAttendRecRemark.get("3")));
        if(Integer.parseInt(_param._gakki) >= Integer.parseInt(_param._lastSemester)) svf.VrsOutn("FIELD1", 4, (String)(student._hReportRemDatAttendRecRemark.get("9")));
    }

    private void printShoken(final DB2UDB db2, final Vrw32alp svf, final Student student) {
//    	if(student._hReportRemarkDatMap.containsKey(_param._gakki)) {
//            final Map map = (Map) student._hReportRemarkDatMap.get(_param._gakki);

//            //統合的な学習  //★特別活動に変更になるはず。
//            if(map.containsKey("1")) {
//                final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) map.get("1");
//                if (null != hReportRemarkDat) {
//                    //内容
//                    String tsac_datsz = StringUtils.defaultString(_param.getRecordTotalstudytimeItemSize(db2, "1", "TOTALSTUDYACT"), "11*11");
//                    final String[] spl1 = StringUtils.split(tsac_datsz, "*");
//                    final KNJPropertiesShokenSize sizeN = new KNJPropertiesShokenSize(Integer.parseInt(spl1[0].trim()), Integer.parseInt(spl1[1].trim()));
//
//                    svfVrsOutnRepeat(svf, "TOTAL_ACT_CONTENT", KNJ_EditKinsoku.getTokenList(hReportRemarkDat._ta1_totalstudyact, sizeN.getKeta(), sizeN._gyo));
//
//                    //観点
//                    String tsav_datsz = StringUtils.defaultString(_param.getRecordTotalstudytimeItemSize(db2, "1", "REMARK1"), "8*11");
//                    final String[] spl2 = StringUtils.split(tsav_datsz, "*");
//                    final KNJPropertiesShokenSize sizeR = new KNJPropertiesShokenSize(Integer.parseInt(spl2[0].trim()), Integer.parseInt(spl2[1].trim()));
//
//                    svfVrsOutnRepeat(svf, "TOTAL_ACT_VIEW", KNJ_EditKinsoku.getTokenList(hReportRemarkDat._ta1_remark1, sizeR.getKeta(), sizeR._gyo));
//
//                    //評価
//                    String tsae_datsz = StringUtils.defaultString(_param.getRecordTotalstudytimeItemSize(db2, "1", "TOTALSTUDYTIME"), "34*11");
//                    final String[] spl3 = StringUtils.split(tsae_datsz, "*");
//                    final KNJPropertiesShokenSize sizeT = new KNJPropertiesShokenSize(Integer.parseInt(spl3[0].trim()), Integer.parseInt(spl3[1].trim()));
//
//                    svfVrsOutnRepeat(svf, "TOTAL_ACT_EVAL", KNJ_EditKinsoku.getTokenList(hReportRemarkDat._ta1_totalstudytime, sizeT.getKeta(), sizeT._gyo));
//                }
//            }
            if (_param._isLastSemester) {
            	int putCnt = 0;
            	for (Iterator ite = student._hReportRemDetDatMap.keySet().iterator();ite.hasNext();) {
            		final String kStr = (String)ite.next();
            		final String val = (String)student._hReportRemDetDatMap.get(kStr);
            		putCnt++;
            		svf.VrsOutn("SP_ACT_EVAL", putCnt, "1".equals(StringUtils.defaultString(val)) ? "〇" : "");  //★特別活動
                }

                //道徳  //★保持データ位置が同じ？出力位置が変わって、表示サイズが変更されるはず。どれぐらい？
//                if(map.containsKey("2")) {
//                    final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) map.get("2");
                    if (null != student._studeTime && !"".equals(student._studeTime)) {
//                        //内容
//                        String ts2a_datsz = StringUtils.defaultString(_param.getRecordTotalstudytimeItemSize(db2, "2", "TOTALSTUDYACT"), "20*11");
//                        final String[] spl5 = StringUtils.split(ts2a_datsz, "*");
//                        final KNJPropertiesShokenSize size2N = new KNJPropertiesShokenSize(Integer.parseInt(spl5[0].trim()), Integer.parseInt(spl5[1].trim()));
    //
//                        svfVrsOutnRepeat(svf, "MORAL_CONTENT", KNJ_EditKinsoku.getTokenList(hReportRemarkDat._ta1_totalstudyact, size2N.getKeta(), size2N._gyo));
    //
//                        //評価
//                        String ts2v_datsz = StringUtils.defaultString(_param.getRecordTotalstudytimeItemSize(db2, "2", "TOTALSTUDYTIME"), "34*11");
//                        final String[] spl4 = StringUtils.split(ts2v_datsz, "*");
//                        final KNJPropertiesShokenSize size2R = new KNJPropertiesShokenSize(Integer.parseInt(spl4[0].trim()), Integer.parseInt(spl4[1].trim()));


//            	if(student._hReportRemDetDatDoutoku != null) {
//            		svfVrsOutnRepeat(svf, "MORAL_EVAL", KNJ_EditKinsoku.getTokenList(student._hReportRemDetDatDoutoku, 84, 5));  //★道徳
//            	}

//                    	svfVrsOutnRepeat(svf, "MORAL_EVAL", KNJ_EditKinsoku.getTokenList(hReportRemarkDat._ta1_totalstudyact, 84, 5));  //★道徳
                    	svfVrsOutnRepeat(svf, "MORAL_EVAL", KNJ_EditKinsoku.getTokenList(student._studeTime, 84, 5));  //★道徳
                  }
//               }
            }
//    	}
    }

    private void svfVrsOutnRepeat(final Vrw32alp svf, final String field, final List token) {
        for (int j = 0; j < token.size(); j++) {
            final int line = j + 1;
            svf.VrsOutn(field, line, (String) token.get(j));
        }
    }

    private static String trimLeft(final String s) {
        if (null == s) {
            return null;
        }
        String rtn = s;
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (ch != ' ' && ch != '　') {
                rtn = s.substring(i);
                break;
            }
        }
        return rtn;
    }

    private static String spacedName(final String name, final int max0) {
        final int max = max0 / 2; // 2行で1レコード
        final int spaceCount = (max - name.length()) / (name.length() + 1);
        final StringBuffer spacedName = new StringBuffer();
        for (int i = 0; i < name.length(); i++) {
            for (int j = 0; j < spaceCount; j++) {
                spacedName.append("　");
            }
            spacedName.append(name.charAt(i));
        }
        for (int j = 0; j < spaceCount; j++) {
            spacedName.append("　");
        }
        for (int i = 0; i < (max - spacedName.length()) / 2; i++) {
            spacedName.insert(0, "　");
            spacedName.append("　");
        }
        for (int i = 0; i < (max - spacedName.length()); i++) {
            spacedName.append("　");
        }
        return spacedName.toString();
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        final String _staffname;
        final String _staffname2;
        final String _grade;
        final String _hrclass;
        List _viewRecordList = Collections.EMPTY_LIST; // 観点
        List _viewValuationList = Collections.EMPTY_LIST; // 評定
        Map _attendSemesDatMap = Collections.EMPTY_MAP; // 出欠の記録
        Map _hReportRemarkDatMap = Collections.EMPTY_MAP; // 通知表所見
        List _chairSubclassList = Collections.EMPTY_LIST;
        Map _hReportRemDetDatMap = Collections.EMPTY_MAP; // 通知表所見
        Map _hReportRemDatAttendRecRemark = Collections.EMPTY_MAP; // 出欠の記録備考
        String _hReportRemDetDatDoutoku; // 通知表所見 道徳
        String _studeTime; // 水都 道徳

        public Student(final String schregno, final String name, final String hrName, final String grade, final String hrclass, final String attendno, final String staffname, final String staffname2) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _grade = grade;
            _hrclass = hrclass;
            _attendno = attendno;
            _staffname = staffname;
            _staffname2 = staffname2;
        }

        public boolean hasChairSubclass(final String subclasscd) {
            return null != ChairSubclass.getChairSubclass(_chairSubclassList, subclasscd);
        }

        /**
         * 観点コードの観点のリストを得る
         * @param subclasscd 科目コード
         * @param viewcd 観点コード
         * @return 観点コードの観点のリスト
         */
        public List getViewList(final String subclasscd, final String viewcd) {
            final List rtn = new ArrayList();
            if (null != viewcd) {
                for (Iterator it = _viewRecordList.iterator(); it.hasNext();) {
                    final ViewRecord viewRecord = (ViewRecord) it.next();
                    if (viewRecord._subclasscd.equals(subclasscd) && viewcd.equals(viewRecord._viewcd)) {
                        rtn.add(viewRecord);
                    }
                }
            }
            return rtn;
        }

        /**
         * 評定のリストを得る
         * @param subclasscd 評定の科目コード
         * @return 評定のリスト
         */
        public List getValueList(final String subclasscd) {
            final List rtn = new ArrayList();
            if (null != subclasscd) {
                for (Iterator it = _viewValuationList.iterator(); it.hasNext();) {
                    final ViewValuation viewValuation = (ViewValuation) it.next();
                    if (subclasscd.equals(viewValuation._subclasscd)) {
                        rtn.add(viewValuation);
                    }
                }
            }
            return rtn;
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentSql(param);
                log.info(" regd sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");

                    final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                    final String hrName = rs.getString("HR_NAME");
                    final String grade = rs.getString("GRADE");
                    final String hrclass = rs.getString("HR_CLASS");
                    final String attendno = rs.getString("ATTENDNO");
                    final String staffname = rs.getString("STAFFNAME");
                    final String staffname2 = rs.getString("STAFFNAME2");
                    final Student student = new Student(schregno, name, hrName, grade, hrclass, attendno, staffname, staffname2);
                    studentList.add(student);
                }

            } catch (SQLException e) {
                log.error("exception!!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            AttendSemesDat.setAttendSemesDatList(db2, param, studentList);
            ViewRecord.setViewRecordList(db2, param, studentList);
            //HReportRemarkDat.setHReportRemarkDatMap(db2, param, studentList);
            HReportRemarkDat.getAttendRecRemark(db2, param, studentList);
            HReportRemarkDat.getSpAct(db2, param, studentList);
            HReportRemarkDat.getDoutoku(db2, param, studentList);
            HReportRemarkDat.getStudyTime(db2, param, studentList);
            ViewValuation.setViewValuationList(db2, param, studentList);
            ChairSubclass.load(db2, param, studentList);
            return studentList;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO_A AS(");
            stb.append("    SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append("    FROM    SCHREG_REGD_DAT T1 ");
            stb.append("            , V_SEMESTER_GRADE_MST T2 ");
            stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.SEMESTER = '"+ param._gakki +"' ");
            stb.append("        AND T1.YEAR = T2.YEAR ");
            stb.append("        AND T1.GRADE = T2.GRADE ");
            stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("        AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("        AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append("    ) ");
            //メイン表
            stb.append("SELECT  T1.SCHREGNO, ");
            stb.append("        T7.HR_NAME, ");
            stb.append("        T1.GRADE, ");
            stb.append("        T1.HR_CLASS, ");
            stb.append("        T1.ATTENDNO, ");
            stb.append("        T5.NAME, ");
            stb.append("        T5.REAL_NAME, ");
            stb.append("        CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append("        T8.STAFFNAME, ");
            stb.append("        T9.STAFFNAME AS STAFFNAME2 ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append("        INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append("        LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
            stb.append("        LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = '" + param._year + "' AND T7.SEMESTER = T1.SEMESTER AND T7.GRADE = T1.GRADE AND T7.HR_CLASS = T1.HR_CLASS ");
            stb.append("        LEFT JOIN STAFF_MST T8 ON T8.STAFFCD = T7.TR_CD1 ");
            stb.append("        LEFT JOIN STAFF_MST T9 ON T9.STAFFCD = T7.TR_CD2 ");
            stb.append("ORDER BY ATTENDNO");
            return stb.toString();
        }
    }

    /**
     * 観点の教科
     */
    private static class ViewClass {
        final String _classcd;
        final String _subclasscd;
        final String _subclassname;
        final String _electDiv;
        final List _viewList;
        final List _valuationList;
        ViewClass(
                final String classcd,
                final String subclasscd,
                final String subclassname,
                final String electDiv) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _electDiv = electDiv;
            _viewList = new ArrayList();
            _valuationList = new ArrayList();
        }

        public void addView(final String viewcd, final String viewname) {
            _viewList.add(new String[]{viewcd, viewname});
        }

        public String getViewCd(final int i) {
            return ((String[]) _viewList.get(i))[0];
        }

        public String getViewName(final int i) {
            return ((String[]) _viewList.get(i))[1];
        }

        public int getViewSize() {
            return _viewList.size();
        }

        public static List getViewClassList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewClassSql(param, false);
                log.info(" viewclass sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String electDiv = rs.getString("ELECTDIV");
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = rs.getString("VIEWNAME");

                    ViewClass viewClass = null;
                    for (final Iterator it = list.iterator(); it.hasNext();) {
                        final ViewClass viewClass0 = (ViewClass) it.next();
                        if (viewClass0._subclasscd.equals(subclasscd)) {
                            viewClass = viewClass0;
                            break;
                        }
                    }

                    if (null == viewClass) {
                        viewClass = new ViewClass(classcd, subclasscd, subclassname, electDiv);
                        list.add(viewClass);
                    }

                    viewClass.addView(viewcd, viewname);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static List getViewClass90List(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewClassSql(param, true);
                log.info(" viewclass sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String electDiv = rs.getString("ELECTDIV");
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = rs.getString("VIEWNAME");

                    ViewClass viewClass = null;
                    for (final Iterator it = list.iterator(); it.hasNext();) {
                        final ViewClass viewClass0 = (ViewClass) it.next();
                        if (viewClass0._subclasscd.equals(subclasscd)) {
                            viewClass = viewClass0;
                            break;
                        }
                    }

                    if (null == viewClass) {
                        viewClass = new ViewClass(classcd, subclasscd, subclassname, electDiv);
                        list.add(viewClass);
                    }

                    viewClass.addView(viewcd, viewname);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getViewClassSql(final Param param, final boolean only90Flg) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T4.SUBCLASSNAME, ");
            stb.append("     VALUE(T4.ELECTDIV, '0') AS ELECTDIV, ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     T1.VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T4.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            if (only90Flg) {
                stb.append("     AND T3.CLASSCD = '90' ");
            } else {
                stb.append("     AND T3.CLASSCD < '90' ");

            }
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T4.ELECTDIV, '0'), ");
            stb.append("     VALUE(T3.SHOWORDER3, -1), ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     VALUE(T1.SHOWORDER, -1), ");
            stb.append("     T1.VIEWCD ");
            return stb.toString();
        }
        public String toString() {
            return _subclasscd + ":" + _subclassname + " " + _electDiv;
        }
    }

    /**
     * 学習の記録（観点）
     */
    private static class ViewRecord {

        final String _semester;
        final String _viewcd;
        final String _status;
        final String _grade;
        final String _viewname;
        final String _classcd;
        final String _subclasscd;
        final String _classMstShoworder;
        final String _showorder;
        ViewRecord(
                final String semester,
                final String viewcd,
                final String status,
                final String grade,
                final String viewname,
                final String classcd,
                final String subclasscd,
                final String classMstShoworder,
                final String showorder) {
            _semester = semester;
            _viewcd = viewcd;
            _status = status;
            _grade = grade;
            _viewname = viewname;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classMstShoworder = classMstShoworder;
            _showorder = showorder;
        }

        public static void setViewRecordList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewRecordSql(param);
                log.info(" view record sql = "+  sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._viewRecordList = new ArrayList();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String viewcd = rs.getString("VIEWCD");
                        final String status = rs.getString("STATUS");
                        final String grade = rs.getString("GRADE");
                        final String viewname = rs.getString("VIEWNAME");
                        final String classcd = rs.getString("CLASSCD");
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String classMstShoworder = rs.getString("CLASS_MST_SHOWORDER");
                        final String showorder = rs.getString("SHOWORDER");

                        final ViewRecord viewRecord = new ViewRecord(semester, viewcd, status, grade, viewname, classcd, subclasscd, classMstShoworder, showorder);

                        //if (param._gakki.equals(semester) || SEMEALL.equals(semester)) {
                            student._viewRecordList.add(viewRecord);
                        //}
                    }

                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getViewRecordSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE   ");
            stb.append("     , T1.VIEWNAME ");
            stb.append("     , T1.VIEWCD ");
            stb.append("     , T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , T3.SCHREGNO ");
            stb.append("     , T3.STATUS ");
            stb.append("     , T4.CLASSCD ");
            stb.append("     , T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     , T4.SHOWORDER AS CLASS_MST_SHOWORDER ");
            stb.append("     , T1.SHOWORDER ");
            stb.append(" FROM JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T3.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND T3.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T3.YEAR = T2.YEAR ");
            if (!param._isLastSemester) {
                stb.append("     AND T3.SEMESTER <= '" + param._gakki + "' ");
            }
            stb.append("         AND T3.SCHREGNO = ? ");
            stb.append("     LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , VALUE(T4.SHOWORDER, 0) ");
            stb.append("     , T4.CLASSCD ");
            stb.append("     , VALUE(T1.SHOWORDER, 0) ");
            stb.append("     , T1.VIEWCD ");
            return stb.toString();
        }
    }

    /**
     * 学習の記録（評定）
     */
    private static class ViewValuation {
        final String _semester;
        final String _classcd;
        final String _subclasscd;
        final String _subclassname;
        final String _value;
        ViewValuation(
                final String semester,
                final String classcd,
                final String subclasscd,
                final String subclassname,
                final String value) {
            _semester = semester;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _value = value;
        }

        public static void setViewValuationList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewValuationSql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._viewValuationList = new ArrayList();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");
                        final String classcd = rs.getString("CLASSCD");
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String value = rs.getString("VALUE");
                        final ViewValuation viewValuation = new ViewValuation(semester, classcd, subclasscd, subclassname, value);

                        student._viewValuationList.add(viewValuation);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getViewValuationSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     SUBSTR(T2.SUBCLASSCD, 1, 2) AS CLASSCD, ");
            stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T4.SUBCLASSNAME, ");
            stb.append("     T2.SCORE AS VALUE ");
            stb.append(" FROM ");
            stb.append("     RECORD_SCORE_DAT T2 ");
            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("         AND T4.CLASSCD = T2.CLASSCD  ");
            stb.append("         AND T4.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            stb.append("         AND T4.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            stb.append("     LEFT JOIN CLASS_MST T5 ON T5.CLASSCD = SUBSTR(T2.SUBCLASSCD, 1, 2) ");
            stb.append("         AND T5.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            if (!param._isLastSemester) {
                stb.append("     AND T2.SEMESTER <= '" + param._gakki + "' ");
            }
            stb.append("     AND T2.TESTKINDCD = '99' ");
            stb.append("     AND T2.TESTITEMCD = '00' ");
            stb.append("     AND T2.SCORE_DIV = '09' ");
            stb.append("     AND T2.SCHREGNO = ? ");
            if ("Y".equals(param._d016Namespare1)) {
                stb.append("     AND NOT EXISTS ( ");
                stb.append("         SELECT 'X' ");
                stb.append("         FROM ");
                stb.append("             SUBCLASS_REPLACE_COMBINED_DAT L1 ");
                stb.append("         WHERE ");
                stb.append("             L1.YEAR = T2.YEAR ");
                stb.append("             AND L1.ATTEND_CLASSCD = T2.CLASSCD ");
                stb.append("             AND L1.ATTEND_SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("             AND L1.ATTEND_CURRICULUM_CD = T2.CURRICULUM_CD ");
                stb.append("             AND L1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD ");
                stb.append("     ) ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T5.SHOWORDER3, ");
            stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD ");
            return stb.toString();
        }
    }

    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {

        final String _semester;
        int _lesson;
        int _suspend;
        int _mourning;
        int _mlesson;
        int _sick;
        int _absent;
        int _present;
        int _late;
        int _early;
        int _transferDate;
        int _offdays;
        int _kekkaJisu;
        int _virus;
        int _koudome;

        public AttendSemesDat(
                final String semester
        ) {
            _semester = semester;
        }

        public void add(
                final AttendSemesDat o
        ) {
            _lesson += o._lesson;
            _suspend += o._suspend;
            _mourning += o._mourning;
            _mlesson += o._mlesson;
            _sick += o._sick;
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

        private static void setAttendSemesDatList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._gakki,
                        null,
                        param._date,
                        param._attendParamMap
                );
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._attendSemesDatMap = new HashMap();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");
                        final int lesson = rs.getInt("LESSON");
                        final int suspend = rs.getInt("SUSPEND");
                        final int mourning = rs.getInt("MOURNING");
                        final int mlesson = rs.getInt("MLESSON");
                        final int sick = rs.getInt("SICK");
                        final int absent = rs.getInt("ABSENT");
                        final int present = rs.getInt("PRESENT");
                        final int late = rs.getInt("LATE");
                        final int early = rs.getInt("EARLY");
                        final int transferDate = rs.getInt("TRANSFER_DATE");
                        final int offdays = rs.getInt("OFFDAYS");
                        final int kekkaJisu = rs.getInt("KEKKA_JISU");
                        final int virus = rs.getInt("VIRUS");
                        final int koudome = rs.getInt("KOUDOME");

                        final AttendSemesDat attendSemesDat = new AttendSemesDat(semester);
                        attendSemesDat._lesson = lesson;
                        attendSemesDat._suspend = suspend;
                        attendSemesDat._mourning = mourning;
                        attendSemesDat._mlesson = mlesson;
                        attendSemesDat._sick = sick;
                        attendSemesDat._absent = absent;
                        attendSemesDat._present = present;
                        attendSemesDat._late = late;
                        attendSemesDat._early = early;
                        attendSemesDat._transferDate = transferDate;
                        attendSemesDat._offdays = offdays;
                        attendSemesDat._kekkaJisu = kekkaJisu;
                        attendSemesDat._virus = virus;
                        attendSemesDat._koudome = koudome;

                        student._attendSemesDatMap.put(semester, attendSemesDat);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

    }

    private static class ChairSubclass {
        final String _subclasscd;
        final List _chaircdList;
        public ChairSubclass(final String subclasscd) {
            _subclasscd = subclasscd;
            _chaircdList = new ArrayList();
        }
        public static void load(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT ");
                stb.append("   T2.CHAIRCD, ");
                stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD ");
                stb.append(" FROM ");
                stb.append("   CHAIR_STD_DAT T1 ");
                stb.append("   INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("     AND T2.CHAIRCD = T1.CHAIRCD ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + param._year + "' ");
                if (!param._isLastSemester) {
                    stb.append("     AND T1.SEMESTER <= '" + param._gakki + "' ");
                }
                stb.append("     AND T1.SCHREGNO = ? ");
                stb.append("     AND '" + param._date + "' BETWEEN T1.APPDATE AND T1.APPENDDATE ");

                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._chairSubclassList = new ArrayList();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {

                        ChairSubclass cs = getChairSubclass(student._chairSubclassList, rs.getString("SUBCLASSCD"));
                        if (null == cs) {
                            cs = new ChairSubclass(rs.getString("SUBCLASSCD"));
                            student._chairSubclassList.add(cs);
                        }
                        cs._chaircdList.add(rs.getString("CHAIRCD"));
                    }
                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        public static ChairSubclass getChairSubclass(final List chairSubclassList, final String subclasscd) {
            ChairSubclass chairSubclass = null;
            for (final Iterator it = chairSubclassList.iterator(); it.hasNext();) {
                final ChairSubclass cs = (ChairSubclass) it.next();
                if (null != cs._subclasscd && cs._subclasscd.equals(subclasscd)) {
                    chairSubclass = cs;
                    break;
                }
            }
            return chairSubclass;
        }
    }

    /**
     * 通知表所見
     */
    private static class HReportRemarkDat {
        final String _semester;
        final String _remark2;
        final String _ta1_totalstudytime;
        final String _ta1_totalstudyact;
        final String _ta1_remark1;

        public HReportRemarkDat(
                final String semester,
                final String remark2,
                final String ta1_totalstudytime,
                final String ta1_totalstudyact,
                final String ta1_remark1) {
            _semester = semester;
            _remark2 = remark2;
            _ta1_totalstudytime = ta1_totalstudytime;
            _ta1_totalstudyact = ta1_totalstudyact;
            _ta1_remark1 = ta1_remark1;
        }

        public static void setHReportRemarkDatMap(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHReportRemarkSql(db2, param);
                log.debug("totalstudy sql = "+sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._hReportRemarkDatMap = new HashMap();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    String semester = "";
                    String remark2 = "";
                    String ta1_totalstudytime = "";
                    String ta1_remark1 = "";
                    String ta1_totalstudyact = "";
                    String sep = "\n";
                    while (rs.next()) {
                        if ("".equals(semester) || !semester.equals(rs.getString("SEMESTER")) || !remark2.equals(rs.getString("REMARK2")) ) {
                            if (!"".equals(semester)) {
                                final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(semester, remark2, ta1_totalstudytime, ta1_totalstudyact, ta1_remark1);
//                              student._hReportRemarkDatMap.put(semester, hReportRemarkDat);
                                Map map = new HashMap();
                                if(student._hReportRemarkDatMap.containsKey(semester)) map = (Map)student._hReportRemarkDatMap.get(semester) ;
                                map.put(remark2, hReportRemarkDat);
                                student._hReportRemarkDatMap.put(semester, map);
                            }
                            semester = rs.getString("SEMESTER");
                            remark2 = rs.getString("REMARK2");
                            ta1_totalstudytime = rs.getString("TA1_TOTALSTUDYTIME");
                            ta1_totalstudyact = rs.getString("TA1_TOTALSTUDYACT");
                            ta1_remark1 = rs.getString("TA1_REMARK1");
                        } else {
                            semester = rs.getString("SEMESTER");
                            remark2 = rs.getString("REMARK2");
                            ta1_totalstudytime = ta1_totalstudytime + sep + rs.getString("TA1_TOTALSTUDYTIME");
                            ta1_totalstudyact = ta1_totalstudyact + sep + rs.getString("TA1_TOTALSTUDYACT");
                            ta1_remark1 = ta1_remark1 + sep + rs.getString("TA1_REMARK1");
                        }

                    }
                    final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(semester, remark2, ta1_totalstudytime, ta1_totalstudyact, ta1_remark1);
//                    student._hReportRemarkDatMap.put(semester, hReportRemarkDat);
                    Map map = new HashMap();
                    if(student._hReportRemarkDatMap.containsKey(semester)) map = (Map)student._hReportRemarkDatMap.get(semester) ;
                    map.put(remark2, hReportRemarkDat);
                    student._hReportRemarkDatMap.put(semester, map);
                    DbUtils.closeQuietly(rs);

                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getHReportRemarkSql(final DB2UDB db2, final Param param) {
        	final String where1 = param.getRecordTotalstudytimeWhere(db2, "T3.", "1"); //総合的な学習の時間のCLASSCD
        	final String where2 = param.getRecordTotalstudytimeWhere(db2, "T3.", "2"); //道徳のCLASSCD
            final StringBuffer stb = new StringBuffer();
            //RECORD_TOTALSTUDYTIME_DATで、紐づけたRECORD_TOTALSTUDYTIME_ITEM_MSTのデータだけが対象。
            stb.append(" WITH GETDOUTOKU as ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("  T4.*, ");
            stb.append("  T3.REMARK2 ");
            stb.append(" FROM ");
            stb.append("  RECORD_TOTALSTUDYTIME_DAT T4 ");
            stb.append("  LEFT JOIN RECORD_TOTALSTUDYTIME_ITEM_MST T3 ");
            stb.append("     ON T3.CLASSCD = T4.CLASSCD ");
            stb.append("    AND T3.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("    T3.SHOW_FLG = '1' ");
            stb.append(" ), COMMON as ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("  T4.SCHREGNO, ");
            stb.append("  T4.YEAR, ");
            stb.append("  T4.SEMESTER ");
            stb.append(" FROM ");
            stb.append("  GETDOUTOKU T4 ");
            stb.append(" ) ");
            //メイン
            stb.append(" SELECT ");
            stb.append("  T1.SEMESTER, ");
            stb.append("  T3.REMARK2, ");
            stb.append("  T3.CLASSCD, ");
            stb.append("  T3.TOTALSTUDYACT as TA1_TOTALSTUDYACT, ");
            stb.append("  T3.REMARK1 as TA1_REMARK1, ");
            stb.append("  T3.TOTALSTUDYTIME as TA1_TOTALSTUDYTIME ");
            stb.append(" FROM ");
            stb.append("  COMMON T1 ");
            stb.append("  LEFT JOIN GETDOUTOKU T3 ");
            stb.append("     ON T3.YEAR = T1.YEAR ");
            stb.append("    AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("    AND (    ( T3.REMARK2  = '1' AND "+where1+" ) ");
            stb.append("          OR ( T3.REMARK2  = '2' AND "+where2+" ) ");
            stb.append("        ) ");
            stb.append("    AND T3.SCHOOL_KIND = '" + param._schoolkind + "' ");
            stb.append("    AND T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + param._year + "' ");
            stb.append("  and T1.SCHREGNO = ? ");
            if (!param._isLastSemester) {
                stb.append("     AND (T1.SEMESTER <= '" + param._gakki + "') ");
            }
            stb.append(" ORDER BY ");
            stb.append("  T1.SEMESTER, ");
            stb.append("  T3.REMARK2, ");
            stb.append("  T3.CLASSCD ");
            return stb.toString();
        }
        private static void getSpAct(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSpActSql(param);
                log.debug("totalstudy sql = "+sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._hReportRemDetDatMap = new HashMap();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                    	final String code = rs.getString("CODE");
                    	final String remark1 = rs.getString("REMARK1");
                    	student._hReportRemDetDatMap.put(code, remark1);
                    }
                    DbUtils.closeQuietly(rs);

                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        private static String getSpActSql(final Param param) {
        	final StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("   CODE, ");
        	stb.append("   REMARK1 ");
        	stb.append(" FROM ");
        	stb.append("   HREPORTREMARK_DETAIL_DAT ");
        	stb.append(" WHERE ");
        	stb.append("   YEAR = '" + param._year + "' ");
        	stb.append("   AND SEMESTER = '" + SEMEALL + "' ");
        	stb.append("   AND SCHREGNO = ? ");
        	stb.append("   AND DIV = '01' ");
        	stb.append("   AND CODE IN ('01', '02', '03') ");
        	return stb.toString();
        }

        private static void getStudyTime(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudyTimeSql(param);
                log.debug("StudyTime sql = "+sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);

                    String str = "";
                    rs = ps.executeQuery();
                    while (rs.next()) {
                    	str += rs.getString("TOTALSTUDYACT");
                    }

                    student._studeTime = str;
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        private static String getStudyTimeSql(final Param param) {
        	final StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("    *");
        	stb.append(" FROM ");
        	stb.append("   RECORD_TOTALSTUDYTIME_DAT ");
        	stb.append(" WHERE ");
        	stb.append("   YEAR = '" + param._year + "' ");
        	stb.append("   AND SEMESTER = '" + param._lastSemester + "' ");
        	stb.append("   AND SCHREGNO = ? ");
        	stb.append("   AND CLASSCD = '20' "); //水都 道徳
        	stb.append("   AND SCHOOL_KIND = '" + param._schoolkind + "' ");
        	return stb.toString();
        }


        private static void getDoutoku(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getDoutokuSql(param);
                log.debug("Doutoku sql = "+sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                    	student._hReportRemDetDatDoutoku = rs.getString("REMARK1");
                    }
                    DbUtils.closeQuietly(rs);

                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        private static String getDoutokuSql(final Param param) {
        	final StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("   CODE, ");
        	stb.append("   REMARK1 ");
        	stb.append(" FROM ");
        	stb.append("   HREPORTREMARK_DETAIL_DAT ");
        	stb.append(" WHERE ");
        	stb.append("   YEAR = '" + param._year + "' ");
        	stb.append("   AND SEMESTER = '" + param._gakki + "' ");
        	stb.append("   AND SCHREGNO = ? ");
        	stb.append("   AND DIV = '02' ");
        	stb.append("   AND CODE = '01' ");
        	return stb.toString();
        }

        private static void getAttendRecRemark(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getAttendRecRemarkSql(param);
                log.debug("AttendRecRemark sql = "+sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    student._hReportRemDatAttendRecRemark = new HashMap();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                    	final String sem = rs.getString("SEMESTER");
                    	final String remark = rs.getString("ATTENDREC_REMARK");
                    	student._hReportRemDatAttendRecRemark.put(sem, remark);
                    }
                    DbUtils.closeQuietly(rs);

                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        private static String getAttendRecRemarkSql(final Param param) {
        	final StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("   SEMESTER, ");
        	stb.append("   ATTENDREC_REMARK ");
        	stb.append(" FROM ");
        	stb.append("   HREPORTREMARK_DAT ");
        	stb.append(" WHERE ");
        	stb.append("   YEAR = '" + param._year + "' ");
        	stb.append("   AND SEMESTER <= '" + SEMEALL + "' ");
        	stb.append("   AND SCHREGNO = ? ");
        	return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75719 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _gakki;
        final String _date;
        final String _gradeHrclass;
        final String _grade;
        final String[] _categorySelected;

        final String _gradeCdStr;
        final String _certifSchoolSchoolName;
        final String _certifSchoolRemark3;
        final String _certifSchoolPrincipalName;
        final String _gradeName;
        final String _d016Namespare1;
        private boolean _isLastSemester;
        String _lastSemester;

        final Map _attendParamMap;

//        private String _semestername1;
        private String _schoolkind;
        private List _semesNameList;

//        final Map _totalStudyTime_DataSizeMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _gakki = request.getParameter("GAKKI");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            _categorySelected = request.getParameterValues("category_selected");

            _gradeCdStr = getGradeCdIntStr(db2, _grade);
            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolRemark3 = getCertifSchoolDat(db2, "REMARK3");
            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _gradeName = "第" + StringUtils.defaultString(_gradeCdStr) + "学年";
            _d016Namespare1 = getNameMst(db2, _year, "D016", "01", "NAMESPARE1");

            _semesNameList = setSemester(db2);

            _schoolkind = getSchoolKind(db2);
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);

//            _totalStudyTime_DataSizeMap = getTotalStudyTime_DataSizeMap(db2);

        }

//        private Map getTotalStudyTime_DataSizeMap(final DB2UDB db2) {
//        	StringBuffer stb = new StringBuffer();
//        	stb.append(" SELECT CLASSCD || COLUMNNAME AS CODE, DATA_SIZE FROM RECORD_TOTALSTUDYTIME_ITEM_MST ");
//        	stb.append(" WHERE ");
//        	stb.append("   CLASSCD = '" + CLASSCD_TOTALSTDY + "' AND SCHOOL_KIND = '" + _schoolkind + "' ");
//        	stb.append(" UNION ");
//        	stb.append(" SELECT CLASSCD || COLUMNNAME AS CODE, DATA_SIZE FROM RECORD_TOTALSTUDYTIME_ITEM_MST ");
//        	stb.append(" WHERE ");
//        	stb.append("   CLASSCD = '" + CLASSCD_MORAL + "' AND SCHOOL_KIND = '" + _schoolkind + "' ");
//        	return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "CODE", "DATA_SIZE");
//        }

        private String getSchoolKind(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2," SELECT GDAT.SCHOOL_KIND FROM SCHREG_REGD_GDAT GDAT WHERE GDAT.YEAR = '" + _year + "' AND GDAT.GRADE = '" + _grade + "' "));
        }

        private String getNameMst(final DB2UDB db2, final String year, final String namecd1, final String namecd2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("    " + field + " ");
                sql.append(" FROM NAME_MST T1 ");
                sql.append("    INNER JOIN NAME_YDAT T2 ON T2.YEAR = '" + year + "' AND T2.NAMECD1 = T1.NAMECD1 AND T2.NAMECD2 = T1.NAMECD2 ");
                sql.append(" WHERE ");
                sql.append("    T1.NAMECD1 = '" + namecd1 + "' ");
                sql.append("    AND T1.NAMECD2 = '" + namecd2 + "' ");
                sql.append("   ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '103' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getGradeCdIntStr(final DB2UDB db2, final String grade) {
            String gradeCd = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.SCHOOL_KIND = 'J' ");
                stb.append("     AND T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String tmp = rs.getString("GRADE_CD");
                    if (NumberUtils.isDigits(tmp)) {
                        gradeCd = String.valueOf(Integer.parseInt(tmp));
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradeCd;
        }

        private List setSemester(final DB2UDB db2) {
        	List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;

            String lastSemester = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SEMESTER_MST T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.SEMESTER IN ('1', '2', '3', '9') ");  //名称としては、1,2,9で取得。3を取るのは、lastsemesterのため。
                stb.append(" ORDER BY ");
                stb.append("     T1.SEMESTER ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    if (!"3".equals(semester)) {
                        retList.add(rs.getString("SEMESTERNAME"));  //3は飛ばして9の名称を入れる。
                    }
                    if (!SEMEALL.equals(semester)) {
                        lastSemester = semester;
                    }
                }
                _lastSemester = lastSemester;
                _isLastSemester = null != lastSemester && lastSemester.equals(_gakki);
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        /**
         * RECORD_TOTALSTUDYTIME_DATのWHERE句を作成
         * @param tab テーブル別名
         * @param remark2 1:総合的な学習の時間 2:道徳
         * @return 作成した文字列
         */
        private String getRecordTotalstudytimeWhere(DB2UDB db2, final String tab, final String remark2) {
        	boolean flg = false;
        	String cone = "";
			final StringBuffer rtnStr = new StringBuffer();
			rtnStr.append( tab + "CLASSCD IN ( ");
            final List list = getRecordTotalstudytimeItemClassList(db2, remark2);
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final String classcd = (String) it.next();
                rtnStr.append(cone).append("'"+ classcd +"'");
                cone = ",";
                flg = true;
            }

            if(!flg) return "";

            rtnStr.append(" ) ");
        	return rtnStr.toString();
        }

        //RECORD_TOTALSTUDYTIME_DATのCLASSCD取得
        private List getRecordTotalstudytimeItemClassList(DB2UDB db2, final String remark2) {
            List rtnList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT");
                stb.append("   CLASSCD ");
                stb.append(" FROM ");
                stb.append("   RECORD_TOTALSTUDYTIME_ITEM_MST ");
                stb.append(" WHERE ");
                stb.append("       SCHOOL_KIND = '"+ _schoolkind +"' ");
                stb.append("   AND SHOW_FLG = '1' ");
                stb.append("   AND REMARK2 = '"+ remark2 +"' "); //1:総合的な学習の時間 2:道徳
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classCd = StringUtils.defaultString(rs.getString("CLASSCD"));
                    rtnList.add(classCd);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnList;
        }

        //RECORD_TOTALSTUDYTIME_DATのDATA_SIZE取得
        private String getRecordTotalstudytimeItemSize(DB2UDB db2, final String remark2, final String columname) {
            String rtnStr = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                //最小のCLASSCDを取得
                stb.append(" WITH MIN_CLASS AS ( ");
                stb.append(" SELECT  ");
                stb.append("   MIN(CLASSCD) AS CLASSCD ");
                stb.append(" FROM ");
                stb.append("   RECORD_TOTALSTUDYTIME_ITEM_MST ");
                stb.append(" WHERE  ");
                stb.append("       SCHOOL_KIND = '"+ _schoolkind +"' ");
                stb.append("   AND SHOW_FLG    = '1' ");
                stb.append("   AND REMARK2     = '"+ remark2 +"' "); //1:総合的な学習の時間 2:道徳
                stb.append("   AND COLUMNNAME  = '"+ columname +"' ");
                stb.append(" ) ");
                //メイン
                stb.append(" SELECT  ");
                stb.append("   T1.*  ");
                stb.append(" FROM ");
                stb.append("   RECORD_TOTALSTUDYTIME_ITEM_MST T1 ");
                stb.append("   INNER JOIN MIN_CLASS T2 ");
                stb.append("           ON T2.CLASSCD = T1.CLASSCD ");
                stb.append(" WHERE  ");
                stb.append("       T1.SCHOOL_KIND = '"+ _schoolkind +"' ");
                stb.append("   AND T1.SHOW_FLG    = '1' ");
                stb.append("   AND T1.REMARK2     = '"+ remark2 +"' "); //1:総合的な学習の時間 2:道徳
                stb.append("   AND T1.COLUMNNAME  = '"+ columname +"' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                	rtnStr = rs.getString("DATA_SIZE");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnStr;
        }
    }
}

// eof

