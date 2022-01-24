<?php
class knjl015yModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $ObjYear;

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;
        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //学校種別(データ取込)
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分1(データ取込)
                             "KINDDIV"          =>  VARS::post("KINDDIV"),          //入試種別(データ取込)
                             "EXAM_TYPE"        =>  "1",                            //固定
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            if ($this->field["SHORI_MEI"] == "1") {
                // 更新
                $cnt = knjl015yQuery::updateQueryCsv($this, $this->data_arr);
            } else {
                // 削除
                $cnt = knjl015yQuery::deleteQueryCsv($this, $this->data_arr);
            }

            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");
            Query::dbCheckIn($db);

            $this->setMessage(MSG202, "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    public function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();
            $checkdata = array();

            //チェック用 CSV取込項目
            $checkdata = array();
            $checkdata["ENTEXAMYEAR"]              = array(4,    "HISSU",  "INT",   "年度");
            $checkdata["TESTDIV"]                  = array(500,  "HISSU",  "INT",   "入試区分");
            $checkdata["SHDIV"]                    = array(1,  "HISSU",  "INT",   "専併区分");
            $checkdata["DESIREDIV"]                = array(1,  "HISSU",  "INT",   "志望区分");
            $checkdata["TESTDIV_NAME"]             = array(500,  "HISSU",  "",      "試験名"); //本来は必須項目ではないが、取込時の処理のために必須項目とする。
            $checkdata["EXAMNO"]                   = array(5,    "HISSU",  "INT",   "受験番号");
            $checkdata["NAME_SEI"]                 = array(60,   "HISSU",  "",      "志願者氏名（漢字）"); //NAME_SEIとNAME_MEIは連結したものに対してチェックをかける
            $checkdata["NAME_MEI"]                 = array(60,   "HISSU",  "",      "志願者氏名（漢字）");
            $checkdata["NAME_KANA_SEI"]            = array(120,  "HISSU",  "",      "志願者氏名（カナ）"); //NAME_KANA_SEIとNAME_KANA_MEIは連結したものに対してチェックをかける
            $checkdata["NAME_KANA_MEI"]            = array(120,  "HISSU",  "",      "志願者氏名（カナ）");
            $checkdata["SEX"]                      = array(500,  "HISSU",  "",      "性別");
            $checkdata["BIRTHDAY"]                 = array(10,   "",       "DATE",  "生年月日");
            $checkdata["ZIPCD"]                    = array(8,    "",       "",      "郵便番号");
            $checkdata["PREF_NAME"]                = array(75,   "",       "",      "都道府県"); //PREF_NAMEとCITY_NAMEとBANCHI_NAMEは連結したものに対してチェックをかける
            $checkdata["CITY_NAME"]                = array(75,   "",       "",      "市区町村");
            $checkdata["BANCHI_NAME"]              = array(75,   "",       "",      "町名・番地");
            $checkdata["ADDRESS2"]                 = array(75,   "",       "",      "建物名・部屋番号");
            $checkdata["TELNO"]                    = array(14,   "",       "",      "電話番号");
            $checkdata["FS_CD"]                    = array(7,    "",       "INT",   "出身校コード");
            $checkdata["MIRAI_GRD_DIV"]            = array(500,  "",       "",      "在籍卒業区分");
            $checkdata["GNAME_SEI"]                = array(60,   "",       "",      "保護者氏名(漢字)（姓）");
            $checkdata["GNAME_MEI"]                = array(60,   "",       "",      "保護者氏名(漢字)（名）");
            $checkdata["GKANA_SEI"]                = array(120,  "",       "",      "保護者氏名(カナ)（姓）");
            $checkdata["GKANA_MEI"]                = array(120,  "",       "",      "保護者氏名(カナ)（名）");
            $checkdata["GTELNO"]                   = array(14,   "",       "",      "保護者氏名(カナ)（名）");

            //志望区分
            $desireDiv = array();
            $query = knjl015yQuery::getDesirediv($this->ObjYear, $this->field["APPLICANTDIV"], $this->field["TESTDIV"], "1");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $desireDiv[$row["EXAMCOURSE_NAME"]."コース"] = $row["DESIREDIV"];
            }

            //続柄
            $relationArr = array();
            $query = knjl015yQuery::getNameMst($this->ObjYear, "H201");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $relationArr[$row["NAME1"]] = $row["VALUE"];
            }

            //性別
            $sexArr = array();
            $query = knjl015yQuery::getNameMst($this->ObjYear, "Z002");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sexArr[$row["NAME2"]] = $row["VALUE"];
            }

            //エラーデータの削除
            $query = knjl015yQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;

            while (!feof($fp)) {
                $tmp = fgetcsv_reg($fp, 4096);

                if ($tmp) {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;

                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = implode(",", $tmp);
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $buffer = str_replace("\"", "", $buffer);
                    $buffer = str_replace("↓", "\n", $buffer);
                    $tmp = explode(",", $buffer);

                    //配列の要素数チェック
                    $checkcount = count($tmp);
                    //ホワイトスペースを取り除く
                    for ($t = 0; $t < $checkcount; $t++) {
                        $tmp[$t] = trim($tmp[$t]);
                    }

                    //チェック用 CSV取込項目
                    $cnt = 0;
                    $data = array();
                    $data["ENTEXAMYEAR"]              = $tmp[$cnt++];
                    $data["APPLICANTDIV"]             = $tmp[$cnt++];
                    $data["TEST_DATE"]                = $tmp[$cnt++];
                    $data["TESTDIV_NAME"]             = $tmp[$cnt++];
                    $data["TESTDIV_ABBV"]             = $tmp[$cnt++];
                    $data["EXAMNO"]                   = $tmp[$cnt++];
                    $data["EXAMNO_MANUAL"]            = $tmp[$cnt++];
                    $data["APPNO"]                    = $tmp[$cnt++];
                    $data["LOGIN_USER_SEQ"]           = $tmp[$cnt++];
                    $data["LOGIN_MAIL"]               = $tmp[$cnt++];
                    $data["APP_MAIL"]                 = $tmp[$cnt++];
                    $data["SHIGANSYA_SEQ"]            = $tmp[$cnt++];
                    $data["NAME_SEI"]                 = $tmp[$cnt++];
                    $data["NAME_MEI"]                 = $tmp[$cnt++];
                    $data["NAME_KANA_SEI"]            = $tmp[$cnt++];
                    $data["NAME_KANA_MEI"]            = $tmp[$cnt++];
                    $data["NAME_ROMA_SEI"]            = $tmp[$cnt++];
                    $data["NAME_ROMA_MEI"]            = $tmp[$cnt++];
                    $data["NYUURYOKU_FUKA"]           = $tmp[$cnt++];
                    $data["SEX"]                      = $tmp[$cnt++];
                    $data["BIRTHDAY"]                 = $tmp[$cnt++];
                    $data["ZIPCD"]                    = $tmp[$cnt++];
                    $data["PREF_NAME"]                = $tmp[$cnt++];
                    $data["CITY_NAME"]                = $tmp[$cnt++];
                    $data["BANCHI_NAME"]              = $tmp[$cnt++];
                    $data["ADDRESS2"]                 = $tmp[$cnt++];
                    $data["TELNO"]                    = $tmp[$cnt++];
                    $data["MIRAI_FS_CD"]              = $tmp[$cnt++];
                    $data["MIRAI_FS_NAME"]            = $tmp[$cnt++];
                    $data["MIRAI_FS_NAME_OTHER"]      = $tmp[$cnt++];
                    if ($this->field["APPLICANTDIV"] == "1" || $this->field["APPLICANTDIV"] == "2" && $this->field["TESTDIV"] != "4") {
                        $data["MIRAI_GRD_DIV"]            = $tmp[$cnt++];
                    }
                    if ($this->field["APPLICANTDIV"] == "1" && $this->field["TESTDIV"] != "1") {
                        $data["MIRAI_SH_CD1"]             = $tmp[$cnt++];
                        $data["MIRAI_SH_NAME1"]           = $tmp[$cnt++];
                        $data["MIRAI_SH_NAME_OTHER1"]     = $tmp[$cnt++];
                        $data["MIRAI_SH_CD2"]             = $tmp[$cnt++];
                        $data["MIRAI_SH_NAME2"]           = $tmp[$cnt++];
                        $data["MIRAI_SH_NAME_OTHER2"]     = $tmp[$cnt++];
                    }
                    if ($this->field["APPLICANTDIV"] == "2" && $this->field["TESTDIV"] != "2" && $this->field["TESTDIV"] != "4") {
                        $data["MIRAI_SH_CD1"]             = $tmp[$cnt++];
                        $data["MIRAI_SH_NAME1"]           = $tmp[$cnt++];
                        $data["MIRAI_SH_NAME_OTHER1"]     = $tmp[$cnt++];
                    }
                    $data["MIRAI_PS_CD"]              = $tmp[$cnt++];
                    $data["MIRAI_PS_NAME"]            = $tmp[$cnt++];
                    $data["MIRAI_PS_NAME_OTHER"]      = $tmp[$cnt++];
                    if ($this->field["APPLICANTDIV"] == "2" && $this->field["TESTDIV"] != "4") {
                        if ($this->field["TESTDIV"] == "2") {
                            $data["BLANK1"]                   = $tmp[$cnt++];
                            $data["SUB5_EVAL_HOPE"]           = $tmp[$cnt++];
                        }
                        if ($this->field["TESTDIV"] == "1") {
                            $data["SHIFT_DESIRE_FLG"]         = $tmp[$cnt++];
                        }
                        $data["SPORTS_FLG"]               = $tmp[$cnt++];
                        $data["SPORTS_FLG2"]              = $tmp[$cnt++];
                        if ($this->field["TESTDIV"] != "2") {
                            $data["ENG_PLUS"]                 = $tmp[$cnt++];
                        }
                    }
                    if ($this->field["APPLICANTDIV"] == "1") {
                        $data["HOPE_REASON"]              = $tmp[$cnt++];
                        $data["FUTURE_HOPE"]              = $tmp[$cnt++];
                        $data["HOBBY_SKILL"]              = $tmp[$cnt++];
                        $data["OTHER_REMARK"]             = $tmp[$cnt++];
                        $data["OTHER_REMARK2"]            = $tmp[$cnt++];
                    } else {
                        $data["INPUT1_1"]              = $tmp[$cnt++];
                        $data["INPUT1_2"]              = $tmp[$cnt++];
                        $data["INPUT2_1"]              = $tmp[$cnt++];
                        $data["INPUT2_2"]              = $tmp[$cnt++];
                    }
                    $data["GNAME_SEI"]                = $tmp[$cnt++];
                    $data["GNAME_MEI"]                = $tmp[$cnt++];
                    $data["GKANA_SEI"]                = $tmp[$cnt++];
                    $data["GKANA_MEI"]                = $tmp[$cnt++];
                    $data["GZIPCD"]                   = $tmp[$cnt++];
                    $data["GPREF_NAME"]               = $tmp[$cnt++];
                    $data["GCITY_NAME"]               = $tmp[$cnt++];
                    $data["GBANCHI_NAME"]             = $tmp[$cnt++];
                    $data["GADDRESS2"]                = $tmp[$cnt++];
                    $data["GTELNO"]                   = $tmp[$cnt++];
                    $data["EXPLAIN"]                  = $tmp[$cnt++];
                    if ($this->field["APPLICANTDIV"] == "2") {
                        $data["RELATIONSHIP"]         = $tmp[$cnt++];
                    } else {
                        $data["RELATIONSHIP_REMARK"]    = $tmp[$cnt++];
                        $data["REL_INFO1"]              = $tmp[$cnt++];
                        $data["REL_INFO2"]              = $tmp[$cnt++];
                        $data["REL_INFO3"]              = $tmp[$cnt++];
                        $data["REL_INFO4"]              = $tmp[$cnt++];
                        $data["REL_INFO5"]              = $tmp[$cnt++];
                        $data["REL_INFO6"]              = $tmp[$cnt++];
                    }
                    $data["APP_INFO_DIV"]             = $tmp[$cnt++];
                    $data["SRV_RCPT_DIV"]             = $tmp[$cnt++];
                    $data["SRV_RCPT_MAIL_DATE"]       = $tmp[$cnt++];
                    $data["COUNTER_RCPTNO"]           = $tmp[$cnt++];
                    $data["REMARKS"]                  = $tmp[$cnt++];
                    $data["SETTLE_MONEY"]             = $tmp[$cnt++];
                    $data["EXAM_MONEY"]               = $tmp[$cnt++];
                    $data["FEES"]                     = $tmp[$cnt++];
                    $data["PAY_TYPE_CD"]              = $tmp[$cnt++];
                    $data["PAY_DUE_DATE"]             = $tmp[$cnt++];
                    $data["PAY_DAY"]                  = $tmp[$cnt++];
                    $data["PAY_DAY_MANUAL"]           = $tmp[$cnt++];
                    $data["PAY_INFO_DIV"]             = $tmp[$cnt++];
                    $data["APP_COMP_DAY"]             = $tmp[$cnt++];
                    $data["CANCEL_FLG"]               = $tmp[$cnt++];
                    $data["CANCEL_DATE"]              = $tmp[$cnt++];

                    //続柄
                    if ($data["RELATIONSHIP"] != "") {
                        $data["RELATIONSHIP"] = $relationArr[$data["RELATIONSHIP"]];
                    }

                    //性別
                    if ($data["SEX"] != "") {
                        $data["SEX"] = $sexArr[$data["SEX"]];
                    }

                    //帰国フラグ
                    if (strpos($data["TESTDIV_NAME"], "帰国生") !== false) {
                        $data["INTERVIEW_ATTEND_FLG"] = "1";
                    }

                    //試験名から抽出
                    $data["SHDIV"] = "1";
                    $data["DESIREDIV"] = "1";
                    if (preg_match("/第(１|２|３)回　(.*)（(.*)）/", $data["TESTDIV_NAME"], $matches)) { //中学 --「推薦入試」「一般入試」「適性検査型入試」用のパターン
                        $data["APPLICANTDIV"] = "1";
                        $testDivName = $matches[2];
                        if ($testDivName == "推薦入試") {
                            $data["TESTDIV"]     = "1";
                            $recomItemArray = array("学業" => "1", "英検４級以上" => "2", "スポーツ" => "3", "芸術" => "4");
                            $data["RECOM_ITEM".$recomItemArray[$matches[3]]]  = $recomItemArray[$matches[3]];
                        } else {
                            $data["TESTDIV"]   = ($testDivName == "一般入試") ? sprintf("%d", intval(mb_convert_kana($matches[1], "a")) +  1) : "5";
                            $data["SHDIV"]     = ($matches[3] == "単願") ? "1" : "2";
                        }
                    } elseif (preg_match("/【内部進学】(.*)$/", $data["TESTDIV_NAME"], $matches)) { //高校 --「内部進学」用のパターン
                        $data["APPLICANTDIV"] = "2";
                        $data["TESTDIV"] = "4";
                    } elseif (strpos($data["TESTDIV_NAME"], "学業特別奨学生入試") !== false) { //高校 --「学業特別奨学生入試」用のパターン
                        $data["APPLICANTDIV"] = "2";
                        $data["TESTDIV"] = "1";
                        $pattern = (strpos($data["TESTDIV_NAME"], "帰国") !== false) ? "/【(.*)】(.*（.*）)(.*)/" : "/【(.*)】(.*)　(.*)/";
                        if (preg_match($pattern, $data["TESTDIV_NAME"], $matches)) {
                            $data["SHDIV"] = ($matches[1] == "単願") ? "1" : "2";
                            $data["DESIREDIV"] = $desireDiv[$matches[3]];
                        }
                    } elseif (preg_match("/^【(.*)】(.*)　(.*)/", $data["TESTDIV_NAME"], $matches)) { //高校 --「一般入試」
                        $data["APPLICANTDIV"] = "2";
                        $data["SHDIV"] = ($matches[1] == "単願") ? "1" : "2";
                        $data["TESTDIV"] = "3";
                        $data["DESIREDIV"] = $desireDiv[$matches[3]];
                    } elseif (preg_match("/(.*)【(.*)】　(.*)/", $data["TESTDIV_NAME"], $matches)) { //高校 --「推薦」用のパターン
                        $data["APPLICANTDIV"] = "2";
                        $testDivName = $matches[1];
                        $data["DESIREDIV"] = $desireDiv[$matches[3]];
                        $data["TESTDIV"] = "2";
                        $recomKindArray = array("学力" => "1", "スポーツ" => "2", "文化・芸術等" => "3", "帰国生" => "4", "教会" => "5", "英検" => "6");
                        $data["RECOM_KIND"]  = $recomKindArray[$matches[2]];
                    }

                    //併願校
                    if ($data["APPLICANTDIV"] == "2" && ($data["TESTDIV"] == "1" || $data["TESTDIV"] == "3")) {
                        $data["SH_SCHOOLCD"] = $data["MIRAI_SH_CD1"];
                    }

                    //希望あり
                    if ($data["APPLICANTDIV"] == "2") {
                        $data["SHIFT_DESIRE_FLG"] = (strpos($data["SHIFT_DESIRE_FLG"], "利用する") !== false) ? "1" : "" ;
                        $data["SPORTS_FLG"] = (strpos($data["SPORTS_FLG"], "希望する") !== false) ? "1" : "" ;
                        $data["SPORTS_FLG2"] = (strpos($data["SPORTS_FLG2"], "希望する") !== false) ? "1" : "" ;
                    }

                    //備考1・備考2
                    if ($data["APPLICANTDIV"] == "1") {
                        $sep1 = ($data["HOPE_REASON"] && ($data["HOBBY_SKILL"] || $data["FUTURE_HOPE"])) ? "," : "";
                        $sep2 = ($data["HOBBY_SKILL"] && $data["FUTURE_HOPE"]) ? "," : "";
                        $data["REMARK1"] = $data["HOPE_REASON"].$sep1.$data["FUTURE_HOPE"].$sep2.$data["HOBBY_SKILL"];
                        $data["REMARK2"] = $data["OTHER_REMARK2"];
                    } else {
                        $data["REMARK1"] = $data["INPUT1_2"];
                    }

                    //スライド希望
                    $slideFlg = "";
                    if ($data["APPLICANTDIV"] == "2") {
                        $query = knjl015yQuery::getDesirediv($this->ObjYear, $data["APPLICANTDIV"], $this->field["TESTDIV"], "2", $data["DESIREDIV"]);
                        $desiredivRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        if (is_array($desiredivRow)) {
                            //入試区分が「2：高校」かつ、
                            //志望区分マスタに第２志望があればスライド希望とする
                            $slideFlg = "1";
                        }
                    }
                    $data["SLIDE_FLG"] = $slideFlg;

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    $line++;
                }
            }

            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    /********************/
    /*  エラーチェック  */
    /********************/
    public function errCheck($db, &$data, $checkdata, $line)
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

        foreach ($checkdata as $key => $val) {
            // 更新
            if ($this->field["SHORI_MEI"] == "1") {
                //必須項目チェック
                if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
                }
                //数値チェック
                if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
                }
                //チェック用にゼロ埋め
                if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                    $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                }
                //存在チェック
                $array = array("ENTEXAMYEAR");
                if (in_array($key, $array)) {
                    if ($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                    }
                }
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                }
                
                //日付チェック
                if ($val[2] == "DATE" && $data[$key] != "" && $this->date_rep($data[$key]) == "") {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]);
                } elseif ($val[2] == "DATE2" && $data[$key] != "" && $this->date_rep2($data[$key]) == "") {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]);
                }
                // 削除
            } else {
                //必須項目チェック
                $array = array("ENTEXAMYEAR", "EXAMNO");
                if (in_array($key, $array) && !strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
                }
                //存在チェック
                $array = array("ENTEXAMYEAR", "EXAMNO");
                if (in_array($key, $array)) {
                    if ($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                    }
                    if ($key == "EXAMNO" && 1 > $db->getOne(knjl015yQuery::cntEntexamApplicantbaseDat($this, $data[$key]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                    }
                }
            }
            
            //存在
            if (in_array($key, array("APPLICANTDIV", "TESTDIV")) && $this->field[$key] != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl015yQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //生年月日変換 項目チェック後に行う。
    public function date_rep($data)
    {
        if ($data != "") {
            $data = str_replace("/", "", $data);
            $data = substr($data, 0, 4)."-".substr($data, 4, 2)."-".substr($data, 6, 2);
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }

    //テスト日付変換 項目チェック後に行う。
    public function date_rep2($data)
    {
        if ($data != "") {
            $data = str_replace("/", "-", $data);
            list($year, $month, $day) = explode("-", $data);
            $data = sprintf("%04d-%02d-%02d", $year, $month, $day);
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
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
    public function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (count($data_arr) > 5000) {
            $this->setWarning(MSG915, "処理件数を５千件以下にして下さい 処理件数：".count($data_arr)."件");
            return false;
        }
        return true;
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/

        //中学取込
        $header = array();
        $header["ENTEXAMYEAR"]              = "※年度";
        $header["APPLICANTDIV"]             = "※入試区分";
        $header["TEST_DATE"]                = "試験日";
        $header["TESTDIV_NAME"]             = "試験名";
        $header["TESTDIV_ABBV"]             = "試験名略称";
        $header["EXAMNO"]                   = "受験番号_システム";
        $header["EXAMNO_MANUAL"]            = "受験番号_手動";
        $header["APPNO"]                    = "出願申込番号";
        $header["LOGIN_USER_SEQ"]           = "ログインユーザSEQ";
        $header["LOGIN_MAIL"]               = "ログインメールアドレス";
        $header["APP_MAIL"]                 = "申込時メールアドレス";
        $header["SHIGANSYA_SEQ"]            = "志願者SEQ";
        $header["NAME_SEI"]                 = "※志願者氏名（漢字）（姓）";
        $header["NAME_MEI"]                 = "※志願者氏名（漢字）（名）";
        $header["NAME_KANA_SEI"]            = "志願者氏名（カナ）（姓）";
        $header["NAME_KANA_MEI"]            = "志願者氏名（カナ）（名）";
        $header["NAME_ROMA_SEI"]            = "志願者氏名（ローマ字）（姓）";
        $header["NAME_ROMA_MEI"]            = "志願者氏名（ローマ字）（名）";
        $header["NYUURYOKU_FUKA"]           =  "パソコンに入力できない文字の有無";
        $header["SEX"]                      = "※性別";
        $header["BIRTHDAY"]                 = "※生年月日";
        $header["ZIPCD"]                    = "郵便番号";
        $header["PREF_NAME"]                = "都道府県";
        $header["CITY_NAME"]                = "市区町村";
        $header["BANCHI_NAME"]              = "町名・番地";
        $header["ADDRESS2"]                 = "建物名・部屋番号";
        $header["TELNO"]                    = "電話番号";
        $header["MIRAI_FS_CD"]              = "出身校コード";
        $header["MIRAI_FS_NAME"]            = "出身校名称";
        $header["MIRAI_FS_NAME_OTHER"]      = "出身校名称その他";
        if ($this->field["APPLICANTDIV"] == "1" || $this->field["APPLICANTDIV"] == "2" && $this->field["TESTDIV"] != "4") {
            $header["MIRAI_GRD_DIV"]            = "在籍卒業区分";
        }
        if ($this->field["APPLICANTDIV"] == "1" && $this->field["TESTDIV"] != "1") {
            $header["MIRAI_SH_CD1"]             = "併願校1コード";
            $header["MIRAI_SH_NAME1"]           = "併願校1名称";
            $header["MIRAI_SH_NAME_OTHER1"]     = "併願校1名称その他";
            $header["MIRAI_SH_CD2"]             = "併願校2コード";
            $header["MIRAI_SH_NAME2"]           = "併願校2名称";
            $header["MIRAI_SH_NAME_OTHER2"]     = "併願校2名称その他";
        }
        if ($this->field["APPLICANTDIV"] == "2" && $this->field["TESTDIV"] != "2" && $this->field["TESTDIV"] != "4") {
            $header["MIRAI_SH_CD1"]             = "併願校※併願受験者のみ入力して下さい。コード";
            $header["MIRAI_SH_NAME1"]           = "併願校※併願受験者のみ入力して下さい。名称";
            $header["MIRAI_SH_NAME_OTHER1"]     = "併願校※併願受験者のみ入力して下さい。名称その他";
        }
        $header["MIRAI_PS_CD"]              = "塾コード";
        $header["MIRAI_PS_NAME"]            = "塾名称";
        $header["MIRAI_PS_NAME_OTHER"]      = "塾名称その他";
        if ($this->field["APPLICANTDIV"] == "2" && $this->field["TESTDIV"] != "4") {
            if ($this->field["TESTDIV"] == "2") {
                $header["BLANK1"]               = "";
                $header["SUB5_EVAL_HOPE"]       = "5教科判定希望";
            }
            if ($this->field["TESTDIV"] == "1") {
                $header["SHIFT_DESIRE_FLG"]     = "学業特別奨学生入試　特別判定制度について";
            }
            $header["SPORTS_FLG"]               = "スポーツ/文化・芸術優秀者対象の特別奨学生として採択希望（推薦・学業特別・一般入試の志願者対象）";
            $header["SPORTS_FLG2"]              = "T（タブレット）特奨の採用について";
            if ($this->field["TESTDIV"] != "2") {
                $header["ENG_PLUS"]             = "英検2級以上、TOEIC480点以上取得しており、加点措置を希望しますか";
            }
        }
        if ($this->field["APPLICANTDIV"] == "1") {
            $header["HOPE_REASON"]           = "本校を受験させる理由";
            $header["FUTURE_HOPE"]           = "将来の希望";
            $header["HOBBY_SKILL"]           = "趣味・特技";
            $header["OTHER_REMARK"]          = "備考";
            $header["OTHER_REMARK2"]         = "備考2";
        } else {
            $header["INPUT1_1"]              = "";
            $header["INPUT1_2"]              = "入力欄";
            $header["INPUT2_1"]              = "";
            $header["INPUT2_2"]              = "入力欄";
        }
        $header["GNAME_SEI"]                = "保護者氏名(漢字)（姓）";
        $header["GNAME_MEI"]                = "保護者氏名(漢字)（名）";
        $header["GKANA_SEI"]                = "保護者氏名(カナ)（姓）";
        $header["GKANA_MEI"]                = "保護者氏名(カナ)（名）";
        $header["GZIPCD"]                   = "郵便番号";
        $header["GPREF_NAME"]               = "都道府県";
        $header["GCITY_NAME"]               = "市区町村";
        $header["GBANCHI_NAME"]             = "町名・番地";
        $header["GADDRESS2"]                = "建物名・部屋番号";
        $header["GTELNO"]                   = "緊急連絡先";
        $header["EXPLAIN"]                  = "上記緊急連絡先の名称(例：父の携帯番号)";
        if ($this->field["APPLICANTDIV"] == "2") {
            $header["RELATIONSHIP"]         = "志願者との続柄";
        } else {
            $header["RELATIONSHIP_REMARK"]    = "家族について";
            $header["REL_INFO1"]              = "家族１";
            $header["REL_INFO2"]              = "家族２";
            $header["REL_INFO3"]              = "家族３";
            $header["REL_INFO4"]              = "家族４";
            $header["REL_INFO5"]              = "家族５";
            $header["REL_INFO6"]              = "家族６";
        }
        $header["APP_INFO_DIV"]             = "出願情報状態区分";
        $header["SRV_RCPT_DIV"]             = "調査書受領区分";
        $header["SRV_RCPT_MAIL_DATE"]       = "調査書受領メール送信日";
        $header["COUNTER_RCPTNO"]           = "窓口受付番号";
        $header["REMARKS"]                  = "備考欄";
        $header["SETTLE_MONEY"]             = "決済金額";
        $header["EXAM_MONEY"]               = "受験料";
        $header["FEES"]                     = "事務手数料";
        $header["PAY_TYPE_CD"]              = "支払種別コード";
        $header["PAY_DUE_DATE"]             = "支払期限日";
        $header["PAY_DAY"]                  = "入金日";
        $header["PAY_DAY_MANUAL"]           = "入金日_手動";
        $header["PAY_INFO_DIV"]             = "入金情報区分";
        $header["APP_COMP_DAY"]             = "申込完了日";
        $header["CANCEL_FLG"]               = "キャンセルフラグ";
        $header["CANCEL_DATE"]              = "キャンセル日";
        
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "2") {
            //エラー
            $contents .= implode($header_err, ",")."\n";
        }


        /********************/
        /*  データ部分出力  */
        /********************/
        $db = Query::dbCheckOut();

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "3") {
            //エラー
            $query = knjl015yQuery::selectCsvErrQuery();

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //エラー
                $setArray = $header_err;

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($setArray as $field => $label) {
                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";

                $header_data = true;
            }
            $result->free();
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "2") {      //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "志願者データ";
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

function fgetcsv_reg(&$handle, $length = null, $d = ',', $e = '"')
{
    $eof = false;
    $d = preg_quote($d);
    $e = preg_quote($e);
    $_line = "";
    while (($eof != true)and(!feof($handle))) {
        $_line .= (empty($length) ? fgets($handle) : fgets($handle, $length));
        $itemcnt = preg_match_all('/'.$e.'/', $_line, $dummy);
        if ($itemcnt % 2 == 0) {
            $eof = true;
        }
    }
    $_csv_line = preg_replace('/(?:\\r\\n|[\\r\\n])?$/', $d, trim($_line));
    $_csv_pattern = '/('.$e.'[^'.$e.']*(?:'.$e.$e.'[^'.$e.']*)*'.$e.'|[^'.$d.']*)'.$d.'/';
    preg_match_all($_csv_pattern, $_csv_line, $_csv_matches);
    $_csv_data = $_csv_matches[1];
    for ($_csv_i=0;$_csv_i<count($_csv_data);$_csv_i++) {
        $_csv_data[$_csv_i]=preg_replace('/^'.$e.'(.*)'.$e.'$/s', '$1', $_csv_data[$_csv_i]);
        $_csv_data[$_csv_i]=str_replace($e.$e, $e, $_csv_data[$_csv_i]);
    }
    return empty($_line) ? false : $_csv_data;
}
