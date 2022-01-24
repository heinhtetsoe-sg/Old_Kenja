// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2005/08/09
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJZ.detail;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;

/**
 *  日付・時刻編集
 */
/*  2005/08/09 yamashiro システム日付＆時刻を返すメソッドgetNowDateWaを追加
 *  2005/10/24 yamashiro 英国式追加
 */

public class KNJ_EditDate {

    private static final String revision = "$Revision: 71919 $ $Date: 2020-01-23 09:50:11 +0900 (木, 23 1 2020) $";

    private static final Log log = LogFactory.getLog(KNJ_EditDate.class);

    private static String patternYyyyMMddHyphen = "yyyy-MM-dd";
    private static String patternYyyyMMddSlash = "yyyy/MM/dd";

    public static class L007Class {
        private final String _seireki;
        private final String _name1;
        private final String _name2;
        private final String _name3;
        private final String _abbv1;
        private final String _startYear;
        private final String _endYear;
        private final int _calcNen;

        public L007Class (final String seireki, final String name1, final String name2, final String name3, final String abbv1, final String startYear, final String endYear) {
            _seireki = seireki;
            _name1 = name1;
            _name2 = name2;
            _name3 = name3;
            _abbv1 = abbv1;
            _startYear = startYear;
            _endYear = endYear;
            _calcNen = Integer.parseInt(seireki) - Integer.parseInt(startYear) + 1;
        }

        public String getSeireki() {
            return _seireki;
        }

        public String getName1() {
            return _name1;
        }

        public String getName2() {
            return _name2;
        }

        public String getName3() {
            return _name3;
        }

        public String getAbbv1() {
            return _abbv1;
        }

        public String getStartYear() {
            return _startYear;
        }

        public String getEndYear() {
            return _endYear;
        }

        public int calcNen() {
            return _calcNen;
        }
    }

    String pdate, phour, pminute, psecond;

    public KNJ_EditDate() {}

    public KNJ_EditDate(String pdate) {
        this.pdate = pdate;
        this.phour = null;
        this.pminute = null;
        this.psecond = null;
    }

    public KNJ_EditDate(String pdate,String phour,String pminute) {
        this.pdate = pdate;
        this.phour = phour;
        this.pminute = pminute;
        this.psecond = "00";
    }

    public KNJ_EditDate(String pdate,String phour,String pminute,String psecond) {
        this.pdate = pdate;
        this.phour = phour;
        this.pminute = pminute;
        this.psecond = psecond;
    }

