<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjb070Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjb070Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }
    function init() {
        $this->field = array("RADIO"                =>  VARS::post("RADIO"),                //時間割種別
                             "DATE1"                =>  VARS::post("DATE1"),                //指定日付FROM
                             "DATE2"                =>  VARS::post("DATE2"),                //指定日付TO
                             "NEN"                  =>  VARS::post("NEN"),                  //学年（１年）
                             "NEN2"                 =>  VARS::post("NEN2"),                 //学年（２年）
                             "NEN3"                 =>  VARS::post("NEN3"),                 //学年（３年）
                             "TITLE"                =>  VARS::post("TITLE"),                //時間割コンボボックス選択データ
                             "KUBUN"                =>  VARS::post("KUBUN"),                //帳票区分
                             "SUBCLASS_CHAIR_DIV"   =>  VARS::post("SUBCLASS_CHAIR_DIV"),   //学級出力項目(科目名・講座名)上
                             "STAFF_SISETU_DIV"     =>  VARS::post("STAFF_SISETU_DIV"),     //学級出力項目(職員名・施設名)下
                             "K2SUBCLASS_CHAIR_DIV" =>  VARS::post("K2SUBCLASS_CHAIR_DIV"), //職員出力項目(科目名・講座名)上
                             "SISETU_CLASS_DIV"     =>  VARS::post("SISETU_CLASS_DIV"),     //職員出力項目(施設名・受講クラス)下
                             "K3SUBCLASS_CHAIR_DIV" =>  VARS::post("K3SUBCLASS_CHAIR_DIV"), //施設出力項目(科目名・講座名)上
                             "STAFF_CLASS_DIV"      =>  VARS::post("STAFF_CLASS_DIV"),      //施設出力項目(職員名・受講クラス)下
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
