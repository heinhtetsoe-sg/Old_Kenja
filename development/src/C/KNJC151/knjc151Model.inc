<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc151Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectdata;
    var $schoolCd;
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjc151Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->auth = AUTHORITY;

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                            "OUTPUT"            =>  VARS::post("OUTPUT"),           //名票
                            "OUTPUT2"           =>  VARS::post("OUTPUT2"),          //空行を詰めて印字
                            "MONTH"             =>  VARS::post("MONTH"),            //該当月
                            "RATE"              =>  VARS::post("RATE"),             //遅刻率
                            "PRINT_DATE"        =>  VARS::post("PRINT_DATE"),       //印刷日付
                            "GAKKI"             =>  VARS::post("GAKKI"),            //学期コード
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->selectdata  = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

}
?>