    /*--------------*
     * 西暦判定     *
     *--------------*/
    public static boolean isSeireki(final DB2UDB db2) {
        return "2".equals(KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00'")), "NAME1"));
    }

    /*----------------*
     * 日付の編集 	  *
     *----------------*/
    public String h_format() {
        StringBuffer sbx = new StringBuffer();

        //日付
        sbx.append(pdate);
        sbx.replace(4,5,"-");
        sbx.replace(7,8,"-");

        //時刻
        if(phour != null){
            sbx.append("-");
            sbx.append(phour);
            sbx.append(".");
            sbx.append(pminute);
            sbx.append(".00.000000");
        }

        return sbx.toString();

    }//h_formatの括り


    private static Locale localja_JP() {
        return new Locale("ja","JP");
    }

    private static Locale localen_US() {
        return new Locale("en","US");
    }

    private static Date parseDate(final String pattern, final String strx) throws ParseException {
        Date dat = null;
        if (null != strx) {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.setLenient(true);
            sdf.applyPattern(pattern);
            dat = sdf.parse(strx);
        }
        return dat;
    }


    /**
     * 日付の編集 2003/07/22 	yyyy/m/d --> yyyy-mm-dd
     * @deprecated
     * 注）static String H_Format_Haifun(String)を使用してください。
     **/
    public String h_format_sec(String strx) {
        return H_Format_Haifun(strx);
    }//h_format_secの括り

    /*---------------------------------------------------*
     * 日付の編集 yyyy/m/d の形式を yyyy-mm-dd へ編集
     * 2004/07/16作成
     *---------------------------------------------------*/
    public static String H_Format_Haifun(String strx) {

        String stry = null;

        try {
            Date date = parseDate("yyyy/M/d", strx);
            if (null != date) {
                stry = new SimpleDateFormat(patternYyyyMMddHyphen).format( date );
            }
        } catch (ParseException e) {
            System.out.println( "[EditDate] err = " + e.getMessage() );
        }
        return stry;

    }//static String H_Format_Haifun()の括り



    /*---------------------------------------------------*
     * 日付の編集  	yyyy-m-d --> yy/mm/dd
     *	04/07/20 編集をyyyy/mm/ddへ変更 KNJD140で使用
     *---------------------------------------------------*/
    public static String h_format_thi(String strx,int h_code) {

        String stry = "";
        try {
            Date date = parseDate(patternYyyyMMddHyphen, strx);
            if (null != date) {
                final String pat;
                if (h_code == 0) {
                    pat = patternYyyyMMddSlash;
                } else {
                    pat = "yy/MM/dd";
                }
                stry = new SimpleDateFormat(pat).format( date );
            }
        } catch (ParseException e) {
            System.out.println( "[EditDate]h_format_thi err = " + e.getMessage() );
        }
        return stry;

    }//h_format_thiの括り



    /*----------------------------------------------------------------------*
     * ＴＩＭＥＳＴＡＭＰの編集  	yyyy/MM/dd,HH,mm --> yy-MM-dd-HH.mm.ss
     *----------------------------------------------------------------------*/
    public static String h_format_tims(String pdate,String phh,String pmm) {

        if( phh==null )	phh = "00";
        if( pmm==null )	pmm = "00";
        pdate = pdate+"."+phh+"."+pmm;

        SimpleDateFormat sdf = new SimpleDateFormat();
        Date dat = new Date();
        try {
            sdf.applyPattern("yyyy-MM-dd.HH.mm");
            dat = sdf.parse( pdate );
        } catch (Exception e) {
            try {
                sdf.applyPattern("yyyy/MM/dd.HH.mm");
                dat = sdf.parse( pdate );
            } catch (Exception e2) {
                return "";
            }
        }

        String ptime = "";
        try {
            ptime = new SimpleDateFormat("yyyy-MM-dd-HH.mm.00", localja_JP()).format( dat );
        } catch (Exception e3) {
        }

        return ptime;

    }//h_format_timesの括り


    /**---------------------------------------------------------------------------------------------*
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP("2002-10-27")     :平成14年10月27日
     *   String dat = h_format_JP("2002/10/27")     :平成14年10月27日
     *----------------------------------------------------------------------------------------------
     *@deprecated use {@link #h_format_JP(DB2UDB db2, String strx)}
     **/
    public static String h_format_JP(String strx) {

        String hdate = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern(patternYyyyMMddHyphen);
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern(patternYyyyMMddSlash);
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    hdate = "";
                    return hdate;
                }
            }
            Calendar cal = new GregorianCalendar(localja_JP());
            cal.setTime(dat);
            hdate = KenjaProperties.gengou(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DATE));
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_JPの括り



    /**---------------------------------------------------------------------------------------------*
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP_M("2002-10-27")		:平成14年10月
     *   String dat = h_format_JP_M("2002/10/27")		:平成14年10月
     *----------------------------------------------------------------------------------------------
     *@deprecated use {@link #h_format_JP_M(DB2UDB db2, String strx)}
     **/
    public static String h_format_JP_M(String strx) {

        String hdate = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern(patternYyyyMMddHyphen);
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern(patternYyyyMMddSlash);
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    try {										//applyPatternを追加 2003/12/15
                        sdf.applyPattern("yyyyMMdd");
                        dat = sdf.parse(strx);
                    } catch (Exception e3) {
                        hdate = "";
                        return hdate;
                    }
                }
            }
            Calendar cal = new GregorianCalendar(localja_JP());
            cal.setTime(dat);
            String stra = KenjaProperties.gengou(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DATE));
            int ia = stra.indexOf('月');
            if( ia>=0 )		hdate = stra.substring(0,ia+1);
            else			hdate = "";
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_JP_Mの括り



    /**---------------------------------------------------------------------------------------------*
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP_N("2002-10-27")		:平成14年
     *   String dat = h_format_JP_N("2002/10/27")		:平成14年
     *----------------------------------------------------------------------------------------------
     *@deprecated use {@link #h_format_JP_N(DB2UDB db2, String strx)}
     **/
    public static String h_format_JP_N(String strx) {

        String hdate = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern(patternYyyyMMddHyphen);
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern(patternYyyyMMddSlash);
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    hdate = "";
                    return hdate;
                }
            }
            Calendar cal = new GregorianCalendar(localja_JP());
            cal.setTime(dat);
            String stra = KenjaProperties.gengou(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DATE));
            int ia = stra.indexOf('年');
            if( ia>=0 )		hdate = stra.substring(0,ia+1);
            else			hdate = "";
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_JP_Nの括り



    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP("2002-10-27")		:10月27日
     *   String dat = h_format_JP("2002/10/27")		:10月27日
     *----------------------------------------------------------------------------------------------*/
    public static String h_format_JP_MD(String strx) {

        String hdate = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern(patternYyyyMMddHyphen);
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern(patternYyyyMMddSlash);
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    hdate = "";
                    return hdate;
                }
            }
            hdate = new SimpleDateFormat("M月d日", localja_JP()).format( dat );
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_JP_MDの括り



    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(米国式)
     * ※使い方
     *   String dat = h_format_US("2002-10-27")		:Apr 8,2002
     *   String dat = h_format_US("2002/10/27")		:Apr 8,2002
     *----------------------------------------------------------------------------------------------*/
    public static String h_format_US(String strx) {

        String hdate = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern(patternYyyyMMddHyphen);
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern(patternYyyyMMddSlash);
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    hdate = "";
                    return hdate;
                }
            }
            hdate = new SimpleDateFormat("d,MMM,yyyy", localen_US()).format( dat );
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_US_Mの括り


    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(英国式)   05/10/24 Build
     * ※使い方
     *   String dat = h_format_US("2002-10-27")		:Apr 8,2002
     *   String dat = h_format_US("2002/10/27")		:Apr 8,2002
     *----------------------------------------------------------------------------------------------*/
    public static String h_format_UK( String strx, String sf ) {

        String hdate = "";
        if (sf == null) {
            sf = "MMM";
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern(patternYyyyMMddHyphen);
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern(patternYyyyMMddSlash);
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    hdate = "";
                    return hdate;
                }
            }
            hdate = new SimpleDateFormat( sf + " d,yyyy", localen_US()).format( dat );
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_US_Mの括り


    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(米国式)
     * ※使い方
     *   String dat = h_format_US("2002-10-27")		:Apr 2002
     *   String dat = h_format_US("2002/10/27")		:Apr 2002
     *----------------------------------------------------------------------------------------------*/
    public static String h_format_US_M(String strx) {

        String hdate = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern(patternYyyyMMddHyphen);
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern(patternYyyyMMddSlash);
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    hdate = "";
                    return hdate;
                }
            }
            hdate = new SimpleDateFormat("MMM,yyyy", localen_US()).format( dat );
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_US_Mの括り



    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(米国式)
     * ※使い方
     *   String dat = h_format_US_Y("2002-10-27")		:2002
     *   String dat = h_format_US_Y("2002/10/27")		:2002
     *----------------------------------------------------------------------------------------------*/
    public static String h_format_US_Y(String strx) {

        String hdate = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern(patternYyyyMMddHyphen);
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern(patternYyyyMMddSlash);
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    hdate = "";
                    return hdate;
                }
            }
            hdate = new SimpleDateFormat("yyyy", localen_US()).format( dat );
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_US_Yの括り



    /**----------------------------------------------------------------------------------------------*
     * 日付の編集(汎用)
     * ※使い方
     *   String dat = h_format("2002-10-27","yyyy-MM-dd","年M月d日","ja","JP")		:平成14年10月27日
     *   String dat = h_format("2002/03/31","yyyy/MM/dd","年M月","ja","JP")			:平成14年3月
     *   String dat = h_format("2002-04-08","yyyy-MM-dd",MMM d,yyyy,"en","US")		:Apr 8,2002
     *----------------------------------------------------------------------------------------------
     *@deprecated use {@link #h_format(DB2UDB db2, String strx, String pat, String fmt, String lag, String cnt)}
     **/
    public static String h_format(String strx,String pat,String fmt,String lag,String cnt) {

        String stry = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern( pat );
            Date dat = sdf.parse(strx);
            Locale local = new Locale(lag,cnt);
            String stra = new SimpleDateFormat(fmt,local).format( dat );
            if( cnt.equals("JP") ){
                Calendar cal = new GregorianCalendar(local);
                cal.setTime(dat);
                String strb = KenjaProperties.gengou(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DATE));
                int ia = stra.indexOf('年');
                if( ia>=0 )		stry = strb.substring(0,ia)+strb;
                else			stry = "";

                stry = strb + stra;
            }else
                stry = stra;
        } catch (ParseException e) {
            System.out.println( "[EditDate]h_format err = " + e.getMessage() );
            stry = "";
        }

        return stry;

    }//String h_formatの括り

    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP("2016-10-27")     :2016年10月27日
     *   String dat = h_format_JP("2016/10/27")     :2016年10月27日
     *----------------------------------------------------------------------------------------------*/
    public static String h_format_SeirekiJP(final String date) {
        if (null == date) {
            return date;
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        String retVal = "";
        sdf.applyPattern("yyyy年M月d日");
        retVal = sdf.format(java.sql.Date.valueOf(StringUtils.replace(date, "/", "-")));

        return retVal;
    }

    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(曜日の取得)
     * ※使い方
     *----------------------------------------------------------------------------------------------*/
    public static String h_format_W(String strx) {

        String hweek = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern(patternYyyyMMddHyphen);
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern(patternYyyyMMddSlash);
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    hweek = "";
                    return hweek;
                }
            }
            Calendar cal = new GregorianCalendar(localja_JP());
            cal.setTime(dat);
            int week_day = cal.get(Calendar.DAY_OF_WEEK);
            switch (week_day){
            case 1:		hweek = "日";	break;
            case 2:		hweek = "月";	break;
            case 3:		hweek = "火";	break;
            case 4:		hweek = "水";	break;
            case 5:		hweek = "木";	break;
            case 6:		hweek = "金";	break;
            case 7:		hweek = "土";	break;
            default:	hweek = "";		break;
            }

        } catch (Exception e3) {
            hweek = "";
        }

        return hweek;

    }//String h_formatの括り



    /**----------------------------------------------------------------------------------------------*
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP_F("2003-01-01")		:平成15年1月1日生
     *   String dat = h_format_JP_F(null)				:
     *----------------------------------------------------------------------------------------------
     *@deprecated use {@link #h_format_JP_Bth(DB2UDB db2, String strx)}
     **/
    public static String h_format_JP_Bth(String strx) {

        String hdate = "";
        try {
            if (strx != null) {
                hdate = h_format_JP(strx);
                if (hdate.length() > 0) {
                    hdate = hdate + "生";
                }
            }
        } catch (Exception e3) {
        }
        return hdate;

    }//String h_format_JP_Fの括り



    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP_S("2002-10-27","M")		:10
     *   String dat = h_format_JP_S("2002/10/27","d")		:27
     *----------------------------------------------------------------------------------------------*/
    public static String h_format_S(String strx,String pat) {

        String hdate = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern(patternYyyyMMddHyphen);
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern(patternYyyyMMddSlash);
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    hdate = "";
                    return hdate;
                }
            }
            hdate = new SimpleDateFormat(pat, localja_JP()).format( dat );
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_Sの括り



    /*---------------------------------------------------*
     * 日付の編集 2003/07/22 	yyyy/m/d --> yyyy-mm-dd  *
     *---------------------------------------------------*/
    public static String h_format_sec_2(String strx) {

        String stry = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.setLenient( true );
            sdf.applyPattern( "yyyy/M/d" );
            Date dat = sdf.parse(strx);
            stry = new SimpleDateFormat(patternYyyyMMddHyphen).format( dat );
        } catch (ParseException e) {
            System.out.println( "[EditDate] err = " + e.getMessage() );
        }

        return stry;

    }//h_format_secの括り



    /*---------------------------------------------------*
     *	年度算出
     *			2003/04/30 --> 2003
     *			2004/03/01 --> 2003
     *---------------------------------------------------*/
    public static String b_year(String strx) {

        String byear = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern(patternYyyyMMddHyphen);
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern(patternYyyyMMddSlash);
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    byear = "";
                    return byear;
                }
            }
            Calendar cal = new GregorianCalendar(localja_JP());
            cal.setTime(dat);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            if (month < 3) {
                year -= 1;
            }
            byear = String.valueOf(year);
        } catch (Exception e3) {
            byear = "";
        }
        return byear;

    }//b_yearの括り



    /*-----------------------------------------------------------------------------------------------
     *	日付変換
     *		平成15年12月20日 --> {"平成","15","12","20"}
     *-----------------------------------------------------------------------------------------------*/
    public static String[] tate_format(final String hdate) {

        final String arr_adate[] = new String[4];
        try {
            boolean isDigitBefore = false;  //数値？
            int ib = 0;					//使用済み配列要素
            int ic = 0;					//開始位置
            for (int i = 0; i < hdate.length(); i++) {
                final char ch = hdate.charAt(i);
                final boolean isDigit = Character.isDigit(ch) || ch == '元';
                if ((isDigit && !isDigitBefore) || (!isDigit && isDigitBefore)) {
                    switch (ch) {
                    case '年':
                        arr_adate[1] = hdate.substring(ic, i);
                        ib = 1;
                        ic = i+1;
                        break;
                    case '月':
                        arr_adate[2] = hdate.substring(ic, i);
                        ib = 2;
                        ic = i+1;
                        break;
                    case '日':
                        arr_adate[3] = hdate.substring(ic, i);
                        ib = 3;
                        ic = i+1;
                        break;
                    default:
                        if (ib == 0) {
                            arr_adate[0] = hdate.substring(ic, i);
                            ic = i;
                        }
                        break;
                    }
                }
                isDigitBefore = isDigit;
            }
            System.out.println("[EditDate]tate_format hdate=" + hdate);
            System.out.println("[EditDate]tate_format hdate=" + arr_adate[0] + arr_adate[1] + " 年 " + arr_adate[2] + " 月 " + arr_adate[3] + " 日");
        } catch (Exception e) {
            System.out.println("h_format error!");
            System.out.println(e);
        }//try-catchの括り

        return arr_adate;

    }//tate_formatの括り



    /*-----------------------------------------------------------------------------------------------
     *	日付変換
     *		平成15年12月20日 --> {"平成","15","年","12","月","20","日"}
     *-----------------------------------------------------------------------------------------------*/
    public static String[] tate2_format(final String hdate) {

        final String[] arr_adate = new String[7];
        try {
            boolean isDigitBefore = false;  //数値？
            int ib = 0;					//使用済み配列要素
            int ic = 0;					//開始位置
            for (int i = 0; i < hdate.length(); i++) {
                final char ch = hdate.charAt(i);
                final boolean isDigit = Character.isDigit(ch) || ch == '元';
                if ((isDigit && !isDigitBefore) || (!isDigit && isDigitBefore)) {
                    switch (ch) {
                    case '年':
                        arr_adate[1] = hdate.substring(ic, i);
                        arr_adate[2] = "年";
                        ib = 2;
                        ic = i+1;
                        break;
                    case '月':
                        arr_adate[3] = hdate.substring(ic, i);
                        arr_adate[4] = "月";
                        ib = 4;
                        ic = i+1;
                        break;
                    case '日':
                        arr_adate[5] = hdate.substring(ic, i);
                        arr_adate[6] = "日";
                        ib = 6;
                        ic = i+1;
                        break;
                    default:
                        if (ib == 0) {
                            arr_adate[0] = hdate.substring(ic, i);
                            ic = i;
                        }
                        break;
                    }
                }
                isDigitBefore = isDigit;
            }
        } catch (Exception e) {
            System.out.println("h_format error!");
            System.out.println(e);
        }//try-catchの括り

        return arr_adate;

    }//tate_formatの括り



    /*-----------------------------------------------------------------------------------------------
     *	日付変換
     *		平成15年12月20日 --> {"平","15","年","12","月","20","日"}
     *-----------------------------------------------------------------------------------------------*/
    public static String[] tate3_format(final String hdate) {

        final String[] arr_adate = new String[7];
        try {
            boolean isDigitBefore = false;  //数値？
            int ib = 0;					//使用済み配列要素
            int ic = 0;					//開始位置
            for (int i = 0; i < hdate.length(); i++) {
                final char ch = hdate.charAt(i);
                final boolean isDigit = Character.isDigit(ch) || ch == '元';
                if ((isDigit && !isDigitBefore) || (!isDigit && isDigitBefore)) {
                    switch (ch) {
                    case '年':
                        arr_adate[1] = hdate.substring(ic, i);
                        arr_adate[2] = "年";
                        ib = 2;
                        ic = i+1;
                        break;
                    case '月':
                        arr_adate[3] = hdate.substring(ic, i);
                        arr_adate[4] = "月";
                        ib = 4;
                        ic = i+1;
                        break;
                    case '日':
                        arr_adate[5] = hdate.substring(ic, i);
                        arr_adate[6] = "日";
                        ib = 6;
                        ic = i+1;
                        break;
                    default:
                        if (ib == 0) {
                            arr_adate[0] = hdate.substring(ic, ic + 1);
                            ic = i;
                        }
                        break;
                    }
                }
                isDigitBefore = isDigit;
            }
            System.out.println("[EditDate]tate_format hdate=" + hdate);
            System.out.println("[EditDate]tate_format hdate=" + arr_adate[0] + arr_adate[1] + " 年 " + arr_adate[2] + " 月 " + arr_adate[3] + " 日");
        } catch (Exception e) {
            System.out.println("h_format error!");
            System.out.println(e);
        }//try-catchの括り

        return arr_adate;

    }//tate_formatの括り

    public static Calendar getCalendar(final String dateStr) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(java.sql.Date.valueOf(dateStr));
        return cal;
    }

    /**-----------------------------------------------------------------------------------------------
     *  日付変換
     *      "1989-12-31" --> {"平成","元","12","31"}
     *-----------------------------------------------------------------------------------------------
     *@deprecated use {@link #tate_format4(DB2UDB db2, String dateStr)}
     **/
    public static String[] tate_format4(final String dateStr) {
        if (null == dateStr) {
            return null;
        }
        final Calendar cal = getCalendar(dateStr);
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH) + 1;
        final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        final String nengoNen = KenjaProperties.gengou(year);

        if (null == nengoNen) {
            return null;
        }
        int nenStartIdx = -1;
        for (int i = nengoNen.length() - 1; i >= 0; i--) {
            nenStartIdx = i + 1;
            if (!Character.isDigit(nengoNen.charAt(i)) && nengoNen.charAt(i) != '元') {
                break;
            }
        }

        final String[] rtn = new String[4];
        if (0 <= nenStartIdx) {
            rtn[0] = nengoNen.substring(0, nenStartIdx);
            rtn[1] = nengoNen.substring(nenStartIdx);
        }
        rtn[2] = String.valueOf(month);
        rtn[3] = String.valueOf(dayOfMonth);
        return rtn;

    }//tate_formatの括り

    /**
     *  システム日付＆時刻
     *  2005/08/09
     *@deprecated use {@link #getNowDateWa(DB2UDB db2, boolean t)}
     */
    public static String getNowDateWa( boolean t )
    {
        StringBuffer stb = new StringBuffer();
        try {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            stb.append( KenjaProperties.gengou(Integer.parseInt( sdf.format(date) )) );
            if( t )sdf = new SimpleDateFormat("年M月d日H時m分");
            else   sdf = new SimpleDateFormat("年M月d日");
            stb.append( sdf.format(date) );
        } catch (Exception ex) {
            log.error("error! " + ex);
        }
        log.debug("date="+stb.toString());
        return stb.toString();
    }

    /**
     *  日付の編集（ブランク挿入）
     *  ○引数について >> １番目は編集対象日付「平成18年1月1日」、２番目は元号取得用年度
     *  ○戻り値について >> 「平成3年1月1日」-> 「平成 3年 1月 1日」
     *@deprecated use {@link #setDateFormat(DB2UDB db2, String hdate, String nendo)}
     */
    public static String setDateFormat(
            final String hdate,
            final String nendo
            ) {
        StringBuffer stb = new StringBuffer();
        try {
            //日付が無い場合は「平成　年  月  日」の様式とする
            if (hdate == null) {
                stb.append(KenjaProperties.gengou(Integer.parseInt(nendo) + 1, 3, 31));
                if (2 < stb.length()) {
                    stb.delete(2, stb.length());
                }
                stb.append("    年    月    日");
                return stb.toString();
            } else {
                //「平成18年 1月 1日」の様式とする => 数値は２桁
                stb = setFormatInsertBlank(hdate);
            }
        } catch (NumberFormatException e) {
            log.error("NumberFormatException", e);
        }
        return stb.toString();
    }

    /**
     *  年度の編集（ブランク挿入）
     *  ○引数について >> １番目は編集対象年度「平成3年度」、２番目は元号取得用年度
     *  ○戻り値について >> 「平成3年度」-> 「平成 3年度」
     *@deprecated use {@link #setNendoFormat(DB2UDB db2, String hdate, String nendo)}
     */
    public static String setNendoFormat(
            final String hdate,
            final String nendo
            ) {
        StringBuffer stb = new StringBuffer();
        //日付が無い場合は「平成　年度」の様式とする
        try {
            if (hdate == null) {
                stb.append(KenjaProperties.gengou(Integer.parseInt(nendo)));
                if(2 < stb.length() )stb.delete(2, stb.length());
                stb.append("    年度");
            } else {
                //「平成18年度」の様式とする => 数値は２桁
                stb = setFormatInsertBlank(hdate);
            }
        } catch (NumberFormatException e) {
            log.error("NumberFormatException", e);
        }
        return stb.toString();
    }

    /**
     *  文字編集（ブランク挿入）
     */
    private static StringBuffer setFormatInsertBlank(final String hdate) {
        final StringBuffer stb = new StringBuffer();
        final StringBuffer digits = new StringBuffer();
        for (int i = 0; i < hdate.length(); i++) {
            final char ch = hdate.charAt(i);
            if (Character.isDigit(ch) || ch == '元') {
                digits.append(ch);
            } else {
                if (0 < digits.length()) {
                    final int keta = KNJ_EditEdit.getMS932ByteLength(digits.toString());
                    digits.insert(0, StringUtils.repeat(" ", 2 - keta));
                    stb.append(" ").append(digits).append(" ");
                    digits.delete(0, digits.length());
                }
                stb.append(ch);
            }
        }
        return stb;
    }

    /**
     * 日付の編集（XXXX年XX月XX日の様式に編集）
     * @param hdate
     * @return
     */
    public static String setDateFormat2(final String hdate) {
        StringBuffer stb = new StringBuffer();
        try {
            if (hdate == null || 0 == hdate.length()) {
                stb.append("    年  月  日");
                return stb.toString();
            } else {
                stb = setFormatInsertBlank2(stb.append(hdate));
            }
        } catch (NumberFormatException e) {
            log.error("NumberFormatException", e);
        }
        return stb.toString();
    }

    /**
     * 文字編集（日付の数字が１桁の場合、ブランクを挿入）
     * @param stb
     * @return
     */
    private static StringBuffer setFormatInsertBlank2(final StringBuffer stb) {
        int n = 0;
        for (int i = 0; i < stb.length(); i++) {
            final char ch = stb.charAt(i);
            if (Character.isDigit(ch)) {
                n++;
            } else if (0 < n) {
                if (1 == n) {
                    stb.insert( i - n, " " );
                    i++;
                    n = 0;
                }
            }
        }
        return stb;
    }


    /**
     * 西暦⇒和暦変換<br>
     **/
    public static String gengou(final DB2UDB db2, int yyyy, int mm, int dd){
        String rtnStr = "";

        // 日付の妥当性チェック
        Calendar cal = new GregorianCalendar();
        cal.setLenient( false );
        cal.set( yyyy, mm-1, dd );
        try{
            cal.getTime();
        } catch( IllegalArgumentException e ){
            throw e;
        }

        // “明治”より古い日付はエラー
        if (cal.before(new GregorianCalendar(1868, 9-1, 8))) {
            throw new IllegalArgumentException();
        }

        final String setMonth = 10 > mm ? "0" + mm : String.valueOf(mm);
        final String setDay = 10 > dd ? "0" + dd : String.valueOf(dd);
        final String setDate = String.valueOf(yyyy) + "/" + setMonth + "/" + setDay;
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     DB2INST1.NAME_MST ");
        stb.append(" WHERE ");
        stb.append("     NAMECD1 = 'L007' ");
        stb.append("     AND '" + setDate + "' BETWEEN NAMESPARE2 AND NAMESPARE3 ");

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            String wrk = "";
            while (rs.next()) {
                final String gengou = rs.getString("NAME1");
                final String ganNen = rs.getString("NAMESPARE1");
                // 和暦変換
                final int nen = yyyy - Integer.parseInt(ganNen) + 1;
                // １年は「元年」
                if (nen == 1) {
                    wrk = gengou + "元";
                } else {
                    wrk = gengou + nen;
                }
                // 日付文字列の作成
                rtnStr = wrk
                    + "年"
                    + mm + "月"
                    + dd + "日";
            }
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return rtnStr;
    }

    /**
     * 日付⇒和暦変換<br>
     **/
    public static String dateGengou(final DB2UDB db2, int yyyy, int mm, int dd) {
        String rtnStr = "";

        // 日付の妥当性チェック
        Calendar cal = new GregorianCalendar();
        cal.setLenient( false );
        cal.set( yyyy, mm-1, dd );
        try{
            cal.getTime();
        } catch( IllegalArgumentException e ){
            throw e;
        }

        // “明治”より古い日付はエラー
        if (cal.before(new GregorianCalendar(1868, 9-1, 8))) {
            throw new IllegalArgumentException();
        }

        final String setMonth = 10 > mm ? "0" + mm : String.valueOf(mm);
        final String setDay = 10 > dd ? "0" + dd : String.valueOf(dd);
        final String setDate = String.valueOf(yyyy) + "/" + setMonth + "/" + setDay;
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     DB2INST1.NAME_MST ");
        stb.append(" WHERE ");
        stb.append("     NAMECD1 = 'L007' ");
        stb.append("     AND '" + setDate + "' BETWEEN NAMESPARE2 AND NAMESPARE3 ");

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            String wrk = "";
            while (rs.next()) {
                final String gengou = rs.getString("NAME1");
                final String ganNen = rs.getString("NAMESPARE1");
                // 和暦変換
                final int nen = yyyy - Integer.parseInt(ganNen) + 1;
                // １年は「元年」
                if (nen == 1) {
                    wrk = gengou + "元";
                } else {
                    wrk = gengou + nen;
                }
                // 日付文字列の作成
                rtnStr = wrk;
            }
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return rtnStr;
    }

    /**
     * 西暦⇒和暦変換
     */
    public static String gengou(final DB2UDB db2, int seireki){
        String rtnStr = "";

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     DB2INST1.NAME_MST ");
        stb.append(" WHERE ");
        stb.append("     NAMECD1='L007' ");
        stb.append("     AND '" + seireki + "' BETWEEN NAMESPARE1 AND ABBV3 ");

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String gengou = rs.getString("NAME1");
                final String ganNen = rs.getString("NAMESPARE1");
                // 和暦変換
                final int nen = seireki - Integer.parseInt(ganNen) + 1;
                // １年は「元年」
                if (nen == 1) {
                    rtnStr = gengou + "元";
                } else {
                    rtnStr = gengou + nen;
                }
            }
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return rtnStr;
    }

    /**
     * 和暦のアルファベット頭文字
     **/
    public static String gengouAlphabetMark(final DB2UDB db2, int yyyy, int mm, int dd){

        // 日付の妥当性チェック
        Calendar cal = new GregorianCalendar();
        cal.setLenient( false );
        cal.set( yyyy, mm-1, dd );
        try{
            cal.getTime();
        } catch( IllegalArgumentException e ){
            throw e;
        }

        // “明治”より古い日付はエラー
        if (cal.before(new GregorianCalendar(1868, 9-1, 8))) {
            throw new IllegalArgumentException();
        }

        final String setMonth = 10 > mm ? "0" + mm : String.valueOf(mm);
        final String setDay = 10 > dd ? "0" + dd : String.valueOf(dd);
        final String setDate = String.valueOf(yyyy) + "/" + setMonth + "/" + setDay;

        return gengouAlphabetMarkOfDate(db2, setDate);
    }

    /**
     * 指定年度に対応するL007の行
     * @param db2
     * @param seireki 年度
     * @return 指定年度に対応するL007の行と、「CALCNEN=和暦の年(Integer型)」のキーセットを返す。
     */
    public static L007Class getL007ofYear(final DB2UDB db2, final String seireki) {

        final StringBuffer sql = new StringBuffer();
        sql.append(" SELECT ");
        sql.append("     * ");
        sql.append(" FROM ");
        sql.append("     DB2INST1.NAME_MST ");
        sql.append(" WHERE ");
        sql.append("     NAMECD1 = 'L007' ");
        sql.append("     AND '" + seireki + "' BETWEEN NAMESPARE1 AND ABBV3 ");
        final Map<String, String> l007 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));
        final String name1 = l007.get("NAME1");
        final String name2 = l007.get("NAME2");
        final String name3 = l007.get("NAME3");
        final String abbv1 = l007.get("ABBV1");
        final String startYear = l007.get("NAMESPARE1");
        final String endYear = l007.get("ABBV3");
        L007Class l007Class = new L007Class(seireki, name1, name2, name3, abbv1, startYear, endYear);

        return l007Class;
    }

    /**
     * 指定日付に対応するL007の行
     * @param db2
     * @param date 日付
     * @return 指定日付に対応するL007の行
     */
    public static L007Class getL007ofDate(final DB2UDB db2, final String date) {

        final StringBuffer sql = new StringBuffer();
        sql.append(" SELECT ");
        sql.append("     * ");
        sql.append(" FROM ");
        sql.append("     DB2INST1.NAME_MST ");
        sql.append(" WHERE ");
        sql.append("     NAMECD1 = 'L007' ");
        sql.append("     AND REPLACE('" + date + "', '-', '/') BETWEEN NAMESPARE2 AND NAMESPARE3 ");
        final Map<String, String> l007 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));
        final String name1 = l007.get("NAME1");
        final String name2 = l007.get("NAME2");
        final String name3 = l007.get("NAME3");
        final String abbv1 = l007.get("ABBV1");
        final String startYear = l007.get("NAMESPARE1");
        final String endYear = l007.get("ABBV3");
        L007Class l007Class = new L007Class(date.substring(0, 4), name1, name2, name3, abbv1, startYear, endYear);

        return l007Class;
    }

    /**
     * 和暦のアルファベット頭文字
     * @param db2
     * @param date 日付 (例: "2019/05/01")
     * @return 和暦のアルファベット頭文字
     */
    public static String gengouAlphabetMarkOfDate(final DB2UDB db2, final String date){
        L007Class l007 = getL007ofDate(db2, String.valueOf(date));
        return l007.getAbbv1();
    }

    /**
     * 和暦のアルファベット頭文字
     * @param db2
     * @param seireki 年度
     * @return 和暦のアルファベット頭文字
     */
    public static String gengouAlphabetMark(final DB2UDB db2, final int seireki){
        L007Class l007 = getL007ofYear(db2, String.valueOf(seireki));
        return l007.getAbbv1();
    }

    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP("2002-10-27")     :平成14年10月27日
     *   String dat = h_format_JP("2002/10/27")     :平成14年10月27日
     *----------------------------------------------------------------------------------------------*/
    public static String h_format_JP(final DB2UDB db2, String strx) {

        String hdate = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern(patternYyyyMMddHyphen);
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern(patternYyyyMMddSlash);
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    hdate = "";
                    return hdate;
                }
            }
            Calendar cal = new GregorianCalendar(localja_JP());
            cal.setTime(dat);
            hdate = gengou(db2, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DATE));
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_JPの括り



    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP_M("2002-10-27")       :平成14年10月
     *   String dat = h_format_JP_M("2002/10/27")       :平成14年10月
     *----------------------------------------------------------------------------------------------*/
    public static String h_format_JP_M(final DB2UDB db2, String strx) {

        String hdate = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern(patternYyyyMMddHyphen);
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern(patternYyyyMMddSlash);
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    try {                                       //applyPatternを追加 2003/12/15
                        sdf.applyPattern("yyyyMMdd");
                        dat = sdf.parse(strx);
                    } catch (Exception e3) {
                        hdate = "";
                        return hdate;
                    }
                }
            }
            Calendar cal = new GregorianCalendar(localja_JP());
            cal.setTime(dat);
            String stra = gengou(db2, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DATE));
            int ia = stra.indexOf('月');
            if( ia>=0 )     hdate = stra.substring(0,ia+1);
            else            hdate = "";
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_JP_Mの括り


    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP("2016-10-27")     :2016年10月
     *   String dat = h_format_JP("2016/10/27")     :2016年10月
     *----------------------------------------------------------------------------------------------*/
    public static String h_format_Seireki_M(final String date) {
        if (null == date) {
            return date;
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        String retVal = "";
        sdf.applyPattern("yyyy年M月");
        retVal = sdf.format(java.sql.Date.valueOf(date));

        return retVal;
    }


    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP_N("2002-10-27")       :平成14年
     *   String dat = h_format_JP_N("2002/10/27")       :平成14年
     *----------------------------------------------------------------------------------------------*/
    public static String h_format_JP_N(final DB2UDB db2, String strx) {

        String hdate = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern(patternYyyyMMddHyphen);
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern(patternYyyyMMddSlash);
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    hdate = "";
                    return hdate;
                }
            }
            Calendar cal = new GregorianCalendar(localja_JP());
            cal.setTime(dat);
            String stra = gengou(db2, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DATE));
            int ia = stra.indexOf('年');
            if( ia>=0 )     hdate = stra.substring(0,ia+1);
            else            hdate = "";
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_JP_Nの括り


    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP("2016-10-27")     :2016年
     *   String dat = h_format_JP("2016/10/27")     :2016年
     *----------------------------------------------------------------------------------------------*/
    public static String h_format_Seireki_N(final String strx) {
        if (null == strx) {
            return strx;
        }
        String hdate = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern(patternYyyyMMddHyphen);
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern(patternYyyyMMddSlash);
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    hdate = "";
                    return hdate;
                }
            }
            sdf.applyPattern("yyyy年");
            hdate = sdf.format(dat);

        } catch (Exception e3) {
            hdate = "";
        }
        return hdate;
    }

    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(汎用)
     * ※使い方
     *   String dat = h_format("2002-10-27","yyyy-MM-dd","年M月d日","ja","JP")     :平成14年10月27日
     *   String dat = h_format("2002/03/31","yyyy/MM/dd","年M月","ja","JP")           :平成14年3月
     *   String dat = h_format("2002-04-08","yyyy-MM-dd",MMM d,yyyy,"en","US")      :Apr 8,2002
     *----------------------------------------------------------------------------------------------*/
    public static String h_format(final DB2UDB db2, String strx,String pat,String fmt,String lag,String cnt) {

        String stry = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern( pat );
            Date dat = sdf.parse(strx);
            Locale local = new Locale(lag,cnt);
            String stra = new SimpleDateFormat(fmt,local).format( dat );
            if( cnt.equals("JP") ){
                Calendar cal = new GregorianCalendar(local);
                cal.setTime(dat);
                String strb = gengou(db2, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DATE));
                int ia = stra.indexOf('年');
                if( ia>=0 )     stry = strb.substring(0,ia)+strb;
                else            stry = "";

                stry = strb + stra;
            }else
                stry = stra;
        } catch (ParseException e) {
            System.out.println( "[EditDate]h_format err = " + e.getMessage() );
            stry = "";
        }

        return stry;

    }//String h_formatの括り

    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP_F("2003-01-01")       :平成15年1月1日生
     *   String dat = h_format_JP_F(null)               :
     *----------------------------------------------------------------------------------------------*/
    public static String h_format_JP_Bth(final DB2UDB db2, String strx) {

        String hdate = "";
        try {
            if( strx!=null )    hdate = h_format_JP(db2, strx);
            else                hdate = "";
            if( hdate.length()>0 )      hdate = hdate+"生";
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_JP_Fの括り

    /*-----------------------------------------------------------------------------------------------
     *  日付変換
     *      "1989-12-31" --> {"平成","元","12","31"}
     *-----------------------------------------------------------------------------------------------*/
    public static String[] tate_format4(final DB2UDB db2, final String dateStr) {
        if (null == dateStr) {
            return null;
        }
        final Calendar cal = getCalendar(dateStr);
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH) + 1;
        final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        final String nengoNen = dateGengou(db2, year, month, dayOfMonth);

        if (null == nengoNen) {
            return null;
        }
        int nenStartIdx = -1;
        for (int i = nengoNen.length() - 1; i >= 0; i--) {
            nenStartIdx = i + 1;
            if (!Character.isDigit(nengoNen.charAt(i)) && nengoNen.charAt(i) != '元') {
                break;
            }
        }

        final String[] rtn = new String[4];
        if (0 <= nenStartIdx) {
            rtn[0] = nengoNen.substring(0, nenStartIdx);
            rtn[1] = nengoNen.substring(nenStartIdx);
        }
        rtn[2] = String.valueOf(month);
        rtn[3] = String.valueOf(dayOfMonth);
        return rtn;

    }//tate_formatの括り

    /**
     *  システム日付＆時刻
     *  2005/08/09
     */
    public static String getNowDateWa(final DB2UDB db2, boolean t)
    {
        StringBuffer stb = new StringBuffer();
        try {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            stb.append( gengou(db2, Integer.parseInt( sdf.format(date) )) );
            if( t )sdf = new SimpleDateFormat("年M月d日H時m分");
            else   sdf = new SimpleDateFormat("年M月d日");
            stb.append( sdf.format(date) );
        } catch (Exception ex) {
            log.error("error! " + ex);
        }
        log.debug("date="+stb.toString());
        return stb.toString();
    }

    /**
     *  日付の編集（ブランク挿入）
     *  ○引数について >> １番目は編集対象日付「平成18年1月1日」、２番目は元号取得用年度
     *  ○戻り値について >> 「平成3年1月1日」-> 「平成 3年 1月 1日」
     */
    public static String setDateFormat(
            final DB2UDB db2,
            final String hdate,
            final String nendo
            ) {
        StringBuffer stb = new StringBuffer();
        try {
            //日付が無い場合は「平成　年  月  日」の様式とする
            if (hdate == null) {
                stb.append(gengou(db2, Integer.parseInt(nendo) + 1, 3, 31));
                if (2 < stb.length()) {
                    stb.delete(2, stb.length());
                }
                stb.append("    年    月    日");
                return stb.toString();
            } else {
                //「平成18年 1月 1日」の様式とする => 数値は２桁
                stb = setFormatInsertBlank(hdate);
            }
        } catch (NumberFormatException e) {
            log.error("NumberFormatException", e);
        }
        return stb.toString();
    }

    /**
     *  年度の編集（ブランク挿入）
     *  ○引数について >> １番目は編集対象年度「平成3年度」、２番目は元号取得用年度
     *  ○戻り値について >> 「平成3年度」-> 「平成 3年度」
     */
    public static String setNendoFormat(
            final DB2UDB db2,
            final String hdate,
            final String nendo
            ) {
        StringBuffer stb = new StringBuffer();
        //日付が無い場合は「平成　年度」の様式とする
        try {
            if (hdate == null) {
                stb.append(gengou(db2, Integer.parseInt(nendo)));
                if(2 < stb.length() )stb.delete(2, stb.length());
                stb.append("    年度");
            } else {
                //「平成18年度」の様式とする => 数値は２桁
                stb = setFormatInsertBlank(hdate);
            }
        } catch (NumberFormatException e) {
            log.error("NumberFormatException", e);
        }
        return stb.toString();
    }


    /**
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP("2016-10-27")     :平成28年10月27日 OR 2016年10月27日
     *   String dat = h_format_JP("2016/10/27")     :平成28年10月27日 OR 2016年10月27日
     **/
    public static String getAutoFormatDate(final DB2UDB db2, final String date) {
        String retDate = "";
        final boolean isSeireki = isSeireki(db2);
        if (isSeireki) {
            retDate = h_format_SeirekiJP(date);
        } else {
            retDate = h_format_JP(db2, date);
        }
        return retDate;
    }

    /**
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP_N("2002-10-27")       :平成14年10月 OR 2002年10月
     *   String dat = h_format_JP_N("2002/10/27")       :平成14年10月 OR 2002年10月
     **/
    public static String getAutoFormatYM(final DB2UDB db2, String date) {

        String retDate = "";
        final boolean isSeireki = isSeireki(db2);
        if (isSeireki) {
            retDate = h_format_Seireki_M(date);
        } else {
            retDate = h_format_JP_M(db2, date);
        }
        return retDate;

    }//String h_format_JP_Nの括り

    /**
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP_N("2002-10-27")       :平成14年 OR 2002年
     *   String dat = h_format_JP_N("2002/10/27")       :平成14年 OR 2002年
     **/
    public static String getAutoFormatYearNen(final DB2UDB db2, String date) {

        String retDate = "";
        final boolean isSeireki = isSeireki(db2);
        if (isSeireki) {
            retDate = h_format_Seireki_N(date);
        } else {
            retDate = h_format_JP_N(db2, date);
        }
        return retDate;

    }//String h_format_JP_Nの括り

    /**
     * 平成14 OR 2002
     */
    public static String getAutoFormatYear(final DB2UDB db2, int seireki){
        String retDate = "";
        final boolean isSeireki = isSeireki(db2);
        if (isSeireki) {
            return String.valueOf(seireki);
        }

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     DB2INST1.NAME_MST ");
        stb.append(" WHERE ");
        stb.append("     NAMECD1='L007' ");
        stb.append("     AND '" + seireki + "' BETWEEN NAMESPARE1 AND ABBV3 ");

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String gengou = rs.getString("NAME1");
                final String ganNen = rs.getString("NAMESPARE1");
                // 和暦変換
                final int nen = seireki - Integer.parseInt(ganNen) + 1;
                // １年は「元年」
                if (nen == 1) {
                    retDate = gengou + "元";
                } else {
                    retDate = gengou + nen;
                }
            }
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return retDate;
    }

}
