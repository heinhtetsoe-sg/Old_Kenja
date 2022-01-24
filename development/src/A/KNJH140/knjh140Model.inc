<?php

require_once('for_php7.php');

class knjh140Model extends Model
{
    var $cmd;
    var $file;
    var $field    = array();
    var $data_arr = array();
    var $controls = array();
    var $headercheck;
    var $testsubclasscd;
    var $testsubclassnm;
    var $coursecd;
    var $majorcd;
    var $coursecode;
    var $wk;
    var $appli;

    function knjh140Model()
    {
        $this->year = CTRL_YEAR + 1;
    }

    function init()
    {
        $this->field = array("YEAR"           =>  VARS::post("YEAR"),             //年度一覧
                             "GRADE_HR_CLASS" =>  VARS::post("GRADE_HR_CLASS")    //年組一覧
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("PROGRAMID")) {
            $this->headercheck = "1";
            $this->target      = 1;
        } else {
            $this->headercheck = VARS::post("HEADERCHECK");
            $this->target      = VARS::post("TARGET");
        }

        $this->file = VARS::file("FILE");
    }

    function &getUpdateModel()
    {
        //事項時間の制限を無くす
        set_time_limit(0);

        $this->data_arr = array();
        $this->errData  = array();
        //ファイルの文字列の取得
        $data_arr = $this->GetFileString();

        //データの整合性チェック
        $erraDatas = "";
        switch ($this->target) {
            case 1:
                $tables = array("1" => "SCHREG_RELA_DAT");
                break;
            case 2:
                $tables = array("1" => "SCHREG_BASE_MST");
                break;
            case 3:
                $tables = array("1" => "SCHREG_ENVIR_DAT");
                break;
            case 4:
                $tables = array("1" => "SCHREG_DETAILHIST_DAT");
                break;
        }
        $header = array("SCHREGNO"     => "学籍番号",
                        "RELANO"       => "連番",
                        "YEAR"         => "年度",
                        "DETAIL_DIV"   => "詳細区分",
                        "DETAIL_SDATE" => "登録開始日付"
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
            knjh140Query::InsertQuery($this, $data_arr);
            $this->setMessage("MSG202", "読込み件数：".get_count($data_arr)."件");
        }
    }

