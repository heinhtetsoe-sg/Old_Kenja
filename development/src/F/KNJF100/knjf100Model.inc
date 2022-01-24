<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjf100Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjf100Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("DATE1"         =>  VARS::post("DATE1"),     //日付FROM
                            "DATE2"          =>  VARS::post("DATE2"),     //日付TO
                            "CHECK1"        =>  VARS::post("CHECK1"),   //チェックボックス選択データ
                            "YEAR"           =>  VARS::post("YEAR"),      //年度
                            "SEMESTER"       =>  VARS::post("SEMESTER"),     //学期
                            "SEME_DATE"       =>  VARS::post("SEME_DATE"),     //学期期間
                            "DBNAME"         =>  VARS::post("DBNAME"),    //DB変更に備えて
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
