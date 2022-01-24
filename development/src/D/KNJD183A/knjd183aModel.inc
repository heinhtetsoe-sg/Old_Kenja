<?php

require_once('for_php7.php');
/**
 * 定数定義
*/

class knjd183aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $semester;
        
    /**
     * メンバ変数の初期化
     */
    function knjd183aModel()
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
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //クラス
                             "TESTCD"           =>  VARS::post("TESTCD"),           //テスト
                             "FRM_DIV"          =>  VARS::post("FRM_DIV"),          //縦横ラジオボタン
                             "OUTPUT_HEIJOU"    =>  VARS::post("OUTPUT_HEIJOU"),    //平常点
                             "DISP"             =>  VARS::post("DISP"),             //クラス or 個人
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->semester = VARS::post("SEMESTER");
        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }
        
        $this->test_cd  = VARS::post("TEST_CD");
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
