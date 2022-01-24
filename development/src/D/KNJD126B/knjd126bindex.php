<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd126bindex.php 56581 2017-10-22 12:37:16Z maeshiro $

require_once('knjd126bModel.inc');
require_once('knjd126bQuery.inc');

class knjd126bController extends Controller
{
    public $ModelClassName = "knjd126bModel";
    public $ProgramID      = "KNJD126B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subclasscd":
                case "chaircd":
                case "reset":
                    $this->callView("knjd126bForm1");
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
$knjd126bCtl = new knjd126bController();
