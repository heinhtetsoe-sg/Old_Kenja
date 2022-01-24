// kanji=漢字
/*
 * $Id: 718094da6be8adaaf6e0b46204cbdba49b7285c4 $
 *
 * 作成日: 2017/06/05
 * 作成者: maesiro
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *  学校教育システム 賢者 [保健管理]
 *
 *                  ＜ＫＮＪＦ０３５＞  健康診断受検者名簿
 *
 */

public class KNJF035 {

    private static final Log log = LogFactory.getLog(KNJF035.class);

    private boolean _hasData = false;                               //該当データなしフラグ

    /**
     * HTTP Get リクエストの処理
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        final Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        //  print設定
        final PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //  svf設定
        svf.VrInit();                           //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());           //PDFファイル名の設定

        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意

        KNJServletUtils.debugParam(request, log);

        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        //  ＳＶＦ作成処理
        Param param = null;
        try {
            param = new Param(db2, request);
            
            for (int i = 0; i < param._categorySelected.length; i++) {
                final String gradeHrclass = param._categorySelected[i];
                printMain(db2, svf, param, gradeHrclass);
            }
            
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            
            //  該当データ無し
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();                //DBを閉じる
            outstrm.close();            //ストリームを閉じる
        }

    }//doGetの括り

    private static List query(final DB2UDB db2, final String sql) {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            final ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 0; i < meta.getColumnCount(); i++) {
                    final String columnName = meta.getColumnName(i + 1);
                    final String val = rs.getString(columnName);
                    m.put(columnName, val);
                }
                rtn.add(m);
            }
        } catch (Exception e) {
            log.error("exception! sql=" + sql, e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private static String getString(final Map map, final String field) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            if (null == field || !map.containsKey(field)) {
                // フィールド名が間違い
                throw new RuntimeException("指定されたフィールドのデータがありません。フィールド名：'" + field + "' :" + map);
            }
        } catch (final Exception e) {
            log.error("exception!", e);
        }
        return (String) map.get(field);
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Param param, final String gradeHrclass) {
        
        final String form = "KNJF035.frm";
        
        final String sql = getSql(param, gradeHrclass);
        final List studentList = query(db2, sql);
        

        final int maxLine = 45;
        final List pageList = getPageList(studentList, maxLine);
        svf.VrSetForm(form, 1);
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            
            final List dataList = (List) pageList.get(pi);
            for (int j = 0; j < dataList.size(); j++) {
                
                final Map student = (Map) dataList.get(j);
                
                svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度　受検者名簿"); // タイトル
                svf.VrsOut("HR_NAME", getString(student, "HR_NAME")); // 年組
                svf.VrsOut("TEACHER_NAME", getString(student, "STAFFNAME")); // 担任名

                final int line = j + 1;
                svf.VrsOutn("NO", line, NumberUtils.isDigits(getString(student, "ATTENDNO")) ? String.valueOf(Integer.parseInt(getString(student, "ATTENDNO"))) : getString(student, "ATTENDNO")); // 番号
                svf.VrsOutn("KANA1", line, getString(student, "NAME_KANA")); // ふりがな
                svf.VrsOutn("NAME1", line, getString(student, "NAME")); // 氏名
                svf.VrsOutn("GENERAL1", line, StringUtils.replace(getString(student, "DATE"), "-", "/")); // 一般
                svf.VrsOutn("GENERAL2_1", line, getText(1, student)); // 一般
                svf.VrsOutn("GENERAL2_2", line, getText(2, student)); // 一般
                svf.VrsOutn("TOOTH", line, StringUtils.replace(getString(student, "TOOTH_DATE"), "-", "/")); // 歯・口腔
//                svf.VrsOutn("REMARK", line, null); // 備考
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    private static String getText(final int i, final Map student) {
        final StringBuffer stb = new StringBuffer();
        final String[] field = new String[3];
        if (i == 1) {
            stb.append("一次");
            field[0] = "ALBUMINURIA1CD";
            field[1] = "URICSUGAR1CD";
            field[2] = "URICBLEED1CD";
        } else if (i == 2) {
            stb.append("二次");
            field[0] = "ALBUMINURIA2CD";
            field[1] = "URICSUGAR2CD";
            field[2] = "URICBLEED2CD";
        }
        stb.append("　蛋白:");
        stb.append(formatKeta(nullOrGe3(getString(student, field[0])) ? "" : getString(student, field[0] + "_NAME"), 3));
        stb.append("　糖:");
        stb.append(formatKeta(nullOrGe3(getString(student, field[1])) ? "" : getString(student, field[1] + "_NAME"), 3));
        stb.append("　潜血:");
        stb.append(formatKeta(nullOrGe3(getString(student, field[2])) ? "" : getString(student, field[2] + "_NAME"), 3));
        return stb.toString();
    }

    /**
     * valがnullか3以上
     * @param val
     * @return valがnullか3以上ならtrue
     */
    private static boolean nullOrGe3(final String val) {
        final int n = toInt(val, -1);
        return n == -1 || n >= 3;
    }

