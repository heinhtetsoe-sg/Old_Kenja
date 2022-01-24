<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjb043Model extends Model
{
    public $field      = array();
    public $year;
    public $cmd;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    public function knjb043Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }

    public function init()
    {
        $this->year = CTRL_YEAR;
        $this->field = array("SDATE"   =>  VARS::post("SDATE"),    //学籍処理日
                             "EDATE"   =>  VARS::post("EDATE"),    //学籍処理日
                             "DBNAME"  =>  VARS::post("DBNAME"),   //DB変更に備えて
                     );

        $this->getPropertiesAll();
        $this->cmd = VARS::request("cmd");
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
