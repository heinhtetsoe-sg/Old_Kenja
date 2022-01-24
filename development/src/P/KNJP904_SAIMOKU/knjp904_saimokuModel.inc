<?php

require_once('for_php7.php');

class knjp904_saimokuModel extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $updateArr = array();

    function knjp904_saimokuModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "LEVY_L_CD"        =>  VARS::post("LEVY_L_CD"),     //預り金科目
                             "OUTGO_L_M_CD"     =>  VARS::post("OUTGO_L_M_CD"),     //支出項目
                             "REQUEST_NO"       =>  VARS::post("REQUEST_NO"),       //伝票番号
                             "REQUEST_DATE"     =>  VARS::post("REQUEST_DATE"),     //伺い日
                             "OUTGO_DATE"       =>  VARS::post("OUTGO_DATE")        //支出日
                            );

        if (VARS::get("SEND_PRGID")) {
            $this->getSchoolCd   = VARS::get("SEND_SCHOOLCD");
            $this->getSchoolKind = VARS::get("SEND_SCHOOL_KIND");
            $this->getYear       = VARS::get("SEND_YEAR");
            $this->lastYear      = $this->getYear - 1;
            $this->getOutgoLcd   = VARS::get("SEND_OUTGO_L_CD");
            $this->getOutgoMcd   = VARS::get("SEND_OUTGO_M_CD");
        }

        //更新用データセット
        foreach ($this->updateArr as $sCd) {
            $this->field["BUDGET_MONEY:".$sCd] = VARS::post("BUDGET_MONEY:".$sCd);
        }

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
    }

    //データチェック
    function dataCheck() {
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if (!$this->dataCheck()) {
            return;
        }

        //削除
        knjp904_saimokuQuery::getDeleteQuery($this);

        //追加
        knjp904_saimokuQuery::getUpdateQuery($this);

        $this->setMessage("MSG201");
        return;
    }
}
?>
