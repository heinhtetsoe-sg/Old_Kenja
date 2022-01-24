/*
 * $Id: c24cffa32df8380bfe2cb41c60e05e0fe6a7656b $
 *
 * 作成日: 2012/10/23
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 教科書・学習書給与申請に係る審査結果通知書
 */
public class KNJM809 {

    private static final Log log = LogFactory.getLog(KNJM809.class);

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
        final String sql = sql();
        log.debug(" sql =" + sql);
        
        final Map printDateCache = new HashMap();
        
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	
        	final Map row = (Map) it.next();
        	
        	svf.VrSetForm("KNJM809.frm", 1);
        	
        	svf.VrsOut("ZIP_NO", KnjDbUtils.getString(row, "ZIPCD"));
        	
        	final String addr1 = KnjDbUtils.getString(row, "ADDR1");
        	final String addr2 = KnjDbUtils.getString(row, "ADDR2");
        	if ((KNJ_EditEdit.getMS932ByteLength(addr1) > 50 || KNJ_EditEdit.getMS932ByteLength(addr2) > 50)) {
        		svf.VrsOut("ADDRESS1_3", addr1);
        		svf.VrsOut("ADDRESS2_3", addr2);
        	} else if ((KNJ_EditEdit.getMS932ByteLength(addr1) > 40 || KNJ_EditEdit.getMS932ByteLength(addr2) > 40)) {
        		svf.VrsOut("ADDRESS1_2", addr1);
        		svf.VrsOut("ADDRESS2_2", addr2);
        	} else {
        		svf.VrsOut("ADDRESS1", addr1);
        		svf.VrsOut("ADDRESS2", addr2);
        	}
        	final String name = KnjDbUtils.getString(row, "NAME");
        	if (KNJ_EditEdit.getMS932ByteLength(name) > 20) {
        		svf.VrsOut("NAME", name);
        	} else if (KNJ_EditEdit.getMS932ByteLength(name) > 0) {
        		svf.VrsOut("NAME", name + " 様");
        	}
        	svf.VrsOut("SCH_NO", KnjDbUtils.getString(row, "SCHREGNO"));
        	
        	final String judgeResult = KnjDbUtils.getString(row, "JUDGE_RESULT");
			if ("2".equals(judgeResult)) {
        		svf.VrsOut("RESULT", "教科書学習書を無償で給与する。");
        	} else if ("3".equals(judgeResult)) {
        		svf.VrsOut("RESULT", "教科書学習書を有償とする。");
        	}
        	
        	final String decisionDate = KnjDbUtils.getString(row, "DECISION_DATE");
        	if (null != decisionDate) {
        		if (!printDateCache.containsKey(decisionDate)) {
        			printDateCache.put(decisionDate, KNJ_EditDate.h_format_JP(db2, decisionDate));
        		}
        		svf.VrsOut("DATE", (String) printDateCache.get(decisionDate));
        	}
        	svf.VrsOut("SCHOOL_NAME", KnjDbUtils.getString(row, "SCHOOL_NAME"));
        	svf.VrsOut("PRESIDENT", KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
        	
        	svf.VrEndPage();
        	
        	_hasData = true;
        }
    }

    public String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     L1.SEND_ZIPCD AS ZIPCD, ");
        stb.append("     L1.SEND_ADDR1 AS ADDR1, ");
        stb.append("     L1.SEND_ADDR2 AS ADDR2, ");
        stb.append("     L1.SEND_NAME AS NAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.JUDGE_RESULT, ");
        stb.append("     T1.DECISION_DATE, ");
        stb.append("     L2.SCHOOL_NAME, ");
        stb.append("     L2.PRINCIPAL_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_TEXTBOOK_FREE_APPLY_DAT T1 ");
        stb.append("     INNER JOIN (SELECT SCHREGNO, YEAR, MAX(REGISTER_DATE) AS REGISTER_DATE ");
        stb.append("                 FROM SCHREG_TEXTBOOK_FREE_APPLY_DAT T1 ");
        stb.append("                 GROUP BY SCHREGNO, YEAR) TT1 ON TT1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                                             AND TT1.YEAR = T1.YEAR ");
        stb.append("                                             AND TT1.REGISTER_DATE = T1.REGISTER_DATE ");
        stb.append("     LEFT JOIN SCHREG_SEND_ADDRESS_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND L1.DIV = '1' ");
        stb.append("     LEFT JOIN CERTIF_SCHOOL_DAT L2 ON L2.CERTIF_KINDCD = '121' ");
        stb.append("         AND L2.YEAR = T1.YEAR ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categoryName) + " ");
        if ("1".equals(_param._output)) {
            stb.append("     AND JUDGE_RESULT = '2' ");
            stb.append("     AND DECISION_DATE IS NOT NULL ");
        } else if ("2".equals(_param._output)) {
            stb.append("     AND JUDGE_RESULT = '3' ");
            stb.append("     AND DECISION_DATE IS NOT NULL ");
        } else if ("3".equals(_param._output)) {
            stb.append("     AND (JUDGE_RESULT = '2' OR JUDGE_RESULT = '3') ");
            stb.append("     AND DECISION_DATE IS NOT NULL ");
        }
        stb.append(" ORDER BY T1.SCHREGNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63400 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _ctrlDate;
        private final String _output;
        private final String[] _categoryName;
        private final String _useAddrField2;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _output = request.getParameter("OUTPUT");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _categoryName = request.getParameterValues("category_name");
            _useAddrField2 = request.getParameter("useAddrField2");
        }
    }
}

// eof

