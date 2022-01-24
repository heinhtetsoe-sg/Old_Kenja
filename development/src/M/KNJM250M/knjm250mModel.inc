<?php

require_once('for_php7.php');
//メニュー画面
class knjm250mModel extends model
{
    public $cmd;                   //一覧表示時のcmd
    public $ObjYear;               //年度
    public $sub;                   //講座コード+科目コード
    public $repcntall;             //回数
    public $repstartcnt;           //開始回数
    public $setdata = array();     //表示データ配列
    public $kaisu = array();       //回数
    public $error_flg = false;     //入力エラーフラグ
    public $file;
    public $field;
    public $lastColumn;//CSVの最後の列の値

    public function knjm250mModel()
    {
        $this->ObjYear = CTRL_YEAR;
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->ObjYear = VARS::request("YEAR");

        $this->sub = VARS::post("SELSUB");

        if ($this->cmd != "reset") {
            for ($ini = $this->repstartcnt ; $ini < ($this->repstartcnt + $this->repcntall); $ini++) {
                $this->setdata["REPDIV"][$ini]  = VARS::post("REPDIV".$ini);
                $this->setdata["ENDDAY"][$ini]  = VARS::post("ENDDAY".$ini);
                $this->setdata["BASEDAY"][$ini] = VARS::post("BASEDAY".$ini);
                $this->setdata["REMARK"][$ini]  = VARS::post("REMARK".$ini);
            }
        }

        $this->field["HEADER"] = VARS::post("HEADER");
        $this->field["OUTPUT"] = VARS::post("OUTPUT");

        $this->getPropertiesAll();  //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->file = VARS::file("FILE");

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    /**
     * メイン表示用のモデルを返します
    */
    public function &getMainModel()
    {
        $this->setdata = array();       //表示データ配列
    }

    //入力項目チェック
    public function validateInput()
    {
        $this->error_flg = false;
        $checdata = array();
        $checcnt  = array();
        $checary  = array();
        $s = 0;

        for ($i = $this->repstartcnt; $i < ($this->repstartcnt + $this->repcntall); $i++) {
            $this->setdata["REPDIV"][$i]  = VARS::post("REPDIV".$i);
            $this->setdata["ENDDAY"][$i]  = VARS::post("ENDDAY".$i);
            $this->setdata["BASEDAY"][$i] = VARS::post("BASEDAY".$i);
            $this->setdata["REMARK"][$i]  = VARS::post("REMARK".$i);
            if ($this->setdata["REPDIV"][$i] == "" || $this->setdata["BASEDAY"][$i] == "") {
                $this->setWarning("レポート区分、提出基準日は必須です。");
                return false;
            }
        }
        return true;
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->delete();
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    public function &update()
    {
        $result = knjm250mQuery::getUpdateQuery($this);
        return $result;
    }

    public function &delete()
    {
        $result = knjm250mQuery::getDeleteQuery($this);
        return $result;
    }


    /**
     * 前年度からのコピーのモデルを返します
    */
    public function &getCopyModel()
    {
        $db = Query::dbCheckOut();

        //存在チェック（指定年度） -- レポート区分
        $repCnt = $db->getOne(knjm250mQuery::checkReportdiv($this));

        //存在チェック（前年度） -- REP_STANDARDDATE_DAT
        $pre_year = $this->ObjYear - 1;
        $preYearCnt = $db->getOne(knjm250mQuery::checkDataExists($this, $pre_year));

        //存在チェック（指定年度） -- REP_STANDARDDATE_DAT
        $thisYearCnt = $db->getOne(knjm250mQuery::checkDataExists($this, $this->ObjYear));

        Query::dbCheckIn($db);

        if (!$repCnt) {
            $this->setWarning('MSG305', ' 　　( レポート区分 )');
            return false;
        } elseif (!$preYearCnt) {
            $this->setWarning('MSG303', ' 　　( 前年度 )');
            return false;
        } elseif ($thisYearCnt > 0) {
            $this->setWarning('MSG203', '指定年度にデータが存在します。');
            return false;
        }

        //フィールド一覧取得
        $table_field = knjm250mQuery::getFieldList();

        //コピー件数取得
        $cnt = knjm250mQuery::getCopyQuery($this, $table_field, "cnt");

        if ($cnt == '0') {
            $this->setMessage('更新対象がありませんでした。');
        } else {
            //コピー
            $result = knjm250mQuery::getCopyQuery($this, $table_field, "copy");
            $this->setMessage('MSG201', 'コピー件数：'.$cnt.'件');
        }
    }


    /***** CSV関連 *****/
    //CVSファイル
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        $db = Query::dbCheckOut();

        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ、エラー出力
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");

            $duplHeader = array("YEAR"           => "年度",
                                "STANDARD_SEQ"   => "回数",
                                "CLASSCD"        => "教科",
                                "SCHOOL_KIND"    => "校種",
                                "CURRICULUM_CD"  => "教育課程",
                                "SUBCLASSCD"     => "科目");
            $tables = array();
            $tables = array("1" => "REP_STANDARDDATE_DAT");
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
                "※レポート区分",
                "※提出基準日",
                "最終提出日",
                "備考",
                $this->lastColumn,
            );
            if ($this->field["HEADER"]) {
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }
        }

