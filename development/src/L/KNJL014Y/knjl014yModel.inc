<?php

require_once('for_php7.php');
class knjl014yModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $ObjYear;
    public $lastColumn;//CSVの最後の列の値

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }

        /**************/
        /*  ヘッダー  */
        /**************/
        $this->header = array();
        $this->header["ENTEXAMYEAR"]                = "※入試年度";
        $this->header["APPLICANTDIV"]               = "※入試制度";
        $this->header["APPLICANTDIVNAME"]           = "入試制度名";
        $this->header["TESTDIV"]                    = "※入試区分";
        $this->header["TESTDIVNAME"]                = "入試区分名";
        if ($this->field["APPLICANTDIV"] == '2') {
            $this->header["INTERVIEW_ATTEND_FLG"]       = "帰国生";
            $this->header["DESIREDIV"]                  = "※志望区分コード";
            $this->header["DESIREDIVNAME"]              = "志望区分";
            $this->header["RECOM_KIND"]                 = "推薦区分コード";
            $this->header["RECOM_KINDNAME"]             = "推薦区分";
            $this->header["EXAMNO"]                     = "※受験番号";
            $this->header["SHDIV"]                      = "※専併区分コード";
            $this->header["SHDIVNAME"]                  = "専併区分名称";
            $this->header["SH_SCHOOLCD"]                = "併願校コード";
            $this->header["SH_SCHOOLNAME"]              = "併願校名";
        } else {
            $this->header["RECOM_ITEM1"]                = "推薦事項１";
            $this->header["RECOM_ITEM2"]                = "推薦事項２";
            $this->header["RECOM_ITEM3"]                = "推薦事項３";
            $this->header["RECOM_ITEM4"]                = "推薦事項４";
            $this->header["EXAMNO"]                     = "※受験番号";
        }
        $this->header["NAME"]                       = "※氏名";
        $this->header["NAME_KANA"]                  = "※氏名かな";
        $this->header["SEX"]                        = "※性別区分";
        $this->header["SEXNAME"]                    = "性別区分";
        $this->header["BIRTHDAY"]                   = "生年月日";
        $this->header["FS_CD"]                      = "出身学校コード";
        $this->header["FS_NAME"]                    = "出身学校名";
        $this->header["FS_GRDYEAR"]                 = "卒業年";
        $this->header["FS_M"]                       = "卒業月";
        $this->header["FS_GRDDIV"]                  = "卒見込コード";
        $this->header["FS_GRDDIVNAME"]              = "卒見込";
        $this->header["TELNO"]                      = "電話番号";
        $this->header["ZIPCD"]                      = "郵便番号";
        $this->header["ADDRESS1"]                   = "住所";
        $this->header["ADDRESS2"]                   = "方書";
        $this->header["GNAME"]                      = "保護者名";
        $this->header["GKANA"]                      = "保護者氏名かな";
        $this->header["RELATIONSHIP"]               = "保護者・続柄コード";
        $this->header["RELATIONSHIPNAME"]           = "保護者・続柄";
        $this->header["GTELNO"]                     = "保護者・電話番号";
        $this->header["GZIPCD"]                     = "保護者・郵便番号";
        $this->header["GADDRESS1"]                  = "保護者・住所";
        $this->header["GADDRESS2"]                  = "保護者・方書";
        if ($this->field["APPLICANTDIV"] == '2') {
            $this->header["SHIFT_DESIRE_FLG"]           = "特判希";
            $this->header["SLIDE_FLG"]                  = "スラ希";
            $this->header["GENERAL_FLG"]                = "一般希";
            $this->header["GENERAL_DESIREDIV"]          = "一般希・志望区分";
            $this->header["GENERAL_DESIREDIVNAME"]      = "一般希・志望区分名称";
            $this->header["GENERAL_SHDIV"]              = "一般希・専併区分";
            $this->header["GENERAL_SHDIVNAME"]          = "一般希・専併区分名称";
            $this->header["SPORTS_FLG"]                 = "ス優希";
        }
        $this->header["DORMITORY_FLG"]              = "入寮希";
        $this->header["REMARK1"]                    = "備考１";
        $this->header["REMARK2"]                    = "備考２";
        if ($this->field["APPLICANTDIV"] == '2') {
            $this->header["SPORTS_FLG2"]                = "T特奨";
        }
        $this->header[$this->lastColumn]                 = $this->lastColumn;
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $cnt = knjl014yQuery::updateQueryCsv($this, $this->data_arr);

            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();

            //チェック対象項目
            $checkdata = array();
            $checkdata["ENTEXAMYEAR"]           = array(4,       "HISSU",   "INT",   "DISPLAY");
            $checkdata["APPLICANTDIV"]          = array(1,       "HISSU",   "INT",   "DISPLAY");
            $checkdata["TESTDIV"]               = array(1,       "HISSU",   "INT",   "DISPLAY");
            $checkdata["INTERVIEW_ATTEND_FLG"]  = array(1,       "",        "INT",   "FLG");
            $checkdata["DESIREDIV"]             = array(1,       "HISSU",   "INT",   "DESIRE");
            $checkdata["RECOM_KIND"]            = array(1,       "",        "INT",   "L023");
            $checkdata["EXAMNO"]                = array(5,       "HISSU",   "INT2",  "");
            $checkdata["SHDIV"]                 = array(1,       "HISSU",   "INT",   "L006");
            $checkdata["SH_SCHOOL"]             = array(7,       "",        "INT2",  "FINS");
            $checkdata["RECOM_ITEM1"]           = array(1,       "",        "INT",   "FLG");
            $checkdata["RECOM_ITEM2"]           = array(1,       "",        "INT",   "FLG");
            $checkdata["RECOM_ITEM3"]           = array(1,       "",        "INT",   "FLG");
            $checkdata["RECOM_ITEM4"]           = array(1,       "",        "INT",   "FLG");
            $checkdata["NAME"]                  = array(60,      "HISSU",   "",      "");
            $checkdata["NAME_KANA"]             = array(120,     "HISSU",   "",      "");
            $checkdata["SEX"]                   = array(1,       "HISSU",   "INT",   "Z002");
            $checkdata["BIRTHDAY"]              = array(10,      "",        "DATE",  "");
            $checkdata["FS_CD"]                 = array(7,       "",        "INT2",  "FINS");
            $checkdata["FS_GRDYEAR"]            = array(4,       "",        "INT",   "");
            $checkdata["FS_M"]                  = array(2,       "",        "INT2",  "");
            $checkdata["FS_GRDDIV"]             = array(1,       "",        "INT",   "L016");
            $checkdata["TELNO"]                 = array(14,      "",        "",      "");
            $checkdata["ZIPCD"]                 = array(8,       "",        "",      "");
            $checkdata["ADDRESS1"]              = array(75,      "",        "",      "");
            $checkdata["ADDRESS2"]              = array(75,      "",        "",      "");
            $checkdata["GNAME"]                 = array(60,      "",        "",      "");
            $checkdata["GKANA"]                 = array(120,     "",        "",      "");
            $checkdata["RELATIONSHIP"]          = array(2,       "",        "INT2",  "H201");
            $checkdata["GTELNO"]                = array(14,      "",        "",      "");
            $checkdata["GZIPCD"]                = array(8,       "",        "",      "");
            $checkdata["GADDRESS1"]             = array(75,      "",        "",      "");
            $checkdata["GADDRESS2"]             = array(75,      "",        "",      "");
            $checkdata["SHIFT_DESIRE_FLG"]      = array(1,       "",        "INT",   "FLG");
            $checkdata["SLIDE_FLG"]             = array(1,       "",        "INT",   "FLG");
            $checkdata["GENERAL_FLG"]           = array(1,       "",        "INT",   "FLG");
            $checkdata["GENERAL_DESIREDIV"]     = array(1,       "",        "INT",   "DESIRE");
            $checkdata["GENERAL_SHDIV"]         = array(1,       "",        "INT",   "L006");
            $checkdata["SPORTS_FLG"]            = array(1,       "",        "INT",   "FLG");
            $checkdata["DORMITORY_FLG"]         = array(1,       "",        "INT",   "FLG");
            $checkdata["REMARK1"]               = array("20*4",  "",        "CRLF",  "");
            $checkdata["REMARK2"]               = array("20*2",  "",        "CRLF",  "");
            $checkdata["SPORTS_FLG2"]           = array(1,       "",        "INT",   "FLG");

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl014yQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
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
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    $data = array();
                    $dataCnt = 0;
                    foreach ($this->header as $field => $label) {
                        $data[$field] = $tmp[$dataCnt];
                        $dataCnt++;
                    }

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        foreach ($this->header as $field => $label) {
                            if (in_array($field, array_keys($checkdata))) {
                                list($size, $hissu, $div, $exist) = $checkdata[$field];
                                //日付を変換してセット
                                if ($div == "DATE") {
                                    $data[$field] = $this->dateRep($data[$field]);
                                }
                                //ゼロ埋めしてセット
                                if ($div == "INT2" && $data[$field] != "" && is_numeric($data[$field])) {
                                    $data[$field] = sprintf("%0{$size}d", $data[$field]);
                                }
                                //改行コードに変換してセット
                                if ($div == "CRLF" && $data[$field] != "") {
                                    $data[$field] = str_replace("↓", "\n", $data[$field]);
                                }
                            }
                        }
                        //日付としてセット
                        if (strlen($data["FS_GRDYEAR"]) && strlen($data["FS_M"])) {
                            $data["FS_DAY"] = $data["FS_GRDYEAR"].'-'.$data["FS_M"].'-01';
                        }
                        //固定でセット
                        if ($data["APPLICANTDIV"] == '1') {
                            $data["SHDIV"]      = '1';
                            $data["DESIREDIV"]  = '1';
                        }

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

    /********************/
    /*  エラーチェック  */
    /********************/
    public function errCheck($db, $data, $checkdata, $line)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $otherErrFlg        = false;    //その他エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ

        $hissuSep           = "";
        $existenceSep       = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $otherSep           = "";

        foreach ($this->header as $field => $label) {
            $label = str_replace("※", "", $label);

            if (in_array($field, array_keys($checkdata))) {
                list($size, $hissu, $div, $exist) = $checkdata[$field];

                //必須項目チェック
                if ($hissu == "HISSU" && !strlen(trim($data[$field]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $label);
                }

                //数値チェック
                if (($div == "INT" || $div == "INT2") && $data[$field] != "" && !is_numeric($data[$field])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $label);
                }

                //チェック用にゼロ埋め
                if ($div == "INT2" && $data[$field] != "" && is_numeric($data[$field])) {
                    $data[$field] = sprintf("%0{$size}d", $data[$field]);
                }

                //存在チェック
                if ($exist == "DISPLAY") {
                    if (($field == "ENTEXAMYEAR" && $this->ObjYear != $data[$field]) || ($field != "ENTEXAMYEAR" && $this->field[$field] != $data[$field])) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $label);
                    }
                } elseif ($exist == "FLG") {
                    if (strlen($data[$field]) && '1' != $data[$field]) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $label);
                    }
                } elseif ($exist == "FINS") {
                    if (strlen($data[$field]) && 0 == $db->getOne(knjl014yQuery::fsCdCntSql($data[$field]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $label);
                    }
                } elseif ($exist == "DESIRE") {
                    if (strlen($data[$field]) && 0 == $db->getOne(knjl014yQuery::desiredivCntSql($data, $data[$field]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $label);
                    }
                } elseif (strlen($exist)) {
                    if (strlen($data[$field]) && 0 == $db->getOne(knjl014yQuery::nameCntSql($exist, $data[$field]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $label);
                    }
                }

                //日付チェック
                if ($div == "DATE" && $data[$field] != "" && $this->dateRep($data[$field]) == "") {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $label);
                }

                if ($div == "CRLF") {
                    list($moji, $gyo) = explode('*', $size);
                    //チェック用に改行コード変換
                    if ($data[$field] != "") {
                        $data[$field] = str_replace("↓", "\n", $data[$field]);
                    }

                    //文字数チェック
                    if (strlen($data[$field]) > (($moji * $gyo * 3) + ($gyo - 1))) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $label);

                    //行数チェック
                    } elseif ($this->validate_row_cnt($data[$field], ($moji * 2)) > $gyo) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $label.'(行数)');
                    }
                } else {
                    //文字数チェック
                    if (strlen($data[$field]) > $size) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $label);
                    }
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl014yQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //日付変換 項目チェック後に行う。
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

    /******************************/
    /*  エラーメッセージをセット  */
    /******************************/
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

    /****************************/
    /*  データの整合性チェック  */
    /****************************/
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /****************/
        /*  ヘッダー例  */
        /****************/
        //志望区分
        $desirediv = implode(' ', $db->getCol(knjl014yQuery::getHeaderReiDesireDiv($this, $this->field["TESTDIV"])));
        //推薦区分
        $recom_kind = implode(' ', $db->getCol(knjl014yQuery::getHeaderReiNameMst($this->ObjYear, "L023", "NAMECD2 || ':' || NAME1")));
        //専併区分 / 一般希・専併区分
        $shdiv = implode(' ', $db->getCol(knjl014yQuery::getHeaderReiNameMst($this->ObjYear, "L006", "NAMECD2 || ':' || NAME1")));
        //性別
        $sex = implode(' ', $db->getCol(knjl014yQuery::getHeaderReiNameMst($this->ObjYear, "Z002", "NAMECD2 || ':' || NAME1")));
        //卒見込
        $fs_grddiv = implode(' ', $db->getCol(knjl014yQuery::getHeaderReiNameMst($this->ObjYear, "L016", "NAMECD2 || ':' || NAME1")));
        //一般希・志望区分
        $g_desirediv = implode(' ', $db->getCol(knjl014yQuery::getHeaderReiDesireDiv($this, '3')));

        $yOldE = ($this->field["APPLICANTDIV"] == "1") ? "12" : "15";   //入学年令

        $header_rei = array();
        $header_rei["ENTEXAMYEAR"]              = $this->ObjYear;
        $header_rei["APPLICANTDIV"]             = $this->field["APPLICANTDIV"];
        $header_rei["APPLICANTDIVNAME"]         = '入力の必要なし';
        $header_rei["TESTDIV"]                  = $this->field["TESTDIV"];
        $header_rei["TESTDIVNAME"]              = '入力の必要なし';
        $header_rei["INTERVIEW_ATTEND_FLG"]     = '1';
        $header_rei["DESIREDIV"]                = $desirediv;
        $header_rei["DESIREDIVNAME"]            = '入力の必要なし';     //表示のみ
        $header_rei["RECOM_KIND"]               = $recom_kind;
        $header_rei["RECOM_KINDNAME"]           = '入力の必要なし';     //表示のみ
        $header_rei["EXAMNO"]                   = '12345';
        $header_rei["SHDIV"]                    = $shdiv;
        $header_rei["SHDIVNAME"]                = '入力の必要なし';     //表示のみ
        $header_rei["SH_SCHOOLCD"]              = '1234567';
        $header_rei["SH_SCHOOLNAME"]            = '入力の必要なし';     //表示のみ
        $header_rei["RECOM_ITEM1"]              = '1';
        $header_rei["RECOM_ITEM2"]              = '1';
        $header_rei["RECOM_ITEM3"]              = '1';
        $header_rei["RECOM_ITEM4"]              = '1';
        $header_rei["NAME"]                     = 'アルプ　太郎';
        $header_rei["NAME_KANA"]                = 'あるぷ　たろう';
        $header_rei["SEX"]                      = $sex;
        $header_rei["SEXNAME"]                  = '入力の必要なし';     //表示のみ
        $header_rei["BIRTHDAY"]                 = date('Y/m/d', strtotime(CTRL_DATE." -{$yOldE} years"));
        $header_rei["FS_CD"]                    = '1234567';
        $header_rei["FS_NAME"]                  = '入力の必要なし';     //表示のみ
        $header_rei["FS_GRDYEAR"]               = $this->ObjYear;
        $header_rei["FS_M"]                     = '03';
        $header_rei["FS_GRDDIV"]                = $fs_grddiv;
        $header_rei["FS_GRDDIVNAME"]            = '入力の必要なし';     //表示のみ
        $header_rei["TELNO"]                    = "12-3456-7890";
        $header_rei["ZIPCD"]                    = "123-4567";
        $header_rei["ADDRESS1"]                 = "全角25文字";
        $header_rei["ADDRESS2"]                 = "全角25文字";
        $header_rei["GNAME"]                    = "アルプ　保護者";
        $header_rei["GKANA"]                    = "あるぷ　ほごしゃ";
        $header_rei["RELATIONSHIP"]             = "12";
        $header_rei["RELATIONSHIPNAME"]         = '入力の必要なし';     //表示のみ
        $header_rei["GTELNO"]                   = "123-456-7890";
        $header_rei["GZIPCD"]                   = "123-4567";
        $header_rei["GADDRESS1"]                = "全角25文字";
        $header_rei["GADDRESS2"]                = "全角25文字";
        $header_rei["SHIFT_DESIRE_FLG"]         = '1';
        $header_rei["SLIDE_FLG"]                = '1';
        $header_rei["GENERAL_FLG"]              = '1';
        $header_rei["GENERAL_DESIREDIV"]        = $g_desirediv;
        $header_rei["GENERAL_DESIREDIVNAME"]    = '入力の必要なし';     //表示のみ
        $header_rei["GENERAL_SHDIV"]            = $shdiv;
        $header_rei["GENERAL_SHDIVNAME"]        = '入力の必要なし';     //表示のみ
        $header_rei["SPORTS_FLG"]               = '1';
        $header_rei["DORMITORY_FLG"]            = '1';
        $header_rei["REMARK1"]                  = "全角20文字X4行";
        $header_rei["REMARK2"]                  = "全角40文字X2行";
        $header_rei["SPORTS_FLG2"]              = '1';
        $header_rei[$this->lastColumn]          = $this->lastColumn;

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            // ヘッダ出力
            $header_data = true;
            $contents .= implode($this->header, ',')."\n";
            $sep = '';
            foreach ($this->header as $field => $label) {
                $contents .= $sep.$header_rei[$field];
                $sep = ',';
            }
            $contents .= "\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            // エラー出力
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            // データ出力
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($this->header, ",")."\n";
            }
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        if ($this->field["OUTPUT"] != "1") {
            $query = "";
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = knjl014yQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"] == "4") {
                $query = knjl014yQuery::selectMainQuery($this);
            }

            //日付変換項目一覧
            $date_array = array("BIRTHDAY");
            //改行コード変換項目一覧
            $textarea_array = array("REMARK1", "REMARK2");

            //SQL文発行
            if ($query) {
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["OUTPUT"] == "4") {     //データ
                        //日付変換
                        foreach ($date_array as $key) {
                            $row[$key] = str_replace("-", "/", $row[$key]);
                        }
                        //改行コード変換
                        foreach ($textarea_array as $key) {
                            $row[$key] = str_replace("\r\n", "↓", $row[$key]);
                            $row[$key] = str_replace("\r", "↓", $row[$key]);
                            $row[$key] = str_replace("\n", "↓", $row[$key]);
                        }
                        //カンマ変換
                        $row = str_replace("\r\n", "", str_replace(",", ".", $row));
                        $array = $this->header;
                    } else {
                        $array = $header_err;
                    }

                    //ヘッダ順に値をセット
                    $sep = "";
                    foreach ($array as $field => $label) {
                        $contents .= $sep.$row[$field];
                        $sep = ",";
                    }
                    $contents .= "\n";

                    $header_data = true;
                }
                $result->free();
            }
        }

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //入試制度名称
        $AppName = $db->getOne(knjl014yQuery::getNameMst($this->ObjYear, "L003", $this->field["APPLICANTDIV"]));
        //入試区分名称
        $namecd1 = ($this->field["APPLICANTDIV"] == "2") ? "L004" : "L024";
        $TestdivName = $db->getOne(knjl014yQuery::getNameMst($this->ObjYear, $namecd1, $this->field["TESTDIV"]));
        //ファイル名
        $title = $this->ObjYear.'年度 '.$AppName.' '.$TestdivName.'志願者データ';

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            //エラー出力
            if ($this->field["OUTPUT"] == "3") {
                $filename = sprintf("%s.csv", "エラー出力");
            //エラー出力以外
            } else {
                $filename = sprintf("%s.csv", $title);
            }
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
