// kanji=漢字
/*
 * $Id: bb582b0bb9b61c618406a27f90f0f4cf803b5521 $
 *
 * 作成日: 2005/11/28
 * 作成者: m-yama
 *
 * Copyright(C) 2005-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *学校教育システム 賢者 [生徒指導情報システム]
 *
 *＜ＫＮＪＨ１３０＞  都道府県別名簿
 *
 *  2005/11/28 m-yama 新規作成
 *  2005/11/28 yamashiro
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJH130KenMeiboAddr {

    private static final Log log = LogFactory.getLog(KNJH130KenMeiboAddr.class);

    private String _useSchool_KindField;
    private String _SCHOOLKIND;

    Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
            throws Exception
            {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);

        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;

        _useSchool_KindField = request.getParameter("useSchool_KindField");
        _SCHOOLKIND = request.getParameter("SCHOOLKIND");
        _param = createParam(db2, request);

        //print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        //svf設定
        svf.VrInit();
        svf.VrSetSpoolFileStream(outstrm);

        //ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!", ex);
            return;
        }

        //ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        boolean nonedata = false; //該当データなしフラグ
        Set_Head(db2, svf);//見出し出力のメソッド
        //SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1());//生徒及び公欠・欠席者
        } catch( Exception ex ) {
            log.warn("DB2 open error!", ex);
        }
        //SVF出力
        if (setSvfMain(db2, svf, ps1)) nonedata = true;//帳票出力のメソッド
        //該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

        // 終了処理
        svf.VrQuit();
        Pre_Stat_f(ps1);//preparestatementを閉じる
        db2.commit();
        db2.close();//DBを閉じる
        outstrm.close();//ストリームを閉じる

            }//doGetの括り



    /** SVF-FORM **/
    private void Set_Head(final DB2UDB db2, final Vrw32alp svf){
        if ("3".equals(_param._outputA)) {
            svf.VrSetForm("KNJH130_4.frm", 1);
        } else {
            svf.VrSetForm("KNJH130_3.frm", 1);
        }
    }//Set_Head()の括り

    /**
     *  svf print 印刷処理
     */
    private boolean setSvfMain(
            DB2UDB db2,
            Vrw32alp svf,
            PreparedStatement ps1
            ) {
        boolean nonedata = false;
        try {
            ResultSet rs = ps1.executeQuery();
            final int maxLine = 35;
            int lineCnt = 1;
            int printCnt = 1;
            int kenCd = -1;
            while( rs.next() ){
                //都道府県のブレイク => 改ページ
                if (Integer.parseInt(rs.getString("KENCD")) != kenCd) {
                    if (0 <= kenCd) svf.VrEndPage();
                    printsvfHead(svf, rs.getString("KENNAME"));
                    lineCnt = 1;
                    printCnt = 1;
                } else if (maxLine < lineCnt) {
                    printsvfHead(svf, rs.getString("KENNAME"));
                    lineCnt = 1;
                }

                svf.VrsOutn("NO", lineCnt, String.valueOf(printCnt));
                svf.VrsOutn("HR_NAME", lineCnt, rs.getString("HR_NAME") + " " + rs.getString("ATTENDNO") + "番");
                final String nameField = getMS932ByteLength(rs.getString("NAME")) > 30 ? "3" : getMS932ByteLength(rs.getString("NAME")) > 20 ? "2" : "1";
                svf.VrsOutn("NAME" + nameField, lineCnt, rs.getString("NAME"));
                final String gNameField = getMS932ByteLength(rs.getString("GNAME")) > 30 ? "3" : getMS932ByteLength(rs.getString("GNAME")) > 20 ? "2" : "1";
                svf.VrsOutn("GRD_NAME" + gNameField, lineCnt, rs.getString("GNAME"));
                svf.VrsOutn("ZIP_NO", lineCnt, rs.getString("ZIPCD"));
                final String setAddr = rs.getString("ADDR1") + rs.getString("ADDR2");
                final String addrField = getMS932ByteLength(setAddr) > 50 ? "3" : getMS932ByteLength(setAddr) > 40 ? "2" : "1";
                svf.VrsOutn("ADDR" + addrField, lineCnt, setAddr);
                svf.VrsOutn("TELNO", lineCnt, rs.getString("TELNO"));
                final String setBirthDay = rs.getString("BIRTHDAY");
                svf.VrsOutn("BIRTHDAY", lineCnt, KNJ_EditDate.h_format_JP(setBirthDay));
                svf.VrsOutn("FINSCHOOL_NAME", lineCnt, rs.getString("FINSCHOOL_NAME"));
                String setGrd = !"".equals(rs.getString("GRD_NAME")) ? rs.getString("GRD_NAME") : rs.getString("TRANSFER_NAME");
                svf.VrsOutn("GRD_DIV", lineCnt, setGrd);
                svf.VrsOutn("EM_NAME", lineCnt, rs.getString("EMERGENCYCALL"));
                svf.VrsOutn("EM_TELNO", lineCnt, rs.getString("EMERGENCYTELNO"));

                kenCd = Integer.parseInt(rs.getString("KENCD"));
                lineCnt++;
                printCnt++;
            }

            if( 0 <= kenCd ){
                nonedata = true;
                svf.VrEndPage();
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfMain set error!", ex );
        }
        return nonedata;
    }

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

    /**
     *  SVF-FORM ページ見出し印刷
     */
    private void printsvfHead(final Vrw32alp svf, final String kenname) {

        try {
            final String titleSet = ("1".equals(_param._outputA))? "保護者" : ("2".equals(_param._outputA))? "負担者" : "生徒";
            svf.VrsOut("nendo",  KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度" + "  居住地別" + titleSet + "名簿(" + kenname + ")");
            svf.VrsOut("DATE",   KNJ_EditDate.h_format_JP(_param._ctrlDate) );
            if ("1".equals(_param._outputA)) {
                svf.VrsOut("GRD_NAME_HEADER", "保護者氏名");
                svf.VrsOut("GRD_ADDR_HEADER", "保護者住所");
                svf.VrsOut("GRD_TELNO_HEADER", "保護者電話番号");
            } else if ("2".equals(_param._outputA)) {
                svf.VrsOut("GRD_NAME_HEADER", "負担者氏名");
                svf.VrsOut("GRD_ADDR_HEADER", "負担者住所");
                svf.VrsOut("GRD_TELNO_HEADER", "負担者電話番号");
            }
        } catch( Exception e ){
            log.error("printsvfHead error!", e );
        }
    }

    /**PrepareStatement作成**/
    private String Pre_Stat1(){

        //生徒及び公欠・欠席者データ
        StringBuffer stb = new StringBuffer();
        try {
            String addrTableName = "SCHREG_ADDRESS_DAT";
            String addrFieldName = "";
            if ("1".equals(_param._outputA)){
                addrTableName = "GUARDIAN_ADDRESS_DAT";
                addrFieldName = "GUARD_";
            } else if ("2".equals(_param._outputA)) {
                addrTableName = "GUARANTOR_ADDRESS_DAT";
                addrFieldName = "GUARANTOR_";
            }
            /* 2005/02/18Modify yamasihro 異動者を除外 */
            stb.append(" WITH ADDR_INFO AS( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1." + addrFieldName + "ZIPCD AS ZIPCD, ");
            stb.append("         CASE WHEN T1." + addrFieldName + "ADDR1 IS NULL THEN '' ELSE T1." + addrFieldName + "ADDR1 END AS ADDR1, ");
            stb.append("         CASE WHEN T1." + addrFieldName + "ADDR2 IS NULL THEN '' ELSE T1." + addrFieldName + "ADDR2 END AS ADDR2, ");
            stb.append("         T1." + addrFieldName + "TELNO AS TELNO ");
            stb.append("     FROM ");
            stb.append("         " + addrTableName + " T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.ISSUEDATE IN(SELECT ");
            stb.append("                             MAX(T2.ISSUEDATE) AS ISSUEDATE ");
            stb.append("                         FROM ");
            stb.append("                             " + addrTableName + " T2 ");
            stb.append("                         WHERE ");
            stb.append("                             T1.SCHREGNO = T2.SCHREGNO AND ");
            stb.append("                             '" + _param._ctrlDate + "' BETWEEN T2.ISSUEDATE AND ");
            stb.append("                             CASE WHEN T2.EXPIREDATE IS NULL THEN '9999-12-31' ELSE T2.EXPIREDATE END ) ");
            stb.append(" ), TRANSFER AS (");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         MAX(L1.NAME1) AS TRANSFER_NAME ");
            stb.append("     FROM ");
            stb.append("         SCHREG_TRANSFER_DAT T1 ");
            stb.append("         LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A004' ");
            stb.append("              AND L1.NAMECD2 = T1.TRANSFERCD ");
            stb.append("     WHERE ");
            stb.append("         T1.TRANSFER_SDATE IN(SELECT ");
            stb.append("                             MAX(T2.TRANSFER_SDATE) AS TRANSFER_SDATE ");
            stb.append("                         FROM ");
            stb.append("                             SCHREG_TRANSFER_DAT T2 ");
            stb.append("                         WHERE ");
            stb.append("                             T1.SCHREGNO = T2.SCHREGNO AND ");
            stb.append("                             '" + _param._ctrlDate + "' BETWEEN T2.TRANSFER_SDATE AND ");
            stb.append("                             CASE WHEN T2.TRANSFER_EDATE IS NULL THEN '9999-12-31' ELSE T2.TRANSFER_EDATE END ) ");
            stb.append("     GROUP BY ");
            stb.append("         T1.SCHREGNO ");
            stb.append(" ), ziptable AS (");
            stb.append(" SELECT ");
            stb.append("    ZIPCD, ");
            stb.append("    case when L1.PREF = '北海道' then '00北海道' ");
            stb.append("         when L1.PREF = '青森県' then '01青森' ");
            stb.append("         when L1.PREF = '岩手県' then '02岩手' ");
            stb.append("         when L1.PREF = '宮城県' then '03宮城' ");
            stb.append("         when L1.PREF = '秋田県' then '04秋田' ");
            stb.append("         when L1.PREF = '山形県' then '05山形' ");
            stb.append("         when L1.PREF = '福島県' then '06福島' ");
            stb.append("         when L1.PREF = '茨城県' then '07茨城' ");
            stb.append("         when L1.PREF = '栃木県' then '08栃木' ");
            stb.append("         when L1.PREF = '群馬県' then '09群馬' ");
            stb.append("         when L1.PREF = '埼玉県' then '10埼玉' ");
            stb.append("         when L1.PREF = '千葉県' then '11千葉' ");
            stb.append("         when L1.PREF = '東京都' then '12東京' ");
            stb.append("         when L1.PREF = '神奈川県' then '13神奈川' ");
            stb.append("         when L1.PREF = '新潟県' then '14新潟' ");
            stb.append("         when L1.PREF = '富山県' then '15富山' ");
            stb.append("         when L1.PREF = '石川県' then '16石川' ");
            stb.append("         when L1.PREF = '福井県' then '17福井' ");
            stb.append("         when L1.PREF = '山梨県' then '18山梨' ");
            stb.append("         when L1.PREF = '長野県' then '19長野' ");
            stb.append("         when L1.PREF = '岐阜県' then '20岐阜' ");
            stb.append("         when L1.PREF = '静岡県' then '21静岡' ");
            stb.append("         when L1.PREF = '愛知県' then '22愛知' ");
            stb.append("         when L1.PREF = '三重県' then '23三重' ");
            stb.append("         when L1.PREF = '滋賀県' then '24滋賀' ");
            stb.append("         when L1.PREF = '京都府' then '25京都' ");
            stb.append("         when L1.PREF = '大阪府' then '26大阪' ");
            stb.append("         when L1.PREF = '兵庫県' then '27兵庫' ");
            stb.append("         when L1.PREF = '奈良県' then '28奈良' ");
            stb.append("         when L1.PREF = '和歌山県' then '29和歌山' ");
            stb.append("         when L1.PREF = '鳥取県' then '30鳥取' ");
            stb.append("         when L1.PREF = '島根県' then '31島根' ");
            stb.append("         when L1.PREF = '岡山県' then '32岡山' ");
            stb.append("         when L1.PREF = '広島県' then '33広島' ");
            stb.append("         when L1.PREF = '山口県' then '34山口' ");
            stb.append("         when L1.PREF = '徳島県' then '35徳島' ");
            stb.append("         when L1.PREF = '香川県' then '36香川' ");
            stb.append("         when L1.PREF = '愛媛県' then '37愛媛' ");
            stb.append("         when L1.PREF = '高知県' then '38高知' ");
            stb.append("         when L1.PREF = '福岡県' then '39福岡' ");
            stb.append("         when L1.PREF = '佐賀県' then '40佐賀' ");
            stb.append("         when L1.PREF = '長崎県' then '41長崎' ");
            stb.append("         when L1.PREF = '熊本県' then '42熊本' ");
            stb.append("         when L1.PREF = '大分県' then '43大分' ");
            stb.append("         when L1.PREF = '宮崎県' then '44宮崎' ");
            stb.append("         when L1.PREF = '鹿児島県' then '45鹿児島' ");
            stb.append("         when L1.PREF = '沖縄県' then '46沖縄' ELSE NULL END KENCD ");
            stb.append("FROM ");
            stb.append("    ADDR_INFO t1 ");
            stb.append("    LEFT JOIN ZIPCD_MST L1 ON t1.ZIPCD = L1.NEW_ZIPCD ");
            stb.append("GROUP BY ");
            stb.append("    ZIPCD, ");
            stb.append("    L1.PREF ");
            stb.append(" ) ");

            stb.append("SELECT ");
            stb.append("    SUBSTR(T4.KENCD,1,2) AS KENCD, ");
            stb.append("    SUBSTR(T4.KENCD,3) AS KENNAME, ");
            stb.append("    T1.GRADE || T1.HR_CLASS AS GRADEHR_CLASS, ");
            stb.append("    T5.HR_NAME, ");
            stb.append("    T1.ATTENDNO, ");
            stb.append("    T3.NAME, ");
            stb.append("    T2.ZIPCD, ");
            stb.append("    T2.ADDR1, ");
            stb.append("    T2.ADDR2, ");
            stb.append("    T2.TELNO, ");
            stb.append("    T3.BIRTHDAY, ");
            stb.append("    T3.EMERGENCYCALL, ");
            stb.append("    T3.EMERGENCYTELNO, ");
            stb.append("    L1.FINSCHOOL_NAME, ");
            stb.append("    VALUE(L2.TRANSFER_NAME, '') AS TRANSFER_NAME, ");
            if ("1".equals(_param._outputA)) {
                stb.append("    GUARDIAN.GUARD_NAME AS GNAME, ");
            } else if ("2".equals(_param._outputA)) {
                stb.append("    GUARDIAN.GUARANTOR_NAME AS GNAME, ");
            } else {
                stb.append("    '' AS GNAME, ");
            }
            stb.append("    CASE WHEN '" + _param._ctrlDate + "' > VALUE(T3.GRD_DATE, '9999-12-31') AND T3.GRD_DIV NOT IN ('1', '4') ");
            stb.append("         THEN VALUE(L3.NAME1, '') ");
            stb.append("         ELSE '' ");
            stb.append("    END AS GRD_NAME ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT t1 ");
            if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
                stb.append("   AND T2.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append("    ,ADDR_INFO t2, ");
            stb.append("    SCHREG_BASE_MST t3 ");
            if (!"3".equals(_param._outputA)) {
                stb.append("    INNER JOIN GUARDIAN_DAT GUARDIAN ON T3.SCHREGNO = GUARDIAN.SCHREGNO ");
            }
            stb.append("    LEFT JOIN FINHIGHSCHOOL_MST L1 ON T3.FINSCHOOLCD = L1.FINSCHOOLCD ");
            stb.append("    LEFT JOIN TRANSFER L2 ON T3.SCHREGNO = L2.SCHREGNO ");
            stb.append("    LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'A003' ");
            stb.append("         AND L3.NAMECD2 = T3.GRD_DIV, ");
            stb.append("    ziptable t4, ");
            stb.append("    SCHREG_REGD_HDAT T5 ");
            stb.append("WHERE ");
            stb.append("    t1.YEAR = '" + _param._year + "' AND ");
            stb.append("    t1.SEMESTER = '" + _param._ctrlSemester + "' AND ");
            stb.append("    t1.SCHREGNO = t2.SCHREGNO AND ");
            stb.append("    t1.SCHREGNO = t3.SCHREGNO AND ");
            stb.append("    t2.ZIPCD IS NOT NULL AND ");
            stb.append("    t2.ZIPCD = t4.ZIPCD AND ");
            stb.append("    T4.KENCD IS NOT NULL AND ");
            stb.append("    T5.YEAR = '" + _param._year + "' AND ");
            stb.append("    T5.SEMESTER = '" + _param._ctrlSemester + "' AND ");
            stb.append("    T5.GRADE = T1.GRADE AND ");
            stb.append("    T5.HR_CLASS = T1.HR_CLASS ");
            stb.append("ORDER BY ");
            stb.append("    T4.KENCD, ");
            stb.append("    T1.GRADE, ");
            stb.append("    T1.HR_CLASS, ");
            stb.append("    T1.ATTENDNO ");
log.debug(stb);

        } catch( Exception e ){
            log.warn("Pre_Stat1 error!" + e );
        }
        return stb.toString();

    }//Pre_Stat1()の括り

    /**PrepareStatement close**/
    private void Pre_Stat_f(PreparedStatement ps1)
    {
        try {
            ps1.close();
        } catch( Exception e ){
            log.warn("Pre_Stat_f error!");
        }
    }//Pre_Stat_f()の括り

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _outputA;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlSemester = request.getParameter("GAKKI");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _outputA = request.getParameter("OUTPUTA");
        }

    }

}//クラスの括り
