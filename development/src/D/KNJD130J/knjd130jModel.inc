<?php

require_once('for_php7.php');

class knjd130jModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $control;
    var $detail;

    function knjd130jModel() {
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

        $this->field = array("REMARK1_1"     =>  VARS::post("REMARK1_1"),
                             "REMARK1_2"     =>  VARS::post("REMARK1_2"),
                             "REMARK1_3"     =>  VARS::post("REMARK1_3"),
                             "REMARK2"       =>  VARS::post("REMARK2"),
                             "REMARK3_1"     =>  VARS::post("REMARK3_1"),
                             "REMARK3_2"     =>  VARS::post("REMARK3_2"),
                             "REMARK3_3"     =>  VARS::post("REMARK3_3"),
                             "REMARK4"       =>  VARS::post("REMARK4"),
                             "UPDATED"       =>  VARS::post("UPDATED")
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

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        
        if ($this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_H"]);
            $this->getPro["REMARK1_1"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK1_1"]["gyou"] = (int)trim($gyou);
            $this->getPro["REMARK1_2"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK1_2"]["gyou"] = (int)trim($gyou);
            $this->getPro["REMARK1_3"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK1_3"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK1_1"]["moji"] = 25;
            $this->getPro["REMARK1_1"]["gyou"] = 6;
            $this->getPro["REMARK1_2"]["moji"] = 25;
            $this->getPro["REMARK1_2"]["gyou"] = 6;
            $this->getPro["REMARK1_3"]["moji"] = 25;
            $this->getPro["REMARK1_3"]["gyou"] = 6;
        }
        
        if ($this->Properties["HREPORTREMARK_DAT_REMARK2_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK2_SIZE_H"]);
            $this->getPro["REMARK2"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK2"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK2"]["moji"] = 17;
            $this->getPro["REMARK2"]["gyou"] = 2;
        }
        
        if ($this->Properties["HREPORTREMARK_DAT_REMARK3_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK3_SIZE_H"]);
            $this->getPro["REMARK3_1"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK3_1"]["gyou"] = (int)trim($gyou);
            $this->getPro["REMARK3_2"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK3_2"]["gyou"] = (int)trim($gyou);
            $this->getPro["REMARK3_3"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK3_3"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK3_1"]["moji"] = 17;
            $this->getPro["REMARK3_1"]["gyou"] = 1;
            $this->getPro["REMARK3_2"]["moji"] = 17;
            $this->getPro["REMARK3_2"]["gyou"] = 1;
            $this->getPro["REMARK3_3"]["moji"] = 17;
            $this->getPro["REMARK3_3"]["gyou"] = 1;
        }

        if ($this->Properties["HREPORTREMARK_DAT_REMARK4_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK4_SIZE_H"]);
            $this->getPro["REMARK4"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK4"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK4"]["moji"] = 17;
            $this->getPro["REMARK4"]["gyou"] = 8;
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        
        for ($semes=1;$semes<=3;$semes++) {
            if ($this->validate_row_cnt($this->field["REMARK1_".$semes], ($this->getPro["REMARK1_".$semes]["moji"] * 2)) > $this->getPro["REMARK1_".$semes]["gyou"]) {
                $this->setWarning("MSG915","(総合学習は{$this->getPro["REMARK1_".$semes]["gyou"]}行までです)");
                return false;
            }
            if ($this->validate_row_cnt($this->field["REMARK3_".$semes], ($this->getPro["REMARK3_".$semes]["moji"] * 2)) > $this->getPro["REMARK3_".$semes]["gyou"]) {
                $this->setWarning("MSG915","(クラブ・同好会は{$this->getPro["REMARK3_".$semes]["gyou"]}行までです)");
                return false;
            }
        }
        if ($this->validate_row_cnt($this->field["REMARK2"], ($this->getPro["REMARK2"]["moji"] * 2)) > $this->getPro["REMARK2"]["gyou"]) {
            $this->setWarning("MSG915","(委員・係りは{$this->getPro["REMARK2"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK4"], ($this->getPro["REMARK4"]["moji"] * 2)) > $this->getPro["REMARK4"]["gyou"]) {
            $this->setWarning("MSG915","(資格は{$this->getPro["REMARK4"]["gyou"]}行までです)");
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
        knjd130jQuery::getUpdateQuery($this);
        return ;
    }
}
?>
