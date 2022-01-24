/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: d626cf01fcf6bd567ca055631f79b0c197ba18c7 $
 *
 * 作成日: 2020/05/29
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD187L {

    private static final Log log = LogFactory.getLog(KNJD187L.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "89";

    private static final String SDIV1990009 = "1990009"; //1学期評定
    private static final String SDIV2990009 = "2990009"; //2学期評定
    private static final String SDIV3990009 = "3990009"; //3学期評定
    private static final String SDIV9990009 = "9990009"; //学年5段階評定

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

    private static final int SCORE_MAX_LINE = 9;

    private static final int VIEW_LEFT_MAX_LINE = 25; //観点別学習状況 左列 最大行数
    private static final int VIEW_RIGHT_MAX_LINE = 20; //観点別学習状況 右列 最大行数

//    private static final String HYOTEI_TESTCD = "9990009";

    private boolean _hasData;
    KNJEditString knjobj = new KNJEditString();

    private Param _param;

    /**
     * @param request リクエスト数
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
        final List viewClassList = ViewClass.getViewClassList(db2, _param);
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

            final String form = "KNJD187L.frm";
            svf.VrSetForm(form , 4);

            //明細部以外を印字
            printTitle(db2, svf, student);

            //学習成績
            printScore(db2, svf, student);

            //行動の記録
            printAct(db2, svf, student);

            //出欠の記録
            printAttend(svf, student);

            //各教科の学習の記録
            printSvfViewRecord(svf, student, viewClassList);

            svf.VrEndPage();

            _hasData = true;
        }
    }

    private void printTitle(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        //ヘッダ
        svf.VrsOut("GRADE", student._gradename); //学年
        svf.VrsOut("ATTENDNO", student._attendno); //出席番号
        String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 36 ? "2" : "1";
        svf.VrsOut("NAME"+nameField, student._name); //氏名
        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName); //学校名
        svf.VrsOut("PRINCIPAL_NAME", _param._certifSchoolPrincipalName); //校長名

        //宗教
        int line = 1;
    	final Map totalstudyMap = getSchregTotalstudy(db2, _param._loginYear, _param._semester, student._schregno);
    	for (final Iterator it = totalstudyMap.keySet().iterator(); it.hasNext();) {
            final String kye = (String) it.next();
            final Map map = (Map) totalstudyMap.get(kye);
            final String val = (String) map.get("TOTALSTUDYTIME");
            final List totalstudytime = knjobj.retDividString(val, 50, 6);
            for (int i = 0 ; i < totalstudytime.size(); i++) {
                svf.VrsOutn("MORAL", line, (String) totalstudytime.get(i)); //宗教
                line++;
            }
        }
    }

    /**
     * 観点別学習状況
     * @param svf
     * @param student
     */
    private void printSvfViewRecord(final Vrw32alp svf, final Student student, final List viewClassList) {
        int grp = 1;
        int line =1;
        boolean rightFlg = false;
        String subClassCd = "";
        List subClassNameList = new ArrayList();
        List viewNameList = new ArrayList();
        List view1List = new ArrayList();
        List view2List = new ArrayList();
        List view3List = new ArrayList();
        List view9List = new ArrayList();
        for (final Iterator it = viewClassList.iterator(); it.hasNext();) {
            final ViewClass viewClass = (ViewClass) it.next();

            if ("1".equals(viewClass._electDiv) && !student.hasChairSubclass(viewClass._subclasscd)) { // 選択教科は講座名簿がある科目のみ表示対象
                continue;
            }

            for (int i = 0; i < viewClass.getViewSize(); i++) {
                //観点リスト
                final List viewRecordList = student.getViewList(viewClass._subclasscd, viewClass.getViewCd(i));
                for (final Iterator itv = viewRecordList.iterator(); itv.hasNext();) {

                    final ViewRecord viewRecord = (ViewRecord) itv.next();

                    if(!"".equals(subClassCd) && !subClassCd.equals(viewRecord._subclasscd)) {
                        //教科が変わった場合

                    	//出力行数
                    	int maxLen = subClassNameList.size();
                    	if(maxLen < viewNameList.size()) maxLen = viewNameList.size();
                    	if(maxLen < view1List.size()) maxLen = view1List.size();
                    	if(maxLen < view2List.size()) maxLen = view2List.size();
                    	if(maxLen < view3List.size()) maxLen = view3List.size();
                    	if(maxLen < view9List.size()) maxLen = view9List.size();

                    	if(line + maxLen > VIEW_LEFT_MAX_LINE) {
                			//空行出力
                    		for (int j = line ; j <= VIEW_LEFT_MAX_LINE; j++) {
                            	svf.VrsOut("GRPCD", String.valueOf(grp)); // グループ
                            	svf.VrsOut("CLASS_NAME", "");
                            	svf.VrsOut("VIEW_NAME", "");
                            	svf.VrsOut("VIEW1", "");
                            	svf.VrsOut("VIEW2", "");
                            	svf.VrsOut("VIEW3", "");
                                svf.VrsOut("VIEW4", "");
                                svf.VrEndRecord();
                    		}
                            line = 1;
                            rightFlg = true;
                    	}

                        //出力
                        for (int j = 0 ; j < maxLen; j++) {
                        	svf.VrsOut("GRPCD", String.valueOf(grp)); // グループ
                        	if(j < subClassNameList.size()) svf.VrsOut("CLASS_NAME", (String) subClassNameList.get(j)); // 教科名
                        	if(j < viewNameList.size()) svf.VrsOut("VIEW_NAME", (String) viewNameList.get(j)); // 観点
                        	if(j < view1List.size()) svf.VrsOut("VIEW1", (String) view1List.get(j)); // 1学期 評価
                        	if(j < view2List.size() && _param._semes2Flg) svf.VrsOut("VIEW2", (String) view2List.get(j)); // 2学期 評価
                        	if(j < view3List.size() && _param._semes3Flg) svf.VrsOut("VIEW3", (String) view3List.get(j)); // 3学期 評価
                        	if(j < view9List.size() && _param._semes9Flg) svf.VrsOut("VIEW4", (String) view9List.get(j)); // 9学期 評価
                            svf.VrEndRecord();
                            line++;
                            if(rightFlg && line > VIEW_RIGHT_MAX_LINE) {
                            	return;
                            }
                        }
                        subClassNameList.clear();
                        viewNameList.clear();
                        view1List.clear();
                        view2List.clear();
                        view3List.clear();
                        view9List.clear();
                        grp++;
                    }

                    //出力内容
                    subClassCd = viewRecord._subclasscd;
                    subClassNameList = knjobj.retDividString(viewRecord._subclassname, 2, 999); //教科名
                    viewNameList.add(viewRecord._viewname);
                    view1List.add(viewRecord._status1);
                    view2List.add(viewRecord._status2);
                    view3List.add(viewRecord._status3);
                    view9List.add(viewRecord._status9);
                }
            }
        }
        //最終教科の処理
    	//出力行数
    	int maxLen = subClassNameList.size();
    	if(maxLen < viewNameList.size()) maxLen = viewNameList.size();
    	if(maxLen < view1List.size()) maxLen = view1List.size();
    	if(maxLen < view2List.size()) maxLen = view2List.size();
    	if(maxLen < view3List.size()) maxLen = view3List.size();
    	if(maxLen < view9List.size()) maxLen = view9List.size();

        //出力
        for (int j = 0 ; j < maxLen; j++) {
        	svf.VrsOut("GRPCD", String.valueOf(grp)); // グループ
        	if(j < subClassNameList.size()) svf.VrsOut("CLASS_NAME", (String) subClassNameList.get(j)); // 教科名
        	if(j < viewNameList.size()) svf.VrsOut("VIEW_NAME", (String) viewNameList.get(j)); // 観点
        	if(j < view1List.size()) svf.VrsOut("VIEW1", (String) view1List.get(j)); // 1学期 評価
        	if(j < view2List.size() && _param._semes2Flg) svf.VrsOut("VIEW2", (String) view2List.get(j)); // 2学期 評価
        	if(j < view3List.size() && _param._semes3Flg) svf.VrsOut("VIEW3", (String) view3List.get(j)); // 3学期 評価
        	if(j < view9List.size() && _param._semes9Flg) svf.VrsOut("VIEW4", (String) view9List.get(j)); // 9学期 評価
            svf.VrEndRecord();
            line++;
            if(!rightFlg && line > VIEW_RIGHT_MAX_LINE) return;
        }
    }


    private void printScore(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        //明細部
        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        //■成績一覧
        int idx = 1;
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclasscd;

            if (student._printSubclassMap.containsKey(subclassCd)) {
            	final String field = KNJ_EditEdit.getMS932ByteLength(subclassMst._classname) > 8 ? "3" : "1";
                svf.VrsOutn("CLASS_NAME" + field, idx, subclassMst._classname); //教科名

                final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);
                if (_param._isOutputDebug) {
                    log.info(" score = " + scoreData);
                }

                //1学期
                svf.VrsOutn("VAL1", idx, scoreData.score(SDIV1990009)); //評価

                //2学期
                if(_param._semes2Flg) {
                    svf.VrsOutn("VAL2", idx, scoreData.score(SDIV2990009)); //評価
                }

                //3学期
                if(_param._semes3Flg) {
                    svf.VrsOutn("VAL3", idx, scoreData.score(SDIV3990009)); //評価
                }

                idx++;
                if(idx > SCORE_MAX_LINE) break;
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
            if (_param._isNoPrintMoto &&  subclassMst.isMoto() || !_param._isPrintSakiKamoku &&  subclassMst.isSaki()) {
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

    // 行動の記録
    private void printAct(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next();
            final int line = getSemeLine(semester);
            if(line == 2 && !_param._semes2Flg) continue;
            if(line == 3 && !_param._semes3Flg) continue;
            if(line == 4) continue;

        	final Map actMap = getSchregBehavior(db2, semester, student._schregno);
        	for (final Iterator itAct = actMap.keySet().iterator(); itAct.hasNext();) {
                final String code = (String) itAct.next();
                final String record = (String) actMap.get(code);
                svf.VrsOutn("ACT"+semester, Integer.parseInt(code), record);
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
            int mlesson = 0;
            int suspend = 0;
            int absent = 0;
            int present = 0;
            int late = 0;
            int early = 0;
//            int abroad = 0;
            if (null != att) {
                lesson = att._lesson;
                suspend = att._suspend + att._mourning;
                mlesson = att._mLesson;
                absent = att._sick;
                present = att._present;
                late = att._late;
                early = att._early;
//                abroad = att._abroad;
            }
            if(line == 2 && !_param._semes2Flg) continue;
            if(line == 3 && !_param._semes3Flg) continue;
            svf.VrsOutn("ATTEND"+line, 1, String.valueOf(lesson));   // 授業日数
            svf.VrsOutn("ATTEND"+line, 2, String.valueOf(suspend)); // 忌引出停日数
            svf.VrsOutn("ATTEND"+line, 3, String.valueOf(mlesson));    // 要出席日数
            svf.VrsOutn("ATTEND"+line, 4, String.valueOf(absent));   // 欠席日数
            svf.VrsOutn("ATTEND"+line, 5, String.valueOf(present)); // 出席日数
            svf.VrsOutn("ATTEND"+line, 6, String.valueOf(late+early));       // 遅刻+早退
//            svf.VrsOutn("ATTEND"+line, line, String.valueOf(abroad));   // 留学中の授業日数
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
//                student._hrname = Integer.parseInt(rs.getString("GRADE_CD")) + "年" + rs.getString("HR_CLASS_NAME1") + "組" ;
                student._hrname = rs.getString("HR_CLASS_NAME1") + "組" ;
                student._trcd = StringUtils.defaultString(rs.getString("TR_CD1"));
                student._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                student._staffname2 = StringUtils.defaultString(rs.getString("STAFFNAME2"));
                student._attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番" : rs.getString("ATTENDNO");
                student._grade = rs.getString("GRADE");
                student._hrClass = rs.getString("HR_CLASS");
                student._coursecd = rs.getString("COURSECD");
                student._majorcd = rs.getString("MAJORCD");
                student._coursecode = rs.getString("COURSECODE");
                student._course = rs.getString("COURSE");
                student._majorname = rs.getString("MAJORNAME");
                student._coursename = rs.getString("COURSECODENAME");
                student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                student._entyear = rs.getString("ENT_YEAR");
                student._guard_zipcd = StringUtils.defaultString(rs.getString("GUARD_ZIPCD"));
                student._guard_addr1 = StringUtils.defaultString(rs.getString("GUARD_ADDR1"));
                student._guard_addr2 = StringUtils.defaultString(rs.getString("GUARD_ADDR2"));
                student._guard_name = StringUtils.defaultString(rs.getString("GUARD_NAME"));
                student._viewRecordList = ViewRecord.getViewRecordList(db2, _param, student._schregno);
                student._chairSubclassList = ChairSubclass.load(db2, _param, student._schregno);

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
    	stb.append("    AND T1.GRADE || T1.HR_CLASS = '"+ _param._gradeHrClass +"' ");
        stb.append("    AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));

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
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("            ,REGDH.TR_CD1 ");
        stb.append("            ,STF1.STAFFNAME ");
        stb.append("            ,STF2.STAFFNAME AS STAFFNAME2 ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,REGDG.GRADE ");
        stb.append("            ,REGDG.GRADE_CD ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("            ,REGD.COURSECD ");
        stb.append("            ,REGD.MAJORCD ");
        stb.append("            ,REGD.COURSECODE ");
        stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
        stb.append("            ,MAJOR.MAJORNAME ");
        stb.append("            ,COURSE.COURSECODENAME ");
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
        stb.append("            ,GADDR.GUARD_ZIPCD ");
        stb.append("            ,GADDR.GUARD_ADDR1 ");
        stb.append("            ,GADDR.GUARD_ADDR2 ");
        stb.append("            ,GUARDIAN.GUARD_NAME ");
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
        stb.append("     LEFT JOIN COURSECODE_MST COURSE ");
        stb.append("            ON COURSE.COURSECODE = REGD.COURSECODE ");
        stb.append("     LEFT JOIN STAFF_MST STF2 ON STF2.STAFFCD = REGDH.TR_CD2 ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GUARDIAN ");
        stb.append("            ON GUARDIAN.SCHREGNO = REGD.SCHREGNO  ");
        stb.append("     LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_ADDRESS_DAT GROUP BY SCHREGNO) L_GADDR ");
        stb.append("            ON L_GADDR.SCHREGNO = GUARDIAN.SCHREGNO  ");
        stb.append("     LEFT JOIN GUARDIAN_ADDRESS_DAT GADDR ON GADDR.SCHREGNO = L_GADDR.SCHREGNO AND GADDR.ISSUEDATE = L_GADDR.ISSUEDATE ");
        stb.append("     WHERE   REGD.YEAR = '" + _param._loginYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND REGD.SEMESTER = '" + _param._loginSemester + "' ");
        } else {
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("         AND REGD.GRADE || REGD.HR_CLASS = '"+ _param._gradeHrClass +"' ");
        stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
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


    //対象生徒の道徳を取得
    private Map getSchregTotalstudy(final DB2UDB db2, final String year, final String semester, final String schregno) {
    	Map rtnMap = new TreeMap();
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("   T2.TOTALSTUDYTIME, ");
        stb.append("   T2.TOTALSTUDYTIME_FLG, ");
        stb.append("   T2.TOTALSTUDYACT, ");
        stb.append("   T2.REMARK1 ");
        stb.append(" FROM  ");
        stb.append("   RECORD_TOTALSTUDYTIME_ITEM_MST T1 ");
        stb.append("   INNER JOIN RECORD_TOTALSTUDYTIME_DAT T2 ");
        stb.append("           ON T2.YEAR        = '"+ year +"' ");
        stb.append("          AND T2.SEMESTER    = '"+ semester +"' ");
        stb.append("          AND T2.CLASSCD     = T1.CLASSCD ");
        stb.append("          AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("          AND T2.SCHREGNO    = '"+ schregno +"' ");
        stb.append(" WHERE ");
        stb.append("   T1.REMARK2 = '2'"); //道徳
        stb.append(" ORDER BY SUBCLASSCD ");

        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	Map map = new TreeMap();
            	map.put("SUBCLASSCD", StringUtils.defaultString(rs.getString("SUBCLASSCD")));
            	map.put("TOTALSTUDYTIME", StringUtils.defaultString(rs.getString("TOTALSTUDYTIME")));
            	map.put("TOTALSTUDYTIME_FLG", StringUtils.defaultString(rs.getString("TOTALSTUDYTIME_FLG")));
            	map.put("TOTALSTUDYACT", StringUtils.defaultString(rs.getString("TOTALSTUDYACT")));
            	map.put("REMARK1", StringUtils.defaultString(rs.getString("REMARK1")));
            	final String key = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                rtnMap.put(key, map);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnMap;
    }


    //対象生徒の行動の記録を取得
    private Map getSchregBehavior(final DB2UDB db2, final String semester, final String schregno) {
    	Map rtnMap = new TreeMap();
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     CODE, ");
        stb.append("     RECORD ");
        stb.append(" FROM ");
        stb.append("     BEHAVIOR_SEMES_DAT ");
        stb.append(" WHERE ");
        stb.append("       YEAR     = '"+ _param._loginYear +"' ");
        stb.append("   AND SEMESTER = '"+ semester +"' ");
        stb.append("   AND SCHREGNO = '"+ schregno +"' ");
        stb.append(" ORDER BY ");
        stb.append("     CODE ");
        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String code = StringUtils.defaultString(rs.getString("CODE"));
            	final String record = StringUtils.defaultString(rs.getString("RECORD"));
                rtnMap.put(code, record);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnMap;
    }

    /**
     * 生徒
     */
    private class Student {
        String _schregno;
        String _name;
        String _schoolKind;
        String _gradename;
        String _hrname;
        String _trcd;
        String _staffname;
        String _staffname2;
        String _attendno;
        String _grade;
        String _hrClass;
        String _coursecd;
        String _majorcd;
        String _coursecode;
        String _course;
        String _majorname;
        String _coursename;
        String _hrClassName1;
        String _entyear;
        String _guard_zipcd;
        String _guard_addr1;
        String _guard_addr2;
        String _guard_name;
        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map<String, Map<String, SubclassAttendance>> _attendSubClassMap = new HashMap();
        List _viewRecordList = Collections.EMPTY_LIST; // 観点
        List _chairSubclassList = Collections.EMPTY_LIST; //教科

        private void setSubclass(final DB2UDB db2) {
            final String scoreSql = prestatementSubclass();
            if (_param._isOutputDebug) {
                log.fatal(" scoreSql = " + scoreSql);
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
                    final String credits = rs.getString("CREDITS");

                    String subclasscd = rs.getString("SUBCLASSCD");
                    if (!ALL9.equals(subclasscd)) {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    }

                    final Rank gradeRank = new Rank(rs.getString("GRADE_RANK"), rs.getString("GRADE_AVG_RANK"), rs.getString("GRADE_COUNT"), rs.getString("GRADE_AVG"), rs.getString("GRADE_HIGHSCORE"));
                    final Rank hrRank = new Rank(rs.getString("CLASS_RANK"), rs.getString("CLASS_AVG_RANK"), rs.getString("HR_COUNT"), rs.getString("HR_AVG"), rs.getString("HR_HIGHSCORE"));
                    final Rank courseRank = new Rank(rs.getString("COURSE_RANK"), rs.getString("COURSE_AVG_RANK"), rs.getString("COURSE_COUNT"), rs.getString("COURSE_AVG"), rs.getString("COURSE_HIGHSCORE"));
                    final Rank majorRank = new Rank(rs.getString("MAJOR_RANK"), rs.getString("MAJOR_AVG_RANK"), rs.getString("MAJOR_COUNT"), rs.getString("MAJOR_AVG"), rs.getString("MAJOR_HIGHSCORE"));

                    final String key = subclasscd;
                    if (!_printSubclassMap.containsKey(key)) {
                        _printSubclassMap.put(key, new ScoreData(classcd, classname, subclasscd, subclassname, credits));
                    }
                    if (null == rs.getString("SEMESTER")) {
                        continue;
                    }

                    final ScoreData scoreData = (ScoreData) _printSubclassMap.get(key);
                    final String testcd = rs.getString("SEMESTER") + rs.getString("TESTKINDCD") + rs.getString("TESTITEMCD") + rs.getString("SCORE_DIV");
                    scoreData._scoreMap.put(testcd, StringUtils.defaultString(rs.getString("SCORE")));
                    scoreData._avgMap.put(testcd, StringUtils.defaultString(rs.getString("AVG")));
                    scoreData._gradeRankMap.put(testcd, gradeRank);
                    scoreData._hrRankMap.put(testcd, hrRank);
                    scoreData._courceRankMap.put(testcd, courseRank);
                    scoreData._majorRankMap.put(testcd, majorRank);
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

            final String[] sdivs = {SDIV1990009, SDIV2990009, SDIV3990009, SDIV9990009};
            final StringBuffer divStr = divStr("", sdivs);
//            final StringBuffer divStrT5 = divStr("T5.", sdivs);

            stb.append(" WITH SCHNO AS( ");
            //学籍の表
            stb.append(" SELECT ");
            stb.append("     T2.YEAR, ");
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
            stb.append("     S2.SUBCLASSCD, ");
            stb.append("     S1.CHAIRCD ");
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
            stb.append("     S2.SUBCLASSCD, ");
            stb.append("     S1.CHAIRCD ");
            //成績明細データの表
            stb.append(" ) ,RECORD00 AS( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.CHAIRCD, T1.SCHREGNO ");
            stb.append("     FROM RECORD_RANK_CHAIR_SDIV_DAT T1 ");
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
            stb.append("              T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.CHAIRCD, T1.SCHREGNO ");
            stb.append("            , L1.CREDITS ");
            stb.append("            , L2.SCORE ");
            stb.append("            , L2.AVG ");
            stb.append("            , L2.GRADE_RANK ");
            stb.append("            , L2.GRADE_AVG_RANK ");
            stb.append("            , L2.CLASS_RANK ");
            stb.append("            , L2.CLASS_AVG_RANK ");
            stb.append("            , L2.COURSE_RANK ");
            stb.append("            , L2.COURSE_AVG_RANK ");
            stb.append("            , L2.MAJOR_RANK ");
            stb.append("            , L2.MAJOR_AVG_RANK ");
            stb.append("            , T_AVG1.AVG AS GRADE_AVG ");
            stb.append("            , T_AVG1.COUNT AS GRADE_COUNT ");
            stb.append("            , T_AVG1.HIGHSCORE AS GRADE_HIGHSCORE ");
            stb.append("            , T_AVG2.AVG AS HR_AVG ");
            stb.append("            , T_AVG2.COUNT AS HR_COUNT ");
            stb.append("            , T_AVG2.HIGHSCORE AS HR_HIGHSCORE ");
            stb.append("            , T_AVG3.AVG AS COURSE_AVG ");
            stb.append("            , T_AVG3.COUNT AS COURSE_COUNT ");
            stb.append("            , T_AVG3.HIGHSCORE AS COURSE_HIGHSCORE ");
            stb.append("            , T_AVG4.AVG AS MAJOR_AVG ");
            stb.append("            , T_AVG4.COUNT AS MAJOR_COUNT ");
            stb.append("            , T_AVG4.HIGHSCORE AS MAJOR_HIGHSCORE ");
            stb.append("     FROM RECORD00 T1 ");
            stb.append("     INNER JOIN SCHNO T2 ");
            stb.append("             ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN CREDIT_MST L1 ");
            stb.append("            ON L1.YEAR          = T2.YEAR ");
            stb.append("           AND L1.COURSECD      = T2.COURSECD ");
            stb.append("           AND L1.MAJORCD       = T2.MAJORCD ");
            stb.append("           AND L1.COURSECODE    = T2.COURSECODE ");
            stb.append("           AND L1.GRADE         = T2.GRADE ");
            stb.append("           AND L1.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND L1.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("     INNER JOIN RECORD_RANK_CHAIR_SDIV_DAT L2 ");
            stb.append("            ON L2.YEAR          = T1.YEAR ");
            stb.append("           AND L2.SEMESTER      = T1.SEMESTER ");
            stb.append("           AND L2.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("           AND L2.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("           AND L2.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("           AND L2.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND L2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L2.CHAIRCD       = T1.CHAIRCD ");
            stb.append("           AND L2.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG1 ON T_AVG1.YEAR = T1.YEAR AND T_AVG1.SEMESTER = T1.SEMESTER AND T_AVG1.TESTKINDCD = T1.TESTKINDCD AND T_AVG1.TESTITEMCD = T1.TESTITEMCD AND T_AVG1.SCORE_DIV = T1.SCORE_DIV AND T_AVG1.GRADE = '" + _param._grade + "' AND T_AVG1.CLASSCD = T1.CLASSCD AND T_AVG1.SCHOOL_KIND = T1.SCHOOL_KIND AND T_AVG1.CURRICULUM_CD = T1.CURRICULUM_CD AND T_AVG1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("           AND T_AVG1.AVG_DIV    = '1' "); //学年
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG2 ON T_AVG2.YEAR = T1.YEAR AND T_AVG2.SEMESTER = T1.SEMESTER AND T_AVG2.TESTKINDCD = T1.TESTKINDCD AND T_AVG2.TESTITEMCD = T1.TESTITEMCD AND T_AVG2.SCORE_DIV = T1.SCORE_DIV AND T_AVG2.GRADE = '" + _param._grade + "' AND T_AVG2.CLASSCD = T1.CLASSCD AND T_AVG2.SCHOOL_KIND = T1.SCHOOL_KIND AND T_AVG2.CURRICULUM_CD = T1.CURRICULUM_CD AND T_AVG2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("           AND T_AVG2.AVG_DIV    = '2' "); //クラス
            stb.append("           AND T_AVG2.HR_CLASS   = T2.HR_CLASS ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG3 ON T_AVG3.YEAR = T1.YEAR AND T_AVG3.SEMESTER = T1.SEMESTER AND T_AVG3.TESTKINDCD = T1.TESTKINDCD AND T_AVG3.TESTITEMCD = T1.TESTITEMCD AND T_AVG3.SCORE_DIV = T1.SCORE_DIV AND T_AVG3.GRADE = '" + _param._grade + "' AND T_AVG3.CLASSCD = T1.CLASSCD AND T_AVG3.SCHOOL_KIND = T1.SCHOOL_KIND AND T_AVG3.CURRICULUM_CD = T1.CURRICULUM_CD AND T_AVG3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("           AND T_AVG3.AVG_DIV    = '3' ");
            stb.append("           AND T_AVG3.COURSECD   = T2.COURSECD ");
            stb.append("           AND T_AVG3.MAJORCD    = T2.MAJORCD ");
            stb.append("           AND T_AVG3.COURSECODE = T2.COURSECODE "); //コース
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG4 ON T_AVG4.YEAR = T1.YEAR AND T_AVG4.SEMESTER = T1.SEMESTER AND T_AVG4.TESTKINDCD = T1.TESTKINDCD AND T_AVG4.TESTITEMCD = T1.TESTITEMCD AND T_AVG4.SCORE_DIV = T1.SCORE_DIV AND T_AVG4.GRADE = '" + _param._grade + "' AND T_AVG4.CLASSCD = T1.CLASSCD AND T_AVG4.SCHOOL_KIND = T1.SCHOOL_KIND AND T_AVG4.CURRICULUM_CD = T1.CURRICULUM_CD AND T_AVG4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("           AND T_AVG4.AVG_DIV    = '4' ");
            stb.append("           AND T_AVG4.COURSECD   = T2.COURSECD ");
            stb.append("           AND T_AVG4.MAJORCD    = T2.MAJORCD ");
            stb.append("           AND T_AVG4.COURSECODE = '0000' "); //専攻
            stb.append(" ) ,RECORD AS( ");
            stb.append("     SELECT ");
            stb.append("       T3.CLASSNAME, ");
            stb.append("       T4.SUBCLASSNAME, ");
            stb.append("       T1.* ");
            stb.append("     FROM RECORD0 T1 ");
            stb.append("     INNER JOIN CHAIR_A T5 ");
            stb.append("        ON T5.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("       AND T5.SCHREGNO      = T1.SCHREGNO ");
            stb.append("       AND T5.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND T5.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("       AND T5.CHAIRCD       = T1.CHAIRCD ");
            stb.append("     INNER JOIN CLASS_MST T3 ");
            stb.append("        ON T3.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
            stb.append("       AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("     INNER JOIN SUBCLASS_MST T4 ");
            stb.append("        ON T4.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("       AND T4.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     INNER JOIN SUBCLASS_YDAT SUBY ");
            stb.append("        ON SUBY.YEAR          = '" + _param._loginYear + "' ");
            stb.append("       AND SUBY.SUBCLASSCD    = T4.SUBCLASSCD ");
            stb.append("       AND SUBY.CLASSCD       = T4.CLASSCD ");
            stb.append("       AND SUBY.SCHOOL_KIND   = T4.SCHOOL_KIND ");
            stb.append("       AND SUBY.CURRICULUM_CD = T4.CURRICULUM_CD ");
            stb.append("   UNION ALL ");
            stb.append("     SELECT ");
            stb.append("       CAST(NULL AS VARCHAR(1)) AS CLASSNAME, ");
            stb.append("       CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
            stb.append("       T1.* ");
            stb.append("      FROM RECORD0 T1 ");
            stb.append("           LEFT JOIN ( SELECT T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");
            stb.append("                         FROM RECORD_RANK_CHAIR_SDIV_DAT T1 ");
            stb.append("                              INNER JOIN SCHREG_REGD_DAT REGD ");
            stb.append("                                      ON REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("                                     AND REGD.YEAR     = T1.YEAR ");
            if (SEMEALL.equals(_param._semester)) {
                stb.append("                                     AND REGD.SEMESTER = '" + _param._loginSemester + "' ");
            } else {
                stb.append("                                     AND REGD.SEMESTER = T1.SEMESTER ");
            }
            stb.append("                        WHERE ");
            stb.append("                              T1.YEAR       = '" + _param._loginYear + "' ");
            stb.append("                          AND T1.SUBCLASSCD = '" + ALL9 + "' ");
            stb.append("                        GROUP BY T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV  ");
            stb.append("                     ) T2");
            stb.append("                  ON T2.YEAR       = T1.YEAR ");
            stb.append("                 AND T2.SEMESTER   = T1.SEMESTER ");
            stb.append("                 AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("                 AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("                 AND T2.SCORE_DIV  = T1.SCORE_DIV ");
            stb.append("      WHERE T1.SUBCLASSCD = '" + ALL9 + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("       T1.* ");
            stb.append(" FROM RECORD T1 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD,  ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.TESTKINDCD, ");
            stb.append("     T1.TESTITEMCD, ");
            stb.append("     T1.SCORE_DIV  ");

            return stb.toString();
        }

        /**
         * 学期+テスト種別のWHERE句を作成
         * @param tab テーブル別名
         * @param sdivs 学期+テスト種別
         * @return 作成した文字列
         */
        private StringBuffer divStr(final String tab, final String[] sdivs) {
            final StringBuffer divStr = new StringBuffer();
            divStr.append(" ( ");
            String or = "";
            for (int i = 0; i < sdivs.length; i++) {
                final String semester = sdivs[i].substring(0, 1);
                final String testkindcd = sdivs[i].substring(1, 3);
                final String testitemcd = sdivs[i].substring(3, 5);
                final String scorediv = sdivs[i].substring(5);
                divStr.append(or).append(" " + tab + "SEMESTER = '" + semester + "' AND " + tab + "TESTKINDCD = '" + testkindcd + "' AND " + tab + "TESTITEMCD = '" + testitemcd + "' AND " + tab + "SCORE_DIV = '" + scorediv + "' ");
                or = " OR ";
            }
            divStr.append(" ) ");
            return divStr;
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
     * 得点
     */
    private static class ScoreData {
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final String _credits;
        final Map _scoreMap = new HashMap(); // 得点
        final Map _avgMap = new HashMap(); // 平均点
        final Map _gradeRankMap = new HashMap(); // 学年順位
        final Map _hrRankMap = new HashMap(); // クラス順位
        final Map _courceRankMap = new HashMap(); // コース順位
        final Map _majorRankMap = new HashMap(); // 専攻順位

        private ScoreData(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String subclassname,
                final String credits
        ) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _credits = credits;
        }

        public String score(final String sdiv) {
            return StringUtils.defaultString((String) _scoreMap.get(sdiv), "");
        }

        public String avg(final String sdiv) {
            return StringUtils.defaultString((String) _avgMap.get(sdiv), "");
        }

        public String toString() {
            return "ScoreData(" + _subclasscd + ":" + _subclassname + ", scoreMap = " + _scoreMap + ")";
        }
    }

    /**
     * 換算表
     */
    private static class Kansan {
        final String _level;
        final String _low;
        final String _high;

        private Kansan(final String level, final String low, final String high) {
            _level = level;
            _low = low;
            _high = high;
        }
    }

    /**
     * 各順位
     */
    private static class Rank {
        final String _rank;
        final String _avgRank;
        final String _count;
        final String _avg;
        final String _highscore;
        public Rank(final String rank, final String avgRank, final String count, final String avg, final String highscore) {
            _rank = rank;
            _avgRank = avgRank;
            _count = count;
            _avg = avg;
            _highscore = highscore;
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
                log.debug(" getViewClassSql = " + sql);
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
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._loginYear + "' ");
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
        final String _viewcd;
        final String _status1;
        final String _status2;
        final String _status3;
        final String _status9;
        final String _grade;
        final String _viewname;
        final String _classcd;
        final String _subclasscd;
        final String _subclassname;
        final String _classMstShoworder;
        final String _showorder;
        final String _score;
        ViewRecord(
                final String viewcd,
                final String status1,
                final String status2,
                final String status3,
                final String status9,
                final String grade,
                final String viewname,
                final String classcd,
                final String subclasscd,
                final String subclassname,
                final String classMstShoworder,
                final String showorder,
                final String score
        ) {
            _viewcd = viewcd;
            _status1 = status1;
            _status2 = status2;
            _status3 = status3;
            _status9 = status9;
            _grade = grade;
            _viewname = viewname;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
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
                    final String viewcd = StringUtils.defaultString(rs.getString("VIEWCD"));
                    final String status1 = StringUtils.defaultString(rs.getString("STATUS1"));
                    final String status2 = StringUtils.defaultString(rs.getString("STATUS2"));
                    final String status3 = StringUtils.defaultString(rs.getString("STATUS3"));
                    final String status9 = StringUtils.defaultString(rs.getString("STATUS9"));
                    final String grade = StringUtils.defaultString(rs.getString("GRADE"));
                    final String viewname = StringUtils.defaultString(rs.getString("VIEWNAME"));
                    final String classcd = StringUtils.defaultString(rs.getString("CLASSCD"));
                    final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final String classMstShoworder = StringUtils.defaultString(rs.getString("CLASS_MST_SHOWORDER"));
                    final String showorder = StringUtils.defaultString(rs.getString("SHOWORDER"));
                    final String score = StringUtils.defaultString(rs.getString("SCORE"));
                    final String attendFlg = StringUtils.defaultString(rs.getString("ATTEND_FLG"));
                    final String combinedFlg = StringUtils.defaultString(rs.getString("COMBINED_FLG"));
                    if ("1".equals(attendFlg) && param._isNoPrintMoto) {
                        // 合併元科目を表示しない
                        continue;
                    } else if ("1".equals(combinedFlg) && !param._isPrintSakiKamoku) {
                        // 合併先科目を表示しない
                        continue;
                    }

                    final ViewRecord viewRecord = new ViewRecord(viewcd, status1, status2, status3, status9, grade, viewname, classcd, subclasscd, subclassname, classMstShoworder, showorder, score);
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
            stb.append(" WHERE YEAR = '" + param._loginYear + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     'ATTEND' AS FLG, ATTEND_CLASSCD AS CLASSCD, ATTEND_SCHOOL_KIND AS SCHOOL_KIND, ATTEND_CURRICULUM_CD AS CURRICULUM_CD, ATTEND_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(" FROM SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append(" WHERE YEAR = '" + param._loginYear + "' ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("     T1.GRADE   ");
            stb.append("     , T1.VIEWNAME ");
            stb.append("     , T1.VIEWCD ");
            stb.append("     , T3_1.STATUS AS STATUS1 ");
            stb.append("     , T3_2.STATUS AS STATUS2 ");
            stb.append("     , T3_3.STATUS AS STATUS3 ");
            stb.append("     , T3_9.STATUS AS STATUS9 ");
            stb.append("     , T4.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("     , T1.SUBCLASSCD ");
            }
            stb.append("     , SUBM.SUBCLASSNAME ");
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
            stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT T3_1 ");
            stb.append("            ON T3_1.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3_1.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T3_1.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T3_1.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T3_1.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T3_1.YEAR = T2.YEAR ");
            stb.append("         AND T3_1.SEMESTER = '1' "); //1学期
            stb.append("         AND T3_1.SCHREGNO = '" + schregno + "' ");
            stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT T3_2 ");
            stb.append("            ON T3_2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3_2.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T3_2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T3_2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T3_2.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T3_2.YEAR = T2.YEAR ");
            stb.append("         AND T3_2.SEMESTER = '2' "); //2学期
            stb.append("         AND T3_2.SCHREGNO = '" + schregno + "' ");
            stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT T3_3 ");
            stb.append("            ON T3_3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3_3.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T3_3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T3_3.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T3_3.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T3_3.YEAR = T2.YEAR ");
            stb.append("         AND T3_3.SEMESTER = '3' "); //3学期
            stb.append("         AND T3_3.SCHREGNO = '" + schregno + "' ");

            stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT T3_9 ");
            stb.append("            ON T3_9.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3_9.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T3_9.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T3_9.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T3_9.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T3_9.YEAR = T2.YEAR ");
            stb.append("         AND T3_9.SEMESTER = '9' "); //9学期
            stb.append("         AND T3_9.SCHREGNO = '" + schregno + "' ");
            stb.append("     LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            }
            stb.append("     LEFT JOIN RECORD_RANK_CHAIR_SDIV_DAT T5 ON T5.SUBCLASSCD = T1.SUBCLASSCD ");
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
            stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON SUBM.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND SUBM.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND SUBM.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND SUBM.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._loginYear + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T2.YEAR ");
            stb.append("     , VALUE(T4.SHOWORDER, 0) ");
            stb.append("     , T4.CLASSCD ");
            stb.append("     , VALUE(T1.SHOWORDER, 0) ");
            stb.append("     , T1.VIEWCD ");
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
                sql.append("     T1.YEAR = '" + param._loginYear + "' ");
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

    private static class Attendance {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _present;
        final int _late;
        final int _early;
        final int _abroad;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int present,
                final int late,
                final int early,
                final int abroad
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
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
                            //if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                            if (Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && rs.getBigDecimal("MLESSON").intValue() > 0) {

                                final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                                final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                                final BigDecimal sick = rs.getBigDecimal("SICK2");
                                final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                                final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");
                                final BigDecimal late = rs.getBigDecimal("LATE");
                                final BigDecimal early = rs.getBigDecimal("EARLY");

                                final BigDecimal sick1 = mst.isSaki() ? rawReplacedSick : rawSick;
                                final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                                final BigDecimal sick2 = mst.isSaki() ? replacedSick : sick;

                                final BigDecimal absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");

                                final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2, late, early);

                                //欠課時数上限
                                final Double absent = Double.valueOf(mst.isSaki() ? rs.getString("REPLACED_SICK"): rs.getString("SICK2"));
                                subclassAttendance._isOver = subclassAttendance.judgeOver(absent, absenceHigh);

                                Map setSubAttendMap = null;

                                if (student._attendSubClassMap.containsKey(subclasscd)) {
                                    setSubAttendMap = (Map) student._attendSubClassMap.get(subclasscd);
                                } else {
                                    setSubAttendMap = new TreeMap();
                                }

//                                setMap.put(semesDetail._cdSemesterDetail, subclassAttendance);

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
        final String _calculateCreditFlg;
        SubclassMst _combined = null;
        List<SubclassMst> _attendSubclassList = new ArrayList();
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public boolean isMoto() {
            return null != _combined;
        }
        public boolean isSaki() {
            return !_attendSubclassList.isEmpty();
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

    private static class SemesterDetail implements Comparable<SemesterDetail> {
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
        public int compareTo(final SemesterDetail sd) {
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
        log.fatal("$Revision: 74612 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String[] _categorySelected;
        final String _date;
        final String _gradeHrClass;
        final String _grade;
        final String _loginSemester;
        final String _loginYear;
        final String _prgid;
        final String _semester;
        final String _useCurriculumcd;

        final String _nendo;
        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;
        final String _schoolKind;
        final String _schoolKindName;
        private final Map _testItemMap;
        final String _semesSdate;
        final String _semesEdate;
        final Map _stampMap;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private final List _semesterList;
        private Map _semesterMap;
        private final Map _semesterDetailMap;
        private Map<String, SubclassMst> _subclassMstMap;
        private Map<String, Map<String, String>> _creditMstMap;
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

        private final List _kansanList;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrClass.substring(0, 2);
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginYear = request.getParameter("YEAR");
            _prgid = request.getParameter("PRGID");
            _semester = request.getParameter("SEMESTER");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            _semes2Flg = "2".equals(_semester) || "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes3Flg = "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes9Flg = "9".equals(_semester) ? true : false;
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_loginYear)) + "年度";
            loadNameMstD026(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);
            _schoolKind = getSchoolKind(db2);
            _schoolKindName = getSchoolKindName(db2);
            _testItemMap = settestItemMap(db2);
            _semesSdate = getSemesterMst(db2, SEMEALL, "SDATE");
            _semesEdate = getSemesterMst(db2, _loginSemester, "EDATE");
            _stampMap = getStampNoMap(db2);

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
            setCreditMst(db2);

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            _schoolStampPath = getImageFilePath("SCHOOLSTAMP.bmp");

            _attendTestKindItemList = getTestKindItemList(db2, false, false);
            _attendSemesterDetailList = getAttendSemesterDetailList();

            _kansanList = getKansanList();

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD187N' AND NAME = '" + propName + "' "));
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
            _subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += " T1.CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " COMB1.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3, ";
                sql += " ATT1.COMBINED_CLASSCD || '-' || ATT1.COMBINED_SCHOOL_KIND || '-' || ATT1.COMBINED_CURRICULUM_CD || '-' || ATT1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
                sql += " COMB1.ATTEND_CLASSCD || '-' || COMB1.ATTEND_SCHOOL_KIND || '-' || COMB1.ATTEND_CURRICULUM_CD || '-' || COMB1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT ATT1 ON ATT1.YEAR = '" + _loginYear + "' AND ATT1.ATTEND_CLASSCD = T1.CLASSCD AND ATT1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ATT1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ATT1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
                sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT COMB1 ON COMB1.YEAR = '" + _loginYear + "' AND COMB1.COMBINED_CLASSCD = T1.CLASSCD AND COMB1.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND AND COMB1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD AND COMB1.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!_subclassMstMap.containsKey(rs.getString("SUBCLASSCD"))) {
                        final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), rs.getString("CALCULATE_CREDIT_FLG"));
                        _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                    }
                    final SubclassMst combined = _subclassMstMap.get(rs.getString("COMBINED_SUBCLASSCD"));
                    if (null != combined) {
                        final SubclassMst mst = _subclassMstMap.get(rs.getString("SUBCLASSCD"));
                        mst._combined = combined;
                    }
                    final SubclassMst attend = _subclassMstMap.get(rs.getString("ATTEND_SUBCLASSCD"));
                    if (null != attend) {
                        final SubclassMst mst = _subclassMstMap.get(rs.getString("SUBCLASSCD"));
                        mst._attendSubclassList.add(attend);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getCredits(final Student student, final String subclasscd) {
            final String regdKey = student._coursecd + student._majorcd + student._grade + student._coursecode;
            final Map<String, String> subclasscdCreditMap = _creditMstMap.get(regdKey);
            if (null == subclasscdCreditMap) {
                return null;
            }
            final String credits = subclasscdCreditMap.get(subclasscd);
            if (!subclasscdCreditMap.containsKey(subclasscd)) {
                log.info(" no credit_mst : " + subclasscd);
            }
            return credits;
        }

        private void setCreditMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _creditMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " T1.COURSECD || T1.MAJORCD || T1.GRADE || T1.COURSECODE AS REGD_KEY, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T1.CREDITS ";
                sql += " FROM CREDIT_MST T1 ";
                sql += " WHERE YEAR = '" + _loginYear + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String regdKey = rs.getString("REGD_KEY");
                    if (!_creditMstMap.containsKey(regdKey)) {
                        _creditMstMap.put(regdKey, new TreeMap());
                    }
                    _creditMstMap.get(regdKey).put(rs.getString("SUBCLASSCD"), rs.getString("CREDITS"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _loginYear + "' AND GRADE = '" + _grade + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private String getSchoolKindName(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = 'A023' AND NAME1 = '" + _schoolKind + "' ");
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private Map settestItemMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTITEM, TESTITEMNAME ");
                sql.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ");
                sql.append(" WHERE YEAR = '" + _loginYear + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String testitem = rs.getString("TESTITEM");
                    final String testitemname = StringUtils.defaultString(rs.getString("TESTITEMNAME"));
                    if (!map.containsKey(testitem)) {
                        map.put(testitem, testitemname);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return map;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindcd = "J".equals(_schoolKind) ? "103" : "104";
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _loginYear + "' AND CERTIF_KINDCD = '"+ certifKindcd +"' ");
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
                    final boolean isGakunenHyotei = SDIV9990009.equals(testItem.getTestcd());
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


        //学期情報の取得
        private String getSemesterMst(DB2UDB db2, final String semester, final String column) {
            String rtnStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   * ");
                stb.append(" FROM ");
                stb.append("   SEMESTER_MST ");
                stb.append(" WHERE ");
                stb.append("       YEAR     = '"+ _loginYear +"' ");
                stb.append("   AND SEMESTER = '"+ semester +"' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnStr = StringUtils.defaultString(rs.getString(column), _date);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnStr;
        }

        private List getKansanList() {
            final List list = new ArrayList();
            list.add(new Kansan("1", "1", "15"));
            list.add(new Kansan("2", "16", "29"));
            list.add(new Kansan("3", "30", "37"));
            list.add(new Kansan("4", "38", "45"));
            list.add(new Kansan("5", "46", "55"));
            list.add(new Kansan("6", "56", "65"));
            list.add(new Kansan("7", "66", "75"));
            list.add(new Kansan("8", "76", "85"));
            list.add(new Kansan("9", "86", "92"));
            list.add(new Kansan("10", "93", "100"));
            return list;
        }
    }
}

// eof
