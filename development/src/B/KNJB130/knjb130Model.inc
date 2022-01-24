<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjb130Model.inc 56585 2017-10-22 12:47:53Z maeshiro $

class knjb130Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjb130Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("RADIO"            =>  VARS::post("RADIO"),                 //ラジオ選択データ
                             "DATE1"            =>  VARS::post("DATE1"),                 //指定日付FROM
                             "DATE2"            =>  VARS::post("DATE2"),                 //指定日付TO
                             "OUTPUT"           =>  VARS::post("OUTPUT"),                //出力順
                             "SECTION_CD_NAME1" =>  VARS::post("SECTION_CD_NAME1"),      //所属コンボボックス選択データ
                             "SECTION_CD_NAME2" =>  VARS::post("SECTION_CD_NAME2"),      //所属コンボボックス選択データ
                             "TITLE"            =>  VARS::post("TITLE"),                 //時間割コンボボックス選択データ
                             "DBNAME"           =>  VARS::post("DBNAME"),                //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
