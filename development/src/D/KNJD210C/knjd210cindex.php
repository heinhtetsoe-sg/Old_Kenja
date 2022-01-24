<?php

require_once('for_php7.php');


// kanji=漢字

require_once('knjd210cModel.inc');
require_once('knjd210cQuery.inc');

class knjd210cController extends Controller {
    var $ModelClassName = "knjd210cModel";
    var $ProgramID      = "KNJD210C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    //$sessionInstance->getMainModel();
                    $this->callView("knjd210cForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjd210cCtl = new knjd210cController;
?>
