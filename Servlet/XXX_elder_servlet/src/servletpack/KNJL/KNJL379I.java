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
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 */
public class KNJL379I {

    private static final Log log = LogFactory.getLog("KNJL379I.class");

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

        final int maxLine = 30;
        final Map hall_Map = getHallMap(db2); //受験班Map

        if(hall_Map.isEmpty()) {
            return false;
        }

        int page = 1; //ﾍﾟｰｼﾞ
        int line = 0; // 印字行

        for (Iterator ite = hall_Map.keySet().iterator(); ite.hasNext();) {
            final String hallKey = (String)ite.next();
            final Hall hall = (Hall)hall_Map.get(hallKey);

            if(line > maxLine || line == 0) {
                if(line > 0 ) {
                    svf.VrEndPage();
                    page++;
                }

                line = 1;
                svf.VrSetForm("KNJL379I.frm", 1);
                svf.VrsOut("PAGE", String.valueOf(page) + "頁"); //ページ
                svf.VrsOut("DATE", _param._date.replace("-", "/") + " " + _param._time); //作成日時
                svf.VrsOut("TITLE", _param._year + "年度入学試験" + "　" + _param._testAbbv + "　面接班人数確認表"); //タイトル
                svf.VrsOut("SCHOOL_NAME", _param._schoolName); //学校名
            }
            svf.VrsOutn("GROUP", line, hall._examhall_Name); //面接班
            svf.VrsOutn("EXAM_NO1", line, hall._s_Receptno); //受験番号始
            svf.VrsOutn("EXAM_NO2", line, hall._e_Receptno); //受験番号至
            svf.VrsOutn("GROUP_NUM1", line, hall._capa_Cnt); //班人数
            svf.VrsOutn("GROUP_NUM2", line, hall._sex1); //男人数
            svf.VrsOutn("GROUP_NUM3", line, hall._sex2); //女人数

            line++;
        }
        svf.VrEndPage();

    return true;
    }

    // 面接班取得
    private Map getHallMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        Hall hall = null;

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.EXAMHALLCD, ");
            stb.append("     T1.EXAMHALL_NAME, ");
            stb.append("     T1.EXAMHALL_ABBV, ");
            stb.append("     T1.CAPA_CNT, ");
            stb.append("     T1.S_RECEPTNO, ");
            stb.append("     T1.E_RECEPTNO, ");
            stb.append("     COUNT(T3.SEX= '1' or null) AS SEX1, ");
            stb.append("     COUNT(T3.SEX= '2' or null) AS SEX2 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_HALL_YDAT T1   ");
            stb.append(" LEFT JOIN ");
            stb.append("     ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T2.APPLICANTDIV = T1.APPLICANTDIV AND T2.TESTDIV = T1.TESTDIV AND T2.RECEPTNO BETWEEN T1.S_RECEPTNO AND T1.E_RECEPTNO ");
            stb.append(" LEFT JOIN ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T3 ON T3.ENTEXAMYEAR = T2.ENTEXAMYEAR AND T3.APPLICANTDIV = T2.APPLICANTDIV AND T3.TESTDIV = T2.TESTDIV AND T3.EXAMNO = T2.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' AND ");
            stb.append("     T1.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("     T1.TESTDIV = '" + _param._testDiv + "' AND ");
            stb.append("     T1.EXAM_TYPE = '1'    AND ");
            stb.append("     T1.EXAMHALLCD LIKE '2%' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.EXAMHALLCD,T1.EXAMHALL_NAME,T1.EXAMHALL_ABBV,T1.CAPA_CNT,T1.S_RECEPTNO,T1.E_RECEPTNO ");
            stb.append(" ORDER BY ");
            stb.append("     T1.EXAMHALLCD ");

            log.debug(" hall sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examhallcd = rs.getString("EXAMHALLCD");
                final String examhall_Name = rs.getString("EXAMHALL_NAME");
                final String examhall_Abbv = rs.getString("EXAMHALL_ABBV");
                final String capa_Cnt = rs.getString("CAPA_CNT");
                final String s_Receptno = rs.getString("S_RECEPTNO");
                final String e_Receptno = rs.getString("E_RECEPTNO");
                final String sex1 = rs.getString("SEX1");
                final String sex2 = rs.getString("SEX2");

                if (!retMap.containsKey(examhallcd )) {
                    retMap.put(examhallcd, new Hall(examhallcd, examhall_Name, examhall_Abbv, capa_Cnt, s_Receptno, e_Receptno, sex1, sex2));
                }
            }
        } catch (final SQLException e) {
            log.error("面接班の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }


    private class Hall {
        final String _examhallcd; //会場コード
        final String _examhall_Name; //会場名称
        final String _examhall_Abbv; //会場略称
        final String _capa_Cnt; //面接班人数
        final String _s_Receptno; //受験番号始
        final String _e_Receptno; //受験番号至
        final String _sex1; //男人数
        final String _sex2; //女人数

        public Hall(final String examhallcd, final String examhall_Name, final String examhall_Abbv, final String capa_Cnt, final String s_Receptno,
                final String e_Receptno, final String sex1, final String sex2) {
            _examhallcd = examhallcd;
            _examhall_Name = examhall_Name;
            _examhall_Abbv = examhall_Abbv;
            _capa_Cnt = capa_Cnt;
            _s_Receptno = s_Receptno;
            _e_Receptno = e_Receptno;
            _sex1 = sex1;
            _sex2 = sex2;
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
        final String _date;
        final String _time;
        final String _schoolKind;
        final String _testAbbv;
        final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _schoolKind = request.getParameter("SCHOOLKIND");
              _date = request.getParameter("DATE");
              _time = request.getParameter("TIME");
              _testAbbv = getTestAbbv(db2);
              _schoolName = getSchoolName(db2);
        }

        private String getTestAbbv(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTDIV_ABBV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _year + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' "));
        }

        private String getSchoolName(final DB2UDB db2) {
            final String kindcd = "1".equals(_applicantDiv) ? "105" : "106";
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + kindcd + "' "));

        }
    }
}

// eof
