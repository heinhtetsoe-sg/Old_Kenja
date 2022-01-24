// kanji=漢字
/*
 * $Id: AccumulateMeiboMatrix.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2006/12/22 13:40:37 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.domain.Term;
import jp.co.alp.kenja.common.dao.DbConnection;
import jp.co.alp.kenja.common.dao.query.DaoMeibo;
import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDate;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Student;

/**
 * <<クラスの説明>>。
 * 注意! このクラスは、domainパッケージですが、dao.queryパッケージを利用しています。
 * @author takaesu
 * @version $Id: AccumulateMeiboMatrix.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public class AccumulateMeiboMatrix {
    /*pkg*/static final Log log = LogFactory.getLog(AccumulateMeiboMatrix.class);

    private final DbConnection _dbcon;
    private final ControlMaster _cm;

    private final Map<KenjaDateImpl, Map<Chair, List<Student>>> _dateChairStudentListMapMap = new HashMap<KenjaDateImpl, Map<Chair, List<Student>>>(); // 日付, <講座, 名簿>

    /**
     * コンストラクタ。
     * @param dbcon DB接続情報
     * @param cm コントロールマスタ
     */
    public AccumulateMeiboMatrix(final DbConnection dbcon, final ControlMaster cm) {
        _dbcon = dbcon;
        _cm = cm;
    }

    /**
     * 時間割マトリックスを読み込む。
     * @param schMatrix 時間割マトリックス
     * @param term 期間
     */
    public void load(final AccumulateScheduleMatrix schMatrix, final Term term) {
        final DaoMeibo meibo = new DaoMeibo(_dbcon, _cm);

        try {
            meibo.open();
            createMap(schMatrix, term, meibo);
        } catch (final SQLException e) {
            log.error("SQLException", e);
        } finally {
            meibo.close();
        }
    }

    private void createMap(
            final AccumulateScheduleMatrix schMatrix,
            final Term term,
            final DaoMeibo meibo
    ) throws SQLException {

        // 日のイテレート
        for (final KenjaDateImpl date : term) {

            final List<AccumulateSchedule> accumulateSchedule = schMatrix.get(date);
            if (null == accumulateSchedule) {
                continue;
            }
            // 1日の時間割の講座Setを得る。
            final Set<Chair> chairs = createChairs(accumulateSchedule);
            if (null == chairs || chairs.isEmpty()) {
                continue;
            }

            // 保持する
            final Map<Chair, List<Student>> map = meibo.getMeiboMapList(date, chairs, false); // 生徒が在籍していなくても対象とする
            if (null == map || map.isEmpty()) {
                continue;
            }

            log.debug("名簿: " + date + ", 講座の数=" + map.size());
            _dateChairStudentListMapMap.put(date, map);
        }
    }

    /**
     * 時間割Setから講座Setを得る。
     * @param set
     * @return
     */
    private Set<Chair> createChairs(final List<AccumulateSchedule> list) {
        if (null == list) {
            return null;
        }
        final Set<Chair> rtn = new HashSet<Chair>();
        for (final AccumulateSchedule sch : list) {
            rtn.add(sch.getChair());
        }
        return rtn;
    }

    /**
     * 名簿に存在するか？
     * @param student 生徒
     * @param date 日付
     * @param chair 講座
     * @return 名簿に存在するなら true
     */
    public boolean isEnable(
            final KenjaDateImpl date,
            final Chair chair,
            final Student student
    ) {
        return getStudentList(date, chair).contains(student);
    }

    /**
     * 時間割に一致する生徒のListを得る。
     * @param schedule 時間割
     * @return 生徒のList
     */
    public List<Student> getStudentList(final AccumulateSchedule schedule) {
        return getStudentList(schedule.getDate(), schedule.getChair());
    }

    /**
     * 時間割に一致する生徒のListを得る。
     * @param schedule 時間割
     * @return 生徒のList
     */
    private List<Student> getStudentList(final KenjaDate date, final Chair chair) {
        final Map<Chair, List<Student>> map = _dateChairStudentListMapMap.get(date);
        if (null == map) {
            return Collections.emptyList();
        }
        return map.get(chair);
    }
} // AccumulateMeiboMatrix

// eof
