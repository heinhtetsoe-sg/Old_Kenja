<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx140aModel extends Model
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
    public function knjx140aModel()
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
                             "SEQ"              =>  VARS::post("SEQ"),              //ＳＥＱとタイトル
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
        //ＳＥＱチェック用の配列
        $data_ptrn = "";
        //データの整合性チェック
        if ($this->isDataMatched($this->dataArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    //更新
                    if ($this->field["SHORI_MEI"] == "1") {
                        //時間割パターンヘッダデータ・・・ＣＳＶデータの先頭行のみ処理（１回だけ処理）
                        if ($data_ptrn == "") {
                            knjx140aquery::InsertTableQuery($this, "sch_ptrn_hdat", $db, $this->dataArr[$i]);
                        }
                        //時間割パターンデータ
                        knjx140aquery::InsertTableQuery($this, "sch_ptrn_dat", $db, $this->dataArr[$i]);
                        //時間割パターン職員データ・・・ＣＳＶデータの職員コードが入力されている場合のみ処理
                        if (strlen(trim($this->dataArr[$i]["STAFFCD"]))) {
                            knjx140aquery::InsertTableQuery($this, "sch_ptrn_stf_dat", $db, $this->dataArr[$i]);
                        }
                        //時間割パターン集計フラグ
                        knjx140aquery::InsertTableQuery($this, "sch_ptrn_countflg_dat", $db, $this->dataArr[$i]);
                        $data_ptrn = "off";
                    //削除
                    } else {
                        //時間割パターンデータ
                        $query = knjx140aquery::getDeleteQuery($this, "sch_ptrn_dat", $this->dataArr[$i]);
                        $db->query($query);
                        //時間割パターン職員データ・・・講座単位で削除
                        $query = knjx140aquery::getDeleteQuery($this, "sch_ptrn_stf_dat", $this->dataArr[$i]);
                        $db->query($query);
                        //時間割パターン職員データ・・・講座単位で削除
                        $query = knjx140aquery::getDeleteQuery($this, "sch_ptrn_countflg_dat", $this->dataArr[$i]);
                        $db->query($query);
                        //時間割パターンヘッダデータ・・・時間割パターンデータがゼロ件の場合のみ処理
                        $ret_val = $db->getOne(knjx140aquery::getExistsQuery($this, $this->dataArr[$i]));
                        if ($ret_val == "0") {
                            $query = knjx140aquery::getDeleteQuery($this, "sch_ptrn_hdat", $this->dataArr[$i]);
                            $db->query($query);
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
            $checkdata = array("YEAR"     => array(4, "年度"),
                               "SEMESTER" => array(1, "学期"),
                               "BSCSEQ"   => array(2, "SEQ(パターン)"),
                               "TITLE"    => array(45, "タイトル"),
                               "DAYCD"    => array(1, "曜日コード"),
                               "PERIODCD" => array(1, "校時コード"),
                               "CHAIRCD"  => array(7, "講座コード"),
                               "STAFFCD"  => array(8, "時間割担当（職員コード）"));
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $i = 0;
            $record_no = 1;         //行番号(初期値)
            $primaryKey1 = array();
            $primaryKey2 = array();
            $primaryKey3 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
            $primaryKey1 = $this->getPrimaryKey('SCH_PTRN_DAT');
            $primaryKey2 = $this->getPrimaryKey('SCH_PTRN_STF_DAT');
            $primaryKey3 = $this->getPrimaryKey('SCH_PTRN_COUNTFLG_DAT');

            //ＳＥＱチェック用の配列
            $data_seq = "";

            //ＳＥＱチェック用の配列
            $data_ptrn = "";

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

                    $data = array("YEAR"        => trim($tmp[0]),
                                  "SEMESTER"    => trim($tmp[1]),
                                  "BSCSEQ"      => trim($tmp[2]),
                                  "TITLE"       => trim($tmp[3]),
                                  "DAYCD"       => trim($tmp[4]),
                                  "PERIODCD"    => trim($tmp[5]),
                                  "CHAIRCD"     => trim($tmp[6]),
                                  "STAFFCD"     => trim($tmp[8]));

                    //０埋め
                    $data = $this->zeroUme($data);

                    //ＳＥＱチェック配列にセット
                    $data_cmp = $data["YEAR"] .$data["SEMESTER"] .$data["BSCSEQ"];
                    if ($data_seq == "" && strlen(trim($data_cmp))) {
                        $data_seq = $data_cmp;
                    }
                    if ($this->errCheck($db, $data, $checkdata, $record_no, $data_seq, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$record_no][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$record_no][$val] = $data[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$record_no][$val] = $data[$val];
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
    public function errCheck($db, $data, $checkdata, $line, $data_seq, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数値エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ
        $valuesErrFlg       = false;    //値エラーフラグ

        $hissuErrMsg        = "";     //必須エラーメッセージ
        $numericErrMsg      = "";     //数値エラーメッセージ
        $lengthErrMsg       = "";     //レングスエラーメッセージ
        $masterErrMsg       = "";     //マスタエラーメッセージ
        $existenceErrMsg    = "";     //存在エラーメッセージ
        $duplicateCsvErrMsg = "";     //重複(CSV)メッセージ
        $valuesErrMsg       = "";     //値エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $existenceSep    = "";
        $duplicateCsvSep = "";
        $valuesSep       = "";

        $flg = false;
        $data_ptrn = "";
        if ($this->field["SHORI_MEI"] == "1") {
            //重複(CSV)チェック
            $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
            if ($dupFlg1[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
            }
            $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
            if ($dupFlg2[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
            }
            $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data);
            if ($dupFlg3[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg3[1] ."行目)");
            }
        } else {
            //重複(CSV)チェック
            $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
            if ($dupFlg2[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg2[1] ."行目)");
            }
            $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data);
            if ($dupFlg3[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg3[1] ."行目)");
            }
            $ret_val = $db->getOne(knjx140aquery::getExistsQuery($this, $data));
            if ($ret_val == "0") {
                $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
                if ($dupFlg1[0] == 1) {
                    $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複4(". $dupFlg1[1] ."行目)");
                }
            }
        }
        foreach ($checkdata as $key => $val) {
            //必須チェック
            if (in_array($key, array("YEAR", "SEMESTER", "BSCSEQ", "TITLE", "DAYCD", "PERIODCD", "CHAIRCD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
            //数値チェック
            if (in_array($key, array("YEAR", "SEMESTER", "BSCSEQ", "DAYCD","CHAIRCD"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //値チェック
            if ($key == "DAYCD" && $data[$key] != "" && !in_array($data["DAYCD"], array("1","2","3","4","5","6","7"))) {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
            }
        }
        if ($returnFlg) {
            if ($this->field["SHORI_MEI"] == "1") {
                //職名マスタ
                $result = $db->getOne(knjx140aQuery::getMasterCheck($data, "1"));
                if ($result != "X") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "名称");
                }
                $result = $db->getOne(knjx140aQuery::getMasterCheck($data, "2"));
                if ($result != "X") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "講座コード");
                }
                if ($data["STAFFCD"] != "") {
                    $result = $db->getOne(knjx140aQuery::getMasterCheck($data, "3"));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "職員年度");
                    }
                }
            }
            //ＳＥＱチェック配列にセット
            $data_cmp = $data["YEAR"] .$data["SEMESTER"] .$data["BSCSEQ"];
            //ＳＥＱチェック(一度に取込可能なＳＥＱは、１種類のみとする・・・処理軽減のため)
            if ($data_seq != $data_cmp) {
                $flg = true;
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "一度に取込可能なＳＥＱ（パターン）は１種類のみです。");
            }
            if (!$flg) {
                //ＳＥＱチェック(パターン反映履歴データに存在するＳＥＱは、取込は許可しない)
                if (knjx140aQuery::getReflectCheckQuery($data, $db)) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "通常時間割に反映した基本時間割は、取込できません。");
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg.$masterErrMsg.$existenceErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx140aQuery::insertQueryErr($db, $line, $checkError);
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
                        "BSCSEQ"          => "※ＳＥＱ（パターン）",
                        "TITLE"           => "※タイトル",
                        "DAYCD"           => "※曜日コード",
                        "PERIODCD"        => "※校時コード",
                        "CHAIRCD"         => "※講座コード",
                        "CHAIRNAME"       => "講座名称",
                        "STAFFCD"         => "時間割担当（職員コード）",
                        "STAFFNAME"       => "職員氏名",
                        $this->lastColumn => $this->lastColumn);
        $header_rei = array("YEAR"            => "2005",
                            "SEMESTER"        => "2",
                            "BSCSEQ"          => "6",
                            "TITLE"           => "2005年2学期新バージョン",
                            "DAYCD"           => "2",
                            "PERIODCD"        => "4",
                            "CHAIRCD"         => "7777777",
                            "CHAIRNAME"       => "国語Ⅱ",
                            "STAFFCD"         => "88888888",
                            "STAFFNAME"       => "広島　太郎",
                            $this->lastColumn => $this->lastColumn);

        $contents  = "";    //クリア

        if ($this->field["OUTPUT"] == "1") {
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {     //エラー
            $headerDuplicate = array("YEAR"     => "※年度",
                                     "SEMESTER" => "※学期",
                                     "BSCSEQ"   => "※ＳＥＱ（パターン）",
                                     "DAYCD"    => "※曜日コード",
                                     "PERIODCD" => "※校時コード",
                                     "CHAIRCD"  => "※講座コード",
                                     "STAFFCD"  => "時間割担当（職員コード）",
                                     "GRADE"    => "学年",
                                     "HR_CLASS" => "組");
            $tables = array();
            if ($this->field["SHORI_MEI"] == "1") {
                $tables = array("1" => "SCH_PTRN_HDAT", "2" => "SCH_PTRN_DAT", "3" => "SCH_PTRN_STF_DAT", "4" => "SCH_PTRN_COUNTFLG_DAT");
            } else {
                $tables = array("1" => "SCH_PTRN_DAT", "2" => "SCH_PTRN_STF_DAT", "3" => "SCH_PTRN_COUNTFLG_DAT", "4" => "SCH_PTRN_HDAT");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
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
                $query = knjx140aQuery::getselectDataSQL($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {                //データ
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
            if ($this->field["OUTPUT"] == "3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "基本時間割データ";
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
        if ($data["CHAIRCD"] != "") {
            if (strlen($data["CHAIRCD"]) < 7) {
                $data["CHAIRCD"] = sprintf("%07d", $data["CHAIRCD"]);
            }
        }
        if ($data["STAFFCD"] != "") {
            if (strlen($data["STAFFCD"]) < 8) {
                $data["STAFFCD"] = sprintf("%08s", $data["STAFFCD"]);
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
