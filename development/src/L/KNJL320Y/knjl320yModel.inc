<?php

require_once('for_php7.php');

class knjl320yModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
        
    function knjl320yModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"	=>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"	    =>  VARS::post("TESTDIV"),     		//入試区分
                             "DESIREDIV"    =>  VARS::post("DESIREDIV"),        //志望区分
                             "INOUT"	    =>  VARS::post("INOUT"),     		//対象者
                             "KIKOKU"	    =>  VARS::post("KIKOKU"),     		//対象者(帰国生)
                             "OUTPUT_NAME"  =>  VARS::post("OUTPUT_NAME"),     	//氏名出力
                             "OUTPUT_REMARK1"  =>  VARS::post("OUTPUT_REMARK1"),     	//備考１出力
                             "SPECIAL"	    =>  VARS::post("SPECIAL"),     		//特別奨学生
                             "SORT"	        =>  VARS::post("SORT"),     		//出力順
                             "RATE_DIV"     =>  VARS::post("RATE_DIV"),         //傾斜配点出力
                             "DBNAME"	    =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

    }
}
?>
