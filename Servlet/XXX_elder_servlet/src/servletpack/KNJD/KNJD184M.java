/*
 * $Id: 5a355c5bb29ca3b342ae3d8b14a95807e26f6dca $
 *
 * 作成日: 2019/04/05
 * 作成者: tawada
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
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD184M {

    private static final Log log = LogFactory.getLog(KNJD184M.class);

    private static final String[] MONTH_ARRAY = {"04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03"};
    private static final String SUCLASS_999999 = "99-J-99-999999";
    private static final String CHUKAN_TEST    = "01";

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
        // 学籍情報取得
        final List studentList = getSchList(db2);

        // データセット
        setStudentRank(db2, studentList);// 成績
        setViewRecord(db2, studentList); // 観点
        setAttendance(db2, studentList); // 出欠
        setOtherInfo(db2, studentList);  // 総合学習、行動の記録等

        // 生徒毎に出力
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();

            // 表紙
            if (_param._coverCheckBox) {
                setCover(svf, student);
            }

            // 成績
            if (_param._scoreCheckBox) {
                if (_param._testCd99) {
                    setData99(svf, student); // 通知表
                } else {
                    setData(svf, student); // 試験成績報告書
                }
            }

            // 修了証
            if (_param._completionCheckBox) {
                setCompletion(svf, student);
            }
            _hasData = true;
        }
    }

    /** 表紙 */
    private void setCover(final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD184M_1.frm", 1);

        // 年度
        svf.VrsOut("NENDO", _param._ctrlYear + "年度");

        // 校種
        svf.VrsOut("SCHOOL_KIND", "中等部");

        // 学年
        svf.VrsOut("GRADE", "第" + String.valueOf(Integer.parseInt(student._gradeCd)) + "学年");

        // タイトル
        svf.VrsOut("TITLE", "通知表");

        // 年組番
        svf.VrsOut("HR_ATTTENDNO", student._gradeHrclassnameAttendno);

        // 生徒名
        final String nameIdx = KNJ_EditEdit.getMS932ByteLength(student._name) > 32 ? "_3": KNJ_EditEdit.getMS932ByteLength(student._name) > 26 ? "_2": "";
        svf.VrsOut("NAME" + nameIdx, student._name);

		// 校長
        String principalName = _param._certifSchoolDat._principalName;
        final String priIdx = KNJ_EditEdit.getMS932ByteLength(principalName) > 32 ? "_3": KNJ_EditEdit.getMS932ByteLength(principalName) > 26 ? "_2": "";
        svf.VrsOut("PRINCIPAL_NAME" + priIdx, principalName);

        // 担任
        final String staff1Idx = KNJ_EditEdit.getMS932ByteLength(student._hrClassStaffName1) > 32 ? "_3": KNJ_EditEdit.getMS932ByteLength(student._hrClassStaffName1) > 26 ? "_2": "";
        svf.VrsOut("STAFF_NAME1" + staff1Idx, student._hrClassStaffName1);
        final String staff2Idx = KNJ_EditEdit.getMS932ByteLength(student._hrClassStaffName2) > 32 ? "_3": KNJ_EditEdit.getMS932ByteLength(student._hrClassStaffName2) > 26 ? "_2": "";
        svf.VrsOut("STAFF_NAME2" + staff2Idx, student._hrClassStaffName2);

        // 学校名
        svf.VrsOut("SCHOOLNAME", _param._certifSchoolDat._schoolName);

        svf.VrEndPage();
    }

    /** 修了証 */
    private void setCompletion(final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD184M_4.frm", 1);

        // 年組番
        svf.VrsOut("HR_ATTTENDNO", student._gradeHrclassnameAttendno);

        // 生徒名
        final String nameIdx = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "_3": KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "_2": "";
        svf.VrsOut("NAME" + nameIdx, student._name);

        // 生年月日
        svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_SeirekiJP(student._birthDay) + "生");

        // 学年
        svf.VrsOut("GRADE", "中等部第" + String.valueOf(Integer.parseInt(student._gradeCd)) + "学年");

        // コース
        svf.VrsOut("COURSE", student._courseCodeName);

        // 日付
        svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(_param._idouDate));

        // 学校名
        svf.VrsOut("SCHOOLNAME", _param._certifSchoolDat._schoolName);

        // 校長職
        svf.VrsOut("JOB_NAME", _param._certifSchoolDat._jobName);

        // 校長
        final String priIdx = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolDat._principalName) > 30 ? "_3": KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolDat._principalName) > 20 ? "_2": "";
        svf.VrsOut("PRINCIPAL_NAME" + priIdx, _param._certifSchoolDat._principalName);

        svf.VrEndPage();
    }

    /** 試験成績報告書 */
    private void setData(final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD184M_2.frm", 4);

        if (CHUKAN_TEST.equals(_param._testKindCd)) {
            svf.VrPage("10");
        } else {
            svf.VrPage("01");
        }

        final int MEX_ROW = 20;

        //タイトル
        final String title = _param._ctrlYear + "年度 " + _param._testCdName + "成績報告書";
        svf.VrsOut("TITLE", title);

        //ヘッダー
        final String nameIdx = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "_3": KNJ_EditEdit.getMS932ByteLength(student._name) > 24 ? "_2": "";
        final String staff1Idx = KNJ_EditEdit.getMS932ByteLength(student._hrClassStaffName1) > 30 ? "_3": KNJ_EditEdit.getMS932ByteLength(student._hrClassStaffName1) > 20 ? "_2": "";
        final String staff2Idx = KNJ_EditEdit.getMS932ByteLength(student._hrClassStaffName2) > 30 ? "_3": KNJ_EditEdit.getMS932ByteLength(student._hrClassStaffName2) > 20 ? "_2": "";
        final String courseIdx = KNJ_EditEdit.getMS932ByteLength(student._courseCodeName) > 34 ? "_2": "";
        svf.VrsOut("HR_ATTENDNO", student._gradeHrclassnameAttendno); // 年組番
        svf.VrsOut("NAME" + nameIdx, student._name); // 名前
        svf.VrsOutn("STAFF_NAME" + staff1Idx, 1, student._hrClassStaffName1); // 担任
        svf.VrsOutn("STAFF_NAME" + staff2Idx, 2, student._hrClassStaffName2); // 担任
        svf.VrsOut("COURSECODENAME" + courseIdx, student._courseCodeName); // コース

        //全試験平均
        final Rank rank9 = (Rank) student._rankMap.get(SUCLASS_999999);
        if (null != rank9) {
            svf.VrsOut("TOTALAVG_SCORE", rank9._avg);       // 点数
            svf.VrsOut("TOTALAVG_RANK", rank9._courseRank); // 順位
        }
        final String cmcSubCd99 = student._cmcCd + SUCLASS_999999;
        final CourseAvg courseAvg9 = (CourseAvg) _param._courseAvgMap.get(cmcSubCd99);
        if (null != courseAvg9) {
            svf.VrsOut("TOTALAVG_AVERAGE", courseAvg9._courseAvgAvg); // コース平均
            svf.VrsOut("TOTALAVG_CNT", courseAvg9._courseCount);   // コース順位（分母）
        }

        //出欠の記録
        int totalLesson = 0;
        int totalSusMou = 0;
        int totalAbroad = 0;
        int totalMLesson = 0;
        int totalPresent = 0;
        int totalKesseki = 0;
        int totalLate = 0;
        int totalLeave = 0;
        int totalKekka = 0;
        for (int i = 0; i < MONTH_ARRAY.length; i++) {
            final int line = i + 1;
            final String month = MONTH_ARRAY[i];
            final Attendance att = (Attendance) student._attendanceMap.get(month);

            if (att == null) continue;

            svf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
            svf.VrsOutn("SUSP_MOUR", line, String.valueOf(att._mourning + att._suspend + att._koudome + att._virus)); // 忌引き・出席停止
            svf.VrsOutn("ABROAD", line, String.valueOf(att._abroad)); // 留学
            svf.VrsOutn("MLESSON", line, String.valueOf(att._mlesson)); // 必出席日数
            svf.VrsOutn("PRESENT", line, String.valueOf(att._mlesson - att._absence)); // 出席日数
            svf.VrsOutn("KESSEKI", line, String.valueOf(att._absence)); // 欠席日数
            svf.VrsOutn("LATE", line, String.valueOf(att._late));   // 学校遅刻
            svf.VrsOutn("EARLY", line, String.valueOf(att._leave)); // 学校早退
            svf.VrsOutn("KEKKAJISU", line, String.valueOf(att._kekka)); // 欠課時数

            totalLesson += att._lesson;
            totalSusMou += att._mourning + att._suspend + att._koudome + att._virus;
            totalAbroad += att._abroad;
            totalMLesson += att._mlesson;
            totalPresent += att._mlesson - att._absence;
            totalKesseki += att._absence;
            totalLate += att._late;
            totalLeave += att._leave;
            totalKekka += att._kekka;
        }
        svf.VrsOut("TOTAL_LESSON", String.valueOf(totalLesson));
        svf.VrsOut("TOTAL_SUSP_MOUR", String.valueOf(totalSusMou));
        svf.VrsOut("TOTAL_ABROAD", String.valueOf(totalAbroad));
        svf.VrsOut("TOTAL_MLESSON", String.valueOf(totalMLesson));
        svf.VrsOut("TOTAL_PRESENT", String.valueOf(totalPresent));
        svf.VrsOut("TOTAL_KESSEKI", String.valueOf(totalKesseki));
        svf.VrsOut("TOTAL_LATE", String.valueOf(totalLate));
        svf.VrsOut("TOTAL_EARLY", String.valueOf(totalLeave));
        svf.VrsOut("TOTAL_KEKKAJISU", String.valueOf(totalKekka));

        //出欠の記録(備考)
        svf.VrsOut("ATTENDREMARK", student._attendrecRemark);

        //試験成績
        int rowCnt = 0;
        int listCnt = 1;
        String befClassCd = "";
        final String[] notPrintCd = {"33", "55", "99"};
        for (Iterator it2 = student._rankMap.keySet().iterator(); it2.hasNext();) {
            String subclassCd = (String) it2.next();
            final Subclass sub = (Subclass) _param._subClassMap.get(subclassCd);

            final String classCd = StringUtils.substring(subclassCd, 0, 2);

            if (Arrays.asList(notPrintCd).contains(classCd)) {
                continue;
            }

            final int subclassCnt = Integer.parseInt((String) student._subclassCntMap.get(classCd));
            final int printIdx = (subclassCnt % 2 == 1) ? (subclassCnt / 2) + 1: subclassCnt / 2;
            if (!befClassCd.equals(classCd)) listCnt = 1; //教科コード変わったら、初期化

            svf.VrsOut("SHIRO", sub._classCd); // 教科コード
            final String classIdx = KNJ_EditEdit.getMS932ByteLength(sub._className) > 16 ? "_3": KNJ_EditEdit.getMS932ByteLength(sub._className) > 10 ? "_2": "";
            final String subIdx   = KNJ_EditEdit.getMS932ByteLength(sub._subClassName) > 30 ? "_3": KNJ_EditEdit.getMS932ByteLength(sub._subClassName) > 20 ? "_2": "";
            if (printIdx == listCnt) {
                svf.VrsOut("CLASSNAME" + classIdx, sub._className); // 教科名称
            }
            svf.VrsOut("SUBCLASSNAME" + subIdx, sub._subClassName); // 科目名称

            final Rank rank = (Rank) student._rankMap.get(subclassCd);
            if (null != rank) {
                svf.VrsOut("SCORE", rank._score); // 点数
                svf.VrsOut("RANK", rank._courseRank); // 順位
                final String remarkIdx   = KNJ_EditEdit.getMS932ByteLength(rank._remark) > 20 ? "_3": KNJ_EditEdit.getMS932ByteLength(rank._remark) > 16 ? "_2": "";
                svf.VrsOut("REMARK" + remarkIdx, rank._remark); // 備考
            }

            final String cmcSubCd = student._cmcCd + subclassCd;
            final CourseAvg courseAvg = (CourseAvg) _param._courseAvgMap.get(cmcSubCd);
            if (null != courseAvg) {
                svf.VrsOut("AVERAGE", courseAvg._courseAvg); // コース平均
                svf.VrsOut("CNT", courseAvg._courseCount); // コース順位（分母）
            }

            svf.VrEndRecord();
            rowCnt++;
            listCnt++;
            befClassCd = classCd;
        }
        for (int i = rowCnt; i < MEX_ROW; i++) {
            svf.VrsOut("SHIRO", String.valueOf(i));

            svf.VrEndRecord();
        }
    }

    /** 通知表 */
    private void setData99(final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD184M_3.frm", 4);

        // フォームのPage切り替え
        if (_param._semester9) {
            svf.VrPage("01");
        } else {
            svf.VrPage("10");
        }

        //タイトル
        final String miniTitle = (_param._semester9) ? "学年成績": "期末";
        final String title = _param._ctrlYear + "年度 " + _param._semesterName + " " + miniTitle + " 通知表";
        svf.VrsOut("TITLE", title);

        //ヘッダー
        final String nameIdx = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "_3": KNJ_EditEdit.getMS932ByteLength(student._name) > 24 ? "_2": "";
        final String staff1Idx = KNJ_EditEdit.getMS932ByteLength(student._hrClassStaffName1) > 30 ? "_3": KNJ_EditEdit.getMS932ByteLength(student._hrClassStaffName1) > 20 ? "_2": "";
        final String staff2Idx = KNJ_EditEdit.getMS932ByteLength(student._hrClassStaffName2) > 30 ? "_3": KNJ_EditEdit.getMS932ByteLength(student._hrClassStaffName2) > 20 ? "_2": "";
        final String courseIdx = KNJ_EditEdit.getMS932ByteLength(student._courseCodeName) > 34 ? "_2": "";
        svf.VrsOut("HR_ATTENDNO", student._gradeHrclassnameAttendno); // 年組番
        svf.VrsOut("NAME" + nameIdx, student._name); // 名前
        svf.VrsOutn("STAFF_NAME" + staff1Idx, 1, student._hrClassStaffName1); // 担任
        svf.VrsOutn("STAFF_NAME" + staff2Idx, 2, student._hrClassStaffName2); // 担任
        svf.VrsOut("COURSECODENAME" + courseIdx, student._courseCodeName); // コース

        if (_param._semester9 && _param._testCd99) {
            svf.VrsOut("PROV_TITLE", "評定");
        } else {
            svf.VrsOut("PROV_TITLE_KARI", "仮評定");
        }

        //出欠席の記録
        int totalLesson = 0;
        int totalSusMou = 0;
        int totalAbroad = 0;
        int totalMLesson = 0;
        int totalPresent = 0;
        int totalKesseki = 0;
        int totalLate = 0;
        int totalLeave = 0;
        int totalKekka = 0;
        int line = 1;
        for (Iterator iterator = _param._semsterList.iterator(); iterator.hasNext();) {
            Semester seme = (Semester) iterator.next();

            svf.VrsOutn("SEMESTER", line, String.valueOf(seme._semesterName)); // 学期名

            final Attendance att = (Attendance) student._attendanceMap.get(seme._semester);

            if (att == null) {
                line++;
                continue;
            }

            svf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
            svf.VrsOutn("SUSP_MOUR", line, String.valueOf(att._mourning + att._suspend + att._koudome + att._virus)); // 忌引き・出席停止
            svf.VrsOutn("ABROAD", line, String.valueOf(att._abroad)); // 留学
            svf.VrsOutn("MLESSON", line, String.valueOf(att._mlesson)); // 必出席日数
            svf.VrsOutn("PRESENT", line, String.valueOf(att._mlesson - att._absence)); // 出席日数
            svf.VrsOutn("KESSEKI", line, String.valueOf(att._absence)); // 欠席日数
            svf.VrsOutn("LATE", line, String.valueOf(att._late));   // 学校遅刻
            svf.VrsOutn("EARLY", line, String.valueOf(att._leave)); // 学校早退
            svf.VrsOutn("KEKKAJISU", line, String.valueOf(att._kekka)); // 欠課時数

            totalLesson += att._lesson;
            totalSusMou += att._mourning + att._suspend + att._koudome + att._virus;
            totalAbroad += att._abroad;
            totalMLesson += att._mlesson;
            totalPresent += att._mlesson - att._absence;
            totalKesseki += att._absence;
            totalLate += att._late;
            totalLeave += att._leave;
            totalKekka += att._kekka;
            line++;
        }
        svf.VrsOut("TOTAL_LESSON", String.valueOf(totalLesson));
        svf.VrsOut("TOTAL_SUSP_MOUR", String.valueOf(totalSusMou));
        svf.VrsOut("TOTAL_ABROAD", String.valueOf(totalAbroad));
        svf.VrsOut("TOTAL_MLESSON", String.valueOf(totalMLesson));
        svf.VrsOut("TOTAL_PRESENT", String.valueOf(totalPresent));
        svf.VrsOut("TOTAL_KESSEKI", String.valueOf(totalKesseki));
        svf.VrsOut("TOTAL_LATE", String.valueOf(totalLate));
        svf.VrsOut("TOTAL_EARLY", String.valueOf(totalLeave));
        svf.VrsOut("TOTAL_KEKKAJISU", String.valueOf(totalKekka));

        //所見
        final String[] communication = KNJ_EditEdit.get_token_1(student._communication, 26, 4);
        if (communication != null) {
            for (int i = 0; i < communication.length; i++) {
                String commStr = communication[i];

                svf.VrsOutn("COMMUNICATION", i + 1, commStr);
            }
        }

        //出欠席の記録(備考)
        final int attRow = (_param._semester9) ? 5: 3;
        final String[] attRemark = KNJ_EditEdit.get_token_1(student._attendrecRemark, 20, attRow);
        if (attRemark != null) {
            for (int i = 0; i < attRemark.length; i++) {
                String attendrecRemark = attRemark[i];

                svf.VrsOutn("ATTENDREMARK", i + 1, attendrecRemark);
                svf.VrsOutn("ATTENDREMARK9", i + 1, attendrecRemark);
            }
        }

        //総合的な学習の時間
        final String tsIdx = KNJ_EditEdit.getMS932ByteLength(student._totalStudyTime) > 50 ? "2": "1";
        svf.VrsOut("TOTALSTUDY_MOKU" + tsIdx, student._totalStudyTime); // 目標
        svf.VrsOut("TOTALSTUDY_MOKU9" + tsIdx, student._totalStudyTime); // 目標
        //観点
        int kantenLine = 1;
        for (Iterator it = _param._sougouKantenMap.keySet().iterator(); it.hasNext();) {
            final String nameCd2 = (String) it.next();
            final String kantenName = (String) _param._sougouKantenMap.get(nameCd2);
            final String hyouka = (String) student._sougouMap.get(nameCd2);

            //名称
            if (kantenName != null) {
                final String kantenIdx = KNJ_EditEdit.getMS932ByteLength(kantenName) > 22 ? "_2": "";
                svf.VrsOutn("TOTALSTUDY_KANTEN" + kantenIdx, kantenLine, kantenName);
            }

            //学期評価
            if (hyouka != null) {
                svf.VrsOutn("TOTALSTUDY_HYOKA", kantenLine, (String) _param._sougouHenkanMap.get(hyouka));
            }

            kantenLine++;
        }
        //評価
        final String[] remark4s = KNJ_EditEdit.get_token_1(student._remark4, 30, 5);
        if (remark4s != null) {
            for (int i = 0; i < remark4s.length; i++) {
                String remark4 = remark4s[i];

                svf.VrsOutn("TOTALSTUDY_HYOKA9", i + 1, remark4);
            }
        }

        //委員会
        final String commIdx = KNJ_EditEdit.getMS932ByteLength(student._committee) > 16 ? "_2": "";
        svf.VrsOut("COMMITTEE1" + commIdx, student._committee);
        svf.VrsOut("COMMITTEE91" + commIdx, student._committee);

        //クラス係
        final String comm2Idx = KNJ_EditEdit.getMS932ByteLength(student._kakari) > 16 ? "_2": "";
        svf.VrsOut("COMMITTEE2" + comm2Idx, student._kakari);
        svf.VrsOut("COMMITTEE92" + comm2Idx, student._kakari);

        //部活動
        int clubLine = 0;
        for (Iterator it = student._clubList.iterator(); it.hasNext();) {
            final String club = (String) it.next();

            final int clubLength = KNJ_EditEdit.getMS932ByteLength(club);
            if (clubLine == 0) {
                final String clubIdx = clubLength > 20 ? "_3": clubLength > 16 ? "_2": "";
                svf.VrsOut("CLUB" + clubIdx, club);
                svf.VrsOut("CLUB9" + clubIdx, club);
            } else {
                final String clubIdx = clubLength > 40 ? "_4": clubLength > 30 ? "_3": clubLength > 24 ? "_2": "";
                svf.VrsOutn("SPECIALACT" + clubIdx, clubLine, club);
                svf.VrsOutn("SPECIALACT9" + clubIdx, clubLine, club);
            }

            clubLine++;
        }

        //資格取得
        int qualiLine = 1;
        for (Iterator it = student._qualifiedList.iterator(); it.hasNext();) {
            final String qualified = (String) it.next();

            final int qualiLength = KNJ_EditEdit.getMS932ByteLength(qualified);
            final String qualiIdx = qualiLength > 40 ? "_4": qualiLength > 30 ? "_3": qualiLength > 24 ? "_2": "";
            svf.VrsOutn("QUALIFIED" + qualiIdx, qualiLine, qualified);
            svf.VrsOutn("QUALIFIED9" + qualiIdx, qualiLine, qualified);
            qualiLine++;
        }

        //大会記録等
        int clubRecLine = 1;
        for (Iterator it = student._clubRecordList.iterator(); it.hasNext();) {
            final String clubRec = (String) it.next();

            final int recLength = KNJ_EditEdit.getMS932ByteLength(clubRec);
            final String recIdx = recLength > 30 ? "_3": recLength > 24 ? "_2": "";
            svf.VrsOutn("KIROKU" + recIdx, clubRecLine, clubRec);
            svf.VrsOutn("KIROKU9" + recIdx, clubRecLine, clubRec);
            clubRecLine++;
        }

        //生活および行動の記録
        int koudouLine = 1;
        String rightLeftFrg = "1";
        for (Iterator it = _param._koudouKirokuMap.keySet().iterator(); it.hasNext();) {
            final String code = (String) it.next();
            final String koudouName = (String) _param._koudouKirokuMap.get(code);
            final String hyouka = (String) student._behaviorMap.get(code);

            if (koudouLine > 5) {
                koudouLine = 1;
                rightLeftFrg = "2";
            }

            //名称
            if (koudouName != null) {
                final String koudouIdx = KNJ_EditEdit.getMS932ByteLength(koudouName) > 18 ? "_2": "";
                svf.VrsOutn("CODENAME" + rightLeftFrg + koudouIdx, koudouLine, koudouName);
                svf.VrsOutn("CODENAME9" + rightLeftFrg + koudouIdx, koudouLine, koudouName);
            }

            //学期評価
            if (hyouka != null) {
                svf.VrsOutn("RECORD" + rightLeftFrg, koudouLine, (String) _param._koudouHenkanMap.get(hyouka));
                svf.VrsOutn("RECORD9" + rightLeftFrg, koudouLine, (String) _param._koudouHenkanMap.get(hyouka));
            }

            koudouLine++;
        }

        //観点別学習状況
        final int MEX_ROW = 46;
        final int HALF_ROW = 23;
        int rowCnt = 0;
        int halfRowCnt = 0;

        for (Iterator it = _param._viewNameMap.keySet().iterator(); it.hasNext();) {
            final String subclassCd = (String) it.next();

            final List viewList = (List) _param._viewNameMap.get(subclassCd);
            final int listSize = viewList.size();
            final int printIdx = (listSize % 2 == 1) ? (listSize / 2) + 1: listSize / 2;
            int listCnt = 1;

            //教科が隣にまたがる時は、空レコードで埋めて隣から出力する
            if (HALF_ROW - halfRowCnt < listSize) {
                for (int i = 0; i < HALF_ROW - halfRowCnt; i++) {
                    svf.VrsOut("SHIRO", String.valueOf(i));

                    svf.VrEndRecord();
                    rowCnt++;
                }
                halfRowCnt = 0;
            }

            for (Iterator it2 = viewList.iterator(); it2.hasNext();) {
                ViewName view = (ViewName) it2.next();

                final String setKey = subclassCd + ":" + view._viewCd;

                final ViewRecord viewRec = (ViewRecord) student._viewMap.get(setKey);

                if (viewRec == null) continue;

                //教科
                final Subclass subClass = (Subclass) _param._subClassMst.get(subclassCd);
                if (subClass != null) {
                    if (listCnt == printIdx) {
                        svf.VrsOut("CLASSNAME", subClass._className);
                    }
                    svf.VrsOut("SHIRO", subClass._classCd);
                }

                //観点名称
                final String vNameIdx = KNJ_EditEdit.getMS932ByteLength(view._viewName) > 34 ? "_3": KNJ_EditEdit.getMS932ByteLength(view._viewName) > 28 ? "_2": "";
                svf.VrsOut("VIEWNAME" + vNameIdx, view._viewName);

                if (viewRec != null) {
                    //観点評価
                    svf.VrsOut("VALUE", viewRec._status);

                    //観点仮評定
                    if (listCnt == printIdx) {
                        if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_param._useTestCountflg)) {
                            svf.VrsOut("PROV", viewRec._score);
                        } else {
                            svf.VrsOut("PROV", viewRec._value);
                        }
                    }
                }

                svf.VrEndRecord();
                rowCnt++;
                halfRowCnt++;
                listCnt++;
            }
        }

        for (int i = rowCnt; i < MEX_ROW; i++) {
            svf.VrsOut("SHIRO", String.valueOf(i));

            svf.VrEndRecord();
        }

    }

    private static String zenkaku(final String s) {
    	if (null == s) {
    		return s;
    	}
    	final StringBuffer stb = new StringBuffer();
    	for (int i = 0; i < s.length(); i++) {
    		final char ch = s.charAt(i);
    		if ('0' <= ch && ch <= '9') {
    			stb.append((char) (ch - '0' + '０'));
    		} else {
    			stb.append(ch);
    		}
    	}
    	return stb.toString();
    }

    private List getSchList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSchregSql();
            //log.debug(" schregSQL =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo           = rs.getString("SCHREGNO");
                final String birthDay           = rs.getString("BIRTHDAY");
                final String schoolKind         = rs.getString("SCHOOL_KIND");
                final String gradeCd            = rs.getString("GRADE_CD");
                final String grade              = rs.getString("GRADE");
                final String hrClass            = rs.getString("HR_CLASS");
                final String hrClassName        = rs.getString("HR_NAME");
                final String hrClassName1       = rs.getString("HR_CLASS_NAME1");
                final String attendNo           = rs.getString("ATTENDNO");
                final String name               = rs.getString("NAME");
                final String cmcCd              = rs.getString("CMC_CD");
                final String courseCodeName     = rs.getString("COURSECODENAME");
                final String staff1             = rs.getString("STAFFNAME1");
                final String staff2             = rs.getString("STAFFNAME2");

                final Student student = new Student(schregNo, birthDay, schoolKind, gradeCd, grade, hrClass, hrClassName, hrClassName1, attendNo, name, cmcCd, courseCodeName, staff1, staff2);
                final String gradeCdString = zenkaku(NumberUtils.isDigits(student._gradeCd) ? String.valueOf(Integer.parseInt(student._gradeCd)) : StringUtils.defaultString(student._gradeCd));
				student._gradeHrclassnameAttendno = gradeCdString + " 年 " + StringUtils.defaultString(student._hrClassName1) + " 組 " + String.valueOf(Integer.parseInt(student._attendNo)) + " 番";
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.error("RegdSQL_Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSchregSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     BASE.BIRTHDAY, ");
        stb.append("     GDAT.SCHOOL_KIND, ");
        stb.append("     GDAT.GRADE_CD, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     HDAT.HR_NAME, ");
        stb.append("     HDAT.HR_CLASS_NAME1, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE as CMC_CD, ");
        stb.append("     CODE.COURSECODENAME, ");
        stb.append("     value(STAF1.STAFFNAME, '') as STAFFNAME1, ");
        stb.append("     value(STAF2.STAFFNAME, '') as STAFFNAME2 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN  SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ");
        stb.append("                                    AND HDAT.SEMESTER = REGD.SEMESTER ");
        stb.append("                                    AND HDAT.GRADE    = REGD.GRADE ");
        stb.append("                                    AND HDAT.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR     = REGD.YEAR ");
        stb.append("                                    AND GDAT.GRADE    = REGD.GRADE ");
        stb.append("     LEFT JOIN STAFF_MST STAF1 ON STAF1.STAFFCD = HDAT.TR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST STAF2 ON STAF2.STAFFCD = HDAT.TR_CD2 ");
        stb.append("     LEFT JOIN COURSECODE_MST CODE ON CODE.COURSECODE = REGD.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("         REGD.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester2 + "' ");
        stb.append("     AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._schregNos) + " ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO, ");
        stb.append("     REGD.SCHREGNO ");

        return stb.toString();
    }

    /** 成績データ */
    private void setStudentRank(final DB2UDB db2, final List schList) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSchRankSql();
