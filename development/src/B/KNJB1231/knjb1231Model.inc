<?php

require_once('for_php7.php');


class knjb1231Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $select_opt = array();  //セレクトデータ
    var $selectleft;
    var $selectleftval;

    /**
     * メンバ変数の初期化
     */
    function knjb1231Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }

    function init()
    {               
        $this->field["YEAR"]             = VARS::post("YEAR");              //年度
        $this->field["SEMESTER"]         = VARS::post("SEMESTER");          //学期
        $this->field["SEARCH_DIV"]       = VARS::post("SEARCH_DIV");        //出力種別 (1:新入生 2:在校生)
        $this->field["EXE_YEAR"]         = VARS::post("EXE_YEAR");          //登録年度
        $this->field["EXE_NENDO_PATERN"] = VARS::post("EXE_NENDO_PATERN");  //登録年度パターン
        $this->field["CHOICE"]           = VARS::post("CHOICE");            //出力指定選択 (1:個人指定 2:クラス指定)

        if (VARS::post("GRADE_HR_CLASS")) $this->field["GRADE_HR_CLASS"] = VARS::post("GRADE_HR_CLASS");   //年組

        $this->cmd         = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");
        if ($this->cmd != 'change_class') $this->select_opt = array();

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
