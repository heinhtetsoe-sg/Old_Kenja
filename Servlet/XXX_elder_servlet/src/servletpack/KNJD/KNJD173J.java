/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 45210a3fc2abb11b56d2e99cc7e1570e1fa43ddb $
 *
 * 作成日: 2019/06/17
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD173J {

    private static final Log log = LogFactory.getLog(KNJD173J.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "89";

    private static final String SDIV1010101 = "1010101"; //1学期中間
    private static final String SDIV1020101 = "1020101"; //1学期期末
    private static final String SDIV1990008 = "1990008"; //1学期評定
    private static final String SDIV2010101 = "2010101"; //2学期中間
    private static final String SDIV2020101 = "2020101"; //2学期期末
    private static final String SDIV2990008 = "2990008"; //2学期評定
    private static final String SDIV3020101 = "3020101"; //3学期期末
    private static final String SDIV3990008 = "3990008"; //3学期評定
    private static final String SDIV9990009 = "9990009"; //学年評定

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

    private static final String HYOTEI_TESTCD = "9990009";

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
        final List studentList = getList(db2);
        //下段の出欠
        for (final Iterator rit = _param._attendRanges.values().iterator(); rit.hasNext();) {
            final DateRange range = (DateRange) rit.next();
            Attendance.load(db2, _param, studentList, range);
        }
        //欠課
        for (final Iterator rit = _param._attendRanges.values().iterator(); rit.hasNext();) {
            final DateRange range = (DateRange) rit.next();
            SubclassAttendance.load(db2, _param, studentList, range);
        }
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();

            //表紙
            printSvfHyoshi(db2, svf, student);

            //通知票
            printSvfMain(db2, svf, student);

            _hasData = true;
        }
    }


    /**
     * 表紙を印刷する
     * @param db2
     * @param svf
     * @param student
     */
    private void printSvfHyoshi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD173J_1.frm", 1);

        if(_param._semes9Flg) {
            svf.VrsOut("CERT_NAME", "修了証"); //修了証
            svf.VrsOut("GRADE", student._gradename); //学年
            final String date = KNJ_EditDate.h_format_JP(db2, _param._date);
            svf.VrsOut("DATE", date); //年度
            svf.VrsOut("SCHOOL_NAME1", _param._certifSchoolSchoolName); //学校名
            svf.VrsOut("JOB_NAME", _param._certifSchoolJobName); //役職名
            svf.VrsOut("PRINCIPAL_NAME", _param._certifSchoolPrincipalName); //校長名
            svf.VrsOut("SCHOOL_STAML", _param._schoolStampPath); //捺印
        } else {
            if (null != _param._whiteSpaceImagePath) {
                svf.VrsOut("BLANK", _param._whiteSpaceImagePath);
            }
        }

        final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._date) + "度";
        svf.VrsOut("NENDO", nendo); //年度
        svf.VrsOut("SCHOOL_NAME2", _param._certifSchoolSchoolName); //学校名
        svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoImagePath);
        svf.VrsOut("HR_NAME", student._hrname + "-" + student._attendno); //年組番
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名

        svf.VrEndPage();
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        svf.VrSetForm("KNJD173J_2.frm", 1);

        //明細部以外を印字
        printTitle(svf, student);

        //明細部
        final List<SubclassMst> subclassList = subclassListRemoveD026(student);
        Collections.sort(subclassList);

        //■定期考査
        int idx = 1;
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclasscd;

            if (!student._printSubclassMap.containsKey(subclassCd)) {
                continue;
            }

            final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);
            if (_param._isOutputDebug) {
                log.info(" score = " + scoreData);
            }

            svf.VrsOutn("CLASS_NAME", idx, subclassMst._classname); //教科名

            //1学期
            svf.VrsOutn("SCORE1_1", idx, scoreData.score(SDIV1010101)); //得点 中間
            svf.VrsOutn("SCORE1_2", idx, scoreData.score(SDIV1020101)); //得点 期末
            svf.VrsOutn("SCORE1_9", idx, scoreData.score(SDIV1990008)); //得点 学期成績
            svf.VrsOutn("AVE1_1", idx, sishaGonyu(scoreData.gradeAvg(SDIV1010101))); //平均 中間
            svf.VrsOutn("AVE1_2", idx, sishaGonyu(scoreData.gradeAvg(SDIV1020101))); //平均 期末
//            svf.VrsOutn("AVE1_9", idx, sishaGonyu(scoreData.gradeAvg(SDIV1990008))); //平均 学期成績

            //2学期
            if(_param._semes2Flg) {
                svf.VrsOutn("SCORE2_1", idx, scoreData.score(SDIV2010101)); //得点 中間
                svf.VrsOutn("SCORE2_2", idx, scoreData.score(SDIV2020101)); //得点 期末
                svf.VrsOutn("SCORE2_9", idx, scoreData.score(SDIV2990008)); //得点 学期成績
                svf.VrsOutn("AVE2_1", idx, sishaGonyu(scoreData.gradeAvg(SDIV2010101))); //平均 中間
                svf.VrsOutn("AVE2_2", idx, sishaGonyu(scoreData.gradeAvg(SDIV2020101))); //平均 期末
//                svf.VrsOutn("AVE2_9", idx, sishaGonyu(scoreData.gradeAvg(SDIV2990008))); //平均 学期成績
            }

            //3学期
            if(_param._semes3Flg) {
                svf.VrsOutn("SCORE3_1", idx, scoreData.score(SDIV3020101)); //得点 期末
                svf.VrsOutn("SCORE3_9", idx, scoreData.score(SDIV3990008)); //得点 学期成績
                svf.VrsOutn("AVE3_1", idx, sishaGonyu(scoreData.gradeAvg(SDIV3020101))); //平均 期末
//                svf.VrsOutn("AVE3_9", idx, sishaGonyu(scoreData.gradeAvg(SDIV3990008))); //平均 学期成績
            }

