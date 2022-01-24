<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje391mModel extends Model {
    var $field      = array();
    var $select_data    = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knje391mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //処理年月日
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期コード
                             "HR_CLASS_TYPE"    =>  VARS::post("HR_CLASS_TYPE"),    //クラス方式
                             "PRINT_HR_CLASS_TYPE" =>  VARS::post("PRINT_HR_CLASS_TYPE"),    //出力クラス
                             "GAKUNEN_KONGOU"   =>  VARS::post("GAKUNEN_KONGOU"),   //学年混合チェックボックス
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "PRINT_A"          =>  VARS::post("PRINT_A"),          //Aを印刷
                             "PRINT_B"          =>  VARS::post("PRINT_B"),          //Bを印刷
                             "PRINT_C"          =>  VARS::post("PRINT_C"),          //Cを印刷
                             "PRINT_D"          =>  VARS::post("PRINT_D"),          //Dを印刷
                             "PRINT_E"          =>  VARS::post("PRINT_E"),          //Eを印刷
                             "PRINT_F"          =>  VARS::post("PRINT_F"),          //Fを印刷
                             "PRINT_G"          =>  VARS::post("PRINT_G"),          //Gを印刷
                             "PRINT_H"          =>  VARS::post("PRINT_H"),          //Hを印刷
                             "PRINT_I"          =>  VARS::post("PRINT_I"),          //Iを印刷
                             "A_OUTPUT1"        =>  VARS::post("A_OUTPUT1"),        //基本情報 生育歴
                             "A_OUTPUT2"        =>  VARS::post("A_OUTPUT2"),        //基本情報 発作
                             "A_OUTPUT3"        =>  VARS::post("A_OUTPUT3"),        //基本情報 視力
                             "A_OUTPUT4"        =>  VARS::post("A_OUTPUT4"),        //基本情報 聴力
                             "A_OUTPUT5"        =>  VARS::post("A_OUTPUT5"),        //基本情報 補装具等の給付
                             "D_OUTPUT1"        =>  VARS::post("D_OUTPUT1"),        //アセスメント表 障害名等
                             "D_OUTPUT2"        =>  VARS::post("D_OUTPUT2"),        //アセスメント表 実態概要・障害の特性
                             "D_OUTPUT3"        =>  VARS::post("D_OUTPUT3"),        //アセスメント表 検査
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

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
