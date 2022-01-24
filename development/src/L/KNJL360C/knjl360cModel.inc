<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl360cModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $isGojou;
    var $isCollege;
        
    /**
     * メンバ変数の初期化
     */
    function knjl360cModel()
    {
    }

    function init()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "GOUKAKUSHA"   =>  VARS::post("GOUKAKUSHA"),       //合格者チェックボックス用
                             "PRINT_TYPE"   =>  VARS::post("PRINT_TYPE"),       //出力選択
                             "EXAMNO"       =>  VARS::post("EXAMNO"),           //受験番号
                             "SEX"          =>  VARS::post("SEX"),              //性別
                             "CHECK"        =>  VARS::post("CHECK"),            //入学金なし
                             "ENT_DATE"     =>  VARS::post("ENT_DATE"),         //入学手続き日付
                             "STR_DATE"     =>  VARS::post("STR_DATE"),         //開始期間
                             "END_DATE"     =>  VARS::post("END_DATE"),         //終了期間
                             "PRINT_TIME"   =>  VARS::post("PRINT_TIME"),       //時間
                             //五条の制定学用品代用
                             "STR_DATE2"     =>  VARS::post("STR_DATE2"),       //開始期間
                             "END_DATE2"     =>  VARS::post("END_DATE2"),       //終了期間
                             "PRINT_TIME2"   =>  VARS::post("PRINT_TIME2")      //時間
                            );

        $this->cmd         = VARS::request("cmd");
        
        //初期値設定
        $this->field["PRINT_TIME"] = 15;
        $this->field["PRINT_TIME2"] = 15;

        //学校コードを取得：五条・和歌山・カレッジを判断するため
        $rtnRow = knjl360cQuery::getNameMstZ010();
        $this->isGojou   = $rtnRow["NAME2"] == "30290053001" ? true : false;
        $this->isCollege = $rtnRow["NAME2"] == "30290086001" ? true : false;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
