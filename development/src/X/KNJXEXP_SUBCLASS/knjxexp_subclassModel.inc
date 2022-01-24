<?php
class knjxexp_subclassModel extends Model
{
    public $cmd;
    public $programid;         //プログラムＩＤ
    public $target;            //フレーム
    public $usr_auth;
    public $sendAuth;
    public $search = array();
//    var $button;
    public $firstFlg;
    public $dispData = array();
    public $searchMode;
//    var $searchDivExtra;
    public $auth;
    public $hr_class = array();
    public $grade = array();
    public $control = array();        //コントロールのデータを保持

    public $resultcnt;

    public function knjxexp_subclassModel()
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

        if (VARS::post("RESULTCNT") != "") {
            $model->resultcnt = VARS::post("RESULTCNT");
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
//        if ($this->cmd == "search" || $this->cmd == "search2" || $this->cmd == "edit"){
            //検索用
            $this->search = array("YEAR"       => CTRL_YEAR,
                                  "SEMESTER"   => CTRL_SEMESTER,
                                  "SUBCLASS"   => VARS::post("SUBCLASS"),
                                  "CHAIR"      => VARS::post("CHAIR"),
                                  "TESTTYPE"   => VARS::post("TESTTYPE"),
                                  "REQ_FLG"    => VARS::post("REQ_FLG"),
                                  "RESULTCNT"  => VARS::post("RESULTCNT")
                                 );
//        } else if ($this->cmd == "list"){
//            unset($this->search);
//        }
        //初期値
            if ($this->cmd == "") {
                $this->search["HR_CLASS_HYOUJI_FLG"] = "1";
            }
            $this->getPropertiesAll(); //プロパティファイル
            if ($this->Properties["use_prg_schoolkind"] == "1") {
                $this->getSelectSchoolKind();
            }
    }

    //検索表示項目セット
    public function setDispData()
    {
        $dispTitle = array( "semester", "subclass", "chair", "testtype", "req_flg", "resultcnt", "search");
//        $this->searchDivExtra = "";
        $this->dispData = array();
        foreach ($dispTitle as $key => $val) {
//            if (in_array($val, array("schno", "appno", "search_div")) && VARS::request($val)) {
//                $this->searchDivExtra = " onClick=\" btn_submit('edit')\"";
//            }
            $this->dispData[$val] = VARS::request($val);
        }
    }
}
