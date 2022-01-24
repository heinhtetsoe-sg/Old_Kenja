<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd105cModel extends Model {
    var $field      = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ
    var $schoolName;
    var $schoolCode;

    /**
     * メンバ変数の初期化
     */
    function knjd105cModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //クラス
                             "TESTCD"               =>  VARS::post("TESTCD"),               //テスト
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "GROUP_DIV"            =>  VARS::post("GROUP_DIV"),            //グループ
                             "OUTPUT_KIJUN"         =>  VARS::post("OUTPUT_KIJUN"),         //順位の基準点
                             "USE_ADDRESS"          =>  VARS::post("USE_ADDRESS"),          //住所選択
                             "USE_GRAPH"            =>  VARS::post("USE_GRAPH"),            //グラフ表・分布表の出力
                             "USE_HOGOSYA"          =>  VARS::post("USE_HOGOSYA"),          //保護者欄の出力
                             "DEVIATION_PRINT"      =>  VARS::post("DEVIATION_PRINT"),      //偏差値出力
                             "KANSAN"               =>  VARS::post("KANSAN"),               //100点に換算する
                             "SUBMIT_DATE"          =>  VARS::post("SUBMIT_DATE"),          //提出日
                             "KETTEN"               =>  VARS::post("KETTEN"),               //欠点
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        //初期値の設定
        $this->subclassGroup    = (strlen($this->Properties["useSubclassGroup"])) ? $this->Properties["useSubclassGroup"] : 0;

        //学校名取得
        $query = knjd105cQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);
    }

    function clean() {}
}
?>
