<?php

require_once('for_php7.php');

class knjl333aModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;
    public $examType;

    public function knjl333aModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;
        $this->examType = 1;

        $this->field = array("APPLICANTDIV"    =>  VARS::post("APPLICANTDIV"),     //受験校種
                             "TESTDIV"         =>  VARS::post("TESTDIV"),          //試験回
                             "DOC_TYPE"        =>  VARS::post("DOC_TYPE"),         //届・受領書種別
                             "RECEPTNO_FROM"   =>  VARS::post("RECEPTNO_FROM"),    //受験番号from
                             "RECEPTNO_TO"     =>  VARS::post("RECEPTNO_TO"),      //受験番号to
                             "PRINT_ERACD"     =>  VARS::post("PRINT_ERACD"),      //出力年度元号
                             "PRINT_ERAY"      =>  VARS::post("PRINT_ERAY"),       //出力年度年
                             "PRINT_EMONTH"    =>  VARS::post("PRINT_EMONTH"),     //出力月
                             "PRINT_EDAY"      =>  VARS::post("PRINT_EDAY"),       //出力日
                     );

        $this->cmd = VARS::request("cmd");
    }
}
