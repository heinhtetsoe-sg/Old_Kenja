package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *    学校教育システム 賢者 [SATシステム] 返却作業用シール
 *
 **/

public class KNJL396Q {

    private static final Log log = LogFactory.getLog(KNJL396Q.class);
    
    private boolean _hasData;
    private Param _param;

    private static final String FROM_TO_MARK = "\uFF5E";

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        //print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //svf設定
        svf.VrInit();    //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

        //ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }
        
        try {
            log.fatal("$Revision: 62119 $ $Date: 2018-09-06 10:44:58 +0900 (木, 06 9 2018) $"); // CVSキーワードの取り扱いに注意
            KNJServletUtils.debugParam(request, log);
            _param = new Param(db2, request);

            //SVF出力
            for (int i = 0; i < _param._CHECK.size(); i++) {
                final int checkInt = Integer.parseInt((String) _param._CHECK.get(i));
                log.info(" where condition check = " + checkInt);
                printMain(db2, svf, checkInt); //帳票出力のメソッド
            }

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            //終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();       //DBを閉じる
            outstrm.close();   //ストリームを閉じる
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final int checkInt) {
        
        final int max = 3 * 6;
        final List dataListAll = getList(db2, sql(_param, checkInt));
        log.info(" data size = " + dataListAll.size());
        final List pageList = getPageList(null, dataListAll, max);
        final String form;
        if (checkInt == 23) {
            /*海外受験者シール*/
            form = "KNJL396Q_2.frm";
        } else if (checkInt == 24) {
            /*国内校内生シール*/
            form = "KNJL396Q_3.frm";
        } else {
            /*国内欠席者住所シール*/
            /*返却作業用条件での住所シール印刷*/
            form = "KNJL396Q.frm";
        }

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);
            
            svf.VrSetForm(form, 1);
            
            for (int i = 0; i < dataList.size(); i++) {
                final int col = (i + 1) % 3 == 0 ? 3 : (i + 1) % 3;
                final int row = 1 + i / 3;
                final Map data = (Map) dataList.get(i);

                if (checkInt == 23) {
                    /*海外受験者シール*/
                    svf.VrsOutn("SAT_NO" + col, row, getString(data, "SAT_NO"));
                    svf.VrsOutn("NAME" + col + "_1", row, StringUtils.defaultString(getString(data, "NAME1")) + "　様");
                    svf.VrsOutn("NAME" + col + "_2", row, getString(data, "PLACEAREA"));
                } else if (checkInt == 24) {
                    /*国内校内生シール*/
                    svf.VrsOutn("SAT_NO" + col, row, getString(data, "SAT_NO"));
                    svf.VrsOutn("NAME" + col + "_1", row, StringUtils.defaultString(getString(data, "NAME1")) + "　様");
                    svf.VrsOutn("IN_NO_NAME" + col, row, "校内生番号：");
                    svf.VrsOutn("IN_NO" + col, row, getString(data, "INSIDERNO"));
                    svf.VrsOutn("NAME" + col + "_2", row, getString(data, "GROUPNAME"));
                } else {
                    /*国内欠席者住所シール*/
                    /*返却作業用条件での住所シール印刷*/
                    svf.VrsOutn("ZIPCODE" + col, row, getString(data, "ZIPCODE"));
                    
                    final String addr1 = getString(data, "ADDR1");
                    final String addr2 = getString(data, "ADDR2");
                    if (getMS932Bytecount(addr1) > 50 || getMS932Bytecount(addr2) > 50) {
                        svf.VrsOutn("ADDRESS" + col + "_1_3", row, addr1);
                        svf.VrsOutn("ADDRESS" + col + "_2_3", row, addr2);
                    } else if (getMS932Bytecount(addr1) > 40 || getMS932Bytecount(addr2) > 40) {
                        svf.VrsOutn("ADDRESS" + col + "_1_2", row, addr1);
                        svf.VrsOutn("ADDRESS" + col + "_2_2", row, addr2);
                    } else {
                        svf.VrsOutn("ADDRESS" + col + "_1_1", row, addr1);
                        svf.VrsOutn("ADDRESS" + col + "_2_1", row, addr2);
                    }
                    
                    svf.VrsOutn("NAME" + col + "_1", row, StringUtils.defaultString(getString(data, "NAME1")) + "　様");
                    svf.VrsOutn("NAME" + col + "_2", row, getString(data, "SAT_NO"));
                }
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    private String sishagonyu(final String numString) {
        if (!NumberUtils.isNumber(numString)) {
            return numString;
        }
        return new BigDecimal(numString).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
        }
        return (Map) map.get(key1);
    }

