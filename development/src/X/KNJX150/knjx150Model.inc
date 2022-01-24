<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjx150Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード

    /**
     * メンバ変数の初期化
     */
    function knjx150Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//登録者コード
    }
    function init()
    {
		$this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),			//出力取込種別
        					 "YEAR"				=>  VARS::post("YEAR"),				//年度一覧
        					 "GRADE_HR_CLASS"	=>  VARS::post("GRADE_HR_CLASS"),	//年組一覧
        					 "HEADER"			=>  VARS::post("HEADER"),			//ヘッダ有
        					 "SHORI_MEI"		=>  VARS::post("SHORI_MEI")			//処理名
                     		);

        $this->cmd          = VARS::request("cmd");
        if (VARS::file("FILE")){
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }
    }
    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        $this->dataArr = array();
        $this->dataArr = $this->fileUpload();

        //データの整合性チェック
        if ($this->isDataMatched($this->dataArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    //通知票所見データ
                    $query = knjx150Query::getExist_Check($this->dataArr[$i]);
                    $result = $db->query($query);
                    $flg = false;
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $flg = true;
                    }
                    //更新
                    if ($this->field["SHORI_MEI"]=="1") {
                        if ($flg) {
                            knjx150Query::updateQueryCsv($db, $this->dataArr[$i], $this->staffcd);
                        } else {
                            knjx150Query::insertQueryCsv($db, $this->dataArr[$i], $this->staffcd);
                        }
                    //削除
                    } else {
                        if ($flg) {
                            knjx150Query::deleteQueryCsv($db, $this->dataArr[$i]);
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
        $tempArr = array();
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {
            $checkdata = array("YEAR"          => array(4, "年度"),
                               "SEMESTER"      => array(1, "学期"),
                               "SCHREGNO"      => array(8, "学籍番号"),
                               "COMMUNICATION" => array(258, "通信欄"));
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $i = 0;
            $record_no = 1;               //行番号(初期値)
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('HREPORTREMARK_DAT');

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

                    $data = array("YEAR"            => trim($tmp[0]),
                                  "SEMESTER"        => trim($tmp[1]),
                                  "SCHREGNO"        => trim($tmp[2]),
                                  "COMMUNICATION"   => trim($tmp[3]));

                    //０埋め
                    if ($data["SCHREGNO"] != "") {
                        $data["SCHREGNO"] = (strlen($data["SCHREGNO"]) < 8) ? sprintf("%08d", $data["SCHREGNO"]) : $data["SCHREGNO"];
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
        return $tempArr;
    }

   //エラーチェック
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数値エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //存在エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ

        $hissuErrMsg        = "";     //必須エラーメッセージ
        $numericErrMsg      = "";     //数値エラーメッセージ
        $lengthErrMsg       = "";     //レングスエラーメッセージ
        $masterErrMsg       = "";     //存在エラーメッセージ
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
                if (in_array($key, array("SCHREGNO", "YEAR", "SEMESTER"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
                //数値チェック
                if (in_array($key, array("YEAR","SEMESTER"))) {
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
                //数値チェック
                if (in_array($key, array("YEAR","SEMESTER"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }
        }
        if ($returnFlg) {
            if ($this->field["SHORI_MEI"] == "1") {
                //1.基礎マスタ
                $sonzai_flg = $db->getOne(knjx150Query::getMasterCheck($data, "1"));
                if ($sonzai_flg != "X") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "基礎データ");
                }
                //2.在籍データ
                $sonzai_flg = $db->getOne(knjx150Query::getMasterCheck($data, "2"));
                if ($sonzai_flg != "X") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍データ");
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$masterErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx150Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }
    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        $header = array("YEAR"          => "※年度",
                        "SEMESTER"      => "※学期",
                        "SCHREGNO"      => "※学籍番号",
                        "COMMUNICATION" => "通信欄",
                        "DUMMY"         => "DUMMY");//2005.09.28nakamoto
        $header_rei = array("YEAR"          => "2006",
                            "SEMESTER"      => "1",
                            "SCHREGNO"      => "12345678",
                            "COMMUNICATION" => "通信欄です。入力は、改行せずに続けて入力して下さい。",
                            "DUMMY"         => "DUMMY");//2005.09.28nakamoto

        $contents  = "";      //クリア
        //ヘッダ
        if ($this->field["OUTPUT"] == "1") {
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {      //エラー
            $tables = array();
            $tables = array("1" => "HREPORTREMARK_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
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
        } else {                                   //ヘッダ以外
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            $table = "";
            $where = "";
            if ($this->field["OUTPUT"] == "3") {
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {    //データ
                $query = knjx150Query::getselectDataSQL($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {        //データ
                    $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row["COMMUNICATION"] = str_replace("\r\n", "", $row["COMMUNICATION"]);//改行マークを''に置換する。
                    $row["REGISTERCD"] = "DUMMY";//2005.09.28nakamoto
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
                $couse = "通知票所見データ";
            }
            $filename = sprintf("%s.csv", $couse);
            $filename = i18n_convert($filename, "SJIS-win");
            $contents = i18n_convert($contents, "SJIS-win");
            /* HTTPヘッダの出力 */
            i18n_http_output("pass");
            header("Accept-Ranges: none");
            header("Content-Disposition: inline; filename = $filename");
            header("Content-Transfer-Encoding: binary");
            header("Content-Length: ". strlen($contents));
            header("Content-Type: text/octet-stream");

            echo $contents;
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
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
?>
