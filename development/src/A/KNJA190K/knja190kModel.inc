<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja190kModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $select_opt  = array();    //セレクトデータ
    var $selectleft;

    /**
     * メンバ変数の初期化
     */
    function knja190kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {
        $this->field = array("YEAR"            =>  VARS::post("YEAR"),             //年度
                             "GAKKI"           =>  VARS::post("GAKKI"),            //学期
                             "GRADE_HR_CLASS"  =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DISP"            =>  VARS::post("DISP"),             //表示選択
                             "OUTPUT"          =>  VARS::post("OUTPUT"),           //印刷対象
                             "OUTPUT2"         =>  VARS::post("OUTPUT2"),          //出力順
                             "POROW"           =>  VARS::post("POROW"),            //行
                             "POCOL"           =>  VARS::post("POCOL")             //列
                     );

        $this->cmd        = VARS::request("cmd");
        $this->selectleft = VARS::post("selectleft");
        if ($this->cmd != 'change_class') $this->select_opt = array();
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
