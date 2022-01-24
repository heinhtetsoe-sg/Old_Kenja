<?php

require_once('for_php7.php');
class knjl016pModel extends Model
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
            $cnt = knjl016pQuery::updateQueryCsv($this, $this->data_arr);

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
            $checkdata = array();
            $db = Query::dbCheckOut();

            $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "入試年度");
//          $checkdata["APPLICANTDIV"]          = array(1,    "HISSU",  "INT",   "入試制度");//入試区分でチェックするので不用
            $checkdata["TESTDIV"]               = array(1,    "HISSU",  "INT",   "入試区分");
            $checkdata["EXAMNO"]                = array(6,    "HISSU",  "INT",   "受験番号");
            $checkdata["NAME_SEI"]              = array(60,   "HISSU",  "",      "志願者氏名（漢字姓）");
            $checkdata["NAME_MEI"]              = array(60,   "HISSU",  "",      "志願者氏名（漢字名）");
            $checkdata["NAME_KANA_SEI"]         = array(120,  "HISSU",  "",      "志願者氏名（カナ姓）");
            $checkdata["NAME_KANA_MEI"]         = array(120,  "HISSU",  "",      "志願者氏名（カナ名）");
            $checkdata["SEX"]                   = array(1,    "HISSU",  "",      "性別");
            $checkdata["BIRTHDAY"]              = array(8,    "",       "DATE",  "生年月日");
            $checkdata["ZIPCD"]                 = array(7,    "",       "INT",   "郵便番号");
            $checkdata["CITY_NAME"]             = array(90,   "",       "",      "市区町村");
            $checkdata["BANCHI_NAME"]           = array(90,   "",       "",      "町名・番地");
            $checkdata["ADDRESS2"]              = array(90,   "",       "",      "建物・部屋番号");
            $checkdata["TELNO"]                 = array(14,   "",       "",      "電話番号");
            $checkdata["FS_CD"]                 = array(7,    "",       "INT2",  "出身学校コード");
            //中学・県外
            if ($this->field["APPLICANTDIV"] == "1" && $this->field["TESTDIV"] == "1") {
                $checkdata["PRISCHOOLCD"]           = array(7,    "",       "INT2",  "塾コード");
            //中学・前期
            } elseif ($this->field["APPLICANTDIV"] == "1" && $this->field["TESTDIV"] == "2") {
                $checkdata["REMARK1"]               = array(135,  "",       "",      "志願理由");
            //中学・後期
            } elseif ($this->field["APPLICANTDIV"] == "1" && $this->field["TESTDIV"] == "3") {
                $checkdata["REMARK1"]               = array(135,  "",       "",      "志願理由");
            //高校・一般
            } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["TESTDIV"] == "2") {
                $checkdata["REMARK1"]               = array(135,  "",       "",      "志願理由");
                $checkdata["DORMITORY_FLG"]         = array(1,    "",       "",      "通学方法");
            }
            $checkdata["SIMAI_SEI"]             = array(90,   "",       "",      "本校在籍又は卒業の保護者・兄弟・姉妹（漢字姓）");
            $checkdata["SIMAI_MEI"]             = array(90,   "",       "",      "本校在籍又は卒業の保護者・兄弟・姉妹（漢字名）");
            $checkdata["SIMAI_RELATIONSHIP"]    = array(90,   "",       "",      "志願者との関係");
            $checkdata["GNAME_SEI"]             = array(60,   "",       "",      "保護者氏名（漢字姓）");
            $checkdata["GNAME_MEI"]             = array(60,   "",       "",      "保護者氏名（漢字名）");
            $checkdata["GKANA_SEI"]             = array(120,  "",       "",      "保護者氏名（カナ姓）");
            $checkdata["GKANA_MEI"]             = array(120,  "",       "",      "保護者氏名（カナ名）");
            $checkdata["GTELNO"]                = array(14,   "",       "",      "緊急連絡先");
            $checkdata["RELATIONSHIP"]          = array(2,    "",       "",      "本人と保護者の続柄");

            //チェック用
            $checkdata["NAME"]                  = array(60,   "",       "",      "志願者氏名（漢字姓名）");
            $checkdata["NAME_KANA"]             = array(120,  "",       "",      "志願者氏名（カナ姓名）");
            $checkdata["ADDRESS1"]              = array(90,   "",       "",      "志願者住所（市区町村、町名・番地）");
            $checkdata["GNAME"]                 = array(60,   "",       "",      "保護者氏名（漢字姓名）");
            $checkdata["GKANA"]                 = array(120,  "",       "",      "保護者氏名（カナ姓名）");
            $checkdata["SIMAI_NAME"]            = array(90,   "",       "",      "本校在籍又は卒業の保護者・兄弟・姉妹（漢字姓名）");

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl016pQuery::deleteQueryErr();
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

                    //チェック用 CSV取込項目
                    $data["ENTEXAMYEAR"]            = $tmp[0];
                    $data["APPLICANTDIV"]           = $tmp[1];
                    $data["TESTDIV"]                = $tmp[1];
                    $data["EXAMNO"]                 = $tmp[5];
                    $data["NAME_SEI"]               = $tmp[12];
                    $data["NAME_MEI"]               = $tmp[13];
                    $data["NAME_KANA_SEI"]          = $tmp[14];
                    $data["NAME_KANA_MEI"]          = $tmp[15];
                    $data["SEX"]                    = $tmp[17];
                    $data["BIRTHDAY"]               = $tmp[18];
                    $data["ZIPCD"]                  = $tmp[19];
                    $data["CITY_NAME"]              = $tmp[21];
                    $data["BANCHI_NAME"]            = $tmp[22];
                    $data["ADDRESS2"]               = $tmp[23];
                    $data["TELNO"]                  = $tmp[24];
                    $data["FS_CD"]                  = $tmp[25];
                    //中学・県外
                    if ($this->field["APPLICANTDIV"] == "1" && $this->field["TESTDIV"] == "1") {
                        $data["PRISCHOOLCD"]            = $tmp[28];
                    //中学・前期
                    } elseif ($this->field["APPLICANTDIV"] == "1" && $this->field["TESTDIV"] == "2") {
                        $data["REMARK1"]                = $tmp[34];
                    //中学・後期
                    } elseif ($this->field["APPLICANTDIV"] == "1" && $this->field["TESTDIV"] == "3") {
                        $data["REMARK1"]                = $tmp[34];
                    //高校・一般
                    } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["TESTDIV"] == "2") {
                        $data["REMARK1"]                = $tmp[33];
                        $data["DORMITORY_FLG"]          = $tmp[34];
                    }
                    //中学・県外
                    if ($this->field["APPLICANTDIV"] == "1" && $this->field["TESTDIV"] == "1") {
                        $data["SIMAI_SEI"]              = $tmp[31];
                        $data["SIMAI_MEI"]              = $tmp[32];
                        $data["SIMAI_RELATIONSHIP"]     = $tmp[35];
                        $data["GNAME_SEI"]              = $tmp[36];
                        $data["GNAME_MEI"]              = $tmp[37];
                        $data["GKANA_SEI"]              = $tmp[38];
                        $data["GKANA_MEI"]              = $tmp[39];
                        $data["GTELNO"]                 = $tmp[40];
                        $data["RELATIONSHIP"]           = $tmp[42];
                    //中学・前期
                    //中学・後期
                    //高校・一般
                    } else {
                        $data["SIMAI_SEI"]              = $tmp[28];
                        $data["SIMAI_MEI"]              = $tmp[29];
                        $data["SIMAI_RELATIONSHIP"]     = $tmp[32];
                        $data["GNAME_SEI"]              = $tmp[35];
                        $data["GNAME_MEI"]              = $tmp[36];
                        $data["GKANA_SEI"]              = $tmp[37];
                        $data["GKANA_MEI"]              = $tmp[38];
                        $data["GTELNO"]                 = $tmp[39];
                        $data["RELATIONSHIP"]           = $tmp[41];
                    }

                    //チェック用
                    $data["NAME"]                   = $data["NAME_SEI"]."　".$data["NAME_MEI"];
                    $data["NAME_KANA"]              = $data["NAME_KANA_SEI"]."　".$data["NAME_KANA_MEI"];
                    $data["ADDRESS1"]               = $data["CITY_NAME"].$data["BANCHI_NAME"];
                    $data["GNAME"]                  = $data["GNAME_SEI"]."　".$data["GNAME_MEI"];
                    $data["GKANA"]                  = $data["GKANA_SEI"]."　".$data["GKANA_MEI"];
                    $data["SIMAI_NAME"]             = $data["SIMAI_SEI"].$data["SIMAI_MEI"].$data["SIMAI_RELATIONSHIP"];

                    //賢者コードに変換
                    $data["APPLICANTDIV"]           = $this->getRepData("APPLICANTDIV", $data["APPLICANTDIV"]);
                    $data["TESTDIV"]                = $this->getRepData("TESTDIV", $data["TESTDIV"]);
                    $data["SEX"]                    = $this->getRepData("SEX", $data["SEX"]);
                    $data["RELATIONSHIP"]           = $this->getRepData("RELATIONSHIP", $data["RELATIONSHIP"]);
                    //高校・一般
                    if ($this->field["APPLICANTDIV"] == "2" && $this->field["TESTDIV"] == "2") {
                        $data["DORMITORY_FLG"]          = $this->getRepData("DORMITORY_FLG", $data["DORMITORY_FLG"]);
                    }

                    //中学・県外
                    if ($this->field["APPLICANTDIV"] == "1" && $this->field["TESTDIV"] == "1") {
                        $dataCnt = 60;
                    //中学・前期
                    //中学・後期
                    //高校・一般
                    } else {
                        $dataCnt = 59;
                    }

                    //入試区分によるファイルチェック
                    if ($this->field["APPLICANTDIV"] != $data["APPLICANTDIV"] || $this->field["TESTDIV"] != $data["TESTDIV"]) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。（入試区分）";
                        return;
                    }
                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        foreach ($checkdata as $key => $val) {
                            //郵便番号を変換してセット
                            if ($key == "ZIPCD" && $data[$key] != "") {
                                $data[$key] = substr($data[$key], 0, 3)."-".substr($data[$key], 3);
                            }
                            //日付を変換してセット
                            if ($val[2] == "DATE") {
                                $data[$key] = $this->dateRep($data[$key]);
                            }
                            //ゼロ埋めしてセット
                            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                            }
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

        //受験番号範囲チェック用
        $checkNumber = array();
        $result = $db->query(knjl016pQuery::getEntexamNoDat($this->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $checkNumber[] = $row;
        }
        $result->free();

        foreach ($checkdata as $key => $val) {
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
            if ($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'FS_CD' && strlen($data[$key]) && 0 == $db->getOne(knjl016pQuery::fsCdCntSql($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'PRISCHOOLCD' && strlen($data[$key]) && 0 == $db->getOne(knjl016pQuery::prischoolCnt($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //日付チェック
            if ($val[2] == "DATE" && $data[$key] != "" && $this->dateRep($data[$key]) == "") {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]);
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }
        }

        //受験番号範囲チェック
        $checkNumber_cnt = 0;
        if ($data["EXAMNO"]) {
            foreach ($checkNumber as $key2 => $val2) {
                if ($val2["EXAMNO_FROM"] <= $data["EXAMNO"] && $data["EXAMNO"] <= $val2["EXAMNO_TO"] && $data["APPLICANTDIV"] == $val2["APPLICANTDIV"] && $data["TESTDIV"] == $val2["TESTDIV"]) {
                    $checkNumber_cnt++;
                }
            }
        }
        if ($checkNumber_cnt == 0) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "受験番号範囲不可");
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl016pQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //日付変換 項目チェック後に行う。
    public function dateRep($data)
    {
        if ($data != "") {
            //文字列の中に"/","-"が含まれていない場合
            if (strpos(str_replace("/", "-", $data), "-") === false) {
                $data = substr($data, 0, 4)."-".substr($data, 4, 2)."-".substr($data, 6, 2);
            }
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }

    /**********************/
    /*  賢者コードに変換  */
    /**********************/
    public function getRepData($div, $str)
    {
        //配列(変換用)
        //入試制度、入試区分
        $repData = array();
        $repData["APPLICANTDIV"]["10"] = "1";//中学
        $repData["APPLICANTDIV"]["50"] = "1";//中学
        $repData["APPLICANTDIV"]["60"] = "1";//中学
        $repData["APPLICANTDIV"]["20"] = "2";//高校
        $repData["TESTDIV"]["10"] = "1";//県外
        $repData["TESTDIV"]["50"] = "2";//前期
        $repData["TESTDIV"]["60"] = "3";//後期
        $repData["TESTDIV"]["20"] = "2";//一般
        //性別、通学方法、続柄
        $repData["SEX"]["男"] = "1";
        $repData["SEX"]["女"] = "2";
        $repData["DORMITORY_FLG"]["自宅"] = "1";
        $repData["DORMITORY_FLG"]["下宿"] = "2";
        $repData["DORMITORY_FLG"]["入寮"] = "3";
        $repData["RELATIONSHIP"]["父"] = "01";
        $repData["RELATIONSHIP"]["母"] = "02";
        $repData["RELATIONSHIP"]["その他"] = "11";
        $rtnVal = "";
        foreach ($repData[$div] as $key => $val) {
            $word = (string) $key;
            //文字列の中に含まれている場合
            if (strpos($str, $word) !== false) {
                $rtnVal = $val;
            }
        }
        //文字列の中に含まれていない場合
        return $rtnVal;
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
    public function getHeadDownloadModel()
    {
        $header = array();

        $header[] = "年度";
        $header[] = "入試区分";
        $header[] = "試験日";
        $header[] = "試験名";
        $header[] = "試験名略称";
        $header[] = "受験番号_システム";
        $header[] = "受験番号_手動";
        $header[] = "出願申込番号";
        $header[] = "ログインユーザSEQ";
        $header[] = "ログインメールアドレス";
        $header[] = "申込時メールアドレス";
        $header[] = "志願者SEQ";
        $header[] = "志願者氏名(漢字)（姓）";
        $header[] = "志願者氏名(漢字)（名）";
        $header[] = "志願者氏名(カナ)（姓）";
        $header[] = "志願者氏名(カナ)（名）";
        $header[] = "正しく登録できない氏名等漢字の有無";
        $header[] = "性別";
        $header[] = "生年月日";
        $header[] = "郵便番号";
        $header[] = "都道府県";
        $header[] = "市区町村";
        $header[] = "町名・番地";
        $header[] = "建物名・部屋番号";
        $header[] = "電話番号";
        $header[] = "出身校コード";
        $header[] = "出身校名称";
        $header[] = "出身校名称その他";

        //中学・県外
        if ($this->field["APPLICANTDIV"] == "1" && $this->field["TESTDIV"] == "1") {
            $header[] = "塾コード";
            $header[] = "塾名称";
            $header[] = "塾名称その他";
            $header[] = "本校在籍又は卒業の保護者・兄弟・姉妹（漢字）（姓）";
            $header[] = "本校在籍又は卒業の保護者・兄弟・姉妹（漢字）（名）";
            $header[] = "本校在籍又は卒業の保護者・兄弟・姉妹（カナ）（姓）";
            $header[] = "本校在籍又は卒業の保護者・兄弟・姉妹（カナ）（名）";
            $header[] = "志願者との関係";
            $header[] = "保護者氏名(漢字)（姓）";
            $header[] = "保護者氏名(漢字)（名）";
            $header[] = "保護者氏名(カナ)（姓）";
            $header[] = "保護者氏名(カナ)（名）";
            $header[] = "緊急連絡先";
            $header[] = "緊急連絡先名称（例：父携帯）";
            $header[] = "保護者と本人の続柄";
            $header[] = "保護者と本人の続柄（その他）";
            $header[] = "出願情報状態区分";
            $header[] = "調査書受領区分";
            $header[] = "調査書受領メール送信日";
            $header[] = "窓口受付番号";
            $header[] = "備考欄";
            $header[] = "決済金額";
            $header[] = "受験料";
            $header[] = "事務手数料";
            $header[] = "支払種別コード";
            $header[] = "支払期限日";
            $header[] = "入金日";
            $header[] = "入金日_手動";
            $header[] = "入金情報区分";
            $header[] = "申込完了日";
            $header[] = "キャンセルフラグ";
            $header[] = "キャンセル日";
        //中学・前期・後期
        } elseif ($this->field["APPLICANTDIV"] == "1" && ($this->field["TESTDIV"] == "2" || $this->field["TESTDIV"] == "3")) {
            $header[] = "本校在籍又は卒業の保護者・兄弟・姉妹（漢字）（姓）";
            $header[] = "本校在籍又は卒業の保護者・兄弟・姉妹（漢字）（名）";
            $header[] = "本校在籍又は卒業の保護者・兄弟・姉妹（カナ）（姓）";
            $header[] = "本校在籍又は卒業の保護者・兄弟・姉妹（カナ）（名）";
            $header[] = "志願者との関係";
            $header[] = "塾・教室名（記入例：○○塾○○教室）";
            $header[] = "志願理由（45字以内で記入）";
            $header[] = "保護者氏名(漢字)（姓）";
            $header[] = "保護者氏名(漢字)（名）";
            $header[] = "保護者氏名(カナ)（姓）";
            $header[] = "保護者氏名(カナ)（名）";
            $header[] = "緊急連絡先";
            $header[] = "緊急連絡先名称（例：父携帯）";
            $header[] = "保護者と本人の続柄";
            $header[] = "保護者と本人の続柄（その他）";
            $header[] = "出願情報状態区分";
            $header[] = "調査書受領区分";
            $header[] = "調査書受領メール送信日";
            $header[] = "窓口受付番号";
            $header[] = "備考欄";
            $header[] = "決済金額";
            $header[] = "受験料";
            $header[] = "事務手数料";
            $header[] = "支払種別コード";
            $header[] = "支払期限日";
            $header[] = "入金日";
            $header[] = "入金日_手動";
            $header[] = "入金情報区分";
            $header[] = "申込完了日";
            $header[] = "キャンセルフラグ";
            $header[] = "キャンセル日";
        //高校
        } else {
            $header[] = "本校在籍又は卒業の保護者・兄弟・姉妹（漢字）（姓）";
            $header[] = "本校在籍又は卒業の保護者・兄弟・姉妹（漢字）（名）";
            $header[] = "本校在籍又は卒業の保護者・兄弟・姉妹（カナ）（姓）";
            $header[] = "本校在籍又は卒業の保護者・兄弟・姉妹（カナ）（名）";
            $header[] = "志願者との関係";
            $header[] = "本校を志願する理由（45字以内）";
            $header[] = "通学方法";
            $header[] = "保護者氏名(漢字)（姓）";
            $header[] = "保護者氏名(漢字)（名）";
            $header[] = "保護者氏名(カナ)（姓）";
            $header[] = "保護者氏名(カナ)（名）";
            $header[] = "緊急連絡先";
            $header[] = "緊急連絡先名称（例：父携帯）";
            $header[] = "保護者と本人との続柄";
            $header[] = "保護者と本人との続柄（その他）";
            $header[] = "出願情報状態区分";
            $header[] = "調査書受領区分";
            $header[] = "調査書受領メール送信日";
            $header[] = "窓口受付番号";
            $header[] = "備考欄";
            $header[] = "決済金額";
            $header[] = "受験料";
            $header[] = "事務手数料";
            $header[] = "支払種別コード";
            $header[] = "支払期限日";
            $header[] = "入金日";
            $header[] = "入金日_手動";
            $header[] = "入金情報区分";
            $header[] = "申込完了日";
            $header[] = "キャンセルフラグ";
            $header[] = "キャンセル日";
        }


        $header[] = $this->lastColumn;

        //クリア
        $contents  = "";
        //ヘッダ出力
        $contents .= implode($header, ",")."\n";

        //ファイル名
        $title = $this->ObjYear.'年度 Webヘッダデータ';

        //ＣＳＶ出力
        $filename = sprintf("%s.csv", $title);
        common::downloadContents($contents, $filename, "SJIS-win");
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

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        // エラー出力
        $contents .= implode($header_err, ",")."\n";

        /********************/
        /*  データ部分出力  */
        /********************/
        //エラー
        $query = knjl016pQuery::selectCsvErrQuery();
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header_err as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $header_data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            //エラー出力
            $filename = sprintf("%s.csv", "エラー出力");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
