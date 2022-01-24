<?php

require_once('for_php7.php');

class knjz041Model extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $coursecd = "";

    function knjz041Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->coursecd);
        }

        $this->field = array("COURSECD"     =>  VARS::post("COURSECD"),
                             "COURSENAME"   =>  VARS::post("COURSENAME"),
                             "COURSEABBV"   =>  VARS::post("COURSEABBV"),
                             "COURSEENG"    =>  VARS::post("COURSEENG"),
                             "S_PERIODCD"   =>  VARS::post("S_PERIODCD"),
                             "E_PERIODCD"   =>  VARS::post("E_PERIODCD"),
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("mode") == "1"){
            unset($this->coursecd);
        }
        $temp = VARS::get("COURSECD");
        if (isset($temp)){
            $this->coursecd = VARS::get("COURSECD");
        }
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (trim($this->field["COURSECD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301","(課程コード)");
            return false;
        }
        if ($this->coursecd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->coursecd != $this->field["COURSECD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }

        //１レコード取得
        //教育委員会設定課程コードチェック
        $row = knjz041Query::getRow($this->field["COURSECD"]);
        //学校設定課程コードチェック
        $row_sch_course = knjz041Query::getEdboardCourseDat($this->field["COURSECD"], "2");

        //データの存在チェック
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(課程コード)");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row_sch_course)){
            $this->setWarning("MSG918", "(学校登録の課程コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["COURSENAME"]) == 0) {
            $this->setWarning("MSG301","(課程名称)");
            return false;
        } else if (strlen($this->field["COURSENAME"]) > 30 ) {
            $this->setWarning("MSG915", "(課程名称) 30バイトまで");
            return false;
        } else if (strlen($this->field["COURSEABBV"]) > 6 ) {
            $this->setWarning("MSG915", "(課程略称) 6バイトまで");
            return false;
        } else if (strlen($this->field["COURSEENG"]) > 10) {
            $this->setWarning("MSG915", "(課程英字) 10バイトまで");
            return false;
        }

        //開始校時と終了校時の妥当性
        if($this->field["S_PERIODCD"] > $this->field["E_PERIODCD"]) {
            $this->setWarning("MSG901", "開始校時が終了校時を超えています。");
            return false;
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->coursecd = $this->field["COURSECD"];
            $this->setMessage("MSG201");
        }
    }

    //更新
    function &update() {
        knjz041Query::getUpdateQuery($this->field, $this->cmd);
        return;
    }

    //DELETE
    function &getDeleteModel() {
        //キーの更新チェック
        if ($this->coursecd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->coursecd != $this->field["COURSECD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //存在チェック
        $row = knjz041Query::getRow($this->coursecd);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;    
        }
        //学校登録チェック
        $row_sch_course = knjz041Query::getEdboardCourseDat($this->coursecd);
        if (is_array($row_sch_course)) {
            $this->setWarning("MSG919", "学校に登録されています。");
            return;
        }
        //学科マスタ登録チェック
        if (knjz041Query::checkMajormst($this->coursecd)){
            $this->setWarning("MSG919", "学科マスタに登録されています。");
            return;
        }
        //削除
        knjz041Query::getDeleteQuery($this->field);
        $this->coursecd = "";
        $this->setMessage("MSG201");
    }
}
?>
