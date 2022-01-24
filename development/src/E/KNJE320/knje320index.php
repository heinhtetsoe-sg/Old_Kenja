<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje320index.php 56587 2017-10-22 12:54:51Z maeshiro $

require_once('knje320Model.inc');
require_once('knje320Query.inc');

class knje320Controller extends Controller {
    var $ModelClassName = "knje320Model";
    var $ProgramID      = "KNJE320";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje320":
                    $sessionInstance->knje320Model();
                    $this->callView("knje320Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje320Ctl = new knje320Controller;
?>
