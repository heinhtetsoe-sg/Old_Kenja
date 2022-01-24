<?php

require_once('for_php7.php');

class knjz241Model extends Model {
    var $field      = array();
    var $cmd;
    var $questioncd = "";        //primary

    function knjz241Model()
    {
    }

    function init()
    {              
        $this->field = array("QUESTIONCD"   =>  VARS::post("QUESTIONCD"),
                             "CONTENTS"     =>  VARS::post("CONTENTS"),
                             "SORT"         =>  VARS::post("SORT"),
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd          = VARS::request("cmd");
        if (VARS::get("PROGRAMID") != "") {
            unset($this->questioncd);
        }
        $temp = VARS::get("QUESTIONCD");
        if (isset($temp)){
            $this->questioncd = VARS::get("QUESTIONCD");
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["QUESTIONCD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301","(コード)");
            return false;
        }
        if ($this->questioncd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->questioncd != $this->field["QUESTIONCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //コードのゼロ埋め
        if (strlen($this->field["QUESTIONCD"]) < 2) {
            $this->field["QUESTIONCD"] = sprintf("%02d",$this->field["QUESTIONCD"]);
        }
        //１レコード取得
        $row = knjz241Query::getRow($this->field["QUESTIONCD"]);
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
        if (strlen($this->field["CONTENTS"]) == 0) {
            $this->setWarning("MSG301","(質問)");
            return false;
        } elseif (strlen($this->field["CONTENTS"]) > 78 ) {
            $this->setWarning("MSG915", "(質問) 78バイトまで");
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
            $this->questioncd = $this->field["QUESTIONCD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->questioncd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->questioncd != $this->field["QUESTIONCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
         $row = knjz241Query::getRow($this->questioncd);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
        knjz241Query::getDeleteQuery($this->field);
        $this->questioncd = "";
        $this->setMessage("MSG201");
    }

    function &insert()
    {
        $result = knjz241Query::getInsertQuery($this->field);
        return $result;
    }
    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz241Query::getUpdateQuery($this->field);
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
