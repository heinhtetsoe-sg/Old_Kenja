<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knje363Model extends Model
{
    public $field = array();
    public $data = array();
    public $cmd;
    public $file;
    public $control;
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knje363Model()
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

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "COMPANY_CD"       =>  VARS::post("COMPANY_CD"),         //会社
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        $this->getPropertiesAll();      //プロパティファイル
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
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knje363Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knje363Query::updateQueryCsv($this, $this->data_arr);
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
            $checkdata = array("YEAR"               => array(4,"年度"),
                               "SEQ"                => array(10,"連番"),
                               "SCHREGNO"           => array(8,"学籍番号"),
                               "TOROKU_DATE"        => array(10,"登録日"),
                               "STAT_CD"            => array(8,"会社コード"),
                               "JOBTYPE_LCD"        => array(1,"職業別大コード"),
                               "JOBTYPE_MCD"        => array(2,"職業別中コード"),
                               "JOBTYPE_SCD"        => array(3,"職業別小コード"),
                               "PREF_CD"            => array(2,"就業場所"),
                               "INTRODUCTION_DIV"   => array(1,"紹介区分"),
                               "REMARK1"            => array(99,"備考1"),
                               "REMARK2"            => array(99,"備考2"),
                               "HOWTOEXAM"          => array(2,"受験方式"),
                               "DECISION1"          => array(1,"出願区分"),
                               "DECISION2"          => array(1,"受験区分"),
                               "DECISION3"          => array(1,"合格区分"),
                               "PLANSTAT"           => array(1,"決定区分"));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i = 0;
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
                    $data = array("YEAR"                => (strlen(trim($tmp[0]))) ? sprintf("%04d", $tmp[0]) : "",
                                  "SEQ"                 => $tmp[1],
                                  "SCHREGNO"            => (strlen(trim($tmp[5]))) ? sprintf("%08d", $tmp[5]) : "",
                                  "TOROKU_DATE"         => $tmp[7],
                                  "STAT_CD"             => (strlen(trim($tmp[8]))) ? sprintf("%08d", $tmp[8]) : "",
                                  "JOBTYPE_LCD"         => $tmp[10],
                                  "JOBTYPE_MCD"         => $tmp[12],
                                  "JOBTYPE_SCD"         => $tmp[14],
                                  "PREF_CD"             => $tmp[16],
                                  "INTRODUCTION_DIV"    => $tmp[18],
                                  "REMARK1"             => $tmp[19],
                                  "REMARK2"             => $tmp[20],
                                  "HOWTOEXAM"           => $tmp[21],
                                  "DECISION1"           => $tmp[22],
                                  "DECISION2"           => $tmp[23],
                                  "DECISION3"           => $tmp[24],
                                  "PLANSTAT"            => $tmp[25]);

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        //ハイフンに変換してセット
                        if ($data["TOROKU_DATE"] != "") {
                            $data["TOROKU_DATE"] = $this->dateRep($data["TOROKU_DATE"]);
                        }
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
        $dateErrFlg      = false;    //日付エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $dateErrMsg      = "";       //日付エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $dateSep      = "";

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR", "SCHREGNO", "TOROKU_DATE", "STAT_CD", "INTRODUCTION_DIV"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            if ($this->field["SHORI_MEI"] == "2") {
                //必須項目チェック
                if (in_array($key, array("SEQ"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    } else {
                        //更新データ有無
                        if (1 > $db->getOne(knje363Query::updDataExists($data["YEAR"], $data[$key], $data["SCHREGNO"]))) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                        }
                    }
                }
            } else {
                //連番不要チェック
                if (in_array($key, array("SEQ"))) {
                    if (strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "新規選択時は".$val[1]."は空白");
                    }
                }
            }
            if (in_array($key, array("TOROKU_DATE"))) {
                if ($data[$key] != "" && $this->dateRep($data[$key]) == "") {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付フォーマット(斜線またはハイフン区切り):", $dateSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR", "SEQ", "SCHREGNO", "STAT_CD", "INTRODUCTION_DIV", "HOWTOEXAM", "DECISION1", "DECISION2", "DECISION3", "PLANSTAT"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }

            //存在チェック
            if (in_array($key, array("STAT_CD", "JOBTYPE_LCD", "JOBTYPE_MCD", "JOBTYPE_SCD", "PREF_CD", "DECISION1", "DECISION2", "DECISION3", "PLANSTAT"))) {
                if ($key == "STAT_CD") {
                    $query = knje363Query::chkCompanyMst($data[$key]);
                }
                if ($key == "JOBTYPE_LCD") {
                    $query = knje363Query::chkJobtypeLMst($data[$key]);
                }
                if ($key == "JOBTYPE_MCD") {
                    $query = knje363Query::chkJobtypeMMst($data["JOBTYPE_LCD"], $data[$key]);
                }
                if ($key == "JOBTYPE_SCD") {
                    $query = knje363Query::chkJobtypeSMst($data["JOBTYPE_LCD"], $data["JOBTYPE_MCD"], $data[$key]);
                }
                if ($key == "PREF_CD") {
                    $query = knje363Query::chkPrefMst($data[$key]);
                }
                if ($key == "DECISION1") {
                    $query = knje363Query::chkNameMst($data["YEAR"], 'E005', $data[$key], '1');
                }
                if ($key == "DECISION2") {
                    $query = knje363Query::chkNameMst($data["YEAR"], 'E005', $data[$key], '2');
                }
                if ($key == "DECISION3") {
                    $query = knje363Query::chkNameMst($data["YEAR"], 'E005', $data[$key], '3');
                }
                if ($key == "PLANSTAT") {
                    $query = knje363Query::chkNameMst($data["YEAR"], 'E006', $data[$key]);
                }

                if (strlen(trim($data[$key])) && $db->getOne($query) == 0) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }
            if (in_array($key, array("INTRODUCTION_DIV"))) {
                if (strlen(trim($data[$key])) && !in_array($data[$key], array(1, 2, 3))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$dateErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knje363Query::insertQueryErr($db, $line, $checkError);
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
        //ヘッダ項目（エラー出力）
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        //ヘッダ項目
        $header = array("YEAR"              => "年度",
                        "SEQ"               => "連番",
                        "GRADE"             => "学年",
                        "HR_CLASS"          => "クラス",
                        "ATTENDNO"          => "出席番号",
                        "SCHREGNO"          => "※学籍番号",
                        "NAME"              => "氏名",
                        "TOROKU_DATE"       => "※登録日",
                        "STAT_CD"           => "※会社コード",
                        "COMPANY_NAME"      => "会社名",
                        "JOBTYPE_LCD"       => "職業別大コード",
                        "JOBTYPE_LNAME"     => "職業別大名",
                        "JOBTYPE_MCD"       => "職業別中コード",
                        "JOBTYPE_MNAME"     => "職業別中名",
                        "JOBTYPE_SCD"       => "職業別小コード",
                        "JOBTYPE_SNAME"     => "職業別小名",
                        "PREF_CD"           => "就業場所",
                        "PREF_NAME"         => "就業名",
                        "INTRODUCTION_DIV"  => "※紹介区分",
                        "REMARK1"           => "備考1",
                        "REMARK2"           => "備考2",
                        "HOWTOEXAM"         => "受験方式",
                        "DECISION1"         => "出願区分",
                        "DECISION2"         => "受験区分",
                        "DECISION3"         => "合格区分",
                        "PLANSTAT"          => "決定区分",
                        $this->lastColumn   => $this->lastColumn);

        //ヘッダ例
        $header_rei = array("YEAR"              => "年度",
                            "SEQ"               => "連番",
                            "GRADE"             => "99",
                            "HR_CLASS"          => "999",
                            "ATTENDNO"          => "999",
                            "SCHREGNO"          => "12345678",
                            "NAME"              => "アルプ太郎",
                            "TOROKU_DATE"       => str_replace("-", "/", CTRL_DATE),
                            "STAT_CD"           => "12345678",
                            "COMPANY_NAME"      => "会社名",
                            "JOBTYPE_LCD"       => "A",
                            "JOBTYPE_LNAME"     => "職業別大名",
                            "JOBTYPE_MCD"       => "99",
                            "JOBTYPE_MNAME"     => "職業別中名",
                            "JOBTYPE_SCD"       => "999",
                            "JOBTYPE_SNAME"     => "職業別小名",
                            "PREF_CD"           => "13",
                            "PREF_NAME"         => "東京都",
                            "INTRODUCTION_DIV"  => "1～3",
                            "REMARK1"           => "全角33文字",
                            "REMARK2"           => "全角33文字",
                            "HOWTOEXAM"         => "99",
                            "DECISION1"         => "9",
                            "DECISION2"         => "9",
                            "DECISION3"         => "9",
                            "PLANSTAT"          => "9",
                            $this->lastColumn   => $this->lastColumn);

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
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
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knje363Query::selectMainQuery($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {    //データ
                    $row = str_replace(",", ".", $row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    if ($row["TOROKU_DATE"]) {
                        $row["TOROKU_DATE"] = str_replace("-", "/", $row["TOROKU_DATE"]);
                    }
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {    //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "受験報告";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
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
        if ($data != "") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }
        return $data;
    }
}
