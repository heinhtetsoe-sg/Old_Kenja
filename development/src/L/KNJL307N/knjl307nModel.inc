<?php

require_once('for_php7.php');
class knjl307nModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control; //コントロールマスタ
    public $staffcd; //登録者コード
    public $fieldSize = array();
    public $test_year;
    public $lastColumn;//CSVの最後の列の値

    public function init()
    {
        common::GetControlMaster_Fnc($this->control);

        $this->field = array("OUTPUT"        =>  VARS::post("OUTPUT"),        //出力取込種別
                             "APPLICANTDIV"  =>  VARS::post("APPLICANTDIV"),  //入試制度
                             "TESTDIV"       =>  VARS::post("TESTDIV"),       //入試区分
                             "TOTALCD"       =>  VARS::post("TOTALCD"),       //志望区分
                             "DATADIV"       =>  VARS::post("DATADIV"),       //データ種類
                             "HEADER"        =>  VARS::post("HEADER"),        //ヘッダ有
                             "SHORI_MEI"     =>  VARS::post("SHORI_MEI")      //処理名
                             );

        $this->test_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
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

    /***************/
    /* CSVの取込み */
    /***************/
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            if ($this->field["SHORI_MEI"] == "1") { //1:更新 2:削除
                if ($this->field["DATADIV"] == "1") {
                    //願書
                    $cnt = knjl307nQuery::updateQueryCsv($this, $this->data_arr);
                } else {
                    //調査書
                    $cnt = knjl307nQuery::updateQueryCsv2($this, $this->data_arr);
                }
            } else {
//                $cnt = knjl307nQuery::DeleteQueryCsv($this,$this->data_arr);
            }
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_DAT");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    /***********************/
    /* CSVファイルを配列へ */
    /***********************/
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            if ($this->field["DATADIV"] == "1") {
                //願書
                $checkdata =  array("RECEPTDATE"       => array(10,  "※受付日"),
                                    "REMARK9"          => array(45,   "内諾"),
                                    "EXAMNO"           => array(4,   "※受験番号"),
                                    "NAME"             => array(60,  "※氏名"),
                                    "NAME_KANA"        => array(120, "※氏名かな"),
                                    "SHDIV"            => array(1,   "※専併"),
                                    "SEX"              => array(1,   "※性別"),
                                    "BIRTHDAY"         => array(10,  "生年月日"),
                                    "ZIPCD"            => array(8,   "郵便番号"),
                                    "ADDRESS1"         => array(90,  "現住所1"),
                                    "ADDRESS2"         => array(90,  "現住所2"),
                                    "TELNO"            => array(14,  "電話番号"),
                                    "FS_CD"            => array(7,   "出身中学校コード"),
                                    "FINSCHOOL_NAME"   => array(120, "出身中学校名"),//表示のみ
                                    "FS_DAY"           => array(10,  "卒業年月日"),
                                    "FS_GRDDIV"        => array(1,   "既/卒"),
                                    "GNAME"            => array(60,  "保護者氏名"),
                                    "GKANA"            => array(120, "保護者かな"),
                                    "GZIPCD"           => array(8,   "保護者郵便番号"),
                                    "GADDRESS1"        => array(90,  "保護者現住所1"),
                                    "GADDRESS2"        => array(90,  "保護者現住所2"),
                                    "GTELNO"           => array(14,  "保護者電話番号"),
                                    "EXAMCOURSECD2"    => array(4,   "第2志望コースコード"),
                                    "EXAMCOURSE_NAME2" => array(90,  "第2志望コース名"),//表示のみ
                                    "REMARK7"          => array(1,  "備考1-奨学生"),
                                    "REMARK1"          => array(60,  "備考1"),
                                    "REMARK2"          => array(75,  "備考2"),
                                    "REMARK3"          => array(75,  "備考3"),
                                    "REMARK4"          => array(75,  "備考4"),
                                    "REMARK8"          => array(1,  "備考5-奨学生"),
                                    "REMARK5"          => array(60,  "備考5"),
                                    "REMARK6"          => array(75,  "備考6")
                                    );
            } else {
                //調査書
                $checkdata =  array("EXAMNO"                    => array(4,   "※受験番号"),
                                    "CONFIDENTIAL_RPT01"        => array(2,  "国語"),
                                    "CONFIDENTIAL_RPT02"        => array(2,  "社会"),
                                    "CONFIDENTIAL_RPT03"        => array(2,  "数学"),
                                    "CONFIDENTIAL_RPT04"        => array(2,  "理科"),
                                    "CONFIDENTIAL_RPT05"        => array(2,  "音楽"),
                                    "CONFIDENTIAL_RPT06"        => array(2,  "美術"),
                                    "CONFIDENTIAL_RPT07"        => array(2,  "保体"),
                                    "CONFIDENTIAL_RPT08"        => array(2,  "技家"),
                                    "CONFIDENTIAL_RPT09"        => array(2,  "外国"),
                                    "TOTAL_ALL"                 => array(3,  "評定合計"),
                                    "TOTAL5"                    => array(3,  "5科目評定合計"),
                                    "KASANTEN_ALL"              => array(3,  "段階"),
                                    "ABSENCE_DAYS"              => array(3,  "欠席日数（1年）"),
                                    "ABSENCE_DAYS2"             => array(3,  "欠席日数（2年）"),
                                    "ABSENCE_DAYS3"             => array(3,  "欠席日数（3年）"),
                                    "ABSENCE_REMARK"            => array(90, "主な欠席理由（1年）"),
                                    "ABSENCE_REMARK2"           => array(90, "主な欠席理由（2年）"),
                                    "ABSENCE_REMARK3"           => array(90, "主な欠席理由（3年）"),
                                    "REMARK1_004"               => array(60,  "クラブ"),
                                    "TOKU_REMARK1"              => array(240,"特活・部活・特記事項要約")
                                    );
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl307nQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        if ($line == 2) {//headerが2行分
                            $header = true;
                        }
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);
                    if ($this->field["DATADIV"] == "1" && $checkcount != 33) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    } elseif ($this->field["DATADIV"] == "2" && $checkcount != 22) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    if ($this->field["DATADIV"] == "1") {
                        //願書
                        $data = array("RECEPTDATE"       => $tmp[0],
                                      "REMARK9"          => $tmp[1],
                                      "EXAMNO"           => (strlen(trim($tmp[2])))  ? sprintf("%04d", $tmp[2]) : "",
                                      "NAME"             => $tmp[3],
                                      "NAME_KANA"        => $tmp[4],
                                      "SHDIV"            => (strlen(trim($tmp[5])))  ? sprintf("%01d", $tmp[5]) : "",
                                      "SEX"              => (strlen(trim($tmp[6])))  ? sprintf("%01d", $tmp[6]) : "",
                                      "BIRTHDAY"         => $tmp[7],
                                      "ZIPCD"            => $tmp[8],
                                      "ADDRESS1"         => $tmp[9],
                                      "ADDRESS2"         => $tmp[10],
                                      "TELNO"            => $tmp[11],
                                      "FS_CD"            => (strlen(trim($tmp[12])))  ? sprintf("%07d", $tmp[12]) : "",
                                      "FINSCHOOL_NAME"   => $tmp[13],
                                      "FS_DAY"           => $tmp[14],
                                      "FS_GRDDIV"        => (strlen(trim($tmp[15])))  ? sprintf("%01d", $tmp[15]) : "",
                                      "GNAME"            => $tmp[16],
                                      "GKANA"            => $tmp[17],
                                      "GZIPCD"           => $tmp[18],
                                      "GADDRESS1"        => $tmp[19],
                                      "GADDRESS2"        => $tmp[20],
                                      "GTELNO"           => $tmp[21],
                                      "EXAMCOURSECD2"    => (strlen(trim($tmp[22])))  ? sprintf("%04d", $tmp[22]) : "",
                                      "EXAMCOURSE_NAME2" => $tmp[23],
                                      "REMARK7"          => $tmp[24],
                                      "REMARK1"          => $tmp[25],
                                      "REMARK2"          => $tmp[26],
                                      "REMARK3"          => $tmp[27],
                                      "REMARK4"          => $tmp[28],
                                      "REMARK8"          => $tmp[29],
                                      "REMARK5"          => $tmp[30],
                                      "REMARK6"          => $tmp[31]
                                      );
                    } else {
                        //調査書
                        $data = array("EXAMNO"           => (strlen(trim($tmp[0])))  ? sprintf("%04d", $tmp[0]) : "",
                                      "CONFIDENTIAL_RPT01"      => $tmp[1],
                                      "CONFIDENTIAL_RPT02"      => $tmp[2],
                                      "CONFIDENTIAL_RPT03"      => $tmp[3],
                                      "CONFIDENTIAL_RPT04"      => $tmp[4],
                                      "CONFIDENTIAL_RPT05"      => $tmp[5],
                                      "CONFIDENTIAL_RPT06"      => $tmp[6],
                                      "CONFIDENTIAL_RPT07"      => $tmp[7],
                                      "CONFIDENTIAL_RPT08"      => $tmp[8],
                                      "CONFIDENTIAL_RPT09"      => $tmp[9],
                                      "TOTAL_ALL"               => $tmp[10],
                                      "TOTAL5"                  => $tmp[11],
                                      "KASANTEN_ALL"            => $tmp[12],
                                      "ABSENCE_DAYS"            => $tmp[13],
                                      "ABSENCE_DAYS2"           => $tmp[14],
                                      "ABSENCE_DAYS3"           => $tmp[15],
                                      "ABSENCE_REMARK"          => $tmp[16],
                                      "ABSENCE_REMARK2"         => $tmp[17],
                                      "ABSENCE_REMARK3"         => $tmp[18],
                                      "REMARK1_004"             => $tmp[19],
                                      "TOKU_REMARK1"            => $tmp[20]
                                      );
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

    /*****************/
    /*エラーチェック */
    /*****************/
    public function errCheck($db, $data, $checkdata, $line)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $jufukuErrFlg    = false;    //重複エラーフラグ
        $shdivErrFlg     = false;    //専併区分エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $jufukuErrMsg    = "";       //重複エラーメッセージ
        $shdivErrMsg     = "";       //専併区分エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $jufukuSep    = "";
        $shdivSep     = "";

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if ($this->field["DATADIV"] == "1" && in_array($key, array("RECEPTDATE","EXAMNO","NAME","NAME_KANA","SHDIV","SEX"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            if ($this->field["DATADIV"] == "2" && in_array($key, array("EXAMNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //存在チェック
            if ($this->field["DATADIV"] == "1" && $this->field["SHORI_MEI"] == "1") { //1:更新 2:削除
                if ($key == 'REMARK9' && strlen($data[$key]) && 0 == $db->getOne(knjl307nQuery::nameCntSql("L026", $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'SHDIV' && strlen($data[$key]) && 0 == $db->getOne(knjl307nQuery::nameCntSql("L006", $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'SEX' && strlen($data[$key]) && 0 == $db->getOne(knjl307nQuery::nameCntSql("Z002", $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'FS_CD' && strlen($data[$key]) && 0 == $db->getOne(knjl307nQuery::fsCdCntSql($data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'FS_GRDDIV' && strlen($data[$key]) && 0 == $db->getOne(knjl307nQuery::nameCntSql("L016", $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'REMARK7' && strlen($data[$key]) && 0 == $db->getOne(knjl307nQuery::nameCntSql("L025", $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'REMARK8' && strlen($data[$key]) && 0 == $db->getOne(knjl307nQuery::nameCntSql("L025", $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'EXAMCOURSECD2' && strlen($data[$key]) && 0 == $db->getOne(knjl307nQuery::examcourseCdCntSql($this, $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'EXAMCOURSECD2' && strlen($data[$key]) && substr($this->field["TOTALCD"], 4, 7) == $data[$key]) {
                    $returnFlg = $this->errSet($jufukuErrFlg, $jufukuErrMsg, "※重複:", $jufukuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("EXAMNO", "TOTAL_ALL", "TOTAL5", "KASANTEN_ALL", "CONFIDENTIAL_RPT01", "CONFIDENTIAL_RPT02", "CONFIDENTIAL_RPT03", "CONFIDENTIAL_RPT04", "CONFIDENTIAL_RPT05", "CONFIDENTIAL_RPT06", "CONFIDENTIAL_RPT07", "CONFIDENTIAL_RPT08", "CONFIDENTIAL_RPT09", "ABSENCE_DAYS", "ABSENCE_DAYS2", "ABSENCE_DAYS3"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") { //1:更新 2:削除
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }

            //専併区分チェック
            //取込時、入試区分「2：1次入試Ｂ日程」の場合、専併区分「3：併願」固定。それ以外の場合はエラー
            if ($this->field["DATADIV"] == "1" && $this->field["TESTDIV"] == "2") {
                if ($key == 'SHDIV' && strlen($data[$key]) && $data[$key] != "3") {
                    $returnFlg = $this->errSet($shdivErrFlg, $shdivErrMsg, "※入試区分「2：1次入試Ｂ日程」の場合、専併区分「3：併願」固定です。", $shdivSep, "");
                }
            }
        }

        if ($this->field["DATADIV"] == "2") {
            $base_cnt = $db->getOne(knjl307nQuery::cntEntexamApplicantbaseDat($this, $data));
            //存在チェック -- ENTEXAM_APPLICANTBASE_DAT
            if ($base_cnt == 0) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, "願書データ");
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$jufukuErrMsg.$shdivErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl307nQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    /****************************/
    /* エラーメッセージをセット */
    /****************************/
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

    /**************************/
    /* データの整合性チェック */
    /**************************/
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    /**************/
    /* ＣＳＶ出力 */
    /**************/
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        if ($this->field["DATADIV"] == "1") {
            //願書
            $header =     array("RECEPTDATE"        => "※受付日",
                                "REMARK9"           => "内諾",
                                "EXAMNO"            => "※受験番号",
                                "NAME"              => "※氏名",
                                "NAME_KANA"         => "※氏名かな",
                                "SHDIV"             => "※専併",
                                "SEX"               => "※性別",
                                "BIRTHDAY"          => "生年月日",
                                "ZIPCD"             => "郵便番号",
                                "ADDRESS1"          => "現住所1",
                                "ADDRESS2"          => "現住所2",
                                "TELNO"             => "電話番号",
                                "FS_CD"             => "出身中学校コード",
                                "FINSCHOOL_NAME"    => "出身中学校名",//表示のみ
                                "FS_DAY"            => "卒業年月日",
                                "FS_GRDDIV"         => "既/卒",
                                "GNAME"             => "保護者氏名",
                                "GKANA"             => "保護者かな",
                                "GZIPCD"            => "保護者郵便番号",
                                "GADDRESS1"         => "保護者現住所1",
                                "GADDRESS2"         => "保護者現住所2",
                                "GTELNO"            => "保護者電話番号",
                                "EXAMCOURSECD2"     => "第2志望コースコード",
                                "EXAMCOURSE_NAME2"  => "第2志望コース名",//表示のみ
                                "REMARK7"           => "備考1-奨学生",
                                "REMARK1"           => "備考1",
                                "REMARK2"           => "備考2",
                                "REMARK3"           => "備考3",
                                "REMARK4"           => "備考4",
                                "REMARK8"           => "備考5-奨学生",
                                "REMARK5"           => "備考5",
                                "REMARK6"           => "備考6",
                                $this->lastColumn   => $this->lastColumn
                                );

            $header_rei = array("RECEPTDATE"        => "2015/10/01",
                                "REMARK9"           => "1",
                                "EXAMNO"            => "1234",
                                "NAME"              => "アルプ太郎",
                                "NAME_KANA"         => "あるぷたろう",
                                "SHDIV"             => "1",
                                "SEX"               => "1",
                                "BIRTHDAY"          => "2000/02/01",
                                "ZIPCD"             => "123-4567",
                                "ADDRESS1"          => "東京都",
                                "ADDRESS2"          => "",
                                "TELNO"             => "12-3456-7890",
                                "FS_CD"             => "1234567",
                                "FINSCHOOL_NAME"    => "入力の必要なし", //表示のみ
                                "FS_DAY"            => "2016/03/01",
                                "FS_GRDDIV"         => "1",
                                "GNAME"             => "アルプ父",
                                "GKANA"             => "あるぷちち",
                                "GZIPCD"            => "123-4567",
                                "GADDRESS1"         => "東京都",
                                "GADDRESS2"         => "",
                                "GTELNO"            => "123-456-7890",
                                "EXAMCOURSECD2"     => "1234",
                                "EXAMCOURSE_NAME2"  => "入力の必要なし",//表示のみ
                                "REMARK7"           => "1",
                                "REMARK1"           => "備考1",
                                "REMARK2"           => "備考2",
                                "REMARK3"           => "備考3",
                                "REMARK4"           => "備考4",
                                "REMARK8"           => "2",
                                "REMARK5"           => "備考5",
                                "REMARK6"           => "備考6",
                                $this->lastColumn   => $this->lastColumn
                                );
        } elseif ($this->field["DATADIV"] == "2") {
            //調査書
            $header =     array("EXAMNO"                    => "※受験番号",
                                "CONFIDENTIAL_RPT01"        => "国語",
                                "CONFIDENTIAL_RPT02"        => "社会",
                                "CONFIDENTIAL_RPT03"        => "数学",
                                "CONFIDENTIAL_RPT04"        => "理科",
                                "CONFIDENTIAL_RPT05"        => "音楽",
                                "CONFIDENTIAL_RPT06"        => "美術",
                                "CONFIDENTIAL_RPT07"        => "保体",
                                "CONFIDENTIAL_RPT08"        => "技家",
                                "CONFIDENTIAL_RPT09"        => "外国",
                                "TOTAL_ALL"                 => "評定合計",
                                "TOTAL5"                    => "5科目評定合計",
                                "KASANTEN_ALL"              => "段階",
                                "ABSENCE_DAYS"              => "欠席日数（1年）",
                                "ABSENCE_DAYS2"             => "欠席日数（2年）",
                                "ABSENCE_DAYS3"             => "欠席日数（3年）",
                                "ABSENCE_REMARK"            => "主な欠席理由（1年）",
                                "ABSENCE_REMARK2"           => "主な欠席理由（2年）",
                                "ABSENCE_REMARK3"           => "主な欠席理由（3年）",
                                "REMARK1_004"               => "クラブ",
                                "TOKU_REMARK1"              => "特活・部活・特記事項要約",
                                $this->lastColumn           => $this->lastColumn
                                );

            $header_rei = array("EXAMNO"                    => "1234",
                                "CONFIDENTIAL_RPT01"        => "5",
                                "CONFIDENTIAL_RPT02"        => "4",
                                "CONFIDENTIAL_RPT03"        => "5",
                                "CONFIDENTIAL_RPT04"        => "3",
                                "CONFIDENTIAL_RPT05"        => "5",
                                "CONFIDENTIAL_RPT06"        => "5",
                                "CONFIDENTIAL_RPT07"        => "4",
                                "CONFIDENTIAL_RPT08"        => "3",
                                "CONFIDENTIAL_RPT09"        => "5",
                                "TOTAL_ALL"                 => "39",
                                "TOTAL5"                    => "22",
                                "KASANTEN_ALL"              => "5",
                                "ABSENCE_DAYS"              => "2",
                                "ABSENCE_DAYS2"             => "13",
                                "ABSENCE_DAYS3"             => "4",
                                "ABSENCE_REMARK"            => "特になし",
                                "ABSENCE_REMARK2"           => "入院治療の為",
                                "ABSENCE_REMARK3"           => "特になし",
                                "REMARK1_004"               => "クラブ",
                                "TOKU_REMARK1"              => "全日本代表に選出",
                                $this->lastColumn           => $this->lastColumn
                                );
        } else {
            //DB接続
            $db = Query::dbCheckOut();

            //科目取得
            $this->testSubClassCd = array();
            $query = knjl307nQuery::getTestSubclasscd($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->testSubClassCd[$row["VALUE"]] = $row["LABEL"];
            }

            //願書＋調査書（出力のみの処理）
            $header["TOTALCD"]              = "志望コースコード";
            $header["TOTALCD_NAME"]         = "志望コース名";
            $header["SUC_COURSECODE"]       = "合格コース";
            $header["JUDGEMENT_3"]          = "まわし合格";
            $header["JUDGEMENT"]            = "合否";
            $header["EXAMNO"]               = "※受験番号";
            $header["NAME"]                 = "※氏名";
            $header["NAME_KANA"]            = "※氏名かな";
            $header["SHDIV"]                = "※専併";
            $header["SHDIV_NAME"]           = "※専併";
            $header["SEX"]                  = "※性別";
            $header["RECEPTDATE"]           = "※受付日";
            $header["REMARK9"]              = "内諾";
            $header["REMARK9_NAME"]         = "内諾";
            $header["BIRTH_WAREKI"]         = "生年月日（和暦）";
            $header["BIRTH_YEAR"]           = "年";
            $header["BIRTH_MANTH"]          = "月";
            $header["BIRTH_DAY"]            = "日";
            $header["BIRTHDAY"]             = "生年月日";
            $header["ZIPCD"]                = "郵便番号";
            $header["ADDRESS1"]             = "現住所1";
            $header["ADDRESS2"]             = "現住所2";
            $header["TELNO"]                = "電話番号";
            $header["FS_CD"]                = "出身中学校コード";
            $header["FINSCHOOL_NAME"]       = "出身中学校名";//表示のみ
            $header["FS_DAY"]               = "卒業年月日";
            $header["FS_GRDDIV"]            = "既/卒";
            $header["GNAME"]                = "保護者氏名";
            $header["GKANA"]                = "保護者かな";
            $header["GZIPCD"]               = "保護者郵便番号";
            $header["GADDRESS1"]            = "保護者現住所1";
            $header["GADDRESS2"]            = "保護者現住所2";
            $header["GTELNO"]               = "保護者電話番号";
            $header["EXAMCOURSECD2"]        = "第2志望コースコード";
            $header["EXAMCOURSE_NAME2"]     = "第2志望コース名";//表示のみ
            $header["REMARK7"]              = "備考1-奨学生";
            $header["REMARK7_NAME"]         = "備考1-奨学生";
            $header["REMARK1"]              = "備考1";
            $header["REMARK2"]              = "備考2";
            $header["REMARK3"]              = "備考3";
            $header["REMARK4"]              = "備考4";
            $header["REMARK8"]              = "備考5-奨学生";
            $header["REMARK8_NAME"]         = "備考5-奨学生";
            $header["REMARK5"]              = "備考5";
            $header["REMARK6"]              = "備考6";
            //調査書
            $header["EXAMNO2"]              = "※受験番号";
            $header["CONFIDENTIAL_RPT01"]   = "国語";
            $header["CONFIDENTIAL_RPT02"]   = "社会";
            $header["CONFIDENTIAL_RPT03"]   = "数学";
            $header["CONFIDENTIAL_RPT04"]   = "理科";
            $header["CONFIDENTIAL_RPT05"]   = "音楽";
            $header["CONFIDENTIAL_RPT06"]   = "美術";
            $header["CONFIDENTIAL_RPT07"]   = "保体";
            $header["CONFIDENTIAL_RPT08"]   = "技家";
            $header["CONFIDENTIAL_RPT09"]   = "外国";
            $header["TOTAL_ALL"]            = "評定合計";
            $header["TOTAL5"]               = "5科目評定合計";
            $header["KASANTEN_ALL"]         = "段階";
            $header["ABSENCE_DAYS"]         = "欠席日数（1年）";
            $header["ABSENCE_DAYS2"]        = "欠席日数（2年）";
            $header["ABSENCE_DAYS3"]        = "欠席日数（3年）";
            $header["ABSENCE_REMARK"]       = "主な欠席理由（1年）";
            $header["ABSENCE_REMARK2"]      = "主な欠席理由（2年）";
            $header["ABSENCE_REMARK3"]      = "主な欠席理由（3年）";
            $header["REMARK1_004"]          = "クラブ";
            $header["TOKU_REMARK1"]         = "特活・部活・特記事項要約";
            $header["EXAMNO3"]              = "受験番号";
            $header["E_NAME"]               = "氏名";
            $header["E_TOTAL"]              = "合計";
            foreach ($this->testSubClassCd as $key => $testName) {
                $header["EXAM_SCORE".$key]              = $testName;
            }
            $header[$this->lastColumn]      = $this->lastColumn;

            //DB切断
            Query::dbCheckIn($db);
        }

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ

        //タイトル
        $db = Query::dbCheckOut();
        //和暦年度
        $getWarekiNendo = to_wareki($this->test_year, "04", "01");
        //入試制度
        $getApplicantdivName = $db->getOne(knjl307nQuery::getApplicantdiv($this, "GET"));
        //入試区分
        $getTestDivName = $db->getOne(knjl307nQuery::getTestDiv($this, "GET"));
        //志望区分
        if ($this->field["TOTALCD"] == "9999") {
            $getCourseName = "全コース";
        } else {
            $getCourseName = $db->getOne(knjl307nQuery::getEntExamCourse($this, "GET"));
        }
        //年
        $getYearWareki = to_wareki(date("Y", time()), "04", "01");
        //日付
        $getDateWareki = date("n月j日", time());
        //時間
        $getTimeWareki = date("g時i分", time());
        Query::dbCheckIn($db);

        if ($this->field["OUTPUT"]=="1") {
            // ヘッダ出力
            $header_data = true;
            $contents .= $getWarekiNendo.'度　'.$getTestDivName.$getApplicantdivName.$getCourseName.'入学試験志願者チェックリスト'.'　　　'.$getYearWareki.$getDateWareki.$getTimeWareki.'現在'."\n";
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {
            // エラー出力
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4") {
            // データ出力
            if ($this->field["HEADER"]=="on") {
                $contents .= $getWarekiNendo.'度　'.$getTestDivName.$getApplicantdivName.$getCourseName.'入学試験志願者チェックリスト'.'　　　'.$getYearWareki.$getDateWareki.$getTimeWareki.'現在'."\n";
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = knjl307nQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                if ($this->field["DATADIV"] == "3") {
                    $query = knjl307nQuery::getCsvData3($this);
                } else {
                    $query = knjl307nQuery::selectMainQuery($this);
                }
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    if ($this->field["DATADIV"] == "1") { //願書
                        $row["BIRTHDAY"] = str_replace("-", "/", $row["BIRTHDAY"]);
                        $row["FS_DAY"]   = str_replace("-", "/", $row["FS_DAY"]);
                    }
                    if ($this->field["DATADIV"] == "3") { //願書＋調査書
                        $row["BIRTH_WAREKI"] = ($row["BIRTH_WAREKI"] != "") ? to_wareki2($row["BIRTH_YEAR"], $row["BIRTH_MANTH"], $row["BIRTH_DAY"], "warekiName"): "";
                        $row["BIRTH_YEAR"]   = ($row["BIRTH_YEAR"] != "")   ? to_wareki2($row["BIRTH_YEAR"], $row["BIRTH_MANTH"], $row["BIRTH_DAY"]): "";
                        $row["BIRTHDAY"]     = str_replace("-", "/", $row["BIRTHDAY"]);
                        $row["FS_DAY"]       = str_replace("-", "/", $row["FS_DAY"]);
                        foreach ($this->testSubClassCd as $key => $testName) {
                            $row["E_TOTAL"] = $row["E_TOTAL"] + $row["EXAM_SCORE".$key];
                        }
                    }
                    $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode(",", $row) .",".$this->lastColumn."\n";
                $header_data = true;
            }

            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                if ($this->field["DATADIV"] == "1") {
                    $filename = sprintf("%s.csv", "願書データエラー出力");
                } else {
                    $filename = sprintf("%s.csv", "調査書データエラー出力");
                }
            } else {                                //エラー以外
                if ($this->field["DATADIV"] == "1") {
                    $filename = sprintf("%s.csv", "願書データ");
                } elseif ($this->field["DATADIV"] == "2") {
                    $filename = sprintf("%s.csv", "調査書データ");
                } else {
                    $filename = sprintf("%s.csv", "願書＋調査書データ");
                }
            }
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}

//和暦変換用の関数
function to_wareki($y, $m, $d)
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
    $wareki = "{$gg}{$yy}年";

    return $wareki;
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
