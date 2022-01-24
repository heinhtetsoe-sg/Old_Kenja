// kanji=漢字
/*
 * $Id: 35b87e13e9858095e83e5598c62b92d05eca682d $
 *
 * 作成日: 2014/06/11 19:21:57 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2014 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 35b87e13e9858095e83e5598c62b92d05eca682d $
 */
public class KNJL413_B {
    private static final Log log = LogFactory.getLog("KNJL413_B.class");

    private boolean _hasData;

    Param _param;

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

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

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException, UnsupportedEncodingException {
        final List printList = getPrintList(db2);
        svf.VrSetForm("KNJL413_A.frm", 1);
        int gyou = 1;
        int retu = 1;
        final int gyouMax = 3;
        final int retuMax = 6;
        for (final Iterator iter = printList.iterator(); iter.hasNext();) {
            final RecruitData recruitData = (RecruitData) iter.next();
            if(gyou > gyouMax){
                gyou = 1;
                retu++;
                if(retu > retuMax){
                    svf.VrEndPage();
                    retu = 1;
                }
            }
            if (!StringUtils.isBlank(recruitData._zipCd)) {
                svf.VrsOutn("ZIPCODE" + gyou ,retu ,"〒" + recruitData._zipCd); //郵便番号
            }
            int check_len = 0;
            int check_len2 = 0;
            if (recruitData._addr1 != null){
                check_len  = recruitData._addr1.getBytes("MS932").length;
            }
            if (recruitData._addr2 != null){
                check_len2 = recruitData._addr2.getBytes("MS932").length;
            }
            if (check_len > 40 || check_len2 > 40) {
                svf.VrsOutn("ADDRESS"  + gyou + "_1_2" ,retu ,recruitData._addr1);     //住所
                svf.VrsOutn("ADDRESS"  + gyou + "_2_2" ,retu ,recruitData._addr2);     //住所
                log.debug("40以上" + recruitData._addr1);
            } else if (check_len > 0 || check_len2 > 0) {
                svf.VrsOutn("ADDRESS"  + gyou + "_1_1" ,retu ,recruitData._addr1);     //住所
                svf.VrsOutn("ADDRESS"  + gyou + "_2_1" ,retu ,recruitData._addr2);     //住所
                log.debug("40以下" + recruitData._addr1);
            }

            if (!StringUtils.isBlank(recruitData._gName)) {
                svf.VrsOutn("NAME" + gyou + "_1",retu , recruitData._gName + "　様");  //名称
            }
            final String name2 = recruitData._recruitNo;
            svf.VrsOutn("NAME" + gyou + "_2",retu ,name2); //名称
            gyou++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private List getPrintList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String recruitSql = getRecruitSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(recruitSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String recruitNo = rs.getString("RECRUIT_NO");
                final String name = rs.getString("NAME");
                final String kana = rs.getString("NAME_KANA");
                final String zipCd = rs.getString("ZIPCD");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String gName = rs.getString("GUARD_NAME");
                final String gKana = rs.getString("GUARD_KANA");
                final RecruitData recruitData = new RecruitData(recruitNo, name, kana, zipCd, addr1, addr2, gName, gKana);
                retList.add(recruitData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    /**
     * @return
     */
    private String getRecruitSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("    * ");
        stb.append(" FROM ");
        stb.append("    RECRUIT_DAT ");
        stb.append(" WHERE ");
        stb.append("    YEAR = '" + _param._year + "' ");
        stb.append("    AND RECRUIT_NO IN " + _param._recruitInState + " ");
        stb.append(" ORDER BY ");
        stb.append("    RECRUIT_NO ");
        log.debug(stb.toString());
        return stb.toString();
    }

    private static class RecruitData {
        final String _recruitNo;
        final String _name;
        final String _kana;
        final String _zipCd;
        final String _addr1;
        final String _addr2;
        final String _gName;
        final String _gKana;
        /**
         * コンストラクタ。
         */
        public RecruitData(
                final String recruitNo,
                final String name,
                final String kana,
                final String zipCd,
                final String addr1,
                final String addr2,
                final String gName,
                final String gKana
        ) {
            _recruitNo = recruitNo;
            _name = name;
            _kana = kana;
            _zipCd = zipCd;
            _addr1 = addr1;
            _addr2 = addr2;
            _gName = gName;
            _gKana = gKana;
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        final String[] _classSelected;
        final String _recruitInState;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _classSelected = request.getParameterValues("CATEGORY_SELECTED");   // 学籍番号
            StringBuffer sbx = new StringBuffer();
            sbx.append("(");
            for(int ia=0 ; ia<_classSelected.length ; ia++){
                if(_classSelected[ia] == null)   break;
                if(ia>0)    sbx.append(",");
                sbx.append("'");
                int i = _classSelected[ia].indexOf("-");
                if (-1 < i) {
                    sbx.append(_classSelected[ia].substring(0,i));
                } else {
                    sbx.append(_classSelected[ia]);
                }
                sbx.append("'");
            }
            sbx.append(")");
            _recruitInState = sbx.toString();
        }

    }
}

// eof
