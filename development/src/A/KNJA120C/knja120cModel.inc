<?php

require_once('for_php7.php');

class knja120cModel extends Model {

    var $cmd;
    var $exp_year = "";
    var $exp_semester = "";
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $control;
    var $annual;        //学年(年次)
    var $getPrgId;      //コールされたか
    var $sendAuth;      //親画面の権限
    var $auth;          //権限


    function knja120cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
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

        if (VARS::post("ANNUAL")){   //学年（年次）
            list($tmp["YEAR"], $tmp["ANNUAL"]) = explode(",", VARS::post("ANNUAL"));
            $this->annual = $tmp;
        }

        $this->field = array("TOTALSTUDYACT"        =>  VARS::post("TOTALSTUDYACT"),
                             "TOTALSTUDYVAL"        =>  VARS::post("TOTALSTUDYVAL"),
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),
                             "TOTALREMARK"          =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "UPDATED"              =>  VARS::post("UPDATED")
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
            unset($this->annual);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        //年度コンボ（通知表所見）
        if ($this->Properties["useTuutisyoSyokenNendo"] == "1") {
            $this->year_cmb = VARS::request("YEAR_CMB");
        } else {
            $this->year_cmb = $this->exp_year;
        }

        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]);
            $this->totalstudyact_moji = (int)trim($moji);
            $this->totalstudyact_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyact_moji = 23; //デフォルトの値
            $this->totalstudyact_gyou = 5;  //デフォルトの値
        }
        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]);
            $this->totalstudyval_moji = (int)trim($moji);
            $this->totalstudyval_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyval_moji = 23; //デフォルトの値
            $this->totalstudyval_gyou = 6;  //デフォルトの値
        }
        if ($this->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 20; //デフォルトの値
            $this->attendrec_remark_gyou = 2;  //デフォルトの値
        }
        if ($this->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalremarkSize"]);
            $this->totalremark_moji = (int)trim($moji);
            $this->totalremark_gyou = (int)trim($gyou);
        } else {
            $this->totalremark_moji = 44; //デフォルトの値
            $this->totalremark_gyou = 6;  //デフォルトの値
        }
        if ($this->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]);
            $this->specialactremark_moji = (int)trim($moji);
            $this->specialactremark_gyou = (int)trim($gyou);
        } else {
            $this->specialactremark_moji = 11; //デフォルトの値
            $this->specialactremark_gyou = 6;  //デフォルトの値
        }
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], ((int)$this->totalstudyact_moji * 2)) > $this->totalstudyact_gyou) {
                $this->setWarning("MSG915","(活動内容は{$this->totalstudyact_gyou}行までです)");
                return false;
            }
        } else if ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
            if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], (22 * 2)) > 8) {
                $this->setWarning("MSG915","(活動内容は8行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], (11 * 2)) > 4) {
                $this->setWarning("MSG915","(活動内容は4行までです)");
                return false;
            }
        }

        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], ((int)$this->totalstudyval_moji * 2)) > $this->totalstudyval_gyou) {
                $this->setWarning("MSG915","(評価は{$this->totalstudyval_gyou}行までです)");
                return false;
            }
        } else if ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
            if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], (22 * 2)) > 8) {
                $this->setWarning("MSG915","(評価は8行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], (11 * 2)) > 6) {
                $this->setWarning("MSG915","(評価は6行までです)");
                return false;
            }
        }

        if ($this->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]) {
            if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ((int)$this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
                $this->setWarning("MSG915","(出欠の記録備考は{$this->attendrec_remark_gyou}行までです)");
                return false;
            }
        } else if ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
            if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], (40 * 2)) > 2) {
                $this->setWarning("MSG915","(出欠の記録備考は2行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], (20 * 2)) > 2) {
                $this->setWarning("MSG915","(出欠の記録備考は2行までです)");
                return false;
            }
        }

        if ($this->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]) {
            if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], ((int)$this->specialactremark_moji * 2)) > $this->specialactremark_gyou) {
                $this->setWarning("MSG915","(特別活動所見は{$this->specialactremark_gyou}行までです)");
                return false;
            }
        } else if ($this->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
            if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (22 * 2)) > 10) {
                $this->setWarning("MSG915","(特別活動所見は10行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (11 * 2)) > 6) {
                $this->setWarning("MSG915","(特別活動所見は6行までです)");
                return false;
            }
        }

        if ($this->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
            if ($this->validate_row_cnt($this->field["TOTALREMARK"], ((int)$this->totalremark_moji * 2)) > $this->totalremark_gyou) {
                $this->setWarning("MSG915","(総合所見は{$this->totalremark_gyou}行までです)");
                return false;
            }
        } else if ($this->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
            if ($this->validate_row_cnt($this->field["TOTALREMARK"], (66 * 2)) > 8) {
                $this->setWarning("MSG915","(総合所見は8行までです)");
                return false;
            }
        } elseif ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
            if ($this->validate_row_cnt($this->field["TOTALREMARK"], (66 * 2)) > 7) {
                $this->setWarning("MSG915","(総合所見は7行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["TOTALREMARK"], (44 * 2)) > 6) {
                $this->setWarning("MSG915","(総合所見は6行までです)");
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        knja120cQuery::getInsertQuery($this->field,$this->schregno, $this->exp_year, $this->exp_semester);
        return $result;
    }
}
?>
