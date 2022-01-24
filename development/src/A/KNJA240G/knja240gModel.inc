<?php
/**
 * 定数定義
*/

class knja240gModel extends Model
{
    public $field      = array();
    public $cmd;
    public $semeSDate;
    public $semeEDate;

    /**
     * メンバ変数の初期化
     */
    public function knja240gModel()
    {
    }

    public function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "DATE"         =>  VARS::post("DATE")
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
