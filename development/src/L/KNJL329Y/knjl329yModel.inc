<?php

require_once('for_php7.php');

class knjl329yModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
        
    function knjl329yModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"	=>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"	    =>  VARS::post("TESTDIV"),     		//入試区分
                             "NDATE"	    =>  VARS::post("NDATE"),     	      //通知日付
                             "OUTPUTDIV"	=>  VARS::post("OUTPUTDIV"),     	  //出力帳票
                             "OUTPUT"	    =>  VARS::post("OUTPUT"),     	    //出力範囲
                             "FS_CD"	    =>  VARS::post("FS_CD"),     	      //学校番号
                             "DBNAME"	    =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
