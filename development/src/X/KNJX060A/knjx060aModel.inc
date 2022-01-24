<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx060aModel extends Model
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
    public $keikokutenFlag;    //警告点フラグ

    /**
     * メンバ変数の初期化
     */
    public function knjx060aModel()
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
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "CHAIRCD"          =>  VARS::post("CHAIRCD"),          //講座一覧
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目一覧
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                            );

        $this->cmd = VARS::request("cmd");
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
        $db = Query::dbCheckOut();
        $this->keikokutenFlag = ($db->getOne(knjx060aQuery::getIsKeikokuten($this)) == '1' && $this->Properties["use_school_Detail_009"] == "1");
        Query::dbCheckIn($db);
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
                    $query = knjx060aQuery::getExistCheck($this, $this->dataArr[$i]);
                    $result = $db->query($query);
                    $flg = false;
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $flg = true;
                        $groupcd = $row['GROUPCD'];
                    }
                    //列名称
                    $this->dataArr[$i]['SEQ004_REMARK1'] = $db->getOne(knjx060aQuery::getMasterCheck2($this, $this->dataArr[$i]));
                    //更新
                    if ($this->field["SHORI_MEI"] == "1") {
                        if ($this->dataArr[$i]["GROUPCD"] != '0000' && $groupcd =='0000') {
                            knjx060aQuery::deleteChairClsDat($db, $this->dataArr[$i]);
                        }
                        if ($flg) {   //ＤＢにあれば、update
                            knjx060aQuery::updateQueryCsv($db, $this->dataArr[$i], $this->staffcd, $this);
                        } else {    //ＤＢになければ、insert
                            knjx060aQuery::insertQueryCsv($db, $this->dataArr[$i], $this->staffcd, $this);
                        }
                        //削除
                    } else {
                        //ＤＢにあれば、delete
                        if ($flg) {
                            knjx060aQuery::deleteQueryCsv($db, $this->dataArr[$i]);
                        }
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
        //ファイル名取得
        $target = $_FILES['FILE']['name'];
        $fileInfo = pathinfo($target);
        $kakutyousi = $fileInfo['extension'];
        $kakutyousi = strtoupper($kakutyousi);
        //拡張子チェック
        if ($kakutyousi != "CSV") {
            $this->warning = "CSVファイルを指定して下さい。\\n(".$kakutyousi."ファイルの取込は出来ません。)";
            return false;
        }

        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $checkdata = array("YEAR"               => array(4, "年度"),
                               "SEMESTER"           => array(1, "学期"),
                               "CHAIRCD"            => array(7, "講座コード"),
                               "GROUPCD"            => array(4, "群コード"),
                               "SUBCLASSCD"         => array(6, "科目コード"),
                               "CHAIRNAME"          => array(30, "講座名称"),
                               "CHAIRABBV"          => array(15, "講座略称"),
                               "TAKESEMES"          => array(1, "履修学期"),
                               "LESSONCNT"          => array(2, "週授業回数"),
                               "FRAMECNT"           => array(2, "連続枠数"),
                               "COUNTFLG"           => array(1, "集計フラグ"),
                               "CLASSCD"            => array(2, "教科コード"),
                               "SCHOOL_KIND"        => array(2, "学校種別"),
                               "CURRICULUM_CD"      => array(2, "教育課程コード"),
                               "SEQ004_REMARK1"     => array(150, "列名称"),
                               "SEQ004_REMARK2"     => array(15, "スモールクラス名称"),
                               "SEQ004_REMARK3"     => array(15, "習熟度クラス名称"),
                               "KEIKOKUTEN_SOTEN"   => array(150, "警告点(素点)"),
                               "KEIKOKUTEN_HYOUKA"  => array(150, "警告点(評価)"),
                               "KEIKOKUTEN_HYOUTEI" => array(150, "警告点(評定)"));
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $i = 0;
            $record_no = 1;         //行番号(初期値)
            $primaryKey1 = array();
            $primaryKey2 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $primaryKey1 = $this->getPrimaryKey('CHAIR_DAT');
            $primaryKey2 = $this->getPrimaryKey('CHAIR_DETAIL_DAT');
            $fp = fopen($this->field["FILE"]["tmp_name"], "r");        //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                       //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                             //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"] == "on" && $record_no < 2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");     //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);       //2004/03/26 nakamoto add

                    $tmpCnt = 0;
                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $data = array("YEAR"                => trim($tmp[$tmpCnt]),
                                      "SEMESTER"            => trim($tmp[++$tmpCnt]),
                                      "CHAIRCD"             => trim($tmp[++$tmpCnt]),
                                      "GROUPCD"             => trim($tmp[++$tmpCnt]),
                                      "CLASSCD"             => trim($tmp[++$tmpCnt]),
                                      "SCHOOL_KIND"         => trim($tmp[++$tmpCnt]),
                                      "CURRICULUM_CD"       => trim($tmp[++$tmpCnt]),
                                      "SUBCLASSCD"          => trim($tmp[++$tmpCnt]),
                                      "CHAIRNAME"           => trim($tmp[++$tmpCnt]),
                                      "CHAIRABBV"           => trim($tmp[++$tmpCnt]));
                        if ($this->Properties["chairRetsuMeisho_Hyouji"] == '1') {
                            $data["SEQ004_REMARK1"]         = trim($tmp[++$tmpCnt]);
                            $data["SEQ004_REMARK2"]         = trim($tmp[++$tmpCnt]);
                            $data["SEQ004_REMARK3"]         = trim($tmp[++$tmpCnt]);
                        }
                        $data["TAKESEMES"]                  = trim($tmp[++$tmpCnt]);
                        $data["LESSONCNT"]                  = trim($tmp[++$tmpCnt]);
                        $data["FRAMECNT"]                   = trim($tmp[++$tmpCnt]);
                        $data["COUNTFLG"]                   = trim($tmp[++$tmpCnt]);//集計フラグ---05/01/25Add 1:集計する 0:集計しない---NO001

                        if ($this->keikokutenFlag) {
                            $data["KEIKOKUTEN_SOTEN"]      = trim($tmp[++$tmpCnt]);
                            $data["KEIKOKUTEN_HYOUKA"]     = trim($tmp[++$tmpCnt]);
                            $data["KEIKOKUTEN_HYOUTEI"]    = trim($tmp[++$tmpCnt]);
                        }
                    } else {
                        $data = array("YEAR"                => trim($tmp[$tmpCnt]),
                                      "SEMESTER"            => trim($tmp[++$tmpCnt]),
                                      "CHAIRCD"             => trim($tmp[++$tmpCnt]),
                                      "GROUPCD"             => trim($tmp[++$tmpCnt]),
                                      "SUBCLASSCD"          => trim($tmp[++$tmpCnt]),
                                      "CHAIRNAME"           => trim($tmp[++$tmpCnt]),
                                      "CHAIRABBV"           => trim($tmp[++$tmpCnt]));
                        if ($this->Properties["chairRetsuMeisho_Hyouji"] == '1') {
                            $data["SEQ004_REMARK1"]         = trim($tmp[++$tmpCnt]);
                            $data["SEQ004_REMARK2"]         = trim($tmp[++$tmpCnt]);
                            $data["SEQ004_REMARK3"]         = trim($tmp[++$tmpCnt]);
                        }
                        $data["TAKESEMES"]                  = trim($tmp[++$tmpCnt]);
                        $data["LESSONCNT"]                  = trim($tmp[++$tmpCnt]);
                        $data["FRAMECNT"]                   = trim($tmp[++$tmpCnt]);
                        $data["COUNTFLG"]                   = trim($tmp[++$tmpCnt]);//集計フラグ---05/01/25Add 1:集計する 0:集計しない---NO001
                        if ($this->keikokutenFlag) {
                            $data["KEIKOKUTEN_SOTEN"]      = trim($tmp[++$tmpCnt]);
                            $data["KEIKOKUTEN_HYOUKA"]     = trim($tmp[++$tmpCnt]);
                            $data["KEIKOKUTEN_HYOUTEI"]    = trim($tmp[++$tmpCnt]);
                        }
                    }

                    //０埋め
                    $data = $this->zeroUme($data);
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $record_no, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$record_no][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$record_no][$val] = $data[$val];
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
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数値エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ
        $valuesErrFlg       = false;    //値エラーフラグ

        $hissuErrMsg        = "";     //必須エラーメッセージ
        $numericErrMsg      = "";     //数値エラーメッセージ
        $lengthErrMsg       = "";     //レングスエラーメッセージ
        $masterErrMsg       = "";     //マスタエラーメッセージ
        $existenceErrMsg    = "";     //存在エラーメッセージ
        $duplicateCsvErrMsg = "";     //重複(CSV)メッセージ
        $valuesErrMsg       = "";     //値エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $existenceSep    = "";
        $duplicateCsvSep = "";
        $valuesSep       = "";

        //学校種別取得
        $school_kind = $db->getCol(knjx060aQuery::getSchoolKind($this));
        //教育課程コード取得
        $curriculum_cd = $db->getCol(knjx060aQuery::getCurriculumCd());
        //重複(CSV)チェック
        if ($this->field["SHORI_MEI"] == "1") {
            $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
            if ($dupFlg1[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
            }
            $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
            if ($dupFlg2[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
            }
        } else {
            $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
            if ($dupFlg1[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
            }
        }
        foreach ($checkdata as $key => $val) {
            if ($this->Properties["useCurriculumcd"] == '1') {
                if ($this->field["SHORI_MEI"] == "1") {
                    //必須チェック
                    if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD", "CHAIRNAME", "CHAIRABBV", "CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD", "GROUPCD"))) {
                        if (!strlen(trim($data[$key]))) {
                            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                        }
                    }
                    //文字数チェック
                    if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD", "CHAIRNAME", "CHAIRABBV", "CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD", "GROUPCD", "TAKESEMES", "LESSONCNT", "FRAMECNT", "COUNTFLG"))) {
                        if (strlen($data[$key]) > $val[0]) {
                            $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                        }
                    } else {
                        if ($this->Properties["chairRetsuMeisho_Hyouji"] == '1') {
                            if (in_array($key, array("SEQ004_REMARK2", "SEQ004_REMARK3"))) {
                                if (strlen($data[$key]) > $val[0]) {
                                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                                }
                            }
                        }
                    }
                    //数値チェック
                    if ($this->keikokutenFlag) {
                        if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD", "CLASSCD", "CURRICULUM_CD", "SUBCLASSCD", "GROUPCD", "TAKESEMES", "LESSONCNT", "FRAMECNT", "COUNTFLG", "KEIKOKUTEN_SOTEN", "KEIKOKUTEN_HYOUKA", "KEIKOKUTEN_HYOUTEI"))) {
                            if ($data[$key] != "" && !is_numeric($data[$key])) {
                                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                            }
                        }
                    } else {
                        if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD", "CLASSCD", "CURRICULUM_CD", "SUBCLASSCD", "GROUPCD", "TAKESEMES", "LESSONCNT", "FRAMECNT", "COUNTFLG"))) {
                            if ($data[$key] != "" && !is_numeric($data[$key])) {
                                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                            }
                        }
                    }
                    //値チェック
                    if ($key == "SCHOOL_KIND" && $data[$key] != "" && !in_array($data["SCHOOL_KIND"], $school_kind)) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                    if ($key == "CURRICULUM_CD" && $data[$key] != "" && !in_array($data["CURRICULUM_CD"], $curriculum_cd)) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                    if ($key == "TAKESEMES" && $data[$key] != "" && !in_array($data["TAKESEMES"], array("0","1","2","3"))) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                    if ($key == "COUNTFLG" && $data[$key] != "" && !in_array($data["COUNTFLG"], array("0","1"))) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                } else {
                    //必須チェック
                    if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD"))) {
                        if (!strlen(trim($data[$key]))) {
                            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                        }
                    }
                    //文字数チェック
                    if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD"))) {
                        if (strlen($data[$key]) > $val[0]) {
                            $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                        }
                    }
                    //数値チェック
                    if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD"))) {
                        if ($data[$key] != "" && !is_numeric($data[$key])) {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                        }
                    }
                }
            } else {
                if ($this->field["SHORI_MEI"] == "1") {
                    //必須チェック
                    if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD", "CHAIRNAME", "CHAIRABBV", "SUBCLASSCD", "GROUPCD"))) {
                        if (!strlen(trim($data[$key]))) {
                            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                        }
                    }
                    //文字数チェック
                    if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD", "CHAIRNAME", "CHAIRABBV", "SUBCLASSCD", "GROUPCD", "TAKESEMES", "LESSONCNT", "FRAMECNT", "COUNTFLG"))) {
                        if (strlen($data[$key]) > $val[0]) {
                            $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                        }
                    } else {
                        if ($this->Properties["chairRetsuMeisho_Hyouji"] == '1') {
                            if (in_array($key, array("SEQ004_REMARK2", "SEQ004_REMARK3"))) {
                                if (strlen($data[$key]) > $val[0]) {
                                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                                }
                            }
                        }
                    }
                    //数値チェック
                    if ($this->keikokutenFlag) {
                        if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD", "SUBCLASSCD", "GROUPCD", "TAKESEMES", "LESSONCNT", "FRAMECNT", "COUNTFLG", "KEIKOKUTEN_SOTEN", "KEIKOKUTEN_HYOUKA", "KEIKOKUTEN_HYOUTEI"))) {
                            if ($data[$key] != "" && !is_numeric($data[$key])) {
                                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                            }
                        }
                    } else {
                        if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD", "SUBCLASSCD", "GROUPCD", "TAKESEMES", "LESSONCNT", "FRAMECNT", "COUNTFLG"))) {
                            if ($data[$key] != "" && !is_numeric($data[$key])) {
                                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                            }
                        }
                    }
                    //値チェック
                    if ($key == "TAKESEMES" && $data[$key] != "" && !in_array($data["TAKESEMES"], array("0","1","2","3"))) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                    if ($key == "COUNTFLG" && $data[$key] != "" && !in_array($data["COUNTFLG"], array("0","1"))) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                } else {
                    //必須チェック
                    if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD"))) {
                        if (!strlen(trim($data[$key]))) {
                            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                        }
                    }
                    //文字数チェック
                    if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD"))) {
                        if (strlen($data[$key]) > $val[0]) {
                            $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                        }
                    }
                    //数値チェック
                    if (in_array($key, array("YEAR", "SEMESTER", "CHAIRCD"))) {
                        if ($data[$key] != "" && !is_numeric($data[$key])) {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                        }
                    }
                }
            }
        }
        if ($returnFlg) {
            if ($this->field["SHORI_MEI"] == "1") {
                //1.科目マスタ
                $result = $db->getOne(knjx060aQuery::getMasterCheck($this, $data, "1"));
                if ($result != "X") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "科目コード");
                }
                //2.選択科目マスタ
                if ($data["GROUPCD"] != "0000") {
                    $result = $db->getOne(knjx060aQuery::getMasterCheck($this, $data, "2"));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "選択科目");
                    }
                }
                if ($this->Properties["useCurriculumcd"] == '1') {
                    //1.教科マスタ
                    if ($data["CLASSCD"] != "") {
                        $result = $db->getOne(knjx060aQuery::getMasterCheck($this, $data, "3"));
                        if ($result != "X") {
                            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "教科コード");
                        }
                    }
                }
                if ($this->Properties["chairRetsuMeisho_Hyouji"] == '1') {
                    if ($data["SEQ004_REMARK1"] != "") {
                        $result = $db->getOne(knjx060aQuery::getMasterCheck2($this, $data, "3"));
                        if ($result == null) {
                            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "コード");
                        }
                    }
                }
                //週授業回数と連続枠数の大小チェック(更新時のみ)
                if ($data["LESSONCNT"] == "" && $data["FRAMECNT"] != "") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※範囲:", $existenceSep, "週授業回数が未設定");
                }
                if ($data["LESSONCNT"] != "" && $data["FRAMECNT"] != "") {
                    if ($data["LESSONCNT"] < $data["FRAMECNT"]) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※範囲:", $existenceSep, "週授業回数>=連続枠数で設定して下さい");
                    }
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$lengthErrMsg.$numericErrMsg.$masterErrMsg.$existenceErrMsg.$valuesErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx060aQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        //ヘッダー項目
        $header["YEAR"]         = "※年度";
        $header["SEMESTER"]     = "※学期";
        $header["CHAIRCD"]      = "※講座コード";
        $header["GROUPCD"]      = "※群コード";
        if ($this->Properties["useCurriculumcd"] == '1') {
            $header["CLASSCD"]          = "※教科コード";
            $header["SCHOOL_KIND"]      = "※学校種別";
            $header["CURRICULUM_CD"]    = "※教育課程コード";
        }
        $header["SUBCLASSCD"]   = "※科目コード";
        $header["CHAIRNAME"]    = "※講座名称";
        $header["CHAIRABBV"]    = "※講座略称";
        if ($this->Properties["chairRetsuMeisho_Hyouji"] == '1') {
            $header["SEQ004_REMARK1"]    = "列名称";
            $header["SEQ004_REMARK2"]    = "スモールクラス名称";
            $header["SEQ004_REMARK3"]    = "習熟度クラス名称";
        }
        $header["TAKESEMES"]    = "履修学期";
        $header["LESSONCNT"]    = "週授業回数";
        $header["FRAMECNT"]     = "連続枠数";
        $header["COUNTFLG"]     = "集計フラグ";
        if ($this->keikokutenFlag) {
            $header["KEIKOKUTEN_SOTEN"]    = "警告点(素点)";
            $header["KEIKOKUTEN_HYOUKA"]   = "警告点(評価)";
            $header["KEIKOKUTEN_HYOUTEI"]  = "警告点(評定)";
        }
        $header["LASTCOLUMN"]   = $this->lastColumn;


        //ヘッダー項目例
        $header_rei["YEAR"]         = CTRL_YEAR;
        $header_rei["SEMESTER"]     = CTRL_SEMESTER;
        $header_rei["CHAIRCD"]      = "9999999";
        $header_rei["GROUPCD"]      = "9999";

        if ($this->Properties["useCurriculumcd"] == '1') {
            //学校種別ヘッダー例取得
            $school_kind_rei = "";
            $result = $db->query(knjx060aQuery::getSchoolKind($this));
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

        $header_rei["SUBCLASSCD"]   = "123456";
        $header_rei["CHAIRNAME"]    = "国語Ⅱ";
        $header_rei["CHAIRABBV"]    = "国語Ⅱ";
        if ($this->Properties["chairRetsuMeisho_Hyouji"] == '1') {
            $header_rei["SEQ004_REMARK1"]    = "A";
            $header_rei["SEQ004_REMARK2"]    = "スモール";
            $header_rei["SEQ004_REMARK3"]    = "習熟度";
        }
        $header_rei["TAKESEMES"]    = "1";
        $header_rei["LESSONCNT"]    = "4";
        $header_rei["FRAMECNT"]     = "2";
        $header_rei["COUNTFLG"]     = "1";
        if ($this->keikokutenFlag) {
            $header_rei["KEIKOKUTEN_SOTEN"]    = "30";
            $header_rei["KEIKOKUTEN_HYOUKA"]   = "1";
            $header_rei["KEIKOKUTEN_HYOUTEI"]  = "5";
        }
        $header_rei["LASTCOLUMN"]   = $this->lastColumn;


        $contents  = "";    //クリア

        if ($this->field["OUTPUT"] == "1") {
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {     //エラー
            $headerDuplicate = array("YEAR"     => "※年度",
                                     "SEMESTER" => "※学期",
                                     "CHAIRCD"  => "※講座コード",
                                     "SEQ"      => "SEQ");
            $tables = array();
            $tables = array("1" => "CHAIR_DAT", "2" => "CHAIR_DETAIL_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents = implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {     //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }
        if ($this->field["OUTPUT"] == "1") {
            $header_data = true;
        } else {                                //ヘッダ以外
            $header_data = false;
            $table = "";
            $where = "";
            if ($this->field["OUTPUT"] == "3") {
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {     //データ
                $query = knjx060aQuery::getselectDataSQL($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {                //データ
                    $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row["REGISTERCD"] = $row["UPDATED"] = "";
                    $row["REGISTERCD"] = $this->lastColumn;
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
                $couse = "講座データ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    /**
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    public function zeroUme($data)
    {
        //更新
        if ($this->field["SHORI_MEI"] == "1") {
            if ($data["CHAIRCD"] != "") {
                if (strlen($data["CHAIRCD"]) < 7) {
                    $data["CHAIRCD"] = sprintf("%07d", $data["CHAIRCD"]);
                }
            }
            if ($this->Properties["useCurriculumcd"] == '1') {
                if ($data["CLASSCD"] != "") {
                    if (strlen($data["CLASSCD"]) < 2) {
                        $data["CLASSCD"] = sprintf("%02d", $data["CLASSCD"]);
                    }
                }
            }
            if ($data["SUBCLASSCD"] != "") {
                if (strlen($data["SUBCLASSCD"]) < 6) {
                    $data["SUBCLASSCD"] = sprintf("%06d", $data["SUBCLASSCD"]);
                }
            }
            if ($data["GROUPCD"] != "") {
                if (strlen($data["GROUPCD"]) < 4) {
                    $data["GROUPCD"] = sprintf("%04d", $data["GROUPCD"]);
                }
            }
            //削除
        } else {
            if ($data["CHAIRCD"] != "") {
                if (strlen($data["CHAIRCD"]) < 7) {
                    $data["CHAIRCD"] = sprintf("%07d", $data["CHAIRCD"]);
                }
            }
        }

        return $data;
    }

    //データの整合性チェック
    public function isDataMatched(&$dataArr)
    {
        //１万件を超えたら制限エラー
        if (get_count($dataArr) > 65000) {
            $this->setWarning("MSG915", "処理件数を６万５千件以下にして下さい 処理件数：".get_count($dataArr)."件");
            return false;
        }
        return true;
    }
}
