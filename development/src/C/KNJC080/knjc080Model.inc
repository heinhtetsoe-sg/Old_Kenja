<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc080Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjc080Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("RADIO"           =>  VARS::post("RADIO"),         //ラジオ選択データ
                            "DATE1"            =>  VARS::post("DATE1"),         //日付FROM
                            "DATE2"            =>  VARS::post("DATE2"),         //日付TO
                            "SEMESTER"         =>  VARS::post("SEMESTER"),      //学期コード
                            "SEME_DATE"        =>  VARS::post("SEME_DATE"),     //学期開始日
                            "OUTPUT1"          =>  VARS::post("OUTPUT1"),       //チェックボックス選択データ
                            "OUTPUT2"          =>  VARS::post("OUTPUT2"),       //チェックボックス選択データ
                            "OUTPUT3"          =>  VARS::post("OUTPUT3"),       //チェックボックス選択データ
                            "DBNAME"           =>  VARS::post("DBNAME"),        //DB変更に備えて
                            "YEAR"             =>  VARS::post("YEAR"),          //年度
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
