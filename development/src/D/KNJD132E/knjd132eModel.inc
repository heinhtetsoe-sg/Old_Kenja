<?php

require_once('for_php7.php');
class knjd132eModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $grade_hrclass;
    var $field = array();

    function knjd132eModel() {}

    function init() {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        $this->field = array("TOTALSTUDYTIME"       =>  VARS::post("TOTALSTUDYTIME"),   //学習活動
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"), //評価
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION")     //備考欄
                             );

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
        if (VARS::get("HR_CLASS")!="") {
            $this->grade_hrclass = VARS::get("HR_CLASS");
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
        $query = knjd132eQuery::getSchoolKind($this->grade);
        $this->school_kind = $db->getOne($query);
        $this->school_kind = !strlen($this->school_kind) ? "H" : $this->school_kind;
        Query::dbCheckIn($db);

        //学習活動
        if ($this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_".$this->school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_".$this->school_kind]);
            $this->getPro["TOTALSTUDYTIME"]["moji"] = (int)trim($moji);
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["TOTALSTUDYTIME"]["moji"] = 37;
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = 4;
        }

        //評価
        if ($this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_".$this->school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_".$this->school_kind]);
            $this->getPro["SPECIALACTREMARK"]["moji"] = (int)trim($moji);
            $this->getPro["SPECIALACTREMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["SPECIALACTREMARK"]["moji"] = 37;
            $this->getPro["SPECIALACTREMARK"]["gyou"] = 4;
        }

        //備考
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$this->school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$this->school_kind]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 37;
            $this->getPro["COMMUNICATION"]["gyou"] = 3;
        }

    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        $chkArr = array();
        $chkArr[] = array("fieldName" => "TOTALSTUDYTIME",      "j_Title" => "学習活動");
        $chkArr[] = array("fieldName" => "SPECIALACTREMARK",    "j_Title" => "評価");
        $chkArr[] = array("fieldName" => "COMMUNICATION",       "j_Title" => "備考");
        foreach ($chkArr as $key => $valArr) {
            if ($this->validate_row_cnt($this->field["{$valArr["fieldName"]}"], ($this->getPro["{$valArr["fieldName"]}"]["moji"] * 2)) > $this->getPro["{$valArr["fieldName"]}"]["gyou"]) {
                $this->setWarning("MSG915","({$valArr["j_Title"]}は{$this->getPro["{$valArr["fieldName"]}"]["gyou"]}行までです)");
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
            knjd132eQuery::getDeleteQuery($this->schregno, $this);
            knjd132eQuery::getInsertQuery($this->field, $this->schregno, $this);
        }
        return $result;
    }
}
?>
