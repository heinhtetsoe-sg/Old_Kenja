<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx_a125mModel extends Model
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

    public $exp_year;
    public $exp_semester;
    public $schregno;
    public $prgid;
    public $getAuth;       //権限
    public $flg;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx_a125mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->auth);
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->prgid);
            unset($this->getAuth);
            unset($this->flg);
        }

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        if (VARS::get("EXP_SEMESTER") != "") {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("PRGID") != "") {
            $this->prgid = VARS::get("PRGID");
        }
        if (VARS::get("AUTH") != "") {
            $this->getAuth = VARS::get("AUTH");
        }

        //権限
        $this->auth = ($this->getAuth != "") ? $this->getAuth : AUTHORITY;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd          = VARS::request("cmd");
        $this->file = VARS::file("FILE");
        if (VARS::get("FIELDSIZE")) {
            $this->fieldSize = array();
            $fieldget = preg_split("{,}", VARS::get("FIELDSIZE"));
            for ($i = 0; $i < get_count($fieldget); $i++) {
                $fieldArray = preg_split("{=}", $fieldget[$i]);
                if ($fieldArray[1] > 0) {
                    $this->fieldSize[$fieldArray[0]] = $fieldArray[1];
                }
            }
        }
        if (VARS::get("GYOUSIZE")) {
            $this->gyouSize = array();
            $gyouget = preg_split("{,}", VARS::get("GYOUSIZE"));
            for ($i = 0; $i < get_count($gyouget); $i++) {
                $gyouArray = preg_split("{=}", $gyouget[$i]);
                if ($gyouArray[1] > 0) {
                    $this->gyouSize[$gyouArray[0]] = $gyouArray[1];
                }
            }
        }

        //ヘッダ候補
        $this->candidate = array("TOTALSTUDYACT"    => "総合学習活動",
                                 "TOTALSTUDYVAL"    => "総合学習評価",
                                 "SPECIALJISUU"     => "特別活動出席時数",
                                 "SPECIALACTREMARK" => "特別活動所見",
                                 "TOTALREMARK"      => "総合所見",
                                 "ATTENDREC_REMARK" => "出欠の記録備考",
                                 "NARA_TIME"        => "奈良Time",
                                );

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
                    knjx_a125mQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_a125mQuery::deleteQueryCsv($this, $this->data_arr);
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
                               "ANNUAL"           => array(2,"年次"));
            //対象項目のみデータに追加する。
            foreach ($this->fieldSize as $key => $val) {
                $checkdata[$key] = array($this->fieldSize[$key],$this->candidate[$key]);
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
            $primaryKey2 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $primaryKey1 = $this->getPrimaryKey('HTRAINREMARK_DAT');
            $primaryKey2 = $this->getPrimaryKey('HTRAINREMARK_DETAIL2_DAT');
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

                    $data = array("YEAR"             => (strlen(trim($tmp[4]))) ? sprintf("%04d", $tmp[4]) : "",
                                  "SCHREGNO"         => (strlen(trim($tmp[5]))) ? sprintf("%08d", $tmp[5]) : "",
                                  "ANNUAL"           => (strlen(trim($tmp[6]))) ? sprintf("%02d", $tmp[6]) : "");
                    $tmpcnt = 7;
                    //対象項目のみデータに追加する。
                    foreach ($this->fieldSize as $key => $val) {
                        $data[$key] = $tmp[$tmpcnt];
                        $tmpcnt++;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $checkdata2, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
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
    public function errCheck($db, $data, $data_each, $data_each2, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrErrFlg    = false;    //マスタエラーフラグ
        $valuesErrFlg       = false;    //値エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ
        $orderErrFlg        = false;    //行数エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $valuesErrMsg       = "";       //値エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ
        $orderErrMsg        = "";       //行数エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $valuesSep       = "";
        $duplicateCsvSep = "";
        $orderSep        = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR","SCHREGNO","ANNUAL"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR","SCHREGNO","ANNUAL","SPECIALJISUU"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
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

        if ($this->flg == "sign") {
            //年度チェック
            if ($this->exp_year != $data["YEAR"]) {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, "年度不一致");
            }

            //年次チェック
            $annual = $db->getOne(knjx_a125mQuery::getAnnual($this, $data["SCHREGNO"]));
            if ($annual != $data["ANNUAL"]) {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, "年次不一致");
            }

            //存在チェック
            $RegdData = $db->getCol(knjx_a125mQuery::checkSchregRegdDat($this));
            if (!in_array($data["SCHREGNO"], $RegdData)) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "年組所属なし");
            }

            //署名チェック
            $chage_opi_seq = $db->getOne(knjx_a125mQuery::getOpinionsWk($this->exp_year, $data["SCHREGNO"]));
            if ($chage_opi_seq) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "校長署名済み");
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg.$valuesErrMsg.$orderErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_a125mQuery::insertQueryErr($db, $line, $checkError);
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
        //ヘッダデータ
        $header = array("GRADE"            => "学年",
                        "HR_CLASS"         => "クラス",
                        "ATTENDNO"         => "出席番号",
                        "NAME"             => "氏名",
                        "YEAR"             => "※年度",
                        "SCHREGNO"         => "※学籍番号",
                        "ANNUAL"           => "※年次");
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
        $header_rei = array("GRADE"            => "99",
                            "HR_CLASS"         => "999",
                            "ATTENDNO"         => "999",
                            "NAME"             => "アルプ太郎",
                            "YEAR"             => "9999",
                            "SCHREGNO"         => "12345678",
                            "ANNUAL"           => "99");

        //対象項目のみヘッダデータに追加する。
        foreach ($this->fieldSize as $key => $val) {
            $header_rei[$key] = "全角".$mojisu[$key]."文字";
        }
        $header_rei[$this->lastColumn] = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {
            //エラー
            $headerDuplicate = array("YEAR"                  => "年度",
                                     "SCHREGNO"              => "学籍番号",
                                     "HTRAIN_SEQ"            => "SEQ");
            $tables = array();
            $tables = array("1" => "HTRAINREMARK_DAT", "2" => "HTRAINREMARK_DETAIL2_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
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
                $query = knjx_a125mQuery::selectMainQuery($this);
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
                $couse = "指導要録所見データ（通信制）";
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
