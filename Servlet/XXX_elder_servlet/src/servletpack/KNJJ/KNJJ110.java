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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 * 学校教育システム 賢者 [特別活動管理]
 *
 *                      ＜ＫＮＪＪ１１０＞  委員会名簿一覧（委員会別）
 *
 *
 * 2004/11/19 nakamoto 光明版(KFA072M)を近大版(KNJJ110)へプログラムを移行
 * @author m-yama
 * @version $Id: 4dff1e25989aa0e554b038db7a3ffd19cc43210f $
 *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/
public class KNJJ110 {

    /* ロギング */
    private static final Log log = LogFactory.getLog(KNJJ110.class);

    public static final String PATTERNA = "1";
    public static final String PATTERNB = "2";

    private boolean _hasData;

    private Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        log.fatal("$Revision: 62072 $");
        KNJServletUtils.debugParam(request, log);
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            if (PATTERNA.equals(_param._patern)) {
                svf.VrSetForm("KNJJ110.frm", 4); //SuperVisualFormadeで設計したレイアウト定義態の設定
            } else {
                svf.VrSetForm("KNJJ110_2.frm", 4); //SuperVisualFormadeで設計したレイアウト定義態の設定
            }
            svf.VrAttribute("COMMICD","FF=1");      //ＳＶＦ属性変更--->改ページ
            svf.VrAttribute("CLUBCD","FF=1");      //ＳＶＦ属性変更--->改ページ

            // ＳＶＦ作成処理
            PreparedStatement ps1 = null;
            Set_Head(svf, db2);                                        //作成日のメソッド

            //SQL作成
            try {
                if (PATTERNA.equals(_param._patern)) {
                    ps1 = db2.prepareStatement(Pre_StatA()); //ＳＱＬ
                } else {
                    ps1 = db2.prepareStatement(Pre_StatB()); //ＳＱＬ
                }
            } catch( Exception ex ) {
                log.warn("db2.prepareStatement error!", ex);
            }

            for (int i = 0; i < _param._classSelected.length; i++) {
                if (PATTERNA.equals(_param._patern)) {
                    if (svfoutA(svf, _param._classSelected[i], ps1)) {
                        _hasData = true; //ＳＶＦ出力のメソッド
                    }
                } else {
                    if (svfoutB(svf, _param._classSelected[i], ps1)) {
                        _hasData = true; //ＳＶＦ出力のメソッド
                    }
                }
            }

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
    private void Set_Head(final Vrw32alp svf, final DB2UDB db2) {

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;

    //  年度の取得
        try {
            svf.VrsOut("NENDO"  , KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
        } catch( Exception e ){
            log.warn("nendo get error!");
        }
    //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(returnval.val3));        //作成日
        } catch( Exception e ){
            log.warn("ctrl_date get error!");
        }
        getinfo = null;
        returnval = null;

    }//Set_Head()の括り


    private int getMS932ByteLength(final String str) {
    	return KNJ_EditEdit.getMS932ByteLength(str);
    }

