<?php

require_once('for_php7.php');


class knjg045cModel extends Model {

    var $field  = array();    //画面データ格納
    var $cmd;                 //コマンド
    var $diaryDate;           //日付
    var $control;             //コントロールマスタ
    var $data  = array();
    var $di_cd;
    var $grade;
    var $att_field = array(); //欠席状況

    function knjg045cModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if ($this->Properties["useSchool_KindField"] != "1") {
            $this->field["SCHOOL_KIND"] = SCHOOLKIND;
        }

        $this->diaryDate  = VARS::post("DIARY_DATE");
        if ($this->diaryDate == "") $this->diaryDate  = str_replace("-","/",CTRL_DATE);
        $this->field = array("SCHOOL_KIND"                 =>  VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->field["SCHOOL_KIND"],
                             "WEATHER"                     =>  VARS::post("WEATHER"),
                             "WEATHER2"                    =>  VARS::post("WEATHER2"),
                             "SEQ001_REMARK1"              =>  VARS::post("SEQ001_REMARK1"),
                             "SEQ001_REMARK2"              =>  VARS::post("SEQ001_REMARK2"),
                             "SEQ001_REMARK3"              =>  VARS::post("SEQ001_REMARK3"),
                             "SEQ001_REMARK4"              =>  VARS::post("SEQ001_REMARK4"),
                             "SEQ001_REMARK5"              =>  VARS::post("SEQ001_REMARK5"),
                             "SEQ002_REMARK1"              =>  VARS::post("SEQ002_REMARK1"),
                             "SEQ002_REMARK2"              =>  VARS::post("SEQ002_REMARK2"),
                             "SEQ002_REMARK3"              =>  VARS::post("SEQ002_REMARK3"),
                             "SEQ002_REMARK4"              =>  VARS::post("SEQ002_REMARK4"),
                             "SEQ002_REMARK5"              =>  VARS::post("SEQ002_REMARK5"),
                             "SEQ002_REMARK6"              =>  VARS::post("SEQ002_REMARK6"),
                             "SEQ002_REMARK7"              =>  VARS::post("SEQ002_REMARK7"),
                             "SEQ002_REMARK8"              =>  VARS::post("SEQ002_REMARK8"),
                             "SEQ003_REMARK1"              =>  VARS::post("SEQ003_REMARK1"),
                     );

        $db = Query::dbCheckOut();
        $year = ($this->year) ? $this->year : CTRL_YEAR;
        if ($this->Properties["useSchool_KindField"] == "1") {
            $firstSchool_kind = $db->getOne(knjg045cQuery::getA023($this));
            $this->field["SCHOOL_KIND"] = ($this->field["SCHOOL_KIND"]) ? $this->field["SCHOOL_KIND"] : $firstSchool_kind;
        }
        Query::dbCheckIn($db);

        //休暇欠勤
        $this->SEQ001_REMARK1_moji = 6;
        $this->SEQ001_REMARK1_gyou = 5;

        $this->SEQ001_REMARK2_moji = 18;
        $this->SEQ001_REMARK2_gyou = 5;

        //出張
        $this->SEQ001_REMARK3_moji = 6;
        $this->SEQ001_REMARK3_gyou = 5;

        $this->SEQ001_REMARK4_moji = 6;
        $this->SEQ001_REMARK4_gyou = 5;

        $this->SEQ001_REMARK5_moji = 11;
        $this->SEQ001_REMARK5_gyou = 5;

        //巡視記録
        $this->SEQ002_REMARK7_moji = 19;
        $this->SEQ002_REMARK7_gyou = 4;

        //来校者
        $this->SEQ002_REMARK8_moji = 21;
        $this->SEQ002_REMARK8_gyou = 3;

        //学校行事その他の記録
        $this->SEQ003_REMARK1_moji = 49;
        $this->SEQ003_REMARK1_gyou = 4;
    }

    //入力項目チェック
    function validateInput()
    {
    
        if ($this->validate_row_cnt($this->field["SEQ001_REMARK1"], ($this->SEQ001_REMARK1_moji * 2)) > $this->SEQ001_REMARK1_gyou) {
            $this->setWarning("MSG915","(休暇欠勤は{$this->SEQ001_REMARK1_gyou}行までです)");
            return;
        }
        if ($this->validate_row_cnt($this->field["SEQ001_REMARK2"], ($this->SEQ001_REMARK2_moji * 2)) > $this->SEQ001_REMARK2_gyou) {
            $this->setWarning("MSG915","(休暇欠勤は{$this->SEQ001_REMARK2_gyou}行までです)");
            return;
        }
        if ($this->validate_row_cnt($this->field["SEQ001_REMARK3"], ($this->SEQ001_REMARK3_moji * 2)) > $this->SEQ001_REMARK3_gyou) {
            $this->setWarning("MSG915","(出張は{$this->SEQ001_REMARK3_gyou}行までです)");
            return;
        }
        if ($this->validate_row_cnt($this->field["SEQ001_REMARK4"], ($this->SEQ001_REMARK4_moji * 2)) > $this->SEQ001_REMARK4_gyou) {
            $this->setWarning("MSG915","(出張は{$this->SEQ001_REMARK4_gyou}行までです)");
            return;
        }
        if ($this->validate_row_cnt($this->field["SEQ001_REMARK5"], ($this->SEQ001_REMARK5_moji * 2)) > $this->SEQ001_REMARK5_gyou) {
            $this->setWarning("MSG915","(出張は{$this->SEQ001_REMARK5_gyou}行までです)");
            return;
        }
        if ($this->validate_row_cnt($this->field["SEQ002_REMARK7"], ($this->SEQ002_REMARK7_moji * 2)) > $this->SEQ002_REMARK7_gyou) {
            $this->setWarning("MSG915","(巡視記録は{$this->SEQ002_REMARK7_gyou}行までです)");
            return;
        }
        if ($this->validate_row_cnt($this->field["SEQ002_REMARK8"], ($this->SEQ002_REMARK8_moji * 2)) > $this->SEQ002_REMARK8_gyou) {
            $this->setWarning("MSG915","(来校者は{$this->SEQ002_REMARK8_gyou}行までです)");
            return;
        }
        if ($this->validate_row_cnt($this->field["SEQ003_REMARK1"], ($this->SEQ003_REMARK1_moji * 2)) > $this->SEQ003_REMARK1_gyou) {
            $this->setWarning("MSG915","(学校行事その他の記録は{$this->SEQ003_REMARK1_gyou}行までです)");
            return;
        }
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->cmd == "update") {
            if ($this->validateInput()) {
                knjg045cQuery::getUpdateQuery($this);
            }
        } else if ($this->cmd == "delete") {
            knjg045cQuery::getDeleteQuery($this, "delete");
        }
        $this->setMessage("MSG201");
    }
}
?>
