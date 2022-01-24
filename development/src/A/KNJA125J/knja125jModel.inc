<?php

require_once("for_php7.php");

class knja125jModel extends Model
{
    public $cmd;
    public $exp_year;
    public $exp_semester;
    public $schregno;
    public $name;
    public $grade;
    public $field = array();
    public $record = array();
    public $control;
    public $kinJunior;

    public function knja125jModel()
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

        $this->field = array("TOTALSTUDYACT"        =>  VARS::post("TOTALSTUDYACT"),
                             "VIEWREMARK"           =>  VARS::post("VIEWREMARK"),
                             "TOTALSTUDYVAL"        =>  VARS::post("TOTALSTUDYVAL"),
                             "TOTALREMARK"          =>  VARS::post("TOTALREMARK"),
                             "TRAIN_REF1"           =>  VARS::post("TRAIN_REF1"),
                             "TRAIN_REF2"           =>  VARS::post("TRAIN_REF2"),
                             "TRAIN_REF3"           =>  VARS::post("TRAIN_REF3"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "DEVIATION"            =>  VARS::post("DEVIATION"),
                             "REMARK1_002"          =>  VARS::post("REMARK1_002"),
                             "REMARK1_009"          =>  VARS::post("REMARK1_009"),
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),
                             "TOTALSTUDYACT_BG_COLOR_FLG"       =>  VARS::post("TOTALSTUDYACT_BG_COLOR_FLG"),
                             "TOTALSTUDYVAL_BG_COLOR_FLG"       =>  VARS::post("TOTALSTUDYVAL_BG_COLOR_FLG"),
                             "VIEWREMARK_BG_COLOR_FLG"          =>  VARS::post("VIEWREMARK_BG_COLOR_FLG"),
                             "REMARK1"              =>  VARS::post("REMARK1"),
                             "REMARK1_BG_COLOR_FLG" =>  VARS::post("REMARK1_BG_COLOR_FLG"),
                             "CLASSACT"             =>  VARS::post("CLASSACT"),
                             "STUDENTACT"           =>  VARS::post("STUDENTACT"),
                             "SCHOOLEVENT"          =>  VARS::post("SCHOOLEVENT"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        //行動の記録
        for ($i=1; $i<11; $i++) {
            $ival = "1" . sprintf("%02d", $i);
            $this->record["RECORD"][$ival] = VARS::post("RECORD".$ival);
        }

        //特別活動の記録
        for ($i=1; $i<5; $i++) {
            $ival = "2" . sprintf("%02d", $i);
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
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //道徳
        if ($this->Properties["HTRAINREMARK_DETAIL2_DAT_004_REMARK1_SIZE_J"]) {
            list($moji, $gyou) = preg_split('/\*/', $this->Properties["HTRAINREMARK_DETAIL2_DAT_004_REMARK1_SIZE_J"]);
            $this->remark1_moji = (int)trim($moji);
            $this->remark1_gyou = (int)trim($gyou);
        } else {
            $this->remark1_moji = 32;
            $this->remark1_gyou = 2;
        }

        //総合所見（３分割）
        if ($this->Properties["train_ref_1_2_3_field_size_J"]) {
            list($moji1, $moji2, $moji3) = preg_split('/\-/', $this->Properties["train_ref_1_2_3_field_size_J"]);
            $this->train_ref1_moji = (int)trim($moji1);
            $this->train_ref2_moji = (int)trim($moji2);
            $this->train_ref3_moji = (int)trim($moji3);
        } else {
            $this->train_ref1_moji = 14;
            $this->train_ref2_moji = 14;
            $this->train_ref3_moji = 14;
        }
        if ($this->Properties["train_ref_1_2_3_gyo_size_J"]) {
            $gyou = $this->Properties["train_ref_1_2_3_gyo_size_J"];
            $this->train_ref1_gyou = (int)trim($gyou);
            $this->train_ref2_gyou = (int)trim($gyou);
            $this->train_ref3_gyou = (int)trim($gyou);
        } else {
            $this->train_ref1_gyou = 5;
            $this->train_ref2_gyou = 5;
            $this->train_ref3_gyou = 5;
        }

        if ($this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split('/\*/', $this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J"]);
            $this->totalremark_moji = (int)trim($moji);
            $this->totalremark_gyou = (int)trim($gyou);
        } else {
            $this->totalremark_moji = 44;
            $this->totalremark_gyou = 10;
        }

        if ($this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split('/\*/', $this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 35;
            $this->attendrec_remark_gyou = 1;
        }
        //学習活動
        if ($this->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J"]) {
            list($moji, $gyou) = preg_split('/\*/', $this->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J"]);
            $this->totalstudyact_moji = (int)trim($moji);
            $this->totalstudyact_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyact_moji = 5;
            $this->totalstudyact_gyou = 8;
        }
        //観点
        if ($this->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split('/\*/', $this->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_J"]);
            $this->viewremark_moji = (int)trim($moji);
            $this->viewremark_gyou = (int)trim($gyou);
        } else {
            $this->viewremark_moji = 10;
            $this->viewremark_gyou = 8;
        }
        //評価
        if ($this->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J"]) {
            list($moji, $gyou) = preg_split('/\*/', $this->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J"]);
            $this->totalstudyval_moji = (int)trim($moji);
            $this->totalstudyval_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyval_moji = 15;
            $this->totalstudyval_gyou = 8;
        }
        if ($this->Properties[" HTRAINREMARK_DAT_REMARK1_009_SIZE_J"]) {
            list($moji, $gyou) = preg_split('/\*/', $this->Properties[" HTRAINREMARK_DAT_REMARK1_009_SIZE_J"]);
            $this->remark009_moji = (int)trim($moji);
            $this->remark009_gyou = (int)trim($gyou);
        } else {
            $this->remark009_moji = 25;
            $this->remark009_gyou = 10;
        }
        $this->specialactrec_moji = '18';
        $this->specialactrec_gyou = '6';
        if ($this->Properties["tyousasyoSpecialactrecFieldSize_J"] != "") {
            $temp = explode('*', $this->Properties["tyousasyoSpecialactrecFieldSize_J"]);
            $this->specialactrec_moji = trim($temp[0]);
            $this->specialactrec_gyou = trim($temp[1]);
        }
        $this->clubact_moji = '16';
        $this->clubact_gyou = '6';
        if ($this->Properties["tyousasyoClubactFieldSize_J"] != "") {
            $temp = explode('*', $this->Properties["tyousasyoClubactFieldSize_J"]);
            $this->clubact_moji = trim($temp[0]);
            $this->clubact_gyou = trim($temp[1]);
        }
        $db = Query::dbCheckOut();
        $this->z010 = $db->getOne(knja125jQuery::getZ010());
        if ("sundaikoufu" == $this->z010) {
            $this->totalstudyact_moji = 7;
            $this->totalstudyact_gyou = 9;
            $this->viewremark_moji = 8;
            $this->viewremark_gyou = 9;
            $this->totalstudyval_moji = 15;
            $this->totalstudyval_gyou = 9;
        }
        $this->kinJunior = "";
        if ("KINJUNIOR" == $this->z010) {
            $this->kinJunior = "1";
        }
        $this->doutoku_classcd = "";
        if ($this->z010 == 'chiyoda') {
            $this->doutoku_classcd = "28";
        }
        $this->hreportremark = "";
        if ($this->z010 == 'rakunan') {
            $this->hreportremark = "REMARK2";
        }
        if ($this->z010 == 'teihachi') {
            $this->remark1_002_moji = 59;
            $this->remark1_002_gyou = 5;
        }
        //学校種別
        $this->schoolkind = $db->getOne(knja125jQuery::getSchoolKind($this));

        $this->kansai = $this->z010 == 'kwansei';
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        if ($this->validate_row_cnt($this->field["REMARK1"], ($this->remark1_moji * 2)) > $this->remark1_gyou) {
            $this->setWarning("MSG915", "(道徳は{$this->remark1_gyou}行までです)");
            return false;
        }

        if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], ($this->totalstudyact_moji * 2)) > $this->totalstudyact_gyou) {
            $this->setWarning("MSG915", "(学習活動は{$this->totalstudyact_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], ($this->totalstudyval_moji * 2)) > $this->totalstudyval_gyou) {
            $this->setWarning("MSG915", "(評価は{$this->totalstudyval_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["VIEWREMARK"], ($this->viewremark_moji * 2)) > $this->viewremark_gyou) {
            $this->setWarning("MSG915", "(観点は{$this->viewremark_gyou}行までです)");
            return false;
        }
        //総合所見（３分割）
        if ($this->Properties["train_ref_1_2_3_use_J"] == '1') {
            if ($this->validate_row_cnt($this->field["TRAIN_REF1"], ($this->train_ref1_moji * 2)) > $this->train_ref1_gyou) {
                $this->setWarning("MSG915", "(総合所見(1)(2))は{$this->train_ref1_gyou}行までです)");
                return false;
            }
            if ($this->validate_row_cnt($this->field["TRAIN_REF2"], ($this->train_ref2_moji * 2)) > $this->train_ref2_gyou) {
                $this->setWarning("MSG915", "(総合所見(3)(4))は{$this->train_ref2_gyou}行までです)");
                return false;
            }
            if ($this->validate_row_cnt($this->field["TRAIN_REF3"], ($this->train_ref3_moji * 2)) > $this->train_ref3_gyou) {
                $this->setWarning("MSG915", "(総合所見(5))は{$this->train_ref3_gyou}行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["TOTALREMARK"], ($this->totalremark_moji * 2)) > $this->totalremark_gyou) {
                $this->setWarning("MSG915", "(総合所見は{$this->totalremark_gyou}行までです)");
                return false;
            }
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
            $this->setWarning("MSG915", "(出欠の記録備考は{$this->attendrec_remark_gyou}行までです)");
            return false;
        }
        if ($this->z010 == "teihachi" && $this->validate_row_cnt($this->field["REMARK1_002"], ($this->remark1_002_moji * 2)) > $this->remark1_002_gyou) {
            $this->setWarning("MSG915", "(中学で履修済み備考は{$this->remark1_002_gyou}行までです)");
            return false;
        }
        if ($this->Properties["Specialactremark_3disp_J"] == '1') {
            $arr = array(
            "CLASSACT" => "学級活動",
            "STUDENTACT" => "生徒会活動",
            "SCHOOLEVENT" => "学校行事"
            );
            $moji = 17;
            $gyo = 3;
            foreach ($arr as $field => $name) {
                if ($this->validate_row_cnt($this->field[$field], ($moji * 2)) > $gyo) {
                    $this->setWarning("MSG915", "(".$name."は".$gyo."行までです)");
                    return false;
                }
            }
        } else {
            if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (17 * 2)) > 10) {
                $this->setWarning("MSG915", "(特別活動の記録の観点は10行までです)");
                return false;
            }
        }
        if ($this->Properties["Totalremark_2disp_J"] == '1') {
            if ($this->validate_row_cnt($this->field["REMARK1_009"], ($this->remark009_moji * 2)) > $this->remark009_gyou) {
                $this->setWarning("MSG915", "(総合所見及び指導上参考となる諸事項は{$this->remark009_gyou}行までです)");
                return false;
            }
        }

        return true;
    }

    //定型ボタン作成
    public function createTeikeiBtn(&$arg, &$objForm, $property, $title, $textbox)
    {
        $sendDataDivArr = explode("-", $property);
        if (get_count($sendDataDivArr) != 2) {
            return;
        }

        for ($i = 0; $i < 2; $i++) {
            $sendDataDiv = $sendDataDivArr[$i];
            $bangou = $i + 1;

            $extra  = " onclick=\"loadwindow('../../X/KNJX_TEIKEIBUN/knjx_teikeibunindex.php?";
            $extra .= "cmd=teikei&EXP_YEAR={$this->exp_year}&GRADE={$this->grade}&DATA_DIV={$sendDataDiv}&TITLE={$title}{$bangou}&TEXTBOX={$textbox}'";
            $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ";
            $arg["button"]["btn_teikei".$bangou."_".$textbox] = knjCreateBtn($objForm, "btn_teikei".$bangou, "定型文選択".$bangou, $extra);
        }
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
        knja125jQuery::getInsertQuery($this);

        return ;
    }

    public function &update2()
    {
        knja125jQuery::getInsertQuery2($this);

        return ;
    }
}
