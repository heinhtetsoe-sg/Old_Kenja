<?php

require_once('for_php7.php');

class knje014Model extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knje014Model() {}

    function init() {

        if (VARS::request("PROGRAMID")) {
            $this->field    = array();
            $this->data     = array();
            $this->fields   = array();
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");

        //課程学科コード
        $this->field["COURSE_MAJOR"] = VARS::post("COURSE_MAJOR");

        //学級コード
        if ($this->cmd != "change_course" && VARS::post("GROUP_HR_CLASS")) {
            $this->field["GROUP_HR_CLASS"] = VARS::post("GROUP_HR_CLASS");
        } else {
            $this->field["GROUP_HR_CLASS"] = "";
        }

        if (in_array($this->cmd, array("change_course", "change_hrclass", "reset"))) {
            $this->field["CHECKALL"] = "";
        } else {
            $this->field["CHECKALL"] = VARS::post("CHECKALL");
        }

        //校種
        $this->field["SCHOOL_KIND"] = VARS::post("SCHOOL_KIND");

        //学校区分
        $this->field["SCHOOLDIV"] = VARS::post("SCHOOLDIV");

        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $this->fields["COMMENTEX_A_CD"][]  = VARS::post("COMMENTEX_A_CD-".$i);
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knje014Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
