<?php

require_once('for_php7.php');

class knjxsearch5Model extends Model {
    var $cmd;
    var $programid;         //プログラムＩＤ
    var $target;            //フレーム
    var $usr_auth;
    var $left_grade;
    var $check;
    var $search = array();
    var $mode;      //在籍生
    var $button;
    var $ses_flg;   //中等教育校フラグ
    var $chiyoda;
    var $firstFlg;

    function knjxsearch5Model()
    {
        $this->mode    = "ungrd";  //在籍生
        $this->chiyoda = knjxsearch5Query::GetSchooldiv();//学校区分名称
        $this->firstFlg = true;
    }
    function init()
    {
        if(VARS::request("PROGRAMID")){
            $this->programid = VARS::request("PROGRAMID");
            unset($this->mode);
            unset($this->left_grade);
            unset($this->search);
            $this->firstFlg = true;
        }
        //中等教育校フラグ
        if(VARS::get("SES_FLG")){
            $this->ses_flg[$this->programid] = VARS::get("SES_FLG");
        }
        if(VARS::get("TARGET")){
            $this->target[$this->programid] = VARS::get("TARGET");
        }
        if(VARS::get("PATH")){
            $this->path[$this->programid] = VARS::get("PATH");
        }
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        $this->cmd          = VARS::request("cmd");
        if (VARS::post("LEFT_GRADE")) {
            $this->left_grade    = VARS::post("LEFT_GRADE");
        }
        //チェックボックス表示
        if(VARS::get("CHECK") == "ON"){
            $this->check[$this->programid] = true;
        }
        if ($this->cmd == "search" || $this->cmd == "right"){
            //検索用
            $this->search = array("GRADE"       => VARS::post("GRADE"),
                                  "COURSECODE"  => VARS::post("COURSECODE"),
                                  "CLUBCD"      => VARS::post("CLUBCD"),
                                  "CHAIRCD"     => VARS::post("CHAIRCD"),
                                  "HR_CLASS"    => VARS::post("HR_CLASS"),
                                  "NAME"        => VARS::post("NAME"),
                                  "NAME_SHOW"   => VARS::post("NAME_SHOW"),
                                  "NAME_KANA"   => VARS::post("NAME_KANA"),
                                  "NAME_ENG"    => VARS::post("NAME_ENG"),
                                  "KEYWORD"     => VARS::post("KEYWORD"),
                                  "ACTIONDATE"  => VARS::post("ACTIONDATE"),
                                  //卒業用
                                  "GRD_YEAR"    => VARS::post("GRD_YEAR")
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

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
