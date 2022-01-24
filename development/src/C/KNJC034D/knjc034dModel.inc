<?php

require_once('for_php7.php');

class knjc034dModel extends Model
{
    public $fields = array();
    public $cmd;
    public $hr_class_type;
    public $grade_mix;
    public $grade_hr_class;
    public $month_sem;
    public $data_cnt;
    public $day_from;
    public $day_to;
    public $control;

    public function knjc034dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::post("SCHOOL_KIND")) {
            $this->setSchoolKind = VARS::post("SCHOOL_KIND");
        }

        $this->cmd              = VARS::request("cmd");
        $this->hr_class_type    = VARS::request("HR_CLASS_TYPE");
        $this->grade_mix        = VARS::request("GRADE_MIX");
        $this->grade_hr_class   = VARS::request("GRADE_HR_CLASS");
        $this->month_sem        = VARS::request("MONTH_SEM");
        $this->data_cnt         = VARS::post("DATA_CNT");
        $this->day_from         = VARS::post("DAY_FROM");
        $this->day_to           = VARS::post("DAY_TO");
        $this->last_month       = VARS::post("LAST_MONTH");

        if ($this->cmd == "update") {
            if (VARS::post("HIDDEN_SCHOOL_KIND")) {
                $this->setSchoolKind = VARS::post("HIDDEN_SCHOOL_KIND");
            }
            $this->hr_class_type    = VARS::post("HIDDEN_HR_CLASS_TYPE");
            $this->grade_mix        = VARS::post("HIDDEN_GRADE_MIX");
            $this->grade_hr_class   = VARS::post("HIDDEN_GRADE_HR_CLASS");
            $this->month_sem        = VARS::post("HIDDEN_MONTH_SEM");
        }

        //初期化
        $this->fields = array();

        if ($this->cmd == 'update') {
            //月、学期
            list($month, $sem) = explode('-', $this->month_sem);
            //年取得
            $year = ((int)$month < 4) ? CTRL_YEAR + 1 : CTRL_YEAR;

            for ($g = 0; $g < $this->data_cnt; $g++) {
                $this->fields["SCHREGNO"][]         =  VARS::post("SCHREGNO"."_".$g);
                $this->fields["ATTENDNO"][]         =  VARS::post("ATTENDNO"."_".$g);
                $this->fields["NAME_SHOW"][]        =  VARS::post("NAME_SHOW"."_".$g);
                $this->fields["REMARK"][]           =  VARS::post("REMARK"."_".$g);
                $this->fields["LESSONCNT"][]        =  VARS::post("LESSONCNT"."_".$g);
                $this->fields["IDOU_TYPE"][]        =  VARS::post("IDOU_TYPE"."_".$g);

                for ($i = $this->day_from; $i <= $this->day_to; $i++) {
                    //日付
                    $date = $year."-".$month."-".sprintf('%02d', $i);
                    //出欠データセット
                    $this->fields[$date][] =  VARS::post($date."_".$g);
                }
            }

            //法定クラスのみ
            if ($this->hr_class_type == "1" && $this->grade_mix != "1") {
                for ($i = $this->day_from; $i <= $this->day_to; $i++) {
                    //日付
                    $date = $year."-".$month."-".sprintf('%02d', $i);
                    //出欠状況データセット
                    $this->fields["EXECUTED"][$date] =  VARS::post("EXECUTED"."_".$date);
                }
            }
        }

        //一括変更
        if (VARS::get("REP_ITEM")) {
            $this->replaceItem      = VARS::get("REP_ITEM");
            $this->replaceCounter   = VARS::get("COUNTER");
        }
        if (VARS::get("TYPE")) {
            $this->replaceTYPE      = VARS::get("TYPE");
        }
        if (VARS::get("MIX") != "") {
            $this->replaceMIX       = (VARS::get("MIX") == "1") ? "1" : "";
        }
        if (VARS::get("GHR")) {
            $this->replaceGHR       = VARS::get("GHR");
        }
        if (VARS::get("SEM")) {
            $this->replaceSEM       = VARS::get("SEM");
        }

        $this->replaceValue     = VARS::post("REP_VALUE");
        $this->checked          = VARS::request("CHECKED");
    }

    //入力項目チェック
    public function validateInput()
    {
        if (!$this->grade_hr_class) {
            $this->setWarning("MSG304");
            return false;
        }
        if (!$this->month_sem) {
            $this->setWarning("MSG304");
            return false;
        }
        if (!$this->data_cnt) {
            $this->setWarning("MSG304");
            return false;
        }

        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjc034dQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
