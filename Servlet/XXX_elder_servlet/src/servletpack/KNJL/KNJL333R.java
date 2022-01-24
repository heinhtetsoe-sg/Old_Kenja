/*
 * $Id: 53c1fde51215d29184fe78227efa7e19a9f26ad8 $
 *
 * 作成日: 2019/01/10
 * 作成者: yogi
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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３３３Ｒ＞  合否台帳
 **/
public class KNJL333R {

    private static final Log log = LogFactory.getLog(KNJL333R.class);

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
        final String[] ywk = KNJ_EditDate.tate_format4(db2, _param._entexamyear + "-04-01");
        svf.VrSetForm("KNJL333R_1.frm", 1);
        svf.VrsOut("NENDO", ywk[0] + ywk[1] + "（" + _param._entexamyear + "）年度"); // 年度
        svf.VrsOut("TITLE", _param._testdivName + "合否台帳");
        svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
        svf.VrEndPage();

        final String form = "KNJL333R_2.frm";

        String orderName = "";
        if ("1".equals(_param._output)) {
            orderName = "受験番号";
        } else if ("2".equals(_param._output)) {
            orderName = "出身校";
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
                svf.VrsOutn("EXAM_NO", gyo, recept._receptno); // 受験番号
                svf.VrsOutn("NAME", gyo, recept._name); // 名前
                svf.VrsOutn("SEX1", gyo, recept._sexName); // 性別
                svf.VrsOutn("JH_CODE1", gyo, recept._fsCd); // 中学校コード
                svf.VrsOutn("JH_NAME1", gyo, recept._finschoolAbbv); // 中学校名
                svf.VrsOutn("SH_DIV", gyo, recept._shDivName);  //専併区分
                if ("2".equals(_param._shubetsu)) {
                	if ("on".equals(_param._katen)) {
                        svf.VrsOutn("SCORE", gyo, recept._total1); // 得点(加点あり)
                	} else {
                        svf.VrsOutn("SCORE", gyo, recept._total2); // 得点(加点なし)
                	}
                }
                svf.VrsOutn("JUDGE", gyo, recept._judgement); // 判定
                svf.VrsOutn("REMARK", gyo, recept._remarks); // 備考
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private static class Applicant {
    	final String _receptno;
        final String _examno;
        final String _name;
        final String _sex;
        final String _sexName;
        final String _fsCd;
        final String _finschoolAbbv;
        final String _shdiv;
        final String _shDivName;
        final String _judgement;
        final String _total1;
        final String _total2;
        final String _remarks;

        Applicant(
        	final String receptno,
            final String examno,
            final String name,
            final String sex,
            final String sexName,
            final String fsCd,
            final String finschoolAbbv,
            final String shDiv,
            final String shDivName,
            final String judgement,
            final String total1,
            final String total2,
            final String remarks

        ) {
        	_receptno = receptno;
            _examno = examno;
            _name = name;
            _sex = sex;
            _sexName = sexName;
            _fsCd = fsCd;
            _finschoolAbbv = finschoolAbbv;
            _shdiv = shDiv;
            _shDivName = shDivName;
            _judgement = judgement;
            _total1 = total1;
            _total2 = total2;
            _remarks = remarks;
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
                    final String receptno = rs.getString("RECEPTNO");
                    final String examno = rs.getString("EXAMNO");
                    final String name = rs.getString("NAME");
                    final String sex = rs.getString("SEX");
                    final String sexName = rs.getString("SEX_NAME");
                    final String fsCd = rs.getString("FS_CD");
                    final String finschoolAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                    final String shDiv = rs.getString("SHDIV");
                    final String shDivName = rs.getString("SHDIVNAME");
                    final String judgement = rs.getString("JUDGEMENT");
                    final String total1 = rs.getString("TOTAL1");
                    final String total2 = rs.getString("TOTAL2");
                    final String remarks = rs.getString("REMARKS");
                    final Applicant applicant = new Applicant(receptno, examno, name, sex, sexName, fsCd, finschoolAbbv, shDiv, shDivName, judgement, total1, total2, remarks);
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
        	final String revid = "1".equals(param._testdiv) ? "2" : "1";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     V1.RECEPTNO, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX, ");
            stb.append("     T8_013.REMARK1 AS SHDIV, ");
            stb.append("     NML006.NAME2 AS SHDIVNAME, ");
            stb.append("     NMZ002.ABBV1 AS SEX_NAME, ");
            stb.append("     BASE.FS_CD, ");
            stb.append("     VALUE(T6.FINSCHOOL_NAME_ABBV, T6.FINSCHOOL_NAME) AS FINSCHOOL_NAME_ABBV, ");
            stb.append("     CASE WHEN T1.JUDGEDIV IS NOT NULL THEN NML013.ABBV1 END AS JUDGEMENT, ");
            stb.append("     T1.TOTAL1, ");
            stb.append("     T1.TOTAL2, ");
            if ("2".equals(param._applicantdiv)) {
            	stb.append("     CASE WHEN NML024_2.NAME1 IS NOT NULL THEN NML024_2.NAME1 ELSE '' END ");
            } else {
            	stb.append("     CASE WHEN NML004_2.NAME1 IS NOT NULL THEN NML004_2.NAME1 ELSE '' END ");
            }
            if("2".equals(param._shubetsu)) {
                stb.append("    || ' ' || ");
                if ("on".equals(param._katen)) {
                    stb.append(" CASE WHEN R_T1.TOTAL1 IS NOT NULL THEN '得点' || CAST(R_T1.TOTAL1 AS CHAR(4)) ELSE '' END ");
                } else {
                    stb.append(" CASE WHEN R_T1.TOTAL2 IS NOT NULL THEN '得点' || CAST(R_T1.TOTAL2 AS CHAR(4)) ELSE '' END ");
                }
            }
            stb.append("    || ' ' || ");
            stb.append(" CASE WHEN NML006_2.NAME2 IS NOT NULL THEN NML006_2.NAME2 ELSE '' END ");
            stb.append(" || CASE WHEN NML064_2.NAME1 IS NOT NULL THEN NML064_2.NAME1 ELSE '' END AS REMARKS ");
            stb.append(" FROM ");
            stb.append("     V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT V1");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("          ON BASE.ENTEXAMYEAR  = V1.ENTEXAMYEAR ");
            stb.append("         AND BASE.APPLICANTDIV = V1.APPLICANTDIV ");
            stb.append("         AND BASE.EXAMNO       = V1.EXAMNO ");
            stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T1  ");
            stb.append("          ON T1.ENTEXAMYEAR  = V1.ENTEXAMYEAR ");
            stb.append("         AND T1.APPLICANTDIV = V1.APPLICANTDIV ");
            stb.append("         AND T1.EXAMNO       = V1.EXAMNO ");
            stb.append("         AND T1.TESTDIV      = V1.TESTDIV ");
            stb.append("     LEFT JOIN FINSCHOOL_MST T6 ON T6.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' ");
            stb.append("         AND NMZ002.NAMECD2 = BASE.SEX ");
            stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' ");
            stb.append("         AND NML013.NAMECD2 = T1.JUDGEDIV ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT T8_010_2 ON T8_010_2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T8_010_2.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T8_010_2.EXAMNO       = BASE.EXAMNO ");
            stb.append("         AND T8_010_2.SEQ = '010' ");
            if ("2".equals(param._applicantdiv)) {
                stb.append("     LEFT JOIN NAME_MST NML024_2 ON NML024_2.NAMECD1 = 'L024' ");
                stb.append("         AND NML024_2.NAMECD2 = T8_010_2.REMARK" + revid + " ");
            } else {
                stb.append("     LEFT JOIN NAME_MST NML004_2 ON NML004_2.NAMECD1 = 'L004' ");
                stb.append("         AND NML004_2.NAMECD2 = T8_010_2.REMARK" + revid + " ");
            }
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT T8_012_2 ON T8_012_2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T8_012_2.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T8_012_2.EXAMNO       = BASE.EXAMNO ");
            stb.append("         AND T8_012_2.SEQ = '012' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT T8_013 ON T8_013.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T8_013.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T8_013.EXAMNO       = BASE.EXAMNO ");
            stb.append("         AND T8_013.SEQ = '013' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT T8_014_2 ON T8_014_2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T8_014_2.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T8_014_2.EXAMNO       = BASE.EXAMNO ");
            stb.append("         AND T8_014_2.SEQ = '014' ");
            stb.append("     LEFT JOIN NAME_MST NML064_2 ON NML064_2.NAMECD1 = 'L064' ");
            stb.append("         AND NML064_2.NAMECD2 = T8_014_2.REMARK" + revid + " ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT R_T1 ON R_T1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND R_T1.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND R_T1.TESTDIV = '" + revid + "' ");
            stb.append("         AND R_T1.RECEPTNO = T8_012_2.REMARK" + revid + " ");
            stb.append("     LEFT JOIN NAME_MST NML006 ON NML006.NAMECD1 = 'L006' ");
            stb.append("         AND NML006.NAMECD2 = T8_013.REMARK" + param._testdiv + " ");
            stb.append("     LEFT JOIN NAME_MST NML006_2 ON NML006_2.NAMECD1 = 'L006' ");
            stb.append("         AND NML006_2.NAMECD2 = T8_013.REMARK" + revid + " ");
            stb.append(" WHERE ");
            stb.append("     V1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND V1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND V1.TESTDIV = '" + param._testdiv + "' ");
            stb.append(" ORDER BY ");
            if ("2".equals(param._output)) {
                stb.append("     BASE.FS_CD, ");
            }
            stb.append("     V1.RECEPTNO ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65247 $");
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
        final String _katen;
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
            _katen = StringUtils.defaultString(request.getParameter("KATEN"), "");
            _dateStr = getDateStr(db2, _date);

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            if ("2".equals(_applicantdiv)) {
                _testdivName = getNameMst(db2, "NAME1", "L024", _testdiv);
            } else {
                _testdivName = getNameMst(db2, "NAME1", "L004", _testdiv);
            }
            _schoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
        }

        private String getDateStr(final DB2UDB db2, final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(db2, date);
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
                if ("2".equals(_applicantdiv)) {
                    sql.append("   AND CERTIF_KINDCD = '105' ");
                } else {
                    sql.append("   AND CERTIF_KINDCD = '106' ");
                }
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

