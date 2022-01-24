package servletpack.KNJM;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [通信制]
 *
 *                  ＜ＫＮＪＭ２１０＞  生徒別レポート・スクーリング照会
 *
 *  2006/07/31 m-yama 作成日
 *  @version $Id: acc7a3cd4f0e52af910aa3837dab9d09d95fc2a3 $
 */
public class KNJM210 extends HttpServlet {

    private static final Log log = LogFactory.getLog(KNJM210.class);

    final Map _hmparam = new HashMap();

    /**
     * KNJM.classから最初に呼ばれる処理。
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception IO例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        final Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                            //Databaseクラスを継承したクラス

        //  ＳＶＦ作成処理
        PreparedStatement psmain = null;

        final OutputStream outstrm = response.getOutputStream();
        svf.VrInit(); //クラスの初期化
        try {

            //  print設定
            response.setContentType("application/pdf");

            //  svf設定
            svf.VrSetSpoolFileStream(outstrm); //PDFファイル名の設定

            //  ＤＢ接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            getParam(request, db2);
            boolean nonedata = false; //該当データなしフラグ
            setForm(svf);
            //SQL作成
            psmain = db2.prepareStatement(setSql());

            if (mainProcess(db2, svf, psmain)) {
                nonedata = true;
            }
            if (nonedata) {
                //最終データフィールド出力
                svf.VrEndPage();
            }

            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            if (null != svf) {
                svf.VrQuit();
            }
            DbUtils.closeQuietly(psmain);
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if (null != outstrm) {
                outstrm.close();
            }
        }


    }

    /** パラメータの取得 */
    private void getParam(final HttpServletRequest request, final DB2UDB db2) throws Exception {
        _hmparam.put("YEAR", request.getParameter("YEAR"));             //年度
        _hmparam.put("SEMESTER", request.getParameter("SEMESTER"));     //学期
        _hmparam.put("PRINTKIND", request.getParameter("PRINTKIND"));   //出力種別
        _hmparam.put("SCHREGNO", request.getParameter("SCHREGNO"));     //学籍番号
        _hmparam.put("SUBCLASSCD", request.getParameter("SUBCLASSCD")); //科目コード
        _hmparam.put("CHAIRCD", request.getParameter("CHAIRCD"));       //講座コード
        _hmparam.put("useCurriculumcd", request.getParameter("useCurriculumcd"));       //教育課程
        //タイトル
        if (_hmparam.get("PRINTKIND").equals("SCLSUB") || _hmparam.get("PRINTKIND").equals("REPSUB")) {
            _hmparam.put("TITLE", "科目別");
        } else {
            _hmparam.put("TITLE", "全科目");
        }
        final KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        //作成日(現在処理日)の取得
        returnval = getinfo.Control(db2);
        _hmparam.put("DATE", KNJ_EditDate.h_format_thi(returnval.val3, 0));     //講座コード

        if (null != _hmparam.get("SUBCLASSCD")) {
            //講座名称取得
            String sqlChair = "SELECT SUBCLASSNAME FROM SUBCLASS_MST WHERE SUBCLASSCD='" + _hmparam.get("SUBCLASSCD") + "'";
            if ("1".equals(_hmparam.get("useCurriculumcd"))) {
                sqlChair = "SELECT SUBCLASSNAME FROM SUBCLASS_MST WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD='" + _hmparam.get("SUBCLASSCD") + "'";
            }
            db2.query(sqlChair);
            final ResultSet rsChair = db2.getResultSet();
            try {
                while (rsChair.next()) {
                    _hmparam.put("SUBCLASSNAME", rsChair.getString("SUBCLASSNAME"));
                }
            } finally {
                DbUtils.closeQuietly(rsChair);
            }
        }
        if (null != _hmparam.get("SCHREGNO")) {
            //生徒名称取得
            final String sqlSchreg = "SELECT NAME FROM SCHREG_BASE_MST WHERE SCHREGNO='" + _hmparam.get("SCHREGNO") + "'";
            db2.query(sqlSchreg);
            final ResultSet rsSchreg = db2.getResultSet();
            try {
                while (rsSchreg.next()) {
                    _hmparam.put("SCHREGNAME", rsSchreg.getString("NAME"));
                }
            } finally {
                DbUtils.closeQuietly(rsSchreg);
            }
        }

        for (final Iterator it = _hmparam.keySet().iterator(); it.hasNext();) {
            final String key = (String) it.next();
            log.debug(key + " = " + _hmparam.get(key));
        }
    }

