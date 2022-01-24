<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl353cModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjl353cModel()
    {
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;
    }

    function init()
    {
        $this->field = array("APPLICANTDIV"	    =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"	        =>  VARS::post("TESTDIV"),     		//入試区分
                             "PRINT_TYPE"	    =>  VARS::post("PRINT_TYPE"),     	//学校・塾選択
                             "PRINT_SCORE"	    =>  VARS::post("PRINT_SCORE"),     	//得点表示有無
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");

    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>