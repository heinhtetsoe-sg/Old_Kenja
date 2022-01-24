<?php

require_once('for_php7.php');
class knjf010jModel extends Model
{
    public $cmd;
    public $schregno;          //生徒学籍番号
    public $name;              //生徒名
    public $GradeClass;        //学年、クラス
    public $Hrname;            //クラス名称
    public $school_kind;       //学校種別
    public $year;              //現在処理年度
    public $semester;          //現在処理学期
    public $staffcd;           //職員コード
    public $field = array();           //フィールドデータ
    public $control_data = array();    //コントロールマスタデータ
    public $replace_data = array();    //一括更新処理データ
    public $z010name1;
    public $tb_filmnoFieldSize;    //結核の画像番号サイズ

    public function knjf010jModel()
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

        if (VARS::get("PROGRAMID")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->GradeClass);
            unset($this->getGrade);
            unset($this->Hrname);
            unset($this->school_kind);

            //学校を判断
            $db = Query::dbCheckOut();
            $this->z010name1 = $db->getOne(knjf010jQuery::getSchoolName());
            Query::dbCheckIn($db);

            //福井県フラグ
            $this->isFukui = ($this->z010name1 == "fukuiken") ? true: false;

            //佐賀県フラグ
            $this->isSaga = ($this->z010name1 == "sagaken") ? true: false;

            //三重県フラグ
            $this->isMie = ($this->z010name1 == "mieken") ? true: false;

            //広工大フラグ
            $this->isHirokoudai = ($this->z010name1 == "hirokoudai") ? true: false;

