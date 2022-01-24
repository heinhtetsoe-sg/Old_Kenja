// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2021/01/30
 * 作成者: Nutec
 *
 */
package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＡ１４２Ｂ＞  生徒証明書
 */
public class KNJA142B {

    private static final Log log = LogFactory.getLog(KNJA142B.class);

    private boolean nonedata = false;                               //該当データなしフラグ

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
                     throws ServletException, IOException
    {
        final Vrw32alp svf = new Vrw32alp();            //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        PrintWriter outstrm = null;
        try {
            //  print設定
            response.setContentType("application/pdf");
            outstrm = new PrintWriter (response.getOutputStream());

            //  svf設定
            svf.VrInit();                               //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());       //PDFファイル名の設定

            sd.setSvfInit(request, response, svf);
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }

            final Param param = getParam(db2, request);

            printSvfMainStudent(db2, svf, param);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();                //DBを閉じる
            outstrm.close();            //ストリームを閉じる
        }

    }//doGetの括り

    private void setForm(final Vrw32alp svf, final Param param, final String form, final int data) {
        svf.VrSetForm(form, data);
        if (param._isOutputDebug) {
            log.info(" form = " + form);
        }
    }

    /** 帳票出力 **/
    private void printSvfMainStudent(
        final DB2UDB db2,
        final Vrw32alp svf,
        final Param param
    ) {
        final int maxLine = 5;
        final int maxCol  = 2;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql(param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String schoolstampPath = null;
            final File schoolstampFile = param.getImageFile("SCHOOLSTAMP_H.bmp");
            if (null != schoolstampFile) {
                schoolstampPath = schoolstampFile.getAbsolutePath();
            }
            log.info(" schoolstampPath = " + schoolstampPath);

            final String form = "KNJA142B.xml";
            setForm(svf, param, form, 1);
            int line = Integer.parseInt(param._porow);
            int col  = Integer.parseInt(param._pocol);

            while (rs.next()) {

                if (col > maxCol) {
                    col = 1;
                    line++;
                }

                if (line > maxLine) {
                    svf.VrEndPage();
                    line = 1;

                    svf.VrsOut("DUMMY", "　");
                    svf.VrEndPage();
                }

                final String schregno = rs.getString("SCHREGNO");
                String schregimgPath = null;
                final File schregimg = param.getImageFile("P" + schregno + "." + param._extension); //写真データ存在チェック用
                log.info(" schregimg = " + param.getImageFile("P" + schregno + "." + param._extension));
                if (null != schregimg) {
                    schregimgPath = schregimg.getAbsolutePath();
                    log.info(" schregimg = " + schregimgPath);
                }
                svf.VrsOutn("PIC"       + col, line, schregimgPath);
                svf.VrsOutn("LOGO"      + col, line, schoolstampPath);
                svf.VrsOutn("SCHREGNO"  + col, line, schregno);
                svf.VrsOutn("MAJORNAME" + col, line, rs.getString("MAJORNAME"));

                //名前
                {
                    final int nameLen = KNJ_EditEdit.getMS932ByteLength(rs.getString("NAME"));
                    final String nameField = (nameLen <= 24)? "1": "2";
                    svf.VrsOutn("NAME" + col + "_" + nameField, line, rs.getString("NAME"));
                }

                //生年月日
                svf.VrsOutn("BIRTHDAY" + col, line, KNJ_EditDate.h_format_JP(db2, rs.getString("BIRTHDAY")) + "生");

                //住所1
                {
                    final int addr1Len = KNJ_EditEdit.getMS932ByteLength(rs.getString("ADDR1"));
                    final String addr1Field = (addr1Len <= 40)? "1": (addr1Len <= 50)? "2": "3";
                    svf.VrsOutn("ADDR" + col + "_1_" + addr1Field, line, rs.getString("ADDR1"));
                }

                //住所2
                {
                    final int addr2Len = KNJ_EditEdit.getMS932ByteLength(rs.getString("ADDR2"));
                    final String addr2Field = (addr2Len <= 40)? "1": (addr2Len <= 50)? "2": "3";
                    svf.VrsOutn("ADDR" + col + "_2_" + addr2Field, line, rs.getString("ADDR2"));
                }

                svf.VrsOutn("SCHOOL_NAME" + col, line, rs.getString("SCHOOL_NAME") +" " + rs.getString("JOB_NAME"));
                svf.VrsOutn("PRINT_DATE"  + col, line, KNJ_EditDate.h_format_JP(db2, param._termSdate));
                svf.VrsOutn("LIMIT_DATE"  + col, line, KNJ_EditDate.h_format_JP(db2, param._termEdate));

                col++;
                nonedata = true;
            }

            if (nonedata) {
                svf.VrEndPage();
                svf.VrsOut("DUMMY", "　");
                svf.VrEndPage();
            }

        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (nonedata) {
            svf.VrEndPage();
        }
    }

    /**生徒又は職員情報**/
    private String sql(final Param param)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREG_ADDRESS_MAX AS( ");
        stb.append("    SELECT ");
        stb.append("        SCHREGNO, ");
        stb.append("        MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("    FROM  ");
        stb.append("        SCHREG_ADDRESS_DAT ");
        stb.append("    GROUP BY  ");
        stb.append("        SCHREGNO  ");
        stb.append("    )  ");
        stb.append(" SELECT ");
        stb.append("       SRD.SCHREGNO ");
        stb.append("     , MM.MAJORNAME ");
        stb.append("     , SBM.NAME ");
        stb.append("     , SBM.BIRTHDAY ");
        stb.append("     , SAD.ADDR1 ");
        stb.append("     , SAD.ADDR2 ");
        stb.append("     , CSD.SCHOOL_NAME ");
        stb.append("     , CSD.JOB_NAME ");
        stb.append("   FROM ");
        stb.append("       SCHREG_REGD_DAT SRD ");
        stb.append(" LEFT JOIN ");
        stb.append("       SCHREG_BASE_MST SBM ");
        stb.append("    ON SRD.SCHREGNO = SBM.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("       SCHREG_ADDRESS_MAX SAM ");
        stb.append("    ON SRD.SCHREGNO = SAM.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("       SCHREG_ADDRESS_DAT SAD ");
        stb.append("    ON SRD.SCHREGNO = SAD.SCHREGNO ");
        stb.append("   AND SAD.ISSUEDATE = SAM.ISSUEDATE ");
        stb.append(" LEFT JOIN ");
        stb.append("       CERTIF_SCHOOL_DAT CSD ");
        stb.append("    ON SRD.YEAR = CSD.YEAR ");
        stb.append("   AND CSD.CERTIF_KINDCD = '101' ");
        stb.append(" LEFT JOIN ");
        stb.append("       MAJOR_MST MM ");
        stb.append("    ON SRD.COURSECD = MM.COURSECD ");
        stb.append("   AND SRD.MAJORCD = MM.MAJORCD ");
        stb.append("  WHERE SRD.YEAR = '" + param._year + "' ");
        stb.append("    AND SRD.SEMESTER = '" + param._gakki + "' ");
        stb.append("    AND SRD.GRADE||SRD.HR_CLASS||SRD.SCHREGNO IN " + SQLUtils.whereIn(true, param._schregnos) + " ");
        stb.append("ORDER BY ");
        stb.append("       SRD.GRADE, ");
        stb.append("       SRD.HR_CLASS, ");
        stb.append("       SRD.ATTENDNO ");

        return stb.toString();
    }

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision$ $Date$"); // CVSキーワードの取り扱いに注意
        Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        private final String _year;
        private final String _gakki;
        private final String[] _schregnos;
        private final String _termSdate;
        private final String _termEdate;

        private final String _documentRoot;
        private String _imagepath;
        private String _extension;
        final boolean _isOutputDebug;
        private final String _porow;
        private final String _pocol;


        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");                        //年度
            _gakki = request.getParameter("GAKKI");                      //学期
            _termSdate = request.getParameter("TERM_SDATE").replace('/','-');  // 発行日
            _termEdate = null == request.getParameter("TERM_EDATE") ? null : request.getParameter("TERM_EDATE").replace('/','-');  // 有効期限
            _porow = request.getParameter("POROW");
            _pocol = request.getParameter("POCOL");

            // 学籍番号の指定
            _schregnos = request.getParameterValues("category_selected"); //学籍番号

            _documentRoot = request.getParameter("DOCUMENTROOT");
            //  写真データ
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            try {
                returnval = getinfo.Control(db2);
                _imagepath = returnval.val4;      //格納フォルダ
                _extension = returnval.val5;      //拡張子
            } catch (Exception e) {
                log.error("setHeader set error!", e);
            } finally {
                getinfo = null;
                returnval = null;
            }
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJA142B' AND NAME = '" + propName + "' "));
        }

        public File getImageFile(final String name) {
            final File file = new File(_documentRoot + "/" + _imagepath + "/" + name);
            if (_isOutputDebug) {
                log.info(" file " + file.getAbsolutePath() + " exists? " + file.exists());
            }
            if (file.exists()) {
                return file;
            }
            return null;
        }

    }

}//クラスの括り
