<?php

require_once('for_php7.php');

class knjz217Query extends Query {

    //リスト
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     T1.GROUP_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     L3.GROUP_NAME, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     L1.COURSENAME || L1.MAJORNAME || L2.COURSECODENAME AS COURSE_NAME, ";
        $query .= "     T1.MOCKCD || T1.GROUP_DIV || T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS KEY, ";
        $query .= "     T1.MOCK_SUBCLASS_CD, ";
        $query .= "     L4.SUBCLASS_NAME ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_GROUP_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.COURSECD || T1.MAJORCD = L1.COURSECD || L1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.COURSECODE = L2.COURSECODE ";
        $query .= "     LEFT JOIN MOCK_SUBCLASS_GROUP_MST L3 ON T1.YEAR = L3.YEAR ";
        $query .= "          AND T1.MOCKCD = L3.MOCKCD ";
        $query .= "          AND T1.GROUP_DIV = L3.GROUP_DIV ";
        $query .= "          AND T1.GRADE = L3.GRADE ";
        $query .= "          AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = L3.COURSECD || L3.MAJORCD || L3.COURSECODE ";
        $query .= "     LEFT JOIN MOCK_SUBCLASS_MST L4 ON T1.MOCK_SUBCLASS_CD = L4.MOCK_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->leftMockcd) {
            $query .= "     AND T1.MOCKCD = '".$model->leftMockcd."' ";
        }
        if ($model->leftGroupDiv) {
            $query .= "     AND T1.GROUP_DIV = '".$model->leftGroupDiv."' ";
        }
        if ($model->leftGrade) {
            $query .= "     AND T1.GRADE = '".$model->leftGrade."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.GROUP_DIV, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.MOCK_SUBCLASS_CD ";

        return $query;
    }

    //科目数取得
    function getSubclassCnt($field, $div = "")
    {
        $query  = " SELECT ";
        if ($div == "SELECT") {
            $query .= "     MOCK_SUBCLASS_CD ";
        } else {
            $query .= "     COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND MOCKCD = '".$field["MOCKCD"]."' ";
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
        $query .= "     MOCK_SUBCLASS_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND MOCKCD = '".$field["MOCKCD"]."' ";
        $query .= "     AND GROUP_DIV = '".$field["GROUP_DIV"]."' ";
        $query .= "     AND GRADE = '".$field["GRADE"]."' ";
        $query .= "     AND COURSECD = '".$field["COURSECD"]."' ";
        $query .= "     AND MAJORCD = '".$field["MAJORCD"]."' ";
        $query .= "     AND COURSECODE = '".$field["COURSECODE"]."' ";

        return $query;
    }

    //模試
    function getMockcd($table) {

        $query  = " SELECT ";
        $query .= "     T1.MOCKCD AS VALUE, ";
        $query .= "         T1.MOCKCD || '：' || T2.MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ".$table." T1 ";
        $query .= "     LEFT JOIN MOCK_MST T2 ";
        $query .= "     ON T1.MOCKCD = T2.MOCKCD ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     T2.MOCKNAME1 ";
        $query .= " ORDER BY ";
        $query .= "     T1.MOCKCD ";

        return $query;
    }

    //学年
    function getGrade($table)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE AS LABEL ";
        $query .= " FROM ";
        $query .= "     ".$table." ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     GRADE ";
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
    function getCourse($grade, $groupDiv, $mockCd, $key)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || ':' || T1.GROUP_DIV || ':' || T1.COURSECD || ':' || T1.MAJORCD || ':' || T1.COURSECODE || ':' || T1.MOCKCD AS VALUE, ";
        $query .= "     T1.GRADE || '年 ' || T1.GROUP_DIV || '科 ' || L1.MAJORNAME || ' ' || L2.COURSECODENAME || ' ' || L3.MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_GROUP_MST T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.COURSECD = L1.COURSECD ";
        $query .= "          AND T1.MAJORCD = L1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.COURSECODE = L2.COURSECODE ";
        $query .= "     LEFT JOIN MOCK_MST L3 ON T1.MOCKCD = L3.MOCKCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($key) {
            $query .= "     AND T1.GRADE || ':' || T1.GROUP_DIV || ':' || T1.COURSECD || ':' || T1.MAJORCD || ':' || T1.COURSECODE || ':' || T1.MOCKCD = '".$key."' ";
        } else {
            if ($grade) {
                $query .= "     AND T1.GRADE = '".$grade."' ";
            }
            if ($groupDiv) {
                $query .= "     AND T1.GROUP_DIV = '".$groupDiv."' ";
            }
            if ($mockCd) {
                $query .= "     AND T1.MOCKCD = '".$mockCd."' ";
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
    function getSubclass($key)
    {
        $query  = " SELECT ";
        $query .= "     MOCK_SUBCLASS_CD AS VALUE, ";
        $query .= "     MOCK_SUBCLASS_CD || ':' || SUBCLASS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_MST ";
        if ($key) {
            $query .= " WHERE ";
            $query .= "     MOCK_SUBCLASS_CD = '".$key."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科
    function getCopyList($yearDiv = "NOW")
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_GROUP_DAT ";
        $query .= " WHERE ";
        if ($yearDiv == "NOW") {
            $query .= "     YEAR = '".CTRL_YEAR."' ";
        } else {
            $query .= "     YEAR = '".(CTRL_YEAR - 1)."' ";
        }

        return $query;
    }

    function getLastYearList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_GROUP_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR - 1)."' AND ";
        $query .= "     EXISTS(SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 MOCK_SUBCLASS_GROUP_MST L1 ";
        $query .= "             WHERE ";
        $query .= "                 YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                 T1.MOCKCD = L1.MOCKCD AND ";
        $query .= "                 T1.GROUP_DIV = L1.GROUP_DIV AND ";
        $query .= "                 T1.GRADE = L1.GRADE AND ";
        $query .= "                 T1.COURSECD = T1.COURSECD AND ";
        $query .= "                 T1.MAJORCD = L1.MAJORCD AND ";
        $query .= "                 T1.COURSECODE = L1.COURSECODE ";
        $query .= "             ) ";

        return $query;
    }





    //--- COPY
    function &getCopyQuery()
    {
        $db = Query::dbCheckOut();

        $query = knjz217Query::getLastYearList("LAST");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();

            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["GROUP_DIV"][TEXT]        = $row["GROUP_DIV"];
            $data["GRADE"][TEXT]            = $row["GRADE"];
            $data["COURSECD"][TEXT]         = $row["COURSECD"];
            $data["MAJORCD"][TEXT]          = $row["MAJORCD"];
            $data["COURSECODE"][TEXT]       = $row["COURSECODE"];
            $data["MOCKCD"][TEXT]           = $row["MOCKCD"];
            $data["MOCK_SUBCLASS_CD"][TEXT] = $row["MOCK_SUBCLASS_CD"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "MOCK_SUBCLASS_GROUP_DAT");
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

        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["MOCKCD"][TEXT]       = $fields["MOCKCD"];
        $data["GROUP_DIV"][TEXT]    = $fields["GROUP_DIV"];
        $data["GRADE"][TEXT]        = $fields["GRADE"];
        $data["COURSECD"][TEXT]     = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]      = $fields["MAJORCD"];
        $data["COURSECODE"][TEXT]   = $fields["COURSECODE"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        foreach ($subclass as $subKey => $subVal) {
            $data["MOCK_SUBCLASS_CD"][TEXT]   = $subVal;
            $query = Query::insertSQL($data, "MOCK_SUBCLASS_GROUP_DAT");
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
        $query .= "     MOCK_SUBCLASS_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND MOCKCD = '".$fields["MOCKCD"]."' ";
        $query .= "     AND GROUP_DIV = '".$fields["GROUP_DIV"]."' ";
        $query .= "     AND GRADE = '".$fields["GRADE"]."' ";
        $query .= "     AND COURSECD = '".$fields["COURSECD"]."' ";
        $query .= "     AND MAJORCD = '".$fields["MAJORCD"]."' ";
        $query .= "     AND COURSECODE = '".$fields["COURSECODE"]."' ";
        if ($fields["MOCK_SUBCLASS_CD"]) {
            $query .= "     AND MOCK_SUBCLASS_CD = '".$fields["MOCK_SUBCLASS_CD"]."' ";
        }
        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
    }
}
?>
