/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id$
 *
 * 作成日: 2019/06/27
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

public class KNJD184R {

    private static final Log log = LogFactory.getLog(KNJD184R.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "89";

    private static final String SDIV1990008 = "1990008"; //1学期評定
    private static final String SDIV2990008 = "2990008"; //2学期評定
    private static final String SDIV3990008 = "3990008"; //3学期評定
    private static final String SDIV9990008 = "9990008"; //学年評定

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

            //通知票
            printSvfMain(db2, svf, student);
        }
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String frmName = "J".equals(_param._schoolKind) ? "KNJD184R_1.frm" : "KNJD184R_2.frm";
        svf.VrSetForm(frmName, 1);
        //明細部以外を印字
        printTitle(db2, svf, student);

        //明細部
        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        //■学習の記録
        int idx = 1;
        final List<String> totalScore1 = new ArrayList<String>();
        final List<String> totalScore2 = new ArrayList<String>();
        final List<String> totalScore3 = new ArrayList<String>();
        final List<String> totalScore9 = new ArrayList<String>();
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclasscd;

            if (!student._printSubclassMap.containsKey(subclassCd)) {
                continue;
            }

            final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);

            final String subField = KNJ_EditEdit.getMS932ByteLength(subclassMst._subclassname) > 10 ? "2" : "1";
            svf.VrsOutn("SUBCLASS_NAME" + subField, idx, subclassMst._subclassname); //科目名

            //1学期
            svf.VrsOutn("VAL1", idx, scoreData._score1); //評定

            //2学期
            if(_param._semes2Flg) svf.VrsOutn("VAL2", idx, scoreData._score2); //評定

            //3学期
            if(_param._semes3Flg) svf.VrsOutn("VAL3", idx, scoreData._score3); //評定

            //学年評定
            if(_param._semes9Flg) svf.VrsOutn("VAL9", idx, scoreData._score9); //評定


            //学期事の合計
            totalScore1.add(scoreData._score1);
            totalScore2.add(scoreData._score2);
            totalScore3.add(scoreData._score3);
            totalScore9.add(scoreData._score9);

            //欠時
            if (student._attendSubClassMap.containsKey(subclassCd)) {
                final Map attendSubMap = (Map) student._attendSubClassMap.get(subclassCd);
                for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
                    final String semester = (String) it.next();
                    if("2".equals(semester) && !_param._semes2Flg) continue;
                    if("3".equals(semester) && !_param._semes3Flg) continue;
                    if("9".equals(semester) && !_param._semes9Flg) continue;
                    if(attendSubMap.containsKey(semester)) {
                        final SubclassAttendance attendance= (SubclassAttendance) attendSubMap.get(semester);
                        if(attendance != null && attendance._sick != null && attendance._sick.intValue() != 0) {
                            svf.VrsOutn("KEKKA" + semester, idx, attendance._sick.toString()); //欠時
                        }
                    }
                }
            }

            idx++;
            svf.VrEndRecord();
        }

        //平均の印字
        if(idx > 1) {
            final String avg1 = average(totalScore1);
            final String avg2 = average(totalScore2);
            final String avg3 = average(totalScore3);
            final String avg9 = average(totalScore9);
            svf.VrsOut("AVE_VAL1", avg1); //1学期
            if(_param._semes2Flg) svf.VrsOut("AVE_VAL2", avg2); //2学期
            if(_param._semes3Flg) svf.VrsOut("AVE_VAL3", avg3); //3学期
            if(_param._semes9Flg) svf.VrsOut("AVE_VAL9", avg9); //学年末
        }

        if(idx == 1) svf.VrEndRecord();

        svf.VrEndPage();
        _hasData = true;
    }

    private static String average(List<String> totalScore) {
        int count = 0;
        BigDecimal sum = new BigDecimal(0);
        for (final String score : totalScore) {
            if (!NumberUtils.isNumber(score)) {
                continue;
            }
            sum = sum.add(new BigDecimal(score));
            count += 1;
        }
        if (count == 0) {
            return null;
        }
        return sum.divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printTitle(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        //明細部以外を印字

        //ヘッダー
        final String nendo = StringUtils.defaultString(_param._loginYear) + "年度";
        svf.VrsOut("NENDO", nendo); //年度
        final String hrName = student._gradename + student._hrname + student._attendno;
        svf.VrsOut("HR_NAME", hrName); //年組番
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 22 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名

        //■道徳の評価
        if("J".equals(_param._schoolKind)) {
            printMoral(svf, student);
        }

        //■探究テーマ
        svf.VrsOut("RESEARCH1", student._theme1);
        svf.VrsOut("RESEARCH2", student._theme2);

        //■特別活動の記録
        //生徒会活動
        svf.VrsOutn("SP_ACT1", 1, student._sp_act1);
        svf.VrsOutn("SP_ACT2", 1, student._sp_act2);
        svf.VrsOutn("SP_ACT3", 1, student._sp_act3);

        //学級の係活動
        svf.VrsOutn("SP_ACT1", 2, student._dependent1);
        svf.VrsOutn("SP_ACT2", 2, student._dependent2);
        svf.VrsOutn("SP_ACT3", 2, student._dependent3);

        //部活動
        svf.VrsOutn("SP_ACT1", 3, student._club1);
        svf.VrsOutn("SP_ACT2", 3, student._club2);
        svf.VrsOutn("SP_ACT3", 3, student._club3);

        //その他
        svf.VrsOutn("SP_ACT1", 4, student._remark1);
        svf.VrsOutn("SP_ACT2", 4, student._remark2);
        svf.VrsOutn("SP_ACT3", 4, student._remark3);
        if("H".equals(_param._schoolKind)) {
            svf.VrsOutn("SP_ACT1", 5, student._remark4);
            svf.VrsOutn("SP_ACT2", 5, student._remark5);
            svf.VrsOutn("SP_ACT3", 5, student._remark6);
        }

        if("J".equals(_param._schoolKind)) {
            //■所見
            final int pcharsttSE = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_J, 0);
            final int plinesttSE = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_J, 1);
            final int charsttSE = (-1 == pcharsttSE || -1 == plinesttSE) ? 16 : pcharsttSE; //文字数
            final int linesttSE = (-1 == pcharsttSE || -1 == plinesttSE) ? 10 : plinesttSE; //行数
            VrsOutnRenban(svf, "VIEW1", KNJ_EditEdit.get_token(student._communication1, charsttSE * 2, linesttSE)); //1学期
            if(_param._semes2Flg) VrsOutnRenban(svf, "VIEW2", KNJ_EditEdit.get_token(student._communication2, charsttSE * 2, linesttSE)); //2学期
            if(_param._semes3Flg) VrsOutnRenban(svf, "VIEW3", KNJ_EditEdit.get_token(student._communication3, charsttSE * 2, linesttSE)); //3学期
        }

        //■出欠席の記録
        printAttend(svf, student);

        //担任名
        if("H".equals(_param._schoolKind)) {
            final String schoolName = _param._certifSchoolRemark3 + "　学級担任";
            svf.VrsOut("SCHOOL_NAME2", schoolName); //学校名他
            final String trNameField = KNJ_EditEdit.getMS932ByteLength(student._staffname) > 30 ? "2" : "1";
            svf.VrsOut("TR_NAME" + trNameField, student._staffname); //担任名
            //担任印
            final String path = _param.getStaffImageFilePath(student._trCd1);
            if (null != path) {
                svf.VrsOut("STAFFBTM_1C", path); //1学期
                if(_param._semes2Flg) svf.VrsOut("STAFFBTM_2C", path); //2学期
                if(_param._semes3Flg) svf.VrsOut("STAFFBTM_3C", path); //3学期
            }
        }

        if(_param._semes9Flg) {
            //■修了証書
            svf.VrsOut("GRADE", NumberUtils.isDigits(student._gradeCd) ? String.valueOf(Integer.parseInt(student._gradeCd)) : student._gradeCd); //学年
            final String date = KNJ_EditDate.h_format_SeirekiJP(_param._descDate);
            svf.VrsOut("DATE", date);    //日付
            final String schoolName = _param._certifSchoolRemark8 + "　" +  _param._certifSchoolSchoolName + "　" + _param._certifSchoolPrincipalName; //法人名 学校名 校長名
            svf.VrsOut("SCHOOL_NAME", schoolName); //学校名称等
        } else {
            if (null != _param._whiteSpaceImagePath) {
                svf.VrsOut("BLANK", _param._whiteSpaceImagePath);
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
            if (_param._isNoPrintMoto &&  subclassMst._isMoto || !_param._isPrintSakiKamoku &&  subclassMst._isSaki) {
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

    /**
     * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
     * @param param サイズタイプのパラメータ文字列
     * @param pos split後のインデクス (0:w, 1:h)
     * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
     */
    private static int getParamSizeNum(final String param, final int pos) {
        int num = -1;
        String[] nums = StringUtils.split(param, " * ");
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

    // 出欠記録
    private void printAttend(final Vrw32alp svf, final Student student) {
        for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next();
            final int line = getSemeLine(semester);
            final Attendance att = (Attendance) student._attendMap.get(semester);
            if(line == 2 && !_param._semes2Flg) continue;
            if(line == 3 && !_param._semes3Flg) continue;
            String remark = line == 1 ? student._attendrec_remark1 : line == 2 ? student._attendrec_remark2 : line == 3 ? student._attendrec_remark3 : student._attendrec_remark9;
            if (null != att) {
                if(String.valueOf(att._lesson)  != null) svf.VrsOutn("LESSON", line, String.valueOf(att._lesson));   // 授業日数
                if(String.valueOf(att._suspend) != null && String.valueOf(att._mourning) != null)
                    svf.VrsOutn("KIBIKI", line, String.valueOf(att._suspend + att._mourning)); // 出席停止忌引等
                if(String.valueOf(att._mLesson) != null) svf.VrsOutn("PRESENT", line, String.valueOf(att._mLesson)); // 出席すべき日数
                if(String.valueOf(att._absent)  != null) svf.VrsOutn("ABSENCE", line, String.valueOf(att._absent));  // 欠席日数
                if(String.valueOf(att._late)    != null) svf.VrsOutn("LATE", line, String.valueOf(att._late));       // 遅刻
                if(String.valueOf(att._early)   != null) svf.VrsOutn("LEAVE", line, String.valueOf(att._early));     // 早退

                if(att._absent == 0 && att._late == 0  && att._early == 0) remark = "皆勤　" + remark;
            } else {
                svf.VrsOutn("LESSON", line, "0");   // 授業日数
                svf.VrsOutn("KIBIKI", line, "0");   // 出席停止忌引等
                svf.VrsOutn("PRESENT", line, "0");  //出席すべき日数
                svf.VrsOutn("ABSENCE", line, "0");  // 欠席日数
                svf.VrsOutn("LATE", line, "0");     // 遅刻
                svf.VrsOutn("LEAVE", line, "0");    // 早退
            }
            //備考
            final String remarkField = KNJ_EditEdit.getMS932ByteLength(remark) > 20 ? "2" : "1";
            svf.VrsOutn("REMARK" + remarkField, line, remark);
        }
    }

    /**
     * 『道徳』を印字する
     * @param svf
     * @param student
     */
    private void printMoral(final Vrw32alp svf, final Student student) {
        String moral1 = "";
        String moral2 = "";
        String moral3 = "";
        int charsttSE = 10; //道徳 文字数
        int linesttSE =  8; //道徳 行数
        for (final Iterator it = student._recordTotalstudytimeDatList.iterator(); it.hasNext();) {
            final RecordTotalstudytimeDat recordTotalstudytimeDat = (RecordTotalstudytimeDat) it.next();

            if(!"".equals(recordTotalstudytimeDat._data_size)) {
                final int pcharsttSE = getParamSizeNum(recordTotalstudytimeDat._data_size, 0);
                final int plinesttSE = getParamSizeNum(recordTotalstudytimeDat._data_size, 1);
                charsttSE = (-1 == pcharsttSE || -1 == plinesttSE) ? 10 : pcharsttSE; //文字数
                linesttSE = (-1 == pcharsttSE || -1 == plinesttSE) ?  8 : plinesttSE; //行数
            }

            //道徳を結合
            if(!"".equals(recordTotalstudytimeDat._moral1)) moral1 = !"".equals(moral1)? moral1 + "," + recordTotalstudytimeDat._moral1 : recordTotalstudytimeDat._moral1;
            if(!"".equals(recordTotalstudytimeDat._moral2)) moral2 = !"".equals(moral2)? moral2 + "," + recordTotalstudytimeDat._moral2 : recordTotalstudytimeDat._moral2;
            if(!"".equals(recordTotalstudytimeDat._moral3)) moral3 = !"".equals(moral3)? moral3 + "," + recordTotalstudytimeDat._moral3 : recordTotalstudytimeDat._moral3;

        }
        VrsOutnRenban(svf, "MORAL1", KNJ_EditEdit.get_token(moral1, charsttSE * 2, linesttSE)); //道徳 1学期
        if(_param._semes2Flg) VrsOutnRenban(svf, "MORAL2", KNJ_EditEdit.get_token(moral2, charsttSE * 2, linesttSE)); //道徳 2学期
        if(_param._semes3Flg) VrsOutnRenban(svf, "MORAL3", KNJ_EditEdit.get_token(moral3, charsttSE * 2, linesttSE)); //道徳 3学期

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
                student._gradeCd = rs.getString("GRADE_CD");
                student._hrname = !"".equals(rs.getString("HR_NAME")) ? rs.getString("HR_NAME") + "組" : "";
                student._trCd1 = StringUtils.defaultString(rs.getString("TR_CD1"));
                student._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                student._attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番" : rs.getString("ATTENDNO");
                student._grade = rs.getString("GRADE");
                if (NumberUtils.isDigits(_param._minGrade) && NumberUtils.isDigits(student._grade)) {
                    student._gradename = "第" + (NumberUtils.isDigits(student._grade) ? String.valueOf(Integer.parseInt(student._grade) - Integer.parseInt(_param._minGrade) + 1) : StringUtils.defaultString(student._grade, " ")) + "学年";
                } else {
                    student._gradename = rs.getString("GRADE_NAME2");
                }
                student._hrClass = rs.getString("HR_CLASS");
                student._coursecd = rs.getString("COURSECD");
                student._majorcd = rs.getString("MAJORCD");
                student._course = rs.getString("COURSE");
                student._majorname = rs.getString("MAJORNAME");
                student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                student._entyear = rs.getString("ENT_YEAR");
                student._theme1 = StringUtils.defaultString(rs.getString("THEME1"));
                student._theme2 = StringUtils.defaultString(rs.getString("THEME2"));
                student._sp_act1 = StringUtils.defaultString(rs.getString("SP_ACT1"));
                student._sp_act2 = StringUtils.defaultString(rs.getString("SP_ACT2"));
                student._sp_act3 = StringUtils.defaultString(rs.getString("SP_ACT3"));
                student._dependent1 = StringUtils.defaultString(rs.getString("DEPENDENT1"));
                student._dependent2 = StringUtils.defaultString(rs.getString("DEPENDENT2"));
                student._dependent3 = StringUtils.defaultString(rs.getString("DEPENDENT3"));
                student._club1 = StringUtils.defaultString(rs.getString("CLUB1"));
                student._club2 = StringUtils.defaultString(rs.getString("CLUB2"));
                student._club3 = StringUtils.defaultString(rs.getString("CLUB3"));
                student._remark1 = StringUtils.defaultString(rs.getString("REMARK1"));
                student._remark2 = StringUtils.defaultString(rs.getString("REMARK2"));
                student._remark3 = StringUtils.defaultString(rs.getString("REMARK3"));
                student._remark4 = StringUtils.defaultString(rs.getString("REMARK4"));
                student._remark5 = StringUtils.defaultString(rs.getString("REMARK5"));
                student._remark6 = StringUtils.defaultString(rs.getString("REMARK6"));
                student._communication1 = StringUtils.defaultString(rs.getString("COMMUNICATION1"));
                student._communication2 = StringUtils.defaultString(rs.getString("COMMUNICATION2"));
                student._communication3 = StringUtils.defaultString(rs.getString("COMMUNICATION3"));
                student._attendrec_remark1 = StringUtils.defaultString(rs.getString("ATTENDREC_REMARK1"));
                student._attendrec_remark2 = StringUtils.defaultString(rs.getString("ATTENDREC_REMARK2"));
                student._attendrec_remark3 = StringUtils.defaultString(rs.getString("ATTENDREC_REMARK3"));
                student._attendrec_remark9 = StringUtils.defaultString(rs.getString("ATTENDREC_REMARK9"));

                student.setSubclass(db2);
                student._recordTotalstudytimeDatList = RecordTotalstudytimeDat.getRecordTotalstudytimeDat(db2, _param, student._schregno);
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
        stb.append("            ,REGDG.GRADE_CD ");
        stb.append("            ,REGDG.GRADE_NAME2 ");
        stb.append("            ,REGDH.HR_CLASS_NAME1 AS HR_NAME ");
        stb.append("            ,REGDH.TR_CD1 ");
        stb.append("            ,STF1.STAFFNAME ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,REGD.GRADE ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("            ,REGD.COURSECD ");
        stb.append("            ,REGD.MAJORCD ");
        stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
        stb.append("            ,MAJOR.MAJORNAME ");
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
        stb.append("             ,HRD0201.REMARK1 AS THEME1 ");
        stb.append("             ,HRD0201.REMARK2 AS THEME2 ");
        stb.append("             ,HRD0102.REMARK1 AS SP_ACT1 ");
        stb.append("             ,HRD0102.REMARK2 AS SP_ACT2 ");
        stb.append("             ,HRD0102.REMARK3 AS SP_ACT3 ");
        stb.append("             ,HRD0101.REMARK1 AS DEPENDENT1 ");
        stb.append("             ,HRD0101.REMARK2 AS DEPENDENT2 ");
        stb.append("             ,HRD0101.REMARK3 AS DEPENDENT3 ");
        stb.append("             ,HRD0103.REMARK1 AS CLUB1 ");
        stb.append("             ,HRD0103.REMARK2 AS CLUB2 ");
        stb.append("             ,HRD0103.REMARK3 AS CLUB3 ");
        stb.append("             ,HRD0105.REMARK1 ");
        stb.append("             ,HRD0105.REMARK2 ");
        stb.append("             ,HRD0105.REMARK3 ");
        stb.append("             ,HRD0107.REMARK1 AS REMARK4 ");
        stb.append("             ,HRD0107.REMARK2 AS REMARK5 ");
        stb.append("             ,HRD0107.REMARK3 AS REMARK6 ");
        stb.append("             ,HRR1.COMMUNICATION AS COMMUNICATION1 ");
        stb.append("             ,HRR2.COMMUNICATION AS COMMUNICATION2 ");
        stb.append("             ,HRR3.COMMUNICATION AS COMMUNICATION3 ");
        stb.append("             ,HRR1.ATTENDREC_REMARK AS ATTENDREC_REMARK1 ");
        stb.append("             ,HRR2.ATTENDREC_REMARK AS ATTENDREC_REMARK2 ");
        stb.append("             ,HRR3.ATTENDREC_REMARK AS ATTENDREC_REMARK3 ");
        stb.append("             ,HRR9.ATTENDREC_REMARK AS ATTENDREC_REMARK9 ");
        stb.append("      FROM    SCHNO_A REGD ");
        stb.append("      LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
        stb.append("                   AND REGDG.GRADE = REGD.GRADE ");
        stb.append("      LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("                   AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("                   AND REGDH.GRADE = REGD.GRADE ");
        stb.append("                   AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("      LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("      LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
        stb.append("      LEFT JOIN MAJOR_MST MAJOR ON MAJOR.COURSECD = REGD.COURSECD ");
        stb.append("                   AND MAJOR.MAJORCD = REGD.MAJORCD ");
        stb.append("      LEFT JOIN HREPORTREMARK_DETAIL_DAT HRD0201 ");
        stb.append("             ON HRD0201.YEAR     = REGD.YEAR ");
        stb.append("            AND HRD0201.SEMESTER = '9' ");
        stb.append("            AND HRD0201.SCHREGNO = REGD.SCHREGNO ");
        stb.append("            AND HRD0201.DIV      = '02' ");
        stb.append("            AND HRD0201.CODE     = '01' ");
        stb.append("      LEFT JOIN HREPORTREMARK_DETAIL_DAT HRD0102 ");
        stb.append("             ON HRD0102.YEAR     = REGD.YEAR ");
        stb.append("            AND HRD0102.SEMESTER = '9' ");
        stb.append("            AND HRD0102.SCHREGNO = REGD.SCHREGNO ");
        stb.append("            AND HRD0102.DIV      = '01' ");
        stb.append("            AND HRD0102.CODE     = '02' ");
        stb.append("      LEFT JOIN HREPORTREMARK_DETAIL_DAT HRD0101 ");
        stb.append("             ON HRD0101.YEAR     = REGD.YEAR ");
        stb.append("            AND HRD0101.SEMESTER = '9' ");
        stb.append("            AND HRD0101.SCHREGNO = REGD.SCHREGNO ");
        stb.append("            AND HRD0101.DIV      = '01' ");
        stb.append("            AND HRD0101.CODE     = '01' ");
        stb.append("      LEFT JOIN HREPORTREMARK_DETAIL_DAT HRD0103 ");
        stb.append("             ON HRD0103.YEAR     = REGD.YEAR ");
        stb.append("            AND HRD0103.SEMESTER = '9' ");
        stb.append("            AND HRD0103.SCHREGNO = REGD.SCHREGNO ");
        stb.append("            AND HRD0103.DIV      = '01' ");
        stb.append("            AND HRD0103.CODE     = '03' ");
        stb.append("      LEFT JOIN HREPORTREMARK_DETAIL_DAT HRD0105 ");
        stb.append("             ON HRD0105.YEAR     = REGD.YEAR ");
        stb.append("            AND HRD0105.SEMESTER = '9' ");
        stb.append("            AND HRD0105.SCHREGNO = REGD.SCHREGNO ");
        stb.append("            AND HRD0105.DIV      = '01' ");
        stb.append("            AND HRD0105.CODE     = '05' ");
        stb.append("      LEFT JOIN HREPORTREMARK_DETAIL_DAT HRD0107 ");
        stb.append("             ON HRD0107.YEAR     = REGD.YEAR ");
        stb.append("            AND HRD0107.SEMESTER = '9' ");
        stb.append("            AND HRD0107.SCHREGNO = REGD.SCHREGNO ");
        stb.append("            AND HRD0107.DIV      = '01' ");
        stb.append("            AND HRD0107.CODE     = '07' ");
        stb.append("      LEFT JOIN HREPORTREMARK_DAT HRR1 ");
        stb.append("             ON HRR1.YEAR     = REGD.YEAR ");
        stb.append("            AND HRR1.SEMESTER = '1' ");
        stb.append("            AND HRR1.SCHREGNO = REGD.SCHREGNO ");
        stb.append("      LEFT JOIN HREPORTREMARK_DAT HRR2 ");
        stb.append("             ON HRR2.YEAR     = REGD.YEAR ");
        stb.append("            AND HRR2.SEMESTER = '2' ");
        stb.append("            AND HRR2.SCHREGNO = REGD.SCHREGNO ");
        stb.append("      LEFT JOIN HREPORTREMARK_DAT HRR3 ");
        stb.append("             ON HRR3.YEAR     = REGD.YEAR ");
        stb.append("            AND HRR3.SEMESTER = '3' ");
        stb.append("            AND HRR3.SCHREGNO = REGD.SCHREGNO ");
        stb.append("      LEFT JOIN HREPORTREMARK_DAT HRR9 ");
        stb.append("             ON HRR9.YEAR     = REGD.YEAR ");
        stb.append("            AND HRR9.SEMESTER = '9' ");
        stb.append("            AND HRR9.SCHREGNO = REGD.SCHREGNO ");
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
        String _gradeCd;
        String _gradename;
        String _hrname;
        String _trCd1;
        String _staffname;
        String _attendno;
        String _grade;
        String _hrClass;
        String _coursecd;
        String _majorcd;
        String _course;
        String _majorname;
        String _hrClassName1;
        String _entyear;
        String _theme1;
        String _theme2;
        String _sp_act1;
        String _sp_act2;
        String _sp_act3;
        String _dependent1;
        String _dependent2;
        String _dependent3;
        String _club1;
        String _club2;
        String _club3;
        String _remark1;
        String _remark2;
        String _remark3;
        String _remark4;
        String _remark5;
        String _remark6;
        String _communication1;
        String _communication2;
        String _communication3;
        String _attendrec_remark1;
        String _attendrec_remark2;
        String _attendrec_remark3;
        String _attendrec_remark9;

        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map _attendSubClassMap = new HashMap();
        List _recordTotalstudytimeDatList = Collections.EMPTY_LIST; // 道徳

        public Student() {
        }

        private void setSubclass(final DB2UDB db2) {
            final String scoreSql = prestatementSubclass();
            log.debug(" sql = " + scoreSql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String score1 = rs.getString("SCORE1");
                    final String score2 = rs.getString("SCORE2");
                    final String score3 = rs.getString("SCORE3");
                    final String score9 = rs.getString("SCORE9");

                    final String key = subclasscd;
                    ScoreData scoreData = new ScoreData(classcd, classname, subclasscd, subclassname, score1, score2, score3, score9);
                    if (_printSubclassMap.containsKey(key)) {
                        scoreData = (ScoreData) _printSubclassMap.get(key);
                    }
                    _printSubclassMap.put(key, scoreData);
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
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" S2.CLASSCD, ");
                stb.append(" S2.SCHOOL_KIND, ");
                stb.append(" S2.CURRICULUM_CD, ");
            }
            stb.append("     S2.SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT S1, ");
            stb.append("     CHAIR_DAT S2 ");
            stb.append(" WHERE ");
            stb.append("     S1.YEAR         = '" + _param._loginYear + "' ");
            stb.append("     AND S1.SEMESTER <= '" + _param._semester + "' ");
            stb.append("     AND S2.YEAR     = S1.YEAR          ");
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
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" S2.CLASSCD, ");
                stb.append(" S2.SCHOOL_KIND, ");
                stb.append(" S2.CURRICULUM_CD, ");
            }
            stb.append("     S2.SUBCLASSCD ");
            //成績明細データの表
            stb.append(" ) ,RECORD AS( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     L2.SCORE AS SCORE1, ");
            stb.append("     L3.SCORE AS SCORE2, ");
            stb.append("     L4.SCORE AS SCORE3, ");
            stb.append("     L5.SCORE AS SCORE9 ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L2 ");
            stb.append("            ON L2.YEAR          = T1.YEAR ");
            stb.append("           AND (L2.SEMESTER, L2.TESTKINDCD, L2.TESTITEMCD, L2.SCORE_DIV) = ('" + SDIV1990008.substring(0, 1) + "', '" + SDIV1990008.substring(1, 3) + "', '" + SDIV1990008.substring(3, 5) + "', '" + SDIV1990008.substring(5, 7) + "') ");
            if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND L2.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L2.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L3 ");
            stb.append("            ON L3.YEAR          = T1.YEAR ");
            stb.append("           AND (L3.SEMESTER, L3.TESTKINDCD, L3.TESTITEMCD, L3.SCORE_DIV) = ('" + SDIV2990008.substring(0, 1) + "', '" + SDIV2990008.substring(1, 3) + "', '" + SDIV2990008.substring(3, 5) + "', '" + SDIV2990008.substring(5, 7) + "') ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND L3.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L3.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L3.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L4 ");
            stb.append("            ON L4.YEAR          = T1.YEAR ");
            stb.append("           AND (L4.SEMESTER, L4.TESTKINDCD, L4.TESTITEMCD, L4.SCORE_DIV) = ('" + SDIV3990008.substring(0, 1) + "', '" + SDIV3990008.substring(1, 3) + "', '" + SDIV3990008.substring(3, 5) + "', '" + SDIV3990008.substring(5, 7) + "') ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND L4.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L4.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L4.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L5 ");
            stb.append("            ON L5.YEAR          = T1.YEAR ");
            stb.append("           AND (L5.SEMESTER, L5.TESTKINDCD, L5.TESTITEMCD, L5.SCORE_DIV) = ('" + SDIV9990008.substring(0, 1) + "', '" + SDIV9990008.substring(1, 3) + "', '" + SDIV9990008.substring(3, 5) + "', '" + SDIV9990008.substring(5, 7) + "') ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND L5.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L5.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L5.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L5.SCHREGNO      = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._loginYear + "'  ");
            stb.append("     AND T1.SCHREGNO IN (SELECT ");
            stb.append("                  T2.SCHREGNO ");
            stb.append("                FROM ");
            stb.append("                  SCHNO T2 ");
            stb.append("                GROUP BY ");
            stb.append("                  SCHREGNO ");
            stb.append("             )          ");
            stb.append("     AND T1.SUBCLASSCD NOT LIKE '50%' ");
            stb.append("     AND T1.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");
            //メイン表1
            stb.append(" ) ,T_MAIN AS( ");
            stb.append(" SELECT ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     T3.CLASSNAME, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" T4.SCHOOL_KIND, ");
                stb.append(" T4.CURRICULUM_CD, ");
            }
            stb.append("     T4.SUBCLASSCD, ");
            stb.append("     T4.SUBCLASSNAME, ");
            stb.append("     T1.SCORE1, ");
            stb.append("     T1.SCORE2, ");
            stb.append("     T1.SCORE3, ");
            stb.append("     T1.SCORE9 ");
            stb.append(" FROM ");
            stb.append("     SCHNO T2 ");
            stb.append("     LEFT JOIN RECORD T1 ");
            stb.append("            ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     INNER JOIN CHAIR_A T5 ");
            stb.append("            ON T5.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND T5.SCHREGNO      = T1.SCHREGNO ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       AND T5.CLASSCD       = T1.CLASSCD ");
                stb.append("       AND T5.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("       AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     INNER JOIN CLASS_MST T3 ");
            stb.append("            ON T3.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            }
            stb.append("     INNER JOIN SUBCLASS_MST T4 ");
            stb.append("            ON T4.SUBCLASSCD    = T1.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       AND T4.CLASSCD       = T1.CLASSCD ");
                stb.append("       AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("       AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" ) ");
            //メイン表2
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.CLASSNAME, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            }else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     T1.SUBCLASSNAME, ");
            stb.append("     T1.SCORE1, ");
            stb.append("     T1.SCORE2, ");
            stb.append("     T1.SCORE3, ");
            stb.append("     T1.SCORE9 ");
            stb.append(" FROM ");
            stb.append("     T_MAIN T1 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" ,T1.SCHOOL_KIND ");
                stb.append(" ,T1.CURRICULUM_CD ");
            }

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

    private class ScoreData {
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final String _score1;
        final String _score2;
        final String _score3;
        final String _score9;
        private ScoreData(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String subclassname,
                final String score1,
                final String score2,
                final String score3,
                final String score9
        ) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _score9 = score9;
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
        final int _det006;
        final int _det007;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int abroad,
                final int det006,
                final int det007
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
            _det006 = det006;
            _det007 = det007;
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
            PreparedStatement psAtDetail = null;
            ResultSet rsAtDetail = null;
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

                final String detailSql = getDetailSql(param, dateRange);
                psAtDetail = db2.prepareStatement(detailSql);
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    psAtDetail.setString(1, student._schregno);
                    psAtDetail.setString(2, student._schregno);
                    rsAtDetail = psAtDetail.executeQuery();

                    int set006 = 0;
                    int set007 = 0;
                    while (rsAtDetail.next()) {
                        set006 = rsAtDetail.getInt("CNT006");
                        set007 = rsAtDetail.getInt("CNT007");
                    }
                    DbUtils.closeQuietly(rsAtSeme);

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
                                rsAtSeme.getInt("TRANSFER_DATE"),
                                set006,
                                set007
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

        private static String getDetailSql(final Param param, final DateRange dateRange) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCH_T(SCHREGNO) AS ( ");
            stb.append("     VALUES(CAST(? AS VARCHAR(8))) ");
            stb.append(" ), DET_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     SEQ, ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(CNT) AS CNT ");
            stb.append(" FROM ");
            stb.append("     ATTEND_SEMES_DETAIL_DAT ");
            stb.append(" WHERE ");
            stb.append("     COPYCD = '0' ");
            stb.append("     AND YEAR || MONTH BETWEEN '" + param._loginYear + "04' AND '" + (Integer.parseInt(param._loginYear) + 1) + "03' ");
            stb.append("     AND SEMESTER = '" + dateRange._key + "' ");
            stb.append("     AND SCHREGNO = ? ");
            stb.append("     AND SEQ IN ('006', '007') ");
            stb.append(" GROUP BY ");
            stb.append("     SEQ, ");
            stb.append("     SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     VALUE(DET006.CNT, 0) AS CNT006, ");
            stb.append("     VALUE(DET007.CNT, 0) AS CNT007 ");
            stb.append(" FROM ");
            stb.append("     SCH_T ");
            stb.append("     LEFT JOIN DET_T DET006 ON SCH_T.SCHREGNO = DET006.SCHREGNO ");
            stb.append("          AND DET006.SEQ = '006' ");
            stb.append("     LEFT JOIN DET_T DET007 ON SCH_T.SCHREGNO = DET007.SCHREGNO ");
            stb.append("          AND DET007.SEQ = '007' ");

            return stb.toString();
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

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

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

                            final BigDecimal sick1 = mst._isSaki ? rawReplacedSick : rawSick;
                            final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                            final BigDecimal sick2 = mst._isSaki ? replacedSick : sick;

                            final BigDecimal absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");

                            final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2, late, early);

                            //欠課時数上限
                            final Double absent = Double.valueOf(mst._isSaki ? rs.getString("REPLACED_SICK"): rs.getString("SICK2"));
                            subclassAttendance._isOver = subclassAttendance.judgeOver(absent, absenceHigh);

                            Map setSubAttendMap = null;
                            if (student._attendSubClassMap.containsKey(subclasscd)) {
                                setSubAttendMap = (Map) student._attendSubClassMap.get(subclasscd);
                            } else {
                                setSubAttendMap = new TreeMap();
                            }
                            setSubAttendMap.put(dateRange._key, subclassAttendance);

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

    private static class SubclassMst implements Comparable {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final boolean _isSaki;
        final boolean _isMoto;
        final String _calculateCreditFlg;
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final boolean isSaki, final boolean isMoto, final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _isSaki = isSaki;
            _isMoto = isMoto;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public int compareTo(final Object o) {
            final SubclassMst mst = (SubclassMst) o;
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

    /**
     * 道徳
     */
    private static class RecordTotalstudytimeDat {
        final String _subclasscd;
        final String _moral1;
        final String _moral2;
        final String _moral3;
        final String _data_size;

        public RecordTotalstudytimeDat(
                final String subclasscd,
                final String moral1,
                final String moral2,
                final String moral3,
                final String data_size
                ) {
            _subclasscd = subclasscd;
            _moral1 = moral1;
            _moral2 = moral2;
            _moral3 = moral3;
            _data_size = data_size;

        }

        public static List getRecordTotalstudytimeDat(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getRecordTotalstudytimeDatSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String moral1 = StringUtils.defaultString(rs.getString("MORAl1"));
                    final String moral2 = StringUtils.defaultString(rs.getString("MORAl2"));
                    final String moral3 = StringUtils.defaultString(rs.getString("MORAl3"));
                    final String data_size = StringUtils.defaultString(rs.getString("DATA_SIZE"));
                    final RecordTotalstudytimeDat recordTotalstudytimeDat = new RecordTotalstudytimeDat(subclasscd, moral1, moral2, moral3, data_size);
                    list.add(recordTotalstudytimeDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getRecordTotalstudytimeDatSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            //道徳
            stb.append(" WITH MORAL AS( ");
            stb.append(" SELECT ");
            stb.append("     T2.YEAR, ");
            stb.append("     T2.SEMESTER, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD, ");
                stb.append("     T2.SCHOOL_KIND, ");
                stb.append("     T2.CURRICULUM_CD, ");
            }
            stb.append("     T2.SUBCLASSCD, ");
            stb.append("     T2.SCHREGNO, ");
            stb.append("     T2.TOTALSTUDYTIME ");
            stb.append(" FROM  ");
            stb.append("     RECORD_TOTALSTUDYTIME_ITEM_MST T1 ");
            stb.append("     INNER JOIN RECORD_TOTALSTUDYTIME_DAT T2 ");
            stb.append("            ON T2.CLASSCD     = T1.CLASSCD ");
            stb.append("           AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("     T1.REMARK2 = '2' ");
            //メイン表
            stb.append(" ) ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     L2.TOTALSTUDYTIME AS MORAl1, ");
            stb.append("     L3.TOTALSTUDYTIME AS MORAl2, ");
            stb.append("     L4.TOTALSTUDYTIME AS MORAl3, ");
            stb.append("     L5.DATA_SIZE ");
            stb.append(" FROM ");
            stb.append("     MORAL T1 ");
            stb.append("     LEFT JOIN MORAL L2 ");
            stb.append("            ON L2.YEAR          = T1.YEAR ");
            stb.append("           AND L2.SEMESTER      = '1' ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("           AND L2.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            }
            stb.append("           AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND L2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L2.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN MORAL L3 ");
            stb.append("            ON L3.YEAR          = T1.YEAR ");
            stb.append("           AND L3.SEMESTER      = '2' ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("           AND L3.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L3.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L3.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN MORAL L4 ");
            stb.append("            ON L4.YEAR          = T1.YEAR ");
            stb.append("           AND L4.SEMESTER      = '3' ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("           AND L4.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L4.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L4.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_TOTALSTUDYTIME_ITEM_MST L5 ");
            stb.append("            ON L5.REMARK2       = '2' ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("           AND L5.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L5.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._loginYear + "'  ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "'  ");
            stb.append("     AND T1.SUBCLASSCD NOT LIKE '50%' ");
            stb.append("     AND T1.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SUBCLASSCD ");

            return stb.toString();
        }
    }


    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71410 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _disp;
        final String[] _categorySelected;
        final String _date;
        final String _descDate;
        final String _gradeHrClass;
        final String _grade;
        final String _loginSemester;
        final String _loginYear;
        final String _ctrlDate;
        final String _prgid;
        final String _semester;
        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;
        final String _schoolKind;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE_J;


        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolRemark3;
        private String _certifSchoolRemark8;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private final List _semesterList;
        private Map _semesterMap;
        private final Map _semesterDetailMap;
        private Map _subclassMstMap;
        private final Map _jviewGradeMap;
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

        private final String _useCurriculumcd;

        private final String _use_school_detail_gcm_dat;
        private final List _attendTestKindItemList;
        private final List _attendSemesterDetailList;
        final String _minGrade;
        final Map _stampMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _disp = request.getParameter("DISP");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _date = request.getParameter("DATE").replace('/', '-');
            _descDate = request.getParameter("DESC_DATE").replace('/', '-');
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            if ("1".equals(_disp)) {
                _grade = request.getParameter("GRADE");
            } else {
                _grade = _gradeHrClass.substring(0, 2);
            }
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginYear = request.getParameter("YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _prgid = request.getParameter("PRGID");
            _semester = request.getParameter("SEMESTER");
            _semes2Flg = "2".equals(_semester) || "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes3Flg = "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes9Flg = "9".equals(_semester) ? true : false;
            _schoolKind = request.getParameter("SCHOOL_KIND"); //J:中学 H:高校
            _HREPORTREMARK_DAT_COMMUNICATION_SIZE_J = null == request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"), "+", " "); //所見

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
            _jviewGradeMap = getJviewGradeMap(db2);

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            _schoolStampPath = getImageFilePath("SCHOOLSTAMP.bmp");

            _useCurriculumcd = request.getParameter("useCurriculumcd");

            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");

            _attendTestKindItemList = getTestKindItemList(db2, false, false);
            _attendSemesterDetailList = getAttendSemesterDetailList();

            _minGrade = getMinGrade(db2);

            _stampMap = getStampNoMap(db2);
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
                sql += " WITH REPL AS ( ";
                sql += " SELECT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD, CAST(NULL AS VARCHAR(1)) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _loginYear + "' GROUP BY COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
                sql += " UNION ";
                sql += " SELECT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _loginYear + "' GROUP BY ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += " T1.CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO, ";
                sql += " L2.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3 ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), isSaki, isMoto, rs.getString("CALCULATE_CREDIT_FLG"));
                    _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private Map getJviewGradeMap(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     VIEWCD, ");
            stb.append("     VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST ");
            stb.append(" WHERE ");
            stb.append("     GRADE = '" + _grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     VIEWCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                List jviewGradeList = null;
                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String viewCd = rs.getString("VIEWCD");
                    final String viewName = rs.getString("VIEWNAME");

                    final JviewGrade jviewGrade = new JviewGrade(subclassCd, viewCd, viewName);
                    if (retMap.containsKey(subclassCd)) {
                        jviewGradeList = (List) retMap.get(subclassCd);
                    } else {
                        jviewGradeList = new ArrayList();
                    }
                    jviewGradeList.add(jviewGrade);
                    retMap.put(subclassCd, jviewGradeList);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            final String kindCd = "J".equals(_schoolKind) ? "103" : "104";
            sql.append(" SELECT * FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _loginYear + "' AND CERTIF_KINDCD = '" + kindCd + "' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
            _certifSchoolRemark3 = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK3"));
            _certifSchoolRemark8 = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK8"));
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
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ");
                    stb.append(" INNER JOIN ADMIN_CONTROL_GCM_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                } else {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                    stb.append(" INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                }
                stb.append("    AND T11.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T11.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("    AND T11.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("    AND T11.SCORE_DIV = T1.SCORE_DIV ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("    AND T11.GRADE = T1.GRADE ");
                    stb.append("    AND T11.COURSECD = T1.COURSECD ");
                    stb.append("    AND T11.MAJORCD = T1.MAJORCD ");
                }
                stb.append(" LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ");
                stb.append(" WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("   AND T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD IN "); // 指定の科目が登録されていれば登録された科目、登録されていなければ00-00-00-000000を使用する
                stb.append("    (SELECT MAX(SUBCLASSCD) FROM ADMIN_CONTROL_SDIV_SUBCLASSCD) ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
//                    stb.append("    AND T1.SCHOOLCD = '" + _PRINT_SCHOOLCD + "' ");
//                    stb.append("    AND T1.SCHOOL_KIND = '" + _PRINT_SCHOOLKIND + "' ");
                    stb.append("    AND T1.GRADE = '00' ");
//                    stb.append("    AND T1.COURSECD || '-' || T1.MAJORCD = '"  + _COURSE_MAJOR + "' ");
                }
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

        private String getMinGrade(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT MIN(GRADE) FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _loginYear + "' "));
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

        public String getStaffImageFilePath(final String staffCd) {
            final String stampNo = (String) _stampMap.get(staffCd);
            final String path = _documentroot + "/image/stamp/" + stampNo + ".bmp";
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }
    }
}

// eof
