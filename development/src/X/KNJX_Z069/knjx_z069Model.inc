<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx_z069Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $candidate = array();
    public $schoolkind;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx_z069Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    public function init()
    {
        $this->field = array(
            "OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
            "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
            "GRADE"            =>  VARS::post("GRADE"),             //学年一覧
            "SUBCLASS"         =>  VARS::post("SUBCLASS"),          //科目一覧
            "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
            "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
        );

        $this->cmd = VARS::request("cmd");
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
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx_z069Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_z069Query::deleteQueryCsv($this, $this->data_arr);
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
            $checkdata = array();
            $checkdata["YEAR"]             = array(4,  "HISSU",  "INT",  "※年度");
            $checkdata["SEMESTER"]         = array(4,  "HISSU",  "INT",  "※学期");
            $checkdata["GRADE"]            = array(2,  "HISSU",  "INT",  "※学年");
            $checkdata["CLASSCD"]          = array(2,  "HISSU",  "INT",  "※教科コード");
            $checkdata["SCHOOL_KIND"]      = array(2,  "HISSU",  "",     "※学校種別");
            $checkdata["CURRICULUM_CD"]    = array(2,  "HISSU",  "INT",  "※教育課程コード");
            $checkdata["SUBCLASSCD"]       = array(6,  "HISSU",  "INT",  "※科目コード");
            $checkdata["ELEMENT_DIV"]      = array(1,  "HISSU",  "INT",  "※評価の要素区分");
            $checkdata["ELEMENT_CD"]       = array(3,  "HISSU",  "INT",  "※評価の要素コード");
            $checkdata["SORT"]             = array(1,  "",       "INT",  "表示順");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey('PYP_ELEMENT_DAT');
            $maxCntArray = array();
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

                    $data = array();
                    $data["YEAR"]          = $tmp[0];
                    $data["SEMESTER"]      = $tmp[1];
                    $data["GRADE"]         = $tmp[2];
                    $data["CLASSCD"]       = $tmp[3];
                    $data["SCHOOL_KIND"]   = $tmp[4];
                    $data["CURRICULUM_CD"] = $tmp[5];
                    $data["SUBCLASSCD"]    = $tmp[6];
                    $data["ELEMENT_DIV"]   = $tmp[7];
                    $data["ELEMENT_CD"]    = $tmp[8];
                    $data["SORT"]          = $tmp[9];

                    $cntKey = $data["YEAR"]."-".$data["SEMESTER"]."-".$data["GRADE"]."-".$data["CLASSCD"]."-".$data["SCHOOL_KIND"]."-".$data["CURRICULUM_CD"]."-".$data["SUBCLASSCD"]."-".$data["ELEMENT_DIV"];
                    if (!array_key_exists($cntKey, $maxCntArray)) {
                        $query = knjx_z069Query::getPypElementDat($data);
                        $result = $db->query($query);
                        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $maxCntArray[$cntKey][] = $row["ELEMENT_CD"];
                        }
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $maxCntArray, $cntKey, $duplicateCsv1, $primaryKey1)) {
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
    public function errCheck($db, $data, $data_each, $line, &$maxCntArray, $cntKey, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $orderErrFlg        = false;    //その他エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $orderErrMsg        = "";       //その他エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $orderSep        = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //数値チェック
            if (($val[2] == "INT") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                }
            }
        }

        //存在チェック 学期
        $sem_check = $db->getCol(knjx_z069Query::checkSemester($data["YEAR"]));
        if (!in_array($data["SEMESTER"], $sem_check)) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された学期データがありません。");
        }
        //存在チェック 学年
        $grade_check = $db->getOne(knjx_z069Query::checkGrade($data["YEAR"], $data["SEMESTER"], $data["GRADE"]));
        if ($grade_check == 0) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された学年の講座データがありません。");
        }
        //存在チェック 科目
        $subclass_check = $db->getOne(knjx_z069Query::checkSubclass($data["YEAR"], $data["SEMESTER"], $data["GRADE"], $data["CLASSCD"], $data["SCHOOL_KIND"], $data["CURRICULUM_CD"], $data["SUBCLASSCD"]));
        if ($subclass_check == 0) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された学年・科目の講座データがありません。");
        }
        //存在チェック 評価の要素コード
        $subclass_check = $db->getOne(knjx_z069Query::checkElementCd($data["ELEMENT_DIV"], $data["ELEMENT_CD"]));
        if ($subclass_check == 0) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された評価の要素データがありません。");
        }
        //登録制限チェック
        $element_cd_cnt = $db->getOne(knjx_z069Query::checkPypElementCd($data["YEAR"], $data["SEMESTER"], $data["GRADE"], $data["CLASSCD"], $data["SCHOOL_KIND"], $data["CURRICULUM_CD"], $data["SUBCLASSCD"], $data["ELEMENT_DIV"], $data["ELEMENT_CD"]));
        if ($element_cd_cnt == 0) {
            $maxCntArray[$cntKey][] = $data["ELEMENT_CD"];
            if (get_count($maxCntArray[$cntKey]) > 6) {
                $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "制限数オーバー");
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg.$orderErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_z069Query::insertQueryErr($db, $line, $checkError);
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
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");
        //ヘッダデータ
        $header = array();
        $header["YEAR"]            = "※年度";
        $header["SEMESTER"]        = "※学期";
        $header["GRADE"]           = "※学年";
        $header["CLASSCD"]         = "※教科コード";
        $header["SCHOOL_KIND"]     = "※学校種別";
        $header["CURRICULUM_CD"]   = "※教育課程コード";
        $header["SUBCLASSCD"]      = "※科目コード";
        $header["ELEMENT_DIV"]     = "※評価の要素区分";
        $header["ELEMENT_CD"]      = "※評価の要素コード";
        $header["SORT"]            = "表示順";
        $header[$this->lastColumn] = $this->lastColumn;

        $header_rei = array();
        $header_rei["YEAR"]             = CTRL_YEAR;
        $header_rei["SEMESTER"]         = CTRL_SEMESTER;
        $header_rei["GRADE"]            = "1";
        $header_rei["CLASSCD"]          = "12";
        $header_rei["SCHOOL_KIND"]      = "H";
        $header_rei["CURRICULUM_CD"]    = "1";
        $header_rei["SUBCLASSCD"]       = "123456";
        $header_rei["ELEMENT_DIV"]      = "1";
        $header_rei["ELEMENT_CD"]       = "123";
        $header_rei["SORT"]             = "1";
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
            $tables = array();
            $tables = array("1" => "PYP_ELEMENT_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
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
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knjx_z069Query::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {                //データ
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                    $array = $header;
                } else {
                    $array = $header_err;
                }

                //ヘッダ順に値をセット
                $comma = "";
                foreach ($array as $key => $headerName) {
                    $contents .= $comma.$row[$key];
                    $comma = ",";
                }
                $contents .= "\n";

                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "評価の要素データ";
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