//            //欠時
//            BigDecimal totalAbsence = new BigDecimal(0.0); //欠時 学年合計
//            BigDecimal totalSemesAbsence = new BigDecimal(0.0); //欠時 学期合計
//            if (student._attendSubClassMap.containsKey(subclassCd)) {
//                final Map attendSubMap = (Map) student._attendSubClassMap.get(subclassCd);
//                for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
//                    totalSemesAbsence = new BigDecimal(0.0); //欠時 学期合計
//                    final String semester = (String) it.next();
//                    if(SEMEALL.equals(semester)) continue;
//                    if(Integer.parseInt(semester) > Integer.parseInt(_param._semester)) continue;
//
//                    if (attendSubMap.containsKey(semester)) {
//                        final Map atSubSemMap= (Map) attendSubMap.get(semester);
//                        for (final Iterator it2 = _param._attendSemesterDetailList.iterator(); it2.hasNext();) {
//                            final SemesterDetail semesDetail = (SemesterDetail) it2.next();
//                            if (atSubSemMap.containsKey(semesDetail._cdSemesterDetail)) {
//                                final SubclassAttendance attendance= (SubclassAttendance) atSubSemMap.get(semesDetail._cdSemesterDetail);
//                                final String detail = "9".equals(semesDetail._cdSemesterDetail) ? "2" : semesDetail._cdSemesterDetail;
//                                svf.VrsOutn("ABSENCE" + semester + "_" + detail, idx, attendance._sick.toString()); //欠時 中間/期末
//
//                                if(!"".equals(attendance._sick.toString()) && attendance._sick != null) {
//                                    totalAbsence.add(attendance._sick);
//                                    totalSemesAbsence.add(attendance._sick);
//                                }
//                            }
//
//                        }
//                    }
//                    svf.VrsOutn("ABSENCE" + semester + "_9", idx, totalSemesAbsence.toString()); //欠時 学期成績
//                }
//            }

            //学年評定
            if(_param._semes9Flg) {
                svf.VrsOutn("SCORE9", idx, scoreData.score(SDIV9990009)); //得点 学年評定
//                svf.VrsOutn("ABSENCE9", idx, totalAbsence.toString()); //欠時 学年評定
            }

            idx++;
            svf.VrEndRecord();
        }

        if(idx == 1) svf.VrEndRecord();

        //■定期考査 合計の印字
        printTotal(svf, student);

        svf.VrEndPage();
    }

    private void printTitle(final Vrw32alp svf, final Student student) {
        //明細部以外を印字

        //ヘッダ,フッタ
        svf.VrsOut("TITLE", _param._nendo + "　通知表"); //タイトル
        svf.VrsOut("HR_NAME", student._hrname + "-" + student._attendno); //年組番
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名
        final String trNameField = KNJ_EditEdit.getMS932ByteLength(student._staffname) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._staffname) > 20 ? "2" : "1";
        svf.VrsOut("TR_NAME" + trNameField, student._staffname); //担任名

        //■総合的学習
        VrsOutnRenban(svf, "TOTAL_STUDY1", KNJ_EditEdit.get_token(student._totalstudytime1, 50, 6)); //総合学習 1学期
        if(_param._semes2Flg) VrsOutnRenban(svf, "TOTAL_STUDY2", KNJ_EditEdit.get_token(student._totalstudytime2, 50, 6)); //総合学習 2学期
        if(_param._semes3Flg) VrsOutnRenban(svf, "TOTAL_STUDY3", KNJ_EditEdit.get_token(student._totalstudytime3, 50, 6)); //総合学習 3学期


        //■クラブ・委員会等
        VrsOutnRenban(svf, "COMITTEE1", KNJ_EditEdit.get_token(student._committee, 34, 2)); //委員・係り
        final String clubField1 = KNJ_EditEdit.getMS932ByteLength(student._club1) > 26 ? "2" : "1";
        final String clubField2 = KNJ_EditEdit.getMS932ByteLength(student._club2) > 26 ? "2" : "1";
        final String clubField3 = KNJ_EditEdit.getMS932ByteLength(student._club3) > 26 ? "2" : "1";
        svf.VrsOutn("CLUB" + clubField1, 1, student._club1); //クラブ・同好会 1学期
        if(_param._semes2Flg) svf.VrsOutn("CLUB" + clubField2, 2, student._club2); //クラブ・同好会 2学期
        if(_param._semes3Flg) svf.VrsOutn("CLUB" + clubField3, 3, student._club3); //クラブ・同好会 3学期

        //■資格
        VrsOutnRenban(svf, "QUALIFY", KNJ_EditEdit.get_token(student._qualification, 30, 8)); //資格

        //■出欠の記録
        printAttend(svf, student);
    }

    private void printTotal(final Vrw32alp svf, final Student student) {
        //■定期考査 合計の印字
        final String subclassCd = "999999";
        final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);
        if(scoreData == null) return;

        //1学期
        svf.VrsOut("TOTAL_SCORE1_1", scoreData.score(SDIV1010101)); //合計得点 中間
        svf.VrsOut("TOTAL_SCORE1_2", scoreData.score(SDIV1020101)); //合計得点 期末
        svf.VrsOut("TOTAL_SCORE1_9", scoreData.score(SDIV1990008)); //合計得点 学期成績
        svf.VrsOut("TOTAL_AVE1_1", sishaGonyu(scoreData.avg(SDIV1010101))); //合計平均 中間
        svf.VrsOut("TOTAL_AVE1_2", sishaGonyu(scoreData.avg(SDIV1020101))); //合計平均 期末
