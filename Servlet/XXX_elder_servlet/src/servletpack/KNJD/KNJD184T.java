/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: dd440479a1bc3c397aaf4433e7bc304572dbff4e $
 *
 * 作成日: 2020/07/17
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
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD184T {

    private static final Log log = LogFactory.getLog(KNJD184T.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "89";
    private static final String CLASSCD_KOKUGO = "11"; // 国語
    private static final String CLASSCD_SANSU  = "13"; // 算数
    private static final String CLASSCD_EIGO   = "21"; // 英語
    private static final String CLASSCD_QUEST  = "22"; // 探求

    private static final int PAGE3_MAX_LINE = 9;  // 3ページ目 最大行数

    private static final String SDIV1990008 = "1990008"; //1学期評価
    private static final String SDIV2990008 = "2990008"; //2学期評価
    private static final String SDIV3990008 = "3990008"; //3学期評価
    private static final String SDIV9990008 = "9990008"; //学年評価
    private static final String SDIV9990009 = "9990009"; //学年5段階評定

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

//    private static final String HYOTEI_TESTCD = "9990009";

    private int page;

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
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();
            page = 1;

            //1枚目
            printPage1(db2, svf, student);

            //2枚目
            printPage2(db2, svf, student);

            //3枚目
            printPage3(db2, svf, student);

            //4枚目
            printPage4(db2, svf, student);

            _hasData = true;
        }
    }

    private void printPage1(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form = "KNJD184T_1.frm";
        svf.VrSetForm(form , 1);

        //ヘッダ
        svf.VrsOut("NENDO", _param._loginYear + "年度" + _param._semesterName); //年度 + 学期
        svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoImagePath); //校章
        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName); //学校名
        svf.VrsOut("SCHOOL_NAME_ENG", _param._certifSchoolRemark1); //学校名英字
        svf.VrsOut("HR_NAME", _param._grade.substring(1,2) + "年" + student._hrname + student._attendno); //年組番
        String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名

        //出欠の様子
        printAttend(svf, student);
        VrsOutnRenban(svf, "FIELD1", KNJ_EditEdit.get_token(student._attendrec_remark, 40, 5)); //出欠の様子 備考

        //校長・担任欄
        svf.VrsOut("SEMESTER2", _param._semesterName); //学期
        String pNameField = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 20 ? "2" : "1";
        svf.VrsOut("PRINCIPAL_NAME" + pNameField, _param._certifSchoolPrincipalName); //校長名
        String trNameField = KNJ_EditEdit.getMS932ByteLength(student._staffname) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._staffname) > 20 ? "2" : "1";
        svf.VrsOut("TR_NAME" + trNameField, student._staffname); //担任名
        final String path = _param.getStaffImageFilePath(student._trcd);
        if (null != path) svf.VrsOut("STAFFBTM", path); //担任印
        if(_param._semes2Flg) svf.VrsOut("SLASH_GUARD", _param._backSlashImagePath); //保護者印 斜線

        //生活のようす
        svf.VrsOut("SEMESTER3", _param._semesterName); //学期
        svf.VrsOut("SP_ACT1", student._specialactremark); //特別活動・係
        if(!"".equals(student._remark1)) {
            svf.VrsOut("SP_ACT2", student._remark1); //フィールドワーク等
        } else {
            svf.VrsOut("SLASH_SP_ACT", _param._backSlashImagePath); //斜線
        }

        //保護者通信欄
        svf.VrsOut("SEMESTER4", _param._semesterName); //学期

        //ページ数
        svf.VrsOut("PAGE", String.valueOf(page));
        page++;

        svf.VrEndPage();
    }

    private void printPage2(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form = "KNJD184T_2.frm";
        svf.VrSetForm(form , 1);

        String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名

        svf.VrsOut("SEMESTER1", _param._semesterName); //学期

        //探求単元
        if (student._printSubclassMap.containsKey(CLASSCD_QUEST)) {
            final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(CLASSCD_QUEST);

            //探求の評価
            if(!"".equals(scoreData._testcd)) {
                svf.VrsOut("SP_TOTAL", scoreData._totalScore); //総括評価

                //改行
                String sep = "";

                //作品
                svf.VrsOut("OUTPITS", scoreData._outputsScore); //作品評価
                String outputsElement = "";
                sep = "";
                if(_param._outputsElementMap.containsKey(CLASSCD_QUEST)) {
                    final List outputsElementList = (List) _param._outputsElementMap.get(CLASSCD_QUEST);
                    for (Iterator itOutputs = outputsElementList.iterator(); itOutputs.hasNext();) {
                        final Map map = (Map) itOutputs.next();
                        //作品評価の要素
                        final String str = (String) map.get("ELEMENT_NAME");
                        outputsElement = outputsElement + sep + str;
                        sep = "\r\n";
                    }
                }
                VrsOutnRenban(svf, "OUTPITS_ELEMENT", KNJ_EditEdit.get_token(outputsElement,60,6)); //作品評価の要素

                //技能
                svf.VrsOut("SKILLS", scoreData._skillsScore); //技能評価
                String skillsElement = "";
                sep = "";
                if(_param._skillsElementMap.containsKey(CLASSCD_QUEST)) {
                    final List skillsElementList = (List) _param._skillsElementMap.get(CLASSCD_QUEST);
                    for (Iterator itSkills = skillsElementList.iterator(); itSkills.hasNext();) {
                        final Map map = (Map) itSkills.next();
                        //技能評価の要素
                        final String str = (String) map.get("ELEMENT_NAME");
                        skillsElement = skillsElement + sep + str;
                        sep = "\r\n";
                    }
                }
                VrsOutnRenban(svf, "SKILLS＿ELEMENT", KNJ_EditEdit.get_token(skillsElement,60,6)); //技能評価の要素
            }

        	//探求のコメント
            VrsOutnRenban(svf, "COMMENT", KNJ_EditEdit.get_token(scoreData._comment1,100,10)); //コメント
        }

        //Unit
        int line = 1;
        for (Iterator it = _param._unitMap.keySet().iterator(); it.hasNext();) {
        	final String key = (String) it.next();
        	final Map unitMap = (Map) _param._unitMap.get(key);

        	//名称
        	svf.VrsOut("UNIT_NAME" + line, "【" + (String)unitMap.get("NAME1") + "】");

        	//テーマ
            VrsOutnRenban(svf, "UNIT_THEME" + line, KNJ_EditEdit.get_token((String)unitMap.get("UNIT_THEME"), 60, 2));

            //中心概念
            final String unitIdea = "・Central idea（中心概念）\r\n" + (String)unitMap.get("UNIT_IDEA");
            VrsOutnRenban(svf, "UNIT_IDEA" + line, KNJ_EditEdit.get_token(unitIdea, 60, 4));

            line++;
        }


        //ページ数
        svf.VrsOut("PAGE", String.valueOf(page));
        page++;

        svf.VrEndPage();
    }

    private void printPage3(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form = "KNJD184T_3.frm";
        svf.VrSetForm(form , 1);

        String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名

        //明細部
        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        //印字する教科の設定
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            if(CLASSCD_KOKUGO.equals(subclassMst._classcd) || CLASSCD_SANSU.equals(subclassMst._classcd) || CLASSCD_EIGO.equals(subclassMst._classcd)) {
            	continue;
            }
            final String subclassCd = subclassMst._subclasscd;
            final boolean isPrint = student._printSubclassMap.containsKey(subclassCd) ;
            if (!isPrint) {
                itSubclass.remove();
            }
        }

        //学習の様子（各教科）
        int line = 1;
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclasscd;
            if(CLASSCD_KOKUGO.equals(subclassMst._classcd) || CLASSCD_SANSU.equals(subclassMst._classcd) || CLASSCD_EIGO.equals(subclassMst._classcd)) {
            	subclasscd = subclassMst._classcd;
            } else {
            	subclasscd = subclassMst._subclasscd;
            }

            if (student._printSubclassMap.containsKey(subclasscd)) {
                final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclasscd);
                if("".equals(scoreData._testcd)) continue;

                //改ページ
                if(line > PAGE3_MAX_LINE) {
                    //ページ数
                    svf.VrsOut("PAGE", String.valueOf(page));
                    page++;

                	svf.VrEndPage();
                    svf.VrSetForm(form , 1);
                    line = 1;
                }

                //教科名
                final String classNameField = (line == 1) ? "CLASS_NAME" : "CLASS_NAME" + line;
                svf.VrsOut(classNameField, subclassMst._classname);

                //総括評価
                final String spTotalField = (line == 1) ? "SP_TOTAL" : "SP_TOTAL" + line;
                svf.VrsOut(spTotalField, scoreData._totalScore);

                //改行
                String sep = "";

                //作品
                svf.VrsOut("OUTPITS" + line, scoreData._outputsScore); //作品評価
                String outputsElement = "";
                sep = "";
                if(_param._outputsElementMap.containsKey(subclasscd)) {
                    final List outputsElementList = (List) _param._outputsElementMap.get(subclasscd);
                    for (Iterator itOutputs = outputsElementList.iterator(); itOutputs.hasNext();) {
                        final Map map = (Map) itOutputs.next();
                        //作品評価の要素
                        final String str = (String) map.get("ELEMENT_NAME");
                        outputsElement = outputsElement + sep + str;
                        sep = "\r\n";
                    }
                }
                VrsOutnRenban(svf, "OUTPITS_ELEMENT" + line, KNJ_EditEdit.get_token(outputsElement,60,6)); //作品評価の要素

                //技能
                svf.VrsOut("SKILLS" + line, scoreData._skillsScore); //技能評価
                String skillsElement = "";
                sep = "";
                if(_param._skillsElementMap.containsKey(subclasscd)) {
                    final List skillsElementList = (List) _param._skillsElementMap.get(subclasscd);
                    for (Iterator itSkills = skillsElementList.iterator(); itSkills.hasNext();) {
                        final Map map = (Map) itSkills.next();
                        //技能評価の要素
                        final String str = (String) map.get("ELEMENT_NAME");
                        skillsElement = skillsElement + sep + str;
                        sep = "\r\n";
                    }
                }
                VrsOutnRenban(svf, "SKILLS＿ELEMENT" + line, KNJ_EditEdit.get_token(skillsElement,60,6)); //技能評価の要素
                line++;
            }
        }

        //ページ数
        svf.VrsOut("PAGE", String.valueOf(page));
        page++;

        svf.VrEndPage();
    }

    private void printPage4(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form = "KNJD184T_4.frm";
        svf.VrSetForm(form , 1);

        String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名

        //明細部
        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        //国語
        if (student._printSubclassMap.containsKey(CLASSCD_KOKUGO)) {
            final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(CLASSCD_KOKUGO);
            if(!"".equals(scoreData._cFlg)) {
                svf.VrsOut("CLASS_NAME1", scoreData._classname); //教科名
                VrsOutnRenban(svf, "COMMENT1", KNJ_EditEdit.get_token(scoreData._comment1,100,10)); //コメント
            }
        }

        //算数
        if (student._printSubclassMap.containsKey(CLASSCD_SANSU)) {
            final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(CLASSCD_SANSU);
            if(!"".equals(scoreData._cFlg)) {
                svf.VrsOut("CLASS_NAME2", scoreData._classname); //教科名
                VrsOutnRenban(svf, "COMMENT2", KNJ_EditEdit.get_token(scoreData._comment1,100,10)); //コメント
            }
        }

        //英語
        if (student._printSubclassMap.containsKey(CLASSCD_EIGO)) {
            final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(CLASSCD_EIGO);
            if(!"".equals(scoreData._cFlg)) {
                svf.VrsOut("CLASS_NAME3", scoreData._classname); //教科名
                VrsOutnRenban(svf, "COMMENT3", KNJ_EditEdit.get_token(scoreData._comment1,100,10)); //コメント
            }
        }

        //ページ数
        svf.VrsOut("PAGE", String.valueOf(page));
        page++;

        svf.VrEndPage();
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

    // 出欠記録
    private void printAttend(final Vrw32alp svf, final Student student) {
        svf.VrsOut("SEMESTER1", _param._semesterName); //学期
        final Attendance att = (Attendance) student._attendMap.get(_param._semester);
        int lesson = 0;
        int suspend = 0;
        int absent = 0;
        int present = 0;
        int late = 0;
        int early = 0;
        if (null != att) {
            lesson = att._lesson;
            suspend = att._suspend + att._mourning;
            absent = att._sick + att._notice + att._nonotice;
            present = att._present;
            late = att._late;
            early = att._early;
        }
        svf.VrsOut("LESSON", String.valueOf(lesson));   // 授業日数
        svf.VrsOut("SUSPEND", String.valueOf(suspend)); // 忌引出停日数
        svf.VrsOut("ABSENT", String.valueOf(absent));   // 欠席日数
        svf.VrsOut("PRESENT", String.valueOf(present)); // 出席日数
        svf.VrsOut("LATE", String.valueOf(late));       // 遅刻
        svf.VrsOut("EARLY", String.valueOf(early));     // 早退
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
                student._attendrec_remark = StringUtils.defaultString(rs.getString("ATTENDREC_REMARK"));
                student._specialactremark = StringUtils.defaultString(rs.getString("SPECIALACTREMARK"));
                student._remark1  = StringUtils.defaultString(rs.getString("REMARK1"));

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
        stb.append("            ,R1.ATTENDREC_REMARK ");
        stb.append("            ,R1.SPECIALACTREMARK ");
        stb.append("            ,R1.REMARK1 ");
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
        stb.append("     LEFT JOIN HREPORTREMARK_DAT R1 ");
        stb.append("            ON R1.YEAR     = REGD.YEAR ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("           AND R1.SEMESTER = '" + _param._maxSemes + "' "); //学年末選択時、最終学期を取得
        } else {
            stb.append("           AND R1.SEMESTER = REGD.SEMESTER ");
        }
        stb.append("           AND R1.SCHREGNO = REGD.SCHREGNO ");
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

    //Mapの取得
    private String getMapString(final Map map, final String key, final String def) {
        String rtnStr = def;
        if(map.containsKey(key)) {
            rtnStr = StringUtils.defaultString((String)map.get(key));
        }
        return rtnStr;
    }

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
        String _attendrec_remark;
        String _specialactremark;
        String _remark1;
        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map _totalScoreAvgMap = new TreeMap();

        private void setSubclass(final DB2UDB db2) {
            final String scoreSql = prestatementSubclass();
            log.fatal(" scoreSql = " + scoreSql);
            if (_param._isOutputDebug) {
                log.fatal(" scoreSql = " + scoreSql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = StringUtils.defaultString(rs.getString("CLASSCD"));
                    final String classname = StringUtils.defaultString(rs.getString("CLASSNAME"));

                    final String cFlg = StringUtils.defaultString(rs.getString("C_FLG"));
                    final String comment1 = StringUtils.defaultString(rs.getString("COMMENT1"));
                    final String comment2 = StringUtils.defaultString(rs.getString("COMMENT2"));

                    final String testcd = StringUtils.defaultString(rs.getString("TESTCD"));
                    final String totalScore = StringUtils.defaultString(rs.getString("TOTAL_SCORE"));
                    final String outputsScore = StringUtils.defaultString(rs.getString("OUTPUTS_SCORE"));
                    final String skillsScore = StringUtils.defaultString(rs.getString("SKILLS_SCORE"));

                    String subclasscd = rs.getString("SUBCLASSCD");
                    if (!ALL9.equals(subclasscd)) {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    }

                    if (CLASSCD_QUEST.equals(classcd) || CLASSCD_KOKUGO.equals(classcd) || CLASSCD_SANSU.equals(classcd) || CLASSCD_EIGO.equals(classcd)) {
                        subclasscd = classcd;
                    }

                    final String key = subclasscd;
                    if (!_printSubclassMap.containsKey(key)) {
                        _printSubclassMap.put(key, new ScoreData(classcd, classname, subclasscd, cFlg, comment1, comment2, testcd, totalScore, outputsScore, skillsScore));
                    }


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
            final String testcd = (_param._semes9Flg) ? SDIV9990008 : (_param._semes3Flg) ? SDIV3990008 : (_param._semes2Flg) ? SDIV2990008 : SDIV1990008;
            stb.append(" WITH SCHNO_A AS( ");
            stb.append("   SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.SCHREGNO ");
            stb.append("   FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("     T1.YEAR = '"+ _param._loginYear +"' ");
            stb.append("     AND T1.SEMESTER = '"+ _param._semester +"' ");
            stb.append("     AND T1.SCHREGNO = '"+ _schregno +"' ");
            stb.append(" ), PYP_SCORE AS ( ");
            stb.append("   SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T2.CLASSCD, ");
            stb.append("     T2.SCHOOL_KIND, ");
            stb.append("     T2.CURRICULUM_CD, ");
            stb.append("     T2.SUBCLASSCD, ");
            stb.append("     T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS TESTCD, ");
            stb.append("     T2.TOTAL_SCORE, ");
            stb.append("     T2.OUTPUTS_SCORE, ");
            stb.append("     T2.SKILLS_SCORE ");
            stb.append("   FROM ");
            stb.append("     SCHNO_A T1 ");
            stb.append("     INNER JOIN PYP_SCORE_DAT T2 ");
            stb.append("             ON T2.YEAR        = T1.YEAR ");
            stb.append("            AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '"+ testcd +"' ");
            stb.append("            AND T2.SCHREGNO    = T1.SCHREGNO ");
            stb.append("     INNER JOIN SUBCLASS_MST SUB ");
            stb.append("             ON SUB.CLASSCD       = T2.CLASSCD ");
            stb.append("            AND SUB.SCHOOL_KIND   = T2.SCHOOL_KIND  ");
            stb.append("            AND SUB.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("            AND SUB.SUBCLASSCD    = T2.SUBCLASSCD ");
            stb.append("  ");
            stb.append(" ), PYP_COMMENT AS ( ");
            stb.append("   SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     '1' AS C_FLG, ");
            stb.append("     T2.CLASSCD, ");
            stb.append("     T2.SCHOOL_KIND, ");
            stb.append("     T2.CURRICULUM_CD, ");
            stb.append("     T2.SUBCLASSCD, ");
            stb.append("     T2.COMMENT1, ");
            stb.append("     T2.COMMENT2 ");
            stb.append("   FROM ");
            stb.append("     SCHNO_A T1 ");
            stb.append("     INNER JOIN PYP_COMMENT_DAT T2 ");
            stb.append("             ON T2.YEAR        = T1.YEAR ");
            stb.append("            AND T2.SEMESTER    = '"+ _param._semester +"' ");
            stb.append("            AND T2.SCHREGNO    = T1.SCHREGNO ");
            stb.append("     INNER JOIN SUBCLASS_MST SUB ");
            stb.append("             ON SUB.CLASSCD       = T2.CLASSCD ");
            stb.append("            AND SUB.SCHOOL_KIND   = T2.SCHOOL_KIND  ");
            stb.append("            AND SUB.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("            AND SUB.SUBCLASSCD    = T2.SUBCLASSCD ");
            //メイン
            stb.append(" ), T_MAIN AS ( ");
            stb.append("   SELECT DISTINCT ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.CLASSNAME, ");
            stb.append("     T1.CLASSABBV, ");
            stb.append("     SUB.SCHOOL_KIND, ");
            stb.append("     SUB.CURRICULUM_CD, ");
            stb.append("     SUB.SUBCLASSCD, ");
            stb.append("     T2.TESTCD, ");
            stb.append("     T2.TOTAL_SCORE, ");
            stb.append("     T2.OUTPUTS_SCORE, ");
            stb.append("     T2.SKILLS_SCORE, ");
            stb.append("     T3.C_FLG, ");
            stb.append("     T3.COMMENT1, ");
            stb.append("     T3.COMMENT2, ");
            stb.append("     T1.SHOWORDER3 ");
            stb.append("   FROM ");
            stb.append("     CLASS_MST T1 ");
            stb.append("     INNER JOIN SUBCLASS_MST SUB ");
            stb.append("             ON SUB.CLASSCD       = T1.CLASSCD ");
            stb.append("            AND SUB.SCHOOL_KIND   = T1.SCHOOL_KIND  ");
            stb.append("     LEFT JOIN PYP_SCORE T2 ");
            stb.append("            ON T2.CLASSCD       = SUB.CLASSCD ");
            stb.append("           AND T2.SCHOOL_KIND   = SUB.SCHOOL_KIND ");
            stb.append("           AND T2.CURRICULUM_CD = SUB.CURRICULUM_CD ");
            stb.append("           AND T2.SUBCLASSCD    = SUB.SUBCLASSCD ");
            stb.append("     LEFT JOIN PYP_COMMENT T3 ");
            stb.append("            ON T3.CLASSCD       = SUB.CLASSCD ");
            stb.append("           AND T3.SCHOOL_KIND   = SUB.SCHOOL_KIND ");
            stb.append("           AND T3.CURRICULUM_CD = SUB.CURRICULUM_CD ");
            stb.append("           AND T3.SUBCLASSCD    = SUB.SUBCLASSCD ");
            stb.append("   WHERE ");
            stb.append("     T1.CLASSCD <= '"+ SELECT_CLASSCD_UNDER +"' ");
            stb.append("     AND T1.SCHOOL_KIND = '"+ _param._schoolKind +"' ");
            stb.append("     AND (T2.TESTCD IS NOT NULL OR T3.C_FLG = '1') ");
            stb.append(" ) ");
            stb.append("   SELECT ");
            stb.append("     *  ");
            stb.append("   FROM  ");
            stb.append("     T_MAIN ");
            stb.append("   ORDER BY ");
            stb.append("     SHOWORDER3, ");
            stb.append("     CLASSCD ");
            return stb.toString();
        }
    }

    private static class ScoreData {
        final String _classcd;
        final String _classname;
        final String _subclasscd;

        final String _cFlg; //PYP_COMMENT の存在フラグ
        final String _comment1;
        final String _comment2;

        final String _testcd;
        final String _totalScore; // 総括評価
        final String _outputsScore; // OUTPUTS評価
        final String _skillsScore; // SKILLS評価

        private ScoreData(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String cFlg,
                final String comment1,
                final String comment2,
                final String testcd,
                final String totalScore,
                final String outputsScore,
                final String skillsScore
        ) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _cFlg = cFlg;
            _comment1 = comment1;
            _comment2 = comment2;
            _testcd = testcd;
            _totalScore = totalScore;
            _outputsScore = outputsScore;
            _skillsScore = skillsScore;
        }
    }

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

    private static class Attendance {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _notice;
        final int _nonotice;
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
                final int notice,
                final int nonotice,
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
            _notice = notice;
            _nonotice = nonotice;
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
//                                rsAtSeme.getInt("NOTICE"),
//                                rsAtSeme.getInt("NONOTICE"),
                                rsAtSeme.getInt("NOTICE_ONLY"),
                        		rsAtSeme.getInt("NONOTICE_ONLY"),
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
        log.fatal("$Revision: 75498 $");
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
        final String _semesterName;
        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;
        final String _schoolKind;
        final String _schoolKindName;
        private final Map _testItemMap;
        final String _maxSemes;
        final String _semesSdate;
        final String _semesEdate;
        final Map _outputsElementMap;
        final Map _skillsElementMap;
        final Map _unitMap;

        final Map _stampMap;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolRemark1;

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
            _semesterName = getSemesterMst(db2, _semester, "SEMESTERNAME");
            loadNameMstD026(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);
            _schoolKind = getSchoolKind(db2);
            _schoolKindName = getSchoolKindName(db2);
            _testItemMap = settestItemMap(db2);
            _maxSemes = getMaxSemester(db2);
            _semesSdate = getSemesterMst(db2, SEMEALL, "SDATE");
            _semesEdate = getSemesterMst(db2, _loginSemester, "EDATE");

            _outputsElementMap = getPypElementMap(db2, "1");
            _skillsElementMap = getPypElementMap(db2, "2");
            _unitMap = getPypUnitList(db2);

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
            final String certifKindcd = "117";
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK1, REMARK2, REMARK4, REMARK5 ");
            sql.append(" FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _loginYear + "' AND CERTIF_KINDCD = '"+ certifKindcd +"' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
            _certifSchoolRemark1 = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK1"));
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
                    rtnStr = StringUtils.defaultString(rs.getString(column));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnStr;
        }

        //最終学期の取得
        private String getMaxSemester(DB2UDB db2) {
            String rtnStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   MAX(SEMESTER) AS SEMESTER ");
                stb.append(" FROM ");
                stb.append("   SEMESTER_MST ");
                stb.append(" WHERE ");
                stb.append("       YEAR     = '"+ _loginYear +"' ");
                stb.append("   AND SEMESTER <> '"+ SEMEALL +"' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnStr = StringUtils.defaultString(rs.getString("SEMESTER"));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnStr;
        }

        //評価の要素を取得
        private Map getPypElementMap(final DB2UDB db2, final String elementDiv) {
            final Map rtnMap = new TreeMap();
            List list = new ArrayList();
            String befSubclasscd = "";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   PED.YEAR, ");
            stb.append("   PED.SEMESTER, ");
            stb.append("   PED.GRADE, ");
            stb.append("   PED.CLASSCD, ");
            stb.append("   PED.SCHOOL_KIND, ");
            stb.append("   PED.CURRICULUM_CD, ");
            stb.append("   PED.SUBCLASSCD, ");
            stb.append("   PEM.ELEMENT_DIV, ");
            stb.append("   PEM.ELEMENT_CD, ");
            stb.append("   PEM.ELEMENT_NAME, ");
            stb.append("   PED.SORT ");
            stb.append(" FROM ");
            stb.append("   PYP_ELEMENT_MST PEM ");
            stb.append("   INNER JOIN PYP_ELEMENT_DAT PED ");
            stb.append("           ON PED.ELEMENT_DIV   = PEM.ELEMENT_DIV ");
            stb.append("          AND PED.ELEMENT_CD    = PEM.ELEMENT_CD ");
            stb.append(" WHERE PED.YEAR          = '"+ _loginYear +"' ");
            stb.append("   AND PED.SEMESTER      = '"+ _semester +"' ");
            stb.append("   AND PED.GRADE         = '"+ _grade +"' ");
            stb.append("   AND PED.ELEMENT_DIV   = '"+ elementDiv +"' ");
            stb.append(" ORDER BY ");
            stb.append("   PED.CLASSCD, ");
            stb.append("   PED.SCHOOL_KIND, ");
            stb.append("   PED.CURRICULUM_CD, ");
            stb.append("   PED.SUBCLASSCD, ");
            stb.append("   PED.SORT, ");
            stb.append("   PED.ELEMENT_CD ");
            final String sql =  stb.toString();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                log.debug(sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    String subclasscd = rs.getString("SUBCLASSCD");
                    if (!ALL9.equals(subclasscd)) {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    }

                    final String classcd = StringUtils.defaultString(rs.getString("CLASSCD"));
                    if (CLASSCD_QUEST.equals(classcd) || CLASSCD_KOKUGO.equals(classcd) || CLASSCD_SANSU.equals(classcd) || CLASSCD_EIGO.equals(classcd)) {
                        subclasscd = classcd;
                    }

                    //リストの初期化
                    if(!befSubclasscd.equals(subclasscd)) {
                        list = new ArrayList();
                    }

                    final Map map = new HashMap();
                    map.put("ELEMENT_CD", StringUtils.defaultString(rs.getString("ELEMENT_CD")));
                    map.put("ELEMENT_NAME", StringUtils.defaultString(rs.getString("ELEMENT_NAME")));
                    list.add(map);
                    rtnMap.put(subclasscd, list);
                    befSubclasscd = subclasscd;
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }

        //PYP_UNIT_DATの取得
        private Map getPypUnitList(final DB2UDB db2) {
            final Map rtnMap = new TreeMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T2.UNIT_CD, ");
            stb.append("   T1.NAME1, ");
            stb.append("   T2.UNIT_THEME, ");
            stb.append("   T2.UNIT_IDEA ");
            stb.append(" FROM  ");
            stb.append("   V_NAME_MST T1 ");
            stb.append("   INNER JOIN PYP_UNIT_DAT T2 ");
            stb.append("           ON T2.YEAR     = T1.YEAR ");
            stb.append("          AND T2.SEMESTER = T1.NAMESPARE1 ");
            stb.append("          AND T2.GRADE    = '"+ _grade +"' ");
            stb.append("          AND T2.UNIT_CD  = T1.NAMECD2 ");
            stb.append(" WHERE T1.YEAR       = '"+ _loginYear +"' ");
            stb.append("   AND T1.NAMECD1    = 'Z056' ");
            stb.append("   AND T1.NAMESPARE1 = '"+ _semester +"' ");
            stb.append(" ORDER BY T2.UNIT_CD ");

            final String sql =  stb.toString();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                log.debug(sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map map = new HashMap();
                    map.put("UNIT_CD", StringUtils.defaultString(rs.getString("UNIT_CD")));
                    map.put("NAME1", StringUtils.defaultString(rs.getString("NAME1")));
                    map.put("UNIT_THEME", StringUtils.defaultString(rs.getString("UNIT_THEME")));
                    map.put("UNIT_IDEA", StringUtils.defaultString(rs.getString("UNIT_IDEA")));

                    final String key = StringUtils.defaultString(rs.getString("UNIT_CD"));
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

    }
}

// eof
