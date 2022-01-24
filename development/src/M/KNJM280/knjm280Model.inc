<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm280Model extends Model {
    var $field      = array();
    var $cmd;
    var $semester;      //学期
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjm280Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->Date  = VARS::post("DATE");
        $this->field = array("DATE"         =>  VARS::post("DATE"),     //日付
                             "OUTPUT"       =>  VARS::post("OUTPUT"),   //出力種類
                             "STAFF"        =>  VARS::post("STAFF"),    //STAFF
                             "DBNAME"       =>  VARS::post("DBNAME")    //DB変更に備えて
                     );

        $this->semester = 0;                //データ更新時に使用
        if ($this->control["学期数"] == 3){
            if ($this->control["学期開始日付"]["1"] <= $this->Date && $this->control["学期終了日付"]["1"] >= $this->Date) $this->semester = 1;
            if ($this->control["学期開始日付"]["2"] <= $this->Date && $this->control["学期終了日付"]["2"] >= $this->Date) $this->semester = 2;
            if ($this->control["学期開始日付"]["3"] <= $this->Date && $this->control["学期終了日付"]["3"] >= $this->Date) $this->semester = 3;              //データ更新時に使用
        }else {
            if ($this->control["学期開始日付"]["1"] <= $this->Date && $this->control["学期終了日付"]["1"] >= $this->Date) $this->semester = 1;
            if ($this->control["学期開始日付"]["2"] <= $this->Date && $this->control["学期終了日付"]["2"] >= $this->Date) $this->semester = 2;
        }

        //年度内チェック：エラー時は、年度と学期を今年度・今学期にする。
        if ($this->semester == 0 && $this->Date != ''){
            $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->semester = CTRL_SEMESTER;
            $this->setWarning("MSG915","(年度範囲外)");
            return false;
        }

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
