<?php

require_once('for_php7.php');
class knja062aModel extends Model
{
    public $fields      = array();
    public $cmd;
    public $hr_class;        //primary
    public $grade;
    public $term;
    public $term2;
    public $control;
    public $check;
    public $grd_div;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $defFlg;
    public $lastColumn;//CSVの最後の列の値

    public function knja062aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
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
        $this->getPropertiesAll(); //プロパティファイル

        $this->cmd          = VARS::request("cmd");
        if ($this->cmd == '') {
            unset($this->term);
            unset($this->term2);
            unset($this->check);
            unset($this->grd_div);
            $this->defFlg = "on";
        }

        if (VARS::request("term") != "") {
            $this->term  = VARS::request("term");
            list($this->term_year, $this->term_semester) = preg_split('/-/', $this->term);
        }
        if (VARS::request("term2") != "") {
            $this->term2 = VARS::request("term2");
        }
        if ((VARS::request("PROGRAMID") != "") && (CTRL_SEMESTER != $this->control["学期数"])) {
            $semester = CTRL_SEMESTER +1;
            $this->term  = CTRL_YEAR. "-" .$semester;
        }
        if ((VARS::request("PROGRAMID") != "") && (CTRL_SEMESTER == $this->control["学期数"])) {
            $year = CTRL_YEAR + 1;
            $semester = 1;
            $this->term  = $year. "-" .$semester;
        }

        $this->fields = array("GRADE"           =>  VARS::post("GRADE"),
                              "HR_CLASS"        =>  VARS::post("HR_CLASS"),
                              "RECORD_DIV"      =>  VARS::post("RECORD_DIV"),
                              "HR_NAME"         =>  VARS::post("HR_NAME"),
                              "HR_NAMEABBV"     =>  VARS::post("HR_NAMEABBV"),
                              "HR_CLASS_NAME1"  =>  VARS::post("HR_CLASS_NAME1"),
                              "HR_CLASS_NAME2"  =>  VARS::post("HR_CLASS_NAME2"),
                              "GRADE_NAME"      =>  VARS::post("GRADE_NAME"),
                              "TR_CD1"          =>  VARS::post("TR_CD1"),
                              "TR_CD2"          =>  VARS::post("TR_CD2"),
                              "TR_CD3"          =>  VARS::post("TR_CD3"),
                              "SUBTR_CD1"       =>  VARS::post("SUBTR_CD1"),
                              "SUBTR_CD2"       =>  VARS::post("SUBTR_CD2"),
                              "SUBTR_CD3"       =>  VARS::post("SUBTR_CD3"),
                              "HR_FACCD"        =>  VARS::post("HR_FACCD"),
                              "CLASSWEEKS"      =>  VARS::post("CLASSWEEKS"),
                              "CLASSDAYS"       =>  VARS::post("CLASSDAYS"),
                              "UPDATED"         =>  VARS::post("UPDATED"),

                              "TR_DIV1"          =>  VARS::post("TR_DIV1"),           //担任区分
                              "FROM_DATE1"       =>  VARS::post("FROM_DATE1"),        //開始日付
                              "TO_DATE1"         =>  VARS::post("TO_DATE1"),          //終了日付

                              "TR_DIV2"          =>  VARS::post("TR_DIV2"),           //担任区分
                              "FROM_DATE2"       =>  VARS::post("FROM_DATE2"),        //開始日付
                              "TO_DATE2"         =>  VARS::post("TO_DATE2"),          //終了日付

                              "TR_DIV3"          =>  VARS::post("TR_DIV3"),           //担任区分
                              "FROM_DATE3"       =>  VARS::post("FROM_DATE3"),        //開始日付
                              "TO_DATE3"         =>  VARS::post("TO_DATE3"),          //終了日付

                              "TR_DIV4"          =>  VARS::post("TR_DIV4"),           //担任区分
                              "FROM_DATE4"       =>  VARS::post("FROM_DATE4"),        //開始日付
                              "TO_DATE4"         =>  VARS::post("TO_DATE4"),          //終了日付

                              "TR_DIV5"          =>  VARS::post("TR_DIV5"),           //担任区分
                              "FROM_DATE5"       =>  VARS::post("FROM_DATE5"),        //開始日付
                              "TO_DATE5"         =>  VARS::post("TO_DATE5"),          //終了日付

                              "TR_DIV6"          =>  VARS::post("TR_DIV6"),           //担任区分
                              "FROM_DATE6"       =>  VARS::post("FROM_DATE6"),        //開始日付
                              "TO_DATE6"         =>  VARS::post("TO_DATE6"),          //終了日付

                              "HEADER"           =>  VARS::post("HEADER"),             //ヘッダの有無
                              "OUTPUT"           =>  VARS::post("OUTPUT"),             //処理の種類(1:取込、2:書出、3:エラー出力)

                              "CLASS_RADIO"      =>  VARS::post("CLASS_RADIO")         //クラス種別ラジオ
                             );

