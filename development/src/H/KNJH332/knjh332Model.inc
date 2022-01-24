<?php

require_once('for_php7.php');

class knjh332Model extends Model {
    var $field      = array();
    var $cmd;
    var $targetcd;        //primary
    var $staffcd;
    var $groupcd;

    function knjh332Model()
    {
    }

    function init()
    {              

        if (VARS::get("PROGRAMID") || $this->cmd == "group"){
        	$this->groupcd = "";
        	$this->targetcd = "";
        }
        if (VARS::post("GROUPCD")!=""){
        	$this->groupcd = VARS::post("GROUPCD");
        }

        $this->field = array("TARGETCD"      =>  VARS::post("TARGETCD"),
                             "TARGETNAME1"   =>  VARS::post("TARGETNAME1"),
                             "UPDATED"       =>  VARS::post("UPDATED")
                             );

        $this->cmd          = VARS::request("cmd");

        if (VARS::get("mode") == "1"){
            unset($this->targetcd);
        }
        if (VARS::get("PROGRAMID")){
        	$this->targetcd = "";
        }
        $temp = VARS::get("TARGETCD");
        if (isset($temp)){
            $this->targetcd = VARS::get("TARGETCD");
        }

        if (VARS::get("GROUPCD")){
            $this->groupcd   = VARS::get("GROUPCD");
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["TARGETCD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301","(目標値コード)");
            return false;
        }
        if ($this->targetcd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->targetcd != $this->field["TARGETCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //１レコード取得
        $row = knjh332Query::getRow($this->field["TARGETCD"],$this->groupcd);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(目標値コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["TARGETNAME1"]) == 0) {
            $this->setWarning("MSG301","(目標値名称)");
            return false;
        } elseif (strlen($this->field["TARGETNAME1"]) > 60 ) {
            $this->setWarning("MSG915", "(目標値名称) 60バイトまで");
            return false;
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
            $this->targetcd = $this->field["TARGETCD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->targetcd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->targetcd != $this->field["TARGETCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
         $row = knjh332Query::getRow($this->targetcd,$this->groupcd);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
        knjh332Query::getDeleteQuery($this);
        $this->targetcd = "";
        $this->setMessage("MSG201");
    }

    function &insert()
    {
        $result = knjh332Query::getInsertQuery($this);
        return $result;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjh332Query::getUpdateQuery($this);
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
