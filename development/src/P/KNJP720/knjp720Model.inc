<?php

require_once('for_php7.php');

require_once('../../common/mycalendar.php');

class knjp720Model extends Model {
    var $field = array();
    var $cmd;
    var $schoolkind;
    var $reduction_div_cd;
    var $schregno;
    var $s_year_month;

    function knjp720Model() {}

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->schoolkind);
            unset($this->reduction_div_cd);
            unset($this->schregno);
            unset($this->s_year_month);
            unset($this->flg);
            unset($this->grade);
            unset($this->grade_hr_class);
            unset($this->clubcd);
            $this->field = array();

            $this->right_frame = "edit";

        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SCHREGNO"             =>  VARS::post("SCHREGNO"),
                             "SCHOOL_KIND"          =>  VARS::post("SCHOOL_KIND"),
                             "REDUCTION_DIV_CD"     =>  VARS::post("REDUCTION_DIV_CD"),
                             "S_YEAR_MONTH"         =>  $this->getSeirekiHenkan(VARS::post("S_YEAR_MONTH")),
                             "E_YEAR_MONTH"         =>  $this->getSeirekiHenkan(VARS::post("E_YEAR_MONTH")),
                             "REMARK"               =>  VARS::post("REMARK")
                            );

        $this->cmd = VARS::request("cmd");

        $this->selectdata = VARS::post("selectdata");

        //list→edit
        if (VARS::request("SCHOOL_KIND") != "") {
            $this->schoolkind = VARS::request("SCHOOL_KIND");
        }
        if (VARS::request("REDUCTION_DIV_CD") != "") {
            $this->reduction_div_cd = VARS::request("REDUCTION_DIV_CD");
        }

        //絞り込み条件
        $this->grade    = VARS::request("GRADE");
        $this->grade_hr_class = VARS::request("GRADE_HR_CLASS");
        $this->clubcd   = VARS::request("CLUBCD");

        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("S_YEAR_MONTH")) {
            $this->s_year_month = str_replace("/", "-", VARS::get("S_YEAR_MONTH"));
        }
        if (VARS::get("FLG")) {
            $this->flg = VARS::get("FLG");
        }

        $this->right_frame = "edit";
        if (VARS::request("RIGHT_FRAME")) {
            $this->right_frame = VARS::request("RIGHT_FRAME");
        }


    }

    //入力番号検索
    function schreg_chk($schregno) {
        unset($this->flg);
        //必須チェック
        if (!strlen($schregno)) {
            $this->setWarning("学籍番号が入力されていません。");
            return false;
        }
        //存在チェック
        $db = Query::dbCheckOut();
        $schdata = $db->getOne(knjp720Query::getSchData($this, $schregno));
        Query::dbCheckIn($db);
        if (!strlen($schdata)) {
            $this->setWarning("該当する学籍番号はありません。");
            return false;
        } else {
            return true;
        }
    }

    //入力項目チェック
    function validateInput($cmd) {
        if ($cmd == "add") {
            //必須項目チェック
            if (in_array("", array($this->field["SCHREGNO"], $this->field["SCHOOL_KIND"], $this->field["REDUCTION_DIV_CD"], $this->field["S_YEAR_MONTH"], $this->field["E_YEAR_MONTH"]))) {
                $this->setWarning("MSG301");
                return false;
            }
        }

        if ($cmd == "update" || $cmd == "delete") {
            //リストより選択しているか
            if ($this->schregno == "" || $this->schoolkind == "" || $this->reduction_div_cd == "" || $this->s_year_month == "") {
                $this->setWarning("MSG308");
                return false;
            }
            //必須項目が変更されていないか
            if ($this->field["SCHREGNO"] != $this->schregno) {
                $this->setWarning("MSG308","( 学籍番号/氏名 )");
                return false;
            }
            if ($this->field["SCHOOL_KIND"] != $this->schoolkind) {
                $this->setWarning("MSG308","( 校種 )");
                return false;
            }
            if ($this->field["REDUCTION_DIV_CD"] != $this->reduction_div_cd) {
                $this->setWarning("MSG308","( 交付種別 )");
                return false;
            }
            if ($cmd == "delete") {
                if ($this->field["S_YEAR_MONTH"] != $this->s_year_month) {
                    $this->setWarning("MSG308","( 期間/開始年月 )");
                    return false;
                }
            }
        }

        if ($cmd == "update" || $cmd == "add") {
            list ($fld_sYM, $fld_eYM)  = str_replace("-", "", array($this->field["S_YEAR_MONTH"], $this->field["E_YEAR_MONTH"]));

            //必須項目チェック
            if ($fld_sYM == "" || $fld_eYM == "") {
                $this->setWarning("MSG301", "( 期間 )");
                return false;
            }

            //範囲チェック
            if ($fld_sYM > $fld_eYM) {
                $this->setWarning("MSG916", "( 期間の開始年月と終了年月の大小 )");
                return false;
            }
            $db = Query::dbCheckOut();
            $errflg  = false;
            $s_year_month = ($cmd == "update") ? $this->s_year_month : "";
            $div = ($cmd == "update") ? "1" : "0";
            $query = knjp720Query::getReductionSchoolStdDat($this, $this->field["SCHREGNO"], $s_year_month, $div);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                list ($data_sYM, $data_eYM)  = str_replace("-", "", array($row["S_YEAR_MONTH"], $row["E_YEAR_MONTH"]));
                if (!($fld_eYM < $data_sYM || $data_eYM < $fld_sYM)) {
                    $errflg  = true;
                }
            }
            $result->free();
            Query::dbCheckIn($db);
            if ($errflg) {
                $this->setWarning("MSG302", "( 期間 )");
                return false;
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput($this->cmd)) {
            if ($this->update()) {
                $this->setMessage("MSG201");
                $this->schregno     = $this->field["SCHREGNO"];
                $this->s_year_month = $this->field["S_YEAR_MONTH"];
                $this->flg          = "ON";
                $this->field = array();
                return;
            }
        }
    }
    function &update() {
        $result = knjp720Query::getUpdateQuery($this, $this->field);
        return $result;
    }

    //削除
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            if ($this->delete()) {
                $this->setMessage("MSG201");
                $this->field     = array();
                unset($this->schregno);
                unset($this->s_year_month);
                unset($this->flg);
                return;
            }
        }
    }
    function &delete() {
        $result = knjp720Query::getDeleteQuery($this, $this->field);
        return $result;
    }


