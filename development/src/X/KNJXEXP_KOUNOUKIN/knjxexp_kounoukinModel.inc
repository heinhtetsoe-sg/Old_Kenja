<?php

require_once('for_php7.php');

class knjxexp_kounoukinModel extends Model
{
    public $cmd;
    public $programid;         //プログラムＩＤ
    public $target;            //フレーム
    public $usr_auth;
    public $sendAuth;
    public $search = array();
    public $button;
    public $firstFlg;
    public $dispData = array();
    public $searchMode;
    public $searchDivExtra;
    public $auth;
    public $hr_class = array();
    public $grade = array();
    public $control = array();        //コントロールのデータを保持

    public function knjxexp_kounoukinModel()
    {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control);
        $this->firstFlg = true;
    }
    public function init()
    {
        $this->searchBack = VARS::request("back") ? VARS::request("back") : "";

        if (VARS::request("PROGRAMID")) {
            $this->programid = VARS::request("PROGRAMID");
            unset($this->search);
            $this->firstFlg = true;
        }
        $this->searchMode = VARS::request("searchMode");

        //検索表示項目セット
        $this->setDispData();

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

        //親画面から送られてきた検索条件
        if (VARS::request("SEND_SCHREGNO")) {
            $this->sendSchregno = VARS::get("SEND_SCHREGNO");
        }

        $this->cmd          = VARS::request("cmd");

        if ($this->cmd == "search" || $this->cmd == "search2" || $this->cmd == "edit") {
            //検索用
            $this->search = array("ENT_YEAR"                => VARS::post("ENT_YEAR"),
                                  "GRD_YEAR"                => VARS::post("GRD_YEAR"),
                                  "HR_CLASS_TYPE"           => VARS::post("HR_CLASS_TYPE"),
                                  "GAKUNEN_KONGOU"          => VARS::post("GAKUNEN_KONGOU"),
                                  "GRD_CHECK"               => VARS::post("GRD_CHECK"),
                                  "SCHOOL_KIND"             => VARS::post("SCHOOL_KIND"),
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

            if ($this->dispData["search_div_KNJP850"] == "1") {
                $this->search["CONTACT_EXISTS_CHK"]     = VARS::post("CONTACT_EXISTS_CHK");
                $this->search["EXCEPT_CONTACT_END_CHK"] = VARS::post("EXCEPT_CONTACT_END_CHK");
            }
        } elseif ($this->cmd == "list") {
            unset($this->search);
        }
        //初期値
        if ($this->cmd == "") {
            $this->search["HR_CLASS_HYOUJI_FLG"] = "1";
            $this->search["GRD_CHECK"] = "";
        }
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //検索表示項目セット
    public function setDispData()
    {
        $dispTitle = array();
        $dispTitle[] = "search_div";
        $dispTitle[] = "search_div2";
        $dispTitle[] = "search_tenhen";
        $dispTitle[] = "schno";
        $dispTitle[] = "ent_year";
        $dispTitle[] = "grd_year";
        $dispTitle[] = "course_major";
        $dispTitle[] = "coursecode";
        $dispTitle[] = "schkind";
        $dispTitle[] = "grade";
        $dispTitle[] = "hr_class";
        $dispTitle[] = "name";
        $dispTitle[] = "name_kana";
        $dispTitle[] = "a028";
        $dispTitle[] = "paid_umu";
        $dispTitle[] = "select_grd";
        $dispTitle[] = "sort";
        $dispTitle[] = "hrclass_type";
        $dispTitle[] = "grdCheckBox";
        $dispTitle[] = "repayDisp";
        $dispTitle[] = "grdDateDisp";
        $dispTitle[] = "search_div_KNJP850";

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
