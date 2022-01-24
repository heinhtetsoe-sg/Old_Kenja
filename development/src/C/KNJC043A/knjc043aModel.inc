<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc043aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjc043aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード
    }
    function init()
    {               
        $this->field = array("DATE1"            =>  VARS::post("DATE1"),            //印刷範囲開始日
                             "DATE2"            =>  VARS::post("DATE2"),            //印刷範囲終了日
                             "OUTPUT1"          =>  VARS::post("OUTPUT1"),			//集計票出力
                             "OUTPUT2"          =>  VARS::post("OUTPUT2"),			//明細票出力
                             "OUTPUT4"          =>  VARS::post("OUTPUT4"),			//空行をつめて印字
                             "OUTPUT5"          =>  VARS::post("OUTPUT5"),			//「未」出力
                             "PERIOD"           =>  VARS::post("PERIOD"),			//校時範囲
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
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
