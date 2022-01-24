<?php

require_once('for_php7.php');

class knjl330fModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl330fModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "OLD_TESTDIV"      =>  VARS::post("OLD_TESTDIV"),      //入試区分
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //帳票種類
                             "F_EXAMNO"         =>  VARS::post("F_EXAMNO"),         //受験番号FROM
                             "T_EXAMNO"         =>  VARS::post("T_EXAMNO"),         //受験番号TO
                             "LIMIT_DATE"       =>  VARS::post("LIMIT_DATE"),       //納入期限
                             "SALES_DATE"       =>  VARS::post("SALES_DATE"),       //販売日付
                             "SALES_HOUR"       =>  VARS::post("SALES_HOUR"),       //販売時間
                             "SALES_HOUR2"      =>  VARS::post("SALES_HOUR2"),      //販売時間2
                             "SALES_HOUR3"      =>  VARS::post("SALES_HOUR3"),      //販売時間3
                             "SALES_LOCATION"   =>  VARS::post("SALES_LOCATION")    //販売場所
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
