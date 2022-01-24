<?php

/**
 * 定数定義
*/
class knjx442bModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $fieldSize = array();
    public $gyouSize  = array();
    public $candidate = array();
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $schoolNm;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx442bModel()
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

        $this->cmd  = VARS::request("cmd");
        $this->file = VARS::file("FILE");
        if (VARS::get("FIELDSIZE")) {
            $this->fieldSize = array();
            $fieldget = explode(",", VARS::get("FIELDSIZE"));
            for ($i = 0; $i < count($fieldget); $i++) {
                $fieldArray = explode("=", $fieldget[$i]);
                if ($fieldArray[1] > 0) {
                    $this->fieldSize[$fieldArray[0]] = $fieldArray[1];
                }
            }
        }
        if (VARS::get("GYOUSIZE")) {
            $this->gyouSize = array();
            $gyouget = explode(",", VARS::get("GYOUSIZE"));
            for ($i = 0; $i < count($gyouget); $i++) {
                $gyouArray = explode("=", $gyouget[$i]);
                if ($gyouArray[1] > 0) {
                    $this->gyouSize[$gyouArray[0]] = $gyouArray[1];
                }
            }
        }

        //学校名取得
        $db = Query::dbCheckOut();
        $this->schoolNm = $db->getOne(knjx442bQuery::getSchoolNm());
        Query::dbCheckIn($db);

        //ヘッダ候補
        $this->candidate = array();
        $this->candidate["GRADE1_REMARK"]     = "行動記録 １年";
        $this->candidate["GRADE2_REMARK"]     = "行動記録 ２年";
        $this->candidate["GRADE3_REMARK"]     = "行動記録 ３年";
        $this->candidate["CONDITION"]         = "条件";
        $this->candidate["HOPE_COLLEGE_NAME1"] = "受験希望 指定校推薦 大学・学部・学科";
        $this->candidate["HOPE_COURSE_NAME1"]  = "受験希望 指定校推薦 コース";
        $this->candidate["HOPE_COLLEGE_NAME2"] = "受験希望 日大付属選抜 大学・学部・学科";
        $this->candidate["HOPE_COURSE_NAME2"]  = "受験希望 日大付属選抜 コース";

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
                    knjx442bQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx442bQuery::deleteQueryCsv($this, $this->data_arr);
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
            $checkdata = array("YEAR"             => array(4,"年度"),
                               "SCHREGNO"         => array(8,"学籍番号"),
                            );
            $checkdata2 = array();
            //対象項目のみデータに追加する。
            foreach ($this->fieldSize as $key => $val) {
                $checkdata[$key] = array($this->fieldSize[$key], $this->candidate[$key]);
            }
            //対象項目のみデータに追加する。(行数チェック)
            foreach ($this->gyouSize as $key => $val) {
                $checkdata2[$key] = array($this->gyouSize[$key], $this->candidate[$key]);
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
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('MOCK_TRAINREMARK_DAT');
            while (!feof($fp)) {
                $tmp = fgetcsv($fp, 4096);

                if ($tmp) {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }

                    //文字コードを判別、改行コードを削除
                    $buffer = implode(",", $tmp);
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $buffer = str_replace("\"", "", $buffer);
                    $buffer = str_replace("↓", "\n", $buffer);
                    $tmp = explode(",", $buffer);

                    //配列の要素数チェック
                    $checkcount = count($tmp);

                    $data = array("YEAR"             => (strlen(trim($tmp[4]))) ? sprintf("%04d", $tmp[4]) : "",
                                  "SCHREGNO"         => (strlen(trim($tmp[5]))) ? sprintf("%08d", $tmp[5]) : "",
                                );

                    //項目数 (更新対象外項目数 + 更新対象項目数 + LASTCOLUMN)
                    $tmpcnt = 6;
                    $dataCnt = $tmpcnt + count($this->candidate) + 1;

                    //対象項目のみデータに追加する。
                    foreach ($this->fieldSize as $key => $val) {
                        $data[$key] = $tmp[$tmpcnt];
                        $tmpcnt++;
                    }

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
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
        $masterErrFlg       = false;    //マスタエラーフラグ
        $orderErrFlg        = false;
        $duplicateCsvErrFlg = false;    //重複エラーフラグ
        $valuesErrFlg       = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $orderErrMsg        = "";
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ
        $valuesErrMsg       = "";

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $orderSep        = "";
        $duplicateCsvSep = "";
        $valuesSep       = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR", "SCHREGNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR", "SCHREGNO"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //年度
            if ($key == "YEAR" && CTRL_YEAR != $data[$key]) {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, "対象年度と異なる");
            }

            //学籍番号
            if ($key == "SCHREGNO" && $data[$key] != "") {
                $query = knjx442bQuery::checkSchregNo($this, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された学籍番号がありません。");
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                if (in_array($key, array("GRADE1_REMARK", "GRADE2_REMARK", "GRADE3_REMARK", "CONDITION"))) {
                    //文字数チェック
                    if ((strlen(str_replace("\n", "", $data[$key])) + substr_count($data[$key], "\n")) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                } else {
                    //文字数チェック
                    if (strlen(str_replace("\n", "", $data[$key])) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            }
        }
        //行数チェック
        foreach ($data_each2 as $key => $val) {
            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (substr_count($data[$key], "\n") >= $val[0]) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg.$orderErrMsg.$valuesErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx442bQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".count($data_arr)."件");
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
        //ヘッダデータ
        $header = array("GRADE"            => "学年",
                        "HR_CLASS"         => "クラス",
                        "ATTENDNO"         => "出席番号",
                        "NAME"             => "氏名",
                        "YEAR"             => "※年度",
                        "SCHREGNO"         => "※学籍番号",
                    );

        //対象項目のみヘッダデータに追加する。
        foreach ($this->fieldSize as $key => $val) {
            $header[$key] = $this->candidate[$key];
        }
        $header[$this->lastColumn] = $this->lastColumn;

        $mojisu = array();
        foreach ($this->fieldSize as $key => $val) {
            $data = ($val / 3);
            if (mb_strpos($data, ".") > 0) {
                $mojisu[$key] = substr($data, 0, mb_strpos($data, "."));
            } else {
                $mojisu[$key] = $data;
            }
        }
        $header_rei2 = array("GRADE"            => "99",
                            "HR_CLASS"         => "999",
                            "ATTENDNO"         => "999",
                            "NAME"             => "アルプ太郎",
                            "YEAR"             => "9999",
                            "SCHREGNO"         => "12345678",
                        );

        //対象項目のみヘッダデータに追加する。
        foreach ($header as $key => $val) {
            if (isset($header_rei2[$key])) {
                $header_rei[$key] = $header_rei2[$key];
            } elseif (isset($mojisu[$key])) {
                $header_rei[$key] = "全角".$mojisu[$key]."文字";
            }
        }
        $header_rei[$this->lastColumn] = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode(",", $header)."\n";
            $contents .= implode(",", $header_rei)."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $tables = array();
            $tables = array("1" => "MOCK_TRAINREMARK_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode(",", $header_err)."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode(",", $header)."\n";
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
                $query = knjx442bQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {                //データ
                    $row = str_replace(",", ".", $row);  //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                }
                $contents .= implode(",", $row) ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                if ($this->field["GRADE_HR_CLASS"]) {
                    $substr_grade = substr($this->field["GRADE_HR_CLASS"], 0, 2);
                    $substr_hr_class = substr($this->field["GRADE_HR_CLASS"], 2, 4);
                    $couse = "指定校推薦・日大特別選抜成績状況所見データ".ltrim($substr_grade, '0').'-'.str_replace("00", "0", $substr_hr_class);
                } else {
                    $couse = "指定校推薦・日大特別選抜成績状況所見データ";
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
