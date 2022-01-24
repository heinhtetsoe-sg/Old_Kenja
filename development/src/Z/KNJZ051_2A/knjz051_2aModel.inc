<?php

require_once('for_php7.php');

class knjz051_2aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $coursecd = "";
    var $majorcd = "";

    function knjz051_2aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("COURSECD"     =>  VARS::post("COURSECD"),
                             "MAJORCD"      =>  VARS::post("MAJORCD"),
                             "MAJORNAME"    =>  VARS::post("MAJORNAME"),
                             "MAJORNAME2"   =>  VARS::post("MAJORNAME2"),
                             "MAJORABBV"    =>  VARS::post("MAJORABBV"),
                             "MAJORENG"     =>  VARS::post("MAJORENG"),
                             "MAJORBANKCD"  =>  VARS::post("MAJORBANKCD"),
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("mode") == "1"){
            unset($this->coursecd);
            unset($this->majorcd);
        }
        $temp = VARS::get("COURSECD");
        if (isset($temp)){
            $this->coursecd = VARS::get("COURSECD");
        }
        $temp = VARS::get("MAJORCD");
        if (isset($temp)){
            $this->majorcd = VARS::get("MAJORCD");
        }

        $db = Query::dbCheckOut();
        $query = knjz051_2aQuery::getMajorengLength();
        $this->majorengLength = $db->getOne($query);
        Query::dbCheckIn($db);

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
        //学校DB
        $row = knjz051_2aQuery::getRow($this->field["COURSECD"], $this->field["MAJORCD"]);
        //教育委員会DB
        $row_edboard = knjz051_2aQuery::getRow($this->field["COURSECD"], $this->field["MAJORCD"], "edboard");

        //データの存在チェック
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(学科コード)");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row_edboard)){
            $this->setWarning("MSG918", "(教育委員会登録の学科コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
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
        } else if (strlen($this->field["MAJORNAME2"]) > 60 ) {
            $this->setWarning("MSG915", "(表示用名称) 60バイトまで");
            return false;
        } else if (strlen($this->field["MAJORABBV"]) > 6 ) {
            $this->setWarning("MSG915", "(学科略称) 6バイトまで");
            return false;
        } else if (strlen($this->field["MAJORENG"]) > ($this->majorengLength <= 20 ? 20 : 45)) {
            $this->setWarning("MSG915", "(学科名称英字) ".($this->majorengLength <= 20 ? 20 : 45)."バイトまで");
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
        knjz051_2aQuery::getUpdateQuery($this->field, $this->cmd);
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
        $row = knjz051_2aQuery::getRow($this->coursecd, $this->majorcd);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;    
        }
        if (knjz051_2aQuery::IsExisting($this->coursecd, $this->majorcd)) {
            $this->setWarning("MSG919", "年度データに登録されています。");
            return;
        }

        //削除
        knjz051_2aQuery::getDeleteQuery($this->field);
        $this->coursecd = "";
        $this->setMessage("MSG201");
    }
}
?>
