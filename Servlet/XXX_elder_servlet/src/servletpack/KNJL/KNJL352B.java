/*
 * $Id: a4b3401f308915047a45df528a9d7562062b5410 $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３５２Ｂ＞  入学手続者集計表
 **/
public class KNJL352B {

    private static final Log log = LogFactory.getLog(KNJL352B.class);

    // 入学コース1
    private static final String PASSCOURSE_DIV_1特進 = "1"; // 特進
    private static final String PASSCOURSE_DIV_3特進選抜 = "3"; // 特進選抜

    private static final String CFLG_RECOM = "1"; // L5.推薦入試（正規）（TAKE_RECOMMEND_TEST_FLG）＝’1’
    private static final String CFLG_GENER = "2"; // L5.一般入試（正規）（TAKE_GENERAL_TEST_FLG）＝’1’
    private static final String CFLG_SINGL = "3"; // L5.一般入試（単願切換）（CHANGE_SINGLE_TEST_FLG）＝’1’
    private static final String CFLG_GENER2 = "4"; // L5.一般入試（正規）（TAKE_GENERAL_TEST_FLG）＝’1’AND (L1.PROCEDUREDIV1 IS NULL OR L1.PROCEDUREDIV1 = '1')

    private static final String ENTDIV_NULL = null; // 入学手続者
    private static final String ENTDIV1 = "1"; // 入学者
    private static final String ENTDIV2 = "2"; // 入学辞退者

    // 入学区分2
    private static final String K_特進選抜_学業特待3年 = "10010001";
    private static final String K_特進選抜_学業特待    = "10010002";
    private static final String K_特進選抜_学業特待_単願切替 = "10010003";
    private static final String K_特進選抜_学業準特待  = "10010004";
    private static final String K_特進選抜_学業準特待_単願切替 = "10010005";
    private static final String K_特進選抜_特進選抜    = "10010006";
    private static final String K_特進選抜_体育特待    = "10010007";
    private static final String K_特進選抜_体育準特待  = "10010008";
    private static final String K_特進選抜_特進選抜_単願切替 = "10010009";
    private static final String K_特進                  = "10010010";
    private static final String K_特進_体育特待        = "10010011";
    private static final String K_特進_体育準特待      = "10010012";
    private static final String K_特進_単願切替        = "10010018";
    
    private static final String SP_NULL = null; // 指定無し
    private static final String SP1 = "1";
    private static final String SP2 = "2";
    
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
    
    public void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final String form = "KNJL352B.frm";
        svf.VrSetForm(form, 4);
        
