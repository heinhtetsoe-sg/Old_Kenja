<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjj010Model extends Model {
    var $cmd;
    var $year;  //年度
    var $selectdata;
    
    /**
     * メンバ変数の初期化
     */
    function knjj010Model() {
    }
    function init() {
        $this->cmd               = VARS::request("cmd");

        $temp = VARS::post("year");
        if (isset($temp)) {
            $this->year              = VARS::post("year");
        } else {
            $this->year              = CTRL_YEAR;               //現在処理年度を初期値にする
        }

        if (VARS::post("SCHKIND") != "") {
            $this->schKind = VARS::post("SCHKIND");
        }

        $this->selectdata        = VARS::post("selectdata");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        // 権限チェック
        if (common::SecurityCheck(STAFFCD,PROGRAMID) != DEF_UPDATABLE) {
            $this->setWarning("MSG300");
            return false;
        }

        if (!is_numeric($this->year)) {
            $this->warning = "対象年度を選択してください。";
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
            $this->setMessage("MSG201");
        }
    }
    
    /**
     * アップデート時の内部メソッド
     */
    function &update() {
        $result = knjj010Query::getUpdateQuery($this);
        return $result;
    }
}
?>
