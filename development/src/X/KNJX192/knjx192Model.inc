<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx192Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $gyou;
    public $schoolName;
    public $schoolCode;
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $maxPattern;
    public $lastColumn;

    /**
     * メンバ変数の初期化
     */
    public function knjx192Model()
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
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd          = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        //最初にこのプログラムが呼ばれたとき
        if ($this->cmd == "") {
            $this->program_id = VARS::request("program_id") ? VARS::request("program_id") : PROGRAMID;
            $this->mode       = VARS::request("mode")       ? VARS::request("mode")       : 'ungrd';
        }
        define("AUTHORITY2", common::SecurityCheck(STAFFCD, $this->program_id));   //セキュリティチェック

        //学校名取得
        $query = knjx192query::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->tyousasyoEMPTokuBetuFieldSize = (strlen($this->Properties["tyousasyoEMPTokuBetuFieldSize"])) ? $this->Properties["tyousasyoEMPTokuBetuFieldSize"] : 0;

        if ($this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $this->maxPattern = 3;
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
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx192Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx192Query::deleteQueryCsv($this, $this->data_arr);
                }

                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();//更新用の配列
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $this->gyou = array();
            if ($this->schoolName == 'miyagiken') {
                $this->gyou["JOBHUNT_RECOMMEND"]["GYOU"] = 16;
                $this->gyou["JOBHUNT_RECOMMEND"]["MOJI"] = 38;
            } elseif ($this->tyousasyoEMPTokuBetuFieldSize == 1) {
                $this->gyou["JOBHUNT_RECOMMEND"]["GYOU"] = 13;
                $this->gyou["JOBHUNT_RECOMMEND"]["MOJI"] = 38;
            } else {
                $this->gyou["JOBHUNT_RECOMMEND"]["GYOU"] = 10;
                $this->gyou["JOBHUNT_RECOMMEND"]["MOJI"] = 38;
            }

            if ($this->tyousasyoEMPTokuBetuFieldSize == 1) {
                $this->gyou["JOBHUNT_REC"]["GYOU"] = 10;
                $this->gyou["JOBHUNT_REC"]["MOJI"] = 20;
            } else {
                $this->gyou["JOBHUNT_REC"]["GYOU"] = 8;
                $this->gyou["JOBHUNT_REC"]["MOJI"] = 20;
            }

            if ($this->tyousasyoEMPTokuBetuFieldSize == 1) {
                $this->gyou["JOBHUNT_ABSENCE"]["GYOU"] = 6;
                $this->gyou["JOBHUNT_ABSENCE"]["MOJI"] = 10;
            } else {
                $this->gyou["JOBHUNT_ABSENCE"]["GYOU"] = 4;
                $this->gyou["JOBHUNT_ABSENCE"]["MOJI"] = 10;
            }

            $this->gyou["JOBHUNT_HEALTHREMARK"]["GYOU"] = 3;
            $this->gyou["JOBHUNT_HEALTHREMARK"]["MOJI"] = 14;

            if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $checkdata = array("SCHREGNO"               => array(array(8,1),"学籍番号"),
                                   "PATTERN_SEQ"            => array(array(1,1),"パターン"),
                                   "JOBHUNT_REC"            => array(array($this->gyou["JOBHUNT_REC"]["MOJI"],$this->gyou["JOBHUNT_REC"]["GYOU"]),"特別活動の記録"),
                                   "JOBHUNT_ABSENCE"        => array(array($this->gyou["JOBHUNT_ABSENCE"]["MOJI"],$this->gyou["JOBHUNT_ABSENCE"]["GYOU"]),"欠席の主な理由"),
                                   "JOBHUNT_HEALTHREMARK"   => array(array($this->gyou["JOBHUNT_HEALTHREMARK"]["MOJI"],$this->gyou["JOBHUNT_HEALTHREMARK"]["GYOU"]),"身体状況備考"),
                                   "JOBHUNT_RECOMMEND"      => array(array($this->gyou["JOBHUNT_RECOMMEND"]["MOJI"],$this->gyou["JOBHUNT_RECOMMEND"]["GYOU"]),"本人の長所・推薦事由"));
            } else {
                $checkdata = array("SCHREGNO"               => array(array(8,1),"学籍番号"),
                                   "JOBHUNT_REC"            => array(array($this->gyou["JOBHUNT_REC"]["MOJI"],$this->gyou["JOBHUNT_REC"]["GYOU"]),"特別活動の記録"),
                                   "JOBHUNT_ABSENCE"        => array(array($this->gyou["JOBHUNT_ABSENCE"]["MOJI"],$this->gyou["JOBHUNT_ABSENCE"]["GYOU"]),"欠席の主な理由"),
                                   "JOBHUNT_HEALTHREMARK"   => array(array($this->gyou["JOBHUNT_HEALTHREMARK"]["MOJI"],$this->gyou["JOBHUNT_HEALTHREMARK"]["GYOU"]),"身体状況備考"),
                                   "JOBHUNT_RECOMMEND"      => array(array($this->gyou["JOBHUNT_RECOMMEND"]["MOJI"],$this->gyou["JOBHUNT_RECOMMEND"]["GYOU"]),"本人の長所・推薦事由"));
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
            $primaryKey1 = array();
            $primaryKey2 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            if ($this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $primaryKey1 = $this->getPrimaryKey('HEXAM_EMPREMARK_SEQ_DAT');
            } else {
                $primaryKey1 = $this->getPrimaryKey('HEXAM_EMPREMARK_DAT');
            }
            if ($this->mode == 'grd') {
                $primaryKey2 = $this->getPrimaryKey('GRD_HEXAM_EMPREMARK_DAT');
            }
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
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

                    if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                        $data = array("SCHREGNO"                => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                      "PATTERN_SEQ"             => $tmp[5],
                                      "JOBHUNT_REC"             => $tmp[6],
                                      "JOBHUNT_ABSENCE"         => $tmp[7],
                                      "JOBHUNT_HEALTHREMARK"    => $tmp[8],
                                      "JOBHUNT_RECOMMEND"       => $tmp[9]);
                    } else {
                        $data = array("SCHREGNO"                => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                      "JOBHUNT_REC"             => $tmp[5],
                                      "JOBHUNT_ABSENCE"         => $tmp[6],
                                      "JOBHUNT_HEALTHREMARK"    => $tmp[7],
                                      "JOBHUNT_RECOMMEND"       => $tmp[8]);
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    if ($this->mode == 'grd') {
                        foreach ($primaryKey2 as $key => $val) {
                            $duplicateCsv2[$line][$val] = $data[$val];
                        }
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
        $orderErrFlg        = false;
        $existenceErrFlg    = false;    //存在エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $orderErrMsg        = "";
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $orderSep        = "";
        $existenceErrSep = "";
        $duplicateCsvSep = "";
        $masterSep       = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        if ($this->mode == 'grd') {
            $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
            if ($dupFlg2[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
            }
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("SCHREGNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("SCHREGNO"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == 1) {
                //必須項目チェック
                if (in_array($key, array("PATTERN_SEQ"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //数値チェック
                if (in_array($key, array("PATTERN_SEQ"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if ($this->validate_row_cnt($data[$key], ($val[0][0] * 2)) > $val[0][1]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
                if ($this->validateKinsoku($data[$key])) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, $val[1]);
                }
            }

            //存在チェック
            if (in_array($key, array("SCHREGNO"))) {
                $query = "SELECT COUNT(*) FROM SCHREG_BASE_MST WHERE SCHREGNO = '".$data[$key]."'";
                $cnt = $db->getOne($query);
                if ($cnt < 1) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された学籍番号がありません。");
                }
            }

            if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == 1) {
                //パターン上限チェック
                if (in_array($key, array("PATTERN_SEQ"))) {
                    if ($data[$key] != "" && $data[$key] > $this->maxPattern) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※パターン上限:", $existenceErrSep, $val[1]);
                    }
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$orderErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx192Query::insertQueryErr($db, $line, $checkError);
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
        $header_err = array("MSGROW"                => "行番号",
                            "MSGREMARK"             => "メッセージ");

        if ($this->mode != 'grd' && $this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $header = array("GRADE"                     => "学年",
                            "HR_CLASS"                  => "クラス",
                            "ATTENDNO"                  => "出席番号",
                            "NAME"                      => "氏名",
                            "SCHREGNO"                  => "※学籍番号",
                            "PATTERN_SEQ"               => "※パターン",
                            "JOBHUNT_REC"               => "特別活動の記録",
                            "JOBHUNT_ABSENCE"           => "欠席の主な理由",
                            "JOBHUNT_HEALTHREMARK"      => "身体状況備考",
                            "JOBHUNT_RECOMMEND"         => "本人の長所・推薦事由",
                            "LASTCOLUMN"                => $this->lastColumn);

            $header_rei = array("GRADE"                 => "99",
                                "HR_CLASS"              => "999",
                                "ATTENDNO"              => "999",
                                "NAME"                  => "アルプ太郎",
                                "SCHREGNO"              => "12345678",
                                "PATTERN_SEQ"           => "1",
                                "JOBHUNT_REC"           => "全角".$this->gyou["JOBHUNT_REC"]["MOJI"] * $this->gyou["JOBHUNT_REC"]["GYOU"]."文字",
                                "JOBHUNT_ABSENCE"       => "全角".$this->gyou["JOBHUNT_ABSENCE"]["MOJI"] * $this->gyou["JOBHUNT_ABSENCE"]["GYOU"]."文字",
                                "JOBHUNT_HEALTHREMARK"  => "全角".$this->gyou["JOBHUNT_HEALTHREMARK"]["MOJI"] * $this->gyou["JOBHUNT_HEALTHREMARK"]["GYOU"]."文字",
                                "JOBHUNT_RECOMMEND"     => "全角".$this->gyou["JOBHUNT_RECOMMEND"]["MOJI"] * $this->gyou["JOBHUNT_RECOMMEND"]["GYOU"]."文字",
                                "LASTCOLUMN"            => $this->lastColumn);
        } else {
            $header = array("GRADE"                     => "学年",
                            "HR_CLASS"                  => "クラス",
                            "ATTENDNO"                  => "出席番号",
                            "NAME"                      => "氏名",
                            "SCHREGNO"                  => "※学籍番号",
                            "JOBHUNT_REC"               => "特別活動の記録",
                            "JOBHUNT_ABSENCE"           => "欠席の主な理由",
                            "JOBHUNT_HEALTHREMARK"      => "身体状況備考",
                            "JOBHUNT_RECOMMEND"         => "本人の長所・推薦事由",
                            "LASTCOLUMN"                => $this->lastColumn);

            $header_rei = array("GRADE"                 => "99",
                                "HR_CLASS"              => "999",
                                "ATTENDNO"              => "999",
                                "NAME"                  => "アルプ太郎",
                                "SCHREGNO"              => "12345678",
                                "JOBHUNT_REC"           => "全角".$this->gyou["JOBHUNT_REC"]["MOJI"] * $this->gyou["JOBHUNT_REC"]["GYOU"]."文字",
                                "JOBHUNT_ABSENCE"       => "全角".$this->gyou["JOBHUNT_ABSENCE"]["MOJI"] * $this->gyou["JOBHUNT_ABSENCE"]["GYOU"]."文字",
                                "JOBHUNT_HEALTHREMARK"  => "全角".$this->gyou["JOBHUNT_HEALTHREMARK"]["MOJI"] * $this->gyou["JOBHUNT_HEALTHREMARK"]["GYOU"]."文字",
                                "JOBHUNT_RECOMMEND"     => "全角".$this->gyou["JOBHUNT_RECOMMEND"]["MOJI"] * $this->gyou["JOBHUNT_RECOMMEND"]["GYOU"]."文字",
                                "LASTCOLUMN"            => $this->lastColumn);
        }

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode(",", $header)."\n";
            $contents .= implode(",", $header_rei)."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $tables = array();
            if ($this->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $tables = array("1" => "HEXAM_EMPREMARK_SEQ_DAT");
            } else {
                $tables = array("1" => "HEXAM_EMPREMARK_DAT");
            }
            if ($this->mode == 'grd') {
                $tables = array("2" => "GRD_HEXAM_EMPREMARK_DAT");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode(",", $header_err)."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode(",", $header)."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knjx192Query::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {                //データ
                    $row = str_replace(",", ".", $row);  //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                }
                $contents .= implode(",", $row) ."\n";
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
                if ($this->mode == 'grd') {
                    $couse = "調査書就職用所見データ(卒業)";
                } else {
                    $couse = "調査書就職用所見データ";
                }
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
