<?php

require_once('for_php7.php');
class knjd131wModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $grade_hrclass;
    var $field = array();

    function knjd131wModel() {}

    function init() {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        $this->field = array("REMARK1"          =>  VARS::post("REMARK1"),     //備考欄
                             "SEMESTER"         =>  VARS::post("SEMESTER"),    //学期
                             "DETAIL_SEME"      =>  VARS::post("DETAIL_SEME")  //DETAIL学期
                             );
        if (is_array($this->koumoku)) {
            foreach ($this->koumoku as $key => $val) {
                $this->detailField["DETAIL_CHECK{$key}"] = VARS::post("DETAIL_CHECK{$key}");
                $this->detailField["DETAIL_REMARK1{$key}"] = VARS::post("DETAIL_REMARK1{$key}");
                $this->detailField["DETAIL_CHECK_ALL{$key}"] = VARS::post("DETAIL_CHECK_ALL{$key}");
                $this->detailField["DETAIL_REMARK1_ALL{$key}"] = VARS::post("DETAIL_REMARK1_ALL{$key}");
            }
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name     = VARS::get("NAME");
        }

        if (VARS::get("GRADE")!="") {
            $this->grade    = VARS::get("GRADE");
        }
        $this->hr_class = VARS::get("HR_CLASS");
        if (VARS::get("cmd") == "replace") {
            $this->grade_hrclass = VARS::request("GRADE_HRCLASS");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }
        
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //校種を取得
        $db = Query::dbCheckOut();
        $query = knjd131wQuery::getSchoolKind($this->grade);
        $school_kind = $db->getOne($query);
        $school_kind = !strlen($school_kind) ? "H" : $school_kind;
        Query::dbCheckIn($db);

        if ($this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_".$school_kind]);
            $this->getPro["REMARK1"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK1"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK1"]["moji"] = 15;
            $this->getPro["REMARK1"]["gyou"] = 5;
        }
        
    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK1"], ($this->getPro["REMARK1"]["moji"] * 2)) > $this->getPro["REMARK1"]["gyou"]) {
            $this->setWarning("MSG915","(備考は{$this->getPro["REMARK1"]["gyou"]}行までです)");
            return false;
        }
        foreach ($this->koumoku as $key => $val) {
            if (strlen($this->detailField["DETAIL_REMARK1{$key}"]) > 45) {
                $this->setWarning("MSG915","(出欠備考は45バイトです)");
                return false;
            }
            if (strlen($this->detailField["DETAIL_REMARK1_ALL{$key}"]) > 45) {
                $this->setWarning("MSG915","(出欠備考は45バイトです)");
                return false;
            }
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
        if ($this->cmd == 'update') {
            knjd131wQuery::getDeleteQuery($this->schregno, $this);
            knjd131wQuery::getInsertQuery($this->field, $this->schregno, $this);
        }
        return $result;
    }
}
?>
