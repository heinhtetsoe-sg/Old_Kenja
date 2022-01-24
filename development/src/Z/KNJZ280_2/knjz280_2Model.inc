<?php

require_once('for_php7.php');


class knjz280_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $jobcd;        //primary

    function knjz280_2Model()
    {
        ;
    }

    function init()
    {
        $this->field = array("JOBCD"        =>  VARS::post("JOBCD"),
                             "JOBNAME"      =>  VARS::post("JOBNAME"),
                             "BASE_JOBNAME" =>  VARS::post("BASE_JOBNAME"),
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd   = VARS::request("cmd");

        if (VARS::get("mode") == "1"){
            $this->jobcd = "";
        }
        $temp = VARS::get("JOBCD");
        if (isset($temp)){
            $this->jobcd      = VARS::get("JOBCD");
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["JOBCD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301", "(職名コード)");
            return false;
        }

        if ($this->jobcd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308");
            return false;
        }

        if ($this->cmd == "update" && $this->jobcd != $this->field["JOBCD"]) {
            $this->setWarning("MSG308");
            return;
        }

        //教科コードのゼロ埋め
        if (strlen($this->field["JOBCD"]) < 4) {
            $this->field["JOBCD"] = sprintf("%04d", $this->field["JOBCD"]);
        }

        //１レコード取得
        $row = knjz280_2Query::getRow($this->field["JOBCD"]);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(職名コード)");
            return false;
        }

        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }

        //文字数チェック    
        if (strlen($this->field["JOBNAME"]) > 60 ) {
            $this->setWarning("MSG914", "(職名名) 60バイトまで");
            return false;
        } 
        if (strlen($this->field["BASE_JOBNAME"]) > 60 ) {
            $this->setWarning("MSG915", "(学校基本調査名称) 60バイトまで");
            return false;
        }

        //文字数チェック    
        if (strlen($this->field["JOBNAME"]) <= 0 ) {
            $this->setWarning("MSG301", "(職名名)");
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
            $this->jobcd = $this->field["JOBCD"];
            $this->setMessage("MSG201");
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->jobcd == "") {
            $this->setWarning("MSG308");
            return false;
        }

        if ($this->jobcd != $this->field["JOBCD"]) {
            $this->setWarning("MSG308");
            return;
        }

         $row = knjz280_2Query::getRow($this->jobcd);

        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;
        }

        if (knjz280_2Query::IsExisting($this->jobcd)){
            $this->setWarning("MSG919","年度データに登録されています。");
            return;
        }

        knjz280_2Query::getDeleteQuery($this->field);
        $this->jobcd = "";
        $this->setMessage("MSG201");
    }

    function &insert()
    {
        $result = knjz280_2Query::getInsertQuery($this->field);
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz280_2Query::getUpdateQuery($this->field);
    }

    function clean()
    {
    }
}
?>
