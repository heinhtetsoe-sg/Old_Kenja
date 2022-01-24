<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx050Model extends Model
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
    public function knjx050Model()
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
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組一覧
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
                    //学籍在籍データ
                    $query = knjx050Query::getExistCheck($this->dataArr[$i]);
                    $result = $db->query($query);
                    $flg = false;
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $flg = true;
                    }
                    //更新
                    if ($this->field["SHORI_MEI"] == "1") {
                        if ($flg) {   //ＤＢにあれば、update
                            knjx050Query::updateQueryCsv($db, $this->dataArr[$i], $this->staffcd);
                        } else {    //ＤＢになければ、insert
                            knjx050Query::insertQueryCsv($db, $this->dataArr[$i], $this->staffcd);
                        }
                        //削除
                    } else {
                        //ＤＢにあれば、delete
                        if ($flg) {
                            knjx050Query::deleteQueryCsv($db, $this->dataArr[$i]);
                        }
                    }
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
            $checkdata = array("SCHREGNO"   => array(8, "学籍番号"),
                               "YEAR"       => array(4, "年度"),
                               "SEMESTER"   => array(1, "学期"),
                               "GRADE"      => array(2, "学年"),
                               "HR_CLASS"   => array(3, "組"),
                               "ATTENDNO"   => array(3, "出席番号"),
                               "ANNUAL"     => array(2, "年次生"),
                               "SEAT_ROW"   => array(2, "座席行"),
                               "SEAT_COL"   => array(2, "座席列"),
                               "COURSECD"   => array(1, "課程コード"),
                               "MAJORCD"    => array(3, "学科コード"),
                               "COURSECODE" => array(4, "コースコード"));
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $i = 0;
            $record_no = 1;         //行番号(初期値)
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('SCHREG_REGD_DAT');

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

                    $tmp = explode(",", $buffer);       //2004/03/26 nakamoto add

                    $data = array("SCHREGNO"    => trim($tmp[0]),
                                  "YEAR"        => trim($tmp[1]),
                                  "SEMESTER"    => trim($tmp[2]),
                                  "GRADE"       => trim($tmp[3]),
                                  "HR_CLASS"    => trim($tmp[4]),
                                  "ATTENDNO"    => trim($tmp[5]),
                                  "ANNUAL"      => trim($tmp[6]),
                                  "SEAT_ROW"    => trim($tmp[7]),
                                  "SEAT_COL"    => trim($tmp[8]),
                                  "COURSECD"    => trim($tmp[9]),
                                  "MAJORCD"     => trim($tmp[10]),
                                  "COURSECODE"  => trim($tmp[11]));

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
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数値エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ

        $hissuErrMsg        = "";     //必須エラーメッセージ
        $numericErrMsg      = "";     //数値エラーメッセージ
        $lengthErrMsg       = "";     //レングスエラーメッセージ
        $masterErrMsg       = "";     //マスタエラーメッセージ
        $duplicateCsvErrMsg = "";     //重複(CSV)メッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $duplicateCsvSep = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($checkdata as $key => $val) {
            if ($this->field["SHORI_MEI"] == "1") {
                //必須チェック
                if (in_array($key, array("SCHREGNO", "YEAR", "SEMESTER", "GRADE", "HR_CLASS", "ATTENDNO", "ANNUAL", "COURSECD", "MAJORCD", "COURSECODE"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
                //桁数チェック
                if (in_array($key, array("YEAR", "SEMESTER", "GRADE", "ATTENDNO", "ANNUAL", "SEAT_ROW", "SEAT_COL", "COURSECD", "MAJORCD", "COURSECODE"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            } else {
                //必須チェック
                if (in_array($key, array("SCHREGNO", "YEAR", "SEMESTER"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                //文字数チェック
                if (in_array($key, array("SCHREGNO", "YEAR", "SEMESTER"))) {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
                //桁数チェック
                if (in_array($key, array("YEAR", "SEMESTER"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }
        }
        if ($returnFlg) {
            if ($this->field["SHORI_MEI"] == "1") {
                $result = $db->getOne(knjx050Query::getMasterCheck($data, "1"));
                if ($result != "X") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学籍番号");
                }
                $result = $db->getOne(knjx050Query::getMasterCheck($data, "2"));
                if ($result != "X") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍ヘッダ");
                }
                $result = $db->getOne(knjx050Query::getMasterCheck($data, "3"));
                if ($result != "X") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "課程コード");
                }
                $result = $db->getOne(knjx050Query::getMasterCheck($data, "4"));
                if ($result != "X") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学科コード");
                }
                $result = $db->getOne(knjx050Query::getMasterCheck($data, "5"));
                if ($result != "X") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "コースコード");
                }
                $result = $db->getOne(knjx050Query::getMasterCheck($data, "6"));
                if ($result != "X") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学期");
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx050Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        $header = array("SCHREGNO"        => "※学籍番号",
                        "YEAR"            => "※年度",
                        "SEMESTER"        => "※学期",
                        "GRADE"           => "※学年",
                        "HR_CLASS"        => "※組",
                        "ATTENDNO"        => "※出席番号",
                        "ANNUAL"          => "※年次生",
                        "SEAT_ROW"        => "座席行",
                        "SEAT_COL"        => "座席列",
                        "COURSECD"        => "※課程コード",
                        "MAJORCD"         => "※学科コード",
                        "COURSECODE"      => "※コースコード",
                        $this->lastColumn => $this->lastColumn);//2005.09.28nakamoto
        $header_rei = array("SCHREGNO"        => "12345678",
                            "YEAR"            => CTRL_YEAR,
                            "SEMESTER"        => "1",
                            "GRADE"           => "99",
                            "HR_CLASS"        => "999",       //2004-07-21 nakamoto ２桁→３桁へ変更
                            "ATTENDNO"        => "999",
                            "ANNUAL"          => "99",
                            "SEAT_ROW"        => "99",
                            "SEAT_COL"        => "99",
                            "COURSECD"        => "1",
                            "MAJORCD"         => "999",
                            "COURSECODE"      => "9999",
                            $this->lastColumn => $this->lastColumn);//2005.09.28nakamoto

        $contents  = "";    //クリア

        if ($this->field["OUTPUT"] == "1") {
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {     //エラー
            $tables = array();
            $tables = array("1" => "SCHREG_REGD_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents = implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {     //データ
            if ($this->field["HEADER"]=="on") {
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
                $query = knjx050Query::getselectDataSQL($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {                //データ
                    $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row["REGISTERCD"] = $row["UPDATED"] = "";
                    $row["REGISTERCD"] = $this->lastColumn;//2005.09.28nakamoto
                    unset($row["UPDATED"]);
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }
        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "学籍在籍データ";
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
        //更新
        if ($this->field["SHORI_MEI"] == "1") {
            if ($data["SCHREGNO"] != "") {
                if (strlen($data["SCHREGNO"]) < 8) {
                    $data["SCHREGNO"] = sprintf("%08d", $data["SCHREGNO"]);
                }
            }
            if ($data["GRADE"] != "") {
                if (strlen($data["GRADE"]) < 2) {
                    $data["GRADE"] = sprintf("%02d", $data["GRADE"]);
                }
            }
            if ($data["HR_CLASS"] != "") {
                if (strlen($data["HR_CLASS"]) < 3) {
                    $data["HR_CLASS"] = sprintf("%03d", $data["HR_CLASS"]);
                }
            }
            if ($data["ATTENDNO"] != "") {
                if (strlen($data["ATTENDNO"]) < 3) {
                    $data["ATTENDNO"] = sprintf("%03d", $data["ATTENDNO"]);
                }
            }
            if ($data["ANNUAL"] != "") {
                if (strlen($data["ANNUAL"]) < 2) {
                    $data["ANNUAL"] = sprintf("%02d", $data["ANNUAL"]);
                }
            }
            if ($data["SEAT_ROW"] != "") {
                if (strlen($data["SEAT_ROW"]) < 2) {
                    $data["SEAT_ROW"] = sprintf("%02d", $data["SEAT_ROW"]);
                }
            }
            if ($data["SEAT_COL"] != "") {
                if (strlen($data["SEAT_COL"]) < 2) {
                    $data["SEAT_COL"] = sprintf("%02d", $data["SEAT_COL"]);
                }
            }
            if ($data["MAJORCD"] != "") {
                if (strlen($data["MAJORCD"]) < 3) {
                    $data["MAJORCD"] = sprintf("%03d", $data["MAJORCD"]);
                }
            }
            if ($data["COURSECODE"] != "") {
                if (strlen($data["COURSECODE"]) < 4) {
                    $data["COURSECODE"] = sprintf("%04d", $data["COURSECODE"]);
                }
            }
            //削除
        } else {
            if ($data["SCHREGNO"] != "") {
                if (strlen($data["SCHREGNO"]) < 8) {
                    $data["SCHREGNO"] = sprintf("%08d", $data["SCHREGNO"]);
                }
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
