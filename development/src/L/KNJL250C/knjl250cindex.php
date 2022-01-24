<?php

require_once('for_php7.php');

require_once('knjl250cModel.inc');
require_once('knjl250cQuery.inc');

class knjl250cController extends Controller {
    var $ModelClassName = "knjl250cModel";
    var $ProgramID      = "KNJL250C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl250c":
                    $sessionInstance->knjl250cModel();
                    $this->callView("knjl250cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl250cCtl = new knjl250cController;
//var_dump($_REQUEST);
?>
