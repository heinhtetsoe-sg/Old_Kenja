<?php

require_once('for_php7.php');

require_once('knje011xModel.inc');
require_once('knje011xQuery.inc');

class knje011xController extends Controller {
    var $ModelClassName = "knje011xModel";
    var $ProgramID      = "KNJE011X";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "updEdit":
                case "edit":
                case "clear":
                    $this->callView("knje011xForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knje011xModel();   //コントロールマスタの呼び出し
                    $this->callView("knje011xForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje011xCtl = new knje011xController;
?>
