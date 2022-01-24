<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd501kindex.php 56580 2017-10-22 12:35:29Z maeshiro $
require_once('knjd501kModel.inc');
require_once('knjd501kQuery.inc');

class knjd501kController extends Controller
{
    var $ModelClassName = "knjd501kModel";
    var $ProgramID      = "KNJD501K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true )
        {
            switch (trim($sessionInstance->cmd))
            {
                case "main":
                    $this->callView("knjd501kForm1");
                   break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "cancel":
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd501kCtl = new knjd501kController;
//var_dump($_REQUEST);
?>
