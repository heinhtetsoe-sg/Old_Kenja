package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 学校教育システム 賢者 [入試管理]
 **/

public class KNJL340F {

    private static final Log log = LogFactory.getLog(KNJL340F.class);

    private boolean _hasData;

    private Param _param;
    private final DecimalFormat df = new DecimalFormat(",###");

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        log.fatal("$Revision: 70826 $ $Date: 2019-11-22 14:36:45 +0900 (金, 22 11 2019) $"); // CVSキーワードの取り扱いに注意

        Vrw32alp svf = null; // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; // Databaseクラスを継承したクラス

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        PrintWriter outstrm = new PrintWriter(response.getOutputStream());
        try {
            KNJServletUtils.debugParam(request, log);
            _param = new Param(db2, request);

            final List dataList = MoneyDat.getMoneyDatList(db2, _param);

            if ("csv".equals(_param._cmd)) {
                final String filename = getTitle() + ".csv";
                CsvUtils.outputLines(log, response, filename, getCsvOutputLine(db2, dataList));
            } else {
                response.setContentType("application/pdf");

                svf = new Vrw32alp();
                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                printMain(db2, svf, dataList);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if ("csv".equals(_param._cmd)) {
            } else {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
                svf.VrQuit();
                outstrm.close(); // ストリームを閉じる
            }

            // 終了処理
            db2.commit();
            db2.close(); // DBを閉じる
        }
    }

