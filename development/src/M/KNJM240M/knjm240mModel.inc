<?php

require_once('for_php7.php');
class knjm240mModel extends Model
{
    public $field      = array();
    public $cmd;
    public $Year;
    public $AddorUp;               //登録判定用
    public $subclasscd;            //科目コード
    public $chaircd;               //講座コード
    public $subclass_show;         //講座名
    public $file;
    public $lastColumn;//CSVの最後の列の値

    //更新するフィールド
    public $item = array("KAMOKU", "SCHCNT", "CHECKCNT");

    public $control_data = array();    //コントロールマスタデータを保持

    public function knjm240mModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        if (VARS::get("CHAIRCD") != "") {
            $this->chaircd          = VARS::get("CHAIRCD");
        }
        if (VARS::get("SUBCLASSCD") != "") {
            $this->subclasscd       = VARS::get("SUBCLASSCD");
        }
        if (VARS::get("SUBCLASS_SHOW") != "") {
            $this->subclass_show    = VARS::get("SUBCLASS_SHOW");
        }

        if (VARS::post("GrYEAR")) {
            $this->Year = VARS::post("GrYEAR");
        }

        if (VARS::get("GetYear")) {
            $this->Year = VARS::get("GetYear");
        }

        if (VARS::get("rightInit")) {
            $this->Year = CTRL_YEAR;
        }

        $this->cmd = VARS::request("cmd");

        if ($this->cmd == 'init' || VARS::get("PROGRAMID")) {
            unset($this->chaircd);
            unset($this->subclasscd);
            unset($this->subclass_show);
            unset($this->file);
        }

        foreach ($this->item as $field) {
            $this->field[$field] = VARS::post($field);
        }

