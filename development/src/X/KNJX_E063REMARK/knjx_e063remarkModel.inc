<?php

require_once('for_php7.php');
class knjx_e063remarkModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $schoolName;
    public $schoolCode;
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx_e063remarkModel()
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
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->grade);
            unset($this->hr_class);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS")) {
            $this->hr_class = VARS::get("HR_CLASS");
        }

        $this->field = array("OUTPUT"       =>  VARS::post("OUTPUT"),   //出力取込種別
                             "HEADER"       =>  VARS::post("HEADER"),   //ヘッダ有
                             "HR_NAME"      =>  VARS::post("HR_NAME")   //クラス名
                             );

        $this->cmd  = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        //学校名取得
        $query = knjx_e063remarkquery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);

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
            $errFlg = $this->checkCsvErrQuery('KNJX_E063');
            if ($errFlg) {
                knjx_e063remarkQuery::insertQueryCsv($this, $this->data_arr);
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
            $checkdata["YEAR"]          = array(4,  "年度");
            $checkdata["SCHREGNO"]      = array(8,  "学籍番号");
            $checkdata["ANNUAL"]        = array(2,  "学年（年次）");
            $checkdata["CLASSCD"]       = array(5,  "教科");
            $checkdata["CURRICULUM_CD"] = array(2,  "教育課程");
            $checkdata["SUBCLASSCD"]    = array(6,  "科目");
            $checkdata["REMARK"]        = array(150,"備考");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr('KNJX_E063');
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey('STUDYRECREMARK_DAT');
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        if ($line == 2) {
                            $header = true;
                        }
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer      = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp         = common::csv2array($buffer);

                    $data = array();
                    $data["YEAR"]           = (strlen(trim($tmp[4]))) ? sprintf("%04d", $tmp[4]) : "";
                    $data["SCHREGNO"]       = (strlen(trim($tmp[5]))) ? sprintf("%08d", $tmp[5]) : "";
                    $data["ANNUAL"]         = (strlen(trim($tmp[6]))) ? sprintf("%02d", $tmp[6]) : "";

                    $tmpVal = array();
                    if ($tmp[7]) {
                        $tmpVal = explode(':', $tmp[7]);
                    }
                    $data["CLASSCD"]        = $tmpVal[0];

                    $data["CURRICULUM_CD"]  = $tmp[8];

                    $tmpVal = array();
                    if ($tmp[9]) {
                        $tmpVal = explode(':', $tmp[9]);
                    }
                    $data["SUBCLASSCD"]     = $tmpVal[0];

                    $data["REMARK"]         = $tmp[10];
                    $data["SCHOOL_KIND"]    = "";

                    //項目数
                    $tmpCnt = 24;
                    if ($this->Properties["useProvFlg"] == '1') {
                        $tmpCnt += 3;
                    }
                    $checkCntFlg = get_count($tmp) == $tmpCnt ? true : false;

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $checkCntFlg, $duplicateCsv1, $primaryKey1)) {
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
    public function errCheck($db, $data, $data_each, $line, $checkCntFlg, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $colCntErrFlg       = false;    //フィールド数エラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $colCntErrMsg       = "";       //フィールド数エラー
        $duplicateCsvErrMsg = "";

        $hissuSep        = "";
        $masterSep       = "";
        $numericSep      = "";
        $lengthSep       = "";
        $colCntSep       = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        if (!$checkCntFlg) {
            $returnFlg = $this->errSet($colCntErrFlg, $colCntErrMsg, "※項目数エラー:", $colCntSep, "データ出力をしてみて下さい。");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR","SCHREGNO","ANNUAL"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR","SCHREGNO","ANNUAL"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //サイズチェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }


            //存在チェック
            if (in_array($key, array("SCHREGNO"))) {
                $query = knjx_e063remarkQuery::checkSchregNo($this, $data["SCHREGNO"]);
                if ($query) {
                    $cnt = $db->getOne($query);
                    if ($cnt < 1) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                    }
                }
            }
        }

        //存在チェック
        $query = knjx_e063remarkQuery::checkSchregStudyrecDat($data);
        $cnt = $db->getOne($query);
        if ($cnt < 1) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指導要録・調査書成績データがありません");
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg.$colCntErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_e063remarkQuery::insertQueryErr($db, $line, $checkError);
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
        /************/
        /* ヘッダー */
        /************/
        $header = array();
        if ($this->field["OUTPUT"] == "2") {
            $header["MSGROW"]           = "行番号";
            $header["MSGREMARK"]        = "メッセージ";
        } else {
            $header["GRADE"]            = array("",         "年");
            $header["HR_CLASS"]         = array("",         "組");
            $header["ATTENDNO"]         = array("",         "出席番号");
            $header["NAME"]             = array("",         "氏名");
            $header["YEAR"]             = array("",         "※年度");
            $header["SCHREGNO"]         = array("",         "※学籍番号");
            $header["ANNUAL"]           = array("",         "※学年（年次）");
            $header["CLASSNAME"]        = array("",         "※教科");
            $header["CURRICULUM_CD"]    = array("",         "※教育課程");
            $header["SUBCLASSNAME"]     = array("",         "※科目");
            $header["REMARK"]           = array("備考",     "※備考のみ取込します。");
            if ($this->Properties["useProvFlg"] == '1') {
                $header["PROV_FLG0"]        = array("在籍中",   "仮評定");
            }
            $header["VALUATION0"]       = array("在籍中",   "評定");
            $header["GET_CREDIT0"]      = array("在籍中",   "修得単位");
            $header["ADD_CREDIT0"]      = array("在籍中",   "増加単位");
            $header["COMP_CREDIT0"]     = array("在籍中",   "履修単位");
            if ($this->Properties["useProvFlg"] == '1') {
                $header["PROV_FLG1"]        = array("在籍前",   "仮評定");
            }
            $header["VALUATION1"]       = array("在籍前",   "評定");
            $header["GET_CREDIT1"]      = array("在籍前",   "修得単位");
            $header["ADD_CREDIT1"]      = array("在籍前",   "増加単位");
            $header["COMP_CREDIT1"]     = array("在籍前",   "履修単位");
            if ($this->Properties["useProvFlg"] == '1') {
                $header["PROV_FLG2"]        = array("高認試験", "仮評定");
            }
            $header["VALUATION2"]       = array("高認試験", "評定");
            $header["GET_CREDIT2"]      = array("高認試験", "修得単位");
            $header["ADD_CREDIT2"]      = array("高認試験", "増加単位");
            $header["COMP_CREDIT2"]     = array("高認試験", "履修単位");
            $header[$this->lastColumn]  = array("",         $this->lastColumn);
        }

        $contents  = "";    //クリア

        $dataFlg = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "2") {
            $headerDuplicate = array("YEAR"          => "年度",
                                     "SCHREGNO"      => "学籍番号",
                                     "CLASSCD"       => "教科コード",
                                     "SCHOOL_KIND"   => "学校種別",
                                     "CURRICULUM_CD" => "教育課程コード",
                                     "SUBCLASSCD"    => "科目コード");

            $tables = array();
            $tables = array("1" => "STUDYRECREMARK_DAT");
            $dupTable = $this->getDuplicateErrorHeader('KNJX_E063', $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
        }

        //データ
        $h1 = $h2 = array();
        foreach ($header as $key => $val) {
            if ($this->field["OUTPUT"] == "2") {
                $h1[] = $val;
            } elseif ($this->field["HEADER"] == "on") {
                $h1[] = $val[0];
                $h2[] = $val[1];
            }
        }
        if (get_count($h1) > 0) {
            $contents .= implode($h1, ",")."\n";
        }
        if (get_count($h2) > 0) {
            $contents .= implode($h2, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        if ($this->field["OUTPUT"] == "2") {
            //エラー
            $query = $this->selectCsvErrQuery('KNJX_E063');
        } else {
            //データ
            $query = knjx_e063remarkQuery::selectMainQuery($this);
        }

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($this->field["OUTPUT"] == "3") {    //データ
                $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            }
            $sep = "";
            foreach ($header as $key => $val) {
                $contents .= $sep.$row[$key];
                $sep = ",";
            }
            $contents .= "\n";
            $dataFlg = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($dataFlg) {
            if ($this->field["OUTPUT"] == "2") {    //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = $this->field["HR_NAME"]." 学習記録備考データ";
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
