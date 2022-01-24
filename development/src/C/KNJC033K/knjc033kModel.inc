<?php

require_once('for_php7.php');

/**
 * 定数定義
 */
class knjc033kModel extends Model {
    var $field      = array();
    var $reset      = array();
    var $subfield   = array();
    var $sickdiv    = array();
    var $cmd;
    var $schregno;
    var $chaircd;
    var $attendno;
    var $grade;
    var $appointed_day;
    var $lesson;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjc033kModel()
    {
        $this->field = array();
    }

    function init()
    {
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd = VARS::request("cmd");

        //欠課種別コード
        $this->sickdiv[1] = "ABSENT";
        $this->sickdiv[2] = "SUSPEND";
        $this->sickdiv[3] = "MOURNING";
        $this->sickdiv[4] = "SICK";
        $this->sickdiv[5] = "NOTICE";
        $this->sickdiv[6] = "NONOTICE";
        $this->sickdiv[15] = "LATE";
        $this->sickdiv[16] = "EARLY";
        $this->sickdiv[19] = "VIRUS";
        $this->sickdiv[25] = "KOUDOME";
        $this->field["SICK"] = VARS::request("SICK");

        $gradeAppDay = VARS::post("GradeAppDay");
        $this->gradeAppDay = array();
        if ($gradeAppDay) {
            $getAppGradeApp = preg_split("/:/", $gradeAppDay);
            foreach ($getAppGradeApp as $key => $val) {
                list($grade, $appDay) = preg_split("/_/", $val);
                $this->gradeAppDay[$grade] = $appDay;
            }
        }

        $schGrade = VARS::post("SchGradeArray");
        $this->schGradeArray = array();
        if ($schGrade) {
            $getSchGrade = preg_split("/:/", $schGrade);
            foreach ($getSchGrade as $key => $val) {
                list($schregNo, $grade) = preg_split("/_/", $val);
                $this->schGradeArray[$schregNo] = $grade;
            }
        }

        if ($this->Properties["useSchool_KindField"] == "1") {
            $this->field["SCHOOL_KIND"] = SCHOOLKIND;
        }
        //課程学科コード
        if ($this->Properties["use_school_detail_gcm_dat"] == "1") {
            $this->field["COURSE_MAJOR"] = VARS::request("COURSE_MAJOR");
        }

        //科目コード
        $this->field["SUBCLASSCD"] = VARS::request("SUBCLASSCD");

        //講座コード
        $this->field["CHAIRCD"] = VARS::request("CHAIRCD");

        //月コード
        $month = preg_split("/-/",VARS::request("MONTHCD"));
        $this->field["MONTHCD"]  = VARS::request("MONTHCD");
        $this->field["MONTH"]    = $month[0];
        $this->field["SEMESTER"] = $month[1];

        //締め日
        $this->appointed_day = VARS::post("APPOINTED_DAY");

        //授業時数
        $this->lesson = VARS::post("LESSON");

        if ($this->cmd == "update") {
            $this->field["SUBCLASSCD"]  = VARS::post("HIDDEN_SUBCLASSCD");
            $this->field["CHAIRCD"]     = VARS::post("HIDDEN_CHAIRCD");
            $this->field["MONTHCD"]     = VARS::post("HIDDEN_MONTHCD");
            list($this->field["MONTH"], $this->field["SEMESTER"]) = explode("-", $this->field["MONTHCD"]);
            $this->field["SICK"]        = VARS::post("HIDDEN_SICK");
            $this->lesson               = VARS::post("HIDDEN_LESSON");
            if ($this->Properties["use_school_detail_gcm_dat"] == "1") {
                $this->field["COURSE_MAJOR"]    = VARS::post("HIDDEN_COURSE_MAJOR");
            }
        }

        $this->field["std_num"] = VARS::post("std_num");

        if (isset($this->field["std_num"])) {
            $this->reset = array();
            for($i = 1; $i <= $this->field["std_num"]; $i++)
            {
                $temp = "SCHREGNO".sprintf("%02d", $i);
                $this->field[$temp] = VARS::post($temp);
                $resetKey = VARS::post($temp);

                $temp = $this->sickdiv[$this->field["SICK"]]."-".sprintf("%02d", $i);
                $this->field[$temp] = VARS::post($temp);
                $resetVal = VARS::post($temp);

                $this->reset[$resetKey] = $resetVal;
            }
        }
        if (VARS::get("cmd") && VARS::get("cmd") == "newpopup") {
            if (VARS::get("SCHREGNO") && VARS::get("CHAIRCD") && VARS::get("ATTENDNO") && VARS::get("GRADE")) {
                $this->schregno = VARS::get("SCHREGNO");
                $this->chaircd  = VARS::get("CHAIRCD");
                $this->attendno = VARS::get("ATTENDNO");
                $this->grade    = VARS::get("GRADE");
            }
        } else if (VARS::get("cmd") && VARS::get("cmd") != "back") {
            unset($this->schregno);
            unset($this->chaircd);
            unset($this->attendno);
            unset($this->grade);
        }

        if ($this->cmd == "replace_update") {

            $this->subfield["SELECTDATA"]  = VARS::post("selectdata");

            $this->setSubFields("RCHECK1", "APPOINTED_DAY");
            $this->setSubFields("RCHECK2", "LESSON");
        }

    }

