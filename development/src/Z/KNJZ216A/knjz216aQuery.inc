<?php

require_once('for_php7.php');

class knjz216aQuery extends Query {

    function getSemester() {
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト
    function getList($model, $yearDiv = "NOW")
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.GROUP_DIV, ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     T1.PROFICIENCYCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     G1.GRADE_NAME1, ";
        $query .= "     T1.GROUP_NAME, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE || '：' || L1.MAJORNAME || '　' || L2.COURSECODENAME AS COURSE_NAME ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_GROUP_MST T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.COURSECD || T1.MAJORCD = L1.COURSECD || L1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.COURSECODE = L2.COURSECODE ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ON T1.YEAR = G1.YEAR ";
        $query .= "          AND T1.GRADE = G1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "          AND G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        if ($yearDiv == "NOW") {
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        } else {
            $query .= "     T1.YEAR = '".(CTRL_YEAR - 1)."' ";
            $query .= "     AND EXISTS( ";
            $query .= "                SELECT ";
            $query .= "                    'X' ";
            $query .= "                FROM ";
            $query .= "                    V_COURSECODE_MST L3 ";
            $query .= "                LEFT JOIN V_COURSE_MAJOR_MST L4 ON L4.YEAR = L3.YEAR ";
            $query .= "                WHERE     L3.YEAR       = '".CTRL_YEAR."' ";
            $query .= "                      AND L3.YEAR       = L4.YEAR ";
            $query .= "                      AND L3.COURSECODE = T1.COURSECODE ";
            $query .= "                      AND L4.COURSECD || L4.MAJORCD = T1.COURSECD || T1.MAJORCD  ";
            $query .= "                ) ";
        }
        if ($yearDiv == "NOW" && $model->leftSemester) {
            $query .= "     AND T1.SEMESTER = '".$model->leftSemester."' ";
        }
        if ($yearDiv == "NOW" && $model->leftProficiencyDiv) {
            $query .= "     AND T1.PROFICIENCYDIV = '".$model->leftProficiencyDiv."' ";
        }
        if ($yearDiv == "NOW" && $model->leftProficiencyCd) {
            $query .= "     AND T1.PROFICIENCYCD = '".$model->leftProficiencyCd."' ";
        }
        if ($yearDiv == "NOW" && $model->leftGroupDiv) {
            $query .= "     AND T1.GROUP_DIV = '".$model->leftGroupDiv."' ";
        }
        if ($yearDiv == "NOW" && $model->leftGrade) {
            $query .= "     AND T1.GRADE = '".$model->leftGrade."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     T1.PROFICIENCYCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.GROUP_DIV, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";

        return $query;
    }

    //１レコード取得
    function getSelectData($field)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER        = '".$field["SEMESTER"]."' ";
        $query .= "     AND PROFICIENCYDIV  = '".$field["PROFICIENCYDIV"]."' ";
        $query .= "     AND PROFICIENCYCD   = '".$field["PROFICIENCYCD"]."' ";
        $query .= "     AND GROUP_DIV       = '".$field["GROUP_DIV"]."' ";
        $query .= "     AND GRADE           = '".$field["GRADE"]."' ";
        $query .= "     AND COURSECD        = '".$field["COURSECD"]."' ";
        $query .= "     AND MAJORCD         = '".$field["MAJORCD"]."' ";
        $query .= "     AND COURSECODE      = '".$field["COURSECODE"]."' ";

        return $query;
    }

