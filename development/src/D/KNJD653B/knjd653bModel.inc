<?php

require_once('for_php7.php');
/**
 * 定数定義
*/

class knjd653bModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjd653bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト
                             "OUTPUT4"          =>  VARS::post("OUTPUT4"),          //チェックボックス選択データ
                             "OUTPUT5"          =>  VARS::post("OUTPUT5"),          //チェックボックス選択データ
                             "OUTPUT_COURSE"    =>  VARS::post("OUTPUT_COURSE"),    //チェックボックス選択データ
                             "OUTPUT_RANK1"     =>  VARS::post("OUTPUT_RANK1"),     //総合順位出力（クラス）
                             "OUTPUT_RANK2"     =>  VARS::post("OUTPUT_RANK2"),     //総合順位出力（学年）
                             "OUTPUT_RANK3"     =>  VARS::post("OUTPUT_RANK3"),     //総合順位出力（コース）
                             "OUTPUT_RANK5"     =>  VARS::post("OUTPUT_RANK5"),     //総合順位出力（コースグループ）
                             "OUTPUT_KIJUN"     =>  VARS::post("OUTPUT_KIJUN"),     //順位の基準点ラジオボタン
                             "OUTPUT_KAMOKUSU"  =>  VARS::post("OUTPUT_KAMOKUSU"),  //最大科目数ラジオボタン
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
		$this->getPropertiesAll();
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
