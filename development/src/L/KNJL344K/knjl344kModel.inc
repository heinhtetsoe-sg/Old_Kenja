<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl344kModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $testdiv;
    var $passdiv;
    var $output;
    var $judge;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $check1;//NO001
    var $check4;//NO001
    var $selectdata;//NO004
    var $srt;//NO004
    var $scalashipdiv;//NO005
    var $special_reason_div;
        
    /**
     * メンバ変数の初期化
     */
    function knjl344kModel()
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
        $this->output = VARS::post("OUTPUT");
        if ($this->output == 2){
            $this->judge = 5;
        }else if ($this->output == 3){
            $this->judge = 6;
        }else {
            $this->judge = 88;
        }

        $this->check1 = VARS::post("CHECK1");//NO001
        $this->check4 = VARS::post("CHECK4");//NO001
        //ソート---NO004
        $this->selectdata = VARS::post("selectdata");
        $this->srt = VARS::post("SORT");

        $this->scalashipdiv = VARS::post("SCALASHIPDIV");//NO005
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
