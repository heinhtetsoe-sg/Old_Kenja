<?php

require_once('for_php7.php');

require_once('knja120axModel.inc');
require_once('knja120axQuery.inc');

class knja120axController extends Controller {
    var $ModelClassName = "knja120axModel";
    var $ProgramID      = "KNJA120AX";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "updEdit":
                case "edit":
                case "clear":
                    $this->callView("knja120axForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knja120axModel();   //コントロールマスタの呼び出し
                    $this->callView("knja120axForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja120axCtl = new knja120axController;
?>
