<?php

require_once('for_php7.php');

class knjxexp4Model extends Model {
    var $cmd;
    var $programid;         //プログラムＩＤ
    var $target;            //フレーム
    var $usr_auth;
    var $sendAuth;
    var $search = array();
    var $button;
    var $firstFlg;
    var $dispData = array();
    var $searchMode;
    var $searchDivExtra;
    var $auth;
    var $hr_class = array();
    var $grade = array();
    var $control = array();        //コントロールのデータを保持

    function knjxexp4Model()
    {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control);
        $this->firstFlg = true;
    }
    function init()
    {
        $this->searchBack = VARS::request("back") ? VARS::request("back") : "";

        if(VARS::request("PROGRAMID")){
            $this->programid = VARS::request("PROGRAMID");
            unset($this->search);
            $this->firstFlg = true;
        }
        $this->searchMode = VARS::request("searchMode");

        //検索表示項目セット
        $this->setDispData();

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

        if ($this->cmd == "search" || $this->cmd == "search2" || $this->cmd == "edit"){
            //検索用
            $this->search = array("ENT_YEAR"                => VARS::post("ENT_YEAR"),
                                  "GRD_YEAR"                => VARS::post("GRD_YEAR"),
                                  "HR_CLASS_TYPE"           => VARS::post("HR_CLASS_TYPE"),
                                  "GAKUNEN_KONGOU"          => VARS::post("GAKUNEN_KONGOU"),
                                  "GRD_CHECK"               => VARS::post("GRD_CHECK"),
                                  "GRADE"                   => VARS::post("GRADE"),
                                  "HR_CLASS"                => VARS::post("HR_CLASS"),
                                  "SEARCH_DIV"              => VARS::post("SEARCH_DIV"),
                                  "SEARCH_TENHEN"           => VARS::post("SEARCH_TENHEN"),
                                  "COURSE_MAJOR"            => VARS::post("COURSE_MAJOR"),
                                  "COURSECODE"              => VARS::post("COURSECODE"),
                                  "SCHREGNO"                => strlen(VARS::post("SCHREGNO")) > 0 ? sprintf("%08d", VARS::post("SCHREGNO")) : "",
                                  "NAME"                    => VARS::post("NAME"),
                                  "NAME_KANA"               => VARS::post("NAME_KANA"),
                                  "A028"                    => VARS::post("A028"),
                                  "PAID_UMU"                => VARS::post("PAID_UMU"),
                                  "HR_CLASS_HYOUJI_FLG"     => VARS::post("HR_CLASS_HYOUJI_FLG")
                                 );
        } else if ($this->cmd == "list"){
            unset($this->search);
        }
        //初期値
        if ($this->cmd == "") {
            $this->search["HR_CLASS_HYOUJI_FLG"] = "1";
            $this->search["GRD_CHECK"] = "1";
        }
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //検索表示項目セット
    function setDispData()
    {
        $dispTitle = array( "search_div", "search_div2", "search_tenhen", "schno", "ent_year", "grd_year", "course_major", "coursecode", "grade", "hr_class",
                            "name", "name_kana", "a028", "paid_umu", "select_grd", "sort", "hrclass_type", "grdCheckBox", "repayDisp", "grdDateDisp"
                             );
        $this->searchDivExtra = "";
        $this->dispData = array();
        foreach ($dispTitle as $key => $val) {
            if (in_array($val, array("schno", "appno", "search_div", "search_div2", "hrclass_type")) && VARS::request($val)) {
                $this->searchDivExtra = " onClick=\" btn_submit('edit')\"";
            }
            $this->dispData[$val] = VARS::request($val);
        }
    }

}
?>
