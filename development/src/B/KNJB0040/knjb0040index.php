<?php

require_once('for_php7.php');

// kanji=����
// $Id: knjb0040index.php,v 1.3 2004/10/13 12:59:51 tamura Exp $
require_once('knjb0040Model.inc');
require_once('knjb0040Query.inc');

class knjb0040Controller extends Controller {
    var $ModelClassName = "knjb0040Model";
    var $ProgramID      = "KNJB0040";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb0040":
                    $sessionInstance->knjb0040Model();
                    $this->callView("knjb0040Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("���Ή��̃A�N�V����{$sessionInstance->cmd}�ł�"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjb0040Ctl = new knjb0040Controller;
//var_dump($_REQUEST);
?>