//        svf.VrsOut("TOTAL_AVE1_9", sishaGonyu()); //合計平均 学期成績
//        svf.VrsOut("AVE_SCORE1_1", sishaGonyu()); //平均得点 中間
//        svf.VrsOut("AVE_SCORE1_2", sishaGonyu()); //平均得点 期末
//        svf.VrsOut("AVE_SCORE1_9", sishaGonyu()); //平均得点 学期成績
//        svf.VrsOut("AVE_AVE1_1", sishaGonyu()); //平均得点 中間
//        svf.VrsOut("AVE_AVE1_2", sishaGonyu()); //平均得点 期末
//        svf.VrsOut("AVE_AVE1_9", sishaGonyu()); //平均得点 学期成績
        svf.VrsOut("RANK1_1", scoreData.rank(SDIV1010101)); //順位 中間
        svf.VrsOut("RANK1_2", scoreData.rank(SDIV1020101)); //順位 期末
        svf.VrsOut("RANK1_9", scoreData.rank(SDIV1990008)); //順位 学期成績


        //2学期
        if(_param._semes2Flg) {
            svf.VrsOut("TOTAL_SCORE2_1", scoreData.score(SDIV2010101)); //合計得点 中間
            svf.VrsOut("TOTAL_SCORE2_2", scoreData.score(SDIV2020101)); //合計得点 期末
            svf.VrsOut("TOTAL_SCORE2_9", scoreData.score(SDIV2990008)); //合計得点 学期成績
            svf.VrsOut("TOTAL_AVE2_1", sishaGonyu(scoreData.avg(SDIV2010101))); //合計平均 中間
            svf.VrsOut("TOTAL_AVE2_2", sishaGonyu(scoreData.avg(SDIV2020101))); //合計平均 期末
//            svf.VrsOut("TOTAL_AVE2_9", sishaGonyu()); //合計平均 学期成績
//            svf.VrsOut("AVE_SCORE2_1", sishaGonyu()); //平均得点 中間
//            svf.VrsOut("AVE_SCORE2_2", sishaGonyu()); //平均得点 期末
//            svf.VrsOut("AVE_SCORE2_9", sishaGonyu()); //平均得点 学期成績
//            svf.VrsOut("AVE_AVE2_1", sishaGonyu()); //平均得点 中間
//            svf.VrsOut("AVE_AVE2_2", sishaGonyu()); //平均得点 期末
//            svf.VrsOut("AVE_AVE2_9", sishaGonyu()); //平均得点 学期成績
            svf.VrsOut("RANK2_1", scoreData.rank(SDIV2010101)); //順位 中間
            svf.VrsOut("RANK2_2", scoreData.rank(SDIV2020101)); //順位 期末
            svf.VrsOut("RANK2_9", scoreData.rank(SDIV2990008)); //順位 学期成績
        }

        //3学期
        if(_param._semes3Flg) {
            svf.VrsOut("TOTAL_SCORE3_1", scoreData.score(SDIV3020101)); //合計得点 期末
            svf.VrsOut("TOTAL_SCORE3_9", scoreData.score(SDIV3990008)); //合計得点 学期成績
            svf.VrsOut("TOTAL_AVE3_1", sishaGonyu(scoreData.avg(SDIV3020101))); //合計平均 期末
//            svf.VrsOut("TOTAL_AVE3_9", sishaGonyu()); //合計平均 学期成績
//            svf.VrsOut("AVE_SCORE3_1", sishaGonyu()); //平均得点 期末
//            svf.VrsOut("AVE_SCORE3_9", sishaGonyu()); //平均得点 学期成績
//            svf.VrsOut("AVE_AVE3_1", sishaGonyu()); //平均得点 期末
//            svf.VrsOut("AVE_AVE3_9", sishaGonyu()); //平均得点 学期成績
            svf.VrsOut("RANK3_1", scoreData.rank(SDIV3020101)); //順位 期末
            svf.VrsOut("RANK3_9", scoreData.rank(SDIV3990008)); //順位 学期成績
        }

        //学年評定
        if(_param._semes9Flg) {
//            int totalScore = 0;
//            totalScore += NumberUtils.isDigits(scoreData.score(SDIV1990008)) ? Integer.parseInt(scoreData.score(SDIV1990008)) : 0;
//            totalScore += NumberUtils.isDigits(scoreData.score(SDIV2990008)) ? Integer.parseInt(scoreData.score(SDIV2990008)) : 0;
//            totalScore += NumberUtils.isDigits(scoreData.score(SDIV3990008)) ? Integer.parseInt(scoreData.score(SDIV3990008)) : 0;
//            svf.VrsOut("TOTAL_SCORE9", String.valueOf(totalScore)); //合計得点
//            svf.VrsOut("AVE_SCORE9", String.valueOf()); //平均得点
        }
    }

    private List<SubclassMst> subclassListRemoveD026(final Student student) {
        final List<SubclassMst> retList = new ArrayList(_param._subclassMstMap.values());
        for (final Iterator<SubclassMst> it = retList.iterator(); it.hasNext();) {
            final SubclassMst subclassMst = it.next();
            boolean remove = false;
            if (_param._d026List.contains(subclassMst._subclasscd)) {
                if (_param._isOutputDebug) {
                    log.info(" no print d026 : " + subclassMst._subclasscd + ":" + subclassMst._subclassname);
                }
                remove = true;
            }
            if (_param._isNoPrintMoto &&  subclassMst.isMoto(student._grade, student._course)) {
                if (_param._isOutputDebug) {
                    log.info(" no print moto : " + subclassMst._subclasscd + ":" + subclassMst._subclassname);
                }
                remove = true;
            }
            if (!_param._isPrintSakiKamoku &&  subclassMst.isSaki(student._grade, student._course)) {
                if (_param._isOutputDebug) {
                    log.info(" no print saki : " + subclassMst._subclasscd + ":" + subclassMst._subclassname);
                }
                remove = true;
            }
            if (remove) {
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

    // 出欠記録
    private void printAttend(final Vrw32alp svf, final Student student) {
        for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next();
            final int line = getSemeLine(semester);
            final Attendance att = (Attendance) student._attendMap.get(semester);
            int lesson = 0;
            int suspend = 0;
            int absent = 0;
            int present = 0;
            int late = 0;
            int early = 0;
            if (null != att) {
                lesson = att._lesson;
                suspend = att._suspend + att._mourning;
                absent = att._absent;
                present = att._present;
                late = att._late;
                early = att._early;
            }
            if(line == 2 && !_param._semes2Flg) continue;
            if(line == 3 && !_param._semes3Flg) continue;
            svf.VrsOutn("LESSON", line, String.valueOf(lesson));   // 授業日数
            svf.VrsOutn("SUSPEND", line, String.valueOf(suspend)); // 忌引出停日数
            svf.VrsOutn("ABSENT", line, String.valueOf(absent));   // 欠席日数
            svf.VrsOutn("PRESENT", line, String.valueOf(present)); // 出席日数
            svf.VrsOutn("LATE", line, String.valueOf(late));       // 遅刻
            svf.VrsOutn("EARLY", line, String.valueOf(early));     // 早退
        }
    }

    private int getSemeLine(final String semester) {
        final int line;
        if (SEMEALL.equals(semester)) {
            line = 4;
        } else {
            line = Integer.parseInt(semester);
        }
        return line;
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._schoolKind = rs.getString("SCHOOL_KIND");
                student._gradename = rs.getString("GRADE_NAME2");
                student._hrname = rs.getString("HR_NAME");
                student._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                student._staffname2 = StringUtils.defaultString(rs.getString("STAFFNAME2"));
                student._attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番" : rs.getString("ATTENDNO");
                student._grade = rs.getString("GRADE");
                student._hrClass = rs.getString("HR_CLASS");
                student._coursecd = rs.getString("COURSECD");
                student._majorcd = rs.getString("MAJORCD");
                student._course = rs.getString("COURSE");
                student._majorname = rs.getString("MAJORNAME");
                student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                student._entyear = rs.getString("ENT_YEAR");
                student._totalstudytime1 = StringUtils.defaultString(rs.getString("TOTALSTUDYTIME1"));
                student._totalstudytime2 = StringUtils.defaultString(rs.getString("TOTALSTUDYTIME2"));
                student._totalstudytime3 = StringUtils.defaultString(rs.getString("TOTALSTUDYTIME3"));
                student._committee= StringUtils.defaultString(rs.getString("COMMITTEE"));
                student._club1 = StringUtils.defaultString(rs.getString("CLUB1"));
                student._club2 = StringUtils.defaultString(rs.getString("CLUB2"));
                student._club3 = StringUtils.defaultString(rs.getString("CLUB3"));
                student._qualification = StringUtils.defaultString(rs.getString("QUALIFICATION"));

                student.setSubclass(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH SCHNO_A AS(");
        stb.append("    SELECT  T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
        stb.append("    FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2 ");
        stb.append("    WHERE   T1.YEAR = '" + _param._loginYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND T1.SEMESTER = '" + _param._loginSemester + "' ");
        } else {
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("        AND T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
        if ("1".equals(_param._disp)) {
            stb.append("    AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("    AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }

        //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
        //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + _param._date + "' THEN T2.EDATE ELSE '" + _param._date + "' END) ");
        stb.append("                             OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + _param._date + "' THEN T2.EDATE ELSE '" + _param._date + "' END)) ) ");
//        //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
//        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
//        stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//        stb.append(                           "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + parameter._date + "' THEN T2.EDATE ELSE '" + parameter._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        //                      異動者チェック：休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND S1.TRANSFERCD IN ('2') AND CASE WHEN T2.EDATE < '" + _param._date + "' THEN T2.EDATE ELSE '" + _param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append("    ) ");

        //メイン表
        stb.append("     SELECT  REGD.SCHREGNO");
        stb.append("            ,REGD.SEMESTER ");
        stb.append("            ,BASE.NAME ");
        stb.append("            ,REGDG.SCHOOL_KIND ");
        stb.append("            ,REGDG.GRADE_NAME2 ");
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
        stb.append("            ,R1.TOTALSTUDYTIME AS TOTALSTUDYTIME1 ");
        stb.append("            ,R2.TOTALSTUDYTIME AS TOTALSTUDYTIME2 ");
        stb.append("            ,R3.TOTALSTUDYTIME AS TOTALSTUDYTIME3 ");
        stb.append("            ,RD01.REMARK1 AS COMMITTEE ");
        stb.append("            ,RD021.REMARK1 AS CLUB1 ");
        stb.append("            ,RD022.REMARK1 AS CLUB2 ");
        stb.append("            ,RD023.REMARK1 AS CLUB3 ");
        stb.append("            ,RD03.REMARK1 AS QUALIFICATION ");
        stb.append("     FROM    SCHNO_A REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDG.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
        stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON MAJOR.COURSECD = REGD.COURSECD ");
        stb.append("                  AND MAJOR.MAJORCD = REGD.MAJORCD ");
        stb.append("     LEFT JOIN STAFF_MST STF2 ON STF2.STAFFCD = REGDH.TR_CD2 ");
        stb.append("     LEFT JOIN HREPORTREMARK_DAT R1 ");
        stb.append("            ON R1.YEAR     = REGD.YEAR ");
        stb.append("           AND R1.SEMESTER = '1' ");
        stb.append("           AND R1.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN HREPORTREMARK_DAT R2 ");
        stb.append("            ON R2.YEAR     = REGD.YEAR ");
        stb.append("           AND R2.SEMESTER = '2' ");
        stb.append("           AND R2.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN HREPORTREMARK_DAT R3 ");
        stb.append("            ON R3.YEAR     = REGD.YEAR ");
        stb.append("           AND R3.SEMESTER = '3' ");
        stb.append("           AND R3.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD01 ");
        stb.append("            ON RD01.YEAR     = REGD.YEAR ");
        stb.append("           AND RD01.SEMESTER = '" + SEMEALL + "' ");
        stb.append("           AND RD01.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD01.DIV      = '01' ");
        stb.append("           AND RD01.CODE     = '01' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD021 ");
        stb.append("            ON RD021.YEAR     = REGD.YEAR ");
        stb.append("           AND RD021.SEMESTER = '1' ");
        stb.append("           AND RD021.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD021.DIV      = '01' ");
        stb.append("           AND RD021.CODE     = '02' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD022 ");
        stb.append("            ON RD022.YEAR     = REGD.YEAR ");
        stb.append("           AND RD022.SEMESTER = '2' ");
        stb.append("           AND RD022.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD022.DIV      = '01' ");
        stb.append("           AND RD022.CODE     = '02' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD023 ");
        stb.append("            ON RD023.YEAR     = REGD.YEAR ");
        stb.append("           AND RD023.SEMESTER = '3' ");
        stb.append("           AND RD023.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD023.DIV      = '01' ");
        stb.append("           AND RD023.CODE     = '02' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD03 ");
        stb.append("            ON RD03.YEAR     = REGD.YEAR ");
        stb.append("           AND RD03.SEMESTER = '" + SEMEALL + "' ");
        stb.append("           AND RD03.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD03.DIV      = '01' ");
        stb.append("           AND RD03.CODE     = '03' ");

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

    private class Student {
        String _schregno;
        String _name;
        String _schoolKind;
        String _gradename;
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
        String _totalstudytime1;
        String _totalstudytime2;
        String _totalstudytime3;
        String _committee;
        String _club1;
        String _club2;
        String _club3;
        String _qualification;
        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map _attendSubClassMap = new HashMap();

        private void setSubclass(final DB2UDB db2) {
            final String scoreSql = prestatementSubclass();
            if (_param._isOutputDebug) {
                log.info(" scoreSql = " + scoreSql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String subclassname = rs.getString("SUBCLASSNAME");

                    String subclasscd = rs.getString("SUBCLASSCD");
                    if (!"999999".equals(subclasscd)) {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    }

                    final String key = subclasscd;
                    if (!_printSubclassMap.containsKey(key)) {
                        _printSubclassMap.put(key, new ScoreData(classcd, classname, subclasscd, subclassname));
                    }
                    final ScoreData scoreData = (ScoreData) _printSubclassMap.get(key);
                    final String testcd = rs.getString("SEMESTER") + rs.getString("TESTKINDCD") + rs.getString("TESTITEMCD") + rs.getString("SCORE_DIV");
                    scoreData._scoreMap.put(testcd, StringUtils.defaultString(rs.getString("SCORE")));
                    scoreData._gradeAvgMap.put(testcd, StringUtils.defaultString(rs.getString("GRADE_AVG")));
                    scoreData._avgMap.put(testcd, StringUtils.defaultString(rs.getString("AVG")));
                    if (ALL9.equals(subclasscd)) {
                        scoreData._avgAvgMap.put(testcd, StringUtils.defaultString(rs.getString("AVG_AVG")));
                    }
                    scoreData._rankMap.put(testcd, StringUtils.defaultString(rs.getString("GRADE_RANK")));
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

            final String[] sdivs = {SDIV1010101, SDIV1020101, SDIV2010101, SDIV2020101, SDIV3020101, SDIV1990008, SDIV2990008, SDIV3990008, SDIV9990009};
            final StringBuffer divStr = new StringBuffer();
            divStr.append(" ( ");
            String or = "";
            for (int i = 0; i < sdivs.length; i++) {
                final String semester = sdivs[i].substring(0, 1);
                final String testkindcd = sdivs[i].substring(1, 3);
                final String testitemcd = sdivs[i].substring(3, 5);
                final String scorediv = sdivs[i].substring(5);
                divStr.append(or).append(" SEMESTER = '" + semester + "' AND TESTKINDCD = '" + testkindcd + "' AND TESTITEMCD = '" + testitemcd + "' AND SCORE_DIV = '" + scorediv + "' ");
                or = " OR ";
            }
            divStr.append(" ) ");


            stb.append(" WITH SCHNO AS( ");
            //学籍の表
            stb.append(" SELECT ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     T2.SCHREGNO, ");
            stb.append("     T2.GRADE, ");
            stb.append("     T2.HR_CLASS, ");
            stb.append("     T2.ATTENDNO, ");
            stb.append("     T2.COURSECD, ");
            stb.append("     T2.MAJORCD, ");
            stb.append("     T2.COURSECODE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T2 ");
            stb.append(" WHERE ");
            stb.append("         T2.YEAR    = '" + _param._loginYear + "'  ");
            stb.append("     AND T2.GRADE    = '" + _grade + "'  ");
            stb.append("     AND T2.HR_CLASS = '" + _hrClass + "'  ");
            stb.append("     AND T2.SCHREGNO = '" + _schregno + "'  ");
            stb.append("     AND T2.SEMESTER = (SELECT ");
            stb.append("                          MAX(SEMESTER) ");
            stb.append("                        FROM ");
            stb.append("                          SCHREG_REGD_DAT W2 ");
            stb.append("                        WHERE ");
            stb.append("                          W2.YEAR = '" + _param._loginYear + "'  ");
            stb.append("                          AND W2.SEMESTER <= '" + _param._semester + "'  ");
            stb.append("                          AND W2.SCHREGNO = T2.SCHREGNO ");
            stb.append("                     ) ");
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
            stb.append("     S1.YEAR         = '" + _param._loginYear + "' ");
            stb.append("     AND S1.SEMESTER <= '" + _param._semester + "' ");
            stb.append("     AND S2.YEAR     = S1.YEAR          ");
            stb.append("     AND S2.SEMESTER <= '" + _param._semester + "' ");
            stb.append("     AND S2.SEMESTER = S1.SEMESTER          ");
            stb.append("     AND S2.CHAIRCD  = S1.CHAIRCD          ");
            stb.append("     AND EXISTS(SELECT ");
            stb.append("                  'X' ");
            stb.append("                FROM ");
            stb.append("                  SCHNO S3 ");
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
            //成績明細データの表
            stb.append(" ) ,RECORD00 AS( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("     FROM RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     INNER JOIN (SELECT ");
            stb.append("                  SCHREGNO ");
            stb.append("                FROM ");
            stb.append("                  SCHNO T2 ");
            stb.append("                GROUP BY ");
            stb.append("                  SCHREGNO ");
            stb.append("             ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._loginYear + "'  ");
            stb.append("     AND (" + divStr + ") ");
            stb.append("     AND T1.SUBCLASSCD NOT LIKE '50%' ");
            stb.append("     AND T1.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");

            stb.append(" ) ,RECORD0 AS( ");
            stb.append("     SELECT ");
            stb.append("              T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("            , L2.SCORE ");
            stb.append("            , L2.AVG ");
            stb.append("            , L2.GRADE_RANK ");
            stb.append("            , L3.AVG AS GRADE_AVG ");
            stb.append("     FROM RECORD00 T1 ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L2 ");
            stb.append("            ON L2.YEAR          = T1.YEAR ");
            stb.append("           AND L2.SEMESTER      = T1.SEMESTER ");
            stb.append("           AND L2.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("           AND L2.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("           AND L2.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("           AND L2.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND L2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L2.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT L3 ");
            stb.append("            ON L3.YEAR          = T1.YEAR ");
            stb.append("           AND L3.SEMESTER      = T1.SEMESTER ");
            stb.append("           AND L3.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("           AND L3.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("           AND L3.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("           AND L3.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND L3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND L3.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L3.AVG_DIV       = '1' ");
            stb.append("           AND L3.GRADE         = '" + _grade + "' ");
            stb.append("           AND L3.HR_CLASS      = '000' ");
            stb.append("           AND L3.COURSECD      = '0' ");
            stb.append("           AND L3.MAJORCD       = '000' ");
            stb.append("           AND L3.COURSECODE    = '0000' ");
            stb.append(" ) ,RECORD AS( ");
            stb.append("     SELECT ");
            stb.append("       T3.CLASSNAME, ");
            stb.append("       T4.SUBCLASSNAME, ");
            stb.append("       T1.*, ");
            stb.append("       CAST(NULL AS DOUBLE) AS AVG_AVG ");
            stb.append("     FROM RECORD0 T1 ");
            stb.append("     INNER JOIN CHAIR_A T5 ");
            stb.append("        ON T5.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("       AND T5.SCHREGNO      = T1.SCHREGNO ");
            stb.append("       AND T5.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND T5.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     INNER JOIN CLASS_MST T3 ");
            stb.append("        ON T3.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
            stb.append("       AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("     INNER JOIN SUBCLASS_MST T4 ");
            stb.append("        ON T4.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("       AND T4.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("   UNION ALL ");
            stb.append("     SELECT ");
            stb.append("       CAST(NULL AS VARCHAR(1)) AS CLASSNAME, ");
            stb.append("       CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
            stb.append("       T1.*, ");
            stb.append("       T2.AVG_AVG ");
            stb.append("      FROM RECORD0 T1 ");
            stb.append("      LEFT JOIN ( ");
            stb.append("            SELECT   T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, AVG(T1.AVG) AS AVG_AVG ");
            stb.append("            FROM RECORD_RANK_SDIV_DAT T1 ");
            stb.append("            INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("                AND REGD.YEAR = T1.YEAR ");
            if (SEMEALL.equals(_param._semester)) {
                stb.append("            AND REGD.SEMESTER = '" + _param._loginSemester + "' ");
            } else {
                stb.append("            AND REGD.SEMESTER = T1.SEMESTER ");
            }
            stb.append("            WHERE ");
            stb.append("                T1.YEAR = '" + _param._loginYear + "' ");
            stb.append("                AND T1.SUBCLASSCD = '" + ALL9 + "' ");
            stb.append("            GROUP BY T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV  ");
            stb.append("      ) T2 ON T2.YEAR = T1.YEAR ");
            stb.append("          AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("          AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("          AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("          AND T2.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("      WHERE T1.SUBCLASSCD = '" + ALL9 + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("       T1.* ");
            stb.append(" FROM RECORD T1 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SUBCLASSCD ");
            stb.append("    ,T1.SCHOOL_KIND ");
            stb.append("    ,T1.CURRICULUM_CD ");

            return stb.toString();
        }
    }

    private class JviewRecord {
        final String _subclassCd;
        final String _semester;
        final String _viewCd;
        final String _status;
        final String _statusName;
        final String _score;
        final String _hyouka;
        private JviewRecord(
                final String subclassCd,
                final String semester,
                final String viewCd,
                final String status,
                final String statusName,
                final String score,
                final String hyouka
        ) {
            _subclassCd = subclassCd;
            _semester = semester;
            _viewCd = viewCd;
            _status = status;
            _statusName = statusName;
            _score = score;
            _hyouka = hyouka;
        }
    }

    private class RankSdiv {
        final String _score;
        final String _hyouka;
        private RankSdiv(
                final String score,
                final String hyouka
        ) {
            _score = score;
            _hyouka = hyouka;
        }
    }

    private static class ScoreData {
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final Map _scoreMap = new HashMap(); // 得点
        final Map _avgMap = new HashMap(); // 個人の平均点 (999999のみ使用)
        final Map _avgAvgMap = new HashMap(); // 個人の平均点の学年平均 (999999のみ使用)
        final Map _gradeAvgMap = new HashMap(); // 学年平均
        final Map _rankMap = new HashMap(); // 順位

        private ScoreData(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String subclassname
        ) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
        }

        public String score(final String sdiv) {
            return StringUtils.defaultString((String) _scoreMap.get(sdiv), "****");
        }

        public String avg(final String sdiv) {
            return (String) _avgMap.get(sdiv);
        }

        public String gradeAvg(final String sdiv) {
            return (String) _gradeAvgMap.get(sdiv);
        }

        public String rank(final String sdiv) {
            return (String) _rankMap.get(sdiv);
        }

        public String toString() {
            return "ScoreData(" + _subclasscd + ":" + _subclassname + ", scoreMap = " + _scoreMap + ")";
        }
    }

    private static class Attendance {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _abroad;
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
                final List studentList,
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

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

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
                final List studentList,
                final DateRange dateRange) {
            log.info(" subclass attendance dateRange = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }

            for (final Iterator it2 = param._attendSemesterDetailList.iterator(); it2.hasNext();) {
                final SemesterDetail semesDetail = (SemesterDetail) it2.next();
                if (null == semesDetail) {
                    continue;
                }

                final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    param._attendParamMap.put("schregno", "?");

                    final String sql = AttendAccumulate.getAttendSubclassSql(
                            param._loginYear,
                            dateRange._key,
                            dateRange._sdate,
                            edate,
                            param._attendParamMap
                    );

                    ps = db2.prepareStatement(sql);

                    for (final Iterator it3 = studentList.iterator(); it3.hasNext();) {
                        final Student student = (Student) it3.next();

                        ps.setString(1, student._schregno);
                        rs = ps.executeQuery();

                        while (rs.next()) {
                            if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                                continue;
                            }
                            final String subclasscd = rs.getString("SUBCLASSCD");

                            final SubclassMst mst = (SubclassMst) param._subclassMstMap.get(subclasscd);
                            if (null == mst) {
                                log.warn("no subclass : " + subclasscd);
                                continue;
                            }
                            final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                            if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                                if (null == student._printSubclassMap.get(subclasscd)) {
                                    continue;
                                }

                                final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                                final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                                final BigDecimal sick = rs.getBigDecimal("SICK2");
                                final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                                final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");
                                final BigDecimal late = rs.getBigDecimal("LATE");
                                final BigDecimal early = rs.getBigDecimal("EARLY");

                                final BigDecimal sick1 = mst.isSaki(student._grade, student._course) ? rawReplacedSick : rawSick;
                                final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                                final BigDecimal sick2 = mst.isSaki(student._grade, student._course) ? replacedSick : sick;

                                final BigDecimal absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");

                                final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2, late, early);

                                //欠課時数上限
                                final Double absent = Double.valueOf(mst.isSaki(student._grade, student._course) ? rs.getString("REPLACED_SICK"): rs.getString("SICK2"));
                                subclassAttendance._isOver = subclassAttendance.judgeOver(absent, absenceHigh);

                                Map setMap = null;
                                Map setSubAttendMap = null;

                                if (student._attendSubClassMap.containsKey(subclasscd)) {
                                    setSubAttendMap = (Map) student._attendSubClassMap.get(subclasscd);
                                    if (setSubAttendMap.containsKey(dateRange._key)) {
                                        setMap = (Map) setSubAttendMap.get(dateRange._key);
                                    } else {
                                        setMap = new TreeMap();
                                    }
                                } else {
                                    setSubAttendMap = new TreeMap();
                                    setMap = new TreeMap();
                                }

//                                if (student._attendSubClassMap.containsKey(subclasscd)) {
//                                    setSubAttendMap = (Map) student._attendSubClassMap.get(subclasscd);
//                                } else {
//                                    setSubAttendMap = new TreeMap();
//                                }

                                setMap.put(semesDetail._cdSemesterDetail, subclassAttendance);

                                setSubAttendMap.put(dateRange._key, setMap);

                                student._attendSubClassMap.put(subclasscd, setSubAttendMap);
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
        final DateRange _dateRange;
        final List _testItemList;
        final List _semesterDetailList;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
            _testItemList = new ArrayList();
            _semesterDetailList = new ArrayList();
        }
        public int getTestItemIdx(final TestItem testItem) {
            return _testItemList.indexOf(testItem);
        }
        public int getSemesterDetailIdx(final SemesterDetail semesterDetail) {
//          log.debug(" semesterDetail = " + semesterDetail + " , " + _semesterDetailList.indexOf(semesterDetail));
          return _semesterDetailList.indexOf(semesterDetail);
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Semester)) {
                return 0;
            }
            Semester s = (Semester) o;
            return _semester.compareTo(s._semester);
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
//        final boolean _isSaki;
//        final boolean _isMoto;
//        final String _calculateCreditFlg;
        final List<String> _gradeCourseAttendList = new ArrayList<String>();
        final List<String> _gradeCourseCombinedList = new ArrayList<String>();
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3
//                , final boolean isSaki, final boolean isMoto, final String calculateCreditFlg
                ) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
//            _isSaki = isSaki;
//            _isMoto = isMoto;
//            _calculateCreditFlg = calculateCreditFlg;
        }
        public boolean isMoto(final String grade, final String course) {
            return _gradeCourseAttendList.contains(grade + course);
        }
        public boolean isSaki(final String grade, final String course) {
            return _gradeCourseCombinedList.contains(grade + course);
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

    private static class SemesterDetail implements Comparable {
        final Semester _semester;
        final String _cdSemesterDetail;
        final String _semestername;
        final String _sdate;
        final String _edate;
        public SemesterDetail(Semester semester, String cdSemesterDetail, String semestername, final String sdate, final String edate) {
            _semester = semester;
            _cdSemesterDetail = cdSemesterDetail;
            _semestername = StringUtils.defaultString(semestername);
            _sdate = sdate;
            _edate = edate;
        }
        public int compareTo(final Object o) {
            if (!(o instanceof SemesterDetail)) {
                return 0;
            }
            SemesterDetail sd = (SemesterDetail) o;
            int rtn;
            rtn = _semester.compareTo(sd._semester);
            if (rtn != 0) {
                return rtn;
            }
            rtn = _cdSemesterDetail.compareTo(sd._cdSemesterDetail);
            return rtn;
        }
        public String toString() {
            return "SemesterDetail(" + _semester._semester + ", " + _cdSemesterDetail + ", " + _semestername + ")";
        }
    }

    private static class TestItem {
        final String _year;
        final Semester _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _testitemname;
        final String _testitemabbv1;
        final String _sidouInput;
        final String _sidouInputInf;
        final SemesterDetail _semesterDetail;
        boolean _isGakunenKariHyotei;
        int _printKettenFlg; // -1: 表示しない（仮評定）、1: 値が1をカウント（学年評定）、2:換算した値が1をカウント（評価等）
        public TestItem(final String year, final Semester semester, final String testkindcd, final String testitemcd, final String scoreDiv,
                final String testitemname, final String testitemabbv1, final String sidouInput, final String sidouInputInf,
                final SemesterDetail semesterDetail) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _testitemname = testitemname;
            _testitemabbv1 = testitemabbv1;
            _sidouInput = sidouInput;
            _sidouInputInf = sidouInputInf;
            _semesterDetail = semesterDetail;
        }
        public String getTestcd() {
            return _semester._semester +_testkindcd +_testitemcd + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + _semester._semester + _testkindcd + _testitemcd + "(" + _scoreDiv + "))";
        }
    }


    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75783 $");
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
        final String _loginSemester;
        final String _loginYear;
        final String _prgid;
        final String _semester;
        final String _nendo;
        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private final List _semesterList;
        private Map _semesterMap;
        private final Map _semesterDetailMap;
        private Map<String, SubclassMst> _subclassMstMap;
        private List _d026List = new ArrayList();
        private Map _attendRanges;

        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _backSlashImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolStampPath;

        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;

        private boolean _isOutputDebug;

        private final List _attendTestKindItemList;
        private final List _attendSemesterDetailList;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _disp = request.getParameter("DISP");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            if ("1".equals(_disp)) {
                _grade = request.getParameter("GRADE");
            } else {
                _grade = _gradeHrClass.substring(0, 2);
            }
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginYear = request.getParameter("YEAR");
            _prgid = request.getParameter("PRGID");
            _semester = request.getParameter("SEMESTER");
            _semes2Flg = "2".equals(_semester) || "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes3Flg = "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes9Flg = "9".equals(_semester) ? true : false;
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_loginYear)) + "年度";
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


            _semesterList = getSemesterList(db2);
            _semesterMap = loadSemester(db2);
            _semesterDetailMap = new HashMap();
            _attendRanges = new HashMap();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final Semester oSemester = (Semester) _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }
            setSubclassMst(db2);

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            _schoolStampPath = getImageFilePath("SCHOOLSTAMP.bmp");

            _attendTestKindItemList = getTestKindItemList(db2, false, false);
            _attendSemesterDetailList = getAttendSemesterDetailList();

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD173J' AND NAME = '" + propName + "' "));
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
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

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
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
//                sql += " WITH REPL AS ( ";
//                sql += " SELECT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD, CAST(NULL AS VARCHAR(1)) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _loginYear + "' GROUP BY COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
//                sql += " UNION ";
//                sql += " SELECT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _loginYear + "' GROUP BY ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
//                sql += " ) ";
                sql += " SELECT ";
                sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += " T1.CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
//                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
//                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO, ";
//                sql += " L2.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3 ";
                sql += " FROM SUBCLASS_MST T1 ";
//                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
//                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                sql += " WHERE T1.SCHOOL_KIND = 'J' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
//                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
//                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3"))); // , isSaki, isMoto, rs.getString("CALCULATE_CREDIT_FLG"));
                    _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            String sql = "";
            sql += " SELECT ";
            sql += " T1.GRADE, ";
            sql += " T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ";
            sql += " T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
            sql += " T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
            sql += " FROM SUBCLASS_WEIGHTING_COURSE_DAT T1 ";
            sql += " WHERE T1.YEAR = '" + _loginYear + "' ";
            sql += "   AND T1.FLG = '2' ";
            sql += "   AND T1.GRADE = '" + _grade + "' ";

            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String course = KnjDbUtils.getString(row, "COURSE");
                final String combinedSubclasscd = KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD");
                final String attendSubclasscd = KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD");
                final SubclassMst combined = _subclassMstMap.get(combinedSubclasscd);
                final SubclassMst attend = _subclassMstMap.get(attendSubclasscd);
                if (null == combined) {
                    log.info(" null combined : " + combinedSubclasscd);
                }
                if (null == attend) {
                    log.info(" null attend : " + attendSubclasscd);
                }
                attend._gradeCourseAttendList.add(grade + course);
                combined._gradeCourseCombinedList.add(grade + course);
            }
//            final Map<String, List<String>> gradeCourseAttendSubclasscdListMap = new TreeMap<String, List<String>>();
//            final Map<String, List<String>> gradeCourseCombineSubclasscdListMap = new TreeMap<String, List<String>>();
//            for (final SubclassMst mst : _subclassMstMap.values()) {
//            	if (!mst._gradeCourseAttendList.isEmpty()) {
//
//            	}
//            	if (!mst._gradeCourseCombinedList.isEmpty()) {
//
//            	}
//            }
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _loginYear + "' AND CERTIF_KINDCD = '104' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
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
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _loginYear + "' AND NAMECD1 = 'DJ16' AND NAMECD2 = '01' ";
            List<Map<String, String>> rowList = KnjDbUtils.query(db2, sql);
            if (!rowList.isEmpty()) {
                final Map row = KnjDbUtils.firstRow(rowList);
                if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
                log.info("(名称マスタDJ16):元科目を表示しない = " + _isNoPrintMoto);
                return;
            }

            final String sql2 = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _loginYear + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql2));
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

        private List getSemesterList(DB2UDB db2) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _loginYear + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    Semester semes = new Semester(semester, semestername, sdate, edate);
                    list.add(semes);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private List getTestKindItemList(DB2UDB db2, final boolean useSubclassControl, final boolean addSemester) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map semesterMap = new HashMap();
                for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                    final String seme = (String) it.next();
                    if(!_semesterMap.containsKey(seme)) continue;
                    final Semester semester = (Semester) _semesterMap.get(seme);
                    semesterMap.put(semester._semester, semester);
                }
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH ADMIN_CONTROL_SDIV_SUBCLASSCD AS (");
                if (useSubclassControl) {
                    stb.append("   SELECT DISTINCT ");
                    stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                    stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                    stb.append("   WHERE T1.YEAR = '" + _loginYear + "' ");
                    stb.append("   UNION ALL ");
                }
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                stb.append("   WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '00-00-00-000000' ");
                stb.append("   UNION ALL ");
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                stb.append("   WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("     AND T1.CLASSCD = '00' ");
                stb.append("     AND T1.CURRICULUM_CD = '00' ");
                stb.append("     AND T1.SUBCLASSCD = '000000' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.SCORE_DIV, ");
                stb.append("     T1.TESTITEMNAME, ");
                stb.append("     T1.TESTITEMABBV1, ");
                stb.append("     T1.SIDOU_INPUT, ");
                stb.append("     T1.SIDOU_INPUT_INF, ");
                stb.append("     T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T2.SEMESTER_DETAIL, ");
                stb.append("     T2.SEMESTER AS SEMESTER_DETAIL_SEMESTER, ");
                stb.append("     T2.SEMESTERNAME AS SEMESTERDETAILNAME, ");
                stb.append("     T2.SDATE, ");
                stb.append("     T2.EDATE ");
                stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                stb.append(" INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                stb.append("    AND T11.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T11.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("    AND T11.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("    AND T11.SCORE_DIV = T1.SCORE_DIV ");
                stb.append(" LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ");
                stb.append(" WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("   AND T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD IN "); // 指定の科目が登録されていれば登録された科目、登録されていなければ00-00-00-000000を使用する
                stb.append("    (SELECT MAX(SUBCLASSCD) FROM ADMIN_CONTROL_SDIV_SUBCLASSCD) ");
                stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");

                log.debug(" testitem sql ="  + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                String adminSdivSubclasscd = null;
                while (rs.next()) {
                    adminSdivSubclasscd = rs.getString("SUBCLASSCD");
                    final String year = rs.getString("YEAR");
                    final String testkindcd = rs.getString("TESTKINDCD");
                    final String testitemcd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String sidouInput = rs.getString("SIDOU_INPUT");
                    final String sidouInputInf = rs.getString("SIDOU_INPUT_INF");
                    Semester semester = (Semester) semesterMap.get(rs.getString("SEMESTER"));
                    if (null == semester) {
                        continue;
                    }
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final String testitemabbv1 = rs.getString("TESTITEMABBV1");
                    SemesterDetail semesterDetail = null;
                    final String cdsemesterDetail = rs.getString("SEMESTER_DETAIL");
                    if (null != cdsemesterDetail) {
                        if (_semesterDetailMap.containsKey(cdsemesterDetail)) {
                            semesterDetail = (SemesterDetail) _semesterDetailMap.get(cdsemesterDetail);
                        } else {
                            final String semesterdetailname = rs.getString("SEMESTERDETAILNAME");
                            final String sdate = rs.getString("SDATE");
                            final String edate = rs.getString("EDATE");
                            semesterDetail = new SemesterDetail(semester, cdsemesterDetail, semesterdetailname, sdate, edate);
                            Semester semesDetailSemester = (Semester) semesterMap.get(rs.getString("SEMESTER_DETAIL_SEMESTER"));
                            if (null != semesDetailSemester) {
                                semesDetailSemester._semesterDetailList.add(semesterDetail);
                            }
                            _semesterDetailMap.put(semesterDetail._cdSemesterDetail, semesterDetail);
                        }
                    }
                    final TestItem testItem = new TestItem(
                            year, semester, testkindcd, testitemcd, scoreDiv, testitemname, testitemabbv1, sidouInput, sidouInputInf,
                            semesterDetail);
                    final boolean isGakunenHyotei = HYOTEI_TESTCD.equals(testItem.getTestcd());
                    if (isGakunenHyotei) {
                        testItem._printKettenFlg = 1;
                    } else if (!SEMEALL.equals(semester._semester) && "09".equals(scoreDiv)) { // 9学期以外の09=9学期以外の仮評定
                        testItem._printKettenFlg = -1;
                    } else {
                        testItem._printKettenFlg = 2;
                    }
                    if (isGakunenHyotei) {
                        final TestItem testItemKari = new TestItem(
                                year, semester, testkindcd, testitemcd, scoreDiv, "仮評定", "仮評定", sidouInput, sidouInputInf,
                                semesterDetail);
                        testItemKari._printKettenFlg = -1;
                        testItemKari._isGakunenKariHyotei = true;
                        if (addSemester) {
                            semester._testItemList.add(testItemKari);
                        }
                        list.add(testItemKari);
                    }
                    if (addSemester) {
                        semester._testItemList.add(testItem);
                    }
                    list.add(testItem);
                }
                log.debug(" testitem admin_control_sdiv_dat subclasscd = " + adminSdivSubclasscd);

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.debug(" testcd = " + list);
            return list;
        }

        private List getAttendSemesterDetailList() {
            final Map rtn = new TreeMap();
            for (final Iterator it = _attendTestKindItemList.iterator(); it.hasNext();) {
                final TestItem item = (TestItem) it.next();
                if (null != item._semesterDetail && null != item._semesterDetail._cdSemesterDetail) {
                    rtn.put(item._semesterDetail._cdSemesterDetail, item._semesterDetail);
                }
            }
            Semester semester9 = null;
            for (final Iterator it = _semesterList.iterator(); it.hasNext();) {
                final Semester semester = (Semester) it.next();
                if (semester._semester.equals(SEMEALL)) {
                    semester9 = semester;
                    final SemesterDetail semesterDetail9 = new SemesterDetail(semester9, SEMEALL, "学年", semester9._dateRange._sdate, semester9._dateRange._edate);
                    semester9._semesterDetailList.add(semesterDetail9);
                    rtn.put(SEMEALL, semesterDetail9);
                    break;
                }
            }
            return new ArrayList(rtn.values());
        }


    }
}

// eof
