<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx_d132hModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $candidate = array();
    public $schoolkind;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx_d132hModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    public function init()
    {
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),          //学期
                             "OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        if (VARS::get("SCHOOL_KIND") != "") {
            $this->schoolkind = VARS::get("SCHOOL_KIND");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"]) {
            $this->getSelectSchoolKind();
        }

        //委員会・係・部活動の成果
        if ($this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_".$this->schoolkind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_".$this->schoolkind]);
            $this->remark1_01_01_moji = (int)trim($moji);
            $this->remark1_01_01_gyou = (int)trim($gyou);
        } else {
            $this->remark1_01_01_moji = 30;
            if ($this->schoolkind == "J") {
                $this->remark1_01_01_gyou = 7;
            } else {
                $this->remark1_01_01_gyou = 5;
            }
        }

        //資格取得・検定・表彰など
        if ($this->Properties["HREPORTREMARK_DETAIL_DAT_01_02_REMARK1_SIZE_".$this->schoolkind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DETAIL_DAT_01_02_REMARK1_SIZE_".$this->schoolkind]);
            $this->remark1_01_02_moji = (int)trim($moji);
            $this->remark1_01_02_gyou = (int)trim($gyou);
        } else {
            $this->remark1_01_02_moji = 20;
            if ($this->schoolkind == "J") {
                $this->remark1_01_02_gyou = 7;
            } else {
                $this->remark1_01_02_gyou = 5;
            }
        }

        //備考
        if ($this->Properties["HREPORTREMARK_DETAIL_DAT_02_01_REMARK1_SIZE_".$this->schoolkind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DETAIL_DAT_02_01_REMARK1_SIZE_".$this->schoolkind]);
            $this->remark1_02_01_moji = (int)trim($moji);
            $this->remark1_02_01_gyou = (int)trim($gyou);
        } else {
            $this->remark1_02_01_moji = 23;
            $this->remark1_02_01_gyou = 5;
        }

        //通信欄
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$this->schoolkind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$this->schoolkind]);
            $this->communication_moji = (int)trim($moji);
            $this->communication_gyou = (int)trim($gyou);
        } else {
            $this->communication_moji = 30;
            if ($this->schoolkind == "J") {
                $this->communication_gyou = 3;
            } else {
                $this->communication_gyou = 2;
            }
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
                    knjx_d132hQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_d132hQuery::deleteQueryCsv($this, $this->data_arr);
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
            $setVal1 = ($this->remark1_01_01_moji * 3 * $this->remark1_01_01_gyou);
            $setVal2 = ($this->remark1_01_02_moji * 3 * $this->remark1_01_02_gyou);
            $setVal3 = ($this->remark1_02_01_moji * 3 * $this->remark1_02_01_gyou);
            $setVal4 = ($this->communication_moji * 3 * $this->communication_gyou);

            $checkdata = array();
            $checkdata["YEAR"]          = array(4,  "HISSU",  "INT",  "年度");
            $checkdata["SEMESTER"]      = array(4,  "HISSU",  "INT",  "学期");
            $checkdata["SCHREGNO"]      = array(8,  "HISSU",  "INT",  "学籍番号");
            $checkdata["REMARK1_01_01"] = array($setVal1,  "",       "",     "委員会・係・部活動の成果");
            $checkdata["REMARK1_01_02"] = array($setVal2,  "",       "",     "資格取得・検定・表彰など");
            if ($this->schoolkind == "H") {
                $checkdata["REMARK1_02_01"] = array($setVal3,  "",       "",     "備考");
            }
            $checkdata["COMMUNICATION"] = array($setVal4,  "",       "",     "通信欄");

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
            $primaryKey2   = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $primaryKey1   = $this->getPrimaryKey('HREPORTREMARK_DAT');
            $primaryKey2   = $this->getPrimaryKey('HREPORTREMARK_DETAIL_DAT');
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
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    $tmpcnt = 0;
                    $data = array();
                    $data["YEAR"]           = $tmp[4];
                    $data["SEMESTER"]       = $tmp[5];
                    $data["SCHREGNO"]       = $tmp[6];
                    $data["REMARK1_01_01"]  = $tmp[7];
                    $data["REMARK1_01_02"]  = $tmp[8];
                    if ($this->schoolkind == "H") {
                        $data["REMARK1_02_01"]  = $tmp[9];
                        $data["COMMUNICATION"]  = $tmp[10];

                        $dataCnt = 12;
                    } else {
                        $data["COMMUNICATION"]  = $tmp[9];
                        $dataCnt = 11;
                    }
                    $data["DIV"]                = "";
                    $data["CODE"]               = "";

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
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
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $duplicateCsvSep = "";

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
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                }
            }
        }

        if ($this->schoolkind) {
            //存在チェック
            $cnt_check = $db->getOne(knjx_d132hQuery::checkSchoolKind($this, $data));
            if ($cnt_check == 0) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍データがありません。");
            }
        }
        //存在チェック
        $sem_check = $db->getCol(knjx_d132hQuery::checkSemester($data["YEAR"]));
        if (!in_array($data["SEMESTER"], $sem_check)) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された学期データがありません。");
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_d132hQuery::insertQueryErr($db, $line, $checkError);
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
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");
        //ヘッダデータ
        $header = array();
        $header["GRADE"]            = "学年";
        $header["HR_CLASS"]         = "クラス";
        $header["ATTENDNO"]         = "出席番号";
        $header["NAME"]             = "氏名";
        $header["YEAR"]             = "※年度";
        $header["SEMESTER"]         = "※学期";
        $header["SCHREGNO"]         = "※学籍番号";
        $header["REMARK1_01_01"]    = "委員会・係・部活動の成果";
        $header["REMARK1_01_02"]    = "資格取得・検定・表彰など";
        if ($this->schoolkind == "H") {
            $header["REMARK1_02_01"]    = "備考";
        }
        $header["COMMUNICATION"]    = "通信欄";
        $header[$this->lastColumn ] = $this->lastColumn ;

        $header_rei = array();
        $header_rei["GRADE"]            = "99";
        $header_rei["HR_CLASS"]         = "999";
        $header_rei["ATTENDNO"]         = "999";
        $header_rei["NAME"]             = "アルプ太郎";
        $header_rei["YEAR"]             = CTRL_YEAR;
        $header_rei["SEMESTER"]         = CTRL_SEMESTER;
        $header_rei["SCHREGNO"]         = "12345678";
        $header_rei["REMARK1_01_01"]    = "(全角".$this->remark1_01_01_moji."文字X".$this->remark1_01_01_gyou."行まで)";
        $header_rei["REMARK1_01_02"]    = "(全角".$this->remark1_01_02_moji."文字X".$this->remark1_01_02_gyou."行まで)";
        if ($this->schoolkind == "H") {
            $header_rei["REMARK1_02_01"]    = "(全角".$this->remark1_02_01_moji."文字X".$this->remark1_02_01_gyou."行まで)";
        }
        $header_rei["COMMUNICATION"]    = "(全角".$this->communication_moji."文字X".$this->communication_gyou."行まで)";
        $header_rei[$this->lastColumn ] = $this->lastColumn ;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {
            //エラー
            $headerDuplicate = array("YEAR"     => "年度",
                                     "SEMESTER" => "学期",
                                     "SCHREGNO" => "学籍番号",
                                     "DIV"      => "区分",
                                     "CODE"     => "コード");

            $tables = array();
            $tables = array("1" => "HREPORTREMARK_DAT", "2" => "HREPORTREMARK_DETAIL_DAT");
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
                $query = knjx_d132hQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                    $array = $header;
                } else {
                    $array = $header_err;
                }

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
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "通知表所見データ";
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
