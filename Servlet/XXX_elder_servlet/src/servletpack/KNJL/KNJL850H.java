// kanji=漢字
package servletpack.KNJL;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: eb18a3b43cc049dd71c01377cde359a0619b8b63 $
 */
public class KNJL850H {

    private static final Log log = LogFactory.getLog("KNJL850H.class");

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
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            _param = createParam(db2, request);

            // csv以外
            if(!_param._iscsv) {
                response.setContentType("application/pdf");
                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());
            }

            _hasData = false;

            // 1:合格者一覧表
            if("1".equals(_param._outputDiv)) {
                print1GoukakuCatalog(db2, svf);
            }
            // 2:合格者一覧表(掲示用) CSV
            else if("2".equals(_param._outputDiv)) {
                final List<List<String>> outputLines = new ArrayList<List<String>>();
                final Map csvParam = new HashMap();
                csvParam.put("HttpServletRequest", request);
                setOutputCsvLines(db2, _param, outputLines);
                CsvUtils.outputLines(log, response, _param._examYear + "年度合格者一覧表.csv", outputLines, csvParam);
            }
            // 3:合格証
            else if("3".equals(_param._outputDiv)) {
                print3GoukakuProof(db2, svf);
            }
            // 4:入学許可証
            else if("4".equals(_param._outputDiv)) {
                print4Permit(db2, svf);
            }
            // 5:繰上候補者一覧表
            else if("5".equals(_param._outputDiv)) {
                print5AdvanceCatalog(db2, svf);
            }
            // 6:繰上候補者通知書
            else if("6".equals(_param._outputDiv)) {
                print6AdvanceNotice(db2, svf);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if (_param._iscsv) {
            } else {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
                svf.VrQuit();
            }
        }
    }

    // 2:合格者一覧表(掲示用)CSV
    private void setOutputCsvLines(final DB2UDB db2, final Param param, final List<List<String>> outputList) throws SQLException {

        final Map printCsvMap = getPrint1Map(db2); //志願者Map

        // 印刷処理
        final List<List<String>> headerLineList = new ArrayList<List<String>>();
        String examnos = "";
        for(Iterator ite = printCsvMap.keySet().iterator(); ite.hasNext();) {
            final String key = (String) ite.next();
            final GoukakuCatalog gc = (GoukakuCatalog) printCsvMap.get(key);
            if(!"".equals(examnos)) examnos += ",";
            examnos += gc._examno;
        }
        final List<String> header1Line = newLine(headerLineList);
        header1Line.addAll(Arrays.asList(examnos));
        outputList.addAll(headerLineList);
    }

    private List<String> newLine(final List<List<String>> listList) {
        final List<String> line = line();
        listList.add(line);
        return line;
    }

    private List<String> line() {
        return line(0);
    }

    private List<String> line(final int size) {
        final List<String> line = new ArrayList<String>();
        for (int i = 0; i < size; i++) {
            line.add(null);
        }
        return line;
    }



    // 1:合格者一覧表
    private void print1GoukakuCatalog(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        final Map print1Map = getPrint1Map(db2); //志願者Map

        if(print1Map.isEmpty()) { //対象なし
            return;
        }

        log.debug(print1Map.size());
        final int maxLine = 50; //最大印字行
        int page = 0; // ページ
        int line = 1; //印字行

        for (Iterator ite = print1Map.keySet().iterator(); ite.hasNext();) {
            final String Key = (String)ite.next();
            final GoukakuCatalog gc = (GoukakuCatalog)print1Map.get(Key);

            if (line > maxLine || page == 0) {
                if(line > maxLine) svf.VrEndPage();
                page++;
                line = 1;
                svf.VrSetForm("KNJL850H_1.frm", 1);
                final String date = _param._date != null ? _param._date.replace("-", ".") : "";
                svf.VrsOut("DATE", date); //日付
                svf.VrsOut("PAGE", String.valueOf(page) + "頁"); //ページ
                svf.VrsOut("TITLE", _param._examYear + "年度　" + "□□□合格者一覧表□□□"); //タイトル
                svf.VrsOut("EXAM_DIV", _param._testName); //入試区分
            }

            svf.VrsOutn("RANK", line, gc._total_Rank4); //順位
            svf.VrsOutn("EXAM_NO1", line, gc._examno); //受験番号

            final String fieldName = getFieldName(gc._name, "1");
            svf.VrsOutn("NAME1_" + fieldName, line, gc._name); //氏名

            final String addr = StringUtils.defaultString(gc._address1) + StringUtils.defaultString(gc._address2);
            final String fieldAddr = getFieldName(addr, "2");
            svf.VrsOutn("ADDR" + fieldAddr, line, addr); //住所

            svf.VrsOutn("REMARK", line, gc._remark1); //備考

            line++;
        }
        svf.VrEndPage();
        _hasData = true;
    }

    // 1:合格者一覧表 SQL取得
    private Map getPrint1Map(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("  RECEPT.ENTEXAMYEAR, ");
            stb.append("  RECEPT.APPLICANTDIV, ");
            stb.append("  RECEPT.TESTDIV, ");
            stb.append("  RECEPT.EXAM_TYPE, ");
            stb.append("  RECEPT.EXAMNO, ");
            stb.append("  RECEPT.TOTAL_RANK4, ");
            stb.append("  BASE.JUDGEMENT, ");
            stb.append("  BASE.NAME, ");
            stb.append("  ADDR.ADDRESS1, ");
            stb.append("  ADDR.ADDRESS2, ");
            stb.append("  BASEDTL.REMARK1  ");
            stb.append(" FROM ");
            stb.append("  ENTEXAM_RECEPT_DAT RECEPT ");
            stb.append(" LEFT JOIN ");
            stb.append("  ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV AND BASE.TESTDIV = RECEPT.TESTDIV AND BASE.EXAMNO = RECEPT.EXAMNO ");
            stb.append(" LEFT JOIN ");
            stb.append("  ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL ON BASEDTL.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND BASEDTL.APPLICANTDIV = RECEPT.APPLICANTDIV AND BASEDTL.EXAMNO = RECEPT.EXAMNO AND BASEDTL.SEQ = '033' ");
            stb.append(" LEFT JOIN ");
            stb.append("  ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND ADDR.APPLICANTDIV = RECEPT.APPLICANTDIV AND ADDR.EXAMNO = RECEPT.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("  RECEPT.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
            stb.append("  RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("  RECEPT.TESTDIV = '" + _param._testDiv + "' AND ");
            stb.append("  BASE.JUDGEMENT IN ('1', '3') AND ");
            stb.append("  RECEPT.EXAM_TYPE = '1' ");
            stb.append(" ORDER BY ");
            stb.append("  RECEPT.EXAMNO ");

            log.debug(" applicant sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String entexamyear = rs.getString("ENTEXAMYEAR");
                final String applicantdiv = rs.getString("APPLICANTDIV");
                final String testdiv = rs.getString("TESTDIV");
                final String exam_Type = rs.getString("EXAM_TYPE");
                final String examno = rs.getString("EXAMNO");
                final String total_Rank4 = rs.getString("TOTAL_RANK4");
                final String judgement = rs.getString("JUDGEMENT");
                final String name = rs.getString("NAME");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String remark1 = rs.getString("REMARK1");

                if(!retMap.containsKey(examno)) {
                    final GoukakuCatalog goukakuCatalog = new GoukakuCatalog(entexamyear, applicantdiv, testdiv, exam_Type, examno,
                            total_Rank4, judgement, name, address1, address2, remark1);
                    retMap.put(examno, goukakuCatalog);
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

    // 3:合格証
    private void print3GoukakuProof(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        final Map print3Map = getPrint3Map(db2); //志願者Map

        if(print3Map.isEmpty()) { //対象なし
            return;
        }
        log.debug(print3Map.size());

        for(Iterator printite = print3Map.keySet().iterator(); printite.hasNext();) {
            final String key = (String) printite.next();
            final GoukakuProof gp = (GoukakuProof) print3Map.get(key);

            svf.VrSetForm("KNJL850H_2.frm", 1);
            svf.VrsOut("EXAM_NO", gp._examno); //受験番号

            final String fieldName = getFieldName2(gp._name);
            svf.VrsOut("NAME" + fieldName, gp._name); //氏名

            final String date =  KNJ_EditDate.h_format_JP(db2, _param._date);
            svf.VrsOut("DATE", date); //日付

            svf.VrsOut("SCHOOL_NAME", _param._schoolName); //学校名
            svf.VrsOut("PRINCIPAL_NAME", _param._principalName); //校長名

            if(_param._stampFilePath != null) {
                svf.VrsOut("SCHOOL_STAMP", _param._stampFilePath); //校長印
            }

            svf.VrEndPage();
        }
        _hasData = true;
    }

    // 3:合格証 SQL取得
    private Map getPrint3Map(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("  BASE.ENTEXAMYEAR, ");
            stb.append("  BASE.APPLICANTDIV, ");
            stb.append("  BASE.TESTDIV, ");
            stb.append("  BASE.EXAMNO, ");
            stb.append("  BASE.JUDGEMENT, ");
            stb.append("  BASE.NAME ");
            stb.append(" FROM ");
            stb.append("  ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append(" WHERE ");
            stb.append("  BASE.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
            stb.append("  BASE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("  BASE.TESTDIV = '" + _param._testDiv + "' AND ");
            stb.append("  BASE.JUDGEMENT IN ('1','3') "); //合格、補欠合格
            stb.append(" ORDER BY ");
            stb.append("  BASE.EXAMNO ");

            log.debug(" applicant sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String entexamyear = rs.getString("ENTEXAMYEAR");
                final String applicantdiv = rs.getString("APPLICANTDIV");
                final String testdiv = rs.getString("TESTDIV");
                final String examno = rs.getString("EXAMNO");
                final String judgement = rs.getString("JUDGEMENT");
                final String name = rs.getString("NAME");

                if(!retMap.containsKey(examno)) {
                    final GoukakuProof goukakuProof = new GoukakuProof(entexamyear, applicantdiv, testdiv, examno,
                            judgement, name);
                    retMap.put(examno, goukakuProof);
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

    // 4:入学許可証
    private void print4Permit(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        final Map print4Map = getPrint4Map(db2); //志願者Map

        if(print4Map.isEmpty()) { //対象なし
            return;
        }

        log.debug(print4Map.size());

        for(Iterator printite = print4Map.keySet().iterator(); printite.hasNext();) {
            final String key = (String) printite.next();
            final Permit permit = (Permit) print4Map.get(key);

            svf.VrSetForm("KNJL850H_3.frm", 1);
            svf.VrsOut("EXAM_NO", permit._examno); //受験番号

            final String fieldName = getFieldName2(permit._name);
            svf.VrsOut("NAME" + fieldName, permit._name); //氏名

            svf.VrsOut("SCHOOL_NAME", _param._schoolName); //学校名
            svf.VrsOut("PRINCIPAL_NAME", _param._principalName); //校長名

            if(_param._stampFilePath != null) {
                svf.VrsOut("SCHOOL_STAMP", _param._stampFilePath); //校長印
            }

            svf.VrEndPage();
        }
        _hasData = true;
    }

    // 4:入学許可証  SQL取得
    private Map getPrint4Map(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("  BASE.ENTEXAMYEAR, ");
            stb.append("  BASE.APPLICANTDIV, ");
            stb.append("  BASE.TESTDIV, ");
            stb.append("  BASE.EXAMNO, ");
            stb.append("  BASE.ENTDIV, ");
            stb.append("  BASE.NAME ");
            stb.append(" FROM ");
            stb.append("  ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append(" WHERE ");
            stb.append("  BASE.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
            stb.append("  BASE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("  BASE.TESTDIV = '" + _param._testDiv + "' AND ");
            stb.append("  BASE.JUDGEMENT IN ('1', '3') AND ");
            stb.append("  BASE.ENTDIV = '1' ");
            stb.append(" ORDER BY ");
            stb.append("  BASE.EXAMNO ");

            log.debug(" applicant sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String entexamyear = rs.getString("ENTEXAMYEAR");
                final String applicantdiv = rs.getString("APPLICANTDIV");
                final String testdiv = rs.getString("TESTDIV");
                final String examno = rs.getString("EXAMNO");
                final String entdiv = rs.getString("ENTDIV");
                final String name = rs.getString("NAME");

                if(!retMap.containsKey(examno)) {
                    final Permit permit = new Permit(entexamyear, applicantdiv, testdiv, examno,
                            entdiv, name);
                    retMap.put(examno, permit);
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

    // 5:繰上候補者一覧表
    private void print5AdvanceCatalog(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        final Map print5Map = getPrint5Map(db2); //志願者Map
        if(print5Map.isEmpty()) { //対象なし
            return;
        }

        log.debug(print5Map.size());

        final int maxLine = 20; //最大印字行
        int page = 0; // ページ
        int line = 1; //印字行

        for(Iterator printite = print5Map.keySet().iterator(); printite.hasNext();) {
            final String key = (String) printite.next();
            final AdvanceCatalog ac = (AdvanceCatalog) print5Map.get(key);
            if (line > maxLine || page == 0) {
                if(line > maxLine) svf.VrEndPage();
                page++;
                line = 1;
                svf.VrSetForm("KNJL850H_4.frm", 1);
                svf.VrsOut("DATE", _param._date.replace("-", "/")); //日付
                svf.VrsOut("PAGE", String.valueOf(page) + "頁"); //ページ
                svf.VrsOut("TITLE", _param._examYear + "年度 " + "□□□ 繰上候補者一覧表 □□□"); //タイトル
                svf.VrsOut("EXAM_DIV", _param._testName); //入試区分
                svf.VrsOut("SCHOOL_NAME", _param._schoolName); //学校名
            }

            svf.VrsOutn("RANK", line, ac._sub_Order); //順位
            svf.VrsOutn("EXAM_NO1", line, ac._examno); //受験番号

            final String fieldKana = getFieldName(ac._name_Kana, "1");
            svf.VrsOutn("KANA1_" + fieldKana, line, ac._name_Kana); //カナ
            final String fieldName = getFieldName(ac._name, "1");
            svf.VrsOutn("NAME1_" + fieldName, line, ac._name); //氏名

            final String gengou = KNJ_EditDate.gengouAlphabetMarkOfDate(db2, ac._birthday);
            final String[] date = KNJ_EditDate.tate_format4(db2, ac._birthday);
            if(date != null) {
                final String printDate = gengou + date[1] + "." + date[2] + "." + date[3];
                svf.VrsOutn("BIRTHDAY", line, printDate); //生年月日
            }
            svf.VrsOutn("SEX", line, "1".equals(ac._sex) ? "男" : "女"); //性別
            svf.VrsOutn("ZIP_NO", line, ac._zipcd); //郵便番号
            svf.VrsOutn("TEL_NO", line, ac._telno); //電話番号

            final String addr = StringUtils.defaultString(ac._address1) + StringUtils.defaultString(ac._address2);
            final String fieldAddr = getFieldName(addr, "2");
            svf.VrsOutn("ADDR" + fieldAddr, line, addr); //住所
            final String kuriagePass = ("3".equals(ac._judgement)) ? "合格 " : "";
            final String remark = StringUtils.defaultString(kuriagePass) + StringUtils.defaultString(ac._remark1);
            svf.VrsOutn("REMARK", line, remark); // 備考

            line++;
        }
        svf.VrEndPage();
        _hasData = true;
    }

    // 5:繰上候補者一覧表  SQL取得
    private Map getPrint5Map(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("  RECEPT.ENTEXAMYEAR, ");
            stb.append("  RECEPT.APPLICANTDIV, ");
            stb.append("  RECEPT.TESTDIV, ");
            stb.append("  RECEPT.EXAMNO, ");
            stb.append("  BASE.JUDGEMENT, ");
            stb.append("  BASE.SUB_ORDER, ");
            stb.append("  BASE.NAME, ");
            stb.append("  BASE.NAME_KANA, ");
            stb.append("  BASE.SEX, ");
            stb.append("  BASE.BIRTHDAY, ");
            stb.append("  ADDR.ZIPCD, ");
            stb.append("  ADDR.ADDRESS1, ");
            stb.append("  ADDR.ADDRESS2, ");
            stb.append("  ADDR.TELNO, ");
            stb.append("  BASEDTL.REMARK1 ");
            stb.append(" FROM ");
            stb.append("  ENTEXAM_RECEPT_DAT RECEPT ");
            stb.append(" LEFT JOIN ");
            stb.append("  ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV AND BASE.EXAMNO = RECEPT.EXAMNO AND BASE.TESTDIV = RECEPT.TESTDIV ");
            stb.append(" LEFT JOIN ");
            stb.append("  ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND ADDR.APPLICANTDIV = RECEPT.APPLICANTDIV AND ADDR.EXAMNO = RECEPT.EXAMNO ");
            stb.append(" LEFT JOIN ");
            stb.append("  ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL ON BASEDTL.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND BASEDTL.APPLICANTDIV = RECEPT.APPLICANTDIV AND BASEDTL.EXAMNO = RECEPT.EXAMNO AND BASEDTL.SEQ = '033' ");
            stb.append(" WHERE ");
            stb.append("  RECEPT.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
            stb.append("  RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("  RECEPT.TESTDIV = '" + _param._testDiv + "' AND ");
            stb.append("  RECEPT.EXAM_TYPE = '1' AND ");
            stb.append("  BASE.JUDGEMENT IN ('3', '4') ");
            stb.append(" ORDER BY ");
            stb.append("  BASE.SUB_ORDER ");

            log.debug(" applicant sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String entexamyear = rs.getString("ENTEXAMYEAR");
                final String applicantdiv = rs.getString("APPLICANTDIV");
                final String testdiv = rs.getString("TESTDIV");
                final String examno = rs.getString("EXAMNO");
                final String judgement = rs.getString("JUDGEMENT");
                final String sub_Order = rs.getString("SUB_ORDER");
                final String name = rs.getString("NAME");
                final String name_Kana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String birthday = rs.getString("BIRTHDAY");
                final String zipcd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String telno = rs.getString("TELNO");
                final String remark1 = rs.getString("REMARK1");

                if (!retMap.containsKey(examno)) {
                    final AdvanceCatalog advanceCatalog = new AdvanceCatalog(entexamyear, applicantdiv, testdiv, examno,
                    		judgement, sub_Order, name, name_Kana, sex, birthday, zipcd, address1, address2, telno,
                            remark1);
                    retMap.put(examno, advanceCatalog);
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

    // 6:繰上候補者通知書
    private void print6AdvanceNotice(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        final Map print6Map = getPrint6Map(db2); //志願者Map
        if(print6Map.isEmpty()) { //対象なし
            return;
        }

        log.debug(print6Map.size());
        final String form = "1".equals(_param._applicantDiv) ? "KNJL850H_5.frm" : "KNJL850H_6.frm";

        for(Iterator printite = print6Map.keySet().iterator(); printite.hasNext();) {
            final String key = (String) printite.next();
            final AdvanceNotice ac = (AdvanceNotice) print6Map.get(key);

            svf.VrSetForm(form, 1);
            svf.VrsOut("NO1", _param._number); //No.

            final String date =  KNJ_EditDate.h_format_JP(db2, _param._date);
            svf.VrsOut("DATE", date); //発行日

            svf.VrsOut("SCHOOL_NAME", _param._schoolName); //学校名
            svf.VrsOut("PRINCIPAL_NAME", _param._principalName); //校長名
            svf.VrsOut("EXAN_NO", ac._examno); //受験番号

            final int keta = KNJ_EditEdit.getMS932ByteLength(ac._name);
            final String fieldName = keta <= 22 ? "1" : keta <= 30 ? "2" : "3";
            svf.VrsOut("NAME" + fieldName, ac._name); //氏名

            svf.VrsOut("RANK", ac._sub_Order); //順位
            svf.VrsOut("PLAN", _param._yoteikikan); //連絡予定期間
            svf.VrsOut("LIMIT", _param._yoteibi); //通知予定日
            svf.VrsOut("NO2", _param._number); //No.
            svf.VrsOut("TEL_NO", _param._denwabango); //電話番号

            svf.VrEndPage();
        }
        _hasData = true;
    }

    // 6:繰上候補者通知書  SQL取得
    private Map getPrint6Map(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("  RECEPT.ENTEXAMYEAR, ");
            stb.append("  RECEPT.APPLICANTDIV, ");
            stb.append("  RECEPT.TESTDIV, ");
            stb.append("  RECEPT.EXAMNO, ");
            stb.append("  BASE.JUDGEMENT, ");
            stb.append("  BASE.SUB_ORDER, ");
            stb.append("  BASE.NAME ");
            stb.append(" FROM ");
            stb.append("  ENTEXAM_RECEPT_DAT RECEPT ");
            stb.append(" LEFT JOIN ");
            stb.append("  ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV AND BASE.EXAMNO = RECEPT.EXAMNO AND BASE.TESTDIV = RECEPT.TESTDIV ");
            stb.append(" WHERE ");
            stb.append("  RECEPT.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
            stb.append("  RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("  RECEPT.TESTDIV = '" + _param._testDiv + "' AND ");
            stb.append("  RECEPT.EXAM_TYPE = '1' AND ");
            stb.append("  BASE.JUDGEMENT = '4' ");
            stb.append(" ORDER BY ");
            stb.append("  BASE.SUB_ORDER,RECEPT.EXAMNO ");

            log.debug(" applicant sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String entexamyear = rs.getString("ENTEXAMYEAR");
                final String applicantdiv = rs.getString("APPLICANTDIV");
                final String testdiv = rs.getString("TESTDIV");
                final String examno = rs.getString("EXAMNO");
                final String judgement = rs.getString("JUDGEMENT");
                final String sub_Order = rs.getString("SUB_ORDER");
                final String name = rs.getString("NAME");

                if (!retMap.containsKey(examno)) {
                    final AdvanceNotice advanceNotice = new AdvanceNotice(entexamyear, applicantdiv, testdiv, examno, judgement, sub_Order, name);
                    retMap.put(examno, advanceNotice);
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

    private String getFieldName(final String str, final String flg) {
        final int keta = KNJ_EditEdit.getMS932ByteLength(str);

        if("1".equals(flg)) {
            return keta <= 20 ? "1" : keta <= 30 ? "2" : "3"; //氏名
        } else {
            return keta <= 40 ? "1" : keta <= 50 ? "2" : "3"; //住所
        }
    }

    //print3,4
    private String getFieldName2(final String str) {
        final int keta = KNJ_EditEdit.getMS932ByteLength(str);
        return keta <= 14 ? "1" : keta <= 20 ? "2" : keta <= 30 ? "3" : "4"; //氏名
    }

    // 1:合格者一覧表クラス
    private class GoukakuCatalog {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _exam_Type;
        final String _examno;
        final String _total_Rank4;
        final String _judgement;
        final String _name;
        final String _address1;
        final String _address2;
        final String _remark1;


        public GoukakuCatalog(final String entexamyear, final String applicantdiv, final String testdiv,
                final String exam_Type, final String examno, final String total_Rank4, final String judgement,
                final String name, final String address1, final String address2, final String remark1) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _exam_Type = exam_Type;
            _examno = examno;
            _total_Rank4 = total_Rank4;
            _judgement = judgement;
            _name = name;
            _address1 = address1;
            _address2 = address2;
            _remark1 = remark1;

        }
    }

    // 3:合格証明書クラス
    private class GoukakuProof {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _examno;
        final String _judgement;
        final String _name;

        public GoukakuProof(final String entexamyear, final String applicantdiv, final String testdiv,
                final String examno, final String judgement, final String name) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _examno = examno;
            _judgement = judgement;
            _name = name;
        }
    }

    // 4:入学許可証クラス
    private class Permit {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _examno;
        final String _entdiv;
        final String _name;

        public Permit(final String entexamyear, final String applicantdiv, final String testdiv,
                final String examno, final String entdiv, final String name) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _examno = examno;
            _entdiv = entdiv;
            _name = name;
        }
    }

    // 5:繰上候補者一覧表クラス
    private class AdvanceCatalog {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _examno;
        final String _judgement;
        final String _sub_Order;
        final String _name;
        final String _name_Kana;
        final String _sex;
        final String _birthday;
        final String _zipcd;
        final String _address1;
        final String _address2;
        final String _telno;
        final String _remark1;

        public AdvanceCatalog(final String entexamyear, final String applicantdiv, final String testdiv,
                final String examno, final String judgement, final String sub_Order, final String name,
                final String name_Kana, final String sex, final String birthday, final String zipcd,
                final String address1, final String address2, final String telno, final String remark1) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _examno = examno;
            _judgement = judgement;
            _sub_Order = sub_Order;
            _name = name;
            _name_Kana = name_Kana;
            _sex = sex;
            _birthday = birthday;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
            _telno = telno;
            _remark1 = remark1;
        }
    }

    // 6:繰上候補者通知書クラス
    private class AdvanceNotice {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _examno;
        final String _judgement;
        final String _sub_Order;
        final String _name;

        public AdvanceNotice(final String entexamyear, final String applicantdiv, final String testdiv,
                final String examno, final String judgement, final String sub_Order, final String name) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _examno = examno;
            _judgement = judgement;
            _sub_Order = sub_Order;
            _name = name;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 76037 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _examYear;
        final String _applicantDiv; //入試制度
        final String _testDiv; //入試区分
        final String _outputDiv;  //各種帳票
        final String _testName; //入試区分名称
        final String _number; //No.
        String _yoteikikan; //連絡予定期間
        String _yoteibi; //不合格通知予定日
        final String _denwabango; //連絡電話番号
        final String _date;
        final String _schoolName;
        final String _principalName;
        final String _imagepath;
        final String _documentroot;
        final String _stampFilePath;
        final boolean _iscsv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _examYear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _outputDiv = request.getParameter("OUTPUT");
              _date = request.getParameter("LOGIN_DATE");

              _number = request.getParameter("NUMBER");
              try {
                _yoteikikan = new String(StringUtils.defaultString(request.getParameter("YOTEIKIKAN")).getBytes("ISO8859-1"));
                _yoteibi = new String(StringUtils.defaultString(request.getParameter("YOTEIBI")).getBytes("ISO8859-1"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
              _denwabango = request.getParameter("DENWABANGO");
              _testName = getTestDivAbbv(db2);
              _schoolName = getSchoolName(db2);
              _principalName = getPrincipalName(db2);
              _documentroot = request.getParameter("DOCUMENTROOT");
              _iscsv = "2".equals(_outputDiv);

              if("3".equals(_outputDiv) || "4".equals(_outputDiv)) {
                  KNJ_Control imagepath_extension = new KNJ_Control();                //取得クラスのインスタンス作成
                KNJ_Control.ReturnVal returnval = imagepath_extension.Control(db2);
                _imagepath = returnval.val4;                                        //写真データ格納フォルダ
                if ("1".equals(_applicantDiv)) {
                    _stampFilePath = getImageFilePath("SCHOOLSTAMP_J.bmp");//校長印
                } else {
                    _stampFilePath = getImageFilePath("SCHOOLSTAMP_H.bmp");//校長印
                }
              } else {
                  _imagepath = null;
                  _stampFilePath = null;
              }

        }

        private String getPrincipalName(final DB2UDB db2) {
            final String kindcd = "1".equals(_applicantDiv) ? "105" : "104";
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT PRINCIPAL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _examYear + "' AND CERTIF_KINDCD = '" + kindcd + "' "));
        }

        private String getTestDivAbbv(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTDIV_NAME FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' "));
        }

        private String getSchoolName(final DB2UDB db2) {
            final String kindcd = "1".equals(_applicantDiv) ? "105" : "104";
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _examYear + "' AND CERTIF_KINDCD = '" + kindcd + "' "));
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFilePath(final String filename) {
            if (null == _documentroot || null == _imagepath || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentroot).append("/").append(_imagepath).append("/").append(filename);
            final File file = new File(path.toString());
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }
    }

    public static String h_format_Seireki_MD(final String date) {
        if (null == date) {
            return date;
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        String retVal = "";
        sdf.applyPattern("yyyy年M月d日");
        retVal = sdf.format(java.sql.Date.valueOf(date));

        return retVal;
    }
}

// eof
