<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjb0050index.php 56585 2017-10-22 12:47:53Z maeshiro $
require_once('knjb0050Model.inc');
require_once('knjb0050Query.inc');

class knjb0050Controller extends Controller {
    var $ModelClassName = "knjb0050Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {

                case "":
                case "knjb0050":
                    $this->callView("knjb0050Form1");
                    break 2;

                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjb0050Ctl = new knjb0050Controller;
//var_dump($_REQUEST);
?>
