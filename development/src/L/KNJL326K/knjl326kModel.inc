<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl326kModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $testdiv;
    var $passdiv;
    var $output ;
    var $output2;
    var $special_reason_div;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl326kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {
        $this->cmd     = VARS::request("cmd");
        $this->testdiv = VARS::post("TESTDIV");
        $this->passdiv = VARS::post("PASSDIV");
        $this->output  = VARS::post("OUTPUT");
        $this->output2 = VARS::post("OUTPUT2");
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>