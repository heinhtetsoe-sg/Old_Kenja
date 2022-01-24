<?php

require_once('for_php7.php');


class knjp734Model extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knjp734Model() {
    }

    function init() {
        $this->field = array("DATA_DIV"         =>  VARS::post("DATA_DIV"),
                             "COURSECD"         =>  VARS::post("COURSECD"),         //課程(1:全日制、2:定時・通信制)
                             "HEADER"           =>  VARS::post("HEADER"),
                             "OUTPUT"           =>  VARS::post("OUTPUT")
                     );

        $this->cmd = VARS::request("cmd");
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;
        $this->month = VARS::post("MONTH");

        $this->file = VARS::file("FILE");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $db = Query::dbCheckOut();
        $this->schoolName = $db->getOne(knjp734query::getNameMstZ010());
        Query::dbCheckIn($db);
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                knjp734Query::updateQueryCsv($this, $this->data_arr);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    function GetFileString() { 
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            if ($this->field["COURSECD"] == "1") {
                $checkdata = array("PASSNO"                     => array(25, "登録番号a"),
                                   "STATUS"                     => array(2,  "現在のステータス"),
                                   "BEGIN_YEARMONTH"            => array(10, "開始年月a"));
            } else {
                $checkdata = array("PASSNO"                     => array(25, "登録番号b"),
                                   "BEGIN_YEARMONTH"            => array(10, "開始年月b"));
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $duplicateCsv1 = array();

            $primaryKey1[] = "RENBEN";
            while (!feof ($fp)) {
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

                    if ($this->field["COURSECD"] == "1") {
                        $data = array("PASSNO"                     => $tmp[0],
                                      "STATUS"                     => $tmp[2],
                                      "BEGIN_YEARMONTH"            => $tmp[3]);
                    } else {
                        $data = array("PASSNO"                     => $tmp[0],
                                      "BEGIN_YEARMONTH"            => $tmp[3]);
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }

                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = substr($data["PASSNO"], -4);
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
    function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1) { 
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $dataFuseiErrFlg = false;    //データ不正エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $dataFuseiErrMsg = "";       //データ不正エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $dataFuseiSep = "";
        $duplicateCsvSep = "";

        $data["RENBEN"] = substr($data["PASSNO"], -4);
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {

            //必須項目チェック
            if (in_array($key, array("PASSNO", "BEGIN_YEARMONTH", "STATUS"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
/*
            //データ不正チェック
            if (in_array($key, array("STATUS"))) {
                $checkVal = $this->field["DATA_DIV"] == "1" ? "02" : "03";
                if ($data[$key] != $checkVal) {
                    $returnFlg = $this->errSet($dataFuseiErrFlg, $dataFuseiErrMsg, "※データ不正:", $dataFuseiSep, $val[1]);
                }
            }
*/
            //存在チェック
            if (in_array($key, array("PASSNO"))) {
                if ($data[$key] != "" && 1 > $db->getOne(knjp734Query::checkPassNo($this, $data[$key], $data["BEGIN_YEARMONTH"]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }

            //日付の書式チェック
            if (in_array($key, array("BEGIN_YEARMONTH"))) {
                if($data[$key] != "" && !$this->date_rep($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※日付:", $numericSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("STATUS"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }

        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$dataFuseiErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjp734Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr) { 
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;

    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    function date_rep($data) {
        if ($data!="") {
            $data = common::DateConv1(str_replace("-","/",$data),4);
            if($data) $data = str_replace("/","-",$data);
        }

        return $data;
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        if ($this->field["COURSECD"] == "1") {
            $header["PASSNO"]           = "登録番号";
            $header["NAME"]             = "生徒氏名";
            $header["STATUS"]           = "現在のステータス";
            $header["BEGIN_YEARMONTH"]  = "開始年月";
            $header["INTENTION_FLG"]    = "申請しない人（希望しない人）";
            $header["G_CHECK1"]         = "保護者１の所得確認";
            $header["REDUC_INCOME1"]    = "保護者１の市町村所得割";
            $header["G_CHECK2"]         = "保護者２の所得確認";
            $header["REDUC_INCOME2"]    = "保護者２の市町村所得割";
            $header["REMARK"]           = "備考";

            //福井県
            if ($this->schoolName == "fukui") {
                $header["GRADE"]                            = "学年";
                $header["ZIPCD"]                            = "郵便番号";
                $header["ADDR1"]                            = "住所１";
                $header["ADDR2"]                            = "住所２";
                $header["SCHOLARSHIP_PAYMENT_YES_NO_FLG"]   = "就学給付金の申請書有無";
                $header["GUARD_NAME"]                       = "保護者氏名";
            }

        } else {
            $header["PASSNO"]           = "登録番号";
            $header["GRP_NAME"]         = "グループ名";
            $header["CREDIT"]           = "単位数";
            $header["BEGIN_YEARMONTH2"] = "開始年月";
            $header["RISYU_KIKAN"]      = "履修期間";
            $header["GRP_REMARK"]       = "備考";
        }

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "2") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, "\t")."\n";
        } else if ($this->field["OUTPUT"] == "3") {
            //エラー
            $tables = array();
            $tables = array("1" => "REDUCTION_AUTHORIZE_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            $header = array();
            if ($this->field["COURSECD"] == "1") {
                $header["PASSNO"] = "登録番号aの最後の4桁";
            } else {
                $header["PASSNO"] = "登録番号bの最後の4桁";
            }
            if (sizeof($dupTable) > 0) {
                $headerErr = array( "MSGROW"            => "",
                                    "MSGREMARK"         => "〇重複1:[".$header["PASSNO"]."]");
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } else {
                $query = knjp734Query::selectCsvData($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if ($this->field["OUTPUT"]=="2") {                //データ
                    $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                    //ヘッダ順に値をセット
                    $comma = "";
                    foreach ($header as $key => $headerName) {
                        $contents .= $comma.$row[$key];
                        $comma = ",";
                    }
                    $contents .= "\n";
                } else {
                    $contents .= implode($row, ",") ."\n";
                }
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if($this->field["OUTPUT"] == "3"){      //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $setHeaderName = ($this->field["COURSECD"] == "1") ? "全日制": "定時・通信制";
                $couse = $setHeaderName."_データ出力";
            }
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

}
?>