//            log.debug(" schRankSQL =" + sql);
            ps = db2.prepareStatement(sql);

            for (Iterator it = schList.iterator(); it.hasNext();) {
                Student student = (Student) it.next();

                ps.setString(1, student._schregNo);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classCd    = rs.getString("CLASSCD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String score      = rs.getString("SCORE");
                    final String avg        = avgStr(rs.getBigDecimal("AVG"));
                    final String rank       = rs.getString("COURSE_RANK");
                    final String remark     = rs.getString("REMARK");

                    student._rankMap.put(subclassCd, new Rank(score, avg, rank, remark));
                    if (student._subclassCntMap.containsKey(classCd)) {
                        final int subCnt = Integer.parseInt((String) student._subclassCntMap.get(classCd)) + 1;
                        student._subclassCntMap.put(classCd, String.valueOf(subCnt));
                    } else {
                        student._subclassCntMap.put(classCd, "1");
                    }
                }
            }

        } catch (SQLException ex) {
            log.error("RankSQL_Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private static String avgStr(final BigDecimal bd) {
        return bd == null ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private String getSchRankSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RANK.SCHREGNO, ");
        stb.append("     RANK.CLASSCD, ");
        stb.append("     RANK.CLASSCD || '-' || RANK.SCHOOL_KIND || '-' || RANK.CURRICULUM_CD || '-' || RANK.SUBCLASSCD as SUBCLASSCD, ");
        stb.append("     RANK.SCORE, ");
        stb.append("     RANK.AVG, ");
        stb.append("     RANK.COURSE_RANK, ");
        stb.append("     INFO.REMARK ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_SDIV_DAT RANK ");
        stb.append("     LEFT JOIN RECORD_INFO_SDIV_DAT INFO ON INFO.YEAR          = RANK.YEAR ");
        stb.append("                                        AND INFO.SEMESTER      = RANK.SEMESTER ");
        stb.append("                                        AND INFO.TESTKINDCD    = RANK.TESTKINDCD ");
        stb.append("                                        AND INFO.TESTITEMCD    = RANK.TESTITEMCD ");
        stb.append("                                        AND INFO.SCORE_DIV     = RANK.SCORE_DIV ");
        stb.append("                                        AND INFO.CLASSCD       = RANK.CLASSCD ");
        stb.append("                                        AND INFO.SCHOOL_KIND   = RANK.SCHOOL_KIND ");
        stb.append("                                        AND INFO.CURRICULUM_CD = RANK.CURRICULUM_CD ");
        stb.append("                                        AND INFO.SUBCLASSCD    = RANK.SUBCLASSCD ");
        stb.append("                                        AND INFO.SCHREGNO      = RANK.SCHREGNO ");
        stb.append("                                        AND INFO.SEQ           = '002' ");//002:備考入力
        stb.append(" WHERE ");
        stb.append("         RANK.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND RANK.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND RANK.TESTKINDCD || RANK.TESTITEMCD || RANK.SCORE_DIV = '" + _param._testCd + "' ");
        stb.append("     AND RANK.SCHREGNO = ? ");
        stb.append(" ORDER BY ");
        stb.append("     RANK.SCHREGNO, ");
        stb.append("     RANK.CLASSCD || '-' || RANK.SCHOOL_KIND || '-' || RANK.CURRICULUM_CD || '-' || RANK.SUBCLASSCD ");

        return stb.toString();
    }

    /** 観点データ */
    private void setViewRecord(final DB2UDB db2, final List schList) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getViewRecordSql();
//            log.debug(" schViewRecordSQL =" + sql);
            ps = db2.prepareStatement(sql);

            for (Iterator it = schList.iterator(); it.hasNext();) {
                Student student = (Student) it.next();

                ps.setString(1, student._schregNo);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String viewCd     = rs.getString("VIEWCD");
                    final String status     = rs.getString("STATUS");
                    final String score      = rs.getString("SCORE");
                    final String value      = rs.getString("VALUE");

                    final String setKey = subclassCd + ":" + viewCd;
                    student._viewMap.put(setKey, new ViewRecord(status, score, value));
                }
            }

        } catch (SQLException ex) {
            log.error("ViewRecordSQL_Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getViewRecordSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHINFO AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT ");
        stb.append("     WHERE ");
        stb.append("             YEAR      = '" + _param._ctrlYear + "' ");
        stb.append("         AND SEMESTER  = '" + _param._semester2 + "' ");
        stb.append("         AND SCHREGNO  = ? ");
        stb.append(" ), VIEWSTAT AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T2.CLASSCD, ");
        stb.append("         T2.SCHOOL_KIND, ");
        stb.append("         T2.CURRICULUM_CD, ");
        stb.append("         T2.SUBCLASSCD, ");
        stb.append("         T2.VIEWCD, ");
        stb.append("         T2.STATUS ");
        stb.append("     FROM ");
        stb.append("         SCHINFO T1, ");
        stb.append("         JVIEWSTAT_RECORD_DAT T2 ");
        stb.append("     WHERE ");
        stb.append("             T2.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("         AND T2.SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("    ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD as SUBCLASSCD, ");
        stb.append("     T2.VIEWCD, ");
        stb.append("     T2.STATUS, ");
        stb.append("     T3.SCORE, ");
        stb.append("     T3.VALUE ");
        stb.append(" FROM ");
        stb.append("     SCHINFO T1 ");
        stb.append("     LEFT JOIN VIEWSTAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN RECORD_SCORE_DAT T3 ON T3.YEAR          = '" + _param._ctrlYear + "' ");
        stb.append("                                  AND T3.SEMESTER      = '" + _param._semester + "' ");
        stb.append("                                  AND T3.TESTKINDCD    = '99' ");
        stb.append("                                  AND T3.TESTITEMCD    = '00' ");
        if (_param._semester9 && !"1".equals(_param._useHyoukaHyouteiFlg)) {
            stb.append("                              AND T3.SCORE_DIV     = '09' ");
        } else {
            stb.append("                              AND T3.SCORE_DIV     = '08' ");
        }
        stb.append("                                  AND T3.CLASSCD       = T2.CLASSCD ");
        stb.append("                                  AND T3.SCHOOL_KIND   = T2.SCHOOL_KIND ");
        stb.append("                                  AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("                                  AND T3.SUBCLASSCD    = T2.SUBCLASSCD ");
        stb.append("                                  AND T3.SCHREGNO      = T1.SCHREGNO ");

        return stb.toString();
    }

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    /** 出欠データ */
    private void setAttendance(final DB2UDB db2, final List schList) {
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        _param._attendParamMap.put("schregno", "?");

        final Map _attendResultMap = new HashMap();
        final Integer zero = new Integer(0);
        try {
            // 通知表は学期ごとにセット
            if (_param._testCd99) {

                boolean breakFlg = false;

                for (Iterator iterator = _param._semsterList.iterator(); iterator.hasNext();) {
                    Semester seme = (Semester) iterator.next();

                    if (breakFlg) break;

                    // 指示画面日付と比較
                    final int semeEdate = Integer.parseInt(StringUtils.remove(seme._eDate, "-"));
                    final int paramDate = Integer.parseInt(StringUtils.remove(_param._idouDate, "-"));
                    final String setSDate = seme._sDate;
                    String setEdate;
                    if (semeEdate > paramDate) {
                        setEdate = _param._idouDate;
                        breakFlg = true;
                    } else {
                        setEdate = seme._eDate;
                    }

                    String sql;
                    sql = AttendAccumulate.getAttendSemesSql(
                            _param._ctrlYear,
                            seme._semester,
                            setSDate,
                            setEdate,
                            _param._attendParamMap
                            );

//                    log.debug(" attend semes sql = " + sql);
                    ps = db2.prepareStatement(sql);

                    for (Iterator it = schList.iterator(); it.hasNext();) {
                        Student student = (Student) it.next();

                        for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregNo}).iterator(); rit.hasNext();) {
                        	final Map row = (Map) rit.next();
                            final String semester = KnjDbUtils.getString(row, "SEMESTER");

                            if ("9".equals(semester)) continue;

                            final int lesson    = KnjDbUtils.getInt(row, "LESSON", zero).intValue();
                            final int mourning  = KnjDbUtils.getInt(row, "MOURNING", zero).intValue();
                            final int suspend   = KnjDbUtils.getInt(row, "SUSPEND", zero).intValue();
                            final int koudome   = KnjDbUtils.getInt(row, "KOUDOME", zero).intValue();
                            final int virus     = KnjDbUtils.getInt(row, "VIRUS", zero).intValue();
                            final int abroad    = KnjDbUtils.getInt(row, "TRANSFER_DATE", zero).intValue();
                            final int mlesson   = KnjDbUtils.getInt(row, "MLESSON", zero).intValue();
                            final int absence   = KnjDbUtils.getInt(row, "SICK", zero).intValue();
                            final int attend    = KnjDbUtils.getInt(row, "PRESENT", zero).intValue();
                            final int late      = KnjDbUtils.getInt(row, "LATE", zero).intValue();
                            final int early     = KnjDbUtils.getInt(row, "EARLY", zero).intValue();

                            final Attendance attendance = new Attendance(lesson, mourning, suspend, koudome, virus, abroad, mlesson, absence, attend, late, early);
                            // log.debug("   schregno = " + student._schregno + " , attendance = " + attendance);
                            student._attendanceMap.put(seme._semester, attendance);
                        }
                    }
                    
                    String sql2;
                    sql2 = AttendAccumulate.getAttendSubclassSql(
                            _param._ctrlYear,
                            _param._semester,
                            setSDate,
                            setEdate,
                            _param._subclassAttendParamMap
                    );
                    final String key = seme._semester;
                	for (final Iterator rit = KnjDbUtils.query(db2, sql2).iterator(); rit.hasNext();) {
                		final Map row = (Map) rit.next();
                        final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
    					final Student student = Student.getStudent(schregno, schList);
                        if (student == null || !"9".equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                            continue;
                        }
                		
                		//final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final Attendance att = (Attendance) student._attendanceMap.get(key);
                        if (null == att) {
                        	continue;
                        }
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final boolean isCombinedSubclass = _param._subClassRepMap.values().contains(subclasscd);
                        if (!isCombinedSubclass) {
                        	final BigDecimal sick2 = KnjDbUtils.getBigDecimal(row, "SICK2", null);
                        	if (null != sick2 && sick2.doubleValue() > 0) {
                        		att._kekka += sick2.intValue();
                        		getMappedList(getMappedMap(getMappedMap(getMappedMap(_attendResultMap, schregno), "date"), key), subclasscd).add(sick2);
                        		getMappedList(getMappedMap(getMappedMap(getMappedMap(_attendResultMap, schregno), "subclasscd"), subclasscd), key).add(sick2);
                        		//log.info(" add " + schregno + " sick2 (" + setSDate + " ~ " + setEdate  + ")" + subclasscd + " = " + sick2 + "( => " + att._kekka + ")");
                        	}
                        }
                	}

                    if (seme._semester.equals(_param._semester)) break;
                }

            // 試験報告書は月毎にセット
            } else {

                boolean breakFlg = false;

                for (int i = 0; i < MONTH_ARRAY.length; i++) {
                    final String month = MONTH_ARRAY[i];

                    if (breakFlg) break;

                    final String year = Integer.parseInt(month) < 4 ? String.valueOf(Integer.parseInt(_param._ctrlYear) + 1): _param._ctrlYear;
                    final String sDate = year + "-" + month + "-01";
                    final String eDate = (String) _param._eDateMap.get(month);

                    // 指示画面日付と比較
                    final int semeEdate = Integer.parseInt(StringUtils.remove(eDate, "-"));
                    final int paramDate = Integer.parseInt(StringUtils.remove(_param._idouDate, "-"));
                    String setEdate;
                    if (semeEdate > paramDate) {
                        setEdate = _param._idouDate;
                        breakFlg = true;
                    } else {
                        setEdate = eDate;
                    }

                    String sql;
                    sql = AttendAccumulate.getAttendSemesSql(
                            _param._ctrlYear,
                            _param._semester,
                            sDate,
                            setEdate,
                            _param._attendParamMap
                            );

//                    log.debug(" attend semes sql = " + sql);
                    ps = db2.prepareStatement(sql);

                    for (Iterator it = schList.iterator(); it.hasNext();) {
                        Student student = (Student) it.next();

                        for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregNo}).iterator(); rit.hasNext();) {
                        	final Map row = (Map) rit.next();
                            final String semester = KnjDbUtils.getString(row, "SEMESTER");

                             if ("9".equals(semester)) continue;

                            final int lesson    = KnjDbUtils.getInt(row, "LESSON", zero).intValue();
                            final int mourning  = KnjDbUtils.getInt(row, "MOURNING", zero).intValue();
                            final int suspend   = KnjDbUtils.getInt(row, "SUSPEND", zero).intValue();
                            final int koudome   = KnjDbUtils.getInt(row, "KOUDOME", zero).intValue();
                            final int virus     = KnjDbUtils.getInt(row, "VIRUS", zero).intValue();
                            final int abroad    = KnjDbUtils.getInt(row, "TRANSFER_DATE", zero).intValue();
                            final int mlesson   = KnjDbUtils.getInt(row, "MLESSON", zero).intValue();
                            final int absence   = KnjDbUtils.getInt(row, "SICK", zero).intValue();
                            final int attend    = KnjDbUtils.getInt(row, "PRESENT", zero).intValue();
                            final int late      = KnjDbUtils.getInt(row, "LATE", zero).intValue();
                            final int early     = KnjDbUtils.getInt(row, "EARLY", zero).intValue();

                            final Attendance attendance = new Attendance(lesson, mourning, suspend, koudome, virus, abroad, mlesson, absence, attend, late, early);
                            if (student._attendanceMap.containsKey(month)) {
                                final Attendance att = (Attendance) student._attendanceMap.get(month);
                                att.add(attendance);
                            } else {
                                // log.debug("   schregno = " + student._schregno + " , attendance = " + attendance);
                                student._attendanceMap.put(month, attendance);
                            }
                        }
                    }
                    
                    String sql2;
                    sql2 = AttendAccumulate.getAttendSubclassSql(
                            _param._ctrlYear,
                            _param._semester,
                            sDate,
                            setEdate,
                            _param._subclassAttendParamMap
                    );
                    final String key = month;
                	for (final Iterator rit = KnjDbUtils.query(db2, sql2).iterator(); rit.hasNext();) {
                		final Map row = (Map) rit.next();
                        final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
    					final Student student = Student.getStudent(schregno, schList);
                        if (student == null || !"9".equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                            continue;
                        }
                		
                        final Attendance att = (Attendance) student._attendanceMap.get(key);
                        if (null == att) {
                        	continue;
                        }
                		final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final boolean isCombinedSubclass = _param._subClassRepMap.values().contains(subclasscd);
                		if (!isCombinedSubclass) {
                            final BigDecimal sick2 = "1".equals(KnjDbUtils.getString(row, "IS_COMBINED_SUBCLASS")) ? KnjDbUtils.getBigDecimal(row, "REPLACED_SICK2", null) : KnjDbUtils.getBigDecimal(row, "SICK2", null);
                        	if (null != sick2 && sick2.doubleValue() > 0) {
                        		att._kekka += sick2.intValue();
                        		getMappedList(getMappedMap(getMappedMap(getMappedMap(_attendResultMap, schregno), "date"), key), subclasscd).add(sick2);
                        		getMappedList(getMappedMap(getMappedMap(getMappedMap(_attendResultMap, schregno), "subclasscd"), subclasscd), key).add(sick2);
                        		//log.info(" add " + schregno + " sick2 (" + sDate + " ~ " + setEdate  + ")" + subclasscd + " = " + sick2 + "( => " + att._kekka + ")");
                        	}
                		}
                	}
                }
            }
            
            if (_param._isOutputDebug) {
            	for (final Iterator it = _attendResultMap.keySet().iterator(); it.hasNext();) {
            		final String schregno = (String) it.next();
            		log.info(" schregno = " + schregno);
            		log.info("   date = ");
            		for (final Iterator it2 = getMappedMap((Map) _attendResultMap.get(schregno), "date").entrySet().iterator(); it2.hasNext();) {
            			final Map.Entry e = (Map.Entry) it2.next();
            			log.info("     " + e);
            		}
            		log.info("   subclasscd = ");
            		for (final Iterator it2 = getMappedMap((Map) _attendResultMap.get(schregno), "subclasscd").entrySet().iterator(); it2.hasNext();) {
            			final Map.Entry e = (Map.Entry) it2.next();
            			log.info("     " + e);
            		}
            	}
            }

        } catch (SQLException e) {
            log.error("AttendSQL exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(ps2);
            db2.commit();
        }
    }

    /** その他情報セット */
    private void setOtherInfo(final DB2UDB db2, final List schList) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // 総合(目標、評価）、出欠備考
            final String sql1 = getHreportSql();
