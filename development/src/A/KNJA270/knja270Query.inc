<?php

require_once('for_php7.php');

class knja270Query extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA270' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //学校名取得の取得(
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //年度取得
    function getSelectYear()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL  ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期マスタ取得
    function getSemesterMst($year)
    {
        $query  = " SELECT  ";
        $query .= " 	* ";
        $query .= " FROM ";
        $query .= " 	SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= " 	YEAR = '".$year."' AND ";
        $query .= " 	SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= " 	SEMESTER ";

        return $query;
    }

    //名称取得
    function getSelectName()
    {
        $query  = " SELECT NAMECD2,NAME1 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE NAMECD1 = 'C001' ";
        $query .= " AND NAMECD2 in ('2','4','5','6') ";
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }

    //近大判定用
    function getSelectKindai()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOLNAME1 like '%近畿大学%' AND ";
        $query .= "     SCHOOLNAME1 not like '%中学%' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model, $year,$semester)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T1.YEAR='" .$year ."'";
            $query .= "AND SEMESTER='".$semester ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T1.YEAR='" .$year ."'";
            $query .= "AND SEMESTER='".$semester ."' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校マスタの取得
    function getSchoolMst() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //講座担当職員数の取得
    function getStaffCount($model) {

        $query  = " SELECT ";
        $query .= "     COUNT(T3.STAFFCD) AS STAFFCNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T2 ON T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "        T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN CHAIR_STF_DAT T3 ON T1.YEAR = T3.YEAR AND ";
        $query .= "        T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "        T2.CHAIRCD = T3.CHAIRCD AND ";
        $query .= "        T3.CHARGEDIV = 1 ";
        $query .= "     INNER JOIN ATTEND_DAT T4 ON T1.SCHREGNO = T4.SCHREGNO AND ";
        $query .= "        T2.CHAIRCD = T4.CHAIRCD AND ";
        $query .= "        T4.ATTENDDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER BETWEEN '".$model->date_gakki."' AND '".$model->date_gakki2."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".implode($model->select_data["selectdata"],"','")."') AND ";
        $query .= "     T4.DI_CD <> '0' AND ";
        $query .= "     T4.ATTENDDATE BETWEEN '".str_replace("/","-",$model->field["DATE"])."' AND '".str_replace("/","-",$model->field["DATE2"])."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T4.ATTENDDATE, ";
        $query .= "     T4.PERIODCD, ";
        $query .= "     T2.CHAIRCD ";
        $query .= " ORDER BY ";
        $query .= "     STAFFCNT DESC ";

        return $query;
    }

    //校時名称取得
    function getPeriodcd($model, $nameCd2="") {
        $query  = " SELECT ";
        if ($nameCd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'B001' ";
        if ($nameCd2) {
            $query .= "     AND NAMECD2 = '{$nameCd2}' ";
        }

        return $query;
    }

    //CVS作成用のQUERY（勤怠毎のデータ）
    function selectCsvQuery($model)
    {
        $query .= " WITH SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME_SHOW ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER BETWEEN '".$model->date_gakki."' AND '".$model->date_gakki2."' AND ";
        $query .= "         T1.GRADE || T1.HR_CLASS IN ('" .implode($model->select_data["selectdata"], "','") ."') ";
        $query .= " ), ATTEND AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T2.ATTENDDATE, ";
        $query .= "         T2.PERIODCD, ";
        $query .= "         T2.CHAIRCD, ";
        $query .= "         CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD, ";
        $query .= "         ADD.DI_NAME1 AS DI_NAME, ";
        $query .= "         T2.DI_REMARK_CD, ";
        $query .= "         T2.DI_REMARK ";
        $query .= "     FROM ";
        $query .= "         SCHINFO T1 ";
        $query .= "         INNER JOIN ATTEND_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         INNER JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                        AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE ";
        $query .= "         T2.ATTENDDATE BETWEEN '".str_replace("/","-",$model->field["DATE"])."' AND '".str_replace("/","-",$model->field["DATE2"])."' AND ";
        $query .= "         ADD.REP_DI_CD <> '0' AND ";
        $query .= "         T2.YEAR        = '".CTRL_YEAR."' ";
        //テスト項目マスタの集計フラグの表
        $query .= " ), TEST_COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T2.COUNTFLG "; //0：集計しない 0以外：集計する
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST T1, ";
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ";
        } else {
            $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
        }
        $query .= "     WHERE ";
        $query .= "             T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "         AND T2.SCORE_DIV = '01' ";
        }
        $query .= " ),ATTEND_FLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         CASE WHEN DAT.DATADIV='2' THEN TEST.COUNTFLG ELSE T2.COUNTFLG END AS COUNTFLG ";
        $query .= "     FROM ";
        $query .= "         ATTEND T1 ";
        $query .= "         INNER JOIN SCH_CHR_DAT DAT ";
        $query .= "             ON  DAT.EXECUTEDATE = T1.ATTENDDATE ";
        $query .= "             AND DAT.PERIODCD    = T1.PERIODCD ";
        $query .= "             AND DAT.CHAIRCD     = T1.CHAIRCD ";
        $query .= "         LEFT JOIN TEST_COUNTFLG TEST ";
        $query .= "             ON  TEST.EXECUTEDATE = T1.ATTENDDATE ";
        $query .= "             AND TEST.PERIODCD    = T1.PERIODCD ";
        $query .= "             AND TEST.CHAIRCD     = T1.CHAIRCD ";
        $query .= "         LEFT JOIN SCH_CHR_COUNTFLG T2 ON ";
        $query .= "             T2.EXECUTEDATE = T1.ATTENDDATE AND  ";
        $query .= "             T2.PERIODCD = T1.PERIODCD AND  ";
        $query .= "             T2.CHAIRCD = T1.CHAIRCD AND  ";
        $query .= "             T2.GRADE = T1.GRADE  AND  ";
        $query .= "             T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " ),CHAIR AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.CHAIRCD, ";
        $query .= "         T3.CHAIRNAME, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.CLASSCD, ";
            $query .= "         T3.SCHOOL_KIND, ";
            $query .= "         T3.CURRICULUM_CD, ";
        }
        $query .= "         T3.SUBCLASSCD, ";
        $query .= "         T4.STAFFCD ";
        $query .= "     FROM ";
        $query .= "         SCHINFO T1 ";
        $query .= "         INNER JOIN CHAIR_STD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "           T2.SEMESTER = T1.SEMESTER ";
        $query .= "         INNER JOIN CHAIR_DAT T3 ON T2.YEAR = T3.YEAR AND ";
        $query .= "           T2.SEMESTER = T3.SEMESTER AND ";
        $query .= "           T2.CHAIRCD = T3.CHAIRCD ";
        $query .= "         INNER JOIN CHAIR_STF_DAT T4 ON T2.YEAR = T4.YEAR AND ";
        $query .= "           T2.SEMESTER = T4.SEMESTER AND ";
        $query .= "           T2.CHAIRCD = T4.CHAIRCD AND ";
        $query .= "           T4.CHARGEDIV = 1 ";
        $query .= "     WHERE ";
        $query .= "         T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     ORDER BY ";
        $query .= "         T4.STAFFCD ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.ATTENDDATE, ";
        $query .= "     T1.DI_CD, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T3.SUBCLASSNAME, ";
        $query .= "     T1.DI_NAME, ";
        $query .= "     T1.DI_REMARK_CD, ";
        $query .= "     T1.DI_REMARK, ";
        $query .= "     T1.COUNTFLG, ";
        $query .= "     T2.CHAIRNAME, ";
        $query .= "     T4.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     ATTEND_FLG T1, ";
        $query .= "     CHAIR T2, ";
        $query .= "     SUBCLASS_MST T3, ";
        $query .= "     STAFF_MST T4 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.CHAIRCD = T2.CHAIRCD AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD = T3.CLASSCD AND ";
            $query .= "     T2.SCHOOL_KIND = T3.SCHOOL_KIND AND ";
            $query .= "     T2.CURRICULUM_CD = T3.CURRICULUM_CD AND ";
        }
        $query .= "     T2.SUBCLASSCD = T3.SUBCLASSCD AND ";
        $query .= "     T2.STAFFCD = T4.STAFFCD ";
        $query .= " ORDER BY ";
        $query .= "     2, 3, 4, 6, 7, 8 ";

        return $query;
    }

    //CVS作成用のQUERY（学期別）
    function selectCsvQuery2($model, $knjSchoolMst)
    {
//NO004--->
        if ($model->schoolName == 'tosajoshi') {
            $absent_cov = $model->absent_cov;
            $absent_cov_late = $model->absent_cov_late;
        }

        $query  = "WITH SCHNO AS ( ";
        $query .= "    SELECT SCHREGNO,SEMESTER,GRADE,HR_CLASS,ATTENDNO ";
        $query .= "    FROM   SCHREG_REGD_DAT ";
        $query .= "    WHERE  YEAR = '".$model->field["YEAR"]."' ";
        $query .= "      AND  SEMESTER = '".$model->field["GAKKI"]."' ";
        $query .= "      AND  GRADE||HR_CLASS IN ('" .implode($model->select_data["selectdata"], "','") ."') ";
        $query .= "    ) ";
        $query .= ",ATTEND AS ( ";
        $query .= "    SELECT SCHREGNO, ";
        $query .= "           SUM(LESSON) LESSON, ";
        $query .= "           MAX(CASE WHEN MONTH BETWEEN '01' AND '03' ";
        $query .= "                    THEN RTRIM(CHAR(INT(YEAR)+1)) || '/' || MONTH || '/' || APPOINTED_DAY ";
        $query .= "                    ELSE YEAR || '/' || MONTH || '/' || APPOINTED_DAY END) AS MAXDAY, ";
        $query .= "           SUM(ABSENT) ABSENT, ";
        if ($model->virus) {
            if ($model->koudome) {
                $query .= "           CASE WHEN SUM(VIRUS) IS NULL AND SUM(KOUDOME) IS NULL THEN SUM(SUSPEND) ";
                $query .= "                WHEN SUM(VIRUS) IS NULL AND SUM(KOUDOME) IS NOT NULL THEN SUM(SUSPEND) + SUM(KOUDOME) ";
                $query .= "                WHEN SUM(VIRUS) IS NOT NULL AND SUM(KOUDOME) IS NULL THEN SUM(SUSPEND) + SUM(VIRUS) ";
                $query .= "                ELSE SUM(SUSPEND) + SUM(VIRUS) + SUM(KOUDOME) END AS SUSPEND, ";
            } else {
                $query .= "           CASE WHEN SUM(VIRUS) IS NULL THEN SUM(SUSPEND) ELSE SUM(SUSPEND) + SUM(VIRUS) END AS SUSPEND, ";
            }
        } else {
            if ($model->koudome) {
                $query .= "           CASE WHEN SUM(KOUDOME) IS NULL THEN SUM(SUSPEND) ELSE SUM(SUSPEND) + SUM(KOUDOME) END AS SUSPEND, ";
            } else {
                $query .= "           SUM(SUSPEND) SUSPEND, ";
            }
        }
        $query .= "           SUM(MOURNING) MOURNING, ";
        $query .= "           SUM(SICK) SICK, ";
        $query .= "           SUM(OFFDAYS) OFFDAYS, ";
        $query .= "           SUM(ABROAD) ABROAD, ";
        $query .= "           SUM(NOTICE) NOTICE, ";
        $query .= "           SUM(NONOTICE) NONOTICE, ";
        $query .= "           SUM(LATE) LATE, ";
        $query .= "           SUM(EARLY) EARLY ";
        if ($model->schoolName == 'tosajoshi') {
                $query .= "           ,SUM(TOCHU_KEKKA) KEKKA ";
        }
        if ($model->schoolName == 'tosajoshi') {
            $query .= " FROM  V_ATTEND_SEMES_DAT ";
        } else {
            $query .= " FROM  ATTEND_SEMES_DAT ";
        }
        $query .= "    WHERE  YEAR='".$model->field["YEAR"]."' ";
        $query .= "      AND  SEMESTER = '".$model->field["GAKKI"]."' ";
        $query .= "    GROUP BY SCHREGNO ";
        $query .= "    ) ";

        //メイン
        $query .= "SELECT T1.SCHREGNO,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO, ";
        $query .= "       T2.NAME_SHOW, ";
        $query .= "       LESSON,MAXDAY,ABSENT,SUSPEND,MOURNING,OFFDAYS,ABROAD,SICK,NOTICE,NONOTICE, ";
        if ($model->schoolName == 'tosajoshi') {
            $query .= "       KEKKA, ";
            $query .= "       LATE,EARLY, ";
            $query .= "       ( value(KEKKA, 0) + value(EARLY,0) ) AS SUM_LE_KEKKA ";
        } else {
            $query .= "       LATE,EARLY ";
        }

        $query .= "FROM   SCHNO T1 ";
        $query .= "       INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO=T2.SCHREGNO ";
        $query .= "       INNER JOIN ATTEND T3 ON T1.SCHREGNO=T3.SCHREGNO ";
        if ($model->field["OUTDIV"] == "on"){
            $query .= "WHERE ( ";
            $query .= "       ABSENT>0 ";
            $query .= "    or SUSPEND>0 ";
            $query .= "    or MOURNING>0 ";
            $query .= "    or SICK>0 ";
            $query .= "    or OFFDAYS>0 ";
            $query .= "    or ABROAD>0 ";
            $query .= "    or NONOTICE>0 ";
            $query .= "    or NOTICE>0 ";
            $query .= "    or LATE>0 ";
            $query .= "    or EARLY>0 ";
            $query .= "    ) ";
        }
        $query .= "ORDER BY T1.GRADE,T1.HR_CLASS,T1.ATTENDNO ";
//NO004<---

        return $query;
    }

    // 遅刻何回で欠課とするかの指数取得
    function getScAbsentCov($year) {
        $db = Query::dbCheckOut();
        $query = "SELECT absent_cov,absent_cov_late FROM school_mst WHERE year = '$year'";
        $absent = array();
        $absent = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $absent;
    }

    //CVS作成用のQUERY（科目別:累積データの対象学期のみ）
    function selectCsvQuery3($model, $knjSchoolMst)
    {

        $absent_cov = $model->absent_cov;
        $absent_cov_late = $model->absent_cov_late;
//NO004--->
        $query  = "WITH SCHNO AS ( ";
        $query .= "    SELECT SCHREGNO,SEMESTER,COURSECD,MAJORCD,COURSECODE,GRADE,HR_CLASS,ATTENDNO ";
        $query .= "    FROM   SCHREG_REGD_DAT ";
        $query .= "    WHERE  YEAR = '".$model->field["YEAR"]."' ";
        $query .= "      AND  SEMESTER = '".$model->field["GAKKI"]."' ";
        $query .= "      AND  GRADE||HR_CLASS IN ('" .implode($model->select_data["selectdata"], "','") ."') ";
        $query .= "    ) ";
        $query .= ",ATTEND AS ( ";
        $query .= "    SELECT SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           CLASSCD, ";
            $query .= "           SCHOOL_KIND, ";
            $query .= "           CURRICULUM_CD, ";
        }
        $query .= "           SUBCLASSCD, ";
        $query .= "           SUM(LESSON) LESSON, ";
        $query .= "           MAX(CASE WHEN MONTH BETWEEN '01' AND '03' ";
        $query .= "                    THEN RTRIM(CHAR(INT(YEAR)+1)) || '/' || MONTH || '/' || APPOINTED_DAY ";
        $query .= "                    ELSE YEAR || '/' || MONTH || '/' || APPOINTED_DAY END) AS MAXDAY, ";
        $query .= "           SUM(ABSENT) ABSENT, ";
        if ($model->virus) {
            if ($model->koudome) {
                $query .= "           CASE WHEN SUM(VIRUS) IS NULL AND SUM(KOUDOME) IS NULL THEN SUM(SUSPEND) ";
                $query .= "                WHEN SUM(VIRUS) IS NULL AND SUM(KOUDOME) IS NOT NULL THEN SUM(SUSPEND) + SUM(KOUDOME) ";
                $query .= "                WHEN SUM(VIRUS) IS NOT NULL AND SUM(KOUDOME) IS NULL THEN SUM(SUSPEND) + SUM(VIRUS) ";
                $query .= "                ELSE SUM(SUSPEND) + SUM(VIRUS) + SUM(KOUDOME) END AS SUSPEND, ";
            } else {
                $query .= "           CASE WHEN SUM(VIRUS) IS NULL THEN SUM(SUSPEND) ELSE SUM(SUSPEND) + SUM(VIRUS) END AS SUSPEND, ";
            }
        } else {
            if ($model->koudome) {
                $query .= "           CASE WHEN SUM(KOUDOME) IS NULL THEN SUM(SUSPEND) ELSE SUM(SUSPEND) + SUM(KOUDOME) END AS SUSPEND, ";
            } else {
                $query .= "           SUM(SUSPEND) SUSPEND, ";
            }
        }
        $query .= "           SUM(MOURNING) MOURNING, ";
        $query .= "           SUM(SICK) SICK, ";
        $query .= "           SUM(OFFDAYS) OFFDAYS, ";
        $query .= "           SUM(ABROAD) ABROAD, ";
        $query .= "           SUM(NOTICE) NOTICE, ";
        $query .= "           SUM(NONOTICE) NONOTICE, ";
        $query .= "           SUM(NURSEOFF) NURSEOFF, ";
        $query .= "           SUM(LATE) LATE, ";
        $query .= "           SUM(EARLY) EARLY ";

        $tmpSql = "";
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $tmpSql .= "            + value(sum(OFFDAYS), 0) ";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $tmpSql .= "            + value(sum(SUSPEND), 0) ";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $tmpSql .= "            + value(sum(VIRUS), 0) ";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $tmpSql .= "            + value(sum(KOUDOME), 0) ";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $tmpSql .= "            + value(sum(MOURNING), 0) ";
        }
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $tmpSql .= "            + value(sum(ABSENT), 0) ";
        }

        if (($absent_cov == "3" || $absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,decimal((float(sum(LATE) + sum(EARLY)) / ".$absent_cov_late.") + (value(sum(NOTICE), 0) + value(sum(NONOTICE), 0) + value(sum(SICK), 0) + value(sum(NURSEOFF), 0) ". $tmpSql;
            $query .= "      ),4,1) as NOTICE_LATE ";
        } elseif (($absent_cov == "1" || $absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,((sum(LATE) + sum(EARLY)) / ".$absent_cov_late.") + (value(sum(NOTICE), 0) + value(sum(NONOTICE), 0) + value(sum(SICK), 0) + value(sum(NURSEOFF), 0) ". $tmpSql;
            $query .= "      ) as NOTICE_LATE ";
        } else {
            $query .= "      ,value(sum(NOTICE),0) + value(sum(NONOTICE),0) + value(sum(SICK),0) + value(sum(NURSEOFF), 0) ". $tmpSql;
            $query .= "      as NOTICE_LATE ";
        }

        $query .= "     FROM  ATTEND_SUBCLASS_DAT ";
        $query .= "    WHERE  YEAR='".$model->field["YEAR"]."' ";
        $query .= "      AND  SEMESTER = '".$model->field["GAKKI"]."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    GROUP BY SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD ";
        } else {
            $query .= "    GROUP BY SCHREGNO,SUBCLASSCD ";
        }
        $query .= "    ) ";

        //メイン
        $query .= "SELECT T1.SCHREGNO,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO, ";
        $query .= "       T2.NAME_SHOW, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD AS SUBCLASSCD, ";
            $query .= "       T4.SUBCLASSNAME, ";
        } else {
            $query .= "       T4.SUBCLASSCD,T4.SUBCLASSNAME, ";
        }
        $query .= "      (SELECT CRE.CREDITS ";
        $query .= "         FROM CREDIT_MST CRE ";
        $query .= "        WHERE CRE.YEAR='".$model->field["YEAR"]."' ";
        $query .= "          AND CRE.COURSECD=T1.COURSECD ";
        $query .= "          AND CRE.MAJORCD=T1.MAJORCD ";
        $query .= "          AND CRE.GRADE=T1.GRADE ";
        $query .= "          AND CRE.COURSECODE=T1.COURSECODE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND CRE.CLASSCD=T3.CLASSCD ";
            $query .= "          AND CRE.SCHOOL_KIND=T3.SCHOOL_KIND ";
            $query .= "          AND CRE.CURRICULUM_CD=T3.CURRICULUM_CD ";
        }
        $query .= "          AND CRE.SUBCLASSCD=T3.SUBCLASSCD) as CREDITS, ";
        $query .= "       LESSON,MAXDAY,ABSENT,SUSPEND,MOURNING,OFFDAYS,ABROAD,SICK,NOTICE,NONOTICE, ";
        $query .= "       NURSEOFF, ";
        $query .= "       LATE,EARLY, ";
        $query .= "       NOTICE_LATE ";
        $query .= "FROM   SCHNO T1 ";
        $query .= "       INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO=T2.SCHREGNO ";
        $query .= "       INNER JOIN ATTEND T3 ON T1.SCHREGNO=T3.SCHREGNO  ";
        $query .= "       INNER JOIN SUBCLASS_MST T4 ON T3.SUBCLASSCD=T4.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND  T3.CLASSCD=T4.CLASSCD ";
            $query .= "  AND  T3.SCHOOL_KIND=T4.SCHOOL_KIND ";
            $query .= "  AND  T3.CURRICULUM_CD=T4.CURRICULUM_CD ";
        }
        if ($model->field["OUTDIV"] == "on"){
            $query .= " WHERE ( ";
            $query .= "       ABSENT>0 ";
            $query .= "    or SUSPEND>0 ";
            $query .= "    or MOURNING>0 ";
            $query .= "    or SICK>0 ";
            $query .= "    or OFFDAYS>0 ";
            $query .= "    or ABROAD>0 ";
            $query .= "    or NONOTICE>0 ";
            $query .= "    or NOTICE>0 ";
            $query .= "    or NURSEOFF>0 ";
            $query .= "    or LATE>0 ";
            $query .= "    or EARLY>0 ";
            $query .= "    ) ";
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "ORDER BY T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,SUBCLASSCD ";
        } else {
            $query .= "ORDER BY T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T4.SUBCLASSCD ";
        }

//NO004<---
        return $query;
    }

    //出欠集計開始日付などを取得---NO001
    function getAttendDate($model)
    {
        $query  = "SELECT SEMESTER ";
        $query .= "      ,MAX(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END) AS MAX_YEAR ";
        $query .= "      ,MONTH ";
        $query .= "      ,MAX(APPOINTED_DAY) AS MAX_APP ";
        $query .= "FROM   ATTEND_SEMES_DAT ";
        $query .= "WHERE  YEAR='".$model->field["YEAR"]."' AND SEMESTER <= '".$model->field["GAKKI"]."' ";
        $query .= "GROUP BY SEMESTER,MONTH ";
        $query .= "ORDER BY 2,3 ";

        return $query;
    }

    //CVS作成用のQUERY（科目別:対象学期までの累計）NO001
    function selectCsvQuery4($model, $semester, $month, $attend_sdate, $knjSchoolMst)
    {
        $absent_cov = $model->absent_cov;
        $absent_cov_late = $model->absent_cov_late;
        $query  = "WITH SCHNO AS ( ";
        $query .= "    SELECT SCHREGNO,COURSECD,MAJORCD,COURSECODE,GRADE,HR_CLASS,ATTENDNO ";
        $query .= "    FROM   SCHREG_REGD_DAT ";
        $query .= "    WHERE  YEAR = '".$model->field["YEAR"]."' ";
        $query .= "      AND  SEMESTER = '".$model->field["GAKKI"]."' ";
        $query .= "      AND  GRADE||HR_CLASS IN ('" .implode($model->select_data["selectdata"], "','") ."') ";
        $query .= "    ) ";
        $query .= ",ATTEND_SUBCLASS AS ( ";
        $query .= "    SELECT * ";
        $query .= "    FROM   ATTEND_SUBCLASS_DAT ";
        $query .= "    WHERE  YEAR='".$model->field["YEAR"]."' ";
        $query .= "      AND  SEMESTER <= '".$semester."' ";
        $query .= "      AND  MONTH IN ('" .implode($month, "','") ."') ";
        $query .= "    ) ";
        $query .= ",SCHEDULE AS( ";
        $query .= "    SELECT EXECUTEDATE, PERIODCD, CHAIRCD, DATADIV ";
        $query .= "    FROM   SCH_CHR_DAT  ";
        $query .= "    WHERE  EXECUTEDATE BETWEEN DATE('".$attend_sdate."') AND DATE('".str_replace("/","-",$model->field["ATTENDDATE"])."')  ";
        $query .= "    ) ";
        $query .= ",T_attend_dat AS( ";
        $query .= "     SELECT W1.SCHREGNO,W1.ATTENDDATE,W1.PERIODCD, ";
        $query .= "            CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END AS DI_CD, ";
        $query .= "            L1.MULTIPLY ";
        $query .= "     FROM   ATTEND_DAT W1  ";
        $query .= "            LEFT JOIN ATTEND_DI_CD_DAT L1 ON W1.YEAR = L1.YEAR AND L1.DI_CD = W1.DI_CD ";
        $query .= "     WHERE  W1.ATTENDDATE BETWEEN DATE('".$attend_sdate."') AND DATE('".str_replace("/","-",$model->field["ATTENDDATE"])."')  ";
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT T7  ";
        $query .= "                           WHERE  T7.SCHREGNO = W1.SCHREGNO  ";
        $query .= "                             AND  T7.TRANSFERCD IN('1','2')  ";
        $query .= "                             AND  W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE )  ";
        $query .= "    ) ";
        //テスト項目マスタの集計フラグの表
        $query .= " , TEST_COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         '2' AS DATADIV ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST T1, ";
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ";
        } else {
            $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
        }
        $query .= "     WHERE ";
        $query .= "             T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND T2.COUNTFLG   = '0' "; //0：集計しない 0以外：集計する
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "         AND T2.SCORE_DIV = '01' ";
        }
        $query .= "     ) ";
        $query .= ",T_attend AS( ";
        $query .= "SELECT S1.SCHREGNO  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      ,S1.CLASSCD  ";
            $query .= "      ,S1.SCHOOL_KIND  ";
            $query .= "      ,S1.CURRICULUM_CD  ";
        }
        $query .= "      ,S1.SUBCLASSCD  ";
        $query .= "      ,S1.SEMESTER  ";
        $query .= "      ,COUNT(*)  AS LESSON  ";
        $query .= "      ,SUM(CASE WHEN S3.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END)  AS  OFFDAYS  ";
        $query .= "      ,SUM(CASE WHEN S4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END)  AS  ABROAD  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8' THEN 1 ELSE 0 END)  AS  ABSENT  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9' THEN 1 ELSE 0 END)  AS  SUSPEND  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '19' THEN 1 WHEN '20' THEN 1 ELSE 0 END)  AS  VIRUS  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '25' THEN 1 WHEN '26' THEN 1 ELSE 0 END)  AS  KOUDOME  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF  ";
        $query .= "      ,SUM(CASE WHEN S2.DI_CD IN('15','23','24') THEN SMALLINT(VALUE(S2.MULTIPLY, '1')) ELSE 0 END)  AS  LATE  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)  AS  EARLY  ";
        $query .= "FROM  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " (SELECT T2.SCHREGNO ,T1.EXECUTEDATE, T1.PERIODCD, ";
            $query .= "         T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, T3.SUBCLASSCD, T2.SEMESTER ";
        } else {
            $query .= " (SELECT T2.SCHREGNO ,T1.EXECUTEDATE, T1.PERIODCD, SUBCLASSCD, T2.SEMESTER ";
        }
        $query .= "  FROM   SCHEDULE T1  ";
        $query .= "        ,CHAIR_STD_DAT T2  ";
        $query .= "        ,CHAIR_DAT T3  ";
        $query .= "        ,SCHNO T4  ";
        $query .= "  WHERE  T1.CHAIRCD  = T3.CHAIRCD  ";
        $query .= "    AND  T3.YEAR     = '".$model->field["YEAR"]."'  ";
        $query .= "    AND  T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE  ";
        $query .= "    AND  T2.YEAR     = '".$model->field["YEAR"]."'  ";
        $query .= "    AND  T2.SEMESTER = T3.SEMESTER  ";
        $query .= "    AND  T2.CHAIRCD  = T1.CHAIRCD  ";
        $query .= "    AND  T4.SCHREGNO = T2.SCHREGNO  ";
        $query .= "    AND     NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T5  ";
        $query .= "                    WHERE   T5.EXECUTEDATE = T1.EXECUTEDATE AND  ";
        $query .= "                            T5.PERIODCD = T1.PERIODCD AND  ";
        $query .= "                            T5.CHAIRCD = T1.CHAIRCD AND  ";
        $query .= "                            T5.GRADE = T4.GRADE AND  ";
        $query .= "                            T5.HR_CLASS = T4.HR_CLASS AND  ";
        $query .= "                            T1.DATADIV IN ('0','1') AND "; //テスト(DATADIV=2)以外
        $query .= "                            T5.COUNTFLG = '0') ";
        $query .= "    AND     NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ";
        $query .= "                        WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "                          AND TEST.PERIODCD    = T1.PERIODCD ";
        $query .= "                          AND TEST.CHAIRCD     = T1.CHAIRCD ";
        $query .= "                          AND TEST.DATADIV     = T1.DATADIV) "; //テスト(DATADIV=2)
        $query .= "    AND     NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6  ";
        $query .= "                    WHERE   T6.SCHREGNO = T4.SCHREGNO AND  ";
        $query .= "                          ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR  ";
        $query .= "                           (T6.ENT_DIV IN('4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) )  ";
        $query .= "    AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ADD ON L4.YEAR  = ADD.YEAR ";
        $query .= "                                                     AND L4.DI_CD = ADD.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                           L4.SCHREGNO   = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD   = T1.PERIODCD ";
        $query .= "                       AND ADD.REP_DI_CD = '27' "; // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "                  ) ";
        $query .= "    AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ADD ON L4.YEAR  = ADD.YEAR ";
        $query .= "                                                     AND L4.DI_CD = ADD.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                           L4.SCHREGNO   = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD   = T1.PERIODCD ";
        $query .= "                       AND ADD.REP_DI_CD = '28' "; // 勤怠コード'28'は時間割にカウントしない
        $query .= "                  ) ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD, T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, T3.SUBCLASSCD, T2.SEMESTER)S1  ";
        } else {
            $query .= "  GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD, SUBCLASSCD, T2.SEMESTER)S1  ";
        }
        $query .= " LEFT JOIN T_attend_dat S2 ON S2.SCHREGNO = S1.SCHREGNO AND S2.ATTENDDATE = S1.EXECUTEDATE AND S2.PERIODCD = S1.PERIODCD  ";
        $query .= " LEFT JOIN SCHREG_TRANSFER_DAT S3 ON S3.SCHREGNO = S1.SCHREGNO AND S3.TRANSFERCD = '2' AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE  ";
        $query .= " LEFT JOIN SCHREG_TRANSFER_DAT S4 ON S4.SCHREGNO = S1.SCHREGNO AND S4.TRANSFERCD = '1' AND S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "GROUP BY S1.SCHREGNO, S1.CLASSCD, S1.SCHOOL_KIND, S1.CURRICULUM_CD, S1.SUBCLASSCD, S1.SEMESTER ";
        } else {
            $query .= "GROUP BY S1.SCHREGNO, S1.SUBCLASSCD, S1.SEMESTER ";
        }
        $query .= "    ) ";
        $query .= ",ATTEND_SUM AS( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SELECT W1.SCHREGNO,W1.CLASSCD,W1.SCHOOL_KIND,W1.CURRICULUM_CD,W1.SUBCLASSCD,W1.SEMESTER, ";
        } else {
            $query .= "    SELECT W1.SCHREGNO,W1.SUBCLASSCD,W1.SEMESTER, ";
        }
        $query .= "           SUM(W1.LESSON) LESSON, ";
        $query .= "           SUM(W1.OFFDAYS) OFFDAYS, ";
        $query .= "           SUM(W1.ABROAD) ABROAD, ";
        $query .= "           SUM(W1.ABSENT) ABSENT, ";
        $query .= "           SUM(W1.SUSPEND) SUSPEND, ";
        $query .= "           SUM(W1.MOURNING) MOURNING, ";
        $query .= "           SUM(W1.SICK) SICK, ";
        if ($model->virus) {
            $query .= "           SUM(W1.VIRUS) VIRUS, ";
        }
        if ($model->koudome) {
            $query .= "           SUM(W1.KOUDOME) KOUDOME, ";
        }
        $query .= "           SUM(W1.NOTICE) NOTICE, ";
        $query .= "           SUM(W1.NONOTICE) NONOTICE, ";
        $query .= "           SUM(W1.NURSEOFF) NURSEOFF, ";
        $query .= "           SUM(W1.LATE) LATE, ";
        $query .= "           SUM(W1.EARLY) EARLY ";
        $query .= "    FROM   ATTEND_SUBCLASS W1, SCHNO W0 ";
        $query .= "    WHERE  W1.SCHREGNO = W0.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    GROUP BY W1.SCHREGNO,W1.CLASSCD,W1.SCHOOL_KIND,W1.CURRICULUM_CD,W1.SUBCLASSCD,W1.SEMESTER ";
            $query .= "     UNION ALL ";
            $query .= "    SELECT W2.SCHREGNO,W2.CLASSCD,W2.SCHOOL_KIND,W2.CURRICULUM_CD,W2.SUBCLASSCD,W2.SEMESTER, ";
        } else {
            $query .= "    GROUP BY W1.SCHREGNO,W1.SUBCLASSCD,W1.SEMESTER ";
            $query .= "     UNION ALL ";
            $query .= "    SELECT W2.SCHREGNO,W2.SUBCLASSCD,W2.SEMESTER, ";
        }
        $query .= "           W2.LESSON, ";
        $query .= "           W2.OFFDAYS, ";
        $query .= "           W2.ABROAD, ";
        $query .= "           W2.ABSENT, ";
        $query .= "           W2.SUSPEND, ";
        $query .= "           W2.MOURNING, ";
        $query .= "           W2.SICK, ";
        if ($model->virus) {
            $query .= "           W2.VIRUS, ";
        }
        if ($model->koudome) {
            $query .= "           W2.KOUDOME, ";
        }
        $query .= "           W2.NOTICE, ";
        $query .= "           W2.NONOTICE, ";
        $query .= "           W2.NURSEOFF, ";
        $query .= "           W2.LATE, ";
        $query .= "           W2.EARLY ";
        $query .= "     FROM   T_attend W2 ";
        $query .= "    ) ";
        //学期毎に清算
        $query .= ",ATTEND_SUM2 AS ( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SELECT SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SEMESTER, ";
        } else {
            $query .= "    SELECT SCHREGNO,SUBCLASSCD,SEMESTER, ";
        }
        $query .= "           SUM(LESSON) LESSON, ";
        $query .= "           SUM(OFFDAYS) OFFDAYS, ";
        $query .= "           SUM(ABROAD) ABROAD, ";
        $query .= "           SUM(ABSENT) ABSENT, ";
        $query .= "           SUM(SUSPEND) SUSPEND, ";
        $query .= "           SUM(MOURNING) MOURNING, ";
        $query .= "           SUM(SICK) SICK, ";
        if ($model->virus) {
            $query .= "           SUM(VIRUS) VIRUS, ";
        }
        if ($model->koudome) {
            $query .= "           SUM(KOUDOME) KOUDOME, ";
        }
        $query .= "           SUM(NOTICE) NOTICE, ";
        $query .= "           SUM(NONOTICE) NONOTICE, ";
        $query .= "           SUM(NURSEOFF) NURSEOFF, ";
        $query .= "           SUM(LATE) LATE, ";
        $query .= "           SUM(EARLY) EARLY ";

        $tmpSql = "";
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $tmpSql .= "            + value(sum(OFFDAYS), 0) ";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $tmpSql .= "            + value(sum(SUSPEND), 0) ";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $tmpSql .= "            + value(sum(VIRUS), 0) ";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $tmpSql .= "            + value(sum(KOUDOME), 0) ";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $tmpSql .= "            + value(sum(MOURNING), 0) ";
        }
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $tmpSql .= "            + value(sum(ABSENT), 0) ";
        }

        if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,decimal((float(sum(LATE) + sum(EARLY)) / ".$absent_cov_late.") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK) + sum(NURSEOFF) ". $tmpSql;
            $query .= "      ), 4, 1) as NOTICE_LATE ";
        } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,((sum(LATE) + sum(EARLY)) / ".$absent_cov_late.") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK) + sum(NURSEOFF) ". $tmpSql;
            $query .= "      ) as NOTICE_LATE ";
        } else {
            $query .= "      ,value(sum(NOTICE),0) + value(sum(NONOTICE),0) + value(sum(SICK),0) + value(sum(NURSEOFF),0) ". $tmpSql;
            $query .= "      as NOTICE_LATE ";
        }

        $query .= "    FROM   ATTEND_SUM ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    GROUP BY SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SEMESTER ";
        } else {
            $query .= "    GROUP BY SCHREGNO,SUBCLASSCD,SEMESTER ";
        }
        $query .= "    ) ";
        //年間で清算
        $query .= ",ATTEND_SUM3 AS ( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SELECT SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD, ";
        } else {
            $query .= "    SELECT SCHREGNO,SUBCLASSCD, ";
        }
        $query .= "           SUM(LESSON) LESSON, ";
        $query .= "           SUM(ABSENT) ABSENT, ";
        if ($model->virus) {
            if ($model->koudome) {
                $query .= "           CASE WHEN SUM(VIRUS) IS NULL AND SUM(KOUDOME) IS NULL THEN SUM(SUSPEND) ";
                $query .= "                WHEN SUM(VIRUS) IS NULL AND SUM(KOUDOME) IS NOT NULL THEN SUM(SUSPEND) + SUM(KOUDOME) ";
                $query .= "                WHEN SUM(VIRUS) IS NOT NULL AND SUM(KOUDOME) IS NULL THEN SUM(SUSPEND) + SUM(VIRUS) ";
                $query .= "                ELSE SUM(SUSPEND) + SUM(VIRUS) + SUM(KOUDOME) END AS SUSPEND, ";
            } else {
                $query .= "           CASE WHEN SUM(VIRUS) IS NULL THEN SUM(SUSPEND) ELSE SUM(SUSPEND) + SUM(VIRUS) END AS SUSPEND, ";
            }
        } else {
            if ($model->koudome) {
                $query .= "           CASE WHEN SUM(KOUDOME) IS NULL THEN SUM(SUSPEND) ELSE SUM(SUSPEND) + SUM(KOUDOME) END AS SUSPEND, ";
            } else {
                $query .= "           SUM(SUSPEND) SUSPEND, ";
            }
        }
        $query .= "           SUM(MOURNING) MOURNING, ";
        $query .= "           SUM(OFFDAYS) OFFDAYS, ";
        $query .= "           SUM(ABROAD) ABROAD, ";
        $query .= "           SUM(SICK) SICK, ";
        $query .= "           SUM(NOTICE) NOTICE, ";
        $query .= "           SUM(NONOTICE) NONOTICE, ";
        $query .= "           SUM(NURSEOFF) NURSEOFF, ";
        $query .= "           SUM(LATE) LATE, ";
        $query .= "           SUM(EARLY) EARLY ";
        $query .= "          ,SUM(NOTICE_LATE) NOTICE_LATE1 ";

        $tmpSql2 = "";
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $tmpSql2 .= "            + value(sum(OFFDAYS), 0) ";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $tmpSql2 .= "            + value(sum(SUSPEND), 0) ";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $tmpSql2 .= "            + value(sum(VIRUS), 0) ";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $tmpSql2 .= "            + value(sum(KOUDOME), 0) ";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $tmpSql2 .= "            + value(sum(MOURNING), 0) ";
        }
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $tmpSql2 .= "            + value(sum(ABSENT), 0) ";
        }

        if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,decimal((float(sum(LATE) + sum(EARLY)) / ".$absent_cov_late.") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK) + sum(NURSEOFF) ". $tmpSql2;
            $query .= "      ), 4, 1) as NOTICE_LATE2 ";
        } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,((sum(LATE) + sum(EARLY)) / ".$absent_cov_late.") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK) + sum(NURSEOFF) ". $tmpSql2;
            $query .= "      ) as NOTICE_LATE2 ";
        } else {
            $query .= "      ,value(sum(NOTICE),0) + value(sum(NONOTICE),0) + value(sum(SICK),0) + value(sum(NURSEOFF),0) ". $tmpSql2;
            $query .= "      as NOTICE_LATE2 ";
        }
        $query .= "    FROM   ATTEND_SUM2 ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    GROUP BY SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD ";
        } else {
            $query .= "    GROUP BY SCHREGNO,SUBCLASSCD ";
        }
        $query .= "    ) ";

        //メイン
        $query .= "SELECT T1.SCHREGNO,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO, ";
        $query .= "       T2.NAME_SHOW, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD AS SUBCLASSCD, ";
            $query .= "       T4.SUBCLASSNAME, ";
        } else {
            $query .= "       T4.SUBCLASSCD,T4.SUBCLASSNAME, ";
        }
        $query .= "      (SELECT CRE.CREDITS ";
        $query .= "         FROM CREDIT_MST CRE ";
        $query .= "        WHERE CRE.YEAR='".$model->field["YEAR"]."' ";
        $query .= "          AND CRE.COURSECD=T1.COURSECD ";
        $query .= "          AND CRE.MAJORCD=T1.MAJORCD ";
        $query .= "          AND CRE.GRADE=T1.GRADE ";
        $query .= "          AND CRE.COURSECODE=T1.COURSECODE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND CRE.CLASSCD=T3.CLASSCD ";
            $query .= "          AND CRE.SCHOOL_KIND=T3.SCHOOL_KIND ";
            $query .= "          AND CRE.CURRICULUM_CD=T3.CURRICULUM_CD ";
        }
        $query .= "          AND CRE.SUBCLASSCD=T3.SUBCLASSCD) as CREDITS, ";
        $query .= "       T3.LESSON,'".$model->field["ATTENDDATE"]."' AS MAXDAY, ";
        $query .= "       ABSENT,SUSPEND,MOURNING,OFFDAYS,ABROAD,SICK,NOTICE,NONOTICE,NURSEOFF, ";

        $noticeLate = ($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0) ? "NOTICE_LATE1" : "NOTICE_LATE2" ;

        $query .= "       LATE,EARLY, ";
        $query .= "       ".$noticeLate." AS NOTICE_LATE ";
        $query .= "FROM   SCHNO T1, ";
        $query .= "       SCHREG_BASE_MST T2, ";
        $query .= "       ATTEND_SUM3 T3 ";
        $query .= "      ,SUBCLASS_MST T4 ";
        $query .= "WHERE  T1.SCHREGNO=T2.SCHREGNO ";
        $query .= "  AND  T1.SCHREGNO=T3.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND  T3.CLASSCD=T4.CLASSCD ";
            $query .= "  AND  T3.SCHOOL_KIND=T4.SCHOOL_KIND ";
            $query .= "  AND  T3.CURRICULUM_CD=T4.CURRICULUM_CD ";
        }
        $query .= "  AND  T3.SUBCLASSCD=T4.SUBCLASSCD ";
        //NO004Add
        if ($model->field["OUTDIV"] == "on"){
            $query .= "and ( ";
            $query .= "       ABSENT>0 ";
            $query .= "    or SUSPEND>0 ";
            $query .= "    or MOURNING>0 ";
            $query .= "    or SICK>0 ";
            $query .= "    or OFFDAYS>0 ";
            $query .= "    or ABROAD>0 ";
            $query .= "    or NONOTICE>0 ";
            $query .= "    or NOTICE>0 ";
            $query .= "    or NURSEOFF>0 ";
            $query .= "    or LATE>0 ";
            $query .= "    or EARLY>0 ";
            $query .= "    ) ";
        }
        $query .= "ORDER BY 2,3,4,6 ";

        return $query;
    }
    //CVS作成用のQUERY（欠席者、日付、校時指定のデータ）
    function selectCsvQuery5($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     ATT.CHAIRCD, ";
        $query .= "     BASE.NAME_SHOW, ";
        $query .= "     ADD.DI_NAME1 AS DI_NAME ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAT ATT ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON ATT.SCHREGNO = REGD.SCHREGNO ";
        $query .= "                                   AND ATT.YEAR     = REGD.YEAR ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON ATT.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN ATTEND_DI_CD_DAT ADD ON ATT.YEAR  = ADD.YEAR ";
        $query .= "                                   AND ATT.DI_CD = ADD.DI_CD ";
        $query .= " WHERE ";
        $query .= "         ATT.ATTENDDATE = '".str_replace("/","-",$model->field["ABBDATE"])."' ";
        $query .= "     AND ADD.REP_DI_CD <> '0' ";
        $query .= "     AND ATT.PERIODCD   = '".$model->field["PERIODCD"]."' ";
        $query .= "     AND ATT.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS IN ('" .implode($model->select_data["selectdata"], "','") ."') ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }
}
?>
