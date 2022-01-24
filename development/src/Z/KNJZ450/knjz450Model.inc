<?php

require_once('for_php7.php');

class knjz450Model extends Model {
    var $field      = array();
    var $cmd;
    var $qualified_cd;        //primary

    function knjz450Model() {}
    function init() {
        $this->field = array("QUALIFIED_CD"      =>  VARS::post("QUALIFIED_CD"),
                             "QUALIFIED_NAME"    =>  VARS::post("QUALIFIED_NAME"),
                             "QUALIFIED_ABBV"    =>  VARS::post("QUALIFIED_ABBV"),
                             "CONDITION_DIV"     =>  VARS::post("CONDITION_DIV"),
                             "SAKI_FLG"          =>  VARS::post("SAKI_FLG"),
                             "PROMOTER"          =>  VARS::post("PROMOTER"),
                             "MANAGEMENT_FLG"    =>  VARS::post("MANAGEMENT_FLG"),      //学校管理資格フラグ
                             "UPDATED"           =>  VARS::post("UPDATED")
                             );

        $this->cmd       = VARS::request("cmd");
        if (VARS::get("mode") == "1") {
            unset($this->qualified_cd);
        }
        $temp = VARS::get("QUALIFIED_CD");
        if (isset($temp)) {
            $this->qualified_cd  = VARS::get("QUALIFIED_CD");
        }

        //プロパティファイル
        $this->getPropertiesAll();
    }

   //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (trim($this->field["QUALIFIED_CD"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901',"(資格コード)");
            return false;
        }
        if (trim($this->field["QUALIFIED_NAME"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901',"(資格名称)");
            return false;
        }
        if (trim($this->field["QUALIFIED_ABBV"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901',"(資格略称)");
            return false;
        }
        if ($this->qualified_cd  == "" && $this->cmd == "update") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->cmd == "update" && $this->qualified_cd != $this->field["QUALIFIED_CD"]) {
            $this->setWarning('MSG308');
            return;
        }
        //コードのゼロ埋め
        if (strlen($this->field["QUALIFIED_CD"]) < 4) {
            $this->field["QUALIFIED_CD"] = sprintf("%04d", $this->field["QUALIFIED_CD"]);
        }

        //１レコード取得
        $db = Query::dbCheckOut();
        $query = knjz450Query::getQualifiedMst($this->field["QUALIFIED_CD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning('MSG303');
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning('MSG918', "(資格コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning('MSG307');
            return false;
        }
        if (strlen($this->field["QUALIFIED_CD"]) >4 ) {
            $this->setWarning('MSG901', "(資格コード) 4バイトまで");
            return false;
        } elseif (strlen($this->field["QUALIFIED_NAME"])>150) {
            $this->setWarning('MSG901', "(資格名称) 150バイトまで");
            return false;
        } elseif (strlen($this->field["QUALIFIED_ABBV"])>75) {
            $this->setWarning('MSG901', "(資格略称) 75バイトまで");
            return false;
        } elseif (strlen($this->field["PROMOTER"])>150) {
            $this->setWarning('MSG901', "(主催) 150バイトまで");
            return false;
        }
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage('MSG201');
        }
    }
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->qualified_cd = $this->field["QUALIFIED_CD"];
            $this->setMessage('MSG201');
        }
    }

    // 削除用のモデルを返します
    function &getDeleteModel() {
        //キーの更新チェック
        if ($this->qualified_cd == "") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->qualified_cd != $this->field["QUALIFIED_CD"]) {
            $this->setWarning('MSG308');
            return;
        }
        $db = Query::dbCheckOut();
        $query = knjz450Query::getQualifiedMst($this->qualified_cd);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        if (!is_array($row)) {
            $this->setWarning('MSG303');
            return;
        }
        knjz450Query::getDeleteQuery($this->field);
        $this->setMessage('MSG201');
    }

    function &insert() {
        knjz450Query::getInsertQuery($this->field);
    }

    //アップデート時の内部メソッド
    function &update() {
        knjz450Query::getUpdateQuery($this->field);

    }

    function clean() {}
}
?>
