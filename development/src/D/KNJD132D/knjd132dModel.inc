<?php

require_once('for_php7.php');
class knjd132dModel extends Model {
    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $grade;
    var $field = array();
    var $record = array();
    var $ikkatsu_data = array();
    var $semesCnt;

    function knjd132dModel() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),
                             "TOTALSTUDYTIME"       =>  VARS::post("TOTALSTUDYTIME"),   //内容
                             "REMARK1"              =>  VARS::post("REMARK1"),          //取り組みのようす
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"), //出席のようす備考
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION"),    //学校から
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );
        $this->field["TEIKEI_CMD"] = VARS::get("TEIKEI_CMD");

        //生活のようす
        if (is_array($this->itemArrayL)) {
            foreach ($this->itemArrayL as $Lkey => $Lval) {
                foreach ($this->itemArrayM[$Lkey] as $Mkey => $Mval) {
                    for ($semes = 1; $semes <= $this->semesCnt; $semes++){
                        $lmKey = $Lkey."_".$Mkey."_".$semes;
                        $this->record["RECORD"][$lmKey] = VARS::post("RECORD_{$lmKey}");
                    }
                }
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
        if (VARS::get("HR_CLASS") != "") {
            $this->hr_class = VARS::get("HR_CLASS");
        }
        if (VARS::get("sendSEME") != "") {
            $this->ikkatsuSeme = VARS::get("sendSEME");
        }

        //校種取得
        $db = Query::dbCheckOut();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $query = knjd132dQuery::getSchoolKind($this);
            $this->schoolkind = $db->getOne($query);
        } else if ($this->Properties["useSchool_KindField"] == "1") {
            $this->schoolkind = SCHOOLKIND;
        } else {
            $query = knjd132dQuery::getSchoolKind($this);
            $this->schoolkind = $db->getOne($query);
        }

        //GRADE_CD取得
        $query = knjd132dQuery::getGradeCd($this);
        $this->grade_cd = $db->getOne($query);
        
        //学期制取得
        $query = knjd132dQuery::getSemesterCnt($this);
        $this->semesCnt = $db->getOne($query);

        Query::dbCheckIn($db);

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

        //一括更新用
        if ($this->cmd == "ikkatsu_update") {
            $this->ikkatsu_data["selectdata"] = VARS::post("selectdata");
            $this->ikkatsu_data["hreport"] = array();
            $this->ikkatsu_data["check"] = array(VARS::post("CHECK_ALL"),//ALL
                                                 VARS::post("CHECK1"),
                                                 VARS::post("CHECK2"),
                                                 VARS::post("CHECK3"),
                                                 VARS::post("CHECK4"));
            if (VARS::post("CHECK1") == "1") $this->ikkatsu_data["hreport"]["TOTALSTUDYTIME"]   = VARS::post("TOTALSTUDYTIME");
            if (VARS::post("CHECK2") == "1") $this->ikkatsu_data["hreport"]["REMARK1"]          = VARS::post("REMARK1");
            if (VARS::post("CHECK3") == "1") $this->ikkatsu_data["hreport"]["ATTENDREC_REMARK"] = VARS::post("ATTENDREC_REMARK");
            if (VARS::post("CHECK4") == "1") $this->ikkatsu_data["hreport"]["COMMUNICATION"]    = VARS::post("COMMUNICATION");
        }
        if(VARS::get("ini2")) {
            unset($this->ikkatsu_data);
        }

        //入力文字初期値の設定
        //内容
        if ($this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P"]);
            $this->totalstudytime_moji = (int)trim($moji);
            $this->totalstudytime_gyou = (int)trim($gyou);
        } else {
            $this->totalstudytime_moji = 45;
            $this->totalstudytime_gyou = 4;
        }

        //取り組みのようす
        if ($this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_P"]);
            $this->remark1_moji = (int)trim($moji);
            $this->remark1_gyou = (int)trim($gyou);
        } else {
            $this->remark1_moji = 45;
            $this->remark1_gyou = 4;
        }

        //出席のようす備考
        if ($this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 9;
            $this->attendrec_remark_gyou = 1;
        }

        //学校から
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_P"]);
            $this->communication_moji = (int)trim($moji);
            $this->communication_gyou = (int)trim($gyou);
        } else {
            $this->communication_moji = 45;
            $this->communication_gyou = 5;
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
        if ($this->validate_row_cnt($this->field["TOTALSTUDYTIME"], ($this->totalstudytime_moji * 2)) > $this->totalstudytime_gyou) {
            $this->setWarning("MSG915","(内容は{$this->totalstudytime_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK1"], ($this->remark1_moji * 2)) > $this->remark1_gyou) {
            $this->setWarning("MSG915","(取り組みのようすは{$this->remark1_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
            $this->setWarning("MSG915","(出席のようす備考は{$this->attendrec_remark_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->communication_moji * 2)) > $this->communication_gyou) {
            $this->setWarning("MSG915","(学校からは{$this->communication_gyou}行までです)");
            return false;
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        //HREPORTREMARK_DAT
        knjd132dQuery::getInsertQuery($this);
        //HREPORT_BEHAVIOR_LM_DAT
        knjd132dQuery::getInsertQuery2($this);

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
            if ($this->validate_row_cnt($this->field["TOTALSTUDYTIME"], ($this->totalstudytime_moji * 2)) > $this->totalstudytime_gyou) {
                $this->setWarning("MSG915","(内容は{$this->totalstudytime_gyou}行までです)");
                return false;
            }
        }
        if (VARS::post("CHECK2") == "1") {
            if ($this->validate_row_cnt($this->field["REMARK1"], ($this->remark1_moji * 2)) > $this->remark1_gyou) {
                $this->setWarning("MSG915","(取り組みのようすは{$this->remark1_gyou}行までです)");
                return false;
            }
        }
        if (VARS::post("CHECK3") == "1") {
            if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
                $this->setWarning("MSG915","(出席のようす備考は{$this->attendrec_remark_gyou}行までです)");
                return false;
            }
        }
        if (VARS::post("CHECK4") == "1") {
            if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->communication_moji * 2)) > $this->communication_gyou) {
                $this->setWarning("MSG915","(学校からは{$this->communication_gyou}行までです)");
                return false;
            }
        }

        $db = Query::dbCheckOut();
        knjd132dQuery::IkkatsuUpdate($db, $this);
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }
}
?>
