<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd665fModel extends Model {
    var $field      = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ
    var $schoolName;
    var $schoolCode;

    /**
     * メンバ変数の初期化
     */
    function knjd665fModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //クラス
                             "TESTCD"               =>  VARS::post("TESTCD"),               //テスト
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "PRINT_REGD"           =>  VARS::post("PRINT_REGD"),           //年組は法定クラスを印字する 
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->subclassGroup    = (strlen($this->Properties["useSubclassGroup"])) ? $this->Properties["useSubclassGroup"] : 0;

        //学校名取得
        $query = knjd665fQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);
    }

    function clean() {}
}
?>