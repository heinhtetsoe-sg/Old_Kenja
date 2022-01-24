<?php

require_once('for_php7.php');

require_once('knjg046cModel.inc');
require_once('knjg046cQuery.inc');

class knjg046cController extends Controller {
    var $ModelClassName = "knjg046cModel";
    var $ProgramID      = "KNJG046C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjg046cModel();
                    $this->callView("knjg046cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjg046cCtl = new knjg046cController;
?>
