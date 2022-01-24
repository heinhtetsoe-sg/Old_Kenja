<?php

require_once('for_php7.php');

class knjz233Model extends Model {
    var $cmd;
    var $subclasscd;        //primary
    var $record_dat_flg = "0";
    var $selectdata;
    var $rightclasscd;  //右画面の教科コンボ
    var $calculate_credit_flg;
    var $school_kind;
    var $curriculum_Cd;

    //メンバ変数の初期化
    function knjz233Model()
    {
    }

    function init()
    {   
        if (VARS::get("PROGRAMID")) {
            unset($this->classcd);
            unset($this->curriculumCd);
            unset($this->subclasscd);
            unset($this->rightclasscd);
            //教育課程用
            unset($this->school_kind);
            unset($this->curriculum_Cd);
            unset($this->rightclasscd);
            
        }

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (VARS::request("VALUE")) {
                $this->school_kind   = substr(VARS::get("VALUE"),3,1);
                $this->curriculum_Cd = substr(VARS::get("VALUE"),5,1);
                $this->subclasscd    = substr(VARS::get("VALUE"),7,13);
                $this->rightclasscd  = substr(VARS::get("VALUE"),0,2);
            }
        } else {
            if (VARS::request("VALUE")) {
                $this->subclasscd = VARS::request("VALUE");
                $this->rightclasscd = substr(VARS::get("VALUE"),0,2);
            }
        }
        
        if ($this->cmd == "check"){
            $this->selectdata   = VARS::post("selectdata");
        }
        if (strlen(VARS::post("classcd"))) $this->classcd = VARS::post("classcd");
        if (strlen(VARS::post("CURRICULUM_CD"))) $this->curriculumCd = VARS::post("CURRICULUM_CD");

        $this->calculate_credit_flg = VARS::post("CALCULATE_CREDIT_FLG");

        $this->cmd              = VARS::request("cmd");
        $this->record_dat_flg   = VARS::post("record_dat_flg");
        
    }

    function &getUpdateModel()
    {
        knjz233Query::getUpdateQuery($this);
        $this->setMessage("MSG201");

        return;
    }
    
    //データコピー
    function &getCopyModel()
    {
        if (!$this->copyCheck()) {
            return;
        }

        knjz233Query::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }
    //コピーチェック
    function copyCheck()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $lastyear = CTRL_YEAR - 1;

        if (0 < $db->getOne(knjz233Query::checkTypeTable($this, CTRL_YEAR))) {
            $this->setWarning("データが既に存在しています。");
            return false;
        }

        if (0 == $db->getOne(knjz233Query::checkTypeTable($this, $lastyear))) {
            $this->setWarning("コピー対象データがありません。");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);

        return true;
    }
}
?>
