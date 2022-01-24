<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx_e061attendrecModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx_e061attendrecModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    public function init()
    {
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "GRADE"            =>  VARS::post("GRADE"),            //学年一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組一覧
                             "STUDENT"          =>  VARS::post("STUDENT"),          //生徒一覧
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd          = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }
    public function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery('KNJX_E061M');
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx_e061attendrecQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_e061attendrecQuery::deleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("SCHOOLCD"          => array(1,"※本校区分"),
                               "YEAR"              => array(4,"※年度"),
                               "SCHREGNO"          => array(8,"※学籍番号"),
                               "ANNUAL"            => array(2,"※履修学年"),
                               "SUMDATE"           => array(10,"集計日付"),
                               "CLASSDAYS"         => array(3,"授業日数"),
                               "OFFDAYS"           => array(3,"休学日数"),
                               "ABSENT"            => array(3,"公欠日数"),
                               "SUSPEND"           => array(3,"出停日数"),
                               "MOURNING"          => array(3,"忌引日数"),
                               "ABROAD"            => array(3,"留学日数"),
                               "REQUIREPRESENT"    => array(3,"要出席日数"),
                               "SICK"              => array(3,"病欠日数"),
                               "ACCIDENTNOTICE"    => array(3,"事故欠届日数"),
                               "NOACCIDENTNOTICE"  => array(3,"事故欠無日数"),
                               "PRESENT"           => array(3,"出席日数")
                               );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr('KNJX_E061M');
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey('SCHREG_ATTENDREC_DAT');
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    $data = array("SCHOOLCD"         => $tmp[0],
                                  "YEAR"             => $tmp[6],
                                  "SCHREGNO"         => strlen(trim($tmp[4]))  ? sprintf("%08d", $tmp[4])  : "",
                                  "ANNUAL"           => strlen(trim($tmp[7]))  ? sprintf("%02d", $tmp[7])  : "",
                                  "SUMDATE"          => $tmp[8],
                                  "CLASSDAYS"        => $tmp[9],
                                  "OFFDAYS"          => $tmp[10],
                                  "ABSENT"           => $tmp[11],
                                  "SUSPEND"          => $tmp[12],
                                  "MOURNING"         => $tmp[13],
                                  "ABROAD"           => $tmp[14],
                                  "REQUIREPRESENT"   => $tmp[15],
                                  "SICK"             => $tmp[16],
                                  "ACCIDENTNOTICE"   => $tmp[17],
                                  "NOACCIDENTNOTICE" => $tmp[18],
                                  "PRESENT"          => $tmp[19]
                                  );

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
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $dateErrFlg      = false;
        $duplicateCsvErrFlg = false;

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $dateErrMsg      = "";
        $duplicateCsvErrMsg = "";

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $dateSep      = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("SCHOOLCD","YEAR","SCHREGNO","ANNUAL"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("CLASSDAYS","OFFDAYS","ABSENT","SUSPEND","MOURNING","ABROAD","REQUIREPRESENT","SICK","ACCIDENTNOTICE","NOACCIDENTNOTICE","PRESENT"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //文字数チェック
            if (in_array($key, array("SCHOOLCD","YEAR","SCHREGNO","ANNUAL"))) {
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            } elseif ($this->field["SHORI_MEI"] == "1") {
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }

            //日付チェック
            if (in_array($key, array("SUMDATE"))) {
                if (strlen($data[$key])) {
                    $date_check_array = explode('/', str_replace('-', '/', $data[$key]));
                    if (!checkdate($date_check_array[1], $date_check_array[2], $date_check_array[0])) {
                        $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, $val[1]);
                    }
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$dateErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_e061attendrecQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning(MSG915, "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");
        //ヘッダデータ
        $header = array("SCHOOLCD"          => "※本校区分",
                        "GRADE"             => "学年",
                        "HR_CLASS"          => "組",
                        "ATTENDNO"          => "出席番号",
                        "SCHREGNO"          => "※学籍番号",
                        "NAME"              => "生徒氏名",
                        "YEAR"              => "※年度",
                        "ANNUAL"            => "※履修学年",
                        "SUMDATE"           => "集計日付",
                        "CLASSDAYS"         => "授業日数",
                        "OFFDAYS"           => "休学日数",
                        "ABSENT"            => "公欠日数",
                        "SUSPEND"           => "出停日数",
                        "MOURNING"          => "忌引日数",
                        "ABROAD"            => "留学日数",
                        "REQUIREPRESENT"    => "要出席日数",
                        "SICK"              => "病欠日数",
                        "ACCIDENTNOTICE"    => "事故欠届日数",
                        "NOACCIDENTNOTICE"  => "事故欠無日数",
                        "PRESENT"           => "出席日数",
                        $this->lastColumn   => $this->lastColumn);
        //ヘッダ例
        $header_rei = array("SCHOOLCD"          => "0",
                        "GRADE"             => "01",
                        "HR_CLASS"          => "001",
                        "ATTENDNO"          => "001",
                        "SCHREGNO"          => "20051003",
                        "NAME"              => "今井花子",
                        "YEAR"              => "2005",
                        "ANNUAL"            => "01",
                        "SUMDATE"           => "2009/07/10",
                        "CLASSDAYS"         => "191",
                        "OFFDAYS"           => "0",
                        "ABSENT"            => "0",
                        "SUSPEND"           => "1",
                        "MOURNING"          => "1",
                        "ABROAD"            => "0",
                        "REQUIREPRESENT"    => "189",
                        "SICK"              => "1",
                        "ACCIDENTNOTICE"    => "1",
                        "NOACCIDENTNOTICE"  => "1",
                        "PRESENT"           => "186",
                        $this->lastColumn   => $this->lastColumn);

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {
            //エラー
            $tables = array();
            $tables = array("1" => "SCHREG_ATTENDREC_DAT");
            $dupTable = $this->getDuplicateErrorHeader('KNJX_E061M', $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4") {
            //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery('KNJX_E061M');
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx_e061attendrecQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "学籍出欠記録データ";
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
