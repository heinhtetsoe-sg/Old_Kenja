<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjm431Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
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
    public function knjm431Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //登録者コード
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
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目一覧
                             "HEADER"           =>  VARS::post("HEADER")            //ヘッダ有
                            );

        $this->cmd          = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
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
        //ファイル取り込み
        $this->dataArr = array();
        $this->dataArr = $this->fileUpload();

        //データの整合性チェック
        if ($this->isDataMatched($this->dataArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    //講座データ
                    $query = knjm431Query::getExistCheck($this->dataArr[$i], $this);
                    $result = $db->query($query);
                    $flg = false;
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $flg = true;
                    }
                    //履修単位・修得単位を更新する為単位マスタから単位数を取得する
                    $queryC = knjm431Query::getCredits($this->dataArr[$i], $this);
                    $resultC = $db->query($queryC);
                    $rowC = $resultC->fetchRow(DB_FETCHMODE_ASSOC);
                    //更新
                    if ($flg) {   //ＤＢにあれば、update
                        knjm431Query::updateQueryCsv($db, $this->dataArr[$i], $this->staffcd, $rowC["CREDITS"], $this);
                    } else {    //ＤＢになければ、insert
                        knjm431Query::insertQueryCsv($db, $this->dataArr[$i], $this->staffcd, $rowC["CREDITS"], $this);
                    }
                }
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
            }
        }
    }

    //取込
    public function fileUpload()
    {
        $tempArr = array();
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            if ($this->Properties["useCurriculumcd"] == "1") {
                $checkdata = array("YEAR"               => array(4, "年度"),
                                    "SCHREGNO"          => array(8, "学籍番号"),
                                    "CLASSCD"           => array(2, "教科コード"),
                                    "SCHOOL_KIND"       => array(2, "校種"),
                                    "CURRICULUM_CD"     => array(2, "教育課程"),
                                    "SUBCLASSCD"        => array(6, "科目コード"),
                                    "SEM1_TERM_SCORE"   => array(3, "前期素点"),
                                    "SEM1_VALUE"        => array(3, "前期評価"),
                                    "SEM2_TERM_SCORE"   => array(3, "認定素点"),
                                    "SEM2_VALUE"        => array(3, "認定評価"),
                                    "GRAD_VALUE"        => array(3, "学年評定"));
            } else {
                $checkdata = array("YEAR"              => array(4, "年度"),
                                    "SCHREGNO"          => array(8, "学籍番号"),
                                    "SUBCLASSCD"        => array(6, "科目コード"),
                                    "SEM1_TERM_SCORE"   => array(3, "前期素点"),
                                    "SEM1_VALUE"        => array(3, "前期評価"),
                                    "SEM2_TERM_SCORE"   => array(3, "認定素点"),
                                    "SEM2_VALUE"        => array(3, "認定評価"),
                                    "GRAD_VALUE"        => array(3, "学年評定"));
            }
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $i = 0;
            $record_no = 1;         //行番号(初期値)
            //取得主キー
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('RECORD_DAT');
            $fp = fopen($this->field["FILE"]["tmp_name"], "r");        //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                       //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                             //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"]=="on" && $record_no<2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");     //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    if ($this->Properties["useCurriculumcd"] == "1") {
                        $data = array("YEAR"            => trim($tmp[0]),
                                      "SCHREGNO"        => trim($tmp[1]),
                                      "NAME"            => trim($tmp[2]),
                                      "HR_NAME"         => trim($tmp[3]),
                                      "ATTENDNO"        => trim($tmp[4]),
                                      "CLASSCD"         => trim($tmp[5]),
                                      "SCHOOL_KIND"     => trim($tmp[6]),
                                      "CURRICULUM_CD"   => trim($tmp[7]),
                                      "SUBCLASSCD"      => trim($tmp[8]),
                                      "SUBCLASSNAME"    => trim($tmp[9]),
                                      "SEM1_TERM_SCORE" => trim($tmp[10]),
                                      "SEM1_VALUE"      => trim($tmp[11]),
                                      "SEM2_TERM_SCORE" => trim($tmp[12]),
                                      "SEM2_VALUE"      => trim($tmp[13]),
                                      "GRAD_VALUE"      => trim($tmp[14]),
                                      "TAKESEMES"       => "0",
                                      $this->lastColumn => trim($tmp[15]));
                    } else {
                        $data = array("YEAR"            => trim($tmp[0]),
                                      "SCHREGNO"        => trim($tmp[1]),
                                      "NAME"            => trim($tmp[2]),
                                      "HR_NAME"         => trim($tmp[3]),
                                      "ATTENDNO"        => trim($tmp[4]),
                                      "SUBCLASSCD"      => trim($tmp[8]),
                                      "SUBCLASSNAME"    => trim($tmp[9]),
                                      "SEM1_TERM_SCORE" => trim($tmp[10]),
                                      "SEM1_VALUE"      => trim($tmp[11]),
                                      "SEM2_TERM_SCORE" => trim($tmp[12]),
                                      "SEM2_VALUE"      => trim($tmp[13]),
                                      "GRAD_VALUE"      => trim($tmp[14]),
                                      "TAKESEMES"       => "0",
                                      $this->lastColumn => trim($tmp[15]));
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $record_no, $duplicateCsv1, $primaryKey1)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$record_no][$val] = $data[$val];
                    }
                }//ifの綴り
                $record_no++;   //行番号(カウント)
            }//whileの綴り
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        } else {
            $this->warning = "ファイル名が不正です。";
        }
        return $tempArr;
    }

    //エラーチェック
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg      = true;
        $hissuErrFlg    = false;  //必須エラーフラグ
        $numericErrFlg  = false;  //数値エラーフラグ
        $lengthErrFlg   = false;  //レングスエラーフラグ
        $masterErrFlg   = false; //存在エラーフラグ
        $duplicateCsvErrFlg = false; //重複(CSV)エラーフラグ
        $valuesErrFlg    = false;    //値エラーフラグ

        $hissuErrMsg    = "";     //必須エラーメッセージ
        $numericErrMsg  = "";     //数値エラーメッセージ
        $lengthErrMsg   = "";     //レングスエラーメッセージ
        $masterErrMsg   = "";    //存在エラーメッセージ
        $duplicateCsvErrMsg = ""; //重複(CSV)メッセージ
        $valuesErrMsg    = "";       //値エラーメッセージ

        $hissuSep       = "";
        $numericSep     = "";
        $lengthSep      = "";
        $masterSep      = "";
        $duplicateCsvSep= "";
        $valuesSep      = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR", "SCHREGNO", "SUBCLASSCD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //桁数チェック
            if (in_array($key, array("YEAR", "SCHREGNO", "SUBCLASSCD", "SEM1_TERM_SCORE", "SEM2_TERM_SCORE", "SEM1_VALUE", "SEM2_VALUE", "GRAD_VALUE"))) {
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }

                //数字チェック
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //値チェック
            if (in_array($key, array("SEM1_TERM_SCORE", "SEM2_TERM_SCORE", "SEM1_VALUE", "SEM2_VALUE", "GRAD_VALUE"))) {
                if (($data[$key] != "" && is_numeric($data[$key]) && $data[$key] > 100 )) {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }
            }

            if ($this->Properties["useCurriculumcd"] == "1") {
                if (in_array($key, array("SCHOOL_KIND", "CURRICULUM_CD", "CLASSCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }

                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }

                if (in_array($key, array("CURRICULUM_CD", "CLASSCD"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }
        }
        if ($returnFlg) {
            //０埋め
            $data = $this->zeroUme($data);
            foreach ($checkdata as $key => $val) {
                //1.在籍データなし
                if (in_array($key, array("SCHREGNO")) && $data[$key] != "") {
                    $query = knjm431Query::getMasterCheck($data, "1", $this);
                    $chkdt = $db->getOne($query);
                    if ($chkdt == null) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                    }
                }
                //2.科目マスタ
                if (in_array($key, array("SUBCLASSCD")) && $data[$key] != "") {
                    $query = knjm431Query::getMasterCheck($data, "2", $this);
                    $chkdt = $db->getOne($query);
                    if ($chkdt == null) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                    }
                }

                if ($this->Properties["useCurriculumcd"] == "1") {
                    //3.教育課程 ※教育課程(名称マスタZ018)なし:
                    if (in_array($key, array("CURRICULUM_CD")) && $data["CURRICULUM_CD"]!="") {
                        $query = knjm431Query::getMasterCheck($data, "3", $this);
                        $chkdt = $db->getOne($query);
                        if ($chkdt == null) {
                            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                        }
                    }
                    //4.校種 ※学籍学年データなし:
                    if (in_array($key, array("SCHOOL_KIND")) && $data["SCHOOL_KIND"]!="") {
                        $query = knjm431Query::getMasterCheck($data, "4", $this);
                        $chkdt = $db->getOne($query);
                        if ($chkdt == null) {
                            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                        }
                    }
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$masterErrMsg.$numericErrMsg.$valuesErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjm431Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        if ($this->Properties["useCurriculumcd"] == "1") {
            $header = array("YEAR"              => "※年度",
                            "SCHREGNO"          => "※学籍番号",
                            "NAME"              => "氏名",
                            "HR_NAME"           => "クラス",
                            "ATTENDNO"          => "番号",
                            "CLASSCD"           => "※教科コード",
                            "SCHOOL_KIND"       => "※校種",
                            "CURRICULUM_CD"     => "※教育課程",
                            "SUBCLASSCD"        => "※科目コード",
                            "SUBCLASSNAME"      => "科目名",
                            "SEM1_TERM_SCORE"   => "前期素点",
                            "SEM1_VALUE"        => "前期評価",
                            "SEM2_TERM_SCORE"   => "認定素点",
                            "SEM2_VALUE"        => "認定評価",
                            "GRAD_VALUE"        => "学年評定",
                            $this->lastColumn   => $this->lastColumn);
            $header_rei = array("YEAR"              => "2005",
                                "SCHREGNO"          => "99999999",
                                "NAME"              => "サンプル　太郎",
                                "HR_NAME"           => "1年1組",
                                "ATTENDNO"          => "999",
                                "CLASSCD"           => "99",
                                "SCHOOL_KIND"       => "9",
                                "CURRICULUM_CD"     => "9",
                                "SUBCLASSCD"        => "999999",
                                "SUBCLASSNAME"      => "国語",
                                "SEM1_TERM_SCORE"   => "999",
                                "SEM1_VALUE"        => "999",
                                "SEM2_TERM_SCORE"   => "999",
                                "SEM2_VALUE"        => "999",
                                "GRAD_VALUE"        => "99",
                                $this->lastColumn   => $this->lastColumn);
        } else {
            $header = array("YEAR"              => "※年度",
                            "SCHREGNO"          => "※学籍番号",
                            "NAME"              => "氏名",
                            "HR_NAME"           => "クラス",
                            "ATTENDNO"          => "番号",
                            "SUBCLASSCD"        => "※科目コード",
                            "SUBCLASSNAME"      => "科目名",
                            "SEM1_TERM_SCORE"   => "前期素点",
                            "SEM1_VALUE"        => "前期評価",
                            "SEM2_TERM_SCORE"   => "認定素点",
                            "SEM2_VALUE"        => "認定評価",
                            "GRAD_VALUE"        => "学年評定",
                            $this->lastColumn   => $this->lastColumn);
            $header_rei = array("YEAR"              => "2005",
                                "SCHREGNO"          => "99999999",
                                "NAME"              => "サンプル　太郎",
                                "HR_NAME"           => "1年1組",
                                "ATTENDNO"          => "999",
                                "SUBCLASSCD"        => "999999",
                                "SUBCLASSNAME"      => "国語",
                                "SEM1_TERM_SCORE"   => "999",
                                "SEM1_VALUE"        => "999",
                                "SEM2_TERM_SCORE"   => "999",
                                "SEM2_VALUE"        => "999",
                                "GRAD_VALUE"        => "99",
                                $this->lastColumn   => $this->lastColumn);
        }

        $contents  = "";    //クリア

        if ($this->field["OUTPUT"]=="1") {             //ヘッダ
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {     //エラー
            $duplicateHeader = array("YEAR"             => "※年度",
                                    "SCHREGNO"          => "※学籍番号",
                                    "CLASSCD"           => "※教科コード",
                                    "SCHOOL_KIND"       => "※校種",
                                    "CURRICULUM_CD"     => "※教育課程",
                                    "SUBCLASSCD"        => "※科目コード",
                                    "TAKESEMES"         => "履修学期");
            $tables = array();
            $tables = array("1" => "RECORD_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $duplicateHeader);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4") {     //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header, ",")."\n";
            }
        }
        if ($this->field["OUTPUT"]=="1") {         //ヘッダ
            $header_data = true;
        } else {                                //ヘッダ以外
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            $table = "";
            $where = "";
            if ($this->field["OUTPUT"]=="3") {             //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {     //データ
                $query = knjm431Query::getselectDataSQL($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
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
                $couse = "成績データ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //値チェック
    public function ataiCheck($data, $atai)
    {
        $flg = true;

        if ($data!="") {
            if (!in_array($data, $atai)) {
                $flg = false;
            }
        }

        return $flg;
    }

    /**
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    public function zeroUme($data)
    {
        //更新
        if ($data["SUBCLASSCD"]!="") {
            if (strlen($data["SUBCLASSCD"]) < 6) {
                $data["SUBCLASSCD"] = sprintf("%06d", $data["SUBCLASSCD"]);
            }
        }

        if ($this->Properties["useCurriculumcd"] == "1" && $data["CLASSCD"]!="") {
            if (strlen($data["CLASSCD"]) < 2) {
                $data["CLASSCD"] = sprintf("%02d", $data["CLASSCD"]);
            }
        }

        return $data;
    }

    //データの整合性チェック
    public function isDataMatched(&$dataArr)
    {
        //１万件を超えたら制限エラー
        if (get_count($dataArr) > 10000) {
            $this->setWarning(MSG915, "処理件数を１万件以下にして下さい 処理件数：".get_count($dataArr)."件");
            return false;
        }

        return true;
    }
}
