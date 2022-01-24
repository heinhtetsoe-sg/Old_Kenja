<?php

require_once('for_php7.php');
class knjx_c033kModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $sickdiv    = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    public function knjx_c033kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"]) {
            $this->getSelectSchoolKind();
        }

        //欠課種別コード
        $this->sickdiv[1] = "ABSENT";
        $this->sickdiv[2] = "SUSPEND";
        $this->sickdiv[3] = "MOURNING";
        $this->sickdiv[4] = "SICK";
        $this->sickdiv[5] = "NOTICE";
        $this->sickdiv[6] = "NONOTICE";
        $this->sickdiv[15] = "LATE";
        $this->sickdiv[16] = "EARLY";
        if ($this->Properties["useVirus"] == "true") {
            $this->sickdiv[19] = "VIRUS";
        }
        if ($this->Properties["useKoudome"] == "true") {
            $this->sickdiv[25] = "KOUDOME";
        }

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目一覧
                             "CHAIRCD"          =>  VARS::post("CHAIRCD"),          //講座一覧
                             "MONTH"            =>  VARS::post("MONTH"),            //対象月
                             "SICK"             =>  VARS::post("SICK"),             //欠課種別
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        if (VARS::get("SEND_PRGID")) {
            $this->field["SUBCLASSCD"] = (VARS::get("SEND_SUBCLASSCD")) ? VARS::get("SEND_SUBCLASSCD") : $this->field["SUBCLASSCD"];
            $this->field["CHAIRCD"]    = (VARS::get("SEND_CHAIRCD"))    ? VARS::get("SEND_CHAIRCD")    : $this->field["CHAIRCD"];
            $this->field["MONTH"]      = (VARS::get("SEND_MONTHCD"))    ? VARS::get("SEND_MONTHCD")    : $this->field["MONTH"];
            $this->field["SICK"]       = (VARS::get("SEND_SICK"))       ? VARS::get("SEND_SICK")       : $this->field["SICK"];
        }

        $this->cmd  = VARS::request("cmd");
        $this->file = VARS::file("FILE");

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
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx_c033kQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_c033kQuery::deleteQueryCsv($this, $this->data_arr);
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
            $db = Query::dbCheckOut();

            if ($this->Properties["useCurriculumcd"] == '1') {
                $checkdata = array("CLASSCD"        => array(2,"教科コード"),
                                   "SCHOOL_KIND"    => array(2,"学校種別"),
                                   "CURRICULUM_CD"  => array(2,"教育課程コード"),
                                   "SUBCLASSCD"     => array(6,"科目コード"),
                                   "CHAIRCD"        => array(7,"講座コード"),
                                   "YEAR"           => array(4,"年度"),
                                   "MONTH"          => array(2,"対象月"),
                                   "SEMESTER"       => array(1,"学期"),
                                   "SCHREGNO"       => array(8,"学籍番号"),
                                   "LESSON"         => array(3,"授業時数"),
                                   "SICK_DIV"       => array(2,"欠課種別"),
                                   "SICK_DATA"      => array(3,"欠課数"));
            } else {
                $checkdata = array("SUBCLASSCD"     => array(6,"科目コード"),
                                   "CHAIRCD"        => array(7,"講座コード"),
                                   "YEAR"           => array(4,"年度"),
                                   "MONTH"          => array(2,"対象月"),
                                   "SEMESTER"       => array(1,"学期"),
                                   "SCHREGNO"       => array(8,"学籍番号"),
                                   "LESSON"         => array(3,"授業時数"),
                                   "SICK_DIV"       => array(2,"欠課種別"),
                                   "SICK_DATA"      => array(3,"欠課数"));
            }

            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $primaryKey2   = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $primaryKey1   = $this->getPrimaryKey('ATTEND_SUBCLASS_DAT');
            $primaryKey2   = $this->getPrimaryKey('ATTEND_SEMES_DAT');
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
                    $tmp    = str_replace("↓", "\n", $tmp);

                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $data = array("CLASSCD"         => (strlen(trim($tmp[0]))) ? sprintf("%02d", $tmp[0]) : "",
                                      "SCHOOL_KIND"     => (strlen(trim($tmp[1]))) ? $tmp[1] : "",
                                      "CURRICULUM_CD"   => (strlen(trim($tmp[2]))) ? $tmp[2] : "",
                                      "SUBCLASSCD"      => (strlen(trim($tmp[3]))) ? sprintf("%06d", $tmp[3]) : "",
                                      "CHAIRCD"         => (strlen(trim($tmp[5]))) ? sprintf("%07d", $tmp[5]) : "",
                                      "YEAR"            => (strlen(trim($tmp[7]))) ? $tmp[7] : "",
                                      "MONTH"           => (strlen(trim($tmp[8]))) ? sprintf("%02d", $tmp[8]) : "",
                                      "SEMESTER"        => (strlen(trim($tmp[9]))) ? $tmp[9] : "",
                                      "SCHREGNO"        => (strlen(trim($tmp[10]))) ? sprintf("%08d", $tmp[10]) : "",
                                      "LESSON"          => (strlen(trim($tmp[15]))) ? $tmp[15] : "",
                                      "SICK_DIV"        => (strlen(trim($tmp[16]))) ? $tmp[16] : "",
                                      "SICK_DATA"       => (strlen(trim($tmp[17]))) ? $tmp[17] : "",
                                      "COPYCD"          => "0");
                    } else {
                        $data = array("SUBCLASSCD"      => (strlen(trim($tmp[0]))) ? sprintf("%06d", $tmp[0]) : "",
                                      "CHAIRCD"         => (strlen(trim($tmp[2]))) ? sprintf("%07d", $tmp[2]) : "",
                                      "YEAR"            => (strlen(trim($tmp[4]))) ? $tmp[4] : "",
                                      "MONTH"           => (strlen(trim($tmp[5]))) ? sprintf("%02d", $tmp[5]) : "",
                                      "SEMESTER"        => (strlen(trim($tmp[6]))) ? $tmp[6] : "",
                                      "SCHREGNO"        => (strlen(trim($tmp[7]))) ? sprintf("%08d", $tmp[7]) : "",
                                      "LESSON"          => (strlen(trim($tmp[12]))) ? $tmp[12] : "",
                                      "SICK_DIV"        => (strlen(trim($tmp[13]))) ? $tmp[13] : "",
                                      "SICK_DATA"       => (strlen(trim($tmp[14]))) ? $tmp[14] : "",
                                      "COPYCD"          => "0");
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
            $db->commit();  // トランザクションをコミットする。
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
        $masterErrFlg       = false;    //マスタエラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($this->Properties["useCurriculumcd"] == '1') {
                if (in_array($key, array("CLASSCD","SCHOOL_KIND","CURRICULUM_CD","SUBCLASSCD","CHAIRCD","YEAR","MONTH","SEMESTER","SCHREGNO","LESSON","SICK_DIV"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            } else {
                if (in_array($key, array("SUBCLASSCD","CHAIRCD","YEAR","MONTH","SEMESTER","SCHREGNO","LESSON","SICK_DIV"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            }

            //数値チェック
            if ($this->Properties["useCurriculumcd"] == '1') {
                if ($data[$key] != "" && !is_numeric($data[$key]) && $key != "SCHOOL_KIND") {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            } else {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }

            //文字チェック
            if ($this->Properties["useCurriculumcd"] == '1') {
                $school_kind = $db->getCol(knjx_c033kQuery::getSchoolKind($this));
                if ($data[$key] != "" && !in_array($data[$key], $school_kind) && $key == "SCHOOL_KIND") {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※文字:", $lengthSep, $val[1]);
                }

                $curriculum_cd = $db->getCol(knjx_c033kQuery::getCurriculumCd());
                if ($data[$key] != "" && !in_array($data[$key], $curriculum_cd) && $key == "CURRICULUM_CD") {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※文字:", $lengthSep, $val[1]);
                }
            }
        }

        //存在チェック（在籍）
        if (1 > $db->getOne(knjx_c033kQuery::checkRegdDat($this, $data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"]))) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍データがありません。");
        }

        //存在チェック（講座）
        if ($this->Properties["useCurriculumcd"] == '1') {
            $ck_subclass = array($data["CLASSCD"], $data["SCHOOL_KIND"], $data["CURRICULUM_CD"], $data["SUBCLASSCD"]);
        } else {
            $ck_subclass = $data["SUBCLASSCD"];
        }
        if (1 > $db->getOne(knjx_c033kQuery::checkChairDat($data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"], $ck_subclass, $data["CHAIRCD"], $this))) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された講座コードがありません。");
        }

        //存在チェック（対象月）
        if (!$db->getOne(knjx_c033kQuery::selectMonthQuery($data["YEAR"], $data["MONTH"], $this))) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された年月のデータがありません。");
        }

        //存在チェック（学期）
        $seme = $db->getRow(knjx_c033kQuery::selectSemesAll($data["YEAR"], $data["SEMESTER"]), DB_FETCHMODE_ASSOC);
        if (($seme["S_MONTH"] > $data["MONTH"]) || ($data["MONTH"] > $seme["E_MONTH"])) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された学期データがありません。");
        }

        //存在チェック（欠課種別）
        if (!$db->getOne(knjx_c033kQuery::getSickDiv($data["YEAR"], $data["SICK_DIV"]))) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された欠課種別がありません。");
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_c033kQuery::insertQueryErr($db, $line, $checkError);
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

        //ヘッダー（エラー）
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        //ヘッダー
        if ($this->Properties["useCurriculumcd"] == '1') {
            $header["CLASSCD"]          = "※教科コード";
            $header["SCHOOL_KIND"]      = "※学校種別";
            $header["CURRICULUM_CD"]    = "※教育課程コード";
        }
        $header["SUBCLASSCD"]       = "※科目コード";
        $header["SUBCLASSNAME"]     = "科目名";
        $header["CHAIRCD"]          = "※講座コード";
        $header["CHAIRNAME"]        = "講座名";
        $header["YEAR"]             = "※年度";
        $header["MONTH"]            = "※対象月";
        $header["SEMESTER"]         = "※学期";
        $header["SCHREGNO"]         = "※学籍番号";
        $header["HR_NAME"]          = "年組";
        $header["ATTENDNO"]         = "出席番号";
        $header["NAME"]             = "氏名";
        $header["APPOINTED_DAY"]    = "締め日";
        $header["LESSON"]           = "※授業時数";
        $header["SICK_DIV"]         = "※欠課種別";
        $header["SICK_DATA"]        = "欠課数";
        $header[$this->lastColumn]  = $this->lastColumn;

        //ヘッダー例
        if ($this->Properties["useCurriculumcd"] == '1') {
            //学校種別ヘッダー例取得
            $school_kind_rei = "";
            $result = $db->query(knjx_c033kQuery::getSchoolKind($this));
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

            $header_rei["CLASSCD"]          = "12";
            $header_rei["SCHOOL_KIND"]      = $school_kind_rei;
            $header_rei["CURRICULUM_CD"]    = "2";
        }
        $header_rei["SUBCLASSCD"]       = "123456";
        $header_rei["SUBCLASSNAME"]     = "数学";
        $header_rei["CHAIRCD"]          = "1234567";
        $header_rei["CHAIRNAME"]        = "数学1-1";
        $header_rei["YEAR"]             = CTRL_YEAR;
        $header_rei["MONTH"]            = "04";
        $header_rei["SEMESTER"]         = "1";
        $header_rei["SCHREGNO"]         = "12345678";
        $header_rei["HR_NAME"]          = "1年1組";
        $header_rei["ATTENDNO"]         = "001";
        $header_rei["NAME"]             = "アルプ太郎";
        $header_rei["APPOINTED_DAY"]    = "30";
        $header_rei["LESSON"]           = "15";
        $header_rei["SICK_DIV"]         = "4";
        $header_rei["SICK_DATA"]        = "2";
        $header_rei[$this->lastColumn]  = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $headerDuplicate = array("COPYCD"         => "複写区分",
                                     "YEAR"           => "年度",
                                     "MONTH"          => "対象月",
                                     "SEMESTER"       => "学期",
                                     "SCHREGNO"       => "学籍番号",
                                     "CLASSCD"        => "教科コード",
                                     "SCHOOL_KIND"    => "学校種別",
                                     "CURRICULUM_CD"  => "教育課程コード",
                                     "SUBCLASSCD"     => "科目コード");

            $tables = array();
            $tables = array("1" => "ATTEND_SUBCLASS_DAT", "2" => "ATTEND_SEMES_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx_c033kQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {              //データ
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);

                    $monthsem = preg_split("/-/", $this->field["MONTH"]);
                    $row["MONTH"] = $monthsem[0];
                    $row["APPOINTED_DAY"] = $db->getOne(knjx_c033kQuery::getAppointedDay($row["YEAR"], $row["MONTH"], $row["SEMESTER"]));
                    $row["SICK_DIV"] = $this->field["SICK"];

                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $subclasscd = array($row["CLASSCD"], $row["SCHOOL_KIND"], $row["CURRICULUM_CD"], $row["SUBCLASSCD"]);
                    } else {
                        $subclasscd = $row["SUBCLASSCD"];
                    }

                    $attend = $db->getRow(knjx_c033kQuery::getAttendSubclassDat($row["YEAR"], $row["SEMESTER"], $row["MONTH"], $row["SCHREGNO"], $subclasscd, $this->sickdiv[$this->field["SICK"]], $this), DB_FETCHMODE_ASSOC);
                    $row["LESSON"] = $attend["LESSON"];
                    $row["SICK_DATA"] = $attend["SICK_DATA"];
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {      //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "欠時数情報入力";
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
