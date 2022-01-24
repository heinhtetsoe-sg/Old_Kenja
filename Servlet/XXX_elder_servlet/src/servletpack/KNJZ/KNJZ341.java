// kanji=漢字
/*
 * $Id: 748498189ffe9a0e0aa5a216c5eae86caa9f8294 $
 *
 * 作成日: 2019/08/05 20:00:00 - JST
 * 作成者: yogi
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJZ341 {

    private static final Log log = LogFactory.getLog("KNJZ341.class");

    Param _param;
	private static boolean _hasData = false;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;

        try {
            // ＤＢ接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(request, db2);
            // print設定
            response.setContentType("application/pdf");

            // svf設定
            svf.VrInit();                           //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());      //PDFファイル名の設定

            printMain(db2, svf);
            //  該当データ無し
            if (_hasData == false) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            // 終了処理
            if (null != db2) {
                db2.close();        // DBを閉じる
            }
            svf.VrQuit();
        }

    }

    /** 出力処理 */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        List staffList = getStaffInfo(db2);
        svf.VrSetForm("KNJZ341.frm", 1);
        final int rowMaxCnt = 7;
        final int colMaxCnt = 6;
        int rowCnt = 1;
        int colCnt = 1;
        int pageCnt = 1;
        for (Iterator ite = staffList.iterator();ite.hasNext();) {
            PrintData outwk = (PrintData)ite.next();
            if (colCnt > colMaxCnt) {
                rowCnt++;
                colCnt = 1;
            }
            if (rowCnt > rowMaxCnt) {
                rowCnt = 1;
                colCnt = 1;
                pageCnt++;
                svf.VrEndPage();
            }
            if (rowCnt == 1 && colCnt == 1) {
                svf.VrsOut("NENDO",KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度");
                svf.VrsOut("SEMESTER", _param._semesterName);
                svf.VrsOut("TODAY", KNJ_EditDate.h_format_JP(db2, _param._date));
                svf.VrsOut("PAGE",pageCnt + "頁");
            }
            final String fName = "T" + outwk._staffcd;
            final String fpath = _param.getImageFilePath("image", fName, "jpg");
            if (!"".equals(StringUtils.defaultString(fpath))) {
                svf.VrsOutn("Bitmap_Field"+colCnt, rowCnt, fpath);
            } else {
                svf.VrsOutn("NO_DATA"+colCnt, rowCnt, "データなし");
                svf.VrsOutn("ATTENDNO"+colCnt, rowCnt, fName + ".jpg");
            }
            svf.VrsOutn("field1_"+colCnt, rowCnt, outwk._jobname);
            svf.VrsOutn("field2_"+colCnt, rowCnt, outwk._staffcd);
            svf.VrsOutn("NAME_SHOW_"+colCnt, rowCnt, outwk._staffname);
            svf.VrsOutn("PHONE"+colCnt, rowCnt, outwk._stafftelno);
            colCnt++;
            _hasData = true;
        }
        if (_hasData == true) {
            svf.VrEndPage();
        }
    }

    private List getStaffInfo(final DB2UDB db2) {
        List retList = new ArrayList();
        final String sql = getStaffInfoSql();
        try {
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                final String staffcd = rs.getString("STAFFCD");
                final String sectioncd = rs.getString("SECTIONCD");
                final String sectionabbv = rs.getString("SECTIONABBV");
                final String jobcd = rs.getString("JOBCD");
                final String jobname = rs.getString("JOBNAME");
                final String staffname = rs.getString("STAFFNAME");
                final String stafftelno = rs.getString("STAFFTELNO");
                PrintData addwk = new PrintData(staffcd, sectioncd, sectionabbv, jobcd, jobname, staffname, stafftelno);
                retList.add(addwk);
            }
        } catch (Exception ex) {
        }
    	return retList;
    }

    private String getStaffInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.STAFFCD, ");
        stb.append("   T1.SECTIONCD, ");
        stb.append("   CHAR(T4.SECTIONABBV,6) AS SECTIONABBV, ");
        stb.append("   T1.JOBCD, ");
        stb.append("   VALUE(T5.JOBNAME,'') AS JOBNAME, ");
        stb.append("   T1.STAFFNAME, ");
        stb.append("   T1.STAFFTELNO ");
        stb.append(" FROM ");
        stb.append("   STAFF_MST T1 ");
        stb.append("   INNER JOIN STAFF_YDAT T7 ON (T7.STAFFCD = T1.STAFFCD) ");
        stb.append("   LEFT JOIN SECTION_MST T4 ON (T4.SECTIONCD = T1.SECTIONCD) ");
        stb.append("   LEFT JOIN JOB_MST T5 ON (T5.JOBCD = T1.JOBCD) ");
        stb.append(" WHERE ");
        stb.append("   T7.YEAR = '" + _param._year + "' ");
        stb.append(" ORDER BY ");
        stb.append("   T1.JOBCD, ");
        stb.append("   T1.SECTIONCD, ");
        stb.append("   T1.STAFFCD ");
        return stb.toString();
    }

    private class PrintData {
        final String _staffcd;
        final String _sectioncd;
        final String _sectionabbv;
        final String _jobcd;
        final String _jobname;
        final String _staffname;
        final String _stafftelno;
        PrintData (
            final String staffcd, final String sectioncd, final String sectionabbv, final String jobcd,
            final String jobname, final String staffname, final String stafftelno
            ) {
            _staffcd = staffcd;
            _sectioncd = sectioncd;
            _sectionabbv = sectionabbv;
            _jobcd = jobcd;
            _jobname = jobname;
            _staffname = staffname;
            _stafftelno = stafftelno;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) throws Exception {
        final Param param = new Param(request, db2);
        log.fatal("$Revision: 69095 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    protected class Param {
        final String _year;
        final String _date;
        final String _semester;
        final String _documentRoot;
        final String _semesterName;

        Param(final HttpServletRequest request, final DB2UDB db2) throws SQLException {
            _year  = request.getParameter("YEAR");          //年度
            _date  = request.getParameter("DATE");          //日付
            _semester  = request.getParameter("SEMESTER");  //学期（学籍処理日から取得）
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _semesterName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));
        }

        String getImageFilePath(final String imageDir, final String filename, final String ext) {
            if (null == _documentRoot || null == imageDir || null == ext || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentRoot).append("/").append(imageDir).append("/").append(filename).append(".").append(ext);
            final File file = new File(path.toString());
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }

    }
}

// eof
