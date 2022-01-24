<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc037Model extends Model {
    var $field         = array();
    var $select_data   = array();
    var $opt_subclass  = array();
    var $tableName;
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjc037Model() {}

    function init() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

        list($grade, $hr_class) = explode(':', VARS::post("NENKUMI"));

        $this->field = array("GAKKI"           =>  VARS::post("GAKKI"),    //学期
                             "NENKUMI"         =>  VARS::post("NENKUMI"),  //年組
                             "GRADE"           =>  $grade,                 //学年
                             "HR_CLASS"        =>  $hr_class,              //組
                             "S_DATE"          =>  VARS::post("S_DATE"),   //集計範囲(開始)
                             "E_DATE"          =>  VARS::post("E_DATE"),   //集計範囲(終了)
                             "OUT_PUT_KEKKA"   =>  VARS::post("OUT_PUT_KEKKA"),   //1日欠席の時は欠課を集計する
                             "DBNAME"          =>  VARS::post("DBNAME"));  //DB変更に備えて

        $this->field["GAKKI"] = $this->field["GAKKI"] ? $this->field["GAKKI"] : CTRL_SEMESTER;

        $this->cmd = VARS::request("cmd");

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }


    /**
     * 変数をクリアします
     */
    function clean(){}
}
?>
