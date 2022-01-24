<?php

require_once('for_php7.php');


class knjz055_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $coursecode;        //primary

    function knjz055_2Model() {
        ;
    }

    function init() {
        $this->field = array("COURSECODE"       =>  VARS::post("COURSECODE"),
                             "COURSECODENAME"   =>  VARS::post("COURSECODENAME"),
                             "COURSECODEABBV1"  =>  VARS::post("COURSECODEABBV1"),
                             "COURSECODEABBV2"  =>  VARS::post("COURSECODEABBV2"),
                             "COURSECODEABBV3"  =>  VARS::post("COURSECODEABBV3"),
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        $this->cmd   = VARS::request("cmd");

        if (VARS::get("mode") == "1") {
            $this->coursecode = "";
        }
        $temp = VARS::get("COURSECODE");
        if (isset($temp)) {
            $this->coursecode      = VARS::get("COURSECODE");
        }
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (trim($this->field["COURSECODE"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301", "(コースコード)");
            return false;
        }

        if ($this->coursecode == "" && $this->cmd == "update") {
            $this->setWarning("MSG308");
            return false;
        }

        if ($this->cmd == "update" && $this->coursecode != $this->field["COURSECODE"]) {
            $this->setWarning("MSG308");
            return;
        }

        //教科コードのゼロ埋め
        if (strlen($this->field["COURSECODE"]) < 4) {
            $this->field["COURSECODE"] = sprintf("%04d", $this->field["COURSECODE"]);
        }

        //１レコード取得
        $row = knjz055_2Query::getRow($this->field["COURSECODE"]);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(コースコード)");
            return false;
        }

        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning("MSG307");
            return false;
        }

        //文字数チェック    
        if (strlen($this->field["COURSECODENAME"]) > 33 ) {
            $this->setWarning("MSG914", "(コース名) 33バイトまで");
            return false;
        }
        if (strlen($this->field["COURSECODEABBV1"]) > 30 ) {
            $this->setWarning("MSG914", "(コース略称) 30バイトまで");
            return false;
        }
        if (strlen($this->field["COURSECODEABBV2"]) > 30 ) {
            $this->setWarning("MSG914", "(コース略称２) 30バイトまで");
            return false;
        }
        if (strlen($this->field["COURSECODEABBV3"]) > 30 ) {
            $this->setWarning("MSG914", "(コース略称３) 30バイトまで");
            return false;
        }

        //文字数チェック
        if (strlen($this->field["COURSECODENAME"]) <= 0 ) {
            $this->setWarning("MSG301", "(コース名)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->coursecode = $this->field["COURSECODE"];
            $this->setMessage("MSG201");
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //キーの更新チェック
        if ($this->coursecode == "") {
            $this->setWarning("MSG308");
            return false;
        }

        if ($this->coursecode != $this->field["COURSECODE"]) {
            $this->setWarning("MSG308");
            return;
        }

         $row = knjz055_2Query::getRow($this->coursecode);

        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;
        }

        if (knjz055_2Query::IsExisting($this->coursecode)) {
            $this->setWarning("MSG919","年度データに登録されています。");
            return;
        }

        knjz055_2Query::getDeleteQuery($this->field);
        $this->coursecode = "";
        $this->setMessage("MSG201");
    }

    function &insert() {
        $result = knjz055_2Query::getInsertQuery($this->field);
    }

    //アップデート時の内部メソッド
    function &update() {
        $result = knjz055_2Query::getUpdateQuery($this->field);
    }

    function clean() {
    }
}
?>
