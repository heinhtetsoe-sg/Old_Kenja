<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl312cModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjl312cModel()
    {
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;
    }

    function init()
    {
        $this->field = array("APPLICANTDIV"	    =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"	        =>  VARS::post("TESTDIV"),     		//入試区分
                             "EXAM_TYPE"	    =>  VARS::post("EXAM_TYPE"),     	//受験型
                             "TESTSUBCLASSCD"	=>  VARS::post("TESTSUBCLASSCD"),   //受験科目
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
