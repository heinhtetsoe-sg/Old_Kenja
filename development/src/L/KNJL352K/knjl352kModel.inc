<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl352kModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $testdiv;
    var $output;
    var $output2;
    var $output3;   //NO001
    var $output4;   //NO003
    var $judge;
    var $special_reason_div;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $date;      //NO004
        
    /**
     * メンバ変数の初期化
     */
    function knjl352kModel()
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
        $this->output2 = VARS::post("OUTPUT2");//---2005.08.23
        $this->output3 = VARS::post("OUTPUT3");//NO001
        $this->output4 = VARS::post("OUTPUT4");//NO003
        $this->output  = VARS::post("OUTPUT"); //---2005.09.13
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");
        //---2005.09.13
        if ($this->output == 5){
            $this->judge = 5;
        }else if ($this->output == 6){
            $this->judge = 6;
        }else {
            $this->judge = 88;
        }
        $this->date  = VARS::post("DATE");//NO004
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
