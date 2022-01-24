<?php

require_once('for_php7.php');

class knjj510Model extends Model {
    var $field = array();
    var $cmd;
    var $itemcd;

    function knjj510Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->itemcd);
        }

        $this->field = array("ITEMCD"       => VARS::post("ITEMCD"),        //種目コード
                             "ITEMNAME"     => VARS::post("ITEMNAME"),      //種目名称
                             "ITEMABBV"     => VARS::post("ITEMABBV"),      //種目略称
                             "UNIT"         => VARS::post("UNIT"),          //単位
                             "SHOWORDER"    => VARS::post("SHOWORDER")      //通知表への印字順
                             );

        $this->cmd = VARS::request("cmd");

        if(VARS::get("ITEMCD")){
            $this->itemcd = VARS::get("ITEMCD");
        }
    }

    //入力項目チェック
    function validateInput() {

        //必須チェック
        if (trim($this->field["ITEMCD"]) == "") {
            $this->setWarning("MSG301","(種目コード)");
            return false;
        }
        if (trim($this->field["ITEMNAME"]) == "") {
            $this->setWarning("MSG301","(種目名称)");
            return false;
        }

        //頭ゼロ埋め
        if (strlen($this->field["ITEMCD"]) <= 3) {
            $this->field["ITEMCD"] = sprintf("%03d", $this->field["ITEMCD"]);
        }

        //キーの更新チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && ($this->itemcd != $this->field["ITEMCD"])) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "delete") && !strlen($this->itemcd)) {
            $this->setWarning("MSG308");
            return false;
        }

        //入力範囲チェック
        if (trim($this->field["ITEMCD"]) > 899) {
            $this->setWarning("MSG914","(種目コード：000～899)");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjj510Query::getSportsItemMst($this->field["ITEMCD"]);
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
            if (strlen($this->field["ITEMCD"]) > 3 ) {
                $this->setWarning("MSG915", "(種目コード) 3バイトまでです。");
                return false;
            } elseif (strlen($this->field["ITEMNAME"]) > 30 ) {
                $this->setWarning("MSG915", "(種目名称) 30バイトまでです。");
                return false;
            } elseif (strlen($this->field["ITEMABBV"]) > 30 ) {
                $this->setWarning("MSG915", "(種目名称) 30バイトまでです。");
                return false;
            } elseif (strlen($this->field["UNIT"]) > 9 ) {
                $this->setWarning("MSG915", "(単位) 9バイトまでです。");
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
            knjj510Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        if ($this->validateInput()){
            knjj510Query::getInsertQuery($this);
            $this->itemcd = $this->field["ITEMCD"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()){
            knjj510Query::getDeleteQuery($this);
            unset($this->itemcd);
            unset($this->field);
            $this->setMessage("MSG201");
        }
    }
}
?>
