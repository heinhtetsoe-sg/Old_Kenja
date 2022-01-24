<?php

require_once('for_php7.php');

class knjz041_course_reflectionModel extends Model {
    var $cmd;
    var $year;
    var $control;
    var $selectdata;

    //メンバ変数の初期化
    function knjz041_course_reflectionModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $temp = VARS::post("year");
        $this->year = (isset($temp)) ? VARS::post("year") : CTRL_YEAR;

        //権限
        if (!$this->auth) $this->auth = VARS::get("SEND_AUTH");

        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput() {

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
        $result = knjz041_course_reflectionQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
