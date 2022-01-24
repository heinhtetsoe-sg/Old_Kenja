<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd500kindex.php 56580 2017-10-22 12:35:29Z maeshiro $
require_once('knjd500kModel.inc');
require_once('knjd500kQuery.inc');

class knjd500kController extends Controller
{
    var $ModelClassName = "knjd500kModel";
    var $ProgramID      = "KNJD500K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true )
        {
            switch (trim($sessionInstance->cmd))
            {
                case "main":
                    $this->callView("knjd500kForm1");
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
$knjd500kCtl = new knjd500kController;
//var_dump($_REQUEST);
?>
