<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjc010index.php 56585 2017-10-22 12:47:53Z maeshiro $
require_once('knjc010Model.inc');
require_once('knjc010Query.inc');

class knjc010Controller extends Controller {
    var $ModelClassName = "knjc010Model";
    var $ProgramID      = "knjc010";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc010":
                    $sessionInstance->knjc010Model();
                    //$this->callView("knjc010");
                    $this->callView("knjc010Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjc010Ctl = new knjc010Controller;
//var_dump($_REQUEST);
?>
