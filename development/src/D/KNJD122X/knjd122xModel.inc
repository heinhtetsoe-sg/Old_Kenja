<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjd122xModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $schoolCd;
    public $useTestCountflg;
    public $selectdata = array();
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $maxSemester;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjd122xModel()
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

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "OUT_YEAR"         =>  VARS::post("OUT_YEAR"),         //年度(出力)
                             "OUT_SEMESTER"     =>  VARS::post("OUT_SEMESTER"),     //学期(出力)
                             "OUT_TESTKIND"     =>  VARS::post("OUT_TESTKIND"),     //テスト種別(出力)
                             "IN_YEAR"          =>  VARS::post("IN_YEAR"),          //年度(取込)
                             "IN_SEMESTER"      =>  VARS::post("IN_SEMESTER"),      //学期(取込)
                             "IN_TESTKIND"      =>  VARS::post("IN_TESTKIND"),      //テスト種別(取込)
                             "CHAIR_DATE"       =>  VARS::post("CHAIR_DATE"),       //講座基準日
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "CHK_COMP"         =>  VARS::post("CHK_COMP"),         //成績入力完了
                             "CHK_SEM9"         =>  VARS::post("CHK_SEM9"),         //ヘッダ出力(学年末用)
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        $this->useTestCountflg = ($this->Properties["useTestCountflg"]) ? $this->Properties["useTestCountflg"] : 'TESTITEM_MST_COUNTFLG_NEW';
        $this->file = VARS::file("FILE");

        $this->selectdata = array("SUBCLASSCD"  => VARS::post("selectdataSubclasscd"),
                                  "CHAIRCD"     => VARS::post("selectdataChaircd"));

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
                    knjd122xQuery::insertQueryCsv($this, $this->data_arr);
                    knjd122xQuery::updateRecordChkfin($this, $this->data_arr);
                } else {
                    knjd122xQuery::deleteQueryCsv($this, $this->data_arr);
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
            $checkdata = array();
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                if ($this->field["IN_SEMESTER"] == "9") {
                    $checkdata = array("YEAR"            => array(4,"年度"),
                                       "SEMESTER"        => array(1,"学期"),
                                       "TESTKINDCD"      => array(2,"テスト種別コード"),
                                       "TESTITEMCD"      => array(2,"テスト項目コード"),
                                       "SCORE_DIV"       => array(1,"区分"),
                                       "CLASSCD"         => array(2,"教科コード"),
                                       "SCHOOL_KIND"     => array(2,"学校種別"),
                                       "CURRICULUM_CD"   => array(2,"教育課程コード"),
                                       "SUBCLASSCD"      => array(6,"科目コード"),
                                       "CHAIRCD"         => array(7,"講座コード"),
                                       "CHAIR_DATE"      => array(10,"講座基準日"),
                                       "SCHREGNO"        => array(8,"学籍番号"),
                                       "SCORE"           => array(3,"成績"),
                                       "COMP_CREDIT"     => array(2,"履修単位"),
                                       "GET_CREDIT"      => array(2,"修得単位")
                                       );
                } else {
                    $checkdata = array("YEAR"            => array(4,"年度"),
                                       "SEMESTER"        => array(1,"学期"),
                                       "TESTKINDCD"      => array(2,"テスト種別コード"),
                                       "TESTITEMCD"      => array(2,"テスト項目コード"),
                                       "SCORE_DIV"       => array(1,"区分"),
                                       "CLASSCD"         => array(2,"教科コード"),
                                       "SCHOOL_KIND"     => array(2,"学校種別"),
                                       "CURRICULUM_CD"   => array(2,"教育課程コード"),
                                       "SUBCLASSCD"      => array(6,"科目コード"),
                                       "CHAIRCD"         => array(7,"講座コード"),
                                       "CHAIR_DATE"      => array(10,"講座基準日"),
                                       "SCHREGNO"        => array(8,"学籍番号"),
                                       "SCORE"           => array(3,"成績")
                                       );
                }
            } else {
                if ($this->field["IN_SEMESTER"] == "9") {
                    $checkdata = array("YEAR"            => array(4,"年度"),
                                       "SEMESTER"        => array(1,"学期"),
                                       "TESTKINDCD"      => array(2,"テスト種別コード"),
                                       "TESTITEMCD"      => array(2,"テスト項目コード"),
                                       "SCORE_DIV"       => array(1,"区分"),
                                       "SUBCLASSCD"      => array(6,"科目コード"),
                                       "CHAIRCD"         => array(7,"講座コード"),
                                       "CHAIR_DATE"      => array(10,"講座基準日"),
                                       "SCHREGNO"        => array(8,"学籍番号"),
                                       "SCORE"           => array(3,"成績"),
                                       "COMP_CREDIT"     => array(2,"履修単位"),
                                       "GET_CREDIT"      => array(2,"修得単位")
                                       );
                } else {
                    $checkdata = array("YEAR"            => array(4,"年度"),
                                       "SEMESTER"        => array(1,"学期"),
                                       "TESTKINDCD"      => array(2,"テスト種別コード"),
                                       "TESTITEMCD"      => array(2,"テスト項目コード"),
                                       "SCORE_DIV"       => array(1,"区分"),
                                       "SUBCLASSCD"      => array(6,"科目コード"),
                                       "CHAIRCD"         => array(7,"講座コード"),
                                       "CHAIR_DATE"      => array(10,"講座基準日"),
                                       "SCHREGNO"        => array(8,"学籍番号"),
                                       "SCORE"           => array(3,"成績")
                                       );
                }
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
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey2 = array();
            $duplicateCsv2 = array();
            $primaryKey1 = $this->getPrimaryKey('RECORD_DAT');
            list($testkindcd, $testitemcd, $scoreDiv) = explode("-", $this->field["IN_TESTKIND"]);
            $updateTable = ($scoreDiv == "1") ? "SCH_CHR_TEST" : "RECORD_CHKFIN_DAT";
            $primaryKey2 = $this->getPrimaryKey($updateTable);
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

                    $data = array();
                    //教育課程対応
                    if ($this->Properties["useCurriculumcd"] == '1') {
                        if ($this->field["IN_SEMESTER"] == "9") {
                            $data = array("YEAR"            => $tmp[0],
                                          "SEMESTER"        => $tmp[1],
                                          "TESTKINDCD"      => (strlen(trim($tmp[2])))  ? sprintf("%02d", $tmp[2])  : "",
                                          "TESTITEMCD"      => (strlen(trim($tmp[3])))  ? sprintf("%02d", $tmp[3])  : "",
                                          "SCORE_DIV"       => $tmp[4],
                                          "CLASSCD"         => (strlen(trim($tmp[6])))  ? sprintf("%02d", $tmp[6])  : "",
                                          "SCHOOL_KIND"     => $tmp[7],
                                          "CURRICULUM_CD"   => $tmp[8],
                                          "SUBCLASSCD"      => (strlen(trim($tmp[9])))  ? sprintf("%06d", $tmp[9])  : "",
                                          "CHAIRCD"         => (strlen(trim($tmp[11])))  ? sprintf("%07d", $tmp[11])  : "",
                                          "CHAIR_DATE"      => $tmp[13],
                                          "SCHREGNO"        => (strlen(trim($tmp[16]))) ? sprintf("%08d", $tmp[16]) : "",
                                          "SCORE"           => $tmp[17],
                                          "COMP_CREDIT"     => $tmp[18],
                                          "GET_CREDIT"      => $tmp[19],
                                          "TAKESEMES"       => "0",
                                          "RECORD_DIV"      => "",
                                          "EXECUTEDATE"     => "",
                                          "PERIODCD"        => ""
                                          );
                        } else {
                            $data = array("YEAR"            => $tmp[0],
                                          "SEMESTER"        => $tmp[1],
                                          "TESTKINDCD"      => (strlen(trim($tmp[2])))  ? sprintf("%02d", $tmp[2])  : "",
                                          "TESTITEMCD"      => (strlen(trim($tmp[3])))  ? sprintf("%02d", $tmp[3])  : "",
                                          "SCORE_DIV"       => $tmp[4],
                                          "CLASSCD"         => (strlen(trim($tmp[6])))  ? sprintf("%02d", $tmp[6])  : "",
                                          "SCHOOL_KIND"     => $tmp[7],
                                          "CURRICULUM_CD"   => $tmp[8],
                                          "SUBCLASSCD"      => (strlen(trim($tmp[9])))  ? sprintf("%06d", $tmp[9])  : "",
                                          "CHAIRCD"         => (strlen(trim($tmp[11])))  ? sprintf("%07d", $tmp[11])  : "",
                                          "CHAIR_DATE"      => $tmp[13],
                                          "SCHREGNO"        => (strlen(trim($tmp[16]))) ? sprintf("%08d", $tmp[16]) : "",
                                          "SCORE"           => $tmp[17],
                                          "TAKESEMES"       => "0",
                                          "RECORD_DIV"      => "",
                                          "EXECUTEDATE"     => "",
                                          "PERIODCD"        => ""
                                          );
                        }
                    } else {
                        if ($this->field["IN_SEMESTER"] == "9") {
                            $data = array("YEAR"            => $tmp[0],
                                          "SEMESTER"        => $tmp[1],
                                          "TESTKINDCD"      => (strlen(trim($tmp[2])))  ? sprintf("%02d", $tmp[2])  : "",
                                          "TESTITEMCD"      => (strlen(trim($tmp[3])))  ? sprintf("%02d", $tmp[3])  : "",
                                          "SCORE_DIV"       => $tmp[4],
                                          "SUBCLASSCD"      => (strlen(trim($tmp[6])))  ? sprintf("%06d", $tmp[6])  : "",
                                          "CHAIRCD"         => (strlen(trim($tmp[8])))  ? sprintf("%07d", $tmp[8])  : "",
                                          "CHAIR_DATE"      => $tmp[10],
                                          "SCHREGNO"        => (strlen(trim($tmp[13]))) ? sprintf("%08d", $tmp[13]) : "",
                                          "SCORE"           => $tmp[14],
                                          "COMP_CREDIT"     => $tmp[15],
                                          "GET_CREDIT"      => $tmp[16],
                                          "TAKESEMES"       => "0",
                                          "RECORD_DIV"      => "",
                                          "EXECUTEDATE"     => "",
                                          "PERIODCD"        => ""
                                          );
                        } else {
                            $data = array("YEAR"            => $tmp[0],
                                          "SEMESTER"        => $tmp[1],
                                          "TESTKINDCD"      => (strlen(trim($tmp[2])))  ? sprintf("%02d", $tmp[2])  : "",
                                          "TESTITEMCD"      => (strlen(trim($tmp[3])))  ? sprintf("%02d", $tmp[3])  : "",
                                          "SCORE_DIV"       => $tmp[4],
                                          "SUBCLASSCD"      => (strlen(trim($tmp[6])))  ? sprintf("%06d", $tmp[6])  : "",
                                          "CHAIRCD"         => (strlen(trim($tmp[8])))  ? sprintf("%07d", $tmp[8])  : "",
                                          "CHAIR_DATE"      => $tmp[10],
                                          "SCHREGNO"        => (strlen(trim($tmp[13]))) ? sprintf("%08d", $tmp[13]) : "",
                                          "SCORE"           => $tmp[14],
                                          "TAKESEMES"       => "0",
                                          "RECORD_DIV"      => "",
                                          "EXECUTEDATE"     => "",
                                          "PERIODCD"        => ""
                                          );
                        }
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
        $returnFlg = true;
        $agreeErrFlg     = false;    //不一致エラーフラグ
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $valuesErrFlg    = false;    //値エラーフラグ
        $duplicateCsvErrFlg = false; //重複(CSV)エラーフラグ

        $agreeErrMsg     = "";       //不一致エラーメッセージ
        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $valuesErrMsg    = "";       //値エラーメッセージ
        $duplicateCsvErrMsg = ""; //重複(CSV)メッセージ

        $agreeSep     = "";
        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $valuesSep    = "";
        $duplicateCsvSep= "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        //不一致チェック用
        list($testkindcd, $testitemcd, $scoreDiv) = explode("-", $this->field["IN_TESTKIND"]);
        $agreeArray = array("YEAR"            => $this->field["IN_YEAR"],
                            "SEMESTER"        => $this->field["IN_SEMESTER"],
                            "TESTKINDCD"      => $testkindcd,
                            "TESTITEMCD"      => $testitemcd,
                            "SCORE_DIV"       => $scoreDiv
                            );
        foreach ($data_each as $key => $val) {
            //不一致チェック
            if (in_array($key, array("YEAR","SEMESTER","TESTKINDCD","TESTITEMCD","SCORE_DIV"))) {
                if ($data[$key] != $agreeArray[$key]) {
                    $returnFlg = $this->errSet($agreeErrFlg, $agreeErrMsg, "※不一致:", $agreeSep, $val[1]);
                }
            }

            //必須チェック
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                if (in_array($key, array("YEAR","SEMESTER","TESTKINDCD","TESTITEMCD","SCORE_DIV","CLASSCD","SCHOOL_KIND","CURRICULUM_CD","SUBCLASSCD","CHAIRCD","CHAIR_DATE","SCHREGNO"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            } else {
                if (in_array($key, array("YEAR","SEMESTER","TESTKINDCD","TESTITEMCD","SCORE_DIV","SUBCLASSCD","CHAIRCD","CHAIR_DATE","SCHREGNO"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            }

            //日付チェック
            if (in_array($key, array("CHAIR_DATE"))) {
                if ($data[$key] != "" && !$this->dateRep($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //数値チェック
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                if (in_array($key, array("YEAR","SEMESTER","TESTKINDCD","TESTITEMCD","SCORE_DIV","CLASSCD","SUBCLASSCD","CHAIRCD","SCHREGNO"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            } else {
                if (in_array($key, array("YEAR","SEMESTER","TESTKINDCD","TESTITEMCD","SCORE_DIV","SUBCLASSCD","CHAIRCD","SCHREGNO"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }
            if ($this->field["IN_SEMESTER"] == "9") {
                if (in_array($key, array("COMP_CREDIT","GET_CREDIT"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }

            //成績・出欠記号チェック
            if ($data[$key] != "" && in_array($key, array("SCORE"))) {
                if ($scoreDiv == "1" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }
                if ($scoreDiv == "2" && !is_numeric($data[$key]) && $data[$key] != "-" && $data[$key] != "=") {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }
            }
        }

        //存在チェック(項目エラーなしの場合にチェック)
        if ($returnFlg) {
            if (1 > $db->getOne(knjd122xQuery::checkMeiboData($data["YEAR"], $data["SEMESTER"], $data["CHAIRCD"], $data["CHAIR_DATE"], $data["SCHREGNO"]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "講座名簿");
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$agreeErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjd122xQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 35000) {
            $this->setWarning("MSG915", "処理件数を３万５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
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

        $header = array();
        $header_rei = array();
        //学年末の場合、履修単位と修得単位を追加する。
        if (($this->field["OUTPUT"] == "1" && $this->field["CHK_SEM9"] != "") ||
            ($this->field["OUTPUT"] == "4" && $this->field["OUT_SEMESTER"] == "9")) {
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $header = array("YEAR"                  => "※年度",
                                "SEMESTER"              => "※学期",
                                "TESTKINDCD"            => "※テスト種別コード",
                                "TESTITEMCD"            => "※テスト項目コード",
                                "SCORE_DIV"             => "※区分（1:素点、2:評価or評定）",
                                "TESTITEMNAME"          => "テスト種別名称",
                                "CLASSCD"               => "※教科コード",
                                "SCHOOL_KIND"           => "※学校種別",
                                "CURRICULUM_CD"         => "※教育課程コード",
                                "SUBCLASSCD"            => "※科目コード",
                                "SUBCLASSNAME"          => "科目名称",
                                "CHAIRCD"               => "※講座コード",
                                "CHAIRNAME"             => "講座名称",
                                "CHAIR_DATE"            => "※講座基準日",
                                "ATTENDNO"              => "年組番",
                                "NAME"                  => "氏名",
                                "SCHREGNO"              => "※学籍番号",
                                "SCORE"                 => "成績",
                                "COMP_CREDIT"           => "履修単位",
                                "GET_CREDIT"            => "修得単位",
                                $this->lastColumn       => $this->lastColumn);

                $header_rei = array("YEAR"                  => "2010",
                                    "SEMESTER"              => "9",
                                    "TESTKINDCD"            => "99",
                                    "TESTITEMCD"            => "00",
                                    "SCORE_DIV"             => "2",
                                    "TESTITEMNAME"          => "学年評定",
                                    "CLASSCD"               => "11",
                                    "SCHOOL_KIND"           => "H",
                                    "CURRICULUM_CD"         => "2",
                                    "SUBCLASSCD"            => "110101",
                                    "SUBCLASSNAME"          => "実用国語",
                                    "CHAIRCD"               => "1101011",
                                    "CHAIRNAME"             => "実用国語 1-1",
                                    "CHAIR_DATE"            => "2011-03-01",
                                    "ATTENDNO"              => "1年1組-01番",
                                    "NAME"                  => "アルプ太郎",
                                    "SCHREGNO"              => "12345678",
                                    "SCORE"                 => "5",
                                    "COMP_CREDIT"           => "2",
                                    "GET_CREDIT"            => "2",
                                    $this->lastColumn       => $this->lastColumn);
            } else {
                $header = array("YEAR"                  => "※年度",
                                "SEMESTER"              => "※学期",
                                "TESTKINDCD"            => "※テスト種別コード",
                                "TESTITEMCD"            => "※テスト項目コード",
                                "SCORE_DIV"             => "※区分（1:素点、2:評価or評定）",
                                "TESTITEMNAME"          => "テスト種別名称",
                                "SUBCLASSCD"            => "※科目コード",
                                "SUBCLASSNAME"          => "科目名称",
                                "CHAIRCD"               => "※講座コード",
                                "CHAIRNAME"             => "講座名称",
                                "CHAIR_DATE"            => "※講座基準日",
                                "ATTENDNO"              => "年組番",
                                "NAME"                  => "氏名",
                                "SCHREGNO"              => "※学籍番号",
                                "SCORE"                 => "成績",
                                "COMP_CREDIT"           => "履修単位",
                                "GET_CREDIT"            => "修得単位",
                                $this->lastColumn       => $this->lastColumn);

                $header_rei = array("YEAR"                  => "2010",
                                    "SEMESTER"              => "9",
                                    "TESTKINDCD"            => "99",
                                    "TESTITEMCD"            => "00",
                                    "SCORE_DIV"             => "2",
                                    "TESTITEMNAME"          => "学年評定",
                                    "SUBCLASSCD"            => "110101",
                                    "SUBCLASSNAME"          => "実用国語",
                                    "CHAIRCD"               => "1101011",
                                    "CHAIRNAME"             => "実用国語 1-1",
                                    "CHAIR_DATE"            => "2011-03-01",
                                    "ATTENDNO"              => "1年1組-01番",
                                    "NAME"                  => "アルプ太郎",
                                    "SCHREGNO"              => "12345678",
                                    "SCORE"                 => "5",
                                    "COMP_CREDIT"           => "2",
                                    "GET_CREDIT"            => "2",
                                    $this->lastColumn       => $this->lastColumn);
            }
        } else {
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $header = array("YEAR"                  => "※年度",
                                "SEMESTER"              => "※学期",
                                "TESTKINDCD"            => "※テスト種別コード",
                                "TESTITEMCD"            => "※テスト項目コード",
                                "SCORE_DIV"             => "※区分（1:素点、2:評価or評定）",
                                "TESTITEMNAME"          => "テスト種別名称",
                                "CLASSCD"               => "※教科コード",
                                "SCHOOL_KIND"           => "※学校種別",
                                "CURRICULUM_CD"         => "※教育課程コード",
                                "SUBCLASSCD"            => "※科目コード",
                                "SUBCLASSNAME"          => "科目名称",
                                "CHAIRCD"               => "※講座コード",
                                "CHAIRNAME"             => "講座名称",
                                "CHAIR_DATE"            => "※講座基準日",
                                "ATTENDNO"              => "年組番",
                                "NAME"                  => "氏名",
                                "SCHREGNO"              => "※学籍番号",
                                "SCORE"                 => "成績",
                                $this->lastColumn       => $this->lastColumn);

                $header_rei = array("YEAR"                  => "2010",
                                    "SEMESTER"              => "1",
                                    "TESTKINDCD"            => "01",
                                    "TESTITEMCD"            => "01",
                                    "SCORE_DIV"             => "1",
                                    "TESTITEMNAME"          => "中間考査",
                                    "CLASSCD"               => "11",
                                    "SCHOOL_KIND"           => "H",
                                    "CURRICULUM_CD"         => "2",
                                    "SUBCLASSCD"            => "110101",
                                    "SUBCLASSNAME"          => "実用国語",
                                    "CHAIRCD"               => "1101011",
                                    "CHAIRNAME"             => "実用国語 1-1",
                                    "CHAIR_DATE"            => "2010-04-04",
                                    "ATTENDNO"              => "1年1組-01番",
                                    "NAME"                  => "アルプ太郎",
                                    "SCHREGNO"              => "12345678",
                                    "SCORE"                 => "100",
                                    $this->lastColumn       => $this->lastColumn);
            } else {
                $header = array("YEAR"                  => "※年度",
                                "SEMESTER"              => "※学期",
                                "TESTKINDCD"            => "※テスト種別コード",
                                "TESTITEMCD"            => "※テスト項目コード",
                                "SCORE_DIV"             => "※区分（1:素点、2:評価or評定）",
                                "TESTITEMNAME"          => "テスト種別名称",
                                "SUBCLASSCD"            => "※科目コード",
                                "SUBCLASSNAME"          => "科目名称",
                                "CHAIRCD"               => "※講座コード",
                                "CHAIRNAME"             => "講座名称",
                                "CHAIR_DATE"            => "※講座基準日",
                                "ATTENDNO"              => "年組番",
                                "NAME"                  => "氏名",
                                "SCHREGNO"              => "※学籍番号",
                                "SCORE"                 => "成績",
                                $this->lastColumn       => $this->lastColumn);

                $header_rei = array("YEAR"                  => "2010",
                                    "SEMESTER"              => "1",
                                    "TESTKINDCD"            => "01",
                                    "TESTITEMCD"            => "01",
                                    "SCORE_DIV"             => "1",
                                    "TESTITEMNAME"          => "中間考査",
                                    "SUBCLASSCD"            => "110101",
                                    "SUBCLASSNAME"          => "実用国語",
                                    "CHAIRCD"               => "1101011",
                                    "CHAIRNAME"             => "実用国語 1-1",
                                    "CHAIR_DATE"            => "2010-04-04",
                                    "ATTENDNO"              => "1年1組-01番",
                                    "NAME"                  => "アルプ太郎",
                                    "SCHREGNO"              => "12345678",
                                    "SCORE"                 => "100",
                                    $this->lastColumn       => $this->lastColumn);
            }
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
            $headerDuplicate = array("YEAR"                 => "年度",
                                    "SEMESTER"              => "学期",
                                    "TESTKINDCD"            => "テスト種別コード",
                                    "TESTITEMCD"            => "テスト項目コード",
                                    "RECORD_DIV"            => "レコード区分",
                                    "CLASSCD"               => "教科コード",
                                    "SCHOOL_KIND"           => "学校校種",
                                    "CURRICULUM_CD"         => "教育課程コード",
                                    "SUBCLASSCD"            => "科目コード",
                                    "SCHREGNO"              => "学籍番号",
                                    "EXECUTEDATE"           => "実施日付",
                                    "PERIODCD"              => "校時コード",
                                    "CHAIRCD"               => "講座コード",
                                    "TAKESEMES"             => "履修学期");
            list($testkindcd, $testitemcd, $scoreDiv) = explode("-", $this->field["IN_TESTKIND"]);
            $updateTable2 = ($scoreDiv == "1") ? "SCH_CHR_TEST" : "RECORD_CHKFIN_DAT";
            $tables = array();
            $tables = array("1" => "RECORD_DAT", "2" => $updateTable2);
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

        //DB接続
        $db = Query::dbCheckOut();

        //テスト種別名称を取得
        $query = knjd122xQuery::getSelectTestkind($this->useTestCountflg, $this->field["OUT_YEAR"], $this->field["OUT_SEMESTER"], $this->field["OUT_TESTKIND"]);
        $testItemName = $db->getOne($query);

        /**********/
        /* エラー */
        /**********/
        if ($this->field["OUTPUT"]=="3") {
            //SQL文発行
            $query = $this->selectCsvErrQuery(PROGRAMID);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();

        /**********/
        /* データ */
        /**********/
        } elseif ($this->field["OUTPUT"]=="4") {
            //変数取得
            list($testkindcd, $testitemcd, $scoreDiv) = explode("-", $this->field["OUT_TESTKIND"]);
            $scoreField = "";
            if ($this->field["OUT_SEMESTER"] == "9") {
                $scoreField = "GRAD_VALUE";
            } else {
                //学期
                $scoreField = "SEM".$this->field["OUT_SEMESTER"];
                //テスト種別
                if ($testkindcd == "01" && $testitemcd == "01") {
                    $scoreField .= "_INTR";
                }
                if ($testkindcd == "02" && $testitemcd == "01") {
                    $scoreField .= "_TERM";
                }
                if ($testkindcd == "02" && $testitemcd == "02") {
                    $scoreField .= "_TERM2";
                }
                //区分（1:素点、2:評価or評定）
                if ($scoreDiv == "1") {
                    $scoreField .= "_SCORE";
                }
                if ($scoreDiv == "2") {
                    $scoreField .= "_VALUE";
                }
            }
            //SQL文発行
            $query = knjd122xQuery::selectMainQuery($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {      //データ
                    $row = str_replace(",", ".", $row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                //変数セット
                $row["YEAR"]            = $this->field["OUT_YEAR"];
                $row["SEMESTER"]        = $this->field["OUT_SEMESTER"];
                $row["TESTKINDCD"]      = $testkindcd;
                $row["TESTITEMCD"]      = $testitemcd;
                $row["SCORE_DIV"]       = $scoreDiv;
                $row["TESTITEMNAME"]    = $testItemName;
                $row["CHAIR_DATE"]      = $this->field["CHAIR_DATE"];
                //年組番
                if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
                    $row["ATTENDNO"]    = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]) .番;
                }
                //*_DI のフィールドに値があればそっちを使う
                $row["SCORE"] = ($scoreDiv == "2" && strlen($row[$scoreField."_DI"])) ? $row[$scoreField."_DI"] : $row[$scoreField];
                //データセット
                foreach ($header as $key => $val) {
                    $contents .= $row[$key]. ",";
                }
                $contents .= "\n";
                $header_data = true;
            }
            $result->free();
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "成績データ";
            }
            $filename = $couse ."（" .$testItemName ."）.csv";
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    public function dateRep($data)
    {
        if ($data!="") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }
}
//var_dump($_REQUEST);
