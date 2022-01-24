<?php

require_once('for_php7.php');
class knjh441bQuery extends Query {

    //校種取得
    function getSchoolkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE ";
        $query .= "   , ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND NAME1 IN ('".SCHOOLKIND."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     ABBV3, NAME1 ";

        return $query;
    }

    // 学年取得
    function getGrade($model) {
        $query  = "    SELECT DISTINCT ";
        $query .= "        GRADE_NAME1 AS LABEL ";
        $query .= "      , GRADE AS VALUE";
        $query .= "    FROM ";
        $query .= "      SCHREG_REGD_GDAT ";
        $query .= "    WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";
        //校種対応
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "    AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND IN ('".SCHOOLKIND."') ";
        }
        if ($model->field["SCHOOL_KIND"]) {
            $query .= "     AND SCHOOL_KIND IN ('".$model->field["SCHOOL_KIND"]."') ";
        }

        return $query;
    }

    //模擬試験一覧取得
    function getTestInfo($model) {

        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.MOCKCD || ':' || T1.MOCKNAME1 AS LABEL ";
        $query .= "   , T1.MOCKCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     MOCK_MST T1 ";
        $query .= "     INNER JOIN MOCK_RANK_RANGE_DAT T2 ";
        $query .= "       ON T1.MOCKCD = T2.MOCKCD ";
        $query .= " WHERE ";
        $query .= "     SUBSTR(T1.MOCKCD, 7, 1) = '".substr($model->field["GRADE"], -1)."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.MOCKCD ";

        return $query;
    }

    //科目一覧取得
    function getSubclass($model) {

        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SUBCLASS_NAME AS LABEL ";
        $query .= "   , T1.MOCK_SUBCLASS_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_MST T1 ";
        $query .= "     INNER JOIN MOCK_RANK_RANGE_DAT T2 ";
        $query .= "       ON T1.MOCK_SUBCLASS_CD = T2.MOCK_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "     SUBSTR(T2.MOCKCD, 7, 1) = '".substr($model->field["GRADE"], -1)."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.MOCK_SUBCLASS_CD ";

        return $query;
    }


    // CSV出力用データ取得
    function selectCsvQuery($model) {

        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , T1.GRADE ";
        $query .= "   , T2.SCHOOL_KIND ";
        $query .= "   , T1.HR_CLASS ";
        $query .= "   , T3.HR_NAME ";
        $query .= "   , T1.ATTENDNO ";
        $query .= "   , T4.NAME ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.GRADE = T2.GRADE ";
        $query .= "   INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "     ON T1.YEAR = T3.YEAR ";
        $query .= "     AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "     AND T1.GRADE = T3.GRADE ";
        $query .= "     AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= "   INNER JOIN SCHREG_BASE_MST T4 ";
        $query .= "     ON T1.SCHREGNO = T4.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "   AND T1.GRADE = '{$model->field["GRADE"]}' ";
        if ($model->field["SCHOOL_KIND"]) {
            $query .= "   AND T2.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE ";
        $query .= "   , T2.SCHOOL_KIND ";
        $query .= "   , T1.HR_CLASS ";
        $query .= "   , T1.ATTENDNO ";
        $query .= "   , T1.SCHREGNO ";

        return $query;
    }

    // 対象学籍番号取得
    function getSchregNo($model) {

        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.GRADE = T2.GRADE ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "   AND T1.GRADE = '{$model->field["GRADE"]}' ";
        if ($model->field["SCHOOL_KIND"]) {
            $query .= "   AND T2.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        }

        return $query;
    }

    // 模試＆科目取得
    function getMockSubclass($model) {

        $query  = "";
        $query .= " WITH REGD AS ( ";
        $query .= knjh441bQuery::getSchregNo($model);
        $query .= " ) ";
        $query .= " , TARGETYEAR AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR ";
        $query .= "     FROM SCHREG_REGD_DAT T1 ";
        $query .= "     WHERE T1.SCHREGNO IN ( SELECT SCHREGNO FROM REGD ) ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.MOCKCD ";
        $query .= "   , T2.MOCKNAME1 MOCKNAME ";
        $query .= "   , T1.MOCK_SUBCLASS_CD ";
        $query .= "   , T3.SUBCLASS_NAME ";

        $query .= " FROM MOCK_RANK_RANGE_DAT T1 ";
        $query .= " INNER JOIN MOCK_MST T2 ";
        $query .= "     ON T1.MOCKCD = T2.MOCKCD ";
        $query .= " INNER JOIN MOCK_SUBCLASS_MST T3 ";
        $query .= "     ON T1.MOCK_SUBCLASS_CD = T3.MOCK_SUBCLASS_CD ";

        //対象年度
        $query .= " WHERE T1.YEAR IN ( SELECT YEAR FROM TARGETYEAR ) ";
        //対象学生
        $query .= "   AND T1.SCHREGNO IN ( SELECT SCHREGNO FROM REGD ) ";
        //模試
        $query .= "   AND T1.MOCKCD IN ( '". implode("','", $model->selectTestInfo) ."' ) ";
        //科目
        $query .= "   AND T1.MOCK_SUBCLASS_CD IN ( '". implode("','", $model->selectSubclass) ."' ) ";

        return $query;
    }

    // 学生の模試情報
    function getSchregMock($model, $schregNo) {

        $query  = "";
        $query .= " WITH REGD AS ( ";
        $query .= knjh441bQuery::getSchregNo($model);
        $query .= " ) ";
        $query .= " , TARGETYEAR AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR ";
        $query .= "     FROM SCHREG_REGD_DAT T1 ";
        $query .= "     WHERE T1.SCHREGNO IN ( SELECT SCHREGNO FROM REGD ) ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "   , T1.MOCKCD ";
        $query .= "   , T1.MOCK_SUBCLASS_CD ";
        $query .= "   , T1.SCORE ";
        $query .= "   , T1.RANK ";
        $query .= "   , T1.LEVEL ";

        $query .= " FROM MOCK_RANK_RANGE_DAT T1 ";

        //対象年度
        $query .= " WHERE T1.YEAR IN ( SELECT YEAR FROM TARGETYEAR ) ";
        //模試
        $query .= "   AND T1.MOCKCD IN ( '". implode("','", $model->selectTestInfo) ."' ) ";
        //科目
        $query .= "   AND T1.MOCK_SUBCLASS_CD IN ( '". implode("','", $model->selectSubclass) ."' ) ";
        //対象学生
        $query .= "   AND T1.SCHREGNO = '{$schregNo}' ";

        $query .= "   AND T1.RANK_RANGE = '1' ";
        $query .= "   AND T1.RANK_DIV = '02' ";
        $query .= "   AND T1.MOCKDIV = '1' ";

        return $query;
    }

}
?>
