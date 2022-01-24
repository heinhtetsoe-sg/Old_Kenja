// kanji=漢字
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id:$
 */
public class KNJL332A {

    private static final Log log = LogFactory.getLog("KNJL332A.class");

    private boolean _hasData;

    private Param _param;

    private static final String SIZE_KAKU2 = "1"; //1:角2

    private static final String SIZE_NAGA3 = "2"; //2:長3

    private static final String SIZE_TACKSEAL = "3"; //3:タックシール

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

            _hasData = printMain(db2, svf);
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

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final Map applicantMap = getApplicantMap(db2); //志願者Map
        if(applicantMap.size() == 0) {
            return false;
        }

        Map finsMap = new TreeMap();

        final String form;

        //1:封筒（角２）、2:封筒（長３）、3:タックシール
        if(SIZE_TACKSEAL.equals(_param._size)) { //3:タックシール
            form = "KNJL332A_3.frm";

            final int maxLine = 6; //最大印字行
            int line = 0; //印字行
            int col = 1; //印字列
            for (Iterator ite = applicantMap.keySet().iterator(); ite.hasNext();) {
                final String key = (String)ite.next();
                final Applicant applicant = (Applicant)applicantMap.get(key);

                if ("3".equals(_param._printType) && (applicant._finschoolCd == null || finsMap.containsKey(applicant._finschoolCd))) {
                    //出身学校宛てでは既に表示済みの出身学校はスキップ
                    continue;
                }
                if (applicant._finschoolCd != null) {
                    finsMap.put(applicant._finschoolCd, "1");
                }

                if(line == 0 || line > maxLine) {
                    if(line > maxLine) svf.VrEndPage();
                    svf.VrSetForm(form, 1);
                    line = 1;
                }

                if("1".equals(_param._printType) || "3".equals(_param._printType)) {
                    final String zipcd = "1".equals(_param._printType) ? applicant._zipcd : applicant._finschool_Zipcd;
                    final String addr1 = "1".equals(_param._printType) ? applicant._address1 : applicant._finschool_Addr1;
                    final String addr2 = "1".equals(_param._printType) ? applicant._address2 : applicant._finschool_Addr2;
                    svf.VrsOutn("ZIPCODE" + col, line, zipcd); //郵便番号
                    svf.VrsOutn("ADDRESS" + col + "_1_" + getFieldAddr(addr1), line, addr1); //住所1
                    svf.VrsOutn("ADDRESS" + col + "_2_" + getFieldAddr(addr2), line, addr2); //住所2
                }

                final String name;
                final String type;
                if("3".equals(_param._printType)) {
                    type =  "3".equals(applicant._finschooltype) ? "中学校" : "2".equals(applicant._finschooltype) ? "小学校" : "";
                    name = applicant._finschoolname != null ? applicant._finschoolname +  type + "長 殿" : "";
                } else if("4".equals(_param._printType)) {
                    name = applicant._name != null ? applicant._name+ " 保護者殿" : "";
                } else {
                    name = applicant._name != null ? applicant._name+ " 様" : "";
                }

                svf.VrsOutn("NAME" + col + "_" + getFieldName(name), line, name); //氏名、校長名、保護者
                if("3".equals(_param._printType)) {
                    svf.VrsOutn("EXAM_NO" + col, line, applicant._finschoolCd); //学校CD
                } else {
                    svf.VrsOutn("EXAM_NO" + col, line, "受験番号　" + applicant._receptno); //受験番号
                }

                col++;
                if(col > 2) {
                    col = 1;
                    line++;
                }
            }
            svf.VrEndPage();
        } else { //1:封筒（角２）、2:封筒（長３）
            if (SIZE_KAKU2.equals(_param._size)) { //1:封筒（角２）
                if ("1".equals(_param._printType)) { // 1:受験者住所あり
                    form = "KNJL332A_1_2.frm";
                }
                else { // 4:保護者宛
                    form = "KNJL332A_1_1.frm";
                }
            } else { //2:封筒（長３）
                if ("1".equals(_param._printType)) { // 1:受験者住所あり
                    form = "KNJL332A_2_6.frm";
                }
                else if ("2".equals(_param._printType)) { // 2:受験者住所なし
                    form = "KNJL332A_2_4.frm";
                }
                else if ("3".equals(_param._printType)) { // 3:出身学校宛
                    form = "on".equals(_param._blank) ? "KNJL332A_2_2.frm" : "KNJL332A_2_3.frm";
                }
                else { // 4:保護者宛
                    form = "KNJL332A_2_1.frm";
                }
            }

            for (Iterator ite = applicantMap.keySet().iterator(); ite.hasNext();) {
                final String key = (String)ite.next();
                final Applicant applicant = (Applicant)applicantMap.get(key);

                if ("3".equals(_param._printType) && (applicant._finschoolCd == null || finsMap.containsKey(applicant._finschoolCd))) {
                    //出身学校宛てでは既に表示済みの出身学校はスキップ
                    continue;
                }
                if (applicant._finschoolCd != null) {
                    finsMap.put(applicant._finschoolCd, "1");
                }

                svf.VrSetForm(form, 1);

                if("1".equals(_param._printType) || "3".equals(_param._printType)) {
                    String zipcd = "1".equals(_param._printType) ? applicant._zipcd : applicant._finschool_Zipcd;
                    zipcd = SIZE_KAKU2.equals(_param._size) ? zipcd : StringUtils.defaultString(zipcd).replace("-", "");
                    final String addr1 = "1".equals(_param._printType) ? applicant._address1 : applicant._finschool_Addr1;
                    final String addr2 = "1".equals(_param._printType) ? applicant._address2 : applicant._finschool_Addr2;
                    svf.VrsOut("ZIP_NO", zipcd); //郵便番号
                    svf.VrsOut("ADDR1_" + getFieldAddr(addr1), addr1); //住所1
                    svf.VrsOut("ADDR2_" + getFieldAddr(addr2), addr2); //住所2
                }

                final String name;
                final String type;
                if("3".equals(_param._printType)) {
                    type =  "3".equals(applicant._finschooltype) ? "中学校" : "2".equals(applicant._finschooltype) ? "小学校" : "";
                    name = applicant._finschoolname != null ? applicant._finschoolname +  type + "長 殿" : "";
                } else if("4".equals(_param._printType)) {
                    name = applicant._name != null ? applicant._name + " 保護者殿" : "";
                } else {
                    name = applicant._name != null ? applicant._name + " 様" : "";
                }
                svf.VrsOut("NAME" + getFieldName(name), name); //氏名、校長名、保護者
                if("3".equals(_param._printType)) {
                    svf.VrsOut("FINSCHOOL_CD", applicant._finschoolCd); //学校CD
                    final String comment = "1".equals(_param._comment) ? "入試関係書類在中" : "個人報告書受領書在中";
                    svf.VrsOut("CONTENT", comment); //在中文章
                } else {
                    svf.VrsOut("EXAM_NO", applicant._receptno); //受験番号
                }
                svf.VrEndPage();
            }
        }
        return true;
    }

    private String getFieldAddr(final String str) {
        final int keta = KNJ_EditEdit.getMS932ByteLength(str);

        if (SIZE_TACKSEAL.equals(_param._size)) {
            return keta <= 40 ? "1" : keta <= 50 ? "2" : "3";
        } else if (SIZE_KAKU2.equals(_param._size)) {
            if("1".equals(_param._printType)) {
                return keta <= 32 ? "1" : keta <= 40 ? "2" : keta <= 50 ? "3" : "4";
            } else {
                return keta <= 36 ? "1" : keta <= 50 ? "2" : "3";
            }
        } else {
            return keta <= 36 ? "1" : keta <= 50 ? "2" : "3";
        }
    }

    private String getFieldName(final String str) {
        final int keta = KNJ_EditEdit.getMS932ByteLength(str);

        if (SIZE_TACKSEAL.equals(_param._size)) {
            return keta <= 32 ? "1" : keta <= 38 ? "4" : keta <= 44 ? "5" : "6";
        } else if (SIZE_KAKU2.equals(_param._size)) {
            return keta <= 20 ? "1" : keta <= 30 ? "2" : keta <= 40 ? "3" : keta <= 50 ? "4" : "6";
        } else {
            return keta <= 30 ? "1" : keta <= 40 ? "2" : keta <= 50 ? "3" : "4";
        }
    }

    private Map getApplicantMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String schoolKind = "2".equals(_param._applicantDiv) ? "H" : "J";
        final String course = "1".equals(_param._shDiv) ? "REMARK8" : "REMARK9";

        try{
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("  RECEPT.TESTDIV, ");
            stb.append("  BASE.EXAMNO, ");
            stb.append("  RECEPT.RECEPTNO, ");
            stb.append("  BASE.NAME, ");
            stb.append("  ADDR.GNAME, ");
            stb.append("  ADDR.ZIPCD, ");
            stb.append("  ADDR.ADDRESS1, ");
            stb.append("  ADDR.ADDRESS2, ");
            stb.append("  BASE.FS_CD, ");
            stb.append("  SCHOOL.FINSCHOOL_TYPE, ");
            stb.append("  SCHOOL.FINSCHOOL_NAME, ");
            stb.append("  SCHOOL.FINSCHOOL_ZIPCD, ");
            stb.append("  SCHOOL.FINSCHOOL_ADDR1, ");
            stb.append("  SCHOOL.FINSCHOOL_ADDR2, ");
            stb.append("  RD006.REMARK1, ");
            stb.append("  RD006.REMARK8, ");
            stb.append("  RD006.REMARK9, ");
            stb.append("  BD030.REMARK3 ");
            stb.append(" FROM ");
            stb.append("  ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append(" LEFT JOIN ");
            stb.append("  ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV AND ADDR.EXAMNO = BASE.EXAMNO ");
            stb.append(" INNER JOIN ");
            stb.append("  ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV AND RECEPT.EXAMNO = BASE.EXAMNO ");
            stb.append(" LEFT JOIN ");
            stb.append("  FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ");
            stb.append(" LEFT JOIN ");
            stb.append("  ENTEXAM_RECEPT_DETAIL_DAT RD006 ON RD006.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND RD006.APPLICANTDIV = RECEPT.APPLICANTDIV AND RD006.TESTDIV = RECEPT.TESTDIV AND RD006.RECEPTNO = RECEPT.RECEPTNO AND RD006.SEQ = '006' ");
            stb.append(" LEFT JOIN ");
            stb.append("  V_NAME_MST L_58 ON L_58.YEAR = RD006.ENTEXAMYEAR AND L_58.NAMECD1 = 'L" + schoolKind + "58' AND L_58.NAMECD2 = RD006.REMARK2 ");
            stb.append(" LEFT JOIN ");
            stb.append("  V_NAME_MST L_13_1 ON L_13_1.YEAR = RD006.ENTEXAMYEAR AND L_13_1.NAMECD1 = 'L" + schoolKind + "13' AND L_13_1.NAMECD2 = RD006.REMARK8 ");
            stb.append(" LEFT JOIN ");
            stb.append("  V_NAME_MST L_13_2 ON L_13_2.YEAR = RD006.ENTEXAMYEAR AND L_13_2.NAMECD1 = 'L" + schoolKind + "13' AND L_13_2.NAMECD2 = RD006.REMARK9 ");
            stb.append(" LEFT JOIN ");
            stb.append("  ENTEXAM_APPLICANTBASE_DETAIL_DAT BD030 ON BD030.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BD030.APPLICANTDIV = BASE.APPLICANTDIV AND BD030.EXAMNO = BASE.EXAMNO AND BD030.SEQ = '030' ");
            stb.append(" WHERE ");
            stb.append("  BASE.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
            stb.append("  BASE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("  RECEPT.RECEPTNO IS NOT NULL ");
            //試験区分絞り込み
            if(!"ALL".equals(_param._testDiv)) {
                stb.append("  AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
            }
            //専併区分絞り込み
            if(!"ALL".equals(_param._shDiv)) {
                stb.append("  AND RD006.REMARK1 = '" + _param._shDiv + "' ");
            }
            //出願コース絞り込み
            if(!"ALL".equals(_param._desireDiv)) {
                stb.append("  AND RD006.REMARK2 = '" + _param._desireDiv + "' ");
            }
            //合否絞り込み
            if("1".equals(_param._passCourse)) { //合格の場合
                if("ALL".equals(_param._shDiv)) {
                    stb.append("  AND (L_13_1.NAMESPARE1 = '1' OR L_13_2.NAMESPARE1 = '1') ");
                } else if ("1".equals(_param._shDiv)) {
                    stb.append("  AND L_13_1.NAMESPARE1 = '1' ");
                } else {
                    stb.append("  AND L_13_2.NAMESPARE1 = '1' ");
                }
            } else if("2".equals(_param._passCourse)) { //不合格の場合
                if("ALL".equals(_param._shDiv)) {
                    stb.append("  AND (RD006.REMARK8 = '0' OR RD006.REMARK9 = '0') ");
                } else {
                    stb.append("  AND RD006." + course + " = '0' ");
                }
            }
            //入学コース絞り込み
            if(!"ALL".equals(_param._entCourse)) {
                stb.append("  AND CASE ");
                stb.append("    WHEN BD030.REMARK3 = '1' THEN RD006.REMARK8 ");
                stb.append("    WHEN BD030.REMARK3 = '2' THEN RD006.REMARK9 ");
                stb.append("  END = '" + _param._entCourse + "' ");
            }
            //受験番号絞り込み
            if(_param._receptNoFrom.length() > 0 && _param._receptNoTo.length() > 0) {
                stb.append("  AND RECEPT.RECEPTNO BETWEEN '" + _param._receptNoFrom + "' AND '" + _param._receptNoTo + "' ");
            } else if(_param._receptNoFrom.length() > 0) {
                stb.append("  AND RECEPT.RECEPTNO >= '" + _param._receptNoFrom + "' ");
            } else if(_param._receptNoTo.length() > 0) {
                stb.append("  AND RECEPT.RECEPTNO <= '" + _param._receptNoTo + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("  RECEPT.TESTDIV,RECEPT.RECEPTNO ");

            log.debug(" applicant sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testdiv = rs.getString("TESTDIV");
                final String examno = rs.getString("EXAMNO");
                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String gname = rs.getString("GNAME");
                final String zipcd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String fs_Cd = rs.getString("FS_CD");
                final String finschooltype = rs.getString("FINSCHOOL_TYPE");
                final String finschoolname = rs.getString("FINSCHOOL_NAME");
                final String finschool_Zipcd = rs.getString("FINSCHOOL_ZIPCD");
                final String finschool_Addr1 = rs.getString("FINSCHOOL_ADDR1");
                final String finschool_Addr2 = rs.getString("FINSCHOOL_ADDR2");
                final String remark1 = rs.getString("REMARK1");
                final String remark8 = rs.getString("REMARK8");
                final String remark9 = rs.getString("REMARK9");
                final String remark3 = rs.getString("REMARK3");

                final String key = testdiv + receptno;
                if(!retMap.containsKey(key)) {
                    final Applicant applicant = new Applicant(testdiv, examno, receptno, name, gname, zipcd, address1, address2, fs_Cd, finschooltype, finschoolname, finschool_Zipcd, finschool_Addr1, finschool_Addr2, remark1, remark8, remark9, remark3);
                    retMap.put(key, applicant);
                }
            }

        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;

    }

    private class Applicant {
        final String _testdiv;
        final String _examno;
        final String _receptno; //受験番号
        final String _name; //氏名
        final String _gname; //保護者
        final String _zipcd; //郵便
        final String _address1; //住所1
        final String _address2; //住所2
        final String _finschoolCd; //学校CD
        final String _finschooltype; //学校種
        final String _finschoolname; //学校名
        final String _finschool_Zipcd; //学校郵便
        final String _finschool_Addr1; //学校住所1
        final String _finschool_Addr2; //学校住所2
        final String _remark1; //専併区分
        final String _remark8; //専願合格コース
        final String _remark9; //併願合格コース
        final String _remark3; //入学コース

        public Applicant(final String testdiv, final String examno, final String receptno, final String name, final String gname, final String zipcd,
                final String address1, final String address2, final String finschoolCd, final String finschooltype, final String finschoolname,
                final String finschool_Zipcd, final String finschool_Addr1, final String finschool_Addr2,
                final String remark1, final String remark8, final String remark9, final String remark3) {
            _testdiv = testdiv;
            _examno = examno;
            _receptno = receptno;
            _name = name;
            _gname = gname;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
            _finschoolCd = finschoolCd;
            _finschooltype = finschooltype;
            _finschoolname = finschoolname;
            _finschool_Zipcd = finschool_Zipcd;
            _finschool_Addr1 = finschool_Addr1;
            _finschool_Addr2 = finschool_Addr2;
            _remark1 = remark1;
            _remark8 = remark8;
            _remark9 = remark9;
            _remark3 = remark3;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Id:$");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _examYear;
        final String _applicantDiv; //入試制度
        final String _testDiv; //入試区分
        final String _date;
        final String _printType; //帳票区分
        final String _size; //サイズ区分
        final String _shDiv; //専併区分
        final String _desireDiv; //出願コース
        final String _passCourse; //合否
        final String _entCourse; //入学コース
        final String _receptNoFrom; //受験番号From
        final String _receptNoTo; //受験番号To
        final String _blank; //在中あり
        final String _comment; //在中文章

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _examYear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _date = request.getParameter("LOGIN_DATE");
            _printType = request.getParameter("PRINT_TYPE");
            _size = request.getParameter("SIZE");
            _shDiv = request.getParameter("SHDIV");
            _desireDiv = request.getParameter("DESIREDIV");
            _passCourse = request.getParameter("PASS_COURSE");
            _entCourse = request.getParameter("ENT_COURSE");
            _receptNoFrom = request.getParameter("RECEPTNO_FROM");
            _receptNoTo = request.getParameter("RECEPTNO_TO");
            _blank = request.getParameter("BLANK");
            _comment = request.getParameter("COMMENT");
        }
    }
}

// eof
