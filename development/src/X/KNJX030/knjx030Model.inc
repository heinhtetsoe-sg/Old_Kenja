<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx030Model extends Model
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
    public $addrSLen;  //住所の入力文字数

    /**
     * メンバ変数の初期化
     */
    public function knjx030Model()
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
                             "YEAR"             =>  VARS::post("YEAR"),             //年度・学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
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
        if ($this->Properties["ADDR_INPUT_SIZE"] != "") {
            $this->addrSLen = $this->Properties["ADDR_INPUT_SIZE"];
        } else {
            $this->addrSLen = 50;
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
        //データ取込
        $this->dataArr = array();
        $this->dataArr = $this->fileUpload();
        if ($this->isDataMatched($this->dataArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                $table = "schreg_address_dat";
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    $where = " where SCHREGNO = '".$this->dataArr[$i]["SCHREGNO"]."' AND ISSUEDATE = '".$this->dataArr[$i]["ISSUEDATE"]."' ";
                    $query = knjx030Query::getMasterCheck($table, $where);
                    $result = $db->query($query);
                    $flg = false;
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $flg = true;
                    }

                    //更新
                    if ($this->field["SHORI_MEI"] == "1") {
                        if ($flg) {   //ＤＢにあれば、update
                            knjx030Query::updateQueryCsv($db, $this->dataArr[$i], $this->staffcd);
                        } else {    //ＤＢになければ、insert
                            knjx030Query::insertQueryCsv($db, $this->dataArr[$i], $this->staffcd);
                        }
                        //削除
                    } else {
                        //ＤＢにあれば、delete
                        if ($flg) {
                            knjx030Query::deleteQueryCsv($db, $this->dataArr[$i]);
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
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $addLen = $this->Properties["ADDR_INPUT_SIZE"] == "" ? intval($this->addrSLen) * 3 : $this->addrSLen;
            $checkData = array("SCHREGNO"   => array(8, "※学籍番号"),
                               "ISSUEDATE"  => array(10, "※有効期間開始年月日"),
                               "EXPIREDATE" => array(10, "有効期間終了年月日"),
                               "ZIPCD"      => array(8, "郵便番号"),
                               "AREACD"     => array(2, "地区コード"),
                               "ADDR1"      => array($addLen, "住所1"),
                               "ADDR2"      => array($addLen, "住所2"),
                               "ADDR_FLG"   => array(1, "方書き印刷可"),
                               "ADDR1_ENG"  => array(150, "住所1英字"),
                               "ADDR2_ENG"  => array(150, "住所2英字"),
                               "TELNO"      => array(14, "電話番号"),
                               "TELNO2"     => array(14, "電話番号２"),
                               "FAXNO"      => array(14, "FAX番号"),
                               "EMAIL"      => array(50, "E-Mail"));
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $record_no = 1;         //行番号(初期値)
            $cnt = 0;           //処理済件数(初期値)
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey("SCHREG_ADDRESS_DAT");
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

                    $tmp = explode(",", $buffer);

                    $data = array("SCHREGNO"        => trim($tmp[0]),
                                  "ISSUEDATE"       => trim($tmp[5]),
                                  "EXPIREDATE"      => trim($tmp[6]),
                                  "ZIPCD"           => trim($tmp[7]),
                                  "AREACD"          => trim($tmp[8]),
                                  "ADDR1"           => trim($tmp[9]),
                                  "ADDR2"           => trim($tmp[10]),
                                  "ADDR_FLG"        => trim($tmp[11]),
                                  "ADDR1_ENG"       => trim($tmp[12]),
                                  "ADDR2_ENG"       => trim($tmp[13]),
                                  "TELNO"           => trim($tmp[14]),
                                  "TELNO2"          => trim($tmp[15]),
                                  "FAXNO"           => trim($tmp[16]),
                                  "EMAIL"           => trim($tmp[17]));

                    //日付変換
                    if ($data["ISSUEDATE"] != "" && $this->dateRep($data["ISSUEDATE"])) {
                        $data["ISSUEDATE"] = $this->dateRep($data["ISSUEDATE"]);
                    }

                    //更新
                    if ($this->field["SHORI_MEI"] == "1") {
                        if ($data["EXPIREDATE"] != "" && $this->dateRep($data["EXPIREDATE"])) {
                            $data["EXPIREDATE"] = $this->dateRep($data["EXPIREDATE"]);
                        }
                    }

                    //０埋め
                    $data["SCHREGNO"] = $this->zeroUme($data["SCHREGNO"], 8);
                    if (strlen($data[$key]) && !is_numeric($data["AREACD"])) {
                        $data["AREACD"] = $this->zeroUme($data["AREACD"], 2);
                    }

                    //取り込み処理（エラーの場合、エラーＤＢへの追加）
                    if ($this->errCheck($db, $data, $checkData, $record_no, $duplicateCsv1, $primaryKey1)) {
                        $tempArr[$cnt] = $data;
                        $cnt++; //処理済件数
                    }//ifの綴り
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
    public function errCheck($db, $data, $checkData, $record_no, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg     = true;
        $hissuErrFlg   = false;    //必須エラーフラグ
        $lengthErrFlg  = false;    //レングスエラーフラグ
        $numericErrFlg = false;    //数字エラーフラグ
        $valuesErrFlg  = false;
        $orderErrFlg   = false;
        $dateErrFlg    = false;
        $masterErrFlg  = false;
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg   = "";       //必須エラーメッセージ
        $lengthErrMsg  = "";       //レングスエラーメッセージ
        $numericErrMsg = "";       //数字エラーメッセージ
        $valuesErrMsg  = "";
        $orderErrMsg   = "";
        $dateErrMsg    = "";
        $masterErrMsg  = "";
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep   = "";
        $lengthSep  = "";
        $numericSep = "";
        $valuesSep  = "";
        $orderSep   = "";
        $dateSep    = "";
        $masterSep  = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        //更新
        if ($this->field["SHORI_MEI"] == "1") {
            if ($this->Properties["chkAddrInputHankaku"] == "1") {
                if (!$this->chkHankaku($data["ADDR1"])) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "住所1");
                } elseif (!$this->chkHankaku($data["ADDR2"])) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "住所2");
                }
            }
            foreach ($checkData as $key => $val) {
                //桁数チェック
                if (!in_array($key, array("ADDR1", "ADDR2"))) {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
                if (in_array($key, array("ADDR1", "ADDR2"))) {
                    if (
                        ($this->Properties["ADDR_INPUT_SIZE"] == "" && strlen($data[$key]) > $val[0]) ||
                        ($this->Properties["ADDR_INPUT_SIZE"] != "" && mb_strlen($data[$key], 'UTF-8') > $val[0])
                    ) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }

                //必須チェック
                if (in_array($key, array("SCHREGNO", "ISSUEDATE", "ADDR1"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //数字チェック
                if (in_array($key, array("AREACD"))) {
                    if (strlen($data[$key]) && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                //１ OR NULL以外はエラー
                if (in_array($key, array("ADDR_FLG"))) {
                    if ($data["ADDR_FLG"] && $data["ADDR_FLG"] != "1") {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                }
            }
            //削除
        } else {
            //桁数チェック
            foreach ($checkData as $key => $val) {
                if (in_array($key, array("SCHREGNO", "ISSUEDATE"))) {
                    //桁数チェック
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                    //必須チェック
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            }
        }
        //存在チェック(項目チェックがＯＫの場合)
        if ($returnFlg) {
            //日付変換
            if ($data["ISSUEDATE"] != "" && !$this->dateRep($data["ISSUEDATE"])) {
                $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, "有効期間開始年月日");
            }

            //更新
            if ($this->field["SHORI_MEI"] == "1") {
                if ($data["EXPIREDATE"] != "" && !$this->dateRep($data["EXPIREDATE"])) {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, "有効期間終了年月日");
                }
                //基礎マスタ
                $table = "schreg_base_mst";
                $where = " where SCHREGNO = '".$data["SCHREGNO"]."' ";
                $sonzaiFlg = $this->sonzaiCheck($db, $data["SCHREGNO"], $table, $where);
                if (!$sonzaiFlg) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学籍番号");
                }
                //名称マスタ
                //AREACD=0,00の時は除く
                if ($data["AREACD"] !== '00') {
                    $table = "name_mst";
                    $where = " where NAMECD1 = 'A020' AND NAMECD2 = '".$data["AREACD"]."'";
                    $sonzaiFlg = $this->sonzaiCheck($db, $data["AREACD"], $table, $where);
                    if (!$sonzaiFlg) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "地区コード");
                    }
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg.$orderErrMsg.$dateErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx030Query::insertQueryErr($db, $record_no, $checkError);
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
                        "GRADE"           => "学年",
                        "HR_CLASS"        => "クラス",
                        "ATTENDNO"        => "出席番号",
                        "NAME"            => "氏名",
                        "ISSUEDATE"       => "※有効期間開始年月日",
                        "EXPIREDATE"      => "有効期間終了年月日",
                        "ZIPCD"           => "郵便番号",
                        "AREACD"          => "地区コード",
                        "ADDR1"           => "※住所1",
                        "ADDR2"           => "住所2",
                        "ADDR_FLG"        => "方書き印刷可",
                        "ADDR1_ENG"       => "住所1英字",
                        "ADDR2_ENG"       => "住所2英字",
                        "TELNO"           => "電話番号",
                        "TELNO2"          => "電話番号２",
                        "FAXNO"           => "FAX番号",
                        "EMAIL"           => "E-Mail",
                        $this->lastColumn => $this->lastColumn);

        $header_rei = array("SCHREGNO"        => "12345678",
                            "GRADE"           => "01",
                            "HR_CLASS"        => "001",
                            "ATTENDNO"        => "001",
                            "NAME"            => "アルプ　太郎",
                            "ISSUEDATE"       => "2010-04-04",
                            "EXPIREDATE"      => "2013-03-31",
                            "ZIPCD"           => "102-0093",
                            "AREACD"          => "01",
                            "ADDR1"           => "東京都千代田区平河町1-2-10",
                            "ADDR2"           => "平河町第一生命ビル",
                            "ADDR_FLG"        => "1 又は 無し",
                            "ADDR1_ENG"       => "1-2-10 Hirakawatyo Chiyoda Tokyo",
                            "ADDR2_ENG"       => "Hirakawatyo-Daiichiseimei-Bldg.",
                            "TELNO"           => "03-3234-3571",
                            "TELNO2"          => "080-3234-3571",
                            "FAXNO"           => "098-869-1785",
                            "EMAIL"           => "aaa123@alp.co.jp",
                            $this->lastColumn => $this->lastColumn);

        $contents  = "";    //クリア

        if ($this->field["OUTPUT"] == "1") {             //ヘッダ
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {       //エラー
            $tables = array();
            $tables = array("1" => "SCHREG_ADDRESS_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {       //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }
        if ($this->field["OUTPUT"] == "1") {      //ヘッダ
            $header_data = true;
        } else {                                //ヘッダ以外
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            if ($this->field["OUTPUT"] == "3") {              //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {       //データ
                $query = knjx030Query::getMainCsvData($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {          //データ
                    $row = str_replace(",", ".", $row);       //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
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
                $couse = "学籍住所データ";
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
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    public function dateRep($data)
    {
        if ($data != "") {
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
    public function zeroUme($data, $ketasuu)
    {
        $shosiki = "%0".$ketasuu."d";
        if ($data != "") {
            if (strlen($data) < $ketasuu) {
                $data = sprintf($shosiki, $data);
            }
        }

        return $data;
    }

    /**
     * マスタの存在チェック
     */
    public function sonzaiCheck(&$db, $data, $table, $where)
    {
        $sonzaiFlg = true;
        if ($data != "") {
            $sonzaiFlg = false;
            $query = knjx030Query::getMasterCheck($table, $where);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sonzaiFlg = true;
            }
        }
        return $sonzaiFlg;
    }

    public function chkHankaku($chkStr)
    {
        foreach (preg_split('//u', $chkStr, -1, PREG_SPLIT_NO_EMPTY) as $idx => $v) {
            if (strlen($v) == 1) {
                return false;
            }
        }
        return true;
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
