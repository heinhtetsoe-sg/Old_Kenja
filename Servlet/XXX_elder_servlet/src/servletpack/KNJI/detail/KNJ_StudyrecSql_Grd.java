// kanji=漢字
/*
 * $Id: ffb97d88abb2aa8eeaf9e7c5d8d16011d2f79386 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJI.detail;

import servletpack.KNJE.detail.KNJ_StudyrecSql;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 *  [進路情報・調査書]学習記録データSQL作成(卒業生用)
 *
 *  2005/07/25 yamashiro・在校生用と卒業生用のSQLを共通化するため作成
 */

public class KNJ_StudyrecSql_Grd extends KNJ_StudyrecSql
{
    private static final Log log = LogFactory.getLog(KNJ_StudyrecSql_Grd.class);

    /**
     * @deprecated
     */
    public KNJ_StudyrecSql_Grd( String hyoutei, boolean english) {
        this(hyoutei, english, null);
    }

    public KNJ_StudyrecSql_Grd( String hyoutei, boolean english, final String useCurriculumcd) {
        super(hyoutei, english, useCurriculumcd);
        super._config.put(CONFIG_PRINT_GRD, "1");
    }

    /**
     * @deprecated
     */
    public KNJ_StudyrecSql_Grd(String hyoutei, String atype, int stype, boolean english, boolean isHosei){
        this(hyoutei, atype, stype, english, isHosei, false, null);
    }

    /**
     * @deprecated
     */
    public KNJ_StudyrecSql_Grd(String hyoutei, String atype, int stype, boolean english, boolean isHosei, final String useCurriculumcd){
        this(hyoutei, atype, stype, english, isHosei, false, useCurriculumcd);
    }

    /**
     * @deprecated
     */
    public KNJ_StudyrecSql_Grd(String hyoutei, String atype, int stype, boolean english, boolean isHosei, boolean isNotPrintMirishu){
        this(hyoutei, atype, stype, english, isHosei, isNotPrintMirishu, null);
    }

    public KNJ_StudyrecSql_Grd(String hyoutei, String atype, int stype, boolean english, boolean isHosei, boolean isNotPrintMirishu, final String useCurriculumcd){
        super(hyoutei, atype, stype, english, isHosei, isNotPrintMirishu, useCurriculumcd);
        super._config.put(CONFIG_PRINT_GRD, "1");
        log.debug(" $Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $");
    }

    /**
     *  在校生用と卒業生用のテーブル名を設定
     *  2005/07/25 Build 在校生用と卒業生用で共有する
     */
    public void setFieldName()
    {
        tname1 = "GRD_STUDYREC_DAT";
        tname2 = "GRD_TRANSFER_DAT";
        tname3 = "GRD_REGD_DAT";
    }
}
