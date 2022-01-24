<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc052Model extends Model {

    var $field      = array();
    var $cmd;       //一覧表示時のcmd
    var $staffcd;   //職員コード
    var $control;           //コントロールマスタ
        
    /**
     * メンバ変数の初期化
     */
    function knjc052Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }
    function init()
    {               
        $this->field = array("DATE"             =>  VARS::post("DATE"),       //印刷範囲開始日
                             "DATE2"            =>  VARS::post("DATE2"),      //印刷範囲終了日
                             "OUTPUT3"          =>  VARS::post("OUTPUT3"),    //チェック選択
                             "GAKKI"            =>  VARS::post("GAKKI")       //学期コード
                     );
        $this->cmd          = VARS::request("cmd");         //一覧表示時のcmd

        $this->getPropertiesAll(); //プロパティファイルの読込み

    }
}
?>
