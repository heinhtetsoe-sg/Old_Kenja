<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd626lModel extends Model
{
    public $field = array();
    public $cmd;
    public $schoolKind = "H";

    /**
     * メンバ変数の初期化
     */
    public function knjd626lModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                  //職員コード
    }
    public function init()
    {
        $this->field = array("GRADE"    => VARS::post("GRADE"),   //学年
                             "SEMESTER" => VARS::post("SEMESTER") //学期
                            );

        $this->cmd = VARS::request("cmd");
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
