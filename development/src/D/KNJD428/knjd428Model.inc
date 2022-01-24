<?php

require_once('for_php7.php');

class knjd428Model extends Model {
    var $cmd;
    var $exp_year;
    var $schregno;
    var $name;
    var $field = array();
    var $getPrgId;
    var $auth;
    var $sendAuth;
    var $grade;
    var $hrClass;

    function knjd428Model() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hrClass);
        }
        $this->semester9  = "9";  //固定
        $this->detailDiv  = "06"; //固定
        $this->detailCode = "01"; //固定

        $this->field = array("REMARK1"              =>  VARS::post("REMARK1"),          //年間目標
                             "REMARK2"              =>  VARS::post("REMARK2"),          //年間目標
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"), //出欠の備考
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION"),    //学校より
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //左フレームから取得
        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }

        if (VARS::post("SEMESTER") != "") {
            $this->semester = VARS::post("SEMESTER");
        }
        
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS") != "") {
            $this->hrClass = VARS::get("HR_CLASS");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hrClass);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $db = Query::dbCheckOut();
        //帳票パターン取得 GRADE_KIND_SCHREG_GROUP_DAT
        $rowG = $db->getRow(knjd428Query::getGradeKindSchregGroupDat($this), DB_FETCHMODE_ASSOC);
        $query = knjd428Query::getPrintPattern($this, $rowG);
        $this->printPattern = $db->getOne($query);
        $this->printPattern = substr($this->printPattern, 1, 1);
        Query::dbCheckIn($db);

        //入力文字初期値の設定
        //年間目標
        if ($this->printPattern == 'C') {
            $this->remark1_moji = 20;
            $this->remark1_gyou = 5;
            $this->remark2_moji = 30;
            $this->remark2_gyou = 5;
        } else {
            $this->remark1_moji = 45;
            $this->remark1_gyou = 4;
        }

        //出欠の備考
        $this->attendrec_remark_moji = 10;
        $this->attendrec_remark_gyou = 2;

        //学校より
        $this->communication_moji = 16;
        $this->communication_gyou = 6;
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["REMARK1"]) > 0) {
            if ($this->validate_row_cnt($this->field["REMARK1"], ($this->remark1_moji * 2)) > $this->remark1_gyou) {
                $this->setWarning("MSG915","(年間目標は{$this->remark1_gyou}行までです)");
                return false;
            }
        }
        if (strlen($this->field["REMARK2"]) > 0) {
            if ($this->validate_row_cnt($this->field["REMARK2"], ($this->remark2_moji * 2)) > $this->remark2_gyou) {
                $this->setWarning("MSG915","(年間目標は{$this->remark2_gyou}行までです)");
                return false;
            }
        }
        if (strlen($this->field["ATTENDREC_REMARK"]) > 0) {
            if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
                $this->setWarning("MSG915","(出欠の備考は{$this->attendrec_remark_gyou}行までです)");
                return false;
            }
        }
        if (strlen($this->field["COMMUNICATION"]) > 0) {
            if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->communication_moji * 2)) > $this->communication_gyou) {
                $this->setWarning("MSG915","(学校よりは{$this->communication_gyou}行までです)");
                return false;
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        //HREPORTREMARK_DAT
        knjd428Query::getInsertQuery($this);

        return ;
    }
}
?>
