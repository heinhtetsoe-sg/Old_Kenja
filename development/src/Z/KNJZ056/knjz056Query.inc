<?php

require_once('for_php7.php');

class knjz056Query extends Query {
    //リスト取得
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.COURSECD   || ':' || L1.COURSENAME AS COURSENAME, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.MAJORCD    || ':' || L2.MAJORNAME AS MAJORNAME, ";
        $query .= "     T1.CATEGORYCD, ";
        $query .= "     T1.CATEGORYCD || ':' || L3.NAME1 AS NAME1";
        $query .= " FROM ";
        $query .= "     MAJOR_CATEGORY_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSE_MST L1 ON  L1.COURSECD = T1.COURSECD ";
        $query .= " LEFT JOIN ";
        $query .= "     MAJOR_MST  L2 ON  L2.MAJORCD  = T1.MAJORCD ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST   L3 ON  L3.NAMECD2  = T1.CATEGORYCD ";
        $query .= "                   AND L3.NAMECD1  = 'E020' ";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.CATEGORYCD ";
        return $query;
    }
    
    //課程コード
    function getCourse()
    {
        $query  = " SELECT ";
        $query .= "     COURSECD || ' ' || COURSENAME AS LABEL, ";
        $query .= "     COURSECD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COURSE_MST ";

        return $query;
    }
    
    //学科コード
    function getMajor($coursecd)
    {
        $query  = " SELECT ";
        $query .= "     T1.MAJORCD || ' ' || T1.MAJORNAME AS LABEL, ";
        $query .= "     T1.MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     MAJOR_MST T1";
        $query .= " WHERE ";
        $query .= "     T1.COURSECD = '{$coursecd}' ";
        return $query;
    }

    //学科分類
    function getMajorName() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E020' ";

        return $query;
    }

    //1レコード取得
    function getMajorCategoryDat($coursecd, $majorcd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAJOR_CATEGORY_DAT ";
        $query .= " WHERE ";
        $query .= "     COURSECD = '{$coursecd}' ";
        $query .= " AND ";
        $query .= "     MAJORCD  = '{$majorcd}' ";
        
        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();

        $data["COURSECD"][TEXT]   = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]    = $fields["MAJORCD"];
        $data["CATEGORYCD"][TEXT] = $fields["CATEGORYCD"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $query = Query::insertSQL($data, "MAJOR_CATEGORY_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        $data["COURSECD"][TEXT]   = $fields["COURSECD"];
        $data["MAJORCD"][TEXT]    = $fields["MAJORCD"];
        $data["CATEGORYCD"][TEXT] = $fields["CATEGORYCD"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $where = "WHERE COURSECD  = '{$fields["COURSECD"]}'";
        $where .= "AND   MAJORCD   = '{$fields["MAJORCD"]}'";
        
        $query = Query::updateSQL($data, "MAJOR_CATEGORY_DAT", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM MAJOR_CATEGORY_DAT "
                ." WHERE COURSECD  = '{$fields["COURSECD"]}'"
                ." AND   MAJORCD   = '{$fields["MAJORCD"]}'";
        $query;
        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }
}
?>
