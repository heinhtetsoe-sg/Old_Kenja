<?php

require_once('for_php7.php');

class knja110aModel extends Model
{
    public $field      = array();
    public $cmd;
    public $GradeClass = "";
    public $schoolKind = "";
    public $schregno;
    public $name;
    public $control_data = array();    //コントロールマスタデータを保持
    public $replace_data = array();    //置換で選択された生徒一覧
    public $coursemajorcd = "";        //課程学科コード(置換処理に仕様)
    public $chargeclass_flg = 1;       //ユーザに担当クラスがあるかどうか
    public $hist_issuedate;
    public $histField = array();
    public $grdEntHistField = array();

    public $ghr_cd = "";
    public $fi_grade_hr_class = "";

    public $getPrgId;      //コールされたか
    public $sendAuth;      //親画面の権限
    public $auth;          //権限
    public $nameSLen;      //氏名の入力文字数

    public function knja110aModel()
    {
    }

    public function init()
    {
        common::GetControlMaster_Fnc($this->control_data);
        if (VARS::get("PROGRAMID")!="") {
            $this->schregno = "";
            unset($this->GradeClass);
            unset($this->hist_issuedate);
            unset($this->ghr_cd);
            unset($this->fi_grade_hr_class);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->finschoolcdKeta = ($this->Properties["useFinschoolcdFieldSize"] == '12' ? 12 : 7);

        //生徒情報
        $this->field = array("SCHREGNO"                     =>  VARS::post("SCHREGNO"),        //学籍番号
                             "VISITOR"                      =>  VARS::post("VISITOR"),         //訪問生
                             "INOUTCD"                      =>  VARS::post("INOUTCD"),         //内外区分コード
                             "NAME"                         =>  VARS::post("NAME"),            //氏名
                             "GRCL"                         =>  VARS::post("GRADE_CLASS"),     //学年
                             "ANNUAL"                       =>  VARS::post("ANNUAL"),          //年次
                             "NAME_SHOW"                    =>  VARS::post("NAME_SHOW"),       //氏名表示用
                             "NAME_KANA"                    =>  VARS::post("NAME_KANA"),       //氏名かな
                             "NAME_ENG"                     =>  VARS::post("NAME_ENG"),        //英字氏名
                             "REAL_NAME"                    =>  VARS::post("REAL_NAME"),       //本籍氏名
                             "REAL_NAME_KANA"               =>  VARS::post("REAL_NAME_KANA"),  //本籍氏名かな
                             "BIRTHDAY"                     =>  VARS::post("BIRTHDAY"),        //誕生日
                             "SEX"                          =>  VARS::post("SEX"),             //性別
                             "BLOODTYPE"                    =>  VARS::post("BLOODTYPE"),       //血液型(型)
                             "BLOOD_RH"                     =>  VARS::post("BLOOD_RH"),        //血液型(RH型)
                             "HANDICAP"                     =>  VARS::post("HANDICAP"),        //その他
                             "NATIONALITY"                  =>  VARS::post("NATIONALITY"),     //国籍
                             "ORIGIN"                       =>  VARS::post("ORIGIN"),          //出身
                             "FINSCHOOLCD"                  =>  VARS::post("FINSCHOOLCD"),     //出身中学校コード
                             "FINISH_DATE"                  =>  VARS::post("FINISH_DATE"),     //卒業年月日
                             "PRISCHOOLCD"                  =>  VARS::post("PRISCHOOLCD"),     //塾コード
                             "PRISCHOOL_CLASS_CD"           =>  VARS::post("PRISCHOOL_CLASS_CD"),//教室（塾）コード
                             "ENT_DATE"                     =>  VARS::post("ENT_DATE"),        //入学日付
                             "CURRICULUM_YEAR"              =>  VARS::post("CURRICULUM_YEAR"), //課程入学年度
                             "ENT_DIV"                      =>  VARS::post("ENT_DIV"),         //入学区分
                             "ENT_REASON"                   =>  VARS::post("ENT_REASON"),      //事由
                             "ENT_SCHOOL"                   =>  VARS::post("ENT_SCHOOL"),      //学校
                             "ENT_ADDR"                     =>  VARS::post("ENT_ADDR"),        //住所1
                             "ENT_ADDR2"                    =>  VARS::post("ENT_ADDR2"),       //住所2
                             "GRD_DATE"                     =>  VARS::post("GRD_DATE"),        //卒業日付
                             "GRD_DIV"                      =>  VARS::post("GRD_DIV"),         //卒業区分
                             "GRD_REASON"                   =>  VARS::post("GRD_REASON"),      //事由
                             "GRD_SCHOOL"                   =>  VARS::post("GRD_SCHOOL"),      //学校
                             "GRD_ADDR"                     =>  VARS::post("GRD_ADDR"),        //住所1
                             "GRD_ADDR2"                    =>  VARS::post("GRD_ADDR2"),       //住所2
                             "REMARK1"                      =>  VARS::post("REMARK1") ,        //備考1
                             "REMARK2"                      =>  VARS::post("REMARK2") ,        //備考2
                             "REMARK3"                      =>  VARS::post("REMARK3") ,        //備考3
                             "TENGAKU_SAKI_ZENJITU"         =>  VARS::post("TENGAKU_SAKI_ZENJITU") ,        //備考3
                             "TENGAKU_SAKI_GRADE"           =>  VARS::post("TENGAKU_SAKI_GRADE") ,          //DETAL SEQ='001' BASE_REMARK2
                             "NYUGAKUMAE_SYUSSIN_JOUHOU"    =>  VARS::post("NYUGAKUMAE_SYUSSIN_JOUHOU") ,        //備考3
                             "EXAMNO"                       =>  VARS::post("EXAMNO") ,         //受験番号
                             "UPDATED1"                     =>  VARS::post("UPDATED1"),        //更新日付
                             "ATTENDNO"                     =>  VARS::post("ATTENDNO"),        //出席番号
                             "COURSEMAJORCD"                =>  VARS::post("COURSEMAJORCD"),   //課程学科
                             "COURSECODE"                   =>  VARS::post("COURSECODE"),      //コースコード
                             "UPDATED2"                     =>  VARS::post("UPDATED2"),        //更新日付

                             "TESTDIV"                      =>  VARS::post("TESTDIV"),         //入試区分1(茗渓のみ利用)
                             "EXAM_TYPE"                    =>  VARS::post("EXAM_TYPE"),       //入試区分2(茗渓のみ利用)

                             "GHR_CD"                       =>  VARS::post("GHR_CD"),          //複式年組
                             "GHR_ATTENDNO"                 =>  VARS::post("GHR_ATTENDNO"),    //複式出席番号

                             "FI_GRADE_HR_CLASS"            =>  VARS::post("FI_GRADE_HR_CLASS"), //FI複式年組
                             "FI_ATTENDNO"                  =>  VARS::post("FI_ATTENDNO"),       //FI複式出席番号

                             "E_APPDATE"                    =>  VARS::post("E_APPDATE")        //変更開始日付
                        );

        //履歴フラグ
        $this->setFlg = array("GRADE_FLG"                       =>  VARS::post("GRADE_FLG"),
                              "HR_CLASS_FLG"                    =>  VARS::post("HR_CLASS_FLG"),
                              "ATTENDNO_FLG"                    =>  VARS::post("ATTENDNO_FLG"),
                              "ANNUAL_FLG"                      =>  VARS::post("ANNUAL_FLG"),
                              "COURSECD_FLG"                    =>  VARS::post("COURSECD_FLG"),
                              "MAJORCD_FLG"                     =>  VARS::post("MAJORCD_FLG"),
                              "COURSECODE_FLG"                  =>  VARS::post("COURSECODE_FLG"),
                              "NAME_FLG"                        =>  VARS::post("NAME_FLG"),
                              "NAME_SHOW_FLG"                   =>  VARS::post("NAME_SHOW_FLG"),
                              "NAME_KANA_FLG"                   =>  VARS::post("NAME_KANA_FLG"),
                              "NAME_ENG_FLG"                    =>  VARS::post("NAME_ENG_FLG"),
                              "REAL_NAME_FLG"                   =>  VARS::post("REAL_NAME_FLG"),
                              "REAL_NAME_KANA_FLG"              =>  VARS::post("REAL_NAME_KANA_FLG"),
                              "HANDICAP_FLG"                    =>  VARS::post("HANDICAP_FLG"),
                              "NATIONALITY2_FLG"                =>  VARS::post("NATIONALITY2_FLG"),
                              "NATIONALITY_NAME_FLG"            =>  VARS::post("NATIONALITY_NAME_FLG"),
                              "NATIONALITY_NAME_KANA_FLG"       =>  VARS::post("NATIONALITY_NAME_KANA_FLG"),
                              "NATIONALITY_NAME_ENG_FLG"        =>  VARS::post("NATIONALITY_NAME_ENG_FLG"),
                              "NATIONALITY_REAL_NAME_FLG"       =>  VARS::post("NATIONALITY_REAL_NAME_FLG"),
                              "NATIONALITY_REAL_NAME_KANA_FLG"  =>  VARS::post("NATIONALITY_REAL_NAME_KANA_FLG"),
                        );

        //履歴日付
        if (VARS::get("HIST_ISSUEDATE")) {
            $this->hist_issuedate = VARS::get("HIST_ISSUEDATE");
        }

        //履歴修正
        if (VARS::request("ISSUEDATE") || $this->cmd == "histAdd" || $this->cmd == "histUpd" || $this->cmd == "histDel" || $this->cmd == "changeCmb") {
            $setGradeHrAtteFlg = VARS::request("GRADE_HR_ATTE_FLG");
            $setGradeHr = explode(",", VARS::request("GRADE_CLASS"));
            $setCourseMajor = explode(",", VARS::request("COURSEMAJORCD"));
            $this->histField = array("ISSUEDATE"                        =>  VARS::request("ISSUEDATE"),
                                     "EXPIREDATE"                       =>  VARS::request("EXPIREDATE"),
                                     "YEAR"                             =>  VARS::request("YEAR"),
                                     "SEMESTER"                         =>  VARS::request("SEMESTER"),
                                     "GRADE"                            =>  $setGradeHr[0],
                                     "HR_CLASS"                         =>  $setGradeHr[1],
                                     "GRADE_CLASS"                      =>  VARS::request("GRADE_CLASS"),
                                     "ATTENDNO"                         =>  VARS::request("ATTENDNO"),
                                     "ANNUAL"                           =>  VARS::request("ANNUAL"),
                                     "COURSECD"                         =>  $setCourseMajor[0],
                                     "MAJORCD"                          =>  $setCourseMajor[1],
                                     "COURSEMAJORCD"                    =>  VARS::request("COURSEMAJORCD"),
                                     "COURSECODE"                       =>  VARS::request("COURSECODE"),
                                     "SCHREGNO"                         =>  VARS::request("SCHREGNO"),
                                     "NAME"                             =>  VARS::request("NAME"),
                                     "NAME_SHOW"                        =>  VARS::request("NAME_SHOW"),
                                     "NAME_KANA"                        =>  VARS::request("NAME_KANA"),
                                     "NAME_ENG"                         =>  VARS::request("NAME_ENG"),
                                     "REAL_NAME"                        =>  VARS::request("REAL_NAME"),
                                     "REAL_NAME_KANA"                   =>  VARS::request("REAL_NAME_KANA"),
                                     "HANDICAP"                         =>  VARS::request("HANDICAP"),
                                     "NATIONALITY2"                     =>  VARS::request("NATIONALITY2"),
                                     "NATIONALITY_NAME"                 =>  VARS::request("NATIONALITY_NAME"),
                                     "NATIONALITY_NAME_KANA"            =>  VARS::request("NATIONALITY_NAME_KANA"),
                                     "NATIONALITY_NAME_ENG"             =>  VARS::request("NATIONALITY_NAME_ENG"),
                                     "NATIONALITY_REAL_NAME"            =>  VARS::request("NATIONALITY_REAL_NAME"),
                                     "NATIONALITY_REAL_NAME_KANA"       =>  VARS::request("NATIONALITY_REAL_NAME_KANA"),
                                     "GRADE_FLG"                        =>  $setGradeHrAtteFlg,
                                     "HR_CLASS_FLG"                     =>  $setGradeHrAtteFlg,
                                     "ATTENDNO_FLG"                     =>  $setGradeHrAtteFlg,
                                     "ANNUAL_FLG"                       =>  VARS::request("ANNUAL_FLG"),
                                     "COURSECD_FLG"                     =>  VARS::request("COURSECD_FLG"),
                                     "MAJORCD_FLG"                      =>  VARS::request("MAJORCD_FLG"),
                                     "COURSECODE_FLG"                   =>  VARS::request("COURSECODE_FLG"),
                                     "NAME_FLG"                         =>  VARS::request("NAME_FLG"),
                                     "NAME_SHOW_FLG"                    =>  VARS::request("NAME_SHOW_FLG"),
                                     "NAME_KANA_FLG"                    =>  VARS::request("NAME_KANA_FLG"),
                                     "NAME_ENG_FLG"                     =>  VARS::request("NAME_ENG_FLG"),
                                     "REAL_NAME_FLG"                    =>  VARS::request("REAL_NAME_FLG"),
                                     "REAL_NAME_KANA_FLG"               =>  VARS::request("REAL_NAME_KANA_FLG"),
                                     "HANDICAP_FLG"                     =>  VARS::request("HANDICAP_FLG"),
                                     "NATIONALITY2_FLG"                 =>  VARS::request("NATIONALITY2_FLG"),
                                     "NATIONALITY_NAME_FLG"             =>  VARS::request("NATIONALITY_NAME_FLG"),
                                     "NATIONALITY_NAME_KANA_FLG"        =>  VARS::request("NATIONALITY_NAME_KANA_FLG"),
                                     "NATIONALITY_NAME_ENG_FLG"         =>  VARS::request("NATIONALITY_NAME_ENG_FLG"),
                                     "NATIONALITY_REAL_NAME_FLG"        =>  VARS::request("NATIONALITY_REAL_NAME_FLG"),
                                     "NATIONALITY_REAL_NAME_KANA_FLG"   =>  VARS::request("NATIONALITY_REAL_NAME_KANA_FLG")
                            );
        } else {
            unset($this->histField);
        }

        //入学卒業履歴修正
        if (VARS::request("SCHOOL_KIND") || $this->cmd == "entGrdHistAdd" || $this->cmd == "entGrdHistUpd" || $this->cmd == "entGrdHistDel" || $this->cmd == "changeEntGrdCmb") {
            $this->grdEntHistField = array("SCHREGNO"                   =>  VARS::request("SCHREGNO"),
                                           "SCHOOL_KIND"                =>  VARS::request("SCHOOL_KIND"),
                                           "FINSCHOOLCD"                =>  VARS::request("FINSCHOOLCD"),
                                           "FINISH_DATE"                =>  VARS::request("FINISH_DATE"),
                                           "ENT_DATE"                   =>  VARS::request("ENT_DATE"),
                                           "CURRICULUM_YEAR"            =>  VARS::request("CURRICULUM_YEAR"),
                                           "ENT_DIV"                    =>  VARS::request("ENT_DIV"),
                                           "ENT_REASON"                 =>  VARS::request("ENT_REASON"),
                                           "ENT_SCHOOL"                 =>  VARS::request("ENT_SCHOOL"),
                                           "ENT_ADDR"                   =>  VARS::request("ENT_ADDR"),
                                           "ENT_ADDR2"                  =>  VARS::request("ENT_ADDR2"),
                                           "GRD_DATE"                   =>  VARS::request("GRD_DATE"),
                                           "GRD_DIV"                    =>  VARS::request("GRD_DIV"),
                                           "GRD_REASON"                 =>  VARS::request("GRD_REASON"),
                                           "GRD_SCHOOL"                 =>  VARS::request("GRD_SCHOOL"),
                                           "GRD_ADDR"                   =>  VARS::request("GRD_ADDR"),
                                           "GRD_ADDR2"                  =>  VARS::request("GRD_ADDR2"),
                                           "GRD_TERM"                   =>  VARS::request("GRD_TERM"),
                                           "TENGAKU_SAKI_ZENJITU"       =>  VARS::request("TENGAKU_SAKI_ZENJITU"),
                                           "TENGAKU_SAKI_GRADE"         =>  VARS::request("TENGAKU_SAKI_GRADE") ,
                                           "NYUGAKUMAE_SYUSSIN_JOUHOU"  =>  VARS::request("NYUGAKUMAE_SYUSSIN_JOUHOU")
                                          );
        } else {
            unset($this->grdEntHistField);
        }

        if ($this->cmd != 'update') {
            $this->subFrm = $_GET;
        }

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
            $this->name = VARS::get("NAME");
            $this->GradeClass = VARS::get("GRADE").",".VARS::get("HR_CLASS");
            $this->ghr_cd = VARS::get("GHR_CD");
            $this->fi_grade_hr_class = VARS::get("FI_GRADE_HR_CLASS");
        }
        if (VARS::get("SCHREGNO_FLG") != "") {
            $this->schregno_flg = VARS::get("SCHREGNO_FLG");
        }

        if ($this->schregno) {
            $studentInfo = knja110aQuery::getStudentData($this->schregno, $this);
            //DB接続
            $db = Query::dbCheckOut();
            if ($this->cmd != "subUpdate" && $this->cmd != "update" && $studentInfo["GRCL"]) {
                $this->field["GRCL"] = $studentInfo["GRCL"];
            }
            $this->schoolKind = $db->getOne(knja110aQuery::getSchoolKind($this));
            //DB切断
            Query::dbCheckIn($db);
        } else {
            //DB接続
            $db = Query::dbCheckOut();
            $this->schoolKind = $db->getOne(knja110aQuery::getSchoolKind($this));

            //DB切断
            Query::dbCheckIn($db);
        }
        //置換
        if ($this->cmd == "replace_update" || $this->cmd == "subReplace_update") {
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["base_field"] = $this->replace_data["regd_field"] = $this->replace_data["entGrd_field"] = array();
            //チェックされた項目をテーブル別に分ける
            $this->replace_data["check"] = array(VARS::post("RCHECK0"),
                                                 VARS::post("RCHECK1"),
                                                 VARS::post("RCHECK2"),
                                                 VARS::post("RCHECK3"),
                                                 VARS::post("RCHECK4"),
                                                 VARS::post("RCHECK5"),
                                                 VARS::post("RCHECK6"),
                                                 VARS::post("RCHECK7"),
                                                 VARS::post("RCHECK8"),
                                                 VARS::post("RCHECK9"),
                                                 VARS::post("RCHECK10"),
                                                 VARS::post("RCHECK11"),
                                                 VARS::post("RCHECK12"),
                                                 VARS::post("RCHECK13"),
                                                 VARS::post("RCHECK14"),
                                                 VARS::post("RCHECK15"),
                                                 VARS::post("RCHECK16"),
                                                 VARS::post("RCHECK17"),
                                                 VARS::post("RCHECK18"));
            $this->replace_data["regd_check"] = array(VARS::post("RCHECK0"),VARS::post("RCHECK1"));
            $this->replace_data["base_check"] = array(VARS::post("RCHECK2"),
                                                      VARS::post("RCHECK3"),
                                                      VARS::post("RCHECK4"),
                                                      VARS::post("RCHECK5"),
                                                      VARS::post("RCHECK6"),
                                                      VARS::post("RCHECK7"),
                                                      VARS::post("RCHECK8"),
                                                      VARS::post("RCHECK9"),
                                                      VARS::post("RCHECK10"),
                                                      VARS::post("RCHECK11"),
                                                      VARS::post("RCHECK12"),
                                                      VARS::post("RCHECK13"),
                                                      VARS::post("RCHECK14"),
                                                      VARS::post("RCHECK15"),
                                                      VARS::post("RCHECK16"),
                                                      VARS::post("RCHECK17"),
                                                      VARS::post("RCHECK18"));
            $this->replace_data["check_all"] = VARS::post("RCHECK19");
            $arr = explode(",", VARS::post("COURSEMAJORCD"));
            if (VARS::post("RCHECK0") == "1") {
                $this->replace_data["regd_field"]["COURSECD"] = $arr[0];
                $this->replace_data["regd_field"]["MAJORCD"] = $arr[1];
            }
            if (VARS::post("RCHECK1") =="1") {
                $this->replace_data["regd_field"]["COURSECODE"] = VARS::post("COURSECODE");
            }
            if (VARS::post("RCHECK2") =="1") {
                $this->replace_data["base_field"]["INOUTCD"] = VARS::post("INOUTCD");
            }
            if (VARS::post("RCHECK3") =="1") {
                $this->replace_data["base_field"]["BIRTHDAY"] = str_replace("/", "-", VARS::post("BIRTHDAY"));
            }
            if (VARS::post("RCHECK4") =="1") {
                $this->replace_data["base_field"]["ENT_DATE"] = str_replace("/", "-", VARS::post("ENT_DATE"));
            }
            if (VARS::post("RCHECK5") =="1") {
                $this->replace_data["base_field"]["ENT_DIV"] = VARS::post("ENT_DIV");
            }
            if (VARS::post("RCHECK6") =="1") {
                $this->replace_data["base_field"]["ENT_REASON"] = VARS::post("ENT_REASON");
            }
            if (VARS::post("RCHECK7") =="1") {
                $this->replace_data["base_field"]["ENT_SCHOOL"] = VARS::post("ENT_SCHOOL");
            }
            if (VARS::post("RCHECK8") =="1") {
                $this->replace_data["base_field"]["ENT_ADDR"] = VARS::post("ENT_ADDR");
            }
            if (VARS::post("RCHECK9") =="1") {
                $this->replace_data["base_field"]["ENT_ADDR2"] = VARS::post("ENT_ADDR2");
            }
            if (VARS::post("RCHECK10") =="1") {
                $this->replace_data["base_field"]["GRD_DATE"] = str_replace("/", "-", VARS::post("GRD_DATE"));
            }
            if (VARS::post("RCHECK11")=="1") {
                $this->replace_data["base_field"]["GRD_DIV"] = VARS::post("GRD_DIV");
            }
            if (VARS::post("RCHECK12")=="1") {
                $this->replace_data["base_field"]["GRD_REASON"] = VARS::post("GRD_REASON");
            }
            if (VARS::post("RCHECK13")=="1") {
                $this->replace_data["base_field"]["GRD_SCHOOL"] = VARS::post("GRD_SCHOOL");
            }
            if (VARS::post("RCHECK14")=="1") {
                $this->replace_data["base_field"]["GRD_ADDR"] = VARS::post("GRD_ADDR");
            }
            if (VARS::post("RCHECK15")=="1") {
                $this->replace_data["base_field"]["GRD_ADDR2"] = VARS::post("GRD_ADDR2");
            }
            //出身学校
            if ($this->Properties["use_finSchool_teNyuryoku_{$this->schoolKind}"] == "1") {
                if (VARS::post("RCHECK16")=="1") {
                    $this->replace_data["base_field"]["NYUGAKUMAE_SYUSSIN_JOUHOU"] = VARS::post("NYUGAKUMAE_SYUSSIN_JOUHOU");
                }
            } else {
                if (VARS::post("RCHECK16")=="1") {
                    $this->replace_data["base_field"]["FINSCHOOLCD"] = VARS::post("FINSCHOOLCD");
                }
                if (VARS::post("RCHECK17")=="1") {
                    $this->replace_data["base_field"]["FINISH_DATE"] = str_replace("/", "-", VARS::post("FINISH_DATE"));
                }
            }
            if (VARS::post("RCHECK18") =="1") {
                $this->replace_data["entGrd_field"]["CURRICULUM_YEAR"] = str_replace("/", "-", VARS::post("CURRICULUM_YEAR"));
            }
            if (VARS::post("RCHECK4") =="1") {
                $this->replace_data["entGrd_field"]["ENT_DATE"] = str_replace("/", "-", VARS::post("ENT_DATE"));
            }
            if (VARS::post("RCHECK5") =="1") {
                $this->replace_data["entGrd_field"]["ENT_DIV"] = VARS::post("ENT_DIV");
            }
            if (VARS::post("RCHECK6") =="1") {
                $this->replace_data["entGrd_field"]["ENT_REASON"] = VARS::post("ENT_REASON");
            }
            if (VARS::post("RCHECK7") =="1") {
                $this->replace_data["entGrd_field"]["ENT_SCHOOL"] = VARS::post("ENT_SCHOOL");
            }
            if (VARS::post("RCHECK8") =="1") {
                $this->replace_data["entGrd_field"]["ENT_ADDR"] = VARS::post("ENT_ADDR");
            }
            if (VARS::post("RCHECK9") =="1") {
                $this->replace_data["entGrd_field"]["ENT_ADDR2"] = VARS::post("ENT_ADDR2");
            }
            if (VARS::post("RCHECK10") =="1") {
                $this->replace_data["entGrd_field"]["GRD_DATE"] = str_replace("/", "-", VARS::post("GRD_DATE"));
            }
            if (VARS::post("RCHECK11")=="1") {
                $this->replace_data["entGrd_field"]["GRD_DIV"] = VARS::post("GRD_DIV");
            }
            if (VARS::post("RCHECK12")=="1") {
                $this->replace_data["entGrd_field"]["GRD_REASON"] = VARS::post("GRD_REASON");
            }
            if (VARS::post("RCHECK13")=="1") {
                $this->replace_data["entGrd_field"]["GRD_SCHOOL"] = VARS::post("GRD_SCHOOL");
            }
            if (VARS::post("RCHECK14")=="1") {
                $this->replace_data["entGrd_field"]["GRD_ADDR"] = VARS::post("GRD_ADDR");
            }
            if (VARS::post("RCHECK15")=="1") {
                $this->replace_data["entGrd_field"]["GRD_ADDR2"] = VARS::post("GRD_ADDR2");
            }
            //出身学校
            if ($this->Properties["use_finSchool_teNyuryoku_{$this->schoolKind}"] == "1") {
                if (VARS::post("RCHECK16")=="1") {
                    $this->replace_data["entGrd_field"]["NYUGAKUMAE_SYUSSIN_JOUHOU"] = VARS::post("NYUGAKUMAE_SYUSSIN_JOUHOU");
                }
            } else {
                if (VARS::post("RCHECK16")=="1") {
                    $this->replace_data["entGrd_field"]["FINSCHOOLCD"] = VARS::post("FINSCHOOLCD");
                }
                if (VARS::post("RCHECK17")=="1") {
                    $this->replace_data["entGrd_field"]["FINISH_DATE"] = str_replace("/", "-", VARS::post("FINISH_DATE"));
                }
            }
        }
        if (VARS::get("init")) {
            $this->schregno="";
            unset($this->field);
            unset($this->setFlg);
            unset($this->hist_issuedate);
        }
        if (VARS::get("ini2")) {
            unset($this->replace_data);
        }

        if ($this->cmd == "nationalityUpd") {
            $this->detail011["NATIONALITY2"] = VARS::post("NATIONALITY2");
            $this->detail011["NAME"] = VARS::post("NATIONALITY_NAME");
            $this->detail011["NAME_KANA"] = VARS::post("NATIONALITY_NAME_KANA");
            $this->detail011["NAME_ENG"] = VARS::post("NATIONALITY_NAME_ENG");
            $this->detail011["REAL_NAME"] = VARS::post("NATIONALITY_REAL_NAME");
            $this->detail011["REAL_NAME_KANA"] = VARS::post("NATIONALITY_REAL_NAME_KANA");

            $this->detail011["NATIONALITY2_FLG"] = VARS::post("NATIONALITY2_FLG");
            $this->detail011["NATIONALITY_NAME_FLG"] = VARS::post("NATIONALITY_NAME_FLG");
            $this->detail011["NATIONALITY_NAME_KANA_FLG"] = VARS::post("NATIONALITY_NAME_KANA_FLG");
            $this->detail011["NATIONALITY_NAME_ENG_FLG"] = VARS::post("NATIONALITY_NAME_ENG_FLG");
            $this->detail011["NATIONALITY_REAL_NAME_FLG"] = VARS::post("NATIONALITY_REAL_NAME_FLG");
            $this->detail011["NATIONALITY_REAL_NAME_KANA_FLG"] = VARS::post("NATIONALITY_REAL_NAME_KANA_FLG");
        }

        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //学校名取得
        $db = Query::dbCheckOut();
        $query = knja110aQuery::getSchoolName();
        $this->school_name = $db->getOne($query);
        Query::dbCheckIn($db);
        if ($this->Properties["NAME_INPUT_SIZE"] != "") {
            $this->nameSLen = $this->Properties["NAME_INPUT_SIZE"];
        } else {
            $this->nameSLen = 40;
        }
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        //ファイル名取得
        $target = $_FILES['FILE']['name'];
        $fileInfo = pathinfo($target);
        $kakutyousi = $fileInfo['extension'];
        $kakutyousi = strtoupper($kakutyousi);
        //拡張子チェック
        if ($kakutyousi != "jpg" && $kakutyousi != "JPG") {
            $this->warning = "JPGファイルを指定して下さい。\\n(".$kakutyousi."ファイルの取込は出来ません。)";
            return false;
        }

        $filename = DOCUMENTROOT ."/" .$this->control_data["LargePhotoPath"] .'/P' .$this->schregno .'.jpg';
        if (!is_uploaded_file($this->file['tmp_name'])) {
            $this->warning = "ファイル名又は\\nサイズ(100KB以上)等が不正です。";
        } elseif ($this->schregno == "") {
            $this->warning = "学籍番号が不正です。";
        } else {
            if (move_uploaded_file($this->file['tmp_name'], $filename) == false) {
                $this->warning = "顔写真の取込失敗です。";
            } else {
                $this->message = "顔写真の取込正常です。";
            }
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->field["GRCL"]=="") {
            return false;
        }

        if ($this->schregno == "" && ($this->cmd == "delete" || $this->cmd == "update" || $this->cmd == "pre_up" || $this->cmd == "next_up")) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "pre_up" || $this->cmd == "next_up") && $this->field["SCHREGNO"] != $this->schregno) {
            $this->setMessage("MSG300", "\\n学籍番号は変更できません！");
            return false;
        }

        if ($this->cmd == "update" || $this->cmd == "pre_up" || $this->cmd == "next_up") {
            $row = knja110aQuery::getStudentData($this->schregno, $this);
            if ($row["UPDATED1"] != $this->field["UPDATED1"] || $row["UPDATED2"] != $this->field["UPDATED2"]) {
                $this->setWarning("MSG307");
                return false;
            }
        }
        //必須項目チェック
        if ($this->cmd == "update" || $this->cmd == "pre_up" || $this->cmd == "next_up" || $this->cmd == "add") {
            $flag = false;
            $msg ="";
            //特別支援学校対応
            if ($this->Properties["useSpecial_Support_Hrclass"] == '1') {
                //複式年組
                if ($this->field["GHR_CD"] == "" && $this->field["GHR_ATTENDNO"] != "") {
                    $flag = true;
                    $msg .= "\\n複式年組";
                }
                //複式出席番号
                if ($this->field["GHR_CD"] != "" && $this->field["GHR_ATTENDNO"] == "") {
                    $flag = true;
                    $msg .= "\\n複式出席番号";
                }
                //FI複式クラスを使うためのプロパティ
            } elseif ($this->Properties["useFi_Hrclass"] == '1') {
                //複式年組
                if ($this->field["FI_GRADE_HR_CLASS"] == "" && $this->field["FI_ATTENDNO"] != "") {
                    $flag = true;
                    $msg .= "\\n複式年組";
                }
                //複式出席番号
                if ($this->field["FI_GRADE_HR_CLASS"] != "" && $this->field["FI_ATTENDNO"] == "") {
                    $flag = true;
                    $msg .= "\\n複式出席番号";
                }
            }
            //年組
            if ($this->field["GRCL"] == "") {
                $flag = true;
                $msg .= "\\n年組";
            }
            //出席番号
            if ($this->field["ATTENDNO"] == "") {
                $flag = true;
                $msg .= "\\n出席番号";
            }
            //年次
            if ($this->field["ANNUAL"] == "") {
                $flag = true;
                $msg .= "\\n年次";
            }
            //課程学科コード
            if ($this->field["COURSEMAJORCD"] == "") {
                $flag = true;
                $msg .= "\\n課程学科";
            }
            //コースコード
            if ($this->field["COURSECODE"] == "") {
                $flag = true;
                $msg .= "\\n";
            }
            //学籍番号
            if ($this->field["SCHREGNO"] == "") {
                $flag = true;
                $msg .= "\\n学籍番号";
            }
            //内外区分
            if ($this->field["INOUTCD"] == "") {
                $flag = true;
                $msg .= "\\n内外区分";
            }
            //氏名
            if ($this->field["NAME"] == "") {
                $flag = true;
                $msg .= "\\n氏名";
            }
            //氏名表示用
            if ($this->field["NAME_SHOW"] == "") {
                $flag = true;
                $msg .= "\\n氏名表示用";
            }
            //生年月日
            if ($this->field["BIRTHDAY"] == "") {
                $flag = true;
                $msg .= "\\n生年月日";
            }
            //入学日付
            if ($this->field["ENT_DATE"] == "") {
                $flag = true;
                $msg .= "\\n入学日付";
            }
            //入学区分
            if ($this->field["ENT_DIV"] == "") {
                $flag = true;
                $msg .= "\\n入学区分";
            }
            if ($flag == true) {
                $this->setWarning("MSG301", $msg);
                return false;
            }
        }

        //文字型チェック
        if (!preg_match("/^[ a-z]*$/i", trim($this->field["NAME_ENG"]))) {
            $this->warning = "半角英字ではありません。( 英字氏名 )";
            return false;
        }

        foreach (
                 array("NAME"           => array("( 氏名 )","[｡-ﾟ]"),
                       "NAME_SHOW"      => array("( 氏名表示用 )","[｡-ﾟ]"),
                       "REAL_NAME"      => array("( 戸籍氏名 )","[｡-ﾟ]"),
                       "REAL_NAME_KANA" => array("( 戸籍氏名かな )","[｡-ﾟ]"),
                       "NAME_KANA"      => array("( 氏名かな )","[｡-ﾟ]")) as $key => $val
        ) {
            if (mbereg($val[1], $this->field[$key])) {
                $this->setWarning("MSG901", $val[0]);
                return false;
            }
        }

        if ($this->field["BLOODTYPE"] != "") {
            if (
                strtoupper($this->field["BLOODTYPE"])!="A" && strtoupper($this->field["BLOODTYPE"])!="B" &&
                strtoupper($this->field["BLOODTYPE"])!="O" && strtoupper($this->field["BLOODTYPE"])!="AB"
            ) {
                $this->setWarning("MSG901", "\\n正しい血液型( A, B, O, AB )を入力してください！");
                return false;
            }
        }

        if ($this->field["BLOOD_RH"] != "") {
            if (!preg_match("/[+]|[-]/", $this->field["BLOOD_RH"])) {
                $this->setWarning("MSG901", "\\n正しいＲＨ型( +, - )を入力してください！");
                return false;
            }
        }

        if ($this->field["ANNUAL"] == "0") {
            $this->setWarning("MSG916", "( 年次 ) 1～99まで");
            return false;
        }

        if ($this->field["BIRTHDAY"] != "" && strtotime($this->field["BIRTHDAY"]) >= strtotime(CTRL_DATE)) {
            $this->setWarning("MSG901", "生年月日には学籍処理日より以前の日付を入力してください");
            return false;
        }

        //文字数チェック
        if ($this->cmd == "update" || $this->cmd == "pre_up" || $this->cmd == "next_up"  || $this->cmd == "add") {
            if ($this->Properties["chkNameInputHankaku"] == "1" && !$this->chkHankaku($this->field["NAME"])) {
                $this->setWarning("MSG901", "( 氏名 ) 半角文字が入力されています。");
                return false;
            } elseif ($this->Properties["NAME_INPUT_SIZE"] == "" && strlen($this->field["NAME"]) > intval($this->nameSLen) * 3) {
                $this->setWarning("MSG901", "( 氏名 ) {intval($this->nameSLen) * 3}バイトまで");
                return false;
            } elseif ($this->Properties["NAME_INPUT_SIZE"] != "" && mb_strlen($this->field["NAME"], 'UTF-8') > $this->nameSLen) {
                $this->setWarning("MSG901", "( 氏名 ) 全角{$this->nameSLen}文字を超えています。");
                return false;
            } elseif (strlen($this->field["NAME_SHOW"]) > 30) {
                $this->setWarning("MSG901", "( 表示用氏名 ) 30バイトまで");
                return false;
            } elseif (strlen($this->field["NAME_KANA"]) > 240) {
                $this->setWarning("MSG901", "( 氏名かな ) 240バイトまで");
                return false;
            } elseif (strlen($this->field["NAME_ENG"]) > 40) {
                $this->setWarning("MSG901", "( 英字氏名 ) 40バイトまで");
                return false;
            } elseif ($this->Properties["chkNameInputHankaku"] == "1" && !$this->chkHankaku($this->field["REAL_NAME"])) {
                $this->setWarning("MSG901", "( 戸籍氏名 ) 半角文字が入力されています。");
                return false;
            } elseif ($this->Properties["NAME_INPUT_SIZE"] == "" && strlen($this->field["REAL_NAME"]) > intval($this->nameSLen) * 3) {
                $this->setWarning("MSG901", "( 戸籍氏名 ) {intval($this->nameSLen) * 3}バイトまで");
                return false;
            } elseif ($this->Properties["NAME_INPUT_SIZE"] != "" && mb_strlen($this->field["REAL_NAME"], 'UTF-8') > $this->nameSLen) {
                $this->setWarning("MSG901", "( 戸籍氏名 ) 全角{$this->nameSLen}文字を超えています。");
                return false;
            } elseif (strlen($this->field["REAL_NAME_KANA"]) > 240) {
                $this->setWarning("MSG901", "( 戸籍氏名かな ) 240バイトまで");
                return false;
            } elseif (strlen($this->field["BLOODTYPE"]) > 2) {
                $this->setWarning("MSG901", "( 血液型 ) 2バイトまで");
                return false;
            } elseif (strlen($this->field["BLOOD_RH"]) > 1) {
                $this->setWarning("MSG901", "( 血液 RH型 ) 1バイトまで");
                return false;
            } elseif (strlen($this->field["ENT_REASON"]) > 120) {
                $this->setWarning("MSG901", "( 入学:事由 ）全角40文字まで");
                return false;
            } elseif (strlen($this->field["ENT_SCHOOL"]) > 75) {
                $this->setWarning("MSG901", "( 入学:学校名 ）75バイトまで");
                return false;
            } elseif (strlen($this->field["ENT_ADDR"]) > 90) {
                $this->setWarning("MSG901", "( 入学:学校住所1 ）90バイトまで");
                return false;
            } elseif (strlen($this->field["ENT_ADDR2"]) > 90) {
                $this->setWarning("MSG901", "( 入学:学校住所2 ）90バイトまで");
                return false;
            } elseif (strlen($this->field["GRD_REASON"]) > 75) {
                $this->setWarning("MSG901", "( 卒業:事由 ）75バイトまで");
                return false;
            } elseif (strlen($this->field["GRD_SCHOOL"]) > 75) {
                $this->setWarning("MSG901", "( 卒業:学校名 ）75バイトまで");
                return false;
            } elseif (strlen($this->field["GRD_ADDR"]) > 90) {
                $this->setWarning("MSG901", "( 卒業:学校住所1 ）90バイトまで");
                return false;
            } elseif (strlen($this->field["GRD_ADDR2"]) > 90) {
                if ($this->schoolKind != "H") {
                    $this->setWarning("MSG901", "( 卒業:学校住所2 ）90バイトまで");
                    return false;
                } else {
                    $this->setWarning("MSG901", "( 卒業:課程・学科等 ）90バイトまで");
                    return false;
                }
            } elseif (strlen($this->field["REMARK1"]) > 150) {
                $this->setWarning("MSG901", "( 備考１ ) 150バイトまで");
                return false;
            } elseif (strlen($this->field["REMARK2"]) > 150) {
                $this->setWarning("MSG901", "( 備考２ ) 150バイトまで");
                return false;
            } elseif (strlen($this->field["REMARK3"]) > 150) {
                $this->setWarning("MSG901", "( 備考３ ) 150バイトまで");
                return false;
            } elseif (strlen($this->field["NYUGAKUMAE_SYUSSIN_JOUHOU"]) > 306) {
                $this->setWarning("MSG901", "( 出身学校 ) 306バイトまで");
                return false;
            } elseif ($this->validate_row_cnt($this->field["NYUGAKUMAE_SYUSSIN_JOUHOU"], (25 * 2)) > 4) {
                $this->setWarning("MSG901", "( 出身学校 )は4行まで");
                return false;
            }
        }
        return true;
    }

    //データが存在するかチェック
    public function existCheck($table_flag)
    {
        if ($table_flag == "schreg_regd_dat") {
            return knja110aQuery::getExistRegdDat($this);
        }
        if ($table_flag == "schreg_base_mst") {
            return knja110aQuery::getExistBaseMst($this->field["SCHREGNO"]);
        }
        if ($table_flag == "attendno") {
            return knja110aQuery::isExistAttendno($this);
        }
        //特別支援学校対応
        if ($this->Properties["useSpecial_Support_Hrclass"] == '1') {
            if ($table_flag == "schreg_regd_ghr_dat") {
                return knja110aQuery::getExistRegdGhrDat($this);
            }
            if ($table_flag == "ghr_attendno") {
                return knja110aQuery::isExistGhrAttendno($this);
            }
            //FI複式クラスを使うためのプロパティ
        } elseif ($this->Properties["useFi_Hrclass"] == '1') {
            if ($table_flag == "schreg_regd_fi_dat") {
                return knja110aQuery::getExistRegdFiDat($this);
            }
            if ($table_flag == "attendno") {
                return knja110aQuery::isExistFiAttendno($this);
            }
        }
    }

    public function checkChange($histArray)
    {
        //内容が変更されているかのチェック(一人一人の更新)履歴を取るか取らないかの判断
        $ary = explode(",", $this->field["COURSEMAJORCD"]);
        $arr = explode(",", $this->field["GRCL"]);

        $coursecd = $ary[0];
        $majorcd  = $ary[1];

        $grade   = $arr[0];
        $hrclass = $arr[1];

        $attendno = sprintf("%03d", $this->field["ATTENDNO"]);
        $annual   = sprintf("%02d", $this->field["ANNUAL"]);

        if ($histArray["YEAR"] != CTRL_YEAR) {
            return true;
        }
        if ($histArray["SEMESTER"] != CTRL_SEMESTER) {
            return true;
        }
        if ($histArray["GRADE"] != $grade) {
            return true;
        }
        if ($histArray["HR_CLASS"] != $hrclass) {
            return true;
        }
        if ($histArray["ATTENDNO"] != $attendno) {
            return true;
        }
        if ($histArray["ANNUAL"] != $annual) {
            return true;
        }
        if ($histArray["COURSECD"] != $coursecd) {
            return true;
        }
        if ($histArray["MAJORCD"] != $majorcd) {
            return true;
        }
        if ($histArray["COURSECODE"] != $this->field["COURSECODE"]) {
            return true;
        }
        if ($histArray["NAME"] != $this->field["NAME"]) {
            return true;
        }
        if ($histArray["NAME_SHOW"] != $this->field["NAME_SHOW"]) {
            return true;
        }
        if ($histArray["NAME_KANA"] != $this->field["NAME_KANA"]) {
            return true;
        }
        if ($histArray["NAME_ENG"] != $this->field["NAME_ENG"]) {
            return true;
        }
        if ($histArray["REAL_NAME"] != $this->field["REAL_NAME"]) {
            return true;
        }
        if ($histArray["REAL_NAME_KANA"] != $this->field["REAL_NAME_KANA"]) {
            return true;
        }
        if ($histArray["HANDICAP"] != $this->field["HANDICAP"]) {
            return true;
        }

        return false;
    }

    //特別支援、FI用のSUBMIT用
    public function checkChange2($histArray)
    {
        if ($histArray["GHR_CD"] != $this->field["GHR_CD"]) {
            return true;
        }
        if ($histArray["GHR_ATTENDNO"] != $this->field["GHR_ATTENDNO"]) {
            return true;
        }
        if ($histArray["FI_GRADE_HR_CLASS"] != $this->field["FI_GRADE_HR_CLASS"]) {
            return true;
        }
        if ($histArray["FI_ATTENDNO"] != $this->field["FI_ATTENDNO"]) {
            return true;
        }

        return false;
    }

    public function &getUpdateModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        $histArray = array();
        $query = knja110aQuery::getStudentDataBefore($this, $this->schregno);
        $histArray = $db->getRow($query, DB_FETCHMODE_ASSOC); //今までのデータ
        //DB切断
        Query::dbCheckIn($db);
        if ($this->validateInput()) {
            if ($this->existCheck("attendno")) {
                $this->setWarning("MSG302", "( 出席番号 )");
                $this->setCmd("edit");
                return;
            }
            //特別支援学校対応
            if ($this->Properties["useSpecial_Support_Hrclass"] == '1') {
                if ($this->field["GHR_CD"] != "" && $this->field["GHR_ATTENDNO"] != "") {
                    if ($this->existCheck("ghr_attendno")) {
                        $this->setWarning("MSG302", "( 複式出席番号 )");
                        $this->setCmd("edit");
                        return;
                    }
                }
                //FI複式クラスを使うためのプロパティ
            } elseif ($this->Properties["useFi_Hrclass"] == '1') {
                if ($this->field["FI_GRADE_HR_CLASS"] != "" && $this->field["FI_ATTENDNO"] != "") {
                    if ($this->existCheck("attendno")) {
                        $this->setWarning("MSG302", "( 複式出席番号 )");
                        $this->setCmd("edit");
                        return;
                    }
                }
            }
            if ($this->checkChange($histArray)) {  //どれか項目が変更されていたらtrue、変更されていない場合false
                //何か変更されていたら
                $this->setCmd("subForm");
            } elseif ($this->checkChange2($histArray)) {
                //特別支援またはFIのクラス、出席番号が変更されていたら
                $this->update();
                $this->setMessage("MSG201");
                $this->setCmd("subEdit");
            } else {
                //何も変更されていない場合
                $this->update();
                $this->setMessage("MSG201");
                $this->setCmd("edit");
            }
        } else {
            $this->setCmd("edit");
            return;
        }
    }

    //更新
    public function &getSubUpdateModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        if ($this->validateInput()) {
            //まず現状を履歴に保存
            $histArray = array();
            $query = knja110aQuery::getStudentDataBefore($this, $this->schregno);
            $histArray = $db->getRow($query, DB_FETCHMODE_ASSOC); //今までのデータ

            $query = knja110aQuery::getEdate($this->field["SCHREGNO"]); //前回の最終日をとってくる
            $edate = $db->getOne($query);

            $histPutFlg = false;
            foreach ($this->setFlg as $key => $val) {
                $histPutFlg = $val == "1" ? true : $histPutFlg;
            }
            if ($histPutFlg) {
                list($histQuery, $yoyakuQuery) = knja110aQuery::insertHistAndYoyaku($db, $histArray, $this, $edate, $this->field["E_APPDATE"], $this->setFlg); //履歴に保存
                $db->query($histQuery);

                if ($yoyakuQuery) {
                    $db->query($yoyakuQuery);
                }
            }

            $this->update();
            $this->setMessage("MSG201");
            if ($this->GradeClass != $this->field["GRCL"]) {
                unset($this->field);
                unset($this->schregno);
            }
            if ($this->schregno == "") {
                unset($this->field);
            }
        }
        //DB切断
        Query::dbCheckIn($db);
    }

    public function &replaceModel()
    {
        //生徒が選択されてない場合
        if (!strlen($this->replace_data["selectdata"])) {
            $this->setCmd("replace");
            $this->setWarning("MSG304");
            return false;
        }
        //生年月日は過去日付
        if (
            $this->replace_data["base_field"]["BIRTHDAY"] != "" &&
            strtotime($this->replace_data["base_field"]["BIRTHDAY"]) >= strtotime(CTRL_DATE)
        ) {
            $this->setCmd("replace");
            $this->setWarning("生年月日には学籍処理日より以前の日付を入力してください");
            return false;
        }
        //最低ひとつでもチェックがついてる場合のみ更新
        if (!in_array("1", $this->replace_data["base_check"]) && !in_array("1", $this->replace_data["regd_check"])) {
            $this->setCmd("replace");
            $this->setWarning("更新する項目がありません");
            return false;
        }
        //入力チェック
        if (strlen($this->replace_data["base_field"]["GRD_REASON"]) > 75) {
            $this->setCmd("replace");
            $this->setWarning("MSG901", "( 事由 ）75バイトまで");
            return false;
        }
        if (strlen($this->replace_data["base_field"]["ENT_ADDR"]) > 90) {
            $this->setCmd("replace");
            $this->setWarning("MSG901", "( 入学住所1 ）90バイトまで");
            return false;
        }
        if (strlen($this->replace_data["base_field"]["ENT_ADDR2"]) > 90) {
            $this->setCmd("replace");
            $this->setWarning("MSG901", "( 入学住所2 ）90バイトまで");
            return false;
        }
        if (strlen($this->replace_data["base_field"]["GRD_ADDR"]) > 90) {
            $this->setCmd("replace");
            $this->setWarning("MSG901", "( 卒業住所1 ）90バイトまで");
            return false;
        }
        if (strlen($this->replace_data["base_field"]["GRD_ADDR2"]) > 90) {
            $this->setCmd("replace");
            $this->setWarning("MSG901", "( 卒業住所2 ）90バイトまで");
            return false;
        }
        if (strlen($this->field["ENT_REASON"]) > 120) {
            $this->setWarning("MSG901", "( 入学:事由 ）全角40文字まで");
            return false;
        }

        //チェックがついている項目があるテーブルのみを更新
        $db = Query::dbCheckOut();

        $result = $db->query(knja110aQuery::getEntGrdDate($this));
        $schregnos = array();
        $dateflg = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($this->replace_data["entGrd_field"]['ENT_DATE'] != '') {
                $row['ENT_DATE'] = $this->replace_data["entGrd_field"]['ENT_DATE'];
            }
            if ($this->replace_data["entGrd_field"]['GRD_DATE'] != '') {
                $row['GRD_DATE'] = $this->replace_data["entGrd_field"]['GRD_DATE'];
            }
            if ($row['GRD_DATE'] == '' || $row['ENT_DATE'] == '' || $row['GRD_DATE']>$row['ENT_DATE']) {
                $schregnos[] = $row['SCHREGNO'];
            } else {
                $dateflg = true;
            }
        }
        $selectdata = $this->replace_data["selectdata"];
        $this->replace_data["selectdata"] = implode(',', $schregnos);

        if (in_array("1", $this->replace_data["base_check"]) && in_array("1", $this->replace_data["regd_check"])) {
            knja110aQuery::replaceUpdateRegd1($db, $this);
            knja110aQuery::replaceUpdateBase1($db, $this);
            knja110aQuery::replaceUpdateEntGrd1($db, $this);

            $array = explode(",", $this->replace_data["selectdata"]);
            $db->autoCommit(false);
            for ($i = 0; $i < get_count($array); $i++) {
                $this->baseRegdUpdFromHist($db, $array[$i]);
            }
            $db->commit();
        } elseif (!in_array("1", $this->replace_data["base_check"]) && in_array("1", $this->replace_data["regd_check"])) {
            knja110aQuery::replaceUpdateRegd1($db, $this);

            $array = explode(",", $this->replace_data["selectdata"]);
            $db->autoCommit(false);
            for ($i = 0; $i < get_count($array); $i++) {
                $this->baseRegdUpdFromHist($db, $array[$i]);
            }
            $db->commit();
        } elseif (in_array("1", $this->replace_data["base_check"]) && !in_array("1", $this->replace_data["regd_check"])) {
            knja110aQuery::replaceUpdateBase1($db, $this);
            knja110aQuery::replaceUpdateEntGrd1($db, $this);

            $array = explode(",", $this->replace_data["selectdata"]);
            $db->autoCommit(false);
            for ($i = 0; $i < get_count($array); $i++) {
                $this->baseRegdUpdFromHist($db, $array[$i]);
            }
            $db->commit();
        }
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
        if ($dateflg) {
            $this->message .= '卒業日付が範囲外の生徒は更新対象外です。';
        }
        $this->replace_data["selectdata"] = $selectdata;
        //添え字を０から順序良くする
        $this->replace_data["check"] = array_merge((array)$this->replace_data["regd_check"], (array)$this->replace_data["base_check"]);
        $this->replace_data["check"][] = $this->replace_data["check_all"];
    }

    public function &subReplaceModel()
    {
        //生徒が選択されてない場合
        if (!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304");
            return false;
        }
        //生年月日は過去日付
        if (
            $this->replace_data["base_field"]["BIRTHDAY"] != "" &&
            strtotime($this->replace_data["base_field"]["BIRTHDAY"]) >= strtotime(CTRL_DATE)
        ) {
            $this->setWarning("生年月日には学籍処理日より以前の日付を入力してください");
            return false;
        }
        //最低ひとつでもチェックがついてる場合のみ更新
        if (!in_array("1", $this->replace_data["base_check"]) && !in_array("1", $this->replace_data["regd_check"])) {
            return false;
        }
        //入力チェック
        if (strlen($this->replace_data["base_field"]["GRD_REASON"]) > 75) {
            $this->setWarning("MSG901", "( 事由 ）75バイトまで");
            return false;
        }
        //チェックがついている項目があるテーブルのみを更新
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $schregnos = explode(",", $this->replace_data["selectdata"]);
        foreach ($this->replace_data["regd_field"] as $key => $val) {
            $regd_field_data[$key][TEXT] = $val;
        }
        foreach ($this->replace_data["base_field"] as $key => $val) {
            $base_field_data[$key][TEXT] = $val;
        }

        for ($i = 0; $i < get_count($schregnos); $i++) {
            $changeFlag = true; //変更があったかのチェックに使うtrueは変更なし
            /*********** 前もって変更前のデータを取得しておく *************/
            $histArray = array();
            $query = knja110aQuery::getStudentDataBefore($this, $schregnos[$i]);
            $histArray = $db->getRow($query, DB_FETCHMODE_ASSOC); //今までのデータ
            /************ ここまで ************/
            if (in_array("1", $this->replace_data["base_check"]) && in_array("1", $this->replace_data["regd_check"])) {
                $changeFlag = $this->checkChangeBaseField($db, $base_field_data, $schregnos[$i]); //データに変更があるのかチェック
                $changeFlag = $this->checkChangeRegdField($db, $regd_field_data, $schregnos[$i]); //データに変更があるのかチェック
                knja110aQuery::replaceUpdateRegd($db, $regd_field_data, $schregnos[$i]);
                knja110aQuery::replaceUpdateBase($db, $this, $base_field_data, $schregnos[$i]);
            } elseif (!in_array("1", $this->replace_data["base_check"]) && in_array("1", $this->replace_data["regd_check"])) {
                $changeFlag = $this->checkChangeRegdField($db, $regd_field_data, $schregnos[$i]); //データに変更があるのかチェック
                knja110aQuery::replaceUpdateRegd($db, $regd_field_data, $schregnos[$i]);
            } elseif (in_array("1", $this->replace_data["base_check"]) && !in_array("1", $this->replace_data["regd_check"])) {
                $changeFlag = $this->checkChangeBaseField($db, $base_field_data, $schregnos[$i]); //データに変更があるのかチェック
                knja110aQuery::replaceUpdateBase($db, $this, $base_field_data, $schregnos[$i]);
            }
            if (!$changeFlag) {
                //変更有りの人は履歴を取る
                $query = knja110aQuery::getEdate($schregnos[$i]); //前回の最終日をとってくる
                $edate = $db->getOne($query);

                $query = knja110aQuery::insertHist($histArray, $edate, $this->field["E_APPDATE"], $this->setFlg); //履歴に保存
                $db->query($query);
            }
        }
        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
        //添え字を０から順序良くする
        $this->replace_data["check"] = array_merge((array)$this->replace_data["regd_check"], (array)$this->replace_data["base_check"]);
        $this->replace_data["check"][] = $this->replace_data["check_all"];
    }

    //その人のデータに変更があるのけチェックschreg_base_mst
    public function checkChangeBaseField($db, $base_field_data, $schregno)
    {
        $query = knja110aQuery::getBaseField($schregno);
        $result = $db->getRow($query, DB_FETCHMODE_ASSOC);
        foreach ($base_field_data as $key => $val) {
            if ($result[$key] != $val[0][0]) {
                return false;
            }
        }
        return true;
    }

    //その人のデータに変更があるのけチェックschreg_regd_dat
    public function checkChangeRegdField($db, $regd_field_data, $schregno)
    {
        $query = knja110aQuery::getRegdField($schregno);
        $result = $db->getRow($query, DB_FETCHMODE_ASSOC);
        foreach ($regd_field_data as $key => $val) {
            if ($result[$key] != $val[0][0]) {
                return false;
            }
        }
        return true;
    }


    public function &getAddingModel()
    {
        if ($this->validateInput()) {
            //データの有無チェック
            if ($this->existCheck("schreg_regd_dat")) {
                $this->setWarning("MSG302");
                return;
            }
            if ($this->existCheck("schreg_base_mst")) {
                $this->setWarning("MSG302");
                return;
            }
            if ($this->existCheck("attendno")) {
                $this->setWarning("MSG302", "( 出席番号 )");
                return;
            }
            //特別支援学校対応
            if ($this->Properties["useSpecial_Support_Hrclass"] == '1') {
                if ($this->field["GHR_CD"] != "" && $this->field["GHR_ATTENDNO"] != "") {
                    if ($this->existCheck("schreg_regd_ghr_dat")) {
                        $this->setWarning("MSG302");
                        return;
                    }
                    if ($this->existCheck("ghr_attendno")) {
                        $this->setWarning("MSG302", "( 複式出席番号 )");
                        return;
                    }
                }
                //FI複式クラスを使うためのプロパティ
            } elseif ($this->Properties["useFi_Hrclass"] == '1') {
                if ($this->field["FI_GRADE_HR_CLASS"] != "" && $this->field["FI_ATTENDNO"] != "") {
                    if ($this->existCheck("schreg_regd_fi_dat")) {
                        $this->setWarning("MSG302");
                        return;
                    }
                    if ($this->existCheck("attendno")) {
                        $this->setWarning("MSG302", "( 複式出席番号 )");
                        return;
                    }
                }
            }
            if ($this->add()) {
                $this->setMessage("MSG201");
                $this->schregno = $this->field["SCHREGNO"];
                if ($this->GradeClass != $this->field["GRCL"]) {
                    unset($this->field);
                    unset($this->schregno);
                }
                return;
            }
        }
    }

    public function &update()
    {
        $resultEntGrd = knja110aQuery::getInsUpdEntGrd($this);               //学籍学歴データ更新
        $result1 = knja110aQuery::getUpdateQuery($this, "schreg_regd_dat");   //在籍データ更新
        $result2 = knja110aQuery::getUpdateQuery($this, "schreg_base_mst");   //基礎マスタ更新
        //特別支援学校対応
        if ($this->Properties["useSpecial_Support_Hrclass"] == '1') {
            $result3 = knja110aQuery::getUpdateQuery($this, "schreg_regd_ghr_dat");   //複式在籍データ更新
        //FI複式クラスを使うためのプロパティ
        } elseif ($this->Properties["useFi_Hrclass"] == '1') {
            $result4 = knja110aQuery::getUpdateQuery($this, "schreg_regd_fi_dat");    //FI複式在籍データ更新
        }

        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        $this->baseRegdUpdFromHist($db, $this->field["SCHREGNO"]);
        $db->commit();

        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }

    public function &add()
    {
        $resultEntGrd = knja110aQuery::getInsUpdEntGrd($this);               //学籍学歴データ更新
        $result1 = knja110aQuery::getInsertQuery($this, "schreg_regd_dat");
        $result2 = knja110aQuery::getInsertQuery($this, "schreg_base_mst");
        //特別支援学校対応
        if ($this->Properties["useSpecial_Support_Hrclass"] == '1') {
            $result3 = knja110aQuery::getInsertQuery($this, "schreg_regd_ghr_dat");
        //FI複式クラスを使うためのプロパティ
        } elseif ($this->Properties["useFi_Hrclass"] == '1') {
            $result4 = knja110aQuery::getInsertQuery($this, "schreg_regd_fi_dat");
        }

        if ($result1 == true && $result2 == true) {
            $result = true;
        } else {
            $result = false;
        }
        return $result;
    }

    //各コンボボックス作成
    public function createCombo($objForm, $db, $namecd, $varname, $value, $fst)
    {
        $result = $db->query(knja110aQuery::getNameMstData($namecd));
        $opt = array();

        //性別と卒業区分には先頭に空をセット
        if ($fst=="1") {
            $opt[] = array("label" => "","value" => "");
        }

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array( "label" => $row["NAMECD2"]."  ".htmlspecialchars($row["NAME1"]),
                              "value" => $row["NAMECD2"]);
        }

        $objForm->ae(
            array("type"        => "select",
                  "name"        => $varname,
                  "size"        => 1,
                  "maxlength"   => 10,
                  "extrahtml"   => "",
                  "value"       => $value,
                  "options"     => $opt)
        );

        return $objForm->ge($varname);
    }

    public function &getUpdateHistModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        if (!$this->validateInputHist($db)) {
            $this->cmd = "histEdit";
            return;
        }

        if ($this->cmd == "histAdd") {
            $this->addHist($db);
        } elseif ($this->cmd == "histUpd") {
            $this->delHist($db);
            $this->addHist($db);
        } else {
            $this->delHist($db);
        }

        $this->baseRegdUpdFromHist($db, $this->schregno);

        $this->setMessage("MSG201");

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
        return;
    }

    //入力項目チェック
    public function validateInputHist($db)
    {
        if ($this->schregno == "" && ($this->cmd == "histAdd" || $this->cmd == "histUpd" || $this->cmd == "histDel")) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "histAdd" || $this->cmd == "histUpd" || $this->cmd == "histDel") && $this->histField["SCHREGNO"] != $this->schregno) {
            $this->setMessage("MSG300", "\\n学籍番号は変更できません！");
            return false;
        }

        $checkArray["ISSUEDATE"]                  = array("TITLE" => "開始日",       "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
        $checkArray["EXPIREDATE"]                 = array("TITLE" => "終了日",       "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
        $checkArray["GRADE_CLASS"]                = array("TITLE" => "年組",         "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
        $checkArray["ATTENDNO"]                   = array("TITLE" => "番号",         "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
        $checkArray["ANNUAL"]                     = array("TITLE" => "年次",         "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
        $checkArray["COURSEMAJORCD"]              = array("TITLE" => "課程学科",     "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
        $checkArray["COURSECODE"]                 = array("TITLE" => "コース",       "HISSU" => "ON", "LENGTH" => "",   "LEN" => "");
        $checkArray["NAME"]                       = array("TITLE" => "氏名",         "HISSU" => "ON", "LENGTH" => "ON", "LEN" => intval($this->nameSLen) * 3);
        $checkArray["NAME_SHOW"]                  = array("TITLE" => "表示用氏名",   "HISSU" => "ON", "LENGTH" => "ON", "LEN" => "30");
        $checkArray["NAME_KANA"]                  = array("TITLE" => "氏名かな",     "HISSU" => "",   "LENGTH" => "ON", "LEN" => "240");
        $checkArray["NAME_ENG"]                   = array("TITLE" => "英字氏名",     "HISSU" => "",   "LENGTH" => "ON", "LEN" => "40");
        $checkArray["REAL_NAME"]                  = array("TITLE" => "戸籍氏名",     "HISSU" => "",   "LENGTH" => "ON", "LEN" => intval($this->nameSLen) * 3);
        $checkArray["REAL_NAME_KANA"]             = array("TITLE" => "戸籍氏名かな", "HISSU" => "",   "LENGTH" => "ON", "LEN" => "240");
        $checkArray["NATIONALITY_NAME"]           = array("TITLE" => "第二国籍氏名",         "HISSU" => "",   "LENGTH" => "ON", "LEN" => "120");
        $checkArray["NATIONALITY_NAME_KANA"]      = array("TITLE" => "第二国籍氏名かな",     "HISSU" => "",   "LENGTH" => "ON", "LEN" => "240");
        $checkArray["NATIONALITY_NAME_ENG"]       = array("TITLE" => "第二国籍英字氏名",     "HISSU" => "",   "LENGTH" => "ON", "LEN" => "40");
        $checkArray["NATIONALITY_REAL_NAME"]      = array("TITLE" => "第二国籍戸籍氏名",     "HISSU" => "",   "LENGTH" => "ON", "LEN" => "120");
        $checkArray["NATIONALITY_REAL_NAME_KANA"] = array("TITLE" => "第二国籍戸籍氏名かな", "HISSU" => "",   "LENGTH" => "ON", "LEN" => "240");

        //必須項目チェック
        $flag = false;
        $msg = "";
        if ($this->cmd == "histAdd" || $this->cmd == "histUpd") {
            foreach ($checkArray as $fieldName => $fieldVal) {
                if ($fieldVal["HISSU"]) {
                    if ($this->histField[$fieldName] == "") {
                        $flag = true;
                        $msg .= "\\n".$fieldVal["TITLE"];
                    }
                }
            }
        } else {
            //開始日付
            if ($this->histField["ISSUEDATE"] == "") {
                $flag = true;
                $msg .= "\\n開始日付";
            }
            //学籍番号
            if ($this->histField["SCHREGNO"] == "") {
                $flag = true;
                $msg .= "\\n学籍番号";
            }
        }
        if ($flag == true) {
            $this->setWarning("MSG301", $msg);
            return false;
        }
        if ($this->cmd == "histAdd" || $this->cmd == "histUpd") {
            if (str_replace("/", "", $this->histField["ISSUEDATE"]) > str_replace("/", "", $this->histField["EXPIREDATE"])) {
                $this->setWarning("MSG901", "開始終了日付が、逆転しています。");
                return false;
            }

            $query = knja110aQuery::getSchregHistDateCheck($this);
            $dateExists = $db->getOne($query);
            if ($dateExists > "0") {
                $this->setWarning("MSG901", "日付範囲が不正です。");
                return false;
            }
        }

        $query = knja110aQuery::getSchregHistData($this->schregno, $this->histField["ISSUEDATE"]);
        $checkExists = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($this->cmd == "histAdd" && is_array($checkExists)) {
            $this->setWarning("MSG302");
            return false;
        } elseif (($this->cmd == "histUpd" || $this->cmd == "histDel") && !is_array($checkExists)) {
            $this->setWarning("MSG308");
            return false;
        }

        //文字型チェック
        if (!preg_match("/^[ a-z]*$/i", trim($this->histField["NAME_ENG"]))) {
            $this->warning = "半角英字ではありません。( 英字氏名 )";
            return false;
        }

        foreach (
                 array("NAME"           => array("( 氏名 )","[｡-ﾟ]"),
                       "NAME_SHOW"      => array("( 氏名表示用 )","[｡-ﾟ]"),
                       "REAL_NAME"      => array("( 戸籍氏名 )","[｡-ﾟ]"),
                       "REAL_NAME_KANA" => array("( 戸籍氏名かな )","[｡-ﾟ]"),
                       "NAME_KANA"      => array("( 氏名かな )","[｡-ﾟ]")) as $key => $val
        ) {
            if (mbereg($val[1], $this->histField[$key])) {
                $this->setWarning("MSG901", $val[0]);
                return false;
            }
        }

        if ($this->histField["ANNUAL"] == "0") {
            $this->setWarning("MSG916", "( 年次 ) 1～99まで");
            return false;
        }

        //文字数チェック
        if ($this->cmd == "histAdd" || $this->cmd == "histUpd") {
            foreach ($checkArray as $fieldName => $fieldVal) {
                if ($fieldVal["LENGTH"]) {
                    if ($this->Properties["chkNameInputHankaku"] == "1" && in_array($fieldName, array("NAME", "REAL_NAME"), true) && !$this->chkHankaku($this->histField[$fieldName])) {
                        $this->setWarning("MSG901", "( ".$fieldVal["TITLE"]." ) 半角文字が入力されています。");
                        return false;
                    } elseif ($this->Properties["NAME_INPUT_SIZE"] != "" && in_array($fieldName, array("NAME", "REAL_NAME"), true)) {
                        if (mb_strlen($this->histField[$fieldName], 'UTF-8') > $this->nameSLen) {
                            $this->setWarning("MSG901", "( ".$fieldVal["TITLE"]." ) 全角{$this->nameSLen}文字を超えています。");
                            return false;
                        }
                    } else {
                        if (strlen($this->histField[$fieldName]) > $fieldVal["LEN"]) {
                            $this->setWarning("MSG901", "( ".$fieldVal["TITLE"]." ) ".$fieldVal["LEN"]."バイトまで");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public function addHist($db)
    {
        $query = knja110aQuery::getInsHistQuery($this);
        $this->hist_issuedate = $this->histField["ISSUEDATE"];
        $this->cmd = "histEdit";
        $db->query($query);
        $query = knja110aQuery::setBaseYearDetail($db, $this);
    }

    public function delHist($db)
    {
        $query = knja110aQuery::getDelHistQuery($this);
        $this->cmd = "changeCmb";
        $db->query($query);
    }

    public function &getUpdateEntGrdHistModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        if (!$this->validateInputEntGrdHist($db)) {
            $this->cmd = "entGrdEdit";
            return;
        }

        if ($this->cmd == "entGrdHistAdd") {
            $this->entGrdAddHist($db);
        } elseif ($this->cmd == "entGrdHistUpd") {
            $this->entGrdUpdHist($db);
        } else {
            $this->entGrdDelHist($db);
        }

        $this->setMessage("MSG201");

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
        return;
    }

    //入力項目チェック
    public function validateInputEntGrdHist($db)
    {
        if ($this->schregno == "" && ($this->cmd == "entGrdHistAdd" || $this->cmd == "entGrdHistUpd" || $this->cmd == "entGrdHistDel")) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "entGrdHistAdd" || $this->cmd == "entGrdHistUpd" || $this->cmd == "entGrdHistDel") && $this->grdEntHistField["SCHREGNO"] != $this->schregno) {
            $this->setMessage("MSG300", "\\n学籍番号は変更できません！");
            return false;
        }

        $checkArray["SCHOOL_KIND"]                  = array("TITLE" => "校種",            "HISSU" => "ON", "LENGTH" => "",    "LEN" => "");
        $checkArray["FINSCHOOLCD"]                  = array("TITLE" => "出身学校",        "HISSU" => "",   "LENGTH" => "ON",  "LEN" => $this->finschoolcdKeta);
        $checkArray["FINISH_DATE"]                  = array("TITLE" => "出身学校卒業日",  "HISSU" => "",   "LENGTH" => "",    "LEN" => "");
        $checkArray["ENT_DATE"]                     = array("TITLE" => "入学日",          "HISSU" => "ON", "LENGTH" => "",    "LEN" => "");
        $checkArray["CURRICULUM_YEAR"]              = array("TITLE" => "課程入学年度",    "HISSU" => "",   "LENGTH" => "ON",  "LEN" => "4");
        $checkArray["ENT_DIV"]                      = array("TITLE" => "入学区分",        "HISSU" => "ON", "LENGTH" => "",    "LEN" => "");
        $checkArray["ENT_REASON"]                   = array("TITLE" => "入学:事由",       "HISSU" => "",   "LENGTH" => "ON",  "LEN" => "120");
        $checkArray["ENT_SCHOOL"]                   = array("TITLE" => "入学:学校名",     "HISSU" => "",   "LENGTH" => "ON",  "LEN" => "75");
        $checkArray["ENT_ADDR"]                     = array("TITLE" => "入学:学校住所1",  "HISSU" => "",   "LENGTH" => "ON",  "LEN" => "90");
        $checkArray["ENT_ADDR2"]                    = array("TITLE" => "入学:学校住所2",  "HISSU" => "",   "LENGTH" => "ON",  "LEN" => "90");
        $checkArray["GRD_DATE"]                     = array("TITLE" => "卒業日",          "HISSU" => "",   "LENGTH" => "",    "LEN" => "");
        $checkArray["GRD_DIV"]                      = array("TITLE" => "卒業区分",        "HISSU" => "",   "LENGTH" => "",    "LEN" => "");
        $checkArray["GRD_REASON"]                   = array("TITLE" => "卒業:事由",       "HISSU" => "",   "LENGTH" => "ON",  "LEN" => "75");
        $checkArray["GRD_SCHOOL"]                   = array("TITLE" => "卒業:学校名",     "HISSU" => "",   "LENGTH" => "ON",  "LEN" => "75");
        $checkArray["GRD_ADDR"]                     = array("TITLE" => "卒業:学校住所1",  "HISSU" => "",   "LENGTH" => "ON",  "LEN" => "90");
        if ($this->schoolKind != "H") {
            $checkArray["GRD_ADDR2"]                    = array("TITLE" => "卒業:学校住所2",  "HISSU" => "",   "LENGTH" => "ON",  "LEN" => "90");
        } else {
            $checkArray["GRD_ADDR2"]                    = array("TITLE" => "卒業:課程・学科等",  "HISSU" => "",   "LENGTH" => "ON",  "LEN" => "90");
        }
        $checkArray["GRD_TERM"]                     = array("TITLE" => "卒期",            "HISSU" => "",   "LENGTH" => "ON",  "LEN" => "4");
        $checkArray["TENGAKU_SAKI_ZENJITU"]         = array("TITLE" => "出身学校",        "HISSU" => "",   "LENGTH" => "ON",  "LEN" => "768");
        $checkArray["TENGAKU_SAKI_GRADE"]           = array("TITLE" => "出身学年",        "HISSU" => "",   "LENGTH" => "ON",  "LEN" => "60");
        $checkArray["NYUGAKUMAE_SYUSSIN_JOUHOU"]    = array("TITLE" => "出身学校",        "HISSU" => "",   "LENGTH" => "ON",  "LEN" => "306");

        //必須項目チェック
        $flag = false;
        $msg = "";
        if ($this->cmd == "entGrdHistAdd" || $this->cmd == "entGrdHistUpd") {
            foreach ($checkArray as $fieldName => $fieldVal) {
                if ($fieldVal["HISSU"]) {
                    if ($this->grdEntHistField[$fieldName] == "") {
                        $flag = true;
                        $msg .= "\\n".$fieldVal["TITLE"];
                    }
                }
            }
        } else {
            //校種
            if ($this->grdEntHistField["SCHOOL_KIND"] == "") {
                $flag = true;
                $msg .= "\\n校種";
            }
            //学籍番号
            if ($this->grdEntHistField["SCHREGNO"] == "") {
                $flag = true;
                $msg .= "\\n学籍番号";
            }
        }
        if ($flag == true) {
            $this->setWarning("MSG301", $msg);
            return false;
        }

        $query = knja110aQuery::getEntGrdHistData($this, $this->schregno, $this->grdEntHistField["SCHOOL_KIND"]);
        $checkExists = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($this->cmd == "entGrdHistAdd" && is_array($checkExists)) {
            $this->setWarning("MSG302");
            return false;
        } elseif (($this->cmd == "entGrdHistUpd" || $this->cmd == "entGrdHistDel") && !is_array($checkExists)) {
            $this->setWarning("MSG308");
            return false;
        }

        //文字数チェック
        if ($this->cmd == "entGrdHistAdd" || $this->cmd == "entGrdHistUpd") {
            foreach ($checkArray as $fieldName => $fieldVal) {
                if ($fieldVal["LENGTH"]) {
                    if ($fieldName == "ENT_REASON"
                        && strlen($this->grdEntHistField[$fieldName]) > $fieldVal["LEN"]) {
                        $this->setWarning("MSG901", "( ".$fieldVal["TITLE"]." ) "."全角40文字まで");
                        return false;
                    } elseif (strlen($this->grdEntHistField[$fieldName]) > $fieldVal["LEN"]) {
                        // if (strlen($this->grdEntHistField[$fieldName]) > $fieldVal["LEN"]) {
                        $this->setWarning("MSG901", "( ".$fieldVal["TITLE"]." ) ".$fieldVal["LEN"]."バイトまで");
                        return false;
                    } elseif ($this->validate_row_cnt($this->grdEntHistField["NYUGAKUMAE_SYUSSIN_JOUHOU"], (25 * 2)) > 4) {
                        $this->setWarning("MSG901", "( 出身学校 )は4行まで");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public function entGrdAddHist($db)
    {
        $query = knja110aQuery::getInsEntGrdHistQuery($db, $this);
        $this->baseMstUpd($db);
        $this->cmd = "entGrdEdit";
    }

    public function entGrdUpdHist($db)
    {
        $query = knja110aQuery::getUpdEntGrdHistQuery($db, $this);
        $this->baseMstUpd($db);
        $this->cmd = "entGrdEdit";
    }

    public function entGrdDelHist($db)
    {
        $query = knja110aQuery::getEntGrdDelHistQuery($db, $this);
        $this->baseMstUpd($db);
        $this->cmd = "changeEntGrdCmb";
    }

    public function baseMstUpd($db)
    {
        $query = knja110aQuery::getNowSchoolKind($this);
        $nowSchoolKind = $db->getOne($query);
        if ($nowSchoolKind == $this->grdEntHistField["SCHOOL_KIND"]) {
            $query = knja110aQuery::getUpdBaseMstEntGrd($db, $this);
        }
    }

    public function baseRegdUpdFromHist($db, $schregno)
    {
        $query = knja110aQuery::getBaseHistUpd($schregno);
        $histData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $baseUpdFlg = false;
        $base = array();
        $regdUpdFlg = false;
        $regd = array();
        if (is_array($histData)) {
            if ($histData["GRADE_FLG"] == "1") {
                $regdUpdFlg = true;
                $regd["GRADE"][TEXT] = $histData["GRADE"];
            }
            if ($histData["HR_CLASS_FLG"] == "1") {
                $regdUpdFlg = true;
                $regd["HR_CLASS"][TEXT] = $histData["HR_CLASS"];
            }
            if ($histData["ATTENDNO_FLG"] == "1") {
                $regdUpdFlg = true;
                $regd["ATTENDNO"][TEXT] = $histData["ATTENDNO"];
            }
            if ($histData["ANNUAL_FLG"] == "1") {
                $regdUpdFlg = true;
                $regd["ANNUAL"][TEXT] = $histData["ANNUAL"];
            }
            if ($histData["COURSECD_FLG"] == "1") {
                $regdUpdFlg = true;
                $regd["COURSECD"][TEXT] = $histData["COURSECD"];
            }
            if ($histData["MAJORCD_FLG"] == "1") {
                $regdUpdFlg = true;
                $regd["MAJORCD"][TEXT] = $histData["MAJORCD"];
            }
            if ($histData["COURSECODE_FLG"] == "1") {
                $regdUpdFlg = true;
                $regd["COURSECODE"][TEXT] = $histData["COURSECODE"];
            }
            if ($histData["NAME_FLG"] == "1") {
                $baseUpdFlg = true;
                $base["NAME"][TEXT] = $histData["NAME"];
            }
            if ($histData["NAME_SHOW_FLG"] == "1") {
                $baseUpdFlg = true;
                $base["NAME_SHOW"][TEXT] = $histData["NAME_SHOW"];
            }
            if ($histData["NAME_KANA_FLG"] == "1") {
                $baseUpdFlg = true;
                $base["NAME_KANA"][TEXT] = $histData["NAME_KANA"];
            }
            if ($histData["NAME_ENG_FLG"] == "1") {
                $baseUpdFlg = true;
                $base["NAME_ENG"][TEXT] = $histData["NAME_ENG"];
            }
            if ($histData["REAL_NAME_FLG"] == "1") {
                $baseUpdFlg = true;
                $base["REAL_NAME"][TEXT] = $histData["REAL_NAME"];
            }
            if ($histData["REAL_NAME_KANA_FLG"] == "1") {
                $baseUpdFlg = true;
                $base["REAL_NAME_KANA"][TEXT] = $histData["REAL_NAME_KANA"];
            }
            if ($histData["HANDICAP_FLG"] == "1") {
                $baseUpdFlg = true;
                $base["HANDICAP"][TEXT] = $histData["HANDICAP"];
            }

            if ($baseUpdFlg) {
                $query = knja110aQuery::getBaseUpdFromHist($base, $schregno);
                $db->query($query);
            }
            if ($regdUpdFlg) {
                $query = knja110aQuery::getRegdUpdFromHist($regd, $histData, $schregno);
                $db->query($query);
            }
        }

        return true;
    }

    public function getNationalityUpdateModel()
    {

        //DB接続
        $db = Query::dbCheckOut();
        $query = knja110aQuery::delDetail011($this);
        $db->query($query);

        $query = knja110aQuery::insDetail011($this);
        $db->query($query);

        //DB切断
        Query::dbCheckIn($db);
    }

    public function chkHankaku($chkStr)
    {
        foreach (preg_split('//u', $chkStr, -1, PREG_SPLIT_NO_EMPTY) as $idx => $v) {
            if (strlen($v) == 1) {
                return false;
            }
        }
        return true;
    }
}
