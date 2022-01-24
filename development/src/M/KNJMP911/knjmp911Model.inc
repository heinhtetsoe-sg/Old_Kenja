<?php

require_once('for_php7.php');


class knjmp911Model extends Model {

    var $cmd;
    var $field  = array();
    var $data  = array();

    function init() {
        $this->field = array("CLOSE_FLG"  =>  VARS::post("CLOSE_FLG"));         //NULL:仮締め、1:本締め
        $this->cmd         = VARS::request("cmd");
    }
    
    //締めデータのUPDATE
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        //次年度の繰越金項目を取得
        $getCount = "";
        if ($this->field["CLOSE_FLG"] === '1') {
            $getCount = $db->getOne(knjmp911Query::getKurikosicd("count"));
            if ($getCount == 0) {
                $this->setWarning("MSG203", "次年度の繰越金の項目が設定されていません。");
                return false;
            }
            //次年度の繰越金データ削除(Delete Insert用)
            knjmp911Query::deleteKurikosiData($db);
        }

        //対象の締めデータ取得
        //Delete
        knjmp911Query::deleteCloseData($db);
        //Insert
        $query = knjmp911Query::getCloseData();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            knjmp911Query::updateCloseData($db, $row, $this);
            //本締めの場合は、次年度に繰越金として、データ生成
            if ($this->field["CLOSE_FLG"] === '1') {
                $getCount = $db->getOne(knjmp911Query::getKurikosicd("count", $row["CLOSE_L_CD"]));
                if ($getCount == 0) {
                    $this->setWarning("MSG203", "次年度の繰越金の項目が設定されていません。(科目コード:".$row["CLOSE_L_CD"].")");
                    return false;
                }
                //次年度の繰越金データ作成
                knjmp911Query::updateKurikosiData($db, $row, $this);
            }
        }
        $result->free();
        
        //対象の生徒ごとの締めデータ取得
        //Delete
        knjmp911Query::deleteCloseSchregData($db);
        //Insert
        $query = knjmp911Query::getCloseSchregData();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            knjmp911Query::updateCloseSchregData($db, $row, $this);
        }
        $result->free();

        if (!isset($this->warning)){
            $this->setMessage("MSG201");
        }
        
        $db->commit();
        Query::dbCheckIn($db);
        
        return;
    }
}
?>
