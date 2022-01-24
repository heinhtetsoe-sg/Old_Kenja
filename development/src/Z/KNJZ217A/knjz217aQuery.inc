<?php

require_once('for_php7.php');

class knjz217aQuery extends Query {

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

    //リスト
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     T1.PROFICIENCYCD, ";
        $query .= "     T1.GROUP_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     G1.GRADE_NAME1, ";
        $query .= "     L3.GROUP_NAME, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE || '：' || L1.MAJORNAME || '　' || L2.COURSECODENAME AS COURSE_NAME, ";
        $query .= "     T1.PROFICIENCYDIV || T1.PROFICIENCYCD || T1.GROUP_DIV || T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS KEY, ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD, ";
        $query .= "     L4.SUBCLASS_NAME ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_GROUP_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.COURSECD || T1.MAJORCD = L1.COURSECD || L1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.COURSECODE = L2.COURSECODE ";
        $query .= "     LEFT JOIN PROFICIENCY_SUBCLASS_GROUP_MST L3 ON T1.YEAR = L3.YEAR ";
        $query .= "          AND T1.SEMESTER = L3.SEMESTER ";
        $query .= "          AND T1.PROFICIENCYDIV = L3.PROFICIENCYDIV ";
        $query .= "          AND T1.PROFICIENCYCD = L3.PROFICIENCYCD ";
        $query .= "          AND T1.GROUP_DIV = L3.GROUP_DIV ";
        $query .= "          AND T1.GRADE = L3.GRADE ";
        $query .= "          AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = L3.COURSECD || L3.MAJORCD || L3.COURSECODE ";
        $query .= "     LEFT JOIN PROFICIENCY_SUBCLASS_MST L4 ON T1.PROFICIENCY_SUBCLASS_CD = L4.PROFICIENCY_SUBCLASS_CD ";
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
        if ($model->leftSemester) {
            $query .= "     AND T1.SEMESTER = '".$model->leftSemester."' ";
        }
        if ($model->leftProficiencyDiv) {
            $query .= "     AND T1.PROFICIENCYDIV = '".$model->leftProficiencyDiv."' ";
        }
        if ($model->leftProficiencyCd) {
            $query .= "     AND T1.PROFICIENCYCD = '".$model->leftProficiencyCd."' ";
        }
        if ($model->leftGroupDiv) {
            $query .= "     AND T1.GROUP_DIV = '".$model->leftGroupDiv."' ";
        }
        if ($model->leftGrade) {
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
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.PROFICIENCY_SUBCLASS_CD ";

        return $query;
    }

