<?php

require_once('for_php7.php');
/**
 * 定数定義
*/

class knjb1217Model extends Model {
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
    function knjb1217Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "KYOUSAI"          =>  VARS::post("KYOUSAI"),          //共済掛金
                             "KENKOUSHINDAN"    =>  VARS::post("KENKOUSHINDAN"),    //健康診断費
                             "SHASIN"           =>  VARS::post("SHASIN"),           //写真代
                             "LOCKER"           =>  VARS::post("LOCKER"),           //ロッカー代
                             "KAIHI"            =>  VARS::post("KAIHI"),            //立志会費
                             "RYOUSYUU"         =>  VARS::post("RYOUSYUU"),         //領収書
                             "HIKIWATASHI"      =>  VARS::post("HIKIWATASHI"),      //引渡書
                             "MEISAI"           =>  VARS::post("MEISAI"),           //明細書
                             "PRINTDATE"        =>  VARS::post("PRINTDATE"),        //日付
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //志願者・在学者選択
                             "FORMTYPE"         =>  VARS::post("FORMTYPE"),         //帳票種別
                             "PRINT_STR"        =>  VARS::post("PRINT_STR")         //文言
                             );

        $this->cmd         = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");
        if ($this->cmd != 'change_class') $this->select_opt = array();
        if ($this->cmd == "") $this->hr_set = "";
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //データチェック
    function dataCheck() {
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if (!$this->dataCheck()) {
            return;
        }
        knjb1217Query::getUpdateQuery($this);
        return;
    }
}
?>
