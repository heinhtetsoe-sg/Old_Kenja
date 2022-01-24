/*
 * $Id$
 *
 * 作成日: 2018/08/06
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJL331A {

    private static final Log log = LogFactory.getLog(KNJL331A.class);

    private final String JUDGE_PASS = "1";
    private final String JUDGE_UNPASS = "0";

    private final String SCHOOLKIND_J = "J";
    private final String SCHOOLKIND_H = "H";

    private boolean _hasData;

    private Param _param;

    private String bithdayField;

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
        svf.VrSetForm("KNJL331A.frm", 1);
        final List printList = getList(db2);

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();

            setTitle(db2, svf);//ヘッダ

            //受験番号
            svf.VrsOut("EXAM_NO", printData._receptNo);

            //元号(記入項目用)
            String[] dwk;
            if (_param._loginDate.indexOf('/') >= 0) {
                dwk = StringUtils.split(_param._loginDate, '/');
            } else if (_param._loginDate.indexOf('-') >= 0) {
                dwk = StringUtils.split(_param._loginDate, '-');
            } else {
                //ありえないので、固定値で設定。
                dwk = new String[1];
            }
            if (dwk.length >= 3) {
                final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(dwk[0]), Integer.parseInt(dwk[1]), Integer.parseInt(dwk[2]));
                svf.VrsOut("ERA_NAME", gengou);
            }

            //生徒氏名
            int namelen = KNJ_EditEdit.getMS932ByteLength(StringUtils.defaultString(printData._name, ""));
            String namefield = namelen > 30 ? "NAME2" : "NAME1";
            svf.VrsOut(namefield, StringUtils.defaultString(printData._name, ""));

            //性別
            svf.VrsOut("SEX", "(" + StringUtils.defaultString(printData._sex, "") + ")");

            //ふりがな
            int kanalen = KNJ_EditEdit.getMS932ByteLength(StringUtils.defaultString(printData._nameKana, ""));
            String kanafield = kanalen > 60 ? "KANA4" : kanalen > 50 ? "KANA3" : kanalen > 44 ? "KANA2" : "KANA1";
            svf.VrsOut(kanafield, StringUtils.defaultString(printData._nameKana, ""));

            //生年月日
            if (printData._birthDay != null && !"".equals(printData._birthDay)) {
                svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(db2, printData._birthDay));
            }

            //郵便番号
            svf.VrsOut("ZIP_NO", StringUtils.defaultString(printData._zipCd, ""));
            //生徒住所
            final String addr2 = StringUtils.defaultString(printData._addr2, "");
            final String addrData = StringUtils.defaultString(printData._addr1, "") + ("".equals(addr2) ? "" : "　" + addr2);
            final int addrLen = KNJ_EditEdit.getMS932ByteLength(addrData);
            String addrfield = addrLen > 100 ? "ADDR6" : addrLen > 80 ? "ADDR5" : addrLen > 60 ? "ADDR4" : addrLen > 50 ? "ADDR3" : addrLen > 44 ? "ADDR2" : "ADDR1";
            svf.VrsOut(addrfield, addrData);
            //電話番号
            svf.VrsOut("TEL_NO", StringUtils.defaultString(printData._telNo, ""));
            //保護者氏名
            svf.VrsOut("GUARD_NAME1", StringUtils.defaultString(printData._gName, ""));
            //ふりがな
            int gkanalen = KNJ_EditEdit.getMS932ByteLength(StringUtils.defaultString(printData._gKana, ""));
            String gkanafield = gkanalen > 60 ? "GUARD_KANA4" : gkanalen > 50 ? "GUARD_KANA3" : gkanalen > 44 ? "GUARD_KANA2" : "GUARD_KANA1";
            svf.VrsOut(gkanafield, StringUtils.defaultString(printData._gKana, ""));

            svf.VrEndPage();
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrsOut("SCHOOL_NAME", "羽衣学園　中・高等学校長");
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

// dbg int testexno = 1001;
// dbg int maxdbgcnt = 1;
// dbg int cnt = 0;
// dbg while (cnt < maxdbgcnt) {
// dbg  //old     final PrintData printData = new PrintData("テスト", String.valueOf((testexno + cnt), String.valueOf((testexno + cnt), "ｔｓｔ", "お", "こーす1", "だめ中学", "1"));
//            final PrintData printData = new PrintData("1001", "10001", "名前かな", "男", "なまえかな", "2006-01-01", "900-0001", "アドレス1", "アドレス2", "090-123-4567", "保護者かな", "ほごしゃかな");
//     retList.add(printData);
// dbg     cnt++;
            while (rs.next()) {
                final String receptno = rs.getString("RECEPTNO");
                final String examno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String namekana = rs.getString("NAME_KANA");
                final String birthday = rs.getString("BIRTHDAY");
                final String zipcd = rs.getString("zipcd");
                final String addr1 = rs.getString("ADDRESS1");
                final String addr2 = rs.getString("ADDRESS2");
                final String telno = rs.getString("TELNO");
                final String gname = rs.getString("GNAME");
                final String gkana = rs.getString("GKANA");

                final PrintData printData = new PrintData(receptno, examno, name, sex, namekana, birthday, zipcd, addr1, addr2, telno, gname, gkana);
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

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("   T0.RECEPTNO, ");
        stb.append("   BASE.EXAMNO, ");
        stb.append("   BASE.NAME, ");
        stb.append("   N1.ABBV1 AS SEX, ");
        stb.append("   BASE.NAME_KANA, ");
        stb.append("   BASE.BIRTHDAY, ");
        stb.append("   ADDR.ZIPCD, ");
        stb.append("   ADDR.ADDRESS1, ");
        stb.append("   ADDR.ADDRESS2, ");
        stb.append("   ADDR.TELNO, ");
        stb.append("   ADDR.GNAME, ");
        stb.append("   ADDR.GKANA ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_RECEPT_DAT T0 ");
        stb.append("   INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RD006 ");
        stb.append("      ON T0.ENTEXAMYEAR 	= RD006.ENTEXAMYEAR ");
        stb.append("     AND T0.APPLICANTDIV 	= RD006.APPLICANTDIV ");
        stb.append("     AND T0.EXAM_TYPE 		= RD006.EXAM_TYPE ");
        stb.append("     AND T0.RECEPTNO 		= RD006.RECEPTNO ");
        stb.append("     AND RD006.SEQ 			= '006' ");
        if (!"ALL".equals(_param._shDiv)) {
            stb.append("   AND RD006.REMARK1 = '" + _param._shDiv + "' ");
        }
        if (!"ALL".equals(_param._passCourse)) {
            if ("1".equals(_param._shDiv)) {    //専願合格コース
                stb.append("   AND RD006.REMARK8 = '" + _param._passCourse + "' ");
            } else if("2".equals(_param._shDiv)) {                          //併願合格コース
                stb.append("   AND RD006.REMARK9 = '" + _param._passCourse + "' ");
            } else if("ALL".equals(_param._shDiv)) {                        //両方
                stb.append("   AND '" + _param._passCourse + "' IN (RD006.REMARK8, RD006.REMARK9) ");
            }
        }
        stb.append("   LEFT JOIN V_NAME_MST L013_1 ");
        stb.append("      ON L013_1.YEAR     = RD006.ENTEXAMYEAR ");
        stb.append("     AND L013_1.NAMECD1  = 'L" + _param._schoolkind + "13' ");
        stb.append("     AND L013_1.NAMECD2  = RD006.REMARK8 "); //専願合格コース
        stb.append("   LEFT JOIN V_NAME_MST L013_2 ");
        stb.append("      ON L013_2.YEAR     = RD006.ENTEXAMYEAR ");
        stb.append("     AND L013_2.NAMECD1  = 'L" + _param._schoolkind + "13' ");
        stb.append("     AND L013_2.NAMECD2  = RD006.REMARK9 "); //併願合格コース
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("      ON BASE.ENTEXAMYEAR 	= T0.ENTEXAMYEAR ");
        stb.append("     AND BASE.APPLICANTDIV 	= T0.APPLICANTDIV ");
        stb.append("     AND BASE.EXAMNO 		= T0.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD007 ");
        stb.append("      ON BD007.ENTEXAMYEAR 	= BASE.ENTEXAMYEAR ");
        stb.append("     AND BD007.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND BD007.EXAMNO 		= BASE.EXAMNO ");
        stb.append("     AND BD007.SEQ	 		= '007' ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ");
        stb.append("      ON ADDR.ENTEXAMYEAR  	= BASE.ENTEXAMYEAR ");
        stb.append("     AND ADDR.APPLICANTDIV 	= BASE.APPLICANTDIV ");
        stb.append("     AND ADDR.EXAMNO       	= BASE.EXAMNO ");
        stb.append("   LEFT JOIN NAME_MST N1 ");
        stb.append("      ON N1.NAMECD1 = 'Z002' ");
        stb.append("     AND N1.NAMECD2 = BASE.SEX ");
        stb.append(" WHERE ");
        stb.append("   T0.ENTEXAMYEAR = '" + _param._entExamYear + "' ");
        stb.append("   AND T0.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!"ALL".equals(_param._testDiv)) {
            stb.append("   AND T0.TESTDIV = '" + _param._testDiv + "' ");
        }
        if (!"ALL".equals(_param._enterCourse)) {
            stb.append("   AND BASE.ENTDIV = '" + _param._enterCourse + "' ");
        }
        stb.append("   AND T0.EXAM_TYPE = '1' "); //固定
        //合格者のみ
        stb.append("   AND (L013_1.NAMESPARE1 = '" + JUDGE_PASS + "' OR L013_2.NAMESPARE1 = '" + JUDGE_PASS + "') ");
        stb.append(" ORDER BY ");
        stb.append("   T0.RECEPTNO  ");
        return stb.toString();
    }

    private class PrintData {
        final String _receptNo;
        final String _examNo;
        final String _name;
        final String _sex;
        final String _nameKana;
        final String _birthDay;
        final String _zipCd;
        final String _addr1;
        final String _addr2;
        final String _telNo;
        final String _gName;
        final String _gKana;

        public PrintData(
                final String receptNo,
                final String examNo,
                final String name,
                final String sex,
                final String nameKana,
                final String birthDay,
                final String zipCd,
                final String addr1,
                final String addr2,
                final String telNo,
                final String gName,
                final String gKana
        ) {
            _receptNo = receptNo;
            _examNo = examNo;
            _name = name;
            _sex = sex;
            _nameKana = nameKana;
            _birthDay = birthDay;
            _zipCd = zipCd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telNo = telNo;
            _gName = gName;
            _gKana = gKana;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 73735 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;
        private final String _entExamYear;
        private final String _applicantDiv;
        private final String _schoolkind;
        private final String _appDivName;
        private final String _testDiv;
        private final String _shDiv;
        private final String _enterCourse;
        private final String _passCourse;

        private Map _schoolKindNameList  = Collections.EMPTY_MAP;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear      = request.getParameter("LOGIN_YEAR");
            _loginSemester  = request.getParameter("LOGIN_SEMESTER");
            _loginDate      = request.getParameter("LOGIN_DATE");
            _entExamYear    = request.getParameter("ENTEXAMYEAR");
            _applicantDiv   = request.getParameter("APPLICANTDIV");
            _schoolkind = ("1".equals(_applicantDiv)) ? SCHOOLKIND_J : SCHOOLKIND_H;
            _testDiv        = request.getParameter("TESTDIV");
            _shDiv     		= request.getParameter("SHDIV");
            _enterCourse    = request.getParameter("ENTER_COURSE");
            _passCourse     = request.getParameter("PASS_COURSE");
            _appDivName     = StringUtils.defaultString(getNameMst(db2, "NAME1", "L003", _applicantDiv));

            _schoolKindNameList = setSchoolKindNameMap(db2);
        }

        private Map setSchoolKindNameMap(final DB2UDB db2) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' "), "NAMECD2", "NAME1");
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof
