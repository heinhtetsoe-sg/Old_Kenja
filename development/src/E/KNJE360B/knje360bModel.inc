<?php

require_once('for_php7.php');

class knje360bModel extends Model
{
    public $cmd;
    public $schregno = "";
    public $name = "";
    public $grade;
    public $hr_class;
    public $attendno;
    public $field = array();
    public $fstRead = array();
    public $control;
    public $mode;

    public $entrydate;
    public $seq;
    public $checked;
    public $data = array();
    public $replace = array();    //一括更新処理データ
    public $type;

    public function knje360bModel()
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
            unset($this->KeepSort);
            unset($this->sort);
            unset($this->asc_or_desc);
            unset($this->entrydate);
            unset($this->seq);
            unset($this->type);
        }

        $this->field = array("ENTRYDATE"          => VARS::post("ENTRYDATE"),            //登録日
                             "SEQ"                => VARS::post("SEQ"),                  //SEQ
                             "COURSE_KIND"        => VARS::post("COURSE_KIND"),          //進路種別
                             "QUESTIONNAIRECD"    => VARS::post("QUESTIONNAIRECD"),      //調査コード
                             "REMARK"             => VARS::post("REMARK"),               //備考

                             "JOBTYPE_LCD1"       => VARS::post("JOBTYPE_LCD1"),         //職業種別（大分類）
                             "JOBTYPE_MCD1"       => VARS::post("JOBTYPE_MCD1"),         //職業種別（中分類）
                             "JOBTYPE_SCD1"       => VARS::post("JOBTYPE_SCD1"),         //職業種別（小分類）
                             "JOBTYPE_SSCD1"      => VARS::post("JOBTYPE_SSCD1"),        //職業種別（細分類）
                             "WORK_AREA1"         => VARS::post("WORK_AREA1"),           //就業場所
                             "INTRODUCTION_DIV1"  => VARS::post("INTRODUCTION_DIV1"),    //紹介区分
                             "JOBTYPE_LCD2"       => VARS::post("JOBTYPE_LCD2"),         //職業種別（大分類）
                             "JOBTYPE_MCD2"       => VARS::post("JOBTYPE_MCD2"),         //職業種別（中分類）
                             "JOBTYPE_SCD2"       => VARS::post("JOBTYPE_SCD2"),         //職業種別（小分類）
                             "JOBTYPE_SSCD2"      => VARS::post("JOBTYPE_SSCD2"),        //職業種別（細分類）
                             "WORK_AREA2"         => VARS::post("WORK_AREA2"),           //就業場所
                             "INTRODUCTION_DIV2"  => VARS::post("INTRODUCTION_DIV2"),    //紹介区分

                             "TOROKU_DATE"        => VARS::post("TOROKU_DATE"),          //登録日
                             "THINKEXAM"          => VARS::post("THINKEXAM"),            //指導要録に表記する進路先
                             "SEARCH10"           => VARS::post("SEARCH10"),             //駿台甲府学校コード
                             "SCHOOL_CD"          => VARS::request("SCHOOL_CD") ? sprintf("%08d", VARS::request("SCHOOL_CD")) : "",          //学校コード
                             "FACULTYCD"          => VARS::request("FACULTYCD") ? sprintf("%03d", VARS::request("FACULTYCD")) : "",          //学部コード
                             "DEPARTMENTCD"       => VARS::request("DEPARTMENTCD") ? sprintf("%03d", VARS::request("DEPARTMENTCD")) : "",    //学科コード
                             "SCHOOL_CATEGORY_CD" => VARS::request("SCHOOL_CATEGORY_CD"),//東京都学校区分
                             "TOKYO_L_CD"         => VARS::request("TOKYO_L_CD"),        //東京都大分類
                             "TOKYO_M_CD"         => VARS::request("TOKYO_M_CD"),        //東京都中分類
                             "PREF_CD"            => VARS::post("PREF_CD"),              //所在地
                             "ADVERTISE_DIV"      => VARS::request("ADVERTISE_DIV"),     //募集区分
                             "PROGRAM_CD"         => VARS::request("PROGRAM_CD"),        //日程
                             "FORM_CD"            => VARS::request("FORM_CD"),           //方式
                             "L_CD"               => VARS::request("L_CD"),              //大分類
                             "S_CD"               => VARS::request("S_CD"),              //小分類
                             "LIMIT_DATE_WINDOW"  => VARS::request("LIMIT_DATE_WINDOW"), //締切日（窓口）
                             "LIMIT_DATE_MAIL"    => VARS::request("LIMIT_DATE_MAIL"),   //締切日（窓口）
                             "LIMIT_MAIL_DIV"     => VARS::request("LIMIT_MAIL_DIV"),    //郵送区分
                             "STAT_DATE1"         => VARS::request("STAT_DATE1"),        //入試日
                             "STAT_DATE3"         => VARS::request("STAT_DATE3"),        //合格発表日
                             "EXAMNO"             => VARS::post("EXAMNO"),               //受験番号
                             "CONTENTEXAM"        => VARS::post("CONTENTEXAM"),          //備考1
                             "REASONEXAM"         => VARS::post("REASONEXAM"),           //備考2
                             "HOWTOEXAM"          => VARS::post("HOWTOEXAM"),            //受験方式
                             "SHDIV"              => VARS::post("SHDIV"),                //専併区分
                             "DECISION"           => VARS::post("DECISION"),             //受験結果
                             "PLANSTAT"           => VARS::post("PLANSTAT"),             //進路状況
                             "TOKUMEI"            => VARS::post("TOKUMEI"),              //合格短冊匿名希望
                             "ISSUE"              => VARS::post("ISSUE"),                //調査書発行
                             "DESIRED_RANK"       => VARS::request("DESIRED_RANK"),      //志望順位
                             "SELECT_CATEGORY"    => VARS::request("SELECT_CATEGORY"),   //選考分類
                             "SELECT_RESULT"      => VARS::request("SELECT_RESULT"),     //選考結果

                             "SENKOU_NO"          => VARS::post("SENKOU_NO"),            //求人番号
                             "STAT_CD"            => VARS::post("STAT_CD"),              //会社コード
                             "INTRODUCTION_DIV"   => VARS::post("INTRODUCTION_DIV"),     //紹介区分
                             "JOBTYPE_LCD"        => VARS::post("JOBTYPE_LCD"),          //職業別大
                             "JOBTYPE_MCD"        => VARS::post("JOBTYPE_MCD"),          //職業別中
                             "JOBTYPE_SCD"        => VARS::post("JOBTYPE_SCD"),          //職業別小
                             "JOB_THINK"          => VARS::post("JOB_THINK"),            //指導要録に表記する連絡先
                             "REMARK1"            => VARS::post("REMARK1"),              //備考1
                             "REMARK2"            => VARS::post("REMARK2"),              //備考2

                             "REASON1"            => VARS::post("REASON1"),              //志望理由1
                             "REASON2"            => VARS::post("REASON2"),              //志望理由2
                             "CHALLENGE_SEMI1"    => VARS::post("CHALLENGE_SEMI1"),      //セミナー1
                             "CHALLENGE_SEMI2"    => VARS::post("CHALLENGE_SEMI2"),      //セミナー2

                             "SENKOU_KIND_SUB"    => VARS::post("SENKOU_KIND_SUB"),      //その他進路

                             "TITLE"              => VARS::post("TITLE"),                //相談件名
                             "CONTENTS"           => VARS::post("CONTENTS"),             //相談内容

                             "SEARCH_DIV"         => VARS::post("SEARCH_DIV"),           //検索方法

                             "DBNAME"             => VARS::post("DBNAME")                //DB変更に備えて
        );

        if ($this->Properties["Show_Recommendation"] == "1") {
            $arg["Show_Recommendation"] = 1;
            $this->kibouCnt = 8;
        } else {
            $arg["Not_Show_Recommendation"] = 1;
            $this->kibouCnt = 6;
        }
        for ($i = 1; $i <= $this->kibouCnt; $i++) {
            $this->field["SCHOOL_GROUP".$i]     = VARS::post("SCHOOL_GROUP".$i);
            $this->field["FACULTY_GROUP".$i]    = VARS::post("FACULTY_GROUP".$i);
            $this->field["DEPARTMENT_GROUP".$i] = VARS::post("DEPARTMENT_GROUP".$i);
            $this->field["SCHOOL_CD".$i]        = VARS::post("SCHOOL_CD".$i);
            $this->field["FACULTYCD".$i]        = VARS::post("FACULTYCD".$i);
            $this->field["DEPARTMENTCD".$i]     = VARS::post("DEPARTMENTCD".$i);
            $this->field["HOWTOEXAM".$i]        = VARS::post("HOWTOEXAM".$i);
        }

        $this->output = VARS::post("OUTPUT");
        $this->checked = VARS::request("CHECKED");     //チェック
        $this->cmd = VARS::request("cmd");
        $this->clicBtn = VARS::request("clicBtn");

        if (VARS::get("sort")) {
            $this->sort = VARS::get("sort");
        }
        if (VARS::get("SEND_OUTPUT")) {
            $this->output   = VARS::get("SEND_OUTPUT");
        }
        if (VARS::get("SEND_SCHREGNO")) {
            $this->schregno = VARS::get("SEND_SCHREGNO");
        }

        $this->seq_list = VARS::post("SEQ_LIST");

        //一括入力処理（受験報告・進学）
        $this->fields = array();
        if ($this->cmd == "subform6_update") {
            $seq_array = preg_split("/,/", $this->seq_list);

            foreach ($seq_array as $key) {
                $this->fields["TOROKU_DATE"][$key]       = VARS::post("TOROKU_DATE"."-".$key);
                $this->fields["SCHOOL_CD"][$key]         = VARS::post("SCHOOL_CD"."-".$key);
                $this->fields["FACULTYCD"][$key]         = VARS::post("FACULTYCD"."-".$key);
                $this->fields["DEPARTMENTCD"][$key]      = VARS::post("DEPARTMENTCD"."-".$key);
                $this->fields["PREF_CD"][$key]           = VARS::post("PREF_CD"."-".$key);
                $this->fields["HOWTOEXAM"][$key]         = VARS::post("HOWTOEXAM"."-".$key);
                $this->fields["SHDIV"][$key]             = VARS::post("SHDIV"."-".$key);
                $this->fields["DECISION"][$key]          = VARS::post("DECISION"."-".$key);
                $this->fields["TOKUMEI"][$key]           = VARS::post("TOKUMEI"."-".$key);
                $this->fields["ISSUE"][$key]             = VARS::post("ISSUE"."-".$key);
                $this->fields["PLANSTAT"][$key]          = VARS::post("PLANSTAT"."-".$key);
                $this->fields["ADVERTISE_DIV"][$key]     = VARS::post("ADVERTISE_DIV"."-".$key);
                $this->fields["PROGRAM_CD"][$key]        = VARS::post("PROGRAM_CD"."-".$key);
                $this->fields["FORM_CD"][$key]           = VARS::post("FORM_CD"."-".$key);
                $this->fields["L_CD"][$key]              = VARS::post("L_CD"."-".$key);
                $this->fields["S_CD"][$key]              = VARS::post("S_CD"."-".$key);
                $this->fields["LIMIT_DATE_WINDOW"][$key] = VARS::post("LIMIT_DATE_WINDOW"."-".$key);
                $this->fields["LIMIT_DATE_MAIL"][$key]   = VARS::post("LIMIT_DATE_MAIL"."-".$key);
                $this->fields["LIMIT_MAIL_DIV"][$key]    = VARS::post("LIMIT_MAIL_DIV"."-".$key);
                $this->fields["STAT_DATE1"][$key]        = VARS::post("STAT_DATE1"."-".$key);
                $this->fields["STAT_DATE3"][$key]        = VARS::post("STAT_DATE3"."-".$key);
                $this->fields["EXAMNO"][$key]            = VARS::post("EXAMNO"."-".$key);
                $this->fields["CONTENTEXAM"][$key]       = VARS::post("CONTENTEXAM"."-".$key);
                $this->fields["REASONEXAM"][$key]        = VARS::post("REASONEXAM"."-".$key);
            }
        }

        //一括更新処理（進路調査・進学）
        if ($this->cmd == "replace_update1" || $this->cmd == "replace1B") {
            $this->replace["selectdata"] = VARS::post("selectdata");
            $this->replace["ghr"] = VARS::post("GRADE_HR_CLASS");
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

            $this->replace["field"]["ENTRYDATE"]       = VARS::post("ENTRYDATE");
            $this->replace["field"]["COURSE_KIND"]     = VARS::post("COURSE_KIND");
            $this->replace["field"]["QUESTIONNAIRECD"] = VARS::post("QUESTIONNAIRECD");
            $this->replace["field"]["SCHOOL_GROUP1"]   = VARS::post("SCHOOL_GROUP1");

            if (VARS::post("RCHECK0") == "1") {
                $this->replace["field"]["FACULTY_GROUP1"]    = VARS::post("FACULTY_GROUP1");
            }
            if (VARS::post("RCHECK1") == "1") {
                $this->replace["field"]["DEPARTMENT_GROUP1"] = VARS::post("DEPARTMENT_GROUP1");
            }
            if (VARS::post("RCHECK2") == "1") {
                $this->replace["field"]["SCHOOL_CD1"]        = VARS::post("SCHOOL_CD1");
                $this->replace["field"]["FACULTYCD1"]        = VARS::post("FACULTYCD1");
                $this->replace["field"]["DEPARTMENTCD1"]     = VARS::post("DEPARTMENTCD1");
            }
            if (VARS::post("RCHECK3") == "1") {
                $this->replace["field"]["HOWTOEXAM1"]        = VARS::post("HOWTOEXAM1");
            }
            if (VARS::post("RCHECK4") == "1") {
                $this->replace["field"]["SCHOOL_GROUP2"]     = VARS::post("SCHOOL_GROUP2");
            }
            if (VARS::post("RCHECK5") == "1") {
                $this->replace["field"]["FACULTY_GROUP2"]    = VARS::post("FACULTY_GROUP2");
            }
            if (VARS::post("RCHECK6") == "1") {
                $this->replace["field"]["DEPARTMENT_GROUP2"] = VARS::post("DEPARTMENT_GROUP2");
            }
            if (VARS::post("RCHECK7") == "1") {
                $this->replace["field"]["SCHOOL_CD2"]        = VARS::post("SCHOOL_CD2");
                $this->replace["field"]["FACULTYCD2"]        = VARS::post("FACULTYCD2");
                $this->replace["field"]["DEPARTMENTCD2"]     = VARS::post("DEPARTMENTCD2");
            }
            if (VARS::post("RCHECK8") == "1") {
                $this->replace["field"]["HOWTOEXAM2"]        = VARS::post("HOWTOEXAM2");
            }
        }

        //一括更新処理（受験報告・進学）
        if ($this->cmd == "replace_update2" || $this->cmd == "replace2_college" || $this->cmd == "replace2B") {
            $this->replace["selectdata"] = VARS::post("selectdata");
            $this->replace["ghr"] = VARS::post("GRADE_HR_CLASS");
            $this->replace["field"] = array();
            $this->replace["data_chk"][0] = VARS::post("RCHECK0");
            $this->replace["data_chk"][1] = VARS::post("RCHECK1");
            $this->replace["data_chk"][2] = VARS::post("RCHECK2");
            $this->replace["data_chk"][3] = VARS::post("RCHECK3");
            $this->replace["data_chk"][4] = VARS::post("RCHECK4");
            $this->replace["data_chk"][5] = VARS::post("RCHECK5");
            $this->replace["data_chk"][6] = VARS::post("RCHECK6");
            $this->replace["check_all"]   = VARS::post("RCHECK_ALL");

            $this->replace["field"]["SCHOOL_CD"]    = VARS::request("SCHOOL_CD")    ? sprintf("%08d", VARS::request("SCHOOL_CD"))    : "";
            $this->replace["field"]["FACULTYCD"]    = VARS::request("FACULTYCD")    ? sprintf("%03d", VARS::request("FACULTYCD"))    : "";
            $this->replace["field"]["DEPARTMENTCD"] = VARS::request("DEPARTMENTCD") ? sprintf("%03d", VARS::request("DEPARTMENTCD")) : "";
            $this->replace["field"]["TOROKU_DATE"]  = VARS::post("TOROKU_DATE");

            if (VARS::post("RCHECK0") == "1") {
                $this->replace["field"]["PREF_CD"]    = VARS::post("PREF_CD");
            }
            if (VARS::post("RCHECK1") == "1") {
                $this->replace["field"]["HOWTOEXAM"]  = VARS::post("HOWTOEXAM");
            }
            if (VARS::post("RCHECK1") == "1") {
                $this->replace["field"]["SHDIV"]      = VARS::post("SHDIV");
            }
            if (VARS::post("RCHECK2") == "1") {
                $this->replace["field"]["DECISION"]   = VARS::post("DECISION");
            }
            if (VARS::post("RCHECK3") == "1") {
                $this->replace["field"]["PLANSTAT"]   = VARS::post("PLANSTAT");
            }
            if (VARS::post("RCHECK4") == "1") {
                $this->replace["field"]["STAT_DATE1"] = VARS::post("STAT_DATE1");
            }
            if (VARS::post("RCHECK5") == "1") {
                $this->replace["field"]["STAT_DATE3"] = VARS::post("STAT_DATE3");
            }
            if (VARS::post("RCHECK6") == "1") {
                $this->replace["field"]["TOKUMEI"]    = VARS::post("TOKUMEI");
            }
            if (VARS::post("RCHECK7") == "1") {
                $this->replace["field"]["ISSUE"]      = VARS::post("ISSUE");
            }

            //東京都集計用
            if ($this->Properties["useTokyotoShinroTyousasyo"] == "1") {
                $this->replace["field"]["SCHOOL_CATEGORY_CD"] = VARS::request("SCHOOL_CATEGORY_CD");
                $this->replace["field"]["TOKYO_L_CD"]         = VARS::request("TOKYO_L_CD");
                $this->replace["field"]["TOKYO_M_CD"]         = VARS::request("TOKYO_M_CD");
            }
        }

        //学校選択（受験報告・進学・一括入力）
        if ($this->cmd == "replace6") {
            unset($this->replace);
        }
        if ($this->cmd == "replace6_search" || $this->cmd == "replace_update6") {
            if ($this->cmd == "replace6_search") {
                unset($this->replace["check_all"]);
                unset($this->replace["data_chk"]);
            } else {
                $this->replace["check_all"] = VARS::post("CHECKALL");
                $this->replace["data_chk"]  = VARS::request("CHECKED");
            }

            $this->replace["field"] = array();
            $this->replace["field"]["SEARCH_TXT"]  = VARS::post("SEARCH_TXT");
            $this->replace["field"]["SEARCH_NO"]   = VARS::post("SEARCH_NO");
            $this->replace["field"]["TOROKU_DATE"] = VARS::post("TOROKU_DATE");

            //学校コードを半角スペースで分割
            $this->search_no = array();
            $this->search_no = explode(' ', $this->replace["field"]["SEARCH_NO"]);
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

        //GRADE_CD取得
        $db = Query::dbCheckOut();
        $this->gradeCd = $db->getOne(knje360bQuery::getGradeCd($this->grade));
        Query::dbCheckIn($db);

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

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if ($model->Properties["useAutoSetCollegeNameToThinkExam"] == "1") {
            $this->fstRead = array(
                                 "SCHOOL_CD"   => VARS::post("HID_FSTREAD_SCHOOL_CD"),  //初回読み込み時の学校コード
                                 "SCHOOL_NAME" => VARS::post("HID_FSTREAD_SCHOOL_NAME"),//初回読み込み時の学校名
                                 "PLANSTAT"    => VARS::post("HID_FSTREAD_PLANSTAT")    //初回読み込み時の進路状況
            );
        }

        //生徒項目名切替処理
        $schName = "";
        $db = Query::dbCheckOut();
        //テーブルの有無チェック
        $query = knje360bQuery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && ($this->schregno || ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""))) {
            //生徒項目名取得
            $schName = $db->getOne(knje360bQuery::getSchName($this));
        }
        $this->sch_label = (strlen($schName) > 0) ? $schName : '生徒';
        Query::dbCheckIn($db);
    }

    // 学校・会社マスタ検索チェック
    public function chkCollegeOrCompanyMst($stat_cd)
    {
        $db = Query::dbCheckOut();
        $row = $db->getRow(knje360bQuery::getCollegeOrCompanyMst($stat_cd), DB_FETCHMODE_ASSOC);
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

        knje360bQuery::getDeleteQuery($db, $this);

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
        if ($cmd1 == "subform1") {
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
                $cnt = get_count($db->getCol(knje360bQuery::getSubQuery1($this, $this->field["ENTRYDATE"])));
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
        if ($cmd1 == "subform2") {
            //必須項目
            if ($this->field["TOROKU_DATE"] == "") {
                $this->setWarning("MSG304", "　　（登録日）");
                return false;
            }
            if ($this->field["SCHOOL_CD"] == "") {
                $this->setWarning("MSG304", "　　（学校）");
                return false;
            }
            //E002,NAMESPARE1に1の項目を選択した時
            $chkSpare1 = $db->getOne(knje360bQuery::getNameMstNamecd2("E002", $this->field["HOWTOEXAM"]));
            if ($chkSpare1 == "1" && $this->field["SHDIV"] == "") {
                $this->setWarning("MSG304", "　　（専併区分）");
                return false;
            }

            //桁数チェック
            if (strlen($this->field["EXAMNO"]) > 10) {
                $this->setWarning("MSG915", "(受験番号は10バイトまでです)");
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
            $sch_cnt = $db->getOne(knje360bQuery::checkCollegeData($this->field["SCHOOL_CD"], "", "", "", ""));
            $fac_cnt = $db->getOne(knje360bQuery::checkCollegeData($this->field["SCHOOL_CD"], $this->field["FACULTYCD"], "", "1", ""));
            $dep_cnt = $db->getOne(knje360bQuery::checkCollegeData($this->field["SCHOOL_CD"], $this->field["FACULTYCD"], $this->field["DEPARTMENTCD"], "1", "1"));

            if (!$sch_cnt) {
                $this->setWarning("MSG901", "(学校コード)");
                return false;
            }
            if (strlen($this->field["FACULTYCD"]) && !$fac_cnt) {
                $this->setWarning("MSG901", "(学部コード)");
                return false;
            }
            if (strlen($this->field["DEPARTMENTCD"]) && !$dep_cnt) {
                $this->setWarning("MSG901", "(学科コード)");
                return false;
            }
        }

        //受験報告（就職）
        if ($cmd1 == "subform3") {
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
        if ($cmd1 == "subform4") {
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

        //受験報告（進学・一括入力）
        if ($cmd1 == "subform6") {
            $errFlg = false;
            $errTmp = array();
            $seq_array = preg_split("/,/", $this->seq_list);
            $errMsg = array("1" => "登録日",
                            "2" => "受験番号",
                            "3" => "備考1",
                            "4" => "備考2",
                            "5" => "専併区分");

            foreach ($seq_array as $seq) {
                //必須項目
                if ($this->fields["TOROKU_DATE"][$seq] == "") {
                    $errFlg = true;
                    $errTmp[] = 1;
                }

                //E002,NAMESPARE1に1の項目を選択した時
                $chkSpare1 = $db->getOne(knje360bQuery::getNameMstNamecd2("E002", $this->fields["HOWTOEXAM"][$seq]));
                if ($chkSpare1 == "1" && $this->fields["SHDIV"][$seq] == "") {
                    $errFlg = true;
                    $errTmp[] = 5;
                }

                //桁数チェック
                if (strlen($this->fields["EXAMNO"][$seq]) > 10) {
                    $errFlg = true;
                    $errTmp[] = 2;
                }
                if (strlen($this->fields["CONTENTEXAM"][$seq]) > 120) {
                    $errFlg = true;
                    $errTmp[] = 3;
                }
                if (strlen($this->fields["REASONEXAM"][$seq]) > 222) {
                    $errFlg = true;
                    $errTmp[] = 4;
                }
            }

            if ($errFlg == true) {
                $err = array_unique($errTmp);
                sort($err);

                $errShow = "";
                foreach ($err as $key) {
                    $errShow .= ($errShow) ? ",".$errMsg[$key] : $errMsg[$key];
                }

                $this->setWarning("MSG901", "（".$errShow."）");
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

        if ($cmd[0] == "subform1") {
            knje360bQuery::getUpdateQuery1($db, $this, $this->field, $cmd[1]);
            $this->entrydate = ($cmd[1] == "insert") ? str_replace("/", "-", $this->field["ENTRYDATE"]) : $this->entrydate;
            $this->seq = ($cmd[1] == "insert") ? $db->getOne(knje360bQuery::getMaxSeq($db, $this, "COURSE_HOPE_SS_DAT")) : $this->seq;
        } elseif ($cmd[0] == "subform2") {
            knje360bQuery::getUpdateQuery2($db, $this, $this->field, $cmd[1]);
            $this->seq = ($cmd[1] == "insert") ? $db->getOne(knje360bQuery::getMaxSeq2($db)) : $this->seq;
        } elseif ($cmd[0] == "subform3") {
            knje360bQuery::getUpdateQuery3($db, $this, $this->field, $cmd[1]);
            $this->seq = ($cmd[1] == "insert") ? $db->getOne(knje360bQuery::getMaxSeq2($db)) : $this->seq;
        } elseif ($cmd[0] == "subform4") {
            knje360bQuery::getUpdateQuery4($db, $this, $this->field, $cmd[1]);
            $this->entrydate = ($cmd[1] == "insert") ? str_replace("/", "-", $this->field["ENTRYDATE"]) : $this->entrydate;
            $this->seq = ($cmd[1] == "insert") ? $db->getOne(knje360bQuery::getMaxSeq($db, $this, "COURSE_COUNSELING_DAT")) : $this->seq;
        } elseif ($cmd[0] == "subform5") {
            knje360bQuery::getUpdateQuery5($db, $this, $this->field, $cmd[1]);
            $this->seq = ($cmd[1] == "insert") ? $db->getOne(knje360bQuery::getMaxSeq2($db)) : $this->seq;
        } elseif ($cmd[0] == "subform6") {
            knje360bQuery::getUpdateQuery6($db, $this, $this->fields);
        }

        if (in_array($cmd[0], array("subform2", "subform3")) && $cmd[1] == "insert") {
            $this->cmd = $cmd[0]."B";
            $this->setMessage("MSG201", "画面をクリアしました。");
        } else {
            $this->cmd = $cmd[0]."A";
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }

    //一括更新処理（進路調査・進学）
    public function &replaceModel1()
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
        $this->repUpdate1();
        $this->setMessage("MSG201");

        //添え字を０から順序良くする
        $this->replace["check"]   = $this->replace["data_chk"];
        $this->replace["check"][] = $this->replace["check_all"];
        return;
    }

    //進路調査（進学）の一括更新
    public function &repUpdate1()
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        $sch_array = explode(",", $this->replace["selectdata"]);
        for ($i = 0; $i < get_count($sch_array); $i++) {
            list($gha, $schregno) = preg_split("/_/", $sch_array[$i]);
            $query = knje360bQuery::getRepUpdateQuery1($db, $this, $this->replace["field"], $schregno);
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //一括更新処理（受験報告・進学）
    public function &replaceModel2()
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

        //E002,NAMESPARE1に1の項目を選択した時
        $db = Query::dbCheckOut();
        $chkSpare1 = $db->getOne(knje360bQuery::getNameMstNamecd2("E002", $this->replace["field"]["HOWTOEXAM"]));
        Query::dbCheckIn($db);
        if ($chkSpare1 == "1" && $this->replace["field"]["SHDIV"] == "") {
            $this->setWarning("MSG304", "　　（専併区分）");
            return false;
        }

        //学校コードの入力チェック
        if (!$this->replace["field"]["SCHOOL_CD"]) {
            $this->setWarning("MSG304", "　　（学校コード）");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $sch_cnt = $db->getOne(knje360bQuery::checkCollegeData($this->replace["field"]["SCHOOL_CD"], "", "", "", ""));
        $fac_cnt = $db->getOne(knje360bQuery::checkCollegeData($this->replace["field"]["SCHOOL_CD"], $this->replace["field"]["FACULTYCD"], "", "1", ""));
        $dep_cnt = $db->getOne(knje360bQuery::checkCollegeData($this->replace["field"]["SCHOOL_CD"], $this->replace["field"]["FACULTYCD"], $this->replace["field"]["DEPARTMENTCD"], "1", "1"));
        Query::dbCheckIn($db);

        if (!$sch_cnt) {
            $this->setWarning("MSG901", "(学校コード)");
            return false;
        }
        if (strlen($this->replace["field"]["FACULTYCD"]) && !$fac_cnt) {
            $this->setWarning("MSG901", "(学部コード)");
            return false;
        }
        if (strlen($this->replace["field"]["DEPARTMENTCD"]) && !$dep_cnt) {
            $this->setWarning("MSG901", "(学科コード)");
            return false;
        }

        //最低ひとつでもチェックがついてる場合のみ更新
        if (!in_array("1", $this->replace["data_chk"])) {
            return false;
        }
        if (in_array("1", $this->replace["data_chk"])) {
            $this->repUpdate2();
        }
        $this->setMessage("MSG201");
        return;
    }

    //受験報告（進学）の一括更新
    public function &repUpdate2()
    {
        knje360bQuery::getRepUpdateQuery2($this, $this->replace["field"]);
    }

    //学校選択処理（受験報告・進学・一括入力）
    public function &replaceModel6()
    {
        //登録日の入力チェック
        if (!$this->replace["field"]["TOROKU_DATE"]) {
            $this->setWarning("MSG304", "　　（登録日）");
            return false;
        }

        //選択済み、選択中の件数取得
        $db           = Query::dbCheckOut();
        $selected_cnt = get_count($db->getCol(knje360bQuery::getSelectedSchool($this)));
        Query::dbCheckIn($db);
        $data_cnt     = get_count($this->replace["data_chk"]);
        $total_cnt    = (int)$selected_cnt + $data_cnt;

        if ($total_cnt > 50) {
            $this->setWarning("MSG915", "選択中：{$data_cnt}件　選択済み：{$selected_cnt}件\\n（合計50件まで選択可）");
            return false;
        //最低ひとつでもチェックがついてる場合のみ更新
        } elseif ($data_cnt > 0) {
            $this->repUpdate6();
        } else {
            $this->setWarning("MSG304");
            return false;
        }
        $this->setMessage("MSG201");

        return;
    }

    //進路先選択（更新）
    public function &repUpdate6()
    {
        $db = Query::dbCheckOut();
        knje360bQuery::getRepUpdateQuery6($db, $this);
        Query::dbCheckIn($db);
        return;
    }

    public function getPdfModel()
    {
        $path_file  = $this->field["SENKOU_NO"].'.pdf';
        $path_file  = mb_convert_encoding($path_file, "SJIS-win", "UTF-8");
        $kakuTyousi = explode(".", $this->field["SENKOU_NO"]);
        $filename   = $path_file;

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
