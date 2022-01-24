<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja200Model extends Model
{
    public $field      = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    public function knja200Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    public function init()
    {
        $this->field = array("DATE"            =>  VARS::post("DATE"),              //学籍処理日
                            "DATEFT"           =>  VARS::post("DATEFT"),            //学期開始終了日
                            "YEAR"             =>  VARS::post("YEAR"),              //年度
                            "SEMESTER"         =>  VARS::post("SEMESTER"),          //学期      // add 02/10/02
                            "HR_CLASS_TYPE"    =>  VARS::post("HR_CLASS_TYPE"),     //法定クラス/実クラス
                            "GAKUNEN_KONGOU"   =>  VARS::post("GAKUNEN_KONGOU"),    //学年混合
                            "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組
                            "TEL"              =>  VARS::post("TEL"),               //TEL
                            "SCHOOLNAME"       =>  VARS::post("SCHOOLNAME"),        //SCHOOLNAME
                            "FORM_SENTAKU"     =>  VARS::post("FORM_SENTAKU"),      //フォーム選択ラジオ
                            "PRINT_INFO"       =>  VARS::post("PRINT_INFO"),        //出力情報(出身学校/ふりがな)
                            "DBNAME"           =>  VARS::post("DBNAME"),            //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->getPrgDefaultVal("KNJA200", SCHOOLCD, SCHOOLKIND);
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
