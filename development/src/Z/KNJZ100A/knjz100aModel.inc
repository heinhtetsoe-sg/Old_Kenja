<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjz100aModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;       //コントロールマスタ
    public $staffcd;       //職員コード
    public $isJyoto;

    /**
     * メンバ変数の初期化
     */
    public function knjz100aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    public function init()
    {
        $this->field = array("YEAR"                  =>  VARS::post("YEAR"),                   //年度
                             "POROW"                 =>  VARS::post("POROW"),                  //行
                             "POCOL"                 =>  VARS::post("POCOL"),                  //列
                             "GAKKI"                 =>  VARS::post("GAKKI"),                  //学期コード
                             "FINSCHOOLDIV"          =>  VARS::post("FINSCHOOLDIV"),           //区分
                             "TO_PRINCIPAL"          =>  VARS::post("TO_PRINCIPAL"),           //学校長宛
                             "PERSONNEL"             =>  VARS::post("PERSONNEL"),              //担当者あり
                             "SELECT_FINSCHOOL_TYPE" =>  VARS::post("SELECT_FINSCHOOL_TYPE"),  //校種
                             "DBNAME"                =>  VARS::post("DBNAME")                  //DB変更に備えて
                            );


        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
