<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx191Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $schoolName;
    public $schoolCode;
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $maxPattern;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx191Model()
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
        if ($this->cmd == "") {
            $this->tyousasyo2020 = VARS::request("tyousasyo2020");
        }

        //最初にこのプログラムが呼ばれたとき
        if ($this->cmd == "") {
            $this->program_id = VARS::request("program_id") ? VARS::request("program_id") : PROGRAMID;
            $this->mode       = VARS::request("mode")       ? VARS::request("mode")       : 'ungrd';
        }

        //学校名取得
        $query = knjx191query::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->getFieldSize();

        if ($this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $this->maxPattern = 3;
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
                    knjx191Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx191Query::deleteQueryCsv($this, $this->data_arr);
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
            $checkdata["SCHREGNO"]       = array(array(8,1),"学籍番号");
            $checkdata["COMMENTEX_A_CD"] = array(array(1,1),"概評特A区分");
            if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $checkdata["PATTERN_SEQ"] = array(array(1,1),"パターン");
            }
            if ($this->Properties["tyousasyoSougouHyoukaNentani"] != 1 || "1" == $this->tyousasyo2020) {
                $checkdata["TOTALSTUDYACT"] = array(array((int)$this->totalstudyact_moji, (int)$this->totalstudyact_gyou),"総合学習活動");
                $checkdata["TOTALSTUDYVAL"] = array(array((int)$this->totalstudyval_moji, (int)$this->totalstudyval_gyou),"総合学習評価");
            }
            $checkdata["REMARK"] = array(array((int)$this->remark_moji, (int)$this->remark_gyou),"備考");
            if ($this->Properties["useHexamRemark2Flg"] == 1) {
                $checkdata["REMARK2"] = array(array((int)$this->remark_moji, (int)$this->remark2_gyou),"備考2");
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

            //取得主キー
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey2   = array();
            $duplicateCsv2 = array();
            $primaryKey3   = array();
            $duplicateCsv3 = array();
            if ($this->mode == 'grd') {
                $primaryKey1 = $this->getPrimaryKey('GRD_HEXAM_ENTREMARK_HDAT');
                if ($this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $primaryKey2 = $this->getPrimaryKey('HEXAM_ENTREMARK_SEQ_HDAT');
                } else {
                    $primaryKey2 = $this->getPrimaryKey('HEXAM_ENTREMARK_HDAT');
                }
            } else {
                if ($this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $primaryKey1 = $this->getPrimaryKey('HEXAM_ENTREMARK_SEQ_HDAT');
                } else {
                    $primaryKey1 = $this->getPrimaryKey('HEXAM_ENTREMARK_HDAT');
                }
            }
            if ("1" == $this->tyousasyo2020) {
                if ($primaryKey2 != "") {
                    $primaryKey3 = $this->getPrimaryKey('HEXAM_ENTREMARK_REMARK_HDAT');
                } else {
                    $primaryKey2 = $this->getPrimaryKey('HEXAM_ENTREMARK_REMARK_HDAT');
                }
            }
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

                    if ($this->Properties["tyousasyoSougouHyoukaNentani"] != 1 || "1" == $this->tyousasyo2020) {
                        if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == 1) {
                            $data = array("SCHREGNO"       => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                          "COMMENTEX_A_CD" => $tmp[5],
                                          "PATTERN_SEQ"    => $tmp[6],
                                          "TOTALSTUDYACT"  => $tmp[7],
                                          "TOTALSTUDYVAL"  => $tmp[8],
                                          "REMARK"         => $tmp[9]);
                            $maxCount = 11;
                        } else {
                            $data = array("SCHREGNO"       => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                          "COMMENTEX_A_CD" => $tmp[5],
                                          "TOTALSTUDYACT"  => $tmp[6],
                                          "TOTALSTUDYVAL"  => $tmp[7],
                                          "REMARK"         => $tmp[8]);
                            $maxCount = 10;
                        }
                    } else {
                        $data = array("SCHREGNO"       => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                      "COMMENTEX_A_CD" => $tmp[5],
                                      "REMARK"         => $tmp[6]);
                        $maxCount = 8;
                    }
                    if ($this->Properties["useHexamRemark2Flg"] == 1) {
                        if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == 1) {
                            $data["REMARK2"] = $tmp[10];
                        } else {
                            $data["REMARK2"] = $tmp[9];
                        }
                        $maxCount += 1;
                    }
                    $checkCntFlg = get_count($tmp) == $maxCount ? true : false;

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $checkCntFlg, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$line][$val] = $data[$val];
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
    public function errCheck($db, $data, $data_each, $line, $checkCntFlg, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $colCntErrFlg       = false;    //フィールド数エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $colCntErrMsg       = "";       //フィールド数エラー
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ

        $hissuSep        = "";
        $existenceSep    = "";
        $numericSep      = "";
        $lengthSep       = "";
        $colCntSep       = "";
        $duplicateCsvSep = "";
        $masterSep       = "";

        if (!$checkCntFlg) {
            $returnFlg = $this->errSet($colCntErrFlg, $colCntErrMsg, "※項目数エラー:", $colCntSep, "ヘッダ出力をしてみて下さい。");
        }

        //重複(CSV)チェック
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

            if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == 1) {
                //必須項目チェック
                if (in_array($key, array("PATTERN_SEQ"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //数値チェック
                if (in_array($key, array("PATTERN_SEQ"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //行数チェック
                if ($this->validate_row_cnt($data[$key], ($val[0][0] * 2)) > $val[0][1]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }

            //存在チェック
            if (in_array($key, array("SCHREGNO"))) {
                $query = knjx191Query::countSchregNo($data[$key]);
                $cnt = $db->getOne($query);
                if ($cnt < 1) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された学籍番号がありません。");
                }
            }

            if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == 1) {
                //パターン上限チェック
                if (in_array($key, array("PATTERN_SEQ"))) {
                    if ($data[$key] != "" && $data[$key] > $this->maxPattern) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※パターン上限:", $existenceSep, $val[1]);
                    }
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$colCntErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx191Query::insertQueryErr($db, $line, $checkError);
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

        $header["GRADE"]          = "学年";
        $header["HR_CLASS"]       = "クラス";
        $header["ATTENDNO"]       = "出席番号";
        $header["NAME"]           = "氏名";
        $header["SCHREGNO"]       = "※学籍番号";
        $header["COMMENTEX_A_CD"] = "概評特A区分";
        if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $header["PATTERN_SEQ"]    = "※パターン";
        }
        if ($this->Properties["tyousasyoSougouHyoukaNentani"] != 1 || "1" == $this->tyousasyo2020) {
            $header["TOTALSTUDYACT"]  = "総合学習活動";
            $header["TOTALSTUDYVAL"]  = "総合学習評価";
        }
        $header["REMARK"]         = "備考";
        if ($this->Properties["useHexamRemark2Flg"] == 1) {
            $header["REMARK2"]    = "備考2";
        }
        $header[$this->lastColumn]    = $this->lastColumn;

        $header_rei["GRADE"]          = "99";
        $header_rei["HR_CLASS"]       = "999";
        $header_rei["ATTENDNO"]       = "999";
        $header_rei["NAME"]           = "アルプ太郎";
        $header_rei["SCHREGNO"]       = "12345678";
        $header_rei["COMMENTEX_A_CD"] = "9";
        if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $header_rei["PATTERN_SEQ"]    = "1";
        }
        if ($this->Properties["tyousasyoSougouHyoukaNentani"] != 1 || "1" == $this->tyousasyo2020) {
            $setsu = ($this->totalstudyact_moji * $this->totalstudyact_gyou);
            $header_rei["TOTALSTUDYACT"]  = "全角".$setsu."文字";
            $setsu = ($this->totalstudyval_moji * $this->totalstudyval_gyou);
            $header_rei["TOTALSTUDYVAL"]  = "全角".$setsu."文字";
        }
        $setsu = ($this->remark_moji * $this->remark_gyou);
        $header_rei["REMARK"]          = "全角".$setsu."文字";
        if ($this->Properties["useHexamRemark2Flg"] == 1) {
            $setsu = ($this->remark_moji * $this->remark2_gyou);
            $header_rei["REMARK2"]     = "全角".$setsu."文字";
        }
        $header_rei[$this->lastColumn] = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode(",", $header)."\n";
            $contents .= implode(",", $header_rei)."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $tables = array();
            $table2 = array();
            $table3 = array();
            if ($this->mode == 'grd') {
                $tables = array("1" => "GRD_HEXAM_ENTREMARK_HDAT");
                if ($this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $table2 = array("2" => "HEXAM_ENTREMARK_SEQ_HDAT");
                    $tables = array_merge($tables, $table2);
                } else {
                    $table2 = array("2" => "HEXAM_ENTREMARK_HDAT");
                    $tables = array_merge($tables, $table2);
                }
            } else {
                if ($this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $tables = array("1" => "HEXAM_ENTREMARK_SEQ_HDAT");
                } else {
                    $tables = array("1" => "HEXAM_ENTREMARK_HDAT");
                }
            }
            if ("1" == $this->tyousasyo2020) {
                if ($table2 != "") {
                    $table3 = array("3" => "HEXAM_ENTREMARK_REMARK_HDAT");
                    $tables = array_merge($tables, $table3);
                } else {
                    $table2 = array("2" => "HEXAM_ENTREMARK_REMARK_HDAT");
                    $tables = array_merge($tables, $table2);
                }
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode(",", $header_err)."\n";
        } elseif ($this->field["OUTPUT"]=="4") {
            //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= implode(",", $header)."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            $rows = array();
            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
                //SQL文発行
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $rows[] = $row;
                }
                $result->free();
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $rows = knjx191Query::selectMainQuery($db, $this);
            }
            Query::dbCheckIn($db);

            foreach ($rows as $row) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);  //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                }
                $contents .= implode(",", $row) ."\n";
                $header_data = true;
            }
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                if ($this->mode == 'grd') {
                    $couse = "調査書進学用所見ヘッダデータ(卒業)";
                } else {
                    $couse = "調査書進学用所見ヘッダデータ";
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

    public function getFieldSize()
    {
        //活動内容の文字数と行数の設定
        if ("1" == $this->tyousasyo2020) {
            $this->totalstudyact_moji = 48;
            $this->totalstudyact_gyou = $this->Properties["tyousasyo2020shojikouExtends"] > 0 && $this->Properties["tyousasyo2020TotalstudyactGyou"] ? $this->Properties["tyousasyo2020TotalstudyactGyou"] : 8;
        } elseif ($this->Properties["tyousasyoTotalstudyactFieldSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["tyousasyoTotalstudyactFieldSize"]);
            $this->totalstudyact_moji = (int)trim($moji);
            $this->totalstudyact_gyou = (int)trim($gyou);
        } else {
            if ($this->Properties["tyousasyoSougouHyoukaNentani"] == 1) {
                $this->totalstudyact_moji = 19; //デフォルトの値
                $this->totalstudyact_gyou = 5; //デフォルトの値
            } else {
                $this->totalstudyact_moji = 41; //デフォルトの値
                $this->totalstudyact_gyou = 2; //デフォルトの値
            }
        }
        //評価の文字数と行数の設定
        if ("1" == $this->tyousasyo2020) {
            $this->totalstudyval_moji = 48;
            $this->totalstudyval_gyou = $this->Properties["tyousasyo2020shojikouExtends"] > 0 && $this->Properties["tyousasyo2020TotalstudyvalGyou"] ? $this->Properties["tyousasyo2020TotalstudyvalGyou"] : 8;
        } elseif ($this->Properties["tyousasyoTotalstudyvalFieldSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["tyousasyoTotalstudyvalFieldSize"]);
            $this->totalstudyval_moji = (int)trim($moji);
            $this->totalstudyval_gyou = (int)trim($gyou);
        } else {
            if ($this->Properties["tyousasyoSougouHyoukaNentani"] == 1) {
                $this->totalstudyval_moji = 19; //デフォルトの値
                $this->totalstudyval_gyou = 6; //デフォルトの値
            } else {
                $this->totalstudyval_moji = 41; //デフォルトの値
                $this->totalstudyval_gyou = 3; //デフォルトの値
            }
        }
        //備考の文字数と行数の設定
        if ("1" == $this->tyousasyo2020) {
            $this->remark_moji = 45;
            $this->remark_gyou = 50;
        } elseif ($this->Properties["tyousasyoRemarkFieldSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["tyousasyoRemarkFieldSize"]);
            $this->remark_moji = (int)trim($moji);
            $this->remark_gyou = (int)trim($gyou);
        } else {
            $this->remark_moji = 41; //デフォルトの値
            $this->remark_gyou = 5;  //デフォルトの値
        }
        $this->remark2_gyou = 3;  //3行固定
    }
}
