<?php

require_once('for_php7.php');

require_once('knjm812cModel.inc');
require_once('knjm812cQuery.inc');

class knjm812cController extends Controller {
    var $ModelClassName = "knjm812cModel";
    var $ProgramID      = "KNJM812C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_reg":
                case "knjm812c":
                    $sessionInstance->knjm812cModel();
                    $this->callView("knjm812cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm812cCtl = new knjm812cController;
var_dump($_REQUEST);
?>
