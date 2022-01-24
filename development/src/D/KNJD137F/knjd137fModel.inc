<?php

require_once('for_php7.php');
class knjd137fModel extends Model {
    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $grade;
    var $field = array();
    var $record = array();
    var $control;

    function knjd137fModel() {
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

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        //生活の記録
        if (is_array($this->itemArray)) {
            foreach ($this->itemArray as $key => $val) {
                $this->record["RECORD"][$key] = VARS::post("RECORD".$key);
            }
        }

        $this->cmd = VARS::request("cmd");

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
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
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

        //サイズの初期値設定
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"]);
            $this->communication_moji = (int)trim($moji);
            $this->communication_gyou = (int)trim($gyou);
        } else {
            $this->communication_moji = 43;
            $this->communication_gyou = 3;
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG304","（学期）");
            return false;
        }
        //文字数チェック
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->communication_moji * 2)) > $this->communication_gyou) {
            $this->setWarning("MSG915","(通信欄は{$this->communication_gyou}行までです)");
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
        //HREPORTREMARK_DAT
        knjd137fQuery::getInsertQuery($this);
        //BEHAVIOR_SEMES_DAT
        knjd137fQuery::getInsertQuery2($this);

        return ;
    }
}
?>