    /** フォームセット */
    private void setForm(final Vrw32alp svf) {

        if (_hmparam.get("PRINTKIND").equals("SCLSUB") || _hmparam.get("PRINTKIND").equals("SCLALL")) {
            svf.VrSetForm("KNJM210_1.frm", 1);
        } else {
            svf.VrSetForm("KNJM210_2.frm", 1);
        }

    }

    /**
     * SQL設定
     * @return String&lt;SQL文&gt;
     */
    private String setSql() {
        String rtnSql = null;
        if (_hmparam.get("PRINTKIND").equals("SCLSUB")) {
            rtnSql = sqlSclSub();
        } else if (_hmparam.get("PRINTKIND").equals("REPSUB")) {
            rtnSql = sqlRepSub();
        } else if (_hmparam.get("PRINTKIND").equals("SCLALL")) {
            rtnSql = sqlSclAll();
        } else {
            rtnSql = sqlRepAll();
        }
        return rtnSql;
    }

    /** メイン処理 */
    private boolean mainProcess(final DB2UDB db2, final Vrw32alp svf, final PreparedStatement psmain) throws Exception {
        boolean nonedata = false;
        final ResultSet rs = psmain.executeQuery();
        try {
            int gyo   = 1;          //行数カウント用
            while (rs.next()) {
                if (gyo > 50) {
                    gyo = 1;
                    svf.VrEndPage();                  //SVFフィールド出力
                }

                //ヘッダ出力
                svf.VrsOut("NENDO", (String) _hmparam.get("YEAR") + "年度");
                svf.VrsOut("DATE", (String) _hmparam.get("DATE"));
                svf.VrsOut("TITLE", (String) _hmparam.get("TITLE"));
                svf.VrsOut("SCHREG_NO", (String) _hmparam.get("SCHREGNO"));
                svf.VrsOut("NAME", (String) _hmparam.get("SCHREGNAME"));
                //明細出力
                final String fieldData = getSubclassName(rs);
                final int fieldno = getFieldNo(fieldData);
                svf.VrsOutn("SUBCLASS" + fieldno, gyo, fieldData);
                svf.VrsOutn("STAFFNAME", gyo, rs.getString("TANTOU"));

                if (_hmparam.get("PRINTKIND").equals("SCLSUB") || _hmparam.get("PRINTKIND").equals("SCLALL")) {
                    //印字データセット
                    setSclData(svf, rs, gyo);
                } else {
                    //印字データセット
                    setRepData(svf, rs, gyo);
                }

                nonedata = true;
                gyo++;          //行数カウント用
            }

        } finally {
            DbUtils.closeQuietly(rs);
        }
        return nonedata;

    }

    /** スクーリング出席状況データセット */
    private void setSclData(
            final Vrw32alp svf,
            final ResultSet rs,
            final int gyo
    ) throws SQLException {
        svf.VrsOutn("SCHOOLING_SEQ", gyo, rs.getString("SCH_SEQ_NAME"));
        svf.VrsOutn("EXECUTEDATE", gyo, rs.getString("EXECUTEDATE").replace('-', '/'));
        svf.VrsOutn("PERIOD", gyo, rs.getString("PERIOD_NAME"));
    }

