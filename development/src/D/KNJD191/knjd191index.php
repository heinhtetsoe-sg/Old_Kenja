<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd191index.php 56586 2017-10-22 12:52:35Z maeshiro $

require_once('knjd191Model.inc');
require_once('knjd191Query.inc');

class knjd191Controller extends Controller {
    var $ModelClassName = "knjd191Model";
    var $ProgramID      = "KNJD191";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd191":
                    $sessionInstance->knjd191Model();
                    $this->callView("knjd191Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd191Ctl = new knjd191Controller;
var_dump($_REQUEST);
?>
