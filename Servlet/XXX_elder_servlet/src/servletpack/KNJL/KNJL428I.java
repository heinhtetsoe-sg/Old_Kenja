// kanji=漢字
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: c637d1ec28e87882ded86e262437da919a51e28e $
 */
public class KNJL428I {

    private static final Log log = LogFactory.getLog("KNJL428I.class");

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

            _hasData = printMain(db2, svf);
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

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        final Map applicantMap = getApplicantMap(db2); //志願者Map

        if(applicantMap.isEmpty()) {
            return false;
        }

        log.debug(applicantMap.size());
        final int maxLine = 45; //最大印字行
        int page = 0; // ページ
        int line = 1; //印字行
        final String form = "1".equals(_param._testDiv) ? "KNJL428I_1.frm" : "KNJL428I_2.frm";

        for (Iterator ite = applicantMap.keySet().iterator(); ite.hasNext();) {
            final String Key = (String)ite.next();
            final Applicant applicant = (Applicant)applicantMap.get(Key);

            if (line > maxLine || page == 0) {
                if(line > maxLine) svf.VrEndPage();
                page++;
                line = 1;
                svf.VrSetForm(form, 1);
                svf.VrsOut("PAGE", String.valueOf(page) + "頁"); //ページ

                final String date = h_format_Seireki_MD(_param._date);
                svf.VrsOut("DATE", date + " " + _param._time); //作成日付
                svf.VrsOut("TITLE", _param._year + "年度　入学試験　　" + _param._testAbbv + "　　調査書の記録"); //タイトル
                svf.VrsOut("SCHOOL_NAME", _param._schoolName); //学校名
                svf.VrsOut("CLASS_NAME1", "国語"); //教科01
                svf.VrsOut("CLASS_NAME2", "社会"); //教科02
                svf.VrsOut("CLASS_NAME3", "数学"); //教科03
                svf.VrsOut("CLASS_NAME4", "理科"); //教科04
                svf.VrsOut("CLASS_NAME5", "音楽"); //教科05
                svf.VrsOut("CLASS_NAME6", "美術"); //教科06
                svf.VrsOut("CLASS_NAME7", "保体"); //教科07
                svf.VrsOut("CLASS_NAME8", "技家"); //教科08
                svf.VrsOut("CLASS_NAME9", "英語"); //教科09
            }

            final String fieldName = getFieldName(applicant._name);
            svf.VrsOutn("NAME" + fieldName, line, applicant._name); //氏名
            final String fieldSchool = getFieldSchoolName(applicant._finschool_Name);
            svf.VrsOutn("FINSCHOOL_NAME" + fieldSchool, line, applicant._finschool_Name); //学校名称

            //評定
            if(applicant._applicantConfRptDat != null) {
                svf.VrsOutn("DIV1", line, printScore(applicant._applicantConfRptDat._confidential_rpt01,2));
                svf.VrsOutn("DIV2", line, printScore(applicant._applicantConfRptDat._confidential_rpt02,2));
                svf.VrsOutn("DIV3", line, printScore(applicant._applicantConfRptDat._confidential_rpt03,2));
                svf.VrsOutn("DIV4", line, printScore(applicant._applicantConfRptDat._confidential_rpt04,2));
                svf.VrsOutn("DIV5", line, printScore(applicant._applicantConfRptDat._confidential_rpt05,2));
                svf.VrsOutn("DIV6", line, printScore(applicant._applicantConfRptDat._confidential_rpt06,2));
                svf.VrsOutn("DIV7", line, printScore(applicant._applicantConfRptDat._confidential_rpt07,2));
                svf.VrsOutn("DIV8", line, printScore(applicant._applicantConfRptDat._confidential_rpt08,2));
                svf.VrsOutn("DIV9", line, printScore(applicant._applicantConfRptDat._confidential_rpt09,2));
                svf.VrsOutn("ABSENT1", line, applicant._applicantConfRptDat._absenceDays); //欠席1
                svf.VrsOutn("ABSENT2", line, applicant._applicantConfRptDat._absenceDays2); //欠席2
                svf.VrsOutn("ABSENT3", line, applicant._applicantConfRptDat._absenceDays3); //欠席3

                //9教科合計
                svf.VrsOutn("PASS_SCORE", line, printScore(applicant._applicantConfRptDat._totalAll,29));
            }

            //9,3教科ポイント
            final String point9 = printPoint(applicant._applicantConfRptDetaiDat._total9_Plus, applicant._applicantConfRptDetaiDat._total9_Minus);
            final String point3 = printPoint(applicant._applicantConfRptDetaiDat._total3_Plus, applicant._applicantConfRptDetaiDat._total3_Minus);

            //出欠
            svf.VrsOutn("POINT3", line, printPoint(applicant._applicantConfRptDetaiDat._absence_Total_Plus, applicant._applicantConfRptDetaiDat._absence_Total_Minus));

            //A方式
            if("1".equals(_param._testDiv)) {
                boolean katen9 = false; //9教科加点フラグ

                svf.VrsOutn("EXAM_NO1", line, applicant._examno); //受験番号A
                svf.VrsOutn("EXAM_NO2", line, applicant._recom_Examno); //受験番号B

                //英検
                if(_param._eikenMap.containsKey(applicant._applicantConfRptDetaiDat._qualified_Eng)) {
                    final String eikenName = (String) _param._eikenMap.get(applicant._applicantConfRptDetaiDat._qualified_Eng);
                    svf.VrsOutn("EIKEN", line, eikenName);
                }

                //9教科ポイント
                if(point9 != null && Integer.parseInt(point9) > 0) { //＋なら網掛け
                    svf.VrAttributen("POINT1", line, "Paint=(9,10,2),Bold=1");
                    svf.VrsOutn("POINT1", line, String.valueOf(point9));
                    svf.VrAttributen("POINT1", line, "Paint=(0,0,0),Bold=0");
                    katen9 = true;
                } else {
                    svf.VrsOutn("POINT1", line, point9);
                }

                svf.VrsOutn("POINT2", line, point3); //3教科
                svf.VrsOutn("POINT4", line, applicant._applicantConfRptDetaiDat._report_Plus); //調+
                svf.VrsOutn("POINT5", line, applicant._applicantConfRptDetaiDat._report_Minus); //調-
                svf.VrsOutn("POINT6", line, applicant._abbv1); //特相
                svf.VrsOutn("POINT7", line, "2".equals(applicant._remark1) ? "第２" : ""); //志望
                svf.VrsOutn("POINT8", line, "1".equals(applicant._remark1) ? _param._katen : ""); //加点
                svf.VrsOutn("POINT9", line, applicant._applicantConfRptDetaiDat._tyousei_Plus); //調+
                svf.VrsOutn("POINT10", line, applicant._applicantConfRptDetaiDat._tyousei_Minus); //調-

                //評価
                final String hyoukaPlus = "0".equals(applicant._applicantConfRptDetaiDat._hyouka_Plus) ? "  " : applicant._applicantConfRptDetaiDat._hyouka_Plus != null ? applicant._applicantConfRptDetaiDat._hyouka_Plus : "  ";
                final String mark;
                final String hyoukaMinus;
                if (Integer.parseInt(nullCheck(applicant._applicantConfRptDetaiDat._hyouka_Minus)) > 0) {
                    mark = "● -";
                    hyoukaMinus = applicant._applicantConfRptDetaiDat._hyouka_Minus;
                } else {
                    if (applicant._applicantConfRptDetaiDat._hyouka_Plus == null || "0".equals(applicant._applicantConfRptDetaiDat._hyouka_Plus)) {
                        mark = "";
                    } else {
                        mark = "○";
                    }
                    hyoukaMinus = "";
                }
                String space = "";
                for(int i = 3; i >= hyoukaPlus.length(); i-- ) {
                    space += " ";
                }
                if(katen9) { //+加点40以上なら網掛け
                    //final int total = Integer.parseInt(nullCheck(applicant._applicantConfRptDetaiDat._hyouka_Plus)) - Integer.parseInt(nullCheck(applicant._applicantConfRptDetaiDat._hyouka_Minus));
                    if(Integer.parseInt(nullCheck(applicant._applicantConfRptDetaiDat._hyouka_Plus)) >= 40) {
                        svf.VrAttributen("POINT11", line, "Paint=(9,10,2),Bold=1");
                        svf.VrsOutn("POINT11", line, space + hyoukaPlus + mark + hyoukaMinus);
                        svf.VrAttributen("POINT11", line, "Paint=(0,0,0),Bold=0");
                    } else {
                        svf.VrsOutn("POINT11", line, space + hyoukaPlus + mark + hyoukaMinus);
                    }
                } else {
                    svf.VrsOutn("POINT11", line, space + hyoukaPlus + mark + hyoukaMinus);
                }
            } else {
                //B方式
                svf.VrsOutn("EXAM_NO1", line, applicant._recom_Examno); //受験番号B
                svf.VrsOutn("EXAM_NO2", line, applicant._examno); //受験番号A
                svf.VrsOutn("TOTAL_DIV1", line, applicant._applicantConfRptDetaiDat._total_Hyoutei); //1,2年評定
                svf.VrsOutn("POINT1", line, point9);//9教科ポイント
                svf.VrsOutn("POINT2", line, printPoint(applicant._applicantConfRptDetaiDat._hyoutei1_2_Plus, applicant._applicantConfRptDetaiDat._hyoutei1_2_Minus)); //２評
                svf.VrsOutn("POINT4", line, applicant._applicantConfRptDetaiDat._dousou_Plus); //同窓
                svf.VrsOutn("POINT5", line, applicant._applicantConfRptDetaiDat._achievement); //業績
                svf.VrsOutn("POINT6", line, printPoint(applicant._interViewDat._interview_Plus, applicant._interViewDat._interview_Minus)); //試験
                svf.VrsOutn("POINT7", line, printPoint(applicant._applicantConfRptDetaiDat._self_Rec_Plus, applicant._applicantConfRptDetaiDat._self_Rec_Minus)); //推薦
                svf.VrsOutn("POINT8", line, point3); //3教
                svf.VrsOutn("TOTAL", line, applicant._applicantConfRptDetaiDat._souten); //総点
                svf.VrsOutn("RANK", line, applicant._applicantConfRptDetaiDat._rank); //序列
            }
            line++; //印字行
        }
        svf.VrEndPage();

