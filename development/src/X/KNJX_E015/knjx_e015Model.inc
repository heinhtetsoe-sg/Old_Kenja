<?php

require_once('for_php7.php');
class knjx_e015Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $schoolName;
    public $schoolCode;
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx_e015Model()
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
            $this->selectSchoolKind = VARS::get("SELECT_SCHKIND");
            $this->gradeHrClass = VARS::get("GRADE_HR_CLASS");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "GRADE_HR_CLASS"   =>  (VARS::post("GRADE_HR_CLASS")) ? VARS::post("GRADE_HR_CLASS") : $this->gradeHrClass,    //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd  = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        //最初にこのプログラムが呼ばれたとき
        if ($this->cmd == "") {
            $this->program_id = VARS::request("program_id") ? VARS::request("program_id") : PROGRAMID;
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->gyou = 66;
        $this->moji = 15;

        if ($this->Properties["unuse_KNJE015_HEXAM_ENTREMARK_LEARNING_DAT_REMARK"] !== '1') {
            //まなびの記録
            $this->remark_moji = 50;
            $this->remark_gyou = 36;

            if ($this->Properties["HEXAM_ENTREMARK_LEARNING_DAT__REMARK"] != '') {
                $this->remark_title = $this->Properties["HEXAM_ENTREMARK_LEARNING_DAT__REMARK"];
            } else {
                $this->remark_title = "備考";
            }
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
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1" || $this->field["SHORI_MEI"] == "3") {
                    knjx_e015Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_e015Query::deleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $setVal = ($this->moji * 3 * $this->gyou + $this->gyou);

            $checkdata = array();
            $checkdata["YEAR"]          = array(4,  "HISSU",  "INT",  "年度", '', '');
            $checkdata["SEMESTER"]      = array(4,  "",  "INT",  "学期", '', '');
            $checkdata["SCHREGNO"]      = array(8,  "HISSU",  "INT",  "学籍番号", '', '');
            
            $checkdata["SEQ001"]          = array($setVal,  "REMARK",       "",     "学習における特徴等", $this->moji, $this->gyou);
            $checkdata["SEQ002"]          = array($setVal,  "REMARK",       "",     "行動の特徴、特技等", $this->moji, $this->gyou);
            $checkdata["SEQ003"]          = array($setVal,  "REMARK",       "",     "部活動、ボランティア活動、留学、海外経験等", $this->moji, $this->gyou);
            $checkdata["SEQ004"]          = array($setVal,  "REMARK",       "",     "取得資格、検定等", $this->moji, $this->gyou);
            $checkdata["SEQ005"]          = array($setVal,  "REMARK",       "",     "表彰・顕彰等の記録", $this->moji, $this->gyou);
            $checkdata["SEQ006"]          = array($setVal,  "REMARK",       "",     "その他", $this->moji, $this->gyou);
            if ($this->Properties["unuse_KNJE015_HEXAM_ENTREMARK_LEARNING_DAT_REMARK"] !== '1') {
                $checkdata["REMARK"]          = array($setVal,  "",       "",     $this->remark_title, $this->remark_moji, $this->remark_gyou);
            }
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $primaryKey2   = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $primaryKey1   = $this->getPrimaryKey('HEXAM_ENTREMARK_TRAINREF_DAT');
            $primaryKey2   = $this->getPrimaryKey('HEXAM_ENTREMARK_LEARNING_DAT');
            while (!feof($fp)) {
                $b = fgets($fp, 4096);
                $buffer = $b;
                while (strlen($b) == (4096 - 1) && trim($b, "\n\r") == $b) {
                    $b = fgets($fp, 4096);
                    $buffer .= $b;
                }
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $tmp    = str_replace("↓", "\n", $tmp);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    $tmpcnt = 0;
                    $data = array();
                    $schoolName =  knjx_e015Query::getSchoolName();
                    if ($schoolName == 'kumamoto') {
                        $data["YEAR"]                  = sprintf("%04d", $tmp[4]);
                        $data["SEMESTER"]              = sprintf("%01d", $tmp[5]);
                        $data["SCHREGNO"]              = sprintf("%08d", $tmp[6]);
                    } else {
                        $data["YEAR"]                  = $tmp[4];
                        $data["SEMESTER"]              = $tmp[5];
                        $data["SCHREGNO"]              = $tmp[6];
                    }
                    $data["SEQ001"]                = $tmp[7];
                    $data["SEQ002"]                = $tmp[8];
                    $data["SEQ003"]                = $tmp[9];
                    $data["SEQ004"]                = $tmp[10];
                    $data["SEQ005"]                = $tmp[11];
                    $data["SEQ006"]                = $tmp[12];
                    $data["TRAIN_SEQ"]             = "";
                    if ($this->Properties["unuse_KNJE015_HEXAM_ENTREMARK_LEARNING_DAT_REMARK"] !== '1') {
                        $data["REMARK"]                = $tmp[13];
                        $dataCnt = 15;
                    } else {
                        $dataCnt = 14;
                    }

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $orderErrFlg        = false;    //担任エラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $orderErrMsg        = "";       //担任エラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $orderSep        = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }

            if ($this->field["SHORI_MEI"] == "1" || $this->field["SHORI_MEI"] == "3") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                }
            }
            if ($val[4]!='' && $val[5]!='') {
                //行数チェック
                $checkdata = $data[$key];
                if ($this->field["SHORI_MEI"] == "3") {
                    $dbdata = "";
                    if (preg_match("/SEQ00[1-6]/", $key)) {
                        $query = knjx_e015Query::getHexamEntremarkTrainrefDat($data["YEAR"], $data["SCHREGNO"], substr($key, 3, 3));
                        $dbdata = $db->getOne($query);
                    } elseif ($key == "REMARK") {
                        $query = knjx_e015Query::getHexamEntremarkLearningDat($data["YEAR"], $data["SCHREGNO"]);
                        $dbdata = $db->getOne($query);
                    }
                    if (substr($dbdata, -1) != "\n") {
                        $dbdata .= "\n";
                    }
                    $checkdata = $dbdata.$checkdata;
                }

                if ($this->validate_row_cnt($checkdata, ($val[4] * 2)) > $val[5]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                }
            }
        }
        //存在チェック
        $baseCnt = $db->getOne(knjx_e015Query::getExistsSchregBase($data["SCHREGNO"]));
        if ($baseCnt < 1) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学籍基礎データ");
        }
        //存在チェック
        $sem_check = $db->getCol(knjx_e015Query::checkSemester($data["YEAR"]));
        if ($data["SEMESTER"] != "" && !in_array($data["SEMESTER"], $sem_check)) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された学期データがありません。");
        }

        //参照・更新可（制限付き）の場合 担当クラス以外は更新しない
        if ($this->Properties["KNJX_E015_checkAuth"] == "1") {
            if ($this->auth == DEF_REFER_RESTRICT || $this->auth == DEF_UPDATE_RESTRICT) {
                $query = knjx_e015Query::getGradeHrclassSql($this, $data["YEAR"], $data["SCHREGNO"]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($row["VALUE"] == '') {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "無");
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg.$orderErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_e015Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    public function validateKinsoku($str)
    {
        if ($this->Properties["tyousasyoKinsokuForm"] == '1') {
            if (false !== strpos($str, '　ー') || false !== strpos($str, ' ー')) {
                return true;
            }
        }
        return false;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        /************/
        /* ヘッダー */
        /************/
        $header = array();
        
        $header["GRADE"]            = "学年";
        $header["HR_CLASS"]         = "クラス";
        $header["ATTENDNO"]         = "出席番号";
        $header["NAME"]             = "氏名";
        $header["YEAR"]             = "※年度";
        $header["SEMESTER"]         = "学期";
        $header["SCHREGNO"]         = "※学籍番号";

        $header["SEQ001"]           = "学習における特徴等";
        $header["SEQ002"]           = "行動の特徴、特技等";
        $header["SEQ003"]           = "部活動、ボランティア活動、留学、海外経験等";
        $header["SEQ004"]           = "取得資格、検定等";
        $header["SEQ005"]           = "表彰・顕彰等の記録";
        $header["SEQ006"]           = "その他";
        if ($this->Properties["unuse_KNJE015_HEXAM_ENTREMARK_LEARNING_DAT_REMARK"] !== '1') {
            $header["REMARK"]           = $this->remark_title;
        }
        $header[$this->lastColumn]  = $this->lastColumn;

        /**************/
        /* ヘッダー例 */
        /**************/
        $header_rei = array();

        $header_rei["GRADE"]            = "99";
        $header_rei["HR_CLASS"]         = "999";
        $header_rei["ATTENDNO"]         = "999";
        $header_rei["NAME"]             = "アルプ太郎";
        $header_rei["YEAR"]             = "9999";
        $header_rei["SEMESTER"]         = "9";
        $header_rei["SCHREGNO"]         = "12345678";

        $header_rei["SEQ001"]           = "全角".($this->moji * $this->gyou)."文字";
        $header_rei["SEQ002"]           = "全角".($this->moji * $this->gyou)."文字";
        $header_rei["SEQ003"]           = "全角".($this->moji * $this->gyou)."文字";
        $header_rei["SEQ004"]           = "全角".($this->moji * $this->gyou)."文字";
        $header_rei["SEQ005"]           = "全角".($this->moji * $this->gyou)."文字";
        $header_rei["SEQ006"]           = "全角".($this->moji * $this->gyou)."文字";
        if ($this->Properties["unuse_KNJE015_HEXAM_ENTREMARK_LEARNING_DAT_REMARK"] !== '1') {
            $header_rei["REMARK"]           = "全角".($this->remark_moji * $this->remark_gyou)."文字";
        }

        $header_rei[$this->lastColumn]  = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {
            //エラー
            $headerDuplicate = array("YEAR"       => "※年度",
                                     "SCHREGNO"   => "※学籍番号",
                                     "TRAIN_SEQ"  => "SEQ");
            $tables = array();
            $tables = array("1" => "HEXAM_ENTREMARK_TRAINREF_DAT", "2" => "HEXAM_ENTREMARK_LEARNING_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4") {
            //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx_e015Query::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);  //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
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
                $couse = "指導上参考となる諸事項(6分割)";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
//var_dump($_REQUEST);
