/*
 * $Id: cca0ff6e1ad42c226c3c09916a642741f8ad3f0f $
 *
 * 作成日: 2017/10/30
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class KNJL301D {

    private static final Log log = LogFactory.getLog(KNJL301D.class);

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
        svf.VrSetForm("KNJL301D.frm", 1);
        final List printList = getList(db2);
        final int maxLine = 50;
        int printLine = 1;

        setTitle(db2, svf);//ヘッダ
        printLine = 1;
        String addrStr = "";
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (printLine > maxLine) {
                svf.VrEndPage();
                setTitle(db2, svf);//ヘッダ
                printLine = 1;
            }

            //データ
            //受験番号
            svf.VrsOutn("EXAM_NO" , printLine, printData._examNo);
            //氏名
            if (20 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                svf.VrsOutn("NAME1" , printLine, printData._name);
            }else if (30 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                svf.VrsOutn("NAME2" , printLine, printData._name);
            }else {
                svf.VrsOutn("NAME3" , printLine, printData._name);
            }
            //氏名かな
            if (20 >= KNJ_EditEdit.getMS932ByteLength(printData._nameKana)) {
                svf.VrsOutn("KANA1" , printLine, printData._nameKana);
            }else if (30 >= KNJ_EditEdit.getMS932ByteLength(printData._nameKana)) {
                svf.VrsOutn("KANA2" , printLine, printData._nameKana);
            }else {
                svf.VrsOutn("KANA3" , printLine, printData._nameKana);
            }
            //性別
            svf.VrsOutn("SEX" , printLine, printData._sex);
            //生年月日
            String prtbirth = "";
            if (null != printData._birth_y && null != printData._birth_m && null !=  printData._birth_d){
            	prtbirth = printData._eracd + printData._birth_y + "年" + printData._birth_m + "月" + printData._birth_d + "日";
            }
            svf.VrsOutn("BIRTHDAY" , printLine, prtbirth);
            //志望累計
            svf.VrsOutn("DESIRE_DIV" , printLine, printData._desirediv);
            //出身学校
            svf.VrsOutn("FINSCHOOL_NAME" , printLine, printData._finschool_name);
            //卒業年度
            svf.VrsOutn("GRD_YEAR" , printLine, printData._fs_grdyear);
            //浪人
            svf.VrsOutn("PAST_STUDENT" , printLine, printData._past_student);
            //保護者名
            if (20 >= KNJ_EditEdit.getMS932ByteLength(printData._gname)) {
                svf.VrsOutn("GRD_NAME1" , printLine, printData._gname);
            }else if (30 >= KNJ_EditEdit.getMS932ByteLength(printData._gname)) {
                svf.VrsOutn("GRD_NAME2" , printLine, printData._gname);
            }else {
                svf.VrsOutn("GRD_NAME3" , printLine, printData._gname);
            }
            //保護者名かな
            if (20 >= KNJ_EditEdit.getMS932ByteLength(printData._gkana)) {
                svf.VrsOutn("GRD_KANA1" , printLine, printData._gkana);
            }else if (30 >= KNJ_EditEdit.getMS932ByteLength(printData._gkana)) {
                svf.VrsOutn("GRD_KANA2" , printLine, printData._gkana);
            }else {
                svf.VrsOutn("GRD_KANA3" , printLine, printData._gkana);
            }
            //続柄
            svf.VrsOutn("RELATION" , printLine, printData._relationship);
            //郵便番号
            svf.VrsOutn("ZIP_NO" , printLine, printData._gzipcd);
            //住所
            if (null != printData._gaddress1) {
                addrStr = printData._gaddress1;
            } else {
            	addrStr = "";
            }
            if (null != printData._gaddress2) {
            	addrStr = addrStr + printData._gaddress2;
            }
            if (40 >= KNJ_EditEdit.getMS932ByteLength(addrStr)) {
                svf.VrsOutn("ADDR1", printLine, addrStr);
            }else if (60 >= KNJ_EditEdit.getMS932ByteLength(addrStr)) {
                svf.VrsOutn("ADDR2", printLine, addrStr);
            }else if (80 >= KNJ_EditEdit.getMS932ByteLength(addrStr)) {
                svf.VrsOutn("ADDR3", printLine, addrStr);
            }else {
                svf.VrsOutn("ADDR4", printLine, addrStr);
            }
            //電話番号
            svf.VrsOutn("TEL_NO" , printLine, printData._gtelno);

            printLine++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        String setYear = KNJ_EditDate.h_format_JP_N(db2, _param._entExamYear + "/04/01");
        final String printDateTime = KNJ_EditDate.h_format_thi(_param._loginDate, 0);// + "　" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        svf.VrsOut("DATE", printDateTime);
        svf.VrsOut("TITLE", setYear + "度　" + "　" + _param._testdivName + "　受験者確認名簿");
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
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String eracd = rs.getString("ERACD");
                final String birth_y = rs.getString("BIRTH_Y");
                final String birth_m = rs.getString("BIRTH_M");
                final String birth_d = rs.getString("BIRTH_D");
                final String desirediv = rs.getString("DESIREDIV");
                final String fs_cd = rs.getString("FS_CD");
                final String finschool_name = rs.getString("FINSCHOOL_NAME_ABBV");
                final String fs_grdyear = rs.getString("FS_GRDYEAR");
                final String past_student = rs.getString("PAST_STUDENT");
                final String gname = rs.getString("GNAME");
                final String gkana = rs.getString("GKANA");
                final String relationship = rs.getString("REL_NAME");
                final String gzipcd = rs.getString("GZIPCD");
                final String gaddress1 = rs.getString("GADDRESS1");
                final String gaddress2 = rs.getString("GADDRESS2");
                final String gtelno = rs.getString("GTELNO");

                final PrintData printData = new PrintData(examNo, name, nameKana, sex, eracd, birth_y, birth_m, birth_d, desirediv, fs_cd, finschool_name, fs_grdyear, past_student, gname, gkana, relationship, gzipcd, gaddress1, gaddress2, gtelno);
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
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     NM_SEX.NAME1 AS SEX, ");
        stb.append("     NM_ERACD.NAME1 AS ERACD, ");
        stb.append("     T1.BIRTH_Y , ");
        stb.append("     T1.BIRTH_M , ");
        stb.append("     T1.BIRTH_D , ");
        stb.append("     T1.DESIREDIV, ");
        stb.append("     T1.FS_CD, ");
        stb.append("     FSM.FINSCHOOL_NAME_ABBV, ");
        stb.append("     T1.FS_GRDYEAR, ");
        stb.append("     CASE WHEN (INTEGER('" + _param._loginYear + "')+1 - INTEGER(T1.FS_GRDYEAR) > 0) THEN REPLACE(CHAR(INTEGER('" + _param._loginYear + "')+1 - INTEGER(T1.FS_GRDYEAR)), ' ', '') || '浪' ELSE '' END AS PAST_STUDENT, ");
        stb.append("     AD1.GNAME, ");
        stb.append("     AD1.GKANA, ");
        stb.append("     AD1.RELATIONSHIP, ");
        stb.append("     NM_REL.NAME1 AS REL_NAME, ");
        stb.append("     AD1.GZIPCD, ");
        stb.append("     AD1.GADDRESS1, ");
        stb.append("     AD1.GADDRESS2, ");
        stb.append("     AD1.GTELNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT AD1 ON T1.EXAMNO = AD1.EXAMNO and ");
        stb.append("       T1.ENTEXAMYEAR = AD1.ENTEXAMYEAR and ");
        stb.append("       T1.APPLICANTDIV = AD1.APPLICANTDIV");
        stb.append("    LEFT JOIN NAME_MST NM_ERACD ON NM_ERACD.NAMECD1 = 'L007' AND NM_ERACD.NAMECD2 = T1.ERACD");
        stb.append("    LEFT JOIN NAME_MST NM_SEX ON NM_SEX.NAMECD1 = 'Z002' AND NM_SEX.NAMECD2 = T1.SEX");
        stb.append("    LEFT JOIN FINSCHOOL_MST FSM ON T1.FS_CD = FSM.FINSCHOOLCD");
        stb.append("    LEFT JOIN NAME_MST NM_REL ON NM_REL.NAMECD1 = 'H201' AND NM_REL.NAMECD2 = AD1.RELATIONSHIP ");
        stb.append(" WHERE ");
        stb.append("         T1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY T1.EXAMNO ASC");

        return stb.toString();
    }

    private class PrintData {
        final String _examNo;
        final String _name;
        final String _nameKana;
        final String _sex;
        final String _eracd;
        final String _birth_y;
        final String _birth_m;
        final String _birth_d;
        final String _desirediv;
        final String _fs_cd;
        final String _finschool_name;
        final String _fs_grdyear;
        final String _past_student;
        final String _gname;
        final String _gkana;
        final String _relationship;
        final String _gzipcd;
        final String _gaddress1;
        final String _gaddress2;
        final String _gtelno;


        public PrintData(
                final String examNo,
                final String name,
                final String nameKana,
                final String sex,
                final String eracd,
                final String birth_y,
                final String birth_m,
                final String birth_d,
                final String desirediv,
                final String fs_cd,
                final String finschool_name,
                final String fs_grdyear,
                final String past_student,
                final String gname,
                final String gkana,
                final String relationship,
                final String gzipcd,
                final String gaddress1,
                final String gaddress2,
                final String gtelno
        ) {
            _examNo = examNo;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _eracd = eracd;
            _birth_y = birth_y;
            _birth_m = birth_m;
            _birth_d = birth_d;
            _desirediv = desirediv;
            _fs_cd = fs_cd;
            _finschool_name = finschool_name;
            _fs_grdyear = fs_grdyear;
            _past_student = past_student;
            _gname = gname;
            _gkana = gkana;
            _relationship = relationship;
            _gzipcd = gzipcd;
            _gaddress1 = gaddress1;
            _gaddress2 = gaddress2;
            _gtelno = gtelno;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71866 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginDate;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _entExamYear;
        private final String _testdivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginDate     = request.getParameter("LOGIN_DATE");
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _entExamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _testdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", _testDiv));
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