    /*----------------------------*
     * ＳＶＦ作成
     *----------------------------*/
    private boolean svfoutA(final Vrw32alp svf, final String classcd, final PreparedStatement ps1)
    {
        boolean nonedata = false;

        try {
            int pp = 0;
            ps1.setString(++pp,classcd);    //委員会区分+委員会コード
            ResultSet rs = ps1.executeQuery();

            while( rs.next() ){
                svf.VrsOut("SEMESTERNAME"        , rs.getString("SEMESTERNAME"));
                svf.VrsOut("COMMICD"        , rs.getString("COMMITTEECD"));
                svf.VrsOut("COMMINAME"  , rs.getString("COMMITTEENAME"));
                //明細出力
                svf.VrsOut("HR_CLASS"   , rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO"   , rs.getString("ATTENDNO"));
                svf.VrsOut("NAME"       , rs.getString("NAME"));
                svf.VrsOut("EXEC_NAME"       , rs.getString("ROLE_NAME"));
                svf.VrEndRecord();
                nonedata = true;
            }
            rs.close();
        } catch( Exception ex ){
            log.error("[KNJJ110]svfout read error!", ex);
        }

        return nonedata;
    }


    /*----------------------------*
     * ＳＶＦ作成
     *----------------------------*/
    private boolean svfoutB(final Vrw32alp svf, final String classcd, final PreparedStatement ps1) {
        boolean nonedata = false;

        try {
            int pp = 0;
            ps1.setString(++pp,classcd);    //部クラブコード
            ResultSet rs = ps1.executeQuery();

            while( rs.next() ){
                svf.VrsOut("CLUBCD"   , rs.getString("COMMITTEECD"));
                svf.VrsOut("CLUBNAME" , rs.getString("COMMITTEENAME"));
                //明細出力
                svf.VrsOut("HR_CLASS" , rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO" , rs.getString("ATTENDNO"));
                String setField = getMS932ByteLength(rs.getString("NAME")) > 14 ? "2_1" : "";
                svf.VrsOut("NAME" + setField     , rs.getString("NAME"));
                setField = getMS932ByteLength(rs.getString("NAME_KANA")) > 20 ? "2_1" : "";
                svf.VrsOut("KANA" + setField     , rs.getString("NAME_KANA"));
                svf.VrsOut("SCHREG_NO", rs.getString("SCHREGNO"));
                if ("1".equals(_param._pattern2PrintBirthday)) {
                	if (null != rs.getString("BIRTHDAY")) {
                		svf.VrsOut("DATE_S" , StringUtils.replace(rs.getString("BIRTHDAY"), "-", "/"));
                	}
                }
                svf.VrsOut("SEX"      , rs.getString("SEX"));

                if ((getMS932ByteLength(rs.getString("ADDR1")) > 50 || getMS932ByteLength(rs.getString("ADDR2")) > 50)) {
                    svf.VrsOut("ADDRESS1_2"  , rs.getString("ADDR1"));
                    svf.VrsOut("ADDRESS2_2"  , rs.getString("ADDR2"));
                } else {
                    svf.VrsOut("ADDRESS1"  , rs.getString("ADDR1"));
                    svf.VrsOut("ADDRESS2"  , rs.getString("ADDR2"));
                }
                svf.VrsOut("TELNO"    , rs.getString("TELNO"));

                setField = getMS932ByteLength(rs.getString("GUARD_NAME")) > 14 ? "2_1" : "";
                svf.VrsOut("GUARD_NAME" + setField , rs.getString("GUARD_NAME"));
                setField = getMS932ByteLength(rs.getString("GUARD_KANA")) > 20 ? "2_1" : "";
                svf.VrsOut("GUARD_KANA" + setField , rs.getString("GUARD_KANA"));
                svf.VrsOut("FINSCHOOL_NAME"    , rs.getString("FINSCHOOL_NAME"));

                svf.VrEndRecord();
                nonedata = true;
            }
            rs.close();
        } catch( Exception ex ){
            log.error("[KNJJ060]svfout read error!", ex);
        }

        return nonedata;
    }


    /* 委員会一覧 */
    private String Pre_StatA() {
        //委員会区分+委員会コードをパラメータとする
        StringBuffer stb = new StringBuffer();
        stb.append("SELECT  ");
        stb.append("    T4.HR_NAME, T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, ");
        stb.append("    T2.NAME, T3.COMMITTEE_FLG || '-' || T3.COMMITTEECD COMMITTEECD, ");
        stb.append("    T6.COMMITTEENAME, T3.CHARGENAME, T3.EXECUTIVECD, T5.NAME1 AS ROLE_NAME, ");
        stb.append("    T3.SEMESTER, ");
        stb.append("    T7.NAME1 AS SEMESTERNAME ");
        stb.append("FROM ");
        stb.append("    SCHREG_REGD_HDAT T4 ");
        if ("1".equals(_param.use_prg_schoolkind)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T4.YEAR AND GDAT.GRADE = T4.GRADE ");
            if (!StringUtils.isBlank(_param.selectSchoolKindSql)) {
                stb.append("   AND GDAT.SCHOOL_KIND IN " + _param.selectSchoolKindSql + " ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T4.YEAR AND GDAT.GRADE = T4.GRADE ");
            stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append("    INNER JOIN SCHREG_REGD_DAT T1 ON T1.YEAR = T4.YEAR ");
        stb.append("         AND T1.SEMESTER = T4.SEMESTER ");
        stb.append("         AND T1.GRADE = T4.GRADE ");
        stb.append("         AND T1.HR_CLASS = T4.HR_CLASS ");
        stb.append("    INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    INNER JOIN SCHREG_COMMITTEE_HIST_DAT T3 ON T3.YEAR = T1.YEAR  ");

        stb.append("        AND T3.COMMITTEE_FLG || T3.COMMITTEECD = ? ");
        if ("1".equals(_param.use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param.selectSchoolKindSql)) {
                stb.append("   AND T3.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                stb.append("   AND T3.SCHOOL_KIND IN " + _param.selectSchoolKindSql + " ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
            stb.append("   AND T3.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
            stb.append("   AND T3.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append("        AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("        AND T3.GRADE = T1.GRADE ");
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
        stb.append("    T1.SEMESTER = '" + _param._semester + "' ");
        if ("1".equals(_param.use_prg_schoolkind)) {
            if (null != _param.SCHKIND) {
                stb.append("   AND T3.SCHOOL_KIND = '" + _param.SCHKIND + "' ");
            }
        }
        stb.append("ORDER BY ");
        stb.append("    T3.COMMITTEE_FLG, T3.COMMITTEECD, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T3.SEMESTER ");
        return stb.toString();

    }//Pre_Stat1()の括り


    /* 委員会一覧 */
    String Pre_StatB()
    {
        //委員会区分+委員会コードをパラメータとする
        StringBuffer stb = new StringBuffer();
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
        if ("1".equals(_param.use_prg_schoolkind)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
            if (!StringUtils.isBlank(_param.selectSchoolKindSql)) {
                stb.append("   AND GDAT.SCHOOL_KIND IN " + _param.selectSchoolKindSql + " ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
            stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
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
        stb.append("     T1.COMMITTEECD, ");
        stb.append("     C1.COMMITTEENAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.HR_NAME, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.NAME_KANA, ");
        stb.append("     T2.BIRTHDAY, ");
        stb.append("     N1.NAME1 AS SEX, ");
        stb.append("     VALUE(T2.ADDR1,'') AS ADDR1, ");
        stb.append("     VALUE(T2.ADDR2,'') AS ADDR2, ");
        stb.append("     T2.TELNO, ");
        stb.append("     G1.GUARD_NAME, ");
        stb.append("     G1.GUARD_KANA, ");
        stb.append("     T2.FINSCHOOL_NAME, ");
        stb.append("     T1.COMMITTEE_FLG, ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS ");
        stb.append(" FROM ");
        stb.append("     SCHREG_COMMITTEE_HIST_DAT T1 ");
        stb.append("     LEFT JOIN COMMITTEE_MST C1 ON T1.COMMITTEE_FLG = C1.COMMITTEE_FLG AND T1.COMMITTEECD = C1.COMMITTEECD ");
        if ("1".equals(_param.use_prg_schoolkind)) {
            stb.append("   AND C1.SCHOOLCD = T1.SCHOOLCD ");
            stb.append("   AND C1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            if (!StringUtils.isBlank(_param.selectSchoolKindSql)) {
                stb.append("   AND C1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                stb.append("   AND C1.SCHOOL_KIND IN " + _param.selectSchoolKindSql + " ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
            stb.append("   AND C1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
            stb.append("   AND C1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append("     LEFT JOIN GUARDIAN_DAT G1 ON T1.SCHREGNO = G1.SCHREGNO, ");
        stb.append("     SCHINFO T2 ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND T2.SEX = N1.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '"+_param._year+"' AND ");
        stb.append("     T1.SCHREGNO = T2.SCHREGNO AND ");
        stb.append("     T1.GRADE = T2.GRADE AND ");
        stb.append("     T1.COMMITTEE_FLG || T1.COMMITTEECD = ? ");
        if ("1".equals(_param.use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param.selectSchoolKindSql)) {
                stb.append("   AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                stb.append("   AND T1.SCHOOL_KIND IN " + _param.selectSchoolKindSql + " ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
            stb.append("   AND T1.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
            stb.append("   AND T1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        if ("1".equals(_param.use_prg_schoolkind)) {
            if (null != _param.SCHKIND) {
                stb.append("   AND T1.SCHOOL_KIND = '" + _param.SCHKIND + "' ");
            }
        }
        if (!"ALL".equals(_param._j004)) {
            stb.append("    AND T1.SEMESTER = '" + _param._j004 + "' ");
        }
        stb.append(" GROUP BY ");
        stb.append("     T1.COMMITTEECD, ");
        stb.append("     C1.COMMITTEENAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.HR_NAME, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.NAME_KANA, ");
        stb.append("     T2.BIRTHDAY, ");
        stb.append("     N1.NAME1, ");
        stb.append("     VALUE(T2.ADDR1,''), ");
        stb.append("     VALUE(T2.ADDR2,''), ");
        stb.append("     T2.TELNO, ");
        stb.append("     G1.GUARD_NAME, ");
        stb.append("     G1.GUARD_KANA, ");
        stb.append("     T2.FINSCHOOL_NAME, ");
        stb.append("     T1.COMMITTEE_FLG, ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS ");
        stb.append(" ORDER BY ");
        stb.append("     T1.COMMITTEE_FLG, ");
        stb.append("     T1.COMMITTEECD, ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.ATTENDNO ");
        return stb.toString();

    }//Pre_Stat1()の括り

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 62072 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    public class Param {
        final String _year;
        final String _semester;
        final String _patern;
        final String _j004;
        final String[] _classSelected;
        final String _useAddrField2;
        final String _useSchool_KindField;
        final String _SCHOOLCD;
        final String _SCHOOLKIND;
        final String _pattern2PrintBirthday;
        private String use_prg_schoolkind;
        private String selectSchoolKind;
        private String selectSchoolKindSql;
        private String SCHKIND;

        public Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _patern = request.getParameter("PATTERN");
            _j004 = request.getParameter("J004");

            _classSelected = request.getParameterValues("COMMI_SELECTED");

            _useAddrField2 = request.getParameter("useAddrField2");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _pattern2PrintBirthday = request.getParameter("PATTERN2_PRINT_BIRTHDAY");
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
            SCHKIND = request.getParameter("SCHKIND");
        }
    }

}  //クラスの括り
