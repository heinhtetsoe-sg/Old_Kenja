<?php

require_once('for_php7.php');
class knjl310qModel extends Model
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
                             "TESTDIV0"         =>  VARS::post("TESTDIV0"),         //入試区分
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
        if (SCHOOLKIND == "P") {
            $this->nameMstTest = "LP24";
        } elseif (SCHOOLKIND == "J") {
            $this->nameMstTest = "L024";
        } else {
            $this->nameMstTest = "L004";
        }

        //プロパティファイルの読込み
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
            if (SCHOOLKIND == "P") {
                //小学・願書
                if ($this->field["DATADIV"] == "1") {
                    $cnt = knjl310qQuery::updateQueryCsv3_1($this, $this->data_arr);
                }
            } elseif (SCHOOLKIND == "J") {
                //中学・願書
                if ($this->field["DATADIV"] == "1") {
                    $cnt = knjl310qQuery::updateQueryCsv1_1($this, $this->data_arr);
                }
            } else {
                //高校・願書
                if ($this->field["DATADIV"] == "1") {
                    $cnt = knjl310qQuery::updateQueryCsv2_1($this, $this->data_arr);

                //高校・調査書
                } elseif ($this->field["DATADIV"] == "2") {
                    $cnt = knjl310qQuery::updateQueryCsv2_2($this, $this->data_arr);

                //高校・活動実績
                } elseif ($this->field["DATADIV"] == "3") {
                    $cnt = knjl310qQuery::updateQueryCsv2_3($this, $this->data_arr);
                }
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

            if (SCHOOLKIND == "P") {
                //小学・願書
                if ($this->field["DATADIV"] == "1") {
                    $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "入試年度");
                    $checkdata["APPLICANTDIV"]          = array(1,    "HISSU",  "INT",   "入試制度");
                    $checkdata["TESTDIV"]               = array(1,    "HISSU",  "INT",   "入試区分");
                    $checkdata["DATADIV"]               = array(1,    "HISSU",  "INT",   "データ種類");
                    $checkdata["EXAMNO"]                = array(4,    "HISSU",  "INT2",  "受験番号");
                    $checkdata["NAME"]                  = array(60,   "HISSU",  "",      "氏名");
                    $checkdata["NAME_KANA"]             = array(120,  "HISSU",  "",      "ふりがな");
                    $checkdata["SEX"]                   = array(1,    "HISSU",  "INT",   "性別");
                    $checkdata["BIRTHDAY"]              = array(10,   "",       "DATE",  "生年月日");
                    $checkdata["ZIPCD"]                 = array(8,    "",       "",      "郵便番号");
                    $checkdata["ADDRESS1"]              = array(90,   "",       "",      "住所");
                    $checkdata["ADDRESS2"]              = array(90,   "",       "",      "方書");
                    $checkdata["TELNO"]                 = array(14,   "",       "",      "電話番号");
                    $checkdata["FS_CD"]                 = array(7,    "",       "INT2",  "出身学校コード");
                    $checkdata["FAMILY_REGISTER"]       = array(2,    "",       "INT2",  "本籍");
                    $checkdata["GNAME"]                 = array(60,   "",       "",      "保護者氏名");
                    $checkdata["GKANA"]                 = array(120,  "",       "",      "保護者ふりがな");
                    $checkdata["GZIPCD"]                = array(8,    "",       "",      "保護者郵便番号");
                    $checkdata["GADDRESS1"]             = array(90,   "",       "",      "保護者住所");
                    $checkdata["GADDRESS2"]             = array(90,   "",       "",      "保護者方書");
                    $checkdata["GTELNO"]                = array(14,   "",       "",      "保護者電話番号");
                    $checkdata["RELATIONSHIP"]          = array(2,    "",       "INT2",  "保護者続柄");
                }
            } elseif (SCHOOLKIND == "J") {
                //中学・願書
                if ($this->field["DATADIV"] == "1") {
                    $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "入試年度");
                    $checkdata["APPLICANTDIV"]          = array(1,    "HISSU",  "INT",   "入試制度");
                    $checkdata["TESTDIV"]               = array(1,    "HISSU",  "INT",   "入試区分");
                    $checkdata["DATADIV"]               = array(1,    "HISSU",  "INT",   "データ種類");
                    $checkdata["EXAMNO"]                = array(5,    "HISSU",  "INT2",  "受験番号");
                    $checkdata["NAME"]                  = array(60,   "HISSU",  "",      "氏名");
                    $checkdata["NAME_KANA"]             = array(120,  "HISSU",  "",      "ふりがな");
                    $checkdata["SEX"]                   = array(1,    "HISSU",  "INT",   "性別");
                    $checkdata["BIRTHDAY"]              = array(10,   "",       "DATE",  "生年月日");
                    $checkdata["ZIPCD"]                 = array(8,    "",       "",      "郵便番号");
                    $checkdata["ADDRESS1"]              = array(90,   "",       "",      "住所");
                    $checkdata["ADDRESS2"]              = array(90,   "",       "",      "方書");
                    $checkdata["TELNO"]                 = array(14,   "",       "",      "電話番号");
                    $checkdata["FS_CD"]                 = array(7,    "",       "INT2",  "出身学校コード");
                    $checkdata["FAMILY_REGISTER"]       = array(2,    "",       "INT2",  "本籍");
                    $checkdata["BROS_INFO1"]            = array(150,  "",       "",      "兄弟情報１");
                    $checkdata["BROS_INFO2"]            = array(150,  "",       "",      "兄弟情報２");
                    $checkdata["GNAME"]                 = array(60,   "",       "",      "保護者氏名");
                    $checkdata["GKANA"]                 = array(120,  "",       "",      "保護者ふりがな");
                    $checkdata["GZIPCD"]                = array(8,    "",       "",      "保護者郵便番号");
                    $checkdata["GADDRESS1"]             = array(90,   "",       "",      "保護者住所");
                    $checkdata["GADDRESS2"]             = array(90,   "",       "",      "保護者方書");
                    $checkdata["GTELNO"]                = array(14,   "",       "",      "保護者電話番号");
                    $checkdata["RELATIONSHIP"]          = array(2,    "",       "INT2",  "保護者続柄");
                    $checkdata["GJOB"]                  = array(150,  "",       "",      "保護者職業");
                    $checkdata["REMARK1"]               = array(150,  "",       "",      "保護者備考");
                    $checkdata["ABSENCE_DAYS2"]         = array(3,    "",       "INT",   "出欠の記録5年生");
                    $checkdata["ABSENCE_DAYS3"]         = array(3,    "",       "INT",   "出欠の記録6年生");
                    $checkdata["SHDIV"]                 = array(1,    "HISSU",  "INT",   "専併区分");
                    $checkdata["RECOM_EXAMNO"]          = array(5,    "",       "INT",   "推薦受験番号");
                    $checkdata["SH_SCHOOLCD1"]          = array(7,    "",       "INT2",  "志望校第１志望");
                    $checkdata["SH_SCHOOLCD2"]          = array(7,    "",       "INT2",  "志望校第２志望");
                    $checkdata["SH_SCHOOLCD3"]          = array(7,    "",       "INT2",  "志望校第３志望");
                    $checkdata["SH_SCHOOLCD4"]          = array(7,    "",       "INT2",  "志望校第４志望");
                }
            } else {
                //高校・願書
                if ($this->field["DATADIV"] == "1") {
                    $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "入試年度");
                    $checkdata["APPLICANTDIV"]          = array(1,    "HISSU",  "INT",   "入試制度");
                    $checkdata["TESTDIV0"]              = array(1,    "HISSU",  "INT",   "入試区分");
                    $checkdata["DATADIV"]               = array(1,    "HISSU",  "INT",   "データ種類");
                    $checkdata["EXAMNO"]                = array(5,    "HISSU",  "INT2",  "受験番号");
                    $checkdata["JIZEN_BANGOU"]          = array(5,    "",       "INT",   "実戦模試受験番号");
                    $checkdata["NAME"]                  = array(60,   "HISSU",  "",      "氏名");
                    $checkdata["NAME_KANA"]             = array(120,  "HISSU",  "",      "ふりがな");
                    $checkdata["SEX"]                   = array(1,    "HISSU",  "INT",   "性別");
                    $checkdata["BIRTHDAY"]              = array(10,   "",       "DATE",  "生年月日");
                    $checkdata["ZIPCD"]                 = array(8,    "",       "",      "郵便番号");
                    $checkdata["ADDRESS1"]              = array(90,   "",       "",      "住所");
                    $checkdata["ADDRESS2"]              = array(90,   "",       "",      "方書");
                    $checkdata["TELNO"]                 = array(14,   "",       "",      "電話番号");
                    $checkdata["FS_CD"]                 = array(7,    "",       "INT2",  "出身学校コード");
                    $checkdata["FS_DAY"]                = array(10,   "",       "DATE",  "卒業年月日");
                    $checkdata["FS_GRDDIV"]             = array(1,    "",       "INT",   "既/卒");
                    $checkdata["GNAME"]                 = array(60,   "",       "",      "保護者氏名");
                    $checkdata["GKANA"]                 = array(120,  "",       "",      "保護者ふりがな");
                    $checkdata["GZIPCD"]                = array(8,    "",       "",      "保護者郵便番号");
                    $checkdata["GADDRESS1"]             = array(90,   "",       "",      "保護者住所");
                    $checkdata["GADDRESS2"]             = array(90,   "",       "",      "保護者方書");
                    $checkdata["GTELNO"]                = array(14,   "",       "",      "保護者電話番号");
                    $checkdata["RELATIONSHIP"]          = array(2,    "",       "INT2",  "保護者続柄");
                    $checkdata["DORMITORY_FLG"]         = array(1,    "",       "INT",   "入寮希望");
                    $checkdata["SCHOLAR_KIBOU"]         = array(1,    "",       "INT",   "スカラーシップ希望");
                    if ($this->field["TESTDIV0"] != "1") {
                        $checkdata["SHDIV"]                 = array(1,    "HISSU",  "INT",   "専併区分");
                    }
                    $checkdata["SH_SCHOOLCD1"]          = array(7,    "",       "INT2",  "志望校第１志望");
                    $checkdata["SH_SCHOOLCD2"]          = array(7,    "",       "INT2",  "志望校第２志望");
                    $checkdata["SH_SCHOOLCD3"]          = array(7,    "",       "INT2",  "志望校第３志望");
                    $checkdata["SH_SCHOOLCD4"]          = array(7,    "",       "INT2",  "志望校第４志望");

                //高校・調査書
                } elseif ($this->field["DATADIV"] == "2") {
                    //教科
                    $class_array = array();
                    $result = $db->query(knjl310qQuery::getNameMstAll($this->ObjYear, "L008"));
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $class_array[$row["NAMECD2"]] = $row["ABBV1"];
                    }

                    $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "入試年度");
                    $checkdata["APPLICANTDIV"]          = array(1,    "HISSU",  "INT",   "入試制度");
                    $checkdata["TESTDIV0"]              = array(1,    "HISSU",  "INT",   "入試区分");
                    $checkdata["DATADIV"]               = array(1,    "HISSU",  "INT",   "データ種類");
                    $checkdata["EXAMNO"]                = array(5,    "HISSU",  "INT2",  "受験番号");
                    for ($grade = 1; $grade <= 3; $grade++) {
                        $gradename = $grade.'年生';
                        foreach ($class_array as $classcd => $label) {
                            $checkdata["CONFRPT".$classcd."_".$grade]   = array(1,    "",       "INT",   $gradename.$label);
                        }
                        $checkdata["AVG5_".$grade]          = array(3,    "",       "FLT",   $gradename."5科平均");
                        $checkdata["AVGALL_".$grade]        = array(3,    "",       "FLT",   $gradename."9科平均");
                    }
                    for ($grade = 1; $grade <= 3; $grade++) {
                        $gradename = $grade.'年生';
                        $checkdata["ABSENCE_DAYS".$grade]       = array(3,    "",       "INT",   $gradename."欠席日数");
                        $checkdata["ABSENCE_REMARK".$grade]     = array(30,   "",       "",      $gradename."欠席理由");
                    }
                    $checkdata["REMARK1"]               = array(240,  "",       "",      "特記事項");

                //高校・活動実績
                } elseif ($this->field["DATADIV"] == "3") {
                    $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "入試年度");
                    $checkdata["APPLICANTDIV"]          = array(1,    "HISSU",  "INT",   "入試制度");
                    $checkdata["TESTDIV0"]              = array(1,    "HISSU",  "INT",   "入試区分");
                    $checkdata["DATADIV"]               = array(1,    "HISSU",  "INT",   "データ種類");
                    $checkdata["EXAMNO"]                = array(5,    "HISSU",  "INT2",  "受験番号");
                    $checkdata["REMARK1"]               = array(150,  "",       "",      "活動実績１");
                    $checkdata["REMARK2"]               = array(150,  "",       "",      "活動実績２");
                    $checkdata["REMARK3"]               = array(150,  "",       "",      "活動実績３");
                    $checkdata["REMARK9"]               = array(150,  "",       "",      "特記事項");
                }
            }

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl310qQuery::deleteQueryErr();
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

                    if (SCHOOLKIND == "P") {
                        //小学・願書
                        if ($this->field["DATADIV"] == "1") {
                            $data["ENTEXAMYEAR"]            = $tmp[0];
                            $data["APPLICANTDIV"]           = $tmp[1];
                            $data["TESTDIV"]                = $tmp[2];
                            $data["DATADIV"]                = $tmp[3];
                            $data["EXAMNO"]                 = $tmp[4];
                            $data["NAME"]                   = $tmp[7];
                            $data["NAME_KANA"]              = $tmp[8];
                            $data["SEX"]                    = $tmp[9];
                            $data["BIRTHDAY"]               = $tmp[10];
                            $data["ZIPCD"]                  = $tmp[11];
                            $data["ADDRESS1"]               = $tmp[12];
                            $data["ADDRESS2"]               = $tmp[13];
                            $data["TELNO"]                  = $tmp[14];
                            $data["FS_CD"]                  = $tmp[15];
                            $data["FAMILY_REGISTER"]        = $tmp[17];
                            $data["GNAME"]                  = $tmp[18];
                            $data["GKANA"]                  = $tmp[19];
                            $data["GZIPCD"]                 = $tmp[20];
                            $data["GADDRESS1"]              = $tmp[21];
                            $data["GADDRESS2"]              = $tmp[22];
                            $data["GTELNO"]                 = $tmp[23];
                            $data["RELATIONSHIP"]           = $tmp[24];

                            $dataCnt = 26;
                        }
                    } elseif (SCHOOLKIND == "J") {
                        //中学・願書
                        if ($this->field["DATADIV"] == "1") {
                            $data["ENTEXAMYEAR"]            = $tmp[0];
                            $data["APPLICANTDIV"]           = $tmp[1];
                            $data["TESTDIV"]                = $tmp[2];
                            $data["DATADIV"]                = $tmp[3];
                            $data["EXAMNO"]                 = $tmp[4];
                            $data["NAME"]                   = $tmp[7];
                            $data["NAME_KANA"]              = $tmp[8];
                            $data["SEX"]                    = $tmp[9];
                            $data["BIRTHDAY"]               = $tmp[10];
                            $data["ZIPCD"]                  = $tmp[11];
                            $data["ADDRESS1"]               = $tmp[12];
                            $data["ADDRESS2"]               = $tmp[13];
                            $data["TELNO"]                  = $tmp[14];
                            $data["FS_CD"]                  = $tmp[15];
                            $data["FAMILY_REGISTER"]        = $tmp[17];
                            $data["BROS_INFO1"]             = $tmp[18];
                            $data["BROS_INFO2"]             = $tmp[19];
                            $data["GNAME"]                  = $tmp[20];
                            $data["GKANA"]                  = $tmp[21];
                            $data["GZIPCD"]                 = $tmp[22];
                            $data["GADDRESS1"]              = $tmp[23];
                            $data["GADDRESS2"]              = $tmp[24];
                            $data["GTELNO"]                 = $tmp[25];
                            $data["RELATIONSHIP"]           = $tmp[26];
                            $data["GJOB"]                   = $tmp[27];
                            $data["REMARK1"]                = $tmp[28];
                            $data["ABSENCE_DAYS2"]          = $tmp[29];
                            $data["ABSENCE_DAYS3"]          = $tmp[30];
                            $data["SHDIV"]                  = $tmp[31];
                            $data["RECOM_EXAMNO"]           = $tmp[32];
                            $data["SH_SCHOOLCD1"]           = $tmp[33];
                            $data["SH_SCHOOLCD2"]           = $tmp[35];
                            $data["SH_SCHOOLCD3"]           = $tmp[37];
                            $data["SH_SCHOOLCD4"]           = $tmp[39];

                            $dataCnt = 42;
                        }
                    } else {
                        //高校・願書
                        if ($this->field["DATADIV"] == "1") {
                            $data["ENTEXAMYEAR"]            = $tmp[0];
                            $data["APPLICANTDIV"]           = $tmp[1];
                            $data["TESTDIV0"]               = $tmp[2];
                            $data["DATADIV"]                = $tmp[3];
                            $data["EXAMNO"]                 = $tmp[4];
                            $data["JIZEN_BANGOU"]           = $tmp[8];
                            $data["NAME"]                   = $tmp[9];
                            $data["NAME_KANA"]              = $tmp[10];
                            $data["SEX"]                    = $tmp[11];
                            $data["BIRTHDAY"]               = $tmp[12];
                            $data["ZIPCD"]                  = $tmp[13];
                            $data["ADDRESS1"]               = $tmp[14];
                            $data["ADDRESS2"]               = $tmp[15];
                            $data["TELNO"]                  = $tmp[16];
                            $data["FS_CD"]                  = $tmp[17];
                            $data["FS_DAY"]                 = $tmp[19];
                            $data["FS_GRDDIV"]              = $tmp[20];
                            $data["GNAME"]                  = $tmp[21];
                            $data["GKANA"]                  = $tmp[22];
                            $data["GZIPCD"]                 = $tmp[23];
                            $data["GADDRESS1"]              = $tmp[24];
                            $data["GADDRESS2"]              = $tmp[25];
                            $data["GTELNO"]                 = $tmp[26];
                            $data["RELATIONSHIP"]           = $tmp[27];
                            $data["DORMITORY_FLG"]          = $tmp[28];
                            $data["SCHOLAR_KIBOU"]          = $tmp[29];
                            $startCnt = 30;
                            $addCnt = 0;
                            if ($this->field["TESTDIV0"] != "1") {
                                $data["SHDIV"]                  = $tmp[$startCnt++];
                            }
                            $data["SH_SCHOOLCD1"]           = $tmp[$startCnt + ($addCnt++ * 2)];
                            $data["SH_SCHOOLCD2"]           = $tmp[$startCnt + ($addCnt++ * 2)];
                            $data["SH_SCHOOLCD3"]           = $tmp[$startCnt + ($addCnt++ * 2)];
                            $data["SH_SCHOOLCD4"]           = $tmp[$startCnt + ($addCnt++ * 2)];

                            $dataCnt = ($this->field["TESTDIV0"] != "1") ? 40 : 39;

                        //高校・調査書
                        } elseif ($this->field["DATADIV"] == "2") {
                            $data["ENTEXAMYEAR"]            = $tmp[0];
                            $data["APPLICANTDIV"]           = $tmp[1];
                            $data["TESTDIV0"]               = $tmp[2];
                            $data["DATADIV"]                = $tmp[3];
                            $data["EXAMNO"]                 = $tmp[4];

                            $tmpCnt = 4;
                            for ($grade = 1; $grade <= 3; $grade++) {
                                foreach ($class_array as $classcd => $label) {
                                    $data["CONFRPT".$classcd."_".$grade]    = $tmp[++$tmpCnt];
                                }
                                $data["AVG5_".$grade]               = $tmp[++$tmpCnt];
                                $data["AVGALL_".$grade]             = $tmp[++$tmpCnt];
                            }
                            for ($grade = 1; $grade <= 3; $grade++) {
                                $data["ABSENCE_DAYS".$grade]        = $tmp[++$tmpCnt];
                                $data["ABSENCE_REMARK".$grade]      = $tmp[++$tmpCnt];
                            }
                            $data["REMARK1"]                = $tmp[++$tmpCnt];
                            $data[$this->lastColumn]        = $tmp[++$tmpCnt];

                        //高校・活動実績
                        } elseif ($this->field["DATADIV"] == "3") {
                            $data["ENTEXAMYEAR"]            = $tmp[0];
                            $data["APPLICANTDIV"]           = $tmp[1];
                            $data["TESTDIV0"]               = $tmp[2];
                            $data["DATADIV"]                = $tmp[3];
                            $data["EXAMNO"]                 = $tmp[4];
                            $data["REMARK1"]                = $tmp[5];
                            $data["REMARK2"]                = $tmp[6];
                            $data["REMARK3"]                = $tmp[7];
                            $data["REMARK9"]                = $tmp[8];
                            $data[$this->lastColumn]        = $tmp[9];
                        }
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
            $result = $db->query(knjl310qQuery::getEntexamNoDat($this->ObjYear, $this->field["APPLICANTDIV"]));
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
            if (SCHOOLKIND == "J" || SCHOOLKIND == "P") {
                $array = array("ENTEXAMYEAR","APPLICANTDIV","TESTDIV","DATADIV");
            } else {
                $array = array("ENTEXAMYEAR","APPLICANTDIV","TESTDIV0","DATADIV");
            }
            if (in_array($key, $array)) {
                if (($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) || ($key != "ENTEXAMYEAR" && $this->field[$key] != $data[$key])) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
            if ($key == 'SEX' && strlen($data[$key]) && 0 == $db->getOne(knjl310qQuery::nameCntSql("Z002", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if (in_array($key, array('FS_CD', 'SH_SCHOOLCD1', 'SH_SCHOOLCD2', 'SH_SCHOOLCD3', 'SH_SCHOOLCD4')) && strlen($data[$key]) && 0 == $db->getOne(knjl310qQuery::fsCdCntSql($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'FS_GRDDIV' && strlen($data[$key]) && 0 == $db->getOne(knjl310qQuery::nameCntSql("L016", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'RELATIONSHIP' && strlen($data[$key]) && 0 == $db->getOne(knjl310qQuery::nameCntSql("H201", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if (in_array($key, array('DORMITORY_FLG','SCHOLAR_KIBOU')) && strlen($data[$key]) && !in_array($data[$key], array('1', '2'))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'FAMILY_REGISTER' && strlen($data[$key]) && 0 == $db->getOne(knjl310qQuery::prefCdCntSql($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'SHDIV' && strlen($data[$key])) {
                $shdiv_err = false;
                if (0 == $db->getOne(knjl310qQuery::nameCntSql("L006", $data[$key]))) {
                    $shdiv_err = true;
                } elseif (SCHOOLKIND != "J" && $this->field["TESTDIV0"] != "3" && $data[$key] != "1") {
                    $shdiv_err = true;
                }
                if ($shdiv_err) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
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

        //受験番号範囲チェック
        $checkNumber_cnt = 0;
        $testdiv = (SCHOOLKIND == "J" || SCHOOLKIND == "P") ? "TESTDIV" : "TESTDIV0";
        if ($this->field["DATADIV"] == "1") {
            if ($data["EXAMNO"]) {
                foreach ($checkNumber as $key2 => $val2) {
                    if ($val2["EXAMNO_FROM"] <= $data["EXAMNO"] && $data["EXAMNO"] <= $val2["EXAMNO_TO"] && $this->field[$testdiv] == $val2[$testdiv]) {
                        $checkNumber_cnt++;
                    }
                }
            }
            if ($checkNumber_cnt == 0) {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "受験番号範囲不可");
            }
        }

        if (SCHOOLKIND == "H" && $this->field["DATADIV"] != "1") {
            $base_cnt = $db->getOne(knjl310qQuery::cntEntexamApplicantbaseDat($this, $data));
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
            knjl310qQuery::insertQueryErr($db, $line, $check_error);
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

        if (SCHOOLKIND == "P") {

            /******************/
            /*  小学（願書）  */
            /******************/
            if ($this->field["DATADIV"] == "1") {
                /**************/
                /*  ヘッダー  */
                /**************/
                $header["ENTEXAMYEAR"]          = "※入試年度";
                $header["APPLICANTDIV"]         = "※入試制度";
                $header["TESTDIV"]              = "※入試区分";
                $header["DATADIV"]              = "※データ種類";
                $header["EXAMNO"]               = "※受験番号";
                $header["EXAMCOURSE_NAME"]      = "コース区分";
                $header["TESTDIV_NAME"]         = "入試区分名";
                $header["NAME"]                 = "※氏名";
                $header["NAME_KANA"]            = "※ふりがな";
                $header["SEX"]                  = "※性別";
                $header["BIRTHDAY"]             = "生年月日";
                $header["ZIPCD"]                = "郵便番号";
                $header["ADDRESS1"]             = "住所";
                $header["ADDRESS2"]             = "方書";
                $header["TELNO"]                = "電話番号";
                $header["FS_CD"]                = "出身学校・園コード";
                $header["FS_NAME"]              = "出身学校・園名";
                $header["FAMILY_REGISTER"]      = "本籍";
                $header["GNAME"]                = "保護者氏名";
                $header["GKANA"]                = "保護者ふりがな";
                $header["GZIPCD"]               = "保護者郵便番号";
                $header["GADDRESS1"]            = "保護者住所";
                $header["GADDRESS2"]            = "保護者方書";
                $header["GTELNO"]               = "保護者電話番号";
                $header["RELATIONSHIP"]         = "保護者続柄";
                $header[$this->lastColumn]      = $this->lastColumn;

                /****************/
                /*  ヘッダー例  */
                /****************/

                //性別
                $sex = implode(' ', $db->getCol(knjl310qQuery::getHeaderReiNameMst($this->ObjYear, "Z002", "NAME1")));

                $yOldE = "6";  //入学年令

                $header_rei["ENTEXAMYEAR"]          = $this->ObjYear;
                $header_rei["APPLICANTDIV"]         = $this->field["APPLICANTDIV"];
                $header_rei["TESTDIV"]              = $this->field["TESTDIV"];
                $header_rei["DATADIV"]              = $this->field["DATADIV"];
                $header_rei["EXAMNO"]               = "1234";
                $header_rei["EXAMCOURSE_NAME"]      = "入力の必要なし";     //表示のみ
                $header_rei["TESTDIV_NAME"]         = "入力の必要なし";     //表示のみ
                $header_rei["NAME"]                 = "アルプ太郎";
                $header_rei["NAME_KANA"]            = "あるぷたろう";
                $header_rei["SEX"]                  = $sex;
                $header_rei["BIRTHDAY"]             = date('Y/m/d', strtotime(CTRL_DATE." -{$yOldE} years"));
                $header_rei["ZIPCD"]                = "123-4567";
                $header_rei["ADDRESS1"]             = "全角30文字";
                $header_rei["ADDRESS2"]             = "全角30文字";
                $header_rei["TELNO"]                = "12-3456-7890";
                $header_rei["FS_CD"]                = "1234567";
                $header_rei["FS_NAME"]              = "入力の必要なし";     //表示のみ
                $header_rei["FAMILY_REGISTER"]      = "12";
                $header_rei["GNAME"]                = "アルプ父";
                $header_rei["GKANA"]                = "あるぷちち";
                $header_rei["GZIPCD"]               = "123-4567";
                $header_rei["GADDRESS1"]            = "全角30文字";
                $header_rei["GADDRESS2"]            = "全角30文字";
                $header_rei["GTELNO"]               = "123-456-7890";
                $header_rei["RELATIONSHIP"]         = "12";
                $header_rei[$this->lastColumn]      = $this->lastColumn;
            }
        } elseif (SCHOOLKIND == "J") {

            /******************/
            /*  中学（願書）  */
            /******************/
            if ($this->field["DATADIV"] == "1") {
                /**************/
                /*  ヘッダー  */
                /**************/
                $header["ENTEXAMYEAR"]          = "※入試年度";
                $header["APPLICANTDIV"]         = "※入試制度";
                $header["TESTDIV"]              = "※入試区分";
                $header["DATADIV"]              = "※データ種類";
                $header["EXAMNO"]               = "※受験番号";
                $header["EXAMCOURSE_NAME"]      = "コース区分";
                $header["TESTDIV_NAME"]         = "入試区分名";
                $header["NAME"]                 = "※氏名";
                $header["NAME_KANA"]            = "※ふりがな";
                $header["SEX"]                  = "※性別";
                $header["BIRTHDAY"]             = "生年月日";
                $header["ZIPCD"]                = "郵便番号";
                $header["ADDRESS1"]             = "住所";
                $header["ADDRESS2"]             = "方書";
                $header["TELNO"]                = "電話番号";
                $header["FS_CD"]                = "出身学校コード";
                $header["FS_NAME"]              = "出身学校名";
                $header["FAMILY_REGISTER"]      = "本籍";
                $header["BROS_INFO1"]           = "兄弟情報１";
                $header["BROS_INFO2"]           = "兄弟情報２";
                $header["GNAME"]                = "保護者氏名";
                $header["GKANA"]                = "保護者ふりがな";
                $header["GZIPCD"]               = "保護者郵便番号";
                $header["GADDRESS1"]            = "保護者住所";
                $header["GADDRESS2"]            = "保護者方書";
                $header["GTELNO"]               = "保護者電話番号";
                $header["RELATIONSHIP"]         = "保護者続柄";
                $header["GJOB"]                 = "保護者職業";
                $header["REMARK1"]              = "保護者備考";
                $header["ABSENCE_DAYS2"]        = "出欠の記録5年生";
                $header["ABSENCE_DAYS3"]        = "出欠の記録6年生";
                $header["SHDIV"]                = "※専併区分";
                $header["RECOM_EXAMNO"]         = "推薦受験番号";
                $header["SH_SCHOOLCD1"]         = "志望校第１志望";
                $header["SH_SCHOOLCD1_NAME"]    = "志望校第１志望名";
                $header["SH_SCHOOLCD2"]         = "志望校第２志望";
                $header["SH_SCHOOLCD2_NAME"]    = "志望校第２志望名";
                $header["SH_SCHOOLCD3"]         = "志望校第３志望";
                $header["SH_SCHOOLCD3_NAME"]    = "志望校第３志望名";
                $header["SH_SCHOOLCD4"]         = "志望校第４志望";
                $header["SH_SCHOOLCD4_NAME"]    = "志望校第４志望名";
                $header[$this->lastColumn]      = $this->lastColumn;

                /****************/
                /*  ヘッダー例  */
                /****************/

                //性別
                $sex = implode(' ', $db->getCol(knjl310qQuery::getHeaderReiNameMst($this->ObjYear, "Z002", "NAME1")));
                //専併区分
                $shdiv = implode(' ', $db->getCol(knjl310qQuery::getHeaderReiNameMst($this->ObjYear, "L006", "NAME1")));

                $yOldE = "12";  //入学年令

                $header_rei["ENTEXAMYEAR"]          = $this->ObjYear;
                $header_rei["APPLICANTDIV"]         = $this->field["APPLICANTDIV"];
                $header_rei["TESTDIV"]              = $this->field["TESTDIV"];
                $header_rei["DATADIV"]              = $this->field["DATADIV"];
                $header_rei["EXAMNO"]               = "12345";
                $header_rei["EXAMCOURSE_NAME"]      = "入力の必要なし";     //表示のみ
                $header_rei["TESTDIV_NAME"]         = "入力の必要なし";     //表示のみ
                $header_rei["NAME"]                 = "アルプ太郎";
                $header_rei["NAME_KANA"]            = "あるぷたろう";
                $header_rei["SEX"]                  = $sex;
                $header_rei["BIRTHDAY"]             = date('Y/m/d', strtotime(CTRL_DATE." -{$yOldE} years"));
                $header_rei["ZIPCD"]                = "123-4567";
                $header_rei["ADDRESS1"]             = "全角30文字";
                $header_rei["ADDRESS2"]             = "全角30文字";
                $header_rei["TELNO"]                = "12-3456-7890";
                $header_rei["FS_CD"]                = "1234567";
                $header_rei["FS_NAME"]              = "入力の必要なし";     //表示のみ
                $header_rei["FAMILY_REGISTER"]      = "12";
                $header_rei["BROS_INFO1"]           = "アルプ兄";
                $header_rei["BROS_INFO2"]           = "アルプ姉";
                $header_rei["GNAME"]                = "アルプ父";
                $header_rei["GKANA"]                = "あるぷちち";
                $header_rei["GZIPCD"]               = "123-4567";
                $header_rei["GADDRESS1"]            = "全角30文字";
                $header_rei["GADDRESS2"]            = "全角30文字";
                $header_rei["GTELNO"]               = "123-456-7890";
                $header_rei["RELATIONSHIP"]         = "12";
                $header_rei["GJOB"]                 = "全角50文字";
                $header_rei["REMARK1"]              = "全角50文字";
                $header_rei["ABSENCE_DAYS2"]        = "123";
                $header_rei["ABSENCE_DAYS3"]        = "123";
                $header_rei["SHDIV"]                = $shdiv;
                $header_rei["RECOM_EXAMNO"]         = "12345";
                $header_rei["SH_SCHOOLCD1"]         = "1234567";
                $header_rei["SH_SCHOOLCD1_NAME"]    = "入力の必要なし";     //表示のみ
                $header_rei["SH_SCHOOLCD2"]         = "1234567";
                $header_rei["SH_SCHOOLCD2_NAME"]    = "入力の必要なし";     //表示のみ
                $header_rei["SH_SCHOOLCD3"]         = "1234567";
                $header_rei["SH_SCHOOLCD3_NAME"]    = "入力の必要なし";     //表示のみ
                $header_rei["SH_SCHOOLCD4"]         = "1234567";
                $header_rei["SH_SCHOOLCD4_NAME"]    = "入力の必要なし";     //表示のみ
                $header_rei[$this->lastColumn]      = $this->lastColumn;
            }
        } else {

            /******************/
            /*  高校（願書）  */
            /******************/
            if ($this->field["DATADIV"] == "1") {
                /**************/
                /*  ヘッダー  */
                /**************/
                $header["ENTEXAMYEAR"]          = "※入試年度";
                $header["APPLICANTDIV"]         = "※入試制度";
                $header["TESTDIV0"]             = "※入試区分";
                $header["DATADIV"]              = "※データ種類";
                $header["EXAMNO"]               = "※受験番号";
                $header["EXAMCOURSE_NAME"]      = "コース区分";
                $header["TESTDIV_NAME"]         = "入試区分２";
                $header["EXAMHALL_NAME"]        = "試験会場";
                $header["JIZEN_BANGOU"]         = "実戦模試受験番号";
                $header["NAME"]                 = "※氏名";
                $header["NAME_KANA"]            = "※ふりがな";
                $header["SEX"]                  = "※性別";
                $header["BIRTHDAY"]             = "生年月日";
                $header["ZIPCD"]                = "郵便番号";
                $header["ADDRESS1"]             = "住所";
                $header["ADDRESS2"]             = "方書";
                $header["TELNO"]                = "電話番号";
                $header["FS_CD"]                = "出身学校コード";
                $header["FS_NAME"]              = "出身学校名";
                $header["FS_DAY"]               = "卒業年月日";
                $header["FS_GRDDIV"]            = "既/卒";
                $header["GNAME"]                = "保護者氏名";
                $header["GKANA"]                = "保護者ふりがな";
                $header["GZIPCD"]               = "保護者郵便番号";
                $header["GADDRESS1"]            = "保護者住所";
                $header["GADDRESS2"]            = "保護者方書";
                $header["GTELNO"]               = "保護者電話番号";
                $header["RELATIONSHIP"]         = "保護者続柄";
                $header["DORMITORY_FLG"]        = "入寮希望";
                $header["SCHOLAR_KIBOU"]        = "スカラーシップ希望";
                if ($this->field["TESTDIV0"] != "1") {
                    $header["SHDIV"]                = "※専併区分";
                }
                $header["SH_SCHOOLCD1"]         = "志望校第１志望";
                $header["SH_SCHOOLCD1_NAME"]    = "志望校第１志望名";
                $header["SH_SCHOOLCD2"]         = "志望校第２志望";
                $header["SH_SCHOOLCD2_NAME"]    = "志望校第２志望名";
                $header["SH_SCHOOLCD3"]         = "志望校第３志望";
                $header["SH_SCHOOLCD3_NAME"]    = "志望校第３志望名";
                $header["SH_SCHOOLCD4"]         = "志望校第４志望";
                $header["SH_SCHOOLCD4_NAME"]    = "志望校第４志望名";
                $header[$this->lastColumn]      = $this->lastColumn;

                /****************/
                /*  ヘッダー例  */
                /****************/

                //性別
                $sex = implode(' ', $db->getCol(knjl310qQuery::getHeaderReiNameMst($this->ObjYear, "Z002", "NAME1")));
                //既/卒
                $fs_grddiv = implode(' ', $db->getCol(knjl310qQuery::getHeaderReiNameMst($this->ObjYear, "L016", "NAME1")));
                //専併区分
                if ($this->field["TESTDIV0"] != "1") {
                    $namecd2  = ($this->field["TESTDIV0"] == "3") ? "" : "1";
                    $shdiv = implode(' ', $db->getCol(knjl310qQuery::getHeaderReiNameMst($this->ObjYear, "L006", "NAME1", $namecd2)));
                }

                $yOldE = "15";  //入学年令

                $header_rei["ENTEXAMYEAR"]          = $this->ObjYear;
                $header_rei["APPLICANTDIV"]         = $this->field["APPLICANTDIV"];
                $header_rei["TESTDIV0"]             = $this->field["TESTDIV0"];
                $header_rei["DATADIV"]              = $this->field["DATADIV"];
                $header_rei["EXAMNO"]               = "12345";
                $header_rei["EXAMCOURSE_NAME"]      = "入力の必要なし";     //表示のみ
                $header_rei["TESTDIV_NAME"]         = "入力の必要なし";     //表示のみ
                $header_rei["EXAMHALL_NAME"]        = "入力の必要なし";     //表示のみ
                $header_rei["JIZEN_BANGOU"]         = "12345";
                $header_rei["NAME"]                 = "アルプ太郎";
                $header_rei["NAME_KANA"]            = "あるぷたろう";
                $header_rei["SEX"]                  = $sex;
                $header_rei["BIRTHDAY"]             = date('Y/m/d', strtotime(CTRL_DATE." -{$yOldE} years"));
                $header_rei["ZIPCD"]                = "123-4567";
                $header_rei["ADDRESS1"]             = "全角30文字";
                $header_rei["ADDRESS2"]             = "全角30文字";
                $header_rei["TELNO"]                = "12-3456-7890";
                $header_rei["FS_CD"]                = "1234567";
                $header_rei["FS_NAME"]              = "入力の必要なし";     //表示のみ
                $header_rei["FS_DAY"]               = $this->ObjYear."/03/01";
                $header_rei["FS_GRDDIV"]            = $fs_grddiv;
                $header_rei["GNAME"]                = "アルプ父";
                $header_rei["GKANA"]                = "あるぷちち";
                $header_rei["GZIPCD"]               = "123-4567";
                $header_rei["GADDRESS1"]            = "全角30文字";
                $header_rei["GADDRESS2"]            = "全角30文字";
                $header_rei["GTELNO"]               = "123-456-7890";
                $header_rei["RELATIONSHIP"]         = "12";
                $header_rei["DORMITORY_FLG"]        = "1:有 2:無";
                $header_rei["SCHOLAR_KIBOU"]        = "1:特別 2:一般";
                if ($this->field["TESTDIV0"] != "1") {
                    $header_rei["SHDIV"]                = $shdiv;
                }
                $header_rei["SH_SCHOOLCD1"]         = "1234567";
                $header_rei["SH_SCHOOLCD1_NAME"]    = "入力の必要なし";     //表示のみ
                $header_rei["SH_SCHOOLCD2"]         = "1234567";
                $header_rei["SH_SCHOOLCD2_NAME"]    = "入力の必要なし";     //表示のみ
                $header_rei["SH_SCHOOLCD3"]         = "1234567";
                $header_rei["SH_SCHOOLCD3_NAME"]    = "入力の必要なし";     //表示のみ
                $header_rei["SH_SCHOOLCD4"]         = "1234567";
                $header_rei["SH_SCHOOLCD4_NAME"]    = "入力の必要なし";     //表示のみ
                $header_rei[$this->lastColumn]      = $this->lastColumn;
            }

            /********************/
            /*  高校（調査書）  */
            /********************/
            if ($this->field["DATADIV"] == "2") {
                //教科
                $class_array = array();
                $result = $db->query(knjl310qQuery::getNameMstAll($this->ObjYear, "L008"));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $class_array[$row["NAMECD2"]] = $row["ABBV1"];
                }

                /**************/
                /*  ヘッダー  */
                /**************/
                $header["ENTEXAMYEAR"]          = "※入試年度";
                $header["APPLICANTDIV"]         = "※入試制度";
                $header["TESTDIV0"]             = "※入試区分";
                $header["DATADIV"]              = "※データ種類";
                $header["EXAMNO"]               = "※受験番号";
                for ($grade = 1; $grade <= 3; $grade++) {
                    $gradename = $grade.'年生';
                    foreach ($class_array as $key => $label) {
                        $header["CONFRPT".$key."_".$grade] = $gradename.$label;
                    }
                    $header["AVG5_".$grade]         = $gradename."5科平均";
                    $header["AVGALL_".$grade]      = $gradename."9科平均";
                }
                for ($grade = 1; $grade <= 3; $grade++) {
                    $gradename = $grade.'年生';
                    $header["ABSENCE_DAYS".$grade]      = $gradename."欠席日数";
                    $header["ABSENCE_REMARK".$grade]    = $gradename."欠席理由";
                }
                $header["REMARK1"]              = "特記事項";
                $header[$this->lastColumn]      = $this->lastColumn;

                /****************/
                /*  ヘッダー例  */
                /****************/
                $header_rei["ENTEXAMYEAR"]          = $this->ObjYear;
                $header_rei["APPLICANTDIV"]         = $this->field["APPLICANTDIV"];
                $header_rei["TESTDIV0"]             = $this->field["TESTDIV0"];
                $header_rei["DATADIV"]              = $this->field["DATADIV"];
                $header_rei["EXAMNO"]               = "12345";
                for ($grade = 1; $grade <= 3; $grade++) {
                    foreach ($class_array as $key => $label) {
                        $header_rei["CONFRPT".$key."_".$grade] = "1";
                    }
                    $header_rei["AVG5_".$grade]         = "1.2";
                    $header_rei["AVGALL_".$grade]      = "1.2";
                }
                for ($grade = 1; $grade <= 3; $grade++) {
                    $header_rei["ABSENCE_DAYS".$grade]      = "123";
                    $header_rei["ABSENCE_REMARK".$grade]    = "30バイトまで";
                }
                $header_rei["REMARK1"]              = "240バイトまで";
                $header_rei[$this->lastColumn]      = $this->lastColumn;
            }

            /**********************/
            /*  高校（活動実績）  */
            /**********************/
            if ($this->field["DATADIV"] == "3") {
                /**************/
                /*  ヘッダー  */
                /**************/
                $header["ENTEXAMYEAR"]          = "※入試年度";
                $header["APPLICANTDIV"]         = "※入試制度";
                $header["TESTDIV0"]             = "※入試区分";
                $header["DATADIV"]              = "※データ種類";
                $header["EXAMNO"]               = "※受験番号";
                $header["REMARK1"]              = "活動実績１";
                $header["REMARK2"]              = "活動実績２";
                $header["REMARK3"]              = "活動実績３";
                $header["REMARK9"]              = "特記事項";
                $header[$this->lastColumn]      = $this->lastColumn;

                /****************/
                /*  ヘッダー例  */
                /****************/
                $header_rei["ENTEXAMYEAR"]          = $this->ObjYear;
                $header_rei["APPLICANTDIV"]         = $this->field["APPLICANTDIV"];
                $header_rei["TESTDIV0"]             = $this->field["TESTDIV0"];
                $header_rei["DATADIV"]              = $this->field["DATADIV"];
                $header_rei["EXAMNO"]               = "12345";
                $header_rei["REMARK1"]              = "150バイトまで";
                $header_rei["REMARK2"]              = "150バイトまで";
                $header_rei["REMARK3"]              = "150バイトまで";
                $header_rei["REMARK9"]              = "150バイトまで";
                $header_rei[$this->lastColumn]      = $this->lastColumn;
            }
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
                $query = knjl310qQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"] == "4") {
                if (SCHOOLKIND == "P") {
                    //データ
                    if ($this->field["DATADIV"] == "1") {
                        //日付変換項目一覧
                        $date_array = array("BIRTHDAY");

                        //小学・願書
                        $query = knjl310qQuery::selectMainQuery3_1($this);
                    }
                } elseif (SCHOOLKIND == "J") {
                    //データ
                    if ($this->field["DATADIV"] == "1") {
                        //出身学校
                        $finInfo = array();
                        $query = knjl310qQuery::getFinschoolDistcdName();
                        $result = $db->query($query);
                        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $finInfo[$row["FINSCHOOLCD"]] = $row["FINSCHOOL_DISTCD_NAME"];
                        }
                        $result->free();

                        //日付変換項目一覧
                        $date_array = array("BIRTHDAY");

                        //中学・願書
                        $query = knjl310qQuery::selectMainQuery1_1($this);
                    }
                } else {
                    //データ
                    if ($this->field["DATADIV"] == "1") {
                        //出身学校
                        $finInfo = array();
                        $query = knjl310qQuery::getFinschoolDistcdName();
                        $result = $db->query($query);
                        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $finInfo[$row["FINSCHOOLCD"]] = $row["FINSCHOOL_DISTCD_NAME"];
                        }
                        $result->free();

                        //日付変換項目一覧
                        $date_array = array("BIRTHDAY", "FS_DAY");

                        //高校・願書
                        $query = knjl310qQuery::selectMainQuery2_1($this);
                    } elseif ($this->field["DATADIV"] == "2") {
                        //高校・調査書
                        $query = knjl310qQuery::selectMainQuery2_2($this, $class_array);
                    } elseif ($this->field["DATADIV"] == "3") {
                        //高校・活動実績
                        $query = knjl310qQuery::selectMainQuery2_3($this);
                    }
                }
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
                            //志望校名セット
                            for ($no = 1; $no <= 4; $no++) {
                                $row["SH_SCHOOLCD".$no."_NAME"] = $finInfo[$row["SH_SCHOOLCD".$no]];
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
        $AppName = $db->getOne(knjl310qQuery::getNameMst($this->ObjYear, "L003", $this->field["APPLICANTDIV"]));
        //入試区分名称
        if (SCHOOLKIND == "P") {
            $Testdiv0Name = $db->getOne(knjl310qQuery::getNameMst($this->ObjYear, "LP24", $this->field["TESTDIV"]));
        } elseif (SCHOOLKIND == "J") {
            $Testdiv0Name = $db->getOne(knjl310qQuery::getNameMst($this->ObjYear, "L024", $this->field["TESTDIV"]));
        } else {
            $Testdiv0Name = $db->getOne(knjl310qQuery::getNameMst($this->ObjYear, "L045", $this->field["TESTDIV0"]));
        }
        //データ種類
        $datadiv = array("1" => "願書", "2" => "調査書", "3" => "活動実績");
        $DataDivName = $datadiv[$this->field["DATADIV"]];
        //ファイル名
        $title = $this->ObjYear.'年度 '.$AppName.' '.$Testdiv0Name.$DataDivName.'データ';

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
