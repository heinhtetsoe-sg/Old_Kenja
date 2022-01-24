// kanji=漢字
/*
 * $Id: KnjAnotherSchoolGetcreditsDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ANOTHER_SCHOOL_GETCREDITS_DATを作る。
 * @author takaesu
 * @version $Id: KnjAnotherSchoolGetcreditsDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjAnotherSchoolGetcreditsDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjAnotherSchoolGetcreditsDat.class);

    public static final String ICASS_TABLE = "SEITO_JIKOGAI_RISHU_KAMOKU";

    public int _countter99 = 0;
    public static final DecimalFormat _subClassCd99Format = new DecimalFormat("990000");
    public static final DecimalFormat _subClassCd = new DecimalFormat("0000");

    public static final Map _kyouka_henkan = new HashMap();

    public KnjAnotherSchoolGetcreditsDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "自校外修得単位データ"; }

    void migrate() throws SQLException {
        setKyoukaHenkan();
        for (int strSep = 1; strSep < 60000; strSep += 5000) {
            final int endSep = strSep + 4999;
            final List list = loadIcass(strSep, endSep);
            log.debug(ICASS_TABLE + "データ件数=" + list.size());
            log.debug(String.valueOf(strSep) + "～" + String.valueOf(endSep));

            try {
                saveKnj(list);
            } catch (final SQLException e) {
                _db2.conn.rollback();
                log.fatal("更新処理中にエラー! rollback した。");
                throw e;
            }
        }
        _db2.commit();
        _countter99 = 0;
        for (int strSep = 60001; strSep < 120000; strSep += 5000) {
            final int endSep = strSep + 4999;
            final List list = loadIcass(strSep, endSep);
            log.debug(ICASS_TABLE + "データ件数=" + list.size());
            log.debug(String.valueOf(strSep) + "～" + String.valueOf(endSep));

            try {
                saveKnj(list);
            } catch (final SQLException e) {
                _db2.conn.rollback();
                log.fatal("更新処理中にエラー! rollback した。");
                throw e;
            }
        }
        _db2.commit();
    }

    private void setKyoukaHenkan() {
        _kyouka_henkan.put("工業", "42");
        _kyouka_henkan.put("商業", "12");
        _kyouka_henkan.put("農業", "41");
        _kyouka_henkan.put("学校設定", "13");
        _kyouka_henkan.put("その他", "81");
        _kyouka_henkan.put("家庭", "45");
        _kyouka_henkan.put("専門科目", "81");
        _kyouka_henkan.put("音楽", "51");
        _kyouka_henkan.put("情報", "47");
        _kyouka_henkan.put("外国語", "8");
        _kyouka_henkan.put("体育", "50");
        _kyouka_henkan.put("英語", "53");
        _kyouka_henkan.put("福祉", "14");
        _kyouka_henkan.put("宗教", "13");
        _kyouka_henkan.put("専門", "81");
        _kyouka_henkan.put("総合", "11");
        _kyouka_henkan.put("美術", "52");
        _kyouka_henkan.put("国際理解", "13");
        _kyouka_henkan.put("国語", "1");
        _kyouka_henkan.put("特別活動", "82");
        _kyouka_henkan.put("工業(電気)", "42");
        _kyouka_henkan.put("調理", "45");
        _kyouka_henkan.put("理数", "15");
        _kyouka_henkan.put("保健体育", "6");
        _kyouka_henkan.put("看護", "17");
        _kyouka_henkan.put("水産", "44");
        _kyouka_henkan.put("専門音楽", "51");
        _kyouka_henkan.put("数学", "4");
        _kyouka_henkan.put("　", "41");
        _kyouka_henkan.put("工業（機械）", "42");
        _kyouka_henkan.put("社会福祉", "14");
        _kyouka_henkan.put("専門教科", "81");
        _kyouka_henkan.put("選択", "13");
        _kyouka_henkan.put("選択教科", "13");
        _kyouka_henkan.put("学校設定教科", "13");
        _kyouka_henkan.put("教養", "13");
        _kyouka_henkan.put("国際", "13");
        _kyouka_henkan.put("理科", "5");
        _kyouka_henkan.put("映像芸術", "55");
        _kyouka_henkan.put("音楽専門", "51");
        _kyouka_henkan.put("学校設定科目", "13");
        _kyouka_henkan.put("国際コミュニケーション", "13");
        _kyouka_henkan.put("自動車", "42");
        _kyouka_henkan.put("地歴", "2");
        _kyouka_henkan.put("特活", "82");
        _kyouka_henkan.put("保育", "45");
        _kyouka_henkan.put("共通", "81");
        _kyouka_henkan.put("芸術", "55");
        _kyouka_henkan.put("産業社会", "16");
        _kyouka_henkan.put("商船", "81");
        _kyouka_henkan.put("生活科学系列", "81");
        _kyouka_henkan.put("総合文明", "13");
        _kyouka_henkan.put("美術工芸", "52");
        _kyouka_henkan.put("福祉ライフ", "14");
        _kyouka_henkan.put("ＩＴ", "47");
        _kyouka_henkan.put("芸能文化", "81");
        _kyouka_henkan.put("工業（機械テクノロジー）", "42");
        _kyouka_henkan.put("工業（機械科）", "42");
        _kyouka_henkan.put("工業（電子機械）", "42");
        _kyouka_henkan.put("国際流通", "12");
        _kyouka_henkan.put("産業科学", "13");
        _kyouka_henkan.put("聖書", "13");
        _kyouka_henkan.put("専門教育に関する各教科・科目", "81");
        _kyouka_henkan.put("総合学習", "11");
        _kyouka_henkan.put("土曜特別講座", "81");
        _kyouka_henkan.put("Ａ", "41");
        _kyouka_henkan.put("ＨＲ", "83");
        _kyouka_henkan.put("ＩＴビジネス", "12");
        _kyouka_henkan.put("ＳＳＨ", "81");
        _kyouka_henkan.put("演習", "81");
        _kyouka_henkan.put("基礎", "81");
        _kyouka_henkan.put("公民", "3");
        _kyouka_henkan.put("工業（電子）", "42");
        _kyouka_henkan.put("工業（理工環境）", "42");
        _kyouka_henkan.put("国数英基礎", "81");
        _kyouka_henkan.put("鉄道専門", "81");
        _kyouka_henkan.put("特別教育活動", "82");
        _kyouka_henkan.put("美術専門", "52");
        _kyouka_henkan.put("キャリア", "81");
        _kyouka_henkan.put("クリエイティブスタディ", "81");
        _kyouka_henkan.put("コミュニケーション", "81");
        _kyouka_henkan.put("ゼミナール", "81");
        _kyouka_henkan.put("ホームルーム", "83");
        _kyouka_henkan.put("ライフデザイン", "45");
        _kyouka_henkan.put("学校裁量", "13");
        _kyouka_henkan.put("学校設置科目", "13");
        _kyouka_henkan.put("韓国語", "8");
        _kyouka_henkan.put("機械システム", "42");
        _kyouka_henkan.put("現代文明論", "13");
        _kyouka_henkan.put("自由選択科目", "13");
        _kyouka_henkan.put("社会産業", "13");
        _kyouka_henkan.put("情報科学", "47");
        _kyouka_henkan.put("人文", "13");
        _kyouka_henkan.put("生活・文化", "45");
        _kyouka_henkan.put("総合的な学習の時間", "11");
        _kyouka_henkan.put("他", "81");
        _kyouka_henkan.put("道徳", "13");
        _kyouka_henkan.put("特設", "13");
        _kyouka_henkan.put("礼法", "13");
        _kyouka_henkan.put("C A D", "42");
        _kyouka_henkan.put("LHR", "83");
        _kyouka_henkan.put("O.A", "81");
        _kyouka_henkan.put("ビジュアルデザイン", "81");
        _kyouka_henkan.put("ホーム・ルーム", "83");
        _kyouka_henkan.put("ホームルーム活動", "83");
        _kyouka_henkan.put("モラル", "13");
        _kyouka_henkan.put("医療看護", "17");
        _kyouka_henkan.put("音楽実技1", "51");
        _kyouka_henkan.put("家庭（専門）", "45");
        _kyouka_henkan.put("科学技術", "42");
        _kyouka_henkan.put("海洋", "44");
        _kyouka_henkan.put("絵画", "52");
        _kyouka_henkan.put("学活", "83");
        _kyouka_henkan.put("学校外学修", "11");
        _kyouka_henkan.put("学校外設定", "81");
        _kyouka_henkan.put("観光", "19");
        _kyouka_henkan.put("基礎講座", "81");
        _kyouka_henkan.put("郷土学S", "81");
        _kyouka_henkan.put("現代ビジネス", "12");
        _kyouka_henkan.put("工業・理工学", "42");
        _kyouka_henkan.put("産業", "12");
        _kyouka_henkan.put("産業技術", "12");
        _kyouka_henkan.put("産業社会と人間", "16");
        _kyouka_henkan.put("産社", "16");
        _kyouka_henkan.put("自由選択科目1", "13");
        _kyouka_henkan.put("実践行動学", "81");
        _kyouka_henkan.put("情報処理1", "47");
        _kyouka_henkan.put("職業", "11");
        _kyouka_henkan.put("職業教育I", "11");
        _kyouka_henkan.put("人間と科学", "13");
        _kyouka_henkan.put("人間科", "13");
        _kyouka_henkan.put("人生と社会", "13");
        _kyouka_henkan.put("設定", "13");
        _kyouka_henkan.put("専門国語", "1");
        _kyouka_henkan.put("前籍高校", "81");
        _kyouka_henkan.put("創造的特別活動", "82");
        _kyouka_henkan.put("相互交流", "81");
        _kyouka_henkan.put("総合の学習", "11");
        _kyouka_henkan.put("総合科学", "81");
        _kyouka_henkan.put("総合基礎", "81");
        _kyouka_henkan.put("総合実習", "41");
        _kyouka_henkan.put("体験学習", "11");
        _kyouka_henkan.put("地理", "2");
        _kyouka_henkan.put("地理歴史", "2");
        _kyouka_henkan.put("電気基礎", "42");
        _kyouka_henkan.put("電気製図", "42");
        _kyouka_henkan.put("道義", "81");
        _kyouka_henkan.put("徳育", "81");
        _kyouka_henkan.put("特HR", "83");
        _kyouka_henkan.put("特ＨＲ", "83");
        _kyouka_henkan.put("特教", "82");
        _kyouka_henkan.put("特別活動の研究", "82");
        _kyouka_henkan.put("特別講座", "81");
        _kyouka_henkan.put("特別設定", "81");
        _kyouka_henkan.put("美容", "81");
        _kyouka_henkan.put("表現", "81");
        _kyouka_henkan.put("文化", "81");
        _kyouka_henkan.put("保育福祉", "45");
        _kyouka_henkan.put("奉仕", "81");
        _kyouka_henkan.put("留学", "81");
        _kyouka_henkan.put("礼拝", "13");
        _kyouka_henkan.put("礼拝・終礼・教養講座", "13");
        _kyouka_henkan.put("連携", "81");
        _kyouka_henkan.put("労作", "13");
    }

    private List loadIcass(final int strSep, final int endSep) throws SQLException {
        final List rtn = new ArrayList();

        // SQL実行
        final List result;
        try {
            final String sql = getSql(strSep, endSep);
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: データ量が多いので溢れてしまう。対策を考えろ!
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        // 結果の処理
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final AnotherSchoolGetcreditsDat anotherSchoolGetcreditsDat = new AnotherSchoolGetcreditsDat(map);
            rtn.add(anotherSchoolGetcreditsDat);
        }
        return rtn;
    }

    private String getSql(final int strSep, final int endSep) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ANOTHER_KAMOKU AS ( ");
        stb.append(" SELECT  ");
        stb.append("     RANK() OVER(PARTITION BY SHUTOKU_NENDO, KYOIKUKATEI_TEKIYO_NENDO_CODE, KYOKA_CODE ");
        stb.append("                 ORDER BY T1.KYOIKUKATEI_TEKIYO_NENDO_CODE,KYOKA_CODE,KAMOKU_NAME) AS KAMOKU_CODE, ");
        stb.append("     T1.KYOKA_NAME,  ");
        stb.append("     T1.KAMOKU_NAME,  ");
        stb.append("     T1.SHUTOKU_NENDO,  ");
        stb.append("     T1.TANI_SHUTOKU_SHUDAN_CODE,  ");
        stb.append("     T1.KYOIKUKATEI_TEKIYO_NENDO_CODE,  ");
        stb.append("     T1.KYOKA_CODE ");
        stb.append(" FROM  ");
        stb.append("     SEITO_JIKOGAI_RISHU_KAMOKU T1  ");
        stb.append(" WHERE  ");
        stb.append("     VALUE(T1.TANI_SHUTOKU_SHUDAN_CODE, '') IN ('01', '02', '03', '04', '11', '99')  ");
        stb.append("     AND VALUE(T1.KAMOKU_CODE, '') = '' ");
        stb.append(" GROUP BY  ");
        stb.append("     T1.KYOKA_NAME,  ");
        stb.append("     T1.KAMOKU_NAME,  ");
        stb.append("     T1.SHUTOKU_NENDO,  ");
        stb.append("     T1.TANI_SHUTOKU_SHUDAN_CODE,  ");
        stb.append("     T1.KYOIKUKATEI_TEKIYO_NENDO_CODE,  ");
        stb.append("     T1.KYOKA_CODE ");
        stb.append(" ), MK AS (  ");
        stb.append(" SELECT  ");
        stb.append("     CAST(ROW_NUMBER() OVER() AS INTEGER) AS COUN,   ");
        stb.append("     T1.SHIGANSHA_RENBAN,  ");
        stb.append("     T1.KYOKA_NAME,  ");
        stb.append("     T1.KAMOKU_NAME,  ");
        stb.append("     T1.SHUTOKU_NENDO,  ");
        stb.append("     CASE VALUE(T1.TANI_SHUTOKU_SHUDAN_CODE, '')  ");
        stb.append("          WHEN '01' THEN '0'  ");
        stb.append("          WHEN '02' THEN '1'  ");
        stb.append("          WHEN '03' THEN '9'  ");
        stb.append("          WHEN '04' THEN '0'  ");
        stb.append("          WHEN '11' THEN '9'  ");
        stb.append("          WHEN '12' THEN 'null'  ");
        stb.append("          WHEN '99' THEN '9'  ");
        stb.append("          ELSE '9'  ");
        stb.append("     END AS GET_METHOD,  ");
        stb.append("     MAX(L2.SCHOOL_CD) AS SCHOOL_CD,  ");
        stb.append("     SUM(CASE WHEN T1.FURIKAE_TANI = ''  ");
        stb.append("              THEN 0  ");
        stb.append("              ELSE CAST(T1.FURIKAE_TANI AS SMALLINT)  ");
        stb.append("         END  ");
        stb.append("     ) AS FURIKAE_TANI,  ");
        stb.append("     MAX(CASE WHEN T1.HYOTEI = ''  ");//←用修正。「SUM」でわなくてMAX。宮城さんの返事があり次第。修正。。。。
        stb.append("              THEN 0  ");
        stb.append("              WHEN T1.HYOTEI = 'A'  ");
        stb.append("              THEN 0  ");
        stb.append("              WHEN T1.HYOTEI = 'B'  ");
        stb.append("              THEN 0  ");
        stb.append("              WHEN T1.HYOTEI = 'C'  ");
        stb.append("              THEN 0  ");
        stb.append("              ELSE CAST(T1.HYOTEI AS INTEGER)  ");
        stb.append("         END  ");
        stb.append("     ) AS HYOTEI,  ");
        stb.append("     T1.KYOIKUKATEI_TEKIYO_NENDO_CODE,  ");
        stb.append("     L1.NAMECD2 AS CURRICULUM_CD,  ");
        stb.append("     T1.KYOKA_CODE,  ");
        stb.append("     CASE WHEN L3.KAMOKU_CODE IS NOT NULL ");
        stb.append("          THEN '99' || CAST(L3.KAMOKU_CODE AS CHAR(3)) ");
        stb.append("          ELSE T1.KAMOKU_CODE ");
        stb.append("     END AS KAMOKU_CODE,  ");
        stb.append("     CASE WHEN MAX(T1.HYOTEI) = 'A' OR MAX(T1.HYOTEI) = 'B' OR MAX(T1.HYOTEI) = 'C'  ");
        stb.append("          THEN MAX(T1.BIKO) || ' ' || MAX(T1.HYOTEI)  ");
        stb.append("          ELSE MAX(T1.BIKO)  ");
        stb.append("     END AS BIKO,  ");
        stb.append("     COUNT(*) AS CNT  ");
        stb.append(" FROM  ");
        stb.append("     SEITO_JIKOGAI_RISHU_KAMOKU T1  ");
        stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'W002'  ");
        stb.append("          AND T1.KYOIKUKATEI_TEKIYO_NENDO_CODE BETWEEN L1.NAMESPARE1 AND L1.NAMESPARE2  ");
        stb.append("     LEFT JOIN KOKO_YOMIKAE L2 ON T1.KOKO_KANRI_NO = L2.KOKO_KANRI_NO ");
        stb.append("     LEFT JOIN ANOTHER_KAMOKU L3 ON T1.KYOKA_NAME = L3.KYOKA_NAME ");
        stb.append("          AND T1.KAMOKU_NAME = L3.KAMOKU_NAME ");
        stb.append("          AND T1.SHUTOKU_NENDO = L3.SHUTOKU_NENDO ");
        stb.append("          AND T1.TANI_SHUTOKU_SHUDAN_CODE = L3.TANI_SHUTOKU_SHUDAN_CODE ");
        stb.append("          AND T1.KYOIKUKATEI_TEKIYO_NENDO_CODE = L3.KYOIKUKATEI_TEKIYO_NENDO_CODE ");
        stb.append("          AND T1.KYOKA_CODE = L3.KYOKA_CODE ");
        stb.append(" WHERE  ");
        stb.append("     VALUE(T1.TANI_SHUTOKU_SHUDAN_CODE, '') IN ('01', '02', '03', '04', '11', '99')  ");
        stb.append(" GROUP BY  ");
        stb.append("     T1.SHIGANSHA_RENBAN,  ");
        stb.append("     T1.KYOKA_NAME,  ");
        stb.append("     T1.KAMOKU_NAME,  ");
        stb.append("     T1.SHUTOKU_NENDO,  ");
        stb.append("     CASE VALUE(T1.TANI_SHUTOKU_SHUDAN_CODE, '')  ");
        stb.append("          WHEN '01' THEN '0'  ");
        stb.append("          WHEN '02' THEN '1'  ");
        stb.append("          WHEN '03' THEN '9'  ");
        stb.append("          WHEN '04' THEN '0'  ");
        stb.append("          WHEN '11' THEN '9'  ");
        stb.append("          WHEN '12' THEN 'null'  ");
        stb.append("          WHEN '99' THEN '9'  ");
        stb.append("          ELSE '9'  ");
        stb.append("     END,  ");
        stb.append("     T1.KYOIKUKATEI_TEKIYO_NENDO_CODE,  ");
        stb.append("     L1.NAMECD2,  ");
        stb.append("     T1.KYOKA_CODE,  ");
        stb.append("     CASE WHEN L3.KAMOKU_CODE IS NOT NULL ");
        stb.append("          THEN '99' || CAST(L3.KAMOKU_CODE AS CHAR(3)) ");
        stb.append("          ELSE T1.KAMOKU_CODE ");
        stb.append("     END  ");
        stb.append(" )  ");
        stb.append(" SELECT  ");
        stb.append("     T1.COUN,   ");
        stb.append("     T1.SHIGANSHA_RENBAN,  ");
        stb.append("     T1.KYOKA_NAME,  ");
        stb.append("     T1.KAMOKU_NAME,  ");
        stb.append("     T1.SHUTOKU_NENDO,  ");
        stb.append("     T1.GET_METHOD,  ");
        stb.append("     T1.SCHOOL_CD,  ");
        stb.append("     T1.FURIKAE_TANI,  ");
        stb.append("     T1.HYOTEI,  ");
        stb.append("     T1.KYOIKUKATEI_TEKIYO_NENDO_CODE,  ");
        stb.append("     CASE VALUE(T1.CURRICULUM_CD, '')  ");
        stb.append("        WHEN '' THEN L2.NAMECD2 ");
        stb.append("        ELSE T1.CURRICULUM_CD ");
        stb.append("     END AS CURRICULUM_CD, ");
        stb.append("     T1.KYOKA_CODE,  ");
        stb.append("     RTRIM(T1.KAMOKU_CODE) AS KAMOKU_CODE,  ");
        stb.append("     T1.BIKO  ");
        stb.append(" FROM  ");
        stb.append("     MK T1 ");
        stb.append("     LEFT JOIN SEITO L1 ON L1.SHIGANSHA_RENBAN = T1.SHIGANSHA_RENBAN ");
        stb.append("     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'W002'  ");
        stb.append("          AND L1.KYOIKUKATEI_TEKIYO_NENDO_CODE BETWEEN L2.NAMESPARE1 AND L2.NAMESPARE2  ");
        stb.append(" WHERE COUN BETWEEN " + strSep +  " AND " + endSep + " ");
        stb.append(" ORDER BY COUN ");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class AnotherSchoolGetcreditsDat {
        final String _shiganshaRenban;
        final String _year;
        final String _getDiv;
        final String _applicantno;
        final String _getMethod;
        final String _classcd;
        final String _curriculumCd;
        final String _subclasscd;
        final String _creditCurriculumCd;
        final String _creditAdmitscd;
        final String _subclassname;
        final String _subclassabbv;
        final Integer _getCredit;
        final Integer _valuation;
        final String _formerRegSchoolcd;
        final String _getDate;
        final String _remark;

        public AnotherSchoolGetcreditsDat(final Map map) {
            _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
            _year = (String) map.get("SHUTOKU_NENDO");
            _getDiv = "1";  // TODO:とりあえず
            _applicantno = _param.getApplicantNo(_shiganshaRenban);
            _getMethod = (String) map.get("GET_METHOD");
            String classCd = (String) map.get("KYOKA_CODE");
            String curriculumCd = (String) map.get("CURRICULUM_CD");
            String subclassCd = (String) map.get("KAMOKU_CODE");
            if (null == classCd || (classCd).equals("")) {
                classCd = "99";
                final String kyokaName = (String) map.get("KYOKA_NAME");
                if (_kyouka_henkan.containsKey(kyokaName)) {
                    classCd = (String) _kyouka_henkan.get(kyokaName);
                }
            }
            if (null == curriculumCd || (curriculumCd).equals("")) {
                curriculumCd = "2"; // TODO:とりあえず固定
            }
            if (null == subclassCd || (subclassCd).equals("")) {
                subclassCd = _subClassCd99Format.format(_countter99);
                _countter99++;
            }
            _classcd = classCd;
            _curriculumCd = curriculumCd;
            if (subclassCd.startsWith("99")) {
                _subclasscd = "99" + _subClassCd.format(Integer.parseInt(subclassCd.substring(2)));
            } else {
                _subclasscd = _classcd + _subClassCd.format(Integer.parseInt(subclassCd));
            }
            _creditCurriculumCd = "2";  // TODO:とりあえず固定
            _creditAdmitscd = "000000"; // TODO:とりあえずオール'0'
            _subclassname = (String) map.get("KAMOKU_NAME");
            _subclassabbv = "";    // TODO:未定
            _getCredit = (Integer) map.get("FURIKAE_TANI");
            _valuation = (Integer) map.get("HYOTEI");
            _formerRegSchoolcd = (String) map.get("SCHOOL_CD");
            _getDate = "";    // TODO:未定
            final String biko = (String) map.get("BIKO");
            if (biko != null && biko.length() > 30) {
                _remark = biko.substring(0,30);
                log.debug(_remark);
                log.debug("********************" + _shiganshaRenban);
            } else {
                _remark = biko;
            }
        }
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table ANOTHER_SCHOOL_GETCREDITS_DAT

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
        GET_DIV                        SYSIBM    VARCHAR                   1     0 いいえ
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 いいえ
        GET_METHOD                     SYSIBM    VARCHAR                   1     0 いいえ
        CLASSCD                        SYSIBM    VARCHAR                   2     0 いいえ
        CURRICULUM_CD                  SYSIBM    VARCHAR                   1     0 いいえ
        SUBCLASSCD                     SYSIBM    VARCHAR                   6     0 いいえ
        CREDIT_CURRICULUM_CD           SYSIBM    VARCHAR                   1     0 いいえ
        CREDIT_ADMITSCD                SYSIBM    VARCHAR                   6     0 いいえ
        SUBCLASSNAME                   SYSIBM    VARCHAR                  60     0 はい
        SUBCLASSABBV                   SYSIBM    VARCHAR                  15     0 はい
        GET_CREDIT                     SYSIBM    SMALLINT                  2     0 はい
        VALUATION                      SYSIBM    SMALLINT                  2     0 はい
        FORMER_REG_SCHOOLCD            SYSIBM    VARCHAR                  11     0 はい
        GET_DATE                       SYSIBM    DATE                      4     0 はい
        REMARK                         SYSIBM    VARCHAR                  90     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
          18 レコードが選択されました。
     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final AnotherSchoolGetcreditsDat anotherSchoolGetcreditsDat = (AnotherSchoolGetcreditsDat) it.next();
            final String insSql = getInsertSql(anotherSchoolGetcreditsDat);
            try{
                _db2.stmt.executeUpdate(insSql);
            } catch(SQLException e) {
                log.debug(insSql);
                log.error("SQLException: " + e.getMessage()+ ":" + ((SQLException)e).getSQLState());
                throw e;
            }
            totalCount++;
        }
        DbUtils.closeQuietly(rs);
        log.warn("挿入件数=" + totalCount);
    }

    private String getInsertSql(final AnotherSchoolGetcreditsDat anotherSchoolGetcreditsDat) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO ANOTHER_SCHOOL_GETCREDITS_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._year) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._getDiv) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._applicantno) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._getMethod) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._classcd) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._curriculumCd) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._subclasscd) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._creditCurriculumCd) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._creditAdmitscd) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._subclassname) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._subclassabbv) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._getCredit) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._valuation) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._formerRegSchoolcd) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._getDate) + ", ");
        stb.append(" " + getInsertVal(anotherSchoolGetcreditsDat._remark) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

