<?php

require_once('for_php7.php');

class knjf331aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    function knjf331aModel() {}
    function init() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),
                             "DATA_DIV"         =>  VARS::post("DATA_DIV"),
                             "DATA_DIV2"        =>  VARS::post("DATA_DIV2"),
                             "DBNAME"           =>  VARS::post("DBNAME")
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    function clean() {}
}
?>
