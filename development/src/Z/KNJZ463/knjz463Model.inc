<?php

require_once('for_php7.php');

class knjz463Model extends Model {
    var $field = array();
    var $cmd;
    var $recordcd;

    function knjz463Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->recordcd);
            unset($this->schkind);
        }

        if (VARS::post("SCHKIND")) {
            $this->leftSchkind = VARS::post("SCHKIND");
        }

        $this->field = array("SCHKIND"      =>  VARS::post("SCHKIND"),
                             "RECORDCD"     => VARS::post("RECORDCD"),
                             "RECORDNAME"   => VARS::post("RECORDNAME")
                             );

        if(VARS::get("SEND_SCHKIND")) {
            $this->schkind = VARS::get("SEND_SCHKIND");
        }
        if(VARS::get("COMBO_SCHKIND")) {
            $this->schkind  = VARS::get("COMBO_SCHKIND");
            $this->recordcd = "";
        }
        //校種初期値取得
        $db = Query::dbCheckOut();
        $firstSchkind = $db->getOne(knjz463Query::getSchkind($this));
        Query::dbCheckIn($db);
        $this->schkind = ($this->schkind) ? $this->schkind : $firstSchkind;

        $this->cmd = VARS::request("cmd");

        if(VARS::get("RECORDCD")){
            $this->recordcd = VARS::get("RECORDCD");
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {

        //必須チェック
        if (trim($this->field["RECORDCD"]) == "") {
            $this->setWarning("MSG301");
            return false;
        }

        //頭ゼロ埋め
        if (strlen($this->field["RECORDCD"]) <= 3) {
            $this->field["RECORDCD"] = sprintf("%03d", $this->field["RECORDCD"]);
        }

        //キーの更新チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && ($this->recordcd != $this->field["RECORDCD"])) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "delete") && !strlen($this->recordcd)) {
            $this->setWarning("MSG308");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz463Query::checkClubRecordMst($this);
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
            if (strlen($this->field["RECORDCD"]) > 3 ) {
                $this->setWarning("MSG915", "(成績コード) 3バイトまでです。");
                return false;
            } elseif (strlen($this->field["RECORDNAME"]) > 60 ) {
                $this->setWarning("MSG915", "(成績名称) 60バイトまでです。");
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
            knjz463Query::getUpdateQuery($this);
            $this->leftSchkind = $this->schkind;
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        if ($this->validateInput()){
            knjz463Query::getInsertQuery($this);
            $this->recordcd = $this->field["RECORDCD"];
            $this->leftSchkind = $this->schkind;
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()){
            knjz463Query::getDeleteQuery($this);
            unset($this->special_group_cd);
            unset($this->field);
            $this->leftSchkind = $this->schkind;
            $this->setMessage("MSG201");
        }
    }
}
?>
