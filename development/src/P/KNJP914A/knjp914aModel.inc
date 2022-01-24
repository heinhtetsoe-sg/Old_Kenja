<?php

require_once('for_php7.php');

class knjp914aModel extends Model
{
    public $cmd;
    public $field = array();
    public $data  = array();

    public function init()
    {
        $this->cmd  = VARS::request("cmd");
        $this->year = CTRL_YEAR;

        $this->schoolKind       = VARS::post("SCHOOL_KIND");
        $this->incomeLMcdMoto   = VARS::post("INCOME_LM_CD_MOTO");
        $this->incomeLMcdSaki   = VARS::post("INCOME_LM_CD_SAKI");
        $this->outgoLMcdMoto    = VARS::post("OUTGO_LM_CD_MOTO");
        $this->outgoLMScdMoto   = VARS::post("OUTGO_LMS_CD_MOTO");
        $this->requestReason    = VARS::post("REQUEST_REASON");
        $this->setMoney         = VARS::post("SET_MONEY");
        $this->taigaku          = VARS::post("TAIGAKU");

        //選択生徒
        $this->selectStudent = VARS::post("selectStudent");

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //名称マスタ取得
        $db = Query::dbCheckOut();

        $query = knjp914aQuery::getNameMst($this, "P015");
        $this->collectLMcd = $db->getOne($query);

        Query::dbCheckIn($db);
    }

    //入力項目チェック
    public function validateInput()
    {
        $db = Query::dbCheckOut();

        //名称マスタP015に設定した入金項目がマスタにあるかチェック
        if ($db->getOne(knjp914aQuery::getExistsCollectMMst($this, $this->collectLMcd)) == 0) {
            $this->setWarning("MSG917", "名称マスタ(P015)に設定された入金項目はマスタに存在しません。");
            return false;
        }

        //サイズ
        if (strlen($this->requestReason) > 120) {
            $this->setWarning("MSG901", "( 件名 ) 120バイトまで");
            return false;
        }

        Query::dbCheckIn($db);

        return true;
    }

    //実行
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjp914aQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }

        return;
    }
}
