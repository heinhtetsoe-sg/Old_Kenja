<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjp175kModel extends Model {
    var $cmd;
    var $control;
    var $reducDiv;
    var $paidYearMonth;
    var $paidMonthS;
    var $paidMonthE;
    var $grade;
    var $offset;

    /**
     * メンバ変数の初期化
     */
    function knjp175kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        if (!$this->cmd) {
            unset($this->reducDiv);
            unset($this->paidYearMonth);
            unset($this->paidMonthS);
            unset($this->paidMonthE);
            unset($this->grade);
            unset($this->offset);
        }
        $this->reducDiv = VARS::post("REDUC_DIV");
        $this->paidYearMonth = VARS::post("PAID_YEARMONTH");
        $this->paidMonthS = "04";
        $this->paidMonthE = VARS::post("E_MONTH");
        $this->grade = VARS::post("GRADE");
        $this->offset = VARS::post("OFFSET");
    }

    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        knjp175kQuery::update($this);
        $this->setMessage("MSG202");
    }
}
?>
