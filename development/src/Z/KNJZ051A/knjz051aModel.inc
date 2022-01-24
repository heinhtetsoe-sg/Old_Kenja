<?php

require_once('for_php7.php');

class knjz051aModel extends Model {
    var $cmd;
    var $year;
    var $control;
    var $selectdata;

    //メンバ変数の初期化
    function knjz051aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $temp = VARS::post("year");
        $this->year         = (isset($temp)) ? VARS::post("year") : CTRL_YEAR;

        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
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
        $result = knjz051aQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
