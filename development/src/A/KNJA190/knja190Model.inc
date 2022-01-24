<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja190Model extends Model {
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
    function knja190Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }

    function init()
    {               
        $this->field["YEAR"]             = VARS::post("YEAR");             //年度
        $this->field["GAKKI"]            = VARS::post("GAKKI");            //学期
        $this->field["OUTPUT"]           = VARS::post("OUTPUT");           //印刷対象
        $this->field["OUTPUT2"]          = VARS::post("OUTPUT2");          //出力順        NO001
        $this->field["CHECK1"]           = VARS::post("CHECK1");           //生徒名出力    NO001
        $this->field["CHECK2"]           = VARS::post("CHECK2");           //学籍番号出力  NO001
        $this->field["CHECK3"]           = VARS::post("CHECK3");           //保護者・保証人様出力
        $this->field["GRDDIV"]           = VARS::post("GRDDIV");           //出力条件
        $this->field["POROW"]            = VARS::post("POROW");            //行
        $this->field["POCOL"]            = VARS::post("POCOL");            //列
        $this->field["HUKUSIKI_KIRIKAE"] = VARS::post("HUKUSIKI_KIRIKAE"); //複式クラス用ラジオボタン
        $this->field["CHOICE"]           = VARS::post("CHOICE");           //出力指定選択 (1:個人指定 2:クラス指定)ラジオボタン

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
