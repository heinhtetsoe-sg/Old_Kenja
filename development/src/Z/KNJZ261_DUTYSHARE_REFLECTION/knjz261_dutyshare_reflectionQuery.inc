<?php

require_once('for_php7.php');

class knjz261_dutyshare_reflectionQuery extends Query {

    //校務分掌一覧取得
    function selectQuery($dutysharecd="") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     DUTYSHARE_MST ";
        if ($dutysharecd) {
            $query .= " WHERE ";
            $query .= "     DUTYSHARECD = '".$dutysharecd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     DUTYSHARECD ";

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

        //校務分掌コード配列に格納
        $array = explode (",", $model->selectdata);
        $db->autoCommit(false);
        $db2->autoCommit(false);

        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++) {

                //学校校務分掌コードチェック
                $query = knjz261_dutyshare_reflectionQuery::selectQuery($array[$i]);
                $school_dutyshare = $db->getOne($query);

                if (!$school_dutyshare) {
                    //教育委員会の校務分掌マスタを参照
                    $query = knjz261_dutyshare_reflectionQuery::selectQuery($array[$i]);
                    $edboard = $db2->getRow($query, DB_FETCHMODE_ASSOC);

                    //追加
                    $data = array();
                    $data["DUTYSHARECD"][TEXT]      = $edboard["DUTYSHARECD"];
                    $data["SHARENAME"][TEXT]        = $edboard["SHARENAME"];
                    $data["BASE_SHARENAME"][TEXT]   = $edboard["BASE_SHARENAME"];
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "sysdate()";

                    $db->query(Query::insertSQL($data, "DUTYSHARE_MST"));

                    //学校コード取得
                    $schoolcd = $db->getOne(knjz261_dutyshare_reflectionQuery::getSchoolCd());

                    //学校別教育委員会テーブルへのデータ追加
                    $data2 = array();
                    $data2["EDBOARD_SCHOOLCD"][TEXT]    = $schoolcd;
                    $data2["DUTYSHARECD"][TEXT]         = $edboard["DUTYSHARECD"];
                    $data2["EDBOARD_FLG"][TEXT]         = "1";
                    $data2["REGISTERCD"][TEXT]          = STAFFCD;
                    $data2["UPDATED"][NUMBER]           = "sysdate()";

                    $query = Query::insertSQL($data2, "EDBOARD_DUTYSHARE_DAT");
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
