<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjb036Model extends Model {
    var $cmd;
    var $executedate;                       //カレンダーコントロール用データ
    var $jikanwari;
    var $semester;
    var $seq;
    var $faccd;

    /**
     * メンバ変数の初期化
     */
    function knjb036Model()
    {
    }

    function init()
    {
        $this->jikanwari = VARS::post("jikanwari");
        $this->faccd 	= VARS::post("faccd");
        $this->semester = VARS::post("semester");
        $this->seq 		= VARS::post("seq");
        $this->executedate = VARS::post("executedate");
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
