<?php

require_once('for_php7.php');

// kanji=����
// $Id: knjb0056index.php,v 1.2 2009/06/02 00:17:43 maesiro Exp $
require_once('knjb0056Model.inc');
require_once('knjb0056Query.inc');

class knjb0056Controller extends Controller {
    var $ModelClassName = "knjb0056Model";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {

                case "":
                case "knjb0056":
                    $this->callView("knjb0056Form1");
                    break 2;

                default:
                    $sessionInstance->setError(new PEAR_Error("���Ή��̃A�N�V����{$sessionInstance->cmd}�ł�"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb0056Ctl = new knjb0056Controller;
//var_dump($_REQUEST);
?>
