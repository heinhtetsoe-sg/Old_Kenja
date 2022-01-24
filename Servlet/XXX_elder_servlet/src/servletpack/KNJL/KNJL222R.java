/*
 * $Id: 062ac0eab0f31e78991de91951b249637b27f264 $
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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
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
 *                  ＜ＫＮＪＬ２２２Ｒ＞  専願者名簿
 **/
public class KNJL222R {

    private static final Log log = LogFactory.getLog(KNJL222R.class);

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
		final List list = ApplicantBefore.load(db2, _param);
		if ("1".equals(_param._testdiv)) {
		    printMain1(svf, list);
		} else if ("2".equals(_param._testdiv)) {
            printMain2(svf, list);
		}
    }
    
    public void printMain1(final Vrw32alp svf, final List list) {
        final String form = "KNJL222R_1.frm";
        final int maxLine = 50;
        
        for (int i = 0; i < list.size(); i++) {
            final ApplicantBefore appl = (ApplicantBefore) list.get(i);
            if (i % maxLine == 0) {
                if (i > 0) {
                    svf.VrEndPage();
                }
                svf.VrSetForm(form, 1);

                svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + _param._testdivAbbv2 + "者名簿"); // タイトル
                svf.VrsOut("DATE", _param._dateStr);
            }
            
            final int line = i % maxLine + 1;
            svf.VrsOutn("NO", line, String.valueOf(i + 1)); // NO
            svf.VrsOutn("PAGE", line, appl._beforePage); // ページ
            svf.VrsOutn("NUMBER", line, String.valueOf(Integer.parseInt(appl._beforeSeq))); // 番号
            svf.VrsOutn("JH_CODE", line, appl._fsCd); // 中学校コード
            svf.VrsOutn("JH_NAME", line, appl._finschoolName); // 中学校名
            svf.VrsOutn("CONSENT_COURSE", line, appl._examcourseMark); // 内諾コース
            svf.VrsOutn("NAME1", line, appl.split(appl._name)[0]); // 名前
            svf.VrsOutn("NAME2", line, appl.split(appl._name)[1]); // 名前
            svf.VrsOutn("KANA1", line, appl.split(appl._nameKana)[0]); // 名前
            svf.VrsOutn("KANA2", line, appl.split(appl._nameKana)[1]); // 名前
            svf.VrsOutn("SEX", line, appl._sexName); // 性別
            svf.VrsOutn("SCORE1", line, appl._naisin2); // 75点満点
            svf.VrsOutn("SCORE2", line, appl._naisin3); // 135点満点
            svf.VrsOutn("ABSENCE1", line, appl._attend1); // 欠席
            svf.VrsOutn("ABSENCE2", line, appl._attend2); // 欠席
            svf.VrsOutn("ABSENCE3", line, appl._attend3); // 欠席
            svf.VrsOutn("ABSENT", line, appl._attendTotal); // 欠席
            final String nankanStr = "1".equals(appl._nankanFlg) ? "有" : "";
            svf.VrsOutn("DIF_COURSE", line, nankanStr); // 難関コース希望
            svf.VrsOutn("SCHOLARSHIP", line, appl._scholarshipName); // 奨学生
            svf.VrsOutn("REMARK", line, appl._remark); // 備考
            
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }
    
    public void printMain2(final Vrw32alp svf, final List list) {
        final String form = "KNJL222R_2.frm";
        final int maxLine = 50;
        
        for (int i = 0; i < list.size(); i++) {
            final ApplicantBefore appl = (ApplicantBefore) list.get(i);
            if (i % maxLine == 0) {
                if (i > 0) {
                    svf.VrEndPage();
                }
                svf.VrSetForm(form, 1);

                svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + _param._testdivAbbv2 + "者名簿"); // タイトル
                svf.VrsOut("DATE", _param._dateStr);
            }
            
            final int line = i % maxLine + 1;
            svf.VrsOutn("NO1", line, String.valueOf(i + 1)); // NO
            svf.VrsOutn("PAGE1", line, appl._beforePage); // ページ
            svf.VrsOutn("NUMBER1", line, String.valueOf(Integer.parseInt(appl._beforeSeq))); // 番号
            svf.VrsOutn("JH_CODE1", line, appl._fsCd); // 中学校コード
            svf.VrsOutn("JH_NAME1", line, appl._finschoolName); // 中学校名
            svf.VrsOutn("CONSENT_COURSE1", line, appl._examcourseMark); // 内諾コース
            
            svf.VrsOutn("NAME1", line, appl.split(appl._name)[0]); // 姓
            svf.VrsOutn("NAME2", line, appl.split(appl._name)[1]); // 名
            svf.VrsOutn("KANA1", line, appl.split(appl._nameKana)[0]); // ふりがな姓
            svf.VrsOutn("KANA2", line, appl.split(appl._nameKana)[1]); // ふりがな名
            svf.VrsOutn("SEX1", line, appl._sexName); // 性別
            svf.VrsOutn("SCORE1", line, appl._naisin1); // 25点満点
            svf.VrsOutn("SCORE2", line, appl._naisin2); // 75点満点
            svf.VrsOutn("SCORE3", line, appl._naisin3); // 135点満点
            svf.VrsOutn("ABSENT1", line, appl._attend1); // 欠席
            svf.VrsOutn("ABSENT2", line, appl._attend2); // 欠席
            svf.VrsOutn("ABSENT3", line, appl._attend3); // 欠席
            svf.VrsOutn("ABSENT4", line, appl._attendTotal); // 欠席
            svf.VrsOutn("SELECT1_1", line, appl._senbatu1School); // 選抜1
            svf.VrsOutn("SELECT1_2", line, appl._senbatu1Major); // 選抜1
            svf.VrsOutn("SELECT2_1", line, appl._senbatu2School); // 選抜2
            svf.VrsOutn("SELECT2_2", line, appl._senbatu2Major); // 選抜2
            final String nankanStr = "1".equals(appl._nankanFlg) ? "有" : "";
            svf.VrsOutn("DIF_COURSE", line, nankanStr); // 難関コース希望
            svf.VrsOutn("SCHOLARSHIP", line, appl._scholarshipName); // 奨学生
            svf.VrsOutn("REMARK", line, appl._remark); // 備考
            
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }
    
    private static class ApplicantBefore {
        final String _beforePage;
        final String _beforeSeq;
        final String _examcourseName;
        final String _examcourseAbbv;
        final String _examcourseMark;
        final String _fsCd;
        final String _finschoolName;
        final String _name;
        final String _nameKana;
        final String _sex;
        final String _sexName;
        final String _naisin1;
        final String _naisin2;
        final String _naisin3;
        final String _attend1;
        final String _attend2;
        final String _attend3;
        final String _attendTotal;
        final String _senbatu1School;
        final String _senbatu1Major;
        final String _senbatu2School;
        final String _senbatu2Major;
        final String _nankanFlg;
        final String _scholarship;
        final String _scholarshipName;
        final String _remark;
        
        ApplicantBefore(
            final String beforePage,
            final String beforeSeq,
            final String examcourseName,
            final String examcourseAbbv,
            final String examcourseMark,
            final String fsCd,
            final String finschoolName,
            final String name,
            final String nameKana,
            final String sex,
            final String sexName,
            final String naisin1,
            final String naisin2,
            final String naisin3,
            final String attend1,
            final String attend2,
            final String attend3,
            final String attendTotal,
            final String senbatu1School,
            final String senbatu1Major,
            final String senbatu2School,
            final String senbatu2Major,
            final String nankanFlg,
            final String scholarship,
            final String scholarshipName,
            final String remark
        ) {
            _beforePage = beforePage;
            _beforeSeq = beforeSeq;
            _examcourseName = examcourseName;
            _examcourseAbbv = examcourseAbbv;
            _examcourseMark = examcourseMark;
            _fsCd = fsCd;
            _finschoolName = finschoolName;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _sexName = sexName;
            _naisin1 = naisin1;
            _naisin2 = naisin2;
            _naisin3 = naisin3;
            _attend1 = attend1;
            _attend2 = attend2;
            _attend3 = attend3;
            _attendTotal = attendTotal;
            _senbatu1School = senbatu1School;
            _senbatu1Major = senbatu1Major;
            _senbatu2School = senbatu2School;
            _senbatu2Major = senbatu2Major;
            _nankanFlg = nankanFlg;
            _scholarship = scholarship;
            _scholarshipName = scholarshipName;
            _remark = remark;
        }
        
        public String[] split(String s) {
            if (null == s) {
                return new String[] {null, null};
            }
            int idx = s.indexOf('　');
            if (-1 == idx) {
                return new String[] {s, null};
            }
            return new String[] {s.substring(0, idx), s.substring(idx + 1)};
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
                    final String beforePage = rs.getString("BEFORE_PAGE");
                    final String beforeSeq = rs.getString("BEFORE_SEQ");
                    final String examcourseName = rs.getString("EXAMCOURSE_NAME");
                    final String examcourseAbbv = rs.getString("EXAMCOURSE_ABBV");
                    final String examcourseMark = rs.getString("EXAMCOURSE_MARK");
                    final String fsCd = rs.getString("FS_CD");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String name = rs.getString("NAME");
                    final String nameKana = rs.getString("NAME_KANA");
                    final String sex = rs.getString("SEX");
                    final String sexName = rs.getString("SEX_NAME");
                    final String naisin1 = rs.getString("NAISIN1");
                    final String naisin2 = rs.getString("NAISIN2");
                    final String naisin3 = rs.getString("NAISIN3");
                    final String attend1 = rs.getString("ATTEND1");
                    final String attend2 = rs.getString("ATTEND2");
                    final String attend3 = rs.getString("ATTEND3");
                    final String attendTotal = rs.getString("ATTEND_TOTAL");
                    final String senbatu1School = rs.getString("SENBATU1_SCHOOL");
                    final String senbatu1Major = rs.getString("SENBATU1_MAJOR");
                    final String senbatu2School = rs.getString("SENBATU2_SCHOOL");
                    final String senbatu2Major = rs.getString("SENBATU2_MAJOR");
                    final String nankanFlg = rs.getString("NANKAN_FLG");
                    final String scholarship = rs.getString("SCHOLARSHIP");
                    final String scholarshipName = rs.getString("SCHOLARSHIP_NAME");
                    final String recomFlgName = rs.getString("RECOM_FLG_NAME");
                    final String recomRemark = null == rs.getString("RECOM_FLG") || null == rs.getString("RECOM_REMARK") ? "" : "（" + rs.getString("RECOM_REMARK") + "）" ;
                    final String rsRemark = rs.getString("REMARK");
                    final String remark = StringUtils.defaultString(recomFlgName) + StringUtils.defaultString(recomRemark) + StringUtils.defaultString(rsRemark);
                    final ApplicantBefore applicantbefore = new ApplicantBefore(beforePage, beforeSeq, examcourseName, examcourseAbbv, examcourseMark, fsCd, finschoolName, name, nameKana, sex, sexName, naisin1, naisin2, naisin3, attend1, attend2, attend3, attendTotal, senbatu1School, senbatu1Major, senbatu2School, senbatu2Major, nankanFlg, scholarship, scholarshipName, remark);
                    list.add(applicantbefore);
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
            stb.append("     T1.BEFORE_PAGE, ");
            stb.append("     T1.BEFORE_SEQ, ");
            stb.append("     T2.EXAMCOURSE_NAME, ");
            stb.append("     T2.EXAMCOURSE_ABBV, ");
            stb.append("     T2.EXAMCOURSE_MARK, ");
            stb.append("     T1.FS_CD, ");
            stb.append("     T3.FINSCHOOL_NAME, ");
            stb.append("     T1.NAME, ");
            stb.append("     T1.NAME_KANA, ");
            stb.append("     T1.SEX, ");
            stb.append("     NMZ002.NAME2 AS SEX_NAME, ");
            stb.append("     T1.NAISIN1, ");
            stb.append("     T1.NAISIN2, ");
            stb.append("     T1.NAISIN3, ");
            stb.append("     T1.ATTEND1, ");
            stb.append("     T1.ATTEND2, ");
            stb.append("     T1.ATTEND3, ");
            stb.append("     T1.ATTEND_TOTAL, ");
            stb.append("     T1.SENBATU1_SCHOOL, ");
            stb.append("     T1.SENBATU1_MAJOR, ");
            stb.append("     T1.SENBATU2_SCHOOL, ");
            stb.append("     T1.SENBATU2_MAJOR, ");
            stb.append("     T1.NANKAN_FLG, ");
            stb.append("     T1.SCHOLARSHIP, ");
            stb.append("     NML031.NAME1 AS SCHOLARSHIP_NAME, ");
            stb.append("     T1.RECOM_FLG, ");
            stb.append("     NML032.NAME2 AS RECOM_FLG_NAME, ");
            stb.append("     T1.RECOM_REMARK, ");
            stb.append("     T1.REMARK ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANT_BEFORE_DAT T1 ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T2.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T2.COURSECD = T1.BEFORE_COURSECD ");
            stb.append("         AND T2.MAJORCD = T1.BEFORE_MAJORCD ");
            stb.append("         AND T2.EXAMCOURSECD = T1.BEFORE_EXAMCOURSECD ");
            stb.append("     LEFT JOIN V_FINSCHOOL_MST T3 ON T3.YEAR = T1.ENTEXAMYEAR AND T3.FINSCHOOLCD = T1.FS_CD ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' ");
            stb.append("         AND NMZ002.NAMECD2 = T1.SEX ");
            stb.append("     LEFT JOIN NAME_MST NML031 ON NML031.NAMECD1 = 'L031' ");
            stb.append("         AND NML031.NAMECD2 = T1.SCHOLARSHIP ");
            stb.append("     LEFT JOIN NAME_MST NML032 ON NML032.NAMECD1 = 'L032' ");
            stb.append("         AND NML032.NAMECD2 = T1.RECOM_FLG ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + param._testdiv + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.BEFORE_PAGE, ");
            stb.append("     T1.BEFORE_SEQ ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71521 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;

        final String _dateStr;
        final String _testdivAbbv2;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE");
            _dateStr = getDateStr(_date);
            
            _testdivAbbv2 = StringUtils.defaultString(getNameMst(db2, "ABBV2", "L004", _testdiv));
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

