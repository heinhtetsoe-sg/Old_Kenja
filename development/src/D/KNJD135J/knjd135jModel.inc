<?php

require_once('for_php7.php');

class knjd135jModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $control;
    var $detail;
    var $schoolName;
    var $maxSemester;

    function knjd135jModel() {
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
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "MORAL"                =>  VARS::post("MORAL"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $db = Query::dbCheckOut();

        //学校を判断
        $this->schoolName = $db->getOne(knjd135jQuery::getSchoolName());

        //学期制取得
        $this->maxSemester = $db->getOne(knjd135jQuery::getMaxSemester());

        Query::dbCheckIn($db);

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

        //総合的な学習の時間の判定
        $sem = ($this->Properties["tutisyoSougouHyoukaTunen"] == 1) ? "9" : "";
        
        $this->detail = null;
        $this->detail[] = array($sem, "01", "01", "REMARK1", 25, 9, "総合的な学習の時間・学習活動");
        $this->detail[] = array($sem, "01", "02", "REMARK1", 32, 9, "総合的な学習の時間・評価");
        $this->detail[] = array("",   "02", "01", "REMARK1", 27, 9, "特別活動");
        $this->detail[] = array("9",  "03", "01", "REMARK1", 30, 9, "部活動");
        if($this->maxSemester == "3"){
            if ($this->Properties["tutisyoShokenntunen"] == "1") {
                $setsemsval = "9";
            } else {
                $setsemsval = "";
            }
            $this->detail[] = array($setsemsval,   "04", "01", "REMARK1", 24, 9, "所見");
        } else {
            $this->detail[] = array("",   "04", "01", "REMARK1", 21, 7, "所見");
        }

        foreach ($this->detail as $key) {
            list ($semester, $div, $code, $field, $default_moji, $default_gyou, $comment) = $key;
            $p_name = "reportSpecialSize".$div."_".$code;
            $name   = 'DETAIL_'.$div.'_'.$code.'_'.$field;

            $this->field[$name] = VARS::post($name);

            //文字数・行数
            if ($this->Properties[$p_name]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties[$p_name]);
                $this->getPro[$name]["moji"] = (int)trim($moji);
                $this->getPro[$name]["gyou"] = (int)trim($gyou);
            } else {
                $this->getPro[$name]["moji"] = $default_moji;
                $this->getPro[$name]["gyou"] = $default_gyou;
            }
        }

        if ($this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]);
            $this->getPro["ATTENDREC_REMARK"]["moji"] = (int)trim($moji);
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["ATTENDREC_REMARK"]["moji"] = 30;
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = 1;
        }
        
        if($this->maxSemester == "3"){
            $this->getPro["MORAL"]["moji"] = 24;
            $this->getPro["MORAL"]["gyou"] = 9;
        } else {
            $this->getPro["MORAL"]["moji"] = 23;
            $this->getPro["MORAL"]["gyou"] = 14;
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
        foreach ($this->detail as $key) {
            list ($semester, $div, $code, $field, $default_moji, $default_gyou, $comment) = $key;
            $name = 'DETAIL_'.$div.'_'.$code.'_'.$field;
            if ($this->validate_row_cnt($this->field[$name], ($this->getPro[$name]["moji"] * 2)) > $this->getPro[$name]["gyou"]) {
                $this->setWarning("MSG915","({$comment}は{$this->getPro[$name]["gyou"]}行までです)");
                return false;
            }
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->getPro["ATTENDREC_REMARK"]["moji"] * 2)) > $this->getPro["ATTENDREC_REMARK"]["gyou"]) {
            $this->setWarning("MSG915","(出欠の記録備考は{$this->getPro["ATTENDREC_REMARK"]["gyou"]}行までです)");
            return false;
        }

        if ($this->schoolName == "kyoto") {
            if ($this->validate_row_cnt($this->field["MORAL"], ($this->getPro["MORAL"]["moji"] * 2)) > $this->getPro["MORAL"]["gyou"]) {
                $this->setWarning("MSG915","(道徳は{$this->getPro["MORAL"]["gyou"]}行までです)");
                return false;
            }
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
        knjd135jQuery::getUpdateQuery($this);
        return ;
    }
}
?>
