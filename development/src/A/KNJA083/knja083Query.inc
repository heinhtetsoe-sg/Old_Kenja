<?php

require_once('for_php7.php');
class knja083Query extends Query
{
    //年度・学期取得
    public function getYearSemester($model, $flg)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1, ";
        $query .= "     (SELECT ";
        $query .= "         MAX(SEMESTER) AS MAX_SEM ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER <> '9' ";
        $query .= "     ) T2 ";
        $query .= " WHERE ";
        if ($flg == "new") {
            $query .= "     T1.YEAR     = (CASE WHEN MAX_SEM = '".CTRL_SEMESTER."' THEN '".(CTRL_YEAR+1)."' ELSE '".CTRL_YEAR."' END) AND ";
            $query .= "     T1.SEMESTER = (CASE WHEN MAX_SEM = '".CTRL_SEMESTER."' THEN '1' ELSE '".(CTRL_SEMESTER+1)."' END) ";
        } else {
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = T2.MAX_SEM ";
        }

        return $query;
    }

    //学年取得
    public function getGrade($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     (CASE WHEN T2.SEMESTER = '1' THEN '新' ELSE '' END) || T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     INNER JOIN CLASS_FORMATION_DAT T2 ";
        $query .= "          ON T1.YEAR     = T2.YEAR ";
        $query .= "         AND T2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "         AND T1.GRADE    = T2.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } else {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //開始学年取得
    public function getStartGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";

        return $query;
    }

    //クラス取得
    public function getHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース取得
    public function getCourseList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ";
        $query .= "     T2.COURSENAME || T2.MAJORNAME || T3.COURSECODENAME AS COURSENAME, ";
        $query .= "     T1.CNT ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         YEAR, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE, ";
        $query .= "         COUNT(SCHREGNO) AS CNT ";
        $query .= "     FROM ";
        $query .= "         CLASS_FORMATION_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$model->field["YEAR"]."' AND ";
        $query .= "         SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         GRADE       = '".$model->field["GRADE"]."' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE ";
        $query .= "     ) T1 ";
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST T2 ON T1.YEAR = T2.YEAR AND T1.COURSECD = T2.COURSECD AND T1.MAJORCD = T2.MAJORCD ";
        $query .= "     INNER JOIN V_COURSECODE_MST T3 ON T1.YEAR = T3.YEAR AND T1.COURSECODE = T3.COURSECODE ";
        $query .= " ORDER BY ";
        $query .= "     COURSE ";

        return $query;
    }

    //履修パターン取得
    public function getPatternList($model, $flg = "")
    {
        $query  = " WITH SCH_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.*, ";
        $query .= "         T3.PATTERN_NAME ";
        $query .= "     FROM ";
        $query .= "         CLASS_FORMATION_DAT T1, ";
        $query .= "         COMP_CREDITS_PATTERN_STD_COURSE_DAT T2 ";
        $query .= "         INNER JOIN COMP_CREDITS_PATTERN_COURSE_MST T3 ";
        $query .= "              ON T2.YEAR         = T3.YEAR ";
        $query .= "             AND T2.PATTERN_CD   = T3.PATTERN_CD ";
        $query .= "             AND T2.GRADE        = T3.GRADE ";
        $query .= "             AND T2.COURSECD     = T3.COURSECD ";
        $query .= "             AND T2.MAJORCD      = T3.MAJORCD ";
        $query .= "             AND T2.COURSECODE   = T3.COURSECODE ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.YEAR         = '".$model->field["YEAR"]."' AND ";
        $query .= "         T1.SEMESTER     = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.GRADE        = T2.GRADE AND ";
        $query .= "         T1.GRADE        = '".$model->field["GRADE"]."' AND ";
        $query .= "         T1.COURSECD     = T2.COURSECD AND ";
        $query .= "         T1.MAJORCD      = T2.MAJORCD AND ";
        $query .= "         T1.COURSECODE   = T2.COURSECODE AND ";
        $query .= "         T1.SCHREGNO     = T2.SCHREGNO ";
        $query .= " ) ";

        if ($flg) {
            $query .= " , SUBMAIN AS ( ";
        }
        $query .= " SELECT ";
        $query .= "     T2.PATTERN_CD AS COURSE, ";
        $query .= "     T2.PATTERN_NAME AS COURSENAME, ";
        if ($flg) {
            $query .= "     T2.SCHREGNO ";
        } else {
            $query .= "     COUNT(T2.SCHREGNO) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         MAX(RIREKI_CODE) AS RIREKI_CODE ";
        $query .= "     FROM ";
        $query .= "         SCH_DATA ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= "     ) T1, ";
        $query .= "     SCH_DATA T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "     T1.RIREKI_CODE  = T2.RIREKI_CODE ";
        if ($flg) {
            $query .= " ) ";
        } else {
            $query .= " GROUP BY ";
            $query .= "     T2.PATTERN_CD, ";
            $query .= "     T2.PATTERN_NAME ";
            $query .= " ORDER BY ";
            $query .= "     T2.PATTERN_CD ";
        }

        return $query;
    }

    //処理結果一覧取得
    public function getList($model)
    {
        $query  = " WITH SUBMAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         CASE WHEN L2.SEX IS NOT NULL THEN L2.SEX ELSE L1.SEX END AS SEX, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CLASS_FORMATION_DAT T1 ";
        $query .= "         LEFT JOIN FRESHMAN_DAT L1 ON T1.YEAR = L1.ENTERYEAR AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST L2 ON T1.SCHREGNO = L2.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".$model->field["YEAR"]."' AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.GRADE    = '".$model->field["GRADE"]."' AND ";
        $query .= "         T1.HR_CLASS IS NOT NULL ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS BOY, ";
        $query .= "         SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS GIRL, ";
        $query .= "         COUNT(SCHREGNO) AS TOTAL ";
        $query .= "     FROM ";
        $query .= "         SUBMAIN ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.COURSENAME || T2.MAJORNAME || T3.COURSECODENAME AS COURSENAME, ";
        $query .= "     T4.HR_NAME ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST T2 ON T1.YEAR = T2.YEAR AND T1.COURSECD = T2.COURSECD AND T1.MAJORCD = T2.MAJORCD ";
        $query .= "     INNER JOIN V_COURSECODE_MST T3 ON T1.YEAR = T3.YEAR AND T1.COURSECODE = T3.COURSECODE ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T4 ON T1.YEAR = T4.YEAR AND T1.SEMESTER = T4.SEMESTER AND T1.GRADE = T4.GRADE AND T1.HR_CLASS = T4.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS ";

        return $query;
    }

    //処理結果一覧取得
    public function getList2($model)
    {
        $query  = " WITH SCH_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.RIREKI_CODE, ";
        $query .= "         T2.PATTERN_CD, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T3.PATTERN_NAME AS COURSENAME ";
        $query .= "     FROM ";
        $query .= "         CLASS_FORMATION_DAT T1, ";
        $query .= "         COMP_CREDITS_PATTERN_STD_COURSE_DAT T2 ";
        $query .= "         INNER JOIN COMP_CREDITS_PATTERN_COURSE_MST T3 ";
        $query .= "              ON T2.YEAR         = T3.YEAR ";
        $query .= "             AND T2.PATTERN_CD   = T3.PATTERN_CD ";
        $query .= "             AND T2.GRADE        = T3.GRADE ";
        $query .= "             AND T2.COURSECD     = T3.COURSECD ";
        $query .= "             AND T2.MAJORCD      = T3.MAJORCD ";
        $query .= "             AND T2.COURSECODE   = T3.COURSECODE ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.YEAR         = '".$model->field["YEAR"]."' AND ";
        $query .= "         T1.SEMESTER     = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.GRADE        = T2.GRADE AND ";
        $query .= "         T1.GRADE        = '".$model->field["GRADE"]."' AND ";
        $query .= "         T1.COURSECD     = T2.COURSECD AND ";
        $query .= "         T1.MAJORCD      = T2.MAJORCD AND ";
        $query .= "         T1.COURSECODE   = T2.COURSECODE AND ";
        $query .= "         T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "         T1.HR_CLASS IS NOT NULL ";
        $query .= " ), SUBMAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T2.PATTERN_CD, ";
        $query .= "         T2.COURSENAME, ";
        $query .= "         T2.GRADE, ";
        $query .= "         T2.HR_CLASS, ";
        $query .= "         L1.SEX ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             MAX(RIREKI_CODE) AS RIREKI_CODE ";
        $query .= "         FROM ";
        $query .= "             SCH_DATA ";
        $query .= "         GROUP BY ";
        $query .= "             SCHREGNO ";
        $query .= "         ) T1, ";
        $query .= "         SCH_DATA T2 ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST L1 ON T2.SCHREGNO = L1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "         T1.RIREKI_CODE  = T2.RIREKI_CODE ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         PATTERN_CD, ";
        $query .= "         COURSENAME, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS BOY, ";
        $query .= "         SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS GIRL, ";
        $query .= "         COUNT(SCHREGNO) AS TOTAL ";
        $query .= "     FROM ";
        $query .= "         SUBMAIN ";
        $query .= "     GROUP BY ";
        $query .= "         PATTERN_CD, ";
        $query .= "         COURSENAME, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.HR_NAME ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "          ON T2.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= "         AND T2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "         AND T1.GRADE    = T2.GRADE ";
        $query .= "         AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     T1.PATTERN_CD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS ";

        return $query;
    }

    //割り振り対象生徒取得
    public function getSchList($model)
    {
        $query  = "";
        if (!$model->field["S_GRADE"]) {
            $query .= knja083Query::getPatternList($model, "data");
        }
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        if ($model->field["S_GRADE"]) {
            $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ";
            $query .= "     CASE WHEN T1.SCORE IS NULL THEN 0 ELSE 1 END AS SCORE_FLG, ";
            $query .= "     T1.SCORE, ";
        } else {
            $query .= "     T2.COURSE, ";
            $query .= "     CASE WHEN L3.AVG IS NULL THEN 0 ELSE 1 END AS SCORE_FLG, ";
            $query .= "     L3.AVG, ";
        }
        $query .= "     CASE WHEN L2.SEX IS NOT NULL THEN L2.SEX ELSE L1.SEX END AS SEX ";
        $query .= " FROM ";
        $query .= "     CLASS_FORMATION_DAT T1 ";
        if (!$model->field["S_GRADE"]) {
            $query .= "     INNER JOIN SUBMAIN T2 ON  T1.SCHREGNO = T2.SCHREGNO ";
        }
        $query .= "     LEFT JOIN FRESHMAN_DAT L1 ON T1.YEAR = L1.ENTERYEAR AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L2 ON T1.SCHREGNO = L2.SCHREGNO ";
        $query .= "     LEFT JOIN RECORD_RANK_SDIV_DAT L3 ";
        $query .= "          ON T1.SCHREGNO     = L3.SCHREGNO ";
        $query .= "         AND L3.YEAR         = '".CTRL_YEAR."' ";
        $query .= "         AND L3.SEMESTER     = '9' ";
        $query .= "         AND L3.TESTKINDCD   = '99' ";
        $query .= "         AND L3.TESTITEMCD   = '00' ";
        $query .= "         AND L3.SCORE_DIV    = '09' ";
        $query .= "         AND L3.SUBCLASSCD   = '999999' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.SEMESTER     = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE        = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     COURSE, ";
        $query .= "     SEX, ";
        $query .= "     SCORE_FLG DESC, ";
        if ($model->field["S_GRADE"]) {
            $query .= "     T1.SCORE DESC, ";
        } else {
            $query .= "     L3.AVG DESC, ";
        }
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //割り振り処理
    public function getExecuteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //対象学年の生徒データ取得
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CLASS_FORMATION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' ";

        //クラスをクリア
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //項目
            $data = array();
            $data["HR_CLASS"][TEXT]     = null;
            $data["REGISTERCD"][TEXT]   = STAFFCD ;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->field["YEAR"]."' AND ";
            $where .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
            $where .= "     SCHREGNO    = '".$row["SCHREGNO"]."'";

            $query = Query::updateSQL($data, "CLASS_FORMATION_DAT", $where);
            $db->query($query);
        }
        $result->free();

        //生徒一覧取得
        $schArray = array();
        $query = knja083Query::getSchList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schArray[$row["COURSE"]][$row["SEX"]][] = $row["SCHREGNO"];
        }
        $result->free();

        $hr = array();
        for ($i = 0; $i < get_count($model->data["COURSE"]); $i++) {
            $course         = $model->data["COURSE"][$i];
            $class_cnt      = $model->fields["CLASS_CNT"][$i];
            $target_class   = explode(',', $model->fields["TARGET_CLASS"][$i]);

            if ($class_cnt > 0) {
                $amari = array();
                for ($sex = 1; $sex <= 2; $sex++) {
                    $counter = 0;
                    $start  = ($sex == "1") ? 0 : $class_cnt;
                    $end    = ($sex == "1") ? $class_cnt : ($class_cnt * 2);
                    for ($j = $start; $j < $end; $j++) {
                        $amari[$sex ][$j] = $counter;
                        $counter++;
                    }

                    $counter = 0;
                    $start  = ($sex == "1") ? ($class_cnt * 2)-1 : ($class_cnt-1);
                    $end    = ($sex == "1") ? $class_cnt : 0;
                    for ($j = $start; $j >= $end; $j--) {
                        $amari[$sex][$j] = $counter;
                        $counter++;
                    }

                    foreach ($schArray[$course][$sex] as $rank => $schregno) {
                        $hr[$schregno] = $target_class[$amari[$sex][$rank % ($class_cnt*2)]];

                        //項目
                        $data = array();
                        $data["HR_CLASS"][TEXT]     = $hr[$schregno];
                        $data["REGISTERCD"][TEXT]   = STAFFCD ;
                        $data["UPDATED"][FUNC]      = "sysdate()";

                        $where  = " WHERE ";
                        $where .= "     YEAR        = '".$model->field["YEAR"]."' AND ";
                        $where .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
                        $where .= "     SCHREGNO    = '".$schregno."'";

                        $query = Query::updateSQL($data, "CLASS_FORMATION_DAT", $where);
                        $db->query($query);
                    }
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //CSVデータ取得
    public function getCsvQuery($model)
    {
        $query  = "";
        if (!$model->field["S_GRADE"]) {
            $query .= knja083Query::getPatternList($model, "data");
        }
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     CASE WHEN L2.NAME IS NOT NULL THEN L2.NAME ELSE L1.NAME END AS NAME, ";
        $query .= "     CASE WHEN L2.NAME IS NOT NULL THEN L2.NAME_KANA ELSE L1.NAME_KANA END AS NAME_KANA, ";
        $query .= "     CASE WHEN L2.NAME IS NOT NULL THEN L2.SEX ELSE L1.SEX END AS SEX, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.REMAINGRADE_FLG, ";
        $query .= "     T1.OLD_SCHREGNO, ";
        $query .= "     T1.OLD_GRADE, ";
        $query .= "     T1.OLD_HR_CLASS, ";
        $query .= "     T1.OLD_ATTENDNO, ";
        $query .= "     T1.SCORE, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn.", ";
        $query .= "     L3.AVG AS VALUE_AVG, ";
        $query .= "     L4.AVG AS SCORE_AVG ";
        if (!$model->field["S_GRADE"]) {
            $query .= "    ,T2.COURSE ";
            $query .= "    ,T2.COURSENAME ";
        }
        $query .= " FROM ";
        $query .= "     CLASS_FORMATION_DAT T1 ";
        if (!$model->field["S_GRADE"]) {
            $query .= "     INNER JOIN SUBMAIN T2 ON  T1.SCHREGNO = T2.SCHREGNO ";
        }
        $query .= "     LEFT JOIN FRESHMAN_DAT L1 ON T1.YEAR = L1.ENTERYEAR AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L2 ON T1.SCHREGNO = L2.SCHREGNO ";
        $query .= "     LEFT JOIN RECORD_RANK_SDIV_DAT L3 ";
        $query .= "          ON T1.SCHREGNO     = L3.SCHREGNO ";
        $query .= "         AND L3.YEAR         = '".CTRL_YEAR."' ";
        $query .= "         AND L3.SEMESTER     = '9' ";
        $query .= "         AND L3.TESTKINDCD   = '99' ";
        $query .= "         AND L3.TESTITEMCD   = '00' ";
        $query .= "         AND L3.SCORE_DIV    = '09' ";
        $query .= "         AND L3.SUBCLASSCD   = '999999' ";
        $query .= "     LEFT JOIN RECORD_RANK_SDIV_DAT L4 ";
        $query .= "          ON T1.SCHREGNO     = L4.SCHREGNO ";
        $query .= "         AND L4.YEAR         = '".CTRL_YEAR."' ";
        $query .= "         AND L4.SEMESTER     = '9' ";
        $query .= "         AND L4.TESTKINDCD   = '99' ";
        $query .= "         AND L4.TESTITEMCD   = '00' ";
        $query .= "         AND L4.SCORE_DIV    = '08' ";
        $query .= "         AND L4.SUBCLASSCD   = '999999' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        if (!$model->field["S_GRADE"]) {
            $query .= "     T2.COURSE, ";
        }
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
