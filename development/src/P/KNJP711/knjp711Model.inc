<?php

require_once('for_php7.php');

class knjp711Model extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knjp711Model() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            $this->data = array();
        }

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field["SCHOOL_KIND"]         = VARS::post("SCHOOL_KIND");
        $this->field["COLLECT_GRP_CD"]      = VARS::post("COLLECT_GRP_CD");

        $this->colcd    = VARS::post("COLCD_LIST");

        $this->data = array();
        $array = explode(",", $this->colcd);
        foreach ($array as $key => $val) {
            $this->data["MONTH_CNT"][$val]          = VARS::post("MONTH_CNT:".$val);
            $this->data["ALL_MONTH"][$val]          = VARS::post("ALL_MONTH:".$val);
            for ($i = 1; $i <= 12; $i++) {
                $this->data["COLLECT_MONTH_".$i][$val]    = VARS::post("COLLECT_MONTH_".$i.":".$val);
            }
        }
    }

    //入力項目チェック
    function validateInput() {
        if (!strlen($this->field["SCHOOL_KIND"])) {
            $this->setWarning("MSG304","( 校種 )");
            return false;
        }
        if (!strlen($this->field["COLLECT_GRP_CD"])) {
            $this->setWarning("MSG304","( 入金グループ )");
            return false;
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjp711Query::updateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
