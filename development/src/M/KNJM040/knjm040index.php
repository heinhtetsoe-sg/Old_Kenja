<?php

require_once('for_php7.php');

require_once('knjm040Model.inc');
require_once('knjm040Query.inc');

class knjm040Controller extends Controller {
    var $ModelClassName = "knjm040Model";
    var $ProgramID      = "KNJM040";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm040":
                    $sessionInstance->knjm040Model();       //コントロールマスタの呼び出し
                    $this->callView("knjm040Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm040Form1");
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
$knjm040Ctl = new knjm040Controller;
//var_dump($_REQUEST);
?>
