<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja171mModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knja171mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "CHOICE"               =>  VARS::post("CHOICE"),               //出力指定
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //クラス
                             "RISHUUCHECK"          =>  VARS::post("RISHUUCHECK"),          //履修登録者チェックボックス
                             "TAISHOUSHA"           =>  VARS::post("TAISHOUSHA"),           //対象者ラジオボタン
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->nextyear = CTRL_YEAR+1;

        //初期値設定
        if ($this->cmd == "") {
            $this->field["RISHUUCHECK"] = '1';
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
