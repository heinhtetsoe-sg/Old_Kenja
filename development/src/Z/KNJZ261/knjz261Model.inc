<?php

require_once('for_php7.php');

class knjz261Model extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $dutysharecd = "";

    function knjz261Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->dutysharecd);
        }

        $this->field = array("DUTYSHARECD"      =>  VARS::post("DUTYSHARECD"),
                             "SHARENAME"        =>  VARS::post("SHARENAME"),
                             "BASE_SHARENAME"   =>  VARS::post("BASE_SHARENAME"),
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        $temp = VARS::get("DUTYSHARECD");
        if (isset($temp)){
            $this->dutysharecd = VARS::get("DUTYSHARECD");
        }
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (!strlen($this->field["DUTYSHARECD"]) && $this->cmd == "add") {
            $this->setWarning("MSG301", "(校務分掌部コード)");
            return false;
        }
        if (!strlen($this->dutysharecd) && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->dutysharecd != $this->field["DUTYSHARECD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }

        //コードのゼロ埋め
        if (strlen($this->field["DUTYSHARECD"]) < 4) {
            $this->field["DUTYSHARECD"] = sprintf("%04d", $this->field["DUTYSHARECD"]);
        }

        //１レコード取得
        //教育委員会設定校務分掌部コードチェック
        $row = knjz261Query::getRow($this->field["DUTYSHARECD"]);
        //学校設定校務分掌部コードチェック
        $row_sch_dutyshare = knjz261Query::getEdboardDutyshareDat($this->field["DUTYSHARECD"], "2");

        //データの存在チェック
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(校務分掌部コード)");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row_sch_dutyshare)){
            $this->setWarning("MSG918", "(学校登録の校務分掌部コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["SHARENAME"]) == 0) {
            $this->setWarning("MSG301","(校務分掌部名称)");
            return false;
        } else if (strlen($this->field["SHARENAME"]) > 24 ) {
            $this->setWarning("MSG915", "(校務分掌部名称) 24バイトまで");
            return false;
        }
        if (strlen($this->field["BASE_SHARENAME"]) > 24 ) {
            $this->setWarning("MSG915", "(学校基本調査名称) 24バイトまで");
            return false;
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->dutysharecd = $this->field["DUTYSHARECD"];
            $this->setMessage("MSG201");
        }
    }

    //更新
    function &update() {
        knjz261Query::getUpdateQuery($this->field, $this->cmd);
        return;
    }

    //DELETE
    function &getDeleteModel() {
        //キーの更新チェック
        if (!strlen($this->dutysharecd)) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->dutysharecd != $this->field["DUTYSHARECD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //存在チェック
        $row = knjz261Query::getRow($this->dutysharecd);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;    
        }
        //学校登録チェック
        $row_sch_dutyshare = knjz261Query::getEdboardDutyshareDat($this->dutysharecd);
        if (is_array($row_sch_dutyshare)) {
            $this->setWarning("MSG919", "学校に登録されています。");
            return;
        }

        //削除
        knjz261Query::getDeleteQuery($this->field);
        $this->dutysharecd = "";
        $this->setMessage("MSG201");
    }
}
?>
