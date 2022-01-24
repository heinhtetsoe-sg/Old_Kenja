<?php

require_once('for_php7.php');

class knjz336Model extends Model {
    var $field      = array();
    var $cmd;

    function knjz336Model() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->systemId);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd = VARS::request("cmd");

        $this->field = array("SYSTEMID"                 =>  VARS::post("SYSTEMID"),             //システムＩＤ
                             "SYSTEM_NAME"              =>  VARS::post("SYSTEM_NAME"),          //システム名称
                             "SYSTEM_NAME_ABBV"         =>  VARS::post("SYSTEM_NAME_ABBV"),     //システム名略称
                             "SHOWORDER"                =>  VARS::post("SHOWORDER")             //表示順
                             );

        $temp = VARS::get("SYSTEMID");
        if (isset($temp)) {
            $this->systemId = VARS::get("SYSTEMID");
        }
    }

    //入力項目チェック
    function validateInput() {
        //コードのゼロ埋め
        $this->field["SYSTEMID"]  = sprintf("%08d", $this->field["SYSTEMID"]);

        //キーのチェック
        if (strlen($this->field["SYSTEMID"]) == 0 && $this->cmd == "add") {
            $this->setWarning("MSG301","(システムＩＤ)");
            return false;
        }
        if (strlen($this->field["SYSTEM_NAME"]) == 0 && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning("MSG301","(システム名称)");
            return false;
        }
        if (strlen($this->field["SHOWORDER"]) == 0 && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning("MSG301","(表示順)");
            return false;
        }

        if (($this->cmd == "update" || $this->cmd == "delete") && ($this->systemId != $this->field["SYSTEMID"] || $this->systemId == "")) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }

        //１レコード取得
        $query = knjz336Query::getRow($this->field["SYSTEMID"]);
        $db = Query::dbCheckOut(); //DB接続
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);     //DB切断
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "システムＩＤ");
            return false;
        }
        //文字数チェック
        if ($this->cmd != "delete") {
            $checkStrLen["SYSTEM_NAME"]      = array("LEN" => 120, "MESAGE" => "システム名称");
            $checkStrLen["SYSTEM_NAME_ABBV"] = array("LEN" => 120, "MESAGE" => "システム名略称");
            foreach ($checkStrLen as $key => $val) {
                if (strlen($this->field[$key]) > $val["LEN"] ) {
                    $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                    return false;
                }
            }
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
    //アップデート時の内部メソッド
    function &update() {
        $result = knjz336Query::getUpdateQuery($this, $this->field);
        return $result;
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->systemId = $this->field["SYSTEMID"];
            $this->setMessage("MSG201");
        }
        return;
    }
    //インサート
    function &insert() {
        $result = knjz336Query::getInsertQuery($this, $this->field);
        return $result;
    }

    //DELETE
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjz336Query::getDeleteQuery($this, $this->field);
            $this->setMessage("MSG201");

            $this->field = array();
        }
    }
    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
