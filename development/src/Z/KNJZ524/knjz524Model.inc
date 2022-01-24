<?php

require_once('for_php7.php');

class knjz524Model extends Model {
    var $field = array();
    var $cmd;
    var $namecd;

    function knjz524Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->namecd);
        }

        $this->field = array("CERTIFNAME_CD"            =>  VARS::post("CERTIFNAME_CD"),
                             "CERTIFNAME" =>  VARS::post("CERTIFNAME")
                             );

        $this->cmd = VARS::request("cmd");

        if(VARS::get("CERTIFNAME_CD")) {
            $this->namecd = VARS::get("CERTIFNAME_CD");
        }
    }

    //入力項目チェック
    function validateInput() {

        //必須チェック
        if (trim($this->field["CERTIFNAME_CD"]) == "") {
            $this->setWarning("MSG301", "　　　　(　受給者証コード　)");
            return false;
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if (trim($this->field["CERTIFNAME"]) == "") {
                $this->setWarning("MSG301", "　　　　　　(　受給者証名称　)");
                return false;
            }
        }

        //頭ゼロ埋め
        if (strlen($this->field["CERTIFNAME_CD"]) <= 2) {
            $this->field["CERTIFNAME_CD"] = sprintf("%02d", $this->field["CERTIFNAME_CD"]);
        }

        //キーの更新チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && ($this->namecd != $this->field["CERTIFNAME_CD"])) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "delete") && !strlen($this->namecd)) {
            $this->setWarning("MSG308");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz524Query::getChallengedCertifMst($this->field["CERTIFNAME_CD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //更新対象がなければエラー
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //追加時 既にレコードがあればエラー
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }

        //文字数チェック
        if ($this->cmd == "add" || $this->cmd == "update") {
            if (strlen($this->field["CERTIFNAME_CD"]) > 2 ) {
                $this->setWarning("MSG915", "(受給者証コード) 2バイトまでです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["CERTIFNAME"], (30 * 2)) > 1) {
                $this->setWarning("MSG915", "(受給者証名称) 全角30文字までです。");
                return false;
            }
        }

        return true;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateModel() {
        if ($this->validateInput()){
            knjz524Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        if ($this->validateInput()){
            knjz524Query::getInsertQuery($this);
            $this->namecd = $this->field["CERTIFNAME_CD"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()){
            knjz524Query::getDeleteQuery($this);
            unset($this->namecd);
            unset($this->field);
            $this->setMessage("MSG201");
        }
    }
}
?>