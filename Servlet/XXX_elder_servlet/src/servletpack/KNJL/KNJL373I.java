// kanji=漢字
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 */
public class KNJL373I {

    private static final Log log = LogFactory.getLog("KNJL373I.class");

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

        final Map district_Map = getApplicantMap(db2); //志願者Map

        if(district_Map.isEmpty()) {
            return false;
        }

        final int MaxLine = 50;
        int page = 0; // ページ

        for (Iterator ite = district_Map.keySet().iterator(); ite.hasNext();) {
            int line = 0; // 印字行
            final String Key = (String)ite.next();
            final District district = (District)district_Map.get(Key);
            for (Iterator printite = district._applicantMap.keySet().iterator(); printite.hasNext();) {
                final String getKey = (String)printite.next();
                final Applicant applicant = (Applicant)district._applicantMap.get(getKey);
                if (line > MaxLine || line == 0) {
                    if (line > MaxLine) svf.VrEndPage();
                    svf.VrSetForm("KNJL373I.frm", 1);
                    line = 1;
                    page++;
                    svf.VrsOut("PAGE", String.valueOf(page) + "頁"); //ページ
                    final String date = _param._date != null ? _param._date.replace("-", "/") : "";
                    svf.VrsOut("DATE", "作成日時：" + date + " " + _param._time); //作成日時
                    final String div = "1".equals(_param._disp) ? "男女共" : "2".equals(_param._disp) ? "男子" : "女子";
                    svf.VrsOut("TITLE", applicant._testabbv + " " + _param._year + "年度入学試験 小学校別志願者名簿《出願者》(" + div + ")" ); //タイトル
                    svf.VrsOut("SCHOOL_NAME", "関西学院 中学部"); //学校名
                    svf.VrsOut("DISTRICT_NAME", district._name1); //地区名
                }
                svf.VrsOutn("FINSCHOOL_DIV", line, "1".equals(applicant._finschool_Div) ? "国" : "2".equals(applicant._finschool_Div) ? "公" : "3".equals(applicant._finschool_Div) ? "私" : "" ); //設置
                final String nameField = getFieldName(applicant._name);
                svf.VrsOutn("NAME" + nameField, line, applicant._name); //氏名
                final String kanaField = getFieldName(applicant._name_Kana);
                svf.VrsOutn("KANA" + kanaField, line, applicant._name_Kana); //フリガナ
                String finschoolName = applicant._finschool_Name;
                if(finschoolName.endsWith("小学校")) finschoolName = finschoolName.replace("小学校", ""); //末尾が"小学校"の場合、カット
                svf.VrsOutn("FINSCHOOL_NAME", line, finschoolName); //出身学校
                svf.VrsOutn("SEX", line, "1".equals(applicant._sex) ? "男" : "女"); //性別
                svf.VrsOutn("EXAM_NO1", line, applicant._examno); //受験番号
                svf.VrsOutn("JUDGE1", line, "1".equals(applicant._judgement) ? "合" : "不");
                svf.VrsOutn("EXAM_NO2", line, applicant._recom_Examno); //他方式受験番号
                if(applicant._recom_Examno != null) {
                    svf.VrsOutn("JUDGE2", line, "1".equals(applicant._judgement2) ? "合" : "不");
                }

                line++;
            }
            svf.VrEndPage();
        }

