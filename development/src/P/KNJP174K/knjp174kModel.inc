<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjp174kModel extends Model {
    var $cmd;
    var $control;
    var $paidMonthS;
    var $paidMonthE;
    var $grade;
    var $reducDiv;

    /**
     * メンバ変数の初期化
     */
    function knjp174kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        if (!$this->cmd) {
            unset($this->paidMonthS);
            unset($this->paidMonthE);
            unset($this->grade);
            unset($this->reducDiv);
            unset($this->grdDate);
        }
        $this->paidMonthS = VARS::post("S_MONTH");
        $this->paidMonthE = VARS::post("E_MONTH");
        $this->grade = VARS::post("GRADE");
        $this->reducDiv = VARS::post("REDUC_DIV");
        $this->grdDate = VARS::post("GRD_DATE");
    }

    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        knjp174kQuery::update($this);
        $this->setMessage("MSG202");
    }
}
?>
