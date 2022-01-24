<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: d4c0284f6cd9642fc7f9745d2c5f96fe4a0f029e $
require_once('knjb3041Model.inc');
require_once('knjb3041Query.inc');

class knjb3041Controller extends Controller {
    var $ModelClassName = "knjb3041Model";
    var $ProgramID      = "KNJB3041";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb3041":
                    $sessionInstance->knjb3041Model();
                    $this->callView("knjb3041Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjb3041Ctl = new knjb3041Controller;
//var_dump($_REQUEST);
?>
