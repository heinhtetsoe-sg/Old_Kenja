<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx_d138jModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $fieldSize = array();
    public $fieldSize2 = array();
    public $fieldSize3 = array();
    public $candidate = array();
    public $schoolkind;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx_d138jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    public function init()
    {
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),          //学期
                             "OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");
        if (VARS::get("FIELDSIZE")) {
            $this->fieldSize = array();
            $fieldget = preg_split("{,}", VARS::get("FIELDSIZE"));
            for ($i = 0; $i < get_count($fieldget); $i++) {
                list($fName, $fSize, $fMeisyou) = preg_split("{=}", $fieldget[$i]);
                if ($fSize > 0) {
                    $this->fieldSize[$fName]["SIZE"] = $fSize;
                    $this->fieldSize[$fName]["MEISYOU"] = $fMeisyou;
                }
            }
        }
        if (VARS::get("FIELDSIZE2")) {
            $this->fieldSize2 = array();
            $fieldget = preg_split("{,}", VARS::get("FIELDSIZE2"));
            for ($i = 0; $i < get_count($fieldget); $i++) {
                list($fName, $fSize, $fMeisyou) = preg_split("{=}", $fieldget[$i]);
                if ($fSize > 0) {
                    $this->fieldSize2[$fName]["SIZE"] = $fSize;
                    $this->fieldSize2[$fName]["MEISYOU"] = $fMeisyou;
                }
            }
        }
        if (VARS::get("FIELDSIZE3")) {
            $this->fieldSize3 = array();
            $fieldget = preg_split("{,}", VARS::get("FIELDSIZE3"));
            for ($i = 0; $i < get_count($fieldget); $i++) {
                list($fName, $fSize, $fMeisyou) = preg_split("{=}", $fieldget[$i]);
                if ($fSize > 0) {
                    $this->fieldSize3[$fName]["SIZE"] = $fSize;
                    $this->fieldSize3[$fName]["MEISYOU"] = $fMeisyou;
                }
            }
        }
        $this->schoolkind = VARS::get("SCHOOL_KIND") ? VARS::get("SCHOOL_KIND") : "";

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
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
            if ($this->field["SHORI_MEI"] == "1") {
                $cnt = knjx_d138jQuery::insertQueryCsv($this, $this->data_arr);
            } else {
                $cnt = knjx_d138jQuery::deleteQueryCsv($this, $this->data_arr);
            }
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_DAT");
            Query::dbCheckIn($db);

            $this->setMessage(MSG202, "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("YEAR"       => array(4, "年度"),
                               "SEMESTER"   => array(4, "学期"),
                               "SCHREGNO"   => array(8, "学籍番号"));
            //対象項目のみデータに追加する。
            foreach ($this->fieldSize2 as $key => $val) {
                $checkdata[$key] = array(array($val["SIZE"], $val["MEISYOU"]));
            }
            foreach ($this->fieldSize3 as $key => $val) {
                $checkdata[$key] = array(array($val["SIZE"], $val["MEISYOU"]));
            }
            foreach ($this->fieldSize as $key => $val) {
                $checkdata[$key] = array(array($val["SIZE"], $val["MEISYOU"]));
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjx_d138jQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
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

                    $tmpcnt = 0;
                    $data = array("GRADE"      => $tmp[$tmpcnt++],
                                  "HR_CLASS"   => $tmp[$tmpcnt++],
                                  "ATTENDNO"   => $tmp[$tmpcnt++],
                                  "NAME"       => $tmp[$tmpcnt++],
                                  "YEAR"       => $tmp[$tmpcnt++],
                                  "SEMESTER"   => $tmp[$tmpcnt++],
                                  "SCHREGNO"   => $tmp[$tmpcnt++]);

                    //対象項目のみデータに追加する。
                    foreach ($this->fieldSize2 as $key => $val) {
                        $data[$key] = $tmp[$tmpcnt++];
                    }
                    foreach ($this->fieldSize3 as $key => $val) {
                        $data[$key] = $tmp[$tmpcnt++];
                    }
                    foreach ($this->fieldSize as $key => $val) {
                        $data[$key] = $tmp[$tmpcnt++];
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        $temp_arr[$i] = $data;
                        $i++;
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
    public function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $sonzaiErrFlg    = false;    //存在エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $sonzaiErrMsg    = "";       //存在エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $sonzaiSep    = "";

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR", "SEMESTER", "SCHREGNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR", "SEMESTER", "SCHREGNO"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
        }

        if ($this->schoolkind) {
            //存在チェック
            $cnt_check = $db->getOne(knjx_d138jQuery::checkSchoolKind($this, $data));
            if ($cnt_check == 0) {
                $returnFlg = $this->errSet($sonzaiErrFlg, $sonzaiErrMsg, "※存在:", $sonzaiSep, "年度");
            }
        }
        //存在チェック
        $sem_check = $db->getCol(knjx_d138jQuery::checkSemester($data["YEAR"]));
        if (!in_array($data["SEMESTER"], $sem_check)) {
            $returnFlg = $this->errSet($sonzaiErrFlg, $sonzaiErrMsg, "※存在:", $sonzaiSep, "学期");
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$sonzaiErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjx_d138jQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //エラーメッセージをセット
    public function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val)
    {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning(MSG915, "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");
        //ヘッダデータ
        $header = array("GRADE"             => "学年",
                        "HR_CLASS"          => "クラス",
                        "ATTENDNO"          => "出席番号",
                        "NAME"              => "氏名",
                        "YEAR"              => "※年度",
                        "SEMESTER"          => "※学期",
                        "SCHREGNO"          => "※学籍番号");

        //対象項目のみヘッダデータに追加する。
        foreach ($this->fieldSize2 as $key => $val) {
            $header[$key] = $val["MEISYOU"];
        }
        foreach ($this->fieldSize3 as $key => $val) {
            $header[$key] = $val["MEISYOU"];
        }
        foreach ($this->fieldSize as $key => $val) {
            $header[$key] = $val["MEISYOU"];
        }
        $header[$this->lastColumn] = $this->lastColumn;

        $header_rei = array("GRADE"            => "99",
                            "HR_CLASS"         => "999",
                            "ATTENDNO"         => "999",
                            "NAME"             => "アルプ太郎",
                            "YEAR"             => "9999",
                            "SEMESTER"         => "9",
                            "SCHREGNO"         => "12345678");

        //対象項目のみヘッダデータに追加する。
        foreach ($this->fieldSize2 as $key => $val) {
            $header_rei[$key] = "全角".$val["SIZE"]."文字";
        }
        foreach ($this->fieldSize3 as $key => $val) {
            $header_rei[$key] = "全角".$val["SIZE"]."文字";
        }
        foreach ($this->fieldSize as $key => $val) {
            $header_rei[$key] = "全角".$val["SIZE"]."文字";
        }
        $header_rei[$this->lastColumn] = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {
            //エラー
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
                $query = knjx_d138jQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx_d138jQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
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
                $couse = "通知表所見データ";
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
