<?php

require_once('for_php7.php');

class knjz094Model extends Model {
    var $field      = array();
    var $cmd;
    var $districtcd = "";        //primary

    function knjz094Model() {
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->districtcd);
        }

        //コードのゼロ埋め
        if (VARS::post("DISTRICTCD") != "") {
             $zeroDistrictcd = sprintf("%05d",VARS::post("DISTRICTCD"));
        }
        $this->field = array("DISTRICTCD"           =>  $zeroDistrictcd,
                             "DISTRICT_NAME"        =>  VARS::post("DISTRICT_NAME"),
                             "DISTRICT_NAME_ABBV"   =>  VARS::post("DISTRICT_NAME_ABBV")
                             );

        $this->cmd          = VARS::request("cmd");

        $temp = VARS::get("DISTRICTCD");
        if (isset($temp)) {
            $this->districtcd = VARS::get("DISTRICTCD");
        }
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (trim($this->field["DISTRICTCD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301","(所在地コード)");
            return false;
        }
        if ($this->districtcd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->districtcd != $this->field["DISTRICTCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //１レコード取得
        $db = Query::dbCheckOut();
        $row = $db->getRow(knjz094Query::getRow($this->field["DISTRICTCD"]), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(所在地コード)");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["DISTRICT_NAME"]) == 0) {
            $this->setWarning("MSG301","(所在地名称)");
            return false;
        } elseif (strlen($this->field["DISTRICT_NAME"]) > 75 ) {
            $this->setWarning("MSG915", "(所在地名称) 75バイトまで");
            return false;
        } elseif (strlen($this->field["DISTRICT_NAME_ABBV"]) > 75 ) {
            $this->setWarning("MSG915", "(所在地略称) 75バイトまで");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }    
    }
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->districtcd = $this->field["DISTRICTCD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel() {
        //キーの更新チェック
        if ($this->districtcd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->districtcd != $this->field["DISTRICTCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        $db = Query::dbCheckOut();
        $row = $db->getRow(knjz094Query::getRow($this->districtcd), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;    
        }

        knjz094Query::getDeleteQuery($this->field);
        $this->districtcd = "";
        $this->setMessage("MSG201");
    }

    function &insert() {
        $result = knjz094Query::getInsertQuery($this->field);
        return $result;
    }
    //アップデート時の内部メソッド
    function &update() {
        $result = knjz094Query::getUpdateQuery($this->field);
        return $result;
    }
    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
