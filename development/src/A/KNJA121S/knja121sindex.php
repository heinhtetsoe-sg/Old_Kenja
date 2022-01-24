<?php

require_once('for_php7.php');

require_once('knja121sModel.inc');
require_once('knja121sQuery.inc');

class knja121sController extends Controller {
    var $ModelClassName = "knja121sModel";
    var $ProgramID      = "KNJA121S";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "https":
                case "knja121s":
                case "knja121s2":
                    $sessionInstance->knja121sModel();
                    $this->callView("knja121sForm1");
                    exit;
                case "sslExe":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knja121s");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knja121sCtl = new knja121sController;
var_dump($_REQUEST);
?>
