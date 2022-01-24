<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd125cindex.php 56581 2017-10-22 12:37:16Z maeshiro $

require_once('knjd125cModel.inc');
require_once('knjd125cQuery.inc');

class knjd125cController extends Controller
{
    public $ModelClassName = "knjd125cModel";
    public $ProgramID      = "KNJD125C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subclasscd":
                case "chaircd":
                case "reset":
                    $this->callView("knjd125cForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
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
$knjd125cCtl = new knjd125cController();
