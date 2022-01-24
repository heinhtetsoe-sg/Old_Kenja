<?php

require_once('for_php7.php');

class knjl016d_2Model extends Model {
    var $field = array();
    var $cmd;
    var $decisionCd;        //primary

    /**
     * メンバ変数の初期化
     */
    function knjl016d_2Model() {}

    function init() {
        $this->field = array("DECISION_CD"         => VARS::post("DECISION_CD"),         //内部判定コード
                             "DECISION_NAME"       => VARS::post("DECISION_NAME")        //内部判定名称
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("mode") == "1") {
            unset($this->decisionCd);
        }
        $temp = VARS::get("DECISION_CD");
        if (isset($temp)) {
            $this->decisionCd  = VARS::get("DECISION_CD");
        }
    }

    //入力項目チェック
    function validateInput($kind) {
        //キーのチェック
        if (trim($this->field["DECISION_CD"]) == "" && $kind == "add") {
            $this->setWarning('MSG901',"( 内部判定コード )");
            return false;
        }
        //キーの更新チェック
        if ($this->decisionCd  == "" && ($kind == "update" || $kind == "delete")) {
            $this->setWarning('MSG308');
            return false;
        }
        if (($kind == "update" || $kind == "delete") && $this->decisionCd != $this->field["DECISION_CD"]) {
            $this->setWarning('MSG308');
            return;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //１レコード取得
        $row = $db->getRow(knjl016d_2Query::getDecisionData($this->field["DECISION_CD"]), DB_FETCHMODE_ASSOC);
        $chkYdat = $db->getRow(knjl016d_2Query::IsExisting($this->field["DECISION_CD"]), DB_FETCHMODE_ASSOC);

        //DB切断
        Query::dbCheckIn($db);

        if (($kind == "update" || $kind == "delete") && !is_array($row)) {
            $this->setWarning('MSG303');
            return false;
        }
        //重複チェック
        if ($kind == "add" && is_array($row)) {
            $this->setWarning('MSG918', "( 内部判定コード )");
            return false;
        }

        //年度データの存在チェック
        if ($kind == "delete" && is_array($chkYdat)) {
            $this->setWarning('MSG919',"年度データに登録されています。");
            return;
        }

        if ($kind != "delete" && strlen($this->field["DECISION_CD"]) > 1) {
            $this->setWarning('MSG901', "( 内部判定コード ) 1バイトまで");
            return false;
        } elseif ($kind != "delete" && strlen($this->field["DECISION_NAME"]) > 120) { 
            $this->setWarning('MSG901', "( 内部判定名称 ) 120バイトまで");
            return false;
        }

        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
            $this->setMessage('MSG201');
        }
    }
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("add")){
            $this->insert();
            $this->decisionCd = $this->field["DECISION_CD"];
            $this->setMessage('MSG201');
        }
    }

    // 削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")){
            //削除
            knjl016d_2Query::getDeleteQuery($this->field);
            $this->decisionCd = "";
            $this->setMessage('MSG201');
        }
    }

    //追加
    function &insert() {
        knjl016d_2Query::getInsertQuery($this->field);
    }

    //更新
    function &update() {
        knjl016d_2Query::getUpdateQuery($this->field);
    }

    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
