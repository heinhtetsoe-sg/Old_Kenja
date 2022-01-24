<?php

require_once('for_php7.php');

class knjz286Model extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $positioncd = "";

    function knjz286Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->positioncd);
        }

        $this->field = array("POSITIONCD"           =>  VARS::post("POSITIONCD"),
                             "POSITIONNAME"         =>  VARS::post("POSITIONNAME"),
                             "BASE_POSITIONNAME"    =>  VARS::post("BASE_POSITIONNAME"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("mode") == "1"){
            unset($this->positioncd);
        }
        $temp = VARS::get("POSITIONCD");
        if (isset($temp)){
            $this->positioncd = VARS::get("POSITIONCD");
        }
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (!strlen($this->field["POSITIONCD"]) && $this->cmd == "add") {
            $this->setWarning("MSG301", "(教務主任等コード)");
            return false;
        }
        if (!strlen($this->positioncd) && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->positioncd != $this->field["POSITIONCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }

        //コードのゼロ埋め
        if (strlen($this->field["POSITIONCD"]) < 4) {
            $this->field["POSITIONCD"] = sprintf("%04d", $this->field["POSITIONCD"]);
        }

        //１レコード取得
        //教育委員会設定教務主任等コードチェック
        $row = knjz286Query::getRow($this->field["POSITIONCD"]);
        //学校設定教務主任等コードチェック
        $row_sch_position = knjz286Query::getEdboardPositionDat($this->field["POSITIONCD"], "2");

        //データの存在チェック
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(教務主任等コード)");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row_sch_position)){
            $this->setWarning("MSG918", "(学校登録の教務主任等コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["POSITIONNAME"]) == 0) {
            $this->setWarning("MSG301","(教務主任等名称)");
            return false;
        } else if (strlen($this->field["POSITIONNAME"]) > 60 ) {
            $this->setWarning("MSG915", "(教務主任等名称) 60バイトまで");
            return false;
        }
        if (strlen($this->field["BASE_POSITIONNAME"]) > 60 ) {
            $this->setWarning("MSG915", "(学校基本調査名称) 60バイトまで");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->positioncd = $this->field["POSITIONCD"];
            $this->setMessage("MSG201");
        }
    }

    //更新
    function &update() {
        knjz286Query::getUpdateQuery($this->field, $this->cmd);
        return;
    }

    //DELETE
    function &getDeleteModel() {
        //キーの更新チェック
        if (!strlen($this->positioncd)) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->positioncd != $this->field["POSITIONCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //存在チェック
        $row = knjz286Query::getRow($this->positioncd);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;    
        }
        //学校登録チェック
        $row_sch_position = knjz286Query::getEdboardPositionDat($this->positioncd);
        if (is_array($row_sch_position)) {
            $this->setWarning("MSG919", "学校に登録されています。");
            return;
        }

        //削除
        knjz286Query::getDeleteQuery($this->field);
        $this->positioncd = "";
        $this->setMessage("MSG201");
    }
}
?>