//            log.debug(" getHreportSqlSQL =" + sql1);
            ps = db2.prepareStatement(sql1);

            for (Iterator it = schList.iterator(); it.hasNext();) {
                Student student = (Student) it.next();

                ps.setString(1, student._schregNo);
                rs = ps.executeQuery();
                while (rs.next()) {
                    student._totalStudyTime     = rs.getString("TOTALSTUDYTIME");
                    student._remark4            = rs.getString("REMARK4");
                    student._communication      = rs.getString("COMMUNICATION");
                    student._attendrecRemark    = rs.getString("ATTENDREC_REMARK");
                }
            }

            // 総合(観点)
            final String sql2 = getHreportDetailSql();
//            log.debug(" getHreportDetailSqlSQL =" + sql2);
            ps = db2.prepareStatement(sql2);

            for (Iterator it = schList.iterator(); it.hasNext();) {
                Student student = (Student) it.next();

                ps.setString(1, student._schregNo);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code       = rs.getString("CODE");
                    final String remark1    = rs.getString("REMARK1");

                    student._sougouMap.put(code, remark1);
                }
            }

            // 行動の記録
            final String sql3 = getBehaviorDatSql();
//            log.debug(" getBehaviorDatSQL =" + sql3);
            ps = db2.prepareStatement(sql3);

            for (Iterator it = schList.iterator(); it.hasNext();) {
                Student student = (Student) it.next();

                ps.setString(1, student._schregNo);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code   = rs.getString("CODE");
                    final String record = rs.getString("RECORD");

                    student._behaviorMap.put(code, record);
                }
            }

            // 委員会
            final String sql4 = getSchregCommitteeHistSql();
