<?php

require_once('for_php7.php');

class knjxjob_search_ssModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;//コントロールマスタ
    public $target_number;

    public function knjxjob_search_ssModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->field = array("SORT_TYPE"     => VARS::post("SORT_TYPE"),
                             "JOBTYPE_SNAME" => VARS::post("JOBTYPE_SNAME"));

        if (!$this->cmd) {
            unset($this->target_number);
        }

        if (VARS::get("target_number")) {
            $this->target_number = VARS::get("target_number");
        }
    }
}
