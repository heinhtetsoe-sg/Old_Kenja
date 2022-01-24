<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx153cModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx153cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    public function init()
    {
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "OUT_SEMESTER"     =>  VARS::post("OUT_SEMESTER"),     //学期一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI"),        //処理名
                             "SEMESTER"         =>  VARS::post("SEMESTER")          //テスト
                             );

        $this->cmd          = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        //プロパティファイル
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
                    knjx153cQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx153cQuery::deleteQueryCsv($this, $this->data_arr);
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
            $checkdata = array("YEAR"                     => array(4,"年度"),
                               "SEMESTER"                 => array(1,"学期"),
                               "SCHREGNO"                 => array(8,"学籍番号"),
                               "REMARK1"                  => array(405,"個人評"));

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
            $primaryKey1 = $this->getPrimaryKey('HEXAM_RECORD_REMARK_DAT');
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
                    $tmp = common::csv2array($buffer);

                    $data = array("YEAR"            => (strlen(trim($tmp[4]))) ? sprintf("%04d", $tmp[4]) : "",
                                  "SEMESTER"        => (strlen(trim($tmp[5]))) ? sprintf("%01d", $tmp[5]) : "",
                                  "SCHREGNO"        => (strlen(trim($tmp[6]))) ? sprintf("%08d", $tmp[6]) : "",
                                  "REMARK1"         => $tmp[7]);

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
        $returnFlg = true;

        //必須エラー フラグ：メッセージ：セパレータ
        $hissuErrFlg = false;
        $hissuErrMsg = "";
        $hissuSep = "";

        //数字エラー フラグ：メッセージ：セパレータ
        $numericErrFlg = false;
        $numericErrMsg = "";
        $numericSep = "";

        //レングスエラー フラグ：メッセージ：セパレータ
        $lengthErrFlg = false;
        $lengthErrMsg = "";
        $lengthSep = "";

        //項目エラー フラグ：メッセージ：セパレータ
        $itemErrFlg = false;
        $itemErrMsg = "";
        $itemSep = "";

        //重複(CSV)エラー フラグ：メッセージ：セパレータ
        $duplicateCsvErrFlg = false;
        $duplicateCsvErrMsg = "";
        $duplicateCsvSep = "";

        $dupFlg = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg[1] ."行目)");
        }
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR", "SEMESTER", "SCHREGNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR", "SEMESTER", "SCHREGNO"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //文字数チェック(更新時)
            if ($this->field["SHORI_MEI"] == "1") {
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }

            //チェック
            if (in_array($key, array("YEAR"))) {
                if ($data[$key] != "" && $data[$key] != CTRL_YEAR) {
                    $returnFlg = $this->errSet($itemErrFlg, $itemErrMsg, "※不一致:", $itemSep, $val[1]);
                }
            }

            //チェック
            if (in_array($key, array("SEMESTER"))) {
                if ($data[$key] != "" && $data[$key] != $this->field["SEMESTER"]) {
                    $returnFlg = $this->errSet($itemErrFlg, $itemErrMsg, "※不一致:", $itemSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$itemErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx153cQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
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
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = array("GRADE"                     => "学年",
                        "HR_CLASS"                  => "クラス",
                        "ATTENDNO"                  => "出席番号",
                        "NAME"                      => "氏名",
                        "YEAR"                      => "※年度",
                        "SEMESTER"                  => "※学期",
                        "SCHREGNO"                  => "※学籍番号",
                        "REMARK1"                   => "個人評",
                        $this->lastColumn           => $this->lastColumn);

        $header_rei = array("GRADE"                     => "99",
                            "HR_CLASS"                  => "999",
                            "ATTENDNO"                  => "999",
                            "NAME"                      => "アルプ太郎",
                            "YEAR"                      => "9999",
                            "SEMESTER"                  => "9",
                            "SCHREGNO"                  => "12345678",
                            "REMARK1"                   => "全角30文字",
                            $this->lastColumn           => $this->lastColumn);

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $headerDuplicate = array();
            $headerDuplicate["YEAR"]           = "年度";
            $headerDuplicate["SEMESTER"]       = "学期";
            $headerDuplicate["TESTKINDCD"]     = "テスト種別コード";
            $headerDuplicate["TESTITEMCD"]     = "テスト項目コード";
            $headerDuplicate["SCHREGNO"]       = "学籍番号";
            $headerDuplicate["REMARK_DIV"]     = "所見１";
            $tables = array();
            $tables = array("1" => "HEXAM_RECORD_REMARK_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
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

            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx153cQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {
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
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $couse = "エラー出力";
            } else {
                //エラー以外
                $couse = "類型グループコース個人評所見データ";
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
