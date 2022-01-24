<?php

require_once('for_php7.php');

class knje383Model extends Model {
    var $cmd;
    var $field;

    function knje383Model() {}

    function init() {
        //初期化
        if (VARS::get("PROGRAMID") != "") {
            $this->field = array();
        }

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SCHOOL_KIND"          =>  VARS::post("SCHOOL_KIND"),
                             "COURSE_KIND"          =>  VARS::post("COURSE_KIND"),
                             "QUESTIONNAIRECD"      =>  VARS::post("QUESTIONNAIRECD"),
                             "ENTRYDATE"            =>  VARS::post("ENTRYDATE")
                            );

        if ($this->cmd == "execute") {
            if (VARS::post("SHOW_SCHOOL_KIND") == "1") {
                $this->field["SCHOOL_KIND"]     = VARS::post("HIDDEN_SCHOOL_KIND");
            }
            $this->field["QUESTIONNAIRECD"]     = VARS::post("HIDDEN_QUESTIONNAIRECD");
            $this->field["ENTRYDATE"]           = VARS::post("HIDDEN_ENTRYDATE");
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->field["SCHOOL_KIND"] == "") {
            $this->setWarning("MSG301","　　（ 校種 ）");
            return false;
        }
        if ($this->field["QUESTIONNAIRECD"] == "") {
            $this->setWarning("MSG301","　　（ 調査名 ）");
            return false;
        }
        if ($this->field["ENTRYDATE"] == "") {
            $this->setWarning("MSG301","　　（ 登録日 ）");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $Cnt = $db->getOne(knje383Query::getCourseHopeDat($this, 'cnt'));
        Query::dbCheckIn($db);
        if (!$Cnt) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    //実行モデルを返します
    function &getExecModel() {
        if ($this->validateInput()) {
            //実行履歴作成
            knje383Query::executeRireki($this);

            //コピー
            knje383Query::ExecuteQuery($this);
            $this->setMessage("MSG202");
        }
    }
}
?>
