<?php
/**
 * 定数定義
*/

class knje061aModel extends Model
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
    public $schoolName;
    public $recordTableDiv;
    public $maxSemester;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
    */
    public function knje061aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        if ($this->control["学校区分"] == "0") { //学年制
            $this->RegdTable = "v_regdyear_grade_dat";
        } elseif ($this->control["学校区分"] == "1") { //単位制
            $this->RegdTable = "v_regdyear_unit_dat";
        }
        $this->maxSemester = knje061aQuery::getMaxSemester();
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
        
        if (VARS::file("FILE")) {
            $this->file = VARS::file("FILE");        //ファイルのアップロード
        }
        //画面上のデータ
        $this->field = array("KIND"     => VARS::post("KIND"),         //種別
                            "PROV"      => VARS::post("PROV"),         //仮評定ラジオ
                            "RANGE"     => VARS::post("RANGE"),        //生成範囲
                            "METHOD"    => VARS::post("METHOD"),       //生成方法
                            "CREATEDIV" => VARS::post("CREATEDIV"),     //新規作成 OR 追加作成
                            "HEADERFLG" => VARS::post("HEADERFLG"));   //ヘッダ有りフラグ
        // 成績テーブルを判断するためのフラグを取得(法政・自修館・その他)
        $rtnRow = knje061aQuery::getNameMst();
        $this->schoolName       = $rtnRow["NAME1"];
        $this->recordTableDiv   = $rtnRow["NAMESPARE1"];

        $this->getPropertiesAll();
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
        $header = array("SCHOOLCD"          => "本校区分",
                        "YEAR"              => "年度",
                        "SCHREGNO"          => "学籍番号",
                        "ANNUAL"            => "年次",
                        "CLASSCD"           => "教科コード",
                        "SCHOOL_KIND"       => "学校校種",
                        "CURRICULUM_CD"     => "教育課程コード",
                        "SUBCLASSCD"        => "科目コード");

        if ($this->field["KIND"] == 2 && $this->field["METHOD"] == 2) {
            //ファイル取り込み
            $this->dataArr = array();
            $this->errData = array();
            $this->dataArr = $this->fileUpload();
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            $erraDatas = "";
            $tables = array();
            $tables = array("1" => "SCHREG_STUDYREC_DAT", "2" => "STUDYRECREMARK_DAT");
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
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    $db->expectError();
                    $result = $db->query(knje061aQuery::deleteQuerySchregStudyRecDat($this->dataArr[$i]));
                    if (PEAR::isError($result)) {
                        continue;
                    }

                    //CLASS_MSTより科目数を取得
                    $subclasses = $db->getOne(knje061aQuery::getSubclasses($this->dataArr[$i]));
                    $this->dataArr[$i]["SUBCLASSES"] = $subclasses;

                    //学籍学習記録データの作成（ＣＳＶデータより読込)
                    $result = $db->query(knje061aQuery::addQuerySchregStudyRecDat($this->dataArr[$i]));
                    if (PEAR::isError($result)) {
                        continue;
                    }
                    $result = $db->query(knje061aQuery::deleteStudyRecRemark2($this->dataArr[$i]));
                    if (PEAR::isError($result)) {
                        continue;
                    }
                    $result = $db->query(knje061aQuery::insertStudyRecRemark2($this->dataArr[$i]));
                    if (PEAR::isError($result)) {
                        continue;
                    }
                    $db->popExpect();
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
            }
            $db->commit();
            Query::dbCheckIn($db);
        } elseif ($this->field["KIND"] == 2 && $this->field["METHOD"] == 1) { //"成績データ抽出中..."
            if ($this->field["CREATEDIV"] == 1) {
                //新規作成
                knje061aQuery::addRecordSchregStudyrecDat($this);
            } elseif ($this->field["CREATEDIV"] == 2) {
                //追加作成
                knje061aQuery::addRecordSchregStudyrecDat2($this);
            } else {
                //上書作成
                knje061aQuery::addRecordSchregStudyrecDat3($this);
            }
            $this->message = "学籍学習記録データの生成が完了しました。";
        } elseif ($this->field["KIND"] == 1) { //"勤怠データ抽出＆集計中..."
            //出欠学期別累積データより集計
            knje061aQuery::addRecordSchregAttendrecDat($this);
            $this->message = "学籍出欠記録データの生成が完了しました。";
        }
    }

    public function fileUpload()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $tempArr = array();
        if (is_uploaded_file($this->file["tmp_name"])) {
            $fp = fopen($this->file["tmp_name"], "r");
            //学籍学習記録データのサイズ
            $checkdata = array( "SCHOOLCD"          => array(1,"本校区分"),
                                "GRADE"             => array("学年"),
                                "HR_CLASS"          => array("組"),
                                "ATTENDNO"          => array("出席番号"),
                                "SCHREGNO"          => array(8,"学籍番号"),
                                "NAME"              => array("生徒氏名"),
                                "SEX"               => array("性別区分"),
                                "SEX_NAME"          => array("性別"),
                                "YEAR"              => array(4,"年度"),
                                "ANNUAL"            => array(2,"履修学年"),
                                "COURSECD"          => array("課程コード"),
                                "MAJORCD"           => array("学科コード"),
                                "COURSECODE"        => array("コースコード"),
                                "COURSECODENAME"    => array("コースコード名"),
                                "CLASSCD"           => array(2, "教科コード"),
                                "CLASSNAME"         => array(30,"教科名"),
                                "CLASSABBV"         => array(15,"教科略称"),
                                "CLASSNAME_ENG"     => array(40,"教科名英字"),
                                "CLASSABBV_ENG"     => array(30,"教科略称英字"),
                                "SUBCLASSCD"        => array(6,"科目コード"),
                                "SUBCLASSNAME"      => array(60,"科目名"),
                                "SUBCLASSABBV"      => array(9,"科目略称"),
                                "SUBCLASSNAME_ENG"  => array(40,"科目名英字"),
                                "SUBCLASSABBV_ENG"  => array(20,"科目略称英字"),
                                "VALUATION"         => array(2,"評価"),
                                "GET_CREDIT"        => array(2,"標準修得単位"),
                                "ADD_CREDIT"        => array(2,"増加修得単位"),
                                "COMP_CREDIT"       => array(2,"履修単位"),
                                "REMARK"            => array(60,"履修科目備考"),
                                "SCHOOL_KIND"       => array(2, "学校校種"),
                                "CURRICULUM_CD"     => array(2, "教育課程コード"));

            $i = 0;
            $line = 1;
            $err = array();
            $header = false;

            //取得主キー
            $primaryKey1    = array();
            $duplicateCsv1  = array();
            $primaryKey2    = array();
            $duplicateCsv2  = array();
            $primaryKey1 = $this->getPrimaryKey('SCHREG_STUDYREC_DAT');
            $primaryKey2 = $this->getPrimaryKey('STUDYRECREMARK_DAT');

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
                    $validate = true;

                    $data = array(  "SCHOOLCD"        => $tmp[0],
                                    "YEAR"            => $tmp[8],
                                    "SCHREGNO"        => $tmp[4],
                                    "ANNUAL"          => $tmp[9],
                                    "CLASSCD"         => $tmp[14],
                                    "SUBCLASSCD"      => $tmp[19],
                                    "CLASSNAME"       => $tmp[15],
                                    "CLASSABBV"       => $tmp[16],
                                    "CLASSNAME_ENG"   => $tmp[17],
                                    "CLASSABBV_ENG"   => $tmp[18],
                                    "SUBCLASSNAME"    => $tmp[20],
                                    "SUBCLASSABBV"    => $tmp[21],
                                    "SUBCLASSNAME_ENG"=> $tmp[22],
                                    "SUBCLASSABBV_ENG"=> $tmp[23],
                                    "VALUATION"       => $tmp[24],
                                    "GET_CREDIT"      => $tmp[25],
                                    "ADD_CREDIT"      => $tmp[26],
                                    "COMP_CREDIT"     => $tmp[27],
                                    "REMARK"          => $tmp[28],
                                    "SCHOOL_KIND"     => $tmp[29],
                                    "CURRICULUM_CD"   => $tmp[30]);

                    $data = str_replace("'", "''", $data);

                    $db->expectError();
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $tempArr[$i] = $data;
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
            $db->commit();
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

        foreach ($checkdata as $key => $val) {
            if (in_array($key, array("SCHOOLCD", "SCHREGNO", "YEAR", "ANNUAL", "CLASSCD", "SUBCLASSCD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
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

    //テンプレートの書き出し
    public function outputTmpFile()
    {
        $contents  ="※本校区分,学年,組,出席番号,※学籍番号,生徒氏名,性別区分,性別,";
        $contents .="※年度,※履修学年,課程コード,学科コード,コースコード,コースコード名,";
        $contents .="※教科コード,教科名,教科略称,教科名英字,教科略称英字,";
        $contents .="※科目コード,科目名,科目略称,科目名英字,科目略称英字,";
        $contents .="評価,標準修得単位,増加修得単位,履修単位,履修科目備考,学校校種,教育課程コード,LASTCOLUMN\n";
        $contents .="0,01,001,001,20051003,今井花子,2,女性,";
        $contents .="2005,01,1,001,0001,文Ⅰ,";
        $contents .="13,公民,公民,Civics,Civ,";
        $contents .="130001,現代社会,現代社,Civics,Civ,";
        $contents .="2,2,2,2,備考です,J,3,".$this->lastColumn;

        $filename = sprintf("%s.csv", "学籍学習記録データ");
        common::downloadContents($contents, $filename);
        return true;
    }
}
