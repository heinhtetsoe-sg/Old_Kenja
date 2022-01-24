<?php

require_once('for_php7.php');

class knjd626aModel extends Model
{
    public $semester;
    public $hrClass;
    public $cmd;
    public $control;        //コントロールマスタ

    public function knjd626aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
    }

    public function init()
    {
        $this->semester = VARS::post("SEMESTER");
        $this->hrClass  = VARS::post("GRADE_HR_CLASS");
        $this->test_cd  = VARS::post("TEST_CD");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
            unset($this->test_cd);
        }

        $this->field = array("SEMESTER"                    =>  VARS::post("SEMESTER"),                     //学期
                             );
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        
        //備考に表示される文字数
        if ($this->Properties["kojinSeisekihyouSyokenFieldSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["kojinSeisekihyouSyokenFieldSize"]);
            $this->hexam_record_remark_dat_remark1_moji = (int)trim($moji);
            $this->hexam_record_remark_dat_remark1_gyou = (int)trim($gyou);
        } else {
            $this->hexam_record_remark_dat_remark1_moji = 30; //デフォルトの値
            $this->hexam_record_remark_dat_remark1_gyou = 5;  //デフォルトの値
        }
    }
}
