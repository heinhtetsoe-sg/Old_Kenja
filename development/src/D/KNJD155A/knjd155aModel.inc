<?php

require_once('for_php7.php');

class knjd155aModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $replace_data   = array();    //置換で選択された生徒一覧
    public $semester       = "";
    public $grade_hr_class = "";
    public $testkindcd     = "";

    public function knjd155aModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト種別
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd     = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if ($this->Properties["kojinSeisekihyouSyokenFieldSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["kojinSeisekihyouSyokenFieldSize"]);
            $this->hexam_record_remark_dat_remark1_moji = (int)trim($moji);
            $this->hexam_record_remark_dat_remark1_gyou = (int)trim($gyou);
        } else {
            $this->hexam_record_remark_dat_remark1_moji = 30; //デフォルトの値
            $this->hexam_record_remark_dat_remark1_gyou = 5;  //デフォルトの値
        }

        //置換
        if ($this->cmd == "replace_update") {
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["field"] = array();

            $this->replace_data["check"] = array(VARS::post("RCHECK0"));
            $this->replace_data["check_all"] = VARS::post("RCHECK1");

            if (VARS::post("RCHECK0") == "1") {
                $this->replace_data["field"]["REMARK"]  = VARS::post("REMARK");
            }
        }

        if (VARS::get("SEMESTER")) {
            $this->semester = VARS::get("SEMESTER");
        }
        if (VARS::get("GRADE_HR_CLASS")) {
            $this->grade_hr_class = VARS::get("GRADE_HR_CLASS");
        }
        if (VARS::get("TESTKINDCD")) {
            $this->testkindcd = VARS::get("TESTKINDCD");
        }

        if ($this->cmd == "back") {
            unset($this->replace_data);
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須入力チェック
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG304", "( 学期 )");
            return false;
        }
        if ($this->field["TESTKINDCD"] == "") {
            $this->setWarning("MSG304", "( テスト種別 )");
            return false;
        }

        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        //桁数チェック
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if ($this->validate_row_cnt($this->fields["REMARK"][$i], ((int)$this->hexam_record_remark_dat_remark1_moji * 2)) > $this->hexam_record_remark_dat_remark1_gyou) {
                $alert_title = $this->Properties["kojinSeisekihyouSyokenTitle"] ? $this->Properties["kojinSeisekihyouSyokenTitle"] : '通信欄';
                $this->setWarning("MSG915", "({$alert_title}は{$this->hexam_record_remark_dat_remark1_gyou}行までです)");
                return false;
            }
        }

        return true;
    }

    //更新
    public function &getUpdateModel()
    {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $term_name = "REMARK"."-".$i;
            $this->fields["REMARK"][] = VARS::post($term_name);
        }

        if ($this->validateInput()) {
            knjd155aQuery::update($this);
            $this->setMessage("MSG201");
        }
    }

    //一括更新
    public function &getReplaceModel()
    {
        //生徒が選択されてない場合
        if (!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304", "　　　（生徒）");
            return false;
        }
        //最低ひとつでもチェックがついてる場合のみ更新
        if (!in_array("1", $this->replace_data["check"])) {
            $this->setWarning("MSG304", "　　（チェック）");
            return false;
        }

        //入力チェック
        if ($this->validate_row_cnt($this->replace_data["field"]["REMARK"], ((int)$this->hexam_record_remark_dat_remark1_moji * 2)) > $this->hexam_record_remark_dat_remark1_gyou) {
            $alert_title = $this->Properties["kojinSeisekihyouSyokenTitle"] ? $this->Properties["kojinSeisekihyouSyokenTitle"] : '通信欄';
            $this->setWarning("MSG915", "({$alert_title}は{$this->hexam_record_remark_dat_remark1_gyou}行までです)");
            return false;
        }

        //チェックがついている項目があるテーブルのみを更新
        if (in_array("1", $this->replace_data["check"])) {
            knjd155aQuery::ReplaceUpdate($this);
        }
        $this->setMessage("MSG201");
        //添え字を０から順序良くする
        $this->replace_data["check"][] = $this->replace_data["check_all"];
    }
}