    /** レポート提出状況データセット */
    private void setRepData(
            final Vrw32alp svf,
            final ResultSet rs,
            final int gyo
    ) throws SQLException {
        svf.VrsOutn("SCHOOLING_SEQ", gyo, rs.getString("REP_SEQ_NAME"));
        svf.VrsOutn("STANDARD_SEQ", gyo, rs.getString("SAIHYOUKA"));
        svf.VrsOutn("RECEIPT_DATE", gyo, rs.getString("RECEIPT_DATE").replace('-', '/'));
        if (null == rs.getString("GRAD_DATE") && null == rs.getString("GRAD_VALUE") && null == rs.getString("GRAD_TIME")) {
            svf.VrsOutn("GRAD_VALUE", gyo, "受付中");
        } else {
            if (null != rs.getString("GRAD_DATE")) {
                svf.VrsOutn("GRAD_DATE", gyo, rs.getString("GRAD_DATE").replace('-', '/'));
            }
            svf.VrsOutn("GRAD_VALUE", gyo, rs.getString("HYOUKA"));
        }
    }

    /** 科目名称の取得 */
    private String getSubclassName(final ResultSet rs) throws SQLException {
        String rtnfieldData;
        if (_hmparam.get("PRINTKIND").equals("SCLSUB") || _hmparam.get("PRINTKIND").equals("REPSUB")) {
            rtnfieldData = (String) _hmparam.get("SUBCLASSNAME");
        } else {
            rtnfieldData = rs.getString("SUBCLASSNAME");
        }
        return rtnfieldData;
    }

    /** データの長さにより出力フィールドを切替える */
    private int getFieldNo(final String fieldData) throws UnsupportedEncodingException {
        int rtnfield = 1;
        if (null != fieldData) {
            byte[] check_len = new byte[40];
            check_len = fieldData.getBytes("MS932");
            if (check_len.length > 30) {
                rtnfield = 2;
            }
        }
        return rtnfield;
    }

