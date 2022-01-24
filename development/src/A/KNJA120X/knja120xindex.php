<?php

require_once('for_php7.php');

require_once('knja120xModel.inc');
require_once('knja120xQuery.inc');

class knja120xController extends Controller {
    var $ModelClassName = "knja120xModel";
    var $ProgramID      = "KNJA120X";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "updEdit":
                case "edit":
                case "clear":
                    $this->callView("knja120xForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knja120xModel();   //コントロールマスタの呼び出し
                    $this->callView("knja120xForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja120xCtl = new knja120xController;
?>
