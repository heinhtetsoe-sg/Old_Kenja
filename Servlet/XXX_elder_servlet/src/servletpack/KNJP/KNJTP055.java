/*
 * $Id: ad4124bbe02e27a36585a67c064862bdc1a63019 $
 *
 * 作成日: 2012/09/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 京都府修学金 高等学校修学金貸与台帳 / 修学支度金貸与台帳
 */
public class KNJTP055 {

    private static final Log log = LogFactory.getLog(KNJTP055.class);

    private boolean _hasData;
    
    private Param _param;
    
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
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }
    

    private static class Kojin {
        final String _shuugakuNo;
        final String _name;
        final String _hSchoolCd;
        final String _schoolName;
        final String _katei;
        final String _sumShishutsuYoteiGk;
        final String _dataDiv;

        Kojin(final String shuugakuNo, final String name, final String hSchoolCd, final String schoolName, final String katei, final String sumShishutsuYoteiGk, final String dataDiv) {
            _shuugakuNo = shuugakuNo;
            _name = name;
            _hSchoolCd = hSchoolCd;
            _schoolName = schoolName;
            _katei = katei;
            _sumShishutsuYoteiGk = sumShishutsuYoteiGk;
            _dataDiv = dataDiv;
        }

        public static List getKojinList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.info(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                    final String name = rs.getString("NAME");
                    final String hSchoolCd = rs.getString("H_SCHOOL_CD");
                    final String schoolName = rs.getString("SCHOOL_NAME");
                    final String katei = rs.getString("KATEI");
                    final String sumShishutsuYoteiGk = rs.getString("SUM_SHISHUTSU_YOTEI_GK");
                    final Kojin kojin = new Kojin(shuugakuNo, name, hSchoolCd, schoolName, katei, sumShishutsuYoteiGk, rs.getString("DATA_DIV"));
                    list.add(kojin);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SUM_TAIYO_KEIKAKU AS ( ");
            stb.append(" SELECT ");
            stb.append("     SHUUGAKU_NO, ");
            stb.append("     SHINSEI_YEAR, ");
            stb.append("     SUM(SHISHUTSU_YOTEI_GK) AS SUM_SHISHUTSU_YOTEI_GK ");
            stb.append(" FROM ");
            stb.append("     TAIYO_KEIKAKU_DAT ");
            stb.append(" WHERE ");
            stb.append("     SHINSEI_YEAR = '" + param._shinseiYear + "' ");
            stb.append(" GROUP BY ");
            stb.append("     SHUUGAKU_NO, ");
            stb.append("     SHINSEI_YEAR ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SHUUGAKU_NO, ");
            stb.append("     L1.FAMILY_NAME || '　' || L1.FIRST_NAME AS NAME, ");
            stb.append("     T1.H_SCHOOL_CD, ");
            stb.append("     L2.NAME AS SCHOOL_NAME, ");
            stb.append("     T1.KATEI, ");
            stb.append("     L3.SUM_SHISHUTSU_YOTEI_GK ");
            stb.append("   , CASE  ");
            stb.append("        WHEN T1.SHIKIN_SHOUSAI_DIV IN ('02', '08') THEN '1' ");
            stb.append("        WHEN T1.SHIKIN_SHOUSAI_DIV IN ('03', '05') THEN '2' ");
            stb.append("     END AS DATA_DIV ");
            stb.append(" FROM ");
            stb.append("     KOJIN_SHINSEI_HIST_DAT T1 ");
            stb.append("     LEFT JOIN KOJIN_HIST_DAT L1 ON L1.KOJIN_NO = T1.KOJIN_NO ");
            stb.append("     LEFT JOIN SCHOOL_DAT L2 ON L2.SCHOOLCD = T1.H_SCHOOL_CD ");
            stb.append("     LEFT JOIN SUM_TAIYO_KEIKAKU L3 ON L3.SHUUGAKU_NO = T1.SHUUGAKU_NO ");
            stb.append("                                   AND L3.SHINSEI_YEAR = T1.SHINSEI_YEAR ");
            stb.append(" WHERE ");
            stb.append("     T1.SHINSEI_YEAR = '" + param._shinseiYear + "' ");
            stb.append(" AND T1.SHUUGAKU_NO IS NOT NULL ");
            stb.append(" AND SUBSTR(T1.SHUUGAKU_NO, 1,1) IN ('1', '2') ");
            stb.append(" AND L1.ISSUEDATE = (SELECT  ");
            stb.append("                         MAX(M1.ISSUEDATE)  ");
            stb.append("                     FROM  ");
            stb.append("                         KOJIN_HIST_DAT M1  ");
            stb.append("                     WHERE  ");
            stb.append("                         T1.KOJIN_NO =M1.KOJIN_NO  ");
            stb.append("                     AND FISCALYEAR(M1.ISSUEDATE) <= T1.SHINSEI_YEAR ");
            stb.append("                     )  ");
            if(!StringUtils.isBlank(param._uke)){
                stb.append(" AND T1.SHINSEI_YEAR || '-' || T1.UKE_YEAR || '-' || T1.UKE_NO || '-' || T1.UKE_EDABAN = '" + param._uke + "' ");
            }
            if ("3".equals(param._classDiv)) {
                stb.append(" AND T1.H_SCHOOL_CD IN " + SQLUtils.whereIn(true, param._classSelected) + " ");
            } else if ("2".equals(param._classDiv)) {
                stb.append(" AND T1.SHUUGAKU_NO = '" + param._shuugakuno +"' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.SHINSEI_YEAR, ");
            stb.append("     T1.H_SCHOOL_CD, ");
            stb.append("     T1.KATEI, ");
            stb.append("     L1.KOJIN_NO, ");
            stb.append("     T1.SHUUGAKU_NO ");
            return stb.toString();
        }
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final List kojinList = Kojin.getKojinList(db2, _param);
        
        for (int i = 0; i < kojinList.size(); i++) {
            final Kojin kojin = (Kojin) kojinList.get(i);
            
            final String form;
            if ("1".equals(kojin._dataDiv)) {
                form = "KNJTP055_1.frm";
            } else if ("2".equals(kojin._dataDiv)) {
                form = "KNJTP055_2.frm";
            } else {
                continue;
            }
            svf.VrSetForm(form, 1);
            
            svf.VrsOut("TITLE", _param._shugakuDate.gengou(_param._shinseiYear));
            
            final StringBuffer stb = new StringBuffer();
            stb.append(StringUtils.defaultString(kojin._hSchoolCd));
            stb.append(" ").append(StringUtils.defaultString(kojin._katei));
            stb.append(" ").append(StringUtils.defaultString(kojin._shuugakuNo));
            stb.append(" ").append(StringUtils.defaultString(kojin._name));
            svf.VrsOut("TEXT", stb.toString());

            svf.VrsOut("MONEY", kojin._sumShishutsuYoteiGk);
            
            svf.VrEndPage();
            _hasData = true;
        }
    }

    
    private static int getMS932Length(final String s) {
    	return KNJ_EditEdit.getMS932ByteLength(s);
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 67227 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _shinseiYear;
        final String _classDiv; // 1:すべて 3:学校 2:修学生番号
        final String[] _classSelected; // 学校選択
        final String _shuugakuno; // 修学生番号
        final String _loginDate;
        final String _prgid;
        final String _uke;
        final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shinseiYear = request.getParameter("SHINSEI_YEAR");
            _classDiv = request.getParameter("CLASS_DIV");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _shuugakuno = request.getParameter("SHUUGAKUNO");
            _loginDate = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");
            _uke = request.getParameter("UKE");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
        }

    }
}

// eof

