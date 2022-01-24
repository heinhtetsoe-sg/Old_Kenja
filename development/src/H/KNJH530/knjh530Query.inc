<?php

require_once('for_php7.php');

class knjh530Query extends Query {

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
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
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
        $query .= "     T1.PROFICIENCYCD || ':' || T1.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' AND ";
        $query .= "     T1.PROFICIENCYCD NOT IN (SELECT ";
        $query .= "                                 PROFICIENCYCD ";
        $query .= "                              FROM ";
        $query .= "                                 PROFICIENCY_YMST ";
        $query .= "                              WHERE ";
        $query .= "                                 YEAR            = '".$model->field["YEAR"]."' AND ";
        $query .= "                                 SEMESTER        = '".$model->field["SEMESTER"]."' AND ";
        $query .= "                                 PROFICIENCYDIV  = '".$model->field["PROFICIENCYDIV"]."' AND ";
        $query .= "                                 GRADE           = '".$model->field["GRADE"]."') ";
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
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //実力グループ新規追加
    function &getProficiencyInsQuery(&$db, $select, $model)
    {
        $data = array();
        $selectdata = explode(",", $select);
        for ($i = 0; $i < get_count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }
            $db->query(knjh530Query::makeInsProficiencyQuery($model, $selectdata[$i]));
        }
        return;
    }

    //データ削除SQL作成
    function makeDelProficiencyQuery($model)
    {
        $query  = "DELETE FROM ";
        $query .= "    PROFICIENCY_YMST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->field["YEAR"]."' ";
        $query .= "    AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "    AND PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "    AND GRADE = '".$model->field["GRADE"]."' ";

        return $query;
    }

    //データ新規SQL作成
    function makeInsProficiencyQuery($model, $selectdata)
    {
        $data["YEAR"][TEXT]             = $model->field["YEAR"];
        $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
        $data["PROFICIENCYDIV"][TEXT]   = $model->field["PROFICIENCYDIV"];
        $data["PROFICIENCYCD"][TEXT]    = $selectdata;
        $data["GRADE"][TEXT]            = $model->field["GRADE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "PROFICIENCY_YMST");

        return $query;
    }

    //データ削除SQL作成
    function getProficiencyCopyDelQuery($model)
    {
        $query  = "DELETE FROM ";
        $query .= "    PROFICIENCY_YMST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->field["COPY_YEAR"]."' ";

        return $query;
    }

    //実力グループ新規追加
    function getProficiencyCopyQuery($model)
    {
        $query  = " INSERT INTO PROFICIENCY_YMST ";
        $query .= " SELECT ";
        $query .= "     '".$model->field["COPY_YEAR"]."' AS YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     PROFICIENCYDIV, ";
        $query .= "     PROFICIENCYCD, ";
        $query .= "     GRADE, ";
        $query .= "     UPDATE_FLG, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR='".$model->field["YEAR"]."' ";

        return $query;
    }
}
?>
