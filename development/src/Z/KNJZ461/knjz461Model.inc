<?php

require_once('for_php7.php');

class knjz461Model extends Model {
    var $field = array();
    var $cmd;
    var $hostcd;

    function knjz461Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->hostcd);
            unset($this->schkind);
        }

        if (VARS::post("SCHKIND")) {
            $this->leftSchkind = VARS::post("SCHKIND");
        }

        $this->field = array("SCHKIND"  =>  VARS::post("SCHKIND"),
                             "HOSTCD"   =>  VARS::post("HOSTCD"),
                             "HOSTNAME" =>  VARS::post("HOSTNAME")
                             );

        $this->cmd = VARS::request("cmd");

        if(VARS::get("SEND_SCHKIND")) {
            $this->schkind = VARS::get("SEND_SCHKIND");
        }
        if(VARS::get("COMBO_SCHKIND")) {
            $this->schkind = VARS::get("COMBO_SCHKIND");
            $this->hostcd = "";
        }
        //校種初期値取得
        $db = Query::dbCheckOut();
        $firstSchkind = $db->getOne(knjz461Query::getSchkind($this));
        Query::dbCheckIn($db);
        $this->schkind = ($this->schkind) ? $this->schkind : $firstSchkind;

        if(VARS::get("HOSTCD")){
            $this->hostcd = VARS::get("HOSTCD");
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {

        //必須チェック
        if (trim($this->field["HOSTCD"]) == "") {
            $this->setWarning("MSG301");
            return false;
        }

        //頭ゼロ埋め
        if (strlen($this->field["HOSTCD"]) <= 2) {
            $this->field["HOSTCD"] = sprintf("%02d", $this->field["HOSTCD"]);
        }

        //キーの更新チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && ($this->hostcd != $this->field["HOSTCD"])) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "delete") && !strlen($this->hostcd)) {
            $this->setWarning("MSG308");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz461Query::checkClubHostMst($this);
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
            if (strlen($this->field["HOSTCD"]) > 2 ) {
                $this->setWarning("MSG915", "(開催地域コード) 2バイトまでです。");
                return false;
            } elseif (strlen($this->field["HOSTNAME"]) > 60 ) {
                $this->setWarning("MSG915", "(開催地域名称) 60バイトまでです。");
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
            knjz461Query::getUpdateQuery($this);
            $this->leftSchkind = $this->schkind;
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        if ($this->validateInput()){
            knjz461Query::getInsertQuery($this);
            $this->hostcd  = $this->field["HOSTCD"];
            $this->leftSchkind = $this->schkind;
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()){
            knjz461Query::getDeleteQuery($this);
            unset($this->special_group_cd);
            unset($this->field);
            $this->leftSchkind = $this->schkind;
            $this->setMessage("MSG201");
        }
    }
}
?>
