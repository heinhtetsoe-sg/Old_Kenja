<?php

require_once('for_php7.php');

require_once("PrimarySchoolProcess.php");

/**
 * 定数定義
*/
class knjx101Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $testTable;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx101Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;       //登録者コード
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
                             "YEAR_SEME"        =>  VARS::post("YEAR_SEME"),        //年度＆学期
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト種目
                             "EXECUTEDATE"      =>  VARS::post("EXECUTEDATE"),      //実施日付一覧
                             "CHAIRCD"          =>  VARS::post("CHAIRCD"),          //講座一覧
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "CHECK_DATADIV"    =>  VARS::post("CHECK_DATADIV"),    //DATADIVチェックボックス
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();      //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }

        //初期値の設定
        if (strlen($this->Properties["useTestCountflg"])) {
            $this->testTable = $this->Properties["useTestCountflg"];
        } else {
            $this->testTable = "TESTITEM_MST_COUNTFLG_NEW";
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
        //ファイル取り込み
        $this->dataArr = array();
        $this->dataArr = $this->fileUpload();

        //データの整合性チェック
        if ($this->isDataMatched($this->dataArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    //テスト時間割講座データ
                    $result = $db->query(knjx101Query::getExistCheck($this->dataArr[$i]));
                    $flg1 = false;
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $flg1 = true;
                    }
                    //時間割講座テスト
                    $query = $db->getOne(knjx101Query::getExistCheck2($this->dataArr[$i]));
                    $flg2 = false;
                    if ($query) {
                        $flg2 = true;
                    }
                    //更新
                    if ($this->field["SHORI_MEI"] == "1") {
                        if ($flg1) {   //ＤＢにあれば、update
                            knjx101Query::updateQueryCsv($db, $this->dataArr[$i], $this->staffcd);
                        } else {    //ＤＢになければ、insert
                            knjx101Query::insertQueryCsv($db, $this->dataArr[$i], $this->staffcd);
                        }

                        if ($flg2) {   //ＤＢにあれば、update
                            knjx101Query::updateQueryCsv2($db, $this->dataArr[$i], $this->staffcd);
                        } else {    //ＤＢになければ、insert
                            knjx101Query::insertQueryCsv2($db, $this->dataArr[$i], $this->staffcd);
                        }

                        //削除
                    } else {
                        //ＤＢにあれば、delete
                        if ($flg1) {
                            knjx101Query::deleteQueryCsv($db, $this->dataArr[$i]);
                        }
                        if ($flg2) {
                            knjx101Query::deleteQueryCsv2($db, $this->dataArr[$i]);
                        }
                        //関連テーブルの削除
                        knjx101Query::deleteQueryCsvOthers($db, $this->dataArr[$i]);
                    }
                }
                if ($this->Properties["usePrimarySchoolProcess"] == "1") {
                    //UNIT_SCH_CHR_RANK_DATの更新
                    PrimarySchoolProcess::delInsUnitSchChrRankDat($db, CTRL_YEAR);
                    PrimarySchoolProcess::delInsUnitSchChrRankDat($db, (CTRL_YEAR + 1));
                }
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
            }
        }
    }

    //取込
    public function fileUpload()
    {
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $checkdata = array("EXECUTEDATE" => array(10, "実施日付"),
                               "PERIODCD"    => array(1, "校時コード"),
                               "CHAIRCD"     => array(7, "講座コード"),
                               "TESTKINDCD"  => array(2, "テスト種別"),
                               "TESTITEMCD"  => array(2, "テスト項目"));
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $csv_processing = true;

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $i = 0;
            $record_no = 1;         //行番号(初期値)
            $primaryKey1 = array();
            $primaryKey2 = array();
            $primaryKey3 = array();
            $primaryKey4 = array();
            $primaryKey5 = array();
            $primaryKey6 = array();
            $primaryKey7 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
            $duplicateCsv4 = array();
            $duplicateCsv5 = array();
            $duplicateCsv6 = array();
            $duplicateCsv7 = array();
            $primaryKey1 = $this->getPrimaryKey('SCH_CHR_DAT');
            $primaryKey2 = $this->getPrimaryKey('SCH_CHR_TEST');
            $primaryKey3 = $this->getPrimaryKey('SCH_CHR_COUNTFLG');
            $primaryKey4 = $this->getPrimaryKey('SCH_CHR_HRATE_DAT');
            $primaryKey5 = $this->getPrimaryKey('SCH_STF_DAT');
            $primaryKey6 = $this->getPrimaryKey('SCH_FAC_DAT');
            $primaryKey7 = $this->getPrimaryKey('ATTEND_DAT');

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");        //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                       //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                             //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"] == "on" && $record_no < 2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");     //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    $data = array("EXECUTEDATE" => trim($tmp[0]),
                                  "PERIODCD"    => trim($tmp[1]),
                                  "CHAIRCD"     => trim($tmp[2]),
                                  "TESTKINDCD"  => trim($tmp[3]),
                                  "TESTITEMCD"  => trim($tmp[4]),
                                  "YEAR"        => "0",             //実施日付より年度を算出しセットする
                                  "SEMESTER"    => "0");            //実施日付より学期を算出しセットする
                    $data_check = $this->dateRep($data["EXECUTEDATE"]);
                    if ($data_check) {
                        $data["EXECUTEDATE"] = $data_check;
                    }
                    //実施日付より年度・学期を算出しセットする
                    if ($data["EXECUTEDATE"] != "" && $data_check) {
                        $data["YEAR"]       = $db->getOne("VALUES(Fiscalyear(date('".$data["EXECUTEDATE"]."')))");
                        $data["SEMESTER"]   = $db->getOne("VALUES(Y2t(date('".$data["EXECUTEDATE"]."'),'N'))");
                    }

                    //０埋め
                    $data = $this->zeroUme($data);
                    //エラーチェック
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $record_no, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $duplicateCsv4, $duplicateCsv5, $duplicateCsv6, $duplicateCsv7, $primaryKey1, $primaryKey2, $primaryKey3, $primaryKey4, $primaryKey5, $primaryKey6, $primaryKey7)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$record_no][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$record_no][$val] = $data[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$record_no][$val] = $data[$val];
                    }
                    foreach ($primaryKey4 as $key => $val) {
                        $duplicateCsv4[$record_no][$val] = $data[$val];
                    }
                    foreach ($primaryKey5 as $key => $val) {
                        $duplicateCsv5[$record_no][$val] = $data[$val];
                    }
                    foreach ($primaryKey6 as $key => $val) {
                        $duplicateCsv6[$record_no][$val] = $data[$val];
                    }
                    foreach ($primaryKey7 as $key => $val) {
                        $duplicateCsv7[$record_no][$val] = $data[$val];
                    }
                }//ifの綴り
                $record_no++;   //行番号(カウント)
            }//whileの綴り
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        } else {
            $this->warning = "ファイル名が不正です。";
        }
        return $tempArr;
    }

   //エラーチェック
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $duplicateCsv4, $duplicateCsv5, $duplicateCsv6, $duplicateCsv7, $primaryKey1, $primaryKey2, $primaryKey3, $primaryKey4, $primaryKey5, $primaryKey6, $primaryKey7)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数値エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $dateErrFlg         = false;    //日付エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ

        $hissuErrMsg        = "";     //必須エラーメッセージ
        $numericErrMsg      = "";     //数値エラーメッセージ
        $lengthErrMsg       = "";     //レングスエラーメッセージ
        $masterErrMsg       = "";     //マスタエラーメッセージ
        $existenceErrMsg    = "";     //存在エラーメッセージ
        $dateErrMsg         = "";     //日付エラーメッセージ
        $duplicateCsvErrMsg = "";     //重複(CSV)メッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $existenceSep    = "";
        $dateSep         = "";
        $duplicateCsvSep = "";

        $data_check = $this->dateRep($data["EXECUTEDATE"]);
        if ($data["EXECUTEDATE"] != "" && $data_check && $data["PERIODCD"] != "" && $data["CHAIRCD"]) {
            $result = $db->query(knjx101Query::getExistCheck($data));
            $flg1 = false;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $flg1 = true;
            }
            $query = $db->getOne(knjx101Query::getExistCheck2($data));
            $flg2 = false;
            if ($query) {
                $flg2 = true;
            }
        }
        if ($this->field["SHORI_MEI"] == "1") {
            //重複(CSV)チェック
            $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
            if ($dupFlg1[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
            }
            $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
            if ($dupFlg2[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
            }
        } else {
            $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data);
            if ($dupFlg3[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg3[1] ."行目)");
            }
            $dupFlg4 = $this->duplicateCsvCheck($duplicateCsv4, $primaryKey4, $data);
            if ($dupFlg4[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg4[1] ."行目)");
            }
            $dupFlg5 = $this->duplicateCsvCheck($duplicateCsv5, $primaryKey5, $data);
            if ($dupFlg5[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg5[1] ."行目)");
            }
            $dupFlg6 = $this->duplicateCsvCheck($duplicateCsv6, $primaryKey6, $data);
            if ($dupFlg6[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複4(". $dupFlg6[1] ."行目)");
            }
            $dupFlg7 = $this->duplicateCsvCheck($duplicateCsv7, $primaryKey7, $data);
            if ($dupFlg7[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複5(". $dupFlg7[1] ."行目)");
            }
            if ($flg1) {
                $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
                if ($dupFlg1[0] == 1) {
                    $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複6(". $dupFlg1[1] ."行目)");
                }
            }
            if ($flg2) {
                $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
                if ($dupFlg2[0] == 1) {
                    $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複7(". $dupFlg2[1] ."行目)");
                }
            }
        }
        foreach ($checkdata as $key => $val) {
            //必須チェック
            if (in_array($key, array("EXECUTEDATE", "PERIODCD", "CHAIRCD", "TESTKINDCD", "TESTITEMCD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
            //桁数チェック
            if (in_array($key, array("CHAIRCD", "TESTKINDCD", "TESTITEMCD"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
        }
        if ($returnFlg) {
            if ($data["EXECUTEDATE"] != "") {
                if (!$data_check) {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, "実施日付");
                }
            }
            if ($this->field["SHORI_MEI"] == "1") {
                //職名マスタ
                if ($data["EXECUTEDATE"] != "" && $data_check && $data["CHAIRCD"] != "") {
                    $result = $db->getOne(knjx101Query::getMasterCheck($data, $this, "1"));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "講座コード");
                    }
                }
                if ($data["EXECUTEDATE"] != "" && $data_check && $data["TESTKINDCD"] != "" && $data["TESTITEMCD"] != "") {
                    $result = $db->getOne(knjx101Query::getMasterCheck($data, $this, "2"));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "テスト種別/テスト項目");
                    }
                }
                if ($data["PERIODCD"] != "") {
                    $result = $db->getOne(knjx101Query::getMasterCheck($data, $this, "3"));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "校時コード");
                    }
                }
            }
        }
        if ($returnFlg) {
            if ($data["EXECUTEDATE"] != "" && $data_check && $data["PERIODCD"] != "" && $data["CHAIRCD"] != "") {
                $result = $db->query(knjx101Query::getExistCheck($data));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["SHORI_MEI"] == "1") {
                        if ($row["EXECUTED"] == "1") {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "更新不可");
                        }
                    }
                    //データ区分のチェック
                    if ($row["DATADIV"] != "2" && $this->field["CHECK_DATADIV"] != "on") {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "通常時間割あり");
                    }
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$dateErrMsg.$existenceErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx101Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        $header = array("EXECUTEDATE"       => "※実施日付",
                        "PERIODCD"          => "※校時コード",
                        "CHAIRCD"           => "※講座コード",
                        "TESTKINDCD"        => "※テスト種別",
                        "TESTITEMCD"        => "※テスト項目",
                        "TESTITEMNAME"      => "テスト名称",
                        $this->lastColumn   => $this->lastColumn);

        $header_rei = array("EXECUTEDATE"     => str_replace("-", "/", CTRL_DATE),
                            "PERIODCD"        => "4",
                            "CHAIRCD"         => "1234567",
                            "TESTKINDCD"      => "01",
                            "TESTITEMCD"      => "01",
                            "TESTITEMNAME"    => "中間テスト",
                            $this->lastColumn => $this->lastColumn);

        $contents  = "";    //クリア

        if ($this->field["OUTPUT"] == "1") {
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {     //エラー
            $headerDuplicate = array("EXECUTEDATE" => "※実施日付",
                                     "PERIODCD"    => "※校時コード",
                                     "CHAIRCD"     => "※講座コード",
                                     "GRADE"       => "学年",
                                     "HR_CLASS"    => "組",
                                     "STAFFCD"     => "職員コード",
                                     "FACCD"       => "施設コード",
                                     "SCHREGNO"    => "学籍番号",
                                     "ATTENDDATE"  => "出欠日付");
            $tables = array();
            if ($this->field["SHORI_MEI"] == "1") {
                $tables = array("1" => "SCH_CHR_DAT", "2" => "SCH_CHR_TEST");
            } else {
                $tables = array("1" => "SCH_CHR_COUNTFLG", "2" => "SCH_CHR_HRATE_DAT", "3" => "SCH_STF_DAT", "4" => "SCH_FAC_DAT", "5" => "ATTEND_DAT", "6" => "SCH_CHR_DAT", "7" => "SCH_CHR_TEST");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents = implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {     //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }
        if ($this->field["OUTPUT"] == "1") {
            $header_data = true;
        } else {                                //ヘッダ以外
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            $table = "";
            $where = "";
            if ($this->field["OUTPUT"] == "3") {
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {     //データ
                $query = knjx101Query::getselectDataSQL($this);
            }
            $limit_flg = 0;     //行数上限65536
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $limit_flg++;
                if ($limit_flg > 65535) {
                    break;
                }
                if ($this->field["OUTPUT"] == "4") {    //データ
                    //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace(",", ".", $row);
                    $row["REGISTERCD"] = $this->lastColumn;
                }//if
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }//while
            $result->free();
            Query::dbCheckIn($db);
        }
        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {    //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "テスト時間割講座データ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //日付変換 項目チェック後に行う。
    public function dateRep($data)
    {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }

    //０埋め 項目チェック後に行う。
    public function zeroUme($data)
    {
        if ($data["CHAIRCD"] != "") {
            if (strlen($data["CHAIRCD"]) < 7) {
                $data["CHAIRCD"] = sprintf("%07d", $data["CHAIRCD"]);
            }
        }
        if ($data["TESTKINDCD"] != "") {
            if (strlen($data["TESTKINDCD"]) < 2) {
                $data["TESTKINDCD"] = sprintf("%02d", $data["TESTKINDCD"]);
            }
        }
        if ($data["TESTITEMCD"] != "") {
            if (strlen($data["TESTITEMCD"]) < 2) {
                $data["TESTITEMCD"] = sprintf("%02d", $data["TESTITEMCD"]);
            }
        }

        return $data;
    }

    //データの整合性チェック
    public function isDataMatched(&$dataArr)
    {
        //１万件を超えたら制限エラー
        if (get_count($dataArr) > 10000) {
            $this->setWarning(MSG915, "処理件数を１万件以下にして下さい 処理件数：".get_count($dataArr)."件");
            return false;
        }
        return true;
    }
}
