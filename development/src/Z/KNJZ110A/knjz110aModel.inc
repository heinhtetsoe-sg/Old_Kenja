<?php

require_once('for_php7.php');

class knjz110aModel extends Model {
    var $field = array();
    var $cmd;
    var $city_cd;
    var $pref_cd;

    function knjz110aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("PREF_CD"      =>  VARS::post("PREF_CD"),
                             "CITY_CD"      =>  VARS::post("CITY_CD"),
                             "CITY_NAME"    =>  VARS::post("CITY_NAME"),
                             "CITY_KANA"    =>  VARS::post("CITY_KANA"),
                             "CITY_FLG1"    =>  VARS::post("CITY_FLG1"),
                             "CITY_FLG2"    =>  VARS::post("CITY_FLG2"),
                             "CITY_FLG3"    =>  VARS::post("CITY_FLG3"),
                             "CITY_FLG4"    =>  VARS::post("CITY_FLG4"),
                             "CITY_FLG5"    =>  VARS::post("CITY_FLG5")
                             );

        $this->cmd          = VARS::request("cmd");
        if (VARS::get("SEND_FLG") == "1"){
            $this->city_cd = VARS::get("CITY_CD");
            $this->pref_cd = VARS::get("PREF_CD");
        }
        if ($this->cmd == "") {
            unset($this->city_cd);
            unset($this->pref_cd);
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->cmd == "add" &&
            (trim($this->field["CITY_CD"]) == "" ||
             trim($this->field["PREF_CD"]) == ""
            )) {
            $this->setWarning("MSG301");
            return false;
        }
        //キーの更新チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && ($this->city_cd != $this->field["CITY_CD"] || $this->pref_cd != $this->field["PREF_CD"])) {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->cmd == "add" && (!strlen($this->field["CITY_CD"]) || !strlen($this->field["PREF_CD"]))) {
            $this->setWarning("MSG308");
            return false;
        }
        //頭ゼロ埋め
        if (strlen($this->field["CITY_CD"]) <= 3) {
            $this->field["SPECIAL_GROUP_CD"] = sprintf("%03d", $this->field["SPECIAL_GROUP_CD"]);
        }
        //アップデートの対象があるかチェック
        $db = Query::dbCheckOut();
        $query = knjz110aQuery::getCityMst_check($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //更新対象がなければエラー
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        } elseif ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        } elseif ($this->cmd == "delete" && !is_array($row)){
            $this->setWarning("MSG303");
            return false;
        }

        //文字数チェック
        if (strlen($this->field["CITY_CD"]) > 3 ) {
            $this->setWarning("MSG915", "(市区町村コード) 3バイトまでです。");
            return false;
        } elseif (strlen($this->field["CITY_NAME"]) > 120) {
            $this->setWarning("MSG915", "(市区町村名称) 120バイトまでです。");
            return false;
        } elseif (strlen($this->field["CITY_KANA"]) > 120) {
            $this->setWarning("MSG915", "(市区町村かな名) 120バイトまでです。");
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
            $this->city_cd = $this->field["CITY_CD"];
            $this->pref_cd = $this->field["PREF_CD"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        knjz110aQuery::getDeleteQuery($this);
        unset($this->city_cd);
        unset($this->pref_cd);
        $this->setMessage("MSG201");
    }

    /* INSERT */
    function &insert() {
        knjz110aQuery::getInsertMstQuery($this);
    }

    /* UPDATE */
    function &update() {
        knjz110aQuery::getUpdateMstQuery($this);
    }
}
?>