        svf.VrsOut("YEAR", _param._entexamyear); // 年度（西暦）
        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年"); // 年度（和暦）
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._date)); // 作成日
        
        // 入学手続者（試験区分別）
        svf.VrsOut("TITLE1_1", "○試験区分別入学手続者集計表");
        svf.VrEndRecord();
        svf.VrsOut("TITLE1_2", "データの個数 / 受付番号");
        svf.VrEndRecord();
        printStats(svf, -1, getStatLineGroupByCourseFlg(db2, ENTDIV_NULL), " blk1  入学手続者（試験区分別）");

        int fieldDiv;
        fieldDiv = 2;
        svf.VrsOut("TITLE2_1", "○入学コース別入学手続者");
        svf.VrEndRecord();
        svf.VrsOut("TITLE2_2", "データの個数：受付番号");
        svf.VrEndRecord();
        printStats(svf, fieldDiv, getStatLineGroupByCourseDiv(db2, fieldDiv, ENTDIV_NULL), " blk2  入学手続者（入学コース別）");
        
        svf.VrsOut("BLANK2", "1");
        svf.VrEndRecord();
        
        svf.VrsOut("TITLE2_1", "○特待生集計表");
        svf.VrEndRecord();
        printTokutai(svf, db2, fieldDiv, ENTDIV_NULL, " blk3  特待生");
        
        svf.VrsOut("BLANK2", "1");
        svf.VrEndRecord();

        svf.VrsOut("TITLE2_1", "○男女比・コース比割合");
        svf.VrEndRecord();
        printPercent(svf, db2, fieldDiv, ENTDIV_NULL, " blk4  男女比・コース比割合");

        fieldDiv = 3;
        // 入学手続者（入学辞退者を除く）
        svf.VrsOut("TITLE3_1", "○入学辞退者を除く入学コース別入学手続者集計表");
        svf.VrEndRecord();
        svf.VrsOut("TITLE3_2", "データの個数 / 受付番号");
        svf.VrEndRecord();
        printStats(svf, fieldDiv, getStatLineGroupByCourseDiv(db2, fieldDiv, ENTDIV1), " blk5  入学手続者（入学辞退者を除く）");

        // 入学辞退者
        svf.VrsOut("TITLE3_1", "〇入学辞退者数集計表");
        svf.VrEndRecord();
        svf.VrsOut("TITLE3_2", "データの個数 / 受付番号");
        svf.VrEndRecord();
        printStats(svf, fieldDiv, getStatLineGroupByCourseDiv(db2, fieldDiv, ENTDIV2), " blk6  入学辞退者");
        // 特待生（入学辞退者）
        svf.VrsOut("BLANK3", "1");
        svf.VrEndRecord();
        
        svf.VrsOut("TITLE3_1", "○入学辞退者を除く特待生集計表");
        svf.VrEndRecord();
        printTokutai(svf, db2, fieldDiv, ENTDIV2, " blk7  特待生（入学辞退者）");

        // 男女比・コース比割合（入学辞退者を除く）
        svf.VrsOut("TITLE3_1", "○男女比・コース比割合");
        svf.VrEndRecord();
        printPercent(svf, db2, fieldDiv, ENTDIV1, " blk8  男女比・コース比割合（入学辞退者を除く）");
        
        _hasData = true;
    }
    
    private Stat[] getStatLineGroupByCourseFlg(final DB2UDB db2, final String entDiv) {
        final String TESTDIV1 = "1";
        final String TESTDIV2 = "2";

        
        final Stat[] l = new Stat[25 + 1];
        l[1] = new Stat(TESTDIV1, PASSCOURSE_DIV_3特進選抜, K_特進選抜_学業特待3年, CFLG_RECOM, entDiv).setTitle1("1", "推薦", "特選選抜", "学業特待(3年)");
        l[2] = new Stat(TESTDIV1, PASSCOURSE_DIV_3特進選抜, K_特進選抜_学業特待, CFLG_RECOM, entDiv).setTitle1("1",        "",         "", "学業特待");
        l[3] = new Stat(TESTDIV1, PASSCOURSE_DIV_3特進選抜, K_特進選抜_学業準特待, CFLG_RECOM, entDiv).setTitle1("1",        "",         "", "学業準特待");
        l[4] = new Stat(TESTDIV1, PASSCOURSE_DIV_3特進選抜, K_特進選抜_体育特待, CFLG_RECOM, entDiv).setTitle1("1",        "",         "", "体育特待");
        l[5] = new Stat(TESTDIV1, PASSCOURSE_DIV_3特進選抜, K_特進選抜_体育準特待, CFLG_RECOM, entDiv).setTitle1("1",        "",         "", "体育準特待");
        l[6] = new Stat(TESTDIV1, PASSCOURSE_DIV_3特進選抜, K_特進選抜_特進選抜, CFLG_RECOM, entDiv).setTitle1("1",        "",         "", "特進選抜");

        l[8] = new Stat(TESTDIV1, PASSCOURSE_DIV_1特進, K_特進_体育特待, CFLG_RECOM, entDiv).setTitle1("2",                "",     "特進", "体育特待");
        l[9] = new Stat(TESTDIV1, PASSCOURSE_DIV_1特進, K_特進_体育準特待, CFLG_RECOM, entDiv).setTitle1("2",                "",         "", "体育準特待");
        l[10] = new Stat(TESTDIV1, PASSCOURSE_DIV_1特進, K_特進, CFLG_RECOM, entDiv).setTitle1("2",                         "",         "", "特進");

        l[13] = new Stat(TESTDIV2, PASSCOURSE_DIV_3特進選抜, K_特進選抜_学業特待3年, CFLG_GENER, entDiv).setTitle1("3",       "一般", "特選選抜", "学業特待(3年)");
        l[14] = new Stat(TESTDIV2, PASSCOURSE_DIV_3特進選抜, K_特進選抜_学業特待, CFLG_GENER, entDiv).setTitle1("3",          "",         "", "学業特待");
        l[15] = new Stat(TESTDIV2, PASSCOURSE_DIV_3特進選抜, K_特進選抜_学業準特待, CFLG_GENER, entDiv).setTitle1("3",           "",         "", "学業準特待");
        l[16] = new Stat(TESTDIV2, PASSCOURSE_DIV_3特進選抜, K_特進選抜_特進選抜, CFLG_GENER, entDiv).setTitle1("3",           "",         "", "特進選抜");
        l[17] = new Stat(TESTDIV2, PASSCOURSE_DIV_3特進選抜, K_特進選抜_学業特待_単願切替, CFLG_SINGL, entDiv).setTitle1("3", "",         "", "学業特待（単願切替）");
        l[18] = new Stat(TESTDIV2, PASSCOURSE_DIV_3特進選抜, K_特進選抜_学業準特待_単願切替, CFLG_SINGL, entDiv).setTitle1("3", "",         "", "学業準特待（単願切替）");
        l[19] = new Stat(TESTDIV2, PASSCOURSE_DIV_3特進選抜, K_特進選抜_特進選抜_単願切替, CFLG_SINGL, entDiv).setTitle1("3", "",         "", "特進選抜（単願切替）");

        l[21] = new Stat(TESTDIV2, PASSCOURSE_DIV_1特進, K_特進, CFLG_GENER, entDiv).setTitle1("4",                     "",         "特進", "特進");
        l[22] = new Stat(TESTDIV2, PASSCOURSE_DIV_1特進, K_特進_単願切替, CFLG_SINGL, entDiv).setTitle1("4",           "",             "", "特進（単願切替）");

        for (int i = 0; i < l.length; i++) {
            if (null != l[i]) l[i].setCount(db2, _param);
        }
        
        l[7] = Stat.sum(Stat.getIdx(l, new int[] {1, 2, 3, 4, 5, 6})).setTitle1Total1("特選選抜　集計");
        l[11] = Stat.sum(Stat.getIdx(l, new int[] {8, 9, 10})).setTitle1Total1("特進　集計");
        l[12] = Stat.sum(Stat.getIdx(l, new int[] {7, 11})).setTitle1Total2("推薦　集計").setAmikake();

        l[20] = Stat.sum(Stat.getIdx(l, new int[] {13, 14, 15, 16, 17, 18, 19})).setTitle1Total1("特進選抜　集計");
        l[23] = Stat.sum(Stat.getIdx(l, new int[] {21, 22})).setTitle1Total1("特進　集計");
        l[24] = Stat.sum(Stat.getIdx(l, new int[] {20, 23})).setTitle1Total2("一般　集計").setAmikake();
        
        l[25] = Stat.sum(Stat.getIdx(l, new int[] {12, 24})).setTitle1Total2("総計");
        
        return l;
    }
    
    private Stat[] getStatLineGroupByCourseDiv(final DB2UDB db2, final int fieldDiv, final String entDiv) {
        final Stat[] l = new Stat[12 + 1];
        
        l[1] = new Stat(null, PASSCOURSE_DIV_3特進選抜, K_特進選抜_学業特待3年, null, entDiv).setTitle23("1", fieldDiv, "特選選抜", "学業特待(3年)");
        l[2] = new Stat(null, PASSCOURSE_DIV_3特進選抜, K_特進選抜_学業特待, null, entDiv).setTitle23("1", fieldDiv,            "", "学業特待");
        l[3] = new Stat(null, PASSCOURSE_DIV_3特進選抜, K_特進選抜_学業準特待, null, entDiv).setTitle23("1", fieldDiv,            "", "学業準特待");
        l[4] = new Stat(null, PASSCOURSE_DIV_3特進選抜, K_特進選抜_体育特待, null, entDiv).setTitle23("1", fieldDiv,            "", "体育特待");
        l[5] = new Stat(null, PASSCOURSE_DIV_3特進選抜, K_特進選抜_体育準特待, null, entDiv).setTitle23("1", fieldDiv,            "", "体育準特待");
        l[6] = new Stat(null, PASSCOURSE_DIV_3特進選抜, K_特進選抜_特進選抜, null, entDiv).setTitle23("1", fieldDiv,            "", "特進選抜");

        l[8] = new Stat(null, PASSCOURSE_DIV_1特進, K_特進_体育特待, null, entDiv).setTitle23("2", fieldDiv,                "特進", "体育特待");
        l[9] = new Stat(null, PASSCOURSE_DIV_1特進, K_特進_体育準特待, null, entDiv).setTitle23("2", fieldDiv,                    "", "体育準特待");
        l[10] = new Stat(null, PASSCOURSE_DIV_1特進, K_特進, null, entDiv).setTitle23("2", fieldDiv,                             "", "特進");

        for (int i = 0; i < l.length; i++) {
            if (null != l[i]) l[i].setCount(db2, _param);
        }

        final Stat[] m = new Stat[12 + 1];
        m[2] = new Stat(null, PASSCOURSE_DIV_3特進選抜, K_特進選抜_学業特待_単願切替, null, entDiv);
        m[3] = new Stat(null, PASSCOURSE_DIV_3特進選抜, K_特進選抜_学業準特待_単願切替, null, entDiv);
        m[6] = new Stat(null, PASSCOURSE_DIV_3特進選抜, K_特進選抜_特進選抜_単願切替, null, entDiv);

        m[10] = new Stat(null, PASSCOURSE_DIV_1特進, K_特進_単願切替, null, entDiv);
        for (int i = 0; i < m.length; i++) {
            if (null != m[i]) {
                m[i].setCount(db2, _param);
                l[i] = Stat.sum(new Stat[] {l[i], m[i]});
            }
        }

        l[7] = Stat.sum(Stat.getIdx(l, new int[] {1, 2, 3, 4, 5, 6})).setTitle23Total1(fieldDiv, "特進選抜　集計");
        l[11] = Stat.sum(Stat.getIdx(l, new int[] {8, 9, 10})).setTitle23Total1(fieldDiv, "特進　集計");
        l[12] = Stat.sum(Stat.getIdx(l, new int[] {7, 11})).setTitle23Total1(fieldDiv, "総計");

        return l;
    }
    
    private void printStats(final Vrw32alp svf, final int fieldDiv, final Stat[] stats, final String debugTitle) {
    	log.info(debugTitle);
        for (int line = 1; line < stats.length; line++) {
        	log.info(" " + line + " = " + stats[line]._fieldFlg + ", " + stats[line]._title23_2 + ", " + stats[line]._title23_3);
        }
        for (int line = 1; line < stats.length; line++) {
            printStat(svf, fieldDiv, stats[line]);
        }
    }

    private void printStat(final Vrw32alp svf, final int fieldDiv, final Stat stat) {
    	if (Stat.FIELD_FLG_1.equals(stat._fieldFlg)) {
        	svf.VrsOut("G10_1", stat._group);
        	svf.VrsOut("G10_2", stat._group);
        	svf.VrsOut("G10_3", stat._group);
        	svf.VrsOut("G11", stat._group);
        	svf.VrsOut("G12", stat._group);
        	svf.VrsOut("G13", stat._group);

            svf.VrsOut("ENT_DIV1_1", stat._title1_1);
            svf.VrsOut("ENT_DIV1_2", stat._title1_2);
            svf.VrsOut("ENT_DIV1_3", stat._title1_3);

            svf.VrsOut("ENT_CNT_BOY1", stat._count1); // （男）
            svf.VrsOut("ENT_CNT_GIRL1", stat._count2); // （女）
            svf.VrsOut("ENT_CNT_TOTAL1", stat._countTotal); // 総計）
    	} else if (Stat.FIELD_FLG_1_TOTAL1.equals(stat._fieldFlg)) {
            svf.VrsOut("ENT_DIV1_2T", stat._title1_2);

            svf.VrsOut("ENT_CNT_BOY1T", stat._count1); // （男）
            svf.VrsOut("ENT_CNT_GIRL1T", stat._count2); // （女）
            svf.VrsOut("ENT_CNT_TOTAL1T", stat._countTotal); // 総計）
    	} else if (Stat.FIELD_FLG_1_TOTAL2.equals(stat._fieldFlg)) {
    		if (stat._isAmikake) {
                svf.VrAttribute("ENT_DIV1_1T2", "Paint=(1,60,2)");
                svf.VrAttribute("ENT_CNT_BOY1T2", "Paint=(1,60,2)");
                svf.VrAttribute("ENT_CNT_GIRL1T2", "Paint=(1,60,2)");
                svf.VrAttribute("ENT_CNT_TOTAL1T2", "Paint=(1,60,2)");
    		}
            svf.VrsOut("ENT_DIV1_1T2", stat._title1_1);

            svf.VrsOut("ENT_CNT_BOY1T2", stat._count1); // （男）
            svf.VrsOut("ENT_CNT_GIRL1T2", stat._count2); // （女）
            svf.VrsOut("ENT_CNT_TOTAL1T2", stat._countTotal); // 総計）
    		
    	} else if (Stat.FIELD_FLG_2.equals(stat._fieldFlg) || Stat.FIELD_FLG_3.equals(stat._fieldFlg)) {
        	svf.VrsOut("G" + fieldDiv + "0_1", stat._group);
        	svf.VrsOut("G" + fieldDiv + "0_2", stat._group);
        	svf.VrsOut("G" + fieldDiv + "0_3", stat._group);
        	svf.VrsOut("G" + fieldDiv + "1", stat._group);
        	svf.VrsOut("G" + fieldDiv + "2", stat._group);
        	svf.VrsOut("G" + fieldDiv + "3", stat._group);

        	svf.VrsOut("ENT_DIV" + fieldDiv + "_2", stat._title23_2);
            svf.VrsOut("ENT_DIV" + fieldDiv + "_3", stat._title23_3);

            svf.VrsOut("ENT_CNT_BOY" + fieldDiv, stat._count1); // （男）
            svf.VrsOut("ENT_CNT_GIRL" + fieldDiv, stat._count2); // （女）
            svf.VrsOut("ENT_CNT_TOTAL" + fieldDiv, stat._countTotal); // 総計）
    	} else if (Stat.FIELD_FLG_2_TOTAL1.equals(stat._fieldFlg) || Stat.FIELD_FLG_3_TOTAL1.equals(stat._fieldFlg)) {
            svf.VrsOut("ENT_DIV" + fieldDiv + "_2T", stat._title23_2);
            if (stat._titleCentering) {
            	svf.VrAttribute("ENT_DIV" + fieldDiv + "_2T", "Hensyu=3"); // CENTERING
            }

            svf.VrsOut("ENT_CNT_BOY" + fieldDiv + "T", stat._count1); // （男）
            svf.VrsOut("ENT_CNT_GIRL" + fieldDiv + "T", stat._count2); // （女）
            svf.VrsOut("ENT_CNT_TOTAL" + fieldDiv + "T", stat._countTotal); // 総計）
    	} else {
    		throw new IllegalArgumentException(" unkonwn fieldFlg : " + stat._fieldFlg);
    	}
        svf.VrEndRecord();
    }

    private void printTokutai(final Vrw32alp svf, final DB2UDB db2, final int fieldDiv, final String entDiv, final String debugTitle) {
    	log.info(debugTitle);
    	svf.VrsOut("TOTAL_HEADER" + fieldDiv + "_1", "特待区分");
    	svf.VrsOut("TOTAL_HEADER" + fieldDiv + "_2", "総計");
    	svf.VrEndRecord();
        final Stat l1 = new Stat(null, null, null, null, entDiv).setTitle23Total1(fieldDiv, "特待生（学業+体育）");
        l1._specialDiv = SP1;
        final Stat l2 = new Stat(null, null, null, null, entDiv).setTitle23Total1(fieldDiv, "準特待生（学業準+体育準）");
        l2._specialDiv = SP2;
        l1.setCount(db2, _param);
        l2.setCount(db2, _param);
        final Stat l3 = Stat.sum(new Stat[] {l1, l2}).setTitle23Total1(fieldDiv, "総計");
        l1._titleCentering = true;
        l2._titleCentering = true;
        l3._titleCentering = true;
        printStat(svf, fieldDiv, l1);
        printStat(svf, fieldDiv, l2);
        printStat(svf, fieldDiv, l3);
    }

    private void printPercent(final Vrw32alp svf, final DB2UDB db2, final int fieldDiv, final String entDiv, final String debugTitle) {
    	log.info(debugTitle);
    	svf.VrsOut("TOTAL_HEADER" + fieldDiv + "_1", "コース");
    	svf.VrsOut("TOTAL_HEADER" + fieldDiv + "_2", "ｺｰｽ比");
    	svf.VrEndRecord();
        final Stat l1 = new Stat(null, PASSCOURSE_DIV_3特進選抜, null, null, entDiv).setTitle23Total1(fieldDiv, "特進選抜");
        final Stat l2 = new Stat(null, PASSCOURSE_DIV_1特進, null, null, entDiv).setTitle23Total1(fieldDiv, "特進");
        l1.setCount(db2, _param);
        l2.setCount(db2, _param);
        final Stat l3 = Stat.sum(new Stat[] {l1, l2}).setTitle23Total1(fieldDiv, "男女比");
        l1._titleCentering = true;
        l2._titleCentering = true;
        l3._titleCentering = true;
        printStat(svf, fieldDiv, l1.getPercentage(l3._countTotal));
        printStat(svf, fieldDiv, l2.getPercentage(l3._countTotal));
        printStat(svf, fieldDiv, l3.getPercentage(l3._countTotal));
    }
    
    private static class Stat {
    	private static final String FIELD_FLG_1 = "1_1";
    	private static final String FIELD_FLG_1_TOTAL1 = "1_TOTAL1";
    	private static final String FIELD_FLG_1_TOTAL2 = "1_TOTAL2";
    	private static final String FIELD_FLG_2 = "2_1";
    	private static final String FIELD_FLG_2_TOTAL1 = "2_TOTAL1";
    	private static final String FIELD_FLG_3 = "3_1";
    	private static final String FIELD_FLG_3_TOTAL1 = "3_TOTAL1";
    	
        final String _testDiv;
        final String _passCourseDiv;
        final String _passCourseCode;
        final String _flg;
        final String _entDiv;
        String _specialDiv;
    	String _title1_1;
    	String _title1_2;
    	String _title1_3;
    	String _title23_2;
    	String _title23_3;
    	boolean _isAmikake;
        String _count1;
        String _count2;
        String _countTotal;
        String _fieldFlg;
        String _group;
        boolean _titleCentering;
        Stat(final String testDiv, final String passCourseDiv, final String passCourseCode, final String flg, final String entDiv) {
            _testDiv = testDiv;
            _passCourseDiv = passCourseDiv;
            _passCourseCode = passCourseCode;
            _flg = flg;
            _entDiv = entDiv;
        }
        
        Stat(final Stat stat) {
        	this(stat._testDiv, stat._passCourseDiv, stat._passCourseCode, stat._flg, stat._entDiv);
        	_specialDiv = stat._specialDiv;
        	_title1_1 = stat._title1_1;
        	_title1_2 = stat._title1_2;
        	_title1_3 = stat._title1_3;
        	_title23_2 = stat._title23_2;
        	_title23_3 = stat._title23_3;
        	_isAmikake = stat._isAmikake;
        	_fieldFlg = stat._fieldFlg;
        	_titleCentering = stat._titleCentering;
        	_group = stat._group;
        }

        public Stat setTitle1(final String group, final String title1, final String title2, final String title3) {
        	_group = group;
        	_fieldFlg = FIELD_FLG_1;
        	_title1_1 = title1;
        	_title1_2 = title2;
        	_title1_3 = title3;
        	_isAmikake = false;
        	return this;
        }
        
        public Stat setTitle1Total1(final String title2) {
        	_fieldFlg = FIELD_FLG_1_TOTAL1;
        	_title1_1 = null;
        	_title1_2 = title2;
        	_title1_3 = null;
        	_isAmikake = false;
        	return this;
        }
        
        public Stat setTitle1Total2(final String title1) {
        	_fieldFlg = FIELD_FLG_1_TOTAL2;
        	_title1_1 = title1;
        	_title1_2 = null;
        	_title1_3 = null;
        	_isAmikake = false;
        	return this;
        }
        
        public Stat setAmikake() {
        	_isAmikake = true;
			return this;
		}

        public Stat setTitle23(final String group, final int fielddiv23, final String title2, final String title3) {
        	if (fielddiv23 == 2) {
            	_fieldFlg = FIELD_FLG_2;
        	} else if (fielddiv23 == 3) {
            	_fieldFlg = FIELD_FLG_3;
        	} else {
        		throw new IllegalArgumentException("fieldDiv23 should be 2 or 3 : " + fielddiv23);
        	}
        	_group = group;
        	_title23_2 = title2;
        	_title23_3 = title3;
        	return this;
        }
        
        public Stat setTitle23Total1(final int fielddiv23, final String title2) {
        	if (fielddiv23 == 2) {
            	_fieldFlg = FIELD_FLG_2_TOTAL1;
        	} else if (fielddiv23 == 3) {
            	_fieldFlg = FIELD_FLG_3_TOTAL1;
        	} else {
        		throw new IllegalArgumentException("fieldDiv23 should be 2 or 3 : " + fielddiv23);
        	}
        	_title23_2 = title2;
        	_title23_3 = null;
        	return this;
        }
        
        public Stat getPercentage(final String countTotalAll) {
            final Stat dummy = new Stat(this);
            dummy._count1 = percentage(_count1, _countTotal);
            dummy._count2 = percentage(_count2, _countTotal);
            dummy._countTotal = percentage(_countTotal, countTotalAll);
            return dummy; 
        }
        
        public static Stat[] getIdx(final Stat[] stats, final int[] idxs) {
            final Stat[] rtn = new Stat[idxs.length];
            for (int i = 0; i < idxs.length; i++) {
                rtn[i] = stats[idxs[i]]; 
            }
            return rtn;
        }

        public static Stat sum(final Stat[] stats) {
            final Stat dummy = new Stat(stats[0]);
            for (int i = 0; i < stats.length; i++) {
                final Stat s = stats[i];
                dummy._count1 = add(dummy._count1, s._count1);
                dummy._count2 = add(dummy._count2, s._count2);
                dummy._countTotal = add(dummy._countTotal, s._countTotal);
            }
            return dummy;
        }

        public static String add(final String a, final String b) {
            if (!NumberUtils.isDigits(a) && !NumberUtils.isDigits(b)) {
                return null;
            }
            return String.valueOf(Integer.parseInt(StringUtils.defaultString(a, "0")) + Integer.parseInt(StringUtils.defaultString(b, "0")));
        }
        
        public static String percentage(final String bunshi, final String bunbo) {
            if (!NumberUtils.isDigits(bunshi) || !NumberUtils.isDigits(bunbo) || 0 == Integer.parseInt(bunbo)) {
                return "0.0%";
            }
            return new BigDecimal(bunshi).multiply(new BigDecimal(100)).divide(new BigDecimal(bunbo), 1, BigDecimal.ROUND_HALF_UP).toString() + "%";
        }
        
        public void setCount(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.info("  sql (testDiv = " + _testDiv + ", entDiv = " + _entDiv + ", passCourseCode = " + _passCourseCode + ", passCourseDiv = " + _passCourseDiv + ", specialDiv = " + _specialDiv + ", flg = " + _flg + ") ");
                if (param._isOutputDebug) {
                	log.info(" " + sql);
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (0 == rs.getInt("COUNT_TOTAL")) {
                        continue;
                    }
                    _count1 = rs.getString("COUNT1");
                    _count2 = rs.getString("COUNT2");
                    _countTotal = rs.getString("COUNT_TOTAL");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH COUNTS AS ( ");
            stb.append(" SELECT ");
            stb.append("     VALUE(T1.SEX, '9') AS SEX, ");
            stb.append("     COUNT(*) AS COUNT ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("     INNER JOIN ENTEXAM_COURSE_JUDGMENT_MST T5 ON T5.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("                                    AND L1.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("                                    AND L1.TESTDIV = T1.TESTDIV ");
            stb.append("                                    AND L1.EXAM_TYPE = '1' ");
            stb.append("                                    AND L1.EXAMNO = T1.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            if (null != _testDiv) {
                stb.append("     AND T1.TESTDIV = '" + _testDiv + "' ");
            }
            stb.append("     AND T1.JUDGEMENT = '1' ");
            if (null == _entDiv) {
                stb.append("     AND T1.PROCEDUREDIV = '1' ");
            } else {
                stb.append("     AND T1.ENTDIV = '" + _entDiv + "' ");
            }
            if (null != _passCourseCode) {
                stb.append("     AND T1.SUC_COURSECD || T1.SUC_MAJORCD || T1.SUC_COURSECODE = '" + _passCourseCode + "' ");
            }
            if (null != _passCourseDiv) {
                stb.append("     AND T5.PASSCOURSE_DIV = '" + _passCourseDiv + "' ");
            }
            if (null != _specialDiv) {
                stb.append("     AND T5.SPECIAL_DIV = '" + _specialDiv + "' ");
            }
            stb.append("     AND (");

            String or = null; // _flg が null の場合OR選択
            if (CFLG_RECOM.equals(_flg) || null == _flg) {
                stb.append(StringUtils.defaultString(or));
                stb.append("    (T1.TESTDIV = '1' AND T5.TAKE_RECOMMEND_TEST_FLG = '1' AND L1.PROCEDUREDIV1 IS NULL ");
                stb.append("     AND T5.NORMAL_PASSCOURSECD = T1.SUC_COURSECD ");
                stb.append("     AND T5.NORMAL_PASSMAJORCD = T1.SUC_MAJORCD ");
                stb.append("     AND T5.NORMAL_PASSEXAMCOURSECD = T1.SUC_COURSECODE) ");
                or = " OR ";
            }
            if (CFLG_GENER.equals(_flg) || null == _flg) {
                stb.append(StringUtils.defaultString(or));
                stb.append("    (T1.TESTDIV = '2' AND T5.TAKE_GENERAL_TEST_FLG = '1' AND L1.PROCEDUREDIV1 IS NULL ");
                stb.append("     AND T5.NORMAL_PASSCOURSECD = T1.SUC_COURSECD ");
                stb.append("     AND T5.NORMAL_PASSMAJORCD = T1.SUC_MAJORCD ");
                stb.append("     AND T5.NORMAL_PASSEXAMCOURSECD = T1.SUC_COURSECODE) ");
                or = " OR ";
            }
            if (CFLG_SINGL.equals(_flg) || null == _flg) {
                stb.append(StringUtils.defaultString(or));
                stb.append("    (T1.TESTDIV = '2' AND T5.CHANGE_SINGLE_TEST_FLG = '1' AND L1.PROCEDUREDIV1 = '1' ");
                stb.append("     AND T5.EARLY_PASSCOURSECD = T1.SUC_COURSECD ");
                stb.append("     AND T5.EARLY_PASSMAJORCD = T1.SUC_MAJORCD ");
                stb.append("     AND T5.EARLY_PASSEXAMCOURSECD = T1.SUC_COURSECODE) ");
                or = " OR ";
            }
            if (CFLG_GENER2.equals(_flg) || null == _flg) {
                stb.append(StringUtils.defaultString(or));
                stb.append("    (T1.TESTDIV = '2' AND T5.TAKE_GENERAL_TEST_FLG = '1' AND (L1.PROCEDUREDIV1 IS NULL OR L1.PROCEDUREDIV1 = '1') ");
                stb.append("     AND T5.NORMAL_PASSCOURSECD = T1.SUC_COURSECD ");
                stb.append("     AND T5.NORMAL_PASSMAJORCD = T1.SUC_MAJORCD ");
                stb.append("     AND T5.NORMAL_PASSEXAMCOURSECD = T1.SUC_COURSECODE) ");
                or = " OR ";
            }
            stb.append("     ) ");
            stb.append(" GROUP BY ");
            stb.append("     GROUPING SETS ((SEX), ()) ");
            stb.append(" ) ");
            stb.append(" SELECT VALUE(L1.COUNT, 0) AS COUNT1, VALUE(L2.COUNT, 0) AS COUNT2, VALUE(T1.COUNT, 0) AS COUNT_TOTAL ");
            stb.append(" FROM ");
            stb.append("     COUNTS T1 ");
            stb.append("     LEFT JOIN COUNTS L1 ON L1.SEX = '1' ");
            stb.append("     LEFT JOIN COUNTS L2 ON L2.SEX = '2' ");
            stb.append(" WHERE ");
            stb.append("     T1.SEX = '9' ");
            return stb.toString();
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70811 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _date;
        final boolean _isOutputDebug;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _date = request.getParameter("CTRL_DATE");
            _isOutputDebug = "1".equals(KnjDbUtils.getDbPrginfoProperties(db2, "KNJL352B", "outputDebug"));
        }
    }
}

// eof

