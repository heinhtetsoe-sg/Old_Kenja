<?php

require_once('for_php7.php');

class knjp746aModel extends Model
{
    public $cmd;
    public $field = array();
    public $data  = array();

    public function init()
    {
        $this->cmd  = VARS::request("cmd");
        $this->year = CTRL_YEAR;

        $this->schoolKind   = VARS::post("SCHOOL_KIND");
        $this->paidDate     = VARS::post("PAID_DATE");

        //表示結果用
        $this->totalCnt = 0;
        $this->totalIncome = 0;

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //実行
    public function &getUpdateModel()
    {
        $setMassage = knjp746aQuery::getUpdateQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }

        return;
    }
}
