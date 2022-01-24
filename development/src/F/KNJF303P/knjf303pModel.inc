<?php

require_once('for_php7.php');

class knjf303pModel extends Model {

    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjf303pModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("SDATE"    =>  VARS::post("SDATE"),    //対象日(開始)
                             "EDATE"    =>  VARS::post("EDATE"),    //対象日(終了)
                             "DBNAME"   =>  VARS::post("DBNAME")    //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
    }

    function clean()
    {
    }
}
?>
