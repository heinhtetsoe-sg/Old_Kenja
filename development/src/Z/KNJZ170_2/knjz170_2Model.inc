<?php

require_once('for_php7.php');


class knjz170_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $groupcd;        //primary

    function knjz170_2Model()
    {
        $this->groupcd = "";
    }

    function init()
    {              
        $this->field = array("GROUPCD"      =>  VARS::post("GROUPCD"),
                             "GROUPNAME"    =>  VARS::post("GROUPNAME"),
                             "GROUPABBV"    =>  VARS::post("GROUPABBV"),
                             "REMARK"       =>  VARS::post("REMARK"),
                             "SHOWORDER"    =>  VARS::post("SHOWORDER"),
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd      = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
          //  unset($this->groupcd);
             $this->groupcd = "";
        }
        $temp = VARS::get("GROUPCD");
        if (isset($temp)){
            $this->groupcd      = VARS::get("GROUPCD");
        }
    }
    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["GROUPCD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301", "(群コード)");
            return false;
        }
        if ($this->groupcd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308");
            return false;
        }

        $checkArray = array("GROUPNAME" => "(群名称)", "GROUPABBV" => "(群略称)");
        foreach ($checkArray as $key => $val) {
            if ($this->field[$key] == "") {
                $this->setWarning("MSG301", $val);
                return false;
            }
        }

        if ($this->cmd == "update" && $this->groupcd != $this->field["GROUPCD"]) {
            $this->setWarning("MSG308","キー値が変更されています。");
            return;
        }
        //１レコード取得
        $row = knjz170_2Query::getRow($this->field["GROUPCD"]);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG302", "(群コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }
        if (strlen($this->field["GROUPNAME"]) >9 ) {
            $this->setWarning("MSG914", "(群名称) 9バイトまで");
            return false;
        } elseif (strlen($this->field["GROUPABBV"]) >6 ) {
            $this->setWarning("MSG914", "(群略称) 6バイトまで");
            return false;
        } elseif (strlen($this->field["REMARK"])>90){ 
            $this->setWarning("MSG914", "(備考) 90バイトまで");
            return false;
        } elseif (strlen($this->field["SHOWORDER"])> 4){ 
            $this->setWarning("MSG914", "(表示順) 4バイトまで");
            return false;
        }
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
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
            $this->groupcd = $this->field["GROUPCD"];
            $this->setMessage("MSG201");
        }
    }

    //DELETE
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->groupcd == "") {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->groupcd != $this->field["GROUPCD"]) {
            $this->setWarning("MSG308","キー値が変更されています。");
            return;
        }
         $row = knjz170_2Query::getRow($this->groupcd);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
        if (knjz170_2Query::IsExisting($this->groupcd)){
            $this->setWarning("MSG919");
            return;
        }
        knjz170_2Query::getDeleteQuery($this->field);
        unset($this->groupcd);
        $this->setMessage("MSG201");
    }

    function &insert()
    {
        knjz170_2Query::getInsertQuery($this->field);
    }

    function &update()
    {
        knjz170_2Query::getUpdateQuery($this->field);
    }

    function clean()
    {
    }
}
?>
