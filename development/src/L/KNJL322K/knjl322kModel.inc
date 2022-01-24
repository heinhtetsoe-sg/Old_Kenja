<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl322kModel extends Model {
    var $cmd;
    var $ObjYear;
    var $testdiv;
    var $special_reason_div;
    var $subclass;
    var $output;
    var $pchange;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjl322kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear = CTRL_YEAR+1;
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->cmd      = VARS::request("cmd");
        $this->testdiv  = VARS::post("TESTDIV");
        $this->subclass = VARS::post("SUBCLASS");
        $this->output   = VARS::post("OUTPUT");
        $this->pchange  = VARS::post("PCHANGE");
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
