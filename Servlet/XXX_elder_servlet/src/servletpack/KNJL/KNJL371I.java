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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 9d162c8f179c9576f95ed14cefad85c294da74d1 $
 */
public class KNJL371I {

    private static final Log log = LogFactory.getLog("KNJL371I.class");

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

        final int MaxLine = 5;
        final Map applicant_Map = getApplicantMap(db2); //志願者Map

        if(applicant_Map.isEmpty()) {
            return false;
        }

        int cnt = 1; // １頁内の連番
        int line = 0; // 印字行
        int col = 1; // 列
        for (Iterator ite = applicant_Map.keySet().iterator(); ite.hasNext();) {

            final String getKey = (String) ite.next();
            final Applicant applicant = (Applicant) applicant_Map.get(getKey);

            if (col > 4) {
                line++;
                col = 1;
            }

            if (line > MaxLine || line == 0) {
                if(line > MaxLine) svf.VrEndPage();
                svf.VrSetForm("KNJL371I.frm", 1);
                cnt = 1;
                line = 1;
            }

            final String zipno = "1".equals(_param._formDiv) ? StringUtils.defaultString(applicant._schoolZipCd,"") : StringUtils.defaultString(applicant._applicantZipCd,"");
            final String addr1 = "1".equals(_param._formDiv) ? "  " + StringUtils.defaultString(applicant._schoolAddr1,"") : "  " + StringUtils.defaultString(applicant._applicantAddr1,"");
            final String addr2 = "1".equals(_param._formDiv) ? "  " + StringUtils.defaultString(applicant._schoolAddr2,"") : "  " + StringUtils.defaultString(applicant._applicantAddr2,"");
            final String name = "1".equals(_param._formDiv) ? "  " + StringUtils.defaultString(applicant._schoolName,"") + "　校長様" : "  " + StringUtils.defaultString(applicant._name,"")  + " 様";

            svf.VriOutn("NO" + col, line, cnt); // 連番

            svf.VrsOutn("ZIPNO" + col, line, "〒" + zipno); // 郵便番号

            final int ad1len = KNJ_EditEdit.getMS932ByteLength(addr1);
            final String ad1field = ad1len <= 36 ? "1" : ad1len <= 50 ? "2" : "3";
            svf.VrsOutn("ADDR" + col + "_1_" + ad1field, line, addr1); // 住所１

            final int ad2len = KNJ_EditEdit.getMS932ByteLength(addr2);
            final String ad2field = ad2len <= 36 ? "1" : ad2len <= 50 ? "2" : "3";
            svf.VrsOutn("ADDR" + col + "_2_" + ad2field, line, addr2); // 住所２

            final int nlen = KNJ_EditEdit.getMS932ByteLength(name);
            final String nfield = nlen <= 36 ? "1" : nlen <= 50 ? "2" : "3";
            svf.VrsOutn("NAME" + col + "_" + nfield, line, name); // 名前

            cnt++;
            col++;

        }
    svf.VrEndPage();
    return true;
    }

    // 志願者取得
    private Map getApplicantMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("   T1.ENTEXAMYEAR, ");
            stb.append("   T1.APPLICANTDIV, ");
            stb.append("   T1.EXAMNO, ");
            stb.append("   T1.TESTDIV, ");
            stb.append("   T1.NAME, ");
            stb.append("   T1.FS_CD, ");
            stb.append("   T1.JUDGEMENT, ");
            stb.append("   T2.ZIPCD, ");
            stb.append("   T2.ADDRESS1, ");
            stb.append("   T2.ADDRESS2, ");
            stb.append("   T3.FINSCHOOL_ZIPCD, ");
            stb.append("   T3.FINSCHOOL_ADDR1, ");
            stb.append("   T3.FINSCHOOL_ADDR2, ");
            stb.append("   T3.FINSCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(" LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND T1.APPLICANTDIV = T2.APPLICANTDIV AND T1.EXAMNO = T2.EXAMNO ");
            stb.append(" LEFT JOIN FINSCHOOL_MST T3 ON T1.FS_CD = T3.FINSCHOOLCD ");
            stb.append(" WHERE ");
            if("2".equals(_param._disp)) {
                stb.append("   T1.JUDGEMENT = '1' AND "); //合格者のみ
            }
            if(!_param._bangouFrom.equals("") && !_param._bangouTo.equals("")) {
                stb.append("   T1.EXAMNO BETWEEN '"+ _param._bangouFrom + "' AND '" + _param._bangouTo + "' AND ");
            } else if (!_param._bangouFrom.equals("")) {
                stb.append("   T1.EXAMNO >= '" + _param._bangouFrom + "' AND ");
            } else if (!_param._bangouTo.equals("")) {
                stb.append("   T1.EXAMNO <= '" + _param._bangouTo + "' AND ");
            }
            stb.append("   T1.ENTEXAMYEAR = '" + _param._year + "' AND ");
            stb.append("   T1.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("   T1.TESTDIV = '" + _param._testDiv + "' AND ");
            stb.append("   VALUE(T1.JUDGEMENT, '') NOT IN ('5') ");
            stb.append(" ORDER BY ");
            if("1".equals(_param._formDiv)) {
                stb.append(" T1.FS_CD, ");
            }
            stb.append(" T1.EXAMNO ");

            log.debug(" applicant sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examNo = rs.getString("EXAMNO");
                final String applicantDiv = rs.getString("APPLICANTDIV");
                final String testDiv = rs.getString("TESTDIV");
                final String name = rs.getString("NAME");
                final String judgement = rs.getString("JUDGEMENT");
                final String fsCd = rs.getString("FS_CD");
                final String schoolName = rs.getString("FINSCHOOL_NAME");
                final String schoolZipCd = rs.getString("FINSCHOOL_ZIPCD");
                final String schoolAddr1 = rs.getString("FINSCHOOL_ADDR1");
                final String schoolAddr2 = rs.getString("FINSCHOOL_ADDR2");
                final String applicantZipCd = rs.getString("ZIPCD");
                final String applicantAddr1 = rs.getString("ADDRESS1");
                final String applicantAddr2 = rs.getString("ADDRESS2");

                final Applicant applicant = new Applicant(examNo, applicantDiv, testDiv, name, judgement, fsCd, schoolName,
                        schoolZipCd, schoolAddr1, schoolAddr2, applicantZipCd, applicantAddr1, applicantAddr2);

                if (!retMap.containsKey(examNo)) {
                    retMap.put(examNo, applicant);
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
        final String _examNo; //受験番号
        final String _applicantDiv; //入試制度
        final String _testDiv; //入試区分
        final String _name; //氏名
        final String _judgement; //合否判定
        final String _fsCd; //出身学校コード
        final String _schoolName; //学校名称
        final String _schoolZipCd; //学校郵便番号
        final String _schoolAddr1; //学校住所１
        final String _schoolAddr2; //学校住所２
        final String _applicantZipCd; //志願者郵便番号
        final String _applicantAddr1; //志願者住所１
        final String _applicantAddr2; //志願者住所２

        public Applicant(
                final String examNo, final String applicantDiv, final String testDiv, final String name,final String judgement,
                final String fsCd, final String schoolName, final String schoolZipCd, final String schoolAddr1, final String schoolAddr2,
                final String applicantZipCd, final String applicantAddr1, final String applicantAddr2) {
            _examNo = examNo;
            _applicantDiv = applicantDiv;
            _testDiv = testDiv;
            _name = name;
            _judgement = judgement;
            _fsCd = fsCd;
            _schoolName = schoolName;
            _schoolZipCd = schoolZipCd;
            _schoolAddr1 = schoolAddr1;
            _schoolAddr2 = schoolAddr2;
            _applicantZipCd = applicantZipCd;
            _applicantAddr1 = applicantAddr1;
            _applicantAddr2 = applicantAddr2;
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
        final String _bangouFrom; //受験番号１
        final String _bangouTo; //受験番号２
        final String _applicantDiv; //入試制度
        final String _testDiv; //入試区分
        final String _formDiv; //帳票タイプ　1:出身校住所ラベル 2:対象者住所ラベル
        final String _disp;  //抽出区分　1:全員 2:合格者のみ
        final String _schoolKind;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _bangouFrom = request.getParameter("BANGOU1");
            _bangouTo = request.getParameter("BANGOU2");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _formDiv = request.getParameter("FORMDIV");
            _disp = request.getParameter("DISP");
            _schoolKind = request.getParameter("SCHOOLKIND");
        }
    }
}

// eof
