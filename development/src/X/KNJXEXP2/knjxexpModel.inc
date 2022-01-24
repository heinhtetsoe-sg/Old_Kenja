<?php

require_once('for_php7.php');

class knjxexpModel extends Model
{
    public $cmd;
    public $programid;         //プログラムＩＤ
    public $target;            //フレーム
    public $usr_auth;
    public $grade;
    public $yearseme;
    public $year;
    public $seme;
    public $check;
    public $search = array();
    public $mode;  //在籍生
    public $button;

    public function knjxexpModel()
    {
        $this->mode   = "ungrd";  //在籍生
    }
    public function init()
    {
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if (VARS::request("PROGRAMID")) {
            $this->programid = VARS::request("PROGRAMID");
            unset($this->mode);
            unset($this->grade);
            unset($this->yearseme);
            unset($this->year);
            unset($this->seme);
            unset($this->search);
        }
        if (VARS::request("cmd") == "") {
            $this->warning = "指導要録を元に、確認してから\\n入力して下さい";
        }
        if (VARS::get("TARGET")) {
            $this->target[$this->programid] = VARS::get("TARGET");
        }
        if (VARS::get("PATH")) {
            $this->path[$this->programid] = VARS::get("PATH");
        }
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        $this->cmd          = VARS::request("cmd");

        if (VARS::post("GRADE") && VARS::request("cmd") != "chg_year") {
            $this->grade    = VARS::post("GRADE");
        }
        if (VARS::post("CTRL_YEAR")) {
            $this->yearseme    = VARS::post("CTRL_YEAR");
            $this->year        = SUBSTR($this->yearseme, 0, 4);
            $this->seme        = SUBSTR($this->yearseme, 5);
        }
        //チェックボックス表示
        if (VARS::get("CHECK") == "ON") {
            $this->check[$this->programid] = true;
        }
        if ($this->cmd == "search") {
            //検索用
            $this->search = array("GRADE"       => VARS::post("GRADE2"),
                                  "GRD_YEAR"    => VARS::post("GRD_YEAR"),
                                  "HR_CLASS"    => VARS::post("HR_CLASS"),
                                  "NAME"        => VARS::post("NAME"),
                                  "NAME_SHOW"   => VARS::post("NAME_SHOW"),
                                  "NAME_KANA"   => VARS::post("NAME_KANA"),
                                  "NAME_ENG"    => VARS::post("NAME_ENG"),
                                  "SEX"         => VARS::post("SEX"),
                                  "COURSECODE"  => VARS::post("COURSECODE")
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
    }
}