        if ($this->field["OUTPUT"] == "4") {
            $contents .= $this->ObjYear.",";                         //年度
            $contents .= substr($this->sub, 0, 7).",";   //講座コード
            $contents .= ($this->Properties["useCurriculumcd"] == "1") ? substr($this->sub, 7)."," : substr($this->sub, 0, 7).",";   //科目コード
            $contents .= "国語総合,";                                //科目名
            $contents .= "1,";                                       //回数
            $contents .= "1,";                                       //レポート区分
            $contents .= "2020/07/24,";                              //提出基準日
            $contents .= "2020/08/09,";                              //最終提出日
            $contents .= "全角20文字,";                              //備考
            $contents .= $this->lastColumn."\r\n";

            $data = true;
        } elseif ($this->field["OUTPUT"] == "2") {
            //科目名を取得
            $query = knjm250mQuery::getChairname($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            for ($ini = $this->repstartcnt; $ini < ($this->repstartcnt + $this->repcntall); $ini++) {
                //書出
                $contents .= $this->ObjYear.",";                       //年度
                $contents .= substr($this->sub, 0, 7).","; //講座コード
                $contents .= ($this->Properties["useCurriculumcd"] == "1") ? substr($this->sub, 7)."," : substr($this->sub, 0, 7).","; //科目コード
                $contents .= $row["CHAIRNAME"].",";                    //科目名
                $contents .= $ini.",";                                 //回数
                $contents .= VARS::post("REPDIV".$ini).",";     //レポート区分
                $contents .= VARS::post("BASEDAY".$ini).",";    //提出基準日
                $contents .= VARS::post("ENDDAY".$ini).",";     //最終提出日
                $contents .= VARS::post("REMARK".$ini).",";     //備考
                $contents .= "\r\n";

                $data = true;
            }
        } else {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあれば置換、置換する文字はカンマでなければ何でも良い。
                //置換（日付）
                foreach ($row as $key => $val) {
                    if (preg_match('/DATE/', $key) && $row[$key] != "") {
                        $row[$key] = str_replace("-", "/", $val);
                    }
                }

                //エラー出力
                //配列の値を,で連結
                $contents .= implode(",", $row)."\r\n";

                $data = true;
            }
            $result->free();
        }

        Query::dbCheckIn($db);

