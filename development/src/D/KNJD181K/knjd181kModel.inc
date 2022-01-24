<?php

require_once('for_php7.php');

class knjd181kModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $selectdata;

    function knjd181kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("DATE"                 =>  VARS::post("DATE"),                 //異動対象日付
                             "DESC_DATE"            =>  VARS::post("DESC_DATE"),            //記載日付
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コード
                             "PRINT_SEMESTER"       =>  VARS::post("PRINT_SEMESTER"),       //印刷学期コード
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //年組
                             "PRINT_SIDE1"          =>  VARS::post("PRINT_SIDE1"),          //1.表紙
                             "PRINT_SIDE2"          =>  VARS::post("PRINT_SIDE2"),          //2.学習の記録
                             "HR_CLASS_TYPE"        =>  VARS::post("HR_CLASS_TYPE"),        //実クラス選択
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                     );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        $this->getPropertiesAll();
    }
}
?>
