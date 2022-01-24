<?php

require_once('for_php7.php');

require_once('knjl353Model.inc');
require_once('knjl353Query.inc');

class knjl353Controller extends Controller {
    var $ModelClassName = "knjl353Model";
    var $ProgramID      = "KNJL353";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl353":
                case "read":
                    $sessionInstance->knjl353Model();
                    $this->callView("knjl353Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl353Ctl = new knjl353Controller;
//var_dump($_REQUEST);
?>
