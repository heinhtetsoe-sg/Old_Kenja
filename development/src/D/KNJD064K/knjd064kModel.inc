<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd064kModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjd064kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト
                             "OUTPUT4"          =>  VARS::post("OUTPUT4"),          //チェックボックス選択データ
                             "OUTPUT3"          =>  VARS::post("OUTPUT3"),          //チェックボックス選択データ
                             "OUTPUT5"          =>  VARS::post("OUTPUT5"),          //チェックボックス選択データ
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DATE"             =>  VARS::post("DATE"),             //学籍処理日
                             "PRINT_DATE"       =>  VARS::post("PRINT_DATE"),       //印刷日付 NO002 学籍処理日
                             "DBNAME"           =>  VARS::post("DBNAME"),           //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>