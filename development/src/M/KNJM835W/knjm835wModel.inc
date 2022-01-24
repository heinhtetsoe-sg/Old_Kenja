<?php

/**
 * 定数定義
*/

class knjm835wModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    public function knjm835wModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    public function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),              //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),             //学期
                             "TESTCD"           =>  VARS::post("TESTCD"),            //テスト種別
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
    }
}
