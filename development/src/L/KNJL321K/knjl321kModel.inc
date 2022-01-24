<?php
/**
 * 定数定義
*/

class knjl321kModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $testdiv;
    var $special_reason_div;
    var $hall;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $title;
    var $print = "off";
    var $selected = array();

    /**
     * メンバ変数の初期化
     */
    function knjl321kModel()
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
        $this->title   = VARS::post("TITLE");
        $this->selected = split(",", VARS::post("SELECTED"));
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");
//      $this->hall    = VARS::post("HALL"); NO001
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->dataCheck()) {
            $result = knjl321kQuery::getUpdateQuery($this);
            $this->print = "on";
        }
        return;
    }

    //データチェック
    function dataCheck()
    {
        //文字数チェック
        if (strlen($this->title) > 30) {
            $this->setWarning("MSG915","タイトルは30バイトまでです。");
            return false;
        }
        return true;
    }

}
?>