/******** 一括更新処理  ********/
    //編集完了用のモデルを返します
    function &getSubInsertModel() {

        list ($fld_sYM, $fld_eYM)  = str_replace("-", "", array($this->field["S_YEAR_MONTH"], $this->field["E_YEAR_MONTH"]));
        $selectdata = (strlen($this->selectdata)) ? explode(",", $this->selectdata) : array();

        //必須項目チェック
        if (in_array("", array($this->field["SCHOOL_KIND"], $this->field["REDUCTION_DIV_CD"], $this->field["S_YEAR_MONTH"], $this->field["E_YEAR_MONTH"]))) {
            $this->setWarning("MSG301");
            return false;
        }
        if (!get_count($selectdata)) {
            $this->setWarning("MSG304", "( 対象者 )");
            return false;
        }

        //範囲チェック
        if ($fld_sYM > $fld_eYM) {
            $this->setWarning("MSG916", "( 期間の開始年月と終了年月の大小 )");
            return false;
        }

        $db = Query::dbCheckOut();
        $errflg  = false;
        $schData = $tmpData = array();
        $query = knjp720Query::getReduSchlStdDat($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schData[$row["SCHREGNO"]] = $row["HR_NAME"].''.$row["ATTENDNO"].'番　'.$row["NAME_SHOW"];
            $tmpData[$row["SCHREGNO"]][] = array("S_YM"     => str_replace("-", "", $row["S_YEAR_MONTH"]),
                                                 "E_YM"     => str_replace("-", "", $row["E_YEAR_MONTH"]));
        }
        $result->free();
        Query::dbCheckIn($db);

        //範囲チェック（生徒振り分け）
        $schArray = $schArrayErr = array();
        foreach ($selectdata as $schregno) {
            $errflg  = false;
            if (get_count($tmpData[$schregno])) {
                foreach ($tmpData[$schregno] as $val) {
                    if (!($fld_eYM < $val["S_YM"] || $val["E_YM"] < $fld_sYM)) {
                        $errflg  = true;
                    }
                }
            }

            if ($errflg) {
                //エラー対象生徒
                $schArrayErr[] = $schData[$schregno];
            } else {
                //更新対象生徒
                $schArray[] = $schregno;
            }
        }

        if (get_count($schArray)) {
            //一括更新処理
            knjp720Query::getIkkatsuInsertQuery($this, $this->field, $schArray);

            if (get_count($schArrayErr)) {
                $message = implode("\\n", $schArrayErr);
                $this->setMessage("MSG201", "以下の生徒は期間の重複のため対象外\\n".$message);
            } else {
                $this->setMessage("MSG201");
            }
        } else if (get_count($schArrayErr)) {
            $this->setWarning("MSG302", "( 期間/対象者全て )");
        }

        return;
    }

    //対象のフィールド西暦変換
    function getSeirekiHenkan($year_month) {
        if ($year_month) {
            $setYearMonth = array();
            $setYearMonth = preg_split("/\//", $year_month);
            $gengo = substr($setYearMonth[0], 0, 1);
            $warekiyear = substr($setYearMonth[0], 1, 2);
            $warekiList = common::getWarekiList();
            // 和暦から西暦
            foreach($warekiList as $w) {
                if ($gengo == $w["SName"]) {
                    $seireki = intval($warekiyear) + intval($w["YearStart"]) - 1;
                    break;
                }
            }
            $year_month = $seireki.'-'.$setYearMonth[1];
        }

        return $year_month;
    }
}
?>