    private List getCsvOutputLine(final DB2UDB db2, final List dataList) {

        final List lines = new ArrayList();

        lines.add(Arrays.asList(new String[] {getTitle(), "", "", "", "", "", "", "", KNJ_EditDate.h_format_JP(db2, _param._loginDate) + _param._jifun}));
        String[] header = {};
        if ("1".equals(_param._output)) {
            if ("1".equals(_param._applicantdiv)) {
                header = new String[] {"No.", "受領日", "ID", "ふりがな", "帰国\u2460", "帰国\u2461", "帰国\u2462", "受験番号\u2460", "受験番号\u2461", "受験番号\u2462", "受験番号\u2463", "受験番号\u2464", "受験番号\u2465", "受験番号\u2466", "振込", "現金", "備考"};
            } else if ("2".equals(_param._applicantdiv)) {
                header = new String[] {"No.", "受領日", "受験番号", "ふりがな", "氏　名", "振込", "現金", "備考"};
            }
        } else if ("2".equals(_param._output) || "3".equals(_param._output)) {
            if ("1".equals(_param._applicantdiv)) {
                header = new String[] {"No.", "受領日", "ID", "ふりがな", "帰国\u2460", "帰国\u2461", "帰国\u2462", "受験番号\u2460", "受験番号\u2461", "受験番号\u2462", "受験番号\u2463", "受験番号\u2464", "受験番号\u2465", "受験番号\u2466", "振込", "現金", "入金額", "備考"};
            } else if ("2".equals(_param._applicantdiv)) {
                header = new String[] {"No.", "受領日", "受験番号", "ふりがな", "氏　名", "振込", "現金", "入金額", "備考"};
            }
        }

        lines.add(Arrays.asList(header)); // ヘッダー

        BigDecimal total1 = new BigDecimal(0);
        BigDecimal total2 = new BigDecimal(0);
        for (int j = 0; j < dataList.size(); j++) {
            final MoneyDat md = (MoneyDat) dataList.get(j);
            final List line = new ArrayList();
            lines.add(line);
            line.add(String.valueOf(j + 1)); // 番号
            if ("1".equals(_param._output)) {
                line.add(dateSlash(md._examPayChakDate)); // 着金日
            } else if ("2".equals(_param._output)) {
                line.add(dateSlash(md._entPayChakDate)); // 着金日
            } else if ("3".equals(_param._output)) {
                line.add(dateSlash(md._expPayChakDate)); // 着金日
            }
            line.add(md._examno); // ID
            line.add(md._nameKana);
            if ("2".equals(_param._applicantdiv)) {
                line.add(md._name);
            } else if ("1".equals(_param._applicantdiv)) {
                line.add(md._receptno6); // 受験番号
                line.add(md._receptno7); // 受験番号
                line.add(md._receptno15); // 受験番号（帰国③）
                line.add(md._receptno1); // 受験番号
                line.add(md._receptno16); // 受験番号
                line.add(md._receptno2); // 受験番号
                line.add(md._receptno3); // 受験番号
                line.add(md._receptno5); // 受験番号
                line.add(md._receptno17); // 受験番号
                line.add(null); // 受験番号
            }
            if ("1".equals(_param._output)) {
                line.add("1".equals(md._examPayDiv) ? "○" : ""); // 振込
                line.add("2".equals(md._examPayDiv) ? "○" : ""); // 現金
                if (NumberUtils.isNumber(md._examPayMoney)) {
                    if ("1".equals(md._examPayDiv)) {
                        total1 = total1.add(new BigDecimal(1));
                    } else if ("2".equals(md._examPayDiv)) {
                        total2 = total2.add(new BigDecimal(1));
                    }
                }
            } else if ("2".equals(_param._output)) {
                line.add("1".equals(md._entPayDiv) ? "○" : ""); // 振込
                line.add("2".equals(md._entPayDiv) ? "○" : ""); // 現金
                line.add("\"" + (null != md._entPayMoney ? df.format(new BigDecimal(md._entPayMoney)) : "") + "\""); // 入学金
                if (NumberUtils.isNumber(md._entPayMoney)) {
                    if ("1".equals(md._entPayDiv)) {
                        total1 = total1.add(new BigDecimal(md._entPayMoney));
                    } else if ("2".equals(md._entPayDiv)) {
                        total2 = total2.add(new BigDecimal(md._entPayMoney));
                    }
                }
            } else if ("3".equals(_param._output)) {
                line.add("1".equals(md._expPayDiv) ? "○" : ""); // 振込
                line.add("2".equals(md._expPayDiv) ? "○" : ""); // 現金
                line.add("\"" + (null != md._expPayMoney ? df.format(new BigDecimal(md._expPayMoney)) : "") + "\""); // 入学金
                if (NumberUtils.isNumber(md._expPayMoney)) {
                    if ("1".equals(md._expPayDiv)) {
                        total1 = total1.add(new BigDecimal(md._expPayMoney));
                    } else if ("2".equals(md._expPayDiv)) {
                        total2 = total2.add(new BigDecimal(md._expPayMoney));
                    }
                }
            }
            line.add(null); // 備考
        }

        String totalCount = "";
        if ("1".equals(_param._output)) {
            totalCount = "\"振込" + df.format(total1) + "件、現金" + df.format(total2) + "件、計" + df.format(total1.add(total2)) + "件\"";
        } else if ("2".equals(_param._output) || "3".equals(_param._output)) {
            totalCount = "\"振込" + df.format(total1) + "円、現金" + df.format(total2) + "円、計" + df.format(total1.add(total2)) + "円\"";
        }
        if ("1".equals(_param._applicantdiv)) {
            lines.add(Arrays.asList(new String[] {"", "", "", "", "", "", "", "", "", "", "", "", totalCount})); // 合計カウント
        } else if ("2".equals(_param._applicantdiv)) {
            lines.add(Arrays.asList(new String[] {"", "", "", "", "", "",                     totalCount})); // 合計カウント
        }

        _hasData = true;
        return lines;
    }

    private String getTitle() {
        String title = "";
        if ("1".equals(_param._output)) {
            title = "検定料入金明細";
        } else if ("2".equals(_param._output) || "3".equals(_param._output)) {
            if ("2".equals(_param._output)) {
                title = "入学金入金明細";
            } else if ("3".equals(_param._output)) {
                title = "諸費入金明細";
            }
        }
        title = KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度 " + StringUtils.defaultString(_param._applicantdivname) + "入試 " + title;
        return title;
    }

