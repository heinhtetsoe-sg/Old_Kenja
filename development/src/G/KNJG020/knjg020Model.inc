<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjg020Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjg020Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("RADIO"          =>  VARS::post("RADIO"),      //ラジオ選択データ（卒業生/在学生）
                             "OUTPUT2"        =>  VARS::post("OUTPUT2"),       //印刷ページ番号条件ラジオボタン NO002
                             "OUTPUT"         =>  VARS::post("OUTPUT"),       //出力順選択
                             "DATE"           =>  VARS::post("DATE"),       //証明書発行日付
                             "SEMESTER"       =>  VARS::post("SEMESTER"),    //学期コード
                             "CERTIF_KIND"    =>  VARS::post("CERTIF_KIND"),//証明書種類 NO001
                             "PAGE"           =>  VARS::post("PAGE"),       //ページ番号初期値
                             "DBNAME"         =>  VARS::post("DBNAME"),     //DB変更に備えて
                             "YEAR"           =>  VARS::post("YEAR"),       //年度
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
