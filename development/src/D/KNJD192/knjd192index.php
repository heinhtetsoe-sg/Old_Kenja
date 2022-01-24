<?php

require_once('for_php7.php');

require_once('knjd192Model.inc');
require_once('knjd192Query.inc');

class knjd192Controller extends Controller {
    var $ModelClassName = "knjd192Model";
    var $ProgramID      = "KNJD192";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd192":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd192Model();        //コントロールマスタの呼び出し
                    $this->callView("knjd192Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd192Form1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd192Ctl = new knjd192Controller;
//var_dump($_REQUEST);
?>
