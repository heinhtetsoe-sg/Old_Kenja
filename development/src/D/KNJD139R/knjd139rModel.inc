<?php

require_once('for_php7.php');

class knjd139rModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $field = array();
    var $exp_year;

    function knjd139rModel() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->semester);
        }

        $this->field = array("TANKYU_THEME01"       =>  VARS::post("TANKYU_THEME01"),    //探究テーマ1
                             "TANKYU_THEME02"       =>  VARS::post("TANKYU_THEME02"),    //探究テーマ2
                             "COMMITTEE01"          =>  VARS::post("COMMITTEE01"),       //生徒会活動1
                             "COMMITTEE02"          =>  VARS::post("COMMITTEE02"),       //生徒会活動2
                             "COMMITTEE03"          =>  VARS::post("COMMITTEE03"),       //生徒会活動3
                             "CLASSACT01"           =>  VARS::post("CLASSACT01"),        //学級の活動1
                             "CLASSACT02"           =>  VARS::post("CLASSACT02"),        //学級の活動2
                             "CLASSACT03"           =>  VARS::post("CLASSACT03"),        //学級の活動3
                             "CLUBACT01"            =>  VARS::post("CLUBACT01"),         //部活動1
                             "CLUBACT02"            =>  VARS::post("CLUBACT02"),         //部活動2
                             "CLUBACT03"            =>  VARS::post("CLUBACT03"),         //部活動3
                             "OTHER01"              =>  VARS::post("OTHER01"),           //その他1
                             "OTHER02"              =>  VARS::post("OTHER02"),           //その他2
                             "OTHER03"              =>  VARS::post("OTHER03"),           //その他3
                             "OTHER04"              =>  VARS::post("OTHER04"),           //その他4
                             "OTHER05"              =>  VARS::post("OTHER05"),           //その他5
                             "OTHER06"              =>  VARS::post("OTHER06"),           //その他6
                             "ATTENDRECREMARK"      =>  VARS::post("ATTENDRECREMARK"),   //出欠状況備考
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION")      //所見
                             );

        if (VARS::post("SEMESTER") != "") {
            $this->semester = VARS::post("SEMESTER");
        }

        $this->cmd = VARS::request("cmd");
        $this->exp_year = CTRL_YEAR;

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name     = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade    = VARS::get("GRADE");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->semester);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //校種を取得
        $db = Query::dbCheckOut();
        $this->school_kind = $db->getOne(knjd139rQuery::getSchoolKind($this->grade));
        if (!strlen($this->school_kind)) $this->school_kind = "H";
        Query::dbCheckIn($db);

        //探究テーマ
        $this->getPro["TANKYU_THEME01"]["moji"] = 30;
        $this->getPro["TANKYU_THEME01"]["gyou"] = 1;
        $this->getPro["TANKYU_THEME02"]["moji"] = 30;
        $this->getPro["TANKYU_THEME02"]["gyou"] = 1;
        
        //特別活動の記録
        ////生徒会活動
        $this->getPro["COMMITTEE01"]["moji"] = 20;
        $this->getPro["COMMITTEE01"]["gyou"] = 1;
        $this->getPro["COMMITTEE02"]["moji"] = 20;
        $this->getPro["COMMITTEE02"]["gyou"] = 1;
        $this->getPro["COMMITTEE03"]["moji"] = 20;
        $this->getPro["COMMITTEE03"]["gyou"] = 1;
        
        ////学級の活動
        $this->getPro["CLASSACT01"]["moji"] = 20;
        $this->getPro["CLASSACT01"]["gyou"] = 1;
        $this->getPro["CLASSACT02"]["moji"] = 20;
        $this->getPro["CLASSACT02"]["gyou"] = 1;
        $this->getPro["CLASSACT03"]["moji"] = 20;
        $this->getPro["CLASSACT03"]["gyou"] = 1;
        
        ////部活動
        $this->getPro["CLUBACT01"]["moji"] = 20;
        $this->getPro["CLUBACT01"]["gyou"] = 1;
        $this->getPro["CLUBACT02"]["moji"] = 20;
        $this->getPro["CLUBACT02"]["gyou"] = 1;
        $this->getPro["CLUBACT03"]["moji"] = 20;
        $this->getPro["CLUBACT03"]["gyou"] = 1;
        
        ////その他
        $this->getPro["OTHER01"]["moji"] = 20;
        $this->getPro["OTHER01"]["gyou"] = 1;
        $this->getPro["OTHER02"]["moji"] = 20;
        $this->getPro["OTHER02"]["gyou"] = 1;
        $this->getPro["OTHER03"]["moji"] = 20;
        $this->getPro["OTHER03"]["gyou"] = 1;
        $this->getPro["OTHER04"]["moji"] = 20;
        $this->getPro["OTHER04"]["gyou"] = 1;
        $this->getPro["OTHER05"]["moji"] = 20;
        $this->getPro["OTHER05"]["gyou"] = 1;
        $this->getPro["OTHER06"]["moji"] = 20;
        $this->getPro["OTHER06"]["gyou"] = 1;
        
        //出欠状況備考
        if ($this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_".$this->school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_".$this->school_kind]);
            $this->getPro["ATTENDRECREMARK"]["moji"] = (int)trim($moji);
            $this->getPro["ATTENDRECREMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["ATTENDRECREMARK"]["moji"] = 15;
            $this->getPro["ATTENDRECREMARK"]["gyou"] = 1;
        }

        //所見
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$this->school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$this->school_kind]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            // $this->getPro["COMMUNICATION"]["moji"] = ($this->school_kind == "J") ? 40 : 32;
            // $this->getPro["COMMUNICATION"]["gyou"] = ($this->school_kind == "J") ? 7 : 5;
            $this->getPro["COMMUNICATION"]["moji"] = 16;
            $this->getPro["COMMUNICATION"]["gyou"] = 8;
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if (!strlen($this->semester)) {
            $this->setWarning("MSG304", "( 学期 )");
            return false;
        }

        //文字数チェック
        if (strlen($this->field["ATTENDRECREMARK"]) > 0) {
            if ($this->validate_row_cnt($this->field["ATTENDRECREMARK"], ($this->getPro["ATTENDRECREMARK"]["moji"] * 2)) > $this->getPro["ATTENDRECREMARK"]["gyou"]) {
                $this->setWarning("MSG915","(出欠状況備考は{$this->getPro["ATTENDRECREMARK"]["gyou"]}行{$this->getPro["ATTENDRECREMARK"]["moji"]}文字までです)");
                return false;
            }
        }
        if ($this->school_kind == "J") {
            if (strlen($this->field["COMMUNICATION"]) > 0) {
                if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
                    $this->setWarning("MSG915","(所見は{$this->getPro["COMMUNICATION"]["gyou"]}行{$this->getPro["COMMUNICATION"]["moji"]}文字までです)");
                    return false;
                }
            }
        }

        //探究テーマ
        if (strlen($this->field["TANKYU_THEME01"]) > 0) {
            if ($this->validate_row_cnt($this->field["TANKYU_THEME01"], ($this->getPro["TANKYU_THEME01"]["moji"] * 2)) > $this->getPro["TANKYU_THEME01"]["gyou"]) {
                $gyoStr = $this->getPro["TANKYU_THEME01"]["gyo"] > 1 ? $this->getPro["TANKYU_THEME01"]["gyo"]."行" : "";
                $this->setWarning("MSG915","(探究テーマは{$gyoStr}{$this->getPro["TANKYU_THEME01"]["moji"]}文字までです)");
                return false;
            }
        }
        if (strlen($this->field["TANKYU_THEME02"]) > 0) {
            if ($this->validate_row_cnt($this->field["TANKYU_THEME02"], ($this->getPro["TANKYU_THEME02"]["moji"] * 2)) > $this->getPro["TANKYU_THEME02"]["gyou"]) {
                $gyoStr = $this->getPro["TANKYU_THEME02"]["gyo"] > 1 ? $this->getPro["TANKYU_THEME02"]["gyo"]."行" : "";
                $this->setWarning("MSG915","(探究テーマは{$gyoStr}{$this->getPro["TANKYU_THEME02"]["moji"]}文字までです)");
                return false;
            }
        }

        //生徒会活動
        if (strlen($this->field["COMMITTEE01"]) > 0) {
            if ($this->validate_row_cnt($this->field["COMMITTEE01"], ($this->getPro["COMMITTEE01"]["moji"] * 2)) > $this->getPro["COMMITTEE01"]["gyou"]) {
                $gyoStr = $this->getPro["COMMITTEE01"]["gyo"] > 1 ? $this->getPro["COMMITTEE01"]["gyo"]."行" : "";
                $this->setWarning("MSG915","(生徒会活動は{$gyoStr}{$this->getPro["COMMITTEE01"]["moji"]}文字までです)");
                return false;
            }
        }
        if (strlen($this->field["COMMITTEE02"]) > 0) {
            if ($this->validate_row_cnt($this->field["COMMITTEE02"], ($this->getPro["COMMITTEE02"]["moji"] * 2)) > $this->getPro["COMMITTEE02"]["gyou"]) {
                $gyoStr = $this->getPro["COMMITTEE02"]["gyo"] > 1 ? $this->getPro["COMMITTEE02"]["gyo"]."行" : "";
                $this->setWarning("MSG915","(生徒会活動は{$gyoStr}{$this->getPro["COMMITTEE02"]["moji"]}文字までです)");
                return false;
            }
        }
        if (strlen($this->field["COMMITTEE03"]) > 0) {
            if ($this->validate_row_cnt($this->field["COMMITTEE03"], ($this->getPro["COMMITTEE03"]["moji"] * 2)) > $this->getPro["COMMITTEE03"]["gyou"]) {
                $gyoStr = $this->getPro["COMMITTEE03"]["gyo"] > 1 ? $this->getPro["COMMITTEE03"]["gyo"]."行" : "";
                $this->setWarning("MSG915","(生徒会活動は{$gyoStr}{$this->getPro["COMMITTEE03"]["moji"]}文字までです)");
                return false;
            }
        }

        //学級の活動
        if (strlen($this->field["CLASSACT01"]) > 0) {
            if ($this->validate_row_cnt($this->field["CLASSACT01"], ($this->getPro["CLASSACT01"]["moji"] * 2)) > $this->getPro["CLASSACT01"]["gyou"]) {
                $gyoStr = $this->getPro["CLASSACT01"]["gyo"] > 1 ? $this->getPro["CLASSACT01"]["gyo"]."行" : "";
                $this->setWarning("MSG915","(学級の活動は{$gyoStr}{$this->getPro["CLASSACT01"]["moji"]}文字までです)");
                return false;
            }
        }
        if (strlen($this->field["CLASSACT02"]) > 0) {
            if ($this->validate_row_cnt($this->field["CLASSACT02"], ($this->getPro["CLASSACT02"]["moji"] * 2)) > $this->getPro["CLASSACT02"]["gyou"]) {
                $gyoStr = $this->getPro["CLASSACT02"]["gyo"] > 1 ? $this->getPro["CLASSACT02"]["gyo"]."行" : "";
                $this->setWarning("MSG915","(学級の活動は{$gyoStr}{$this->getPro["CLASSACT02"]["moji"]}文字までです)");
                return false;
            }
        }
        if (strlen($this->field["CLASSACT03"]) > 0) {
            if ($this->validate_row_cnt($this->field["CLASSACT03"], ($this->getPro["CLASSACT03"]["moji"] * 2)) > $this->getPro["CLASSACT03"]["gyou"]) {
                $gyoStr = $this->getPro["CLASSACT03"]["gyo"] > 1 ? $this->getPro["CLASSACT03"]["gyo"]."行" : "";
                $this->setWarning("MSG915","(学級の活動は{$gyoStr}{$this->getPro["CLASSACT03"]["moji"]}文字までです)");
                return false;
            }
        }

        //部活動
        if (strlen($this->field["CLUBACT01"]) > 0) {
            if ($this->validate_row_cnt($this->field["CLUBACT01"], ($this->getPro["CLUBACT01"]["moji"] * 2)) > $this->getPro["CLUBACT01"]["gyou"]) {
                $gyoStr = $this->getPro["CLUBACT01"]["gyo"] > 1 ? $this->getPro["CLUBACT01"]["gyo"]."行" : "";
                $this->setWarning("MSG915","(部活動は{$gyoStr}{$this->getPro["CLUBACT01"]["moji"]}文字までです)");
                return false;
            }
        }
        if (strlen($this->field["CLUBACT02"]) > 0) {
            if ($this->validate_row_cnt($this->field["CLUBACT02"], ($this->getPro["CLUBACT02"]["moji"] * 2)) > $this->getPro["CLUBACT02"]["gyou"]) {
                $gyoStr = $this->getPro["CLUBACT02"]["gyo"] > 1 ? $this->getPro["CLUBACT02"]["gyo"]."行" : "";
                $this->setWarning("MSG915","(部活動は{$gyoStr}{$this->getPro["CLUBACT02"]["moji"]}文字までです)");
                return false;
            }
        }
        if (strlen($this->field["CLUBACT03"]) > 0) {
            if ($this->validate_row_cnt($this->field["CLUBACT03"], ($this->getPro["CLUBACT03"]["moji"] * 2)) > $this->getPro["CLUBACT03"]["gyou"]) {
                $gyoStr = $this->getPro["CLUBACT03"]["gyo"] > 1 ? $this->getPro["CLUBACT03"]["gyo"]."行" : "";
                $this->setWarning("MSG915","(部活動は{$gyoStr}{$this->getPro["CLUBACT03"]["moji"]}文字までです)");
                return false;
            }
        }

        //その他
        if (strlen($this->field["OTHER01"]) > 0) {
            if ($this->validate_row_cnt($this->field["OTHER01"], ($this->getPro["OTHER01"]["moji"] * 2)) > $this->getPro["OTHER01"]["gyou"]) {
                $gyoStr = $this->getPro["OTHER01"]["gyo"] > 1 ? $this->getPro["OTHER01"]["gyo"]."行" : "";
                $this->setWarning("MSG915","(その他は{$gyoStr}{$this->getPro["OTHER01"]["moji"]}文字までです)");
                return false;
            }
        }
        if (strlen($this->field["OTHER02"]) > 0) {
            if ($this->validate_row_cnt($this->field["OTHER02"], ($this->getPro["OTHER02"]["moji"] * 2)) > $this->getPro["OTHER02"]["gyou"]) {
                $gyoStr = $this->getPro["OTHER02"]["gyo"] > 1 ? $this->getPro["OTHER02"]["gyo"]."行" : "";
                $this->setWarning("MSG915","(その他は{$gyoStr}{$this->getPro["OTHER02"]["moji"]}文字までです)");
                return false;
            }
        }
        if (strlen($this->field["OTHER03"]) > 0) {
            if ($this->validate_row_cnt($this->field["OTHER03"], ($this->getPro["OTHER03"]["moji"] * 2)) > $this->getPro["OTHER03"]["gyou"]) {
                $gyoStr = $this->getPro["OTHER03"]["gyo"] > 1 ? $this->getPro["OTHER03"]["gyo"]."行" : "";
                $this->setWarning("MSG915","(その他は{$gyoStr}{$this->getPro["OTHER03"]["moji"]}文字までです)");
                return false;
            }
        }
        if ($this->school_kind == "H") {
            if (strlen($this->field["OTHER04"]) > 0) {
                if ($this->validate_row_cnt($this->field["OTHER04"], ($this->getPro["OTHER04"]["moji"] * 2)) > $this->getPro["OTHER04"]["gyou"]) {
                    $gyoStr = $this->getPro["OTHER04"]["gyo"] > 1 ? $this->getPro["OTHER04"]["gyo"]."行" : "";
                    $this->setWarning("MSG915","(その他は{$gyoStr}{$this->getPro["OTHER04"]["moji"]}文字までです)");
                    return false;
                }
            }
            if (strlen($this->field["OTHER05"]) > 0) {
                if ($this->validate_row_cnt($this->field["OTHER05"], ($this->getPro["OTHER05"]["moji"] * 2)) > $this->getPro["OTHER05"]["gyou"]) {
                    $gyoStr = $this->getPro["OTHER05"]["gyo"] > 1 ? $this->getPro["OTHER05"]["gyo"]."行" : "";
                    $this->setWarning("MSG915","(その他は{$gyoStr}{$this->getPro["OTHER05"]["moji"]}文字までです)");
                    return false;
                }
            }
            if (strlen($this->field["OTHER06"]) > 0) {
                if ($this->validate_row_cnt($this->field["OTHER06"], ($this->getPro["OTHER06"]["moji"] * 2)) > $this->getPro["OTHER06"]["gyou"]) {
                    $gyoStr = $this->getPro["OTHER06"]["gyo"] > 1 ? $this->getPro["OTHER06"]["gyo"]."行" : "";
                    $this->setWarning("MSG915","(その他は{$gyoStr}{$this->getPro["OTHER06"]["moji"]}文字までです)");
                    return false;
                }
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
        knjd139rQuery::getUpdateQuery($this, $this->field, $this->schregno);
        return;
    }
}
?>