//            log.debug(" getSchCommSQL =" + sql4);
            ps = db2.prepareStatement(sql4);
            for (Iterator it = schList.iterator(); it.hasNext();) {
                Student student = (Student) it.next();
                
                String comm1 = "";
                String comm2 = "";
                String sep1  = "";
                String sep2  = "";

                ps.setString(1, student._schoolKind);
                ps.setString(2, student._schregNo);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cmmFlg   = rs.getString("COMMITTEE_FLG");
                    final String commName = rs.getString("COMMITTEENAME");

                    // 委員会
                    if ("1".equals(cmmFlg)) {
                        comm1 = comm1 + sep1 + commName;
                        sep1 = ",";

                    // クラス係
                    } else if ("2".equals(cmmFlg)) {
                        comm2 = comm2 + sep2 + commName;
                        sep2 = ",";
                    }
                }
                student._committee = (!"".equals(comm1))  ? comm1: "";
                student._kakari    = (!"".equals(comm2))  ? comm2: "";
            }

            // 部活動
            final String sql5 = getClubSql();
//            log.debug(" getClubSQL =" + sql5);
            ps = db2.prepareStatement(sql5);

            for (Iterator it = schList.iterator(); it.hasNext();) {
                Student student = (Student) it.next();

                ps.setString(1, student._schoolKind);
                ps.setString(2, student._schregNo);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String clubName = rs.getString("CLUBNAME");

                    student._clubList.add(clubName);
                }
            }

            // 大会記録等
            final String sql6 = getClubHdetailSql();
