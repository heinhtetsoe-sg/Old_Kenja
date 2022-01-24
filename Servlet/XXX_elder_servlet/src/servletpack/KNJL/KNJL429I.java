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

import jp.co.alp.kenja.common.dao.SQLUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: d4dc6c4b2ea581c616b45bba7bd6a496c3e2c51a $
 */
public class KNJL429I {

    private static final Log log = LogFactory.getLog("KNJL429I.class");

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

        final int maxLine = 50;
        final Map applicant_Map = getApplicantMap(db2); //志願者Map

        if(applicant_Map.isEmpty()) {
            return false;
        }

        int page = 1; //ﾍﾟｰｼﾞ
        for (Iterator ite = applicant_Map.keySet().iterator(); ite.hasNext();) {
            final String hallKey = (String)ite.next();
            final Hall hall = (Hall)applicant_Map.get(hallKey);
            int line = 1; // 印字行
            int col = 1; // 列

            svf.VrSetForm("KNJL429I.frm", 1);
            svf.VrsOut("PAGE", String.valueOf(page) + "頁"); //ページ
            final String date = h_format_Seireki_MD(_param._date);
            svf.VrsOut("DATE", date + " " + _param._time); //作成日時
            svf.VrsOut("TITLE", "                " + _param._year + "年度 入学試験" + " " + _param._testAbbv + "    面接評価確認表"); //タイトル
            final String div = "1".equals(_param._disp) ? "(男女共)" : "2".equals(_param._disp) ? "(男子のみ)" : "(女子のみ)";
            svf.VrsOut("SUBTITLE", div); //サブタイトル
            svf.VrsOut("SCHOOL_NAME", "関西学院高等部"); //学校名
            for (Iterator hallite = hall._applicantMap.keySet().iterator(); hallite.hasNext();) {
                final String getKey = (String)hallite.next();
                final Applicant applicant = (Applicant)hall._applicantMap.get(getKey);

                if(line > maxLine) {
                    col++;
                    line = 1;
                }

                svf.VrsOutn("EXAM_NO" + col, line, applicant._examno); //受験番号
                svf.VrsOutn("SEX" + col, line, "1".equals(applicant._sex) ? "男" : "女"); //性別
                svf.VrsOutn("DIV" + col, line, applicant._interview); //評価

                line++;
            }
            svf.VrEndPage();
            page++;
        }

