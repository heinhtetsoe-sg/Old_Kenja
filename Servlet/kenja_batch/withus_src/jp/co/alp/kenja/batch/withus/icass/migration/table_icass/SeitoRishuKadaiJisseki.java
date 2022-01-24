// kanji=漢字
/*
 * $Id: SeitoRishuKadaiJisseki.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/18 15:03:34 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration.table_icass;

import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.withus.Curriculum;
import jp.co.alp.kenja.batch.withus.icass.migration.AbstractKnj;
import jp.co.alp.kenja.batch.withus.icass.migration.Param;

/**
 * 生徒履修課題実績
 * <pre>
[takaesu@withus takaesu]$ db2 describe table seito_rishu_kadai_jisseki

                               タイプ・
列名                           スキーマ  タイプ名           長さ    位取り NULL
------------------------------ --------- ------------------ -------- ----- ------
SHIGANSHA_RENBAN               SYSIBM    VARCHAR                  10     0 いいえ
KATEI_CODE                     SYSIBM    VARCHAR                   1     0 いいえ
KYOIKUKATEI_TEKIYO_NENDO_CODE  SYSIBM    VARCHAR                   4     0 いいえ
NENDO_CODE                     SYSIBM    VARCHAR                   4     0 いいえ
KYOKA_CODE                     SYSIBM    VARCHAR                   5     0 いいえ
KAMOKU_CODE                    SYSIBM    VARCHAR                   5     0 いいえ
RISHU_KIKAN_CODE               SYSIBM    VARCHAR                   1     0 いいえ
RISHU_KADAI_SHUBETSU_CODE      SYSIBM    VARCHAR                   1     0 いいえ
JISSHI_NO                      SYSIBM    VARCHAR                  10     0 いいえ
KADAI_LEVEL                    SYSIBM    VARCHAR                   1     0 はい
KADAI_NO                       SYSIBM    VARCHAR                  10     0 はい
TEISHUTSU_NENGAPPI             SYSIBM    VARCHAR                  10     0 はい
TOKUTEN                        SYSIBM    VARCHAR                  10     0 はい
SHOKAI_TOKUTEN                 SYSIBM    VARCHAR                  10     0 はい
TSUISHI_TOKUTEN                SYSIBM    VARCHAR                  10     0 はい
HYOTEI                         SYSIBM    VARCHAR                   1     0 はい
TEISHUTSU_KAISU                SYSIBM    VARCHAR                  10     0 はい
HYOKA_GAKKO_KANKEISHA_NO       SYSIBM    VARCHAR                   6     0 はい
TOROKU_DATE                    SYSIBM    VARCHAR                  20     0 はい
KOSHIN_DATE                    SYSIBM    VARCHAR                  20     0 はい

  20 レコードが選択されました。
  </pre>
 */

public class SeitoRishuKadaiJisseki {
    /*pkg*/static final Log log = LogFactory.getLog(SeitoRishuKadaiJisseki.class);
    final Param _param;

    final String _shiganshaRenban;
    final String _kateiCode;
    final String _kyouikukatei適用年度;
    final String _nendo;
    final String _kyoka;
    final String _kamoku;
    final String _rishuKikanCode;
    final String _rishuKadaiShubetsuCode;
    final String _jisshiNo;
    final String _kadaiLevel;
    final String _kadaiNo;
    final String _teishutsuNengappi;
    final String _tokuten;
    final String _shokaiTokuten;
    final String _tsuishiTokuten;
    final String _teishutsuKaisu;
    final String _hyokaGakkoKankeishaNo;
    final String _torokuDate;
    final String _koshinDate;

    public SeitoRishuKadaiJisseki(final Param param, final Map map) {
        _param = param;

        _shiganshaRenban = (String) map.get("shigansha_renban");
        _kateiCode = (String) map.get("KATEI_CODE");
        _kyouikukatei適用年度 = (String) map.get("kyoikukatei_tekiyo_nendo_code");
        _nendo = (String) map.get("nendo_code");
        _kyoka = (String) map.get("kyoka_code");
        final String kamokuCode = (String) map.get("kamoku_code");
        _kamoku = AbstractKnj._subClassCdFormat.format(Integer.valueOf(kamokuCode));
        _rishuKikanCode = (String) map.get("RISHU_KIKAN_CODE");
        _rishuKadaiShubetsuCode = (String) map.get("RISHU_KADAI_SHUBETSU_CODE");
        _jisshiNo = (String) map.get("jisshi_no");
        _kadaiLevel = (String) map.get("KADAI_LEVEL");
        _kadaiNo = (String) map.get("KADAI_NO");
        _teishutsuNengappi = (String) map.get("teishutsu_nengappi");
        _tokuten = (String) map.get("TOKUTEN");
        _shokaiTokuten = (String) map.get("shokai_tokuten");
        _teishutsuKaisu = (String) map.get("TEISHUTSU_KAISU");
        _tsuishiTokuten = (String) map.get("tsuishi_tokuten");
        _hyokaGakkoKankeishaNo = (String) map.get("HYOKA_GAKKO_KANKEISHA_NO");
        _torokuDate = (String) map.get("TOROKU_DATE");
        _koshinDate = (String) map.get("KOSHIN_DATE");
    }

