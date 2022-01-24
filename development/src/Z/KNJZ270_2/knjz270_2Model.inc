<?php

require_once('for_php7.php');


class knjz270_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $sectioncd = "";        //primary

    function knjz270_2Model()
    {
    }

    function init()
    {              
        $this->field = array("SECTIONCD"          =>  VARS::post("SECTIONCD"),
                             "SECTIONNAME"        =>  VARS::post("SECTIONNAME"),
                             "SECTIONABBV"        =>  VARS::post("SECTIONABBV"),
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        $this->cmd             = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->sectioncd);
        }

        $temp = VARS::get("SECTIONCD");
        if (isset($temp)){
            $this->sectioncd      = VARS::get("SECTIONCD");
        }

    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["SECTIONCD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301", "(所属コード)");
            return false;
        }
        if ($this->sectioncd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->sectioncd != $this->field["SECTIONCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }

        //教科コードのゼロ埋め
        if (strlen($this->field["SECTIONCD"]) < 4) {
            $this->field["SECTIONCD"] = sprintf("%04d", $this->field["SECTIONCD"]);
        }
        //１レコード取得
        $row = knjz270_2Query::getRow($this->field["SECTIONCD"]);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(所属コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック    
        if (strlen($this->field["SECTIONNAME"]) > 24 ) {
            $this->setWarning("MSG915", "(所属名) 24バイトまで");
            return false;
        } elseif (strlen($this->field["SECTIONABBV"]) > 12 ) {
            $this->setWarning("MSG915", "(所属略称) 12バイトまで");
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
            $this->sectioncd = $this->field["SECTIONCD"];
            $this->setMessage("MSG201");
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->sectioncd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->sectioncd != $this->field["SECTIONCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
         $row = knjz270_2Query::getRow($this->sectioncd);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;
        }
        if (knjz270_2Query::IsExisting($this->sectioncd)){
            $test = knjz270_2Query::IsPart($this->sectioncd);

            if($test == 1){
                $this->setWarning("MSG919","年度データに登録されています。");
                return;
            }
            if($test == 2){
                $this->setWarning("MSG919","職員年度データに登録されています。");
                return;
            }
        }

        knjz270_2Query::getDeleteQuery($this->field);
        $this->sectioncd = "";
        $this->setMessage("MSG201");
    }

    function &insert()
    {
        $result = knjz270_2Query::getInsertQuery($this->field);
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz270_2Query::getUpdateQuery($this->field);
        return $result;
    }

    function clean()
    {
    }
}
?>
