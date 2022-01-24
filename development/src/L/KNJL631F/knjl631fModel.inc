<?php
class knjl631fModel extends Model
{
    public $field = array();
    public $cmd;
    public $year;
    public $applicantdiv;
    public $file;

    public function knjl631fModel()
    {
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->applicantdiv = "1"; // 1:中学 固定

        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //実行種別
                             "HEADER"           =>  VARS::post("HEADER")            //ヘッダ有
                        );

        if (VARS::file("FILE")) {
            $this->file = VARS::file("FILE");        //ファイルのアップロード
            $this->file["name"] = i18n_convert($this->file["name"], "UTF-8", "SJIS-win");
        }
    }

    public function clean()
    {
    }

    //CSV取込
    public function &getCsvInputModel()
    {
        if ($this->validateInput()) {
            //ファイルの文字列の取得
            $data_arr = array();
            $this->err_count = 0;
            $data_arr = $this->getFileString();

            //データの整合性チェック
            if ($this->isDataMatched($data_arr)) {
                $cnt = knjl631fQuery::updateQueryCsv($this, $data_arr);

                $this->setMessage("MSG202", "処理済件数　：" . $cnt . "件\\nエラー件数    ：" . $this->err_count."件\\n" . $erraDatas);
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
            $checkdata = array();
            $checkdata["PRE_CNT"]      = array(1,  "HISSU", "INT",  "プレテスト回数");
            $checkdata["PRE_RECEPTNO"] = array(4,  "",      "INT",  "プレテスト受験番号");
            $checkdata["NAME"]         = array(20, "",      "CHAR", "氏名");
            $checkdata["NAME_KANA"]    = array(40, "HISSU", "CHAR", "かな氏名");
            $checkdata["PRE_KOKUGO"]   = array(3,  "",      "INT",  "国語得点");
            $checkdata["PRE_SANSUU"]   = array(3,  "",      "INT",  "算数得点");
            $checkdata["PRE_EIGO"]     = array(2,  "",      "INT",  "英語得点");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl631fQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof($fp)) {
                $buffer = fgets($fp);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = count($tmp);
                    //ホワイトスペースを取り除く
                    for ($t = 0; $t < $checkcount; $t++) {
                        $tmp[$t] = trim($tmp[$t]);
                    }

                    $cnt = 0;
                    $data = array();
                    $data["PRE_CNT"]      = $tmp[$cnt++];
                    $data["PRE_RECEPTNO"] = $tmp[$cnt++];
                    $data["NAME"]         = $tmp[$cnt++];
                    $data["NAME_KANA"]    = $tmp[$cnt++];
                    $data["PRE_KOKUGO"]   = $tmp[$cnt++];
                    $data["PRE_SANSUU"]   = $tmp[$cnt++];
                    $data["PRE_EIGO"]     = $tmp[$cnt++];

                    $dataCnt = count($data);

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $intaviewList)) {
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
    public function errCheck($db, $data, $data_each, $line, $intaviewList)
    {
        $errMessage = "";

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (strlen($errMessage) == 0 && $val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $errMessage = "※必須：".$val[3];
            }

            //数値チェック
            if (strlen($errMessage) == 0 && ($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $errMessage = "※数値：".$val[3];
            }

            //文字数チェック
            if (strlen($errMessage) == 0 && $val[0] != "" && strlen($data[$key]) > $val[0]) {
                $errMessage = "※サイズ：".$val[3];
            }

            //かな氏名存在チェック、重複チェック
            if (strlen($errMessage) == 0 && $key == "NAME_KANA" && $data[$key] != "") {
                $query = knjl631fQuery::checkNameKanaExists($this, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $errMessage = "願書データ未登録";
                } elseif ($rowCount > 1) {
                    $errMessage = "願書データに複数存在";
                }
            }

            //プレテスト回数 限界値越えチェック
            if (strlen($errMessage) == 0 && $key == "PRE_CNT" && $data[$key] != "") {
                if ($data[$key] > 2) {
                    $errMessage = "プレテスト回数が不正";
                }
            }

            //国語得点　満点越えチェック
            if (strlen($errMessage) == 0 && $key == "PRE_KOKUGO" && $data[$key] != "") {
                if ($data[$key] > 100) {
                    $errMessage = "国語得点が不正";
                }
            }

            //算数得点　満点越えチェック
            if (strlen($errMessage) == 0 && $key == "PRE_SANSUU" && $data[$key] != "") {
                if ($data[$key] > 100) {
                    $errMessage = "算数得点が不正";
                }
            }

            //英語得点　満点越えチェック
            if (strlen($errMessage) == 0 && $key == "PRE_EIGO" && $data[$key] != "") {
                if ($data[$key] > 50) {
                    $errMessage = "英語得点が不正";
                }
            }
        }

        //取り込み処理（エラーの場合、エラーＤＢへの追加）
        $returnFlg = strlen($errMessage) == 0 ? true : false;
        if (!$returnFlg) {
            $this->err_count++;
            knjl631fQuery::insertQueryErr($db, $line, $errMessage);
        }

        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //１万件を超えたら制限エラー
        if (count($data_arr) > 10000) {
            $this->setWarning("MSG915", "処理件数を１万件以下にして下さい 処理件数：" . count($data_arr) . "件");
            return false;
        }
        return true;
    }


    //ヘッダ出力
    public function getHeaderModel()
    {
        $header = array();
        $header += array("PRE_CNT"       => "プレテスト回数");
        $header += array("PRE_RECEPTNO"  => "プレテスト受験番号");
        $header += array("NAME"          => "氏名");
        $header += array("NAME_KANA"     => "かな氏名");
        $header += array("PRE_KOKUGO"    => "国語得点");
        $header += array("PRE_SANSUU"    => "算数得点");
        $header += array("PRE_EIGO"      => "英語得点");

        $header_rei = array();
        $header_rei += array("PRE_CNT"      => "1");
        $header_rei += array("PRE_RECEPTNO" => "999");
        $header_rei += array("NAME"         => "橋本　次郎");
        $header_rei += array("NAME_KANA"    => "はしもと　じろう");
        $header_rei += array("PRE_KOKUGO"   => "100");
        $header_rei += array("PRE_SANSUU"   => "100");
        $header_rei += array("PRE_EIGO"     => "50");

        $contents  = implode($header, ",")."\n";
        $contents .= implode($header_rei, ",")."\n";

        $header_data = true;

        $couse = "プレテスト";
        $filename = sprintf("%s.csv", $couse);
        common::downloadContents($contents, $filename);

        return true;
    }

    //ＣＳＶへエラー出力
    public function getCsvModel()
    {
        $header = array("MSGROW"     => "行番号",
                        "MSGREMARK"  => "メッセージ");

        $contents  = implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knjl631fQuery::selectCsvQuery();
        $result = $db->query($query);
        $err_data = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",") ."\n";
            $err_data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($err_data) {
            $couse = "エラー出力";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    public function validateInput()
    {
        if (!($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }

        return true;
    }
}
