<?php

require_once('for_php7.php');

class knja128pModel extends Model
{
    public $cmd;
    public $exp_year;
    public $exp_semester;
    public $schregno;
    public $name;
    public $field = array();
    public $record = array();
    public $control;
    public $grade;
    public $z010;

    public function knja128pModel()
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
        }
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }

        $this->field = array("TOTALSTUDYACT"            =>  VARS::post("TOTALSTUDYACT"),
                             "TOTALSTUDYVAL"            =>  VARS::post("TOTALSTUDYVAL"),
                             "VIEWREMARK"               =>  VARS::post("VIEWREMARK"),
                             "TOTALREMARK"              =>  VARS::post("TOTALREMARK"),
                             "FOREIGNLANGACT1"          =>  VARS::post("FOREIGNLANGACT1"),
                             "FOREIGNLANGACT1_2"        =>  VARS::post("FOREIGNLANGACT1_2"),
                             "FOREIGNLANGACT2"          =>  VARS::post("FOREIGNLANGACT2"),
                             "FOREIGNLANGACT3"          =>  VARS::post("FOREIGNLANGACT3"),
                             "ATTENDREC_REMARK"         =>  VARS::post("ATTENDREC_REMARK"),
                             "INDEPENDENT_REMARK"       =>  VARS::post("INDEPENDENT_REMARK"),
                             "ENT_DISABILITY_REMARK"    =>  VARS::post("ENT_DISABILITY_REMARK"),
                             "FOREIGNLANGACT4"          =>  VARS::post("FOREIGNLANGACT4"),
                             "SPECIALACTREMARK"         =>  VARS::post("SPECIALACTREMARK"),
                             "UPDATED"                  =>  VARS::post("UPDATED")
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

        //自立活動の記録
        if ($this->Properties["HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_P"]);
            $this->indep_remark_moji = (int)trim($moji);
            $this->indep_remark_gyou = (int)trim($gyou);
        } else {
            $this->indep_remark_moji = 22;
            $this->indep_remark_gyou = 7;
        }
        //総合所見及び指導上参考となる諸事項
        if ($this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_P"]);
            $this->totalremark_moji = (int)trim($moji);
            $this->totalremark_gyou = (int)trim($gyou);
        } else {
            $this->totalremark_moji = 28;
            $this->totalremark_gyou = 8;
        }
        //総合的な学習の時間の記録（学習活動）
        if ($this->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_P"]);
            $this->totalstudyact_moji = (int)trim($moji);
            $this->totalstudyact_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyact_moji = 8;
            $this->totalstudyact_gyou = 9;
        }
        //総合的な学習の時間の記録（観点）
        if ($this->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_P"]);
            $this->viewremark_moji = (int)trim($moji);
            $this->viewremark_gyou = (int)trim($gyou);
        } else {
            $this->viewremark_moji = 8;
            $this->viewremark_gyou = 9;
        }
        //総合的な学習の時間の記録（評価）
        if ($this->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_P"]);
            $this->totalstudyval_moji = (int)trim($moji);
            $this->totalstudyval_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyval_moji = 13;
            $this->totalstudyval_gyou = 9;
        }
        //入学時の障害の状態
        if ($this->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_P"]);
            $this->disability_moji = (int)trim($moji);
            $this->disability_gyou = (int)trim($gyou);
        } else {
            $this->disability_moji = 9;
            $this->disability_gyou = 15;
        }
        //出欠の記録備考
        if ($this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 37;
            $this->attendrec_remark_gyou = 1;
        }
        //特別活動の記録の観点
        if ($this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P"]);
            $this->specialactremark_moji = (int)trim($moji);
            $this->specialactremark_gyou = (int)trim($gyou);
        } else {
            $this->specialactremark_moji = 12;
            $this->specialactremark_gyou = 10;
        }
        // 道徳
        if ($this->Properties["HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P"]);
            $this->foreignlangact4_moji = (int)trim($moji);
            $this->foreignlangact4_gyou = (int)trim($gyou);
        } else {
            $this->foreignlangact4_moji = 35;
            $this->foreignlangact4_gyou = 2;
        }
        // 外国語活動の記録2020年度
        if ($this->Properties["HTRAINREMARK_P_DAT_FOREIGNLANGACT_SIZE_P2020"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_P_DAT_FOREIGNLANGACT_SIZE_P2020"]);
            $this->foreignlangact_2020_moji = (int)trim($moji);
            $this->foreignlangact_2020_gyou = (int)trim($gyou);
        } else {
            $this->foreignlangact_2020_moji = 32;
            $this->foreignlangact_2020_gyou = 4;
        }
        // 2020年度から外国語欄はひとつ
        $startyear = strlen($this->Properties["seitoSidoYoroku3KantenStartYearP"]) ? (int)($this->Properties["seitoSidoYoroku3KantenStartYearP"]) : 2020;
        $this->is_gaikokugo1 = $this->exp_year == '' || $this->exp_year >= $startyear;

        $this->useForeignlangActFlg = false;
        if (isset($this->Properties["use_ForeignlangAct"])) {
            $list = explode(',', $this->Properties["use_ForeignlangAct"]);
            $this->useForeignlangActFlg = in_array($this->grade, $list);
        }
        $this->useTotalStudyValFlg = false;
        if (isset($this->Properties["use_TotalStudyVal"])) {
            $list = explode(',', $this->Properties["use_TotalStudyVal"]);
            $this->useTotalStudyValFlg = in_array($this->grade, $list);
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //文字数チェック
        if ($this->is_gaikokugo1) {
            if ($this->validate_row_cnt($this->field["FOREIGNLANGACT1_2"], ($this->foreignlangact_2020_moji * 2)) > $this->foreignlangact_2020_gyou) {
                $this->setWarning("MSG915", "(知識・技能　思考・判断・表現　主体的に学習に取り組む態度は".$this->foreignlangact_2020_gyou."行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["FOREIGNLANGACT1"], (11 * 2)) > 4) {
                $this->setWarning("MSG915", "(コミュニケーションへの関心・意欲・態度は4行までです)");
                $this->message915="FOREIGNLANGACT1";
                return false;
            }
            if ($this->validate_row_cnt($this->field["FOREIGNLANGACT2"], (11 * 2)) > 4) {
                $this->setWarning("MSG915", "(外国語への慣れ親しみは4行までです)");
                $this->message915="FOREIGNLANGACT2";
                return false;
            }
            if ($this->validate_row_cnt($this->field["FOREIGNLANGACT3"], (11 * 2)) > 4) {
                $this->setWarning("MSG915", "(言語や文化に関する気付きは4行までです)");
                $this->message915="FOREIGNLANGACT3";
                return false;
            }
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], ((int)$this->totalstudyact_moji * 2)) > $this->totalstudyact_gyou) {
            $this->setWarning("MSG915", "(学習活動は{$this->totalstudyact_gyou}行までです)");
            $this->message915="TOTALSTUDYACT";
            return false;
        }
        if ($this->validate_row_cnt($this->field["VIEWREMARK"], ((int)$this->viewremark_moji * 2)) > $this->viewremark_gyou) {
            $this->setWarning("MSG915", "(観点は{$this->viewremark_gyou}行までです)");
            $this->message915="VIEWREMARK";
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], ((int)$this->totalstudyval_moji * 2)) > $this->totalstudyval_gyou) {
            $this->setWarning("MSG915", "(評価は{$this->totalstudyval_gyou}行までです)");
            $this->message915="TOTALSTUDYVAL";
            return false;
        }
        if ($this->validate_row_cnt($this->field["INDEPENDENT_REMARK"], ((int)$this->indep_remark_moji * 2)) > $this->indep_remark_gyou) {
            $this->setWarning("MSG915", "(自立活動の記録は{$this->indep_remark_gyou}行までです)");
            $this->message915="INDEPENDENT_REMARK";
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALREMARK"], ((int)$this->totalremark_moji * 2)) > $this->totalremark_gyou) {
            $this->setWarning("MSG915", "(総合所見は{$this->totalremark_gyou}行までです)");
            $this->message915="TOTALREMARK";
            return false;
        }
        if ($this->validate_row_cnt($this->field["ENT_DISABILITY_REMARK"], ((int)$this->disability_moji * 2)) > $this->disability_gyou) {
            $this->setWarning("MSG915", "(入学時の障害の状態は{$this->disability_gyou}行までです)");
            $this->message915="ENT_DISABILITY_REMARK";
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ((int)$this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
            $this->setWarning("MSG915", "(出欠の記録備考は{$this->attendrec_remark_gyou}行までです)");
            $this->message915="ATTENDREC_REMARK";
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], ((int)$this->specialactremark_moji * 2)) > $this->specialactremark_gyou) {
            $this->setWarning("MSG915", "(特別活動の記録の観点は{$this->specialactremark_gyou}行までです)");
            $this->message915="SPECIALACTREMARK";
            return false;
        }

        if ($this->validate_row_cnt($this->field["FOREIGNLANGACT4"], ((int)$this->foreignlangact4_moji * 2)) > $this->foreignlangact4_gyou) {
            $this->setWarning("MSG915", "(道徳は{$this->foreignlangact4_gyou}行までです)");
            $this->message915="FOREIGNLANGACT4";
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
        knja128pQuery::getInsertQuery($this);
        return ;
    }

    public function &update2()
    {
        knja128pQuery::getInsertQuery2($this);
        return ;
    }
}
