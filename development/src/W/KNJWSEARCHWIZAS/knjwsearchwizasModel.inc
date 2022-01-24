<?php

require_once('for_php7.php');

class knjwsearchwizasModel extends Model {
    var $cmd;
    var $programid;         //プログラムＩＤ
    var $target;            //フレーム
    var $search = array();
    var $button;
    var $firstFlg;
    var $dispData = array();
    var $searchMode;
    var $searchDivExtra;
    var $auth;
    var $belonging = array();
    var $hr_class = array();
    var $grade = array();
    var $control = array();        //コントロールのデータを保持

    function knjwsearchwizasModel()
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

        if (VARS::get("AUTH")) {
            //DB接続
            $db     = Query::dbCheckOut();

            $this->auth = VARS::get("AUTH");
            $this->belonging = $db->getRow(knjwsearchwizasQuery::getStaff(), DB_FETCHMODE_ASSOC);
            
            $this->hr_class = array();
            $this->grade = array();
            $result = $db->query(knjwsearchwizasQuery::getHrclass());
            while ( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->hr_class[] = $row["VALUE"];
                list($grade, $hr_class) = preg_split("/-/", $row["VALUE"]);
                $this->grade[] = $grade;
            }
            $result->free();

            //DB切断
            Query::dbCheckIn($db);
        }

        //検索表示項目セット
        $this->setDispData();

        if(VARS::get("TARGET")){
            $this->target[$this->programid] = VARS::get("TARGET");
        }

        if(VARS::get("PATH")){
            $this->path[$this->programid] = VARS::get("PATH");
        }

        $this->cmd          = VARS::request("cmd");

        if ($this->cmd == "search" || $this->cmd == "search2" || $this->cmd == "edit"){
            //検索用
            $this->search = array("EXE_YEAR"                => VARS::post("EXE_YEAR"),
                                  "YEAR"                    => VARS::post("YEAR"),
                                  "REC_GET_FLG"             => VARS::post("REC_GET_FLG"),
                                  "GRD_DIV"                 => VARS::post("GRD_DIV"),
                                  "SEARCH_DIV"              => VARS::post("SEARCH_DIV"),
                                  "SEARCH_ACCOUNT"          => VARS::post("SEARCH_ACCOUNT"),
                                  "VIRTUAL_BANK_CD"         => VARS::post("VIRTUAL_BANK_CD"),
                                  "F_VIRTUAL_ACCOUNT_NO"    => VARS::post("F_VIRTUAL_ACCOUNT_NO"),
                                  "T_VIRTUAL_ACCOUNT_NO"    => VARS::post("T_VIRTUAL_ACCOUNT_NO"),
                                  "APPLICANT_DIV"           => VARS::post("APPLICANT_DIV"),
                                  "APPLICANT"               => VARS::post("APPLICANT"),
                                  "PROCEDURE_DIV"           => VARS::post("PROCEDURE_DIV"),
                                  "SCHREGNO"                => strlen(VARS::post("SCHREGNO")) > 0 ? sprintf("%08d", VARS::post("SCHREGNO")) : "",
                                  "SCHREGNO1"               => strlen(VARS::post("SCHREGNO1")) > 0 ? sprintf("%08d", VARS::post("SCHREGNO1")) : "",
                                  "SCHREGNO2"               => strlen(VARS::post("SCHREGNO2")) > 0 ? sprintf("%08d", VARS::post("SCHREGNO2")) : "",
                                  "APPLICANTNO"             => strlen(VARS::post("APPLICANTNO")) > 0 ? sprintf("%07d", VARS::post("APPLICANTNO")) : "",
                                  "MANNER_PAYMENT"          => VARS::post("MANNER_PAYMENT"),
                                  "SLIP_NO"                 => VARS::post("SLIP_NO"),
                                  "NAME"                    => VARS::post("NAME"),
                                  "NAME_KANA"               => VARS::post("NAME_KANA"),
                                  "SEX"                     => VARS::post("SEX"),
                                  "MISYUKIN"                => VARS::post("MISYUKIN"),
                                  "BELONGING_DIV"           => VARS::post("BELONGING_DIV"),
                                  "STUDENT_DIV"             => VARS::post("STUDENT_DIV"),
                                  "COURSE"                  => VARS::post("COURSE"),
                                  "ANNUAL"                  => VARS::post("ANNUAL"),
                                  "GRD_DATE"                => VARS::post("GRD_DATE"),
                                  "GRD_MONTH"               => VARS::post("GRD_MONTH"),
                                  "GRD_SHOW"                => VARS::post("GRD_SHOW"),
                                  "GRD_REQUIRE"             => VARS::post("GRD_REQUIRE"),
                                  "TEIKEI"                  => VARS::post("TEIKEI"),
                                  "SUBCLASS"                => VARS::post("SUBCLASS"),
                                  "REPORT"                  => VARS::post("REPORT"),
                                  "INPORT"                  => VARS::post("INPORT"),
                                  "SCHOOLING_DIV"           => VARS::post("SCHOOLING_DIV"),
                                  "ATTEND_INFO"             => VARS::post("ATTEND_INFO"),
                                  "ENT_APP_DIV"             => VARS::post("ENT_APP_DIV"),
                                  "PAYMENT_MONEY"           => VARS::post("PAYMENT_MONEY"));
        } else if ($this->cmd == "list"){
            unset($this->search);
        }
    }

    //検索表示項目セット
    function setDispData()
    {
        $dispTitle = array( "search_div", "exe_year", "year", "grddiv", "schorzai", "schno", "appno",
                            "slip", "name", "name_kana", "sex", "misyukin", "belong", "studiv", "course", "teikei",
                            "subclass", "report", "inport", "mannerpayment", "serch1", "serch2", "annual",
                            "grddate", "grdmonth", "appdiv", "appdiv2", "procedure", "virtual_account",
                            "branch", "account", "grd_require", "schooling", "attend_info", "schno_range",
                            "ent_app_div", "rec_get_flg", "paymentmoney", "grdshow"
                             );
        $this->searchDivExtra = "";
        foreach ($dispTitle as $key => $val) {
            if (in_array($val, array("schno", "appno", "search_div")) && VARS::request($val)) {
                $this->searchDivExtra = " onClick=\" btn_submit('edit')\"";
            }
            $this->dispData[$val] = VARS::request($val);
        }
    }

}
?>
