// kanji=漢字
/*
 * $Id: f58efcb3778f0bd9509e9bc6804e6f27284eb71d $
 *
 * 作成日: 2007/11/08 09:20:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJWP.KNJWP102ParamList;
import servletpack.KNJWP.KNJWP107ParamList;

public class KNJWP110 {

    private static final Log log = LogFactory.getLog(KNJWP110.class);
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

            for (int i = 0; i < _param._applicantNo.length; i++) {
                final String applicantNo = _param._applicantNo[i];
                log.debug(">>志願者番号=" + applicantNo);

                // 帳票出力クラス呼び出し処理
                printSvf(request, response, i);
            }
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
	}

    /**
     *  帳票クラス呼び出し処理
     *  
     */
    private void printSvf(HttpServletRequest request, HttpServletResponse response, final int i)
        throws Exception{
		try {
            ApplicantBaseMst applicantBaseMst = createApplicant(db2, i);

            /*
             * 仮想口座付請求書
             */
            KNJWP102 objKNJWP102 = new KNJWP102();
            KNJWP102ParamList _KNJWP102paramList = createKNJWP102ParamList(i, applicantBaseMst);
            _hasData = objKNJWP102.svf_out(request, response, _form._svf, db2, _KNJWP102paramList);

            /*
             * 送り状
             */
            KNJWP107 objKNJWP107 = new KNJWP107();
            KNJWP107ParamList _KNJWP107paramList = createKNJWP107ParamList(i, applicantBaseMst);
            _hasData = objKNJWP107.svf_out(request, response, _form._svf, db2, _KNJWP107paramList);

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
        private final String[] _applicantNo;
        private final String _claimDate;
        private final String _timelimitDay1;
        private final String _namecd2;      // 時候挨拶

        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String[] applicantNo,
                final String claimDate,
                final String timelimitDay1,
                final String namecd2
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _applicantNo = applicantNo;
            _claimDate = claimDate;
            _timelimitDay1 = timelimitDay1;
            _namecd2 = namecd2;
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = request.getParameter("LOGIN_DATE");

        // 志願者番号
        final String[] applicantNo = request.getParameterValues("APPLICANTNO[]");
        // 請求日付／作成日
        final String claimDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("CLAIM_DATE"));
        // 納入期限
        final String timelimitDay1 = KNJ_EditDate.H_Format_Haifun(cnvNull(request.getParameter("TIMELIMIT_DAY1")));
        final String nameCd2 = request.getParameter("HELLO");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                applicantNo,
                claimDate,
                timelimitDay1,
                nameCd2
        );
        return param;
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $");
        KNJServletUtils.debugParam(request, log);
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

    // ======================================================================
    /**
     * 志願者基礎マスタ。
     */
    private class ApplicantBaseMst {
        private final String _schregNo;

        private Student _student;
        private ClaimPrintHistDat _claimPrintHistDat;

        ApplicantBaseMst(
                final String schregNo
        ) {
            _schregNo = schregNo;
        }

        public void load(DB2UDB db2, int i) throws SQLException {
            _student = createStudent(db2, _schregNo, i);
            _claimPrintHistDat = createClaimPrintHistDat(db2, i);
        }
    }

    public ApplicantBaseMst createApplicant(DB2UDB db2, final int i) throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlApplicantBaseMst(i));
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo = rs.getString("schregNo");

                final ApplicantBaseMst applicantBaseMst = new ApplicantBaseMst(
                        schregNo
                );
                
                applicantBaseMst.load(db2, i);
                
                return applicantBaseMst;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        log.debug(">>>APPLICANT_BASE_MST に該当するものがありません。：志願者番号(" + _param._applicantNo[i] + ")");
        return null;
    }

    private String sqlApplicantBaseMst(final int i) {
        return " select"
        + "    SCHREGNO as schregNo"
        + " from"
        + "    APPLICANT_BASE_MST"
        + " where"
        + "    APPLICANTNO = '" + _param._applicantNo[i] + "'"
        ;
    }

    // ======================================================================
    /**
     * 学籍。学籍基礎マスタ。
     */
    private class Student {
        private final String _claimSend;

        Student(final String claimSend
        ) {
            _claimSend = claimSend;
        }
    }

    private Student createStudent(final DB2UDB db2, String schregno, final int i) throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlStudents(schregno));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String claimSend = rs.getString("claimSend");

                final Student studentDat = new Student(
                        claimSend
                );
                return studentDat;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        log.debug(">>>SCHREG_BASE_MST に該当するものがありません。：学籍番号(" + schregno + ")");
        return null;
    }

    private String sqlStudents(String schregno) {
        return " select"
                + "    CLAIM_SEND as claimSend"
                + " from"
                + "    SCHREG_BASE_MST"
                + " where" 
                + "    SCHREGNO = '" + schregno + "'";
    }

    // ======================================================================
    /**
     * 請求書発行履歴データ。
     */
    private class ClaimPrintHistDat {
        private final String _claimNo;
        private final String _seq;
        private final String _reissueCnt;
        private final String _reClaimCnt;
        private final String _slipNo;

        ClaimPrintHistDat(
                final String claimNo,
                final String seq,
                final String reissueCnt,
                final String reClaimCnt,
                final String slipNo
        ) {
            _claimNo = claimNo;
            _seq = seq;
            _reissueCnt = reissueCnt;
            _reClaimCnt = reClaimCnt;
            _slipNo = slipNo;
        }
    }

    public ClaimPrintHistDat createClaimPrintHistDat(DB2UDB db2, int i) throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlClaimPrintHistDat(i));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String claimNo = rs.getString("claimNo");
            final String seq = rs.getString("seq");
            final String reissueCnt = rs.getString("reissueCnt");
            final String reClaimCnt = rs.getString("reClaimCnt");
            final String slipNo = rs.getString("slipNo");

            final ClaimPrintHistDat claimPrintHistDat = new ClaimPrintHistDat(
                    claimNo,
                    seq,
                    reissueCnt,
                    reClaimCnt,
                    slipNo
            );

            return claimPrintHistDat;
        }

        log.debug(">>>CLAIM_PRINT_HIST_DAT に該当するものがありません。：志願者番号(" + _param._applicantNo[i] + ")");
        return null;
    }

    private String sqlClaimPrintHistDat(int i) {
        return " select"
                + "    CLAIM_NO as claimNo,"
                + "    SEQ as seq,"
                + "    REISSUE_CNT as reissueCnt,"
                + "    RE_CLAIM_CNT as reClaimCnt,"
                + "    SLIP_NO as slipNo"
                + " from"
                + "    CLAIM_PRINT_HIST_DAT"
                + " where"
                + "    APPLICANTNO = '" + _param._applicantNo[i] + "'"
                + " order by CLAIM_NO DESC, SEQ, REISSUE_CNT, RE_CLAIM_CNT, SLIP_NO"
                ;
    }

    private class ParamList {
        private final String _year;
        private final String _semester;
        private final String _programId;
        private final String _dbName;
        private final String _loginDate;
        private final String[] _applicantNo;
        private final String _claimDate;
        private final String _timelimitDay1;
        private final String _namecd2;      // 時候挨拶

        public ParamList(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String[] applicantNo,
                final String claimDate,
                final String timelimitDay1,
                final String namecd2
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _applicantNo = applicantNo;
            _claimDate = claimDate;
            _timelimitDay1 = timelimitDay1;
            _namecd2 = namecd2;
        }
    }

    private ParamList createParamList(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("LOGIN_DATE"));

        // 志願者番号
        final String[] applicantNo = request.getParameterValues("APPLICANTNO[]");
        // 請求日付／作成日
        final String claimDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("CLAIM_DATE"));
        // 納入期限
        final String timelimitDay1 = KNJ_EditDate.H_Format_Haifun(
                request.getParameter("TIMELIMIT_DAY1") != null ?
                request.getParameter("TIMELIMIT_DAY1") : "");
        final String nameCd2 = request.getParameter("HELLO");


        final ParamList paramList = new ParamList
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                applicantNo,
                claimDate,
                timelimitDay1,
                nameCd2
        );
        return paramList;
    }

    private KNJWP102ParamList createKNJWP102ParamList(final int i, ApplicantBaseMst applicantBaseMst) {
        final String year = _param._year;
        final String semester = _param._semester;
        final String programId = _param._programId;
        final String dbName = _param._dbName;
        final String loginDate = _param._loginDate;

        // 請求書番号
        final String[] claimNo = {applicantBaseMst._claimPrintHistDat._claimNo};
        // 分割回数
        final String[] seq = {applicantBaseMst._claimPrintHistDat._seq};
        // 請求回数
        final String[] reissueCnt = {applicantBaseMst._claimPrintHistDat._reissueCnt};
        // 発行回数
        final String[] reClaimCnt = {applicantBaseMst._claimPrintHistDat._reClaimCnt};
        // 伝票番号
        final String[] slipNo = {applicantBaseMst._claimPrintHistDat._slipNo};

        // 志願者番号
        final String applicantNo = _param._applicantNo[i];
        // 請求日付／作成日
        final String claimDate = _param._claimDate;
        // 納入期限
        final String[] timelimitDay = {_param._timelimitDay1};

        /*
         * 本科生印刷指示 --------------------------------------------------------------------------
         */
        // 仮想口座付請求書
        final String checkKasou1 = "1";
        // 仮想口座付請求書
        final String checkKasou2 = "1";
        final String select = applicantBaseMst._student._claimSend;
        final String schregno = applicantBaseMst._schregNo;

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

    private KNJWP107ParamList createKNJWP107ParamList(final int i, ApplicantBaseMst applicantBaseMst) {
        final String year = _param._year;
        final String semester = _param._semester;
        final String programId = _param._programId;
        final String dbName = _param._dbName;
        final String loginDate = _param._loginDate;
        final String applicantNo = _param._applicantNo[i];
        final String schregNo = applicantBaseMst._schregNo;
        final String date = _param._claimDate;
        final String checkOkuri1 = "1";
        final String checkOkuri2 = "1";
        final String[] claimNo = {applicantBaseMst._claimPrintHistDat._claimNo};
        final String[] seq = {applicantBaseMst._claimPrintHistDat._seq};
        final String[] reissueCnt = {applicantBaseMst._claimPrintHistDat._reissueCnt};
        final String[] reClaimCnt = {applicantBaseMst._claimPrintHistDat._reClaimCnt};
        final String[] slpNo = {applicantBaseMst._claimPrintHistDat._slipNo};
        final String timelimitDay1 = _param._timelimitDay1;
        final String select = applicantBaseMst._student._claimSend;
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
                timelimitDay1,
                select,
                nameCd2
        );
        return paramList;
    }
}//クラスの括り
