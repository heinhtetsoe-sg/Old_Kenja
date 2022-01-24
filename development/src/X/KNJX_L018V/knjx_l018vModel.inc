<?php

require_once('for_php7.php');
class knjx_l018vModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $schoolCd;

    public function knjx_l018vModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),              //出力取込種別
                             "EXAM_YEAR"        =>  VARS::request("EXAM_YEAR"),        //入試年度
                             "EXAM_SCHOOL_KIND" =>  VARS::request("EXAM_SCHOOL_KIND"), //校種
                             "EXAM_ID"          =>  VARS::request("EXAM_ID"),          //試験ID
                             "PLACE_ID"         =>  VARS::request("PLACE_ID"),         //会場
                             "OUTPUT"           =>  VARS::post("OUTPUT"),              //処理種別
                             "HEADER"           =>  VARS::post("HEADER")               //ヘッダ有
                            );

        $this->cmd  = VARS::request("cmd");

        $this->file = VARS::file("FILE");
    }

    /**
     * 実行モデルを返します
    */
    public function &getUploadModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                knjx_l018vQuery::insertQueryCsv($this, $this->data_arr);
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
            $db = Query::dbCheckOut();

            $checkdata["YEAR"]        = array(4,  "年度");
            $checkdata["EXAM_ID"]     = array(9,  "試験ID");
            $checkdata["RECEPTNO"]    = array(4,  "受験番号");
            $checkdata["ABSENCE_FLG"] = array(1,  "欠席フラグ");

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjx_l018vQuery::deleteQueryErr();
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

                    $dataCnt = 0;
                    $data["YEAR"]           = (strlen(trim($tmp[$dataCnt]))) ? $tmp[$dataCnt] : "";
                    $data["EXAM_ID"]        = (strlen(trim($tmp[++$dataCnt]))) ? $tmp[$dataCnt] : "";
                    ++$dataCnt;
                    $data["RECEPTNO"]       = (strlen(trim($tmp[++$dataCnt]))) ? sprintf("%04d", $tmp[$dataCnt]) : "";
                    ++$dataCnt;
                    $data["ABSENCE_FLG"]    = (strlen(trim($tmp[++$dataCnt]))) ? $tmp[$dataCnt] : "";

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    $line++;
                }
            }
            $db->commit();  // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }
        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ

        $hissuSep     = "";
        $lengthSep    = "";
        $existenceSep = "";

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR","EXAM_ID","RECEPTNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }

        //存在チェック（志願者）
        $query = knjx_l018vQuery::selectReceptDat($data["YEAR"], $data["RECEPTNO"], $data["EXAM_ID"]);
        $receptno = $db->getOne($query);
        if (!$receptno) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $data_each["RECEPTNO"][1]);
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$lengthErrMsg.$existenceErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjx_l018vQuery::insertQueryErr($db, $line, $check_error);
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

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //ヘッダー（エラー）
        $header_err = array("PROGRAMID"     => "プログラムID",
                            "MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        //ヘッダー
        $header["YEAR"]        = "年度";
        $header["EXAM_ID"]     = "試験ID";
        $header["EXAM_NAME"]   = "試験名";
        $header["RECEPTNO"]    = "受験番号";
        $header["NAME"]        = "氏名";
        $header["ABSENCE_FLG"] = "欠席フラグ";

        //ヘッダー例
        $header_rei["YEAR"]        = "2017";
        $header_rei["EXAM_ID"]     = "半角9桁";
        $header_rei["EXAM_NAME"]   = "一般入試 4科目";
        $header_rei["RECEPTNO"]    = "数字4桁";
        $header_rei["NAME"]        = "アルプ太郎";
        $header_rei["ABSENCE_FLG"] = "1";

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
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = knjx_l018vQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx_l018vQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {              //データ
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();

            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {      //エラー
                $title = "エラー出力";
            } else {                                //エラー以外
                $title = "欠席者登録";
            }
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
