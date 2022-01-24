<?php

require_once('for_php7.php');

require_once('knjl345cModel.inc');
require_once('knjl345cQuery.inc');

class knjl345cController extends Controller {
    var $ModelClassName = "knjl345cModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl345c":
                    $sessionInstance->knjl345cModel();
                    $this->callView("knjl345cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl345cCtl = new knjl345cController;
//var_dump($_REQUEST);
?>
