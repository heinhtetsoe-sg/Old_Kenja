<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjb040Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjb040Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("RADIO"            =>  VARS::post("RADIO"),            //時間割種別ラジオボタン
                             "TITLE"            =>  VARS::post("TITLE"),            //基本時間割コンボボックス
                             "DATE"             =>  VARS::post("DATE"),             //開始日付（通常時間割）
                             "DATE2"            =>  VARS::post("DATE2"),            //終了日付（通常時間割）
                             "SECTION_CD_NAME1" =>  VARS::post("SECTION_CD_NAME1"), //所属コンボボックス
                             "SECTION_CD_NAME2" =>  VARS::post("SECTION_CD_NAME2"), //所属コンボボックス
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //帳票区分チェックボックス
                             "OUTPUT2"          =>  VARS::post("OUTPUT2"),          //帳票区分チェックボックス
                             "SUBCLASS_CHAIR_DIV"  =>  VARS::post("SUBCLASS_CHAIR_DIV"),  //出力選択(科目名・講座名)ラジオボタン
                             "DBNAME"           =>  VARS::post("DBNAME"),           //DB変更に備えて
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
    function clean()
    {
    }
}
?>
