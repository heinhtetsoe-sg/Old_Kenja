// kanji=漢字
/*
 * $Id: edbd896970d9d3a89213de057574186ac59de104 $
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

public class KNJWA180 {

    private static final Log log = LogFactory.getLog(KNJWA180.class);
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
             * 転学生徒の必要書類送付願
             */
            if (_param._output1.equals(PRINT_ON)) {
                KNJWA181 objKNJWA181 = new KNJWA181();
                _hasData = objKNJWA181.svf_out(request, response, _form._svf, db2);
            }

            /*
             * 新卒・既卒生徒の必要書類送付願
             */
            if (_param._output3.equals(PRINT_ON)) {
                KNJWA181 objKNJWA181 = new KNJWA181();
                _hasData = objKNJWA181.svf_out(request, response, _form._svf, db2);
            }

            /*
             * 編入生徒の必要書類送付願
             */
            if (_param._output4.equals(PRINT_ON)) {
                KNJWA181 objKNJWA181 = new KNJWA181();
                _hasData = objKNJWA181.svf_out(request, response, _form._svf, db2);
            }

            /*
             * 受領書（指導要録）
             */
            if (_param._output2.equals(PRINT_ON)) {
                KNJWA182 objKNJWA182 = new KNJWA182();
                _hasData = objKNJWA182.svf_out(request, response, _form._svf, db2);
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
        private final String _schregno;
        private final String _type;                 // 証明書種別コード
        private final String _certif1;              // 転学生徒の必要書類送付願連番
        private final String _certif2;              // 受領書連番
        private final String _output1;              // 1:転学生徒の必要書類送付願印刷
        private final String _output2;              // 1:受領書（指導要録）印刷
        private final String _output3;              // 1:新卒・既卒生徒の必要書類送付願
        private final String _output4;              // 1:編入生徒の必要書類送付願

        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String schregno,
                final String type,
                final String certif1,
                final String certif2,
                final String output1,
                final String output2,
                final String output3,
                final String output4
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _schregno = schregno;
            _type = type;
            _certif1 = certif1;
            _certif2 = certif2;
            _output1 = output1;
            _output2 = output2;
            _output3 = output3;
            _output4 = output4;
        }

    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("LOGIN_DATE"));
        final String schregno = request.getParameter("SCHREGNO");
        final String type = request.getParameter("TYPE");
        final String certif1 = request.getParameter("SYORUI");
        final String certif2 = request.getParameter("JYURYOU");
        final String output1 = request.getParameter("CHECK_SYORUI") != null ?
                                    request.getParameter("CHECK_SYORUI") : "";
        final String output2 = request.getParameter("CHECK_JYURYOU") != null ?
                                    request.getParameter("CHECK_JYURYOU") : "";
        final String output3 = request.getParameter("CHECK_SINSOTU") != null ?
                                    request.getParameter("CHECK_SINSOTU") : "";
        final String output4 = request.getParameter("CHECK_HENNYU") != null ?
                                    request.getParameter("CHECK_HENNYU") : "";
        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                schregno,
                type,
                certif1,
                certif2,
                output1,
                output2,
                output3,
                output4
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
