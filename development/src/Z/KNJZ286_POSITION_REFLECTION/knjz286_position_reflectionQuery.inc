<?php

require_once('for_php7.php');

class knjz286_position_reflectionQuery extends Query {

    //教務主任等一覧取得
    function selectQuery($positioncd="") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     POSITION_MST ";
        if ($positioncd) {
            $query .= " WHERE ";
            $query .= "     POSITIONCD = '".$positioncd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     POSITIONCD ";

        return $query;
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
    function &getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //教務主任等コード配列に格納
        $array = explode (",", $model->selectdata);
        $db->autoCommit(false);
        $db2->autoCommit(false);

        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++) {

                //学校教務主任等コードチェック
                $query = knjz286_position_reflectionQuery::selectQuery($array[$i]);
                $school_position = $db->getOne($query);

                if (!$school_position) {
                    //教育委員会の教務主任等マスタを参照
                    $query = knjz286_position_reflectionQuery::selectQuery($array[$i]);
                    $edboard = $db2->getRow($query, DB_FETCHMODE_ASSOC);

                    //追加
                    $data = array();
                    $data["POSITIONCD"][TEXT]           = $edboard["POSITIONCD"];
                    $data["POSITIONNAME"][TEXT]         = $edboard["POSITIONNAME"];
                    $data["BASE_POSITIONNAME"][TEXT]    = $edboard["BASE_POSITIONNAME"];
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][NUMBER]            = "sysdate()";

                    $db->query(Query::insertSQL($data, "POSITION_MST"));

                    //学校コード取得
                    $schoolcd = $db->getOne(knjz286_position_reflectionQuery::getSchoolCd());

                    //学校別教育委員会テーブルへのデータ追加
                    $data2 = array();
                    $data2["EDBOARD_SCHOOLCD"][TEXT]    = $schoolcd;
                    $data2["POSITIONCD"][TEXT]          = $edboard["POSITIONCD"];
                    $data2["EDBOARD_FLG"][TEXT]         = "1";
                    $data2["REGISTERCD"][TEXT]          = STAFFCD;
                    $data2["UPDATED"][NUMBER]           = "sysdate()";

                    $query = Query::insertSQL($data2, "EDBOARD_POSITION_DAT");
                    $db2->query($query);
                }
            }
        }
        $db->commit();
        $db2->commit();

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return;
    }
}
?>
