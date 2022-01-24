/*
 * $Id: f34101301a1728a768d85d79d4ae06be51a6a2a7 $
 *
 * 作成日: 2019/12/23
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJL627F {

    private static final Log log = LogFactory.getLog(KNJL627F.class);

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
        String useForm = "";
        String[] nendo = KNJ_EditDate.tate_format4(db2,_param._entexamyear + "-12-31");
//        String[] date = KNJ_EditDate.tate_format4(db2,KNJ_EditDate.H_Format_Haifun(_param._printDate));
//        String date2 = KNJ_EditDate.h_format_JP(db2,_param._printDate);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String finSchoolName = rs.getString("FINSCHOOL_NAME");
                final String shogaku = StringUtils.defaultString(rs.getString("SHOGAKU"));
                final String shogakuName = StringUtils.defaultString(rs.getString("SHOGAKU_NAME"));
                final String courseName = StringUtils.defaultString(rs.getString("COURSENAME"));
                final String majorName = StringUtils.defaultString(rs.getString("MAJORNAME"));
                final String sucCourse = StringUtils.defaultString(rs.getString("SUC_COURSE"));
                final String course1 = StringUtils.defaultString(rs.getString("COURSE1"));

                if ("4".equals(_param._printDiv)) {
                    Map outWk = (Map)_param._entMoneyMap.get(shogaku);
                    if (null == outWk) {
                        continue;
                    }
                }

                if("1".equals(_param._printDiv)) { //出力帳票
                    useForm = "KNJL627F_1.frm"; //合格通知書
                } else if("2".equals(_param._printDiv)) {
                    useForm = "KNJL627F_2.frm"; //不合格通知
                } else if("3".equals(_param._printDiv)) {
                    useForm = "KNJL627F_3.frm"; //
                } else if("4".equals(_param._printDiv)) {
                    useForm = "KNJL627F_4.frm"; //合格通知書
                } else {
                    continue;
                }
                svf.VrSetForm(useForm, 1);

                //共通
                svf.VrsOut("EXAM_NO", receptno); //受験番号
                svf.VrsOut("NAME", name); //氏名

                final String prtDate = KNJ_EditDate.h_format_JP(db2, _param._printDate.replace('/', '-'));
                svf.VrsOut("DATE", prtDate);  //日付

                svf.VrsOut("SCHOOL_NAME", trimSpace((String)_param._certifSchoolMap.get("SCHOOL_NAME")));  //学校名(証明書系の学校名)
                svf.VrsOut("JOBNAME", trimSpace((String)_param._certifSchoolMap.get("JOB_NAME")));
                svf.VrsOut("STAFFNAME", (String)_param._certifSchoolMap.get("PRINCIPAL_NAME"));  //校長名

                svf.VrsOut("SCHOOL_NAME2", trimSpace((String)_param._certifSchoolMap.get("SCHOOL_NAME")));  //学校名(証明書系の学校名)
                svf.VrsOut("JOBNAME2", trimSpace((String)_param._certifSchoolMap.get("JOB_NAME")));
                svf.VrsOut("STAFFNAME2", (String)_param._certifSchoolMap.get("PRINCIPAL_NAME"));  //校長名

                svf.VrsOut("FINSCHOOL_NAME", finSchoolName);  //出身中学?小学?
                svf.VrsOut("KIND2", "入学");

                if ("1".equals(_param._printDiv)) {
                    final String NENDO = nendo[0] + nendo[1] + "年度";
                    final String SCHOOL_NAME = trimSpace((String)_param._certifSchoolMap.get("SCHOOL_NAME"));
                    svf.VrsOutn("TEXT", 1, "　" + NENDO + SCHOOL_NAME + "入学試験" + StringUtils.defaultString(_param._testDivName2) + "の結果を下記の通り");
                    svf.VrsOutn("TEXT", 2, "通知します。");
                    svf.VrsOut("PASS_COURSE", sucCourse);  //合格コース名
                } else if ("2".equals(_param._printDiv)) {
                    svf.VrsOut("NENDO", nendo[0] + nendo[1] + "年度"); //年度(文章中の年度)
                    svf.VrsOut("JUDGE", "不合格");  //「不合格」固定
                    svf.VrsOut("PASS_COURSE", course1);  //合格コース名
//                    svf.VrsOut("COURSE", course1);  //不合格コース名
                    //svf.VrsOut("TEXT1", );
                    //svf.VrsOut("COMMENT", );
                    //svf.VrsOut("NOTICE", );
                } else if ("3".equals(_param._printDiv)) {
                    final String NENDO = nendo[0] + nendo[1] + "年度";
                    final String SCHOOL_NAME = trimSpace((String)_param._certifSchoolMap.get("SCHOOL_NAME"));
                    svf.VrsOutn("TEXT", 1, "　" + NENDO + SCHOOL_NAME + "入学試験" + StringUtils.defaultString(_param._testDivName2) + "の結果を下記の通り");
                    svf.VrsOutn("TEXT", 2, "通知します。");
//                    svf.VrsOut("NENDO", nendo[0] + nendo[1] + "年度入学試験の結果、次の科・コースに合格しました。"); //年度(文章)
                    svf.VrsOut("PASS_COURSE", sucCourse);  //教育課程+学科 合格コース

//                    final String cutWk = "あなたは、" + course1 + "を第１志望されましたが、入学試験の結果" + sucCourse + "に合格しました。";
//                    final String[] cutArry = KNJ_EditEdit.get_token(cutWk, 46, 3);
//                    for (int cnt = 0;cnt < 3;cnt++) {
//                        if (cutArry[cnt] != null) {
//                            svf.VrsOut("REMARK"+(cnt+1), cutArry[cnt]);
//                        }
//                    }
//                    svf.VrsOut("FSTCOURSENAME", course1);  //第一志望コース
                } else if("4".equals(_param._printDiv)) {
                    svf.VrsOut("JUDGEDIV", shogakuName);  //奨学区分名称
                    Map outWk = (Map)_param._entMoneyMap.get(shogaku);
                    final String kindName = StringUtils.defaultString((String) outWk.get("ENT_KIND_NAME")) + "奨学生";
                    final String act = StringUtils.defaultString((String) outWk.get("ENT_ACT"), "");
                    svf.VrsOut("TITLE", kindName + "の決定について（通知）");  //タイトル
                    final List<String> sen = Arrays.asList(
                            "　この度は、本校入学試験合格おめでとうございました。"
                          , "　さて、あなたは選考の結果、" + kindName +  "に選抜されましたので通知いたします。"
                          , "　" + kindName + "としての入学手続時の納入金及び入学後毎月の納入金は、下記のとおりとなります。"
                          , "　本校では、あなたのご入学を心よりお待ち申し上げますとともに、入学後、" + kindName + "として" + act + "に励まれ、他の生徒の模範となられますことを期待しております。"
                            );

                    int n = 0;
                    for (final Iterator it = KNJ_EditKinsoku.getTokenList(mkString(sen, "\n").toString(), 39 * 2).iterator(); it.hasNext();) {
                        svf.VrsOutn("TEXT", ++n, (String) it.next());  //本文
                    }

                    final String emStr = "  " + (String)outWk.get("ENT_MONEY");  //"・入学金      →  " + (String)outWk.get("ENT_MONEY");
                    svf.VrsOut("ENT_MONEY", emStr);  //入学金
                    final String efStr = "  " + (String)outWk.get("ENT_FACILITY");  // "・教育会入会金      →  " + (String)outWk.get("ENT_FACILITY");
                    svf.VrsOut("ENT_FACILITY", efStr);  //教育会入学金
                    final String etStr = "  " + (String)outWk.get("EM_TUTITION_DEFAULT");  // "・" + (String)outWk.get("EM_TUTITION_DEFAULT");
                    svf.VrsOut("EM_TUTITION_DEFAULT", etStr);  //奨学金
                }

                svf.VrEndPage();
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private static StringBuffer mkString(final List<String> list, final String comma) {
        final StringBuffer stb = new StringBuffer();
        String c = "";
        for (final String line : list) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            stb.append(c).append(line);
            c = comma;
        }
        return stb;
    }

    //文字列の左右にある空白を削除(trim関数)※全角文字列も除去
    private String trimSpace(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        int st = 0;
        int len = str.length();
        char[] val = str.toCharArray();
        while ((st < len) && ((val[st] <= '\u0020') || (val[st] == '\u00A0') || (val[st] == '\u3000'))) {
            st++;
        }
        while ((st < len) && ((val[len - 1] <= '\u0020') || (val[len - 1] == '\u00A0') || (val[len - 1] == '\u3000'))) {
            len--;
        }
        return ((st > 0) || (len < str.length())) ? str.substring(st, len) : str;
    }

    private static int getMS932Bytecount(String str) {
        return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RECEPT.RECEPTNO, ");
        stb.append("     BASEDAT.NAME, ");
        stb.append("     BASEDAT.FS_CD, ");
        stb.append("     FM.FINSCHOOL_NAME, ");
        stb.append("     RECEPT.JUDGEDIV, ");
        stb.append("     BDETAIL005.REMARK2 AS SHOGAKU, ");
        stb.append("     L025.NAME1 AS SHOGAKU_NAME, ");
        stb.append("     value(VCM.COURSECD, '') AS COURSECD, ");
        stb.append("     value(VCM.COURSENAME, '') AS COURSENAME, ");
        stb.append("     value(VCM.MAJORCD, '') AS MAJORCD, ");
        stb.append("     value(VCM.MAJORNAME, '') AS MAJORNAME, ");
        stb.append("     value(RDETAIL001.REMARK3, '') AS SUC_COURSECD, ");
        stb.append("     value(ECM1.EXAMCOURSE_NAME, '') AS SUC_COURSE, ");
        stb.append("     value(BDETAIL001.REMARK10, '') AS COURSE1CD, ");
        stb.append("     value(ECM2.EXAMCOURSE_NAME, '') AS COURSE1 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASEDAT ");  //氏名、出身校等
        stb.append("       ON BASEDAT.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("      AND BASEDAT.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("      AND BASEDAT.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST L013 ");
        stb.append("       ON L013.NAMECD2 = RECEPT.JUDGEDIV ");
        stb.append("      AND L013.NAMECD1 = 'L013' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL005 ");  //奨学生(DETDAT_SEQ005.REMARK2)
        stb.append("       ON BDETAIL005.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("      AND BDETAIL005.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("      AND BDETAIL005.EXAMNO = RECEPT.EXAMNO ");
        stb.append("      AND BDETAIL005.SEQ = '005' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RDETAIL001 ");
        stb.append("       ON RDETAIL001.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("      AND RDETAIL001.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("      AND RDETAIL001.TESTDIV = RECEPT.TESTDIV ");
        stb.append("      AND RDETAIL001.RECEPTNO = RECEPT.RECEPTNO ");
        stb.append("      AND RDETAIL001.SEQ = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL001 ");  //第一志望コース(DETDAT_SEQ001.REMARK8～10)
        stb.append("       ON BDETAIL001.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("      AND BDETAIL001.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("      AND BDETAIL001.EXAMNO = RECEPT.EXAMNO ");
        stb.append("      AND BDETAIL001.SEQ = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECM1 ");  //合格コース名称
        stb.append("       ON ECM1.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("      AND ECM1.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("      AND ECM1.TESTDIV = '1' ");
        stb.append("      AND ECM1.COURSECD = RDETAIL001.REMARK1 ");
        stb.append("      AND ECM1.MAJORCD = RDETAIL001.REMARK2 ");
        stb.append("      AND ECM1.EXAMCOURSECD = RDETAIL001.REMARK3 ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECM2 ");  //第一志望コース名称
        stb.append("       ON ECM2.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("      AND ECM2.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("      AND ECM2.TESTDIV = '1' ");
        stb.append("      AND ECM2.COURSECD = BDETAIL001.REMARK8 ");
        stb.append("      AND ECM2.MAJORCD = BDETAIL001.REMARK9 ");
        stb.append("      AND ECM2.EXAMCOURSECD = BDETAIL001.REMARK10 ");
        stb.append("     LEFT JOIN NAME_MST L025 ");  //奨学生名称
        stb.append("       ON L025.NAMECD1 = 'L025' ");
        stb.append("      AND L025.NAMECD2 = BDETAIL005.REMARK2 ");
        stb.append("      AND L025.NAMESPARE1 = '" + _param._applicantDiv + "' ");
        stb.append("     LEFT JOIN V_COURSE_MAJOR_MST VCM "); //教育課程学科名称
        stb.append("       ON VCM.YEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("      AND VCM.COURSECD = RDETAIL001.REMARK1 ");
        stb.append("      AND VCM.MAJORCD = RDETAIL001.REMARK2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FM "); //出身学校
        stb.append("       ON FM.FINSCHOOLCD = BASEDAT.FS_CD ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
        if ("1".equals(_param._printDiv) && "3".equals(_param._passDiv)) {
            if(!"".equals(_param._passReceptno)) {
                stb.append("           AND RECEPT.RECEPTNO >= '" + _param._passReceptno + "' ");
            }
            if(!"".equals(_param._passReceptnoTo)) {
                stb.append("           AND RECEPT.RECEPTNO <= '" + _param._passReceptnoTo + "' ");
            }
        }
        if ("2".equals(_param._printDiv) && "3".equals(_param._unpassDiv)) {
            if(!"".equals(_param._unpassReceptno)) {
                stb.append("           AND RECEPT.RECEPTNO >= '" + _param._unpassReceptno + "' ");
            }
            if(!"".equals(_param._unpassReceptnoTo)) {
                stb.append("           AND RECEPT.RECEPTNO <= '" + _param._unpassReceptnoTo + "' ");
            }
        }
        if ("3".equals(_param._printDiv) && "2".equals(_param._slidePassDiv)) {
            if(!"".equals(_param._slidePassReceptno)) {
                stb.append("           AND RECEPT.RECEPTNO >= '" + _param._slidePassReceptno + "' ");
            }
            if(!"".equals(_param._slidePassReceptnoTo)) {
                stb.append("           AND RECEPT.RECEPTNO <= '" + _param._slidePassReceptnoTo + "' ");
            }
        }
        if ("4".equals(_param._printDiv) && "2".equals(_param._scholarshipPassDiv)) {
            if(!"".equals(_param._scholarshipPassReceptno)) {
                stb.append("           AND RECEPT.RECEPTNO >= '" + _param._scholarshipPassReceptno + "' ");
            }
            if(!"".equals(_param._scholarshipPassReceptnoTo)) {
                stb.append("           AND RECEPT.RECEPTNO <= '" + _param._scholarshipPassReceptnoTo + "' ");
            }
        }
        if ("1".equals(_param._printDiv) && !"2".equals(_param._passDiv)) {
            stb.append("          AND L013.NAMESPARE1 = '1' ");
            stb.append("          AND RECEPT.JUDGEDIV = '1' ");
        }
        if ("2".equals(_param._printDiv) && !"2".equals(_param._unpassDiv)) {
            stb.append("          AND ( L013.NAMESPARE1 <> '1' OR L013.NAMESPARE1 IS NULL ) ");
        }
        if ("3".equals(_param._printDiv)) {
            stb.append("          AND L013.NAMESPARE1 = '1' ");
            stb.append("          AND RECEPT.JUDGEDIV = '3' ");
        }
        if ("4".equals(_param._printDiv)) {
            stb.append("          AND L013.NAMESPARE1 = '1' ");
            stb.append("          AND L025.NAMECD2 IS NOT NULL ");
        }
        stb.append(" ORDER BY ");
        stb.append("     VALUE(RECEPT.RECEPTNO, 0) ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72385 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _printDate;
        private final String _printDiv;
        private final String _passDiv;
        private final String _passReceptno;
        private final String _passReceptnoTo;
        private final String _unpassDiv;
        private final String _unpassReceptno;
        private final String _unpassReceptnoTo;
        private final String _slidePassDiv;
        private final String _slidePassReceptno;
        private final String _slidePassReceptnoTo;
        private final String _scholarshipPassDiv;
        private final String _scholarshipPassReceptno;
        private final String _scholarshipPassReceptnoTo;
        private final String _documentRoot;
        private final String _schoolStampPath;
        private final String _imagePath;

        private final Map _certifSchoolMap;
        private final String _testDivName2;

        private final Map _entMoneyMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _printDate = request.getParameter("PRINT_DATE");
            _printDiv = request.getParameter("PRINT_DIV");
            _passDiv = request.getParameter("PASS_DIV");
            _passReceptno = StringUtils.defaultString(request.getParameter("PASS_EXAMNO"));
            _passReceptnoTo = StringUtils.defaultString(request.getParameter("PASS_EXAMNO_TO"));
            _unpassDiv = request.getParameter("UNPASS_DIV");
            _unpassReceptno = StringUtils.defaultString(request.getParameter("UNPASS_EXAMNO"));
            _unpassReceptnoTo = StringUtils.defaultString(request.getParameter("UNPASS_EXAMNO_TO"));
            _slidePassDiv = request.getParameter("SLIDEPASS_DIV");
            _slidePassReceptno = StringUtils.defaultString(request.getParameter("SLIDEPASS_EXAMNO"));
            _slidePassReceptnoTo = StringUtils.defaultString(request.getParameter("SLIDEPASS_EXAMNO_TO"));
            _scholarshipPassDiv = request.getParameter("SCHOLARSHIPPASS_DIV");
            _scholarshipPassReceptno = StringUtils.defaultString(request.getParameter("SCHOLARSHIPPASS_EXAMNO"));
            _scholarshipPassReceptnoTo = StringUtils.defaultString(request.getParameter("SCHOLARSHIPPASS_EXAMNO_TO"));

            _imagePath = loadControlMst(db2);
            _documentRoot = StringUtils.defaultString(request.getParameter("DOCUMENTROOT"));
            final String _jorh = "1".equals(_applicantDiv) ? "J" : "H";
            _schoolStampPath = checkFilePath(_documentRoot + "/" + _imagePath + "/SCHOOLSTAMP_" + _jorh + ".bmp");

            _certifSchoolMap = getCertifScholl(db2);
            _testDivName2 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT '（' || NAME2 || '）' FROM V_NAME_MST WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = '" + ("1".equals(_applicantDiv) ? "L024" : "L004") + "' AND NAMECD2 = '" + _testDiv + "' "));
            _entMoneyMap = setEntMoneyInfo(db2);
        }
        private Map setEntMoneyInfo(final DB2UDB db2) {
            Map retMap = new LinkedMap();
            if ("1".equals(_applicantDiv)) {
                retMap.put("01", setEMoneyAddWork("入試特別",       "学業やスポーツ等の活動", "全額免除", "適用なし", "3年間の授業料相当額を給付"));
                retMap.put("02", setEMoneyAddWork("入試特別",       "学業やスポーツ等の活動", "全額免除", "適用なし", "（32万円）支給"));
                retMap.put("03", setEMoneyAddWork("入試特別",       "学業やスポーツ等の活動", "全額免除", "適用なし", ""));
                retMap.put("04", setEMoneyAddWork("入試特別",       "学業やスポーツ等の活動", "全額免除", "適用なし", "（32万円）支給"));
                retMap.put("05", setEMoneyAddWork("入試特別",       "学業やスポーツ等の活動", "全額免除", "適用なし", ""));
            } else {
                retMap.put("11", setEMoneyAddWork("入試成績特別",   "学業",                   "全額免除", "適用なし", "（32万円）支給"));
                retMap.put("12", setEMoneyAddWork("入試成績特別",   "学業",                   "全額免除", "適用なし", "（32万円）支給"));
                retMap.put("13", setEMoneyAddWork("入試成績特別",   "学業",                   "全額免除", "適用なし", "（32万円）支給"));
                retMap.put("21", setEMoneyAddWork("入学時特別活動", "学業やスポーツ等の活動", "全額免除", "適用なし", "（32万円）支給"));
                retMap.put("22", setEMoneyAddWork("入学時特別活動", "学業やスポーツ等の活動", "全額免除", "適用なし", "（20万円）支給"));
                retMap.put("23", setEMoneyAddWork("入学時特別活動", "学業やスポーツ等の活動", "全額免除", "適用なし", "適用なし"));
                retMap.put("24", setEMoneyAddWork("入学時特別活動", "学業やスポーツ等の活動", "半額免除", "適用なし", "適用なし"));
                retMap.put("25", setEMoneyAddWork("入試特別",       "学業やスポーツ等の活動", "全額免除", "適用なし", ""));
            }
            return retMap;
        }
        private Map setEMoneyAddWork(final String word1, final String word2, final String word3, final String word4, final String word5) {
            Map setMp = new LinkedMap();
            setMp.put("ENT_KIND_NAME", word1);
            setMp.put("ENT_ACT", word2);
            setMp.put("ENT_MONEY", word3);
            setMp.put("ENT_FACILITY", word4);
            setMp.put("EM_TUTITION_DEFAULT", word5);
            return setMp;
        }

        private Map getCertifScholl(final DB2UDB db2) {
            final Map rtnMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String cdStr = "1".equals(_applicantDiv) ? "105" : "106";
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '" + cdStr + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnMap.put("CORP_NAME", rs.getString("REMARK6"));
                    rtnMap.put("SCHOOL_NAME", rs.getString("SCHOOL_NAME"));
                    rtnMap.put("JOB_NAME", rs.getString("JOB_NAME"));
                    rtnMap.put("PRINCIPAL_NAME", rs.getString("PRINCIPAL_NAME"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtnMap;
        }
        private String loadControlMst(final DB2UDB db2) {
            String retStr = "";
            final String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("IMAGEPATH");
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }
        private String checkFilePath(final String path) {
            final boolean exists = new File(path).exists();
            if (!exists) {
                log.info("file not found:" + path);
                return null;
            }
            log.info("exists:" + path);
            return path;
        }


    }
}

// eof

