<?php

require_once('for_php7.php');

class knjh080fModel extends Model {
    var $field = array();
    var $cmd;
    var $selectdata;

    function knjh080fModel() {}

    function init() {
        $this->field = array("SEMESTER" =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"    =>  VARS::post("GRADE"),            //学年
                             "SDATE"    =>  VARS::post("SDATE"),            //出欠状況出力範囲
                             "EDATE"    =>  VARS::post("EDATE"),            //出欠状況出力範囲
                             "DBNAME"   =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
}
?>
