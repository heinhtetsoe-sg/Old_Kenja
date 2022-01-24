/*
 * $Id: 3184debf3d3c9fc72a932d32004f205089621f27 $
 *
 * 作成日: 2017/04/06
 * 作成者: nakamoto
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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;


public class KNJL508J_4 extends HttpServlet {

	private static final Log log = LogFactory.getLog(KNJL508J_4.class);

    Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB    db2;                  // Databaseクラスを継承したクラス
    boolean nonedata;           // 該当データなしフラグ

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
    	svf.VrSetForm("KNJL508J_3.frm", 1);
        final List printList = getList(db2);
        final int maxRow = 6;
        int row = 1;
        int col = 1;

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData putwk = (PrintData) iterator.next();

            if (row > maxRow) {
                svf.VrEndPage();
                svf.VrSetForm("KNJL508J_3.frm", 1);
                row = 1;
            }

            final String zipcd = "".equals(putwk._finschoolZipcd) ? "": "〒" + putwk._finschoolZipcd;
            svf.VrsOutn("ZIPCODE" + String.valueOf(col), row, zipcd);   //郵便番号

            final String addr1Field = KNJ_EditEdit.getMS932ByteLength(putwk._finschoolAddr1) > 50 ? "_1_3" : KNJ_EditEdit.getMS932ByteLength(putwk._finschoolAddr1) > 40 ? "_1_2" : "_1_1" ;
            final String addr2Field = KNJ_EditEdit.getMS932ByteLength(putwk._finschoolAddr2) > 50 ? "_2_3" : KNJ_EditEdit.getMS932ByteLength(putwk._finschoolAddr2) > 40 ? "_2_2" : "_2_1" ;
            svf.VrsOutn("ADDRESS" + String.valueOf(col) + addr1Field, row, putwk._finschoolAddr1);     //住所
            svf.VrsOutn("ADDRESS" + String.valueOf(col) + addr2Field, row, putwk._finschoolAddr2);     //住所

            final boolean nameFlg = KNJ_EditEdit.getMS932ByteLength(putwk._finschoolName) > 30 ? true : false;
            if(nameFlg) {
                final String[] name = KNJ_EditEdit.get_token(putwk._finschoolName, 30, 2);
                svf.VrsOutn("NAME" + String.valueOf(col) + "_1", row, name[0]);    //出身学校名
                svf.VrsOutn("NAME" + String.valueOf(col) + "_2", row, name[1]);    //出身学校名
            }else {
                svf.VrsOutn("NAME" + String.valueOf(col) + "_1", row, putwk._finschoolName);    //出身学校名
            }

            svf.VrsOutn("EXAM_NO" + String.valueOf(col), row, "(" + putwk._fsCd + ")");    //学校コード

            row = col == 2 ? row + 1 : row;
            col = col % 2 == 0 ? 1 : 2;
            _hasData = true;
        }

        svf.VrEndPage();
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String receptNo = rs.getString("RECEPTNO");
            	final String finschoolZipcd = StringUtils.defaultString(rs.getString("FINSCHOOL_ZIPCD"));
            	final String finschoolAddr1 = StringUtils.defaultString(rs.getString("FINSCHOOL_ADDR1"));
            	final String finschoolAddr2 = StringUtils.defaultString(rs.getString("FINSCHOOL_ADDR2"));
            	final String finschoolName = ("".equals(StringUtils.defaultString(rs.getString("FINSCHOOL_NAME")))) ? "" : rs.getString("FINSCHOOL_NAME") + "　御中";
            	final String fsCd = StringUtils.defaultString(rs.getString("FS_CD"));

                final PrintData printData = new PrintData(receptNo, finschoolZipcd,  finschoolAddr1, finschoolAddr2, finschoolName, fsCd);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        // *** ここから入学者データの抽出。L508_2と同じ処理をするため、変更は注意。 ***
        stb.append(" WITH SRCHPASSDATA_TBL AS ( ");
        stb.append(" SELECT ");
        stb.append("  T1.ENTEXAMYEAR, ");
        stb.append("  T1.APPLICANTDIV, ");
        stb.append("  T1.TESTDIV, ");
        stb.append("  T1.EXAM_TYPE, ");
        stb.append("  T1.RECEPTNO, ");
        stb.append("  T1.EXAMNO ");
        stb.append(" FROM ");
        stb.append("  ENTEXAM_RECEPT_DAT T1 ");
        stb.append("  INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ");
        stb.append("    ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("  LEFT JOIN NAME_MST T3 ");
        stb.append("    ON T3.NAMECD2 = T1.JUDGEDIV ");
        stb.append("   AND T3.NAMECD1 = 'L013' ");
        stb.append(" WHERE ");
        stb.append("  T1.ENTEXAMYEAR = '"+_param._entexamyear+"' ");
        stb.append("  AND T1.APPLICANTDIV = '"+_param._applicantdiv+"' ");
        if (!"".equals(_param._testdiv)) {
            stb.append("     AND T1.TESTDIV = '"+_param._testdiv+"' ");
        }
        stb.append("  AND T3.NAMESPARE1 = '1' ");
        stb.append("  AND T1.ADJOURNMENTDIV = '1' ");
        stb.append("  AND T1.PROCEDUREDIV1 = '1' ");
        // *** ここまで、合格者データの抽出 ***
        stb.append(" ) ");
        // 以下、表示用データ。
        stb.append(" SELECT ");
        stb.append("  T1.RECEPTNO, ");
        stb.append("  T4.FINSCHOOL_ZIPCD, ");
        stb.append("  T4.FINSCHOOL_ADDR1, ");
        stb.append("  T4.FINSCHOOL_ADDR2, ");
        stb.append("  T4.FINSCHOOL_NAME, ");
        stb.append("  T2.FS_CD ");
        stb.append(" FROM ");
        stb.append("  ENTEXAM_RECEPT_DAT T1 ");
        stb.append("  INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ");
        stb.append("    ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("   AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T2.EXAMNO       = T1.EXAMNO ");
        stb.append("  INNER JOIN SRCHPASSDATA_TBL T3 ");
        stb.append("    ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("   AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T3.TESTDIV      = T1.TESTDIV ");
        stb.append("   AND T3.EXAM_TYPE    = T1.EXAM_TYPE ");
        stb.append("   AND T3.RECEPTNO     = T1.RECEPTNO ");
        stb.append("  LEFT JOIN FINSCHOOL_MST T4 ");
        stb.append("    ON T4.FINSCHOOLCD = T2.FS_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '"+_param._entexamyear+"' ");
        stb.append("     AND T1.APPLICANTDIV = '"+_param._applicantdiv+"' ");
        stb.append(" ORDER BY ");
        stb.append("     RECEPTNO ");
        return stb.toString();
    }


    private class PrintData{
    	final String _receptNo;        //受験番号
    	final String _finschoolZipcd;  //郵便番号
    	final String _finschoolAddr1;  //住所1
    	final String _finschoolAddr2;  //住所2
    	final String _finschoolName;   //出身学校名
    	final String _fsCd;            //学校コード

    	PrintData(
    	    	final String receptNo,
    	    	final String finschoolZipcd,
    	    	final String finschoolAddr1,
    	    	final String finschoolAddr2,
    	    	final String finschoolName,
    	    	final String fsCd
    			) {
    		_receptNo = receptNo;
    		_finschoolZipcd = finschoolZipcd;
    		_finschoolAddr1 = finschoolAddr1;
    		_finschoolAddr2 = finschoolAddr2;
        	_finschoolName = finschoolName;
        	_fsCd = fsCd;
    	}
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72638 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _loginDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
        }
    }
}

// eof

