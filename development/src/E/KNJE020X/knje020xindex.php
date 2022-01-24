<?php

require_once('for_php7.php');

require_once('knje020xModel.inc');
require_once('knje020xQuery.inc');

class knje020xController extends Controller {
    var $ModelClassName = "knje020xModel";
    var $ProgramID      = "KNJE020X";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "updEdit":
                case "edit":
                case "clear":
                    $this->callView("knje020xForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knje020xModel();   //コントロールマスタの呼び出し
                    $this->callView("knje020xForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje020xCtl = new knje020xController;
?>
