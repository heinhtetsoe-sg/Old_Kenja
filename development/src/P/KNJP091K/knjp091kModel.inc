<?php

require_once('for_php7.php');

class knjp091kModel extends Model {
    var $field = array();
    var $cmd;
    var $paid_money_date;
    var $file;
    var $exe_year;

    /**
     * メンバ変数の初期化
     */
    function knjp091kModel() {}

    function init() {
        $this->field = array("PAID_MONEY_DATE"      => VARS::post("PAID_MONEY_DATE"),       //振替日
                             "REPLACE_MONEY_DATE"   => VARS::post("REPLACE_MONEY_DATE"),    //振込日
                             "REMARK"               => VARS::post("REMARK")                 //備考
                             );

        $this->cmd = VARS::request("cmd");

        $this->file = VARS::file("FILE");

        if (VARS::get("mode") == "1") {
            unset($this->paid_money_date);
        }

        if (VARS::get("PAID_MONEY_DATE")){
            $this->paid_money_date = VARS::get("PAID_MONEY_DATE");
        }

        $this->exe_year = VARS::post("EXE_YEAR");
    }

    //入力項目チェック
    function validateInput()
    {

        //キーのチェック
        if (trim($this->field["PAID_MONEY_DATE"]) == "" && $this->cmd == "add") {
            $this->setWarning('MSG901', "( 振替日 )");
            return false;
        }
        if (trim($this->field["REPLACE_MONEY_DATE"]) == "" && $this->cmd == "add") {
            $this->setWarning('MSG901', "( 振込日 )");
            return false;
        }
        if ($this->paid_money_date  == "" && $this->cmd == "update") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->cmd == "update" && $this->paid_money_date != $this->field["PAID_MONEY_DATE"]) {
            $this->setWarning('MSG308');
            return;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //１レコード取得
        $row = $db->getRow(knjp091kQuery::getRow($this->field["PAID_MONEY_DATE"]), DB_FETCHMODE_ASSOC);

        //DB切断
        Query::dbCheckIn($db);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning('MSG303');
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning('MSG918', "( 振替日 )");
            return false;
        }
        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage('MSG201');
        }    
    }
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->paid_money_date = $this->field["PAID_MONEY_DATE"];
            $this->setMessage('MSG201');
        }
    }

    // 削除用のモデルを返します
    function &getDeleteModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //キーの更新チェック
        if ($this->paid_money_date == "") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->paid_money_date != $this->field["PAID_MONEY_DATE"]) {
            $this->setWarning('MSG308');
            return;
        }

        $row = $db->getRow(knjp091kQuery::getRow($this->paid_money_date), DB_FETCHMODE_ASSOC);
        if (!is_array($row)){
            $this->setWarning('MSG303');
            return;
        }

        //削除
        knjp091kQuery::getDeleteQuery($db, $this->field);
        $this->setMessage('MSG201');

        //DB切断
        Query::dbCheckIn($db);

    }

    //追加
    function &insert() {
        knjp091kQuery::getInsertQuery($this->field);
    }

    //更新
    function &update() {
        knjp091kQuery::getUpdateQuery($this->field);
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
