<?php

require_once('for_php7.php');

// kanji=����
// $Id: knjb0060index.php,v 1.1 2004/10/13 13:29:39 takaesu Exp $
require_once('knjb0060Model.inc');
require_once('knjb0060Query.inc');

class knjb0060Controller extends Controller {
    var $ModelClassName = "knjb0060Model";
    var $ProgramID      = "KNJB0060";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb0060":
                    $sessionInstance->knjb0060Model();
                    $this->callView("knjb0060Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("���Ή��̃A�N�V����{$sessionInstance->cmd}�ł�"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjb0060Ctl = new knjb0060Controller;
//var_dump($_REQUEST);
?>
