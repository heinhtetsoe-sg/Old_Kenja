<?php

require_once('for_php7.php');

require_once('knjl342tModel.inc');
require_once('knjl342tQuery.inc');

class knjl342tController extends Controller {
    var $ModelClassName = "knjl342tModel";
    var $ProgramID      = "KNJL342T";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl342t":
                    $sessionInstance->knjl342tModel();
                    $this->callView("knjl342tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl342tCtl = new knjl342tController;
?>
