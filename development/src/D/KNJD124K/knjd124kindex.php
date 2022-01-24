<?php

require_once('for_php7.php');


// kanji=漢字
// $Id: knjd124kindex.php 56580 2017-10-22 12:35:29Z maeshiro $

require_once('knjd124kModel.inc');
require_once('knjd124kQuery.inc');

class knjd124kController extends Controller {
    var $ModelClassName = "knjd124kModel";
    var $ProgramID      = "KNJD124K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "semester":
                case "classcd":
                case "chaircd":
                case "reset":
                    $this->callView("knjd124kForm1");
                   break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
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
$knjd124kCtl = new knjd124kController;
?>
