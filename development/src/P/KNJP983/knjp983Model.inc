<?php

require_once('for_php7.php');

class knjp983Model extends Model {
    var $cmd;
    var $year;
    var $control;
    var $selectdata;

    //メンバ変数の初期化
    function knjp983Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $temp = VARS::post("year");
        $this->year         = (isset($temp)) ? VARS::post("year") : CTRL_YEAR;

        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if (!is_numeric($this->year)) {
            $this->setWarning("MSG310", "対象年度を選択してください。");

            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    function &update() {
        $result = knjp983Query::getUpdateQuery($this);
        return $result;
    }
}
?>
