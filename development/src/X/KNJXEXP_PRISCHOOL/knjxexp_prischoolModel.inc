<?php

require_once('for_php7.php');

class knjxexp_prischoolModel extends Model {
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
    var $hr_class = array();
    var $grade = array();
    var $control = array();        //コントロールのデータを保持

    function knjxexp_prischoolModel()
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

        $this->cmd          = VARS::request("cmd");

        if ($this->cmd == "search" || $this->cmd == "search2" || $this->cmd == "priChange" || $this->cmd == "searchUpd" || $this->cmd == "edit"){
            //検索用
            $this->search = array("PRISCHOOLCD"             => VARS::post("PRISCHOOLCD"),
                                  "PRISCHOOL_NAME"          => VARS::post("PRISCHOOL_NAME"),
                                  "PRISCHOOL_KANA"          => VARS::post("PRISCHOOL_KANA"),
                                  "PRISCHOOL_CLASS_NAME"    => VARS::post("PRISCHOOL_CLASS_NAME"),
                                  "PRISCHOOL_CLASS_KANA"    => VARS::post("PRISCHOOL_CLASS_KANA"),
                                  "ROSEN_NAME"              => VARS::post("ROSEN_NAME"),
                                  "NEAREST_STATION_NAME"    => VARS::post("NEAREST_STATION_NAME"),
                                  "DISTRICTCD"              => VARS::post("DISTRICTCD")
                                 );
        } else if ($this->cmd == "list"){
            unset($this->search);
        }
    }

    //検索表示項目セット
    function setDispData()
    {

        $dispTitle = array("prischoolcd", "prischool_name", "prischool_kana", "prischool_class_name",
                           "prischool_class_kana", "rosen_name", "nearest_station_name", "districtcd"
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
