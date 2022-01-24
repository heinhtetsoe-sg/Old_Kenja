<?php

require_once('for_php7.php');
class knji100c_11Model extends Model
{

    public $cmd;
    public $schregno;                  //学籍番号
    public $control_data = array();    //コントロールマスタデータを保持
    public $item;
    public $selectdata_l;              //対象生徒
    public $selectdata_r;              //書出し項目一覧
    public $koteidata;                 //CSV項目(固定)
    public $mode;                      //生徒区分（在校生・卒業生）

    public function knja150Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll();

        //固定
        $this->kotei = array();
        $this->kotei[]["SCHREGNO"]              = "学籍番号";
        $this->kotei[]["NAME"]                  = "生徒氏名";
        $this->kotei[]["GRADE"]                 = "学年";
        $this->kotei[]["HR_CLASS"]              = "組";
        $this->kotei[]["SEX"]                   = "性別区分(*)";

        //画面表示項目一覧
        $this->item = array();
        $this->item[]["ATTENDNO"]               = "出席番号";
        $this->item[]["HEIGHT"]                 = "身長";
        $this->item[]["WEIGHT"]                 = "体重";
        $this->item[]["SITHEIGHT"]              = "座高";
        $this->item[]["CHEST"]                  = "胸囲";
        $this->item[]["R_BAREVISION"]           = "視力右裸眼";
        $this->item[]["R_BAREVISION_MARK"]      = "視力右裸眼(文字)";
        $this->item[]["L_BAREVISION"]           = "視力左裸眼";
        $this->item[]["L_BAREVISION_MARK"]      = "視力左裸眼（文字）";
        $this->item[]["R_VISION"]               = "視力右矯正";
        $this->item[]["R_VISION_MARK"]          = "視力右矯正（文字）";
        $this->item[]["L_VISION"]               = "視力左矯正";
        $this->item[]["L_VISION_MARK"]          = "視力左矯正(文字)";
        $this->item[]["R_EAR"]                  = "聴力右(*)";
        $this->item[]["R_EAR_DB"]               = "聴力右DB";
        $this->item[]["L_EAR"]                  = "聴力左(*)";
        $this->item[]["L_EAR_DB"]               = "聴力左DB";
        $this->item[]["ALBUMINURIA1CD"]         = "一次尿蛋白(*)";
        $this->item[]["URICSUGAR1CD"]           = "一次尿糖(*)";
        $this->item[]["URICBLEED1CD"]           = "一次尿潜血(*)";
        if ($this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
            $this->item[]["URICPH1"]                = "一次尿PH(*)";
        }
        $this->item[]["ALBUMINURIA2CD"]         = "二次尿蛋白(*)";
        $this->item[]["URICSUGAR2CD"]           = "二次尿糖(*)";
        $this->item[]["URICBLEED2CD"]           = "二次尿潜血(*)";
        if ($this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
            $this->item[]["URICPH2"]                = "二次尿PH(*)";
        }
        $this->item[]["URICOTHERTEST"]          = "尿その他の検査";
        $this->item[]["URI_ADVISECD"]           = "尿指導区分(*)";
        $this->item[]["NUTRITIONCD"]            = "栄養状態(*)";
        $this->item[]["SPINERIBCD"]             = "脊柱胸郭(*)";
        $this->item[]["SPINERIBCD_REMARK"]      = "脊柱胸郭所見";
        $this->item[]["EYEDISEASECD"]           = "目疾病及異常(*)";
        $this->item[]["EYE_TEST_RESULT"]        = "眼科所見";
        $this->item[]["NOSEDISEASECD"]          = "耳鼻咽頭疾患(*)";
        $this->item[]["NOSEDISEASECD_REMARK"]   = "耳鼻咽頭疾患所見";
        $this->item[]["SKINDISEASECD"]          = "皮膚疾患(*)";
        $this->item[]["HEART_MEDEXAM"]          = "心臓臨床医学的検査(*)";
        $this->item[]["HEART_MEDEXAM_REMARK"]   = "心臓臨床医学的検査";
        $this->item[]["HEARTDISEASECD"]         = "心臓疾病及異常(*)";
        $this->item[]["HEARTDISEASECD_REMARK"]  = "心臓疾病及異常所見";
        $this->item[]["MANEGEMENT_DIV"]         = "心臓管理区分";
        $this->item[]["MANAGEMENT_REMARK"]      = "心臓管理所見";
        $this->item[]["TB_DATE"]                = "結核ツ反実施日";
        $this->item[]["TB_REACT"]               = "結核ツ反応";
        $this->item[]["TB_RESULT"]              = "結核ツ反判定(*)";
        $this->item[]["TB_BCGDATE"]             = "結核BCG検査日";
        $this->item[]["TB_FILMDATE"]            = "結核撮影日付";
        $this->item[]["TB_FILMNO"]              = "結核フィルム番号";
        $this->item[]["TB_REMARKCD"]            = "結核所見(*)";
        $this->item[]["TB_OTHERTESTCD"]         = "結核その他検査(*)";
        $this->item[]["TB_NAMECD"]              = "結核病名(*)";
        $this->item[]["TB_ADVISECD"]            = "結核指導区分(*)";
        $this->item[]["TB_X_RAY"]               = "結核所見（X線）";
        $this->item[]["ANEMIA_REMARK"]          = "貧血所見";
        $this->item[]["HEMOGLOBIN"]             = "貧血ヘモグロビン値";
        $this->item[]["PARASITE"]               = "寄生虫卵";
        $this->item[]["OTHERDISEASECD"]         = "その他疾病及異常(*)";
        $this->item[]["OTHER_ADVISECD"]         = "その他疾病指導区分(*)";
        $this->item[]["OTHER_REMARK"]           = "その他疾病及び異常所見";
        $this->item[]["DOC_CD"]                 = "学校医(*)（内科検診）";
        $this->item[]["DOC_REMARK"]             = "学校医所見";
        $this->item[]["DOC_DATE"]               = "学校医日付";
        $this->item[]["TREATCD"]                = "事後処置(*)";
        $this->item[]["REMARK"]                 = "備考";
        $this->item[]["NUTRITION_RESULT"]       = "内科検診結果(栄養状態）";
        $this->item[]["EYEDISEASE_RESULT"]      = "眼科検診結果(目疾病及異常)";
        $this->item[]["SKINDISEASE_RESULT"]     = "皮膚科検診結果(皮膚疾患)";
        $this->item[]["SPINERIB_RESULT"]        = "脊柱胸郭検診結果(脊柱胸郭)";
        $this->item[]["NOSEDISEASE_RESULT"]     = "耳鼻科検診結果(耳鼻咽頭疾患)";
        $this->item[]["OTHERDISEASE_RESULT"]    = "その他検診結果(その他疾病及異常)";
        $this->item[]["HEARTDISEASE_RESULT"]    = "心臓検診結果(心臓疾病及異常)";
        $this->item[]["GUIDE_DIV"]              = "指導区分(*)";
        $this->item[]["JOINING_SPORTS_CLUB"]    = "運動部活動(*)";
        $this->item[]["MEDICAL_HISTORY1"]       = "既往症1(*)";
        $this->item[]["MEDICAL_HISTORY2"]       = "既往症2(*)";
        $this->item[]["MEDICAL_HISTORY3"]       = "既往症3(*)";
        $this->item[]["DIAGNOSIS_NAME"]         = "診断名";
        if ($this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
            $this->item[]["JAWS_JOINTCD"]           = "歯列(*)";
            $this->item[]["JAWS_JOINTCD3"]          = "咬合(*)";
        } else {
            $this->item[]["JAWS_JOINTCD"]           = "歯列・咬合(*)";
        }
        $this->item[]["JAWS_JOINTCD2"]          = "顎関節(*)";
        if ($this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
            $this->item[]["PLAQUECD"]               = "歯垢付着(*)";
        } else {
            $this->item[]["PLAQUECD"]               = "歯垢状態(*)";
        }
        $this->item[]["GUMCD"]                  = "歯肉状態(*)";
        if ($this->Properties["KenkouSindan_Ippan_Pattern"] != "1") {
            $this->item[]["CALCULUS"]               = "歯石沈着(*)";
        }
        $this->item[]["ORTHODONTICS"]           = "矯正の有無";
        $this->item[]["UP_R_BABY5"]             = "乳歯：上：右5(*)";
        $this->item[]["UP_R_BABY4"]             = "乳歯：上：右4(*)";
        $this->item[]["UP_R_BABY3"]             = "乳歯：上：右3(*)";
        $this->item[]["UP_R_BABY2"]             = "乳歯：上：右2(*)";
        $this->item[]["UP_R_BABY1"]             = "乳歯：上：右1(*)";
        $this->item[]["UP_L_BABY1"]             = "乳歯：上：左1(*)";
        $this->item[]["UP_L_BABY2"]             = "乳歯：上：左2(*)";
        $this->item[]["UP_L_BABY3"]             = "乳歯：上：左3(*)";
        $this->item[]["UP_L_BABY4"]             = "乳歯：上：左4(*)";
        $this->item[]["UP_L_BABY5"]             = "乳歯：上：左5(*)";
        $this->item[]["LW_R_BABY5"]             = "乳歯：下：右5(*)";
        $this->item[]["LW_R_BABY4"]             = "乳歯：下：右4(*)";
        $this->item[]["LW_R_BABY3"]             = "乳歯：下：右3(*)";
        $this->item[]["LW_R_BABY2"]             = "乳歯：下：右2(*)";
        $this->item[]["LW_R_BABY1"]             = "乳歯：下：右1(*)";
        $this->item[]["LW_L_BABY1"]             = "乳歯：下：左1(*)";
        $this->item[]["LW_L_BABY2"]             = "乳歯：下：左2(*)";
        $this->item[]["LW_L_BABY3"]             = "乳歯：下：左3(*)";
        $this->item[]["LW_L_BABY4"]             = "乳歯：下：左4(*)";
        $this->item[]["LW_L_BABY5"]             = "乳歯：下：左5(*)";
        $this->item[]["BABYTOOTH"]              = "乳歯現在数";
        $this->item[]["REMAINBABYTOOTH"]        = "乳歯未処置数";
        $this->item[]["TREATEDBABYTOOTH"]       = "乳歯処置数";
        $this->item[]["BRACK_BABYTOOTH"]        = "要注意乳歯数";
        $this->item[]["UP_R_ADULT8"]            = "永久歯：上：右8(*)";
        $this->item[]["UP_R_ADULT7"]            = "永久歯：上：右7(*)";
        $this->item[]["UP_R_ADULT6"]            = "永久歯：上：右6(*)";
        $this->item[]["UP_R_ADULT5"]            = "永久歯：上：右5(*)";
        $this->item[]["UP_R_ADULT4"]            = "永久歯：上：右4(*)";
        $this->item[]["UP_R_ADULT3"]            = "永久歯：上：右3(*)";
        $this->item[]["UP_R_ADULT2"]            = "永久歯：上：右2(*)";
        $this->item[]["UP_R_ADULT1"]            = "永久歯：上：右1(*)";
        $this->item[]["UP_L_ADULT1"]            = "永久歯：上：左1(*)";
        $this->item[]["UP_L_ADULT2"]            = "永久歯：上：左2(*)";
        $this->item[]["UP_L_ADULT3"]            = "永久歯：上：左3(*)";
        $this->item[]["UP_L_ADULT4"]            = "永久歯：上：左4(*)";
        $this->item[]["UP_L_ADULT5"]            = "永久歯：上：左5(*)";
        $this->item[]["UP_L_ADULT6"]            = "永久歯：上：左6(*)";
        $this->item[]["UP_L_ADULT7"]            = "永久歯：上：左7(*)";
        $this->item[]["UP_L_ADULT8"]            = "永久歯：上：左8(*)";
        $this->item[]["LW_R_ADULT8"]            = "永久歯：下：右8(*)";
        $this->item[]["LW_R_ADULT7"]            = "永久歯：下：右7(*)";
        $this->item[]["LW_R_ADULT6"]            = "永久歯：下：右6(*)";
        $this->item[]["LW_R_ADULT5"]            = "永久歯：下：右5(*)";
        $this->item[]["LW_R_ADULT4"]            = "永久歯：下：右4(*)";
        $this->item[]["LW_R_ADULT3"]            = "永久歯：下：右3(*)";
        $this->item[]["LW_R_ADULT2"]            = "永久歯：下：右2(*)";
        $this->item[]["LW_R_ADULT1"]            = "永久歯：下：右1(*)";
        $this->item[]["LW_L_ADULT1"]            = "永久歯：下：左1(*)";
        $this->item[]["LW_L_ADULT2"]            = "永久歯：下：左2(*)";
        $this->item[]["LW_L_ADULT3"]            = "永久歯：下：左3(*)";
        $this->item[]["LW_L_ADULT4"]            = "永久歯：下：左4(*)";
        $this->item[]["LW_L_ADULT5"]            = "永久歯：下：左5(*)";
        $this->item[]["LW_L_ADULT6"]            = "永久歯：下：左6(*)";
        $this->item[]["LW_L_ADULT7"]            = "永久歯：下：左7(*)";
        $this->item[]["LW_L_ADULT8"]            = "永久歯：下：左8(*)";
        $this->item[]["ADULTTOOTH"]             = "永久歯数";
        $this->item[]["REMAINADULTTOOTH"]       = "永久歯未処置数";
        $this->item[]["TREATEDADULTTOOTH"]      = "永久歯処置数";
        $this->item[]["LOSTADULTTOOTH"]         = "永久歯喪失数";
        $this->item[]["BRACK_ADULTTOOTH"]       = "要観察歯数";
        $this->item[]["CHECKADULTTOOTH"]        = "要精検歯数";
        $this->item[]["T_OTHERDISEASECD"]       = "その他疾病及異常(*)";
        $this->item[]["T_OTHERDISEASE"]         = "その他疾病及び異常";
        $this->item[]["DENTISTREMARKCD"]        = "学校歯科医所見(*)";
        $this->item[]["DENTISTREMARK"]          = "学校医所見";
        $this->item[]["DENTISTREMARKDATE"]      = "学校歯科医所見日付";
        $this->item[]["DENTISTTREATCD"]         = "学校歯科医事後処置(*)";
        $this->item[]["DENTISTTREAT"]           = "学校歯科医事後処置";

