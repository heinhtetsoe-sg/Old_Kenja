<?php

require_once('for_php7.php');

class knje360jModel extends Model
{
    public $cmd;
    public $schregno = "";
    public $name = "";
    public $grade;
    public $hr_class;
    public $attendno;
    public $field = array();
    public $control;
    public $mode;

    public $entrydate;
    public $seq;
    public $checked;
    public $data = array();
    public $replace = array();    //一括更新処理データ
    public $type;

    public function knje360jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
            unset($this->mode);

            unset($this->entrydate);
            unset($this->seq);
            unset($this->type);
        }
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        $this->finschoolcdKeta = ($this->Properties["useFinschoolcdFieldSize"] == '12' ? 12 : 7);

        $this->field = array("ENTRYDATE"            =>  VARS::post("ENTRYDATE"),            //登録日
                             "SEQ"                  =>  VARS::post("SEQ"),                  //SEQ
                             "COURSE_KIND"          =>  VARS::post("COURSE_KIND"),          //進路種別
                             "QUESTIONNAIRECD"      =>  VARS::post("QUESTIONNAIRECD"),      //調査コード
                             "REMARK"               =>  VARS::post("REMARK"),               //備考

                             "JOBTYPE_LCD1"         =>  VARS::post("JOBTYPE_LCD1"),         //職業種別（大分類）
                             "JOBTYPE_MCD1"         =>  VARS::post("JOBTYPE_MCD1"),         //職業種別（中分類）
                             "JOBTYPE_SCD1"         =>  VARS::post("JOBTYPE_SCD1"),         //職業種別（小分類）
                             "WORK_AREA1"           =>  VARS::post("WORK_AREA1"),           //就業場所
                             "INTRODUCTION_DIV1"    =>  VARS::post("INTRODUCTION_DIV1"),    //紹介区分
                             "JOBTYPE_LCD2"         =>  VARS::post("JOBTYPE_LCD2"),         //職業種別（大分類）
                             "JOBTYPE_MCD2"         =>  VARS::post("JOBTYPE_MCD2"),         //職業種別（中分類）
                             "JOBTYPE_SCD2"         =>  VARS::post("JOBTYPE_SCD2"),         //職業種別（小分類）
                             "WORK_AREA2"           =>  VARS::post("WORK_AREA2"),           //就業場所
                             "INTRODUCTION_DIV2"    =>  VARS::post("INTRODUCTION_DIV2"),    //紹介区分

                             "TOROKU_DATE"          =>  VARS::post("TOROKU_DATE"),          //登録日
                             "THINKEXAM"            =>  VARS::post("THINKEXAM"),            //指導要録に表記する進路先
                             "FINSCHOOLCD"          =>  VARS::request("FINSCHOOLCD") ? sprintf("%0".$this->finschoolcdKeta."s", VARS::request("FINSCHOOLCD")) : "",          //学校コード
                             "FACULTYCD"            =>  VARS::request("FACULTYCD") ? sprintf("%03d", VARS::request("FACULTYCD")) : "",          //学部コード
                             "DEPARTMENTCD"         =>  VARS::request("DEPARTMENTCD") ? sprintf("%03d", VARS::request("DEPARTMENTCD")) : "",    //学科コード
                             "BUNAME"               =>  VARS::post("BUNAME"),               //学科
                             "PREF_CD"              =>  VARS::post("PREF_CD"),              //所在地
                             "STAT_DATE1"           =>  VARS::request("STAT_DATE1"),        //入試日
                             "STAT_DATE3"           =>  VARS::request("STAT_DATE3"),        //合格発表日
                             "EXAMNO"               =>  VARS::post("EXAMNO"),               //受験番号
                             "CONTENTEXAM"          =>  VARS::post("CONTENTEXAM"),          //備考1
                             "REASONEXAM"           =>  VARS::post("REASONEXAM"),           //備考2
                             "HOWTOEXAM"            =>  VARS::post("HOWTOEXAM"),            //受験方式
                             "DECISION"             =>  VARS::post("DECISION"),             //受験結果
                             "PLANSTAT"             =>  VARS::post("PLANSTAT"),             //進路状況
                             "ISSUE"                =>  VARS::post("ISSUE"),                //調査書発行

                             "SENKOU_NO"            =>  VARS::post("SENKOU_NO"),            //求人番号
                             "STAT_CD"              =>  VARS::post("STAT_CD"),              //会社コード
                             "INTRODUCTION_DIV"     =>  VARS::post("INTRODUCTION_DIV"),     //紹介区分
                             "JOBTYPE_LCD"          =>  VARS::post("JOBTYPE_LCD"),          //職業別大
                             "JOBTYPE_MCD"          =>  VARS::post("JOBTYPE_MCD"),          //職業別中
                             "JOBTYPE_SCD"          =>  VARS::post("JOBTYPE_SCD"),          //職業別小
                             "JOB_THINK"            =>  VARS::post("JOB_THINK"),            //指導要録に表記する連絡先
                             "REMARK1"              =>  VARS::post("REMARK1"),              //備考1
                             "REMARK2"              =>  VARS::post("REMARK2"),              //備考2

                             "SENKOU_KIND_SUB"      =>  VARS::post("SENKOU_KIND_SUB"),      //その他進路

                             "TITLE"                =>  VARS::post("TITLE"),                //相談件名
                             "CONTENTS"             =>  VARS::post("CONTENTS"),             //相談内容

                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        for ($i=1; $i<7; $i++) {
            $this->field["SCHOOL_GROUP".$i]         = VARS::post("SCHOOL_GROUP".$i);
            $this->field["FACULTY_GROUP".$i]        = VARS::post("FACULTY_GROUP".$i);
            $this->field["DEPARTMENT_GROUP".$i]     = VARS::post("DEPARTMENT_GROUP".$i);
            $this->field["SCHOOL_CD".$i]            = VARS::post("SCHOOL_CD".$i);
            $this->field["FACULTYCD".$i]            = VARS::post("FACULTYCD".$i);
            $this->field["DEPARTMENTCD".$i]         = VARS::post("DEPARTMENTCD".$i);
            $this->field["HOWTOEXAM".$i]            = VARS::post("HOWTOEXAM".$i);
        }

        $this->checked = VARS::request("CHECKED");     //チェック
        $this->cmd = VARS::request("cmd");

        $this->seq_list = VARS::post("SEQ_LIST");

        //一括入力処理（受験報告・進学）
        $this->fields = array();

        //一括更新処理（進路調査・進学）
        if ($this->cmd == "replace_update1") {
            $this->replace["selectdata"] = VARS::post("selectdata");
            $this->replace["field"] = $this->replace["data_chk"] = array();
            $this->replace["data_chk"] = array(VARS::post("RCHECK0"),
                                               VARS::post("RCHECK1"),
                                               VARS::post("RCHECK2"),
                                               VARS::post("RCHECK3"),
                                               VARS::post("RCHECK4"),
                                               VARS::post("RCHECK5"),
                                               VARS::post("RCHECK6"),
                                               VARS::post("RCHECK7"),
                                               VARS::post("RCHECK8"));
            $this->replace["check_all"] = VARS::post("RCHECK9");

            $this->replace["field"]["ENTRYDATE"]        = VARS::post("ENTRYDATE");
            $this->replace["field"]["COURSE_KIND"]      = VARS::post("COURSE_KIND");
            $this->replace["field"]["QUESTIONNAIRECD"]  = VARS::post("QUESTIONNAIRECD");
            $this->replace["field"]["SCHOOL_GROUP1"]    = VARS::post("SCHOOL_GROUP1");

            if (VARS::post("RCHECK0")=="1") {
                $this->replace["field"]["FACULTY_GROUP1"]       = VARS::post("FACULTY_GROUP1");
            }
            if (VARS::post("RCHECK1")=="1") {
                $this->replace["field"]["DEPARTMENT_GROUP1"]    = VARS::post("DEPARTMENT_GROUP1");
            }
            if (VARS::post("RCHECK2")=="1") {
                $this->replace["field"]["FINSCHOOLCD1"]         = VARS::post("FINSCHOOLCD1");
                $this->replace["field"]["FACULTYCD1"]           = VARS::post("FACULTYCD1");
                $this->replace["field"]["DEPARTMENTCD1"]        = VARS::post("DEPARTMENTCD1");
            }
            if (VARS::post("RCHECK3")=="1") {
                $this->replace["field"]["HOWTOEXAM1"]           = VARS::post("HOWTOEXAM1");
            }
            if (VARS::post("RCHECK4")=="1") {
                $this->replace["field"]["SCHOOL_GROUP2"]        = VARS::post("SCHOOL_GROUP2");
            }
            if (VARS::post("RCHECK5")=="1") {
                $this->replace["field"]["FACULTY_GROUP2"]       = VARS::post("FACULTY_GROUP2");
            }
            if (VARS::post("RCHECK6")=="1") {
                $this->replace["field"]["DEPARTMENT_GROUP2"]    = VARS::post("DEPARTMENT_GROUP2");
            }
            if (VARS::post("RCHECK7")=="1") {
                $this->replace["field"]["FINSCHOOLCD2"]         = VARS::post("FINSCHOOLCD2");
                $this->replace["field"]["FACULTYCD2"]           = VARS::post("FACULTYCD2");
                $this->replace["field"]["DEPARTMENTCD2"]        = VARS::post("DEPARTMENTCD2");
            }
            if (VARS::post("RCHECK8")=="1") {
                $this->replace["field"]["HOWTOEXAM2"]           = VARS::post("HOWTOEXAM2");
            }
        }

        //一括更新処理（受験報告・進学）
        if ($this->cmd == "replace_update2" || $this->cmd == "replace2_college" || $this->cmd == "replace2B") {
            $this->replace["selectdata"] = VARS::post("selectdata");
            $this->replace["ghr"] = VARS::post("GRADE_HR_CLASS");
            $this->replace["field"] = array();
            $this->replace["data_chk"] = array(VARS::post("RCHECK0"),
                                               VARS::post("RCHECK1"),
                                               VARS::post("RCHECK2"),
                                               VARS::post("RCHECK3"),
                                               VARS::post("RCHECK4"),
                                               VARS::post("RCHECK5"),
                                               VARS::post("RCHECK6"),
                                               VARS::post("RCHECK7"));
            $this->replace["check_all"] = VARS::post("RCHECK8");

            $this->replace["field"]["FINSCHOOLCD"]  = VARS::request("FINSCHOOLCD") ? sprintf("%0".$this->finschoolcdKeta."s", VARS::request("FINSCHOOLCD")) : "";
            $this->replace["field"]["FACULTYCD"]    = VARS::request("FACULTYCD") ? sprintf("%03d", VARS::request("FACULTYCD")) : "";
            $this->replace["field"]["DEPARTMENTCD"] = VARS::request("DEPARTMENTCD") ? sprintf("%03d", VARS::request("DEPARTMENTCD")) : "";
            $this->replace["field"]["TOROKU_DATE"]  = VARS::post("TOROKU_DATE");

            if (VARS::post("RCHECK0")=="1") {
                $this->replace["field"]["PREF_CD"]      = VARS::post("PREF_CD");
            }
            if (VARS::post("RCHECK1")=="1") {
                $this->replace["field"]["HOWTOEXAM"]    = VARS::post("HOWTOEXAM");
            }
            if (VARS::post("RCHECK2")=="1") {
                $this->replace["field"]["DECISION"]     = VARS::post("DECISION");
            }
            if (VARS::post("RCHECK3")=="1") {
                $this->replace["field"]["PLANSTAT"]     = VARS::post("PLANSTAT");
            }
            if (VARS::post("RCHECK4")=="1") {
                $this->replace["field"]["STAT_DATE1"]   = VARS::post("STAT_DATE1");
            }
            if (VARS::post("RCHECK5")=="1") {
                $this->replace["field"]["STAT_DATE3"]   = VARS::post("STAT_DATE3");
            }
            if (VARS::post("RCHECK6")=="1") {
                $this->replace["field"]["ISSUE"]        = VARS::post("ISSUE");
            }
            if (VARS::post("RCHECK7")=="1") {
                $this->replace["field"]["BUNAME"]       = VARS::post("BUNAME");
            }
        }

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
            unset($this->seq);
            unset($this->relano);
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS") != "") {
            $this->hr_class = VARS::get("HR_CLASS");
        }
        if (VARS::get("ATTENDNO") != "") {
            $this->attendno = VARS::get("ATTENDNO");
        }
        if (VARS::get("ENTRYDATE") != "") {
            $this->entrydate = str_replace("/", "-", VARS::get("ENTRYDATE"));
        }
        if (VARS::get("SEQ") != "") {
            $this->seq = VARS::get("SEQ");
        }
        if (VARS::get("TYPE") != "") {
            $this->type = VARS::get("TYPE");
        }
        if (VARS::get("mode") != "") {
            $this->mode = VARS::get("mode");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
            unset($this->mode);

            unset($this->seq);
            unset($this->entrydate);
            unset($this->type);
        }

        if ($this->cmd == "edit") {
            unset($this->seq);
            unset($this->entrydate);
        }

        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //生徒項目名切替処理
        $schName = "";
        $db = Query::dbCheckOut();
        //テーブルの有無チェック
        $query = knje360jQuery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && ($this->schregno || ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""))) {
            //生徒項目名取得
            $schName = $db->getOne(knje360jQuery::getSchName($this));
        }
        $this->sch_label = (strlen($schName) > 0) ? $schName : '生徒';
        Query::dbCheckIn($db);
    }

    // 学校・会社マスタ検索チェック
    public function chkCollegeOrCompanyMst($stat_cd)
    {
        $db = Query::dbCheckOut();
        $row = $db->getRow(knje360jQuery::getCollegeOrCompanyMst($stat_cd), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        $Msg = "会社";
        if (!isset($row)) {
            $this->setWarning("MSG303", "該当する".$Msg."コードはありません。$stat_cd");
            return false;
        }
        return true;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        $db = Query::dbCheckOut();

        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return;
        }

        knje360jQuery::getDeleteQuery($db, $this);

        $this->setMessage("MSG201");
        $this->cmd = "edit";
        $this->field = array();
        Query::dbCheckIn($db);

        return;
    }

    //入力項目チェック
    public function validateInput($db, $cmd1, $cmd2)
    {
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return false;
        }

        //進路調査
        if ($cmd1 == "shinro") {

            //必須項目
            if ($this->field["ENTRYDATE"] == "") {
                $this->setWarning("MSG304", "　　（登録日）");
                return false;
            }
            if ($this->field["COURSE_KIND"] == "") {
                $this->setWarning("MSG304", "　　（進路種別）");
                return false;
            }
            if ($this->field["QUESTIONNAIRECD"] == "") {
                $this->setWarning("MSG304", "　　（調査名）");
                return false;
            }

            //桁数チェック
            if (strlen($this->field["REMARK"]) > 300) {
                $this->setWarning("MSG915", "(備考は300バイトまでです)");
                return false;
            }

            //登録日の存在チェック
            if ($cmd2 == "update") {
                $cnt = get_count($db->getCol(knje360jQuery::getSubQuery1($this, $this->field["ENTRYDATE"])));
                if (!$cnt) {
                    $this->setWarning("MSG901", "（登録日）");
                    return false;
                }
            }

            //進学
            if ($this->field["COURSE_KIND"] == "1") {
                if ($this->field["SCHOOL_GROUP1"] == "" && $this->field["SCHOOL_CD1"] == "") {
                    $this->setWarning("MSG304", "（第一希望・学校系列）");
                    return false;
                }
            }

            //就職
            if ($this->field["COURSE_KIND"] == "2") {
                if ($this->field["INTRODUCTION_DIV1"] == "") {
                    $this->setWarning("MSG304", "（第一希望・紹介区分）");
                    return false;
                }
            }
        }

        //受験報告（進学）
        if ($cmd1 == "shingaku") {

            //必須項目
            if ($this->field["TOROKU_DATE"] == "") {
                $this->setWarning("MSG304", "　　（登録日）");
                return false;
            }
            if ($this->field["FINSCHOOLCD"] == "") {
                $this->setWarning("MSG304", "　　（学校）");
                return false;
            }

            //桁数チェック
            if (strlen($this->field["EXAMNO"]) > 10) {
                $this->setWarning("MSG915", "(受験番号は10バイトまでです)");
                return false;
            }
            if (strlen($this->field["BUNAME"]) > 120) {
                $this->setWarning("MSG915", "(学科は120バイトまでです)");
                return false;
            }
            if (strlen($this->field["CONTENTEXAM"]) > 120) {
                $this->setWarning("MSG915", "(備考1は120バイトまでです)");
                return false;
            }
            if (strlen($this->field["REASONEXAM"]) > 222) {
                $this->setWarning("MSG915", "(備考2は222バイトまでです)");
                return false;
            }

            //存在チェック
            $sch_cnt = $db->getOne(knje360jQuery::checkFinSchoolData($this->field["FINSCHOOLCD"]));

            if (!$sch_cnt) {
                $this->setWarning("MSG901", "(学校コード)");
                return false;
            }
        }

        //受験報告（就職）
        if ($cmd1 == "syuusyoku") {
            //必須チェック
            if ($this->field["STAT_CD"] == "") {
                $this->setWarning("MSG304", "　　（会社）");
                return false;
            }
            if ($this->field["INTRODUCTION_DIV"] == "") {
                $this->setWarning("MSG304", "　（紹介区分）");
                return false;
            }

            //桁数チェック
            if (strlen($this->field["JOB_THINK"]) > 444) {
                $this->setWarning("MSG915", "(備考は444バイトまでです)");
                return false;
            }
            if (strlen($this->field["REMARK1"]) > 120) {
                $this->setWarning("MSG915", "(備考1は120バイトまでです)");
                return false;
            }
            if (strlen($this->field["REMARK2"]) > 222) {
                $this->setWarning("MSG915", "(備考2は222バイトまでです)");
                return false;
            }
        }

        //進路相談
        if ($cmd1 == "shinroSoudan") {
            if (strlen($this->field["TITLE"]) > 150) {
                $this->setWarning("MSG915", "(相談件名は150バイトまでです)");
                return false;
            }
            if (strlen($this->field["CONTENTS"]) > 900) {
                $this->setWarning("MSG915", "(相談内容は900バイトまでです)");
                return false;
            }
            if ((strlen($this->field["TITLE"]) == 0) && (strlen($this->field["CONTENTS"]) == 0)) {
                $this->setWarning("MSG304", "（相談件名または相談内容）");
                return false;
            }
        }

        return true;
    }

    //更新用のモデルを返します
    public function &getUpdateModel()
    {
        $db = Query::dbCheckOut();

        $cmd = preg_split("/_/", $this->cmd);

        if (!$this->validateInput($db, $cmd[0], $cmd[1])) {
            $this->cmd = $cmd[0]."A";
            return;
        }

        if ($cmd[0] == "shinro") {
            knje360jQuery::getUpdateQuery1($db, $this, $this->field, $cmd[1]);
            $this->entrydate = ($cmd[1] == "insert") ? str_replace("/", "-", $this->field["ENTRYDATE"]) : $this->entrydate;
            $this->seq = ($cmd[1] == "insert") ? $db->getOne(knje360jQuery::getMaxSeq($db, $this, "COURSE_HOPE_DAT")) : $this->seq;
        } elseif ($cmd[0] == "shingaku") {
            knje360jQuery::getUpdateQuery2($db, $this, $this->field, $cmd[1]);
            $this->seq = ($cmd[1] == "insert") ? $db->getOne(knje360jQuery::getMaxSeq2($db)) : $this->seq;
        } elseif ($cmd[0] == "syuusyoku") {
            knje360jQuery::getUpdateQuery3($db, $this, $this->field, $cmd[1]);
            $this->seq = ($cmd[1] == "insert") ? $db->getOne(knje360jQuery::getMaxSeq2($db)) : $this->seq;
        } elseif ($cmd[0] == "shinroSoudan") {
            knje360jQuery::getUpdateQuery4($db, $this, $this->field, $cmd[1]);
            $this->entrydate = ($cmd[1] == "insert") ? str_replace("/", "-", $this->field["ENTRYDATE"]) : $this->entrydate;
            $this->seq = ($cmd[1] == "insert") ? $db->getOne(knje360jQuery::getMaxSeq($db, $this, "COURSE_COUNSELING_DAT")) : $this->seq;
        } elseif ($cmd[0] == "sonota") {
            knje360jQuery::getUpdateQuery5($db, $this, $this->field, $cmd[1]);
            $this->seq = ($cmd[1] == "insert") ? $db->getOne(knje360jQuery::getMaxSeq2($db)) : $this->seq;
        }

        $this->cmd = $cmd[0]."A";
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }

    //一括更新処理（進路調査・進学）
    public function &ReplaceModel1()
    {
        //生徒が選択されてない場合
        if (!strlen($this->replace["selectdata"])) {
            $this->setWarning("MSG304");
            return false;
        }

        //登録日の入力チェック
        if (!$this->replace["field"]["ENTRYDATE"]) {
            $this->setWarning("MSG304", "　　（登録日）");
            return false;
        }
        $this->replace["field"]["ENTRYDATE"] = str_replace("/", "-", $this->replace["field"]["ENTRYDATE"]);

        //調査名の選択チェック
        if (!$this->replace["field"]["QUESTIONNAIRECD"]) {
            $this->setWarning("MSG304", "　　（調査名）");
            return false;
        }

        //学校系列の選択チェック
        if (!$this->replace["field"]["SCHOOL_GROUP1"]) {
            $this->setWarning("MSG304", "（第一希望・学校系列）");
            return false;
        }

        //更新
        $this->rep_update1();
        $this->setMessage("MSG201");

        //添え字を０から順序良くする
        $this->replace["check"] = $this->replace["data_chk"];
        $this->replace["check"][] = $this->replace["check_all"];
        return;
    }

    //進路調査（進学）の一括更新
    public function &rep_update1()
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        $schregno = explode(",", $this->replace["selectdata"]);
        for ($i = 0; $i < get_count($schregno); $i++) {
            $query = knje360jQuery::getRepUpdateQuery1($db, $this, $this->replace["field"], $schregno[$i]);
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //一括更新処理（受験報告・進学）
    public function &ReplaceModel2()
    {
        //生徒が選択されてない場合
        if (!strlen($this->replace["selectdata"])) {
            $this->setWarning("MSG304");
            return false;
        }

        //登録日の入力チェック
        if (!$this->replace["field"]["TOROKU_DATE"]) {
            $this->setWarning("MSG304", "　　（登録日）");
            return false;
        }
        $this->replace["field"]["TOROKU_DATE"] = str_replace("/", "-", $this->replace["field"]["TOROKU_DATE"]);

        //学校コードの入力チェック
        if (!$this->replace["field"]["FINSCHOOLCD"]) {
            $this->setWarning("MSG304", "　　（学校コード）");
            return false;
        }

        if (strlen($this->replace["field"]["BUNAME"]) > 120) {
            $this->setWarning("MSG915", "(学科は120バイトまでです)");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $sch_cnt = $db->getOne(knje360jQuery::checkFinSchoolData($this->replace["field"]["FINSCHOOLCD"]));
        Query::dbCheckIn($db);

        if (!$sch_cnt) {
            $this->setWarning("MSG901", "(学校コード)");
            return false;
        }

        // //最低ひとつでもチェックがついてる場合のみ更新
        // if (!in_array("1", $this->replace["data_chk"])) {
        //     return false;
        // }
        // if (in_array("1", $this->replace["data_chk"])) {
            $this->rep_update2();
        // }
        $this->setMessage("MSG201");
        //添え字を０から順序良くする
        $this->replace["check"] = $this->replace["data_chk"];
        $this->replace["check"][] = $this->replace["check_all"];
        return;
    }

    //受験報告（進学）の一括更新
    public function &rep_update2()
    {
        knje360jQuery::getRepUpdateQuery2($this, $this->replace["field"]);
    }

    public function getPdfModel()
    {
        $path_file = $this->field["SENKOU_NO"].'.pdf';
        $path_file = mb_convert_encoding($path_file, "SJIS-win", "UTF-8");
        $kakuTyousi = explode(".", $this->field["SENKOU_NO"]);
        $filename = $path_file;

        $path_file = DOCUMENTROOT ."/pdf_download/".$path_file;
        /* ファイルの存在確認 */
        if (!file_exists($path_file)) {
            $this->setWarning("MSG303", "指定の求人番号のPDFデータは存在しません。");
            return false;
        }

        /* オープンできるか確認 */
        if (!($fp = fopen($path_file, "r"))) {
            $this->setWarning("MSG203", "指定の求人番号のPDFデータは開くことができません。");
            return false;
        }
        fclose($fp);

        /* ファイルサイズの確認 */
        if (($content_length = filesize($path_file)) == 0) {
            $this->setWarning("MSG203", "指定の求人番号のPDFデータのファイルサイズは 0 byteです。");
            return false;
        }

        ini_set('mbstring.http_output', 'pass');
        /* ダウンロード用のHTTPヘッダ送信 */
        header('Content-Type: application/pdf');
        common::setDownloadHeader($filename);
        header("Content-Length: ".$content_length);

        /* ファイルを読んで出力 */
        if (!readfile($path_file)) {
            $this->setWarning("MSG203", "指定の求人番号のPDFデータは開くことができません。");
            return false;
        }

        return true;
    }
}
