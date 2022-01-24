<?php

require_once('for_php7.php');


class knje066Model extends Model {

    var $cmd;
    var $schregno;
    var $search_div;
    var $payment_date;
    var $payment_money;
    var $delchk;
    var $year;
    var $subwin;

    //更新で使用する
    var $up_year;
    var $up_schregno;
    var $up_class;
    var $up_subclass;
    var $up_curriculum_cd;
    var $up_rep_class;
    var $up_rep_subclass;
    var $up_rep_curriculum_cd;
    var $up_rep_schoolcd;

    var $select_year;
    var $select_schregno;
    var $select_class;
    var $select_school_kind;
    var $select_subclass;
    var $select_subclass2;
    var $select_curriculum_cd;
    var $select_rep_class;
    var $select_rep_school_kind;
    var $select_rep_subclass;
    var $select_rep_curriculum_cd;
    var $select_rep_former_schoolcd;
    
    var $checked;
    var $mode;

    function knje066Model() {
    }
 
    function init()
    {
        $this->auth = $this->sendAuth ? $this->sendAuth : AUTHORITY;
        $this->cmd = VARS::request("cmd");

        $this->delchk = VARS::request("DELCHK");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("PROGRAMID")) {
            $this->subwin = VARS::request("SUBWIN");
        }