    //科目数取得
    function getSubclassCnt($field, $div = "")
    {
        $query  = " SELECT ";
        if ($div == "SELECT") {
            $query .= "     PROFICIENCY_SUBCLASS_CD ";
        } else {
            $query .= "     COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$field["SEMESTER"]."' ";
        $query .= "     AND PROFICIENCYDIV = '".$field["PROFICIENCYDIV"]."' ";
        $query .= "     AND PROFICIENCYCD = '".$field["PROFICIENCYCD"]."' ";
        $query .= "     AND GROUP_DIV = '".$field["GROUP_DIV"]."' ";
        $query .= "     AND GRADE = '".$field["GRADE"]."' ";
        $query .= "     AND COURSECD = '".$field["COURSECD"]."' ";
        $query .= "     AND MAJORCD = '".$field["MAJORCD"]."' ";
        $query .= "     AND COURSECODE = '".$field["COURSECODE"]."' ";

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
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$field["SEMESTER"]."' ";
        $query .= "     AND PROFICIENCYDIV = '".$field["PROFICIENCYDIV"]."' ";
        $query .= "     AND PROFICIENCYCD = '".$field["PROFICIENCYCD"]."' ";
        $query .= "     AND GROUP_DIV = '".$field["GROUP_DIV"]."' ";
        $query .= "     AND GRADE = '".$field["GRADE"]."' ";
        $query .= "     AND COURSECD = '".$field["COURSECD"]."' ";
        $query .= "     AND MAJORCD = '".$field["MAJORCD"]."' ";
        $query .= "     AND COURSECODE = '".$field["COURSECODE"]."' ";

        return $query;
    }

    //模試
    function getProficiencycd($table, $div, $model) {

        $query  = " SELECT ";
        $query .= "     T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     T1.PROFICIENCYCD || ':' || T2.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ".$table." T1 ";
        $query .= "     LEFT JOIN PROFICIENCY_MST T2 ON T1.PROFICIENCYDIV = T2.PROFICIENCYDIV ";
        $query .= "          AND T1.PROFICIENCYCD = T2.PROFICIENCYCD ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->leftSemester."' ";
        $query .= "     AND T1.PROFICIENCYDIV = '".$div."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.PROFICIENCYCD, ";
        $query .= "     T2.PROFICIENCYNAME1 ";
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
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
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
    function getGroupDiv($table)
    {
        $query  = " SELECT ";
        $query .= "     GROUP_DIV AS VALUE, ";
        $query .= "     GROUP_DIV AS LABEL ";
        $query .= " FROM ";
        $query .= "     ".$table." ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     GROUP_DIV ";
        $query .= " ORDER BY ";
        $query .= "     GROUP_DIV ";

        return $query;
    }

    //課程
    function getCourse($model, $semester, $grade, $groupDiv, $ProficiencyDiv, $ProficiencyCd, $key)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || ':' || T1.GROUP_DIV || ':' || T1.COURSECD || ':' || T1.MAJORCD || ':' || T1.COURSECODE || ':' || T1.PROFICIENCYDIV || ':' || T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     G1.GRADE_NAME1 || ' ' || T1.GROUP_DIV || '科 ' || L1.MAJORNAME || ' ' || L2.COURSECODENAME || ' ' || L3.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_GROUP_MST T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.COURSECD = L1.COURSECD ";
        $query .= "          AND T1.MAJORCD = L1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.COURSECODE = L2.COURSECODE ";
        $query .= "     LEFT JOIN PROFICIENCY_MST L3 ON T1.PROFICIENCYDIV = L3.PROFICIENCYDIV ";
        $query .= "          AND T1.PROFICIENCYCD = L3.PROFICIENCYCD ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ON T1.YEAR = G1.YEAR ";
        $query .= "          AND T1.GRADE = G1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "          AND G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($key) {
            $query .= "     AND T1.GRADE || ':' || T1.GROUP_DIV || ':' || T1.COURSECD || ':' || T1.MAJORCD || ':' || T1.COURSECODE || ':' || T1.PROFICIENCYDIV || ':' || T1.PROFICIENCYCD = '".$key."' ";
            if ($semester) {
                $query .= "     AND T1.SEMESTER = '".$semester."' ";
            }
        } else {
            if ($semester) {
                $query .= "     AND T1.SEMESTER = '".$semester."' ";
            }
            if ($grade) {
                $query .= "     AND T1.GRADE = '".$grade."' ";
            }
            if ($groupDiv) {
                $query .= "     AND T1.GROUP_DIV = '".$groupDiv."' ";
            }
            if ($ProficiencyDiv) {
                $query .= "     AND T1.PROFICIENCYDIV = '".$ProficiencyDiv."' ";
            }
            if ($ProficiencyCd) {
                $query .= "     AND T1.PROFICIENCYCD = '".$ProficiencyCd."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科
    function getClass()
    {
        $query  = " SELECT ";
        $query .= "     CLASSCD AS VALUE, ";
        $query .= "     CLASSCD || ':' || CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目
    function getSubclass($model, $key="")
    {
        $query  = " SELECT ";
        $query .= "     PROFICIENCY_SUBCLASS_CD AS VALUE, ";
        $query .= "     PROFICIENCY_SUBCLASS_CD || ':' || SUBCLASS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_MST ";
        if ($key) {
            $query .= " WHERE ";
            $query .= "     PROFICIENCY_SUBCLASS_CD = '".$key."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                if ($key) {
                    $query .= " AND ";
                } else {
                    $query .= " WHERE ";
                }
                $query .= " SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            if ($key) {
                $query .= " AND ";
            } else {
                $query .= " WHERE ";
            }
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科
    function getCopyList($model, $yearDiv = "NOW")
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_GROUP_DAT T1 ";
        $query .= " WHERE ";
        if ($yearDiv == "NOW") {
            $query .= "     YEAR = '".CTRL_YEAR."' ";
        } else {
            $query .= "     YEAR = '".(CTRL_YEAR - 1)."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND   IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "             ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "             ) ";
        }

        return $query;
    }

    function getLastYearList($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_GROUP_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR - 1)."' AND ";
        $query .= "     EXISTS(SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 PROFICIENCY_SUBCLASS_GROUP_MST L1 ";
        $query .= "             WHERE ";
        $query .= "                 YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                 T1.SEMESTER = L1.SEMESTER AND ";
        $query .= "                 T1.PROFICIENCYDIV = L1.PROFICIENCYDIV AND ";
        $query .= "                 T1.PROFICIENCYCD = L1.PROFICIENCYCD AND ";
        $query .= "                 T1.GROUP_DIV = L1.GROUP_DIV AND ";
        $query .= "                 T1.GRADE = L1.GRADE AND ";
        $query .= "                 T1.COURSECD = T1.COURSECD AND ";
        $query .= "                 T1.MAJORCD = L1.MAJORCD AND ";
        $query .= "                 T1.COURSECODE = L1.COURSECODE ";
        $query .= "             ) ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND   IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "             ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "             ) ";
        }

        return $query;
    }





    //--- COPY
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();

        $query = knjz217aQuery::getLastYearList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();

            $data["YEAR"][TEXT]                     = CTRL_YEAR;
            $data["SEMESTER"][TEXT]                 = $row["SEMESTER"];
            $data["GROUP_DIV"][TEXT]                = $row["GROUP_DIV"];
            $data["GRADE"][TEXT]                    = $row["GRADE"];
            $data["COURSECD"][TEXT]                 = $row["COURSECD"];
            $data["MAJORCD"][TEXT]                  = $row["MAJORCD"];
            $data["COURSECODE"][TEXT]               = $row["COURSECODE"];
            $data["PROFICIENCYDIV"][TEXT]           = $row["PROFICIENCYDIV"];
            $data["PROFICIENCYCD"][TEXT]            = $row["PROFICIENCYCD"];
            $data["PROFICIENCY_SUBCLASS_CD"][TEXT]  = $row["PROFICIENCY_SUBCLASS_CD"];
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][NUMBER]                = "sysdate()";

            $query = Query::insertSQL($data, "PROFICIENCY_SUBCLASS_GROUP_DAT");
            $db->query($query);
        }
        $result->free();