        $this->cmd = VARS::request("cmd");

        $this->file = VARS::file("FILE");


        $temp = VARS::get("HR_CLASS");
        if (isset($temp)) {
            $this->hr_class = VARS::get("HR_CLASS");
        }
        $temp = VARS::get("GRADE");
        if (isset($temp)) {
            $this->grade    = VARS::get("GRADE");
        }
        if (VARS::get("init") == "1") {
            unset($this->grade);
            unset($this->hr_class);
        }
        $this->check = VARS::request("check");
        $this->grd_div = VARS::request("grd_div");

        //生徒項目名切替処理
        $schName = "";
        $db = Query::dbCheckOut();
        //テーブルの有無チェック
        $query = knja062aquery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && $this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            //生徒項目名取得
            $schName = $db->getOne(knja062aquery::getSchName($this));
        }
        $this->sch_label = (strlen($schName) > 0) ? $schName : '生徒';
        Query::dbCheckIn($db);

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //CVSファイル
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        $zero_ume = "";
        //STAFFCDフィールドサイズ変更対応
        if ($this->Properties["useStaffcdFieldSize"] === '10') {
            $zero_ume = "000000000";
        } else {
            $zero_ume = "0000000";
        }

        if ($this->fields["OUTPUT"] == "3") {
            //ヘッダ、エラー出力
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");

            $headerDuplicate = array("YEAR"                 => "年度",
                                    "SEMESTER"              => "学期",
                                    "GRADE"                 => "学年",
                                    "HR_CLASS"              => "組",
                                    "TR_DIV"                => "担任区分",
                                    "FROM_DATE"             => "担当開始日");
            $tables = array();
            $tables = array("1" => "SCHREG_REGD_FI_HDAT", "2" => "STAFF_CLASS_HIST_FI_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header, ",")."\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } elseif ($this->fields["OUTPUT"] == "2" || $this->fields["OUTPUT"] == "4") {
            //ヘッダ、書出
            $header = array("※年度",
                            "※学期",
                            "※学年",
                            "※組",
                            "※クラス形態",
                            "年組名称",
                            "年組略称",
                            "組名称１",
                            "組名称２",
                            "担任コード1",
                            "担当開始日",
                            "担当終了日",
                            "担任コード2",
                            "担当開始日",
                            "担当終了日",
                            "担任コード3",
                            "担当開始日",
                            "担当終了日",
                            "副担任コード1",
                            "担当開始日",
                            "担当終了日",
                            "副担任コード2",
                            "担当開始日",
                            "担当終了日",
                            "副担任コード3",
                            "担当開始日",
                            "担当終了日",
                            "HR施設コード",
                            "学期授業週数",
                            "学期授業日数",
                            $this->lastColumn
                            );
            if ($this->fields["HEADER"]) {
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }
            $query = knja062aQuery::getCsvData($this);
        }
        if ($this->fields["OUTPUT"] == "4") {
            $contents .= "2011,";          // YEAR                年度
            $contents .= "1,";             // SEMESTER            学期
            $contents .= "01,";            // GRADE               学年
            $contents .= "001,";           // HR_CLASSE           組
            $contents .= "1,";             // RECORD_DIV          クラス形態
            $contents .= "１年１組,";      // HR_NAME             年組名称
            $contents .= "1-1,";           // HR_NAMEABBV         年組略称
            $contents .= "全角10文字,";    // HR_CLASS_NAME1      組名称１
            $contents .= "全角10文字,";    // HR_CLASS_NAME2      組名称２
            $contents .= $zero_ume."1,";   // TR_CD1              担任コード1
            $contents .= "2011/04/05,";    // FROM_DATE           担当開始日
            $contents .= "2011/07/21,";    // TO_DATE             担当終了日
            $contents .= $zero_ume."2,";   // TR_CD2              担任コード2
            $contents .= "2011/04/05,";    // FROM_DATE           担当開始日
            $contents .= "2011/07/21,";    // TO_DATE             担当終了日
            $contents .= $zero_ume."3,";   // TR_CD3              担任コード3
            $contents .= "2011/04/05,";    // FROM_DATE           担当開始日
            $contents .= "2011/07/21,";    // TO_DATE             担当終了日
            $contents .= $zero_ume."4,";   // SUBTR_CD1           副担任コード1
            $contents .= "2011/04/05,";    // FROM_DATE           担当開始日
            $contents .= "2011/07/21,";    // TO_DATE             担当終了日
            $contents .= $zero_ume."5,";   // SUBTR_CD2           副担任コード2
            $contents .= "2011/04/05,";    // FROM_DATE           担当開始日
            $contents .= "2011/07/21,";    // TO_DATE             担当終了日
            $contents .= $zero_ume."6,";   // SUBTR_CD3           副担任コード3
            $contents .= "2011/04/05,";    // FROM_DATE           担当開始日
            $contents .= "2011/07/21,";    // TO_DATE             担当終了日
            $contents .= "半角3文字,";     // HR_FACCD            HR施設コード
            $contents .= "半角2文字,";     // CLASSWEEKS          学期授業週数
            $contents .= "半角2文字,";     // CLASSDAYS           学期授業日数
            $contents .= $this->lastColumn."\n";

            $data = true;
        } else {
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //$row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあれば置換、置換する文字はカンマでなければ何でも良い。
                //置換（日付）
                foreach ($row as $key => $val) {
                    if (preg_match('/DATE/', $key) && $row[$key] != "") {
                        $row[$key] = str_replace("-", "/", $val);
                    }
                }

                //配列の値を,で連結
                $contents .= implode($row, ",") ."\n";
                $data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }
        if ($data) {
            if ($this->fields["OUTPUT"] == "3") {
                $couse="クラス詳細一覧エラー";
            } else {
                $couse="クラス詳細一覧";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //ここから取り込み処理

    //CVSファイル
    //クライアント ⇒ サーバ側の処理
    public function getUploadCsvModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                knja062aQuery::insertQueryCsv($db, $this, $this->data_arr);
                knja062aQuery::insert2QueryCsv($db, $this, $this->data_arr);
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //送られたファイルの文字列を取得
    public function getFileString()
    {
        $setsize = "";
        $zero_ume = "";
        //STAFFCDフィールドサイズ変更対応
        if ($this->Properties["useStaffcdFieldSize"] === '10') {
            $setsize = 10;
            $zero_ume = "%010s";
        } else {
            $setsize = 8;
            $zero_ume = "%08s";
        }

        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("YEAR"                   => array(4   ,"年度"         ), //0
                               "SEMESTER"               => array(1   ,"学期"         ), //1
                               "GRADE"                  => array(2   ,"学年"         ), //2
                               "HR_CLASS"               => array(3   ,"組"           ), //3
                               "RECORD_DIV"             => array(1   ,"クラス形態"   ), //1
                               "HR_NAME"                => array(15  ,"年組名称"     ), //4
                               "HR_NAMEABBV"            => array(5   ,"年組略称"     ), //5 ,
                               "HR_CLASS_NAME1"         => array(30  ,"組名称１"     ), //6
                               "HR_CLASS_NAME2"         => array(30  ,"組名称２"     ), //7
                               "TR_DIV1"                => array(1   ,"担任区分1"  ),   //8
                               "TR_CD1"                 => array($setsize   ,"担任コード1"  ), //8
                               "FROM_DATE1"             => array(6   ,"担当開始日"  ),  //9
                               "TO_DATE1"               => array(6   ,"担当終了日"  ),  //10
                               "TR_DIV2"                => array(1   ,"担任区分2"  ),   //8
                               "TR_CD2"                 => array($setsize   ,"担任コード2"  ), //11
                               "FROM_DATE2"             => array(6   ,"担当開始日"  ),  //12
                               "TO_DATE2"               => array(6   ,"担当終了日"  ),  //13
                               "TR_DIV3"                => array(1   ,"担任区分3"  ),   //8
                               "TR_CD3"                 => array($setsize   ,"担任コード3"  ), //14
                               "FROM_DATE3"             => array(6   ,"担当開始日"  ),  //15
                               "TO_DATE3"               => array(6   ,"担当終了日"  ),  //16
                               "TR_DIV4"                => array(1   ,"担任区分4"  ),   //8
                               "SUBTR_CD1"              => array($setsize   ,"副担任コード1"), //17
                               "FROM_DATE4"             => array(6   ,"担当開始日"  ),  //18
                               "TO_DATE4"               => array(6   ,"担当終了日"  ),  //19
                               "TR_DIV5"                => array(1   ,"担任区分5"  ),   //8
                               "SUBTR_CD2"              => array($setsize   ,"副担任コード2"), //20
                               "FROM_DATE5"             => array(6   ,"担当開始日"  ),  //21
                               "TO_DATE5"               => array(6   ,"担当終了日"  ),  //22
                               "TR_DIV6"                => array(1   ,"担任区分6"  ),   //8
                               "SUBTR_CD3"              => array($setsize   ,"副担任コード3"), //23
                               "FROM_DATE6"             => array(6   ,"担当開始日"  ),  //24
                               "TO_DATE6"               => array(6   ,"担当終了日"  ),  //25
                               "HR_FACCD"               => array(4   ,"HR施設コード" ), //26
                               "CLASSWEEKS"             => array(2   ,"学期授業週数" ), //26
                               "CLASSDAYS"              => array(2   ,"学期授業日数" )  //27
                               );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;

            //取得主キー
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
            $duplicateCsv4 = array();
            $duplicateCsv5 = array();
            $duplicateCsv6 = array();
            $duplicateCsv7 = array();
            $primaryKey = array();
            $primaryKey1 = array();
            $primaryKey2 = array();
            $primaryKey3 = array();
            $primaryKey4 = array();
            $primaryKey5 = array();
            $primaryKey6 = array();
            $primaryKey7 = array();
            $primaryKey1 = $this->getPrimaryKey('SCHREG_REGD_FI_HDAT');
            $primaryKey = $this->getPrimaryKey('STAFF_CLASS_HIST_FI_DAT');

            for ($dateCnt = 2; $dateCnt <= 7; $dateCnt++) {
                $index = 0;
                $index1 = 0;
                $index2 = 0;
                foreach ($primaryKey as $key => $val) {
                    $index = $dateCnt - 1;
                    if ($val == "FROM_DATE") {
                        if ($dateCnt < 5) {
                            $index1 = $dateCnt - 1;
                            ${"primaryKey$dateCnt"}[$key] = $val. "_T".$index1;
                        } else {
                            $index2 = $dateCnt - 4;
                            ${"primaryKey$dateCnt"}[$key] = $val. "_S".$index2;
                        }
                    } elseif ($val == "TR_DIV") {
                        ${"primaryKey$dateCnt"}[$key] = $val.$index;
                    } else {
                        ${"primaryKey$dateCnt"}[$key] = $val;
                    }
                }
            }
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->fields["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $tmpCnt = 0;
                    $data = array("YEAR"           => $tmp[$tmpCnt],
                                  "SEMESTER"       => (strlen(trim($tmp[++$tmpCnt]))) ? sprintf("%01d", $tmp[$tmpCnt]) : "",
                                  "GRADE"          => (strlen(trim($tmp[++$tmpCnt]))) ? sprintf("%02d", $tmp[$tmpCnt]) : "",
                                  "HR_CLASS"       => (strlen(trim($tmp[++$tmpCnt]))) ? sprintf("%03s", $tmp[$tmpCnt]) : "",
                                  "RECORD_DIV"     => (strlen(trim($tmp[++$tmpCnt]))) ? sprintf("%01s", $tmp[$tmpCnt]) : "",
                                  "HR_NAME"        => $tmp[++$tmpCnt],
                                  "HR_NAMEABBV"    => $tmp[++$tmpCnt],
                                  "HR_CLASS_NAME1" => $tmp[++$tmpCnt],
                                  "HR_CLASS_NAME2" => $tmp[++$tmpCnt],
                                  "TR_DIV1"        => '1',
                                  "TR_CD1"         => (strlen(trim($tmp[++$tmpCnt]))) ? sprintf($zero_ume, $tmp[$tmpCnt]) : "",
                                  "FROM_DATE_T1"   => (strlen(trim($tmp[++$tmpCnt]))) ? str_replace("/", "-", $tmp[$tmpCnt]) : "",
                                  "TO_DATE_T1"     => (strlen(trim($tmp[++$tmpCnt]))) ? str_replace("/", "-", $tmp[$tmpCnt]) : "",
                                  "TR_DIV2"        => '2',
                                  "TR_CD2"         => (strlen(trim($tmp[++$tmpCnt]))) ? sprintf($zero_ume, $tmp[$tmpCnt]) : "",
                                  "FROM_DATE_T2"   => (strlen(trim($tmp[++$tmpCnt]))) ? str_replace("/", "-", $tmp[$tmpCnt]) : "",
                                  "TO_DATE_T2"     => (strlen(trim($tmp[++$tmpCnt]))) ? str_replace("/", "-", $tmp[$tmpCnt]) : "",
                                  "TR_DIV3"        => '3',
                                  "TR_CD3"         => (strlen(trim($tmp[++$tmpCnt]))) ? sprintf($zero_ume, $tmp[$tmpCnt]) : "",
                                  "FROM_DATE_T3"   => (strlen(trim($tmp[++$tmpCnt]))) ? str_replace("/", "-", $tmp[$tmpCnt]) : "",
                                  "TO_DATE_T3"     => (strlen(trim($tmp[++$tmpCnt]))) ? str_replace("/", "-", $tmp[$tmpCnt]) : "",
                                  "TR_DIV4"        => '4',
                                  "SUBTR_CD1"      => (strlen(trim($tmp[++$tmpCnt]))) ? sprintf($zero_ume, $tmp[$tmpCnt]) : "",
                                  "FROM_DATE_S1"   => (strlen(trim($tmp[++$tmpCnt]))) ? str_replace("/", "-", $tmp[$tmpCnt]) : "",
                                  "TO_DATE_S1"     => (strlen(trim($tmp[++$tmpCnt]))) ? str_replace("/", "-", $tmp[$tmpCnt]) : "",
                                  "TR_DIV5"        => '5',
                                  "SUBTR_CD2"      => (strlen(trim($tmp[++$tmpCnt]))) ? sprintf($zero_ume, $tmp[$tmpCnt]) : "",
                                  "FROM_DATE_S2"   => (strlen(trim($tmp[++$tmpCnt]))) ? str_replace("/", "-", $tmp[$tmpCnt]) : "",
                                  "TO_DATE_S2"     => (strlen(trim($tmp[++$tmpCnt]))) ? str_replace("/", "-", $tmp[$tmpCnt]) : "",
                                  "TR_DIV6"        => '6',
                                  "SUBTR_CD3"      => (strlen(trim($tmp[++$tmpCnt]))) ? sprintf($zero_ume, $tmp[$tmpCnt]) : "",
                                  "FROM_DATE_S3"   => (strlen(trim($tmp[++$tmpCnt]))) ? str_replace("/", "-", $tmp[$tmpCnt]) : "",
                                  "TO_DATE_S3"     => (strlen(trim($tmp[++$tmpCnt]))) ? str_replace("/", "-", $tmp[$tmpCnt]) : "",
                                  "HR_FACCD"       => $tmp[++$tmpCnt],
                                  "CLASSWEEKS"     => $tmp[++$tmpCnt],
                                  "CLASSDAYS"      => $tmp[++$tmpCnt]
                                );

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $duplicateCsv4, $duplicateCsv5, $duplicateCsv6, $duplicateCsv7, $primaryKey1, $primaryKey2, $primaryKey3, $primaryKey4, $primaryKey5, $primaryKey6, $primaryKey7)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }

                    for ($dateCnt = 2; $dateCnt <= 7; $dateCnt++) {
                        foreach (${"primaryKey$dateCnt"} as $key => $val) {
                            ${"duplicateCsv$dateCnt"}[$line][$val] = $data[$val];
                        }
                    }
                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $duplicateCsv4, $duplicateCsv5, $duplicateCsv6, $duplicateCsv7, $primaryKey1, $primaryKey2, $primaryKey3, $primaryKey4, $primaryKey5, $primaryKey6, $primaryKey7)
    {
        $returnFlg       = true;
        $hissuErrFlg     = false;  //必須エラーフラグ
        $lengthErrFlg    = false;  //レングスエラーフラグ
        $yearErrFlg      = false;  //年度エラーフラグ
        $dayErrFlg       = false;  //日付エラーフラグ
        $masterErrFlg    = false;  //職員マスタにないエラーフラグ
        $duplicateCsvErrFlg = false;//重複(CSV)エラーフラグ

        $hissuErrMsg     = "";     //必須エラーメッセージ
        $lengthErrMsg    = "";     //レングスエラーメッセージ
        $yearErrMsg      = "";     //年度のエラーメッセージ
        $dayErrMsg       = "";     //日付のエラーメッセージ
        $masterErrMsg    = "";     //マスタにないエラーメッセージ
        $duplicateCsvErrMsg = "";  //重複(CSV)メッセージ

        $hissuSep        = "";
        $lengthSep       = "";
        $yearSep         = "";
        $daySep          = "";
        $masterSep       = "";
        $duplicateCsvSep = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        for ($dateCnt = 2; $dateCnt <= 7; $dateCnt++) {
            $dupFlg2 = $this->duplicateCsvCheck(${"duplicateCsv$dateCnt"}, ${"primaryKey$dateCnt"}, $data);
            if ($dupFlg2[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
                break;
            }
        }

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            if (in_array($key, array("SEMESTER"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            if (in_array($key, array("GRADE"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            if (in_array($key, array("HR_CLASS"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //講座施設に存在するかチェック
            if (in_array($key, array("HR_FACCD"))) {
                if ($data[$key] != '') {
                    if ($key == "HR_FACCD") {
                        $query = knja062aQuery::checkFaccd($data[$key]);
                    }
                    $rowCount = $db->getOne($query);
                    if ($rowCount == '0') {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                    }
                }
            }

            //職員マスタに存在するかチェック
            if (in_array($key, array("TR_CD1","TR_CD2","TR_CD3","SUBTR_CD1","SUBTR_CD2","SUBTR_CD3"))) {
                if ($data[$key] != '') {
                    if ($key == "TR_CD1" || $key == "TR_CD2" || $key == "TR_CD3" || $key == "SUBTR_CD1" || $key == "SUBTR_CD2" || $key == "SUBTR_CD3") {
                        $query = knja062aQuery::checkStaffMst($data[$key]);
                    }
                    $rowCount = $db->getOne($query);
                    if ($rowCount == '0') {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                    }
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }

        //STAFF_CLASS_HIST_FI_DATテーブルのチェック処理
        for ($staffcnt = 1; $staffcnt <= 6; $staffcnt++) {
            $soeji     = $staffcnt < 4 ? $staffcnt : $staffcnt - 3;
            $dateSoeji = $staffcnt < 4 ? "_T" : "_S";
            $trSoeji   = $staffcnt < 4 ? "" : "SUB";
            $trError   = $staffcnt < 4 ? "( 担任 )" : "( 副担任 )";
            //空文字チェック
            $strnotchk = array("FROM_DATE".$dateSoeji.$soeji     => "( 開始日付 )",
                               "TO_DATE".$dateSoeji.$soeji       => "( 終了日付 )",
                               $trSoeji."TR_CD".$soeji           => $trError
                        );
            $allNullFlg = true;
            foreach ($strnotchk as $key => $val) {
                if (strlen($data[$key]) > 0) {
                    $allNullFlg = false;
                }
            }

            //上の4つの項目のうち、いずれかが入っていたらチェックする
            if (!$allNullFlg) {
                foreach ($strnotchk as $key => $val) {
                    if (strlen($data[$key]) <= 0) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val.$staffcnt);
                    }
                }
                //キーの更新チェック
                $checkDate = preg_split("{/}", str_replace("-", "/", $data["TO_DATE".$dateSoeji.$soeji]));
                $checkYear = $checkDate[1] < 4 ? $checkDate[0] - 1 : $checkDate[0];

                if ($checkYear != $data["YEAR"]) {
                    $returnFlg = $this->errSet($yearErrFlg, $yearErrMsg, "※日付(範囲):", $yearSep, "担当".$staffcnt."終了日");
                }
                $checkDate = preg_split("{/}", str_replace("-", "/", $data["FROM_DATE".$dateSoeji.$soeji]));
                $checkYear = $checkDate[1] < 4 ? $checkDate[0] - 1 : $checkDate[0];

                if ($checkYear != $data["YEAR"]) {
                    $returnFlg = $this->errSet($yearErrFlg, $yearErrMsg, "※日付(範囲):", $yearSep, "担当".$staffcnt."開始日");
                }

                if (str_replace("/", "-", $data["FROM_DATE".$dateSoeji.$soeji]) > str_replace("/", "-", $data["TO_DATE".$dateSoeji.$soeji])) {
                    $returnFlg = $this->errSet($dayErrFlg, $dayErrMsg, "※日付:", $daySep, "担当".$staffcnt." 開始日より終了日が大きい");
                }

                $query = knja062aQuery::getSemesterMst($data["YEAR"], $data["SEMESTER"]);
                $semeDate = $db->getRow($query, DB_FETCHMODE_ASSOC);

                //学期マスタチェック
                if ($semeDate["EDATE"] < str_replace("/", "-", $data["FROM_DATE".$dateSoeji.$soeji])) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "担当".$staffcnt." 開始日付学期範囲外");
                }
                if (str_replace("/", "-", $data["TO_DATE".$dateSoeji.$soeji]) < $semeDate["SDATE"]) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "担当".$staffcnt." 終了日付学期範囲外");
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$lengthErrMsg.$masterErrMsg.$yearErrMsg.$dayErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knja062aQuery::insertQueryErr($db, $line, $checkError);
        }

        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }

        return true;
    }
//ここまでCSV

    //入力項目チェック
    public function validateInput()
    {
        if ($this->cmd == "add" && (trim($this->fields["GRADE"]) == "" || trim($this->fields["HR_CLASS"]) == "")) {
            $this->setWarning("MSG301", "(学年および組)");
            return false;
        }
        if ($this->cmd == "add" && (trim($this->fields["HR_NAME"]) == "" || trim($this->fields["HR_NAMEABBV"]) == "")) {
            $this->setWarning("MSG301", "(組名称および組略称)");
            return false;
        }
        //キーの更新チェック
        if ($this->cmd == "update" && ($this->grade != $this->fields["GRADE"] || $this->hr_class != $this->fields["HR_CLASS"])) {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->cmd == "update" && (trim($this->fields["HR_NAME"]) == "" || trim($this->fields["HR_NAMEABBV"]) == "")) {
            $this->setWarning("MSG301", "(組名称および組略称)");
            return false;
        }
        if ((!strlen($this->grade) || !strlen($this->hr_class)) && $this->cmd == "update") {
            $this->setWarning("MSG308");
            return false;
        }
        //学年ゼロ埋め
        if (strlen($this->fields["GRADE"]) == 1) {
            $this->fields["GRADE"] = sprintf("%02d", $this->fields["GRADE"]);
        }
        //組ゼロ埋め
        if (strlen($this->fields["HR_CLASS"]) == 1) {
            //2004/08/19 arakaki $this->fields["HR_CLASS"] = sprintf("%02d", $this->fields["HR_CLASS"]);
            $this->fields["HR_CLASS"] = sprintf("%03s", $this->fields["HR_CLASS"]);
        }
        //１レコード取得
        $row = knja062aQuery::getRow($this, $this->term, $this->fields["GRADE"], $this->fields["HR_CLASS"]);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(学年および組)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->fields["UPDATED"]) {
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->fields["GRADE"]) > 2) {
            $this->setWarning("MSG915", "(学年) 2バイトまでです。");
            return false;
        } elseif (strlen($this->fields["HR_CLASS"]) > 3) {
            $this->setWarning("MSG915", "(組) 3バイトまでです。");
            return false;
        } elseif (strlen($this->fields["HR_NAME"]) > 15) {
            $this->setWarning("MSG915", "(組名称) 15バイトまでです。");
            return false;
        } elseif (strlen($this->fields["HR_NAMEABBV"]) > 5) {
            $this->setWarning("MSG915", "(組略称) 5バイトまでです。");
            return false;
        } elseif (strlen($this->fields["CLASSWEEKS"]) > 2) {
            $this->setWarning("MSG915", "(授業週数) 2桁までです。");
            return false;
        } elseif (strlen($this->fields["CLASSDAYS"]) > 3) {
            $this->setWarning("MSG915", "(授業日数) 3桁までです。");
            return false;
        }
        //「コピーボタン」押し時のチェック
        if ($this->cmd =="copy") {
            if ($this->term == $this->term2) {
                $this->setWarning("MSG300", "同年度、同学期のコピーは出来ません。");
                return false;
            }
            $count = knja062aQuery::count($this, $this->term);
            if ($count > "0") {
                $this->warning="クラスが存在するのでコピーは出来ません。";
                return false;
            }
            $count2 = knja062aQuery::count2($this->term, $this);
            if ($this->check && $count2 > "0") {
                $this->warning = "{$this->sch_label}が既に割り振られています。{$this->sch_label}HR割振り処理で削除してから再度実行してください。";
                return false;
            }
        }
        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->grade = $this->fields["GRADE"];
            $this->hr_class = $this->fields["HR_CLASS"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    public function &getDeleteModel()
    {
        //キーの更新チェック
        if (!strlen($this->grade) || !strlen($this->hr_class)) {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->hr_class != $this->fields["HR_CLASS"] || $this->grade != $this->fields["GRADE"]) {
            $this->setWarning("MSG308");
            return false;
        }
        //１レコード取得
        $row = knja062aQuery::getRow($this, $this->term, $this->fields["GRADE"], $this->fields["HR_CLASS"]);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        $ret_val = knja062aQuery::getDeleteQuery($this);
        if ($ret_val == "schreg_regd_fi") {
            $this->warning = "{$this->sch_label}が既に割り振られています。{$this->sch_label}HR割振り処理で削除してから再度実行してください。";
            return false;
        }
        unset($this->grade);
        unset($this->hr_class);
        $this->setMessage("MSG201");
    }

    //INSERT
    public function &insert()
    {
        $result = knja062aQuery::getInsertQuery($this);
        return $result;
    }

    //UPDATE
    public function &update()
    {
        $result = knja062aQuery::getUpdateQuery($this);
        return $result;
    }

    //コピーボタン押し時の処理
    public function &getInsertCopyModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            if ($this->fields['CLASS_RADIO'] == 2) {
                knja062aQuery::getInsertCopyQuery2($this, $this->term2, $this->term, $this->check, $this->grd_div);
            } else {
                knja062aQuery::getInsertCopyQuery($this, $this->term2, $this->term, $this->check, $this->grd_div);
            }
            //unset($this->check);
            //unset($this->grd_div);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
