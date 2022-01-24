<?php

require_once('for_php7.php');

class knjl321fModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $testdivname;
    var $cmd;
    var $control;   //コントロールマスタ
        
    function knjl321fModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "EXAM_TYPE"    =>  VARS::post("EXAM_TYPE"),        //受験型
                             "SORT"         =>  VARS::post("SORT"),             //出力順
                             "TESTDIV5OUT"  =>  VARS::post("TESTDIV5OUT"),      //出力対象
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
