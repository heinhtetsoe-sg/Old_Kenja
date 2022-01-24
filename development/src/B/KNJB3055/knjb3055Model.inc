<?php

require_once('for_php7.php');

class knjb3055Model extends Model {
    var $field = array();
    var $cmd;
    var $control; //コントロールマスタ

    function knjb3055Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SUBCLASSCD" => VARS::request("SUBCLASSCD"),    //科目コード
                             "SEMESTER"   => VARS::request("SEMESTER"),      //年度 - 学期
                             "CLASSCD"    => VARS::request("CLASSCD"),       //教科コード
                             );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
    function &getMainModel() {}
}
?>
