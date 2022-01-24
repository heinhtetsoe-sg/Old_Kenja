<?php

require_once('for_php7.php');


class knjp183kModel extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knjp183kModel() {
    }

    function init()
    {
        $this->field = array("DATA_DIV" =>  VARS::post("DATA_DIV"),
                             "HEADER"   =>  VARS::post("HEADER"),
                             "OUTPUT"   =>  VARS::post("OUTPUT")
                     );

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            $cnt = knjp183kQuery::updateQueryCsv($this, $this->data_arr);

            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_DAT");
            Query::dbCheckIn($db);
            $this->setMessage("MSG202","処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    //取込
    function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            if ($this->field["DATA_DIV"] == "1") {
                $checkdata = array("PASSNO"                     => array(16, "認定番号"),
                                   "STATUS"                     => array(2, "ステータス"),
                                   "RECEIVE_MONEY"              => array(60, "支給額"),
                                   "REMAIN_SUP_LIMIT_MONTH"     => array(120, "残支給期間"));
            } else {
                $checkdata = array("PASSNO"                     => array(16, "認定番号"),
                                   "BEGIN_YEARMONTH"            => array(10, "開始年月"),
                                   "STATUS"                     => array(2, "ステータス"),
                                   "RECEIVE_MONEY"              => array(60, "支給額"),
                                   "ADD_MONEY"                  => array(60, "加算額"));
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjp183kQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof ($fp))
            {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '')
                {
                    if (!$header && $this->field["HEADER"] == "on"){
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer, "\t");

                    if ($this->field["DATA_DIV"] == "1") {
                        $data = array("PASSNO"                     => $tmp[1],
                                      "STATUS"                     => sprintf("%02d", $tmp[5]),
                                      "RECEIVE_MONEY"              => $tmp[8],
                                      "REMAIN_SUP_LIMIT_MONTH"     => $tmp[9]);
                    } else {
                        $data = array("PASSNO"                     => $tmp[1],
                                      "BEGIN_YEARMONTH"            => $tmp[2],
                                      "STATUS"                     => sprintf("%02d", $tmp[3]),
                                      "RECEIVE_MONEY"              => $tmp[5],
                                      "ADD_MONEY"                  => $tmp[6]);
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
    function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $dataFuseiErrFlg = false;    //データ不正エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $dataFuseiErrMsg = "";       //データ不正エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $dataFuseiSep = "";

        foreach ($data_each as $key => $val) {

            //必須項目チェック
            if (in_array($key, array("PASSNO", "BEGIN_YEARMONTH", "STATUS", "RECEIVE_MONEY", "REMAIN_SUP_LIMIT_MONTH", "ADD_MONEY"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //データ不正チェック
            if (in_array($key, array("STATUS"))) {
                $checkVal = $this->field["DATA_DIV"] == "1" ? "02" : "03";
                if ($data[$key] != $checkVal) {
                    $returnFlg = $this->errSet($dataFuseiErrFlg, $dataFuseiErrMsg, "※データ不正:", $dataFuseiSep, $val[1]);
                }
            }

            //存在チェック
            if (in_array($key, array("PASSNO"))) {
                if ($data[$key] != "" && 1 > $db->getOne(knjp183kQuery::checkPassNo($this, $data[$key], $data["BEGIN_YEARMONTH"]))) {
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
            if (in_array($key, array("STATUS", "RECEIVE_MONEY", "REMAIN_SUP_LIMIT_MONTH", "ADD_MONEY"))) {
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
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$dataFuseiErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjp183kQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //エラーメッセージをセット
    function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val)
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
    function IsDataMatched(&$data_arr)
    {
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
    function date_rep($data)
    {
        if ($data!="") {
            $data = common::DateConv1(str_replace("-","/",$data),4);
            if($data) $data = str_replace("/","-",$data);
        }

        return $data;
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        if ($this->field["DATA_DIV"] == "1") {
            $header = array("SCHOOLCD"                  => "学校コード(未使用)",
                            "PASSNO"                    => "認定番号",
                            "TOSI_NO"                   => "通し番号(未使用)",
                            "BUNSYO_NO"                 => "文書番号(未使用)",
                            "TUTI_BI"                   => "通知日(未使用)",
                            "STATUS"                    => "ステータス",
                            "FUMEI"                     => "(不明)",
                            "FUFUKU"                    => "不服申立期間(未使用)",
                            "RECEIVE_MONEY"             => "月額支給額",
                            "REMAIN_SUP_LIMIT_MONTH"    => "残支給期間");

            $header_rei = array("SCHOOLCD"                  => "99",
                                "PASSNO"                    => "99-999-9999-9999",
                                "TOSI_NO"                   => "9999",
                                "BUNSYO_NO"                 => "私第9999号",
                                "TUTI_BI"                   => "9999/99/99",
                                "STATUS"                    => "99",
                                "FUMEI"                     => "",
                                "FUFUKU"                    => "99",
                                "RECEIVE_MONEY"             => "9999",
                                "REMAIN_SUP_LIMIT_MONTH"    => "99");
        } else {
            $header = array("SCHOOLCD"          => "学校コード(未使用)",
                            "PASSNO"            => "認定番号",
                            "BEGIN_YEARMONTH"   => "開始年月",
                            "STATUS"            => "ステータス",
                            "GETUGAKU"          => "月額授業料(未使用)",
                            "RECEIVE_MONEY"     => "支給額",
                            "ADD_MONEY"         => "加算額",
                            "FUMEI1"            => "(不明)",
                            "FUMEI2"            => "(不明)");

            $header_rei = array("SCHOOLCD"          => "99",
                                "PASSNO"            => "99-999-9999-9999",
                                "BEGIN_YEARMONTH"   => "9999/99/99",
                                "STATUS"            => "99",
                                "GETUGAKU"          => "99999",
                                "RECEIVE_MONEY"     => "9999",
                                "ADD_MONEY"         => "9999",
                                "FUMEI1"            => "",
                                "FUMEI2"            => "");
        }

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "2") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, "\t")."\n";
            $contents .= implode($header_rei, "\t")."\n";
        } else if ($this->field["OUTPUT"] == "3") {
            //エラー
            $contents .= implode($header_err, ",")."\n";
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "2") {
            //DB接続
            $db = Query::dbCheckOut();

            //エラー
            $query = knjp183kQuery::selectCsvErrQuery();

            //SQL文発行
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($this->field["OUTPUT"]=="4"){                //データ
                    $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
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
                $setName = $this->field["DATA_DIV"] == "1" ? "受給資格認定" : "加算決定通知";
                $couse = $setName."テンプレート";
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
