<?php

require_once('for_php7.php');

class knjz214Query extends Query {

    //リスト
    function getList($model, $yearDiv = "NOW")
    {
        $query  = " SELECT ";
        $query .= "     T1.GROUP_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     G1.GRADE_NAME1, ";
        $query .= "     T1.GROUP_NAME, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     L1.COURSENAME || L1.MAJORNAME || L2.COURSECODENAME AS COURSE_NAME ";
        $query .= " FROM ";
        $query .= "     REC_SUBCLASS_GROUP_MST T1 ";
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
            $query .= "          AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        if ($yearDiv == "NOW") {
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        } else {
            $query .= "     T1.YEAR = '".(CTRL_YEAR - 1)."' ";
        }
        if ($yearDiv == "NOW" && $model->leftGroupDiv) {
            $query .= "     AND T1.GROUP_DIV = '".$model->leftGroupDiv."' ";
        }
        if ($yearDiv == "NOW" && $model->leftGrade) {
            $query .= "     AND T1.GRADE = '".$model->leftGrade."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.GROUP_DIV ";

        return $query;
    }

    //１レコード取得
    function getSelectData($field)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     REC_SUBCLASS_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GROUP_DIV = '".$field["GROUP_DIV"]."' ";
        $query .= "     AND GRADE = '".$field["GRADE"]."' ";
        $query .= "     AND COURSECD = '".$field["COURSECD"]."' ";
        $query .= "     AND MAJORCD = '".$field["MAJORCD"]."' ";
        $query .= "     AND COURSECODE = '".$field["COURSECODE"]."' ";

        return $query;
    }

    //科目数
    function getGroupDiv()
    {
        $query  = " SELECT ";
        $query .= "     GROUP_DIV AS VALUE, ";
        $query .= "     GROUP_DIV AS LABEL ";
        $query .= " FROM ";
        $query .= "     REC_SUBCLASS_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     GROUP_DIV ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程
    function getCourse($model, $grade, $key)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || ':' || T1.COURSECD || ':' || T1.MAJORCD || ':' || T1.COURSECODE AS VALUE, ";
        $query .= "     G1.GRADE_NAME1 || ' ' || L1.COURSENAME || ' ' || L1.MAJORNAME || ' ' || L2.COURSECODENAME AS LABEL ";
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
            $query .= "          AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
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

        $result = $db->query(knjz214Query::getList($model, "LAST"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();

            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["GROUP_DIV"][TEXT]    = $row["GROUP_DIV"];
            $data["GRADE"][TEXT]        = $row["GRADE"];
            $data["COURSECD"][TEXT]     = $row["COURSECD"];
            $data["MAJORCD"][TEXT]      = $row["MAJORCD"];
            $data["COURSECODE"][TEXT]   = $row["COURSECODE"];
            $data["GROUP_NAME"][TEXT]   = $row["GROUP_NAME"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $query = Query::insertSQL($data, "REC_SUBCLASS_GROUP_MST");
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

        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["GROUP_DIV"][TEXT]    = $fields["GROUP_DIV"];
        $data["GRADE"][TEXT]        = $fields["GRADE"];
        $data["COURSECD"][TEXT]     = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]      = $fields["MAJORCD"];
        $data["COURSECODE"][TEXT]   = $fields["COURSECODE"];
        $data["GROUP_NAME"][TEXT]   = $fields["GROUP_NAME"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "REC_SUBCLASS_GROUP_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields, $del_div = "")
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     REC_SUBCLASS_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GROUP_DIV = '".$fields["GROUP_DIV"]."' ";
        $query .= "     AND GRADE = '".$fields["GRADE"]."' ";
        $query .= "     AND COURSECD = '".$fields["COURSECD"]."' ";
        $query .= "     AND MAJORCD = '".$fields["MAJORCD"]."' ";
        $query .= "     AND COURSECODE = '".$fields["COURSECODE"]."' ";

        $db->query($query);

        if ($del_div == "ALL") {
            $query  = " DELETE FROM ";
            $query .= "     REC_SUBCLASS_GROUP_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND GROUP_DIV = '".$fields["GROUP_DIV"]."' ";
            $query .= "     AND GRADE = '".$fields["GRADE"]."' ";
            $query .= "     AND COURSECD = '".$fields["COURSECD"]."' ";
            $query .= "     AND MAJORCD = '".$fields["MAJORCD"]."' ";
            $query .= "     AND COURSECODE = '".$fields["COURSECODE"]."' ";

            $db->query($query);
        }

        Query::dbCheckIn($db);

        return $result;
    }
}
?>
