<?php

require_once('for_php7.php');

require_once('knjl342cModel.inc');
require_once('knjl342cQuery.inc');

class knjl342cController extends Controller {
    var $ModelClassName = "knjl342cModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl342c":
                    $sessionInstance->knjl342cModel();
                    $this->callView("knjl342cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl342cCtl = new knjl342cController;
//var_dump($_REQUEST);
?>