    public SeitoRishuKadaiJisseki(final Param param, final ResultSet rs) throws SQLException {
        _param = param;

        _shiganshaRenban = rs.getString("SHIGANSHA_RENBAN");
        _kateiCode = rs.getString("KATEI_CODE");
        _kyouikukatei適用年度 = rs.getString("KYOIKUKATEI_TEKIYO_NENDO_CODE");
        _nendo = rs.getString("NENDO_CODE");
        _kyoka = rs.getString("KYOKA_CODE");
        final String kamokuCode = rs.getString("KAMOKU_CODE");
        _kamoku = AbstractKnj._subClassCdFormat.format(Integer.valueOf(kamokuCode));

        _rishuKikanCode = rs.getString("RISHU_KIKAN_CODE");
        _rishuKadaiShubetsuCode = rs.getString("RISHU_KADAI_SHUBETSU_CODE");
        _jisshiNo = rs.getString("JISSHI_NO");
        _kadaiLevel = rs.getString("KADAI_LEVEL");
        _kadaiNo = rs.getString("KADAI_NO");
        _teishutsuNengappi = rs.getString("TEISHUTSU_NENGAPPI");
        _tokuten = rs.getString("TOKUTEN");
        _shokaiTokuten = rs.getString("SHOKAI_TOKUTEN");
        _tsuishiTokuten = rs.getString("TSUISHI_TOKUTEN");
//        rs.getString("HYOTEI");
        _teishutsuKaisu = rs.getString("TEISHUTSU_KAISU");
        _hyokaGakkoKankeishaNo = rs.getString("HYOKA_GAKKO_KANKEISHA_NO");
        _torokuDate = rs.getString("TOROKU_DATE");
        _koshinDate = rs.getString("KOSHIN_DATE");
    }

    public Object[] toRecReportDat() {
        final String schregno = _param.getSchregno(_shiganshaRenban);
        final String curriculumCd = Curriculum.getCurriculumCd(_kyouikukatei適用年度);
        final String subclassCd = _kyoka + AbstractKnj._subClassCdFormat.format(Integer.parseInt(_kamoku));
        final Integer reportSeq = _jisshiNo == null ? null : Integer.valueOf(_jisshiNo);
        java.sql.Date commitedDate1 = null;
        try {
            commitedDate1 = (_teishutsuNengappi == null || "".equals(_teishutsuNengappi)) ? null : java.sql.Date.valueOf(_teishutsuNengappi);
        } catch (final Exception e) {
            log.debug("  ");
        }
        final java.sql.Date commitedDate2 = StringUtils.isEmpty(_tsuishiTokuten) ? null : commitedDate1;
        final Integer committedScore1 = (_shokaiTokuten == null || "".equals(_shokaiTokuten)) ? null : Integer.valueOf(_shokaiTokuten);
        final Integer committedScore2 = (_tsuishiTokuten == null || "".equals(_tsuishiTokuten)) ? null : Integer.valueOf(_tsuishiTokuten);
        final Object[] rtn = {
                _nendo,
                _kyoka,
                curriculumCd,
                subclassCd,
                schregno,
                reportSeq,
                commitedDate1,
                commitedDate2,
                committedScore1,
                committedScore2,
                _hyokaGakkoKankeishaNo,
                Param.REGISTERCD,
        };
        return rtn;
    }

    public Object[] toRecTestDat() {
        final String schregno = _param.getSchregno(_shiganshaRenban);
        final String curriculumCd = Curriculum.getCurriculumCd(_kyouikukatei適用年度);
        final String subclassCd = _kyoka + AbstractKnj._subClassCdFormat.format(Integer.parseInt(_kamoku));
        final String month = ("1".equals(_jisshiNo)) ? "09" : "03";
        final String score = "".equals(_tokuten) ? null : _tokuten;

        final Object[] rtn = {
                _nendo,
                _kyoka,
                curriculumCd,
                subclassCd,
                schregno,
                month,
                score,
                _hyokaGakkoKankeishaNo,
                Param.REGISTERCD,
        };
        return rtn;
    }

    public String getRecTestDatInsertSql() {
        final String schregno = _param.getSchregno(_shiganshaRenban);
        final String curriculumCd = Curriculum.getCurriculumCd(_kyouikukatei適用年度);
        final String month = ("1".equals(_jisshiNo)) ? "09" : "03";

        final String insertSql = "INSERT INTO rec_test_dat VALUES("
            + "'" + _nendo + "',"
            + "'" + _kyoka + "',"
            + "'" + curriculumCd + "',"
            + "'" + _kamoku + "',"
            + "'" + schregno + "',"
            + "'" + month + "',"
            + _tokuten + ","
            + "'" + _hyokaGakkoKankeishaNo + "',"//CREATOR
            + "'" + Param.REGISTERCD + "',"
            + "current timestamp)";
        return insertSql;
    }
} // SeitoRishuKadaiJisseki
// eof
