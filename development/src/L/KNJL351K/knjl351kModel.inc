<?php
/**
 * 定数定義
*/

class knjl351kModel extends Model {
    var $cmd;
    var $select_data      = array();
    var $ObjYear;
    var $testdiv;
    var $output;
    var $output2;
    var $date;
    var $special_reason_div;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjl351kModel()
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
        $this->output  = VARS::post("OUTPUT");
        $this->output2  = VARS::post("OUTPUT2");
        $this->date  = VARS::post("DATE");
        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
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
