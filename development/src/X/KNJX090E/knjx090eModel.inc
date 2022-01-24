<?php

require_once('for_php7.php');
class knjx090eModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $file;
    public $lastColumn;//CSVの最後の列の値

    public function knjx090eModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            unset($this->field);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR_SEMESTER"    =>  VARS::post("YEAR_SEMESTER"),    //年度・学期
                             "CHAIRCD"          =>  VARS::post("CHAIRCD"),          //講座
                             "SCHREGNO"         =>  VARS::post("SCHREGNO"),         //学籍番号
                             "DATE"             =>  VARS::post("DATE"),             //名簿基準日付
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->file = VARS::file("FILE");

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    /*************/
    /*  CSV取込  */
    /*************/
    public function getExecModel()
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

                //処理分岐
                if ($this->field["SHORI_MEI"] == "1") {
                    //更新（処理件数取得）
                    knjx090eQuery::getUpdateQuery($db, $this->data_arr);
                    $db->commit();
                } else {
                    //削除（処理件数取得）
                    knjx090eQuery::getDeleteQuery($db, $this->data_arr);
                    $db->commit();
                }
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //送られたファイルの文字列を取得
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {   //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            //チェック対象項目（フィールド名 => サイズ, 項目名（メッセージ用））
            $checkdata = array("SCHREGNO"       => array(8,  "学籍番号"),
                               "APPDATE"        => array(10, "適用開始日付"),
                               "APPENDDATE"     => array(10, "適用終了日付"),
                               "CHAIRCD"        => array(7, "講座コード")
                               );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //APPDATE, APPENDDATE取得
            $date = $db->getRow(knjx090eQuery::getSemesterMst());
            $data_arr["APPDATE"] = $date['0'];
            $data_arr["APPENDDATE"] = $date['1'];

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            $record_no = 1;     //行番号(初期値)
            $cnt = 0;           //処理済件数(初期値)

            //取得主キー
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('CHAIR_STD_DAT');

            $fp = fopen($this->file["tmp_name"], "r");     //ファイルまたはURLをオープンする
            while (!feof($fp)) {                           //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                 //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"] == "on" && $record_no < 2) {
                        $record_no++;
                        continue;
                    }

                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    //データ項目・値セット
                    $data = array("YEAR"        => CTRL_YEAR,
                                  "SEMESTER"    => CTRL_SEMESTER,
                                  "SCHREGNO"    => (trim($tmp[0])) ? sprintf("%08d", trim($tmp[0])) : "",
                                  "APPDATE"     => ($data_arr["APPDATE"]) ? $data_arr["APPDATE"] : "",
                                  "APPENDDATE"  => ($data_arr["APPENDDATE"]) ? $data_arr["APPENDDATE"] : ""
                                  );

                    //件数チェック
                    $col_no = 0;
                    for ($j = 9; $j <= count($tmp); $j++) {
                        if ($tmp[$j] == $this->lastColumn) {
                            break;
                        }
                        $col_no++;
                        $data["CHAIRCD".$col_no]    = (strlen(trim($tmp[$j]))) ? sprintf("%07d", $tmp[$j]) : "";
                    }

                    //チェック対象項目（フィールド名 = サイズ, 項目名（メッセージ用））
                    for ($j = 1; $j <= $col_no; $j++) {
                        $checkdata["CHAIRCD".$j]    = array(7, "講座コード".$j);
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $col_no, $record_no, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$cnt] = $data;
                        $temp_arr[$cnt]["COL_NO"] = $col_no;
                        $cnt++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$record_no][$val] = $data[$val];
                    }
                    $record_no++;
                }
            }

            $db->commit();  // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }
        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $checkdata, $col_no, $record_no, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $dateErrFlg         = false;    //日付エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $dateErrMsg         = "";       //日付エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $dateSep         = "";
        $lengthSep       = "";
        $masterSep       = "";
        $duplicateCsvSep = "";

        $chaircd = $row = $column = array();
        for ($i = 1; $i <= $col_no; $i++) {
            $chaircd[]  = "CHAIRCD".$i;
        }

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        //講座一覧取得
        $chair_array = array();
        $chair_array = $db->getCol(knjx090eQuery::getChairDat($data["YEAR"], $data["SEMESTER"]));

        //存在チェック（年度・学期）
        $year_semes_cnt = $db->getOne(knjx090eQuery::getYearSemesterMst($data["YEAR"], $data["SEMESTER"]));
        if ($year_semes_cnt == 0) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "年度・学期");
        }

        //必須項目チェック
        if (!$col_no) {
            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, "講座コード");
        }

        //講座コード件数チェック
        if ($col_no > 30) {
            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※件数:", $hissuSep, "講座コード30件まで");
        }

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array_merge(array("YEAR","SEMESTER","SCHREGNO","APPDATE","APPENDDATE"), $chaircd))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array_merge(array("YEAR","SEMESTER","SCHREGNO"), $chaircd))) {
                if ($data[$key] != '' && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //日付チェック
            if (in_array($key, array("APPDATE","APPENDDATE"))) {
                if ($data[$key] != '') {
                    $date = date("Y-m-d", strtotime($data[$key]));
                    list($year, $month, $day) = preg_split('/-/', $data[$key]);
                    $ck_date = $db->getOne(knjx090eQuery::checkSemesterMst($data["YEAR"], $data["SEMESTER"], $date));

                    if (!checkdate($month, $day, $year) || $ck_date == 0) {
                        $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, $val[1]);
                    }
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }

            //存在チェック（在籍）
            if ($key == "SCHREGNO") {
                $ck_sch = $db->getOne(knjx090eQuery::checkSchregno($data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"]));
                if ($ck_sch == 0) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                }
            }
            //存在チェック（講座）
            if (in_array($key, $chaircd) && !in_array($data[$key], $chair_array)) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
            }
        }

        //日付の大小チェック
        $appdate = date("Y-m-d", strtotime($data["APPDATE"]));
        $appenddate= date("Y-m-d", strtotime($data["APPENDDATE"]));
        if ($appdate > $appenddate) {
            $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, "開始日付と終了日付の大小が不正");
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$dateErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx090eQuery::insertQueryErr($db, $record_no, $checkError);
        }

        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //１万件を超えたら制限エラー
        if (get_count($data_arr) > 10000) {
            $this->setWarning("MSG915", "処理件数を１万件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }

        return true;
    }

    /*************/
    /*  CSV出力  */
    /*************/
    public function getDownloadModel()
    {
        $header_data = false;
        $contents  = "";

        //DB接続
        $db = Query::dbCheckOut();

        //エラー出力
        if ($this->field["OUTPUT"] == "3") {
            //項目
            $header = array("MSGROW"     => "行番号",
                            "MSGREMARK"  => "メッセージ");
            $headerDuplicate = array("YEAR"     => "※年度",
                                     "SEMESTER" => "※学期",
                                     "SCHREGNO" => "※学籍番号",
                                     "APPDATE"  => "※適用開始日付",
                                     "CHAIRCD"  => "※講座コード");
            $tables = array();
            $tables = array("1" => "CHAIR_STD_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header, ",")."\n";

            //SQL文発行
            $query = $this->selectCsvErrQuery(PROGRAMID);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();

        //ヘッダ出力
        } elseif ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header = array("SCHREGNO"        => "※学籍番号",
                            "OLD_HR_CLASS"    => "旧組番",
                            "NAME_SEI"        => "姓",
                            "NAME"            => "名",
                            "SEX"             => "性別",
                            "HR_CLASS"        => "新組",
                            "NAME_KANA_SEI"   => "かな（みょうじ）",
                            "NAME_KANA"       => "かな（なまえ）",
                            "SEI_ENG"         => "姓アルファベット",
                            "CHAIRCD"         => "講座コード",
                            $this->lastColumn => $this->lastColumn);

            //学期開始日・終了日取得
            $seminfo = $db->getRow(knjx090eQuery::getSemesterMst(), DB_FETCHMODE_ASSOC);

            //ヘッダ例
            $header_rei = array("SCHREGNO"        => '12345678',
                                "OLD_HR_CLASS"    => "旧組番",
                                "NAME_SEI"        => "姓",
                                "NAME"            => "名",
                                "SEX"             => "性別",
                                "HR_CLASS"        => "新組",
                                "NAME_KANA_SEI"   => "かな（みょうじ）",
                                "NAME_KANA"       => "かな（なまえ）",
                                "SEI_ENG"         => "姓アルファベット",
                                "CHAIRCD"         => "講座コード",
                                $this->lastColumn => $this->lastColumn);

            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
            $contents .= implode($header_rei, ",")."\n";
            $header_data = true;

        //データ出力
        } elseif ($this->field["OUTPUT"] == "4") {
            $dataTemp = array();
            $tempCnt = array();
            $this->year_semester = $db->getOne(knjx090eQuery::getYearSemeListOld($this));
            $query = knjx090eQuery::getCsvQuery($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",", ".", $row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row["NAME_SEI"]      = str_replace(" ", "　", $row["NAME_SEI"]);      //半角スペースを全角に
                $row["NAME"]          = str_replace(" ", "　", $row["NAME"]);          //半角スペースを全角に
                $row["NAME_KANA_SEI"] = str_replace(" ", "　", $row["NAME_KANA_SEI"]); //半角スペースを全角に
                $row["NAME_KANA"]     = str_replace(" ", "　", $row["NAME_KANA"]);     //半角スペースを全角に

                $key = $row["SCHREGNO"].$row["APPDATE"];
                if ($dataTemp[$key] == "") {
                    $dataTemp[$key][] = $row["SCHREGNO"];
                    $dataTemp[$key][] = $row["OLD_HR_CLASS"];
                    $dataTemp[$key][] = (strstr($row["NAME_SEI"], '　', true) == "")? "" : strstr($row["NAME_SEI"], '　', true);
                    $dataTemp[$key][] = (preg_replace("/( |　)/", "", strstr($row["NAME"], '　')) == "")? $row["NAME"] : preg_replace("/( |　)/", "", strstr($row["NAME"], '　'));
                    $dataTemp[$key][] = $row["SEX"];
                    $dataTemp[$key][] = $row["HR_CLASS"];
                    $dataTemp[$key][] = (strstr($row["NAME_KANA_SEI"], '　', true) == "")? "" : strstr($row["NAME_KANA_SEI"], '　', true);
                    $dataTemp[$key][] = (preg_replace("/( |　)/", "", strstr($row["NAME_KANA"], '　')) == "")? $row["NAME_KANA"] : preg_replace("/( |　)/", "", strstr($row["NAME_KANA"], '　'));
                    $dataTemp[$key][] = $row["NAME_ENG"];
                    $dataTemp[$key][] = $row["COURSECODE"];
                    $dataTemp[$key][] = $row["COURSECODENAME"];
                    $dataTemp[$key][] = $row["CHAIRCD"];
                    $dataTemp[$key][] = $row["CHAIRNAME"];
                } else {
                    $dataTemp[$key][] = $row["CHAIRCD"];
                    $dataTemp[$key][] = $row["CHAIRNAME"];
                }

                $tempCnt[$key]++;
            }
            $result->free();

            //ヘッダ
            $header = array("SCHREGNO"       => "※学籍番号",
                            "OLD_HR_CLASS"   => "旧組番",
                            "NAME_SEI"       => "姓",
                            "NAME"           => "名",
                            "SEX"            => "性別",
                            "HR_CLASS"       => "新組",
                            "NAME_KANA_SEI"  => "かな（みょうじ）",
                            "NAME_KANA"      => "かな（なまえ）",
                            "NAME_ENG"       => "姓アルファベット",
                            "COURSECODE"     => "コースコード",
                            "COURSECODENAME" => "コース名称",
                            );

            //データが出力されるMAX列数を取得
            $headCnt = 0;
            foreach ($tempCnt as $key => $val) {
                $headCnt = ($headCnt < $val) ? $val : $headCnt;
            }
            //MAX列数分のヘッダを取得
            for ($i = 0; $i < $headCnt; $i++) {
                $header["CHAIRCD".$i]       = "※講座コード".($i+1);
                $header["CHAIRNAME".$i]     = "講座名称".($i+1);
            }
            $header[$this->lastColumn] = $this->lastColumn;

            //ヘッダ出力
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }

            //ＣＳＶデータをセット
            foreach ($dataTemp as $key => $val) {
                $contents .= implode($val, ",").",".$this->lastColumn."\n";
                $header_data = true;
            }
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {    //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "講座名簿データ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
