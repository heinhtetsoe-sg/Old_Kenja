<?php
/**
 * 定数定義
*/

class knjl358kModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $testdiv;
    var $output;
    var $special_reason_div;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl358kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");
    }
    function init()
    {
        $this->cmd     = VARS::request("cmd");
        $this->testdiv = VARS::post("TESTDIV");
        $this->output = VARS::post("OUTPUT");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
