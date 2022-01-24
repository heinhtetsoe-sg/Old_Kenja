// kanji=漢字
/*
 * $Id: 4c5dc0e1790ff772a20c65706fc5af4073dd681f $
 *
 * 作成日: 2007/11/08 09:20:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

public class KNJWP140S1 {

    private static final Log log = LogFactory.getLog(KNJWP140S1.class);
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
             * 督促状１ 
             */
            if (_param._checkToku1.equals(PRINT_ON)) {
                KNJWP141 objKNJWP141 = new KNJWP141();
                _hasData = objKNJWP141.svf_out(request, response, _form._svf, db2);
            }

            /*
             * 督促状２ 
             */
            if (_param._checkToku2.equals(PRINT_ON)) {
                KNJWP142 objKNJWP142 = new KNJWP142();
                _hasData = objKNJWP142.svf_out(request, response, _form._svf, db2);
            }

            /*
             * 仮想口座付請求書
             */
            if (_param._checkKasou1.equals(PRINT_ON)) {
                KNJWP102 objKNJWP102 = new KNJWP102();

                KNJWP102ParamList _paramList = createKNJWP102ParamList();
                _hasData = objKNJWP102.svf_out(request, response, _form._svf, db2, _paramList);
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
        private final String[] _claimNo;
        private final String[] _seq;
        private final String[] _reissueCnt;
        private final String[] _reClaimCnt;
        private final String[] _slipNo;
        private final String _applicantNo;
        private final String _schregNo;
        private final String _claimDate;
        private final String _timelimitDay1;
        private final String _send;
        private final String _nameCd2;
        private final String _checkToku1;
        private final String _checkToku2;
        private final String _checkKasou1;

        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String[] claimNo,
                final String[] seq,
                final String[] reissueCnt,
                final String[] reClaimCnt,
                final String[] slipNo,
                final String applicantNo,
                final String schregNo,
                final String claimDate,
                final String timelimitDay1,
                final String send,
                final String nameCd2,
                final String checkToku1,
                final String checkToku2,
                final String checkKasou1
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _claimNo = claimNo;
            _seq = seq;
            _reissueCnt = reissueCnt;
            _reClaimCnt = reClaimCnt;
            _slipNo = slipNo;
            _applicantNo = applicantNo;
            _schregNo = schregNo;
            _claimDate = claimDate;
            _timelimitDay1 = timelimitDay1;
            _send = send;
            _nameCd2 = nameCd2;
            _checkToku1 = checkToku1;
            _checkToku2 = checkToku2;
            _checkKasou1 = checkKasou1;
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("LOGIN_DATE"));

        // 請求書番号
        final String[] claimNo = request.getParameterValues("CLAIM_NO[]");
        // 分割回数
        final String[] seq = request.getParameterValues("SEQ[]");
        // 請求回数
        final String[] reissueCnt = request.getParameterValues("REISSUE_CNT[]");
        // 発行回数
        final String[] reClaimCnt = request.getParameterValues("RE_CLAIM_CNT[]");
        // 伝票番号
        final String[] slipNo = request.getParameterValues("SLIP_NO[]");
        // 志願者番号
        final String applicantNo = request.getParameter("APPLICANTNO");
        // 学籍番号
        final String schregNo = request.getParameter("SCHREGNO");
        // 請求日付／作成日
        final String claimDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("CLAIM_DATE"));
        // 納入期限
        final String timelimitDay1 = KNJ_EditDate.H_Format_Haifun(
                request.getParameter("PRINT_TIMELIMIT_DAY") != null ?
                request.getParameter("PRINT_TIMELIMIT_DAY") : "");
        // 送付先
        final String send = request.getParameter("SEND");
        // 送り状の時候の挨拶
        final String nameCd2 = request.getParameter("NAMECD2");

        /*
         * 印刷指示 --------------------------------------------------------------------------
         */
        // 督促状１印刷
        final String checkToku1 = request.getParameter("CHECK_TOKU1") != null ?
                                     request.getParameter("CHECK_TOKU1") : "";
        // 督促状２印刷
        final String checkToku2 = request.getParameter("CHECK_TOKU2") != null ?
                                    request.getParameter("CHECK_TOKU2") : "";
        // 仮想口座付請求印刷
        final String checkKasou1 = request.getParameter("CHECK_KASOU1") != null ?
                                    request.getParameter("CHECK_KASOU1") : "";

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                claimNo,
                seq,
                reissueCnt,
                reClaimCnt,
                slipNo,
                applicantNo,
                schregNo,
                claimDate,
                timelimitDay1,
                send,
                nameCd2,
                checkToku1,
                checkToku2,
                checkKasou1
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

    private KNJWP102ParamList createKNJWP102ParamList() {
        final String year = _param._year;
        final String semester = _param._semester;
        final String programId = _param._programId;
        final String dbName = _param._dbName;
        final String loginDate = _param._loginDate;

        // 請求書番号
        final String[] claimNo = _param._claimNo;
        // 分割回数
        final String[] seq = _param._seq;
        // 請求回数
        final String[] reissueCnt = _param._reissueCnt;
        // 発行回数
        final String[] reClaimCnt = _param._reClaimCnt;
        // 伝票番号
        final String[] slipNo = _param._slipNo;

        // 志願者番号
        final String applicantNo = _param._applicantNo;
        // 請求日付／作成日
        final String claimDate = _param._claimDate;
        // 納入期限
        final String[] timelimitDay = {_param._timelimitDay1};

        /*
         * 本科生印刷指示 --------------------------------------------------------------------------
         */
        // 仮想口座付請求書
        final String checkKasou1 = _param._checkKasou1;
        // 仮想口座付請求書
        final String checkKasou2 = "";
        final String select = _param._send;
        final String schregno = _param._schregNo;

        final KNJWP102ParamList paramList = new KNJWP102ParamList
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                claimNo,
                seq,
                reissueCnt,
                reClaimCnt,
                slipNo,
                applicantNo,
                claimDate,
                timelimitDay,
                checkKasou1,
                checkKasou2,
                select,
                schregno
        );
        return paramList;
    }
}//クラスの括り
