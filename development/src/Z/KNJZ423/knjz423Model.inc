<?php

require_once('for_php7.php');

//定数定義
class knjz423Model extends Model {
    var $cmd;
    var $data;
    var $max;
     //メンバ変数の初期化
    function knjz423Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd      = VARS::request("cmd");

        $this->field = array("TAISYOU"       =>  VARS::post("TAISYOU"),     //取込種別
                             "OUTPUT"        =>  VARS::post("OUTPUT"),      //ラジオ(1:データ取込 2:エラー出力)
                             "HEADER"        =>  VARS::post("HEADER")       //ヘッダ有
                             );

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }

    }

    function &getMainModel() {
    }

    //実行モデルを返します
    function &getExecuteModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                knjz423Query::insertQueryCsv($this, $this->data_arr);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            if ($this->field["TAISYOU"] == "1") {
                $checkdata = array("INDUSTRY_LCD"           => array(1,   "大分類コード"),
                                   "INDUSTRY_MCD"           => array(2,   "中分類コード"),
                                   "INDUSTRY_SCD"           => array(3,   "小分類コード"),
                                   "INDUSTRY_LMSNAME"       => array(150, "項目名")
                                   );
            } else {
                $checkdata = array("JOBTYPE_LCD"           => array(2,   "大分類コード"),
                                   "JOBTYPE_MCD"           => array(2,   "中分類コード"),
                                   "JOBTYPE_SCD"           => array(3,   "小分類コード"),
                                   "JOBTYPE_LMSNAME"       => array(150, "項目名")
                                   );
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen ($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey2 = array();
            $primaryKey3 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
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

                    if ($this->field["TAISYOU"] == "1") {
                        $data = array("INDUSTRY_LCD"                => $tmp[0],
                                      "INDUSTRY_MCD"                => (strlen(trim($tmp[1]))) ? sprintf("%02d",$tmp[1]) : "",
                                      "INDUSTRY_SCD"                => (strlen(trim($tmp[2]))) ? sprintf("%03d",$tmp[2]) : "",
                                      "INDUSTRY_DCD"                => (strlen(trim($tmp[3]))) ? sprintf("%04d",$tmp[3]) : "",
                                      "INDUSTRY_LMSNAME"            => $tmp[4]);
                    } else {
                        $data = array("JOBTYPE_LCD"                => $tmp[0],
                                      "JOBTYPE_MCD"                => (strlen(trim($tmp[1]))) ? sprintf("%02d",$tmp[1]) : "",
                                      "JOBTYPE_SCD"                => (strlen(trim($tmp[2]))) ? sprintf("%03d",$tmp[2]) : "",
                                      "JOBTYPE_LMSNAME"            => $tmp[3]);
                    }
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    if ($this->field["TAISYOU"] == "1") {
                        if ($data["INDUSTRY_DCD"] == "0000") {
                            if ($data["INDUSTRY_SCD"] == "000") {
                                if ($data["INDUSTRY_MCD"] == "00") {
                                    $primaryKey3 = $this->getPrimaryKey('INDUSTRY_L_MST');
                                    foreach ($primaryKey3 as $key => $val) {
                                        $duplicateCsv3[$line][$val] = $data[$val];
                                    }
                                } else {
                                    $primaryKey2 = $this->getPrimaryKey('INDUSTRY_M_MST');
                                    foreach ($primaryKey2 as $key => $val) {
                                        $duplicateCsv2[$line][$val] = $data[$val];
                                    }
                                }
                            } else {
                                $primaryKey1 = $this->getPrimaryKey('INDUSTRY_S_MST');
                                foreach ($primaryKey1 as $key => $val) {
                                    $duplicateCsv1[$line][$val] = $data[$val];
                                }
                            }
                        }
                    } else {
                        if ($data["JOBTYPE_SCD"] == "000") {
                            if ($data["JOBTYPE_MCD"] == "00") {
                                $primaryKey3 = $this->getPrimaryKey('JOBTYPE_L_MST');
                                foreach ($primaryKey3 as $key => $val) {
                                    $duplicateCsv3[$line][$val] = $data[$val];
                                }
                            } else {
                                $primaryKey2 = $this->getPrimaryKey('JOBTYPE_M_MST');
                                foreach ($primaryKey2 as $key => $val) {
                                    $duplicateCsv2[$line][$val] = $data[$val];
                                }
                            }
                        } else {
                            $primaryKey1 = $this->getPrimaryKey('JOBTYPE_S_MST');
                            foreach ($primaryKey1 as $key => $val) {
                                $duplicateCsv1[$line][$val] = $data[$val];
                            }
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
    function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3) {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }
        $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data);
        if ($dupFlg3[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg3[1] ."行目)");
        }
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("INDUSTRY_LCD", "INDUSTRY_MCD", "INDUSTRY_SCD", "JOBTYPE_LCD", "JOBTYPE_MCD", "JOBTYPE_SCD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("INDUSTRY_MCD", "INDUSTRY_SCD", "JOBTYPE_MCD", "JOBTYPE_SCD"))) {
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
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjz423Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;

    }

    //ＣＳＶ出力
    function getDownloadModel() {
        $duplicateHeader = array("INDUSTRY_LCD"  =>  "大分類コード",
                                "INDUSTRY_MCD"   => "中分類コード",
                                "INDUSTRY_SCD"   =>  "小分類コード",
                                "JOBTYPE_LCD"    =>  "大分類コード",
                                "JOBTYPE_MCD"    =>  "中分類コード",
                                "JOBTYPE_SCD"    =>  "小分類コード");
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = array();

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="2") {
            //エラー
            $tables = array();
            if ($this->field["TAISYOU"] == "1") {
                $tables = array("1" => "INDUSTRY_S_MST", "2" => "INDUSTRY_M_MST", "3" => "INDUSTRY_L_MST");
            } else {
                $tables = array("1" => "JOBTYPE_S_MST", "2" => "JOBTYPE_M_MST", "3" => "JOBTYPE_L_MST");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $duplicateHeader);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        if($this->field["OUTPUT"]=="2") {
            //エラー
            $query = $this->selectCsvErrQuery(PROGRAMID);
        }

        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $array = $header_err;

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

        //ＣＳＶ出力
        if ($header_data) {
            if($this->field["OUTPUT"]=="2"){        //エラー
                $couse = "エラー出力";
            }
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

    //変数をクリアします
    function clean() {
    }
}
?>