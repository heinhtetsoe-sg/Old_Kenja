<?php

require_once('for_php7.php');
class knji100c_01Model extends Model
{

    public $cmd;
    public $schregno;                  //学籍番号
    public $control_data = array();    //コントロールマスタデータを保持
    public $item;
    public $selectdata_l;              //対象生徒
    public $selectdata_r;              //書出し項目一覧
    public $koteidata;                 //CSV項目(固定)
    public $mode;                      //生徒区分（在校生・卒業生）
    public $z010Name;                  //学校判別名称

    public $getPrgId;
    public $sendAuth;
    public $auth;

    public function knja150Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }

        $addr1Title = $this->Properties["useAddrField2"] == "1" ? "１" : "";

        //CSV固定表示
        $this->kotei = array();
        $this->kotei[]["SCHREGNO"]              = "学籍番号";
        $this->kotei[]["NAME"]                  = "生徒氏名";
        $this->kotei[]["SEX"]                   = "性別区分(*)";
        $this->kotei[]["GRADE"]                 = "学年";
        $this->kotei[]["HR_CLASS"]              = "組";

        //基礎情報
        $this->item = array();
        $this->item[]["INOUTCD"]                = "内外区分(*)";
        $this->item[]["NAME_SHOW"]              = "生徒氏名表示用";
        $this->item[]["NAME_KANA"]              = "生徒氏名かな";
        $this->item[]["NAME_ENG"]               = "生徒氏名英字";
        $this->item[]["REAL_NAME"]              = "生徒戸籍氏名";
        $this->item[]["REAL_NAME_KANA"]         = "生徒戸籍氏名かな";
        $this->item[]["BIRTHDAY"]               = "生年月日";
        $this->item[]["HANDICAP"]               = "その他(身体状態)";
        $this->item[]["BLOODTYPE"]              = "血液型";
        $this->item[]["BLOOD_RH"]               = "血液型(RH)";
        $this->item[]["PRISCHOOLCD"]            = "塾(*)";
        $this->item[]["PRISCHOOL_CLASS_CD"]     = "教室名(*)";
        if ($this->Properties["Origin_hyouji"] == "1") {
            $this->item[]["ORIGIN"]                 = "出身";
        }
        $this->item[]["FINSCHOOLCD"]            = "出身学校(*)";
        $this->item[]["FINISH_DATE"]            = "出身校卒業日付";
        $this->item[]["ENT_DATE"]               = "入学日付";
        $this->item[]["ENT_DIV"]                = "入学区分(*)";
        $this->item[]["ENT_REASON"]             = "入学事由";
        $this->item[]["ENT_SCHOOL"]             = "入学学校名";
        $this->item[]["ENT_ADDR"]               = "入学学校住所".$addr1Title;
        if ($this->Properties["useAddrField2"] == "1") {
            $this->item[]["ENT_ADDR2"]              = "入学学校住所２";
        }
        $this->item[]["GRD_DATE"]               = "卒業(除籍)日付";
        $this->item[]["GRD_DIV"]                = "卒業(除籍)区分(*)";
        $this->item[]["GRD_REASON"]             = "卒業(除籍)事由";
        $this->item[]["GRD_SCHOOL"]             = "卒業(除籍)学校名";
        $this->item[]["GRD_ADDR"]               = "卒業(除籍)学校住所".$addr1Title;
        if ($this->Properties["useAddrField2"] == "1") {
            $this->item[]["GRD_ADDR2"]              = "卒業(除籍)学校住所２";
        }
        $this->item[]["GRD_NO"]                 = "卒業生台帳番号";
        $this->item[]["GRD_TERM"]               = "卒業期";
        $this->item[]["REMARK1"]                = "備考１";
        $this->item[]["REMARK2"]                = "備考２";
        $this->item[]["REMARK3"]                = "備考３";
        $this->item[]["EMERGENCYCALL"]          = "急用連絡先";
        $this->item[]["EMERGENCYNAME"]          = "急用連絡先名";
        $this->item[]["EMERGENCYRELA_NAME"]     = "急用連絡先続柄";
        $this->item[]["EMERGENCYTELNO"]         = "急用電話番号";
        $this->item[]["EMERGENCYCALL2"]         = "急用連絡先２";
        $this->item[]["EMERGENCYNAME2"]         = "急用連絡先名２";
        $this->item[]["EMERGENCYRELA_NAME2"]    = "急用連絡先続柄２";
        $this->item[]["EMERGENCYTELNO2"]        = "急用電話番号２";
        $this->item[]["BIBOUROKU"]              = "備忘録";
        //在籍情報
        $this->item[]["ATTENDNO"]               = "出席番号";
        $this->item[]["ANNUAL"]                 = "年次";
        $this->item[]["COURSECD"]               = "課程";
        $this->item[]["MAJORCD"]                = "学科";
        $this->item[]["COURSECODE"]             = "コース";
        $this->item[]["HR_NAME"]                = "年組名称";
        $this->item[]["STAFFNAME"]              = "担任";
        $this->item[]["ISSUEDATE"]              = "有効期間開始日付";
        $this->item[]["EXPIREDATE"]             = "有効期間終了日付";
        $this->item[]["ZIPCD"]                  = "現住所郵便番号";
        $this->item[]["AREACD"]                 = "地区(*)";
        $this->item[]["ADDR1"]                  = "現住所１";
        $this->item[]["ADDR2"]                  = "現住所２";
        $this->item[]["ADDR1_ENG"]              = "現住所１(英字)";
        $this->item[]["ADDR2_ENG"]              = "現住所２(英字)";
        $this->item[]["ADDR_FLG"]               = "方書の出力可";
        $this->item[]["TELNO"]                  = "現住所電話番号";
        $this->item[]["FAXNO"]                  = "現住所FAX番号";
        $this->item[]["EMAIL"]                  = "現住所E-MAILアドレス";
        //保護者情報
        $this->item[]["RELATIONSHIP"]           = "保護者との続柄(*)";
        $this->item[]["GUARD_NAME"]             = "保護者氏名";
        $this->item[]["GUARD_KANA"]             = "保護者氏名かな";
        $this->item[]["GUARD_REAL_NAME"]        = "保護者戸籍氏名";
        $this->item[]["GUARD_REAL_KANA"]        = "保護者戸籍氏名かな";
        $this->item[]["GUARD_SEX"]              = "保護者性別区分(*)";
        $this->item[]["GUARD_BIRTHDAY"]         = "保護者生年月日";
        $this->item[]["GUARD_ISSUEDATE"]        = "有効期間開始日付";
        $this->item[]["GUARD_EXPIREDATE"]       = "有効期間終了日付";
        $this->item[]["GUARD_ZIPCD"]            = "保護者郵便番号";
        $this->item[]["GUARD_ADDR1"]            = "保護者住所１";
        $this->item[]["GUARD_ADDR2"]            = "保護者住所２";
        $this->item[]["GUARD_ADDR_FLG"]         = "方書の出力可";
        $this->item[]["GUARD_TELNO"]            = "保護者電話番号";
        $this->item[]["GUARD_FAXNO"]            = "保護者FAX番号";
        $this->item[]["GUARD_E_MAIL"]           = "保護者E-MAILアドレス";
        $this->item[]["GUARD_JOBCD"]            = "保護者職種(*)";
        $this->item[]["GUARD_WORK_NAME"]        = "保護者勤務先名称";
        $this->item[]["GUARD_WORK_TELNO"]       = "保護者勤務先電話番号";
        //保護者情報２
        if ($this->Properties["useGuardian2"] == "1") {
            $this->item[]["GUARD2_RELATIONSHIP"]    = "保護者２との続柄(*)";
            $this->item[]["GUARD2_NAME"]            = "保護者２氏名";
            $this->item[]["GUARD2_KANA"]            = "保護者２氏名かな";
            $this->item[]["GUARD2_REAL_NAME"]       = "保護者２戸籍氏名";
            $this->item[]["GUARD2_REAL_KANA"]       = "保護者２戸籍氏名かな";
            $this->item[]["GUARD2_SEX"]             = "保護者２性別区分(*)";
            $this->item[]["GUARD2_BIRTHDAY"]        = "保護者２生年月日";
            $this->item[]["GUARD2_ISSUEDATE"]       = "保護者２有効期間開始日付";
            $this->item[]["GUARD2_EXPIREDATE"]      = "保護者２有効期間終了日付";
            $this->item[]["GUARD2_ZIPCD"]           = "保護者２郵便番号";
            $this->item[]["GUARD2_ADDR1"]           = "保護者２住所１";
            $this->item[]["GUARD2_ADDR2"]           = "保護者２住所２";
            $this->item[]["GUARD2_ADDR_FLG"]        = "保護者２方書の出力可";
            $this->item[]["GUARD2_TELNO"]           = "保護者２電話番号";
            $this->item[]["GUARD2_FAXNO"]           = "保護者２FAX番号";
            $this->item[]["GUARD2_E_MAIL"]          = "保護者２E-MAILアドレス";
            $this->item[]["GUARD2_JOBCD"]           = "保護者２職種(*)";
            $this->item[]["GUARD2_WORK_NAME"]       = "保護者２勤務先名称";
            $this->item[]["GUARD2_WORK_TELNO"]      = "保護者２勤務先電話番号";
        }
        //保証人情報
        $this->item[]["GUARANTOR_RELATIONSHIP"] = "保証人との続柄(*)";
        $this->item[]["GUARANTOR_NAME"]         = "保証人氏名";
        $this->item[]["GUARANTOR_KANA"]         = "保証人氏名かな";
        $this->item[]["GUARANTOR_REAL_NAME"]    = "保証人戸籍氏名";
        $this->item[]["GUARANTOR_REAL_KANA"]    = "保証人戸籍氏名かな";
        $this->item[]["GUARANTOR_SEX"]          = "保証人性別区分(*)";
        $this->item[]["GUARANTOR_ZIPCD"]        = "保証人郵便番号";
        $this->item[]["GUARANTOR_ADDR1"]        = "保証人住所１";
        $this->item[]["GUARANTOR_ADDR2"]        = "保証人住所２";
        $this->item[]["GUARANTOR_TELNO"]        = "保証人電話番号";
        $this->item[]["GUARANTOR_JOBCD"]        = "保証人職種(*)";
        $this->item[]["PUBLIC_OFFICE"]          = "兼ねている公職";
        $this->item[]["NOT_SENDER"]             = "非送付者";
        
        $this->item[]["EXAMNO"]                 = "受験番号";
        $this->item[]["TESTDIV"]                = "入試区分";

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

        if (VARS::get("SEND_PRGID")) {
            unset($this->subsystem);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }

        $this->getPrgId = VARS::get("SEND_PRGID");
        $this->sendAuth = VARS::get("SEND_AUTH");
        $this->auth     = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //住所１分割
        $this->addr1_div = VARS::post("ADDR1_DIV");
        //氏名分割
        $this->name_div = VARS::post("NAME_DIV");

        //サブシステム
        if (VARS::get("SUBSYSTEM")) {
            $this->subsystem        = VARS::get("SUBSYSTEM");
        }
        //出力設定
        if (VARS::get("OUTPUT")) {
            $this->output           = VARS::get("OUTPUT");
        }
        //学校判別名称
        $db = Query::dbCheckOut();
        $this->z010Name = $db->getOne(knji100c_01Query::getZ010Name1());
        Query::dbCheckIn($db);
        if ($this->z010Name == "meikei" || $this->Properties["usedormitory_hyouji"] == "1") {
            $this->item[]["DOMITORY"] = "寮生";
        }
    }

    //CSV書出し
    public function &getCsvModel()
    {
        $db           = Query::dbCheckOut();
        $result       = $db->query(knji100c_01Query::selectUngrdQuery($this));
        $this->koteidata = "";
        foreach ($this->kotei as $key => $val) {
            foreach ($val as $field => $label) {
                $this->koteidata = $this->koteidata.$field.",";
            }
        }
        $item = explode(",", $this->selectdata_r);
        $query = knji100c_01Query::getDelFieldSql($this);
        $db->query($query);
        $setSeq = 1;
        foreach ($item as $key => $val) {
            $query = knji100c_01Query::getFieldInsSql($this, $setSeq, $val);
            $db->query($query);
            $setSeq++;
        }
        $this->selectdata_r = $this->koteidata.$this->selectdata_r;
        $item = explode(",", $this->selectdata_r);

        //住所１の分割出力
        //住所１と郵便番号の対応項目一覧
        $zipcdList = array("ADDR1"           => "ZIPCD",
                           "GUARD_ADDR1"     => "GUARD_ZIPCD",
                           "GUARD2_ADDR1"    => "GUARD2_ZIPCD",
                           "GUARANTOR_ADDR1" => "GUARANTOR_ZIPCD");
        //住所１と住所２の対応項目一覧
        $addrArray = array("ADDR1"           => "ADDR2",
                           "GUARD_ADDR1"     => "GUARD_ADDR2",
                           "GUARD2_ADDR1"    => "GUARD2_ADDR2",
                           "GUARANTOR_ADDR1" => "GUARANTOR_ADDR2");
        //住所１と住所２のITEM指定判定
        $addr2_div = array();
        foreach ($addrArray as $addr1 => $addr2) {
            //ITEM：住所１と住所２
            if (in_array($addr1, $item) && in_array($addr2, $item)) {
                $addr2_div[] = $addr1;
            }
        }

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
        $schoolkind = $db->getCol(knji100c_01Query::getSchoolKind());
        //ヘッダ作成
        //固定項目+画面からの選択項目
        for ($i = 0; $i < get_count($item); $i++) {
            if ($this->output == OUT_CODE_NAME) {    //コード＋名称
                $codePrefix = str_replace("(*)", "", $item_array[$item[$i]]);
                switch ($item[$i]) {
                    case "INOUTCD":
                    case "SEX":
                    case "PRISCHOOLCD":
                    case "PRISCHOOL_CLASS_CD":
                    case "AREACD":
                    case "RELATIONSHIP":
                    case "GUARD_SEX":
                    case "GUARD_JOBCD":
                    case "GUARD2_RELATIONSHIP":
                    case "GUARD2_SEX":
                    case "GUARD2_JOBCD":
                    case "GUARANTOR_RELATIONSHIP":
                    case "GUARANTOR_SEX":
                    case "GUARANTOR_JOBCD":
                    case "COURSECD":
                    case "MAJORCD":
                    case "COURSECODE":
                    case "ORIGIN":
                    case "TESTDIV":
                        $contents .= $sep .$codePrefix."コード,".$item_array[$item[$i]];
                        break;
                    case "ENT_DIV":
                    case "GRD_DIV":
                        for ($j = 0; $j < get_count($schoolkind); $j++) {
                            $contents .= $sep .$schoolkind[$j].$codePrefix."コード,".$schoolkind[$j].$item_array[$item[$i]];
                            $sep = ",";
                        }
                        break;
                    case "FINSCHOOLCD":
                        for ($j = 0; $j < get_count($schoolkind); $j++) {
                            $fin_schoolkind = "";
                            switch ($schoolkind[$j]) {
                                case "P":
                                    $fin_schoolkind = 'K';
                                    break;
                                case "J":
                                    $fin_schoolkind = 'P';
                                    break;
                                case "H":
                                    $fin_schoolkind = 'J';
                                    break;
                            }
                            if ($fin_schoolkind != "") {
                                $contents .= $sep .$fin_schoolkind.$codePrefix."コード,".$fin_schoolkind.$item_array[$item[$i]];
                                $sep = ",";
                            }
                        }
                        break;
                    case "ENT_DATE":
                    case "ENT_REASON":
                    case "ENT_SCHOOL":
                    case "ENT_ADDR":
                    case "ENT_ADDR2":
                    case "GRD_DATE":
                    case "GRD_REASON":
                    case "GRD_SCHOOL":
                    case "GRD_ADDR":
                    case "GRD_ADDR2":
                    case "GRD_NO":
                    case "GRD_TERM":
                        for ($j = 0; $j < get_count($schoolkind); $j++) {
                            $contents .= $sep .$schoolkind[$j].$item_array[$item[$i]];
                            $sep = ",";
                        }
                        break;
                    case "FINISH_DATE":
                        for ($j = 0; $j < get_count($schoolkind); $j++) {
                            $fin_schoolkind = "";
                            switch ($schoolkind[$j]) {
                                case "P":
                                    $fin_schoolkind = 'K';
                                    break;
                                case "J":
                                    $fin_schoolkind = 'P';
                                    break;
                                case "H":
                                    $fin_schoolkind = 'J';
                                    break;
                            }
                            if ($fin_schoolkind != "") {
                                $contents .= $sep .$fin_schoolkind.$item_array[$item[$i]];
                                $sep = ",";
                            }
                        }
                        break;
                    case "ADDR1":
                    case "GUARD_ADDR1":
                    case "GUARD2_ADDR1":
                    case "GUARANTOR_ADDR1":
                    case "EXAMNO":
                        if ($this->addr1_div && !in_array($item[$i], $addr2_div)) {
                            $contents .= $sep .$item_array[$item[$i]] .",".$item_array[$item[$i]]."-1";
                        } else {
                            $contents .= $sep .$item_array[$item[$i]];
                        }
                        break;
                    case "NAME":
                    case "NAME_KANA":
                    case "REAL_NAME":
                    case "REAL_NAME_KANA":
                    case "GUARD_NAME":
                    case "GUARD_KANA":
                    case "GUARD_REAL_NAME":
                    case "GUARD_REAL_KANA":
                    case "GUARD2_NAME":
                    case "GUARD2_KANA":
                    case "GUARD2_REAL_NAME":
                    case "GUARD2_REAL_KANA":
                    case "GUARANTOR_NAME":
                    case "GUARANTOR_KANA":
                    case "GUARANTOR_REAL_NAME":
                    case "GUARANTOR_REAL_KANA":
                        if ($this->name_div) {
                            $contents .= $sep .$item_array[$item[$i]] .",".$item_array[$item[$i]]."-名";
                        } else {
                            $contents .= $sep .$item_array[$item[$i]];
                        }
                        break;
                    default:
                        $contents .= $sep .$item_array[$item[$i]];
                        break;
                }
            } else {
                switch ($item[$i]) {
                    case "ENT_DATE":
                    case "ENT_DIV":
                    case "ENT_REASON":
                    case "ENT_SCHOOL":
                    case "ENT_ADDR":
                    case "ENT_ADDR2":
                    case "GRD_DATE":
                    case "GRD_DIV":
                    case "GRD_REASON":
                    case "GRD_SCHOOL":
                    case "GRD_ADDR":
                    case "GRD_ADDR2":
                    case "GRD_NO":
                    case "GRD_TERM":
                        for ($j = 0; $j < get_count($schoolkind); $j++) {
                            $contents .= $sep .$schoolkind[$j].$item_array[$item[$i]];
                            $sep = ",";
                        }
                        break;
                    case "FINSCHOOLCD":
                    case "FINISH_DATE":
                        for ($j = 0; $j < get_count($schoolkind); $j++) {
                            $fin_schoolkind = "";
                            switch ($schoolkind[$j]) {
                                case "P":
                                    $fin_schoolkind = 'K';
                                    break;
                                case "J":
                                    $fin_schoolkind = 'P';
                                    break;
                                case "H":
                                    $fin_schoolkind = 'J';
                                    break;
                            }
                            if ($fin_schoolkind != "") {
                                $contents .= $sep .$fin_schoolkind.$item_array[$item[$i]];
                                $sep = ",";
                            }
                        }
                        break;
                    case "ADDR1":
                    case "GUARD_ADDR1":
                    case "GUARD2_ADDR1":
                    case "GUARANTOR_ADDR1":
                    case "EXAMNO":
                        if ($this->addr1_div && !in_array($item[$i], $addr2_div)) {
                            $contents .= $sep .$item_array[$item[$i]] .",".$item_array[$item[$i]]."-1";
                        } else {
                            $contents .= $sep .$item_array[$item[$i]];
                        }
                        break;
                    case "NAME":
                    case "NAME_KANA":
                    case "REAL_NAME":
                    case "REAL_NAME_KANA":
                    case "GUARD_NAME":
                    case "GUARD_KANA":
                    case "GUARD_REAL_NAME":
                    case "GUARD_REAL_KANA":
                    case "GUARD2_NAME":
                    case "GUARD2_KANA":
                    case "GUARD2_REAL_NAME":
                    case "GUARD2_REAL_KANA":
                    case "GUARANTOR_NAME":
                    case "GUARANTOR_KANA":
                    case "GUARANTOR_REAL_NAME":
                    case "GUARANTOR_REAL_KANA":
                        if ($this->name_div) {
                            $contents .= $sep .$item_array[$item[$i]] .",".$item_array[$item[$i]]."-名";
                        } else {
                            $contents .= $sep .$item_array[$item[$i]];
                        }
                        break;
                    default:
                        $contents .= $sep .$item_array[$item[$i]];
                        break;
                }
            }
            $sep = ",";
        }
        $contents .= "\n";
        //データ部作成
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //★住所１を分割チェックあり
            if ($this->addr1_div) {
                foreach ($addrArray as $addr1 => $addr2) {
                    //★住所１のITEM指定あり
                    //ＣＳＶ項目住所１：住所１の都道府県＋市町村
                    //ＣＳＶ項目住所２：住所１の残り
                    if (in_array($addr1, $item)) {
                        $zip_addr = $db->getOne(knji100c_01Query::getZipAddress($row[$zipcdList[$addr1]]));
                        if (strpos($row[$addr1], $zip_addr) === 0) {
                            $row[$addr1."-1"] = str_replace($zip_addr, '', $row[$addr1]);
                            $row[$addr1] = $zip_addr;
                            //★住所２のITEM指定あり
                            //ＣＳＶ項目住所２：住所１の残り＋住所２
                            if (in_array($addr2, $item)) {
                                $row[$addr2] = $row[$addr1."-1"] . $row[$addr2];
                            }
                        } else {
                            $row[$addr1."-1"] = "";
                            $row[$addr1] = $row[$addr1];
                            //★住所２のITEM指定あり
                            //ＣＳＶ項目住所２：住所１の残り＋住所２
                            if (in_array($addr2, $item)) {
                                $row[$addr2] = $row[$addr1."-1"] . $row[$addr2];
                            }
                        }
                    }
                }//foreach
            }

            $sep = "";
            for ($i = 0; $i < get_count($item); $i++) {
                switch ($item[$i]) {
                    case "ENT_DATE":
                    case "ENT_REASON":
                    case "ENT_SCHOOL":
                    case "ENT_ADDR":
                    case "ENT_ADDR2":
                    case "GRD_DATE":
                    case "GRD_REASON":
                    case "GRD_SCHOOL":
                    case "GRD_ADDR":
                    case "GRD_ADDR2":
                    case "GRD_NO":
                    case "GRD_TERM":
                        for ($j = 0; $j < get_count($schoolkind); $j++) {
                            $hist_data = $db->getOne(knji100c_01Query::getSchregEntGrdHistDat($item[$i], $row["SCHREGNO"], $schoolkind[$j]));
                            $contents .= $sep .$hist_data;
                            $sep = ",";
                        }
                        break;
                    case "FINISH_DATE":
                        for ($j = 0; $j < get_count($schoolkind); $j++) {
                            $fin_schoolkind = "";
                            switch ($schoolkind[$j]) {
                                case "P":
                                    $fin_schoolkind = 'K';
                                    break;
                                case "J":
                                    $fin_schoolkind = 'P';
                                    break;
                                case "H":
                                    $fin_schoolkind = 'J';
                                    break;
                            }
                            if ($fin_schoolkind != "") {
                                $hist_data = $db->getOne(knji100c_01Query::getSchregEntGrdHistDat($item[$i], $row["SCHREGNO"], $schoolkind[$j]));
                                $contents .= $sep .$hist_data;
                                $sep = ",";
                            }
                        }
                        break;
                    case "FINSCHOOLCD":
                        for ($j = 0; $j < get_count($schoolkind); $j++) {
                            $fin_schoolkind = "";
                            switch ($schoolkind[$j]) {
                                case "P":
                                    $fin_schoolkind = 'K';
                                    break;
                                case "J":
                                    $fin_schoolkind = 'P';
                                    break;
                                case "H":
                                    $fin_schoolkind = 'J';
                                    break;
                            }
                            if ($fin_schoolkind != "") {
                                $hist_data = $db->getOne(knji100c_01Query::getSchregEntGrdHistDat($item[$i], $row["SCHREGNO"], $schoolkind[$j]));
                                if (!$hist_data && $this->output == OUT_CODE_NAME) {
                                    $chg_data  =  ",";
                                } else {
                                    $chg_data  = $db->getOne(knji100c_01Query::getFinSchoolMst($this, $hist_data));
                                    if (!$chg_data && $this->output == OUT_CODE_NAME) {
                                        $chg_data = ",";
                                    }
                                }
                                $contents .= $sep .$chg_data;
                                $sep = ",";
                            }
                        }
                        break;
                    case "ENT_DIV":
                    case "GRD_DIV":
                        $namecd1 = ($item[$i] == "ENT_DIV") ? 'A002' : 'A003';
                        for ($j = 0; $j < get_count($schoolkind); $j++) {
                            $hist_data = $db->getOne(knji100c_01Query::getSchregEntGrdHistDat($item[$i], $row["SCHREGNO"], $schoolkind[$j]));
                            $chg_data  = (!$hist_data && $this->output == OUT_CODE_NAME) ? "," : $db->getOne(knji100c_01Query::getNameMst($this, $namecd1, $hist_data));
                            $contents .= $sep .$chg_data;
                            $sep = ",";
                        }
                        break;
                    case "ADDR1":
                    case "GUARD_ADDR1":
                    case "GUARD2_ADDR1":
                    case "GUARANTOR_ADDR1":
                        if ($this->addr1_div && !in_array($item[$i], $addr2_div)) {
                            $contents .= $sep .$row[$item[$i]]. "," .$row[$item[$i]."-1"];
                        } else {
                            $contents .= $sep .$row[$item[$i]];
                        }
                        $sep = ",";
                        break;
                    case "NAME":
                    case "NAME_KANA":
                    case "REAL_NAME":
                    case "REAL_NAME_KANA":
                    case "GUARD_NAME":
                    case "GUARD_KANA":
                    case "GUARD_REAL_NAME":
                    case "GUARD_REAL_KANA":
                    case "GUARD2_NAME":
                    case "GUARD2_KANA":
                    case "GUARD2_REAL_NAME":
                    case "GUARD2_REAL_KANA":
                    case "GUARANTOR_NAME":
                    case "GUARANTOR_KANA":
                    case "GUARANTOR_REAL_NAME":
                    case "GUARANTOR_REAL_KANA":
                        if ($this->name_div) {
                            $nameArray = explode("　", $row[$item[$i]]);
                            if (1 < get_count($nameArray)) {
                                $name   = $nameArray[0]. "," .$nameArray[1];
                            } else {
                                $name   = $nameArray[0]. ",";
                            }
                            $contents .= $sep .$name;
                        } else {
                            $contents .= $sep .$row[$item[$i]];
                        }
                        $sep = ",";
                        break;
                    case "COURSECD":
                    case "MAJORCD":
                    case "COURSECODE":
                        $name = "";
                        if ($this->output == OUT_CODE_NAME) {
                            if ($item[$i] == "COURSECD") {
                                $name = $db->getOne(knji100c_01Query::getCourseMst($this, $row[$item[$i]]));
                            } elseif ($item[$i] == "MAJORCD") {
                                $name = $db->getOne(knji100c_01Query::getMajorMst($this, $row["COURSECD"], $row["MAJORCD"]));
                            } elseif ($item[$i] == "COURSECODE") {
                                $name = $db->getOne(knji100c_01Query::getCoursecodeMst($this, $row[$item[$i]]));
                            }
                            $name = ",".$name;
                        }
                        $contents .= $sep .$row[$item[$i]].$name;
                        $sep = ",";
                        break;
                    default:
                        $contents .= $sep .$row[$item[$i]];
                        break;
                }
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        Query::dbCheckIn($db);
        if ($data) {
            $filename = sprintf("%s.csv", "生徒基礎データ（学籍基礎情報）");
            common::downloadContents($contents, $filename);
            exit;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
