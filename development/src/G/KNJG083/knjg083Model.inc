<?php

require_once('for_php7.php');

class knjg083Model extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knjg083Model() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            $this->fields = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("GROUP_DIV"        =>  VARS::post("GROUP_DIV"),        //グループ
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd     = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須入力チェック
        if ($this->field["GROUP_DIV"] == "") {
            $this->setWarning("MSG304", "( グループ )");
            return false;
        }

        if ($this->field["GRADE_HR_CLASS"] == "") {
            $this->setWarning("MSG304", "( 年組 )");
            return false;
        }

        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $div1_name2 = "DIV1_REMARK2"."_".$i;
            $div1_name3 = "DIV1_REMARK3"."_".$i;
            $div1_name4 = "DIV1_REMARK4"."_".$i;
            $div2_name1 = "DIV2_REMARK1"."_".$i;
            $div2_name2 = "DIV2_REMARK2"."_".$i;
            $div2_name3 = "DIV2_REMARK3"."_".$i;
            $div2_name4 = "DIV2_REMARK4"."_".$i;
            $this->fields["DIV1_REMARK2"][] = VARS::post($div1_name2);
            $this->fields["DIV1_REMARK3"][] = VARS::post($div1_name3);
            $this->fields["DIV1_REMARK4"][] = VARS::post($div1_name4);
            $this->fields["DIV2_REMARK1"][] = VARS::post($div2_name1);
            $this->fields["DIV2_REMARK2"][] = VARS::post($div2_name2);
            $this->fields["DIV2_REMARK3"][] = VARS::post($div2_name3);
            $this->fields["DIV2_REMARK4"][] = VARS::post($div2_name4);
        }

        if ($this->validateInput()) {
            knjg083Query::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
