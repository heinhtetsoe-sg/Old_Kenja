<?php

// kanji=漢字
// $Id: knjd211index.php 56580 2017-10-22 12:35:29Z maeshiro $

require_once('knjd211Model.inc');
require_once('knjd211Query.inc');

class knjd211Controller extends Controller {
    var $ModelClassName = "knjd211Model";
    var $ProgramID      = "KNJD211";

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
                case "chg_grade":
                    //$sessionInstance->getMainModel();
                    $this->callView("knjd211Form1");
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
$knjd211Ctl = new knjd211Controller;
?>
