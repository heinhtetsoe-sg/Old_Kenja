<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl362aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $ObjYear;

    /**
     * メンバ変数の初期化
     */
    function knjl362aModel()
    {
    }

    function init()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
        $this->ObjYear      = CTRL_YEAR+1;
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),         //年度
                             "POROW"            =>  VARS::post("POROW"),        //行
                             "POCOL"            =>  VARS::post("POCOL"),        //列
                             "APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"), //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),      //入試区分
                             "FINSCHOOLDIV"     =>  VARS::post("FINSCHOOLDIV"), //区分
                             "DBNAME"           =>  VARS::post("DBNAME")        //DB変更に備えて
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
