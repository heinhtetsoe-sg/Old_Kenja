<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl314nModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl314nModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("APPLICANTDIV"       =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"            =>  VARS::post("TESTDIV"),        //入試区分
                             "EXAMCOURSE"         =>  VARS::post("EXAMCOURSE"),     //志望区分
                             "S_EXAMNO"           =>  VARS::post("S_EXAMNO"),       //開始受験番号
                             "E_EXAMNO"           =>  VARS::post("E_EXAMNO"),       //終了受験番号
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
