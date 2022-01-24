<?php
/**
 * 定数定義
*/

class knjl324kModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $output;
    var $border;
    var $special_reason_div;
    var $passflg;//2005.10.28Add
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl324kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {
        $this->cmd     = VARS::request("cmd");
        $this->output = VARS::post("OUTPUT");
        $this->border = VARS::post("BORDER");
        $this->passflg = VARS::post("PASSFLG");//2005.10.28Add
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
