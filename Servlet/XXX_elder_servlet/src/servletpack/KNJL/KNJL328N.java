/*
 * $Id: 22205ffde26aac6f5fc1c376caab1b513d6a8a26 $
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２７Ｎ＞  各種通知書（個人宛）
 **/
public class KNJL328N {

    private static final Log log = LogFactory.getLog(KNJL328N.class);

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
    
    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }
    
    public void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final List list = Applicant.load(db2, _param);
        
        final String form = "KNJL327N_4.frm";
        svf.VrSetForm(form, 1);

        final int maxRow = 2;
        final int maxLine = 5;
        int row = 1;
        int line = 1;
        
        for (int i = 0; i < list.size(); i++) {
            final Applicant appl = (Applicant) list.get(i);
            
            if (row > maxRow) {
                row = 1;
                line += 1;
                if (line > maxLine) {
                    svf.VrEndPage();
                    line = 1;
                }
            }

            final String srow = String.valueOf(row);
            svf.VrsOutn("ZIPCODE" + srow, line, appl._zipcd); // 郵便番号
            
            final int addr1Keta = getMS932ByteLength(appl._address1);
            final int addr2Keta = getMS932ByteLength(appl._address2);
            if (addr1Keta > 50 || addr2Keta > 50) {
                svf.VrsOutn("ADDRESS1_" + srow + "_5", line, appl._address1); // 住所
                svf.VrsOutn("ADDRESS2_" + srow + "_5", line, appl._address2); // 住所
            } else if (addr1Keta > 40 || addr2Keta > 40) {
                svf.VrsOutn("ADDRESS1_" + srow + "_4", line, appl._address1); // 住所
                svf.VrsOutn("ADDRESS2_" + srow + "_4", line, appl._address2); // 住所
            } else if (addr1Keta > 30 || addr2Keta > 30) {
                svf.VrsOutn("ADDRESS1_" + srow + "_3", line, appl._address1); // 住所
                svf.VrsOutn("ADDRESS2_" + srow + "_3", line, appl._address2); // 住所
            } else if (addr1Keta > 26 || addr2Keta > 26) {
                svf.VrsOutn("ADDRESS1_" + srow + "_2", line, appl._address1); // 住所
                svf.VrsOutn("ADDRESS2_" + srow + "_2", line, appl._address2); // 住所
            } else {
                svf.VrsOutn("ADDRESS1_" + srow, line, appl._address1); // 住所
                svf.VrsOutn("ADDRESS2_" + srow, line, appl._address2); // 住所
            }
            
            if (row == 1) {
                svf.VrsOutn("EXAM_NO", line, "受験番号　" + StringUtils.defaultString(appl._examno)); // 受験番号
            } else {
                svf.VrsOutn("EXAM_NO2", line, "受験番号　" + StringUtils.defaultString(appl._examno)); // 受験番号
            }

            final String name = StringUtils.isBlank(appl._name) ? "" : appl._name + "　様";
            final int nameKeta = getMS932ByteLength(name);
            if (nameKeta < 14) {
                svf.VrsOutn("NAME" + srow + "_1", line, name); // 住所
            } else if (nameKeta < 18) {
                svf.VrsOutn("NAME" + srow + "_2", line, name); // 住所
            } else {
                svf.VrsOutn("NAME" + srow + "_3", line, name); // 住所
            }

            row += 1;
            _hasData = true;
        }
        
        svf.VrEndPage();
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
    
    // 横線を縦書きで縦線に表示されるように変換する
    private String replaceHyphenMinus(final String s) {
        if (null == s) {
            return null;
        }
        final char hyphenMinus = '-'; // NG−
//        final char zenkakuHyphen = '‐'; // OK
        final char minus =  '\u2212'; // NG
        final char zenkakuMinus = '\uFF0D'; // NG
        final char zenkakuDash =  '\u2015'; // OK
        return s.replace(minus, zenkakuDash).replace(hyphenMinus, zenkakuDash).replace(zenkakuMinus, zenkakuDash);
    }
    
    private static class Applicant {
        final String _examno;
        final String _zipcd;
        final String _address1;
        final String _address2;
        final String _name;
        final String _finschoolName;
        final String _judgement;
        final String _l013Namespare1;
        final String _examcourseName;
        final String _majorname;
        final String _judgementName;
        final String _sucExamcourseName;
        
        Applicant(
            final String examno,
            final String zipcd,
            final String address1,
            final String address2,
            final String name,
            final String finschoolName,
            final String judgement,
            final String l013Namespare1,
            final String examcourseName,
            final String majorname,
            final String judgementName,
            final String sucExamcourseName
        ) {
            _examno = examno;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
            _name = name;
            _finschoolName = finschoolName;
            _judgement = judgement;
            _l013Namespare1 = l013Namespare1;
            _examcourseName = examcourseName;
            _majorname = majorname;
            _judgementName = judgementName;
            _sucExamcourseName = sucExamcourseName;
        }
        
        public static List load(final DB2UDB db2, final Param param) {
            final List applicantList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");
                    final String zipcd = rs.getString("ZIPCD");
                    final String address1 = rs.getString("ADDRESS1");
                    final String address2 = rs.getString("ADDRESS2");
                    final String name = rs.getString("NAME");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String judgement = rs.getString("JUDGEMENT");
                    final String l013Namespare1 = rs.getString("L013NAMESPARE1");
                    final String examcourseName = rs.getString("EXAMCOURSE_NAME");
                    final String majorname = rs.getString("MAJORNAME");
                    final String judgementName = rs.getString("JUDGEMENT_NAME");
                    final String sucExamcourseName = rs.getString("SUC_EXAMCOURSE_NAME");
                    final Applicant applicant = new Applicant(examno, zipcd, address1, address2, name, finschoolName, judgement, l013Namespare1, examcourseName, majorname, judgementName, sucExamcourseName);
                    applicantList.add(applicant);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return applicantList;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     BASE.EXAMNO, ");
            stb.append("     ADDR.ZIPCD, ");
            stb.append("     ADDR.ADDRESS1, ");
            stb.append("     ADDR.ADDRESS2, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.FS_CD, ");
            stb.append("     FIN.FINSCHOOL_NAME, ");
            stb.append("     FIN.FINSCHOOL_ZIPCD, ");
            stb.append("     FIN.FINSCHOOL_ADDR1, ");
            stb.append("     FIN.FINSCHOOL_ADDR2, ");
            stb.append("     FIN.PRINCNAME, ");
            stb.append("     BASE.JUDGEMENT, ");
            stb.append("     NML013.NAMESPARE1 AS L013NAMESPARE1, ");
            stb.append("     CRS1.EXAMCOURSE_NAME, ");
            stb.append("     MAJ.MAJORNAME, ");
            stb.append("     CRS2.EXAMCOURSE_NAME AS SUC_EXAMCOURSE_NAME, ");
            stb.append("     CASE WHEN NML013.NAMESPARE1 = '1' THEN '合格' ELSE NML013.NAME1 END AS JUDGEMENT_NAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND ADDR.EXAMNO = BASE.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND RECEPT.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND RECEPT.EXAMNO = BASE.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL1 ON BDETAIL1.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND BDETAIL1.EXAMNO = BASE.EXAMNO ");
            stb.append("         AND BDETAIL1.SEQ = '001'  ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL9 ON BDETAIL9.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND BDETAIL9.EXAMNO = BASE.EXAMNO ");
            stb.append("         AND BDETAIL9.SEQ = '009'  ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST CRS1 ON CRS1.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("         AND CRS1.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND CRS1.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND CRS1.COURSECD = BDETAIL1.REMARK8 ");
            stb.append("         AND CRS1.MAJORCD = BDETAIL1.REMARK9 ");
            stb.append("         AND CRS1.EXAMCOURSECD = BDETAIL1.REMARK10  ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST CRS2 ON CRS2.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("         AND CRS2.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND CRS2.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND CRS2.COURSECD = BASE.SUC_COURSECD ");
            stb.append("         AND CRS2.MAJORCD = BASE.SUC_MAJORCD ");
            stb.append("         AND CRS2.EXAMCOURSECD = BASE.SUC_COURSECODE ");
            stb.append("     LEFT JOIN V_MAJOR_MST MAJ ON MAJ.YEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND MAJ.COURSECD = BDETAIL1.REMARK8 ");
            stb.append("         AND MAJ.MAJORCD = BDETAIL1.REMARK9 ");
            stb.append("     LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' ");
            stb.append("         AND NML013.NAMECD2 = BASE.JUDGEMENT ");
            stb.append("     LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025' ");
            stb.append("         AND NML025.NAMECD2 = BDETAIL9.REMARK8 ");
            stb.append(" WHERE ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND BASE.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND BASE.EXAMNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(" ORDER BY ");
            stb.append("     BASE.EXAMNO ");
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
        final String[] _categorySelected;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
        }
    }
}

// eof