//            log.debug(" getClubHdetailSQL =" + sql6);
            ps = db2.prepareStatement(sql6);

            for (Iterator it = schList.iterator(); it.hasNext();) {
                Student student = (Student) it.next();

                ps.setString(1, student._schoolKind);
                ps.setString(2, student._schregNo);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String meetName = rs.getString("MEET_NAME");

                    student._clubRecordList.add(meetName);
                }
            }

            // 取得資格
            final String sql7 = getQualifiedHobbyDatSql();
//            log.debug(" getQualifiedSQL =" + sql7);
            ps = db2.prepareStatement(sql7);

            for (Iterator it = schList.iterator(); it.hasNext();) {
                Student student = (Student) it.next();

                ps.setString(1, student._schregNo);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String qualified = rs.getString("QUALIFIED");

                    student._qualifiedList.add(qualified);
                }
            }

        } catch (SQLException ex) {
            log.error("etc... Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    // 総合（目標、評価）、出欠備考SQL
    private String getHreportSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     H1.TOTALSTUDYTIME, ");
        stb.append("     H1.ATTENDREC_REMARK, ");
        stb.append("     H1.COMMUNICATION, ");
        stb.append("     H2.REMARK4 ");
        stb.append(" FROM ");
        stb.append("     HREPORTREMARK_DAT H1 ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT H2 ON H2.YEAR     = H1.YEAR ");
        stb.append("                                          AND H2.SEMESTER = H1.SEMESTER ");
        stb.append("                                          AND H2.SCHREGNO = H1.SCHREGNO ");
        stb.append("                                          AND H2.DIV      = '07' ");
        stb.append("                                          AND H2.CODE     = '00' ");
        stb.append(" WHERE ");
        stb.append("         H1.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND H1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND H1.SCHREGNO = ? ");

        return stb.toString();
    }

    // 総合（観点）SQL
    private String getHreportDetailSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     H1.CODE, ");
        stb.append("     H1.REMARK1 ");
        stb.append(" FROM ");
        stb.append("     HREPORTREMARK_DETAIL_DAT H1 ");
        stb.append(" WHERE ");
        stb.append("         H1.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND H1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND H1.SCHREGNO = ? ");
        stb.append("     AND H1.DIV      = '07' ");

        return stb.toString();
    }

    // 行動の記録SQL
    private String getBehaviorDatSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     B1.CODE, ");
        stb.append("     B1.RECORD ");
        stb.append(" FROM ");
        stb.append("     BEHAVIOR_SEMES_DAT B1 ");
        stb.append(" WHERE ");
        stb.append("         B1.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND B1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND B1.SCHREGNO = ? ");

        return stb.toString();
    }

    // 委員会SQL
    private String getSchregCommitteeHistSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     C1.COMMITTEE_FLG, ");
        stb.append("     C2.COMMITTEENAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_COMMITTEE_HIST_DAT C1 ");
        stb.append("     LEFT JOIN COMMITTEE_MST C2 ON C2.SCHOOLCD      = C1.SCHOOLCD ");
        stb.append("                               AND C2.SCHOOL_KIND   = C1.SCHOOL_KIND ");
        stb.append("                               AND C2.COMMITTEE_FLG = C1.COMMITTEE_FLG ");
        stb.append("                               AND C2.COMMITTEECD   = C1.COMMITTEECD ");
        stb.append(" WHERE ");
        stb.append("         C1.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("     AND C1.SCHOOL_KIND = ? ");
        stb.append("     AND C1.YEAR        = '" + _param._ctrlYear + "' ");
        if (!_param._semester9) {
            stb.append("     AND C1.SEMESTER  IN ('" + _param._semester + "', '9') ");
        }
        stb.append("     AND C1.SCHREGNO    = ? ");
        stb.append(" ORDER BY ");
        stb.append("     C1.SEQ desc ");

        return stb.toString();
    }

    // 部活動SQL
    private String getClubSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     C2.CLUBNAME || ");
        stb.append("     case ");
        stb.append("        when C1.EXECUTIVECD is null or C1.EXECUTIVECD = '0' then '' ");
        stb.append("        else '(' || J001.NAME1 || ')' ");
        stb.append("     end as CLUBNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_CLUB_HIST_DAT C1 ");
        stb.append("     LEFT JOIN CLUB_MST C2 ON C2.SCHOOLCD    = C1.SCHOOLCD ");
        stb.append("                          AND C2.SCHOOL_KIND = C1.SCHOOL_KIND ");
        stb.append("                          AND C2.CLUBCD      = C1.CLUBCD ");
        stb.append("     LEFT JOIN V_NAME_MST J001 ON J001.YEAR    = '" + _param._ctrlYear + "' ");
        stb.append("                              AND J001.NAMECD1 = 'J001' ");
        stb.append("                              AND J001.NAMECD2 = C1.EXECUTIVECD ");
        stb.append(" WHERE ");
        stb.append("         C1.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("     AND C1.SCHOOL_KIND = ? ");
        stb.append("     AND C1.SCHREGNO    = ? ");
        stb.append("     AND '" + _param._idouDate + "' between C1.SDATE and value(C1.EDATE, '9999-12-31') ");
        stb.append(" ORDER BY ");
        stb.append("     C1.SDATE desc ");

        return stb.toString();
    }

    // 大会記録等SQL
    private String getClubHdetailSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     C1.MEET_NAME || value(M1.RECORDNAME, '') as MEET_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_CLUB_HDETAIL_DAT C1 ");
        stb.append("     LEFT JOIN CLUB_RECORD_MST M1 ON M1.SCHOOLCD    = C1.SCHOOLCD ");
        stb.append("                                 AND M1.SCHOOL_KIND = C1.SCHOOL_KIND ");
        stb.append("                                 AND M1.RECORDCD    = C1.RECORDCD ");
        stb.append(" WHERE ");
        stb.append("         C1.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("     AND C1.SCHOOL_KIND = ? ");
        stb.append("     AND C1.SCHREGNO    = ? ");
        stb.append("     AND C1.DETAIL_DATE <= '" + _param._idouDate + "' ");
        stb.append(" ORDER BY ");
        stb.append("     C1.DETAIL_DATE desc, ");
        stb.append("     C1.DETAIL_SEQ desc ");

        return stb.toString();
    }

    // 資格取得SQL
    private String getQualifiedHobbyDatSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     QMST.QUALIFIED_NAME || value(H312.NAME1, '') as QUALIFIED ");
        stb.append(" FROM ");
        stb.append("     SCHREG_QUALIFIED_HOBBY_DAT HOBY ");
        stb.append("     LEFT JOIN QUALIFIED_MST QMST ON QMST.QUALIFIED_CD = HOBY.QUALIFIED_CD ");
        stb.append("     LEFT JOIN V_NAME_MST H312 ON H312.YEAR    = HOBY.YEAR ");
        stb.append("                              AND H312.NAMECD1 = 'H312' ");
        stb.append("                              AND H312.NAMECD2 = HOBY.RANK ");
        stb.append(" WHERE ");
        stb.append("         HOBY.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND HOBY.SCHREGNO = ? ");
        stb.append("     AND HOBY.REGDDATE <= '" + _param._idouDate + "' ");
        stb.append(" ORDER BY ");
        stb.append("     HOBY.REGDDATE desc, ");
        stb.append("     HOBY.SEQ desc ");

        return stb.toString();
    }

    private String trim(final String str) {
    	if (null == str) {
    		return null;
    	}
    	String t = StringUtils.trim(str);
    	int start = 0;
    	while (start < t.length() && t.charAt(start) == '　') {
    		start += 1;
    	}
    	t = t.substring(start);
    	if (t.length() > 0) {
    		int end = t.length();
    		while (0 <= end - 1 && t.charAt(end - 1) == '　') {
    			end -= 1;
    		}
    		t = t.substring(0, end);
    	}
    	return t;
    }

    private static class Student {
        final String _schregNo;
        final String _birthDay;
        final String _schoolKind;
        final String _gradeCd;
        final String _grade;
        final String _hrClass;
        final String _hrClassName;
        final String _hrClassName1;
        final String _attendNo;
        final String _name;
        final String _cmcCd;
        final String _courseCodeName;
        final String _hrClassStaffName1;
        final String _hrClassStaffName2;
        String _gradeHrclassnameAttendno = "";
        String _totalStudyTime = null;
        String _remark4 = null;
        String _attendrecRemark = null;
        String _communication = null;

        /** 成績 */
        final Map _rankMap = new TreeMap();
        final Map _subclassCntMap = new TreeMap();

        /** 観点 */
        final Map _viewMap = new TreeMap();

        /** 出欠 */
        final Map _attendanceMap = new TreeMap();

        /** 総合的な学習 */
        final Map _sougouMap = new TreeMap();

        /** 行動の記録 */
        final Map _behaviorMap = new TreeMap();

        /** 委員会 */
        String _committee = null;

        /** クラス係 */
        String _kakari = null;

        /** 部活動 */
        final List _clubList = new ArrayList();

        /** 取得資格 */
        final List _qualifiedList = new ArrayList();

        /** 大会記録等 */
        final List _clubRecordList = new ArrayList();

        public Student(
                final String schregNo,
                final String birthDay,
                final String schoolKind,
                final String gradeCd,
                final String grade,
                final String hrClass,
                final String hrClassName,
                final String hrClassName1,
                final String attendNo,
                final String name,
                final String cmcCd,
                final String courseCodeName,
                final String hrClassStaffName1,
                final String hrClassStaffName2
        ) {
            _schregNo           = schregNo;
            _birthDay           = birthDay;
            _schoolKind         = schoolKind;
            _gradeCd            = gradeCd;
            _grade              = grade;
            _hrClass            = hrClass;
            _hrClassName        = hrClassName;
            _hrClassName1       = hrClassName1;
            _attendNo           = attendNo;
            _name               = name;
            _cmcCd              = cmcCd;
            _courseCodeName     = courseCodeName;
            _hrClassStaffName1  = hrClassStaffName1;
            _hrClassStaffName2  = hrClassStaffName2;
        }
        
        static Student getStudent(final String schregno, final List students) {
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student._schregNo.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }
    }

    /**
     * 1日出欠データ
     */
    private static class Attendance {
        /** 授業日数 */
        int _lesson;
        /** 忌引 */
        int _mourning;
        /** 出停 */
        int _suspend;
        /** 交止 */
        int _koudome;
        /** 出停伝染病 */
        int _virus;
        /** 留学 */
        int _abroad;
        /** 出席すべき日数 */
        int _mlesson;
        /** 欠席 */
        int _absence;
        /** 出席 */
        int _attend;
        /** 遅刻 */
        int _late;
        /** 早退 */
        int _leave;
        /** 欠課 */
        int _kekka;

        public Attendance() {
            this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        public Attendance(
                final int lesson,
                final int mourning,
                final int suspend,
                final int koudome,
                final int virus,
                final int abroad,
                final int mlesson,
                final int absence,
                final int attend,
                final int late,
                final int leave
        ) {
            _lesson     = lesson;
            _mourning   = mourning;
            _suspend    = suspend;
            _koudome    = koudome;
            _virus      = virus;
            _abroad     = abroad;
            _mlesson    = mlesson;
            _absence    = absence;
            _attend     = attend;
            _late       = late;
            _leave      = leave;
        }

        public void add(final Attendance att) {
            _lesson     = _lesson + att._lesson;
            _mourning   = _mourning + att._mourning;
            _suspend    = _suspend + att._suspend;
            _koudome    = _koudome + att._koudome;
            _virus      = _virus + att._virus;
            _abroad     = _abroad + att._abroad;
            _mlesson    = _mlesson + att._mlesson;
            _absence    = _absence + att._absence;
            _attend     = _attend + att._attend;
            _late       = _late + att._late;
            _leave      = _leave + att._leave;
            _kekka      = _kekka + att._kekka;
        }
    }

    private class ViewRecord {
        final String _status;
        final String _score;
        final String _value;
        public ViewRecord (
                final String status,
                final String score,
                final String value
                ) {
            _status = status;
            _score  = score;
            _value  = value;
        }
    }

    private class Rank {
        final String _score;
        final String _avg;
        final String _courseRank;
        final String _remark;
        public Rank (
                final String score,
                final String avg,
                final String courseRank,
                final String remark
                ) {
            _score      = score;
            _avg        = avg;
            _courseRank = courseRank;
            _remark     = remark;
        }
    }

    private static class CourseAvg {
        final String _courseCount;
        final String _courseAvg;
        final String _courseAvgAvg;
        public CourseAvg (
                final String courseCount,
                final String courseAvg,
                final String courseAvgAvg
                ) {
            _courseCount  = courseCount;
            _courseAvg    = courseAvg;
            _courseAvgAvg = courseAvgAvg;
        }
    }

    private static class Semester {
        final String _semester;
        final String _semesterName;
        final String _sDate;
        final String _eDate;
        public Semester (
                final String semester,
                final String semesterName,
                final String sDate,
                final String eDate
                ) {
            _semester       = semester;
            _semesterName   = semesterName;
            _sDate          = sDate;
            _eDate          = eDate;
        }
    }

    private static class Subclass {
        final String _classCd;
        final String _className;
        final String _subClassName;
        public Subclass (
                final String classCd,
                final String className,
                final String subClassName
                ) {
            _classCd      = classCd;
            _className    = className;
            _subClassName = subClassName;
        }
    }

    private static class ViewName {
        final String _viewCd;
        final String _viewName;
        public ViewName (
                final String viewCd,
                final String viewName
                ) {
            _viewCd     = viewCd;
            _viewName   = viewName;
        }
    }

    private static class CertifSchoolDat {
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        public CertifSchoolDat (
                final String schoolName,
                final String jobName,
                final String principalName
                ) {
            _schoolName     = schoolName;
            _jobName        = jobName;
            _principalName  = principalName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 70553 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _semester;
        private final String _semester2;
        private final String _semesterName;
        private final boolean _semester9;
        /** 異動対象日付 */
        private final String _idouDate;
        /** checkbox【表紙】 */
        private final boolean _coverCheckBox;
        /** checkbox【成績】 */
        private final boolean _scoreCheckBox;
        /** checkbox【修了証】 */
        private final boolean _completionCheckBox;
        private final String _testCd;
        private final String _testKindCd;
        /** _testCdの頭２桁が"99"の時 true */
        private final boolean _testCd99;
        private final String _testCdName;
        private final String _gradeHrclass;
        private final String _grade;
        private final String _schoolCd;
        private final String _useCurriculumcd;
        private final String _useVirus;
        private final String _useKoudome;
        private final String _useHyoukaHyouteiFlg;
        private final String _useTestCountflg;
        private final boolean _isOutputDebug;
        private final List _semsterList;

        private final CertifSchoolDat _certifSchoolDat;

        private final String[] _schregNos;

        /* 科目マスタ */
        private final Map _subClassMst;

        /* 合併科目 */
        private final Map _subClassRepMap;

        /* 試験科目 */
        private final Map _subClassMap;

        /* コース情報セット */
        private final Map _courseAvgMap;

        /* 観点セット(観点別学習状況) */
        private final Map _viewNameMap;

        /* 観点セット(総合) */
        private final Map _sougouKantenMap;

        /* 総合、評価変換マップ */
        private final Map _sougouHenkanMap;

        /* 生活および行動の記録 */
        private final Map _koudouKirokuMap;

        /* 行動の記録、評価変換マップ */
        private final Map _koudouHenkanMap;

        /** 出欠情報取得SQLに使う */
        private final Map _attendParamMap;
        private final Map _subclassAttendParamMap;
        private final Map _eDateMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear               = request.getParameter("CTRL_YEAR");
            _ctrlSemester           = request.getParameter("CTRL_SEMESTER");
            _ctrlDate               = request.getParameter("CTRL_DATE");
            _semester               = request.getParameter("SEMESTER");
            _semester2              = "9".equals(request.getParameter("SEMESTER")) ? _ctrlSemester: request.getParameter("SEMESTER");
            _semesterName           = getSemesterName(db2);
            _semester9              = "9".equals(request.getParameter("SEMESTER"));
            _idouDate               = StringUtils.replace(request.getParameter("DATE"), "/","-");
            _coverCheckBox          = "1".equals(request.getParameter("COVER"));
            _scoreCheckBox          = "1".equals(request.getParameter("SCORE"));
            _completionCheckBox     = "1".equals(request.getParameter("COMPLETION"));
            _testCd                 = request.getParameter("TESTCD");
            _testKindCd             = request.getParameter("TESTCD").substring(0, 2);
            _testCd99               = "99".equals(_testKindCd);
            _testCdName             = getTestName(db2);
            _gradeHrclass           = request.getParameter("GRADE_HR_CLASS");
            _grade                  = request.getParameter("GRADE_HR_CLASS").substring(0, 2);
            _schoolCd               = request.getParameter("SCHOOLCD");
            _useCurriculumcd        = request.getParameter("useCurriculumcd");
            _useVirus               = request.getParameter("useVirus");
            _useKoudome             = request.getParameter("useKoudome");
            _useHyoukaHyouteiFlg    = request.getParameter("useHyoukaHyouteiFlg");
            _useTestCountflg        = request.getParameter("useTestCountflg");
            _semsterList            = getSemsterList(db2, _ctrlYear);

            _certifSchoolDat        = getCertifSchoolDat(db2);

            // 学籍番号の指定
            _schregNos = request.getParameterValues("CATEGORY_SELECTED"); //学籍番号

            _subClassMst  = getSubClassMst(db2);

            _subClassMap  = getSubClassMap(db2, _ctrlYear, _semester, _testCd, _schregNos);
            _courseAvgMap = getCourseAvgMap(db2, _ctrlYear, _semester, _testCd, _grade);
            _eDateMap     = getEdateMap(db2, _ctrlYear);
            _subClassRepMap = getSubClassReplace(db2);

            _viewNameMap      = getJviewNameMap(db2, _ctrlYear, _grade);

            _sougouHenkanMap  = getNameMstMap(db2, _ctrlYear, "D102", "NAME1", "NAMESPARE1");
            _koudouHenkanMap  = getNameMstMap(db2, _ctrlYear, "D036", "NAME1", "NAMESPARE1");
            _sougouKantenMap  = getNameMstMap(db2, _ctrlYear, "D101", "NAMECD2", "NAME1");
            _koudouKirokuMap  = getBehaviorSemesMstMap(db2, _ctrlYear, _grade);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            
            _subclassAttendParamMap = new HashMap();
            _subclassAttendParamMap.put("DB2UDB", db2);
            _subclassAttendParamMap.put("useCurriculumcd", _useCurriculumcd);
            _subclassAttendParamMap.put("HttpServletRequest", request);
            _subclassAttendParamMap.put("grade", _grade);
            _subclassAttendParamMap.put("hrClass", _gradeHrclass.substring(2, 5));
            _subclassAttendParamMap.put("useTestCountflg", _useTestCountflg);
            
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }
        
        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD184M' AND NAME = '" + propName + "' "));
        }

        private String getSemesterName(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTERNAME ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST T1 ");
            stb.append(" WHERE ");
            stb.append("         T1.YEAR     = '" + _ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + _semester + "' ");
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString())));
        }

        private String getTestName(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TESTITEMNAME ");
            stb.append(" FROM ");
            stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
            stb.append(" WHERE ");
            stb.append("         T1.YEAR     = '" + _ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + _semester + "' ");
            stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _testCd + "' ");
            
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString()));
            String testCdName = StringUtils.defaultString(KnjDbUtils.getString(row, "TESTITEMNAME"));
            if (testCdName.endsWith("素点")) {
            	testCdName = testCdName.substring(0, testCdName.length() - 2) + "試験素点";
            } else {
            	testCdName = testCdName + "試験";
            }
            return testCdName;
        }

        private List getSemsterList(final DB2UDB db2, final String year) {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SEME.SEMESTER, ");
                stb.append("     SEME.SEMESTERNAME, ");
                stb.append("     SEME.SDATE, ");
                stb.append("     SEME.EDATE ");
                stb.append(" FROM ");
                stb.append("     SEMESTER_MST SEME ");
                stb.append(" WHERE ");
                stb.append("         SEME.YEAR     = '" + year + "' ");
                stb.append("     AND SEME.SEMESTER <> '9' ");
                stb.append(" ORDER BY ");
                stb.append("     SEME.SEMESTER ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester       = rs.getString("SEMESTER");
                    final String semesterName   = rs.getString("SEMESTERNAME");
                    final String sDate          = rs.getString("SDATE");
                    final String eDate          = rs.getString("EDATE");

                    retList.add(new Semester(semester, semesterName, sDate, eDate));
                }
            } catch (SQLException ex) {
                log.error("getSemester exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        private Map getSubClassMst(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT ");
                stb.append("     SUB.CLASSCD, ");
                stb.append("     SUB.CLASSCD || '-' || SUB.SCHOOL_KIND || '-' || SUB.CURRICULUM_CD || '-' || SUB.SUBCLASSCD as SUBCLASSCD, ");
                stb.append("     CLAS.CLASSNAME, ");
                stb.append("     SUB.SUBCLASSNAME ");
                stb.append(" FROM ");
                stb.append("     SUBCLASS_MST SUB ");
                stb.append("     LEFT JOIN  CLASS_MST CLAS ON CLAS.CLASSCD     = SUB.CLASSCD ");
                stb.append("                              AND CLAS.SCHOOL_KIND = SUB.SCHOOL_KIND ");
                stb.append(" ORDER BY ");
                stb.append("     SUBCLASSCD ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classCd        = rs.getString("CLASSCD");
                    final String subclassCd     = rs.getString("SUBCLASSCD");
                    final String className      = rs.getString("CLASSNAME");
                    final String subclassName   = rs.getString("SUBCLASSNAME");

                    retMap.put(subclassCd, new Subclass(classCd, className, subclassName));
                }
            } catch (SQLException ex) {
                log.error("getSubclass exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getSubClassMap(final DB2UDB db2, final String year, final String semester, final String testCd, final String[] schregNos) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT ");
                stb.append("     RANK.CLASSCD, ");
                stb.append("     RANK.CLASSCD || '-' || RANK.SCHOOL_KIND || '-' || RANK.CURRICULUM_CD || '-' || RANK.SUBCLASSCD as SUBCLASSCD, ");
                stb.append("     CLAS.CLASSNAME, ");
                stb.append("     SUBC.SUBCLASSNAME ");
                stb.append(" FROM ");
                stb.append("     RECORD_RANK_SDIV_DAT RANK ");
                stb.append("     INNER JOIN  SUBCLASS_MST SUBC ON SUBC.CLASSCD       = RANK.CLASSCD ");
                stb.append("                                  AND SUBC.SCHOOL_KIND   = RANK.SCHOOL_KIND ");
                stb.append("                                  AND SUBC.CURRICULUM_CD = RANK.CURRICULUM_CD ");
                stb.append("                                  AND SUBC.SUBCLASSCD    = RANK.SUBCLASSCD ");
                stb.append("     INNER JOIN  CLASS_MST CLAS ON CLAS.CLASSCD     = RANK.CLASSCD ");
                stb.append("                               AND CLAS.SCHOOL_KIND = RANK.SCHOOL_KIND ");
                stb.append(" WHERE ");
                stb.append("         RANK.YEAR     = '" + year + "' ");
                stb.append("     AND RANK.SEMESTER = '" + semester + "' ");
                stb.append("     AND RANK.TESTKINDCD || RANK.TESTITEMCD || RANK.SCORE_DIV = '" + testCd + "' ");
                stb.append("     AND RANK.SCHREGNO IN " + SQLUtils.whereIn(true, schregNos) + " ");
                stb.append(" ORDER BY ");
                stb.append("     SUBCLASSCD ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classCd        = rs.getString("CLASSCD");
                    final String subclassCd     = rs.getString("SUBCLASSCD");
                    final String className      = rs.getString("CLASSNAME");
                    final String subclassName   = rs.getString("SUBCLASSNAME");

                    retMap.put(subclassCd, new Subclass(classCd, className, subclassName));
                }
            } catch (SQLException ex) {
                log.error("getSubclass exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getJviewNameMap(final DB2UDB db2, final String year, final String grade) {
            Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD as SUBCLASSCD, ");
                stb.append("     T1.VIEWCD, ");
                stb.append("     T2.VIEWNAME ");
                stb.append(" FROM ");
                stb.append("     JVIEWNAME_GRADE_YDAT T1, ");
                stb.append("     JVIEWNAME_GRADE_MST T2 ");
                stb.append(" WHERE ");
                stb.append("         T1.YEAR          = '" + year + "' ");
                stb.append("     AND T1.GRADE         = '" + grade + "' ");
                stb.append("     AND T1.GRADE         = T2.GRADE ");
                stb.append("     AND T1.CLASSCD       = T2.CLASSCD ");
                stb.append("     AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ");
                stb.append("     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
                stb.append("     AND T1.SUBCLASSCD    = T2.SUBCLASSCD ");
                stb.append("     AND T1.VIEWCD        = T2.VIEWCD ");
                stb.append(" ORDER BY ");
                stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ");
                stb.append("     T1.VIEWCD ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String viewCd     = rs.getString("VIEWCD");
                    final String viewName   = rs.getString("VIEWNAME");

                    if (retMap.containsKey(subclassCd)) {
                        List list = (List) retMap.get(subclassCd);
                        list.add(new ViewName(viewCd, viewName));
                    } else {
                        List setList = new ArrayList();
                        setList.add(new ViewName(viewCd, viewName));
                        retMap.put(subclassCd, setList);
                    }
                }
            } catch (SQLException ex) {
                log.error("getJviewName exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getBehaviorSemesMstMap(final DB2UDB db2, final String year, final String grade) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CODE, ");
            stb.append("     CODENAME ");
            stb.append(" FROM ");
            stb.append("     BEHAVIOR_SEMES_MST ");
            stb.append(" WHERE ");
            stb.append("         YEAR  = '" + year + "' ");
            stb.append("     AND GRADE = '" + grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     CODE ");
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "CODE", "CODENAME");
        }

        private Map getNameMstMap(final DB2UDB db2, final String year, final String nameCd1, final String field1, final String field2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     " + field1 + " , ");
            stb.append("     " + field2 + " ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("         YEAR    = '" + year + "' ");
            stb.append("     AND NAMECD1 = '" + nameCd1 + "' ");
            if("D101".equals(nameCd1)) {
                stb.append("     AND NAMESPARE1 IS NOT NULL ");
                stb.append(" ORDER BY  ");
                stb.append("     NAMESPARE1 ");
            }
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), field1, field2);
        }

        private Map getCourseAvgMap(final DB2UDB db2, final String year, final String semester, final String testCd, final String grade) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT ");
                stb.append("     AVGD.COURSECD || AVGD.MAJORCD || AVGD.COURSECODE as CMC_CD, ");
                stb.append("     AVGD.CLASSCD || '-' || AVGD.SCHOOL_KIND || '-' || AVGD.CURRICULUM_CD || '-' || AVGD.SUBCLASSCD as SUBCLASSCD, ");
                stb.append("     AVGD.COUNT, ");
                stb.append("     AVGD.AVG, ");
                stb.append("     T_AVGAVG.AVGAVG ");
                stb.append(" FROM ");
                stb.append("     RECORD_AVERAGE_SDIV_DAT AVGD ");
                stb.append("     LEFT JOIN (SELECT ");
                stb.append("                    REGD.COURSECD, REGD.MAJORCD, REGD.COURSECODE, ");
                stb.append("                    AVG(T1.AVG) AS AVGAVG ");
                stb.append("                FROM RECORD_RANK_SDIV_DAT T1 ");
                stb.append("                INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
                stb.append("                    AND REGD.YEAR     = '" + year + "' ");
                stb.append("                    AND REGD.SEMESTER = '" + _semester2 + "' ");
                stb.append("                    AND REGD.GRADE    = '" + grade + "' ");
                stb.append("                WHERE ");
                stb.append("                    T1.YEAR     = '" + year + "' ");
                stb.append("                AND T1.SEMESTER = '" + semester + "' ");
                stb.append("                AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + testCd + "' ");
                stb.append("                AND T1.SUBCLASSCD = '999999' ");
                stb.append("                GROUP BY ");
                stb.append("                    REGD.COURSECD, REGD.MAJORCD, REGD.COURSECODE ");
                stb.append("                ) T_AVGAVG ON T_AVGAVG.COURSECD   = AVGD.COURSECD ");
                stb.append("                          AND T_AVGAVG.MAJORCD    = AVGD.MAJORCD ");
                stb.append("                          AND T_AVGAVG.COURSECODE = AVGD.COURSECODE ");
                stb.append("                          AND AVGD.SUBCLASSCD     = '999999' ");
                stb.append(" WHERE ");
                stb.append("         AVGD.YEAR     = '" + year + "' ");
                stb.append("     AND AVGD.SEMESTER = '" + semester + "' ");
                stb.append("     AND AVGD.TESTKINDCD || AVGD.TESTITEMCD || AVGD.SCORE_DIV = '" + testCd + "' ");
                stb.append("     AND AVGD.AVG_DIV  = '3' "); // 3:コース
                stb.append("     AND AVGD.GRADE    = '" + grade + "' ");
                stb.append("     AND AVGD.HR_CLASS = '000' ");
                stb.append(" ORDER BY ");
                stb.append("     SUBCLASSCD ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cmcCd      = rs.getString("CMC_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String count      = rs.getString("COUNT");
                    final String avg        = avgStr(rs.getBigDecimal("AVG"));
                    final String avgAvg     = avgStr(rs.getBigDecimal("AVGAVG"));

                    final String setKey = cmcCd + subclassCd;
                    retMap.put(setKey, new CourseAvg(count, avg, avgAvg));
                }
            } catch (SQLException ex) {
                log.error("getCourseAvg exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getEdateMap(final DB2UDB db2, final String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH EDATE_TABLE(MONTH, EDATE) AS ( ");
            stb.append("    VALUES ");
            String sep = "";
            for (int i = 0; i < MONTH_ARRAY.length; i++) {
                String month = MONTH_ARRAY[i];
                final String setYear = Integer.parseInt(month) < 4 ? String.valueOf(Integer.parseInt(year) + 1) : year;

                stb.append("    " + sep + "('" + month + "' , LAST_DAY(DATE('" + setYear + "-" + month + "-01')))  ");
                sep = ",";
            }
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     MONTH, ");
            stb.append("     EDATE ");
            stb.append(" FROM ");
            stb.append("     EDATE_TABLE ");
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "MONTH", "EDATE");
        }

        private CertifSchoolDat getCertifSchoolDat(final DB2UDB db2) {
            CertifSchoolDat rtn = new CertifSchoolDat(null, null, null);

            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     SCHOOL_NAME, ");
            sql.append("     JOB_NAME, ");
            sql.append("     PRINCIPAL_NAME ");
            sql.append(" FROM ");
            sql.append("     CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE ");
            sql.append("         YEAR          = '" + _ctrlYear + "' ");
            sql.append("     AND CERTIF_KINDCD = '103' ");
            //log.debug("certif_school_dat sql = " + sql.toString());

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String schoolName     = rs.getString("SCHOOL_NAME");
                    final String jobName        = rs.getString("JOB_NAME");
                    final String principalName  = rs.getString("PRINCIPAL_NAME");

                    rtn = new CertifSchoolDat(schoolName, jobName, principalName);
                }
            } catch (Exception ex) {
                log.error("CertifSchoolDat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private Map getSubClassReplace(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS C_SUBCLASSCD, ");
                stb.append("     ATTEND_CLASSCD   || '-' || ATTEND_SCHOOL_KIND   || '-' || ATTEND_CURRICULUM_CD   || '-' || ATTEND_SUBCLASSCD   AS A_SUBCLASSCD ");
                stb.append(" FROM ");
                stb.append("     SUBCLASS_REPLACE_COMBINED_DAT ");
                stb.append(" WHERE ");
                stb.append("         REPLACECD = '1' ");
                stb.append("     AND YEAR      = '" + _ctrlYear + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cSubclassCd     = rs.getString("C_SUBCLASSCD");
                    final String AsubclassCd     = rs.getString("A_SUBCLASSCD");

                    retMap.put(AsubclassCd, cSubclassCd);
                }
            } catch (SQLException ex) {
                log.error("getSubclass exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }
    }
}

// eof
