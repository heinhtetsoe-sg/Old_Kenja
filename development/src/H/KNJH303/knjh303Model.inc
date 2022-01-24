<?php

require_once('for_php7.php');


class knjh303Model extends Model {

    var $field        = array();
    var $cmd;
    var $schregno;
    var $control_data = array();    //コントロールマスタデータを保持

    function knjh303Model()
    {
    }

    function init()
    {

        $this->field = array("TRAINDATE"    =>  VARS::post("TRAINDATE"),             //指導日付
                             "PATIENTCD"    =>  VARS::post("PATIENTCD"),             //相談者
                             "STAFFCD"      =>  VARS::post("STAFFCD"),               //対応者
                             "HOWTOTRAINCD" =>  VARS::post("HOWTOTRAINCD"),          //指導方法
                             "CONTENT"      =>  VARS::post("CONTENT"),               //指導内容
                             );

        $this->cmd          = VARS::request("cmd");
        $this->schregno		= VARS::request("SCHREGNO");

    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

}
?>
