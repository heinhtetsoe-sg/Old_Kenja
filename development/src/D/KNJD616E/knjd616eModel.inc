<?php

require_once('for_php7.php');

class knjd616eModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $selectdata;
    var $selectSemeName;
    var $selectTestName;
    var $virus;
    var $hjFlg;

    /**
     * メンバ変数の初期化
     */
    function knjd616eModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //職員コード
    }
    function init() {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テストコンボ
                             "OUTPUT_SOUGOU"    =>  VARS::post("OUTPUT_SOUGOU"),    //チェックボックス選択データ
                             "RANK_BASE_SCORE"  =>  VARS::post("RANK_BASE_SCORE"),  //順位の基準点ラジオボタン
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        $this->selectdata = VARS::post("selectdata");
        $this->selectSemeName = VARS::post("selectSemeName");
        $this->selectTestName = VARS::post("selectTestName");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

}
?>
