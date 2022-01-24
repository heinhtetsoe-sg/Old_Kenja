/*
 * $Id: 099c083966cc3319d8833cf17db29025a7aa2497 $
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 京都府修学金 修学支援特別融資利用申請者（予約）名簿
 */
public class KNJTP083 {

    private static final Log log = LogFactory.getLog(KNJTP083.class);

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
    
    private void printHeader(final Vrw32alp svf, final Param param) {
        svf.VrsOut("DATE", param._shugakuDate.formatDate(param._ctrlDate, false));
        svf.VrsOut("YEAR", param._shugakuDate.gengou(param._shinseiYear) + "年度");
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final List list = KojinTaiyoYoyakuHist.load(db2, _param);
        
        final List schoolList = new ArrayList();
        List taiyoyoyakuList0 = null;
        String schoolcd = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final KojinTaiyoYoyakuHist taiyoyoyaku = (KojinTaiyoYoyakuHist) it.next();
            if (null == schoolcd || !schoolcd.equals(taiyoyoyaku._jSchoolCd)) {
                taiyoyoyakuList0 = new ArrayList();
                schoolList.add(taiyoyoyakuList0);
            }
            schoolcd = taiyoyoyaku._jSchoolCd;
            taiyoyoyakuList0.add(taiyoyoyaku);
        }
        
