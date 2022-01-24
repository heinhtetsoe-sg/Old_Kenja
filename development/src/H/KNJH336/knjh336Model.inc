<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjh336Model extends Model
{
    public $cmd;
    public $control;
    public $file;
    public $field = array();
    public $data_arr = array();
    public $headercheck;
    public $z010Name1;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjh336Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    public function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->field = array("PUTDATA"          =>  VARS::post("PUTDATA"),          //得点/総点
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI"),        //処理名
                             "HEAD_DATA_DIV"    =>  VARS::post("HEAD_DATA_DIV"),    //テンプレートデータ種別
                             "INS_DATA_DIV"     =>  VARS::post("INS_DATA_DIV"),     //更新データ種別
                             "INS_MOCK_TARGET"  =>  VARS::post("INS_MOCK_TARGET"),  //更新テスト名称
                             "OUT_DATA_DIV"     =>  VARS::post("OUT_DATA_DIV"),     //出力データ種別
                             "OUT_MOCK_TARGET"  =>  VARS::post("OUT_MOCK_TARGET")   //出力テスト名称
                             );

        if (VARS::get("PROGRAMID")) {
            $this->headercheck    = "1";
        } else {
            $this->headercheck    = VARS::post("HEADER");
        }
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
                    if ($this->field["PUTDATA"] != "2") {
                        if ($this->field["INS_DATA_DIV"] < 3) {
                            knjh336Query::insertMockQuery($this, $this->data_arr);
                        } else {
                            knjh336Query::insertTargetQuery($this, $this->data_arr);
                        }
                    } else {
                        knjh336Query::insertMockSoutenQuery($this, $this->data_arr);
                    }
                } else {
                    if ($this->field["PUTDATA"] != "2") {
                        if ($this->field["INS_DATA_DIV"] < 3) {
                            knjh336Query::deleteMockQuery($this, $this->data_arr);
                        } else {
                            knjh336Query::deleteTargetQuery($this, $this->data_arr);
                        }
                    } else {
                        knjh336Query::deleteMockSoutenQuery($this, $this->data_arr);
                    }
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //ファイルの文字列の取得
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {
            $this->warning = "ファイル名が不正です。";
        } else {
            //項目チェック用の配列
            if ($this->field["PUTDATA"] != "2") {
                if ($this->field["INS_DATA_DIV"] < 3) {
                    $data_each = $this->getMockHeadCheck();
                } else {
                    $data_each = $this->getTargetHeadCheck();
                }
            } else {
                $data_each = $this->getMockHeadSoutenCheck();
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
            if ($this->field["PUTDATA"] != "2") {
                if ($this->field["INS_DATA_DIV"] < 3) {
                    $primaryKey1 = $this->getPrimaryKey('MOCK_DAT');
                } else {
                    $primaryKey1 = $this->getPrimaryKey('MOCK_TARGET_DAT');
                }
            } else {
                $primaryKey1 = $this->getPrimaryKey('MOCK_SCHREG_DAT');
            }
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->headercheck == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $data   = $this->setData($tmp);
                    //エラーチェック
                    if ($this->errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)) {
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

    //データ退避
    public function setData($tmp)
    {
        if ($this->field["PUTDATA"] != "2") {
            if ($this->field["INS_DATA_DIV"] < 3) {
                $data = $this->getMockHeadArray();
            } else {
                $data = $this->getTargetHeadArray();
            }
        } else {
            $data = $this->getMockHeadSoutenArray();
        }
        $i = 0;
        foreach ($data as $key => $val) {
            $data[$key] = $tmp[$i];
            $i++;
        }

        return $data;
    }

    //エラーチェック
    public function errCheck($db, &$data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $duplicateCsvSep    = "";

        $buff_target_div = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            if ($key == "TARGET_DIV") {
                $buff_target_div = $data[$key];
            }
            if ($this->field["SHORI_MEI"] == 2) {
                //必須項目チェック
                if (in_array($key, array("YEAR", "MOCKCD", "SCHREGNO", "MOCK_SUBCLASS_CD",
                                         "TARGET_DIV", "STF_AUTH_CD", "TARGETCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //存在チェック
                if (in_array($key, array("MOCKCD", "TARGETCD"))) {
                    if ($buff_target_div.$data[$key] != $this->field["INS_MOCK_TARGET"]) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
            } else {
                //必須項目チェック
                if (in_array($key, array("YEAR", "MOCKCD", "SCHREGNO", "MOCK_SUBCLASS_CD",
                                         "SCORE", "TARGET_DIV", "STF_AUTH_CD", "TARGETCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //存在チェック
                if (in_array($key, array("MOCKCD", "TARGETCD"))) {
                    if ($buff_target_div.$data[$key] != $this->field["INS_MOCK_TARGET"]) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }

                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }

                //得点チェック
                if (in_array($key, array("DEVIATION"))) {
                    if ($data[$key] != "") {
                        $itema = explode(".", $data[$key]);
                        $itemb = str_replace(".", "", $data[$key]);
                        if ($itemb != "" && !is_numeric($itemb)) {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                        }
                        if (strlen($itema[0]) > 3) {
                            $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                        }
                    }
                }

                //数値チェック
                if (in_array($key, array("YEAR", "MOCKCD", "SCHREGNO", "MOCK_SUBCLASS_CD", "GRADE",
                                         "ATTENDNO", "FORMNO", "EXAMNO", "SCHOOLCD", "SCHEDULE",
                                         "TOTALWISHRANK", "TOTALWISHCNT", "JUDGEVALUE", "SUBCLASS_NAMECD",
                                         "POINT_CONVERSION", "SCORE", "RANK", "TARGET_DIV",
                                         "STF_AUTH_CD", "TARGETCD",
                                         "SOUTEN", "SOUTEN_PERCENT", "RISOUTEN", "RISOUTEN_PERCENT"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                //日付チェック
                if (in_array($key, array("EXECUTION_DAY"))) {
                    if ($data[$key] != "" && strlen($data[$key]) < 6) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                    if ($data[$key] != "" && strlen($data[$key]) == 6 &&
                        !$this->date_rep("20".substr($data[$key], 0, 2)."/".substr($data[$key], 2, 2)."/".substr($data[$key], 4))) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    } else {
                        $data[$key] = (!$this->dateRep("20".substr($data[$key], 0, 2)."/".substr($data[$key], 2, 2)."/".substr($data[$key], 4))) ? "" : "20".substr($data[$key], 0, 2)."/".substr($data[$key], 2, 2)."/".substr($data[$key], 4);
                    }
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjh336Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
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

    //模試データチェック用配列
    public function getMockHeadCheck()
    {
        $data_each = array("YEAR"                  => array(4, "年度"),
                           "MOCKCD"                => array(9, "模試コード"),
                           "SCHREGNO"              => array(8, "学籍番号"),
                           "MOCK_SUBCLASS_CD"      => array(6, "科目区分"),
                           "GRADE"                 => array(2, "学年"),
                           "HR_CLASS"              => array(3, "クラス"),
                           "ATTENDNO"              => array(3, "出席番号"),
                           "NAME_KANA"             => array(120, "氏名かな"),
                           "SEX"                   => array(3, "性別"),
                           "FORMNO"                => array(6, "模試用紙番号"),
                           "EXAMNO"                => array(7, "受験番号"),
                           "SCHOOLCD"              => array(6, "高校コード"),
                           "COURSEDIV"             => array(3, "コース区分"),
                           "STATE_EXAM"            => array(3, "受験生状態"),
                           "EXECUTION_DAY"         => array(10, "模試実施日"),
                           "SCHEDULE"              => array(2, "日程"),
                           "TOTALWISHRANK"         => array(6, "総志望席次"),
                           "TOTALWISHCNT"          => array(5, "総志望人数"),
                           "JUDGEEVALUATION"       => array(3, "判定評価"),
                           "JUDGEVALUE"            => array(5, "判定数値"),
                           "SUBCLASS_NAMECD"       => array(10, "科目/大学コード"),
                           "SUBCLASS_NAME"         => array(30, "科目/大学名"),
                           "POINT_CONVERSION"      => array(3, "配点/換算得点"),
                           "SCORE"                 => array(4, "得点"),
                           "DEVIATION"             => array(5, "偏差値"),
                           "EVALUATION_S"          => array(3, "評セ"),
                           "EVALUATION_N"          => array(3, "評ニ"),
                           "EVALUATION_P"          => array(3, "評ポ"),
                           "EVALUATION_T"          => array(3, "評総"),
                           "RANK"                  => array(7, "席次"));
        return $data_each;
    }

    //目標値データチェック用配列
    public function getTargetHeadCheck()
    {
        $data_each = array("TARGET_DIV"            => array(1, "目標値区分"),
                           "STF_AUTH_CD"           => array(8, "職員/権限コード"),
                           "TARGETCD"              => array(9, "目標値コード"),
                           "MOCK_SUBCLASS_CD"      => array(6, "科目区分"),
                           "SUBCLASS_NAMECD"       => array(10, "科目/大学コード"),
                           "SUBCLASS_NAME"         => array(30, "科目/大学名"),
                           "POINT_CONVERSION"      => array(3, "配点/換算得点"),
                           "SCORE"                 => array(4, "得点"),
                           "DEVIATION"             => array(5, "偏差値"));
        return $data_each;
    }

    //総点データチェック用配列
    public function getMockHeadSoutenCheck()
    {
        $data_each = array("YEAR"               => array(4, "年度"),
                           "MOCKCD"             => array(9, "模試コード"),
                           "SCHREGNO"           => array(8, "学籍番号"),
                           "SOUTEN"             => array(4, "総点"),
                           "SOUTEN_PERCENT"     => array(9, "総点％"),
                           "RISOUTEN"           => array(4, "理総点"),
                           "RISOUTEN_PERCENT"   => array(9, "理総点％"));
        if ($this->z010Name1 == 'musashinohigashi') {
            $data_each["SOUTEN"] = array(4, "３科合計点");
            $data_each["SOUTEN_PERCENT"] = array(9, "３科偏差値");
            $data_each["RISOUTEN"] = array(4, "５科合計点");
            $data_each["RISOUTEN_PERCENT"] = array(9, "５科偏差値");
        }
        return $data_each;
    }

    //CSV出力
    public function outputDataFile()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";
        $label = $db->getRow(knjh336Query::getMockDiv($this->field["OUT_DATA_DIV"]), DB_FETCHMODE_ASSOC);
        $filename = sprintf("%s.csv", $label["LABEL"]);
        if ($this->field["OUTPUT"] == "3") {
            //エラー
            $tables          = array();
            $headerDuplicate = array();
            if ($this->field["PUTDATA"] != "2") {
                if ($this->field["INS_DATA_DIV"] < 3) {
                    $tables = array("1" => "MOCK_DAT");
                    $headerDuplicate = $this->getMockHeadArray();
                } else {
                    $tables = array("1" => "MOCK_TARGET_DAT");
                    $headerDuplicate = $this->getTargetHeadArray();
                }
            } else {
                $tables = array("1" => "MOCK_SCHREG_DAT");
                $headerDuplicate = $this->getMockHeadSoutenArray();
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents  = implode($headerErr, ",")."\n";
            }
            $contents .= "行番号, メッセージ\n";
            $filename = sprintf("%s.csv", "模試エラーデータ");
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } else {
            //データ
            if ($this->field["HEADER"] == "on") {
                if ($this->field["PUTDATA"] != "2") {
                    if ($this->field["OUT_DATA_DIV"] < 3) {
                        $contents .= implode(",", $this->getMockHeadArray())."\n";
                        $query = knjh336Query::getselectMockDataSQL($this);
                    } else {
                        $contents .= implode(",", $this->getTargetHeadArray())."\n";
                        $query = knjh336Query::getselectTargetDataSQL($this);
                    }
                } else {
                    $contents .= implode(",", $this->getMockHeadSoutenArray())."\n";
                    $query = knjh336Query::getselectMockSoutenDataSQL($this);
                }
            } else {
                if ($this->field["OUT_DATA_DIV"] < 3) {
                    $query = knjh336Query::getselectMockDataSQL($this);
                } else {
                    $query = knjh336Query::getselectTargetDataSQL($this);
                }
            }
        }

        $header_data = false;
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //テンプレートの書き出し
    public function outputTmpFile()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $label = $db->getRow(knjh336Query::getMockDiv($this->field["HEAD_DATA_DIV"]), DB_FETCHMODE_ASSOC);
        $filename = sprintf("%s.csv", $label["LABEL"]);
        if ($this->field["PUTDATA"] != "2") {
            if ($this->field["HEAD_DATA_DIV"] < 3) {
                $contents  = implode(",", $this->getMockHeadArray())."\n";
                $contents .= implode(",", $this->getMockExample())."\n";
            } else {
                $contents  = implode(",", $this->getTargetHeadArray())."\n";
                $contents .= implode(",", $this->getTargetExample())."\n";
            }
        } else {
            $contents  = implode(",", $this->getMockHeadSoutenArray())."\n";
            $contents .= implode(",", $this->getMockSoutenExample())."\n";
        }

        common::downloadContents($contents, $filename);

        //DB切断
        Query::dbCheckIn($db);

        return true;
    }

    //模試データヘッダ配列
    public function getMockHeadArray()
    {
        $header = array("YEAR"                  => "※年度",
                        "MOCKCD"                => "※模試コード",
                        "SCHREGNO"              => "※学籍番号",
                        "MOCK_SUBCLASS_CD"      => "※科目区分",
                        "GRADE"                 => "学年",
                        "HR_CLASS"              => "クラス",
                        "ATTENDNO"              => "出席番号",
                        "NAME_KANA"             => "氏名かな",
                        "SEX"                   => "性別",
                        "FORMNO"                => "模試用紙番号",
                        "EXAMNO"                => "受験番号",
                        "SCHOOLCD"              => "高校コード",
                        "COURSEDIV"             => "コース区分",
                        "STATE_EXAM"            => "受験生状態",
                        "EXECUTION_DAY"         => "模試実施日",
                        "SCHEDULE"              => "日程",
                        "TOTALWISHRANK"         => "総志望席次",
                        "TOTALWISHCNT"          => "総志望人数",
                        "JUDGEEVALUATION"       => "判定評価",
                        "JUDGEVALUE"            => "判定数値",
                        "SUBCLASS_NAMECD"       => "科目/大学コード",
                        "SUBCLASS_NAME"         => "科目/大学名",
                        "POINT_CONVERSION"      => "配点/換算得点",
                        "SCORE"                 => "※得点",
                        "DEVIATION"             => "偏差値",
                        "EVALUATION_S"          => "評セ",
                        "EVALUATION_N"          => "評ニ",
                        "EVALUATION_P"          => "評ポ",
                        "EVALUATION_T"          => "評総",
                        "RANK"                  => "席次",
                        $this->lastColumn       => $this->lastColumn);
        return $header;
    }

    //模試データヘッダ例
    public function getMockExample()
    {
        $header = array("YEAR"                  => "9999",
                        "MOCKCD"                => "99999999",
                        "SCHREGNO"              => "99999999",
                        "MOCK_SUBCLASS_CD"      => "999999",
                        "GRADE"                 => "99",
                        "HR_CLASS"              => "999",
                        "ATTENDNO"              => "999",
                        "NAME_KANA"             => "120バイト",
                        "SEX"                   => "男",
                        "FORMNO"                => "999999",
                        "EXAMNO"                => "9999999",
                        "SCHOOLCD"              => "999999",
                        "COURSEDIV"             => "文",
                        "STATE_EXAM"            => "現",
                        "EXECUTION_DAY"         => "9999/99/99",
                        "SCHEDULE"              => "99",
                        "TOTALWISHRANK"         => "999999",
                        "TOTALWISHCNT"          => "99999",
                        "JUDGEEVALUATION"       => "A",
                        "JUDGEVALUE"            => "99999",
                        "SUBCLASS_NAMECD"       => "9999999999",
                        "SUBCLASS_NAME"         => "30バイト",
                        "POINT_CONVERSION"      => "999",
                        "SCORE"                 => "9999",
                        "DEVIATION"             => "999.9",
                        "EVALUATION_S"          => "評",
                        "EVALUATION_N"          => "評",
                        "EVALUATION_P"          => "評",
                        "EVALUATION_T"          => "評",
                        "RANK"                  => "9999999",
                        $this->lastColumn       => $this->lastColumn);
        return $header;
    }

    //目標値データヘッダ配列
    public function getTargetHeadArray()
    {
        $header = array("TARGET_DIV"            => "※目標値区分",
                        "STF_AUTH_CD"           => "※職員/権限コード",
                        "TARGETCD"              => "※目標値コード",
                        "MOCK_SUBCLASS_CD"      => "※科目区分",
                        "SUBCLASS_NAMECD"       => "科目/大学コード",
                        "SUBCLASS_NAME"         => "科目/大学名",
                        "POINT_CONVERSION"      => "配点/換算得点",
                        "SCORE"                 => "※得点",
                        "DEVIATION"             => "偏差値",
                        $this->lastColumn       => $this->lastColumn);
        return $header;
    }

    //目標値データヘッダ例
    public function getTargetExample()
    {
        $header = array("TARGET_DIV"            => "9",
                        "STF_AUTH_CD"           => "99999999",
                        "TARGETCD"              => "99999999",
                        "MOCK_SUBCLASS_CD"      => "999999",
                        "SUBCLASS_NAMECD"       => "9999999999",
                        "SUBCLASS_NAME"         => "30バイト",
                        "POINT_CONVERSION"      => "999",
                        "SCORE"                 => "9999",
                        "DEVIATION"             => "999.9",
                        $this->lastColumn       => $this->lastColumn);
        return $header;
    }

    //総点データヘッダ配列
    public function getMockHeadSoutenArray()
    {
        $header = array("YEAR"              => "※年度",
                        "MOCKCD"            => "※模試コード",
                        "SCHREGNO"          => "※学籍番号",
                        "GRADE"             => "学年",
                        "HR_CLASS"          => "クラス",
                        "ATTENDNO"          => "出席番号",
                        "NAME_KANA"         => "氏名かな",
                        "SOUTEN"            => "総点",
                        "SOUTEN_PERCENT"    => "総点％",
                        "RISOUTEN"          => "理総点",
                        "RISOUTEN_PERCENT"  => "理総点％",
                        $this->lastColumn   => $this->lastColumn);
        if ($this->z010Name1 == 'musashinohigashi') {
            $header["SOUTEN"] = "３科合計点";
            $header["SOUTEN_PERCENT"] = "３科偏差値";
            $header["RISOUTEN"] = "５科合計点";
            $header["RISOUTEN_PERCENT"] = "５科偏差値";
        }
        return $header;
    }

    //模試データヘッダ例
    public function getMockSoutenExample()
    {
        $header = array("YEAR"              => "9999",
                        "MOCKCD"            => "999999999",
                        "SCHREGNO"          => "99999999",
                        "GRADE"             => "99",
                        "HR_CLASS"          => "999",
                        "ATTENDNO"          => "999",
                        "NAME_KANA"         => "120バイト",
                        "SOUTEN"            => "999",
                        "SOUTEN_PERCENT"    => "999.9",
                        "RISOUTEN"          => "999",
                        "RISOUTEN_PERCENT"  => "999.9",
                        $this->lastColumn   => $this->lastColumn);
        return $header;
    }
}
