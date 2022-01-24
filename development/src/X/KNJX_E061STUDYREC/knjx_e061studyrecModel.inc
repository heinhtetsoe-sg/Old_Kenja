<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx_e061studyrecModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx_e061studyrecModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    public function init()
    {
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "GRADE"            =>  VARS::post("GRADE"),            //学年一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組一覧
                             "STUDENT"          =>  VARS::post("STUDENT"),          //生徒一覧
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd          = VARS::request("cmd");
        $this->file = VARS::file("FILE");

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
            $errFlg = $this->checkCsvErrQuery('KNJX_E061M');
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx_e061studyrecQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_e061studyrecQuery::deleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            if ($this->Properties["useCurriculumcd"] == '1') {
                $checkdata = array("SCHOOLCD"          => array(1,"本校区分"),
                                   "YEAR"              => array(4,"年度"),
                                   "SCHREGNO"          => array(8,"学籍番号"),
                                   "ANNUAL"            => array(2,"履修学年"),
                                   "CLASSCD"           => array(2,"教科コード"),
                                   "SCHOOL_KIND"       => array(1,"学校種別"),
                                   "CURRICULUM_CD"     => array(1,"教育課程コード"),
                                   "SUBCLASSCD"        => array(6,"科目コード"),
                                   "CLASSNAME"         => array(30,"教科名"),
                                   "CLASSABBV"         => array(15,"教科略称"),
                                   "CLASSNAME_ENG"     => array(40,"教科名英字"),
                                   "CLASSABBV_ENG"     => array(30,"教科略称英字"),
                                   "SUBCLASSNAME"      => array(90,"科目名"),
                                   "SUBCLASSABBV"      => array(90,"科目略称"),
                                   "SUBCLASSNAME_ENG"  => array(40,"科目名英字"),
                                   "SUBCLASSABBV_ENG"  => array(20,"科目略称英字"),
                                   "VALUATION"         => array(2,"評価"),
                                   "GET_CREDIT"        => array(2,"標準修得単位"),
                                   "ADD_CREDIT"        => array(2,"増加修得単位"),
                                   "COMP_CREDIT"       => array(2,"履修単位"),
                                   "REMARK"            => array(150,"履修科目備考")
                                   );
            } else {
                $checkdata = array("SCHOOLCD"          => array(1,"本校区分"),
                                   "YEAR"              => array(4,"年度"),
                                   "SCHREGNO"          => array(8,"学籍番号"),
                                   "ANNUAL"            => array(2,"履修学年"),
                                   "CLASSCD"           => array(2,"教科コード"),
                                   "SUBCLASSCD"        => array(6,"科目コード"),
                                   "CLASSNAME"         => array(30,"教科名"),
                                   "CLASSABBV"         => array(15,"教科略称"),
                                   "CLASSNAME_ENG"     => array(40,"教科名英字"),
                                   "CLASSABBV_ENG"     => array(30,"教科略称英字"),
                                   "SUBCLASSNAME"      => array(90,"科目名"),
                                   "SUBCLASSABBV"      => array(90,"科目略称"),
                                   "SUBCLASSNAME_ENG"  => array(40,"科目名英字"),
                                   "SUBCLASSABBV_ENG"  => array(20,"科目略称英字"),
                                   "VALUATION"         => array(2,"評価"),
                                   "GET_CREDIT"        => array(2,"標準修得単位"),
                                   "ADD_CREDIT"        => array(2,"増加修得単位"),
                                   "COMP_CREDIT"       => array(2,"履修単位"),
                                   "REMARK"            => array(150,"履修科目備考")
                                   );
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr('KNJX_E061M');
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $primaryKey2   = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $primaryKey1   = $this->getPrimaryKey('SCHREG_STUDYREC_DAT');
            $primaryKey2   = $this->getPrimaryKey('STUDYRECREMARK_DAT');
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

                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $data = array("SCHOOLCD"         => $tmp[0],
                                      "YEAR"             => $tmp[8],
                                      "SCHREGNO"         => strlen(trim($tmp[4]))  ? sprintf("%08d", $tmp[4])  : "",
                                      "ANNUAL"           => strlen(trim($tmp[9]))  ? sprintf("%02d", $tmp[9])  : "",
                                      "CLASSCD"          => strlen(trim($tmp[14])) ? sprintf("%02d", $tmp[14]) : "",
                                      "SCHOOL_KIND"      => $tmp[15],
                                      "CURRICULUM_CD"    => $tmp[16],
                                      "SUBCLASSCD"       => strlen(trim($tmp[21])) ? sprintf("%06d", $tmp[21]) : "",
                                      "CLASSNAME"        => $tmp[17],
                                      "CLASSABBV"        => $tmp[18],
                                      "CLASSNAME_ENG"    => $tmp[19],
                                      "CLASSABBV_ENG"    => $tmp[20],
                                      "SUBCLASSNAME"     => $tmp[22],
                                      "SUBCLASSABBV"     => $tmp[23],
                                      "SUBCLASSNAME_ENG" => $tmp[24],
                                      "SUBCLASSABBV_ENG" => $tmp[25],
                                      "VALUATION"        => $tmp[26],
                                      "GET_CREDIT"       => $tmp[27],
                                      "ADD_CREDIT"       => $tmp[28],
                                      "COMP_CREDIT"      => $tmp[29],
                                      "REMARK"           => $tmp[30]
                                      );
                    } else {
                        $data = array("SCHOOLCD"         => $tmp[0],
                                      "YEAR"             => $tmp[8],
                                      "SCHREGNO"         => strlen(trim($tmp[4]))  ? sprintf("%08d", $tmp[4])  : "",
                                      "ANNUAL"           => strlen(trim($tmp[9]))  ? sprintf("%02d", $tmp[9])  : "",
                                      "CLASSCD"          => strlen(trim($tmp[14])) ? sprintf("%02d", $tmp[14]) : "",
                                      "SUBCLASSCD"       => strlen(trim($tmp[19])) ? sprintf("%06d", $tmp[19]) : "",
                                      "CLASSNAME"        => $tmp[15],
                                      "CLASSABBV"        => $tmp[16],
                                      "CLASSNAME_ENG"    => $tmp[17],
                                      "CLASSABBV_ENG"    => $tmp[18],
                                      "SUBCLASSNAME"     => $tmp[20],
                                      "SUBCLASSABBV"     => $tmp[21],
                                      "SUBCLASSNAME_ENG" => $tmp[22],
                                      "SUBCLASSABBV_ENG" => $tmp[23],
                                      "VALUATION"        => $tmp[24],
                                      "GET_CREDIT"       => $tmp[25],
                                      "ADD_CREDIT"       => $tmp[26],
                                      "COMP_CREDIT"      => $tmp[27],
                                      "REMARK"           => $tmp[28]
                                      );
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
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
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $valuesErrFlg       = false;    //値不正エラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $valuesErrMsg       = "";       //値不正エラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $valuesSep       = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        //学校種別取得
        $school_kind = $db->getCol(knjx_e061studyrecQuery::getSchoolKind());
        //教育課程コード取得
        $curriculum_cd = $db->getCol(knjx_e061studyrecQuery::getCurriculumCd());

        foreach ($data_each as $key => $val) {
            if ($this->Properties["useCurriculumcd"] == '1') {
                //必須項目チェック
                if (in_array($key, array("SCHOOLCD","YEAR","SCHREGNO","ANNUAL","CLASSCD","SCHOOL_KIND","CURRICULUM_CD","SUBCLASSCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //文字数チェック
                if (in_array($key, array("SCHOOLCD","YEAR","SCHREGNO","ANNUAL","CLASSCD","SCHOOL_KIND","CURRICULUM_CD","SUBCLASSCD"))) {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                } elseif ($this->field["SHORI_MEI"] == "1") {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
                //値チェック
                if (in_array($key, array("SCHOOL_KIND"))) {
                    if (!in_array($data[$key], $school_kind)) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                }
                if (in_array($key, array("CURRICULUM_CD"))) {
                    if (!in_array($data[$key], $curriculum_cd)) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                }
            } else {
                //必須項目チェック
                if (in_array($key, array("SCHOOLCD","YEAR","SCHREGNO","ANNUAL","CLASSCD","SUBCLASSCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //文字数チェック
                if (in_array($key, array("SCHOOLCD","YEAR","SCHREGNO","ANNUAL","CLASSCD","SUBCLASSCD"))) {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                } elseif ($this->field["SHORI_MEI"] == "1") {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            }

            //数値チェック
            if (in_array($key, array("VALUATION","GET_CREDIT","ADD_CREDIT","COMP_CREDIT"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_e061studyrecQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
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
        //DB接続
        $db = Query::dbCheckOut();

        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        if ($this->Properties["useCurriculumcd"] == '1') {
            //ヘッダデータ
            $header = array("SCHOOLCD"          => "※本校区分",
                            "GRADE"             => "学年",
                            "HR_CLASS"          => "組",
                            "ATTENDNO"          => "出席番号",
                            "SCHREGNO"          => "※学籍番号",
                            "NAME"              => "生徒氏名",
                            "SEX"               => "性別区分",
                            "SEX_NAME"          => "性別",
                            "YEAR"              => "※年度",
                            "ANNUAL"            => "※履修学年",
                            "COURSECD"          => "課程コード",
                            "MAJORCD"           => "学科コード",
                            "COURSECODE"        => "コースコード",
                            "COURSECODENAME"    => "コースコード名",
                            "CLASSCD"           => "※教科コード",
                            "SCHOOL_KIND"       => "※学校種別",
                            "CURRICULUM_CD"     => "※教育課程コード",
                            "CLASSNAME"         => "教科名",
                            "CLASSABBV"         => "教科略称",
                            "CLASSNAME_ENG"     => "教科名英字",
                            "CLASSABBV_ENG"     => "教科略称英字",
                            "SUBCLASSCD"        => "※科目コード",
                            "SUBCLASSNAME"      => "科目名",
                            "SUBCLASSABBV"      => "科目略称",
                            "SUBCLASSNAME_ENG"  => "科目名英字",
                            "SUBCLASSABBV_ENG"  => "科目略称英字",
                            "VALUATION"         => "評価",
                            "GET_CREDIT"        => "標準修得単位",
                            "ADD_CREDIT"        => "増加修得単位",
                            "COMP_CREDIT"       => "履修単位",
                            "REMARK"            => "履修科目備考",
                            $this->lastColumn   => $this->lastColumn);

            //学校種別ヘッダー例取得
            $school_kind_rei = "";
            $result = $db->query(knjx_e061studyrecQuery::getSchoolKind());
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($school_kind_rei) {
                    $school_kind_rei .= " ";
                }
                $school_kind_rei .= $row["NAME1"].":".$row["ABBV1"];
            }
            if ($school_kind_rei == "") {
                $school_kind_rei = "名称マスタ未設定";
            }
            $result->free();

            //ヘッダ例
            $header_rei = array("SCHOOLCD"      => "0",
                            "GRADE"             => "01",
                            "HR_CLASS"          => "001",
                            "ATTENDNO"          => "001",
                            "SCHREGNO"          => "20051003",
                            "NAME"              => "今井花子",
                            "SEX"               => "2",
                            "SEX_NAME"          => "女性",
                            "YEAR"              => "2005",
                            "ANNUAL"            => "01",
                            "COURSECD"          => "1",
                            "MAJORCD"           => "001",
                            "COURSECODE"        => "0001",
                            "COURSECODENAME"    => "文Ⅰ",
                            "CLASSCD"           => "13",
                            "SCHOOL_KIND"       => $school_kind_rei,
                            "CURRICULUM_CD"     => "2",
                            "CLASSNAME"         => "公民",
                            "CLASSABBV"         => "公民",
                            "CLASSNAME_ENG"     => "Civics",
                            "CLASSABBV_ENG"     => "Civ",
                            "SUBCLASSCD"        => "130001",
                            "SUBCLASSNAME"      => "現代社会",
                            "SUBCLASSABBV"      => "現代社",
                            "SUBCLASSNAME_ENG"  => "Civics",
                            "SUBCLASSABBV_ENG"  => "Civ",
                            "VALUATION"         => "2",
                            "GET_CREDIT"        => "2",
                            "ADD_CREDIT"        => "2",
                            "COMP_CREDIT"       => "2",
                            "REMARK"            => "備考です",
                            $this->lastColumn   => $this->lastColumn);
        } else {
            //ヘッダデータ
            $header = array("SCHOOLCD"          => "※本校区分",
                            "GRADE"             => "学年",
                            "HR_CLASS"          => "組",
                            "ATTENDNO"          => "出席番号",
                            "SCHREGNO"          => "※学籍番号",
                            "NAME"              => "生徒氏名",
                            "SEX"               => "性別区分",
                            "SEX_NAME"          => "性別",
                            "YEAR"              => "※年度",
                            "ANNUAL"            => "※履修学年",
                            "COURSECD"          => "課程コード",
                            "MAJORCD"           => "学科コード",
                            "COURSECODE"        => "コースコード",
                            "COURSECODENAME"    => "コースコード名",
                            "CLASSCD"           => "※教科コード",
                            "CLASSNAME"         => "教科名",
                            "CLASSABBV"         => "教科略称",
                            "CLASSNAME_ENG"     => "教科名英字",
                            "CLASSABBV_ENG"     => "教科略称英字",
                            "SUBCLASSCD"        => "※科目コード",
                            "SUBCLASSNAME"      => "科目名",
                            "SUBCLASSABBV"      => "科目略称",
                            "SUBCLASSNAME_ENG"  => "科目名英字",
                            "SUBCLASSABBV_ENG"  => "科目略称英字",
                            "VALUATION"         => "評価",
                            "GET_CREDIT"        => "標準修得単位",
                            "ADD_CREDIT"        => "増加修得単位",
                            "COMP_CREDIT"       => "履修単位",
                            "REMARK"            => "履修科目備考",
                            $this->lastColumn   => $this->lastColumn);
            //ヘッダ例
            $header_rei = array("SCHOOLCD"      => "0",
                            "GRADE"             => "01",
                            "HR_CLASS"          => "001",
                            "ATTENDNO"          => "001",
                            "SCHREGNO"          => "20051003",
                            "NAME"              => "今井花子",
                            "SEX"               => "2",
                            "SEX_NAME"          => "女性",
                            "YEAR"              => "2005",
                            "ANNUAL"            => "01",
                            "COURSECD"          => "1",
                            "MAJORCD"           => "001",
                            "COURSECODE"        => "0001",
                            "COURSECODENAME"    => "文Ⅰ",
                            "CLASSCD"           => "13",
                            "CLASSNAME"         => "公民",
                            "CLASSABBV"         => "公民",
                            "CLASSNAME_ENG"     => "Civics",
                            "CLASSABBV_ENG"     => "Civ",
                            "SUBCLASSCD"        => "130001",
                            "SUBCLASSNAME"      => "現代社会",
                            "SUBCLASSABBV"      => "現代社",
                            "SUBCLASSNAME_ENG"  => "Civics",
                            "SUBCLASSABBV_ENG"  => "Civ",
                            "VALUATION"         => "2",
                            "GET_CREDIT"        => "2",
                            "ADD_CREDIT"        => "2",
                            "COMP_CREDIT"       => "2",
                            "REMARK"            => "備考です",
                            $this->lastColumn   => $this->lastColumn);
        }


        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {
            //エラー
            $headerDuplicate = array("SCHOOLCD"      => "本校区分",
                                     "YEAR"          => "年度",
                                     "SCHREGNO"      => "学籍番号",
                                     "ANNUAL"        => "履修学年",
                                     "CLASSCD"       => "教科コード",
                                     "SCHOOL_KIND"   => "学校種別",
                                     "CURRICULUM_CD" => "教育課程コード",
                                     "SUBCLASSCD"    => "科目コード");

            $tables = array();
            $tables = array("1" => "SCHREG_STUDYREC_DAT", "2" => "STUDYRECREMARK_DAT");
            $dupTable = $this->getDuplicateErrorHeader('KNJX_E061M', $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4") {
            //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery('KNJX_E061M');
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx_e061studyrecQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "学籍学習記録データ";
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
