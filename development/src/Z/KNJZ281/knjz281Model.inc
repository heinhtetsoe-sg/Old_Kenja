<?php

require_once('for_php7.php');

class knjz281Model extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $jobcd = "";

    function knjz281Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->jobcd);
        }

        $this->field = array("JOBCD"        =>  VARS::post("JOBCD"),
                             "JOBNAME"      =>  VARS::post("JOBNAME"),
                             "BASE_JOBNAME" =>  VARS::post("BASE_JOBNAME"),
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("mode") == "1"){
            unset($this->jobcd);
        }
        $temp = VARS::get("JOBCD");
        if (isset($temp)){
            $this->jobcd = VARS::get("JOBCD");
        }
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (!strlen($this->field["JOBCD"]) && $this->cmd == "add") {
            $this->setWarning("MSG301", "(職名コード)");
            return false;
        }
        if (!strlen($this->jobcd) && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->jobcd != $this->field["JOBCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }

        //コードのゼロ埋め
        if (strlen($this->field["JOBCD"]) < 4) {
            $this->field["JOBCD"] = sprintf("%04d", $this->field["JOBCD"]);
        }

        //１レコード取得
        //教育委員会設定職名コードチェック
        $row = knjz281Query::getRow($this->field["JOBCD"]);
        //学校設定職名コードチェック
        $row_sch_job = knjz281Query::getEdboardJobDat($this->field["JOBCD"], "2");

        //データの存在チェック
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(職名コード)");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row_sch_job)){
            $this->setWarning("MSG918", "(学校登録の職名コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["JOBNAME"]) == 0) {
            $this->setWarning("MSG301","(職名名称)");
            return false;
        } else if (strlen($this->field["JOBNAME"]) > 60 ) {
            $this->setWarning("MSG915", "(職名名称) 60バイトまで");
            return false;
        }
        if (strlen($this->field["BASE_JOBNAME"]) > 60 ) {
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
            $this->jobcd = $this->field["JOBCD"];
            $this->setMessage("MSG201");
        }
    }

    //更新
    function &update() {
        knjz281Query::getUpdateQuery($this->field, $this->cmd);
        return;
    }

    //DELETE
    function &getDeleteModel() {
        //キーの更新チェック
        if (!strlen($this->jobcd)) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->jobcd != $this->field["JOBCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //存在チェック
        $row = knjz281Query::getRow($this->jobcd);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;    
        }
        //学校登録チェック
        $row_sch_job = knjz281Query::getEdboardJobDat($this->jobcd);
        if (is_array($row_sch_job)) {
            $this->setWarning("MSG919", "学校に登録されています。");
            return;
        }

        //削除
        knjz281Query::getDeleteQuery($this->field);
        $this->jobcd = "";
        $this->setMessage("MSG201");
    }
}
?>
