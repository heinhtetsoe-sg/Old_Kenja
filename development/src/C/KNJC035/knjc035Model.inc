<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjc035Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjc035Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //登録者コード
    }
    public function init()
    {
        $this->field = array("KINTAI"       =>  VARS::post("KINTAI"),   //勤怠種別
                             "OUTPUT"       =>  VARS::post("OUTPUT"),   //出力取込種別
                             "STAFFCD"      =>  VARS::post("STAFFCD"),  //職員コード
                             "HEADER"       =>  VARS::post("HEADER"),   //ヘッダ有
                             "SHORI_MEI"    =>  VARS::post("SHORI_MEI") //処理名
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }
    public function &getMainModel()
    {
    }

    /**
     * データ取込
    */
    public function &getExecModel()
    {
        //更新
        $this->dataArr = array();
        $this->dataArr = $this->fileUpload();

        //データの整合性チェック
        if ($this->isDataMatched($this->dataArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    knjc035Query::insertQueryCsv($db, $this->dataArr[$i]);
                    knjc035Query::insertQueryCsv2($db, $this->dataArr[$i], $this);
                    //出欠済みフラグ
                    knjc035Query::insertQueryCsv3($db, $this->dataArr[$i]);
                }
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
            }
        }
    }

    //更新(データ取込)
    public function fileUpload()
    {
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $tempArr = array();
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            $checkdata = array("SCHREGNO"           => array(8 ,"学籍番号"),
                               "NAME"               => array("生徒氏名"),
                               "SDATE"              => array(10 ,"異動開始日付"),
                               "EDATE"              => array(10 ,"異動終了日付"),
                               "REASON"             => array(75 ,"事由")
                               );

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $i  = 0;
            $record_no = 1; //行番号(初期値)

            //取得主キー
            $primaryKey1    = array();
            $duplicateCsv1  = array();
            $primaryKey1    = $this->getPrimaryKey('SCHREG_TRANSFER_DAT');

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");    //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                   //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                         //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"]=="on" && $record_no<2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");        //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    $data = array("SCHREGNO"    => trim($tmp[0]),
                                  "NAME"        => trim($tmp[1]),
                                  "SDATE"       => str_replace("/", "-", $tmp[2]),
                                  "EDATE"       => str_replace("/", "-", $tmp[3]),
                                  "REASON"      => trim($tmp[4]),
                                  "TRANSFERCD"  => "3");

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $record_no, $duplicateCsv1, $primaryKey1)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        if ($val == 'TRANSFER_SDATE') {
                            $val = "SDATE";
                            $primaryKey1[$key] = $val;
                            $duplicateCsv1[$record_no][$val] = $data[$val];
                        } else {
                            $duplicateCsv1[$record_no][$val] = $data[$val];
                        }
                    }
                    $record_no++;    //行番号(カウント)
                }
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
        $returnFlg      = true;
        $hissuErrFlg    = false;  //必須エラーフラグ
        $numericErrFlg  = false;  //数値エラーフラグ
        $lengthErrFlg   = false;  //レングスエラーフラグ
        $existenceErrFlg = false; //存在エラーフラグ
        $duplicateCsvErrFlg = false; //重複(CSV)エラーフラグ
        $duplicateDbErrFlg = false;  //重複(登録)エラーフラグ
        $dayErrFlg       = false;  //日付エラーフラグ
        $masterErrFlg    = false;  //職員マスタにないエラーフラグ

        $hissuErrMsg    = "";     //必須エラーメッセージ
        $numericErrMsg  = "";     //数値エラーメッセージ
        $lengthErrMsg   = "";     //レングスエラーメッセージ
        $existenceErrMsg = "";    //存在エラーメッセージ
        $duplicateCsvErrMsg = ""; //重複(CSV)メッセージ
        $duplicateDbErrMsg = ""; //重複(登録)メッセージ
        $dayErrMsg       = "";     //日付のエラーメッセージ
        $masterErrMsg    = "";     //マスタにないエラーメッセージ

        $hissuSep       = "";
        $numericSep     = "";
        $lengthSep      = "";
        $existenceSep   = "";
        $duplicateCsvSep = "";
        $duplicateDbSep = "";
        $daySep         = "";
        $masterSep      = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        if ($data["SCHREGNO"] != "" && $data["SDATE"] != "") {
            $query = knjc035Query::getCountTransData($data);
            $result = $db->getOne($query);
            if ($result != 0) {
                $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, "重複1");
            }
        }

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("SCHREGNO", "SDATE", "EDATE"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //文字数チェック
            if (in_array($key, array("SCHREGNO", "SDATE", "EDATE", "REASON"))) {
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
            //存在チェック
            if (in_array($key, array("SCHREGNO"))) {
                if ($data["SCHREGNO"] != "" && 0 == $db->getOne(knjc035Query::checkSchreg($data["SCHREGNO"]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }
        }
        //日付チェック
        $semeSdate = str_replace("/", "-", $this->control["学期開始日付"][9]);
        $semeEdate = str_replace("/", "-", $this->control["学期終了日付"][9]);
        $dataSdate = str_replace("/", "-", $data["SDATE"]);
        $dataEdate = str_replace("/", "-", $data["EDATE"]);
        if ($dataSdate < $semeSdate || $dataSdate > $semeEdate) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学期開始日付");
        }
        if ($dataEdate < $semeSdate || $dataEdate > $semeEdate) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学期終了日付");
        }

        if ($returnFlg) {
            //日付エラー
            if ($data["SDATE"] != "" && !$this->dateRep($data["SDATE"])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, "異動開始日付");
            }
            if ($data["EDATE"] != "" && !$this->dateRep($data["EDATE"])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, "異動終了日付");
            }
            //日付エラーチェック
            if ($this->dateRep($data["SDATE"]) > $this->dateRep($data["EDATE"])) {
                $returnFlg = $this->errSet($dayErrFlg, $dayErrMsg, "※日付:", $daySep, "開始日より終了日が大きい");
            }

            //０埋め
            $data["SCHREGNO"] = $this->zeroUme($data["SCHREGNO"], 8);

            //異動チェック
            $transfer_flg = $this->transferCheck($db, $data["SCHREGNO"], $data["SDATE"], $data["EDATE"]);
            if (!$transfer_flg) {
                $returnFlg = $this->errSet($dayErrFlg, $dayErrMsg, "※日付:", $daySep, "異動日付");
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$duplicateDbErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg.$dayErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjc035Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }
    //ＣＳＶへエラー出力
    public function getDownloadModel()
    {
        $header = array("MSGROW"    => "行番号",
                        "MSGREMARK" => "メッセージ");

        $headerDuplicate = array("SCHREGNO"       => "学籍番号",
                                 "TRANSFER_SDATE" => "異動期間開始日付",
                                 "TRANSFERCD"     => "異動区分");
        $tables = array();
        $tables = array("1" => "SCHREG_TRANSFER_DAT");
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
        $header = array("SCHREGNO"        => "※学籍番号",
                        "NAME"            => "生徒氏名",
                        "SDATE"           => "※異動開始日付",
                        "EDATE"           => "※異動終了日付",
                        "REASON"          => "事由",
                        $this->lastColumn => $this->lastColumn);

        $header_rei = array(
                        "SCHREGNO"        => "12345678",
                        "NAME"            => "アルプ　太郎",
                        "SDATE"           => CTRL_DATE,
                        "EDATE"           => date('Y-m-d', strtotime(CTRL_DATE." +7 day")),
                        "REASON"          => "インフルエンザのため",
                        $this->lastColumn => $this->lastColumn);

        $contents  = implode($header, ",")."\n";
        $contents .= implode($header_rei, ",")."\n";

        $header_data = true;


        if ($header_data) {
            $couse = "出席停止者CSV登録";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
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
    /**
     * 異動情報チェック
     */
    public function transferCheck(&$db, $schregno, $sdate, $edate)
    {
        $transfer_flg = true;
        if ($schregno!="") {
            $query = knjc035Query::getTransData($schregno);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (($row["TRANSFER_SDATE"] <= $sdate && $row["TRANSFER_EDATE"] >= $sdate) ||
                   ($row["TRANSFER_SDATE"] <= $edate && $row["TRANSFER_EDATE"] >= $edate) ||
                   ($row["TRANSFER_SDATE"] >= $sdate && $row["TRANSFER_EDATE"] <= $edate) ||
                    !$transfer_flg) {
                    $transfer_flg = false;
                } else {
                    $transfer_flg = true;
                }
            }
        }

        return $transfer_flg;
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
//var_dump($_REQUEST);
