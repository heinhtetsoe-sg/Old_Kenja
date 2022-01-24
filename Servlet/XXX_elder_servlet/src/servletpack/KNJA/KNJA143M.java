// kanji=漢字
/*
 * $Id: e586f9d2308755de7c88627dbdaa5de470c4357a $
 *
 * 作成日: 2005/03/25 16:07:30 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＡ１４３Ｍ＞  職員証
 *
 **/

public class KNJA143M {

    private static final Log log = LogFactory.getLog(KNJA143M.class);

    private boolean _hasdata = false;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        log.fatal("$Revision: 60570 $ $Date: 2018-06-11 14:15:29 +0900 (月, 11 6 2018) $"); // CVSキーワードの取り扱いに注意

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //  svf設定
        svf.VrInit();                               //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());       //PDFファイル名の設定

        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        try {
            //  パラメータの取得
            KNJServletUtils.debugParam(request, log);
            final Param param = new Param(db2, request);
            
            //  ＳＶＦ作成処理
            printMain(db2, svf, param);

        } catch (Exception ex) {
            log.error("parameter error!", ex);
        } finally {
            //  該当データ無し
            if (!_hasdata) {
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

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
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

    private void printMain(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Param param
            ) {
        final int maxLine = 5;
        final int maxCol = 2;
        final List pageList = getPageList(getStaffList(db2, param), maxLine * maxCol);
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            
            final List dataList = (List) pageList.get(pi);

            svf.VrSetForm("KNJA143M_3.frm", 1);
            
            for (int line = 1; line <= maxLine; line++) {
                for (int col = 1; col <= maxCol; col++) {
                    final String field = col == 1 ? "" : "_" + String.valueOf(col);
                    svf.VrsOutn("TITLE" + field, line, "職員証"); // タイトル
                    svf.VrsOutn("SENTENCE" + field, line, "職員"); // 証明文言
                }
            }

            for (int j = 0; j < dataList.size(); j++) {
                final int line = j / maxCol + 1;
                final int col = (j + 1) % maxCol == 0 ? maxCol : (j + 1) % maxCol;
                final String field = col == 1 ? "" : "_" + String.valueOf(col);
                final Map data = (Map) dataList.get(j);
                
                svf.VrsOutn("STAFFNO" + field, line, getString(data, "STAFFCD")); // 職員番号
                svf.VrsOutn("NAME" + field, line, getString(data, "STAFFNAME")); // 職員氏名
                
                final String staffImagePath = param.getImageFilePath("P" + getString(data, "STAFFCD") + "." + param._extensionPhoto);
                if (null != staffImagePath) {
                    svf.VrsOutn("PHOTO_BMP" + field, line, staffImagePath); // 
                }
                if (null != param._schoolLogoPath) {
                    svf.VrsOutn("SCHOOL_LOGO" + field, line, param._schoolLogoPath); // 
                }
                if (null != param._schoolStampPath) {
                    svf.VrsOutn("STAMP_BMP" + field, line, param._schoolStampPath); // 
                }
                if (null != param._termSdate) {
                    svf.VrsOutn("SDATE1" + field, line, KNJ_EditDate.h_format_JP(param._termSdate)); // 発行日
                }
                svf.VrsOutn("SCHOOLADDRESS1" + field, line, param._remark2); // 学校所在地
                svf.VrsOutn("STAFFNAME" + field, line, param._principalName); // 役職・氏名
                svf.VrsOutn("SCHOOLNAME1" + field, line, param._schoolName); // 学校名
            }
            svf.VrEndPage();
            
            svf.VrSetForm("KNJA143M_4.frm", 1);
            for (int j = 0; j < dataList.size(); j++) {
                final int line = j / maxCol + 1;
                final int col = (j + 1) % maxCol == 0 ? maxCol : (j + 1) % maxCol;
                final String field = col == 1 ? "" : "_" + String.valueOf(col);

                svf.VrsOutn("SCHOOLNAME1" + field, line, param._remark1); // 学校法人名
                svf.VrsOutn("SCHOOLNAME2" + field, line, param._remark3); // 学校名
                svf.VrsOutn("TELNO" + field, line, param._remark4); // 電話番号
            }
            svf.VrEndPage();
            _hasdata = true;
        }
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    private static String getString(final Map row, final String field) {
        if (row.isEmpty()) {
            return null;
        }
        if (!row.containsKey(field)) {
            throw new IllegalStateException("no such field : " + field + " / " + row);
        }
        return (String) row.get(field);
    }

    private List getStaffList(final DB2UDB db2, final Param param) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = " SELECT * FROM STAFF_MST WHERE STAFFCD IN " + SQLUtils.whereIn(true, param._categorySelected);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Map map = new TreeMap();
                final ResultSetMetaData meta = rs.getMetaData();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    final String field = meta.getColumnName(i);
                    final String data = rs.getString(field);
                    map.put(field, data);
                }
                list.add(map);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _termSdate;
        final String _useAddrField2;
        final String _documentroot;
        final String[] _categorySelected; // 学籍番号
        private String _jobName;
        private String _imagePass;
        private String _extensionPhoto;
        private String _principalName;
        private String _schoolName;
        private String _remark1;
        private String _remark2;
        private String _remark3;
        private String _remark4;
        private String _extensionStamp;
        private boolean _hasCertifSchoolDatRecord = false;
        private String _z010Name1;
        private String _schoolStampPath;
        private String _schoolLogoPath;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _termSdate = request.getParameter("TERM_SDATE").replace('/','-'); //発行日
            _documentroot = request.getParameter("DOCUMENTROOT");
            _categorySelected = request.getParameterValues("category_selected");
            _useAddrField2 = request.getParameter("useAddrField2");
            
            setSchoolInfo(db2);
            setHeader(db2);
            
            _schoolStampPath = getImageFilePath("SCHOOLSTAMP_H" + _extensionStamp);
            _schoolLogoPath = getImageFilePath("SCHOOLLOGO_H" + _extensionStamp);
        }
        
