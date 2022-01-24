// kanji=漢字
/*
 * $Id: 9541b52e92514b959d2afe1c626a2cefc8f56b0f $
 *
 * 作成日: 2007/07/11 10:12:21 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.comparators.ReverseComparator;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * 得点情報。<br>
 * 点数を追加していく。
 *    最高・最低・平均・総数が判明。
 *    1つの点数を渡せば順位が判明。
 * @author takaesu
 * @version $Id: 9541b52e92514b959d2afe1c626a2cefc8f56b0f $
 */
public class ScoreInfo {
    private static final ReverseComparator _reverseComparator = new ReverseComparator();
    private final List _scores = new ArrayList();
    private int _scale = 0;

    public ScoreInfo() {
        this(0);
    }

    /**
     * コンストラクタ。
     * @param scale スケール。四捨五入する。
     */
    public ScoreInfo(final int scale) {
        _scale = scale;
    }

    public void add(final int score) {
        add(new Integer(score));
    }
    
    private int total() {
        int total = 0;
        for (final Iterator it = _scores.iterator(); it.hasNext();) {
            final Integer score = (Integer) it.next();
            total += score.intValue();
        }
        return total;
    }

    public void add(final Integer score) {
        if (null == score) {
            return;
        }
        _scores.add(score);
        Collections.sort(_scores, _reverseComparator);
    }

    public Integer getHigh() {
        if (_scores.size() == 0) {
            return null;
        }
        return (Integer) _scores.get(0);
    }

    public Integer getLow() {
        if (_scores.size() == 0) {
            return null;
        }
        return (Integer) _scores.get(_scores.size() - 1);
    }

    public BigDecimal getAvg() {
        if (_scores.size() == 0) {
            return null;
        }
        final double d = (double) total() / size();
        final BigDecimal bigDecimal = new BigDecimal(d);
        return bigDecimal;
    }

    public int size() {
        return _scores.size();
    }

    public int rank(final int score) {
        final Integer val = new Integer(score);
        if (_scores.contains(val)) {
            return 1 + _scores.indexOf(val);
        }
        return 0;   // ランク外、判定不能。
    }

    /**
     * 標準偏差を得る。
     * @return 標準偏差
     */
    public double getStddev() {
        final int[] v = new int[size()];
        int i = 0;
        for (final Iterator it = _scores.iterator(); it.hasNext();) {
            final Integer score = (Integer) it.next();
            v[i++] = score.intValue();
        }
        return getSD(v);
    }

    /*
     * @see http://634.ayumu-baby.com/algorithm/standarddeviation.html
     */
    private double getSD(int[] x){
        //算術平均の算出
        double ave = 0;
        for(int i = 0; i < x.length; i++){
            ave = ave + x[i];
        }
        ave = ave / (x.length);

        //分散の算出
        double sum = 0;
        for(int i = 0; i < x.length; i++){
            sum = sum + (Math.abs(x[i] - ave) * Math.abs(x[i] - ave));
        }
        sum = sum / (x.length);

        return Math.sqrt(sum);
    }


    /**
     * 偏差値を得る。
     * @param v 値
     * @return 偏差値
     * @see http://www-06.ibm.com/jp/developerworks/kaburobo/kr-algo2.shtml
     */
    public Double getStdScoreDouble(final int v) {
        final double median = 50; // 中間値50
        if (1 >= size()) { // 人数が１人 == 平均点は１人の得点と同一かつ標準偏差は0 => 偏差値は無しとする。
            return null;
        }
        final double stdDev = getStddev();
        if (0.0 == stdDev) { // 標準偏差が0 => 偏差値は50とする。
            return new Double(median);
        }
        final double stdScore = median + 10 * (v - getAvg().doubleValue()) / getStddev();
        return new Double(stdScore);
    }

    /**
     * 偏差値を得る。
     * @param v 値
     * @return 偏差値
     * @see http://www-06.ibm.com/jp/developerworks/kaburobo/kr-algo2.shtml
     */
    public double getStdScore(final int v) {
        return 50 + 10 * (v - getAvg().doubleValue()) / getStddev();
    }

    /** {@inheritDoc} */
    public String toString() {
        final BigDecimal avg;
        if (0 == _scale) {
            avg = getAvg();
        } else {
            avg = getAvg().setScale(_scale, BigDecimal.ROUND_HALF_UP);
        }
        final String stddev;
        final double stddevd = getStddev();
        if (Double.isNaN(stddevd) || Double.isInfinite(stddevd) || 0 == _scale) {
            stddev = String.valueOf(stddevd);
        } else {
            stddev = new BigDecimal(String.valueOf(stddevd)).setScale(_scale, BigDecimal.ROUND_HALF_UP).toString();
        }

        return "[high=" + getHigh() + ", low=" + getLow() + ", avg=" + avg + ", size=" + size() + ", stddev=" + stddev + "]";
    }
} // ScoreInfo

// eof
