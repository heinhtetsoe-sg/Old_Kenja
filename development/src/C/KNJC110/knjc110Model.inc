<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc110Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjc110Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD; //職員コード
    }

    function init() {
        $this->field = array("DATE1"            =>  VARS::post("DATE1"),            //日付FROM
                             "DATE2"            =>  VARS::post("DATE2"),            //日付TO
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "OUTPUTNAME"       =>  VARS::post("OUTPUTNAME"),      //出力内容（科目または講座）
                             "OUTPUTJISUU"      =>  VARS::post("OUTPUTJISUU"),     //出力時数
                             "GAKKINAME"        =>  VARS::post("GAKKINAME"),        //学期名
                             "DBNAME"           =>  VARS::post("DBNAME"),           //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    /**
     * 変数をクリアします
     */
    function clean() {}
}
?>