    function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            switch ($this->target) {
                case 1:
                    $checkdata = array( "YEAR"                  => array(4,"年度"),
                                        "SCHREGNO"              => array(8,"学籍番号"),
                                        "RELANO"                => array(2,"連番"),
                                        "RELANAME"              => array(60,"親族氏名"),
                                        "RELAKANA"              => array(120,"親族氏名かな"),
                                        "RELASEX"               => array(1,"親族性別"),
                                        "RELABIRTHDAY"          => array(10,"生年月日"),
                                        "OCCUPATION"            => array(60,"勤務先または学校名"),
                                        "REGIDENTIALCD"         => array(2,"同居区分"),
                                        "RELATIONSHIP"          => array(2,"親族続柄"),
                                        "RELA_SCHREGNO"         => array(8,"親族学籍番号"),
                                        "REMARK"                => array(45,"備考"));
                    break;
                case 2:
                    $checkdata = array( "YEAR"                  => array(4,"年度"),
                                        "SCHREGNO"              => array(8,"学籍番号"),
                                        "EMERGENCYCALL"         => array(60,"緊急連絡先"),
                                        "EMERGENCYNAME"         => array(60,"緊急連絡先氏名"),
                                        "EMERGENCYRELA_NAME"    => array(30,"緊急連絡先続柄名"),
                                        "EMERGENCYTELNO"        => array(14,"緊急連絡先電話番号"),
                                        "EMERGENCYCALL2"        => array(60,"緊急連絡先2"),
                                        "EMERGENCYNAME2"        => array(60,"緊急連絡先氏名2"),
                                        "EMERGENCYRELA_NAME2"   => array(30,"緊急連絡先続柄名2"),
                                        "EMERGENCYTELNO2"       => array(14,"緊急連絡先電話番号2"));
                    break;
                case 3:
                    $checkdata = array( "YEAR"                 => array(4,"年度"),
                                        "SCHREGNO"             => array(8,"学籍番号"),
                                        "COMMUTE_HOURS"        => array(2,"通学所要時間"),
                                        "COMMUTE_MINUTES"      => array(2,"通学所要分"),
                                        "STATIONNAME"          => array(30,"最寄駅名"),
                                        "OTHERHOWTOCOMMUTE"    => array(30,"最寄駅路線名"),
                                        "HOWTOCOMMUTECD"       => array(1,"最寄駅までの手段"),
                                        "CHANGETRAIN1"         => array(7,"途中経由駅1"),
                                        "CHANGETRAIN2"         => array(7,"途中経由駅2"),
                                        "CHANGETRAIN3"         => array(7,"途中経由駅3"),
                                        "CHANGETRAIN4"         => array(7,"途中経由駅4"),
                                        "CHANGETRAIN5"         => array(7,"途中経由駅5"),
                                        "CHANGETRAIN6"         => array(7,"途中経由駅6"),
                                        "CHANGETRAIN7"         => array(7,"途中経由駅7"),
                                        "CHANGETRAIN8"         => array(7,"途中経由駅8"));
                    break;
    //NO001↓
                case 4:
                    $checkdata = array( "YEAR"                 => array(4,"年度"),
                                        "SCHREGNO"             => array(8,"学籍番号"),
                                        "DETAIL_SDATE"         => array(10,"登録開始日付"),
                                        "DETAIL_EDATE"         => array(10,"登録終了日付"),
                                        "BICYCLE_CD"           => array(8,"許可番号"),
                                        "BICYCLE_NO"           => array(4,"駐輪場番号"),
                                        "CONTENT"              => array(120,"詳細内容"),
                                        "REMARK"               => array(60,"備考"));
                    break;
    //NO001↑
            }
            $db = Query::dbCheckOut();
            $fp = fopen($this->file["tmp_name"], "r");
            $i    = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            switch ($this->target) {
                case 1:
                    $primaryKey1 = $this->getPrimaryKey('SCHREG_RELA_DAT');
                    break;
                case 2:
                    $primaryKey1 = $this->getPrimaryKey('SCHREG_BASE_MST');
                    break;
                case 3:
                    $primaryKey1 = $this->getPrimaryKey('SCHREG_ENVIR_DAT');
                    break;
                case 4:
                    $primaryKey1 = $this->getPrimaryKey('SCHREG_DETAILHIST_DAT');
                    break;
            }
            $duplicateCsv1 = array();
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->headercheck == 1) {
                        $line++;
                        $header = true;
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
                            $data = array ( "YEAR"             => $tmp[1],
                                            "GRADE"            => (strlen(trim($tmp[2]))) ? sprintf("%02d", $tmp[2]) : "",
                                            "HR_CLASS"         => (strlen(trim($tmp[3]))) ? sprintf("%03d", $tmp[3]) : "",
                                            "ATTENDNO"         => (strlen(trim($tmp[4]))) ? sprintf("%03d", $tmp[4]) : "",
                                            "SCHREGNO"         => $tmp[5],
                                            "NAME"             => $tmp[6],
                                            "RELANO"           => $tmp[7],
                                            "RELANAME"         => $tmp[8],
                                            "RELAKANA"         => $tmp[9],
                                            "RELASEX"          => $tmp[10],
                                            "RELABIRTHDAY"     => $tmp[11],
                                            "OCCUPATION"       => $tmp[12],
                                            "REGIDENTIALCD"    => (strlen(trim($tmp[13]))) ? sprintf("%02d", $tmp[13]) : "",
                                            "RELATIONSHIP"     => (strlen(trim($tmp[14]))) ? sprintf("%02d", $tmp[14]) : "",
                                            "RELA_SCHREGNO"    => $tmp[15],
                                            "REMARK"           => $tmp[16]);
                            break;
                        case 2:
                            if ($tmp[0] != 2) {
                                $this->warning = "CSVCDが緊急連絡先情報ではありません。\\n緊急連絡先情報【2】";
                            }
                            $data = array ( "YEAR"               => $tmp[1],
                                            "GRADE"              => (strlen(trim($tmp[2]))) ? sprintf("%02d", $tmp[2]) : "",
                                            "HR_CLASS"           => (strlen(trim($tmp[3]))) ? sprintf("%03d", $tmp[3]) : "",
                                            "ATTENDNO"           => (strlen(trim($tmp[4]))) ? sprintf("%03d", $tmp[4]) : "",
                                            "SCHREGNO"           => $tmp[5],
                                            "NAME"               => $tmp[6],
                                            "EMERGENCYCALL"      => $tmp[7],
                                            "EMERGENCYNAME"      => $tmp[8],
                                            "EMERGENCYRELA_NAME" => $tmp[9],
                                            "EMERGENCYTELNO"     => $tmp[10],
                                            "EMERGENCYCALL2"     => $tmp[11],
                                            "EMERGENCYNAME2"     => $tmp[12],
                                            "EMERGENCYRELA_NAME2"=> $tmp[13],
                                            "EMERGENCYTELNO2"    => $tmp[14]);
                            break;
                        case 3:
                            if ($tmp[0] != 3) {
                                $this->warning = "CSVCDが通学経路情報ではありません。\\n通学経路情報【3】";
                            }
                            $data = array ( "YEAR"                => $tmp[1],
                                            "GRADE"               => (strlen(trim($tmp[2]))) ? sprintf("%02d", $tmp[2]) : "",
                                            "HR_CLASS"            => (strlen(trim($tmp[3]))) ? sprintf("%03d", $tmp[3]) : "",
                                            "ATTENDNO"            => (strlen(trim($tmp[4]))) ? sprintf("%03d", $tmp[4]) : "",
                                            "SCHREGNO"            => $tmp[5],
                                            "NAME"                => $tmp[6],
                                            "COMMUTE_HOURS"       => (strlen(trim($tmp[7]))) ? sprintf("%02d", $tmp[7]) : "",
                                            "COMMUTE_MINUTES"     => (strlen(trim($tmp[8]))) ? sprintf("%02d", $tmp[8]) : "",
                                            "STATIONNAME"         => $tmp[9],
                                            "OTHERHOWTOCOMMUTE"   => $tmp[10],
                                            "HOWTOCOMMUTECD"      => $tmp[11],
                                            "CHANGETRAIN1"        => (strlen(trim($tmp[12]))) ? sprintf("%07d", $tmp[12]) : "",
                                            "CHANGETRAIN2"        => (strlen(trim($tmp[13]))) ? sprintf("%07d", $tmp[13]) : "",
                                            "CHANGETRAIN3"        => (strlen(trim($tmp[14]))) ? sprintf("%07d", $tmp[14]) : "",
                                            "CHANGETRAIN4"        => (strlen(trim($tmp[15]))) ? sprintf("%07d", $tmp[15]) : "",
                                            "CHANGETRAIN5"        => (strlen(trim($tmp[16]))) ? sprintf("%07d", $tmp[16]) : "",
                                            "CHANGETRAIN6"        => (strlen(trim($tmp[17]))) ? sprintf("%07d", $tmp[17]) : "",
                                            "CHANGETRAIN7"        => (strlen(trim($tmp[18]))) ? sprintf("%07d", $tmp[18]) : "",
                                            "CHANGETRAIN8"        => (strlen(trim($tmp[19]))) ? sprintf("%07d", $tmp[19]) : "");
                            break;
//NO001↓
                        case 4:
                            if ($tmp[0] != 4) {
                                $this->warning = "CSVCDが自転車許可番号ではありません。\\n自転車許可番号【4】";
                            }
                            $data = array ( "YEAR"                 => $tmp[1],
                                            "SCHREGNO"             => $tmp[2],
                                            "GRADE"                => (strlen(trim($tmp[3]))) ? sprintf("%02d", $tmp[3]) : "",
                                            "HR_CLASS"             => (strlen(trim($tmp[4]))) ? sprintf("%03d", $tmp[4]) : "",
                                            "ATTENDNO"             => (strlen(trim($tmp[5]))) ? sprintf("%03d", $tmp[5]) : "",
                                            "NAME"                 => $tmp[6],
                                            "DETAIL_SDATE"         => (strlen(trim($tmp[7]))) ? str_replace("/", "-", $tmp[7]) : "",
                                            "DETAIL_EDATE"         => (strlen(trim($tmp[8]))) ? str_replace("/", "-", $tmp[8]) : "",
                                            "BICYCLE_CD"           => $tmp[9],
                                            "BICYCLE_NO"           => $tmp[10],
                                            "CONTENT"              => $tmp[11],
                                            "REMARK"               => $tmp[12]);
                            break;
//NO001↑
                    }
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                         $duplicateCsv1[$line][$val] = $data[$val];
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
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $otherErrFlg        = false;
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $otherSep           = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
          //必須項目チェック
            if (in_array($key, array("YEAR","SCHREGNO","RELANO","DETAIL_SDATE","DETAIL_EDATE","BICYCLE_CD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if ($this->target == 1) {
                if (in_array($key, array("RELANO", "REGIDENTIALCD", "RELATIONSHIP"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }
            if ($this->target == 3) {
                if (in_array($key, array("COMMUTE_HOURS", "COMMUTE_MINUTES", "HOWTOCOMMUTECD", "CHANGETRAIN1", "CHANGETRAIN2", "CHANGETRAIN3", "CHANGETRAIN4", "CHANGETRAIN5", "CHANGETRAIN6", "CHANGETRAIN7", "CHANGETRAIN8"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }

            //登録開始日付と登録終了日付の日付型チェック
            if (in_array($key, array("DETAIL_SDATE","DETAIL_EDATE"))) {
                if (common::isDate($data[$key]) == false) {
                     $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※日付(フォーマット):", $otherSep, $val[1]);
                }
            }

             //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                 $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }
        //登録開始日付と登録終了日付の大小チェック
        if ($this->dateRep($data["DETAIL_SDATE"]) != "" && $this->dateRep($data["DETAIL_EDATE"]) != "" && $this->dateRep($data["DETAIL_SDATE"]) > $this->dateRep($data["DETAIL_EDATE"])) {
            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※日付の大小:", $numericSep, $val[1]);
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$otherErrMsg;
            $checkError = $this->checkErrLength($checkError);
            $this->errData[$line]["MSG"] = $checkError;
        }
        return $returnFlg;
    }

    //テンプレートの書き出し
    function OutputTmpFile()
    {
        switch ($this->target) {
            case 1:
                $contents ="※CSVCD,※年度,学年,組,出席番号,";
                $contents .="※学籍番号,氏名,※連番(2桁),親族氏名,親族氏名かな,親族性別,生年月日,";
                $contents .="通勤先又は学校名,同居区分,親族続柄,親族学籍番号,備考,DUMMY\n";
                $contents .="1,2005,03,001,001,";
                $contents .="20050101,山田　花子,01,山田　父,やまだ　ちち,1,1960-01-01,";
                $contents .="山田商事,00,01,20050101,備考,DUMMY";
                $filename = sprintf("%s.csv", "家族情報");
                break;
            case 2:
                $contents ="※CSVCD,※年度,学年,組,出席番号,";
                $contents .="※学籍番号,氏名,緊急連絡先,緊急連絡先氏名,緊急連絡先続柄名,緊急連絡先電話番号,";
                $contents .="緊急連絡先２,緊急連絡先氏名２,緊急連絡先続柄名２,緊急連絡先電話番号２,DUMMY\n";
                $contents .="2,2005,03,001,001,";
                $contents .="20050101,山田　花子,山田商事,山田　父,父,99-9999-9999,";
                $contents .="母携帯,山田　母,母,090-9999-9999,DUMMY";
                $filename = sprintf("%s.csv", "緊急連絡先情報");
                break;
            case 3:
                $contents ="※CSVCD,※年度,学年,組,出席番号,";
                $contents .="※学籍番号,氏名,通学所要時間,通学所要分,最寄駅名,最寄駅路線名,最寄駅までの手段,";
                $contents .="途中経由駅1,途中経由駅2,途中経由駅3,途中経由駅4,途中経由駅5,途中経由駅6,途中経由駅7,途中経由駅8,DUMMY\n";
                $contents .="3,2005,03,001,001,";
                $contents .="20050101,山田　花子,01,30,最寄,路線,1,";
                $contents .="01,02,03,04,05,06,07,08,DUMMY";
                $filename = sprintf("%s.csv", "通学経路情報");
                break;
//NO001↓
            case 4:
                $contents ="※CSVCD,※年度,※学籍番号,学年,組,出席番号,氏名,※登録開始日付,※登録終了日付,※許可番号,駐輪場番号,詳細内容,備考,DUMMY\n";
                $contents .="4,2005,20050101,03,001,001,山田　花子,1960-01-01,1960-01-02,12345678,1234,詳細内容40,備考20,DUMMY";
                $filename = sprintf("%s.csv", "自転車許可番号");
                break;
//NO001↑
        }
        common::downloadContents($contents, $filename);
        return true;
    }


    //ＣＳＶ出力
    function getDownloadModel()
    {
        switch ($this->target) {
            case 1:
                $header = array("CSVCD"            => "※CSVCD",
                                "YEAR"             => "※年度",
                                "GRADE"            => "学年",
                                "HR_CLASS"         => "組",
                                "ATTENDNO"         => "出席番号",
                                "SCHREGNO"         => "※学籍番号",
                                "NAME"             => "氏名",
                                "RELANO"           => "※連番(2桁)",
                                "RELANAME"         => "親族氏名",
                                "RELAKANA"         => "親族氏名かな",
                                "RELASEX"          => "親族性別",
                                "RELABIRTHDAY"     => "生年月日",
                                "OCCUPATION"       => "通勤先又は学校名",
                                "REGIDENTIALCD"    => "同居区分",
                                "RELATIONSHIP"     => "親族続柄",
                                "RELA_SCHREGNO"    => "親族学籍番号",
                                "REMARK"           => "備考",
                                "DUMMY"            => "DUMMY");
                break;

            case 2:
                $header = array("CSVCD"              => "※CSVCD",
                                "YEAR"               => "※年度",
                                "GRADE"              => "学年",
                                "HR_CLASS"           => "組",
                                "ATTENDNO"           => "出席番号",
                                "SCHREGNO"           => "※学籍番号",
                                "NAME"               => "氏名",
                                "EMERGENCYCALL"      => "緊急連絡先",
                                "EMERGENCYNAME"      => "緊急連絡先氏名",
                                "EMERGENCYRELA_NAME" => "緊急連絡先続柄名",
                                "EMERGENCYTELNO"     => "緊急連絡先電話番号",
                                "EMERGENCYCALL2"     => "緊急連絡先２",
                                "EMERGENCYNAME2"     => "緊急連絡先氏名２",
                                "EMERGENCYRELA_NAME2"=> "緊急連絡先続柄名２",
                                "EMERGENCYTELNO2"    => "緊急連絡先電話番号２",
                                "DUMMY"              => "DUMMY");
                break;

            case 3:
                $header = array("CSVCD"               => "※CSVCD",
                                "YEAR"                => "※年度",
                                "GRADE"               => "学年",
                                "HR_CLASS"            => "組",
                                "ATTENDNO"            => "出席番号",
                                "SCHREGNO"            => "※学籍番号",
                                "NAME"                => "氏名",
                                "COMMUTE_HOURS"       => "通学所要時間",
                                "COMMUTE_MINUTES"     => "通学所要分",
                                "STATIONNAME"         => "最寄駅名",
                                "OTHERHOWTOCOMMUTE"   => "最寄駅路線名",
                                "HOWTOCOMMUTECD"      => "最寄駅までの手段",
                                "CHANGETRAIN1"        => "途中経由駅1",
                                "CHANGETRAIN2"        => "途中経由駅2",
                                "CHANGETRAIN3"        => "途中経由駅3",
                                "CHANGETRAIN4"        => "途中経由駅4",
                                "CHANGETRAIN5"        => "途中経由駅5",
                                "CHANGETRAIN6"        => "途中経由駅6",
                                "CHANGETRAIN7"        => "途中経由駅7",
                                "CHANGETRAIN8"        => "途中経由駅8",
                                "DUMMY"               => "DUMMY");
                break;
//NO001↓
            case 4:
                $header = array("CSVCD"               => "※CSVCD",
                                "YEAR"                => "※年度",
                                "SCHREGNO"            => "※学籍番号",
                                "GRADE"               => "学年",
                                "HR_CLASS"            => "組",
                                "ATTENDNO"            => "出席番号",
                                "NAME"                => "氏名",
                                "DETAIL_SDATE"        => "※登録開始日付",
                                "DETAIL_EDATE"        => "※登録終了日付",
                                "BICYCLE_CD"          => "※許可番号",
                                "BICYCLE_NO"          => "駐輪場",
                                "CONTENT"             => "詳細内容",
                                "REMARK"              => "備考",
                                "DUMMY"               => "DUMMY");
                break;
//NO001↑
        }
        $contents  = "";    //クリア

        if ($this->headercheck=="1") {
            $contents .= implode($header, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();
        $table = "";
        $where = "";
        $query = knjh140query::getselectDataSQL($this);
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
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
            } else {
                $couse = "自転車許可番号";
            }
            $filename = sprintf("%s.csv", $couse);
            $filename = i18n_convert($filename, "SJIS-win");
            $contents = i18n_convert($contents, "SJIS-win");
            /* HTTPヘッダの出力 */
            i18n_http_output("pass");
            header("Accept-Ranges: none");
            header("Content-Disposition: inline; filename=$filename");
            header("Content-Transfer-Encoding: binary");
            header("Content-Length: ". strlen($contents));
            header("Content-Type: text/octet-stream");

            echo $contents;
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
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
?>
