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

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 */
public class KNJL412I {

    private static final Log log = LogFactory.getLog("KNJL412I.class");

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

        final Map applicantMap = getApplicantMap(db2); // 志願者Map

        if (applicantMap.isEmpty()) {
            return false;
        }

        log.debug(applicantMap.size());
        final int maxLine = 50; // 最大印字行
        int page = 0; // ページ
        int line = 1; // 印字行
        int no = 1; // 連番

        for (Iterator ite = applicantMap.keySet().iterator(); ite.hasNext();) {
            final String Key = (String) ite.next();
            final Applicant applicant = (Applicant) applicantMap.get(Key);

            if (line > maxLine || page == 0) {
                if (line > maxLine)
                    svf.VrEndPage();
                page++;
                line = 1;
                svf.VrSetForm("KNJL412I.frm", 1);
                svf.VrsOut("PAGE", String.valueOf(page) + "頁"); // ページ

                final String date = h_format_Seireki_MD(_param._date);
                svf.VrsOut("DATE", date + " " + _param._time); // 作成日付

                final String div = "1".equals(_param._outputDiv) ? "男子のみ" : "2".equals(_param._outputDiv) ? "女子のみ" : "男女共";
                final String order = "1".equals(_param._sortDiv) ? "受験番号順" : "2".equals(_param._sortDiv) ? "氏名カナ順" : "出身校コード順";
                svf.VrsOut("TITLE", _param._year + "年度　入学試験　" + _param._testAbbv + "　　合格者・手続者名簿" + " (" + div + ")" + order); // タイトル
                svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
            }

            svf.VrsOutn("NO", line, String.valueOf(no)); // No
            svf.VrsOutn("EXAM_NO1", line, applicant._examno); // 受験番号1
            final String fieldName = getFieldName(applicant._name);
            svf.VrsOutn("NAME" + fieldName, line, applicant._name); // 氏名
            final String fieldKana = getFieldName(applicant._name_Kana);
            svf.VrsOutn("KANA" + fieldKana, line, applicant._name_Kana); // フリガナ
            final String finSchool_Name = deleteShogakkou(applicant._finschool_Name);
            final String fieldSchool = getFieldSchoolName(finSchool_Name);
            svf.VrsOutn("FINSCHOOL_NAME" + fieldSchool, line, finSchool_Name); // 学校名称

            if(applicant._prefName != null) {
                svf.VrsOutn("PREF_NAME", line, "(" + applicant._prefName + ")"); // 都道府県
            }
            svf.VrsOutn("PROCESS1", line, "1".equals(applicant._remark1) ? "○" : ""); // 1次
            svf.VrsOutn("PROCESS2", line, "1".equals(applicant._procedurediv) ? "○" : ""); // 2次
            if ((applicant._remark1 != null) || (applicant._procedurediv != null)) {
                svf.VrsOutn("PROCESS3", line, "1".equals(applicant._remark3) ? "〇" : "欠席"); // 招集
            }
            svf.VrsOutn("PROCESS4", line, "2".equals(applicant._entdiv) ? "辞退" : ""); // 入辞

            line++; // 印字行
            no++; // 連番
        }
        svf.VrsOut("FOOTER", "以上" + String.valueOf(applicantMap.size()) + "名"); // 受験番号2
        svf.VrEndPage();

