<?php

require_once('for_php7.php');
require_once('../../common/mycalendar.php');

class knjl014dModel extends Model
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
        $this->applicantdiv = 1;//1固定

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //受験種別
                             "DATADIV"          =>  VARS::post("DATADIV"),          //データ種類(1:願書, 2:内申)
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();   //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->examLen = $this->Properties["examnoLen"] ? $this->Properties["examnoLen"] : "5";

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
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
                $cnt = knjl014dQuery::updateQueryCsv1($this, $this->data_arr);

            //内申
            } elseif ($this->field["DATADIV"] == "2") {
                $cnt = knjl014dQuery::updateQueryCsv2($this, $this->data_arr);
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
                $checkdata["TESTDIV"]               = array(1,    "HISSU",  "INT",   "受験種別");
                $checkdata["DATADIV"]               = array(1,    "HISSU",  "INT",   "データ種類");
                $checkdata["SHDIV"]                 = array(1,    "HISSU",  "INT",   "専併区分");
                $checkdata["DESIREDIV"]             = array(1,    "HISSU",  "INT",   "志望類型");
                $checkdata["EXAMNO"]                = array($this->examLen,    "HISSU",  "INT2",  "受験番号");
                $checkdata["NAME"]                  = array(60,   "HISSU",  "",      "氏名");
                $checkdata["NAME_KANA"]             = array(120,  "HISSU",  "",      "ふりがな");
                $checkdata["SEX"]                   = array(1,    "HISSU",  "INT",   "性別");
                $checkdata["ERACD"]                 = array(1,    "",       "INT",   "元号");
                $checkdata["BIRTH_Y"]               = array(2,    "",       "INT2",  "年");
                $checkdata["BIRTH_M"]               = array(2,    "",       "INT2",  "月");
                $checkdata["BIRTH_D"]               = array(2,    "",       "INT2",  "日");
                $checkdata["CHILD"]                 = array(1,    "",       "",      "学園子女");
                $checkdata["ZIPCD"]                 = array(8,    "",       "",      "郵便番号");
                $checkdata["ADDRESS1"]              = array(150,  "",       "",      "住所");
                $checkdata["ADDRESS2"]              = array(150,  "",       "",      "方書");
                $checkdata["TELNO"]                 = array(14,   "",       "",      "電話番号");
                $checkdata["FS_CD"]                 = array(7,    "",       "INT2",  "出身学校コード");
                $checkdata["FS_GRDYEAR"]            = array(4,    "",       "INT",   "卒業年");

                $checkdata["GNAME"]                 = array(60,   "",       "",      "保護者氏名");
                $checkdata["GKANA"]                 = array(120,  "",       "",      "保護者ふりがな");
                $checkdata["RELATIONSHIP"]          = array(2,    "",       "INT2",  "保護者続柄");
                $checkdata["GZIPCD"]                = array(8,    "",       "",      "保護者郵便番号");
                $checkdata["GADDRESS1"]             = array(150,  "",       "",      "保護者住所");
                $checkdata["GADDRESS2"]             = array(150,  "",       "",      "保護者方書");
                $checkdata["GTELNO"]                = array(14,   "",       "",      "保護者電話番号");

                $checkdata["REMARK1"]               = array(120,  "",       "",      "備考１");
                $checkdata["REMARK2"]               = array(120,  "",       "",      "備考２");
                $checkdata["PRISCHOOLCD1"]          = array(7,    "",       "INT2",  "塾１");
                $checkdata["PRISCHOOL_CLASS_CD1"]   = array(7,    "",       "INT2",  "教室１");
                $checkdata["PRISCHOOLCD2"]          = array(7,    "",       "INT2",  "塾２");
                $checkdata["PRISCHOOL_CLASS_CD2"]   = array(7,    "",       "INT2",  "教室２");

            //内申
            } elseif ($this->field["DATADIV"] == "2") {
                //教科
                $class_array = array();
                $result = $db->query(knjl014dQuery::getNameMstAll($this->ObjYear, "L008"));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $class_array[$row["NAMECD2"]] = $row["ABBV1"];
                }

                $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "入試年度");
                $checkdata["TESTDIV"]               = array(1,    "HISSU",  "INT",   "受験種別");
                $checkdata["DATADIV"]               = array(1,    "HISSU",  "INT",   "データ種類");
                $checkdata["EXAMNO"]                = array($this->examLen,    "HISSU",  "INT2",  "受験番号");
                for ($grade = 1; $grade <= 3; $grade++) {
                    $gradename = $grade.'年生';
                    foreach ($class_array as $classcd => $label) {
                        $checkdata["CONFRPT".$classcd."_".$grade]   = array(1,    "",       "INT",   $gradename.$label);
                    }
                }
                $checkdata["REMARK1"]               = array(150,  "",       "",      "行動の記録");
                $checkdata["REMARK2"]               = array(150,  "",       "",      "英検");
                $checkdata["REMARK3"]               = array(150,  "",       "",      "数検");
                $checkdata["REMARK4"]               = array(150,  "",       "",      "漢検");
                $checkdata["REMARK5"]               = array(150,  "",       "",      "特別活動の記録");
                for ($grade = 1; $grade <= 3; $grade++) {
                    $gradename = $grade.'年生';
                    $checkdata["ABSENCE_DAYS".$grade]       = array(3,    "",       "INT",   $gradename."欠席日数");
                }
            }

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl014dQuery::deleteQueryErr();
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
                        $data["ENTEXAMYEAR"]           = $tmp[0];
                        $data["TESTDIV"]               = $tmp[1];
                        $data["DATADIV"]               = $tmp[2];
                        $data["SHDIV"]                 = $tmp[3];
                        $data["DESIREDIV"]             = $tmp[4];
                        $data["EXAMNO"]                = $tmp[5];
                        $data["NAME"]                  = $tmp[6];
                        $data["NAME_KANA"]             = $tmp[7];
                        $data["SEX"]                   = $tmp[8];
                        $data["ERACD"]                 = $tmp[9];
                        $data["BIRTH_Y"]               = $tmp[10];
                        $data["BIRTH_M"]               = $tmp[11];
                        $data["BIRTH_D"]               = $tmp[12];
                        $data["CHILD"]                 = $tmp[13];
                        $data["ZIPCD"]                 = $tmp[14];
                        $data["ADDRESS1"]              = $tmp[15];
                        $data["ADDRESS2"]              = $tmp[16];
                        $data["TELNO"]                 = $tmp[17];
                        $data["FS_CD"]                 = $tmp[18];
                        $data["FS_GRDYEAR"]            = $tmp[20];
                        $data["GNAME"]                 = $tmp[21];
                        $data["GKANA"]                 = $tmp[22];
                        $data["RELATIONSHIP"]          = $tmp[23];
                        $data["GZIPCD"]                = $tmp[24];
                        $data["GADDRESS1"]             = $tmp[25];
                        $data["GADDRESS2"]             = $tmp[26];
                        $data["GTELNO"]                = $tmp[27];
                        $data["REMARK1"]               = $tmp[28];
                        $data["REMARK2"]               = $tmp[29];
                        $data["PRISCHOOLCD1"]          = $tmp[30];
                        $data["PRISCHOOL_CLASS_CD1"]   = $tmp[32];
                        $data["PRISCHOOLCD2"]          = $tmp[34];
                        $data["PRISCHOOL_CLASS_CD2"]   = $tmp[36];

                        $dataCnt = 35+4;
                    //内申
                    } elseif ($this->field["DATADIV"] == "2") {
                        $data["ENTEXAMYEAR"]            = $tmp[0];
                        $data["TESTDIV"]                = $tmp[1];
                        $data["DATADIV"]                = $tmp[2];
                        $data["EXAMNO"]                 = $tmp[3];

                        $tmpCnt = 3;
                        for ($grade = 1; $grade <= 3; $grade++) {
                            foreach ($class_array as $classcd => $label) {
                                $data["CONFRPT".$classcd."_".$grade]    = $tmp[++$tmpCnt];
                            }
                            $tmpCnt++;
                            $tmpCnt++;
                        }
                        $data["REMARK1"]                = $tmp[++$tmpCnt];
                        $data["REMARK2"]                = $tmp[++$tmpCnt];
                        $data["REMARK3"]                = $tmp[++$tmpCnt];
                        $data["REMARK4"]                = $tmp[++$tmpCnt];
                        $data["REMARK5"]                = $tmp[++$tmpCnt];
                        for ($grade = 1; $grade <= 3; $grade++) {
                            $data["ABSENCE_DAYS".$grade]        = $tmp[++$tmpCnt];
                        }
                        $data[$this->lastColumn]        = $tmp[++$tmpCnt];

                        $dataCnt2 = 47;
                    }

                    //項目数によるファイルチェック
                    if (($this->field["DATADIV"] == "1" && $checkcount != $dataCnt) || ($this->field["DATADIV"] == "2" && $checkcount != $dataCnt2)) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        foreach ($checkdata as $key => $val) {
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

        $checkNameMstCd = array("SHDIV"             => "L004",
                                "DESIREDIV"         => "L058",
                                "SEX"               => "Z002",
                                "ERACD"             => "L007",
                                "RELATIONSHIP"      => "H201"
                                );

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
            $array = array("ENTEXAMYEAR","TESTDIV","DATADIV");
            if (in_array($key, $array)) {
                if (($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) || ($key != "ENTEXAMYEAR" && $this->field[$key] != $data[$key])) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
            //存在チェック（名称マスタ）
            if ($checkNameMstCd[$key] && $data[$key] != "") {
                $query = knjl014dQuery::nameCntSql($checkNameMstCd[$key], $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在(名称マスタ):", $existenceSep, $val[3]);
                }
            }
            if (in_array($key, array('FS_CD')) && strlen($data[$key]) && 0 == $db->getOne(knjl014dQuery::fsCdCntSql($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if (in_array($key, array('PRISCHOOLCD1', 'PRISCHOOLCD2')) && strlen($data[$key]) && 0 == $db->getOne(knjl014dQuery::priSchCdCntSql($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if (in_array($key, array('PRISCHOOL_CLASS_CD1')) && strlen($data[$key]) && 0 == $db->getOne(knjl014dQuery::priSchClassCdCntSql($data["PRISCHOOLCD1"], $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if (in_array($key, array('PRISCHOOL_CLASS_CD2')) && strlen($data[$key]) && 0 == $db->getOne(knjl014dQuery::priSchClassCdCntSql($data["PRISCHOOLCD2"], $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            //フラグチェック(値は1かNULL)
            if (in_array($key, array("CHILD"))) {
                if ($data[$key] > 1) {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※フラグの値:", $otherSep, $val[3]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }
        }

        if ($this->field["DATADIV"] != "1") {
            $base_cnt = $db->getOne(knjl014dQuery::cntEntexamApplicantbaseDat($this, $data));
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
            knjl014dQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
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
        $header = $header_rei = array();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /************/
        /* （願書） */
        /************/
        if ($this->field["DATADIV"] == "1") {
            /**************/
            /*  ヘッダー  */
            /**************/
            $header["ENTEXAMYEAR"]              = "※入試年度";
            $header["TESTDIV"]                  = "※受験種別";
            $header["DATADIV"]                  = "※データ種類";
            $header["SHDIV"]                    = "※専併区分";
            $header["DESIREDIV"]                = "※志望類型";
            $header["EXAMNO"]                   = "※受験番号";
            $header["NAME"]                     = "※氏名";
            $header["NAME_KANA"]                = "※ふりがな";
            $header["SEX"]                      = "※性別";
            $header["ERACD"]                    = "元号";
            $header["BIRTH_Y"]                  = "年";
            $header["BIRTH_M"]                  = "月";
            $header["BIRTH_D"]                  = "日";
            $header["CHILD"]                    = "学園子女";
            $header["ZIPCD"]                    = "郵便番号";
            $header["ADDRESS1"]                 = "住所";
            $header["ADDRESS2"]                 = "方書";
            $header["TELNO"]                    = "電話番号";
            $header["FS_CD"]                    = "出身学校コード";
            $header["FS_NAME"]                  = "出身学校名";
            $header["FS_GRDYEAR"]               = "卒業年";
            $header["GNAME"]                    = "保護者氏名";
            $header["GKANA"]                    = "保護者ふりがな";
            $header["RELATIONSHIP"]             = "保護者続柄";
            $header["GZIPCD"]                   = "保護者郵便番号";
            $header["GADDRESS1"]                = "保護者住所";
            $header["GADDRESS2"]                = "保護者方書";
            $header["GTELNO"]                   = "保護者電話番号";
            $header["REMARK1"]                  = "備考１";
            $header["REMARK2"]                  = "備考２";
            $header["PRISCHOOLCD1"]             = "塾１";
            $header["PRISCHOOL_NAME1"]          = "塾１（名称）";
            $header["PRISCHOOL_CLASS_CD1"]      = "教室１";
            $header["PRISCHOOL_CLASS_NAME1"]    = "教室１（名称）";
            $header["PRISCHOOLCD2"]             = "塾２";
            $header["PRISCHOOL_NAME2"]          = "塾２（名称）";
            $header["PRISCHOOL_CLASS_CD2"]      = "教室２";
            $header["PRISCHOOL_CLASS_NAME2"]    = "教室２（名称）";
            $header[$this->lastColumn]          = $this->lastColumn;

            /****************/
            /*  ヘッダー例  */
            /****************/
            //性別
            $sex = implode(' ', $db->getCol(knjl014dQuery::getHeaderReiNameMst($this->ObjYear, "Z002", "NAME1")));
            //既/卒
            $fs_grddiv = implode(' ', $db->getCol(knjl014dQuery::getHeaderReiNameMst($this->ObjYear, "L016", "NAME1")));
            //専併区分
            $shdiv = implode(' ', $db->getCol(knjl014dQuery::getHeaderReiNameMst($this->ObjYear, "L006", "NAME1")));
            //志望類型
            $desirediv = implode(' ', $db->getCol(knjl014dQuery::getHeaderReiNameMst($this->ObjYear, "L058", "NAME1")));
            //元号
            $eracd = implode(' ', $db->getCol(knjl014dQuery::getHeaderReiNameMst($this->ObjYear, "L007", "NAME1")));
            //受験種別
            $testDiv = implode(' ', $db->getCol(knjl014dQuery::getHeaderReiNameMst($this->ObjYear, "L004", "NAME1")));
            //保護者続柄
            $relationship = implode(' ', $db->getCol(knjl014dQuery::getHeaderReiNameMst($this->ObjYear, "H201", "NAME1", "RELA")));

            $header_rei["ENTEXAMYEAR"]              = $this->ObjYear;
            $header_rei["TESTDIV"]                  = $testDiv;
            $header_rei["DATADIV"]                  = "1:願書 2:内申";
            $header_rei["SHDIV"]                    = $shdiv;
            $header_rei["DESIREDIV"]                = $desirediv;
            $header_rei["EXAMNO"]                   = $this->examLen."桁";
            $header_rei["NAME"]                     = "アルプ太郎";
            $header_rei["NAME_KANA"]                = "あるぷたろう";
            $header_rei["SEX"]                      = $sex;
            $header_rei["ERACD"]                    = $eracd;
            $header_rei["BIRTH_Y"]                  = to_wareki2(substr(CTRL_DATE, "0", "4"), substr(CTRL_DATE, "5", "2"), substr(CTRL_DATE, "8", "2"));
            $header_rei["BIRTH_M"]                  = substr(CTRL_DATE, "5", "2");
            $header_rei["BIRTH_D"]                  = substr(CTRL_DATE, "8", "2");
            $header_rei["CHILD"]                    = "1";
            $header_rei["ZIPCD"]                    = "123-4567";
            $header_rei["ADDRESS1"]                 = "全角30文字";
            $header_rei["ADDRESS2"]                 = "全角30文字";
            $header_rei["TELNO"]                    = "123-456-7890";
            $header_rei["FS_CD"]                    = "1234567";
            $header_rei["FS_NAME"]                  = "入力の必要なし";     //表示のみ
            $header_rei["FS_GRDYEAR"]               = $this->ObjYear;
            $header_rei["GNAME"]                    = "アルプ父";
            $header_rei["GKANA"]                    = "あるぷちち";
            $header_rei["RELATIONSHIP"]             = $relationship;
            $header_rei["GZIPCD"]                   = "123-4567";
            $header_rei["GADDRESS1"]                = "全角30文字";
            $header_rei["GADDRESS2"]                = "全角30文字";
            $header_rei["GTELNO"]                   = "123-456-7890";
            $header_rei["REMARK1"]                  = "全角20文字";
            $header_rei["REMARK2"]                  = "全角20文字";
            $header_rei["PRISCHOOLCD1"]             = "1234567";
            $header_rei["PRISCHOOL_NAME1"]          = "入力の必要なし";     //表示のみ
            $header_rei["PRISCHOOL_CLASS_CD1"]      = "1234567";
            $header_rei["PRISCHOOL_CLASS_NAME1"]    = "入力の必要なし";     //表示のみ
            $header_rei["PRISCHOOLCD2"]             = "1234567";
            $header_rei["PRISCHOOL_NAME2"]          = "入力の必要なし";     //表示のみ
            $header_rei["PRISCHOOL_CLASS_CD2"]      = "1234567";
            $header_rei["PRISCHOOL_CLASS_NAME2"]    = "入力の必要なし";     //表示のみ
            $header_rei[$this->lastColumn]          = $this->lastColumn;
        }

        /************/
        /* （内申） */
        /************/
        if ($this->field["DATADIV"] == "2") {
            //教科
            $class_array = array();
            $result = $db->query(knjl014dQuery::getNameMstAll($this->ObjYear, "L008"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $class_array[$row["NAMECD2"]] = $row["ABBV1"];
            }

            /**************/
            /*  ヘッダー  */
            /**************/
            $header["ENTEXAMYEAR"]          = "※入試年度";
            $header["TESTDIV"]              = "※受験種別";
            $header["DATADIV"]              = "※データ種類";
            $header["EXAMNO"]               = "※受験番号";
            for ($grade = 1; $grade <= 3; $grade++) {
                $gradename = $grade.'年生';
                foreach ($class_array as $key => $label) {
                    $header["CONFRPT".$key."_".$grade] = $gradename.$label;
                }
                $header["TOTAL5_".$grade]       = $gradename."5科合計";
                $header["TOTALALL_".$grade]     = $gradename."全体合計";
            }
            $header["REMARK1"]              = "行動の記録";
            $header["REMARK2"]              = "英検";
            $header["REMARK3"]              = "数検";
            $header["REMARK4"]              = "漢検";
            $header["REMARK5"]              = "特別活動の記録";
            for ($grade = 1; $grade <= 3; $grade++) {
                $gradename = $grade.'年生';
                $header["ABSENCE_DAYS".$grade]      = $gradename."欠席日数";
            }
            $header["ABSENCE_DAYS_TOTAL"]   = "欠席日数合計";
            $header[$this->lastColumn]      = $this->lastColumn;

            /****************/
            /*  ヘッダー例  */
            /****************/
            //受験種別
            $testDiv = implode(' ', $db->getCol(knjl014dQuery::getHeaderReiNameMst($this->ObjYear, "L004", "NAME1")));

            $header_rei["ENTEXAMYEAR"]          = $this->ObjYear;
            $header_rei["TESTDIV"]              = $testDiv;
            $header_rei["DATADIV"]              = "1:願書 2:内申";
            $header_rei["EXAMNO"]               = $this->examLen."桁";
            for ($grade = 1; $grade <= 3; $grade++) {
                foreach ($class_array as $key => $label) {
                    $header_rei["CONFRPT".$key."_".$grade] = "1";
                }
                $header_rei["TOTAL5_".$grade]       = "入力の必要なし";     //表示のみ
                $header_rei["TOTALALL_".$grade]     = "入力の必要なし";     //表示のみ
            }
            $header_rei["REMARK1"]              = "1";
            $header_rei["REMARK2"]              = "2";
            $header_rei["REMARK3"]              = "3";
            $header_rei["REMARK4"]              = "4";
            $header_rei["REMARK5"]              = "(全角15文字×3行)";
            for ($grade = 1; $grade <= 3; $grade++) {
                $header_rei["ABSENCE_DAYS".$grade]      = "123";
            }
            $header_rei["ABSENCE_DAYS_TOTAL"]   = "入力の必要なし";     //表示のみ
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
                $query = knjl014dQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                if ($this->field["DATADIV"] == "1") {
                    //願書
                    $query = knjl014dQuery::selectMainQuery1($this);
                } elseif ($this->field["DATADIV"] == "2") {
                    //内申
                    $query = knjl014dQuery::selectMainQuery2($this, $class_array);
                }
            }

            //SQL文発行
            if ($query) {
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["OUTPUT"] == "4") {     //データ
                        if ($this->field["DATADIV"] == "2") { //内申
                            $row["ABSENCE_DAYS_TOTAL"] = $row["ABSENCE_DAYS1"] + $row["ABSENCE_DAYS2"] + $row["ABSENCE_DAYS3"];
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
        //受験種別名称
        $TestdivName = $db->getOne(knjl014dQuery::getNameMst($this->ObjYear, "L004", $this->field["TESTDIV"]));
        //データ種類
        $datadiv = array("1" => "願書", "2" => "内申");
        $DataDivName = $datadiv[$this->field["DATADIV"]];
        //ファイル名
        $title = $this->ObjYear.'年度 '.$TestdivName.$DataDivName.'データ';

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
//和暦変換用の関数2
function to_wareki2($y, $m, $d, $warekiName = "")
{
    //年月日を文字列として結合
    $ymd = sprintf("%02d%02d%02d", $y, $m, $d);
    if ($ymd <= "19120729") {
        $gg = "明治";
        $yy = $y - 1867;
    } elseif ($ymd >= "19120730" && $ymd <= "19261224") {
        $gg = "大正";
        $yy = $y - 1911;
    } elseif ($ymd >= "19261225" && $ymd <= "19890107") {
        $gg = "昭和";
        $yy = $y - 1925;
    } elseif ($ymd >= "19890108") {
        $gg = "平成";
        $yy = $y - 1988;
    }
    if ($warekiName) {
        $wareki = $gg;
    } else {
        $wareki = $yy;
    }

    return $wareki;
}
