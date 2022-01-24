<?php
/**
 * 定数定義
*/

class knjl354Model extends Model
{
    public $field      = array();
    public $cmd;
    public $ObjYear;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    public function knjl354Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
        $this->field = array("OUTPUT" =>  VARS::post("OUTPUT")); //印刷対象
    }
    public function init()
    {
        $this->field = array("APDIV"        =>  VARS::post("APDIV"),  //入試制度
                             "TESTSCR"      =>  VARS::post("TESTSCR") //指定範囲
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
