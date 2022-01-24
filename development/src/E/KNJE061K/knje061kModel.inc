<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje061kModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $annual;            //年次
    public $control;           //コントロールマスタ
    public $hr_class;
    public $coursecode;
    public $RegdTable;         //学校区分別のテーブル名

    /**
     * メンバ変数の初期化
    */
    public function knje061kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        if ($this->control["学校区分"] == "0") { //学年制
            $this->RegdTable = "v_regdyear_grade_dat";
        } elseif ($this->control["学校区分"] == "1") { //単位制
            $this->RegdTable = "v_regdyear_unit_dat";
        }
    }

    public function init()
    {
        $this->cmd    = VARS::request("cmd");
        //年次
        $this->annual = VARS::post("ANNUAL");
        //組
        $this->hr_class = VARS::post("HR_CLASS");
        //コース
        $this->coursecode = VARS::post("COURSECODE");
        $this->schregno   = VARS::post("SCHREGNO");

        //年次が変更したら初期化する
        if ($this->cmd == "annual") {
            $this->hr_class = "";
            $this->coursecode = "";
        }
        //組を変更したら初期化する
        if ($this->cmd == "hr_class") {
            $this->coursecode = "";
        }

        if (VARS::file("FILE")) {
            $this->file = VARS::file("FILE");        //ファイルのアップロード
        }
        //画面上のデータ
        $this->field = array("KIND"     => VARS::post("KIND"),         //種別
                            "RANGE"     => VARS::post("RANGE"),        //生成範囲
                            "METHOD"    => VARS::post("METHOD"),       //生成方法
                            "CREATEDIV" => VARS::post("CREATEDIV"),     //新規作成 OR 追加作成
                            "HEADERFLG" => VARS::post("HEADERFLG"));   //ヘッダ有りフラグ
        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    public function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        if ($this->field["KIND"] == 2 && $this->field["METHOD"] == 2) {
            //ファイルの文字列の取得
            $this->data_arr = array();
            $this->errData = array();
            $this->data_arr = $this->fileUpload();
            //データの整合性チェック
            if ($this->isDataMatched($this->data_arr)) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                $db->expectError();
                for ($i = 0; $i < get_count($this->data_arr); $i++) {
                    //学籍学習記録データの削除
                    $result = $db->query(knje061kQuery::deleteQuerySchreg_StudyRec_Dat($this->data_arr[$i], $this));
                    if (PEAR::isError($result)) {
                        return;
                    }

                    //CLASS_MSTより科目数を取得
                    $subclasses = $db->getOne(knje061kQuery::getSubclasses($this->data_arr[$i], $this));
                    $this->data_arr[$i]["SUBCLASSES"] = $subclasses;

                    //学籍学習記録データの作成（ＣＳＶデータより読込)
                    $result = $db->query(knje061kQuery::addQuerySchreg_StudyRec_Dat($this->data_arr[$i], $this));
                    if (PEAR::isError($result)) {
                        return;
                    }
                    $result = $db->query(knje061kQuery::deleteStudyclassremark($this->data_arr[$i], $this));
                    if (PEAR::isError($result)) {
                        return;
                    }
                    $result = $db->query(knje061kQuery::insertStudyclassremark($this->data_arr[$i], $this));
                    if (PEAR::isError($result)) {
                        return;
                    }
                    $db->popExpect();
                }
                $db->commit();
                Query::dbCheckIn($db);

                $erraDatas = "";
                $tables = array("1" => "SCHREG_STUDYREC_DAT",
                                "2" => "STUDYCLASSREMARK_DAT");
                $header = array("SCHOOLCD"          => "本校区分",
                                "YEAR"              => "年度",
                                "SCHREGNO"          => "学籍番号",
                                "ANNUAL"            => "年次",
                                "CLASSCD"           => "教科コード",
                                "SCHOOL_KIND"       => "学校校種",
                                "CURRICULUM_CD"     => "教育課程コード",
                                "SUBCLASSCD"        => "科目コード");
                $dupTable = $this->getDuplicateErrorHeaderNoSave(PROGRAMID, $tables, $this->errData);
                if (sizeof($dupTable) > 0) {
                    $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                }
                if (sizeof($this->errData) > 0) {
                    $erraDatas .= "行番号　メッセージ"."\\n";
                }
                foreach ($this->errData as $errKey => $errVal) {
                    $erraDatas .= $errKey."行目　".$errVal["MSG"]."\\n";
                }
                if ($erraDatas != "") {
                    $this->setMessage("MSG204", "\\n"."取込みエラー内容:". "\\n".$headerErr["MSGREMARK"]. "\\n".$erraDatas);
                } else {
                    $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
                }
            }
        } elseif ($this->field["KIND"] == 2 && $this->field["METHOD"] == 1) { //"成績データ抽出中..."
            //新規作成と追加作成
            knje061kQuery::addRecordSchregStudyrec($this);
            $this->message = "学籍学習記録データの生成が完了しました。";
        } elseif ($this->field["KIND"] == 1) { //"勤怠データ抽出＆集計中..."
            //出欠累積データの存在チェック
            if (knje061kQuery::existsRecordAttendSemesDat($this)) {
                //出欠学期別累積データより集計
                if (knje061kQuery::addRecordSchregAttendrec($this)) {
                    $this->message = "学籍出欠記録データの生成が完了しました。";
                }
            }
        } elseif ($this->field["KIND"] == 3) {
            $this->updateKanten();
            $this->message = "観点データの生成が完了しました。";
        }
    }

    public function fileUpload()
    {
        $temp_arr = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            if ($this->Properties["useCurriculumcd"] == '1') {
                $checkdata = array( "SCHOOLCD"              => array(1,"本校区分"),
                                    "YEAR"                  => array(4,"年度"),
                                    "SCHREGNO"              => array(8,"学籍番号"),
                                    "ANNUAL"                => array(2,"年次"),
                                    "CLASSCD"               => array(2,"教科コード"),
                                    "CLASSNAME"             => array(30,"教科名"),
                                    "CLASSABBV"             => array(15,"教科略称"),
                                    "CLASSNAME_ENG"         => array(40,"教科名称英字"),
                                    "CLASSABBV_ENG"         => array(30,"教科略称英字"),
                                    "SCHOOL_KIND"           => array(2,"学校校種"),
                                    "CURRICULUM_CD"         => array(2,"教育課程コード"),
                                    "SUBCLASSCD"            => array(6,"科目コード"),
                                    "SUBCLASSNAME"          => array(90,"科目名"),
                                    "SUBCLASSABBV"          => array(90,"科目略称"),
                                    "SUBCLASSNAME_ENG"      => array(40,"科目名称英字"),
                                    "SUBCLASSABBV_ENG"      => array(20,"科目略称英字"),
                                    "VALUATION"             => array(2,"評定"),
                                    "GET_CREDIT"            => array(2,"標準修得単位"),
                                    "ADD_CREDIT"            => array(2,"増加修得単位"),
                                    "REMARK"                => array(60,"備考"));
            } else {
                $checkdata = array( "SCHOOLCD"              => array(1,"本校区分"),
                                    "YEAR"                  => array(4,"年度"),
                                    "SCHREGNO"              => array(8,"学籍番号"),
                                    "ANNUAL"                => array(2,"年次"),
                                    "CLASSCD"               => array(2,"教科コード"),
                                    "CLASSNAME"             => array(30,"教科名"),
                                    "CLASSABBV"             => array(15,"教科略称"),
                                    "CLASSNAME_ENG"         => array(40,"教科名称英字"),
                                    "CLASSABBV_ENG"         => array(30,"教科略称英字"),
                                    "SUBCLASSCD"            => array(6,"科目コード"),
                                    "SUBCLASSNAME"          => array(90,"科目名"),
                                    "SUBCLASSABBV"          => array(90,"科目略称"),
                                    "SUBCLASSNAME_ENG"      => array(40,"科目名称英字"),
                                    "SUBCLASSABBV_ENG"      => array(20,"科目略称英字"),
                                    "VALUATION"             => array(2,"評定"),
                                    "GET_CREDIT"            => array(2,"標準修得単位"),
                                    "ADD_CREDIT"            => array(2,"増加修得単位"),
                                    "REMARK"                => array(60,"備考"));
            }
            $fp = fopen($this->file["tmp_name"], "r");
            $i = 0;
            $line = 1;  //行数
            $header = false;

            $primaryKey1 = array();
            $primaryKey1 = $this->getPrimaryKey('SCHREG_STUDYREC_DAT');
            $duplicateCsv1 = array();

            $primaryKey2 = array();
            $primaryKey2 = $this->getPrimaryKey('STUDYCLASSREMARK_DAT');
            $duplicateCsv2 = array();

            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);

                if (trim($buffer) != '') {
                    //ヘッダ有りの場合は1行目を無視
                    if ($this->field["HEADERFLG"] == "1" && !$header) {
                        $header = true;
                        $line++;
                        continue;
                    }

                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp = common::csv2array($buffer);
                    //教育課程対応
                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $data = array("SCHOOLCD"          => $tmp[0],
                                        "YEAR"            => $tmp[8],
                                        "SCHREGNO"        => $tmp[4],
                                        "ANNUAL"          => $tmp[9],
                                        "CLASSCD"         => $tmp[14],
                                        "CLASSNAME"       => $tmp[15],
                                        "CLASSABBV"       => $tmp[16],
                                        "CLASSNAME_ENG"   => $tmp[17],
                                        "CLASSABBV_ENG"   => $tmp[18],
                                        "SCHOOL_KIND"     => $tmp[19],
                                        "CURRICULUM_CD"   => $tmp[20],
                                        "SUBCLASSCD"      => $tmp[21],
                                        "SUBCLASSNAME"    => $tmp[22],
                                        "SUBCLASSABBV"    => $tmp[23],
                                        "SUBCLASSNAME_ENG"=> $tmp[24],
                                        "SUBCLASSABBV_ENG"=> $tmp[25],
                                        "VALUATION"       => $tmp[26],
                                        "GET_CREDIT"      => $tmp[27],
                                        "ADD_CREDIT"      => $tmp[28],
                                        "REMARK"          => $tmp[29]);
                    } else {
                        $data = array("SCHOOLCD"          => $tmp[0],
                                        "YEAR"            => $tmp[8],
                                        "SCHREGNO"        => $tmp[4],
                                        "ANNUAL"          => $tmp[9],
                                        "CLASSCD"         => $tmp[14],
                                        "CLASSNAME"       => $tmp[15],
                                        "CLASSABBV"       => $tmp[16],
                                        "CLASSNAME_ENG"   => $tmp[17],
                                        "CLASSABBV_ENG"   => $tmp[18],
                                        "SUBCLASSCD"      => $tmp[19],
                                        "SUBCLASSNAME"    => $tmp[20],
                                        "SUBCLASSABBV"    => $tmp[21],
                                        "SUBCLASSNAME_ENG"=> $tmp[22],
                                        "SUBCLASSABBV_ENG"=> $tmp[23],
                                        "VALUATION"       => $tmp[24],
                                        "GET_CREDIT"      => $tmp[25],
                                        "ADD_CREDIT"      => $tmp[26],
                                        "REMARK"          => $tmp[27]);
                    }
                    $data = str_replace("'", "''", $data);
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
                }
                $line++;
            }
            $db->commit();
            Query::dbCheckIn($db);
            fclose($fp);
        }
         return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            if ($this->Properties["useCurriculumcd"] == '1') {
                //必須項目チェック
                if (in_array($key, array("SCHOOLCD", "SCHREGNO", "YEAR", "ANNUAL", "CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            } else {
                if (in_array($key, array("SCHOOLCD", "SCHREGNO", "YEAR", "ANNUAL", "CLASSCD", "SCHOOL_KIND"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            $this->errData[$line]["MSG"] = $checkError;
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

    /****************************/
    /* ↓↓↓ 観点データ ↓↓↓ */
    /****************************/

    //メイン
    public function updateKanten()
    {
        //ＤＢ接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knje061kQuery::getStudent($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            //削除
            knje061kQuery::delDataJview($db, $schregNo, $this);
            //追加
            knje061kQuery::insDataJview($db, $schregNo, $this);
        }

        $db->commit();
        //ＤＢ切断
        Query::dbCheckIn($db);
    }
}
