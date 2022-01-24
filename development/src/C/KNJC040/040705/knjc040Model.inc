<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc040Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjc040Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
		$this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("YEAR"            =>  VARS::post("YEAR"),              //年度
                            "SEMESTER"         =>  VARS::post("SEMESTER"),          //学期
                            "SEME_DATE"        =>  VARS::post("SEME_DATE"),         //学期
                            "DATE1"            =>  VARS::post("DATE1"),             //印刷範囲開始日
                            "DATE2"            =>  VARS::post("DATE2"),             //印刷範囲終了日
                            "OUTPUT2"          =>  VARS::post("OUTPUT2"),           //ラジオ選択データ（累計種別）
                            "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組
                            "DBNAME"           =>  VARS::post("DBNAME"),            //DB変更に備えて
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
