<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh441Model extends Model {
    var $field      = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ
    var $schoolName;
    var $schoolCode;

    /**
     * メンバ変数の初期化
     */
    function knjh441Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //クラス
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();

        //学校名取得
        $query = knjh441Query::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);
    }

    function clean() {}
}
?>
