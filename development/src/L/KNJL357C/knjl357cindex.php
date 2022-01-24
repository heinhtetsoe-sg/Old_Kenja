<?php

require_once('for_php7.php');

require_once('knjl357cModel.inc');
require_once('knjl357cQuery.inc');

class knjl357cController extends Controller {
    var $ModelClassName = "knjl357cModel";
    var $ProgramID      = "KNJL357C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl357c":
                    $sessionInstance->knjl357cModel();
                    $this->callView("knjl357cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl357cCtl = new knjl357cController;
//var_dump($_REQUEST);
?>
