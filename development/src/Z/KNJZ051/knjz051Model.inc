<?php

require_once('for_php7.php');

class knjz051Model extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $coursecd = "";
    var $majorcd = "";

    function knjz051Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->coursecd);
            unset($this->majorcd);
        }

        $this->field = array("COURSECD"     =>  VARS::post("COURSECD"),
                             "MAJORCD"      =>  VARS::post("MAJORCD"),
                             "MAJORNAME"    =>  VARS::post("MAJORNAME"),
                             "MAJORABBV"    =>  VARS::post("MAJORABBV"),
                             "MAJORENG"     =>  VARS::post("MAJORENG"),
                             "MAJORBANKCD"  =>  VARS::post("MAJORBANKCD"),
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        $temp = VARS::get("COURSECD");
        if (isset($temp)){
            $this->coursecd = VARS::get("COURSECD");
        }
        $temp = VARS::get("MAJORCD");
        if (isset($temp)){
            $this->majorcd = VARS::get("MAJORCD");
        }
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if ((!strlen($this->field["MAJORCD"]) || !strlen($this->field["COURSECD"])) && $this->cmd == "add") {
            $this->setWarning("MSG301", "(学科コード及び課程コード)");
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
        //教育委員会設定学科コードチェック
        $row = knjz051Query::getRow($this->field["COURSECD"], $this->field["MAJORCD"]);
        //学校設定学科コードチェック
        $row_sch_course = knjz051Query::getEdboardMajorDat($this->field["COURSECD"], $this->field["MAJORCD"], "2");

        //データの存在チェック
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(学科コード)");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row_sch_course)){
            $this->setWarning("MSG918", "(学校登録の学科コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["MAJORNAME"]) == 0) {
            $this->setWarning("MSG301","(学科名称)");
            return false;
        } else if (strlen($this->field["MAJORNAME"]) > 60 ) {
            $this->setWarning("MSG915", "(学科名称) 60バイトまで");
            return false;
        } else if (strlen($this->field["MAJORABBV"]) > 6 ) {
            $this->setWarning("MSG915", "(学科略称) 6バイトまで");
            return false;
        } else if (strlen($this->field["MAJORENG"]) > 20 ) {
            $this->setWarning("MSG915", "(学科名称英字) 20バイトまで");
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
            $this->majorcd = $this->field["MAJORCD"];
            $this->setMessage("MSG201");
        }
    }

    //更新
    function &update() {
        knjz051Query::getUpdateQuery($this->field, $this->cmd);
        return;
    }

    //DELETE
    function &getDeleteModel() {
        //キーの更新チェック
        if (!strlen($this->coursecd) || !strlen($this->majorcd)) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->majorcd != $this->field["MAJORCD"] || $this->coursecd != $this->field["COURSECD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //存在チェック
        $row = knjz051Query::getRow($this->coursecd, $this->majorcd);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;    
        }
        //学校登録チェック
        $row_sch_course = knjz051Query::getEdboardMajorDat($this->coursecd, $this->majorcd);
        if (is_array($row_sch_course)) {
            $this->setWarning("MSG919", "学校に登録されています。");
            return;
        }

        //削除
        knjz051Query::getDeleteQuery($this->field);
        $this->coursecd = "";
        $this->setMessage("MSG201");
    }
}
?>
