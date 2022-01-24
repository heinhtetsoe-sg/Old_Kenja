<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd104dModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjd104dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("YEAR"              =>  VARS::post("YEAR"),                //年度
                             "SEMESTER"          =>  VARS::post("SEMESTER"),            //学期
                             "GRADE"             =>  VARS::post("GRADE"),               //学年
                             "HR_CLASS"          =>  VARS::post("HR_CLASS"),            //クラス
                             "TESTCD"            =>  VARS::post("TESTCD"),              //テスト
                             "USE_GRAPH"         =>  VARS::post("USE_GRAPH"),           //チェックボックス選択データ
                             "CATEGORY_IS_CLASS" =>  VARS::post("CATEGORY_IS_CLASS"),   //カテゴリ区分
                             "GRADE_HR_CLASS"    =>  VARS::post("GRADE_HR_CLASS"),      //年組
                             "DBNAME"            =>  VARS::post("DBNAME"),              //DB変更に備えて
                     );
        $this->cmd         = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