        $this->cdAri = array ("SEX"                => "Z002",
                              "R_EAR"              => "F010",
                              "L_EAR"              => "F010",
                              "ALBUMINURIA1CD"     => "F020",
                              "URICSUGAR1CD"       => "F019",
                              "URICBLEED1CD"       => "F018",
                              "ALBUMINURIA2CD"     => "F020",
                              "URICSUGAR2CD"       => "F019",
                              "URICBLEED2CD"       => "F018",
                              "URI_ADVISECD"       => "F021",
                              "NUTRITIONCD"        => "F030",
                              "SPINERIBCD"         => "F040",
                              "EYEDISEASECD"       => "F050",
                              "NOSEDISEASECD"      => "F060",
                              "SKINDISEASECD"      => "F070",
                              "HEART_MEDEXAM"      => "F080",
                              "HEARTDISEASECD"     => "F090",
                              "MANEGEMENT_DIV"     => "F091",
                              "TB_RESULT"          => "F020",
                              "TB_REMARKCD"        => "F100",
                              "TB_OTHERTESTCD"     => "F110",
                              "TB_NAMECD"          => "F120",
                              "TB_ADVISECD"        => "F130",
                              "OTHERDISEASECD"     => "F140",
                              "OTHER_ADVISECD"     => "F145",
                              "DOC_CD"             => "F144",
                              "TREATCD"            => "F150",
                              "GUIDE_DIV"          => "F141",
                              "JOINING_SPORTS_CLUB"=> "F142",
                              "MEDICAL_HISTORY1"   => "F143",
                              "MEDICAL_HISTORY2"   => "F143",
                              "MEDICAL_HISTORY3"   => "F143",
                              "JAWS_JOINTCD"       => "F510",
                              "JAWS_JOINTCD2"      => "F511",
                              "PLAQUECD"           => "F520",
                              "GUMCD"              => "F510",
                              "CALCULUS"           => "F521",
                              "UP_R_BABY5"         => "F550",
                              "UP_R_BABY4"         => "F550",
                              "UP_R_BABY3"         => "F550",
                              "UP_R_BABY2"         => "F550",
                              "UP_R_BABY1"         => "F550",
                              "UP_L_BABY1"         => "F550",
                              "UP_L_BABY2"         => "F550",
                              "UP_L_BABY3"         => "F550",
                              "UP_L_BABY4"         => "F550",
                              "UP_L_BABY5"         => "F550",
                              "LW_R_BABY5"         => "F550",
                              "LW_R_BABY4"         => "F550",
                              "LW_R_BABY3"         => "F550",
                              "LW_R_BABY2"         => "F550",
                              "LW_R_BABY1"         => "F550",
                              "LW_L_BABY1"         => "F550",
                              "LW_L_BABY2"         => "F550",
                              "LW_L_BABY3"         => "F550",
                              "LW_L_BABY4"         => "F550",
                              "LW_L_BABY5"         => "F550",
                              "UP_R_ADULT8"        => "F550",
                              "UP_R_ADULT7"        => "F550",
                              "UP_R_ADULT6"        => "F550",
                              "UP_R_ADULT5"        => "F550",
                              "UP_R_ADULT4"        => "F550",
                              "UP_R_ADULT3"        => "F550",
                              "UP_R_ADULT2"        => "F550",
                              "UP_R_ADULT1"        => "F550",
                              "UP_L_ADULT1"        => "F550",
                              "UP_L_ADULT2"        => "F550",
                              "UP_L_ADULT3"        => "F550",
                              "UP_L_ADULT4"        => "F550",
                              "UP_L_ADULT5"        => "F550",
                              "UP_L_ADULT6"        => "F550",
                              "UP_L_ADULT7"        => "F550",
                              "UP_L_ADULT8"        => "F550",
                              "LW_R_ADULT8"        => "F550",
                              "LW_R_ADULT7"        => "F550",
                              "LW_R_ADULT6"        => "F550",
                              "LW_R_ADULT5"        => "F550",
                              "LW_R_ADULT4"        => "F550",
                              "LW_R_ADULT3"        => "F550",
                              "LW_R_ADULT2"        => "F550",
                              "LW_R_ADULT1"        => "F550",
                              "LW_L_ADULT1"        => "F550",
                              "LW_L_ADULT2"        => "F550",
                              "LW_L_ADULT3"        => "F550",
                              "LW_L_ADULT4"        => "F550",
                              "LW_L_ADULT5"        => "F550",
                              "LW_L_ADULT6"        => "F550",
                              "LW_L_ADULT7"        => "F550",
                              "LW_L_ADULT8"        => "F550",
                              "T_OTHERDISEASECD"   => "F530",
                              "DENTISTREMARKCD"    => "F540",
                              "DENTISTTREATCD"     => "F541");

