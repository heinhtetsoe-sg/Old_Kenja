/*
 * $Id: 4118e7099a512f731d375f2314c6783ee1fd8d58 $
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３０６Ｂ＞  志願者数一覧
 **/
public class KNJL306B {

    private static final Log log = LogFactory.getLog(KNJL306B.class);

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
    
    public void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List finschoolList = Finschool.load(db2, _param);
        final int maxLine = 40;
        
        final List pageList = getPageList(finschoolList, maxLine);
        int boyCount = 0;
        int girlCount = 0;
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List list = (List) pageList.get(pi);

            final String form = "KNJL306B.frm";
            svf.VrSetForm(form, 1);
            
            svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度"); // 年度
            svf.VrsOut("KIND", _param._testdivAbbv1); // 入試制度
            // svf.VrsOut("HOPE_COURSE", _param._majorName); // 志望所属
            svf.VrsOut("TITLE", "志願者数一覧表（出身校別）"); // タイトル
            svf.VrsOut("DATE", _param._dateStr); // 作成日
            
            for (int j = 0; j < list.size(); j++) {
                final Finschool finschool = (Finschool) list.get(j); 
                
                final String k = (j >= 20) ? "2" : "1";
                final int line = j + 1 + (j >= 20 ? -20 : 0);
                svf.VrsOutn("NO" + k, line, String.valueOf(pi * maxLine + (j + 1))); // NO
                final int alen = getMS932ByteLength(finschool._finschoolAddr1);
                final int slen = getMS932ByteLength(finschool._finschoolName);
                final String a = alen > 30 ? "_3" : alen > 24 ? "_2" : "";
                final String s = slen > 30 ? "_3" : slen > 24 ? "_2" : "";
                svf.VrsOutn("ADDR" + k + a, line, finschool._finschoolAddr1); // 所在地
                svf.VrsOutn("SCHOOL_NAME" + k + s, line, finschool._finschoolName); // 学校名
                svf.VrsOutn("BOY" + k, line, finschool._countSex1); // 男子
                svf.VrsOutn("GIRL" + k, line, finschool._countSex2); // 女子
                svf.VrsOutn("TOTAL" + k, line, finschool._countTotal); // 合計
                if (null != finschool._countSex1) {
                    boyCount += Integer.parseInt(finschool._countSex1);
                }
                if (null != finschool._countSex2) {
                    girlCount += Integer.parseInt(finschool._countSex2);
                }
            }
            if (pi == pageList.size() - 1) {
                svf.VrsOut("TOTAL", _param._sexName1 + boyCount + "名、" + _param._sexName2 + girlCount + "名、合計" + String.valueOf(boyCount + girlCount) + "名"); // 合計
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    private int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
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
    
    private static class Finschool {
        final String _fsCd;
        final String _finschoolName;
        final String _finschoolAddr1;
        final String _countSex1;
        final String _countSex2;
        final String _countTotal;

        Finschool(
            final String fsCd,
            final String finschoolName,
            final String finschoolAddr1,
            final String countSex1,
            final String countSex2,
            final String countTotal
        ) {
            _fsCd = fsCd;
            _finschoolName = finschoolName;
            _finschoolAddr1 = finschoolAddr1;
            _countSex1 = countSex1;
            _countSex2 = countSex2;
            _countTotal = countTotal;
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String fsCd = rs.getString("FS_CD");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String finschoolAddr1 = rs.getString("FINSCHOOL_ADDR1");
                    final String countSex1 = rs.getString("COUNT_SEX1");
                    final String countSex2 = rs.getString("COUNT_SEX2");
                    final String countTotal = rs.getString("COUNT_TOTAL");
                    final Finschool finschool = new Finschool(fsCd, finschoolName, finschoolAddr1, countSex1, countSex2, countTotal);
                    list.add(finschool);
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
            stb.append(" WITH APPLICANTBASE AS ( ");
            stb.append("     SELECT ");
            stb.append("         BASE.FS_CD, ");
            stb.append("         FIN.FINSCHOOL_NAME, ");
            stb.append("         FIN.FINSCHOOL_ADDR1, ");
            stb.append("         BASE.SEX ");
            stb.append("     FROM ");
            stb.append("         ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("         INNER JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("     WHERE ");
            stb.append("        BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("        AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("        AND BASE.TESTDIV = '" + param._testdiv + "' ");
            stb.append(" ), SCHOOL_GROUP AS ( ");
            stb.append("     SELECT ");
            stb.append("         FS_CD, ");
            stb.append("         FINSCHOOL_NAME, ");
            stb.append("         FINSCHOOL_ADDR1, ");
            stb.append("         VALUE(SEX, '9') AS SEX, ");
            stb.append("         COUNT(*) AS COUNT ");
            stb.append("     FROM ");
            stb.append("         APPLICANTBASE ");
            stb.append("     GROUP BY ");
            stb.append("         GROUPING SETS  ");
            stb.append("             ((FS_CD, FINSCHOOL_NAME, FINSCHOOL_ADDR1, SEX), ");
            stb.append("              (FS_CD, FINSCHOOL_NAME, FINSCHOOL_ADDR1)) ");
            stb.append(" ) ");
            stb.append(" SELECT  ");
            stb.append("     T1.FS_CD, ");
            stb.append("     T1.FINSCHOOL_NAME, ");
            stb.append("     T1.FINSCHOOL_ADDR1, ");
            stb.append("     VALUE(L1.COUNT, 0) AS COUNT_SEX1, ");
            stb.append("     VALUE(L2.COUNT, 0) AS COUNT_SEX2, ");
            stb.append("     T1.COUNT AS COUNT_TOTAL ");
            stb.append(" FROM SCHOOL_GROUP T1 ");
            stb.append(" LEFT JOIN SCHOOL_GROUP L1 ON L1.FS_CD = T1.FS_CD AND L1.SEX = '1' ");
            stb.append(" LEFT JOIN SCHOOL_GROUP L2 ON L2.FS_CD = T1.FS_CD AND L2.SEX = '2' ");
            stb.append(" WHERE ");
            stb.append("     T1.SEX = '9' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.FS_CD ");
            return stb.toString();
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;

        final String _sexName1;
        final String _sexName2;
        final String _testdivAbbv1;
        final String _dateStr;
        final String _majorName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _dateStr = getDateStr(_date);

            _sexName1 = StringUtils.defaultString(getNameMst(db2, "NAME2", "Z002", "1"));
            _sexName2 = StringUtils.defaultString(getNameMst(db2, "NAME2", "Z002", "2"));
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);
            _majorName = getMajorName(db2);
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

        private String getMajorName(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT MAJORNAME ");
                sql.append(" FROM MAJOR_MST ");
                sql.append(" WHERE COURSECD || MAJORCD =  ");
                sql.append("  (SELECT MAX(COURSECD || MAJORCD) ");
                sql.append("   FROM ENTEXAM_COURSE_MST ");
                sql.append("   WHERE ENTEXAMYEAR = '" + _entexamyear + "' ");
                sql.append("     AND APPLICANTDIV = '" + _applicantdiv + "' ");
                sql.append("     AND TESTDIV = '" + _testdiv + "') ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("MAJORNAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

