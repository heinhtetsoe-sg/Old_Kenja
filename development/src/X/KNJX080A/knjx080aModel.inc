<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx080aModel extends Model
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
    public function knjx080aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
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

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"                =>  VARS::post("YEAR"),                //年度一覧
                             "CHAIRCD"            =>  VARS::post("CHAIRCD"),            //講座一覧
                             "TRGTCD"            =>  VARS::post("TRGTCD"),            //年組一覧
                             "HEADER"            =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")            //処理名
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
                    //講座受講クラスデータ
                    $query = knjx080aQuery::getExistCheck($this->dataArr[$i]);
                    $result = $db->query($query);
                    $flg = false;
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $flg = true;
                    }
                    //更新
                    if ($this->field["SHORI_MEI"]=="1") {
                        if ($flg) {    //ＤＢにあれば、update
                            knjx080aQuery::updateQueryCsv($db, $this->dataArr[$i], $this->staffcd);
                        } else {    //ＤＢになければ、insert
                            knjx080aQuery::insertQueryCsv($db, $this->dataArr[$i], $this->staffcd);
                        }
                        //削除
                    } else {
                        //ＤＢにあれば、delete
                        if ($flg) {
                            knjx080aQuery::deleteQueryCsv($db, $this->dataArr[$i]);
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
            $checkdata = array("YEAR"      => array(4, "年度"),
                               "SEMESTER"  => array(1, "学期"),
                               "CHAIRCD"   => array(7, "講座コード"),
                               "GROUPCD"   => array(4, "群コード"),
                               "TRGTGRADE" => array(2, "対象年"),
                               "TRGTCLASS" => array(3, "対象組"));
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
            $primaryKey1 = $this->getPrimaryKey('CHAIR_CLS_DAT');
            $fp = fopen($this->field["FILE"]["tmp_name"], "r");        //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                        //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                                //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"]=="on" && $record_no<2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");        //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);        //2004/03/26 nakamoto add

                    $data = array("YEAR"      => trim($tmp[0]),
                                  "SEMESTER"  => trim($tmp[1]),
                                  "CHAIRCD"   => trim($tmp[2]),
                                  "GROUPCD"   => trim($tmp[4]),
                                  "TRGTGRADE" => trim($tmp[6]),
                                  "TRGTCLASS" => trim($tmp[7]));
                    //０埋め
                    $data = $this->zeroUme($data);

                    if ($this->errCheck($db, $data, $checkdata, $record_no, $duplicateCsv1, $primaryKey1)) {
                        $tempArr[$cnt] = $data;
                        $cnt++; //処理済件数
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
    public function errCheck($db, $data, $checkdata, $record_no, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //存在エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ
        $otherErrFlg        = false;    //その他エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //存在エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $duplicateCsvSep = "";
        $otherSep        = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (!strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
            }
            //数値チェック
            if (in_array($key, array_merge(array("YEAR","SEMESTER","CHAIRCD","GROUPCD","TRGTGRADE")))) {
                if ($data[$key] != '' && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }
        if ($returnFlg) {
            //講座・群項目の矛盾チェック（どちらかは０でなければならない。）
            if ($data["CHAIRCD"] != "0000000" && $data["GROUPCD"] != "0000") {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "講座・群項目エラー（どちらか一方は０を入力）");
            }
        }
        if ($returnFlg) {
            if ($this->field["SHORI_MEI"] == "1") {
                //1.講座データ
                if ($data["CHAIRCD"] != "" && $data["CHAIRCD"] != "0000000") {
                    $result = $db->getOne(knjx080aQuery::getMasterCheck($data, "1"));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "講座コード");
                    }
                }
                //2.在籍ヘッダ
                $result = $db->getOne(knjx080aQuery::getMasterCheck($data, "2"));
                if ($result != "X") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍ヘッダ");
                }
                //3.選択科目マスタ
                if ($data["GROUPCD"] != "" && $data["GROUPCD"] != "0000") {
                    $result = $db->getOne(knjx080aQuery::getMasterCheck($data, "3"));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "選択科目");
                    }
                }
                //群コードチェック
                if ($data["GROUPCD"] == "0000") {
                    $result=$db->getOne(knjx080aQuery::getGunCheck($data));
                    if (!is_null($result) && $result != '0000') {
                        $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "群に設定されているので登録不可");
                    }
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$otherErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx080aQuery::insertQueryErr($db, $record_no, $checkError);
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
                        "CHAIRNAME"       => "講座名称",
                        "GROUPCD"         => "※群コード",
                        "GROUPNAME"       => "群名称",
                        "TRGTGRADE"       => "※対象年",
                        "TRGTCLASS"       => "※対象組",
                        $this->lastColumn => $this->lastColumn);//2005.09.28nakamoto
        $header_rei = array("YEAR"            => "2004",
                            "SEMESTER"        => "1",
                            "CHAIRCD"         => "9999999",
                            "CHAIRNAME"       => "国語Ⅱ",
                            "GROUPCD"         => "9999",
                            "GROUPNAME"       => "群名称",
                            "TRGTGRADE"       => "99",
                            "TRGTCLASS"       => "999",//2004-07-21 nakamoto ２桁→３桁へ変更
                            $this->lastColumn => $this->lastColumn);//2005.09.28nakamoto

        $contents  = "";    //クリア

        if ($this->field["OUTPUT"]=="1") {                //ヘッダ
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {        //エラー
            $tables = array();
            $tables = array("1" => "CHAIR_CLS_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4") {        //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header, ",")."\n";
            }
        }
        if ($this->field["OUTPUT"]=="1") {            //ヘッダ
            $header_data = true;
        } else {                                //ヘッダ以外
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            $table = "";
            $where = "";
            if ($this->field["OUTPUT"]=="3") {                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {        //データ
                $query = knjx080aQuery::getselectDataSQL($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
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
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "講座受講クラスデータ";
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
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    public function zeroUme($data)
    {
        //更新 削除
        if ($data["CHAIRCD"] != "") {
            if (strlen($data["CHAIRCD"]) < 7) {
                $data["CHAIRCD"] = sprintf("%07d", $data["CHAIRCD"]);
            }
        }
        if ($data["GROUPCD"] != "") {
            if (strlen($data["GROUPCD"]) < 4) {
                $data["GROUPCD"] = sprintf("%04d", $data["GROUPCD"]);
            }
        }
        if ($data["TRGTGRADE"] != "") {
            if (strlen($data["TRGTGRADE"]) < 2) {
                $data["TRGTGRADE"] = sprintf("%02d", $data["TRGTGRADE"]);
            }
        }
        if ($data["TRGTCLASS"] != "") {
            if (strlen($data["TRGTCLASS"]) < 3) {
                $data["TRGTCLASS"] = sprintf("%03d", $data["TRGTCLASS"]);
            }
        }    //2004-07-21 nakamoto ２桁→３桁へ変更
        return $data;
    }

    // 受講クラスの自動生成
    public function getDownloadModel2()
    {
        // ＣＳＶ項目
        $header = array("YEAR"          => "※年度",
                        "SEMESTER"      => "※学期",
                        "CHAIRCD"       => "※講座コード",
                        "GROUPCD"       => "※群コード",
                        "TRGTGRADE"     => "※対象年",
                        "TRGTCLASS"     => "※対象組");

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $header_data = false;
        $contents  = "";
        $contents .= implode($header, ",")."\n";

        $db->query(knjx080aQuery::deleteChairClsDat(CTRL_YEAR, CTRL_SEMESTER));

        // 0:HRな講座 1:群
        for ($flg = 0; $flg < 2; $flg++) {
            $query = knjx080aQuery::selectAutoChairCls(CTRL_YEAR, CTRL_SEMESTER, $flg);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                // DB追加
                $query = knjx080aQuery::insertAutoChairCls($row, CTRL_YEAR, CTRL_SEMESTER, STAFFCD, $flg);
                $db->query($query);
                // CSV出力
                $row = str_replace(",", ".", $row); // データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $contents .= CTRL_YEAR .",";
                $contents .= CTRL_SEMESTER .",";
                if ($flg == 0) {
                    $contents .= $row["CHAIRCD"] .",";
                    $contents .= "0000" .",";
                } else {
                    $contents .= "0000000" .",";
                    $contents .= $row["GROUPCD"] .",";
                }
                $contents .= $row["GRADE"] .",";
                $contents .= $row["HR_CLASS"] ."\n";
                $header_data = true;
            }
            $result->free();
        }

        $db->commit();
        Query::dbCheckIn($db);

        // ＣＳＶ出力
        if ($header_data) {
            if (!$this->Properties["useXLS"]) {
                $filename = sprintf("%s.csv", "講座受講クラスデータ(自動生成)");
                common::downloadContents($contents, $filename);
            } else {
                $this->warning = "データを更新しました。";
                return false;
            }
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
