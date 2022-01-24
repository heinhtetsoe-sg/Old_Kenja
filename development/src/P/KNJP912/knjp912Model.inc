<?php

require_once('for_php7.php');


class knjp912Model extends Model {

    var $cmd;
    var $field  = array();
    var $data  = array();

    function init() {
        $this->field = array("STATUS_REQUEST_NO"  =>  VARS::post("STATUS_REQUEST_NO"),
                             "HENKIN_DATE"        =>  VARS::post("HENKIN_DATE")
                            );
        $this->cmd         = VARS::request("cmd");
    }
    
    //締めデータのUPDATE
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
                
        //対象の生徒ごとの締めデータ取得
        $upflg = false;
        $query = knjp912Query::getOutGoHenkinData($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //生徒ごとに実行時点で返金可能かをチェック
            $henkin_kanou_gk = $db->getOne(knjp912Query::getSchnoCollectMoneyData($row, $this));
            if ($row["COMMODITY_PRICE"] > intval($henkin_kanou_gk)) {
                $this->setWarning("MSG203", "返金額が生徒の返金可能な金額を超えています。\\n対象の支出伺をキャンセルし、新規で支出伺を作成し直して下さい。\\n(伝票番号：".$row["REQUEST_NO"]."　対象生徒：".$row["SCHREGNO"].")");
                return false;
            }
            //対象データ更新
            knjp912Query::updateQuery($db, $row, $this);
            $upflg = true;
        }
        $result->free();

        if (!$upflg) {
            $this->setMessage("MSG303");
            return false;
        }
        if (!isset($this->warning)){
            $this->setMessage("MSG201");
        }
        
        $db->commit();
        Query::dbCheckIn($db);
        
        return;
    }
}
?>
