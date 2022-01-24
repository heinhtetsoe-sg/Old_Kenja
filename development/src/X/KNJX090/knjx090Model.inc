<?php

require_once('for_php7.php');
class knjx090Model extends Model
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

    public function knjx090Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
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

        $this->field = array("OUTPUT"    =>  VARS::post("OUTPUT"),      //出力取込種別
                             "YEAR"      =>  VARS::post("YEAR"),        //年度一覧
                             "CHAIRCD"   =>  VARS::post("CHAIRCD"),     //講座一覧
                             "SCHREGNO"  =>  VARS::post("SCHREGNO"),    //学籍番号一覧
                             "APPDATE"   =>  VARS::post("APPDATE"),     //適用開始日付・終了日付一覧 04/11/25Add
                             "HEADER"    =>  VARS::post("HEADER"),      //ヘッダ有
                             "SHORI_MEI" =>  VARS::post("SHORI_MEI"),   //処理名
                             "GRADE"     =>  VARS::post("GRADE")        //学年
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getMainModel()
    {
    }

    //取込
    public function getExecModel()
    {
        //ファイルの文字列の取得
        $this->dataArr = array();
        $this->dataArr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->dataArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    //講座受講生徒データ
                    $query = knjx090Query::getExistCheck($this->dataArr[$i]);
                    $result = $db->query($query);
                    $flg = false;
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $flg = true;
                    }
                    //更新
                    if ($this->field["SHORI_MEI"]=="1") {
                        if ($flg) {    //ＤＢにあれば、update
                            knjx090Query::updateQueryCsv($db, $this->dataArr[$i], STAFFCD);
                        } else {    //ＤＢになければ、insert
                            knjx090Query::insertQueryCsv($db, $this->dataArr[$i], STAFFCD);
                        }
                        //削除
                    } else {
                        //ＤＢにあれば、delete
                        if ($flg) {
                            knjx090Query::deleteQueryCsv($db, $this->dataArr[$i]);
                        }
                    }
                }
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
            }
        }
    }

    public function getFileString()
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

        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $checkdata = array("YEAR"       => array(4, "年度"),
                               "SEMESTER"   => array(1, "学期"),
                               "SCHREGNO"   => array(8, "学籍番号"),
                               "APPDATE"    => array(10, "適用開始日付"),
                               "APPENDDATE" => array(10, "適用終了日付"),
                               "CHAIRCD"    => array(7, "講座コード"),
                               "ROW"        => array(2, "座席行"),
                               "COLUMN"     => array(2, "座席列"));
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $record_no = 1;         //行番号(初期値)
            $cnt = 0;               //処理済件数(初期値)

            //取得主キー
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('CHAIR_STD_DAT');
            $fp = fopen($this->field["FILE"]["tmp_name"], "r");  //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                 //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                       //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //１万件を超えたらループから抜ける。
                    if ($record_no > 65000) {
                        break;
                    }
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"]=="on" && $record_no<2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");        //文字列を指定コードに変換する


                    $tmp = explode(",", $buffer);

                    $data = array("YEAR"        => trim($tmp[0]),
                                  "SEMESTER"    => trim($tmp[1]),
                                  "CHAIRCD"     => trim($tmp[2]),
                                  "SCHREGNO"    => trim($tmp[7]),
                                  "APPDATE"     => (trim($tmp[8])) ? date("Y-m-d", strtotime(trim($tmp[8]))) : "",
                                  "APPENDDATE"  => (trim($tmp[9])) ? date("Y-m-d", strtotime(trim($tmp[9]))) : "",
                                  "ROW"         => trim($tmp[10]),
                                  "COLUMN"      => trim($tmp[11]));
                    if ($this->errCheck($db, $data, $checkdata, $record_no, $duplicateCsv1, $primaryKey1)) {
                        $tempArr[$cnt] = $data;
                        $cnt++; //処理済件数
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$record_no][$val] = $data[$val];
                    }
                    $record_no++;    //行番号(カウント)
                }//ifの綴り
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
    public function errCheck($db, $data, $checkdata, $record_no, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタにないエラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ
        $dateErrFlg         = false;    //日付エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタにないエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ
        $dateErrMsg         = "";       //日付エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $duplicateCsvSep = "";
        $dateSep         = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
            if ($this->field["SHORI_MEI"]=="1") {
                //必須項目チェック
                if (in_array($key, array("YEAR","SEMESTER","SCHREGNO","APPDATE", "APPENDDATE", "CHAIRCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //数値チェック
                if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD", "ROW", "COLUMN"))) {
                    if ($data[$key] != '' && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, "$val[1]");
                    }
                }

                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            } else {
                //必須項目チェック
                if (in_array($key, array("YEAR", "SEMESTER", "SCHREGNO", "APPDATE", "CHAIRCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //数値チェック
                if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD"))) {
                    if ($data[$key] != '' && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                //文字数チェック
                if (in_array($key, array("YEAR","SEMESTER","SCHREGNO","APPDATE", "CHAIRCD"))) {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            }
        }
        if ($returnFlg) {
            if ($data["APPDATE"] != "" && !$this->dateRep($data["APPDATE"])) {
                $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, "適用開始日付");
            }

            if ($data["APPENDDATE"] != "" && !$this->dateRep($data["APPENDDATE"])) {
                $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, "適用終了日付");
            }

            //０埋め
            $data = $this->zeroUme($data);

            if ($this->field["SHORI_MEI"]=="1") {
                //1.講座データ
                if ($data["CHAIRCD"]!="") {
                    $chaircd = $db->getOne(knjx090Query::getMasterCheck($this, $data, "1"));
                    if ($chaircd != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "講座コード");
                    }
                }
                //2.在籍データ
                if ($data["YEAR"]!="" && $data["SEMESTER"]!="" && $data["SCHREGNO"]!="") {
                    $schregno = $db->getOne(knjx090Query::getMasterCheck($this, $data, "2"));
                    if ($schregno != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学籍番号");
                    }
                }
                //3.学期マスタ(適用日付の学期範囲内チェック)
                if ($data["YEAR"]!="" && $data["SEMESTER"]!="" && $data["APPDATE"]!="" && $data["APPENDDATE"]!="") {
                    $exit = $db->getOne(knjx090Query::getMasterCheck($this, $data, "3"));
                    if ($exit != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "適用日付が学期範囲外");
                    }
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg.$dateErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx090Query::insertQueryErr($db, $record_no, $checkError);
        }

        return $returnFlg;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"     => "行番号",
                            "MSGREMARK"  => "メッセージ");
        $header = array("YEAR"            => "※年度",
                        "SEMESTER"        => "※学期",
                        "CHAIRCD"         => "※講座コード",
                        "GRADE"           => "学年",
                        "HR_CLASS"        => "組",
                        "ATTENDNO"        => "出席番号",
                        "NAME"            => "氏名",
                        "SCHREGNO"        => "※学籍番号",
                        "APPDATE"         => "※適用開始日付",
                        "APPENDDATE"      => "※適用終了日付",
                        "ROW"             => "座席行",
                        "COLUMN"          => "座席列",
                        $this->lastColumn => $this->lastColumn);
        $header_rei = array("YEAR"            => "2004",
                            "SEMESTER"        => "1",
                            "CHAIRCD"         => "9999999",
                            "GRADE"           => "01",
                            "HR_CLASS"        => "001",
                            "ATTENDNO"        => "001",
                            "NAME"            => "アルプ太郎",
                            "SCHREGNO"        => "99999999",
                            "APPDATE"         => "2004-04-08",
                            "APPENDDATE"      => "2004-07-31",
                            "ROW"             => "99",
                            "COLUMN"          => "99",
                            $this->lastColumn => $this->lastColumn);

        $contents  = "";

        if ($this->field["OUTPUT"]=="1") {           //ヘッダ
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {    //エラー
            $tables = array();
            $tables = array("1" => "CHAIR_STD_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4") {    //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header, ",")."\n";
            }
        }
        if ($this->field["OUTPUT"]=="1") {           //ヘッダ
            $header_data = true;
        } else {                                    //ヘッダ以外
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            $table = "";
            $where = "";
            if ($this->field["OUTPUT"]=="3") {                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {        //データ
                $query = knjx090Query::getselectDataSQL($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {         //データ
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }
        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "講座受講生徒データ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
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
            if ($data["SCHREGNO"]!="") {
                if (strlen($data["SCHREGNO"]) < 8) {
                    $data["SCHREGNO"] = sprintf("%08d", $data["SCHREGNO"]);
                }
            }
            if ($data["ROW"]!="") {
                if (strlen($data["ROW"]) < 2) {
                    $data["ROW"] = sprintf("%02d", $data["ROW"]);
                }
            }
            if ($data["COLUMN"]!="") {
                if (strlen($data["COLUMN"]) < 2) {
                    $data["COLUMN"] = sprintf("%02d", $data["COLUMN"]);
                }
            }
            //削除
        } else {
            if ($data["CHAIRCD"]!="") {
                if (strlen($data["CHAIRCD"]) < 7) {
                    $data["CHAIRCD"] = sprintf("%07d", $data["CHAIRCD"]);
                }
            }
            if ($data["SCHREGNO"]!="") {
                if (strlen($data["SCHREGNO"]) < 8) {
                    $data["SCHREGNO"] = sprintf("%08d", $data["SCHREGNO"]);
                }
            }
        }

        return $data;
    }

    // HRクラスの自動名簿生成
    public function getDownloadModel2()
    {
        // ＣＳＶ項目
        $header = array("YEAR"            => "※年度",
                        "SEMESTER"        => "※学期",
                        "CHAIRCD"         => "※講座コード",
                        "GRADE"           => "学年",
                        "HR_CLASS"        => "組",
                        "ATTENDNO"        => "出席番号",
                        "NAME"            => "氏名",
                        "SCHREGNO"        => "※学籍番号",
                        "APPDATE"         => "※適用開始日付",
                        "APPENDDATE"      => "※適用終了日付",
                        "ROW"             => "座席行",
                        "COLUMN"          => "座席列",
                        $this->lastColumn => $this->lastColumn);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $header_data = false;
        $contents  = "";
        $contents .= implode($header, ",")."\n";

        $query = knjx090Query::selectAutoChairStd($this, CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            // DB追加
            $query = knjx090Query::insertAutoChairStd($row, CTRL_YEAR, CTRL_SEMESTER, STAFFCD);
            $db->query($query);
            // CSV出力
            $row = str_replace(",", ".", $row); // データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= CTRL_YEAR .",";
            $contents .= CTRL_SEMESTER .",";
            $contents .= $row["CHAIRCD"] .",";
            $contents .= $row["GRADE"] .",";
            $contents .= $row["HR_CLASS"] .",";
            $contents .= $row["ATTENDNO"] .",";
            $contents .= $row["NAME"] .",";
            $contents .= $row["SCHREGNO"] .",";
            $contents .= $row["SDATE"] .",";
            $contents .= $row["EDATE"] .",";
            $contents .= "" .",";
            $contents .= "" .",";
            $contents .= $this->lastColumn ."\n";
            $header_data = true;
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);

        // ＣＳＶ出力
        if ($header_data) {
            if (!$this->Properties["useXLS"]) {
                $filename = sprintf("%s.csv", "講座受講生徒データ(自動生成)");
                common::downloadContents($contents, $filename);
                return true;
            } else {
                $this->warning = "データを更新しました。";
                return false;
            }
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //１万件を超えたら制限エラー
        if (get_count($data_arr) > 65000) {
            $this->setWarning("MSG915", "処理件数を６万５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }

        return true;
    }
}
