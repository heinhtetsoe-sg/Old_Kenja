<?php

require_once('for_php7.php');

class knjh080eModel extends Model {

    var $field = array();
    var $data  = array();
    var $checked     = array();
    var $baseRemark1 = array();
    var $cmd;
    var $semester;
    var $grade_hr_class;

    /**
     * メンバ変数の初期化
     */
    function knjh080eModel() {
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field   = array();
            $this->checked = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );
        foreach ((array)$this->data["SCHREGNO"] as $schregno) {
            $this->field["BASE_REMARK1{$schregno}"] = VARS::post("BASE_REMARK1".$schregno);
        }
        
        //プロパティファイルの読込み
        $this->getPropertiesAll();

        $this->remark1_moji = 21;
        $this->remark1_gyou = 7;

        $this->cmd     = VARS::request("cmd");

        $this->checkSch     = VARS::post("checkSch");
        
        if(VARS::get("SEMESTER")) {
            $this->semester = VARS::get("SEMESTER");
        }
        if(VARS::get("GRADE_HR_CLASS")) {
            $this->grade_hr_class = VARS::get("GRADE_HR_CLASS");
        }
        //置換
        if ($this->cmd == "replace_update") {
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["field"] = array();

            $this->replace_data["check"] = array(VARS::post("RCHECK0"));
            $this->replace_data["check_all"] = VARS::post("RCHECK1");

            if (VARS::post("RCHECK0") == "1") $this->replace_data["field"]["REMARK"]  = VARS::post("REMARK");
        }
        if ($this->cmd == "back") {
            unset($this->replace_data);
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須入力チェック
        if ($this->field["GRADE_HR_CLASS"] == "") {
            $this->setWarning("MSG304", "( 年組 )");
            return false;
        }
        //文字数チェック
        foreach ((array)$this->checked as $schregno) {
            if ($this->validate_row_cnt($this->field["BASE_REMARK1{$schregno}"], (21 * 2)) > 7) {
                $this->setWarning("MSG915","(担任報告は７行までです)");
                $this->checked = array();
                return false;
            }
        }

        //桁数チェック
        foreach ((array)$this->checked as $schregno) {
            if (strlen($this->field["BASE_REMARK1{$schregno}"]) > 768) {
                $this->setWarning("MSG915","( 担任報告は768バイトまでです )");
                $this->checked = array();
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel() {
        foreach ((array)$this->data["SCHREGNO"] as $schregno) {
            if (VARS::request("CHECKED".$schregno)) {
                $this->checked[$schregno]     = VARS::request("CHECKED".$schregno);     //チェック
            }
        }

        if ($this->validateInput()) {
            knjh080eQuery::update($this);
            $this->checked = array();
            $this->setMessage("MSG201");
        }
    }
    
    //一括更新
    function &getReplaceModel()
    {
        //生徒が選択されてない場合
        if (!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304","　　　（生徒）");
            return false;
        }
        //最低ひとつでもチェックがついてる場合のみ更新
        if (!in_array("1",$this->replace_data["check"])) {
            $this->setWarning("MSG304","　　（チェック）");
            return false;
        }

        //入力チェック
        if ($this->validate_row_cnt($this->replace_data["field"]["REMARK"], ($this->remark1_moji * 2)) > $this->remark1_gyou) {
            $alert_title = '担任報告';
            $this->setWarning("MSG915","({$alert_title}は{$this->remark1_gyou}行までです)");
            return false;
        }

        //チェックがついている項目があるテーブルのみを更新
        if (in_array("1",$this->replace_data["check"])) {
            knjh080eQuery::ReplaceUpdate($this);
        }
        $this->setMessage("MSG201");
        //添え字を０から順序良くする
        $this->replace_data["check"][] = $this->replace_data["check_all"];
    }
}
//var_dump($_REQUEST);
?>
