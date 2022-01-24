<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjb3040index.php 56585 2017-10-22 12:47:53Z maeshiro $
require_once('knjb3040Model.inc');
require_once('knjb3040Query.inc');

class knjb3040Controller extends Controller {
    var $ModelClassName = "knjb3040Model";
    var $ProgramID      = "KNJB3040";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb3040":
                    $sessionInstance->knjb3040Model();
                    $this->callView("knjb3040Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjb3040Ctl = new knjb3040Controller;
//var_dump($_REQUEST);
?>
