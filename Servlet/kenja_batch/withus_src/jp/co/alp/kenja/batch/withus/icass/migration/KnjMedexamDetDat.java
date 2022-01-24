// kanji=漢字
/*
 * $Id: KnjMedexamDetDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * MEDEXAM_DET_DAT <賢者のテーブル名に書き換えてください。例) REC_REPORT_DAT>を作る。
 * @author takaesu
 * @version $Id: KnjMedexamDetDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjMedexamDetDat extends AbstractKnj implements IKnj{
    /*pkg*/static final Log log = LogFactory.getLog(KnjMonthKeepingMoneyDat.class);
    private static final String KNJTABLE = "MEDEXAM_DET_DAT";
    private static final String IJONASI = "異常なし";
    private static final String NANCHO = "難聴";

    public KnjMedexamDetDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "健康診断詳細データ"; }

    void migrate() throws SQLException {
        final List list = loadIcass();

        log.debug("データ件数=" + list.size());

        _runner.listToKnj(list, KNJTABLE, this);

    }

    private List loadIcass() throws SQLException{
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     NENDO_CODE, ");
        stb.append("     SHIGANSHA_RENBAN, ");
        stb.append("     SHINCHO, ");
        stb.append("     TAIJU, ");
        stb.append("     ZAKO, ");
        stb.append("     L_RAGAN_SHIRYOKU, ");
        stb.append("     R_RAGAN_SHIRYOKU, ");
        stb.append("     L_KYOSEI_SHIRYOKU, ");
        stb.append("     R_KYOSEI_SHIRYOKU, ");
        stb.append("     L_CHORYOKU, ");
        stb.append("     R_CHORYOKU, ");
        stb.append("     KEKKAKU_KENSA_SHOKEN, ");
        stb.append("     SHINZO_SHIPPEI_SHOKEN, ");
        stb.append("     NYO_1_TO, ");
        stb.append("     NYO_1_TANPAKU, ");
        stb.append("     NYO_1_SENKETSU, ");
        stb.append("     NYO_2_TO, ");
        stb.append("     NYO_2_TANPAKU, ");
        stb.append("     NYO_2_SENKETSU, ");
        stb.append("     SONOTA_SHIPPEI, ");
        stb.append("     TANTO_GAKKOI_SHOKEN, ");
        stb.append("     TANTO_KYOIN_SHOKEN ");
        stb.append(" FROM ");
        stb.append("     SEITO_KENKO_SHINDAN_KEKKA ");

        final List result;
        try {
            result = (List) _runner.query(_db2.conn, stb.toString(), _handler);
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        return result;
    }

    public Object[] mapToArray(Map map) {
        final String _year =            (String) map.get("NENDO_CODE");
        final String _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
        final String _schregno =        _param.getSchregno(_shiganshaRenban);
        final Double _height =          map.get("SHINCHO") != null ? Double.valueOf((String) map.get("SHINCHO")) : null;
        final Double _weight =          map.get("TAIJU") != null ? Double.valueOf((String) map.get("TAIJU")) : null;
        final Double _sitheight =       map.get("ZAKO") != null ? Double.valueOf((String) map.get("ZAKO")) : null;
        final String l_barevision =     (String) map.get("L_RAGAN_SHIRYOKU");
        final String r_barevision =     (String) map.get("R_RAGAN_SHIRYOKU");
        final String l_vision =         (String) map.get("L_KYOSEI_SHIRYOKU");
        final String r_vision =         (String) map.get("R_KYOSEI_SHIRYOKU");
        final String l_ear =            (String) map.get("L_CHORYOKU");
        final String r_ear =            (String) map.get("R_CHORYOKU");
        String _l_ear;
        String _r_ear;
        String _l_barevision = "a";
        String _l_barevision_mark = "a";
        String _r_barevision;
        String _r_barevision_mark;
        String _l_vision;
        String _l_vision_mark;
        String _r_vision;
        String _r_vision_mark;


        float l_bv;
        boolean flag_l_barevision = false;
        if (l_barevision != null) {
            try {
                l_bv = Float.parseFloat(l_barevision);
                flag_l_barevision = true;
            } catch (final NumberFormatException e) {
                flag_l_barevision = false;
                l_bv = 0.0f;
            }
            if (l_barevision != null && l_barevision.equals("A")) {
                _l_barevision = null;
                _l_barevision_mark = "A";
            } else if (l_barevision != null && l_barevision.equals("B")) {
                _l_barevision = null;
                _l_barevision_mark = "B";
            } else if (l_barevision != null && l_barevision.equals("C")) {
                _l_barevision = null;
                _l_barevision_mark = "C";
            } else if (l_barevision != null && l_barevision.equals("D")) {
                _l_barevision = null;
                _l_barevision_mark = "D";
            } else if (l_barevision != null && flag_l_barevision){
                if (l_bv >= 0.999999) {
                    _l_barevision = l_barevision;
                    _l_barevision_mark = "A";
                } else if (l_bv < 1.0 && l_bv >= 0.6999999) {
                    _l_barevision = l_barevision;
                    _l_barevision_mark = "B";
                } else if (l_bv < 0.7 && l_bv >= 0.29999999) {
                    _l_barevision = l_barevision;
                    _l_barevision_mark = "C";
                } else {
                    _l_barevision = l_barevision;
                    _l_barevision_mark = "D";
                }
            } else {
                _l_barevision = null;
                _l_barevision_mark = null;
            }
        } else {
            _l_barevision = null;
            _l_barevision_mark = null;
        }


        float r_bv;
        boolean flag_r_barevision = false;
        if (r_barevision != null) {
            try {
                r_bv = Float.parseFloat(r_barevision);
                flag_r_barevision = true;
            } catch (final NumberFormatException e) {
                flag_r_barevision = false;
                r_bv = 0.0f;
            }
            if (r_barevision != null && r_barevision.equals("A")) {
                _r_barevision = null;
                _r_barevision_mark = "A";
            } else if (r_barevision != null && r_barevision.equals("B")) {
                _r_barevision = null;
                _r_barevision_mark = "B";
            } else if (r_barevision != null && r_barevision.equals("C")) {
                _r_barevision = null;
                _r_barevision_mark = "C";
            } else if (r_barevision != null && r_barevision.equals("D")) {
                _r_barevision = null;
                _r_barevision_mark = "D";
            } else if (r_barevision != null && flag_r_barevision){
                if (r_bv >= 0.999999) {
                    _r_barevision = r_barevision;
                    _r_barevision_mark = "A";
                } else if (r_bv < 1.0 && r_bv >= 0.6999999) {
                    _r_barevision = r_barevision;
                    _r_barevision_mark = "B";
                } else if (r_bv < 0.7 && r_bv >= 0.29999999) {
                    _r_barevision = r_barevision;
                    _r_barevision_mark = "C";
                } else {
                    _r_barevision = r_barevision;
                    _r_barevision_mark = "D";
                }
            } else {
                _r_barevision = null;
                _r_barevision_mark = null;
            }
        } else {
            _r_barevision = null;
            _r_barevision_mark = null;
        }

        float r_v;
        boolean flag_r_vision = false;
        if (r_vision != null) {
            try {
                r_v = Float.parseFloat(r_vision);
                flag_r_vision = true;
            } catch (final NumberFormatException e) {
                flag_r_vision = false;
                r_v = 0.0f;
            }
            if (r_vision != null && r_vision.equals("A")) {
                _r_vision = null;
                _r_vision_mark = "A";
            } else if (r_vision != null && r_vision.equals("B")) {
                _r_vision = null;
                _r_vision_mark = "B";
            } else if (r_vision != null && r_vision.equals("C")) {
                _r_vision = null;
                _r_vision_mark = "C";
            } else if (r_vision != null && r_vision.equals("D")) {
                _r_vision = null;
                _r_vision_mark = "D";
            } else if (r_vision != null && flag_r_vision){
                if (r_v >= 0.999999) {
                    _r_vision = r_vision;
                    _r_vision_mark = "A";
                } else if (r_v < 1.0 && r_v >= 0.6999999) {
                    _r_vision = r_vision;
                    _r_vision_mark = "B";
                } else if (r_v < 0.7 && r_v >= 0.29999999) {
                    _r_vision = r_vision;
                    _r_vision_mark = "C";
                } else {
                    _r_vision = r_vision;
                    _r_vision_mark = "D";
                }
            } else {
                _r_vision = null;
                _r_vision_mark = null;
            }
        } else {
            _r_vision = null;
            _r_vision_mark = null;
        }


        float l_v;
        boolean flag_l_vision = false;
        if (l_vision != null) {
            try {
                l_v = Float.parseFloat(l_vision);
                flag_l_vision = true;
            } catch (final NumberFormatException e) {
                flag_l_vision = false;
                l_v = 0.0f;
            }
            if (l_vision != null && l_vision.equals("A")) {
                _l_vision = null;
                _l_vision_mark = "A";
            } else if (l_vision != null && l_vision.equals("B")) {
                _l_vision = null;
                _l_vision_mark = "B";
            } else if (l_vision != null && l_vision.equals("C")) {
                _l_vision = null;
                _l_vision_mark = "C";
            } else if (l_vision != null && l_vision.equals("D")) {
                _l_vision = null;
                _l_vision_mark = "D";
            } else if (l_vision != null && flag_l_vision){
                if (l_v >= 0.999999) {
                    _l_vision = l_vision;
                    _l_vision_mark = "A";
                } else if (l_v < 1.0 && l_v >= 0.6999999) {
                    _l_vision = l_vision;
                    _l_vision_mark = "B";
                } else if (l_v < 0.7 && l_v >= 0.29999999) {
                    _l_vision = l_vision;
                    _l_vision_mark = "C";
                } else {
                    _l_vision = l_vision;
                    _l_vision_mark = "D";
                }
            } else {
                _l_vision = null;
                _l_vision_mark = null;
            }
        } else {
            _l_vision = null;
            _l_vision_mark = null;
        }



        if (l_ear != null && l_ear.equals(IJONASI)) {
            _l_ear = "01";
        } else if (l_ear != null && l_ear.equals(NANCHO)) {
            _l_ear = "02";
        } else {
            _l_ear = null;
        }

        if (r_ear != null && r_ear.equals(IJONASI)) {
            _r_ear = "01";
        } else if (r_ear != null && r_ear.equals(NANCHO)) {
            _r_ear = "02";
        } else {
            _r_ear = null;
        }

        final Object[] rtn = {
                _year,
                _schregno,
                null,
                _height,
                _weight,
                _sitheight,
                _r_barevision,
                _r_barevision_mark,
                _l_barevision,
                _l_barevision_mark,
                _r_vision,
                _r_vision_mark,
                _l_vision,
                _l_vision_mark,
                _r_ear,
                null,
                _l_ear,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

