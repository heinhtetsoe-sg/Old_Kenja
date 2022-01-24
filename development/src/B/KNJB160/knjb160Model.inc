<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjb160Model extends Model
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
    public function knjb160Model()
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

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度＆学期一覧（講座別）
                             "CHAIRCD"          =>  VARS::post("CHAIRCD"),          //講座一覧
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目一覧
                             "YEAR2"            =>  VARS::post("YEAR2"),            //年度＆学期一覧（個人別）
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組一覧
                             "STUDENT"          =>  VARS::post("STUDENT"),          //生徒一覧
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        //ファイル取り込み
        $this->dataArr = array();
        $this->dataArr = $this->fileUpload();
        if ($this->isDataMatched($this->dataArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjb160Query::insertQuery($this->dataArr);
                } else {
                    knjb160Query::deleteQuery($this->dataArr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
            }
        }
    }

    //取込
    public function fileUpload()
    {
        $tempArr = array();
        //HTTP POSTによりアップロードされたファイルかどうかを調べる
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) {
            $this->warning = "ファイル名が不正です。";
        } else {
            //チェック用データの作成
            $checkdata = array( "SCHREGNO"   => array(8, "学籍番号"),
                                "YEAR"       => array(4, "年度"),
                                "SEMESTER"   => array(1, "学期"),
                                "CHAIRCD"    => array(7, "講座コード"),
                                "TEXTBOOKCD" => array(12, "教科書コード"));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $record_no = 1;            //行番号(初期値)

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");    //ファイルまたはURLをオープンする
            $i  = 0;

            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('SCHREG_TEXTBOOK_DAT');

            while (!feof($fp)) {                                   //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                         //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //１万件を超えたらループから抜ける。
                    if ($record_no > 10000) {
                        break;
                    }
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"] == "on" && $record_no < 2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win"); //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    $data = array("SCHREGNO"    => (trim($tmp[0]) == "") ? "" : sprintf("%08d", trim($tmp[0])),
                                  "YEAR"        => (trim($tmp[1]) == "") ? "" : sprintf("%04d", trim($tmp[1])),
                                  "SEMESTER"    => trim($tmp[2]),
                                  "CHAIRCD"     => (trim($tmp[3]) == "") ? "" : sprintf("%07d", trim($tmp[3])),
                                  "TEXTBOOKCD"  => (trim($tmp[4]) == "") ? "" : sprintf("%012s", trim($tmp[4])));

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $record_no, $duplicateCsv1, $primaryKey1)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$record_no][$val] = $data[$val];
                    }
                    $record_no++;    //行番号(カウント)
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }
        return $tempArr;
    }

    /**
     * //エラーチェック
     */
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ

        $hissuSep        = "";
        $lengthSep       = "";
        $numericSep      = "";
        $duplicateCsvSep = "";
        $existenceSep    = "";

        $dupFlg = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (!strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
            //数値チェック
            if (strlen($data[$key]) && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
            }
        }
        if ($returnFlg) {
            //1.講座マスタ
            $query = knjb160Query::getChair($data["CHAIRCD"], $data["YEAR"], $data["SEMESTER"]);
            $chkdt = $db->getOne($query);
            if ($chkdt == 0) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※マスタチェック:", $existenceSep, "講座コード");
            }

            //2.教科書マスタ
            $query = knjb160Query::getText($data["TEXTBOOKCD"], $data["YEAR"]);
            $chkdt = $db->getOne($query);
            if ($chkdt == 0) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※マスタチェック:", $existenceSep, "教科書コード");
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjb160Query::insertQueryErr($db, $line, $checkError);
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
                        "CHAIRCD"         => "※講座コード",
                        "TEXTBOOKCD"      => "※教科書コード",
                        $this->lastColumn => $this->lastColumn);

        $header_rei = array("SCHREGNO"        => "88888888",
                            "YEAR"            => "4444",
                            "SEMESTER"        => "1",
                            "CHAIRCD"         => "7777777",
                            "TEXTBOOKCD"      => "123456789012",
                            $this->lastColumn => $this->lastColumn);

        $contents  = "";    //クリア

        $header_data = false;
        //ヘッダ
        if ($this->field["OUTPUT"]=="1") {
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {
            //エラー
            $tables = array();
            $tables = array("1" => "SCHREG_TEXTBOOK_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4" || $this->field["OUTPUT"]=="5") {
            //データ
            $contents .= ($this->field["HEADER"] == "on") ? implode($header, ",")."\n" : "";
        }
        //ヘッダ
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ出力（講座別）
                $query = knjb160Query::getselectDataSQL($this);
            } elseif ($this->field["OUTPUT"]=="5") {
                //データ出力（個人別）
                $query = knjb160Query::getselectDataSQL2($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ
                if ($this->field["OUTPUT"] == "4" || $this->field["OUTPUT"] == "5") {
                    $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }
        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse = "エラー出力";
            } else {
                $couse = "教科書購入データ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
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
            $this->setWarning(MSG915, "処理件数を１万件以下にして下さい 処理件数：".get_count($dataArr)."件");
            return false;
        }

        return true;
    }
}
//var_dump($_REQUEST);
