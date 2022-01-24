<?php

require_once('for_php7.php');

class knjf020Model extends Model
{
    public $cmd;
    public $schregno;          //生徒学籍番号
    public $name;              //生徒名
    public $GradeClass;        //学年、クラス
    public $Hrname;            //クラス名称
    public $year;              //現在処理年度
    public $semester;          //現在処理学期
    public $staffcd;           //職員コード
    public $field = array();           //フィールドデータ
    public $control_data = array();    //コントロールマスタデータ
    public $replace_data = array();    //一括更新処理データ
    public $adultUpLabelName = array();
    public $adultLwLabelName = array();
    public $babyUpLabelName = array();
    public $babyLwLabelName = array();
    public $subField = array();
    public $subTotalField = array();
    public $nyuryoku;
    public $type_div;
    public $sisikiClick;
    public $is_f020_otherdisese_hyouji;
    public $is_f020_otherdisese_hyouji2;
    public $is_f020_dentistremark_hyouji;

    public function knjf020Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->staffcd = STAFFCD;

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->KNJF030D = ($this->Properties["hokenChouhyouProgramid"] == "KNJF030D") ? "1" : "";

        if (VARS::get("PROGRAMID")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->GradeClass);
            unset($this->Hrname);
            unset($this->getGrade);
        }
        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->GradeClass);
            unset($this->Hrname);
            unset($this->getGrade);
        }
        if (VARS::get("SCHREGNO")) {
            $this->schregno  = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }
        //フィールド
        $this->field = array("TOOTH_DATE"            =>  VARS::post("TOOTH_DATE"),            //健康診断実施日付
                             "JAWS_JOINTCD"          =>  VARS::post("JAWS_JOINTCD"),          //歯列・咬合
                             "JAWS_JOINTCD3"         =>  VARS::post("JAWS_JOINTCD3"),         //咬合
                             "JAWS_JOINTCD2"         =>  VARS::post("JAWS_JOINTCD2"),         //顎関節
                             "PLAQUECD"              =>  VARS::post("PLAQUECD"),              //歯垢の状態
                             "GUMCD"                 =>  VARS::post("GUMCD"),                 //歯肉の状態
                             "CALCULUS"              =>  VARS::post("CALCULUS"),              //歯石沈着
                             "ORTHODONTICS"          =>  VARS::post("ORTHODONTICS"),          //矯正
                             "BABYTOOTH"             =>  VARS::post("BABYTOOTH"),             //乳歯・現在数
                             "REMAINBABYTOOTH"       =>  VARS::post("REMAINBABYTOOTH"),       //乳歯・未処置数
                             "TREATEDBABYTOOTH"      =>  VARS::post("TREATEDBABYTOOTH"),      //乳歯・処置数
                             "BRACK_BABYTOOTH"       =>  VARS::post("BRACK_BABYTOOTH"),       //乳歯・要注意乳歯数
                             "ADULTTOOTH"            =>  VARS::post("ADULTTOOTH"),            //永久歯・現在数
                             "REMAINADULTTOOTH"      =>  VARS::post("REMAINADULTTOOTH"),      //永久歯・未処置数
                             "TREATEDADULTTOOTH"     =>  VARS::post("TREATEDADULTTOOTH"),     //永久歯・処置数
                             "LOSTADULTTOOTH"        =>  VARS::post("LOSTADULTTOOTH"),        //永久歯・喪失数
                             "BRACK_ADULTTOOTH"      =>  VARS::post("BRACK_ADULTTOOTH"),      //永久歯・要観察歯数
                             "OTHERDISEASECD"        =>  VARS::post("OTHERDISEASECD"),        //その他疾病及び異常
                             "OTHERDISEASE"          =>  VARS::post("OTHERDISEASE"),          //その他疾病及び異常・その他
                             "OTHERDISEASECD3"       =>  VARS::post("OTHERDISEASECD3"),       //その他疾病及び異常２
                             "OTHERDISEASE3"         =>  VARS::post("OTHERDISEASE3"),         //その他疾病及び異常２
                             "OTHERDISEASECD4"       =>  VARS::post("OTHERDISEASECD4"),       //その他疾病及び異常３
                             "OTHERDISEASE4"         =>  VARS::post("OTHERDISEASE4"),         //その他疾病及び異常３
                             "OTHERDISEASECD2"       =>  VARS::post("OTHERDISEASECD2"),       //口腔の疾病及び異常
                             "OTHERDISEASE2"         =>  VARS::post("OTHERDISEASE2"),         //口腔の疾病及び異常・その他
                             "OTHERDISEASE_REMARK1"  =>  VARS::post("OTHERDISEASE_REMARK1"),  //口腔の疾病及び異常 2行目
                             "OTHERDISEASE_REMARK2"  =>  VARS::post("OTHERDISEASE_REMARK2"),  //口腔の疾病及び異常・その他 2行目
                             "OTHERDISEASE_REMARK3"  =>  VARS::post("OTHERDISEASE_REMARK3"),  //口腔の疾病及び異常 3行目
                             "OTHERDISEASE_REMARK4"  =>  VARS::post("OTHERDISEASE_REMARK4"),  //口腔の疾病及び異常・その他 3行目
                             "DENTISTREMARKCD"       =>  VARS::post("DENTISTREMARKCD"),       //所見
                             "DENTISTREMARK"         =>  VARS::post("DENTISTREMARK"),         //所見テキスト
                             "DENTISTREMARK_REMARK1" =>  VARS::post("DENTISTREMARK_REMARK1"), //学校医 所見 2行目
                             "DENTISTREMARK_REMARK2" =>  VARS::post("DENTISTREMARK_REMARK2"), //学校医 所見テキスト 2行目
                             "DENTISTREMARK_REMARK3" =>  VARS::post("DENTISTREMARK_REMARK3"), //学校医 所見 3行目
                             "DENTISTREMARK_REMARK4" =>  VARS::post("DENTISTREMARK_REMARK4"), //学校医 所見テキスト 3行目
                             "DENTISTREMARKDATE"     =>  VARS::post("DENTISTREMARKDATE"),     //所見日付
                             "DENTISTREMARK_CO"      =>  VARS::post("DENTISTREMARK_CO"),      //所見(CO)
                             "DENTISTREMARK_GO"      =>  VARS::post("DENTISTREMARK_GO"),      //所見(GO)
                             "DENTISTREMARK_G"       =>  VARS::post("DENTISTREMARK_G"),       //所見(G)
                             "DOC_NAME"              =>  VARS::post("DOC_NAME"),              //学校医・氏名(三重のみ)
                             "DENTISTTREATCD"        =>  VARS::post("DENTISTTREATCD"),        //事後処置
                             "DENTISTTREAT"          =>  VARS::post("DENTISTTREAT"),          //事後処置
                             "DENTISTTREAT2"         =>  VARS::post("DENTISTTREAT2"),         //事後処置2
                             "DENTISTTREAT3"         =>  VARS::post("DENTISTTREAT3")          //事後処置3
                             );

        //フィールド（歯列）
        $array1 = array("UP", "LW");
        $array2 = array("R", "L");
        $array3 = array("BABY" => 5, "ADULT" => 8);
        foreach ($array1 as $uplw) {
            foreach ($array2 as $rl) {
                foreach ($array3 as $ba => $no) {
                    for ($i = 1; $i <= $no; $i++) {
                        $fieldName = $uplw."_".$rl."_".$ba.$i;
                        $this->field[$fieldName] = VARS::post($fieldName."_FORM_ID");
                    }
                }
            }
        }

        $this->year = CTRL_YEAR;
        $this->semester= CTRL_SEMESTER;
        $this->disp = VARS::post("DISP");
        if ($this->disp == "") {
            $this->disp = $this->Properties["F020_SISIKI_DISP"];
        }
        //一括更新処理処理
        if ($this->cmd == "replace_update") {
            $this->replace_data["selectdata"]   = VARS::post("selectdata");
            $this->replace_data["head_field"]   = $this->replace_data["tooth_field"] = array();
            $this->replace_data["head_check"]   = array(VARS::post("RCHECK0"));
            $this->replace_data["tooth_check"]  = array(VARS::post("RCHECK1"),
                                                        VARS::post("RCHECK2"),
                                                        VARS::post("RCHECK3"),
                                                        VARS::post("RCHECK4"),
                                                        VARS::post("RCHECK5"),
                                                        VARS::post("RCHECK6"),
                                                        VARS::post("RCHECK7"),
                                                        VARS::post("RCHECK8"),
                                                        VARS::post("RCHECK9"),
                                                        VARS::post("RCHECK10"),
                                                        VARS::post("RCHECK11"),
                                                        VARS::post("RCHECK12"),
                                                        VARS::post("RCHECK13"),
                                                        VARS::post("RCHECK14"),
                                                        VARS::post("RCHECK15"),
                                                        VARS::post("RCHECK16"),
                                                        VARS::post("RCHECK18"));
            $this->replace_data["check_all"] = VARS::post("RCHECK17");
            if (VARS::post("RCHECK0")=="1") {
                $this->replace_data["head_field"]["TOOTH_DATE"]         = str_replace("/", "-", VARS::post("TOOTH_DATE"));
            }
            if (VARS::post("RCHECK1")=="1") {
                $this->replace_data["tooth_field"]["JAWS_JOINTCD"]      = VARS::post("JAWS_JOINTCD");
            }
            if (VARS::post("RCHECK2")=="1") {
                $this->replace_data["tooth_field"]["JAWS_JOINTCD2"]     = VARS::post("JAWS_JOINTCD2");
            }
            if (VARS::post("RCHECK3")=="1") {
                $this->replace_data["tooth_field"]["PLAQUECD"]          = VARS::post("PLAQUECD");
            }
            if (VARS::post("RCHECK4")=="1") {
                $this->replace_data["tooth_field"]["GUMCD"]             = VARS::post("GUMCD");
            }
            if (VARS::post("RCHECK5")=="1") {
                $this->replace_data["tooth_field"]["CALCULUS"]          = VARS::post("CALCULUS");
            }
            if (VARS::post("RCHECK6")=="1") {
                $this->replace_data["tooth_field"]["ORTHODONTICS"]      = VARS::post("ORTHODONTICS");
            }
            if (VARS::post("RCHECK7")=="1") {
                $this->replace_data["tooth_field"]["OTHERDISEASECD"]    = VARS::post("OTHERDISEASECD");
                $this->replace_data["tooth_field"]["OTHERDISEASE"]      = VARS::post("OTHERDISEASE");
            }
            if (VARS::post("RCHECK8")=="1") {
                $this->replace_data["tooth_field"]["DENTISTREMARKCD"] = VARS::post("DENTISTREMARKCD");
                $this->replace_data["tooth_field"]["DENTISTREMARK"]   = VARS::post("DENTISTREMARK");
            }
            if (VARS::post("RCHECK9")=="1") {
                $this->replace_data["tooth_field"]["DENTISTREMARKDATE"] = str_replace("/", "-", VARS::post("DENTISTREMARKDATE"));
            }
            if (VARS::post("RCHECK10")=="1") {
                $this->replace_data["tooth_field"]["DENTISTTREATCD"] = VARS::post("DENTISTTREATCD");
                $this->replace_data["tooth_field"]["DENTISTTREAT"]   = VARS::post("DENTISTTREAT");
            }
            if (VARS::post("RCHECK11")=="1") {
                $this->replace_data["tooth_field"]["JAWS_JOINTCD3"]    = VARS::post("JAWS_JOINTCD3");
            }
            if (VARS::post("RCHECK12")=="1") {
                $this->replace_data["tooth_field"]["OTHERDISEASECD"]   = VARS::post("OTHERDISEASECD");
                $this->replace_data["tooth_field"]["OTHERDISEASE"]     = VARS::post("OTHERDISEASE");
            }
            if (VARS::post("RCHECK13")=="1") {
                $this->replace_data["tooth_field"]["DENTISTREMARK_CO"] = VARS::post("DENTISTREMARK_CO");
            }
            if (VARS::post("RCHECK14")=="1") {
                $this->replace_data["tooth_field"]["DENTISTREMARK_GO"] = VARS::post("DENTISTREMARK_GO");
            }
            if (VARS::post("RCHECK15")=="1") {
                $this->replace_data["tooth_field"]["DENTISTREMARK_G"]  = VARS::post("DENTISTREMARK_G");
            }
            if (VARS::post("RCHECK16")=="1") {
                $this->replace_data["tooth_field"]["DENTISTTREATCD"]   = VARS::post("DENTISTTREATCD");
                $this->replace_data["tooth_field"]["DENTISTTREAT"]     = VARS::post("DENTISTTREAT");
                $this->replace_data["tooth_field"]["DENTISTTREAT2"]    = VARS::post("DENTISTTREAT2");
                $this->replace_data["tooth_field"]["DENTISTTREAT3"]    = VARS::post("DENTISTTREAT3");
            }
            if (VARS::post("RCHECK18")=="1") {
                $this->replace_data["tooth_field"]["DOC_NAME"]  = VARS::post("DOC_NAME");
            }
        }
        if (VARS::get("ini2")) {
            unset($this->replace_data);
        }

        unset($this->nyuryoku);
        unset($this->type_div);
        $this->subInit();

        //生徒項目名切替処理
        $schName = "";
        $db = Query::dbCheckOut();
        //校種取得
        if (VARS::get("GRADE")) {
            $this->getGrade = VARS::get("GRADE");
        }
        $query = knjf020Query::getSchoolKind($this->getGrade);
        $this->getSchKind = $db->getOne($query);
        //テーブルの有無チェック
        $query = knjf020Query::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && ($this->schregno || ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""))) {
            //生徒項目名取得
            $schName = $db->getOne(knjf020Query::getSchName($this));
        }
        $this->sch_label = (strlen($schName) > 0) ? $schName : '生徒';

        $this->is_f020_otherdisese_hyouji = false;
        $this->is_f020_otherdisese_hyouji2 = false;
        $this->is_f020_dentistremark_hyouji = false;
        //名称マスタ取得(Z010)
        $this->z010 = $db->getOne(knjf020Query::getZ010($this));
        if ($this->Properties["printKenkouSindanIppan"] == "1") {
            if ($this->z010 != "miyagiken" && $this->z010 != "mieken") {
                //三重県、宮城県以外の場合
                if ($this->Properties["F020_OTHERDISESE_HYOUJI"] == "3") {
                    $this->is_f020_otherdisese_hyouji = true;
                }
                if ($this->Properties["F020_OTHERDISESE_HYOUJI2"] == "3") {
                    $this->is_f020_otherdisese_hyouji2 = true;
                }
                if ($this->Properties["F020_DENTISTREMARK_HYOUJI"] == "3") {
                    $this->is_f020_dentistremark_hyouji = true;
                }
            } else {
                $this->is_f020_otherdisese_hyouji = false;
                $this->is_f020_otherdisese_hyouji2 = false;
                $this->is_f020_dentistremark_hyouji = false;
            }
        }

        Query::dbCheckIn($db);
    }

    //歯式入力Init
    public function subInit()
    {
        unset($this->subField);
        unset($this->subTotalField);
        $setR = "R";
        $setL = "L";
        $setRname = "右";
        $setLname = "左";
        if ($this->disp == "2") {
            $setR = "L";
            $setL = "R";
            $setRname = "左";
            $setLname = "右";
        }
        $this->adultUpLabelName = array("UP_{$setR}_ADULT8" => "{$setRname}8",
                                        "UP_{$setR}_ADULT7" => "{$setRname}7",
                                        "UP_{$setR}_ADULT6" => "{$setRname}6",
                                        "UP_{$setR}_ADULT5" => "{$setRname}5",
                                        "UP_{$setR}_ADULT4" => "{$setRname}4",
                                        "UP_{$setR}_ADULT3" => "{$setRname}3",
                                        "UP_{$setR}_ADULT2" => "{$setRname}2",
                                        "UP_{$setR}_ADULT1" => "{$setRname}1",
                                        "UP_{$setL}_ADULT1" => "{$setLname}1",
                                        "UP_{$setL}_ADULT2" => "{$setLname}2",
                                        "UP_{$setL}_ADULT3" => "{$setLname}3",
                                        "UP_{$setL}_ADULT4" => "{$setLname}4",
                                        "UP_{$setL}_ADULT5" => "{$setLname}5",
                                        "UP_{$setL}_ADULT6" => "{$setLname}6",
                                        "UP_{$setL}_ADULT7" => "{$setLname}7",
                                        "UP_{$setL}_ADULT8" => "{$setLname}8");

        $this->adultLwLabelName = array("LW_{$setR}_ADULT8" => "{$setRname}8",
                                        "LW_{$setR}_ADULT7" => "{$setRname}7",
                                        "LW_{$setR}_ADULT6" => "{$setRname}6",
                                        "LW_{$setR}_ADULT5" => "{$setRname}5",
                                        "LW_{$setR}_ADULT4" => "{$setRname}4",
                                        "LW_{$setR}_ADULT3" => "{$setRname}3",
                                        "LW_{$setR}_ADULT2" => "{$setRname}2",
                                        "LW_{$setR}_ADULT1" => "{$setRname}1",
                                        "LW_{$setL}_ADULT1" => "{$setLname}1",
                                        "LW_{$setL}_ADULT2" => "{$setLname}2",
                                        "LW_{$setL}_ADULT3" => "{$setLname}3",
                                        "LW_{$setL}_ADULT4" => "{$setLname}4",
                                        "LW_{$setL}_ADULT5" => "{$setLname}5",
                                        "LW_{$setL}_ADULT6" => "{$setLname}6",
                                        "LW_{$setL}_ADULT7" => "{$setLname}7",
                                        "LW_{$setL}_ADULT8" => "{$setLname}8");

        $this->babyUpLabelName = array("UP_{$setR}_BABY5" => "{$setRname}E",
                                       "UP_{$setR}_BABY4" => "{$setRname}D",
                                       "UP_{$setR}_BABY3" => "{$setRname}C",
                                       "UP_{$setR}_BABY2" => "{$setRname}B",
                                       "UP_{$setR}_BABY1" => "{$setRname}A",
                                       "UP_{$setL}_BABY1" => "{$setLname}A",
                                       "UP_{$setL}_BABY2" => "{$setLname}B",
                                       "UP_{$setL}_BABY3" => "{$setLname}C",
                                       "UP_{$setL}_BABY4" => "{$setLname}D",
                                       "UP_{$setL}_BABY5" => "{$setLname}E");

        $this->babyLwLabelName = array("LW_{$setR}_BABY5" => "{$setRname}E",
                                       "LW_{$setR}_BABY4" => "{$setRname}D",
                                       "LW_{$setR}_BABY3" => "{$setRname}C",
                                       "LW_{$setR}_BABY2" => "{$setRname}B",
                                       "LW_{$setR}_BABY1" => "{$setRname}A",
                                       "LW_{$setL}_BABY1" => "{$setLname}A",
                                       "LW_{$setL}_BABY2" => "{$setLname}B",
                                       "LW_{$setL}_BABY3" => "{$setLname}C",
                                       "LW_{$setL}_BABY4" => "{$setLname}D",
                                       "LW_{$setL}_BABY5" => "{$setLname}E");

        //クリア
        $this->subTotalField["BABYTOOTH"] = 0;
        $this->subTotalField["REMAINBABYTOOTH"] = 0;
        $this->subTotalField["TREATEDBABYTOOTH"] = 0;
        $this->subTotalField["BRACK_BABYTOOTH"] = 0;
        $this->subTotalField["ADULTTOOTH"] = 0;
        $this->subTotalField["REMAINADULTTOOTH"] = 0;
        $this->subTotalField["TREATEDADULTTOOTH"] = 0;
        $this->subTotalField["LOSTADULTTOOTH"] = 0;
        $this->subTotalField["BRACK_ADULTTOOTH"] = 0;

        $this->setSubField($this->adultUpLabelName, "ADULT");
        $this->setSubField($this->adultLwLabelName, "ADULT");
        $this->setSubField($this->babyUpLabelName, "BABY");
        $this->setSubField($this->babyLwLabelName, "BABY");

        if (VARS::post("NYURYOKU")) {
            $this->nyuryoku = VARS::post("NYURYOKU");
        }
        if (VARS::post("TYPE_DIV")) {
            $this->type_div = VARS::post("TYPE_DIV");
        }
        if (VARS::request("SISIKI_CLICK")) {
            $this->sisikiClick = VARS::request("SISIKI_CLICK");
        }
    }

    public function setSubField($setField, $div)
    {
        foreach ($setField as $key => $val) {
            $toothVal = VARS::post($key) ? VARS::post($key."_FORM_ID") : "";
            $this->subField[$key] = $toothVal;
            if ($toothVal && $toothVal != "04") {
                $this->subTotalField[$div."TOOTH"] += 1;
            }
            if ($toothVal == "02") {
                $this->subTotalField["REMAIN".$div."TOOTH"] += 1;
            }
            if ($toothVal == "03") {
                $this->subTotalField["TREATED".$div."TOOTH"] += 1;
            }
            if ($toothVal == "04" && $div == "ADULT") {
                $this->subTotalField["LOST".$div."TOOTH"] += 1;
            }
            if ($toothVal == "05" && $div == "BABY") {
                $this->subTotalField["BRACK_".$div."TOOTH"] += 1;
            }
            if ($toothVal == "06" && $div == "ADULT") {
                $this->subTotalField["BRACK_".$div."TOOTH"] += 1;
                $this->subTotalField["DENTISTREMARK_CO"] += 1;
            }
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG308");
            return false;
        }
        //健康診断実施日が入力されていない場合現在処理日付(ATTEND_CTRL_DATE)を代入
        if ($this->field["TOOTH_DATE"]=="") {
            if (VARS::post("HIDDENDATE")) {
                $this->field["TOOTH_DATE"] = VARS::post("HIDDENDATE");
            } else {
                $this->field["TOOTH_DATE"] = CTRL_DATE;
            }
        }

        if (
            $this->field["BABYTOOTH"] != ""         || $this->field["REMAINBABYTOOTH"] != "" ||
            $this->field["TREATEDBABYTOOTH"] != ""  || $this->field["BRACK_BABYTOOTH"] != "" ||
            $this->field["ADULTTOOTH"] != ""        || $this->field["REMAINADULTTOOTH"] != "" ||
            $this->field["TREATEDADULTTOOTH"] != "" || $this->field["LOSTADULTTOOTH"] != "" ||
            $this->field["BRACK_ADULTTOOTH"] != ""  || $this->field["DENTISTREMARK_CO"] != ""
        ) {
            if ($this->field["BABYTOOTH"] == "") {
                $this->field["BABYTOOTH"] = "0";
            }
            if ($this->field["REMAINBABYTOOTH"] == "") {
                $this->field["REMAINBABYTOOTH"] = "0";
            }
            if ($this->field["TREATEDBABYTOOTH"] == "") {
                $this->field["TREATEDBABYTOOTH"] = "0";
            }
            if ($this->field["BRACK_BABYTOOTH"] == "") {
                $this->field["BRACK_BABYTOOTH"] = "0";
            }
            if ($this->field["ADULTTOOTH"] == "") {
                $this->field["ADULTTOOTH"] = "0";
            }
            if ($this->field["REMAINADULTTOOTH"] == "") {
                $this->field["REMAINADULTTOOTH"] = "0";
            }
            if ($this->field["TREATEDADULTTOOTH"] == "") {
                $this->field["TREATEDADULTTOOTH"] = "0";
            }
            if ($this->field["LOSTADULTTOOTH"] == "") {
                $this->field["LOSTADULTTOOTH"] = "0";
            }
            if ($this->field["BRACK_ADULTTOOTH"] == "") {
                $this->field["BRACK_ADULTTOOTH"] = "0";
            }
            if ($this->field["DENTISTREMARK_CO"] == "") {
                $this->field["DENTISTREMARK_CO"] = "0";
            }
        }

        if (strlen($this->field["BABYTOOTH"]) > 2) {
            $this->setWarning("MSG915", "乳歯現在数は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMAINBABYTOOTH"]) > 2) {
            $this->setWarning("MSG915", "乳歯未処置数は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["TREATEDBABYTOOTH"]) > 2) {
            $this->setWarning("MSG915", "乳歯処置数は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["BRACK_BABYTOOTH"]) > 2) {
            $this->setWarning("MSG915", "要注意乳歯数は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADULTTOOTH"]) > 2) {
            $this->setWarning("MSG915", "永久歯現在数は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMAINADULTTOOTH"]) > 2) {
            $this->setWarning("MSG915", "永久歯未処置数は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["TREATEDADULTTOOTH"]) > 2) {
            $this->setWarning("MSG915", "永久歯処置数は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["LOSTADULTTOOTH"]) > 2) {
            $this->setWarning("MSG915", "永久歯喪失数は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["BRACK_ADULTTOOTH"]) > 2) {
            $this->setWarning("MSG915", "要観察歯数は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["OTHERDISEASE"]) > 60) {
            $this->setWarning("MSG915", "その他疾病及び異常は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["OTHERDISEASE3"]) > 60) {
            $this->setWarning("MSG915", "その他疾病及び異常は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["OTHERDISEASE4"]) > 60) {
            $this->setWarning("MSG915", "その他疾病及び異常は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["OTHERDISEASE2"]) > 60) {
            $this->setWarning("MSG915", "口腔の疾病及び異常は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["DENTISTREMARK_CO"]) > 2) {
            $this->setWarning("MSG915", "所見(CO)は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["DENTISTREMARK"]) > 60) {
            $this->setWarning("MSG915", "所見は60バイトまでです。");
            return false;
        }
        if ($this->Properties["printKenkouSindanIppan"] == "2") {
            if (strlen($this->field["DENTISTTREAT"]) > 30) {
                $this->setWarning("MSG915", "事後処置は30バイトまでです。");
                return false;
            }
        } else {
            if (strlen($this->field["DENTISTTREAT"]) > 60) {
                $this->setWarning("MSG915", "事後処置は60バイトまでです。");
                return false;
            }
        }
        if (strlen($this->field["DENTISTTREAT2"]) > 30) {
            $this->setWarning("MSG915", "事後処置2は30バイトまでです。");
            return false;
        }
        if (strlen($this->field["DENTISTTREAT3"]) > 30) {
            $this->setWarning("MSG915", "事後処置3は30バイトまでです。");
            return false;
        }
        $this->field["TOOTH_DATE"] = str_replace("/", "-", $this->field["TOOTH_DATE"]);
        $this->field["DENTISTREMARKDATE"] = str_replace("/", "-", $this->field["DENTISTREMARKDATE"]);
        return true;
    }
    //更新処理
    public function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }
    public function &update()
    {
        //レコードが存在するのかチェック
        $db = Query::dbCheckOut();
        $query = knjf020Query::checkHdat($this);
        $count = $db->getOne($query);
        Query::dbCheckIn($db);

        if ($count >= 1) {
            knjf020Query::getHdatUpdate($this);//あればMEDEXAM_HDATをアップデート
        } else {
            knjf020Query::getHdatInsert($this);//なければMEDEXAM_HDATにインサート
        }
        //年度及び学籍番号でMEDEXAM_TOOTH_DATが存在する場合は更新(UPDATE)、存在しない場合(INSERT)する
        $row = knjf020Query::getMedexamToothDat($this);       //MEDEXAM_TOOTH_DAT取得
        //データが存在しない場合
        if ($row == "") {
            $flag = "insert";
            knjf020Query::getInsertUpdateQuery($this, $flag);
        } else {//データが存在する場合
            $flag = "update";
            knjf020Query::getInsertUpdateQuery($this, $flag);
        }
        return $result;
    }
    //削除処理
    public function &getDeleteModel()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG308");
            return false;
        }
        //年度及び学籍番号でMEDEXAM_TOOTH_DATが存在する場合、削除(DELETE)
        $row = knjf020Query::getMedexamToothDat($this);       //MEDEXAM_TOOTH_DAT取得
        //データが存在しない場合エラー
        if ($row == "") {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->delete()) {
            $this->setMessage("MSG201");
            return;
        }
    }

    //削除
    public function &delete()
    {
        knjf020Query::getDeleteQuery($this);

        $row = knjf020Query::getMedexamDetDat($this);

        if ($row == "") { //MEDEXAM_DET_DATが存在しない場合はMEDEXAM_HDATを削除
            knjf020Query::getHdatDeleteQuery($this);
        } else { //MEDEXAM_DET_DATがあればMEDEXAM_HDATのTOOTH_DATEだけNULLにする
            knjf020Query::getHdatDateDeleteQuery($this);
        }
        return true;
    }

    //一括更新処理
    public function &replaceModel()
    {
        //生徒が選択されてない場合
        if (!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->replace_data["tooth_field"]["DENTISTREMARK"]) > 60) {
            $this->setWarning("MSG915", "所見は60バイトまでです。");
            return false;
        }
        if ($this->Properties["printKenkouSindanIppan"] != "2") {
            if (strlen($this->replace_data["tooth_field"]["DENTISTTREAT"]) > 60) {
                $this->setWarning("MSG915", "事後処置は60バイトまでです。");
                return false;
            }
        }
        //最低ひとつでもチェックがついてる場合のみ更新
        if (!in_array("1", $this->replace_data["head_check"]) && !in_array("1", $this->replace_data["tooth_check"])) {
            return false;
        }
        if (in_array("1", $this->replace_data["tooth_check"]) || in_array("1", $this->replace_data["head_check"])) {
            $this->updateHead();
            $this->updateTooth();
        }
        $this->setMessage("MSG201");
        //添え字を０から順序良くする
        $this->replace_data["check"] = array_merge((array)$this->replace_data["head_check"], (array)$this->replace_data["tooth_check"]);
        $this->replace_data["check"][] = $this->replace_data["check_all"];
    }
    //MEDEXAM_HDATの更新
    public function &updateHead()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $array = explode(",", $this->replace_data["selectdata"]);
        //健康診断実施日が入力されていない場合現在処理日付(ATTEND_CTRL_DATE)を代入
        if ($this->replace_data["head_field"]["TOOTH_DATE"] == "") {
            $flg = "NOINPUT";
            $this->replace_data["head_field"]["TOOTH_DATE"] = CTRL_DATE;
            if (VARS::post("REPLACEHIDDENDATE")=="") {
                $flg2 = "NULL";
                $this->replace_data["head_field"]["TOOTH_DATE"] = CTRL_DATE;
            }
        }
        for ($i = 0; $i < get_count($array); $i++) {
            //MEDEXAM_HDATが存在するかチェックする
            $query = knjf020Query::checkMedexamHdat($this->year, $array[$i]);
            $data = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!is_array($data)) {
                $flag = "noRecord"; //レコードが存在しない
            } elseif (!$data["TOOTH_DATE"]) {
                $flag = "noToothDate"; //レコードが存在してもDATEフィールドが空
            }
            if ($flg == "NOINPUT" && ($flag == "noRecord" || $flag == "noToothDate")) {
                $query = knjf020Query::replaceUpdateHead($this->year, $this->replace_data["head_field"]["TOOTH_DATE"], $array[$i], $flag);
            } elseif ($flg != "NOINPUT") {
                $query = knjf020Query::replaceUpdateHead($this->year, $this->replace_data["head_field"]["TOOTH_DATE"], $array[$i], $flag);
            } elseif ($flg2 == "NULL") {
                $query = knjf020Query::replaceUpdateHead($this->year, $this->replace_data["head_field"]["TOOTH_DATE"], $array[$i], $flag);
            } else {
                continue;
            }
            $flag = "";     //フラグを初期化
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
    }
    //MEDEXAM_TOOTH_DATの更新
    public function &updateTooth()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $array = explode(",", $this->replace_data["selectdata"]);
        for ($i = 0; $i < get_count($array); $i++) {
            $query = knjf020Query::getDentistremarkDate($this->year, $array[$i]);
            $dentistremark_date = $db->getOne($query);

            //近大は「学校歯科医・日付」が設定されていないとき、nullのまま更新する
            if ($this->z010 !== 'KINDAI' && $this->z010 !== 'KINJUNIOR') {
                if ($dentistremark_date == '' && $this->replace_data["tooth_field"]["DENTISTREMARKDATE"] == '') {
                    $this->replace_data["tooth_field"]["DENTISTREMARKDATE"] = $this->replace_data["head_field"]["TOOTH_DATE"];
                }
            }
            //MEDEXAM_TOOTH_DATが存在するかチェックする
            $data = common::GetMasterData("SELECT SCHREGNO FROM MEDEXAM_TOOTH_DAT WHERE YEAR = '".$this->year."' AND SCHREGNO = '".$array[$i]."'");
            if ($data == "") {
                $flag = "insert";
            }
            $query = knjf020Query::replaceUpdateTooth($this, $array[$i], $flag, $db);
            $flag = "";     //フラグを初期化
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
    }

    //歯式更新処理
    public function &getSubUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //MEDEXAM_HDATが存在するかチェックする
        $query = knjf020Query::checkMedexamHdat($this->year, $this->schregno);
        $data = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (!is_array($data)) { //レコードが存在しない場合は今日の日付をインサート
            $query = knjf020Query::replaceUpdateHead($this->year, CTRL_DATE, $this->schregno, "noRecord");
            $db->query($query);
        } elseif (!$data["TOOTH_DATE"]) { //レコードが存在してもTOOTH_DATEフィールドが空の時は今日の日付でアップデート
            $query = knjf020Query::replaceUpdateHead($this->year, CTRL_DATE, $this->schregno, "noToothDate");
            $db->query($query);
        }

        $total_check = 0;
        foreach ($this->subTotalField as $val) {
            $total_check += $val;
        }
        if ($total_check == 0) {
            foreach ($this->subTotalField as $key => $val) {
                $this->subTotalField[$key] = "";
            }
        }

        knjf020Query::sisikiInsUpd($this, $db);

        $db->commit();
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }

    // 歯式入力更新後のモデルを返します
    public function &getSendModel()
    {
        //金額入力
        $db = Query::dbCheckOut();
        $RowT = knjf020Query::getMedexamToothDat($this); //生徒健康診断歯口腔データ取得
        if (is_array($RowT)) {
            $json_array = array();
            foreach ($RowT as $key => $val) {
                $json_array []= "'{$key}':'{$val}'";
            }
            $json = implode(',', $json_array);
            echo "{ 'result':true,{$json} }";
        } else {
            echo "{ 'result':false }";
        }
        Query::dbCheckIn($db);
    }
}
