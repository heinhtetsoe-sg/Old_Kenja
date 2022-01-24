<?php

require_once('for_php7.php');

class knjz286_2aQuery extends Query {

    //リスト取得
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     POSITION_MST ";
        $query .= " ORDER BY  ";
        $query .= "     POSITIONCD ";

        return $query;
    }

    //１レコード取得
    function getRow($positioncd, $flg="") {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     POSITION_MST ";
        $query .= " WHERE ";
        $query .= "     POSITIONCD = '".$positioncd."' ";

        if (!$flg) {
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $row = $db2->getRow($query, DB_FETCHMODE_ASSOC);
        }

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return $row;
    }

    //年度データの確認
    function IsExisting($positioncd) {
        $db    = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     POSITION_YDAT ";
        $query .= " WHERE ";
        $query .= "     POSITIONCD = '".$positioncd."' ";

        $row = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)) {
            return true;
        } else {
            return false;
        }
    }

    //教育委員会用の学校コード取得
    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "      KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "      V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //UPDATE 
    function &getUpdateQuery($fields, $div) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $data = array();
        if ($div == "add") {
            $data["POSITIONCD"][TEXT]       = $fields["POSITIONCD"];
        }
        $data["POSITIONNAME"][TEXT]         = $fields["POSITIONNAME"];
        $data["BASE_POSITIONNAME"][TEXT]    = $fields["BASE_POSITIONNAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        if ($div == "add") {
            $query = Query::insertSQL($data, "POSITION_MST");
            $db->query($query);

            //学校コード取得
            $schoolcd = $db->getOne(knjz286_2aQuery::getSchoolCd());

            //学校別教育委員会テーブルへのデータ追加
            $data2 = array();
            $data2["EDBOARD_SCHOOLCD"][TEXT]    = $schoolcd;
            $data2["POSITIONCD"][TEXT]          = $fields["POSITIONCD"];
            $data2["EDBOARD_FLG"][TEXT]         = "2";
            $data2["REGISTERCD"][TEXT]          = STAFFCD;
            $data2["UPDATED"][NUMBER]           = "sysdate()";

            $query = Query::insertSQL($data2, "EDBOARD_POSITION_DAT");
            $db2->query($query);
        } else {
            $where = " WHERE POSITIONCD = '".$fields["POSITIONCD"]."' ";
            $query = Query::updateSQL($data, "POSITION_MST", $where);
            $db->query($query);
        }

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //削除
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     POSITION_MST ";
        $query .= " WHERE ";
        $query .= "     POSITIONCD = '".$fields["POSITIONCD"]."' ";

        $db->query($query);

        //学校コード取得
        $schoolcd = $db->getOne(knjz286_2aQuery::getSchoolCd());

        //削除（学校別教育委員会テーブル）
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     EDBOARD_POSITION_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$schoolcd."' AND ";
        $query .= "     POSITIONCD = '".$fields["POSITIONCD"]."' ";

        $db2->query($query);

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return;
    }
}
?>