    //模試(右フレーム)
    function getProficiencyName1($div)
    {
        $query  = " SELECT ";
        $query .= "     PROFICIENCYCD AS VALUE, ";
        $query .= "     PROFICIENCYCD || ':' || PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST ";
        $query .= " WHERE ";
        $query .= "     PROFICIENCYDIV = '".$div."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getProficiencyDiv() {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H508' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //模試(左フレーム)
    function getProficiencyCd($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     T1.PROFICIENCYCD || ':' || T2.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_GROUP_MST T1 ";
        $query .= "     LEFT JOIN PROFICIENCY_MST T2 ON T1.PROFICIENCYDIV = T2.PROFICIENCYDIV ";
        $query .= "          AND T1.PROFICIENCYCD = T2.PROFICIENCYCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->leftSemester."' ";
        $query .= "     AND T1.PROFICIENCYDIV = '".$model->leftProficiencyDiv."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.PROFICIENCYCD, T2.PROFICIENCYNAME1 ";
        $query .= " ORDER BY ";
        $query .= "     T1.PROFICIENCYCD ";

        return $query;
    }

    //学年
    function getGrade($model, $table)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ".$table." T1";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T2.GRADE_NAME1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目数
    function getGroupDiv()
    {
        $query  = " SELECT ";
        $query .= "     GROUP_DIV AS VALUE, ";
        $query .= "     GROUP_DIV AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     GROUP_DIV ";
        $query .= " ORDER BY ";
        $query .= "     GROUP_DIV ";

        return $query;
    }

    //課程
    function getCourse($model, $grade, $key)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || ':' || T1.COURSECD || ':' || T1.MAJORCD || ':' || T1.COURSECODE AS VALUE, ";
        $query .= "     G1.GRADE_NAME1 || ' ' || L1.MAJORNAME || ' ' || L2.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.COURSECD = L1.COURSECD ";
        $query .= "          AND T1.MAJORCD = L1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.COURSECODE = L2.COURSECODE ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ON T1.YEAR = G1.YEAR ";
        $query .= "          AND T1.GRADE = G1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "          AND G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($key) {
            $query .= "     AND T1.GRADE || ':' || T1.COURSECD || ':' || T1.MAJORCD || ':' || T1.COURSECODE = '".$key."' ";
        } else if ($grade) {
            $query .= "     AND T1.GRADE = '".$grade."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //--- COPY
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();

        $result = $db->query(knjz216aQuery::getList($model, "LAST"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $row["SEMESTER"];
            $data["PROFICIENCYDIV"][TEXT]   = $row["PROFICIENCYDIV"];
            $data["PROFICIENCYCD"][TEXT]    = $row["PROFICIENCYCD"];
            $data["GROUP_DIV"][TEXT]        = $row["GROUP_DIV"];
            $data["GRADE"][TEXT]            = $row["GRADE"];
            $data["COURSECD"][TEXT]         = $row["COURSECD"];
            $data["MAJORCD"][TEXT]          = $row["MAJORCD"];
            $data["COURSECODE"][TEXT]       = $row["COURSECODE"];
            $data["GROUP_NAME"][TEXT]       = $row["GROUP_NAME"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "PROFICIENCY_SUBCLASS_GROUP_MST");
            $db->query($query);
        }
        $result->free();

        Query::dbCheckIn($db);
        return;
    }

    //--- INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $fields["SEMESTER"];
        $data["PROFICIENCYDIV"][TEXT]   = $fields["PROFICIENCYDIV"];
        $data["PROFICIENCYCD"][TEXT]    = $fields["PROFICIENCYCD"];
        $data["GROUP_DIV"][TEXT]        = $fields["GROUP_DIV"];
        $data["GRADE"][TEXT]            = $fields["GRADE"];
        $data["COURSECD"][TEXT]         = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]          = $fields["MAJORCD"];
        $data["COURSECODE"][TEXT]       = $fields["COURSECODE"];
        $data["GROUP_NAME"][TEXT]       = $fields["GROUP_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "PROFICIENCY_SUBCLASS_GROUP_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields, $del_div = "")
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER        = '".$fields["SEMESTER"]."' ";
        $query .= "     AND PROFICIENCYDIV  = '".$fields["PROFICIENCYDIV"]."' ";
        $query .= "     AND PROFICIENCYCD   = '".$fields["PROFICIENCYCD"]."' ";
        $query .= "     AND GROUP_DIV       = '".$fields["GROUP_DIV"]."' ";
        $query .= "     AND GRADE           = '".$fields["GRADE"]."' ";
        $query .= "     AND COURSECD        = '".$fields["COURSECD"]."' ";
        $query .= "     AND MAJORCD         = '".$fields["MAJORCD"]."' ";
        $query .= "     AND COURSECODE      = '".$fields["COURSECODE"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //削除の際 PROFICIENCY_SUBCLASS_GROUP_DAT にデータがないことを確認する
    function checkDat($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER        = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     PROFICIENCYDIV  = '{$model->field["PROFICIENCYDIV"]}' AND ";
        $query .= "     PROFICIENCYCD   = '{$model->field["PROFICIENCYCD"]}' AND ";
        $query .= "     GROUP_DIV       = '{$model->field["GROUP_DIV"]}' AND ";
        $query .= "     GRADE           = '{$model->field["GRADE"]}' AND ";
        $query .= "     COURSECD        = '{$model->field["COURSECD"]}' AND ";
        $query .= "     MAJORCD         = '{$model->field["MAJORCD"]}' AND ";
        $query .= "     COURSECODE      = '{$model->field["COURSECODE"]}' ";

        return $query;
    }
}
?>
