/*
 * $Id: e533adfcdad76d1fba772fa3523c9d89b453bb70 $
 *
 * 作成日: 2017/10/26
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL326A {

    private static final Log log = LogFactory.getLog(KNJL326A.class);

    private boolean _hasData;
    private final String PRINT_PASS = "1";
    private final String PRINT_UNPASS = "2";
    private final String PRINT_NKYOKA = "3";
    private final String PRINT_TOKUTAI = "4";
    private final String PRINT_KUIKIGAI = "5";

    private final String JUDGE_PASS = "1";
    private final String JUDGE_UNPASS = "0";

    private final String SUISEN = "1";
    private final String SENBATSU = "2";
    private final String NAIBU = "9";

    private final String SITEI = "2";

    private final String SCHOOLKIND_J = "J";
    private final String SCHOOLKIND_H = "H";

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

            if (PRINT_PASS.equals(_param._noticeType)) {
                printPass(db2, svf);
            }
            if (PRINT_UNPASS.equals(_param._noticeType)) {
                printUnPass(db2, svf);
            }
            if (PRINT_NKYOKA.equals(_param._noticeType)) {
                printNKyoka(db2, svf);
            }
            if (PRINT_TOKUTAI.equals(_param._noticeType) && SCHOOLKIND_J.equals(_param._schoolkind)) {
                printTokutaiJ(db2, svf);
            }
            if (PRINT_TOKUTAI.equals(_param._noticeType) && SCHOOLKIND_H.equals(_param._schoolkind)) {
                printTokutaiH(db2, svf);
            }
            if (PRINT_KUIKIGAI.equals(_param._noticeType)) {
                printKuikigai(db2, svf);
            }
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

    private void printPass(final DB2UDB db2, final Vrw32alp svf) {
        String frmId = null;
        if (SCHOOLKIND_J.equals(_param._schoolkind)) {
            frmId = "KNJL326A_1J.frm"; //1:合格通知
        } else if (SCHOOLKIND_H.equals(_param._schoolkind)) {
            frmId = "KNJL326A_1H.frm"; //1:選考結果通知書（合格）
        }
        svf.VrSetForm(frmId, 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlPass();
            log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxLine = 1;
            int lineCnt = 1;
            while (rs.next()) {
                if (lineCnt > maxLine) {
                    svf.VrEndPage();
                    lineCnt = 1;
                }

                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String passCourseName1 = rs.getString("PASS_COURSE_NAME1");
                final String passCourseName2 = rs.getString("PASS_COURSE_NAME2");
                final String shDiv = rs.getString("SHDIV");

                svf.VrsOut("EXAM_NO", receptno);
                final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(name) > 20 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, name);
                if (SCHOOLKIND_J.equals(_param._schoolkind)) {
                    final String printPassCourseName = ("1".equals(shDiv)) ? passCourseName1 : passCourseName2;
                    svf.VrsOut("PASS_COURSE", printPassCourseName);
                }
                if (SCHOOLKIND_H.equals(_param._schoolkind)) {
                    svf.VrsOut("DIV1", (null != passCourseName1) ? "専願" : "―");
                    svf.VrsOut("DIV2", (null != passCourseName2) ? "併願" : "―");
                    svf.VrsOut("PASS_COURSE1", (null != passCourseName1) ? passCourseName1 : "―");
                    svf.VrsOut("PASS_COURSE2", (null != passCourseName2) ? passCourseName2 : "―");
                    svf.VrsOut("LIMIT", (null != passCourseName1 && null != passCourseName2) ? _param._sLimitDateJPMD : "");
                }
                svf.VrsOut("DATE", _param._noticeDateJP);
                schoolInfoPrint(svf);

                lineCnt++;
                _hasData = true;
            }

            if (_hasData) {
                svf.VrEndPage();
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlPass() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RCPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     L013_1.NAME1 AS PASS_COURSE_NAME1, ");
        stb.append("     L013_2.NAME1 AS PASS_COURSE_NAME2, ");
        stb.append("     RCPT.EXAMNO, ");
        stb.append("     R006.REMARK1 AS SHDIV ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RCPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND BASE.EXAMNO         = RCPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT R006 ");
        stb.append("          ON R006.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND R006.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND R006.TESTDIV        = RCPT.TESTDIV ");
        stb.append("         AND R006.EXAM_TYPE      = RCPT.EXAM_TYPE ");
        stb.append("         AND R006.RECEPTNO       = RCPT.RECEPTNO ");
        stb.append("         AND R006.SEQ            = '006' ");
        stb.append("     LEFT JOIN V_NAME_MST L013_1 ");
        stb.append("          ON L013_1.YEAR     = R006.ENTEXAMYEAR ");
        stb.append("         AND L013_1.NAMECD1  = 'L" + _param._schoolkind + "13' ");
        stb.append("         AND L013_1.NAMECD2  = R006.REMARK8 "); //専願合格コース
        stb.append("     LEFT JOIN V_NAME_MST L013_2 ");
        stb.append("          ON L013_2.YEAR     = R006.ENTEXAMYEAR ");
        stb.append("         AND L013_2.NAMECD1  = 'L" + _param._schoolkind + "13' ");
        stb.append("         AND L013_2.NAMECD2  = R006.REMARK9 "); //併願合格コース
        stb.append(" WHERE ");
        stb.append("         RCPT.ENTEXAMYEAR    = '" + _param._entexamyear + "' ");
        stb.append("     AND RCPT.APPLICANTDIV   = '" + _param._applicantdiv + "' ");
        stb.append("     AND RCPT.TESTDIV        = '" + _param._testDiv + "' ");
        stb.append("     AND RCPT.EXAM_TYPE      = '1' ");
        if (_param._receptnoFrom != null && _param._receptnoTo != null) {
            stb.append("     AND RCPT.RECEPTNO BETWEEN '" + _param._receptnoFrom + "' AND '" + _param._receptnoTo + "' ");
        } else if (_param._receptnoFrom != null) {
            stb.append("     AND RCPT.RECEPTNO = '" + _param._receptnoFrom + "' ");
        } else if (_param._receptnoTo != null) {
            stb.append("     AND RCPT.RECEPTNO = '" + _param._receptnoTo + "' ");
        }
        if (!"ALL".equals(_param._shDiv)) {
            stb.append("     AND R006.REMARK1 = '" + _param._shDiv + "' ");
        }
        if (!"ALL".equals(_param._wishCourse)) {
            stb.append("     AND R006.REMARK2 = '" + _param._wishCourse + "' ");
        }
        //1:合格通知
        stb.append("     AND (L013_1.NAMESPARE1 = '" + JUDGE_PASS + "' OR L013_2.NAMESPARE1 = '" + JUDGE_PASS + "') ");
        stb.append(" ORDER BY ");
        stb.append("     RCPT.RECEPTNO ");
        return stb.toString();
    }

    private void printUnPass(final DB2UDB db2, final Vrw32alp svf) {
        String frmId = "KNJL326A_2H.frm"; //2:選考結果通知書（不合格）
        svf.VrSetForm(frmId, 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlUnPass();
            log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxLine = 3;
            int lineCnt = 1;
            while (rs.next()) {
                if (lineCnt > maxLine) {
                    svf.VrEndPage();
                    lineCnt = 1;
                }

                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");

                svf.VrsOutn("EXAM_NO", lineCnt, receptno);
                final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(name) > 20 ? "2" : "1";
                svf.VrsOutn("NAME" + nameField, lineCnt, name);
                svf.VrsOutn("DATE", lineCnt, _param._noticeDateJP);
                schoolInfoPrintN(svf, lineCnt);

                lineCnt++;
                _hasData = true;
            }

            if (_hasData) {
                svf.VrEndPage();
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlUnPass() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RCPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     L013_1.NAME1 AS PASS_COURSE_NAME1, ");
        stb.append("     L013_2.NAME1 AS PASS_COURSE_NAME2, ");
        stb.append("     RCPT.EXAMNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RCPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND BASE.EXAMNO         = RCPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT R006 ");
        stb.append("          ON R006.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND R006.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND R006.TESTDIV        = RCPT.TESTDIV ");
        stb.append("         AND R006.EXAM_TYPE      = RCPT.EXAM_TYPE ");
        stb.append("         AND R006.RECEPTNO       = RCPT.RECEPTNO ");
        stb.append("         AND R006.SEQ            = '006' ");
        stb.append("     LEFT JOIN V_NAME_MST L013_1 ");
        stb.append("          ON L013_1.YEAR     = R006.ENTEXAMYEAR ");
        stb.append("         AND L013_1.NAMECD1  = 'L" + _param._schoolkind + "13' ");
        stb.append("         AND L013_1.NAMECD2  = R006.REMARK8 "); //専願合格コース
        stb.append("     LEFT JOIN V_NAME_MST L013_2 ");
        stb.append("          ON L013_2.YEAR     = R006.ENTEXAMYEAR ");
        stb.append("         AND L013_2.NAMECD1  = 'L" + _param._schoolkind + "13' ");
        stb.append("         AND L013_2.NAMECD2  = R006.REMARK9 "); //併願合格コース
        stb.append(" WHERE ");
        stb.append("         RCPT.ENTEXAMYEAR    = '" + _param._entexamyear + "' ");
        stb.append("     AND RCPT.APPLICANTDIV   = '" + _param._applicantdiv + "' ");
        stb.append("     AND RCPT.TESTDIV        = '" + _param._testDiv + "' ");
        stb.append("     AND RCPT.EXAM_TYPE      = '1' ");
        if (_param._receptnoFrom != null && _param._receptnoTo != null) {
            stb.append("     AND RCPT.RECEPTNO BETWEEN '" + _param._receptnoFrom + "' AND '" + _param._receptnoTo + "' ");
        } else if (_param._receptnoFrom != null) {
            stb.append("     AND RCPT.RECEPTNO = '" + _param._receptnoFrom + "' ");
        } else if (_param._receptnoTo != null) {
            stb.append("     AND RCPT.RECEPTNO = '" + _param._receptnoTo + "' ");
        }
        if (!"ALL".equals(_param._shDiv)) {
            stb.append("     AND R006.REMARK1 = '" + _param._shDiv + "' ");
        }
        if (!"ALL".equals(_param._wishCourse)) {
            stb.append("     AND R006.REMARK2 = '" + _param._wishCourse + "' ");
        }
        //2:選考結果通知書（不合格）
        stb.append("	AND (CASE WHEN R006.REMARK1 = '1' AND  R006.REMARK8 = '" + JUDGE_UNPASS + "' THEN 1 ");
        stb.append("	          WHEN R006.REMARK1 = '2' AND (R006.REMARK8 = '" + JUDGE_UNPASS + "' OR R006.REMARK8 IS NULL) AND R006.REMARK9 = '" + JUDGE_UNPASS + "' THEN 1 ");
        stb.append("          ELSE NULL END) = 1 ");
        stb.append(" ORDER BY ");
        stb.append("     RCPT.RECEPTNO ");
        return stb.toString();
    }

    private void printNKyoka(final DB2UDB db2, final Vrw32alp svf) {
        String frmId = null;
        if (SCHOOLKIND_J.equals(_param._schoolkind)) {
            frmId = "KNJL326A_2J.frm"; //3:入学許可書
        } else if (SCHOOLKIND_H.equals(_param._schoolkind)) {
            frmId = "KNJL326A_3H.frm"; //3:入学許可書
        }
        svf.VrSetForm(frmId, 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlNKyoka();
            log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");

                svf.VrsOut("EXAM_NO", receptno);
                final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(name) > 20 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, name);
                svf.VrsOut("DATE", _param._noticeDateJP);
                schoolInfoPrint(svf);

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

    private String sqlNKyoka() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RCPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     RCPT.EXAMNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RCPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND BASE.EXAMNO         = RCPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT R006 ");
        stb.append("          ON R006.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND R006.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND R006.TESTDIV        = RCPT.TESTDIV ");
        stb.append("         AND R006.EXAM_TYPE      = RCPT.EXAM_TYPE ");
        stb.append("         AND R006.RECEPTNO       = RCPT.RECEPTNO ");
        stb.append("         AND R006.SEQ            = '006' ");
        stb.append("     LEFT JOIN V_NAME_MST L013_1 ");
        stb.append("          ON L013_1.YEAR     = R006.ENTEXAMYEAR ");
        stb.append("         AND L013_1.NAMECD1  = 'L" + _param._schoolkind + "13' ");
        stb.append("         AND L013_1.NAMECD2  = R006.REMARK8 "); //専願合格コース
        stb.append("     LEFT JOIN V_NAME_MST L013_2 ");
        stb.append("          ON L013_2.YEAR     = R006.ENTEXAMYEAR ");
        stb.append("         AND L013_2.NAMECD1  = 'L" + _param._schoolkind + "13' ");
        stb.append("         AND L013_2.NAMECD2  = R006.REMARK9 "); //併願合格コース
        stb.append(" WHERE ");
        stb.append("         RCPT.ENTEXAMYEAR    = '" + _param._entexamyear + "' ");
        stb.append("     AND RCPT.APPLICANTDIV   = '" + _param._applicantdiv + "' ");
        stb.append("     AND RCPT.TESTDIV        = '" + _param._testDiv + "' ");
        stb.append("     AND RCPT.EXAM_TYPE      = '1' ");
        if (_param._receptnoFrom != null && _param._receptnoTo != null) {
            stb.append("     AND RCPT.RECEPTNO BETWEEN '" + _param._receptnoFrom + "' AND '" + _param._receptnoTo + "' ");
        } else if (_param._receptnoFrom != null) {
            stb.append("     AND RCPT.RECEPTNO = '" + _param._receptnoFrom + "' ");
        } else if (_param._receptnoTo != null) {
            stb.append("     AND RCPT.RECEPTNO = '" + _param._receptnoTo + "' ");
        }
        if (!"ALL".equals(_param._shDiv)) {
            stb.append("     AND R006.REMARK1 = '" + _param._shDiv + "' ");
        }
        if (!"ALL".equals(_param._wishCourse)) {
            stb.append("     AND R006.REMARK2 = '" + _param._wishCourse + "' ");
        }
        //3:入学許可書
        stb.append("     AND (L013_1.NAMESPARE1 = '" + JUDGE_PASS + "' OR L013_2.NAMESPARE1 = '" + JUDGE_PASS + "') ");
        stb.append(" ORDER BY ");
        stb.append("     RCPT.RECEPTNO ");
        return stb.toString();
    }

    private void printTokutaiJ(final DB2UDB db2, final Vrw32alp svf) {
        String frmId = null;
        String kindName = "";
        String enrollFees = "";
        String schoolFees = "";
        final String enrollFeesZengk = _param._honordivInfo._enrollFees;
        final String schoolFeesZengk = _param._honordivInfo._schoolFees;
        final String enrollFeesHangk = _param._honordivInfo._enrollFees;
        final String schoolFeesHangk = _param._honordivInfo._schoolFees;
        final String enrollFeesIppan = "210000";
        final String schoolFeesIppan = "570000";
        String titleName = "";
        /***
        NOTICE_KIND
          1:学力
          2:ファミリー
          3:英検
          4:資格
          5:クラブ
        NOTICE_CLASS
          1:第1種
          2:第2種
          3:第3種
          4:第4種
        ***/
        if (SCHOOLKIND_J.equals(_param._schoolkind)) {
            if ("1".equals(_param._honordivInfo._noticeKind) && "1".equals(_param._honordivInfo._noticeClass)) {
                frmId = "KNJL326A_3J.frm"; //学力特待生決定通知書1種
                kindName = "学力特待生（１種）";
                enrollFees = enrollFeesZengk;
                schoolFees = schoolFeesZengk;
            } else if ("1".equals(_param._honordivInfo._noticeKind) && "2".equals(_param._honordivInfo._noticeClass)) {
                frmId = "KNJL326A_3J.frm"; //学力特待生決定通知書2種
                kindName = "学力特待生（２種）";
                enrollFees = enrollFeesHangk;
                schoolFees = schoolFeesHangk;
            } else if ("1".equals(_param._honordivInfo._noticeKind) && "3".equals(_param._honordivInfo._noticeClass)) {
                frmId = "KNJL326A_4J.frm"; //学力特待生決定通知書3種
                kindName = "学力特待生（３種）";
                enrollFees = enrollFeesZengk;
                schoolFees = schoolFeesZengk;
            } else if ("2".equals(_param._honordivInfo._noticeKind) && "3".equals(_param._honordivInfo._noticeClass)) {
                frmId = "KNJL326A_5J.frm"; //ファミリー特待生決定通知書
                titleName = "ファミリー特待生決定通知書";
                kindName = "ファミリー特待生";
            } else if ("5".equals(_param._honordivInfo._noticeKind) && "1".equals(_param._honordivInfo._noticeClass)) {
                frmId = "KNJL326A_6J.frm"; //クラブ特待生決定通知書1種
                enrollFees = enrollFeesZengk;
                schoolFees = schoolFeesZengk;
            } else if ("5".equals(_param._honordivInfo._noticeKind) && "2".equals(_param._honordivInfo._noticeClass)) {
                frmId = "KNJL326A_6J.frm"; //クラブ特待生決定通知書2種
                enrollFees = enrollFeesHangk;
                schoolFees = schoolFeesHangk;
            } else if ("5".equals(_param._honordivInfo._noticeKind) && "3".equals(_param._honordivInfo._noticeClass)) {
                frmId = "KNJL326A_5J.frm"; //クラブ特待生決定通知書3種
                titleName = "特待生決定通知書";
                kindName = "クラブ特待生";
            }
            if (null == frmId) return;
        }
        svf.VrSetForm(frmId, 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlTokutaiJ();
            log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");

                svf.VrsOut("EXAM_NO", receptno);
                final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(name) > 20 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, name);
                svf.VrsOut("DATE", _param._noticeDateJP);
                svf.VrsOut("KIND1", kindName);
                svf.VrsOut("KIND2", kindName);
                svf.VrsOut("ENROLL_FEES1", enrollFees);
                svf.VrsOut("ENROLL_FEES2", enrollFeesIppan);
                svf.VrsOut("SCHOOL_FEES1", schoolFees);
                svf.VrsOut("SCHOOL_FEES2", schoolFeesIppan);
                svf.VrsOut("TITLE", titleName);
                svf.VrsOut("REFERENCE_YEAR", _param._referenceYear);
                schoolInfoPrint(svf);

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

    private String sqlTokutaiJ() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RCPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     RCPT.EXAMNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RCPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND BASE.EXAMNO         = RCPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B029 ");
        stb.append("          ON B029.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND B029.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND B029.EXAMNO         = RCPT.EXAMNO ");
        stb.append("         AND B029.SEQ            = '029' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT R006 ");
        stb.append("          ON R006.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND R006.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND R006.TESTDIV        = RCPT.TESTDIV ");
        stb.append("         AND R006.EXAM_TYPE      = RCPT.EXAM_TYPE ");
        stb.append("         AND R006.RECEPTNO       = RCPT.RECEPTNO ");
        stb.append("         AND R006.SEQ            = '006' ");
        stb.append("     LEFT JOIN V_NAME_MST L013_1 ");
        stb.append("          ON L013_1.YEAR     = R006.ENTEXAMYEAR ");
        stb.append("         AND L013_1.NAMECD1  = 'L" + _param._schoolkind + "13' ");
        stb.append("         AND L013_1.NAMECD2  = R006.REMARK8 "); //専願合格コース
        stb.append("     LEFT JOIN V_NAME_MST L013_2 ");
        stb.append("          ON L013_2.YEAR     = R006.ENTEXAMYEAR ");
        stb.append("         AND L013_2.NAMECD1  = 'L" + _param._schoolkind + "13' ");
        stb.append("         AND L013_2.NAMECD2  = R006.REMARK9 "); //併願合格コース
        stb.append(" WHERE ");
        stb.append("         RCPT.ENTEXAMYEAR    = '" + _param._entexamyear + "' ");
        stb.append("     AND RCPT.APPLICANTDIV   = '" + _param._applicantdiv + "' ");
        stb.append("     AND RCPT.TESTDIV        = '" + _param._testDiv + "' ");
        stb.append("     AND RCPT.EXAM_TYPE      = '1' ");
        if (_param._receptnoFrom != null && _param._receptnoTo != null) {
            stb.append("     AND RCPT.RECEPTNO BETWEEN '" + _param._receptnoFrom + "' AND '" + _param._receptnoTo + "' ");
        } else if (_param._receptnoFrom != null) {
            stb.append("     AND RCPT.RECEPTNO = '" + _param._receptnoFrom + "' ");
        } else if (_param._receptnoTo != null) {
            stb.append("     AND RCPT.RECEPTNO = '" + _param._receptnoTo + "' ");
        }
        if (!"ALL".equals(_param._shDiv)) {
            stb.append("     AND R006.REMARK1 = '" + _param._shDiv + "' ");
        }
        if (!"ALL".equals(_param._wishCourse)) {
            stb.append("     AND R006.REMARK2 = '" + _param._wishCourse + "' ");
        }
        stb.append("     AND VALUE(B029.REMARK5,'') != '1' "); //仮決定は出力対象外
        stb.append("     AND B029.REMARK7 = '" + _param._honordiv + "' ");
        stb.append("     AND (L013_1.NAMESPARE1 = '" + JUDGE_PASS + "' OR L013_2.NAMESPARE1 = '" + JUDGE_PASS + "') "); //合格者
        stb.append(" ORDER BY ");
        stb.append("     RCPT.RECEPTNO ");
        return stb.toString();
    }

    private void printTokutaiH(final DB2UDB db2, final Vrw32alp svf) {
        String frmId = null;
        String frmId2 = null;
        String titleName = "";
        String kindName = "";
        String enrollFees = "";
        String scholarship = "";
        String scholarshipRemark = "";
        String avreage = "";
        final String enrollFeesZengk = _param._honordivInfo._enrollFees;
        final String scholarshipZengk = "月額" + _param._honordivInfo._scholarship1 + "円(年間" + _param._honordivInfo._scholarship2 + "円)支給";
        final String scholarshipRemarkZengk = "月額" + _param._honordivInfo._scholarship1 + "円・年間" + _param._honordivInfo._scholarship2 + "円";
        final String scholarshipHangk = "月額" + _param._honordivInfo._scholarship1 + "円(年間" + _param._honordivInfo._scholarship2 + "円)支給";
        final String scholarshipRemarkHangk = "月額" + _param._honordivInfo._scholarship1 + "円・年間" + _param._honordivInfo._scholarship2 + "円";
        final String avreage1 = "3.5";
        final String avreage2 = "3.2";
        /***
        NOTICE_KIND
          1:学力
          2:ファミリー
          3:英検
          4:資格
          5:クラブ
        NOTICE_CLASS
          1:第1種
          2:第2種
          3:第3種
          4:第4種
        ***/
        if (SCHOOLKIND_H.equals(_param._schoolkind)) {
            if ("1".equals(_param._honordivInfo._noticeKind) && "1".equals(_param._honordivInfo._noticeClass)) {
                frmId = "KNJL326A_4H.frm"; //第1種学力特待生通知書
                frmId2 = "KNJL326A_5H.frm"; //第1種学力特待生通知書（専願切替）/第4種学力特待生通知書
                titleName = "第１種学力特待生通知書";
                kindName = "第１種学力特待生";
                enrollFees = enrollFeesZengk;
                scholarship = scholarshipZengk;
                scholarshipRemark = scholarshipRemarkZengk;
                avreage = avreage1;
            } else if ("1".equals(_param._honordivInfo._noticeKind) && "2".equals(_param._honordivInfo._noticeClass)) {
                frmId = "KNJL326A_4H.frm"; //第2種学力特待生通知書
                frmId2 = "KNJL326A_5H.frm"; //第2種学力特待生通知書（専願切替）/第4種学力特待生通知書
                titleName = "第２種学力特待生通知書";
                kindName = "第２種学力特待生";
                enrollFees = enrollFeesZengk;
                scholarship = scholarshipHangk;
                scholarshipRemark = scholarshipRemarkHangk;
                avreage = avreage2;
            } else if ("1".equals(_param._honordivInfo._noticeKind) && "3".equals(_param._honordivInfo._noticeClass)) {
                frmId = "KNJL326A_6H.frm"; //第3種学力特待生通知書
                frmId2 = "KNJL326A_6H.frm"; //第3種学力特待生通知書（専願切替）
                enrollFees = enrollFeesZengk;
            } else if ("1".equals(_param._honordivInfo._noticeKind) && "4".equals(_param._honordivInfo._noticeClass)) {
                frmId = "KNJL326A_7H.frm"; //第4種学力特待生通知書
            } else if ("5".equals(_param._honordivInfo._noticeKind)) {
                frmId = "KNJL326A_8H.frm"; //クラブ特待生通知書
                titleName = "クラブ特待生通知書";
                kindName = "クラブ特待生";
                enrollFees = enrollFeesZengk;
            } else if ("2".equals(_param._honordivInfo._noticeKind)) {
                frmId = "KNJL326A_8H.frm"; //ファミリー特待生通知書
                titleName = "ファミリー特待生通知書";
                kindName = "ファミリー特待生";
                enrollFees = enrollFeesZengk;
            } else if ("4".equals(_param._honordivInfo._noticeKind)) {
                frmId = "KNJL326A_8H.frm"; //資格特待生通知書
                titleName = "資格特待生通知書";
                kindName = "資格特待生";
                enrollFees = enrollFeesZengk;
            }
            if (null == frmId) return;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlTokutaiH();
            log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String senganPagePrintFlg = rs.getString("SENGAN_PAGE_PRINT_FLG");

                //page1
                svf.VrSetForm(frmId, 1);
                svf.VrsOut("EXAM_NO", receptno);
                final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(name) > 20 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, name);
                svf.VrsOut("DATE", _param._noticeDateJP);
                svf.VrsOut("TITLE", titleName);
                svf.VrsOut("KIND1", kindName);
                svf.VrsOut("KIND2", kindName);
                svf.VrsOut("KIND3", kindName);
                svf.VrsOut("ENROLL_FEES", enrollFees);
                svf.VrsOut("SCHOLARSHIP1", scholarship);
                svf.VrsOut("SCHOLARSHIP2", scholarshipRemark);
                svf.VrsOut("AVREAGE", avreage);
                if ("1".equals(_param._honordivInfo._noticeKind) && "4".equals(_param._honordivInfo._noticeClass)) {
                    svf.VrsOut("LIMIT2", (null != _param._hLimitDateJP) ? _param._hLimitDateJP : "");
                }
                schoolInfoPrint(svf);
                //第4種学力特待生通知書
                if ("1".equals(_param._honordivInfo._noticeKind) && "4".equals(_param._honordivInfo._noticeClass)) {
                    schoolInfoPrintContact2(svf);
                } else {
                    schoolInfoPrintContact(svf);
                }
                svf.VrEndPage();
                _hasData = true;

                //page2
                if (null != frmId2 && "1".equals(senganPagePrintFlg)) {
                    svf.VrSetForm(frmId2, 1);
                    svf.VrsOut("EXAM_NO", receptno);
                    svf.VrsOut("NAME" + nameField, name);
                    svf.VrsOut("DATE", _param._noticeDateJP);
                    svf.VrsOut("TITLE", titleName);
                    svf.VrsOut("KIND1", kindName);
                    svf.VrsOut("KIND2", kindName);
                    svf.VrsOut("KIND3", kindName);
                    svf.VrsOut("ENROLL_FEES", enrollFees);
                    svf.VrsOut("SCHOLARSHIP1", scholarship);
                    svf.VrsOut("SCHOLARSHIP2", scholarshipRemark);
                    svf.VrsOut("AVREAGE", avreage);
                    svf.VrsOut("LIMIT1", (null != _param._sLimitDateJP) ? _param._sLimitDateJP : "");
                    svf.VrsOut("LIMIT2", (null != _param._hLimitDateJP) ? _param._hLimitDateJP : "");
                    schoolInfoPrint(svf);
                    //第4種学力特待生通知書
                    if ("1".equals(_param._honordivInfo._noticeKind) && "4".equals(_param._honordivInfo._noticeClass)) {
                        schoolInfoPrintContact2(svf);
                    } else {
                        schoolInfoPrintContact(svf);
                    }
                    svf.VrEndPage();
                }
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlTokutaiH() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RCPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        //第1種学力特待生通知書（専願切替）/第4種学力特待生通知書
        //第2種学力特待生通知書（専願切替）/第4種学力特待生通知書
        //第3種学力特待生通知書（専願切替）
        stb.append("     CASE WHEN R006.REMARK1 = '2' AND L013_1.NAMESPARE1 = '1' AND L013_2.NAMESPARE1 = '1' ");
        if ("1".equals(_param._honordivInfo._noticeKind) && ("1".equals(_param._honordivInfo._noticeClass) || "2".equals(_param._honordivInfo._noticeClass))) {
            stb.append("           AND R006.REMARK8 = '1' AND ((HNR1.NOTICE_CLASS = '4' AND HNR1.NOTICE_KIND = '1') OR (HNR2.NOTICE_CLASS = '4' AND HNR2.NOTICE_KIND = '1') OR (HNR3.NOTICE_CLASS = '4' AND HNR3.NOTICE_KIND = '1')) ");
        }
        stb.append("          THEN '1' ELSE '0' ");
        stb.append("     END AS SENGAN_PAGE_PRINT_FLG, ");
        stb.append("     RCPT.EXAMNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RCPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND BASE.EXAMNO         = RCPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B029 ");
        stb.append("          ON B029.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND B029.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND B029.EXAMNO         = RCPT.EXAMNO ");
        stb.append("         AND B029.SEQ            = '029' ");
        stb.append("     LEFT JOIN ENTEXAM_HONORDIV_MST HNR1 ");
        stb.append("          ON HNR1.ENTEXAMYEAR    = B029.ENTEXAMYEAR ");
        stb.append("         AND HNR1.APPLICANTDIV   = B029.APPLICANTDIV ");
        stb.append("         AND HNR1.HONORDIV       = B029.REMARK1 ");
        stb.append("     LEFT JOIN ENTEXAM_HONORDIV_MST HNR2 ");
        stb.append("          ON HNR2.ENTEXAMYEAR    = B029.ENTEXAMYEAR ");
        stb.append("         AND HNR2.APPLICANTDIV   = B029.APPLICANTDIV ");
        stb.append("         AND HNR2.HONORDIV       = B029.REMARK2 ");
        stb.append("     LEFT JOIN ENTEXAM_HONORDIV_MST HNR3 ");
        stb.append("          ON HNR3.ENTEXAMYEAR    = B029.ENTEXAMYEAR ");
        stb.append("         AND HNR3.APPLICANTDIV   = B029.APPLICANTDIV ");
        stb.append("         AND HNR3.HONORDIV       = B029.REMARK3 ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT R006 ");
        stb.append("          ON R006.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND R006.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND R006.TESTDIV        = RCPT.TESTDIV ");
        stb.append("         AND R006.EXAM_TYPE      = RCPT.EXAM_TYPE ");
        stb.append("         AND R006.RECEPTNO       = RCPT.RECEPTNO ");
        stb.append("         AND R006.SEQ            = '006' ");
        stb.append("     LEFT JOIN V_NAME_MST L013_1 ");
        stb.append("          ON L013_1.YEAR     = R006.ENTEXAMYEAR ");
        stb.append("         AND L013_1.NAMECD1  = 'L" + _param._schoolkind + "13' ");
        stb.append("         AND L013_1.NAMECD2  = R006.REMARK8 "); //専願合格コース
        stb.append("     LEFT JOIN V_NAME_MST L013_2 ");
        stb.append("          ON L013_2.YEAR     = R006.ENTEXAMYEAR ");
        stb.append("         AND L013_2.NAMECD1  = 'L" + _param._schoolkind + "13' ");
        stb.append("         AND L013_2.NAMECD2  = R006.REMARK9 "); //併願合格コース
        stb.append(" WHERE ");
        stb.append("         RCPT.ENTEXAMYEAR    = '" + _param._entexamyear + "' ");
        stb.append("     AND RCPT.APPLICANTDIV   = '" + _param._applicantdiv + "' ");
        stb.append("     AND RCPT.TESTDIV        = '" + _param._testDiv + "' ");
        stb.append("     AND RCPT.EXAM_TYPE      = '1' ");
        if (_param._receptnoFrom != null && _param._receptnoTo != null) {
            stb.append("     AND RCPT.RECEPTNO BETWEEN '" + _param._receptnoFrom + "' AND '" + _param._receptnoTo + "' ");
        } else if (_param._receptnoFrom != null) {
            stb.append("     AND RCPT.RECEPTNO = '" + _param._receptnoFrom + "' ");
        } else if (_param._receptnoTo != null) {
            stb.append("     AND RCPT.RECEPTNO = '" + _param._receptnoTo + "' ");
        }
        if (!"ALL".equals(_param._shDiv)) {
            stb.append("     AND R006.REMARK1 = '" + _param._shDiv + "' ");
        }
        if (!"ALL".equals(_param._wishCourse)) {
            stb.append("     AND R006.REMARK2 = '" + _param._wishCourse + "' ");
        }
        stb.append("     AND VALUE(B029.REMARK5,'') != '1' "); //仮決定は出力対象外
        stb.append("     AND B029.REMARK7 = '" + _param._honordiv + "' ");
        stb.append("     AND (L013_1.NAMESPARE1 = '" + JUDGE_PASS + "' OR L013_2.NAMESPARE1 = '" + JUDGE_PASS + "') "); //合格者
        stb.append(" ORDER BY ");
        stb.append("     RCPT.RECEPTNO ");
        return stb.toString();
    }

    private void printKuikigai(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL326A_7J.frm", 1); // 5: 区域外就学届出書（中学入試のみ）

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlKuikigai();
            log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String nameAndSex = StringUtils.defaultString(rs.getString("NAME"), "") + "（" + StringUtils.defaultString(rs.getString("SEX_NAME"), "") + "）";
                final String birthday = rs.getString("BIRTHDAY");
                final String address = StringUtils.defaultString(rs.getString("ADDRESS1"), "") + StringUtils.defaultString(rs.getString("ADDRESS2"), "");
                final String gname = rs.getString("GNAME");
                final String relationshipName = rs.getString("RELATIONSHIP_NAME");

                final String gaddrField = KNJ_EditEdit.getMS932ByteLength(address) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(address) > 24 ? "2" : "1";
                svf.VrsOut("GUARD_ADDR" + gaddrField, address);
                svf.VrsOut("RELATION", relationshipName);
                final String gnameField = KNJ_EditEdit.getMS932ByteLength(gname) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(gname) > 20 ? "2" : "1";
                svf.VrsOut("GUARD_NAME" + gnameField, gname);

                final String addrField = KNJ_EditEdit.getMS932ByteLength(address) > 50 ? "3" : KNJ_EditEdit.getMS932ByteLength(address) > 40 ? "2" : "1";
                svf.VrsOut("ADDR" + addrField, address);
                final String nameField = KNJ_EditEdit.getMS932ByteLength(nameAndSex) > 40 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, nameAndSex);
                svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP_Bth(db2, birthday));

                svf.VrsOut("DATE", _param._noticeDateJP);
                svf.VrsOut("SCHOOL_ADDR", "大阪府高石市東羽衣1丁目11-57");
                if (null != _param._certifSchool) {
                    svf.VrsOut("SCHOOL_NAME", StringUtils.defaultString(trim(_param._certifSchool._schoolName), "") + "長");
                    svf.VrsOut("STAFF_NAME", StringUtils.defaultString(trim(_param._certifSchool._principalName), ""));
                }
                if (null != _param._staffStampFilePath) {
                    svf.VrsOut("SCHOOLSTAMP", _param._staffStampFilePath);
                }

                _hasData = true;
                svf.VrEndPage();
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private static String trim(final String s) {
        if (null == s) {
            return s;
        }
        int st = 0, ed = s.length();
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (ch == ' ' || ch == '　') {
                st = i + 1;
            } else {
                break;
            }
        }
        for (int i = s.length() - 1; i >= 0; i--) {
            final char ch = s.charAt(i);
            if (ch == ' ' || ch == '　') {
                ed = i;
            } else {
                break;
            }
        }
        if (st < ed) {
            return s.substring(st, ed);
        }
        return s;
    }

    private String sqlKuikigai() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RCPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.ABBV1 AS SEX_NAME, ");
        stb.append("     BASE.BIRTHDAY, ");
        stb.append("     ADDR.ADDRESS1, ");
        stb.append("     ADDR.ADDRESS2, ");
        stb.append("     ADDR.GNAME, ");
        stb.append("     H201.NAME1 AS RELATIONSHIP_NAME, ");
        stb.append("     RCPT.EXAMNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RCPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND BASE.EXAMNO         = RCPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ");
        stb.append("          ON ADDR.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND ADDR.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND ADDR.EXAMNO         = RCPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT R006 ");
        stb.append("          ON R006.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND R006.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND R006.TESTDIV        = RCPT.TESTDIV ");
        stb.append("         AND R006.EXAM_TYPE      = RCPT.EXAM_TYPE ");
        stb.append("         AND R006.RECEPTNO       = RCPT.RECEPTNO ");
        stb.append("         AND R006.SEQ            = '006' ");
        stb.append("     LEFT JOIN V_NAME_MST L013_1 ");
        stb.append("          ON L013_1.YEAR     = R006.ENTEXAMYEAR ");
        stb.append("         AND L013_1.NAMECD1  = 'L" + _param._schoolkind + "13' ");
        stb.append("         AND L013_1.NAMECD2  = R006.REMARK8 "); // 専願合格コース
        stb.append("     LEFT JOIN V_NAME_MST L013_2 ");
        stb.append("          ON L013_2.YEAR     = R006.ENTEXAMYEAR ");
        stb.append("         AND L013_2.NAMECD1  = 'L" + _param._schoolkind + "13' ");
        stb.append("         AND L013_2.NAMECD2  = R006.REMARK9 "); // 併願合格コース
        stb.append("     LEFT JOIN V_NAME_MST Z002 ");
        stb.append("          ON Z002.YEAR       = BASE.ENTEXAMYEAR ");
        stb.append("         AND Z002.NAMECD1    = 'Z002' ");
        stb.append("         AND Z002.NAMECD2    = BASE.SEX ");
        stb.append("     LEFT JOIN V_NAME_MST H201 ");
        stb.append("          ON H201.YEAR       = ADDR.ENTEXAMYEAR ");
        stb.append("         AND H201.NAMECD1    = 'H201' ");
        stb.append("         AND H201.NAMECD2    = ADDR.RELATIONSHIP ");
        stb.append(" WHERE ");
        stb.append("         RCPT.ENTEXAMYEAR    = '" + _param._entexamyear + "' ");
        stb.append("     AND RCPT.APPLICANTDIV   = '" + _param._applicantdiv + "' ");
        stb.append("     AND RCPT.TESTDIV        = '" + _param._testDiv + "' ");
        stb.append("     AND RCPT.EXAM_TYPE      = '1' ");
        if (_param._receptnoFrom != null && _param._receptnoTo != null) {
            stb.append("     AND RCPT.RECEPTNO BETWEEN '" + _param._receptnoFrom + "' AND '" + _param._receptnoTo + "' ");
        } else if (_param._receptnoFrom != null) {
            stb.append("     AND RCPT.RECEPTNO = '" + _param._receptnoFrom + "' ");
        } else if (_param._receptnoTo != null) {
            stb.append("     AND RCPT.RECEPTNO = '" + _param._receptnoTo + "' ");
        }
        if (!"ALL".equals(_param._shDiv)) {
            stb.append("     AND R006.REMARK1 = '" + _param._shDiv + "' ");
        }
        if (!"ALL".equals(_param._wishCourse)) {
            stb.append("     AND R006.REMARK2 = '" + _param._wishCourse + "' ");
        }
        stb.append("     AND (L013_1.NAMESPARE1 = '" + JUDGE_PASS + "' OR L013_2.NAMESPARE1 = '" + JUDGE_PASS + "') ");
        stb.append(" ORDER BY ");
        stb.append("     RCPT.RECEPTNO ");
        return stb.toString();
    }

    private void schoolInfoPrint(final Vrw32alp svf) {
        if (null != _param._staffStampFilePath) {
            svf.VrsOut("SCHOOLSTAMP", _param._staffStampFilePath);
        }
        if (null != _param._certifSchool) {
            svf.VrsOut("SCHOOL_NAME", _param._certifSchool._schoolName);
            svf.VrsOut("STAFF_NAME", StringUtils.defaultString(_param._certifSchool._jobName, "") + StringUtils.defaultString(_param._certifSchool._principalName, ""));
        }
    }

    private void schoolInfoPrintN(final Vrw32alp svf, final int lineCnt) {
        if (null != _param._staffStampFilePath) {
            svf.VrsOutn("SCHOOLSTAMP", lineCnt, _param._staffStampFilePath);
        }
        if (null != _param._certifSchool) {
            svf.VrsOutn("SCHOOL_NAME", lineCnt, _param._certifSchool._schoolName);
            svf.VrsOutn("STAFF_NAME", lineCnt, StringUtils.defaultString(_param._certifSchool._jobName, "") + StringUtils.defaultString(_param._certifSchool._principalName, ""));
        }
    }

    private void schoolInfoPrintContact(final Vrw32alp svf) {
//        svf.VrsOut("CONTACT", "連絡先：" + _param._certifSchool._remark7 + "　電話 " + _param._certifSchool._remark1 + "(" + _param._certifSchool._remark8 + ")");
        String remark8 = (null != _param._certifSchool) ? StringUtils.defaultString(_param._certifSchool._remark8) : "";
        svf.VrsOut("CONTACT", "連絡先：" + "羽衣学園高等学校" + "　電話 " + "072-265-7561" + "(" + remark8 + ")");
    }

    private void schoolInfoPrintContact2(final Vrw32alp svf) {
//        svf.VrsOut("CONTACT", _param._certifSchool._remark8 + "まで");
//        svf.VrsOut("ZIPNO", _param._certifSchool._remark2);
//        svf.VrsOut("ADDR", _param._certifSchool._remark4);
//        svf.VrsOut("TELNO", _param._certifSchool._remark1);
        String remark8 = (null != _param._certifSchool) ? StringUtils.defaultString(_param._certifSchool._remark8) : "";
        svf.VrsOut("CONTACT", remark8 + "まで");
        svf.VrsOut("ZIPNO", "592-0003");
        svf.VrsOut("ADDR", "大阪府高石市東羽衣1-11-57");
        svf.VrsOut("TELNO", "072-265-7561");
    }

    private class CertifSchool {
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        final String _remark1;
        final String _remark2;
        final String _remark4;
        final String _remark7;
        final String _remark8;
        final String _remark9;
        public CertifSchool(
                final String schoolName,
                final String jobName,
                final String principalName,
                final String remark1,
                final String remark2,
                final String remark4,
                final String remark7,
                final String remark8,
                final String remark9
        ) {
            _schoolName = schoolName;
            _jobName = jobName;
            _principalName = principalName;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark4 = remark4;
            _remark7 = remark7;
            _remark8 = remark8;
            _remark9 = remark9;
        }
    }

    private class HonordivInfo {
        final String _noticeClass;
        final String _noticeKind;
        final String _enrollFees;
        final String _schoolFees;
        final String _scholarship1;
        final String _scholarship2;
        public HonordivInfo(
                final String noticeClass,
                final String noticeKind,
                final String enrollFees,
                final String schoolFees,
                final String scholarship1,
                final String scholarship2
        ) {
            _noticeClass = noticeClass;
            _noticeKind = noticeKind;
            _enrollFees = enrollFees;
            _schoolFees = schoolFees;
            _scholarship1 = scholarship1;
            _scholarship2 = scholarship2;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 73659 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _applicantdiv;
        final String _testDiv;
        final String _noticeType;
        final String _honordiv;
        final String _noticeDate;
        final String _sLimitDate;
        final String _hLimitDate;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final String _documentroot;
        final String _imagepath;
        final String _schoolkind;
        final String _staffStampFilePath;
        final CertifSchool _certifSchool;
        final HonordivInfo _honordivInfo;
        final String _shDiv;
        final String _wishCourse;
        final String _receptnoFrom;
        final String _receptnoTo;

        final String _noticeDateJP;
        final String _sLimitDateJP;
        final String _sLimitDateJPMD;
        final String _hLimitDateJP;
        final String _referenceYear;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _noticeType = request.getParameter("NOTICE_TYPE");
            _honordiv = request.getParameter("HONORDIV");
            _noticeDate = request.getParameter("NOTICE_DATE");
            _sLimitDate = request.getParameter("S_LIMIT_DATE");
            _hLimitDate = request.getParameter("H_LIMIT_DATE");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _shDiv = request.getParameter("SHDIV");
            _wishCourse = request.getParameter("WISH_COURSE");
            _receptnoFrom = StringUtils.isBlank(request.getParameter("RECEPTNO_FROM")) ? null : request.getParameter("RECEPTNO_FROM");
            _receptnoTo = StringUtils.isBlank(request.getParameter("RECEPTNO_TO")) ? null : request.getParameter("RECEPTNO_TO");
            KNJ_Control imagepath_extension = new KNJ_Control();                //取得クラスのインスタンス作成
            KNJ_Control.ReturnVal returnval = imagepath_extension.Control(db2);
            _imagepath = returnval.val4;                                        //写真データ格納フォルダ
            _schoolkind = ("1".equals(_applicantdiv)) ? SCHOOLKIND_J : SCHOOLKIND_H;
            if (SCHOOLKIND_J.equals(_schoolkind)) {
                _staffStampFilePath = getImageFilePath("SCHOOLSTAMP_J.bmp");//校長印
                _certifSchool = getCertifSchool(db2, "105");
            } else {
                _staffStampFilePath = getImageFilePath("SCHOOLSTAMP_H.bmp");//校長印
                _certifSchool = getCertifSchool(db2, "106");
            }
            _honordivInfo = getHonordivInfo(db2, _honordiv);

            _noticeDateJP = KNJ_EditDate.h_format_JP(db2, _noticeDate);
            final String sLimitDateW = KNJ_EditDate.h_format_W(_sLimitDate);
            _sLimitDateJP = KNJ_EditDate.h_format_JP(db2, _sLimitDate) + "（" + sLimitDateW + "）";
            _sLimitDateJPMD = KNJ_EditDate.h_format_JP_MD(_sLimitDate) + "（" + sLimitDateW + "）";
            final String hLimitDateW = KNJ_EditDate.h_format_W(_hLimitDate);
            _hLimitDateJP = KNJ_EditDate.h_format_JP(db2, _hLimitDate) + "（" + hLimitDateW + "）";
            _referenceYear = KNJ_EditDate.h_format_JP_N(db2, _entexamyear + "-04-01") + "度";
        }

        private HonordivInfo getHonordivInfo(final DB2UDB db2, final String honordiv) {
            HonordivInfo honordivInfo = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM ENTEXAM_HONORDIV_MST WHERE ENTEXAMYEAR = '" + _entexamyear + "' AND APPLICANTDIV = '" + _applicantdiv + "' AND HONORDIV = '" + honordiv + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String noticeClass = rs.getString("NOTICE_CLASS");
                    final String noticeKind = rs.getString("NOTICE_KIND");
                    final String enrollFees = rs.getString("ENROLL_FEES");
                    final String schoolFees = rs.getString("SCHOOL_FEES");
                    final String scholarship1 = rs.getString("SCHOLARSHIP1");
                    final String scholarship2 = rs.getString("SCHOLARSHIP2");
                    honordivInfo = new HonordivInfo(noticeClass, noticeKind, enrollFees, schoolFees, scholarship1, scholarship2);
                }
            } catch (SQLException ex) {
                log.debug("getHonordivInfo exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return honordivInfo;
        }

        private CertifSchool getCertifSchool(final DB2UDB db2, final String certifKindcd) {
            CertifSchool certifSchool = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '" + certifKindcd + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String schoolName = rs.getString("SCHOOL_NAME");
                    final String jobName = rs.getString("JOB_NAME");
                    final String principalName = rs.getString("PRINCIPAL_NAME");
                    final String remark1 = rs.getString("REMARK1");
                    final String remark2 = rs.getString("REMARK2");
                    final String remark4 = rs.getString("REMARK4");
                    final String remark7 = rs.getString("REMARK7");
                    final String remark8 = rs.getString("REMARK8");
                    final String remark9 = rs.getString("REMARK9");
                    certifSchool = new CertifSchool(schoolName, jobName, principalName, remark1, remark2, remark4, remark7, remark8, remark9);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return certifSchool;
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFilePath(final String filename) {
            if (null == _documentroot || null == _imagepath || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentroot).append("/").append(_imagepath).append("/").append(filename);
            final File file = new File(path.toString());
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

    }
}

// eof
