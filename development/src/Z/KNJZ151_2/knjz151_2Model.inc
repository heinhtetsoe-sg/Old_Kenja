<?php

require_once('for_php7.php');


class knjz151_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $issuecd;        //primary

    /**
     * メンバ変数の初期化
     */
    function knjz151_2Model()
    {
    }
    function init()
    {              
        $this->field = array("ISSUECOMPANYCD"            =>  VARS::post("ISSUECOMPANYCD"),
                             "ISSUECOMPANYNAME"          =>  VARS::post("ISSUECOMPANYNAME"),
                             "ISSUECOMPANYABBV"          =>  VARS::post("ISSUECOMPANYABBV"),
                             "UPDATED"                   =>  VARS::post("UPDATED")
                             );

        $this->cmd       = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->issuecd);
        }
        $temp = VARS::get("ISSUECOMPANYCD");
        if (isset($temp)){
            $this->issuecd  = VARS::get("ISSUECOMPANYCD");
        }
    }

   //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["ISSUECOMPANYCD"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901',"(発行者コード)");
            return false;
        }
        if (trim($this->field["ISSUECOMPANYNAME"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901',"(発行者名)");
            return false;
        }
        if ($this->issuecd  == "" && $this->cmd == "update") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->cmd == "update" && $this->issuecd != $this->field["ISSUECOMPANYCD"]) {
            $this->setWarning('MSG308');
            return;
        }
        //コードのゼロ埋め
        if (strlen($this->field["ISSUECOMPANYCD"]) < 4){
            $this->field["ISSUECOMPANYCD"] = sprintf("%04d", $this->field["ISSUECOMPANYCD"]);
        }

        //１レコード取得
        $row = knjz151_2Query::getRow($this->field["ISSUECOMPANYCD"]);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning('MSG303');
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning('MSG918', "(発行者コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning('MSG307');
            return false;
        }
        if (strlen($this->field["ISSUECOMPANYCD"]) >4 ) {
            $this->setWarning('MSG901', "(発行者コード) 4バイトまで");
            return false;
        } elseif (strlen($this->field["ISSUECOMPANYNAME"])>60){ 
            $this->setWarning('MSG901', "(発行者名) 60バイトまで");
            return false;
        } elseif (strlen($this->field["ISSUECOMPANYABBV"])>15){ 
            $this->setWarning('MSG901', "(発行者略称) 15バイトまで");
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
            $this->setMessage('MSG201');
        }    
    }
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->issuecd = $this->field["ISSUECOMPANYCD"];
            $this->setMessage('MSG201');
        }
    }

    // 削除用のモデルを返します
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->issuecd == "") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->issuecd != $this->field["ISSUECOMPANYCD"]) {
            $this->setWarning('MSG308');
            return;
        }
        $row = knjz151_2Query::getRow($this->issuecd);
        if (!is_array($row)){
            $this->setWarning('MSG303');
            return;
        }
        if (knjz151_2Query::IsExisting($this->issuecd)){
            $this->setWarning('MSG919',"年度データに登録されています。");
            return;
        }
        knjz151_2Query::getDeleteQuery($this->field);
        $this->setMessage('MSG201');
    }
    
    function &insert()
    {
        knjz151_2Query::getInsertQuery($this->field);
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjz151_2Query::getUpdateQuery($this->field);

    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
