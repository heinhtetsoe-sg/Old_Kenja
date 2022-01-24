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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＡ１４２Ｃ ＞  職員名札
 */
public class KNJA142C {

    private static final Log log = LogFactory.getLog(KNJA142C.class);

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

            printSvfMainStaff(db2, svf, param);

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
        log.info(" form = " + form);
        if (param._isOutputDebug) {
            log.info(" form = " + form);
        }
    }

    /** 帳票出力 **/
    private void printSvfMainStaff(
        final DB2UDB db2,
        final Vrw32alp svf,
        final Param param
    ) {
        final int maxLine = 5;
        final int maxCol  = 2;

        PreparedStatement ps = null;
        ResultSet rs = null;

        String schoolstampPath = null;
        final File schoolstampFile = param.getImageFile("SCHOOLSTAMP_H.bmp");
        if (null != schoolstampFile) {
            schoolstampPath = schoolstampFile.getAbsolutePath();
        }
        log.info(" schoolstampPath = " + schoolstampPath);

        try {
            final String sql = sql(param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            setForm(svf, param, "KNJA142C.xml", 1);

            int line = 1;
            int col  = 1;
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

                //校章
                svf.VrsOutn("LOGO" + col, line, schoolstampPath);

                //学校名
                {
                    final int schoolNameLen = KNJ_EditEdit.getMS932ByteLength(param._schoolName);
                    final String schoolNameField = (schoolNameLen <= 30)? "_1": "_2";
                    svf.VrsOutn("SCHOOL_NAME" + col + schoolNameField, line, param._schoolName);
                }

                //役職名
                svf.VrsOutn("JOBNAME" + col, line, rs.getString("JOBNAME"));

                //職員名
                {
                    final int staffNameLen = KNJ_EditEdit.getMS932ByteLength(rs.getString("STAFFNAME"));
                    final String staffNameField = (staffNameLen <= 20)? "_1": (staffNameLen <= 30)? "_2": "_3";
                    svf.VrsOutn("NAME" + col + staffNameField, line, rs.getString("STAFFNAME"));
                }

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

    /**職員情報**/
    private String sql(final Param param)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT VSM.STAFFCD");
        stb.append("         , JOB.JOBNAME");
        stb.append("         , VSM.STAFFNAME");
        stb.append("      FROM V_STAFF_MST  VSM");
        stb.append(" LEFT JOIN JOB_MST      JOB");
        stb.append("        ON VSM.JOBCD = JOB.JOBCD");
        stb.append("     WHERE VSM.YEAR  = '" + param._year + "'");
        stb.append("       AND VSM.STAFFCD IN " + SQLUtils.whereIn(true, param._schregnos) + " ");
        stb.append("ORDER BY ");
        stb.append("    STAFFCD ");

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
        private final String[] _schregnos;

        private final String _documentRoot;
        private String _imagepath;
        private String _extension;
        final boolean _isOutputDebug;
        private final String _schoolName;
        private final String _schoolKind;


        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");                        //年度
            _schoolKind = request.getParameter("SCHOOLKIND");

            // 職員コードの指定
            _schregnos = request.getParameterValues("category_selected"); //職員コード

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

            _schoolName = getSchoolMst(db2, _year, "000000000000", _schoolKind);
        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJA142C' AND NAME = '" + propName + "' "));
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

        public String getSchoolMst(final DB2UDB db2, final String year, final String schoolcd, final String school_kind) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();

                stb.append("    SELECT SCHOOLNAME1");
                stb.append("      FROM SCHOOL_MST");
                stb.append("     WHERE YEAR        = '" + year + "'");
                stb.append("       AND SCHOOLCD    = '" + schoolcd + "'");
                stb.append("       AND SCHOOL_KIND = '" + school_kind + "'");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOLNAME1");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}//クラスの括り
