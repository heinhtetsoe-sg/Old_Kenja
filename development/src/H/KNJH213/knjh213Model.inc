<?php

require_once('for_php7.php');

class knjh213Model extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knjh213Model() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            $this->data = array();
        }

        $this->cmd = VARS::request("cmd");

        //Windowサイズ
        $this->windowHeight = VARS::post("windowHeight");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->monthArray = array("04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03");

        $this->field = array("SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "CLUB"             =>  VARS::post("CLUB"),             //部活動
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS")    //対象クラス
                             );

        $this->sch_list    = VARS::post("SCH_LIST");

        $this->data = array();
        $schArr = explode(",", $this->sch_list);
        foreach ($schArr as $key => $schregNo) {
            $this->data["CHK_SCHREG"][$schregNo] = VARS::post("CHK_SCHREG_".$schregNo);
            foreach ($this->monthArray as $key => $month) {
                $this->data["FLG".$month][$schregNo]    = VARS::post("FLG".$month.":".$schregNo);
            }
        }
    }

    //入力項目チェック
    function validateInput() {
        if (!strlen($this->field["SCHOOL_KIND"])) {
            $this->setWarning("MSG304","( 校種 )");
            return false;
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjh213Query::updateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
