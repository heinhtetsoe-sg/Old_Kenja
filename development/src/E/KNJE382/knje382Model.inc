<?php

require_once('for_php7.php');

class knje382Model extends Model {
    var $cmd;
    var $field;

    function knje382Model() {}

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

        $this->field = array("SCHOOL_KIND"              =>  VARS::post("SCHOOL_KIND"),
                             "COURSE_KIND"              =>  VARS::post("COURSE_KIND"),
                             "FROM_QUESTIONNAIRECD"     =>  VARS::post("FROM_QUESTIONNAIRECD"),
                             "TO_QUESTIONNAIRECD"       =>  VARS::post("TO_QUESTIONNAIRECD"),
                             "TO_ENTRYDATE"             =>  VARS::post("TO_ENTRYDATE")
                            );

        if ($this->cmd == "execute") {
            if (VARS::post("SHOW_SCHOOL_KIND") == "1") {
                $this->field["SCHOOL_KIND"]         = VARS::post("HIDDEN_SCHOOL_KIND");
            }
            $this->field["COURSE_KIND"]             = VARS::post("HIDDEN_COURSE_KIND");
            $this->field["FROM_QUESTIONNAIRECD"]    = VARS::post("HIDDEN_FROM_QUESTIONNAIRECD");
            $this->field["TO_QUESTIONNAIRECD"]      = VARS::post("HIDDEN_TO_QUESTIONNAIRECD");
            $this->field["TO_ENTRYDATE"]            = VARS::post("HIDDEN_TO_ENTRYDATE");
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->field["SCHOOL_KIND"] == "") {
            $this->setWarning("MSG301","　　（ 校種 ）");
            return false;
        }
        if ($this->field["COURSE_KIND"] == "") {
            $this->setWarning("MSG301","　　（ 進路種別 ）");
            return false;
        }
        if ($this->field["FROM_QUESTIONNAIRECD"] == "") {
            $this->setWarning("MSG301","　　（ コピー元調査名 ）");
            return false;
        }
        if ($this->field["TO_QUESTIONNAIRECD"] == "") {
            $this->setWarning("MSG301","　　（ コピー先調査名 ）");
            return false;
        }
        if ($this->field["TO_ENTRYDATE"] == "") {
            $this->setWarning("MSG301","　　（ コピー先登録日 ）");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $fromCnt = $db->getOne(knje382Query::getCourseHopeDat($this, 'cnt'));
        Query::dbCheckIn($db);
        if (!$fromCnt) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    //実行モデルを返します
    function &getExecModel() {
        if ($this->validateInput()) {
            //実行履歴作成
            knje382Query::executeRireki($this);

            //コピー
            knje382Query::ExecuteQuery($this);
            $this->setMessage("MSG202");
        }
    }
}
?>
