<?php

require_once('for_php7.php');

class knjxexp5Model extends Model {
    var $cmd;
    var $programid;         //プログラムＩＤ
    var $target;            //フレーム
    var $usr_auth;
    var $sendAuth;
    var $grade;
    var $diagtype;
    var $check;
    var $search = array();
    var $mode;  //在籍生
    var $button;
    var $ses_flg;//中等教育校フラグ NO001
    var $chiyoda;//NO001
    var $exp_year;
    var $school_kind;
    var $loadFrame;

    function knjxexp5Model()
    {
        $this->mode   = "ungrd";  //在籍生
    }
    function init()
    {
        if(VARS::request("PROGRAMID")){
            $this->programid = VARS::request("PROGRAMID");
            unset($this->mode);
            unset($this->grade);
            unset($this->grade_hrclass);
            unset($this->search);
            unset($this->exp_year);
            unset($this->searchGrade);
            unset($this->searchGradeHr);
        }
        if(VARS::get("PROGRAMID")){
            unset($this->school_kind);
            unset($this->unUseSchoolkind);
            unset($this->sendAuth);
            unset($this->diagtype);
            unset($this->loadFrame);
        }
        //年度選択コンボ
        if (VARS::post("EXP_YEAR")) {
            $this->exp_year    = VARS::post("EXP_YEAR");
        }
        //中等教育校フラグ NO001
        if(VARS::get("SES_FLG")){
            $this->ses_flg[$this->programid] = VARS::get("SES_FLG");
        }
        if(VARS::get("TARGET")){
            $this->target[$this->programid] = VARS::get("TARGET");
        }
        if(VARS::get("PATH")){
            $this->path[$this->programid] = VARS::get("PATH");
        }
        if(strlen($this->sendAuth) == 0){
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        if (strlen($this->sendAuth) > 0) {
            $this->usr_auth = $this->sendAuth;
        }
        $this->cmd          = VARS::request("cmd");
        if (VARS::post("GRADE")) {
            $this->grade    = VARS::post("GRADE");
        } else if(VARS::get("GRADE")) {
            $this->grade    = VARS::get("GRADE");
        }
        if (VARS::post("GRADE_HR_CLASS")) {
            $this->grade_hrclass    = VARS::post("GRADE_HR_CLASS");
        } else if(VARS::get("GRADE_HR_CLASS")) {
            $this->grade_hrclass    = VARS::get("GRADE_HR_CLASS");
        }

        if (VARS::request("GRADE2")) {
            $this->searchGrade = VARS::request("GRADE2");
        }
        if (VARS::request("GRADE_HR_CLASS2")) {
            $this->searchGradeHr = VARS::request("GRADE_HR_CLASS2");
        }
        if ($this->cmd == 'list') {
            $this->searchGrade   = $this->grade;
            $this->searchGradeHr = $this->grade_hrclass;
        }

        if (VARS::post("DIAGTYPE")) {
            $this->diagtype    = VARS::post("DIAGTYPE");
        } else if (VARS::get("DIAGTYPE")) {
            $this->diagtype    = VARS::get("DIAGTYPE");
        }
        if (VARS::post("LOADFRM")) {
            $this->loadFrame    = VARS::post("LOADFRM");
        } else if (VARS::get("LOADFRM")) {
            $this->loadFrame    = VARS::get("LOADFRM");
        }
        //チェックボックス表示
        if(VARS::get("CHECK") == "ON"){
            $this->check[$this->programid] = true;
        }
        if ($this->cmd == "search" || $this->cmd == "right"){
            //検索用
            $this->search = array("GRADE"           => VARS::post("GRADE2"),
                                  "GRADE_HR_CLASS"  => VARS::post("GRADE_HR_CLASS2"),
                                  "GRD_YEAR"        => VARS::post("GRD_YEAR"),
                                  "HR_CLASS"        => VARS::post("HR_CLASS"),
                                  "SRCH_SCHREGNO"   => VARS::post("SRCH_SCHREGNO"),
                                  "NAME"            => VARS::post("NAME"),
                                  "NAME_SHOW"       => VARS::post("NAME_SHOW"),
                                  "NAME_KANA"       => VARS::post("NAME_KANA"),
                                  "NAME_ENG"        => VARS::post("NAME_ENG"),
                                  "SEX"             => VARS::post("SEX"),
                                  "DATE"            => str_replace("/", "-", VARS::post("DATE")),
                                  "COURSECODE"      => VARS::post("COURSECODE")
                                 );
            $this->search["HR_CLASS"] = ($this->search["HR_CLASS"] != "") ? sprintf("%03s", $this->search["HR_CLASS"]) : "";
        }else if ($this->cmd == "list"){
            unset($this->search);
        }
        //検索モード
        if(VARS::request("mode")){
            $this->mode = VARS::request("mode");
        }
        //検索ボタン
        if(VARS::get("button")){
            $this->button[$this->programid] = VARS::get("button");
            if ($this->button[$this->programid] == 2){  //卒業ボタン
                $this->mode   = "grd";    //卒業
            }else{
                $this->mode   = "ungrd";  //在籍生
            }
        }
        if (!$this->mode){
            $this->mode   = "ungrd";  //在籍生
        }

        $this->getPropertiesAll(); //プロパティファイル

        //校種
        if (VARS::get("SCHOOL_KIND")) {
            $this->school_kind = VARS::get("SCHOOL_KIND");
        } else if (VARS::get("unUseSchoolkind") == "1" || $this->unUseSchoolkind == "1") {
            $this->school_kind = '';
            $this->unUseSchoolkind = 1;
        } else if ($this->programid == "KNJA125J") {
            $this->school_kind = 'J';
        } else if ($this->programid == "KNJA120A") {
            $this->school_kind = 'H';
        } else if ($this->Properties["useSchool_KindField"] == "1") {
            $this->school_kind = $this->school_kind ? $this->school_kind : SCHOOLKIND;
        }
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if (VARS::get("grdGrade")) {
            $this->grdGrade = VARS::get("grdGrade");
        }
    }
}
?>
