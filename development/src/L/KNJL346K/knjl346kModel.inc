<?php
/**
 * 定数定義
*/

class knjl346kModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $testdiv;
    var $output;
    var $date;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $out;//NO002
    var $sort;//NO002
    var $passdiv;//NO002
    var $special_reason_div;

    /**
     * メンバ変数の初期化
     */
    function knjl346kModel()
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
        $this->date  = VARS::post("DATE");

        $this->out      = VARS::post("OUT");//NO002
        $this->sort     = VARS::post("SORT");//NO002
        $this->passdiv  = VARS::post("PASSDIV");//NO002
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
