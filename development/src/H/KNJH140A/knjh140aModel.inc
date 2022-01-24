<?php

require_once('for_php7.php');
class knjh140aModel extends Model
{
    public $cmd;
    public $file;
    public $field      = array();
    public $data_arr = array();
    public $controls = array();
    public $headercheck;
    public $testsubclasscd;
    public $testsubclassnm;
    public $coursecd;
    public $majorcd;
    public $coursecode;
    public $wk;
    public $appli;
    public $schoolCd;
    public $schoolname;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $sin_check;
    public $lastColumn;//CSVの最後の列の値

    public function knjh140aModel()
    {
        $this->year  = CTRL_YEAR + 1;
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組一覧
                             "GRD_CHECK"        =>  VARS::post("GRD_CHECK")         //異動生徒出力選択
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("PROGRAMID")) {
            $this->headercheck        = "1";
            $this->target             = "1";
            $this->field["GRD_CHECK"] = "1";
        } else {
            $this->headercheck  = VARS::post("HEADERCHECK");
            $this->sin_check    = VARS::post("SINNYUSEI");
            $this->target       = VARS::post("TARGET");
        }

        $this->file = VARS::file("FILE");

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getUpdateModel()
    {
        //事項時間の制限を無くす
        set_time_limit(0);

        $this->data_arr = array();
        $this->errData = array();
        //ファイルの文字列の取得
        $data_arr = $this->getFileString();

        //データの整合性チェック
        $erraDatas = "";
        switch ($this->target) {
            case 1:
                $tables = array("1" => "SCHREG_RELA_DAT");
                break;
            case 2:
                $tables = array("1" => "SCHREG_BASE_MST", "2" => "SCHREG_BASE_DETAIL_MST", "3" => "SCHREG_ADDRESS_DAT");
                break;
            case 3:
                $tables = array("1" => "SCHREG_ENVIR_DAT");
                break;
            case 4:
                $tables = array("1" => "SCHREG_DETAILHIST_DAT");
                break;
            case 5:
                $tables = array("1" => "SCHREG_SEND_ADDRESS_DAT");
                break;
        }
        $header = array("SCHREGNO"     => "学籍番号",
                        "RELANO"       => "連番",
                        "BASE_SEQ"     => "学籍基礎SEQ",
                        "ISSUEDATE"    => "有効開始日付",
                        "YEAR"         => "年度",
                        "DETAIL_DIV"   => "詳細区分",
                        "DETAIL_SDATE" => "登録開始日付",
                        "DIV"          => "区分"
                        );
        $dupTable = $this->getDuplicateErrorHeaderNoSave(PROGRAMID, $tables, $this->errData);
        if (sizeof($dupTable) > 0) {
            $headerErr = $this->duplicateCsvHeader($dupTable, $header);
        }
        if (sizeof($this->errData) > 0) {
            $erraDatas .= "行番号　メッセージ"."\\n";
        }
        foreach ($this->errData as $errKey => $errVal) {
            $erraDatas .= $errKey."行目　".$errVal["MSG"]."\\n";
        }
        if ($erraDatas != "") {
            $this->setMessage("MSG204", "\\n"."取込みエラー内容:". "\\n".$headerErr["MSGREMARK"]. "\\n".$erraDatas);
        } else {
            knjh140aQuery::insertQuery($this, $data_arr);
            $this->setMessage("MSG202", "更新件数".get_count($data_arr)."件");
        }
    }

    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $nameSLen = $this->Properties["NAME_INPUT_SIZE"] != "" ? $this->Properties["NAME_INPUT_SIZE"] : 20;
            $addrSLen = $this->Properties["ADDR_INPUT_SIZE"] != "" ? $this->Properties["ADDR_INPUT_SIZE"] : 30;
            switch ($this->target) {
                case 1:
                    $checkdata = array( "YEAR"              => array(4,"年度"),
                                        "SCHREGNO"          => array(8,"学籍番号"),
                                        "RELANO"            => array(2,"連番"),
                                        "RELANAME"          => array(intval($nameSLen) * 3,"親族氏名"),
                                        "RELAKANA"          => array(120,"親族氏名かな"),
                                        "RELASEX"           => array(1,"親族性別"),
                                        "RELABIRTHDAY"      => array(10,"生年月日"),
                                        "OCCUPATION"        => array(60,"勤務先または学校名"),
                                        "REGIDENTIALCD"     => array(2,"同居区分"),
                                        "RELATIONSHIP"      => array(2,"親族続柄"),
                                        "RELA_SCHREGNO"     => array(8,"親族学籍番号"),
                                        "REGD_GRD_FLG"      => array(1,"親族在卒区分"),
                                        "RELA_GRADE"        => array(8,"親族学年"),
                                        "REMARK"            => array(45,"備考"));
                    break;
                case 2:
                    $checkdata = array( "YEAR"                  => array(4,"年度"),
                                        "SCHREGNO"              => array(8,"学籍番号"),
                                        "TELNO"                 => array(14,"本人電話番号1"),
                                        "PRIORITY1"             => array(3,"優先順位"),
                                        "TELNO2"                => array(14,"本人電話番号2"),
                                        "PRIORITY2"             => array(3,"優先順位"),
                                        "EMERGENCYCALL"         => array(60,"緊急連絡先"),
                                        "EMERGENCYNAME"         => array(intval($nameSLen) * 3,"緊急連絡先1氏名"),
                                        "EMERGENCYRELA_NAME"    => array(30,"緊急連絡先1続柄名"),
                                        "EMERGENCYTELNO"        => array(14,"緊急連絡先1電話番号1"),
                                        "E_TELNO_MEMO"          => array(24,"番号1備考"),
                                        "PRIORITY3"             => array(3,"優先順位"),
                                        "EMERGENCYTELNO_2"      => array(14,"緊急連絡先1電話番号2"),
                                        "E_TELNO_MEMO_2"        => array(24,"番号2備考"),
                                        "PRIORITY4"             => array(3,"優先順位"),
                                        "EMERGENCYCALL2"        => array(60,"緊急連絡先2"),
                                        "EMERGENCYNAME2"        => array(intval($nameSLen) * 3,"緊急連絡先2氏名"),
                                        "EMERGENCYRELA_NAME2"   => array(30,"緊急連絡先2続柄名"),
                                        "EMERGENCYTELNO2"       => array(14,"緊急連絡先2電話番号1"),
                                        "E_TELNO_MEMO2"         => array(24,"番号1備考"),
                                        "PRIORITY5"             => array(3,"優先順位"),
                                        "EMERGENCYTELNO2_2"     => array(14,"緊急連絡先2電話番号2"),
                                        "E_TELNO_MEMO2_2"       => array(24,"番号2備考"),
                                        "PRIORITY6"             => array(3,"優先順位"));
                    break;
                case 3:
                    $checkdata = array( "YEAR"                  => array(4,"年度"),
                                        "SCHREGNO"              => array(8,"学籍番号"),
                                        "GO_HOME_GROUP_NO"      => array(2,"災害時帰宅グループ番号"),
                                        "COMMUTE_HOURS"         => array(2,"通学所要時間"),
                                        "COMMUTE_MINUTES"       => array(2,"通学所要分"),
                                        "HOWTOCOMMUTECD"        => array(1,"最寄駅までの手段"),
                                        "JOSYA_1"               => array(45,"最寄駅（自宅）"),
                                        "ROSEN_1"               => array(45,"路線（自宅）"),
                                        "GESYA_1"               => array(45,"下車駅（自宅）"),
                                        "FLG_1"                 => array(1,"通学手段（自宅）"),
                                        "JOSYA_2"               => array(45,"乗車駅1"),
                                        "ROSEN_2"               => array(45,"路線1"),
                                        "GESYA_2"               => array(45,"下車駅1"),
                                        "FLG_2"                 => array(1,"通学手段1"),
                                        "JOSYA_3"               => array(45,"乗車駅2"),
                                        "ROSEN_3"               => array(45,"路線2"),
                                        "GESYA_3"               => array(45,"下車駅2"),
                                        "FLG_3"                 => array(1,"通学手段2"),
                                        "JOSYA_4"               => array(45,"乗車駅3"),
                                        "ROSEN_4"               => array(45,"路線3"),
                                        "GESYA_4"               => array(45,"下車駅3"),
                                        "FLG_4"                 => array(1,"通学手段3"),
                                        "JOSYA_5"               => array(45,"乗車駅4"),
                                        "ROSEN_5"               => array(45,"路線4"),
                                        "GESYA_5"               => array(45,"下車駅4"),
                                        "FLG_5"                 => array(1,"通学手段4"),
                                        "JOSYA_6"               => array(45,"乗車駅5"),
                                        "ROSEN_6"               => array(45,"路線5"),
                                        "GESYA_6"               => array(45,"下車駅5"),
                                        "FLG_6"                 => array(1,"通学手段5"),
                                        "JOSYA_7"               => array(45,"最寄駅（学校）"),
                                        "ROSEN_7"               => array(45,"路線（学校）"),
                                        "GESYA_7"               => array(45,"下車駅（学校）"),
                                        "FLG_7"                 => array(1,"通学手段（学校）"));
                    break;
                case 4:
                    $checkdata = array( "YEAR"                  => array(4,"年度"),
                                        "SCHREGNO"              => array(8,"学籍番号"),
                                        "DETAIL_SDATE"          => array(10,"登録開始日付"),
                                        "DETAIL_EDATE"          => array(10,"登録終了日付"),
                                        "BICYCLE_CD"            => array(8,"許可番号"),
                                        "BICYCLE_NO"            => array(4,"駐輪場番号"),
                                        "CONTENT"               => array(120,"詳細内容"),
                                        "REMARK"                => array(60,"備考"));
                    break;
                case 5:
                    $checkdata = array( "YEAR"                  => array(4,"年度"),
                                        "SCHREGNO"              => array(8,"学籍番号"),
                                        "SEND_RELATIONSHIP"     => array(2,"その他の続柄"),
                                        "SEND_NAME"             => array(intval($nameSLen) * 3,"その他の氏名"),
                                        "SEND_KANA"             => array(120,"その他のかな"),
                                        "SEND_SEX"              => array(1,"その他の性別"),
                                        "SEND_ZIPCD"            => array(8,"その他ぼ郵便番号"),
                                        "SEND_ADDR1"            => array(intval($addrSLen) * 3, "その他の住所1"),
                                        "SEND_ADDR2"            => array(intval($addrSLen) * 3, "その他の住所2"),
                                        "SEND_TELNO"            => array(14,"その他の電話番号"),
                                        "SEND_JOBCD"            => array(2,"その他の職種コード"),
                                        "PUBLIC_OFFICE"         => array(30,"その他の兼ねている公職"));
                    break;
            }
            $db = Query::dbCheckOut();
            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey2 = array();
            $primaryKey3 = array();
            switch ($this->target) {
                case 1:
                    $primaryKey1 = $this->getPrimaryKey('SCHREG_RELA_DAT');
                    break;
                case 2:
                    $primaryKey1 = $this->getPrimaryKey('SCHREG_BASE_MST');
                    $primaryKey2 = $this->getPrimaryKey('SCHREG_BASE_DETAIL_MST');
                    $primaryKey3 = $this->getPrimaryKey('SCHREG_ADDRESS_DAT');
                    break;
                case 3:
                    $primaryKey1 = $this->getPrimaryKey('SCHREG_ENVIR_DAT');
                    break;
                case 4:
                    $primaryKey1 = $this->getPrimaryKey('SCHREG_DETAILHIST_DAT');
                    break;
                case 5:
                    $primaryKey1 = $this->getPrimaryKey('SCHREG_SEND_ADDRESS_DAT');
                    break;
            }
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->headercheck == 1) {
                        $header = true;
                        $line++;
                        continue;
                    }

                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    switch ($this->target) {
                        case 1:
                            if ($tmp[0] != 1) {
                                $this->warning = "CSVCDが家族情報ではありません。\\n家族情報【1】";
                            }
                            $data = array("YEAR"              => $tmp[1],
                                          "GRADE"             => (strlen(trim($tmp[2]))) ? sprintf("%02d", $tmp[2]) : "",
                                          "HR_CLASS"          => (strlen(trim($tmp[3]))) ? sprintf("%03d", $tmp[3]) : "",
                                          "ATTENDNO"          => (strlen(trim($tmp[4]))) ? sprintf("%03d", $tmp[4]) : "",
                                          "SCHREGNO"          => (strlen(trim($tmp[5]))) ? sprintf("%08d", $tmp[5]) : "",
                                          "NAME"              => $tmp[6],
                                          "RELANO"            => $tmp[7],
                                          "RELANAME"          => $tmp[8],
                                          "RELAKANA"          => $tmp[9],
                                          "RELASEX"           => $tmp[10],
                                          "RELABIRTHDAY"      => $tmp[11],
                                          "OCCUPATION"        => $tmp[12],
                                          "REGIDENTIALCD"     => (strlen(trim($tmp[13]))) ? sprintf("%02d", $tmp[13]) : "",
                                          "RELATIONSHIP"      => (strlen(trim($tmp[14]))) ? sprintf("%02d", $tmp[14]) : "",
                                          "RELA_SCHREGNO"     => (strlen(trim($tmp[15]))) ? sprintf("%08d", $tmp[15]) : "",
                                          "REGD_GRD_FLG"      => $tmp[16],
                                          "RELA_GRADE"        => (strlen(trim($tmp[17]))) ? sprintf("%02d", $tmp[17]) : "",
                                          "REMARK"            => $tmp[18]);
                            break;
                        case 2:
                            if ($tmp[0] != 2) {
                                $this->warning = "CSVCDが緊急連絡先情報ではありません。\\n緊急連絡先情報【2】";
                            }
                            $data = array("YEAR"                  => $tmp[1],
                                          "GRADE"                 => (strlen(trim($tmp[2]))) ? sprintf("%02d", $tmp[2]) : "",
                                          "HR_CLASS"              => (strlen(trim($tmp[3]))) ? sprintf("%03d", $tmp[3]) : "",
                                          "ATTENDNO"              => (strlen(trim($tmp[4]))) ? sprintf("%03d", $tmp[4]) : "",
                                          "SCHREGNO"              => (strlen(trim($tmp[5]))) ? sprintf("%08d", $tmp[5]) : "",
                                          "NAME"                  => $tmp[6],
                                          "TELNO"                 => $tmp[7],
                                          "PRIORITY1"             => $tmp[8],
                                          "TELNO2"                => $tmp[9],
                                          "PRIORITY2"             => $tmp[10],
                                          "EMERGENCYCALL"         => $tmp[11],
                                          "EMERGENCYNAME"         => $tmp[12],
                                          "EMERGENCYRELA_NAME"    => $tmp[13],
                                          "EMERGENCYTELNO"        => $tmp[14],
                                          "E_TELNO_MEMO"          => $tmp[15],
                                          "PRIORITY3"             => $tmp[16],
                                          "EMERGENCYTELNO_2"      => $tmp[17],
                                          "E_TELNO_MEMO_2"        => $tmp[18],
                                          "PRIORITY4"             => $tmp[19],
                                          "EMERGENCYCALL2"        => $tmp[20],
                                          "EMERGENCYNAME2"        => $tmp[21],
                                          "EMERGENCYRELA_NAME2"   => $tmp[22],
                                          "EMERGENCYTELNO2"       => $tmp[23],
                                          "E_TELNO_MEMO2"         => $tmp[24],
                                          "PRIORITY5"             => $tmp[25],
                                          "EMERGENCYTELNO2_2"     => $tmp[26],
                                          "E_TELNO_MEMO2_2"       => $tmp[27],
                                          "PRIORITY6"             => $tmp[28]);
                            break;
                        case 3:
                            if ($tmp[0] != 3) {
                                $this->warning = "CSVCDが通学経路情報ではありません。\\n通学経路情報【3】";
                            }
                            $data = array("YEAR"                  => $tmp[1],
                                          "GRADE"                 => (strlen(trim($tmp[2]))) ? sprintf("%02d", $tmp[2]) : "",
                                          "HR_CLASS"              => (strlen(trim($tmp[3]))) ? sprintf("%03d", $tmp[3]) : "",
                                          "ATTENDNO"              => (strlen(trim($tmp[4]))) ? sprintf("%03d", $tmp[4]) : "",
                                          "SCHREGNO"              => (strlen(trim($tmp[5]))) ? sprintf("%08d", $tmp[5]) : "",
                                          "NAME"                  => $tmp[6],
                                          "GO_HOME_GROUP_NO"      => (strlen(trim($tmp[7]))) ? sprintf("%02d", $tmp[7]) : "",
                                          "COMMUTE_HOURS"         => (strlen(trim($tmp[8]))) ? sprintf("%02d", $tmp[8]) : "",
                                          "COMMUTE_MINUTES"       => (strlen(trim($tmp[9]))) ? sprintf("%02d", $tmp[9]) : "",
                                          "HOWTOCOMMUTECD"        => $tmp[10],
                                          "JOSYA_1"               => $tmp[11],
                                          "JOSYANAME_1"           => $tmp[12],
                                          "ROSEN_1"               => $tmp[13],
                                          "ROSENNAME_1"           => $tmp[14],
                                          "GESYA_1"               => $tmp[15],
                                          "GESYANAME_1"           => $tmp[16],
                                          "FLG_1"                 => (strlen(trim($tmp[17]))) ? sprintf("%01d", $tmp[17]) : "",
                                          "JOSYA_2"               => $tmp[18],
                                          "JOSYANAME_2"           => $tmp[19],
                                          "ROSEN_2"               => $tmp[20],
                                          "ROSENNAME_2"           => $tmp[21],
                                          "GESYA_2"               => $tmp[22],
                                          "GESYANAME_2"           => $tmp[23],
                                          "FLG_2"                 => (strlen(trim($tmp[24]))) ? sprintf("%01d", $tmp[24]) : "",
                                          "JOSYA_3"               => $tmp[25],
                                          "JOSYANAME_3"           => $tmp[26],
                                          "ROSEN_3"               => $tmp[27],
                                          "ROSENNAME_3"           => $tmp[28],
                                          "GESYA_3"               => $tmp[29],
                                          "GESYANAME_3"           => $tmp[30],
                                          "FLG_3"                 => (strlen(trim($tmp[31]))) ? sprintf("%01d", $tmp[31]) : "",
                                          "JOSYA_4"               => $tmp[32],
                                          "JOSYANAME_4"           => $tmp[33],
                                          "ROSEN_4"               => $tmp[34],
                                          "ROSENNAME_4"           => $tmp[35],
                                          "GESYA_4"               => $tmp[36],
                                          "GESYANAME_4"           => $tmp[37],
                                          "FLG_4"                 => (strlen(trim($tmp[38]))) ? sprintf("%01d", $tmp[38]) : "",
                                          "JOSYA_5"               => $tmp[39],
                                          "JOSYANAME_5"           => $tmp[40],
                                          "ROSEN_5"               => $tmp[41],
                                          "ROSENNAME_5"           => $tmp[42],
                                          "GESYA_5"               => $tmp[43],
                                          "GESYANAME_5"           => $tmp[44],
                                          "FLG_5"                 => (strlen(trim($tmp[45]))) ? sprintf("%01d", $tmp[45]) : "",
                                          "JOSYA_6"               => $tmp[46],
                                          "JOSYANAME_6"           => $tmp[47],
                                          "ROSEN_6"               => $tmp[48],
                                          "ROSENNAME_6"           => $tmp[49],
                                          "GESYA_6"               => $tmp[50],
                                          "GESYANAME_6"           => $tmp[51],
                                          "FLG_6"                 => (strlen(trim($tmp[52]))) ? sprintf("%01d", $tmp[52]) : "",
                                          "JOSYA_7"               => $tmp[53],
                                          "JOSYANAME_7"           => $tmp[54],
                                          "ROSEN_7"               => $tmp[55],
                                          "ROSENNAME_7"           => $tmp[56],
                                          "GESYA_7"               => $tmp[57],
                                          "GESYANAME_7"           => $tmp[58],
                                          "FLG_7"                 => (strlen(trim($tmp[59]))) ? sprintf("%01d", $tmp[59]) : "");
                            break;
                        case 4:
                            if ($tmp[0] != 4) {
                                $this->warning = "CSVCDが自転車許可番号ではありません。\\n自転車許可番号【4】";
                            }
                            $data = array("YEAR"                  => $tmp[1],
                                          "SCHREGNO"              => (strlen(trim($tmp[2]))) ? sprintf("%08d", $tmp[2]) : "",
                                          "GRADE"                 => (strlen(trim($tmp[3]))) ? sprintf("%02d", $tmp[3]) : "",
                                          "HR_CLASS"              => (strlen(trim($tmp[4]))) ? sprintf("%03d", $tmp[4]) : "",
                                          "ATTENDNO"              => (strlen(trim($tmp[5]))) ? sprintf("%03d", $tmp[5]) : "",
                                          "NAME"                  => $tmp[6],
                                          "DETAIL_SDATE"          => (strlen(trim($tmp[7]))) ? str_replace("/", "-", $tmp[7]) : "",
                                          "DETAIL_EDATE"          => (strlen(trim($tmp[8]))) ? str_replace("/", "-", $tmp[8]) : "",
                                          "BICYCLE_CD"            => $tmp[9],
                                          "BICYCLE_NO"            => $tmp[10],
                                          "CONTENT"               => $tmp[11],
                                          "REMARK"                => $tmp[12]);
                            break;
                        case 5:
                            if ($tmp[0] != 5) {
                                $this->warning = "CSVCDがその他（通知票等 送付先）ではありません。\\nその他（通知票等 送付先）【5】";
                            }
                            $data = array("YEAR"                  => $tmp[1],
                                          "GRADE"                 => (strlen(trim($tmp[2]))) ? sprintf("%02d", $tmp[2]) : "",
                                          "HR_CLASS"              => (strlen(trim($tmp[3]))) ? sprintf("%03d", $tmp[3]) : "",
                                          "ATTENDNO"              => (strlen(trim($tmp[4]))) ? sprintf("%03d", $tmp[4]) : "",
                                          "SCHREGNO"              => (strlen(trim($tmp[5]))) ? sprintf("%08d", $tmp[5]) : "",
                                          "NAME"                  => $tmp[6],
                                          "SEND_RELATIONSHIP"     => $tmp[7],
                                          "SEND_NAME"             => $tmp[8],
                                          "SEND_KANA"             => $tmp[9],
                                          "SEND_SEX"              => $tmp[10],
                                          "SEND_ZIPCD"            => $tmp[11],
                                          "SEND_ADDR1"            => $tmp[12],
                                          "SEND_ADDR2"            => $tmp[13],
                                          "SEND_TELNO"            => $tmp[14],
                                          "SEND_JOBCD"            => $tmp[15],
                                          "PUBLIC_OFFICE"         => $tmp[16]);
                            break;
                    }
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$line][$val] = $data[$val];
                    }
                    $line++;
                }
            }
            Query::dbCheckIn($db);
            fclose($fp);
        }
        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)
    {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $otherErrFlg        = false;
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $otherSep           = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }
        $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data);
        if ($dupFlg3[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg3[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR","SCHREGNO","RELANO","DETAIL_SDATE","DETAIL_EDATE","BICYCLE_CD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            if ($this->Properties["chkNameInputHankaku"] == "1") {
                $chkFlg = false;
                switch ($this->target) {
                    case 1:
                        if ($key == "RELANAME") {
                            $chkFlg = true;
                        }
                        break;
                    case 2:
                        if ($key == "EMERGENCYNAME" || $key == "EMERGENCYNAME2") {
                            $chkFlg = true;
                        }
                        break;
                    case 5:
                        if ($key == "SEND_NAME") {
                            $chkFlg = true;
                        }
                        break;
                    default:
                        break;
                }
                if ($chkFlg) {
                    if (!$this->chkHankaku($data[$key])) {
                        $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "半角不可".$val[1]);
                        return false;
                    }
                }
            }
            if ($this->Properties["chkAddrInputHankaku"] == "1") {
                if ($this->target == 5) {
                    if ($key == "SEND_ADDR1" || $key == "SEND_ADDR2") {
                        if (!$this->chkHankaku($data[$key])) {
                            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "半角不可".$val[1]);
                            return false;
                        }
                    }
                }
            }

            //数値チェック
            if (in_array($key, array("RELANO", "REGIDENTIALCD", "RELATIONSHIP", "REGD_GRD_FLG", "RELA_GRADE", "COMMUTE_HOURS", "COMMUTE_MINUTES", "FLG_1", "FLG_2", "FLG_3", "FLG_4", "FLG_5", "FLG_6", "FLG_7"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //存在チェック
            if ($this->target == 1) {
                $gradeList = $db->getCol(knjh140aQuery::getGradeList($data["YEAR"]));

                if ($data[$key] != "" && in_array($key, array("RELA_GRADE")) && !in_array($data[$key], $gradeList)) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }

                if ($data[$key] != "" && in_array($key, array("REGD_GRD_FLG")) && !in_array($data[$key], array("1", "2"))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }

             //登録開始日付と登録終了日付の日付型チェック
            if (in_array($key, array("DETAIL_SDATE","DETAIL_EDATE"))) {
                if (common::isDate($data[$key]) == false) {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※日付(フォーマット):", $otherSep, $val[1]);
                }
            //登録開始日付と登録終了日付の大小チェック
                if ($this->dateRep($data["DETAIL_EDATE"]) != "" && $this->dateRep($data["DETAIL_SDATE"]) > $this->dateRep($data["DETAIL_EDATE"])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※日付の大小:", $numericSep, $val[1]);
                }
            }

            //文字数チェック
            if ($this->Properties["NAME_INPUT_SIZE"] != "" && in_array($key, array("RELANAME","EMERGENCYNAME", "EMERGENCYNAME2", "SEND_NAME"))) {
                if (mb_strlen($data[$key], 'UTF-8') > $nameSLen) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, "全角{$this->nameSLen}文字超過".$val[1]);
                }
            } elseif ($this->Properties["ADDR_INPUT_SIZE"] != "" && in_array($key, array("SEND_ADDR1","SEND_ADDR2"))) {
                if (mb_strlen($data[$key], 'UTF-8') > $addrSLen) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, "全角{$this->addrSLen}文字超過".$val[1]);
                }
            } else {
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
            if ($this->sin_check == '1') {
                $schregno = $db->getOne(knjh140aQuery::getClassFormationDat($data["YEAR"]), $data["SCHREGNO"]);
                if ($schregno == '') {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$otherErrMsg;
            $checkError = $this->checkErrLength($checkError);
            $this->errData[$line]["MSG"] = $checkError;
        }
        return $returnFlg;
    }

    //テンプレートの書き出し
    public function outputTmpFile()
    {
        switch ($this->target) {
            case 1:
                $contents   ="※CSVCD,※年度,学年,組,出席番号,";
                $contents .="※学籍番号,氏名,※連番(2桁),親族氏名,親族氏名かな,親族性別,生年月日,";
                $contents .="通勤先又は学校名,同居区分,親族続柄,親族学籍番号,親族在卒区分,親族学年,備考,".$this->lastColumn."\n";
                $contents .="1,2005,03,001,001,";
                $contents .="20050101,山田　花子,01,山田　父,やまだ　ちち,1,1960-01-01,";
                $contents .="山田商事,00,01,20050101,1:在学 2:卒業,01,備考,".$this->lastColumn;
                $filename = sprintf("%s.csv", "家族情報");
                break;
            case 2:
                $contents   ="※CSVCD,※年度,学年,組,出席番号,";
                $contents .="※学籍番号,氏名,本人電話番号1,優先順位,本人電話番号2,優先順位,";
                $contents .="緊急連絡先1,緊急連絡先1氏名,緊急連絡先1続柄名,緊急連絡先1電話番号1,番号1備考,優先順位,緊急連絡先1電話番号2,番号2備考,優先順位,";
                $contents .="緊急連絡先2,緊急連絡先2氏名,緊急連絡先2続柄名,緊急連絡先2電話番号1,番号1備考,優先順位,緊急連絡先2電話番号2,番号2備考,優先順位,".$this->lastColumn."\n";
                $contents .="2,2005,03,001,001,";
                $contents .="20050101,山田　花子,99-9999-9999,1,99-9999-9999,2,";
                $contents .="山田商事,山田　父,父,99-9999-9999,職場,3,99-9999-9999,携帯,4,";
                $contents .="母携帯,山田　母,母,090-9999-9999,携帯,5,99-9999-9999,自宅,6,".$this->lastColumn;
                $filename = sprintf("%s.csv", "緊急連絡先情報");
                break;
            case 3:
                $contents   ="※CSVCD,※年度,学年,組,出席番号,";
                $contents .="※学籍番号,氏名,災害時帰宅グループ番号,通学所要時間,通学所要分,最寄駅までの手段,";
                $contents .="最寄駅（自宅）,最寄駅名（自宅）,路線（自宅）,路線名（自宅）,下車駅（自宅）,下車駅名（自宅）,通学手段（自宅）,";
                $contents .="乗車駅1,乗車駅名1,路線1,路線名1,下車駅1,下車駅名1,通学手段1,";
                $contents .="乗車駅2,乗車駅名2,路線2,路線名2,下車駅2,下車駅名2,通学手段2,";
                $contents .="乗車駅3,乗車駅名3,路線3,路線名3,下車駅3,下車駅名3,通学手段3,";
                $contents .="乗車駅4,乗車駅名4,路線4,路線名4,下車駅4,下車駅名4,通学手段4,";
                $contents .="乗車駅5,乗車駅名5,路線5,路線名5,下車駅5,下車駅名5,通学手段5,";
                $contents .="最寄駅（学校）,最寄駅名（学校）,路線（学校）,路線名（学校）,下車駅（学校）,下車駅名（学校）,通学手段（学校）,".$this->lastColumn."\n";
                $contents .="3,2005,03,001,001,";
                $contents .="20050101,山田　花子,01,01,30,1,";
                $contents .="9999999,○○駅,99999,■■線,9999999,△△駅,1,乗車駅,,路線,,下車駅,,2,";
                $contents .="9999999,○○駅,99999,■■線,9999999,△△駅,1,乗車駅,,路線,,下車駅,,2,";
                $contents .="9999999,○○駅,99999,■■線,9999999,△△駅,1,乗車駅,,路線,,下車駅,,2,";
                $contents .="9999999,○○駅,99999,■■線,9999999,△△駅,1,".$this->lastColumn;
                $filename = sprintf("%s.csv", "通学経路情報");
                break;
            case 4:
                $contents   ="※CSVCD,※年度,※学籍番号,学年,組,出席番号,氏名,※登録開始日付,※登録終了日付,※許可番号,駐輪場番号,詳細内容,備考,".$this->lastColumn."\n";
                $contents .="4,2005,20050101,03,001,001,山田　花子,1960-01-01,1960-01-02,12345678,1234,詳細内容40,備考20,".$this->lastColumn;
                $filename = sprintf("%s.csv", "自転車許可番号");
                break;
            case 5:
                $contents   ="※CSVCD,※年度,学年,組,出席番号,";
                $contents .="※学籍番号,氏名,その他の続柄,その他の氏名,その他のかな,その他の性別,";
                $contents .="その他の郵便番号,その他の住所1,その他の住所2,その他の電話番号,その他の職種コード,その他の兼ねている公職,".$this->lastColumn."\n";
                $contents .="5,2005,03,001,001,";
                $contents .="20050101,山田　花子,1,その他の氏名,そのたのしめい,1,";
                $contents .="111-1111,その他の住所１,その他の住所２,99-9999-9999,01,その他の兼ねている公職,".$this->lastColumn;
                $filename = sprintf("%s.csv", "その他（通知票等 送付先）");
                break;
        }
        common::downloadContents($contents, $filename);
        return true;
    }


    //ＣＳＶ出力
    public function getDownloadModel()
    {
        switch ($this->target) {
            case 1:
                $header = array("CSVCD"             => "※CSVCD",
                                "YEAR"              => "※年度",
                                "GRADE"             => "学年",
                                "HR_CLASS"          => "組",
                                "ATTENDNO"          => "出席番号",
                                "SCHREGNO"          => "※学籍番号",
                                "NAME"              => "氏名",
                                "RELANO"            => "※連番(2桁)",
                                "RELANAME"          => "親族氏名",
                                "RELAKANA"          => "親族氏名かな",
                                "RELASEX"           => "親族性別",
                                "RELABIRTHDAY"      => "生年月日",
                                "OCCUPATION"        => "通勤先又は学校名",
                                "REGIDENTIALCD"     => "同居区分",
                                "RELATIONSHIP"      => "親族続柄",
                                "RELA_SCHREGNO"     => "親族学籍番号",
                                "REGD_GRD_FLG"      => "親族在卒区分",
                                "RELA_GRADE"        => "親族学年",
                                "REMARK"            => "備考",
                                $this->lastColumn   => $this->lastColumn);
                break;

            case 2:
                $header = array("CSVCD"                 => "※CSVCD",
                                "YEAR"                  => "※年度",
                                "GRADE"                 => "学年",
                                "HR_CLASS"              => "組",
                                "ATTENDNO"              => "出席番号",
                                "SCHREGNO"              => "※学籍番号",
                                "NAME"                  => "氏名",
                                "TELNO"                 => "本人電話番号1",
                                "PRIORITY1"             => "優先順位",
                                "TELNO2"                => "本人電話番号2",
                                "PRIORITY2"             => "優先順位",
                                "EMERGENCYCALL"         => "緊急連絡先1",
                                "EMERGENCYNAME"         => "緊急連絡先1氏名",
                                "EMERGENCYRELA_NAME"    => "緊急連絡先1続柄名",
                                "EMERGENCYTELNO"        => "緊急連絡先1電話番号1",
                                "E_TELNO_MEMO"          => "番号1備考",
                                "PRIORITY3"             => "優先順位",
                                "EMERGENCYTELNO_2"      => "緊急連絡先1電話番号2",
                                "E_TELNO_MEMO_2"        => "番号2備考",
                                "PRIORITY4"             => "優先順位",
                                "EMERGENCYCALL2"        => "緊急連絡先2",
                                "EMERGENCYNAME2"        => "緊急連絡先2氏名",
                                "EMERGENCYRELA_NAME2"   => "緊急連絡先2続柄名",
                                "EMERGENCYTELNO2"       => "緊急連絡先2電話番号1",
                                "E_TELNO_MEMO2"         => "番号1備考",
                                "PRIORITY5"             => "優先順位",
                                "EMERGENCYTELNO2_2"     => "緊急連絡先2電話番号2",
                                "E_TELNO_MEMO2_2"       => "番号2備考",
                                "PRIORITY6"             => "優先順位",
                                $this->lastColumn       => $this->lastColumn);
                break;

            case 3:
                $header = array("CSVCD"             => "※CSVCD",
                                "YEAR"              => "※年度",
                                "GRADE"             => "学年",
                                "HR_CLASS"          => "組",
                                "ATTENDNO"          => "出席番号",
                                "SCHREGNO"          => "※学籍番号",
                                "NAME"              => "氏名",
                                "GO_HOME_GROUP_NO"  => "災害時帰宅グループ番号",
                                "COMMUTE_HOURS"     => "通学所要時間",
                                "COMMUTE_MINUTES"   => "通学所要分",
                                "HOWTOCOMMUTECD"    => "最寄駅までの手段",
                                "JOSYA_1"           => "最寄駅（自宅）",
                                "JOSYANAME_1"       => "最寄駅名（自宅）",
                                "ROSEN_1"           => "路線（自宅）",
                                "ROSENNAME_1"       => "路線名（自宅）",
                                "GESYA_1"           => "下車駅（自宅）",
                                "GESYANAME_1"       => "下車駅名（自宅）",
                                "FLG_1"             => "通学手段（自宅）",
                                "JOSYA_2"           => "乗車駅1",
                                "JOSYANAME_2"       => "乗車駅名1",
                                "ROSEN_2"           => "路線1",
                                "ROSENNAME_2"       => "路線名1",
                                "GESYA_2"           => "下車駅1",
                                "GESYANAME_2"       => "下車駅名1",
                                "FLG_2"             => "通学手段1",
                                "JOSYA_3"           => "乗車駅2",
                                "JOSYANAME_3"       => "乗車駅名2",
                                "ROSEN_3"           => "路線2",
                                "ROSENNAME_3"       => "路線名2",
                                "GESYA_3"           => "下車駅2",
                                "GESYANAME_3"       => "下車駅名2",
                                "FLG_3"             => "通学手段2",
                                "JOSYA_4"           => "乗車駅3",
                                "JOSYANAME_4"       => "乗車駅名3",
                                "ROSEN_4"           => "路線3",
                                "ROSENNAME_4"       => "路線名3",
                                "GESYA_4"           => "下車駅3",
                                "GESYANAME_4"       => "下車駅名3",
                                "FLG_4"             => "通学手段3",
                                "JOSYA_5"           => "乗車駅4",
                                "JOSYANAME_5"       => "乗車駅名4",
                                "ROSEN_5"           => "路線4",
                                "ROSENNAME_5"       => "路線名4",
                                "GESYA_5"           => "下車駅4",
                                "GESYANAME_5"       => "下車駅名4",
                                "FLG_5"             => "通学手段4",
                                "JOSYA_6"           => "乗車駅5",
                                "JOSYANAME_6"       => "乗車駅名5",
                                "ROSEN_6"           => "路線5",
                                "ROSENNAME_6"       => "路線名5",
                                "GESYA_6"           => "下車駅5",
                                "GESYANAME_6"       => "下車駅名5",
                                "FLG_6"             => "通学手段5",
                                "JOSYA_7"           => "最寄駅（学校）",
                                "JOSYANAME_7"       => "最寄駅名（学校）",
                                "ROSEN_7"           => "路線（学校）",
                                "ROSENNAME_7"       => "路線名（学校）",
                                "GESYA_7"           => "下車駅（学校）",
                                "GESYANAME_7"       => "下車駅名（学校）",
                                "FLG_7"             => "通学手段（学校）",
                                $this->lastColumn   => $this->lastColumn);
                break;
            case 4:
                $header = array("CSVCD"                 => "※CSVCD",
                                "YEAR"                  => "※年度",
                                "SCHREGNO"              => "※学籍番号",
                                "GRADE"                 => "学年",
                                "HR_CLASS"              => "組",
                                "ATTENDNO"              => "出席番号",
                                "NAME"                  => "氏名",
                                "DETAIL_SDATE"          => "※登録開始日付",
                                "DETAIL_EDATE"          => "※登録終了日付",
                                "BICYCLE_CD"            => "※許可番号",
                                "BICYCLE_NO"            => "駐輪場",
                                "CONTENT"               => "詳細内容",
                                "REMARK"                => "備考",
                                $this->lastColumn       => $this->lastColumn);
                break;

            case 5:
                $header = array("CSVCD"                 => "※CSVCD",
                                "YEAR"                  => "※年度",
                                "GRADE"                 => "学年",
                                "HR_CLASS"              => "組",
                                "ATTENDNO"              => "出席番号",
                                "SCHREGNO"              => "※学籍番号",
                                "NAME"                  => "氏名",
                                "SEND_RELATIONSHIP"     => "その他の続柄",
                                "SEND_NAME"             => "その他の氏名",
                                "SEND_KANA"             => "その他のかな",
                                "SEND_SEX"              => "その他の性別",
                                "SEND_ZIPCD"            => "その他の郵便番号",
                                "SEND_ADDR1"            => "その他の住所1",
                                "SEND_ADDR2"            => "その他の住所2",
                                "SEND_TELNO"            => "その他の電話番号",
                                "SEND_JOBCD"            => "その他の職種コード",
                                "PUBLIC_OFFICE"         => "その他の兼ねている公職",
                                $this->lastColumn       => $this->lastColumn);
                break;
        }
        $contents  = "";    //クリア

        if ($this->headercheck=="1") {
            $contents .= implode($header, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();
        $table = "";
        $where = "";
        $query = knjh140aquery::getselectDataSQL($this);
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            if ($this->target == 3) {
                for ($i=1; $i < 8; $i++) {
                    if ($row["FLG_".$i] == "1") {
                        $row["JOSYANAME_".$i] = ($row["JOSYA_".$i]) ? $db->getOne(knjh140aquery::getStationNetInfo("STATION_NAME", "STATION_CD", $row["JOSYA_".$i])) : "";
                        $row["ROSENNAME_".$i] = ($row["ROSEN_".$i]) ? $db->getOne(knjh140aquery::getStationNetInfo("LINE_NAME", "LINE_CD", $row["ROSEN_".$i])) : "";
                        $row["GESYANAME_".$i] = ($row["GESYA_".$i]) ? $db->getOne(knjh140aquery::getStationNetInfo("STATION_NAME", "STATION_CD", $row["GESYA_".$i])) : "";
                    }
                }
            }

            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->target==1) {
                $couse = "家族情報";
            } elseif ($this->target==2) {
                $couse = "緊急連絡先情報";
            } elseif ($this->target==3) {
                $couse = "通学経路情報";
            } elseif ($this->target==4) {
                $couse = "自転車許可番号";
            } elseif ($this->target==5) {
                $couse = "その他（通知票等 送付先）";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    public function chkHankaku($chkStr)
    {
        foreach (preg_split('//u', $chkStr, -1, PREG_SPLIT_NO_EMPTY) as $idx => $v) {
            if (strlen($v) == 1) {
                return false;
            }
        }
        return true;
    }

    /**
    * 日付変換 項目ﾁｪｯｸ後に行う。
    */
    public function dateRep($data)
    {
        if ($data!="") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }
        return $data;
    }
}
