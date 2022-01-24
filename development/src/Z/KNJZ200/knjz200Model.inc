<?php

require_once('for_php7.php');
//定数定義
class knjz200Model extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $course_pk;         //primary
    public $sch_kind;
    public $subclasscd;
    public $coursename;
    public $replace = array();    //一括更新処理データ
    //教育課程用
    public $classcd;
    public $school_kind;
    public $curriculum_cd;
    public $file;
    public $lastColumn;//CSVの最後の列の値

    public function knjz200Model()
    {
    }

    public function init()
    {
        common::GetControlMaster_Fnc($this->control);

        $this->cmd        = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->file = VARS::file("FILE");

        if (VARS::get("PROGRAMID")) {
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->classcd="";
                $this->school_kind="";
                $this->curriculum_cd="";
            }
            $this->subclasscd="";
            $this->coursename="";
            $this->sch_kind="";
            $this->replace = array();
        }

        if (VARS::post("COURSENAME")) {
            $this->coursename   = VARS::post("COURSENAME");
        }
        if (VARS::post("SCH_KIND")) {
            $this->sch_kind     = VARS::post("SCH_KIND");
        }
        if (VARS::get("COURSENAME")) {
            $this->coursename   = VARS::get("COURSENAME");
        }
        if (VARS::get("SCH_KIND")) {
            $this->sch_kind     = VARS::get("SCH_KIND");
        }

        $this->field = array("SUBCLASSCD"           =>  VARS::post("SUBCLASSCD"),
                             "CREDITS"              =>  VARS::post("CREDITS"),
                             "ABSENCE_HIGH"         =>  VARS::post("ABSENCE_HIGH"),
                             "GET_ABSENCE_HIGH"     =>  VARS::post("GET_ABSENCE_HIGH"),
                             "ABSENCE_WARN"         =>  VARS::post("ABSENCE_WARN"),
                             "ABSENCE_WARN2"        =>  VARS::post("ABSENCE_WARN2"),
                             "ABSENCE_WARN3"        =>  VARS::post("ABSENCE_WARN3"),
                             "REQUIRE_FLG"          =>  VARS::post("REQUIRE_FLG"),
                             "AUTHORIZE_FLG"        =>  VARS::post("AUTHORIZE_FLG"),
                             "COMP_UNCONDITION_FLG" =>  VARS::post("COMP_UNCONDITION_FLG"),
                             "TIME_UNIT"            =>  VARS::post("TIME_UNIT"),
                             "RATE"                 =>  VARS::post("RATE"),
                             "UPDATED"              =>  VARS::post("UPDATED"),
                             "GRADE"                =>  VARS::request("GRADE"),
                             "YEAR"                 =>  CTRL_YEAR,
                             "HEADER"               =>  VARS::post("HEADER"),              //ヘッダの有無
                             "OUTPUT"               =>  VARS::post("OUTPUT")               //処理の種類(1:取込、2:書出、3:エラー出力)
                             );

        if (VARS::get("SUBCLASSCD")) {
            $this->subclasscd   = VARS::get("SUBCLASSCD");
        }
        //教育課程用
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (VARS::get("CLASSCD")) {
                $this->classcd        = VARS::get("CLASSCD");
            }
            if (VARS::get("SCHOOL_KIND")) {
                $this->school_kind        = VARS::get("SCHOOL_KIND");
            }
            if (VARS::get("CURRICULUM_CD")) {
                $this->curriculum_cd        = VARS::get("CURRICULUM_CD");
            }
        }
        if (VARS::get("cmd") == "edit" || $this->cmd == "reset") {
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->field["SUBCLASSCD"] = $this->classcd.'-'.$this->school_kind.'-'.$this->curriculum_cd.'-'.$this->subclasscd;
            } else {
                $this->field["SUBCLASSCD"] = $this->subclasscd;
            }
        }

        //一括処理用
        $this->replaceCheckBoxName = array(
            "CREDITS",
            "ABSENCE_HIGH",
            "ABSENCE_WARN",
            "ABSENCE_WARN2",
            "ABSENCE_WARN3",
            "REQUIRE_FLG",
            "AUTHORIZE_FLG",
            "COMP_UNCONDITION_FLG",
            "TIME_UNIT",
            "RATE",
        );

        //一括処理 更新押下
        if ($this->cmd == "replace_update") {
            $this->replace["selectdata_course"]     = VARS::post("selectdata_course");
            $this->replace["selectdata_subclass"]   = VARS::post("selectdata_subclass");
            $this->replace["field"] = $this->replace["data_chk"] = array();
            foreach ($this->replaceCheckBoxName as $key) {
                $this->replace["data_chk"][$key] = VARS::post("RCHECK_".$key);
                if ($this->replace["data_chk"][$key] == "1") {
                    if ($key == "ABSENCE_HIGH") {
                        $this->replace["field"]["ABSENCE_HIGH"]     = VARS::post("ABSENCE_HIGH");
                        $this->replace["field"]["GET_ABSENCE_HIGH"] = VARS::post("GET_ABSENCE_HIGH");
                    } else {
                        $this->replace["field"][$key] = VARS::post($key);
                    }
                }
            }
            $this->replace["check_all"] = VARS::post("RCHECK_ALL");
        }

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //１レコード取得
        $db = Query::dbCheckOut();
        $row = knjz200Query::chkCODE($this, $db);
        $Row = knjz200Query::getRow($this, $db);
        $regdSchoolKind = knjz200Query::getSchregRegdGdatSchoolKind($this, $db);
        Query::dbCheckIn($db);
        
        //update,delete共通のチェック
        $check_cd = "";
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $check_cd = $this->classcd.'-'.$this->school_kind.'-'.$this->curriculum_cd.'-'.$this->subclasscd;
        } else {
            $check_cd = $this->subclasscd;
        }
        if ($this->cmd=="update" || $this->cmd=="delete") {
            //リストより選択していない
            if ($check_cd=="") {
                $this->setWarning("MSG308");
                return false;
            }
            //キー項目を変更した場合
            if ($check_cd != $this->field["SUBCLASSCD"]) {
                if ($this->cmd == "update" && is_array($row)) {
                    $this->setWarning("MSG308");
                    return false;
                }
            }
            //キー変更をしての削除
            if ($check_cd != $this->field["SUBCLASSCD"]) {
                $this->setWarning("MSG308");
                return false;
            }

            if ($this->Properties["useTimeUnit"] !== '1') {
                //更新日付チェック1
                if ($this->cmd == "update" && $Row["UPDATED"] == "") {
                    $this->setWarning("MSG303");
                    return false;
                }

                //更新日付チェック2
                if ($this->cmd == "update" && $Row["UPDATED"] != $this->field["UPDATED"]) {
                    $this->setWarning("MSG307");
                    return false;
                }
            }
        }
        if ($this->cmd=="update" || $this->cmd=="add") {
            //必須項目チェック
            if ($this->field["CREDITS"]=="" && $regdSchoolKind == 'H') {
                $this->setWarning("MSG301", "(単位数)");
                return false;
            }
            //重複チェック
            if ($this->cmd == "add" && is_array($row)) {
                $this->setWarning("MSG918");
                return false;
            }
            //無条件チェック
            if ($this->field["AUTHORIZE_FLG"]=="1" && $this->field["COMP_UNCONDITION_FLG"]=="1") {
                $this->setWarning("MSG901", "(無条件履修修得フラグ)\\n半期認定は、無条件にはできません。");
                return false;
            }
            //掛け率チェック
            if ($this->field["RATE"] > 1) {
                $this->setWarning("MSG901", "(掛け率)\\n掛け率は、1以下の値を入力してください。");
                return false;
            }
        }
        return true;
    }

    //前年度からのコピーのモデルを返します
    public function &getCopyModel()
    {
        $db = Query::dbCheckOut();
        //学籍在籍データ件数
        list($coursecode, $coursecd, $majorcd, $grade) = explode(" ", $this->coursename);
        $regd_cnt = $db->getOne(knjz200Query::getRegdDatCnt($grade));
        $flg = ($regd_cnt > 0) ? "" : 1;

        $result    = $db->query(knjz200Query::getOldYear($this, $flg, $coursecode));    //前年度のデータ取得
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $db->query(knjz200Query::copyInsert($row, $this));              //前年度のデータを今年度のデータとしてINSERT
        }
        Query::dbCheckIn($db);
    }

    //編集完了用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
        }
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->delete();
        }
        return;
    }

    //DELETE時の内部メソッド
    public function &delete()
    {
        knjz200Query::getDeleteQuery($this);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->classcd          = "";
                $this->school_kind      = "";
                $this->curriculum_cd    = "";
                $this->subclasscd       = "";
            }
            $this->field = array();
        }
    }

    //INSERT・UPDATE時の内部メソッド
    public function &insert()
    {
        $db = Query::dbCheckOut();
        //授業時数のフラグ  欠課数上限値の入力可、付加の判定に使う
        $query = knjz200Query::getJugyouJisuFlg();
        $this->jugyou_jisu_flg = $db->getOne($query); //1:法定授業 2:実授業
        Query::dbCheckIn($db);

        $result = knjz200Query::getInsertQuery($this);
        if (!isset($this->warning)) {
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->classcd          = substr($this->field["SUBCLASSCD"], 0, 2);
                $this->school_kind      = substr($this->field["SUBCLASSCD"], 3, 1);
                $this->curriculum_cd    = substr($this->field["SUBCLASSCD"], 5, 1);
                $this->subclasscd       = substr($this->field["SUBCLASSCD"], 7, 6);
            } else {
                $this->subclasscd = $this->field["SUBCLASSCD"];
            }
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //一括処理
    public function &replaceModel()
    {
        //課程学科、科目が選択されてない場合
        if (!strlen($this->replace["selectdata_course"])) {
            $this->setWarning("MSG304", "　　（課程学科）");
            return false;
        }

        //課程学科、科目が選択されてない場合
        if (!strlen($this->replace["selectdata_subclass"])) {
            $this->setWarning("MSG304", "　　（科目）");
            return false;
        }

        //掛け率チェック
        if ($this->replace["data_chk"]["RATE"] && $this->field["RATE"] > 1) {
            $this->setWarning("MSG901", "(掛け率)\\n掛け率は、1以下の値を入力してください。");
            return false;
        }

        //更新
        $this->repUpdate();
        $this->setMessage("MSG201");

        return;
    }

    //単位マスタの一括更新
    public function &repUpdate()
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        $course = explode(",", $this->replace["selectdata_course"]);
        $subclass = explode(",", $this->replace["selectdata_subclass"]);

        for ($i = 0; $i < get_count($course); $i++) {
            for ($j = 0; $j < get_count($subclass); $j++) {
                $query = knjz200Query::getRepUpdateQuery($db, $this, $this->replace["field"], $course[$i], $subclass[$j]);
                $db->query($query);
            }
        }
        $db->commit();

        Query::dbCheckIn($db);
        return;
    }
    
    //CVSファイル
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        if ($this->field["OUTPUT"] == "3") {
            $headerDuplicate = array("YEAR"                 => "年度",
                                    "GRADE"                  => "学年",
                                    "COURSECD"               => "学部コード",
                                    "MAJORCD"                => "学科コード",
                                    "COURSECODE"             => "コースコード",
                                    "CLASSCD"                => "教科コード",
                                    "SCHOOL_KIND"            => "学校校種",
                                    "CURRICULUM_CD"          => "教育課程コード",
                                    "SUBCLASSCD"             => "科目コード");
            //ヘッダ、エラー出力
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");

            $tables = array();
            $tables = array("1" => "CREDIT_MST");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header, ",")."\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } elseif ($this->field["OUTPUT"] == "2" || $this->field["OUTPUT"] == "4") {
            //学期数取得
            //欠課数オーバーのタイトル
            $semHeader = "";
            $semContents = "";
            $timeUnitHeader = "";
            $timeContents = "";
            $rateHeader = "";
            $rateContents = "";
            $conma = ",";
            if (in_array("1", $this->control["SEMESTER"])) {
                if (in_array("2", $this->control["SEMESTER"])) {
                    $semHeader  .= $this->control["学期名"]["1"]."欠課数オーバの警告".$conma;
                    $semContents .= "1".$conma;
                } else {
                    $semHeader  .= $this->control["学期名"]["1"]."欠課数オーバの警告";
                    $semContents .= "1";
                }
            }
            if (in_array("2", $this->control["SEMESTER"])) {
                if (in_array("3", $this->control["SEMESTER"])) {
                    $semHeader  .= $this->control["学期名"]["2"]."欠課数オーバの警告".$conma;
                    $semContents .= "2".$conma;
                } else {
                    $semHeader  .= $this->control["学期名"]["2"]."欠課数オーバの警告";
                    $semContents .= "2";
                }
            }
            if (in_array("3", $this->control["SEMESTER"])) {
                $semHeader  .= $this->control["学期名"]["3"]."欠課数オーバの警告";
                $semContents .= "3";
            }
            //時間単位
            if ($this->Properties["useTimeUnit"] == '1') {
                $timeUnitHeader = '単位時間'.$conma;
                $timeContents = '156.5'.$conma;
            }
            //掛け率
            if ($this->Properties["useMultiplicationRate"] == '1') {
                $rateHeader = '掛け率'.$conma;
                $rateContents = '0.9'.$conma;
            }
            //ヘッダ、書出
            $header = array("※年度",                           //0
                            "※学年",                           //1
                            "※学部コード",                     //2
                            "※学科コード",                     //3
                            "※コースコード",                   //4
                            "※教科コード",                     //5
                            "※学校校種",                       //6
                            "※教育課程コード",                 //7
                            "※科目コード",                     //8
                            "※単位数",                         //9
                            "法定欠課数上限値の履修",           //10
                            "法定欠課数上限値の習得",           //11
                            $semHeader.",".                     //12 MAX12～14
                            "必履修区分",                       //15
                            "半期認定フラグ",                   //16
                            "無条件履修修得フラグ",             //17
                            $timeUnitHeader.                    //18
                            $rateHeader.                        //19
                            $this->lastColumn
                            );
            if ($this->field["HEADER"]) {
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }
            $query = knjz200Query::getCsvData($this);
        }

        if ($this->field["OUTPUT"] == "4") {
            $contents .= CTRL_YEAR.",";
            $contents .= "01,";
            $contents .= "02,";
            $contents .= "001,";
            $contents .= "0001,";
            $contents .= "01,";
            if ($this->Properties["use_prg_schoolkind"] == "1") {
                if ($this->selectSchoolKind) {
                    $contents .= $this->selectSchoolKind.",";
                } else {
                    $contents .= "H,";
                }
            } elseif ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $contents .= SCHOOLKIND.",";
            } else {
                $contents .= "H,";
            }
            $contents .= "3,";
            $contents .= "010001,";
            $contents .= "2,";
            $contents .= "33.0,";
            $contents .= "25.5,";
            $contents .= $semContents.",";
            $contents .= "2,";
            $contents .= "1,";
            $contents .= "1,";
            $contents .= $timeContents;
            $contents .= $rateContents;
            $contents .= $this->lastColumn."\n";

            $data = true;
        } else {
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあれば置換、置換する文字はカンマでなければ何でも良い。
                $contents .= implode($row, ",") ."\n";
                $data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        if ($data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse="単位マスタ一覧エラー";
            } else {
                $couse="単位マスタ一覧";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //CVSファイル
    //クライアント ⇒ サーバ
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
                knjz200Query::insertQueryCsv($db, $this, $this->data_arr);
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //送られたファイルの文字列を取得
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("YEAR"                   => array(4  ,"年度"),
                               "GRADE"                  => array(2  ,"学年"),
                               "COURSECD"               => array(1  ,"学部コード"),
                               "MAJORCD"                => array(3  ,"学科コード"),
                               "COURSECODE"             => array(4  ,"コースコード"),
                               "CLASSCD"                => array(2  ,"教科コード"),
                               "SCHOOL_KIND"            => array(2  ,"学校校種"),
                               "CURRICULUM_CD"          => array(2  ,"教育課程コード"),
                               "SUBCLASSCD"             => array(6  ,"科目コード"),
                               "CREDITS"                => array(2  ,"単位数"),
                               "ABSENCE_HIGH"           => array(6  ,"法定欠課数上限値の履修"),
                               "GET_ABSENCE_HIGH"       => array(6  ,"法定欠課数上限値の習得"),
                               "REQUIRE_FLG"            => array(1  ,"必履修区分"),
                               "AUTHORIZE_FLG"          => array(1  ,"半期認定フラグ"),
                               "COMP_UNCONDITION_FLG"   => array(1  ,"無条件履修修得フラグ")
                               );
            //欠課数オーバ
            $tmpCount = "11";
            if (in_array("1", $this->control["SEMESTER"])) {
                $tmpCount++;
                $setCount1 = $tmpCount;
                $checkdata = $checkdata + array("ABSENCE_WARN"  => array(2, $this->control["学期名"]["1"]."欠課数オーバの警告"));
            }
            if (in_array("2", $this->control["SEMESTER"])) {
                $tmpCount++;
                $setCount2 = $tmpCount;
                $checkdata = $checkdata + array("ABSENCE_WARN2"  => array(2, $this->control["学期名"]["2"]."欠課数オーバの警告"));
            }
            if (in_array("3", $this->control["SEMESTER"])) {
                $tmpCount++;
                $setCount3 = $tmpCount;
                $checkdata = $checkdata + array("ABSENCE_WARN3"  => array(2, $this->control["学期名"]["3"]."欠課数オーバの警告"));
            }
            //REQUIRE_FLGの配列番号
            $tmpCount++;
            $setRequireCount = $tmpCount;
            //AUTHORIZE_FLGの配列番号
            $tmpCount++;
            $setAuthorizeCount = $tmpCount;
            //COMP_UNCONDITION_FLGの配列番号
            $tmpCount++;
            $setCompCount = $tmpCount;
            //時間単位
            if ($this->Properties["useTimeUnit"] == '1') {
                $tmpCount++;
                $setTimeCount = $tmpCount;
                $checkdata = $checkdata + array("TIME_UNIT"  => array(6, "単位時間"));
            }
            //掛け率
            if ($this->Properties["useMultiplicationRate"] == '1') {
                $tmpCount++;
                $setTimeCount = $tmpCount;
                $checkdata = $checkdata + array("RATE"  => array(3, "掛け率"));
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
            $primaryKey1 = $this->getPrimaryKey('CREDIT_MST');

            $duplicateCsv1 = array();
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
                    $data = array("YEAR"                => $tmp[0],
                                  "GRADE"               => (strlen(trim($tmp[1]))) ? sprintf("%02d", $tmp[1]) : "",
                                  "COURSECD"            => $tmp[2],
                                  "MAJORCD"             => (strlen(trim($tmp[3]))) ? sprintf("%03d", $tmp[3]) : "",
                                  "COURSECODE"          => (strlen(trim($tmp[4]))) ? sprintf("%04d", $tmp[4]) : "",
                                  "CLASSCD"             => (strlen(trim($tmp[5]))) ? sprintf("%02d", $tmp[5]) : "",
                                  "SCHOOL_KIND"         => $tmp[6],
                                  "CURRICULUM_CD"       => $tmp[7],
                                  "SUBCLASSCD"          => (strlen(trim($tmp[8]))) ? sprintf("%06d", $tmp[8]) : "",
                                  "CREDITS"             => $tmp[9],
                                  "ABSENCE_HIGH"        => $tmp[10],
                                  "GET_ABSENCE_HIGH"    => $tmp[11],
                                  //学期欠課オーバ数がセット
                                  "REQUIRE_FLG"         => $tmp[$setRequireCount],
                                  "AUTHORIZE_FLG"       => $tmp[$setAuthorizeCount],
                                  "COMP_UNCONDITION_FLG"=> $tmp[$setCompCount]
                                  );
                    //欠課数オーバ
                    if (in_array("1", $this->control["SEMESTER"])) {
                        $data = $data + array("ABSENCE_WARN"  => $tmp[$setCount1]);
                    }
                    if (in_array("2", $this->control["SEMESTER"])) {
                        $data = $data + array("ABSENCE_WARN2"  => $tmp[$setCount2]);
                    }
                    if (in_array("3", $this->control["SEMESTER"])) {
                        $data = $data + array("ABSENCE_WARN3"  => $tmp[$setCount3]);
                    }
                    //時間単位
                    if ($this->Properties["useTimeUnit"] == '1') {
                        $data = $data + array("TIME_UNIT"  => $tmp[$setTimeCount]);
                    }
                    //掛け率
                    if ($this->Properties["useMultiplicationRate"] == '1') {
                        $data = $data + array("RATE"  => $tmp[$setTimeCount]);
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
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;  //必須エラーフラグ
        $numericErrFlg      = false;  //数値エラーフラグ
        $lengthErrFlg       = false;  //レングスエラーフラグ
        $orderErrFlg        = false;  //固有エラーフラグ
        $masterErrFlg       = false;  //マスタエラーフラグ
        $duplicateCsvErrFlg = false;  //重複(CSV)エラーフラグ
        $valuesErrFlg       = false;

        $hissuErrMsg        = "";     //必須エラーメッセージ
        $numericErrMsg      = "";     //数値エラーメッセージ
        $lengthErrMsg       = "";     //レングスエラーメッセージ
        $orderErrMsg        = "";     //固有エラーメッセージ
        $masterErrMsg       = "";     //マスタエラーメッセージ
        $duplicateCsvErrMsg = "";     //重複(CSV)メッセージ
        $valuesErrMsg       = "";

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $orderSep        = "";
        $masterSep       = "";
        $duplicateCsvSep = "";
        $valuesSep       = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR", "GRADE", "COURSECD", "MAJORCD", "COURSECODE", "CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD", "CREDITS"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //数値チェック
            if (in_array($key, array("YEAR", "GRADE", "COURSECD", "MAJORCD", "COURSECODE", "CLASSCD", "CURRICULUM_CD", "SUBCLASSCD", "CREDITS", "ABSENCE_HIGH", "GET_ABSENCE_HIGH", "ABSENCE_WARN", "ABSENCE_WARN2", "ABSENCE_WARN3", "TIME_UNIT", "RATE"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
            //フラグの値、無条件チェック
            if (in_array($key, array("AUTHORIZE_FLG", "COMP_UNCONDITION_FLG"))) {
                if ($data[$key] != "" && $data[$key] != "1") {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }
                if ($data["AUTHORIZE_FLG"]=="1" && $data["COMP_UNCONDITION_FLG"]=="1") {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, $val[1]);
                }
            }
            //掛け率の制限チェック
            if (in_array($key, array("RATE"))) {
                if ($data[$key] != "" && $data[$key] > 1) {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }
            }
        }

        //存在チェック
        if ($this->Properties["use_prg_schoolkind"] == "1" || ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "")) {
            //学年
            $grade_cnt = $db->getOne(knjz200Query::checkGrade($this, $data));
            if ($grade_cnt == 0) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $checkdata["GRADE"][1]);
            }
            if ($this->Properties["use_prg_schoolkind"] == "1") {
                if ($this->selectSchoolKind == '' || in_array($data["SCHOOL_KIND"], explode(':', $this->selectSchoolKind))) {
                    //科目
                    $subclass_cnt = $db->getOne(knjz200Query::checkSubclass($this, $data));
                    if ($subclass_cnt == 0) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $checkdata["SUBCLASSCD"][1]);
                    }
                } else {
                    //校種
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $checkdata["SCHOOL_KIND"][1]);
                }
            } elseif ($this->Properties["useSchool_KindField"] == "1") {
                if ($data["SCHOOL_KIND"] == SCHOOLKIND) {
                    //科目
                    $subclass_cnt = $db->getOne(knjz200Query::checkSubclass($this, $data));
                    if ($subclass_cnt == 0) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $checkdata["SUBCLASSCD"][1]);
                    }
                } else {
                    //校種
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $checkdata["SCHOOL_KIND"][1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$masterErrMsg.$numericErrMsg.$lengthErrMsg.$orderErrMsg.$valuesErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjz200Query::insertQueryErr($db, $line, $checkError);
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
