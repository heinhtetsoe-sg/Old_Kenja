<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx092Model extends Model
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
    public function knjx092Model()
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

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "CHAIRCD"          =>  VARS::post("CHAIRCD"),          //講座一覧
                             "FACCD"            =>  VARS::post("FACCD"),            //施設一覧
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
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

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        $this->dataArr = array();
        $this->dataArr = $this->fileUpload();
        if ($this->isDataMatched($this->dataArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                //講座施設データ
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    $query = knjx092Query::getExistCheck($this->dataArr[$i]);
                    $result = $db->query($query);
                    $flg = false;
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $flg = true;
                    }
                    //更新
                    if ($this->field["SHORI_MEI"]=="1") {
                        if ($flg) {   //ＤＢにあれば、update
                            knjx092Query::updateQueryCsv($db, $this->dataArr[$i]);
                        } else {    //ＤＢになければ、insert
                            knjx092Query::insertQueryCsv($db, $this->dataArr[$i]);
                        }
                        //削除
                    } else {
                        //ＤＢにあれば、delete
                        if ($flg) {
                            knjx092Query::deleteQueryCsv($db, $this->dataArr[$i]);
                        }
                    }
                }
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
            }
        }
    }

    //取込
    public function fileUploadTest()
    {
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $record_no = 1;         //行番号(初期値)
            $fp = fopen($this->field["FILE"]["tmp_name"], "r");        //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                       //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                             //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"]=="on" && $record_no < 2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");     //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);
                    $tmp2[$record_no][] = $tmp[0];
                    $tmp2[$record_no][] = $tmp[1];
                    $tmp2[$record_no][] = $tmp[2];
                    $tmp2[$record_no][] = $tmp[3];

                    $data = array("YEAR"        => trim($tmp[0]),
                                  "SEMESTER"    => trim($tmp[1]),
                                  "CHAIRCD"     => trim($tmp[2]),
                                  "FACCD"       => trim($tmp[3]));
                }//ifの綴り
                $record_no++;   //行番号(カウント)
            }//whileの綴り
            fclose($fp);
            $this->message = "CSV登録に成功しました。".$record_no."))".$tmp2[$record_no-2][0].$tmp2[$record_no-2][1].$tmp2[$record_no-2][2]."((";
        } else {
            $this->warning = "ファイル名が不正です。";
        }
    }

    //取込
    public function fileUpload()
    {
        $tempArr = array();
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $checkdata = array("YEAR"       => array(4, "年度"),
                               "SEMESTER"   => array(1, "学期"),
                               "CHAIRCD"    => array(7, "講座コード"),
                               "FACCD"      => array(4, "施設コード"));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);
            $i = 0;
            $record_no = 1;         //行番号(初期値)
            $primaryKey1    = array();
            $duplicateCsv1  = array();
            $primaryKey1    = $this->getPrimaryKey('CHAIR_FAC_DAT');

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");        //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                       //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                             //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"]=="on" && $record_no < 2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");     //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    $data = array("YEAR"        => trim($tmp[0]),
                                  "SEMESTER"    => trim($tmp[1]),
                                  "CHAIRCD"     => trim($tmp[2]),
                                  "FACCD"       => trim($tmp[3]));
                    //０埋め
                    $data = $this->zeroUme($data);
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $record_no, $duplicateCsv1, $primaryKey1)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$record_no][$val] = $data[$val];
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
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;     //必須エラーフラグ
        $numericErrFlg      = false;     //数値エラーフラグ
        $lengthErrFlg       = false;     //レングスエラーフラグ
        $masterErrFlg       = false;
        $duplicateCsvErrFlg = false;     //重複(CSV)エラーフラグ

        $hissuErrMsg        = "";        //必須エラーメッセージ
        $numericErrMsg      = "";        //数値エラーメッセージ
        $lengthErrMsg       = "";        //レングスエラーメッセージ
        $masterErrMsg       = "";
        $duplicateCsvErrMsg = "";        //重複(CSV)メッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
            //更新
            if ($this->field["SHORI_MEI"]=="1") {
                //必須項目チェック
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }

                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }

                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            //削除
            } else {
                //必須項目チェック
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }

                if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }
        }

        if ($returnFlg) {
            //存在チェック(更新時のみ)
            if ($this->field["SHORI_MEI"]=="1") {
                //1.講座データ
                if ($data["CHAIRCD"] != "") {
                    $result = $db->getOne(knjx092Query::getMasterCheck($data, "1"));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "講座データ");
                    }
                }
                //2.施設マスタ
                if ($data["FACCD"] != "") {
                    $result = $db->getOne(knjx092Query::getMasterCheck($data, "2"));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "施設コード");
                    }
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$masterErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx092Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }
    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = array("YEAR"            => "※年度",
                        "SEMESTER"        => "※学期",
                        "CHAIRCD"         => "※講座コード",
                        "FACCD"           => "※施設コード",
                        $this->lastColumn =>  $this->lastColumn);

        $header_rei = array("YEAR"            => CTRL_YEAR,
                            "SEMESTER"        => CTRL_SEMESTER,
                            "CHAIRCD"         => "9999999",
                            "FACCD"           => "8888",
                            $this->lastColumn =>  $this->lastColumn);

        $contents  = "";    //クリア

        if ($this->field["OUTPUT"]=="1") {             //ヘッダ
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {       //エラー
            $tables = array();
            $tables = array("1" => "CHAIR_FAC_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents = implode($headerErr, ",")."\n";
            }

            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {       //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }
        if ($this->field["OUTPUT"]=="1") {         //ヘッダ
            $header_data = true;
        } else {                                //ヘッダ以外
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            $table = "";
            $where = "";
            if ($this->field["OUTPUT"] == "3") {               //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {       //データ
                $query = knjx092Query::getselectDataSQL($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {              //データ
                    $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row["REGISTERCD"] = $row["UPDATED"] = "";
                    $row["REGISTERCD"] =  $this->lastColumn;
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }
        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {      //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "講座施設データ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    /**
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    public function zeroUme($data)
    {
        //更新  削除
        if ($data["CHAIRCD"] != "") {
            if (strlen($data["CHAIRCD"]) < 7) {
                $data["CHAIRCD"] = sprintf("%07d", $data["CHAIRCD"]);
            }
        }

        return $data;
    }

    //データの整合性チェック
    public function isDataMatched(&$dataArr)
    {
        //１万件を超えたら制限エラー
        if (get_count($dataArr) > 10000) {
            $this->setWarning("MSG915", "処理件数を１万件以下にして下さい 処理件数：".get_count($dataArr)."件");
            return false;
        }
        return true;
    }
}
