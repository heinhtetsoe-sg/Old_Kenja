<?php

require_once('for_php7.php');

class knjd128xModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $schoolCd;
    var $auth;      //権限

    function knjd128xModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),                                                                   //出力取込種別
                             "SCHOOL_KIND"      =>  (VARS::post("SCHOOL_KIND")) ? VARS::post("SCHOOL_KIND") : SCHOOLKIND,                   //校種一覧
                             "YEAR_SEMES"       =>  (VARS::post("YEAR_SEMES")) ? VARS::post("YEAR_SEMES") : CTRL_YEAR."-".CTRL_SEMESTER,    //年度一覧
                             "TRGTGRADE"        =>  VARS::post("TRGTGRADE"),                                                                //学年一覧
                             "HEADER"           =>  VARS::post("HEADER"),                                                                   //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI"),                                                                //処理名
                             "TESTKIND_ITEMCD1" =>  VARS::post("TESTKIND_ITEMCD1"),                                                         //テスト種別(ヘッダ出力)
                             "TESTKIND_ITEMCD2" =>  VARS::post("TESTKIND_ITEMCD2"),                                                         //テスト種別(取込）
                             "TESTKIND_ITEMCD4" =>  VARS::post("TESTKIND_ITEMCD4"),                                                         //テスト種別(出力)
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),                                                               //科目
                             "CHAIRCD"          =>  VARS::post("CHAIRCD"),                                                                   //講座
                             "NENDO_GAKKI"      =>  VARS::post("NENDO_GAKKI"),                                                             //csvタイトル用の年度・学期ラベル
                             "SEMESTER_GAKKI"      =>  VARS::post("SEMESTER_GAKKI"),                                                             //csvタイトル用の年度・学期ラベル
                             "SEMESTER"         =>  VARS::post("SEMESTER")                                                               //csvタイトル用の年度・学期ラベル
                             );

        //「全て出力」を選択したコンボのvalue値リスト
        $this->testKindValList    = VARS::post("HID_TESTKIND_ITEMCD") ? explode(",", VARS::post("HID_TESTKIND_ITEMCD")) : array();
        $tmpArr                   = VARS::post("HID_TESTKIND_ITEMCD_LABEL") ? explode(",", VARS::post("HID_TESTKIND_ITEMCD_LABEL")) : array();
        foreach ($tmpArr as $key_label) {
            list($key, $label) = explode(":", $key_label);
            $this->testKindLabelList[$key] = $label;
        }
        $this->trgtGradeList      = VARS::post("HID_TRGTGRADE") ? explode(",", VARS::post("HID_TRGTGRADE")) : array();
        $this->chairCdList        = VARS::post("HID_CHAIRCD") ? explode(",", VARS::post("HID_CHAIRCD")) : array();

        if (!$this->testKindValList) {
            $index = $this->field["OUTPUT"];
            $this->testKindValList[] = $this->field["TESTKIND_ITEMCD".$index];
        }
        if (!$this->trgtGradeList) {
            $this->trgtGradeList[] = $this->field["TRGTGRADE"];
        }
        if (!$this->chairCdList) {
            $this->chairCdList[] = $this->field["CHAIRCD"];
        }

        //学期取得
        $maxSemes = knjd128xQuery::getMaxSemes();
        list($this->recordYear,  $this->recordSemester) = explode("-", $this->field["YEAR_SEMES"]);
        $this->alterSemester = ($this->recordSemester == '9') ? $maxSemes : $this->recordSemester;  //講座は学期末に存在しないため最終学期で代替する

        $this->adminSdivExists = ($this->adminSdivExists) ? $this->adminSdivExists : 0 ;

        $this->grade = substr($this->field["GRADE"], 0, 2);

        $this->cmd  = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"]) {
            $this->getSelectSchoolKind();
        }
        $this->useTestCountflg = ($this->Properties["useTestCountflg"]) ? $this->Properties["useTestCountflg"] : 'TESTITEM_MST_COUNTFLG_NEW';
        $this->file = VARS::file("FILE");

    }

    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */

    //CSV取り込みの際に既にデータが存在するかチェック
    function &getCsvUpdateCheck() {
        
    }

    function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();
        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjd128xQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjd128xQuery::DeleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $checkdata = array();
            if ($this->Properties["useCurriculumcd"] == '1') {
                $checkdata["SUBCLASSCD"]    = array(13,"科目コード");
            } else {
                $checkdata["SUBCLASSCD"]    = array(6,"科目コード");
            }
            $checkdata["CHAIRCD"]           = array(7,"講座コード");
            $checkdata["SCHREGNO"]          = array(8,"学籍番号");

            if (get_count($this->testKindValList) > 0) {
                for ($j = 0; $j < get_count($this->testKindValList); $j++) {
                    $testcd = $this->testKindValList[$j];
                    $checkdata["TEST_SCORE_".$testcd] = array(3, $this->testKindLabelList[$testcd]);
                }
            }

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;

            //取得主キー
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('RECORD_SCORE_DAT');
            while (!feof ($fp))
            {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '')
                {
                    if (!$header && $this->field["HEADER"] == "on"){
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    $data = array();
                    $data["SUBCLASSCD"]             = (strlen(trim($tmp[0]))) ? $tmp[0] : "";
                    $data["CHAIRCD"]                = (strlen(trim($tmp[2]))) ? sprintf("%07d", $tmp[2]) : "";
                    $data["SCHREGNO"]               = (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "";
                    $tmpCnt = 7;
                    if (get_count($this->testKindValList) > 0) {
                        for ($j = 0; $j < get_count($this->testKindValList); $j++) {
                            $data["TEST_SCORE_".$this->testKindValList[$j]] = (strlen(trim($tmp[$j + $tmpCnt]))) ? $tmp[$j + $tmpCnt] : "";
                        }
                    }
                    $data["CLASSCD"]          = "";
                    $data["SCHOOL_KIND"]      = "";
                    $data["CURRICULUM_CD"]    = "";
                    $data["TESTKINDCD"]       = "";
                    $data["TESTITEMCD"]       = "";
                    $data["SCORE_DIV"]        = "";
                    $data["YEAR"]             = CTRL_YEAR;
                    $data["SEMESTER"]         = $this->field["SEMESTER"];
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
            $db->commit();  // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }
        return $temp_arr;
    }

    //エラーチェック
    function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $duplicateCsvErrFlg = false; //重複(CSV)エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $duplicateCsvErrMsg = "";    //重複(CSV)メッセージ

        $hissuSep     = "";
        $numericSep   = "";
        $lengthSep    = "";
        $existenceSep = "";
        $duplicateCsvSep = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key,array("SUBCLASSCD", "CHAIRCD", "SCHREGNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key,array("CHAIRCD", "SCHREGNO"))) {
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

        //存在チェック（在籍)
        if (1 > $db->getOne(knjd128xQuery::checkRegdDat($this->recordYear, $this->alterSemester, $data["SCHREGNO"]))) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $data_each["SCHREGNO"][1]);
        }

        //存在チェック（科目)
        if (1 > $db->getOne(knjd128xQuery::checkSubclassMst($this, $data["SUBCLASSCD"]))) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $data_each["SUBCLASSCD"][1]);
        }

        //存在チェック（講座)
        if (1 > $db->getOne(knjd128xQuery::checkChairDat($this->recordYear, $this->alterSemester, $data["CHAIRCD"]))) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $data_each["CHAIRCD"][1]);
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjd128xQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();
        $header = $header_rei = array();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /**************/
        /*  ヘッダー  */
        /**************/
        $header["SUBCLASSCD"]      = "※科目コード";
        $header["SUBCLASSNAME"]    = "科目名";
        $header["CHAIRCD"]         = "※講座コード";
        $header["CHAIRNAME"]       = "講座名";
        $header["SCHREGNO"]        = "※学籍番号";
        $header["HR_ATTENDNO"]     = "クラス-出席番号";
        $header["NAME"]            = "氏名";

        for ($j = 0; $j < get_count($this->testKindValList); $j++) {
            $testcd = $this->testKindValList[$j];
            $header["TEST_SCORE_".$testcd] = $this->testKindLabelList[$testcd];                 //(例) $header["TEST_SCORE_010101"]  = "1学期中間素点"
        }

        /****************/
        /*  ヘッダー例  */
        /****************/
        $header_rei["SUBCLASSCD"]      = ($this->Properties["useCurriculumcd"] == "1") ? "01-H-1-010001" : "010001";
        $header_rei["SUBCLASSNAME"]    = "国語";
        $header_rei["CHAIRCD"]         = "1101011";
        $header_rei["CHAIRNAME"]       = "実用国語";
        $header_rei["SCHREGNO"]        = "12345678";
        $header_rei["HR_ATTENDNO"]     = "1年1組-01";
        $header_rei["NAME"]            = "アルプ太郎";

        for ($j = 0; $j < get_count($this->testKindValList); $j++) {
            $header_rei["TEST_SCORE_".$this->testKindValList[$j]] = "";             //(例) $header_rei["TEST_SCORE_010101"]  = "1学期中間素点"
        }

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            // ヘッダ出力
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } else if ($this->field["OUTPUT"] == "3") {
            // エラー出力
            $headerDuplicate = array("YEAR"                 => "年度",
                                    "SEMESTER"              => "学期",
                                    "TESTKINDCD"            => "テスト種別コード",
                                    "TESTITEMCD"            => "テスト項目コード",
                                    "SCORE_DIV"             => "区分",
                                    "CLASSCD"               => "教科コード",
                                    "SCHOOL_KIND"           => "学校校種",
                                    "CURRICULUM_CD"         => "教育課程コード",
                                    "SUBCLASSCD"            => "科目コード",
                                    "SCHREGNO"              => "学籍番号");
            $tables = array();
            $tables = array("1" => "RECORD_SCORE_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } else if ($this->field["OUTPUT"] == "4") {
            // データ出力
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        if ($this->field["OUTPUT"] != "1") {
            $query = "";
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } else if ($this->field["OUTPUT"] == "4") {
                $query = knjd128xQuery::selectMainQuery($this);
            }

            //SQL文発行
            if ($query) {
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["OUTPUT"] == "4") {     //データ
                        $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                        $array = $header;
                    } else {
                        $array = $header_err;
                    }

                    //ヘッダ順に値をセット
                    $sep = "";
                    foreach ($array as $field => $label) {
                        $contents .= $sep.$row[$field];
                        $sep = ",";
                    }
                    $contents .= "\n";

                    $header_data = true;
                }
                $result->free();
            }
        }

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //ファイル名
        if ($this->field["OUTPUT"] == "4") {
            $title = $this->field["NENDO_GAKKI"].' 成績データ';        
        } else {
            $title = CTRL_YEAR.'年度 '.$this->field["SEMESTER_GAKKI"].' 成績データ';
        }
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        //エラー出力
        if ($this->field["OUTPUT"] == "3") {
            $filename = sprintf("%s.csv", "エラー出力");
        //エラー出力以外
        } else {
            $filename = sprintf("%s.csv", $title);
        }

        if (!$header_data && $this->field["OUTPUT"] == "3") {
            $this->warning = "該当データは、ありません。";
            return false;
        }

        //データ出力時にデータが存在しなければ空行を追加
        if (!$header_data && $this->field["OUTPUT"] == "4") {
            $tmp = array_fill(0, get_count($header), '');
            $contents .= implode(',', $tmp)."\n";
        }

        common::downloadContents($contents, $filename);
        return true;
    }
}
?>
