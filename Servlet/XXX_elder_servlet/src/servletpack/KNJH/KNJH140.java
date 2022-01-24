// kanji=漢字
/*
 * $Id: db4a72fc6cdcc084d7a40d69058b2df8d6de9640 $
 *
 * 作成日: 2011/02/28 11:32:50 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: db4a72fc6cdcc084d7a40d69058b2df8d6de9640 $
 */
public class KNJH140 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJH140.class");

    private static final String KAZOKU = "1";
    private static final String KINKYU = "2";
    private static final String TUUGAKU = "3";
    private static final String JITENSYA = "4";
    private static final String SONOTA = "5";    
    private static final int STATION_CNT = 7;

    private boolean _hasData;

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void xls_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        _param = createParam(_db2, request);

        //テンプレートの読込&初期化
        setIniTmpBook(_param._templatePath);

        //ヘッダデータ取得
        _headList = getHeadData();

        //出力データ取得
        if (TUUGAKU.equals(_param._target)) {
            _dataList = getXlsDataList2();
        } else {
            _dataList = getXlsDataList();
        }

        outPutXls(response, _param._header);
    }

    private List getXlsDataList2() throws SQLException {
        final StationNetmst stationNetmst = new StationNetmst();
        final String sql = getSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List dataList = new ArrayList();
        try {
            ps = _db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final List xlsData = new ArrayList();
                xlsData.add(rs.getString("CSVCD"));
                xlsData.add(rs.getString("REGYEAR"));
                xlsData.add(rs.getString("GRADE"));
                xlsData.add(rs.getString("HR_CLASS"));
                xlsData.add(rs.getString("ATTENDNO"));
                xlsData.add(rs.getString("REGSCH"));
                xlsData.add(rs.getString("NAME"));
                xlsData.add(rs.getString("GO_HOME_GROUP_NO"));
                xlsData.add(rs.getString("COMMUTE_HOURS"));
                xlsData.add(rs.getString("COMMUTE_MINUTES"));
                xlsData.add(rs.getString("HOWTOCOMMUTECD"));
                xlsData.add("1".equals(rs.getString("FLG_1")) ? rs.getString("JOSYA_1") : "");
                xlsData.add(getStationNet(stationNetmst._stationMap, rs.getString("FLG_1"), rs.getString("JOSYA_1")));
                xlsData.add("1".equals(rs.getString("FLG_1")) ? rs.getString("ROSEN_1") : "");
                xlsData.add(getStationNet(stationNetmst._lineCdMap, rs.getString("FLG_1"), rs.getString("ROSEN_1")));
                xlsData.add("1".equals(rs.getString("FLG_1")) ? rs.getString("GESYA_1") : "");
                xlsData.add(getStationNet(stationNetmst._stationMap, rs.getString("FLG_1"), rs.getString("GESYA_1")));
                xlsData.add(rs.getString("FLG_1"));
                xlsData.add("1".equals(rs.getString("FLG_2")) ? rs.getString("JOSYA_2") : "");
                xlsData.add(getStationNet(stationNetmst._stationMap, rs.getString("FLG_2"), rs.getString("JOSYA_2")));
                xlsData.add("1".equals(rs.getString("FLG_2")) ? rs.getString("ROSEN_2") : "");
                xlsData.add(getStationNet(stationNetmst._lineCdMap, rs.getString("FLG_2"), rs.getString("ROSEN_2")));
                xlsData.add("1".equals(rs.getString("FLG_2")) ? rs.getString("GESYA_2") : "");
                xlsData.add(getStationNet(stationNetmst._stationMap, rs.getString("FLG_2"), rs.getString("GESYA_2")));
                xlsData.add(rs.getString("FLG_2"));
                xlsData.add("1".equals(rs.getString("FLG_3")) ? rs.getString("JOSYA_3") : "");
                xlsData.add(getStationNet(stationNetmst._stationMap, rs.getString("FLG_3"), rs.getString("JOSYA_3")));
                xlsData.add("1".equals(rs.getString("FLG_3")) ? rs.getString("ROSEN_3") : "");
                xlsData.add(getStationNet(stationNetmst._lineCdMap, rs.getString("FLG_3"), rs.getString("ROSEN_3")));
                xlsData.add("1".equals(rs.getString("FLG_3")) ? rs.getString("GESYA_3") : "");
                xlsData.add(getStationNet(stationNetmst._stationMap, rs.getString("FLG_3"), rs.getString("GESYA_3")));
                xlsData.add(rs.getString("FLG_3"));
                xlsData.add("1".equals(rs.getString("FLG_4")) ? rs.getString("JOSYA_4") : "");
                xlsData.add(getStationNet(stationNetmst._stationMap, rs.getString("FLG_4"), rs.getString("JOSYA_4")));
                xlsData.add("1".equals(rs.getString("FLG_4")) ? rs.getString("ROSEN_4") : "");
                xlsData.add(getStationNet(stationNetmst._lineCdMap, rs.getString("FLG_4"), rs.getString("ROSEN_4")));
                xlsData.add("1".equals(rs.getString("FLG_4")) ? rs.getString("GESYA_4") : "");
                xlsData.add(getStationNet(stationNetmst._stationMap, rs.getString("FLG_4"), rs.getString("GESYA_4")));
                xlsData.add(rs.getString("FLG_4"));
                xlsData.add("1".equals(rs.getString("FLG_5")) ? rs.getString("JOSYA_5") : "");
                xlsData.add(getStationNet(stationNetmst._stationMap, rs.getString("FLG_5"), rs.getString("JOSYA_5")));
                xlsData.add("1".equals(rs.getString("FLG_5")) ? rs.getString("ROSEN_5") : "");
                xlsData.add(getStationNet(stationNetmst._lineCdMap, rs.getString("FLG_5"), rs.getString("ROSEN_5")));
                xlsData.add("1".equals(rs.getString("FLG_5")) ? rs.getString("GESYA_5") : "");
                xlsData.add(getStationNet(stationNetmst._stationMap, rs.getString("FLG_5"), rs.getString("GESYA_5")));
                xlsData.add(rs.getString("FLG_5"));
                xlsData.add("1".equals(rs.getString("FLG_6")) ? rs.getString("JOSYA_6") : "");
                xlsData.add(getStationNet(stationNetmst._stationMap, rs.getString("FLG_6"), rs.getString("JOSYA_6")));
                xlsData.add("1".equals(rs.getString("FLG_6")) ? rs.getString("ROSEN_6") : "");
                xlsData.add(getStationNet(stationNetmst._lineCdMap, rs.getString("FLG_6"), rs.getString("ROSEN_6")));
                xlsData.add("1".equals(rs.getString("FLG_6")) ? rs.getString("GESYA_6") : "");
                xlsData.add(getStationNet(stationNetmst._stationMap, rs.getString("FLG_6"), rs.getString("GESYA_6")));
                xlsData.add(rs.getString("FLG_6"));
                xlsData.add("1".equals(rs.getString("FLG_7")) ? rs.getString("JOSYA_7") : "");
                xlsData.add(getStationNet(stationNetmst._stationMap, rs.getString("FLG_7"), rs.getString("JOSYA_7")));
                xlsData.add("1".equals(rs.getString("FLG_7")) ? rs.getString("ROSEN_7") : "");
                xlsData.add(getStationNet(stationNetmst._lineCdMap, rs.getString("FLG_7"), rs.getString("ROSEN_7")));
                xlsData.add("1".equals(rs.getString("FLG_7")) ? rs.getString("GESYA_7") : "");
                xlsData.add(getStationNet(stationNetmst._stationMap, rs.getString("FLG_7"), rs.getString("GESYA_7")));
                xlsData.add(rs.getString("FLG_7"));
                xlsData.add("DUMMY");
                dataList.add(xlsData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            _db2.commit();
        }
        return dataList;
    }

    private String getStationNet(final Map netmstMap, final String flg, final String cd) {

        String retStr = cd;
        if ("1".equals(flg) && netmstMap.containsKey(cd)) {
            retStr = (String) netmstMap.get(cd);
        }
        return retStr;
    }

    protected List getHeadData() {
        final List retList = new ArrayList();
        if (KAZOKU.equals(_param._target)) {
            retList.add("※CSVCD");
            retList.add("※年度");
            retList.add("学年");
            retList.add("組");
            retList.add("出席番号");
            retList.add("※学籍番号");
            retList.add("氏名");
            retList.add("※連番(2桁)");
            retList.add("親族氏名");
            retList.add("親族氏名かな");
            retList.add("親族性別");
            retList.add("生年月日");
            retList.add("通勤先又は学校名");
            retList.add("同居区分");
            retList.add("親族続柄");
            retList.add("親族学籍番号");
            retList.add("備考");
        }
        if (KINKYU.equals(_param._target)) {
            retList.add("※CSVCD");
            retList.add("※年度");
            retList.add("学年");
            retList.add("組");
            retList.add("出席番号");
            retList.add("※学籍番号");
            retList.add("氏名");
            retList.add("緊急連絡先");
            retList.add("緊急連絡先氏名");
            retList.add("緊急連絡先続柄名");
            retList.add("緊急連絡先電話番号");
            retList.add("緊急連絡先２");
            retList.add("緊急連絡先氏名２");
            retList.add("緊急連絡先続柄名２");
            retList.add("緊急連絡先電話番号２");
        }
        if (TUUGAKU.equals(_param._target)) {
            retList.add("※CSVCD");
            retList.add("※年度");
            retList.add("学年");
            retList.add("組");
            retList.add("出席番号");
            retList.add("※学籍番号");
            retList.add("氏名");
            retList.add("災害時帰宅グループ番号");
            retList.add("通学所要時間");
            retList.add("通学所要分");
            retList.add("最寄駅までの手段");
            retList.add("最寄駅（自宅）");
            retList.add("最寄駅名（自宅）");
            retList.add("路線（自宅）");
            retList.add("路線名（自宅）");
            retList.add("下車駅（自宅）");
            retList.add("下車駅名（自宅）");
            retList.add("通学手段（自宅）");
            retList.add("乗車駅1");
            retList.add("乗車駅名1");
            retList.add("路線1");
            retList.add("路線名1");
            retList.add("下車駅1");
            retList.add("下車駅名1");
            retList.add("通学手段1");
            retList.add("乗車駅2");
            retList.add("乗車駅名2");
            retList.add("路線2");
            retList.add("路線名2");
            retList.add("下車駅2");
            retList.add("下車駅名2");
            retList.add("通学手段2");
            retList.add("乗車駅3");
            retList.add("乗車駅名3");
            retList.add("路線3");
            retList.add("路線名3");
            retList.add("下車駅3");
            retList.add("下車駅名3");
            retList.add("通学手段3");
            retList.add("乗車駅4");
            retList.add("乗車駅名4");
            retList.add("路線4");
            retList.add("路線名4");
            retList.add("下車駅4");
            retList.add("下車駅名4");
            retList.add("通学手段4");
            retList.add("乗車駅5");
            retList.add("乗車駅名5");
            retList.add("路線5");
            retList.add("路線名5");
            retList.add("下車駅5");
            retList.add("下車駅名5");
            retList.add("通学手段5");
            retList.add("最寄駅（学校）");
            retList.add("最寄駅名（学校）");
            retList.add("路線（学校）");
            retList.add("路線名（学校）");
            retList.add("下車駅（学校）");
            retList.add("下車駅名（学校）");
            retList.add("通学手段（学校）");
        }
        if (JITENSYA.equals(_param._target)) {
            retList.add("※CSVCD");
            retList.add("※年度");
            retList.add("※学籍番号");
            retList.add("学年");
            retList.add("組");
            retList.add("出席番号");
            retList.add("氏名");
            retList.add("※登録開始日付");
            retList.add("※登録終了日付");
            retList.add("※許可番号");
            retList.add("駐輪場");
            retList.add("詳細内容");
            retList.add("備考");
        }
        if (SONOTA.equals(_param._target)) {
            retList.add("※CSVCD");
            retList.add("※年度");
            retList.add("学年");
            retList.add("組");
            retList.add("出席番号");
            retList.add("※学籍番号");
            retList.add("氏名");
            retList.add("その他の続柄");
            retList.add("その他の氏名");
            retList.add("その他のかな");
            retList.add("その他の性別");
            retList.add("その他の郵便番号");
            retList.add("その他の住所1");
            retList.add("その他の住所2");
            retList.add("その他の電話番号");
            retList.add("その他の職種コード");
            retList.add("その他の兼ねている公職");
        }
        return retList;
    }

    protected String[] getCols() {
        final String[] cols1 = {"CSVCD",
                "REGYEAR",
                "GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "REGSCH",
                "NAME",
                "RELANO",
                "RELANAME",
                "RELAKANA",
                "RELASEX",
                "RELABIRTHDAY",
                "OCCUPATION",
                "REGIDENTIALCD",
                "RELATIONSHIP",
                "RELA_SCHREGNO",
                "REMARK",};

        final String[] cols2 = {"CSVCD",
                "REGYEAR",
                "GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "REGSCH",
                "NAME",
                "EMERGENCYCALL",
                "EMERGENCYNAME",
                "EMERGENCYRELA_NAME",
                "EMERGENCYTELNO",
                "EMERGENCYCALL2",
                "EMERGENCYNAME2",
                "EMERGENCYRELA_NAME2",
                "EMERGENCYTELNO2",};

        final String[] cols4 = {"CSVCD",
                "REGYEAR",
                "REGSCH",
                "GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "NAME",
                "DETAIL_SDATE",
                "DETAIL_EDATE",
                "BICYCLE_CD",
                "BICYCLE_NO",
                "CONTENT",
                "REMARK",};
        
        final String[] cols5 = {"CSVCD",
                "REGYEAR",
                "GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "REGSCH",
                "NAME",
                "SEND_RELATIONSHIP",
                "SEND_NAME",
                "SEND_KANA",
                "SEND_SEX",
                "SEND_ZIPCD",
                "SEND_ADDR1",
                "SEND_ADDR2",
                "SEND_TELNO",
                "SEND_JOBCD",                
                "PUBLIC_OFFICE",};        

        return KAZOKU.equals(_param._target) ? cols1 : KINKYU.equals(_param._target) ? cols2 : JITENSYA.equals(_param._target) ? cols4 : cols5;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH REGD_DAT AS ( ");
        stb.append("     SELECT YEAR AS REGYEAR,SCHREGNO AS REGSCH,GRADE,HR_CLASS,ATTENDNO ");
        stb.append("     FROM SCHREG_REGD_DAT ");
        stb.append("     WHERE YEAR || SEMESTER = '" + _param._yearSem + "' ");
        if(_param._gradeHrClass != null && !"".equals(_param._gradeHrClass)){
            stb.append("     AND GRADE || HR_CLASS = '" + _param._gradeHrClass + "' ");
        }
        if (_param._isGrdCheck) {
            stb.append("     AND SCHREGNO NOT IN ( ");
            stb.append("             SELECT  T1.SCHREGNO ");
            stb.append("             FROM    SCHREG_REGD_DAT T1, ");
            stb.append("                     V_SEMESTER_GRADE_MST T2 ");
            stb.append("             WHERE   T1.YEAR = T2.YEAR AND ");
            stb.append("                     T1.SEMESTER = T2.SEMESTER AND ");
            stb.append("                     T1.GRADE = T2.GRADE AND ");
            stb.append("                     T1.YEAR || T1.SEMESTER = '" + _param._yearSem + "' AND ");
            stb.append("                     EXISTS (SELECT 'X' FROM SCHREG_BASE_MST S1 ");
            stb.append("                             WHERE S1.SCHREGNO = T1.SCHREGNO AND ");
            stb.append("                                   (S1.GRD_DIV IN ('1','2','3') AND ");
            stb.append("                                   S1.GRD_DATE < T2.EDATE))) ");
        }
        stb.append(" ) ");
        if (KAZOKU.equals(_param._target)){
            stb.append(" SELECT ");
            stb.append("     '1' AS CSVCD,REGYEAR,GRADE,HR_CLASS,ATTENDNO,REGSCH,T2.NAME, ");
            stb.append("     T1.RELANO,T1.RELANAME,T1.RELAKANA,T1.RELASEX,T1.RELABIRTHDAY, ");
            stb.append("     T1.OCCUPATION,T1.REGIDENTIALCD,T1.RELATIONSHIP,T1.RELA_SCHREGNO,T1.REMARK ");
            stb.append(" FROM ");
            stb.append("     REGD_DAT LEFT JOIN SCHREG_RELA_DAT T1 ON REGSCH = T1.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST T2 ON REGSCH = T2.SCHREGNO ");
        } else if (KINKYU.equals(_param._target)){
            stb.append(" SELECT ");
            stb.append("     '2' AS CSVCD,REGYEAR,GRADE,HR_CLASS,ATTENDNO,REGSCH,T1.NAME, ");
            stb.append("     T1.EMERGENCYCALL,T1.EMERGENCYNAME,T1.EMERGENCYRELA_NAME,T1.EMERGENCYTELNO, ");
            stb.append("     T1.EMERGENCYCALL2,T1.EMERGENCYNAME2,T1.EMERGENCYRELA_NAME2,T1.EMERGENCYTELNO2 ");
            stb.append(" FROM ");
            stb.append("     REGD_DAT LEFT JOIN SCHREG_BASE_MST T1 ON REGSCH = T1.SCHREGNO ");
        } else if (TUUGAKU.equals(_param._target)){
            stb.append(" SELECT ");
            stb.append("     '3' AS CSVCD,REGYEAR,GRADE,HR_CLASS,ATTENDNO,REGSCH,T2.NAME, ");
            stb.append("     T1.GO_HOME_GROUP_NO,T1.COMMUTE_HOURS,T1.COMMUTE_MINUTES,T1.HOWTOCOMMUTECD, ");
            stb.append("     T1.JOSYA_1, T1.ROSEN_1, T1.GESYA_1, T1.FLG_1, T1.JOSYA_2, T1.ROSEN_2, T1.GESYA_2, T1.FLG_2, ");
            stb.append("     T1.JOSYA_3, T1.ROSEN_3, T1.GESYA_3, T1.FLG_3, T1.JOSYA_4, T1.ROSEN_4, T1.GESYA_4, T1.FLG_4, ");
            stb.append("     T1.JOSYA_5, T1.ROSEN_5, T1.GESYA_5, T1.FLG_5, T1.JOSYA_6, T1.ROSEN_6, T1.GESYA_6, T1.FLG_6, ");
            stb.append("     T1.JOSYA_7, T1.ROSEN_7, T1.GESYA_7, T1.FLG_7 ");
            stb.append(" FROM ");
            stb.append("     REGD_DAT LEFT JOIN SCHREG_ENVIR_DAT T1 ON REGSCH = T1.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST T2 ON REGSCH = T2.SCHREGNO ");
        } else if (JITENSYA.equals(_param._target)){
            stb.append(" SELECT ");
            stb.append("     '4' AS CSVCD,REGYEAR,REGSCH,GRADE,HR_CLASS,ATTENDNO,T2.NAME,T1.DETAIL_SDATE,T1.DETAIL_EDATE,T1.BICYCLE_CD,T1.BICYCLE_NO,T1.CONTENT,T1.REMARK ");
            stb.append(" FROM ");
            stb.append("     REGD_DAT LEFT JOIN SCHREG_DETAILHIST_DAT T1 ON REGYEAR = T1.YEAR AND REGSCH = T1.SCHREGNO ");
            stb.append("     AND T1.YEAR = '" + _param._yearSem.substring(0, 4) + "' ");
            stb.append("     AND T1.DETAIL_DIV = '3' ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST T2 ON REGSCH = T2.SCHREGNO ");
        } else if (SONOTA.equals(_param._target)){
            stb.append(" SELECT ");
            stb.append("     '5' AS CSVCD,REGYEAR,GRADE,HR_CLASS,ATTENDNO,REGSCH,T2.NAME, ");
            stb.append("     T1.SEND_RELATIONSHIP,T1.SEND_NAME,T1.SEND_KANA,T1.SEND_SEX,T1.SEND_ZIPCD,T1.SEND_ADDR1,T1.SEND_ADDR2,T1.SEND_TELNO,T1.SEND_JOBCD,T1.PUBLIC_OFFICE ");
            stb.append(" FROM ");
            stb.append("     REGD_DAT LEFT JOIN SCHREG_SEND_ADDRESS_DAT T1 ON REGSCH = T1.SCHREGNO AND T1.DIV = '1' ");            
            stb.append("     LEFT JOIN SCHREG_BASE_MST T2 ON REGSCH = T2.SCHREGNO ");  
        }
        stb.append(" ORDER BY ");
        stb.append("     REGYEAR,GRADE,HR_CLASS,ATTENDNO ");
        return stb.toString();
    }

    private class StationNetmst {
        final Map _lineCdMap = new HashMap();
        final Map _stationMap = new HashMap();
        public StationNetmst() throws SQLException {
            final String stationNetSql = getStationNetSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(stationNetSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _lineCdMap.put(rs.getString("LINE_CD"), rs.getString("LINE_NAME"));
                    _stationMap.put(rs.getString("STATION_CD"), rs.getString("STATION_NAME"));
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }
        }

        private String getStationNetSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     LINE_CD, ");
            stb.append("     STATION_CD, ");
            stb.append("     LINE_NAME, ");
            stb.append("     STATION_NAME ");
            stb.append(" FROM ");
            stb.append("     STATION_NETMST ");
            return stb.toString();
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
        private final String _yearSem;
        private final String _gradeHrClass;
        private final String _target;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final boolean _isGrdCheck;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _target = request.getParameter("TARGET");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADERCHECK") == null ? false : true;
            _isGrdCheck = request.getParameter("GRD_CHECK") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
