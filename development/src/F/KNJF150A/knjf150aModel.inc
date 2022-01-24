<?php

require_once('for_php7.php');

class knjf150aModel extends Model
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

    public $control_data = array();    //コントロールマスタデータを保持

    public function knjf150aModel()
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
        }

        $this->checked  = VARS::request("CHECKED");     //チェック

        $this->field = array("VISIT_DATE"           =>  str_replace("/", "-", VARS::post("VISIT_DATE")),    //来室日付
                             "VISIT_HOUR"           =>  (strlen(VARS::post("VISIT_HOUR"))) ? sprintf("%02d", VARS::post("VISIT_HOUR")) : "",        //来室時間（時）
                             "VISIT_MINUTE"         =>  (strlen(VARS::post("VISIT_MINUTE"))) ? sprintf("%02d", VARS::post("VISIT_MINUTE")) : "",    //来室時間（分）
                             "TYPE"                 =>  VARS::post("TYPE"),                 //種別区分
                             "LESSON_CLASS"         =>  VARS::post("LESSON_CLASS"),         //現在の教科
                             "VISIT_PERIODCD"       =>  VARS::post("VISIT_PERIODCD"),       //来室校時
                             "VISIT_REASON1"        =>  VARS::post("VISIT_REASON1"),        //来室理由１
                             "VISIT_REASON1_TEXT"   =>  VARS::post("VISIT_REASON1_TEXT"),   //来室理由１（テキスト）
                             "VISIT_REASON2"        =>  VARS::post("VISIT_REASON2"),        //来室理由２
                             "VISIT_REASON2_TEXT"   =>  VARS::post("VISIT_REASON2_TEXT"),   //来室理由２（テキスト）
                             "VISIT_REASON3"        =>  VARS::post("VISIT_REASON3"),        //来室理由３
                             "VISIT_REASON3_TEXT"   =>  VARS::post("VISIT_REASON3_TEXT"),   //来室理由３（テキスト）
                             "SINCE_WHEN"           =>  VARS::post("SINCE_WHEN"),           //いつから
                             "SINCE_WHEN_TEXT"      =>  VARS::post("SINCE_WHEN_TEXT"),      //いつから（テキスト）
                             "CONDITION1"           =>  VARS::post("CONDITION1"),           //体調
                             "SLEEPTIME"            =>  VARS::post("SLEEPTIME"),            //睡眠時間（時）
                             "SLEEPTIME_M"          =>  VARS::post("SLEEPTIME_M"),          //睡眠時間（分）
                             "BEDTIME_H"            =>  VARS::post("BEDTIME_H"),            //就寝時間（時）
                             "BEDTIME_M"            =>  VARS::post("BEDTIME_M"),            //就寝時間（分）
                             "CONDITION3"           =>  VARS::post("CONDITION3"),           //体調２
                             "CONDITION4"           =>  VARS::post("CONDITION4"),           //体調３
                             "CONDITION7"           =>  VARS::post("CONDITION7"),           //体調４
                             "CONDITION7_TEXT"      =>  VARS::post("CONDITION7_TEXT"),      //体調４（テキスト）
                             "CONDITION8"           =>  VARS::post("CONDITION8"),           //体調５
                             "CONDITION8_TEXT"      =>  VARS::post("CONDITION8_TEXT"),      //体調５（テキスト）
                             "CONDITION9"           =>  VARS::post("CONDITION9"),           //体調６
                             "CONDITION10"          =>  VARS::post("CONDITION10"),          //体調７
                             "CONDITION11"          =>  VARS::post("CONDITION11"),          //体調８
                             "CONDITION12"          =>  VARS::post("CONDITION12"),          //体調８_１
                             "CONDITION12_TEXT"     =>  VARS::post("CONDITION12_TEXT"),     //体調８_１（テキスト）
                             "TEMPERATURE1"         =>  VARS::post("TEMPERATURE1"),         //体温
                             "MEASURE_HOUR1"        =>  VARS::post("MEASURE_HOUR1"),        //測定時間（時）
                             "MEASURE_MINUTE1"      =>  VARS::post("MEASURE_MINUTE1"),      //測定時間（分）
                             "PULSE"                =>  VARS::post("PULSE"),                //脈拍
                             "BLOOD_PRESSURE_L"     =>  VARS::post("BLOOD_PRESSURE_L"),     //血圧（下）
                             "BLOOD_PRESSURE_H"     =>  VARS::post("BLOOD_PRESSURE_H"),     //血圧（上）
                             "TREATMENT1"           =>  VARS::post("TREATMENT1"),           //処置１
                             "TREATMENT1_TEXT"      =>  VARS::post("TREATMENT1_TEXT"),      //処置１（テキスト）
                             "TREATMENT2"           =>  VARS::post("TREATMENT2"),           //処置２
                             "TREATMENT2_TEXT"      =>  VARS::post("TREATMENT2_TEXT"),      //処置２（テキスト）
                             "TREATMENT3"           =>  VARS::post("TREATMENT3"),           //処置３
                             "TREATMENT3_TEXT"      =>  VARS::post("TREATMENT3_TEXT"),      //処置３（テキスト）
                             "RESTTIME"             =>  VARS::post("RESTTIME"),             //在室時間
                             "CONTACT"              =>  VARS::post("CONTACT"),              //連絡
                             "CONTACT_TEXT"         =>  VARS::post("CONTACT_TEXT"),         //連絡（テキスト）
                             "SPECIAL_NOTE"         =>  VARS::post("SPECIAL_NOTE"),         //特記事項
                             "INJURY_PART1"         =>  VARS::post("INJURY_PART1"),         //けがの部位１
                             "INJURY_PART1_TEXT"    =>  VARS::post("INJURY_PART1_TEXT"),    //けがの部位１（テキスト）
                             "INJURY_PART2"         =>  VARS::post("INJURY_PART2"),         //けがの部位２
                             "INJURY_PART2_TEXT"    =>  VARS::post("INJURY_PART2_TEXT"),    //けがの部位２（テキスト）
                             "INJURY_PART3"         =>  VARS::post("INJURY_PART3"),         //けがの部位３
                             "INJURY_PART3_TEXT"    =>  VARS::post("INJURY_PART3_TEXT"),    //けがの部位３（テキスト）
                             "OCCUR_DATE"           =>  str_replace("/", "-", VARS::post("OCCUR_DATE")),    //発生時の場合（日付）
                             "OCCUR_HOUR"           =>  VARS::post("OCCUR_HOUR"),           //発生時の場合（時）
                             "OCCUR_MINUTE"         =>  VARS::post("OCCUR_MINUTE"),         //発生時の場合（分）
                             "OCCUR_ACT"            =>  VARS::post("OCCUR_ACT"),            //発生時の場合
                             "OCCUR_SITUATION"      =>  VARS::post("OCCUR_SITUATION"),      //発生時の場合（テキスト）
                             "OCCUR_PLACE"          =>  VARS::post("OCCUR_PLACE"),          //発生時の場所
                             "OCCUR_PLACE_TEXT"     =>  VARS::post("OCCUR_PLACE_TEXT"),     //発生時の場所（テキスト）
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        //selectDetailSeq (NURSEOFF_VISITREC_DETAIL_DATのSEQごとの設定用配列)
        // count:チェックボックス数
        // fieldName:NURSEOFF_VISITREC_DETAIL_DATのフィールド名
        // fieldNameMain:NURSEOFF_VISITREC_DATのフィールド名
        // digit:桁数
        // types:1=内科,2=外科
        $this->selectDetailSeq = array();

        //来室校時(SEQ=11)
        $this->selectDetailSeq["11"] = array(
            "count" => 15,
            "fieldName" => "SEQ11_REMARK1",
            "fieldNameMain" => "VISIT_PERIODCD",
            "digit" => "2",
            "types" => array(1, 2)
        );
        //体調 ４．具合が悪くなった原因は？(SEQ=12)
        $this->selectDetailSeq["12"] = array(
            "count" => 8,
            "fieldName" => "SEQ12_REMARK1",
            "fieldNameMain" => "CONDITION7",
            "digit" => "1",
            "types" => array(1)
        );
        //体調 ５．今、どうしたいか？(SEQ=13)
        $this->selectDetailSeq["13"] = array(
            "count" => 7,
            "fieldName" => "SEQ13_REMARK1",
            "fieldNameMain" => "CONDITION8",
            "digit" => "1",
            "types" => array(1)
        );
        //体調 ８．気になることや悩みごとがあるか？ 「ある」と答えた人(SEQ=14)
        $this->selectDetailSeq["14"] = array(
            "count" => 10,
            "fieldName" => "SEQ14_REMARK1",
            "fieldNameMain" => "CONDITION12",
            "digit" => "1",
            "types" => array(1)
        );
        //連絡(SEQ=15)
        $this->selectDetailSeq["15"] = array(
            "count" => 9,
            "fieldName" => "SEQ15_REMARK1",
            "fieldNameMain" => "CONTACT",
            "digit" => "2",
            "types" => array(1, 2)
        );

        foreach ($this->selectDetailSeq as $seq => $val) {
            $comma = "";
            $cnt = 0;
            //NURSEOFF_VISITREC_DETAIL_DATのフィールドへカンマ区切り文字列をセット
            $this->field[$val["fieldName"]] = "";
            for ($idx = 1; $idx <= $val["count"]; $idx++) {
                $num = sprintf("%02d", $idx);
                $getPostName = $val["fieldName"]."_".$num;
                $this->field[$getPostName] = VARS::post($getPostName);
                if (VARS::post($getPostName) == "1") {
                    $this->field[$val["fieldName"]] .= $comma.$num;
                    $comma = ",";
                    $cnt++;

                    //NURSEOFF_VISITREC_DATのフィールドへカンマ区切り文字列の先頭の値をセット
                    if ($cnt == 1) {
                        if ($val["digit"] == "1") {
                            //1桁をセット
                            $this->field[$val["fieldNameMain"]] = (int)$num;
                        } else {
                            //ゼロ埋め2桁をセット
                            $this->field[$val["fieldNameMain"]] = $num;
                        }
                    }
                }
            }
        }

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
        $this->cmd_keep = VARS::post("cmd_keep");

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
        $query = knjf150aQuery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && ($this->schregno || ($this->Properties["use_prg_schoolkind"] == "1"))) {
            //生徒項目名取得
            $schName = $db->getOne(knjf150aQuery::getSchName($this));
        } elseif ($table_cnt > 0 && ($this->schregno || ($this->Properties["useSchool_KindField"] == "1"))) {
            //生徒項目名取得
            $schName = $db->getOne(knjf150aQuery::getSchName($this));
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

        knjf150aQuery::getDeleteQuery($this);
        $this->setMessage("MSG201");
        $this->field = array();

        return;
    }

    //入力項目チェック
    public function validateInput($flg)
    {
        $count = get_count(knjf150aQuery::getExistCheck($this));

        if (($flg == "update") && (($this->visit_date   != $this->field["VISIT_DATE"]) ||
                                   ($this->visit_hour   != $this->field["VISIT_HOUR"]) ||
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

        //桁数チェック項目
        $checkField = array("LESSON_CLASS"          => array(60, "現在の教科"),
                            "VISIT_REASON1_TEXT"    => array(90, "来室理由"),
                            "VISIT_REASON2_TEXT"    => array(90, "来室理由"),
                            "VISIT_REASON3_TEXT"    => array(90, "来室理由"),
                            "SINCE_WHEN_TEXT"       => array(90, "いつから"),
                            "CONDITION7_TEXT"       => array(90, "体調等４"),
                            "CONDITION8_TEXT"       => array(90, "体調等５"),
                            "CONDITION12_TEXT"      => array(90, "「ある」と答えた人"),
                            "INJURY_PART1_TEXT"     => array(90, "けがの部位１"),
                            "INJURY_PART2_TEXT"     => array(90, "けがの部位２"),
                            "INJURY_PART3_TEXT"     => array(90, "けがの部位３"),
                            "OCCUR_SITUATION"       => array(90, "発生時の場合"),
                            "OCCUR_PLACE_TEXT"      => array(90, "発生時の場所"),
                            "TREATMENT1_TEXT"       => array(90, "処置１"),
                            "TREATMENT2_TEXT"       => array(90, "処置２"),
                            "TREATMENT3_TEXT"       => array(90, "処置３"),
                            "CONTACT_TEXT"          => array(90, "連絡"));

        //桁数チェック
        foreach ($checkField as $key => $val) {
            if (strlen($this->field[$key]) > $val[0]) {
                $this->setWarning("MSG915", "(".$val[1]."は".$val[0]."バイトまでです)");
                return false;
            }
        }

        //行数チェック
        $moji = 44;
        $gyou = ($this->type == "1") ? 4 : 8;
        if ($this->validate_row_cnt($this->field["SPECIAL_NOTE"], ($moji * 2)) > $gyou) {
            $this->setWarning("MSG915", "(特記事項は{$gyou}行までです)");
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

        knjf150aQuery::getUpdateQuery($this, $this->field, $flg);
        $this->setMessage("MSG201");

        $this->visit_date   = $this->field["VISIT_DATE"];
        $this->visit_hour   = $this->field["VISIT_HOUR"];
        $this->visit_minute = $this->field["VISIT_MINUTE"];
        $this->cmd          = "subform".$this->type."A";

        return;
    }
}
