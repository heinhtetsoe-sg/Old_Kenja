<?php

require_once('for_php7.php');

class knjd139Model extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knjd139Model() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科
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
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            if ($this->field["SCHOOL_KIND"] == "") {
                $this->setWarning("MSG304", "( 校種 )");
                return false;
            }
        }

        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG304", "( 学期 )");
            return false;
        }

        if ($this->field["SUBCLASSCD"] == "") {
            $this->setWarning("MSG304", "( 科目 )");
            return false;
        }

        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }
        //桁数、バイトチェック
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if ($this->validate_row_cnt($this->fields["REMARK1"][$i], (20 * 2)) > 5) {
                $this->setWarning("MSG915","(コメントは5行までです)");
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $term_name = "REMARK1"."-".$i;
            $this->fields["REMARK1"][] = VARS::post($term_name);
        }

        if ($this->validateInput()) {
            knjd139Query::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>