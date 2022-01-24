<?php

require_once('for_php7.php');
class knjf323Model extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $attendno;
    var $field = array();

    function knjf323Model() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
        }

        $this->field = array("CARE_FLG01"           =>  VARS::post("CARE_FLG01"),
                             "CARE_FLG02"           =>  VARS::post("CARE_FLG02"),
                             "CARE_FLG03"           =>  VARS::post("CARE_FLG03"),
                             "CARE_FLG04"           =>  VARS::post("CARE_FLG04"),
                             "CARE_FLG05"           =>  VARS::post("CARE_FLG05"),
                             "CARE_FLG06"           =>  VARS::post("CARE_FLG06"),
                             "MEDICAL_HISTORY1"     =>  VARS::post("MEDICAL_HISTORY1"),
                             "MEDICAL_HISTORY2"     =>  VARS::post("MEDICAL_HISTORY2"),
                             "MEDICAL_HISTORY3"     =>  VARS::post("MEDICAL_HISTORY3"),
                             "DIAGNOSIS_NAME"       =>  VARS::post("DIAGNOSIS_NAME"),
                             "GUIDE_DIV"            =>  VARS::post("GUIDE_DIV"),
                             "JOINING_SPORTS_CLUB"  =>  VARS::post("JOINING_SPORTS_CLUB"),
                             "EMERGENCYNAME"        =>  VARS::post("EMERGENCYNAME"),
                             "EMERGENCYTELNO"       =>  VARS::post("EMERGENCYTELNO"),
                             "EMERGENCYNAME2"       =>  VARS::post("EMERGENCYNAME2"),
                             "EMERGENCYTELNO2"      =>  VARS::post("EMERGENCYTELNO2"),
                             "DATE"                 =>  VARS::post("DATE"),
                             "DOCTOR"               =>  VARS::post("DOCTOR"),
                             "HOSPITAL"             =>  VARS::post("HOSPITAL"),
                             "REMARK"               =>  VARS::post("REMARK"),
                             "YEAR"                 =>  VARS::post("YEAR"),
                             "DBNAME"               =>  VARS::post("DBNAME")
                            );

        //種別
        for ($i=1; $i <= 2 ; $i++) {
            //項目
            for ($j=1; $j <= 6 ; $j++) {
                //SEQ
                for ($k=0; $k <= 12 ; $k++) {
                    $cd = sprintf("%02d", $i)."_".sprintf("%02d", $j)."_".sprintf("%02d", $k);

                    $this->field["CHECK".$cd]   =  VARS::post("CHECK".$cd);
                    $this->field["TEXT".$cd]    =  VARS::post("TEXT".$cd);

                    if ($i.'_'.$j == '1_3' || $i.'_'.$j.'_'.$k == '1_1_02') {
                        $this->field["DETAIL".$cd]    =  VARS::post("DETAIL".$cd);

                        for ($l=1; $l <= 4 ; $l++) {
                            $this->field["REASON".sprintf("%02d", $k)."_".$l]   =  VARS::post("REASON".sprintf("%02d", $k)."_".$l);
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

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput($cmd) {
        //必須チェック
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return false;
        }

        //対象年度チェック（処理年度のみ更新可）
        if ($this->field["YEAR"] != CTRL_YEAR) {
            $this->setWarning("MSG300", "更新は".CTRL_YEAR."年度のみ可能です。");
            return false;
        }

        //文字数チェック項目一覧
        $txt_array = $sonota = array();
        $txt_array["subform1"]["TEXT01_02_04"]      = array("gyo" => 1,  "moji" => 20, "title" => "その他");
        $txt_array["subform1"]["TEXT01_03_04"]      = array("gyo" => 1,  "moji" => 20, "title" => "その他");
        $txt_array["subform1"]["TEXT01_05_00"]      = array("gyo" => 15, "moji" => 20, "title" => "急性発作時の対応");
        $txt_array["subform1"]["TEXT02_02_03"]      = array("gyo" => 1,  "moji" => 40, "title" => "動物名");
        $txt_array["subform1"]["TEXT02_04_00"]      = array("gyo" => 7,  "moji" => 40, "title" => "その他の配慮・管理事項");

        $txt_array["subform2"]["TEXT01_02_04"]      = array("gyo" => 1,  "moji" => 20, "title" => "その他");
        $txt_array["subform2"]["TEXT01_03_02"]      = array("gyo" => 1,  "moji" => 40, "title" => "その他");
        $txt_array["subform2"]["TEXT02_02_03"]      = array("gyo" => 1,  "moji" => 40, "title" => "動物名");
        $txt_array["subform2"]["TEXT02_04_00"]      = array("gyo" => 7,  "moji" => 20, "title" => "その他の配慮・管理事項");

        $txt_array["subform3"]["TEXT01_01_05"]      = array("gyo" => 1,  "moji" => 20, "title" => "その他");
        $txt_array["subform3"]["TEXT01_02_04"]      = array("gyo" => 1,  "moji" => 20, "title" => "その他");
        $txt_array["subform3"]["TEXT02_03_00"]      = array("gyo" => 2,  "moji" => 40, "title" => "その他の配慮・管理事項");

        $txt_array["subform4"]["TEXT01_02_01"]      = array("gyo" => 1,  "moji" => 20, "title" => "原因");
        $txt_array["subform4"]["TEXT01_02_06"]      = array("gyo" => 1,  "moji" => 20, "title" => "その他");
        $txt_array["subform4"]["TEXT01_04_03"]      = array("gyo" => 1,  "moji" => 20, "title" => "その他");
        $txt_array["subform4"]["TEXT01_04_03"]      = array("gyo" => 1,  "moji" => 20, "title" => "内服薬");
        $txt_array["subform4"]["TEXT01_04_03"]      = array("gyo" => 1,  "moji" => 20, "title" => "エピペン");
        $txt_array["subform4"]["TEXT02_01_02"]      = array("gyo" => 2,  "moji" => 40, "title" => "保護者と相談し決定");
        $txt_array["subform4"]["TEXT02_02_02"]      = array("gyo" => 2,  "moji" => 40, "title" => "保護者と相談し決定");
        $txt_array["subform4"]["TEXT02_03_02"]      = array("gyo" => 2,  "moji" => 40, "title" => "保護者と相談し決定");
        $txt_array["subform4"]["TEXT02_04_02"]      = array("gyo" => 2,  "moji" => 40, "title" => "保護者と相談し決定");
        $txt_array["subform4"]["TEXT02_05_00"]      = array("gyo" => 7,  "moji" => 25, "title" => "医師記入欄");
        $txt_array["subform4"]["TEXT02_06_00"]      = array("gyo" => 7,  "moji" => 40, "title" => "診断のきっかけ");
        $txt_array["subform4"]["DETAIL01_03_06"]    = array("gyo" => 1,  "moji" => 20, "title" => "種実類・木の実類");
        $txt_array["subform4"]["DETAIL01_03_07"]    = array("gyo" => 1,  "moji" => 20, "title" => "甲殻類");
        $txt_array["subform4"]["DETAIL01_03_08"]    = array("gyo" => 1,  "moji" => 20, "title" => "果物類");
        $txt_array["subform4"]["DETAIL01_03_09"]    = array("gyo" => 1,  "moji" => 20, "title" => "魚類");
        $txt_array["subform4"]["DETAIL01_03_10"]    = array("gyo" => 1,  "moji" => 20, "title" => "肉類");
        $txt_array["subform4"]["DETAIL01_03_11"]    = array("gyo" => 1,  "moji" => 20, "title" => "その他１");
        $txt_array["subform4"]["DETAIL01_03_12"]    = array("gyo" => 1,  "moji" => 20, "title" => "その他２");

        $txt_array["subform6"]["TEXT01_01_02"]      = array("gyo" => 1,  "moji" => 20, "title" => "主な症状の時期");
        $txt_array["subform6"]["TEXT01_02_03"]      = array("gyo" => 1,  "moji" => 20, "title" => "その他");
        $txt_array["subform6"]["TEXT02_02_00"]      = array("gyo" => 7,  "moji" => 25, "title" => "その他の配慮・管理事項");

        $sonota["EMERGENCYNAME"]        = array("gyo" => 1,  "moji" => 10, "title" => "（緊急時連絡先）保護者氏名");
        $sonota["EMERGENCYTELNO"]       = array("gyo" => 0,  "moji" => 14, "title" => "（緊急時連絡先）保護者電話");
        $sonota["EMERGENCYNAME2"]       = array("gyo" => 1,  "moji" => 40, "title" => "（緊急時連絡先）医療機関名");
        $sonota["EMERGENCYTELNO2"]      = array("gyo" => 0,  "moji" => 14, "title" => "（緊急時連絡先）医療機関電話");
        $sonota["DOCTOR"]               = array("gyo" => 1,  "moji" => 10, "title" => "医師名");
        $sonota["HOSPITAL"]             = array("gyo" => 1,  "moji" => 40, "title" => "医療機関名");
        $sonota["REMARK"]               = array("gyo" => 17, "moji" => 40, "title" => "緊急時対応プラン");

        //文字数チェック
        foreach ($txt_array[$cmd] as $key => $val) {
            if ($this->validate_row_cnt($this->field[$key], ($val["moji"] * 2)) > $val["gyo"]) {
                if ($val["gyo"] == 1) {
                    $this->setWarning("MSG915","({$val["title"]}は{$val["moji"]}文字までです)");
                } else {
                    $this->setWarning("MSG915","({$val["title"]}は{$val["gyo"]}行までです)");
                }
                return false;
            }
        }

        //文字数チェック
        foreach ($sonota as $key => $val) {
            if ($val["gyo"] == 0) {
                if (strlen($this->field[$key]) > $val["moji"]) {
                    $this->setWarning("MSG915","({$val["title"]}は{$val["moji"]}バイトまでです)");
                    return false;
                }
            } else {
                if ($this->validate_row_cnt($this->field[$key], ($val["moji"] * 2)) > $val["gyo"]) {
                    if ($val["gyo"] == 1) {
                        $this->setWarning("MSG915","({$val["title"]}は{$val["moji"]}文字までです)");
                    } else {
                        $this->setWarning("MSG915","({$val["title"]}は{$val["gyo"]}行までです)");
                    }
                    return false;
                }
            }
        }

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        //必須チェック
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return;
        }

        //文字数チェック
        if ($this->cmd == "update2" && ($this->validate_row_cnt($this->field["DIAGNOSIS_NAME"], (50 * 2)) > 1)) {
            $this->setWarning("MSG915","(診断名は50文字までです)");
            return;
        }

        //更新
        knjf323Query::getUpdateQuery($this, $this->field);
        $this->setMessage("MSG201");

        return;
    }


    //更新用のモデルを返します
    function &getSubformUpdateModel() {
        $cmd = preg_split("{_}", $this->cmd);

        if (!$this->validateInput($cmd[0])) {
            $this->cmd = $cmd[0]."A";
            return;
        }

        $care_div = sprintf("%02d", substr($cmd[0],7,1));

        knjf323Query::getSubUpdateQuery($this, $this->field, $care_div);
        $this->cmd = $cmd[0]."A";
        $this->setMessage("MSG201");

        return;
    }

    //コピー用のモデルを返します
    function &getSubformCopyModel() {
        $cmd = preg_split("{_}", $this->cmd);
        $care_div = sprintf("%02d", substr($cmd[0],7,1));

        $db = Query::dbCheckOut();

        //対象年度が処理年度ではない場合、エラーを返す
        if ($this->field["YEAR"] != CTRL_YEAR) {
            $this->setWarning("MSG300");
            $this->cmd = $cmd[0]."A";
            return;
        }

        //データ件数
        $pre_year = $this->field["YEAR"] - 1;
        $cntH = get_count($db->getCol(knjf323Query::getCareHData($this, $pre_year, $care_div)));
        $cntD = get_count($db->getCol(knjf323Query::getCareData($this, $pre_year, $care_div)));

        //前年度のデータがない場合、エラーを返す
        if ($cntH == 0 && $cntD == 0) {
            $this->setWarning("MSG303");
            $this->cmd = $cmd[0]."A";
            return;
        }

        //コピー
        knjf323Query::getSubCopyQuery($db, $this, $care_div);
        $this->cmd = $cmd[0]."A";
        $this->setMessage("MSG201");

        Query::dbCheckIn($db);
        return;
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダー
        $header = array();
        $header["SCHREGNO"]             = "学籍番号";
        $header["ATTENDNO"]             = "年組番号";
        $header["NAME"]                 = "生徒氏名";
        $header["SEX"]                  = "性別";
        $header["BIRTHDAY"]             = "生年月日";

        $header["01-CARE_FLG"]          = "気管支喘息有無";
        $header["01-CHECK01-01"]        = "重症度分類（発作型）";
        $header["01-CHECK01-02"]        = "長期管理薬（吸入薬）";
        $header["01-CHECK01-03"]        = "長期管理薬（内服薬・貼付薬）";
        $header["01-CHECK01-04"]        = "急性発作治療薬";
        $header["01-TEXT01-05"]         = "急性発作時の対応";
        $header["01-CHECK02-01"]        = "運動（体育・部活動等）";
        $header["01-CHECK02-02"]        = "動物との接触やホコリ等の舞う環境での活動";
        $header["01-CHECK02-03"]        = "宿泊を伴う校外活動";
        $header["01-TEXT02-04"]         = "気管支喘息その他の配慮・管理事項";
        $header["01-EMERGENCYNAME"]     = "気管支喘息緊急連絡先保護者氏名";
        $header["01-EMERGENCYTELNO"]    = "気管支喘息緊急連絡先保護者電話番号";
        $header["01-EMERGENCYNAME2"]    = "気管支喘息緊急連絡先医療機関名";
        $header["01-EMERGENCYTELNO2"]   = "気管支喘息緊急連絡先医療機関電話番号";
        $header["01-DATE"]              = "気管支喘息記載日";
        $header["01-DOCTOR"]            = "気管支喘息医師名";
        $header["01-HOSPITAL"]          = "気管支喘息医療機関名";

        $header["02-CARE_FLG"]          = "アトピー性皮膚炎有無";
        $header["02-CHECK01-01"]        = "重症度のめやす";
        $header["02-CHECK01-02"]        = "常用する外用薬";
        $header["02-CHECK01-03"]        = "常用する内用薬";
        $header["02-CHECK01-04"]        = "食物アレルギーの合併";
        $header["02-CHECK02-01"]        = "プール指導及び長時間の紫外線下での活動";
        $header["02-CHECK02-02"]        = "動物との接触";
        $header["02-CHECK02-03"]        = "発汗後";
        $header["02-TEXT02-04"]         = "アトピー性皮膚炎その他の配慮・管理事項";
        $header["02-DATE"]              = "アトピー性皮膚炎記載日";
        $header["02-DOCTOR"]            = "アトピー性皮膚炎医師名";
        $header["02-HOSPITAL"]          = "アトピー性皮膚炎医療機関名";

        $header["03-CARE_FLG"]          = "アレルギー性結膜炎有無";
        $header["03-CHECK01-01"]        = "病型";
        $header["03-CHECK01-02"]        = "治療";
        $header["03-CHECK02-01"]        = "プール指導";
        $header["03-CHECK02-02"]        = "屋外活動";
        $header["03-TEXT02-03"]         = "アレルギー性結膜炎その他の配慮・管理事項";
        $header["03-DATE"]              = "アレルギー性結膜炎記載日";
        $header["03-DOCTOR"]            = "アレルギー性結膜炎医師名";
        $header["03-HOSPITAL"]          = "アレルギー性結膜炎医療機関名";

        $header["04-CARE_FLG"]          = "食物アレルギー有無";
        $header["05-CARE_FLG"]          = "アナフィラキシー有無";
        $header["04-CHECK01-01"]        = "食物アレルギー病型";
        $header["04-CHECK01-02"]        = "アナフィラキシー病型";
        $header["04-CHECK01-03"]        = "原因食物・診断根拠";
        $header["04-CHECK01-04"]        = "緊急時に備えた処方薬";
        $header["04-CHECK02-01"]        = "給食";
        $header["04-CHECK02-02"]        = "食物・食材を扱う授業・活動";
        $header["04-CHECK02-03"]        = "運動（体育・部活動）";
        $header["04-CHECK02-04"]        = "宿泊を伴う校外活動";
        $header["04-TEXT02-05"]         = "食物アレルギー・アナフィラキシーその他の配慮・管理事項";
        $header["04-EMERGENCYNAME"]     = "食物アレルギー・アナフィラキシー緊急連絡先保護者氏名";
        $header["04-EMERGENCYTELNO"]    = "食物アレルギー・アナフィラキシー緊急連絡先保護者電話番号";
        $header["04-EMERGENCYNAME2"]    = "食物アレルギー・アナフィラキシー緊急連絡先医療機関名";
        $header["04-EMERGENCYTELNO2"]   = "食物アレルギー・アナフィラキシー緊急連絡先医療機関電話番号";
        $header["04-DATE"]              = "食物アレルギー・アナフィラキシー記載日";
        $header["04-DOCTOR"]            = "食物アレルギー・アナフィラキシー医師名";
        $header["04-HOSPITAL"]          = "食物アレルギー・アナフィラキシー医療機関名";

        $header["06-CARE_FLG"]          = "アレルギー性鼻炎有無";
        $header["06-CHECK01-01"]        = "病型";
        $header["06-CHECK01-02"]        = "治療";
        $header["06-CHECK02-01"]        = "屋外活動";
        $header["06-TEXT02-02"]         = "アレルギー性鼻炎その他の配慮・管理事項";
        $header["06-DATE"]              = "アレルギー性鼻炎記載日";
        $header["06-DOCTOR"]            = "アレルギー性鼻炎医師名";
        $header["06-HOSPITAL"]          = "アレルギー性鼻炎医療機関名";

        //選択項目名一覧
        $label = array();
        $label["01-CHECK01-01-01"]      = "間欠型";
        $label["01-CHECK01-01-02"]      = "軽症持続型";
        $label["01-CHECK01-01-03"]      = "中等症持続型";
        $label["01-CHECK01-01-04"]      = "重症持続型";
        $label["01-CHECK01-02-01"]      = "ステロイド吸入薬";
        $label["01-CHECK01-02-02"]      = "長時間作用性吸入ベータ刺激薬";
        $label["01-CHECK01-02-03"]      = "吸入抗アレルギー薬";
        $label["01-CHECK01-02-04"]      = "その他";
        $label["01-CHECK01-03-01"]      = "テオフィリン徐放製剤";
        $label["01-CHECK01-03-02"]      = "ロイコトリエン受容体拮抗薬";
        $label["01-CHECK01-03-03"]      = "ベータ刺激内服薬・貼付薬";
        $label["01-CHECK01-03-04"]      = "その他";
        $label["01-CHECK01-04-01"]      = "ベータ刺激薬吸入";
        $label["01-CHECK01-04-02"]      = "ベータ刺激薬内服";
        $label["01-CHECK02-01-01"]      = "管理不要";
        $label["01-CHECK02-01-02"]      = "保護者と相談し決定";
        $label["01-CHECK02-01-03"]      = "強い運動は不可";
        $label["01-CHECK02-02-01"]      = "配慮不要";
        $label["01-CHECK02-02-02"]      = "保護者と相談し決定";
        $label["01-CHECK02-02-03"]      = "動物へのアレルギーが強いため不可";
        $label["01-CHECK02-03-01"]      = "配慮不要";
        $label["01-CHECK02-03-02"]      = "保護者と相談し決定";

        $label["02-CHECK01-01-01"]      = "軽症";
        $label["02-CHECK01-01-02"]      = "中等症";
        $label["02-CHECK01-01-03"]      = "重症";
        $label["02-CHECK01-01-04"]      = "最重症";
        $label["02-CHECK01-02-01"]      = "ステロイド軟膏";
        $label["02-CHECK01-02-02"]      = "タクロリムス軟膏";
        $label["02-CHECK01-02-03"]      = "保湿剤";
        $label["02-CHECK01-02-04"]      = "その他";
        $label["02-CHECK01-03-01"]      = "抗ヒスタミン薬";
        $label["02-CHECK01-03-02"]      = "その他";
        $label["02-CHECK01-04-01"]      = "あり";
        $label["02-CHECK01-04-02"]      = "なし";
        $label["02-CHECK02-01-01"]      = "管理不要";
        $label["02-CHECK02-01-02"]      = "保護者と相談し決定";
        $label["02-CHECK02-02-01"]      = "配慮不要";
        $label["02-CHECK02-02-02"]      = "保護者と相談し決定";
        $label["02-CHECK02-02-03"]      = "動物へのアレルギーが強いため不可";
        $label["02-CHECK02-03-01"]      = "配慮不要";
        $label["02-CHECK02-03-02"]      = "保護者と相談し決定";
        $label["02-CHECK02-03-03"]      = "夏季シャワー浴";

        $label["03-CHECK01-01-01"]      = "通年性アレルギー性結膜炎";
        $label["03-CHECK01-01-02"]      = "季節性アレルギー性結膜炎";
        $label["03-CHECK01-01-03"]      = "春季カタル";
        $label["03-CHECK01-01-04"]      = "アトピー性角結膜炎";
        $label["03-CHECK01-01-05"]      = "その他";
        $label["03-CHECK01-02-01"]      = "抗アレルギー点眼薬";
        $label["03-CHECK01-02-02"]      = "ステロイド点眼薬";
        $label["03-CHECK01-02-03"]      = "免疫抑制点眼薬";
        $label["03-CHECK01-02-04"]      = "その他";
        $label["03-CHECK02-01-01"]      = "管理不要";
        $label["03-CHECK02-01-02"]      = "保護者と相談し決定";
        $label["03-CHECK02-01-03"]      = "プールへの入水不可";
        $label["03-CHECK02-02-01"]      = "管理不要";
        $label["03-CHECK02-02-02"]      = "保護者と相談し決定";

        $label["04-CHECK01-01-01"]      = "即時型";
        $label["04-CHECK01-01-02"]      = "口腔アレルギー症候群";
        $label["04-CHECK01-01-03"]      = "食物依存性運動誘発アナフィラキシー";
        $label["04-CHECK01-02-01"]      = "食物";
        $label["04-CHECK01-02-02"]      = "食物依存性運動誘発アナフィラキシー";
        $label["04-CHECK01-02-03"]      = "運動誘発アナフィラキシー";
        $label["04-CHECK01-02-04"]      = "昆虫";
        $label["04-CHECK01-02-05"]      = "医薬品";
        $label["04-CHECK01-02-06"]      = "その他";
        $label["04-CHECK01-03-01"]      = "鶏卵";
        $label["04-CHECK01-03-02"]      = "牛乳・乳製品";
        $label["04-CHECK01-03-03"]      = "小麦";
        $label["04-CHECK01-03-04"]      = "ソバ";
        $label["04-CHECK01-03-05"]      = "ピーナッツ";
        $label["04-CHECK01-03-06"]      = "種実類・木の実類";
        $label["04-CHECK01-03-07"]      = "甲殻類";
        $label["04-CHECK01-03-08"]      = "果物類";
        $label["04-CHECK01-03-09"]      = "魚類";
        $label["04-CHECK01-03-10"]      = "肉類";
        $label["04-CHECK01-03-11"]      = "その他１";
        $label["04-CHECK01-03-12"]      = "その他２";
        $label["04-CHECK01-04-01"]      = "内服薬";
        $label["04-CHECK01-04-02"]      = "アドレナリン自己注射薬";
        $label["04-CHECK01-04-03"]      = "その他";
        $label["04-CHECK02-01-01"]      = "管理不要";
        $label["04-CHECK02-01-02"]      = "保護者と相談し決定";
        $label["04-CHECK02-02-01"]      = "配慮不要";
        $label["04-CHECK02-02-02"]      = "保護者と相談し決定";
        $label["04-CHECK02-03-01"]      = "管理不要";
        $label["04-CHECK02-03-02"]      = "保護者と相談し決定";
        $label["04-CHECK02-04-01"]      = "配慮不要";
        $label["04-CHECK02-04-02"]      = "食事やイベントの際に配慮が必要";

        $label["06-CHECK01-01-01"]      = "通年性アレルギー性鼻炎";
        $label["06-CHECK01-01-02"]      = "季節性アレルギー性鼻炎";
        $label["06-CHECK01-02-01"]      = "抗ヒスタミン薬・抗アレルギー薬";
        $label["06-CHECK01-02-02"]      = "鼻噴霧用ステロイド薬";
        $label["06-CHECK01-02-03"]      = "その他";
        $label["06-CHECK02-01-01"]      = "管理不要";
        $label["06-CHECK02-01-02"]      = "保護者と相談し決定";

        //ヘッダー出力
        $contents  = "";    //クリア
        $contents .= implode($header, ",")."\n";

        //出力フラグ
        $header_data = false;

        //データセット
        $setData = array();
        $schregno = $care_div = "";
        $query = knjf323Query::selectMainQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(","   , "." , $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\r\n", "↓", $row);
            $row = str_replace("\r"  , "↓", $row);
            $row = str_replace("\n"  , "↓", $row);

            if (!$schregno) {
                foreach (array("SCHREGNO","ATTENDNO","NAME","SEX","BIRTHDAY") as $key) {
                    if ($key == "BIRTHDAY") {
                        $setData[$key] = str_replace("-", "/", $row[$key]);
                    } else {
                        $setData[$key] = $row[$key];
                    }
                }
            }

            if ($care_div != $row["CARE_DIV"]) {
                foreach (array("CARE_DIV","CARE_FLG","EMERGENCYNAME","EMERGENCYTELNO","EMERGENCYNAME2","EMERGENCYTELNO2","DATE","DOCTOR","HOSPITAL","REMARK") as $key) {
                    if ($key == "DATE") {
                        $setData[$row["CARE_DIV"]."-".$key] = str_replace("-", "/", $row[$key]);
                    } else {
                        $setData[$row["CARE_DIV"]."-".$key] = $row[$key];
                    }
                }
            }

            if (!in_array($row["CARE_DIV"]."-".$row["CARE_KIND"]."-".$row["CARE_ITEM"]."-".$row["CARE_SEQ"], array('04-01-04-04', '04-01-04-05'))) {

                $setData[$row["CARE_DIV"]."-CHECK".$row["CARE_KIND"]."-".$row["CARE_ITEM"]][$row["CARE_SEQ"]] = $label[$row["CARE_DIV"]."-CHECK".$row["CARE_KIND"]."-".$row["CARE_ITEM"]."-".$row["CARE_SEQ"]];

                if (strlen($row["CARE_REMARK1"]) > 0) {
                    if ($row["CARE_DIV"]."-".$row["CARE_KIND"]."-".$row["CARE_ITEM"] == '04-01-03') {
                        //診断根拠
                        $array = explode('.', $row["CARE_REMARK1"]);
                        if (in_array(1, $array)) $setData[$row["CARE_DIV"]."-CHECK".$row["CARE_KIND"]."-".$row["CARE_ITEM"]][$row["CARE_SEQ"]] .= "①";
                        if (in_array(2, $array)) $setData[$row["CARE_DIV"]."-CHECK".$row["CARE_KIND"]."-".$row["CARE_ITEM"]][$row["CARE_SEQ"]] .= "②";
                        if (in_array(3, $array)) $setData[$row["CARE_DIV"]."-CHECK".$row["CARE_KIND"]."-".$row["CARE_ITEM"]][$row["CARE_SEQ"]] .= "③";
                    } else if ($row["CARE_SEQ"] == "00") {
                        $setData[$row["CARE_DIV"]."-TEXT".$row["CARE_KIND"]."-".$row["CARE_ITEM"]] = $row["CARE_REMARK1"];
                    } else {
                        if ($row["CARE_DIV"]."-".$row["CARE_KIND"]."-".$row["CARE_ITEM"] == '06-01-01') {
                            $row["CARE_REMARK1"] = str_replace(".", "、", $row["CARE_REMARK1"]);
                        }
                        $word = ($row["CARE_DIV"]."-".$row["CARE_KIND"]."-".$row["CARE_ITEM"]."-".$row["CARE_SEQ"] == '04-01-02-01') ? "原因 " : "";
                        $setData[$row["CARE_DIV"]."-CHECK".$row["CARE_KIND"]."-".$row["CARE_ITEM"]][$row["CARE_SEQ"]] .= "(".$word.$row["CARE_REMARK1"].")";
                    }
                }

                if (strlen($row["CARE_REMARK2"]) > 0) $setData[$row["CARE_DIV"]."-CHECK".$row["CARE_KIND"]."-".$row["CARE_ITEM"]][$row["CARE_SEQ"]] .= "(".$row["CARE_REMARK2"].")";

            }

            $schregno = $row["SCHREGNO"];
            $care_div = $row["CARE_DIV"];
            $header_data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //データ出力
        $sep1 = "";
        foreach ($header as $key => $val) {
            if (is_array($setData[$key])) {
                $contents .= $sep1;
                $sep2 = "";
                foreach ($setData[$key] as $dkey => $dval) {
                    $contents .= $sep2.$dval;
                    $sep2 = "、";
                }
            } else {
                $field = explode('-', $key);
                if ($field[1] == "CARE_FLG") {
                    $setData[$key] = ($setData[$key] == "1") ? "あり" : "なし";
                }
                $contents .= $sep1.$setData[$key];
            }
            $sep1 = ",";
        }

        $contents .= "\n";

        //ＣＳＶ出力
        if ($header_data) {
            $wrk_nendo = common::DateConv1(str_replace("-","/",CTRL_DATE),10);
            $couse = $wrk_nendo."　アレルギー疾患学校生活管理指導(".$this->schregno.")";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
