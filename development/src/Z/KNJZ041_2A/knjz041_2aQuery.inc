<?php

require_once('for_php7.php');

class knjz041_2aQuery extends Query {

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
    function getRow($coursecd, $flg="") {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     COURSECD = '".$coursecd."' ";

        if (!$flg) {
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $row = $db2->getRow($query, DB_FETCHMODE_ASSOC);
        }

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

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

    //年度データの確認
    function IsExisting($coursecd) {
        $db    = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COURSE_YDAT ";
        $query .= " WHERE ";
        $query .= "     COURSECD = '".$coursecd."' ";

        $row = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)) {
            return true;
        } else {
            return false;
        }
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

            //学校コード取得
            $schoolcd = $db->getOne(knjz041_2aQuery::getSchoolCd());

            //学校別教育委員会テーブルへのデータ追加
            $data2 = array();
            $data2["EDBOARD_SCHOOLCD"][TEXT]    = $schoolcd;
            $data2["COURSECD"][TEXT]            = $fields["COURSECD"];
            $data2["EDBOARD_FLG"][TEXT]         = "2";
            $data2["REGISTERCD"][TEXT]          = STAFFCD;
            $data2["UPDATED"][NUMBER]           = "sysdate()";

            $query = Query::insertSQL($data2, "EDBOARD_COURSE_DAT");
            $db2->query($query);
        } else {
            $where = " WHERE COURSECD = '".$fields["COURSECD"]."' ";
            $query = Query::updateSQL($data, "COURSE_MST", $where);
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
        $query .= "     COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     COURSECD = '".$fields["COURSECD"]."' ";

        $db->query($query);

        //学校コード取得
        $schoolcd = $db->getOne(knjz041_2aQuery::getSchoolCd());

        //削除（学校別教育委員会テーブル）
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     EDBOARD_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$schoolcd."' AND ";
        $query .= "     COURSECD = '".$fields["COURSECD"]."' ";

        $db2->query($query);

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return;
    }
}
?>
