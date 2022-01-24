<?php

require_once('for_php7.php');
class knjc038Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $control;  //コントロールマスタ
    public $staffcd;  //登録者コード
    public $properties = array();
    public $lastColumn;//CSVの最後の列の値

    public function knjc038Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //登録者コード
    }

    public function init()
    {
        $this->field = array("OUTPUT"     => VARS::post("OUTPUT"),     //出力取込種別
                             "HANI_DIV"   => VARS::post("HANI_DIV"),   //範囲選択ラジオ
                             "STAFFCD"    => VARS::post("STAFFCD"),    //職員コード
                             "HEADER"     => VARS::post("HEADER"),     //ヘッダ有
                             "KINTAI"     => VARS::post("KINTAI"),     //勤怠コード名だったけど、出欠コードになった
                             "YEAR"       => VARS::post("YEAR"),       //年度
                             "SHORI_MEI"  => VARS::post("SHORI_MEI"),  //処理名
                             "SYUKETU"    => VARS::post("SYUKETU")     //出欠
                             );
        $this->cmd = VARS::request("cmd");
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }

        $this->getProperties();
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getMainModel()
    {
    }

    //更新(データ取込)
    public function getExecModel()
    {
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

        $tempArr = array();
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            if ($this->field["HANI_DIV"] == "1") {
                $checkdata = array( "SCHREGNO"           => array(8 , "学籍番号"),
                                    "NAME"               => array("生徒氏名"),
                                    "DI_CD"              => array(2, "出欠コード"),
                                    "DATE"               => array(10, "日付"),
                                    "PERIODCD"           => array(1, "校時"),
                                    "DI_REMARK_CD"       => array(3, "備考コード"),
                                    "DI_REMARK"          => array(60, "備考"));
            } else {
                $checkdata = array( "SCHREGNO"           => array(8 , "学籍番号"),
                                    "NAME"               => array("生徒氏名"),
                                    "DI_CD"              => array(2, "出欠コード"),
                                    "SDATE"              => array(10, "開始日"),
                                    "EDATE"              => array(10, "終了日"),
                                    "DI_REMARK_CD"       => array(3, "備考コード"),
                                    "DI_REMARK"          => array(60, "備考"));
            }
            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $i = 0;
            $record_no = 1; //行番号(初期値)
            $error_no = 0;  //エラー件数(初期値)

            //取得主キー
            $primaryKey1    = array();
            $duplicateCsv1  = array();
            $primaryKey1    = $this->getPrimaryKey('ATTEND_DAT');
            $fp = fopen($this->field["FILE"]["tmp_name"], "r"); //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                      //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"]=="on" && $record_no<2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");        //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    if ($this->field["HANI_DIV"] == "1") { //範囲 1:日・校指定  2:期間範囲
                        $data = array("SCHREGNO"        => trim($tmp[0]),
                                      "NAME"            => trim($tmp[1]),
                                      "DI_CD"           => trim($tmp[2]),
                                      "DATE"            => trim($tmp[3]),
                                      "PERIODCD"        => trim($tmp[4]),
                                      "DI_REMARK_CD"    => (trim($tmp[5])) ? sprintf("%03d", trim($tmp[5])) : trim($tmp[5]),
                                      "DI_REMARK"       => trim($tmp[6]),
                                      "ATTENDDATE"      => "");
                    } else {
                        $data = array("SCHREGNO"        => trim($tmp[0]),
                                      "NAME"            => trim($tmp[1]),
                                      "DI_CD"           => trim($tmp[2]),
                                      "SDATE"           => trim($tmp[3]),
                                      "EDATE"           => trim($tmp[4]),
                                      "DI_REMARK_CD"    => (trim($tmp[5])) ? sprintf("%03d", trim($tmp[5])) : trim($tmp[5]),
                                      "DI_REMARK"       => trim($tmp[6]),
                                      "PERIODCD"        => "",
                                      "ATTENDDATE"      => "");
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $record_no, $duplicateCsv1, $primaryKey1)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$record_no][$val] = $data[$val];
                    }
                }//ifの綴り
                $record_no++;    //行番号(カウント)
            }//whileの綴り
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        } else {
            $this->warning = "ファイル名が不正です。";
        }
        if ($this->isDataMatched($tempArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                for ($i = 0; $i < get_count($tempArr); $i++) {
                    if ($this->field["HANI_DIV"] == "1") { //範囲 1:日・校指定  2:期間範囲
                        if ($this->field["SHORI_MEI"] == "1") { //処理名 1:更新  2:削除
                            //時間割取得
                            $query = knjc038Query::getSchChrDat1($tempArr[$i], $this->field["YEAR"]);
                            $result = $db->query($query);
                            while ($rowS = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                                $schedule[] = array($rowS["SCHREGNO"], $rowS["EXECUTEDATE"], $rowS["PERIODCD"], $rowS["CHAIRCD"]);
                            }
                            //同じ日・校時に複数講座がある場合は講座コードが大きい方が追加される。
                            for ($s = 0; $s < get_count($schedule); $s++) {
                                knjc038Query::insertQueryCsv1($db, $tempArr[$i], $this->field["YEAR"], $schedule[$s]);
                            }
                        } else {
                            knjc038Query::deleteQueryCsv1($db, $tempArr[$i], $this->field["YEAR"]);
                        }
                    } else {
                        if ($this->field["SHORI_MEI"] == "1") { //処理名 1:更新  2:削除
                            //時間割取得
                            $query = knjc038Query::getSchChrDat2($tempArr[$i], $this->field["YEAR"]);
                            $result = $db->query($query);
                            while ($rowS = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                                $schedule[] = array($rowS["SCHREGNO"], $rowS["EXECUTEDATE"], $rowS["PERIODCD"], $rowS["CHAIRCD"]);
                            }
                            //同じ日・校時に複数講座がある場合は講座コードが大きい方が追加される。
                            for ($s = 0; $s < get_count($schedule); $s++) {
                                knjc038Query::insertQueryCsv2($db, $tempArr[$i], $this->field["YEAR"], $schedule[$s]);
                            }
                        } else {
                            knjc038Query::deleteQueryCsv2($db, $tempArr[$i], $this->field["YEAR"]);
                        }
                    }
                }
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($tempArr)."件");
            }
        }
    }

    //エラーチェック
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $dayErrFlg          = false;    //日付エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ
        $agreeErrFlg        = false;    //不一致エラーフラグ
        $masterErrFlg       = false;    //職員マスタにないエラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $dayErrMsg          = "";       //日付エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ
        $agreeErrMsg        = "";       //不一致エラーメッセージ
        $masterErrMsg       = "";       //マスタにないエラーメッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $existenceSep       = "";
        $daySep             = "";
        $duplicateCsvSep    = "";
        $agreeSep           = "";
        $masterSep          = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $errFlg = 0;
        foreach ($checkdata as $key => $val) {
            if ($this->field["HANI_DIV"] == "1") {
                //必須チェック
                if (in_array($key, array("SCHREGNO", "DI_CD", "DATE", "PERIODCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        if ($key == "DATE") {
                            $errFlg = 1;
                        }
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                //出欠コードチェック
                if ($key == "DI_CD" && $data[$key] != "" && $data["DI_CD"] != $this->field["KINTAI"]) {
                    $returnFlg = $this->errSet($agreeErrFlg, $agreeErrMsg, "※不一致:", $agreeSep, $val[1]);
                }

                //桁数チェック
                if ($key != "NAME" && strlen($data[$key]) > $val[0]) {
                    if ($key == "DATE") {
                        $errFlg = 1;
                    }
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }

                //存在チェック  存在しない生徒データ
                if (in_array($key, array("SCHREGNO"))) {
                    if ($data["SCHREGNO"] != "" && 0 == $db->getOne(knjc038Query::checkSchreg($data["SCHREGNO"]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
                //日付チェック 日付または校時エラー
                if ($this->dateRep($data[$key]) && $errFlg == 0 && in_array($key, array("DATE"))) {
                    $query = knjc038Query::getChaircd($data, $this);
                    $chaircd = $db->getOne($query);
                    if (!$chaircd) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "日付または校時");
                    }
                }
                //削除用存在チェック 存在しない出欠データ
                if ($key == "SCHREGNO" && strlen(trim($data[$key]))) {
                    if ($this->field["SHORI_MEI"] == "2" && 0 == $db->getOne(knjc038Query::checkAttendDat($this, $data))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
                //更新用備考コード存在チェック  存在しない備考コード
                if (in_array($key, array("DI_REMARK_CD"))) {
                    if ($this->field["SHORI_MEI"] == "1" && $data["DI_REMARK_CD"] && 0 == $db->getOne(knjc038Query::checkDiRemarkCd($this, $data["DI_REMARK_CD"]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
            } else {
                //必須項目チェック
                if (in_array($key, array("SCHREGNO","DI_CD","SDATE","EDATE"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                //桁数チェック
                if ($key != "NAME" && strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
                //出欠コードチェック    出欠コードエラー
                if ($key == "DI_CD" && $data[$key] != "" && $data["DI_CD"] != $this->field["KINTAI"]) {
                    $returnFlg = $this->errSet($agreeErrFlg, $agreeErrMsg, "※不一致:", $agreeSep, $val[1]);
                }
                //存在チェック  存在しない生徒データ
                if ($key == "SCHREGNO" && $data["SCHREGNO"] != "" && 0 == $db->getOne(knjc038Query::checkSchreg($data["SCHREGNO"]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }

                //日付チェック
                if (in_array($key, array("SDATE")) && $data[$key] != "") {
                    $query = knjc038Query::getYearDate($this->field["YEAR"]);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if ($row) {
                        $yearSdate = str_replace("/", "-", $row["SDATE"]);
                        $yearEdate = str_replace("/", "-", $row["EDATE"]);
                        $dataSdate = str_replace("/", "-", $data["SDATE"]);
                        $dataEdate = str_replace("/", "-", $data["EDATE"]);
                        if ($dataSdate < $yearSdate || $dataSdate > $yearEdate || $dataEdate < $yearSdate || $dataEdate > $yearEdate) {
                            $returnFlg = $this->errSet($dayErrFlg, $dayErrMsg, "※日付:", $daySep, "※日付年度外");
                        }
                    } else {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学期マスタに年度が登録されていません");
                    }
                }

                //削除用存在チェック    存在しない出欠データ
                if ($key == "SCHREGNO" && strlen(trim($data[$key]))) {
                    if ($this->field["SHORI_MEI"] == "2" && 0 == $db->getOne(knjc038Query::checkAttendDat($this, $data))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }

                //更新用備考コード存在チェック  存在しない備考コード
                if ($key == "DI_REMARK_CD" && strlen(trim($data[$key]))) {
                    if ($this->field["SHORI_MEI"] == "1" && $data["DI_REMARK_CD"] && 0 == $db->getOne(knjc038Query::checkDiRemarkCd($this, $data["DI_REMARK_CD"]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
            }
        }
        if ($returnFlg) {
            //０埋め
            $data["SCHREGNO"] = $this->zeroUme($data["SCHREGNO"], 8);
            if ($this->field["HANI_DIV"] == "1") {
                if ($data["DATE"] != "" && !$this->dateRep($data["DATE"])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, "日付");
                }
            } else {
                if ($data["SDATE"] != "" && !$this->dateRep($data["SDATE"])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, "開始日");
                }

                if ($data["EDATE"] != "" && !$this->dateRep($data["EDATE"])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, "終了日");
                }
                //日付大小エラ
                if ($this->dateRep($data["SDATE"]) > $this->dateRep($data["EDATE"])) {
                    $returnFlg = $this->errSet($dayErrFlg, $dayErrMsg, "※日付:", $daySep, " 開始日より終了日が大きい");
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$dayErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg.$agreeErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjc038Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }
    //ＣＳＶへエラー出力
    public function getDownloadModel()
    {
        $header = array("MSGROW"    => "行番号",
                        "MSGREMARK" => "メッセージ");
        $headerDuplicate = array("SCHREGNO"   => "学籍番号",
                                 "ATTENDDATE" => "出欠日付",
                                 "PERIODCD"   => "校時コード");
        $tables = array();
        $tables = array("1" => "ATTEND_DAT");
        $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
        if (sizeof($dupTable) > 0) {
            $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
            $contents .= implode($headerErr, ",")."\n";
        }
        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $query = $this->selectCsvErrQuery(PROGRAMID);
        $result = $db->query($query);
        $err_data = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",") ."\n";
            $err_data = true;
        }

        $result->free();
        Query::dbCheckIn($db);


        if ($err_data) {
            $couse = "エラー出力";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //ヘッダ出力
    public function getHeaderModel()
    {
        //出欠コード取得
        $db = Query::dbCheckOut();
        if ($this->field["HANI_DIV"] == '1') {
            $kintai = "'1','2','3','6','14'";
        } else {
            $kintai = "'1','2','3','6'";
        }
        $di_cd = $sep = "";
        $query = knjc038Query::getDiCd($kintai, $this->field["YEAR"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $di_cd .= $sep.$row["VALUE"].':'.$row["DI_NAME1"];
            $sep = "、";
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($this->field["HANI_DIV"] == "1") { //範囲 1:日・校指定  2:期間範囲
            $header = array("SCHREGNO"        => "※学籍番号",
                            "NAME"            => "生徒氏名",
                            "DI_CD"           => "※出欠コード　".$di_cd,
                            "DATE"            => "※日付",
                            "PERIODCD"        => "※校時",
                            "DI_REMARK_CD"    => "備考コード",
                            "DI_REMARK"       => "備考",
                            $this->lastColumn => $this->lastColumn);

            $header_rei = array("SCHREGNO"        => "12345678",
                                "NAME"            => "アルプ　太郎",
                                "DI_CD"           => $this->field["KINTAI"],
                                "DATE"            => CTRL_DATE,
                                "PERIODCD"        => "1",
                                "DI_REMARK_CD"    => "001",
                                "DI_REMARK"       => "補導された",
                                $this->lastColumn => $this->lastColumn);

            $contents  = implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";

            $header_data = true;
        } else {
            $header = array("SCHREGNO"        => "※学籍番号",
                            "NAME"            => "生徒氏名",
                            "DI_CD"           => "※出欠コード　".$di_cd,
                            "SDATE"           => "※開始日",
                            "EDATE"           => "※終了日",
                            "DI_REMARK_CD"    => "備考コード",
                            "DI_REMARK"       => "備考",
                            $this->lastColumn => $this->lastColumn);

            $header_rei = array("SCHREGNO"        => "12345678",
                                "NAME"            => "アルプ　太郎",
                                "DI_CD"           => $this->field["KINTAI"],
                                "SDATE"           => CTRL_DATE,
                                "EDATE"           => date("Y-m-d", strtotime(CTRL_DATE." +7 day ")),
                                "DI_REMARK_CD"    => "001",
                                "DI_REMARK"       => "補導された",
                                $this->lastColumn => $this->lastColumn);

            $contents  = implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";

            $header_data = true;
        }

        if ($header_data) {
            $fileTitle = $this->field["HANI_DIV"] == '1' ? "(日・校指定)" : "(期間範囲)";
            $couse = "出欠データCSV登録{$fileTitle}";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    /**
     * 日付変換 項目チェック後に行う。
     */
    public function dateRep($data)
    {
        $data = common::DateConv1(str_replace("-", "/", $data), 4);
        if ($data) {
            $data = str_replace("/", "-", $data);
        }

        return $data;
    }

    /**
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    public function zeroUme($data, $ketasuu)
    {
        $shosiki = "%0".$ketasuu."s";
        if ($data!="") {
            if (strlen($data) < $ketasuu) {
                $data = sprintf($shosiki, $data);
            }
        }

        return $data;
    }

    //menuInfo.properties からデータを読み込む
    public function getProperties()
    {
        //初期値
        $this->properties["useChgSemesUser"] = "1";
        $this->properties["useAdminYearPast"] = "6";
        $this->properties["useAdminYearFuture"] = "1";
        $this->properties["useUnAdminYearPast"] = "6";
        $this->properties["useUnAdminYearFuture"] = "0";
        $this->properties["useRootMenu"] = "C1000";
        $this->properties["useCheckAttendUnInput"] = "OFF";
        $this->properties["useUnAdminMenuPrgid"] = "";

        $arr_useUnAdminMenuPrgid = array();
        $retVal = "";

        /*
         * configディレクトリ確認
         */
        if (file_exists(CONFDIR ."/menuInfo.properties")) {
            $filename = CONFDIR ."/menuInfo.properties";
        } else {
            $filename = DOCUMENTROOT ."/menuInfo.properties";
        }

        $fp = @fopen($filename, 'r');
        while ($line = fgets($fp, 1024)) {
            foreach ($this->properties as $key => $value) {
                $pos = strpos($line, $key);
                if ($pos !== false) { //$pos === trueでは結果が違う
                    $retVal = str_replace($key." = ", "", $line);
                    $this->properties[$key] = str_replace("\r\n", "", $retVal);
                    if ($key == "useUnAdminMenuPrgid") {
                        $arr_useUnAdminMenuPrgid[] = str_replace("\r\n", "", $retVal);
                    }
                }
            }
        }
        fclose($fp);
        $this->root = $this->properties["useRootMenu"];
        if ($this->properties["useUnAdminMenuPrgid"] != "") {
            $this->properties["useUnAdminMenuPrgid"] = implode("','", $arr_useUnAdminMenuPrgid);
        }

        return $retVal;
    }

    //データの整合性チェック
    public function isDataMatched(&$dataArr)
    {
        //一万件を超えたら制限エラー
        if (get_count($dataArr) > 10000) {
            $this->setWarning(MSG915, "処理件数を一万件以下にして下さい 処理件数：".get_count($dataArr)."件");
            return false;
        }
        return true;
    }
}
