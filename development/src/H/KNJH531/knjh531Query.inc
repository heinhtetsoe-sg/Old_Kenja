<?php
class knjh531Query extends Query {

    //年度取得
    function getYear()
    {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //学期取得
    function getSemester($model)
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER AS VALUE, ";
        $query .= "    SEMESTERNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "    SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //学年コンボ
    function getGrade($model) {

        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ区分コンボ
    function getProficiencyDiv($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND NAMECD1 = 'H508' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧取得
    function getRightList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     T1.PROFICIENCYCD || ':' || L1.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST T1 ";
        $query .= "     LEFT JOIN PROFICIENCY_MST L1 ON T1.PROFICIENCYDIV = L1.PROFICIENCYDIV ";
        $query .= "          AND T1.PROFICIENCYCD = L1.PROFICIENCYCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND VALUE(T1.UPDATE_FLG, '0') = '0' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //登録一覧取得
    function getLeftList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     T1.PROFICIENCYCD || ':' || L1.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST T1 ";
        $query .= "     LEFT JOIN PROFICIENCY_MST L1 ON T1.PROFICIENCYDIV = L1.PROFICIENCYDIV ";
        $query .= "          AND T1.PROFICIENCYCD = L1.PROFICIENCYCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.UPDATE_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ削除SQL作成
    function getUpdateFlgClearQuery($model)
    {
        $query  = "UPDATE PROFICIENCY_YMST ";
        $query .= "    SET(UPDATE_FLG) = CAST(NULL AS VARCHAR(1)) ";
        $query .= "WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";

        return $query;
    }

    //実力グループ新規追加
    function &getProficiencyInsQuery(&$db, $select, $model)
    {
        $data = array();
        $selectdata = explode(",", $select);
        for ($i = 0; $i < count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }
            $db->query(knjh531Query::makeInsProficiencyQuery($model, $selectdata[$i]));
        }
        return;
    }

    //データ新規SQL作成
    function makeInsProficiencyQuery($model, $selectdata)
    {
        $data["UPDATE_FLG"][TEXT] = "1";
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "SYSDATE()";

        $where .= " WHERE ";
        $where .= "     YEAR = '".$model->field["YEAR"]."' ";
        $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $where .= "     AND PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $where .= "     AND PROFICIENCYCD = '".$selectdata."' ";
        $where .= "     AND GRADE = '".$model->field["GRADE"]."' ";

        $query = Query::updateSQL($data, "PROFICIENCY_YMST", $where);

        return $query;
    }
}
?>
