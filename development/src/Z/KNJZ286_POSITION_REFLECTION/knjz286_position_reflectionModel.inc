<?php

require_once('for_php7.php');

class knjz286_position_reflectionModel extends Model {
    var $cmd;
    var $control;
    var $selectdata;

    //メンバ変数の初期化
    function knjz286_position_reflectionModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->update();
        $this->setMessage("MSG201");
    }

    //アップデート時の内部メソッド
    function &update() {
        $result = knjz286_position_reflectionQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