    private List getList(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
                }
                list.add(m);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }
    
    private String sql(final Param param, final int checkInt) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     t1.YEAR, ");                                                                //
        stb.append("     t1.SAT_NO, ");                                                              //受験番号
        stb.append("     t1.NAME1, ");                                                               //氏名
        stb.append("     SUBSTR(t1.ZIPCODE,1,3) || '-' || SUBSTR(t1.ZIPCODE, 4,4) as ZIPCODE, ");    //郵便番号
        stb.append("     t1.ADDR1, ");                                                               //住所
        stb.append("     t1.ADDR2, ");                                                               //住所2
        /*海外受験者シール*/
        stb.append("     t7.PLACEAREA, ");      //受験会場
        /*国内校内生シール*/
        stb.append("     t1.INSIDERNO, ");      //校内生番号
        stb.append("     t7.PLACENAME_SHORT, "); //試験会場
        stb.append("     t8.GROUPNAME "); //団体名
        stb.append(" FROM ");
        stb.append("     SAT_APP_FORM_MST t1 ");
        stb.append("     left join SAT_EXAM_DAT t2 on t1.YEAR = t2.YEAR and t1.SAT_NO = t2.SAT_NO ");
        stb.append("     left join SAT_EXAM_DAT t5 on t1.YEAR = t5.YEAR and t1.SAT_NO = t5.SAT_NO ");
        stb.append("     left join FINSCHOOL_MST t6 on t1.SCHOOLCD = t6.FINSCHOOLCD and t6.FINSCHOOL_TYPE = '3' ");
        stb.append("     left join SAT_EXAM_PLACE_DAT t7 on t1.YEAR = t7.YEAR and t1.PLACECD = t7.PLACECD ");
        stb.append("     left join SAT_GROUP_DAT t8 on t1.YEAR = t8.YEAR and t1.GROUPCD = t8.GROUPCD ");
        if (checkInt == 22) {
            /*国内欠席者住所シール*/
            stb.append("     WHERE ");
            stb.append("         t1.YEAR = '" + param._CTRL_YEAR + "' AND ");
            stb.append("         t1.SCHOOLCD != '2008005' AND ");
            stb.append("         t1.ABSENCE = '0' AND ");
            stb.append("         t1.INOUT_KUBUN != '4' ");
        } else if (checkInt == 23) {
            /*海外受験者シール*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.INOUT_KUBUN = '4' ");
        } else if (checkInt == 24) {
            /*国内校内生シール*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.INOUT_KUBUN != '4' AND ");
            stb.append("     t1.IND_KUBUN = '3' ");
        } else if (checkInt == 0) {
            /*1.県外会場受験の県内生*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.PREFCD = '19' AND ");
            stb.append("     t1.PLACECD not in ('01','02','03') ");
        } else if (checkInt == 1) {
            /*2.県外会場受験の長野県中学校生*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t6.FINSCHOOL_PREF_CD = '20' AND ");
            stb.append("     t1.PLACECD not in ('01','02','03') ");
        } else if (checkInt == 2) {
            /*3.県内会場受験の長野除く県外生*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.PREFCD not in ('19','20') AND ");
            stb.append("     t1.PLACECD in ('01','02','03') ");
        } else if (checkInt == 3) {
            /*4.すべての長野県中学校生*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t6.FINSCHOOL_PREF_CD = '20' ");
        } else if (checkInt == 4) {
            /*5.県内の中2以下*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.PREFCD = '19' AND ");
            stb.append("     t1.GRADUATION < '09' ");
        } else if (checkInt == 5) {
            /*6.県内の特奨生*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.PREFCD = '19' AND ");
            stb.append("     t1.GRADUATION > '08' AND ");
            stb.append("     t2.JUDGE_SAT = '1' ");
        } else if (checkInt == 6) {
            /*7.長野の特奨生*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.PREFCD = '20' AND ");
            stb.append("     t1.GRADUATION > '08' AND ");
            stb.append("     t2.JUDGE_SAT = '1' ");
        } else if (checkInt == 7) {
            /*8.長野の特奨生除くA・準A現役*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.PREFCD = '20' AND ");
            stb.append("     t1.GRADUATION = '09' AND ");
            stb.append("     t2.JUDGE_SAT in ('2','3') ");
        } else if (checkInt == 8) {
            /*9.長野のB〜Dの現役*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.PREFCD = '20' AND ");
            stb.append("     t1.GRADUATION = '09' AND ");
            stb.append("     t2.JUDGE_SAT > '3' ");
        } else if (checkInt == 9) {
            /*10.長野除く県外の中2以下*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.PREFCD not in ('19', '20', '48') AND ");
            stb.append("     t1.GRADUATION < '09' ");
        } else if (checkInt == 10) {
            /*11.長野除く県外の特奨生*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.PREFCD not in ('19', '20', '48') AND ");
            stb.append("     t1.GRADUATION > '08' AND ");
            stb.append("     t2.JUDGE_SAT = '1' ");
        } else if (checkInt == 11) {
            /*12.長野除く県外の特奨生外A現役*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.PREFCD not in ('19', '20', '48') AND ");
            stb.append("     t1.GRADUATION = '09' AND ");
            stb.append("     t2.JUDGE_SAT = '2' ");
        } else if (checkInt == 12) {
            /*13.長野県除く県外のB現役*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.PREFCD not in ('19', '20', '48') AND ");
            stb.append("     t1.GRADUATION = '09' AND ");
            stb.append("     t2.JUDGE_SAT = '4' ");
        } else if (checkInt == 13) {
            /*14.長野除く県外のC・Dまたは現役以外*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.PREFCD not in ('19', '20', '48') AND ");
            stb.append("     (t1.GRADUATION = '99' OR t2.JUDGE_SAT > '4') ");
        } else if (checkInt == 14) {
            /*15.海外の特奨生*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.PREFCD = '48' AND ");
            stb.append("     t2.JUDGE_SAT = '1' ");
        } else if (checkInt == 15) {
            /*16.海外の特奨生除くA・B現役*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.PREFCD = '48' AND ");
            stb.append("     t2.JUDGE_SAT in ('2','4') ");
        } else if (checkInt == 16) {
            /*17.県内の欠席者リスト*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.ABSENCE = '0' AND ");
            stb.append("     t1.PREFCD = '19' ");
        } else if (checkInt == 17) {
            /*18.長野県の欠席者リスト*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.ABSENCE = '0' AND ");
            stb.append("     t1.PREFCD = '20' ");
        } else if (checkInt == 18) {
            /*19.県外の欠席者リスト*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.ABSENCE = '0' AND ");
            stb.append("     t1.PREFCD not in ('19','20','48') ");
        } else if (checkInt == 19) {
            /*20.海外の欠席者リスト*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.ABSENCE = '0' AND ");
            stb.append("     t1.PREFCD = '48' ");
        } else if (checkInt == 20) {
            /*21.浪人リスト*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.GRADUATION = '99' ");
        } else if (checkInt == 21) {
            /*22.欠科目者リスト*/
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("     t1.SCHOOLCD != '2008005' AND ");
            stb.append("     t1.ABSENCE != '0' AND ");
            stb.append("     (t5.ABSENCE_ENGLISH = '0' OR t5.ABSENCE_MATH = '0' OR t5.ABSENCE_JAPANESE = '0') ");
        }
        /*************************/
        stb.append("     ORDER BY ");
        stb.append("         t1.SAT_NO ");

        return stb.toString();
    }
    
    private static Map firstRow(final List list) {
        if (null == list || list.isEmpty()) {
            return new HashMap();
        }
        return (Map) list.get(0);
    }

    private static String getString(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }
    
    private static String formatDate(final String date) {
        if (null == date) {
            return "";
        }
        final String nen = new SimpleDateFormat("yyyy年").format(Date.valueOf(date));
        return nen + KNJ_EditDate.h_format_JP_MD(date);
    }
    
    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final String groupField, final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(row);
        }
        return rtn;
    }
    
    private static class Param {
        final String _CTRL_YEAR;
//        final String _CTRL_SEMESTER;
//        final String _CTRL_DATE;

        final Map _titleMap;
        final List _CHECK;
//        final String _CHECK_CNT;
        final String _PRGID;
        final String _cmd;
        private Param(final DB2UDB db2, final HttpServletRequest request) {
            _CTRL_YEAR = request.getParameter("CTRL_YEAR");
//            _CTRL_SEMESTER = request.getParameter("CTRL_SEMESTER");
//            _CTRL_DATE = request.getParameter("CTRL_DATE");
//            _CHECK2 = new ArrayList();
            _CHECK = new ArrayList();
            final String[] paramCheck = request.getParameterValues("CHECK[]");
            for (int i = 0; i < paramCheck.length; i++) {
                if (!NumberUtils.isDigits(paramCheck[i])) {
                    continue;
                }
                _CHECK.add(paramCheck[i]);
            }
//            _CHECK_CNT = request.getParameter("CHECK_CNT");
            _PRGID = request.getParameter("PRGID");
            _cmd = request.getParameter("cmd");
            
            _titleMap = new HashMap();
            _titleMap.put("0", "1. 県外会場受験の県内生");
            _titleMap.put("1", "2. 県外会場受験の長野県中学校生");
            _titleMap.put("2", "3. 県内会場受験の長野を除く県外生");
            _titleMap.put("3", "4. すべての長野県中学校生");
            _titleMap.put("4", "5. 県内の中2以下");
            _titleMap.put("5", "6. 県内の特奨生");
            _titleMap.put("6", "7. 長野の特奨生");
            _titleMap.put("7", "8. 長野の特奨生除くA・準A現役");
            _titleMap.put("8", "9. 長野のB〜Dの現役");
            _titleMap.put("9", "10.長野除く県外の中2以下");
            _titleMap.put("10", "11.長野除く県外の特奨生");
            _titleMap.put("11", "12.長野除く県外の特奨生外A現役");
            _titleMap.put("12", "13.長野除く県外のB現役");
            _titleMap.put("13", "14.長野除く県外のC・Dまたは現役以外");
            _titleMap.put("14", "15.海外の特奨生");
            _titleMap.put("15", "16.海外の特奨生除くA・B現役");
            _titleMap.put("16", "17.県内の欠席者リスト");
            _titleMap.put("17", "18.長野県の欠席者リスト");
            _titleMap.put("18", "19.県外の欠席者リスト");
            _titleMap.put("19", "20.海外の欠席者リスト");
            _titleMap.put("20", "21.浪人リスト");
            _titleMap.put("21", "22.欠科目者リスト");
            _titleMap.put("22", "国内欠席者住所シール");
            _titleMap.put("23", "海外受験者シール");
            _titleMap.put("24", "国内校内生シール");

        }
    }
}//クラスの括り