        Query::dbCheckIn($db);
        return;
    }

    //--- INSERT
    function &getInsertQuery($fields, $subclass)
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
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        foreach ($subclass as $subKey => $subVal) {
            $data["PROFICIENCY_SUBCLASS_CD"][TEXT]   = $subVal;
            $query = Query::insertSQL($data, "PROFICIENCY_SUBCLASS_GROUP_DAT");
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$fields["SEMESTER"]."' ";
        $query .= "     AND PROFICIENCYDIV = '".$fields["PROFICIENCYDIV"]."' ";
        $query .= "     AND PROFICIENCYCD = '".$fields["PROFICIENCYCD"]."' ";
        $query .= "     AND GROUP_DIV = '".$fields["GROUP_DIV"]."' ";
        $query .= "     AND GRADE = '".$fields["GRADE"]."' ";
        $query .= "     AND COURSECD = '".$fields["COURSECD"]."' ";
        $query .= "     AND MAJORCD = '".$fields["MAJORCD"]."' ";
        $query .= "     AND COURSECODE = '".$fields["COURSECODE"]."' ";
        if ($fields["PROFICIENCY_SUBCLASS_CD"]) {
            $query .= "     AND PROFICIENCY_SUBCLASS_CD = '".$fields["PROFICIENCY_SUBCLASS_CD"]."' ";
        }
        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
    }
}
?>
