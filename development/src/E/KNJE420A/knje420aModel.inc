<?php

require_once('for_php7.php');

class knje420aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    function knje420aModel() {}
    function init() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "DATA_DIV"         =>  VARS::post("DATA_DIV"),
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    function clean() {}
}
?>
