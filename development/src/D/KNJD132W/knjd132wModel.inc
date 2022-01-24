<?php

require_once('for_php7.php');
class knjd132wModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $grade_hrclass;
    var $field = array();

    function knjd132wModel() {}

    function init() {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        $this->field = array(
                             "TOTALSTUDYTIME"    =>  VARS::post("TOTALSTUDYTIME"),    //総合的な学習の時間
                             "CLUB"              =>  VARS::post("CLUB"),              //部活動
                             "COMMITTEE"         =>  VARS::post("COMMITTEE"),         //委員会
                             "REMARK1"           =>  VARS::post("REMARK1"),           //備考
                             "COMMUNICATION"     =>  VARS::post("COMMUNICATION"),     //所見
                             "ATTENDREC_REMARK"  =>  VARS::post("ATTENDREC_REMARK"),  //出欠備考
                             "SEMESTER"          =>  9                                //学期
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::post("SEMESTER")!="") {
            $this->field["SEMESTER"] = VARS::post("SEMESTER");
        }
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

        //校種取得
        $db = Query::dbCheckOut();
        $this->schoolkind = $db->getOne(knjd132wQuery::getSchoolkind($this->grade));
        Query::dbCheckIn($db);

        $db = Query::dbCheckOut();
        $query = knjd132wQuery::getConditionDatRemark1($this);
        $ConditionDatRemark1 = $db->getOne($query);
        Query::dbCheckIn($db);
        if ($ConditionDatRemark1 == '') {
            $ConditionDatRemark1 = "1";
        }
        $attMoji = 1; $attgyou = 1;
        switch($ConditionDatRemark1){
            case '1':
                $attMoji = 20; $attgyou = 1;
                break;
            case '2':
                $attMoji = 20; $attgyou = 1;
                break;
            case '3':
                break;
            case '4':
                $attMoji = 7; $attgyou = 2;
                break;
            case '5':
                $attMoji = 14; $attgyou = 2;
                break;
            case '6':
                break;
        }
 
        if ($this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_{$this->schoolkind}"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_{$this->schoolkind}"]);
            $this->getPro["TOTALSTUDYTIME"]["moji"] = (int)trim($moji);
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["TOTALSTUDYTIME"]["moji"] = 22;
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = 4;
        }
        if ($this->Properties["reportSpecialSize01_01"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize01_01"]);
            $this->getPro["CLUB"]["moji"] = (int)trim($moji);
            $this->getPro["CLUB"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["CLUB"]["moji"] = 25;
            $this->getPro["CLUB"]["gyou"] = 2;
        }
        if ($this->Properties["reportSpecialSize01_02"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize01_02"]);
            $this->getPro["COMMITTEE"]["moji"] = (int)trim($moji);
            $this->getPro["COMMITTEE"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMITTEE"]["moji"] = 25;
            $this->getPro["COMMITTEE"]["gyou"] = 2;
        }
        if ($this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_{$this->schoolkind}"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_{$this->schoolkind}"]);
            $this->getPro["REMARK1"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK1"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK1"]["moji"] = 25;
            $this->getPro["REMARK1"]["gyou"] = 5;
        }
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_{$this->schoolkind}"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_{$this->schoolkind}"]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 25;
            $this->getPro["COMMUNICATION"]["gyou"] = 5;
        }
        if ($this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_{$this->schoolkind}"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_{$this->schoolkind}"]);
            $this->getPro["ATTENDREC_REMARK"]["moji"] = (int)trim($moji);
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["ATTENDREC_REMARK"]["moji"] = $attMoji;
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = $attgyou;
        }
        
    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYTIME"], ($this->getPro["TOTALSTUDYTIME"]["moji"] * 2)) > $this->getPro["TOTALSTUDYTIME"]["gyou"]) {
            $this->setWarning("MSG915","(総合的な学習の時間は{$this->getPro["TOTALSTUDYTIME"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["CLUB"], ($this->getPro["CLUB"]["moji"] * 2)) > $this->getPro["CLUB"]["gyou"]) {
            $this->setWarning("MSG915","(部活動は{$this->getPro["CLUB"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMITTEE"], ($this->getPro["COMMITTEE"]["moji"] * 2)) > $this->getPro["COMMITTEE"]["gyou"]) {
            $this->setWarning("MSG915","(委員会は{$this->getPro["COMMITTEE"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK1"], ($this->getPro["REMARK1"]["moji"] * 2)) > $this->getPro["REMARK1"]["gyou"]) {
            $this->setWarning("MSG915","(備考は{$this->getPro["REMARK1"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
            $this->setWarning("MSG915","(所見は{$this->getPro["COMMUNICATION"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->getPro["ATTENDREC_REMARK"]["moji"] * 2)) > $this->getPro["ATTENDREC_REMARK"]["gyou"]) {
            $this->setWarning("MSG915","(出欠備考は{$this->getPro["ATTENDREC_REMARK"]["gyou"]}行までです)");
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
        if ($this->cmd == 'update') {
        
            if(knjd132wQuery::isCheckDatQuery($this,$this->schregno)){
                knjd132wQuery::getUpdateQuery($this->field, $this->schregno);
            } else {
                knjd132wQuery::getInsertQuery($this->field, $this->schregno);
        	}
        	
            if(knjd132wQuery::isCheckDetailDatQuery01($this,$this->schregno)){
                knjd132wQuery::getUpdateDetailQuery01($this->field, $this->schregno);
            } else {
                knjd132wQuery::getInsertDetailQuery01($this->field, $this->schregno);
        	}
        	
            if(knjd132wQuery::isCheckDetailDatQuery02($this,$this->schregno)){
                knjd132wQuery::getUpdateDetailQuery02($this->field, $this->schregno);
            } else {
                knjd132wQuery::getInsertDetailQuery02($this->field, $this->schregno);
        	}
        }
        return $result;
    }
}
?>
