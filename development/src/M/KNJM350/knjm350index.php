<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjm350index.php 56590 2017-10-22 13:01:54Z maeshiro $
require_once('knjm350Model.inc');
require_once('knjm350Query.inc');

class knjm350Controller extends Controller {
    var $ModelClassName = "knjm350Model";
    var $ProgramID      = "KNJM350";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "change":
                case "allcheck":
                case "read":
                    $sessionInstance->getMainModel();
                    $this->callView("knjm350Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm350Ctl = new knjm350Controller;
?>
