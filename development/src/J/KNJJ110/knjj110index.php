<?php

require_once('for_php7.php');

require_once('knjj110Model.inc');
require_once('knjj110Query.inc');

class knjj110Controller extends Controller {
    var $ModelClassName = "knjj110Model";
    var $ProgramID      = "KNJJ110";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj110":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjj110Model();       //コントロールマスタの呼び出し
                    $this->callView("knjj110Form1");
                    exit;
                case "csv":         //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjj110Form1");
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
$knjj110Ctl = new knjj110Controller;
//var_dump($_REQUEST);
?>
