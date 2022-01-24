<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjb0070Model extends Model {
    var $cmd;
    var $executedate;                       //カレンダーコントロール用データ
    var $jtype;
    var $semester;
    var $seq;

    /**
     * メンバ変数の初期化
     */
    function knjb0070Model()
    {
    }

    function init()
    {
        $this->jtype = VARS::post("jtype");
        $this->semester = VARS::post("semester");
        $this->seq 		= VARS::post("seq");
        $this->executedate = VARS::post("executedate");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //年度内の処理か？チェック
    function checkCtrlDay($day)
    {
        $day = str_replace("/","-",$day);

        //年度内の処理に限る
        if((CTRL_YEAR."-04-01" <= $day && $day < (CTRL_YEAR + 1)."-04-01"))
        {
            return true;
        }else{
            $this->setWarning("MSG915","(処理年度範囲外)");
            return false;
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
