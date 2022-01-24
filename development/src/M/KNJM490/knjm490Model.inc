<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm490Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $subcd;     //科目コード
    var $chacd;     //口座コード
        
    /**
     * メンバ変数の初期化
     */
    function knjm490Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),         //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),        //学期
                             "SUBCLASS"         =>  VARS::post("SUBCLASS"),     //科目
                             "OUTPUT"           =>  VARS::post("OUTPUT"),       //印刷対象
                             "OUTPUT2"          =>  VARS::post("OUTPUT2"),      //出力順
                     );
        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["useCurriculumcd"] == "1") {
            $this->subcd = substr($this->field["SUBCLASS"], 0, 13);
            $this->chacd = substr($this->field["SUBCLASS"], 13, 7);
        } else {
            $this->subcd = substr($this->field["SUBCLASS"], 0, 6);
            $this->chacd = substr($this->field["SUBCLASS"], 6, 7);
        }
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
