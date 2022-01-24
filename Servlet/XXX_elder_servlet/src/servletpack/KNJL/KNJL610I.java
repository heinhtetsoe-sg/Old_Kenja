/*
 * 作成日: 2020/12/14
 * 作成者: ishimine
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL610I {

    private static final Log log = LogFactory.getLog(KNJL610I.class);

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
        if ("1".equals(_param._output)) {
            printApplicantListCheck(db2, svf);
        } else if ("2".equals(_param._output)) {
            printApplicantList(db2, svf);
        } else if ("3".equals(_param._output)) {
            printAttendance(db2, svf);
        } else if ("4".equals(_param._output)) {
            printScore(db2, svf);
        } else if ("5".equals(_param._output)) {
            printInterView(db2, svf);
        }
    }

    /**
     * 1:志願者リスト（窓口確認用）を出力する。
     *
     * @param db2
     * @param svf
     */
    private void printApplicantListCheck(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL610I_1.frm", 1);

        final int maxLine = 50;
        int lineCnt = 1; // 書き込み行数
        String hallCd = ""; //会場コード　改ページ用
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 ";

        List<PrintData1> printData1List = getApplicantListCheck(db2);

        for(PrintData1 printData1 : printData1List) {
            // 改ページの制御
            if (lineCnt > maxLine || !printData1._hallcd.equals(hallCd)) {
                if(!"".equals(hallCd)) {
                    svf.VrsOut("NUM", (lineCnt -1) + "名"); //人数
                    svf.VrEndPage();
                }
                lineCnt = 1;
                hallCd = printData1._hallcd;
                svf.VrsOut("TITLE", nendo + "志願者リスト(" + _param._testDivName + ")");
                svf.VrsOut("HALL_NAME", printData1._hallName + "会場");
                svf.VrsOut("RANGE", printData1._s_ReceptNo + "～" + printData1._e_ReceptNo);
            }

            svf.VrsOutn("NO", lineCnt, printData1._no);
            svf.VrsOutn("FUCULTY_NAME", lineCnt, printData1._gakka);
            String receptno = printData1._examno;
            if ("1".equals(printData1._duplicateFlg)) {
                receptno = "*" + receptno;
            }
            svf.VrsOutn("EXAM_NO", lineCnt, receptno);

            final int nameByte = KNJ_EditEdit.getMS932ByteLength(printData1._name);
            final String nameFieldStr = nameByte <= 20 ? "1" : nameByte <= 30 ? "2" : "3";
            svf.VrsOutn("NAME" + nameFieldStr, lineCnt, printData1._name);

            final int kanaByte = KNJ_EditEdit.getMS932ByteLength(printData1._name_Kana);
            final String kanaFieldStr = kanaByte <= 20 ? "1" : kanaByte <= 30 ? "2" : "3";
            svf.VrsOutn("KANA" + kanaFieldStr, lineCnt, printData1._name_Kana);

            svf.VrsOutn("SEX", lineCnt, printData1._sex);
            svf.VrsOutn("BIRTHDAY", lineCnt, printData1._birthday);

            final int schoolByte = KNJ_EditEdit.getMS932ByteLength(printData1._finschool_Name_Abbv);
            final String schoolFieldStr = schoolByte <= 20 ? "1" : schoolByte <= 30 ? "2" : "3";
            svf.VrsOutn("FINSCHOOL_NAME" + schoolFieldStr, lineCnt, printData1._finschool_Name_Abbv);

            lineCnt++;
            _hasData = true;
        }
        svf.VrsOut("NUM", (lineCnt -1) + "名"); //人数
        svf.VrEndPage();
    }

    /**
     * 2:志願者リストを出力する。
     *
     * @param db2
     * @param svf
     */
    private void printApplicantList(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL610I_2.frm", 1);

        int lineCnt = 1; // 書き込み行数
        final int maxLine = 40;
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 ";
        final String subtitle = "1".equals(_param._special) ? "志願者全員" : "2".equals(_param._special) ? "特待生・学業" : "特待生・部活";

        List<PrintData2> printData2List = getApplicantList(db2);

        svf.VrsOut("TITLE", nendo + "志願者リスト(" + _param._testDivName + ")(" + subtitle + ")" );

        for(PrintData2 printData2 : printData2List) {
            // 改ページの制御
            if (lineCnt > maxLine) {
                lineCnt = 1;
                svf.VrEndPage();

                svf.VrsOut("TITLE", nendo + "志願者リスト（" + _param._testDivName + "）（" + subtitle + "）" );
            }

            svf.VrsOutn("NO", lineCnt, printData2._no);
            svf.VrsOutn("EXAM_DIV", lineCnt, _param._testDivName);
            String receptno = printData2._examno;
            if ("1".equals(printData2._duplicateFlg)) {
                receptno = "*" + receptno;
            }
            svf.VrsOutn("EXAM_NO", lineCnt, receptno);
            svf.VrsOutn("HOPE_RANK1", lineCnt, printData2._aspiring1);
            svf.VrsOutn("HOPE_RANK2", lineCnt, printData2._aspiring2);
            svf.VrsOutn("HOPE_RANK3", lineCnt, printData2._aspiring3);
            svf.VrsOutn("HOPE_RANK4", lineCnt, printData2._aspiring4);

            final int nameByte = KNJ_EditEdit.getMS932ByteLength(printData2._name);
            final String nameFieldStr = nameByte <= 20 ? "1" : nameByte <= 30 ? "2" : "3";
            svf.VrsOutn("NAME" + nameFieldStr, lineCnt, printData2._name);

            final int kanaByte = KNJ_EditEdit.getMS932ByteLength(printData2._name_Kana);
            final String kanaFieldStr = kanaByte <= 20 ? "1" : kanaByte <= 30 ? "2" : "3";
            svf.VrsOutn("KANA1_" + kanaFieldStr, lineCnt, printData2._name_Kana);

            final int hiraByte = KNJ_EditEdit.getMS932ByteLength(printData2._name_Hira);
            final String hiraFieldStr = hiraByte <= 20 ? "1" : hiraByte <= 30 ? "2" : "3";
            svf.VrsOutn("KANA2_" + hiraFieldStr, lineCnt, printData2._name_Hira);

            svf.VrsOutn("SEX", lineCnt, printData2._sex);
            svf.VrsOutn("FINSCHOOL_CD", lineCnt, printData2._fs_Cd);

            final int schoolByte = KNJ_EditEdit.getMS932ByteLength(printData2._finschool_Name_Abbv);
            final String schoolFieldStr = schoolByte <= 20 ? "1" : schoolByte <= 30 ? "2" : "3";
            svf.VrsOutn("FINSCHOOL_NAME" + schoolFieldStr, lineCnt, printData2._finschool_Name_Abbv);

            svf.VrsOutn("SP_CD", lineCnt, printData2._generalCd);
            svf.VrsOutn("SP_NAME", lineCnt, printData2._generalMark);
            svf.VrsOutn("SP_REASON", lineCnt, printData2._spReason);
            svf.VrsOutn("SCHOLAR_HOPE", lineCnt, printData2._shogaku);
            svf.VrsOutn("DORMITORY_NAME", lineCnt, printData2._ryo);

            final int gnameByte = KNJ_EditEdit.getMS932ByteLength(printData2._gname);
            final String gnameFieldStr = gnameByte <= 16 ? "1" : gnameByte <= 20 ? "2" : gnameByte <= 30 ? "3": "4";
            svf.VrsOutn("GUARD_NAME" + gnameFieldStr, lineCnt, printData2._gname);

            lineCnt++;
            _hasData = true;
        }
        svf.VrEndPage();
    }

    /**
     * 3:出欠席点検確認書を出力する。
     *
     * @param db2
     * @param svf
     */
    private void printAttendance(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL610I_3.frm", 1);

        int lineCnt = 1; // 書き込み行数
        String hallCd = ""; //会場コード　改行用
        String subclassCd= ""; //科目コード　改行用
        final int maxLine = 50;
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 ";

        List<PrintData3> printData3List = getAttendance(db2);

        for(PrintData3 printData3 : printData3List) {
            if ("1".equals(_param._testSubclasscdCheck)) {
                // 改ページの制御
                if (lineCnt > maxLine || !printData3._examhall_Cd.equals(hallCd) ||  !printData3._subclassCd.equals(subclassCd)) {
                    if (!"".equals(hallCd)) {
                        svf.VrEndPage();
                    }
                    lineCnt = 1;
                    hallCd = printData3._examhall_Cd;
                    subclassCd = printData3._subclassCd;
                    svf.VrsOut("TITLE", nendo + _param._testDivName + " 出欠席点検確認書" );
                    svf.VrsOut("HALL_NAME", "試験会場【" + printData3._examhall_Name + "】　" + printData3._subclassName);
                }
            } else {
                // 改ページの制御
                if (lineCnt > maxLine || !printData3._examhall_Cd.equals(hallCd)) {
                    if (!"".equals(hallCd)) {
                        svf.VrEndPage();
                    }
                    lineCnt = 1;
                    hallCd = printData3._examhall_Cd;
                    svf.VrsOut("TITLE", nendo + _param._testDivName + " 出欠席点検確認書" );
                    svf.VrsOut("HALL_NAME", "試験会場【" + printData3._examhall_Name + "】");
                }
            }

            svf.VrsOutn("NO", lineCnt, printData3._no);
            svf.VrsOutn("EXAM_NO", lineCnt, printData3._examno);
            String receptno = printData3._examno;
            if ("1".equals(printData3._duplicateFlg)) {
                receptno = "*" + receptno;
            }
            svf.VrsOutn("EXAM_NO", lineCnt, receptno);

            final int nameByte = KNJ_EditEdit.getMS932ByteLength(printData3._name + " " + printData3._name_Kana);
            final String nameFieldStr = nameByte <= 30 ? "1" : nameByte <= 40 ? "2" : nameByte <= 50 ? "3" : "4";
            svf.VrsOutn("NAME" + nameFieldStr, lineCnt, printData3._name + " " + printData3._name_Kana);

            final int schoolByte = KNJ_EditEdit.getMS932ByteLength(printData3._finschool_Name_Abbv);
            final String schoolFieldStr = schoolByte <= 20 ? "1" : schoolByte <= 30 ? "2" : "3";
            svf.VrsOutn("FINSCHOOL_NAME" + schoolFieldStr, lineCnt, printData3._finschool_Name_Abbv);

            lineCnt++;
            _hasData = true;
        }
        svf.VrEndPage();
    }

    /**
     * 4:得点記入書を出力する。
     *
     * @param db2
     * @param svf
     */
    private void printScore(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL610I_4.frm", 1);

        int lineCnt = 1; // 書き込み行数
        String subclassCd = ""; //科目コード　改行用
        String hallCd = ""; //会場コード　改行用
        final int maxLine = 50;
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 ";

        List<PrintData4> printData4List = getScore(db2);

        for(PrintData4 printData4 : printData4List) {
            // 改ページの制御
            if (lineCnt > maxLine || !printData4._examhallCd.equals(hallCd) || !printData4._subclassCd.equals(subclassCd)) {
                if (!"".equals(hallCd)) {
                    svf.VrEndPage();
                }
                lineCnt = 1;
                subclassCd = printData4._subclassCd;
                hallCd = printData4._examhallCd;

                svf.VrsOut("TITLE", nendo + _param._testDivName + " 得点記入書");

                //教科名
                svf.VrsOut("CLASS_NAME", printData4._subclassName);
                //会場名
                svf.VrsOut("HALL_NAME", printData4._examhallName);
                //受験番号From
                svf.VrsOut("EXAM_NO_FROM", printData4._s_receptno);
                //受験番号To
                svf.VrsOut("EXAM_NO_TO", printData4._e_receptno);
            }

            //連番
            svf.VrsOutn("NO", lineCnt, printData4._no);
            //受験番号
            svf.VrsOutn("EXAM_NO", lineCnt, printData4._examno);

            lineCnt++;
            _hasData = true;
        }
        svf.VrEndPage();
    }

    /**
     * 5:面接表を出力する。
     *
     * @param db2
     * @param svf
     */
    private void printInterView(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL610I_5.frm", 1);

        int lineCnt = 1; // 書き込み行数
        final int maxLine = 12;
        String hallCd = ""; //会場コード　改行用
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 ";

        List<PrintData5> printData5List = getInterView(db2);

        for(PrintData5 printData5 : printData5List) {
            // 改ページの制御
            if (lineCnt > maxLine || !printData5._examhallcd.equals(hallCd)) {
                if (!"".equals(hallCd)) {
                    svf.VrEndPage();
                }
                lineCnt = 1;
                hallCd = printData5._examhallcd;
                //タイトル
                svf.VrsOut("TITLE", nendo + _param._testDivName + " 面接表");
                //会場番号
                svf.VrsOut("HALL_NO", hallCd);
            }

            //受験番号
            svf.VrsOutn("EXAM_NO", lineCnt, printData5._examno);

            //中学校
            final int schoolByte = KNJ_EditEdit.getMS932ByteLength(printData5._finschool_Name_Abbv);
            final String schoolFieldStr = schoolByte <= 20 ? "1" : schoolByte <= 30 ? "2" : "3";
            svf.VrsOutn("FINSCHOOL_NAME" + schoolFieldStr, lineCnt, printData5._finschool_Name_Abbv);

            //氏名
            final int nameByte = KNJ_EditEdit.getMS932ByteLength(printData5._name);
            final String nameFieldStr = nameByte <= 20 ? "1" : nameByte <= 30 ? "2" : "3";
            svf.VrsOutn("NAME" + nameFieldStr, lineCnt, printData5._name);

            //カナ
            final int kanaByte = KNJ_EditEdit.getMS932ByteLength(printData5._name_Kana);
            final String kanaFieldStr = kanaByte <= 20 ? "1" : kanaByte <= 30 ? "2" : "3";
            svf.VrsOutn("KANA1_" + kanaFieldStr, lineCnt, printData5._name_Kana);

            //志望学科　類・コース
            svf.VrsOutn("HOPE_COURSE", lineCnt, printData5._general_Abbv);

            //出欠状況
            svf.VrsOutn("ATTEND1", lineCnt, printData5._absence_Days);
            svf.VrsOutn("ATTEND2", lineCnt, printData5._absence_Days2);
            svf.VrsOutn("ATTEND3", lineCnt, printData5._absence_Days3);

            //入寮予定
            svf.VrsOutn("DORMITORY", lineCnt, printData5._ryo);

            lineCnt++;
            _hasData = true;
        }
        svf.VrEndPage();
    }

    //カタカナ⇒ひらがな
    private String getHiraFrom(String str) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
          char code = str.charAt(i);
          if ((code >= 0x30a1) && (code <= 0x30f3)) {
            sb.append((char) (code - 0x60));
          } else {
            sb.append(code);
          }
        }
        return sb.toString();
    }

    private List<PrintData1> getApplicantListCheck(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<PrintData1> printData1List = new ArrayList<PrintData1>();
        PrintData1 printData1 = null;

        try {
            final String sql = getApplicantListCheckSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String no = rs.getString("NO");
                final String gakka = rs.getString("GAKKA");
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String name_Kana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String birthday = rs.getString("BIRTHDAY");
                final String finschool_Name_Abbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String hallcd = rs.getString("EXAMHALLCD");
                final String s_ReceptNo = rs.getString("S_RECEPTNO");
                final String e_ReceptNo = rs.getString("E_RECEPTNO");
                final String hallName = rs.getString("EXAMHALL_NAME");
                final String duplicateFlg = rs.getString("DUPLICATE_FLG");

                printData1 = new PrintData1(no, gakka, examno, name, name_Kana, sex, birthday, finschool_Name_Abbv, hallcd, s_ReceptNo, e_ReceptNo, hallName, duplicateFlg);
                printData1List.add(printData1);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return printData1List;
    }

    private List<PrintData2> getApplicantList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<PrintData2> printData2List = new ArrayList<PrintData2>();
        PrintData2 printData2 = null;

        try {
            final String sql = getApplicantListSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String no = rs.getString("NO");
                final String testdiv_Abbv = rs.getString("TESTDIV_ABBV");
                final String examno = rs.getString("EXAMNO");
                final String aspiring1 = rs.getString("ASPIRING1");
                final String aspiring2 = rs.getString("ASPIRING2");
                final String aspiring3 = rs.getString("ASPIRING3");
                final String aspiring4 = rs.getString("ASPIRING4");
                final String name = rs.getString("NAME");
                final String name_Kana = rs.getString("NAME_KANA");
                final String name_Hira = getHiraFrom(name_Kana);
                final String sex = rs.getString("SEX");
                final String fs_Cd = rs.getString("FS_CD");
                final String finschool_Name_Abbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String generalCd = rs.getString("REMARK2");
                final String generalMark = rs.getString("GENERAL_MARK");
                final String spReason = rs.getString("SP_REASON");
                final String shogaku = rs.getString("SHOGAKU");
                final String ryo = rs.getString("RYO");
                final String gname = rs.getString("GNAME");
                final String duplicateFlg = rs.getString("DUPLICATE_FLG");

                printData2 = new PrintData2(no, testdiv_Abbv, examno, aspiring1, aspiring2, aspiring3, aspiring4, name,
                        name_Kana, name_Hira, sex, fs_Cd, finschool_Name_Abbv, generalCd, generalMark, spReason, shogaku, ryo,
                        gname, duplicateFlg);
                printData2List.add(printData2);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return printData2List;
    }

    private List<PrintData3> getAttendance(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<PrintData3> printData3List = new ArrayList<PrintData3>();
        PrintData3 printData3 = null;

        try {
            final String sql = getAttendanceSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String no = rs.getString("NO");
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String name_Kana = rs.getString("NAME_KANA");
                final String finschool_Name = rs.getString("FINSCHOOL_NAME_ABBV");
                final String examhall_Name_Abbv = rs.getString("EXAMHALL_NAME");
                final String examhall_Cd = rs.getString("EXAMHALLCD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassName = rs.getString("SUBCLASS_NAME");
                final String duplicateFlg = rs.getString("DUPLICATE_FLG");

                printData3 = new PrintData3(no, examno, name, name_Kana, finschool_Name, examhall_Name_Abbv, examhall_Cd, subclassCd, subclassName, duplicateFlg);
                printData3List.add(printData3);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return printData3List;
    }

    private List<PrintData4> getScore(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<PrintData4> printData4List = new ArrayList<PrintData4>();
        PrintData4 printData4 = null;

        try {
            final String sql = getScoreSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examhallCd = rs.getString("EXAMHALLCD");
                final String examhallName = rs.getString("EXAMHALL_NAME");
                final String subclassCd = rs.getString("SEQ");
                final String subclassName = rs.getString("NAME1");
                final String no = rs.getString("NO");
                final String examno = rs.getString("EXAMNO");
                final String s_receptno = rs.getString("S_RECEPTNO");
                final String e_receptno = rs.getString("E_RECEPTNO");

                printData4 = new PrintData4(examhallCd, examhallName, subclassCd, subclassName, no, examno, s_receptno, e_receptno);
                printData4List.add(printData4);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return printData4List;
    }

    private List<PrintData5> getInterView(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<PrintData5> printData5List = new ArrayList<PrintData5>();
        PrintData5 printData5 = null;

        try {
            final String sql = getInterViewSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examhall_Name = rs.getString("EXAMHALL_NAME");
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String name_Kana = rs.getString("NAME_KANA");
                final String finschool_Name_Abbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String examhallcd = rs.getString("EXAMHALLCD");
                final String remark1 = rs.getString("REMARK1");
                final String general_Abbv = rs.getString("GENERAL_ABBV");
                final String absence_Days = rs.getString("ABSENCE_DAYS");
                final String absence_Days2 = rs.getString("ABSENCE_DAYS2");
                final String absence_Days3 = rs.getString("ABSENCE_DAYS3");
                final String ryo = rs.getString("RYO");

                printData5 = new PrintData5(examhall_Name, examno, name, name_Kana, finschool_Name_Abbv, examhallcd, remark1, general_Abbv, absence_Days, absence_Days2, absence_Days3, ryo);
                printData5List.add(printData5);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return printData5List;
    }

    private String getApplicantListCheckSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if("1".equals(_param._sort)) {
            stb.append("   ROW_NUMBER() OVER(PARTITION BY HALL.EXAMHALLCD ORDER BY BASE.EXAMNO, BASE.NAME_KANA) AS NO, ");
        } else {
            stb.append("   ROW_NUMBER() OVER(PARTITION BY HALL.EXAMHALLCD ORDER BY BASE.NAME_KANA, BASE.EXAMNO) AS NO, ");
        }
        stb.append("   CASE WHEN BASE.TESTDIV0 = '1' THEN '普通科' ELSE '工業科' END AS GAKKA, ");
        stb.append("   BASE.EXAMNO, ");
        stb.append("   BASE.NAME, ");
        stb.append("   BASE.NAME_KANA, ");
        stb.append("   NAME.NAME2 AS SEX, ");
        stb.append("   REPLACE(BASE.BIRTHDAY, '-' ,'') AS BIRTHDAY, ");
        stb.append("   SCHOOL.FINSCHOOL_NAME_ABBV, ");
        stb.append("   HALL.EXAMHALLCD, ");
        stb.append("   HALL.S_RECEPTNO, ");
        stb.append("   HALL.E_RECEPTNO, ");
        stb.append("   HALL.EXAMHALL_NAME, ");
        stb.append("   CASE WHEN BASE_D012.REMARK1 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK1 THEN '1' ");
        stb.append("        WHEN BASE_D012.REMARK2 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK2 THEN '1' ");
        stb.append("        WHEN BASE_D012.REMARK3 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK3 THEN '1' ");
        stb.append("        ELSE NULL ");
        stb.append("   END AS DUPLICATE_FLG ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BD031.APPLICANTDIV = BASE.APPLICANTDIV AND BD031.EXAMNO = BASE.EXAMNO AND BD031.SEQ = '031' ");
        stb.append(" LEFT JOIN ");
        stb.append("   V_NAME_MST NAME ON NAME.YEAR = BASE.ENTEXAMYEAR AND NAMECD1 = 'Z002' AND NAMECD2 = BASE.SEX ");
        stb.append(" LEFT JOIN ");
        stb.append("   FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_HALL_YDAT HALL ON HALL.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND HALL.APPLICANTDIV = BASE.APPLICANTDIV AND HALL.TESTDIV = BASE.TESTDIV AND HALL.EXAM_TYPE = BASE.TESTDIV0 ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D012 ON BASE_D012.ENTEXAMYEAR  = BASE.ENTEXAMYEAR AND BASE_D012.APPLICANTDIV = BASE.APPLICANTDIV AND BASE_D012.EXAMNO = BASE.EXAMNO AND BASE_D012.SEQ = '012' ");
        stb.append(" WHERE ");
        stb.append("   BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' AND ");
        stb.append("   BASE.APPLICANTDIV = '2' AND ");
        stb.append("   BASE.TESTDIV = '" + _param._testDiv + "' AND ");
        stb.append("   BASE.TESTDIV0 = '" + _param._gakka + "' AND ");
        if(!"".equals(_param._ruibetsu)) {
            stb.append("   BD031.REMARK7 = '" + _param._ruibetsu + "' AND ");
        }
        if(!"".equals(_param._course)) {
            stb.append("   BD031.REMARK1 = '" + _param._course + "' AND ");
        }
        if(!"".equals(_param._hall)) {
            stb.append("   HALL.EXAMHALLCD = '" + _param._hall + "' AND ");
        }
        if(!"3".equals(_param._sex)) {
            stb.append("   BASE.SEX = '" + _param._sex + "' AND ");
        }
        stb.append("   BASE.EXAMNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ");

        return stb.toString();
    }

    private String getApplicantListSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if("1".equals(_param._sort)) {
            stb.append("   ROW_NUMBER() OVER(ORDER BY BASE.EXAMNO, BASE.NAME_KANA) AS NO, ");
        } else {
            stb.append("   ROW_NUMBER() OVER(ORDER BY BASE.NAME_KANA, BASE.EXAMNO) AS NO, ");
        }
        stb.append("   TEST.TESTDIV_ABBV, ");
        stb.append("   BASE.EXAMNO, ");
        stb.append("   BD031.REMARK1 AS ASPIRING1, ");
        stb.append("   VALUE(BD031.REMARK2, '0') AS ASPIRING2, ");
        stb.append("   VALUE(BD031.REMARK3, '0') AS ASPIRING3, ");
        stb.append("   VALUE(BD031.REMARK4, '0') AS ASPIRING4, ");
        stb.append("   BASE.NAME, ");
        stb.append("   BASE.NAME_KANA, ");
        stb.append("   '' AS NAME_HIRA, ");
        stb.append("   NAME.NAME2 AS SEX, ");
        stb.append("   BASE.FS_CD, ");
        stb.append("   SCHOOL.FINSCHOOL_NAME_ABBV, ");
        stb.append("   CD009.REMARK2, ");
        stb.append("   GENE04.GENERAL_MARK, ");
        stb.append("   GENE05.GENERAL_MARK AS SP_REASON, ");
        stb.append("   CASE WHEN BD031.REMARK6 = '1' THEN '有' END AS SHOGAKU, ");
        stb.append("   CASE WHEN BASE.DORMITORY_FLG = '1' THEN SET042.NAME1 END AS RYO, ");
        stb.append("   ADDR.GNAME, ");
        stb.append("   CASE WHEN BASE_D012.REMARK1 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK1 THEN '1' ");
        stb.append("        WHEN BASE_D012.REMARK2 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK2 THEN '1' ");
        stb.append("        WHEN BASE_D012.REMARK3 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK3 THEN '1' ");
        stb.append("        ELSE NULL ");
        stb.append("   END AS DUPLICATE_FLG ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append(" LEFT JOIN ");
        stb.append("   V_NAME_MST NAME ON NAME.YEAR = BASE.ENTEXAMYEAR AND NAMECD1 = 'Z002' AND NAMECD2 = BASE.SEX ");
        stb.append(" LEFT JOIN ");
        stb.append("   FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_HALL_YDAT HALL ON HALL.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND HALL.APPLICANTDIV = BASE.APPLICANTDIV AND HALL.TESTDIV = BASE.TESTDIV AND HALL.EXAM_TYPE = BASE.TESTDIV0 ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BD031.APPLICANTDIV = BASE.APPLICANTDIV AND BD031.EXAMNO = BASE.EXAMNO AND BD031.SEQ = '031' ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_TESTDIV_MST TEST ON TEST.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND TEST.APPLICANTDIV = BASE.APPLICANTDIV AND TEST.TESTDIV = BASE.TESTDIV ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD009 ON CD009.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND CD009.APPLICANTDIV = BASE.APPLICANTDIV AND CD009.EXAMNO = BASE.EXAMNO AND CD009.SEQ = '009' ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV AND ADDR.EXAMNO = BASE.EXAMNO ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_SETTING_MST SET042 ON SET042.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND SET042.APPLICANTDIV = BASE.APPLICANTDIV AND SET042.SETTING_CD = 'L042' AND SET042.SEQ = BASE.SEX ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GENE04 ON GENE04.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE04.APPLICANTDIV = BASE.APPLICANTDIV AND GENE04.TESTDIV = '0' AND GENE04.GENERAL_DIV = '04' AND GENE04.GENERAL_CD = CD009.REMARK2 ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GENE05 ON GENE05.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE05.APPLICANTDIV = BASE.APPLICANTDIV AND GENE05.TESTDIV = '0' AND GENE05.GENERAL_DIV = '05' AND GENE05.GENERAL_CD = CD009.REMARK3 ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D012 ON BASE_D012.ENTEXAMYEAR  = BASE.ENTEXAMYEAR AND BASE_D012.APPLICANTDIV = BASE.APPLICANTDIV AND BASE_D012.EXAMNO = BASE.EXAMNO AND BASE_D012.SEQ = '012' ");
        stb.append(" WHERE ");
        stb.append("   BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' AND ");
        stb.append("   BASE.APPLICANTDIV = '2' AND ");
        stb.append("   BASE.TESTDIV = '" + _param._testDiv + "' AND ");
        stb.append("   BASE.TESTDIV0 = '" + _param._gakka + "' AND ");
        if(!"".equals(_param._ruibetsu)) {
            stb.append("   BD031.REMARK7 = '" + _param._ruibetsu + "' AND ");
        }
        if(!"".equals(_param._course)) {
            stb.append("   BD031.REMARK1 = '" + _param._course + "' AND ");
        }
        if(!"".equals(_param._hall)) {
            stb.append("   HALL.EXAMHALLCD = '" + _param._hall + "' AND ");
        }
        if(!"3".equals(_param._sex)) {
            stb.append("   BASE.SEX = '" + _param._sex + "' AND ");
        }
        if("2".equals(_param._special)) {
            stb.append("   CD009.REMARK3 = '01' AND ");
        } else if("3".equals(_param._special)) {
            stb.append("   CD009.REMARK3 != '01' AND ");
        }
        stb.append("   BASE.EXAMNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ");

        return stb.toString();
    }

    private String getAttendanceSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if("1".equals(_param._sort)) {
            if ("1".equals(_param._testSubclasscdCheck)) {
                stb.append("   ROW_NUMBER() OVER(PARTITION BY HALL.EXAMHALLCD, L009.SEQ ORDER BY HALL.EXAMHALLCD, L009.SEQ, BASE.EXAMNO, BASE.NAME_KANA) AS NO, ");
            } else {
                stb.append("   ROW_NUMBER() OVER(PARTITION BY HALL.EXAMHALLCD ORDER BY HALL.EXAMHALLCD, BASE.EXAMNO, BASE.NAME_KANA) AS NO, ");
            }
        } else {
            if ("1".equals(_param._testSubclasscdCheck)) {
                stb.append("   ROW_NUMBER() OVER(PARTITION BY HALL.EXAMHALLCD, L009.SEQ ORDER BY HALL.EXAMHALLCD, L009.SEQ, BASE.NAME_KANA, BASE.EXAMNO) AS NO, ");
            } else {
                stb.append("   ROW_NUMBER() OVER(PARTITION BY HALL.EXAMHALLCD ORDER BY HALL.EXAMHALLCD, BASE.NAME_KANA, BASE.EXAMNO) AS NO, ");
            }
        }
        stb.append("   BASE.EXAMNO, ");
        stb.append("   BASE.NAME, ");
        stb.append("   BASE.NAME_KANA, ");
        stb.append("   SCHOOL.FINSCHOOL_NAME_ABBV, ");
        stb.append("   HALL.EXAMHALLCD, ");
        stb.append("   HALL.EXAMHALL_NAME, ");
        if ("1".equals(_param._testSubclasscdCheck)) {
            stb.append("   L009.SEQ AS SUBCLASSCD, ");
            stb.append("   L009.NAME1 AS SUBCLASS_NAME, ");
        } else {
            stb.append("   '' AS SUBCLASSCD, ");
            stb.append("   '' AS SUBCLASS_NAME, ");
        }
        stb.append("   CASE WHEN BASE_D012.REMARK1 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK1 THEN '1' ");
        stb.append("        WHEN BASE_D012.REMARK2 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK2 THEN '1' ");
        stb.append("        WHEN BASE_D012.REMARK3 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK3 THEN '1' ");
        stb.append("        ELSE NULL ");
        stb.append("   END AS DUPLICATE_FLG ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_HALL_YDAT HALL  ");
        if ("1".equals(_param._testSubclasscdCheck)) {
            stb.append(" INNER JOIN ( ");
            stb.append("   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK1 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ");
            stb.append("   UNION ALL ");
            stb.append("   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK2 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ");
            stb.append("   UNION ALL ");
            stb.append("   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK3 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ");
            stb.append("   UNION ALL ");
            stb.append("   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK4 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ");
            stb.append("   UNION ALL ");
            stb.append("   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK5 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ");
            stb.append("   UNION ALL ");
            stb.append("   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK6 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ");
            stb.append("   UNION ALL ");
            stb.append("   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK7 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ");
            stb.append("   UNION ALL ");
            stb.append("   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK8 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ");
            stb.append("   UNION ALL ");
            stb.append("   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK9 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ");
            stb.append(" ) TD002 ON TD002.ENTEXAMYEAR = HALL.ENTEXAMYEAR AND TD002.APPLICANTDIV = HALL.APPLICANTDIV AND TD002.TESTDIV = HALL.TESTDIV AND TD002.SEQ = '002' AND TD002.REMARK IS NOT NULL ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_SETTING_MST L009 ON L009.ENTEXAMYEAR = TD002.ENTEXAMYEAR AND L009.APPLICANTDIV = TD002.APPLICANTDIV AND L009.SETTING_CD = 'L009' AND L009.SEQ = TD002.REMARK ");
        }
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = HALL.ENTEXAMYEAR AND BASE.APPLICANTDIV = HALL.APPLICANTDIV AND BASE.TESTDIV = HALL.TESTDIV AND BASE.TESTDIV0 = HALL.EXAM_TYPE AND BASE.EXAMNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ");
        stb.append(" LEFT JOIN ");
        stb.append("   FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D012 ON BASE_D012.ENTEXAMYEAR  = BASE.ENTEXAMYEAR AND BASE_D012.APPLICANTDIV = BASE.APPLICANTDIV AND BASE_D012.EXAMNO = BASE.EXAMNO AND BASE_D012.SEQ = '012' ");
        stb.append(" WHERE ");
        stb.append("   HALL.ENTEXAMYEAR = '" + _param._entexamyear + "' AND ");
        stb.append("   HALL.APPLICANTDIV = '2' AND ");
        stb.append("   HALL.TESTDIV = '" + _param._testDiv + "' AND ");
        stb.append("   BASE.EXAMNO IS NOT NULL  AND ");
        stb.append("   HALL.EXAM_TYPE = '" + _param._gakka + "' ");
        if(!"3".equals(_param._sex)) {
            stb.append("   AND BASE.SEX = '" + _param._sex + "' ");
        }
        if(!"".equals(_param._hall)) {
            stb.append("   AND HALL.EXAMHALLCD = '" + _param._hall + "' ");
        }

        return stb.toString();
    }

    private String getScoreSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   HALL.EXAMHALLCD, ");
        stb.append("   HALL.EXAMHALL_NAME, ");
        stb.append("   HALL.S_RECEPTNO, ");
        stb.append("   HALL.E_RECEPTNO, ");
        stb.append("   SET009.SEQ, ");
        stb.append("   SET009.NAME1, ");
        if("1".equals(_param._sort)) {
            stb.append(" ROW_NUMBER() OVER(PARTITION BY HALL.EXAMHALLCD, SET009.SEQ ORDER BY HALL.EXAMHALLCD, SET009.SEQ, BASE.EXAMNO, BASE.NAME_KANA) AS NO, ");
        } else {
            stb.append(" ROW_NUMBER() OVER(PARTITION BY HALL.EXAMHALLCD, SET009.SEQ ORDER BY HALL.EXAMHALLCD, SET009.SEQ, BASE.NAME_KANA, BASE.EXAMNO) AS NO, ");
        }
        stb.append("   BASE.EXAMNO ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_HALL_YDAT HALL  ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = HALL.ENTEXAMYEAR AND BASE.APPLICANTDIV = HALL.APPLICANTDIV AND BASE.TESTDIV = HALL.TESTDIV AND BASE.TESTDIV0 = HALL.EXAM_TYPE AND BASE.EXAMNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ");
        stb.append(" LEFT JOIN ");
        stb.append("  ENTEXAM_SETTING_MST SET009 ON SET009.ENTEXAMYEAR = HALL.ENTEXAMYEAR AND SET009.APPLICANTDIV = HALL.APPLICANTDIV AND SET009.SETTING_CD = 'L009' ");
        stb.append(" WHERE ");
        stb.append("   HALL.ENTEXAMYEAR = '" + _param._entexamyear + "' AND ");
        stb.append("   HALL.APPLICANTDIV = '2' AND ");
        stb.append("   HALL.TESTDIV = '" + _param._testDiv + "' AND ");
        stb.append("   BASE.EXAMNO IS NOT NULL  AND ");
        stb.append("   HALL.EXAM_TYPE = '" + _param._gakka + "' ");
        if(!"".equals(_param._hall)) {
            stb.append("   AND HALL.EXAMHALLCD = '" + _param._hall + "' ");
        }
        return stb.toString();
    }

    private String getInterViewSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     HALL.EXAMHALL_NAME, ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     SCHOOL.FINSCHOOL_NAME_ABBV, ");
        stb.append("     HALL.EXAMHALLCD, ");
        stb.append("     BD031.REMARK1, ");
        stb.append("     GDIV02.GENERAL_ABBV, ");
        stb.append("     CONF.ABSENCE_DAYS, ");
        stb.append("     CONF.ABSENCE_DAYS2, ");
        stb.append("     CONF.ABSENCE_DAYS3, ");
        stb.append("     CASE WHEN BASE.DORMITORY_FLG = '1' THEN '有' END AS RYO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_HALL_YDAT HALL ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = HALL.ENTEXAMYEAR AND BASE.APPLICANTDIV = HALL.APPLICANTDIV AND BASE.TESTDIV = HALL.TESTDIV AND BASE.TESTDIV0 = HALL.EXAM_TYPE AND BASE.EXAMNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ");
        stb.append(" LEFT JOIN ");
        stb.append("   FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BD031.APPLICANTDIV = BASE.APPLICANTDIV AND BD031.EXAMNO = BASE.EXAMNO AND BD031.SEQ = '031' ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_GENERAL_MST GDIV02 ON GDIV02.ENTEXAMYEAR = BD031.ENTEXAMYEAR AND GDIV02.APPLICANTDIV = BD031.APPLICANTDIV AND GDIV02.GENERAL_DIV = '02' AND GDIV02.GENERAL_CD = BD031.REMARK1 ");
        stb.append(" LEFT JOIN ");
        stb.append("   ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND CONF.APPLICANTDIV = BASE.APPLICANTDIV AND CONF.EXAMNO = BASE.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("   HALL.ENTEXAMYEAR = '" + _param._entexamyear + "' AND ");
        stb.append("   HALL.APPLICANTDIV = '2' AND ");
        stb.append("   HALL.TESTDIV = '" + _param._testDiv + "' AND ");
        stb.append("   BASE.EXAMNO IS NOT NULL  AND ");
        stb.append("   HALL.EXAM_TYPE = '" + _param._gakka + "' ");
        if(!"".equals(_param._hall)) {
            stb.append("   AND HALL.EXAMHALLCD = '" + _param._hall + "' ");
        }
        stb.append(" ORDER BY ");
        if("1".equals(_param._sort)) {
            stb.append("   HALL.EXAMHALLCD, BASE.EXAMNO, BASE.NAME_KANA ");
        } else {
            stb.append("   HALL.EXAMHALLCD, BASE.NAME_KANA, BASE.EXAMNO ");
        }

        return stb.toString();
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class PrintData1 {
        final String _no;
        final String _gakka;
        final String _examno;
        final String _name;
        final String _name_Kana;
        final String _sex;
        final String _birthday;
        final String _finschool_Name_Abbv;
        final String _hallcd;
        final String _s_ReceptNo;
        final String _e_ReceptNo;
        final String _hallName;
        final String _duplicateFlg;

        PrintData1(final String no, final String gakka, final String examno, final String name, final String name_Kana,
                final String sex, final String birthday, final String finschool_Name, final String hallcd,
                final String s_ReceptNo, final String e_ReceptNo, final String hallName, final String duplicateFlg) {
            _no = no;
            _gakka = gakka;
            _examno = examno;
            _name = name;
            _name_Kana = name_Kana;
            _sex = sex;
            _birthday = birthday;
            _finschool_Name_Abbv = finschool_Name;
            _hallcd = hallcd;
            _s_ReceptNo = s_ReceptNo;
            _e_ReceptNo = e_ReceptNo;
            _hallName = hallName;
            _duplicateFlg = duplicateFlg;
        }
    }

    private class PrintData2 {
        final String _no;
        final String _testdiv_Abbv;
        final String _examno;
        final String _aspiring1;
        final String _aspiring2;
        final String _aspiring3;
        final String _aspiring4;
        final String _name;
        final String _name_Kana;
        final String _name_Hira;
        final String _sex;
        final String _fs_Cd;
        final String _finschool_Name_Abbv;
        final String _generalCd;
        final String _generalMark;
        final String _spReason;
        final String _shogaku;
        final String _ryo;
        final String _gname;
        final String _duplicateFlg;

        PrintData2(final String no, final String testdiv_Abbv, final String examno, final String aspiring1,
                final String aspiring2, final String aspiring3, final String aspiring4, final String name,
                final String name_Kana, final String name_Hira, final String sex, final String fs_Cd,
                final String finschool_Name, final String generalCd, final String generalMark,
                final String spReason, final String shogaku, final String ryo, final String gname, final String duplicateFlg) {
            _no = no;
            _testdiv_Abbv = testdiv_Abbv;
            _examno = examno;
            _aspiring1 = aspiring1;
            _aspiring2 = aspiring2;
            _aspiring3 = aspiring3;
            _aspiring4 = aspiring4;
            _name = name;
            _name_Kana = name_Kana;
            _name_Hira = name_Hira;
            _sex = sex;
            _fs_Cd = fs_Cd;
            _finschool_Name_Abbv = finschool_Name;
            _generalCd = generalCd;
            _generalMark = generalMark;
            _spReason = spReason;
            _shogaku = shogaku;
            _ryo = ryo;
            _gname = gname;
            _duplicateFlg = duplicateFlg;
        }
    }

    private class PrintData3 {
        final String _no;
        final String _examno;
        final String _name;
        final String _name_Kana;
        final String _finschool_Name_Abbv;
        final String _examhall_Name;
        final String _examhall_Cd;
        final String _subclassCd;
        final String _subclassName;
        final String _duplicateFlg;

        PrintData3(
            final String no,
            final String examno,
            final String name,
            final String name_Kana,
            final String finschool_Name,
            final String examhall_Name,
            final String examhall_Cd,
            final String subclassCd,
            final String subclassName,
            final String duplicateFlg
        ) {
            _no = no;
            _examno = examno;
            _name = name;
            _name_Kana = name_Kana;
            _finschool_Name_Abbv = finschool_Name;
            _examhall_Name = examhall_Name;
            _examhall_Cd = examhall_Cd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _duplicateFlg = duplicateFlg;
        }
    }

    private class PrintData4 {
        final String _examhallCd;
        final String _examhallName;
        final String _subclassCd;
        final String _subclassName;
        final String _no;
        final String _examno;
        final String _s_receptno;
        final String _e_receptno;

        PrintData4(final String examhallCd, final String examhallName, final String subclassCd,
                final String subclassName, final String no, final String examno, final String s_receptno,
                final String e_receptno) {
            _examhallCd = examhallCd;
            _examhallName = examhallName;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _no = no;
            _examno = examno;
            _s_receptno = s_receptno;
            _e_receptno = e_receptno;
        }
    }

    private class PrintData5 {
        final String _examhall_Name;
        final String _examno;
        final String _name;
        final String _name_Kana;
        final String _finschool_Name_Abbv;
        final String _examhallcd;
        final String _remark1;
        final String _general_Abbv;
        final String _absence_Days;
        final String _absence_Days2;
        final String _absence_Days3;
        final String _ryo;

        PrintData5(final String examhall_Name, final String examno, final String name, final String name_Kana,
                final String finschool_Name, final String examhallcd, final String remark1,
                final String general_Abbv, final String absence_Days, final String absence_Days2,
                final String absence_Days3, final String ryo) {
            _examhall_Name = examhall_Name;
            _examno = examno;
            _name = name;
            _name_Kana = name_Kana;
            _finschool_Name_Abbv = finschool_Name;
            _examhallcd = examhallcd;
            _remark1 = remark1;
            _general_Abbv = general_Abbv;
            _absence_Days = absence_Days;
            _absence_Days2 = absence_Days2;
            _absence_Days3 = absence_Days3;
            _ryo = ryo;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _testDiv;
        private final String _sex;
        private final String _order;
        private final String _sort;
        private final String _output;
        private final String _testSubclasscdCheck;
        private final String _gakka;
        private final String _special;
        private final String _hall;
        private final String _course;
        private final String _ruibetsu;
        private final String _testDivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _testDiv = request.getParameter("TESTDIV");
            _sex = request.getParameter("SEX");
            _order = request.getParameter("ORDER");
            _sort = request.getParameter("SORT");
            _output = request.getParameter("OUTPUT");
            _testSubclasscdCheck = request.getParameter("TESTSUBCLASSCD_CHECK");
            _gakka = request.getParameter("GAKKA");
            _special = request.getParameter("SPECIAL");
            _hall = request.getParameter("HALL");
            _course = request.getParameter("COURSE");
            _ruibetsu = request.getParameter("RUIBETSU");
            _testDivName = getTestDivName(db2);
        }

        private String getTestDivName(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            String sql = " SELECT TESTDIV_NAME FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR='" + _entexamyear + "' AND APPLICANTDIV = '2' AND TESTDIV = '" + _testDiv + "' ";
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTDIV_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
    }
}

// eof

