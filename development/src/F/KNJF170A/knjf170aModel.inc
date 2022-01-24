<?php

require_once('for_php7.php');

class knjf170aModel extends Model {
    var $field = array();
    var $cmd;
    var $year = "";
    var $campus_div = "";
    var $date = "";
    var $di_cd;
    var $grade;
    var $att_field = array();

    function knjf170aModel() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->campus_div);
            unset($this->date);
            unset($this->schoolkind);
            unset($this->sendSchoolKind);
            //ソート処理の初期化
            $this->sort["SRT_D"] = 1;
        }

        if (VARS::post("YEAR")) {
            $this->year = VARS::post("YEAR");
        }
        if (VARS::post("CAMPUS_DIV")) {
            $this->campus_div = VARS::post("CAMPUS_DIV");
        }
        if (VARS::post("SCHKIND")) {
            $this->schoolkind = VARS::post("SCHKIND");
        }

        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "CAMPUS_DIV"   =>  VARS::post("CAMPUS_DIV"),
                             "DATE"         =>  VARS::post("DATE"),
                             "WEATHER"      =>  VARS::post("WEATHER"),
                             "WEATHER_TEXT" =>  VARS::post("WEATHER_TEXT"),
                             "TEMPERATURE"  =>  VARS::post("TEMPERATURE"),
                             "EVENT"        =>  VARS::post("EVENT"),
                             "DIARY"        =>  VARS::post("DIARY"),
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        if (VARS::request("YEAR")) {
            $this->year = VARS::request("YEAR");
        }
        if (VARS::request("CAMPUS_DIV")) {
            $this->campus_div = VARS::request("CAMPUS_DIV");
        }

        if (VARS::get("DATE")) {
            $this->date = VARS::get("DATE");
        }

        $this->cmd = VARS::request("cmd");

        //出欠コード
        $this->di_cd = array('4' => '病　欠',
                             '5' => '事故欠',
                             '6' => '無　届',
                             '2' => '出　停',
                             '3' => '忌　引');

        //校種取得
        $db = Query::dbCheckOut();
        $this->firstSchkind = $db->getOne(knjf170aQuery::getCampusDiv2($this, CTRL_YEAR));
        Query::dbCheckIn($db);
        $this->schoolkind = ($this->schoolkind) ? $this->schoolkind: $this->firstSchkind;

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
            if (VARS::request("SENDSCHOOLKIND")) {
                $this->sendSchoolKind = VARS::request("SENDSCHOOLKIND");
            }
            $this->sendSchoolKind = ($this->sendSchoolKind) ? $this->sendSchoolKind : $this->firstSchkind;
        } elseif ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $this->sendSchoolKind = SCHOOLKIND;
        }

        //学年
        $this->grade = array();
        $db = Query::dbCheckOut();
        $year = ($this->year) ? $this->year : CTRL_YEAR;
        $campus_div = ($this->campus_div) ? $this->campus_div : $db->getOne(knjf170aQuery::getCampusDiv($this, $year));
        $schoolkind = ($this->sendSchoolKind) ? $this->sendSchoolKind : $this->firstSchkind;
        $query = knjf170aQuery::getGrade($this, $year, $campus_div, $schoolkind);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->grade[$row["VALUE"]] = $row["LABEL"];
        }
        $result->free();

        Query::dbCheckIn($db);

        //欠席状況
        foreach ($this->di_cd as $dkey => $dval) {
            foreach ($this->grade as $gkey => $gval) {
                $this->att_field[$dkey][$gkey] =  VARS::post("CNT_".$dkey."_".$gkey);
            }
        }

        //日誌（文字数・行数）
        if ($this->Properties["nurseoff_diarySize"]) {
            list($moji, $gyou) = explode(' * ', $this->Properties["nurseoff_diarySize"]);
            $this->nurseoff_diary_moji = (int)trim($moji);
            $this->nurseoff_diary_gyou = (int)trim($gyou);
        } else {
            $this->nurseoff_diary_moji = 43;  //デフォルトの値
            $this->nurseoff_diary_gyou = 10;  //デフォルトの値
        }

        //ソートが押下
        if (trim(VARS::request("sort")) != "") {
            if (VARS::request("sort") == "SRT_D" && $this->sort[VARS::get("sort")] != 0) {
                //昇順⇔降順
                $this->sort["SRT_D"] = $this->sort["SRT_D"] *(-1);
            }
        }
    }

    //入力項目チェック
    function validateInput() {
        $db = Query::dbCheckOut();

        $date = str_replace("/", "-", $this->field["DATE"]);
        $row = $db->getRow(knjf170aQuery::getRow($this, $date), DB_FETCHMODE_ASSOC);

        //必須項目チェック
        if ($this->campus_div == "") {
            $this->setWarning("MSG310");
            return false;
        }

        if ($this->cmd == "update" || $this->cmd == "delete") {
            //リストより選択していない
            if ($this->date == "") {
                $this->setWarning("MSG308");
                return false;
            }
            //キー変更をしての削除
            if ($this->date != $this->field["DATE"]) {
               $this->setWarning("MSG308");
               return false;
            }
            //存在チェック
            if (!is_array($row)) {
                $this->setWarning("MSG303");
                $this->date = $this->field["DATE"];
                return false;
            }
        }

        if ($this->cmd == "update" || $this->cmd == "add") {
            //必須項目チェック
            if ($this->field["DATE"] == "") {
                $this->setWarning("MSG301","(日付)");
                return false;
            }

            //重複チェック
            if ($this->cmd == "add" && is_array($row)) {
                $this->setWarning("MSG918");
                $this->date = $this->field["DATE"];
                return false;
            }

            if (strlen($this->field["WEATHER_TEXT"]) > 30) {
                $this->setWarning("MSG915","(天気は30バイトまでです)");
                return false;
            }

            if (strlen($this->field["EVENT"]) > 226 ) {
                $this->setWarning("MSG915","(行事は226バイトまでです)");
                return false;
            }

            if (strlen($this->field["DIARY"]) > 2000) {
                $this->setWarning("MSG915","(日誌は2000バイトまでです)");
                return false;
            }
        }
        Query::dbCheckIn($db);
        return true;
    }

    //編集完了用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
        }

        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->delete();
        }
        return;
    }

    //DELETE時の内部メソッド
    function &delete() {
        knjf170aQuery::getDeleteQuery($this);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
            $this->date = "";
            $this->schoolkind = $this->sendSchoolKind;
        }
    }

    //INSERT・UPDATE時の内部メソッド
    function &insert() {
        knjf170aQuery::getInsertQuery($this);
        if (!isset($this->warning)) {
            $this->date = $this->field["DATE"];
            $this->schoolkind = $this->sendSchoolKind;
            $this->setMessage("MSG201");
        }
        return $result;
    }
}
?>
