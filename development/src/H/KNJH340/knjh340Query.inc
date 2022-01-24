<?php

require_once('for_php7.php');

class knjh340Query extends Query {

    //年度
    function getYear($div = "OYEAR")
    {
        $query  = " WITH CTRL_T (LABEL, VALUE) AS ( ";
        $query .= " VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     MOCK_PERFECT_DAT ";
        if ($div == "OYEAR") {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     CTRL_T ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト
    function getList($model, $select = "")
    {

        if ($select == "CHECK") {
            $course_div         = $model->field["COURSE_DIV"];
            $grade              = $model->field["GRADE"];
            $mock_subclass_cd   = $model->field["MOCK_SUBCLASS_CD"];
        } else {
            $course_div         = $model->course_div;
            $grade              = $model->grade;
            $mock_subclass_cd   = $model->mock_subclass_cd;
        }

        $query  = " SELECT ";
        if ($select == "CNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.*, ";
            $query .= "     L1.SUBCLASS_NAME, ";
            $query .= "     L2.NAME1 AS COURSE_NAME ";
        }
        $query .= " FROM ";
        $query .= "     MOCK_PERFECT_DAT T1 ";
        $query .= "     LEFT JOIN MOCK_SUBCLASS_MST L1 ON L1.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ";
        $query .= "     LEFT JOIN V_NAME_MST L2 ON L2.YEAR = '".$model->oyear."' ";
        $query .= "          AND L2.NAMECD1 = 'H309' ";
        $query .= "          AND L2.NAMECD2 = T1.COURSE_DIV ";
        $query .= " WHERE ";
        if ($select == "ONE" || $select == "CHECK") {
            $query .= "     T1.YEAR = '".$model->oyear."' ";
            $query .= "     AND T1.COURSE_DIV = '".$course_div."' ";
            $query .= "     AND T1.GRADE = '".$grade."' ";
            $query .= "     AND T1.MOCK_SUBCLASS_CD = '".$mock_subclass_cd."' ";
        } else {
            $query .= "     T1.YEAR = '".$model->oyear."' ";
        }
        if ($select != "CNT") {
            $query .= " ORDER BY ";
            $query .= "     T1.COURSE_DIV, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.MOCK_SUBCLASS_CD ";
        }

        return $query;
    }

    //区分
    function getCourse($model)
    {

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->oyear."' ";
        $query .= "     AND NAMECD1 = 'H309' ";

        return $query;
    }

    //学年
    function getGrade($model)
    {

        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     CAST(CAST(GRADE AS SMALLINT) AS CHAR(1)) || '学年' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->oyear."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //模試科目
    function getMockSubclass()
    {

        $query  = " SELECT ";
        $query .= "     MOCK_SUBCLASS_CD AS VALUE, ";
        $query .= "     SUBCLASS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コピー処理
    function getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "INSERT INTO MOCK_PERFECT_DAT ";
        $query .= "( ";
        $query .= "SELECT ";
        $query .= "    '".$model->oyear."', ";
        $query .= "    COURSE_DIV, ";
        $query .= "    GRADE, ";
        $query .= "    MOCK_SUBCLASS_CD, ";
        $query .= "    PERFECT, ";
        $query .= "    PASS_SCORE, ";
        $query .= "    '".STAFFCD."', ";
        $query .= "    SYSDATE() ";
        $query .= "FROM ";
        $query .= "    MOCK_PERFECT_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->ryear."' ";
        $query .= ") ";

        $db->query($query);
        $db->commit();

        Query::dbCheckIn($db);

        return;
    }

    //--- INSERT
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = $model->oyear;
        $data["COURSE_DIV"][TEXT]       = $fields["COURSE_DIV"];
        $data["GRADE"][TEXT]            = $fields["GRADE"];
        $data["MOCK_SUBCLASS_CD"][TEXT] = $fields["MOCK_SUBCLASS_CD"];
        $data["PERFECT"][NUMBER]        = $fields["PERFECT"];
        $data["PASS_SCORE"][NUMBER]     = $fields["PASS_SCORE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "MOCK_PERFECT_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data["PERFECT"][NUMBER]        = $fields["PERFECT"];
        $data["PASS_SCORE"][NUMBER]     = $fields["PASS_SCORE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR = '".$model->oyear."' ";
        $where .= "     AND COURSE_DIV = '" .$fields["COURSE_DIV"]."' ";
        $where .= "     AND GRADE = '" .$fields["GRADE"]."' ";
        $where .= "     AND MOCK_SUBCLASS_CD = '" .$fields["MOCK_SUBCLASS_CD"]."' ";

        $query = Query::updateSQL($data, "MOCK_PERFECT_DAT", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($db, $model, $fields)
    {

        $query  = " DELETE FROM ";
        $query .= "     MOCK_PERFECT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".$model->oyear."' ";
        $query .= "     AND COURSE_DIV  = '".$fields["COURSE_DIV"]."' ";
        $query .= "     AND GRADE  = '".$fields["GRADE"]."' ";
        $query .= "     AND MOCK_SUBCLASS_CD  = '".$fields["MOCK_SUBCLASS_CD"]."' ";

        $db->query($query);

        return $result;
    }
}
?>
