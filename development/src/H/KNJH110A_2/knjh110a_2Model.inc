<?php

require_once('for_php7.php');

class knjh110a_2Model extends Model {

    var $field = array();
    var $cmd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限
    var $schregNo;      //親画面の学籍番号
    var $subclassCd;    //親画面の科目コード
    var $classCd;       //親画面の教科コード ※教育課程用
    var $cut_subclassCd;//親画面の科目コード ※教育課程用
    var $seq;           //子画面のSEQ
    var $grade;         //親画面の学年

    function knjh110a_2Model() {}

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);

            unset($this->schregNo);
            unset($this->subclassCd);
            
            //※教育課程用
            unset($this->classCd);
            unset($this->curriculum_Cd);
            unset($this->useCurriculum_subclassCd);
            unset($this->first_set_Cd);
            
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->getPropertiesAll(); //プロパティファイル
        
        //親画面
        if (VARS::get("SEND_SCHREGNO")) {
            $this->schregNo     = VARS::get("SEND_SCHREGNO");
            //教育課程用
            if ($this->Properties["useCurriculumcd"] == '1') {
                unset($this->subclassCd);
                $this->classCd                  = VARS::get("SEND_CLASSCD");
                $this->useCurriculum_subclassCd = VARS::get("SEND_SUBCLASSCD");
                $this->curriculum_Cd            = VARS::get("SEND_CURRICULUM_CD");
                $this->first_set_Cd = $this->classCd.'-'.$this->schoolkind.'-'.$this->curriculum_Cd.'-'.$this->useCurriculum_subclassCd;
            } else {
                $this->subclassCd   = VARS::get("SEND_SUBCLASSCD");
            }
            unset($this->seq);
        }
        //学年を親画面から取得
        if (VARS::get("SEND_GRADE")) {
            $this->getGrade = VARS::get("SEND_GRADE");
        }
        //子画面（リンクから取得）
        if (VARS::get("SEQ")) {
            $this->seq          = VARS::get("SEQ");
        }
        //入力項目
        $this->field = array("REGDDATE"     => VARS::post("REGDDATE"),
                             "MINUTES"      => VARS::post("MINUTES"),
                             "REMARK"       => VARS::post("REMARK")
                             );
        //子画面（科目コンボの変更時）
        if (VARS::request("cmd") == "cmb_sub") {
            $this->subclassCd   = VARS::post("SUBCLASSCD");
            
            //教育課程対応
            $this->classCd      = substr($this->subclassCd,0,2);
            $this->curriculum_Cd = substr($this->subclassCd,5,1);
            $this->useCurriculum_subclassCd = substr($this->subclassCd,7,13);

            $this->field = array();
            unset($this->seq);
        }

        $this->cmd = VARS::request("cmd");
    }

    function validateInput($kind)
    {
        //データ指定チェック
        if (!strlen($this->schregNo) || !strlen($this->subclassCd)) {
           $this->setWarning("MSG304", "(生徒・科目)");
           return false;
        }
        //更新・削除
        if ($kind=="update" || $kind=="delete") {
            //データ選択チェック
            if (!strlen($this->field["REGDDATE"]) || !strlen($this->seq)) {
               $this->setWarning("MSG308");
               return false;
            }
        }
        //更新・追加
        if ($kind=="update" || $kind=="insert") {
            //入力必須チェック
            if (!strlen($this->field["REGDDATE"]) || !strlen($this->field["MINUTES"])) {
               $this->setWarning("MSG301");
               return false;
            }
            //制限チェック
            if (strlen($this->field["REMARK"]) > 90) {
                $this->setWarning("MSG915", "(備考は90バイトまでです)");
                return false;
            }
        }

        return true;
    }

    function &getInsertModel() {
        if ($this->validateInput("insert")) {
            $this->seq = knjh110a_2Query::insertQuery($this);
            $this->setMessage("MSG201");
        }
    }

    function &getUpdateModel() {
        if ($this->validateInput("update")) {
            knjh110a_2Query::updateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    function &getDeleteModel() {
        if ($this->validateInput("delete")) {
            knjh110a_2Query::deleteQuery($this);
            $this->setMessage("MSG201");
            unset($this->seq);
            $this->field = array();
        }
    }
}
?>
