// kanji=漢字
/*
 * $Id: e809db53c27eed8fcaa173ba1942d3b263da2ba6 $
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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *					＜ＫＮＪＡ１４３＞  身分証明書（東京都）
 *
 *	2005/03/25 nakamoto 作成日
 *	2005/03/31 nakamoto 生年月日・発行日を元号・年・月・日と分けてフィールドにセットするように変更 ---NO001
 *	2005/04/05 nakamoto 所属：所属名＋固定文字「課程」を追加 ---NO002
 *  2007/03/16 nakamoto NO003:内外区分(inoutcd)が（'9'の時、'9'以外の時）の「タイトル・所属・文」の表示を変更した。
 *  2007/03/30 nakamoto NO004:パラメータ"GRADE_HR_CLASS"は、使用しないよう変更した。（不具合例：クラスコードに漢字）
 *
 **/

public class KNJA143D {

    private static final Log log = LogFactory.getLog(KNJA143D.class);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意

        Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;						//Databaseクラスを継承したクラス
        Param param = null;

        //	print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //	svf設定
        svf.VrInit();						   		//クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());  		//PDFファイル名の設定

        //	ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        PreparedStatement ps1 = null;
        boolean nonedata = false;
        try {
            //  パラメータの取得
            KNJServletUtils.debugParam(request, log);
            param = new Param(db2, request);

            //  ＳＶＦ作成処理
            //SQL作成
            try {
                ps1 = db2.prepareStatement(preStat1(param));        //生徒情報

            } catch (Exception ex) {
                log.error("DB2 prepareStatement set error!", ex);
            }
            if ("musashinohigashi".equals(param._z010Name1)) {
                if (setSvfoutMusasinoHigasi(db2, svf, param, ps1)) {
                    nonedata = true;     //帳票出力のメソッド
                }
            } else {
                if (setSvfout(db2,svf,param,ps1)) {
                    nonedata = true;        //帳票出力のメソッド
                }
            }

            log.debug("nonedata = "+nonedata);
        } catch (Exception ex) {
            log.error("parameter error!", ex);
        } finally {
            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            //  終了処理
            svf.VrQuit();
            DbUtils.closeQuietly(ps1);
            db2.commit();
            db2.close();                //DBを閉じる
            outstrm.close();            //ストリームを閉じる


        }

    }//doGetの括り

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        return KNJ_EditEdit.getMS932ByteLength(str);
    }

    /**帳票出力**/
    private boolean setSvfout(
            DB2UDB db2,
            Vrw32alp svf,
            Param param,
            PreparedStatement ps1
            ) {
        boolean nonedata = false;
        ResultSet rs = null;
        try {
            rs = ps1.executeQuery();

            //画像--------------
            String photo = "";                      //顔写真
            String stamp = "SCHOOLSTAMP" + (null == param._schoolKind ? "" : ("_" + param._schoolKind)) + param._extensionStamp;  //学校印
            String photo_check = "";
            String stamp_check = param._documentroot + "/" + param._imagePass + "/" + stamp;
            File f2 = new File(stamp_check);        //学校長印データ存在チェック用

            svf.VrSetForm("KNJA143D_1.frm", 4);//用紙(表)

            int cnt = 0;
            boolean kisuuFlg = false;
            boolean gusuuFlg = false;
            boolean kaipageFlg = false;

            while( rs.next() ){
                cnt++;
                kisuuFlg = cnt % 2 == 1;
                gusuuFlg = cnt % 2 == 0;
                String fieldNo = (gusuuFlg) ? "_2" : "";
                kaipageFlg = cnt % 10 == 0;
                //画像--------------
                //顔写真
                photo = "P" + rs.getString("SCHREGNO") + "." + param._extensionPhoto;
                photo_check = param._documentroot + "/" + param._imagePass + "/" + photo;
                File f1 = new File(photo_check);//写真データ存在チェック用
                if (f1.exists())
                    svf.VrsOut("PHOTO_BMP"+fieldNo    , photo_check );//顔写真
                //学校印
                if (f2.exists())
                    svf.VrsOut("STAMP_BMP"+fieldNo    , stamp_check );//学校印
                //生徒情報--------------
                if (rs.getString("INOUTCD").equals("9")) {
                    svf.VrsOut("COURSE"+fieldNo       ,"" );                              //所属
                    svf.VrsOut("ENT_SCHOOL"+fieldNo   ,rs.getString("ENT_SCHOOL") );      //所属
                    svf.VrsOut("TITLE"+fieldNo        ,"併修生証" );                      //タイトル
                    svf.VrsOut("SENTENCE"+fieldNo     ,"併修生" );                        //文
                } else {
                    String gradeName = (rs.getString("GRADE_NAME2") != null) ? rs.getString("GRADE_NAME2") : "";
                    String hrclassName = (rs.getString("HR_CLASS_NAME1") != null) ? getConvertHanToZenSuuji(rs.getString("HR_CLASS_NAME1")) + "組" : "　組";
                    String attendnoName = (rs.getString("ATTENDNO") != null) ? getConvertHanToZenSuuji(rs.getString("ATTENDNO")) + "番" : "　番";
                    svf.VrsOut("COURSE"+fieldNo       ,gradeName + hrclassName + attendnoName );
                    svf.VrsOut("ENT_SCHOOL"+fieldNo   ,"" );      //所属
                    svf.VrsOut("TITLE"+fieldNo        ,"生徒証" );                        //タイトル
                    svf.VrsOut("SENTENCE"+fieldNo     ,"生徒" );                          //文
                }
                svf.VrsOut("SCHREGNO"+fieldNo     ,rs.getString("SCHREGNO") );            //学籍番号
                svf.VrsOut("NAME"+fieldNo 	    ,"1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME"));                //氏名(漢字)
//				svf.VrsOut("BARCODE"      ,rs.getString("SCHREGNO") );            //バーコード
                /* 生年月日・発行日---NO001 */
                setDivisionDate(db2, svf, param, rs.getString("BIRTHDAY"), fieldNo);
                //発行者情報--------------
                if ("1".equals(param._useAddrField2) && getMS932ByteLength(param._remark1) > 50) {
                    svf.VrsOut("SCHOOLADDRESS5"+fieldNo   ,param._remark1 );                       //学校住所
                } else if ("1".equals(param._useAddrField2) && getMS932ByteLength(param._remark1) > 30) {
                    svf.VrsOut("SCHOOLADDRESS3"+fieldNo   ,param._remark1 );                       //学校住所
                } else {
                    svf.VrsOut("SCHOOLADDRESS1"+fieldNo   ,param._remark1 );                       //学校住所
                }
                svf.VrsOut("SCHOOLNAME1"+fieldNo  ,param._schoolName );                           //学校名
                svf.VrsOut("JOBNAME"+fieldNo      ,param._jobName );                            //職名
                svf.VrsOut("STAFFNAME"+fieldNo    ,param._principalName );                           //職員名

                if (gusuuFlg) {
                    svf.VrEndRecord();//１行出力
                    if (kaipageFlg) {
                        svf.VrSetForm("KNJA143D_2.frm", 4);//用紙(裏)
                        svf.VrsOut("DUMMY"     ,"DUMMY" );            //ダミー
                        svf.VrEndRecord();//１枚出力
                        svf.VrSetForm("KNJA143D_1.frm", 4);//用紙(表)
                    }
                }
                nonedata = true;
            }
            if (kisuuFlg) {
                svf.VrEndRecord();//１行出力
            }
            if (kisuuFlg || gusuuFlg && !kaipageFlg) {
                svf.VrSetForm("KNJA143D_2.frm", 4);//用紙(裏)
                svf.VrsOut("DUMMY"     ,"DUMMY" );            //ダミー
                svf.VrEndRecord();//１枚出力
            }
        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return nonedata;
    }

    /**帳票出力**/
    private boolean setSvfoutMusasinoHigasi(
            DB2UDB db2,
            Vrw32alp svf,
            Param param,
            PreparedStatement ps1
            ) {
        boolean nonedata = false;
        ResultSet rs = null;
        try {
            rs = ps1.executeQuery();

            //画像--------------
            String photo = "";                      //顔写真
            String stamp = "SCHOOLSTAMP" + (null == param._schoolKind ? "" : ("_" + param._schoolKind)) + param._extensionStamp;  //学校印
            String photo_check = "";
            String stamp_check = param._documentroot + "/" + param._imagePass + "/" + stamp;
            File f2 = new File(stamp_check);        //学校長印データ存在チェック用

            svf.VrSetForm("KNJA143D_3.frm", 4);//用紙(表)

            Integer limitDateFiscalYear = null;
            if (!StringUtils.isBlank(param._limitDate)) {
                final String limitDateFiscalYearString = KnjDbUtils.getOne(KnjDbUtils.query(db2, " VALUES FISCALYEAR('" + param._limitDate + "') "));
                if (NumberUtils.isDigits(limitDateFiscalYearString)) {
                    limitDateFiscalYear = Integer.parseInt(limitDateFiscalYearString);
                }
            }
            final DecimalFormat df00 = new DecimalFormat("00");
            final Map<String, String> gradeMap = new HashMap<String, String>();
            for (int g = 1; g <= 6; g++) {
                gradeMap.put(df00.format(g), String.valueOf((char) ('０' + g)));
            }
            int cnt = 0;
            boolean kisuuFlg = false;
            boolean gusuuFlg = false;
            boolean kaipageFlg = false;
            List backPrint = new ArrayList();

            while( rs.next() ){
                cnt++;
                kisuuFlg = cnt % 2 == 1;
                gusuuFlg = cnt % 2 == 0;
                String fieldNo = (gusuuFlg) ? "_2" : "";
                kaipageFlg = cnt % 10 == 0;
                //画像--------------
                //顔写真
                photo = "P" + rs.getString("SCHREGNO") + "." + param._extensionPhoto;
                photo_check = param._documentroot + "/" + param._imagePass + "/" + photo;
                File f1 = new File(photo_check);//写真データ存在チェック用
                if (f1.exists()) {
                    svf.VrsOut("PHOTO_BMP"+fieldNo    , photo_check );//顔写真
                }
                //学校印
                if (f2.exists()) {
                    svf.VrsOut("STAMP_BMP"+fieldNo    , stamp_check );//学校印
                }
                //生徒情報--------------
                final String gradeCd = rs.getString("GRADE_CD");
                String gradeName = "";
                if (NumberUtils.isDigits(gradeCd)) {
                    String printGradeCd = gradeCd;
                    if (null != limitDateFiscalYear) { // 有効期限時点の学年を表示
                        printGradeCd = df00.format(Integer.parseInt(gradeCd) + limitDateFiscalYear - Integer.parseInt(param._year));
                    }
                    if (gradeMap.containsKey(printGradeCd)) {
                        gradeName = "第" + gradeMap.get(printGradeCd) + "学年";
                    }
                }
                svf.VrsOut("GRADE"+fieldNo       ,gradeName);
                svf.VrsOut("NAME"+fieldNo       ,"1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME"));                //氏名(漢字)
                svf.VrsOut("TITLE"+fieldNo        ,"身分証明書" );
                svf.VrsOut("SENTENCE"+fieldNo     ,"生徒" );
                svf.VrsOut("SCHREGNO"+fieldNo     ,rs.getString("SCHREGNO") );
                setDivisionDate(db2, svf, param, rs.getString("BIRTHDAY"), fieldNo);
                String setAddr = null != rs.getString("ADDR1") ? rs.getString("ADDR1") : "";
                setAddr = null != rs.getString("ADDR2") ? setAddr + rs.getString("ADDR2") : setAddr;
                if (getMS932ByteLength(setAddr) > 50) {
                    svf.VrsOut("ADDRESS5" + fieldNo, setAddr);
                } else if (getMS932ByteLength(setAddr) > 30) {
                    svf.VrsOut("ADDRESS3" + fieldNo, setAddr);
                } else {
                    svf.VrsOut("ADDRESS1" + fieldNo, setAddr);
                }

                //発行者情報--------------
                if ("1".equals(param._useAddrField2) && getMS932ByteLength(param._remark1) > 50) {
                    svf.VrsOut("SCHOOLADDRESS5"+fieldNo   ,param._remark1 );                       //学校住所
                } else if ("1".equals(param._useAddrField2) && getMS932ByteLength(param._remark1) > 30) {
                    svf.VrsOut("SCHOOLADDRESS3"+fieldNo   ,param._remark1 );                       //学校住所
                } else {
                    svf.VrsOut("SCHOOLADDRESS1"+fieldNo   ,param._remark1 );                       //学校住所
                }
                svf.VrsOut("SCHOOLNAME1" + fieldNo, param._schoolName );
                svf.VrsOut("JOBNAME"+fieldNo      ,param._jobName );                            //職名
                svf.VrsOut("STAFFNAME"+fieldNo    ,param._principalName );                           //職員名
                backPrint.add(fieldNo);

                if (gusuuFlg) {
                    svf.VrEndRecord();//１行出力
                    if (kaipageFlg) {
                        svf.VrSetForm("KNJA143D_4.frm", 1);//用紙(裏)
                        int setLine = 0;
                        for (Iterator iterator = backPrint.iterator(); iterator.hasNext();) {
                            String setField = (String) iterator.next();
                            setLine = "_2".equals(setField) ? setLine : setLine + 1;
                            svf.VrsOutn("SCHOOL_NAME" + setField, setLine, param._schoolName );
                            svf.VrsOutn("LIMIT" + setField, setLine, KNJ_EditDate.h_format_JP(db2, param._yuukouKigen));
                        }
                        svf.VrEndPage();
                        svf.VrSetForm("KNJA143D_3.frm", 4);//用紙(表)
                        backPrint = new ArrayList();
                    }
                }
                nonedata = true;
            }
            if (kisuuFlg) {
                svf.VrEndRecord();//１行出力
            }
            if (kisuuFlg || gusuuFlg && !kaipageFlg) {
                svf.VrSetForm("KNJA143D_4.frm", 1);//用紙(裏)
                int setLine = 0;
                for (Iterator iterator = backPrint.iterator(); iterator.hasNext();) {
                    String setField = (String) iterator.next();
                    setLine = "_2".equals(setField) ? setLine : setLine + 1;
                    svf.VrsOutn("SCHOOL_NAME" + setField, setLine, param._schoolName );
                    svf.VrsOutn("LIMIT" + setField, setLine, KNJ_EditDate.h_format_JP(db2, param._yuukouKigen));
                }
                svf.VrEndPage();
            }
        } catch( Exception ex ) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return nonedata;
    }

    /**
     *  svf print 半角数字を全角数字へ変換(文字単位)
     */
    private String getConvertHanToZenSuuji(final String suuji)
    {
        final String arrayZenSuuji[] = {"０","１","２","３","４","５","６","７","８","９"};
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < suuji.length(); i++) {
            if (Character.isDigit(suuji.charAt(i))) {
                stb.append(arrayZenSuuji[Integer.parseInt(suuji.substring(i, i + 1))]);
            } else {
                stb.append(suuji.substring(i, i + 1));
            }
        }
        stb.append("");
        return stb.toString();
    }

    /** 生年月日・発行日を元号・年・月・日と分ける---NO001 */
    private void setDivisionDate(
            final DB2UDB db2,
            Vrw32alp svf,
            Param param,
            final String birthday,
            String fieldNo
            ) {
        try {
            //発行日
            if (param._termSdate != null) {
                String sdate = KNJ_EditDate.h_format_JP(db2, param._termSdate);
                String arr_sdate[] = KNJ_EditDate.tate_format(sdate);
                for (int i = 1; i < 5; i++)
                    svf.VrsOut("SDATE"+String.valueOf(i)+fieldNo	,arr_sdate[i-1] );
            }
            //有効期限
            if (!StringUtils.isBlank(param._limitDate)) {
                String ldate = KNJ_EditDate.h_format_JP(db2, param._limitDate);
                String arr_sdate[] = KNJ_EditDate.tate_format(ldate);
                for (int i = 1; i < 5; i++)
                    svf.VrsOut("EDATE"+String.valueOf(i)+fieldNo	,arr_sdate[i-1] );
            }
            //生年月日
            if (birthday != null) {
                String birth = KNJ_EditDate.h_format_JP(db2, birthday);
//log.debug("birth = "+birth);
                String arr_birth[] = KNJ_EditDate.tate_format(birth);
                for (int i = 1; i < 5; i++) {
                    if (arr_birth[1] == null) arr_birth[1] = (arr_birth[0]).substring(2);
                    svf.VrsOut("BIRTHDAY"+String.valueOf(i)+fieldNo	,arr_birth[i-1] );
//log.debug("arr_birth = "+arr_birth[i-1]);
                }
            }
        } catch( Exception ex ) {
            log.error("setDivisionDate error!", ex);
        }
    }

    /**生徒情報**/
    private String preStat1(Param param)
    {
        StringBuffer stb = new StringBuffer();
        //メイン
        stb.append(" WITH SCHREG_ADDRESS_MAX AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ADDRESS_DAT ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO ");
        stb.append("     ) ");
        stb.append(" , SCHREG_ADDRESS AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.ZIPCD, ");
        stb.append("         P1.PREF_CD, ");
        stb.append("         P1.PREF_NAME, ");
        stb.append("         T1.AREACD, ");
        stb.append("         N1.NAME1 AS AREA_NAME, ");
        stb.append("         T1.ADDR1, ");
        stb.append("         T1.ADDR2, ");
        stb.append("         T1.TELNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ADDRESS_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_ADDRESS_MAX T2 ");
        stb.append("             ON  T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND T2.ISSUEDATE = T1.ISSUEDATE ");
        stb.append("         LEFT JOIN ZIPCD_MST Z1 ON Z1.NEW_ZIPCD = T1.ZIPCD ");
        stb.append("         LEFT JOIN PREF_MST P1 ON P1.PREF_CD = SUBSTR(Z1.CITYCD,1,2) ");
        stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A020' AND N1.NAMECD2 = T1.AREACD ");
        stb.append("     ) ");
        stb.append("SELECT T1.SCHREGNO, ");
        stb.append("       L1.GRADE_CD, ");
        stb.append("       L1.GRADE_NAME2, ");
        stb.append("       L2.HR_CLASS_NAME1, ");
        stb.append("       SMALLINT(T1.ATTENDNO) AS ATTENDNO, ");
        stb.append("       value(T2.INOUTCD,'') as INOUTCD, ");
        stb.append("       T2.ENT_SCHOOL, ");
        stb.append("       T2.NAME, ");
        stb.append("       T2.REAL_NAME, ");
        stb.append("       (CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
        stb.append("       ADDR.ADDR1, ");
        stb.append("       ADDR.ADDR2, ");
        stb.append("       T2.BIRTHDAY ");
        stb.append("FROM   SCHREG_REGD_DAT T1 ");
        stb.append("       INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ");
        stb.append("       LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO=T1.SCHREGNO ");
        stb.append("            AND T6.DIV='05' ");
        stb.append("       LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR AND L1.GRADE = T1.GRADE ");
        stb.append("       LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.YEAR = T1.YEAR AND L2.SEMESTER = T1.SEMESTER ");
        stb.append("            AND L2.GRADE = T1.GRADE AND L2.HR_CLASS = T1.HR_CLASS ");
        stb.append("       LEFT JOIN SCHREG_ADDRESS ADDR ON ADDR.SCHREGNO = T1.SCHREGNO ");
        stb.append("WHERE  T1.YEAR='"+param._year+"' AND ");
        stb.append("       T1.SEMESTER='"+param._semester+"' AND ");
        stb.append("       T1.SCHREGNO IN "+param._schregnoIn+" ");
        stb.append("ORDER BY ");
        stb.append("       T1.GRADE, ");
        stb.append("       T1.HR_CLASS, ");
        stb.append("       T1.ATTENDNO ");
        return stb.toString();

    }//preStat1()の括り

    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _hrclass;
        private final String _schregnoIn;
        private final String _termSdate;
        private final String _limitDate; // 武蔵野東のみ
        private final String _useAddrField2;
        private String _jobName;
        private final String _documentroot;
        private String _imagePass;
        private String _extensionPhoto;
        private String _principalName;
        private String _schoolName;
        private String _remark1;
        private String _extensionStamp;
        private String _schoolKind;
        private boolean _hasCertifSchoolDatRecord = false;
        private String _z010Name1;
        private final String _yuukouKigen;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");                        //年度
            _semester = request.getParameter("GAKKI");                       //学期
            _hrclass = request.getParameter("GRADE_HR_CLASS");              //学年＋組
            String sdate = request.getParameter("TERM_SDATE");              //発行日
            _termSdate = sdate.replace('/','-');
            _limitDate = StringUtils.replace(request.getParameter("LIMIT_DATE"), "/", "-");
            _documentroot = request.getParameter("DOCUMENTROOT");                // '/usr/local/deve_oomiya/src'
            // 学籍番号の指定
            String schno[] = request.getParameterValues("category_selected");//学籍番号
            int i = 0;
            String str = "(";
            while (i < schno.length) {
                if(schno[i] == null ) break;
                if(i > 0) str = str + ",";
                str = str + "'" + schno[i] + "'";
                i++;
            }
            str = str + ")";
            _schregnoIn = str;

            _useAddrField2 = request.getParameter("useAddrField2");
            _yuukouKigen = getYuukouKigen(db2);

            setHeader(db2);
            loadCertifSchoolDat(db2);

            if (!_hasCertifSchoolDatRecord) {
                //SVF出力
                setStaffJobName(db2);                     //職名・職員名取得メソッド
                setSchoolName(db2);                       //学校名取得メソッド
            }
        }

        /**職名・職員名を取得**/
        private void setStaffJobName(
                DB2UDB db2
                ) {
            PreparedStatement ps2 = null;
            ResultSet rs = null;
            try {
                StringBuffer stb = new StringBuffer();
                stb.append("SELECT STAFFNAME, ");
                stb.append("       (SELECT JOBNAME FROM JOB_MST T2 WHERE T2.JOBCD=T1.JOBCD) AS JOBNAME ");
                stb.append("FROM   V_STAFF_MST T1 ");
                stb.append("WHERE  YEAR='" + _year + "' AND JOBCD='0001' ");//学校長

                ps2 = db2.prepareStatement(stb.toString());        //職名・職員名

                rs = ps2.executeQuery();
                while( rs.next() ){
                    _jobName  = rs.getString("JOBNAME");    //職名
                    _principalName = rs.getString("STAFFNAME");  //職員名
                }
            } catch (Exception ex) {
                log.error("setStaffJobName set error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps2, rs);
                db2.commit();
            }
        }

        /**学校名を取得**/
        private void setSchoolName(
                DB2UDB db2
                ) {
            PreparedStatement ps3 = null;
            ResultSet rs = null;
            try {
                StringBuffer stb = new StringBuffer();
                if ("musashinohigashi".equals(_z010Name1)) {
                    stb.append("SELECT SCHOOLNAME1,SCHOOLADDR1 ");
                    stb.append("FROM   SCHOOL_MST ");
                    stb.append("WHERE  YEAR='" + _year + "' ");
                } else {
                    stb.append("SELECT SCHOOLNAME1,SCHOOLADDR1 ");
                    stb.append("FROM   SCHOOL_MST ");
                    stb.append("WHERE  YEAR='" + _year + "' ");
                }

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

        private String getYuukouKigen(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String retstr = "";
            try {
                final String sql = "SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _hrclass.substring(0, 2) + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String gradeCd = rs.getString("GRADE_CD");
                    if ("01".equals(gradeCd)) {
                        retstr = String.valueOf(Integer.parseInt(_year) + 1 + "/03/31");
                    } else if ("02".equals(gradeCd)) {
                        retstr = String.valueOf(Integer.parseInt(_year) + 2) + "/03/31";
                    } else {
                        retstr = String.valueOf(Integer.parseInt(_year) + 3) + "/03/31";
                    }
                }
            } catch( Exception e ){
                log.error("setHeader name_mst error!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retstr;
        }

        public void loadCertifSchoolDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String certifKindcd = null;
            try {
                final String sql = "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _hrclass.substring(0, 2) + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolKind = rs.getString("SCHOOL_KIND");
                    if ("J".equals(_schoolKind)) {
                        certifKindcd = "102";
                    } else if ("H".equals(_schoolKind)) {
                        certifKindcd = "101";
                    }
                }
            } catch( Exception e ){
                log.error("setHeader name_mst error!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            if ("musashinohigashi".equals(_z010Name1)) {
                certifKindcd = "101";
            }
            if (null != certifKindcd) {
                try {
                    final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + certifKindcd + "'";
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        _jobName =  rs.getString("JOB_NAME");
                        _principalName = rs.getString("PRINCIPAL_NAME");
                        _schoolName = rs.getString("SCHOOL_NAME");
                        _remark1 = rs.getString("REMARK1"); // 学校住所
                        _hasCertifSchoolDatRecord = true;
                    }
                } catch( Exception e ){
                    log.error("setHeader name_mst error!", e);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
            }
        }

        /** 事前処理 **/
        private void setHeader(
                DB2UDB db2) {
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
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String schoolName = rs.getString("NAME1");
                    _z010Name1 = schoolName;
                    if ("jisyukan".equals(schoolName)) {
                        _extensionStamp = ".jpg";
                    }
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