    private static int toInt(final String s, final int def) {
        if (!NumberUtils.isDigits(s)) {
            return def;
        }
        return Integer.parseInt(s);
    }

    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }
    
    private static String formatKeta(final String str, final int keta) {
        return StringUtils.repeat(" ", keta - getMS932ByteLength(str)) + StringUtils.defaultString(str);
    }

    private String getSql(final Param param, final String gradeHrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT REGD.SCHREGNO ");
        stb.append("      , REGD.YEAR ");
        stb.append("      , REGD.SEMESTER ");
        stb.append("      , REGD.GRADE ");
        stb.append("      , REGD.HR_CLASS ");
        stb.append("      , REGDG.SCHOOL_KIND ");
        stb.append("      , REGD.ATTENDNO ");
        stb.append("      , REGDH.HR_NAME ");
        stb.append("      , BASE.NAME ");
        stb.append("      , BASE.NAME_KANA ");
        stb.append("      , MEDH.DATE ");
        stb.append("      , MEDH.TOOTH_DATE ");
        stb.append("      , MED1.ALBUMINURIA1CD  , NM020_1.NAME1 AS ALBUMINURIA1CD_NAME ");
        stb.append("      , MED1.URICSUGAR1CD    , NM019_1.NAME1 AS URICSUGAR1CD_NAME ");
        stb.append("      , MED1.URICBLEED1CD    , NM018_1.NAME1 AS URICBLEED1CD_NAME ");
        stb.append("      , MED1.ALBUMINURIA2CD  , NM020_2.NAME1 AS ALBUMINURIA2CD_NAME ");
        stb.append("      , MED1.URICSUGAR2CD    , NM019_2.NAME1 AS URICSUGAR2CD_NAME ");
        stb.append("      , MED1.URICBLEED2CD    , NM018_2.NAME1 AS URICBLEED2CD_NAME ");
        stb.append("      , STF.STAFFNAME ");
        stb.append(" FROM  SCHREG_REGD_HDAT REGDH ");
        stb.append("       LEFT JOIN SCHREG_REGD_DAT REGD ON REGDH.YEAR = REGD.YEAR AND REGDH.SEMESTER = REGD.SEMESTER AND REGDH.GRADE = REGD.GRADE AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("       LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("       LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR AND REGDG.GRADE = REGD.GRADE ");
        stb.append("       LEFT JOIN MEDEXAM_HDAT MEDH ON MEDH.YEAR = REGD.YEAR AND MEDH.SCHREGNO = REGD.SCHREGNO ");
        stb.append("       LEFT JOIN MEDEXAM_DET_DAT MED1 ON MED1.YEAR = REGD.YEAR AND MED1.SCHREGNO = REGD.SCHREGNO ");
        stb.append("       LEFT JOIN STAFF_MST STF ON STF.STAFFCD = REGDH.TR_CD1 ");
        stb.append("       LEFT JOIN NAME_MST NM020_1 ON NM020_1.NAMECD1 = 'F020' AND NM020_1.NAMECD2 = MED1.ALBUMINURIA1CD ");
        stb.append("       LEFT JOIN NAME_MST NM019_1 ON NM019_1.NAMECD1 = 'F019' AND NM019_1.NAMECD2 = MED1.URICSUGAR1CD ");
        stb.append("       LEFT JOIN NAME_MST NM018_1 ON NM018_1.NAMECD1 = 'F018' AND NM018_1.NAMECD2 = MED1.URICBLEED1CD ");
        stb.append("       LEFT JOIN NAME_MST NM020_2 ON NM020_2.NAMECD1 = 'F020' AND NM020_2.NAMECD2 = MED1.ALBUMINURIA2CD ");
        stb.append("       LEFT JOIN NAME_MST NM019_2 ON NM019_2.NAMECD1 = 'F019' AND NM019_2.NAMECD2 = MED1.URICSUGAR2CD ");
        stb.append("       LEFT JOIN NAME_MST NM018_2 ON NM018_2.NAMECD1 = 'F018' AND NM018_2.NAMECD2 = MED1.URICBLEED2CD ");
        stb.append(" WHERE  REGDH.YEAR = '" + param._year + "' ");
        stb.append("       AND REGDH.SEMESTER = '" + param._gakki + "' ");
        stb.append("       AND REGDH.GRADE || REGDH.HR_CLASS = '" + gradeHrclass + "' ");
        stb.append("ORDER BY ");
        stb.append("   REGDH.YEAR ");
        stb.append(" , REGDH.GRADE ");
        stb.append(" , REGDH.HR_CLASS ");
        stb.append(" , REGD.ATTENDNO ");
        return stb.toString();
    }

    private static class Param {
        final String _year; //年度
        final String _gakki; //学期 1,2,3
        final String[] _categorySelected;  //学年・組
        final String _z010Name1;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _gakki = request.getParameter("GAKKI");
            _categorySelected = request.getParameterValues("category_selected");
            _z010Name1 = getNamemstZ010(db2);
            log.info(" _namemstZ010Name1 = " + _z010Name1);
        }

        private String getNamemstZ010(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String namemstZ010Name1 = "";
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    namemstZ010Name1 = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return namemstZ010Name1;
        }

    }

}//クラスの括り
