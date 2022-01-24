<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjp372kModel extends Model {
    var $cmd;
    var $control;
    var $reducDiv;
    var $paidYearMonth;
    var $grade;
    var $lock;

    /**
     * メンバ変数の初期化
     */
    function knjp372kModel()
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
            unset($this->grade);
            unset($this->lock);
        }
        $this->reducDiv = VARS::post("REDUC_DIV");
        $this->paidYearMonth = VARS::post("PAID_YEARMONTH");
        $this->grade = VARS::post("GRADE");
        $this->lock = VARS::post("LOCK");
    }

    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        knjp372kQuery::update($this);
        $this->setMessage("MSG202");
    }
}
?>
