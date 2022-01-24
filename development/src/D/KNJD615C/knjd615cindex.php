<?php

require_once('for_php7.php');

require_once('knjd615cModel.inc');
require_once('knjd615cQuery.inc');

class knjd615cController extends Controller {
    var $ModelClassName = "knjd615cModel";
    var $ProgramID      = "KNJD615C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd615c":
                case "change_testkindcd":
                case "change_hr_class":
                case "gakki":
                    $sessionInstance->knjd615cModel();
                    $this->callView("knjd615cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd615cCtl = new knjd615cController;
?>
