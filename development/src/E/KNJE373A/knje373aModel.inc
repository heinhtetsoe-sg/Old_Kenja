<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knje373aModel extends Model
{
    public $field      = array();
    public $file;
    public $cmd;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $header;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knje373aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //登録者コード
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
        }

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI"),        //処理名
                             "SEMESTER"         =>  VARS::post("SEMESTER"),
                             "PROFICIENCYDIV"   =>  VARS::post("PROFICIENCYDIV"),
                             "PROFICIENCYCD"    =>  VARS::post("PROFICIENCYCD")
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル

        $this->file = VARS::file("FILE");

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }

        //ヘッダー
        $this->header = array();
        $this->header["YEAR"]                         = "※年度";
        $this->header["SCHREGNO"]                     = "※学籍番号";
        $this->header["GRADE"]                        = "学年";
        $this->header["HR_CLASS"]                     = "組";
        $this->header["ATTENDNO"]                     = "出席番号";
        $this->header["NAME"]                         = "生徒氏名";
        $this->header["NAME_KANA"]                    = "生徒氏名かな";
        $this->header["SEX"]                          = "性別";
        $this->header["FINSCHOOL_NAME"]               = "出身中学校";
        $this->header["TOTAL_POINT1"]                 = "1年次学年末 総点";
        $this->header["TOTAL_POINT_IG_GS1"]           = "1年次学年末 総点（GS除く）";
        $this->header["AVG1"]                         = "1年次学年末 平均点";
        $this->header["GRADE_RANK1"]                  = "1年次学年末 学年序列";
        $this->header["LESSON1"]                      = "1年次 授業日数";
        $this->header["SUSPMOU1"]                     = "1年次 出停忌引日数";
        $this->header["SICKNONO1"]                    = "1年次 欠席日数";
        $this->header["PRESENT1"]                     = "1年次 出席日数";
        $this->header["LATE1"]                        = "1年次 遅刻回数";
        $this->header["KEKKA_JISU1"]                  = "1年次 欠課回数";
        $this->header["ATTENDREC_REMARK1"]            = "1年次 出欠の備考";
        $this->header["TOTAL_POINT2"]                 = "2年次学年末 総点";
        $this->header["AVG2"]                         = "2年次学年末 平均点";
        $this->header["GRADE_RANK2"]                  = "2年次学年末 学年序列";
        $this->header["LESSON2"]                      = "2年次 授業日数";
        $this->header["SUSPMOU2"]                     = "2年次 出停忌引日数";
        $this->header["SICKNONO2"]                    = "2年次 欠席日数";
        $this->header["PRESENT2"]                     = "2年次 出席日数";
        $this->header["LATE2"]                        = "2年次 遅刻回数";
        $this->header["KEKKA_JISU2"]                  = "2年次 欠課回数";
        $this->header["ATTENDREC_REMARK2"]            = "2年次 出欠の備考";
        $this->header["TOTAL_POINT3"]                 = "3年次学年末 総点";
        $this->header["TOTAL_POINT_HISSU3"]           = "3年次学年末 総点（選択科目除く）";
        $this->header["CALC_CREDIT3"]                 = "3年次学年末 総点（30単位換算）";
        $this->header["AVG3"]                         = "3年次学年末 平均点";
        $this->header["GRADE_RANK3"]                  = "3年次学年末 学年序列";
        $this->header["LESSON3"]                      = "3年次 授業日数";
        $this->header["SUSPMOU3"]                     = "3年次 出停忌引日数";
        $this->header["SICKNONO3"]                    = "3年次 欠席日数";
        $this->header["PRESENT3"]                     = "3年次 出席日数";
        $this->header["LATE3"]                        = "3年次 遅刻回数";
        $this->header["KEKKA_JISU3"]                  = "3年次 欠課回数";
        $this->header["ATTENDREC_REMARK3"]            = "3年次 出欠の備考";
        $this->header["OUTER_ENG_TEST"]               = "外部英語テスト判定結果";
        $this->header["ENG_RANK"]                     = "英語力判定試験結果";
        $this->header["SCORE_JUDGE"]                  = "成績判定";
        $this->header["ATTEND_STATUS_JUDGE"]          = "出欠状況判定";
        $this->header["INTERDISCIPLINARY_JUDGE"]      = "学際的活動判定";
        $this->header["CLUB_OTHER_JUDGE"]             = "クラブ・学友会活動判定";
        $this->header["SCHOOL_LIFE_JUDGE"]            = "学校生活態度判定";
        $this->header["GRADE_GROUP_JUDGE"]            = "学年団評価判定";
        $this->header["BIBLE_JUDGE"]                  = "聖書判定";
        $this->header["READING_JUDGE"]                = "読書判定";
        $this->header["CLASS_STF_JUDGE1"]             = "教科担当者判定1";
        $this->header["CLASS_STF_JUDGE2"]             = "教科担当者判定2";
        $this->header["CLASS_STF_JUDGE_TOTAL"]        = "教科担当者判定合計";
        $this->header["JUDGE_TOTAL_POINT"]            = "判定ポイント総合計";
        $this->header["ALL_GRADE_TOTAL_SCORE"]        = "1・2・3年総点計（ポイント含む）";
        $this->header["ALL_GRADE_TOTAL_SCORE_ORDER"]  = "1・2・3年総点序列（ポイント含む）";
        $this->header["HOPE_CD1"]                     = "第1志望コード";
        $this->header["HOPE_CONFIRM_FLG1"]            = "第1志望確定フラグ";
        $this->header["HOPE_CD2"]                     = "第2志望コード";
        $this->header["HOPE_CONFIRM_FLG2"]            = "第2志望確定フラグ";
        $this->header["HOPE_CD3"]                     = "第3志望コード";
        $this->header["HOPE_CONFIRM_FLG3"]            = "第3志望確定フラグ";
        $this->header["DONT_RECOMMENDATION_FLG"]      = "非推薦フラグ";
        $this->header[$this->lastColumn]              = $this->lastColumn;
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
        $data_arr = array();
        //ファイル取り込み
        $data_arr = $this->getFileString();

        if ($data_arr === null) {
            return;
        }
        //データの整合性チェック
        if ($this->isDataMatched($data_arr)) {
            if ($this->field["SHORI_MEI"] == "1") {
                $cnt = knje373aQuery::updateQueryCsv($this, $data_arr);
            } else {
                $cnt = knje373aQuery::deleteQueryCsv($this, $data_arr);
            }
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata                                = array();
            $checkdata["YEAR"]                        = array(4,    "HISSU",  "INT",      "年度");
            $checkdata["SCHREGNO"]                    = array(8,    "HISSU",  "INT2",     "学籍番号");
            $checkdata["HOPE_CD1"]                    = array(4,    "",       "INT2",     "第1志望コード");
            $checkdata["HOPE_CD2"]                    = array(4,    "",       "INT2",     "第2志望コード");
            $checkdata["HOPE_CD3"]                    = array(4,    "",       "INT2",     "第3志望コード");
            $checkdata["HOPE_CONFIRM_FLG1"]           = array(1,    "",       "INT",      "第1志望確定フラグ");
            $checkdata["HOPE_CONFIRM_FLG2"]           = array(1,    "",       "INT",      "第2志望確定フラグ");
            $checkdata["HOPE_CONFIRM_FLG3"]           = array(1,    "",       "INT",      "第3志望確定フラグ");
            $checkdata["DONT_RECOMMENDATION_FLG"]     = array(1,    "",       "INT",      "非推薦フラグ");
            $checkdata["TOTAL_POINT_IG_GS1"]          = array(3,    "",       "INT",      "1年次学年末 総点（GS除く）");
            $checkdata["TOTAL_POINT2"]                = array(3,    "",       "INT",      "2年次学年末 総点");
            $checkdata["COL_36"]                      = array(3,    "",       "INT",      "3年次学年末 総点（30単位換算）");
            $checkdata["JUDGE_TOTAL_POINT"]           = array(3,    "",       "INT",      "判定ポイント総合計");
            $checkdata["BIBLE_JUDGE"]                 = array(2,    "",       "HAN_EN",      "聖書判定");
            $checkdata["READING_JUDGE"]               = array(2,    "",       "HAN_EN",      "読書判定");
            $checkdata["SCORE_JUDGE"]                 = array(4,    "",       "HAN_NUMEN",   "成績判定");
            $checkdata["ATTEND_STATUS_JUDGE"]         = array(4,    "",       "HAN_EN",   "出欠状況判定");
            $checkdata["INTERDISCIPLINARY_JUDGE"]     = array(4,    "",       "HAN_EN",   "学際敵活動判定");
            $checkdata["CLUB_OTHER_JUDGE"]            = array(4,    "",       "HAN_EN",   "クラブ・学友会活動判定");
            $checkdata["SCHOOL_LIFE_JUDGE"]           = array(4,    "",       "HAN_EN",   "学校生活態度判定");
            $checkdata["GRADE_GROUP_JUDGE"]           = array(4,    "",       "HAN_EN",   "学年団評価判定");
            $checkdata["CLASS_STF_JUDGE1"]            = array(2,    "",       "INT",      "教科担当者判定1");
            $checkdata["CLASS_STF_JUDGE2"]            = array(4,    "",       "HAN_EN",   "教科担当者判定2");
            $checkdata["CLASS_STF_JUDGE_TOTAL"]       = array(4,    "",       "HAN_EN",   "教科担当者判定合計");
            $checkdata["ALL_GRADE_TOTAL_SCORE"]       = array(5,    "",       "INT",      "１・２・３年総点計(ポイント含む)");
            $checkdata["ALL_GRADE_TOTAL_SCORE_ORDER"] = array(3,    "",       "INT",      "１・２・３年総点序列(ポイント含む)");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knje373aQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");        //ファイルまたはURLをオープンする
            $idx  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof($fp)) {                                       //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                             //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    $data = array();
                    $dataCnt = 0;
                    foreach ($this->header as $field => $label) {
                        $tmpWk = str_replace("^", ",", $tmp[$dataCnt]);
                        $tmpWk = str_replace("↓", "\r\n", $tmpWk);
                        $data[$field] = $tmpWk;
                        $dataCnt++;
                    }

                    //フォーマット違いなら、終了
                    if (get_count($tmp) < $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        Query::dbCheckIn($db);
                        fclose($fp);
                        return null;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        foreach ($checkdata as $key => $val) {
                            //ゼロ埋めしてセット
                            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                            }
                        }

                        $temp_arr[$idx] = $data;
                        $idx++;
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
        $returnFlg       = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existErrFlg     = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $otherErrFlg     = false;    //その他エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existErrMsg     = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $otherErrMsg     = "";       //その他エラーメッセージ

        $hissuSep     = "";
        $existSep     = "";
        $numericSep   = "";
        $lengthSep    = "";
        $otherSep     = "";

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }
            //チェック用にゼロ埋め
            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
            }
            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }
            //半角英数字
            if ($val[2] == "HAN_NUMEN" && ($data[$key] != "" && !preg_match("/^[a-zA-Z0-9]+$/", $data[$key]))) {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※半角英数字:", $otherSep, $val[3]);
            }
            //半角英字
            if ($val[2] == "HAN_EN" && ($data[$key] != "" && !preg_match("/^[a-zA-Z]+$/", $data[$key]))) {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※半角英字:", $otherSep, $val[3]);
            }
            //文字数チェック
            if ($this->field["SHORI_MEI"] == "1") {
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                }
            }
        }

        //存在チェック（在籍）
        $query = knje373aQuery::checkSchData($this, $data["YEAR"], $data["SCHREGNO"]);
        if (0 == $db->getOne($query)) {
            $returnFlg = $this->errSet($existErrFlg, $existErrMsg, "※存在:", $existSep, "学籍番号");
        }

        //年度チェック ログイン年度とCSVの年度が不一致の場合エラー
        if ($data["YEAR"] != CTRL_YEAR) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "年度不正");
        }

        if ($this->field["SHORI_MEI"] == "1") {
            //第1志望確定フラグ、第2志望確定フラグ、第3志望確定フラグ、非推薦フラグに「1」が複数セットされていないかチェック
            $aryFlg = array($data["HOPE_CONFIRM_FLG1"], $data["HOPE_CONFIRM_FLG2"], $data["HOPE_CONFIRM_FLG3"], $data["DONT_RECOMMENDATION_FLG"]);
            foreach (array_count_values($aryFlg) as $flg => $cnt) {
                if ($flg == "1" && $cnt > 1) {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※重複:", $otherSep, "第1志望確定フラグ、第2志望確定フラグ、第3志望確定フラグ、非推薦フラグ");
                }
            }

            //存在チェック（第1志望コード）
            if ($data["HOPE_CD1"] != "" && !$this->isCollegeDataMatched($db, $data["HOPE_CD1"])) {
                $returnFlg = $this->errSet($existErrFlg, $existErrMsg, "※存在:", $existSep, "第1志望コード");
            }

            //存在チェック（第2志望コード）
            if ($data["HOPE_CD2"] != "" && !$this->isCollegeDataMatched($db, $data["HOPE_CD2"])) {
                $returnFlg = $this->errSet($existErrFlg, $existErrMsg, "※存在:", $existSep, "第2志望コード");
            }

            //存在チェック（第3志望コード）
            if ($data["HOPE_CD3"] != "" && !$this->isCollegeDataMatched($db, $data["HOPE_CD3"])) {
                $returnFlg = $this->errSet($existErrFlg, $existErrMsg, "※存在:", $existSep, "第3志望コード");
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existErrMsg.$numericErrMsg.$lengthErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knje373aQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //存在チェック（志望コード）
    public function isCollegeDataMatched($db, $hopeCd)
    {
        $campusFaccd = substr($hopeCd, 0, 2);
        $campusDeptcd = substr($hopeCd, -2, 2);

        $query = knje373aQuery::getCollegeFacultycdDepartmentcd($campusFaccd, $campusDeptcd);
        $collegeRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($collegeRow)) {
            return true;
        }
        return false;
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
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header_rei = array("YEAR"                         => CTRL_YEAR,
                            "SCHREGNO"                     => "12345678",
                            "GRADE"                        => "99",
                            "HR_CLASS"                     => "999",
                            "ATTENDNO"                     => "999",
                            "NAME"                         => "関学　太郎",
                            "NAME_KANA"                    => "かんがく　たろう",
                            "SEX"                          => "男",
                            "FINSCHOOL_NAME"               => "関西学院中学部",
                            "TOTAL_POINT1"                 => "1893",
                            "TOTAL_POINT_IG_GS1"           => "",
                            "AVG1"                         => "76.6",
                            "GRADE_RANK1"                  => "234",
                            "LESSON1"                      => "202",
                            "SUSPMOU1"                     => "0",
                            "SICKNONO1"                    => "4",
                            "PRESENT1"                     => "198",
                            "LATE1"                        => "3",
                            "KEKKA_JISU1"                  => "2",
                            "ATTENDREC_REMARK1"            => "体調不良4日",
                            "TOTAL_POINT2"                 => "1943",
                            "AVG2"                         => "80.4",
                            "GRADE_RANK2"                  => "187",
                            "LESSON2"                      => "209",
                            "SUSPMOU2"                     => "2",
                            "SICKNONO2"                    => "0",
                            "PRESENT2"                     => "209",
                            "LATE2"                        => "1",
                            "KEKKA_JISU2"                  => "2",
                            "ATTENDREC_REMARK2"            => "親族不幸2日",
                            "TOTAL_POINT3"                 => "1879",
                            "TOTAL_POINT_HISSU3"           => "",
                            "CALC_CREDIT3"                 => "",
                            "AVG3"                         => "75.7",
                            "GRADE_RANK3"                  => "211",
                            "LESSON3"                      => "209",
                            "SUSPMOU3"                     => "0",
                            "SICKNONO3"                    => "0",
                            "PRESENT3"                     => "209",
                            "LATE3"                        => "0",
                            "KEKKA_JISU3"                  => "0",
                            "ATTENDREC_REMARK3"            => "",
                            "OUTER_ENG_TEST"               => "AA",
                            "ENG_RANK"                     => "A",
                            "SCORE_JUDGE"                  => "空欄",
                            "ATTEND_STATUS_JUDGE"          => "空欄",
                            "INTERDISCIPLINARY_JUDGE"      => "空欄",
                            "CLUB_OTHER_JUDGE"             => "空欄",
                            "SCHOOL_LIFE_JUDGE"            => "空欄",
                            "GRADE_GROUP_JUDGE"            => "空欄",
                            "BIBLE_JUDGE"                  => "",
                            "READING_JUDGE"                => "",
                            "CLASS_STF_JUDGE1"             => "",
                            "CLASS_STF_JUDGE2"             => "空欄",
                            "CLASS_STF_JUDGE_TOTAL"        => "空欄",
                            "JUDGE_TOTAL_POINT"            => "",
                            "ALL_GRADE_TOTAL_SCORE"        => "",
                            "ALL_GRADE_TOTAL_SCORE_ORDER"  => "",
                            "HOPE_CD1"                     => "2301",
                            "HOPE_CONFIRM_FLG1"            => "1",
                            "HOPE_CD2"                     => "2500",
                            "HOPE_CONFIRM_FLG2"            => "",
                            "HOPE_CD3"                     => "2600",
                            "HOPE_CONFIRM_FLG3"            => "",
                            "DONT_RECOMMENDATION_FLG"      => "1",
                            $this->lastColumn => $this->lastColumn);

        $contents  = "";

        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $contents .= implode(",", $this->header)."\n";
            $contents .= implode(",", $header_rei)."\n";
            $header_data = true;
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $contents .= implode(",", $header_err)."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode(",", $this->header)."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            $header_data = false;

            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = knje373aQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $semedat = array();
                $kekkadats = array();
                $query = knje373aQuery::getYearGradeCdSet($this);
                $result = $db->query($query);
                while ($ygObj = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $keyStr = $ygObj["YEAR"]."-". $ygObj["GRADE_CD"];
                    $semeDat[$keyStr] = knje373aQuery::getSemesDat($db, $this, $ygObj["YEAR"], $ygObj["GRADE_CD"]);
                    $kekkadats[$keyStr] = knje373aQuery::getSubclsDat($db, $this, $ygObj["YEAR"], $ygObj["GRADE_CD"]);
                }
                $result->free();
                $query = knje373aQuery::getBaseInfo($this);
            }

            //SQL文発行
            $fstFlg = true;
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {                //データ
                    for ($grdIdx = 1; $grdIdx <=3; $grdIdx++) {
                        if ($row["T7".$grdIdx."YEAR"] != "") {
                            $srchKey = $row["T7".$grdIdx."YEAR"]."-0".$grdIdx;
                            $chkArry = $semeDat[$srchKey];
                            $sndKey = "9-".$row["SCHREGNO"];
                            if (get_count($chkArry) > 0 && !is_null($chkArry[$sndKey])) {
                                $record = $chkArry[$sndKey];
                                $row["LESSON".$grdIdx] = $record["LESSON"];
                                $row["SUSPMOU".$grdIdx] = $record["SUSPEND"] + $record["MOURNING"];
                                $row["SICKNONO".$grdIdx] = $record["SICK"];
                                $row["PRESENT".$grdIdx] = $record["PRESENT"];
                                $row["LATE".$grdIdx] = $record["LATE"];
                            }
                            $kekkadat = $kekkadats[$srchKey];
                            if (get_count($kekkadat) > 0 && !is_null($kekkadat[$sndKey])) {
                                $recKekka = null;
                                foreach ($kekkadat[$sndKey] as $rec) {
                                    if ($rec["SICK2"] != null) {
                                        $recKekka += $rec["SICK2"];
                                    }
                                }
                                $row["KEKKA_JISU".$grdIdx] = $recKekka == null ? "" : $recKekka;
                            }
                        }
                    }
                    $row = str_replace(",", "^", $row);      //データ内にカンマがあればハットに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);  //データ内改行は↓に置換
                    $row = str_replace("\n", "↓", $row);    //データ内改行は↓に置換  ※取込時は\r\nになるので注意。
                    $row[$this->lastColumn] = $this->lastColumn;

                    $sep = "";
                    foreach ($this->header as $field => $val) {
                        $contents .= $sep.$row[$field];
                        $sep = ",";
                    }
                    $contents .= "\n";
                } elseif ($this->field["OUTPUT"] == "3") {
                    //エラー
                    $contents .= implode(",", $row) ."\n";
                }
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {        //エラー
                $title = "エラー出力";
            } else {                                //エラー以外
                // $title = "学籍在籍データ";
                $title = "学内推薦先データ";
            }
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
