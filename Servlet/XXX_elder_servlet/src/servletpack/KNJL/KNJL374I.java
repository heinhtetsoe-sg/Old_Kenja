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
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 */
public class KNJL374I {

    private static final Log log = LogFactory.getLog("KNJL374I.class");

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

        final Map district_Map = getDistrictMap(db2); //志願者数Map

        if(district_Map.isEmpty()) {
            return false;
        }

        final int MaxLine = 50;
        int page = 1; // ページ
        int col = 1; //列

        svf.VrSetForm("KNJL374I.frm", 1);
        svf.VrsOut("PAGE", String.valueOf(page) + "頁"); //ページ
        final String date = _param._date != null ? _param._date.replace("-", "/") : "";
        svf.VrsOut("DATE", "作成日時：" + date + " " + _param._time); //作成日時
        final String div = "1".equals(_param._disp) ? "男女共" : "2".equals(_param._disp) ? "男子" : "女子";
        svf.VrsOut("TITLE", _param._testAbbv + "   " + _param._year + "年度入学試験   小学校別 志願者数・合格者数リスト   《合格者／出願者》  (" + div + ")" ); //タイトル
        svf.VrsOut("SCHOOL_NAME", "関西学院 中学部"); //学校名

        for (Iterator ite = district_Map.keySet().iterator(); ite.hasNext();) {
            int line = 1; //印字行
            final String Key = (String)ite.next();
            final District district = (District)district_Map.get(Key);

            if (col > 5) {
                svf.VrEndPage();
                svf.VrSetForm("KNJL374I.frm", 1);
                col = 1;
                page++;
                svf.VrsOut("PAGE", String.valueOf(page) + "頁"); //ページ
                svf.VrsOut("DATE", "作成日時：" + date + " " + _param._time); //作成日時
                svf.VrsOut("TITLE", _param._testAbbv + "   " + _param._year + "年度入学試験   小学校別 志願者数・合格者数リスト   《合格者／出願者》  (" + div + ")" ); //タイトル
                svf.VrsOut("SCHOOL_NAME", "関西学院 中学部"); //学校名
            }

            svf.VrsOut("DISTRICT_NAME" + col, district._districtname); //地区名
            svf.VrsOut("SCHOOL_NUM" + col, district._schoolcount + "校"); //学校数
            svf.VrsOut("DISTRICT_NUM" + col + "_1", "0".equals(district._goukaku) ? "" : district._goukaku + "人"); //合格者
            svf.VrsOut("DISTRICT_NUM" + col + "_2", district._applicant + "人"); //志願者

            for (Iterator printite = district._schoolMap.keySet().iterator(); printite.hasNext();) {
                final String getKey = (String)printite.next();
                final School school = (School)district._schoolMap.get(getKey);

                if(line > MaxLine) {
                    line = 1;
                    col++;
                    if (col > 5) {
                        svf.VrEndPage();
                        svf.VrSetForm("KNJL374I.frm", 1);
                        col = 1;
                        page++;
                        svf.VrsOut("PAGE", String.valueOf(page)); //ページ
                        svf.VrsOut("DATE", "作成日時：" + date + " " + _param._time); //作成日時
                        svf.VrsOut("TITLE", _param._testAbbv + "   " + _param._year + "年度入学試験   小学校別 志願者数・合格者数リスト   《合格者／出願者》  (" + div + ")" ); //タイトル
                        svf.VrsOut("SCHOOL_NAME", "関西学院 中学部"); //学校名
                    }
                    svf.VrsOut("DISTRICT_NAME" + col, district._districtname); //地区名
                    svf.VrsOut("SCHOOL_NUM" + col, district._schoolcount + "校"); //学校数
                    svf.VrsOut("DISTRICT_NUM" + col + "_1", "0".equals(district._goukaku) ? "" : district._goukaku + "人"); //合格者
                    svf.VrsOut("DISTRICT_NUM" + col + "_2", district._applicant + "人"); //志願者
                }

                svf.VrsOutn("NO" + col, line, String.valueOf(line)); //No
                svf.VrsOutn("FINSCHOOL_DIV" + col, line, "1".equals(school._finschool_Div) ? "国" : "2".equals(school._finschool_Div) ? "公" : "3".equals(school._finschool_Div) ? "私" : "" ); //設置
                String finschoolName = StringUtils.defaultString(school._finschool_Name);
                if(finschoolName.endsWith("小学校")) finschoolName = finschoolName.replace("小学校", ""); //末尾が"小学校"の場合、カット
                svf.VrsOutn("FINSCHOOL_NAME" + col, line, finschoolName); //学校名

                svf.VrsOutn("NUM" + col + "_1", line, "0".equals(school._goukaku) ? "" : school._goukaku); //合格者
                svf.VrsOutn("NUM" + col + "_2", line, school._applicant); //志願者

                line++;
            }
            col++;
        }
        svf.VrEndPage();

