<?php

require_once('for_php7.php');

class knjd171pModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $selectdata;

    function knjd171pModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "DESC_DATE"        =>  VARS::post("DESC_DATE"),        //記載日付
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "PRINT_SIDE1"      =>  VARS::post("PRINT_SIDE1"),      //1.表紙
                             "PRINT_SIDE2"      =>  VARS::post("PRINT_SIDE2"),      //2.成績
                             "PRINT_SIDE3"      =>  VARS::post("PRINT_SIDE3"),      //3.所見
                             "PRINT_SIDE4"      =>  VARS::post("PRINT_SIDE4"),      //4.修了証
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        $this->getPropertiesAll();
    }
}
?>
