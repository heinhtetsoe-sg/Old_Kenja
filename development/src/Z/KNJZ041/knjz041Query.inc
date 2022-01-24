<?php

require_once('for_php7.php');

class knjz041Query extends Query {

    //リスト取得
    function getList() {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.NAME1 AS S_NAME, ";
        $query .= "     T3.NAME1 AS E_NAME ";
        $query .= " FROM ";
        $query .= "     COURSE_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST T2 ON T1.S_PERIODCD = T2.NAMECD2 ";
        $query .= "                          AND T2.NAMECD1 = 'B001' ";
        $query .= "     LEFT JOIN NAME_MST T3 ON T1.E_PERIODCD = T3.NAMECD2 ";
        $query .= "                          AND T3.NAMECD1 = 'B001' ";
        $query .= " ORDER BY  ";
        $query .= "     T1.COURSECD ";

        return $query;
    }

    //１レコード取得
    function getRow($coursecd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     COURSECD = '".$coursecd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //校時名称取得
    function getName() {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'B001' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学校設定課程コードチェック
    function getEdboardCourseDat($coursecd, $flg="") {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     EDBOARD_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     COURSECD = '".$coursecd."' ";
        if ($flg) {
            $query .= "     AND EDBOARD_FLG = '2' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //学科マスタの確認
    function checkMajormst($coursecd)
    {
        $db    = Query::dbCheckOut();
        $query = "SELECT * FROM MAJOR_MST WHERE coursecd = '".$coursecd."'"; 
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)){
            return true;
        } else {
            return false;
        }
    }
    
    //UPDATE 
    function &getUpdateQuery($fields, $div) {
        $db = Query::dbCheckOut();

        $data = array();
        if ($div == "add") {
            $data["COURSECD"][TEXT]     = $fields["COURSECD"];
        }
        $data["COURSENAME"][TEXT]       = $fields["COURSENAME"];
        $data["COURSEABBV"][TEXT]       = $fields["COURSEABBV"];
        $data["COURSEENG"][TEXT]        = $fields["COURSEENG"];
        $data["S_PERIODCD"][TEXT]       = $fields["S_PERIODCD"];
        $data["E_PERIODCD"][TEXT]       = $fields["E_PERIODCD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($div == "add") {
            $query = Query::insertSQL($data, "COURSE_MST");
            $db->query($query);
        } else {
            $where = " WHERE COURSECD = '".$fields["COURSECD"]."' ";
            $query = Query::updateSQL($data, "COURSE_MST", $where);
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        //削除
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     COURSECD = '".$fields["COURSECD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }
}
?>
