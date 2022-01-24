<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje311index.php 56587 2017-10-22 12:54:51Z maeshiro $

require_once('knje311Model.inc');
require_once('knje311Query.inc');

class knje311Controller extends Controller {
    var $ModelClassName = "knje311Model";
    var $ProgramID      = "KNJE311";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "knje311":
                    $this->callView("knje311Form1");
                    break 2;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje311Form1");
                    }
                    break 2;
                case "update":                   
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knje311");
                    break 1;
                case "":
                    $sessionInstance->knje311Model();
                    $this->callView("knje311Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje311Ctl = new knje311Controller;
?>
