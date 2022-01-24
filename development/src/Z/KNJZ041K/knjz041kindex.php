<?php
require_once('knjz041kModel.inc');
require_once('knjz041kQuery.inc');

class knjz041kController extends Controller {
    var $ModelClassName = "knjz041kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjz041k":
                    $sessionInstance->knjz041kModel();
                    $this->callView("knjz041kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjz041kCtl = new knjz041kController;
var_dump($_REQUEST);
?>
