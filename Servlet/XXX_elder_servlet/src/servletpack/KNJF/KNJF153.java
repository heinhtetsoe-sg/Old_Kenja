/*
 * $Id: cc0339d743f01c9558230462948339f0af3fd83b $
 *
 * 作成日: 2019/02/18
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJF153 {

    private static final Log log = LogFactory.getLog(KNJF153.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        printSvf1(db2,svf);

        printSvf2(db2,svf);

    }

    private void printSvf1(final DB2UDB db2, final Vrw32alp svf) {
        final int maxLine = 31;
        String[] nendo = KNJ_EditDate.tate_format4(db2,_param._year + "-04-01");
        final String title = _param._schoolName + "　" + nendo[0] + nendo[1] + "年度　保健室利用状況表";

        final List list = BaseDat.load(db2, _param);
        final List pageList = getPageList(list, maxLine);
        for (int page = 0; page < pageList.size(); page++) {
            final List baseDatList = (List) pageList.get(page);

            //内科・外科・健康相談・その他の種別毎の月別来室記録表
            svf.VrSetForm("KNJF153_1.frm", 1);

            final String titleField = getMS932Bytecount(title) > 50 ? "2" : "";
            svf.VrsOut("TITLE" + titleField, title); // タイトル
            svf.VrsOut("PAGE1", String.valueOf(page + 1)); // ページ
            svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // 総ページ数

            for (int j = 0; j < baseDatList.size(); j++) {
                final BaseDat base = (BaseDat) baseDatList.get(j);
                final int line = j + 1;
                svf.VrsOutn("NUM1", line, base._m04cnt);  // 4月
                svf.VrsOutn("NUM2", line, base._m05cnt);  // 5月
                svf.VrsOutn("NUM3", line, base._m06cnt);  // 6月
                svf.VrsOutn("NUM4", line, base._m07cnt);  // 7月
                svf.VrsOutn("NUM5", line, base._m08cnt);  // 8月
                svf.VrsOutn("NUM6", line, base._m09cnt);  // 9月
                svf.VrsOutn("NUM7", line, base._m10cnt);  // 10月
                svf.VrsOutn("NUM8", line, base._m11cnt);  // 11月
                svf.VrsOutn("NUM9", line, base._m12cnt);  // 12月
                svf.VrsOutn("NUM10", line, base._m01cnt); // 1月
                svf.VrsOutn("NUM11", line, base._m02cnt); // 2月
                svf.VrsOutn("NUM12", line, base._m03cnt); // 3月
                svf.VrsOutn("TOTAL", line, base._total);  // 合計
                svf.VrsOutn("PER", line, base._per);      // 率
            }
            _hasData = true;
            svf.VrEndPage();
        }
    }

    private void printSvf2(final DB2UDB db2, final Vrw32alp svf) {
        final int maxLine = 20;

        final List list = BaseDat2.load(db2, _param);
        final List pageList = getPageList(list, maxLine);

        for (int page = 0; page < pageList.size(); page++) {
            final List baseDatList = (List) pageList.get(page);

            //健康相談の詳細項目毎の月別来室記録表(延べ&実人数)、保険膣登校状況表(延べ&実人数)
            svf.VrSetForm("KNJF153_2.frm", 1);

            int row = 1;
            for (int j = 0; j < baseDatList.size(); j++) {
                final BaseDat2 base = (BaseDat2) baseDatList.get(j);
                final int line = j + 1;
                final String field = line > 18 ? "3" : line > 9 ? "2" : "1";
                svf.VrsOutn("NUM" + field + "_1", row, base._m04cnt);  // 4月
                svf.VrsOutn("NUM" + field + "_2", row, base._m05cnt);  // 5月
                svf.VrsOutn("NUM" + field + "_3", row, base._m06cnt);  // 6月
                svf.VrsOutn("NUM" + field + "_4", row, base._m07cnt);  // 7月
                svf.VrsOutn("NUM" + field + "_5", row, base._m08cnt);  // 8月
                svf.VrsOutn("NUM" + field + "_6", row, base._m09cnt);  // 9月
                svf.VrsOutn("NUM" + field + "_7", row, base._m10cnt);  // 10月
                svf.VrsOutn("NUM" + field + "_8", row, base._m11cnt);  // 11月
                svf.VrsOutn("NUM" + field + "_9", row, base._m12cnt);  // 12月
                svf.VrsOutn("NUM" + field + "_10", row, base._m01cnt); // 1月
                svf.VrsOutn("NUM" + field + "_11", row, base._m02cnt); // 2月
                svf.VrsOutn("NUM" + field + "_12", row, base._m03cnt); // 3月
                svf.VrsOutn("TOTAL" + field , row, base._total);  // 合計

                if(line == 18 || line == 9) {
                    row = 1;
                }else {
                	row++;
                }
            }
            _hasData = true;
            svf.VrEndPage();
        }
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private static class BaseDat {
        final String _m04cnt;
        final String _m05cnt;
        final String _m06cnt;
        final String _m07cnt;
        final String _m08cnt;
        final String _m09cnt;
        final String _m10cnt;
        final String _m11cnt;
        final String _m12cnt;
        final String _m01cnt;
        final String _m02cnt;
        final String _m03cnt;
        final String _total;
        final String _per;

        BaseDat(
                final String m04cnt,
                final String m05cnt,
                final String m06cnt,
                final String m07cnt,
                final String m08cnt,
                final String m09cnt,
                final String m10cnt,
                final String m11cnt,
                final String m12cnt,
                final String m01cnt,
                final String m02cnt,
                final String m03cnt,
                final String total,
                final String per
        ) {
            _m04cnt = m04cnt;
            _m05cnt = m05cnt;
            _m06cnt = m06cnt;
            _m07cnt = m07cnt;
            _m08cnt = m08cnt;
            _m09cnt = m09cnt;
            _m10cnt = m10cnt;
            _m11cnt = m11cnt;
            _m12cnt = m12cnt;
            _m01cnt = m01cnt;
            _m02cnt = m02cnt;
            _m03cnt = m03cnt;
            _total = total;
            _per = per;

        }

        public static List load(final DB2UDB db2, final Param param) {
            String[] reasonCd = {"TYPE1_01","TYPE1_02","TYPE1_03","TYPE1_04","TYPE1_05","TYPE1_06","TYPE1_07","TYPE1_08","TYPE1_99","TYPE1_SUB","TYPE2_01","TYPE2_02","TYPE2_03","TYPE2_04","TYPE2_05","TYPE2_06","TYPE2_07","TYPE2_08","TYPE2_99","TYPE2_SUB","TYPE3_01","TYPE3_02","TYPE3_03","TYPE3_SUB","R100","04","R101","R102"};
            String[] reasonCd3 = {"R104","R105"};
            int idx = 0;
            boolean dataFlg = false;
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {

                //胃・腹痛～休養者数
                final String sql = sql(param,1);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    while(!rs.getString("REASON_CD").equals(reasonCd[idx])) {
                        //取得できなかった行の挿入
                        final BaseDat basedat = new BaseDat("", "", "", "", "", "", "", "", "", "", "", "", "0", "0%");
                        list.add(basedat);
                        idx++;
                    }
                    idx++;

                    final String m04cnt = rs.getString("M04CNT");
                    final String m05cnt = rs.getString("M05CNT");
                    final String m06cnt = rs.getString("M06CNT");
                    final String m07cnt = rs.getString("M07CNT");
                    final String m08cnt = rs.getString("M08CNT");
                    final String m09cnt = rs.getString("M09CNT");
                    final String m10cnt = rs.getString("M10CNT");
                    final String m11cnt = rs.getString("M11CNT");
                    final String m12cnt = rs.getString("M12CNT");
                    final String m01cnt = rs.getString("M01CNT");
                    final String m02cnt = rs.getString("M02CNT");
                    final String m03cnt = rs.getString("M03CNT");
                    final String total = "".equals(StringUtils.defaultString(rs.getString("TOTAL"))) ? "0" : rs.getString("TOTAL");
                    final String per = "".equals(StringUtils.defaultString(rs.getString("PER"))) ? "0%" : rs.getString("PER") + "%";

                    final BaseDat basedat = new BaseDat(m04cnt, m05cnt, m06cnt, m07cnt, m08cnt, m09cnt, m10cnt, m11cnt, m12cnt, m01cnt, m02cnt, m03cnt, total, per);
                    list.add(basedat);

                }
                while(idx != reasonCd.length) {
                    //取得できなかった行の挿入
                    final BaseDat basedat = new BaseDat("", "", "", "", "", "", "", "", "", "", "", "", "0", "0%");
                    list.add(basedat);
                    idx++;
                }

                //1日平均利用者数
                dataFlg = false;
                final String sql2 = sql(param,2);
                log.debug(" sql = " + sql2);
                ps = db2.prepareStatement(sql2);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String m04cnt = rs.getString("M04CNT");
                    final String m05cnt = rs.getString("M05CNT");
                    final String m06cnt = rs.getString("M06CNT");
                    final String m07cnt = rs.getString("M07CNT");
                    final String m08cnt = rs.getString("M08CNT");
                    final String m09cnt = rs.getString("M09CNT");
                    final String m10cnt = rs.getString("M10CNT");
                    final String m11cnt = rs.getString("M11CNT");
                    final String m12cnt = rs.getString("M12CNT");
                    final String m01cnt = rs.getString("M01CNT");
                    final String m02cnt = rs.getString("M02CNT");
                    final String m03cnt = rs.getString("M03CNT");
                    final String total = "".equals(StringUtils.defaultString(rs.getString("TOTAL"))) ? "0" : rs.getString("TOTAL");
                    final String per = "".equals(StringUtils.defaultString(rs.getString("PER"))) ? "0%" : rs.getString("PER") + "%";

                    final BaseDat basedat = new BaseDat(m04cnt, m05cnt, m06cnt, m07cnt, m08cnt, m09cnt, m10cnt, m11cnt, m12cnt, m01cnt, m02cnt, m03cnt, total, per);
                    list.add(basedat);
                    dataFlg = true;

                }
                if(!dataFlg) {
                    //取得できなかった行の挿入
                    final BaseDat basedat = new BaseDat("", "", "", "", "", "", "", "", "", "", "", "", "0", "0%");
                    list.add(basedat);
                }

                //在籍者数、授業日数
                idx = 0;
                final String sql3 = sql(param,3);
                log.debug(" sql = " + sql3);
                ps = db2.prepareStatement(sql3);
                rs = ps.executeQuery();
                while (rs.next()) {

                    while(!rs.getString("REASON_CD").equals(reasonCd3[idx])) {
                        //取得できなかった行の挿入
                        final BaseDat basedat = new BaseDat("", "", "", "", "", "", "", "", "", "", "", "", "0", "");
                        list.add(basedat);
                        idx++;
                    }
                    idx++;

                    final String m04cnt = rs.getString("M04CNT");
                    final String m05cnt = rs.getString("M05CNT");
                    final String m06cnt = rs.getString("M06CNT");
                    final String m07cnt = rs.getString("M07CNT");
                    final String m08cnt = rs.getString("M08CNT");
                    final String m09cnt = rs.getString("M09CNT");
                    final String m10cnt = rs.getString("M10CNT");
                    final String m11cnt = rs.getString("M11CNT");
                    final String m12cnt = rs.getString("M12CNT");
                    final String m01cnt = rs.getString("M01CNT");
                    final String m02cnt = rs.getString("M02CNT");
                    final String m03cnt = rs.getString("M03CNT");
                    final String total = "".equals(StringUtils.defaultString(rs.getString("TOTAL"))) ? "0" : rs.getString("TOTAL");
                    final String per = "";

                    final BaseDat basedat = new BaseDat(m04cnt, m05cnt, m06cnt, m07cnt, m08cnt, m09cnt, m10cnt, m11cnt, m12cnt, m01cnt, m02cnt, m03cnt, total, per);
                    list.add(basedat);
                }

                while(idx != reasonCd3.length) {
                    //取得できなかった行の挿入
                    final BaseDat basedat = new BaseDat("", "", "", "", "", "", "", "", "", "", "", "", "0", "");
                    list.add(basedat);
                    idx++;
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param,int selCase) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH MONTH AS( ");
            stb.append(" SELECT  ");
            stb.append("     MONTH(VISIT_DATE)      AS MONTH, ");
            stb.append("     TYPE, ");
            stb.append("     VISIT_REASON1, ");
            stb.append("     COUNT(*)               AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-4-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH(VISIT_DATE) , ");
            stb.append("     TYPE              , ");
            stb.append("     VISIT_REASON1      ");
            stb.append(" ), R_TOTAL AS( ");
            stb.append(" SELECT      ");
            stb.append("     TYPE, ");
            stb.append("     VISIT_REASON1, ");
            stb.append("     COUNT(*)               AS CNT ");
            stb.append(" FROM ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-4-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append(" GROUP BY  ");
            stb.append("     TYPE          , ");
            stb.append("     VISIT_REASON1   ");

            stb.append(setSqlType1(param)); //内科
            stb.append(setSqlType2(param)); //外科
            stb.append(setSqlType3(param)); //その他
            stb.append(setSqlType5(param)); //相談活動
            stb.append(setSqlType3_2(param)); //保健室登校(延べ人数)
            stb.append(setSqlTotal(param)); //総合計
            stb.append(setSqlRrt(param)); //休養者数
            stb.append(setSqlZaiseki(param)); //在籍者数
            stb.append(setSqlLesson(param)); //授業日数
            stb.append(setSqlOnAv(param)); //1日平均利用者数

            stb.append(" ), MAIN AS( ");
            stb.append(" SELECT * FROM TYPE1 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM TYPE2 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM TYPE3 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM TYPE5 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM TYPE3_2 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM TOTAL ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM RRT ");
            if(selCase == 2) {
                stb.append(" UNION ");
                stb.append(" SELECT * FROM ONAV ");
            }
            stb.append("), MAIN_TABLE AS(");
            stb.append(" SELECT ");
            stb.append("    T1.* , ");
            stb.append("    DECIMAL(DOUBLE(COALESCE(T1.TOTAL,0))/DOUBLE(T2.TOTAL) * 100,3,0) AS PER ");
            stb.append(" FROM ");
            stb.append("     (SELECT T1.*, 'X' AS JOINCD FROM MAIN T1) T1 ");
            stb.append("     FULL OUTER JOIN (SELECT T1.*, 'X' AS JOINCD FROM TOTAL T1) T2 ON T2.JOINCD = T1.JOINCD ");
            stb.append(" ORDER BY T1.ROW, T1.REASON_CD ");
            stb.append(") ");

            switch(selCase) {
            case 1:
                //胃・腹痛～休養者数 の取得
                stb.append(" SELECT * FROM MAIN_TABLE ");
                break;
            case 2:
                //1日平均利用者数 の取得
                stb.append(" SELECT * FROM MAIN_TABLE ");
                stb.append(" WHERE REASON_CD = 'R103'");
                break;
            case 3:
                //在籍者数、授業日数
                stb.append(" SELECT * FROM ZAISEKI ");
                stb.append(" UNION ");
                stb.append(" SELECT * FROM LESSON ");
                stb.append(" ORDER BY ROW, REASON_CD ");
                break;
            }

            return stb.toString();
        }

        private static String setSqlType1(final Param param) {
            final StringBuffer stb = new StringBuffer();

            //内科
            stb.append(" ), TYPE1_MAIN AS( ");
            stb.append(" SELECT  ");
            stb.append("     'TYPE1_' || N1.NAMECD2 AS REASON_CD, ");
            stb.append("     N1.NAME1   AS REASON_NAME, ");
            stb.append("     M04.CNT AS M04CNT, ");
            stb.append("     M05.CNT AS M05CNT, ");
            stb.append("     M06.CNT AS M06CNT, ");
            stb.append("     M07.CNT AS M07CNT, ");
            stb.append("     M08.CNT AS M08CNT, ");
            stb.append("     M09.CNT AS M09CNT, ");
            stb.append("     M10.CNT AS M10CNT, ");
            stb.append("     M11.CNT AS M11CNT, ");
            stb.append("     M12.CNT AS M12CNT, ");
            stb.append("     M01.CNT AS M01CNT, ");
            stb.append("     M02.CNT AS M02CNT, ");
            stb.append("     M03.CNT AS M03CNT, ");
            stb.append("     R_TOTAL.CNT AS TOTAL, ");
            stb.append("     1 AS ROW ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST N1  ");
            stb.append("     LEFT JOIN MONTH M04 ON M04.VISIT_REASON1 = N1.NAMECD2 AND M04.TYPE = '1' AND M04.MONTH = 04 ");
            stb.append("     LEFT JOIN MONTH M05 ON M05.VISIT_REASON1 = N1.NAMECD2 AND M05.TYPE = '1' AND M05.MONTH = 05 ");
            stb.append("     LEFT JOIN MONTH M06 ON M06.VISIT_REASON1 = N1.NAMECD2 AND M06.TYPE = '1' AND M06.MONTH = 06 ");
            stb.append("     LEFT JOIN MONTH M07 ON M07.VISIT_REASON1 = N1.NAMECD2 AND M07.TYPE = '1' AND M07.MONTH = 07 ");
            stb.append("     LEFT JOIN MONTH M08 ON M08.VISIT_REASON1 = N1.NAMECD2 AND M08.TYPE = '1' AND M08.MONTH = 08 ");
            stb.append("     LEFT JOIN MONTH M09 ON M09.VISIT_REASON1 = N1.NAMECD2 AND M09.TYPE = '1' AND M09.MONTH = 09 ");
            stb.append("     LEFT JOIN MONTH M10 ON M10.VISIT_REASON1 = N1.NAMECD2 AND M10.TYPE = '1' AND M10.MONTH = 10 ");
            stb.append("     LEFT JOIN MONTH M11 ON M11.VISIT_REASON1 = N1.NAMECD2 AND M11.TYPE = '1' AND M11.MONTH = 11 ");
            stb.append("     LEFT JOIN MONTH M12 ON M12.VISIT_REASON1 = N1.NAMECD2 AND M12.TYPE = '1' AND M12.MONTH = 12 ");
            stb.append("     LEFT JOIN MONTH M01 ON M01.VISIT_REASON1 = N1.NAMECD2 AND M01.TYPE = '1' AND M01.MONTH = 01 ");
            stb.append("     LEFT JOIN MONTH M02 ON M02.VISIT_REASON1 = N1.NAMECD2 AND M02.TYPE = '1' AND M02.MONTH = 02 ");
            stb.append("     LEFT JOIN MONTH M03 ON M03.VISIT_REASON1 = N1.NAMECD2 AND M03.TYPE = '1' AND M03.MONTH = 03 ");
            stb.append("     LEFT JOIN R_TOTAL ON R_TOTAL.VISIT_REASON1 = N1.NAMECD2 AND R_TOTAL.TYPE = '1' ");
            stb.append(" WHERE ");
            stb.append("     N1.YEAR = '" + param._year + "' ");
            stb.append("     AND N1.NAMECD1 = 'F200' ");
            stb.append("     AND N1.NAMECD2 IN ('01','02','03','04','05','06','07','08','99') ");
            stb.append(" ORDER BY ");
            stb.append("     N1.NAMECD2 ");
            stb.append(" ), M_TOTAL1 AS( ");
            stb.append(" SELECT  ");
            stb.append("     MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("     TYPE, ");
            stb.append("     COUNT(*)          AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-4-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("     AND VISIT_REASON1 IN ('01','02','03','04','05','06','07','08','99') ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH(VISIT_DATE) , ");
            stb.append("     TYPE ");
            stb.append(" ), M_ALL_TOTAL1 AS( ");
            stb.append(" SELECT  ");
            stb.append("     TYPE, ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-4-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("     AND VISIT_REASON1 IN ('01','02','03','04','05','06','07','08','99') ");
            stb.append(" GROUP BY ");
            stb.append("     TYPE ");
            stb.append(" ), TYPE1_SUB AS ( ");
            stb.append(" SELECT ");
            stb.append("     'TYPE1_SUB' AS REASON_CD, ");
            stb.append("     '内科小計' AS REASON_NAME, ");
            stb.append("     MT04.CNT AS M04CNT, ");
            stb.append("     MT05.CNT AS M05CNT, ");
            stb.append("     MT06.CNT AS M06CNT, ");
            stb.append("     MT07.CNT AS M07CNT, ");
            stb.append("     MT08.CNT AS M08CNT, ");
            stb.append("     MT09.CNT AS M09CNT, ");
            stb.append("     MT10.CNT AS M10CNT, ");
            stb.append("     MT11.CNT AS M11CNT, ");
            stb.append("     MT12.CNT AS M12CNT, ");
            stb.append("     MT01.CNT AS M01CNT, ");
            stb.append("     MT02.CNT AS M02CNT, ");
            stb.append("     MT03.CNT AS M03CNT, ");
            stb.append("     MAT.CNT AS TOTAL, ");
            stb.append("     2 AS ROW ");
            stb.append(" FROM ");
            stb.append("     (SELECT TYPE FROM M_TOTAL1 GROUP BY TYPE) T1 ");
            stb.append("     LEFT JOIN M_TOTAL1 MT04 ON T1.TYPE = MT04.TYPE AND MT04.MONTH = 04 ");
            stb.append("     LEFT JOIN M_TOTAL1 MT05 ON T1.TYPE = MT05.TYPE AND MT05.MONTH = 05 ");
            stb.append("     LEFT JOIN M_TOTAL1 MT06 ON T1.TYPE = MT06.TYPE AND MT06.MONTH = 06 ");
            stb.append("     LEFT JOIN M_TOTAL1 MT07 ON T1.TYPE = MT07.TYPE AND MT07.MONTH = 07 ");
            stb.append("     LEFT JOIN M_TOTAL1 MT08 ON T1.TYPE = MT08.TYPE AND MT08.MONTH = 08 ");
            stb.append("     LEFT JOIN M_TOTAL1 MT09 ON T1.TYPE = MT09.TYPE AND MT09.MONTH = 09 ");
            stb.append("     LEFT JOIN M_TOTAL1 MT10 ON T1.TYPE = MT10.TYPE AND MT10.MONTH = 10 ");
            stb.append("     LEFT JOIN M_TOTAL1 MT11 ON T1.TYPE = MT11.TYPE AND MT11.MONTH = 11 ");
            stb.append("     LEFT JOIN M_TOTAL1 MT12 ON T1.TYPE = MT12.TYPE AND MT12.MONTH = 12 ");
            stb.append("     LEFT JOIN M_TOTAL1 MT01 ON T1.TYPE = MT01.TYPE AND MT01.MONTH = 01 ");
            stb.append("     LEFT JOIN M_TOTAL1 MT02 ON T1.TYPE = MT02.TYPE AND MT02.MONTH = 02 ");
            stb.append("     LEFT JOIN M_TOTAL1 MT03 ON T1.TYPE = MT03.TYPE AND MT03.MONTH = 03 ");
            stb.append("     LEFT JOIN M_ALL_TOTAL1 MAT ON T1.TYPE = MAT.TYPE ");
            stb.append(" WHERE ");
            stb.append("    T1.TYPE = '1' ");
            stb.append(" ), TYPE1 AS( ");
            stb.append(" SELECT * FROM TYPE1_MAIN ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM TYPE1_SUB ");
            return stb.toString();
        }

        private static String setSqlType2(final Param param) {
            final StringBuffer stb = new StringBuffer();

            //外科
            stb.append(" ), TYPE2_MAIN AS( ");
            stb.append(" SELECT  ");
            stb.append("     'TYPE2_' || N1.NAMECD2 AS REASON_CD, ");
            stb.append("     N1.NAME1   AS REASON_NAME, ");
            stb.append("     M04.CNT AS M04CNT, ");
            stb.append("     M05.CNT AS M05CNT, ");
            stb.append("     M06.CNT AS M06CNT, ");
            stb.append("     M07.CNT AS M07CNT, ");
            stb.append("     M08.CNT AS M08CNT, ");
            stb.append("     M09.CNT AS M09CNT, ");
            stb.append("     M10.CNT AS M10CNT, ");
            stb.append("     M11.CNT AS M11CNT, ");
            stb.append("     M12.CNT AS M12CNT, ");
            stb.append("     M01.CNT AS M01CNT, ");
            stb.append("     M02.CNT AS M02CNT, ");
            stb.append("     M03.CNT AS M03CNT, ");
            stb.append("     R_TOTAL.CNT AS TOTAL, ");
            stb.append("     3 AS ROW ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST N1  ");
            stb.append("     LEFT JOIN MONTH M04 ON M04.VISIT_REASON1 = N1.NAMECD2 AND M04.TYPE = '2' AND M04.MONTH = 04 ");
            stb.append("     LEFT JOIN MONTH M05 ON M05.VISIT_REASON1 = N1.NAMECD2 AND M05.TYPE = '2' AND M05.MONTH = 05 ");
            stb.append("     LEFT JOIN MONTH M06 ON M06.VISIT_REASON1 = N1.NAMECD2 AND M06.TYPE = '2' AND M06.MONTH = 06 ");
            stb.append("     LEFT JOIN MONTH M07 ON M07.VISIT_REASON1 = N1.NAMECD2 AND M07.TYPE = '2' AND M07.MONTH = 07 ");
            stb.append("     LEFT JOIN MONTH M08 ON M08.VISIT_REASON1 = N1.NAMECD2 AND M08.TYPE = '2' AND M08.MONTH = 08 ");
            stb.append("     LEFT JOIN MONTH M09 ON M09.VISIT_REASON1 = N1.NAMECD2 AND M09.TYPE = '2' AND M09.MONTH = 09 ");
            stb.append("     LEFT JOIN MONTH M10 ON M10.VISIT_REASON1 = N1.NAMECD2 AND M10.TYPE = '2' AND M10.MONTH = 10 ");
            stb.append("     LEFT JOIN MONTH M11 ON M11.VISIT_REASON1 = N1.NAMECD2 AND M11.TYPE = '2' AND M11.MONTH = 11 ");
            stb.append("     LEFT JOIN MONTH M12 ON M12.VISIT_REASON1 = N1.NAMECD2 AND M12.TYPE = '2' AND M12.MONTH = 12 ");
            stb.append("     LEFT JOIN MONTH M01 ON M01.VISIT_REASON1 = N1.NAMECD2 AND M01.TYPE = '2' AND M01.MONTH = 01 ");
            stb.append("     LEFT JOIN MONTH M02 ON M02.VISIT_REASON1 = N1.NAMECD2 AND M02.TYPE = '2' AND M02.MONTH = 02 ");
            stb.append("     LEFT JOIN MONTH M03 ON M03.VISIT_REASON1 = N1.NAMECD2 AND M03.TYPE = '2' AND M03.MONTH = 03 ");
            stb.append("     LEFT JOIN R_TOTAL ON R_TOTAL.VISIT_REASON1 = N1.NAMECD2 AND R_TOTAL.TYPE = '2' ");
            stb.append(" WHERE ");
            stb.append("     N1.YEAR = '" + param._year + "' ");
            stb.append("     AND N1.NAMECD1 = 'F201' ");
            stb.append("     AND N1.NAMECD2 IN ('01','02','03','04','05','06','07','08','99') ");
            stb.append(" ORDER BY ");
            stb.append("     N1.NAMECD2 ");
            stb.append(" ), M_TOTAL2 AS( ");
            stb.append(" SELECT  ");
            stb.append("     MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("     TYPE, ");
            stb.append("     COUNT(*)          AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-4-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("     AND VISIT_REASON1 IN ('01','02','03','04','05','06','07','08','99') ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH(VISIT_DATE) , ");
            stb.append("     TYPE ");
            stb.append(" ), M_ALL_TOTAL2 AS( ");
            stb.append(" SELECT  ");
            stb.append("     TYPE, ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-4-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("     AND VISIT_REASON1 IN ('01','02','03','04','05','06','07','08','99') ");
            stb.append(" GROUP BY ");
            stb.append("     TYPE ");
            stb.append(" ), TYPE2_SUB AS ( ");
            stb.append(" SELECT ");
            stb.append("     'TYPE2_SUB' AS REASON_CD, ");
            stb.append("     '外科小計' AS REASON_NAME, ");
            stb.append("     MT04.CNT AS M04CNT, ");
            stb.append("     MT05.CNT AS M05CNT, ");
            stb.append("     MT06.CNT AS M06CNT, ");
            stb.append("     MT07.CNT AS M07CNT, ");
            stb.append("     MT08.CNT AS M08CNT, ");
            stb.append("     MT09.CNT AS M09CNT, ");
            stb.append("     MT10.CNT AS M10CNT, ");
            stb.append("     MT11.CNT AS M11CNT, ");
            stb.append("     MT12.CNT AS M12CNT, ");
            stb.append("     MT01.CNT AS M01CNT, ");
            stb.append("     MT02.CNT AS M02CNT, ");
            stb.append("     MT03.CNT AS M03CNT, ");
            stb.append("     MAT.CNT AS TOTAL, ");
            stb.append("     4 AS ROW ");
            stb.append(" FROM ");
            stb.append("     (SELECT TYPE FROM M_TOTAL2 GROUP BY TYPE) T1 ");
            stb.append("     LEFT JOIN M_TOTAL2 MT04 ON MT04.TYPE = T1.TYPE AND MT04.MONTH = 04 ");
            stb.append("     LEFT JOIN M_TOTAL2 MT05 ON MT05.TYPE = T1.TYPE AND MT05.MONTH = 05 ");
            stb.append("     LEFT JOIN M_TOTAL2 MT06 ON MT06.TYPE = T1.TYPE AND MT06.MONTH = 06 ");
            stb.append("     LEFT JOIN M_TOTAL2 MT07 ON MT07.TYPE = T1.TYPE AND MT07.MONTH = 07 ");
            stb.append("     LEFT JOIN M_TOTAL2 MT08 ON MT08.TYPE = T1.TYPE AND MT08.MONTH = 08 ");
            stb.append("     LEFT JOIN M_TOTAL2 MT09 ON MT09.TYPE = T1.TYPE AND MT09.MONTH = 09 ");
            stb.append("     LEFT JOIN M_TOTAL2 MT10 ON MT10.TYPE = T1.TYPE AND MT10.MONTH = 10 ");
            stb.append("     LEFT JOIN M_TOTAL2 MT11 ON MT11.TYPE = T1.TYPE AND MT11.MONTH = 11 ");
            stb.append("     LEFT JOIN M_TOTAL2 MT12 ON MT12.TYPE = T1.TYPE AND MT12.MONTH = 12 ");
            stb.append("     LEFT JOIN M_TOTAL2 MT01 ON MT01.TYPE = T1.TYPE AND MT01.MONTH = 01 ");
            stb.append("     LEFT JOIN M_TOTAL2 MT02 ON MT02.TYPE = T1.TYPE AND MT02.MONTH = 02 ");
            stb.append("     LEFT JOIN M_TOTAL2 MT03 ON MT03.TYPE = T1.TYPE AND MT03.MONTH = 03 ");
            stb.append("     LEFT JOIN M_ALL_TOTAL2 MAT ON MAT.TYPE = T1.TYPE ");
            stb.append(" WHERE ");
            stb.append("    T1.TYPE = '2' ");
            stb.append(" ), TYPE2 AS ( ");
            stb.append(" SELECT * FROM TYPE2_MAIN ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM TYPE2_SUB ");
            return stb.toString();
        }

        private static String setSqlType3(final Param param) {
            final StringBuffer stb = new StringBuffer();

            //その他
            stb.append(" ), TYPE3_MAIN AS( ");
            stb.append(" SELECT  ");
            stb.append("     'TYPE3_' || N1.NAMECD2 AS REASON_CD, ");
            stb.append("     N1.NAME1   AS REASON_NAME, ");
            stb.append("     M04.CNT AS M04CNT, ");
            stb.append("     M05.CNT AS M05CNT, ");
            stb.append("     M06.CNT AS M06CNT, ");
            stb.append("     M07.CNT AS M07CNT, ");
            stb.append("     M08.CNT AS M08CNT, ");
            stb.append("     M09.CNT AS M09CNT, ");
            stb.append("     M10.CNT AS M10CNT, ");
            stb.append("     M11.CNT AS M11CNT, ");
            stb.append("     M12.CNT AS M12CNT, ");
            stb.append("     M01.CNT AS M01CNT, ");
            stb.append("     M02.CNT AS M02CNT, ");
            stb.append("     M03.CNT AS M03CNT, ");
            stb.append("     R_TOTAL.CNT AS TOTAL, ");
            stb.append("     5 AS ROW ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST N1  ");
            stb.append("     LEFT JOIN MONTH M04 ON M04.VISIT_REASON1 = N1.NAMECD2 AND M04.TYPE = '3' AND M04.MONTH = 04 ");
            stb.append("     LEFT JOIN MONTH M05 ON M05.VISIT_REASON1 = N1.NAMECD2 AND M05.TYPE = '3' AND M05.MONTH = 05 ");
            stb.append("     LEFT JOIN MONTH M06 ON M06.VISIT_REASON1 = N1.NAMECD2 AND M06.TYPE = '3' AND M06.MONTH = 06 ");
            stb.append("     LEFT JOIN MONTH M07 ON M07.VISIT_REASON1 = N1.NAMECD2 AND M07.TYPE = '3' AND M07.MONTH = 07 ");
            stb.append("     LEFT JOIN MONTH M08 ON M08.VISIT_REASON1 = N1.NAMECD2 AND M08.TYPE = '3' AND M08.MONTH = 08 ");
            stb.append("     LEFT JOIN MONTH M09 ON M09.VISIT_REASON1 = N1.NAMECD2 AND M09.TYPE = '3' AND M09.MONTH = 09 ");
            stb.append("     LEFT JOIN MONTH M10 ON M10.VISIT_REASON1 = N1.NAMECD2 AND M10.TYPE = '3' AND M10.MONTH = 10 ");
            stb.append("     LEFT JOIN MONTH M11 ON M11.VISIT_REASON1 = N1.NAMECD2 AND M11.TYPE = '3' AND M11.MONTH = 11 ");
            stb.append("     LEFT JOIN MONTH M12 ON M12.VISIT_REASON1 = N1.NAMECD2 AND M12.TYPE = '3' AND M12.MONTH = 12 ");
            stb.append("     LEFT JOIN MONTH M01 ON M01.VISIT_REASON1 = N1.NAMECD2 AND M01.TYPE = '3' AND M01.MONTH = 01 ");
            stb.append("     LEFT JOIN MONTH M02 ON M02.VISIT_REASON1 = N1.NAMECD2 AND M02.TYPE = '3' AND M02.MONTH = 02 ");
            stb.append("     LEFT JOIN MONTH M03 ON M03.VISIT_REASON1 = N1.NAMECD2 AND M03.TYPE = '3' AND M03.MONTH = 03 ");
            stb.append("     LEFT JOIN R_TOTAL ON R_TOTAL.VISIT_REASON1 = N1.NAMECD2 AND R_TOTAL.TYPE = '3' ");
            stb.append(" WHERE ");
            stb.append("     N1.YEAR = '" + param._year + "' ");
            stb.append("     AND N1.NAMECD1 = 'F203' ");
            stb.append("     AND N1.NAMECD2 IN ('01','02','03') ");
            stb.append(" ORDER BY ");
            stb.append("     N1.NAMECD2 ");
            stb.append(" ), M_TOTAL3 AS( ");
            stb.append(" SELECT  ");
            stb.append("     MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("     TYPE, ");
            stb.append("     COUNT(*)          AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-4-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("     AND VISIT_REASON1 IN ('01','02','03') ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH(VISIT_DATE) , ");
            stb.append("     TYPE ");
            stb.append(" ), M_ALL_TOTAL3 AS( ");
            stb.append(" SELECT  ");
            stb.append("     TYPE, ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-4-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("     AND VISIT_REASON1 IN ('01','02','03') ");
            stb.append(" GROUP BY ");
            stb.append("     TYPE ");
            stb.append(" ), TYPE3_SUB AS ( ");
            stb.append(" SELECT ");
            stb.append("     'TYPE3_SUB'  AS REASON_CD, ");
            stb.append("     'その他小計' AS REASON_NAME, ");
            stb.append("     MT04.CNT AS M04CNT, ");
            stb.append("     MT05.CNT AS M05CNT, ");
            stb.append("     MT06.CNT AS M06CNT, ");
            stb.append("     MT07.CNT AS M07CNT, ");
            stb.append("     MT08.CNT AS M08CNT, ");
            stb.append("     MT09.CNT AS M09CNT, ");
            stb.append("     MT10.CNT AS M10CNT, ");
            stb.append("     MT11.CNT AS M11CNT, ");
            stb.append("     MT12.CNT AS M12CNT, ");
            stb.append("     MT01.CNT AS M01CNT, ");
            stb.append("     MT02.CNT AS M02CNT, ");
            stb.append("     MT03.CNT AS M03CNT, ");
            stb.append("     MAT.CNT AS TOTAL, ");
            stb.append("     6 AS ROW ");
            stb.append(" FROM ");
            stb.append("     (SELECT TYPE FROM M_TOTAL3 GROUP BY TYPE) T1 ");
            stb.append("     LEFT JOIN M_TOTAL3 MT04 ON MT04.TYPE = T1.TYPE AND MT04.MONTH = 04 ");
            stb.append("     LEFT JOIN M_TOTAL3 MT05 ON MT05.TYPE = T1.TYPE AND MT05.MONTH = 05 ");
            stb.append("     LEFT JOIN M_TOTAL3 MT06 ON MT06.TYPE = T1.TYPE AND MT06.MONTH = 06 ");
            stb.append("     LEFT JOIN M_TOTAL3 MT07 ON MT07.TYPE = T1.TYPE AND MT07.MONTH = 07 ");
            stb.append("     LEFT JOIN M_TOTAL3 MT08 ON MT08.TYPE = T1.TYPE AND MT08.MONTH = 08 ");
            stb.append("     LEFT JOIN M_TOTAL3 MT09 ON MT09.TYPE = T1.TYPE AND MT09.MONTH = 09 ");
            stb.append("     LEFT JOIN M_TOTAL3 MT10 ON MT10.TYPE = T1.TYPE AND MT10.MONTH = 10 ");
            stb.append("     LEFT JOIN M_TOTAL3 MT11 ON MT11.TYPE = T1.TYPE AND MT11.MONTH = 11 ");
            stb.append("     LEFT JOIN M_TOTAL3 MT12 ON MT12.TYPE = T1.TYPE AND MT12.MONTH = 12 ");
            stb.append("     LEFT JOIN M_TOTAL3 MT01 ON MT01.TYPE = T1.TYPE AND MT01.MONTH = 01 ");
            stb.append("     LEFT JOIN M_TOTAL3 MT02 ON MT02.TYPE = T1.TYPE AND MT02.MONTH = 02 ");
            stb.append("     LEFT JOIN M_TOTAL3 MT03 ON MT03.TYPE = T1.TYPE AND MT03.MONTH = 03 ");
            stb.append("     LEFT JOIN M_ALL_TOTAL3 MAT ON MAT.TYPE = T1.TYPE ");
            stb.append(" WHERE ");
            stb.append("    T1.TYPE = '3' ");
            stb.append(" ), TYPE3 AS ( ");
            stb.append(" SELECT * FROM TYPE3_MAIN ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM TYPE3_SUB ");
            return stb.toString();
        }

        private static String setSqlType5(final Param param) {
            final StringBuffer stb = new StringBuffer();

            //相談活動
            stb.append(" ), M_TOTAL5 AS( ");
            stb.append(" SELECT  ");
            stb.append("     MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("     TYPE, ");
            stb.append("     COUNT(*)          AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-4-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH(VISIT_DATE) , ");
            stb.append("     TYPE ");
            stb.append(" ), M_ALL_TOTAL5 AS( ");
            stb.append(" SELECT  ");
            stb.append("     TYPE, ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-4-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append(" GROUP BY ");
            stb.append("     TYPE ");
            stb.append(" ), TYPE5 AS ( ");
            stb.append(" SELECT ");
            stb.append("     'R100'     AS REASON_CD, ");
            stb.append("     '相談活動' AS REASON_NAME, ");
            stb.append("     MT04.CNT AS M04CNT, ");
            stb.append("     MT05.CNT AS M05CNT, ");
            stb.append("     MT06.CNT AS M06CNT, ");
            stb.append("     MT07.CNT AS M07CNT, ");
            stb.append("     MT08.CNT AS M08CNT, ");
            stb.append("     MT09.CNT AS M09CNT, ");
            stb.append("     MT10.CNT AS M10CNT, ");
            stb.append("     MT11.CNT AS M11CNT, ");
            stb.append("     MT12.CNT AS M12CNT, ");
            stb.append("     MT01.CNT AS M01CNT, ");
            stb.append("     MT02.CNT AS M02CNT, ");
            stb.append("     MT03.CNT AS M03CNT, ");
            stb.append("     MAT.CNT  AS TOTAL, ");
            stb.append("     7 AS ROW ");
            stb.append(" FROM ");
            stb.append("     (SELECT TYPE FROM M_TOTAL5 GROUP BY TYPE) T1 ");
            stb.append("     LEFT JOIN M_TOTAL5 MT04 ON T1.TYPE = MT04.TYPE AND MT04.MONTH = 04 ");
            stb.append("     LEFT JOIN M_TOTAL5 MT05 ON T1.TYPE = MT05.TYPE AND MT05.MONTH = 05 ");
            stb.append("     LEFT JOIN M_TOTAL5 MT06 ON T1.TYPE = MT06.TYPE AND MT06.MONTH = 06 ");
            stb.append("     LEFT JOIN M_TOTAL5 MT07 ON T1.TYPE = MT07.TYPE AND MT07.MONTH = 07 ");
            stb.append("     LEFT JOIN M_TOTAL5 MT08 ON T1.TYPE = MT08.TYPE AND MT08.MONTH = 08 ");
            stb.append("     LEFT JOIN M_TOTAL5 MT09 ON T1.TYPE = MT09.TYPE AND MT09.MONTH = 09 ");
            stb.append("     LEFT JOIN M_TOTAL5 MT10 ON T1.TYPE = MT10.TYPE AND MT10.MONTH = 10 ");
            stb.append("     LEFT JOIN M_TOTAL5 MT11 ON T1.TYPE = MT11.TYPE AND MT11.MONTH = 11 ");
            stb.append("     LEFT JOIN M_TOTAL5 MT12 ON T1.TYPE = MT12.TYPE AND MT12.MONTH = 12 ");
            stb.append("     LEFT JOIN M_TOTAL5 MT01 ON T1.TYPE = MT01.TYPE AND MT01.MONTH = 01 ");
            stb.append("     LEFT JOIN M_TOTAL5 MT02 ON T1.TYPE = MT02.TYPE AND MT02.MONTH = 02 ");
            stb.append("     LEFT JOIN M_TOTAL5 MT03 ON T1.TYPE = MT03.TYPE AND MT03.MONTH = 03 ");
            stb.append("     LEFT JOIN M_ALL_TOTAL5 MAT ON T1.TYPE = MAT.TYPE ");
            stb.append(" WHERE ");
            stb.append("     T1.TYPE = '5' ");

            return stb.toString();
        }

        private static String setSqlType3_2(final Param param) {
            final StringBuffer stb = new StringBuffer();

            //保健室登校(延べ人数)
            stb.append(" ), TYPE3_2 AS( ");
            stb.append(" SELECT  ");
            stb.append("     N1.NAMECD2 AS REASON_CD, ");
            stb.append("     N1.NAME1   AS REASON_NAME, ");
            stb.append("     M04.CNT AS M04CNT, ");
            stb.append("     M05.CNT AS M05CNT, ");
            stb.append("     M06.CNT AS M06CNT, ");
            stb.append("     M07.CNT AS M07CNT, ");
            stb.append("     M08.CNT AS M08CNT, ");
            stb.append("     M09.CNT AS M09CNT, ");
            stb.append("     M10.CNT AS M10CNT, ");
            stb.append("     M11.CNT AS M11CNT, ");
            stb.append("     M12.CNT AS M12CNT, ");
            stb.append("     M01.CNT AS M01CNT, ");
            stb.append("     M02.CNT AS M02CNT, ");
            stb.append("     M03.CNT AS M03CNT, ");
            stb.append("     R_TOTAL.CNT AS TOTAL, ");
            stb.append("     8 AS ROW ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST N1  ");
            stb.append("     LEFT JOIN MONTH M04 ON M04.VISIT_REASON1 = N1.NAMECD2 AND M04.TYPE = '3' AND M04.MONTH = 04 ");
            stb.append("     LEFT JOIN MONTH M05 ON M05.VISIT_REASON1 = N1.NAMECD2 AND M05.TYPE = '3' AND M05.MONTH = 05 ");
            stb.append("     LEFT JOIN MONTH M06 ON M06.VISIT_REASON1 = N1.NAMECD2 AND M06.TYPE = '3' AND M06.MONTH = 06 ");
            stb.append("     LEFT JOIN MONTH M07 ON M07.VISIT_REASON1 = N1.NAMECD2 AND M07.TYPE = '3' AND M07.MONTH = 07 ");
            stb.append("     LEFT JOIN MONTH M08 ON M08.VISIT_REASON1 = N1.NAMECD2 AND M08.TYPE = '3' AND M08.MONTH = 08 ");
            stb.append("     LEFT JOIN MONTH M09 ON M09.VISIT_REASON1 = N1.NAMECD2 AND M09.TYPE = '3' AND M09.MONTH = 09 ");
            stb.append("     LEFT JOIN MONTH M10 ON M10.VISIT_REASON1 = N1.NAMECD2 AND M10.TYPE = '3' AND M10.MONTH = 10 ");
            stb.append("     LEFT JOIN MONTH M11 ON M11.VISIT_REASON1 = N1.NAMECD2 AND M11.TYPE = '3' AND M11.MONTH = 11 ");
            stb.append("     LEFT JOIN MONTH M12 ON M12.VISIT_REASON1 = N1.NAMECD2 AND M12.TYPE = '3' AND M12.MONTH = 12 ");
            stb.append("     LEFT JOIN MONTH M01 ON M01.VISIT_REASON1 = N1.NAMECD2 AND M01.TYPE = '3' AND M01.MONTH = 01 ");
            stb.append("     LEFT JOIN MONTH M02 ON M02.VISIT_REASON1 = N1.NAMECD2 AND M02.TYPE = '3' AND M02.MONTH = 02 ");
            stb.append("     LEFT JOIN MONTH M03 ON M03.VISIT_REASON1 = N1.NAMECD2 AND M03.TYPE = '3' AND M03.MONTH = 03 ");
            stb.append("     LEFT JOIN R_TOTAL ON R_TOTAL.VISIT_REASON1 = N1.NAMECD2 AND R_TOTAL.TYPE = '3' ");
            stb.append(" WHERE ");
            stb.append("     N1.YEAR = '" + param._year + "' ");
            stb.append("     AND N1.NAMECD1 = 'F203' ");
            stb.append("     AND N1.NAMECD2 IN ('04') ");
            stb.append(" ORDER BY ");
            stb.append("     N1.NAMECD2 ");
            return stb.toString();
        }

        private static String setSqlTotal(final Param param) {
            final StringBuffer stb = new StringBuffer();

            //総合計
            stb.append(" ), TOTAL AS ( ");
            stb.append(" SELECT  ");
            stb.append("     'R101'   AS REASON_CD, ");
            stb.append("     '総合計' AS REASON_NAME, ");
            stb.append("     SUM(T1.M04CNT) AS M04CNT, ");
            stb.append("     SUM(T1.M05CNT) AS M05CNT, ");
            stb.append("     SUM(T1.M06CNT) AS M06CNT, ");
            stb.append("     SUM(T1.M07CNT) AS M07CNT, ");
            stb.append("     SUM(T1.M08CNT) AS M08CNT, ");
            stb.append("     SUM(T1.M09CNT) AS M09CNT, ");
            stb.append("     SUM(T1.M10CNT) AS M10CNT, ");
            stb.append("     SUM(T1.M11CNT) AS M11CNT, ");
            stb.append("     SUM(T1.M12CNT) AS M12CNT, ");
            stb.append("     SUM(T1.M01CNT) AS M01CNT, ");
            stb.append("     SUM(T1.M02CNT) AS M02CNT, ");
            stb.append("     SUM(T1.M03CNT) AS M03CNT, ");
            stb.append("     SUM(TOTAL) AS TOTAL, ");
            stb.append("     9 AS ROW ");
            stb.append(" FROM  ");
            stb.append("     ( ");
            stb.append("     SELECT * FROM TYPE1_SUB ");
            stb.append("     UNION ");
            stb.append("     SELECT * FROM TYPE2_SUB ");
            stb.append("     UNION ");
            stb.append("     SELECT * FROM TYPE3_SUB ");
            stb.append("     UNION ");
            stb.append("     SELECT * FROM TYPE5 ");
            stb.append("     UNION ");
            stb.append("     SELECT * FROM TYPE3_2 ");
            stb.append("     ) AS T1 ");
            return stb.toString();
        }

        private static String setSqlRrt(final Param param) {
            final StringBuffer stb = new StringBuffer();

            //休養者数
            stb.append(" ), RESULT_REST_TOTAL AS( ");
            stb.append(" SELECT  ");
            stb.append("     MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("     COUNT(VISIT_DATE) AS CNT, ");
            stb.append("     'X' AS JOINCD ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     RESULT_REST = '1' ");
            stb.append("     AND VISIT_DATE >= '" + param._year + "-4-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH(VISIT_DATE) , ");
            stb.append("     RESULT_REST ");
            stb.append(" ), RESULT_REST_ALL_TOTAL AS( ");
            stb.append(" SELECT  ");
            stb.append("     COUNT(VISIT_DATE) AS CNT, ");
            stb.append("     'X' AS JOINCD ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     RESULT_REST = '1' ");
            stb.append("     AND VISIT_DATE >= '" + param._year + "-4-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append(" GROUP BY ");
            stb.append("     RESULT_REST ");
            stb.append(" ), RRT AS ( ");
            stb.append(" SELECT ");
            stb.append("     'R102'     AS REASON_CD, ");
            stb.append("     '休養者数' AS REASON_NAME, ");
            stb.append("     RRT04.CNT AS M04CNT, ");
            stb.append("     RRT05.CNT AS M05CNT, ");
            stb.append("     RRT06.CNT AS M06CNT, ");
            stb.append("     RRT07.CNT AS M07CNT, ");
            stb.append("     RRT08.CNT AS M08CNT, ");
            stb.append("     RRT09.CNT AS M09CNT, ");
            stb.append("     RRT10.CNT AS M10CNT, ");
            stb.append("     RRT11.CNT AS M11CNT, ");
            stb.append("     RRT12.CNT AS M12CNT, ");
            stb.append("     RRT01.CNT AS M01CNT, ");
            stb.append("     RRT02.CNT AS M02CNT, ");
            stb.append("     RRT03.CNT AS M03CNT, ");
            stb.append("     RRAT.CNT  AS TOTAL, ");
            stb.append("     10 AS ROW ");
            stb.append(" FROM ");
            stb.append("     (SELECT JOINCD FROM RESULT_REST_TOTAL GROUP BY JOINCD) T1 ");
            stb.append("     LEFT JOIN RESULT_REST_TOTAL RRT04 ON RRT04.JOINCD = T1.JOINCD AND RRT04.MONTH = 04 ");
            stb.append("     LEFT JOIN RESULT_REST_TOTAL RRT05 ON RRT05.JOINCD = T1.JOINCD AND RRT05.MONTH = 05 ");
            stb.append("     LEFT JOIN RESULT_REST_TOTAL RRT06 ON RRT06.JOINCD = T1.JOINCD AND RRT06.MONTH = 06 ");
            stb.append("     LEFT JOIN RESULT_REST_TOTAL RRT07 ON RRT07.JOINCD = T1.JOINCD AND RRT07.MONTH = 07 ");
            stb.append("     LEFT JOIN RESULT_REST_TOTAL RRT08 ON RRT08.JOINCD = T1.JOINCD AND RRT08.MONTH = 08 ");
            stb.append("     LEFT JOIN RESULT_REST_TOTAL RRT09 ON RRT09.JOINCD = T1.JOINCD AND RRT09.MONTH = 09 ");
            stb.append("     LEFT JOIN RESULT_REST_TOTAL RRT10 ON RRT10.JOINCD = T1.JOINCD AND RRT10.MONTH = 10 ");
            stb.append("     LEFT JOIN RESULT_REST_TOTAL RRT11 ON RRT11.JOINCD = T1.JOINCD AND RRT11.MONTH = 11 ");
            stb.append("     LEFT JOIN RESULT_REST_TOTAL RRT12 ON RRT12.JOINCD = T1.JOINCD AND RRT12.MONTH = 12 ");
            stb.append("     LEFT JOIN RESULT_REST_TOTAL RRT01 ON RRT01.JOINCD = T1.JOINCD AND RRT01.MONTH = 01 ");
            stb.append("     LEFT JOIN RESULT_REST_TOTAL RRT02 ON RRT02.JOINCD = T1.JOINCD AND RRT02.MONTH = 02 ");
            stb.append("     LEFT JOIN RESULT_REST_TOTAL RRT03 ON RRT03.JOINCD = T1.JOINCD AND RRT03.MONTH = 03 ");
            stb.append("     LEFT JOIN RESULT_REST_ALL_TOTAL RRAT ON RRAT.JOINCD = T1.JOINCD ");
            return stb.toString();
        }

        private static String setSqlZaiseki(final Param param) {
            final StringBuffer stb = new StringBuffer();

            //在籍者数
            stb.append(" ), REGD AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T2.GRD_DATE, ");
            stb.append("     T2.ENT_DATE ");
            stb.append(" FROM  ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ");
            stb.append("             ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE  ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append(" ), ZAISEKI AS ( ");
            stb.append(" SELECT  ");
            stb.append("     'R104'     AS REASON_CD, ");
            stb.append("     '在籍者数' AS REASON_NAME, ");
            stb.append("     Z04.CNT    AS M04CNT, ");
            stb.append("     Z05.CNT    AS M05CNT, ");
            stb.append("     Z06.CNT    AS M06CNT, ");
            stb.append("     Z07.CNT    AS M07CNT, ");
            stb.append("     Z08.CNT    AS M08CNT, ");
            stb.append("     Z09.CNT    AS M09CNT, ");
            stb.append("     Z10.CNT    AS M10CNT, ");
            stb.append("     Z11.CNT    AS M11CNT, ");
            stb.append("     Z12.CNT    AS M12CNT, ");
            stb.append("     Z01.CNT    AS M01CNT, ");
            stb.append("     Z02.CNT    AS M02CNT, ");
            stb.append("     Z03.CNT    AS M03CNT, ");
            stb.append("     ( Z04.CNT + Z05.CNT + Z06.CNT + Z07.CNT + Z08.CNT + Z09.CNT +  ");
            stb.append("       Z10.CNT + Z11.CNT + Z12.CNT + Z01.CNT + Z02.CNT + Z03.CNT ) AS TOTAL, ");
            stb.append("     12 AS ROW  ");
            stb.append(" FROM ");
            stb.append("     (SELECT COUNT(*) AS CNT, 'X' AS JOINCD FROM REGD WHERE  CHAR(ENT_DATE) <= '" + param._year + "-04-01' AND (GRD_DATE IS NULL OR  CHAR(GRD_DATE) < '" + param._year + "-04-31' )) Z04 ");
            stb.append("     LEFT JOIN (SELECT COUNT(*) AS CNT, 'X' AS JOINCD FROM REGD WHERE  CHAR(ENT_DATE) <= '" + param._year + "-05-01' AND (GRD_DATE IS NULL OR  CHAR(GRD_DATE) < '" + param._year + "-05-31' )) Z05 ON Z05.JOINCD = Z04.JOINCD ");
            stb.append("     LEFT JOIN (SELECT COUNT(*) AS CNT, 'X' AS JOINCD FROM REGD WHERE  CHAR(ENT_DATE) <= '" + param._year + "-06-01' AND (GRD_DATE IS NULL OR  CHAR(GRD_DATE) < '" + param._year + "-06-31' )) Z06 ON Z06.JOINCD = Z04.JOINCD ");
            stb.append("     LEFT JOIN (SELECT COUNT(*) AS CNT, 'X' AS JOINCD FROM REGD WHERE  CHAR(ENT_DATE) <= '" + param._year + "-07-01' AND (GRD_DATE IS NULL OR  CHAR(GRD_DATE) < '" + param._year + "-07-31' )) Z07 ON Z07.JOINCD = Z04.JOINCD ");
            stb.append("     LEFT JOIN (SELECT COUNT(*) AS CNT, 'X' AS JOINCD FROM REGD WHERE  CHAR(ENT_DATE) <= '" + param._year + "-08-01' AND (GRD_DATE IS NULL OR  CHAR(GRD_DATE) < '" + param._year + "-08-31' )) Z08 ON Z08.JOINCD = Z04.JOINCD ");
            stb.append("     LEFT JOIN (SELECT COUNT(*) AS CNT, 'X' AS JOINCD FROM REGD WHERE  CHAR(ENT_DATE) <= '" + param._year + "-09-01' AND (GRD_DATE IS NULL OR  CHAR(GRD_DATE) < '" + param._year + "-09-31' )) Z09 ON Z09.JOINCD = Z04.JOINCD ");
            stb.append("     LEFT JOIN (SELECT COUNT(*) AS CNT, 'X' AS JOINCD FROM REGD WHERE  CHAR(ENT_DATE) <= '" + param._year + "-10-01' AND (GRD_DATE IS NULL OR  CHAR(GRD_DATE) < '" + param._year + "-10-31' )) Z10 ON Z10.JOINCD = Z04.JOINCD ");
            stb.append("     LEFT JOIN (SELECT COUNT(*) AS CNT, 'X' AS JOINCD FROM REGD WHERE  CHAR(ENT_DATE) <= '" + param._year + "-11-01' AND (GRD_DATE IS NULL OR  CHAR(GRD_DATE) < '" + param._year + "-11-31' )) Z11 ON Z11.JOINCD = Z04.JOINCD ");
            stb.append("     LEFT JOIN (SELECT COUNT(*) AS CNT, 'X' AS JOINCD FROM REGD WHERE  CHAR(ENT_DATE) <= '" + param._year + "-12-01' AND (GRD_DATE IS NULL OR  CHAR(GRD_DATE) < '" + param._year + "-12-31' )) Z12 ON Z12.JOINCD = Z04.JOINCD ");
            stb.append("     LEFT JOIN (SELECT COUNT(*) AS CNT, 'X' AS JOINCD FROM REGD WHERE  CHAR(ENT_DATE) <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-01-01' AND (GRD_DATE IS NULL OR  CHAR(GRD_DATE) < '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-01-31' )) Z01 ON Z01.JOINCD = Z04.JOINCD ");
            stb.append("     LEFT JOIN (SELECT COUNT(*) AS CNT, 'X' AS JOINCD FROM REGD WHERE  CHAR(ENT_DATE) <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-02-01' AND (GRD_DATE IS NULL OR  CHAR(GRD_DATE) < '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-02-31' )) Z02 ON Z02.JOINCD = Z04.JOINCD ");
            stb.append("     LEFT JOIN (SELECT COUNT(*) AS CNT, 'X' AS JOINCD FROM REGD WHERE  CHAR(ENT_DATE) <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-01' AND (GRD_DATE IS NULL OR  CHAR(GRD_DATE) < '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' )) Z03 ON Z03.JOINCD = Z04.JOINCD ");
            return stb.toString();
        }

        private static String setSqlLesson(final Param param) {
            final StringBuffer stb = new StringBuffer();

            //授業日数
            stb.append(" ), LESSON AS ( ");
            stb.append(" SELECT  ");
            stb.append("     'R105'     AS REASON_CD, ");
            stb.append("     '授業日数' AS REASON_NAME, ");
            stb.append("     DECIMAL(M04.LESSON) AS M04CNT, ");
            stb.append("     DECIMAL(M05.LESSON) AS M05CNT, ");
            stb.append("     DECIMAL(M06.LESSON) AS M06CNT, ");
            stb.append("     DECIMAL(M07.LESSON) AS M07CNT, ");
            stb.append("     DECIMAL(M08.LESSON) AS M08CNT, ");
            stb.append("     DECIMAL(M09.LESSON) AS M09CNT, ");
            stb.append("     DECIMAL(M10.LESSON) AS M10CNT, ");
            stb.append("     DECIMAL(M11.LESSON) AS M11CNT, ");
            stb.append("     DECIMAL(M12.LESSON) AS M12CNT, ");
            stb.append("     DECIMAL(M01.LESSON) AS M01CNT, ");
            stb.append("     DECIMAL(M02.LESSON) AS M02CNT, ");
            stb.append("     DECIMAL(M03.LESSON) AS M03CNT, ");
            stb.append("     DECIMAL(ALL.CNT)    AS TOTAL, ");
            stb.append("     13 AS ROW ");
            stb.append(" FROM ");
            stb.append("     (SELECT YEAR FROM NURSEOFF_LESSON_DAT GROUP BY YEAR) T1 ");
            stb.append("     LEFT JOIN NURSEOFF_LESSON_DAT M04 ON M04.YEAR = T1.YEAR AND M04.MONTH = '04' ");
            stb.append("     LEFT JOIN NURSEOFF_LESSON_DAT M05 ON M05.YEAR = T1.YEAR AND M05.MONTH = '05' ");
            stb.append("     LEFT JOIN NURSEOFF_LESSON_DAT M06 ON M06.YEAR = T1.YEAR AND M06.MONTH = '06' ");
            stb.append("     LEFT JOIN NURSEOFF_LESSON_DAT M07 ON M07.YEAR = T1.YEAR AND M07.MONTH = '07' ");
            stb.append("     LEFT JOIN NURSEOFF_LESSON_DAT M08 ON M08.YEAR = T1.YEAR AND M08.MONTH = '08' ");
            stb.append("     LEFT JOIN NURSEOFF_LESSON_DAT M09 ON M09.YEAR = T1.YEAR AND M09.MONTH = '09' ");
            stb.append("     LEFT JOIN NURSEOFF_LESSON_DAT M10 ON M10.YEAR = T1.YEAR AND M10.MONTH = '10' ");
            stb.append("     LEFT JOIN NURSEOFF_LESSON_DAT M11 ON M11.YEAR = T1.YEAR AND M11.MONTH = '11' ");
            stb.append("     LEFT JOIN NURSEOFF_LESSON_DAT M12 ON M12.YEAR = T1.YEAR AND M12.MONTH = '12' ");
            stb.append("     LEFT JOIN NURSEOFF_LESSON_DAT M01 ON M01.YEAR = T1.YEAR AND M01.MONTH = '01' ");
            stb.append("     LEFT JOIN NURSEOFF_LESSON_DAT M02 ON M02.YEAR = T1.YEAR AND M02.MONTH = '02' ");
            stb.append("     LEFT JOIN NURSEOFF_LESSON_DAT M03 ON M03.YEAR = T1.YEAR AND M03.MONTH = '03' ");
            stb.append("     LEFT JOIN (SELECT YEAR,SUM(INTEGER(LESSON)) AS CNT FROM NURSEOFF_LESSON_DAT GROUP BY YEAR) ALL ");
            stb.append("                  ON ALL.YEAR = T1.YEAR   ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");

            return stb.toString();
        }

        private static String setSqlOnAv(final Param param) {
            final StringBuffer stb = new StringBuffer();

            //1日平均利用者数
            stb.append(" ), ONAV AS ( ");
            stb.append(" SELECT ");
            stb.append("     'R103'            AS REASON_CD, ");
            stb.append("     '1日平均利用者数' AS REASON_NAME, ");
            stb.append("     DECIMAL(ROUND((FLOAT(T1.M04CNT) / FLOAT(T2.M04CNT))*10,0)/10,5,1) AS M04CNT, ");
            stb.append("     DECIMAL(ROUND((FLOAT(T1.M05CNT) / FLOAT(T2.M05CNT))*10,0)/10,5,1) AS M05CNT, ");
            stb.append("     DECIMAL(ROUND((FLOAT(T1.M06CNT) / FLOAT(T2.M06CNT))*10,0)/10,5,1) AS M06CNT, ");
            stb.append("     DECIMAL(ROUND((FLOAT(T1.M07CNT) / FLOAT(T2.M07CNT))*10,0)/10,5,1) AS M07CNT, ");
            stb.append("     DECIMAL(ROUND((FLOAT(T1.M08CNT) / FLOAT(T2.M08CNT))*10,0)/10,5,1) AS M08CNT, ");
            stb.append("     DECIMAL(ROUND((FLOAT(T1.M09CNT) / FLOAT(T2.M09CNT))*10,0)/10,5,1) AS M09CNT, ");
            stb.append("     DECIMAL(ROUND((FLOAT(T1.M10CNT) / FLOAT(T2.M10CNT))*10,0)/10,5,1) AS M10CNT, ");
            stb.append("     DECIMAL(ROUND((FLOAT(T1.M11CNT) / FLOAT(T2.M11CNT))*10,0)/10,5,1) AS M11CNT, ");
            stb.append("     DECIMAL(ROUND((FLOAT(T1.M12CNT) / FLOAT(T2.M12CNT))*10,0)/10,5,1) AS M12CNT, ");
            stb.append("     DECIMAL(ROUND((FLOAT(T1.M01CNT) / FLOAT(T2.M01CNT))*10,0)/10,5,1) AS M01CNT, ");
            stb.append("     DECIMAL(ROUND((FLOAT(T1.M02CNT) / FLOAT(T2.M02CNT))*10,0)/10,5,1) AS M02CNT, ");
            stb.append("     DECIMAL(ROUND((FLOAT(T1.M03CNT) / FLOAT(T2.M03CNT))*10,0)/10,5,1) AS M03CNT, ");
            stb.append("     DECIMAL(ROUND((FLOAT(T1.TOTAL) / FLOAT(T2.TOTAL))*10,0)/10,5,1) AS TOTAL, ");
            stb.append("     11 AS ROW ");
            stb.append(" FROM ");
            stb.append("     (SELECT T1.*, 'X' AS JOINCD FROM TOTAL T1) T1 ");
            stb.append("     FULL OUTER JOIN (SELECT T1.*, 'X' AS JOINCD FROM LESSON T1)T2 ON T2.JOINCD = T1.JOINCD ");
            return stb.toString();
        }

    }

    private static class BaseDat2 {
        final String _m04cnt;
        final String _m05cnt;
        final String _m06cnt;
        final String _m07cnt;
        final String _m08cnt;
        final String _m09cnt;
        final String _m10cnt;
        final String _m11cnt;
        final String _m12cnt;
        final String _m01cnt;
        final String _m02cnt;
        final String _m03cnt;
        final String _total;

        BaseDat2(
                final String m04cnt,
                final String m05cnt,
                final String m06cnt,
                final String m07cnt,
                final String m08cnt,
                final String m09cnt,
                final String m10cnt,
                final String m11cnt,
                final String m12cnt,
                final String m01cnt,
                final String m02cnt,
                final String m03cnt,
                final String total
        ) {
            _m04cnt = m04cnt;
            _m05cnt = m05cnt;
            _m06cnt = m06cnt;
            _m07cnt = m07cnt;
            _m08cnt = m08cnt;
            _m09cnt = m09cnt;
            _m10cnt = m10cnt;
            _m11cnt = m11cnt;
            _m12cnt = m12cnt;
            _m01cnt = m01cnt;
            _m02cnt = m02cnt;
            _m03cnt = m03cnt;
            _total = total;

        }

        public static List load(final DB2UDB db2, final Param param) {
            String[] reasonCd = {"T1_01","T1_02","T1_03","T1_04","T1_05","T1_06","T1_07","T1_99","T1_SUB","T2_01","T2_02","T2_03","T2_04","T2_05","T2_06","T2_07","T2_99","T2_SUB","R100","R101"};
            int idx = 0;
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    while(!rs.getString("REASON_CD").equals(reasonCd[idx])) {
                        //取得できなかった行の挿入
                        final BaseDat2 basedat = new BaseDat2("0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0");
                        list.add(basedat);
                        idx++;
                    }
                    idx++;

                    final String m04cnt = rs.getString("M04CNT");
                    final String m05cnt = rs.getString("M05CNT");
                    final String m06cnt = rs.getString("M06CNT");
                    final String m07cnt = rs.getString("M07CNT");
                    final String m08cnt = rs.getString("M08CNT");
                    final String m09cnt = rs.getString("M09CNT");
                    final String m10cnt = rs.getString("M10CNT");
                    final String m11cnt = rs.getString("M11CNT");
                    final String m12cnt = rs.getString("M12CNT");
                    final String m01cnt = rs.getString("M01CNT");
                    final String m02cnt = rs.getString("M02CNT");
                    final String m03cnt = rs.getString("M03CNT");
                    final String total = rs.getString("TOTAL");

                    final BaseDat2 basedat = new BaseDat2(m04cnt, m05cnt, m06cnt, m07cnt, m08cnt, m09cnt, m10cnt, m11cnt, m12cnt, m01cnt, m02cnt, m03cnt, total);
                    list.add(basedat);

                }

                while(idx != reasonCd.length) {
                    //取得できなかった行の挿入
                    final BaseDat2 basedat = new BaseDat2("0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0");
                    list.add(basedat);
                    idx++;
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

            stb.append(setSqlType5_1(param)); //相談活動内容(延べ)
            stb.append(setSqlType5_2(param)); //相談活動内容(実人数)
            stb.append(setSqlType3(param));   //保健室登校
            stb.append(" ) ");
            stb.append(" SELECT * FROM MAIN1 "); //相談活動内容(延べ)
            stb.append(" UNION ");
            stb.append(" SELECT * FROM MAIN2 "); //相談活動内容(実人数)
            stb.append(" UNION ");
            stb.append(" SELECT * FROM MAIN3 "); //保健室登校
            stb.append(" ORDER BY ROW,REASON_CD ");

            return stb.toString();
        }

        private static String setSqlType5_1(final Param param) {
            final StringBuffer stb = new StringBuffer();

            //相談活動内容(延べ)
            stb.append(" WITH REASON01_1 AS ( ");
            stb.append(" SELECT  ");
            stb.append("     MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("     TYPE, ");
            stb.append("     '01'              AS VISIT_REASON, ");
            stb.append("     COUNT(*)          AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-04-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("     AND ( VISIT_REASON1    = '01' ");
            stb.append("           OR VISIT_REASON2 = '01' ");
            stb.append("           OR VISIT_REASON3 = '01' )  ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH(VISIT_DATE) , ");
            stb.append("     TYPE               ");
            stb.append(" ), REASON02_1 AS ( ");
            stb.append(" SELECT  ");
            stb.append("     MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("     TYPE, ");
            stb.append("     '02'              AS VISIT_REASON, ");
            stb.append("     COUNT(*)          AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-04-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("     AND ( VISIT_REASON1    = '02' ");
            stb.append("           OR VISIT_REASON2 = '02' ");
            stb.append("           OR VISIT_REASON3 = '02' )  ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH(VISIT_DATE) , ");
            stb.append("     TYPE               ");
            stb.append(" ), REASON03_1 AS ( ");
            stb.append(" SELECT  ");
            stb.append("     MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("     TYPE, ");
            stb.append("     '03'              AS VISIT_REASON, ");
            stb.append("     COUNT(*)          AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-04-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("     AND ( VISIT_REASON1    = '03' ");
            stb.append("           OR VISIT_REASON2 = '03' ");
            stb.append("           OR VISIT_REASON3 = '03' )  ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH(VISIT_DATE) , ");
            stb.append("     TYPE               ");
            stb.append(" ), REASON04_1 AS ( ");
            stb.append(" SELECT  ");
            stb.append("     MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("     TYPE, ");
            stb.append("     '04'              AS VISIT_REASON, ");
            stb.append("     COUNT(*)          AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-04-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("     AND ( VISIT_REASON1    = '04' ");
            stb.append("           OR VISIT_REASON2 = '04' ");
            stb.append("           OR VISIT_REASON3 = '04' )  ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH(VISIT_DATE) , ");
            stb.append("     TYPE               ");
            stb.append(" ), REASON05_1 AS ( ");
            stb.append(" SELECT  ");
            stb.append("     MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("     TYPE, ");
            stb.append("     '05'              AS VISIT_REASON, ");
            stb.append("     COUNT(*)          AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-04-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("     AND ( VISIT_REASON1    = '05' ");
            stb.append("           OR VISIT_REASON2 = '05' ");
            stb.append("           OR VISIT_REASON3 = '05' )  ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH(VISIT_DATE) , ");
            stb.append("     TYPE               ");
            stb.append(" ), REASON06_1 AS ( ");
            stb.append(" SELECT  ");
            stb.append("     MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("     TYPE, ");
            stb.append("     '06'              AS VISIT_REASON, ");
            stb.append("     COUNT(*)          AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-04-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("     AND ( VISIT_REASON1    = '06' ");
            stb.append("           OR VISIT_REASON2 = '06' ");
            stb.append("           OR VISIT_REASON3 = '06' )  ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH(VISIT_DATE) , ");
            stb.append("     TYPE               ");
            stb.append(" ), REASON07_1 AS ( ");
            stb.append(" SELECT  ");
            stb.append("     MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("     TYPE, ");
            stb.append("     '07'              AS VISIT_REASON, ");
            stb.append("     COUNT(*)          AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-04-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("     AND ( VISIT_REASON1    = '07' ");
            stb.append("           OR VISIT_REASON2 = '07' ");
            stb.append("           OR VISIT_REASON3 = '07' )  ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH(VISIT_DATE) , ");
            stb.append("     TYPE               ");
            stb.append(" ), REASON99_1 AS ( ");
            stb.append(" SELECT  ");
            stb.append("     MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("     TYPE, ");
            stb.append("     '99'              AS VISIT_REASON, ");
            stb.append("     COUNT(*)          AS CNT ");
            stb.append(" FROM  ");
            stb.append("     NURSEOFF_VISITREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     VISIT_DATE >= '" + param._year + "-04-01' ");
            stb.append("     AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("     AND ( VISIT_REASON1    = '99' ");
            stb.append("           OR VISIT_REASON2 = '99' ");
            stb.append("           OR VISIT_REASON3 = '99' )  ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH(VISIT_DATE) , ");
            stb.append("     TYPE               ");
            stb.append(" ), TYPE5_1 AS( ");
            stb.append(" SELECT * FROM REASON01_1 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM REASON02_1 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM REASON03_1 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM REASON04_1 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM REASON05_1 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM REASON06_1 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM REASON07_1 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM REASON99_1 ");
            stb.append(" ), TYPE5_MAIN1 AS( ");
            stb.append(" SELECT  ");
            stb.append("     'T1_' || N1.NAMECD2 AS REASON_CD, ");
            stb.append("     N1.NAME1   AS REASON_NAME, ");
            stb.append("     COALESCE(M04.CNT,0) AS M04CNT, ");
            stb.append("     COALESCE(M05.CNT,0) AS M05CNT, ");
            stb.append("     COALESCE(M06.CNT,0) AS M06CNT, ");
            stb.append("     COALESCE(M07.CNT,0) AS M07CNT, ");
            stb.append("     COALESCE(M08.CNT,0) AS M08CNT, ");
            stb.append("     COALESCE(M09.CNT,0) AS M09CNT, ");
            stb.append("     COALESCE(M10.CNT,0) AS M10CNT, ");
            stb.append("     COALESCE(M11.CNT,0) AS M11CNT, ");
            stb.append("     COALESCE(M12.CNT,0) AS M12CNT, ");
            stb.append("     COALESCE(M01.CNT,0) AS M01CNT, ");
            stb.append("     COALESCE(M02.CNT,0) AS M02CNT, ");
            stb.append("     COALESCE(M03.CNT,0) AS M03CNT, ");
            stb.append("     ( COALESCE(M04.CNT,0) + COALESCE(M05.CNT,0) + COALESCE(M06.CNT,0) +  ");
            stb.append("       COALESCE(M07.CNT,0) + COALESCE(M08.CNT,0) + COALESCE(M09.CNT,0) +  ");
            stb.append("       COALESCE(M10.CNT,0) + COALESCE(M11.CNT,0) + COALESCE(M12.CNT,0) +  ");
            stb.append("       COALESCE(M01.CNT,0) + COALESCE(M02.CNT,0) + COALESCE(M03.CNT,0) ) AS TOTAL, ");
            stb.append("     1 AS ROW ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST N1  ");
            stb.append("     LEFT JOIN TYPE5_1 M04 ON M04.VISIT_REASON = N1.NAMECD2 AND M04.TYPE = '5' AND M04.MONTH = 04 ");
            stb.append("     LEFT JOIN TYPE5_1 M05 ON M05.VISIT_REASON = N1.NAMECD2 AND M05.TYPE = '5' AND M05.MONTH = 05 ");
            stb.append("     LEFT JOIN TYPE5_1 M06 ON M06.VISIT_REASON = N1.NAMECD2 AND M06.TYPE = '5' AND M06.MONTH = 06 ");
            stb.append("     LEFT JOIN TYPE5_1 M07 ON M07.VISIT_REASON = N1.NAMECD2 AND M07.TYPE = '5' AND M07.MONTH = 07 ");
            stb.append("     LEFT JOIN TYPE5_1 M08 ON M08.VISIT_REASON = N1.NAMECD2 AND M08.TYPE = '5' AND M08.MONTH = 08 ");
            stb.append("     LEFT JOIN TYPE5_1 M09 ON M09.VISIT_REASON = N1.NAMECD2 AND M09.TYPE = '5' AND M09.MONTH = 09 ");
            stb.append("     LEFT JOIN TYPE5_1 M10 ON M10.VISIT_REASON = N1.NAMECD2 AND M10.TYPE = '5' AND M10.MONTH = 10 ");
            stb.append("     LEFT JOIN TYPE5_1 M11 ON M11.VISIT_REASON = N1.NAMECD2 AND M11.TYPE = '5' AND M11.MONTH = 11 ");
            stb.append("     LEFT JOIN TYPE5_1 M12 ON M12.VISIT_REASON = N1.NAMECD2 AND M12.TYPE = '5' AND M12.MONTH = 12 ");
            stb.append("     LEFT JOIN TYPE5_1 M01 ON M01.VISIT_REASON = N1.NAMECD2 AND M01.TYPE = '5' AND M01.MONTH = 01 ");
            stb.append("     LEFT JOIN TYPE5_1 M02 ON M02.VISIT_REASON = N1.NAMECD2 AND M02.TYPE = '5' AND M02.MONTH = 02 ");
            stb.append("     LEFT JOIN TYPE5_1 M03 ON M03.VISIT_REASON = N1.NAMECD2 AND M03.TYPE = '5' AND M03.MONTH = 03 ");
            stb.append(" WHERE ");
            stb.append("     N1.YEAR = '" + param._year + "' ");
            stb.append("     AND N1.NAMECD1 = 'F219' ");
            stb.append("     AND N1.NAMECD2 IN ('01','02','03','04','05','06','07','99') ");
            stb.append(" ),TYPE5_TOTAL1 AS( ");
            stb.append(" SELECT ");
            stb.append("     MONTH, ");
            stb.append("     TYPE, ");
            stb.append("     SUM(CNT) AS CNT ");
            stb.append(" FROM TYPE5_1 ");
            stb.append(" GROUP BY  ");
            stb.append("     MONTH, ");
            stb.append("     TYPE ");
            stb.append(" ),TYPE5_ALL_TOTAL1 AS( ");
            stb.append(" SELECT ");
            stb.append("     TYPE, ");
            stb.append("     SUM(CNT) AS CNT ");
            stb.append(" FROM TYPE5_1 ");
            stb.append(" GROUP BY  ");
            stb.append("     TYPE ");
            stb.append(" ), TYPE5_SUB1 AS ( ");
            stb.append(" SELECT ");
            stb.append("     'T1_SUB'   AS REASON_CD, ");
            stb.append("     '延べ小計' AS REASON_NAME, ");
            stb.append("     COALESCE(M04.CNT,0) AS M04CNT, ");
            stb.append("     COALESCE(M05.CNT,0) AS M05CNT, ");
            stb.append("     COALESCE(M06.CNT,0) AS M06CNT, ");
            stb.append("     COALESCE(M07.CNT,0) AS M07CNT, ");
            stb.append("     COALESCE(M08.CNT,0) AS M08CNT, ");
            stb.append("     COALESCE(M09.CNT,0) AS M09CNT, ");
            stb.append("     COALESCE(M10.CNT,0) AS M10CNT, ");
            stb.append("     COALESCE(M11.CNT,0) AS M11CNT, ");
            stb.append("     COALESCE(M12.CNT,0) AS M12CNT, ");
            stb.append("     COALESCE(M01.CNT,0) AS M01CNT, ");
            stb.append("     COALESCE(M02.CNT,0) AS M02CNT, ");
            stb.append("     COALESCE(M03.CNT,0) AS M03CNT, ");
            stb.append("     COALESCE(TAT.CNT,0) AS TOTAL, ");
            stb.append("     2 AS ROW ");
            stb.append(" FROM ");
            stb.append("     (SELECT TYPE FROM TYPE5_TOTAL1 GROUP BY TYPE) T1 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL1 M04 ON T1.TYPE = M04.TYPE AND M04.MONTH = 04 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL1 M05 ON T1.TYPE = M05.TYPE AND M05.MONTH = 05 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL1 M06 ON T1.TYPE = M06.TYPE AND M06.MONTH = 06 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL1 M07 ON T1.TYPE = M07.TYPE AND M07.MONTH = 07 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL1 M08 ON T1.TYPE = M08.TYPE AND M08.MONTH = 08 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL1 M09 ON T1.TYPE = M09.TYPE AND M09.MONTH = 09 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL1 M10 ON T1.TYPE = M10.TYPE AND M10.MONTH = 10 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL1 M11 ON T1.TYPE = M11.TYPE AND M11.MONTH = 11 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL1 M12 ON T1.TYPE = M12.TYPE AND M12.MONTH = 12 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL1 M01 ON T1.TYPE = M01.TYPE AND M01.MONTH = 01 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL1 M02 ON T1.TYPE = M02.TYPE AND M02.MONTH = 02 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL1 M03 ON T1.TYPE = M03.TYPE AND M03.MONTH = 03 ");
            stb.append("     LEFT JOIN TYPE5_ALL_TOTAL1 TAT ON TAT.TYPE = T1.TYPE ");
            stb.append(" WHERE ");
            stb.append("     T1.TYPE = '5' ");
            stb.append(" ), MAIN1 AS( ");
            stb.append(" SELECT * FROM TYPE5_MAIN1 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM TYPE5_SUB1 ");
            stb.append(" ORDER BY ROW, REASON_CD ");
            return stb.toString();
        }

        private static String setSqlType5_2(final Param param) {
            final StringBuffer stb = new StringBuffer();

            //相談活動内容(実人数)
            stb.append(" ), REASON01_2 AS( ");
            stb.append(" SELECT  ");
            stb.append("     T1.MONTH AS MONTH, ");
            stb.append("     T1.TYPE, ");
            stb.append("     '01'     AS VISIT_REASON, ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM  ");
            stb.append("     ( SELECT  ");
            stb.append("           MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("           TYPE ");
            stb.append("       FROM  ");
            stb.append("           NURSEOFF_VISITREC_DAT ");
            stb.append("       WHERE ");
            stb.append("           VISIT_DATE >= '" + param._year + "-04-01' ");
            stb.append("           AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("           AND ( VISIT_REASON1    = '01' ");
            stb.append("                 OR VISIT_REASON2 = '01' ");
            stb.append("                 OR VISIT_REASON3 = '01' )  ");
            stb.append("       GROUP BY ");
            stb.append("           SCHREGNO , ");
            stb.append("           MONTH(VISIT_DATE) , ");
            stb.append("           TYPE ");
            stb.append("     ) T1  ");
            stb.append(" GROUP BY ");
            stb.append("     T1.MONTH , ");
            stb.append("     T1.TYPE ");
            stb.append(" ), REASON02_2 AS( ");
            stb.append(" SELECT  ");
            stb.append("     T1.MONTH AS MONTH, ");
            stb.append("     T1.TYPE, ");
            stb.append("     '02'     AS VISIT_REASON, ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM  ");
            stb.append("     ( SELECT  ");
            stb.append("           MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("           TYPE ");
            stb.append("       FROM  ");
            stb.append("           NURSEOFF_VISITREC_DAT ");
            stb.append("       WHERE ");
            stb.append("           VISIT_DATE >= '" + param._year + "-04-01' ");
            stb.append("           AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("           AND ( VISIT_REASON1    = '02' ");
            stb.append("                 OR VISIT_REASON2 = '02' ");
            stb.append("                 OR VISIT_REASON3 = '02' )  ");
            stb.append("       GROUP BY ");
            stb.append("           SCHREGNO , ");
            stb.append("           MONTH(VISIT_DATE) , ");
            stb.append("           TYPE ");
            stb.append("     ) T1  ");
            stb.append(" GROUP BY ");
            stb.append("     T1.MONTH , ");
            stb.append("     T1.TYPE ");
            stb.append(" ), REASON03_2 AS( ");
            stb.append(" SELECT  ");
            stb.append("     T1.MONTH AS MONTH, ");
            stb.append("     T1.TYPE, ");
            stb.append("     '03'     AS VISIT_REASON, ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM  ");
            stb.append("     ( SELECT  ");
            stb.append("           MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("           TYPE ");
            stb.append("       FROM  ");
            stb.append("           NURSEOFF_VISITREC_DAT ");
            stb.append("       WHERE ");
            stb.append("           VISIT_DATE >= '" + param._year + "-04-01' ");
            stb.append("           AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("           AND ( VISIT_REASON1    = '03' ");
            stb.append("                 OR VISIT_REASON2 = '03' ");
            stb.append("                 OR VISIT_REASON3 = '03' )  ");
            stb.append("       GROUP BY ");
            stb.append("           SCHREGNO , ");
            stb.append("           MONTH(VISIT_DATE) , ");
            stb.append("           TYPE ");
            stb.append("     ) T1  ");
            stb.append(" GROUP BY ");
            stb.append("     T1.MONTH , ");
            stb.append("     T1.TYPE ");
            stb.append(" ), REASON04_2 AS( ");
            stb.append(" SELECT  ");
            stb.append("     T1.MONTH AS MONTH, ");
            stb.append("     T1.TYPE, ");
            stb.append("     '04'     AS VISIT_REASON, ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM  ");
            stb.append("     ( SELECT  ");
            stb.append("           MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("           TYPE ");
            stb.append("       FROM  ");
            stb.append("           NURSEOFF_VISITREC_DAT ");
            stb.append("       WHERE ");
            stb.append("           VISIT_DATE >= '" + param._year + "-04-01' ");
            stb.append("           AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("           AND ( VISIT_REASON1    = '04' ");
            stb.append("                 OR VISIT_REASON2 = '04' ");
            stb.append("                 OR VISIT_REASON3 = '04' )  ");
            stb.append("       GROUP BY ");
            stb.append("           SCHREGNO , ");
            stb.append("           MONTH(VISIT_DATE) , ");
            stb.append("           TYPE ");
            stb.append("     ) T1  ");
            stb.append(" GROUP BY ");
            stb.append("     T1.MONTH , ");
            stb.append("     T1.TYPE ");
            stb.append(" ), REASON05_2 AS( ");
            stb.append(" SELECT  ");
            stb.append("     T1.MONTH AS MONTH, ");
            stb.append("     T1.TYPE, ");
            stb.append("     '05'     AS VISIT_REASON, ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM  ");
            stb.append("     ( SELECT  ");
            stb.append("           MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("           TYPE ");
            stb.append("       FROM  ");
            stb.append("           NURSEOFF_VISITREC_DAT ");
            stb.append("       WHERE ");
            stb.append("           VISIT_DATE >= '" + param._year + "-04-01' ");
            stb.append("           AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("           AND ( VISIT_REASON1    = '05' ");
            stb.append("                 OR VISIT_REASON2 = '05' ");
            stb.append("                 OR VISIT_REASON3 = '05' )  ");
            stb.append("       GROUP BY ");
            stb.append("           SCHREGNO , ");
            stb.append("           MONTH(VISIT_DATE) , ");
            stb.append("           TYPE ");
            stb.append("     ) T1  ");
            stb.append(" GROUP BY ");
            stb.append("     T1.MONTH , ");
            stb.append("     T1.TYPE ");
            stb.append(" ), REASON06_2 AS( ");
            stb.append(" SELECT  ");
            stb.append("     T1.MONTH AS MONTH, ");
            stb.append("     T1.TYPE, ");
            stb.append("     '06'     AS VISIT_REASON, ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM  ");
            stb.append("     ( SELECT  ");
            stb.append("           MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("           TYPE ");
            stb.append("       FROM  ");
            stb.append("           NURSEOFF_VISITREC_DAT ");
            stb.append("       WHERE ");
            stb.append("           VISIT_DATE >= '" + param._year + "-04-01' ");
            stb.append("           AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("           AND ( VISIT_REASON1    = '06' ");
            stb.append("                 OR VISIT_REASON2 = '06' ");
            stb.append("                 OR VISIT_REASON3 = '06' )  ");
            stb.append("       GROUP BY ");
            stb.append("           SCHREGNO , ");
            stb.append("           MONTH(VISIT_DATE) , ");
            stb.append("           TYPE ");
            stb.append("     ) T1  ");
            stb.append(" GROUP BY ");
            stb.append("     T1.MONTH , ");
            stb.append("     T1.TYPE ");
            stb.append(" ), REASON07_2 AS( ");
            stb.append(" SELECT  ");
            stb.append("     T1.MONTH AS MONTH, ");
            stb.append("     T1.TYPE, ");
            stb.append("     '07'     AS VISIT_REASON, ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM  ");
            stb.append("     ( SELECT  ");
            stb.append("           MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("           TYPE ");
            stb.append("       FROM  ");
            stb.append("           NURSEOFF_VISITREC_DAT ");
            stb.append("       WHERE ");
            stb.append("           VISIT_DATE >= '" + param._year + "-04-01' ");
            stb.append("           AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("           AND ( VISIT_REASON1    = '07' ");
            stb.append("                 OR VISIT_REASON2 = '07' ");
            stb.append("                 OR VISIT_REASON3 = '07' )  ");
            stb.append("       GROUP BY ");
            stb.append("           SCHREGNO , ");
            stb.append("           MONTH(VISIT_DATE) , ");
            stb.append("           TYPE ");
            stb.append("     ) T1  ");
            stb.append(" GROUP BY ");
            stb.append("     T1.MONTH , ");
            stb.append("     T1.TYPE ");
            stb.append(" ), REASON99_2 AS( ");
            stb.append(" SELECT  ");
            stb.append("     T1.MONTH AS MONTH, ");
            stb.append("     T1.TYPE, ");
            stb.append("     '99'     AS VISIT_REASON, ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM  ");
            stb.append("     ( SELECT  ");
            stb.append("           MONTH(VISIT_DATE) AS MONTH, ");
            stb.append("           TYPE ");
            stb.append("       FROM  ");
            stb.append("           NURSEOFF_VISITREC_DAT ");
            stb.append("       WHERE ");
            stb.append("           VISIT_DATE >= '" + param._year + "-04-01' ");
            stb.append("           AND VISIT_DATE <= '" + String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31' ");
            stb.append("           AND ( VISIT_REASON1    = '99' ");
            stb.append("                 OR VISIT_REASON2 = '99' ");
            stb.append("                 OR VISIT_REASON3 = '99' )  ");
            stb.append("       GROUP BY ");
            stb.append("           SCHREGNO , ");
            stb.append("           MONTH(VISIT_DATE) , ");
            stb.append("           TYPE ");
            stb.append("     ) T1  ");
            stb.append(" GROUP BY ");
            stb.append("     T1.MONTH , ");
            stb.append("     T1.TYPE ");
            stb.append(" ), TYPE5_2 AS( ");
            stb.append(" SELECT * FROM REASON01_2 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM REASON02_2 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM REASON03_2 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM REASON04_2 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM REASON05_2 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM REASON06_2 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM REASON07_2 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM REASON99_2 ");
            stb.append(" ), TYPE5_MAIN2 AS( ");
            stb.append(" SELECT  ");
            stb.append("     'T2_' || N1.NAMECD2 AS REASON_CD, ");
            stb.append("     N1.NAME1   AS REASON_NAME, ");
            stb.append("     COALESCE(M04.CNT,0) AS M04CNT, ");
            stb.append("     COALESCE(M05.CNT,0) AS M05CNT, ");
            stb.append("     COALESCE(M06.CNT,0) AS M06CNT, ");
            stb.append("     COALESCE(M07.CNT,0) AS M07CNT, ");
            stb.append("     COALESCE(M08.CNT,0) AS M08CNT, ");
            stb.append("     COALESCE(M09.CNT,0) AS M09CNT, ");
            stb.append("     COALESCE(M10.CNT,0) AS M10CNT, ");
            stb.append("     COALESCE(M11.CNT,0) AS M11CNT, ");
            stb.append("     COALESCE(M12.CNT,0) AS M12CNT, ");
            stb.append("     COALESCE(M01.CNT,0) AS M01CNT, ");
            stb.append("     COALESCE(M02.CNT,0) AS M02CNT, ");
            stb.append("     COALESCE(M03.CNT,0) AS M03CNT, ");
            stb.append("     ( COALESCE(M04.CNT,0) + COALESCE(M05.CNT,0) + COALESCE(M06.CNT,0) +  ");
            stb.append("       COALESCE(M07.CNT,0) + COALESCE(M08.CNT,0) + COALESCE(M09.CNT,0) +  ");
            stb.append("       COALESCE(M10.CNT,0) + COALESCE(M11.CNT,0) + COALESCE(M12.CNT,0) +  ");
            stb.append("       COALESCE(M01.CNT,0) + COALESCE(M02.CNT,0) + COALESCE(M03.CNT,0) ) AS TOTAL, ");
            stb.append("     3 AS ROW ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST N1  ");
            stb.append("     LEFT JOIN TYPE5_2 M04 ON M04.VISIT_REASON = N1.NAMECD2 AND M04.TYPE = '5' AND M04.MONTH = 04 ");
            stb.append("     LEFT JOIN TYPE5_2 M05 ON M05.VISIT_REASON = N1.NAMECD2 AND M05.TYPE = '5' AND M05.MONTH = 05 ");
            stb.append("     LEFT JOIN TYPE5_2 M06 ON M06.VISIT_REASON = N1.NAMECD2 AND M06.TYPE = '5' AND M06.MONTH = 06 ");
            stb.append("     LEFT JOIN TYPE5_2 M07 ON M07.VISIT_REASON = N1.NAMECD2 AND M07.TYPE = '5' AND M07.MONTH = 07 ");
            stb.append("     LEFT JOIN TYPE5_2 M08 ON M08.VISIT_REASON = N1.NAMECD2 AND M08.TYPE = '5' AND M08.MONTH = 08 ");
            stb.append("     LEFT JOIN TYPE5_2 M09 ON M09.VISIT_REASON = N1.NAMECD2 AND M09.TYPE = '5' AND M09.MONTH = 09 ");
            stb.append("     LEFT JOIN TYPE5_2 M10 ON M10.VISIT_REASON = N1.NAMECD2 AND M10.TYPE = '5' AND M10.MONTH = 10 ");
            stb.append("     LEFT JOIN TYPE5_2 M11 ON M11.VISIT_REASON = N1.NAMECD2 AND M11.TYPE = '5' AND M11.MONTH = 11 ");
            stb.append("     LEFT JOIN TYPE5_2 M12 ON M12.VISIT_REASON = N1.NAMECD2 AND M12.TYPE = '5' AND M12.MONTH = 12 ");
            stb.append("     LEFT JOIN TYPE5_2 M01 ON M01.VISIT_REASON = N1.NAMECD2 AND M01.TYPE = '5' AND M01.MONTH = 01 ");
            stb.append("     LEFT JOIN TYPE5_2 M02 ON M02.VISIT_REASON = N1.NAMECD2 AND M02.TYPE = '5' AND M02.MONTH = 02 ");
            stb.append("     LEFT JOIN TYPE5_2 M03 ON M03.VISIT_REASON = N1.NAMECD2 AND M03.TYPE = '5' AND M03.MONTH = 03 ");
            stb.append(" WHERE ");
            stb.append("     N1.YEAR = '" + param._year + "' ");
            stb.append("     AND N1.NAMECD1 = 'F219' ");
            stb.append("     AND N1.NAMECD2 IN ('01','02','03','04','05','06','07','99') ");
            stb.append(" ),TYPE5_TOTAL2 AS( ");
            stb.append(" SELECT ");
            stb.append("     MONTH, ");
            stb.append("     TYPE, ");
            stb.append("     SUM(CNT) AS CNT ");
            stb.append(" FROM TYPE5_2 ");
            stb.append(" GROUP BY  ");
            stb.append("     MONTH, ");
            stb.append("     TYPE ");
            stb.append(" ),TYPE5_ALL_TOTAL2 AS( ");
            stb.append(" SELECT ");
            stb.append("     TYPE, ");
            stb.append("     SUM(CNT) AS CNT ");
            stb.append(" FROM TYPE5_2 ");
            stb.append(" GROUP BY  ");
            stb.append("     TYPE ");
            stb.append(" ), TYPE5_SUB2 AS( ");
            stb.append(" SELECT ");
            stb.append("     'T2_SUB'     AS REASON_CD, ");
            stb.append("     '実人数小計' AS REASON_NAME, ");
            stb.append("     COALESCE(M04.CNT,0) AS M04CNT, ");
            stb.append("     COALESCE(M05.CNT,0) AS M05CNT, ");
            stb.append("     COALESCE(M06.CNT,0) AS M06CNT, ");
            stb.append("     COALESCE(M07.CNT,0) AS M07CNT, ");
            stb.append("     COALESCE(M08.CNT,0) AS M08CNT, ");
            stb.append("     COALESCE(M09.CNT,0) AS M09CNT, ");
            stb.append("     COALESCE(M10.CNT,0) AS M10CNT, ");
            stb.append("     COALESCE(M11.CNT,0) AS M11CNT, ");
            stb.append("     COALESCE(M12.CNT,0) AS M12CNT, ");
            stb.append("     COALESCE(M01.CNT,0) AS M01CNT, ");
            stb.append("     COALESCE(M02.CNT,0) AS M02CNT, ");
            stb.append("     COALESCE(M03.CNT,0) AS M03CNT, ");
            stb.append("     COALESCE(TAT.CNT,0) AS TOTAL, ");
            stb.append("     4 AS ROW ");
            stb.append(" FROM ");
            stb.append("     (SELECT TYPE FROM TYPE5_TOTAL2 GROUP BY TYPE) T1 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL2 M04 ON T1.TYPE = M04.TYPE AND M04.MONTH = 04 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL2 M05 ON T1.TYPE = M05.TYPE AND M05.MONTH = 05 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL2 M06 ON T1.TYPE = M06.TYPE AND M06.MONTH = 06 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL2 M07 ON T1.TYPE = M07.TYPE AND M07.MONTH = 07 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL2 M08 ON T1.TYPE = M08.TYPE AND M08.MONTH = 08 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL2 M09 ON T1.TYPE = M09.TYPE AND M09.MONTH = 09 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL2 M10 ON T1.TYPE = M10.TYPE AND M10.MONTH = 10 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL2 M11 ON T1.TYPE = M11.TYPE AND M11.MONTH = 11 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL2 M12 ON T1.TYPE = M12.TYPE AND M12.MONTH = 12 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL2 M01 ON T1.TYPE = M01.TYPE AND M01.MONTH = 01 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL2 M02 ON T1.TYPE = M02.TYPE AND M02.MONTH = 02 ");
            stb.append("     LEFT JOIN TYPE5_TOTAL2 M03 ON T1.TYPE = M03.TYPE AND M03.MONTH = 03 ");
            stb.append("     LEFT JOIN TYPE5_ALL_TOTAL2 TAT ON TAT.TYPE = T1.TYPE ");
            stb.append(" WHERE ");
            stb.append("     T1.TYPE = '5' ");
            stb.append(" ), MAIN2 AS( ");
            stb.append(" SELECT * FROM TYPE5_MAIN2 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM TYPE5_SUB2 ");
            stb.append(" ORDER BY ROW, REASON_CD ");
            return stb.toString();
        }

        private static String setSqlType3(final Param param) {
            final StringBuffer stb = new StringBuffer();

            //保健室登校
            stb.append(" ), TYPE3_1 AS( ");
            stb.append(" SELECT  ");
            stb.append("     'R100'     AS REASON_CD, ");
            stb.append("     '延べ人数' AS REASON_NAME, ");
            stb.append("     COALESCE(M04.CNT,0) AS M04CNT, ");
            stb.append("     COALESCE(M05.CNT,0) AS M05CNT, ");
            stb.append("     COALESCE(M06.CNT,0) AS M06CNT, ");
            stb.append("     COALESCE(M07.CNT,0) AS M07CNT, ");
            stb.append("     COALESCE(M08.CNT,0) AS M08CNT, ");
            stb.append("     COALESCE(M09.CNT,0) AS M09CNT, ");
            stb.append("     COALESCE(M10.CNT,0) AS M10CNT, ");
            stb.append("     COALESCE(M11.CNT,0) AS M11CNT, ");
            stb.append("     COALESCE(M12.CNT,0) AS M12CNT, ");
            stb.append("     COALESCE(M01.CNT,0) AS M01CNT, ");
            stb.append("     COALESCE(M02.CNT,0) AS M02CNT, ");
            stb.append("     COALESCE(M03.CNT,0) AS M03CNT, ");
            stb.append("     ( COALESCE(M04.CNT,0) + COALESCE(M05.CNT,0) + COALESCE(M06.CNT,0) +  ");
            stb.append("       COALESCE(M07.CNT,0) + COALESCE(M08.CNT,0) + COALESCE(M09.CNT,0) +  ");
            stb.append("       COALESCE(M10.CNT,0) + COALESCE(M11.CNT,0) + COALESCE(M12.CNT,0) +  ");
            stb.append("       COALESCE(M01.CNT,0) + COALESCE(M02.CNT,0) + COALESCE(M03.CNT,0) ) AS TOTAL, ");
            stb.append("     5 AS ROW ");
            stb.append(" FROM ");
            stb.append("     (SELECT TYPE FROM REASON04_1 GROUP BY TYPE) T1 ");
            stb.append("     LEFT JOIN REASON04_1 M04 ON M04.TYPE = T1.TYPE AND M04.MONTH = 04 ");
            stb.append("     LEFT JOIN REASON04_1 M05 ON M05.TYPE = T1.TYPE AND M05.MONTH = 05 ");
            stb.append("     LEFT JOIN REASON04_1 M06 ON M06.TYPE = T1.TYPE AND M06.MONTH = 06 ");
            stb.append("     LEFT JOIN REASON04_1 M07 ON M07.TYPE = T1.TYPE AND M07.MONTH = 07 ");
            stb.append("     LEFT JOIN REASON04_1 M08 ON M08.TYPE = T1.TYPE AND M08.MONTH = 08 ");
            stb.append("     LEFT JOIN REASON04_1 M09 ON M09.TYPE = T1.TYPE AND M09.MONTH = 09 ");
            stb.append("     LEFT JOIN REASON04_1 M10 ON M10.TYPE = T1.TYPE AND M10.MONTH = 10 ");
            stb.append("     LEFT JOIN REASON04_1 M11 ON M11.TYPE = T1.TYPE AND M11.MONTH = 11 ");
            stb.append("     LEFT JOIN REASON04_1 M12 ON M12.TYPE = T1.TYPE AND M12.MONTH = 12 ");
            stb.append("     LEFT JOIN REASON04_1 M01 ON M01.TYPE = T1.TYPE AND M01.MONTH = 01 ");
            stb.append("     LEFT JOIN REASON04_1 M02 ON M02.TYPE = T1.TYPE AND M02.MONTH = 02 ");
            stb.append("     LEFT JOIN REASON04_1 M03 ON M03.TYPE = T1.TYPE AND M03.MONTH = 03 ");
            stb.append(" WHERE ");
            stb.append("     T1.TYPE = '3' ");
            stb.append(" ), TYPE3_2 AS( ");
            stb.append(" SELECT  ");
            stb.append("     'R101'     AS REASON_CD, ");
            stb.append("     '実人数' AS REASON_NAME, ");
            stb.append("     COALESCE(M04.CNT,0) AS M04CNT, ");
            stb.append("     COALESCE(M05.CNT,0) AS M05CNT, ");
            stb.append("     COALESCE(M06.CNT,0) AS M06CNT, ");
            stb.append("     COALESCE(M07.CNT,0) AS M07CNT, ");
            stb.append("     COALESCE(M08.CNT,0) AS M08CNT, ");
            stb.append("     COALESCE(M09.CNT,0) AS M09CNT, ");
            stb.append("     COALESCE(M10.CNT,0) AS M10CNT, ");
            stb.append("     COALESCE(M11.CNT,0) AS M11CNT, ");
            stb.append("     COALESCE(M12.CNT,0) AS M12CNT, ");
            stb.append("     COALESCE(M01.CNT,0) AS M01CNT, ");
            stb.append("     COALESCE(M02.CNT,0) AS M02CNT, ");
            stb.append("     COALESCE(M03.CNT,0) AS M03CNT, ");
            stb.append("     ( COALESCE(M04.CNT,0) + COALESCE(M05.CNT,0) + COALESCE(M06.CNT,0) +  ");
            stb.append("       COALESCE(M07.CNT,0) + COALESCE(M08.CNT,0) + COALESCE(M09.CNT,0) +  ");
            stb.append("       COALESCE(M10.CNT,0) + COALESCE(M11.CNT,0) + COALESCE(M12.CNT,0) +  ");
            stb.append("       COALESCE(M01.CNT,0) + COALESCE(M02.CNT,0) + COALESCE(M03.CNT,0) ) AS TOTAL, ");
            stb.append("     6 AS ROW ");
            stb.append(" FROM ");
            stb.append("     (SELECT TYPE FROM REASON04_2 GROUP BY TYPE) T1 ");
            stb.append("     LEFT JOIN REASON04_2 M04 ON M04.TYPE = T1.TYPE AND M04.MONTH = 04 ");
            stb.append("     LEFT JOIN REASON04_2 M05 ON M05.TYPE = T1.TYPE AND M05.MONTH = 05 ");
            stb.append("     LEFT JOIN REASON04_2 M06 ON M06.TYPE = T1.TYPE AND M06.MONTH = 06 ");
            stb.append("     LEFT JOIN REASON04_2 M07 ON M07.TYPE = T1.TYPE AND M07.MONTH = 07 ");
            stb.append("     LEFT JOIN REASON04_2 M08 ON M08.TYPE = T1.TYPE AND M08.MONTH = 08 ");
            stb.append("     LEFT JOIN REASON04_2 M09 ON M09.TYPE = T1.TYPE AND M09.MONTH = 09 ");
            stb.append("     LEFT JOIN REASON04_2 M10 ON M10.TYPE = T1.TYPE AND M10.MONTH = 10 ");
            stb.append("     LEFT JOIN REASON04_2 M11 ON M11.TYPE = T1.TYPE AND M11.MONTH = 11 ");
            stb.append("     LEFT JOIN REASON04_2 M12 ON M12.TYPE = T1.TYPE AND M12.MONTH = 12 ");
            stb.append("     LEFT JOIN REASON04_2 M01 ON M01.TYPE = T1.TYPE AND M01.MONTH = 01 ");
            stb.append("     LEFT JOIN REASON04_2 M02 ON M02.TYPE = T1.TYPE AND M02.MONTH = 02 ");
            stb.append("     LEFT JOIN REASON04_2 M03 ON M03.TYPE = T1.TYPE AND M03.MONTH = 03 ");
            stb.append(" WHERE ");
            stb.append("     T1.TYPE = '3' ");
            stb.append(" ), MAIN3 AS( ");
            stb.append(" SELECT * FROM TYPE3_1 ");
            stb.append(" UNION ");
            stb.append(" SELECT * FROM TYPE3_2 ");
            stb.append(" ORDER BY ROW, REASON_CD ");
            return stb.toString();
        }

    }

    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    /** パラメータ取得処理 */

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70773 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */

 private static class Param {
        final String _year;
        final String _date;
        final String _prgid;
        final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _date = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");
            _schoolName = getCertifSchoolDat(db2, "SCHOOL_NAME", "");
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field, final String blank) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT " + field + " ");
                sql.append(" FROM CERTIF_SCHOOL_DAT ");
                sql.append(" WHERE YEAR = '" + _year + "' ");
                sql.append("   AND CERTIF_KINDCD = '125' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            if (null == rtn) {
                rtn = blank;
            } else {
                int start = 0;
                for (int i = 0; i < rtn.length(); i++) {
                    if (rtn.charAt(i) != ' ' && rtn.charAt(i) != '　') {
                        break;
                    }
                    start = i + 1;
                }
                rtn = rtn.substring(start);
            }
            return rtn;
        }

    }

}

// eof
