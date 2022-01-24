<?php

require_once('for_php7.php');

//定数定義
class knjf170cModel extends Model
{
    public $field = array();
    public $cmd;
    public $year = "";
    public $date = "";
    public $sort;//昇順,降順
    public $di_cd;
    public $grade;
    public $att_field = array();

    //メンバ変数の初期化
    public function knjf170cModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->schoolkind);
            unset($this->date);
            unset($this->sendSchoolKind);
            //ソート処理の初期化
            $this->sort["SRT_D"] = 1;
        }

        if (VARS::post("YEAR")) {
            $this->year = VARS::post("YEAR");
        }

        //特別支援学校では校種コンボを表示せず、メニュー校種を利用する。
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $this->schoolkind = SCHOOLKIND;

            $this->field = array("YEAR"              => VARS::post("YEAR"),
                                 "SCHKIND"           => SCHOOLKIND,
                                 "DATE"              => VARS::post("DATE"),
                                 "WEATHER"           => VARS::post("WEATHER"),
                                 "WEATHER_TEXT"      => VARS::post("WEATHER_TEXT"),
                                 "TEMPERATURE"       => VARS::post("TEMPERATURE"),
                                 "EVENT"             => VARS::post("EVENT"),
                                 "DIARY"             => VARS::post("DIARY"),
                                 "UPDATED"           => VARS::post("UPDATED"),
                                 "HUMIDITY"          => VARS::post("HUMIDITY"),
                                 "CHECK_HOUR"        => VARS::post("CHECK_HOUR"),
                                 "CHECK_MINUTE"      => VARS::post("CHECK_MINUTE"),
                                 "COLOR"             => VARS::post("COLOR"),
                                 "TURBIDITY"         => VARS::post("TURBIDITY"),
                                 "SMELL"             => VARS::post("SMELL"),
                                 "TASTE"             => VARS::post("TASTE"),
                                 "RESIDUAL_CHLORINE" => VARS::post("RESIDUAL_CHLORINE"),
                                 "WATER_REMARK"      => VARS::post("WATER_REMARK"),
                                 "AED"               => VARS::post("AED")
            );
        } else {
            if (VARS::post("SCHKIND")) {
                $this->schoolkind = VARS::post("SCHKIND");
            }

            $this->field = array("YEAR"              => VARS::post("YEAR"),
                                 "SCHKIND"           => VARS::post("SCHKIND"),
                                 "DATE"              => VARS::post("DATE"),
                                 "WEATHER"           => VARS::post("WEATHER"),
                                 "WEATHER_TEXT"      => VARS::post("WEATHER_TEXT"),
                                 "TEMPERATURE"       => VARS::post("TEMPERATURE"),
                                 "EVENT"             => VARS::post("EVENT"),
                                 "DIARY"             => VARS::post("DIARY"),
                                 "UPDATED"           => VARS::post("UPDATED"),
                                 "HUMIDITY"          => VARS::post("HUMIDITY"),
                                 "CHECK_HOUR"        => VARS::post("CHECK_HOUR"),
                                 "CHECK_MINUTE"      => VARS::post("CHECK_MINUTE"),
                                 "COLOR"             => VARS::post("COLOR"),
                                 "TURBIDITY"         => VARS::post("TURBIDITY"),
                                 "SMELL"             => VARS::post("SMELL"),
                                 "TASTE"             => VARS::post("TASTE"),
                                 "RESIDUAL_CHLORINE" => VARS::post("RESIDUAL_CHLORINE"),
                                 "WATER_REMARK"      => VARS::post("WATER_REMARK"),
                                 "AED"               => VARS::post("AED")
            );
        }

        if (VARS::request("YEAR")) {
            $this->year = VARS::request("YEAR");
        }

        if (VARS::get("DATE")) {
            $this->date = VARS::get("DATE");
        }

        $this->cmd = VARS::request("cmd");

        $db = Query::dbCheckOut();

        //校種初期値
        $this->firstKind = $db->getOne(knjf170cQuery::getSchkind($this));

        //名称マスタ取得(Z010)
        $this->z010 = $db->getOne(knjf170cQuery::getZ010());

        Query::dbCheckIn($db);

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
            if (VARS::request("SENDSCHOOLKIND")) {
                $this->sendSchoolKind = VARS::request("SENDSCHOOLKIND");
            }
            $this->sendSchoolKind = ($this->sendSchoolKind) ? $this->sendSchoolKind : $this->firstKind;
        } elseif ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $this->sendSchoolKind = SCHOOLKIND;
        }

        //出欠コード
        $this->di_cd = array('4' => '病　欠',
                             '5' => '事故欠',
                             '6' => '無　届',
                             '2' => '出　停',
                             '3' => '忌　引');

        //学年
        $this->grade = array();
        $db = Query::dbCheckOut();
        $year = ($this->year) ? $this->year : CTRL_YEAR;
        $schoolkind = ($this->sendSchoolKind) ? $this->sendSchoolKind : $this->firstKind;
        $query = knjf170cQuery::getGrade($this, $year, $schoolkind);
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
        //特記事項（文字数・行数）
        if ($this->Properties["nurseoff_tokkizikoSize"]) {
            list($moji, $gyou) = explode(' * ', $this->Properties["nurseoff_tokkizikoSize"]);
            $this->nurseoff_tokkiziko_moji = (int)trim($moji);
            $this->nurseoff_tokkiziko_gyou = (int)trim($gyou);
        }
        //行事（文字数・行数）
        if ($this->Properties["nurseoff_gyouziSize"]) {
            list($moji, $gyou) = explode(' * ', $this->Properties["nurseoff_gyouziSize"]);
            $this->nurseoff_gyouzi_moji = (int)trim($moji);
            $this->nurseoff_gyouzi_gyou = (int)trim($gyou);
        }
        //日誌（文字数・行数）
        if ($this->Properties["nurseoff_diarySize"]) {
            list($moji, $gyou) = explode(' * ', $this->Properties["nurseoff_diarySize"]);
            $this->nurseoff_diary_moji = (int)trim($moji);
            $this->nurseoff_diary_gyou = (int)trim($gyou);
        }

        //ソートが押下
        if (trim(VARS::request("sort")) != "") {
            if (VARS::request("sort") == "SRT_D" && $this->sort[VARS::get("sort")] != 0) {
                //昇順⇔降順
                $this->sort["SRT_D"] = (int)$this->sort["SRT_D"] *(-1);
            }
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        $db = Query::dbCheckOut();

        $date = str_replace("/", "-", $this->field["DATE"]);
        $row = $db->getRow(knjf170cQuery::getRow($this, $date), DB_FETCHMODE_ASSOC);

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
                $this->setWarning("MSG301", "(日付)");
                return false;
            }
            //重複チェック
            if ($this->cmd == "add" && is_array($row)) {
                $this->setWarning("MSG918");
                $this->date = $this->field["DATE"];
                return false;
            }
            if (strlen($this->field["WEATHER_TEXT"]) > 30) {
                $this->setWarning("MSG915", "(天気は30バイトまでです)");
                return false;
            }
            if ($this->validate_row_cnt($this->field["WATER_REMARK"], $this->nurseoff_tokkiziko_moji * 2) > $this->nurseoff_tokkiziko_gyou) {
                $this->setWarning("MSG914", "本文は全角{$this->nurseoff_tokkiziko_moji}文字までです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["EVENT"], $this->nurseoff_gyouzi_moji * 2) > $this->nurseoff_gyouzi_gyou) {
                $this->setWarning("MSG914", "本文は全角{$this->nurseoff_gyouzi_moji}文字{$this->nurseoff_gyouzi_gyou}行までです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["DIARY"], $this->nurseoff_diary_moji * 2) > $this->nurseoff_diary_gyou) {
                $this->setWarning("MSG914", "本文は全角{$this->nurseoff_diary_moji}文字{$this->nurseoff_diary_gyou}行までです。");
                return false;
            }
        }
        Query::dbCheckIn($db);
        return true;
    }

    //編集完了用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
        }
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->delete();
        }
        return;
    }

    //DELETE時の内部メソッド
    public function &delete()
    {
        knjf170cQuery::getDeleteQuery($this);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
            $this->date = "";
            $this->schoolkind = $this->sendSchoolKind;
        }
    }

    //INSERT・UPDATE時の内部メソッド
    public function &insert()
    {
        knjf170cQuery::getInsertQuery($this);
        if (!isset($model->warning)) {
            $this->date = $this->field["DATE"];
            $this->schoolkind = $this->sendSchoolKind;
            $this->setMessage("MSG201");
        }
        return $result;
    }
}
