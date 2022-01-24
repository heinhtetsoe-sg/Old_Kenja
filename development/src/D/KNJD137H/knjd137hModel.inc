<?php

require_once('for_php7.php');
class knjd137hModel extends Model {
    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $control;
    var $jdgTotalStudy2018Flg;
    var $TotalStudyStr;

    function knjd137hModel() {
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

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),
                             "TOTALSTUDYTIME"       =>  VARS::post("TOTALSTUDYTIME"),
                             "REMARK1"              =>  VARS::post("REMARK1"),
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "REMARK3"              =>  VARS::post("REMARK3"),
                             "SPECIAL1"             =>  VARS::post("SPECIAL1"),
                             "SPECIAL2"             =>  VARS::post("SPECIAL2"),
                             "SPECIAL3"             =>  VARS::post("SPECIAL3"),
                             "UPDATED"              =>  VARS::post("UPDATED")
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

        $db = Query::dbCheckOut();
        $this->pattern=$db->getOne(knjd137hQuery::getHreportConditionDat($this));
        $this->jdgTotalStudy2018Flg = knjd137hModel::judgeTotalRemarkName($db);
        $this->TotalStudyStr = $this->jdgTotalStudy2018Flg ? "探究" : "総合的な学習の時間";
        Query::dbCheckIn($db);
        
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        /****************************/
        /* 文字数・行数の初期値設定 */
        /****************************/
        //総合的な学習の時間（学習内容）
        if ($this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_H"]);
            $this->getPro["TOTALSTUDYTIME"]["moji"] = (int)trim($moji);
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = (int)trim($gyou);
        } else {
            if($this->pattern == '2' || $this->pattern == '3'){
                $this->getPro["TOTALSTUDYTIME"]["moji"] = 40;
                $this->getPro["TOTALSTUDYTIME"]["gyou"] = 3;
            } else {
                $this->getPro["TOTALSTUDYTIME"]["moji"] = 16;
                $this->getPro["TOTALSTUDYTIME"]["gyou"] = 3;
            }
        }
        //総合的な学習の時間（評価）
        if ($this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_H"]);
            $this->getPro["REMARK1"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK1"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK1"]["moji"] = 16;
            $this->getPro["REMARK1"]["gyou"] = 5;
        }
        //通信欄
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_H"]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            if($this->pattern == 2){
                $this->getPro["COMMUNICATION"]["moji"] = 31;
                $this->getPro["COMMUNICATION"]["gyou"] = 4;
            } else {
                $this->getPro["COMMUNICATION"]["moji"] = 30;
                $this->getPro["COMMUNICATION"]["gyou"] = 5;
            }
        }
        //自立活動
        if ($this->Properties["HREPORTREMARK_DAT_REMARK3_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK3_SIZE_H"]);
            $this->getPro["REMARK3"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK3"]["gyou"] = (int)trim($gyou);
        } else {
            if($this->pattern == '2' || $this->pattern == '3'){
                $this->getPro["REMARK3"]["moji"] = 40;
                $this->getPro["REMARK3"]["gyou"] = 3;
            } else {
                $this->getPro["REMARK3"]["moji"] = 16;
                $this->getPro["REMARK3"]["gyou"] = 12;
            }
        }
        
        //特別活動（部活動）
        if ($this->Properties["reportSpecialSize01_01"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize01_01"]);
            $this->getPro["SPECIAL1"]["moji"] = (int)trim($moji);
            $this->getPro["SPECIAL1"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["SPECIAL1"]["moji"] = 10;
            $this->getPro["SPECIAL1"]["gyou"] = 5;
        }
        
        //特別活動（委員会）
        if ($this->Properties["reportSpecialSize01_02"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize01_02"]);
            $this->getPro["SPECIAL2"]["moji"] = (int)trim($moji);
            $this->getPro["SPECIAL2"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["SPECIAL2"]["moji"] = 10;
            $this->getPro["SPECIAL2"]["gyou"] = 5;
        }
        
        //特別活動（その他）
        if ($this->Properties["reportSpecialSize01_03"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize01_03"]);
            $this->getPro["SPECIAL3"]["moji"] = (int)trim($moji);
            $this->getPro["SPECIAL3"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["SPECIAL3"]["moji"] = 10;
            $this->getPro["SPECIAL3"]["gyou"] = 5;
        }
        
        if ($this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_H"]);
            $this->getPro["ATTENDREC_REMARK"]["moji"] = (int)trim($moji);
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["ATTENDREC_REMARK"]["moji"] = 6;
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = 4;
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //文字数・行数チェック
        if ($this->validate_row_cnt($this->field["TOTALSTUDYTIME"], ($this->getPro["TOTALSTUDYTIME"]["moji"] * 2)) > $this->getPro["TOTALSTUDYTIME"]["gyou"]) {
            $this->setWarning("MSG915","(".$this->$TotalStudyStr."（学習内容）は{$this->getPro["TOTALSTUDYTIME"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK3"], ($this->getPro["REMARK3"]["moji"] * 2)) > $this->getPro["REMARK3"]["gyou"]) {
            $this->setWarning("MSG915","(自立活動は{$this->getPro["REMARK3"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIAL1"], ($this->getPro["SPECIAL1"]["moji"] * 2)) > $this->getPro["SPECIAL1"]["gyou"]) {
            $this->setWarning("MSG915","(特別活動（委員会）は{$this->getPro["SPECIAL1"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIAL2"], ($this->getPro["SPECIAL2"]["moji"] * 2)) > $this->getPro["SPECIAL2"]["gyou"]) {
            $this->setWarning("MSG915","(特別活動（部活動）は{$this->getPro["SPECIAL2"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIAL3"], ($this->getPro["SPECIAL3"]["moji"] * 2)) > $this->getPro["SPECIAL3"]["gyou"]) {
            $this->setWarning("MSG915","(特別活動（その他）は{$this->getPro["SPECIAL3"]["gyou"]}行までです)");
            return false;
        }

        return true;
    }

    //注意：要dbCheckOut
    function judgeTotalRemarkName($db) {
        $retflg = false;
        if ($this->exp_year < 2018) {
            $retflg = false;
        } else if ($this->exp_year < 2020) {
            $isAny = false;
            $query = knjd137hQuery::getTotalRemarkDisable($this);
            $result = $db->query($query);
            while ($disableRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($disableRow["YEAR"] == $this->exp_year
                    && $disableRow["GRADE"] == $this->exp_grade) {
                    $isAny = true;
                    break;
                }
            }
            $result->free();
            if ($isAny) {
                $retflg = true;
            }
        } else {
            // 2020年度以降は全て使用不可
            $retflg = true;
        }
        return $retflg;
    }

    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        knjd137hQuery::getInsertQuery($this);
        return ;
    }
    
    function &getUpdateModel2() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update2();
        $this->setMessage("MSG201");
    }

    function &update2() {
        knjd137hQuery::getInsertQuery2($this);
        return ;
    }
}
?>