    return true;
    }

    private String getFieldName(final String str) {
        final int keta = KNJ_EditEdit.getMS932ByteLength(str);
        return keta <= 20 ? "1" : keta <= 30 ? "2" : "3" ;
    }

    // 志願者取得
    private Map getApplicantMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        District district = null;

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.ENTEXAMYEAR, ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T4.TESTDIV_ABBV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.NAME, ");
            stb.append("     T1.NAME_KANA, ");
            stb.append("     T1.SEX, ");
            stb.append("     T1.FS_CD, ");
            stb.append("     T2.FINSCHOOL_NAME, ");
            stb.append("     T2.FINSCHOOL_DIV, ");
            stb.append("     T2.DISTRICTCD, ");
            stb.append("     T3.NAME1, ");
            stb.append("     T1.JUDGEMENT, ");
            stb.append("     T1.RECOM_EXAMNO, ");
            stb.append("     (SELECT S1.JUDGEMENT FROM ENTEXAM_APPLICANTBASE_DAT S1 WHERE S1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND S1.APPLICANTDIV = T1.APPLICANTDIV AND S1.TESTDIV <> T1.TESTDIV AND S1.EXAMNO = T1.RECOM_EXAMNO) AS JUDGEMENT2 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1   ");
            stb.append(" LEFT JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T1.FS_CD ");
            stb.append(" LEFT JOIN V_NAME_MST T3 ON T3.YEAR = '" + _param._year + "' AND T3.NAMECD1 = 'Z003' AND T3.NAMECD2 = T2.DISTRICTCD ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_TESTDIV_MST T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T4.APPLICANTDIV = T1.APPLICANTDIV AND T4.TESTDIV = T1.TESTDIV ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "'");
            if(Integer.parseInt(_param._disp) > 1) {
                final String sex = String.valueOf(Integer.parseInt(_param._disp) - 1);
                stb.append("     AND T1.SEX = '" + sex + "' "); //1:男子のみ 2:女子のみ
            }
            if(!"".equals(_param._districtCd)) {
                stb.append("     AND T2.DISTRICTCD = '" + _param._districtCd + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T2.DISTRICTCD,T1.FS_CD,T1.EXAMNO ");

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
                final String sex = rs.getString("SEX");
                final String fs_Cd = rs.getString("FS_CD");
                final String finschool_Name = rs.getString("FINSCHOOL_NAME");
                final String finschool_Div = rs.getString("FINSCHOOL_DIV");
                final String districtcd = rs.getString("DISTRICTCD");
                final String name1 = rs.getString("NAME1");
                final String judgement = rs.getString("JUDGEMENT");
                final String recom_Examno = rs.getString("RECOM_EXAMNO");
                final String judgement2 = rs.getString("JUDGEMENT2");
                final String testabbv = rs.getString("TESTDIV_ABBV");

                final Applicant applicant = new Applicant(entexamyear, applicantdiv, testdiv, examno, name, name_Kana, sex, fs_Cd, finschool_Name, finschool_Div, districtcd, name1, judgement, recom_Examno,judgement2,testabbv);
                if (retMap.containsKey(districtcd )) {
                    district = (District)retMap.get(districtcd);
                } else {
                    district = new District(districtcd, name1);
                    retMap.put(districtcd, district);
                }

                if(!district._applicantMap.containsKey(examno)) {
                    district._applicantMap.put(examno, applicant);
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
        final String _sex;
        final String _fs_Cd;
        final String _finschool_Name;
        final String _finschool_Div;
        final String _districtcd;
        final String _name1;
        final String _judgement;
        final String _recom_Examno;
        final String _judgement2;
        final String _testabbv;

        public Applicant(final String entexamyear, final String applicantdiv, final String testdiv, final String examno,
                final String name, final String name_Kana, final String sex, final String fs_Cd,
                final String finschool_Name, final String finschool_Div, final String districtcd, final String name1,
                final String judgement, final String recom_Examno, final String judgement2, final String testabbv) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _examno = examno;
            _name = name;
            _name_Kana = name_Kana;
            _sex = sex;
            _fs_Cd = fs_Cd;
            _finschool_Name = finschool_Name;
            _finschool_Div = finschool_Div;
            _districtcd = districtcd;
            _name1 = name1;
            _judgement = judgement;
            _recom_Examno = recom_Examno;
            _judgement2 = judgement2;
            _testabbv = testabbv;

        }
    }

    private class District {
        final String _districtcd;
        final String _name1;
        final Map _applicantMap;

        public District(final String districtcd, final String name1){
                    _districtcd = districtcd;
                    _name1 = name1;
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
        final String _output; //出力対象　1:全て 2:地区選択
        final String _districtCd; //地区コード
        final String _date;
        final String _time;
        final String _schoolKind;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _disp = request.getParameter("DISP");
            _output = request.getParameter("OUTPUT");
            _schoolKind = request.getParameter("SCHOOLKIND");
              _districtCd = ("2".equals(_output)) ? request.getParameter("DISTRICTCD") : "";
              _date = request.getParameter("DATE");
              _time = request.getParameter("TIME");

        }
    }
}

// eof
