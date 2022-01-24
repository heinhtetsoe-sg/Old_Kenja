<?php

require_once('for_php7.php');

class knjd141aQuery extends Query
{
    //年度リスト
    public function getYearList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR || '年度' AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }
    //学期リスト
    public function getSemesterList($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }
    //考査リスト
    public function getKousaList($model, $year, $semester)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV || ':' || TESTITEMNAME AS LABEL, ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND SCORE_DIV = '01' ";
        $query .= " ORDER BY ";
        $query .= "     TESTKINDCD, TESTITEMCD, SCORE_DIV ";

        return $query;
    }
    //科目リスト
    public function getKamokuList($model, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || T2.SUBCLASSNAME AS LABEL, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_YDAT T1 ";
        $query .= " LEFT JOIN SUBCLASS_MST T2 ";
        $query .= "     ON T1.CLASSCD = T2.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //登録データ取得
    public function selectQuery($model, $chaircd = '')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME, ";
        $query .= "     T6.ATTENTION_SCORE, ";
        $query .= "     T6.YEAR, ";
        $query .= "     T5.CLASSCD, ";
        $query .= "     T5.SCHOOL_KIND, ";
        $query .= "     T5.CURRICULUM_CD, ";
        $query .= "     T5.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T4 ";
        $query .= "     ON T1.YEAR = T4.YEAR ";
        $query .= "     AND T1.SEMESTER = T4.SEMESTER ";
        $query .= "     AND T4.SCORE_DIV = '01' ";
        $query .= " INNER JOIN SUBCLASS_YDAT T5 ";
        $query .= "     ON T1.YEAR = T5.YEAR ";
        $query .= "     AND T1.CLASSCD = T5.CLASSCD  ";
        $query .= "     AND T1.SCHOOL_KIND = T5.SCHOOL_KIND  ";
        $query .= "     AND T1.CURRICULUM_CD = T5.CURRICULUM_CD  ";
        $query .= "     AND T1.SUBCLASSCD = T5.SUBCLASSCD ";
        $query .= " LEFT JOIN CHAIR_ATTENTION_SCORE_MST T6 ";
        $query .= "     ON T1.YEAR = T6.YEAR ";
        $query .= "     AND T1.SEMESTER = T6.SEMESTER ";
        $query .= "     AND T1.CHAIRCD = T6.CHAIRCD ";
        $query .= "     AND T4.TESTKINDCD = T6.TESTKINDCD ";
        $query .= "     AND T4.TESTITEMCD = T6.TESTITEMCD ";
        $query .= "     AND T4.SCORE_DIV = T6.SCORE_DIV ";
        $query .= "     AND T5.CLASSCD = T6.CLASSCD ";
        $query .= "     AND T5.SCHOOL_KIND = T6.SCHOOL_KIND ";
        $query .= "     AND T5.CURRICULUM_CD = T6.CURRICULUM_CD ";
        $query .= "     AND T5.SUBCLASSCD = T6.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["taisyouSemester"]}' ";
        $query .= "     AND T4.TESTKINDCD = '{$model->testkindcd}' ";
        $query .= "     AND T4.TESTITEMCD = '{$model->testitemcd}' ";
        $query .= "     AND T4.SCORE_DIV = '{$model->scoreDiv}' ";
        if ($model->field['taisyouKamoku'] != 'all') {
            $query .= "     AND T5.CLASSCD = '{$model->classcd}' ";
            $query .= "     AND T5.SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= "     AND T5.CURRICULUM_CD = '{$model->curriculumCd}' ";
            $query .= "     AND T5.SUBCLASSCD = '{$model->subclasscd}' ";
        }
        if ($chaircd != '') {
            $query .= "     AND T1.CHAIRCD = '{$chaircd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }
    //登録データ取得(担任)
    public function selectStaff($model, $chaircd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.STAFFCD, ";
        $query .= "     CASE WHEN T2.CHARGEDIV = '1' THEN '正' ELSE '副' END AS CHARGEDIV, ";
        $query .= "     T3.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " LEFT JOIN CHAIR_STF_DAT T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "     AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= " LEFT JOIN STAFF_MST T3 ";
        $query .= "     ON T2.STAFFCD = T3.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["taisyouSemester"]}' ";
        $query .= "     AND T1.CHAIRCD = '{$chaircd}' ";
        $query .= " ORDER BY ";
        $query .= "     T2.STAFFCD ";

        return $query;
    }
    //データの更新
    public function getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM CHAIR_ATTENTION_SCORE_MST ";
        $query .= " WHERE ";
        $query .= "    YEAR = '{$model->year}' ";
        $query .= "    AND SEMESTER = '{$model->field["taisyouSemester"]}' ";
        $query .= "    AND TESTKINDCD = '{$model->testkindcd}' ";
        $query .= "    AND TESTITEMCD = '{$model->testitemcd}' ";
        $query .= "    AND SCORE_DIV = '{$model->scoreDiv}' ";
        if ($model->field['taisyouKamoku'] != 'all') {
            $query .= "    AND CLASSCD = '{$model->classcd}' ";
            $query .= "    AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= "    AND CURRICULUM_CD = '{$model->curriculumCd}' ";
            $query .= "    AND SUBCLASSCD = '{$model->subclasscd}' ";
        }
        $db->query($query);

        Query::dbCheckIn($db);
        return true;
    }
    //データの更新
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data); $i++) {
            if ($model->data[$i]['ATTENTION_SCORE'] =='') {
                continue;
            }
            $data = array();
            $data["CHAIRCD"][TEXT] = $model->data[$i]['CHAIRCD'];
            $data["ATTENTION_SCORE"][TEXT] = $model->data[$i]['ATTENTION_SCORE'];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $row = $db->getRow(knjd141aQuery::selectQuery($model, $model->data[$i]['CHAIRCD']), DB_FETCHMODE_ASSOC);
            $data["YEAR"][TEXT]          = $model->year;
            $data["SEMESTER"][TEXT]      = $model->field["taisyouSemester"];
            $data["TESTKINDCD"][TEXT]    = $model->testkindcd;
            $data["TESTITEMCD"][TEXT]    = $model->testitemcd;
            $data["SCORE_DIV"][TEXT]     = $model->scoreDiv;
            $data["CLASSCD"][TEXT]       = $row['CLASSCD'];
            $data["SCHOOL_KIND"][TEXT]   = $row['SCHOOL_KIND'];
            $data["CURRICULUM_CD"][TEXT] = $row['CURRICULUM_CD'];
            $data["SUBCLASSCD"][TEXT]    = $row['SUBCLASSCD'];

            $query = Query::insertSQL($data, "CHAIR_ATTENTION_SCORE_MST");
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return true;
    }
    //登録データ取得
    public function selectQuery2($model, $chaircd = '')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME, ";
        $query .= "     T6.ATTENTION_SCORE, ";
        $query .= "     T6.YEAR, ";
        $query .= "     T5.CLASSCD, ";
        $query .= "     T5.SCHOOL_KIND, ";
        $query .= "     T5.CURRICULUM_CD, ";
        $query .= "     T5.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T4 ";
        $query .= "     ON T1.YEAR = T4.YEAR ";
        $query .= "     AND T1.SEMESTER = T4.SEMESTER ";
        $query .= "     AND T4.SCORE_DIV = '01' ";
        $query .= " INNER JOIN SUBCLASS_YDAT T5 ";
        $query .= "     ON T1.YEAR = T5.YEAR ";
        $query .= "     AND T1.CLASSCD = T5.CLASSCD  ";
        $query .= "     AND T1.SCHOOL_KIND = T5.SCHOOL_KIND  ";
        $query .= "     AND T1.CURRICULUM_CD = T5.CURRICULUM_CD  ";
        $query .= "     AND T1.SUBCLASSCD = T5.SUBCLASSCD ";
        $query .= " INNER JOIN CHAIR_ATTENTION_SCORE_MST T6 ";
        $query .= "     ON T1.YEAR = T6.YEAR ";
        $query .= "     AND T1.SEMESTER = T6.SEMESTER ";
        $query .= "     AND T1.CHAIRCD = T6.CHAIRCD ";
        $query .= "     AND T4.TESTKINDCD = T6.TESTKINDCD ";
        $query .= "     AND T4.TESTITEMCD = T6.TESTITEMCD ";
        $query .= "     AND T4.SCORE_DIV = T6.SCORE_DIV ";
        $query .= "     AND T5.CLASSCD = T6.CLASSCD ";
        $query .= "     AND T5.SCHOOL_KIND = T6.SCHOOL_KIND ";
        $query .= "     AND T5.CURRICULUM_CD = T6.CURRICULUM_CD ";
        $query .= "     AND T5.SUBCLASSCD = T6.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field['sansyouYear']}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field['sansyouSemester']}' ";
        $query .= "     AND T4.TESTKINDCD = '{$model->sansyouTestkindcd}' ";
        $query .= "     AND T4.TESTITEMCD = '{$model->sansyouTestitemcd}' ";
        $query .= "     AND T4.SCORE_DIV = '{$model->sansyouScoreDiv}' ";
        if ($model->field['sansyouKamoku'] != 'all') {
            $query .= "     AND T5.CLASSCD = '{$model->sansyouClasscd}' ";
            $query .= "     AND T5.SCHOOL_KIND = '{$model->sansyouSchoolKind}' ";
            $query .= "     AND T5.CURRICULUM_CD = '{$model->sansyouCurriculumCd}' ";
            $query .= "     AND T5.SUBCLASSCD = '{$model->sansyouSubclasscd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }
    public function selectQuery3($model, $chaircd = '')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME, ";
        $query .= "     T6.ATTENTION_SCORE, ";
        $query .= "     T6.YEAR, ";
        $query .= "     T5.CLASSCD, ";
        $query .= "     T5.SCHOOL_KIND, ";
        $query .= "     T5.CURRICULUM_CD, ";
        $query .= "     T5.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T4 ";
        $query .= "     ON T1.YEAR = T4.YEAR ";
        $query .= "     AND T1.SEMESTER = T4.SEMESTER ";
        $query .= "     AND T4.SCORE_DIV = '01' ";
        $query .= " INNER JOIN SUBCLASS_YDAT T5 ";
        $query .= "     ON T1.YEAR = T5.YEAR ";
        $query .= "     AND T1.CLASSCD = T5.CLASSCD  ";
        $query .= "     AND T1.SCHOOL_KIND = T5.SCHOOL_KIND  ";
        $query .= "     AND T1.CURRICULUM_CD = T5.CURRICULUM_CD  ";
        $query .= "     AND T1.SUBCLASSCD = T5.SUBCLASSCD ";
        $query .= " INNER JOIN CHAIR_ATTENTION_SCORE_MST T6 ";
        $query .= "     ON T1.YEAR = T6.YEAR ";
        $query .= "     AND T1.SEMESTER = T6.SEMESTER ";
        $query .= "     AND T1.CHAIRCD = T6.CHAIRCD ";
        $query .= "     AND T4.TESTKINDCD = T6.TESTKINDCD ";
        $query .= "     AND T4.TESTITEMCD = T6.TESTITEMCD ";
        $query .= "     AND T4.SCORE_DIV = T6.SCORE_DIV ";
        $query .= "     AND T5.CLASSCD = T6.CLASSCD ";
        $query .= "     AND T5.SCHOOL_KIND = T6.SCHOOL_KIND ";
        $query .= "     AND T5.CURRICULUM_CD = T6.CURRICULUM_CD ";
        $query .= "     AND T5.SUBCLASSCD = T6.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["taisyouSemester"]}' ";
        $query .= "     AND T4.TESTKINDCD = '{$model->testkindcd}' ";
        $query .= "     AND T4.TESTITEMCD = '{$model->testitemcd}' ";
        $query .= "     AND T4.SCORE_DIV = '{$model->scoreDiv}' ";
        if ($model->field['taisyouKamoku'] != 'all') {
            $query .= "     AND T5.CLASSCD = '{$model->classcd}' ";
            $query .= "     AND T5.SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= "     AND T5.CURRICULUM_CD = '{$model->curriculumCd}' ";
            $query .= "     AND T5.SUBCLASSCD = '{$model->subclasscd}' ";
        }
        if ($chaircd != '') {
            $query .= "     AND T1.CHAIRCD = '{$chaircd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }
    //データのコピー
    public function getCopyQuery($model)
    {
        $query  = " INSERT INTO CHAIR_ATTENTION_SCORE_MST ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T7.YEAR, ";
        $query .= "         T7.SEMESTER, ";
        $query .= "         T8.TESTKINDCD, ";
        $query .= "         T8.TESTITEMCD, ";
        $query .= "         T8.SCORE_DIV, ";
        $query .= "         T6.CLASSCD, ";
        $query .= "         T6.SCHOOL_KIND, ";
        $query .= "         T6.CURRICULUM_CD, ";
        $query .= "         T6.SUBCLASSCD, ";
        $query .= "         T6.CHAIRCD, ";
        $query .= "         T6.ATTENTION_SCORE, ";
        $query .= "         '" . STAFFCD . "', ";
        $query .= "         SYSDATE() ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T4 ";
        $query .= "         ON T1.YEAR = T4.YEAR ";
        $query .= "         AND T1.SEMESTER = T4.SEMESTER ";
        $query .= "         AND T4.SCORE_DIV = '01' ";
        $query .= "     INNER JOIN SUBCLASS_YDAT T5 ";
        $query .= "         ON T1.YEAR = T5.YEAR ";
        $query .= "         AND T1.CLASSCD = T5.CLASSCD  ";
        $query .= "         AND T1.SCHOOL_KIND = T5.SCHOOL_KIND  ";
        $query .= "         AND T1.CURRICULUM_CD = T5.CURRICULUM_CD  ";
        $query .= "         AND T1.SUBCLASSCD = T5.SUBCLASSCD ";
        $query .= "     INNER JOIN CHAIR_ATTENTION_SCORE_MST T6 ";
        $query .= "         ON T1.YEAR = T6.YEAR ";
        $query .= "         AND T1.SEMESTER = T6.SEMESTER ";
        $query .= "         AND T1.CHAIRCD = T6.CHAIRCD ";
        $query .= "         AND T4.TESTKINDCD = T6.TESTKINDCD ";
        $query .= "         AND T4.TESTITEMCD = T6.TESTITEMCD ";
        $query .= "         AND T4.SCORE_DIV = T6.SCORE_DIV ";
        $query .= "         AND T5.CLASSCD = T6.CLASSCD ";
        $query .= "         AND T5.SCHOOL_KIND = T6.SCHOOL_KIND ";
        $query .= "         AND T5.CURRICULUM_CD = T6.CURRICULUM_CD ";
        $query .= "         AND T5.SUBCLASSCD = T6.SUBCLASSCD ";
        $query .= "     INNER JOIN CHAIR_DAT T7 ";
        $query .= "         ON T1.CHAIRCD = T7.CHAIRCD ";
        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T8 ";
        $query .= "         ON T1.YEAR = T8.YEAR ";
        $query .= "         AND T1.SEMESTER = T8.SEMESTER ";
        $query .= "         AND T8.SCORE_DIV = '01' ";
        $query .= "     INNER JOIN SUBCLASS_YDAT T9 ";
        $query .= "         ON T7.YEAR = T9.YEAR ";
        $query .= "         AND T7.CLASSCD = T9.CLASSCD  ";
        $query .= "         AND T7.SCHOOL_KIND = T9.SCHOOL_KIND  ";
        $query .= "         AND T7.CURRICULUM_CD = T9.CURRICULUM_CD  ";
        $query .= "         AND T7.SUBCLASSCD = T9.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$model->field["sansyouYear"]}' ";
        $query .= "         AND T1.SEMESTER = '{$model->field["sansyouSemester"]}' ";
        $query .= "         AND T4.TESTKINDCD = '{$model->sansyouTestkindcd}' ";
        $query .= "         AND T4.TESTITEMCD = '{$model->sansyouTestitemcd}' ";
        $query .= "         AND T4.SCORE_DIV = '{$model->sansyouScoreDiv}' ";
        if ($model->field['sansyouKamoku'] != 'all') {
            $query .= "             AND T5.CLASSCD = '{$model->sansyouClasscd}' ";
            $query .= "             AND T5.SCHOOL_KIND = '{$model->sansyouSchoolKind}' ";
            $query .= "             AND T5.CURRICULUM_CD = '{$model->sansyouCurriculumCd}' ";
            $query .= "             AND T5.SUBCLASSCD = '{$model->sansyouSubclasscd}' ";
        }
        $query .= "         AND T7.YEAR = '{$model->year}' ";
        $query .= "         AND T7.SEMESTER = '{$model->field["taisyouSemester"]}' ";
        $query .= "         AND T8.TESTKINDCD = '{$model->testkindcd}' ";
        $query .= "         AND T8.TESTITEMCD = '{$model->testitemcd}' ";
        $query .= "         AND T8.SCORE_DIV = '{$model->scoreDiv}' ";
        if ($model->field['taisyouKamoku'] != 'all') {
            $query .= "             AND T9.CLASSCD = '{$model->classcd}' ";
            $query .= "             AND T9.SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= "             AND T9.CURRICULUM_CD = '{$model->curriculumCd}' ";
            $query .= "             AND T9.SUBCLASSCD = '{$model->subclasscd}' ";
        }
        $query .= " ) ";

        $db = Query::dbCheckOut();
        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }
}
