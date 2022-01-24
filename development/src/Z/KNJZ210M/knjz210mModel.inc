<?php

require_once('for_php7.php');
class knjz210mModel extends Model
{
    public $field = array();
    public $field2 = array();
    public $cmd;
    public $year;
    public $grade;
    public $ibprg_course;
    public $classcd;
    public $school_kind;
    public $curriculum_cd;
    public $subclasscd;
    public $file;
    public $lastColumn;//CSVの最後の列の値

    public function knjz210mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->grade);
            unset($this->ibprg_course);
            unset($this->classcd);
            unset($this->school_kind);
            unset($this->curriculum_cd);
            unset($this->subclasscd);
            $this->field = array();
            $this->field2 = array();
        }

        list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', VARS::post("SUBCLASS"));

        $this->field = array("GRADE"            =>  VARS::post("GRADE"),
                             "IBPRG_COURSE"     =>  VARS::post("IBPRG_COURSE"),
                             "CLASSCD"          =>  $classcd,
                             "SCHOOL_KIND"      =>  $school_kind,
                             "CURRICULUM_CD"    =>  $curriculum_cd,
                             "SUBCLASSCD"       =>  $subclasscd,
                             "MAX_SEQ"          =>  VARS::post("MAX_SEQ"),
                             "HEADER"           =>  VARS::post("HEADER"),
                             "OUTPUT"           =>  VARS::post("OUTPUT")
                             );

        if ($this->field["MAX_SEQ"] > 0) {
            $this->field2 = array();
            for ($i = 1; $i <= $this->field["MAX_SEQ"]; $i++) {
                $this->field2["CUTTING_MARK_".$i]   =  VARS::post("CUTTING_MARK_".$i);
                $this->field2["CUTTING_HIGH_".$i]   =  VARS::post("CUTTING_HIGH_".$i);
            }
        } else {
            $this->field2 = array();
        }

        $this->cmd  = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        //年度（初期値）
        $this->year = ($this->year == "") ? CTRL_YEAR : $this->year;

        if (VARS::post("YEAR") != "") {
            $this->year = VARS::post("YEAR");
        }

        //左フレームからのパラメータ
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("IBPRG_COURSE") != "") {
            $this->ibprg_course = VARS::get("IBPRG_COURSE");
        }
        if (VARS::get("CLASSCD") != "") {
            $this->classcd = VARS::get("CLASSCD");
        }
        if (VARS::get("SCHOOL_KIND") != "") {
            $this->school_kind = VARS::get("SCHOOL_KIND");
        }
        if (VARS::get("CURRICULUM_CD") != "") {
            $this->curriculum_cd = VARS::get("CURRICULUM_CD");
        }
        if (VARS::get("SUBCLASSCD") != "") {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須チェック
        if (trim($this->year) == "") {
            $this->setWarning("MSG301", "(年度)");
            return false;
        }
        if (trim($this->field["GRADE"]) == "") {
            $this->setWarning("MSG301", "(学年)");
            return false;
        }
        if (trim($this->field["IBPRG_COURSE"]) == "") {
            $this->setWarning("MSG301", "(IBコース)");
            return false;
        }
        if (trim($this->field["CLASSCD"]) == "" || trim($this->field["SCHOOL_KIND"]) == "" || trim($this->field["CURRICULUM_CD"]) == "" || trim($this->field["SUBCLASSCD"]) == "") {
            $this->setWarning("MSG301", "(科目)");
            return false;
        }

        if ($this->cmd == "delete") {
            //キーの更新チェック
            if ($this->grade != $this->field["GRADE"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->ibprg_course != $this->field["IBPRG_COURSE"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->classcd != $this->field["CLASSCD"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->school_kind != $this->field["SCHOOL_KIND"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->curriculum_cd != $this->field["CURRICULUM_CD"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->subclasscd != $this->field["SUBCLASSCD"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if (!strlen($this->grade) || !strlen($this->ibprg_course) || !strlen($this->classcd) || !strlen($this->school_kind) || !strlen($this->curriculum_cd) || !strlen($this->subclasscd)) {
                $this->setWarning("MSG308");
                return false;
            }
        } else {
            //必須チェック
            if (trim($this->field["MAX_SEQ"]) == "") {
                $this->setWarning("MSG301", "(段階数)");
                return false;
            }

            $seq = 0;
            for ($i = 1; $i <= $this->field["MAX_SEQ"]; $i++) {
                //必須チェック
                if (trim($this->field2["CUTTING_MARK_".$i]) == "") {
                    $this->setWarning("MSG301", "(評定段階".$i." : 評定記号)");
                    return false;
                }
                if (trim($this->field2["CUTTING_HIGH_".$i]) == "") {
                    $this->setWarning("MSG301", "(評定段階".$i." : 到達度上限)");
                    return false;
                }
                //範囲チェック
                if ($i != $this->field["MAX_SEQ"]) {
                    if (strlen(trim($this->field2["CUTTING_HIGH_".$i])) > 4) {
                        $this->setWarning("MSG916", "(評定段階".$i.")");
                        return false;
                    }
                }
                if (trim($this->field2["CUTTING_HIGH_".$i]) <= $seq) {
                    $this->setWarning("MSG916", "(評定段階".$i.")");
                    return false;
                }
                $seq = $this->field2["CUTTING_HIGH_".$i];
            }
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz210mQuery::getIBViewCuttingDat($this->year, $this->field["GRADE"], $this->field["IBPRG_COURSE"], $this->field["CLASSCD"], $this->field["SCHOOL_KIND"], $this->field["CURRICULUM_CD"], $this->field["SUBCLASSCD"], "", "check");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //対象がなければエラー
        if ($this->cmd == "delete" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    /**********/
    /* INSERT */
    /**********/
    public function &getInsertModel()
    {
        if ($this->validateInput()) {
            //登録
            knjz210mQuery::getInsertQuery($this);
            $this->setMessage("MSG201");

            $this->grade            = $this->field["GRADE"];
            $this->ibprg_course     = $this->field["IBPRG_COURSE"];
            $this->classcd          = $this->field["CLASSCD"];
            $this->school_kind      = $this->field["SCHOOL_KIND"];
            $this->curriculum_cd    = $this->field["CURRICULUM_CD"];
            $this->subclasscd       = $this->field["SUBCLASSCD"];

            $this->setCmd("edit");
        } else {
            $this->setCmd("check");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    public function &getDeleteModel()
    {
        if ($this->validateInput()) {
            //削除
            knjz210mQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");

            unset($this->grade);
            unset($this->ibprg_course);
            unset($this->classcd);
            unset($this->school_kind);
            unset($this->curriculum_cd);
            unset($this->subclasscd);
            unset($this->field);
            unset($this->field2);
        }
    }

    /********/
    /* COPY */
    /********/
    public function &getCopyModel()
    {
        $db = Query::dbCheckOut();

        //存在チェック（前年度）
        $pre_year = $this->year - 1;
        $cnt_pre = $db->getOne(knjz210mQuery::checkIBViewCuttingDat($pre_year));
        if ($cnt_pre == 0) {
            $this->setWarning("MSG917", "前年度のデータが存在しません。");
            return false;
        }
        //存在チェック（対象年度）
        $cnt_this = $db->getOne(knjz210mQuery::checkIBViewCuttingDat($this->year));
        if ($cnt_this > 0) {
            $this->setWarning("MSG917", "対象年度にデータが存在します。");
            return false;
        }

        Query::dbCheckIn($db);

        //コピー
        knjz210mQuery::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }

    /***********/
    /* CSV出力 */
    /***********/
    public function getDownloadCsvModel()
    {
        $duplHeader = array("YEAR"              => "年度",
                            "GRADE"             => "学年",
                            "IBPRG_COURSE"      => "IBコース".(get_count($ibprg_course) > 0 ? "(".implode("/", $ibprg_course).")" : ""),
                            "CLASSCD"           => "教科コード",
                            "SCHOOL_KIND"       => "校種コード",
                            "CURRICULUM_CD"     => "教育課程コード",
                            "SUBCLASSCD"        => "科目コード",
                            "DATA_DIV"          => "データ区分",
                            "SEQ"               => "評定段階"
                            );
        $db = Query::dbCheckOut();

        $data = false;
        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ（エラー出力）
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
            //エラー
            $tables = array();
            $tables = array("1" => "IBVIEW_CUTTING_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $duplHeader);
                $contents .= implode($headerErr, ",")."\n";
            }
            //ヘッダをセット
            $contents .= implode(",", $header) . "\r\n";

            //エラー出力データ
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } elseif ($this->field["OUTPUT"] == "2" || $this->field["OUTPUT"] == "4") {
            //IBコース一覧（ヘッダ表示用）
            $ibprg_course = $db->getCol(knjz210mQuery::getIBPrgCourseHeader($this));

            //ヘッダ項目
            $header = array("YEAR"              => "※年度",
                            "GRADE"             => "※学年",
                            "IBPRG_COURSE"      => "※IBコース".(get_count($ibprg_course) > 0 ? "(".implode("/", $ibprg_course).")" : ""),
                            "CLASSCD"           => "※教科コード",
                            "SCHOOL_KIND"       => "※校種コード",
                            "CURRICULUM_CD"     => "※教育課程コード",
                            "SUBCLASSCD"        => "※科目コード",
                            "SUBCLASSNAME"      => "科目名",
                            "SEQ"               => "※評定段階",
                            "CUTTING_MARK"      => "※評定記号",
                            "CUTTING_LOW"       => "※下限",
                            "CUTTING_HIGH"      => "※上限",
                            $this->lastColumn   => $this->lastColumn
                            );

            //ヘッダ有チェックボックス
            if ($this->field["HEADER"]) {
                //ヘッダをセット
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }

            //ＣＳＶ出力データ
            $query = knjz210mQuery::getCsvData($this);
        }

        if ($this->field["OUTPUT"] == "4") {
            //ヘッダ例用データ取得
            $rei = $db->getRow(knjz210mQuery::getHeaderRei(), DB_FETCHMODE_ASSOC);

            //ヘッダ例
            $header_rei = array("YEAR"              => $this->year,
                                "GRADE"             => ($rei["GRADE"]) ? $rei["GRADE"] : "01",
                                "IBPRG_COURSE"      => ($rei["IBPRG_COURSE"]) ? $rei["IBPRG_COURSE"] : "M",
                                "CLASSCD"           => "00",
                                "SCHOOL_KIND"       => "00",
                                "CURRICULUM_CD"     => "00",
                                "SUBCLASSCD"        => "000000",
                                "SUBCLASSNAME"      => "基本設定",
                                "SEQ"               => "1",
                                "CUTTING_MARK"      => "C",
                                "CUTTING_LOW"       => "0",
                                "CUTTING_HIGH"      => "39.9",
                                $this->lastColumn   => $this->lastColumn
                                );

            //ヘッダ例をセット
            $contents .= implode(",", $header_rei) . "\r\n";
            $data = true;
        } else {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //ヘッダ項目のみデータをセット
                foreach ($header as $key => $val) {
                    $contents .= ($key == $this->lastColumn) ? $row[$key] : $row[$key].",";
                }
                $contents .= "\r\n";
                $data = true;
            }
            $result->free();
        }

        Query::dbCheckIn($db);

        if ($data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse="エラー出力";
            } else {
                $couse="観点別学習状況カッティングポイント設定表";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    /***********/
    /* CSV取込 */
    /***********/
    public function getUploadCsvModel()
    {

        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);

                //更新（処理件数取得）
                knjz210mQuery::insertQueryCsv($db, $this, $this->data_arr);
                $db->commit();

                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //送られたファイルの文字列を取得
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {   //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            //チェック対象項目（フィールド名 => サイズ, 項目名（メッセージ用））
            $checkdata = array("YEAR"               => array(4, "年度"),
                               "GRADE"              => array(2, "学年"),
                               "IBPRG_COURSE"       => array(1, "IBコース"),
                               "CLASSCD"            => array(2, "教科コード"),
                               "SCHOOL_KIND"        => array(2, "校種コード"),
                               "CURRICULUM_CD"      => array(2, "教育課程コード"),
                               "SUBCLASSCD"         => array(6, "科目コード"),
                               "SEQ"                => array(3, "評定段階"),
                               "CUTTING_MARK"       => array(2, "評定記号"),
                               "CUTTING_LOW"        => array(4, "下限"),
                               "CUTTING_HIGH"       => array(4, "上限")
                               );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey1 = $this->getPrimaryKey('IBVIEW_CUTTING_DAT');
            $duplicateCsv1 = array();
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    //ヘッダ有の場合、1行目をスキップ
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    //データ項目＆値
                    $data = array("YEAR"            => $tmp[0],
                                  "GRADE"           => (strlen(trim($tmp[1]))) ? sprintf("%02d", $tmp[1]) : "",
                                  "IBPRG_COURSE"    => $tmp[2],
                                  "CLASSCD"         => (strlen(trim($tmp[3]))) ? sprintf("%02d", $tmp[3]) : "",
                                  "SCHOOL_KIND"     => (strlen(trim($tmp[4])) && $tmp[4] == "0") ? sprintf("%02d", $tmp[4]) : $tmp[4],
                                  "CURRICULUM_CD"   => (strlen(trim($tmp[5])) && $tmp[5] == "0") ? sprintf("%02d", $tmp[5]) : $tmp[5],
                                  "SUBCLASSCD"      => (strlen(trim($tmp[6]))) ? sprintf("%06d", $tmp[6]) : "",
                                  "SEQ"             => $tmp[8],
                                  "CUTTING_MARK"    => $tmp[9],
                                  "CUTTING_LOW"     => $tmp[10],
                                  "CUTTING_HIGH"    => $tmp[11]
                                );
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
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)
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
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (!strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
            }

            //数値チェック
            if (in_array($key, array("YEAR","GRADE","CLASSCD","CURRICULUM_CD","SUBCLASSCD","SEQ","CUTTING_LOW","CUTTING_HIGH"))) {
                if ($data[$key] != '' && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }

        //存在チェック
        $chk_data = array();
        $chk_data[] = array("QUERY"    => knjz210mQuery::checkIbprgCourse($data),
                            "MSG"      => "IBコース");
        $chk_data[] = array("QUERY"    => knjz210mQuery::checkSubclasscd($data),
                            "MSG"      => "科目コード");

        for ($i=0; $i < get_count($chk_data); $i++) {
            if ($db->getOne($chk_data[$i]["QUERY"]) == "0") {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $chk_data[$i]["MSG"]);
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjz210mQuery::insertQueryErr($db, $line, $checkError);
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
}
