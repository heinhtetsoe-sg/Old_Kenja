<?php

require_once('for_php7.php');

require_once('knjd667nModel.inc');
require_once('knjd667nQuery.inc');

class knjd667nController extends Controller {
    var $ModelClassName = "knjd667nModel";
    var $ProgramID      = "KNJD667N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd667n":
                    $sessionInstance->knjd667nModel();
                    $this->callView("knjd667nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd667nCtl = new knjd667nController;
?>
