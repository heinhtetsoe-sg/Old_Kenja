<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx_d183dModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;//コントロールマスタ
    public $staffcd;//登録者コード
    public $setSchoolKind;

    /**
     * メンバ変数の初期化
     */
    public function knjx_d183dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    public function init()
    {
        $this->field = array("OUTPUT"         => VARS::post("OUTPUT"),         //出力取込種別
                             "YEAR"           => VARS::post("YEAR"),           //年度一覧
                             "GRADE_HR_CLASS" => VARS::post("GRADE_HR_CLASS"), //年組一覧
                             "HEADER"         => VARS::post("HEADER"),         //ヘッダ有
                             "SHORI_MEI"      => VARS::post("SHORI_MEI")       //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");
        if (VARS::get("FIELDSIZE")) {
            $this->fieldSize = array();
            $fieldget = VARS::get("FIELDSIZE");
            list($fName, $fSize, $fMeisyou) = preg_split("{=}", $fieldget);
            if ($fSize > 0) {
                $this->fieldSize[$fName]["SIZE"] = $fSize;
                $this->fieldSize[$fName]["MEISYOU"] = $fMeisyou;
            }
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");

            if (VARS::get("SEND_SCHOOLKIND") != "") {
                $this->setSchoolKind = VARS::get("SEND_SCHOOLKIND");
            }
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
                $cnt = knjx_d183dQuery::insertQueryCsv($this, $this->data_arr);
            } else {
                $cnt = knjx_d183dQuery::deleteQueryCsv($this, $this->data_arr);
            }
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_DAT");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("YEAR"          => array(4, "年度"),
                               "SEMESTER"      => array(1, "学期"),
                               "SCHREGNO"      => array(8, "学籍番号"),
                               "REMARK1"       => array(0, "取得資格"),//チェックする文字数は動的に変更する
                               "ZIRITU"        => array(0, "自立活動"),//チェックする文字数は動的に変更する
                               "COMMUNICATION" => array(0, "所見"),    //チェックする文字数は動的に変更する
            );
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjx_d183dQuery::deleteQueryErr();
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
                    $data = array("GRADE"            => $tmp[$tmpcnt++],
                                  "HR_CLASS"         => $tmp[$tmpcnt++],
                                  "ATTENDNO"         => $tmp[$tmpcnt++],
                                  "NAME"             => $tmp[$tmpcnt++],
                                  "YEAR"             => $tmp[$tmpcnt++],
                                  "SEMESTER"         => $tmp[$tmpcnt++],
                                  "SCHREGNO"         => $tmp[$tmpcnt++],
                                  "ATTENDREC_REMARK" => $tmp[$tmpcnt++],
                                  "REMARK1"          => $tmp[$tmpcnt++],
                                  "ZIRITU"           => $tmp[$tmpcnt++],
                                  "COMMUNICATION"    => $tmp[$tmpcnt++],
                                  "DUMMY"            => $tmp[$tmpcnt++],);

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
        $hissuErrFlg      = false;//必須エラーフラグ
        $numericErrFlg    = false;//数字エラーフラグ
        $lengthErrFlg     = false;//レングスエラーフラグ
        $existenceErrFlg  = false;//存在エラーフラグ
        $nyuryokuErrFlg   = false;//入力不可エラーフラグ

        $hissuErrMsg      = "";   //必須エラーメッセージ
        $numericErrMsg    = "";   //数字エラーメッセージ
        $lengthErrMsg     = "";   //レングスエラーメッセージ
        $existenceErrMsg  = "";   //存在エラーメッセージ

        $shutokuShikakuNG = "取得資格・検定は登録できません";
        $zirituNG         = "自立活動は登録できません";

        $hissuSep         = "";
        $existenceSep     = "";
        $numericSep       = "";
        $lengthSep        = "";
        $existenceSep     = "";
        $nyuryokuSep      = "";

        $result = $db->query(knjx_d183dQuery::selectNyuryokuPatternQuery($data));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $Row["REMARK1"]= $row["REMARK1"];
        }
        $result->free();

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
                if (in_array($key, array("YEAR", "SEMESTER", "SCHREGNO"))) {
                    if (strlen(str_replace("\n", "", $data[$key])) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
                //文字数チェック(取得資格、自立活動、所見)
                if ($key == 'SCHREGNO') {
                    $result = $db->query(knjx_d183dQuery::selectHandiCapQuery($data[$key]));
                    while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $Row["HANDICAP"]= $row1["HANDICAP"];
                    }
                    $result->free();
                }
                //通知表Aパターン
                if ($key == 'REMARK1') {
                    if ($Row["HANDICAP"] != '002') {
                        if ($Row["REMARK1"] == '2') {
                            if ($this->validate_row_cnt($data["REMARK1"], 55 * 2) > 4) {
                                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                            }
                        } elseif ($Row["REMARK1"] == '3') {
                            if ($this->validate_row_cnt($data["REMARK1"], 25 * 2) > 4) {
                                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                            }
                        } elseif ($data["REMARK1"] != "") {
                            $returnFlg = $this->errSet($nyuryokuErrFlg, $lengthErrMsg, "※入力不可:", $nyuryokuSep, $shutokuShikakuNG);
                        }
                    } else {
                        //通知表Bパターン
                        if ($Row["REMARK1"] == '2') {
                            if ($this->validate_row_cnt($data["REMARK1"], 35 * 2) > 4) {
                                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                            }
                        } elseif ($Row["REMARK1"] == '3') {
                            if ($this->validate_row_cnt($data["REMARK1"], 15 * 2) > 4) {
                                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                            }
                        } elseif ($data["REMARK1"] != "") {
                            $returnFlg = $this->errSet($nyuryokuErrFlg, $lengthErrMsg, "※入力不可:", $nyuryokuSep, $shutokuShikakuNG);
                        }
                    }
                }
                //自立活動の文字数チェック
                if ($key == 'ZIRITU') {
                    if ($Row["HANDICAP"] != '002') {
                        if ($data["ZIRITU"] != "") {
                            $returnFlg = $this->errSet($nyuryokuErrFlg, $lengthErrMsg, "※入力不可:", $nyuryokuSep, $zirituNG);
                        }
                    } else {
                        if ($this->validate_row_cnt($data["ZIRITU"], 20 * 2) > 4) {
                            $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                        }
                    }
                }
                //所見
                if ($key == 'COMMUNICATION') {
                    if ($this->validate_row_cnt($data["COMMUNICATION"], 25 * 2) > 6) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            }
        }

        //存在チェック
        $regd_cnt = $db->getOne(knjx_d183dQuery::checkRegdDat($this, $data));
        if ($regd_cnt == 0) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $data_each["SCHREGNO"][1]);
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjx_d183dQuery::insertQueryErr($db, $line, $check_error);
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
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
        $header = array("GRADE"                => "学年",
                        "HR_CLASS"             => "クラス",
                        "ATTENDNO"             => "出席番号",
                        "NAME"                 => "氏名",
                        "YEAR"                 => "※年度",
                        "SEMESTER"             => "※学期",
                        "SCHREGNO"             => "※学籍番号",
                        "ATTENDREC_REMARK"     => "精勤フラグ",
                        "REMARK1"              => "取得資格・検定",
                        "ZIRITU"               => "自立活動",
                        "COMMUNICATION"        => "所見",
                        "DUMMY"                => "DUMMY");

        $header_rei = array("GRADE"            => "99",
                            "HR_CLASS"         => "999",
                            "ATTENDNO"         => "999",
                            "NAME"             => "アルプ太郎",
                            "YEAR"             => "9999",
                            "SEMESTER"         => "1",
                            "SCHREGNO"         => "12345678",
                            "ATTENDREC_REMARK" => "1",
                            "REMARK1"          => "全角文字",
                            "ZIRITU"           => "全角20文字×4行",
                            "COMMUNICATION"    => "全角25文字×6行",
                            "DUMMY"            => "DUMMY");

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = knjx_d183dQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knjx_d183dQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {//データ
                    $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
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
            if ($this->field["OUTPUT"] == "3") {//エラー
                $couse = "エラー出力";
            } else {                            //エラー以外
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
