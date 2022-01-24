<?php

require_once('for_php7.php');

class knjj201_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $div = "";        //primary
    var $executivecd = "";        //primary

    function knjj201_2Model()
    {
    }

    function init()
    {              
        $this->field = array("DIV"          =>  VARS::post("DIV"),
                             "EXECUTIVECD"  =>  VARS::post("EXECUTIVECD"),
                             "NAME"         =>  VARS::post("NAME"),
                             "ABBV"         =>  VARS::post("ABBV"),
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd          = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->div);
            unset($this->executivecd);
        }
        $temp = VARS::get("DIV");
        if (isset($temp)){
            $this->div = VARS::get("DIV");
        }
        $temp = VARS::get("EXECUTIVECD");
        if (isset($temp)){
            $this->executivecd = VARS::get("EXECUTIVECD");
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if ((!strlen($this->field["DIV"]) || !strlen($this->field["EXECUTIVECD"])) && $this->cmd == "add") {
            $this->setWarning("MSG301","(区分及びコード)");
            return false;
        }
        if ((!strlen($this->div) || !strlen($this->executivecd)) && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && ($this->div != $this->field["DIV"] || $this->executivecd != $this->field["EXECUTIVECD"])) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //コードのゼロ埋め
        if (strlen($this->field["EXECUTIVECD"]) < 2) {
            $this->field["EXECUTIVECD"] = sprintf("%02d",$this->field["EXECUTIVECD"]);
        }
        //１レコード取得
        $row = knjj201_2Query::getRow($this->field["DIV"], $this->field["EXECUTIVECD"]);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["NAME"]) > 60 ) {
            $this->setWarning("MSG915", "(役職名称) 60バイトまで");
            return false;
        } elseif (strlen($this->field["ABBV"]) > 15 ) {
            $this->setWarning("MSG915", "(役職略称) 15バイトまで");
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
            $this->div = $this->field["DIV"];
            $this->executivecd = $this->field["EXECUTIVECD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel()
    {
        //キーの更新チェック
        if (!strlen($this->div) || !strlen($this->executivecd)) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->div != $this->field["DIV"] || $this->executivecd != $this->field["EXECUTIVECD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
         $row = knjj201_2Query::getRow($this->div, $this->executivecd);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
        if (knjj201_2Query::IsExisting($this->div, $this->executivecd)){
            $this->setWarning("MSG919", "年度データに登録されています。");
            return;
        }
        knjj201_2Query::getDeleteQuery($this->field);
        $this->div = "";
        $this->executivecd = "";
        $this->setMessage("MSG201");
    }

    function &insert()
    {
        $result = knjj201_2Query::getInsertQuery($this->field);
        return $result;
    }
    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjj201_2Query::getUpdateQuery($this->field);
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
