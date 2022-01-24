/*
 * $Id: d67bdafb6f7aa99afa1256634744d6e3bbeaee71 $
 *
 * 作成日: 2019/05/08
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 学校教育システム 賢者 [成績管理] 中学通知票
 */

public class KNJD185B {

    private static final Log log = LogFactory.getLog(KNJD185B.class);

    private boolean _hasData;

    private Param _param;

    KNJEditString knjobj = new KNJEditString();

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

        final List studentList = getStudentList(db2);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            student.load(db2, _param);

            // 表紙
            printSvfHyoshi(db2, svf, student);

            // 学習のようす等
            printSvfMainSeiseki(db2, svf, student, viewClassList);
            _hasData = true;
        }
    }

    private List getStudentList(final DB2UDB db2) {
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql(_param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");

                final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? StringUtils.defaultString(rs.getString("REAL_NAME")) : StringUtils.defaultString(rs.getString("NAME"));
                final String hrName = StringUtils.defaultString(rs.getString("HR_NAME"));
                final String attendno = StringUtils.defaultString(rs.getString("ATTENDNO"));
                final String staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                final Student student = new Student(schregno, name, hrName, attendno, staffname);
                studentList.add(student);
            }

        } catch (SQLException e) {
            log.error("exception!!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return studentList;
    }

    private String getStudentSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH SCHNO_A AS(");
        stb.append("    SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
        stb.append("    FROM    SCHREG_REGD_DAT T1 ");
        stb.append("            , V_SEMESTER_GRADE_MST T2 ");
        stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
        stb.append("        AND T1.SEMESTER = '"+ param._semester +"' ");
        stb.append("        AND T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.GRADE = T2.GRADE ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("        AND T1.GRADE||T1.HR_CLASS = '" + param._gradeHrclass + "' ");
        stb.append("        AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
        //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
        //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
        stb.append("                             OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
        //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append("    ) ");
        //メイン表
        stb.append("SELECT  T1.SCHREGNO, ");
        stb.append("        T7.HR_NAME, ");
        stb.append("        T1.ATTENDNO, ");
        stb.append("        T8.STAFFNAME, ");
        stb.append("        T5.NAME, ");
        stb.append("        T5.REAL_NAME, ");
        stb.append("        CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME ");
        stb.append("FROM    SCHNO_A T1 ");
        stb.append("        INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
        stb.append("        LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
        stb.append("        LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = '" + param._year + "' AND T7.SEMESTER = T1.SEMESTER AND T7.GRADE = T1.GRADE AND T7.HR_CLASS = T1.HR_CLASS ");
        stb.append("        LEFT JOIN STAFF_MST T8 ON T8.STAFFCD = T7.TR_CD1 ");
        stb.append("ORDER BY ATTENDNO");
        return stb.toString();
    }

    protected void VrsOutRenban(final Vrw32alp svf, final String field, final List list) {
        if (null != list) {
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOut(field, (String) list.get(i));
            }
        }
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final List list) {
        if (null != list) {
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOutn(field, i + 1, (String) list.get(i));
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

    /**
     * 表紙を印刷する
     * @param db2
     * @param svf
     * @param student
     */
    private void printSvfHyoshi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD185B_1.frm", 1);

        String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名

        String staffField1 = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 20 ? "2" : "1";
        svf.VrsOut("STAFF_NAME1_" + staffField1, _param._certifSchoolPrincipalName); //学校長氏名

        String staffField2 = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 20 ? "2" : "1";
        svf.VrsOut("STAFF_NAME2_" + staffField2, student._hrStaffName); //担任氏名

        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName); //学校名
        svf.VrEndPage();
    }

    /**
     * 学習のようす等を印刷する
     * @param db2
     * @param svf
     * @param student
     * @param viewClassList
     */
    private void printSvfMainSeiseki(final DB2UDB db2, final Vrw32alp svf, final Student student, final List viewClassList) {

        svf.VrSetForm("KNJD185B_2.frm", 4);

        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度"); //年度

        //生徒名、年組
        printSvfStudent(svf, student);

        if(_param._lastSemester.equals(_param._semester)) {
        	//学年末の時のみ印字

            //総合的な学習の時間
            printSvfReportTotalstudytime(svf, student);

            //特別活動の記録
            printSvfReport(svf, student);
        }


        //部活動
        printSvfClub(svf, student);

        //道徳、特記すべき事項
        printSvfHReportRemark(svf, student);

        //行動の記録
        printSvfReportActivity(svf, student);

        //出欠の記録
        printShukketsu(svf, student);

        //各教科の学習の記録
        printSvfViewRecord(svf, student, viewClassList);

        svf.VrEndPage();
    }

    /**
     * 『生徒名』『年組』を印字する
     * @param svf
     * @param student
     */
    private void printSvfStudent(final Vrw32alp svf, final Student student) {
        final String nameField = getMS932ByteLength(student._name) > 30 ? "3" : getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name);
        final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno;
        svf.VrsOut("HR_NAME", student._hrName + " " + attendno + "番");
    }

    /**
     * 総合的な学習の時間を印字する
     * @param svf
     * @param student
     */
    private void printSvfReportTotalstudytime(final Vrw32alp svf, final Student student) {

        for (final Iterator it = student._recordTotalstudytimeDat.iterator(); it.hasNext();) {
            final RecordTotalstudytimeDat recordTotalStudytimeDat = (RecordTotalstudytimeDat) it.next();

            VrsOutnRenban(svf, "TOTAL_ACT", knjobj.retDividString(recordTotalStudytimeDat._totalstudyact, 36, 4)); //学習活動
            VrsOutnRenban(svf, "TOTAL_VIEW", knjobj.retDividString(recordTotalStudytimeDat._remark1, 36, 4)); //観点
            VrsOutnRenban(svf, "TOTAL_VAL", knjobj.retDividString(recordTotalStudytimeDat._totalstudytime, 36, 4)); //評価

        }

    }

    /**
     * 特別活動の記録を印字する
     * @param svf
     * @param student
     */
    private void printSvfReport(final Vrw32alp svf, final Student student) {

      for (final Iterator it = student._hReportRemarkDetailDatList.iterator(); it.hasNext();) {
          final HReportRemarkDetailDat hReportRemarkDetailDat = (HReportRemarkDetailDat) it.next();
          VrsOutnRenban(svf, "SP_CLASS", knjobj.retDividString(hReportRemarkDetailDat._remark1, 30, 8)); //学級活動
          svf.VrsOut("SP_CLASS_STATUS", hReportRemarkDetailDat._remark1Chk); //学級活動 状況
          VrsOutnRenban(svf, "SP_COUNCIL", knjobj.retDividString(hReportRemarkDetailDat._remark2, 30, 8)); //生徒会活動
          svf.VrsOut("SP_COUNCIL_STATUS", hReportRemarkDetailDat._remark2Chk); //生徒会活動 状況
      }
    }

    /**
     * 部活動を印字する
     * @param svf
     * @param student
     */
    private void printSvfClub(final Vrw32alp svf, final Student student) {
        String clubName = "";
        String sep = "";

        for (final Iterator it = student._clubList.iterator(); it.hasNext();) {
            final Club club = (Club) it.next();
            clubName = clubName + sep + StringUtils.defaultString(club._clubname);
            sep = ",";
        }
        VrsOutnRenban(svf, "CLUB", knjobj.retDividString(clubName, 50, 2)); //部活動
    }

    /**
     * 『道徳』『特記すべき事項』を印字する
     * @param svf
     * @param student
     */
    private void printSvfHReportRemark(final Vrw32alp svf, final Student student) {

        final int pcharsttSE = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_H, 0);
        final int plinesttSE = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_H, 1);
        final int charsttSE = (-1 == pcharsttSE || -1 == plinesttSE) ? 50 : pcharsttSE; //文字数
        final int linesttSE = (-1 == pcharsttSE || -1 == plinesttSE) ?  3 : plinesttSE; //行数

        for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) it.next();
            VrsOutnRenban(svf, "MORAL", knjobj.retDividString(hReportRemarkDat._remark1, 50, 5)); //道徳
            svf.VrsOut("SEMESTER2", "特記すべき事項(" + _param._semesterName + ")"); //特記すべき事項 学期
            VrsOutnRenban(svf, "NOTICE", knjobj.retDividString(hReportRemarkDat._communication, charsttSE * 2, linesttSE)); //特記すべき事項
        }

    }


    /**
    * 行動の記録を印字する
     * @param svf
     * @param student
     */
    private void printSvfReportActivity(final Vrw32alp svf, final Student student) {
    	int idx = 1;
        for (final Iterator it = student._behaviorSemesDatList.iterator(); it.hasNext();) {
            final BehaviorSemesDat behaviorSemesDat = (BehaviorSemesDat) it.next();
            svf.VrsOutn("ACTION_NAME", idx, behaviorSemesDat._name1); //項目
            if(_param._lastSemester.equals(_param._semester)) {
                //学年末の時のみ印字
                svf.VrsOutn("ACTION_STATUS", idx, behaviorSemesDat._val); //状況
            }
            idx++;
        }
    }


    /**
    * 出欠の記録を印字する
     * @param svf
     * @param student
     */
    private void printShukketsu(final Vrw32alp svf, final Student student) {

        for (final Iterator it = student._attendSemesDatList.iterator(); it.hasNext();) {
            final AttendSemesDat attSemes = (AttendSemesDat) it.next();

            final int suspend = attSemes._suspend + attSemes._mourning + attSemes._virus + attSemes._koudome;

            //授業日数、出席停止・忌引等の日数、出席しなければならない日数、欠席日数、出席日数、遅刻、早退
            final int[] val = {attSemes._lesson, suspend, attSemes._mlesson, attSemes._sick, attSemes._present, attSemes._late, attSemes._early};

            for (int i = 0; i < val.length; i++) {
                svf.VrsOutn("ATTEND", i+1, String.valueOf(val[i]));
            }

        }

        String remark = "";
        String sep = "";
        for (final Iterator it = student._attendrecRemarkList.iterator(); it.hasNext();) {
            final AttendrecRemark attendrecRemark = (AttendrecRemark) it.next();
            remark = remark + sep + attendrecRemark._attendrecRemark;
            sep = "\r";
        }
        VrsOutnRenban(svf, "REMARK", knjobj.retDividString(remark, 50, 9)); //出欠備考
    }


    /**
     * 各教科の学習の記録を印字する
     * @param svf
     * @param student
     */
    private void printSvfViewRecord(final Vrw32alp svf, final Student student, final List viewClassList) {
        int grp = 0;
        int idx = 0;
        String subClassCd = "";
        String subClassName = "";
        String score = "";

        svf.VrsOut("SEMESTER1", _param._semesterName);

        for (final Iterator it = viewClassList.iterator(); it.hasNext();) {
            final ViewClass viewClass = (ViewClass) it.next();

            if ("1".equals(viewClass._electDiv) && !student.hasChairSubclass(viewClass._subclasscd)) { // 選択教科は講座名簿がある科目のみ表示対象
                continue;
            }

            for (int i = 0; i < viewClass.getViewSize(); i++) {

                //観点リスト
                final List viewRecordList = student.getViewList(viewClass._subclasscd, viewClass.getViewCd(i));
                for (final Iterator itv = viewRecordList.iterator(); itv.hasNext();) {

                    if(!subClassCd.equals(viewClass._subclasscd)) {
                        while (subClassName.length() > idx) {
                            //教科名を最終文字まで出力
                            final String setName = subClassName.length() > idx ? subClassName.substring(idx,idx+1) : "";
                            svf.VrsOut("CLASS_NAME", setName); // 教科名
                            svf.VrsOut("VAL", score); // 評定
                            svf.VrsOut("GRPCD", String.valueOf(grp)); // グループ
                            idx++;
                            svf.VrEndRecord();
                        }
                        subClassCd = viewClass._subclasscd;
                        subClassName = viewClass._subclassname;
                        grp++;
                        idx = 0;
                    }

                    final ViewRecord viewRecord = (ViewRecord) itv.next();

                    final String setName = subClassName.length() > idx ? subClassName.substring(idx,idx+1) : "";
                    svf.VrsOut("CLASS_NAME", setName); // 教科名
                    svf.VrsOut("VIEW_NAME", viewRecord._viewname); // 観点
                    svf.VrsOut("EVA", viewRecord._status); // 評価
                    score  =  viewRecord._score;
                    svf.VrsOut("VAL", score); // 評定

                    svf.VrsOut("GRPCD", String.valueOf(grp)); // グループ

                    idx++;
                    svf.VrEndRecord();
                }

            }
        }
    }

    private static int getMS932ByteLength(final String str) {
    	return KNJ_EditEdit.getMS932ByteLength(str);
    }


    /**
     * 生徒情報
     */
    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        final String _hrStaffName;
        List _viewRecordList = Collections.EMPTY_LIST; // 観点
        List _viewValuationList = Collections.EMPTY_LIST; // 評定
        List _chairSubclassList = Collections.EMPTY_LIST; //教科

        List _recordTotalstudytimeDat = Collections.EMPTY_LIST; // 総合的な学習の時間
        List _hReportRemarkDetailDatList = Collections.EMPTY_LIST; // 特別活動の記録
        List _clubList = Collections.EMPTY_LIST; //部活動
        List _hReportRemarkDatList = Collections.EMPTY_LIST; // 道徳
        List _behaviorSemesDatList = Collections.EMPTY_LIST; // 行動の記録
        Map _attendSemesMap = Collections.EMPTY_MAP; // 出欠の記録
        List _attendSemesDatList = Collections.EMPTY_LIST; // 出欠の記録
        List _attendrecRemarkList = Collections.EMPTY_LIST; // 出欠の記録(備考)

        public Student(final String schregno, final String name, final String hrName, final String attendno, final String hrStaffName) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
            _hrStaffName = hrStaffName;
        }

        public void load(final DB2UDB db2, final Param param) {
            _viewRecordList = ViewRecord.getViewRecordList(db2, param, _schregno);
            _chairSubclassList = ChairSubclass.load(db2, param, _schregno);
            _recordTotalstudytimeDat = RecordTotalstudytimeDat.getRecordTotalstudytimeDatList(db2, param, _schregno);
            _hReportRemarkDetailDatList = HReportRemarkDetailDat.getHReportRemarkDetailDatList(db2, param, _schregno);
            _clubList = Club.getClubList(db2, param, _schregno);
            _hReportRemarkDatList = HReportRemarkDat.getHReportRemarkDatList(db2, param, _schregno);
            _behaviorSemesDatList = BehaviorSemesDat.getBehaviorSemesDatList(db2, param, _schregno);
            _attendSemesDatList = AttendSemesDat.getAttendSemesDatMap(db2, param, _schregno);
            _attendrecRemarkList = AttendrecRemark.getAttendrecRemarkList(db2, param, _schregno);
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
                final String sql = getViewClassSql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final String electDiv = rs.getString("ELECTDIV");
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = StringUtils.defaultString(rs.getString("VIEWNAME"));

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

        private static String getViewClassSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T3.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     T4.SUBCLASSNAME, ");
            stb.append("     VALUE(T4.ELECTDIV, '0') AS ELECTDIV, ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     T1.VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            }
            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T3.CLASSCD < '90' ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T4.ELECTDIV, '0'), ");
            stb.append("     VALUE(T3.SHOWORDER3, -1), ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     VALUE(T1.SHOWORDER, -1), ");
            stb.append("     T1.VIEWCD ");
            return stb.toString();
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
        final String _score;
        ViewRecord(
                final String semester,
                final String viewcd,
                final String status,
                final String grade,
                final String viewname,
                final String classcd,
                final String subclasscd,
                final String classMstShoworder,
                final String showorder,
                final String score) {
            _semester = semester;
            _viewcd = viewcd;
            _status = status;
            _grade = grade;
            _viewname = viewname;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classMstShoworder = classMstShoworder;
            _showorder = showorder;
            _score = score;
        }

        public static List getViewRecordList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewRecordSql(param, schregno);
                log.debug(" view record sql = "+  sql);
                ps = db2.prepareStatement(sql);
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
                    final String score = rs.getString("SCORE");
                    final String attendFlg = rs.getString("ATTEND_FLG");
                    final String combinedFlg = rs.getString("COMBINED_FLG");
                    if ("1".equals(attendFlg) && param._isNoPrintMoto) {
                    	// 合併元科目を表示しない
                    	continue;
                    } else if ("1".equals(combinedFlg) && !param._isPrintSakiKamoku) {
                    	// 合併先科目を表示しない
                    	continue;
                    }

                    final ViewRecord viewRecord = new ViewRecord(semester, viewcd, status, grade, viewname, classcd, subclasscd, classMstShoworder, showorder, score);

                    list.add(viewRecord);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getViewRecordSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH REPLACE_COMBINED AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     'COMBINED' AS FLG, COMBINED_CLASSCD AS CLASSCD, COMBINED_SCHOOL_KIND AS SCHOOL_KIND, COMBINED_CURRICULUM_CD AS CURRICULUM_CD, COMBINED_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(" FROM SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append(" WHERE YEAR = '" + param._year + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     'ATTEND' AS FLG, ATTEND_CLASSCD AS CLASSCD, ATTEND_SCHOOL_KIND AS SCHOOL_KIND, ATTEND_CURRICULUM_CD AS CURRICULUM_CD, ATTEND_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(" FROM SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append(" WHERE YEAR = '" + param._year + "' ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("     T1.GRADE   ");
            stb.append("     , T1.VIEWNAME ");
            stb.append("     , T1.VIEWCD ");
            stb.append("     , T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , T3.SCHREGNO ");
            stb.append("     , T3.STATUS ");
            stb.append("     , T4.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("     , T1.SUBCLASSCD ");
            }
            stb.append("     , CASE WHEN SUB_ATT.FLG IS NOT NULL THEN '1' END AS ATTEND_FLG ");
            stb.append("     , CASE WHEN SUB_COM.FLG IS NOT NULL THEN '1' END AS COMBINED_FLG ");
            stb.append("     , T4.SHOWORDER AS CLASS_MST_SHOWORDER ");
            stb.append("     , T1.SHOWORDER ");
            stb.append("     , T5.SCORE ");
            stb.append(" FROM JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T3.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T3.YEAR = T2.YEAR ");
            stb.append("         AND T3.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND T3.SCHREGNO = '" + schregno + "' ");
            stb.append("     LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            }
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T5 ON T5.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T5.CLASSCD       = T1.CLASSCD  ");
                stb.append("         AND T5.SCHOOL_KIND   = T1.SCHOOL_KIND  ");
                stb.append("         AND T5.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T5.YEAR       = T2.YEAR ");
            stb.append("         AND T5.SEMESTER   = '" + param._semester + "' ");
            stb.append("         AND T5.TESTKINDCD = '99' ");
            stb.append("         AND T5.TESTITEMCD = '00' ");
            stb.append("         AND T5.SCORE_DIV  = '08' ");
            stb.append("         AND T5.SCHREGNO   = '" + schregno + "' ");
            stb.append("     LEFT JOIN REPLACE_COMBINED SUB_ATT ON SUB_ATT.FLG = 'ATTEND'   AND SUB_ATT.CLASSCD = T1.CLASSCD AND SUB_ATT.SCHOOL_KIND = T1.SCHOOL_KIND AND SUB_ATT.CURRICULUM_CD = T1.CURRICULUM_CD AND SUB_ATT.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN REPLACE_COMBINED SUB_COM ON SUB_COM.FLG = 'COMBINED' AND SUB_COM.CLASSCD = T1.CLASSCD AND SUB_COM.SCHOOL_KIND = T1.SCHOOL_KIND AND SUB_COM.CURRICULUM_CD = T1.CURRICULUM_CD AND SUB_COM.SUBCLASSCD = T1.SUBCLASSCD ");

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
     * 学習の記録
     */
    private static class Club {

        final String _clubcd;
        final String _clubname;
        Club(
                final String clubcd,
                final String clubname) {
            _clubcd = clubcd;
            _clubname = clubname;
        }

        public static List getClubList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getClubSql(param, schregno);
                log.debug(" view record sql = "+  sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String clubcd = StringUtils.defaultString(rs.getString("CLUBCD"));
                    final String clubname = StringUtils.defaultString(rs.getString("CLUBNAME"));

                    final Club club = new Club(clubcd, clubname);

                    list.add(club);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getClubSql(final Param param, final String schregno) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("  T1.CLUBCD, ");
            stb.append("  CM.CLUBNAME ");
            stb.append(" FROM  ");
            stb.append("  SCHREG_CLUB_HIST_DAT T1 ");
            stb.append("  INNER JOIN CLUB_MST CM ");
            stb.append("     ON CM.SCHOOLCD    = T1.SCHOOLCD ");
            stb.append("    AND CM.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND CM.CLUBCD      = T1.CLUBCD ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND T1.SDATE >= '" + param._sDate + "' ");
            stb.append("     AND T1.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append(" ORDER BY  ");
            stb.append("  T1.CLUBCD ");

            return stb.toString();
        }
    }

    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {

        final String _year;
        int _lesson;
        int _suspend;
        int _mourning;
        int _mlesson;
        int _sick;
        int _absent;
        int _present;
        int _late;
        int _early;
        int _abroad;
        int _offdays;
        int _virus;
        int _koudome;

        private AttendSemesDat(
                final String year,
                final int lesson,
                final int suspend,
                final int mourning,
                final int mlesson,
                final int sick,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int abroad,
                final int offdays,
                final int virus,
                final int koudome
        ) {
            _year = year;
            _lesson = lesson;
            _suspend = suspend;
            _mourning = mourning;
            _mlesson = mlesson;
            _sick = sick;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _abroad = abroad;
            _offdays = offdays;
            _virus = virus;
            _koudome = koudome;

        }

        public static List getAttendSemesDatMap(final DB2UDB db2, final Param param, final String schregno) {
        	final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("   YEAR, ");
                sql.append("   SUM(LESSON)   AS LESSON, ");
                sql.append("   SUM(ABROAD)   AS ABROAD, ");
                sql.append("   SUM(OFFDAYS)  AS OFFDAYS, ");
                sql.append("   SUM(SUSPEND)  AS SUSPEND, ");
                sql.append("   SUM(MOURNING) AS MOURNING, ");
                sql.append("   SUM(SICK)     AS SICK, ");
                sql.append("   SUM(NOTICE)   AS NOTICE, ");
                sql.append("   SUM(NONOTICE) AS NONOTICE, ");
                sql.append("   SUM(VIRUS)    AS VIRUS, ");
                sql.append("   SUM(KOUDOME)  AS KOUDOME, ");
                sql.append("   SUM(ABSENT)   AS ABSENT, ");
                sql.append("   SUM(LATE)     AS LATE, ");
                sql.append("   SUM(EARLY)    AS EARLY ");
                sql.append(" FROM ");
                sql.append("   ATTEND_SEMES_DAT ");
                sql.append(" WHERE YEAR = '" + param._year + "' ");
                sql.append("   AND SEMESTER <= '" + param._semester + "' ");
                sql.append("   AND SCHREGNO = '" + schregno + "' ");
                sql.append(" GROUP BY ");
                sql.append("  YEAR ");

                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                        final String year = rs.getString("YEAR");
                        final int lesson0 = rs.getInt("LESSON");
                        final int abroad = rs.getInt("ABROAD");
                        final int offdays = rs.getInt("OFFDAYS");
                        final int lesson = lesson0 - abroad - offdays + ("1".equals(param._knjSchoolMst._semOffDays) ? offdays : 0);
                        final int suspend = rs.getInt("SUSPEND");
                        final int mourning = rs.getInt("MOURNING");
                        final int sick = rs.getInt("SICK") + rs.getInt("NOTICE") + rs.getInt("NONOTICE");
                        final int absent = rs.getInt("ABSENT");
                        final int virus = "true".equals(param._useVirus) ? rs.getInt("VIRUS") : 0;
                        final int koudome = "true".equals(param._useKoudome) ? rs.getInt("KOUDOME") : 0;
                        final int mlesson = lesson - suspend - virus - koudome - mourning;
                        final int present = mlesson - sick;
                        final int late = rs.getInt("LATE");
                        final int early = rs.getInt("EARLY");

                        final AttendSemesDat attendSemesDat = new AttendSemesDat(year,lesson,suspend,mourning,mlesson,sick,absent,present,late,early,abroad,offdays,virus,koudome);
                        list.add(attendSemesDat);

                    }
                } catch (SQLException e) {
                    log.error("exception!", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
                return list;
            }

    }

    /**
     * 出欠の記録(備考)
     */
    private static class AttendrecRemark {
        final String _semester; //学期
        final String _attendrecRemark; //出欠備考

        public AttendrecRemark(
                final String semester,
                final String attendrecRemark
                ) {
            _semester = semester;
            _attendrecRemark = attendrecRemark;
        }

        public static List getAttendrecRemarkList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getAttendrecRemarkSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                    final String attendrecRemark = StringUtils.defaultString(rs.getString("ATTENDREC_REMARK"));

                    final AttendrecRemark attendrecRemarkList = new AttendrecRemark(semester, attendrecRemark);
                    list.add(attendrecRemarkList);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getAttendrecRemarkSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTER,");
            stb.append("     ATTENDREC_REMARK ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");

            return stb.toString();
        }
    }

    /**
     * 総合的な学習の時間
     */
    private static class RecordTotalstudytimeDat {
        final String _totalstudyact;      // 学習活動
        final String _remark1;            // 観点
        final String _totalstudytime;     // 評価

        public RecordTotalstudytimeDat(
                final String totalstudyact,
                final String remark1,
                final String totalstudytime) {
            _totalstudyact = totalstudyact;
            _remark1 = remark1;
            _totalstudytime = totalstudytime;
        }

        public static List getRecordTotalstudytimeDatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getRecordTotalstudytimeDatSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String totalstudyact = StringUtils.defaultString(rs.getString("TOTALSTUDYACT"));
                    final String remark1 = StringUtils.defaultString(rs.getString("REMARK1"));
                    final String totalstudytime = StringUtils.defaultString(rs.getString("TOTALSTUDYTIME"));
                    final RecordTotalstudytimeDat recordTotalstudytimeDat = new RecordTotalstudytimeDat(totalstudyact, remark1, totalstudytime);
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
            stb.append(" SELECT  ");
            stb.append("   TOTALSTUDYACT, ");
            stb.append("   REMARK1, ");
            stb.append("   TOTALSTUDYTIME ");
            stb.append(" FROM ");
            stb.append("   RECORD_TOTALSTUDYTIME_DAT ");
            stb.append(" WHERE  ");
            stb.append("   YEAR = '" + param._year + "' ");
            stb.append("   AND SEMESTER  = '9' ");
            stb.append("   AND SCHREGNO = '" + schregno + "' ");

            return stb.toString();
        }
    }

    /**
     * 特別活動の記録
     */
    private static class HReportRemarkDetailDat {
        final String _remark1;    //学級活動
        final String _remark1Chk; //学級活動 状況
        final String _remark2;    //生徒会活動
        final String _remark2Chk; //生徒会活動 状況

        public HReportRemarkDetailDat(
                final String remark1,
                final String remark1Chk,
                final String remark2,
                final String remark2Chk
                ) {
            _remark1 = remark1;
            _remark1Chk = remark1Chk;
            _remark2 = remark2;
            _remark2Chk = remark2Chk;
        }

        public static List getHReportRemarkDetailDatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHReportRemarkDetailSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String remark1 = StringUtils.defaultString(rs.getString("REMARK1"));
                    final String remark1Chk = StringUtils.defaultString(rs.getString("REMARK1_CHK"));
                    final String remark2 = StringUtils.defaultString(rs.getString("REMARK2"));
                    final String remark2Chk = StringUtils.defaultString(rs.getString("REMARK2_CHK"));

                    final HReportRemarkDetailDat hReportRemarkDetailDat = new HReportRemarkDetailDat(remark1, remark1Chk, remark2, remark2Chk);
                    list.add(hReportRemarkDetailDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getHReportRemarkDetailSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.REMARK1, ");
            stb.append("     CASE WHEN T2.REMARK1 IS NOT NULL THEN '○' ELSE '' END AS REMARK1_CHK, ");
            stb.append("     T1.REMARK2, ");
            stb.append("     CASE WHEN T2.REMARK2 IS NOT NULL THEN '○' ELSE '' END AS REMARK2_CHK ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DETAIL_DAT T1 ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT T2 ");
            stb.append("       ON T2.YEAR     = T1.YEAR ");
            stb.append("      AND T2.SEMESTER = T2.SEMESTER ");
            stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("      AND T2.DIV  = '01' ");
            stb.append("      AND T2.CODE = '02' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '9' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND T1.DIV  = '01' ");
            stb.append("     AND T1.CODE = '01' ");
            return stb.toString();
        }
    }


    /**
     * 『道徳』『特記すべき事項』
     */
    private static class HReportRemarkDat {
        final String _remark1; //道徳
        final String _communication; //特記すべき事項

        public HReportRemarkDat(
                final String remark1,
                final String communication
                ) {
            _remark1 = remark1;
            _communication = communication;
        }

        public static List getHReportRemarkDatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = gethReportRemarkDatSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String remark1 = StringUtils.defaultString(rs.getString("REMARK1"));
                    final String communication = StringUtils.defaultString(rs.getString("COMMUNICATION"));

                    final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(remark1,communication);
                    list.add(hReportRemarkDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String gethReportRemarkDatSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '9' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            return stb.toString();
        }
    }


    /**
     * 行動の記録
     */
    private static class BehaviorSemesDat {
        final String _namecd2; //コード
        final String _name1; //項目名
        final String _val; //データ

        public BehaviorSemesDat(
                final String namecd2,
                final String name1,
                final String val
                ) {
            _namecd2 = namecd2;
            _name1 = name1;
            _val = val;
        }

        public static List getBehaviorSemesDatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getBehaviorSemesDatSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String namecd2 = StringUtils.defaultString(rs.getString("NAMECD2"));
                    final String name1 = StringUtils.defaultString(rs.getString("NAME1"));
                    final String val = StringUtils.defaultString(rs.getString("VAL"));

                    final BehaviorSemesDat behaviorSemesDat = new BehaviorSemesDat(namecd2, name1, val);
                    list.add(behaviorSemesDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getBehaviorSemesDatSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("   D035.NAMECD2, ");
            stb.append("   D035.NAME1, ");
            stb.append("   CASE WHEN L1.CODE IS NOT NULL THEN '○' ELSE '' END AS VAL ");
            stb.append(" FROM  ");
            stb.append("   NAME_MST D035 ");
            stb.append("   LEFT JOIN BEHAVIOR_SEMES_DAT L1 ");
            stb.append("     ON L1.CODE     = D035.NAMECD2 ");
            stb.append("    AND L1.YEAR = '" + param._year + "' ");
            stb.append("    AND L1.SEMESTER = '3' ");
            stb.append("    AND L1.SCHREGNO = '" + schregno + "' ");
            stb.append(" WHERE ");
            stb.append("   D035.NAMECD1 = 'D035' ");
            stb.append(" ORDER BY ");
            stb.append("   D035.NAMECD2 ");

            return stb.toString();
        }
    }


    /**
     * 科目
     */
    private static class ChairSubclass {
        final String _subclasscd;
        final List _chaircdList;
        public ChairSubclass(final String subclasscd) {
            _subclasscd = subclasscd;
            _chaircdList = new ArrayList();
        }
        public static List load(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT DISTINCT ");
                sql.append("   T2.CHAIRCD, ");
                if ("1".equals(param._useCurriculumcd)) {
                    sql.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD ");
                } else {
                    sql.append("     T2.SUBCLASSCD ");
                }
                sql.append(" FROM ");
                sql.append("   CHAIR_STD_DAT T1 ");
                sql.append("   INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
                sql.append("     AND T2.SEMESTER = T1.SEMESTER ");
                sql.append("     AND T2.CHAIRCD = T1.CHAIRCD ");
                sql.append(" WHERE ");
                sql.append("     T1.YEAR = '" + param._year + "' ");
                sql.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
                sql.append("     AND T1.SCHREGNO = '" + schregno + "' ");
                sql.append("     AND '" + param._date + "' BETWEEN T1.APPDATE AND T1.APPENDDATE ");

                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    ChairSubclass cs = getChairSubclass(list, rs.getString("SUBCLASSCD"));
                    if (null == cs) {
                        cs = new ChairSubclass(rs.getString("SUBCLASSCD"));
                        list.add(cs);
                    }
                    cs._chaircdList.add(rs.getString("CHAIRCD"));
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 72962 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _date;
        final String _gradeHrclass;
        final String _grade;
        final String[] _categorySelected;
        final String _schoolKind;

        final String _sDate;
        final String _eDate;
        final String _semesterName;

        final String _certifSchoolSchoolName;
        final String _certifSchoolPrincipalName;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE_H;

        final String _lastSemester;
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;

        /** 各学校における定数等設定 */
        KNJSchoolMst _knjSchoolMst;
        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;

        final DecimalFormat _df02 = new DecimalFormat("00");

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            _categorySelected = request.getParameterValues("category_selected");
            _schoolKind = getSchoolKind(db2);

            _sDate = getSemesterMst(db2, "SDATE", "1");
            _eDate = getSemesterMst(db2, "EDATE", _semester);
            _semesterName = getSemesterMst(db2, "SEMESTERNAME", _semester);
            _lastSemester = getLastSemesterMst(db2);

            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _HREPORTREMARK_DAT_COMMUNICATION_SIZE_H = null == request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_H") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_H"), "+", " "); //特記すべき事項

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);
        }

        private String getSchoolKind(final DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString("SCHOOL_KIND")) {
                        rtn = StringUtils.defaultString(rs.getString("SCHOOL_KIND"));
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
                        rtn = StringUtils.defaultString(rs.getString(field));
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

        private String getSemesterMst(final DB2UDB db2, final String field, final String semester) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + semester + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = StringUtils.defaultString(rs.getString(field));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private String getLastSemesterMst(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT MAX(SEMESTER) FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER <> '9' "));
        }

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
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
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR = '" + _year+ "' AND NAMECD1 = 'D021' AND NAMECD2 = '01' "));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE3"))) {
                _isPrintSakiKamoku = false;
            }
            log.info("合併先科目を印刷するか：" + _isPrintSakiKamoku);
        }
    }
}

// eof