        $this->field["REP_START_SEQ"] = VARS::post("REP_START_SEQ");
        $this->field["HEADER"] = VARS::post("HEADER");
        $this->field["OUTPUT"] = VARS::post("OUTPUT");

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->file = VARS::file("FILE");

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->subclasscd == '' || $this->chaircd == '') {
            $this->setWarning("MSG301");
            return false;
        }
        if ($this->field["SCHCNT"] == '') {
            $this->setWarning("MSG304", "(回数)");
            return false;
        }

        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            if ($this->AddorUp == "up") {
                $this->update();
            } else {
                $this->insert();
            }
            $this->setMessage("MSG201");
        }
    }

    //追加
    public function &insert()
    {
        knjm240mQuery::getInsertQuery($this);
    }

    //更新
    public function &update()
    {
        knjm240mQuery::getUpdateQuery($this);
    }

    /**
     * 前年度からのコピーのモデルを返します
    */
    public function &getCopyModel()
    {
        //存在チェック（前年度） -- CHAIR_CORRES_DAT
        $pre_year = $this->Year - 1;
        $preYearCnt = knjm240mQuery::checkDataExists($this, $pre_year);

        //存在チェック（指定年度） -- CHAIR_CORRES_DAT
        $thisYearCnt = knjm240mQuery::checkDataExists($this, $this->Year);

        if (!$preYearCnt) {
            $this->setWarning('MSG303', ' 　　( 前年度 )');
            return false;
        } elseif ($thisYearCnt > 0) {
            $this->setWarning('MSG203', '指定年度にデータが存在します。');
            return false;
        }

        //フィールド一覧取得
        $table_field = knjm240mQuery::getFieldList();

        //コピー件数取得
        $cnt = knjm240mQuery::getCopyQuery($this, $table_field, "cnt");

        if ($cnt == '0') {
            $this->setMessage('更新対象がありませんでした。');
        } else {
            //コピー
            $result = knjm240mQuery::getCopyQuery($this, $table_field, "copy");
            $this->setMessage('MSG201', 'コピー件数：'.$cnt.'件');
        }
    }

    /***** CSV関連 *****/
    //CVSファイル
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        if ($this->field["OUTPUT"] == "3") {
            $duplHeader = array("YEAR"           => "年度",
                                "CHAIRCD"        => "講座コード",
                                "CLASSCD"        => "教科",
                                "SCHOOL_KIND"    => "校種",
                                "CURRICULUM_CD"  => "教育課程",
                                "SUBCLASSCD"     => "科目");
            //ヘッダ、エラー出力
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
            $tables = array();
            $tables = array("1" => "CHAIR_CORRES_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $duplHeader);
                $contents .= implode($headerErr, ",")."\n";
            }

            $contents .= implode(",", $header) . "\r\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } elseif ($this->field["OUTPUT"] == "2" || $this->field["OUTPUT"] == "4") {
            //ヘッダ、書出
            $header = array(
                "※年度",
                "※講座コード",
                "※科目コード",
                "科目名",
                "※回数",
                "単位認定レポート回数",
                "開始回数",
                $this->lastColumn,
            );
            if ($this->field["HEADER"]) {
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }
            $query = knjm240mQuery::readQuery($this);
        }

        if ($this->field["OUTPUT"] == "4") {
            $contents .= $this->Year.",";                                                               //年度
            $contents .= "1103000,";                                                                    //講座コード
            $contents .= ($this->Properties["useCurriculumcd"] == "1") ? "11-H-2-110300," : "110300,";  //科目コード
            $contents .= "国語総合,";                                                                   //科目名
            $contents .= "10,";                                                                         //回数
            $contents .= "5,";                                                                          //単位認定レポート回数
            $contents .= "1,";                                                                          //開始回数
            $contents .= $this->lastColumn."\r\n";

            $data = true;
        } else {
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあれば置換、置換する文字はカンマでなければ何でも良い。
                //置換（日付）
                foreach ($row as $key => $val) {
                    if (preg_match('/DATE/', $key) && $row[$key] != "") {
                        $row[$key] = str_replace("-", "/", $val);
                    }
                }

                if ($this->field["OUTPUT"] == "2") {
                    //書出
                    $contents .= $this->Year.",";           //年度
                    $contents .= $row["CHAIRCD"].",";       //講座コード
                    $contents .= $row["SUBCLASSCD"].",";    //科目コード
                    $contents .= $row["SUBCLASSNAME"].",";  //科目名
                    $contents .= $row["REP_SEQ_ALL"].",";   //回数
                    $contents .= $row["REP_LIMIT"].",";     //単位認定レポート回数
                    $contents .= $row["REP_START_SEQ"].","; //開始回数
                    $contents .= "\r\n";
                } elseif ($this->field["OUTPUT"] == "3") {
                    //エラー出力
                    $contents .= implode(",", $row)."\r\n";
                }
                //配列の値を,で連結
                $data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        if ($data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse="レポート年間回数入力エラー";
            } else {
                $couse="レポート年間回数入力";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //ここから取り込み処理
    //CVSファイル
    //クライアント ⇒ サーバ側の処理
    public function getUploadCsvModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                knjm240mQuery::insertQueryCsv($db, $this, $this->data_arr);
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //送られたファイルの文字列を取得
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array(
                "YEAR"                 => array(4   ,"年度"                     ), //0
                "CHAIRCD"              => array(7   ,"講座コード"               ), //1
                "SUBCLASSCD"           => array(($this->Properties["useCurriculumcd"] == "1") ? 15 : 6 ,"科目コード"  ), //2
                "REP_SEQ_ALL"          => array(4   ,"回数"                     ), //4
                "REP_LIMIT"            => array(4   ,"単位認定レポート回数"     ), //5
                "REP_START_SEQ"        => array(4   ,"開始回数"                 ), //6
            );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('CHAIR_CORRES_DAT');
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

                    $data = array(
                        "YEAR"          => $tmp[0],
                        "CHAIRCD"       => $tmp[1],
                        "SUBCLASSCD"    => $tmp[2],
                        "REP_SEQ_ALL"   => $tmp[4],
                        "REP_LIMIT"     => $tmp[5],
                        "REP_START_SEQ" => $tmp[6],
                        "CLASSCD"       => "",
                        "SCHOOL_KIND"   => "",
                        "CURRICULUM_CD" => ""
                    );

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $temp_arr, $duplicateCsv1, $primaryKey1)) {
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

    //エラーチェック
    public function errCheck($db, $data, $checkdata, $line, $temp_arr, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;       //必須エラーフラグ
        $numericErrFlg      = false;       //数値エラーフラグ
        $lengthErrFlg       = false;       //レングスエラーフラグ
        $existanceErrFlg    = false;       //存在エラーフラグ
        $otherErrFlg        = false;       //その他エラーフラグ
        $duplicateCsvErrFlg = false;       //重複エラーフラグ

        $hissuErrMsg        = "";          //必須エラーメッセージ
        $numericErrMsg      = "";          //数値エラーメッセージ
        $lengthErrMsg       = "";          //レングスエラーメッセージ
        $existanceErrMsg    = "";          //存在エラーメッセージ
        $otherErrMsg        = "";          //その他エラーメッセージ
        $duplicateCsvErrMsg = "";          //重複エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $existanceSep    = "";
        $otherSep        = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR","CHAIRCD","SUBCLASSCD","REP_SEQ_ALL"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //数値チェック
            if (in_array($key, array("YEAR","REP_SEQ_ALL","REP_LIMIT","REP_START_SEQ"))) {
                if ($data[$key] != "" && !preg_match('/^[0-9]+$/', $data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }
        //年度チェック
        if ($data["YEAR"] != "" && $this->Year != $data["YEAR"]) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "年度不一致");
        }
        //存在チェック
        if ($data["CHAIRCD"] != "" && $data["SUBCLASSCD"] != "" && 0 == $db->getOne(knjm240mQuery::checkChairDat($this, $data["CHAIRCD"], $data["SUBCLASSCD"]))) {
            $returnFlg = $this->errSet($existanceErrFlg, $existanceErrMsg, "※存在:", $existanceSep, "講座コード、科目コード");
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existanceErrMsg.$otherErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjm240mQuery::insertQueryErr($db, $line, $checkError);
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
}
