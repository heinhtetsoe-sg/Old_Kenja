<?php

require_once('for_php7.php');


class knjd231vQuery extends Query {
    //1：法定時数・2：実時数
    function getJugyouJisuFlg() {
        $query  = " SELECT ";
        $query .= "     JUGYOU_JISU_FLG ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //上限値(分数)
    function getSyuutokuJougenti($model) {
        $query  = " SELECT ";
        $query .= "     SYUTOKU_BUNSI || '/' || SYUTOKU_BUNBO AS SYUTOKU_JOUGENTI, ";
        $query .= "     RISYU_BUNSI   || '/' || RISYU_BUNBO   AS RISYU_JOUGENTI, ";
        $query .= "     SYUTOKU_BUNSI_SPECIAL || '/' || SYUTOKU_BUNBO_SPECIAL AS SYUTOKU_JOUGENTI_SPECIAL, ";
        $query .= "     RISYU_BUNSI_SPECIAL   || '/' || RISYU_BUNBO_SPECIAL   AS RISYU_JOUGENTI_SPECIAL ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        if ($model->jugyou_jisu_flg == '1') { //1：法定時数 2：実時数
            $query .= "     JUGYOU_JISU_FLG = '1' ";
        } else {
            $query .= "     JUGYOU_JISU_FLG = '2' ";
        }
        return $query;
    }

    // 遅刻何回で欠課とするかの指数取得
    function getScAbsentCov() {
        $db = Query::dbCheckOut();
        $query = "SELECT * FROM v_school_mst WHERE year = '". CTRL_YEAR ."'";
        $absent = array();
        $absent = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $absent;
    }

    //出欠集計開始日付などを取得
    function getAttendDate($model) {
        $semester = ($model->field["GAKKI2"] == 9) ? CTRL_SEMESTER : $model->field["GAKKI2"];
        $query  = "SELECT SEMESTER ";
        $query .= "      ,MAX(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END) AS MAX_YEAR ";
        $query .= "      ,MONTH ";
        $query .= "      ,MAX(APPOINTED_DAY) AS MAX_APP ";
        $query .= "FROM   ATTEND_SEMES_DAT ";
        $query .= "WHERE  YEAR='".CTRL_YEAR."' AND SEMESTER <= '{$semester}' ";
        $query .= "GROUP BY SEMESTER,MONTH ";
        $query .= "ORDER BY 2,3,1 ";

        return $query;
    }

    //学期取得
    function getSelectSeme() {
        $query  = " SELECT DISTINCT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER";

        return $query;
    }

