<?php
require_once('knjl340kModel.inc');
require_once('knjl340kQuery.inc');

class knjl340kController extends Controller {
    var $ModelClassName = "knjl340kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl340k":
                    $sessionInstance->knjl340kModel();
                    $this->callView("knjl340kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl340kCtl = new knjl340kController;
var_dump($_REQUEST);
?>
