<?php

require_once('for_php7.php');

class knjz181aModel extends Model {
    var $field       = array();
    var $low_stock   = array();
    var $high_stock  = array();
    var $cmd;
    var $sepa;
    var $level;

    function knjz181aModel() {}

    function init() {
        $this->cmd  = VARS::request("cmd");

        $this->field = array("PRE_DATA_DIV"     =>  VARS::post("PRE_DATA_DIV"),     //データ種別(参照)
                             "PRE_MOCKCD"       =>  VARS::post("PRE_MOCKCD"),       //テスト名称(参照)
                             "DATA_DIV"         =>  VARS::request("DATA_DIV"),      //データ種別(対象)
                             "MOCKCD"           =>  VARS::request("MOCKCD"),        //テスト名称(対象)
                             "DIV"              =>  VARS::request("DIV"),           //区分
                             "GRADE"            =>  VARS::request("GRADE"),         //学年
                             "HR_CLASS"         =>  VARS::post("HR_CLASS"),         //クラス
                             "COURSE"           =>  VARS::post("COURSE"),           //課程学科コース
                             "MOCK_SUBCLASS_CD" =>  VARS::post("MOCK_SUBCLASS_CD"), //科目
                             "ASSESSLEVELCNT"   =>  VARS::post("ASSESSLEVELCNT")    //評定段階数
                             );

        if ($this->cmd == 'update') {
            for ($i = 1; $i <= $this->field["ASSESSLEVELCNT"]; $i++) {
                $this->field["ASSESSLOW" .$i] = VARS::post("ASSESSLOW" .$i);
                $this->field["ASSESSHIGH".$i] = VARS::post("ASSESSHIGH".$i);
            }
        }
        //プロパティファイルの読込み
        $this->getPropertiesAll();
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
            knjz181aQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
            return true;
        }
        return false;
    }

    /**********/
    /* コピー */
    /**********/
    function &getCopyModel()
    {
        $result = knjz181aQuery::getCopyQuery($this);
        $this->setMessage("MSG201");
        return true;
    }

    //変数をクリアします
    function clean() {}
}
?>
