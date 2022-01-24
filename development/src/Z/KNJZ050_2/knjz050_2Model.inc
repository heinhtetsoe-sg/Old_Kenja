<?php

require_once('for_php7.php');


class knjz050_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $majorcd = "";        //primary
    var $coursecd = "";

    function knjz050_2Model()
    {
    }

    function init()
    {
        $this->field = array("COURSECD"         =>  VARS::post("COURSECD"),
                             "MAJORCD"          =>  VARS::post("MAJORCD"),
                             "MAJORNAME"        =>  VARS::post("MAJORNAME"),
                             "MAJORNAME2"       =>  VARS::post("MAJORNAME2"),
                             "MAJORABBV"        =>  VARS::post("MAJORABBV"),
                             "MAJORENG"         =>  VARS::post("MAJORENG"),
                             "MAJORBANKCD"      =>  VARS::post("MAJORBANKCD"),
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        $this->cmd         = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->coursecd);
            unset($this->majorcd);
        }
        $temp = VARS::get("COURSECD");
        if (isset($temp)){
            $this->coursecd      = VARS::get("COURSECD");
        }
        $temp = VARS::get("MAJORCD");
        if (isset($temp)){
            $this->majorcd      = VARS::get("MAJORCD");
        }

        $db = Query::dbCheckOut();
        $query = knjz050_2Query::getMajorengLength();
        $this->majorengLength = $db->getOne($query);
        Query::dbCheckIn($db);

    }
    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if ((!strlen($this->field["MAJORCD"]) || !strlen($this->field["COURSECD"])) && $this->cmd == "add") {
            $this->setWarning("MSG301", "(学科コード及び課程コード)");
            return false;
        }
        if (!strlen($this->field["MAJORNAME"]) && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning("MSG301","(学科名称)");
            return false;
        }
        if ((!strlen($this->coursecd) || !strlen($this->majorcd)) && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && ($this->majorcd != $this->field["MAJORCD"] || $this->coursecd != $this->field["COURSECD"])) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //コードのゼロ埋め
        if (strlen($this->field["MAJORCD"]) < 3) {
            $this->field["MAJORCD"] = sprintf("%03d", $this->field["MAJORCD"]);
        }
        if (strlen($this->field["MAJORBANKCD"]) == 1){
            $this->field["MAJORBANKCD"] = sprintf("%02d", $this->field["MAJORBANKCD"]);
        }
        //１レコード取得
        $row = knjz050_2Query::getRow($this->field["MAJORCD"], $this->field["COURSECD"]);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(学科コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["MAJORNAME"]) > 60 ) {
            $this->setWarning("MSG915", "(学科名称) 60バイトまで");
            return false;
        } elseif (strlen($this->field["MAJORNAME2"]) > 60 ) {
            $this->setWarning("MSG915", "(表示用名称) 60バイトまで");
            return false;
        } elseif (strlen($this->field["MAJORABBV"]) > 6 ) {
            $this->setWarning("MSG915", "(学科略称) 6バイトまで");
            return false;
        } elseif (strlen($this->field["MAJORENG"]) > ($this->majorengLength <= 20 ? 20 : 45)) {
            $this->setWarning("MSG915", "(学科名称英字) ".($this->majorengLength <= 20 ? 20 : 45)."バイトまで");
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
            $this->coursecd = $this->field["COURSECD"];
            $this->majorcd  = $this->field["MAJORCD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //キーの更新チェック
        if (!strlen($this->coursecd) || !strlen($this->majorcd)) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->majorcd != $this->field["MAJORCD"] || $this->coursecd != $this->field["COURSECD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
         $row = knjz050_2Query::getRow($this->field["MAJORCD"], $this->field["COURSECD"]);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
        if (knjz050_2Query::IsExisting($this->field["MAJORCD"], $this->field["COURSECD"])){
            $this->setWarning("MSG919","年度データに登録されています。");
            return;
        }
        knjz050_2Query::getDeleteQuery($this->field);
        $this->coursecd = "";
        $this->majorcd = "";
        $this->setMessage("MSG201");
    }

    function &insert()
    {
        $result = knjz050_2Query::getInsertQuery($this->field);
    }
    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz050_2Query::getUpdateQuery($this->field);
    }
    //変数をクリアします
    function clean()
    {
    }
}
?>
