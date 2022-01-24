<?php

require_once('for_php7.php');

class knja128jModel extends Model {
    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $record = array();
    var $control;

    function knja128jModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        $this->field = array("TOTALSTUDYACT"            =>  VARS::post("TOTALSTUDYACT"),
                             "VIEWREMARK"               =>  VARS::post("VIEWREMARK"),
                             "TOTALSTUDYVAL"            =>  VARS::post("TOTALSTUDYVAL"),
                             "INDEPENDENT_REMARK"       =>  VARS::post("INDEPENDENT_REMARK"),
                             "ENT_DISABILITY_REMARK"    =>  VARS::post("ENT_DISABILITY_REMARK"),
                             "TOTALREMARK"              =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"         =>  VARS::post("ATTENDREC_REMARK"),
                             "SPECIALACTREMARK"         =>  VARS::post("SPECIALACTREMARK"),
                             "REMARK1"                  =>  VARS::post("REMARK1"),
                             "REMARK1_BG_COLOR_FLG"     =>  VARS::post("REMARK1_BG_COLOR_FLG")
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

        //文字数・行数設定
        //道徳
        if ($this->Properties["HTRAINREMARK_DAT_REMARK1_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_REMARK1_SIZE_P"]);
            $this->remark1_moji = (int)trim($moji);
            $this->remark1_gyou = (int)trim($gyou);
        } else {
            $this->remark1_moji = 32;
            $this->remark1_gyou = 2;
        }
        //学習活動
        if ($this->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J"]);
            $this->totalstudyact_moji = (int)trim($moji);
            $this->totalstudyact_gyou = (int)trim($gyou);
        } else { 
            $this->totalstudyact_moji = 7;
            $this->totalstudyact_gyou = 10;
        }
        //観点
        if ($this->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_J"]);
            $this->viewremark_moji = (int)trim($moji);
            $this->viewremark_gyou = (int)trim($gyou);
        } else {  
            $this->viewremark_moji = 7;
            $this->viewremark_gyou = 10;
        }
        //評価
        if ($this->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J"]);
            $this->totalstudyval_moji = (int)trim($moji);
            $this->totalstudyval_gyou = (int)trim($gyou);
        } else {  
            $this->totalstudyval_moji = 13;
            $this->totalstudyval_gyou = 10;
        }
        //自立活動の記録
        if ($this->Properties["HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_J"]);
            $this->indep_remark_moji = (int)trim($moji);
            $this->indep_remark_gyou = (int)trim($gyou);
        } else {   
            $this->indep_remark_moji = 48;
            $this->indep_remark_gyou = 7;
        }
        //入学時の障害の状態
        if ($this->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J"]);
            $this->disability_moji = (int)trim($moji);
            $this->disability_gyou = (int)trim($gyou);
        } else { 
            $this->disability_moji = 9;
            $this->disability_gyou = 15;
        }
        //総合所見及び指導上参考となる諸事項
        if ($this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J"]);
            $this->totalremark_moji = (int)trim($moji);
            $this->totalremark_gyou = (int)trim($gyou);
        } else {
            $this->totalremark_moji = 60;
            $this->totalremark_gyou = 8;
        }
        //出欠の記録備考
        if ($this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 38;
            $this->attendrec_remark_gyou = 2;
        }
        //特別活動の記録の観点
        if ($this->Properties["HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_J"]);
            $this->specialactremark_moji = (int)trim($moji);
            $this->specialactremark_gyou = (int)trim($gyou);
        } else { 
            $this->specialactremark_moji = 14;
            $this->specialactremark_gyou = 7;
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //文字数・行数チェック
        if ($this->validate_row_cnt($this->field["REMARK1"], ((int)$this->remark1_moji * 2)) > $this->remark1_gyou) {
            $this->setWarning("MSG915","(道徳は{$this->remark1_gyou}行までです)");
            // Add by HPA for textarea_cursor start 2020/01/20
            $this->message915="REMARK1";
            // Add by HPA for textarea_cursor end 2020/01/31
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], ((int)$this->totalstudyact_moji * 2)) > $this->totalstudyact_gyou) {
            $this->setWarning("MSG915","(学習活動は{$this->totalstudyact_gyou}行までです)");
            // Add by HPA for textarea_cursor start 2020/01/20
            $this->message915="TOTALSTUDYACT";
            // Add by HPA for textarea_cursor end 2020/01/31
            return false;
        }
        if ($this->validate_row_cnt($this->field["VIEWREMARK"], ((int)$this->viewremark_moji * 2)) > $this->viewremark_gyou) {
            $this->setWarning("MSG915","(観点は{$this->viewremark_gyou}行までです)");
            // Add by HPA for textarea_cursor start 2020/01/20
            $this->message915="VIEWREMARK";
            // Add by HPA for textarea_cursor end 2020/01/31
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], ((int)$this->totalstudyval_moji * 2)) > $this->totalstudyval_gyou) {
            $this->setWarning("MSG915","(評価は{$this->totalstudyval_gyou}行までです)");
            // Add by HPA for textarea_cursor start 2020/01/20
            $this->message915="TOTALSTUDYVAL";
            // Add by HPA for textarea_cursor end 2020/01/31
            return false;
        }
        if ($this->validate_row_cnt($this->field["INDEPENDENT_REMARK"], ((int)$this->indep_remark_moji * 2)) > $this->indep_remark_gyou) {
            $this->setWarning("MSG915","(自立活動の記録は{$this->indep_remark_gyou}行までです)");
            // Add by HPA for textarea_cursor start 2020/01/20
            $this->message915="INDEPENDENT_REMARK";
            // Add by HPA for textarea_cursor end 2020/01/31
            return false;
        }
        if ($this->validate_row_cnt($this->field["ENT_DISABILITY_REMARK"], ((int)$this->disability_moji * 2)) > $this->disability_gyou) {
            $this->setWarning("MSG915","(入学時の障害の状態は{$this->disability_gyou}行までです)");
            // Add by HPA for textarea_cursor start 2020/01/20
            $this->message915="ENT_DISABILITY_REMARK";
            // Add by HPA for textarea_cursor end 2020/01/31
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALREMARK"], ((int)$this->totalremark_moji * 2)) > $this->totalremark_gyou) {
            $this->setWarning("MSG915","(総合所見は{$this->totalremark_gyou}行までです)");
            // Add by HPA for textarea_cursor start 2020/01/20
            $this->message915="TOTALREMARK";
            // Add by HPA for textarea_cursor end 2020/01/31
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ((int)$this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
            $this->setWarning("MSG915","(出欠の記録備考は{$this->attendrec_remark_gyou}行までです)");
            // Add by HPA for textarea_cursor start 2020/01/20
            $this->message915="ATTENDREC_REMARK";
            // Add by HPA for textarea_cursor end 2020/01/31
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], ((int)$this->specialactremark_moji * 2)) > $this->specialactremark_gyou) {
            $this->setWarning("MSG915","(特別活動の記録の観点は{$this->specialactremark_gyou}行までです)");
            // Add by HPA for textarea_cursor start 2020/01/20
            $this->message915="SPECIALACTREMARK";
            // Add by HPA for textarea_cursor end 2020/01/31
            return false;
        }

        return true;
    }

    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        if ($this->cmd == "update") $this->update();
        if ($this->cmd == "update2") $this->update2();
        $this->setMessage("MSG201");
    }

    function &update() {
        knja128jQuery::getInsertQuery($this);
        return ;
    }

    function &update2() {
        knja128jQuery::getInsertQuery2($this);
        return ;
    }
}
?>
