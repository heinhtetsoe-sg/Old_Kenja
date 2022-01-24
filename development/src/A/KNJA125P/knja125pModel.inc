<?php

require_once('for_php7.php');

class knja125pModel extends Model
{

    public $cmd;
    public $exp_year;
    public $exp_semester;
    public $schregno;
    public $name;
    public $grade;
    public $gradeCd;
    public $field = array();
    public $record = array();
    public $control;

    public function knja125pModel()
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
            unset($this->gradeCd);
        }

        $this->field = array("TOTALSTUDYACT"                =>  VARS::post("TOTALSTUDYACT"),
                             "TOTALSTUDYVAL"                =>  VARS::post("TOTALSTUDYVAL"),
                             "TOTALREMARK"                  =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"             =>  VARS::post("ATTENDREC_REMARK"),
                             "VIEWREMARK"                   =>  VARS::post("VIEWREMARK"),
                             "FOREIGNLANGACT1"              =>  VARS::post("FOREIGNLANGACT1"),
                             "FOREIGNLANGACT1_2"            =>  VARS::post("FOREIGNLANGACT1_2"),
                             "FOREIGNLANGACT2"              =>  VARS::post("FOREIGNLANGACT2"),
                             "FOREIGNLANGACT3"              =>  VARS::post("FOREIGNLANGACT3"),
                             "SPECIALACTREMARK"             =>  VARS::post("SPECIALACTREMARK"),
                             "FOREIGNLANGACT4"              =>  VARS::post("FOREIGNLANGACT4"),
                             "FOREIGNLANGACT4_BG_COLOR_FLG" =>  VARS::post("FOREIGNLANGACT4_BG_COLOR_FLG"),
                             "UPDATED"                      =>  VARS::post("UPDATED")
                             );

        //行動の記録
        for ($i=1; $i<11; $i++) {
            $ival = "3" . sprintf("%02d", $i);
            $this->record["RECORD"][$ival] = VARS::post("RECORD".$ival);
        }

        //特別活動の記録
        for ($i=1; $i < get_count($this->itemArray) + 1; $i++) {
            $ival = "4" . sprintf("%02d", $i);
            $this->record["RECORD"][$ival] = VARS::post("RECORD".$ival);
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
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

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->gradeCd);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if ($this->Properties["HTRAINREMARK_P_DAT_ATTENDREC_REMARK_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_P_DAT_ATTENDREC_REMARK_SIZE_P"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 35;
            $this->attendrec_remark_gyou = 1;
        }

        if ($this->Properties["HTRAINREMARK_P_DAT_FOREIGNLANGACT4_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_P_DAT_FOREIGNLANGACT4_SIZE_P"]);
            $this->foreignlangact4_moji = (int)trim($moji);
            $this->foreignlangact4_gyou = (int)trim($gyou);
        } else {
            $this->foreignlangact4_moji = 32;
            $this->foreignlangact4_gyou = 2;
        }

        if ($this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P"]);
            $this->specialactremark_moji = (int)trim($moji);
            $this->specialactremark_gyou = (int)trim($gyou);
        } else {
            $this->specialactremark_moji = 16;
            $this->specialactremark_gyou = 4;
        }

        if ($this->Properties["HTRAINREMARK_P_DAT_TOTALSTUDYACT_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_P_DAT_TOTALSTUDYACT_SIZE_P"]);
            $this->totalstudyact_moji = (int)trim($moji);
            $this->totalstudyact_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyact_moji = 8;
            $this->totalstudyact_gyou = 8;
        }

        if ($this->Properties["HTRAINREMARK_P_DAT_VIEWREMARK_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_P_DAT_VIEWREMARK_SIZE_P"]);
            $this->viewremark_moji = (int)trim($moji);
            $this->viewremark_gyou = (int)trim($gyou);
        } else {
            $this->viewremark_moji = 7;
            $this->viewremark_gyou = 8;
        }

        if ($this->Properties["HTRAINREMARK_P_DAT_TOTALSTUDYVAL_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_P_DAT_TOTALSTUDYVAL_SIZE_P"]);
            $this->totalstudyval_moji = (int)trim($moji);
            $this->totalstudyval_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyval_moji = 15;
            $this->totalstudyval_gyou = 8;
        }

        if ($this->Properties["HTRAINREMARK_P_DAT_TOTALREMARK_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_P_DAT_TOTALREMARK_SIZE_P"]);
            $this->totalremark_moji = (int)trim($moji);
            $this->totalremark_gyou = (int)trim($gyou);
        } else {
            $this->totalremark_moji = 22;
            $this->totalremark_gyou = 15;
        }

        $db = Query::dbCheckOut();

        //学校チェック
        $query = knja125pQuery::getZ010();
        $z010 = $db->getOne($query);
        $this->isMusashinohigashi = "musashinohigashi" == $z010;
        $this->isOsakashinnai = "osakashinnai" == $z010;

        if ($this->grade) {
            $this->gradeCd = $db->getOne(knja125pQuery::getGradeCd2($this));
        }

        Query::dbCheckIn($db);

        // 2020年度から外国語欄はひとつ
        $this->is_gaikokugo1 = $this->exp_year >= 2020;
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], ((int)$this->totalstudyact_moji * 2)) > $this->totalstudyact_gyou) {
            $this->setWarning("MSG915", "(学習活動は{$this->totalstudyact_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], ((int)$this->totalstudyval_moji * 2)) > $this->totalstudyval_gyou) {
            $this->setWarning("MSG915", "(評価は{$this->totalstudyval_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["VIEWREMARK"], (int)($this->viewremark_moji * 2)) > $this->viewremark_gyou) {
            $this->setWarning("MSG915", "(観点は{$this->viewremark_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALREMARK"], ((int)$this->totalremark_moji * 2)) > $this->totalremark_gyou) {
            $this->setWarning("MSG915", "(総合所見は{$this->totalremark_gyou}行までです)");
            return false;
        }

        if ($this->is_gaikokugo1) {
            if ($this->validate_row_cnt($this->field["FOREIGNLANGACT1_2"], (32 * 2)) > 4) {
                $this->setWarning("MSG915", "(知識・技能　思考・判断・表現　主体的に学習に取り組む態度は4行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["FOREIGNLANGACT1"], (10 * 2)) > 4) {
                $this->setWarning("MSG915", "(コミュニケーションへの関心・意欲・態度は4行までです)");
                return false;
            }
            if ($this->validate_row_cnt($this->field["FOREIGNLANGACT2"], (10 * 2)) > 4) {
                $this->setWarning("MSG915", "(外国語への慣れ親しみは4行までです)");
                return false;
            }
            if ($this->validate_row_cnt($this->field["FOREIGNLANGACT3"], (10 * 2)) > 4) {
                $this->setWarning("MSG915", "(言語や文化に関する気付きは4行までです)");
                return false;
            }
        }

        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ((int)$this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
            $this->setWarning("MSG915", "(出欠の記録備考は{$this->attendrec_remark_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (17 * 2)) > 10) {
            $this->setWarning("MSG915", "(特別活動の記録の観点は10行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ((int)$this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
            $this->setWarning("MSG915", "(出欠の記録備考は{$this->attendrec_remark_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["FOREIGNLANGACT4"], ((int)$this->foreignlangact4_moji * 2)) > $this->foreignlangact4_gyou) {
            $this->setWarning("MSG915", "(道徳は{$this->foreignlangact4_gyou}行までです)");
            return false;
        }

        return true;
    }

    public function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        if ($this->cmd == "update") {
            $this->update();
        }
        if ($this->cmd == "update2") {
            $this->update2();
        }
        $this->setMessage("MSG201");
    }

    public function &update()
    {
        knja125pQuery::getInsertQuery($this);

        return ;
    }

    public function &update2()
    {
        knja125pQuery::getInsertQuery2($this);

        return ;
    }
}
