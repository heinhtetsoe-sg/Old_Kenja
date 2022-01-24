<?php

require_once('for_php7.php');

require_once('knjd614Model.inc');
require_once('knjd614Query.inc');

class knjd614Controller extends Controller {
    var $ModelClassName = "knjd614Model";
    var $ProgramID      = "KNJD614";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd614":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd614Model();        //コントロールマスタの呼び出し
                    $this->callView("knjd614Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd614Form1");
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
$knjd614Ctl = new knjd614Controller;
//var_dump($_REQUEST);
?>