    /**
     * 文字桁数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final List dataListAll) {
        final int maxLine = 50;
        final List pageList = getPageList(dataListAll, maxLine);
        String form = "";
        if ("1".equals(_param._output)) {
            if ("1".equals(_param._applicantdiv)) {
                form = "KNJL340F_1J.frm";
            } else if ("2".equals(_param._applicantdiv)) {
                form = "KNJL340F_1H.frm";
            }
        } else if ("2".equals(_param._output) || "3".equals(_param._output)) {
            if ("1".equals(_param._applicantdiv)) {
                form = "KNJL340F_2J.frm";
            } else if ("2".equals(_param._applicantdiv)) {
                form = "KNJL340F_2H.frm";
            }
        }
        svf.VrSetForm(form, 1);

        BigDecimal total1 = new BigDecimal(0);
        BigDecimal total2 = new BigDecimal(0);
        for (int pi = 0; pi < pageList.size(); pi++) {
            svf.VrsOut("PAGE1", String.valueOf(pi + 1)); // ページ
            svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // ページ
            svf.VrsOut("TITLE", getTitle()); //
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate) + _param._jifun); // 日付

            final List dataList = (List) pageList.get(pi);
            for (int j = 0; j < dataList.size(); j++) {
                final int line = j + 1;
                final MoneyDat md = (MoneyDat) dataList.get(j);
                svf.VrsOutn("NO", line, String.valueOf(pi * maxLine + line)); // 番号
                if ("1".equals(_param._output)) {
                    svf.VrsOutn("MONEY_DATE", line, dateSlash(md._examPayChakDate)); // 着金日
                } else if ("2".equals(_param._output)) {
                    svf.VrsOutn("MONEY_DATE", line, dateSlash(md._entPayChakDate)); // 着金日
                } else if ("3".equals(_param._output)) {
                    svf.VrsOutn("MONEY_DATE", line, dateSlash(md._expPayChakDate)); // 着金日
                }
                svf.VrsOutn("ID", line, md._examno); // ID
                final int ketaKana = getMS932ByteLength(md._nameKana);
                svf.VrsOutn(ketaKana <= 20 ? "KANA1" : ketaKana <= 30 ? "KANA2" : "KANA3", line, md._nameKana);
                if ("2".equals(_param._applicantdiv)) {
                    final int ketaName = getMS932ByteLength(md._name);
                    svf.VrsOutn(ketaName <= 20 ? "NAME1" : ketaName <= 30 ? "NAME2" : "NAME3", line, md._name);
                } else if ("1".equals(_param._applicantdiv)) {
                    svf.VrsOutn("EXAM_NO1", line, md._receptno6); // 受験番号(帰国①)
                    svf.VrsOutn("EXAM_NO2", line, md._receptno7); // 受験番号(帰国②)
                    svf.VrsOutn("EXAM_NO3", line, md._receptno15); // 受験番号(帰国③)
                    svf.VrsOutn("EXAM_NO4", line, md._receptno1); // 受験番号①
                    svf.VrsOutn("EXAM_NO5", line, md._receptno16); // 受験番号②
                    svf.VrsOutn("EXAM_NO6", line, md._receptno2); // 受験番号③
                    svf.VrsOutn("EXAM_NO7", line, md._receptno3); // 受験番号④
                    svf.VrsOutn("EXAM_NO8", line, md._receptno5); // 受験番号⑤
                    svf.VrsOutn("EXAM_NO9", line, md._receptno17); // 受験番号⑥
                    // svf.VrsOutn("EXAM_NO9", line, null); // 受験番号⑥
                }
                if ("1".equals(_param._output)) {
                    svf.VrsOutn("TRANSFER", line, "1".equals(md._examPayDiv) ? "○" : ""); // 振込
                    svf.VrsOutn("CASH", line, "2".equals(md._examPayDiv) ? "○" : ""); // 現金
                    if (NumberUtils.isNumber(md._examPayMoney)) {
                        if ("1".equals(md._examPayDiv)) {
                            total1 = total1.add(new BigDecimal(1));
                        } else if ("2".equals(md._examPayDiv)) {
                            total2 = total2.add(new BigDecimal(1));
                        }
                    }
                } else if ("2".equals(_param._output)) {
                    svf.VrsOutn("TRANSFER", line, "1".equals(md._entPayDiv) ? "○" : ""); // 振込
                    svf.VrsOutn("CASH", line, "2".equals(md._entPayDiv) ? "○" : ""); // 現金
                    svf.VrsOutn("ENT_MONEY", line, md._entPayMoney); // 入学金
                    if (NumberUtils.isNumber(md._entPayMoney)) {
                        if ("1".equals(md._entPayDiv)) {
                            total1 = total1.add(new BigDecimal(md._entPayMoney));
                        } else if ("2".equals(md._entPayDiv)) {
                            total2 = total2.add(new BigDecimal(md._entPayMoney));
                        }
                    }
                } else if ("3".equals(_param._output)) {
                    svf.VrsOutn("TRANSFER", line, "1".equals(md._expPayDiv) ? "○" : ""); // 振込
                    svf.VrsOutn("CASH", line, "2".equals(md._expPayDiv) ? "○" : ""); // 現金
                    svf.VrsOutn("ENT_MONEY", line, md._expPayMoney); // 入学金
                    if (NumberUtils.isNumber(md._expPayMoney)) {
                        if ("1".equals(md._expPayDiv)) {
                            total1 = total1.add(new BigDecimal(md._expPayMoney));
                        } else if ("2".equals(md._expPayDiv)) {
                            total2 = total2.add(new BigDecimal(md._expPayMoney));
                        }
                    }
                }
                // svf.VrsOutn("REMARK", line, null); // 備考
            }

            if (pageList.size() - 1 == pi) {
                final DecimalFormat df = new DecimalFormat(",###");
                String totalCount = "";
                if ("1".equals(_param._output)) {
                    totalCount = "振込" + df.format(total1) + "件、現金" + df.format(total2) + "件、計" + df.format(total1.add(total2)) + "件";
                } else if ("2".equals(_param._output) || "3".equals(_param._output)) {
                    totalCount = "振込" + df.format(total1) + "円、現金" + df.format(total2) + "円、計" + df.format(total1.add(total2)) + "円";
                }
                svf.VrsOut("TOTAL_COUNT", totalCount); // 合計カウント
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private String dateSlash(String date) {
        if (null != date) {
            return KNJ_EditDate.h_format_JP_MD(date);
        }
        return date;
    }

    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static class MoneyDat {
        final String _examno;
        final String _examPayChakDate;
        final String _entPayChakDate;
        final String _expPayChakDate;
        final String _examPayDiv;
        final String _entPayDiv;
        final String _expPayDiv;
        final String _examPayMoney;
        final String _entPayMoney;
        final String _expPayMoney;
        final String _name;
        final String _nameKana;
        final String _receptno1;
        final String _receptno2;
        final String _receptno3;
        final String _receptno4;
        final String _receptno5;
        final String _receptno6;
        final String _receptno7;
        final String _receptno15;
        final String _receptno16;
        final String _receptno17;

        MoneyDat(
                final String examno,
                final String examPayChakDate,
                final String entPayChakDate,
                final String expPayChakDate,
                final String examPayDiv,
                final String entPayDiv,
                final String expPayDiv,
                final String examPayMoney,
                final String entPayMoney,
                final String expPayMoney,
                final String name,
                final String nameKana,
                final String receptno1,
                final String receptno2,
                final String receptno3,
                final String receptno4,
                final String receptno5,
                final String receptno6,
                final String receptno7,
                final String receptno15,
                final String receptno16,
                final String receptno17
                ) {
            _examno = examno;
            _examPayChakDate = examPayChakDate;
            _entPayChakDate = entPayChakDate;
            _expPayChakDate = expPayChakDate;
            _examPayDiv = examPayDiv;
            _entPayDiv = entPayDiv;
            _expPayDiv = expPayDiv;
            _examPayMoney = examPayMoney;
            _entPayMoney = entPayMoney;
            _expPayMoney = expPayMoney;
            _name = name;
            _nameKana = nameKana;
            _receptno1 = receptno1;
            _receptno2 = receptno2;
            _receptno3 = receptno3;
            _receptno4 = receptno4;
            _receptno5 = receptno5;
            _receptno6 = receptno6;
            _receptno7 = receptno7;
            _receptno15 = receptno15;
            _receptno16 = receptno16;
            _receptno17 = receptno17;
        }

        public static List getMoneyDatList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");
                    final String examPayChakDate = rs.getString("EXAM_PAY_CHAK_DATE");
                    final String entPayChakDate = rs.getString("ENT_PAY_CHAK_DATE");
                    final String expPayChakDate = rs.getString("EXP_PAY_CHAK_DATE");
                    final String examPayDiv = rs.getString("EXAM_PAY_DIV");
                    final String entPayDiv = rs.getString("ENT_PAY_DIV");
                    final String expPayDiv = rs.getString("EXP_PAY_DIV");
                    final String examPayMoney = rs.getString("EXAM_PAY_MONEY");
                    final String entPayMoney = rs.getString("ENT_PAY_MONEY");
                    final String expPayMoney = rs.getString("EXP_PAY_MONEY");
                    final String name = rs.getString("NAME");
                    final String nameKana = rs.getString("NAME_KANA");
                    final String receptno1 = rs.getString("RECEPTNO1");
                    final String receptno2 = rs.getString("RECEPTNO2");
                    final String receptno3 = rs.getString("RECEPTNO3");
                    final String receptno4 = rs.getString("RECEPTNO4");
                    final String receptno5 = rs.getString("RECEPTNO5");
                    final String receptno6 = rs.getString("RECEPTNO6");
                    final String receptno7 = rs.getString("RECEPTNO7");
                    final String receptno15 = rs.getString("RECEPTNO15");
                    final String receptno16 = rs.getString("RECEPTNO16");
                    final String receptno17 = rs.getString("RECEPTNO17");
                    final MoneyDat moneydat = new MoneyDat(examno, examPayChakDate, entPayChakDate, expPayChakDate, examPayDiv, entPayDiv, expPayDiv, examPayMoney, entPayMoney, expPayMoney, name, nameKana,
                            receptno1, receptno2, receptno3, receptno4, receptno5, receptno6, receptno7, receptno15, receptno16, receptno17);
                    list.add(moneydat);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("  SELECT  ");
            stb.append("      EMD.EXAMNO ");
            stb.append("      , EMD.EXAM_PAY_CHAK_DATE ");
            stb.append("      , EMD.ENT_PAY_CHAK_DATE ");
            stb.append("      , EMD.EXP_PAY_CHAK_DATE ");
            stb.append("      , EMD.EXAM_PAY_DIV ");
            stb.append("      , EMD.ENT_PAY_DIV ");
            stb.append("      , EMD.EXP_PAY_DIV ");
            stb.append("      , EMD.EXAM_PAY_MONEY ");
            stb.append("      , EMD.ENT_PAY_MONEY ");
            stb.append("      , EMD.EXP_PAY_MONEY ");
            stb.append("      ,T2.NAME  ");
            stb.append("      ,T2.NAME_KANA  ");
            stb.append("      ,TD2.REMARK1 AS RECEPTNO1  ");
            stb.append("      ,TD2.REMARK2 AS RECEPTNO2  ");
            stb.append("      ,TD2.REMARK3 AS RECEPTNO3  ");
            stb.append("      ,TD2.REMARK4 AS RECEPTNO4  ");
            stb.append("      ,TD2.REMARK5 AS RECEPTNO5  ");
            stb.append("      ,TD2.REMARK6 AS RECEPTNO6  ");
            stb.append("      ,TD2.REMARK7 AS RECEPTNO7  ");
            stb.append("      ,TD2.REMARK15 AS RECEPTNO15  ");
            if ("1".equals(param._applicantdiv)) {
                stb.append("      ,TD2.REMARK16 AS RECEPTNO16  ");
                stb.append("      ,TD2.REMARK17 AS RECEPTNO17  ");
            } else {
                stb.append("      ,'' AS RECEPTNO16  ");
                stb.append("      ,'' AS RECEPTNO17  ");
            }
            stb.append("  FROM  ");
            stb.append("      ENTEXAM_MONEY_DAT EMD ");
            stb.append("      INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ON T2.ENTEXAMYEAR = EMD.ENTEXAMYEAR ");
            stb.append("          AND T2.EXAMNO = EMD.EXAMNO ");
            stb.append("      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT TD2 ON TD2.ENTEXAMYEAR = T2.ENTEXAMYEAR  ");
            stb.append("                                     AND TD2.EXAMNO       = T2.EXAMNO  ");
            stb.append("                                     AND TD2.SEQ          = '012'  ");
            stb.append("  WHERE  ");
            stb.append("      EMD.ENTEXAMYEAR = '" + param._entexamyear + "'  ");
            stb.append("      AND EMD.APPLICANTDIV = '" + param._applicantdiv + "'  ");
            if ("1".equals(param._output)) {
                stb.append("      AND EMD.EXAM_PAY_CHAK_DATE IS NOT NULL ");
            } else if ("2".equals(param._output)) {
                stb.append("      AND EMD.ENT_PAY_CHAK_DATE IS NOT NULL ");
            } else if ("3".equals(param._output)) {
                stb.append("      AND EMD.EXP_PAY_CHAK_DATE IS NOT NULL ");
            }
            stb.append("  ORDER BY ");
            if ("1".equals(param._output)) {
                stb.append("      EMD.EXAM_PAY_CHAK_DATE, ");
            } else if ("2".equals(param._output)) {
                stb.append("      EMD.ENT_PAY_CHAK_DATE, ");
            } else if ("3".equals(param._output)) {
                stb.append("      EMD.EXP_PAY_CHAK_DATE, ");
            }
            stb.append("      EXAMNO ");
            return stb.toString();
        }
    }

    private static class Param {
        final String _entexamyear;
        final String _loginDate;
        final String _applicantdiv;
        final String _output; // 1: 検定料（受験料）入金明細表 2: 入学金入金明細表 3: 諸費入金明細表
        final String _cmd;

        final String _applicantdivname;
        final String _jifun;
        private boolean _seirekiFlg;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _output = request.getParameter("OUTPUT");
            _cmd = request.getParameter("cmd");

            _seirekiFlg = getSeirekiFlg(db2);
            _applicantdivname = getApplicantdivName(db2);

            final Calendar cal = Calendar.getInstance();
            _jifun = String.valueOf(cal.get(Calendar.HOUR)) + "時" + String.valueOf(cal.get(Calendar.MINUTE)) + "分";
        }

        private String getApplicantdivName(DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantdiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                    schoolName = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }

        /* 西暦表示にするのかのフラグ */
        private boolean getSeirekiFlg(final DB2UDB db2) {
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("NAME1").equals("2"))
                        seirekiFlg = true; // 西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return seirekiFlg;
        }

        private String gethiduke(final DB2UDB db2, final String inputDate) {
            // 西暦か和暦はフラグで判断
            String date;
            if (null != inputDate) {
                if (_seirekiFlg) {
                    date = inputDate.toString().substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(inputDate);
                } else {
                    date = KNJ_EditDate.h_format_JP(db2, inputDate);
                }
                return date;
            }
            return null;
        }

    }
}// クラスの括り