        if (VARS::post("MODE")) {
            $this->mode = VARS::post("MODE");
        }

        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }

        if (VARS::get("SEARCH_DIV")) {
            $this->search_div = VARS::get("SEARCH_DIV");
        }

        //コマンド時の処理
        if ($this->cmd == "reset") {
            $this->select_year                  = $this->up_year;
            $this->select_schregno              = $this->up_schregno;
            $this->select_class                 = $this->up_class;
            $this->select_school_kind           = $this->up_school_kind;
            $this->select_subclass              = $this->up_subclass;
            $this->select_curriculum_cd         = $this->up_curriculum_cd;
            $this->select_rep_class             = $this->up_rep_class;
            $this->select_rep_school_kind       = $this->up_rep_school_kind;
            $this->select_rep_subclass          = $this->up_rep_subclass;
            $this->select_rep_curriculum_cd     = $this->up_rep_curriculum_cd;
            $this->select_rep_former_schoolcd   = $this->up_rep_schoolcd;
            $this->select_all_subclass          = $this->up_all_subclass;
            $this->select_rep_all_subclass      = $this->up_rep_all_subclass;
        } else if ($this->cmd == "edit_src") {
            $this->select_year                = VARS::request("YEAR");
            $this->select_class               = VARS::request("CLASSCD");
            $this->select_school_kind         = VARS::request("SCHOOL_KIND");
            $this->select_curriculum_cd       = VARS::request("CURRICULUM_CD");
            $this->select_subclass            = VARS::request("SUBCLASSCD");
            $this->select_rep_class           = VARS::request("REP_CLASSCD");
            $this->select_rep_school_kind     = VARS::request("REP_SCHOOL_KIND");
            $this->select_rep_curriculum_cd   = VARS::request("REP_CURRICULUM_CD");
            $this->select_rep_subclass        = VARS::request("REP_SUBCLASSCD");
            $this->select_rep_former_schoolcd = VARS::request("REP_SCHOOLCD");
            $this->select_all_subclass = $this->select_subclass;
            $this->select_rep_all_subclass = $this->select_rep_subclass;
        } else if ($this->cmd == "edit") {
            unset($this->up_year);
            unset($this->up_schregno);
            unset($this->up_another);
            unset($this->up_class);
            unset($this->up_subclass);
            unset($this->up_curriculum_cd);
            unset($this->up_rep_class);
            unset($this->up_rep_subclass);
            unset($this->up_rep_curriculum_cd);
            unset($this->up_rep_schoolcd);

            unset($this->select_year);
            unset($this->select_schregno);
            unset($this->select_class);
            unset($this->select_school_kind);
            unset($this->select_subclass);
            unset($this->select_curriculum_cd);
            unset($this->select_rep_class);
            unset($this->select_rep_school_kind);
            unset($this->select_rep_subclass);
            unset($this->select_rep_curriculum_cd);
            unset($this->select_rep_former_schoolcd);
        }

        if (VARS::get("CHECKED")) {
            $a = explode(":", VARS::get("CHECKED"));
            $this->checked = VARS::get("CHECKED");
            $this->up_all_subclass = $a[3];
            $this->select_all_subclass = $a[3];
            list($classcd, $school_kind, $curriculum_cd, $subclasscd)
                    = preg_split("/-/", $a[3]);
            $this->up_rep_all_subclass = $a[4];
            $this->select_rep_all_subclass = $a[4];
            list($rep_classcd, $rep_school_kind, $rep_curriculum_cd, $rep_subclasscd)
                    = preg_split("/-/", $a[4]);
            $this->up_year                 = $a[0];
            $this->up_schregno             = $a[1];
            $this->up_schoolcd             = $a[2];
            $this->up_class                = $classcd."-".$school_kind;
            $this->up_school_kind          = $school_kind;
            $this->up_curriculum_cd        = $curriculum_cd;
            $this->up_subclass             = $classcd."-".$school_kind."-".$curriculum_cd."-".$subclasscd;
            $this->up_rep_class            = $rep_classcd."-".$rep_school_kind;
            $this->up_rep_school_kind      = $rep_school_kind;
            $this->up_rep_curriculum_cd    = $rep_curriculum_cd;
            $this->up_rep_subclass         = $rep_classcd."-".$rep_school_kind."-".$rep_curriculum_cd."-".$rep_subclasscd;
            $this->up_rep_former_schoolcd  = $a[5];

            $this->select_year                  = $this->up_year;
            $this->select_schregno              = $this->up_schregno;
            $this->select_schoolcd              = $this->up_schoolcd;
            $this->select_class                 = $this->up_class;
            $this->select_school_kind           = $this->up_school_kind;
            $this->select_subclass              = $this->up_subclass;
            $this->select_curriculum_cd         = $this->up_curriculum_cd;
            $this->select_rep_class             = $this->up_rep_class;
            $this->select_rep_school_kind       = $this->up_rep_school_kind;
            $this->select_rep_curriculum_cd     = $this->up_rep_curriculum_cd;
            $this->select_rep_subclass          = $this->up_rep_subclass;
            $this->select_rep_former_schoolcd   = $this->up_rep_former_schoolcd;
            unset($this->mode);
        } else {
            $this->checked = "";
        }
        
        if ($this->cmd == "") {
            unset($this->applicantno);
            unset($this->schregno);

            unset($this->up_year);
            unset($this->up_schregno);
            unset($this->up_another);
            unset($this->up_class);
            unset($this->up_subclass);
            unset($this->up_curriculum_cd);
            unset($this->up_rep_class);
            unset($this->up_rep_subclass);
            unset($this->up_rep_curriculum_cd);
            unset($this->up_rep_schoolcd);
            unset($this->up_get_div);

            unset($this->select_year);
            unset($this->select_schregno);
            unset($this->select_schoolcd);
            unset($this->select_class);
            unset($this->select_school_kind);
            unset($this->select_subclass);
            unset($this->select_curriculum_cd);
            unset($this->select_rep_class);
            unset($this->select_rep_school_kind);
            unset($this->select_rep_subclass);
            unset($this->select_rep_curriculum_cd);
            unset($this->select_rep_former_schoolcd);
            unset($this->select_get_div);
            
            unset($this->checked);
        }
        //フィールド
        $this->field = array("YEAR"                  => VARS::post("YEAR"),                    //年度
                             "SCHREGNO"              => VARS::post("SCHREGNO"),             //志願者番号
                             "SCHOOLCD"              => VARS::post("SCHOOLCD"),              //修得方法
                             "CLASSCD"               => VARS::post("CLASSCD"),                 //教科コード
                             "CURRICULUM_CD"         => VARS::post("CURRICULUM_CD"),           //教育課程年度
                             "SUBCLASSCD"            => VARS::post("SUBCLASSCD"),              //科目コード
                             "REP_CLASSCD"           => VARS::post("REP_CLASSCD"),                 //教科コード
                             "REP_CURRICULUM_CD"     => VARS::post("REP_CURRICULUM_CD"),           //教育課程年度
                             "REP_SUBCLASSCD"        => VARS::post("REP_SUBCLASSCD"),              //科目コード
                             "SUBCLASSNAME"          => VARS::post("SUBCLASSNAME"),            //科目名称
                             "SUBCLASSABBV"          => VARS::post("SUBCLASSABBV"),            //科目略称
                             "GET_CREDIT"            => VARS::post("GET_CREDIT"),              //修得単位
                             "COMP_CREDIT"           => VARS::post("COMP_CREDIT"),             //履修単位
                             "VALUATION"             => VARS::post("VALUATION"),               //評定
                             "REP_GET_CREDIT"        => VARS::post("REP_GET_CREDIT"),          //修得単位
                             "REP_COMP_CREDIT"       => VARS::post("REP_COMP_CREDIT"),         //履修単位
                             "REP_VALUATION"         => VARS::post("REP_VALUATION"),           //評定
                             "FORMER_REG_SCHOOLCD"   => VARS::post("FORMER_REG_SCHOOLCD"),     //前籍校コード
                             "REMARK"                => VARS::post("REMARK"),                  //備考
                             "MODE"                  => VARS::post("MODE"),                    //入力方法
                             "GET_DIV"               => VARS::post("GET_DIV"),                 //修得区分
                             "GET_DATE"              => VARS::post("GET_DATE"),                //修得日付
                             "UPDATED"               => VARS::post("UPDATED")                  //更新日付
                            );

        $this->field2 = array("APPLICANTNO"           => VARS::post("APPLICANTNO"),             //志願者番号
                              "SCHREGNO"              => VARS::post("SCHREGNO"),                //学籍番号
                              "EDUCATION_REC_PUT_FLG" => VARS::post("EDUCATION_REC_PUT_FLG"),   //指導要録入力完
                              "UPDATED"               => VARS::post("UPDATED")                  //更新日付
                        );

    }
    
    //入力項目チェック
    function validateInput($kind)
    {
        if ($kind=="update" || $kind=="delete") {
            $db = Query::dbCheckOut();

            $query = knje066Query::getAnotherChk($this);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);

            //キー値が変更されていますかをチェック
            if ($kind=="update") {
                if (!$Row["SCHREGNO"]) $this->cmd = "add";
            } else {
                if ($this->field["YEAR"] != $this->select_year){
                    $this->setWarning('MSG308');
                    return false;
                }
                if ($this->field["SCHREGNO"] != $this->select_schregno){
                    $this->setWarning('MSG308');
                    return false;
                }
                
                if ($this->field["SCHOOLCD"] != $this->select_schoolcd){
                    $this->setWarning('MSG308');
                    return false;
                }
                
                if ($this->field["CURRICULUM_CD"] != $this->select_curriculum_cd){
                    $this->setWarning('MSG308');
                    return false;
                }
                if ($this->field["SUBCLASSCD"] != $this->select_all_subclass){
                    $this->setWarning('MSG308');
                    return false;
                }
            }
        }
        if ($kind != "delete") {
            //空文字チェック
            if ($this->field["MODE"] == "1") {
                $strnotchk = array("YEAR"                => "( 年度 )",
                                   "SCHREGNO"            => "( 学籍番号 )",
                                   "SCHOOLCD"            => "( 修得方法 )",
                                   "CLASSCD"             => "( 教科コード )",
                                   "CURRICULUM_CD"       => "( 教育課程年度 )",
                                   "SUBCLASSCD"          => "( 科目コード )"
                            );
            } else {
                $strnotchk = array("YEAR"                => "( 年度 )",
                                   "SCHREGNO"            => "( 学籍番号 )",
                                   "SCHOOLCD"            => "( 修得方法 )",
                                   "CLASSCD"             => "( 教科コード )",
                                   "CURRICULUM_CD"       => "( 教育課程年度 )",
                                   "SUBCLASSCD"          => "( 科目コード )",
                                   "SUBCLASSNAME"        => "( 科目名称 )",
                                   "SUBCLASSABBV"        => "( 科目略称 )"
                            );
            }
            foreach ($strnotchk as $key => $val) {
                if (strlen($this->field[$key]) <= 0) {
                    $this->setWarning("MSG301",$val); 
                    return false;
                }
            }

            //空文字チェック
            if ($this->field["MODE"] == "1") {
                //査定の教科コード、教育課程年度、科目コードが一つでも選択されている場合は全ての項目で選択必須
                if (strlen($this->field["REP_CLASSCD"].$this->field["REP_CURRICULUM_CD"].$this->field["REP_SUBCLASSCD"]) > 0) {
                    if (strlen($this->field["REP_CLASSCD"]) <= 0) {
                        $this->setWarning("MSG310", "査定教育課程年度、査定教科コード、査定科目コードは全て選択するか全て選択しないかのいずれかです。"); 
                        return false;
                    }
                    if (strlen($this->field["REP_CURRICULUM_CD"]) <= 0) {
                        $this->setWarning("MSG310", "査定教育課程年度、査定教科コード、査定科目コードは全て選択するか全て選択しないかのいずれかです。"); 
                        return false;
                    }
                    if (strlen($this->field["REP_SUBCLASSCD"]) <= 0) {
                        $this->setWarning("MSG310", "査定教育課程年度、査定教科コード、査定科目コードは全て選択するか全て選択しないかのいずれかです。"); 
                        return false;
                    }
                }
            }
            //バイト数チェック
            $strlenchk = array("YEAR"            => array(4,"( 年度 )"),
                               "REMARK"          => array(90,"( 備考 )")
                 );
            
            if ($this->field["MODE"] == "2") {
                $this->field["SUBCLASSCD"] = "99".sprintf("%04d", intval($this->field["SUBCLASSCD"]));
                $strlenchk["SUBCLASSCD"]     = array(6,"( 科目コード )");
                $strlenchk["SUBCLASSNAME"]   = array(90,"( 科目名称 )");
                $strlenchk["SUBCLASSABBV"]   = array(15,"( 科目略称 )");
            }
            
            foreach ($strlenchk as $key => $val) {
                if (strlen($this->field[$key]) > $val[0]) {
                    $this->setWarning("MSG915",$val[1]); return false;
                }
            }    
        }
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        if ($this->validateInput("insert")) {
            $this->Insert();
        }
        $this->setMessage("MSG201");
        return;
    }
    
    function &Insert() {
        knje066Query::getInsertQuery($this);
        $this->select_year                  = $this->field["YEAR"];
        $this->select_schregno              = $this->field["SCHREGNO"];
        $this->select_schoolcd              = $this->field["SCHOOLCD"];

        $subclassAll = $this->field["SUBCLASSCD"];
        if ($model->field["MODE"] == "2") {
            $subclassAll = $this->field["CLASSCD"]."-H-".$this->field["CURRICULUM_CD"]."-".$this->field["SUBCLASSCD"];
        }

        list($classcd, $school_kind, $curriculum_cd, $subclasscd)
                = preg_split("/-/", $subclassAll);

        $this->select_class                 = $classcd;
        $this->select_school_kind           = $school_kind;
        $this->select_curriculum_cd         = $curriculum_cd;
        $this->select_subclass              = $subclasscd;
        $this->select_all_subclass          = $subclassAll;
        $this->select_rep_all_subclass      = $this->field["REP_SUBCLASSCD"];
        $this->select_rep_former_schoolcd   = $this->field["FORMER_REG_SCHOOLCD"];
        if ($this->field["MODE"] == "1") {
            list($classcdSaki, $school_kindSaki, $curriculum_cdSaki, $subclasscdSaki)
                    = preg_split("/-/", $this->field["REP_SUBCLASSCD"]);
            $this->select_rep_class             = $classcdSaki;
            $this->select_rep_school_kind       = $school_kindSaki;
            $this->select_rep_curriculum_cd     = $curriculum_cdSaki;
            $this->select_rep_subclass          = $subclasscdSaki;
        } else {
            $this->select_rep_class             = "0";
            $this->select_rep_school_kind       = "0";
            $this->select_rep_curriculum_cd     = "0";
            $this->select_rep_subclass          = "0";
        }
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput("update")) {
            if ($this->cmd == "add") $this->Insert();
            else if ($this->cmd == "update") knje066Query::getUpdateQuery($this);
            
            $this->checked  = $this->field["YEAR"].":";
            $this->checked .= $this->field["APPLICANTNO"].":";
            $this->checked .= $this->field["SCHOOLCD"].":";
            $this->checked .= $this->field["CLASSCD"].":";
            $this->checked .= $this->field["SUBCLASSCD"].":";
            $this->checked .= $this->field["CURRICULUM_CD"];
            $this->checked .= $this->field["GET_DIV"];
            //査定評定、査定修得単位、査定履修単位、査定日付はレコードが作られない場合は入力内容をNULLとする
            if ($this->field["MODE"] == "1") {
                if ($this->field["REP_SUBCLASSCD"] == "" || $this->field["REP_CURRICULUM_CD"] == "") {
                    $this->field["REP_GET_CREDIT"] = "";
                    $this->field["REP_COMP_CREDIT"] = "";
                    $this->field["REP_VALUATION"] = "";
                    $this->field["GET_DATE"] = "";
                }
            }
        }
        $this->setMessage("MSG201");
        return;
    }
    
    //削除処理
    function &getDeleteModel()
    {
        if ($this->validateInput("delete")) {
            knje066Query::getDeleteQuery($this);
        }
        $this->setMessage("MSG201");
    }
    
    //更新処理
    function &getUpdateTopModel()
    {
        $db = Query::dbCheckOut();
        $query = knje066Query::getSchregChk($this->field2);
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        
        if (intval($Row["CNT"]) > 0) {
            knje066Query::getUpdateTopQuery($this);
            $this->setMessage("MSG201");
        }
        else $this->setWarning("MSG303","( 学籍在籍データ )");
        
        return;
    }

    //削除処理
    function &getDeleteTopModel()
    {
        knje066Query::getDeleteTopQuery($this);
        $this->setMessage("MSG201");
    }
    
    
}
?>
