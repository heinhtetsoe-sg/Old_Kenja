// kanji=漢字
/*
 * $Id: 6f38252abb2780225ae626c45bc9a4c686e0e7f3 $
 *
 * 作成日: 2007/11/08 09:20:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWA;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

public class KNJWA170 {

    private static final Log log = LogFactory.getLog(KNJWA170.class);
    private static final String PRINT_ON = "1";

    private Form _form;
    private boolean _hasData;

    private DB2UDB db2;

    Param _param;

	/**
	  *  KNJWP.classから最初に起動されるクラス
	  **/
	public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        dumpParam(request);
        _param = createParam(request);
        _form = new Form(response);

        db2 = null;       

        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            // 帳票出力クラス呼び出し処理
            printSvf(request, response);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
	}

    /**
     *  帳票クラス呼び出し処理
     *  
     */
    private void printSvf(HttpServletRequest request, HttpServletResponse response)
        throws Exception{
		try {
            /*
             * 生徒証
             */
            if (_param._output1.equals(PRINT_ON)) {
                KNJWA171 objKNJWA171 = new KNJWA171();
                _hasData = objKNJWA171.svf_out(request, response, _form._svf, db2);
            }

            /*
             * 通学証明書
             */
            if (_param._output2.equals(PRINT_ON)) {
                KNJWA172 objKNJWA172 = new KNJWA172();
                _hasData = objKNJWA172.svf_out(request, response, _form._svf, db2);
            }

            /*
             * 賃割引証
             */
            if (_param._output3.equals(PRINT_ON)) {
                KNJWA173 objKNJWA173 = new KNJWA173();
                _hasData = objKNJWA173.svf_out(request, response, _form._svf, db2);
            }

		} catch( Exception e ){
            log.error("Exception:", e);
		}
    }

    private boolean openDb(final DB2UDB db2) {
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return true;
        }
        return false;
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    // ======================================================================
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _programId;
        private final String _dbName;
        private final String _loginDate;
        private final String[] _schregno;
        private final String[] _certif1;            // 通学証明書番号
        private final String[] _certif2;            // 運賃割引証番号
        private final String _type;                 // 証明書種別コード
        private final String _output1;              // 1:生徒証印刷
        private final String _output2;              // 1:通学証明書印刷
        private final String _output3;              // 1:運賃割引証印刷
        private final String _docBase;
        private final String _public_date;          // 発効日
        private final String _effect_date;          // 有効日
        private final String[] _certif0;            // 生徒証

        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String[] schregno,
                final String[] certif1,
                final String[] certif2,
                final String type,
                final String output1,
                final String output2,
                final String output3,
                final String docBase,
                final String public_date,
                final String effect_date,
                final String[] certif0
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _schregno = schregno;
            _certif1 = certif1;
            _certif2 = certif2;
            _type = type;
            _output1 = output1;
            _output2 = output2;
            _output3 = output3;
            _docBase = docBase;
            _public_date = public_date;
            _effect_date = effect_date;
            _certif0 = certif0;
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("LOGIN_DATE"));
        final String[] schregno = request.getParameterValues("SCHREGNO[]");

        String[] certif1 = null;
        if (request.getParameterValues("TUGAKU[]") != null) {
            certif1 = request.getParameterValues("TUGAKU[]");
        }

        String[] certif2 = null;
        if (request.getParameterValues("UNTIN[]") != null) {
            certif2 = request.getParameterValues("UNTIN[]");
        }

        final String type = request.getParameter("TYPE");
        final String output1 = request.getParameter("CHECK_SEITO") != null ?
                                    request.getParameter("CHECK_SEITO") : "";
        final String output2 = request.getParameter("CHECK_TUGAKU") != null ?
                                    request.getParameter("CHECK_TUGAKU") : "";
        final String output3 = request.getParameter("CHECK_UNTIN") != null ?
                                    request.getParameter("CHECK_UNTIN") : "";
        final String docBase = request.getParameter("DOCUMENTROOT");
        final String public_date = request.getParameter("DATE");
        final String effect_date = request.getParameter("END_DATE");

        String[] certif0 = null;
        if (request.getParameterValues("SEITO[]") != null) {
            certif0 = request.getParameterValues("SEITO[]");
        }

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                schregno,
                certif1,
                certif2,
                type,
                output1,
                output2,
                output3,
                docBase,
                public_date,
                effect_date,
                certif0
        );
        return param;
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }
    private class Form {
        private Vrw32alp _svf;

        public Form(final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();
            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");
        }

        private void closeSvf() {
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }

            final int ret = _svf.VrQuit();
            log.info("===> VrQuit():" + ret);
        }
    }
}//クラスの括り
