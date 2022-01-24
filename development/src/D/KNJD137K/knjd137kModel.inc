<?php

require_once('for_php7.php');
class knjd137kModel extends Model {
    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $grade;
    var $field = array();
    var $control;

    function knjd137kModel() {
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

        $this->field = array("PRINT_SEMESTER"       =>  VARS::post("PRINT_SEMESTER"),   //印刷学期
                             "REMARK1"              =>  VARS::post("REMARK1"),          //道徳
                             "REMARK1_03_01"        =>  VARS::post("REMARK1_03_01"),    //自立活動（学習内容）
                             "REMARK3"              =>  VARS::post("REMARK3"),          //自立活動（学習の様子）
                             "REMARK1_04_01"        =>  VARS::post("REMARK1_04_01"),    //総合（テーマ）
                             "TOTALSTUDYTIME"       =>  VARS::post("TOTALSTUDYTIME"),   //総合（活動の様子）
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION"),    //総合所見
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $db = Query::dbCheckOut();
        //出力項目取得(特別活動)
        $query = knjd137kQuery::getNameMst($this, "D038");
        $result = $db->query($query);
        $this->itemArrayD038 = array();
        while ($setItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->itemArrayD038["02_".$setItem["NAMECD2"]] = $setItem;
        }

        //出力項目取得(特別活動)
        $query = knjd137kQuery::getNameMst($this, "D034");
        $result = $db->query($query);
        $this->itemArrayD034 = array();
        $this->itemArrayD034Sort = array();
        while ($setItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->itemArrayD034["01_".$setItem["NAMECD2"]] = $setItem;
            $this->itemArrayD034Sort[] = "01_".$setItem["NAMECD2"];
        }
        Query::dbCheckIn($db);

        foreach ($this->itemArrayD038 as $key => $val) {
            $this->field["REMARK1_".$key] = VARS::post("REMARK1_".$key);
        }

        foreach ($this->itemArrayD034 as $key => $val) {
            $this->field["REMARK1_".$key] = VARS::post("REMARK1_".$key);
        }

        $this->cmd = VARS::request("cmd");

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
            unset($this->grade);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        /****************************/
        /* 文字数・行数の初期値設定 */
        /****************************/
        //道徳
        if ($this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_P"]);
            $this->getPro["REMARK1"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK1"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK1"]["moji"] = 50;
            $this->getPro["REMARK1"]["gyou"] = 3;
        }

        //外国語
        foreach ($this->itemArrayD038 as $key => $val) {
            if ($this->Properties["reportSpecialSize".$key]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize".$key]);
                $this->getPro["reportSpecial".$key]["moji"] = (int)trim($moji);
                $this->getPro["reportSpecial".$key]["gyou"] = (int)trim($gyou);
            } else {
                $this->getPro["reportSpecial".$key]["moji"] = 36;
                $this->getPro["reportSpecial".$key]["gyou"] = 1;
            }
        }

        //自立活動(学習内容)
        if ($this->Properties["reportSpecialSize03_01"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize03_01"]);
            $this->getPro["reportSpecial03_01"]["moji"] = (int)trim($moji);
            $this->getPro["reportSpecial03_01"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["reportSpecial03_01"]["moji"] = 12;
            $this->getPro["reportSpecial03_01"]["gyou"] = 3;
        }
        //自立活動(学習の様子)
        if ($this->Properties["HREPORTREMARK_DAT_REMARK3_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK3_SIZE_P"]);
            $this->getPro["REMARK3"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK3"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK3"]["moji"] = 36;
            $this->getPro["REMARK3"]["gyou"] = 3;
        }

        //総合的な学習の時間(テーマ)
        if ($this->Properties["reportSpecialSize04_01"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize04_01"]);
            $this->getPro["reportSpecial04_01"]["moji"] = (int)trim($moji);
            $this->getPro["reportSpecial04_01"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["reportSpecial04_01"]["moji"] = 12;
            $this->getPro["reportSpecial04_01"]["gyou"] = 3;
        }
        //総合的な学習の時間(活動の様子)
        if ($this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P"]);
            $this->getPro["TOTALSTUDYTIME"]["moji"] = (int)trim($moji);
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["TOTALSTUDYTIME"]["moji"] = 36;
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = 3;
        }

        //特別活動
        foreach ($this->itemArrayD034 as $key => $val) {
            if ($this->Properties["reportSpecialSize".$key]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["reportSpecialSize".$key]);
                $this->getPro["reportSpecial".$key]["moji"] = (int)trim($moji);
                $this->getPro["reportSpecial".$key]["gyou"] = (int)trim($gyou);
            } else {
                $this->getPro["reportSpecial".$key]["moji"] = 41;
                $this->getPro["reportSpecial".$key]["gyou"] = 2;
            }
        }

        //総合所見
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_P"]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 52;
            $this->getPro["COMMUNICATION"]["gyou"] = 7;
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        if ($this->validate_row_cnt($this->field["REMARK1"], ($this->getPro["REMARK1"]["moji"] * 2)) > $this->getPro["REMARK1"]["gyou"]) {
            $this->setWarning("MSG915","(道徳は{$this->getPro["REMARK1"]["gyou"]}行までです)");
            return false;
        }

        //１、２年以外
        if (!$this->grade01_02Flg) {
            //外国語
            foreach ($this->itemArrayD038 as $key => $val) {
                if ($this->validate_row_cnt($this->field["REMARK1_".$key], ($this->getPro["reportSpecial".$key]["moji"] * 2)) > $this->getPro["reportSpecial".$key]["gyou"]) {
                    $this->setWarning("MSG915","(外国語（{$val["NAME1"]}）は{$this->getPro["reportSpecial".$key]["gyou"]}行までです)");
                    return false;
                }
            }
            //総合学習（テーマ）
            if ($this->validate_row_cnt($this->field["REMARK1_04_01"], ($this->getPro["reportSpecial04_01"]["moji"] * 2)) > $this->getPro["reportSpecial04_01"]["gyou"]) {
                $this->setWarning("MSG915","(総合学習（テーマ）は{$this->getPro["reportSpecial04_01"]["gyou"]}行までです)");
                return false;
            }
            //総合学習（活動の様子）
            if ($this->validate_row_cnt($this->field["TOTALSTUDYTIME"], ($this->getPro["TOTALSTUDYTIME"]["moji"] * 2)) > $this->getPro["TOTALSTUDYTIME"]["gyou"]) {
                $this->setWarning("MSG915","(総合学習（活動の様子）は{$this->getPro["TOTALSTUDYTIME"]["gyou"]}行までです)");
                return false;
            }
        }
        //自立活動（学習内容）
        if ($this->validate_row_cnt($this->field["REMARK1_03_01"], ($this->getPro["reportSpecial03_01"]["moji"] * 2)) > $this->getPro["reportSpecial03_01"]["gyou"]) {
            $this->setWarning("MSG915","(自立活動（学習内容）は{$this->getPro["reportSpecial03_01"]["gyou"]}行までです)");
            return false;
        }
        //自立活動（学習の様子）
        if ($this->validate_row_cnt($this->field["REMARK3"], ($this->getPro["REMARK3"]["moji"] * 2)) > $this->getPro["REMARK3"]["gyou"]) {
            $this->setWarning("MSG915","(自立活動（学習の様子）は{$this->getPro["REMARK3"]["gyou"]}行までです)");
            return false;
        }
        //特別活動
        foreach ($this->itemArrayD034 as $key => $val) {
            if ($this->validate_row_cnt($this->field["REMARK1_".$key], ($this->getPro["reportSpecial".$key]["moji"] * 2)) > $this->getPro["reportSpecial".$key]["gyou"]) {
                $this->setWarning("MSG915","(特別活動（{$val["NAME1"]}）は{$this->getPro["reportSpecial".$key]["gyou"]}行までです)");
                return false;
            }
        }
        //総合所見
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
            $this->setWarning("MSG915","(総合所見は{$this->getPro["COMMUNICATION"]["gyou"]}行までです)");
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
        knjd137kQuery::getInsertQuery($this);
        return ;
    }
}
?>