    return true;
    }

    private String getFieldSchoolName(final String str) {
        final int keta = KNJ_EditEdit.getMS932ByteLength(str);
        return keta <= 30 ? "1" : "2";
    }

    private String getFieldName(final String str) {
        final int keta = KNJ_EditEdit.getMS932ByteLength(str);
        return keta <= 20 ? "1" : keta <= 30 ? "2" : "3";
    }

    private String nullCheck(final String str) {
        return str != null ? str : "0";
    }

    private String printScore(final String score, final int under) {
        if(score == null) return "";
        if(Integer.parseInt(score) <= under) {
            return "* " + score;
        }
        return score;
    }

    private String printPoint(final String plus, final String minus) {
        if(plus == null && minus == null) {
            return null;
        }
        return String.valueOf(Integer.parseInt(nullCheck(plus)) - Integer.parseInt(nullCheck(minus)));
    }

    // 志願者取得
    private Map getApplicantMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   BASE.ENTEXAMYEAR, ");
            stb.append("   BASE.APPLICANTDIV, ");
            stb.append("   BASE.TESTDIV, ");
            stb.append("   BASE.EXAMNO, ");
            stb.append("   BASE.RECOM_EXAMNO, ");
            stb.append("   BASE.NAME, ");
            stb.append("   SCHOOL.FINSCHOOL_NAME, ");
            stb.append("   BASE2.REMARK1, ");
            stb.append("   STG.ABBV1 "); //特相
            stb.append(" FROM ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT DTL ON DTL.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND DTL.APPLICANTDIV = BASE.APPLICANTDIV AND DTL.EXAMNO = BASE.EXAMNO AND DTL.SEQ = '001' ");
            stb.append(" LEFT JOIN ");
            stb.append("   FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE2 ON BASE2.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BASE2.APPLICANTDIV = BASE.APPLICANTDIV AND BASE2.EXAMNO = BASE.EXAMNO AND BASE2.SEQ = '005' ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_SETTING_MST STG ON STG.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND STG.APPLICANTDIV = BASE.APPLICANTDIV AND STG.SETTING_CD = 'L025' AND STG.SEQ = BASE.JUDGE_KIND ");
            stb.append(" WHERE ");
            stb.append("   BASE.ENTEXAMYEAR = '" + _param._year + "' AND ");
            stb.append("   BASE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("   BASE.TESTDIV = '" + _param._testDiv + "' AND ");
            stb.append("   VALUE(BASE.JUDGEMENT, '') <> '5' ");
            stb.append(" ORDER BY BASE.EXAMNO ");

            log.debug(" applicant sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String entexamyear = rs.getString("ENTEXAMYEAR");
                final String applicantdiv = rs.getString("APPLICANTDIV");
                final String testdiv = rs.getString("TESTDIV");
                final String examno = rs.getString("EXAMNO");
                final String recom_Examno = rs.getString("RECOM_EXAMNO");
                final String name = rs.getString("NAME");
                final String finschool_Name = rs.getString("FINSCHOOL_NAME");
                final String remark1 = rs.getString("REMARK1");
                final String abbv1 = rs.getString("ABBV1");

                final Applicant applicant = new Applicant(entexamyear, applicantdiv, testdiv, examno, recom_Examno,
                        name, finschool_Name, remark1, abbv1,
                        setEntexamApplicantConfRptDat(db2,examno),
                        setEntexamApplicantConfRptDetailDat(db2,examno),
                        setEntexamInterViewDat(db2,examno));

                if(!retMap.containsKey(examno)) {
                    retMap.put(examno, applicant);
                }
            }
        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;
    }

    //調査書評定・出欠取得
    private ApplicantConfRptDat setEntexamApplicantConfRptDat(final DB2UDB db2, final String examno) throws SQLException {
        ApplicantConfRptDat retClass = null;
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   CONF.CONFIDENTIAL_RPT01, "); //教科1
        stb.append("   CONF.CONFIDENTIAL_RPT02, "); //教科2
        stb.append("   CONF.CONFIDENTIAL_RPT03, "); //教科3
        stb.append("   CONF.CONFIDENTIAL_RPT04, "); //教科4
        stb.append("   CONF.CONFIDENTIAL_RPT05, "); //教科5
        stb.append("   CONF.CONFIDENTIAL_RPT06, "); //教科6
        stb.append("   CONF.CONFIDENTIAL_RPT07, "); //教科7
        stb.append("   CONF.CONFIDENTIAL_RPT08, "); //教科8
        stb.append("   CONF.CONFIDENTIAL_RPT09, "); //教科9
        stb.append("   CONF.ABSENCE_DAYS, "); //欠席1
        stb.append("   CONF.ABSENCE_DAYS2, "); //欠席2
        stb.append("   CONF.ABSENCE_DAYS3, "); //欠席3
        stb.append("   CONF.TOTAL_ALL, "); //9教科計
        stb.append("   CONF.TOTAL3 "); //3教科計
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTCONFRPT_DAT CONF ");
        stb.append(" WHERE ");
        stb.append("   CONF.ENTEXAMYEAR = '" + _param._year + "' AND ");
        stb.append("   CONF.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
        stb.append("   CONF.EXAMNO = '" + examno + "' ");

        for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
            retClass = new ApplicantConfRptDat(
                    KnjDbUtils.getString(row, "CONFIDENTIAL_RPT01"),
                    KnjDbUtils.getString(row, "CONFIDENTIAL_RPT02"),
                    KnjDbUtils.getString(row, "CONFIDENTIAL_RPT03"),
                    KnjDbUtils.getString(row, "CONFIDENTIAL_RPT04"),
                    KnjDbUtils.getString(row, "CONFIDENTIAL_RPT05"),
                    KnjDbUtils.getString(row, "CONFIDENTIAL_RPT06"),
                    KnjDbUtils.getString(row, "CONFIDENTIAL_RPT07"),
                    KnjDbUtils.getString(row, "CONFIDENTIAL_RPT08"),
                    KnjDbUtils.getString(row, "CONFIDENTIAL_RPT09"),
                    KnjDbUtils.getString(row, "ABSENCE_DAYS"),
                    KnjDbUtils.getString(row, "ABSENCE_DAYS2"),
                    KnjDbUtils.getString(row, "ABSENCE_DAYS3"),
                    KnjDbUtils.getString(row, "TOTAL_ALL"),
                    KnjDbUtils.getString(row, "TOTAL3")
                );
        }

        return retClass;
    }

    private class ApplicantConfRptDat {
        final String _confidential_rpt01;
        final String _confidential_rpt02;
        final String _confidential_rpt03;
        final String _confidential_rpt04;
        final String _confidential_rpt05;
        final String _confidential_rpt06;
        final String _confidential_rpt07;
        final String _confidential_rpt08;
        final String _confidential_rpt09;
        final String _absenceDays;
        final String _absenceDays2;
        final String _absenceDays3;
        final String _totalAll;
        final String _total3;
        public ApplicantConfRptDat(final String confidential_rpt01, final String confidential_rpt02, final String confidential_rpt03,
                final String confidential_rpt04, final String confidential_rpt05, final String confidential_rpt06,
                final String confidential_rpt07, final String confidential_rpt08, final String confidential_rpt09,
                final String absenceDays, final String absenceDays2, final String absenceDays3, final String totalAll, final String total3) {
            _confidential_rpt01 = confidential_rpt01;
            _confidential_rpt02 = confidential_rpt02;
            _confidential_rpt03 = confidential_rpt03;
            _confidential_rpt04 = confidential_rpt04;
            _confidential_rpt05 = confidential_rpt05;
            _confidential_rpt06 = confidential_rpt06;
            _confidential_rpt07 = confidential_rpt07;
            _confidential_rpt08 = confidential_rpt08;
            _confidential_rpt09 = confidential_rpt09;
            _absenceDays = absenceDays;
            _absenceDays2 = absenceDays2;
            _absenceDays3 = absenceDays3;
            _totalAll = totalAll;
            _total3 = total3;
        }
    }

    //表示区分「その他」取得
    private ApplicantConfRptDetailDat setEntexamApplicantConfRptDetailDat(final DB2UDB db2, final String examno) throws SQLException {
        ApplicantConfRptDetailDat retClass = null;

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   DTL1.REMARK1 AS TOTAL_HYOUTEI, "); //1,2年評定
        stb.append("   DTL1.REMARK2 AS ACHIEVEMENT, "); //業績
        stb.append("   DTL1.REMARK3 AS REPORT_PLUS, "); //調査+
        stb.append("   DTL1.REMARK4 AS REPORT_MINUS, "); //調査-
        stb.append("   DTL1.REMARK5 AS SELF_REC_PLUS, "); //推薦+
        stb.append("   DTL1.REMARK6 AS SELF_REC_MINUS, "); //推薦-
        stb.append("   DTL1.REMARK7 AS DOUSOU_PLUS, "); //同窓
        stb.append("   DTL1.REMARK8 AS QUALIFIED_ENG, "); //英検CD
        stb.append("   DTL1.REMARK9 AS TYOUSEI_PLUS, "); //調整+
        stb.append("   DTL1.REMARK10 AS TYOUSEI_MINUS, "); //調整-
        stb.append("   DTL2.REMARK1 AS TOTAL9_PLUS, "); //9教科+
        stb.append("   DTL2.REMARK2 AS TOTAL9_MINUS, "); //9教科-
        stb.append("   DTL2.REMARK3 AS TOTAL3_PLUS, "); //3教科+
        stb.append("   DTL2.REMARK4 AS TOTAL3_MINUS, "); //3教科-
        stb.append("   DTL2.REMARK5 AS ABSENCE_TOTAL_PLUS, "); //出欠+
        stb.append("   DTL2.REMARK6 AS ABSENCE_TOTAL_MINUS, "); //出欠-
        stb.append("   DTL2.REMARK7 AS HYOUTEI1_2_PLUS, "); //1,2年評定+
        stb.append("   DTL2.REMARK8 AS HYOUTEI1_2_MINUS, "); //1,2年評定-
        stb.append("   DTL3.REMARK1 AS HYOUKA_PLUS, "); //評価+
        stb.append("   DTL3.REMARK2 AS HYOUKA_MINUS, "); //評価-
        stb.append("   DTL3.REMARK3 AS SOUTEN, "); //総点
        stb.append("   DTL3.REMARK4 AS RANK "); //序列
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT DTL1 ON DTL1.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND DTL1.APPLICANTDIV = BASE.APPLICANTDIV AND DTL1.EXAMNO = BASE.EXAMNO AND DTL1.SEQ = '001' ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT DTL2 ON DTL2.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND DTL2.APPLICANTDIV = BASE.APPLICANTDIV AND DTL2.EXAMNO = BASE.EXAMNO AND DTL2.SEQ = '002' ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT DTL3 ON DTL3.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND DTL3.APPLICANTDIV = BASE.APPLICANTDIV AND DTL3.EXAMNO = BASE.EXAMNO AND DTL3.SEQ = '003' ");
        stb.append(" WHERE ");
        stb.append("   BASE.ENTEXAMYEAR = '" + _param._year + "' AND ");
        stb.append("   BASE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
        stb.append("   BASE.TESTDIV = '" + _param._testDiv + "' AND ");
        stb.append("   BASE.EXAMNO = '" + examno + "' ");

        for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
            retClass = new ApplicantConfRptDetailDat(
                    KnjDbUtils.getString(row, "TOTAL_HYOUTEI"),
                    KnjDbUtils.getString(row, "ACHIEVEMENT"),
                    KnjDbUtils.getString(row, "REPORT_PLUS"),
                    KnjDbUtils.getString(row, "REPORT_MINUS"),
                    KnjDbUtils.getString(row, "SELF_REC_PLUS"),
                    KnjDbUtils.getString(row, "SELF_REC_MINUS"),
                    KnjDbUtils.getString(row, "DOUSOU_PLUS"),
                    KnjDbUtils.getString(row, "QUALIFIED_ENG"),
                    KnjDbUtils.getString(row, "TYOUSEI_PLUS"),
                    KnjDbUtils.getString(row, "TYOUSEI_MINUS"),
                    KnjDbUtils.getString(row, "TOTAL9_PLUS"),
                    KnjDbUtils.getString(row, "TOTAL9_MINUS"),
                    KnjDbUtils.getString(row, "TOTAL3_PLUS"),
                    KnjDbUtils.getString(row, "TOTAL3_MINUS"),
                    KnjDbUtils.getString(row, "ABSENCE_TOTAL_PLUS"),
                    KnjDbUtils.getString(row, "ABSENCE_TOTAL_MINUS"),
                    KnjDbUtils.getString(row, "HYOUTEI1_2_PLUS"),
                    KnjDbUtils.getString(row, "HYOUTEI1_2_MINUS"),
                    KnjDbUtils.getString(row, "HYOUKA_PLUS"),
                    KnjDbUtils.getString(row, "HYOUKA_MINUS"),
                    KnjDbUtils.getString(row, "SOUTEN"),
                    KnjDbUtils.getString(row, "RANK")
                );
        }

        return retClass;

    }

    private class ApplicantConfRptDetailDat {
        final String _total_Hyoutei;
        final String _achievement;
        final String _report_Plus;
        final String _report_Minus;
        final String _self_Rec_Plus;
        final String _self_Rec_Minus;
        final String _dousou_Plus;
        final String _qualified_Eng;
        final String _tyousei_Plus;
        final String _tyousei_Minus;
        final String _total9_Plus;
        final String _total9_Minus;
        final String _total3_Plus;
        final String _total3_Minus;
        final String _absence_Total_Plus;
        final String _absence_Total_Minus;
        final String _hyoutei1_2_Plus;
        final String _hyoutei1_2_Minus;
        final String _hyouka_Plus;
        final String _hyouka_Minus;
        final String _souten;
        final String _rank;

        public ApplicantConfRptDetailDat(final String total_Hyoutei, final String achievement, final String report_Plus,
                final String report_Minus, final String self_Rec_Plus, final String self_Rec_Minus,
                final String dousou_Plus, final String qualified_Eng, final String tyousei_Plus,
                final String tyousei_Minus, final String total9_Plus,final String total9_Minus, final String total3_Plus, final String total3_Minus,
                final String absence_Total_Plus, final String absence_Total_Minus, final String hyoutei1_2_Plus,
                final String hyoutei1_2_Minus, final String hyouka_Plus, final String hyouka_Minus, final String souten, final String rank) {
            _total_Hyoutei = total_Hyoutei;
            _achievement = achievement;
            _report_Plus = report_Plus;
            _report_Minus = report_Minus;
            _self_Rec_Plus = self_Rec_Plus;
            _self_Rec_Minus = self_Rec_Minus;
            _dousou_Plus = dousou_Plus;
            _qualified_Eng = qualified_Eng;
            _tyousei_Plus = tyousei_Plus;
            _tyousei_Minus = tyousei_Minus;
            _total9_Plus = total9_Plus;
            _total9_Minus = total9_Minus;
            _total3_Plus = total3_Plus;
            _total3_Minus = total3_Minus;
            _absence_Total_Plus = absence_Total_Plus;
            _absence_Total_Minus = absence_Total_Minus;
            _hyoutei1_2_Plus = hyoutei1_2_Plus;
            _hyoutei1_2_Minus = hyoutei1_2_Minus;
            _hyouka_Plus = hyouka_Plus;
            _hyouka_Minus = hyouka_Minus;
            _souten = souten;
            _rank = rank;
        }
    }

  //試験（面接）取得
    private InterViewDat setEntexamInterViewDat(final DB2UDB db2, final String examno) throws SQLException {
        InterViewDat retClass = null;

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.SCORE1 AS INTERVIEW_PLUS, "); //スコア+
        stb.append("   T1.SCORE2 AS INTERVIEW_MINUS "); //スコア-
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_INTERVIEW_DAT T1 ON T1.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND T1.APPLICANTDIV = BASE.APPLICANTDIV AND T1.EXAMNO = BASE.EXAMNO AND T1.TESTDIV = BASE.TESTDIV ");
        stb.append(" WHERE ");
        stb.append("   BASE.ENTEXAMYEAR = '" + _param._year + "' AND ");
        stb.append("   BASE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
        stb.append("   BASE.TESTDIV = '" + _param._testDiv + "' AND ");
        stb.append("   BASE.EXAMNO = '" + examno + "' ");

        for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
            retClass = new InterViewDat(
                    KnjDbUtils.getString(row, "INTERVIEW_PLUS"),
                    KnjDbUtils.getString(row, "INTERVIEW_MINUS")
                );
        }

        return retClass;

    }

    private class InterViewDat {
        final String _interview_Plus;
        final String _interview_Minus;

        public InterViewDat(final String interview_Plus, final String interview_Minus) {
            _interview_Plus = interview_Plus;
            _interview_Minus = interview_Minus;
        }
    }

    private class Applicant {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _examno;
        final String _recom_Examno;
        final String _name;
        final String _finschool_Name;
        final String _remark1;
        final String _abbv1;
        final ApplicantConfRptDat _applicantConfRptDat;
        final ApplicantConfRptDetailDat _applicantConfRptDetaiDat;
        final InterViewDat _interViewDat;

        public Applicant(final String entexamyear, final String applicantdiv, final String testdiv, final String examno,
                final String recom_Examno, final String name, final String finschool_Name,
                final String remark1, final String abbv1, final ApplicantConfRptDat applicantConfRptDat,
                final ApplicantConfRptDetailDat applicantConfRptDetaiDat, final InterViewDat interViewDat ) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _examno = examno;
            _recom_Examno = recom_Examno;
            _name = name;
            _finschool_Name = finschool_Name;
            _remark1 = remark1;
            _abbv1 = abbv1;
            _applicantConfRptDat = applicantConfRptDat;
            _applicantConfRptDetaiDat = applicantConfRptDetaiDat;
            _interViewDat = interViewDat;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 76037 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _applicantDiv; //入試制度
        final String _testDiv; //入試区分
        final String _testAbbv;
        final String _date;
        final String _time;
        final String _schoolKind;
        final String _schoolName;
        final String _katen; //第一志望時の加点
        final Map<String,String> _eikenMap; //英検名称

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _schoolKind = request.getParameter("SCHOOLKIND");
              _date = request.getParameter("CTRL_DATE");
              _time = request.getParameter("TIME");
              _testAbbv = getTestDivAbbv(db2);
              _schoolName = getSchoolName(db2);
              _katen = getKaten(db2);
              _eikenMap = getEikenMap(db2);

        }

        private String getTestDivAbbv(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTDIV_ABBV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _year + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' "));
        }

        private String getSchoolName(final DB2UDB db2) {
            final String kindcd = "1".equals(_applicantDiv) ? "105" : "106";
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + kindcd + "' "));
        }

        private String getKaten(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM ENTEXAM_SETTING_MST WHERE ENTEXAMYEAR = '" + _year + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND SETTING_CD = 'L014' AND SEQ = '01' "));
        }

        private Map<String,String> getEikenMap(final DB2UDB db2) {
            final Map<String,String> retMap = new LinkedMap();

            for (final Map<String, String> row : KnjDbUtils.query(db2, "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'H305' ")) {

                final String cd = KnjDbUtils.getString(row, "NAMECD2");

                if(!retMap.containsKey(cd)) {
                    retMap.put(cd, KnjDbUtils.getString(row, "NAME1"));
                }
            }
            return retMap;
        }
    }

    public static String h_format_Seireki_MD(final String date) {
        if (null == date) {
            return date;
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        String retVal = "";
        sdf.applyPattern("yyyy年M月d日");
        retVal = sdf.format(java.sql.Date.valueOf(date));

        return retVal;
    }
}

// eof
