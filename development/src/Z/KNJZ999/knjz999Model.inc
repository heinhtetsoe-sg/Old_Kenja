<?php

require_once('for_php7.php');

class knjz999Model extends Model {
    var $field = array();
    var $cmd;
    var $stationcd = ""; //primary

    function knjz999Model() {}

    function init() {
        $this->field = array("STATIONCD"     =>  VARS::post("STATIONCD"),
                             "STATIONNAME"   =>  VARS::post("STATIONNAME"),
                             "UPDATED"       =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::get("mode") == "1") {
            unset($this->stationcd);
        }
        $temp = VARS::get("STATIONCD");
        if (isset($temp)) {
            $this->stationcd = VARS::get("STATIONCD");
        }
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (trim($this->field["STATIONCD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301","(駅コード)");
            return false;
        }
        if ($this->stationcd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->stationcd != $this->field["STATIONCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //１レコード取得
        $row = knjz999Query::getRow($this->field["STATIONCD"]);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(駅コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["STATIONNAME"]) == 0) {
            $this->setWarning("MSG301","(駅名称)");
            return false;
        } elseif (strlen($this->field["STATIONNAME"]) > 30 ) {
            $this->setWarning("MSG915", "(駅名称) 30バイトまで");
            return false;
        }
        return true;
    }

    /********/
    /* 更新 */
    /********/
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    /********/
    /* 追加 */
    /********/
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->stationcd = $this->field["STATIONCD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    /********/
    /* 削除 */
    /********/
    function &getDeleteModel() {
        //キーの更新チェック
        if ($this->stationcd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->stationcd != $this->field["STATIONCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        $row = knjz999Query::getRow($this->stationcd);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;
        }
        if (knjz999Query::IsExisting($this->stationcd)) {
            $this->setWarning("MSG919", "年度データに登録されています。");
            return;
        }
          knjz999Query::getDeleteQuery($this->field);
        $this->stationcd = "";
        $this->setMessage("MSG201");
    }

    function &insert() {
        $result = knjz999Query::getInsertQuery($this->field);
        return $result;
    }

    //アップデート時の内部メソッド
    function &update() {
        $result = knjz999Query::getUpdateQuery($this->field);
        return $result;
    }

    function clean() {}
}
?>
