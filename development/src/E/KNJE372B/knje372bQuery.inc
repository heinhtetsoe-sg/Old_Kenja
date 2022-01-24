<?php

require_once('for_php7.php');

class knje372bQuery extends Query {

    //年度
    function getYear($year)
    {
        if($year == "OYEAR"){
            $query .= "WITH YEAR_T(VALUE, LABEL) AS ( ";
            $query .= "     VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
            $query .= ") ";
            $query .= "SELECT ";
            $query .= "    * ";
            $query .= "FROM ";
            $query .= "    YEAR_T ";
            $query .= "UNION ";
        }
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     AFT_RECOMMENDATION_LIMIT_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学部名
    function getFacultyName($model) {
        $query  = "    SELECT ";
        $query .= "      FACULTYNAME ";
        $query .= "    FROM ";
        $query .= "      COLLEGE_FACULTY_MST ";
        $query .= "    WHERE ";
        $query .= "      SCHOOL_CD = '".$model->field["SCHOOL_CD"]."' ";
        $query .= "      AND FACULTYCD = '".$model->field["FACULTYCD"]."' ";

        return $query;
    }
    //学科名
    function getDepartmentName($model) {
        $query  = "    SELECT ";
        $query .= "      DEPARTMENTNAME ";
        $query .= "    FROM ";
        $query .= "      COLLEGE_DEPARTMENT_MST ";
        $query .= "    WHERE ";
        $query .= "      SCHOOL_CD = '".$model->field["SCHOOL_CD"]."' ";
        $query .= "      AND FACULTYCD = '".$model->field["FACULTYCD"]."' ";
        $query .= "      AND DEPARTMENTCD = '".$model->field["DEPARTMENTCD"]."' ";

        return $query;
    }

    //リスト
    function getList($model, $select="")
    {
        $query  = " SELECT ";
        if ($select == "CNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     AFT_RECOMMENDATION_LIMIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->oyear."' ";
        if ($select == "ONE") {
            $query .= " AND RECOMMENDATION_CD = '".$model->recommendation_cd."' ";
        } else if ($select == "CHECK"){
            $query .= " AND RECOMMENDATION_CD = '".$model->field["RECOMMENDATION_CD"]."' ";
        }
        if ($select != "CNT") {
            $query .= " ORDER BY ";
            $query .= "     RECOMMENDATION_CD ";
        }

        return $query;
    }

    //存在 学科番号
    function getExistsDepCd($model, $targetDepField) {
        $query  = "     SELECT ";
        $query .= "       COUNT(*) ";
        $query .= "     FROM ";
        $query .= "       AFT_RECOMMENDATION_LIMIT_MST ";
        $query .= "     WHERE ";
        $query .= "       YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND {$targetDepField} = '{$model->field[$targetDepField]}' ";

        return $query;
    }

    //存在 表示順・出力順
    function getExistsOrder($model, $targetOrderField) {
        $query  = "     SELECT ";
        $query .= "       COUNT(*) ";
        $query .= "     FROM ";
        $query .= "       AFT_RECOMMENDATION_LIMIT_MST ";
        $query .= "     WHERE ";
        $query .= "       YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND {$targetOrderField} = '{$model->field[$targetOrderField]}' ";

        return $query;
    }

    //コピー処理
    function getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "INSERT INTO AFT_RECOMMENDATION_LIMIT_MST ";
        $query .= "( ";
        $query .= "SELECT ";
        $query .= "    '".$model->oyear."', ";
        $query .= "    RECOMMENDATION_CD, ";
        $query .= "    DEPARTMENT_S, ";
        $query .= "    DEPARTMENT_H, ";
        $query .= "    DISP_ORDER, ";
        $query .= "    FACULTY_NAME, ";
        $query .= "    FACULTY_ABBV, ";
        $query .= "    DEPARTMENT_NAME, ";
        $query .= "    DEPARTMENT_ABBV, ";
        $query .= "    DEPARTMENT_ABBV2, ";
        $query .= "    LIMIT_COUNT_S, ";
        $query .= "    LIMIT_COUNT_H, ";
        $query .= "    WITHOUT_H_FLG, ";
        $query .= "    DEPARTMENT_LIST_CD, ";
        $query .= "    DEPARTMENT_LIST_ORDER, ";
        $query .= "    FACULTY_LIST_NAME, ";
        $query .= "    DEPARTMENT_LIST_NAME, ";
        $query .= "    SCHOOL_CD, ";
        $query .= "    FACULTYCD, ";
        $query .= "    DEPARTMENTCD, ";
        $query .= "    '".STAFFCD."', ";
        $query .= "    SYSDATE() ";
        $query .= "FROM ";
        $query .= "    AFT_RECOMMENDATION_LIMIT_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->ryear."' ";
        $query .= ") ";

        $db->query($query);
        $db->commit();

        Query::dbCheckIn($db);

        return;
    }

    //追加・更新処理
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if($model->cmd == "update"){
            //削除
            $query  = " DELETE FROM ";
            $query .= "     AFT_RECOMMENDATION_LIMIT_MST ";
            $query .= " WHERE ";
            $query .= "     YEAR  = '".$model->oyear."' ";
            $query .= "     AND RECOMMENDATION_CD  = '".$model->field["RECOMMENDATION_CD"]."' ";
            $db->query($query);
        }

        //追加
        $data = array();
        $data["YEAR"][TEXT]                  = $model->oyear;
        $data["RECOMMENDATION_CD"][TEXT]     = $model->field["RECOMMENDATION_CD"];
        $data["DEPARTMENT_S"][TEXT]          = $model->field["DEPARTMENT_S"];
        $data["DEPARTMENT_H"][TEXT]          = $model->field["DEPARTMENT_H"];
        $data["DISP_ORDER"][TEXT]            = $model->field["DISP_ORDER"];
        $data["FACULTY_NAME"][TEXT]          = $model->field["FACULTY_NAME"];
        $data["FACULTY_ABBV"][TEXT]          = $model->field["FACULTY_ABBV"];
        $data["DEPARTMENT_NAME"][TEXT]       = $model->field["DEPARTMENT_NAME"];
        $data["DEPARTMENT_ABBV"][TEXT]       = $model->field["DEPARTMENT_ABBV"];
        $data["DEPARTMENT_ABBV2"][TEXT]      = $model->field["DEPARTMENT_ABBV2"];
        $data["LIMIT_COUNT_S"][NUMBER]       = $model->field["LIMIT_COUNT_S"];
        $data["LIMIT_COUNT_H"][NUMBER]       = $model->field["LIMIT_COUNT_H"];
        $data["WITHOUT_H_FLG"][TEXT]         = $model->field["WITHOUT_H_FLG"];
        $data["DEPARTMENT_LIST_CD"][TEXT]    = $model->field["DEPARTMENT_LIST_CD"];
        $data["DEPARTMENT_LIST_ORDER"][TEXT] = $model->field["DEPARTMENT_LIST_ORDER"];
        $data["FACULTY_LIST_NAME"][TEXT]     = $model->field["FACULTY_LIST_NAME"];
        $data["DEPARTMENT_LIST_NAME"][TEXT]  = $model->field["DEPARTMENT_LIST_NAME"];
        $data["SCHOOL_CD"][TEXT]             = $model->field["SCHOOL_CD"];
        $data["FACULTYCD"][TEXT]             = $model->field["FACULTYCD"];
        $data["DEPARTMENTCD"][TEXT]          = $model->field["DEPARTMENTCD"];
        $data["REGISTERCD"][TEXT]            = STAFFCD;
        $data["UPDATED"][NUMBER]             = "sysdate()";

        $query = Query::insertSQL($data, "AFT_RECOMMENDATION_LIMIT_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;

    }

    //削除処理
    function &getDeleteQuery($db, $model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     AFT_RECOMMENDATION_LIMIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".$model->oyear."' ";
        $query .= "     AND RECOMMENDATION_CD  = '".$model->field["RECOMMENDATION_CD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }
}
?>