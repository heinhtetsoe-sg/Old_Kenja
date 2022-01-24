<?php

require_once('for_php7.php');

class knjz403jModel extends Model {
    var $field = array();
    var $cmd;
    var $school_kind;
    var $studyrec_code;

    function knjz403jModel() {}

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->field);
            unset($this->school_kind);
            unset($this->studyrec_code);
        }
    
        $this->field = array("SCHOOL_KIND"          =>  VARS::post("SCHOOL_KIND"),
                             "STUDYREC_CODE"        =>  (VARS::post("STUDYREC_CODE") == "") ? "" : sprintf("%02d", VARS::post("STUDYREC_CODE")),
                             "STUDYREC_CODENAME"    =>  VARS::post("STUDYREC_CODENAME")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHOOL_KIND") != "") {
            $this->school_kind = VARS::get("SCHOOL_KIND");
        }
        if (VARS::get("STUDYREC_CODE") != "") {
            $this->studyrec_code = VARS::get("STUDYREC_CODE");
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if ($this->cmd == "add") {
            if (trim($this->field["STUDYREC_CODE"]) == "") {
                $this->setWarning("MSG301", "(行動の記録コード)");
                return false;
            }
        }
        if ($this->cmd == "update" && $this->studyrec_code == "") {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->cmd == "update" && ($this->studyrec_code != $this->field["STUDYREC_CODE"] || $this->school_kind != $this->field["SCHOOL_KIND"])) {
            $this->setWarning("MSG308");
            return;
        }

        //１レコード取得
        $row = knjz403jQuery::getRow($this->field["SCHOOL_KIND"], $this->field["STUDYREC_CODE"]);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(行動の記録コード)");
            return false;
        }

        //文字数チェック    
        if (strlen($this->field["STUDYREC_CODENAME"]) > 45 ) {
            $this->setWarning("MSG914", "(行動の記録名称) 45バイトまで");
            return false;
        } 

        //文字数チェック    
        if (strlen($this->field["STUDYREC_CODENAME"]) <= 0 ) {
            $this->setWarning("MSG301", "(行動の記録名称)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            //更新
            knjz403jQuery::getUpdateQuery($this->cmd, $this->field);
            $this->school_kind      = $this->field["SCHOOL_KIND"];
            $this->studyrec_code    = $this->field["STUDYREC_CODE"];
            $this->setMessage("MSG201");
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //キーの更新チェック
        if ($this->studyrec_code == "") {
            $this->setWarning("MSG308");
            return false;
        }

        if ($this->studyrec_code != $this->field["STUDYREC_CODE"] || $this->school_kind != $this->field["SCHOOL_KIND"]) {
            $this->setWarning("MSG308");
            return;
        }

         $row = knjz403jQuery::getRow($this->school_kind, $this->studyrec_code);

        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;
        }

        //削除
        knjz403jQuery::getDeleteQuery($this->field);
        $this->school_kind = "";
        $this->studyrec_code = "";
        $this->setMessage("MSG201");
    }
}
?>
