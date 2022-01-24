/*
 * $Id: 8027d6317a56d294f3d4b960a8f16a6eba4ab12a $
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３０４Ｒ＞  入試願書受付一覧
 **/
public class KNJL304R {

    private static final Log log = LogFactory.getLog(KNJL304R.class);

    private boolean _hasData;

    private static final String GScd = "0001";
    private static final String GAcd = "0002";

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

    public void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String form = "KNJL304R.frm";

        for (final Iterator it0 = _param._examcourseList.iterator(); it0.hasNext();) {
            final Map examCourseMap = (Map) it0.next();
            final String examCourseCd = (String) examCourseMap.get("EXAMCOURSECD");
            final String examCourseName = (String) examCourseMap.get("EXAMCOURSE_MARK");

            final List list = ReceptDat.load(db2, _param, examCourseCd);
            final List pageList = getPageList(list, 50);
            for (int pi = 0; pi < pageList.size(); pi++) {
                final List page = (List) pageList.get(pi);

                svf.VrSetForm(form, 1);
                svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + StringUtils.defaultString(_param._testdivName) + "願書受付一覧（" + StringUtils.defaultString(examCourseName)  + "）"); // タイトル
                svf.VrsOut("DATE", _param._dateStr); // 印刷日
                svf.VrsOut("PAGE1", String.valueOf(pi + 1));
                svf.VrsOut("PAGE2", String.valueOf(pageList.size()));

                for (int i = 0; i < page.size(); i++) {
                    final ReceptDat appl = (ReceptDat) page.get(i);
                    final int line = i + 1;
                    svf.VrsOutn("EXAM_NO", line, appl._examno); // 受験番号
                    svf.VrsOutn("COURSE1", line, appl._examcourseMark1); // コース
                    svf.VrsOutn("COURSE2", line, appl._examcourseMark2); // コース
                    svf.VrsOutn("COURSE3", line, appl._examcourseMark3); // コース
                    svf.VrsOutn("COURSE4", line, appl._examcourseMark4); // コース
                    svf.VrsOutn("NAME1", line, appl._name); // 名前
                    svf.VrsOutn("KANA", line, appl._nameKana); // ふりがな
                    svf.VrsOutn("BIRTHDAY", line, null == appl._birthday ? null : appl._birthday.replace('-', '/')); // 誕生日
                    svf.VrsOutn("SEX", line, appl._sexName); // 性別
                    svf.VrsOutn("ZIP_NO", line, appl._zipcd); // 郵便番号
                    svf.VrsOutn("ADDR", line, StringUtils.defaultString(appl._address1) + StringUtils.defaultString(appl._address2)); // 住所
                    svf.VrsOutn("NAME2", line, appl._gname); // 名前
                    svf.VrsOutn("TEL_NO", line, appl._gtelno); // 電話番号
                    svf.VrsOutn("JH_CODE", line, appl._fsCd); // 中学校コード
                    svf.VrsOutn("JH_NAME1", line, appl._finschoolName); // 中学校名
                    svf.VrsOutn("SCORE", line, appl._totalAll); // 内申135点満点
                    svf.VrsOutn("ABSENT4", line, appl._absenceDays3); // 欠席
                    // svf.VrsOutn("REMARK", line, null); // 備考
                }
                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

    private static class ReceptDat {
        final String _examno;
        final String _name;
        final String _nameKana;
        final String _birthday;
        final String _sex;
        final String _sexName;
        final String _zipcd;
        final String _address1;
        final String _address2;
        final String _gname;
        final String _gtelno;
        final String _fsCd;
        final String _finschoolName;
        final String _examcourseMark1;
        final String _examcourseMark2;
        final String _examcourseMark3;
        final String _examcourseMark4;
        final String _absenceDays3;
        final String _totalAll;

        ReceptDat(
            final String examno,
            final String name,
            final String nameKana,
            final String birthday,
            final String sex,
            final String sexName,
            final String zipcd,
            final String address1,
            final String address2,
            final String gname,
            final String gtelno,
            final String fsCd,
            final String finschoolName,
            final String examcourseMark1,
            final String examcourseMark2,
            final String examcourseMark3,
            final String examcourseMark4,
            final String absenceDays3,
            final String totalAll
        ) {
            _examno = examno;
            _name = name;
            _nameKana = nameKana;
            _birthday = birthday;
            _sex = sex;
            _sexName = sexName;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
            _gname = gname;
            _gtelno = gtelno;
            _fsCd = fsCd;
            _finschoolName = finschoolName;
            _examcourseMark1 = examcourseMark1;
            _examcourseMark2 = examcourseMark2;
            _examcourseMark3 = examcourseMark3;
            _examcourseMark4 = examcourseMark4;
            _absenceDays3 = absenceDays3;
            _totalAll = totalAll;
        }

        public static List load(final DB2UDB db2, final Param param, final String examCourseCd) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(param, examCourseCd));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");
                    final String name = rs.getString("NAME");
                    final String nameKana = rs.getString("NAME_KANA");
                    final String birthday = rs.getString("BIRTHDAY");
                    final String sex = rs.getString("SEX");
                    final String sexName = rs.getString("SEX_NAME");
                    final String zipcd = rs.getString("ZIPCD");
                    final String address1 = rs.getString("ADDRESS1");
                    final String address2 = rs.getString("ADDRESS2");
                    final String gname = rs.getString("GNAME");
                    final String gtelno = rs.getString("GTELNO");
                    final String fsCd = rs.getString("FS_CD");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String examcourseMark1 = rs.getString("EXAMCOURSE_MARK1");
                    final String examcourseMark2 = rs.getString("EXAMCOURSE_MARK2");
                    final String examcourseMark3 = rs.getString("EXAMCOURSE_MARK3");
                    final String examcourseMark4 = rs.getString("EXAMCOURSE_MARK4");
                    final String absenceDays3 = rs.getString("ABSENCE_DAYS3");
                    final String totalAll = rs.getString("TOTAL_ALL");
                    final ReceptDat receptdat = new ReceptDat(examno, name, nameKana, birthday, sex, sexName, zipcd, address1, address2, gname, gtelno, fsCd, finschoolName, examcourseMark1, examcourseMark2, examcourseMark3, examcourseMark4, absenceDays3, totalAll);
                    list.add(receptdat);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param, final String examCourseCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     BASE.EXAMNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     BASE.BIRTHDAY, ");
            stb.append("     BASE.SEX, ");
            stb.append("     NMZ002.NAME2 AS SEX_NAME, ");
            stb.append("     ADDR.ZIPCD, ");
            stb.append("     ADDR.ADDRESS1, ");
            stb.append("     ADDR.ADDRESS2, ");
            stb.append("     ADDR.GNAME, ");
            stb.append("     ADDR.GTELNO, ");
            stb.append("     BASE.FS_CD, ");
            stb.append("     T7.FINSCHOOL_NAME, ");
            stb.append("     T3C.EXAMCOURSE_MARK AS EXAMCOURSE_MARK1, ");
            stb.append("     T4C.EXAMCOURSE_MARK AS EXAMCOURSE_MARK2, ");
            stb.append("     T5C.EXAMCOURSE_MARK AS EXAMCOURSE_MARK3, ");
            stb.append("     T6C.EXAMCOURSE_MARK AS EXAMCOURSE_MARK4, ");
            stb.append("     T8.ABSENCE_DAYS3, ");
            stb.append("     T8.TOTAL_ALL ");
            stb.append(" FROM ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append(" LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("     AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("     AND ADDR.EXAMNO       = BASE.EXAMNO ");
            stb.append(" INNER JOIN ENTEXAM_WISHDIV_MST T3 ON T3.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("     AND T3.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("     AND T3.TESTDIV = BASE.TESTDIV ");
            stb.append("     AND T3.DESIREDIV = BASE.DESIREDIV ");
            stb.append("     AND T3.WISHNO = '1' ");
            stb.append(" LEFT JOIN ENTEXAM_COURSE_MST T3C ON T3C.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
            stb.append("     AND T3C.APPLICANTDIV = T3.APPLICANTDIV ");
            stb.append("     AND T3C.TESTDIV = T3.TESTDIV ");
            stb.append("     AND T3C.COURSECD = T3.COURSECD ");
            stb.append("     AND T3C.MAJORCD = T3.MAJORCD ");
            stb.append("     AND T3C.EXAMCOURSECD = T3.EXAMCOURSECD ");
            stb.append(" LEFT JOIN ENTEXAM_WISHDIV_MST T4 ON T4.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("     AND T4.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("     AND T4.TESTDIV = BASE.TESTDIV ");
            stb.append("     AND T4.DESIREDIV = BASE.DESIREDIV ");
            stb.append("     AND T4.WISHNO = '2' ");
            stb.append(" LEFT JOIN ENTEXAM_COURSE_MST T4C ON T4C.ENTEXAMYEAR = T4.ENTEXAMYEAR ");
            stb.append("     AND T4C.APPLICANTDIV = T4.APPLICANTDIV ");
            stb.append("     AND T4C.TESTDIV = T4.TESTDIV ");
            stb.append("     AND T4C.COURSECD = T4.COURSECD ");
            stb.append("     AND T4C.MAJORCD = T4.MAJORCD ");
            stb.append("     AND T4C.EXAMCOURSECD = T4.EXAMCOURSECD ");
            stb.append(" LEFT JOIN ENTEXAM_WISHDIV_MST T5 ON T5.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("     AND T5.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("     AND T5.TESTDIV = BASE.TESTDIV ");
            stb.append("     AND T5.DESIREDIV = BASE.DESIREDIV ");
            stb.append("     AND T5.WISHNO = '3' ");
            stb.append(" LEFT JOIN ENTEXAM_COURSE_MST T5C ON T5C.ENTEXAMYEAR = T5.ENTEXAMYEAR ");
            stb.append("     AND T5C.APPLICANTDIV = T5.APPLICANTDIV ");
            stb.append("     AND T5C.TESTDIV = T5.TESTDIV ");
            stb.append("     AND T5C.COURSECD = T5.COURSECD ");
            stb.append("     AND T5C.MAJORCD = T5.MAJORCD ");
            stb.append("     AND T5C.EXAMCOURSECD = T5.EXAMCOURSECD ");
            stb.append(" LEFT JOIN ENTEXAM_WISHDIV_MST T6 ON T6.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("     AND T6.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("     AND T6.TESTDIV = BASE.TESTDIV ");
            stb.append("     AND T6.DESIREDIV = BASE.DESIREDIV ");
            stb.append("     AND T6.WISHNO = '4' ");
            stb.append(" LEFT JOIN ENTEXAM_COURSE_MST T6C ON T6C.ENTEXAMYEAR = T6.ENTEXAMYEAR ");
            stb.append("     AND T6C.APPLICANTDIV = T6.APPLICANTDIV ");
            stb.append("     AND T6C.TESTDIV = T6.TESTDIV ");
            stb.append("     AND T6C.COURSECD = T6.COURSECD ");
            stb.append("     AND T6C.MAJORCD = T6.MAJORCD ");
            stb.append("     AND T6C.EXAMCOURSECD = T6.EXAMCOURSECD ");
            stb.append(" LEFT JOIN V_FINSCHOOL_MST T7 ON T7.YEAR = BASE.ENTEXAMYEAR AND T7.FINSCHOOLCD = BASE.FS_CD ");
            stb.append(" LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T8 ON T8.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("     AND T8.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("     AND T8.EXAMNO       = BASE.EXAMNO ");
            stb.append(" LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' ");
            stb.append("     AND NMZ002.NAMECD2 = BASE.SEX ");
            stb.append(" WHERE ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND BASE.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND T3.COURSECD || T3.MAJORCD || T3.EXAMCOURSECD = '" + examCourseCd + "' ");
            if ("1".equals(param._exclSenbatsu1)) {
                stb.append("     AND BASE.JUDGEMENT <> '4' "); // 選抜I合格者を除く
            }
            stb.append(" ORDER BY ");
            stb.append("     T3.EXAMCOURSECD, ");
            stb.append("     BASE.EXAMNO ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72172 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _courseMajorcd;
        final String _examcoursecd;
        final String _date;
        final String _exclSenbatsu1;

        final String _applicantdivName;
        final String _testdivName;
        final List _examcourseList;
        final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _courseMajorcd = request.getParameter("COURSE_MAJORCD");
            _examcoursecd = request.getParameter("EXAMCOURSECD");
            _examcourseList = getExamCourseList(db2, _examcoursecd);
            _date = request.getParameter("CTRL_DATE");
            _exclSenbatsu1 = request.getParameter("EXCL_SENBATSU1");

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testdivName = getNameMst(db2, "NAME1", "L004", _testdiv);
            _dateStr = getDateStr(_date);
        }

        private List getExamCourseList(final DB2UDB db2, final String field) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            List examcourseList = new ArrayList();
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT COURSECD || MAJORCD || EXAMCOURSECD AS EXAMCOURSECD, EXAMCOURSE_MARK ");
                sql.append(" FROM ENTEXAM_COURSE_MST ");
                sql.append(" WHERE ENTEXAMYEAR = '" + _entexamyear + "' ");
                sql.append("   AND APPLICANTDIV = '" + _applicantdiv + "' ");
                sql.append("   AND TESTDIV = '" + _testdiv + "' ");
                if ("9999".equals(_courseMajorcd)) {
                } else {
                    sql.append("   AND COURSECD || MAJORCD = '" + _courseMajorcd + "' ");
                }
                if ("9999".equals(_examcoursecd)) {
                } else if (GAcd.equals(_examcoursecd)) { //特進コース（GA）を選択した時は、難関コース（GS）を合わせて出力
                    sql.append("   AND EXAMCOURSECD in ('" + GScd + "', '" + GAcd + "') ");
                } else {
                    sql.append("   AND EXAMCOURSECD = '" + _examcoursecd + "' ");
                }
                sql.append(" ORDER BY COURSECD, MAJORCD, EXAMCOURSECD ");
                if (GAcd.equals(_examcoursecd)) {
                    sql.append(" desc ");
                }
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = new HashMap();
                    m.put("EXAMCOURSECD", rs.getString("EXAMCOURSECD"));
                    m.put("EXAMCOURSE_MARK", rs.getString("EXAMCOURSE_MARK"));
                    examcourseList.add(m);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return examcourseList;
        }

        private String getDateStr(final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(date);
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
