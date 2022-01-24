<?php

require_once('for_php7.php');

class knje064mquery extends Query {

    //学年取得
    function getGrade()
    {
        $query .= " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = 'J' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getStudent($model) {
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE = '".$model->grade."' ";

        return $query;
    }

    //得点取得
    function getScore($schregNo, $subclassCd, $model)
    {
        $query .= " SELECT ";
        $query .= "     SCORE ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '2' ";
        $query .= "     AND TESTKINDCD = '99' ";
        $query .= "     AND TESTITEMCD = '00' ";
        $query .= "     AND SCORE_DIV = '00' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '".substr($subclassCd, 0, 2)."' ";
            $query .= "     AND SCHOOL_KIND     = 'J' ";
            $query .= "     AND CURRICULUM_CD   = '2' ";
        }
        $query .= "     AND SUBCLASSCD = '".$subclassCd."' ";
        $query .= "     AND SCHREGNO = '".$schregNo."' ";

        return $query;
    }

    //平均得点取得
    function getScoreAvg($schregNo, $subclassInState, $model)
    {
        $query .= " SELECT ";
        $query .= "     SUM(SCORE) AS SCORE, ";
        $query .= "     SUM(CASE WHEN SCORE IS NULL THEN 0 ELSE 1 END) AS CNT ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '2' ";
        $query .= "     AND TESTKINDCD = '99' ";
        $query .= "     AND TESTITEMCD = '00' ";
        $query .= "     AND SCORE_DIV = '00' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '".substr($subclassInState, 2, 2)."' ";
            $query .= "     AND SCHOOL_KIND     = 'J' ";
            $query .= "     AND CURRICULUM_CD   = '2' ";
        }
        $query .= "     AND SUBCLASSCD IN ".$subclassInState." ";
        $query .= "     AND SCHREGNO = '".$schregNo."' ";

        return $query;
    }

    //最高得点取得
    function getScoreMax($schregNo, $subclassInState, $model)
    {
        $query .= " SELECT ";
        $query .= "     VALUE(MAX(SCORE), 0) AS SCORE ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '2' ";
        $query .= "     AND TESTKINDCD = '99' ";
        $query .= "     AND TESTITEMCD = '00' ";
        $query .= "     AND SCORE_DIV = '00' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '".substr($subclassInState, 2, 2)."' ";
            $query .= "     AND SCHOOL_KIND     = 'J' ";
            $query .= "     AND CURRICULUM_CD   = '2' ";
        }
        $query .= "     AND SUBCLASSCD IN ".$subclassInState." ";
        $query .= "     AND SCHREGNO = '".$schregNo."' ";

        return $query;
    }

    //更新
    function delInsData($db, $schregNo, $classCd, $kantenArray, $hyoutei)
    {
        knje064mQuery::deleteData($db, $schregNo, $classCd);

        for ($i = 0; $i < get_count($kantenArray); $i++) {
            knje064mQuery::insData($db, $schregNo, $classCd."0".($i + 1), $kantenArray[$i]);
        }

        knje064mQuery::insData($db, $schregNo, $classCd."99", $hyoutei);
    }

    //更新2
    function delInsData2($db, $schregNo, $classCd, $startView, $kantenArray)
    {

        for ($i = 0; $i < get_count($kantenArray); $i++) {
            knje064mQuery::insData($db, $schregNo, $classCd."0".$startView, $kantenArray[$i]);
            $startView++;
        }
    }

    //新規
    function insData($db, $schregNo, $viewCd, $value)
    {

        $data = array();
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SEMESTER"][TEXT]             = "9";
        $data["SCHREGNO"][TEXT]             = $schregNo;
        $data["VIEWCD"][TEXT]               = $viewCd;
        $data["STATUS"][TEXT]               = $value;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, "JVIEWSTAT_DAT");
        $db->query($query);

        $db->commit();
    }

    //削除
    function deleteData($db, $schregNo, $classCd)
    {
        $query  = " DELETE FROM JVIEWSTAT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '".$schregNo."'";
        $query .= "     AND substr(VIEWCD, 1, 2) = '".$classCd."'";

        $db->query($query);
    }
}
?>
