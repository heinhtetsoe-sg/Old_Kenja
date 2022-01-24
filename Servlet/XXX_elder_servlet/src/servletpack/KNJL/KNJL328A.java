/*
 * $Id: 5d57dd9269a565ad3fea9e703263303180f6d402 $
 *
 * 作成日: 2020/04/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

public class KNJL328A {

    private static final Log log = LogFactory.getLog(KNJL328A.class);

    private final String JUDGE_PASS = "1";
    private final String JUDGE_UNPASS = "0";

    private final String SCHOOLKIND_J = "J";
    private final String SCHOOLKIND_H = "H";

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
        final List printList = getList(db2);

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();

            svf.VrSetForm("KNJL328A.frm", 1);

            printStudent(svf, student);

            printTransferForms(svf);

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printStudent(final Vrw32alp svf, final Student student) {
        //2
        if (!StringUtils.isEmpty(student._zipcd)) {
            final String[] zipcdArray = StringUtils.split(student._zipcd, "-");
            for (int i = 0; i < zipcdArray.length; i++) {
                svf.VrsOut("ZIPNO" + String.valueOf(i+1), zipcdArray[i]);
            }
        }
        final List addrList1 = KNJ_EditKinsoku.getTokenList(student._addr1, 30);
        final List addrList2 = KNJ_EditKinsoku.getTokenList(student._addr2, 30);
        int addrLine = 0;
        for (int i = 0; i < addrList1.size(); i++) {
            addrLine++;
            svf.VrsOut("ADDR1_" + String.valueOf(addrLine), (String) addrList1.get(i));
        }
        for (int i = 0; i < addrList2.size(); i++) {
            addrLine++;
            svf.VrsOut("ADDR1_" + String.valueOf(addrLine), (String) addrList2.get(i));
        }
        final String receptno = " (" + student._receptno + ")";
        final int namelen = KNJ_EditEdit.getMS932ByteLength(student._name);
        if (namelen <= 32) {
            final int namelen2 = KNJ_EditEdit.getMS932ByteLength(student._name + receptno);
            final String nameField = namelen2 > 34 ? "2" : "1";
            svf.VrsOut("NAME1_" + nameField, student._name + receptno);
        } else {
            final String nameField = namelen > 34 ? "2" : "1";
            svf.VrsOut("NAME1_" + nameField, student._name);
            svf.VrsOut("EXAMNO1", receptno);
        }
        if (!StringUtils.isEmpty(student._telno)) {
            final String[] telnoArray = StringUtils.split(student._telno, "-");
            for (int i = 0; i < telnoArray.length; i++) {
                svf.VrsOut("TELNO1_" + String.valueOf(i+1), telnoArray[i]);
            }
        }

        //3
        final List addrList2_1 = KNJ_EditKinsoku.getTokenList(student._addr1, 20);
        addrLine = 0;
        for (int i = 0; i < addrList2_1.size(); i++) {
            addrLine++;
            svf.VrsOut("ADDR2_" + String.valueOf(addrLine), (String) addrList2_1.get(i));
        }
        if (namelen <= 20) {
            final int namelen2 = KNJ_EditEdit.getMS932ByteLength(student._name + receptno);
            final String nameField = namelen2 > 30 ? "3" : namelen2 > 20 ? "2" : "1";
            svf.VrsOut("NAME2_" + nameField, student._name + receptno);
        } else {
            final String nameField = namelen > 30 ? "3" : namelen > 20 ? "2" : "1";
            svf.VrsOut("NAME2_" + nameField, student._name);
            svf.VrsOut("EXAMNO2", receptno);
        }
    }

    private void printTransferForms(final Vrw32alp svf) {
        final TransferForms transferForms = (TransferForms) _param._transferFormsMap.get(_param._patternCd);
        if (null == transferForms) return;

        //1
        svf.VrsOut("AREA_NAME", transferForms._prefName);
        svf.VrsOut("ACC_NO1", transferForms._accountNumber1);
        svf.VrsOut("ACC_NO2", transferForms._accountNumber2);
        svf.VrsOut("ACC_NO3", transferForms._accountNumber3);
        svf.VrsOut("TOTAL_MONEY", transferForms._transferMoney);
        svf.VrsOut("ACC_NAME", transferForms._member);
        svf.VrsOut("ITEM_TITLE", transferForms._subject);
        final List communicationList = KNJ_EditKinsoku.getTokenList(transferForms._communication, 60);
        for (int i = 0; i < communicationList.size(); i++) {
            svf.VrsOutn("ITEM_NAME", i+1, (String) communicationList.get(i));
        }

        //3
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptno = rs.getString("RECEPTNO");
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String zipcd = rs.getString("ZIPCD");
                final String addr1 = rs.getString("ADDRESS1");
                final String addr2 = rs.getString("ADDRESS2");
                final String telno = rs.getString("TELNO");

                final Student student = new Student(receptno, examno, name, zipcd, addr1, addr2, telno);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     RECEPT.RECEPTNO, ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     ADDR.ZIPCD, ");
        stb.append("     ADDR.ADDRESS1, ");
        stb.append("     ADDR.ADDRESS2, ");
        stb.append("     ADDR.TELNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD006 ");
        stb.append("          ON RECEPT.ENTEXAMYEAR = RD006.ENTEXAMYEAR ");
        stb.append("         AND RECEPT.APPLICANTDIV = RD006.APPLICANTDIV ");
        stb.append("         AND RECEPT.TESTDIV = RD006.TESTDIV ");
        stb.append("         AND RECEPT.EXAM_TYPE = RD006.EXAM_TYPE ");
        stb.append("         AND RECEPT.RECEPTNO = RD006.RECEPTNO ");
        stb.append("         AND RD006.SEQ = '006' ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("          ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("         AND BASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ");
        stb.append("          ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("         AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("         AND ADDR.EXAMNO = BASE.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B029 ");
        stb.append("          ON RECEPT.ENTEXAMYEAR = B029.ENTEXAMYEAR ");
        stb.append("         AND RECEPT.APPLICANTDIV = B029.APPLICANTDIV ");
        stb.append("         AND RECEPT.EXAMNO = B029.EXAMNO ");
        stb.append("         AND B029.SEQ = '029' ");
        stb.append("   LEFT JOIN V_NAME_MST L013_1 ");
        stb.append("      ON L013_1.YEAR     = RD006.ENTEXAMYEAR ");
        stb.append("     AND L013_1.NAMECD1  = 'L" + _param._schoolkind + "13' ");
        stb.append("     AND L013_1.NAMECD2  = RD006.REMARK8 "); //専願合格コース
        stb.append("   LEFT JOIN V_NAME_MST L013_2 ");
        stb.append("      ON L013_2.YEAR     = RD006.ENTEXAMYEAR ");
        stb.append("     AND L013_2.NAMECD1  = 'L" + _param._schoolkind + "13' ");
        stb.append("     AND L013_2.NAMECD2  = RD006.REMARK9 "); //併願合格コース
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entExamYear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
        if (!"ALL".equals(_param._shDiv)) {
            stb.append("     AND RD006.REMARK1 = '" + _param._shDiv + "' "); //専併区分
        }
        if (!"ALL".equals(_param._testDiv)) {
            stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' "); //試験区分
        }
        if (!"ALL".equals(_param._desireDiv)) {
            stb.append("     AND RD006.REMARK2 = '" + _param._desireDiv + "' "); //志望コース
        }
        if (null != _param._receptnoFrom && !"".equals(_param._receptnoFrom)) {
            stb.append("     AND RECEPT.RECEPTNO >= '" + _param._receptnoFrom + "' "); //受検番号FROM
        }
        if (null != _param._receptnoTo && !"".equals(_param._receptnoTo)) {
            stb.append("     AND RECEPT.RECEPTNO <= '" + _param._receptnoTo + "' "); //受検番号TO
        }
        if ("001".equals(_param._patternCd)) { // 入学金等
            if ("1".equals(_param._tokutaiSelect)) {
                stb.append("     AND B029.REMARK7 IS NOT NULL "); // 確定特待あり
            } else if ("2".equals(_param._tokutaiSelect)) {
                stb.append("     AND B029.REMARK7 IS NULL "); // 確定特待なし
            }
        }
        //合格者のみ
        stb.append("   AND (L013_1.NAMESPARE1 = '" + JUDGE_PASS + "' OR L013_2.NAMESPARE1 = '" + JUDGE_PASS + "') ");
        stb.append(" ORDER BY ");
        stb.append("     RECEPT.RECEPTNO ");
        return stb.toString();
    }

    private class Student {
        final String _receptno;
        final String _examno;
        final String _name;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _telno;

        public Student(
                final String receptno,
                final String examno,
                final String name,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String telno
        ) {
            _receptno = receptno;
            _examno = examno;
            _name = name;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telno = telno;
        }
    }

    private class TransferForms {
        final String _patternCd;
        final String _prefName;
        final String _accountNumber1;
        final String _accountNumber2;
        final String _accountNumber3;
        final String _member;
        final String _subject;
        final String _communication;
        final String _transferMoney;

        public TransferForms(
                final String patternCd,
                final String prefName,
                final String accountNumber1,
                final String accountNumber2,
                final String accountNumber3,
                final String member,
                final String subject,
                final String communication,
                final String transferMoney
        ) {
            _patternCd = patternCd;
            _prefName = prefName;
            _accountNumber1 = accountNumber1;
            _accountNumber2 = accountNumber2;
            _accountNumber3 = accountNumber3;
            _member = member;
            _subject = subject;
            _communication = communication;
            _transferMoney = transferMoney;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74455 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entExamYear;
        private final String _applicantDiv;
        private final String _schoolkind;
        private final String _testDiv;
        private final String _patternCd;
        private final String _tokutaiSelect;
        private final String _shDiv;
        private final String _desireDiv;
        private final String _receptnoFrom;
        private final String _receptnoTo;
        private final Map _transferFormsMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entExamYear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _schoolkind = ("1".equals(_applicantDiv)) ? SCHOOLKIND_J : SCHOOLKIND_H;
            _testDiv = request.getParameter("TESTDIV");
            _patternCd = request.getParameter("PATTERN_CD");
            _tokutaiSelect = request.getParameter("TOKUTAI_SELECT");
            _shDiv = request.getParameter("SHDIV");
            _desireDiv = request.getParameter("DESIREDIV");
            _receptnoFrom = request.getParameter("RECEPTNO_FROM");
            _receptnoTo = request.getParameter("RECEPTNO_TO");
            _transferFormsMap = getTransferFormsMap(db2);
        }

        private Map getTransferFormsMap(final DB2UDB db2) {
            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getTransferFormsSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String patternCd = rs.getString("PATTERN_CD");
                    final String prefName = rs.getString("PREF_NAME");
                    final String accountNumber1 = rs.getString("ACCOUNT_NUMBER1");
                    final String accountNumber2 = rs.getString("ACCOUNT_NUMBER2");
                    final String accountNumber3 = rs.getString("ACCOUNT_NUMBER3");
                    final String member = rs.getString("MEMBER");
                    final String subject = rs.getString("SUBJECT");
                    final String communication = rs.getString("COMMUNICATION");
                    final String transferMoney = rs.getString("TRANSFER_MONEY");

                    final TransferForms transferForms = new TransferForms(patternCd, prefName, accountNumber1, accountNumber2, accountNumber3, member, subject, communication, transferMoney);
                    retMap.put(patternCd, transferForms);
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String getTransferFormsSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     PATTERN_CD, ");
            stb.append("     PREF_NAME, ");
            stb.append("     ACCOUNT_NUMBER1, ");
            stb.append("     ACCOUNT_NUMBER2, ");
            stb.append("     ACCOUNT_NUMBER3, ");
            stb.append("     MEMBER, ");
            stb.append("     SUBJECT, ");
            stb.append("     COMMUNICATION, ");
            stb.append("     TRANSFER_MONEY ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_TRANSFER_FORMS_WK_MST ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _entExamYear + "' ");
            stb.append("     AND APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("     AND PATTERN_CD = '" + _patternCd + "' ");
            if ("001".equals(_patternCd)) { // 入学金等
                stb.append("     AND TOKUTAI_SELECT = '" + _tokutaiSelect + "' ");
            } else {
                stb.append("     AND TOKUTAI_SELECT = '2' ");
            }
            return stb.toString();
        }
    }
}

// eof
