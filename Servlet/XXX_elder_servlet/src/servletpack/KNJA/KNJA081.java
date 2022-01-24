package servletpack.KNJA;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 * 学校教育システム 賢者 [入試管理]
 *
 *  ＜ＫＮＪＡ０８１＞  クラス編成事前一覧
 *
 * 2006/03/17 m-yama 作成
 * 2006/03/20 m-yama NO001 成績、ふりがな出力指定追加
 *  2006/03/27 yamashiro NO002 １学年における留年生の氏名取得を変更
 */

public class KNJA081 {


    private static final Log log = LogFactory.getLog(KNJA081.class);

    private boolean _isKindai;

    private String _useSchool_KindField;
    private String _SCHOOLCD;
    private String _SCHOOLKIND;
    private String _useKNJA081_2_frm;
    private String _use_prg_schoolkind;
    private String _selectSchoolKind;
    private String _selectSchoolKindSql;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        String param[] = new String[11];

        log.fatal("$Revision: 59492 $");
        KNJServletUtils.debugParam(request, log);

        //パラメータの取得
        try {
            param[0]  = request.getParameter("YEAR");
            param[1]  = request.getParameter("GRADE");
            param[3]  = request.getParameter("OUTPUT1");    //成績出力     NO001
            param[4]  = request.getParameter("OUTPUT2");    //ふりがな出力 NO001
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _useKNJA081_2_frm = request.getParameter("useKNJA081_2.frm");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            if (!StringUtils.isBlank(_selectSchoolKind)) {
                StringBuffer stb = new StringBuffer("('");
                final String[] split = StringUtils.split(_selectSchoolKind, ":");
                if (null != split) {
                    for (int i = 0; i < split.length; i++) {
                        stb.append(split[i] + "', '");
                    }
                }
                _selectSchoolKindSql = stb.append("')").toString();
            }
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
        }