        if ($data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse="レポート基本情報入力エラー";
            } else {
                $couse="レポート基本情報入力";
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
                knjm250mQuery::insertQueryCsv($db, $this, $this->data_arr);
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
                "YEAR"            => array(4    ,"年度"           ), //0
                "CHAIRCD"         => array(7    ,"講座コード"     ), //1
                "SUBCLASSCD"      => array(($this->Properties["useCurriculumcd"] == "1") ? 15 : 6   ,"科目コード"     ),   //2
                "STANDARD_SEQ"    => array(4    ,"回数"           ), //4
                "REPDIV"          => array(2    ,"レポート区分"   ), //5
                "BASEDAY"         => array(10   ,"提出基準日"     ), //6
                "ENDDAY"          => array(10   ,"最終提出日"     ), //7
                "REMARK"          => array(60   ,"備考"           ), //8
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
            $primaryKey1 = $this->getPrimaryKey('REP_STANDARDDATE_DAT');
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
                        "YEAR"            => $tmp[0],
                        "CHAIRCD"         => $tmp[1],
                        "SUBCLASSCD"      => $tmp[2],
                        "STANDARD_SEQ"    => $tmp[4],
                        "REPDIV"          => $tmp[5],
                        "BASEDAY"         => $tmp[6],
                        "ENDDAY"          => $tmp[7],
                        "REMARK"          => $tmp[8],
                        "CLASSCD"         => "",
                        "SCHOOL_KIND"     => "",
                        "CURRICULUM_CD"   => ""
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
        $hissuErrFlg        = false;  //必須エラーフラグ
        $numericErrFlg      = false;  //数値エラーフラグ
        $lengthErrFlg       = false;  //レングスエラーフラグ
        $existanceErrFlg    = false;  //存在エラーフラグ
        $otherErrFlg        = false;  //その他エラーフラグ
        $duplicateCsvErrFlg = false;  //重複エラーフラグ

        $hissuErrMsg        = "";     //必須エラーメッセージ
        $numericErrMsg      = "";     //数値エラーメッセージ
        $lengthErrMsg       = "";     //レングスエラーメッセージ
        $existanceErrMsg    = "";     //存在エラーメッセージ
        $otherErrMsg        = "";     //その他エラーメッセージ
        $duplicateCsvErrMsg = "";     //重複エラーメッセージ

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
            if (in_array($key, array("YEAR","CHAIRCD","SUBCLASSCD","STANDARD_SEQ","REPDIV","BASEDAY"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //数値チェック
            if (in_array($key, array("YEAR","STANDARD_SEQ"))) {
                if ($data[$key] != "" && !preg_match('/^[0-9]+$/', $data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
            //日付チェック
            if (in_array($key, array("BASEDAY","ENDDAY"))) {
                if ($data[$key] != "" && $this->dateRep($data[$key]) == false) {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "日付書式(".$val[1].")");
                }
            }
        }
        //年度チェック
        if ($data["YEAR"] != "" && $this->ObjYear != $data["YEAR"]) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "年度不一致");
        }
        //科目チェック
        if ($data["CHAIRCD"] != "" && $data["SUBCLASSCD"] != "" && $this->sub != $data["CHAIRCD"].$data["SUBCLASSCD"]) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "科目不一致");
        }
        //回数チェック
        if (trim($data["STANDARD_SEQ"]) != "") {
            if ((intval($data["STANDARD_SEQ"]) < $this->repstartcnt) || (($this->repstartcnt + $this->repcntall) <= intval($data["STANDARD_SEQ"]))) {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "回数");
            }
        }
        //存在チェック
        if ($data["REPDIV"] != "" && 0 == $db->getOne(knjm250mQuery::checkReportdiv($this, $data["REPDIV"]))) {
            $returnFlg = $this->errSet($existanceErrFlg, $existanceErrMsg, "※存在:", $existanceSep, "レポート区分");
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existanceErrMsg.$otherErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjm250mQuery::insertQueryErr($db, $line, $checkError);
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

    //日付変換 項目チェック後に行う。
    public function dateRep($data)
    {
        $data = common::DateConv1($data, 4);
        if ($data) {
            $data = str_replace("/", "-", $data);
        }

        return $data;
    }
}
