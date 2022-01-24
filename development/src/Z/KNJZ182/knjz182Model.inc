<?php

require_once('for_php7.php');

class knjz182Model extends Model {
    var $field       = array();
    var $low_stock   = array();
    var $high_stock  = array();
    var $cmd;
    var $sepa;
    var $level;

    function knjz182Model() {}

    function init() {
        $this->cmd  = VARS::request("cmd");

        $this->field = array("PRE_SEMESTER"   =>  VARS::post("PRE_SEMESTER"),   //学期(参照)
                             "PRE_TESTKINDCD" =>  VARS::post("PRE_TESTKINDCD"), //成績種別(参照)
                             "SEMESTER"       =>  VARS::request("SEMESTER"),       //学期(対象)
                             "TESTKINDCD"     =>  VARS::request("TESTKINDCD"),     //成績種別(対象)
                             "DIV"            =>  VARS::request("DIV"),            //区分
                             "GRADE"          =>  VARS::request("GRADE"),          //学年
                             "HR_CLASS"       =>  VARS::post("HR_CLASS"),       //クラス
                             "COURSE"         =>  VARS::post("COURSE"),         //課程学科コース
                             "SUBCLASSCD"     =>  VARS::post("SUBCLASSCD"),     //科目
                             "ASSESSLEVELCNT" =>  VARS::post("ASSESSLEVELCNT")  //評定段階数
                             );

        if ($this->cmd == 'update') {
            for ($i = 1; $i <= $this->field["ASSESSLEVELCNT"]; $i++) {
                $this->field["ASSESSLOW" .$i] = VARS::post("ASSESSLOW" .$i);
                $this->field["ASSESSHIGH".$i] = VARS::post("ASSESSHIGH".$i);
            }
        }
        
        $this->getPropertiesAll(); //プロパティファイル
    }

    /********************/
    /* 入力項目チェック */
    /********************/
    function validateInput() {
        return true;
    }

    /****************/
    /* アップデート */
    /****************/
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjz182Query::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return false;
    }

    /**********/
    /* コピー */
    /**********/
    function &getCopyModel()
    {
        $result = knjz182Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return true;
    }

    //変数をクリアします
    function clean() {}
}
?>
