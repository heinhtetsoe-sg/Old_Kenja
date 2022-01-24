<?php

require_once('for_php7.php');

class knjz900Model extends Model {
    var $field      = array();
    var $cmd;
    var $prg_id;
    var $name;
    var $menuid;
    var $value;

    function knjz900Model() {}

    function init() {
        common::GetControlMaster_Fnc($this->control);
        $this->cmd = VARS::request("cmd");

        if(VARS::get("PROGRAMID")) {
           $this->prg_id = "";
           $this->name = "";
           $this->menuid = "";
           $this->value = "";
        }

        $this->field = array("MENUID"       =>  VARS::post("MENUID"),
                             "PROGRAMID"    =>  VARS::post("PROGRAMID"),
                             "NAME"         =>  VARS::post("NAME"),
                             "VALUE"        =>  VARS::post("VALUE"),
                             "REMARK"       =>  VARS::post("REMARK"),
                             "INPUT"        =>  VARS::post("INPUT")
                             );

        if (VARS::get("PRG_ID")) {
            $this->prg_id   = VARS::get("PRG_ID");
        }
        if (VARS::get("NAME")) {
            $this->name     = VARS::get("NAME");
            $this->value    = VARS::get("VALUE");
        }
        if (VARS::get("MENUID")) {
            $this->menuid   = VARS::get("MENUID");
        }

        $this->selectdata        = VARS::post("selectdata");
    }

    //入力項目チェック
    function validateInput() {

        if (trim($this->field["MENUID"]) == "" && trim($this->field["PROGRAMID"]) == "") {
            $this->setWarning("MSG901", "(プログラム)");
            return false;
        }
        if (trim($this->field["NAME"]) == "") {
            $this->setWarning("MSG901", "(パラメータ)");
            return false;
        }

        if ($this->cmd=="update" || $this->cmd=="delete") {
            //リストより選択していない
            if ($this->menuid == "" && $this->prg_id == "") {
                $this->setWarning("MSG308");
                return false;
            }
            //キー項目を変更した場合
            if(($this->field["INPUT"] == "1") && ($this->menuid != $this->field["MENUID"] || $this->name != $this->field["NAME"])) {
                $this->setWarning("MSG308");
                return false;
            } else if(($this->field["INPUT"] == "2") && ($this->prg_id != $this->field["PROGRAMID"] || $this->name != $this->field["NAME"])) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //重複チェック
        if ($this->cmd == "insert") {
            $row = knjz900Query::chkDuplicationData($this);
            if (is_array($row)) {
                $this->setWarning("MSG918");
                return false;
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->delete();
        }
        return;
    }

    //DELETE時の内部メソッド
    function &delete() {
        $prg_id = ($this->field["INPUT"] == "1") ? knjz900Query::getProgramID($this) : $this->field["PROGRAMID"];
        $result = knjz900Query::getDeleteQuery($this, $prg_id);
        if ( !isset($this->warning)) {
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }

    //INSERT・UPDATE時の内部メソッド
    function &insert() {
        $result = knjz900Query::getInsertQuery($this);
        if (!isset($this->warning)) {
            $this->menuid = $this->field["MENUID"];
            $this->prg_id = $result;
            $this->name = $this->field["NAME"];
            $this->setMessage("MSG201");
        }
        return;
    }

    /**************/
    /*  一括処理  */
    /**************/
    //編集完了用のモデルを返します
    function &getUpdateSubModel1() {
        //更新
        knjz900Query::getUpdateSubQuery1($this);
        if (!isset($this->warning)) {
            $this->name = $this->field["NAME"];
            $this->value = $this->field["VALUE"];
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
