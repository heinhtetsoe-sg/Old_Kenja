// kanji=漢字
/*
 * $Id: 2ee38838817bae4c317999fefcbcf70c7556c7f4 $
 *
 * 作成日: 2008/05/24 10:02:00 - JST
 * 作成者: nakasone
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * 日別入金一覧
 * @author nakasone
 * @version $Id: 2ee38838817bae4c317999fefcbcf70c7556c7f4 $
 */
public class KNJWP132 {
    private static final String FORM_NAME = "KNJWP132.frm";
    private boolean _hasData;
    Param _param;
    // ページ最大行数
    private static final int PAGE_MAX_LINE = 50;
    // 種別：仮想口座
    private static final String VALUE_KASOU_KOUZA = "01";
    private static final String KASOU_KOUZA_NAME = "仮想口座";
    // 種別：信販
    private static final String VALUE_SINPAN = "02";
    private static final String SINPAN_NAME = "信販";
    // 種別：現金
    private static final String VALUE_GENKIN = "03";
    private static final String GENKIN_NAME = "現金";
    // 種別：その他
    private static final String VALUE_SONOTA = "04";
    private static final String SONOTA_NAME = "その他";

    HashMap hSerchDate = new HashMap();

    private static final Log log = LogFactory.getLog(KNJWP132.class);
    
    /**
     * KNJW.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);
            
            _hasData = false;

            printMain(db2, svf);
            
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            close(db2, svf);
        }

    }

    /**
     * 印刷処理メイン
     * @param db2   ＤＢ接続オブジェクト
     * @param svf   帳票オブジェクト
     * @throws Exception
     */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        
        svf.VrSetForm(FORM_NAME, 1);

        // 日付範囲の取得を行う
        
