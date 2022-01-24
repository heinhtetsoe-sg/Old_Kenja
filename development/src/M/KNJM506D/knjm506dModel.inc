<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm506dModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    /**
     * メンバ変数の初期化
     */
    function knjm506dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;
    }
    function init()
    {               
        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "CHOICE"               =>  VARS::post("CHOICE"),               //出力指定
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //クラス
                             "PRINT_DATE"           =>  VARS::post("PRINT_DATE"),           //印刷日
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->nextyear = CTRL_YEAR + 1;
        //初期値設定
        if ($this->cmd == "") {
            $this->field["YEAR"] = CTRL_YEAR;
        }

    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
