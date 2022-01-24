<?php

require_once('for_php7.php');

class knjl322yModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
        
    function knjl322yModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"	=>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"	    =>  VARS::post("TESTDIV"),     		//入試区分
                             "INOUT"	    =>  VARS::post("INOUT"),     		//対象者
                             "KIKOKU"	    =>  VARS::post("KIKOKU"),     		//対象者(帰国生)
                             "KEISHA"	    =>  VARS::post("KEISHA"),     		//傾斜配点
                             "DBNAME"	    =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>