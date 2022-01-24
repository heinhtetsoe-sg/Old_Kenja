<?php

require_once('for_php7.php');

//定数定義
class knjz526nModel extends Model {
    var $field = array();
    var $cmd;
    var $selfDiv;
    var $itemCnt;

    //メンバ変数の初期化
    function knjz526nModel() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->selfDiv);
            unset($this->itemCnt);
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SELF_DIV")) {
            $this->selfDiv = VARS::get("SELF_DIV");
            $this->itemCnt  = VARS::get("ITEM_CNT");
        }

        $this->field = array("SELF_DIV"          => VARS::post("SELF_DIV"),        //区分
                             "SELF_TITLE"        => VARS::post("SELF_TITLE"),      //区分（内容）
                             "ITEM_CNT"          => VARS::post("ITEM_CNT"),        //総項目数
                             "SELF_ITEM"         => VARS::post("SELF_ITEM"),       //項目
                             );
        for ($i = 1; $i <= $this->field["ITEM_CNT"]; $i++) {
            $this->field["SELF_CONTENT_".$i] = VARS::post("SELF_CONTENT_".$i);   //項目（内容）
        }
    }

    //入力項目チェック
    function validateInput($kind) {
        //空文字チェック
        if (strlen($this->field["SELF_DIV"]) == 0) {
            $this->setWarning("MSG301","(区分)");
            return false;
        }
        if (strlen($this->field["ITEM_CNT"]) == 0) {
            $this->setWarning("MSG301","(総項目数)");
            return false;
        }

        //バイト数チェック
        if (strlen($this->field["SELF_DIV"]) > 1) {
            $this->setWarning("MSG915","(区分は半角1文字までです。)");
            return false;
        }
        if (strlen($this->field["SELF_TITLE"]) > 60) {
            $this->setWarning("MSG915","(区分（内容）は全角20文字までです。)");
            return false;
        }
        if (strlen($this->field["ITEM_CNT"]) > 1) {
            $this->setWarning("MSG915","(総項目数は半角1文字までです。)");
            return false;
        }
        for ($i = 1; $i <= $this->field["ITEM_CNT"]; $i++) {
            if (strlen($this->field["SELF_CONTENT_".$i]) > 150) {
                $this->setWarning("MSG915","(項目（内容）は全角50文字までです。)");
                return false;
            }
        }

        if (($kind=="update" || $kind=="delete") && $this->selfDiv != $this->field["SELF_DIV"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        $db = Query::dbCheckOut();

        //入力項目の１レコード取得
        $row = $db->getRow(knjz526nQuery::getRow($this->field["SELF_DIV"]), DB_FETCHMODE_ASSOC);
        //重複チェック
        if ($kind == "insert" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        Query::dbCheckIn($db);

        return true;
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjz526nQuery::getInsertQuery($this);
            $this->selfDiv = $this->field["SELF_DIV"];
            $this->itemCnt = $this->field["ITEM_CNT"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjz526nQuery::getUpdateQuery($this, $this->field);
            $this->itemCnt = $this->field["ITEM_CNT"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjz526nQuery::getDeleteQuery($this, $this->field);
            $this->selfDiv = "";
            $this->itemCnt = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度のデータが存在するか確認
        $thisYear = knjz526nQuery::CheckData(CTRL_YEAR);
        if (isset($thisYear)) {
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $preYear = knjz526nQuery::CheckData(CTRL_YEAR - 1);
        if (!isset($preYear)) {
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }

        knjz526nQuery::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }
}
?>
