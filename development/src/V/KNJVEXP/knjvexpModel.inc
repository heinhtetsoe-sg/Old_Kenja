<?php
class knjvexpModel extends Model {
    var $cmd;
    var $programid;         //プログラムＩＤ
    var $target;            //フレーム
    var $usr_auth;
    var $sendAuth;
    var $grade;
    var $check;
    var $search = array();
    var $mode;  //在籍生
    var $button;
    var $ses_flg;//中等教育校フラグ NO001
    var $chiyoda;//NO001
    var $exp_year;
    
    function knjvexpModel()
    {
        $this->mode   = "ungrd";  //在籍生
        $this->chiyoda = knjvexpQuery::GetSchooldiv();//学校区分名称 NO001
    }
    function init()
    {
        if(VARS::request("PROGRAMID")){
            $this->programid = VARS::request("PROGRAMID");
            unset($this->mode);
            unset($this->grade);
            unset($this->search);
            unset($this->exp_year);
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
        }
        //チェックボックス表示
        if(VARS::get("CHECK") == "ON"){
            $this->check[$this->programid] = true;
        }
        /*if ($this->cmd == "search"){
            //検索用
            $this->search = array("GRADE"       => VARS::post("GRADE2"),
                                  "GRD_YEAR"    => VARS::post("GRD_YEAR"),
                                  "HR_CLASS"    => VARS::post("HR_CLASS"),
                                  "NAME"        => VARS::post("NAME"),
                                  "NAME_SHOW"   => VARS::post("NAME_SHOW"),
                                  "NAME_KANA"   => VARS::post("NAME_KANA"),
                                  "NAME_ENG"    => VARS::post("NAME_ENG"),
                                  "SEX"         => VARS::post("SEX"),
                                  "DATE"        => str_replace("/", "-", VARS::post("DATE")),
                                  "COURSECODE"  => VARS::post("COURSECODE")
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
        }*/
        $this->getPropertiesAll(); //プロパティファイル
        
        $this->field = array("GAKUSEKINO"   => VARS::post("GAKUSEKINO"),
                             
                             "GRADE"        => VARS::post("GRADE"),
                             
                             "COURSE"   => VARS::post("COURSE"),
                             
                             "NAME"         => VARS::post("NAME"),
                             
                             "NAME_KANA"    => VARS::post("NAME_KANA"),
                             
                             );
        
    }
}
?>