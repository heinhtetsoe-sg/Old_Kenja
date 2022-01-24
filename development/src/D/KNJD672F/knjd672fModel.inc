<?php

require_once('for_php7.php');

class knjd672fModel extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $selectdata;
    var $selectdata_sg;

    function knjd672fModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),
                             "TANTOU"               =>  VARS::post("TANTOU"),
                             "DATE"                 =>  VARS::post("DATE"),
                             "KAIKIN_KAIKIN_TIKOKU" =>  VARS::post("KAIKIN_KAIKIN_TIKOKU"),         //皆勤者/皆勤者/遅刻・早退
                             "KAIKIN_SEIKIN_TIKOKU" =>  VARS::post("KAIKIN_SEIKIN_TIKOKU"),         //皆勤者/精勤者/遅刻・早退
                             "KAIKIN_KESSEKI"       =>  VARS::post("KAIKIN_KESSEKI"),               //皆勤者欠席
                             "SEIKIN_KESSEKI"       =>  VARS::post("SEIKIN_KESSEKI"),               //精勤者欠席
                             "DBNAME"               =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->selectdata = VARS::post("selectdata");
        $this->selectdata_sg = VARS::post("selectdata_sg");
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
}
?>
