<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl327cModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $isGojouOnly;
    var $isGojou;
    var $isCollege;
    var $isWakayama;

    /**
     * メンバ変数の初期化
     */
    function knjl327cModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"	=>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"	    =>  VARS::post("TESTDIV"),     		//入試区分
                             "SHDIV"	    =>  VARS::post("SHDIV"),     		//専併区分
                             "PRINT_DATE"	=>  VARS::post("PRINT_DATE"),       //通知日付
                             "OUTPUT"	    =>  VARS::post("OUTPUT"),           //帳票種類
                             "OUTPUTA"	    =>  VARS::post("OUTPUTA"),          //出力範囲
                             "EXAMNOA"	    =>  VARS::post("EXAMNOA"),          //受験番号
                             "SG_CLASS_A"	=>  VARS::post("SG_CLASS_A"),       //合格通知書の志願者全員の右のコンボ(SクラスまたはGクラスを選択)(S/G志願者用)
                             "OUTPUTB"	    =>  VARS::post("OUTPUTB"),          //出力範囲
                             "EXAMNOB"	    =>  VARS::post("EXAMNOB"),          //受験番号
                             "SHIFT_COURSE"	=>  VARS::post("SHIFT_COURSE"),     //移行先
                             "SHIFT_SEND"	=>  VARS::post("SHIFT_SEND"),       //移行先から送付
                             "OUTPUTC"	    =>  VARS::post("OUTPUTC"),          //出力範囲
                             "EXAMNOC"	    =>  VARS::post("EXAMNOC"),          //受験番号
                             "OUTPUTD"	    =>  VARS::post("OUTPUTD"),          //出力範囲
                             "EXAMNOD"	    =>  VARS::post("EXAMNOD"),          //受験番号
                             "OUTPUTE"	    =>  VARS::post("OUTPUTE"),          //出力範囲
                             "EXAMNOE"	    =>  VARS::post("EXAMNOE"),          //受験番号
                             "OUTPUTF"	    =>  VARS::post("OUTPUTF"),          //出力範囲
                             "EXAMNOF"	    =>  VARS::post("EXAMNOF"),          //受験番号
                             "SINGAKU_DATE"	=>  VARS::post("SINGAKU_DATE"),     //入学日付
                             "VISIT_DATE"	=>  VARS::post("VISIT_DATE"),       //来校日付
                             "VISIT_HOUR"	=>  VARS::post("VISIT_HOUR"),       //来校時間
                             "VISIT_MINUTE"	=>  VARS::post("VISIT_MINUTE")      //来校時間
                            );

        $this->cmd         = VARS::request("cmd");

        //学校コードを取得：五条・和歌山を判断するため
        $rtnRow = knjl327cQuery::getNameMstZ010();
        $this->isGojouOnly = $rtnRow["NAME2"] == "30290053001" ? true : false;
        $this->isGojou = $rtnRow["NAME2"] == "30290053001" || $rtnRow["NAME2"] == "30290086001" ? true : false;
        $this->isCollege = $rtnRow["NAME2"] == "30290086001" ? true : false;
        $this->isWakayama = $rtnRow["NAME2"] == "30300049001" ? true : false;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
