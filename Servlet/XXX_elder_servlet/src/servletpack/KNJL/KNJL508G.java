/*
 * $Id: ba03d99babd8d37a641fcc019f5f756060e837e1 $
 *
 * 作成日: 2019/01/08
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

public class KNJL508G {

    private static final Log log = LogFactory.getLog(KNJL508G.class);

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
        svf.VrSetForm("KNJL508G.frm", 1);
        final Map uchiwakeMap = getUchiwake(db2);
        final List printList = getList(db2);
        final int maxLine = 3;
        int printLine = 1;

        NumberFormat nfNum = NumberFormat.getNumberInstance();    //カンマ区切り形式

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (printLine > maxLine) {
                svf.VrEndPage();
                printLine = 1;
            }
            final int namelen = KNJ_EditEdit.getMS932ByteLength(printData._name);
            String totalval = "*******";
            final int toalMoney = Integer.parseInt((String) uchiwakeMap.get(printData._examNo));
            if (0 != toalMoney) {
                totalval = String.valueOf(toalMoney);
            }
            final String totalval2 = (!"*******".equals(totalval)) ? nfNum.format(Integer.valueOf(totalval)) : "*******";
            //出力表1
            svf.VrsOutn("EXAM_NO1", printLine, printData._examNo);  //番号
            final String n1field = namelen > 30 ? "_2" : "_1";
            svf.VrsOutn("NAME1" + n1field, printLine, printData._name);  //氏名
            svf.VrsOutn("PRICE1", printLine, totalval2);  //合計金額
            //出力表2
            svf.VrsOutn("EXAM_NO2", printLine, printData._examNo);  //番号
            final String n2field = namelen > 30 ? "_3" : (namelen > 16 ? "_2" : "_1");
            svf.VrsOutn("NAME2" + n2field, printLine, printData._name);  //氏名
            svf.VrsOutn("PRICE2", printLine, totalval);  //合計金額
            //出力表3
            svf.VrsOutn("EXAM_NO3", printLine, printData._examNo);  //番号
            final String n3field = namelen > 30 ? "_3" : (namelen > 20 ? "_2" : "_1");
            svf.VrsOutn("NAME3" + n3field, printLine, printData._name);  //氏名
            svf.VrsOutn("PRICE3", printLine, totalval);  //合計金額

            //内訳
            if (0 != toalMoney) {
                for (int i = 1; i < uchiwakeMap.size(); i++) {
                    svf.VrsOutn("MESSAGE" + i, printLine, (String) uchiwakeMap.get(printData._examNo + "_" + i));
                }
            }

            printLine++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");

                final PrintData printData = new PrintData(examNo, name);
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
        stb.append("   T1.EXAMNO, ");
        stb.append("   T1.NAME ");
        stb.append(" FROM ");
        stb.append("   V_ENTEXAM_APPLICANTBASE_DAT T1 ");
        if (!"3".equals(_param._passDiv)) { // 3:志願者全員
            stb.append("   INNER JOIN NAME_MST L013 ");
            stb.append("     ON L013.NAMECD1 = 'L013' ");
            stb.append("    AND L013.NAMECD2 = T1.JUDGEMENT ");
            stb.append("    AND L013.NAMESPARE1 = '1' ");  //合格者のみ対象
        }
        stb.append(" WHERE ");
        stb.append("       T1.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append("   AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("   AND T1.TESTDIV      = '" + _param._testDiv + "' ");
        if (!"3".equals(_param._passDiv)) { // 3:志願者全員
            stb.append("   AND T1.SUC_COURSECODE IS NOT NULL ");
        }
        if ("2".equals(_param._passDiv)) {
        	if (!"".equals(_param._passExamno)) {
        	    stb.append("   AND T1.EXAMNO >= '" + _param._passExamno + "' ");
        	}
        	if (!"".equals(_param._passExamnoTo)) {
        	    stb.append("   AND T1.EXAMNO <= '" + _param._passExamnoTo + "' ");
        	}
        }
        stb.append(" ORDER BY ");
        stb.append("   T1.EXAMNO ");

        return stb.toString();
    }

    private Map getUchiwake(final DB2UDB db2) {
        final Map retMap = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getUchiwakeSql();
            log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int row = 1;

            NumberFormat nfNum = NumberFormat.getNumberInstance();    //カンマ区切り形式

            while (rs.next()) {
                final String examNo      = rs.getString("EXAMNO");
                String himokuName  = rs.getString("HIMOKU_NAME");
                final int seikyuMoney   = Integer.parseInt(rs.getString("SEIKYU_MONEY"));
                final int genmenMoney   = Integer.parseInt(rs.getString("GENMEN_MONEY"));
                final boolean isNyugakukinItemCd = null != rs.getString("ITEM_CD") && rs.getString("ITEM_CD").endsWith("1");
                final int shitakuMoney  = Integer.parseInt(rs.getString("SHITAKU_MONEY"));

                int uchiwakeMoney = seikyuMoney - genmenMoney;
                if (isNyugakukinItemCd && shitakuMoney > 0) {
                	uchiwakeMoney -= shitakuMoney;
                	if (0 > uchiwakeMoney) {
                		uchiwakeMoney = 0;
                	}
                }

                //内訳セット(「受験番号 + "_" + 行番号」をキーにセットする。)
                if (null == retMap.get(examNo)) {
                    row = 1;
                    retMap.put(examNo + "_" + String.valueOf(row), "内訳"); // 1行目
                    row++;
                    String markStr = "";
                    if (isNyugakukinItemCd && shitakuMoney > 0) {
                    	himokuName = "";
                    	markStr = "入学支度利用￥" + nfNum.format(shitakuMoney);
                    } else if (uchiwakeMoney == 0) { // 請求 - 減免が0の時
                        markStr = "免除";
                    } else {
                        markStr = "￥" + nfNum.format(uchiwakeMoney);
                    }
                    retMap.put(examNo + "_" + String.valueOf(row), himokuName + markStr); // 2行目
                    row++;
                } else {
                    String markStr = "";
                    if (isNyugakukinItemCd && shitakuMoney > 0) {
                    	himokuName = "";
                    	markStr = "入学支度利用￥" + nfNum.format(shitakuMoney);
                    } else if (uchiwakeMoney == 0) { // 請求 - 減免が0の時
                        markStr = "免除";
                    } else {
                        markStr = "￥" + nfNum.format(uchiwakeMoney);
                    }
                    retMap.put(examNo + "_" + String.valueOf(row), himokuName + markStr); // 3行目移行(現状は3まで)
                    row++;
                }

                //合計金額(「受験番号」をキーにセットする。)
                final int mapMoney = null == retMap.get(examNo) ? 0: Integer.parseInt((String)retMap.get(examNo));
                retMap.put(examNo, String.valueOf(mapMoney + uchiwakeMoney));
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getUchiwakeSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ");
        //請求金額（費目毎）
        stb.append(" SEIKYU AS ( ");
        stb.append("     SELECT ");
        stb.append("         BASE.EXAMNO, ");
        stb.append("         PAY_ITEM.ITEM_CD, ");
        stb.append("         PAY_ITEM.REMARK6, ");
        stb.append("         value(PAY_ITEM.ITEM_MONEY, 0) AS SEIKYU_MONEY ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S001 ON S001.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("                                                        AND S001.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("                                                        AND S001.EXAMNO       = BASE.EXAMNO ");
        stb.append("                                                        AND S001.SEQ          = '001' ");
        stb.append("         LEFT JOIN NAME_MST L036_SEIKYU ON L036_SEIKYU.NAMECD1 = 'L036' ");
        if ("3".equals(_param._passDiv)) {
            stb.append("                                       AND L036_SEIKYU.ABBV2   = SUBSTR(S001.REMARK10, 3, 1) ");
        } else {
            stb.append("                                       AND L036_SEIKYU.ABBV2   = SUBSTR(BASE.SUC_COURSECODE, 3, 1) ");
        }
        stb.append("         LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PAY_SEIKYU ON BASE.ENTEXAMYEAR    = PAY_SEIKYU.ENTEXAMYEAR ");
        stb.append("                                                           AND BASE.APPLICANTDIV   = PAY_SEIKYU.APPLICANTDIV ");
        stb.append("                                                           AND PAY_SEIKYU.DIV      = '0' ");
        stb.append("                                                           AND PAY_SEIKYU.KIND_CD  = '2' ");
        stb.append("                                                           AND L036_SEIKYU.NAMECD2 = PAY_SEIKYU.EXEMPTION_CD ");
        stb.append("         LEFT JOIN ENTEXAM_PAYMENT_ITEM_MST PAY_ITEM ON PAY_SEIKYU.ENTEXAMYEAR  = PAY_ITEM.ENTEXAMYEAR ");
        stb.append("                                                    AND PAY_SEIKYU.APPLICANTDIV = PAY_ITEM.APPLICANTDIV ");
        stb.append("                                                    AND PAY_ITEM.DIV            = '0' ");
        stb.append("                                                    AND PAY_SEIKYU.ITEM_CD      = PAY_ITEM.ITEM_CD ");
        stb.append("     WHERE ");
        stb.append("             BASE.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append("         AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("         AND BASE.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append("         AND PAY_ITEM.REMARK7  is null "); // 授業料フラグがあるものは、含めない
        //特待生減免（合算）
        stb.append(" ), GENMEN_TOTAL AS ( ");
        stb.append("     SELECT ");
        stb.append("         BASE.EXAMNO, ");
        stb.append("         SUM(VALUE(PAY_GENMEN.EXEMPTION_MONEY, 0)) AS GENMEN_TOTAL_MONEY ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S001 ON S001.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("                                                        AND S001.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("                                                        AND S001.EXAMNO       = BASE.EXAMNO ");
        stb.append("                                                        AND S001.SEQ          = '001' ");
        stb.append("         LEFT JOIN NAME_MST L036_SEIKYU ON L036_SEIKYU.NAMECD1 = 'L036' ");
        if ("3".equals(_param._passDiv)) {
            stb.append("                                       AND L036_SEIKYU.ABBV2   = SUBSTR(S001.REMARK10, 3, 1) ");
        } else {
            stb.append("                                       AND SUBSTR(BASE.SUC_COURSECODE, 3, 1) = L036_SEIKYU.ABBV2 ");
        }
        stb.append("         LEFT JOIN NAME_MST L036_GENMEN ON L036_GENMEN.NAMECD1 = 'L036' ");
        stb.append("                                       AND BASE.JUDGE_KIND     = L036_GENMEN.ABBV3 ");
        stb.append("         LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PAY_SEIKYU ON BASE.ENTEXAMYEAR    = PAY_SEIKYU.ENTEXAMYEAR ");
        stb.append("                                                           AND BASE.APPLICANTDIV   = PAY_SEIKYU.APPLICANTDIV ");
        stb.append("                                                           AND PAY_SEIKYU.DIV      = '0' ");
        stb.append("                                                           AND PAY_SEIKYU.KIND_CD  = '2' ");
        stb.append("                                                           AND L036_SEIKYU.NAMECD2 = PAY_SEIKYU.EXEMPTION_CD ");
        stb.append("         LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PAY_GENMEN ON BASE.ENTEXAMYEAR    = PAY_GENMEN.ENTEXAMYEAR ");
        stb.append("                                                           AND BASE.APPLICANTDIV   = PAY_GENMEN.APPLICANTDIV ");
        stb.append("                                                           AND PAY_GENMEN.DIV      = '0' ");
        stb.append("                                                           AND PAY_GENMEN.KIND_CD  = '1' ");
        stb.append("                                                           AND L036_GENMEN.NAMECD2 = PAY_GENMEN.EXEMPTION_CD ");
        stb.append("                                                           AND PAY_GENMEN.ITEM_CD  = PAY_SEIKYU.ITEM_CD ");
        stb.append("         INNER JOIN ENTEXAM_PAYMENT_ITEM_MST PAY_ITEM ON PAY_SEIKYU.ENTEXAMYEAR  = PAY_ITEM.ENTEXAMYEAR ");
        stb.append("                                                     AND PAY_SEIKYU.APPLICANTDIV = PAY_ITEM.APPLICANTDIV ");
        stb.append("                                                     AND PAY_ITEM.DIV            = '0' ");
        stb.append("                                                     AND PAY_SEIKYU.ITEM_CD      = PAY_ITEM.ITEM_CD ");
        stb.append("                                                     AND VALUE(PAY_ITEM.REMARK7, '0')  <> '1' ");
        stb.append("     WHERE ");
        stb.append("         BASE.ENTEXAMYEAR      = '" + _param._entexamyear + "' ");
        stb.append("         AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("         AND BASE.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append("     GROUP BY ");
        stb.append("         BASE.EXAMNO ");
        //卒・在籍生減免（合算）
        stb.append(" ), KYOUDAI_GENMEN_TOTAL AS ( ");
        stb.append("     SELECT ");
        stb.append("         BASE.EXAMNO, ");
        stb.append("         SUM(VALUE(PAY_GENMEN.EXEMPTION_MONEY, 0)) AS KYOUDAI_TOTAL_MONEY ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S001 ON S001.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("                                                        AND S001.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("                                                        AND S001.EXAMNO       = BASE.EXAMNO ");
        stb.append("                                                        AND S001.SEQ          = '001' ");
        stb.append("         LEFT JOIN NAME_MST L036_SEIKYU ON L036_SEIKYU.NAMECD1 = 'L036' ");
        if ("3".equals(_param._passDiv)) {
            stb.append("                                       AND L036_SEIKYU.ABBV2   = SUBSTR(S001.REMARK10, 3, 1) ");
        } else {
            stb.append("                                       AND SUBSTR(BASE.SUC_COURSECODE, 3, 1) = L036_SEIKYU.ABBV2 ");
        }
        stb.append("         LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PAY_SEIKYU ON BASE.ENTEXAMYEAR    = PAY_SEIKYU.ENTEXAMYEAR ");
        stb.append("                                                           AND BASE.APPLICANTDIV   = PAY_SEIKYU.APPLICANTDIV ");
        stb.append("                                                           AND PAY_SEIKYU.DIV      = '0' ");
        stb.append("                                                           AND PAY_SEIKYU.KIND_CD  = '2' ");
        stb.append("                                                           AND L036_SEIKYU.NAMECD2 = PAY_SEIKYU.EXEMPTION_CD ");
        stb.append("         INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD018 ON BASE.ENTEXAMYEAR  = BD018.ENTEXAMYEAR ");
        stb.append("                                                          AND BASE.APPLICANTDIV = BD018.APPLICANTDIV ");
        stb.append("                                                          AND BASE.EXAMNO       = BD018.EXAMNO ");
        stb.append("                                                          AND BD018.SEQ         = '018' ");
        stb.append("                                                          AND BD018.REMARK1 IS NOT NULL ");
        stb.append("         LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PAY_GENMEN ON BASE.ENTEXAMYEAR   = PAY_GENMEN.ENTEXAMYEAR ");
        stb.append("                                                           AND BASE.APPLICANTDIV  = PAY_GENMEN.APPLICANTDIV ");
        stb.append("                                                           AND PAY_GENMEN.DIV     = '0' ");
        stb.append("                                                           AND PAY_GENMEN.KIND_CD = '1' ");
        stb.append("                                                           AND PAY_GENMEN.ITEM_CD = PAY_SEIKYU.ITEM_CD ");
        stb.append("         INNER JOIN ENTEXAM_PAYMENT_ITEM_MST PAY_ITEM ON PAY_SEIKYU.ENTEXAMYEAR  = PAY_ITEM.ENTEXAMYEAR ");
        stb.append("                                                     AND PAY_SEIKYU.APPLICANTDIV = PAY_ITEM.APPLICANTDIV ");
        stb.append("                                                     AND PAY_ITEM.DIV            = '0' ");
        stb.append("                                                     AND PAY_SEIKYU.ITEM_CD      = PAY_ITEM.ITEM_CD ");
        stb.append("                                                     AND VALUE(PAY_ITEM.REMARK7, '0')  <> '1' ");
        stb.append("     WHERE ");
        stb.append("            BASE.ENTEXAMYEAR   = '" + _param._entexamyear + "' ");
        stb.append("         AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("         AND BASE.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append("         AND PAY_GENMEN.EXEMPTION_CD IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L036' AND NAMESPARE3 = '1') ");
        stb.append("     GROUP BY ");
        stb.append("         BASE.EXAMNO ");
        //特待生減免　卒・在籍生減免（費目毎）
        stb.append(" ), GENMEN AS ( ");
        stb.append("     SELECT ");
        stb.append("         BASE.EXAMNO, ");
        stb.append("         PAY_SEIKYU.ITEM_CD, ");
        stb.append("         case ");
        stb.append("             when value(G_TOTAL.GENMEN_TOTAL_MONEY, 0) >= value(K_TOTAL.KYOUDAI_TOTAL_MONEY, 0) then value(PAY_GENMEN.EXEMPTION_MONEY, 0) ");
        stb.append("             else value(PAY_KYODAI.EXEMPTION_MONEY, 0) ");
        stb.append("         end as GENMEN_MONEY ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S001 ON S001.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("                                                        AND S001.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("                                                        AND S001.EXAMNO       = BASE.EXAMNO ");
        stb.append("                                                        AND S001.SEQ          = '001' ");
        stb.append("         LEFT JOIN NAME_MST L036_SEIKYU ON L036_SEIKYU.NAMECD1 = 'L036' ");
        if ("3".equals(_param._passDiv)) {
            stb.append("                                       AND L036_SEIKYU.ABBV2   = SUBSTR(S001.REMARK10, 3, 1) ");
        } else {
            stb.append("                                       AND L036_SEIKYU.ABBV2   = SUBSTR(BASE.SUC_COURSECODE, 3, 1) ");
        }
        stb.append("         LEFT JOIN NAME_MST L036_GENMEN ON L036_GENMEN.NAMECD1 = 'L036' ");
        stb.append("                                       AND L036_GENMEN.ABBV3   = BASE.JUDGE_KIND ");
        stb.append("         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S018 ON S018.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("                                                        AND S018.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("                                                        AND S018.EXAMNO       = BASE.EXAMNO ");
        stb.append("                                                        AND S018.SEQ          = '018' ");
        stb.append("         LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PAY_SEIKYU ON BASE.ENTEXAMYEAR    = PAY_SEIKYU.ENTEXAMYEAR ");
        stb.append("                                                           AND BASE.APPLICANTDIV   = PAY_SEIKYU.APPLICANTDIV ");
        stb.append("                                                           AND PAY_SEIKYU.DIV      = '0' ");
        stb.append("                                                           AND PAY_SEIKYU.KIND_CD  = '2' ");
        stb.append("                                                           AND L036_SEIKYU.NAMECD2 = PAY_SEIKYU.EXEMPTION_CD ");
        stb.append("         LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PAY_GENMEN ON BASE.ENTEXAMYEAR    = PAY_GENMEN.ENTEXAMYEAR ");
        stb.append("                                                           AND BASE.APPLICANTDIV   = PAY_GENMEN.APPLICANTDIV ");
        stb.append("                                                           AND PAY_GENMEN.DIV      = '0' ");
        stb.append("                                                           AND PAY_GENMEN.KIND_CD  = '1' ");
        stb.append("                                                           AND L036_GENMEN.NAMECD2 = PAY_GENMEN.EXEMPTION_CD ");
        stb.append("                                                           AND PAY_GENMEN.ITEM_CD  = PAY_SEIKYU.ITEM_CD ");
        stb.append("         LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PAY_KYODAI ON PAY_KYODAI.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("                                                           AND PAY_KYODAI.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("                                                           AND PAY_KYODAI.DIV          = '0' ");
        stb.append("                                                           AND PAY_KYODAI.KIND_CD      = '1' ");
        stb.append("                                                           AND PAY_KYODAI.EXEMPTION_CD = '" + _param._nameCd2GrdFlg + "' "); // 卒業・在学生
        stb.append("                                                           AND PAY_KYODAI.ITEM_CD      = PAY_SEIKYU.ITEM_CD ");
        stb.append("         LEFT JOIN ENTEXAM_PAYMENT_ITEM_MST PAY_ITEM ON PAY_SEIKYU.ENTEXAMYEAR  = PAY_ITEM.ENTEXAMYEAR ");
        stb.append("                                                    AND PAY_SEIKYU.APPLICANTDIV = PAY_ITEM.APPLICANTDIV ");
        stb.append("                                                    AND PAY_ITEM.DIV            = '0' ");
        stb.append("                                                    AND PAY_SEIKYU.ITEM_CD      = PAY_ITEM.ITEM_CD ");
        stb.append("         LEFT JOIN GENMEN_TOTAL         G_TOTAL ON BASE.EXAMNO  = G_TOTAL.EXAMNO ");
        stb.append("         LEFT JOIN KYOUDAI_GENMEN_TOTAL K_TOTAL ON BASE.EXAMNO  = K_TOTAL.EXAMNO ");
        stb.append("     WHERE ");
        stb.append("             BASE.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append("         AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("         AND BASE.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append("         AND PAY_ITEM.REMARK7  is null "); // 授業料フラグがあるものは、含めない
        stb.append(" ) ");
        //メイン
        stb.append(" SELECT ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     value(SEIKYU.REMARK6, '') as HIMOKU_NAME, ");
        stb.append("     value(SEIKYU.SEIKYU_MONEY, 0) as SEIKYU_MONEY, ");
        stb.append("     value(GENMEN.GENMEN_MONEY, 0) as GENMEN_MONEY, ");
        stb.append("     SEIKYU.ITEM_CD, ");
        stb.append("     value(SHITAKU.SHITAKU_TOTAL_ITEM_MONEY, 0) as SHITAKU_MONEY ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN SEIKYU ON BASE.EXAMNO    = SEIKYU.EXAMNO ");
        stb.append("     LEFT JOIN GENMEN ON BASE.EXAMNO    = GENMEN.EXAMNO ");
        stb.append("                     AND GENMEN.ITEM_CD = SEIKYU.ITEM_CD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S001 ON S001.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("                     AND S001.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("                     AND S001.EXAMNO       = BASE.EXAMNO ");
        stb.append("                     AND S001.SEQ          = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S021 ON S021.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("                     AND S021.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("                     AND S021.EXAMNO       = BASE.EXAMNO ");
        stb.append("                     AND S021.SEQ          = '021' ");
        stb.append("     LEFT JOIN (SELECT  ");
        stb.append("                    ENTEXAMYEAR  ");
        stb.append("                  , APPLICANTDIV  ");
        stb.append("                  , SUBSTR(ITEM_CD, 1, 1) AS COURSECD  ");
        stb.append("                  , DIV  ");
        stb.append("                  , SUM(ITEM_MONEY) AS SHITAKU_TOTAL_ITEM_MONEY  ");
        stb.append("                FROM ");
        stb.append("                        ENTEXAM_PAYMENT_ITEM_MST ");
        stb.append("                WHERE ");
        stb.append("                        DIV = '0' ");
        stb.append("                        AND REMARK5 = '1' ");
        stb.append("                GROUP BY ");
        stb.append("                    ENTEXAMYEAR  ");
        stb.append("                  , APPLICANTDIV  ");
        stb.append("                  , SUBSTR(ITEM_CD, 1, 1) ");
        stb.append("                  , DIV  ");
        stb.append("               )  SHITAKU ON BASE.ENTEXAMYEAR  = SHITAKU.ENTEXAMYEAR ");
        stb.append("                         AND BASE.APPLICANTDIV = SHITAKU.APPLICANTDIV ");
        if ("3".equals(_param._passDiv)) {
            stb.append("                     AND SHITAKU.COURSECD   = SUBSTR(S001.REMARK10, 3, 1) ");
        } else {
            stb.append("                     AND SHITAKU.COURSECD   = SUBSTR(BASE.SUC_COURSECODE, 3, 1) ");
        }
        stb.append("                         AND S021.REMARK2   = '1' ");
        stb.append(" WHERE ");
        stb.append("         BASE.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     SEIKYU.ITEM_CD ");

        return stb.toString();
    }

    private class PrintData {
    	final String _examNo;
    	final String _name;

        public PrintData(
        		final String examNo,
        		final String name
        ) {
        	_examNo = examNo;
            _name = name;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 73146 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _passDiv;
        private final String _passExamno;
        private final String _passExamnoTo;
        private final String _nameCd2GrdFlg;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _passDiv = request.getParameter("PASS_DIV");
            _passExamno = StringUtils.defaultString(request.getParameter("PASS_EXAMNO"));
            _passExamnoTo = StringUtils.defaultString(request.getParameter("PASS_EXAMNO_TO"));
            _nameCd2GrdFlg = getNameCd2(db2, _entexamyear);
        }

        private String getNameCd2(final DB2UDB db2, final String year) {
            String retNameCd2 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAMECD2 FROM V_NAME_MST WHERE YEAR = '" + year + "' AND NAMECD1 = 'L036' AND NAMESPARE3 = '1' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retNameCd2 = rs.getString("NAMECD2");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retNameCd2;
        }

    }
}

// eof
