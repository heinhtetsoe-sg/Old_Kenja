<?php

require_once('for_php7.php');

class knjl315yModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
        
    function knjl315yModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"	=>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"	    =>  VARS::post("TESTDIV"),     		//入試区分
                             "INOUT"	    =>  VARS::post("INOUT"),     		//対象者
                             "KIKOKU"	    =>  VARS::post("KIKOKU"),     		//対象者(帰国生)
                             "MAX_SCORE"	=>  VARS::post("MAX_SCORE"),     	//最高点
                             "MIN_SCORE"	=>  VARS::post("MIN_SCORE"),     	//最低点
                             "DBNAME"	    =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
    }
}
?>
