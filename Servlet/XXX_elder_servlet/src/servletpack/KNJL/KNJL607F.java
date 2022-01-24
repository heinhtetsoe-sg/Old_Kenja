/*
 * $Id: d2fa0630f3fbb51a9adb07490ca78a82386006fe $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３０７Ｂ＞  志願者情報確認表
 **/
public class KNJL607F {

    private static final Log log = LogFactory.getLog(KNJL607F.class);

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
    
    public void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List list = Applicant.load(db2, _param);
        
        final String form = "KNJL607F.frm";
        final int maxLine = 20;
        final List pageList = getPageList(list, maxLine);

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List applicantList = (List) pageList.get(pi);

            svf.VrSetForm(form, 1);
            
            String title0 = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 " + StringUtils.defaultString(_param._applicantdivName) + " " + StringUtils.defaultString(_param._testdivName);
            svf.VrsOut("TITLE", title0 + " 志願者情報確認表"); // タイトル
            svf.VrsOut("DATE", _param._dateStr); // 作成日
            svf.VrsOut("PAGE1", String.valueOf(pi + 1)); // ページ
            svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // ページ

            if ("1".equals(_param._applicantdiv)) {
                svf.VrsOut("DETAIL_NAME1", "プレテスト1"); // プレテスト(2教科/3教科200点換算/合計)
                svf.VrsOut("DETAIL_NAME2", "プレテスト2"); // プレテスト(2教科/3教科200点換算/合計)
                svf.VrsOut("DETAIL_NAME3", "奨学区分"); // 奨学区分
            } else {
                svf.VrsOut("DETAIL_NAME1", "実力"); // 実力(3科合計/3科平均、5科合計/5科平均)
                svf.VrsOut("DETAIL_NAME2", "事前相談結果"); // 事前相談結果
                svf.VrsOut("DETAIL_NAME3", "奨学区分"); // 奨学区分
            }
            
