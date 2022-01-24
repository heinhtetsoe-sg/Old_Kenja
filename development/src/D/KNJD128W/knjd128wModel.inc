<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjd128wModel extends Model
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
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjd128wModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //登録者コード
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

        $this->field = array("OUTPUT"       =>  VARS::post("OUTPUT"),           //出力取込種別
                             "TESTCD"       =>  VARS::post("TESTCD"),           //テスト種別
                             "HEADER"       =>  VARS::post("HEADER"),           //ヘッダ有
                             "SCHOOL_KIND"  =>  VARS::post("SCHOOL_KIND"),      //校種
                             "SHORI_MEI"    =>  VARS::post("SHORI_MEI")         //処理名
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if ($this->Properties["useSchool_KindField"] != "1") {
            $this->field["SCHOOL_KIND"] = SCHOOLKIND;
        }

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }

        //DB接続
        $db = Query::dbCheckOut();

        //各種設定を取得
        $D067 = array();
        $query = knjd128wQuery::getNameMstSettei("D067", "01");
        $D067 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->noUseSouGaku = $D067["NAMESPARE1"];//1:総学非表示（９０未満を表示とする）

        //DB切断
        Query::dbCheckIn($db);

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
            //更新
                if ($this->field["SHORI_MEI"] == "1") {
                    knjd128wQuery::insertQueryCsv($this, $this->dataArr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
            }
        }
    }

    //取込
    public function fileUpload()
    {
        $tempArr = array();
        //ファイル名取得
        $target = $_FILES['FILE']['name'];
        $fileInfo = pathinfo($target);
        $kakutyousi = $fileInfo['extension'];
        $kakutyousi = strtoupper($kakutyousi);
        //拡張子チェック
        if ($kakutyousi != "CSV") {
            $this->warning = "CSVファイルを指定して下さい。\\n(".$kakutyousi."ファイルの取込は出来ません。)";
            return false;
        }

        //テスト情報
        $db = Query::dbCheckOut();
        $query = knjd128wQuery::getTestCd($this, $this->field["TESTCD"]);
        $setTestArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {
            $checkdata = array();
            $checkdata = array("SUBCLASSCD"       => array(13,"科目コード"),
                                "CHAIRCD"         => array(7,"講座コード"),
                                "SCHREGNO"        => array(8,"学籍番号"),
                                "SCORE"           => array(3,$setTestArray["TESTITEMNAME"])
                                );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $i = 0;
            $line = 1;         //行番号(初期値)

            //取得主キー
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey('RECORD_SCORE_DAT');

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");        //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                       //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                             //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"] == "on" && $line < 2) {
                        $line++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");

                    $tmp = explode(",", $buffer);

                    list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", trim($tmp[0]));
                    list($semester, $testkindCd, $testitemCd, $scoreDiv) = explode("-", $this->field["TESTCD"]);
                    $data = array("SUBCLASSCD"      => trim($tmp[0]),
                                  "CHAIRCD"         => trim($tmp[2]),
                                  "SCHREGNO"        => trim($tmp[4]),
                                  "SCORE"           => trim($tmp[7]),
                                  "YEAR"            => CTRL_YEAR,
                                  "SEMESTER"        => $semester,
                                  "TESTKINDCD"      => $testkindCd,
                                  "TESTITEMCD"      => $testitemCd,
                                  "SCORE_DIV"       => $scoreDiv,
                                  "CLASSCD"         => $classCd,
                                  "SCHOOL_KIND"     => $schoolKind,
                                  "CURRICULUM_CD"   => $curriculumCd
                                );

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                }
                $line++;   //行番号(カウント)
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        } else {
            $this->warning = "ファイル名が不正です。";
        }
        return $tempArr;
    }

    //エラーチェック
    public function errCheck($db, $data, $dataEach, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $valuesErrFlg    = false;    //値エラーフラグ
        $duplicateCsvErrFlg = false; //重複(CSV)エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $valuesErrMsg    = "";       //値エラーメッセージ
        $duplicateCsvErrMsg = ""; //重複(CSV)メッセージ

        $hissuSep     = "";
        $numericSep   = "";
        $lengthSep    = "";
        $valuesSep    = "";
        $duplicateCsvSep= "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($dataEach as $key => $val) {
            if ($this->field["SHORI_MEI"] == "1") {
                //必須項目チェック
                if (in_array($key, array("SUBCLASSCD", "CHAIRCD", "SCHREGNO"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }

                //数値チェック
                if (in_array($key, array("CHAIRCD", "SCHREGNO"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                //得点チェック
                if (in_array($key, array("SCORE"))) {
                    if ($data["SCORE"] !== "*" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjd128wQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //テスト情報
        $query = knjd128wQuery::getTestCd($this, $this->field["TESTCD"]);
        $setTestArray = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        //ヘッダ
        $header = array("SUBCLASSCD"      => "※科目コード",
                        "SUBCLASSNAME"    => "科目名称",
                        "CHAIRCD"         => "※講座コード",
                        "CHAIRNAME"       => "講座名称",
                        "SCHREGNO"        => "※学籍番号",
                        "GHR_ATTEND"      => "年組番",
                        "NAME"            => "氏名",
                        "SCORE"           => $setTestArray["TESTITEMNAME"],
                        $this->lastColumn => $this->lastColumn);
        //ヘッダ例
        $header_rei = array("SUBCLASSCD"      => "99-H-9-999999",
                            "SUBCLASSNAME"    => "科目名",
                            "CHAIRCD"         => "9999999",
                            "CHAIRNAME"       => "講座名",
                            "SCHREGNO"        => "12345678",
                            "GHR_ATTEND"      => "年組番",
                            "NAME"            => "氏名",
                            "SCORE"           => "999",
                            $this->lastColumn => $this->lastColumn);

        $contents  = "";

        if ($this->field["OUTPUT"] == "1") {
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
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
        } elseif ($this->field["OUTPUT"] == "4") {
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        if ($this->field["OUTPUT"] == "1") {
            $header_data = true;
        } else {
            $header_data = false;
            $table = "";
            $where = "";
            if ($this->field["OUTPUT"] == "3") {
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                $query = knjd128wQuery::getSelectDataSQL($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {
                    $row = str_replace(",", ".", $row);
                }
                $row["SCORE"] = str_replace(" ", "", $row["SCORE"]);
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse = "エラー出力";
            } else {
                $couse = CTRL_YEAR."年度{$setTestArray["LABEL"]}成績データ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //値チェック
    public function ataiCheck($data, $atai)
    {
        $flg = true;

        if ($data!="") {
            if (!in_array($data, $atai)) {
                $flg = false;
            }
        }

        return $flg;
    }

    /**
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    public function zeroUme($data)
    {
        //更新
        if ($this->field["SHORI_MEI"]=="1") {
            if ($data["CHAIRCD"]!="") {
                if (strlen($data["CHAIRCD"]) < 7) {
                    $data["CHAIRCD"] = sprintf("%07d", $data["CHAIRCD"]);
                }
            }
            if ($data["STAFFCD"]!="") {
                if (strlen($data["STAFFCD"]) < $setsize) {
                    $data["STAFFCD"] = sprintf($zeroUme, $data["STAFFCD"]);
                }
            }
            //削除
        } else {
            if ($data["CHAIRCD"]!="") {
                if (strlen($data["CHAIRCD"]) < 7) {
                    $data["CHAIRCD"] = sprintf("%07d", $data["CHAIRCD"]);
                }
            }
            if ($data["STAFFCD"]!="") {
                if (strlen($data["STAFFCD"]) < $setsize) {
                    $data["STAFFCD"] = sprintf($zeroUme, $data["STAFFCD"]);
                }
            }
        }

        return $data;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //1万件を超えたら制限エラー
        if (get_count($data_arr) > 10000) {
            $this->setWarning("MSG915", "処理件数を1万件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }
}
