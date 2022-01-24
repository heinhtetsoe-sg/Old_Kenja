// kanji=漢字

package servletpack.KNJJ;

import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *  学校教育システム 賢者 [特別活動管理]
 *
 *                      ＜ＫＮＪＪ０６０＞  部員名簿一覧（部活動）
 *
 *  2004/11/19 nakamoto 光明版(LFA055M)を近大版(KNJJ060)へプログラムを移行
 *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJJ060 {

    /* ロギング */
    private static final Log log = LogFactory.getLog(KNJJ060.class);

    public static final String PATTERNA = "1";
    public static final String PATTERNB = "2";

    private Param _param;

    private Vrw32alp svf = new Vrw32alp();  //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private DB2UDB    db2;                  //Databaseクラスを継承したクラス
    private String _useSchool_KindField;
    private String _SCHOOLCD;
    private String _SCHOOLKIND;
    private String _useClubMultiSchoolKind;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws Exception
    {
        KNJServletUtils.debugParam(request, log);

        // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        // svf設定
        svf.VrInit();                        //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("[KNJJ060]DB2 open error!", ex);
            return;
        }

        _param = createParam(db2, request);
        _useSchool_KindField = request.getParameter("useSchool_KindField");
        _SCHOOLCD= request.getParameter("SCHOOLCD");
        _SCHOOLKIND = request.getParameter("SCHOOLKIND");
        _useClubMultiSchoolKind = request.getParameter("useClubMultiSchoolKind");

        if (PATTERNA.equals(_param._pattern)) {
            if (_param._hogosyaPrint) {
                svf.VrSetForm("KNJJ060_2.frm", 1);
            } else {
                svf.VrSetForm("KNJJ060.frm", 1);
            }
        } else {
            svf.VrSetForm("KNJJ060_3.frm", 1);
        }

        // ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        boolean nonedata = false;                               //該当データなしフラグ

        //SQL作成
        try {
            String sql;
            if (PATTERNA.equals(_param._pattern)) {
                sql = Pre_StatA();
            } else {
                sql = Pre_StatB();
            }
            //log.info(" sql = " + sql);
            ps1 = db2.prepareStatement(sql); //部員名簿一覧のＳＱＬ
        } catch( Exception ex ) {
            log.error("db2.prepareStatement error!", ex);
        }

        for (int ia = 0; ia < _param._clubSelected.length; ia++) {
            if (PATTERNA.equals(_param._pattern)) {
                if (svfoutA(_param._clubSelected[ia], ps1)) {
                    nonedata = true;
                }
            } else {
                if (svfoutB(_param._clubSelected[ia], ps1)) {
                    nonedata = true;
                }
            }
        }

        log.debug("[KNJJ060]nonedata = " + nonedata);
        // 該当データ無し
        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

        // 終了処理
        svf.VrQuit();
        DbUtils.closeQuietly(ps1);
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる

    }    //doGetの括り



    /** 作成日のメソッド **/
    private void Set_Head(){
        svf.VrsOut("NENDO", _param.changePrintYear(_param._year) + "度");
        svf.VrsOut("DATE", _param.changePrintDate(_param._ctrlDate));      //作成日
    }

    private int getMS932ByteLength(final String str) {
        int len = 0;
        if (null != str) {
            try {
                len = str.getBytes("MS932").length;
            } catch (final Exception e) {
                log.debug("exception!", e);
            }
        }
        return len;
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


    /**
     * sqlを発行した結果のレコード（Map）のリストを得る
     * @param db2 DB2
     * @param ps statement
     * @return レコードのリスト
     */
    public static List query(final DB2UDB db2, final PreparedStatement ps) {
        final List rowList = new ArrayList();
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            final ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                rowList.add(resultSetToRowMap(meta, rs));
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
        return rowList;
    }

    private static Map resultSetToRowMap(final ResultSetMetaData meta, final ResultSet rs) throws SQLException {
        final Map map = new HashMap();
        for (int i = 0; i < meta.getColumnCount(); i++) {
            final String columnName = meta.getColumnLabel(i + 1);
            final String val = rs.getString(columnName);
            map.put(columnName, val);
            map.put(new Integer(i + 1), val);
        }
        return map;
    }

    public static String getString(final Map row, String field) {
        if (null == row || row.isEmpty()) {
            return null;
        }
        field = field.toUpperCase();
        if (!row.containsKey(field)) {
            throw new IllegalStateException("no such field : " + field + " / " + row);
        }
        return (String) row.get(field);
    }

    /*----------------------------*
     * ＳＶＦ作成
     *----------------------------*/
    private boolean svfoutA(final String classcd, final PreparedStatement ps1) {
        boolean nonedata = false;

        try {
            ps1.setString(1, classcd);

            final List list = query(db2, ps1);

            final List pageList = getPageList(list, 20);

            for (int pi = 0; pi < pageList.size(); pi++) {

                final List rowList = (List) pageList.get(pi);

                Set_Head();                                        //作成日のメソッド

                for (int i = 0; i < rowList.size(); i++) {
                    final int line = i + 1;
                    final Map row = (Map) rowList.get(i);

                    svf.VrsOut("CLUBCD"   , getString(row, "CLUBCD"));
                    svf.VrsOut("CLUBNAME" , getString(row, "CLUBNAME"));
                    //明細出力
                    svf.VrsOutn("HR_CLASS" , line, getString(row, "HR_NAME"));
                    svf.VrsOutn("ATTENDNO" , line, getString(row, "ATTENDNO"));
                    final String name = getString(row, "NAME");
                    final int nameByte = KNJ_EditEdit.getMS932ByteLength(name);
                    final String nameField = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "";
                    svf.VrsOutn("NAME" + nameField, line, name);
                    svf.VrsOutn("KANA"     , line, getString(row, "NAME_KANA"));
                    svf.VrsOutn("BIRTHDAY" , line, KNJ_EditDate.h_format_JP(getString(row, "BIRTHDAY")));
                    svf.VrsOutn("DATE_S"   , line, KNJ_EditDate.h_format_JP(getString(row, "SDATE")));
                    svf.VrsOutn("DATE_F"   , line, KNJ_EditDate.h_format_JP(getString(row, "EDATE")));
                    svf.VrsOutn("POST"     , line, getString(row, "POSTNAME"));
                    svf.VrsOutn("NOTE"     , line, getString(row, "REMARK"));

                    if (_param._hogosyaPrint) { // 保護者出力
                        svf.VrsOutn("GUARD_NAME" , line, getString(row, "GUARD_NAME"));
                        if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(getString(row, "GUARD_ADDR1")) > 50 || getMS932ByteLength(getString(row, "GUARD_ADDR1")) > 50)) {
                            svf.VrsOutn("ADDRESS1_2"  , line, getString(row, "GUARD_ADDR1"));
                            svf.VrsOutn("ADDRESS2_2"  , line, getString(row, "GUARD_ADDR2"));
                        } else {
                            svf.VrsOutn("ADDRESS1"  , line, getString(row, "GUARD_ADDR1"));
                            svf.VrsOutn("ADDRESS2"  , line, getString(row, "GUARD_ADDR2"));
                        }
                        svf.VrsOutn("TELNO"    , line, getString(row, "GUARD_TELNO"));
                    }
                    nonedata = true;
                }

                svf.VrEndPage();
            }

        } catch( Exception ex ){
            log.warn("[KNJJ060]svfout read error!");
        }

        return nonedata;
    }

    /*----------------------------*
     * ＳＶＦ作成
     *----------------------------*/
    private boolean svfoutB(final String classcd, final PreparedStatement ps1) {
        boolean nonedata = false;

        try {
            ps1.setString(1, classcd);

            final List list = query(db2, ps1);

            final List pageList = getPageList(list, 20);

            for (int pi = 0; pi < pageList.size(); pi++) {

                final List rowList = (List) pageList.get(pi);

                Set_Head();                                        //作成日のメソッド

                for (int i = 0; i < rowList.size(); i++) {
                    final int line = i + 1;
                    final Map row = (Map) rowList.get(i);

                    svf.VrsOut("CLUBCD"   , getString(row, "CLUBCD"));
                    svf.VrsOut("CLUBNAME" , getString(row, "CLUBNAME"));
                    //明細出力
                    svf.VrsOutn("HR_CLASS" , line, getString(row, "HR_NAME"));
                    svf.VrsOutn("ATTENDNO" , line, getString(row, "ATTENDNO"));
                    String setField = getMS932ByteLength(getString(row, "NAME")) > 14 ? "2_1" : "";
                    svf.VrsOutn("NAME" + setField     , line, getString(row, "NAME"));
                    setField = getMS932ByteLength(getString(row, "NAME_KANA")) > 20 ? "2_1" : "";
                    svf.VrsOutn("KANA" + setField     , line, getString(row, "NAME_KANA"));
                    svf.VrsOutn("SCHREG_NO", line, getString(row, "SCHREGNO"));
                    svf.VrsOutn("DATE_S" , line, null == getString(row, "BIRTHDAY") ? "" : getString(row, "BIRTHDAY").replace('-', '/'));
                    svf.VrsOutn("SEX"      , line, getString(row, "SEX"));

                    if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(getString(row, "ADDR1")) > 50 || getMS932ByteLength(getString(row, "ADDR2")) > 50)) {
                        svf.VrsOutn("ADDRESS1_2"  , line, getString(row, "ADDR1"));
                        svf.VrsOutn("ADDRESS2_2"  , line, getString(row, "ADDR2"));
                    } else {
                        svf.VrsOutn("ADDRESS1"  , line, getString(row, "ADDR1"));
                        svf.VrsOutn("ADDRESS2"  , line, getString(row, "ADDR2"));
                    }
                    svf.VrsOutn("TELNO"    , line, getString(row, "TELNO"));

                    if (_param._hogosyaPrint) { // 保護者出力
                        setField = getMS932ByteLength(getString(row, "GUARD_NAME")) > 14 ? "2_1" : "";
                        svf.VrsOutn("GUARD_NAME" + setField , line, getString(row, "GUARD_NAME"));
                        setField = getMS932ByteLength(getString(row, "GUARD_KANA")) > 20 ? "2_1" : "";
                        svf.VrsOutn("GUARD_KANA" + setField , line, getString(row, "GUARD_KANA"));
                    }
                    svf.VrsOutn("FINSCHOOL_NAME"    , line, getString(row, "FINSCHOOL_NAME"));
                    nonedata = true;
                }

                svf.VrEndPage();
            }
        } catch( Exception ex ){
            log.warn("[KNJJ060]svfout read error!");
        }

        return nonedata;
    }

    /* 科目毎の得点
     * 抽出条件 出力範囲の生徒で、類型評定が１。科目毎。
     */
    private String Pre_StatA()
    {
        //欠点科目、学年、出力生徒範囲をパラメータとする
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT ");
            stb.append("    T6.HR_NAME, ");
            stb.append("    T1.GRADE, ");
            stb.append("    T1.HR_CLASS, ");
            stb.append("    T1.ATTENDNO, ");
            stb.append("    T2.NAME, ");
            stb.append("    T2.NAME_KANA, ");
            stb.append("    T2.BIRTHDAY, ");
            stb.append("    T3.CLUBCD, ");
            stb.append("    T4.CLUBNAME, ");
            stb.append("    VALUE(CHAR(T3.SDATE),' ') AS SDATE, ");
            stb.append("    VALUE(CHAR(T3.EDATE),' ') AS EDATE, ");
            stb.append("    VALUE(T5.NAME1,' ') AS POSTNAME, ");
            stb.append("    VALUE(T3.REMARK,' ') AS REMARK, ");
            stb.append("    T7.GUARD_NAME, ");
            stb.append("    T7.GUARD_ADDR1, ");
            stb.append("    T7.GUARD_ADDR2, ");
            stb.append("    T7.GUARD_TELNO ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_HDAT T6 ");
            stb.append("    INNER JOIN SCHREG_REGD_DAT T1 ON ");
            stb.append("        T1.YEAR = T6.YEAR AND ");
            stb.append("        T1.SEMESTER = T6.SEMESTER AND ");
            stb.append("        T1.GRADE || T1.HR_CLASS = T6.GRADE || T6.HR_CLASS ");
            if ("1".equals(_useClubMultiSchoolKind)) {
                //全生徒が対象
            } else if (null != _param.schKind) {
                stb.append("  INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T6.YEAR AND GDAT.GRADE = T6.GRADE ");
                stb.append("        AND GDAT.SCHOOL_KIND = '" + _param.schKind + "'  ");
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append("  INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T6.YEAR AND GDAT.GRADE = T6.GRADE ");
                stb.append("        AND GDAT.SCHOOL_KIND = '" + _SCHOOLKIND + "'  ");
            }
            stb.append("    INNER JOIN SCHREG_BASE_MST T2 ON ");
            stb.append("        T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    INNER JOIN SCHREG_CLUB_HIST_DAT T3 ON ");
            stb.append("        T3.SCHREGNO = T1.SCHREGNO ");
            if ("1".equals(_useClubMultiSchoolKind)) {
                stb.append("        AND T3.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("        AND T3.SCHOOL_KIND = '" + _SCHOOLKIND + "'  ");
            } else if (null != _param.schKind) {
                stb.append("        AND T3.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("        AND T3.SCHOOL_KIND = '" + _param.schKind + "'  ");
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLCD) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append("        AND T3.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("        AND T3.SCHOOL_KIND = '" + _SCHOOLKIND + "'  ");
            }
            if (_param._taibusyaNotPrint) {
                stb.append("    AND (T3.EDATE IS NULL ");
                if ("2".equals(_param._pattern)) {
                stb.append("        OR T3.EDATE > '" + _param._ctrlDate + "') ");
                } else {
                    stb.append("      OR T3.EDATE > '" + _param._toDate + "') ");
            }
            }
            stb.append("        AND ((T3.SDATE BETWEEN '" + _param._fromDate + "' AND '" + _param._toDate + "') ");
            stb.append("               OR ");
            stb.append("             (VALUE(T3.EDATE, '9999-12-31') BETWEEN '" + _param._fromDate + "' AND '" + _param._toDate + "') ");
            stb.append("               OR ");
            stb.append("             (T3.SDATE <= '" + _param._fromDate + "' AND VALUE(T3.EDATE, '9999-12-31') >= '" + _param._toDate + "') ");
            stb.append("            ) ");
            stb.append("    INNER JOIN CLUB_MST T4 ON ");
            stb.append("        T4.CLUBCD = T3.CLUBCD ");
            if ("1".equals(_useClubMultiSchoolKind)) {
                stb.append("        AND T4.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("        AND T4.SCHOOL_KIND = '" + _SCHOOLKIND + "'  ");
            } else if (null != _param.schKind) {
                stb.append("        AND T4.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("        AND T4.SCHOOL_KIND = '" + _param.schKind + "'  ");
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLCD) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append("        AND T4.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("        AND T4.SCHOOL_KIND = '" + _SCHOOLKIND + "'  ");
            }
            stb.append("    LEFT JOIN NAME_MST T5 ON ");
            stb.append("        T5.NAMECD1 = 'J001' AND ");
            stb.append("        T5.NAMECD2 = T3.EXECUTIVECD ");
            stb.append("    LEFT JOIN GUARDIAN_DAT T7 ON ");
            stb.append("        T7.SCHREGNO = T1.SCHREGNO ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '" + _param._year + "' AND ");
            stb.append("    T1.SEMESTER = '" + _param._semester + "' AND ");
            stb.append("    T3.CLUBCD = ? ");
            stb.append("ORDER BY ");
            stb.append("    T3.CLUBCD, ");
            stb.append("     " + _param._sortSql + " ");
        } catch( Exception e ){
            log.warn("Pre_Stat1 error!");
        }
        return stb.toString();

    }//Pre_Stat1()の括り

    /* 科目毎の得点 Bパターン
     * 抽出条件 出力範囲の生徒で、類型評定が１。科目毎。
     */
    private String Pre_StatB()
    {
        //欠点科目、学年、出力生徒範囲をパラメータとする
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" WITH SCHINFO AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T3.HR_NAME, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T2.NAME, ");
            stb.append("     T2.NAME_KANA, ");
            stb.append("     T2.BIRTHDAY, ");
            stb.append("     ADDR.ADDR1, ");
            stb.append("     ADDR.ADDR2, ");
            stb.append("     ADDR.TELNO, ");
            stb.append("     FIN.FINSCHOOL_NAME, ");
            stb.append("     T2.SEX ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            if ("1".equals(_useClubMultiSchoolKind)) {
                //全生徒が対象
            } else if (null != _param.schKind) {
                stb.append("  INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
                stb.append("        AND GDAT.SCHOOL_KIND = '" + _param.schKind + "'  ");
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append("  INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
                stb.append("        AND GDAT.SCHOOL_KIND = '" + _SCHOOLKIND + "'  ");
            }
            stb.append("     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     LEFT JOIN FINSCHOOL_MST FIN ON T2.FINSCHOOLCD = FIN.FINSCHOOLCD ");
            stb.append("     LEFT JOIN (SELECT ");
            stb.append("                    T1.SCHREGNO, ");
            stb.append("                    T1.ADDR1, ");
            stb.append("                    T1.ADDR2, ");
            stb.append("                    T1.TELNO ");
            stb.append("                FROM ");
            stb.append("                    SCHREG_ADDRESS_DAT T1, ");
            stb.append("                    (SELECT ");
            stb.append("                          SCHREGNO, ");
            stb.append("                          MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("                        FROM ");
            stb.append("                          SCHREG_ADDRESS_DAT ");
            stb.append("                        GROUP BY ");
            stb.append("                          SCHREGNO ");
            stb.append("                    ) T2 ");
            stb.append("                WHERE  ");
            stb.append("                    T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("                    AND T1.ISSUEDATE = T2.ISSUEDATE ");
            stb.append("         ) ADDR ON T1.SCHREGNO = ADDR.SCHREGNO, ");
            stb.append("     SCHREG_REGD_HDAT T3 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = T3.YEAR AND ");
            stb.append("     T1.YEAR = '" + _param._year + "' AND ");
            stb.append("     T1.SEMESTER = T3.SEMESTER AND ");
            stb.append("     T1.SEMESTER = '" + _param._semester + "' AND ");
            stb.append("     T1.GRADE = T3.GRADE AND ");
            stb.append("     T1.HR_CLASS = T3.HR_CLASS ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T3.CLUBCD, ");
            stb.append("     C1.CLUBNAME, ");
            stb.append("     T3.SCHREGNO, ");
            stb.append("     T1.HR_NAME, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.NAME, ");
            stb.append("     T1.NAME_KANA, ");
            stb.append("     T1.BIRTHDAY, ");
            stb.append("     N1.NAME1 AS SEX, ");
            stb.append("     VALUE(T1.ADDR1,'') AS ADDR1, ");
            stb.append("     VALUE(T1.ADDR2,'') AS ADDR2, ");
            stb.append("     T1.TELNO, ");
            stb.append("     G1.GUARD_NAME, ");
            stb.append("     G1.GUARD_KANA, ");
            stb.append("     T1.FINSCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_CLUB_HIST_DAT T3 ");
            stb.append("     LEFT JOIN CLUB_MST C1 ON T3.CLUBCD = C1.CLUBCD ");
            if ("1".equals(_useClubMultiSchoolKind)) {
                stb.append("        AND C1.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("        AND C1.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            } else if (null != _param.schKind) {
                stb.append("        AND C1.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("        AND C1.SCHOOL_KIND = '" + _param.schKind + "' ");
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLCD) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append("        AND C1.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("        AND C1.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append("     LEFT JOIN GUARDIAN_DAT G1 ON T3.SCHREGNO = G1.SCHREGNO, ");
            stb.append("     SCHINFO T1 ");
            stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND T1.SEX = N1.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     T3.SCHREGNO = T1.SCHREGNO AND ");
            if ("1".equals(_useClubMultiSchoolKind)) {
                stb.append("        T3.SCHOOLCD = '" + _SCHOOLCD + "' AND ");
                stb.append("        T3.SCHOOL_KIND = '" + _SCHOOLKIND + "'  AND ");
            } else if (null != _param.schKind) {
                stb.append("        T3.SCHOOLCD = '" + _SCHOOLCD + "' AND ");
                stb.append("        T3.SCHOOL_KIND = '" + _param.schKind + "'  AND ");
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLCD) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append("        T3.SCHOOLCD = '" + _SCHOOLCD + "' AND ");
                stb.append("        T3.SCHOOL_KIND = '" + _SCHOOLKIND + "'  AND ");
            }
            if(_param._taibusyaNotPrint) {
                stb.append("     (T3.EDATE IS NULL OR ");
                stb.append("      T3.EDATE > '" + _param._ctrlDate + "') AND ");
            }
            stb.append("    T3.CLUBCD = ? ");
            stb.append("    AND ((T3.SDATE BETWEEN '" + _param._fromDate + "' AND '" + _param._toDate + "') ");
            stb.append("          OR ");
            stb.append("         (VALUE(T3.EDATE, '9999-12-31') BETWEEN '" + _param._fromDate + "' AND '" + _param._toDate + "') ");
            stb.append("          OR ");
            stb.append("         (T3.SDATE <= '" + _param._fromDate + "' AND VALUE(T3.EDATE, '9999-12-31') >= '" + _param._toDate + "') ");
            stb.append("    ) ");
            stb.append("ORDER BY ");
            stb.append("    T3.CLUBCD, ");
            stb.append("     " + _param._sortSql + " ");

        } catch( Exception e ){
            log.warn("Pre_Stat1 error!");
        }
log.debug(stb.toString());
        return stb.toString();

    }//Pre_Stat1()の括り

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 59804 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    public class Param {
        final boolean _hogosyaPrint;
        final boolean _taibusyaNotPrint;
        final String _year;
        final String _semester;
        final String _ctrlDate;
        final String[] _clubSelected;
        final String[] _sortSelected;
        final String _fromDate;
        final String _toDate;
        final String _sortSql;
        final boolean _isChukyo;
        final String _useAddrField2;
        final String _pattern;
        final String schKind;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _hogosyaPrint = null != request.getParameter("hogosya") ? true : false;
            _taibusyaNotPrint = null != request.getParameter("taibusya_nozoku") ? true : false;
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _clubSelected = request.getParameterValues("CLUB_SELECTED");
            _pattern = null == request.getParameter("PATTERN") ? PATTERNA : request.getParameter("PATTERN");
            _fromDate = StringUtils.replace(request.getParameter("FROM_DATE"), "/", "-");
            _toDate = StringUtils.replace(request.getParameter("TO_DATE"), "/", "-");
            schKind = request.getParameter("SCHKIND");

            final String schoolNameFlg = getSchoolNameFlg(db2);
            _isChukyo = "chukyo".equals(schoolNameFlg);

            _sortSelected = request.getParameterValues("SORT_SELECTED");
            String sep = "";
            final StringBuffer stbSort = new StringBuffer();
            if (null != _sortSelected) {
                boolean findnenkumibanflg = false;
                for (int i = 0; i < _sortSelected.length; i++) {
                    String sortName = "T1.GRADE, T1.HR_CLASS, T1.ATTENDNO";
                    if (_sortSelected[i].equals("SEX")) {
                        if (PATTERNA.equals(_pattern)) {
                        sortName = "T2." + _sortSelected[i];
                        } else {
                            sortName = "T1." + _sortSelected[i];
                        }
                    } else if (_sortSelected[i].equals("EXECUTIVECD")) {
                        //役職は、中京：昇順。その他：降順。
                        //-- 役職コードがNULLの場合、１番下。
                        if (_isChukyo) {
                            sortName = "T3." + _sortSelected[i];
                        } else {
                            sortName = "case when T3." + _sortSelected[i] + " is null then -1 else smallint(T3." + _sortSelected[i] + ") end DESC ";
                        }
                    } else if (_sortSelected[i].equals("EXECUTIVECD2")){
                        sortName = "T3.EXECUTIVECD";
                    } else if (_sortSelected[i].equals("NEN_KUMI_BAN")){
                        findnenkumibanflg = true;
                    }
                    stbSort.append(sep + sortName);
                    sep = ",";
                }
                //最後に、年組番号が設定されていない場合、優先度最低で年組番号を指定。
                if (!findnenkumibanflg) {
                    stbSort.append((0 == _sortSelected.length ? "" : ",") + "T1.GRADE, T1.HR_CLASS, T1.ATTENDNO");
                }
            } else {
                stbSort.append("T1.GRADE, T1.HR_CLASS, T1.ATTENDNO");
            }
            _sortSql = stbSort.toString();
            _useAddrField2 = request.getParameter("useAddrField2");
        }

        public String changePrintDate(final String date) {
            if (null != date) {
                return KNJ_EditDate.h_format_JP(date);
            } else {
                return "";
            }
        }

        public String changePrintYear(final String year) {
            if (null == year) {
                return "";
            }
            return KenjaProperties.gengou(Integer.parseInt(year)) + "年";
        }

        private String getSchoolNameFlg(DB2UDB db2) {
            String rtnName = null;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if ( rs.next() ) {
                    rtnName = rs.getString("NAME1");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSchoolNameFlg Exception", e);
            }
            return rtnName;
        }
    }
}  //クラスの括り
