<?php

require_once('for_php7.php');

class knjh341Model extends Model {
    var $field      = array();
    var $cmd;
    var $pref_subclasscd = "";        //primary
    var $thisIsGet = false;

    function knjh341Model()
    {
    }

    function init()
    {              
        $this->field = array("PREF_SUBCLASSCD" =>  VARS::post("PREF_SUBCLASSCD"),
                             "SUBCLASS_NAME"    =>  VARS::post("SUBCLASS_NAME"),
                             "SUBCLASS_ABBV"    =>  VARS::post("SUBCLASS_ABBV")
                             );

        $this->cmd          = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->pref_subclasscd);
        }
        $temp = VARS::get("PREF_SUBCLASSCD");
        if (isset($temp)){
            $this->pref_subclasscd = VARS::get("PREF_SUBCLASSCD");
            $this->thisIsGet = true;
        } else {
            $this->thisIsGet = false;
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["PREF_SUBCLASSCD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301","(模試科目コード)");
            return false;
        }
        if (strlen($this->field["SUBCLASS_NAME"]) == 0) {
            $this->setWarning("MSG301","(模試科目名称)");
            return false;
        }
        if (strlen($this->field["SUBCLASS_ABBV"]) == 0) {
            $this->setWarning("MSG301","(模試科目略称)");
            return false;
        }

        if ($this->pref_subclasscd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->pref_subclasscd != $this->field["PREF_SUBCLASSCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //１レコード取得
        $db = Query::dbCheckOut(); //DB接続
        $row = knjh341Query::getRow($this->field["PREF_SUBCLASSCD"], $db);
        Query::dbCheckIn($db);     //DB切断
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(模試科目コード)");
            return false;
        }
        //文字数チェック
        $checkStrLen["SUBCLASS_NAME"] = array("LEN" => 60, "MESAGE" => "(模試科目名称)");
        $checkStrLen["SUBCLASS_ABBV"] = array("LEN" => 15,  "MESAGE" => "(模試科目略称)");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"] ) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                return false;
            }
        }
        return true;
    }
    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }    
    }
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->pref_subclasscd = $this->field["PREF_SUBCLASSCD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->pref_subclasscd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->pref_subclasscd != $this->field["PREF_SUBCLASSCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        $db = Query::dbCheckOut(); //DB接続
        $row = knjh341Query::getRow($this->pref_subclasscd, $db);
        $getcount = knjh341Query::getMockPrefAvgDat($this->pref_subclasscd, $db);
        Query::dbCheckIn($db);     //DB切断
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
        if ($getcount > 0) {
            $this->setWarning("MSG919","県下模試平均点が登録されています。");
            return;    
        }
        knjh341Query::getDeleteQuery($this->field);
        $this->pref_subclasscd = "";
        $this->setMessage("MSG201");
        
        $this->field = array();
    }

    //インサート
    function &insert()
    {
        $result = knjh341Query::getInsertQuery($this->field);
        return $result;
    }
    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjh341Query::getUpdateQuery($this->field);
        return $result;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