    /** 一括更新データセット */
    function setSubFields($post, $name)
    {
        if (VARS::post($post) == "1") {
            $this->subfield[$name]  = VARS::request($name);
            $this->subfield[$post]  = VARS::request($post);
        }
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        /*  更新処理 */
        if(0 < $this->field["std_num"]){
            if ($this->validateInput()) {
                $this->update();
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG304");
            return false;
        }
    }

    /* 入力項目チェック */
    function validateInput()
    {
        if ($this->lesson == "") {
            $this->setWarning("MSG301","授業時数");
            return false;
        }

        $setAppNullErr = "";
        $setApp0Err = "";
        foreach ($this->gradeAppDay as $key => $val) {
            if ($val == "") {
                $setAppNullErr .= ($key * 1)."年 ";
            }
            if ($val == 0) {
                $setApp0Err .= ($key * 1)."年 ";
            }
        }
        if ($setAppNullErr != "") {
            $this->setWarning("MSG301","締め日:".$setAppNullErr);
            return false;
        }
        if ($setApp0Err != "") {
            $this->setWarning("MSG301","0日は存在しません。:".$setApp0Err);
            return false;
        }

        return true;
    }

    function &update()
    {
        knjc033kQuery::getUpdateQuery($this);
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModelSub()
    {
        if ($this->validateInputSub()) {
            $this->updateSub();
            $this->setMessage("MSG201");
        }
        return;
    }

    /* 入力項目チェック */
    function validateInputSub()
    {
        $db = Query::dbCheckOut();

        if ($this->subfield["RCHECK1"]) {
            //締め日が0の場合
            if (0 == $this->subfield["APPOINTED_DAY"]) {
                $this->setWarning("MSG901","0日は存在しません。");
                return false;
            }

            $semeday = $db->getRow(knjc033kQuery::selectSemesAll($this->field["SEMESTER"]),DB_FETCHMODE_ASSOC);
            //学期マスタの開始日より小さい場合
            if (sprintf('%02d', $semeday["S_MONTH"]) == $this->field["MONTH"] &&
                $semeday["S_DAY"] > $this->subfield["APPOINTED_DAY"]) {
                $this->setWarning("MSG901",$semeday["SEMESTERNAME"]."の開始日は、".$semeday["S_MONTH"]."月".$semeday["S_DAY"]."日です。");
                return false;
            }
            //学期マスタの最終日より大きい場合
            if (sprintf('%02d', $semeday["E_MONTH"]) == $this->field["MONTH"] &&
                $semeday["E_DAY"] < $this->subfield["APPOINTED_DAY"]) {
                $this->setWarning("MSG901",$semeday["SEMESTERNAME"]."の最終日は、".$semeday["E_MONTH"]."月".$semeday["E_DAY"]."日です。");
                return false;
            }

            //締め日の最終日
            $year = CTRL_YEAR;
            if ($this->field["MONTH"] < "04") {
                $year = CTRL_YEAR + 1;
            }
            $lastday = date("t", mktime( 0, 0, 0, $this->field["MONTH"], 1, $year ));

            //締め日がその月の最終日より大きい場合
            if ($lastday < $this->subfield["APPOINTED_DAY"]) {
                $this->setWarning("MSG901",$this->field["MONTH"]."月の最終日は、".$lastday."日です。");
                return false;
            }
        }
        Query::dbCheckIn($db);

        return true;
    }

    /*  更新処理 */
    function &updateSub()
    {
        knjc033kQuery::getUpdateSubQuery($this);
    }

}
?>
