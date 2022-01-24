<?php

require_once('for_php7.php');

class knjd129jModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knjd129jModel() {}

    function init() {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

}
//var_dump($_REQUEST);
?>
