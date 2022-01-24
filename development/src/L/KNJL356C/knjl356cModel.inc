<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl356cModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $isWakayama;

    /**
     * メンバ変数の初期化
     */
    function knjl356cModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "GOUKAKUSHA"   =>  VARS::post("GOUKAKUSHA"),       //合格者チェックボックス用
                             "PRINT_TYPE"   =>  VARS::post("PRINT_TYPE"),       //出力選択
                             "EXAMNO"       =>  VARS::post("EXAMNO")            //受験番号
                            );

        $this->cmd         = VARS::request("cmd");

        //学校コードを取得
        $rtnRow = knjl356cQuery::getNameMstZ010();
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
