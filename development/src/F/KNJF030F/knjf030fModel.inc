<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjf030fModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $select_opt  = array(); //セレクトデータ
    var $selectleft;
    var $schoolName;

    /**
     * メンバ変数の初期化
     */
    function knjf030fModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),         //年度
                            "GAKKI"             =>  VARS::post("GAKKI"),        //学期コード
                            "KUBUN"             =>  VARS::post("KUBUN"),        //クラス・個人表示指定
                            "GRADE_HR_CLASS"    =>  VARS::post("GRADE_HR_CLASS"),   //クラス
                            "OUTPUTA"           =>  VARS::post("OUTPUTA"),      //出力方法オプションボタン
                            "PRINT_STAMP"       =>  VARS::post("PRINT_STAMP"),  //印影出力
                            "DBNAME"            =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        if ($this->cmd != 'change_class') $this->select_opt = array();

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
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
