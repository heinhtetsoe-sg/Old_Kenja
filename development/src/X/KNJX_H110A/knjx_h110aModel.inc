<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx_h110aModel extends Model
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
    public function knjx_h110aModel()
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

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組一覧
                             "STUDENT"          =>  VARS::post("STUDENT"),           //生徒一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
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
                knjx_h110aQuery::insertQueryCsv($this, $this->data_arr);
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
                $checkdata = array("YEAR"               => array(4,"年度"),
                                   "SCHREGNO"           => array(8,"学籍番号"),
                                   "REGDDATE"           => array(10,"取得日付"),
                                   "CLASSCD"            => array(2,"教科コード"),
                                   "SCHOOL_KIND"        => array(1,"学校種別"),
                                   "CURRICULUM_CD"      => array(1,"教育課程コード"),
                                   "SUBCLASSCD"         => array(6,"科目コード"),
                                   "CONDITION_DIV"      => array(1,"区分コード"),
                                   "CONTENTS"           => array(2,"資格内容コード"),
                                   "REMARK"             => array(90,"備考"),
                                   "CREDITS"            => array(2,"単位数"));
            } else {
                $checkdata = array("YEAR"               => array(4,"年度"),
                                   "SCHREGNO"           => array(8,"学籍番号"),
                                   "REGDDATE"           => array(10,"取得日付"),
                                   "SUBCLASSCD"         => array(6,"科目コード"),
                                   "CONDITION_DIV"      => array(1,"区分コード"),
                                   "CONTENTS"           => array(2,"資格内容コード"),
                                   "REMARK"             => array(90,"備考"),
                                   "CREDITS"            => array(2,"単位数"));
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
                        $data = array("YEAR"                => (strlen(trim($tmp[4])))  ? sprintf("%04d", $tmp[4])  : "",
                                      "SCHREGNO"            => (strlen(trim($tmp[5])))  ? sprintf("%08d", $tmp[5])  : "",
                                      "REGDDATE"            => $tmp[6],
                                      "CLASSCD"             => (strlen(trim($tmp[7])))  ? sprintf("%02d", $tmp[7])  : "",
                                      "SCHOOL_KIND"         => $tmp[8],
                                      "CURRICULUM_CD"       => (strlen(trim($tmp[9])))  ? sprintf("%01d", $tmp[9])  : "",
                                      "SUBCLASSCD"          => (strlen(trim($tmp[10]))) ? sprintf("%06d", $tmp[10])  : "",
                                      "CONDITION_DIV"       => $tmp[12],
                                      "CONTENTS"            => (strlen(trim($tmp[14]))) ? sprintf("%02d", $tmp[14]) : "",
                                      "REMARK"              => $tmp[16],
                                      "CREDITS"             => $tmp[17]);
                    } else {
                        $data = array("YEAR"                => (strlen(trim($tmp[4])))  ? sprintf("%04d", $tmp[4])  : "",
                                      "SCHREGNO"            => (strlen(trim($tmp[5])))  ? sprintf("%08d", $tmp[5])  : "",
                                      "REGDDATE"            => $tmp[6],
                                      "SUBCLASSCD"          => (strlen(trim($tmp[7])))  ? sprintf("%06d", $tmp[7])  : "",
                                      "CONDITION_DIV"       => $tmp[9],
                                      "CONTENTS"            => (strlen(trim($tmp[11]))) ? sprintf("%02d", $tmp[11]) : "",
                                      "REMARK"              => $tmp[13],
                                      "CREDITS"             => $tmp[14]);
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

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $valuesErrFlg       = false;    //値エラーフラグ
        $dateFormatErrFlg   = false;    //日付(フォーマット)エラーフラグ
        $masterErrFlg       = false;    //マスタの存在エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $valuesErrMsg       = "";       //値エラーメッセージ
        $dateFormatErrMsg   = "";       //日付(フォーマット)エラーメッセージ
        $masterErrMsg       = "";       //マスタの存在エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $valuesSep       = "";
        $dateFormatSep   = "";
        $masterSep       = "";

        //学校種別取得
        $school_kind = $db->getCol(knjx_h110aQuery::getSchoolKind());
        //教育課程コード取得
        $curriculum_cd = $db->getCol(knjx_h110aQuery::getCurriculumCd());

        foreach ($data_each as $key => $val) {
            if ($this->Properties["useCurriculumcd"] == '1') {
                //必須項目チェック
                if (in_array($key, array("YEAR","SCHREGNO","REGDDATE","CLASSCD","SCHOOL_KIND","CURRICULUM_CD","SUBCLASSCD","CONDITION_DIV","CONTENTS"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //数値チェック
                if (in_array($key, array("YEAR","SCHREGNO","CLASSCD","CURRICULUM_CD","SUBCLASSCD","CREDITS"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
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
                if (in_array($key, array("YEAR","SCHREGNO","REGDDATE","SUBCLASSCD","CONDITION_DIV","CONTENTS"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //数値チェック
                if (in_array($key, array("YEAR","SCHREGNO","SUBCLASSCD","CREDITS"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }

            //値チェック
            if (in_array($key, array("CONDITION_DIV"))) {
                if ($data[$key] != "" && $data[$key] != "1") {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }
            }

            //日付チェック
            if (in_array($key, array("REGDDATE"))) {
                if (common::isDate($data[$key]) == false) {
                    $returnFlg = $this->errSet($dateFormatErrFlg, $dateFormatErrMsg, "※日付(フォーマット):", $dateFormatSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }

            //存在チェック
            if (in_array($key, array("CONTENTS"))) {
                if ($data[$key] != "") {
                    $query = "SELECT COUNT(*) FROM NAME_MST WHERE NAMECD1 = 'H305' AND NAMECD2 = '".$data[$key]."'";
                    $cnt = $db->getOne($query);
                    if ($cnt < 1) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                    }
                }
            }
        }

        //マスタの存在チェック
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (0 == $db->getOne(knjx_h110aQuery::checkClassMst($this, $data))) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "教科コード");
            }
        }
        if (0 == $db->getOne(knjx_h110aQuery::checkSubclassMst($this, $data))) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "科目コード");
        }

        if (!$returnFlg) {
            $checkError = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg.$dateFormatErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_h110aQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    public function dateRep($data)
    {
        if ($data!="") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
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
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        if ($this->Properties["useCurriculumcd"] == '1') {
            //ヘッダデータ
            $header = array("GRADE"                 => "学年",
                            "HR_CLASS"              => "クラス",
                            "ATTENDNO"              => "出席番号",
                            "NAME"                  => "氏名",
                            "YEAR"                  => "※年度",
                            "SCHREGNO"              => "※学籍番号",
                            "REGDDATE"              => "※取得日付",
                            "CLASSCD"               => "※教科コード",
                            "SCHOOL_KIND"           => "※学校種別",
                            "CURRICULUM_CD"         => "※教育課程コード",
                            "SUBCLASSCD"            => "※科目コード",
                            "SUBCLASSNAME"          => "科目名称",
                            "CONDITION_DIV"         => "※区分コード",
                            "CONDITION_DIV_NAME"    => "区分名称",
                            "CONTENTS"              => "※資格内容コード",
                            "CONTENTS_NAME"         => "資格内容",
                            "REMARK"                => "備考",
                            "CREDITS"               => "単位数",
                            $this->lastColumn       => $this->lastColumn);

            //学校種別ヘッダー例取得
            $school_kind_rei = "";
            $result = $db->query(knjx_h110aQuery::getSchoolKind());
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
            $header_rei = array("GRADE"                 => "01",
                                "HR_CLASS"              => "001",
                                "ATTENDNO"              => "001",
                                "NAME"                  => "アルプ太郎",
                                "YEAR"                  => CTRL_YEAR,
                                "SCHREGNO"              => "12345678",
                                "REGDDATE"              => CTRL_DATE,
                                "CLASSCD"               => "12",
                                "SCHOOL_KIND"           => $school_kind_rei,
                                "CURRICULUM_CD"         => "2",
                                "SUBCLASSCD"            => "123456",
                                "SUBCLASSNAME"          => "情報Ａ",
                                "CONDITION_DIV"         => "1",
                                "CONDITION_DIV_NAME"    => "増加単位認定",
                                "CONTENTS"              => "06",
                                "CONTENTS_NAME"         => "簿記検定2級",
                                "REMARK"                => "備考",
                                "CREDITS"               => "1",
                                $this->lastColumn       => $this->lastColumn);
        } else {
            //ヘッダデータ
            $header = array("GRADE"                 => "学年",
                            "HR_CLASS"              => "クラス",
                            "ATTENDNO"              => "出席番号",
                            "NAME"                  => "氏名",
                            "YEAR"                  => "※年度",
                            "SCHREGNO"              => "※学籍番号",
                            "REGDDATE"              => "※取得日付",
                            "SUBCLASSCD"            => "※科目コード",
                            "SUBCLASSNAME"          => "科目名称",
                            "CONDITION_DIV"         => "※区分コード",
                            "CONDITION_DIV_NAME"    => "区分名称",
                            "CONTENTS"              => "※資格内容コード",
                            "CONTENTS_NAME"         => "資格内容",
                            "REMARK"                => "備考",
                            "CREDITS"               => "単位数",
                            $this->lastColumn       => $this->lastColumn);
            //ヘッダ例
            $header_rei = array("GRADE"                 => "01",
                                "HR_CLASS"              => "001",
                                "ATTENDNO"              => "001",
                                "NAME"                  => "アルプ太郎",
                                "YEAR"                  => CTRL_YEAR,
                                "SCHREGNO"              => "20060001",
                                "REGDDATE"              => CTRL_DATE,
                                "SUBCLASSCD"            => "200001",
                                "SUBCLASSNAME"          => "情報Ａ",
                                "CONDITION_DIV"         => "1",
                                "CONDITION_DIV_NAME"    => "増加単位認定",
                                "CONTENTS"              => "06",
                                "CONTENTS_NAME"         => "簿記検定2級",
                                "REMARK"                => "備考",
                                "CREDITS"               => "1",
                                $this->lastColumn       => $this->lastColumn);
        }

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $tables = array();
            $tables = array("1" => "SCHREG_QUALIFIED_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            $header = array();
            $header["YEAR"]     = "年度";
            $header["SCHREGNO"] = "学籍番号";
            $header["SEQ"]      = "SEQ（連番)";

            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
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
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx_h110aQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {                //データ
                    $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
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
            if ($this->field["OUTPUT"] == "3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "資格データ（増加単位）";
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
