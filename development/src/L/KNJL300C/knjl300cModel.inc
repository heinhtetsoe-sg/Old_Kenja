<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl300cModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjl300cModel()
    {
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;
    }

    function init()
    {
        $this->field = array("TESTDIV"	        =>  VARS::post("TESTDIV"),          //入試区分
                             "EXAM_TYPE"	    =>  VARS::post("EXAM_TYPE"),        //受験型
                             "RECEPTNO_FROM"    =>  VARS::post("RECEPTNO_FROM"),    //受付開始番号（開始）
                             "RECEPTNO_TO"	    =>  VARS::post("RECEPTNO_TO"),      //受付開始番号（終了）
                             "LINE"	            =>  VARS::post("LINE"),     	    //開始位置（行）
                             "ROW"              =>  VARS::post("ROW"),     	        //開始位置（列）
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