        final int maxLine = 50;
        log.debug(" schoolList = " + schoolList);
        for (final Iterator it = schoolList.iterator(); it.hasNext();) {
            final List taiyoyoyakuList = (List) it.next();
            
            svf.VrSetForm("KNJTP083.frm", 1);
            printHeader(svf, _param);
            
            int line = 0;
            int no = 0;
            for (final Iterator taiyoIt = taiyoyoyakuList.iterator(); taiyoIt.hasNext();) {
                final KojinTaiyoYoyakuHist taiyoyoyaku = (KojinTaiyoYoyakuHist) taiyoIt.next();
                if (line >= maxLine) {
                    svf.VrEndPage();
                    line = 0;
                }
                line += 1;
                no += 1;
                svf.VrsOutn("NO", line, String.valueOf(no));
                svf.VrsOutn("SCHOOL_NAME", line, taiyoyoyaku._schoolName);
                svf.VrsOutn("SCHOOL_CD", line, taiyoyoyaku._jSchoolCd);
                if (null != taiyoyoyaku._shinken1) {
                    svf.VrsOutn("APPLI_NAME", line, taiyoyoyaku._shinken1.getName());
                }
                svf.VrsOutn("NAME", line, taiyoyoyaku.getKojinName());
                
                _hasData = true;
            }
            if (line > 0) {
                svf.VrEndPage();
            }
        }
    }
    
    private static class KojinTaiyoYoyakuHist implements Comparable {
        final String _kojinNo;
        final String _shinseiYear;
        final String _shikinShousaiDiv;
        final String _ukeYear;
        final String _ukeNo;
        final String _ukeEdaban;
        final String _yoyakuShinseiDate;
        final String _shuugakuNo;
        final String _nenrei;
        final String _kikonFlg;
        final String _jSchoolCd;
        final String _jGradDiv;
        final String _jGradYm;
        final String _kibouHSchoolDiv;
        final String _yoyakuKibouGk;
        final String _sYoyakuKibouYm;
        final String _eYoyakuKibouYm;
        final String _shitakukinKibouFlg;
        final String _shinseiDiv;
        final String _rentaiCd;
        final String _shinken1Cd;
        final String _shinken2Cd;
        final String _shutaruCd;
        final String _shinseiKanryouFlg;
        final String _shinseiCancelFlg;
        final String _shinseiCancelDate;
        final String _ketteiDate;
        final String _ketteiFlg;
        final String _schoolName;
    
        ShinkenshaHistDat _shinken1 = null;
        String _kojinFamillyName;
        String _kojinFirstName;
        String _kojinFamillyNameKana;
        String _kojinFirstNameKana;
    
        KojinTaiyoYoyakuHist(
                final String kojinNo,
                final String shinseiYear,
                final String shikinShousaiDiv,
                final String ukeYear,
                final String ukeNo,
                final String ukeEdaban,
                final String yoyakuShinseiDate,
                final String shuugakuNo,
                final String nenrei,
                final String kikonFlg,
                final String jSchoolCd,
                final String jGradDiv,
                final String jGradYm,
                final String kibouHSchoolDiv,
                final String yoyakuKibouGk,
                final String sYoyakuKibouYm,
                final String eYoyakuKibouYm,
                final String shitakukinKibouFlg,
                final String shinseiDiv,
                final String rentaiCd,
                final String shinken1Cd,
                final String shinken2Cd,
                final String shutaruCd,
                final String shinseiKanryouFlg,
                final String shinseiCancelFlg,
                final String shinseiCancelDate,
                final String ketteiDate,
                final String ketteiFlg,
                final String schoolName
        ) {
            _kojinNo = kojinNo;
            _shinseiYear = shinseiYear;
            _shikinShousaiDiv = shikinShousaiDiv;
            _ukeYear = ukeYear;
            _ukeNo = ukeNo;
            _ukeEdaban = ukeEdaban;
            _yoyakuShinseiDate = yoyakuShinseiDate;
            _shuugakuNo = shuugakuNo;
            _nenrei = nenrei;
            _kikonFlg = kikonFlg;
            _jSchoolCd = jSchoolCd;
            _jGradDiv = jGradDiv;
            _jGradYm = jGradYm;
            _kibouHSchoolDiv = kibouHSchoolDiv;
            _yoyakuKibouGk = yoyakuKibouGk;
            _sYoyakuKibouYm = sYoyakuKibouYm;
            _eYoyakuKibouYm = eYoyakuKibouYm;
            _shitakukinKibouFlg = shitakukinKibouFlg;
            _shinseiDiv = shinseiDiv;
            _rentaiCd = rentaiCd;
            _shinken1Cd = shinken1Cd;
            _shinken2Cd = shinken2Cd;
            _shutaruCd = shutaruCd;
            _shinseiKanryouFlg = shinseiKanryouFlg;
            _shinseiCancelFlg = shinseiCancelFlg;
            _shinseiCancelDate = shinseiCancelDate;
            _ketteiDate = ketteiDate;
            _ketteiFlg = ketteiFlg;
            _schoolName = schoolName;
        }
        
        public String getKojinName() {
            return StringUtils.defaultString(_kojinFamillyName) + "　" +  StringUtils.defaultString(_kojinFirstName);
        }
        
        public String getKojinNameKana() {
            return StringUtils.defaultString(_kojinFamillyNameKana) + "　" +  StringUtils.defaultString(_kojinFirstNameKana);
        }
        
        public int compareTo(final Object o0) {
            final KojinTaiyoYoyakuHist o = (KojinTaiyoYoyakuHist) o0;
            final int cmpSch = _jSchoolCd.compareTo(o._jSchoolCd);
            if (0 != cmpSch) {
                return cmpSch;
            }
            final int cmpKana = getKojinNameKana().compareTo(o.getKojinNameKana());
            if (0 != cmpKana) {
                return cmpSch;
            }
            return _kojinNo.compareTo(o._kojinNo);
        }
    
        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            Collection shinkenCds = new HashSet();
            try {
                ps = db2.prepareStatement(sql(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String kojinNo = rs.getString("KOJIN_NO");
                    final String shinseiYear = rs.getString("SHINSEI_YEAR");
                    final String shikinShousaiDiv = rs.getString("SHIKIN_SHOUSAI_DIV");
                    final String ukeYear = rs.getString("UKE_YEAR");
                    final String ukeNo = rs.getString("UKE_NO");
                    final String ukeEdaban = rs.getString("UKE_EDABAN");
                    final String yoyakuShinseiDate = rs.getString("YOYAKU_SHINSEI_DATE");
                    final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                    final String nenrei = rs.getString("NENREI");
                    final String kikonFlg = rs.getString("KIKON_FLG");
                    final String jSchoolCd = rs.getString("J_SCHOOL_CD");
                    final String jGradDiv = rs.getString("J_GRAD_DIV");
                    final String jGradYm = rs.getString("J_GRAD_YM");
                    final String kibouHSchoolDiv = rs.getString("KIBOU_H_SCHOOL_DIV");
                    final String yoyakuKibouGk = rs.getString("YOYAKU_KIBOU_GK");
                    final String sYoyakuKibouYm = rs.getString("S_YOYAKU_KIBOU_YM");
                    final String eYoyakuKibouYm = rs.getString("E_YOYAKU_KIBOU_YM");
                    final String shitakukinKibouFlg = rs.getString("SHITAKUKIN_KIBOU_FLG");
                    final String shinseiDiv = rs.getString("SHINSEI_DIV");
                    final String rentaiCd = rs.getString("RENTAI_CD");
                    final String shinken1Cd = rs.getString("SHINKEN1_CD");
                    final String shinken2Cd = rs.getString("SHINKEN2_CD");
                    final String shutaruCd = rs.getString("SHUTARU_CD");
                    final String shinseiKanryouFlg = rs.getString("SHINSEI_KANRYOU_FLG");
                    final String shinseiCancelFlg = rs.getString("SHINSEI_CANCEL_FLG");
                    final String shinseiCancelDate = rs.getString("SHINSEI_CANCEL_DATE");
                    final String ketteiDate = rs.getString("KETTEI_DATE");
                    final String ketteiFlg = rs.getString("KETTEI_FLG");
                    final String schoolName = rs.getString("SCHOOL_NAME");
                    
                    final KojinTaiyoYoyakuHist taiyoyoyaku = new KojinTaiyoYoyakuHist(kojinNo, shinseiYear, shikinShousaiDiv, ukeYear, ukeNo, ukeEdaban, yoyakuShinseiDate, shuugakuNo, nenrei, kikonFlg, jSchoolCd, jGradDiv, jGradYm, kibouHSchoolDiv, yoyakuKibouGk, sYoyakuKibouYm, eYoyakuKibouYm, shitakukinKibouFlg, shinseiDiv, rentaiCd, shinken1Cd, shinken2Cd, shutaruCd, shinseiKanryouFlg, shinseiCancelFlg, shinseiCancelDate, ketteiDate, ketteiFlg, schoolName);
                    list.add(taiyoyoyaku);
                    if (null != taiyoyoyaku._shinken1Cd) {
                        shinkenCds.add(taiyoyoyaku._shinken1Cd);
                    }
                }
           } catch (Exception ex) {
                log.fatal("exception!", ex);
           } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
           }
           if (!list.isEmpty()) {
               Collections.sort(list);
               setKojinName(db2, list, param);
               final Map shinkenshaHists = ShinkenshaHistDat.load(db2, shinkenCds);
               for (final Iterator it = list.iterator(); it.hasNext();) {
                   final KojinTaiyoYoyakuHist taiyoyoyaku = (KojinTaiyoYoyakuHist) it.next();
                   if (null != taiyoyoyaku._shinken1Cd) {
                       taiyoyoyaku._shinken1 = (ShinkenshaHistDat) shinkenshaHists.get(taiyoyoyaku._shinken1Cd);
                   }
               }
           }
    
           return list;
        }
        
        private static void setKojinName(final DB2UDB db2, final Collection taiyoyoyakuList, final Param param) {
            final Set kojinNos = new HashSet();
            for (final Iterator it = taiyoyoyakuList.iterator(); it.hasNext();) {
                final KojinTaiyoYoyakuHist taiyoyoyaku = (KojinTaiyoYoyakuHist) it.next();
                kojinNos.add(taiyoyoyaku._kojinNo);
            }
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map nameMap = new HashMap();
            try {
                final String sql = sqlKojinHistDat(kojinNos);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = new HashMap();
                    m.put("FAMILY_NAME", rs.getString("FAMILY_NAME"));
                    m.put("FIRST_NAME", rs.getString("FIRST_NAME"));
                    m.put("FAMILY_NAME_KANA", rs.getString("FAMILY_NAME_KANA"));
                    m.put("FIRST_NAME_KANA", rs.getString("FIRST_NAME_KANA"));
                    nameMap.put(rs.getString("KOJIN_NO"), m);
                }
                
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = taiyoyoyakuList.iterator(); it.hasNext();) {
                final KojinTaiyoYoyakuHist taiyoyoyaku = (KojinTaiyoYoyakuHist) it.next();
                final Map name = (Map) nameMap.get(taiyoyoyaku._kojinNo);
                if (null != name) {
                    taiyoyoyaku._kojinFamillyName = (String) name.get("FAMILY_NAME");
                    taiyoyoyaku._kojinFirstName = (String) name.get("FIRST_NAME");
                    taiyoyoyaku._kojinFamillyNameKana = (String) name.get("FAMILY_NAME_KANA");
                    taiyoyoyaku._kojinFirstNameKana = (String) name.get("FIRST_NAME_KANA");
                }
            }
        }
        
        private static String sqlKojinHistDat(final Collection kojinNos) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_HIST AS ( ");
            stb.append("     SELECT KOJIN_NO, MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("     FROM KOJIN_HIST_DAT  ");
            stb.append("     WHERE KOJIN_NO IN " + SQLUtils.whereIn(true, toArray(kojinNos)) + " ");
            stb.append("     GROUP BY KOJIN_NO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.KOJIN_NO, ");
            stb.append("     T1.FAMILY_NAME, ");
            stb.append("     T1.FIRST_NAME, ");
            stb.append("     T1.FAMILY_NAME_KANA, ");
            stb.append("     T1.FIRST_NAME_KANA ");
            stb.append(" FROM KOJIN_HIST_DAT T1  ");
            stb.append(" INNER JOIN MAX_HIST T2 ON T2.KOJIN_NO = T1.KOJIN_NO AND T2.ISSUEDATE = T1.ISSUEDATE ");
            return stb.toString();
        }
    
        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.*, ");
            stb.append("     T2.NAME AS SCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("     KOJIN_TAIYOYOYAKU_HIST_DAT T1 ");
            stb.append("     LEFT JOIN SCHOOL_DAT T2 ON T2.SCHOOLCD = T1.J_SCHOOL_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.SHINSEI_YEAR = '" + param._shinseiYear + "' ");
            stb.append("     AND SHIKIN_SHOUSAI_DIV = '06' ");
            stb.append("     AND T1.J_SCHOOL_CD IN " + SQLUtils.whereIn(true, param._classSelected));
            return stb.toString();
        }
    }

    private static class ShinkenshaHistDat {
        
        final String _shinkenCd;
        final String _issuedate;
        final String _familyName;
        final String _firstName;
        final String _familyNameKana;
        final String _firstNameKana;
        final String _birthday;
        final String _shinseiNenrei;
        final String _zipcd;
        final String _citycd;
        final String _addr1;
        final String _addr2;
        final String _telno1;
        final String _telno2;

        ShinkenshaHistDat(
                final String shinkenCd,
                final String issuedate,
                final String familyName,
                final String firstName,
                final String familyNameKana,
                final String firstNameKana,
                final String birthday,
                final String shinseiNenrei,
                final String zipcd,
                final String citycd,
                final String addr1,
                final String addr2,
                final String telno1,
                final String telno2
        ) {
            _shinkenCd = shinkenCd;
            _issuedate = issuedate;
            _familyName = familyName;
            _firstName = firstName;
            _familyNameKana = familyNameKana;
            _firstNameKana = firstNameKana;
            _birthday = birthday;
            _shinseiNenrei = shinseiNenrei;
            _zipcd = zipcd;
            _citycd = citycd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telno1 = telno1;
            _telno2 = telno2;
        }
        
        public String getName() {
            return StringUtils.defaultString(_familyName) + "　" +  StringUtils.defaultString(_firstName);
        }

        public String getKana() {
            return StringUtils.defaultString(_familyNameKana) + "　" +  StringUtils.defaultString(_firstNameKana);
        }

        public static Map load(final DB2UDB db2, final Collection shinkenCdSet) {
            Map map = new HashMap();
            if (shinkenCdSet.isEmpty()) {
                return map;
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                 ps = db2.prepareStatement(sql(shinkenCdSet));
                 rs = ps.executeQuery();
                 while (rs.next()) {
                     final String shinkenCd = rs.getString("SHINKEN_CD");
                     final String issuedate = rs.getString("ISSUEDATE");
                     final String familyName = rs.getString("FAMILY_NAME");
                     final String firstName = rs.getString("FIRST_NAME");
                     final String familyNameKana = rs.getString("FAMILY_NAME_KANA");
                     final String firstNameKana = rs.getString("FIRST_NAME_KANA");
                     final String birthday = rs.getString("BIRTHDAY");
                     final String shinseiNenrei = rs.getString("SHINSEI_NENREI");
                     final String zipcd = rs.getString("ZIPCD");
                     final String citycd = rs.getString("CITYCD");
                     final String addr1 = rs.getString("ADDR1");
                     final String addr2 = rs.getString("ADDR2");
                     final String telno1 = rs.getString("TELNO1");
                     final String telno2 = rs.getString("TELNO2");
                     ShinkenshaHistDat shinkensha = new ShinkenshaHistDat(shinkenCd, issuedate,familyName, firstName, familyNameKana, firstNameKana,
                             birthday, shinseiNenrei, zipcd, citycd, addr1, addr2, telno1, telno2);
                     map.put(shinkenCd, shinkensha);
                 }
            } catch (Exception ex) {
                 log.fatal("exception!", ex);
            } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
            }
            return map;
        }

        public static String sql(final Collection shinkenCdSet) {
            final StringBuffer stb = new StringBuffer();
//            stb.append(" WITH MAX_DATE AS ( ");
//            stb.append("   SELECT SHINKEN_CD, MAX(ISSUEDATE) AS ISSUEDATE ");
//            stb.append("   FROM SHINKENSHA_HIST_DAT ");
//            stb.append("   WHERE SHINKEN_CD IN " + SQLUtils.whereIn(true, toArray(shinkenCdSet)) + " ");
//            stb.append("   GROUP BY SHINKEN_CD ");
//            stb.append(" ) ");
          stb.append(" SELECT * ");
          stb.append(" FROM SHINKENSHA_HIST_DAT T1 ");
          stb.append("   WHERE SHINKEN_CD IN " + SQLUtils.whereIn(true, toArray(shinkenCdSet)) + " ");
//          stb.append(" INNER JOIN MAX_DATE T2 ON T2.SHINKEN_CD = T1.SHINKEN_CD AND T2.ISSUEDATE = T1.ISSUEDATE ");
            return stb.toString();
        }
    }
    
    private static String[] toArray(final Collection col) {
        final String[] array = new String[col.size()];
        int i = 0;
        for (final Iterator it = col.iterator(); it.hasNext(); i++) {
            array[i] = (String) it.next();
        }
        return array;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 67230 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _shinseiYear;
        private final String _ctrlDate;
        private final String[] _classSelected;
        private final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shinseiYear = request.getParameter("SHINSEI_YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
        }
    }
}

// eof