    return true;
    }

    private String getFieldName(final String str) {
        final int keta = KNJ_EditEdit.getMS932ByteLength(str);
        return keta <= 20 ? "1" : keta <= 30 ? "2" : "3" ;
    }

    // 学校別情報取得
    private Map getDistrictMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        District district = null;

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.FS_CD, ");
            stb.append("     T2.DISTRICTCD, ");
            stb.append("     T3.NAME1 AS DISTRICTNAME, ");
            stb.append("     T2.FINSCHOOL_NAME, ");
            stb.append("     T2.FINSCHOOL_DIV, ");
            stb.append("     COUNT(T1.EXAMNO) AS APPLICANT, ");
            stb.append("     COUNT(T1.JUDGEMENT = '1' or null) AS GOUKAKU ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1     ");
            stb.append(" LEFT JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T1.FS_CD   ");
            stb.append(" LEFT JOIN V_NAME_MST T3 ON T3.YEAR = '" + _param._year + "' AND T3.NAMECD1 = 'Z003' AND T3.NAMECD2 = T2.DISTRICTCD ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "' ");
            if(Integer.parseInt(_param._disp) > 1) {
                final String sex = String.valueOf(Integer.parseInt(_param._disp) - 1);
                stb.append("     AND T1.SEX = '" + sex + "' "); //1:男子のみ 2:女子のみ
            }
            stb.append(" GROUP BY T1.FS_CD, ");
            stb.append("          T2.DISTRICTCD, ");
            stb.append("          T3.NAME1, ");
            stb.append("          T2.FINSCHOOL_NAME, ");
            stb.append("          T2.FINSCHOOL_DIV ");
            stb.append(" ORDER BY DISTRICTCD,FS_CD ");

            log.debug(" school sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String fs_Cd = rs.getString("FS_CD");
                final String districtcd = rs.getString("DISTRICTCD");
                final String districtname = rs.getString("DISTRICTNAME");
                final String finschool_Name = rs.getString("FINSCHOOL_NAME");
                final String finschool_Div = rs.getString("FINSCHOOL_DIV");
                final String applicant = rs.getString("APPLICANT");
                final String goukaku = rs.getString("GOUKAKU");


                final School school = new School(fs_Cd, districtcd, districtname, finschool_Name, finschool_Div, applicant, goukaku);
                if (retMap.containsKey(districtcd )) {
                    district = (District)retMap.get(districtcd);
                } else {
                    district = setDistrict(db2,districtcd);
                    retMap.put(districtcd, district);
                }

                if(!district._schoolMap.containsKey(fs_Cd)) {
                    district._schoolMap.put(fs_Cd, school);
                }
            }
        } catch (final SQLException e) {
            log.error("学校別の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;
    }

    //地区別情報取得
    private District setDistrict(final DB2UDB db2, final String districtcd) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        District district = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.DISTRICTCD AS DISTRICTCD, ");
            stb.append("     MAX(T3.NAME1) AS DISTRICTNAME, ");
            stb.append("     COUNT(DISTINCT T1.FS_CD) AS SCHOOLCOUNT, ");
            stb.append("     COUNT(T1.EXAMNO) AS APPLICANT, ");
            stb.append("     COUNT(T1.JUDGEMENT = '1' or null) AS GOUKAKU ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(" LEFT JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T1.FS_CD ");
            stb.append(" LEFT JOIN V_NAME_MST T3 ON T3.YEAR = '" + _param._year + "' AND T3.NAMECD1 = 'Z003' AND T3.NAMECD2 = T2.DISTRICTCD ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' AND ");
            stb.append("     T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("   AND  T1.TESTDIV = '" + _param._testDiv + "' ");
            if (districtcd == null) {
                stb.append(" AND T2.DISTRICTCD IS NULL ");
            } else {
                stb.append(" AND T2.DISTRICTCD = '" + districtcd + "' ");
            }
            if(Integer.parseInt(_param._disp) > 1) {
                final String sex = String.valueOf(Integer.parseInt(_param._disp) - 1);
                stb.append("     AND T1.SEX = '" + sex + "' "); //1:男子のみ 2:女子のみ
            }
            stb.append(" GROUP BY ");
            stb.append("     T2.DISTRICTCD ");
            stb.append(" ORDER BY ");
            stb.append("     DISTRICTCD ");

            log.debug(" district sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            if (rs.next()) {
                final String districtname = rs.getString("DISTRICTNAME");
                final String schoolcount = rs.getString("SCHOOLCOUNT");
                final String applicant = rs.getString("APPLICANT");
                final String goukaku = rs.getString("GOUKAKU");

                district = new District(districtcd, districtname, schoolcount, applicant, goukaku);
            }

        } catch (final SQLException e) {
            log.error("地区別の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return district;
    }

    private class School {
        final String _fs_Cd; //学校コード
        final String _districtcd; //地区コード
        final String _districtname; //地区名
        final String _finschool_Name; //学校名
        final String _finschool_Div; //学校種別
        final String _applicant; //志願者数
        final String _goukaku; //合格者数

        public School(final String fs_Cd, final String districtcd, final String districtname,
                final String finschool_Name, final String finschool_Div, final String applicant,
                final String goukaku) {

            _fs_Cd = fs_Cd;
            _districtcd = districtcd;
            _districtname = districtname;
            _finschool_Name = finschool_Name;
            _finschool_Div = finschool_Div;
            _applicant = applicant;
            _goukaku = goukaku;
        }
    }

    private class District {
        final String _districtcd; //地区コード
        final String _districtname; //地区名
        final String _schoolcount; //学校数
        final String _applicant; //志願者数
        final String _goukaku; //合格者数
        final Map _schoolMap;

        public District(final String districtcd, final String districtname, final String schoolcount, final String applicant, final String goukaku){
                    _districtcd = districtcd;
                    _districtname = districtname;
                    _schoolcount = schoolcount;
                    _applicant = applicant;
                    _goukaku = goukaku;
                    _schoolMap = new LinkedMap();
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
        final String _testAbbv;
        final String _date;
        final String _time;
        final String _schoolKind;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _disp = request.getParameter("DISP");
            _schoolKind = request.getParameter("SCHOOLKIND");
              _date = request.getParameter("DATE");
              _time = request.getParameter("TIME");
              _testAbbv = getTestAbbv(db2);

        }
        private String getTestAbbv(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTDIV_ABBV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _year + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' "));
        }
    }
}

// eof
