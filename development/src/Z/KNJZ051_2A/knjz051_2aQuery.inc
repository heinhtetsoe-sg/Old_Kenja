<?php

require_once('for_php7.php');

class knjz051_2aQuery extends Query {

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

    //学科英字
    function getMajorengLength() {
        $query = "SELECT LENGTH FROM (SELECT T1.LENGTH FROM SYSIBM.SYSCOLUMNS T1 WHERE T1.TBNAME = 'MAJOR_MST' AND T1.NAME = 'MAJORENG') T1 ";
        return $query;
    }

    //１レコード取得
    function getRow($coursecd, $majorcd, $flg="") {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     COURSECD = '".$coursecd."' AND ";
        $query .= "     MAJORCD = '".$majorcd."' ";

        if (!$flg) {
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $row = $db2->getRow($query, DB_FETCHMODE_ASSOC);
        }

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

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

    //年度データの確認
    function IsExisting($coursecd, $majorcd) {
        $db    = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAJOR_YDAT ";
        $query .= " WHERE ";
        $query .= "     COURSECD = '".$coursecd."' AND ";
        $query .= "     MAJORCD = '".$majorcd."' ";

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
            $data["COURSECD"][TEXT]         = $fields["COURSECD"];
            $data["MAJORCD"][TEXT]          = $fields["MAJORCD"];
        }
        $data["MAJORNAME"][TEXT]        = $fields["MAJORNAME"];
        $data["MAJORNAME2"][TEXT]       = $fields["MAJORNAME2"];
        $data["MAJORABBV"][TEXT]        = $fields["MAJORABBV"];
        $data["MAJORENG"][TEXT]         = $fields["MAJORENG"];
        $data["MAJORBANKCD"][TEXT]      = $fields["MAJORBANKCD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($div == "add") {
            $query = Query::insertSQL($data, "MAJOR_MST");
            $db->query($query);

            //学校コード取得
            $schoolcd = $db->getOne(knjz051_2aQuery::getSchoolCd());

            //学校別教育委員会テーブルへのデータ追加
            $data2 = array();
            $data2["EDBOARD_SCHOOLCD"][TEXT]    = $schoolcd;
            $data2["COURSECD"][TEXT]            = $fields["COURSECD"];
            $data2["MAJORCD"][TEXT]             = $fields["MAJORCD"];
            $data2["EDBOARD_FLG"][TEXT]         = "2";
            $data2["REGISTERCD"][TEXT]          = STAFFCD;
            $data2["UPDATED"][NUMBER]           = "sysdate()";

            $query = Query::insertSQL($data2, "EDBOARD_MAJOR_DAT");
            $db2->query($query);
        } else {
            $where  = " WHERE COURSECD  = '".$fields["COURSECD"]."' ";
            $where .= "   AND MAJORCD   = '".$fields["MAJORCD"]."' ";
            $query = Query::updateSQL($data, "MAJOR_MST", $where);
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
        $query .= "     MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     COURSECD = '".$fields["COURSECD"]."' AND ";
        $query .= "     MAJORCD = '".$fields["MAJORCD"]."' ";

        $db->query($query);

        //学校コード取得
        $schoolcd = $db->getOne(knjz051_2aQuery::getSchoolCd());

        //削除（学校別教育委員会テーブル）
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     EDBOARD_MAJOR_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$schoolcd."' AND ";
        $query .= "     COURSECD = '".$fields["COURSECD"]."' AND ";
        $query .= "     MAJORCD = '".$fields["MAJORCD"]."' ";

        $db2->query($query);

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return;
    }
}
?>
