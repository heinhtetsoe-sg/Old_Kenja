package servletpack.KNJJ;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 * 学校教育システム 賢者 [特別活動管理]
 *
 *                      ＜ＫＮＪＪ１００＞  委員会名簿一覧（クラス別）
 *
 *
 * 2004/11/19 nakamoto 光明版(KFA071M)を近大版(KNJJ100)へプログラムを移行
 * @author m-yama
 * @version $Id: 1cabfb2570e3852509ceb0741ce857d1e58f75e5 $
 *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJJ100 {

    /* ロギング */
    private static final Log log = LogFactory.getLog(KNJJ100.class);

    private boolean _hasData;

    private Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        log.fatal("$Revision: 70770 $");
        KNJServletUtils.debugParam(request, log);

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            svf.VrSetForm("KNJJ100.frm", 4);     //SuperVisualFormadeで設計したレイアウト定義態の設定
            svf.VrAttribute("CLASS","FF=1");      //ＳＶＦ属性変更--->改ページ

            _param = createParam(db2, request);

            _hasData = false;

            // ＳＶＦ作成処理
            PreparedStatement ps1 = null;
            Set_Head(svf, db2);                                        //作成日のメソッド

            //SQL作成
            try {
                final String sql = Pre_Stat1();
                log.info(" sql = " + sql);
                ps1 = db2.prepareStatement(sql);       //ＳＱＬ
            } catch (Exception ex) {
                log.error("db2.prepareStatement error!", ex);
            }

            for (int i = 0; i < _param._classSelected.length; i++) {
                if (svfout(svf, db2, _param._classSelected[i], ps1)) {
                    _hasData = true;    //ＳＶＦ出力のメソッド
                }
            }
            DbUtils.closeQuietly(ps1);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }
    }    //doGetの括り

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** 作成日のメソッド **/
    private void Set_Head(final Vrw32alp svf, final DB2UDB db2){

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;

    //  年度の取得
        try {
            svf.VrsOut("NENDO"    , KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
        } catch( Exception e ){
            log.warn("nendo get error!");
        }
    //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));      //作成日
        } catch( Exception e ){
            log.warn("ctrl_date get error!");
        }
        getinfo = null;
        returnval = null;

    }//Set_Head()の括り



    /*----------------------------*
     * ＳＶＦ作成
     *----------------------------*/
    private boolean svfout(final Vrw32alp svf, final DB2UDB db2, final String classcd, final PreparedStatement ps1)
    {
        boolean nonedata = false;

        try {
            int pp = 0;
            ps1.setString(++pp,classcd);    //学年・組
log.debug(classcd);
            ResultSet rs = ps1.executeQuery();

            String schno = "0";
            String strx = "";
            String stry = "";
            while( rs.next() ){
                //学籍番号のブレイク
                if( !rs.getString("SCHREGNO").equals(schno) ){
                    if( !schno.equals("0") ){
                        final String setField = getMS932ByteLength(strx) > 100 ? "3" : getMS932ByteLength(strx) > 80 ? "2" : "";
                        svf.VrsOut("COMMINAME" + setField, strx);
                        svf.VrsOut("POST"     , stry);
                        svf.VrEndRecord();
                        nonedata = true;
                        svf.VrsOut("COMMINAME", "");
                        svf.VrsOut("POST"     , "");
                    }
                    strx = "";
                    stry = "";
                    schno = rs.getString("SCHREGNO");
                }
                //明細出力
                svf.VrsOut("CLASS"    , rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO" , rs.getString("ATTENDNO"));
                final String name = rs.getString("NAME");
                final int nameKeta = getMS932ByteLength(name);
                svf.VrsOut("NAME" + (nameKeta > 30 ? "3" : nameKeta > 20 ? "2" : ""), name);
                //委員会名、係名、役職名の編集
                if( rs.getString("COMMITTEENAME")!=null ){
                    final String committeeName = (rs.getString("SEMESTERNAME") == null ? "" : rs.getString("SEMESTERNAME")) + " " + rs.getString("COMMITTEENAME");
                    if( strx.length()==0 ){
                        strx = committeeName;
                    } else{
                        strx = strx + "," + committeeName;
                    }
                }
                if( rs.getString("CHARGENAME")!=null ){
                    if( strx.length()==0 ){
                        strx = rs.getString("CHARGENAME");
                    } else{
                        strx = strx + "," + rs.getString("CHARGENAME");
                    }
                }
                if( rs.getString("ROLE_NAME")!=null ){
                    if( stry.length()==0 ){
                        stry = rs.getString("ROLE_NAME");
                    } else{
                        stry = stry + "," + rs.getString("ROLE_NAME");
                    }
                }
            }
            if( !schno.equals("0") ){
                final String setField = getMS932ByteLength(strx) > 100 ? "3" : getMS932ByteLength(strx) > 80 ? "2" : "";
                svf.VrsOut("COMMINAME" + setField, strx);
                svf.VrsOut("POST"     , stry);
                svf.VrEndRecord();
                nonedata = true;
            }
            rs.close();
        } catch( Exception ex ){
            log.warn("[KNJJ100]svfout read error!");
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


    /* 委員会一覧 */
    private String Pre_Stat1()
    {
        //学年・組をパラメータとする
        final StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  ");
            stb.append("    T4.HR_NAME, T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, ");
            stb.append("    T2.NAME, T3.COMMITTEE_FLG, T3.COMMITTEECD, ");
            stb.append("    T6.COMMITTEENAME, T3.CHARGENAME, T3.EXECUTIVECD, T5.NAME1 AS ROLE_NAME, ");
            stb.append("    T3.SEMESTER, ");
            stb.append("    T7.NAME1 AS SEMESTERNAME ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_HDAT T4 ");
            if ("1".equals(_param.use_prg_schoolkind)) {
                if (!StringUtils.isBlank(_param.selectSchoolKindSql)) {
                    stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T4.YEAR AND GDAT.GRADE = T4.GRADE ");
                    stb.append("   AND GDAT.SCHOOL_KIND IN " + _param.selectSchoolKindSql + " ");
                }
            } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
                stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T4.YEAR AND GDAT.GRADE = T4.GRADE ");
                stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
            }
            stb.append("    INNER JOIN SCHREG_REGD_DAT T1 ON T1.YEAR = T4.YEAR AND T1.SEMESTER = T4.SEMESTER AND T1.GRADE = T4.GRADE AND T1.HR_CLASS = T4.HR_CLASS ");
            stb.append("    INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_COMMITTEE_HIST_DAT T3 ON T3.YEAR = T1.YEAR AND T3.SCHREGNO = T1.SCHREGNO AND T3.GRADE = T1.GRADE ");
            if ("1".equals(_param.use_prg_schoolkind)) {
                if (!StringUtils.isBlank(_param.selectSchoolKindSql)) {
                    stb.append("   AND T3.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                    stb.append("   AND T3.SCHOOL_KIND IN " + _param.selectSchoolKindSql + " ");
                }
            } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                stb.append("   AND T3.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                stb.append("   AND T3.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
            }
            if (!"ALL".equals(_param._j004)) {
                stb.append("    AND T3.SEMESTER = '" + _param._j004 + "' ");
            }
            stb.append("    LEFT JOIN COMMITTEE_MST T6 ON T6.COMMITTEE_FLG||T6.COMMITTEECD = T3.COMMITTEE_FLG||T3.COMMITTEECD  ");
            if ("1".equals(_param.use_prg_schoolkind)) {
                stb.append("   AND T6.SCHOOLCD = T3.SCHOOLCD ");
                stb.append("   AND T6.SCHOOL_KIND = T3.SCHOOL_KIND ");
                if (!StringUtils.isBlank(_param.selectSchoolKindSql)) {
                    stb.append("   AND T6.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                    stb.append("   AND T6.SCHOOL_KIND IN " + _param.selectSchoolKindSql + " ");
                }
            } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                stb.append("   AND T6.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                stb.append("   AND T6.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
            }
            stb.append("    LEFT JOIN NAME_MST T5 ON T5.NAMECD1 = 'J002' AND T5.NAMECD2 = T3.EXECUTIVECD ");
            stb.append("    LEFT JOIN NAME_MST T7 ON T7.NAMECD1 = 'J004' ");
            stb.append("         AND T3.SEMESTER = T7.NAMECD2 ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '" + _param._year + "' AND ");
            stb.append("    T1.SEMESTER = '" + _param._semester + "' AND ");
            stb.append("    T1.GRADE || T1.HR_CLASS = ? ");
            stb.append("ORDER BY ");
            stb.append("    T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T3.SEMESTER,T3.COMMITTEE_FLG,T3.COMMITTEECD,T3.EXECUTIVECD ");
        } catch( Exception e ){
            log.warn("Pre_Stat1 error!");
        }
        log.fatal(stb.toString());
        return stb.toString();

    }//Pre_Stat1()の括り

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 70770 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _hogosya;
        final String _j004;
        final String[] _classSelected;
        final String _useAddrField2;
        final String _useSchool_KindField;
        final String _SCHOOLCD;
        final String _SCHOOLKIND;
        private String use_prg_schoolkind;
        private String selectSchoolKind;
        private String selectSchoolKindSql;

        public Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _hogosya = request.getParameter("hogosya");
            _j004 = request.getParameter("J004");

            _classSelected = request.getParameterValues("CLASS_SELECTED");

            _useAddrField2 = request.getParameter("useAddrField2");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            selectSchoolKind = request.getParameter("selectSchoolKind");
            if (!StringUtils.isBlank(selectSchoolKind)) {
                final StringBuffer sql = new StringBuffer("('");
                final String[] split = StringUtils.split(selectSchoolKind, ":");
                for (int i = 0; i < split.length; i++) {
                    sql.append(split[i]);
                    if (i < split.length - 1) {
                        sql.append("','");
                    }
                }
                selectSchoolKindSql = sql.append("')").toString();
            }
        }
    }

}  //クラスの括り
