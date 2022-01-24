<?php

require_once('for_php7.php');
class knjx_j120aModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    public function knjx_j120aModel()
    {
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

        $this->field = array("OUTPUT"       =>  VARS::post("OUTPUT"),       //出力取込種別
                             "HEADER"       =>  VARS::post("HEADER")        //ヘッダ有
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();  //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->file = VARS::file("FILE");

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    /****************/
    /*  ＣＳＶ取込  */
    /****************/
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            //エラー件数取得
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                 //ダミーテーブルに更新
                $cnt = knjx_j120aQuery::updateQueryCsv($this, $this->data_arr);
                if ($cnt > 0) {
                    //ダミーテーブルからメインテーブルに更新
                    knjx_j120aQuery::updateQuery($this);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("SCHREGNO"           => array(8, "学籍番号"),
                               "CLUBCD"             => array(4, "部クラブコード"),
                               "DETAIL_DATE"        => array(10,"日付"),
                               "MEET_NAME"          => array(60,"大会名称"),
                               "DIV"                => array(1, "区分"),
                               "GROUPCD"            => array(2, "団体記録番号"),
                               "HOSTCD"             => array(2, "開催地域"),
                               "ITEMCD"             => array(3, "種目"),
                               "KINDCD"             => array(3, "種目種類"),
                               "RECORDCD"           => array(3, "成績"),
                               "DOCUMENT"           => array(40, "記録"),
                               "DETAIL_REMARK"      => array(40,"備考"));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            //ダミーデータの削除
            $query = knjx_j120aQuery::deleteQueryDummy();
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

                    $data = array("SCHREGNO"        => (strlen(trim($tmp[0])))  ? sprintf("%08d", $tmp[0])   : "",
                                  "CLUBCD"          => (strlen(trim($tmp[1])))  ? sprintf("%04d", $tmp[1])   : "",
                                  "DETAIL_DATE"     => $tmp[2],
                                  "MEET_NAME"       => $tmp[3],
                                  "DIV"             => $tmp[4],
                                  "GROUPCD"         => $tmp[5],
                                  "HOSTCD"          => (strlen(trim($tmp[6])))  ? sprintf("%02d", $tmp[6])   : "",
                                  "ITEMCD"          => (strlen(trim($tmp[7])))  ? sprintf("%03d", $tmp[7])   : "",
                                  "KINDCD"          => (strlen(trim($tmp[8])))  ? sprintf("%03d", $tmp[8])   : "",
                                  "RECORDCD"        => (strlen(trim($tmp[9])))  ? sprintf("%03d", $tmp[9])   : "",
                                  "DOCUMENT"        => $tmp[10],
                                  "DETAIL_REMARK"   => $tmp[11]);

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
        $returnFlg        = true;
        $hissuErrFlg      = false;    //必須エラーフラグ
        $masterErrFlg     = false;    //マスタエラーフラグ
        $numericErrFlg    = false;    //数字エラーフラグ
        $lengthErrFlg     = false;    //レングスエラーフラグ
        $orderErrFlg      = false;    //固有エラーフラグ
        $dateFormatErrFlg = false;    //日付(フォーマット)エラーフラグ
        $valuesErrFlg     = false;    //値エラーフラグ

        $hissuErrMsg      = "";       //必須エラーメッセージ
        $masterErrMsg     = "";       //マスタエラーメッセージ
        $numericErrMsg    = "";       //数字エラーメッセージ
        $lengthErrMsg     = "";       //レングスエラーメッセージ
        $orderErrMsg      = "";       //固有エラーメッセージ
        $dateFormatErrMsg = "";       //日付(フォーマット)エラーメッセージ
        $valuesErrMsg     = "";       //値エラーメッセージ

        $hissuSep      = "";
        $numericSep    = "";
        $lengthSep     = "";
        $masterSep     = "";
        $orderSep      = "";
        $dateFormatSep = "";
        $valuesSep     = "";


        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("SCHREGNO","CLUBCD","DETAIL_DATE","DIV"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //数値チェック
            if (in_array($key, array("SCHREGNO","CLUBCD","DIV","GROUPCD","HOSTCD","ITEMCD","KINDCD","RECORDCD"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //日付チェック
            if (in_array($key, array("DETAIL_DATE"))) {
                if ($data[$key] != "" && (!$this->dateRep($data[$key]) || !(CTRL_YEAR.'-04-01' <= $data[$key] && $data[$key] <= (CTRL_YEAR+1).'-03-31'))) {
                    $returnFlg = $this->errSet($dateFormatErrFlg, $dateFormatErrMsg, "※日付(フォーマット):", $dateFormatSep, $val[1]);
                }
            }
            //団体のGROUPCD必須チェック
            if (in_array($key, array("GROUPCD"))) {
                if ($data["DIV"] == "2" && $data[$key] == "") {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, '団体は団体記録番号必須');
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
            //存在チェック
            if (in_array($key, array("SCHREGNO","CLUBCD","DIV","HOSTCD","ITEMCD","KINDCD","RECORDCD"))) {
                $query = "";
                if ($key == "SCHREGNO") {
                    $query = knjx_j120aQuery::checkRegdHistData($this, $data[$key], $data["CLUBCD"]);
                }
                if ($key == "CLUBCD") {
                    $query = knjx_j120aQuery::checkClubMst($this, $data[$key]);
                }
                if ($key == "HOSTCD") {
                    $query = knjx_j120aQuery::checkClubHostMst($this, $data[$key]);
                }
                if ($key == "ITEMCD") {
                    $query = knjx_j120aQuery::checkClubItemMst($this, $data[$key]);
                }
                if ($key == "KINDCD") {
                    $query = knjx_j120aQuery::checkClubItemKindMst($this, $data["ITEMCD"], $data[$key]);
                }
                if ($key == "RECORDCD") {
                    $query = knjx_j120aQuery::checkClubRecordMst($this, $data[$key]);
                }

                if ($query) {
                    if ($data[$key] != "" && 0 == $db->getOne($query)) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                    }
                } else {
                    if ($key == "DIV" && !in_array($data[$key], array('1','2'))) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $hissuErrMsg.$masterErrMsg.$numericErrMsg.$lengthErrMsg.$orderErrMsg.$dateFormatErrMsg.$valuesErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_j120aQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //日付変換 項目ﾁｪｯｸ後に行う。
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

    /****************/
    /*  ＣＳＶ出力  */
    /****************/
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        //ヘッダデータ
        $header = array("SCHREGNO"          => "※学籍番号",
                        "CLUBCD"            => "※部クラブコード",
                        "DETAIL_DATE"       => "※記録日付",
                        "MEET_NAME"         => "大会名称",
                        "DIV"               => "※区分",
                        "GROUPCD"           => "団体記録番号",
                        "HOSTCD"            => "開催地域",
                        "ITEMCD"            => "種目",
                        "KINDCD"            => "種類",
                        "RECORDCD"          => "成績",
                        "DOCUMENT"          => "記録",
                        "DETAIL_REMARK"     => "備考",
                        $this->lastColumn   => $this->lastColumn);

        //ヘッダ例用データ取得
        $gethrclass = $db->getRow(knjx_j120aQuery::getSchregRegdhdat($this), DB_FETCHMODE_ASSOC);
        $getclub = $db->getRow(knjx_j120aQuery::getClubMst($this), DB_FETCHMODE_ASSOC);

        //ヘッダ例
        $header_rei = array("SCHREGNO"          => "12345678",
                            "CLUBCD"            => $getclub["CLUBCD"],
                            "DETAIL_DATE"       => str_replace('-', '/', CTRL_DATE),
                            "MEET_NAME"         => "全角30文字まで",
                            "DIV"               => "1:個人 2:団体",
                            "GROUPCD"           => "12",
                            "HOSTCD"            => "12",
                            "ITEMCD"            => "123",
                            "KINDCD"            => "123",
                            "RECORDCD"          => "123",
                            "DOCUMENT"          => "全角20文字まで",
                            "DETAIL_REMARK"     => "全角20文字まで",
                            $this->lastColumn   => $this->lastColumn);

        $contents  = "";    //クリア

        $csv_output = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ出力
            $csv_output = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー出力
            $contents .= implode($header_err, ",")."\n";
        }

        //エラー出力
        if ($this->field["OUTPUT"] == "3") {
            $query = $this->selectCsvErrQuery(PROGRAMID);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $contents .= implode($row, ",") ."\n";
                $csv_output = true;
            }
            $result->free();
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($csv_output) {
            if ($this->field["OUTPUT"] == "3") {
                $couse = "エラー出力";
            } else {
                $couse = "大会記録備考データ";
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
