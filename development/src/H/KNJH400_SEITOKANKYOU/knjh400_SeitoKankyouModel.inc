<?php

require_once('for_php7.php');

class knjh400_SeitoKankyouModel extends Model
{
    public $cmd;
    public $selectdata;    //新クラス（左リスト）
    public $selectdataLabel;    //新クラス（左リスト）
    public $control;

    public function knjh400_SeitoKankyouModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->schregno = array();
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->schregno = VARS::request("SCHREGNO") != "" ? VARS::request("SCHREGNO") : $this->schregno;

        $this->year = VARS::request("YEAR") != "" ? VARS::request("YEAR") : $this->year;

        $this->top["GYOUSYA"] = VARS::post("GYOUSYA");

        $this->field = array("GYOUSYA" => VARS::request("GYOUSYA"),
                            );
    }

    public function validateInput()
    {
        return true;
    }

}
