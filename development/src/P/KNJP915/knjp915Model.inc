<?php

require_once('for_php7.php');

class knjp915Model extends Model
{
    public $cmd;
    public $field = array();
    public $data  = array();

    public function init()
    {
        $this->cmd  = VARS::request("cmd");
        $this->lastYear = CTRL_YEAR - 1;
        $this->year     = CTRL_YEAR;

        $this->updateDiv        = VARS::post("UPDATE_DIV");
        $this->schoolKind       = VARS::post("SCHOOL_KIND");
        $this->carryLMcd        = VARS::post("CARRY_LM_CD");
        $this->incomeLMcdSaki   = VARS::post("INCOME_LM_CD_SAKI");
        $this->incomeDate       = VARS::post("INCOME_DATE");
        $this->incomeLMcdCancel = VARS::post("INCOME_LM_CD_CANCEL");

        //選択生徒
        $this->selectStudent = VARS::post("selectStudent");

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $db = Query::dbCheckOut();

        //名称マスタP016 (収入伝票作成時にセットする入金項目を取得)
        $query = knjp915Query::getNameMst($this, "P016");
        $this->collectLMcd = $db->getOne($query);

        Query::dbCheckIn($db);
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    //実行
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            if ($this->updateDiv == '1') {
                //更新
                knjp915Query::getUpdateQuery($this);
            } else {
                //削除
                knjp915Query::getCancelUpdateQuery($this);
            }
            $this->setMessage("MSG201");
        }

        return;
    }
}
