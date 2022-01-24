<?php
class knjl830hModel extends Model {
    var $field = array();
    var $cmd;
    var $examyear;

    function knjl830hModel(){}

    function init() {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),      //学校種別
                             "TESTDIV"          =>  VARS::post("TESTDIV"),           //入試区分
                             "KINDDIV"          =>  VARS::post("KINDDIV"),           //入試種別
                             "OUTPUT"           =>  VARS::post("OUTPUT"),            //性別
                             "SORT"             =>  VARS::post("SORT")               //出力順
                              );

    }
}
?>
