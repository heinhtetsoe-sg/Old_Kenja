// kanji=漢字
/*
 * $Id: 10a942b5090eb181e1721963cd268d6117f3a91d $
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
import servletpack.KNJWP.KNJWP102ParamList;
import servletpack.KNJWP.KNJWP107ParamList;

public class KNJWP100S1 {

    private static final Log log = LogFactory.getLog(KNJWP100S1.class);
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
             * 送り状
             */
            if (_param._checkOkuri1.equals(PRINT_ON) ||
                    _param._checkOkuri2.equals(PRINT_ON)
                ) {
                KNJWP107 objKNJWP107 = new KNJWP107();

                KNJWP107ParamList _paramList = createKNJWP107ParamList();
                _hasData = objKNJWP107.svf_out(request, response, _form._svf, db2, _paramList);
            }

            /*
             * 選考科受領書
             * 登録科受領書
		     */
            if (_param._checkSenkou1.equals(PRINT_ON) ||
                _param._checkTouroku2.equals(PRINT_ON)
            ) {
                
                KNJWP101 objKNJWP101 = new KNJWP101();
                _hasData = objKNJWP101.svf_out(request, response, _form._svf, db2);
            }

            /*
             * 仮想口座付請求書
             */
            if (_param._checkKasou1.equals(PRINT_ON) ||
                _param._checkKasou2.equals(PRINT_ON)
            ) {
                KNJWP102 objKNJWP102 = new KNJWP102();

                KNJWP102ParamList _paramList = createKNJWP102ParamList();
                _hasData = objKNJWP102.svf_out(request, response, _form._svf, db2, _paramList);
            }

            /*
             * 合格通知書(個人) 
             */
            if (_param._checkGoukakuK1.equals(PRINT_ON) && !_param._checkSenkou1.equals(PRINT_ON)) {
                KNJWP103 objKNJWP103 = new KNJWP103();
                _hasData = objKNJWP103.svf_out(request, response, _form._svf, db2);
            }

            /*
             * 合格通知書(学校)
             */
            if (_param._checkGoukakuG1.equals(PRINT_ON)) {
                KNJWP104 objKNJWP104 = new KNJWP104();
                _hasData = objKNJWP104.svf_out(request, response, _form._svf, db2);
            }

            /*
             * 転学回答書(個人)
             */
            if (_param._checkKaitouK1.equals(PRINT_ON)) {
                KNJWP105 objKNJWP105 = new KNJWP105();
                _hasData = objKNJWP105.svf_out(request, response, _form._svf, db2);
            }

            /*
             * 転学回答書(学校)
             */
            if (_param._checkKaitouG1.equals(PRINT_ON)) {
                KNJWP106 objKNJWP106 = new KNJWP106();
                _hasData = objKNJWP106.svf_out(request, response, _form._svf, db2);
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
        private final String _claimDate;
        private final String[] _timelimitDay;
        private final String _checkSenkou1;
        private final String _checkKasou1;
        private final String _checkOkuri1;
        private final String _checkGoukakuK1;
        private final String _checkGoukakuG1;
        private final String _checkKaitouK1;
        private final String _checkKaitouG1;
        private final String _checkTouroku2;
        private final String _checkKasou2;
        private final String _checkOkuri2;
        private final String _jukenCertif;
        private final String _tengakuCertif;
        private final String _select;       // 送り先
        private final String _schregno;
        private final String _namecd2;      // 時候挨拶

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
                final String claimDate,
                final String[] timelimitDay,
                final String checkSenkou1,
                final String checkKasou1,
                final String checkOkuri1,
                final String checkGoukakuK1,
                final String checkGoukakuG1,
                final String checkKaitouK1,
                final String checkKaitouG1,
                final String checkTouroku2,
                final String checkKasou2,
                final String checkOkuri2,
                final String jukenCertif,
                final String tengakuCertif,
                final String select,
                final String schregno,
                final String namecd2
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
            _claimDate = claimDate;
            _timelimitDay = timelimitDay;
            _checkSenkou1 = checkSenkou1;
            _checkKasou1 = checkKasou1;
            _checkOkuri1 = checkOkuri1;
            _checkGoukakuK1 = checkGoukakuK1;
            _checkGoukakuG1 = checkGoukakuG1;
            _checkKaitouK1 = checkKaitouK1;
            _checkKaitouG1 = checkKaitouG1;
            _checkTouroku2 = checkTouroku2;
            _checkKasou2 = checkKasou2;
            _checkOkuri2 = checkOkuri2;
            _jukenCertif = jukenCertif;
            _tengakuCertif = tengakuCertif;
            _select = select;
            _schregno = schregno;
            _namecd2 = namecd2;
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
        // 請求日付／作成日
        final String claimDate = request.getParameter("CLAIM_DATE");
        // 納入期限
        final String[] timelimitDay = {cnvNull(request.getParameter("TIMELIMIT_DAY1")),
                cnvNull(request.getParameter("TIMELIMIT_DAY2")),
                cnvNull(request.getParameter("TIMELIMIT_DAY3")),
                cnvNull(request.getParameter("TIMELIMIT_DAY4")),
                cnvNull(request.getParameter("TIMELIMIT_DAY5")),
                cnvNull(request.getParameter("TIMELIMIT_DAY6")),
                cnvNull(request.getParameter("TIMELIMIT_DAY7")),
                cnvNull(request.getParameter("TIMELIMIT_DAY8")),
                cnvNull(request.getParameter("TIMELIMIT_DAY9")),
                cnvNull(request.getParameter("TIMELIMIT_DAY10")),
                cnvNull(request.getParameter("TIMELIMIT_DAY11")),
                cnvNull(request.getParameter("TIMELIMIT_DAY12"))};

        /*
         * 本科生印刷指示 --------------------------------------------------------------------------
         */
        // 選考科受領書
        final String checkSenkou1 = cnvNull(request.getParameter("CHECK_SENKOU1"));
        // 仮想口座付請求書
        final String checkKasou1 = cnvNull(request.getParameter("CHECK_KASOU1"));
        // 送り状
        final String checkOkuri1 = cnvNull(request.getParameter("CHECK_OKURI1"));
        // 合格通知書(個人)
        final String checkGoukakuK1 = cnvNull(request.getParameter("CHECK_GOUKAKU_K1"));
        // 合格通知書(学校)
        final String checkGoukakuG1 = cnvNull(request.getParameter("CHECK_GOUKAKU_G1"));
        // 転学回答書(個人)
        final String checkKaitouK1 = cnvNull(request.getParameter("CHECK_KAITOU_K1"));
        // 転学回答書(学校)
        final String checkKaitouG1 = cnvNull(request.getParameter("CHECK_KAITOU_G1"));
        /*
         * 科目履修印刷指示 ------------------------------------------------------------------------
         */
        // 登録科受領書
        final String checkTouroku2 = cnvNull(request.getParameter("CHECK_TOUROKU2"));
        // 仮想口座付請求書
        final String checkKasou2 = cnvNull(request.getParameter("CHECK_KASOU2"));
        // 送り状
        final String checkOkuri2 = cnvNull(request.getParameter("CHECK_OKURI2"));
        // 受験結果報告書・証明書番号
        final String jukenCertif = cnvNull(request.getParameter("GOUKAKUG1_VAL"));
        // 転学照会に対する回答・証明書番号
        final String tengakuCertif = cnvNull(request.getParameter("KAITOUG1_VAL"));
        final String select = request.getParameter("SEND");
        final String schregno = request.getParameter("SCHREGNO");
        final String nameCd2 = request.getParameter("HELLO");

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
                claimDate,
                timelimitDay,
                checkSenkou1,
                checkKasou1,
                checkOkuri1,
                checkGoukakuK1,
                checkGoukakuG1,
                checkKaitouK1,
                checkKaitouG1,
                checkTouroku2,
                checkKasou2,
                checkOkuri2,
                jukenCertif,
                tengakuCertif,
                select,
                schregno,
                nameCd2
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

    private String cnvNull(String str) {
        if (str == null) {
            return "";
        }

        return str;
    }

    private String[] cnvNull(String[] str) {
        if (str == null) {
            String[] strDummy = {""};
            return strDummy;
        }

        return str;
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
        final String[] timelimitDay = _param._timelimitDay;

        /*
         * 本科生印刷指示 --------------------------------------------------------------------------
         */
        // 仮想口座付請求書
        final String checkKasou1 = _param._checkKasou1;
        // 仮想口座付請求書
        final String checkKasou2 = _param._checkKasou2;
        final String select = _param._select;
        final String schregno = _param._schregno;

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

    private KNJWP107ParamList createKNJWP107ParamList() {
        final String year = _param._year;
        final String semester = _param._semester;
        final String programId = _param._programId;
        final String dbName = _param._dbName;
        final String loginDate = _param._loginDate;
        final String applicantNo = _param._applicantNo;
        final String schregNo = _param._schregno;
        final String date = _param._claimDate;
        final String checkOkuri1 = _param._checkOkuri1;
        final String checkOkuri2 = _param._checkOkuri2;
        final String[] claimNo = _param._claimNo;
        final String[] seq = _param._seq;
        final String[] reissueCnt = _param._reissueCnt;
        final String[] reClaimCnt = _param._reClaimCnt;
        final String[] slpNo = _param._slipNo;
        final String timelimitDay = _param._timelimitDay[0];
        final String select = _param._select;
        final String nameCd2 = _param._namecd2;

        final KNJWP107ParamList paramList = new KNJWP107ParamList
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                applicantNo,
                schregNo,
                date,
                checkOkuri1,
                checkOkuri2,
                claimNo,
                seq,
                reissueCnt,
                reClaimCnt,
                slpNo,
                timelimitDay,
                select,
                nameCd2
        );
        return paramList;
    }
}//クラスの括り
