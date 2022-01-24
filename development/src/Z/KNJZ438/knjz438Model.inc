<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjz438Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjz438Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
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

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "ENTYEAR"          =>  VARS::post("ENTYEAR"),          //入学年度
                             "COURSE"           =>  VARS::post("COURSE"),           //課程・学科
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

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
                    knjz438Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjz438Query::deleteQueryCsv($this, $this->data_arr);
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
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $checkdata = array("ENTYEAR"           => array(4,"入学年度"),
                                   "COURSECD"          => array(1,"課程コード"),
                                   "MAJORCD"           => array(3,"学科コード"),
                                   "CLASSCD"           => array(2,"教科コード"),
                                   "SCHOOL_KIND"       => array(2,"学校校種"),
                                   "CURRICULUM_CD"     => array(2,"教育課程コード"),
                                   "SUBCLASSCD"        => array(6,"科目コード"),
                                   "SELECTKIND"        => array(2,"科目分類コード"),
                                   "STUDY1"            => array(1,"履修制限１"),
                                   "STUDY2"            => array(1,"履修制限２"),
                                   "STUDY3"            => array(1,"履修制限３"),
                                   "STUDY4"            => array(1,"履修制限４"),
                                   "STUDY5"            => array(1,"履修制限５"),
                                   "STUDY6"            => array(1,"履修制限６"),
                                   "CREDITS"           => array(2,"単位数"),
                                   "REMARK"            => array(45,"備考")
                                   );
            } else {
                $checkdata = array("ENTYEAR"           => array(4,"入学年度"),
                                   "COURSECD"          => array(1,"課程コード"),
                                   "MAJORCD"           => array(3,"学科コード"),
                                   "CLASSCD"           => array(2,"教科コード"),
                                   "SUBCLASSCD"        => array(6,"科目コード"),
                                   "SELECTKIND"        => array(2,"科目分類コード"),
                                   "STUDY1"            => array(1,"履修制限１"),
                                   "STUDY2"            => array(1,"履修制限２"),
                                   "STUDY3"            => array(1,"履修制限３"),
                                   "STUDY4"            => array(1,"履修制限４"),
                                   "STUDY5"            => array(1,"履修制限５"),
                                   "STUDY6"            => array(1,"履修制限６"),
                                   "CREDITS"           => array(2,"単位数"),
                                   "REMARK"            => array(45,"備考")
                                   );
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;

            //取得主キー
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('SUBCLASS_HOLD_DAT');
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
                    //教育課程対応
                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $data = array("ENTYEAR"           => $tmp[0],
                                      "COURSECD"          => $tmp[1],
                                      "MAJORCD"           => strlen(trim($tmp[3])) ? sprintf("%03d", $tmp[3]) : "",
                                      "CLASSCD"           => strlen(trim($tmp[5])) ? sprintf("%02d", $tmp[5]) : "",
                                      "SCHOOL_KIND"       => $tmp[7],
                                      "CURRICULUM_CD"     => $tmp[8],
                                      "SUBCLASSCD"        => strlen(trim($tmp[9])) ? sprintf("%06d", $tmp[9]) : "",
                                      "SELECTKIND"        => strlen(trim($tmp[11])) ? sprintf("%02d", $tmp[11]) : "",
                                      "STUDY1"            => $tmp[13],
                                      "STUDY2"            => $tmp[14],
                                      "STUDY3"            => $tmp[15],
                                      "STUDY4"            => $tmp[16],
                                      "STUDY5"            => $tmp[17],
                                      "STUDY6"            => $tmp[18],
                                      "CREDITS"           => $tmp[19],
                                      "REMARK"            => $tmp[20]
                                      );
                    } else {
                        $data = array("ENTYEAR"           => $tmp[0],
                                      "COURSECD"          => $tmp[1],
                                      "MAJORCD"           => strlen(trim($tmp[3])) ? sprintf("%03d", $tmp[3]) : "",
                                      "CLASSCD"           => strlen(trim($tmp[5])) ? sprintf("%02d", $tmp[5]) : "",
                                      "SUBCLASSCD"        => strlen(trim($tmp[7])) ? sprintf("%06d", $tmp[7]) : "",
                                      "SELECTKIND"        => strlen(trim($tmp[9])) ? sprintf("%02d", $tmp[9]) : "",
                                      "STUDY1"            => $tmp[11],
                                      "STUDY2"            => $tmp[12],
                                      "STUDY3"            => $tmp[13],
                                      "STUDY4"            => $tmp[14],
                                      "STUDY5"            => $tmp[15],
                                      "STUDY6"            => $tmp[16],
                                      "CREDITS"           => $tmp[17],
                                      "REMARK"            => $tmp[18]
                                      );
                    }
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
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
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ

        $hissuSep        = "";
        $masterSep       = "";
        $numericSep      = "";
        $lengthSep       = "";
        $duplicateCsvSep = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                //必須項目チェック
                if (in_array($key, array("ENTYEAR","COURSECD","MAJORCD","CLASSCD","SCHOOL_KIND","CURRICULUM_CD","SUBCLASSCD","SELECTKIND"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            } else {
                //必須項目チェック
                if (in_array($key, array("ENTYEAR","COURSECD","MAJORCD","CLASSCD","SUBCLASSCD","SELECTKIND"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            }

            //更新
            if ($this->field["SHORI_MEI"] == "1") {
                //存在チェック
                if ($data[$key] != "" && in_array($key, array("COURSECD","MAJORCD","CLASSCD","SCHOOL_KIND","CURRICULUM_CD", "SUBCLASSCD","SELECTKIND"))) {
                    if ($key == "COURSECD") {
                        $query = knjz438Query::countCoursecd($data[$key]);
                    }
                    if ($key == "MAJORCD") {
                        $query = knjz438Query::countMajorcd($data[$key]);
                    }
                    if ($key == "CLASSCD") {
                        $query = knjz438Query::countClasscd($data[$key]);
                    }
                    //教育課程対応
                    if ($this->Properties["useCurriculumcd"] == '1') {
                        if ($key == "SCHOOL_KIND") {
                            $query = knjz438Query::countSchoolKind($data[$key]);
                        }
                        if ($key == "CURRICULUM_CD") {
                            $query = knjz438Query::countCurriculumCd($data[$key]);
                        }
                    }
                    if ($key == "SUBCLASSCD") {
                        $query = knjz438Query::countSubclasscd($data[$key]);
                    }
                    if ($key == "SELECTKIND") {
                        $query = knjz438Query::countSelectkind($data[$key]);
                    }
                    if (1 > $db->getOne($query)) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                    }
                }

                //数値チェック 備考は数値チェック除外
                if ($data[$key] != "" && $key !== "REMARK" && !is_numeric($data[$key])) {
                    //教育課程対応
                    if ($this->Properties["useCurriculumcd"] == '1') {
                        if ($key == "SCHOOL_KIND") {
                            true;
                        } else {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                        }
                    } else {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$masterErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjz438Query::insertQueryErr($db, $line, $checkError);
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
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $header = array("ENTYEAR"           => "※入学年度",
                            "COURSECD"          => "※課程コード",
                            "COURSENAME"        => "課程名",
                            "MAJORCD"           => "※学科コード",
                            "MAJORNAME"         => "学科名",
                            "CLASSCD"           => "※教科コード",
                            "CLASSNAME"         => "教科名",
                            "SCHOOL_KIND"       => "※学校校種",
                            "CURRICULUM_CD"     => "※教育課程コード",
                            "SUBCLASSCD"        => "※科目コード",
                            "SUBCLASSNAME"      => "科目名",
                            "SELECTKIND"        => "※科目分類コード",
                            "SELECTKIND_NAME"   => "科目分類名",
                            "STUDY1"            => "履修制限１",
                            "STUDY2"            => "履修制限２",
                            "STUDY3"            => "履修制限３",
                            "STUDY4"            => "履修制限４",
                            "STUDY5"            => "履修制限５",
                            "STUDY6"            => "履修制限６",
                            "CREDITS"           => "単位数",
                            "REMARK"            => "備考",
                            $this->lastColumn   => $this->lastColumn);

            $header_rei = array("ENTYEAR"           => "2009",
                                "COURSECD"          => "1",
                                "COURSENAME"        => "全日制",
                                "MAJORCD"           => "001",
                                "MAJORNAME"         => "情報普通科 マルチメディア技術科",
                                "CLASSCD"           => "13",
                                "CLASSNAME"         => "公民",
                                "SCHOOL_KIND"       => "H",
                                "CURRICULUM_CD"     => "2",
                                "SUBCLASSCD"        => "130001",
                                "SUBCLASSNAME"      => "現代社会",
                                "SELECTKIND"        => "00",
                                "SELECTKIND_NAME"   => "必履修",
                                "STUDY1"            => "1",
                                "STUDY2"            => "1",
                                "STUDY3"            => "1",
                                "STUDY4"            => "1",
                                "STUDY5"            => "1",
                                "STUDY6"            => "1",
                                "CREDITS"           => "1",
                                "REMARK"            => "備考コメント",
                                $this->lastColumn   => $this->lastColumn);
        } else {
            $header = array("ENTYEAR"           => "※入学年度",
                            "COURSECD"          => "※課程コード",
                            "COURSENAME"        => "課程名",
                            "MAJORCD"           => "※学科コード",
                            "MAJORNAME"         => "学科名",
                            "CLASSCD"           => "※教科コード",
                            "CLASSNAME"         => "教科名",
                            "SUBCLASSCD"        => "※科目コード",
                            "SUBCLASSNAME"      => "科目名",
                            "SELECTKIND"        => "※科目分類コード",
                            "SELECTKIND_NAME"   => "科目分類名",
                            "STUDY1"            => "履修制限１",
                            "STUDY2"            => "履修制限２",
                            "STUDY3"            => "履修制限３",
                            "STUDY4"            => "履修制限４",
                            "STUDY5"            => "履修制限５",
                            "STUDY6"            => "履修制限６",
                            "CREDITS"           => "単位数",
                            "REMARK"            => "備考",
                            $this->lastColumn   => $this->lastColumn);

            $header_rei = array("ENTYEAR"           => "2009",
                                "COURSECD"          => "1",
                                "COURSENAME"        => "全日制",
                                "MAJORCD"           => "001",
                                "MAJORNAME"         => "情報普通科 マルチメディア技術科",
                                "CLASSCD"           => "13",
                                "CLASSNAME"         => "公民",
                                "SUBCLASSCD"        => "130001",
                                "SUBCLASSNAME"      => "現代社会",
                                "SELECTKIND"        => "00",
                                "SELECTKIND_NAME"   => "必履修",
                                "STUDY1"            => "1",
                                "STUDY2"            => "1",
                                "STUDY3"            => "1",
                                "STUDY4"            => "1",
                                "STUDY5"            => "1",
                                "STUDY6"            => "1",
                                "CREDITS"           => "1",
                                "REMARK"            => "備考コメント",
                                $this->lastColumn   => $this->lastColumn);
        }

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $tables = array();
            $tables = array("1" => "SUBCLASS_HOLD_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
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
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjz438Query::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "入学年度の開講科目";
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
