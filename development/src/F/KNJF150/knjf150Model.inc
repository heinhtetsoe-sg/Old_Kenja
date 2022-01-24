<?php

require_once('for_php7.php');

class knjf150Model extends Model
{
    public $cmd;
    public $schregno = "";
    public $name = "";
    public $grade;
    public $hr_class;
    public $attendno;
    public $field = array();

    public $visit_date;
    public $visit_hour;
    public $visit_minute;
    public $type;
    public $schoolName;

    public $control_data = array();    //コントロールマスタデータを保持

    public function knjf150Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
            unset($this->visit_date);
            unset($this->visit_hour);
            unset($this->visit_minute);
            unset($this->type);
            unset($this->sendProgramId);

            //学校を判断
            $db = Query::dbCheckOut();
            $this->schoolName = $db->getOne(knjf150Query::getSchoolName());
            Query::dbCheckIn($db);
        }

        $this->checked  = VARS::request("CHECKED");     //チェック

        $this->field = array("VISIT_DATE"           =>  str_replace("/", "-", VARS::post("VISIT_DATE")),    //来室日付
                             "VISIT_HOUR"           =>  (strlen(VARS::post("VISIT_HOUR"))) ? sprintf("%02d", VARS::post("VISIT_HOUR")) : "",        //来室時間（時）
                             "VISIT_MINUTE"         =>  (strlen(VARS::post("VISIT_MINUTE"))) ? sprintf("%02d", VARS::post("VISIT_MINUTE")) : "",    //来室時間（分）
                             "TYPE"                 =>  VARS::post("TYPE"),                 //種別区分
                             "VISIT_PERIODCD"       =>  VARS::post("VISIT_PERIODCD"),       //来室校時
                             "CONDITION1"           =>  VARS::post("CONDITION1"),           //体調１
                             "SLEEPTIME"            =>  VARS::post("SLEEPTIME"),            //睡眠時間
                             "CONDITION3"           =>  VARS::post("CONDITION3"),           //体調３
                             "CONDITION4"           =>  VARS::post("CONDITION4"),           //体調４
                             "MEAL"                 =>  VARS::post("MEAL"),                 //食事の内容
                             "CONDITION5"           =>  VARS::post("CONDITION5"),           //体調５
                             "CONDITION6"           =>  VARS::post("CONDITION6"),           //体調６
                             "VISIT_REASON1"        =>  VARS::post("VISIT_REASON1"),        //来室理由１
                             "VISIT_REASON1_TEXT"   =>  VARS::post("VISIT_REASON1_TEXT"),   //来室理由１備考
                             "VISIT_REASON2"        =>  VARS::post("VISIT_REASON2"),        //来室理由２
                             "VISIT_REASON2_TEXT"   =>  VARS::post("VISIT_REASON2_TEXT"),   //来室理由２備考
                             "VISIT_REASON3"        =>  VARS::post("VISIT_REASON3"),        //来室理由３
                             "VISIT_REASON3_TEXT"   =>  VARS::post("VISIT_REASON3_TEXT"),   //来室理由３備考
                             "OCCUR_SITUATION"      =>  VARS::post("OCCUR_SITUATION"),      //発生状況
                             "OCCUR_CAUSE"          =>  VARS::post("OCCUR_CAUSE"),          //原因
                             "OCCUR_CAUSE_TEXT"     =>  VARS::post("OCCUR_CAUSE_TEXT"),     //原因(テキスト)
                             "OCCUR_DATE"           =>  str_replace("/", "-", VARS::post("OCCUR_DATE")),    //発生日付
                             "OCCUR_HOUR"           =>  VARS::post("OCCUR_HOUR"),           //発生時間（時）
                             "OCCUR_MINUTE"         =>  VARS::post("OCCUR_MINUTE"),         //発生時間（分）
                             "OCCUR_ACT"            =>  VARS::post("OCCUR_ACT"),            //発生時の行動
                             "OCCUR_ACT_DETAIL"     =>  VARS::post("OCCUR_ACT_DETAIL"),     //発生時の行動（詳細）
                             "OCCUR_PLACE"          =>  VARS::post("OCCUR_PLACE"),          //発生場所
                             "OCCUR_PLACE_TEXT2"    =>  VARS::post("OCCUR_PLACE_TEXT2"),    //発生場所(テキスト)
                             "INJURY_PART1"         =>  VARS::post("INJURY_PART1"),         //けがの場所１
                             "INJURY_PART1_TEXT"    =>  VARS::post("INJURY_PART1_TEXT"),    //けがの場所１備考
                             "INJURY_PART2"         =>  VARS::post("INJURY_PART2"),         //けがの場所２
                             "INJURY_PART2_TEXT"    =>  VARS::post("INJURY_PART2_TEXT"),    //けがの場所２備考
                             "INJURY_PART3"         =>  VARS::post("INJURY_PART3"),         //けがの場所３
                             "INJURY_PART3_TEXT"    =>  VARS::post("INJURY_PART3_TEXT"),    //けがの場所３備考
                             "TREATMENT1"           =>  VARS::post("TREATMENT1"),           //処置１
                             "TREATMENT1_TEXT"      =>  VARS::post("TREATMENT1_TEXT"),      //処置１備考
                             "TREATMENT2"           =>  VARS::post("TREATMENT2"),           //処置２
                             "TREATMENT2_TEXT"      =>  VARS::post("TREATMENT2_TEXT"),      //処置２備考
                             "TREATMENT3"           =>  VARS::post("TREATMENT3"),           //処置３
                             "TREATMENT3_TEXT"      =>  VARS::post("TREATMENT3_TEXT"),      //処置３備考
                             "TEMPERATURE1"         =>  VARS::post("TEMPERATURE1"),         //体温１
                             "MEASURE_HOUR1"        =>  VARS::post("MEASURE_HOUR1"),        //測定時間（時）
                             "MEASURE_MINUTE1"      =>  VARS::post("MEASURE_MINUTE1"),      //測定時間（分）
                             "TEMPERATURE2"         =>  VARS::post("TEMPERATURE2"),         //体温２
                             "MEASURE_HOUR2"        =>  VARS::post("MEASURE_HOUR2"),        //測定時間（時）
                             "MEASURE_MINUTE2"      =>  VARS::post("MEASURE_MINUTE2"),      //測定時間（分）
                             "TEMPERATURE3"         =>  VARS::post("TEMPERATURE3"),         //体温３
                             "MEASURE_HOUR3"        =>  VARS::post("MEASURE_HOUR3"),        //測定時間（時）
                             "MEASURE_MINUTE3"      =>  VARS::post("MEASURE_MINUTE3"),      //測定時間（分）
                             "RESTTIME"             =>  VARS::post("RESTTIME"),             //休養時間
                             "RESTTIME_TEXT"        =>  VARS::post("RESTTIME_TEXT"),        //休養時間(テキスト)
                             "LEAVE_HOUR"           =>  VARS::post("LEAVE_HOUR"),           //退出時間（時）
                             "LEAVE_MINUTE"         =>  VARS::post("LEAVE_MINUTE"),         //退出時間（分）
                             "LEAVE_PERIODCD"       =>  VARS::post("LEAVE_PERIODCD"),       //退出校時
                             "RESULT_REST"          =>  VARS::post("RESULT_REST"),          //処置結果（休養）
                             "RESULT_EARLY"         =>  VARS::post("RESULT_EARLY"),         //処置結果（早退）
                             "RESULT_MEDICAL"       =>  VARS::post("RESULT_MEDICAL"),       //処置結果（医療機関）
                             "RESULT_RETCLS"        =>  VARS::post("RESULT_RETCLS"),        //処置結果（教室へ戻る）
                             "CONTACT"              =>  VARS::post("CONTACT"),              //連絡
                             "CONTACT2"             =>  VARS::post("CONTACT2"),             //連絡
                             "CONTACT3"             =>  VARS::post("CONTACT3"),             //連絡
                             "HOSPITAL"             =>  VARS::post("HOSPITAL"),             //病院名
                             "COMPANION"            =>  VARS::post("COMPANION"),            //同伴者
                             "COMPANION_DIV"        =>  VARS::post("COMPANION_DIV"),        //同伴者区分
                             "DIAGNOSIS"            =>  VARS::post("DIAGNOSIS"),            //診断名
                             "SPECIAL_NOTE"         =>  VARS::post("SPECIAL_NOTE"),         //特記事項
                             "CONSULTATION_NAME"    =>  VARS::post("CONSULTATION_NAME"),    //相談者名
                             "RELATIONSHIP"         =>  VARS::post("RELATIONSHIP"),         //生徒との関係
                             "RELATIONSHIP_TEXT"    =>  VARS::post("RELATIONSHIP_TEXT"),    //生徒との関係(テキスト)
                             "CONSULTATION_METHOD"  =>  VARS::post("CONSULTATION_METHOD"),  //相談方法
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

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
        if (VARS::get("ATTENDNO") != "") {
            $this->attendno = VARS::get("ATTENDNO");
        }
        if (VARS::get("VISIT_DATE") != "") {
            $this->visit_date = VARS::get("VISIT_DATE");
        }
        if (VARS::get("VISIT_HOUR") != "") {
            $this->visit_hour = VARS::get("VISIT_HOUR");
        }
        if (VARS::get("VISIT_MINUTE") != "") {
            $this->visit_minute = VARS::get("VISIT_MINUTE");
        }
        if (VARS::get("TYPE") != "") {
            $this->type = VARS::get("TYPE");
        }
        if (VARS::get("SEND_PROGRAMID") != "") {
            $this->sendProgramId = VARS::get("SEND_PROGRAMID");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
            unset($this->visit_date);
            unset($this->visit_hour);
            unset($this->visit_minute);
            unset($this->type);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //生徒項目名切替処理
        $schName = "";
        $db = Query::dbCheckOut();
        //テーブルの有無チェック
        $query = knjf150Query::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && ($this->schregno || $this->Properties["use_prg_schoolkind"] == "1")) {
            //生徒項目名取得
            $schName = $db->getOne(knjf150Query::getSchName($this));
        } elseif ($table_cnt > 0 && ($this->schregno || ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""))) {
            //生徒項目名取得
            $schName = $db->getOne(knjf150Query::getSchName($this));
        }
        $this->sch_label = (strlen($schName) > 0) ? $schName : '生徒';
        Query::dbCheckIn($db);
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return;
        }

        knjf150Query::getDeleteQuery($this);
        $this->setMessage("MSG201");
        $this->field = array();

        return;
    }

    //入力項目チェック
    public function validateInput($flg)
    {
        $count = get_count(knjf150Query::getExistCheck($this));

        if (($flg == "update") && (($this->visit_date != $this->field["VISIT_DATE"]) ||
                                  ($this->visit_hour != $this->field["VISIT_HOUR"]) ||
                                  ($this->visit_minute != $this->field["VISIT_MINUTE"]))) {
            $this->setWarning("来室日時が変更されています。");
            return false;
        }
        if (($flg == "update") && !$count) {
            $this->setWarning("対象データがありません。");
            return false;
        }
        if (($flg == "insert") && $count) {
            $this->setWarning("MSG302");
            return false;
        }

        //桁数チェック
        if (strlen($this->field["OCCUR_SITUATION"]) > 90) {
            $this->setWarning("MSG915", "(発生状況は90バイトまでです)");
            return false;
        }
        if (strlen($this->field["MEAL"]) > 30) {
            $this->setWarning("MSG915", "(食事内容は30バイトまでです)");
            return false;
        }
        if (strlen($this->field["CONDITION6"]) > 60) {
            $this->setWarning("MSG915", "(体調５は60バイトまでです)");
            return false;
        }
        if (strlen($this->field["TREATMENT1_TEXT"]) > 90) {
            $this->setWarning("MSG915", "(処置１は90バイトまでです)");
            return false;
        }
        if (strlen($this->field["TREATMENT2_TEXT"]) > 90) {
            $this->setWarning("MSG915", "(処置２は90バイトまでです)");
            return false;
        }
        if (strlen($this->field["TREATMENT3_TEXT"]) > 90) {
            $this->setWarning("MSG915", "(処置３は90バイトまでです)");
            return false;
        }
        if (strlen($this->field["HOSPITAL"]) > 30) {
            $this->setWarning("MSG915", "(病院名は30バイトまでです)");
            return false;
        }
        if (strlen($this->field["COMPANION"]) > 30) {
            $this->setWarning("MSG915", "(同伴者は30バイトまでです)");
            return false;
        }
        if (strlen($this->field["DIAGNOSIS"]) > 30) {
            $this->setWarning("MSG915", "(診断名は30バイトまでです)");
            return false;
        }
        if ($this->schoolName == "fukuiken") {
            $specialNoteLen = 300;
        } else {
            $specialNoteLen = 150;
        }
        if (strlen($this->field["SPECIAL_NOTE"]) > $specialNoteLen) {
            $this->setWarning("MSG915", "(特記事項は".$specialNoteLen."バイトまでです)");
            return false;
        }
        if (strlen($this->field["CONSULTATION_NAME"]) > 30) {
            $this->setWarning("MSG915", "(相談者名は30バイトまでです)");
            return false;
        }

        return true;
    }

    //更新用のモデルを返します
    public function &getUpdateModel($flg)
    {
        $this->cmd = ($flg == "insert") ? "subform".$this->type : "subform".$this->type."A";

        if (!$this->validateInput($flg)) {
            return;
        }

        knjf150Query::getUpdateQuery($this, $this->field, $flg);
        $this->setMessage("MSG201");

        $this->visit_date = $this->field["VISIT_DATE"];
        $this->visit_hour = $this->field["VISIT_HOUR"];
        $this->visit_minute = $this->field["VISIT_MINUTE"];

        return;
    }
}
