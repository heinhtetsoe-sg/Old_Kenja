<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh726Model extends Model
{
    public $field      = array();
    public $cmd;
    public $year;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    public function knjh726Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd   = STAFFCD;                  //職員コード
    }
    public function init()
    {
        $this->year      = CTRL_YEAR;
        $this->field = array("TOTALCHKBOX" =>  VARS::post("TOTALCHKBOX"),  //合計チェックボックス
                             "TESTDIV"     =>  VARS::post("TESTDIV")       //学力テスト
                     );

        $this->cmd         = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
