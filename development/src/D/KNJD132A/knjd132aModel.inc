<?php

require_once('for_php7.php');

class knjd132aModel extends Model {
    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $grade;
    var $field = array();
    var $record = array();
    var $ikkatsu_data = array();
    var $semesCnt;

    function knjd132aModel() {}

    function init() {
        $this->cmd = VARS::request("cmd");

        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        //左フレームから取得
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

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),         //学期
                             "REMARK2"              =>  VARS::post("REMARK2"),          //道徳
                             );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //入力文字初期値の設定
        //道徳
        $this->remark2_moji = 30;
        $this->remark2_gyou = 4;

    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        //文字数チェック
        if ($this->validate_row_cnt($this->field["REMARK2"], ($this->remark2_moji * 2)) > $this->remark2_gyou) {
            $this->setWarning("MSG915","(道徳は{$this->remark2_gyou}行までです)");
            return false;
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
        knjd132aQuery::getInsertQuery($this);
        return ;
    }

}
?>