            for (int j = 0; j < applicantList.size(); j++) {
                final Applicant appl = (Applicant) applicantList.get(j);
                final int line = j + 1;
                svf.VrsOutn("RECEPT_DATE", line, null == appl._receptdate ? null : appl._receptdate.replace('-', '.')); // 受付日
                svf.VrsOutn("EXAM_NO", line, appl._examno); // 受験番号
                svf.VrsOutn("KANA", line, appl._kana); // かな
                svf.VrsOutn("NAME1", line, appl._name); // 氏名
                svf.VrsOutn("SEX", line, appl._sexName); // 性別
                svf.VrsOutn("BIRTHDAY", line, _param.formatDateMarkDot(appl._birthday)); // 生年月日
                svf.VrsOutn("SCHOOL_CD", line, appl._fsCd); // 学校コード
                svf.VrsOutn("SCHOOL_NAME1", line, appl._finschoolName); // 学校名
                svf.VrsOutn("ZIPNO", line, appl._zipcd); // 郵便番号
                final String addrField;
                if (KNJ_EditEdit.getMS932ByteLength(appl._addr1) > 50 || KNJ_EditEdit.getMS932ByteLength(appl._addr2) > 50) {
                    addrField = "3";
                } else if (KNJ_EditEdit.getMS932ByteLength(appl._addr1) > 40 || KNJ_EditEdit.getMS932ByteLength(appl._addr2) > 40) {
                    addrField = "2";
                } else {
                    addrField = "1";
                }
                svf.VrsOutn("ADDR1_" + addrField, line, appl._addr1); // 住所1
                svf.VrsOutn("ADDR2_" + addrField, line, appl._addr2); // 住所2
//                svf.VrsOutn("ADDR1_3", line, appl._addr1); // 住所1
//                svf.VrsOutn("ADDR2_3", line, appl._addr2); // 住所2
                svf.VrsOutn("ACTIVE", line, appl._fsGrdDivName); // 現浪
                svf.VrsOutn("AVERAGE", line, appl._averageAll); // 評定平均
                svf.VrsOutn("SUBCLASS1", line, appl._confidentialRpt01); // 教科
                svf.VrsOutn("SUBCLASS2", line, appl._confidentialRpt02); // 教科
                svf.VrsOutn("SUBCLASS3", line, appl._confidentialRpt03); // 教科
                svf.VrsOutn("SUBCLASS4", line, appl._confidentialRpt04); // 教科
                svf.VrsOutn("SUBCLASS5", line, appl._confidentialRpt05); // 教科
                svf.VrsOutn("SUBCLASS6", line, appl._confidentialRpt06); // 教科
                svf.VrsOutn("SUBCLASS7", line, appl._confidentialRpt07); // 教科
                svf.VrsOutn("SUBCLASS8", line, appl._confidentialRpt08); // 教科
                svf.VrsOutn("SUBCLASS9", line, appl._confidentialRpt09); // 教科
                svf.VrsOutn("EIKEN_RANK", line, appl._eikenName); // 英検級数
                if ("1".equals(_param._applicantdiv)) {
                    svf.VrsOutn("DETAIL1", line, StringUtils.defaultString(appl._detail38_remark4) + "/" + StringUtils.defaultString(appl._detail38_remark5) + "/" + StringUtils.defaultString(appl._detail38_remark6)); // プレテスト1
                    svf.VrsOutn("DETAIL2", line, StringUtils.defaultString(appl._detail39_remark4) + "/" + StringUtils.defaultString(appl._detail39_remark5) + "/" + StringUtils.defaultString(appl._detail39_remark6)); // プレテスト2
                    svf.VrsOutn("DETAIL3", line, appl._shougakuName); // 奨学区分
                } else {
                    svf.VrsOutn("DETAIL1", line, StringUtils.defaultString(appl._mockTotal3) + "/" + StringUtils.defaultString(appl._mockAvg3) + "/" + StringUtils.defaultString(appl._mockTotal5) + "/" + StringUtils.defaultString(appl._mockAvg5)); // 実力 //TODO:高校のテーブル待ち
                    svf.VrsOutn("DETAIL2", line, appl._jizenName); // 事前相談結果 //TODO:高校のテーブル待ち
                    svf.VrsOutn("DETAIL3", line, appl._shougakuName); // 奨学区分
                }
                svf.VrsOutn("REMARK1", line, appl._detail5_remark4); // 中学(備考) 高校(特別条件)
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    private static class Applicant {
        final String _receptdate;
        final String _examno;
        final String _kana;
        final String _name;
        final String _sexName;
        final String _birthday;
        final String _fsCd;
        final String _finschoolZipcd;
        final String _finschoolName;
        final String _fsGrdDivName;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _averageAll;
        final String _confidentialRpt01;
        final String _confidentialRpt02;
        final String _confidentialRpt03;
        final String _confidentialRpt04;
        final String _confidentialRpt05;
        final String _confidentialRpt06;
        final String _confidentialRpt07;
        final String _confidentialRpt08;
        final String _confidentialRpt09;
        final String _detail38_remark4;
        final String _detail38_remark5;
        final String _detail38_remark6;
        final String _detail39_remark4;
        final String _detail39_remark5;
        final String _detail39_remark6;
        final String _eikenName;
        final String _mockTotal3;
        final String _mockAvg3;
        final String _mockTotal5;
        final String _mockAvg5;
        final String _jizenName;
        final String _shougakuName;
        final String _detail5_remark4;

        Applicant(
            final String receptdate,
            final String examno,
            final String kana,
            final String name,
            final String sexName,
            final String birthday,
            final String fsCd,
            final String finschoolZipcd,
            final String finschoolName,
            final String fsGrdDivName,
            final String zipcd,
            final String addr1,
            final String addr2,
            final String averageAll,
            final String confidentialRpt01,
            final String confidentialRpt02,
            final String confidentialRpt03,
            final String confidentialRpt04,
            final String confidentialRpt05,
            final String confidentialRpt06,
            final String confidentialRpt07,
            final String confidentialRpt08,
            final String confidentialRpt09,
            final String detail38_remark4,
            final String detail38_remark5,
            final String detail38_remark6,
            final String detail39_remark4,
            final String detail39_remark5,
            final String detail39_remark6,
            final String eikenName,
            final String mockTotal3,
            final String mockAvg3,
            final String mockTotal5,
            final String mockAvg5,
            final String jizenName,
            final String shougakuName,
            final String detail5_remark4
        ) {
            _receptdate = receptdate;
            _examno = examno;
            _kana = kana;
            _name = name;
            _sexName = sexName;
            _birthday = birthday;
            _fsCd = fsCd;
            _finschoolZipcd = finschoolZipcd;
            _finschoolName = finschoolName;
            _fsGrdDivName = fsGrdDivName;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _averageAll = averageAll;
            _confidentialRpt01 = confidentialRpt01;
            _confidentialRpt02 = confidentialRpt02;
            _confidentialRpt03 = confidentialRpt03;
            _confidentialRpt04 = confidentialRpt04;
            _confidentialRpt05 = confidentialRpt05;
            _confidentialRpt06 = confidentialRpt06;
            _confidentialRpt07 = confidentialRpt07;
            _confidentialRpt08 = confidentialRpt08;
            _confidentialRpt09 = confidentialRpt09;
            _detail38_remark4 = detail38_remark4;
            _detail38_remark5 = detail38_remark5;
            _detail38_remark6 = detail38_remark6;
            _detail39_remark4 = detail39_remark4;
            _detail39_remark5 = detail39_remark5;
            _detail39_remark6 = detail39_remark6;
            _eikenName = eikenName;
            _mockTotal3 = mockTotal3;
            _mockAvg3 = mockAvg3;
            _mockTotal5 = mockTotal5;
            _mockAvg5 = mockAvg5;
            _jizenName = jizenName;
            _shougakuName = shougakuName;
            _detail5_remark4 = detail5_remark4;
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug("sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String receptdate = rs.getString("RECEPTDATE");
                    final String examno = rs.getString("EXAMNO");
                    final String kana = rs.getString("NAME_KANA");
                    final String name = rs.getString("NAME");
                    final String sexName = rs.getString("SEX_NAME");
                    final String birthday = rs.getString("BIRTHDAY");
                    final String fsCd = rs.getString("FS_CD");
                    final String finschoolZipcd = rs.getString("FINSCHOOL_ZIPCD");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String fsGrdDivName = rs.getString("FS_GRD_DIV_NAME");
                    final String zipcd = rs.getString("ZIPCD");
                    final String addr1 = rs.getString("ADDRESS1");
                    final String addr2 = rs.getString("ADDRESS2");
                    final String averageAll = rs.getString("AVERAGE_ALL");
                    final String confidentialRpt01 = rs.getString("CONFIDENTIAL_RPT01");
                    final String confidentialRpt02 = rs.getString("CONFIDENTIAL_RPT02");
                    final String confidentialRpt03 = rs.getString("CONFIDENTIAL_RPT03");
                    final String confidentialRpt04 = rs.getString("CONFIDENTIAL_RPT04");
                    final String confidentialRpt05 = rs.getString("CONFIDENTIAL_RPT05");
                    final String confidentialRpt06 = rs.getString("CONFIDENTIAL_RPT06");
                    final String confidentialRpt07 = rs.getString("CONFIDENTIAL_RPT07");
                    final String confidentialRpt08 = rs.getString("CONFIDENTIAL_RPT08");
                    final String confidentialRpt09 = rs.getString("CONFIDENTIAL_RPT09");
                    final String detail38_remark4 = rs.getString("DETAIL38_REMARK4");
                    final String detail38_remark5 = rs.getString("DETAIL38_REMARK5");
                    final String detail38_remark6 = rs.getString("DETAIL38_REMARK6");
                    final String detail39_remark4 = rs.getString("DETAIL39_REMARK4");
                    final String detail39_remark5 = rs.getString("DETAIL39_REMARK5");
                    final String detail39_remark6 = rs.getString("DETAIL39_REMARK6");
                    final String eikenName = rs.getString("EIKEN_NAME");
                    final String mockTotal3 = rs.getString("MOCK_TOTAL3");
                    final String mockAvg3 = rs.getString("MOCK_AVG3");
                    final String mockTotal5 = rs.getString("MOCK_TOTAL5");
                    final String mockAvg5 = rs.getString("MOCK_AVG5");
                    final String jizenName = rs.getString("JIZEN_NAME");
                    final String shougakuName = rs.getString("SHOUGAKU_NAME");
                    final String detail5_remark4 = rs.getString("DETAIL5_REMARK4");
                    final Applicant applicant = new Applicant(receptdate, examno, kana, name, sexName, birthday, fsCd, finschoolZipcd, finschoolName, fsGrdDivName, zipcd, addr1, addr2, averageAll, confidentialRpt01, confidentialRpt02, confidentialRpt03, confidentialRpt04, confidentialRpt05, confidentialRpt06, confidentialRpt07, confidentialRpt08, confidentialRpt09, detail38_remark4, detail38_remark5, detail38_remark6, detail39_remark4, detail39_remark5, detail39_remark6, eikenName, mockTotal3, mockAvg3, mockTotal5, mockAvg5, jizenName, shougakuName, detail5_remark4);
                    list.add(applicant);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH CONFRPT_DAT AS ( ");
            stb.append("     SELECT ");
            stb.append("         * ");
            stb.append("     FROM ");
            stb.append("         ENTEXAM_APPLICANTCONFRPT_DAT ");
            stb.append("     WHERE ");
            stb.append("         ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append(" ), T_CONF AS ( ");
            stb.append("     SELECT ");
            stb.append("         EXAMNO, ");
            stb.append("         CONFIDENTIAL_RPT01 AS SCORE ");
            stb.append("     FROM ");
            stb.append("         CONFRPT_DAT ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         EXAMNO, ");
            stb.append("         CONFIDENTIAL_RPT02 AS SCORE ");
            stb.append("     FROM ");
            stb.append("         CONFRPT_DAT ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         EXAMNO, ");
            stb.append("         CONFIDENTIAL_RPT03 AS SCORE ");
            stb.append("     FROM ");
            stb.append("         CONFRPT_DAT ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         EXAMNO, ");
            stb.append("         CONFIDENTIAL_RPT04 AS SCORE ");
            stb.append("     FROM ");
            stb.append("         CONFRPT_DAT ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         EXAMNO, ");
            stb.append("         CONFIDENTIAL_RPT05 AS SCORE ");
            stb.append("     FROM ");
            stb.append("         CONFRPT_DAT ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         EXAMNO, ");
            stb.append("         CONFIDENTIAL_RPT06 AS SCORE ");
            stb.append("     FROM ");
            stb.append("         CONFRPT_DAT ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         EXAMNO, ");
            stb.append("         CONFIDENTIAL_RPT07 AS SCORE ");
            stb.append("     FROM ");
            stb.append("         CONFRPT_DAT ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         EXAMNO, ");
            stb.append("         CONFIDENTIAL_RPT08 AS SCORE ");
            stb.append("     FROM ");
            stb.append("         CONFRPT_DAT ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         EXAMNO, ");
            stb.append("         CONFIDENTIAL_RPT09 AS SCORE ");
            stb.append("     FROM ");
            stb.append("         CONFRPT_DAT ");
            stb.append(" ), T_CONF_AVG AS ( ");
            stb.append("     SELECT ");
            stb.append("         EXAMNO, ");
            stb.append("         DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,4,1) AS AVERAGE_ALL ");
            stb.append("     FROM ");
            stb.append("         T_CONF ");
            stb.append("     GROUP BY ");
            stb.append("         EXAMNO ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("     BASE.RECEPTDATE, ");
            if ("1".equals(param._applicantdiv)) {
                stb.append("     DETAIL012.REMARK" + param._testdiv + " AS EXAMNO, ");
            } else {
                stb.append("     BASE.EXAMNO AS EXAMNO, ");
            }
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     BASE.NAME, ");
            stb.append("     NMZ002.NAME2 AS SEX_NAME, ");
            stb.append("     BASE.BIRTHDAY, ");
            stb.append("     BASE.FS_CD, ");
            stb.append("     FIN.FINSCHOOL_ZIPCD, ");
            stb.append("     FIN.FINSCHOOL_NAME, ");
            stb.append("     NML016.NAME1 AS FS_GRD_DIV_NAME, ");
            stb.append("     ADDR.ZIPCD, ");
            stb.append("     ADDR.ADDRESS1, ");
            stb.append("     ADDR.ADDRESS2, ");
            stb.append("     CONFAVG.AVERAGE_ALL, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT01, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT02, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT03, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT04, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT05, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT06, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT07, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT08, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT09, ");
            stb.append("     DETAIL038.REMARK4 AS DETAIL38_REMARK4, ");
            stb.append("     DETAIL038.REMARK5 AS DETAIL38_REMARK5, ");
            stb.append("     DETAIL038.REMARK6 AS DETAIL38_REMARK6, ");
            stb.append("     DETAIL039.REMARK4 AS DETAIL39_REMARK4, ");
            stb.append("     DETAIL039.REMARK5 AS DETAIL39_REMARK5, ");
            stb.append("     DETAIL039.REMARK6 AS DETAIL39_REMARK6, ");
            stb.append("     NML055.NAME1 AS EIKEN_NAME, ");
            stb.append("     DETAIL020.REMARK1 AS MOCK_TOTAL3, ");
            stb.append("     DETAIL020.REMARK2 AS MOCK_AVG3, ");
            stb.append("     DETAIL020.REMARK3 AS MOCK_TOTAL5, ");
            stb.append("     DETAIL020.REMARK4 AS MOCK_AVG5, ");
            stb.append("     NML032.NAME1 AS JIZEN_NAME, ");
            stb.append("     NML025.NAME1 AS SHOUGAKU_NAME, ");
            stb.append("     DETAIL005.REMARK4 AS DETAIL5_REMARK4 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ");
            stb.append("          ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND ADDR.APPLICANTDIV= BASE.APPLICANTDIV ");
            stb.append("         AND ADDR.EXAMNO = BASE.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ");
            stb.append("          ON CONFRPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND CONFRPT.APPLICANTDIV= BASE.APPLICANTDIV ");
            stb.append("         AND CONFRPT.EXAMNO = BASE.EXAMNO ");
            stb.append("     LEFT JOIN T_CONF_AVG CONFAVG ON CONFAVG.EXAMNO = BASE.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL010 ");
            stb.append("          ON DETAIL010.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND DETAIL010.APPLICANTDIV= BASE.APPLICANTDIV ");
            stb.append("         AND DETAIL010.EXAMNO      = BASE.EXAMNO ");
            stb.append("         AND DETAIL010.SEQ         = '010' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL012 ");
            stb.append("          ON DETAIL012.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND DETAIL012.APPLICANTDIV= BASE.APPLICANTDIV ");
            stb.append("         AND DETAIL012.EXAMNO      = BASE.EXAMNO ");
            stb.append("         AND DETAIL012.SEQ         = '012' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL005 ");
            stb.append("          ON DETAIL005.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND DETAIL005.APPLICANTDIV= BASE.APPLICANTDIV ");
            stb.append("         AND DETAIL005.EXAMNO      = BASE.EXAMNO ");
            stb.append("         AND DETAIL005.SEQ         = '005' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL038 ");
            stb.append("          ON DETAIL038.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND DETAIL038.APPLICANTDIV= BASE.APPLICANTDIV ");
            stb.append("         AND DETAIL038.EXAMNO      = BASE.EXAMNO ");
            stb.append("         AND DETAIL038.SEQ         = '038' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL039 ");
            stb.append("          ON DETAIL039.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND DETAIL039.APPLICANTDIV= BASE.APPLICANTDIV ");
            stb.append("         AND DETAIL039.EXAMNO      = BASE.EXAMNO ");
            stb.append("         AND DETAIL039.SEQ         = '039' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL004 ");
            stb.append("          ON DETAIL004.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND DETAIL004.APPLICANTDIV= BASE.APPLICANTDIV ");
            stb.append("         AND DETAIL004.EXAMNO      = BASE.EXAMNO ");
            stb.append("         AND DETAIL004.SEQ         = '004' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL020 ");
            stb.append("          ON DETAIL020.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND DETAIL020.APPLICANTDIV= BASE.APPLICANTDIV ");
            stb.append("         AND DETAIL020.EXAMNO      = BASE.EXAMNO ");
            stb.append("         AND DETAIL020.SEQ         = '020' ");
            stb.append("     LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = BASE.SEX ");
            stb.append("     LEFT JOIN NAME_MST NML016 ON NML016.NAMECD1 = 'L016' AND NML016.NAMECD2 = BASE.FS_GRDDIV ");
            stb.append("     LEFT JOIN NAME_MST NML055 ON NML055.NAMECD1 = 'L055' AND NML055.NAMECD2 = DETAIL005.REMARK1 ");
            stb.append("     LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025' AND NML025.NAMECD2 = DETAIL005.REMARK2 ");
            stb.append("     LEFT JOIN NAME_MST NML032 ON NML032.NAMECD1 = 'L032' AND NML032.NAMECD2 = DETAIL004.REMARK9 ");
            stb.append(" WHERE ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            if ("1".equals(param._applicantdiv)) {
                stb.append("     AND DETAIL010.REMARK" + param._testdiv + " = '" + param._testdiv + "' ");
            } else {
                stb.append("     AND BASE.TESTDIV = '" + param._testdiv + "' ");
            }
            if (null != param._examnoFrom && !"".equals(param._examnoFrom)) {
                if ("1".equals(param._applicantdiv)) {
                    stb.append("     AND DETAIL012.REMARK" + param._testdiv + " >= '" + param._examnoFrom + "' ");
                } else {
                    stb.append("     AND BASE.EXAMNO >= '" + param._examnoFrom + "' ");
                }
            }
            if (null != param._examnoTo && !"".equals(param._examnoTo)) {
                if ("1".equals(param._applicantdiv)) {
                    stb.append("     AND DETAIL012.REMARK" + param._testdiv + " <= '" + param._examnoTo + "' ");
                } else {
                    stb.append("     AND BASE.EXAMNO <= '" + param._examnoTo + "' ");
                }
            }
            stb.append(" ORDER BY ");
            stb.append("     EXAMNO ");
            return stb.toString();
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71709 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        String _examnoFrom;
        String _examnoTo;

        final String _dateStr;
        final String _applicantdivName;
        final String _testdivName;
        final List _nameMstL007;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _examnoFrom = request.getParameter("EXAMNO_FROM");
            _examnoTo = request.getParameter("EXAMNO_TO");
            
            _dateStr = getDateStr(db2, _date);
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testdivName = getNameMst(db2, "NAME1", "1".equals(_applicantdiv) ? "L024" : "L004", _testdiv);
            _nameMstL007 = getNameMstL007(db2);
        }
        
        private String getDateStr(final DB2UDB db2, final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(db2, date);
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
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
        
        public String formatDateMarkDot(final String date) {
            if (null != date) {
                final DateFormat df = new SimpleDateFormat("yyyy-mm-dd");
                try {
                    final Date d = java.sql.Date.valueOf(date);
                    final Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    int nen = -1;
                    final int tuki = cal.get(Calendar.MONTH) + 1;
                    final int hi = cal.get(Calendar.DAY_OF_MONTH);
                    
                    String mark = " ";
                    for (final Iterator it = _nameMstL007.iterator(); it.hasNext();) {
                        final Map m = (Map) it.next();
                        final String namespare2 = (String) m.get("NAMESPARE2");
                        if (null != namespare2) {
                            final Calendar dcal = Calendar.getInstance();
                            dcal.setTime(df.parse(namespare2.replace('/', '-')));
                            if (dcal.before(cal)) {
                                mark = StringUtils.defaultString((String) m.get("ABBV1"), " ");
                                nen = cal.get(Calendar.YEAR) - dcal.get(Calendar.YEAR) + 1;
                                break;
                            }
                        }
                    }
                    return mark + keta(nen, 2) + "." + keta(tuki, 2) + "." + keta(hi, 2);
                } catch (Exception e) {
                    log.error("format exception! date = " + date, e);
                }
            }
            return null;
        }
        
        public String keta(final int n, final int keta) {
            return StringUtils.repeat(" ", keta - String.valueOf(n).length()) + String.valueOf(n);
        }
        
        private List getNameMstL007(final DB2UDB db2) {
            final List list = new ArrayList();
            final String sql = " SELECT * FROM NAME_MST WHERE NAMECD1 = 'L007' ORDER BY NAMECD2 DESC ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnName(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    list.add(m);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
    }
}

// eof

