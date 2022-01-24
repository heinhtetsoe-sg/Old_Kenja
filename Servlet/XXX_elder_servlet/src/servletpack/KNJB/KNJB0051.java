package servletpack.KNJB;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *    学校教育システム 賢者 [時間割管理]
 *
 *                    ＜ＫＮＪＢ００５１＞  時間割重複講座チェックリスト
 *
 * 2011/05/02 nakamoto 新規作成
 **/

public class KNJB0051 {

    private static final Log log = LogFactory.getLog(KNJB0051.class);

    Vrw32alp svf = new Vrw32alp();
    DB2UDB   db2;
    int ret;

    Param _param;

    public void svf_out(
            HttpServletRequest request, 
            HttpServletResponse response
    ) throws ServletException, IOException {
        dumpParam(request);
        _param = createParam(request);

        _param._useSchool_KindField = request.getParameter("useSchool_KindField");
        _param._SCHOOLKIND = request.getParameter("SCHOOLKIND");

        try {
            init(response, svf);

            // DB接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            try {
                db2.open();
            } catch( Exception ex ) {
                log.debug("DB2 open error!", ex);
            }

            _param.load(db2);

            // 印字メイン
            boolean hasData = false;   // 該当データ無しフラグ
            hasData = printMain(db2, svf);
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            close(svf, db2);
        }
    }

    /** 印刷処理メイン */
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws ServletException, IOException {
        boolean rtnflg = false;

        ret = svf.VrSetForm("KNJB0051.frm", 4);
        log.debug("印刷するフォーム:KNJB0051.frm");

        set_head();

        if (printMeisai(db2, svf)) rtnflg = true;

        return rtnflg;
    }

    private boolean printMeisai(
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws ServletException, IOException {
        boolean rtnflg = false;

        try {
            db2.query(getMeisaiSql());
            ResultSet rs = db2.getResultSet();
            log.fatal("sql = "+getMeisaiSql());
            log.fatal("sql ok!");

            String groupDiv = "0";
            String day[]            = {"月","火","水","木","金","土","日"};//曜日

            int div = 0;
            while( rs.next() ){
                //グループ化用
                String tmp_groupDiv = rs.getString("DAYCD") + rs.getString("PERIODCD") + rs.getString("SCHREGNO");
                if (!groupDiv.equals(tmp_groupDiv)) {
                    groupDiv = tmp_groupDiv;
                    div++;
                    if (99 < div) div = 1;
                    //出力
                    if (_param._isKihon) {
                        int iday = rs.getInt("DAYCD");
                        if (iday == 1) iday = 8;
                        svf.VrsOut("EXECUTEDATE", day[iday - 2]);
                    } else {
                        String exeDate = rs.getString("EXECUTEDATE");
                        svf.VrsOut("EXECUTEDATE", exeDate.substring(0,4) + "/" + exeDate.substring(5,7) + "/" + exeDate.substring(8));
                    }
                    svf.VrsOut("PERIOD"     , rs.getString("PERIOD_NAME"));
                } else {
                    svf.VrsOut("EXECUTEDATE", "");
                    svf.VrsOut("PERIOD"     , "");
                }
                log.fatal("div =" + div + ", groupDiv =" + groupDiv);
                svf.VrsOut("EXECUTEDATE_DIV", String.valueOf(div));
                svf.VrsOut("PERIOD_DIV"     , String.valueOf(div));
                //出力
                svf.VrsOut("CHAIRCD"    , rs.getString("CHAIRCD"));
                svf.VrsOut("CHAIRNAME"  , rs.getString("CHAIRNAME"));
                svf.VrsOut("SCHREGNO"   , rs.getString("SCHREGNO"));
                svf.VrsOut("ATTENDNO"   , rs.getString("HR_NAME") + "-" + rs.getString("ATTENDNO"));
                svf.VrsOut("NAME1"      , rs.getString("NAME"));
                svf.VrsOut("DATE_HEADER", (_param._isKihon) ? "曜日" : "日付");
                svf.VrEndRecord();
                rtnflg  = true; //該当データなしフラグ
            }
            log.fatal("while ok!");
            db2.commit();
            log.fatal("read ok!");
        } catch( Exception ex ) {
            log.error("set_chapter1 read error!", ex);
        }

        return rtnflg;
    }

    private String getMeisaiSql()
    {
        StringBuffer stb = new StringBuffer();
        try {
            //名称マスタ（校時：ＳＨＲを取得）
            stb.append("WITH PERIOD AS ( ");
            stb.append("    SELECT NAMECD2 AS PERIODCD ");
            stb.append("    FROM   V_NAME_MST ");
            stb.append("    WHERE  YEAR='"+_param._ctrlYear+"' AND ");
            stb.append("           NAMECD1='B001' AND ");
            stb.append("           NAMESPARE2 IS NOT NULL ) ");
            //在籍
            stb.append(",SCHNO AS ( ");
            stb.append("    SELECT T1.SCHREGNO,T1.YEAR,T1.SEMESTER,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO ");
            stb.append("    FROM   SCHREG_REGD_DAT T1 ");
            if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
                stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
                stb.append("   AND T2.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
            }
            stb.append("    WHERE  T1.YEAR='"+_param._ctrlYear+"' AND ");
            stb.append("           T1.SEMESTER='"+_param._semester+"' ) ");
            //講座時間割
            stb.append(",SCH_DAT AS ( ");
            if (_param._isKihon) {   //基本
                stb.append("SELECT DISTINCT W1.DAYCD,W1.PERIODCD ");
                stb.append("       ,W1.CHAIRCD,W4.CHAIRNAME ");
                stb.append("FROM   SCH_PTRN_DAT W1 ");
            } else {                                //通常
                stb.append("SELECT DISTINCT W1.EXECUTEDATE,DAYOFWEEK(W1.EXECUTEDATE) AS DAYCD,W1.PERIODCD ");
                stb.append("       ,W1.CHAIRCD,W4.CHAIRNAME ");
                stb.append("FROM   SCH_CHR_DAT W1 ");
            }
            stb.append("           LEFT JOIN CHAIR_DAT W4 ON (W4.YEAR = W1.YEAR AND ");
            stb.append("                                      W4.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                      W4.CHAIRCD = W1.CHAIRCD) ");
            if (_param._isKihon) {   //基本
                stb.append("WHERE  W1.YEAR='"+_param._ctrlYear+"' AND  ");
                stb.append("       W1.SEMESTER='"+_param._semester+"' AND  ");
                stb.append("       W1.BSCSEQ = "+_param._seq+" AND  ");
            } else {                                //通常
                stb.append("WHERE  W1.EXECUTEDATE BETWEEN '"+_param._sDate+"' AND '"+_param._eDate+"' AND  ");
            }
            stb.append("           W1.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) ) ");
            //講座生徒
            stb.append(",CHAIR_STD AS ( ");
            stb.append("    SELECT W1.CHAIRCD, W1.SCHREGNO, W1.APPDATE, W1.APPENDDATE ");
            stb.append("    FROM   CHAIR_STD_DAT W1,SCHNO W2 ");
            stb.append("    WHERE  W1.YEAR=W2.YEAR AND  ");
            stb.append("           W1.SEMESTER=W2.SEMESTER AND  ");
            stb.append("           W1.SCHREGNO=W2.SCHREGNO ) ");
            stb.append(",MAX_STD AS ( ");
            stb.append("    SELECT CHAIRCD, SCHREGNO, MAX(APPDATE) AS APPDATE ");
            stb.append("    FROM   CHAIR_STD ");
            stb.append("    GROUP BY CHAIRCD, SCHREGNO ) ");

            //メイン
            stb.append(",T_MAIN AS ( ");
            stb.append("SELECT T1.DAYCD ");
            stb.append("       ,f_period(T1.PERIODCD) as PERIODCD ");
            if (!_param._isKihon)    //通常
                stb.append("       ,T1.EXECUTEDATE ");
            stb.append("       ,T1.CHAIRCD ");
            stb.append("       ,T2.SCHREGNO ");
            stb.append("       ,L1.GRADE ");
            stb.append("       ,L1.HR_CLASS ");
            stb.append("       ,L1.ATTENDNO ");
            stb.append("       ,L2.NAME ");
            stb.append("       ,L3.HR_NAME ");
            stb.append("       ,VALUE(T1.CHAIRNAME,'') AS CHAIRNAME ");
            stb.append("       ,L7.ABBV1 AS PERIOD_NAME ");
            stb.append("FROM   SCH_DAT T1 ");
            stb.append("       LEFT JOIN NAME_MST L7 ON L7.NAMECD1='B001' AND L7.NAMECD2 = T1.PERIODCD ");
            if (_param._isKihon)    //基本
                stb.append("   ,MAX_STD T2 ");
            if (!_param._isKihon)    //通常
                stb.append("   ,CHAIR_STD T2 ");
            stb.append("       LEFT JOIN SCHREG_BASE_MST L2 ON L2.SCHREGNO=T2.SCHREGNO ");
            stb.append("       LEFT JOIN SCHNO L1 ON L1.SCHREGNO=T2.SCHREGNO ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT L3 ON L3.YEAR=L1.YEAR AND  ");
            stb.append("                                        L3.SEMESTER=L1.SEMESTER AND  ");
            stb.append("                                        L3.GRADE=L1.GRADE AND  ");
            stb.append("                                        L3.HR_CLASS=L1.HR_CLASS ");
            stb.append("WHERE  T1.CHAIRCD=T2.CHAIRCD  ");
            if (!_param._isKihon)    //通常
                stb.append("   AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(" ) ");

            stb.append(",T_GROUP AS ( ");
            stb.append("SELECT T1.DAYCD ");
            if (!_param._isKihon)    //通常
                stb.append("       ,T1.EXECUTEDATE ");
            stb.append("       ,T1.PERIODCD ");
            stb.append("       ,T1.SCHREGNO ");
            stb.append("       ,COUNT(DISTINCT T1.CHAIRCD) AS CHAIR_CNT ");
            stb.append("FROM   T_MAIN T1 ");
            stb.append("GROUP BY T1.DAYCD,T1.PERIODCD,T1.SCHREGNO ");
            if (!_param._isKihon)    //通常
                stb.append("       ,T1.EXECUTEDATE ");
            stb.append("HAVING 1 < COUNT(DISTINCT T1.CHAIRCD) ");
            stb.append(" ) ");

            stb.append("SELECT T1.DAYCD ");
            stb.append("       ,T1.PERIODCD ");
            if (!_param._isKihon)    //通常
                stb.append("       ,T1.EXECUTEDATE ");
            stb.append("       ,T1.CHAIRCD ");
            stb.append("       ,T1.SCHREGNO ");
            stb.append("       ,T1.GRADE ");
            stb.append("       ,T1.HR_CLASS ");
            stb.append("       ,T1.ATTENDNO ");
            stb.append("       ,T1.NAME ");
            stb.append("       ,T1.HR_NAME ");
            stb.append("       ,T1.CHAIRNAME ");
            stb.append("       ,T1.PERIOD_NAME ");
            stb.append("FROM   T_MAIN T1 ");
            stb.append("       INNER JOIN T_GROUP T2 ");
            stb.append("            ON T2.DAYCD = T1.DAYCD ");
            stb.append("           AND T2.PERIODCD = T1.PERIODCD ");
            stb.append("           AND T2.SCHREGNO = T1.SCHREGNO ");
            if (!_param._isKihon)    //通常
                stb.append("       AND T2.EXECUTEDATE = T1.EXECUTEDATE ");
            stb.append("ORDER BY T1.DAYCD,T1.PERIODCD,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T1.CHAIRCD ");
        } catch( Exception e ){
            log.debug("getMeisaiSql error!", e);
        }
        return stb.toString();
    }

    private void set_head()
                     throws ServletException, IOException
    {
        try {
            svf.VrsOut("DATE"    , _param.getNow());
            if (_param._isKihon) {
                String subTitle = "基本時間割：";
                svf.VrsOut("SUBTITLE" , subTitle + _param._titleKihon);
            } else {
                String subTitle = "通常時間割：";
                String sDate = _param._sDate;
                String eDate = _param._eDate;
                sDate = sDate.substring(0,4) + "/" + sDate.substring(5,7) + "/" + sDate.substring(8);
                eDate = eDate.substring(0,4) + "/" + eDate.substring(5,7) + "/" + eDate.substring(8);
                svf.VrsOut("SUBTITLE" , subTitle + sDate + " \uFF5E " + eDate);
            }
            log.debug("set_head read ok!");
        } catch( Exception ex ) {
            log.debug("set_head read error!", ex);
        }

    }

    /*----------------*
     * 月の前ゼロ挿入 *
     *----------------*/
    private String h_tuki(int intx)
                     throws ServletException, IOException
    {
        String strx = null;
        try {
            strx = "00" + String.valueOf(intx);
            strx = strx.substring(strx.length()-2);
        } catch( Exception ex ) {
            log.debug("h_tuki error!", ex);
        }
        return strx;
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    private Param createParam(final HttpServletRequest request) {
        //時間割種別・指定日付 1:基本時間割 2:通常時間割
        final String syubetu = request.getParameter("JIKANWARI_SYUBETU");
        //基本時間割
        final String tYear = request.getParameter("T_YEAR");
        final String tSeq  = request.getParameter("T_BSCSEQ");
        final String tSeme = request.getParameter("T_SEMESTER");
        //通常時間割
        final String sDate = request.getParameter("SDATE");
        final String eDate = request.getParameter("EDATE");
        //その他
        final String ctrlYear = request.getParameter("CTRL_YEAR");
        final String ctrlSeme = request.getParameter("CTRL_SEMESTER");
        final String ctrlDate = request.getParameter("CTRL_DATE");

        return new Param(
                syubetu,
                tYear,
                tSeq,
                tSeme,
                sDate,
                eDate,
                ctrlYear,
                ctrlSeme, ctrlDate
                );
    }

    private void init(final HttpServletResponse response, final Vrw32alp svf) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void close(final Vrw32alp svf, final DB2UDB db2) {
        if (null != svf) {
            svf.VrQuit();
        }
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * パラメータクラス
     */
    private class Param {
        //時間割種別・指定日付 1:基本時間割 2:通常時間割
        final private boolean _isKihon;
        //基本時間割
        private String _seq;
        private String _semester;
        //その他
        private final String _ctrlYear;
        private final String _ctrlSeme;
        private final String _ctrlDate;
        //週開始日・週終了日
        private String _sDate;
        private String _eDate;
        //基本・タイトル
        private String _titleKihon;
        String _useSchool_KindField;
        String _SCHOOLKIND;

        Param(
                final String syubetu,
                final String tYear,
                final String tSeq,
                final String tSeme,
                final String sDate,
                final String eDate,
                final String ctrlYear,
                final String ctrlSeme,
                final String ctrlDate
        ) {
            _isKihon = "1".equals(syubetu);
            if (_isKihon) {
                _seq = tSeq;
                _semester = tSeme;
            } else {
                _sDate = sDate.substring(0,4) + "-" + sDate.substring(5,7) + "-" + sDate.substring(8);
                _eDate = eDate.substring(0,4) + "-" + eDate.substring(5,7) + "-" + eDate.substring(8);
            }
            _ctrlYear = ctrlYear;
            _ctrlSeme = ctrlSeme;
            _ctrlDate = ctrlDate.substring(0,4) + "-" + ctrlDate.substring(5,7) + "-" + ctrlDate.substring(8);
        }

        /** 作成日 */
        private String getNow() {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(sdfY.format(date));
//          stb.append(nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }

        private void load(final DB2UDB db2) {
            if (_isKihon) {
                loadTitleKihon(db2);
            } else {
                loadSemesterTuujou(db2);
            }
        }

        private void loadTitleKihon(final DB2UDB db2) {
            _titleKihon = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT TITLE "
                             + "FROM SCH_PTRN_HDAT "
                             + "WHERE YEAR = '" + _ctrlYear + "' AND BSCSEQ = " + _seq + " AND SEMESTER = '" + _semester + "'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _titleKihon = rs.getString("TITLE");
                }
            } catch (final Exception ex) {
                log.error("基本・タイトルのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("基本・タイトル:" + _titleKihon);
        }

        private void loadSemesterTuujou(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT SEMESTER "
                             + "      ,case when '"+_sDate+"' < SDATE then SDATE else null end as SDATE "
                             + "      ,case when '"+_eDate+"' > EDATE then EDATE else null end as EDATE "
                             + "FROM   SEMESTER_MST "
                             + "WHERE  SDATE <= date('"+_sDate+"') AND EDATE >= date('"+_sDate+"') AND YEAR = '"+_ctrlYear+"' AND SEMESTER <> '9'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _semester = rs.getString("SEMESTER");
                    if (rs.getString("SDATE") != null) _sDate = rs.getString("SDATE"); //学期開始日
                    if (rs.getString("EDATE") != null) _eDate = rs.getString("EDATE"); //学期終了日
                }
            } catch (final Exception ex) {
                log.error("通常・学期のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("通常・学期:" + _semester);
            log.debug("開始日:" + _sDate);
            log.debug("終了日:" + _eDate);
        }

    }

}  //クラスの括り
