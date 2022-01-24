<?php

require_once('for_php7.php');

class knjz056Model extends Model {
    var $field      = array();
    var $cmd;
    var $coursecd;        //primary
    var $majorcd;         //primary
    var $categorycd;

    function knjz056Model() {}
    function init() {
        $this->field = array("COURSECD"      =>  VARS::post("COURSECD"),
                             "MAJORCD"       =>  VARS::post("MAJORCD"),
                             "CATEGORYCD"    =>  VARS::post("CATEGORYCD"),
                             "UPDATED"       =>  VARS::post("UPDATED")
                             );
        
        $this->cmd       = VARS::request("cmd");
        if (VARS::get("mode") == "1") {
            unset($this->COURSECD);
            unset($this->MAJORCD);
        }
        
        /***
        if (VARS::request("COURSECD")) {
            $this->coursecd = VARS::request("COURSECD");
        }
        if (VARS::request("MAJORCD")) {
            $this->majorcd = VARS::request("MAJORCD");
        }
        ***/
        if (VARS::get("PROGRAMID")) {
            unset($this->coursecd);
            unset($this->majorcd);
        }
        
        $temp = VARS::get("COURSECD");
        if (isset($temp)) {
            $this->coursecd  = VARS::get("COURSECD");
        }
        $temp2 = VARS::get("MAJORCD");
        if (isset($temp2)) {
            $this->majorcd  = VARS::get("MAJORCD");
        }
        $temp3 = VARS::get("CATEGORYCD");
        if (isset($temp3)) {
            $this->categorycd = VARS::get("CATEGORYCD");
        }
    }

   //入力項目チェック
   
    function validateInput() {
        //必須チェック
        if (!strlen($this->field["COURSECD"]) || !strlen($this->field["MAJORCD"]) || !strlen($this->field["CATEGORYCD"])) {
            $this->setWarning('MSG301');
            return false;
        }
        if ($this->cmd == "update" && ($this->coursecd != $this->field["COURSECD"] || $this->majorcd != $this->field["MAJORCD"])) {
            $this->setWarning('MSG308');
            return;
        }
        //１レコード取得
        $db = Query::dbCheckOut();
        $query = knjz056Query::getMajorCategoryDat($this->field["COURSECD"],$this->field["MAJORCD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning('MSG303');
            return false;
        }
        
        //重複チェック
        if ($this->cmd == "add" && ($row["COURSECD"] == $this->field["COURSECD"]) && ($row["MAJORCD"] == $this->field["MAJORCD"])) {
            $this->setWarning('MSG918',"課程コードと学科コードの組み合わせは１つだけです。");
            return false;
        }
        
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning('MSG307');
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
            $this->coursecd = $this->field["COURSECD"];
            $this->majorcd = $this->field["MAJORCD"];
            $this->setMessage('MSG201');
        }
    }

    // 削除用のモデルを返します
    function &getDeleteModel() {
        //キーの更新チェック
        if ($this->coursecd == "" || $this->majorcd == "") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->coursecd != $this->field["COURSECD"] || $this->majorcd != $this->field["MAJORCD"]) {
            $this->setWarning('MSG308');
            return;
        }
        $db = Query::dbCheckOut();
        $query = knjz056Query::getMajorCategoryDat($this->coursecd,$this->majorcd);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        if (!is_array($row)) {
            $this->setWarning('MSG303');
            return;
        }
        knjz056Query::getDeleteQuery($this->field);
        $this->setMessage('MSG201');
    }

    function &insert() {
        knjz056Query::getInsertQuery($this->field);
    }

    //アップデート時の内部メソッド
    function &update() {
        knjz056Query::getUpdateQuery($this->field);

    }

    function clean() {}
}
?>
