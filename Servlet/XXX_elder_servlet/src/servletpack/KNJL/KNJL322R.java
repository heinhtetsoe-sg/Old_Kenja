/*
 * $Id: 84be8d6fbd1fcf1eb865a26df409385c291bd32e $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２２Ｒ＞  合否台帳
 **/
public class KNJL322R {

    private static final Log log = LogFactory.getLog(KNJL322R.class);

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

    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List list = Applicant.load(db2, _param);
        if (list.size() == 0) {
            return;
        }
        svf.VrSetForm("KNJL322R_1.frm", 1);
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "（" + _param._entexamyear + "）年度"); // 年度
        svf.VrsOut("TITLE", _param._testdivName + "合否台帳");
        svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
        svf.VrEndPage();

        final String form = "2".equals(_param._shubetsu) ? "KNJL322R_3.frm" : "KNJL322R_2.frm";

        String orderName = "";
        if ("1".equals(_param._output)) {
            orderName = "受験番号";
        } else if ("2".equals(_param._output)) {
            orderName = "出身校";
        } else if ("3".equals(_param._output)) {
            orderName = "コース";
        }

        final List pageList = getPageList(list, 50);

        for (int i = 0; i < pageList.size(); i++) {
            final List receptList = (List) pageList.get(i);

            svf.VrSetForm(form, 1);
            svf.VrsOut("TITLE", _param._entexamyear + "年度　" + _param._testdivName + "【合否】（" + (orderName) + "順）"); // タイトル
            svf.VrsOut("DATE", _param._dateStr); // 印刷日

            for (int j = 0; j < receptList.size(); j++) {
                final Applicant recept = (Applicant) receptList.get(j);
                final int gyo = j + 1;
                svf.VrsOutn("EXAM_NO", gyo, recept._examno); // 受験番号
                svf.VrsOutn("NAME", gyo, recept._name); // 名前
                svf.VrsOutn("SEX1", gyo, recept._sexName); // 性別
                svf.VrsOutn("JH_CODE1", gyo, recept._fsCd); // 中学校コード
                svf.VrsOutn("JH_NAME1", gyo, recept._finschoolName); // 中学校名
                if ("2".equals(_param._shubetsu)) {
                    svf.VrsOutn("SCORE", gyo, recept._total4); // 得点
                }
                svf.VrsOutn("JUDGE", gyo, recept._judgement); // 判定
                svf.VrsOutn("COURSE1", gyo, recept._majorname); // コース
                svf.VrsOutn("COURSE2", gyo, recept._examcourseName); // コース
                svf.VrsOutn("EXAM_EXIST", gyo, recept._juken); // 受験有無
                svf.VrsOutn("REMARK", gyo, recept._judgeKindName); // 備考
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private static class Applicant {
        final String _examno;
        final String _name;
        final String _sex;
        final String _sexName;
        final String _fsCd;
        final String _finschoolName;
        final String _judgement;
        final String _juken;
        final String _total4;
        final String _majorname;
        final String _examcourseName;
        final String _judgeKindName;

        Applicant(
            final String examno,
            final String name,
            final String sex,
            final String sexName,
            final String fsCd,
            final String finschoolName,
            final String judgement,
            final String juken,
            final String total4,
            final String majorname,
            final String examcourseName,
            final String judgeKindName
        ) {
            _examno = examno;
            _name = name;
            _sex = sex;
            _sexName = sexName;
            _fsCd = fsCd;
            _finschoolName = finschoolName;
            _judgement = judgement;
            _juken = juken;
            _total4 = total4;
            _majorname = majorname;
            _examcourseName = examcourseName;
            _judgeKindName = judgeKindName;
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");
                    final String name = rs.getString("NAME");
                    final String sex = rs.getString("SEX");
                    final String sexName = rs.getString("SEX_NAME");
                    final String fsCd = rs.getString("FS_CD");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String judgement = rs.getString("JUDGEMENT");
                    final String juken = rs.getString("JUKEN");
                    final String total4 = rs.getString("TOTAL4");
                    final String majorname = rs.getString("MAJORNAME");
                    final String examcourseName = rs.getString("EXAMCOURSE_NAME");
                    final String judgeKindName = rs.getString("JUDGE_KIND_NAME");
                    final Applicant applicant = new Applicant(examno, name, sex, sexName, fsCd, finschoolName, judgement, juken, total4, majorname, examcourseName, judgeKindName);
                    list.add(applicant);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX, ");
            stb.append("     NMZ002.ABBV1 AS SEX_NAME, ");
            stb.append("     BASE.FS_CD, ");
            stb.append("     T6.FINSCHOOL_NAME, ");
            stb.append("     CASE WHEN BASE.JUDGEMENT IS NOT NULL THEN ");
            stb.append("         CASE WHEN NML013.NAMESPARE1 = '1' THEN '合格' ELSE '不合格' END ");
            stb.append("     END AS JUDGEMENT, ");
            stb.append("     CASE WHEN T1.TOTAL4 IS NOT NULL THEN '○' ELSE '×' END AS JUKEN, ");
            stb.append("     T1.TOTAL4, ");
            stb.append("     T13.MAJORNAME, ");
            stb.append("     T14.EXAMCOURSE_NAME, ");
            stb.append("     CASE WHEN BASE.JUDGE_KIND IS NOT NULL THEN '奨学生' END AS JUDGE_KIND_NAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT T1 ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND BASE.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND BASE.EXAMNO       = T1.EXAMNO ");
            stb.append("     LEFT JOIN MAJOR_MST T13 ON T13.COURSECD = BASE.SUC_COURSECD ");
            stb.append("         AND T13.MAJORCD = BASE.SUC_MAJORCD ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T14 ON T14.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T14.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T14.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND T14.COURSECD = BASE.SUC_COURSECD ");
            stb.append("         AND T14.MAJORCD = BASE.SUC_MAJORCD ");
            stb.append("         AND T14.EXAMCOURSECD = BASE.SUC_COURSECODE ");
            stb.append("     LEFT JOIN V_FINSCHOOL_MST T6 ON T6.YEAR = BASE.ENTEXAMYEAR AND T6.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' ");
            stb.append("         AND NMZ002.NAMECD2 = BASE.SEX ");
            stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' ");
            stb.append("         AND NML013.NAMECD2 = BASE.JUDGEMENT ");
            stb.append(" WHERE ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND BASE.TESTDIV = '" + param._testdiv + "' ");
            stb.append(" ORDER BY ");
            if ("2".equals(param._output)) {
                stb.append("     BASE.FS_CD, ");
            } else if ("3".equals(param._output)) {
                stb.append("     BASE.SUC_COURSECD, ");
            } else { // if ("1".equals(param._output)) {
            }
            stb.append("     BASE.EXAMNO ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63854 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        final String _shubetsu; // 出力順 1:得点なし 2:得点あり
        final String _output; // 出力順 1:受験番号 2:出身校 3:コース別

        final String _applicantdivName;
        final String _testdivName;
        final String _dateStr;
        final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE");
            _shubetsu = request.getParameter("SHUBETSU");
            _output = request.getParameter("OUTPUT");
            _dateStr = getDateStr(_date);

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testdivName = getNameMst(db2, "NAME1", "L004", _testdiv);
            _schoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
        }

        private String getDateStr(final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(date);
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT " + field + " ");
                sql.append(" FROM CERTIF_SCHOOL_DAT ");
                sql.append(" WHERE YEAR = '" + _entexamyear + "' ");
                sql.append("   AND CERTIF_KINDCD = '106' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
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

