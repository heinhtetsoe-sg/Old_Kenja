<?php

require_once('for_php7.php');

class knjl326yModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
        
    function knjl326yModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"	=>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"	    =>  VARS::post("TESTDIV"),     		//入試区分
                             "DESIREDIV"	=>  VARS::post("DESIREDIV"),     	//志望区分
                             "NDATE"	    =>  VARS::post("NDATE"),     	    //通知日付
                             "OUTPUT1"	    =>  VARS::post("OUTPUT1"),     	    //出力範囲１
                             "LDATE"	    =>  VARS::post("LDATE"),     	    //締め切り日付
                             "APP_DATE"	    =>  VARS::post("APP_DATE"),     	//出願日
                             "OUTPUT2"	    =>  VARS::post("OUTPUT2"),     	    //出力範囲２
                             "EXAMNO"	    =>  VARS::post("EXAMNO"),     	    //受験番号
                             "DBNAME"	    =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
