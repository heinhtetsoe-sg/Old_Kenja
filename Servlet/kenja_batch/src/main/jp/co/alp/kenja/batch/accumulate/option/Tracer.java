// kanji=漢字
/*
 * $Id: Tracer.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2007/03/27 11:23:13 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate.option;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.domain.Student.GrdDiv;
import jp.co.alp.kenja.common.domain.Student.Transfer;
import jp.co.alp.kenja.batch.accumulate.AccumulateAttendMatrix;
import jp.co.alp.kenja.batch.accumulate.AccumulateSemes;
import jp.co.alp.kenja.batch.accumulate.AccumulateSubclass;
import jp.co.alp.kenja.batch.accumulate.Attendance;
import jp.co.alp.kenja.batch.domain.CourseMst;

/**
 * 生徒の詳細情報をログ出力する。
 * @author takaesu
 * @version $Id: Tracer.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class Tracer {
    /*pkg*/static final Log log = LogFactory.getLog(Tracer.class);

    private final String[] _schregnos;

    /**
     * コンストラクタ。
     * @param schregno 学籍番号
     */
    public Tracer(final String schregnos) {
        if (null == schregnos) {
            _schregnos = null;
        } else {
            _schregnos = StringUtils.split(schregnos, ",");
        }

    }

    public boolean isTrace(Student student) {
        return null != _schregnos && ArrayUtils.contains(_schregnos, student.getCode());
    }

    /**
     * @param student 生徒
     * @param attendances 出欠の集合体
     * @param category カテゴリ
     */
    public void traceAttendaces(
            final Student student,
            final AccumulateAttendMatrix.DateAttendanceMap attendances,
            final CourseMst courseMst
    ) {
        if (!isTrace(student)) {
            return;
        }

        final KenjaDateImpl grdDate = student.getGrdDate();
        final GrdDiv grdDiv = student.getGrdDiv();

        log.fatal("↓↓↓");
        log.fatal(student);
        log.fatal("在籍情報:" + grdDiv + "(" + grdDate + "), コアタイム=" + courseMst);
        final List<Transfer> list = student.getTransfers();
        for (final Transfer transfer : list) {
            log.fatal("異動データ:" + transfer);
        }

        for (final KenjaDateImpl date : attendances.getDateKeySet()) {
            log.fatal(date);
            for (final Attendance attendance : attendances.getAttendanceList(date)) {
                log.fatal("\t" + attendance.getSchedule() + ", " + attendance.getKintai());
                // "[" + _student + ", " + _schedule + ", " + _kintai + "]";
            }
        }
        log.fatal("↑↑↑");
    }

    public void traceAccumulateSemes(final Student student, final KenjaDateImpl date, final Collection<Attendance> coll, final AccumulateSemes rui) {
        if (!isTrace(student)) {
            return;
        }
        final StringBuffer stb = new StringBuffer();
        stb.append(">>" + student + " | " + date + "<<");
        for (final Attendance att : coll) {
            stb.append("\n ").append(att);
        }
        stb.append("\n ").append(rui);
        stb.append("\n");
        log.fatal(stb.toString());
    }

    public void traceCounter(final Student student, final AccumulateSemes acc) {
        if (!isTrace(student)) {
            return;
        }
        log.fatal(" 月累積結果 schregno = " + student.getCode());
        log.fatal("   LESSON   = " + acc.getLesson());
        log.fatal("   ABROAD   = " + acc.getAbroad());
        log.fatal("   OFFDAYS  = " + acc.getOffdays());
        log.fatal("   SUSPEND  = " + acc.getSuspend());
        log.fatal("   VIRUS    = " + acc.getVirus());
        log.fatal("   MOURNING = " + acc.getMourning());
        log.fatal("   ABSENT   = " + acc.getAbsent());
        log.fatal("   NOTICE   = " + acc.getNotice());
        log.fatal("   NONOTICE = " + acc.getNonotice());
        log.fatal("   SICK     = " + acc.getSick());
        log.fatal("   EARLY    = " + acc.getEarly());
        log.fatal("   EARLYNON = " + acc.getEarlyNonotice());
        log.fatal("   LATE     = " + acc.getLate());
        log.fatal("   LATENONO = " + acc.getLateNonotice());
    }

    public void traceCounter(final Student student, final Map<SubClass, AccumulateSubclass> kamo) {
        if (!isTrace(student)) {
            return;
        }
        for (final SubClass subClass : kamo.keySet()) {
            final AccumulateSubclass acc = kamo.get(subClass);
            log.fatal(" 科目累積結果 schregno = " + student.getCode() + " subclasscd = " + subClass.getCode());
            log.fatal("   LESSON   = " + acc.getLesson());
            log.fatal("   ABROAD   = " + acc.getAbroad());
            log.fatal("   OFFDAYS  = " + acc.getOffdays());
            log.fatal("   SUSPEND  = " + acc.getSuspend());
            log.fatal("   VIRUS    = " + acc.getVirus());
            log.fatal("   MOURNING = " + acc.getMourning());
            log.fatal("   ABSENT   = " + acc.getAbsent());
            log.fatal("   NOTICE   = " + acc.getNotice());
            log.fatal("   NONOTICE = " + acc.getNonotice());
            log.fatal("   SICK     = " + acc.getSick());
            log.fatal("   EARLY    = " + acc.getEarly());
            log.fatal("   EARLYNON = " + acc.getEarlyNonotice());
            log.fatal("   LATE     = " + acc.getLate());
            log.fatal("   LATENONO = " + acc.getLateNonotice());
            log.fatal("   NURSEOFF = " + acc.getNurseoff());
        }
    }

} // Tracer

// eof