    //学年取得
    function getSelectGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SCHOOL_KIND IN ('H', 'J') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function getSubclasscd($model) {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
            $query .= "     SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SCHOOL_KIND IN ('H', 'J') ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校マスタの校種有無チェック
    function checkSchoolMst() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME  = 'SCHOOL_MST' AND ";
        $query .= "     NAME    = 'SCHOOL_KIND' ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND IN ('H', 'J') ";
        if ($model->field["GRADE"] != '99') {
            $query .= " AND GRADE   = '".$model->field["GRADE"]."' ";
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->field["SUBCLASSCD"] != "") {
            list ($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $model->field["SUBCLASSCD"]);
            $query .= " AND SCHOOL_KIND    = '".$school_kind."' ";
        }

        return $query;
    }

/**************************************************************************************************/
/**************************************************************************************************/
/**************************************************************************************************/
    //CSV作成(成績不振者)
    function selectListQuery($model, $attend_seme, $month, $attend_sdate, $knjSchoolMst) {
        $semester = ($model->field["GAKKI2"] == 9) ? CTRL_SEMESTER : $model->field["GAKKI2"];
        $date = str_replace("/","-",$model->field["DATE"]);
        $absent_cov      = $model->absent_cov;
        $absent_cov_late = $model->absent_cov_late;
        $amari_kuriage   = $model->amari_kuriage;
        $ctrl_year = CTRL_YEAR;

        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T3.HR_NAME, ";
        $query .= "         T2.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1";
        $query .= "     INNER JOIN ";
        $query .= "         SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN ";
        $query .= "         SCHREG_REGD_HDAT T3 ON T1.YEAR = T3.YEAR ";
        $query .= "                            AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "                            AND T1.GRADE = T3.GRADE ";
        $query .= "                            AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_REGD_GDAT T4 ON T1.YEAR = T4.YEAR ";
        $query .= "                            AND T1.GRADE = T4.GRADE ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '{$ctrl_year}' ";
        $query .= "         AND T1.SEMESTER = '{$semester}' ";
        $query .= "         AND VALUE(T2.GRD_DATE, '9999-12-31') > '{$date}' ";
        if ($model->field["GRADE"] != '99') {
            $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T4.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T4.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ) ";

        $query .= " ,ATTEND_SUBCLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR = '{$ctrl_year}' ";
        $query .= "         AND SEMESTER <= '{$attend_seme}' ";
        $query .= "         AND MONTH IN ('". implode($month, "','") ."') ";
        $query .= " ) ";

        $query .= " ,SCHEDULE AS ( ";
        $query .= "     SELECT ";
        $query .= "         EXECUTEDATE, ";
        $query .= "         PERIODCD, ";
        $query .= "         CHAIRCD ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_DAT ";
        $query .= "     WHERE ";
        $query .= "             EXECUTEDATE BETWEEN DATE('{$attend_sdate}') AND DATE('{$date}') ";
        $query .= "         AND PERIODCD != '0' ";
        $query .= "     GROUP BY ";
        $query .= "         EXECUTEDATE, ";
        $query .= "         PERIODCD, ";
        $query .= "         CHAIRCD ";
        $query .= " ) ";

        $query .= " ,T_attend_dat AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, ";
        $query .= "         W1.ATTENDDATE, ";
        $query .= "         W1.PERIODCD, ";
        $query .= "         CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END AS DI_CD, ";
        $query .= "         L1.MULTIPLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_DAT W1 ";
        $query .= "         LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = W1.YEAR AND L1.DI_CD = W1.DI_CD ";
        $query .= "     WHERE ";
        $query .= "             W1.ATTENDDATE BETWEEN DATE('{$attend_sdate}') AND DATE('{$date}') ";
        $query .= "         AND NOT EXISTS( SELECT ";
        $query .= "                             'X' ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_TRANSFER_DAT T7 ";
        $query .= "                         WHERE ";
        $query .= "                                 T7.SCHREGNO = W1.SCHREGNO ";
        $query .= "                             AND T7.TRANSFERCD IN('1','2') ";
        $query .= "                             AND W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ";
        $query .= " ) ";

        $query .= " ,T_attend AS ( ";
        $query .= "     SELECT ";
        $query .= "         S1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     S1.CLASSCD, ";
            $query .= "     S1.SCHOOL_KIND, ";
            $query .= "     S1.CURRICULUM_CD, ";
        }
        $query .= "         S1.SUBCLASSCD, ";
        $query .= "         S1.SEMESTER, ";
        $query .= "         COUNT(S3.SCHREGNO)  AS OFFDAYS, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8'  THEN 1 ELSE 0 END)  AS  ABSENT, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9'  THEN 1 ELSE 0 END)  AS  SUSPEND, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF, ";
        $query .= "         SUM(CASE WHEN S2.DI_CD IN('15','23','24') THEN SMALLINT(VALUE(S2.MULTIPLY, '1')) ELSE 0 END)  AS  LATE,  ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)  AS  EARLY, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '19' THEN 1 WHEN '20' THEN 1 ELSE 0 END)  AS  VIRUS, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '25' THEN 1 WHEN '26' THEN 1 ELSE 0 END)  AS  KOUDOME ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             T2.SCHREGNO, ";
        $query .= "             T1.EXECUTEDATE, ";
        $query .= "             T1.PERIODCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.CLASSCD, ";
            $query .= "         T3.SCHOOL_KIND, ";
            $query .= "         T3.CURRICULUM_CD, ";
        }
        $query .= "             T3.SUBCLASSCD, ";
        $query .= "             T2.SEMESTER ";
        $query .= "         FROM ";
        $query .= "             SCHEDULE T1, ";
        $query .= "             CHAIR_STD_DAT T2, ";
        $query .= "             CHAIR_DAT T3, ";
        $query .= "             SCHNO T4 ";
        $query .= "         WHERE ";
        $query .= "                 T1.CHAIRCD  = T3.CHAIRCD ";
        $query .= "             AND T3.YEAR     = '{$ctrl_year}' ";
        $query .= "             AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "             AND T2.YEAR     = '{$ctrl_year}' ";
        $query .= "             AND T2.SEMESTER = T3.SEMESTER ";
        $query .= "             AND T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "             AND T4.SCHREGNO = T2.SCHREGNO ";
        $query .= "             AND NOT EXISTS( SELECT ";
        $query .= "                                 'X' ";
        $query .= "                             FROM ";
        $query .= "                                 SCH_CHR_COUNTFLG T5 ";
        $query .= "                             WHERE ";
        $query .= "                                 T5.EXECUTEDATE  = T1.EXECUTEDATE AND ";
        $query .= "                                 T5.PERIODCD     = T1.PERIODCD AND ";
        $query .= "                                 T5.CHAIRCD      = T1.CHAIRCD AND ";
        $query .= "                                 T5.GRADE        = T4.GRADE AND ";
        $query .= "                                 T5.HR_CLASS     = T4.HR_CLASS AND ";
        $query .= "                                 T5.COUNTFLG     = '0' ";
        $query .= "                           ) ";
        $query .= "             AND NOT EXISTS( SELECT ";
        $query .= "                                 'X' ";
        $query .= "                             FROM ";
        $query .= "                                 SCHREG_BASE_MST T6 ";
        $query .= "                             WHERE ";
        $query .= "                                 T6.SCHREGNO = T4.SCHREGNO AND ";
        $query .= "                                 ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                                  (T6.ENT_DIV IN('4','5')     AND T6.ENT_DATE > T1.EXECUTEDATE)) ";
        $query .= "                           ) ";
        $query .= "    AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND ATT_DI.REP_DI_CD = '27' "; // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "                  ) ";
        $query .= "    AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND ATT_DI.REP_DI_CD = '28' "; // 勤怠コード'28'は時間割にカウントしない
        $query .= "                  ) ";
        $query .= "         GROUP BY ";
        $query .= "             T2.SCHREGNO, ";
        $query .= "             T1.EXECUTEDATE, ";
        $query .= "             T1.PERIODCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.CLASSCD, ";
            $query .= "         T3.SCHOOL_KIND, ";
            $query .= "         T3.CURRICULUM_CD, ";
        }
        $query .= "             T3.SUBCLASSCD, ";
        $query .= "             T2.SEMESTER)S1 ";
        $query .= "     LEFT JOIN T_attend_dat        S2 ON  S2.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S2.ATTENDDATE = S1.EXECUTEDATE ";
        $query .= "                                      AND S2.PERIODCD   = S1.PERIODCD ";
        $query .= "     LEFT JOIN SCHREG_TRANSFER_DAT S3 ON  S3.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S3.TRANSFERCD = '2' ";
        $query .= "                                      AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE ";
        $query .= "     LEFT JOIN SCHREG_TRANSFER_DAT S4 ON  S4.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S4.TRANSFERCD = '1' ";
        $query .= "                                      AND S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE ";
        $query .= "     GROUP BY ";
        $query .= "         S1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     S1.CLASSCD, ";
            $query .= "     S1.SCHOOL_KIND, ";
            $query .= "     S1.CURRICULUM_CD, ";
        }
        $query .= "         S1.SUBCLASSCD, ";
        $query .= "         S1.SEMESTER ";
        $query .= " ) ";

        $query .= " ,ATTEND_SUM AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     W1.CLASSCD, ";
            $query .= "     W1.SCHOOL_KIND, ";
            $query .= "     W1.CURRICULUM_CD, ";
        }
        $query .= "         W1.SUBCLASSCD, ";
        $query .= "         W1.SEMESTER, ";
        $query .= "         SUM(VALUE(W1.SICK,0) + VALUE(W1.NURSEOFF,0) ";
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .=           "+ VALUE(W1.ABSENT,0)";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .=           "+ VALUE(W1.SUSPEND,0)";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .=           "+ VALUE(W1.MOURNING,0)";
        }
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .=           "+ VALUE(W1.OFFDAYS,0)";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .=           "+ VALUE(W1.VIRUS,0)";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $query .=           "+ VALUE(W1.KOUDOME,0)";
        }
        $query .= "            ) AS SICK, ";
        $query .= "         SUM(VALUE(W1.NOTICE,0))     NOTICE, ";
        $query .= "         SUM(VALUE(W1.NONOTICE,0))   NONOTICE, ";
        $query .= "         SUM(VALUE(W1.LATE,0))       LATE, ";
        $query .= "         SUM(VALUE(W1.EARLY,0))      EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS W1, ";
        $query .= "         SCHNO W0 ";
        $query .= "     WHERE ";
        $query .= "         W1.SCHREGNO = W0.SCHREGNO ";
        $query .= "     GROUP BY ";
        $query .= "         W1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     W1.CLASSCD, ";
            $query .= "     W1.SCHOOL_KIND, ";
            $query .= "     W1.CURRICULUM_CD, ";
        }
        $query .= "         W1.SUBCLASSCD, ";
        $query .= "         W1.SEMESTER ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         W2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     W2.CLASSCD, ";
            $query .= "     W2.SCHOOL_KIND, ";
            $query .= "     W2.CURRICULUM_CD, ";
        }
        $query .= "         W2.SUBCLASSCD, ";
        $query .= "         W2.SEMESTER, ";
        $query .= "         VALUE(W2.SICK,0) + VALUE(W2.NURSEOFF,0) ";
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .= "          + VALUE(W2.ABSENT,0)";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .= "          + VALUE(W2.SUSPEND,0)";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .= "          + VALUE(W2.MOURNING,0)";
        }
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .= "          + VALUE(W2.OFFDAYS,0)";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .= "          + VALUE(W2.VIRUS,0)";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $query .= "          + VALUE(W2.KOUDOME,0)";
        }
        $query .= "                AS SICK, ";
        $query .= "         VALUE(W2.NOTICE,0)      NOTICE, ";
        $query .= "         VALUE(W2.NONOTICE,0)    NONOTICE, ";
        $query .= "         VALUE(W2.LATE,0)        LATE, ";
        $query .= "         VALUE(W2.EARLY,0)       EARLY ";
        $query .= "     FROM ";
        $query .= "         T_attend W2 ";
        $query .= " ) ";

        //学期毎に清算
        $query .= " ,ATTEND_SUM2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SEMESTER, ";
        $query .= "         SUM(SICK)       SICK, ";
        $query .= "         SUM(NOTICE)     NOTICE, ";
        $query .= "         SUM(NONOTICE)   NONOTICE, ";
        $query .= "         SUM(LATE)       LATE, ";
        $query .= "         SUM(EARLY)      EARLY, ";
        if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      MOD((sum(LATE) + sum(EARLY)), {$absent_cov_late}) AS TIKOKU_SOUTAI, ";
            $query .= "      decimal((float(sum(LATE) + sum(EARLY)) / {$absent_cov_late}) + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)),4,1) as NOTICE_LATE, ";
        } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      MOD((sum(LATE) + sum(EARLY)), {$absent_cov_late}) AS TIKOKU_SOUTAI, ";
            $query .= "      ((sum(LATE) + sum(EARLY)) / {$absent_cov_late}) + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)) as NOTICE_LATE, ";
        } else {
            $query .= "      (SUM(LATE) + SUM(EARLY)) AS TIKOKU_SOUTAI, ";
            $query .= "      sum(NOTICE) + sum(NONOTICE) + sum(SICK) as NOTICE_LATE, ";
        }
        $query .= "         SUM(LATE) + SUM(EARLY) + SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE2, ";
        $query .= "         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE3 ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SEMESTER ";
        $query .= " ) ";

        //年間で清算
        $query .= " ,ATTEND_SUM3 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUM(SICK)       SICK, ";
        $query .= "         SUM(NOTICE)     NOTICE, ";
        $query .= "         SUM(NONOTICE)   NONOTICE, ";
        $query .= "         SUM(LATE)       LATE, ";
        $query .= "         SUM(EARLY)      EARLY, ";
        $query .= "         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS KETUJISU, ";
        if (($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "     SUM(TIKOKU_SOUTAI) TIKOKU_SOUTAI, "; //遅刻早退
            $query .= "     SUM(NOTICE_LATE) NOTICE_LATE, "; //欠課
        } else {
            if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "      MOD((SUM(LATE) + SUM(EARLY)), {$absent_cov_late}) AS TIKOKU_SOUTAI, "; //遅刻早退
                $query .= "      DECIMAL((FLOAT(SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)),4,1) AS NOTICE_LATE, ";
            } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "      MOD((SUM(LATE) + SUM(EARLY)), {$absent_cov_late}) AS TIKOKU_SOUTAI, "; //遅刻早退
                $query .= "      ((SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)) AS NOTICE_LATE, ";
            } elseif (($absent_cov == "5") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "      CASE WHEN MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) >= {$amari_kuriage} ";
                $query .= "           THEN MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) -  {$amari_kuriage} ";
                $query .= "           ELSE MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) ";
                $query .= "      END AS TIKOKU_SOUTAI, "; //遅刻早退
                $query .= "      ((SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)) + (CASE WHEN MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) >= {$amari_kuriage} ";
                $query .= "                                                                                                          THEN 1 ";
                $query .= "                                                                                                          ELSE 0 ";
                $query .= "                                                                                                     END) AS NOTICE_LATE, ";
            } else {
                $query .= "      SUM(LATE) + SUM(EARLY) AS TIKOKU_SOUTAI, ";
                $query .= "      SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE, ";
            }
        }

        $query .= "         SUM(LATE) + SUM(EARLY) AS TIKOKU_SOUTAI1, ";
        $query .= "         SUM(LATE) + SUM(EARLY) + SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE2, ";
        $query .= "         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE3 ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM2 ";
        $query .= "     WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || ";
            $query .= "     SCHOOL_KIND || '-' || ";
            $query .= "     CURRICULUM_CD || '-' || ";
        }
        $query .= "         SUBCLASSCD NOT IN ( ";
        $query .= "                             SELECT ";
        if($model->field["GAKKI2"] == 9) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                         ATTEND_CLASSCD || '-' || ";
                $query .= "                         ATTEND_SCHOOL_KIND || '-' || ";
                $query .= "                         ATTEND_CURRICULUM_CD || '-' || ";
            }
            $query .= "                             ATTEND_SUBCLASSCD ";
        } else {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                         COMBINED_CLASSCD || '-' || ";
                $query .= "                         COMBINED_SCHOOL_KIND || '-' || ";
                $query .= "                         COMBINED_CURRICULUM_CD || '-' || ";
            }
            $query .= "                             COMBINED_SUBCLASSCD ";
        }
        $query .= "                             FROM ";
        $query .= "                                 SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                             WHERE ";
        $query .= "                                 YEAR = '{$ctrl_year}' ";
        $query .= "                             ) ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";

        //評定とか拾ってくる
        $query .= " ), RECORD_SCORE AS( ";

        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T3.SUBCLASSNAME, ";
        $query .= "         T1.SCORE, ";
        $query .= "         T4.CREDITS, ";
        $query .= "         T1.COMP_CREDIT, ";
        $query .= "         T1.GET_CREDIT, ";
        $query .= "         DECIMAL(ROUND(FLOAT(T5.AVG)*10,0)/10,5,1) AS VALUATION ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHNO           T2 ON  T2.SCHREGNO   = T1.SCHREGNO ";
        $query .= "     LEFT  JOIN SUBCLASS_MST    T3 ON  T3.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                               AND T3.CLASSCD = T1.CLASSCD ";
            $query .= "                               AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                               AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "     LEFT  JOIN CREDIT_MST      T4 ON  T4.YEAR       = T1.YEAR ";
        $query .= "                                   AND T4.COURSECD   = T2.COURSECD ";
        $query .= "                                   AND T4.MAJORCD    = T2.MAJORCD ";
        $query .= "                                   AND T4.GRADE      = T2.GRADE ";
        $query .= "                                   AND T4.COURSECODE = T2.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                               AND T4.CLASSCD = T1.CLASSCD ";
            $query .= "                               AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                               AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                   AND T4.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     LEFT  JOIN RECORD_RANK_SDIV_DAT T5 ON T5.YEAR   = T1.YEAR ";
        $query .= "                                   AND T5.SEMESTER   = '{$model->field["GAKKI2"]}' ";
        $query .= "                                   AND T5.TESTKINDCD = '99' ";
        $query .= "                                   AND T5.TESTITEMCD = '00' ";
        $query .= "                                   AND T5.SCORE_DIV  = '09' ";
        $query .= "                                   AND T5.SUBCLASSCD = '999999' ";
        $query .= "                                   AND T5.SCHREGNO   = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '{$ctrl_year}' AND ";
        $query .= "         T1.SEMESTER     = '{$model->field["GAKKI2"]}' AND ";
        $query .= "         T1.TESTKINDCD   = '99' AND ";
        $query .= "         T1.TESTITEMCD   = '00' AND ";
        $query .= "         T1.SCORE_DIV    = '09' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || ";
            $query .= "     T1.SCHOOL_KIND || '-' || ";
            $query .= "     T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "         T1.SUBCLASSCD NOT IN ( ";
        $query .= "                             SELECT ";
        if($model->field["GAKKI2"] == 9) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                         ATTEND_CLASSCD || '-' || ";
                $query .= "                         ATTEND_SCHOOL_KIND || '-' || ";
                $query .= "                         ATTEND_CURRICULUM_CD || '-' || ";
            }
            $query .= "                             ATTEND_SUBCLASSCD ";
        } else {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                         COMBINED_CLASSCD || '-' || ";
                $query .= "                         COMBINED_SCHOOL_KIND || '-' || ";
                $query .= "                         COMBINED_CURRICULUM_CD || '-' || ";
            }
            $query .= "                             COMBINED_SUBCLASSCD ";
        }
        $query .= "                             FROM ";
        $query .= "                                 SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                             WHERE ";
        $query .= "                                 YEAR = '{$ctrl_year}' ";
        $query .= "                             ) ";
        //仮評定は除く
        $query .= "         AND NOT EXISTS(SELECT ";
        $query .= "                            'X' ";
        $query .= "                        FROM ";
        $query .= "                            RECORD_PROV_FLG_DAT P1 ";
        $query .= "                        WHERE ";
        $query .= "                                P1.YEAR = '{$ctrl_year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND P1.CLASSCD = T1.CLASSCD ";
            $query .= "                        AND P1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                        AND P1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                            AND P1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                            AND P1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                            AND P1.PROV_FLG = '1' "; //1:仮評定フラグ
        $query .= "                       ) ";

        //履修単位数・修得単位数 累計
        $query .= " ), STUDYREC AS ( ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(VALUE(T1.COMP_CREDIT, 0)) AS ST_COMP, ";
        $query .= "     SUM(VALUE(T1.GET_CREDIT, 0)) + SUM(VALUE(T1.ADD_CREDIT, 0)) AS ST_GET_ADD ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR < '{$ctrl_year}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";

        //授業時数を取得
        $query .= " ), SUB_LESSON AS ( ";
        $query .= knjd231vQuery::getLessonSql($ctrl_year, $attend_seme, $month, $attend_sdate, $date, $knjSchoolMst, $model);

        /******************************/
        /* 教科・科目 or 総合的な時間 */
        /******************************/
            //対象の生徒を取得
            $query .= " ), SUB_MAIN AS ( ";

            if ($model->field["SEISEKI_HUSIN2"] == '1' || $model->field["SEISEKI_HUSIN3"] == '1') {
                $query .= " SELECT ";
                $query .= "     T1.SCHREGNO, ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " T2.CLASSCD, ";
                    $query .= " T2.SCHOOL_KIND, ";
                    $query .= " T2.CURRICULUM_CD, ";
                }
                $query .= "     T2.SUBCLASSCD ";
                if ($model->jugyou_jisu_flg == '1') { //1：法定時数 2：実時数
                    $query .= " FROM ";
                    $query .= "     SCHNO T1 ";
                    $query .= " INNER JOIN ";
                    $query .= "     ATTEND_SUM3 T2 ON T2.SCHREGNO = T1.SCHREGNO ";
                    $query .= " INNER JOIN ";
                    $query .= "     RECORD_SCORE T3 ON  T3.SCHREGNO   = T2.SCHREGNO ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "                 AND T3.CLASSCD = T2.CLASSCD ";
                        $query .= "                 AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
                        $query .= "                 AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
                    }
                    $query .= "                     AND T3.SUBCLASSCD = T2.SUBCLASSCD ";
                    $query .= " LEFT  JOIN ";
                    $query .= "     CREDIT_MST L1 ON  L1.YEAR       = '{$ctrl_year}' ";
                    $query .= "                   AND L1.COURSECD   = T1.COURSECD ";
                    $query .= "                   AND L1.MAJORCD    = T1.MAJORCD ";
                    $query .= "                   AND L1.GRADE      = T1.GRADE ";
                    $query .= "                   AND L1.COURSECODE = T1.COURSECODE ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "               AND L1.CLASSCD = T2.CLASSCD ";
                        $query .= "               AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ";
                        $query .= "               AND L1.CURRICULUM_CD = T2.CURRICULUM_CD ";
                    }
                    $query .= "                   AND L1.SUBCLASSCD = T2.SUBCLASSCD ";
                } else {
                    $query .= " FROM ";
                    $query .= "     SCHNO T1 ";
                    $query .= " INNER JOIN ";
                    $query .= "     ATTEND_SUM3 T2 ON T1.SCHREGNO = T2.SCHREGNO ";
                    $query .= " INNER JOIN ";
                    $query .= "     RECORD_SCORE T3 ON  T3.SCHREGNO   = T2.SCHREGNO ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "                 AND T3.CLASSCD = T2.CLASSCD ";
                        $query .= "                 AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
                        $query .= "                 AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
                    }
                    $query .= "                     AND T3.SUBCLASSCD = T2.SUBCLASSCD ";
                    $query .= " LEFT  JOIN ";
                    $query .= "     SCHREG_ABSENCE_HIGH_DAT L1 ON  L1.YEAR       = '{$ctrl_year}' ";
                    $query .= "                                AND L1.SCHREGNO   = T1.SCHREGNO ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "                            AND L1.CLASSCD = T2.CLASSCD ";
                        $query .= "                            AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ";
                        $query .= "                            AND L1.CURRICULUM_CD = T2.CURRICULUM_CD ";
                    }
                    $query .= "                                AND L1.SUBCLASSCD = T2.SUBCLASSCD ";
                    $query .= "                                AND L1.DIV        = '2' ";
                }

                if ($model->field["SEISEKI_HUSIN2"] == '1' && $model->field["SEISEKI_HUSIN3"] == '1') {
                    $query .= " WHERE ";
                    $query .= "     T2.NOTICE_LATE > L1.GET_ABSENCE_HIGH ";
                } elseif ($model->field["SEISEKI_HUSIN2"] == '1') {
                    $query .= " WHERE ";
                    $query .= "     T2.NOTICE_LATE >  L1.GET_ABSENCE_HIGH AND ";
                    if ($model->jugyou_jisu_flg == '1') { //1：法定時数 2：実時数
                        $query .= "     T2.NOTICE_LATE <= L1.ABSENCE_HIGH ";
                    } else {
                        $query .= "     T2.NOTICE_LATE <= L1.COMP_ABSENCE_HIGH ";
                    }
                } elseif ($model->field["SEISEKI_HUSIN3"] == '1') {
                    $query .= " WHERE ";
                    if ($model->jugyou_jisu_flg == '1') { //1：法定時数 2：実時数
                        $query .= "     T2.NOTICE_LATE > L1.ABSENCE_HIGH ";
                    } else {
                        $query .= "     T2.NOTICE_LATE > L1.COMP_ABSENCE_HIGH ";
                    }
                }
            }

            if (($model->field["SEISEKI_HUSIN2"] == '1' || $model->field["SEISEKI_HUSIN3"] == '1') && $model->field["SEISEKI_HUSIN1"] == '1') {
                $query .= " UNION ";
            }

            if ($model->field["SEISEKI_HUSIN1"] == '1') {
                $query .= " SELECT ";
                $query .= "     T1.SCHREGNO, ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " T3.CLASSCD, ";
                    $query .= " T3.SCHOOL_KIND, ";
                    $query .= " T3.CURRICULUM_CD, ";
                }
                $query .= "     T3.SUBCLASSCD ";
                $query .= " FROM ";
                $query .= "     SCHNO T1 ";
                $query .= " INNER JOIN ";
                $query .= "     RECORD_SCORE T3 ON T3.SCHREGNO = T1.SCHREGNO ";

                if ($model->field["SEISEKI_HUSIN1"] == '1') {
                    $query .= " WHERE ";
                    if (strlen($model->field["SEISEKI_HUSIN_HYOUTEI_FROM"]) && strlen($model->field["SEISEKI_HUSIN_HYOUTEI_TO"])) {
                        $query .= "    (T3.SCORE  >= {$model->field["SEISEKI_HUSIN_HYOUTEI_FROM"]} ";
                        $query .= " AND T3.SCORE  <= {$model->field["SEISEKI_HUSIN_HYOUTEI_TO"]}) ";
                    } elseif (strlen($model->field["SEISEKI_HUSIN_HYOUTEI_TO"])) {
                        $query .= "    (T3.SCORE  <= {$model->field["SEISEKI_HUSIN_HYOUTEI_TO"]} ";
                        $query .= " OR T3.SCORE IS NULL) ";
                    } else {
                        $query .= " T3.SCORE IS NULL ";
                    }
                }
            }


            //メイン
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     T1.HR_NAME, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.NAME, ";
            $query .= "     L2.SUBCLASSNAME, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " L2.CLASSCD, ";
                $query .= " L2.SCHOOL_KIND, ";
                $query .= " L2.CURRICULUM_CD, ";
            }
            $query .= "     L2.SUBCLASSCD, ";
            $query .= "     T3.SCORE, ";
            $query .= "     T3.CREDITS, ";
            $query .= "     T3.COMP_CREDIT, ";
            $query .= "     T3.GET_CREDIT, ";
            $query .= "     T3.VALUATION, ";
            $query .= "     T4.ST_COMP, ";
            $query .= "     T4.ST_GET_ADD, ";
            $query .= "     T2.KETUJISU, "; //欠次数
            //教育課程対応
            if ($model->Properties["chikokuHyoujiFlg"] == 1) {
                $query .= "     T2.TIKOKU_SOUTAI1 AS TIKOKU_SOUTAI, "; //遅刻・早退
            } else {
                $query .= "     T2.TIKOKU_SOUTAI, "; //遅刻・早退
            }
            $query .= "     T2.NOTICE_LATE, "; //欠課
            $query .= "     T5.MLESSON AS LESSON, "; //LESSON:授業時数、MLESSON:出席すべき時数
            if ($model->jugyou_jisu_flg == '1') { //1：法定時数 2：実時数
                $query .= "     L1.ABSENCE_HIGH     AS COMP_ABSENCE_HIGH, ";
                $query .= "     L1.GET_ABSENCE_HIGH AS GET_ABSENCE_HIGH ";
                $query .= " FROM ";
                $query .= "     SCHNO T1 ";
                $query .= " INNER JOIN ";
                $query .= "     SUB_MAIN        M1 ON M1.SCHREGNO    = T1.SCHREGNO ";
                $query .= " LEFT JOIN ";
                $query .= "     SUB_LESSON      T5 ON T5.SCHREGNO    = M1.SCHREGNO ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                   AND T5.CLASSCD  = M1.CLASSCD ";
                    $query .= "                   AND T5.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                   AND T5.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                       AND T5.SUBCLASSCD  = M1.SUBCLASSCD ";
                $query .= " LEFT JOIN ";
                $query .= "     ATTEND_SUM3     T2 ON T2.SCHREGNO    = M1.SCHREGNO ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                   AND T2.CLASSCD  = M1.CLASSCD ";
                    $query .= "                   AND T2.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                   AND T2.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                       AND T2.SUBCLASSCD  = M1.SUBCLASSCD ";
                $query .= " LEFT JOIN ";
                $query .= "     RECORD_SCORE    T3 ON  T3.SCHREGNO   = M1.SCHREGNO ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                    AND T3.CLASSCD  = M1.CLASSCD ";
                    $query .= "                    AND T3.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                    AND T3.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                        AND T3.SUBCLASSCD = M1.SUBCLASSCD ";
                $query .= " LEFT  JOIN STUDYREC T4 ON T4.SCHREGNO    = T1.SCHREGNO ";
                $query .= " LEFT  JOIN ";
                $query .= "     CREDIT_MST L1 ON  L1.YEAR       = '{$ctrl_year}' ";
                $query .= "                   AND L1.COURSECD   = T1.COURSECD ";
                $query .= "                   AND L1.MAJORCD    = T1.MAJORCD ";
                $query .= "                   AND L1.GRADE      = T1.GRADE ";
                $query .= "                   AND L1.COURSECODE = T1.COURSECODE ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "               AND L1.CLASSCD  = M1.CLASSCD ";
                    $query .= "               AND L1.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "               AND L1.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                   AND L1.SUBCLASSCD = M1.SUBCLASSCD ";
                $query .= " LEFT  JOIN ";
                $query .= "     SUBCLASS_MST L2 ON  L2.SUBCLASSCD = M1.SUBCLASSCD ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                 AND L2.CLASSCD  = M1.CLASSCD ";
                    $query .= "                 AND L2.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                 AND L2.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
            } else {
                $query .= "     L1.COMP_ABSENCE_HIGH AS COMP_ABSENCE_HIGH, ";
                $query .= "     L1.GET_ABSENCE_HIGH  AS GET_ABSENCE_HIGH ";
                $query .= " FROM ";
                $query .= "     SCHNO T1 ";
                $query .= " INNER JOIN ";
                $query .= "     SUB_MAIN    M1 ON M1.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN ";
                $query .= "     SUB_LESSON      T5 ON T5.SCHREGNO    = M1.SCHREGNO ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                   AND T5.CLASSCD  = M1.CLASSCD ";
                    $query .= "                   AND T5.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                   AND T5.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                       AND T5.SUBCLASSCD  = M1.SUBCLASSCD ";
                $query .= " LEFT JOIN ";
                $query .= "     ATTEND_SUM3     T2 ON T2.SCHREGNO    = M1.SCHREGNO ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                   AND T2.CLASSCD  = M1.CLASSCD ";
                    $query .= "                   AND T2.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                   AND T2.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                       AND T2.SUBCLASSCD  = M1.SUBCLASSCD ";
                $query .= " LEFT JOIN ";
                $query .= "     RECORD_SCORE    T3 ON  T3.SCHREGNO   = M1.SCHREGNO ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                    AND T3.CLASSCD  = M1.CLASSCD ";
                    $query .= "                    AND T3.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                    AND T3.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                        AND T3.SUBCLASSCD = M1.SUBCLASSCD ";
                $query .= " LEFT  JOIN STUDYREC T4 ON  T4.SCHREGNO   = T1.SCHREGNO ";
                $query .= " LEFT  JOIN ";
                $query .= "     SCHREG_ABSENCE_HIGH_DAT L1 ON  L1.YEAR       = '{$ctrl_year}' ";
                $query .= "                                AND L1.SCHREGNO   = M1.SCHREGNO ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                            AND L1.CLASSCD  = M1.CLASSCD ";
                    $query .= "                            AND L1.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                            AND L1.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                                AND L1.SUBCLASSCD = M1.SUBCLASSCD ";
                $query .= "                                AND L1.DIV        = '2' ";
                $query .= " LEFT  JOIN ";
                $query .= "     SUBCLASS_MST L2 ON  L2.SUBCLASSCD = M1.SUBCLASSCD ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                 AND L2.CLASSCD  = M1.CLASSCD ";
                    $query .= "                 AND L2.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                 AND L2.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
            }
            if ($model->field["KYOUKA_SOUGOU1"] == '1' || $model->field["KYOUKA_SOUGOU2"] == '1') {
                $query .= " WHERE ";
            }
            if ($model->field["KYOUKA_SOUGOU1"] == '1' && $model->field["KYOUKA_SOUGOU2"] == '1') {
                $query .= "     SUBSTR(M1.SUBCLASSCD,1,2) <= '90' ";
            } elseif ($model->field["KYOUKA_SOUGOU1"] == '1') {
                $query .= "     SUBSTR(M1.SUBCLASSCD,1,2) < '90' ";
            } elseif ($model->field["KYOUKA_SOUGOU2"] == '1') {
                $query .= "     SUBSTR(M1.SUBCLASSCD,1,2) = '90' ";
            }
            //科目
            if (strlen($model->field["SUBCLASSCD"])) {
                if ($model->field["KYOUKA_SOUGOU1"] == '1' || $model->field["KYOUKA_SOUGOU2"] == '1') {
                    $query .= " AND ";
                } else {
                    $query .= " WHERE ";
                }
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
                } else {
                    $query .= " L2.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
                }
            }

            //RECORD_SCORE_BEF_AFT_DAT・・・更新後データ参照
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     T1.HR_NAME, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.NAME, ";
            $query .= "     L2.SUBCLASSNAME, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " L2.CLASSCD, ";
                $query .= " L2.SCHOOL_KIND, ";
                $query .= " L2.CURRICULUM_CD, ";
            }
            $query .= "     L2.SUBCLASSCD, ";
            $query .= "     M1.AFT_SCORE AS SCORE, ";
            $query .= "     T3.CREDITS, ";
            $query .= "     M1.AFT_COMP_CREDIT AS COMP_CREDIT, ";
            $query .= "     M1.AFT_GET_CREDIT AS GET_CREDIT, ";
            $query .= "     T3.VALUATION, ";
            $query .= "     T4.ST_COMP, ";
            $query .= "     T4.ST_GET_ADD, ";
            $query .= "     T2.KETUJISU, "; //欠次数
            //教育課程対応
            if ($model->Properties["chikokuHyoujiFlg"] == 1) {
                $query .= "     T2.TIKOKU_SOUTAI1 AS TIKOKU_SOUTAI, "; //遅刻・早退
            } else {
                $query .= "     T2.TIKOKU_SOUTAI, "; //遅刻・早退
            }
            $query .= "     T2.NOTICE_LATE, "; //欠課
            $query .= "     T5.MLESSON AS LESSON, "; //LESSON:授業時数、MLESSON:出席すべき時数
            if ($model->jugyou_jisu_flg == '1') { //1：法定時数 2：実時数
                $query .= "     L1.ABSENCE_HIGH     AS COMP_ABSENCE_HIGH, ";
                $query .= "     L1.GET_ABSENCE_HIGH AS GET_ABSENCE_HIGH ";
                $query .= " FROM ";
                $query .= "     SCHNO T1 ";
                $query .= " INNER JOIN RECORD_SCORE_BEF_AFT_DAT M1 ";
                $query .= "        ON  M1.YEAR          = '{$ctrl_year}' ";
                $query .= "        AND M1.SEMESTER      = '9' ";
                $query .= "        AND M1.TESTKINDCD    = '99' ";
                $query .= "        AND M1.TESTITEMCD    = '00' ";
                $query .= "        AND M1.SCORE_DIV     = '09' ";
                $query .= "        AND M1.SCHREGNO      = T1.SCHREGNO ";
                $query .= " LEFT JOIN ";
                $query .= "     SUB_LESSON      T5 ON T5.SCHREGNO    = M1.SCHREGNO ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                   AND T5.CLASSCD  = M1.CLASSCD ";
                    $query .= "                   AND T5.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                   AND T5.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                       AND T5.SUBCLASSCD  = M1.SUBCLASSCD ";
                $query .= " LEFT JOIN ";
                $query .= "     ATTEND_SUM3     T2 ON T2.SCHREGNO    = M1.SCHREGNO ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                   AND T2.CLASSCD  = M1.CLASSCD ";
                    $query .= "                   AND T2.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                   AND T2.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                       AND T2.SUBCLASSCD  = M1.SUBCLASSCD ";
                $query .= " LEFT JOIN ";
                $query .= "     RECORD_SCORE    T3 ON  T3.SCHREGNO   = M1.SCHREGNO ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                    AND T3.CLASSCD  = M1.CLASSCD ";
                    $query .= "                    AND T3.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                    AND T3.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                        AND T3.SUBCLASSCD = M1.SUBCLASSCD ";
                $query .= " LEFT  JOIN STUDYREC T4 ON T4.SCHREGNO    = T1.SCHREGNO ";
                $query .= " LEFT  JOIN ";
                $query .= "     CREDIT_MST L1 ON  L1.YEAR       = '{$ctrl_year}' ";
                $query .= "                   AND L1.COURSECD   = T1.COURSECD ";
                $query .= "                   AND L1.MAJORCD    = T1.MAJORCD ";
                $query .= "                   AND L1.GRADE      = T1.GRADE ";
                $query .= "                   AND L1.COURSECODE = T1.COURSECODE ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "               AND L1.CLASSCD  = M1.CLASSCD ";
                    $query .= "               AND L1.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "               AND L1.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                   AND L1.SUBCLASSCD = M1.SUBCLASSCD ";
                $query .= " LEFT  JOIN ";
                $query .= "     SUBCLASS_MST L2 ON  L2.SUBCLASSCD = M1.SUBCLASSCD ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                 AND L2.CLASSCD  = M1.CLASSCD ";
                    $query .= "                 AND L2.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                 AND L2.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
            } else {
                $query .= "     L1.COMP_ABSENCE_HIGH AS COMP_ABSENCE_HIGH, ";
                $query .= "     L1.GET_ABSENCE_HIGH  AS GET_ABSENCE_HIGH ";
                $query .= " FROM ";
                $query .= "     SCHNO T1 ";
                $query .= " INNER JOIN RECORD_SCORE_BEF_AFT_DAT M1 ";
                $query .= "        ON  M1.YEAR          = '{$ctrl_year}' ";
                $query .= "        AND M1.SEMESTER      = '9' ";
                $query .= "        AND M1.TESTKINDCD    = '99' ";
                $query .= "        AND M1.TESTITEMCD    = '00' ";
                $query .= "        AND M1.SCORE_DIV     = '09' ";
                $query .= "        AND M1.SCHREGNO      = T1.SCHREGNO ";
                $query .= " LEFT JOIN ";
                $query .= "     SUB_LESSON      T5 ON T5.SCHREGNO    = M1.SCHREGNO ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                   AND T5.CLASSCD  = M1.CLASSCD ";
                    $query .= "                   AND T5.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                   AND T5.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                       AND T5.SUBCLASSCD  = M1.SUBCLASSCD ";
                $query .= " LEFT JOIN ";
                $query .= "     ATTEND_SUM3     T2 ON T2.SCHREGNO    = M1.SCHREGNO ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                   AND T2.CLASSCD  = M1.CLASSCD ";
                    $query .= "                   AND T2.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                   AND T2.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                       AND T2.SUBCLASSCD  = M1.SUBCLASSCD ";
                $query .= " LEFT JOIN ";
                $query .= "     RECORD_SCORE    T3 ON  T3.SCHREGNO   = M1.SCHREGNO ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                    AND T3.CLASSCD  = M1.CLASSCD ";
                    $query .= "                    AND T3.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                    AND T3.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                        AND T3.SUBCLASSCD = M1.SUBCLASSCD ";
                $query .= " LEFT  JOIN STUDYREC T4 ON  T4.SCHREGNO   = T1.SCHREGNO ";
                $query .= " LEFT  JOIN ";
                $query .= "     SCHREG_ABSENCE_HIGH_DAT L1 ON  L1.YEAR       = '{$ctrl_year}' ";
                $query .= "                                AND L1.SCHREGNO   = M1.SCHREGNO ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                            AND L1.CLASSCD  = M1.CLASSCD ";
                    $query .= "                            AND L1.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                            AND L1.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                                AND L1.SUBCLASSCD = M1.SUBCLASSCD ";
                $query .= "                                AND L1.DIV        = '2' ";
                $query .= " LEFT  JOIN ";
                $query .= "     SUBCLASS_MST L2 ON  L2.SUBCLASSCD = M1.SUBCLASSCD ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                 AND L2.CLASSCD  = M1.CLASSCD ";
                    $query .= "                 AND L2.SCHOOL_KIND  = M1.SCHOOL_KIND ";
                    $query .= "                 AND L2.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
            }
            if ($model->field["KYOUKA_SOUGOU1"] == '1' || $model->field["KYOUKA_SOUGOU2"] == '1') {
                $query .= " WHERE ";
            }
            if ($model->field["KYOUKA_SOUGOU1"] == '1' && $model->field["KYOUKA_SOUGOU2"] == '1') {
                $query .= "     SUBSTR(M1.SUBCLASSCD,1,2) <= '90' ";
            } elseif ($model->field["KYOUKA_SOUGOU1"] == '1') {
                $query .= "     SUBSTR(M1.SUBCLASSCD,1,2) < '90' ";
            } elseif ($model->field["KYOUKA_SOUGOU2"] == '1') {
                $query .= "     SUBSTR(M1.SUBCLASSCD,1,2) = '90' ";
            }
            //科目
            if (strlen($model->field["SUBCLASSCD"])) {
                if ($model->field["KYOUKA_SOUGOU1"] == '1' || $model->field["KYOUKA_SOUGOU2"] == '1') {
                    $query .= " AND ";
                } else {
                    $query .= " WHERE ";
                }
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
                } else {
                    $query .= " L2.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
                }
            }

            $query .= " ORDER BY ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     ATTENDNO, ";
            $query .= "     SCHREGNO, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " CLASSCD, ";
                $query .= " SCHOOL_KIND, ";
                $query .= " CURRICULUM_CD, ";
            }
            $query .= "     SUBCLASSCD ";

        return $query;
    }

    //授業時数(LESSON) 出席すべき時数(MLESSON)
    function getLessonSql($ctrl_year, $attend_seme, $month, $attend_sdate, $date, $knjSchoolMst, $model) {
        $query  = "";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " TT1.CLASSCD, ";
            $query .= " TT1.SCHOOL_KIND, ";
            $query .= " TT1.CURRICULUM_CD, ";
        }
        $query .= "     TT1.SUBCLASSCD, ";
        $query .= "     TT1.SCHREGNO, ";
        $query .= "     SUM(VALUE(TT1.LESSON,0)) AS LESSON, ";
        $query .= "     SUM(VALUE(TT1.MLESSON,0)) AS MLESSON ";
        $query .= " FROM ";
        $query .= "     ( ";
        //集計テーブル参照
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SCHREGNO, ";
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .= "     SUM(VALUE(LESSON,0) - VALUE(ABROAD,0)) AS LESSON, ";
            $query .= "     SUM(VALUE(LESSON,0) - VALUE(ABROAD,0) ";
            if ($knjSchoolMst["SUB_SUSPEND"] != "1") {
                $query .=           " - VALUE(SUSPEND,0) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] != "1") {
                $query .=           " - VALUE(MOURNING,0) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] != "1" && $model->Properties["useVirus"] == "true") {
                $query .=           " - VALUE(VIRUS,0) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] != "1" && $model->Properties["useKoudome"] == "true") {
                $query .=           " - VALUE(KOUDOME,0) ";
            }
            $query .= " ) AS MLESSON ";
        } else {
            $query .= "     SUM(VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0)) AS LESSON, ";
            $query .= "     SUM(VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ";
            if ($knjSchoolMst["SUB_SUSPEND"] != "1") {
                $query .=           " - VALUE(SUSPEND,0) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] != "1") {
                $query .=           " - VALUE(MOURNING,0) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] != "1" && $model->Properties["useVirus"] == "true") {
                $query .=           " - VALUE(VIRUS,0) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] != "1" && $model->Properties["useKoudome"] == "true") {
                $query .=           " - VALUE(KOUDOME,0) ";
            }
            $query .= " ) AS MLESSON ";
        }
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$ctrl_year}' ";
        $query .= "         AND SEMESTER <= '{$attend_seme}' ";
        $query .= "         AND MONTH IN ('". implode($month, "','") ."') ";
        $query .= "     GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SCHREGNO ";
        $query .= "     UNION ALL ";
        //時間割テーブル参照
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     TBL.CLASSCD, ";
            $query .= "     TBL.SCHOOL_KIND, ";
            $query .= "     TBL.CURRICULUM_CD, ";
        }
        $query .= "         TBL.SUBCLASSCD, ";
        $query .= "         TBL.SCHREGNO, ";
        $query .= "         SUM(LESSON) AS LESSON, ";
        $query .= "         SUM(LESSON ";
        if ($knjSchoolMst["SUB_SUSPEND"] != "1") {
            $query .=           " - SUSPEND";
        }
        if ($knjSchoolMst["SUB_MOURNING"] != "1") {
            $query .=           " - MOURNING";
        }
        if ($knjSchoolMst["SUB_VIRUS"] != "1" && $model->Properties["useVirus"] == "true") {
            $query .=           " - VIRUS";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] != "1" && $model->Properties["useKoudome"] == "true") {
            $query .=           " - KOUDOME";
        }
        $query .= "         ) AS MLESSON ";
        $query .= "     FROM ";
        $query .= "         ( ";
        $query .= "         SELECT ";
        $query .= "             T2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T4.CLASSCD, ";
            $query .= "         T4.SCHOOL_KIND, ";
            $query .= "         T4.CURRICULUM_CD, ";
        }
        $query .= "             T4.SUBCLASSCD, ";
        $query .= "             T1.EXECUTEDATE, ";
        $query .= "             T1.PERIODCD, ";
        $query .= "             1 AS LESSON, ";
        $query .= "             (CASE WHEN VALUE(ATT_DI.REP_DI_CD, '0') IN ( '2',  '9') THEN 1 ELSE 0 END) AS SUSPEND, ";
        $query .= "             (CASE WHEN VALUE(ATT_DI.REP_DI_CD, '0') IN ( '3', '10') THEN 1 ELSE 0 END) AS MOURNING, ";
        $query .= "             (CASE WHEN VALUE(ATT_DI.REP_DI_CD, '0') IN ('19', '20') THEN 1 ELSE 0 END) AS VIRUS, ";
        $query .= "             (CASE WHEN VALUE(ATT_DI.REP_DI_CD, '0') IN ('25', '26') THEN 1 ELSE 0 END) AS KOUDOME ";
        $query .= "         FROM ";
        $query .= "             SCH_CHR_DAT     T1 ";
        $query .= "         INNER JOIN ";
        $query .= "             CHAIR_STD_DAT   T2 ON  T2.YEAR     = T1.YEAR ";
        $query .= "                                AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "                                AND T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "         INNER JOIN ";
        $query .= "             SCHREG_REGD_DAT T3 ON  T3.SCHREGNO = T2.SCHREGNO ";
        $query .= "                                AND T3.YEAR     = T1.YEAR ";
        $query .= "                                AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "         INNER JOIN ";
        $query .= "             CHAIR_DAT       T4 ON  T4.YEAR     = T1.YEAR ";
        $query .= "                                AND T4.SEMESTER = T1.SEMESTER ";
        $query .= "                                AND T4.CHAIRCD  = T1.CHAIRCD ";
        $query .= "         LEFT JOIN ";
        $query .= "             ATTEND_DAT      L1 ON  L1.PERIODCD   = T1.PERIODCD ";
        $query .= "                                AND L1.CHAIRCD    = T1.CHAIRCD ";
        $query .= "                                AND L1.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                AND L1.SCHREGNO   = T2.SCHREGNO ";
        $query .= "         LEFT JOIN ";
        $query .= "             ATTEND_DI_CD_DAT ATT_DI  ON ATT_DI.YEAR     = L1.YEAR ";
        $query .= "                                     AND ATT_DI.DI_CD    = L1.DI_CD ";
        $query .= "         WHERE T1.YEAR     = '{$ctrl_year}' ";
        $query .= "            AND T1.EXECUTEDATE BETWEEN DATE('{$attend_sdate}') AND DATE('{$date}') ";
        $query .= "            AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCH_CHR_COUNTFLG E1 ";
        $query .= "                            WHERE E1.EXECUTEDATE   = T1.EXECUTEDATE AND ";
        $query .= "                                  E1.PERIODCD      = T1.PERIODCD AND ";
        $query .= "                                  E1.CHAIRCD       = T1.CHAIRCD AND ";
        $query .= "                                  E1.GRADE         = T3.GRADE AND ";
        $query .= "                                  E1.HR_CLASS      = T3.HR_CLASS AND ";
        $query .= "                                  E1.COUNTFLG      = '0') ";
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST E2 ";
        $query .= "                            WHERE E2.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "                                  ((E2.GRD_DIV IN('1','2','3') AND E2.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                                   (E2.ENT_DIV IN('4','5')     AND E2.ENT_DATE > T1.EXECUTEDATE))) ";
        $query .= "    AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND ATT_DI.REP_DI_CD = '27' "; // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "                  ) ";
        $query .= "    AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND ATT_DI.REP_DI_CD = '28' "; // 勤怠コード'28'は時間割にカウントしない
        $query .= "                  ) ";
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT E3 ";
        $query .= "                            WHERE E3.SCHREGNO = T2.SCHREGNO AND ";
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .= "                              E3.TRANSFERCD IN('1') AND ";
        } else {
            $query .= "                              E3.TRANSFERCD IN('1','2') AND ";
        }
        $query .= "                                  T1.EXECUTEDATE BETWEEN E3.TRANSFER_SDATE AND E3.TRANSFER_EDATE) ";
        $query .= "         GROUP BY T2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T4.CLASSCD, ";
            $query .= "         T4.SCHOOL_KIND, ";
            $query .= "         T4.CURRICULUM_CD, ";
        }
        $query .= "             T4.SUBCLASSCD, T1.EXECUTEDATE, T1.PERIODCD, ATT_DI.REP_DI_CD ";
        $query .= "         ) TBL ";
        $query .= "     GROUP BY TBL.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     TBL.CLASSCD, ";
            $query .= "     TBL.SCHOOL_KIND, ";
            $query .= "     TBL.CURRICULUM_CD, ";
        }
        $query .= "         TBL.SUBCLASSCD ";
        $query .= "     ) TT1 ";
        $query .= " GROUP BY TT1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " TT1.CLASSCD, ";
            $query .= " TT1.SCHOOL_KIND, ";
            $query .= " TT1.CURRICULUM_CD, ";
        }
        $query .= "     TT1.SUBCLASSCD ";
        return $query;
    }

    //訂正後データは、背景色をピンク表示
    function getExistsRecordScoreBefAftDat($model, $row) {
        //条件
        $query  = " SELECT COUNT(*) FROM RECORD_SCORE_BEF_AFT_DAT ";
        $query .= " WHERE YEAR          = '".CTRL_YEAR."'";
        $query .= "   AND SEMESTER      = '9'";
        $query .= "   AND TESTKINDCD    = '99'";
        $query .= "   AND TESTITEMCD    = '00'";
        $query .= "   AND SCORE_DIV     = '09'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND CLASSCD       = '{$row["CLASSCD"]}'";
            $query .= "   AND SCHOOL_KIND   = '{$row["SCHOOL_KIND"]}'";
            $query .= "   AND CURRICULUM_CD = '{$row["CURRICULUM_CD"]}'";
        }
        $query .= "   AND SUBCLASSCD    = '{$row["SUBCLASSCD"]}'";
        $query .= "   AND SCHREGNO      = '{$row["SCHREGNO"]}'";

        return $query;
    }

    /* データ更新処理 */
    function update($model)
    {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            //更新チェックボックス
            if ($model->fields["UPDATE_DATA"][$i] != '1') continue;

            $val = explode("-", $model->data["SCHREGNO"][$i]);
            $schno = $val[0];
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $classCd = $val[1];
                $schoolKind = $val[2];
                $curriculumCd = $val[3];
                $subcd = $val[4];
            } else {
                $subcd = $val[1];
            }
            $year = CTRL_YEAR;
            $semcd = '9';
            $kindcd = '99';
            $itemcd = '00';
            $div = '09';

            //RECORD_SCORE_BEF_AFT_DAT・・・更新前後データ保存
            //条件
            $where  = " WHERE YEAR          = '{$year}'";
            $where .= "   AND SEMESTER      = '{$semcd}'";
            $where .= "   AND TESTKINDCD    = '{$kindcd}'";
            $where .= "   AND TESTITEMCD    = '{$itemcd}'";
            $where .= "   AND SCORE_DIV     = '{$div}'";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= "   AND CLASSCD       = '{$classCd}'";
                $where .= "   AND SCHOOL_KIND   = '{$schoolKind}'";
                $where .= "   AND CURRICULUM_CD = '{$curriculumCd}'";
            }
            $where .= "   AND SUBCLASSCD    = '{$subcd}'";
            $where .= "   AND SCHREGNO      = '{$schno}'";
            //更新前データ取得
            $query  = " SELECT * FROM RECORD_SCORE_DAT " . $where;
            $befRow = array();
            $befRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            //削除
            $query  = " DELETE FROM RECORD_SCORE_BEF_AFT_DAT " . $where;
            $db->query($query);
            //追加
            $data = array();
            $data["YEAR"][TEXT]             = $year;
            $data["SEMESTER"][TEXT]         = $semcd;
            $data["TESTKINDCD"][TEXT]       = $kindcd;
            $data["TESTITEMCD"][TEXT]       = $itemcd;
            $data["SCORE_DIV"][TEXT]        = $div;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]          = $classCd;
                $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
            }
            $data["SUBCLASSCD"][TEXT]       = $subcd;
            $data["SCHREGNO"][TEXT]         = $schno;
            $data["BEF_SCORE"][NUMBER]      = $befRow["SCORE"];
            $data["BEF_COMP_CREDIT"][NUMBER]= $befRow["COMP_CREDIT"];
            $data["BEF_GET_CREDIT"][NUMBER] = $befRow["GET_CREDIT"];
            $data["AFT_SCORE"][NUMBER]      = $model->fields["SCORE"][$i];
            $data["AFT_COMP_CREDIT"][NUMBER]= $model->fields["COMP_CREDIT"][$i];
            $data["AFT_GET_CREDIT"][NUMBER] = $model->fields["GET_CREDIT"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "RECORD_SCORE_BEF_AFT_DAT");
            $db->query($query);

            //RECORD_SCORE_DAT
            //項目
            $data = array();
            $data["SCORE"][NUMBER]          = $model->fields["SCORE"][$i];
            $data["COMP_CREDIT"][NUMBER]    = $model->fields["COMP_CREDIT"][$i];
            $data["GET_CREDIT"][NUMBER]     = $model->fields["GET_CREDIT"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //更新
            $where  = " WHERE YEAR          = '{$year}'";
            $where .= "   AND SEMESTER      = '{$semcd}'";
            $where .= "   AND TESTKINDCD    = '{$kindcd}'";
            $where .= "   AND TESTITEMCD    = '{$itemcd}'";
            $where .= "   AND SCORE_DIV     = '{$div}'";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= "   AND CLASSCD       = '{$classCd}'";
                $where .= "   AND SCHOOL_KIND   = '{$schoolKind}'";
                $where .= "   AND CURRICULUM_CD = '{$curriculumCd}'";
            }
            $where .= "   AND SUBCLASSCD    = '{$subcd}'";
            $where .= "   AND SCHREGNO      = '{$schno}'";
            $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
            $db->query($query);
        }

        Query::dbCheckIn($db);
    }

}
?>
