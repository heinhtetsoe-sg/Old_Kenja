<?php

require_once('for_php7.php');

class knjd135Model extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $schoolName;

    function knjd135Model() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd     = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if ($this->Properties["tutisyoSyokenFieldSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["tutisyoSyokenFieldSize"]);
            $this->hreportremark_dat_communication_moji = (int)trim($moji);
            $this->hreportremark_dat_communication_gyou = (int)trim($gyou);
        } else {
            $this->hreportremark_dat_communication_moji = 30; //デフォルトの値
            $this->hreportremark_dat_communication_gyou = 5;  //デフォルトの値
        }

        $this->tutisyoSyokenTitle = $this->Properties["tutisyoSyokenTitle"] ? $this->Properties["tutisyoSyokenTitle"] : '通信欄';
    }

    //入力項目チェック
    function validateInput() {
        //必須入力チェック
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG304", "( 学期 )");
            return false;
        }

        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        //桁数チェック
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if ($this->validate_row_cnt($this->fields["REMARK"][$i], ($this->hreportremark_dat_communication_moji * 2)) > $this->hreportremark_dat_communication_gyou) {
                $this->setWarning("MSG915","({$this->tutisyoSyokenTitle}は{$this->hreportremark_dat_communication_gyou}行までです)");
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $term_name = "REMARK"."-".$i;
            $this->fields["REMARK"][] = VARS::post($term_name);
        }

        if ($this->validateInput()) {
            knjd135Query::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
