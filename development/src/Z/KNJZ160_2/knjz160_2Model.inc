<?php

require_once('for_php7.php');

class knjz160_2Model extends Model {

    var $field      = array();
    var $cmd;
    var $faccd = "";        //primary
  
    function knjz160_2Model()
    {
    }

    function init()
    {              
        $this->field = array("FACCD"            => VARS::post("FACCD"),
                             "FACILITYNAME"     => VARS::post("FACILITYNAME"),
                             "FACILITYABBV"     => VARS::post("FACILITYABBV"),
                             "CAPACITY"         => VARS::post("CAPACITY"),   
                             "CHR_CAPACITY"     => VARS::post("CHR_CAPACITY"),   
                             "UPDATED"          => VARS::post("UPDATED"));
        if($this->field["FACCD"]!="") $this->field["FACCD"] = sprintf("%04d",VARS::post("FACCD"));

        $this->cmd             = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->faccd);
        }
        
        $temp = VARS::get("FACCD");
        if (isset($temp)){
            $this->faccd = VARS::get("FACCD");
        }
    }
    
    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["FACCD"]) == "" && $this->cmd == "add") {
            $this->setWarning("キー値が入力されていません。", "(施設コード)");
            return false;
        }
        if ($this->faccd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->cmd == "update" && $this->faccd != $this->field["FACCD"]) {
            $this->setWarning("施設コードは変更できません");
            return;
        }

        //１レコード取得
        $row = knjz160_2Query::getRow($this->field["FACCD"]);
        
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(施設コード)");
            return false;
        }

        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }
        
        if (mbereg("[｡-ﾟ]",$this->field["FACILITYNAME"]) || mbereg("[｡-ﾟ]",$this->field["FACILITYABBV"])) {
            $this->setWarning("MSG901","半角カナは入力できません");
            return false;
        }
        //文字数と入力値チェック
        foreach (array("FACILITYNAME" => array(30, "(施設名)"),
                       "FACILITYABBV" => array(9,"(施設略称)"),
                       "CAPACITY" => array(9,"(定員数)"),
                       "CHR_CAPACITY"     => array(4,"(収容講座数)")) as $key => $val) {
        
            if ($key != "CAPACITY" && mbereg("[｡-ﾟ]",$this->field[$key])) {
                $this->setWarning("MSG901","半角カナは入力できません");
                return false;
            }
            if ($key != "CHR_CAPACITY" && mbereg("[｡-ﾟ]",$this->field[$key])) {
                $this->setWarning("MSG901","半角カナは入力できません");
                return false;
            }
            if (strlen($this->field[$key]) > $val[0]) {
                $this->setWarning("MSG914",$val[1].$val[0]."バイトまで");
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
            $this->faccd = $this->field["FACCD"];
            $this->setMessage("MSG201");
        }
        
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->faccd == "") {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->faccd != $this->field["FACCD"]) {
            $this->setWarning("MSG308");
            return;
        }
        $row = knjz160_2Query::getRow($this->faccd);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
        if (knjz160_2Query::IsExisting($this->faccd)){
            $this->setWarning("MSG919");
            return;
        }
        knjz160_2Query::getDeleteQuery($this->field);
        $this->faccd="";
        $this->setMessage("MSG201");
    }
 
    function &insert()
    {
        knjz160_2Query::getInsertQuery($this->field);
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjz160_2Query::getUpdateQuery($this->field);
    }
    
    function clean()
    {
    }
}
?>