    /** スクーリング科目別SQLの作成 */
    private String sqlSclSub() {

        final StringBuffer stb = new StringBuffer();
        stb.append("WITH SCH_ATTEND AS (");
        stb.append("SELECT EXECUTEDATE,");
        stb.append("        PERIODCD,");
        stb.append("        SCHOOLING_SEQ,");
        stb.append("        SCHOOLINGKINDCD,");
        stb.append("        STAFFCD,");
        stb.append("        REMARK");
        stb.append(" FROM   SCH_ATTEND_DAT");
        stb.append(" WHERE  YEAR='").append(_hmparam.get("YEAR")).append("' AND");
        stb.append("        SCHREGNO='").append(_hmparam.get("SCHREGNO")).append("' AND");
        stb.append("        CHAIRCD='").append(_hmparam.get("CHAIRCD")).append("' )");
        stb.append("SELECT T1.EXECUTEDATE,");
        stb.append("    CASE WHEN T1.SCHOOLINGKINDCD > '1' THEN NULL ELSE T2.NAME1 END AS PERIOD_NAME,");
        stb.append("    CASE WHEN T1.SCHOOLINGKINDCD > '1' THEN NULL");
        stb.append("         ELSE '第'||RTRIM(CHAR(T1.SCHOOLING_SEQ))||'回' END AS SCH_SEQ_NAME,");
        stb.append("    CASE WHEN T1.SCHOOLINGKINDCD > '1' THEN NULL ELSE T3.STAFFNAME END AS TANTOU,");
        stb.append("    T4.NAME1 AS KIND_NAME,");
        stb.append("    T1.REMARK");
        stb.append(" FROM   SCH_ATTEND T1");
        stb.append("    LEFT JOIN NAME_MST T2 ON T2.NAMECD1='B001' AND T2.NAMECD2=T1.PERIODCD");
        stb.append("    LEFT JOIN STAFF_MST T3 ON T3.STAFFCD=T1.STAFFCD");
        stb.append("    LEFT JOIN NAME_MST T4 ON T4.NAMECD1='M001' AND T4.NAMECD2=T1.SCHOOLINGKINDCD");
        stb.append(" ORDER BY T1.SCHOOLING_SEQ,T1.EXECUTEDATE,T1.PERIODCD");

        log.debug(stb);
        return stb.toString();

    }
    /** レポート科目別SQLの作成 */
    private String sqlRepSub() {

        final StringBuffer stb = new StringBuffer();
        stb.append("WITH REP_MAIN AS (");
        stb.append("SELECT STANDARD_SEQ,");
        stb.append("           RECEIPT_DATE,");
        stb.append("           STAFFCD,");
        stb.append("           GRAD_DATE,GRAD_VALUE,GRAD_TIME,REPRESENT_SEQ");
        stb.append("    FROM   REP_PRESENT_DAT");
        stb.append("    WHERE  YEAR='").append(_hmparam.get("YEAR")).append("' AND");
        stb.append("           SCHREGNO='").append(_hmparam.get("SCHREGNO")).append("' AND");
        stb.append("           CHAIRCD='").append(_hmparam.get("CHAIRCD")).append("' )");
        stb.append("SELECT T1.RECEIPT_DATE,");
        stb.append("       '第'||RTRIM(CHAR(T1.STANDARD_SEQ))||'回' AS REP_SEQ_NAME,");
        stb.append("       T2.STAFFNAME AS TANTOU,");
        stb.append("       T1.GRAD_DATE,");
        stb.append("       T1.GRAD_VALUE,");
        stb.append("       T1.GRAD_TIME,");
        stb.append("       T3.NAME1 AS HYOUKA,");
        stb.append("       CASE WHEN T1.REPRESENT_SEQ > 0 THEN '再'||RTRIM(CHAR(T1.REPRESENT_SEQ))");
        stb.append("            ELSE NULL END AS SAIHYOUKA");
        stb.append(" FROM  REP_MAIN T1");
        stb.append("       LEFT JOIN STAFF_MST T2 ON T2.STAFFCD=T1.STAFFCD");
        stb.append("       LEFT JOIN NAME_MST T3 ON T3.NAMECD1='M003' AND");
        stb.append("                                T3.NAMECD2=T1.GRAD_VALUE");
        stb.append(" ORDER BY T1.STANDARD_SEQ,T1.REPRESENT_SEQ");

        log.debug(stb);
        return stb.toString();

    }
    /** スクーリング全科目SQLの作成 */
    //  CSOFF: ExecutableStatementCount
    private String sqlSclAll() {

        final StringBuffer stb = new StringBuffer();
        stb.append("WITH CHAIR AS (");
        stb.append("SELECT DISTINCT");
        stb.append("       CHAIRCD");
        stb.append(" FROM   CHAIR_STD_DAT");
        stb.append(" WHERE  YEAR      ='").append(_hmparam.get("YEAR")).append("' AND");
        stb.append("        SEMESTER  ='").append(_hmparam.get("SEMESTER")).append("' AND");
        stb.append("        SCHREGNO  ='").append(_hmparam.get("SCHREGNO")).append("' AND");
        stb.append("        SUBSTR(CHAIRCD,1,2)<>'92' )");
        stb.append(",CHAIR_NAME AS (");
        stb.append("SELECT DISTINCT");
        stb.append("       T1.CHAIRCD,");
        stb.append("       T1.CHAIRNAME,");
        if ("1".equals(_hmparam.get("useCurriculumcd"))) {
            stb.append("       T2.CLASSCD, ");
            stb.append("       T2.SCHOOL_KIND, ");
            stb.append("       T2.CURRICULUM_CD, ");
        }
        stb.append("       T2.SUBCLASSCD,");
        stb.append("       T2.SUBCLASSNAME");
        stb.append(" FROM   CHAIR_DAT T1");
        stb.append("        LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD=T1.SUBCLASSCD");
        if ("1".equals(_hmparam.get("useCurriculumcd"))) {
            stb.append("       AND T2.CLASSCD=T1.CLASSCD ");
            stb.append("       AND T2.SCHOOL_KIND=T1.SCHOOL_KIND ");
            stb.append("       AND T2.CURRICULUM_CD=T1.CURRICULUM_CD ");
        }
        stb.append(" WHERE  T1.YEAR      ='").append(_hmparam.get("YEAR")).append("' AND");
        stb.append("        T1.SEMESTER  ='").append(_hmparam.get("SEMESTER")).append("' AND");
        stb.append("        T1.CHAIRCD   IN (SELECT CHAIRCD FROM CHAIR) )");
        stb.append(",SCH_ATTEND AS (");
        stb.append("SELECT CHAIRCD,");
        stb.append("       EXECUTEDATE,");
        stb.append("       PERIODCD,");
        stb.append("       SCHOOLING_SEQ,");
        stb.append("       SCHOOLINGKINDCD,");
        stb.append("       STAFFCD,");
        stb.append("       REMARK");
        stb.append(" FROM   SCH_ATTEND_DAT");
        stb.append(" WHERE  YEAR='").append(_hmparam.get("YEAR")).append("' AND");
        stb.append("        SCHREGNO='").append(_hmparam.get("SCHREGNO")).append("' AND");
        stb.append("        CHAIRCD IN (SELECT CHAIRCD FROM CHAIR) )");
        stb.append("SELECT T1.EXECUTEDATE,");
        stb.append("       CASE WHEN T1.SCHOOLINGKINDCD > '1' THEN NULL ELSE T2.NAME1 END AS PERIOD_NAME,");
        stb.append("       T5.SUBCLASSNAME,");
        stb.append("       CASE WHEN T1.SCHOOLINGKINDCD > '1' THEN NULL");
        stb.append("            ELSE '第'||RTRIM(CHAR(T1.SCHOOLING_SEQ))||'回' END AS SCH_SEQ_NAME,");
        stb.append("       CASE WHEN T1.SCHOOLINGKINDCD > '1' THEN NULL ELSE T3.STAFFNAME END AS TANTOU,");
        stb.append("       T4.NAME1 AS KIND_NAME,");
        stb.append("       T1.REMARK");
        stb.append(" FROM  SCH_ATTEND T1");
        stb.append("       LEFT JOIN NAME_MST T2 ON T2.NAMECD1='B001' AND T2.NAMECD2=T1.PERIODCD");
        stb.append("       LEFT JOIN STAFF_MST T3 ON T3.STAFFCD=T1.STAFFCD");
        stb.append("       LEFT JOIN NAME_MST T4 ON T4.NAMECD1='M001' AND T4.NAMECD2=T1.SCHOOLINGKINDCD");
        stb.append("       LEFT JOIN CHAIR_NAME T5 ON T5.CHAIRCD=T1.CHAIRCD");
        stb.append(" ORDER BY ");
        if ("1".equals(_hmparam.get("useCurriculumcd"))) {
            stb.append("       t5.CLASSCD, ");
            stb.append("       t5.SCHOOL_KIND, ");
            stb.append("       t5.CURRICULUM_CD, ");
        }
        stb.append(" t5.SUBCLASSCD,T1.SCHOOLING_SEQ,T1.EXECUTEDATE,T1.PERIODCD");

        log.debug(stb);
        return stb.toString();

    }
    //  CSON: ExecutableStatementCount

