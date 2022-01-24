<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knje013bModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knje013bModel()
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

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "OUTPUTTYPE"       =>  VARS::post("OUTPUTTYPE"),        //出力タイプ
                             "YEAR"             =>  VARS::post("YEAR"),              //年度学期
                             "GRADE"            =>  VARS::post("GRADE"),             //学年
                             "HR_CLASS"         =>  VARS::post("HR_CLASS"),          //年組
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
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

    public function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //エラー項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = $header_rei = array();

        if ($this->field['OUTPUTTYPE'] == '1') {
            $header["YEAR"]                 = "※年度";
            $header["GRADE"]                = "※学年";
            $header["ANNUAL"]               = "※年次";
            $header["SCHREGNO"]             = "※学籍番号";
            $header["TOTALSTUDYACT"]        = "総合的な学習の時間の内容";
            $header["TOTALSTUDYVAL"]        = "総合的な学習の時間の評価";
            $header["SPECIALACTREMARK"]     = "特別活動の記録";
            $header["TRAIN_REF1"]           = "(1)学習における特徴等";
            $header["DUMMY1"]               = "(2)行動の特徴、特技等";
            $header["TRAIN_REF2"]           = "(3)部活動、ボランティア、留学海外経験等";
            $header["DUMMY2"]               = "(4)取得資格、検定等";
            $header["TRAIN_REF3"]           = "(5)表彰、顕彰との記録";
            $header["DUMMY3"]               = "(6)その他";
            $header["ATTENDREC_REMARK"]     = "出欠備考";
            $header[$this->lastColumn]      = $this->lastColumn;

            $header_rei = array();
            $header_rei["YEAR"]             = CTRL_YEAR;
            $header_rei["GRADE"]            = '01';
            $header_rei["ANNUAL"]           = "01";
            $header_rei["SCHREGNO"]         = "12345678";
            $header_rei["TOTALSTUDYACT"]    = "総合的な学習の時間の内容";
            $header_rei["TOTALSTUDYVAL"]    = "総合的な学習の時間の評価";
            $header_rei["SPECIALACTREMARK"] = "特別活動の記録";
            $header_rei["TRAIN_REF1"]       = "(1)学習における特徴等";
            $header_rei["DUMMY1"]           = "(2)行動の特徴、特技等";
            $header_rei["TRAIN_REF2"]       = "(3)部活動、ボランティア、留学海外経験等";
            $header_rei["DUMMY2"]           = "(4)取得資格、検定等";
            $header_rei["TRAIN_REF3"]       = "(5)表彰、顕彰との記録";
            $header_rei["DUMMY3"]           = "(6)その他";
            $header_rei["ATTENDREC_REMARK"] = "出欠備考";
            $header_rei[$this->lastColumn]  = $this->lastColumn;
        }
        if ($this->field['OUTPUTTYPE'] == '2') {
            $header["YEAR"]                 = "※年度";
            $header["GRADE"]                = "※学年";
            $header["ANNUAL"]               = "※年次";
            $header["SCHREGNO"]             = "※学籍番号";
            $header["TOTALSTUDYACT"]        = "総合的な学習の時間の内容";
            $header["TOTALSTUDYVAL"]        = "総合的な学習の時間の評価";
            $header["SPECIALACTREC"]        = "特別活動の記録";
            $header["REMARK101"]            = "(1)学習における特徴等";
            $header["REMARK102"]            = "(2)行動の特徴、特技等";
            $header["REMARK103"]            = "(3)部活動、ボランティア、留学海外経験等";
            $header["REMARK104"]            = "(4)取得資格、検定等";
            $header["REMARK105"]            = "(5)表彰、顕彰との記録";
            $header["REMARK106"]            = "(6)その他";
            $header["ATTENDREC_REMARK"]     = "出欠備考";
            $header[$this->lastColumn]      = $this->lastColumn;

            $header_rei = array();
            $header_rei["YEAR"]             = CTRL_YEAR;
            $header_rei["GRADE"]            = '03';
            $header_rei["ANNUAL"]           = "01";
            $header_rei["SCHREGNO"]         = "12345678";
            $header_rei["TOTALSTUDYACT"]    = "総合的な学習の時間の内容";
            $header_rei["TOTALSTUDYVAL"]    = "総合的な学習の時間の評価";
            $header_rei["SPECIALACTREC"]    = "特別活動の記録";
            $header_rei["REMARK101"]        = "(1)学習における特徴等";
            $header_rei["REMARK102"]        = "(2)行動の特徴、特技等";
            $header_rei["REMARK103"]        = "(3)部活動、ボランティア、留学海外経験等";
            $header_rei["REMARK104"]        = "(4)取得資格、検定等";
            $header_rei["REMARK105"]        = "(5)表彰、顕彰との記録";
            $header_rei["REMARK106"]        = "(6)その他";
            $header_rei["ATTENDREC_REMARK"] = "出欠備考";
            $header_rei[$this->lastColumn]  = $this->lastColumn;
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

            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = knje013bQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                if ($this->field['OUTPUTTYPE'] == '1') {
                    $query = knje013bQuery::selectMainQuery($this);
                }
                if ($this->field['OUTPUTTYPE'] == '2') {
                    $query = knje013bQuery::selectMainQuery2($this);
                }
            }

            //SQL文発行
            if ($query) {
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["OUTPUT"] == "4") {                //データ
                        $row = str_replace(array("\r\n", "\r", "\n"), "↓", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                        $array = $header;
                    } else {
                        $array = $header_err;
                    }

                    //ヘッダ順に値をセット
                    $comma = "";
                    foreach ($array as $key => $headerName) {
                        $contents .= $comma.$row[$key];
                        $comma = ",";
                    }
                    $contents .= "\n";

                    $header_data = true;
                }
                $result->free();
            }
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                if ($this->field['OUTPUTTYPE'] == '1') {
                    $couse = "指導要録データ";
                }
                if ($this->field['OUTPUTTYPE'] == '2') {
                    $couse = "調査書データ";
                }
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //ＣＳＶ取込
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            if ($this->field["SHORI_MEI"] == "1") {
                $cnt = knje013bQuery::insertQueryCsv($this, $this->data_arr);
            } else {
                $cnt = knje013bQuery::deleteQueryCsv($this, $this->data_arr);
            }
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");
            Query::dbCheckIn($db);

            $this->setMessage(MSG202, "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata["YEAR"]         = array(4,    "HISSU",  "INT",   "年度");
            $checkdata["GRADE"]        = array(2,    "HISSU",  "INT",   "学年");
            $checkdata["ANNUAL"]       = array(2,    "HISSU",  "INT",   "年次");
            $checkdata["SCHREGNO"]     = array(8,    "HISSU",  "INT",   "学籍番号");

            if ($this->field['OUTPUTTYPE'] == '1') {
                $checkdata["TOTALSTUDYACT"]        = array(980,    "",  "",   "総合的な学習の時間の内容");
                $checkdata["TOTALSTUDYVAL"]        = array(980,    "",  "",   "総合的な学習の時間の評価");
                $checkdata["SPECIALACTREC"]        = array(1000,   "",  "",   "特別活動の記録");
                $checkdata["REMARK101"]            = array(870,    "",  "",   "(1)学習における特徴等");
                $checkdata["REMARK102"]            = array(870,    "",  "",   "(2)行動の特徴、特技等");
                $checkdata["REMARK103"]            = array(226,    "",  "",   "(3)部活動、ボランティア、留学海外経験等");
                $checkdata["REMARK104"]            = array(226,    "",  "",   "(4)取得資格、検定等");
                $checkdata["REMARK105"]            = array(226,    "",  "",   "(5)表彰、顕彰との記録");
                $checkdata["REMARK106"]            = array(226,    "",  "",   "(6)その他");
                $checkdata["ATTENDREC_REMARK"]     = array(400,    "",  "",   "出欠備考");
            }
            if ($this->field['OUTPUTTYPE'] == '2') {
                $checkdata["TOTALSTUDYACT"]        = array(746,    "",  "",   "総合的な学習の時間の内容");
                $checkdata["TOTALSTUDYVAL"]        = array(845,    "",  "",   "総合的な学習の時間の評価");
                $checkdata["SPECIALACTREMARK"]     = array(700,    "",  "",   "特別活動の記録");
                $checkdata["TRAIN_REF1"]           = array(3110,   "",  "",   "(1)学習における特徴等");
                $checkdata["TRAIN_REF2"]           = array(3110,   "",  "",   "(3)部活動、ボランティア、留学海外経験等");
                $checkdata["TRAIN_REF3"]           = array(3110,   "",  "",   "(5)表彰、顕彰との記録");
                $checkdata["ATTENDREC_REMARK"]     = array(238,    "",  "",   "出欠備考");
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knje013bQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
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
                    $tmp    = str_replace("↓", "\n", $tmp);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    if ($this->field['OUTPUTTYPE'] == '1') {
                        $data["YEAR"]                 = $tmp[0];
                        $data["GRADE"]                = $tmp[1];
                        $data["ANNUAL"]               = $tmp[2];
                        $data["SCHREGNO"]             = $tmp[3];
                        $data["TOTALSTUDYACT"]        = $tmp[4];
                        $data["TOTALSTUDYVAL"]        = $tmp[5];
                        $data["SPECIALACTREC"]        = $tmp[6];
                        $data["REMARK101"]            = $tmp[7];
                        $data["REMARK102"]            = $tmp[8];
                        $data["REMARK103"]            = $tmp[9];
                        $data["REMARK104"]            = $tmp[10];
                        $data["REMARK105"]            = $tmp[11];
                        $data["REMARK106"]            = $tmp[12];
                        $data["ATTENDREC_REMARK"]     = $tmp[13];
                    }
                    if ($this->field['OUTPUTTYPE'] == '2') {
                        $data["YEAR"]                 = $tmp[0];
                        $data["GRADE"]                = $tmp[1];
                        $data["ANNUAL"]               = $tmp[2];
                        $data["SCHREGNO"]             = $tmp[3];
                        $data["TOTALSTUDYACT"]        = $tmp[4];
                        $data["TOTALSTUDYVAL"]        = $tmp[5];
                        $data["SPECIALACTREMARK"]     = $tmp[6];
                        $data["TRAIN_REF1"]           = $tmp[7];
                        $data["DUMMY1"]               = $tmp[8];
                        $data["TRAIN_REF2"]           = $tmp[9];
                        $data["DUMMY2"]               = $tmp[10];
                        $data["TRAIN_REF3"]           = $tmp[11];
                        $data["DUMMY3"]               = $tmp[12];
                        $data["ATTENDREC_REMARK"]     = $tmp[13];
                    }

                    $dataCnt = 15;

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        $temp_arr[$i] = $data;
                        $i++;
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
    public function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg       = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $otherErrFlg     = false;    //その他エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $otherErrMsg     = "";       //その他エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $otherSep     = "";

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }
            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }
            //チェック用にゼロ埋め
            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
            }
            /********************/
            /**  存在チェック  **/
            /********************/
            //SCHREG_BASE_MST にその生徒が登録されているのかチェックする
            if (($key == "SCHREGNO") && (0 == $db->getOne(knje013bQuery::schregNoCntSql($data[$key])))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            //SEMESTER_MST が作成されているか
            if (($key == "SEMESTER") && (0 == $db->getOne(knje013bQuery::semesterCntSql($data["YEAR"], $data[$key])))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knje013bQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //エラーメッセージをセット
    public function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val)
    {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
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
}
