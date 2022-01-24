<?php
class knjh442bModel extends Model
{

    public $cmd;
    public $exp_year = "";
    public $exp_semester = "";
    public $schregno = "";
    public $name = "";
    public $field = array();
    public $control;
    public $getPrgId;      //コールされたか
    public $sendAuth;      //親画面の権限
    public $auth;          //権限

    public function knjh442bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        if (VARS::get("PROGRAMID")!="") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        $this->field = array("GRADE1_REMARK"      =>  VARS::post("GRADE1_REMARK"),
                             "GRADE2_REMARK"      =>  VARS::post("GRADE2_REMARK"),
                             "GRADE3_REMARK"      =>  VARS::post("GRADE3_REMARK"),
                             "CONDITION"          =>  VARS::post("CONDITION"),
                             "HOPE_COLLEGE_NAME1"  =>  VARS::post("HOPE_COLLEGE_NAME1"),
                             "HOPE_COURSE_NAME1"   =>  VARS::post("HOPE_COURSE_NAME1"),
                             "HOPE_COLLEGE_NAME2"  =>  VARS::post("HOPE_COLLEGE_NAME2"),
                             "HOPE_COURSE_NAME2"   =>  VARS::post("HOPE_COURSE_NAME2"),
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR")!="") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }

        //行数チェック
        if ($this->validate_row_cnt($this->field["CONDITION"], (150 * 2)) > 3) {
            $this->setWarning("MSG915", "(条件は3行までです)");
            return false;
        }

        //文字数チェック
        if (strlen($this->field["GRADE1_REMARK"]) > 150) {
            $this->setWarning("MSG915", "( 行動記録 １年は150バイトまでです )");
            return false;
        } elseif (strlen($this->field["GRADE2_REMARK"]) > 150) {
            $this->setWarning("MSG915", "( 行動記録 ２年は150バイトまでです )");
            return false;
        } elseif (strlen($this->field["GRADE3_REMARK"]) > 150) {
            $this->setWarning("MSG915", "( 行動記録 ３年は150バイトまでです )");
            return false;
        } elseif (strlen($this->field["CONDITION"]) > 150) {
            $this->setWarning("MSG915", "( 条件は150バイトまでです )");
            return false;
        }

        return true;
    }

    public function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    public function &update()
    {
        knjh442bQuery::getUpdateQuery($this->field, $this->schregno, $this->exp_year);
    }
}
?>
