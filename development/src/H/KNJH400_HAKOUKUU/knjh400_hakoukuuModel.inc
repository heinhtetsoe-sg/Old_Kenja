<?php

require_once('for_php7.php');

class knjh400_hakoukuuModel extends Model
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

    public function knjh400_hakoukuuModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
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
        $this->cmd = VARS::request("cmd");

        $this->schregno = VARS::request("SCHREGNO") != "" ? VARS::request("SCHREGNO") : $this->schregno;

        $this->year = VARS::request("YEAR") != "" ? VARS::request("YEAR") : $this->year;
        $this->exp_year = $this->year;

        $this->semester = VARS::request("SEMESTER") != "" ? VARS::request("SEMESTER") : $this->semester;
        $this->exp_semester = $this->semester;

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

        $this->disp = VARS::post("DISP");
        if ($this->disp == "") {
            $this->disp = $this->Properties["F020_SISIKI_DISP"];
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
        $query = knjh400_hakoukuuQuery::getSchoolKind($this->getGrade);
        $this->getSchKind = $db->getOne($query);
        //テーブルの有無チェック
        $query = knjh400_hakoukuuQuery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && ($this->schregno || ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""))) {
            //生徒項目名取得
            $schName = $db->getOne(knjh400_hakoukuuQuery::getSchName($this));
        }
        $this->sch_label = (strlen($schName) > 0) ? $schName : '生徒';

        $this->is_f020_otherdisese_hyouji = false;
        $this->is_f020_otherdisese_hyouji2 = false;
        $this->is_f020_dentistremark_hyouji = false;
        //名称マスタ取得(Z010)
        $this->z010 = $db->getOne(knjh400_hakoukuuQuery::getZ010($this));
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
        return true;
    }
    // 歯式入力更新後のモデルを返します
    public function &getSendModel()
    {
        //金額入力
        $db = Query::dbCheckOut();
        $RowT = knjh400_hakoukuuQuery::getMedexamToothDat($this); //生徒健康診断歯口腔データ取得
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