    /** レポート全科目SQLの作成 */
    //  CSOFF: ExecutableStatementCount
    private String sqlRepAll() {

        final StringBuffer stb = new StringBuffer();
        stb.append("WITH CHAIR AS (");
        stb.append("SELECT DISTINCT");
        stb.append("       CHAIRCD");
        stb.append(" FROM   CHAIR_STD_DAT");
        stb.append(" WHERE  YEAR      ='").append(_hmparam.get("YEAR")).append("' AND");
        stb.append("        SEMESTER  ='").append(_hmparam.get("SEMESTER")).append("' AND");
        stb.append("        SCHREGNO  ='").append(_hmparam.get("SCHREGNO")).append("' AND");
        stb.append("        SUBSTR(CHAIRCD,1,2)<>'92' )");
        stb.append(",CHAIR_NAME AS (");
        stb.append("SELECT DISTINCT");
        stb.append("       T1.CHAIRCD,");
        stb.append("       T1.CHAIRNAME,");
        if ("1".equals(_hmparam.get("useCurriculumcd"))) {
            stb.append("       T2.CLASSCD, ");
            stb.append("       T2.SCHOOL_KIND, ");
            stb.append("       T2.CURRICULUM_CD, ");
        }
        stb.append("       T2.SUBCLASSCD,");
        stb.append("       T2.SUBCLASSNAME");
        stb.append(" FROM   CHAIR_DAT T1");
        stb.append("        LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD=T1.SUBCLASSCD");
        if ("1".equals(_hmparam.get("useCurriculumcd"))) {
            stb.append("       AND T2.CLASSCD=T1.CLASSCD ");
            stb.append("       AND T2.SCHOOL_KIND=T1.SCHOOL_KIND ");
            stb.append("       AND T2.CURRICULUM_CD=T1.CURRICULUM_CD ");
        }
        stb.append(" WHERE  T1.YEAR      ='").append(_hmparam.get("YEAR")).append("' AND");
        stb.append("        T1.SEMESTER  ='").append(_hmparam.get("SEMESTER")).append("' AND");
        stb.append("        T1.CHAIRCD   IN (SELECT CHAIRCD FROM CHAIR) )");
        stb.append(",REP_MAIN AS (");
        stb.append("SELECT STANDARD_SEQ,");
        stb.append("       RECEIPT_DATE,");
        stb.append("       STAFFCD,");
        stb.append("       GRAD_DATE,");
        stb.append("       GRAD_VALUE,");
        stb.append("       GRAD_TIME,");
        stb.append("       REPRESENT_SEQ,");
        if ("1".equals(_hmparam.get("useCurriculumcd"))) {
            stb.append("       CLASSCD, ");
            stb.append("       SCHOOL_KIND, ");
            stb.append("       CURRICULUM_CD, ");
        }
        stb.append("       SUBCLASSCD");
        stb.append(" FROM   REP_PRESENT_DAT");
        stb.append(" WHERE  YEAR='").append(_hmparam.get("YEAR")).append("' AND");
        stb.append("        SCHREGNO='").append(_hmparam.get("SCHREGNO")).append("' AND");
        stb.append("        CHAIRCD   IN (SELECT CHAIRCD FROM CHAIR) )");
        stb.append("SELECT T1.RECEIPT_DATE,");
        stb.append("       T4.SUBCLASSNAME,");
        stb.append("       '第'||RTRIM(CHAR(T1.STANDARD_SEQ))||'回' AS REP_SEQ_NAME,");
        stb.append("       T2.STAFFNAME AS TANTOU,");
        stb.append("       T1.GRAD_DATE,");
        stb.append("       T1.GRAD_VALUE,");
        stb.append("       T1.GRAD_TIME,");
        stb.append("       T3.NAME1 AS HYOUKA,");
        stb.append("       CASE WHEN T1.REPRESENT_SEQ > 0 THEN '再'||RTRIM(CHAR(T1.REPRESENT_SEQ))");
        stb.append("            ELSE NULL END AS SAIHYOUKA");
        stb.append(" FROM   REP_MAIN T1");
        stb.append("        LEFT JOIN STAFF_MST T2 ON T2.STAFFCD=T1.STAFFCD");
        stb.append("        LEFT JOIN NAME_MST T3 ON T3.NAMECD1='M003' AND");
        stb.append("                                 T3.NAMECD2=T1.GRAD_VALUE");
        stb.append("        LEFT JOIN CHAIR_NAME T4 ON T4.SUBCLASSCD=T1.SUBCLASSCD");
        if ("1".equals(_hmparam.get("useCurriculumcd"))) {
            stb.append("       AND T4.CLASSCD=T1.CLASSCD ");
            stb.append("       AND T4.SCHOOL_KIND=T1.SCHOOL_KIND ");
            stb.append("       AND T4.CURRICULUM_CD=T1.CURRICULUM_CD ");
        }
        stb.append(" ORDER BY ");
        if ("1".equals(_hmparam.get("useCurriculumcd"))) {
            stb.append("       T1.CLASSCD, ");
            stb.append("       T1.SCHOOL_KIND, ");
            stb.append("       T1.CURRICULUM_CD, ");
        }
        stb.append(" T1.SUBCLASSCD,T1.STANDARD_SEQ,T1.REPRESENT_SEQ");

        log.debug(stb);
        return stb.toString();

    }
    //  CSON: ExecutableStatementCount
}
