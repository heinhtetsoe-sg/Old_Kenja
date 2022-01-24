<?php

require_once('for_php7.php');

class knjd139iModel extends Model
{
    public $cmd;
    public $exp_year;
    public $exp_semester;
    public $schregno;
    public $name;
    public $grade;
    public $hrclass;
    public $hid_recordnamelist;
    public $hid__recactvidlist;
    public $idlist = array();
    public $field = array();
    public $record = array();
    public $gradecd;
    public $control;
    public $semeAll;


    public function knjd139iModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->gradecd);
        }

        $this->field = array("TOTALSTUDY_TITLE"      => VARS::post("TOTALSTUDY_TITLE"),
                             "TOTALSTUDY_EVAL"       => VARS::post("TOTALSTUDY_EVAL"),
                             "MORAL_TITLE"           => VARS::post("MORAL_TITLE"),
                             "MORAL_EVAL"            => VARS::post("MORAL_EVAL"),
                             "CLUB_FIRST"            => VARS::post("CLUB_FIRST"),
                             "CLUB_SCND"             => VARS::post("CLUB_SCND"),
                             "FAVORITE_ACTV"         => VARS::post("FAVORITE_ACTV"),
                             "REMARK_ATTEND"         => VARS::post("REMARK_ATTEND"),
                             "REMARK_TCTTL"          => VARS::post("REMARK_TCTTL"),
                             "REMARK_TEACHERCOMMENT" => VARS::post("REMARK_TEACHERCOMMENT"),
                             "SEMESTER"              => VARS::post("SEMESTER"),
                             "UPDATED"               => VARS::post("UPDATED")
                             );

        $this->hid__recactvidlist = VARS::post("HID_RECACTVIDLIST");
        $this->hid_recordnamelist = VARS::post("HID_RECACTVNAMELIST");
        if ($this->hid_recordnamelist) {
            $this->idlist = explode(",", $this->hid_recordnamelist);

            //生徒の記録
            $this->record["RECORD"] = array();
            for ($i = 0; $i < get_count($this->idlist); $i++) {
                $tmpval = $i + 1;
                $ival = sprintf("%02d", $tmpval);
                $this->record["RECORD"][$ival] = VARS::post($this->idlist[$i]);
            }
        }

        $this->cmd = VARS::request("cmd");
        $this->semeAll = "9";

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
        }
        if (VARS::post("SEMESTER")) {
            $this->exp_semester = VARS::post("SEMESTER");
        } elseif (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS") != "") {
            $this->hrclass = VARS::get("HR_CLASS");
        }
        if (VARS::post("HID_GRADE") != "") {
            $this->gradecd = VARS::post("HID_GRADE");
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
        if ($this->exp_semester == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
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
        knjd139iQuery::getInsertQuery($this);
        if ($this->hid_recordnamelist) {
            knjd139iQuery::getInsertQuery2($this);
        }
        knjd139iQuery::getInsertQuery3($this);
        //5、6年生ならば、表示されるので処理する。
        if (intval($this->gradecd) >= 5) {
            knjd139iQuery::getInsertQuery4($this);
        }

        return ;
    }
}
