<?php

require_once('for_php7.php');

require_once('knjm060wModel.inc');
require_once('knjm060wQuery.inc');

class knjm060wController extends Controller {
    var $ModelClassName = "knjm060wModel";
    var $ProgramID      = "KNJM060W";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm060w":
                    $sessionInstance->knjm060wModel();       //コントロールマスタの呼び出し
                    $this->callView("knjm060wForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm060wForm1");
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
$knjm060wCtl = new knjm060wController;
//var_dump($_REQUEST);
?>
