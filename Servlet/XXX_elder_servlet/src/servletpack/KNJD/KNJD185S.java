
/*
 * $Id: 19c7f7e00d61cae97c3ef83b952498f0eefe3b67 $
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
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD185S {

    private static final Log log = LogFactory.getLog(KNJD185S.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "89";

    private static final String SDIV1990008 = "1990008"; //1学期評定
    private static final String SDIV2990008 = "2990008"; //2学期評定
    private static final String SDIV3990008 = "3990008"; //3学期評定
    private static final String SDIV9990009 = "9990009"; //学年評定

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

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

            //表紙
            if(_param._semes3Flg) printSvfHyoshi(db2, svf, student);

            //通知票
            printSvfMain(db2, svf, student);
        }
    }


    /**
     * 表紙を印刷する
     * @param db2
     * @param svf
     * @param student
     */
    private void printSvfHyoshi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD185S_1.frm", 1);

        final String pNameField = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 20 ? "2" : "1";
        svf.VrsOut("PRINCIPAL_NAME" + pNameField, convReverseStr(_param._certifSchoolPrincipalName)); //校長名
        svf.VrsOut("SCHOOL_NAME2", convReverseStr(_param._certifSchoolSchoolName)); //学校名
        final String date = KNJ_EditDate.getAutoFormatDate(db2, _param._date);
        svf.VrsOut("DATE", convReverseStr(date)); //年度
        final String grade = !"".equals(student._gradename) ? "中学校" + student._gradename + "の課程を修了したことを証する" : "";
        svf.VrsOut("GRADE", convReverseStr(grade)); //学年
        final String birthday =  KNJ_EditDate.getAutoFormatDate(db2, student._birthday) + "生";
        svf.VrsOut("BIRTHDAY", convReverseStr(birthday)); //生年月日
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("NAME2_" + nameField, convReverseStr(student._name)); //氏名
        svf.VrsOut("SCHOOL_STAMP", _param._schoolStampRotatePath); //捺印

        final String nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._loginYear)) + "年度";
        svf.VrsOut("NENDO", nendo); //年度
        svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoImagePath);
        svf.VrsOut("SCHOOL_NAME1", _param._certifSchoolSchoolName); //学校名
        svf.VrsOut("HR_NAME", student._hrname + student._attendno); //年組番
        svf.VrsOut("NAME1_" + nameField, student._name); //氏名

        svf.VrEndPage();
    }

    private String convReverseStr(String srcStr) {
    	//渡された文字列を逆順にして返却
        StringBuffer retStr = new StringBuffer();
        for (int ii = srcStr.length() - 1;ii >= 0;ii--) {
            retStr.append(srcStr.charAt(ii));
        }
        return retStr.toString();
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        svf.VrSetForm("KNJD185S_2.frm", 1);

        //明細部以外を印字
        printTitle(db2, svf, student);

        //明細部
        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        //■学習の記録 各種名称
        for(int seme = 1; seme <= 3; seme++) {
            if(seme == 2 && !_param._semes2Flg) continue;
            if(seme == 3 && !_param._semes3Flg) continue;
            svf.VrsOut("VAL_NAME" + seme, "評価");
            svf.VrsOut("CLASS_RANK_NAME" + seme, "クラス順位");
            svf.VrsOut("GRADE_RANK_NAME" + seme, "学年順位");
            svf.VrsOut("CLASS_AVE_NAME" + seme, "クラス平均");
            svf.VrsOut("GRADE_AVE_NAME" + seme, "学年平均");
            svf.VrsOut("CLASS_MAX_NAME" + seme, "クラス最高点");
            svf.VrsOut("GRADE_MAX_NAME" + seme, "学年最高点");
        }
        
        final List<SubclassMst> studentSubclassList = new ArrayList<SubclassMst>();
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            if (!student._printSubclassMap.containsKey(subclassMst._subclasscd)) {
                continue;
            }

            studentSubclassList.add(subclassMst);
        }

        //■学習の記録
        int idx = 1;
        for (final SubclassMst subclassMst : studentSubclassList) {
        	final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassMst._subclasscd);

            final String nameField = KNJ_EditEdit.getMS932ByteLength(subclassMst._classname) > 30 ? "_3" : KNJ_EditEdit.getMS932ByteLength(subclassMst._classname) > 20 ? "_2" : "_1";
            svf.VrsOutn("SUBCLASS_NAME1" + nameField, idx, subclassMst._classname); //教科名 1学期
            svf.VrsOutn("SUBCLASS_NAME2" + nameField, idx, subclassMst._classname); //教科名 2学期
            svf.VrsOutn("SUBCLASS_NAME3" + nameField, idx, subclassMst._classname); //教科名 3学期
            svf.VrsOutn("SUBCLASS_NAME9" + nameField, idx, subclassMst._classname); //教科名 学年評価

            //1学期
            svf.VrsOutn("VAL1", idx, scoreData._score1); //評価
            svf.VrsOutn("CLASS_RANK1", idx, scoreData._class_rank1); //クラス順位
            svf.VrsOutn("GRADE_RANK1", idx, scoreData._grade_rank1); //学年順位
            if(!"".equals(scoreData._class_avg1)) {
                BigDecimal cAvg = new BigDecimal(Double.parseDouble(scoreData._class_avg1)).setScale(1, BigDecimal.ROUND_HALF_UP);
                svf.VrsOutn("CLASS_AVE1", idx, cAvg.toString()); //クラス平均
            }
            if(!"".equals(scoreData._grade_avg1)) {
                BigDecimal gAvg = new BigDecimal(Double.parseDouble(scoreData._grade_avg1)).setScale(1, BigDecimal.ROUND_HALF_UP);
                svf.VrsOutn("GRADE_AVE1", idx, gAvg.toString()); //学年平均
            }
            svf.VrsOutn("CLASS_MAX1", idx, scoreData._class_highscore1); //クラス最高点
            svf.VrsOutn("GRADE_MAX1", idx, scoreData._grade_highscore1); //学年最高点

            //2学期
            if(_param._semes2Flg) {
                svf.VrsOutn("VAL2", idx, scoreData._score2); //評価
                svf.VrsOutn("CLASS_RANK2", idx, scoreData._class_rank2); //クラス順位
                svf.VrsOutn("GRADE_RANK2", idx, scoreData._grade_rank2); //学年順位
                if(!"".equals(scoreData._class_avg2)) {
                    BigDecimal cAvg = new BigDecimal(Double.parseDouble(scoreData._class_avg2)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOutn("CLASS_AVE2", idx, cAvg.toString()); //クラス平均
                }
                if(!"".equals(scoreData._grade_avg2)) {
                    BigDecimal gAvg = new BigDecimal(Double.parseDouble(scoreData._grade_avg2)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOutn("GRADE_AVE2", idx, gAvg.toString()); //学年平均
                }
                svf.VrsOutn("CLASS_MAX2", idx, scoreData._class_highscore2); //クラス最高点
                svf.VrsOutn("GRADE_MAX2", idx, scoreData._grade_highscore2); //学年最高点
            }

            //3学期
            if(_param._semes3Flg) {
                svf.VrsOutn("VAL3", idx, scoreData._score3); //評価
                svf.VrsOutn("CLASS_RANK3", idx, scoreData._class_rank3); //クラス順位
                svf.VrsOutn("GRADE_RANK3", idx, scoreData._grade_rank3); //学年順位
                if(!"".equals(scoreData._class_avg3)) {
                    BigDecimal cAvg = new BigDecimal(Double.parseDouble(scoreData._class_avg3)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOutn("CLASS_AVE3", idx, cAvg.toString()); //クラス平均
                }
                if(!"".equals(scoreData._grade_avg3)) {
                    BigDecimal gAvg = new BigDecimal(Double.parseDouble(scoreData._grade_avg3)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOutn("GRADE_AVE3", idx, gAvg.toString()); //学年平均
                }
                svf.VrsOutn("CLASS_MAX3", idx, scoreData._class_highscore3); //クラス最高点
                svf.VrsOutn("GRADE_MAX3", idx, scoreData._grade_highscore3); //学年最高点
            }

            //学年評価
            if(_param._semes3Flg) svf.VrsOutn("VAL9", idx, scoreData._score9); //学年評価

            idx++;
            svf.VrEndRecord();
        }

        //■学習の記録 合計の印字
        printTotal(db2, svf, student);

        svf.VrEndPage();
        _hasData = true;
    }

    private void printTitle(final DB2UDB db2, final Vrw32alp svf, final Student student) {
    	//明細部以外を印字

        //氏名、担任名
        svf.VrsOut("ATTENDNO", student._attendno2); //年組番
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 24 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名
        final String trNameField = KNJ_EditEdit.getMS932ByteLength(student._staffname) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._staffname) > 24 ? "2" : "1";
        svf.VrsOut("STAFF_NAME" + trNameField, student._staffname); //担任名

        //■特別活動の記録
        String spAct1 = "";
        String sep = "";
        if(!"".equals(student._sp_act1_1)) {
            spAct1 = spAct1 + sep + student._sp_act1_1;
            sep = "\r\n";
        }
        if(!"".equals(student._sp_act1_2) && _param._semes2Flg) {
            spAct1 = spAct1 + sep + student._sp_act1_2;
            sep = "\r\n";
        }
        if(!"".equals(student._sp_act1_3) && _param._semes3Flg) {
            spAct1 = spAct1 + sep + student._sp_act1_3;
            sep = "\r\n";
        }
        VrsOutnRenban(svf, "SP_ACT1", knjobj.retDividString(spAct1, 32, 6)); //特別活動の記録

        //■生徒会活動
        String spAct2 = "";
        sep = "";
        if(!"".equals(student._sp_act2_1)) {
        	spAct2 = spAct2 + sep + student._sp_act2_1;
            sep = "\r\n";
        }
        if(!"".equals(student._sp_act2_2) && _param._semes2Flg) {
            spAct2 = spAct2 + sep + student._sp_act2_2;
            sep = "\r\n";
        }
        if(!"".equals(student._sp_act2_3) && _param._semes3Flg) {
            spAct2 = spAct2 + sep + student._sp_act2_3;
            sep = "\r\n";
        }
        VrsOutnRenban(svf, "SP_ACT2", knjobj.retDividString(spAct2, 32, 6)); //生徒会活動

        //■クラブ部活動
        String spAct4 = "";
        sep = "";
        if(!"".equals(student._sp_act4_1)) {
        	spAct4 = spAct4 + sep + student._sp_act4_1;
            sep = "\r\n";
        }
        if(!"".equals(student._sp_act4_2) && _param._semes2Flg) {
            spAct4 = spAct4 + sep + student._sp_act4_2;
            sep = "\r\n";
        }
        if(!"".equals(student._sp_act4_3) && _param._semes3Flg) {
            spAct4 = spAct4 + sep + student._sp_act4_3;
            sep = "\r\n";
        }
        VrsOutnRenban(svf, "SP_ACT4", knjobj.retDividString(spAct4, 32, 6)); //クラブ部活動

        //■生徒会活動
        String spAct3 = "";
        sep = "";
        if(!"".equals(student._sp_act3_1)) {
        	spAct3 = spAct3 + sep + student._sp_act3_1;
            sep = "\r\n";
        }
        if(!"".equals(student._sp_act3_2) && _param._semes2Flg) {
        	spAct3 = spAct3 + sep + student._sp_act3_2;
            sep = "\r\n";
        }
        if(!"".equals(student._sp_act3_3) && _param._semes3Flg) {
        	spAct3 = spAct3 + sep + student._sp_act3_3;
            sep = "\r\n";
        }
        VrsOutnRenban(svf, "SP_ACT3", knjobj.retDividString(spAct3, 32, 6)); //生徒会活動

        //■出欠の記録
        printAttend(svf, student);
    }

    private void printTotal(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        //■学習の記録 合計の印字
        final String subclassCd = "99-" + student._schoolKind + "-99-999999";
        final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);
        if(scoreData == null) return;

        //1学期
        svf.VrsOut("TOTAL_VAL1", scoreData._score1); //評価
        svf.VrsOut("TOTAL_CLASS_RANK1", scoreData._class_rank1); //クラス順位
        svf.VrsOut("TOTAL_GRADE_RANK1", scoreData._grade_rank1); //学年順位
        svf.VrsOut("TOTAL_CLASS_AVE1", scoreData._class_avg1); //クラス平均
        svf.VrsOut("TOTAL_GRADE_AVE1", scoreData._grade_avg1); //学年平均
        svf.VrsOut("TOTAL_CLASS_MAX1", scoreData._class_highscore1); //クラス最高点
        svf.VrsOut("TOTAL_GRADE_MAX1", scoreData._grade_highscore1); //学年最高点

        //2学期
        if(_param._semes2Flg) {
            svf.VrsOut("TOTAL_VAL2", scoreData._score2); //評価
            svf.VrsOut("TOTAL_CLASS_RANK2", scoreData._class_rank2); //クラス順位
            svf.VrsOut("TOTAL_GRADE_RANK2", scoreData._grade_rank2); //学年順位
            svf.VrsOut("TOTAL_CLASS_AVE2", scoreData._class_avg2); //クラス平均
            svf.VrsOut("TOTAL_GRADE_AVE2", scoreData._grade_avg2); //学年平均
            svf.VrsOut("TOTAL_CLASS_MAX2", scoreData._class_highscore2); //クラス最高点
            svf.VrsOut("TOTAL_GRADE_MAX2", scoreData._grade_highscore2); //学年最高点
        }

        //3学期
        if(_param._semes3Flg) {
            svf.VrsOut("TOTAL_VAL3", scoreData._score3); //評価
            svf.VrsOut("TOTAL_CLASS_RANK3", scoreData._class_rank3); //クラス順位
            svf.VrsOut("TOTAL_GRADE_RANK3", scoreData._grade_rank3); //学年順位
            svf.VrsOut("TOTAL_CLASS_AVE3", scoreData._class_avg3); //クラス平均
            svf.VrsOut("TOTAL_GRADE_AVE3", scoreData._grade_avg3); //学年平均
            svf.VrsOut("TOTAL_CLASS_MAX3", scoreData._class_highscore3); //クラス最高点
            svf.VrsOut("TOTAL_GRADE_MAX3", scoreData._grade_highscore3); //学年最高点
        }
    }

    private List subclassListRemoveD026() {
        final List retList = new ArrayList(_param._subclassMstMap.values());
        for (final Iterator it = retList.iterator(); it.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) it.next();
            if (_param._d026List.contains(subclassMst._subclasscd)) {
                it.remove();
            } else if (_param._isNoPrintMoto &&  subclassMst._isMoto || !_param._isPrintSakiKamoku &&  subclassMst._isSaki) {
                it.remove();
            }
        }
        return retList;
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final List list) {
        if (null != list) {
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOutn(field, i + 1, (String) list.get(i));
            }
        }
    }

    // 出欠記録
    private void printAttend(final Vrw32alp svf, final Student student) {
        for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next();
            final int line = getSemeLine(semester);
            final Attendance att = (Attendance) student._attendMap.get(semester);

            if(line == 2 && !_param._semes2Flg) continue;
            if(line == 3 && !_param._semes3Flg) continue;
            if (null != att) {
                svf.VrsOutn("LESSON", line, String.valueOf(att._lesson));     // 授業日数
                svf.VrsOutn("MOURNING", line, String.valueOf(att._mourning)); // 出席停止忌引等
                svf.VrsOutn("MUST", line, String.valueOf(att._mLesson));      // 出席しなければならない日数
                svf.VrsOutn("ABSENT", line, String.valueOf(att._absent));     // 欠席日数
                svf.VrsOutn("PRESENT", line, String.valueOf(att._present));   // 出席日数
                svf.VrsOutn("LATE", line, String.valueOf(att._late));         // 遅刻
                svf.VrsOutn("EARLY", line, String.valueOf(att._early));       // 早退
            } else {
                svf.VrsOutn("LESSON", line, "0");   // 授業日数
                svf.VrsOutn("MOURNING", line, "0"); // 出席停止忌引等
                svf.VrsOutn("MUST", line, "0");     // 出席しなければならない日数
                svf.VrsOutn("ABSENT", line, "0");   // 欠席日数
                svf.VrsOutn("PRESENT", line, "0");  // 出席日数
                svf.VrsOutn("LATE", line, "0");     // 遅刻
                svf.VrsOutn("EARLY", line, "0");    // 早退
            }
//            final String remark = line == 1 ? student._attendrec_remark1 : line == 2 ? student._attendrec_remark2 : line == 3 ?  student._attendrec_remark3 : "";
//            svf.VrsOutn("ATTEND_REMARK", line, remark);    // 出欠備考
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
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._birthday = rs.getString("BIRTHDAY");
                student._schoolKind = rs.getString("SCHOOL_KIND");
                student._gradename = rs.getString("GRADE_NAME2");
                student._hrname = rs.getString("HR_NAME");
                student._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                student._staffname2 = StringUtils.defaultString(rs.getString("STAFFNAME2"));
                student._attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番" : rs.getString("ATTENDNO");
                student._attendno2 = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");
                student._grade = rs.getString("GRADE");
                student._hrClass = rs.getString("HR_CLASS");
                student._coursecd = rs.getString("COURSECD");
                student._majorcd = rs.getString("MAJORCD");
                student._course = rs.getString("COURSE");
                student._majorname = rs.getString("MAJORNAME");
                student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                student._entyear = rs.getString("ENT_YEAR");
                student._sp_act1_1 = StringUtils.defaultString(rs.getString("SP_ACT1_1"));
                student._sp_act1_2 = StringUtils.defaultString(rs.getString("SP_ACT1_2"));
                student._sp_act1_3 = StringUtils.defaultString(rs.getString("SP_ACT1_3"));
                student._sp_act2_1 = StringUtils.defaultString(rs.getString("SP_ACT2_1"));
                student._sp_act2_2 = StringUtils.defaultString(rs.getString("SP_ACT2_2"));
                student._sp_act2_3 = StringUtils.defaultString(rs.getString("SP_ACT2_3"));
                student._sp_act4_1 = StringUtils.defaultString(rs.getString("SP_ACT4_1"));
                student._sp_act4_2 = StringUtils.defaultString(rs.getString("SP_ACT4_2"));
                student._sp_act4_3 = StringUtils.defaultString(rs.getString("SP_ACT4_3"));
                student._sp_act3_1 = StringUtils.defaultString(rs.getString("SP_ACT3_1"));
                student._sp_act3_2 = StringUtils.defaultString(rs.getString("SP_ACT3_2"));
                student._sp_act3_3 = StringUtils.defaultString(rs.getString("SP_ACT3_3"));
//                student._attendrec_remark1 = StringUtils.defaultString(rs.getString("ATTENDREC_REMARK1"));
//                student._attendrec_remark2 = StringUtils.defaultString(rs.getString("ATTENDREC_REMARK2"));
//                student._attendrec_remark3 = StringUtils.defaultString(rs.getString("ATTENDREC_REMARK3"));

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
        stb.append("        AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrClass + "' ");
        stb.append("        AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));

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
        stb.append("            ,BASE.BIRTHDAY ");
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
        stb.append("            ,RD011.REMARK1 AS SP_ACT1_1 ");
        stb.append("            ,RD012.REMARK1 AS SP_ACT1_2 ");
        stb.append("            ,RD013.REMARK1 AS SP_ACT1_3 ");
        stb.append("            ,RD021.REMARK1 AS SP_ACT2_1 ");
        stb.append("            ,RD022.REMARK1 AS SP_ACT2_2 ");
        stb.append("            ,RD023.REMARK1 AS SP_ACT2_3 ");
        stb.append("            ,RD031.REMARK1 AS SP_ACT4_1 ");
        stb.append("            ,RD032.REMARK1 AS SP_ACT4_2 ");
        stb.append("            ,RD033.REMARK1 AS SP_ACT4_3 ");
        stb.append("            ,RD041.REMARK1 AS SP_ACT3_1 ");
        stb.append("            ,RD042.REMARK1 AS SP_ACT3_2 ");
        stb.append("            ,RD043.REMARK1 AS SP_ACT3_3 ");
//        stb.append("            ,R1.ATTENDREC_REMARK AS ATTENDREC_REMARK1 ");
//        stb.append("            ,R2.ATTENDREC_REMARK AS ATTENDREC_REMARK2 ");
//        stb.append("            ,R3.ATTENDREC_REMARK AS ATTENDREC_REMARK3 ");
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
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD011 ");
        stb.append("            ON RD011.YEAR     = REGD.YEAR ");
        stb.append("           AND RD011.SEMESTER = '1' ");
        stb.append("           AND RD011.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD011.DIV      = '01' ");
        stb.append("           AND RD011.CODE     = '01' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD012 ");
        stb.append("            ON RD012.YEAR     = REGD.YEAR ");
        stb.append("           AND RD012.SEMESTER = '2' ");
        stb.append("           AND RD012.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD012.DIV      = '01' ");
        stb.append("           AND RD012.CODE     = '01' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD013 ");
        stb.append("            ON RD013.YEAR     = REGD.YEAR ");
        stb.append("           AND RD013.SEMESTER = '3' ");
        stb.append("           AND RD013.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD013.DIV      = '01' ");
        stb.append("           AND RD013.CODE     = '01' ");
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
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD031 ");
        stb.append("            ON RD031.YEAR     = REGD.YEAR ");
        stb.append("           AND RD031.SEMESTER = '1' ");
        stb.append("           AND RD031.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD031.DIV      = '01' ");
        stb.append("           AND RD031.CODE     = '03' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD032 ");
        stb.append("            ON RD032.YEAR     = REGD.YEAR ");
        stb.append("           AND RD032.SEMESTER = '2' ");
        stb.append("           AND RD032.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD032.DIV      = '01' ");
        stb.append("           AND RD032.CODE     = '03' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD033 ");
        stb.append("            ON RD033.YEAR     = REGD.YEAR ");
        stb.append("           AND RD033.SEMESTER = '3' ");
        stb.append("           AND RD033.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD033.DIV      = '01' ");
        stb.append("           AND RD033.CODE     = '03' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD041 ");
        stb.append("            ON RD041.YEAR     = REGD.YEAR ");
        stb.append("           AND RD041.SEMESTER = '1' ");
        stb.append("           AND RD041.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD041.DIV      = '01' ");
        stb.append("           AND RD041.CODE     = '04' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD042 ");
        stb.append("            ON RD042.YEAR     = REGD.YEAR ");
        stb.append("           AND RD042.SEMESTER = '2' ");
        stb.append("           AND RD042.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD042.DIV      = '01' ");
        stb.append("           AND RD042.CODE     = '04' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT RD043 ");
        stb.append("            ON RD043.YEAR     = REGD.YEAR ");
        stb.append("           AND RD043.SEMESTER = '3' ");
        stb.append("           AND RD043.SCHREGNO = REGD.SCHREGNO ");
        stb.append("           AND RD043.DIV      = '01' ");
        stb.append("           AND RD043.CODE     = '04' ");
// 出欠の記録 備考
//        stb.append("     LEFT JOIN HREPORTREMARK_DAT R1 ");
//        stb.append("            ON R1.YEAR     = REGD.YEAR ");
//        stb.append("           AND R1.SEMESTER = '1' ");
//        stb.append("           AND R1.SCHREGNO = REGD.SCHREGNO ");
//        stb.append("     LEFT JOIN HREPORTREMARK_DAT R2 ");
//        stb.append("            ON R2.YEAR     = REGD.YEAR ");
//        stb.append("           AND R2.SEMESTER = '2' ");
//        stb.append("           AND R2.SCHREGNO = REGD.SCHREGNO ");
//        stb.append("     LEFT JOIN HREPORTREMARK_DAT R3 ");
//        stb.append("            ON R3.YEAR     = REGD.YEAR ");
//        stb.append("           AND R3.SEMESTER = '3' ");
//        stb.append("           AND R3.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     WHERE   REGD.YEAR = '" + _param._loginYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND REGD.SEMESTER = '" + _param._loginSemester + "' ");
        } else {
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("         AND REGD.GRADE || REGD.HR_CLASS = '" + _param._gradeHrClass + "' ");
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

    private class Student {
        String _schregno;
        String _name;
        String _birthday;
        String _schoolKind;
        String _gradename;
        String _hrname;
        String _staffname;
        String _staffname2;
        String _attendno;
        String _attendno2;
        String _grade;
        String _hrClass;
        String _coursecd;
        String _majorcd;
        String _course;
        String _majorname;
        String _hrClassName1;
        String _entyear;
        String _sp_act1_1;
        String _sp_act1_2;
        String _sp_act1_3;
        String _sp_act2_1;
        String _sp_act2_2;
        String _sp_act2_3;
        String _sp_act4_1;
        String _sp_act4_2;
        String _sp_act4_3;
        String _sp_act3_1;
        String _sp_act3_2;
        String _sp_act3_3;
//        String _attendrec_remark1;
//        String _attendrec_remark2;
//        String _attendrec_remark3;

        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map _attendSubClassMap = new HashMap();

        public Student() {
        }

        private void setSubclass(final DB2UDB db2) {
            final String scoreSql = prestatementSubclass();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                log.debug(" sql =" + scoreSql);
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String score1 = StringUtils.defaultString(rs.getString("SCORE1"));
                    final String class_rank1 = StringUtils.defaultString(rs.getString("CLASS_RANK1"));
                    final String grade_rank1 = StringUtils.defaultString(rs.getString("GRADE_RANK1"));
                    final String class_avg1 = StringUtils.defaultString(rs.getString("CLASS_AVG1"));
                    final String grade_avg1 = StringUtils.defaultString(rs.getString("GRADE_AVG1"));
                    final String class_highscore1 = StringUtils.defaultString(rs.getString("CLASS_HIGHSCORE1"));
                    final String grade_highscore1 = StringUtils.defaultString(rs.getString("GRADE_HIGHSCORE1"));
                    final String score2 = StringUtils.defaultString(rs.getString("SCORE2"));
                    final String class_rank2 = StringUtils.defaultString(rs.getString("CLASS_RANK2"));
                    final String grade_rank2 = StringUtils.defaultString(rs.getString("GRADE_RANK2"));
                    final String class_avg2 = StringUtils.defaultString(rs.getString("CLASS_AVG2"));
                    final String grade_avg2 = StringUtils.defaultString(rs.getString("GRADE_AVG2"));
                    final String class_highscore2 = StringUtils.defaultString(rs.getString("CLASS_HIGHSCORE2"));
                    final String grade_highscore2 = StringUtils.defaultString(rs.getString("GRADE_HIGHSCORE2"));
                    final String score3 = StringUtils.defaultString(rs.getString("SCORE3"));
                    final String class_rank3 = StringUtils.defaultString(rs.getString("CLASS_RANK3"));
                    final String grade_rank3 = StringUtils.defaultString(rs.getString("GRADE_RANK3"));
                    final String class_avg3 = StringUtils.defaultString(rs.getString("CLASS_AVG3"));
                    final String grade_avg3 = StringUtils.defaultString(rs.getString("GRADE_AVG3"));
                    final String class_highscore3 = StringUtils.defaultString(rs.getString("CLASS_HIGHSCORE3"));
                    final String grade_highscore3 = StringUtils.defaultString(rs.getString("GRADE_HIGHSCORE3"));
                    final String score9 = StringUtils.defaultString(rs.getString("SCORE9"));

                    final String key = subclasscd;
                    ScoreData scoreData = new ScoreData(classcd, classname, subclasscd, subclassname, score1, class_rank1, grade_rank1, class_avg1, grade_avg1, class_highscore1, grade_highscore1, score2, class_rank2, grade_rank2, class_avg2, grade_avg2, class_highscore2, grade_highscore2, score3, class_rank3, grade_rank3, class_avg3, grade_avg3, class_highscore3, grade_highscore3, score9);
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
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" S2.CLASSCD, ");
                stb.append(" S2.SCHOOL_KIND, ");
                stb.append(" S2.CURRICULUM_CD, ");
            }
            stb.append("     S2.SUBCLASSCD ");
            //成績明細データの表
            stb.append(" ) ,RECORD AS( ");
            stb.append(" SELECT DISTINCT");
            stb.append("     T1.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     L2.SCORE AS SCORE1, ");
            stb.append("     L2.CLASS_RANK AS CLASS_RANK1, ");
            stb.append("     L2.GRADE_RANK AS GRADE_RANK1, ");
            stb.append("     L3.AVG AS CLASS_AVG1, ");
            stb.append("     L4.AVG AS GRADE_AVG1, ");
            stb.append("     L3.HIGHSCORE AS CLASS_HIGHSCORE1, ");
            stb.append("     L4.HIGHSCORE AS GRADE_HIGHSCORE1, ");
            stb.append("     L5.SCORE AS SCORE2, ");
            stb.append("     L5.CLASS_RANK AS CLASS_RANK2, ");
            stb.append("     L5.GRADE_RANK AS GRADE_RANK2, ");
            stb.append("     L6.AVG AS CLASS_AVG2, ");
            stb.append("     L7.AVG AS GRADE_AVG2, ");
            stb.append("     L6.HIGHSCORE AS CLASS_HIGHSCORE2, ");
            stb.append("     L7.HIGHSCORE AS GRADE_HIGHSCORE2, ");
            stb.append("     L8.SCORE AS SCORE3, ");
            stb.append("     L8.CLASS_RANK AS CLASS_RANK3, ");
            stb.append("     L8.GRADE_RANK AS GRADE_RANK3, ");
            stb.append("     L9.AVG AS CLASS_AVG3, ");
            stb.append("     L10.AVG AS GRADE_AVG3, ");
            stb.append("     L9.HIGHSCORE AS CLASS_HIGHSCORE3, ");
            stb.append("     L10.HIGHSCORE AS GRADE_HIGHSCORE3, ");
            stb.append("     L11.SCORE AS SCORE9 ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L2 ");
            stb.append("            ON L2.YEAR          = T1.YEAR ");
            stb.append("           AND L2.SEMESTER || L2.TESTKINDCD || L2.TESTITEMCD || L2.SCORE_DIV = '" + SDIV1990008 + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND L2.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L2.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT L3 ");
            stb.append("            ON L3.YEAR          = T1.YEAR ");
            stb.append("           AND L3.SEMESTER || L3.TESTKINDCD || L3.TESTITEMCD || L3.SCORE_DIV = '" + SDIV1990008 + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND L3.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L3.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L3.AVG_DIV       = '2' ");
            stb.append("           AND L3.GRADE         = '" + _grade + "' ");
            stb.append("           AND L3.HR_CLASS      = '" + _hrClass + "' ");
            stb.append("           AND L3.COURSECD      = '0' ");
            stb.append("           AND L3.MAJORCD       = '000' ");
            stb.append("           AND L3.COURSECODE    = '0000' ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT L4 ");
            stb.append("            ON L4.YEAR          = T1.YEAR ");
            stb.append("           AND L4.SEMESTER || L4.TESTKINDCD || L4.TESTITEMCD || L4.SCORE_DIV = '" + SDIV1990008 + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND L4.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L4.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L4.AVG_DIV       = '1' ");
            stb.append("           AND L4.GRADE         = '" + _grade + "' ");
            stb.append("           AND L4.HR_CLASS      = '000' ");
            stb.append("           AND L4.COURSECD      = '0' ");
            stb.append("           AND L4.MAJORCD       = '000' ");
            stb.append("           AND L4.COURSECODE    = '0000' ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L5 ");
            stb.append("            ON L5.YEAR          = T1.YEAR ");
            stb.append("           AND L5.SEMESTER || L5.TESTKINDCD || L5.TESTITEMCD || L5.SCORE_DIV = '" + SDIV2990008 + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND L5.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L5.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L5.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L5.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT L6 ");
            stb.append("            ON L6.YEAR          = T1.YEAR ");
            stb.append("           AND L6.SEMESTER || L6.TESTKINDCD || L6.TESTITEMCD || L6.SCORE_DIV = '" + SDIV2990008 + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND L6.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L6.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L6.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L6.AVG_DIV       = '2' ");
            stb.append("           AND L6.GRADE         = '" + _grade + "' ");
            stb.append("           AND L6.HR_CLASS      = '" + _hrClass + "' ");
            stb.append("           AND L6.COURSECD      = '0' ");
            stb.append("           AND L6.MAJORCD       = '000' ");
            stb.append("           AND L6.COURSECODE    = '0000' ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT L7 ");
            stb.append("            ON L7.YEAR          = T1.YEAR ");
            stb.append("           AND L7.SEMESTER || L7.TESTKINDCD || L7.TESTITEMCD || L7.SCORE_DIV = '" + SDIV2990008 + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND L7.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L7.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L7.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L7.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L7.AVG_DIV       = '1' ");
            stb.append("           AND L7.GRADE         = '" + _grade + "' ");
            stb.append("           AND L7.HR_CLASS      = '000' ");
            stb.append("           AND L7.COURSECD      = '0' ");
            stb.append("           AND L7.MAJORCD       = '000' ");
            stb.append("           AND L7.COURSECODE    = '0000' ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L8 ");
            stb.append("            ON L8.YEAR          = T1.YEAR ");
            stb.append("           AND L8.SEMESTER || L8.TESTKINDCD || L8.TESTITEMCD || L8.SCORE_DIV = '" + SDIV3990008 + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND L8.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L8.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L8.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L8.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L8.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT L9 ");
            stb.append("            ON L9.YEAR          = T1.YEAR ");
            stb.append("           AND L9.SEMESTER || L9.TESTKINDCD || L9.TESTITEMCD || L9.SCORE_DIV = '" + SDIV3990008 + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND L9.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L9.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L9.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L9.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L9.AVG_DIV       = '2' ");
            stb.append("           AND L9.GRADE         = '" + _grade + "' ");
            stb.append("           AND L9.HR_CLASS      = '" + _hrClass + "' ");
            stb.append("           AND L9.COURSECD      = '0' ");
            stb.append("           AND L9.MAJORCD       = '000' ");
            stb.append("           AND L9.COURSECODE    = '0000' ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT L10 ");
            stb.append("            ON L10.YEAR          = T1.YEAR ");
            stb.append("           AND L10.SEMESTER || L10.TESTKINDCD || L10.TESTITEMCD || L10.SCORE_DIV = '" + SDIV3990008 + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND L10.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L10.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L10.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L10.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L10.AVG_DIV       = '1' ");
            stb.append("           AND L10.GRADE         = '" + _grade + "' ");
            stb.append("           AND L10.HR_CLASS      = '000' ");
            stb.append("           AND L10.COURSECD      = '0' ");
            stb.append("           AND L10.MAJORCD       = '000' ");
            stb.append("           AND L10.COURSECODE    = '0000' ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L11 ");
            stb.append("            ON L11.YEAR          = T1.YEAR ");
            stb.append("           AND L11.SEMESTER || L11.TESTKINDCD || L11.TESTITEMCD || L11.SCORE_DIV = '" + SDIV9990009 + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("           AND L11.CLASSCD       = T1.CLASSCD ");
                stb.append("           AND L11.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("           AND L11.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("           AND L11.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L11.SCHREGNO      = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._loginYear + "'  ");
            stb.append("     AND EXISTS(SELECT ");
            stb.append("                  'X' ");
            stb.append("                FROM ");
            stb.append("                  SCHNO T2 ");
            stb.append("                WHERE ");
            stb.append("                  T2.SCHREGNO = T1.SCHREGNO ");
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
            stb.append("     T1.CLASS_RANK1, ");
            stb.append("     T1.GRADE_RANK1, ");
            stb.append("     T1.CLASS_AVG1, ");
            stb.append("     T1.GRADE_AVG1, ");
            stb.append("     T1.CLASS_HIGHSCORE1, ");
            stb.append("     T1.GRADE_HIGHSCORE1, ");
            stb.append("     T1.SCORE2, ");
            stb.append("     T1.CLASS_RANK2, ");
            stb.append("     T1.GRADE_RANK2, ");
            stb.append("     T1.CLASS_AVG2, ");
            stb.append("     T1.GRADE_AVG2, ");
            stb.append("     T1.CLASS_HIGHSCORE2, ");
            stb.append("     T1.GRADE_HIGHSCORE2, ");
            stb.append("     T1.SCORE3, ");
            stb.append("     T1.CLASS_RANK3, ");
            stb.append("     T1.GRADE_RANK3, ");
            stb.append("     T1.CLASS_AVG3, ");
            stb.append("     T1.GRADE_AVG3, ");
            stb.append("     T1.CLASS_HIGHSCORE3, ");
            stb.append("     T1.GRADE_HIGHSCORE3, ");
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
            //合計
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     SUBSTR(T1.SUBCLASSCD,1,2) AS CLASSCD, ");
            stb.append("     '' AS CLASSNAME, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     '' AS SUBCLASSNAME, ");
            stb.append("     T1.SCORE1, ");
            stb.append("     T1.CLASS_RANK1, ");
            stb.append("     T1.GRADE_RANK1, ");
            stb.append("     T1.CLASS_AVG1, ");
            stb.append("     T1.GRADE_AVG1, ");
            stb.append("     T1.CLASS_HIGHSCORE1, ");
            stb.append("     T1.GRADE_HIGHSCORE1, ");
            stb.append("     T1.SCORE2, ");
            stb.append("     T1.CLASS_RANK2, ");
            stb.append("     T1.GRADE_RANK2, ");
            stb.append("     T1.CLASS_AVG2, ");
            stb.append("     T1.GRADE_AVG2, ");
            stb.append("     T1.CLASS_HIGHSCORE2, ");
            stb.append("     T1.GRADE_HIGHSCORE2, ");
            stb.append("     T1.SCORE3, ");
            stb.append("     T1.CLASS_RANK3, ");
            stb.append("     T1.GRADE_RANK3, ");
            stb.append("     T1.CLASS_AVG3, ");
            stb.append("     T1.GRADE_AVG3, ");
            stb.append("     T1.CLASS_HIGHSCORE3, ");
            stb.append("     T1.GRADE_HIGHSCORE3, ");
            stb.append("     T1.SCORE9 ");
            stb.append(" FROM ");
            stb.append("     SCHNO T2 ");
            stb.append("     INNER JOIN RECORD T1 ");
            stb.append("            ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("           AND T1.SUBCLASSCD = '" + ALL9 + "' ");
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
            stb.append("     T1.CLASS_RANK1, ");
            stb.append("     T1.GRADE_RANK1, ");
            stb.append("     T1.CLASS_AVG1, ");
            stb.append("     T1.GRADE_AVG1, ");
            stb.append("     T1.CLASS_HIGHSCORE1, ");
            stb.append("     T1.GRADE_HIGHSCORE1, ");
            stb.append("     T1.SCORE2, ");
            stb.append("     T1.CLASS_RANK2, ");
            stb.append("     T1.GRADE_RANK2, ");
            stb.append("     T1.CLASS_AVG2, ");
            stb.append("     T1.GRADE_AVG2, ");
            stb.append("     T1.CLASS_HIGHSCORE2, ");
            stb.append("     T1.GRADE_HIGHSCORE2, ");
            stb.append("     T1.SCORE3, ");
            stb.append("     T1.CLASS_RANK3, ");
            stb.append("     T1.GRADE_RANK3, ");
            stb.append("     T1.CLASS_AVG3, ");
            stb.append("     T1.GRADE_AVG3, ");
            stb.append("     T1.CLASS_HIGHSCORE3, ");
            stb.append("     T1.GRADE_HIGHSCORE3, ");
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
        final String _class_rank1;
        final String _grade_rank1;
        final String _class_avg1;
        final String _grade_avg1;
        final String _class_highscore1;
        final String _grade_highscore1;
        final String _score2;
        final String _class_rank2;
        final String _grade_rank2;
        final String _class_avg2;
        final String _grade_avg2;
        final String _class_highscore2;
        final String _grade_highscore2;
        final String _score3;
        final String _class_rank3;
        final String _grade_rank3;
        final String _class_avg3;
        final String _grade_avg3;
        final String _class_highscore3;
        final String _grade_highscore3;
        final String _score9;
        private ScoreData(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String subclassname,
                final String score1,
                final String class_rank1,
                final String grade_rank1,
                final String class_avg1,
                final String grade_avg1,
                final String class_highscore1,
                final String grade_highscore1,
                final String score2,
                final String class_rank2,
                final String grade_rank2,
                final String class_avg2,
                final String grade_avg2,
                final String class_highscore2,
                final String grade_highscore2,
                final String score3,
                final String class_rank3,
                final String grade_rank3,
                final String class_avg3,
                final String grade_avg3,
                final String class_highscore3,
                final String grade_highscore3,
                final String score9
        ) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _score1 = score1;
            _class_rank1 = class_rank1;
            _grade_rank1 = grade_rank1;
            _class_avg1 = class_avg1;
            _grade_avg1 = grade_avg1;
            _class_highscore1 = class_highscore1;
            _grade_highscore1 = grade_highscore1;
            _score2 = score2;
            _class_rank2 = class_rank2;
            _grade_rank2 = grade_rank2;
            _class_avg2 = class_avg2;
            _grade_avg2 = grade_avg2;
            _class_highscore2 = class_highscore2;
            _grade_highscore2 = grade_highscore2;
            _score3 = score3;
            _class_rank3 = class_rank3;
            _grade_rank3 = grade_rank3;
            _class_avg3 = class_avg3;
            _grade_avg3 = grade_avg3;
            _class_highscore3 = class_highscore3;
            _grade_highscore3 = grade_highscore3;
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


    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74070 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String[] _categorySelected;
        final String _date;
        final String _gradeHrClass;
        final String _grade;
        final String _loginSemester;
        final String _loginYear;
        final String _prgid;
        final String _semester;
        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private Map _semesterMap;
        private Map _subclassMstMap;
        private final Map _jviewGradeMap;
        private List _d026List = new ArrayList();
        Map _attendRanges;

        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _backSlashImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolStampPath;
        final String _schoolStampRotatePath;

        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;

        final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _categorySelected = request.getParameterValues("category_selected");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrClass.substring(0, 2);
            _loginSemester = request.getParameter("CTRL_SEME");
            _loginYear = request.getParameter("YEAR");
            _prgid = request.getParameter("PRGID");
            _semester = request.getParameter("SEMESTER");

            _semes2Flg = "2".equals(_semester) || "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes3Flg = "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes9Flg = "9".equals(_semester) ? true : false;

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

            _semesterMap = loadSemester(db2);
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
            _schoolStampRotatePath = getImageFilePath("SCHOOLSTAMP_ROTATE.bmp");

            _useCurriculumcd = request.getParameter("useCurriculumcd");

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

    }
}

// eof