        // 日別入金データ取得
        final List student = createStudents(db2);
        // 帳票出力のメソッド
        outPutPrint(db2, svf, student);
            
    }

    /**
     * 帳票出力処理
     * @param db2       ＤＢ接続オブジェクト
     * @param svf       帳票オブジェクト
     * @param student   帳票出力対象クラスオブジェクト
     */
    private void outPutPrint(final DB2UDB db2, final Vrw32alp svf, final List student) throws Exception {

        boolean retflg = false; // 対象データ存在フラグ
        String breakPaymentDate = null;                     
        int gyo = 1;                // 現在ページ数の判断用（行）
        int pagecnt = 1;            // 現在ページ数
        int reccnt = 0;             // 合計レコード数
        String total_page = "";
        
        for (Iterator it = student.iterator(); it.hasNext();) {
            final student sudent = (student) it.next();

            // 入金日付のブレイク                           
            if(breakPaymentDate == null || !breakPaymentDate.equals(sudent._payment_date)){

                if(breakPaymentDate != null){
                    //50行超えた場合
                    if (gyo > PAGE_MAX_LINE) {
                        svf.VrsOut("TOTALPAGE"  ,total_page);
                        // ページ出力処理
                        svf.VrEndPage();
                        gyo = 1;
                        ++pagecnt;
                        svf.VrsOut("BELONGING_NAME", _param._belongingName);
                        svf.VrsOut("DATE1"  ,KNJ_EditDate.h_format_JP(breakPaymentDate));   // 発生日付
                        svf.VrsOut("DATE2"  ,KNJ_EditDate.getNowDateWa(true));              // 作成日時
                        svf.VrsOut("TOTALPAGE"  ,total_page);
                        svf.VrsOut("PAGE"   ,String.valueOf(pagecnt));
                        svf.VrsOutn("NAME"  , gyo, "合　　計");
                        svf.VrsOutn("PATMENT_MONEY"     , gyo, String.valueOf(_param.total_peyment_money));
                        // 改ページ処理
                        svf.VrEndPage();
                        _param.total_peyment_money = 0;
                        gyo = 1;
                    } else {
                        svf.VrsOutn("NAME"  , gyo, "合　　計");
                        svf.VrsOutn("PATMENT_MONEY"     , gyo, String.valueOf(_param.total_peyment_money));
                        // 改ページ処理
                        svf.VrEndPage();
                        gyo = 1;
                        _param.total_peyment_money = 0;
                    }
                }
                pagecnt = 1;
            } else {
                if(breakPaymentDate != null){
                    //50行超えた場合改ページを行う
                    if (gyo > PAGE_MAX_LINE) {
                        svf.VrsOut("TOTALPAGE"  ,total_page);
                        // 改ページ処理
                        svf.VrEndPage();
                        gyo = 1;
                        ++pagecnt;
                    } 
                }
            }
            
            if(gyo == 1){
                // 総ページ数取得・設定
                total_page = getTotalPage(db2, svf, sudent._payment_date);
                //*================*
                //* ヘッダ・フッタ *
                //*================*
                svf.VrsOut("BELONGING_NAME", _param._belongingName);
                svf.VrsOut("DATE1"  ,KNJ_EditDate.h_format_JP(sudent._payment_date));   // 発生日付
                svf.VrsOut("DATE2"  ,KNJ_EditDate.getNowDateWa(true));                  // 作成日時
                svf.VrsOut("PAGE"   ,String.valueOf(pagecnt));                          // 現在ページ数
                svf.VrsOut("TOTALPAGE"  ,total_page);
            }
            //*================*
            //* 明細           *
            //*================*
            // 現在ページ
            svf.VrsOutn("PAGE"  , gyo, String.valueOf(pagecnt));
            // 志願者番号
            svf.VrsOutn("APPLICANTNO"   , gyo, sudent._applicantno);
            // 学籍番号
            svf.VrsOutn("SCHREGNO"  , gyo, sudent._schregno);
            // 氏名
            if(!sudent._schregno.equals("")){
                // 学籍番号がある場合、学籍基礎マスタの氏名を設定
                svf.VrsOutn("NAME"  , gyo, sudent._schreg_name);
            } else {
                // 学籍番号がない場合、志願者基礎マスタの氏名を設定
                svf.VrsOutn("NAME"  , gyo, sudent._applicant_name);
            }

            if(sudent._payment_div.equals(VALUE_KASOU_KOUZA)){
                // 種別：仮想口座
                svf.VrsOutn("PAYMENT_DIV"   , gyo, KASOU_KOUZA_NAME);
            } else if(sudent._payment_div.equals(VALUE_SINPAN)){
                // 種別：信販
                svf.VrsOutn("PAYMENT_DIV"   , gyo, SINPAN_NAME);
            } else if(sudent._payment_div.equals(VALUE_GENKIN)){
                // 種別：現金
                svf.VrsOutn("PAYMENT_DIV"   , gyo, GENKIN_NAME);
            } else if(sudent._payment_div.equals(VALUE_SONOTA)){
                // 種別：その他
                svf.VrsOutn("PAYMENT_DIV"   , gyo, SONOTA_NAME);
            }
            
            // 入金
            svf.VrsOutn("PATMENT_MONEY"     , gyo, sudent._payment_money);
            _param.total_peyment_money += Integer.parseInt(sudent._payment_money);
            
            breakPaymentDate = sudent._payment_date;
            //レコード数カウント
            ++reccnt;
            //現在ページ数判断用
            ++gyo;
            _hasData = true;
            retflg = true;
        }

        //最終レコードを出力
        if (retflg) {
            //50行超えた場合改ページを行う
            if (gyo > PAGE_MAX_LINE) {
                svf.VrsOut("TOTALPAGE"  ,total_page);
                // ページ出力処理
                svf.VrEndPage();
                gyo = 1;
                ++pagecnt;
                svf.VrsOut("BELONGING_NAME", _param._belongingName);
                svf.VrsOut("DATE1"  ,KNJ_EditDate.h_format_JP(breakPaymentDate));   // 発生日付
                svf.VrsOut("DATE2"  ,KNJ_EditDate.getNowDateWa(true));              // 作成日時
                svf.VrsOut("TOTALPAGE"  ,total_page);
                svf.VrsOut("PAGE"   ,String.valueOf(pagecnt));
                svf.VrsOutn("NAME"  , gyo, "合　　計");
                svf.VrsOutn("PATMENT_MONEY"     , gyo, String.valueOf(_param.total_peyment_money));
                // 改ページ処理
                svf.VrEndPage();
            } else {
                svf.VrsOut("PAGE"   ,String.valueOf(pagecnt));
                svf.VrsOut("TOTALPAGE"  ,total_page);
                svf.VrsOutn("NAME"  , gyo, "合　　計");
                svf.VrsOutn("PATMENT_MONEY"     , gyo, String.valueOf(_param.total_peyment_money));
                // ページ出力処理
                svf.VrEndPage();
            }
        }
    }
    
    /**
     * @param db2           ＤＢ接続オブジェクト
     * @return              帳票出力対象データリスト
     * @throws Exception
     */
    private List createStudents(final DB2UDB db2)       throws SQLException {
        
        final List rtnList = new ArrayList();
        final String sql = getStudentSql();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final student student = new student(rs.getString("PAYMENT_DATE"),
                        rs.getString("PAYMENT_DIV"),
                        rs.getString("INQUIRY_NO"),
                        rs.getString("PAYMENT_MONEY"),
                        nvlT(rs.getString("APPLICANTNO")),
                        nvlT(rs.getString("APPLICANT_NAME")),
                        nvlT(rs.getString("SCHREGNO")),
                        nvlT(rs.getString("SCHREG_NAME"))
                );
                rtnList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }
    
    /**
     * 帳票出力対象日別入金データ抽出ＳＱＬ生成処理
     * @return              SQL文字列
     * @throws Exception
     */
    private String getStudentSql(){
        StringBuffer stb = new StringBuffer();
        stb.append(" select");
        stb.append("    W1.PAYMENT_DATE,");
        stb.append("    W1.PAYMENT_DIV,");
        stb.append("    W1.INQUIRY_NO,");
        stb.append("    W1.PAYMENT_MONEY,");
        stb.append("    W2.APPLICANTNO,");
        stb.append("    W2.NAME AS APPLICANT_NAME,");
        stb.append("    W3.SCHREGNO,");
        stb.append("    W3.NAME AS SCHREG_NAME");
        stb.append(" from");
        stb.append("    PAYMENT_MONEY_HIST_DAT W1");
        stb.append("    LEFT OUTER JOIN APPLICANT_BASE_MST W2 ON");
        stb.append("        W1.APPLICANTNO = W2.APPLICANTNO ");                 // 志願者番号 
        stb.append("    LEFT OUTER JOIN SCHREG_BASE_MST W3 ON");
        stb.append("        W2.SCHREGNO = W3.SCHREGNO");                        // 学籍番号
        stb.append(" where");
        stb.append("    W1.PAYMENT_DATE >= '" + _param._fromDate + "' and");    // 発生年月日(自)
        stb.append("    W1.PAYMENT_DATE <= '" + _param._toDate + "' ");         // 発生年月日(至)
        if (null != _param._belongingDiv && !_param._belongingDiv.equals("")) {
            stb.append("    AND W2.BELONGING_DIV = '" + _param._belongingDiv + "' ");
        }
        stb.append(" order by");
        stb.append("    W1.PAYMENT_DATE, W2.APPLICANTNO");
        log.debug("帳票出力対象データ抽出 の SQL=" + stb.toString());
        return stb.toString();
    }

    /** 日別入金明細データクラス */
    private class student {
        final String _payment_date;
        final String _payment_div;
        final String _inquiry_no;
        final String _payment_money;
        final String _applicantno;
        final String _applicant_name;
        final String _schregno;
        final String _schreg_name;

        student(
                final String payment_date,
                final String payment_div,
                final String inquiry_no,
                final String payment_money,
                final String applicantno,
                final String applicant_name,
                final String schregno,
                final String schreg_name
        ) {
            _payment_date = payment_date;
            _payment_div = payment_div;
            _inquiry_no = inquiry_no;
            _payment_money = payment_money;
            _applicantno = applicantno;
            _applicant_name = applicant_name;
            _schregno = schregno;
            _schreg_name = schreg_name;
        }
    }

    /**総ページ数を取得**/
    private String getTotalPage(final DB2UDB db2,   final Vrw32alp svf, String keyDate) throws SQLException {
        
        String total_page = "";
        
        // 総ページ数を取得
        final String sql = getTotalPageSql(keyDate);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                total_page = rs.getString("TOTAL_PAGE");
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return total_page;

    }

    /**
     * 帳票出力対象日別入金データ：総ページ数抽出ＳＱＬ生成処理
     * @return              SQL文字列
     * @throws Exception
     */
    
    /**
     * 帳票出力対象日別入金データ：総ページ数抽出ＳＱＬ生成処理
     * @param keyDate   入金日
     * @return
     */
    private String getTotalPageSql(String keyDate){
        final String rtn;
        rtn = " select"
            + "    SUM(T1.TEST_CNT) TOTAL_PAGE"
            + " from"
            + "    ("
            + "    SELECT CASE WHEN MOD(COUNT(*),49) > 0 THEN COUNT(*)/49 + 1 ELSE COUNT(*)/49 END TEST_CNT "
            + "     FROM   PAYMENT_MONEY_HIST_DAT "
            + "     where"
            + "        PAYMENT_DATE = '" + keyDate + "'"
            + "     ) T1 "
            ;
        return rtn;
    }

    
    /**
     * 日付をフォーマットし文字列で返す
     * @param s
     * @return
     */
    private String fomatDate(String cnvDate, String before_sfmt, String after_sfmt) {

        String retDate = "";
        try {
            DateFormat foramt = new SimpleDateFormat(before_sfmt); 
            //文字列よりDate型へ変換
            Date date1 = foramt.parse(cnvDate); 
            // 年月日のフォーマットを指定
            SimpleDateFormat sdf1 = new SimpleDateFormat(after_sfmt);
            // Date型より文字列へ変換
            retDate = sdf1.format(date1);
        } catch( Exception e ){
            log.error("fomatDate error!");
        }
        return retDate;
    }
    
    /**
     * 指定した日付文字列（yyyy/MM/dd or yyyy-MM-dd）
     * における月末日付を返します。
     * 
     * @param strDate 対象の日付文字列
     * @return 月末日付
     */
    public static int getLastDay(String strDate) {

        int yyyy = Integer.parseInt(strDate.substring(0,4));
        int MM = Integer.parseInt(strDate.substring(5,7));
        int dd = Integer.parseInt(strDate.substring(8,10));
        Calendar cal = Calendar.getInstance();
        cal.set(yyyy,MM-1,dd);
        int last = cal.getActualMaximum(Calendar.DATE);
        
        return last;
    }

    /**
     * NULL値を""として返す。
     */
    private String nvlT(String val) {

        if (val == null) {
            return "";
        } else {
            return val;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _programid;
        private final String _year;
        private final String _semester;
        private final String _loginDate;
        private final String _fromDate;
        private final String _toDate;
        private final String _belongingDiv;
        private final String _belongingName;

        private int total_peyment_money = 0;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _fromDate = fomatDate(request.getParameter("FROM_DATE"), "yyyy/MM/dd", "yyyy-MM-dd");
            _toDate = fomatDate(request.getParameter("TO_DATE"), "yyyy/MM/dd", "yyyy-MM-dd");
            _belongingDiv = request.getParameter("BELONGING_DIV");
            _belongingName = getBelongingName(db2, _belongingDiv);
        }

        private String getBelongingName(final DB2UDB db2, final String belongingDiv) throws SQLException {
            String retStr = "";
            if (null != belongingDiv && !belongingDiv.equals("")) {
                final String sql = "SELECT SCHOOLNAME1 FROM BELONGING_MST WHERE BELONGING_DIV = '" + belongingDiv + "'";
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        retStr = rs.getString("SCHOOLNAME1");
                    }
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            } else {
                retStr = "全て";
            }
            return "拠点：" + retStr;
        }
    }
    
    private void preStatClose(final PreparedStatement ps1) {
        try {
            ps1.close();
        } catch( Exception e ){
            log.error("preStatClose error!");
        }
    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }
    
    private void close(final DB2UDB db2, final Vrw32alp svf) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
        if (null != svf) {
            svf.VrQuit();
        }
    }
    
}
