<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx190oModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $schoolName;
    public $schoolCode;

    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx190oModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
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
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd          = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        //最初にこのプログラムが呼ばれたとき
        if ($this->cmd == "") {
            $this->program_id = VARS::request("program_id") ? VARS::request("program_id") : PROGRAMID;
            $this->mode       = VARS::request("mode")       ? VARS::request("mode")       : 'ungrd';
        }

        //学校名取得
        $query = knjx190oquery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);

        //プロパティファイル
        $this->getProperties();

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function getProperties()
    {
        $useSyojikou3Flg = false;
        $tyousasyoFlg = false;
        $retVal = "";

        /*
         * configディレクトリ確認
         */
        if (file_exists(CONFDIR ."/prgInfo.properties")) {
            $filename = CONFDIR ."/prgInfo.properties";
        } else {
            $filename = DOCUMENTROOT ."/prgInfo.properties";
        }

        $fp = @fopen($filename, 'r');
        if ($fp) {
            while ($line = fgets($fp, 1024)) {
                $pos = strpos($line, "useSyojikou3");
                // === を使用していることに注目しましょう。単純に == を使ったのでは
                // 期待通りに動作しません。なぜなら 'doRun' が 0 番目 (最初) の文字だからです。
                if ($pos === false) {
                    if (!$useSyojikou3Flg) {
                        $this->useSyojikou3 = 0;
                    } //デフォルト値
                } else {
                    $retVal = str_replace("useSyojikou3 = ", "", $line);
                    $retVal = str_replace("\r\n", "", $retVal);
                    $retVal = str_replace("\r", "", $retVal);
                    $retVal = str_replace("\n", "", $retVal);
                    $this->useSyojikou3 = $retVal;
                    $useSyojikou3Flg = true;
                }

                $pos = strpos($line, "tyousasyoTokuBetuFieldSize");
                // === を使用していることに注目しましょう。単純に == を使ったのでは
                // 期待通りに動作しません。なぜなら 'doRun' が 0 番目 (最初) の文字だからです。
                if ($pos === false) {
                    if (!$tyousasyoFlg) {
                        $this->tyousasyoTokuBetuFieldSize = 0;
                    } //デフォルト値
                } else {
                    $retVal = str_replace("tyousasyoTokuBetuFieldSize = ", "", $line);
                    $retVal = str_replace("\r\n", "", $retVal);
                    $retVal = str_replace("\r", "", $retVal);
                    $retVal = str_replace("\n", "", $retVal);
                    $this->tyousasyoTokuBetuFieldSize = $retVal;
                    $tyousasyoFlg = true;
                }
            }
            fclose($fp);
        } else {
            $this->useSyojikou3 = "0";
            $this->tyousasyoTokuBetuFieldSize = "0";
        }

        return $retVal;
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
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx190oQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx190oQuery::deleteQueryCsv($this, $this->data_arr);
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
            if ($this->useSyojikou3 == "1") {
                if ($this->tyousasyoTokuBetuFieldSize == 1) {
                    $checkdata = array("YEAR"                     => array(4,"年度"),
                                       "SCHREGNO"                 => array(8,"学籍番号"),
                                       "ANNUAL"                   => array(2,"年次"),
                                       "ATTENDREC_REMARK"         => array(90,"出欠記録備考"),
                                       "SPECIALACTREC"            => array(450,"特別活動記録"),
                                       "TRAIN_REF1"               => array(210,"学習／行動の特技・特徴等"),
                                       "TRAIN_REF2"               => array(210,"部活動・資格取得等"),
                                       "TRAIN_REF3"               => array(210,"その他"));
                } else {
                    $checkdata = array("YEAR"                     => array(4,"年度"),
                                       "SCHREGNO"                 => array(8,"学籍番号"),
                                       "ANNUAL"                   => array(2,"年次"),
                                       "ATTENDREC_REMARK"         => array(90,"出欠記録備考"),
                                       "SPECIALACTREC"            => array(264,"特別活動記録"),
                                       "TRAIN_REF1"               => array(210,"学習／行動の特技・特徴等"),
                                       "TRAIN_REF2"               => array(210,"部活動・資格取得等"),
                                       "TRAIN_REF3"               => array(210,"その他"));
                }
            } else {
                if ($this->tyousasyoTokuBetuFieldSize == 1) {
                    $checkdata = array("YEAR"                     => array(4,"年度"),
                                       "SCHREGNO"                 => array(8,"学籍番号"),
                                       "ANNUAL"                   => array(2,"年次"),
                                       "ATTENDREC_REMARK"         => array(90,"出欠記録備考"),
                                       "SPECIALACTREC"            => array(450,"特別活動記録"),
                                       "TRAIN_REF"                => array(1218,"指導上参考"));
                } else {
                    $checkdata = array("YEAR"                     => array(4,"年度"),
                                       "SCHREGNO"                 => array(8,"学籍番号"),
                                       "ANNUAL"                   => array(2,"年次"),
                                       "ATTENDREC_REMARK"         => array(90,"出欠記録備考"),
                                       "SPECIALACTREC"            => array(264,"特別活動記録"),
                                       "TRAIN_REF"                => array(615,"指導上参考"));
                }
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('HEXAM_ENTREMARK_DAT');
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer      = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp         = common::csv2array($buffer);
                    $year        = (strlen(trim($tmp[4]))) ? sprintf("%04d", $tmp[4]) : "";
                    $schregno    = (strlen(trim($tmp[5]))) ? sprintf("%08d", $tmp[5]) : "";
                    $annualCheck = $db->getOne(knjx190oQuery::getAnnual($schregno, $year));
                    $annual      = ($annualCheck) ? $annualCheck : "00";

                    if ($this->useSyojikou3 == "1") {
                        $data = array("YEAR"             => $year,
                                      "SCHREGNO"         => $schregno,
                                      "ANNUAL"           => $annual,
                                      "ATTENDREC_REMARK" => $tmp[7],
                                      "SPECIALACTREC"    => $tmp[8],
                                      "TRAIN_REF1"       => $tmp[9],
                                      "TRAIN_REF2"       => $tmp[10],
                                      "TRAIN_REF3"       => $tmp[11]);
                    } else {
                        $data = array("YEAR"             => $year,
                                      "SCHREGNO"         => $schregno,
                                      "ANNUAL"           => $annual,
                                      "ATTENDREC_REMARK" => $tmp[7],
                                      "SPECIALACTREC"    => $tmp[8],
                                      "TRAIN_REF"        => $tmp[9]);
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
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }
        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $masterSep       = "";
        $numericSep      = "";
        $lengthSep       = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR","SCHREGNO","ANNUAL"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR","SCHREGNO","ANNUAL"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }

            //存在チェック
            if (in_array($key, array("SCHREGNO"))) {
                $query = knjx190oQuery::countSchregNo($data[$key]);
                $cnt = $db->getOne($query);
                if ($cnt < 1) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された学籍番号がありません。");
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$masterErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx190oQuery::insertQueryErr($db, $line, $checkError);
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


    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        if ($this->useSyojikou3 == "1") {
            $header = array("GRADE"                     => "学年",
                            "HR_CLASS"                  => "クラス",
                            "ATTENDNO"                  => "出席番号",
                            "NAME"                      => "氏名",
                            "YEAR"                      => "※年度",
                            "SCHREGNO"                  => "※学籍番号",
                            "ANNUAL"                    => "※年次",
                            "ATTENDREC_REMARK"          => "出欠記録備考",
                            "SPECIALACTREC"             => "特別活動記録",
                            "TRAIN_REF1"                => "学習／行動の特技・特徴等",
                            "TRAIN_REF2"                => "部活動・資格取得等",
                            "TRAIN_REF3"                => "その他",
                            $this->lastColumn           => $this->lastColumn);
        } else {
            $header = array("GRADE"                     => "学年",
                            "HR_CLASS"                  => "クラス",
                            "ATTENDNO"                  => "出席番号",
                            "NAME"                      => "氏名",
                            "YEAR"                      => "※年度",
                            "SCHREGNO"                  => "※学籍番号",
                            "ANNUAL"                    => "※年次",
                            "ATTENDREC_REMARK"          => "出欠記録備考",
                            "SPECIALACTREC"             => "特別活動記録",
                            "TRAIN_REF"                 => "指導上参考",
                            $this->lastColumn           => $this->lastColumn);
        }

        if ($this->useSyojikou3 == "1") {
            if ($this->tyousasyoTokuBetuFieldSize == 1) {
                $header_rei = array("GRADE"                     => "99",
                                    "HR_CLASS"                  => "999",
                                    "ATTENDNO"                  => "999",
                                    "NAME"                      => "アルプ太郎",
                                    "YEAR"                      => "9999",
                                    "SCHREGNO"                  => "12345678",
                                    "ANNUAL"                    => "99",
                                    "ATTENDREC_REMARK"          => "全角30文字",
                                    "SPECIALACTREC"             => "全角150文字",
                                    "TRAIN_REF1"                => "全角70文字",
                                    "TRAIN_REF2"                => "全角70文字",
                                    "TRAIN_REF3"                => "全角70文字",
                                    $this->lastColumn           => $this->lastColumn);
            } else {
                $header_rei = array("GRADE"                     => "99",
                                    "HR_CLASS"                  => "999",
                                    "ATTENDNO"                  => "999",
                                    "NAME"                      => "アルプ太郎",
                                    "YEAR"                      => "9999",
                                    "SCHREGNO"                  => "12345678",
                                    "ANNUAL"                    => "99",
                                    "ATTENDREC_REMARK"          => "全角30文字",
                                    "SPECIALACTREC"             => "全角88文字",
                                    "TRAIN_REF1"                => "全角70文字",
                                    "TRAIN_REF2"                => "全角70文字",
                                    "TRAIN_REF3"                => "全角70文字",
                                    $this->lastColumn           => $this->lastColumn);
            }
        } else {
            if ($this->tyousasyoTokuBetuFieldSize == 1) {
                $header_rei = array("GRADE"                     => "99",
                                    "HR_CLASS"                  => "999",
                                    "ATTENDNO"                  => "999",
                                    "NAME"                      => "アルプ太郎",
                                    "YEAR"                      => "9999",
                                    "SCHREGNO"                  => "12345678",
                                    "ANNUAL"                    => "99",
                                    "ATTENDREC_REMARK"          => "全角30文字",
                                    "SPECIALACTREC"             => "全角150文字",
                                    "TRAIN_REF"                 => "全角406文字",
                                    $this->lastColumn           => $this->lastColumn);
            } else {
                $header_rei = array("GRADE"                     => "99",
                                    "HR_CLASS"                  => "999",
                                    "ATTENDNO"                  => "999",
                                    "NAME"                      => "アルプ太郎",
                                    "YEAR"                      => "9999",
                                    "SCHREGNO"                  => "12345678",
                                    "ANNUAL"                    => "99",
                                    "ATTENDREC_REMARK"          => "全角30文字",
                                    "SPECIALACTREC"             => "全角88文字",
                                    "TRAIN_REF"                 => "全角205文字",
                                    $this->lastColumn           => $this->lastColumn);
            }
        }

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
            $tables = array("1" => "HEXAM_ENTREMARK_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            $header = array();
            $header["YEAR"]      = "※年度";
            $header["SCHREGNO"]  = "※学籍番号";
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
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx190oQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
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
                $couse = "調査書進学用所見データ";
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
//var_dump($_REQUEST);