        // print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        // svf設定
        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!",ex);
            return;
        }


        // ＳＶＦ作成処理
        boolean nonedata = false;

        getHeaderData(db2,svf,param);

        //SVF出力
        if (printMain(db2,svf,param)) nonedata = true;

        // 該当データ無し
        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

        //  終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();
        outstrm.close();
    }//doGetの括り

    /**ヘッダーデータを抽出*/
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[]){

        // 作成日
        try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            param[2] = KNJ_EditDate.h_format_JP(returnval.val3);
            getinfo = null;
            returnval = null;
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }
    }//getHeaderData()の括り


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
            rs = ps.executeQuery();

            if (rs.next()) {
                _isKindai = "KINDAI".equals(rs.getString("NAME1")) || "KINJUNIOR".equals(rs.getString("NAME1"));
            }
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        try {
            ps = db2.prepareStatement(meisaiSql(param));
            rs = ps.executeQuery();

            //明細データをセット
            nonedata = printMeisai(svf,param,rs);
        } catch (Exception ex) {
            log.warn("printMain read error!",ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;

    }//printMain()の括り

    /**明細データをセット*/
    private boolean printMeisai(final Vrw32alp svf, final String[] param, final ResultSet rs) {
        boolean nonedata = false;
        int row  = 1;     //列カウンタ MAX 8
        int line = 1;     //行カウンタ MAX50
        int boycnt  = 0;  //男子カウンタ
        int girlcnt = 0;  //女子カウンタ
        String classchange = "*"; //改列用
        String gradechange = "*"; //改列用
        try {
            //ヘッダ印刷
            final String form;
            if ("1".equals(_useKNJA081_2_frm)) {
                form = "KNJA081_2.frm";
            } else {
                form = "KNJA081.frm";
            }
            svf.VrSetForm(form, 1);
            svf.VrsOut("DATE" , param[2]);
            while (rs.next()) {

                if (!classchange.equals("*") && !classchange.equalsIgnoreCase(rs.getString("CLASSCHANGE"))){
                    svf.VrsOut("BOY"+row, String.valueOf(boycnt));
                    svf.VrsOut("GIRL"+row, String.valueOf(girlcnt));
                    svf.VrsOut("TOTAL"+row, String.valueOf(boycnt + girlcnt));
                    girlcnt = 0;
                    boycnt  = 0;
                    line = 1;
                    row++;
                }
                if (!gradechange.equals("*") && !gradechange.equalsIgnoreCase(rs.getString("GRADE"))){
                    svf.VrEndPage();
                    girlcnt = 0;
                    boycnt  = 0;
                    line = 1;
                    row  = 1;
                }
                if (line > 50){
                    line = 1;
                    row++;
                }
                if (row > 8){
                    svf.VrEndPage();
                    line = 1;
                    row = 1;
                }
                svf.VrsOut("HR_NAME" + row, rs.getString("HR_NAME") );
                svf.VrsOut("STAFF_NAME" + row, rs.getString("STAFFNAME") );
                if ("1".equals(_useKNJA081_2_frm)) {
                    
                    final String oldHrNameabbv = StringUtils.defaultString(rs.getString("OLD_HR_NAMEABBV"));
                    String oldAttendno = rs.getString("OLD_ATTENDNO");
                    oldAttendno = NumberUtils.isDigits(oldAttendno) ? String.valueOf(Integer.parseInt(oldAttendno)) : StringUtils.defaultString(oldAttendno);
                    oldAttendno = StringUtils.repeat(" ", 3 - oldAttendno.length()) + oldAttendno;
                    svf.VrsOutn("OLD_CLASS" + row, line, oldHrNameabbv + oldAttendno);
                } else {
                    svf.VrsOutn("OLD_CLASS" + row, line, rs.getString("OLD_CLASSNAME"));
                }

                //NO001
                if (null != param[4]){
                    svf.VrsOutn("KANA"+row, line, rs.getString("NAME_KANA") );
                }

                svf.VrsOutn("NAME"+row, line, rs.getString("NAME") );

                //NO001
                if (null != param[3]){
                    svf.VrsOutn("SCORE"+row, line, rs.getString("SCORE") );
                }

                if (null != rs.getString("SEX") && rs.getString("SEX").equals("1")){
                    boycnt++;
                }else {
                    girlcnt++;
                }

                line++;
                classchange = rs.getString("CLASSCHANGE");
                gradechange = rs.getString("GRADE");
                nonedata = true;
            }
            if (line > 1){
                svf.VrsOut("BOY"+row, String.valueOf(boycnt));
                svf.VrsOut("GIRL"+row, String.valueOf(girlcnt));
                svf.VrsOut("TOTAL"+row, String.valueOf(boycnt + girlcnt));
                svf.VrEndPage();
            }
        } catch (Exception ex) {
            log.warn("printMeisai read error!", ex);
        }

        return nonedata;

    }//printMeisai()の括り

    /**
     * 明細データを抽出
     *
     */
    private String meisaiSql(String[] param) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT ");
            stb.append("    T3.HR_NAME, ");
            stb.append("    T4.STAFFNAME, ");
            stb.append("    OLD_REGDH.HR_CLASS_NAME1 AS OLD_CLASSNAME, ");
            stb.append("    OLD_REGDH.HR_NAMEABBV AS OLD_HR_NAMEABBV, ");
            stb.append("    T1.OLD_ATTENDNO, ");
            stb.append("    CASE WHEN N5.NAME2 IS NOT NULL AND T2.NAME IS NOT NULL THEN T2.NAME ELSE T5.NAME END AS NAME, ");  //NO002
            stb.append("    CASE WHEN N5.NAME2 IS NOT NULL AND T2.NAME_KANA IS NOT NULL THEN T2.NAME_KANA ELSE T5.NAME_KANA END AS NAME_KANA, ");  //NO002
            stb.append("    T1.SCORE, ");
            stb.append("    CASE WHEN N5.NAME2 IS NOT NULL AND T2.SCHREGNO IS NOT NULL THEN T2.SEX ELSE T5.SEX END AS SEX, ");  //NO002
            stb.append("    T1.GRADE, ");
            stb.append("    VALUE(T1.GRADE,'00') || VALUE(T1.HR_CLASS,'000') AS CLASSCHANGE ");
            stb.append("FROM ");
            stb.append("    CLASS_FORMATION_DAT T1 ");
            stb.append("    LEFT JOIN FRESHMAN_DAT T2 ON T2.ENTERYEAR = T1.YEAR ");
            stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_BASE_MST T5 ON T5.SCHREGNO = T1.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("      AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("      AND T3.GRADE = T1.GRADE ");
            stb.append("      AND T3.HR_CLASS = T1.HR_CLASS ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT OLD_REGDH ON OLD_REGDH.YEAR = '" + (Integer.parseInt(param[0]) - 1) + "' ");
            stb.append("      AND OLD_REGDH.SEMESTER = '1' ");
            stb.append("      AND OLD_REGDH.GRADE = T1.OLD_GRADE ");
            stb.append("      AND OLD_REGDH.HR_CLASS = T1.OLD_HR_CLASS ");
            stb.append("    LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = T3.TR_CD1 ");
            stb.append("    LEFT JOIN NAME_MST N5 ON N5.NAMECD1 = 'A023' ");
            stb.append("      AND N5.NAME2 = T1.GRADE ");
            stb.append("    LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '"+param[0]+"' ");
            stb.append("    AND T1.SEMESTER = '1' ");
            if (!param[1].equals("99")){
                stb.append("    AND T1.GRADE = '"+param[1]+"' ");
            }
            if ("1".equals(_use_prg_schoolkind)) {
                if (!StringUtils.isBlank(_selectSchoolKind)) {
                    stb.append("        AND GDAT.SCHOOL_KIND IN " + _selectSchoolKindSql + "  ");
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append("   AND GDAT.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append("ORDER BY ");
            stb.append("    T1.GRADE, ");
            stb.append("    T1.HR_CLASS, ");
            if (_isKindai) {
                stb.append("    CASE WHEN N5.NAME2 IS NOT NULL AND T2.NAME_KANA IS NOT NULL THEN T2.NAME_KANA ELSE T5.NAME_KANA END, ");  //NO002
            } else {
                stb.append("    CASE WHEN N5.NAME2 IS NOT NULL AND T2.NAME_KANA IS NOT NULL THEN TRANSLATE_KANA(T2.NAME_KANA) ELSE TRANSLATE_KANA(T5.NAME_KANA) END, ");  //NO002
            }
            stb.append("    T1.ATTENDNO ");

            //log.debug(stb);
        } catch (Exception e) {
            log.warn("meisaiSql error!", e);
        }
        return stb.toString();

    }//meisaiSql()の括り

}//クラスの括り
