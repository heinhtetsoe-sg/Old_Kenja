<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjh455Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjh455Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "COMPANY"          =>  "00000002",          //模試業者
                             //"CSV_DIV"          =>  VARS::post("CSV_DIV"),          //データ区分
                             "MOCKCD"           =>  VARS::post("MOCKCD"),           //模試
                             "GRADE"            =>  VARS::post("GRADE"),            //学年一覧
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        $this->file = VARS::file("FILE");
    }
    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
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
                    knjh455Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjh455Query::DeleteQueryCsv($this, $this->data_arr);
                }
            }
            $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
        }
    }

    //取込
    function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {

            $checkdata["YEAR"]      = array(4, "年度");
            $checkdata["GAKKOCODE"] = array(5, "学校コード");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            //MOCK_SCHOOL_COMP_DATのIMPORT_NOの最大値をとる
            $impnoQuery = knjh455Query::getImportNo();
            $impMax = $db->getOne($impnoQuery);
            $importMax = (int)$impMax + 1;

            //ROW_NO用
            $rowNo = 1;

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            if ($this->field["HEADER"] == "on") {
                $header = false;
            } else {
                $header = true;
            }
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey("MOCK_SCHOOL_COMP_DAT");
            while (!feof($fp)) {
                $buffer = fgets($fp, 8192);
                if (trim($buffer) != '') {
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    if (!$header) {
                        $header = true;
                        $line++;
                        continue;
                    }
                    $tmpCnt = 0;
                    $data = array();

                    $data["IMPORT_NO"]      = $importMax;
                    $data["ROW_NO"]         = $rowNo;
                    $data["YEAR"]           = $tmp[$tmpCnt++];
                    $data["GAKKONAME"]      = $tmp[$tmpCnt++];
                    $data["GAKKOCODE"]      = $tmp[$tmpCnt++];
                    $tmpCnt++;
                    $data["GRADE"]          = $this->field["GRADE"];
                    $data["MOCKCD"]         = $this->field["MOCKCD"];
                    $data["MOCKNAME"]       = $tmp[$tmpCnt++];
                    $data["SUBCLASS_CD"]    = "";
                    $data["SUBCLASS_NAME"]  = $tmp[$tmpCnt++];
                    $data["EXAM_COUNT"]     = $tmp[$tmpCnt++];
                    $data["AVERAGE"]        = $tmp[$tmpCnt++];
                    $data["STD_DEVIATION"]  = $tmp[$tmpCnt++];
                    $data["AVG_DEVIATION"]  = $tmp[$tmpCnt++];
                    $data["PERFECT"]        = $tmp[$tmpCnt++];
                    $data["SIMPLE_80"]      = $tmp[$tmpCnt++];
                    $data["SIMPLE_75"]      = $tmp[$tmpCnt++];
                    $data["SIMPLE_70"]      = $tmp[$tmpCnt++];
                    $data["SIMPLE_65"]      = $tmp[$tmpCnt++];
                    $data["SIMPLE_60"]      = $tmp[$tmpCnt++];
                    $data["SIMPLE_55"]      = $tmp[$tmpCnt++];
                    $data["SIMPLE_50"]      = $tmp[$tmpCnt++];
                    $data["SIMPLE_45"]      = $tmp[$tmpCnt++];
                    $data["SIMPLE_40"]      = $tmp[$tmpCnt++];
                    $data["SIMPLE_35"]      = $tmp[$tmpCnt++];
                    $data["SIMPLE_30"]      = $tmp[$tmpCnt++];
                    $data["SIMPLE_LOW"]     = $tmp[$tmpCnt++];
                    $data["TOTAL_80"]      = $tmp[$tmpCnt++];
                    $data["TOTAL_75"]      = $tmp[$tmpCnt++];
                    $data["TOTAL_70"]      = $tmp[$tmpCnt++];
                    $data["TOTAL_65"]      = $tmp[$tmpCnt++];
                    $data["TOTAL_60"]      = $tmp[$tmpCnt++];
                    $data["TOTAL_55"]      = $tmp[$tmpCnt++];
                    $data["TOTAL_50"]      = $tmp[$tmpCnt++];
                    $data["TOTAL_45"]      = $tmp[$tmpCnt++];
                    $data["TOTAL_40"]      = $tmp[$tmpCnt++];
                    $data["TOTAL_35"]      = $tmp[$tmpCnt++];
                    $data["TOTAL_30"]      = $tmp[$tmpCnt++];
                    $data["TOTAL_LOW"]     = $tmp[$tmpCnt++];

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    $line++;
                    $rowNo++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    //エラーチェック
    function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg       = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $huittiErrFlg    = false;    //年度不一致エラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $huittiErrMsg    = "";       //年度不一致エラーフラグ
        $duplicateCsvErrMsg = "";

        $hissuSep     = "";
        $existenceSep = "";
        $huittiSep    = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $sonzaiarray = array("RELATIONSHIP"             => "H201",
                             "GUARD_SEX"                => "Z002",
                             "GUARD_JOBCD"              => "H202",
                             "GUARANTOR_RELATIONSHIP"   => "H201",
                             "GUARANTOR_SEX"            => "Z002",
                             "GUARANTOR_JOBCD"          => "H202");
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR", "GAKKOCODE"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                if (in_array($key, array("MOCKCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
            } else {
            }
        }
        //選択年度とデータ内のデータが違うときはエラーにする
        if ($data["YEAR"] != substr($data["MOCKCD"], 1, 4)) {
            $returnFlg = $this->errSet($huittiErrFlg, $huittiErrMsg, "※不一致：", $huittiSep, "年度");
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$huittiErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjh455Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    /**
     * ヘッダ取得
     */
    function getCsvHeadData($tmp)
    {
        $tmpCnt = 0;
        $data = array();
        if ($this->field["COMPANY"] == "00000001") {
            $data["YEAR"]           = $tmp[$tmpCnt++];
            $data["MOSI_CD"]        = $tmp[$tmpCnt++];
            $data["MOCKCD"]         = $this->field["MOCKCD"];
            $data["MOSI_NAME"]      = $tmp[$tmpCnt++];
            $data["KANA"]           = $tmp[$tmpCnt++];
            $data["HR_CLASS"]       = $tmp[$tmpCnt++];
            $data["ATTENDNO"]       = $tmp[$tmpCnt++];
            $data["EXAMNO"]         = $tmp[$tmpCnt++];
            $data["SCHOOL_CD"]      = $tmp[$tmpCnt++];
            $data["SCHOOL_EDA"]     = $tmp[$tmpCnt++];
            $data["SCHOOL_NAME"]    = $tmp[$tmpCnt++];
            $data["BUNRI_DIV"]      = $tmp[$tmpCnt++];
            $data["SEX"]            = $tmp[$tmpCnt++];
            $data["GRADE"]          = $tmp[$tmpCnt++];
            $data["GEN_SOTU"]       = $tmp[$tmpCnt++];
            $data["KAMOKU_EIGO"]    = $tmp[$tmpCnt++];
            $data["KAMOKU_SUUGAKU"] = $tmp[$tmpCnt++];
            $data["KAMOKU_KOKUGO"]  = $tmp[$tmpCnt++];
            $data["KAMOKU_RI1"]     = $tmp[$tmpCnt++];
            $data["KAMOKU_RI2"]     = $tmp[$tmpCnt++];
            $data["KAMOKU_REKIKOU1"]= $tmp[$tmpCnt++];
            $data["KAMOKU_REKIKOU2"]= $tmp[$tmpCnt++];
            for ($hCnt = 1; $hCnt <= $this->hopeCnt; $hCnt++) {
                $data["SEQ{$hCnt}"]            = $hCnt;
                $data["SCHOOL_CD{$hCnt}"]      = $tmp[$tmpCnt++];
                $data["SCHOOL_NAME{$hCnt}"]    = $tmp[$tmpCnt++];
                $data["NITTEI{$hCnt}"]         = $tmp[$tmpCnt++];
                $data["RANK{$hCnt}"]           = $tmp[$tmpCnt++];
                $data["CNT{$hCnt}"]            = $tmp[$tmpCnt++];
                $data["JUDGE_HYOUKA{$hCnt}"]   = $tmp[$tmpCnt++];
                $data["JUDGE_SUUTI{$hCnt}"]    = $tmp[$tmpCnt++];
            }
            for ($sCnt = 1; $sCnt <= $this->scoreCnt; $sCnt++) {
                $data["SEQ{$sCnt}"]                = $sCnt;
                $data["MOCK_SUBCLASS_CD{$sCnt}"]   = $this->replaceSubclass[$sCnt];
                $data["SCORE{$sCnt}"]              = $tmp[$tmpCnt++];
                $data["DEVIATION{$sCnt}"]          = $tmp[$tmpCnt++];
                $data["RANK{$sCnt}"]               = $tmp[$tmpCnt++];
            }
            $data["DEVIATION"]      = $tmp[$tmpCnt++];
            $data["RANK"]           = $tmp[$tmpCnt++];
            $data["CNT"]            = $tmp[$tmpCnt++];
        } else if ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "1") {
            $data["YEAR"]       = $tmp[$tmpCnt++];
            $data["KYOUZAICD"]  = $tmp[$tmpCnt++];
            $data["MOCKCD"]     = $this->field["MOCKCD"];
            $data["TYPE"]       = $tmp[$tmpCnt++];
            $data["KYOUZAINAME"]= $tmp[$tmpCnt++];
            $data["GAKKACD"]    = $tmp[$tmpCnt++];
            $data["GAKKANAME"]  = $tmp[$tmpCnt++];
            $data["BENEID"]     = $tmp[$tmpCnt++];
            $data["HR_CLASS"]   = $tmp[$tmpCnt++];
            $data["ATTENDNO"]   = $tmp[$tmpCnt++];
            $data["NAME"]       = $tmp[$tmpCnt++];
            $data["BUNRI_DIV"]  = $tmp[$tmpCnt++];
            $data["BIRTHDAY"]   = $tmp[$tmpCnt++];
            $data["SEX"]        = $tmp[$tmpCnt++];
            for ($sCnt = 1; $sCnt <= $this->scoreCnt; $sCnt++) {
                $data["SEQ{$sCnt}"]                = $sCnt;
                $data["MOCK_SUBCLASS_CD{$sCnt}"]   = $this->replaceSubclass[$sCnt];
                $data["SCORE{$sCnt}"]              = $tmp[$tmpCnt++];
                $data["GTZ{$sCnt}"]                = $tmp[$tmpCnt++];
                $data["ALL_DEV{$sCnt}"]            = $tmp[$tmpCnt++];
                $data["SCHOOL_DEV{$sCnt}"]         = $tmp[$tmpCnt++];
                $data["ALL_RANK{$sCnt}"]           = $tmp[$tmpCnt++];
                $data["SCHOOL_RANK{$sCnt}"]        = $tmp[$tmpCnt++];
            }
            $data["DEVIATION"]  = $tmp[$tmpCnt++];
            $data["RANK"]       = $tmp[$tmpCnt++];
            $data["CNT"]        = $tmp[$tmpCnt++];
        } else if ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "2") {
            $data["YEAR"]       = $tmp[$tmpCnt++];
            $data["KYOUZAICD"]  = $tmp[$tmpCnt++];
            $data["MOCKCD"]     = $this->field["MOCKCD"];
            $data["TYPE"]       = $tmp[$tmpCnt++];
            $data["KYOUZAINAME"]= $tmp[$tmpCnt++];
            $data["GAKKACD"]    = $tmp[$tmpCnt++];
            $data["GAKKANAME"]  = $tmp[$tmpCnt++];
            $data["BENEID"]     = $tmp[$tmpCnt++];
            $data["HR_CLASS"]   = $tmp[$tmpCnt++];
            $data["ATTENDNO"]   = $tmp[$tmpCnt++];
            $data["NAME"]       = $tmp[$tmpCnt++];
            $data["BUNRI_DIV"]  = $tmp[$tmpCnt++];
            $data["BIRTHDAY"]   = $tmp[$tmpCnt++];
            $data["SEX"]        = $tmp[$tmpCnt++];
            for ($hCnt = 1; $hCnt <= $this->hopeCnt; $hCnt++) {
                $data["SEQ{$hCnt}"]            = $hCnt;
                $data["SCHOOL_CD{$hCnt}"]      = $tmp[$tmpCnt++];
                $data["BOSYUTANNI{$hCnt}"]     = $tmp[$tmpCnt++];
                $data["SCHOOL_NAME{$hCnt}"]    = $tmp[$tmpCnt++];
                $data["GAKUBU_NAME{$hCnt}"]    = $tmp[$tmpCnt++];
                $data["GAKKA_NAME{$hCnt}"]     = $tmp[$tmpCnt++];
                $data["NITTEI{$hCnt}"]         = $tmp[$tmpCnt++];
                $data["HOUSIKI{$hCnt}"]        = $tmp[$tmpCnt++];
                $data["ALL_JUDGE{$hCnt}"]      = $tmp[$tmpCnt++];
                $data["JUDGE1{$hCnt}"]         = $tmp[$tmpCnt++];
                $data["JUDGE2{$hCnt}"]         = $tmp[$tmpCnt++];
                $data["ALL_JUDGE_SS{$hCnt}"]   = $tmp[$tmpCnt++];
                $data["JUDGE1_SS{$hCnt}"]      = $tmp[$tmpCnt++];
                $data["JUDGE2_SS{$hCnt}"]      = $tmp[$tmpCnt++];
            }
            $data["DEVIATION"]      = $tmp[$tmpCnt++];
            $data["RANK"]           = $tmp[$tmpCnt++];
            $data["CNT"]            = $tmp[$tmpCnt++];
        } else if ($this->field["COMPANY"] == "00000003") {
            $data["YEAR"]           = $tmp[$tmpCnt++];
            $data["MOSI_CD"]        = $tmp[$tmpCnt++];
            $data["MOCKCD"]         = $this->field["MOCKCD"];
            $data["MOSI_NAME"]      = $tmp[$tmpCnt++];
            $data["HR_CLASS"]       = $tmp[$tmpCnt++];
            $data["ATTENDNO"]       = $tmp[$tmpCnt++];
            $data["KANA"]           = $tmp[$tmpCnt++];
            for ($sCnt = 1; $sCnt <= $this->scoreCnt; $sCnt++) {
                $data["SEQ{$sCnt}"]                = $sCnt;
                $data["MOCK_SUBCLASS_CD{$sCnt}"]   = $this->replaceSubclass[$sCnt];
                $data["SCORE{$sCnt}"]              = $tmp[$tmpCnt++];
                $data["DEVIATION{$sCnt}"]          = $tmp[$tmpCnt++];
            }
            $data["DEVIATION"]  = $tmp[$tmpCnt++];
            $data["RANK"]       = $tmp[$tmpCnt++];
            $data["CNT"]        = $tmp[$tmpCnt++];
        }

        return $data;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;

    }

    //ＣＳＶ出力
    function getDownloadModel()
    {

        //DB接続
        $db = Query::dbCheckOut();

        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = $this->getCsvHead($db);

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "2") {
            $headerDuplicate = array("YEAR"          => "年度",
                                     "GAKKONAME"     => "学校名",
                                     "GAKKOCODE"     => "学校コード",
                                     "GRADE"         => "学年",
                                     "MOCKNAME"      => "模試名称",
                                     "SUBCLASS_NAME" => "科目/大学名");
            $tables = array();
            $tables = array("1" => "MOCK_SCHOOL_COMP_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        }

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();

        $setTitle = $this->field["YEAR"]."年度";
        $query = knjh455Query::getTitleGrade($this);
        $setTitle .= $db->getOne($query);
        $query = knjh455Query::getTitleMockCompany($this);
        $setTitle .= $db->getOne($query);
        $query = knjh455Query::getTitleMockMst($this);
        $setTitle .= $db->getOne($query);
        $query = knjh455Query::getTitleCsvDiv($this);
        $setTitle .= $db->getOne($query);

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "2") {
                $couse = "エラー出力";
            } else {
                $couse = $setTitle;
            }
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

    /**
     * ヘッダ取得
     */
    function getCsvHead($db)
    {
        //希望フィールド数
        $query = knjh455Query::getHopeField($this);
        $this->hopeCnt = $db->getOne($query);

        //得点フィールド数
        $query = knjh455Query::getScoreField($this);
        $this->scoreCnt = $db->getOne($query);

        $retHead = array();
        if ($this->field["COMPANY"] == "00000001") {
            $query = knjh455Query::getCsvTitle($this, "MOCK_CSV_SUNDAI_HEAD_HDAT");
            $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $retHead["YEAR"]           = $setRow["TYEAR"];
            $retHead["MOSI_CD"]        = $setRow["TMOSI_CD"];
            $retHead["MOSI_NAME"]      = $setRow["TMOSI_NAME"];
            $retHead["KANA"]           = $setRow["TKANA"];
            $retHead["HR_CLASS"]       = $setRow["THR_CLASS"];
            $retHead["ATTENDNO"]       = $setRow["TATTENDNO"];
            $retHead["EXAMNO"]         = $setRow["TEXAMNO"];
            $retHead["SCHOOL_CD"]      = $setRow["TSCHOOL_CD"];
            $retHead["SCHOOL_EDA"]     = $setRow["TSCHOOL_EDA"];
            $retHead["SCHOOL_NAME"]    = $setRow["TSCHOOL_NAME"];
            $retHead["BUNRI_DIV"]      = $setRow["TBUNRI_DIV"];
            $retHead["SEX"]            = $setRow["TSEX"];
            $retHead["GRADE"]          = $setRow["TGRADE"];
            $retHead["GEN_SOTU"]       = $setRow["TGEN_SOTU"];
            $retHead["KAMOKU_EIGO"]    = $setRow["TKAMOKU_EIGO"];
            $retHead["KAMOKU_SUUGAKU"] = $setRow["TKAMOKU_SUUGAKU"];
            $retHead["KAMOKU_KOKUGO"]  = $setRow["TKAMOKU_KOKUGO"];
            $retHead["KAMOKU_RI1"]     = $setRow["TKAMOKU_RI1"];
            $retHead["KAMOKU_RI2"]     = $setRow["TKAMOKU_RI2"];
            $retHead["KAMOKU_REKIKOU1"]= $setRow["TKAMOKU_REKIKOU1"];
            $retHead["KAMOKU_REKIKOU2"]= $setRow["TKAMOKU_REKIKOU2"];

            $query = knjh455Query::getCsvSundaiTitle($this, "MOCK_CSV_SUNDAI_HOPE_HEAD_DAT", $setRow["MOSI_CD"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $retHead["SCHOOL_CD{$row["SEQ"]}"]      = $row["TSCHOOL_CD"];
                $retHead["SCHOOL_NAME{$row["SEQ"]}"]    = $row["TSCHOOL_NAME"];
                $retHead["NITTEI{$row["SEQ"]}"]         = $row["TNITTEI"];
                $retHead["RANK{$row["SEQ"]}"]           = $row["TRANK"];
                $retHead["CNT{$row["SEQ"]}"]            = $row["TCNT"];
                $retHead["JUDGE_HYOUKA{$row["SEQ"]}"]   = $row["TJUDGE_HYOUKA"];
                $retHead["JUDGE_SUUTI{$row["SEQ"]}"]    = $row["TJUDGE_SUUTI"];
            }
            $result->free();

            $query = knjh455Query::getCsvSundaiTitle($this, "MOCK_CSV_SUNDAI_SCORE_HEAD_DAT", $setRow["MOSI_CD"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $retHead["SCORE{$row["SEQ"]}"]              = $row["TSCORE"];
                $retHead["DEVIATION{$row["SEQ"]}"]          = $row["TDEVIATION"];
                $retHead["RANK{$row["SEQ"]}"]               = $row["TRANK"];
            }
            $result->free();

            $retHead["DEVIATION"]      = $setRow["TDEVIATION"];
            $retHead["RANK"]           = $setRow["TRANK"];
            $retHead["CNT"]            = $setRow["TCNT"];
        } else if ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "1") {
            $query = knjh455Query::getCsvTitle($this, "MOCK_CSV_BENE_SCORE_HEAD_HDAT");
            $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $retHead["YEAR"]       = $setRow["TYEAR"];
            $retHead["KYOUZAICD"]  = $setRow["TKYOUZAICD"];
            $retHead["TYPE"]       = $setRow["TTYPE"];
            $retHead["KYOUZAINAME"]= $setRow["TKYOUZAINAME"];
            $retHead["GAKKACD"]    = $setRow["TGAKKACD"];
            $retHead["GAKKANAME"]  = $setRow["TGAKKANAME"];
            $retHead["BENEID"]     = $setRow["TBENEID"];
            $retHead["HR_CLASS"]   = $setRow["THR_CLASS"];
            $retHead["ATTENDNO"]   = $setRow["TATTENDNO"];
            $retHead["NAME"]       = $setRow["TNAME"];
            $retHead["BUNRI_DIV"]  = $setRow["TBUNRI_DIV"];
            $retHead["BIRTHDAY"]   = $setRow["TBIRTHDAY"];
            $retHead["SEX"]        = $setRow["TSEX"];

            $query = knjh455Query::getCsvBeneTitle($this, "MOCK_CSV_BENE_SCORE_HEAD_DAT", $setRow["KYOUZAICD"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $retHead["SCORE{$row["SEQ"]}"]              = $row["TSCORE"];
                $retHead["GTZ{$row["SEQ"]}"]                = $row["TGTZ"];
                $retHead["ALL_DEV{$row["SEQ"]}"]            = $row["TALL_DEV"];
                $retHead["SCHOOL_DEV{$row["SEQ"]}"]         = $row["TSCHOOL_DEV"];
                $retHead["ALL_RANK{$row["SEQ"]}"]           = $row["TALL_RANK"];
                $retHead["SCHOOL_RANK{$row["SEQ"]}"]        = $row["TSCHOOL_RANK"];
            }
            $result->free();

            $retHead["DEVIATION"]  = $setRow["TDEVIATION"];
            $retHead["RANK"]       = $setRow["TRANK"];
            $retHead["CNT"]        = $setRow["TCNT"];
        } else if ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "2") {
            $query = knjh455Query::getCsvTitle($this, "MOCK_CSV_BENE_HOPE_HEAD_HDAT");
            $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $retHead["YEAR"]       = $setRow["TYEAR"];
            $retHead["KYOUZAICD"]  = $setRow["TKYOUZAICD"];
            $retHead["TYPE"]       = $setRow["TTYPE"];
            $retHead["KYOUZAINAME"]= $setRow["TKYOUZAINAME"];
            $retHead["GAKKACD"]    = $setRow["TGAKKACD"];
            $retHead["GAKKANAME"]  = $setRow["TGAKKANAME"];
            $retHead["BENEID"]     = $setRow["TBENEID"];
            $retHead["HR_CLASS"]   = $setRow["THR_CLASS"];
            $retHead["ATTENDNO"]   = $setRow["TATTENDNO"];
            $retHead["NAME"]       = $setRow["TNAME"];
            $retHead["BUNRI_DIV"]  = $setRow["TBUNRI_DIV"];
            $retHead["BIRTHDAY"]   = $setRow["TBIRTHDAY"];
            $retHead["SEX"]        = $setRow["TSEX"];

            $query = knjh455Query::getCsvBeneTitle($this, "MOCK_CSV_BENE_HOPE_HEAD_DAT", $setRow["KYOUZAICD"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $retHead["SCHOOL_CD{$row["SEQ"]}"]      = $row["TSCHOOL_CD"];
                $retHead["BOSYUTANNI{$row["SEQ"]}"]     = $row["TBOSYUTANNI"];
                $retHead["SCHOOL_NAME{$row["SEQ"]}"]    = $row["TSCHOOL_NAME"];
                $retHead["GAKUBU_NAME{$row["SEQ"]}"]    = $row["TGAKUBU_NAME"];
                $retHead["GAKKA_NAME{$row["SEQ"]}"]     = $row["TGAKKA_NAME"];
                $retHead["NITTEI{$row["SEQ"]}"]         = $row["TNITTEI"];
                $retHead["HOUSIKI{$row["SEQ"]}"]        = $row["THOUSIKI"];
                $retHead["ALL_JUDGE{$row["SEQ"]}"]      = $row["TALL_JUDGE"];
                $retHead["JUDGE1{$row["SEQ"]}"]         = $row["TJUDGE1"];
                $retHead["JUDGE2{$row["SEQ"]}"]         = $row["TJUDGE2"];
                $retHead["ALL_JUDGE_SS{$row["SEQ"]}"]   = $row["TALL_JUDGE_SS"];
                $retHead["JUDGE1_SS{$row["SEQ"]}"]      = $row["TJUDGE1_SS"];
                $retHead["JUDGE2_SS{$row["SEQ"]}"]      = $row["TJUDGE2_SS"];
            }
            $result->free();
        } else if ($this->field["COMPANY"] == "00000003") {
            $query = knjh455Query::getCsvTitle($this, "MOCK_CSV_ZKAI_HEAD_HDAT");
            $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $retHead["YEAR"]           = $setRow["TYEAR"];
            $retHead["MOSI_CD"]        = $setRow["TMOSI_CD"];
            $retHead["MOSI_NAME"]      = $setRow["TMOSI_NAME"];
            $retHead["HR_CLASS"]       = $setRow["THR_CLASS"];
            $retHead["ATTENDNO"]       = $setRow["TATTENDNO"];
            $retHead["KANA"]           = $setRow["TKANA"];

            $query = knjh455Query::getCsvSundaiTitle($this, "MOCK_CSV_ZKAI_SCORE_HEAD_DAT", $setRow["MOSI_CD"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $retHead["SCORE{$row["SEQ"]}"]              = $row["TSCORE"];
                $retHead["DEVIATION{$row["SEQ"]}"]          = $row["TDEVIATION"];
            }
            $result->free();
            $retHead["DEVIATION"]      = "追加偏差";
            $retHead["RANK"]           = "追加順位";
            $retHead["CNT"]            = "追加人数";
        }
        return $retHead;
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    function date_rep($data)
    {
        if ($data!="") {
            $data = common::DateConv1(str_replace("-","/",$data),4);
            if($data) $data = str_replace("/","-",$data);
        }

        return $data;
    }

}
?>
