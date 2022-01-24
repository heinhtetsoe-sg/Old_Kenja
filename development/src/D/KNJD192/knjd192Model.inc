<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd192Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $schoolName;
    var $schoolCode;

    /**
     * メンバ変数の初期化
     */
    function knjd192Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コード
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //組
                             "TESTCD"               =>  VARS::post("TESTCD"),               //テスト
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "GROUP_DIV"            =>  VARS::post("GROUP_DIV"),            //グループ区分
                             "RANK_DIV"             =>  VARS::post("RANK_DIV"),             //順位の基準点
                             "KETTEN"               =>  VARS::post("KETTEN"),               //欠点
                             "SUBCLASS_MAX"         =>  VARS::post("SUBCLASS_MAX"),         //最大科目数
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();

        //学校名取得
        $query = knjd192Query::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
