// kanji=漢字
/*
 * $Id: 342fd48eba899badf24f833d6c165e37fbf9c4ca $
 *
 * 作成日: 2003/11/17
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Semester;

/**
 * <<住所のタックシール印刷>>。
 */
public class KNJA190W extends HttpServlet {
    private boolean nonedata;           // 該当データなしフラグ
    private static final Log log = LogFactory.getLog(KNJA190W.class);

    private Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws Exception
    {
        // print設定
        //        PrintWriter out = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        // svf設定
        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);    //PDFファイル名の設定

        // ＤＢ接続
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("[KNJA190]DB2 open error!", ex);
            return;
        }

        try {
            // パラメータの取得
            _param = createParam(db2, request);


            /*-----------------------------------------------------------------------------
             ＳＶＦ作成処理
             -----------------------------------------------------------------------------*/
            nonedata = false;       // 該当データなしフラグ(MES001.frm出力用)

            if("1".equals(_param._output)) {
                printList(svf, set_detail2(db2, svf), 2);
            } else {
                printList(svf, set_detail1(db2, svf), 1);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //該当データ無しフォーム出力
            if (nonedata == false) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            // 終了処理
            db2.close();        // DBを閉じる
            svf.VrQuit();
            outstrm.close();    // ストリームを閉じる
        }

    }   //doGetの括り

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    /*----------------------------*
     * SVF出力                    *
     *----------------------------*/
    /*----------------------------*
     * 保護者出力                 *
     *----------------------------*/
    private List set_detail1(final DB2UDB db2, final Vrw32alp svf) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List list = new ArrayList();
        try {
            StringBuffer sql = new StringBuffer();
            final String table = "SCHREG_SEND_ADDRESS_DAT";
            final String fldZipcd = "SEND_ZIPCD";
            final String fldAddr1 = "SEND_ADDR1";
            final String fldAddr2 = "SEND_ADDR2";
            final String fldName = "SEND_NAME";
            final String where = " AND t1.DIV = '1' ";

            sql.append("SELECT ");
            sql.append(" REGD.SCHREGNO, ");
            sql.append(" CASE WHEN t1." + fldAddr1 + " IS NOT NULL THEN VALUE(t1." + fldZipcd + ", '') ELSE t1_2.GUARD_ZIPCD END AS ZIPCD,");
            sql.append(" CASE WHEN t1." + fldAddr1 + " IS NOT NULL THEN t1." + fldAddr1 + "            ELSE t1_2.GUARD_ADDR1 END AS ADDR1, ");
            sql.append(" CASE WHEN t1." + fldAddr1 + " IS NOT NULL THEN t1." + fldAddr2 + "            ELSE t1_2.GUARD_ADDR2 END AS ADDR2, ");
            sql.append(" CASE WHEN t1." + fldAddr1 + " IS NOT NULL THEN t1." + fldName + "             ELSE t1_2.GUARD_NAME END AS NAME, ");
            if ("SCHREG_REGD_GHR_DAT".equals(_param._tableRegdDat)) {
                sql.append("    REGD.GHR_CD AS GRADE_HR_CLASS, ");
                sql.append("    REGD.GHR_ATTENDNO AS ATTENDNO, ");
                sql.append("    t3.GHR_NAME AS HR_NAME, ");
            } else {
                sql.append("    REGD.GRADE || REGD.HR_CLASS AS GRADE_HR_CLASS, ");
                sql.append("    REGD.ATTENDNO AS ATTENDNO, ");
                sql.append("    t3.HR_NAME AS HR_NAME, ");
            }
            sql.append("    t4.NAME AS NAME2 ");
            sql.append("    , t4.NAME_KANA ");
            if (!"SCHREG_REGD_GHR_DAT".equals(_param._tableRegdDat)) {
                sql.append("   , REGDG.SCHOOL_KIND ");
            } else {
                sql.append("   , '' AS SCHOOL_KIND ");
            }
            sql.append(" FROM ");
            sql.append("" + _param._tableRegdDat + " REGD ");
            sql.append("LEFT JOIN " + table + " t1 ON t1.SCHREGNO = REGD.SCHREGNO " + where);
            sql.append("LEFT JOIN GUARDIAN_DAT t1_2 ON t1_2.SCHREGNO = REGD.SCHREGNO ");
            sql.append("LEFT JOIN " + _param._tableRegdHDat + " t3 ");
            if ("SCHREG_REGD_GHR_DAT".equals(_param._tableRegdDat)) {
                sql.append("ON REGD.GHR_CD = t3.GHR_CD ");
            } else {
                sql.append("ON REGD.GRADE = t3.GRADE AND REGD.HR_CLASS = t3.HR_CLASS ");
            }
            sql.append("AND t3.YEAR = '" + _param._year + "' ");
            sql.append("AND t3.SEMESTER = '" + _param._semester + "' ");
            sql.append("LEFT JOIN SCHREG_BASE_MST t4 ON REGD.SCHREGNO = t4.SCHREGNO ");
            if (!"SCHREG_REGD_GHR_DAT".equals(_param._tableRegdDat)) {
                sql.append("LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR AND REGDG.GRADE = REGD.GRADE ");
            }
            sql.append(" WHERE ");
            sql.append(" REGD.YEAR = '" + _param._year + "' ");
            sql.append(" AND REGD.SEMESTER = '" + _param._semester + "' ");
            if ("1".equals(_param._choice) || "3".equals(_param._choice)) {
                sql.append("AND REGD.SCHREGNO IN " + _param._schregInState + " ");
            } else {
                if ("SCHREG_REGD_GHR_DAT".equals(_param._tableRegdDat)) {
                    sql.append(" AND REGD.GHR_CD IN " + _param._schregInState + " ");
                } else {
                    sql.append(" AND REGD.GRADE || REGD.HR_CLASS IN " + _param._schregInState + " ");
                }
            }
            if ("1".equals(_param._grdDiv)) {
                sql.append(" AND NOT ((T4.GRD_DIV IS NOT NULL AND T4.GRD_DIV <> '4') AND GRD_DATE < '" + _param._ctrlDate + "' ) ");
            }
            sql.append(" AND (T1." + fldAddr1 + " IS NOT NULL OR T1_2.GUARD_ADDR1 IS NOT NULL)");
            sql.append(" ORDER BY ");
            if (_param._output2.equals("1")){ //NO002
                sql.append(" GRADE_HR_CLASS, REGD.SCHREGNO");
            } else {
                sql.append(" GRADE_HR_CLASS, ATTENDNO");
            }

            log.fatal("set_detail1 sql = " + sql);
            ps = db2.prepareStatement(sql.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final Map m = new HashMap();
                list.add(m);
                m.put("ZIPCD", rs.getString("ZIPCD"));
                m.put("ADDR1", rs.getString("ADDR1"));
                m.put("ADDR2", rs.getString("ADDR2"));
                m.put("NAME", rs.getString("NAME"));
                m.put("NAME2", rs.getString("NAME2"));
                m.put("NAME_KANA", rs.getString("NAME_KANA"));
                m.put("HR_NAME", rs.getString("HR_NAME"));
                m.put("SCHOOL_KIND", rs.getString("SCHOOL_KIND"));
                m.put("GRADE_HR_CLASS", rs.getString("GRADE_HR_CLASS"));
                m.put("SCHREGNO", rs.getString("SCHREGNO"));

            }
        } catch (Exception ex) {
            log.error("[KNJA190]set_detail1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;

    }   //set_detail1の括り


    /*----------------------------*
     * 生徒出力 2003/11/17        *
     *----------------------------*/
    private List set_detail2(final DB2UDB db2, final Vrw32alp svf) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List list = new ArrayList();
        try {
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT ");
            sql.append("T1.SCHREGNO,");
            sql.append("VALUE(T1.ZIPCD,'') AS ZIPCD,");
            sql.append("T1.ADDR1,T1.ADDR2,");
            if ("SCHREG_REGD_GHR_DAT".equals(_param._tableRegdDat)) {
                sql.append("    t4.GHR_CD AS GRADE_HR_CLASS, ");
                sql.append("    t4.GHR_ATTENDNO AS ATTENDNO, ");
                sql.append("    t5.GHR_NAME AS HR_NAME, ");
            } else {
                sql.append("    t4.GRADE || t4.HR_CLASS AS GRADE_HR_CLASS, ");
                sql.append("    t4.ATTENDNO AS ATTENDNO, ");
                sql.append("    t5.HR_NAME AS HR_NAME, ");
            }
            sql.append("T2.NAME ");
            sql.append(", T2.NAME_KANA ");
            if (!"SCHREG_REGD_GHR_DAT".equals(_param._tableRegdDat)) {
                sql.append("   , REGDG.SCHOOL_KIND ");
            } else {
                sql.append("   , '' AS SCHOOL_KIND ");
            }
            sql.append("FROM ");
            sql.append("SCHREG_BASE_MST T2 ");
            sql.append("LEFT JOIN " + _param._tableRegdDat + " t4 ON t2.SCHREGNO = t4.SCHREGNO AND t4.YEAR = '" + _param._year + "' AND t4.SEMESTER = '" + _param._semester + "' ");
            sql.append("LEFT JOIN " + _param._tableRegdHDat + " t5 ON t5.YEAR = t4.YEAR AND t5.SEMESTER = t4.SEMESTER ");
            if ("SCHREG_REGD_GHR_DAT".equals(_param._tableRegdDat)) {
                sql.append("AND t5.GHR_CD = t4.GHR_CD ");
            } else {
                sql.append("AND t5.GRADE = t4.GRADE AND t5.HR_CLASS = t4.HR_CLASS ");
            }
            if (!"SCHREG_REGD_GHR_DAT".equals(_param._tableRegdDat)) {
                sql.append("LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = t4.YEAR AND REGDG.GRADE = t4.GRADE ");
            }
            sql.append("INNER JOIN (");
            sql.append("SELECT ");
            sql.append("    SCHREGNO,");
            sql.append("    ZIPCD,");
            sql.append("    ADDR1,");
            sql.append("    ADDR2 ");
            sql.append("FROM ");
            sql.append("    SCHREG_ADDRESS_DAT W1 ");
            sql.append("WHERE ");
            sql.append("(    W1.SCHREGNO,W1.ISSUEDATE) IN ( ");
            sql.append("                 SELECT SCHREGNO,MAX(ISSUEDATE) ");
            sql.append("                 FROM   SCHREG_ADDRESS_DAT W2 ");
            sql.append("                 WHERE  W2.ISSUEDATE <= '" + _param._semesterEdate + "' ");
            sql.append("                            AND (W2.EXPIREDATE IS NULL ");
            sql.append("                            OR W2.EXPIREDATE >= '" + _param._semesterSdate + "') ");
            sql.append("                            AND W2.SCHREGNO IN ( ");
            sql.append("                                SELECT ");
            sql.append("                                    w3.SCHREGNO ");
            sql.append("                                FROM ");
            sql.append("                                " + _param._tableRegdDat + " w3 ");
            sql.append("                                WHERE ");
            if ("1".equals(_param._choice) || "3".equals(_param._choice)) {
                sql.append("                                    w3.SCHREGNO IN " + _param._schregInState + " ");
            } else {
                if ("SCHREG_REGD_GHR_DAT".equals(_param._tableRegdDat)) {
                    sql.append("                                w3.GHR_CD IN " + _param._schregInState + " ");
                } else {
                    sql.append("                                w3.GRADE || w3.HR_CLASS IN " + _param._schregInState + " ");
                }
            }
            sql.append("                                AND w3.YEAR = '" + _param._year + "' ");
            sql.append("                                     AND w3.SEMESTER = '" + _param._semester + "' ");
            sql.append("                             ) ");
            sql.append("                            GROUP BY SCHREGNO ) ");
            sql.append("        )T1 ON T1.SCHREGNO = T2.SCHREGNO ");
            if ("1".equals(_param._grdDiv)) {
                sql.append("     WHERE ");
                sql.append("     NOT ((T2.GRD_DIV IS NOT NULL AND T2.GRD_DIV <> '4') AND GRD_DATE < '" + _param._ctrlDate + "' ) ");
            }
            sql.append("    ORDER BY ");
            if (_param._output2.equals("1")) {
                sql.append("    GRADE_HR_CLASS, SCHREGNO");
            } else {
                sql.append("    GRADE_HR_CLASS, ATTENDNO");
            }

            log.fatal("set_detail2 sql = " + sql);
            ps = db2.prepareStatement(sql.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final Map m = new HashMap();
                list.add(m);
                m.put("ZIPCD", rs.getString("ZIPCD"));
                m.put("ADDR1", rs.getString("ADDR1"));
                m.put("ADDR2", rs.getString("ADDR2"));
                m.put("NAME", rs.getString("NAME"));
                m.put("NAME_KANA", rs.getString("NAME_KANA"));
                m.put("HR_NAME", rs.getString("HR_NAME"));
                m.put("ATTENDNO", rs.getString("ATTENDNO"));
                m.put("SCHREGNO", rs.getString("SCHREGNO"));
                m.put("SCHOOL_KIND", rs.getString("SCHOOL_KIND"));
                m.put("GRADE_HR_CLASS", rs.getString("GRADE_HR_CLASS"));
            }
        } catch (Exception ex) {
            log.error("[KNJA190]set_detail2 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;

    }   //set_detail2の括り

    /*----------------------------*
     * 生徒出力 2003/11/17        *
     *----------------------------*/
    private void printList(final Vrw32alp svf, final List list, final int flg) {
        try {
           /** 照会結果の取得とsvf_formへ出力 **/
            final String frmFile = "KNJA190W.frm";
            svf.VrSetForm(frmFile, 1);    //NO002

            int ia = 1;    //行
            final int iaMax = 6;
            int ib = 1;    //列
            final int ibMax = 2;
            boolean hasData = false;
            String beforeGhrClass = (String)(((Map)list.get(0)).get("GRADE_HR_CLASS"));
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                final String rsZipcd = (String) m.get("ZIPCD");
                final String rsAddr1 = (String) m.get("ADDR1");
                final String rsAddr2 = (String) m.get("ADDR2");
                final String rsName = (String) m.get("NAME");
                final String rsName2 = (String) m.get("NAME2");
                final String rsHrname = (String) m.get("HR_NAME");
                final String rsAttendno = (String) m.get("ATTENDNO");
                final String rsSchregno = (String) m.get("SCHREGNO");
                final String rsGradeHrClass = (String) m.get("GRADE_HR_CLASS"); //クラス毎改ページ用に使用

                if (2 == flg) {
                    if (!StringUtils.isBlank(rsZipcd) || null != rsAddr1 || null != rsAddr2 || null != rsName || null != rsHrname || null != rsAttendno) {
                        hasData = true;
                    }
                    if (_param._check2 != null && null != rsSchregno) {
                        hasData = true;
                    }
                } else if (1 == flg) {
                    if (!StringUtils.isBlank(rsZipcd) || null != rsAddr1 || null != rsAddr2 || null != rsName) {
                        hasData = true;
                    }
                    if (_param._check1 != null && (null != rsHrname || null != rsName2)) {
                        hasData = true;
                    }
                }

                //クラス毎に改ページ
                 if (!beforeGhrClass.contentEquals(rsGradeHrClass)) {
                    log.fatal("before:"+beforeGhrClass+" rsGradeHrClass:"+rsGradeHrClass);
                	if (hasData) {
                        svf.VrEndPage();
                        nonedata = true;
                	}
                	beforeGhrClass = rsGradeHrClass;
                	ia = 1;
                	ib = 1;
                }

                if(ib > ibMax){
                    ib = 1;
                    ia++;
                    if (ia > iaMax) {
                        if (hasData) {
                            svf.VrEndPage();
                            nonedata = true;
                        }
                        ia = 1;
                    }
                }

                //出力形態
                if("1".equals(_param._output3)) {
                    //1名1枚
                    if (!StringUtils.isBlank(rsZipcd)) {
                        svf.VrsOutn("ZIPCODE"    + ib, ia, "〒" + rsZipcd); //郵便番号
                    }
                    final int check_len = getMS932ByteLength(rsAddr1);
                    final int check_len2 = getMS932ByteLength(rsAddr2);
                    if (check_len > 50 || check_len2 > 50) {
                        svf.VrsOutn("ADDRESS" + ib + "_1_3" , ia, rsAddr1);     //住所
                        svf.VrsOutn("ADDRESS" + ib + "_2_3" , ia, rsAddr2);     //住所
                    } else if (check_len > 40 || check_len2 > 40) {
                        svf.VrsOutn("ADDRESS" + ib + "_1_2" , ia, rsAddr1);     //住所
                        svf.VrsOutn("ADDRESS" + ib + "_2_2" , ia, rsAddr2);     //住所
                    } else if (check_len > 0 || check_len2 > 0) {
                        svf.VrsOutn("ADDRESS" + ib + "_1_1" , ia, rsAddr1);     //住所
                        svf.VrsOutn("ADDRESS" + ib + "_2_1" , ia, rsAddr2);     //住所
                    }

                    final String name = null == rsName ? "" : rsName;
                    if (!StringUtils.isBlank(name)) {
                        svf.VrsOutn("NAME" + ib + "_1", ia, name + "　" + _param._sama);  //名称
                    }

                    final String hrname = StringUtils.defaultString(rsHrname);
                    if(_param._check3 != null) {
                        svf.VrsOutn("GUARD_NAME" + ib + "_1", ia, "保護者・保証人　様"); //保護者氏名
                    }
                    if(_param._check1 != null) {
                        svf.VrsOutn("HR_NAME" + ib, ia, hrname); //年組
                    }
                    if(_param._check2 != null) {
                        svf.VrsOutn("SCHREGNO" + ib, ia, ib+":"+rsSchregno); //学生番号
                    }
                    ib++;

                } else {
                    //1名1ページ
                    for (ia = 1; ia <= iaMax; ia++){
                        for (ib = 1; ib <= ibMax; ib++){
                            if (!StringUtils.isBlank(rsZipcd)) {
                                svf.VrsOutn("ZIPCODE"    + ib, ia, "〒" + rsZipcd); //郵便番号
                            }
                            final int check_len = getMS932ByteLength(rsAddr1);
                            final int check_len2 = getMS932ByteLength(rsAddr2);
                            if (check_len > 50 || check_len2 > 50) {
                                svf.VrsOutn("ADDRESS" + ib + "_1_3" , ia, rsAddr1);     //住所
                                svf.VrsOutn("ADDRESS" + ib + "_2_3" , ia, rsAddr2);     //住所
                            } else if (check_len > 40 || check_len2 > 40) {
                                svf.VrsOutn("ADDRESS" + ib + "_1_2" , ia, rsAddr1);     //住所
                                svf.VrsOutn("ADDRESS" + ib + "_2_2" , ia, rsAddr2);     //住所
                            } else if (check_len > 0 || check_len2 > 0) {
                                svf.VrsOutn("ADDRESS" + ib + "_1_1" , ia, rsAddr1);     //住所
                                svf.VrsOutn("ADDRESS" + ib + "_2_1" , ia, rsAddr2);     //住所
                            }

                            final String name = null == rsName ? "" : rsName;
                            if (!StringUtils.isBlank(name)) {
                                svf.VrsOutn("NAME" + ib + "_1", ia, name + "　" + _param._sama);  //名称
                            }

                            final String hrname = StringUtils.defaultString(rsHrname);
                            if(_param._check3 != null) {
                                svf.VrsOutn("GUARD_NAME" + ib + "_1", ia, "保護者・保証人　様"); //保護者氏名
                            }
                            if(_param._check1 != null) {
                                svf.VrsOutn("HR_NAME" + ib, ia, hrname); //年組
                            }
                            if(_param._check2 != null) {
                                svf.VrsOutn("SCHREGNO" + ib, ia, ib+":"+rsSchregno); //学生番号
                            }
                        }
                    }
                }
            }

            if (hasData) {
                svf.VrEndPage();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.error("[KNJA190]set_detail2 read error!", ex);
        }

    }   //set_detail2の括り

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 71114 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _output; // 1:生徒 2:その他(送付先)
        final String _output2; // 1:学籍番号 2:年組版
        final String _output3; // 1:1名1枚 2:1名1ページ
        final String _check1;
        final String _check2;
        final String _check3;
        final String _ctrlDate;
        final String _grdDiv;
        final String _choice; // 1:個人指定 2:クラス指定
        final String[] _classSelected;
        final String _prgId;
        final String _sama;
        final String _san;
        final String _schregInState;
        final String _semesterSdate;
        final String _semesterEdate;
        final String _hukusikiKirikae;
        final String _tableRegdDat;
        final String _tableRegdHDat;
        final String _useSpecial_Support_Hrclass;
        final String _useFi_Hrclass;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _prgId = request.getParameter("PRGID");
            _output = request.getParameter("OUTPUT");
            _output2 = request.getParameter("OUTPUT2");
            _output3 = request.getParameter("OUTPUT3");
            _check1 = request.getParameter("CHECK1");   // クラス名印刷
            _check2 = request.getParameter("CHECK2");   // 学籍番号印刷
            _check3 = request.getParameter("CHECK3");   // 保護者・保証人様出力
            _ctrlDate = request.getParameter("CTRL_DATE");   // 日付
            _grdDiv = request.getParameter("GRDDIV");   // 出力条件
            _choice = request.getParameter("CHOICE");
            //対象学籍番号の編集
            if ("3".equals(_choice)) {
                String[] _domiSelected = request.getParameterValues("category_name");   // 学籍番号
                List schListWk = getSchregnoListFromDomitory(db2, _domiSelected);
                _classSelected = (String[]) schListWk.toArray(new String[schListWk.size()]);   // 学籍番号
            } else {
                _classSelected = request.getParameterValues("category_name");   // 学籍番号
            }
            StringBuffer sbx = new StringBuffer();
            sbx.append("(");
            for (int ia = 0; ia < _classSelected.length; ia++){
                if (_classSelected[ia] == null) {
                    break;
                }
                if (ia > 0) {
                    sbx.append(",");
                }
                sbx.append("'");
                int i = _classSelected[ia].indexOf("-");
                if (-1 < i) {
                    sbx.append(_classSelected[ia].substring(0,i));
                } else {
                    sbx.append(_classSelected[ia]);
                }
                sbx.append("'");
            }
            sbx.append(")");
            _schregInState = sbx.toString();

            KNJ_Semester semester = new KNJ_Semester();                     //クラスのインスタンス作成
            KNJ_Semester.ReturnVal returnval = semester.Semester(db2, _year, _semester);
            _semesterSdate = returnval.val2;                                          //学期開始日
            _semesterEdate = returnval.val3;                                          //学期終了日

            _hukusikiKirikae = request.getParameter("HUKUSIKI_KIRIKAE");
            _useSpecial_Support_Hrclass = request.getParameter("useSpecial_Support_Hrclass");
            _useFi_Hrclass = request.getParameter("useFi_Hrclass");
            if ("2".equals(_hukusikiKirikae) && "1".equals(_useSpecial_Support_Hrclass)) {
                _tableRegdDat = "SCHREG_REGD_GHR_DAT";
                _tableRegdHDat = "SCHREG_REGD_GHR_HDAT";
            } else if ("2".equals(_hukusikiKirikae) && "1".equals(_useFi_Hrclass)) {
                _tableRegdDat = "SCHREG_REGD_FI_DAT";
                _tableRegdHDat = "SCHREG_REGD_FI_HDAT";
            } else {
                _tableRegdDat = "SCHREG_REGD_DAT";
                _tableRegdHDat = "SCHREG_REGD_HDAT";
            }

            _sama = "様";
            _san = "さん";
        }

        /**
         * 中高一貫か?
         * @param db2 DB2UDB
         * @return 中高一貫ならtrue
         */
        private String getZ010(final DB2UDB db2, final String field) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT " + field + " FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2 = '00'");
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private List getSchregnoListFromDomitory(final DB2UDB db2, final String[] selDomiCd) {
            List retList = new ArrayList();
            if (selDomiCd.length == 0) {
                return retList;
            }

            String dateStr = _ctrlDate.replace('/', '-');
            String SrchDomiCds = "";
            String delimStr = "";
            for (int ii = 0; ii < selDomiCd.length; ii++) {
                SrchDomiCds += delimStr + " '" + selDomiCd[ii] + "'";
                delimStr = ",";
            }

            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("  T1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   INNER JOIN SCHREG_DOMITORY_HIST_DAT T2 ");
            stb.append("     ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND ((DOMI_ENTDAY <= DATE('" + dateStr + "') AND DOMI_OUTDAY IS NULL) ");
            stb.append("          OR DATE('" + dateStr + "') BETWEEN DOMI_ENTDAY AND DOMI_OUTDAY) ");
            stb.append("   INNER JOIN SCHREG_BASE_MST T3 ");
            stb.append("     ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _year + "' ");
            stb.append("   AND T1.SEMESTER = '" + _semester + "' ");
            stb.append("   AND T2.DOMI_CD IN (" + SrchDomiCds + ") ");
            stb.append(" ORDER BY ");
            if ("2".equals(_output2)) {
                stb.append(" T1.GRADE, ");
                stb.append(" T1.HR_CLASS, ");
                stb.append(" T1.ATTENDNO ");
            } else {
                stb.append(" T1.SCHREGNO ");
            }
            log.debug(" getSchregnoListFromDomitory sql = " + stb.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!"".equals(StringUtils.defaultString(rs.getString("SCHREGNO"), ""))) {
                        retList.add(rs.getString("SCHREGNO"));
                    }
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return retList;
        }
    }
}
