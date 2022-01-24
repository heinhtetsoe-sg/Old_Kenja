<?php

require_once('for_php7.php');
class knjh400_ippankensinModel extends Model
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

    public function knjh400_ippankensinModel()
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
            $this->z010name1 = $db->getOne(knjh400_ippankensinQuery::getSchoolName());
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
        $this->schregno = VARS::request("SCHREGNO") != "" ? VARS::request("SCHREGNO") : $this->schregno;

        $this->year = VARS::request("YEAR") != "" ? VARS::request("YEAR") : $this->year;
        $this->exp_year = $this->year;

        $this->semester = VARS::request("SEMESTER") != "" ? VARS::request("SEMESTER") : $this->semester;
        $this->exp_semester = $this->semester;

        //フィールド
        $this->field = array("DATE"                     =>  VARS::post("DATE"),                     //健康診断実施日付
                             "HEIGHT"                   =>  VARS::post("HEIGHT"),                   //身長
                             "WEIGHT"                   =>  VARS::post("WEIGHT"),                   //体重
                             "SITHEIGHT"                =>  VARS::post("SITHEIGHT"),                //座高
                             "NO_PRINT_OBESITY_INDEX"   =>  VARS::post("NO_PRINT_OBESITY_INDEX"),   //肥満度印字無し
                             "R_BAREVISION"             =>  VARS::post("R_BAREVISION"),             //視力・右裸眼（数字）
                             "R_BAREVISION_MARK"        =>  VARS::post("R_BAREVISION_MARK"),        //視力・右裸眼（文字）
                             "R_VISION"                 =>  VARS::post("R_VISION"),                 //視力・右矯正（数字）
                             "R_VISION_MARK"            =>  VARS::post("R_VISION_MARK"),            //視力・右矯正（文字）
                             "R_VISION_CANTMEASURE"     =>  VARS::post("R_VISION_CANTMEASURE"),     //視力・（右）測定困難
                             "L_BAREVISION"             =>  VARS::post("L_BAREVISION"),             //視力・左裸眼（数字）
                             "L_BAREVISION_MARK"        =>  VARS::post("L_BAREVISION_MARK"),        //視力・左裸眼（文字）
                             "L_VISION"                 =>  VARS::post("L_VISION"),                 //視力・左矯正（数字）
                             "L_VISION_MARK"            =>  VARS::post("L_VISION_MARK"),            //視力・左矯正（文字）
                             "L_VISION_CANTMEASURE"     =>  VARS::post("L_VISION_CANTMEASURE"),     //視力・（左）測定困難
                             "VISION_CANTMEASURE"       =>  VARS::post("VISION_CANTMEASURE"),       //視力・測定不能（文字）
                             "EYE_TEST_RESULT"          =>  VARS::post("EYE_TEST_RESULT"),          //眼科検診結果
                             "EYE_TEST_RESULT2"         =>  VARS::post("EYE_TEST_RESULT2"),         //眼科検診結果2
                             "EYE_TEST_RESULT3"         =>  VARS::post("EYE_TEST_RESULT3"),         //眼科検診結果3
                             "R_EAR_DB"                 =>  VARS::post("R_EAR_DB"),                 //聴力・右DB
                             "R_EAR_DB_1000"            =>  VARS::post("R_EAR_DB_1000"),            //聴力・右DB
                             "R_EAR_DB_4000"            =>  VARS::post("R_EAR_DB_4000"),            //聴力・右4000Hz
                             "R_EAR"                    =>  VARS::post("R_EAR"),                    //聴力・右状態
                             "R_EAR_CANTMEASURE"        =>  VARS::post("R_EAR_CANTMEASURE"),        //聴力・（右）測定困難
                             "R_EAR_DB_IN"              =>  VARS::post("R_EAR_DB_IN"),              //聴力・右DB    （装用時）
                             "R_EAR_DB_4000_IN"         =>  VARS::post("R_EAR_DB_4000_IN"),         //聴力・右4000Hz（装用時）
                             "R_EAR_IN"                 =>  VARS::post("R_EAR_IN"),                 //聴力・右状態  （装用時）
                             "L_EAR_DB"                 =>  VARS::post("L_EAR_DB"),                 //聴力・左DB
                             "L_EAR_DB_1000"            =>  VARS::post("L_EAR_DB_1000"),            //聴力・左DB
                             "L_EAR_DB_4000"            =>  VARS::post("L_EAR_DB_4000"),            //聴力・左4000Hz
                             "L_EAR"                    =>  VARS::post("L_EAR"),                    //聴力・左状態
                             "L_EAR_CANTMEASURE"        =>  VARS::post("L_EAR_CANTMEASURE"),        //聴力・（左）測定困難
                             "L_EAR_DB_IN"              =>  VARS::post("L_EAR_DB_IN"),              //聴力・左DB    （装用時）
                             "L_EAR_DB_4000_IN"         =>  VARS::post("L_EAR_DB_4000_IN"),         //聴力・左4000Hz（装用時）
                             "L_EAR_IN"                 =>  VARS::post("L_EAR_IN"),                 //聴力・左状態  （装用時）
                             "ALBUMINURIA1CD"           =>  VARS::post("ALBUMINURIA1CD"),           //尿・１次蛋白
                             "URICSUGAR1CD"             =>  VARS::post("URICSUGAR1CD"),             //尿・１次糖
                             "URICBLEED1CD"             =>  VARS::post("URICBLEED1CD"),             //尿・１次潜血
                             "URICPH1"                  =>  VARS::post("URICPH1"),                  //尿・１次PH
                             "ALBUMINURIA2CD"           =>  VARS::post("ALBUMINURIA2CD"),           //尿・２次蛋白
                             "URICSUGAR2CD"             =>  VARS::post("URICSUGAR2CD"),             //尿・２次糖
                             "URICBLEED2CD"             =>  VARS::post("URICBLEED2CD"),             //尿・２次潜血
                             "URICPH2"                  =>  VARS::post("URICPH2"),                  //尿・２次PH
                             "URICOTHERTESTCD"          =>  VARS::post("URICOTHERTESTCD"),          //尿・その他の検査(printKenkouSindanIppan = 2)
                             "URICOTHERTEST"            =>  VARS::post("URICOTHERTEST"),            //尿・その他の検査
                             "URI_ADVISECD"             =>  VARS::post("URI_ADVISECD"),             //尿・指導区分(コンボ)
                             "NUTRITIONCD"              =>  VARS::post("NUTRITIONCD"),              //栄養状態
                             "NUTRITIONCD_REMARK"       =>  VARS::post("NUTRITIONCD_REMARK"),       //栄養状態備考
                             "SPINERIBCD"               =>  VARS::post("SPINERIBCD"),               //脊柱・胸部(コンボ)
                             "SPINERIBCD_REMARK"        =>  VARS::post("SPINERIBCD_REMARK"),        //脊柱・胸部(テキスト)
                             "EYEDISEASECD"             =>  VARS::post("EYEDISEASECD"),             //目の疾病及び異常
                             "EYEDISEASECD2"            =>  VARS::post("EYEDISEASECD2"),            //目の疾病及び異常
                             "EYEDISEASECD3"            =>  VARS::post("EYEDISEASECD3"),            //目の疾病及び異常
                             "EYEDISEASECD4"            =>  VARS::post("EYEDISEASECD4"),            //目の疾病及び異常
                             "EYEDISEASECD5"            =>  VARS::post("EYEDISEASECD5"),            //目の疾病及び異常
                             "NOSEDISEASECD"            =>  VARS::post("NOSEDISEASECD"),            //耳鼻咽頭疾患(コンボ)
                             "NOSEDISEASECD2"           =>  VARS::post("NOSEDISEASECD2"),           //耳鼻咽頭疾患(コンボ)
                             "NOSEDISEASECD3"           =>  VARS::post("NOSEDISEASECD3"),           //耳鼻咽頭疾患(コンボ)
                             "NOSEDISEASECD4"           =>  VARS::post("NOSEDISEASECD4"),           //耳鼻咽頭疾患(コンボ)
                             "NOSEDISEASECD5"           =>  VARS::post("NOSEDISEASECD5"),           //耳鼻咽頭疾患(コンボ)
                             "NOSEDISEASECD6"           =>  VARS::post("NOSEDISEASECD6"),           //耳鼻咽頭疾患(コンボ)
                             "NOSEDISEASECD7"           =>  VARS::post("NOSEDISEASECD7"),           //耳鼻咽頭疾患(コンボ)
                             "NOSEDISEASECD_REMARK"     =>  VARS::post("NOSEDISEASECD_REMARK"),     //耳鼻咽頭疾患(テキスト)
                             "NOSEDISEASECD_REMARK1"    =>  VARS::post("NOSEDISEASECD_REMARK1"),    //耳鼻咽頭疾患1(テキスト)
                             "NOSEDISEASECD_REMARK2"    =>  VARS::post("NOSEDISEASECD_REMARK2"),    //耳鼻咽頭疾患2(テキスト)
                             "NOSEDISEASECD_REMARK3"    =>  VARS::post("NOSEDISEASECD_REMARK3"),    //耳鼻咽頭疾患3(テキスト)
                             "SKINDISEASECD"            =>  VARS::post("SKINDISEASECD"),            //皮膚疾患
                             "SKINDISEASECD_REMARK"     =>  VARS::post("SKINDISEASECD_REMARK"),     //皮膚疾患(テキスト)
                             "HEART_MEDEXAM"            =>  VARS::post("HEART_MEDEXAM"),            //心臓・臨床医学的検査(コンボ)
                             "HEART_MEDEXAM_REMARK"     =>  VARS::post("HEART_MEDEXAM_REMARK"),     //心臓・臨床医学的検査(テキスト)
                             "HEART_GRAPH_NO"           =>  VARS::post("HEART_GRAPH_NO"),           //心臓・臨床医学的検査(心電図No)
                             "HEARTDISEASECD"           =>  VARS::post("HEARTDISEASECD"),           //心臓・疾病及び異常(コンボ)
                             "HEARTDISEASECD_REMARK"    =>  VARS::post("HEARTDISEASECD_REMARK"),    //心臓・疾病及び異常(テキスト)
                             "MANAGEMENT_DIV"           =>  VARS::post("MANAGEMENT_DIV"),           //心臓・管理区分（コンボ）
                             "MANAGEMENT_REMARK"        =>  VARS::post("MANAGEMENT_REMARK"),        //心臓・管理区分（テキスト）
                             "TB_FILMDATE"              =>  VARS::post("TB_FILMDATE"),              //結核・撮影日付
                             "TB_FILMNO"                =>  VARS::post("TB_FILMNO"),                //結核・フィルム番号
                             "TB_REMARKCD"              =>  VARS::post("TB_REMARKCD"),              //結核・所見
                             "TB_OTHERTESTCD"           =>  VARS::post("TB_OTHERTESTCD"),           //結核・その他検査
                             "TB_OTHERTEST_REMARK1"     =>  VARS::post("TB_OTHERTEST_REMARK1"),     //結核・その他検査
                             "TB_NAMECD"                =>  VARS::post("TB_NAMECD"),                //結核・病名
                             "TB_NAME_REMARK1"          =>  VARS::post("TB_NAME_REMARK1"),          //結核・病名所見
                             "TB_ADVISECD"              =>  VARS::post("TB_ADVISECD"),              //結核・指導区分
                             "TB_ADVISE_REMARK1"        =>  VARS::post("TB_ADVISE_REMARK1"),        //結核・指導区分
                             "TB_X_RAY"                 =>  VARS::post("TB_X_RAY"),                 //結核・検査(X線)
                             "PARASITE"                 =>  VARS::post("PARASITE"),                 //寄生虫卵
                             "OTHERDISEASECD"           =>  VARS::post("OTHERDISEASECD"),           //その他疾病及び異常(コンボ)
                             "OTHER_ADVISECD"           =>  VARS::post("OTHER_ADVISECD"),           //その他疾病及び異常・指導区分(コンボ)
                             "OTHER_REMARK"             =>  VARS::post("OTHER_REMARK"),             //その他疾病及び異常(テキスト)
                             "OTHER_REMARK2"            =>  VARS::post("OTHER_REMARK2"),            //その他疾病及び異常2(テキスト)
                             "OTHER_REMARK3"            =>  VARS::post("OTHER_REMARK3"),            //その他疾病及び異常3(テキスト)
                             "DOC_CD"                   =>  VARS::post("DOC_CD"),                   //学校医・所見(コンボ)
                             "ANEMIA_REMARK"            =>  VARS::post("ANEMIA_REMARK"),            //貧血(所見)
                             "HEMOGLOBIN"               =>  VARS::post("HEMOGLOBIN"),               //貧血(ヘモグロビン)
                             "DOC_REMARK"               =>  VARS::post("DOC_REMARK"),               //学校医・所見(テキスト)
                             "DOC_DATE"                 =>  VARS::post("DOC_DATE"),                 //学校医・所見日付
                             "DOC_NAME"                 =>  VARS::post("DOC_NAME"),                 //学校医・氏名(三重のみ)
                             "TREATCD"                  =>  VARS::post("TREATCD"),                  //事後措置
                             "TREATCD2"                 =>  VARS::post("TREATCD2"),                 //事後措置2（コンボ）
                             "TREAT_REMARK1"            =>  VARS::post("TREAT_REMARK1"),            //事後措置1
                             "TREAT_REMARK2"            =>  VARS::post("TREAT_REMARK2"),            //事後措置2
                             "TREAT_REMARK3"            =>  VARS::post("TREAT_REMARK3"),            //事後措置3
                             "REMARK"                   =>  VARS::post("REMARK"),                   //備考
                             "MESSAGE"                  =>  VARS::post("MESSAGE"),                  //メッセージ
                             "GUIDE_DIV"                =>  VARS::post("GUIDE_DIV"),                //運動/指導区分
                             "JOINING_SPORTS_CLUB"      =>  VARS::post("JOINING_SPORTS_CLUB"),      //運動/部活動
                             "MEDICAL_HISTORY1"         =>  VARS::post("MEDICAL_HISTORY1"),         //既往症1
                             "MEDICAL_HISTORY2"         =>  VARS::post("MEDICAL_HISTORY2"),         //既往症2
                             "MEDICAL_HISTORY3"         =>  VARS::post("MEDICAL_HISTORY3"),         //既往症3
                             "DIAGNOSIS_NAME"           =>  VARS::post("DIAGNOSIS_NAME")            //診断名
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
        $query = knjh400_ippankensinQuery::getSchoolKind($this->getGrade);
        $this->getSchKind = $db->getOne($query);
        //テーブルの有無チェック
        $query = knjh400_ippankensinQuery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && ($this->schregno || ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""))) {
            //生徒項目名取得
            $schName = $db->getOne(knjh400_ippankensinQuery::getSchName($this));
        }
        $this->sch_label = (strlen($schName) > 0) ? $schName : '生徒';
        Query::dbCheckIn($db);
    }
    //入力項目チェック
    public function validateInput()
    {
        return true;
    }
}
