<?php

require_once('for_php7.php');

class knjl384jModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $control;
    public $staffcd;
    public $examyear;
    public $lastColumn;//CSVの最後の列の値

    public function knjl384jModel()
    {
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;
        $this->examyear = CTRL_YEAR + 1;
    }

    public function init()
    {
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd          = VARS::request("cmd");
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }

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

    public function &getExecModel()
    {
        //ファイル取り込み
        $this->fileUpload();
    }

    //取込
    public function fileUpload()
    {
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl384jQuery::deleteQueryErr();
            $result = $db->query($query);

            $record_no = 1;            //行番号(初期値)

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    //１万件を超えたらループから抜ける。
                    if ($record_no > 10000) {
                        break;
                    }
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"] == "on" && $record_no < 2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");

                    $tmp = explode(",", $buffer);
                    $tmpno  = -1;
                    $data   = array("YEAR"              => trim($tmp[++$tmpno]),
                                    "EXAMNO"            => trim($tmp[++$tmpno]),
                                    "NAME"              => trim($tmp[++$tmpno]),
                                    "HR_CLASS"          => trim($tmp[++$tmpno]),
                                    "ATTENDNO"          => trim($tmp[++$tmpno]),
                                    "PRE_HR_CLASS"      => trim($tmp[++$tmpno]),
                                    "PRE_ATTENDNO"      => trim($tmp[++$tmpno]),
                                    "ATTENDFLG1"        => trim($tmp[++$tmpno]),
                                    "ATTENDFLG2"        => trim($tmp[++$tmpno]),
                                    "SCORE1"            => trim($tmp[++$tmpno]),
                                    "SCORE2"            => trim($tmp[++$tmpno]),
                                    "TOTAL_SCORE"       => trim($tmp[++$tmpno]),
                                    "REMARK"            => trim($tmp[++$tmpno]),
                                    "STATIONCD1"        => trim($tmp[++$tmpno]),
                                    "STATIONCD2"        => trim($tmp[++$tmpno]),
                                    "STATIONCD3"        => trim($tmp[++$tmpno]),
                                    "STATIONCD4"        => trim($tmp[++$tmpno]),
                                    "PRE_INFO1"         => trim($tmp[++$tmpno]),
                                    "PRE_INFO2"         => trim($tmp[++$tmpno]),
                                    "PRE_INFO3"         => trim($tmp[++$tmpno]));

                    $koumoku_error = $sonzai_error = "";    //エラー出力用

                    //項目チェック
                    $koumoku_error = $this->koumokuCheck($data);

                    //存在チェック(項目チェックがＯＫの場合)
                    if ($koumoku_error == "") {
                        $data = $this->zeroUme($data);//０埋め
                    }

                    //取り込み処理（エラーの場合、エラーＤＢへの追加）
                    if ($koumoku_error != "" || $sonzai_error != "") {
                        $check_error = "";
                        $check_error = $koumoku_error .$sonzai_error;
                        knjl384jQuery::insertQueryErr($db, $record_no, $check_error);
                    } else {
                        //プレスクール情報
                        $query = knjl384jQuery::getExistCheck($data);
                        $flg = $db->getOne($query);
                        //更新
                        if ($this->field["SHORI_MEI"] == "1") {
                            if (0 < $flg) {
                                //ＤＢにあれば、update
                                knjl384jQuery::updateQueryCsv($db, $data, $this->staffcd);
                            } else {
                                //ＤＢになければ、insert
                                knjl384jQuery::insertQueryCsv($db, $data, $this->staffcd);
                            }
                            //削除
                        } else {
                            //ＤＢにあれば、delete
                            if (0 < $flg) {
                                knjl384jQuery::deleteQueryCsv($db, $data);
                            }
                        }
                    }//ifの綴り
                }//ifの綴り
                $record_no++;    //行番号(カウント)
            }//whileの綴り
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
            $this->message = "CSV登録に成功しました。";
            //１万件を超えたら強制終了する。
            if ($record_no > 10000) {
                $this->message .= "\\n制限(１万件)を超えました。\\n".$record_no."行目以降は、未登録です。";
            }
        } else {
            $this->warning = "ファイル名が不正です。";
        }
    }


    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        $header = array("YEAR"              => "入学年度",
                        "EXAMNO"            => "受験番号",
                        "NAME"              => "氏名",
                        "HR_CLASS"          => "クラス",
                        "ATTENDNO"          => "出席番号",
                        "PRE_HR_CLASS"      => "仮クラス",
                        "PRE_ATTENDNO"      => "仮出席番号",
                        "ATTENDFLG1"        => "出欠１",
                        "ATTENDFLG2"        => "出欠２",
                        "SCORE1"            => "得点１（国語）",
                        "SCORE2"            => "得点２（算数）",
                        "TOTAL_SCORE"       => "合計",
                        "REMARK"            => "備考",
                        "STATIONCD1"        => "バス１",
                        "STATIONCD2"        => "バス２",
                        "STATIONCD3"        => "バス３",
                        "STATIONCD4"        => "バス４",
                        "PRE_INFO1"         => "情報１",
                        "PRE_INFO2"         => "情報２",
                        "PRE_INFO3"         => "情報３",
                        $this->lastColumn   => $this->lastColumn);
        $header_rei = array("YEAR"          => "2008",
                        "EXAMNO"            => "10001",
                        "NAME"              => "アルプ太郎",
                        "HR_CLASS"          => "001",
                        "ATTENDNO"          => "001",
                        "PRE_HR_CLASS"      => "001",
                        "PRE_ATTENDNO"      => "001",
                        "ATTENDFLG1"        => "1",
                        "ATTENDFLG2"        => "",
                        "SCORE1"            => "85",
                        "SCORE2"            => "75",
                        "TOTAL_SCORE"       => "160",
                        "REMARK"            => "備考です",
                        "STATIONCD1"        => "1",
                        "STATIONCD2"        => "",
                        "STATIONCD3"        => "",
                        "STATIONCD4"        => "",
                        "PRE_INFO1"         => "1",
                        "PRE_INFO2"         => "",
                        "PRE_INFO3"         => "",
                        $this->lastColumn   => $this->lastColumn);

        $contents  = "";

        if ($this->field["OUTPUT"]=="1") {             //ヘッダ
            $contents .= implode($header, ",")."\r\n";
            $contents .= implode($header_rei, ",")."\r\n";
        } elseif ($this->field["OUTPUT"]=="3") {      //エラー
            $contents .= implode($header_err, ",")."\r\n";
        } elseif ($this->field["OUTPUT"]=="4") {      //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header, ",")."\r\n";
            }
        }
        if ($this->field["OUTPUT"]=="1") {      //ヘッダ
            $header_data = true;
        } else {                                //ヘッダ以外
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            $table = "";
            $where = "";
            if ($this->field["OUTPUT"]=="3") {              //エラー
                $query = knjl384jQuery::selectCsvQuery();
            } elseif ($this->field["OUTPUT"]=="4") {        //データ
                $query = knjl384jQuery::getselectDataSQL($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $contents .= implode($row, ",") ."\r\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {      //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "プレスクール";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    /**
     * 項目チェック
     */
    public function koumokuCheck($data)
    {
        $koumoku_error = "";        //項目エラー用
        //更新
        if ($this->field["SHORI_MEI"] == "1") {
            //桁数チェック
            if (strlen($data["YEAR"]) > 4 ||
                strlen($data["EXAMNO"]) > 5 ||
                strlen($data["HR_CLASS"]) > 3 ||
                strlen($data["ATTENDNO"]) > 3 ||
                strlen($data["PRE_HR_CLASS"]) > 3 ||
                strlen($data["PRE_ATTENDNO"]) > 3 ||
                strlen($data["ATTENDFLG1"]) > 1 ||
                strlen($data["ATTENDFLG2"]) > 1 ||
                strlen($data["SCORE1"]) > 3 ||
                strlen($data["SCORE2"]) > 3 ||
                strlen($data["TOTAL_SCORE"]) > 3 ||
                strlen($data["STATIONCD1"]) > 1 ||
                strlen($data["STATIONCD2"]) > 1 ||
                strlen($data["STATIONCD3"]) > 1 ||
                strlen($data["STATIONCD4"]) > 1 ||
                strlen($data["PRE_INFO1"]) > 1 ||
                strlen($data["PRE_INFO2"]) > 1 ||
                strlen($data["PRE_INFO3"]) > 1) {
                $koumoku_error .= "桁数エラー。";
            }
            //必須項目チェック
            if ($data["YEAR"] == "" || $data["EXAMNO"] == "") {
                $koumoku_error .= "必須項目エラー。";
            }
            //数字チェック
            if (!$this->suujiCheck($data["SCORE1"]) ||
                !$this->suujiCheck($data["SCORE2"]) ||
                !$this->suujiCheck($data["TOTAL_SCORE"])) {
                $koumoku_error .= "数字項目エラー。";
            }
            //値チェック
            if (!$this->ataiCheck($data["ATTENDFLG1"], array("1")) ||
                !$this->ataiCheck($data["ATTENDFLG2"], array("1")) ||
                !$this->ataiCheck($data["STATIONCD1"], array("1")) ||
                !$this->ataiCheck($data["STATIONCD2"], array("1")) ||
                !$this->ataiCheck($data["STATIONCD3"], array("1")) ||
                !$this->ataiCheck($data["STATIONCD4"], array("1")) ||
                !$this->ataiCheck($data["PRE_INFO1"], array("1")) ||
                !$this->ataiCheck($data["PRE_INFO2"], array("1")) ||
                !$this->ataiCheck($data["PRE_INFO3"], array("1"))) {
                $koumoku_error .= "値エラー。";
            }
            //削除
        } else {
            //桁数チェック
            if (strlen($data["YEAR"]) > 4 || strlen($data["EXAMNO"]) > 5) {
                $koumoku_error .= "桁数エラー。";
            }
            //必須項目チェック
            if ($data["YEAR"] == "" || $data["EXAMNO"] == "") {
                $koumoku_error .= "必須項目エラー。";
            }
        }
        return $koumoku_error;    //項目エラー用
    }

    //数字チェック
    public function suujiCheck($data)
    {
        $flg = true;
        if ($data != "") {
            if (!is_numeric($data)) {
                $flg = false;
            }
        }
        return $flg;
    }

    //値チェック
    public function ataiCheck($data, $atai)
    {
        $flg = true;
        if ($data != "") {
            if (!in_array($data, $atai)) {
                $flg = false;
            }
        }
        return $flg;
    }

    /**
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    public function zeroUme($data)
    {
        //更新
        if ($this->field["SHORI_MEI"] == "1") {
            if ($data["EXAMNO"] != "") {
                if (strlen($data["EXAMNO"]) < 5) {
                    $data["EXAMNO"] = sprintf("%05s", $data["EXAMNO"]);
                }
            }
            if ($data["HR_CLASS"] != "") {
                if (strlen($data["HR_CLASS"]) < 3) {
                    $data["HR_CLASS"] = sprintf("%03s", $data["HR_CLASS"]);
                }
            }
            if ($data["ATTENDNO"] != "") {
                if (strlen($data["ATTENDNO"]) < 3) {
                    $data["ATTENDNO"] = sprintf("%03s", $data["ATTENDNO"]);
                }
            }
            if ($data["PRE_HR_CLASS"] != "") {
                if (strlen($data["PRE_HR_CLASS"]) < 3) {
                    $data["PRE_HR_CLASS"] = sprintf("%03s", $data["PRE_HR_CLASS"]);
                }
            }
            if ($data["PRE_ATTENDNO"] != "") {
                if (strlen($data["PRE_ATTENDNO"]) < 3) {
                    $data["PRE_ATTENDNO"] = sprintf("%03s", $data["PRE_ATTENDNO"]);
                }
            }
            //削除
        } else {
            if ($data["EXAMNO"] != "") {
                if (strlen($data["EXAMNO"]) < 5) {
                    $data["EXAMNO"] = sprintf("%05s", $data["EXAMNO"]);
                }
            }
        }
        return $data;
    }
}
