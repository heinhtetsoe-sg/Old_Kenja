<?php

require_once('for_php7.php');

require_once('knjs340aModel.inc');
require_once('knjs340aQuery.inc');

class knjs340aController extends Controller {
    var $ModelClassName = "knjs340aModel";
    var $ProgramID      = "KNJS340A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjs340aModel();
                    $this->callView("knjs340aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjs340aCtl = new knjs340aController;
?>
