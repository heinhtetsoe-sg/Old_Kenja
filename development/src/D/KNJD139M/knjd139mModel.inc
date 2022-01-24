<?php

require_once('for_php7.php');

class knjd139mModel extends Model {
    var $cmd;
    var $exp_year;
    var $schregno;
    var $name;
    var $schoolKind;
    var $field = array();

    function knjd139mModel() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }
        $this->cmd = VARS::request("cmd");

        $this->field = array("TOTALSTUDYTIME"       =>  VARS::post("TOTALSTUDYTIME"),   // 総合的な学習の時間
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"), // 出欠の備考
                             "REMARK4_07_00"        =>  VARS::post("REMARK4_07_00"),    // 評価
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION"),    // 所見
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        //観点
        foreach ((array) $this->kantenArr as $nameCd2 => $valArr) {
            $this->field["REMARK1_".$nameCd2] = VARS::post("REMARK1_".$nameCd2);
        }

        //行動の記録
        foreach ((array) $this->recordArr as $code => $valArr) {
            $this->field["RECORD".$code] = VARS::post("RECORD".$code);
        }

        //左フレームから取得
        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade     = VARS::get("GRADE");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }

        //校種の取得
        $db = Query::dbCheckOut();
        $query = knjd139mQuery::getSchoolKind($this);
        $this->schoolKind = $db->getOne($query);
        Query::dbCheckIn($db);

        if (VARS::post("SEMESTER") != "") {
            $this->semester = VARS::post("SEMESTER");
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

        //総合的な学習の時間
        $this->getPro["TOTALSTUDYTIME"]["moji"] = 15;
        $this->getPro["TOTALSTUDYTIME"]["gyou"] = 2;

        //出欠の備考
        if ($this->semester == '9') {
            $this->getPro["ATTENDREC_REMARK"]["moji"] = 10;
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = 5;
        } else {
            $this->getPro["ATTENDREC_REMARK"]["moji"] = 10;
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = 3;
        }

        //評価
        $this->getPro["REMARK4_07_00"]["moji"] = 15;
        $this->getPro["REMARK4_07_00"]["gyou"] = 5;

        //所見
        $this->getPro["COMMUNICATION"]["moji"] = 13;
        $this->getPro["COMMUNICATION"]["gyou"] = 4;
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        //文字数チェック
        $chkArr = array();
        $chkArr[] = array("fieldName" => "TOTALSTUDYTIME",      "j_Title" => "総合的な学習の時間");
        $chkArr[] = array("fieldName" => "ATTENDREC_REMARK",    "j_Title" => "出欠の備考");
        $chkArr[] = array("fieldName" => "REMARK4_07_00",       "j_Title" => "評価");
        $chkArr[] = array("fieldName" => "COMMUNICATION",       "j_Title" => "所見");

        foreach ($chkArr as $key => $valArr) {
            if ($this->validate_row_cnt($this->field["{$valArr["fieldName"]}"], ($this->getPro["{$valArr["fieldName"]}"]["moji"] * 2)) > $this->getPro["{$valArr["fieldName"]}"]["gyou"]) {
                $this->setWarning("MSG915","({$valArr["j_Title"]}は{$this->getPro["{$valArr["fieldName"]}"]["gyou"]}行までです)");
                return false;
            }
        }

        // 値チェック
        //観点
        $setMsg1 = implode(", ", $this->recordlist1);
        foreach ((array) $this->kantenArr as $nameCd2 => $valArr) {
            if ($this->field["REMARK1_".$nameCd2] != '') {
                if (!in_array($this->field["REMARK1_".$nameCd2], $this->recordlist1, true)) {
                    $this->setWarning("MSG901", "観点は「{$setMsg1}」を入力して下さい。");
                }
            }
        }

        //行動の記録
        $setMsg2 = implode(", ", $this->recordlist2);
        foreach ((array) $this->recordArr as $code => $valArr) {
            if ($this->field["RECORD".$code] != '') {
                if (!in_array($this->field["RECORD".$code], $this->recordlist2, true)) {
                    $this->setWarning("MSG901", "行動の記録は「{$setMsg1}」を入力して下さい。");
                }
            }
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
        knjd139mQuery::getInsertQuery($this);

        return ;
    }
}
?>
