<?php

require_once('for_php7.php');

class knja127pModel extends Model {
    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $control;
    var $grade;

    function knja127pModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
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

        $this->field = array("TOTALREMARK"              =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"         =>  VARS::post("ATTENDREC_REMARK"),
                             "BEHAVEREC_REMARK"         =>  VARS::post("BEHAVEREC_REMARK"),
                             "ENT_DISABILITY_REMARK"    =>  VARS::post("ENT_DISABILITY_REMARK"),
                             "FOREIGNLANGACT4"          =>  VARS::post("FOREIGNLANGACT4"),
                             "UPDATED"                  =>  VARS::post("UPDATED")
                             );

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

        // Z010
        $db = Query::dbCheckOut();
        $this->z010 = $db->getOne(knja127pQuery::getZ010());
        Query::dbCheckIn($db);

        //行動の記録
        if ($this->Properties["HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_P_disability"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_P_disability"]);
            $this->behaverec_remark_moji = (int)trim($moji);
            $this->behaverec_remark_gyou = (int)trim($gyou);
        } else {  
            $this->behaverec_remark_moji = 20;
            $this->behaverec_remark_gyou = 7;
        }
        //総合所見及び指導上参考となる諸事項
        if ($this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_P_disability"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_P_disability"]);
            $this->totalremark_moji = (int)trim($moji);
            $this->totalremark_gyou = (int)trim($gyou);
        } else {
            $this->totalremark_moji = 25;
            $this->totalremark_gyou = 9;
        }
        //入学時の障害の状態
        if ($this->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_P_disability"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_P_disability"]);
            $this->disability_moji = (int)trim($moji);
            $this->disability_gyou = (int)trim($gyou);
        } else {
            $this->disability_moji = 9;
            $this->disability_gyou = 18;
        }
        //出欠の記録備考
        if ($this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P_disability"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P_disability"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 37;
            $this->attendrec_remark_gyou = 1;
        }

        if ($this->Properties["HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P_disability"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P_disability"]);
            $this->foreignlangact4_moji = (int)trim($moji);
            $this->foreignlangact4_gyou = (int)trim($gyou);
        } else {
            $this->foreignlangact4_moji = 32;
            $this->foreignlangact4_gyou = 3;
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //文字数チェック
        if ($this->validate_row_cnt($this->field["BEHAVEREC_REMARK"], ((int)$this->behaverec_remark_moji * 2)) > $this->behaverec_remark_gyou) {
            $this->setWarning("MSG915","(行動の記録は{$this->behaverec_remark_gyou}行までです)");
            // Add by PP for textarea_cursor start 2020/01/20
            $this->message915="BEHAVEREC_REMARK";
            // Add by PP for textarea_cursor end 2020/01/31
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALREMARK"], ((int)$this->totalremark_moji * 2)) > $this->totalremark_gyou) {
            $this->setWarning("MSG915","(総合所見は{$this->totalremark_gyou}行までです)");
            // Add by PP for textarea_cursor start 2020/01/20
            $this->message915="TOTALREMARK";
            // Add by PP for textarea_cursor end 2020/01/31
            return false;
        }
        if ($this->validate_row_cnt($this->field["ENT_DISABILITY_REMARK"], ((int)$this->disability_moji * 2)) > $this->disability_gyou) {
            $this->setWarning("MSG915","(入学時の障害の状態は{$this->disability_gyou}行までです)");
            // Add by PP for textarea_cursor start 2020/01/20
            $this->message915="ENT_DISABILITY_REMARK";
            // Add by PP for textarea_cursor end 2020/01/31
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ((int)$this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
            $this->setWarning("MSG915","(出欠の記録備考は{$this->attendrec_remark_gyou}行までです)");
            // Add by PP for textarea_cursor start 2020/01/20
            $this->message915="ATTENDREC_REMARK";
            // Add by PP for textarea_cursor end 2020/01/31
            return false;
        }
        if ($this->validate_row_cnt($this->field["FOREIGNLANGACT4"], ((int)$this->foreignlangact4_moji * 2)) > $this->foreignlangact4_gyou) {
            $this->setWarning("MSG915","(道徳は{$this->foreignlangact4_gyou}行までです)");
            // Add by PP for textarea_cursor start 2020/01/20
            $this->message915="FOREIGNLANGACT4";
            // Add by PP for textarea_cursor end 2020/01/31
            return false;
        }

        return true;
    }

    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        knja127pQuery::getInsertQuery($this);
        return ;
    }
}
?>
