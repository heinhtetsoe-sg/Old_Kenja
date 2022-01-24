<?php

require_once('for_php7.php');

class knjlEventSearchModel extends Model
{
    public $cmd;
    public $programid;         //プログラムＩＤ
    public $target;            //フレーム
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

    public function knjlEventSearchModel()
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

            $this->name         = VARS::get("SEND_NAME");
            $this->kana         = VARS::get("SEND_KANA");
            $this->finschoolcd  = VARS::get("SEND_FINSCHOOLCD");
        }

        if (VARS::get("PATH")) {
            $this->path[$this->programid] = VARS::get("PATH");
        }

        $this->cmd          = VARS::request("cmd");

        if ($this->cmd == "search" || $this->cmd == "search2" || $this->cmd == "priChange" || $this->cmd == "searchUpd" || $this->cmd == "edit") {
            //検索用
            $this->search = array("SCHOOL_KIND"         => VARS::post("SCHOOL_KIND"),
                                  "RECRUIT_NO"          => VARS::post("RECRUIT_NO"),
                                  "NAME"                => VARS::post("NAME"),
                                  "NAME_KANA"           => VARS::post("NAME_KANA"),
                                  "EVENT_CLASS_CD"      => VARS::post("EVENT_CLASS_CD"),
                                  "EVENT_CD"            => VARS::post("EVENT_CD"),
                                  "MEDIA_CD"            => VARS::post("MEDIA_CD"),
                                  "FINSCHOOLCD"         => VARS::post("FINSCHOOLCD"),
                                  "GRADE"               => VARS::post("GRADE"),
                                  "PRISCHOOLCD"         => VARS::post("PRISCHOOLCD"),
                                  "PRISCHOOL_CLASS_CD"  => VARS::post("PRISCHOOL_CLASS_CD")
                                 );
        } elseif ($this->cmd == "list") {
            unset($this->search);
        }
    }

    //検索表示項目セット
    public function setDispData()
    {
        $dispTitle = array( "recruit_no", "schoolKind", "name", "name_kana", "event_cd", "event_class_cd", "media_cd",
                            "finschoolcd", "grade", "prischoolcd", "prischoolClassCd"
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