        if ($this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                $this->cdAri["JAWS_JOINTCD3"] = "F512";
        }

        //ITEM
        $this->selectdata_r     = VARS::post("selectdata_r");
        //対象生徒
        $this->selectdata_l     = VARS::post("selectdata_l");
        //課程学科
        $this->coursemajor      = VARS::post("COURSEMAJOR");
        //学年
        $this->grade            = VARS::post("GRADE");
        //性別
        $this->sex              = VARS::post("SEX");
        //年組
        $this->grade_hr_class   = VARS::post("GRADE_HR_CLASS");
        //異動対象日付
        $this->date             = VARS::post("DATE");
        //出力設定
        $this->output           = VARS::post("OUTPUT");

        //サブシステム
        $this->subsystem = (VARS::get("SUBSYSTEM")) ? VARS::get("SUBSYSTEM") : '11';
        //対象生徒
        if (VARS::get("selectdata_l")) {
            $this->selectdata_l     = VARS::get("selectdata_l");
        }
        //学年
        if (VARS::get("GRADE")) {
            $this->grade            = VARS::get("GRADE");
        }
        //年組
        if (VARS::get("GRADE_HR_CLASS")) {
            $this->grade_hr_class   = VARS::get("GRADE_HR_CLASS");
        }
        //出力設定
        if (VARS::get("OUTPUT")) {
            $this->output = VARS::get("OUTPUT");
        }

        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }
    }

    //CSV書出し
    public function &getCsvModel()
    {
        $db = Query::dbCheckOut();
        $this->koteidata = "";
        foreach ($this->kotei as $key => $val) {
            foreach ($val as $field => $label) {
                $this->koteidata = $this->koteidata.$field.",";
            }
        }
        $item = explode(",", $kotei_data.$this->selectdata_r);

        $query = knji100c_11Query::getDelFieldSql($this);
        $db->query($query);
        $setSeq = 1;
        foreach ($item as $key => $val) {
            $query = knji100c_11Query::getFieldInsSql($this, $setSeq, $val);
            $db->query($query);
            $setSeq++;
        }
        $this->selectdata_r = $this->koteidata.$this->selectdata_r;
        $item = explode(",", $kotei_data.$this->selectdata_r);

        //項目名一覧
        $item_array = array();
        foreach ($this->kotei as $key => $val) {
            foreach ($val as $field => $label) {
                $item_array[$field] = $label; //固定項目
            }
        }
        foreach ($this->item as $key => $val) {
            foreach ($val as $field => $label) {
                $item_array[$field] = $label; //書出し項目
            }
        }

        $contents = $sep = "";
        $schoolkind = $db->getCol(knji100c_11Query::getSchoolKind());
        //ヘッダ作成
        for ($i = 0; $i < get_count($item); $i++) {
            if ($this->output == OUT_CODE_NAME) {    //コード＋名称
                if (array_key_exists($item[$i], $this->cdAri)) {
                    $code  = str_replace("(*)", "", $item_array[$item[$i]]);
                    $code .= "コード,";
                    $contents .= $sep .$code.$item_array[$item[$i]];
                } else {
                    $contents .= $sep .$item_array[$item[$i]];
                }
            } else {
                $contents .= $sep .$item_array[$item[$i]];
            }
            $sep = ",";
        }
        $contents .= "\n";

        //データ部作成
        $query = knji100c_11Query::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";
            for ($i = 0; $i < get_count($item); $i++) {
                if (array_key_exists($item[$i], $this->cdAri)) {
                    if ($this->output == OUT_CODE_NAME) {
                        $name1 = $db->getOne(knji100c_11Query::getMeisyou($this->cdAri[$item[$i]], $row[$item[$i]]));
                        $contents .= $sep .$row[$item[$i]].",".$name1;
                    } elseif ($this->output == OUT_CODE_ONLY) {
                        $contents .= $sep .$row[$item[$i]];
                    } else {
                        $name1 = $db->getOne(knji100c_11Query::getMeisyou($this->cdAri[$item[$i]], $row[$item[$i]]));
                        $contents .= $sep .$name1;
                    }
                } else {
                    $contents .= $sep .$row[$item[$i]];
                }
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        Query::dbCheckIn($db);
        if ($data) {
            $filename = sprintf("%s.csv", "健康情報一般・歯");
            common::downloadContents($contents, $filename);
            exit;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
