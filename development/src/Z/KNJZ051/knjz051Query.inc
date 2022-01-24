<?php

require_once('for_php7.php');

class knjz051Query extends Query {

    //リスト取得
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAJOR_MST ";
        $query .= " ORDER BY  ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD ";

        return $query;
    }

    //１レコード取得
    function getRow($coursecd, $majorcd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     COURSECD = '".$coursecd."' AND ";
        $query .= "     MAJORCD = '".$majorcd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //課程取得
    function getCourse() {
        $query  = " SELECT ";
        $query .= "     COURSECD AS VALUE, ";
        $query .= "     COURSECD || ' ' || COURSENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COURSE_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校設定学科コードチェック
    function getEdboardMajorDat($coursecd, $majorcd, $flg="") {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     EDBOARD_MAJOR_DAT ";
        $query .= " WHERE ";
        $query .= "     COURSECD = '".$coursecd."' AND ";
        $query .= "     MAJORCD = '".$majorcd."' ";
        if ($flg) {
            $query .= "     AND EDBOARD_FLG = '2' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //UPDATE 
    function &getUpdateQuery($fields, $div) {
        $db = Query::dbCheckOut();

        $data = array();
        if ($div == "add") {
            $data["COURSECD"][TEXT]     = $fields["COURSECD"];
            $data["MAJORCD"][TEXT]      = $fields["MAJORCD"];
        }
        $data["MAJORNAME"][TEXT]        = $fields["MAJORNAME"];
        $data["MAJORABBV"][TEXT]        = $fields["MAJORABBV"];
        $data["MAJORENG"][TEXT]         = $fields["MAJORENG"];
        $data["MAJORBANKCD"][TEXT]      = $fields["MAJORBANKCD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($div == "add") {
            $query = Query::insertSQL($data, "MAJOR_MST");
            $db->query($query);
        } else {
            $where  = " WHERE COURSECD  = '".$fields["COURSECD"]."' ";
            $where .= "   AND MAJORCD   = '".$fields["MAJORCD"]."' ";
            $query = Query::updateSQL($data, "MAJOR_MST", $where);
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
        $query .= "     MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     COURSECD    = '".$fields["COURSECD"]."' AND ";
        $query .= "     MAJORCD     = '".$fields["MAJORCD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }
}
?>
