<?php

require_once('for_php7.php');
class knjx190Model extends Model
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
    public function knjx190Model()
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
        if ($this->cmd == "") {
            $this->tyousasyo2020 = VARS::request("tyousasyo2020");
        }

        //最初にこのプログラムが呼ばれたとき
        if ($this->cmd == "") {
            $this->program_id = VARS::request("program_id") ? VARS::request("program_id") : PROGRAMID;
            $this->mode       = VARS::request("mode")       ? VARS::request("mode")       : 'ungrd';
        }

        //学校名取得
        $query = knjx190query::getSchoolName();
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
                    knjx190Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx190Query::deleteQueryCsv($this, $this->data_arr);
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
            $checkdata["YEAR"]             = array(array(4,1),"年度");
            $checkdata["SCHREGNO"]         = array(array(8,1),"学籍番号");
            $checkdata["AN"]               = array(array(2,1),"年次");
            if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $checkdata["PATTERN_SEQ"]               = array(array(1,1),"パターン");
            }
            $checkdata["ATTENDREC_REMARK"] = array(array((int)$this->attendrec_remark_moji, (int)$this->attendrec_remark_gyou),"出欠記録");
            $checkdata["SPECIALACTREC"]    = array(array((int)$this->specialactrec_moji,    (int)$this->specialactrec_gyou),"特別活動記録");

            if ("1" == $this->tyousasyo2020) {
                $checkdata["TRAIN_REF1"] = array(array($this->moji["TRAIN_REF1"], $this->gyou["TRAIN_REF1"]), "(1)学習における特徴等");
                $checkdata["TRAIN_REF2"] = array(array($this->moji["TRAIN_REF2"], $this->gyou["TRAIN_REF2"]), "(2)行動の特徴、特技等");
                $checkdata["TRAIN_REF3"] = array(array($this->moji["TRAIN_REF3"], $this->gyou["TRAIN_REF3"]), "(3)部活動、ボランティア活動 留学・海外経験等");
                $checkdata["TRAIN_REF4"] = array(array($this->moji["TRAIN_REF4"], $this->gyou["TRAIN_REF4"]), "(4)取得資格、検定等");
                $checkdata["TRAIN_REF5"] = array(array($this->moji["TRAIN_REF5"], $this->gyou["TRAIN_REF5"]), "(5)表彰・顕彰等の記録");
                $checkdata["TRAIN_REF6"] = array(array($this->moji["TRAIN_REF6"], $this->gyou["TRAIN_REF6"]), "(6)その他");
            } else {
                if ($this->Properties["tyousasyoTokuBetuFieldSize"] == 1) {
                    $checkdata["TRAIN_REF"] = array(array(58,7), "指導上参考");
                } else {
                    $checkdata["TRAIN_REF"] = array(array(41,5), "指導上参考");
                }
                $checkdata["TRAIN_REF1"] = array(array($this->train_ref1_moji,$this->train_ref1_gyou), "学習／行動の特技・特徴等");
                $checkdata["TRAIN_REF2"] = array(array($this->train_ref2_moji,$this->train_ref2_gyou), "部活動・資格取得等");
                $checkdata["TRAIN_REF3"] = array(array($this->train_ref3_moji,$this->train_ref3_gyou), "その他");
            }

            if ($this->Properties["tyousasyoSougouHyoukaNentani"] == 1 && "1" != $this->tyousasyo2020) {
                $checkdata["TOTALSTUDYACT"] = array(array((int)$this->totalstudyact_moji, (int)$this->totalstudyact_gyou), "活動内容");
                $checkdata["TOTALSTUDYVAL"] = array(array((int)$this->totalstudyval_moji, (int)$this->totalstudyval_gyou), "評価");
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
                $primaryKey1 = $this->getPrimaryKey('GRD_HEXAM_ENTREMARK_DAT');
                if ($this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $primaryKey2 = $this->getPrimaryKey('HEXAM_ENTREMARK_SEQ_DAT');
                } else {
                    $primaryKey2 = $this->getPrimaryKey('HEXAM_ENTREMARK_DAT');
                }
            } else {
                if ($this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $primaryKey1 = $this->getPrimaryKey('HEXAM_ENTREMARK_SEQ_DAT');
                } else {
                    $primaryKey1 = $this->getPrimaryKey('HEXAM_ENTREMARK_DAT');
                }
            }
            if ("1" == $this->tyousasyo2020) {
                if ($this->mode != 'grd') {
                    if ($this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                        $primaryKey2 = $this->getPrimaryKey('HEXAM_ENTREMARK_TRAINREF_SEQ_DAT');
                    } else {
                        $primaryKey2 = $this->getPrimaryKey('HEXAM_ENTREMARK_TRAINREF_DAT');
                    }
                } else {
                    $primaryKey3 = $this->getPrimaryKey('HEXAM_ENTREMARK_TRAINREF_DAT');
                }
            }
            while (!feof($fp)) {
                $b = fgets($fp, 4096);
                $buffer = $b;
                while (strlen($b) == (4096 - 1) && trim($b, "\n\r") == $b) {
                    $b = fgets($fp, 4096);
                    $buffer .= $b;
                }
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer      = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp         = common::csv2array($buffer);
                    $tmp         = str_replace("↓", "\n", $tmp);
                    $year        = (strlen(trim($tmp[4]))) ? sprintf("%04d", $tmp[4]) : "";
                    $schregno    = (strlen(trim($tmp[5]))) ? sprintf("%08d", $tmp[5]) : "";
                    $annualCheck = $db->getOne(knjx190Query::getAnnual($schregno, $year));
                    $annual      = ($annualCheck) ? $annualCheck : "00";

                    if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                        $data = array("YEAR"             => $year,
                                      "SCHREGNO"         => $schregno,
                                      "ANNUAL"           => $annual,
                                      "PATTERN_SEQ"      => $tmp[7],
                                      "ATTENDREC_REMARK" => $tmp[8],
                                      "SPECIALACTREC"    => $tmp[9]
                        );
                    } else {
                        $data = array("YEAR"             => $year,
                                      "SCHREGNO"         => $schregno,
                                      "ANNUAL"           => $annual,
                                      "ATTENDREC_REMARK" => $tmp[7],
                                      "SPECIALACTREC"    => $tmp[8]
                        );
                    }

                    if ("1" == $this->tyousasyo2020) {
                        if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                            $data["TRAIN_REF1"] = $tmp[10];
                            $data["TRAIN_REF2"] = $tmp[11];
                            $data["TRAIN_REF3"] = $tmp[12];
                            $data["TRAIN_REF4"] = $tmp[13];
                            $data["TRAIN_REF5"] = $tmp[14];
                            $data["TRAIN_REF6"] = $tmp[15];
                        } else {
                            $data["TRAIN_REF1"] = $tmp[9];
                            $data["TRAIN_REF2"] = $tmp[10];
                            $data["TRAIN_REF3"] = $tmp[11];
                            $data["TRAIN_REF4"] = $tmp[12];
                            $data["TRAIN_REF5"] = $tmp[13];
                            $data["TRAIN_REF6"] = $tmp[14];
                        }
                    } else {
                        $data["TRAIN_REF"] = $tmp[9];
                        $data["TRAIN_REF1"] = $tmp[10];
                        $data["TRAIN_REF2"] = $tmp[11];
                        $data["TRAIN_REF3"] = $tmp[12];
                    }

                    if ($this->Properties["tyousasyoSougouHyoukaNentani"] == 1 && "1" != $this->tyousasyo2020) {
                        $data["TOTALSTUDYACT"] = $tmp[13];
                        $data["TOTALSTUDYVAL"] = $tmp[14];
                    }

                    $countMax = 14;
                    if ($this->Properties["tyousasyoSougouHyoukaNentani"] == 1 && "1" != $this->tyousasyo2020) {
                        $countMax += 2;
                    }
                    if ($this->tyousasyo2020 == "1") {
                        $countMax += 2;
                    }
                    if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                        $countMax += 1;
                    }
                    $checkCntFlg = get_count($tmp) == $countMax ? true : false;
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
        $orderErrFlg        = false;    //スペースにつづいて長音（ー）フラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $colCntErrFlg       = false;    //フィールド数エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $orderErrMsg        = "";       //スペースにつづいて長音（ー）メッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $colCntErrMsg       = "";       //フィールド数エラー
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ

        $hissuSep        = "";
        $existenceSep    = "";
        $numericSep      = "";
        $lengthSep       = "";
        $orderErrSep     = "";
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
            if (in_array($key, array("YEAR", "SCHREGNO", "ANNUAL"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR", "SCHREGNO", "ANNUAL"))) {
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
                if ($this->validateKinsoku($data[$key])) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderErrSep, $val[1]);
                }
            }

            //存在チェック
            if (in_array($key, array("YEAR", "SCHREGNO"))) {
                if ($key == "YEAR") {
                    $query = knjx190Query::checkGrade($data["SCHREGNO"], $data["YEAR"]);
                } else {
                    $query = knjx190Query::countSchregNo($data[$key]);
                }
                $cnt = $db->getOne($query);
                if ($cnt < 1) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
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
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$colCntErrMsg.$orderErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx190Query::insertQueryErr($db, $line, $checkError);
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

    public function validateKinsoku($str)
    {
        if ($this->Properties["tyousasyoKinsokuForm"] == '1') {
            if (false !== strpos($str, '　ー') || false !== strpos($str, ' ー')) {
                return true;
            }
        }
        return false;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        /************/
        /* ヘッダー */
        /************/
        $header = array();

        $header["GRADE"]            = "学年";
        $header["HR_CLASS"]         = "クラス";
        $header["ATTENDNO"]         = "出席番号";
        $header["NAME"]             = "氏名";
        $header["YEAR"]             = "※年度";
        $header["SCHREGNO"]         = "※学籍番号";
        $header["ANNUAL"]           = "※年次";
        if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $header["PATTERN_SEQ"]      = "※パターン";
        }
        $header["ATTENDREC_REMARK"] = "出欠記録備考";
        $header["SPECIALACTREC"]    = "特別活動記録";

        if ("1" == $this->tyousasyo2020) {
            $header["TRAIN_REF1"]       = "(1)学習における特徴等";
            $header["TRAIN_REF2"]       = "(2)行動の特徴、特技等";
            $header["TRAIN_REF3"]       = "(3)部活動、ボランティア活動 留学・海外経験等";
            $header["TRAIN_REF4"]       = "(4)取得資格、検定等";
            $header["TRAIN_REF5"]       = "(5)表彰・顕彰等の記録";
            $header["TRAIN_REF6"]       = "(6)その他";
        } else {
            $header["TRAIN_REF"]        = "指導上参考";
            $header["TRAIN_REF1"]       = "学習／行動の特技・特徴等";
            $header["TRAIN_REF2"]       = "部活動・資格取得等";
            $header["TRAIN_REF3"]       = "その他";
        }

        if ($this->Properties["tyousasyoSougouHyoukaNentani"] == 1 && "1" != $this->tyousasyo2020) {
            $header["TOTALSTUDYACT"]    = "活動内容";
            $header["TOTALSTUDYVAL"]    = "評価";
        }
        $header[$this->lastColumn]           = $this->lastColumn;

        /**************/
        /* ヘッダー例 */
        /**************/
        $header_rei = array();

        $header_rei["GRADE"]            = "99";
        $header_rei["HR_CLASS"]         = "999";
        $header_rei["ATTENDNO"]         = "999";
        $header_rei["NAME"]             = "アルプ太郎";
        $header_rei["YEAR"]             = "9999";
        $header_rei["SCHREGNO"]         = "12345678";
        $header_rei["ANNUAL"]           = "99";
        if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $header_rei["PATTERN_SEQ"]      = "1";
        }
        $setsu = ($this->attendrec_remark_moji * $this->attendrec_remark_gyou);
        $header_rei["ATTENDREC_REMARK"] = "全角".$setsu."文字";

        $specialactrec_moji_su = $this->specialactrec_moji * $this->specialactrec_gyou;
        $header_rei["SPECIALACTREC"]    = "全角{$specialactrec_moji_su}文字";

        if ("1" == $this->tyousasyo2020) {
            $header_rei["TRAIN_REF1"]       = "全角".($this->moji["TRAIN_REF1"] * $this->gyou["TRAIN_REF1"])."文字";
            $header_rei["TRAIN_REF2"]       = "全角".($this->moji["TRAIN_REF2"] * $this->gyou["TRAIN_REF2"])."文字";
            $header_rei["TRAIN_REF3"]       = "全角".($this->moji["TRAIN_REF3"] * $this->gyou["TRAIN_REF3"])."文字";
            $header_rei["TRAIN_REF4"]       = "全角".($this->moji["TRAIN_REF4"] * $this->gyou["TRAIN_REF4"])."文字";
            $header_rei["TRAIN_REF5"]       = "全角".($this->moji["TRAIN_REF5"] * $this->gyou["TRAIN_REF5"])."文字";
            $header_rei["TRAIN_REF6"]       = "全角".($this->moji["TRAIN_REF6"] * $this->gyou["TRAIN_REF6"])."文字";
        } else {
            if ($this->Properties["tyousasyoTokuBetuFieldSize"] == 1) {
                $header_rei["TRAIN_REF"]    = "全角406文字";
            } else {
                $header_rei["TRAIN_REF"]    = "全角205文字";
            }
            $mojisu1 = $this->train_ref1_moji * $this->train_ref1_gyou;
            $mojisu2 = $this->train_ref2_moji * $this->train_ref2_gyou;
            $mojisu3 = $this->train_ref3_moji * $this->train_ref3_gyou;
            $header_rei["TRAIN_REF1"]       = "全角{$mojisu1}文字";
            $header_rei["TRAIN_REF2"]       = "全角{$mojisu2}文字";
            $header_rei["TRAIN_REF3"]       = "全角{$mojisu3}文字";
        }
        if ($this->Properties["tyousasyoSougouHyoukaNentani"] == 1 && "1" != $this->tyousasyo2020) {
            $setsu = ($this->totalstudyact_moji * $this->totalstudyact_gyou);
            $header_rei["TOTALSTUDYACT"]  = "全角".$setsu."文字";
            $setsu = ($this->totalstudyval_moji * $this->totalstudyval_gyou);
            $header_rei["TOTALSTUDYVAL"]  = "全角".$setsu."文字";
        }
        $header_rei[$this->lastColumn]         = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode(",", $header)."\n";
            $contents .= implode(",", $header_rei)."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $headerDuplicate = array("YEAR"        => "年度",
                                     "SCHREGNO"    => "学籍番号",
                                     "PATTERN_SEQ" => "パターン",
                                     "TRAIN_SEQ"   => "SEQ");
            $tables = array();
            $table2 = array();
            if ($this->mode == 'grd') {
                $tables = array("1" => "GRD_HEXAM_ENTREMARK_DAT");
                if ($this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $table2 = array("2" => "HEXAM_ENTREMARK_SEQ_DAT");
                    $tables = array_merge($tables, $table2);
                } else {
                    $table2 = array("2" => "HEXAM_ENTREMARK_DAT");
                    $tables = array_merge($tables, $table2);
                }
            } else {
                if ($this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                    $tables = array("1" => "HEXAM_ENTREMARK_SEQ_DAT");
                } else {
                    $tables = array("1" => "HEXAM_ENTREMARK_DAT");
                }
            }
            if ("1" == $this->tyousasyo2020) {
                if ($this->mode != 'grd') {
                    if ($this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                        $table2 = array("2" => "HEXAM_ENTREMARK_TRAINREF_SEQ_DAT");
                        $tables = array_merge($tables, $table2);
                    } else {
                        $table2 = array("2" => "HEXAM_ENTREMARK_TRAINREF_DAT");
                        $tables = array_merge($tables, $table2);
                    }
                } else {
                    $table3 = array("3" => "HEXAM_ENTREMARK_TRAINREF_DAT");
                    $tables = array_merge($tables, $table3);
                }
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
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
            if ($this->field["OUTPUT"] == "3") {
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
                $rows = knjx190Query::selectMainQuery($db, $this);
            }

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
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                if ($this->mode == 'grd') {
                    $couse = "調査書進学用所見データ(卒業)";
                } else {
                    $couse = "調査書進学用所見データ";
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
        //出欠の記録備考の文字数と行数の設定
        if ("1" == $this->tyousasyo2020) {
            $this->attendrec_remark_moji = 8;
            $this->attendrec_remark_gyou = 7;
        } elseif ($this->Properties["tyousasyoAttendrecRemarkFieldSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["tyousasyoAttendrecRemarkFieldSize"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 5; //デフォルトの値
            $this->attendrec_remark_gyou = 3; //デフォルトの値
        }
        //活動内容の文字数と行数の設定
        if ("1" == $this->tyousasyo2020) {
            $this->totalstudyact_moji = 48;
            $this->totalstudyact_gyou = 8;
            $this->Properties["tyousasyoTotalstudyactFieldSize"] = "48 * 8";
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
            $this->totalstudyval_gyou = 8;
            $this->Properties["tyousasyoTotalstudyvalFieldSize"] = "48 * 8";
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

        if ("1" == $this->tyousasyo2020) {
            $fields = array("TRAIN_REF1","TRAIN_REF2","TRAIN_REF3","TRAIN_REF4","TRAIN_REF5", "TRAIN_REF6");
            foreach ($fields as $field) {
                $this->moji[$field] = 15;
            }
        } elseif ($this->Properties["train_ref_1_2_3_field_size"] == 1) {
            $this->train_ref1_moji = 14;
            $this->train_ref2_moji = 21;
            $this->train_ref3_moji = 7;
        } elseif ($this->Properties["train_ref_1_2_3_field_size"] == 2) {
            $this->train_ref1_moji = 21;
            $this->train_ref2_moji = 21;
            $this->train_ref3_moji = 7;
        } elseif (substr_count($this->Properties["train_ref_1_2_3_field_size"], '-') == "2") {
            $train_ref_array = array();
            $train_ref_array = explode('-', $this->Properties["train_ref_1_2_3_field_size"]);
            $this->train_ref1_moji = $train_ref_array[0];
            $this->train_ref2_moji = $train_ref_array[1];
            $this->train_ref3_moji = $train_ref_array[2];
        } else {
            $this->train_ref1_moji = 14;
            $this->train_ref2_moji = 14;
            $this->train_ref3_moji = 14;
        }

        if ("1" == $this->tyousasyo2020) {
            $fields = array("TRAIN_REF1","TRAIN_REF2","TRAIN_REF3","TRAIN_REF4","TRAIN_REF5", "TRAIN_REF6");
            foreach ($fields as $field) {
                $this->gyou[$field] = 66;
            }
        } elseif ($this->Properties["train_ref_1_2_3_gyo_size"]) {
            $this->train_ref1_gyou = $this->Properties["train_ref_1_2_3_gyo_size"];
            $this->train_ref2_gyou = $this->Properties["train_ref_1_2_3_gyo_size"];
            $this->train_ref3_gyou = $this->Properties["train_ref_1_2_3_gyo_size"];
        } else {
            $this->train_ref1_gyou = 5;
            $this->train_ref2_gyou = 5;
            $this->train_ref3_gyou = 5;
        }

        //特別活動の記録の文字数と行数の設定
        if ("1" == $this->tyousasyo2020) {
            $db = Query::dbCheckOut();
            $query = knjx190Query::getSchoolDiv($this);
            $schooldiv = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if ($schooldiv["NEN"] == '0') {
                if ($schooldiv["SCHOOLDIV"] == "1") {
                    $schooldiv["NEN"] = "4";
                } else {
                    $schooldiv["NEN"] = "3";
                }
            }
            if ($schooldiv["NEN"] == 3) {
                $this->specialactrec_moji = 16;
                $this->specialactrec_gyou = 18;
            } else {
                $this->specialactrec_moji = 10;
                $this->specialactrec_gyou = 27;
            }
        } elseif ($this->Properties["tyousasyoSpecialactrecFieldSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["tyousasyoSpecialactrecFieldSize"]);
            $this->specialactrec_moji = (int)trim($moji);
            $this->specialactrec_gyou = (int)trim($gyou);
        } else {
            if ($this->Properties["tyousasyoTokuBetuFieldSize"] == 1) {
                $this->specialactrec_moji = 15; //デフォルトの値
                $this->specialactrec_gyou = 10; //デフォルトの値
            } else {
                $this->specialactrec_moji = 11; //デフォルトの値
                $this->specialactrec_gyou = 8;  //デフォルトの値
            }
        }
    }
}
