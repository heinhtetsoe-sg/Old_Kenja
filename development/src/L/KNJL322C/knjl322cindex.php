<?php

require_once('for_php7.php');

require_once('knjl322cModel.inc');
require_once('knjl322cQuery.inc');

class knjl322cController extends Controller {
    var $ModelClassName = "knjl322cModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl322c":
                    $sessionInstance->knjl322cModel();
                    $this->callView("knjl322cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl322cCtl = new knjl322cController;
//var_dump($_REQUEST);
?>