        return true;
    }

    private String deleteShogakkou(String finschool_Name) {
        if (StringUtils.isBlank(finschool_Name) || !finschool_Name.endsWith("小学校")) {
            return finschool_Name;
        }
        return finschool_Name.substring(0, finschool_Name.length() - "小学校".length());
    }

    private String getFieldSchoolName(final String str) {
        final int keta = KNJ_EditEdit.getMS932ByteLength(str);
        return keta <= 30 ? "1" : "2";
    }

    private String getFieldName(final String str) {
        final int keta = KNJ_EditEdit.getMS932ByteLength(str);
        return keta <= 20 ? "1" : keta <= 30 ? "2" : "3";
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
            stb.append("   BASE.NAME, ");
            stb.append("   BASE.NAME_KANA, ");
            stb.append("   BASE.FS_CD, ");
            stb.append("   SCHOOL.FINSCHOOL_NAME, ");
            stb.append("   SCHOOL.FINSCHOOL_PREF_CD, ");
            stb.append("   PREF.PREF_NAME, ");
            stb.append("   BASE.JUDGEMENT, ");
            stb.append("   DTL.REMARK1, ");
            stb.append("   DTL.REMARK2, ");
            stb.append("   BASE.PROCEDUREDIV, ");
            stb.append("   BASE.PROCEDUREDATE, ");
            stb.append("   DTL.REMARK3, ");
            stb.append("   BASE.ENTDIV ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append(" LEFT JOIN ");
            stb.append("   FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ");
            stb.append(" LEFT JOIN ");
            stb.append("   PREF_MST PREF ON PREF.PREF_CD = SCHOOL.FINSCHOOL_PREF_CD ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT DTL ON DTL.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND DTL.APPLICANTDIV = BASE.APPLICANTDIV AND DTL.EXAMNO = BASE.EXAMNO AND DTL.SEQ = '022' ");
            stb.append(" WHERE ");
            stb.append("   BASE.ENTEXAMYEAR = '" + _param._year + "' AND ");
            stb.append("   BASE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("   BASE.TESTDIV = '" + _param._testDiv + "' AND ");
            if(!"3".equals(_param._outputDiv)) {
                stb.append(" BASE.SEX = '" + _param._outputDiv + "' AND ");
            }
            stb.append("   BASE.JUDGEMENT = '1' ");
            stb.append(" ORDER BY ");
            if("1".equals(_param._sortDiv)) {
                stb.append("  BASE.EXAMNO ");
            } else if("2".equals(_param._sortDiv)) {
                stb.append("  BASE.NAME_KANA,BASE.EXAMNO ");
            } else {
                stb.append("  BASE.FS_CD,BASE.EXAMNO ");
            }

            log.debug(" applicant sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String entexamyear = rs.getString("ENTEXAMYEAR");
                final String applicantdiv = rs.getString("APPLICANTDIV");
                final String testdiv = rs.getString("TESTDIV");
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String name_Kana = rs.getString("NAME_KANA");
                final String fs_Cd = rs.getString("FS_CD");
                final String finschool_Name = rs.getString("FINSCHOOL_NAME");
                final String finschool_PrefCd = rs.getString("FINSCHOOL_PREF_CD");
                final String prefName = rs.getString("PREF_NAME");
                final String judgement = rs.getString("JUDGEMENT");
                final String remark1 = rs.getString("REMARK1");
                final String remark2 = rs.getString("REMARK2");
                final String procedurediv = rs.getString("PROCEDUREDIV");
                final String proceduredate = rs.getString("PROCEDUREDATE");
                final String remark3 = rs.getString("REMARK3");
                final String entdiv = rs.getString("ENTDIV");

                final Applicant applicant = new Applicant(entexamyear, applicantdiv, testdiv, examno, name, name_Kana, fs_Cd, finschool_Name, finschool_PrefCd, prefName, judgement, remark1, remark2, procedurediv, proceduredate, remark3, entdiv);

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

    private class Applicant {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _examno;
        final String _name;
        final String _name_Kana;
        final String _fs_Cd;
        final String _finschool_Name;
        final String _finschool_PrefCd;
        final String _prefName;
        final String _judgement;
        final String _remark1; // 1次手続きフラグ
        final String _remark2; // 1次手続き日
        final String _procedurediv; // 2次手続きフラグ
        final String _proceduredate; // 2次手続き日
        final String _remark3; // 招集日
        final String _entdiv; // 入辞区分


        public Applicant(final String entexamyear, final String applicantdiv, final String testdiv, final String examno,
                final String name, final String name_Kana, final String fs_Cd, final String finschool_Name,
                final String finschool_PrefCd, final String prefName, final String judgement, final String remark1,
                final String remark2, final String procedurediv, final String proceduredate, final String remark3, final String entdiv) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _examno = examno;
            _name = name;
            _name_Kana = name_Kana;
            _fs_Cd = fs_Cd;
            _finschool_Name = finschool_Name;
            _finschool_PrefCd = finschool_PrefCd;
            _prefName = prefName;
            _judgement = judgement;
            _remark1 = remark1;
            _remark2 = remark2;
            _procedurediv = procedurediv;
            _proceduredate = proceduredate;
            _remark3 = remark3;
            _entdiv = entdiv;

        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _applicantDiv; //入試制度
        final String _testDiv; //入試区分
        final String _outputDiv;
        final String _sortDiv;
        final String _testAbbv;
        final String _date;
        final String _time;
        final String _schoolKind;
        final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _outputDiv = request.getParameter("OUTPUT_DIV");
            _sortDiv = request.getParameter("SORT_DIV");
            _schoolKind = request.getParameter("SCHOOLKIND");
              _date = request.getParameter("CTRL_DATE");
              _time = request.getParameter("TIME");
              _testAbbv = getTestDivAbbv(db2);
              _schoolName = getSchoolName(db2);

        }

        private String getTestDivAbbv(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTDIV_ABBV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _year + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' "));
        }

        private String getSchoolName(final DB2UDB db2) {
            final String kindcd = "1".equals(_applicantDiv) ? "105" : "106";
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + kindcd + "' "));

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
