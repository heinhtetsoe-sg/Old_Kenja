<?php

require_once('for_php7.php');

class knjz093aModel extends Model {
    var $cmd;
    var $year;
    var $selectdata;

    //メンバ変数の初期化
    function knjz093aModel() { }

    function init() {

        $this->cmd = VARS::request("cmd");

        $temp = VARS::post("year");
        if (isset($temp)) {
            $this->year = VARS::post("year");
        } else {
            $this->year = CTRL_YEAR;    //現在処理年度を初期値にする
        }

        $this->selectdata = VARS::post("selectdata");
    }

    //入力項目チェック
    function validateInput() {
        if (!is_numeric($this->year)) {
            $this->warning = "対象年度を選択してください。";
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage('MSG201');
        }
    }

    //アップデート時の内部メソッド
    function &update() {
        $result = knjz093aQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
