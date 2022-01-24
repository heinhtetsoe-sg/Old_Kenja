<?php

require_once('for_php7.php');

class knjxexpModel extends Model
{
    public $cmd;
    public $programid;         //プログラムＩＤ
    public $target;            //フレーム
    public $usr_auth;
    public $sendAuth;
    public $grade;
    public $check;
    public $search = array();
    public $mode;  //在籍生
    public $button;
    public $ses_flg;//中等教育校フラグ NO001
    public $chiyoda;//NO001
    public $exp_year;
    public $school_kind;

    public function knjxexpModel()
    {
        $this->mode   = "ungrd";  //在籍生
    }
    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->programid = VARS::request("PROGRAMID");
            unset($this->mode);
            unset($this->grade);
            unset($this->search);
            unset($this->exp_year);
        }
        if (VARS::get("PROGRAMID")) {
            unset($this->school_kind);
            unset($this->unUseSchoolkind);
            unset($this->sendAuth);
        }
        //年度選択コンボ
        if (VARS::post("EXP_YEAR")) {
            $this->exp_year    = VARS::post("EXP_YEAR");
        }
        //中等教育校フラグ NO001
        if (VARS::get("SES_FLG")) {
            $this->ses_flg[$this->programid] = VARS::get("SES_FLG");
        }
        if (VARS::get("TARGET")) {
            $this->target[$this->programid] = VARS::get("TARGET");
        }
        if (VARS::get("PATH")) {
            $this->path[$this->programid] = VARS::get("PATH");
        }
        if (strlen($this->sendAuth) == 0) {
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        if (strlen($this->sendAuth) > 0) {
            $this->usr_auth = $this->sendAuth;
        }
        $this->cmd          = VARS::request("cmd");
        if (VARS::post("GRADE")) {
            $this->grade    = VARS::post("GRADE");
        } elseif (VARS::get("GRADE")) {
            $this->grade    = VARS::get("GRADE");
        }
        //チェックボックス表示
        if (VARS::get("CHECK") == "ON") {
            $this->check[$this->programid] = true;
        }
        if ($this->cmd == "search" || $this->cmd == "right") {
            //検索用
            $this->search = array("GRADE"           => VARS::post("GRADE2"),
                                  "GRD_YEAR"        => VARS::post("GRD_YEAR"),
                                  "HR_CLASS"        => VARS::post("HR_CLASS"),
                                  "SRCH_SCHREGNO"   => VARS::post("SRCH_SCHREGNO"),
                                  "NAME"            => VARS::post("NAME"),
                                  "NAME_SHOW"       => VARS::post("NAME_SHOW"),
                                  "NAME_KANA"       => VARS::post("NAME_KANA"),
                                  "NAME_ENG"        => VARS::post("NAME_ENG"),
                                  "SEX"             => VARS::post("SEX"),
                                  "DATE"            => str_replace("/", "-", VARS::post("DATE")),
                                  "COURSECODE"      => VARS::post("COURSECODE"),
                                  "ENT_DIV"         => VARS::post("ENT_DIV"),
                                  "GRD_DIV"         => VARS::post("GRD_DIV"),
                                  "TRANSFERCD"      => VARS::post("TRANSFERCD")
                                 );
            $this->search["HR_CLASS"] = ($this->search["HR_CLASS"] != "") ? sprintf("%03s", $this->search["HR_CLASS"]) : "";
        } elseif ($this->cmd == "list") {
            unset($this->search);
        }
        //検索モード
        if (VARS::request("mode")) {
            $this->mode = VARS::request("mode");
        }
        //検索ボタン
        if (VARS::get("button")) {
            $this->button[$this->programid] = VARS::get("button");
            if ($this->button[$this->programid] == 2) {  //卒業ボタン
                $this->mode   = "grd";    //卒業
            } else {
                $this->mode   = "ungrd";  //在籍生
            }
        }
        if (!$this->mode) {
            $this->mode   = "ungrd";  //在籍生
        }

        $this->getPropertiesAll(); //プロパティファイル

        //校種
        if (VARS::get("SCHOOL_KIND")) {
            $this->school_kind = VARS::get("SCHOOL_KIND");
        } elseif (VARS::get("unUseSchoolkind") == "1" || $this->unUseSchoolkind == "1") {
            $this->school_kind = '';
            $this->unUseSchoolkind = 1;
        } elseif ($this->programid == "KNJA125J") {
            $this->school_kind = 'J';
        } elseif ($this->programid == "KNJA120A") {
            $this->school_kind = 'H';
        } elseif ($this->Properties["useSchool_KindField"] == "1") {
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
