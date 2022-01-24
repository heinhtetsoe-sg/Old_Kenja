<?php

require_once('for_php7.php');

class knjz462Model extends Model {
    var $field = array();
    var $cmd;
    var $itemcd;
    var $kindcd;

    function knjz462Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");
        if (VARS::get("PROGRAMID")) {
            unset($this->itemcd);
            unset($this->kindcd);
            unset($this->schkind);
        }

        $this->field = array("ITEMCD"       =>  VARS::post("ITEMCD"),
                             "ITEMNAME"     =>  VARS::post("ITEMNAME"),
                             "KINDCD"       =>  VARS::post("KINDCD"),
                             "KINDNAME"     =>  VARS::post("KINDNAME")
                             );

        if(VARS::request("SCHKIND") != ""){
            $this->schkind = VARS::request("SCHKIND");
        }

        //校種初期値（プログラムを開き、左画面は何も触らないで [追加] する際、
        //            $this->schkind がセットされない時があるため（原因不明））
        $db = Query::dbCheckOut();
        $firstSchkind = $db->getOne(knjz462Query::getSchkind($this));
        Query::dbCheckIn($db);
        $this->schkind = ($this->schkind) ? $this->schkind : $firstSchkind;


        if(VARS::get("ITEMCD")){
            $this->itemcd = VARS::get("ITEMCD");
        }
        if(VARS::get("KINDCD")){
            $this->kindcd = VARS::get("KINDCD");
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->cmd == "add" &&
           (trim($this->field["ITEMCD"]) == "" || trim($this->field["KINDCD"]) == "")) {
            $this->setWarning("MSG301");
            return false;
        }

        //頭ゼロ埋め
        if (strlen($this->field["ITEMCD"]) <= 3) {
            $this->field["ITEMCD"] = sprintf("%03d", $this->field["ITEMCD"]);
        }
        if (strlen($this->field["KINDCD"]) <= 3) {
            $this->field["KINDCD"] = sprintf("%03d", $this->field["KINDCD"]);
        }

        //キーの更新チェック
        if ($this->cmd == "update" && ($this->itemcd != $this->field["ITEMCD"] || $this->kindcd != $this->field["KINDCD"])) {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->cmd == "update" && (!strlen($this->itemcd) || !strlen($this->kindcd))) {
            $this->setWarning("MSG308");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $item = $db->getRow(knjz462Query::checkClubItemMst($this), DB_FETCHMODE_ASSOC);
        $kind = $db->getRow(knjz462Query::checkClubItemKindMst($this), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //更新対象がなければエラー
        if ($this->cmd == "update" && !is_array($item) && !is_array($kind)) {
            $this->setWarning("MSG303");
            return false;
        }
        //追加時 既にレコードがあればエラー
        if ($this->cmd == "add" && is_array($kind)){
            $this->setWarning("MSG918");
            return false;
        }

        //文字数チェック
        if (strlen($this->field["ITEMCD"]) > 3 ) {
            $this->setWarning("MSG915", "(種目コード) 3バイトまでです。");
            return false;
        } elseif (strlen($this->field["ITEMNAME"]) > 60 ) {
            $this->setWarning("MSG915", "(種目名称) 60バイトまでです。");
            return false;
        } elseif (strlen($this->field["KINDCD"]) > 6 ) {
            $this->setWarning("MSG915", "(種類コード) 6バイトまでです。");
            return false;
        } elseif (strlen($this->field["KINDNAME"]) > 60 ) {
            $this->setWarning("MSG915", "(種類名称) 60バイトまでです。");
            return false;
        }

        return true;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->itemcd = $this->field["ITEMCD"];
            $this->kindcd = $this->field["KINDCD"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        //キーの更新チェック
        if (!strlen($this->itemcd) || !strlen($this->kindcd)) {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->itemcd != $this->field["ITEMCD"] || $this->kindcd != $this->field["KINDCD"]) {
            $this->setWarning("MSG308");
            return false;
        }
        //１レコード取得
        $db    = Query::dbCheckOut();
        $kind = $db->getRow(knjz462Query::checkClubItemKindMst($this), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        if (!is_array($kind)) {
            $this->setWarning("MSG303");
            return false;
        }

        knjz462Query::getDeleteQuery($this);
        unset($this->itemcd);
        unset($this->kindcd);
        unset($this->field);
        $this->setMessage("MSG201");
    }

    /* INSERT */
    function &insert() {
        $db = Query::dbCheckOut();
        $item = $db->getRow(knjz462Query::checkClubItemMst($this), DB_FETCHMODE_ASSOC);
        $kind = $db->getRow(knjz462Query::checkClubItemKindMst($this), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        if (!is_array($item)) {
            knjz462Query::getInsertItemQuery($this);
        }
        if (!is_array($item) || !is_array($kind)) {
            knjz462Query::getInsertKindQuery($this);
        }
    }

    /* UPDATE */
    function &update() {
        knjz462Query::getUpdateItemQuery($this);
        knjz462Query::getUpdateKindQuery($this);
    }
}
?>