            //駒澤フラグ
            $this->isKoma = ($this->z010name1 == "koma") ? true: false;
        }
        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->GradeClass);
            unset($this->getGrade);
            unset($this->Hrname);
            unset($this->school_kind);
        }
        if (VARS::get("SCHREGNO")) {
            $this->schregno  = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }
        //フィールド
        $this->field = array("DATE"                      => VARS::post("DATE"),                       //健康診断実施日付
                             "HEIGHT"                    => VARS::post("HEIGHT"),                     //身長
                             "WEIGHT"                    => VARS::post("WEIGHT"),                     //体重
                             "SITHEIGHT"                 => VARS::post("SITHEIGHT"),                  //座高
                             "NO_PRINT_OBESITY_INDEX"    => VARS::post("NO_PRINT_OBESITY_INDEX"),     //肥満度印字無し
                             "R_BAREVISION"              => VARS::post("R_BAREVISION"),               //視力・右裸眼（数字）
                             "R_BAREVISION_MARK"         => VARS::post("R_BAREVISION_MARK"),          //視力・右裸眼（文字）
                             "R_VISION"                  => VARS::post("R_VISION"),                   //視力・右矯正（数字）
                             "R_VISION_MARK"             => VARS::post("R_VISION_MARK"),              //視力・右矯正（文字）
                             "R_VISION_CANTMEASURE"      => VARS::post("R_VISION_CANTMEASURE"),       //視力・（右）測定困難
                             "R_VISION_MARK_CANTMEASURE" => VARS::post("R_VISION_MARK_CANTMEASURE"),  //視力・（記号右）測定困難
                             "L_BAREVISION"              => VARS::post("L_BAREVISION"),               //視力・左裸眼（数字）
                             "L_BAREVISION_MARK"         => VARS::post("L_BAREVISION_MARK"),          //視力・左裸眼（文字）
                             "L_VISION"                  => VARS::post("L_VISION"),                   //視力・左矯正（数字）
                             "L_VISION_MARK"             => VARS::post("L_VISION_MARK"),              //視力・左矯正（文字）
                             "L_VISION_CANTMEASURE"      => VARS::post("L_VISION_CANTMEASURE"),       //視力・（左）測定困難
                             "L_VISION_MARK_CANTMEASURE" => VARS::post("L_VISION_MARK_CANTMEASURE"),  //視力・（記号左）測定困難
                             "VISION_CANTMEASURE"        => VARS::post("VISION_CANTMEASURE"),         //視力・測定不能（文字）
                             "EYE_TEST_RESULT"           => VARS::post("EYE_TEST_RESULT"),            //目の疾病及び異常(テキスト)
                             "EYE_TEST_RESULT2"          => VARS::post("EYE_TEST_RESULT2"),           //眼科検診結果2
                             "EYE_TEST_RESULT3"          => VARS::post("EYE_TEST_RESULT3"),           //眼科検診結果3
                             "VISION_CANTMEASURE"        => VARS::post("VISION_CANTMEASURE"),         //色覚異常(テキスト)
                             "R_EAR_DB"                  => VARS::post("R_EAR_DB"),                   //聴力・右DB
                             "R_EAR_DB_1000"             => VARS::post("R_EAR_DB_1000"),              //聴力・右DB
                             "R_EAR_DB_4000"             => VARS::post("R_EAR_DB_4000"),              //聴力・右4000Hz
                             "R_EAR"                     => VARS::post("R_EAR"),                      //聴力・右状態
                             "R_EAR_CANTMEASURE"         => VARS::post("R_EAR_CANTMEASURE"),          //聴力・（右）測定困難
                             "R_EAR_DB_IN"               => VARS::post("R_EAR_DB_IN"),                //聴力・右DB    （装用時）
                             "R_EAR_DB_4000_IN"          => VARS::post("R_EAR_DB_4000_IN"),           //聴力・右4000Hz（装用時）
                             "R_EAR_IN"                  => VARS::post("R_EAR_IN"),                   //聴力・右状態  （装用時）
                             "L_EAR_DB"                  => VARS::post("L_EAR_DB"),                   //聴力・左DB
                             "L_EAR_DB_1000"             => VARS::post("L_EAR_DB_1000"),              //聴力・左DB
                             "L_EAR_DB_4000"             => VARS::post("L_EAR_DB_4000"),              //聴力・左4000Hz
                             "L_EAR"                     => VARS::post("L_EAR"),                      //聴力・左状態
                             "L_EAR_CANTMEASURE"         => VARS::post("L_EAR_CANTMEASURE"),          //聴力・（左）測定困難
                             "L_EAR_DB_IN"               => VARS::post("L_EAR_DB_IN"),                //聴力・左DB    （装用時）
                             "L_EAR_DB_4000_IN"          => VARS::post("L_EAR_DB_4000_IN"),           //聴力・左4000Hz（装用時）
                             "L_EAR_IN"                  => VARS::post("L_EAR_IN"),                   //聴力・左状態  （装用時）
                             "ALBUMINURIA1CD"            => VARS::post("ALBUMINURIA1CD"),             //尿・１次蛋白
                             "URICSUGAR1CD"              => VARS::post("URICSUGAR1CD"),               //尿・１次糖
                             "URICBLEED1CD"              => VARS::post("URICBLEED1CD"),               //尿・１次潜血
                             "ALBUMINURIA2CD"            => VARS::post("ALBUMINURIA2CD"),             //尿・再検査蛋白
                             "URICSUGAR2CD"              => VARS::post("URICSUGAR2CD"),               //尿・再検査糖
                             "URICBLEED2CD"              => VARS::post("URICBLEED2CD"),               //尿・再検査潜血
                             "URICOTHERTESTCD"           => VARS::post("URICOTHERTESTCD"),            //尿・その他の検査(printKenkouSindanIppan = 2)
                             "DETAILED_EXAMINATION"      => VARS::post("DETAILED_EXAMINATION"),       //尿・精密検査(コンボ)
                             "URI_ADVISECD"              => VARS::post("URI_ADVISECD"),               //尿・精密検査(指導区分コンボ)
                             "URICOTHERTEST"             => VARS::post("URICOTHERTEST"),              //尿・精密検査(テキスト)
                             "NUTRITIONCD"               => VARS::post("NUTRITIONCD"),                //栄養状態
                             "NUTRITIONCD_REMARK"        => VARS::post("NUTRITIONCD_REMARK"),         //栄養状態備考
                             "SPINERIBCD"                => VARS::post("SPINERIBCD"),                 //脊柱・胸郭・四肢(全般コンボ)
                             "SPINERIBCD1"               => VARS::post("SPINERIBCD1"),                //脊柱・胸郭・四肢(疾患1コンボ)
                             "SPINERIBCD2"               => VARS::post("SPINERIBCD2"),                //脊柱・胸郭・四肢(疾患2コンボ)
                             "SPINERIBCD3"               => VARS::post("SPINERIBCD3"),                //脊柱・胸郭・四肢(疾患3コンボ)
                             "SPINERIBCD_REMARK"         => VARS::post("SPINERIBCD_REMARK"),          //脊柱・胸郭・四肢(全般テキスト)
                             "SPINERIBCD_REMARK1"        => VARS::post("SPINERIBCD_REMARK1"),         //脊柱・胸郭・四肢(疾患1テキスト)
                             "SPINERIBCD_REMARK2"        => VARS::post("SPINERIBCD_REMARK2"),         //脊柱・胸郭・四肢(疾患2テキスト)
                             "SPINERIBCD_REMARK3"        => VARS::post("SPINERIBCD_REMARK3"),         //脊柱・胸郭・四肢(疾患3テキスト)
                             "EYEDISEASECD"              => VARS::post("EYEDISEASECD"),               //目の疾病及び異常(コンボ)
                             "EYEDISEASECD2"             => VARS::post("EYEDISEASECD2"),              //目の疾病及び異常
                             "EYEDISEASECD3"             => VARS::post("EYEDISEASECD3"),              //目の疾病及び異常
                             "EYEDISEASECD4"             => VARS::post("EYEDISEASECD4"),              //目の疾病及び異常
                             "EYEDISEASECD5"             => VARS::post("EYEDISEASECD5"),              //色覚異常(コンボ)
                             "NOSEDISEASECD"             => VARS::post("NOSEDISEASECD"),              //耳鼻咽頭疾患(全般コンボ)
                             "NOSEDISEASECD2"            => VARS::post("NOSEDISEASECD2"),             //耳鼻咽頭疾患(疾患2コンボ)
                             "NOSEDISEASECD3"            => VARS::post("NOSEDISEASECD3"),             //耳鼻咽頭疾患(疾患3コンボ)
                             "NOSEDISEASECD4"            => VARS::post("NOSEDISEASECD4"),             //耳鼻咽頭疾患(コンボ)
                             "NOSEDISEASECD5"            => VARS::post("NOSEDISEASECD5"),             //耳鼻咽頭疾患(コンボ)
                             "NOSEDISEASECD6"            => VARS::post("NOSEDISEASECD6"),             //耳鼻咽頭疾患(コンボ)
                             "NOSEDISEASECD7"            => VARS::post("NOSEDISEASECD7"),             //耳鼻咽頭疾患(コンボ)
                             "NOSEDISEASECD_REMARK"      => VARS::post("NOSEDISEASECD_REMARK"),       //耳鼻咽頭疾患(全般テキスト)
                             "NOSEDISEASECD_REMARK1"     => VARS::post("NOSEDISEASECD_REMARK1"),      //耳鼻咽頭疾患1(疾患1テキスト)
                             "NOSEDISEASECD_REMARK2"     => VARS::post("NOSEDISEASECD_REMARK2"),      //耳鼻咽頭疾患2(疾患2テキスト)
                             "NOSEDISEASECD_REMARK3"     => VARS::post("NOSEDISEASECD_REMARK3"),      //耳鼻咽頭疾患3(疾患3テキスト)
                             "SKINDISEASECD"             => VARS::post("SKINDISEASECD"),              //皮膚疾患
                             "SKINDISEASECD_REMARK"      => VARS::post("SKINDISEASECD_REMARK"),         //皮膚疾患(テキスト)
                             "HEART_MEDEXAM"             => VARS::post("HEART_MEDEXAM"),              //心臓・臨床医学的検査(コンボ)
                             "HEART_MEDEXAM_REMARK"      => VARS::post("HEART_MEDEXAM_REMARK"),       //心臓・臨床医学的検査(テキスト)
                             "HEART_GRAPH_NO"            => VARS::post("HEART_GRAPH_NO"),             //心臓・臨床医学的検査(心電図No)
                             "MANAGEMENT_DIV"            => VARS::post("MANAGEMENT_DIV"),             //心臓・精密検査(コンボ)
                             "MANAGEMENT_REMARK"         => VARS::post("MANAGEMENT_REMARK"),          //心臓・臨床医学的検査(コンボ)
                             "HEARTDISEASECD"            => VARS::post("HEARTDISEASECD"),             //心臓・疾病及び異常(コンボ)
                             "HEARTDISEASECD_REMARK"     => VARS::post("HEARTDISEASECD_REMARK"),      //心臓・疾病及び異常(テキスト)
                             "TB_FILMDATE"               => VARS::post("TB_FILMDATE"),                //結核・撮影日付
                             "TB_RE_EXAMINATION_DATE"    => VARS::post("TB_RE_EXAMINATION_DATE"),     //結核・再検査(検査日)
                             "TB_RE_EXAMINATION_RESULT"  => VARS::post("TB_RE_EXAMINATION_RESULT"),   //結核・再検査(結果)
                             "TB_FILMNO"                 => VARS::post("TB_FILMNO"),                  //結核・フィルム番号
                             "TB_RE_EXAMINATION_FILMNO"  => VARS::post("TB_RE_EXAMINATION_FILMNO"),   //結核・再検査フィルム番号
                             "TB_REMARKCD"               => VARS::post("TB_REMARKCD"),                //結核・所見(コンボ)
                             "TB_X_RAY"                  => VARS::post("TB_X_RAY"),                   //結核・所見(テキスト)
                             "TB_OTHERTESTCD"            => VARS::post("TB_OTHERTESTCD"),             //結核・その他検査(コンボ)
                             "TB_OTHERTEST_REMARK1"      => VARS::post("TB_OTHERTEST_REMARK1"),       //結核・その他検査
                             "TB_NAMECD"                 => VARS::post("TB_NAMECD"),                  //結核・疾病及び異常(コンボ)
                             "TB_NAME_REMARK1"           => VARS::post("TB_NAME_REMARK1"),            //結核・疾病及び異常(テキスト)
                             "TB_ADVISECD"               => VARS::post("TB_ADVISECD"),                //結核・指導区分(コンボ)
                             "TB_ADVISE_REMARK1"         => VARS::post("TB_ADVISE_REMARK1"),          //結核・指導区分
                             "PARASITE"                  => VARS::post("PARASITE"),                   //寄生虫卵
                             "OTHERDISEASECD"            => VARS::post("OTHERDISEASECD"),             //その他疾病及び異常(コンボ)
                             "OTHER_ADVISECD"            => VARS::post("OTHER_ADVISECD"),             //その他疾病及び異常・指導区分(コンボ)
                             "OTHER_REMARK"              => VARS::post("OTHER_REMARK"),               //その他疾病及び異常(テキスト)
                             "OTHER_REMARK2"             => VARS::post("OTHER_REMARK2"),              //その他疾病及び異常2(テキスト)
                             "OTHER_REMARK3"             => VARS::post("OTHER_REMARK3"),              //その他疾病及び異常3(テキスト)
                             "DOC_CD"                    => VARS::post("DOC_CD"),                     //学校医・所見(コンボ)
                             "ANEMIA_REMARK"             => VARS::post("ANEMIA_REMARK"),              //貧血(所見)
                             "HEMOGLOBIN"                => VARS::post("HEMOGLOBIN"),                 //貧血(ヘモグロビン)
                             "DOC_REMARK"                => VARS::post("DOC_REMARK"),                 //学校医・所見(テキスト)
                             "DOC_DATE"                  => VARS::post("DOC_DATE"),                   //学校医・所見日付
                             "DOC_NAME"                  => VARS::post("DOC_NAME"),                   //学校医・氏名(三重のみ)
                             "TREATCD"                   => VARS::post("TREATCD"),                    //事後措置1(コンボ)
                             "TREATCD2"                  => VARS::post("TREATCD2"),                   //事後措置2(コンボ)
                             "TREAT_REMARK1"             => VARS::post("TREAT_REMARK1"),              //事後措置1(テキスト)
                             "TREATCD2_REMARK1"          => VARS::post("TREATCD2_REMARK1"),           //事後措置2(テキスト)
                             "TREAT_REMARK3"             => VARS::post("TREAT_REMARK3"),              //事後措置3
                             "REMARK"                    => VARS::post("REMARK"),                     //連絡欄
                             "MESSAGE"                   => VARS::post("MESSAGE"),                    //メッセージ
                             "GUIDE_DIV"                 => VARS::post("GUIDE_DIV"),                  //運動/指導区分
                             "JOINING_SPORTS_CLUB"       => VARS::post("JOINING_SPORTS_CLUB"),        //運動/部活動
                             "MEDICAL_HISTORY1"          => VARS::post("MEDICAL_HISTORY1"),           //既往症1
                             "MEDICAL_HISTORY2"          => VARS::post("MEDICAL_HISTORY2"),           //既往症2
                             "MEDICAL_HISTORY3"          => VARS::post("MEDICAL_HISTORY3"),           //既往症3
                             "DIAGNOSIS_NAME"            => VARS::post("DIAGNOSIS_NAME")              //診断名
                            );
        $this->year = CTRL_YEAR;
        $this->semester= CTRL_SEMESTER;

        $new = $new2 = $base = "";
        if ($this->Properties["printKenkouSindanIppan"] == "1") {
            $new = "1";
        } elseif ($this->Properties["printKenkouSindanIppan"] == "2" || $this->Properties["printKenkouSindanIppan"] == "3") {
            $new2 = "1";
        } else {
            $base = "1";
        }

        //結核画像番号サイズ
        $this->tb_filmnoFieldSize = ($this->Properties["usetb_filmnoFieldSize"] > 0) ? $this->Properties["usetb_filmnoFieldSize"] : 6;

        //一括更新処理処理1
        if ($this->cmd == "replace_update1") {
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["head_field"]  = $this->replace_data["det_field"] = array();
            $this->replace_data["head_check"]  = array(VARS::post("RCHECK0"));
            $this->replace_data["det_check"] = array();
            $cnt =13;
            for ($i = 1; $i < $cnt; $i++) {
                $this->replace_data["det_check"][]  = VARS::post("RCHECK".$i);
            }
            $this->replace_data["check_all"] = VARS::post("RCHECK_ALL");
            $this->replace_data["head_field"]["DATE"] = "";

            if (VARS::post("RCHECK0") == "1") {
                $this->replace_data["head_field"]["DATE"]                = VARS::post("DATE");
            }
            if (VARS::post("RCHECK1") == "1") {
                $this->replace_data["det_field"]["R_BAREVISION_MARK"]    = VARS::post("R_BAREVISION_MARK");
            }
            if (VARS::post("RCHECK1") == "1") {
                $this->replace_data["det_field"]["R_VISION_CANTMEASURE"] = VARS::post("R_VISION_MARK_CANTMEASURE");
            }
            if (VARS::post("RCHECK2") == "1") {
                $this->replace_data["det_field"]["R_VISION_MARK"]        = VARS::post("R_VISION_MARK");
            }
            if (VARS::post("RCHECK3") == "1") {
                $this->replace_data["det_field"]["L_BAREVISION_MARK"]    = VARS::post("L_BAREVISION_MARK");
            }
            if (VARS::post("RCHECK3") == "1") {
                $this->replace_data["det_field"]["L_VISION_CANTMEASURE"] = VARS::post("L_VISION_MARK_CANTMEASURE");
            }
            if (VARS::post("RCHECK4") == "1") {
                $this->replace_data["det_field"]["L_VISION_MARK"]        = VARS::post("L_VISION_MARK");
            }
            if (VARS::post("RCHECK5") == "1") {
                $this->replace_data["det_field"]["R_BAREVISION"]         = VARS::post("R_BAREVISION");
            }
            if (VARS::post("RCHECK5") == "1") {
                $this->replace_data["det_field"]["R_VISION_CANTMEASURE"] = VARS::post("R_VISION_CANTMEASURE");
            }
            if (VARS::post("RCHECK6") == "1") {
                $this->replace_data["det_field"]["R_VISION"]             = VARS::post("R_VISION");
            }
            if (VARS::post("RCHECK7") == "1") {
                $this->replace_data["det_field"]["L_BAREVISION"]         = VARS::post("L_BAREVISION");
            }
            if (VARS::post("RCHECK7") == "1") {
                $this->replace_data["det_field"]["L_VISION_CANTMEASURE"] = VARS::post("L_VISION_CANTMEASURE");
            }
            if (VARS::post("RCHECK8") == "1") {
                $this->replace_data["det_field"]["L_VISION"]             = VARS::post("L_VISION");
            }
            if (VARS::post("RCHECK9") == "1") {
                $this->replace_data["det_field"]["R_EAR_DB"]             = VARS::post("R_EAR_DB");
            }
            if (VARS::post("RCHECK9") == "1") {
                $this->replace_data["det_field"]["R_EAR_CANTMEASURE"]    = VARS::post("R_EAR_CANTMEASURE");
            }
            if (VARS::post("RCHECK10") == "1") {
                $this->replace_data["det_field"]["R_EAR"]                = VARS::post("R_EAR");
            }
            if (VARS::post("RCHECK11") == "1") {
                $this->replace_data["det_field"]["L_EAR_DB"]             = VARS::post("L_EAR_DB");
            }
            if (VARS::post("RCHECK11") == "1") {
                $this->replace_data["det_field"]["L_EAR_CANTMEASURE"]    = VARS::post("L_EAR_CANTMEASURE");
            }
            if (VARS::post("RCHECK12") == "1") {
                $this->replace_data["det_field"]["L_EAR"]                = VARS::post("L_EAR");
            }
        }
        //一括更新処理処理2
        if ($this->cmd == "replace_update2") {
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["head_field"]  = $this->replace_data["det_field"] = array();
            $this->replace_data["head_check"]  = array(VARS::post("RCHECK0"));
            $this->replace_data["det_check"]   = array(VARS::post("RCHECK1"),
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
                                                       VARS::post("RCHECK17"),
                                                       VARS::post("RCHECK18"),
                                                       VARS::post("RCHECK19"),
                                                       VARS::post("RCHECK20"),
                                                       VARS::post("RCHECK21"));

            $this->replace_data["check_all"] = VARS::post("RCHECK22");
            $this->replace_data["head_field"]["DATE"] = "";
            if (VARS::post("RCHECK1") == "1") {
                $this->replace_data["det_field"]["NUTRITIONCD"]              = VARS::post("NUTRITIONCD");
                $this->replace_data["det_field"]["NUTRITIONCD_REMARK"]       = VARS::post("NUTRITIONCD_REMARK");
            }
            if (VARS::post("RCHECK2") == "1") {
                $this->replace_data["det_field"]["SPINERIBCD"]               = VARS::post("SPINERIBCD");
                $this->replace_data["det_field"]["SPINERIBCD_REMARK"]        = VARS::post("SPINERIBCD_REMARK");
            }
            if (VARS::post("RCHECK3") == "1") {
                $this->replace_data["det_field"]["SPINERIBCD1"]              = VARS::post("SPINERIBCD1");
                $this->replace_data["det_field"]["SPINERIBCD_REMARK1"]       = VARS::post("SPINERIBCD_REMARK1");
            }
            if (VARS::post("RCHECK4") == "1") {
                $this->replace_data["det_field"]["SPINERIBCD2"]              = VARS::post("SPINERIBCD2");
                $this->replace_data["det_field"]["SPINERIBCD_REMARK2"]       = VARS::post("SPINERIBCD_REMARK2");
            }
            if (VARS::post("RCHECK5") == "1") {
                $this->replace_data["det_field"]["SPINERIBCD3"]              = VARS::post("SPINERIBCD3");
                $this->replace_data["det_field"]["SPINERIBCD_REMARK3"]       = VARS::post("SPINERIBCD_REMARK3");
            }
            if (VARS::post("RCHECK6") == "1") {
                $this->replace_data["det_field"]["EYEDISEASECD"]             = VARS::post("EYEDISEASECD");
                $this->replace_data["det_field"]["EYE_TEST_RESULT"]          = VARS::post("EYE_TEST_RESULT");
            }
            if (VARS::post("RCHECK7") == "1") {
                $this->replace_data["det_field"]["EYEDISEASECD5"]            = VARS::post("EYEDISEASECD5");
                $this->replace_data["det_field"]["VISION_CANTMEASURE"]       = VARS::post("VISION_CANTMEASURE");
            }
            if (VARS::post("RCHECK8") == "1") {
                $this->replace_data["det_field"]["NOSEDISEASECD"]            = VARS::post("NOSEDISEASECD");
                $this->replace_data["det_field"]["NOSEDISEASECD_REMARK"]     = VARS::post("NOSEDISEASECD_REMARK");
            }
            if (VARS::post("RCHECK9") == "1") {
                $this->replace_data["det_field"]["NOSEDISEASECD5"]           = VARS::post("NOSEDISEASECD5");
                $this->replace_data["det_field"]["NOSEDISEASECD_REMARK1"]    = VARS::post("NOSEDISEASECD_REMARK1");
            }
            if (VARS::post("RCHECK10") == "1") {
                $this->replace_data["det_field"]["NOSEDISEASECD6"]           = VARS::post("NOSEDISEASECD6");
                $this->replace_data["det_field"]["NOSEDISEASECD_REMARK2"]    = VARS::post("NOSEDISEASECD_REMARK2");
            }
            if (VARS::post("RCHECK11") == "1") {
                $this->replace_data["det_field"]["NOSEDISEASECD7"]           = VARS::post("NOSEDISEASECD7");
                $this->replace_data["det_field"]["NOSEDISEASECD_REMARK3"]    = VARS::post("NOSEDISEASECD_REMARK3");
            }
            if (VARS::post("RCHECK12") == "1") {
                $this->replace_data["det_field"]["SKINDISEASECD"]            = VARS::post("SKINDISEASECD");
                $this->replace_data["det_field"]["SKINDISEASECD_REMARK"]     = VARS::post("SKINDISEASECD_REMARK");
            }
            if (VARS::post("RCHECK13") == "1") {
                $this->replace_data["det_field"]["TB_FILMDATE"]              = str_replace("/", "-", VARS::post("TB_FILMDATE"));
            }
            if (VARS::post("RCHECK14") == "1") {
                $this->replace_data["det_field"]["TB_REMARKCD"]              = VARS::post("TB_REMARKCD");
                $this->replace_data["det_field"]["TB_X_RAY"]                 = VARS::post("TB_X_RAY");
            }
            if (VARS::post("RCHECK15") == "1") {
                $this->replace_data["det_field"]["TB_RE_EXAMINATION_DATE"]   = str_replace("/", "-", VARS::post("TB_RE_EXAMINATION_DATE"));
                $this->replace_data["det_field"]["TB_RE_EXAMINATION_RESULT"] = VARS::post("TB_RE_EXAMINATION_RESULT");
            }
            if (VARS::post("RCHECK16") == "1") {
                $this->replace_data["det_field"]["TB_OTHERTESTCD"]           = VARS::post("TB_OTHERTESTCD");
            }
            if (VARS::post("RCHECK17") == "1") {
                $this->replace_data["det_field"]["TB_NAMECD"]                = VARS::post("TB_NAMECD");
                $this->replace_data["det_field"]["TB_NAME_REMARK1"]          = VARS::post("TB_NAME_REMARK1");
            }
            if (VARS::post("RCHECK18") == "1") {
                $this->replace_data["det_field"]["TB_ADVISECD"]              = VARS::post("TB_ADVISECD");
            }
            if (VARS::post("RCHECK19") == "1") {
                $this->replace_data["det_field"]["HEART_MEDEXAM"]            = VARS::post("HEART_MEDEXAM");
                $this->replace_data["det_field"]["HEART_MEDEXAM_REMARK"]     = VARS::post("HEART_MEDEXAM_REMARK");
            }
            if (VARS::post("RCHECK20") == "1") {
                $this->replace_data["det_field"]["MANAGEMENT_DIV"]           = VARS::post("MANAGEMENT_DIV");
                $this->replace_data["det_field"]["MANAGEMENT_REMARK"]        = VARS::post("MANAGEMENT_REMARK");
            }
            if (VARS::post("RCHECK21") == "1") {
                $this->replace_data["det_field"]["HEARTDISEASECD"]           = VARS::post("HEARTDISEASECD");
                $this->replace_data["det_field"]["HEARTDISEASECD_REMARK"]    = VARS::post("HEARTDISEASECD_REMARK");
            }
        }
        //一括更新処理処理3
        if ($this->cmd == "replace_update3") {
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["head_field"]  = $this->replace_data["det_field"] = array();
            $this->replace_data["head_check"]  = array(VARS::post("RCHECK0"));
            $this->replace_data["det_check"]   = array(VARS::post("RCHECK2"),
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
                                                       VARS::post("RCHECK17"));

            $this->replace_data["check_all"] = VARS::post("RCHECK1");
            $this->replace_data["head_field"]["DATE"] = "";
            if (VARS::post("RCHECK2")) {
                $this->replace_data["det_field"]["ALBUMINURIA1CD"]       = VARS::post("ALBUMINURIA1CD");
            }
            if (VARS::post("RCHECK3")) {
                $this->replace_data["det_field"]["URICSUGAR1CD"]         = VARS::post("URICSUGAR1CD");
            }
            if (VARS::post("RCHECK4")) {
                $this->replace_data["det_field"]["URICBLEED1CD"]         = VARS::post("URICBLEED1CD");
            }
            if (VARS::post("RCHECK5")) {
                $this->replace_data["det_field"]["ALBUMINURIA2CD"]       = VARS::post("ALBUMINURIA2CD");
            }
            if (VARS::post("RCHECK6")) {
                $this->replace_data["det_field"]["URICSUGAR2CD"]         = VARS::post("URICSUGAR2CD");
            }
            if (VARS::post("RCHECK7")) {
                $this->replace_data["det_field"]["URICBLEED2CD"]         = VARS::post("URICBLEED2CD");
            }
            if (VARS::post("RCHECK8")) {
                $this->replace_data["det_field"]["DETAILED_EXAMINATION"] = VARS::post("DETAILED_EXAMINATION");
            }
            if (VARS::post("RCHECK9")) {
                $this->replace_data["det_field"]["URI_ADVISECD"]         = VARS::post("URI_ADVISECD");
            }
            if (VARS::post("RCHECK10")) {
                $this->replace_data["det_field"]["URICOTHERTEST"]        = VARS::post("URICOTHERTEST");
            }
            if (VARS::post("RCHECK11")) {
                $this->replace_data["det_field"]["OTHER_REMARK"]         = VARS::post("OTHER_REMARK");
            }
            if (VARS::post("RCHECK12")) {
                $this->replace_data["det_field"]["DOC_DATE"]             = str_replace("/", "-", VARS::post("DOC_DATE"));
            }
            if (VARS::post("RCHECK13")) {
                $this->replace_data["det_field"]["DOC_REMARK"]           = VARS::post("DOC_REMARK");
            }
            if (VARS::post("RCHECK14")) {
                $this->replace_data["det_field"]["TREATCD"]              = VARS::post("TREATCD");
                $this->replace_data["det_field"]["TREAT_REMARK1"]        = VARS::post("TREAT_REMARK1");
            }
            if (VARS::post("RCHECK15")) {
                $this->replace_data["det_field"]["TREATCD2"]             = VARS::post("TREATCD2");
                $this->replace_data["det_field"]["TREATCD2_REMARK1"]     = VARS::post("TREATCD2_REMARK1");
            }
            if (VARS::post("RCHECK16")) {
                $this->replace_data["det_field"]["REMARK"]               = VARS::post("REMARK");
            }
        }
        //一括更新処理処理4
        if ($this->cmd == "replace_update4") {
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["head_field"]  = $this->replace_data["det_field"] = array();
            $this->replace_data["head_check"]  = array(VARS::post("RCHECK0"));
            $this->replace_data["det_check"]   = array(VARS::post("RCHECK1"),
                                                       VARS::post("RCHECK2"),
                                                       VARS::post("RCHECK3"),
                                                       VARS::post("RCHECK4"));
            $this->replace_data["check_all"] = VARS::post("RCHECK5");
            $this->replace_data["head_field"]["DATE"] = "";
            if (VARS::post("RCHECK1") == "1") {
                $this->replace_data["det_field"]["MEDICAL_HISTORY1"] = VARS::post("MEDICAL_HISTORY1");
            }
            if (VARS::post("RCHECK2") == "1") {
                $this->replace_data["det_field"]["MEDICAL_HISTORY2"] = VARS::post("MEDICAL_HISTORY2");
            }
            if (VARS::post("RCHECK3") == "1") {
                $this->replace_data["det_field"]["MEDICAL_HISTORY3"] = VARS::post("MEDICAL_HISTORY3");
            }
            if (VARS::post("RCHECK4") == "1") {
                $this->replace_data["det_field"]["DIAGNOSIS_NAME"]   = VARS::post("DIAGNOSIS_NAME");
            }
        }
        if (VARS::get("ini2")) {
            unset($this->replace_data);
        }

        //生徒項目名切替処理
        $schName = "";
        $db = Query::dbCheckOut();
        //校種取得
        if (VARS::get("GRADE")) {
            $this->getGrade = VARS::get("GRADE");
        }
        $query = knjf010jQuery::getSchoolKind($this->getGrade);
        $this->getSchKind = $db->getOne($query);
        //テーブルの有無チェック
        $query = knjf010jQuery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && ($this->schregno || ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""))) {
            //生徒項目名取得
            $schName = $db->getOne(knjf010jQuery::getSchName($this));
        }
        $this->sch_label = (strlen($schName) > 0) ? $schName : '生徒';
        Query::dbCheckIn($db);
    }
    //入力項目チェック
    public function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG308");
            return false;
        }
        //健康診断実施日が入力されていない場合現在処理日付(ATTEND_CTRL_DATE)を代入
        if ($this->field["DATE"] == "") {
            if (VARS::post("HIDDENDATE")) {
                $this->field["DATE"] = VARS::post("HIDDENDATE");
            } else {
                $this->field["DATE"] = CTRL_DATE;
            }
        }
        $height = explode(".", $this->field["HEIGHT"]);
        $weight = explode(".", $this->field["WEIGHT"]);
        $sitheight = explode(".", $this->field["SITHEIGHT"]);
        if (strlen($height[0]) > 3) {
            $this->setWarning("MSG901", "(身長)");
            return false;
        } elseif (strlen($height[1])>1) {
            $this->setWarning("MSG901", "(身長) 小数点第一位まで");
            return false;
        }
        if (strlen($weight[0]) > 3) {
            $this->setWarning("MSG901", "(体重)");
            return false;
        } elseif (strlen($weight[1]) > 1) {
            $this->setWarning("MSG901", "(体重) 小数点第一位まで");
            return false;
        }
        if (strlen($sitheight[0]) > 3) {
            $this->setWarning("MSG901", "(座高)");
            return false;
        } elseif (strlen($sitheight[1]) > 1) {
            $this->setWarning("MSG901", "(座高) 小数点第一位まで");
            return false;
        }

        $r_barevision = explode(".", $this->field["R_BAREVISION"]);
        $r_vision     = explode(".", $this->field["R_VISION"]);
        $l_barevision = explode(".", $this->field["L_BAREVISION"]);
        $l_vision     = explode(".", $this->field["L_VISION"]);
        if (strlen($r_barevision[0])>1) {
            $this->setWarning("MSG901", "(視力・右裸眼)");
            return false;
        } elseif (strlen($r_barevision[1]) > 3) {
            $this->setWarning("MSG901", "(視力・右裸眼) 小数点第３位まで");
            return false;
        }
        if (strlen($r_vision[0])>1) {
            $this->setWarning("MSG901", "(視力・右矯正)");
            return false;
        } elseif (strlen($r_vision[1]) > 3) {
            $this->setWarning("MSG901", "(視力・右矯正) 小数点第３位まで");
            return false;
        }
        if (strlen($l_barevision[0])>1) {
            $this->setWarning("MSG901", "(視力・左裸眼)");
            return false;
        } elseif (strlen($l_barevision[1]) > 3) {
            $this->setWarning("MSG901", "(視力・左裸眼) 小数点第３位まで");
            return false;
        }
        if (strlen($l_vision[0]) > 1) {
            $this->setWarning("MSG901", "(視力・左矯正)");
            return false;
        } elseif (strlen($l_vision[1]) > 3) {
            $this->setWarning("MSG901", "(視力・左矯正) 小数点第３位まで");
            return false;
        }

        if ($this->field["R_EAR_DB"] > 999) {
            $this->setWarning("MSG915", "聴力・右DBは半角数字で999までです。");
            return false;
        }
        if ($this->isKoma) {
            if ($this->field["R_EAR_DB_1000"] > 999) {
                $this->setWarning("MSG915", "聴力・右1000Hzは半角数字で999までです。");
                return false;
            }
        }
        if ($this->field["R_EAR_DB_4000"] > 999) {
            $this->setWarning("MSG915", "聴力・右4000Hzは半角数字で999までです。");
            return false;
        }
        if ($this->field["L_EAR_DB"] > 999) {
            $this->setWarning("MSG915", "聴力・左DBは半角数字で999までです。");
            return false;
        }
        if ($this->isKoma) {
            if ($this->field["L_EAR_DB_1000"] > 999) {
                $this->setWarning("MSG915", "聴力・左1000Hzは半角数字で999までです。");
                return false;
            }
        }
        if ($this->field["L_EAR_DB_4000"] > 999) {
            $this->setWarning("MSG915", "聴力・左4000Hzは半角数字で999までです。");
            return false;
        }
        if (strlen($this->field["NUTRITIONCD_REMARK"]) > 60) {
            $this->setWarning("MSG915", "栄養状態は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["SPINERIBCD_REMARK"]) > 60) {
            $this->setWarning("MSG915", "脊柱・胸郭・四肢(全般)は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["SPINERIBCD_REMARK1"]) > 60) {
            $this->setWarning("MSG915", "脊柱・胸郭・四肢(疾患1)は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["SPINERIBCD_REMARK2"]) > 60) {
            $this->setWarning("MSG915", "脊柱・胸郭・四肢(疾患2)は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["SPINERIBCD_REMARK3"]) > 60) {
            $this->setWarning("MSG915", "脊柱・胸郭・四肢(疾患3)は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["EYE_TEST_RESULT"]) > 60) {
            $this->setWarning("MSG915", "目の疾病及び異常は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["VISION_CANTMEASURE"]) > 60) {
            $this->setWarning("MSG915", "色覚異常は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NOSEDISEASECD_REMARK"]) > 60) {
            $this->setWarning("MSG915", "耳鼻咽頭疾患(全般)は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NOSEDISEASECD_REMARK1"]) > 60) {
            $this->setWarning("MSG915", "耳鼻咽頭疾患(疾患1)は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NOSEDISEASECD_REMARK2"]) > 60) {
            $this->setWarning("MSG915", "耳鼻咽頭疾患(疾患2)は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NOSEDISEASECD_REMARK3"]) > 60) {
            $this->setWarning("MSG915", "耳鼻咽頭疾患(疾患3)は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["SKINDISEASECD_REMARK"]) > 60) {
            $this->setWarning("MSG915", "皮膚疾患は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["URICOTHERTEST"]) > 60) {
            $this->setWarning("MSG915", "精密検査・所見は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["det_field"]["EYE_TEST_RESULT"]) > 60) {
            $this->setWarning("MSG915", "眼科検診結果は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["det_field"]["EYE_TEST_RESULT2"]) > 60) {
            $this->setWarning("MSG915", "眼科検診結果2は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["det_field"]["EYE_TEST_RESULT3"]) > 60) {
            $this->setWarning("MSG915", "眼科検診結果3は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["HEART_MEDEXAM_REMARK"]) > 120) {
            $this->setWarning("MSG915", "心臓臨床医学的検査は120バイトまでです。");
            return false;
        }
        if ($this->isKoma) {
            if (strlen($this->field["HEART_GRAPH_NO"]) > 12) {
                $this->setWarning("MSG915", "心電図Noは12バイトまでです。");
                return false;
            }
        }
        if (strlen($this->field["MANAGEMENT_REMARK"]) > 120) {
            $this->setWarning("MSG915", "心臓(精密検査)は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["TB_FILMNO"]) > $this->tb_filmnoFieldSize) {
            $this->setWarning("MSG915", "結核(X線撮影・画像番号)は".$this->tb_filmnoFieldSize ."バイトまでです。");
            return false;
        }
        if (strlen($this->field["TB_X_RAY"]) > 60) {
            $this->setWarning("MSG915", "結核(所見)は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["TB_RE_EXAMINATION_FILMNO"]) > $this->tb_filmnoFieldSize) {
            $this->setWarning("MSG915", "結核(再検査・画像番号)は".$this->tb_filmnoFieldSize ."バイトまでです。");
            return false;
        }
        if (strlen($this->field["TB_NAME_REMARK1"]) > 60) {
            $this->setWarning("MSG915", "結核(疾病及び異常)は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["TB_NAME_REMARK1"]) > 60) {
            $this->setWarning("MSG915", "結核(疾病及び異常)は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["HEARTDISEASECD_REMARK"]) > 60) {
            $this->setWarning("MSG915", "心臓(疾病及び異常)は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["OTHER_REMARK"]) > 60) {
            $this->setWarning("MSG915", "その他疾病及び異常・所見は60バイトまでです。");
            return false;
        }
        if ($this->Properties["printKenkouSindanIppan"] == "2") {
            if (strlen($this->field["OTHER_REMARK2"]) > $this->maxOtherRemark2Byte) {
                $this->setWarning("MSG915", "その他疾病及び異常・所見3は{$this->maxOtherRemark2Byte}バイトまでです。");
                return false;
            }
        } else {
            if (strlen($this->field["OTHER_REMARK2"]) > $this->maxOtherRemark2Byte) {
                $this->setWarning("MSG915", "その他疾病及び異常・所見は{$this->maxOtherRemark2Byte}バイトまでです。");
                return false;
            }
        }
        if (strlen($this->field["OTHER_REMARK3"]) > 60) {
            $this->setWarning("MSG915", "その他疾病及び異常・所見2は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["ANEMIA_REMARK"]) > 30) {
            $this->setWarning("MSG915", "貧血・所見は30バイトまでです。");
            return false;
        }
        $hemog = explode(".", $this->field["HEMOGLOBIN"]);
        if (strlen($hemog[0]) > 2) {
            $this->setWarning("MSG901", "(ヘモグロビン)");
            return false;
        } elseif (strlen($hemog[1]) > 1) {
            $this->setWarning("MSG901", "(ヘモグロビン) 小数点第一位まで");
            return false;
        }
        if (strlen($this->field["DOC_REMARK"]) > 60) {
            $this->setWarning("MSG915", "学校医・所見は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["TREAT_REMARK1"]) > 60) {
            $this->setWarning("MSG915", "事後措置1・は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["TREATCD2_REMARK1"]) > 60) {
            $this->setWarning("MSG915", "事後措置2・は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK"]) > $this->maxRemarkByte) {
            $this->setWarning("MSG915", "備考は{$this->maxRemarkByte}バイトまでです。");
            return false;
        }
        if ($this->validate_row_cnt($this->field["MESSAGE"], (21 * 2)) > 4) {
            $this->setWarning("MSG915", "(メッセージは4行までです)");
            return false;
        }
        if (strlen($this->field["DIAGNOSIS_NAME"]) > 150) {
            $this->setWarning("MSG915", "診断名は150バイトまでです。");
            return false;
        }

        $this->field["DATE"] = str_replace("/", "-", $this->field["DATE"]);
        $this->field["TB_FILMDATE"] = str_replace("/", "-", $this->field["TB_FILMDATE"]);
        $this->field["DOC_DATE"] = str_replace("/", "-", $this->field["DOC_DATE"]);

        if ($this->isFukui) {
            if (strlen($r_barevision[0]) > 0) {
                $this->field["R_BAREVISION"] = $r_barevision[0] . "." . sprintf("%-03s", $r_barevision[1]);
            }
            if (strlen($r_vision[0]) > 0) {
                $this->field["R_VISION"] = $r_vision[0] . "." . sprintf("%-03s", $r_vision[1]);
            }
            if (strlen($l_barevision[0]) > 0) {
                $this->field["L_BAREVISION"] = $l_barevision[0] . "." . sprintf("%-03s", $l_barevision[1]);
            }
            if (strlen($l_vision[0]) > 0) {
                $this->field["L_VISION"] = $l_vision[0] . "." . sprintf("%-03s", $l_vision[1]);
            }
        }
        if (strlen($hemog[0]) > 0) {
            $this->field["HEMOGLOBIN"] = $hemog[0] . "." . sprintf("%-01s", $hemog[1]);
        }

        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }

        $this->checkVisionMark($this->field);//視力の文字をチェック
        $this->update();
        $this->setMessage("MSG201");
    }

    public function checkVisionMark($fieldArray)
    {
        $visions = array('R_BAREVISION', 'R_VISION', 'L_BAREVISION', 'L_VISION');
        foreach ($visions as $val) {
            $valMark = $val . "_MARK";
            if (!$fieldArray[$valMark] && $fieldArray[$val] != '') {
                $query = knjf010jQuery::getVisionMark($this);
                $db = Query::dbCheckOut();
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($fieldArray[$val] >= $row["NAMESPARE1"]) {
                        $fieldArray[$valMark] = $row["NAME1"];
                    } else {
                        break 1;
                    }
                }
                Query::dbCheckIn($db);
            }
        }
    }

    public function &update()
    {
        //レコードが存在するのかチェック
        $db = Query::dbCheckOut();
        $query = knjf010jQuery::checkHdat($this);
        $count = $db->getOne($query);
        Query::dbCheckIn($db);
        if ($count >= 1) {
            knjf010jQuery::getHdatUpdate($this);//あればMEDEXAM_HDATをアップデート
        } else {
            knjf010jQuery::getHdatInsert($this);//なければMEDEXAM_HDATにインサート
        }
        //年度及び学籍番号でMEDEXAM_DET_DATが存在する場合は更新(UPDATE)、存在しない場合(INSERT)する
        $row = knjf010jQuery::getMedexamDetDat($this);       //MEDEXAM_DET_DAT取得
        //データが存在しない場合
        if ($row == "") {
            $flag = "insert";
        }
        knjf010jQuery::getInsertUpdateQuery($this, $flag);
        return $result;
    }

    //削除処理
    public function &getDeleteModel()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG308");
            return false;
        }
        //年度及び学籍番号でMEDEXAM_DET_DATが存在する場合、削除(DELETE)
        $row = knjf010jQuery::getMedexamDetDat($this);       //MEDEXAM_DET_DAT取得
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

    public function &delete()
    {
        knjf010jQuery::getDeleteQuery($this);
        $row = knjf010jQuery::getMedexamToothDat($this);

        if ($row == "") { //MEDEXAM_TOOTH_DATが存在しない場合はMEDEXAM_HDATを削除
            knjf010jQuery::getHdatDeleteQuery($this);
        } else { //TOOTH_DATがあればMEDEXAM_HDATのDATEだけNULLにする
            knjf010jQuery::getHdatDateDeleteQuery($this);
        }
        return true;
    }

    //一括更新処理1
    public function &replaceModel1()
    {
        //生徒が選択されてない場合
        if (!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304");
            return false;
        }
        $r_barevision = explode(".", $this->replace_data["det_field"]["R_BAREVISION"]);
        $r_vision = explode(".", $this->replace_data["det_field"]["R_VISION"]);
        $l_barevision = explode(".", $this->replace_data["det_field"]["L_BAREVISION"]);
        $l_vision = explode(".", $this->replace_data["det_field"]["L_VISION"]);
        if (strlen($r_barevision[0])>1) {
            $this->setWarning("MSG901", "(視力・右裸眼)");
            return false;
        } elseif (strlen($r_barevision[1])>3) {
            $this->setWarning("MSG901", "(視力・右裸眼) 小数点第３位まで");
            return false;
        }
        if (strlen($r_vision[0])>1) {
            $this->setWarning("MSG901", "(視力・右矯正)");
            return false;
        } elseif (strlen($r_vision[1])>3) {
            $this->setWarning("MSG901", "(視力・右矯正) 小数点第３位まで");
            return false;
        }
        if (strlen($l_barevision[0])>1) {
            $this->setWarning("MSG901", "(視力・左裸眼)");
            return false;
        } elseif (strlen($l_barevision[1])>3) {
            $this->setWarning("MSG901", "(視力・左裸眼) 小数点第３位まで");
            return false;
        }
        if (strlen($l_vision[0])>1) {
            $this->setWarning("MSG901", "(視力・左矯正)");
            return false;
        } elseif (strlen($l_vision[1])>3) {
            $this->setWarning("MSG901", "(視力・左矯正) 小数点第３位まで");
            return false;
        }
        $r_ear_db = explode(".", $this->replace_data["det_field"]["R_EAR_DB"]);
        $l_ear_db = explode(".", $this->replace_data["det_field"]["L_EAR_DB"]);
        if (strlen($r_ear_db[1])>1) {
            $this->setWarning("MSG901", "(聴力・右・平均db) 小数点第1位まで");
            return false;
        }
        if (strlen($l_ear_db[1])>1) {
            $this->setWarning("MSG901", "(聴力・左・平均db) 小数点第1位まで");
            return false;
        }
        if ($this->replace_data["det_field"]["R_EAR_DB"] > 999) {
            $this->setWarning("MSG915", "聴力・右DBは半角数字で999までです。");
            return false;
        }
        if ($this->isKoma) {
            if ($this->replace_data["det_field"]["R_EAR_DB_1000"] > 999) {
                $this->setWarning("MSG915", "聴力・右1000Hzは半角数字で999までです。");
                return false;
            }
        }
        if ($this->replace_data["det_field"]["R_EAR_DB_4000"] > 999) {
            $this->setWarning("MSG915", "聴力・右4000Hzは半角数字で999までです。");
            return false;
        }
        if ($this->replace_data["det_field"]["L_EAR_DB"] > 999) {
            $this->setWarning("MSG915", "聴力・右DBは半角数字で999までです。");
            return false;
        }
        if ($this->isKoma) {
            if ($this->replace_data["det_field"]["L_EAR_DB_1000"] > 999) {
                $this->setWarning("MSG915", "聴力・右1000Hzは半角数字で999までです。");
                return false;
            }
        }
        if ($this->replace_data["det_field"]["L_EAR_DB_4000"] > 999) {
            $this->setWarning("MSG915", "聴力・右4000Hzは半角数字で999までです。");
            return false;
        }
        $this->replace_data["head_field"]["DATE"] = str_replace("/", "-", $this->replace_data["head_field"]["DATE"]);

        if ($this->isFukui) {
            if (strlen($r_barevision[0]) > 0) {
                $this->replace_data["det_field"]["R_BAREVISION"] = $r_barevision[0] . "." . sprintf("%-03s", $r_barevision[1]);
            }
            if (strlen($r_vision[0]) > 0) {
                $this->replace_data["det_field"]["R_VISION"] = $r_vision[0] . "." . sprintf("%-03s", $r_vision[1]);
            }
            if (strlen($l_barevision[0]) > 0) {
                $this->replace_data["det_field"]["L_BAREVISION"] = $l_barevision[0] . "." . sprintf("%-03s", $l_barevision[1]);
            }
            if (strlen($l_vision[0]) > 0) {
                $this->replace_data["det_field"]["L_VISION"] = $l_vision[0] . "." . sprintf("%-03s", $l_vision[1]);
            }
        }

        //最低ひとつでもチェックがついてる場合のみ更新
        if (!in_array("1", $this->replace_data["head_check"]) && !in_array("1", $this->replace_data["det_check"])) {
            return false;
        }

        $this->checkVisionMark($this->replace_data["det_field"]);//視力の文字をチェック
        if (in_array("1", $this->replace_data["det_check"]) || in_array("1", $this->replace_data["head_check"])) {
            $this->updateHead();
            $this->updateDet();
        }
        $this->setMessage("MSG201");
        //添え字を０から順序良くする
        $this->replace_data["check"] = array_merge((array)$this->replace_data["head_check"], (array)$this->replace_data["det_check"]);
        $this->replace_data["check"][] = $this->replace_data["check_all"];
    }
    //一括更新処理2
    public function &replaceModel2()
    {

        //添え字を０から順序良くする
        $this->replace_data["check"] = array_merge((array)$this->replace_data["head_check"], (array)$this->replace_data["det_check"]);
        $this->replace_data["check"][] = $this->replace_data["check_all"];

        //生徒が選択されてない場合
        if (!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["EYE_TEST_RESULT"]) > 60) {
            $this->setWarning("MSG915", "目の疾病及び異常は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["SPINERIBCD_REMARK"]) > 60) {
            $this->setWarning("MSG915", "脊柱・胸郭・四肢は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["SPINERIBCD_REMARK1"]) > 60) {
            $this->setWarning("MSG915", "脊柱・胸郭・四肢(疾患1)は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["SPINERIBCD_REMARK2"]) > 60) {
            $this->setWarning("MSG915", "脊柱・胸郭・四肢(疾患2)は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["SPINERIBCD_REMARK3"]) > 60) {
            $this->setWarning("MSG915", "脊柱・胸郭・四肢(疾患3)は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["VISION_CANTMEASURE"]) > 60) {
            $this->setWarning("MSG915", "色覚異常は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["NOSEDISEASECD_REMARK"]) > 60) {
            $this->setWarning("MSG915", "耳鼻咽頭疾患は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["NOSEDISEASECD_REMARK1"]) > 60) {
            $this->setWarning("MSG915", "耳鼻咽喉疾患(疾患1)は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["NOSEDISEASECD_REMARK2"]) > 60) {
            $this->setWarning("MSG915", "耳鼻咽喉疾患(疾患2)は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["NOSEDISEASECD_REMARK3"]) > 60) {
            $this->setWarning("MSG915", "耳鼻咽喉疾患(疾患3)は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["NUTRITIONCD_REMARK"]) > 60) {
            $this->setWarning("MSG915", "栄養状態は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["SKINDISEASECD_REMARK"]) > 60) {
            $this->setWarning("MSG915", "皮膚疾患は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["TB_X_RAY"]) > 60) {
            $this->setWarning("MSG915", "結核・検査(X線)は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["TB_NAME_REMARK1"]) > 60) {
            $this->setWarning("MSG915", "結核(疾病及び異常)は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["HEART_MEDEXAM_REMARK"]) > 120) {
            $this->setWarning("MSG915", "心臓(臨床医学的検査)は120バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["MANAGEMENT_REMARK"]) > 120) {
            $this->setWarning("MSG915", "心臓(精密検査)は120バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["HEARTDISEASECD_REMARK"]) > 60) {
            $this->setWarning("MSG915", "心臓・疾病及び異常は60バイトまでです。");
            return false;
        }
        //最低ひとつでもチェックがついてる場合のみ更新
        if (!in_array("1", $this->replace_data["det_check"])) {
            return false;
        }
        if (in_array("1", $this->replace_data["det_check"])) {
            $this->updateHead();
            $this->updateDet();
        }
        $this->setMessage("MSG201");
    }
    //一括更新処理3
    public function &replaceModel3()
    {
        //生徒が選択されてない場合
        if (!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["URICOTHERTEST"]) > 60) {
            $this->setWarning("MSG915", "尿・その他の検査は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["OTHER_REMARK"]) > 60) {
            $this->setWarning("MSG915", "その他疾病及び異常・所見は60バイトまでです。");
            return false;
        }
        if ($this->Properties["printKenkouSindanIppan"] == "2") {
            if (strlen($this->replace_data["det_field"]["OTHER_REMARK2"]) > $this->maxOtherRemark2Byte) {
                $this->setWarning("MSG915", "その他疾病及び異常・所見2は{$this->maxOtherRemark2Byte}バイトまでです。");
                return false;
            }
        } else {
            if (strlen($this->replace_data["det_field"]["OTHER_REMARK2"]) > $this->maxOtherRemark2Byte) {
                $this->setWarning("MSG915", "その他疾病及び異常・所見は{$this->maxOtherRemark2Byte}バイトまでです。");
                return false;
            }
        }
        if (strlen($this->replace_data["det_field"]["OTHER_REMARK3"]) > 60) {
            $this->setWarning("MSG915", "その他疾病及び異常・所見3は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["ANEMIA_REMARK"]) > 30) {
            $this->setWarning("MSG915", "貧血・所見は30バイトまでです。");
            return false;
        }
        $hemog = explode(".", $this->replace_data["det_field"]["HEMOGLOBIN"]);
        if (strlen($hemog[0]) > 2) {
            $this->setWarning("MSG901", "(ヘモグロビン)");
            return false;
        } elseif (strlen($hemog[1]) > 1) {
            $this->setWarning("MSG901", "(ヘモグロビン) 小数点第一位まで");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["DOC_REMARK"]) > 60) {
            $this->setWarning("MSG915", "学校医・所見は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["TREAT_REMARK1"]) > 60) {
            $this->setWarning("MSG915", "事後措置1・は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["TREATCD2_REMARK1"]) > 60) {
            $this->setWarning("MSG915", "事後措置2・は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["REMARK"]) > $this->maxRemarkByte) {
            $this->setWarning("MSG915", "備考は{$this->maxRemarkByte}バイトまでです。");
            return false;
        }
        //最低ひとつでもチェックがついてる場合のみ更新
        $check_data = "";
        foreach ($this->replace_data["det_check"] as $key) {
            if ($key) {
                $check_data .= $key;
            }
        }
        if ($check_data == "") {
            return false;
        } else {
            $this->updateHead();
            $this->updateDet();
        }
        $this->setMessage("MSG201");
        //添え字を０から順序良くする
        $this->replace_data["check"] = array_merge((array)$this->replace_data["head_check"], (array)$this->replace_data["det_check"]);
        $this->replace_data["check"][] = $this->replace_data["check_all"];
    }
    //一括更新処理4
    public function &replaceModel4()
    {
        //生徒が選択されてない場合
        if (!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["DIAGNOSIS_NAME"]) > 150) {
            $this->setWarning("MSG915", "診断名は150バイトまでです。");
            return false;
        }
        //最低ひとつでもチェックがついてる場合のみ更新
        if (!in_array("1", $this->replace_data["det_check"])) {
            return false;
        }
        if (in_array("1", $this->replace_data["det_check"])) {
            $this->updateHead();
            $this->updateDet();
        }
        $this->setMessage("MSG201");
        //添え字を０から順序良くする
        $this->replace_data["check"] = array_merge((array)$this->replace_data["head_check"], (array)$this->replace_data["det_check"]);
        $this->replace_data["check"][] = $this->replace_data["check_all"];
    }
    //MEDEXAM_HDATの更新
    public function &updateHead()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $array = explode(",", $this->replace_data["selectdata"]);
        //健康診断実施日が入力されていない場合現在処理日付(ATTEND_CTRL_DATE)を代入
        if ($this->replace_data["head_field"]["DATE"] == "") { //チェックボックスにチェックがないまたは日付が空
            $flg = "NOINPUT";
            $this->replace_data["head_field"]["DATE"] = CTRL_DATE;
            if (VARS::post("REPLACEHIDDENDATE") == "") {
                $flg2 = "NULL";
                $this->replace_data["head_field"]["DATE"] = CTRL_DATE;
            }
        }
        for ($i = 0; $i < get_count($array); $i++) {
            //MEDEXAM_HDATが存在するかチェックする
            $query = knjf010jQuery::checkMedexamHdat($this->year, $array[$i]);
            $data = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!is_array($data)) {
                $flag = "noRecord"; //レコードが存在しない
            } elseif (!$data["DATE"]) {
                $flag = "noDate"; //レコードが存在してもDATEフィールドが空
            }
            if ($flg == "NOINPUT" && ($flag == "noRecord" || $flag == "noDate")) {
                $query = knjf010jQuery::replaceUpdateHead($this, $array[$i], $flag);
            } elseif ($flg != "NOINPUT") {
                $query = knjf010jQuery::replaceUpdateHead($this, $array[$i], $flag);
            } elseif ($flg2 == "NULL") {
                $query = knjf010jQuery::replaceUpdateHead($this, $array[$i], $flag);
            } else {
                continue;
            }
            $flag = "";     //フラグを初期化

            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
    }
    //MEDEXAM_DET_DATの更新
    public function &updateDet()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $array = explode(",", $this->replace_data["selectdata"]);
        for ($i = 0; $i < get_count($array); $i++) {
            $query = knjf010jQuery::getDocDate($this->year, $array[$i]);
            $doc_date = $db->getOne($query);

            //近大は「学校医・日付」が設定されていないとき、nullのまま更新する
            $query = knjf010jQuery::getSchoolName();
            $SchoolName = $db->getOne($query);
            if ($SchoolName !== 'KINDAI' && $SchoolName !== 'KINJUNIOR') {
                if ($doc_date == '' && $this->replace_data["det_field"]["DOC_DATE"] == '') {
                    $this->replace_data["det_field"]["DOC_DATE"] = $this->replace_data["head_field"]["DATE"];
                }
            }

            //MEDEXAM_DET_DATが存在するかチェックする
            $data = common::GetMasterData("SELECT SCHREGNO FROM MEDEXAM_DET_DAT WHERE YEAR = '".$this->year."' AND SCHREGNO = '".$array[$i]."'");
            if ($data == "") {
                $flag = "insert";
            }
            $query = knjf010jQuery::replaceUpdateDet($this, $array[$i], $flag, $db);
            $flag = "";     //フラグを初期化
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
    }
}
