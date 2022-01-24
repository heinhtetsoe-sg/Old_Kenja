<?php

require_once('for_php7.php');

class knjj144aQuery extends Query {

    //大会マスタデータ取得
    function getMarathonEventMst($flg = "")
    {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .=  "    COUNT(*) ";
        } else {
            $query .=  "    * ";
        }
        $query .= " FROM ";
        $query .= "     MARATHON_EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEQ  = '00' "; 

        return $query;
    }

    /* データ更新処理 */
    function update($model)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["YEAR"][TEXT]            = CTRL_YEAR;
        $data["SEQ"][TEXT]             = '00';
        $data["NUMBER_OF_TIMES"][TEXT] = $model->field["NUMBER_OF_TIMES"];
        $data["EVENT_NAME"][TEXT]      = $model->field["EVENT_NAME"];
        $data["EVENT_DATE"][TEXT]      = str_replace("/", "-", $model->field["EVENT_DATE"]);
        $data["MAN_METERS"][TEXT]      = $model->field["MAN_METERS"];
        $data["WOMEN_METERS"][TEXT]    = $model->field["WOMEN_METERS"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][NUMBER]       = "sysdate()";

        //存在チェック
        $count = $db->getOne(knjj144aQuery::getMarathonEventMst($cnt));
        if ($count == 0){
            //追加
            $query = Query::insertSQL($data, "MARATHON_EVENT_MST");
        } else {
            //更新
            $where  = " WHERE YEAR = '".CTRL_YEAR."' ";
            $where .= "   AND SEQ  = '00' ";
            $query = Query::updateSQL($data, "MARATHON_EVENT_MST", $where);
        }

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>