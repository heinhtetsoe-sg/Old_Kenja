<?php

require_once('for_php7.php');

class knjd132mModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $control;
    var $detail;
    var $remark_moji = 10;
    var $remark_gyou = 4;
    var $communication_moji = 55;
    Var $communication_gyou = 2;

    function knjd132mModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->exp_grade);
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "REMARK1"          =>  VARS::post("REMARK1"),
                             "REMARK2"          =>  VARS::post("REMARK2"),
                             "REMARK3"          =>  VARS::post("REMARK3"),
                             "REMARK4"          =>  VARS::post("REMARK4"),
                             "REMARK5"          =>  VARS::post("REMARK5"),
                             "COMMUNICATION"    =>  VARS::post("COMMUNICATION"),
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );


        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
            $this->exp_grade = VARS::get("GRADE");
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
            unset($this->exp_grade);
        }
        
        if (VARS::get("sendSEME") != "") {
            $this->ikkatsuSeme = VARS::get("sendSEME");
        }


        //一括更新用
        if ($this->cmd == "ikkatsu_update") {
            $this->ikkatsu_data["selectdata"] = VARS::post("selectdata");
            $this->ikkatsu_data["hreport"] = array();
            $this->ikkatsu_data["check"] = array(VARS::post("CHECK_ALL"),//ALL
                                                 VARS::post("CHECK1"));
            if (VARS::post("CHECK1") == "1") $this->ikkatsu_data["hreport"]["REMARK1"]   = VARS::post("SCHOOL_ACT");
        }
        if(VARS::get("ini2")) {
            unset($this->ikkatsu_data);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }


    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        $moji = $this->remark_moji;
        $gyou = $this->remark_gyou;
        if ($this->validate_row_cnt($this->field["REMARK1"], ($moji * 2)) > $gyou) {
            $this->setWarning("MSG915","(学級活動は{$gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK2"], ($moji * 2)) > $gyou) {
            $this->setWarning("MSG915","(生徒会活動は{$gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK3"], ($moji * 2)) > $gyou) {
            $this->setWarning("MSG915","(部活動の成果は{$gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK4"], ($moji * 2)) > $gyou) {
            $this->setWarning("MSG915","(資格取得・検定等は{$gyou}行までです)");
            return false;
        }
        
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->communication_moji * 2)) > $this->communication_gyou) {
            $this->setWarning("MSG915","(備考は{$this->communication_gyou}行までです)");
            return false;
        }

        return true;
    }

    //更新用モデル
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        
        $this->update();
        $this->setMessage("MSG201");
    }

    //更新
    function &update() {
        knjd132mQuery::getUpdateQuery($this);
        return ;
    }

    function &IkkatsuModel() {
        //生徒が選択されてない場合
        if (!strlen($this->ikkatsu_data["selectdata"])) {
            $this->setCmd("ikkatsu");
            $this->setWarning("MSG304");
            return false;
        }
        //最低ひとつでもチェックがついてる場合のみ更新
        if (!in_array("1", $this->ikkatsu_data["check"])) {
            $this->setCmd("ikkatsu");
            $this->setWarning("更新する項目がありません");
            return false;
        }
        //文字数チェック
        if (VARS::post("CHECK1") == "1") {
            if ($this->validate_row_cnt($model->field["SCOOL_ACT"], (10 * 2)) > 2) {
                $this->setWarning("MSG915","(内容は2行までです)");
                return false;
            }
        }

        $db = Query::dbCheckOut();
        knjd132mQuery::IkkatsuUpdate($db, $this);
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }

}

?>
