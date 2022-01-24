/*
 * $Id: 1101da050f0d69e8b9bd2d74557770ea457bb03c $
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
 *                  ＜ＫＮＪＬ４２３Ｎ＞  各種名簿
 **/
public class KNJL423Y {

    private static final Log log = LogFactory.getLog(KNJL423Y.class);

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
    
    private static int getMS932count(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }
    
    private static String formatDate(final String date) {
        if (null == date) {
            return null;
        }
        return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date) ;
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final String form = "KNJL423Y.frm";
        final int maxLine = 50;
        final String title = StringUtils.defaultString(_param._applicantdivName1) + "入学試験　" + ("2".equals(_param._inout) ? "補欠合格者名簿" : "合格者名簿");
        final List allApplicantList = Applicant.getApplicantList(db2, _param);
        
        final List pageList = getPageList(allApplicantList, maxLine, _param);
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List applicantList = (List) pageList.get(pi);
            
            svf.VrSetForm(form, 1);
            
//            svf.VrsOut("SCHOOLNAME", null); // 学校名
            svf.VrsOut("NENDO", _param._entexamyear + "年度"); // 年度
            svf.VrsOut("TITLE", title); // タイトル
            svf.VrsOut("DATE", formatDate(_param._date)); // 作成日
            svf.VrsOut("PAGE", String.valueOf(pi + 1)); // ページ
            svf.VrsOut("TOTAL_PAGE", String.valueOf(pageList.size())); // 総ページ数
//            svf.VrsOut("NOTE", null); // 備考

            for (int j = 0; j < applicantList.size(); j++) {
                final Applicant appl = (Applicant) applicantList.get(j);
                final int line = j + 1;
                
                svf.VrsOutn("NUMBER", line, String.valueOf(pi * maxLine + line)); // 連番
                svf.VrsOutn("EXAMNO", line, appl._examno); // 受験番号
                svf.VrsOutn("NAME" + (getMS932count(appl._name) > 30 ? "3" : getMS932count(appl._name) > 20 ? "2" : "1"), line, appl._name); // 氏名
                svf.VrsOutn("SEX", line, appl._sexName); // 性別
                svf.VrsOutn("FINSCHOOL" + (getMS932count(appl._fsName) > 40 ? "3" : getMS932count(appl._fsName) > 26 ? "2" : "1"), line, appl._fsName); // 出身校
                svf.VrsOutn("GUARD_NAME" + (getMS932count(appl._gname) > 30 ? "3" : getMS932count(appl._gname) > 20 ? "2" : "1"), line, appl._gname); // 保護者名
                svf.VrsOutn("ZIPCODE", line, appl._zipcd); // 郵便番号
                final String address = StringUtils.defaultString(appl._address1) + StringUtils.defaultString(appl._address2);
                svf.VrsOutn("ADDRESS" + (getMS932count(address) > 50 ? "2" : "1"), line, address); // 現住所
                svf.VrsOutn("TELNO", line, appl._telno); // 電話番号
//                svf.VrsOutn("REMARK1", line, null); // 備考
//                svf.VrsOutn("REMARK2", line, null); // 備考
//                svf.VrsOutn("REMARK3", line, null); // 備考 リンクフィールド
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    private static List getPageList(final List list, final int count, final Param param) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Applicant o = (Applicant) it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    private static class Applicant {
        final String _examno;
        final String _name;
        final String _sexName;
        final String _fsName;
        final String _gname;
        final String _zipcd;
        final String _address1;
        final String _address2;
        final String _telno;

        Applicant(
            final String examno,
            final String name,
            final String sexName,
            final String fsName,
            final String gname,
            final String zipcd,
            final String address1,
            final String address2,
            final String telno
        ) {
            _examno = examno;
            _name = name;
            _sexName = sexName;
            _fsName = fsName;
            _gname = gname;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
            _telno = telno;
        }

        public static List getApplicantList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");
                    final String name = rs.getString("NAME");
                    final String sexName = rs.getString("SEX_NAME");
                    final String fsName = rs.getString("FS_NAME");
                    final String gname = rs.getString("GNAME");
                    final String zipcd = rs.getString("ZIPCD");
                    final String address1 = rs.getString("ADDRESS1");
                    final String address2 = rs.getString("ADDRESS2");
                    final String telno = rs.getString("TELNO");
                    final Applicant applicant = new Applicant(examno, name, sexName, fsName, gname, zipcd, address1, address2, telno);
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
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("     BASE.EXAMNO,  ");
            stb.append("     BASE.NAME,  ");
            stb.append("     NMZ002.NAME2 AS SEX_NAME,   ");
            stb.append("     BASE.FS_NAME,  ");
            stb.append("     ADDR.GNAME,  ");
            stb.append("     ADDR.ZIPCD,  ");
            stb.append("     ADDR.ADDRESS1,  ");
            stb.append("     ADDR.ADDRESS2,  ");
            stb.append("     ADDR.TELNO  ");
            stb.append("  FROM ENTEXAM_APPLICANTBASE_DAT BASE  ");
            stb.append("  LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("     AND ADDR.EXAMNO = BASE.EXAMNO  ");
            stb.append("  INNER JOIN ENTEXAM_RECEPT_DAT RCPT ON RCPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("     AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV  ");
            stb.append("     AND RCPT.EXAM_TYPE = '1'  ");
            stb.append("     AND RCPT.EXAMNO = BASE.EXAMNO  ");
            stb.append("  LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = BASE.SEX   ");
            stb.append("  LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = RCPT.JUDGEDIV   ");
            stb.append(" WHERE   ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "'  ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "'   ");
            stb.append("     AND RCPT.TESTDIV = '" + param._testdiv + "'  ");
            if ("1".equals(param._inout)) {
                stb.append("     AND NML013.NAMESPARE1 = '1'  ");
            } else if ("2".equals(param._inout)) {
                stb.append("     AND RCPT.JUDGEDIV = '3'  ");
            }
            stb.append(" ORDER BY   ");
            stb.append("     BASE.EXAMNO ");
            return stb.toString();
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _inout; // 1:合格名簿 2:補欠合格者名簿一覧
        final String _date;
        
        final String _applicantdivName1;
        final String _testdivName1;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _inout = request.getParameter("INOUT");
            _date = request.getParameter("LOGIN_DATE").replace('/', '-');
            _applicantdivName1 = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testdivName1 = getNameMst(db2, "NAME1", "L004", _testdiv);
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

