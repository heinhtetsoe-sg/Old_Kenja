// kanji=漢字
/*
 * $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/07/15 14:20:49 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.withus.Curriculum;

/**
 * パラメータ。
 * @author takaesu
 * @version $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Param {

    /*pkg*/static final Log log = LogFactory.getLog(Param.class);

    /** 登録者コード. */
    public static final String REGISTERCD = "00999990";

    private final String _dbUrl;
    public final List _classes = new ArrayList();

    /** [志願者連番←→学籍番号]の対応. Keyは前者 */
    private final Map _seito = new HashMap();

    /** 続柄の対応 */
    private final Map _zokugara = new HashMap();

    /** [教育課程年度←→教育課程コード]の対応. Keyは前者 */
    private final Map _curriculumTable = new TreeMap();

    /** 学生区分の対応 */
    private final Map _studentDiv = new HashMap();

    /** コースコードの対応*/
    private final Map _courseCode = new HashMap();
    private static final String _BELONG01 = "001";
    private static final String _COURSE0001 = "0001";
    private static final String _COURSE0002 = "0002";

    public Param(final String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Main <//localhost:50000/witestdb> <移行プログラムクラスファイル...>");
            throw new IllegalArgumentException("引数の数が違う");
        }

        _dbUrl = args[0];

        for (int i = 1; i < args.length; i++) {
            final char moji = args[i].charAt(0);
            if (!isAlphabet(moji)) {
                continue;
            }
            _classes.add(args[i]);
        }

        /** 続柄セット */
        _zokugara.put("101", "01");
        _zokugara.put("102", "02");
        _zokugara.put("401", "03");
        _zokugara.put("402", "04");
        _zokugara.put("451", "05");
        _zokugara.put("452", "06");
        _zokugara.put("011", "07");
        _zokugara.put("012", "08");
        _zokugara.put("200", "09");
        _zokugara.put("501", "11");
        _zokugara.put("502", "12");
        _zokugara.put("701", "13");
        _zokugara.put("702", "14");
        _zokugara.put("600", "90");
        _zokugara.put("801", "90");
        _zokugara.put("802", "90");
        _zokugara.put("900", "90");

        /** 学生区分セット */
        _studentDiv.put("1010101", "01");
        _studentDiv.put("1010103", "01");
        _studentDiv.put("1020101", "01");
        _studentDiv.put("1020103", "01");
        _studentDiv.put("1010102", "02");
        _studentDiv.put("1010104", "02");
        _studentDiv.put("1020102", "02");
        _studentDiv.put("1020104", "02");
        _studentDiv.put("1010105", "05");
        _studentDiv.put("1020105", "05");
        _studentDiv.put("1010106", "06");
        _studentDiv.put("1020106", "06");
        _studentDiv.put("1020121", "07");
        _studentDiv.put("1020123", "07");
        _studentDiv.put("1010107", "08");
        _studentDiv.put("1020107", "08");
        _studentDiv.put("1020108", "08");
        _studentDiv.put("9999999", "99");

        /** コースコードセット*/
        _courseCode.put("1020121", _COURSE0002);
        _courseCode.put("1020123", _COURSE0002);
    }

    public static boolean isAlphabet(final char moji) {
        return (moji >= 'a' && moji <= 'z') || (moji >= 'A' && moji <= 'Z');
    }

    public AbstractKnj createKnj(final String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final Class hoge;
        try {
            hoge = Class.forName(className);
        } catch (final ClassNotFoundException e) {
            throw e;
        }

        final Object newInstance;
        try {
            newInstance = hoge.newInstance();
        } catch (final InstantiationException e) {
            throw e;
        } catch (final IllegalAccessException e) {
            throw e;
        }
        return (AbstractKnj) newInstance;
    }

    public String getDbUrl() {
        return _dbUrl;
    }

    public void load(DB2UDB db) throws SQLException {
        Curriculum.loadCurriculumMst(db);
        log.warn("名称マスタの教育課程情報を読込んだ。");

        loadIcassSeito(db);
    }

    private void loadIcassSeito(DB2UDB db) throws SQLException {
        try {
            final String sql = "SELECT "
                             + "    shigansha_renban, "
                             + "    shigansha_no, "
                             + "    seito_no, "
                             + "    CASE WHEN VALUE(shigansha_no, '') = '' "
                             + "         THEN '' "
                             + "         ELSE RIGHT(RTRIM('0000000' || shigansha_no), 7) "
                             + "    END AS applicantno "
                             + " FROM "
                             + "    seito";
            db.query(sql);
            ResultSet rs = db.getResultSet();
            while (rs.next()) {
                final String shigansyaRenban = rs.getString("shigansha_renban");
                final String shiganshaNo = rs.getString("shigansha_no");
                final String seitoNo = rs.getString("seito_no");
                final String applicantNo = rs.getString("applicantno");
                _seito.put(shigansyaRenban, new Seito(shiganshaNo, seitoNo, applicantNo));
            }
            db.commit();
            rs.close();
        } catch (final SQLException e) {
            log.fatal("ICASS生徒の読込みでエラー");
            throw e;
        }
        log.debug("ICASS:生徒数=" + _seito.keySet().size());
    }

    private class Seito {
        final String _shiganshano;
        final String _seitoNo;
        final String _applicantNo;
        /**
         * コンストラクタ。
         */
        public Seito(final String shiganshaNo, final String seitoNo, final String applicantNo) {
            _shiganshano = shiganshaNo;
            _seitoNo = seitoNo;
            _applicantNo = applicantNo;
        }
    }

    /**
     * 学籍番号を得る。
     * @param shiganshaRenban 志願者連番
     * @return 学籍番号
     */
    public String getSchregno(final String shiganshaRenban) {
        final Seito seito = (Seito) _seito.get(shiganshaRenban);
        if (null == seito || "".equals(seito._seitoNo)) {
            return null;
        }
        return (String) seito._seitoNo;
    }

    /**
     * 志願者番号を得る。
     * @param shiganshaRenban 志願者連番
     * @return 学籍番号
     */
    public String getApplicantNo(final String shiganshaRenban) {
        final Seito seito = (Seito) _seito.get(shiganshaRenban);
        return (String) seito._applicantNo;
    }

    /**
     * 続柄を得る。
     * @param zokugara ICASS続柄
     * @return 賢者続柄
     */
    public String getZokugara(final String zokugara) {
        return _zokugara.containsKey(zokugara) ? (String) _zokugara.get(zokugara) : "11";
    }

    /**
     * 学生区分を得る。
     * @param GAKUSHU_KYOTEN_CODE ICASS学習拠点コード
     * @param course_mst ICASSコースマスタ
     * @return 賢者学生区分
     */
    public String getStudentDiv(final String GAKUSHU_KYOTEN_CODE,final String course_mst) {

        if (GAKUSHU_KYOTEN_CODE == null) {
            return null;
        }
        
        int gakushuKyotenCd = Integer.valueOf(GAKUSHU_KYOTEN_CODE).intValue();       
        String studentDiv = (String) _studentDiv.get(course_mst);

        if ("05".equals(studentDiv) || "06".equals(studentDiv) || "99".equals(studentDiv)) {
            return studentDiv;
        }

        if ( gakushuKyotenCd == 1 ) {
            if ("01".equals(studentDiv) || "07".equals(studentDiv)) {
                return "01";
            }
            return null;
        }
        if (2 <= gakushuKyotenCd && gakushuKyotenCd <= 40) {
            if ("01".equals(studentDiv)) {
                return "03"; // ０３個人生
            } else if ("08".equals(studentDiv) && gakushuKyotenCd == 38) {
                return "07"; // (０８)ＣＰコース生は学習拠点コード=38でなければならない。
            } else if ("02".equals(studentDiv)) {
                return studentDiv;
            }
            return null;
        }
        if (gakushuKyotenCd == 59 || 151 <= gakushuKyotenCd && gakushuKyotenCd <= 169) {
            return "04"; // ０４提携先生
        }
        
        // 学習拠点コードに関連しないその他
        return studentDiv;
    }

    /**
     * コースコードを得る。
     * @param GAKUSHU_KYOTEN_CODE ICASS学習拠点コード
     * @param course_mst ICASSコースマスタ
     * @return コースコード
     */
    public String getCourseCode(final String GAKUSHU_KYOTEN_CODE, final String course_mst) {
        String courseCode = (String) _courseCode.get(course_mst);
        //        ↑この中には0001とかが入っている。
        if (null != GAKUSHU_KYOTEN_CODE && GAKUSHU_KYOTEN_CODE.equals(_BELONG01) && null != courseCode) {
            return courseCode;
        } else {
            return _COURSE0001;
        }
    }

    /**
     * 続柄から性別を得る。
     * @param zokugara ICASS続柄
     * @return 賢者続柄
     */
    public String getZokugaraSex(final String zokugara) {
        final String zoku = getZokugara(zokugara);
        String retSex = "2";
        if (zoku.equals("01") ||
            zoku.equals("03") ||
            zoku.equals("05") ||
            zoku.equals("07") ||
            zoku.equals("09") ||
            zoku.equals("11")
        ) {
            retSex = "1";
        }
        return retSex;
    }
} // Param

// eof
