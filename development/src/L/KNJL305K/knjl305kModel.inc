<?php
/**
 * 定数定義
*/

class knjl305kModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $testdiv;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl305kModel()
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
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