    return true;
    }

    // 志願者取得
    private Map getApplicantMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        Hall hall = null;

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.ENTEXAMYEAR, ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.EXAM_TYPE, ");
            stb.append("     T1.EXAMHALLCD, ");
            stb.append("     T1.EXAMHALL_NAME, ");
            stb.append("     T1.S_RECEPTNO, ");
            stb.append("     T1.E_RECEPTNO, ");
            stb.append("     T2.EXAMNO, ");
            stb.append("     T2.SEX, ");
            if("1".equals(_param._testDiv)) {
                stb.append("     T4.NAME1 || T5.NAME1 AS INTERVIEW ");
            } else {
                stb.append("     T4.NAME1 || T5.NAME1 || T6.NAME1 AS INTERVIEW ");
            }
            stb.append(" FROM ");
            stb.append("   ENTEXAM_HALL_YDAT T1 ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T2.APPLICANTDIV = T1.APPLICANTDIV AND T2.TESTDIV = T1.TESTDIV AND T2.EXAMNO BETWEEN T1.S_RECEPTNO AND T1.E_RECEPTNO ");
            if("2".equals(_param._disp)) {
                stb.append(" AND T2.SEX = '1' ");
            } else if("3".equals(_param._disp)) {
                stb.append(" AND T2.SEX = '2' ");
            }
            stb.append(" LEFT JOIN ENTEXAM_INTERVIEW_DAT T3 ON T3.ENTEXAMYEAR = T2.ENTEXAMYEAR AND T3.APPLICANTDIV = T2.APPLICANTDIV AND T3.TESTDIV = T3.TESTDIV AND T3.EXAMNO = T2.EXAMNO ");
            stb.append(" LEFT JOIN ENTEXAM_SETTING_MST T4 ON T4.ENTEXAMYEAR = T3.ENTEXAMYEAR AND T4.APPLICANTDIV = T3.APPLICANTDIV AND T4.SETTING_CD = 'LH27' AND T4.SEQ = T3.INTERVIEW_A ");
            stb.append(" LEFT JOIN ENTEXAM_SETTING_MST T5 ON T5.ENTEXAMYEAR = T3.ENTEXAMYEAR AND T5.APPLICANTDIV = T3.APPLICANTDIV AND T5.SETTING_CD = 'LH27' AND T5.SEQ = T3.INTERVIEW_B ");
            if("2".equals(_param._testDiv)) {
                stb.append(" LEFT JOIN ENTEXAM_SETTING_MST T6 ON T6.ENTEXAMYEAR = T3.ENTEXAMYEAR AND T6.APPLICANTDIV = T3.APPLICANTDIV AND T6.SETTING_CD = 'LH27' AND T6.SEQ = T3.INTERVIEW_C ");
            }
            stb.append(" WHERE ");
            stb.append("   T1.ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("   AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("   AND T1.TESTDIV = '" + _param._testDiv + "' ");
            stb.append("   AND T1.EXAM_TYPE = '1' ");
            stb.append("   AND T1.EXAMHALLCD IN " + SQLUtils.whereIn(true, _param._groupSelected));
            stb.append("   AND VALUE(T2.JUDGEMENT, '') <> '5' ");
            stb.append(" ORDER BY ");
            stb.append("   T1.EXAMHALLCD,T2.EXAMNO ");

            log.debug(" applicant sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String entexamyear = rs.getString("ENTEXAMYEAR");
                final String applicantdiv = rs.getString("APPLICANTDIV");
                final String testdiv = rs.getString("TESTDIV");
                final String exam_Type = rs.getString("EXAM_TYPE");
                final String examhallcd = "ALL";
                final String examhall_Name = "";
                final String s_Receptno = rs.getString("S_RECEPTNO");
                final String e_Receptno = rs.getString("E_RECEPTNO");
                final String examno = rs.getString("EXAMNO");
                final String sex = rs.getString("SEX");
                final String interview = rs.getString("INTERVIEW");

                final Applicant applicant = new Applicant(entexamyear, applicantdiv, testdiv, exam_Type, examhallcd, examhall_Name, s_Receptno, e_Receptno, examno, sex, interview);

                if (retMap.containsKey(examhallcd )) {
                    hall = (Hall)retMap.get(examhallcd);
                } else {
                    hall = new Hall(entexamyear, applicantdiv, testdiv, exam_Type, examhallcd, examhall_Name, s_Receptno, e_Receptno);
                    retMap.put(examhallcd, hall);
                }

                if(!hall._applicantMap.containsKey(examno)) {
                    hall._applicantMap.put(examno, applicant);
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
        final String _exam_Type;
        final String _examhallcd;
        final String _examhall_Name;
        final String _s_Receptno;
        final String _e_Receptno;
        final String _examno;
        final String _sex;
        final String _interview;

        public Applicant(final String entexamyear, final String applicantdiv, final String testdiv,
                final String exam_Type, final String examhallcd, final String examhall_Name, final String s_Receptno,
                final String e_Receptno, final String examno, final String sex, final String interview) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _exam_Type = exam_Type;
            _examhallcd = examhallcd;
            _examhall_Name = examhall_Name;
            _s_Receptno = s_Receptno;
            _e_Receptno = e_Receptno;
            _examno = examno;
            _sex = sex;
            _interview = interview;
        }
    }

    private class Hall {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _exam_Type;
        final String _examhallcd;
        final String _examhall_Name;
        final String _s_Receptno;
        final String _e_Receptno;
        final Map _applicantMap;

        public Hall(final String entexamyear, final String applicantdiv, final String testdiv, final String exam_Type,
                final String examhallcd, final String examhall_Name, final String s_Receptno, final String e_Receptno) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _exam_Type = exam_Type;
            _examhallcd = examhallcd;
            _examhall_Name = examhall_Name;
            _s_Receptno = s_Receptno;
            _e_Receptno = e_Receptno;
            _applicantMap = new LinkedMap();
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
        final String _disp;  //抽出区分　1:全員 2:男子のみ 3:女子のみ
        final String[] _groupSelected; //会場コード
        final String _date;
        final String _time;
        final String _schoolKind;
        final String _testAbbv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _disp = request.getParameter("DISP");
            _schoolKind = request.getParameter("SCHOOLKIND");
              _date = request.getParameter("DATE");
              _time = request.getParameter("TIME");
              _groupSelected = request.getParameterValues("GROUP_SELECTED");
              _testAbbv = getTestAbbv(db2);
        }

        private String getTestAbbv(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTDIV_ABBV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _year + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' "));
        }
    }

    private String h_format_Seireki_MD(final String date) {
        if (null == date || "".equals(date)) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        String retVal = "";
        sdf.applyPattern("yyyy年M月d日");
        retVal = sdf.format(java.sql.Date.valueOf(date));

        return retVal;
    }
}

// eof
