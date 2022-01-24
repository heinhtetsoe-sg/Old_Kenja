<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl622fModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl622fModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {   
        unset($this->field);
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),        //入試区分
                             "EXAMCOURSE"   =>  VARS::post("EXAMCOURSE"),
                             "SHDIV"        =>  VARS::post("SHDIV"),
                             "TESTDIV1"     =>  VARS::post("TESTDIV1"),
                             "FS_COURSE"    =>  VARS::post("FS_COURSE"),
                             "SORT"         =>  VARS::post("SORT")
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
