<?php

require_once('for_php7.php');
class knja121cModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $grade;
    var $field = array();
    var $record = array();
    var $control;

    function knja121cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
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
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),
                             "TOTALSTUDYACT_BG_COLOR_FLG"       =>  VARS::post("TOTALSTUDYACT_BG_COLOR_FLG"),
                             "TOTALSTUDYVAL_BG_COLOR_FLG"       =>  VARS::post("TOTALSTUDYVAL_BG_COLOR_FLG"),
                             "UPDATED"                          =>  VARS::post("UPDATED")
                             );

        //行動の記録
        for($i=1; $i<11; $i++)
        {
            $ival = "1" . sprintf("%02d", $i);
            $this->record["RECORD"][$ival] = VARS::post("RECORD".$ival);
        }

        //特別活動の記録
        for($i=1; $i<5; $i++)
        {
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

        //学習活動
//        if ($this->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J"]) {
//            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J"]);
//            $this->totalstudyact_moji = (int)trim($moji);
//            $this->totalstudyact_gyou = (int)trim($gyou);
//        } else {
            $this->totalstudyact_moji = 20;
            $this->totalstudyact_gyou = 4;
//        }
        
//        //観点
//        if ($this->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_J"]) {
//            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_J"]);
//            $this->viewremark_moji = (int)trim($moji);
//            $this->viewremark_gyou = (int)trim($gyou);
//        } else {
//            $this->viewremark_moji = 10;
//            $this->viewremark_gyou = 4;
//        }
        
//        //評価
//        if ($this->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J"]) {
//            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J"]);
//            $this->totalstudyval_moji = (int)trim($moji);
//            $this->totalstudyval_gyou = (int)trim($gyou);
//        } else {
            $this->totalstudyval_moji = 35;
            $this->totalstudyval_gyou = 4;
//        }
        
//        //総合所見及び指導上参考となる諸事項
//        if ($this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J"]) {
//            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J"]);
//            $this->totalremark_moji = (int)trim($moji);
//            $this->totalremark_gyou = (int)trim($gyou);
//        } else {
            $this->totalremark_moji = 15;
            $this->totalremark_gyou = 29;
//        }
        
//        //出欠の記録備考
//        if ($this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]) {
//            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]);
//            $this->attendrec_remark_moji = (int)trim($moji);
//            $this->attendrec_remark_gyou = (int)trim($gyou);
//        } else {
            $this->attendrec_remark_moji = 18;
            $this->attendrec_remark_gyou = 2;
//        }
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], ($this->totalstudyact_moji * 2)) > $this->totalstudyact_gyou) {
            $this->setWarning("MSG915","(学習活動は{$this->totalstudyact_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], ($this->totalstudyval_moji * 2)) > $this->totalstudyval_gyou) {
            $this->setWarning("MSG915","(評価は{$this->totalstudyval_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["VIEWREMARK"], ($this->viewremark_moji * 2)) > $this->viewremark_gyou) {
            $this->setWarning("MSG915","(観点は{$this->viewremark_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALREMARK"], ($this->totalremark_moji * 2)) > $this->totalremark_gyou) {
            $this->setWarning("MSG915","(総合所見は{$this->totalremark_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
            $this->setWarning("MSG915","(出欠の記録備考は{$this->attendrec_remark_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (17 * 2)) > 10) {
            $this->setWarning("MSG915","(特別活動の記録の観点は10行までです)");
            return false;
        }

        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        if ($this->cmd == "update") $this->update();
        if ($this->cmd == "update2") $this->update2();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        knja121cQuery::getInsertQuery($this);

        return ;
    }

    function &update2()
    {
        knja121cQuery::getInsertQuery2($this);

        return ;
    }
}
?>
