// kanji=漢字

package servletpack.KNJJ;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
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
import servletpack.KNJZ.detail.KNJ_Get_Info;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *    学校教育システム 賢者 [特別活動管理]
 *
 *                        ＜ＫＮＪＪ０５０＞  部員名簿一覧（クラス別）
 *
 *    2004/11/19 nakamoto 光明版(LFA050M)を近大版(KNJJ050)へプログラムを移行
 *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJJ050 {

    /* ロギング */
    private static final Log log = LogFactory.getLog(KNJJ050.class);

    private boolean _hasData;

    private Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            if ("on".equals(_param._hogosya)) {
                svf.VrSetForm("KNJJ050_2.frm", 4);     //SuperVisualFormadeで設計したレイアウト定義態の設定
            } else {
                svf.VrSetForm("KNJJ050.frm", 4);     //SuperVisualFormadeで設計したレイアウト定義態の設定
            }
            svf.VrAttribute("CLASS", "FF=1");      //ＳＶＦ属性変更--->改ページ

            // ＳＶＦ作成処理
            PreparedStatement ps1 = null;
            Set_Head(svf, db2);

            //SQL作成
            try {
                final String sql = Pre_Stat1();
                log.debug(" sql = " + sql);
                ps1 = db2.prepareStatement(sql);        //部員名簿一覧のＳＱＬ
            } catch (Exception ex) {
                log.warn("db2.prepareStatement error!", ex);
            }

            for (int i = 0; i < _param._classSelected.length; i++) {
                if (svfout(svf, db2, _param._classSelected[i], ps1)) {
                    _hasData = true;    //ＳＶＦ出力のメソッド
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

        //    年度の取得
        try {
            svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
        } catch (Exception e) {
            log.warn("nendo get error!");
        }

        //    作成日(現在処理日)の取得
        try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = getinfo.Control(db2);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(returnval.val3));        //作成日
        } catch (Exception e) {
            log.warn("ctrl_date get error!", e);
        }

    }//Set_Head()の括り

    /*----------------------------*
     * ＳＶＦ作成
     *----------------------------*/
    private boolean svfout(final Vrw32alp svf, final DB2UDB db2, final String gradeHrclass, final PreparedStatement ps1) {
        ResultSet rs = null;
        boolean nonedata = false;
        try {
            int pp = 0;
            ps1.setString(++pp, gradeHrclass);    //学年・組
            rs = ps1.executeQuery();

            while (rs.next()) {
                //明細出力
                svf.VrsOut("CLASS"     , rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO" , rs.getString("ATTENDNO"));
                final String name = rs.getString("NAME");
                final int nameByte = KNJ_EditEdit.getMS932ByteLength(name);
                final String nameField = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "";
                svf.VrsOut("NAME" + nameField, name);
                svf.VrsOut("CLUBCD"     , rs.getString("CLUBCD"));
                if ("on".equals(_param._hogosya) && KNJ_EditEdit.getMS932ByteLength(rs.getString("CLUBNAME")) > 20) {
                    svf.VrsOut("CLUBNAME2" , rs.getString("CLUBNAME"));
                } else {
                    svf.VrsOut("CLUBNAME" , rs.getString("CLUBNAME"));
                }
                svf.VrsOut("DATE_S"     , KNJ_EditDate.h_format_JP(rs.getString("SDATE")));
                svf.VrsOut("DATE_F"     , KNJ_EditDate.h_format_JP(rs.getString("EDATE")));
                svf.VrsOut("POST"     , rs.getString("POSTNAME"));
                svf.VrsOut("NOTE"     , rs.getString("REMARK"));

                if ("on".equals(_param._hogosya)) { // 保護者出力
                    svf.VrsOut("GUARD_NAME" , rs.getString("GUARD_NAME"));
                    if ("1".equals(_param._useAddrField2) && (KNJ_EditEdit.getMS932ByteLength(rs.getString("GUARD_ADDR1")) > 50 || KNJ_EditEdit.getMS932ByteLength(rs.getString("GUARD_ADDR1")) > 50)) {
                        svf.VrsOut("ADDRESS1_2"  , rs.getString("GUARD_ADDR1"));
                        svf.VrsOut("ADDRESS2_2"  , rs.getString("GUARD_ADDR2"));
                    } else {
                        svf.VrsOut("ADDRESS1"  , rs.getString("GUARD_ADDR1"));
                        svf.VrsOut("ADDRESS2"  , rs.getString("GUARD_ADDR2"));
                    }
                    svf.VrsOut("TELNO"    , rs.getString("GUARD_TELNO"));
                }
                svf.VrEndRecord();
                nonedata = true;
                //初期化 2003/12/03
                svf.VrsOut("DATE_S"     , "");
                svf.VrsOut("DATE_F"     , "");
                svf.VrsOut("POST"     , "");
                svf.VrsOut("NOTE"     , "");
            }
        } catch (Exception ex) {
            log.warn("[KNJJ050]svfout read error!", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }

        return nonedata;
    }

    /* 科目毎の得点
     * 抽出条件 出力範囲の生徒で、類型評定が１。科目毎。
     */
    private String Pre_Stat1() {
        //欠点科目、学年、出力生徒範囲をパラメータとする
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    T6.HR_NAME, ");
        stb.append("    T1.GRADE, ");
        stb.append("    T1.HR_CLASS, ");
        stb.append("    T1.ATTENDNO, ");
        stb.append("    T2.NAME, ");
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
        stb.append("    INNER JOIN SCHREG_BASE_MST T2 ON ");
        stb.append("        T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    INNER JOIN SCHREG_CLUB_HIST_DAT T3 ON ");
        stb.append("        T3.SCHREGNO = T1.SCHREGNO AND ");
        stb.append("        T3.SCHREGNO = T1.SCHREGNO ");
        if ("1".equals(_param.useClubMultiSchoolKind)) {
            stb.append("        AND T3.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
            stb.append("        AND T3.SCHOOL_KIND = '" + _param._SCHOOLKIND + "'  ");
        } else if ("1".equals(_param.use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param.selectSchoolKind)) {
                stb.append("        AND T3.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
                stb.append("        AND T3.SCHOOL_KIND IN " + _param.selectSchoolKindSql + "  ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLCD) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append("        AND T3.SCHOOLCD = '" + _param._SCHOOLCD + "' ");
            stb.append("        AND T3.SCHOOL_KIND = '" + _param._SCHOOLKIND + "'  ");
        }
        if (_param._taibusyaNotPrint) {
            stb.append("    AND (T3.EDATE IS NULL OR ");
            stb.append("         T3.EDATE > '" + _param._toDate + "') ");
        }
        stb.append("        AND ((T3.SDATE BETWEEN '" + _param._fromDate + "' AND '" + _param._toDate + "') ");
        stb.append("               OR ");
        stb.append("             (VALUE(T3.EDATE, '9999-12-31') BETWEEN '" + _param._fromDate + "' AND '" + _param._toDate + "') ");
        stb.append("               OR ");
        stb.append("             (T3.SDATE <= '" + _param._fromDate + "' AND VALUE(T3.EDATE, '9999-12-31') >= '" + _param._toDate + "') ");
        stb.append("            ) ");
        stb.append("    INNER JOIN CLUB_MST T4 ON ");
        stb.append("        T4.CLUBCD = T3.CLUBCD ");
        if ("1".equals(_param._useSchool_KindField)) {
            stb.append("        AND T4.SCHOOLCD = T3.SCHOOLCD ");
            stb.append("        AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
        }
        stb.append("    INNER JOIN CLUB_YDAT T8 ON ");
        stb.append("        T8.YEAR = T1.YEAR ");
        stb.append("        AND T8.CLUBCD = T4.CLUBCD ");
        if ("1".equals(_param._useSchool_KindField)) {
            stb.append("        AND T8.SCHOOLCD = T4.SCHOOLCD ");
            stb.append("        AND T8.SCHOOL_KIND = T4.SCHOOL_KIND ");
        }
        stb.append("    LEFT JOIN NAME_MST T5 ON ");
        stb.append("        T5.NAMECD1 = 'J001' AND ");
        stb.append("        T5.NAMECD2 = T3.EXECUTIVECD ");
        stb.append("    LEFT JOIN GUARDIAN_DAT T7 ON ");
        stb.append("        T7.SCHREGNO = T1.SCHREGNO ");
        stb.append("WHERE ");
        stb.append("    T1.YEAR = '" + _param._year + "' AND ");
        stb.append("    T1.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("    T1.GRADE || T1.HR_CLASS = ? ");
        stb.append("ORDER BY ");
        stb.append("    T1.GRADE, ");
        stb.append("    T1.HR_CLASS, ");
        stb.append("    T1.ATTENDNO, ");
        stb.append("    T3.CLUBCD ");
        return stb.toString();

    }//Pre_Stat1()の括り

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _hogosya;
        final boolean _taibusyaNotPrint;
        final String _fromDate;
        final String _toDate;
        final String[] _classSelected;
        final String _useAddrField2;
        final String _useSchool_KindField;
        final String _SCHOOLCD;
        final String _SCHOOLKIND;
        final String use_prg_schoolkind;
        final String selectSchoolKind;
        final String useClubMultiSchoolKind;
        String selectSchoolKindSql = null;
        public Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _hogosya = request.getParameter("hogosya");
            _taibusyaNotPrint = null != request.getParameter("taibusya_nozoku") ? true : false;
            _fromDate = StringUtils.replace(request.getParameter("FROM_DATE"), "/", "-");
            _toDate = StringUtils.replace(request.getParameter("TO_DATE"), "/", "-");

            _classSelected = request.getParameterValues("CLASS_SELECTED");

            _useAddrField2 = request.getParameter("useAddrField2");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            selectSchoolKind = request.getParameter("selectSchoolKind");
            useClubMultiSchoolKind = request.getParameter("useClubMultiSchoolKind");
            if (!StringUtils.isBlank(selectSchoolKind)) {
                StringBuffer stb = new StringBuffer("('");
                final String[] split = StringUtils.split(selectSchoolKind, ":");
                if (null != split) {
                    for (int i = 0; i < split.length; i++) {
                        stb.append(split[i] + "', '");
                    }
                }
                selectSchoolKindSql = stb.append("')").toString();
            }
        }
    }

}  //クラスの括り
