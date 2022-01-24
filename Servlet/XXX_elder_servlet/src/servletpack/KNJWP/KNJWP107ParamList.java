// kanji=漢字
/*
 * $Id: c06e56cc9a6a76907ba3e63759a4a852a93ab9dc $
 *
 * 作成日: 2007/11/09 13:48:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

public class KNJWP107ParamList {
        private String _year;
        private String _semester;
        private String _programId;
        private String _dbName;
        private String _loginDate;
        private String _applicantNo;
        private String _schregNo;     // 学籍番号　null:新入生
        private String _date;         // 請求日付／作成日
        private String _checkOkuri1;  // 1:送り状印刷 本科生
        private String _checkOkuri2;  // 1:送り状印刷 科目履修
        private String[] _claimNo;    // 請求書番号
        private String[] _seq;        // 分割回数
        private String[] _reissueCnt; // 請求回数
        private String[] _reClaimCnt; // 発行回数
        private String[] _slpNo;      // 伝票番号
        private String _date2;        // 納入期限
        private String _select;       // 送り先
        private String _namecd2;      // 時候挨拶

        public KNJWP107ParamList(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String applicantNo,
                final String schregNo,
                final String date,
                final String checkOkuri1,
                final String checkOkuri2,
                final String[] claimNo,
                final String[] seq,
                final String[] reissueCnt,
                final String[] reClaimCnt,
                final String[] slpNo,
                final String date2,
                final String select,
                final String namecd2
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _applicantNo = applicantNo;
            _schregNo = schregNo;
            _date = date;
            _checkOkuri1 = checkOkuri1;
            _checkOkuri2 = checkOkuri2;
            _claimNo = claimNo;
            _seq = seq;
            _reissueCnt = reissueCnt;
            _reClaimCnt = reClaimCnt;
            _slpNo = slpNo;
            _date2 = date2;
            _select = select;
            _namecd2 = namecd2;
        }

        public KNJWP107ParamList() {
            _year = null;
            _semester = null;
            _programId = null;
            _dbName = null;
            _loginDate = null;
            _applicantNo = null;
            _schregNo = null;
            _date = null;
            _checkOkuri1 = null;
            _checkOkuri2 = null;
            _claimNo = null;
            _seq = null;
            _reissueCnt = null;
            _reClaimCnt = null;
            _slpNo = null;
            _date2 = null;
            _select = null;
            _namecd2 = null;
        }

        public String getNamecd2() {
            return _namecd2;
        }

        public void setNamecd2(String namecd2) {
            _namecd2 = namecd2;
        }

        public String getApplicantNo() {
            return _applicantNo;
        }

        public void setApplicantNo(String applicantNo) {
            _applicantNo = applicantNo;
        }

        public String getCheckOkuri1() {
            return _checkOkuri1;
        }

        public void setCheckOkuri1(String checkOkuri1) {
            _checkOkuri1 = checkOkuri1;
        }

        public String getCheckOkuri2() {
            return _checkOkuri2;
        }

        public void setCheckOkuri2(String checkOkuri2) {
            _checkOkuri2 = checkOkuri2;
        }

        public String[] getClaimNo() {
            return _claimNo;
        }

        public void setClaimNo(String[] claimNo) {
            _claimNo = claimNo;
        }

        public String getDate() {
            return _date;
        }

        public void setDate(String date) {
            _date = date;
        }

        public String getDate2() {
            return _date2;
        }

        public void setDate2(String date2) {
            _date2 = date2;
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

        public String getSchregNo() {
            return _schregNo;
        }

        public void setSchregNo(String schregNo) {
            _schregNo = schregNo;
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

        public String[] getSlpNo() {
            return _slpNo;
        }

        public void setSlpNo(String[] slpNo) {
            _slpNo = slpNo;
        }

        public String getYear() {
            return _year;
        }

        public void setYear(String year) {
            _year = year;
        }
} // KNJWP107ParamList

// eof
