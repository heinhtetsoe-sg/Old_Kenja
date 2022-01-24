<?php

require_once('for_php7.php');
class knjl114dModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $ObjYear;
    public $lastColumn;//CSVの最後の列の値

    public function init()
    {
        $this->ObjYear      = VARS::post("YEAR");
        $this->applicantdiv = 1;//1固定
        $this->shdiv        = 1;//1固定

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "DESIREDIV"        =>  VARS::post("DESIREDIV"),        //志願区分
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
            $cnt = knjl114dQuery::updateQueryCsv1($this, $this->data_arr);

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
            $checkdata["DESIREDIV"]             = array(1,    "HISSU",  "INT",   "志願区分");
            $checkdata["TESTDIV"]               = array(2,    "HISSU",  "INT",   "入試区分");
            $checkdata["EXAMNO"]                = array(3,    "HISSU",  "INT2",  "受験番号");
            $checkdata["NAME"]                  = array(60,   "HISSU",  "",      "氏名");
            $checkdata["NAME_KANA"]             = array(120,  "HISSU",  "",      "ふりがな");
            $checkdata["SEX"]                   = array(1,    "HISSU",  "INT",   "性別");
            $checkdata["ERACD"]                 = array(1,    "",       "INT",   "元号");
            $checkdata["BIRTH_Y"]               = array(2,    "",       "INT2",  "年");
            $checkdata["BIRTH_M"]               = array(2,    "",       "INT2",  "月");
            $checkdata["BIRTH_D"]               = array(2,    "",       "INT2",  "日");
            $checkdata["FS_CD"]                 = array(7,    "",       "INT2",  "出身学校（中学）コード");
            $checkdata["FS_ERACD"]              = array(1,    "",       "INT",   "卒業元号");
            $checkdata["FS_Y"]                  = array(2,    "",       "INT2",  "卒業年");
            $checkdata["FS_M"]                  = array(2,    "",       "INT2",  "卒業月");
            $checkdata["FS_GRDDIV"]             = array(1,    "",       "INT",   "卒業区分");
            $checkdata["REMARK8_033"]           = array(10,   "",       "DATE",  "転編入学照会日");
            $checkdata["REMARK1_033"]           = array(1,    "",       "INT",   "転籍フラグ");
            $checkdata["REMARK2_033"]           = array(7,    "",       "INT2",  "出身校（高校）コード");
            $checkdata["REMARK3_033"]           = array(1,    "",       "INT",   "出身校（高校）課程");
            $checkdata["REMARK4_033"]           = array(150,  "",       "",      "出身校（高校）学科");
            $checkdata["REMARK5_033"]           = array(150,  "",       "",      "出身校（高校）学年");
            $checkdata["REMARK6_033"]           = array(1  ,  "",       "INT",   "出身校（高校）状況");
            $checkdata["REMARK9_033"]           = array(10,   "",       "DATE",  "調査書照会日");
            $checkdata["ZIPCD"]                 = array(8,    "",       "",      "郵便番号");
            $checkdata["ADDRESS1"]              = array(150,  "",       "",      "住所");
            $checkdata["ADDRESS2"]              = array(150,  "",       "",      "方書");
            $checkdata["TELNO"]                 = array(14,   "",       "",      "電話番号");

            $checkdata["GNAME"]                 = array(60,   "",       "",      "保護者氏名");
            $checkdata["GKANA"]                 = array(120,  "",       "",      "保護者ふりがな");
            $checkdata["RELATIONSHIP"]          = array(2,    "",       "INT2",  "保護者続柄");
            $checkdata["GZIPCD"]                = array(8,    "",       "",      "保護者郵便番号");
            $checkdata["GADDRESS1"]             = array(150,  "",       "",      "保護者住所");
            $checkdata["GADDRESS2"]             = array(150,  "",       "",      "保護者方書");
            $checkdata["GTELNO"]                = array(14,   "",       "",      "保護者電話番号");
            $checkdata["GTELNO2"]               = array(14,   "",       "",      "保護者携帯電話");

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl114dQuery::deleteQueryErr();
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

                    $data["ENTEXAMYEAR"]              = $tmp[0];
                    $data["DESIREDIV"]                = $tmp[1];
                    $data["TESTDIV"]                  = $tmp[2];
                    $data["EXAMNO"]                   = $tmp[3];
                    $data["NAME"]                     = $tmp[4];
                    $data["NAME_KANA"]                = $tmp[5];
                    $data["SEX"]                      = $tmp[6];
                    $data["ERACD"]                    = $tmp[7];
                    $data["BIRTH_Y"]                  = $tmp[8];
                    $data["BIRTH_M"]                  = $tmp[9];
                    $data["BIRTH_D"]                  = $tmp[10];
                    $data["FS_CD"]                    = $tmp[11];
                    $data["FS_ERACD"]                 = $tmp[12];
                    $data["FS_Y"]                     = $tmp[13];
                    $data["FS_M"]                     = $tmp[14];
                    $data["FS_GRDDIV"]                = $tmp[15];
                    $data["REMARK8_033"]              = $tmp[16];
                    $data["REMARK1_033"]              = $tmp[17];
                    $data["REMARK2_033"]              = $tmp[18];
                    $data["REMARK3_033"]              = $tmp[19];
                    $data["REMARK4_033"]              = $tmp[20];
                    $data["REMARK5_033"]              = $tmp[21];
                    $data["REMARK6_033"]              = $tmp[22];
                    $data["REMARK9_033"]              = $tmp[23];
                    $data["ZIPCD"]                    = $tmp[24];
                    $data["ADDRESS1"]                 = $tmp[25];
                    $data["ADDRESS2"]                 = $tmp[26];
                    $data["TELNO"]                    = $tmp[27];
                    $data["GNAME"]                    = $tmp[28];
                    $data["GKANA"]                    = $tmp[29];
                    $data["RELATIONSHIP"]             = $tmp[30];
                    $data["GZIPCD"]                   = $tmp[31];
                    $data["GADDRESS1"]                = $tmp[32];
                    $data["GADDRESS2"]                = $tmp[33];
                    $data["GTELNO"]                   = $tmp[34];
                    $data["GTELNO2"]                  = $tmp[35];

                    $dataCnt = 37;

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
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

        $checkNameMstCd = array("SEX"               => "Z002",
                                "ERACD"             => "L007",
                                "FS_ERACD"          => "L007",
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
            $array = array("ENTEXAMYEAR","TESTDIV","DESIREDIV");
            if (in_array($key, $array)) {
                if (($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) || ($key != "ENTEXAMYEAR" && $this->field[$key] != $data[$key])) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
            //存在チェック（名称マスタ）
            if ($checkNameMstCd[$key] && $data[$key] != "") {
                $query = knjl114dQuery::nameCntSql($checkNameMstCd[$key], $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在(名称マスタ):", $existenceSep, $val[3]);
                }
            }
            if (in_array($key, array('FS_CD', 'REMARK2_033')) && strlen($data[$key]) && 0 == $db->getOne(knjl114dQuery::fsCdCntSql($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            //フラグチェック(値は1かNULL)
            if (in_array($key, array("REMARK1_033"))) {
                if ($data[$key] > 1) {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※フラグの値:", $otherSep, $val[3]);
                }
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

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl114dQuery::insertQueryErr($db, $line, $check_error);
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
        $header = $header_rei = array();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /**************/
        /*  ヘッダー  */
        /**************/
        $header["ENTEXAMYEAR"]              = "※入試年度";
        $header["DESIREDIV"]                = "※志願区分";
        $header["TESTDIV"]                  = "※入試区分";
        $header["EXAMNO"]                   = "※受験番号";
        $header["NAME"]                     = "※氏名";
        $header["NAME_KANA"]                = "※ふりがな";
        $header["SEX"]                      = "※性別";
        $header["ERACD"]                    = "元号";
        $header["BIRTH_Y"]                  = "年";
        $header["BIRTH_M"]                  = "月";
        $header["BIRTH_D"]                  = "日";
        $header["FS_CD"]                    = "出身校（中学）コード";
        $header["FS_ERACD"]                 = "卒業元号";
        $header["FS_Y"]                     = "卒業年";
        $header["FS_M"]                     = "卒業月";
        $header["FS_GRDDIV"]                = "卒業区分";
        $header["REMARK8_033"]              = "転編入学照会日";
        $header["REMARK1_033"]              = "転籍フラグ";
        $header["REMARK2_033"]              = "出身校（高校）コード";
        $header["REMARK3_033"]              = "出身校（高校）課程";
        $header["REMARK4_033"]              = "出身校（高校）学科";
        $header["REMARK5_033"]              = "出身校（高校）学年";
        $header["REMARK6_033"]              = "出身校（高校）状況";
        $header["REMARK9_033"]              = "調査書照会日";
        $header["ZIPCD"]                    = "郵便番号";
        $header["ADDRESS1"]                 = "住所";
        $header["ADDRESS2"]                 = "方書";
        $header["TELNO"]                    = "電話番号";

        $header["GNAME"]                    = "保護者氏名";
        $header["GKANA"]                    = "保護者ふりがな";
        $header["RELATIONSHIP"]             = "保護者続柄";
        $header["GZIPCD"]                   = "保護者郵便番号";
        $header["GADDRESS1"]                = "保護者住所";
        $header["GADDRESS2"]                = "保護者方書";
        $header["GTELNO"]                   = "保護者電話番号";
        $header["GTELNO2"]                  = "保護者携帯電話";

        $header[$this->lastColumn]          = $this->lastColumn;

        /****************/
        /*  ヘッダー例  */
        /****************/
        //性別
        $sex = implode(' ', $db->getCol(knjl114dQuery::getHeaderReiNameMst($this->ObjYear, "Z002", "NAME1")));
        //既/卒
        $fs_grddiv = implode(' ', $db->getCol(knjl114dQuery::getHeaderReiNameMst($this->ObjYear, "L016", "NAME1")));
        //専併区分
        $shdiv = implode(' ', $db->getCol(knjl114dQuery::getHeaderReiNameMst($this->ObjYear, "L006", "NAME1")));
        //志望区分
        $desirediv = implode(' ', $db->getCol(knjl114dQuery::getHeaderReiNameMst($this->ObjYear, "L058", "NAME1", $this->field["DESIREDIV"])));
        //元号
        $eracd = implode(' ', $db->getCol(knjl114dQuery::getHeaderReiNameMst($this->ObjYear, "L007", "NAME1")));
        //入試区分
        $testDiv = implode(' ', $db->getCol(knjl114dQuery::getHeaderReiNameMst($this->ObjYear, "L004", "NAME1", $this->field["TESTDIV"])));
        //保護者続柄
        $relationship = implode(' ', $db->getCol(knjl114dQuery::getHeaderReiNameMst($this->ObjYear, "H201", "NAME1", "RELA")));

        $header_rei["ENTEXAMYEAR"]          = $this->ObjYear;
        $header_rei["DESIREDIV"]            = $desirediv;
        $header_rei["TESTDIV"]              = $testDiv;
        $header_rei["EXAMNO"]               = "123";
        $header_rei["NAME"]                 = "アルプ太郎";
        $header_rei["NAME_KANA"]            = "あるぷたろう";
        $header_rei["SEX"]                  = $sex;
        $header_rei["ERACD"]                = $eracd;
        $header_rei["BIRTH_Y"]              = to_wareki2(substr(CTRL_DATE, "0", "4"), substr(CTRL_DATE, "5", "2"), substr(CTRL_DATE, "8", "2"));
        $header_rei["BIRTH_M"]              = substr(CTRL_DATE, "5", "2");
        $header_rei["BIRTH_D"]              = substr(CTRL_DATE, "8", "2");
        $header_rei["FS_CD"]                = "1234567";
        $header_rei["FS_ERACD"]             = $eracd;
        $header_rei["FS_Y"]                 = to_wareki2(substr(CTRL_DATE, "0", "4"), substr(CTRL_DATE, "5", "2"), substr(CTRL_DATE, "8", "2"));
        $header_rei["FS_M"]                 = substr(CTRL_DATE, "5", "2");
        $header_rei["FS_GRDDIV"]            = $fs_grddiv;
        $header_rei["REMARK8_033"]          = str_replace('-', '/', CTRL_DATE);
        $header_rei["REMARK1_033"]          = "1";
        $header_rei["REMARK2_033"]          = "1234567";
        $header_rei["REMARK3_033"]          = "1:全日制 2:定時制 3:通信制";
        $header_rei["REMARK4_033"]          = "学科";
        $header_rei["REMARK5_033"]          = "学年";
        $header_rei["REMARK6_033"]          = "1:在学中 2:退学";
        $header_rei["REMARK9_033"]          = str_replace('-', '/', CTRL_DATE);
        $header_rei["ZIPCD"]                = "123-4567";
        $header_rei["ADDRESS1"]             = "全角30文字";
        $header_rei["ADDRESS2"]             = "全角30文字";
        $header_rei["TELNO"]                = "123-456-7890";

        $header_rei["GNAME"]                = "アルプ父";
        $header_rei["GKANA"]                = "あるぷちち";
        $header_rei["RELATIONSHIP"]         = $relationship;
        $header_rei["GZIPCD"]               = "123-4567";
        $header_rei["GADDRESS1"]            = "全角30文字";
        $header_rei["GADDRESS2"]            = "全角30文字";
        $header_rei["GTELNO"]               = "123-456-7890";
        $header_rei["GTELNO2"]              = "123-456-7890";

        $header_rei[$this->lastColumn]      = $this->lastColumn;

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
                $query = knjl114dQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"] == "4") {
                $query = knjl114dQuery::selectMainQuery1($this);
            }

            //SQL文発行
            if ($query) {
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["OUTPUT"] == "4") {     //データ
                        $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                        //転編入学照会日
                        $row["REMARK8_033"] = str_replace('-', '/', $row["REMARK8_033"]);
                        //調査書照会日
                        $row["REMARK9_033"] = str_replace('-', '/', $row["REMARK9_033"]);

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
        //入試区分名称
        $TestdivName = $db->getOne(knjl114dQuery::getNameMst($this->ObjYear, "L004", $this->field["TESTDIV"]));
        //ファイル名
        $title = $this->ObjYear.'年度 '.$TestdivName.'データ';

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
