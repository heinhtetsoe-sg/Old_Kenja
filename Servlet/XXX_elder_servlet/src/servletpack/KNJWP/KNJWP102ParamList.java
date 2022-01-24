// kanji=漢字
/*
 * $Id: 4bdbc096fc5923c11d9ce2e7c7a38fe8f836e7fb $
 *
 * 作成日: 2007/11/09 16:33:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

// ======================================================================
public class KNJWP102ParamList {
    private String _year;
    private String _semester;
    private String _programId;
    private String _dbName;
    private String _loginDate;
    private String[] _claimNo;
    private String[] _seq;
    private String[] _reissueCnt;
    private String[] _reClaimCnt;
    private String[] _slipNo;
    private String _applicantNo;
    private String _claimDate;
    private String[] _timelimitDay;
    private String _checkKasou1;
    private String _checkKasou2;
    private String _select;       // 送り先
    private String _schregno;

    public KNJWP102ParamList(
            String year,
            String semester,
            String programId,
            String dbName,
            String loginDate,
            String[] claimNo,
            String[] seq,
            String[] reissueCnt,
            String[] reClaimCnt,
            String[] slipNo,
            String applicantNo,
            String claimDate,
            String[] timelimitDay,
            String checkKasou1,
            String checkKasou2,
            String select,
            String schregno) {

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
        _checkKasou1 = checkKasou1;
        _checkKasou2 = checkKasou2;
        _select = select;
        _schregno = schregno;
    }

    public KNJWP102ParamList() {
        _year = null;
        _semester = null;
        _programId = null;
        _dbName = null;
        _loginDate = null;
        _claimNo = null;
        _seq = null;
        _reissueCnt = null;
        _reClaimCnt = null;
        _slipNo = null;
        _applicantNo = null;
        _claimDate = null;
        _timelimitDay = null;
        _checkKasou1 = null;
        _checkKasou2 = null;
        _select = null;
        _schregno = null;
    }

    public String getApplicantNo() {
        return _applicantNo;
    }

    public void setApplicantNo(String applicantNo) {
        _applicantNo = applicantNo;
    }

    public String getCheckKasou1() {
        return _checkKasou1;
    }

    public void setCheckKasou1(String checkKasou1) {
        _checkKasou1 = checkKasou1;
    }

    public String getCheckKasou2() {
        return _checkKasou2;
    }

    public void setCheckKasou2(String checkKasou2) {
        _checkKasou2 = checkKasou2;
    }

    public String getClaimDate() {
        return _claimDate;
    }

    public void setClaimDate(String claimDate) {
        _claimDate = claimDate;
    }

    public String[] getClaimNo() {
        return _claimNo;
    }

    public void setClaimNo(String[] claimNo) {
        _claimNo = claimNo;
    }

    public String getDbName() {
        return _dbName;
    }

    public void setDbName(String dbName) {
        _dbName = dbName;
    }

    public String getLoginDate() {
        return _loginDate;
    }

    public void setLoginDate(String loginDate) {
        _loginDate = loginDate;
    }

    public String getProgramId() {
        return _programId;
    }

    public void setProgramId(String programId) {
        _programId = programId;
    }

    public String[] getReClaimCnt() {
        return _reClaimCnt;
    }

    public void setReClaimCnt(String[] reClaimCnt) {
        _reClaimCnt = reClaimCnt;
    }

    public String[] getReissueCnt() {
        return _reissueCnt;
    }

    public void setReissueCnt(String[] reissueCnt) {
        _reissueCnt = reissueCnt;
    }

    public String getSchregno() {
        return _schregno;
    }

    public void setSchregno(String schregno) {
        _schregno = schregno;
    }

    public String getSelect() {
        return _select;
    }

    public void setSelect(String select) {
        _select = select;
    }

    public String getSemester() {
        return _semester;
    }

    public void setSemester(String semester) {
        _semester = semester;
    }

    public String[] getSeq() {
        return _seq;
    }

    public void setSeq(String[] seq) {
        _seq = seq;
    }

    public String[] getSlipNo() {
        return _slipNo;
    }

    public void setSlipNo(String[] slipNo) {
        _slipNo = slipNo;
    }

    public String[] getTimelimitDay() {
        return _timelimitDay;
    }

    public void setTimelimitDay(String[] timelimitDay) {
        _timelimitDay = timelimitDay;
    }

    public String getYear() {
        return _year;
    }

    public void setYear(String year) {
        _year = year;
    }
} // KNJWP102ParamList
// eof
