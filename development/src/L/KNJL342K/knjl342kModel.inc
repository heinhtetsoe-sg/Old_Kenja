<?php
/**
 * 定数定義
*/

class knjl342kModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $testdiv;
    var $output;
    var $passdiv;
    var $judge;
    var $special_reason_div;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $sort;//NO001
        
    /**
     * メンバ変数の初期化
     */
    function knjl342kModel()
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
        $this->output = VARS::post("OUTPUT");
        $this->passdiv = VARS::post("PASSDIV");
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");
        if ($this->output == 5){
            $this->judge = 5;
        }else if ($this->output == 6){
            $this->judge = 6;
        }else {
            $this->judge = 0;
        }
        $this->sort     = VARS::post("SORT");//NO001
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
