<?php

require_once('for_php7.php');
class knjl014pModel extends Model
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
                             "DATADIV"          =>  VARS::post("DATADIV"),          //データ種類
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
            //願書
            if ($this->field["DATADIV"] == "1") {
                $cnt = knjl014pQuery::updateQueryCsv($this, $this->data_arr);

            //高校・調査書
            } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "2") {
                $cnt = knjl014pQuery::updateQueryCsv2($this, $this->data_arr);
            }

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

            //願書
            if ($this->field["DATADIV"] == "1") {
                $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "入試年度");
                $checkdata["APPLICANTDIV"]          = array(1,    "HISSU",  "INT",   "入試制度");
                $checkdata["TESTDIV"]               = array(1,    "HISSU",  "INT",   "入試区分");
                $checkdata["DATADIV"]               = array(1,    "HISSU",  "INT",   "データ種類");
                $checkdata["EXAMNO"]                = array(6,    "HISSU",  "INT2",  "受験番号");
                $checkdata["SIKEN_KAIJOU_CD1"]      = array(4,    "HISSU",  "INT2",  "試験会場");
                $checkdata["RECEPTDATE"]            = array(10,   "HISSU",  "DATE",  "受付日付");
                $checkdata["NAME"]                  = array(60,   "HISSU",  "",      "氏名");
                $checkdata["NAME_KANA"]             = array(120,  "HISSU",  "",      "ふりがな");
                $checkdata["SEX"]                   = array(1,    "HISSU",  "INT",   "性別");
                $checkdata["BIRTHDAY"]              = array(10,   "",       "DATE",  "生年月日");
                $checkdata["REMARK2"]               = array(2,    "",       "INT",   "年齢");
                $checkdata["ZIPCD"]                 = array(8,    "",       "",      "郵便番号");
                $checkdata["ADDRESS1"]              = array(90,   "",       "",      "住所");
                $checkdata["ADDRESS2"]              = array(90,   "",       "",      "方書");
                $checkdata["TELNO"]                 = array(14,   "",       "",      "電話番号");
                $checkdata["FS_CD"]                 = array(7,    "",       "INT2",  "出身学校コード");
                $checkdata["FS_DAY"]                = array(10,   "",       "DATE",  "卒業年月日");
                $checkdata["FS_GRDDIV"]             = array(1,    "",       "INT",   "既/卒");
                $checkdata["PRISCHOOLCD"]           = array(7,    "",       "INT2",  "塾コード");
                $checkdata["PRISCHOOL_CLASS_CD"]    = array(7,    "",       "INT2",  "教室コード");
                $checkdata["GNAME"]                 = array(60,   "",       "",      "保護者氏名");
                $checkdata["GKANA"]                 = array(120,  "",       "",      "保護者ふりがな");
                $checkdata["GZIPCD"]                = array(8,    "",       "",      "保護者郵便番号");
                $checkdata["GADDRESS1"]             = array(90,   "",       "",      "保護者住所");
                $checkdata["GADDRESS2"]             = array(90,   "",       "",      "保護者方書");
                $checkdata["GTELNO"]                = array(14,   "",       "",      "保護者電話番号");
                $checkdata["GTELNO2"]               = array(14,   "",       "",      "保護者電話番号（携帯）");
                $checkdata["RELATIONSHIP"]          = array(2,    "",       "INT2",  "保護者続柄");
                $checkdata["DORMITORY_FLG"]         = array(1,    "",       "INT",   "通学方法");
                $checkdata["SIMAI_NAME"]            = array(90,   "",       "",      "本校在籍又は卒業の保護者・兄弟・姉妹");
                $checkdata["REMARK1"]               = array(135,  "",       "",      "志願理由");

            //高校・調査書
            } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "2") {
                //教科
                $class_array = array();
                $result = $db->query(knjl014pQuery::getNameMstAll($this->ObjYear, "L008"));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $class_array[$row["NAMECD2"]] = $row["ABBV1"];
                }

                $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "入試年度");
                $checkdata["APPLICANTDIV"]          = array(1,    "HISSU",  "INT",   "入試制度");
                $checkdata["TESTDIV"]               = array(1,    "HISSU",  "INT",   "入試区分");
                $checkdata["DATADIV"]               = array(1,    "HISSU",  "INT",   "データ種類");
                $checkdata["EXAMNO"]                = array(6,    "HISSU",  "INT2",  "受験番号");
                for ($grade = 1; $grade <= 3; $grade++) {
                    $gradename = $grade.'年';
                    foreach ($class_array as $classcd => $label) {
                        $checkdata["CONFRPT".$classcd."_".$grade]   = array(2,    "",       "INT",   $gradename.$label);
                    }
                }
                $checkdata["CLASS_CNT"]             = array(3,    "",       "INT",  "クラス人員");
                $checkdata["RANK"]                  = array(3,    "",       "INT",  "順位");
                for ($grade = 1; $grade <= 3; $grade++) {
                    $gradename = $grade.'年';
                    $checkdata["ATTEND_PRESENT".$grade]     = array(3,    "",       "INT",   $gradename."出席すべき日数");
                    $checkdata["ATTEND_KESSEKI".$grade]     = array(3,    "",       "INT",   $gradename."欠席日数");
                    $checkdata["ATTEND_LATE".$grade]        = array(3,    "",       "INT",   $gradename."遅刻回数");
                    $checkdata["ATTEND_EARLY".$grade]       = array(3,    "",       "INT",   $gradename."早退回数");
                    $checkdata["ABSENCE_REMARK".$grade]     = array(30,   "",       "",      $gradename."欠席、遅刻などの理由");
                }
                $checkdata["REMARK1"]               = array(240,   "",      "",     "備考");
                for ($grade = 1; $grade <= 3; $grade++) {
                    $gradename = $grade.'年';
                    $checkdata["SPECIALACT".$grade]         = array(150,  "",       "",      $gradename."特別活動の記録");
                }
                for ($grade = 1; $grade <= 3; $grade++) {
                    $gradename = $grade.'年';
                    $checkdata["TOTALSTUDY".$grade]         = array(150,  "",       "",      $gradename."総合的な学習の時間の記録");
                }
                $checkdata["HEALTH"]                = array(150,   "",      "",     "健康状況");
                $checkdata["TOTAKREMARK"]           = array(150,   "",      "",     "総合所見");
                $checkdata["OTHERACT"]              = array(150,   "",      "",     "その他の活動の記録");
            }

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl014pQuery::deleteQueryErr();
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

                    //願書
                    if ($this->field["DATADIV"] == "1") {
                        $data["ENTEXAMYEAR"]            = $tmp[0];
                        $data["APPLICANTDIV"]           = $tmp[1];
                        $data["TESTDIV"]                = $tmp[2];
                        $data["DATADIV"]                = $tmp[3];
                        $data["EXAMNO"]                 = $tmp[4];
                        $data["SIKEN_KAIJOU_CD1"]       = $tmp[5];
                        $data["RECEPTDATE"]             = $tmp[7];
                        $data["NAME"]                   = $tmp[8];
                        $data["NAME_KANA"]              = $tmp[9];
                        $data["SEX"]                    = $tmp[10];
                        $data["BIRTHDAY"]               = $tmp[11];
                        $data["REMARK2"]                = $tmp[12];
                        $data["ZIPCD"]                  = $tmp[13];
                        $data["ADDRESS1"]               = $tmp[14];
                        $data["ADDRESS2"]               = $tmp[15];
                        $data["TELNO"]                  = $tmp[16];
                        $data["FS_CD"]                  = $tmp[17];
                        $data["FS_DAY"]                 = $tmp[19];
                        $data["FS_GRDDIV"]              = $tmp[20];
                        $data["PRISCHOOLCD"]            = $tmp[21];
                        $data["PRISCHOOL_CLASS_CD"]     = $tmp[23];
                        $data["GNAME"]                  = $tmp[25];
                        $data["GKANA"]                  = $tmp[26];
                        $data["GZIPCD"]                 = $tmp[27];
                        $data["GADDRESS1"]              = $tmp[28];
                        $data["GADDRESS2"]              = $tmp[29];
                        $data["GTELNO"]                 = $tmp[30];
                        $data["GTELNO2"]                = $tmp[31];
                        $data["RELATIONSHIP"]           = $tmp[32];
                        $data["DORMITORY_FLG"]          = $tmp[33];
                        $data["SIMAI_NAME"]             = $tmp[34];
                        $data["REMARK1"]                = $tmp[35];

                        $dataCnt = 37;

                    //高校・調査書
                    } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "2") {
                        $data["ENTEXAMYEAR"]            = $tmp[0];
                        $data["APPLICANTDIV"]           = $tmp[1];
                        $data["TESTDIV"]                = $tmp[2];
                        $data["DATADIV"]                = $tmp[3];
                        $data["EXAMNO"]                 = $tmp[4];

                        $tmpCnt = 4;
                        for ($grade = 1; $grade <= 3; $grade++) {
                            foreach ($class_array as $classcd => $label) {
                                $data["CONFRPT".$classcd."_".$grade]    = $tmp[++$tmpCnt];
                            }
                        }
                        $data["CLASS_CNT"]              = $tmp[++$tmpCnt];
                        $data["RANK"]                   = $tmp[++$tmpCnt];
                        for ($grade = 1; $grade <= 3; $grade++) {
                            $data["ATTEND_PRESENT".$grade]  = $tmp[++$tmpCnt];
                            $data["ATTEND_KESSEKI".$grade]  = $tmp[++$tmpCnt];
                            $data["ATTEND_LATE".$grade]     = $tmp[++$tmpCnt];
                            $data["ATTEND_EARLY".$grade]    = $tmp[++$tmpCnt];
                            $data["ATTEND_REMARK".$grade]   = $tmp[++$tmpCnt];
                        }
                        $data["REMARK1"]                = $tmp[++$tmpCnt];
                        for ($grade = 1; $grade <= 3; $grade++) {
                            $data["SPECIALACT".$grade]      = $tmp[++$tmpCnt];
                        }
                        for ($grade = 1; $grade <= 3; $grade++) {
                            $data["TOTALSTUDY".$grade]      = $tmp[++$tmpCnt];
                        }
                        $data["HEALTH"]                 = $tmp[++$tmpCnt];
                        $data["TOTAKREMARK"]            = $tmp[++$tmpCnt];
                        $data["OTHERACT"]               = $tmp[++$tmpCnt];
                        $data[$this->lastColumn]        = $tmp[++$tmpCnt];
                    }

                    //項目数によるファイルチェック
                    if (($this->field["DATADIV"] == "1" && $checkcount != $dataCnt) || ($this->field["DATADIV"] != "1" && $checkcount !=get_count($data))) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        foreach ($checkdata as $key => $val) {
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
        if ($this->field["DATADIV"] == "1") {
            $checkNumber = array();
            $result = $db->query(knjl014pQuery::getEntexamNoDat($this->ObjYear, $this->field["APPLICANTDIV"]));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $checkNumber[] = $row;
            }
            $result->free();
        }

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
            //数値チェック
            if ($val[2] == "FLT" && $data[$key] != "") {
                $err = false;
                $array = explode('.', $data[$key]);
                for ($i = 0; $i < get_count($array); $i++) {
                    if (!is_numeric($array[$i])) {
                        $err = true;
                    }
                    if (strlen($array[$i]) > 1) {
                        $err = true;
                    }
                }
                if ($err) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
                }
            }

            //存在チェック
            $array = array("ENTEXAMYEAR","APPLICANTDIV","TESTDIV","DATADIV");
            if (in_array($key, $array)) {
                if (($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) || ($key != "ENTEXAMYEAR" && $this->field[$key] != $data[$key])) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
            if ($key == 'SIKEN_KAIJOU_CD1' && strlen($data[$key]) && 0 == $db->getOne(knjl014pQuery::hallCntSql($data["ENTEXAMYEAR"], $data["APPLICANTDIV"], $data["TESTDIV"], $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'SEX' && strlen($data[$key]) && 0 == $db->getOne(knjl014pQuery::nameCntSql("Z002", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if (in_array($key, array('FS_CD', 'SH_SCHOOLCD1', 'SH_SCHOOLCD2', 'SH_SCHOOLCD3', 'SH_SCHOOLCD4')) && strlen($data[$key]) && 0 == $db->getOne(knjl014pQuery::fsCdCntSql($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'FS_GRDDIV' && strlen($data[$key]) && 0 == $db->getOne(knjl014pQuery::nameCntSql("L016", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'PRISCHOOLCD' && strlen($data[$key]) && 0 == $db->getOne(knjl014pQuery::prischoolCnt($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'PRISCHOOL_CLASS_CD' && strlen($data[$key]) && 0 == $db->getOne(knjl014pQuery::prischoolClassCnt($data["PRISCHOOLCD"], $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'RELATIONSHIP' && strlen($data[$key]) && 0 == $db->getOne(knjl014pQuery::nameCntSql("H201", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'DORMITORY_FLG' && strlen($data[$key]) && 0 == $db->getOne(knjl014pQuery::nameCntSql("L047", $data[$key]))) {
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
        if ($this->field["DATADIV"] == "1") {
            if ($data["EXAMNO"]) {
                foreach ($checkNumber as $key2 => $val2) {
                    if ($val2["EXAMNO_FROM"] <= $data["EXAMNO"] && $data["EXAMNO"] <= $val2["EXAMNO_TO"] && $this->field["TESTDIV"] == $val2["TESTDIV"]) {
                        $checkNumber_cnt++;
                    }
                }
            }
            if ($checkNumber_cnt == 0) {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "受験番号範囲不可");
            }
        }

        if ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] != "1") {
            $base_cnt = $db->getOne(knjl014pQuery::cntEntexamApplicantbaseDat($this, $data));
            //存在チェック -- ENTEXAM_APPLICANTBASE_DAT
            if ($base_cnt == 0) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, "願書データ");
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl014pQuery::insertQueryErr($db, $line, $check_error);
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

        /************************/
        /*  願書（中学・高校）  */
        /************************/
        if ($this->field["DATADIV"] == "1") {
            $header = $header_rei = array();

            /**************/
            /*  ヘッダー  */
            /**************/
            $header["ENTEXAMYEAR"]          = "※入試年度";
            $header["APPLICANTDIV"]         = "※入試制度";
            $header["TESTDIV"]              = "※入試区分";
            $header["DATADIV"]              = "※データ種類";
            $header["EXAMNO"]               = "※受験番号";
            $header["SIKEN_KAIJOU_CD1"]     = "※試験会場";
            $header["EXAMHALL_NAME"]        = "試験会場名";
            $header["RECEPTDATE"]           = "※受付日付";
            $header["NAME"]                 = "※氏名";
            $header["NAME_KANA"]            = "※ふりがな";
            $header["SEX"]                  = "※性別";
            $header["BIRTHDAY"]             = "生年月日";
            $header["REMARK2"]              = "年齢";
            $header["ZIPCD"]                = "郵便番号";
            $header["ADDRESS1"]             = "住所";
            $header["ADDRESS2"]             = "方書";
            $header["TELNO"]                = "電話番号";
            $header["FS_CD"]                = "出身学校コード";
            $header["FS_NAME"]              = "出身学校名";
            $header["FS_DAY"]               = "卒業年月日";
            $header["FS_GRDDIV"]            = "既/卒";
            $header["PRISCHOOLCD"]          = "塾コード";
            $header["PRISCHOOL_NAME"]       = "塾名";
            $header["PRISCHOOL_CLASS_CD"]   = "教室コード";
            $header["PRISCHOOL_CLASS_NAME"] = "教室名";
            $header["GNAME"]                = "保護者氏名";
            $header["GKANA"]                = "保護者ふりがな";
            $header["GZIPCD"]               = "保護者郵便番号";
            $header["GADDRESS1"]            = "保護者住所";
            $header["GADDRESS2"]            = "保護者方書";
            $header["GTELNO"]               = "保護者電話番号";
            $header["GTELNO2"]              = "保護者電話番号（携帯）";
            $header["RELATIONSHIP"]         = "保護者続柄";
            $header["DORMITORY_FLG"]        = "通学方法";
            $header["SIMAI_NAME"]           = "本校在籍又は卒業の保護者・兄弟・姉妹";
            $header["REMARK1"]              = "志願理由";
            $header[$this->lastColumn]      = $this->lastColumn;

            /****************/
            /*  ヘッダー例  */
            /****************/

            //試験会場
            $hall = implode(' ', $db->getCol(knjl014pQuery::getHall($this->ObjYear, $this->field["APPLICANTDIV"], $this->field["TESTDIV"])));
            //性別
            $sex = implode(' ', $db->getCol(knjl014pQuery::getHeaderReiNameMst($this->ObjYear, "Z002", "NAME1")));
            //既/卒
            $fs_grddiv = implode(' ', $db->getCol(knjl014pQuery::getHeaderReiNameMst($this->ObjYear, "L016", "NAME1")));
            //通学方法
            $dormitory_flg = implode(' ', $db->getCol(knjl014pQuery::getHeaderReiNameMst($this->ObjYear, "L047", "NAME1")));

            $yOldE = ($this->field["APPLICANTDIV"] == "1") ? "12" : "15";   //入学年令

            $header_rei["ENTEXAMYEAR"]          = $this->ObjYear;
            $header_rei["APPLICANTDIV"]         = $this->field["APPLICANTDIV"];
            $header_rei["TESTDIV"]              = $this->field["TESTDIV"];
            $header_rei["DATADIV"]              = $this->field["DATADIV"];
            $header_rei["EXAMNO"]               = "123456";
            $header_rei["SIKEN_KAIJOU_CD1"]     = $hall;
            $header_rei["EXAMHALL_NAME"]        = "入力の必要なし";     //表示のみ
            $header_rei["RECEPTDATE"]           = date('Y/m/d', strtotime(CTRL_DATE));
            $header_rei["NAME"]                 = "アルプ太郎";
            $header_rei["NAME_KANA"]            = "あるぷたろう";
            $header_rei["SEX"]                  = $sex;
            $header_rei["BIRTHDAY"]             = date('Y/m/d', strtotime(CTRL_DATE." -{$yOldE} years"));
            $header_rei["REMARK2"]              = "12";
            $header_rei["ZIPCD"]                = "123-4567";
            $header_rei["ADDRESS1"]             = "全角30文字";
            $header_rei["ADDRESS2"]             = "全角30文字";
            $header_rei["TELNO"]                = "12-3456-7890";
            $header_rei["FS_CD"]                = "1234567";
            $header_rei["FS_NAME"]              = "入力の必要なし";     //表示のみ
            $header_rei["FS_DAY"]               = $this->ObjYear."/03/01";
            $header_rei["FS_GRDDIV"]            = $fs_grddiv;
            $header_rei["PRISCHOOLCD"]          = "1234567";
            $header_rei["PRISCHOOL_NAME"]       = "入力の必要なし";     //表示のみ
            $header_rei["PRISCHOOL_CLASS_CD"]   = "1234567";
            $header_rei["PRISCHOOL_CLASS_NAME"] = "入力の必要なし";     //表示のみ
            $header_rei["GNAME"]                = "アルプ父";
            $header_rei["GKANA"]                = "あるぷちち";
            $header_rei["GZIPCD"]               = "123-4567";
            $header_rei["GADDRESS1"]            = "全角30文字";
            $header_rei["GADDRESS2"]            = "全角30文字";
            $header_rei["GTELNO"]               = "123-456-7890";
            $header_rei["GTELNO2"]              = "123-456-7890";
            $header_rei["RELATIONSHIP"]         = "12";
            $header_rei["DORMITORY_FLG"]        = $dormitory_flg;
            $header_rei["SIMAI_NAME"]           = "全角30文字";
            $header_rei["REMARK1"]              = "全角45文字";
            $header_rei[$this->lastColumn]      = $this->lastColumn;
        }

        /********************/
        /*  調査書（高校）  */
        /********************/
        if ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "2") {
            $header = $header_rei = array();

            //教科
            $class_array = array();
            $result = $db->query(knjl014pQuery::getNameMstAll($this->ObjYear, "L008"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $class_array[$row["NAMECD2"]] = $row["ABBV1"];
            }

            /**************/
            /*  ヘッダー  */
            /**************/
            $header["ENTEXAMYEAR"]          = "※入試年度";
            $header["APPLICANTDIV"]         = "※入試制度";
            $header["TESTDIV"]              = "※入試区分";
            $header["DATADIV"]              = "※データ種類";
            $header["EXAMNO"]               = "※受験番号";
            for ($grade = 1; $grade <= 3; $grade++) {
                $gradename = $grade.'年';
                foreach ($class_array as $key => $label) {
                    $header["CONFRPT".$key."_".$grade] = $gradename.$label;
                }
            }
            $header["CLASS_CNT"]            = "クラス人員";
            $header["RANK"]                 = "順位";
            for ($grade = 1; $grade <= 3; $grade++) {
                $gradename = $grade.'年';
                $header["ATTEND_PRESENT".$grade]    = $gradename."出席すべき日数";
                $header["ATTEND_KESSEKI".$grade]    = $gradename."欠席日数";
                $header["ATTEND_LATE".$grade]       = $gradename."遅刻回数";
                $header["ATTEND_EARLY".$grade]      = $gradename."早退回数";
                $header["ATTEND_REMARK".$grade]     = $gradename."欠席、遅刻などの理由";
            }
            $header["REMARK1"]              = "備考";
            for ($grade = 1; $grade <= 3; $grade++) {
                $gradename = $grade.'年';
                $header["SPECIALACT".$grade]        = $gradename."特別活動の記録";
            }
            for ($grade = 1; $grade <= 3; $grade++) {
                $gradename = $grade.'年';
                $header["TOTALSTUDY".$grade]        = $gradename."総合的な学習の時間の記録";
            }
            $header["HEALTH"]               = "健康状況";
            $header["TOTAKREMARK"]          = "総合所見";
            $header["OTHERACT"]             = "その他の活動の記録";
            $header[$this->lastColumn]      = $this->lastColumn;

            /****************/
            /*  ヘッダー例  */
            /****************/
            $header_rei["ENTEXAMYEAR"]          = $this->ObjYear;
            $header_rei["APPLICANTDIV"]         = $this->field["APPLICANTDIV"];
            $header_rei["TESTDIV"]              = $this->field["TESTDIV"];
            $header_rei["DATADIV"]              = $this->field["DATADIV"];
            $header_rei["EXAMNO"]               = "123456";
            for ($grade = 1; $grade <= 3; $grade++) {
                foreach ($class_array as $key => $label) {
                    $header_rei["CONFRPT".$key."_".$grade] = "1";
                }
            }
            $header_rei["CLASS_CNT"]            = "123";
            $header_rei["RANK"]                 = "123";
            for ($grade = 1; $grade <= 3; $grade++) {
                $header_rei["ATTEND_PRESENT".$grade]    = "123";
                $header_rei["ATTEND_KESSEKI".$grade]    = "123";
                $header_rei["ATTEND_LATE".$grade]       = "123";
                $header_rei["ATTEND_EARLY".$grade]      = "123";
                $header_rei["ATTEND_REMARK".$grade]     = "全角10文字";
            }
            $header_rei["REMARK1"]              = "全角80文字";
            for ($grade = 1; $grade <= 3; $grade++) {
                $header_rei["SPECIALACT".$grade]        = "全角50文字";
            }
            for ($grade = 1; $grade <= 3; $grade++) {
                $header_rei["TOTALSTUDY".$grade]        = "全角50文字";
            }
            $header_rei["HEALTH"]               = "全角50文字";
            $header_rei["TOTAKREMARK"]          = "全角50文字";
            $header_rei["OTHERACT"]             = "全角50文字";
            $header_rei[$this->lastColumn]      = $this->lastColumn;
        }

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            // ヘッダ出力
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            // エラー出力
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            // データ出力
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        if ($this->field["OUTPUT"] != "1") {
            $query = "";
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = knjl014pQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                if ($this->field["DATADIV"] == "1") {
                    //願書
                    $query = knjl014pQuery::selectMainQuery2_1($this);
                } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "2") {
                    //高校・調査書
                    $query = knjl014pQuery::selectMainQuery2_2($this, $class_array);
                }
            }

            if ($this->field["DATADIV"] == "1") {
                //日付変換項目一覧
                $date_array = array("RECEPTDATE","BIRTHDAY","FS_DAY");
            }

            //SQL文発行
            if ($query) {
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["OUTPUT"] == "4") {     //データ
                        if ($this->field["DATADIV"] == "1") { //願書
                            //日付変換
                            foreach ($date_array as $key) {
                                $row[$key] = str_replace("-", "/", $row[$key]);
                            }
                        }
                        $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                        $array = $header;
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
        $AppName = $db->getOne(knjl014pQuery::getNameMst($this->ObjYear, "L003", $this->field["APPLICANTDIV"]));
        //入試区分名称
        $namecd1 = ($this->field["APPLICANTDIV"] == "2") ? "L004" : "L024";
        $TestdivName = $db->getOne(knjl014pQuery::getNameMst($this->ObjYear, $namecd1, $this->field["TESTDIV"]));
        //データ種類
        $datadiv = array("1" => "願書", "2" => "調査書");
        $DataDivName = $datadiv[$this->field["DATADIV"]];
        //ファイル名
        $title = $this->ObjYear.'年度 '.$AppName.' '.$TestdivName.$DataDivName.'データ';

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
