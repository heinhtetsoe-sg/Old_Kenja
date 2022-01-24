// kanji=漢字
/*
 * $Id: de8c1ce6d075b223ba0d93a5fff3a3ac66ac46f6 $
 *
 * 作成日: 2003/11/12 18:16:28 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2003-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.OutputStream;
import java.util.Enumeration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                     ＜ＫＮＪＡ２５０＞ 異動情報一覧
 *
 * 2003/11/12 nakamoto 和暦変換に対応
 * 2004/01/30 nakamoto schreg_base_mstに卒業事由を追加による修正
 * 2005/03/29 nakamoto 出停・編入を追加---NO001
 * 2005/10/22 m-yama   移動テーブル以外の出力項目を変更---NO002
 * 2005/10/22 m-yama   転入を追加---NO003
 * 2006/03/20 m-yama   異動終了日付指定追加---NO004
 **/

public class KNJA250 extends HttpServlet {
    boolean nonedata;               // 該当データなしフラグ

    private static final Log log = LogFactory.getLog(KNJA250.class);

    Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

    //  print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

    //  svf設定
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);        //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }

        _param = createParam(db2, request);

    //  ＳＶＦ作成処理
        nonedata = false;       // 該当データなしフラグ(MES001.frm出力用)
        PreparedStatement ps1 = null;
        //SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1());       //生徒及び公欠・欠席者
        } catch( Exception ex ) {
            log.warn("DB2 open error!");
        }
        set_detail(db2, svf, ps1);
    //  該当データ無し
        if(nonedata == false){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

        //  終了処理
        svf.VrQuit();
        Pre_Stat_f(ps1);            //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる

    }//doGetの括り


    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 69509 $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _date;
        private final String _output;
        private final String _inStateGakunen;
        private final String _inStateGrdDiv;
        private final String _inStateTrancefer;
        private final String _inStateEntDiv;
        private final String _tenkaI;
        private final String _tenkaO;
        private final String _tensekiI;
        private final String _tensekiO;
        private final String _printDate;
        private String _nendoSdate;
        private String _nendoEdate;
        private String _z010 = "";
        private String _z012 = "";
        private final boolean _isSeireki;
        private final KNJDefineSchool _definecode;
        private final String _useAddrField2;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {

            try {
                _year = request.getParameter("YEAR");
                _semester = request.getParameter("CTRL_SEMESTER");
                _date = request.getParameter("DATE");
                _output = request.getParameter("OUTPUT");
                KNJ_Control control = new KNJ_Control();                            //クラスのインスタンス作成
                KNJ_Control.ReturnVal returnval = control.Control(db2);
                _printDate = returnval.val3;

                String gnen[] = request.getParameterValues("GAKUNEN");
                int i = 0;
                final StringBuffer stbGakunen = new StringBuffer();
                stbGakunen.append("(");
                while(i < gnen.length){
                    if(gnen[i] == null ) break;
                    if(i > 0) stbGakunen.append(",");
                    stbGakunen.append("'" + gnen[i] + "'");
                    i++;
                }
                stbGakunen.append(")");
                _inStateGakunen = stbGakunen.toString();

                String tran1[] = new String[8];
                tran1[1] = request.getParameter("GRAD");       //卒業
                tran1[2] = request.getParameter("DROP");       //退学
                tran1[3] = request.getParameter("MOVE");       //転学
                tran1[6] = request.getParameter("REMOVE");     //除籍
                tran1[7] = request.getParameter("TENSEKI_O");  //転籍
                final StringBuffer stbGrdDiv = new StringBuffer();
                String grdSep = "";
                stbGrdDiv.append("('");
                for (int ia = 0; ia < tran1.length; ia++ ) {
                    if (tran1[ia] != null) {
                        if (tran1[ia].equals("on")) {
                            stbGrdDiv.append(grdSep + ia);
                            grdSep = "','";
                        }
                    }
                }
                stbGrdDiv.append("')");
                _inStateGrdDiv = stbGrdDiv.toString();

                String tran[] = new String[4];
                tran[1] = request.getParameter("FOREIGN");  //留学
                tran[2] = request.getParameter("HOLI");     //休学
                tran[3] = request.getParameter("SUSPEND");  //出停
                final StringBuffer stbTrancefer = new StringBuffer();
                String tranceSep = "";
                stbTrancefer.append("('");
                for (int ia = 0; ia < tran.length; ia++) {
                    if (tran[ia] != null) {
                        if( tran[ia].equals("on") ) {
                            stbTrancefer.append(tranceSep + ia);
                            tranceSep = "','";
                        }
                    }
                }
                stbTrancefer.append("')");
                _inStateTrancefer = stbTrancefer.toString();

                //  対象異動情報の編集
                String tran2[] = new String[8];
                tran2[4] = request.getParameter("MOVINGIN");    //転入
                tran2[5] = request.getParameter("ADMISSION");   //編入
                tran2[7] = request.getParameter("TENSEKI_I");   //転籍
                final StringBuffer stbEntDiv = new StringBuffer();
                String entSep = "";
                stbEntDiv.append("('");
                for (int ia = 0; ia < tran2.length; ia++) {
                    if (tran2[ia] != null) {
                        if (tran2[ia].equals("on")) {
                            stbEntDiv.append(entSep + ia);
                            entSep = "','";
                        }
                    }
                }
                stbEntDiv.append("')");
                _inStateEntDiv = stbEntDiv.toString();

                _tenkaI = request.getParameter("TENKA_I");   //転科(入)
                _tenkaO = request.getParameter("TENKA_O");   //転科(出)
                _tensekiI = request.getParameter("TENSEKI_I");   //転籍(入)
                _tensekiO = request.getParameter("TENSEKI_O");   //転籍(出)

                _nendoSdate = _year + "-04-01";
                final int nextYear = Integer.parseInt(_year) + 1;
                _nendoEdate = String.valueOf(nextYear) + "-03-31";

                _z010 = setNameMst(db2, "Z010", "00");
                _z012 = setNameMst(db2, "Z012", "01");
                _isSeireki = _z012.equals("1") ? false : true;
                _definecode = new KNJDefineSchool();
                _definecode.setSchoolCode(db2, _year);
                _useAddrField2 = request.getParameter("useAddrField2");

            } finally {
                db2.commit();
            }
        }

        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2) throws SQLException {
            String rtnSt = "";
            db2.query(getNameMst(_year, namecd1, namecd2));
            ResultSet rs = db2.getResultSet();
            while (rs.next()) {
                rtnSt = rs.getString("NAME1");
            }
            return rtnSt;
        }

        private String getNameMst(final String year, final String namecd1, final String namecd2) {
            final String rtnSql = " SELECT "
                                + "     * "
                                + " FROM "
                                + "     V_NAME_MST "
                                + " WHERE "
                                + "     YEAR = '" + year + "' "
                                + "     AND NAMECD1 = '" + namecd1 + "' "
                                + "     AND NAMECD2 = '" + namecd2 + "'";
            return rtnSql;
        }

        private String changePrintDate(final String date) {
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(date);
            }
        }
    }


    /*----------------------------*
     * 異動情報明細出力           *
     *----------------------------*/
    public void set_detail(DB2UDB db2,Vrw32alp svf, PreparedStatement ps1)
    {
        try {
            final String form;
            if (_param._definecode.schoolmark.substring(0, 1).equals("K")) {
                form = "KNJA250K.frm";
            } else {
                form = "KNJA250.frm";
            }
            svf.VrSetForm(form, 4);      //SuperVisualFormadeで設計したレイアウト定義態の設定
            svf.VrsOut("nendo"    , KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度");
            svf.VrsOut("TODAY"    , KNJ_EditDate.h_format_JP(db2, _param._printDate));

            ResultSet rs = ps1.executeQuery();
            while( rs.next() ){
                svf.VrsOut("HR_NAME1"     , rs.getString("HR_NAME"));
                svf.VrsOut("STAFFNAME"    , rs.getString("STAFF_NAME"));
                svf.VrsOut("ATTENDNO"     , rs.getString("ATTENDNO"));
                svf.VrsOut("SCHREGNO"     , rs.getString("SCHREGNO"));
                svf.VrsOut("NAME_SHOW"    , rs.getString("NAME"));
                svf.VrsOut("TRANSFER"     , rs.getString("TRANSFERNAME"));
                svf.VrsOut("DATE_S"       , KNJ_EditDate.h_format_JP(db2, rs.getString("TRANSFER_SDATE")));
                svf.VrsOut("DATE_F"       , KNJ_EditDate.h_format_JP(db2, rs.getString("TRANSFER_EDATE")));
                svf.VrsOut("REASON"       , rs.getString("TRANSFERREASON"));
                svf.VrsOut("PLACE1"       , rs.getString("TRANSFERPLACE"));
                if ("1".equals(_param._useAddrField2) && getMS932ByteLength(rs.getString("TRANSFERADDR")) > 26 * 2) {
                    svf.VrsOut("ADDRESS1_2"   , rs.getString("TRANSFERADDR"));
                } else {
                    svf.VrsOut("ADDRESS1"     , rs.getString("TRANSFERADDR"));
                }
                svf.VrsOut("REMARK"       , rs.getString("REMARK"));
                svf.VrEndRecord();
                nonedata = true; //該当データなしフラグ
            }
            rs.close();
            db2.commit();
            log.debug("[KNJA250]set_detail read ok!");
        } catch( Exception ex ){
            log.error("[KNJA250]set_detail read error!", ex);
        }

    }//set_detailの括り

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
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

    //SQL作成
    String Pre_Stat1()
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT ");
            stb.append("    T1.GRADE, ");
            stb.append("    T1.HR_CLASS, ");
            stb.append("    T1.ATTENDNO, ");
            stb.append("    T5.HR_NAME, ");
            stb.append("    T2.NAME_SHOW AS NAME, ");
            stb.append("    T3.SCHREGNO, ");
            stb.append("    T3.TRANSFERCD, ");
            stb.append("    T3.NAME1 AS TRANSFERNAME, ");
            stb.append("    T3.TRANSFER_SDATE, ");
            stb.append("    T3.TRANSFER_EDATE, ");
            stb.append("    T3.TRANSFERPLACE, ");
            stb.append("    T3.TRANSFERADDR, ");
            stb.append("    T3.TRANSFERREASON, ");
            stb.append("    T6.STAFFNAME_SHOW AS STAFF_NAME, ");
            stb.append("    CASE WHEN COMEBACK.SCHREGNO IS NOT NULL ");
            stb.append("         THEN '復学' ");
            stb.append("         ELSE '' ");
            stb.append("    END AS REMARK ");
            stb.append("FROM ");
            stb.append("    ( ");
            stb.append("        SELECT ");
            stb.append("            TT3.SCHREGNO, ");
            stb.append("            TT3.TRANSFERCD, ");
            stb.append("            CHAR(TT3.TRANSFER_SDATE) AS TRANSFER_SDATE, ");
            stb.append("            CHAR(TT3.TRANSFER_EDATE) AS TRANSFER_EDATE, ");
            stb.append("            TT3.TRANSFERPLACE, ");
            stb.append("            TT3.TRANSFERADDR, ");
            stb.append("            TT3.TRANSFERREASON, ");
            stb.append("            TT4.NAME1 ");
            stb.append("        FROM ");
            stb.append("            SCHREG_TRANSFER_DAT TT3 ");
            stb.append("            LEFT JOIN NAME_MST TT4 ON (TT3.TRANSFERCD = TT4.NAMECD2 AND TT4.NAMECD1 = 'A004') ");
            stb.append("        WHERE ");
            stb.append("            TT3.TRANSFERCD IN " + _param._inStateTrancefer +  " ");
            if (null != _param._output && null != _param._date) {
                stb.append("            AND TT3.TRANSFER_EDATE = '" + _param._date.replace('/', '-') + "' ");
            } else {
                stb.append("            AND (TT3.TRANSFER_SDATE BETWEEN '" + _param._nendoSdate + "' AND '" + _param._nendoEdate + "' ");
                stb.append("            OR  TT3.TRANSFER_EDATE BETWEEN '" + _param._nendoSdate + "' AND '" + _param._nendoEdate + "' ");
                stb.append("            OR  TT3.TRANSFER_SDATE <= '" + _param._nendoSdate + "' AND TT3.TRANSFER_EDATE >='" + _param._nendoEdate + "') ");
            }
            stb.append("        UNION ");
            stb.append("        SELECT ");
            stb.append("            TT1.SCHREGNO, ");
            stb.append("            TT1.GRD_DIV AS TRANSFERCD, ");
            stb.append("            CHAR(TT1.GRD_DATE) AS TRANSFER_SDATE, ");
            stb.append("            '' AS TRANSFER_EDATE, ");
            stb.append("            TT1.GRD_SCHOOL AS TRANSFERPLACE, ");    //NO002
            if ("1".equals(_param._useAddrField2)) {
                stb.append("            CASE WHEN TT1.GRD_ADDR IS NULL AND TT1.GRD_ADDR2 IS NULL THEN CAST(NULL AS VARCHAR(1)) ");
                stb.append("                 ELSE  VALUE(TT1.GRD_ADDR, '') || VALUE(TT1.GRD_ADDR2, '') ");
                stb.append("            END AS TRANSFERADDR, ");       //NO002
            } else {
                stb.append("            TT1.GRD_ADDR AS TRANSFERADDR, ");       //NO002
            }
            stb.append("            TT1.GRD_REASON AS TRANSFERREASON, ");
            stb.append("            TT2.NAME1 ");
            stb.append("        FROM ");
            stb.append("            SCHREG_BASE_MST TT1 ");
            stb.append("            LEFT JOIN NAME_MST TT2 ON (TT1.GRD_DIV = TT2.NAMECD2 AND TT2.NAMECD1 = 'A003') ");
            stb.append("        WHERE ");
            stb.append("            TT1.GRD_DIV     IN " + _param._inStateGrdDiv +  " ");
            stb.append("            AND TT1.GRD_DATE BETWEEN '" + _param._nendoSdate + "' AND '" + _param._nendoEdate + "' ");
            stb.append("        UNION ");
            stb.append("        SELECT ");
            stb.append("            TT1.SCHREGNO, ");
            stb.append("            TT1.ENT_DIV AS TRANSFERCD, ");
            stb.append("            CHAR(TT1.ENT_DATE) AS TRANSFER_SDATE, ");
            stb.append("            '' AS TRANSFER_EDATE, ");
            stb.append("            TT1.ENT_SCHOOL AS TRANSFERPLACE, ");    //NO002
            if ("1".equals(_param._useAddrField2)) {
                stb.append("            CASE WHEN TT1.ENT_ADDR IS NULL AND TT1.ENT_ADDR2 IS NULL THEN CAST(NULL AS VARCHAR(1)) ");
                stb.append("                 ELSE  VALUE(TT1.ENT_ADDR, '') || VALUE(TT1.ENT_ADDR2, '') ");
                stb.append("            END AS TRANSFERADDR, ");       //NO002
            } else {
                stb.append("            TT1.ENT_ADDR AS TRANSFERADDR, ");       //NO002
            }
            stb.append("            TT1.ENT_REASON AS TRANSFERREASON, ");
            stb.append("            TT2.NAME1  ");
            stb.append("        FROM  ");
            stb.append("            SCHREG_BASE_MST TT1  ");
            stb.append("            LEFT JOIN NAME_MST TT2 ON (TT1.ENT_DIV = TT2.NAMECD2 AND TT2.NAMECD1 = 'A002')  ");
            stb.append("        WHERE  ");
            stb.append("            TT1.ENT_DIV     IN " + _param._inStateEntDiv +  "  ");
            stb.append("            AND TT1.ENT_DATE BETWEEN '" + _param._nendoSdate + "' AND '" + _param._nendoEdate + "'  ");
            if ("on".equals(_param._tenkaO)) {
                stb.append("        UNION ");
                stb.append("        SELECT ");
                stb.append("            TT1.SCHREGNO, ");
                stb.append("            '' AS TRANSFERCD, ");
                stb.append("            '' AS TRANSFER_SDATE, ");
                stb.append("            CHAR(LHIST.EXPIREDATE) AS TRANSFER_EDATE, ");
                stb.append("            MAJOR.MAJORNAME AS TRANSFERPLACE, ");
                stb.append("            '' AS TRANSFERADDR, ");
                stb.append("            '' AS TRANSFERREASON, ");
                stb.append("            '転科' AS NAME1  ");
                stb.append("        FROM  ");
                stb.append("            SCHREG_BASE_MST TT1,  ");
                stb.append("            (SELECT ");
                stb.append("                 I1.SCHREGNO, ");
                stb.append("                 MAX(I1.ISSUEDATE) AS ISSUEDATE ");
                stb.append("             FROM ");
                stb.append("                 SCHREG_BASE_HIST_DAT I1 ");
                stb.append("             WHERE ");
                stb.append("                 I1.ISSUEDATE BETWEEN '" + _param._nendoSdate + "' AND '" + _param._nendoEdate + "' ");
                stb.append("                 AND I1.MAJORCD_FLG = '1' ");
                stb.append("             GROUP BY ");
                stb.append("                 I1.SCHREGNO ");
                stb.append("            ) TT2 ");
                stb.append("            LEFT JOIN SCHREG_BASE_HIST_DAT LHIST ON TT2.SCHREGNO = LHIST.SCHREGNO ");
                stb.append("                 AND TT2.ISSUEDATE = LHIST.ISSUEDATE ");
                stb.append("            LEFT JOIN MAJOR_MST MAJOR ON LHIST.COURSECD = MAJOR.COURSECD ");
                stb.append("                 AND LHIST.MAJORCD = MAJOR.MAJORCD ");
                stb.append("        WHERE  ");
                stb.append("            TT1.SCHREGNO = TT2.SCHREGNO ");
            }
            if ("on".equals(_param._tenkaI)) {
                stb.append("        UNION ");
                stb.append("        SELECT ");
                stb.append("            TT1.SCHREGNO, ");
                stb.append("            '' AS TRANSFERCD, ");
                stb.append("            char(LHIST.EXPIREDATE + 1 day) AS TRANSFER_SDATE, ");
                stb.append("            '' AS TRANSFER_EDATE, ");
                stb.append("            MAJOR.MAJORNAME AS TRANSFERPLACE, ");
                stb.append("            '' AS TRANSFERADDR, ");
                stb.append("            '' AS TRANSFERREASON, ");
                stb.append("            '転科' AS NAME1  ");
                stb.append("        FROM  ");
                stb.append("            SCHREG_BASE_MST TT1,  ");
                stb.append("            (SELECT ");
                stb.append("                 I1.SCHREGNO, ");
                stb.append("                 MAX(I1.ISSUEDATE) AS ISSUEDATE ");
                stb.append("             FROM ");
                stb.append("                 SCHREG_BASE_HIST_DAT I1 ");
                stb.append("             WHERE ");
                stb.append("                 I1.ISSUEDATE BETWEEN '" + _param._nendoSdate + "' AND '" + _param._nendoEdate + "' ");
                stb.append("                 AND I1.MAJORCD_FLG = '1' ");
                stb.append("             GROUP BY ");
                stb.append("                 I1.SCHREGNO ");
                stb.append("            ) TT2 ");
                stb.append("            LEFT JOIN SCHREG_BASE_HIST_DAT LHIST ON TT2.SCHREGNO = LHIST.SCHREGNO ");
                stb.append("                 AND TT2.ISSUEDATE = LHIST.ISSUEDATE ");
                stb.append("            LEFT JOIN SCHREG_REGD_DAT REGD ON TT2.SCHREGNO = REGD.SCHREGNO ");
                stb.append("                 AND REGD.YEAR = '" + _param._year + "' ");
                stb.append("                 AND REGD.SEMESTER = '" + _param._semester + "' ");
                stb.append("            LEFT JOIN MAJOR_MST MAJOR ON REGD.COURSECD = MAJOR.COURSECD ");
                stb.append("                 AND REGD.MAJORCD = MAJOR.MAJORCD ");
                stb.append("        WHERE  ");
                stb.append("            TT1.SCHREGNO IN ( ");
                stb.append("                           SELECT ");
                stb.append("                               I1.SCHREGNO ");
                stb.append("                           FROM ");
                stb.append("                               SCHREG_BASE_HIST_DAT I1 ");
                stb.append("                           WHERE ");
                stb.append("                               I1.ISSUEDATE BETWEEN '" + _param._nendoSdate + "' AND '" + _param._nendoEdate + "' ");
                stb.append("                               AND I1.MAJORCD_FLG = '1' ");
                stb.append("                           ) ");
            }
            if ("on".equals(_param._tensekiO)) {
                stb.append("        UNION ");
                stb.append("        SELECT ");
                stb.append("            TT1.SCHREGNO, ");
                stb.append("            '' AS TRANSFERCD, ");
                stb.append("            '' AS TRANSFER_SDATE, ");
                stb.append("            CHAR(LHIST.EXPIREDATE) AS TRANSFER_EDATE, ");
                stb.append("            COURSE.COURSENAME AS TRANSFERPLACE, ");
                stb.append("            '' AS TRANSFERADDR, ");
                stb.append("            '' AS TRANSFERREASON, ");
                stb.append("            '転籍' AS NAME1  ");
                stb.append("        FROM  ");
                stb.append("            SCHREG_BASE_MST TT1,  ");
                stb.append("            (SELECT ");
                stb.append("                 I1.SCHREGNO, ");
                stb.append("                 MAX(I1.ISSUEDATE) AS ISSUEDATE ");
                stb.append("             FROM ");
                stb.append("                 SCHREG_BASE_HIST_DAT I1 ");
                stb.append("             WHERE ");
                stb.append("                 I1.ISSUEDATE BETWEEN '" + _param._nendoSdate + "' AND '" + _param._nendoEdate + "' ");
                stb.append("                 AND I1.COURSECD_FLG = '1' ");
                stb.append("             GROUP BY ");
                stb.append("                 I1.SCHREGNO ");
                stb.append("            ) TT2 ");
                stb.append("            LEFT JOIN SCHREG_BASE_HIST_DAT LHIST ON TT2.SCHREGNO = LHIST.SCHREGNO ");
                stb.append("                 AND TT2.ISSUEDATE = LHIST.ISSUEDATE ");
                stb.append("            LEFT JOIN COURSE_MST COURSE ON LHIST.COURSECD = COURSE.COURSECD ");
                stb.append("        WHERE  ");
                stb.append("            TT1.SCHREGNO = TT2.SCHREGNO ");
            }
            if ("on".equals(_param._tensekiI)) {
                stb.append("        UNION ");
                stb.append("        SELECT ");
                stb.append("            TT1.SCHREGNO, ");
                stb.append("            '' AS TRANSFERCD, ");
                stb.append("            char(LHIST.EXPIREDATE + 1 day) AS TRANSFER_SDATE, ");
                stb.append("            '' AS TRANSFER_EDATE, ");
                stb.append("            COURSE.COURSENAME AS TRANSFERPLACE, ");
                stb.append("            '' AS TRANSFERADDR, ");
                stb.append("            '' AS TRANSFERREASON, ");
                stb.append("            '転籍' AS NAME1  ");
                stb.append("        FROM  ");
                stb.append("            SCHREG_BASE_MST TT1,  ");
                stb.append("            (SELECT ");
                stb.append("                 I1.SCHREGNO, ");
                stb.append("                 MAX(I1.ISSUEDATE) AS ISSUEDATE ");
                stb.append("             FROM ");
                stb.append("                 SCHREG_BASE_HIST_DAT I1 ");
                stb.append("             WHERE ");
                stb.append("                 I1.ISSUEDATE BETWEEN '" + _param._nendoSdate + "' AND '" + _param._nendoEdate + "' ");
                stb.append("                 AND I1.COURSECD_FLG = '1' ");
                stb.append("             GROUP BY ");
                stb.append("                 I1.SCHREGNO ");
                stb.append("            ) TT2 ");
                stb.append("            LEFT JOIN SCHREG_BASE_HIST_DAT LHIST ON TT2.SCHREGNO = LHIST.SCHREGNO ");
                stb.append("                 AND TT2.ISSUEDATE = LHIST.ISSUEDATE ");
                stb.append("            LEFT JOIN SCHREG_REGD_DAT REGD ON TT2.SCHREGNO = REGD.SCHREGNO ");
                stb.append("                 AND REGD.YEAR = '" + _param._year + "' ");
                stb.append("                 AND REGD.SEMESTER = '" + _param._semester + "' ");
                stb.append("            LEFT JOIN COURSE_MST COURSE ON REGD.COURSECD = COURSE.COURSECD ");
                stb.append("        WHERE  ");
                stb.append("            TT1.SCHREGNO IN ( ");
                stb.append("                           SELECT ");
                stb.append("                               I1.SCHREGNO ");
                stb.append("                           FROM ");
                stb.append("                               SCHREG_BASE_HIST_DAT I1 ");
                stb.append("                           WHERE ");
                stb.append("                               I1.ISSUEDATE BETWEEN '" + _param._nendoSdate + "' AND '" + _param._nendoEdate + "' ");
                stb.append("                               AND I1.COURSECD_FLG = '1' ");
                stb.append("                           ) ");
            }
            stb.append("    )    T3  ");
            stb.append("    INNER JOIN ( ");
            stb.append("                SELECT  ");
            stb.append("                    ST1.YEAR, ");
            stb.append("                    ST1.SCHREGNO, ");
            stb.append("                    ST1.SEMESTER, ");
            stb.append("                    ST1.GRADE, ");
            stb.append("                    ST1.HR_CLASS, ");
            stb.append("                    ST1.ATTENDNO  ");
            stb.append("                FROM  ");
            stb.append("                    SCHREG_REGD_DAT ST1, ");
            stb.append("                    ( ");
            stb.append("                        SELECT  ");
            stb.append("                            SCHREGNO, ");
            stb.append("                            MAX(SEMESTER) AS SEMESTER  ");
            stb.append("                        FROM  ");
            stb.append("                            SCHREG_REGD_DAT  ");
            stb.append("                        WHERE  ");
            stb.append("                            YEAR = '" + _param._year + "'  ");
            stb.append("                            GROUP BY  ");
            stb.append("                            SCHREGNO  ");
            stb.append("                    )ST2  ");
            stb.append("                WHERE  ");
            stb.append("                    ST1.SCHREGNO = ST2.SCHREGNO  ");
            stb.append("                    AND ST1.SEMESTER = ST2.SEMESTER  ");
            stb.append("                    AND ST1.YEAR = '" + _param._year + "'  ");
            stb.append("    )T1 ON (T1.SCHREGNO = T3.SCHREGNO)  ");
            stb.append("    INNER JOIN SCHREG_BASE_MST   T2 ON (T1.SCHREGNO = T2.SCHREGNO)  ");
            stb.append("    INNER JOIN SCHREG_REGD_HDAT T5 ON (T5.GRADE = T1.GRADE AND T5.HR_CLASS = T1.HR_CLASS  ");
            stb.append("    AND T5.SEMESTER = T1.SEMESTER AND T5.YEAR = T1.YEAR)  ");
            stb.append("    LEFT JOIN STAFF_MST         T6 ON (T5.TR_CD1 = T6.STAFFCD)  ");
            stb.append("    LEFT JOIN SCHREG_REGD_GDAT REGG ON T1.YEAR = REGG.YEAR ");
            stb.append("         AND T1.GRADE = REGG.GRADE ");
            stb.append("    LEFT JOIN (SELECT DISTINCT LC.SCHREGNO, LC.SCHOOL_KIND FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT LC) COMEBACK ON T3.SCHREGNO = COMEBACK.SCHREGNO ");
            stb.append("         AND REGG.SCHOOL_KIND = COMEBACK.SCHOOL_KIND ");
            stb.append("WHERE  ");
            stb.append("    T1.YEAR = '" + _param._year + "'  ");
            stb.append("    AND T1.GRADE IN " + _param._inStateGakunen + "  ");
            stb.append("ORDER BY T1.GRADE,T1.HR_CLASS,T1.ATTENDNO ");

        } catch( Exception e ){
            log.warn("Pre_Stat1 error!");
        }
log.debug(stb);
        return stb.toString();

    }//Pre_Stat3()の括り

    /**PrepareStatement close**/
    private void Pre_Stat_f(PreparedStatement ps1)
    {
        try {
        	if (ps1 != null)
        		ps1.close();
        } catch( Exception e ){
            log.warn("Pre_Stat_f error!");
        }
    }//Pre_Stat_f()の括り

}//クラスの括り

