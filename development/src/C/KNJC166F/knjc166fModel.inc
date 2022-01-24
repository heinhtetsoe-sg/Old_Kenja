<?php

require_once('for_php7.php');

class knjc166fModel extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $pdfcsvflg;

    function knjc166fModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array(
                            "GRADE"                         =>  VARS::post("GRADE"),                        //学年
                            "DATE"                          =>  VARS::post("DATE"),                         //異動対象日付
                            "OUTPUT_KAIKIN"                 =>  VARS::post("OUTPUT_KAIKIN"),                //皆勤者/皆勤者/遅刻・早退
                            "OUTPUT"                        =>  VARS::post("OUTPUT"),                       //皆勤者/精勤者/遅刻・早退
                            "OUTPUT1_RUIKEI"                =>  VARS::post("OUTPUT1_RUIKEI"),               //皆勤者欠席
                            "KESSEKI"                       =>  VARS::post("KESSEKI"),                      //欠席
                            "CHIKOKU_SOUTAI"                =>  VARS::post("CHIKOKU_SOUTAI"),               //遅刻早退
                            "CHIKOKU"                       =>  VARS::post("CHIKOKU"),                      //遅刻
                            "SOUTAI"                        =>  VARS::post("SOUTAI"),                       //早退
                            "KEKKA"                         =>  VARS::post("KEKKA"),                        //欠課
                            "KEKKA_KESSEKI_CNT"             =>  "",                                         //欠席・遅刻・早退・欠課(イベント発生はモード切替なので、この設定値は引き継がない。)
                            "DBNAME"                        =>  VARS::post("DBNAME")                        //DB変更に備えて
                     );
        $pdfcsvflg = VARS::post("OUTPUTCSV");

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
}
?>