        private String getImageFilePath(final String filename) {
            String path = _documentroot + "/" + _imagePass + "/" + filename;
            final boolean exists = new File(path).exists();
            if (!exists) {
                log.info("not exist file:" + path);
                return null;
            }
            return path;
        }

        public void setSchoolInfo(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final String certifKindcd = "139";
            try {
                final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + certifKindcd + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _jobName =  rs.getString("JOB_NAME");
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _remark1 = rs.getString("REMARK1"); // 学校法人名
                    _remark2 = rs.getString("REMARK2"); // 住所
                    _remark3 = rs.getString("REMARK3");
                    _remark4 = rs.getString("REMARK4"); // 電話番号
                    _hasCertifSchoolDatRecord = true;
                }
            } catch (Exception e) {
                log.error("setHeader name_mst error!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            if (!_hasCertifSchoolDatRecord) {
                //SVF出力
                //職名・職員名取得メソッド
                PreparedStatement ps2 = null;
                try {
                    StringBuffer stb = new StringBuffer();
                    stb.append("SELECT STAFFNAME, ");
                    stb.append("       (SELECT JOBNAME FROM JOB_MST T2 WHERE T2.JOBCD=T1.JOBCD) AS JOBNAME ");
                    stb.append("FROM   V_STAFF_MST T1 ");
                    stb.append("WHERE  YEAR='" + _year + "' AND JOBCD='0001' ");//学校長

                    ps2 = db2.prepareStatement(stb.toString());        //職名・職員名

                    rs = ps2.executeQuery();
                    while (rs.next()) {
                        _jobName  = rs.getString("JOBNAME");    //職名
                        _principalName = rs.getString("STAFFNAME");  //職員名
                    }
                } catch (Exception ex) {
                    log.error("setStaffJobName set error!", ex);
                } finally {
                    DbUtils.closeQuietly(null, ps2, rs);
                    db2.commit();
                }

                //学校名取得メソッド
                PreparedStatement ps3 = null;
                try {
                    final StringBuffer stb = new StringBuffer();
                    stb.append("SELECT SCHOOLNAME1, SCHOOLADDR1 ");
                    stb.append("FROM   SCHOOL_MST ");
                    stb.append("WHERE  YEAR='" + _year + "' ");

                    ps3 = db2.prepareStatement(stb.toString());        //学校名・学校住所

                    rs = ps3.executeQuery();
                    while (rs.next()) {
                        _schoolName = rs.getString("SCHOOLNAME1");    //学校名
                        _remark1 = rs.getString("SCHOOLADDR1");    //学校住所
                    }
                } catch (Exception ex) {
                    log.error("setSchoolName set error!", ex);
                } finally {
                    DbUtils.closeQuietly(null, ps3, rs);
                    db2.commit();
                }
            }
        }
        
        /** 事前処理 **/
        private void setHeader(final DB2UDB db2) {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;

            //  写真データ
            try {
                returnval = getinfo.Control(db2);
                _imagePass = returnval.val4;      //格納フォルダ
                _extensionPhoto = returnval.val5;      //拡張子
            } catch (Exception e) {
                log.error("setHeader set error!", e);
            }

            //  名称マスタ（学校区分）
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                _extensionStamp = ".jpg";
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2='00'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _z010Name1 = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("setHeader name_mst error!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            getinfo = null;
            returnval = null;
        }
    }

}//クラスの括り
