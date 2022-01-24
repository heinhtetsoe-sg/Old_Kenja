<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx181Model extends Model
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
    public function knjx181Model()
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
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd          = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        $this->getPropertiesAll(); //プロパティファイル
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
                    knjx181Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx181Query::deleteQueryCsv($this, $this->data_arr);
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
            if ($this->getname === 'tokyoto') {
                $checkdata = array("SCHREGNO"                  => array(8,"学籍番号"),
                                   "TOTALSTUDYACT"             => array(528,"総合学習活動"),
                                   "TOTALSTUDYVAL"             => array(792,"総合学習評価"),
                                   "TOTALSTUDYACT2"            => array(528,"奉仕活動"),
                                   "TOTALSTUDYVAL2"            => array(792,"奉仕評価"),
                                   "CREDITREMARK"              => array(660,"備考"));
                //行数チェック
                $checkdata2 = array("TOTALSTUDYACT"            => array(4,"総合学習活動"),
                                   "TOTALSTUDYVAL"             => array(6,"総合学習評価"),
                                   "TOTALSTUDYACT2"            => array(4,"奉仕活動"),
                                   "TOTALSTUDYVAL2"            => array(6,"奉仕評価"),
                                   "CREDITREMARK"              => array(5,"備考"));
            } else {
                $checkdata = array("SCHREGNO"                  => array(8,"学籍番号"),
                                   "TOTALSTUDYACT"             => array(528,"総合学習活動"),
                                   "TOTALSTUDYVAL"             => array(792,"総合学習評価"));
                //行数チェック
                $checkdata2 = array("TOTALSTUDYACT"            => array(4,"総合学習活動"),
                                   "TOTALSTUDYVAL"             => array(6,"総合学習評価"));
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey('HTRAINREMARK_HDAT');
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

                    if ($this->getname === 'tokyoto') {
                        $data = array("SCHREGNO"       => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                      "TOTALSTUDYACT"  => $tmp[5],
                                      "TOTALSTUDYVAL"  => $tmp[6],
                                      "TOTALSTUDYACT2" => $tmp[7],
                                      "TOTALSTUDYVAL2" => $tmp[8],
                                      "CREDITREMARK"   => $tmp[9]);
                    } else {
                        $data = array("SCHREGNO"      => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                      "TOTALSTUDYACT" => $tmp[5],
                                      "TOTALSTUDYVAL" => $tmp[6]);
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $checkdata2, $line, $duplicateCsv1, $primaryKey1)) {
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
    public function errCheck($db, $data, $data_each, $data_each2, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $orderErrFlg        = false;
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $orderErrMsg        = "";
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $orderErrSep     = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("SCHREGNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("SCHREGNO"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen(str_replace("\n", "", $data[$key])) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
        }
        //行数チェック
        foreach ($data_each2 as $key => $val) {
            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (substr_count($data[$key], "\n") >= $val[0]) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderErrSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$orderErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx181Query::insertQueryErr($db, $line, $checkError);
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
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        if ($this->getname === 'tokyoto') {
            $header = array("GRADE"                     => "学年",
                            "HR_CLASS"                  => "クラス",
                            "ATTENDNO"                  => "出席番号",
                            "NAME"                      => "氏名",
                            "SCHREGNO"                  => "※学籍番号",
                            "TOTALSTUDYACT"             => "総合学習活動",
                            "TOTALSTUDYVAL"             => "総合学習評価",
                            "TOTALSTUDYACT2"            => "奉仕活動",
                            "TOTALSTUDYVAL2"            => "奉仕評価",
                            "CREDITREMARK"              => "備考",
                            $this->lastColumn           => $this->lastColumn);

            $header_rei = array("GRADE"                     => "99",
                                "HR_CLASS"                  => "999",
                                "ATTENDNO"                  => "999",
                                "NAME"                      => "アルプ太郎",
                                "SCHREGNO"                  => "12345678",
                                "TOTALSTUDYACT"             => "全角176文字",
                                "TOTALSTUDYVAL"             => "全角264文字",
                                "TOTALSTUDYACT2"            => "全角176文字",
                                "TOTALSTUDYVAL2"            => "全角264文字",
                                "CREDITREMARK"              => "全角220文字",
                                $this->lastColumn           => $this->lastColumn);
        } else {
            $header = array("GRADE"                     => "学年",
                            "HR_CLASS"                  => "クラス",
                            "ATTENDNO"                  => "出席番号",
                            "NAME"                      => "氏名",
                            "SCHREGNO"                  => "※学籍番号",
                            "TOTALSTUDYACT"             => "総合学習活動",
                            "TOTALSTUDYVAL"             => "総合学習評価",
                            $this->lastColumn           => $this->lastColumn);

            $header_rei = array("GRADE"                     => "99",
                                "HR_CLASS"                  => "999",
                                "ATTENDNO"                  => "999",
                                "NAME"                      => "アルプ太郎",
                                "SCHREGNO"                  => "12345678",
                                "TOTALSTUDYACT"             => "全角176文字",
                                "TOTALSTUDYVAL"             => "全角264文字",
                                $this->lastColumn           => $this->lastColumn);
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
            $tables = array();
            $tables = array("1" => "HTRAINREMARK_HDAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
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

            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx181Query::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);  //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
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
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                if ($this->field["GRADE_HR_CLASS"]) {
                    $substr_grade = substr($this->field["GRADE_HR_CLASS"], 0, 2);
                    $substr_hr_class = substr($this->field["GRADE_HR_CLASS"], 2, 4);
                    $couse = "指導要録所見ヘッダデータ".ltrim($substr_grade, '0').'-'.str_replace("00", "0", $substr_hr_class);
                } else {
                    $couse = "指導要録所見ヘッダデータ";
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
}