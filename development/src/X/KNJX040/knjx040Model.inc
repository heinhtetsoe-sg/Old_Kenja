<?php

require_once('for_php7.php');
//あⅠ㈱②髙い

/**
 * 定数定義
 */
class knjx040Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $useSchregRegdHdat; //フラグ
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx040Model()
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

        $this->field = array("OUTPUT"    =>  VARS::post("OUTPUT"),   //出力取込種別
                             "YEAR"      =>  VARS::post("YEAR"),     //年度一覧
                             "HEADER"    =>  VARS::post("HEADER"),   //ヘッダ有
                             "SHORI_MEI" =>  VARS::post("SHORI_MEI") //処理名
                             );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->useSchregRegdHdat = ($this->Properties["useSchregRegdHdat"] == '1') ? $this->Properties["useSchregRegdHdat"] : 0;

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
                    $table = "SCHREG_REGD_HDAT";
                    $where  = " WHERE ";
                    $where .= "         YEAR     = '{$this->dataArr[$i]["YEAR"]}'     ";
                    $where .= "     AND SEMESTER = '{$this->dataArr[$i]["SEMESTER"]}' ";
                    $where .= "     AND GRADE    = '{$this->dataArr[$i]["GRADE"]}'    ";
                    $where .= "     AND HR_CLASS = '{$this->dataArr[$i]["HR_CLASS"]}' ";
                    $query = knjx040Query::getMasterCheck($table, $where);

                    $result = $db->query($query);
                    $flg = false;
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $flg = true;
                    }

                    if ($this->field["SHORI_MEI"]=="1") { //処理名：更新ならば
                        if ($flg) { //ＤＢにあれば、update
                            knjx040Query::updateQueryCsv($db, $this->dataArr[$i], $this->staffcd, $this->useSchregRegdHdat);
                        } else { //ＤＢになければ、insert
                            knjx040Query::insertQueryCsv($db, $this->dataArr[$i], $this->staffcd, $this->useSchregRegdHdat);
                        }

                        //STAFF_CLASS_HIST_DATの処理
                        knjx040Query::deleteStaffClassHistDat($db, $this->dataArr[$i]);
                        $tr_div = array("TR_CD1", "TR_CD2", "TR_CD3", "SUBTR_CD1", "SUBTR_CD2", "SUBTR_CD3");
                        for ($j=0; $j < get_count($tr_div); $j++) {
                            $tr_staffcd = $db->getOne(knjx040Query::getTrStaffcd($this->dataArr[$i], $tr_div[$j]));
                            if ($tr_staffcd) {
                                knjx040Query::insertStaffClassHistDat($db, $this->dataArr[$i], ($j+1), $tr_staffcd);
                            }
                        }
                    } else { //処理名：削除ならば
                        //ＤＢにあれば、delete
                        if ($flg) {
                            knjx040Query::deleteQueryCsv($db, $this->dataArr[$i]);
                            knjx040Query::deleteStaffClassHistDat($db, $this->dataArr[$i]);
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
        $setsize = "";
        //STAFFCDフィールドサイズ変更対応
        if ($this->Properties["useStaffcdFieldSize"] === '10') {
            $setsize = 10;
        } else {
            $setsize = 8;
        }

        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {      //HTTP POSTによりアップロードされたファイルかどうかを調べる
            if ($this->useSchregRegdHdat == '1') {
                $checkdata = array("YEAR"           => array(4, "年度"),
                                   "SEMESTER"       => array(1, "学期"),
                                   "GRADE"          => array(2, "学年"),
                                   "HR_CLASS"       => array(3, "組"),
                                   "HR_NAME"        => array(15, "年組名称"),
                                   "HR_NAMEABBV"    => array(5, "年組略称"),
                                   "GRADE_NAME"     => array(30, "年名称"),
                                   "HR_CLASS_NAME1" => array(30, "組名称1"),
                                   "HR_CLASS_NAME2" => array(30, "組名称2"),
                                   "HR_FACCD"       => array(4, "HR施設コード"),
                                   "TR_CD1"         => array($setsize, "担任コード1"),
                                   "TR_CD2"         => array($setsize, "担任コード2"),
                                   "TR_CD3"         => array($setsize, "担任コード3"),
                                   "SUBTR_CD1"      => array($setsize, "副担任コード1"),
                                   "SUBTR_CD2"      => array($setsize, "副担任コード2"),
                                   "SUBTR_CD3"      => array($setsize, "副担任コード3"));
            } else {
                $checkdata = array("YEAR"           => array(4, "年度"),
                                   "SEMESTER"       => array(1, "学期"),
                                   "GRADE"          => array(2, "学年"),
                                   "HR_CLASS"       => array(3, "組"),
                                   "HR_NAME"        => array(15, "年組名称"),
                                   "HR_NAMEABBV"    => array(5, "年組略称"),
                                   "HR_FACCD"       => array(4, "HR施設コード"),
                                   "TR_CD1"         => array($setsize, "担任コード1"),
                                   "TR_CD2"         => array($setsize, "担任コード2"),
                                   "TR_CD3"         => array($setsize, "担任コード3"),
                                   "SUBTR_CD1"      => array($setsize, "副担任コード1"),
                                   "SUBTR_CD2"      => array($setsize, "副担任コード2"),
                                   "SUBTR_CD3"      => array($setsize, "副担任コード3"));
            }
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
            $primaryKey1 = $this->getPrimaryKey('SCHREG_REGD_HDAT');
            $primaryKey2 = $this->getPrimaryKey('STAFF_CLASS_HIST_DAT');

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

                    if ($this->useSchregRegdHdat == '1') { //新しいschreg_regd_hdatのテーブルであるならば
                        $data = array("YEAR"           => trim($tmp[0]),
                                      "SEMESTER"       => trim($tmp[1]),
                                      "GRADE"          => trim($tmp[2]),
                                      "HR_CLASS"       => trim($tmp[3]),
                                      "HR_NAME"        => trim($tmp[4]),
                                      "HR_NAMEABBV"    => trim($tmp[5]),
                                      "GRADE_NAME"     => trim($tmp[6]),
                                      "HR_CLASS_NAME1" => trim($tmp[7]),
                                      "HR_CLASS_NAME2" => trim($tmp[8]),
                                      "HR_FACCD"       => trim($tmp[9]),
                                      "TR_CD1"         => trim($tmp[10]),
                                      "TR_CD2"         => trim($tmp[11]),
                                      "TR_CD3"         => trim($tmp[12]),
                                      "SUBTR_CD1"      => trim($tmp[13]),
                                      "SUBTR_CD2"      => trim($tmp[14]),
                                      "SUBTR_CD3"      => trim($tmp[15]));
                    } else {
                        $data = array("YEAR"        => trim($tmp[0]),
                                      "SEMESTER"    => trim($tmp[1]),
                                      "GRADE"       => trim($tmp[2]),
                                      "HR_CLASS"    => trim($tmp[3]),
                                      "HR_NAME"     => trim($tmp[4]),
                                      "HR_NAMEABBV" => trim($tmp[5]),
                                      "HR_FACCD"    => trim($tmp[6]),
                                      "TR_CD1"      => trim($tmp[7]),
                                      "TR_CD2"      => trim($tmp[8]),
                                      "TR_CD3"      => trim($tmp[9]),
                                      "SUBTR_CD1"   => trim($tmp[10]),
                                      "SUBTR_CD2"   => trim($tmp[11]),
                                      "SUBTR_CD3"   => trim($tmp[12]));
                    }

                    if ($this->field["SHORI_MEI"]=="1") { //処理名：更新ならば
                        //０埋め
                        $data["GRADE"]      = $this->zeroUme($data["GRADE"], 2);
                        $data["HR_CLASS"]   = $this->zeroUme($data["HR_CLASS"], 3);
                        $data["HR_FACCD"]   = $this->zeroUme($data["HR_FACCD"], 4);
                        $data["TR_CD1"]     = $this->zeroUme($data["TR_CD1"], $setsize);
                        $data["TR_CD2"]     = $this->zeroUme($data["TR_CD2"], $setsize);
                        $data["TR_CD3"]     = $this->zeroUme($data["TR_CD3"], $setsize);
                        $data["SUBTR_CD1"]  = $this->zeroUme($data["SUBTR_CD1"], $setsize);
                        $data["SUBTR_CD2"]  = $this->zeroUme($data["SUBTR_CD2"], $setsize);
                        $data["SUBTR_CD3"]  = $this->zeroUme($data["SUBTR_CD3"], $setsize);
                    } else {  //処理名：削除ならば
                        //０埋め
                        $data["GRADE"]    = $this->zeroUme($data["GRADE"], 2);
                        $data["HR_CLASS"] = $this->zeroUme($data["HR_CLASS"], 3);
                    }
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
                $record_no++; //行番号(カウント)
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
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ

        $hissuErrMsg        = "";     //必須エラーメッセージ
        $numericErrMsg      = "";     //数値エラーメッセージ
        $lengthErrMsg       = "";     //レングスエラーメッセージ
        $masterErrMsg       = "";     //マスタエラーメッセージ
        $duplicateCsvErrMsg = "";     //重複(CSV)メッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $duplicateCsvSep = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }
        foreach ($checkdata as $key => $val) {
            if ($this->field["SHORI_MEI"] == "1") {
                //必須チェック
                if (in_array($key, array("YEAR", "SEMESTER", "GRADE", "HR_CLASS", "HR_NAME", "HR_NAMEABBV", "TR_CD1"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
                //数値チェック
                if (in_array($key, array("YEAR", "SEMESTER", "GRADE", "HR_FACCD"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            } else {
                //必須チェック
                if (in_array($key, array("YEAR", "SEMESTER", "GRADE", "HR_CLASS"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                //文字数チェック
                if (in_array($key, array("YEAR", "SEMESTER", "GRADE", "HR_CLASS"))) {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
                //数値チェック
                if (in_array($key, array("YEAR", "SEMESTER", "GRADE"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }
        }
        if ($returnFlg) {
            if ($this->field["SHORI_MEI"] == "1") {
                //職名マスタ
                $sonzai_flg = false;
                $table = "semester_mst";
                $where = "where YEAR     = '{$data["YEAR"]}' AND ";
                $where .= "     SEMESTER <> '9' ";
                $result = $db->query(knjx040Query::getMasterSQL($table, $where));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($data["SEMESTER"] == $row["SEMESTER"]) {
                        $sonzai_flg = true;
                    }
                }
                if (!$sonzai_flg) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学期");
                }
                if ($data["HR_FACCD"] != "") {
                    $table = "facility_mst";
                    $where = "where FACCD = '{$data["HR_FACCD"]}'";
                    $result = $db->getOne(knjx040Query::getMasterCheck($table, $where));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "HR施設コード");
                    }
                }
                //職員マスタ
                $table = "staff_mst";
                if ($data["TR_CD1"] != "") {
                    $where = "where staffcd = '{$data["TR_CD1"]}'";
                    $result = $db->getOne(knjx040Query::getMasterCheck($table, $where));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "担任コード1");
                    }
                }
                if ($data["TR_CD2"] != "") {
                    $where = "where staffcd = '{$data["TR_CD2"]}'";
                    $result = $db->getOne(knjx040Query::getMasterCheck($table, $where));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "担任コード2");
                    }
                }
                if ($data["TR_CD3"] != "") {
                    $where = "where staffcd = '{$data["TR_CD3"]}'";
                    $result = $db->getOne(knjx040Query::getMasterCheck($table, $where));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "担任コード3");
                    }
                }
                if ($data["SUBTR_CD1"] != "") {
                    $where = "where staffcd = '{$data["SUBTR_CD1"]}'";
                    $result = $db->getOne(knjx040Query::getMasterCheck($table, $where));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "副担任コード1");
                    }
                }
                if ($data["SUBTR_CD2"] != "") {
                    $where = "where staffcd = '{$data["SUBTR_CD2"]}'";
                    $result = $db->getOne(knjx040Query::getMasterCheck($table, $where));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "副担任コード2");
                    }
                }
                if ($data["SUBTR_CD3"] != "") {
                    $where = "where staffcd = '{$data["SUBTR_CD3"]}'";
                    $result = $db->getOne(knjx040Query::getMasterCheck($table, $where));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "副担任コード3");
                    }
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx040Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        $staffcd_rei = "";
        //STAFFCDフィールドサイズ変更対応
        if ($this->Properties["useStaffcdFieldSize"] === '10') {
            $staffcd_rei = "8888888888";
        } else {
            $staffcd_rei = "88888888";
        }

        //項目
        $header_err = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
        //header部分↓↓↓↓↓↓↓↓///////////////////////////////////////////
        if ($this->useSchregRegdHdat == '1') { //新しいschreg_regd_hdatのテーブルであるならば
            $header = array("YEAR"            => "※年度",
                            "SEMESTER"        => "※学期",
                            "GRADE"           => "※学年",
                            "HR_CLASS"        => "※組",
                            "HR_NAME"         => "※年組名称",
                            "HR_NAMEABBV"     => "※年組略称",
                            "GRADE_NAME"      => "年名称",
                            "HR_CLASS_NAME1"  => "組名称1",
                            "HR_CLASS_NAME2"  => "組名称2",
                            "HR_FACCD"        => "HR施設コード",
                            "TR_CD1"          => "※担任コード1",
                            "TR_CD2"          => "担任コード2",
                            "TR_CD3"          => "担任コード3",
                            "SUBTR_CD1"       => "副担任コード1",
                            "SUBTR_CD2"       => "副担任コード2",
                            "SUBTR_CD3"       => "副担任コード3",
                            $this->lastColumn => $this->lastColumn);
        } else {
            $header = array("YEAR"            => "※年度",
                            "SEMESTER"        => "※学期",
                            "GRADE"           => "※学年",
                            "HR_CLASS"        => "※組",
                            "HR_NAME"         => "※組名称",
                            "HR_NAMEABBV"     => "※組略称",
                            "HR_FACCD"        => "HR施設コード",
                            "TR_CD1"          => "※担任コード1",
                            "TR_CD2"          => "担任コード2",
                            "TR_CD3"          => "担任コード3",
                            "SUBTR_CD1"       => "副担任コード1",
                            "SUBTR_CD2"       => "副担任コード2",
                            "SUBTR_CD3"       => "副担任コード3",
                            $this->lastColumn => $this->lastColumn);
        }
        //header部分↑↑↑↑↑↑↑↑///////////////////////////////////////////

        //例の部分↓↓↓↓↓↓↓↓///////////////////////////////////////////
        if ($this->useSchregRegdHdat == '1') { //新しいschreg_regd_hdatのテーブルであるならば
            $header_rei = array("YEAR"            => CTRL_YEAR,
                                "SEMESTER"        => "1",
                                "GRADE"           => "99",
                                "HR_CLASS"        => "999",
                                "HR_NAME"         => "１年Ａ組",
                                "HR_NAMEABBV"     => "1-A",
                                "GRADE_NAME"      => "１年",
                                "HR_CLASS_NAME1"  => "A組",
                                "HR_CLASS_NAME2"  => "A組",
                                "HR_FACCD"        => "4444",
                                "TR_CD1"          => $staffcd_rei,
                                "TR_CD2"          => $staffcd_rei,
                                "TR_CD3"          => $staffcd_rei,
                                "SUBTR_CD1"       => $staffcd_rei,
                                "SUBTR_CD2"       => $staffcd_rei,
                                "SUBTR_CD3"       => $staffcd_rei,
                                $this->lastColumn => $this->lastColumn);
        } else {
            $header_rei = array("YEAR"            => CTRL_YEAR,
                                "SEMESTER"        => "1",
                                "GRADE"           => "99",
                                "HR_CLASS"        => "999",
                                "HR_NAME"         => "１年Ａ組",
                                "HR_NAMEABBV"     => "1-A",
                                "HR_FACCD"        => "4444",
                                "TR_CD1"          => $staffcd_rei,
                                "TR_CD2"          => $staffcd_rei,
                                "TR_CD3"          => $staffcd_rei,
                                "SUBTR_CD1"       => $staffcd_rei,
                                "SUBTR_CD2"       => $staffcd_rei,
                                "SUBTR_CD3"       => $staffcd_rei,
                                $this->lastColumn => $this->lastColumn);
        }
        //例の部分↑↑↑↑↑↑↑↑///////////////////////////////////////////

        $contents  = ""; //クリア

        if ($this->field["OUTPUT"]=="1") { //ヘッダ出力
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") { //エラー出力
            $headerDuplicate = array("YEAR"      => "※年度",
                                     "SEMESTER"  => "※学期",
                                     "GRADE"     => "※学年",
                                     "HR_CLASS"  => "※組",
                                     "TR_DIV"    => "担当区分",
                                     "FROM_DATE" => "担当開始日");
            $tables = array();
            $tables = array("1" => "SCHREG_REGD_HDAT", "2" => "STAFF_CLASS_HIST_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents = implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4") { //データ出力
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header, ",")."\n";
            }
        }
        if ($this->field["OUTPUT"]=="1") { //ヘッダ出力
            $header_data = true;
        } else { //ヘッダ出力以外
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            $table = "";
            $where = "";
            if ($this->field["OUTPUT"]=="3") { //エラー出力
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") { //データ出力
                $query = knjx040Query::getselectDataSQL($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") { //データ出力
                    $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row["REGISTERCD"] = $this->lastColumn;
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") { //エラー出力
                $couse = "エラー出力";
            } else { //エラー出力以外
                $couse = "学籍在籍ヘッダデータ";
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
    public function zeroUme($data, $ketasuu)
    {
        $shosiki = "%0".$ketasuu."s";
        if ($data!="") {
            if (strlen($data) < $ketasuu) {
                $data = sprintf($shosiki, $data);
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
